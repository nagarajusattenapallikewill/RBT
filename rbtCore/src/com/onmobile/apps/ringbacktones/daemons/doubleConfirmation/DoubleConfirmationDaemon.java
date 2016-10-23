package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationDBFetcher;

public class DoubleConfirmationDaemon extends Thread {

	private static Logger logger = Logger
			.getLogger(DoubleConfirmationDaemon.class);
	private RBTDaemonManager mainDaemonThread = null;
	private static int statusBeforeConsent = 0;
	private static int statusAfterContentProcessed = 2;
	private static int statusBeforeConstentProcessed = 3;
	
	private boolean isAlive = false;

	public DoubleConfirmationDaemon(RBTDaemonManager mainDaemonThread) {
         this.mainDaemonThread = mainDaemonThread;
         setName("DoubleConfirmationDaemon");
	}
	
	public DoubleConfirmationDaemon() {
        setName("DoubleConfirmationDaemonOzonized");
        isAlive = true;
        logger.info("Starting DoubleConfirmationDaemon as Ozonized..");
	}

	public void run() {
	   if((mainDaemonThread != null && mainDaemonThread.isAlive()) || isAlive) {
			logger.info("DoubleConfirmationDaemon Started !!");
			
			boolean isStartContentPusingThread = RBTParametersUtils.getParamAsBoolean("COMMON", "MAKE_ENTRY_CONSENT_ACT_USER_SEL", "TRUE");
			logger.info("isStartContentPusingThread="+isStartContentPusingThread);
				
			if(isStartContentPusingThread) {
				// Starting to get Consent form Subscriber
				startConsentPushingThreads();
			}
			// Threads to Process records
			startContentProcessingThreads();
		}
	}

	private static void startConsentPushingThreads() {
		logger.info("Starting DoubleConfirmationDBFetcher with status="+statusBeforeConsent);
		DoubleConfirmationDBFetcher dbFetcher = new DoubleConfirmationDBFetcher(
				statusBeforeConsent);
		dbFetcher.setName("CPUD");
		dbFetcher.start();
	}

	private static void startContentProcessingThreads() {
		logger.info("Starting DoubleConfirmationDBFetcher with status="+statusAfterContentProcessed);
		DoubleConfirmationDBFetcher dbFetcher = new DoubleConfirmationDBFetcher(
				statusAfterContentProcessed);
		dbFetcher.setName("CPRD");
		dbFetcher.start();

	}
	
	public void stopThread() {
		isAlive = false;
	}

	public static void main(String[] args) {
		DoubleConfirmationDaemon daemon = new DoubleConfirmationDaemon();
		daemon.start();
	}
}
