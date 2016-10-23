/**
 * Ring Back Tone 
 * Copyright OnMobile 2011
 * 
 * $Author: gautam.agrawal $
 * $Id: PendingConfirmationsReminderDaemon.java,v 1.4 2012/12/24 06:29:54 gautam.agrawal Exp $
 * $Revision: 1.4 $
 * $Date: 2012/12/24 06:29:54 $
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PendingConfirmationsRemainder;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;

/**
 * 
 * Subscriber receive SMS's asking confirmation for song download or RBT
 * activation. It send SMS for a configurable number of times in a configurable
 * period for a configurable duration.
 * 
 * @author rajesh.karavadi
 * @Since 22-Jun-2012
 */
public class PendingConfirmationsReminderDaemon extends Thread {

    // time duration to send sms
    private int delayInSentTime = 0;
    private int recordsFetchLimit = 5000;
    private int recordsDeleteLimit = 5000;

	private ParametersCacheManager parametersCacheManager;
	private RBTClient rbtClient = null;
	private RBTDaemonManager rbtDaemonManager = null;
    
    private static Logger LOGGER = Logger
            .getLogger(PendingConfirmationsReminderDaemon.class);
    
	// Track TRANSACTION_LOGGER updates made to pending confirmation records.
	private static final Logger TRANSACTION_LOG = Logger.getLogger("TRANSACTION_LOGGER");
    
    public PendingConfirmationsReminderDaemon(RBTDaemonManager rbtDaemonManager)
    {
        try
        {
        	setName("PendingConfirmationsReminderDaemon");
        	// Load the configurations from DB to Cache.
	        parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
	        // Get the values from the Cache.
	        delayInSentTime = Integer.parseInt(getParamAsString("DAEMON", "DELAY_IN_SENDING_REMINDER_IN_MIN", "30"));
	        rbtClient = RBTClient.getInstance();
	        this.rbtDaemonManager = rbtDaemonManager;
	        
	        LOGGER.info("Successfully initialized PendingConfirmationsRemainderDaemon");
        }
        catch (Exception e)
        {
			LOGGER.error("Issue in creating PendingConfirmationsReminderDaemon", e);
		}
    }
    
    @Override
	public void run() {
    	LOGGER.info("Getting started PendingConfirmationsRemainderDaemon");
		while (rbtDaemonManager != null && rbtDaemonManager.isAlive()) {
			try {
				LOGGER.debug("Started processing pending confirmation requests");

				processPendingRequests();
				
				LOGGER.debug("Successfully send sms for pending confirmation requests");

				clearPendingRequests();
				
				int sleepInterval = Integer.parseInt(getParamAsString(
						"DAEMON","SLEEP_INTERVAL_MINUTES", "5"));
				LOGGER.info("PendingConfirmationsRemainderDaemon Sleeping for "
						+ sleepInterval + " minutes");
				Thread.sleep(sleepInterval * 60 * 1000);
			} catch (Exception e) {
				LOGGER.error("Failed to run PendingConfirmationsRemainderDaemon. Exception: " + e.getMessage(), e);
			}
		}
		LOGGER.info("Stopped running PendingConfirmationsRemainderDaemon");
	}
    

	private void processPendingRequests() {
		int recordPos = 0;
		PendingConfirmationsRemainder[] pendingConfirmations = null;
		while (null == pendingConfirmations || recordsFetchLimit == pendingConfirmations.length) {
			LOGGER.debug("Fetching for pending confirmation requests ");
			pendingConfirmations = getPendingConfirmations(delayInSentTime,
					recordPos, recordsFetchLimit);
			
			sendSmsForPendingRequests(pendingConfirmations);

			// fetch from the nth record and the specified limit.
			recordPos = recordPos + recordsFetchLimit;
		}
	}
	
	protected void sendSmsForPendingRequests(PendingConfirmationsRemainder[] pendingConfirmations) {
		LOGGER.debug("Sending Sms for pending confirmation requests ");
		for (PendingConfirmationsRemainder p : pendingConfirmations) {
			// Send sms
			boolean isSmsSent = Tools.sendSMS(p.getSender(), p.getSubscriberId(), p
					.getRemainderText());
			if (isSmsSent) {
				DataRequest dataRequest = new DataRequest(null);
				dataRequest.setSubscriberID(p.getSubscriberId());
				dataRequest.setRemindersLeft(String.valueOf(p
						.getRemaindersLeft() - 1));
				dataRequest.setLastReminderSent(new Date());
				dataRequest.setSmsID(p.getSmsId());
				rbtClient.updatePendingConfirmationsRemainder(dataRequest);
				LOGGER.warn("Updated PendingConfirmationsRemainder for subscriberId: "
						+p.getSubscriberId());
			} else {
				LOGGER.warn("Failed to send SMS for subscriberId: "
						+ p.getSubscriberId() + ", sender: " + p.getSender()
						+ ", reminder text: " + p.getRemainderText());
			}
		}
	}

	private void clearPendingRequests() {
		DataRequest dataRequest = new DataRequest(null);
		dataRequest.setDeleteLimit(recordsDeleteLimit);
		rbtClient.deletePendingConfirmationsRemainder(dataRequest);
	}
	
	/**
	 * @param delayInSentTime
	 * @param recordFrom
	 * @param numOfRecords
	 * @return
	 */
	protected PendingConfirmationsRemainder[] getPendingConfirmations(int delayInSentTime, int recordFrom, int numOfRecords) {
		DataRequest dataRequest = new DataRequest(null);
		dataRequest.setDelayInSentTime(delayInSentTime);
		dataRequest.setRecordFrom(recordFrom);
		dataRequest.setNumOfRecords(numOfRecords);
		return rbtClient.getPendingConfirmationsRemainders(dataRequest);
	}

    protected String getParamAsString(String type, String param, String defaultValue)           {
        String paramValue = null;
        try {
            paramValue = parametersCacheManager.getParameter(
                    type , param, defaultValue).getValue();
        } catch (Exception e) {
            LOGGER.error("Unable to get param: " + param);
            return defaultValue;
        }
        return paramValue;
    }
}
