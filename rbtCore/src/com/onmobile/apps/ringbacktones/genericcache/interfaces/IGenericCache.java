package com.onmobile.apps.ringbacktones.genericcache.interfaces;

import java.util.List;

/**
 * An interface which all the different caches should implement. This has
 * methods to access the cache and make changes in the cache. The implementation
 * of this interface is injected into the cache managers.
 * 
 * @author manish.shringarpure
 */
public interface IGenericCache
{
	/**
	 * Get data from the cache provided the cache name and the key.
	 * 
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public abstract Object getFromCache(String cacheName, String key);

	/**
	 * This API loads the cache. Clients need not call this API explicitly since
	 * it will get called when you get an instance of CacheManager or when the
	 * implementation is injected in the cache manager
	 */
	public abstract void loadCache();

	/**
	 * Updates the particular cache defined by the cache name with the values
	 * 
	 * @param cacheName
	 * @param key
	 * @param value
	 */
	public abstract void updateToCache(String cacheName, String key, Object value);

	/**
	 * Removes an entry from the cache provided the cache name and the key.
	 * 
	 * @param cacheName
	 * @param key
	 */
	public abstract boolean removeFromCache(String cacheName, String key);

	/**
	 * This API gives all the keys in the cache for a particular cache name.
	 * 
	 * @param cacheName
	 * @return List<String>
	 */
	public abstract List<String> getAllKeysFromCache(String cacheName);

	/**
	 * Some caches provide a shutdown handler or a hook which need to be called
	 * when the application goes down or if there is a abrupt shutdown
	 * encountered.
	 */
	public abstract void safeShutdown();
}
