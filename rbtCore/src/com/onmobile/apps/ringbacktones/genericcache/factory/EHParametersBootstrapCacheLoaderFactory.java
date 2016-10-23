package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.ParametersCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_PARAMETERS cache.
 * 
 * @author manish.shringarpure
 */
public class EHParametersBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static ParametersCacheLoader parametersCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (parametersCacheLoader == null)
			parametersCacheLoader = new ParametersCacheLoader();

		return parametersCacheLoader;
	}

}
