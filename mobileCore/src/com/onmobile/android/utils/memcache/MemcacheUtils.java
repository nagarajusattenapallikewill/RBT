package com.onmobile.android.utils.memcache;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;

public class MemcacheUtils {

	private static Logger logger = Logger.getLogger(MemcacheUtils.class);
	public static Object getFromMemcache(String key) {
		Object object = null;
		try {
			MemCachedClient mc = MemcacheClient.getInstance().getMemcache();
			if (mc != null) {
				object = mc.get(key);
			} else {
				logger.error("Memcache configuration missing or invalid.");
			}
		} catch (Exception e) {
			logger.error("Exception caught! " +  e, e);
		}
		return object;
	}
	
	public static boolean addToMemcache(String key, Object value) {
		boolean isAdded = false;
		try {
			MemCachedClient mc = MemcacheClient.getInstance().getMemcache();
			if (mc != null) {
				isAdded = mc.set(key, value);
			} else {
				logger.error("Memcache configuration missing or invalid.");
			}
		} catch (Exception e) {
			logger.error("Exception caught! " +  e, e);
		}
		return isAdded;
	}
}
