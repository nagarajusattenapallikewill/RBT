package com.onmobile.apps.ringbacktones.webservice.implementation.du;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

public class DuSelectionRBTInformation extends BasicRBTInformation {

	private static Logger logger = Logger
			.getLogger(DuSelectionRBTInformation.class);

	/**
	 * @throws ParserConfigurationException
	 */
	public DuSelectionRBTInformation() throws ParserConfigurationException {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #
	 * getWebServiceSubscriberObject(com.onmobile.apps.ringbacktones.webservice.
	 * common.Task, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected WebServiceSubscriber getWebServiceSubscriberObject(
			WebServiceContext task, Subscriber subscriber) {
		WebServiceSubscriber webServiceSubscriber = super
				.getWebServiceSubscriberObject(task, subscriber);

		String language = webServiceSubscriber.getLanguage();
		if (language == null) {
			language = DuUtility.getUserLanguage(task);
			if (language != null) {
				String subscriberStatus = webServiceSubscriber.getStatus();
				if (!subscriberStatus.equalsIgnoreCase(NEW_USER))
					rbtDBManager.setSubscriberLanguage(
							webServiceSubscriber.getSubscriberID(), language);
			} else {
				Parameters defaultLangParam = parametersCacheManager
						.getParameter(iRBTConstant.COMMON, "DEFAULT_LANGUAGE",
								"eng");
				language = defaultLangParam.getValue().trim();
			}

			webServiceSubscriber.setLanguage(language);
			logger.info("RBT:: webServiceSubscriber: " + webServiceSubscriber);
		}

		return webServiceSubscriber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation
	 * #canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID,
			String contentID) {
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail == null || !subscriberDetail.isValidSubscriber())
			return INVALID;

		Subscriber caller = rbtDBManager.getSubscriber(callerID);
		if (rbtDBManager.isSubscriberActivationPending(caller))
			return GIFTEE_ACT_PENDING;
		if (rbtDBManager.isSubscriberDeactivationPending(caller))
			return GIFTEE_DEACT_PENDING;

		if (caller != null
				&& (caller.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE)
						|| caller.subYes().equals(
								iRBTConstant.STATE_SUSPENDED_INIT) || caller
						.subYes().equals(iRBTConstant.STATE_SUSPENDED)))
			return TECHNICAL_DIFFICULTIES;

		if (contentID == null && DataUtils.isUserActivatedByGift(caller))
			return GIFTEE_GIFT_IN_USE;

		ViralSMSTable[] viralSMSEntries = rbtDBManager
				.getViralSMSByCaller(callerID);
		if (viralSMSEntries != null) {
			for (ViralSMSTable viralSMSEntry : viralSMSEntries) {
				if (contentID == null) {
					if (DataUtils.isServiceGiftInPending(viralSMSEntry, caller))
						return GIFTEE_GIFT_ACT_PENDING;

					if (DataUtils.isServiceGiftInUse(viralSMSEntry, caller))
						return GIFTEE_GIFT_IN_USE;
				} else {
					if (DataUtils.isSongGiftInPending(contentID, viralSMSEntry,
							caller))
						return EXISTS_IN_GIFTEE_LIBRAY;

					if (rbtDBManager.isSubscriberDeactivated(caller)
							&& DataUtils.isServiceGiftInPending(viralSMSEntry,
									caller))
						return GIFTEE_GIFT_ACT_PENDING;
				}
			}
		}

		boolean isClip = true;
		String clipRbtWavFile = null;
		int categoryID = -1;
		if (contentID != null) {
			if (contentID.startsWith("C")) {
				isClip = false;
				contentID = contentID.substring(1);
				categoryID = Integer.parseInt(contentID);
			} else {
				int clipID = Integer.parseInt(contentID);
				Clip clip = rbtCacheManager.getClip(clipID);
				clipRbtWavFile = clip.getClipRbtWavFile();
			}
		}

		if (contentID != null && !rbtDBManager.isSubscriberDeactivated(caller)) {
			SubscriberStatus[] settings = rbtDBManager
					.getAllActiveSubSelectionRecords(callerID);
			if (settings != null) {
				for (SubscriberStatus subscriberStatus : settings) {
					if ((isClip && subscriberStatus.subscriberFile().equals(
							clipRbtWavFile))
							|| subscriberStatus.categoryID() == categoryID) {
						String selStatus = subscriberStatus.selStatus();
						if (selStatus
								.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
								|| selStatus
										.equals(iRBTConstant.STATE_ACTIVATION_PENDING)
								|| selStatus
										.equals(iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
								|| selStatus
										.equals(iRBTConstant.STATE_ACTIVATED)
								|| selStatus
										.equals(iRBTConstant.STATE_TO_BE_DEACTIVATED)
								|| selStatus
										.equals(iRBTConstant.STATE_DEACTIVATION_PENDING)
								|| selStatus
										.equals(iRBTConstant.STATE_ACTIVATION_ERROR)
								|| selStatus
										.equals(iRBTConstant.STATE_DEACTIVATION_ERROR)) {
							return EXISTS_IN_GIFTEE_LIBRAY;
						}
					}
				}
			}
		}

		if (rbtDBManager.isSubscriberDeactivated(caller))
			return GIFTEE_NEW_USER;

		return GIFTEE_ACTIVE;
	}

}
