package com.onmobile.apps.ringbacktones.genericcache.impl;

import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;

import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * An implementation of the IGenericCache. This cache uses EHCache for caching.
 * The implementation requires RBT_EHCACHE.xml to be present in the classpath.
 * This bean gets injected into any of the cache managers for doing CRUD
 * functionalities in the cache.
 * 
 * @author manish.shringarpure
 */
public class EHCacheImpl implements IGenericCache
{
	private CacheManager cacheManager = null;

	private EHCacheImpl()
	{
		loadCache();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#loadCache()
	 */
	public void loadCache()
	{
		return;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#getFromCache(java.lang.String, java.lang.String)
	 */
	public Object getFromCache(String cacheName, String key)
	{
		Object object = null;
		if (getCacheManager() != null)
		{
			Cache cache = cacheManager.getCache(cacheName);
			Element value = cache.get(key);
			if (value != null)
				object = value.getObjectValue();
		}

		return object;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#updateToCache(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void updateToCache(String cacheName, String key, Object value)
	{
		if (getCacheManager() != null)
		{
			Cache cache = cacheManager.getCache(cacheName);
			cache.put(new Element(key, value));
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#removeFromCache(java.lang.String, java.lang.String)
	 */
	public boolean removeFromCache(String cacheName, String key)
	{
		boolean removed = false;
		if (getCacheManager() != null)
		{
			Cache cache = cacheManager.getCache(cacheName);
			removed = cache.remove(key);
		}

		return removed;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#safeShutdown()
	 */
	public void safeShutdown()
	{
		getCacheManager().shutdown();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache#getAllKeysFromCache(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllKeysFromCache(String cacheName)
	{
		List<String> keyList = null;
		if (getCacheManager() != null)
		{
			Cache cache = cacheManager.getCache(cacheName);
			keyList = cache.getKeys();
		}

		return keyList;
	}

	/**
	 * @return the cacheManager
	 */
	public CacheManager getCacheManager()
	{
		return cacheManager;
	}

	/**
	 * @param cacheManager the cacheManager to set
	 */
	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}
	
	/**
	 * @param ehcahe
	 * Re-Register RMICacheReplicator factory with replicatePuts=true.
	 * After Re-register, Master will update new parameter in slave, when new parameter got added in master.
	 */
	
	public static void updateRMICacheRepicatorFactory(Ehcache ehCache) {
		Iterator iterator = ehCache.getCacheEventNotificationService().getCacheEventListeners().iterator();
		while(iterator.hasNext()) {
			Object object = iterator.next();
			if(object instanceof RMISynchronousCacheReplicator) {
				RMISynchronousCacheReplicator replicator = (RMISynchronousCacheReplicator) object;
				ehCache.getCacheEventNotificationService().unregisterListener(replicator);
				replicator = new RMISynchronousCacheReplicator(true, true, true, true, true);
				ehCache.getCacheEventNotificationService().registerListener(replicator);
			}
		}

	}
}
