package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.CosDetailsCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_COS_DETAILS cache.
 * 
 * @author bikash.panda
 */
public class EHCosDetailsBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static CosDetailsCacheLoader cosDetailsCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (cosDetailsCacheLoader == null)
			cosDetailsCacheLoader = new CosDetailsCacheLoader();

		return cosDetailsCacheLoader;
	}
}
