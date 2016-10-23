package com.onmobile.apps.ringbacktones.genericcache.factory;

import com.onmobile.apps.ringbacktones.genericcache.exceptions.GenericCacheException;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator;

/**
 * This factory gives an object of the type ICacheGenerator. ICacheGenerator is
 * used to give the objects to be cached, so any caching mechanism caches the
 * objects in the same fashion for every cache.
 * 
 * @author manish.shringarpure
 */
public class CacheGeneratorFactory
{
	private static ICacheGenerator cacheGenerator;

	/**
	 * Factory method to give the ICacheGenerator reference. The
	 * cacheGeneratorImpl is injected by springs configured in the
	 * rbt-cache-beans.xml.
	 * 
	 * @param cacheGeneratorImpl
	 * @return ICacheGenerator
	 * @throws GenericCacheException
	 */
	public static ICacheGenerator setCacheGenerator(ICacheGenerator cacheGeneratorImpl) throws GenericCacheException
	{
		if (cacheGenerator == null)
			cacheGenerator = cacheGeneratorImpl;
		
		return cacheGenerator;
	}

	/**
	 * API returns the implementation of ICacheGenerator
	 * 
	 * @return ICacheGenerator
	 */
	public static ICacheGenerator getCacheGenerator()
	{
		return cacheGenerator;
	}
}
