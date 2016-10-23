package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class RBTSendSMSDaemon implements Runnable, WebServiceConstants {

	private DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
	private DoubleConfirmationRequestBean selDoubleConfirmationRequestBean = null;
	private static Logger logger = Logger.getLogger(RBTSendSMSDaemon.class);
	private int maxRetry = 1;
	private static int statusForConsentPending = 0;
	private static int statusAfterSuccessfulHit = 1;
	private static int statusAfterConsentFailure = 3;
	private static int comboselstatusAfterSuccessfulHit = 1;
	private RBTDBManager rbtDBManager = null;
	private String smsText = null;
	private int successTime = 60;
	private int failureTime = 10;
	private String defaultLanguage = "eng";

	public RBTSendSMSDaemon(DoubleConfirmationRequestBean doubleConfirmationRequestBean) {
		if (doubleConfirmationRequestBean == null) {
			logger.info("doubleConfirmationRequestBean cannot be null.");
			throw new IllegalArgumentException("doubleConfirmationRequestBean cannot be null.");
		}
		this.doubleConfirmationRequestBean = doubleConfirmationRequestBean;
		init();
	}

	private void init() {
		rbtDBManager = RBTDBManager.getInstance();
		maxRetry = RBTParametersUtils.getParamAsInt("DAEMON", "DOUBLE_OPT_IN_SMS_REQUEST_MAX_RETRY", 1);
		successTime = RBTParametersUtils.getParamAsInt("DAEMON", "DOUBLE_OPT_IN_SMS_REQUEST_RETRY_TIME_AFTER_SUCCESS",
				60);
		failureTime = RBTParametersUtils.getParamAsInt("DAEMON", "DOUBLE_OPT_IN_SMS_REQUEST_RETRY_TIME_AFTER_FAILURE",
				10);

	}

	@Override
	public void run() {
		logger.info("Inside run RBTSendSMSDaemon: doubleConfirmationRequestBean : " + doubleConfirmationRequestBean);
		String requestType = doubleConfirmationRequestBean.getRequestType();
		// Date requestTime = doubleConfirmationRequestBean.getRequestTime();
		int consentStatus = doubleConfirmationRequestBean.getConsentStatus();
		int retryCount = getRetryCount();
		String subscriberId = doubleConfirmationRequestBean.getSubscriberID();
		String transId = doubleConfirmationRequestBean.getTransId();
		String extraInfo = doubleConfirmationRequestBean.getExtraInfo();
		String mode = doubleConfirmationRequestBean.getMode().toUpperCase();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		String response = null;
		Date date = new Date();
		boolean isComboReq = false;
		String selTransId = null;
		String clipWavFile = null;
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		logger.info("RBTSendSMSDaemon: requestType : " + requestType + " subscriberId:" + subscriberId + "subscriber: "
				+ subscriber + "retryCount: " + retryCount);
		if (extraInfo != null) {
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID")) {
				selTransId = extraInfoMap.get("TRANS_ID");
				try {
					selDoubleConfirmationRequestBean = rbtDBManager.getConsentRecordForStatusNMsisdnNTransId("1", subscriberId, selTransId);
				} catch (OnMobileException e) {
					e.printStackTrace();
				}
				isComboReq = true;
				if(selDoubleConfirmationRequestBean != null){
					clipWavFile = selDoubleConfirmationRequestBean.getWavFileName();
				}

			}
		}
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		if(!isComboReq || clipWavFile == null){
			clipWavFile = doubleConfirmationRequestBean.getWavFileName();
		}
		Clip clip = null;
		if (clipWavFile != null) {
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(clipWavFile);
		}
		if (clip != null) {
			selectionRequest.setClipID("" + clip.getClipId());
		}
		int categoryId = doubleConfirmationRequestBean.getCategoryID();
		if(isComboReq && categoryId == 0){
			categoryId = selDoubleConfirmationRequestBean.getCategoryID();
		}
		if (categoryId != 0) {
			selectionRequest.setCategoryID(categoryId + "");
		}
		ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequest);
		String sel_amount = "0,00";
		if (chargeClass != null) {
			sel_amount = chargeClass.getAmount();
		}

		if (retryCount >= maxRetry) {
			consentStatus = statusAfterConsentFailure;
			try {
				logger.info("RBTSendSMSDaemon: maximum retry limit reached moving to consentStatus: " + consentStatus);
				rbtDBManager.updateConsentStatusOfConsentRecord(subscriberId, transId, String.valueOf(consentStatus));
				if (isComboReq) {
					logger.info("RBTSendSMSDaemon: maximum retry limit reached Updating selection of combo request for"
							+ subscriberId);
					rbtDBManager.updateConsentStatusOfConsentRecord(subscriberId, selTransId,
							String.valueOf(consentStatus));
				}
			} catch (OnMobileException e) {
				logger.info("Updating failure");
			}
		} else if ("ACT".equalsIgnoreCase(requestType) && (subscriber == null || !Utility.isSubActive(subscriber))
				&& (retryCount < maxRetry)) {
			defaultLanguage = (subscriber != null && subscriber.getLanguage() != null) ? subscriber.getLanguage()
					: defaultLanguage;
			smsText = CacheManagerUtil.getSmsTextCacheManager()
					.getSmsText(DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS + "_"+mode, defaultLanguage);
			if (smsText == null) {
				smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS,
						defaultLanguage);
			}
			logger.info("RBTSATDaemon: requestType : " + requestType);
			if (isComboReq) {
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS + "_"+mode,defaultLanguage);
				if (smsText == null) {
					smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS,
							defaultLanguage);
				}
			}
			smsText = getSMStext(subscriber, smsText, sel_amount);
			logger.info("RBTSATDaemon: offerText : " + smsText);
			response = sendSMS(subscriber, smsText);

		} else if ("SEL".equalsIgnoreCase(requestType) && (subscriber != null && Utility.isSubActive(subscriber))
				&& (retryCount < maxRetry)) {
			defaultLanguage = ((subscriber != null) && subscriber.getLanguage() != null) ? subscriber.getLanguage()
					: defaultLanguage;
			smsText = CacheManagerUtil.getSmsTextCacheManager()
					.getSmsText(DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS + "_"+mode, defaultLanguage);
			if (smsText == null) {
				smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS,
						defaultLanguage);
			}
			smsText = getSMStext(subscriber, smsText, sel_amount);
			logger.info("RBTSATDaemon: offerText : " + smsText);
			response = sendSMS(subscriber, smsText);
		}

		if (null != response) {
			if (response.equalsIgnoreCase("SUCCESS")) {
				try {
					logger.info("RBTSATDaemon: updating consentStatus: " + consentStatus);
					consentStatus = statusAfterSuccessfulHit;
					date.setMinutes(date.getMinutes() + successTime);
					extraInfo = updateRetryCount(extraInfo);
					rbtDBManager.updateExtraInfoAndStatusWithReqTime(subscriberId, transId, extraInfo,
							String.valueOf(consentStatus), date);
					if (isComboReq) {
						logger.info("RBTSATDaemon: Updating selection of combo request for" + subscriberId);
						rbtDBManager.updateExtraInfoAndStatusWithReqTime(subscriberId, selTransId, "null",
								String.valueOf(comboselstatusAfterSuccessfulHit), date);
					}
				} catch (OnMobileException e) {
					logger.info("Updating failure");
				}
			} else {
				try {
					logger.info("RBTSATDaemon:  updating consentStatus: " + consentStatus);
					date.setMinutes(date.getMinutes() + failureTime);
					// if(response.equalsIgnoreCase("FAILURE")){
					// date.setMinutes(date.getMinutes() + successTime);
					// }
					extraInfo = updateRetryCount(extraInfo);
					rbtDBManager.updateExtraInfoAndStatusWithReqTime(subscriberId, transId, extraInfo, null, date);
					if (isComboReq) {
						logger.info("RBTSATDaemon: Updating selection of combo request for" + subscriberId);
						rbtDBManager.updateExtraInfoAndStatusWithReqTime(subscriberId, selTransId, "null", null, date);
					}
				} catch (OnMobileException e) {
					logger.info("Updating failure");
				}

			}
		}

	}

	private int getRetryCount() {
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(doubleConfirmationRequestBean.getExtraInfo());
		if (extraInfoMap != null && extraInfoMap.containsKey("RETRY_COUNT")) {
			return Integer.parseInt(extraInfoMap.get("RETRY_COUNT"));
		}

		return -1;
	}

	static private String sendSMS(Subscriber subscriber, String sms) {
		String response = "FAILURE";
		try {
			if (sms != null) {
				String subscriptionClassOperatorNameMap = RBTParametersUtils.getParamAsString("COMMON",
						"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "DOUBLE_OPT_IN_SENDER_NO");
				if (senderNumber == null) {
					senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
							.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "SENDER_NO");
				}
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.getBrandName(subscriber.getCircleID());
				if (subscriptionClassOperatorNameMap != null) {
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%SENDER_NO",
							senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms,
							"%BRAND_NAME", brandName);
				}
				if (Tools.sendSMS(senderNumber, subscriber.getSubscriberID(), sms, false)) {
					response = "SUCCESS";
				}
			}
		} catch (Exception e) {
			logger.error("Exceprion sending sms for Subscriber : " + subscriber.getSubscriberID());
			return response;
		}
		return response;
	}

	private String getSMStext(Subscriber subscriber, String smsText , String sel_amount) {
		HashMap<String, String> map = new HashMap<String, String>();
		String subscriptionClass = subscriber.getSubscriptionClass();
		SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager()
				.getSubscriptionClass(subscriptionClass);
		String subAmount = null;
		if (subClass != null) {
			subAmount = subClass.getSubscriptionAmount();
		}
		map.put("SEL_AMT", sel_amount);
		map.put("SMS_TEXT", smsText);
		map.put("CIRCLE_ID", subscriber.getCircleID());
		map.put("ACT_AMT", subAmount == null ? "" : subAmount);
		smsText = SmsProcessor.finalizeSmsText(map);
		logger.info("smsText: " + smsText);
		return smsText;
	}

	private String updateRetryCount(String extraInfo) {
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if (extraInfoMap == null) {
			extraInfoMap = new HashMap<String, String>();
			extraInfoMap.put("RETRY_COUNT", "0");
		} else if (extraInfoMap != null && !extraInfoMap.containsKey("RETRY_COUNT")) {
			extraInfoMap.put("RETRY_COUNT", "0");
		} else if (extraInfoMap.containsKey("RETRY_COUNT")) {
			extraInfoMap.put("RETRY_COUNT", String.valueOf(Integer.parseInt(extraInfoMap.get("RETRY_COUNT")) + 1));
		}
		extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		return extraInfo;

	}

}
