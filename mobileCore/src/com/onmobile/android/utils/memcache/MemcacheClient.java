package com.onmobile.android.utils.memcache;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class MemcacheClient {

	private static Logger logger = Logger.getLogger(MemcacheClient.class);
	private MemCachedClient mc = null;
	private SockIOPool pool = null; 
	private static MemcacheClient obj = null;
	private static Object object = new Object();
	private static ResourceBundle resourceBundle;

	private MemcacheClient() {
		try {
			initMemcachePool();
		}
		catch(Exception e) {
			logger.error("Exception caught. ", e);
		}
	}

	public static MemcacheClient getInstance() {
		if(obj == null) {
			synchronized (object) {
				if(obj == null) {
					obj  = new MemcacheClient();
				}
			}
		}
		return obj;
	}

	public MemCachedClient getMemcache() {
		return mc;
	}


	private void initMemcachePool() throws IllegalAccessException {
		initProps();
		String memCachedServerList = getParameterValue("memcached_serverlist");
		String minConn = getParameterValue("minimum_connections");
		String maxConn = getParameterValue("maximum_connections");
		String memCacheTimeout = getParameterValue("memcache_socket_timeout");
		String memCacheInitTimeout = getParameterValue("memcache_socket_init_timeout");

		String poolName = getParameterValue("pool_name");
		if(logger.isInfoEnabled()) {
			logger.info("MemCachedServer list " + memCachedServerList);
		}
		String[] serverlist = memCachedServerList.split(",");
		if(poolName != null)
			pool = SockIOPool.getInstance(poolName);
		else
			pool = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.initialize();
		pool.setInitConn(Integer.parseInt(minConn));
		pool.setMinConn(Integer.parseInt(minConn));
		pool.setMaxConn(Integer.parseInt(maxConn));
		if(null != memCacheTimeout) {
			try {
				int socketTimeout = Integer.parseInt(memCacheTimeout);
				pool.setSocketTO(socketTimeout);
			} catch (Exception e) {
				logger.error("Unable to parse memcache_socket_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}
		if(null != memCacheInitTimeout) {
			try {
				int socketInitTimeout = Integer.parseInt(memCacheInitTimeout);
				pool.setSocketTO(socketInitTimeout);
			} catch (Exception e) {
				logger.error("Unable to parse memcache_socket_init_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}

		if(poolName != null)
			mc = new MemCachedClient(poolName);
		else
			mc = new MemCachedClient();

		if(logger.isInfoEnabled()) {
			logger.info("MemCachedClient is initialized");
		}
	}

	private void initProps() {
		try {
			resourceBundle = ResourceBundle.getBundle("memcache");
		}
		catch(MissingResourceException t) {
			logger.error(
					"Exception in loading properties file, memcache.properties. ",
					t);
		}
	}

	private static String getParameterValue(String key, String defaultValue) {
		try {
			return resourceBundle.getString(key);
		}
		catch(MissingResourceException e) {
			logger.info(key + " configuration not exist in properties file");
		}
		catch(NullPointerException e) {
			logger.error("Trying to get parameter withe null Key value", e);
		}
		return defaultValue;
	}

	private static String getParameterValue(String key) {
		return getParameterValue(key, null);
	}

	public static void main (String[] args) {
	
	}
}
