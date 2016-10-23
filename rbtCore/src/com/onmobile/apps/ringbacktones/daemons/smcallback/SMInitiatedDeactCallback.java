package com.onmobile.apps.ringbacktones.daemons.smcallback;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.subscriptions.RBTDaemonHelper;

public class SMInitiatedDeactCallback implements SMCallback, SMCallbackConstants, iRBTConstant
{
	private static Logger logger = Logger
			.getLogger(SMInitiatedDeactCallback.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallback#processCallback
	 * ( com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackContext)
	 */
	@Override
	public SMCallbackResponse processCallback(SMCallbackContext smCallbackContext)
	{
		String subscriberID = smCallbackContext.getMsisdn();
		String mode = smCallbackContext.getMode();
		String type = smCallbackContext.getType();
		String classType = smCallbackContext.getSrvkey();
		String refID = smCallbackContext.getRefid();
		String status = smCallbackContext.getStatus();

		String result = SUCCESS;

		if (smCallbackContext.isSelectionCallback())
		{
			if (refID.startsWith("RBTDOWNLOAD") || RBTParametersUtils.getParamAsBoolean(COMMON, "ADD_TO_DOWNLOADS", "FALSE"))
			{
				result = processDownloadRenewalFailureCallback(subscriberID, mode, type, classType, refID, status);
			}
			else
			{
				result = processSelectionRenewalFailureCallback(subscriberID, mode, type, classType, refID);
			}
		}
		else if (smCallbackContext.isActivationCallback())
		{
			result = processActivationRenewalFailureCallback(subscriberID, mode, type, classType);
		}

		return new SMCallbackResponse(result);
	}

	/**
	 * @param subscriberID
	 * @param mode
	 * @param type
	 * @param classType
	 * @return
	 */
	private String processActivationRenewalFailureCallback(String subscriberID, String mode, String type, String classType)
	{
		String result = SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Subscriber subscriber = dbManager.getSubscriber(subscriberID);
		if (subscriber != null)
		{
			if (classType == null)
			{
				// safety check
				classType = subscriber.subscriptionClass();
			}

			logger.info("Deactivating the subscriber: " + subscriberID
					+ " with status: " + subscriber.subYes());
          
			//Return If the Subscriber is already deactive.
			if (subscriber.subYes().equalsIgnoreCase("X")) {
				return RBTDaemonHelper.SUBSCRIPTION_ALREADY_DEACTIVE;
			} 


			// Deactivate the subscriber irrespective of the subscriber current status
			result = dbManager.smSubscriptionRenewalFailure(subscriberID, mode, type,
					classType, false, subscriber.extraInfo(), false, true, null);
		}
		else
		{
			result = RBTDaemonHelper.SUBSCRIPTION_DOES_NOT_EXIST;
			logger.warn("Subscription doesn't exist but received deactivation request for the subscriber: "
					+ subscriberID);
		}

		return result;
	}

	/**
	 * @param subscriberID
	 * @param mode
	 * @param type
	 * @param classType
	 * @param refID
	 * @return
	 */
	private String processSelectionRenewalFailureCallback(String subscriberID, String mode, String type, String classType, String refID)
	{
		String result = SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Connection conn = dbManager.getConnection();
		if (conn == null)
			return "FAILURE";

		SubscriberStatus subscriberStatus = dbManager.getRefIDSelection(conn, subscriberID, refID);
		if (subscriberStatus != null)
		{
			if(subscriberStatus.selStatus().equalsIgnoreCase("X")) {
				result = RBTDaemonHelper.SELECTION_ALREADY_DEACTIVE;
				logger.warn("Selection already deactive, but received deactivation callback request for the selection, subscriberID : "
						+ subscriberID + ", refID : " + refID);
			}
			else {
				char oldLoopStatus = subscriberStatus.loopStatus();
				char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
				if (oldLoopStatus == LOOP_STATUS_EXPIRED)
					newLoopStatus = oldLoopStatus;
				else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT || oldLoopStatus == LOOP_STATUS_LOOP_INIT)
					newLoopStatus = LOOP_STATUS_EXPIRED;
	
				result = dbManager.smSelectionActivationRenewalFailure(
						subscriberID, refID, mode, type, classType,
						newLoopStatus, subscriberStatus.selType(),
						subscriberStatus.extraInfo(),null);
			}
		}
		else
		{
			result = RBTDaemonHelper.SELECTION_REFID_NOT_EXISTS;
			logger.warn("Selection doesn't exist but received deactivation callback request for the selection, subscriberID : "
					+ subscriberID + ", refID : " + refID);
		}

		return result;
	}

	/**
	 * @param subscriberID
	 * @param mode
	 * @param type
	 * @param classType
	 * @param refID
	 * @param status
	 * @return
	 */
	private String processDownloadRenewalFailureCallback(String subscriberID,
			String mode, String type, String classType, String refID, String status)
	{
		String result = SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Connection conn = dbManager.getConnection();
		if (conn == null)
			return "FAILURE";

		SubscriberDownloads download = dbManager.getSMDownloadForCallback(conn, subscriberID, refID);
		if (download != null)
		{
			result = dbManager.smUpdateDownloadRenewalCallback(subscriberID, download.promoId(),
					refID, "FAILURE", RBTParametersUtils.getParamAsBoolean("DAEMON",
							"NO_DOWNLOAD_DEACT_SUB", "FALSE"), type, classType,"NEF");
		}
		else
		{
			result = RBTDaemonHelper.SELECTION_REFID_NOT_EXISTS;
			logger.warn("Download doesn't exist but received deactivation callback request for the download, subscriberID : "
					+ subscriberID + ", refID : " + refID);
		}

		return result;
	}
}
