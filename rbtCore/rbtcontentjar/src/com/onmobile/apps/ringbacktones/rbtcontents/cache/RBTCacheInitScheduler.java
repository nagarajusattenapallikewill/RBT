package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.RBTCacheRefreshThread;

/**
 * Do not schedule this on memcache client jvm. Once it finished the
 * initialization job tries to close the memcache connection pool.
 * 
 * @author laxmankumar
 * 
 */
public class RBTCacheInitScheduler {

	private static final Logger log = Logger.getLogger(RBTCacheInitScheduler.class);
	
	private static final int START = 1;

	private static final int STOP = 0;
	
	public static void main(String[] args) {
		int status = -1;
		try {
			status = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			// ignore
		}
		switch (status) {
			case START:
				try {
					log.info("RBTCacheInit daemon satrted.");
					boolean success = RBTCache.getMemCachedClient().set(
							RBTCache.MC_HEART_BEAT_FLAG, RBTCache.MC_HEART_BEAT_FLAG);
					if(success) {
						RBTMultiThreadInitCache.init();
					} else {
						log.error("ContentCache is not up!!! Please check...");
					}
					RBTCache.shutDown();
				} catch (Exception e) {
					log.error("Error while initializing the memcache.", e);
				}
				try {
					RBTCacheRefreshThread refreshThread = RBTCacheRefreshThread.getInstance();
					// to make it daemon process
					refreshThread.join();
				} catch (InterruptedException e) {
					log.error("Process Interrupted.", e);
				}
				break;
			case STOP: 
				RBTCacheRefreshThread refreshThread = RBTCacheRefreshThread.getInstance();
				refreshThread.stopThread();
				if (RBTMultiThreadInitCache.isUnderProcess()) {
					log.info("Initialzation is under process. Once it is finished the daemon will be killed.");
				}
				break;
			default:
				log.error("Invalid argument");
		}
	}
}
