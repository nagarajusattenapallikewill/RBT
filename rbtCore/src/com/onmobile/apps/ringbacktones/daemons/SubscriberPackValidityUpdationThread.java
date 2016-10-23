package com.onmobile.apps.ringbacktones.daemons;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class SubscriberPackValidityUpdationThread extends Thread {
	private static Logger logger = Logger.getLogger(SubscriberPackValidityUpdationThread.class);
	private static ExecutorService executor;
	static int threadPoolCount = 5;
	static int threadSleepTime = 1000;
	private static int maxRetry = 4;
	private static int smSubStatus = 1;
	private RBTDaemonManager mainDaemonThread;
	static String dtocAppClass;

	public SubscriberPackValidityUpdationThread() {
	}

	public SubscriberPackValidityUpdationThread(RBTDaemonManager mainDaemonThread) {
		this.mainDaemonThread = mainDaemonThread;
	}

	static {
		try {
			logger.info("Inside static SubscriberPackValidityUpdationThread==>");
			threadPoolCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"SUBSCRIBER_PACK_VALIDITY_UPDATION_EXECUTOR_POOL_SIZE", 5);
			threadSleepTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"SUBSCRIBER_PACK_VALIDITY_UPDATION_THREAD_SLEEP_TIME", 1000);
			maxRetry = RBTParametersUtils.getParamAsInt("DAEMON", "SUBSCRIBER_PACK_VALIDITY_UPDATION_REQUEST_MAX_RETRY",
					4);
			smSubStatus = RBTParametersUtils.getParamAsInt("DAEMON", "SM_SUBCRIPTION_ACTIVATION_SUCCESS_STATUS_FOR_PROVISIONING_REQUEST",1);
			dtocAppClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_FREE_TRIAL_COS_ID", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void run() {
		while (mainDaemonThread.isAlive()) {
			logger.info("Inside the SubscriberPackValidityUpdationThread");
			List<ProvisioningRequests> provList = RBTDBManager.getInstance()
					.getActiveProvisioningByType(dtocAppClass.split(","), maxRetry,smSubStatus);
			try {
				if (provList == null) {
					Thread.sleep(threadSleepTime);
					continue;
				}
				executor = Executors.newFixedThreadPool(threadPoolCount);
				logger.info("Success created Exceutor");
				for (ProvisioningRequests provisioningRequests : provList) {
					Runnable packValUpdReq = new SubscriberPackValidityUpdationExecutor(provisioningRequests);
					executor.execute(packValUpdReq);

				}
				executor.shutdown();
				while (!executor.isTerminated()) {
					try {
						Thread.sleep(threadSleepTime);
					} catch (InterruptedException e) {
						System.out.println("Exception" + e.getMessage());
					}
				}
				Thread.sleep(threadSleepTime);
			} catch (Throwable e) {
				logger.error("Exception Occured in SubscriberPackValidityUpdationThread"
							+ e.getMessage());
			}

		}

	}
	
	
}
