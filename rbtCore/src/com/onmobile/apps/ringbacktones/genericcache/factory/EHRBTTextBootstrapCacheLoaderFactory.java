/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.RBTTextCacheLoader;

/**
 * @author vinayasimha.patil
 *
 */
public class EHRBTTextBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static RBTTextCacheLoader rbtTextCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (rbtTextCacheLoader == null)
			rbtTextCacheLoader = new RBTTextCacheLoader();

		return rbtTextCacheLoader;
	}
}
