package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTMultiThreadInitCache;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;

/**
 * It is a daemon thread runs in background to refresh RBT memcache in regular intervals.
 * This implements single-ton pattern so cannot start more than once(thread cannot be started more than once). 
 * 
 * @author laxmankumar
 *
 */
public class RBTCacheRefreshThread extends Thread {

	private static Logger log = Logger.getLogger(RBTCacheRefreshThread.class.getName());

	private boolean stopThread = false;

	// refresh interval
	private int noOfDays = 1;

	// time of initialization 0-23hrs
	private int timeOfInit = -1;

	// parameter to specify refresh interval
	private static final String REFRESH_AFTER_NO_OF_DAYS = "refresh_after_no_of_days";

	// parameter to specify time of initialization
	private static final String REFRESH_TIME = "refresh_time";
	
	private static RBTCacheRefreshThread instance = null;

	private RBTCacheRefreshThread() {
		if (log.isDebugEnabled()) {
			log.debug("RBTCacheRefreshThread constructor start.");
		}
		setDaemon(true);
		setName("RBTCacheRefreshThread");
		RBTContentJarParameters params = RBTContentJarParameters.getInstance();
		noOfDays = Integer.parseInt(params.getParameter(REFRESH_AFTER_NO_OF_DAYS));
		timeOfInit = Integer.parseInt(params.getParameter(REFRESH_TIME));
		if (noOfDays <= 0) {
			log.warn("Refresh interval of memcache is not defined properly. Assuming refresh_after_no_of_days as 1");
			noOfDays = 1;
		}
		if (log.isDebugEnabled()) {
			log.debug("RBTCacheRefreshThread constructor end.");
		}
	}
	
	public static RBTCacheRefreshThread getInstance() {
		if (null == instance) {
			log.info("Instantiating RBTCacheRefreshThread...");
			instance = new RBTCacheRefreshThread();
		}
		return instance;
	}
	
	public void run() {
		log.info("Starting the RBTCacheRefreshThread");
		while(!this.stopThread) {
			try {
				// using calendar object to reset the time to timeOfInit
				// so that we can wake up thread at exactly timeOfInit hrs.
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, timeOfInit);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				// adding noOfDays to date 
				Date d = new Date(c.getTimeInMillis() + noOfDays  * 24 * 60 * 60 * 1000);
				log.info("Next refresh of memcache is scheduled at : " + d);
				long sleepTime = d.getTime() - System.currentTimeMillis();
				Thread.sleep(sleepTime);
				log.info("RefreshCache thread woke up. Going to refresh...");
				if (RBTMultiThreadInitCache.isUnderProcess()) {
					log.info("Refresh memcache is already running. so skipping this turn...");
				} else {
					boolean success = RBTCache.getMemCachedClient().set(
							RBTCache.MC_HEART_BEAT_FLAG, RBTCache.MC_HEART_BEAT_FLAG);
					if(success) {
						// in case of scheduler only shutdown will be called
						if (RBTCache.isShutdown()) {
							RBTCache.initMemcachePool();
							RBTMultiThreadInitCache.init();
							RBTCache.shutDown();
						} else {
							RBTMultiThreadInitCache.init();
						}
					} else {
						log.error("ContentCache is not up!!! Please check...");
					}
				}
			} catch (Exception e) {
				log.error("Error in refresh cache process", e);
				break;
			}
		}
	}
	
	public void stopThread() {
		this.stopThread = true;
	}

}
