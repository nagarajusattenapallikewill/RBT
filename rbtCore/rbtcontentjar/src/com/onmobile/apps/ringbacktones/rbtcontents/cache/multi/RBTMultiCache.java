package com.onmobile.apps.ringbacktones.rbtcontents.cache.multi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.RBTCacheRefreshThread;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;

/**
 * @author SenthilRaja
 * This is main class to create and update the cache.
 */
public class RBTMultiCache {

	public static Logger basicLogger = Logger.getLogger(RBTMultiCache.class);

	//	private static RBTCache instance = null;
	//	private static boolean initialized = false;
	private static MemCachedClient mc = null;
	
	private static Map<String, MemCachedClient> memcachedMap = new HashMap<String, MemCachedClient>();
	private static Map<String, SockIOPool> poolMap = new HashMap<String, SockIOPool>();
	private static Map<String, Boolean> cacheAliveMap = new HashMap<String, Boolean>();
	private static Map<String, Boolean> cacheInitalizedMap = new HashMap<String, Boolean>();	
	
	private static SockIOPool pool = null; 
	private static boolean isCacheAlive = true;
	private static boolean isCacheInitialized = false;
	private static boolean shutdown = true;

	public static String MC_IS_CACHE_INITIALIZED_FLAG = "IsCacheInitialized";
	public static String MC_HEART_BEAT_FLAG = "RBTCacheHeartBeatThread";
	private static final String REFRESH_MEMCACHE = "refresh_memcache";

	private static RBTCacheHeartBeatThread heartBeatThread = null;

	private static HashMap<String, Set<String>> categoryTypeMap = new HashMap<String, Set<String>>();
	
	static {
		try {
			initMemcachePool();
			String refreshMemcache = RBTContentJarParameters.getInstance().getParameter(REFRESH_MEMCACHE);
			if ("yes".equalsIgnoreCase(refreshMemcache)) {
				// to refresh memcache in regular intervals
				RBTCacheRefreshThread refreshThread = RBTCacheRefreshThread.getInstance();
				refreshThread.start();
			}
		} catch (Throwable e) {
			basicLogger.error("Error while initializing the MemCachedClient", e);
			// to avoid class loading, throwing unchecked exception
			throw new RuntimeException(e);
		}
	}
	
