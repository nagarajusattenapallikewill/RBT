package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;

public class RBTSATPushExecutors extends Thread {
	private RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	private static ExecutorService executor;
	static int threadPoolCount = 5;
	static int threadSleepTime = 1000;
	private static Logger logger = Logger.getLogger(RBTSATPushExecutors.class);
	private RBTDaemonManager m_mainDaemonThread;
	private static boolean useDoubleOptInSMSNotification = false;
	private static boolean useRBTSendSMSDaemon = false;

	static {
		logger.info("Inside SAT PUSH thread==>");
		threadPoolCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"DOUBLE_OPT_IN_SAT_REQUEST_EXECUTOR_POOL_SIZE", 5);
		threadSleepTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"DOUBLE_OPT_IN_SAT_REQUEST_THREAD_SLEEP_TIME", 1000);
		useDoubleOptInSMSNotification = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.WEBSERVICE,
						"USE_OPT_IN_SMS_NOTIFICATION", "FALSE");
		useRBTSendSMSDaemon = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.WEBSERVICE,
						"USE_RBT_SEND_SMS_DAEMON", "FALSE");
	}

	public RBTSATPushExecutors(RBTDaemonManager mainDaemonThread) {
		this.m_mainDaemonThread = mainDaemonThread;
	}

	public RBTSATPushExecutors() {
	}

	@Override
	public void run() {
		try {
			while (!useDoubleOptInSMSNotification || (m_mainDaemonThread.isAlive() && useRBTSendSMSDaemon)) {
				logger.info("Inside the executor");
				List<DoubleConfirmationRequestBean> consentBean = rbtDBManager
						.getDoubleConfirmationRequestBeanForSAT();
				if (consentBean == null) {
					Thread.sleep(threadSleepTime);
					continue;
				}
				try {
					logger.info("before executor" + executor);
					executor = Executors.newFixedThreadPool(threadPoolCount);
					logger.info("After executor" + executor);
				} catch (Throwable e) {
					logger.error("executor Exception", e);
				}
				logger.info("Success Exceutor");

				try {
					for (DoubleConfirmationRequestBean doubleConfirmationRequestBean : consentBean) {
						Runnable satRequest  = null;
						if(useRBTSendSMSDaemon){
							satRequest = new RBTSendSMSDaemon(doubleConfirmationRequestBean);
						}else{
							satRequest = new RBTSATPushDaemon(
									doubleConfirmationRequestBean);
						}
						executor.execute(satRequest);
					}
				} catch (Exception e) {
					logger.info("Exception Occured in RBTSATPushExecutors"
							+ e.getMessage());
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
			}
		} catch (Exception e) {
			logger.info("Exception Occured in RBTSATPushExecutors"
					+ e.getMessage());
		}
	}

	public static void main(String[] args) {
		String method = "main()";
		System.out.println("Entering " + method);
		RBTSATPushExecutors rbtsatPushExecutors = new RBTSATPushExecutors();
		rbtsatPushExecutors.start();
	}
}
