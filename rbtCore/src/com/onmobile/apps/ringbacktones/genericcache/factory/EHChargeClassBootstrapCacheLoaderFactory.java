package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.ChargeClassCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_CHARGE_CLASS cache.
 * 
 * @author bikash.panda
 */
public class EHChargeClassBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static ChargeClassCacheLoader chargeClassCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (chargeClassCacheLoader == null)
			chargeClassCacheLoader = new ChargeClassCacheLoader();

		return chargeClassCacheLoader;
	}
}
