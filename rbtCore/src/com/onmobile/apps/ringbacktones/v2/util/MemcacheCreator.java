package com.onmobile.apps.ringbacktones.v2.util;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class MemcacheCreator {

	public static Logger basicLogger = Logger.getLogger(MemcacheCreator.class);
	private MemCachedClient mc = null;
	private SockIOPool pool = null; 
	private boolean shutdown = true;
	
	
	private String memCachedServerList = null;
	private int minConn = 10;
	private int maxConn = 20;
	private String memCacheTimeout = null;
	private String poolName = null;

	
	public MemcacheCreator(String memCachedServerList) {
		super();
		this.memCachedServerList = memCachedServerList;
	}
	
	public MemcacheCreator(String memCachedServerList, int minConn,
			int maxConn, String memCacheTimeout, String poolName) {
		super();
		this.memCachedServerList = memCachedServerList;
		this.minConn = minConn;
		this.maxConn = maxConn;
		this.memCacheTimeout = memCacheTimeout;
		this.poolName = poolName;
	}

	
	public void shutDown(){
		basicLogger.warn("Closing the sockets of MemCached client...");
		pool.shutDown();
		basicLogger.warn("Shutdown normally");
		shutdown = true;
	}
	
	public MemCachedClient initMemcachePool() throws IllegalAccessException {
		if (!shutdown) {
			throw new IllegalAccessException(
					"Reinitialzation is not acceptable without shutting down the existing connection pool");
		}
		basicLogger.warn("Initilizing the MemCachedClient object");
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("MemCachedServer list " + memCachedServerList);
		}
		String[] serverlist = memCachedServerList.split(",");
		if(poolName != null)
			pool = SockIOPool.getInstance(poolName);
		else
			pool = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.initialize();
		pool.setInitConn(minConn);
		pool.setMinConn(minConn);
		pool.setMaxConn(maxConn);
		if(null != memCacheTimeout) {
			try {
				int socketTimeout = Integer.parseInt(memCacheTimeout);
				pool.setSocketTO(socketTimeout);
			} catch (Exception e) {
				basicLogger.error("Unable to parse memcache_socket_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}
				
		if(poolName != null)
			mc = new MemCachedClient(poolName);
		else
			mc = new MemCachedClient();
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("MemCachedClient is initialized");
		}
		shutdown = false;
		return mc;
	}
}
