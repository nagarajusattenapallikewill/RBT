package com.onmobile.apps.ringbacktones.v2.util;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;

public class OfferMemcache {

	public static Logger basicLogger = Logger.getLogger(OfferMemcache.class);
	
	private String memcacheServerList;
	private int minConn = 5;
	private int maxConn = 10;
	private String poolName;
	private String memCacheTimeout;
	
	private int expiryInSeconds;
	
	private MemCachedClient mc = null;
	
	public void setMemcacheServerList(String memcacheServerList) {
		this.memcacheServerList = memcacheServerList;
	}
	public void setMinConn(int minConn) {
		this.minConn = minConn;
	}
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	public void setMemCacheTimeout(String memCacheTimeout) {
		this.memCacheTimeout = memCacheTimeout;
	}
	public void setExpiryInSeconds(int expiryInSeconds) {
		this.expiryInSeconds = expiryInSeconds;
	}
	
	public void initMemCache() {
		
		MemcacheCreator memcacheCreator = new MemcacheCreator(memcacheServerList, minConn, maxConn, memCacheTimeout, poolName);
		try {
			mc = memcacheCreator.initMemcachePool();
		} catch (IllegalAccessException e) {
			basicLogger.error("Exception while initialized memcache: " , e);
		}
		
	}
	
	public boolean saveData(Object value, String key) {
		if(mc != null) {
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expiryInSeconds);
			
			return mc.set(key, value, cal.getTime());
		}
		
		return false;
	}
	
	public Object getData(String key) {
		if(mc != null) {
			return mc.get(key);
		}
		return null;
	}
	
}
