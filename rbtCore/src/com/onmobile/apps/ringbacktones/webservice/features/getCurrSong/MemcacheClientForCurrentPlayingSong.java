package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;

public class MemcacheClientForCurrentPlayingSong {

	private static Logger logger = Logger
			.getLogger(MemcacheClientForCurrentPlayingSong.class);
	private MemCachedClient mc = null;
	private SockIOPool pool = null;
	private static MemcacheClientForCurrentPlayingSong obj = null;
	private static Object object = new Object();
	private static ResourceBundle resourceBundle;
	private static boolean isCacheAlive = true;
	public static String MC_IS_CACHE_UP_FLAG = "IsCacheUp";

	private MemcacheClientForCurrentPlayingSong() {
		try {
			initMemcachePool();
		} catch (Exception e) {
			logger.error("Exception caught. ", e);
		}
	}

	public static MemcacheClientForCurrentPlayingSong getInstance() {
		if (obj == null) {
			synchronized (object) {
				if (obj == null) {
					obj = new MemcacheClientForCurrentPlayingSong();
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
		if (null != memCacheTimeout) {
			try {
				int socketTimeout = Integer.parseInt(memCacheTimeout);
				pool.setSocketTO(socketTimeout);
			} catch (Exception e) {
				logger.error("Unable to parse memcache_socket_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}
		if (null != memCacheInitTimeout) {
			try {
				int socketInitTimeout = Integer.parseInt(memCacheInitTimeout);
				pool.setSocketTO(socketInitTimeout);
			} catch (Exception e) {
				logger.error("Unable to parse memcache_socket_init_timeout."
						+ " Exception: " + e.getMessage(), e);
			}
		}

		if (poolName != null)
			mc = new MemCachedClient(poolName);
		else
			mc = new MemCachedClient();
		// checkCacheInitialized();
		if (logger.isInfoEnabled()) {
			logger.info("MemCachedClient is initialized");
		}
	}

	private void initProps() {
		try {
			resourceBundle = ResourceBundle
					.getBundle("memcacheConfigForCurrentSong");
		} catch (MissingResourceException t) {
			logger.error(
					"Exception in loading properties file, memcacheConfigForCurrentSong.properties. ",
					t);
		}
	}

	private static String getParameterValue(String key, String defaultValue) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			logger.info(key + " configuration not exist in properties file");
		} catch (NullPointerException e) {
			logger.error("Trying to get parameter withe null Key value", e);
		}
		return defaultValue;
	}

	private static String getParameterValue(String key) {
		return getParameterValue(key, null);
	}

	public boolean isCacheAlive() {
		return isCacheAlive;
	}
	//Changes are done for handling the voldemort issues.
	public void checkCacheInitialized() {
		boolean success = MemcacheClientForCurrentPlayingSong.getInstance()
				.getMemcache().set(MC_IS_CACHE_UP_FLAG, MC_IS_CACHE_UP_FLAG);
		if (success) {
			MemcacheClientForCurrentPlayingSong.isCacheAlive = true;
			if (logger.isDebugEnabled()) {
				logger.debug("Checking if RBTLoginCache is initialized");
			}
		} else {
			logger.error("RBTLoginCache is not up!!! Please check...");
			MemcacheClientForCurrentPlayingSong.isCacheAlive = false;
		}
	}

	public static void main(String[] args) {
		CurrentPlayingSongBean bean = new CurrentPlayingSongBean();
		bean.setCalledId("9886448909");
		bean.setWavFileName("rbt_12648_rbt");
		bean.setCallerId("7204189029");
		bean.setCategoryId(-1);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 5000);
		boolean isAdded = MemcacheClientForCurrentPlayingSong
				.getInstance()
				.getMemcache()
				.set(GetCurrentPlayingSong.getKeyForCurrentPlayingSong(
						"9886448909", "7204189029", "calledId"), bean,
						cal.getTime());
		logger.info("bean: " + bean
				+ " tried to be inserted to memcache. Insertion status: "
				+ isAdded);

		CurrentPlayingSongBean currentSong = (CurrentPlayingSongBean) MemcacheClientForCurrentPlayingSong
				.getInstance().getMemcache().get("calledId_9886448909");
		if (currentSong != null) {
			System.out.println(currentSong);
			String wavFileName = currentSong.getWavFileName();
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					wavFileName);
			System.out.println(clip);
		}
	}
}
