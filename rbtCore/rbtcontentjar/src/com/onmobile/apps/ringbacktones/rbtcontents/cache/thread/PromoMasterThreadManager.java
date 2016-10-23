package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;

public class PromoMasterThreadManager {

	private static final Logger log = Logger.getLogger(PromoMasterThreadManager.class);
	
	List<PromoMaster> promoMasterList = null;
	
	int noOfThreads = -1;
	
	public PromoMasterThreadManager(List<PromoMaster> promoMasterList, int noOfThreads) {
		this.promoMasterList = promoMasterList;
		this.noOfThreads = noOfThreads;
	}

	public void startThreads() throws InterruptedException, MultiThreadCacheInitException {
		ArrayList<GenericCacheThread> threadsList = new ArrayList<GenericCacheThread>(noOfThreads); 
		for (int it = 0; it < noOfThreads; it++) {
			PromoMasterThread pmThread = new PromoMasterThread("PromoMasterThread" + (it+1), promoMasterList);
			pmThread.start();
			log.info(pmThread.getName() + " is started...");
			threadsList.add(pmThread);
		}
		log.info("Waiting to finish promomaster threads.");
		ThreadManagerUtils.joinAndCheckThreadsStatus(threadsList);
	}
	
}
