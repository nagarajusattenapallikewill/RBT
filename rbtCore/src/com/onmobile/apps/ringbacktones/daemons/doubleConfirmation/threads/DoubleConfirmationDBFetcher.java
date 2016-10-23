package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;

public class DoubleConfirmationDBFetcher extends Thread {

	private static Logger logger = Logger
			.getLogger(DoubleConfirmationDBFetcher.class);

	public List<DoubleConfirmationRequestBean> contentQueue = new ArrayList<DoubleConfirmationRequestBean>();
	public List<DoubleConfirmationRequestBean> pendingQueue = new ArrayList<DoubleConfirmationRequestBean>();

	public int status = -1;
	private static int consentPushThreadPoolSize = 1;
	private static int fetcherSleepTimeInSec = 120;
	private static int contentProcessThreadPoolSize = 1;
	private static int fetchSize = 5000;
	private static int cleanUpTimeInMins = 120;

	static {
		consentPushThreadPoolSize = RBTParametersUtils.getParamAsInt(
				"DOUBLE_CONFIRMATION", "CONSENT_PUSH_THREAD_POOL_SIZE", 1);
		contentProcessThreadPoolSize = RBTParametersUtils.getParamAsInt(
				"DOUBLE_CONFIRMATION",
				"CONSENT_CONTENT_PROCESS_THREAD_POOL_SIZE", 1);
		fetchSize = RBTParametersUtils.getParamAsInt("DOUBLE_CONFIRMATION",
				"DB_FETCH_SIZE", 5000);
		fetcherSleepTimeInSec = RBTParametersUtils.getParamAsInt(
				"DOUBLE_CONFIRMATION", "SLEEP_TIME_IN_MINS", 5)*60;
		if(RBTParametersUtils.getParamAsInt("DOUBLE_CONFIRMATION", "SLEEP_TIME_IN_SECS", -1) != -1)
			fetcherSleepTimeInSec = RBTParametersUtils.getParamAsInt("DOUBLE_CONFIRMATION", "SLEEP_TIME_IN_SECS", 120);
	}

	public void run() {
		while (true) {
			logger.info("Entering while loop");
			logger.info("contentQueue.size()="+contentQueue.size()+", pendingQueue.size()="+pendingQueue.size());
			if (contentQueue.size() == 0 && pendingQueue.size() == 0) {
			  synchronized (contentQueue) {
					List<DoubleConfirmationRequestBean> contentRequests = null;
					boolean isRequestTimeCheckRequired = false; 
					if (status == 0) {
						isRequestTimeCheckRequired = true;
					}
					contentRequests = RBTDBManager.getInstance()
								.getDoubleConfirmationRequestBeanForStatus(
										status + "", null, null, null, false, isRequestTimeCheckRequired);
					if (contentRequests != null && contentRequests.size() != 0) {
						logger.info("Found "+contentRequests.size() + " pending consent records with status "+status);
						contentQueue.addAll(contentRequests);
						contentQueue.notifyAll();
					}
					else
						logger.info("No pending records found with status="+status);
				  }
				} else {
					logger.info("contentQueue size = "+ contentQueue.size() + ",  pendingQueue.size()="+pendingQueue.size()
									+ ". Now will sleep and let workers clear the queue.");
				}
			

			try {
				logger.info("Sleeping for " + fetcherSleepTimeInSec + " secs");
				Thread.sleep(fetcherSleepTimeInSec *1000);
				logger.info("Woke up");
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public DoubleConfirmationDBFetcher(int status) {
		this.status = status;

		if (status == 0) {
			for (int i = 0; i < consentPushThreadPoolSize; i++) {
				DoubleConfirmationConsentPushThread consentThreads = new DoubleConfirmationConsentPushThread(
						this);
				consentThreads.setName("CPUW-"
						+ (i+1));
				consentThreads.start();
			}
		} else if (status == 2) {
			for (int i = 0; i < contentProcessThreadPoolSize; i++) {
				DoubleConfirmationContentProcessThread contentProcessThread = new DoubleConfirmationContentProcessThread(
						this);
				contentProcessThread
						.setName("CPRW-" + (i+1));
				contentProcessThread.start();
			}
		}
	}

}
