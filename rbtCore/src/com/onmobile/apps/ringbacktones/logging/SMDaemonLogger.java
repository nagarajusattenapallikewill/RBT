package com.onmobile.apps.ringbacktones.logging;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

public class SMDaemonLogger
{
	private static final Logger logger = Logger.getLogger(SMDaemonLogger.class);
	//test

	/**
	 * @param subscriber
	 * @param subscriberStatus
	 * @param downLoads
	 * @param provisioningRequests
	 */
	public static void writeSMDaemonTransactionLog(Subscriber subscriber, SubscriberStatus subscriberStatus, SubscriberDownloads downLoads, 
			ProvisioningRequests provisioningRequests, String url)
	{
		try
		{
			StringBuilder logBuilder = new StringBuilder();

			String subscriberID = null;
			String refID = null;
			String requestType = null;
			String wavFile = null;
			if (subscriber != null) {
				subscriberID = subscriber.subID();
				refID = subscriber.refID();
				requestType = "ACTIVATION";
				wavFile = "";
			}
			else if (subscriberStatus != null) {
				subscriberID = subscriberStatus.subID();
				refID = subscriberStatus.refID();
				requestType = "SELECTION";
				wavFile = subscriberStatus.subscriberFile();
			}
			else if (downLoads != null) {
				subscriberID = downLoads.subscriberId();
				refID = downLoads.refID();
				requestType = "DOWNLOAD";
				wavFile = downLoads.promoId();
			}
			else if (provisioningRequests != null) {
				subscriberID = provisioningRequests.getSubscriberId();
				refID = provisioningRequests.getTransId();
				requestType = "PACK";
				wavFile = "";
			}

			logBuilder.append(subscriberID).append(", ").append(refID)
					.append(", ").append(requestType).append(", ")
					.append(wavFile).append(", ").append(url);
			RBTEventLogger.logEvent(RBTEventLogger.Event.SMDAEMON,
					logBuilder.toString());
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
}
