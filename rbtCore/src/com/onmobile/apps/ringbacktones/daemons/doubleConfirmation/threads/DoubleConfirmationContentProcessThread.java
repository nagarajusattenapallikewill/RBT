package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;


import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationContentProcessUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;

public class DoubleConfirmationContentProcessThread extends Thread implements iRBTConstant{

	DoubleConfirmationDBFetcher dbFetcher = null;
	Logger logger = Logger
			.getLogger(DoubleConfirmationContentProcessThread.class);
	


	public DoubleConfirmationContentProcessThread(
			DoubleConfirmationDBFetcher dbFetcher) {
		this.dbFetcher = dbFetcher;
	}

	public void run() {
		while (true) {
			DoubleConfirmationRequestBean requestConsentBean = null;
			DoubleConfirmationContentProcessUtils processRequest = null; //CG Integration Flow - Jira -12806
			synchronized (dbFetcher.contentQueue) {				
				if (dbFetcher.contentQueue.size() > 0) {
					logger.info("Content Process thread found contentrequest, "
							+ dbFetcher.contentQueue.get(0));
					requestConsentBean = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(requestConsentBean);
				} else {
					try {
						logger
								.info("Content Process thread waiting as queue size="
										+ dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					} catch (InterruptedException e) {
						logger.error("Content Process thread interrupted. Will check queue now", e);
					}
					continue;
				}				
			}//CG Integration Flow - Jira -12806
			processRequest = new DoubleConfirmationContentProcessUtils();
			processRequest.processRecord(requestConsentBean);
			synchronized (dbFetcher.pendingQueue) {
			    dbFetcher.pendingQueue.remove(requestConsentBean);
			}
		}
	}

	

}
