package com.onmobile.apps.ringbacktones.webservice.implementation.telefonica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class TelefonicaPeruRBTProcessor extends TelefonicaRBTProcessor {

	private static Logger logger = Logger
			.getLogger(TelefonicaPeruRBTProcessor.class);
	private static List<String> modesForNoNotifSMSForDoubleOptIn = new ArrayList<String>();
	private static boolean useDoubleOptInSMSNotification = false;
	static {
		String doubelOptInModesForNoNotificationSMS = RBTParametersUtils
				.getParamAsString(iRBTConstant.WEBSERVICE,
						DOUBLE_OPT_IN_MODES_FOR_NO_NOTIFICATION_SMS, null);
		useDoubleOptInSMSNotification = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.WEBSERVICE,
						USE_OPT_IN_SMS_NOTIFICATION, "FALSE");
		logger.info("DOUBLE_OPT_IN_MODES_FOR_NO_NOTIFICATION_SMS: "
				+ doubelOptInModesForNoNotificationSMS);
		if (doubelOptInModesForNoNotificationSMS != null) {
			modesForNoNotifSMSForDoubleOptIn = ListUtils.convertToList(
					doubelOptInModesForNoNotificationSMS, ",");
			logger.info("modesForNoNotifSMSForDoubleOptIn: "
					+ modesForNoNotifSMSForDoubleOptIn);
		}
	}

	@Override
	public String processActivation(WebServiceContext task) {
		logger.debug("processActivation");
		String tpcgId = task.getString(iRBTConstant.EXTRA_INFO_TPCGID);
		String mode = task.getString(MODE);
		String response = super.processActivation(task);
		if (useDoubleOptInSMSNotification && tpcgId == null && Utility.isModeConfiguredForConsent(mode)
				&& !modesForNoNotifSMSForDoubleOptIn.contains(mode)
				&& !task.containsKey(param_requestFromSelection)
				&& response != null && response.equalsIgnoreCase("SUCCESS")) {
			Subscriber subscriber = null;
			try {
				subscriber = DataUtils.getSubscriber(task);
			} catch (RBTException e) {
				logger.error("Exception caught", e);
			}
			String language = subscriber.language();
			HashMap<String, String> map = new HashMap<String, String>();
			String smsText = CacheManagerUtil
					.getSmsTextCacheManager()
					.getSmsText(
							WebServiceConstants.DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS,
							language);
			if (smsText == null) {
				smsText = WebServiceConstants.m_DoubleOptInActivationNotficationSms;
			}
			String subscriptionClass = subscriber.subscriptionClass();
			SubscriptionClass subClass = CacheManagerUtil
					.getSubscriptionClassCacheManager().getSubscriptionClass(
							subscriptionClass);
			String subAmount = null;
			if (subClass != null) {
				subAmount = subClass.getSubscriptionAmount();
			}
			map.put("SMS_TEXT", smsText);
			map.put("CIRCLE_ID", subscriber.circleID());
			map.put("ACT_AMT", subAmount == null ? "" : subAmount);
			smsText = SmsProcessor.finalizeSmsText(map);
			logger.info("smsText: " + smsText);
			sendSMS(subscriber, smsText);
		}
		return response;
	}

	@Override
	public String processSelection(WebServiceContext task) {
		logger.debug("processSelection");
		String tpcgId = task.getString(iRBTConstant.EXTRA_INFO_TPCGID);
		String mode = task.getString(MODE);
		String response = super.processSelection(task);
		if (useDoubleOptInSMSNotification && tpcgId == null && Utility.isModeConfiguredForConsent(mode)
				&& !modesForNoNotifSMSForDoubleOptIn.contains(mode) && response != null
				&& response.equalsIgnoreCase("SUCCESS")) {
			Subscriber subscriber = null;
			try {
				subscriber = DataUtils.getSubscriber(task);
			} catch (RBTException e) {
				logger.error("Exception caught", e);
			}
			String language = subscriber.language();
			HashMap<String, String> map = new HashMap<String, String>();
			String smsText = null;
			String subAmount = null;
			if (task.containsKey(iRBTConstant.param_isSubConsentInserted)) {
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(WebServiceConstants.DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS, language);
				if (smsText == null) {
					smsText = WebServiceConstants.m_DoubleOptInComboNotficationSms;
				}
				String subscriptionClass = subscriber.subscriptionClass();
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(subscriptionClass);
				if (subClass != null) {
					subAmount = subClass.getSubscriptionAmount();
				}
			} else {
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(WebServiceConstants.DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS, language);
				if (smsText == null) {
					smsText = WebServiceConstants.m_DoubleOptInSelectionNotficationSms;
				}
			}

			String categoryId = task.getString(param_categoryID);

			map.put("SMS_TEXT", smsText);
			map.put("CIRCLE_ID", subscriber.circleID());
			String clipId = task.getString(param_clipID);
			String clipName = null;
			String artist = null;
			String album = null;
			String promoId = null;
			Category category = null;
			if (categoryId != null) {
				category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryId));
				if (com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getCategoryTpe())) {
					Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(category.getCategoryId());
					if (clips != null && clips.length > 0) {
						clipId = clips[0].getClipId() + "";
					}
				}
			}

			if (clipId != null) {
				Clip clip = RBTCacheManager.getInstance().getClip(clipId);
				if (clip != null) {
					clipName = clip.getClipName();
					artist = clip.getArtist();
					album = clip.getAlbum();
					promoId = clip.getClipPromoId();
					if (category != null && com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(category.getCategoryTpe())) {
						clipName = category.getCategoryName();
						promoId = category.getCategoryPromoId();
					}

				}
			}
			String classType = task.getString("RECENT_CLASS_TYPE");
			String selAlmount = null;
			if (classType != null) {
				ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
				if (chargeClass != null) {
					selAlmount = chargeClass.getAmount();
				}
			}
			String callerId = task.getString(param_callerID);
			map.put("SONG_NAME", clipName == null ? "" : clipName);
			map.put("ARTIST", artist == null ? "" : artist);
			map.put("PROMO_ID", promoId == null ? "" : promoId);
			map.put("ACT_AMT", subAmount == null ? "" : subAmount);
			map.put("ALBUM", album == null ? "" : album);
			map.put("SEL_AMT", selAlmount == null ? "" : selAlmount);
			// album, promoId, pricePoint -> from subscriber's subClass
			map.put("CALLER_ID",
					callerId == null ? Processor.param(SMS, SmsProcessor.SMS_TEXT_FOR_ALL, "all") : callerId);
			smsText = SmsProcessor.finalizeSmsText(map);
			logger.info("smsText: " + smsText);
			sendSMS(subscriber, smsText);
		}
		return response;
	}

	static private void sendSMS(Subscriber subscriber, String sms) {
		try {
			if (sms != null) {
				String subscriptionClassOperatorNameMap = RBTParametersUtils
						.getParamAsString("COMMON",
								"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.getSenderNumberbyType("GATHERER",
								subscriber.circleID(),
								"DOUBLE_OPT_IN_SENDER_NO");
				if (senderNumber == null) {
					senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
							.getSenderNumberbyType("GATHERER",
									subscriber.circleID(), "SENDER_NO");
				}
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.getBrandName(subscriber.circleID());
				if (subscriptionClassOperatorNameMap != null) {
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
							.findNReplaceAll(sms, "%SENDER_NO", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
							.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
				Tools.sendSMS(senderNumber, subscriber.subID(), sms, false);
			}
		} catch (Exception e) {

		}
	}
}