	public static void initMemcachePool() throws IllegalAccessException {
		if (!shutdown) {
			throw new IllegalAccessException(
					"Reinitialzation is not acceptable without shutting down the existing connection pool");
		}
		basicLogger.warn("Initilizing the MemCachedClient object");
		
		//Get circle ids from rbtcontentjar.properties file
		List<String> circleIdsList = null;
		String strCircleIds = RBTContentJarParameters.getInstance().getParameter("circleid_list");
		if(strCircleIds != null && !(strCircleIds = strCircleIds.trim()).equals("")){
			circleIdsList = Arrays.asList(strCircleIds.split(","));
		}
		
		if(circleIdsList == null || circleIdsList.size() <= 0) {
			basicLogger.error("circleIdslist is not configured in rbtcontentjar.properties file");
			return;
		}
		
		String minConn = RBTContentJarParameters.getInstance().getParameter("minimum_connections");
		String maxConn = RBTContentJarParameters.getInstance().getParameter("maximum_connections");
		for(String circleId : circleIdsList){
			String memCachedServerList = RBTContentJarParameters.getInstance().getParameter(circleId + "_" + "memcached_serverlist");
			String poolName = RBTContentJarParameters.getInstance().getParameter(circleId + "_"  + "pool_name");
			basicLogger.info(circleId + " MemCachedServer List " + memCachedServerList);
			String[] serverlist = memCachedServerList.split(",");

			SockIOPool pool = null;
			MemCachedClient mc = null;
			if(poolName != null)
				pool = SockIOPool.getInstance(poolName);
			else
				pool = SockIOPool.getInstance();
			pool.setServers(serverlist);
			pool.initialize();
			pool.setInitConn(Integer.parseInt(minConn));
			pool.setMinConn(Integer.parseInt(minConn));
			pool.setMaxConn(Integer.parseInt(maxConn));			
			if(poolName != null)
				mc = new MemCachedClient(poolName);
			else
				mc = new MemCachedClient();
			
			memcachedMap.put(circleId, mc);
			poolMap.put(circleId, pool);
		}
		
		
		heartBeatThread = new RBTCacheHeartBeatThread();
		heartBeatThread.start();

		checkCacheInitialized();
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("MemCachedClient is initialized");
		}
		shutdown = false;
	}

	public static boolean isCacheAlive(String circleName) {
		return cacheAliveMap.get(circleName);
	}

	public static boolean isCacheInitialized(String circleName) {
		return cacheInitalizedMap.get(circleName);
	}
	
	private static void checkCacheInitialized()
	{		
		
		Set<String> keys = memcachedMap.keySet();
		
		for(String circleName : keys){
			boolean success = RBTMultiCache.getMemCachedClient(circleName).set(MC_HEART_BEAT_FLAG, MC_HEART_BEAT_FLAG);
			boolean cacheAlive = false;
			boolean cacheInitialized = false;
			if(success) {
				cacheAlive = true;
				if(basicLogger.isDebugEnabled()) {
					basicLogger.debug("Checking if cache is initialized");
				}
				Long isCacheInitialized = (Long)RBTMultiCache.getMemCachedClient(circleName).get(RBTMultiCache.MC_IS_CACHE_INITIALIZED_FLAG);
				if(null != isCacheInitialized) {
					cacheInitialized = true;
					if(basicLogger.isDebugEnabled()) {
						basicLogger.debug("Cache is initialized");
					}
				} else {
					cacheInitialized = false;
					if(basicLogger.isDebugEnabled()) {
						basicLogger.error("Cache is not initialized");
					}
				}
			} else {
				basicLogger.error("ContentCache is not up!!! Please check...");
				cacheAlive = false;
			}
			
			cacheAliveMap.put(circleName, cacheAlive);
			cacheInitalizedMap.put(circleName, cacheInitialized);
		}		
	}

	public static void shutDown() {
		Set<String> keys = poolMap.keySet();
		basicLogger.warn("Stop down the MemCachedHeartBeatThread...");
		heartBeatThread.stopThread();
		basicLogger.warn("Closing the sockets of MemCached client...");
		for(String circleName : keys){			
			poolMap.get(circleName).shutDown();			
		}
		basicLogger.warn("Shutdown normally");
		shutdown = true;
	}
	
	public static boolean isShutdown() {
		return shutdown;
	}

	protected RBTMultiCache() {
	}


	public static MemCachedClient getMemCachedClient(String circleName) {
		return memcachedMap.get(circleName);
	}


	/** 
	 * Class which extends thread and checks the memcached heart beat.
	 * 
	 */
	protected static class RBTCacheHeartBeatThread extends Thread {

		// logger
//		private static Logger log = Logger.getLogger(RBTCacheHeartBeatThread.class.getName());

		//		private RBTCache cache;
		private long interval = 1000 * 60; // every 60 seconds
		private boolean stopThread = false;
		private boolean running;
//		private int counter = 0;

		protected RBTCacheHeartBeatThread() {
			//			this.cache = cache;
			this.setDaemon(true);
			this.setName("RBTCacheHeartBeatThread");
		}

		public void setInterval(long interval) { 
			this.interval = interval; 
		}

		public boolean isRunning() {
			return this.running;
		}

		/** 
		 * sets stop variable and interrupts any wait 
		 */
		public void stopThread() {
			this.stopThread = true;
			this.interrupt();
		}

		/** 
		 * Start the thread.
		 */
		public void run() {
			this.running = true;
			basicLogger.info("Starting the heartBeatThread");
//			System.out.println("Starting the heartBeatThread");
			while(!this.stopThread) {
				try {
					checkCacheInitialized();
					
					Thread.sleep(interval);
				} catch (Exception e) {
					break;
				}
			}
			this.running = false;
		}
	}
}
