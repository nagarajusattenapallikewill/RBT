/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.PredefinedGroupCacheLoader;

/**
 * @author vinayasimha.patil
 *
 */
public class EHPredefinedGroupBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static PredefinedGroupCacheLoader predefinedGroupCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (predefinedGroupCacheLoader == null)
			predefinedGroupCacheLoader = new PredefinedGroupCacheLoader();

		return predefinedGroupCacheLoader;
	}
}
