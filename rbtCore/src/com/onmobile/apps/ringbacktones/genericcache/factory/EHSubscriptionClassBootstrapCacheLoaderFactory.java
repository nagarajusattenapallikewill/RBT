package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.SubscriptionClassCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_SUBSCRIPTION_CLASS cache.
 * 
 * @author manish.shringarpure
 */
public class EHSubscriptionClassBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static SubscriptionClassCacheLoader subscriptionClassCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (subscriptionClassCacheLoader == null)
			subscriptionClassCacheLoader = new SubscriptionClassCacheLoader();
		
		return subscriptionClassCacheLoader;
	}
}
