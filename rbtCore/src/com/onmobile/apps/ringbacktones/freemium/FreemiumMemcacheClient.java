package com.onmobile.apps.ringbacktones.freemium;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class FreemiumMemcacheClient {
	private static Logger logger = Logger.getLogger(FreemiumMemcacheClient.class);
	private static MemCachedClient mc = null;
	private static SockIOPool pool = null;
	private static FreemiumMemcacheClient obj = null;
	private static Object object = new Object();
    private boolean isCacheInitialized = false;
    
	static{
		try {
			initMemcachePool();
		} catch (Exception e) {
			logger.info("Exception while initializing Freemium Memcache");
			e.printStackTrace();
		}
	}
    private FreemiumMemcacheClient() {
		
	}

	public static FreemiumMemcacheClient getInstance() {
		if (obj == null) {
			synchronized (object) {
				if (obj == null) {
					obj = new FreemiumMemcacheClient();
				}
			}
		}
		return obj;
	}

	private static void initMemcachePool() throws IllegalAccessException {
		String memCachedServerList = ConfigurationParameter
				.getParameterValue("memcached_serverlist");
		String minConn = ConfigurationParameter.getParameterValue("minimum_connections");
		String maxConn = ConfigurationParameter.getParameterValue("maximum_connections");
		String poolName = ConfigurationParameter.getParameterValue("pool_name", "freemium");

		if (memCachedServerList == null || minConn == null || maxConn == null || poolName== null ) {
			throw new IllegalAccessException(
					"Configurations memcached_serverlist,minimum_connections, "
							+ "and maximum_connections are not configured in properties file");
		}

		if (logger.isInfoEnabled()) {
			logger.info("MemCachedServer list " + memCachedServerList);
		}
		String[] serverlist = memCachedServerList.split(",");
		if (poolName != null)
			pool = SockIOPool.getInstance(poolName);
		else
			pool = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.initialize();
		pool.setInitConn(Integer.parseInt(minConn));
		pool.setMinConn(Integer.parseInt(minConn));
		pool.setMaxConn(Integer.parseInt(maxConn));
		if (poolName != null)
			mc = new MemCachedClient(poolName);
		else
			mc = new MemCachedClient();

		if (logger.isInfoEnabled()) {
			logger.info("MemCachedClient is initialized");
		}
		if(mc!=null){
		     mc.add("freemium", "true");
		}
	}

	public boolean isCacheInitialized() {
		if(null!=mc &&  mc.get("freemium")!=null){
			isCacheInitialized = true;
		}
		return isCacheInitialized;
	}

	public boolean addSubscriberBlacklistTime(String subId , String time){
		if(mc.keyExists(subId)){
			mc.delete(subId);
		}
	    boolean result = mc.add(subId, time);
		return result;
	}
	
	public String getSubscriberBlacklistTime(String subId){
	    String blacklistTime = null;
		if(null!=mc &&  mc.get("freemium")!=null){
			blacklistTime = (String)mc.get(subId);
		}
		return blacklistTime;
	}

}
