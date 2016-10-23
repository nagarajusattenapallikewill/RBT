package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Circle;

public class CircleThreadManager {

	private static final Logger log = Logger.getLogger(CircleThreadManager.class);
	
	List<Circle> circleList = null;
	
	int noOfThreads = -1;
	
	public CircleThreadManager(List<Circle> circleList, int noOfThreads) {
		this.circleList = circleList;
		this.noOfThreads = noOfThreads;
	}

	public void startThreads() throws InterruptedException, MultiThreadCacheInitException {
		ArrayList<GenericCacheThread> threadsList = new ArrayList<GenericCacheThread>(noOfThreads); 
		for (int it = 0; it < noOfThreads; it++) {
			CircleThread cThread = new CircleThread("CirclesThread" + (it+1), circleList);
			cThread.start();
			log.info(cThread.getName() + " is started...");
			threadsList.add(cThread);
		}
		log.info("Waiting to finish circle threads.");
		ThreadManagerUtils.joinAndCheckThreadsStatus(threadsList);
	}
	
}
