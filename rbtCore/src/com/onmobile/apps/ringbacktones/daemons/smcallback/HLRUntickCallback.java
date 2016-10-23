/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author vinayasimha.patil
 */
public class HLRUntickCallback implements SMCallback, SMCallbackConstants
{
	private static Logger logger = Logger.getLogger(HLRUntickCallback.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.callback.Callback#processCallback
	 * (com.onmobile.apps.ringbacktones.daemons.callback.CallbackContext)
	 */
	@Override
	public SMCallbackResponse processCallback(
			SMCallbackContext smCallbackContext)
	{
		SMCallbackResponse smCallbackResponse = null;
		try
		{
			String status = smCallbackContext.getStatus();
			if (status != null && status.equalsIgnoreCase(SUCCESS))
				smCallbackResponse = processSuccessCallback(smCallbackContext);
			else
				smCallbackResponse = processFailureCallback(smCallbackContext);
		}
		catch (Exception e)
		{
			logger.error("Error in processing the HLR Tick Callback", e);
			smCallbackResponse = new SMCallbackResponse(FAILURE);
		}

		logger.info("smCallbackResponse: " + smCallbackResponse);
		return smCallbackResponse;
	}

	private SMCallbackResponse processSuccessCallback(
			SMCallbackContext smCallbackContext)
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String subscriberID = smCallbackContext.getMsisdn();
		SubscriberAnnouncements[] announcements = rbtDBManager
				.getSubscriberAnnouncemets(subscriberID);
		if (announcements == null || announcements.length == 0)
			return (new SMCallbackResponse(FAILURE));

		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		if (subscriber != null && subscriber.extraInfo() != null
				&& !subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
		{
			Map<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(subscriber.extraInfo());
			if (extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_PCA_FLAG)
					&& extraInfoMap.get(iRBTConstant.EXTRA_INFO_PCA_FLAG).equalsIgnoreCase("TRUE"))
			{
				rbtDBManager.updateExtraInfo(subscriberID, DBUtility
						.removeXMLAttribute(subscriber.extraInfo(), iRBTConstant.EXTRA_INFO_PCA_FLAG));
				rbtDBManager.updatePlayerStatus(subscriberID, "A");

				boolean isUpdated = false;
				for (SubscriberAnnouncements announcement : announcements)
				{
					if (announcement.status() == iRBTConstant.ANNOUNCEMENT_DEACTIVATION_PENDING)
					{
						announcement.setStatus(iRBTConstant.ANNOUNCEMENT_DEACTIVE);
						isUpdated = rbtDBManager.updateAnnouncement(announcement);
					}
				}

				return (new SMCallbackResponse(isUpdated ? SUCCESS : FAILURE));
			}
		}

		boolean isUpdated = false;
		for (SubscriberAnnouncements announcement : announcements)
		{
			if (announcement.status() == iRBTConstant.ANNOUNCEMENT_DEACTIVATION_PENDING)
			{
				announcement.setStatus(iRBTConstant.ANNOUNCEMENT_TO_BE_DEACTIVED_PLAYER);
				isUpdated = rbtDBManager.updateAnnouncement(announcement);
			}
		}

		return (new SMCallbackResponse(isUpdated ? SUCCESS : FAILURE));
	}

	private SMCallbackResponse processFailureCallback(
			SMCallbackContext smCallbackContext)
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String subscriberID = smCallbackContext.getMsisdn();
		SubscriberAnnouncements[] announcements = rbtDBManager
				.getSubscriberAnnouncemets(subscriberID);
		if (announcements == null || announcements.length == 0)
			return (new SMCallbackResponse(FAILURE));

		boolean isUpdated = false;
		for (SubscriberAnnouncements announcement : announcements)
		{
			if (announcement.status() == iRBTConstant.ANNOUNCEMENT_DEACTIVATION_PENDING)
			{
				announcement.setStatus(iRBTConstant.ANNOUNCEMENT_TO_BE_DEACTIVED);
				isUpdated = rbtDBManager.updateAnnouncement(announcement);
			}
		}

		return (new SMCallbackResponse(isUpdated ? SUCCESS : FAILURE));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HLRUntickCallback";
	}
}
