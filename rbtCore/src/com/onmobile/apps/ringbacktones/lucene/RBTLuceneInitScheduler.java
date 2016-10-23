package com.onmobile.apps.ringbacktones.lucene;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;

/**
 * It is a daemon thread runs in background to index content in regular
 * intervals. This implements single-ton pattern so cannot start more than
 * once(thread cannot be started more than once).
 * 
 * This class instance cannot be useful at client-side because it will closes
 * the memcache connections once it is done. So, memcache will be inaccessible.
 * 
 * @author laxmankumar
 * 
 */
public class RBTLuceneInitScheduler extends Thread {

	private static Logger log = Logger.getLogger(RBTLuceneInitScheduler.class.getName());

	private boolean stopThread = false;

	// refresh interval
	private int noOfDays = 1;

	// time of initialization 0-23hrs
	private int timeOfInit = -1;

	private RBTLuceneInitScheduler(String sNoOfDays, String sTimeOfInit) {
		setDaemon(true);
		setName("RBTLuceneInitializer");
		if (null == sNoOfDays || "".equals(sNoOfDays.trim())) {
			throw new IllegalArgumentException("Parameter number of days is invalid");
		}
		if (null == sTimeOfInit || "".equals(sTimeOfInit.trim())) {
			throw new IllegalArgumentException("Parameter time of initialization is invalid");
		}
		try {
			noOfDays = Integer.parseInt(sNoOfDays);
			timeOfInit = Integer.parseInt(sTimeOfInit);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(
					"Parameters: 1. number of days and 2. time of init, should be integers.", e);
		}
		if (noOfDays <= 0) {
			log.warn("Refresh interval of indexing is not defined properly. Assuming refresh_after_no_of_days as 1");
			noOfDays = 1;
		}
	}
	
//	public static RBTLuceneInitScheduler getInstance() {
//		if (null == instance) {
//			log.info("Instantiating RBTLuceneInitScheduler...");
//			instance = new RBTLuceneInitScheduler();
//		}
//		return instance;
//	}
	
	public void run() {
		log.info("Starting the RBTLuceneInitScheduler");
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
				log.info("Next indexing job is scheduled at : " + d);
				long sleepTime = d.getTime() - System.currentTimeMillis();
				Thread.sleep(sleepTime);
				log.info("Indexing thread woke up. Going to refresh...");
				// initializing memcache socket connections
				RBTCache.initMemcachePool();
				AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
				indexer.init();
				// closing memcache socket connections
				RBTCache.shutDown();
			} catch (Exception e) {
				log.error("Error in scheduler process", e);
				break;
			}
		}
	}
	
	public void stopThread() {
		this.stopThread = true;
	}

	public static void main(String[] args) {
		try {
			if (null == args || args.length < 2) {
				log.error("Parameters: 1. number of days and 2. time of init, should be passed as arguments.");
			}
			RBTLuceneInitScheduler sch = new RBTLuceneInitScheduler(args[0], args[1]);
			sch.start();
			AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
			indexer.init();
			// closing memcache socket connections
			RBTCache.shutDown();
			sch.join();
		} catch (Exception e) {
			log.error("Error in scheduler", e);
		}
	}
}
