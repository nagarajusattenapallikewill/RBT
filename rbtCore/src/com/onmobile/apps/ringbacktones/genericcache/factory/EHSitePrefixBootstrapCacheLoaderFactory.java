package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.SitePrefixCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_SITE_PREFIX cache.
 * 
 * @author bikash.panda
 */
public class EHSitePrefixBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static SitePrefixCacheLoader sitePrefixCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (sitePrefixCacheLoader == null)
			sitePrefixCacheLoader = new SitePrefixCacheLoader();

		return sitePrefixCacheLoader;
	}
}
