package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.ChargeClassMapCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_CHARGE_CLASS_MAP cache.
 * 
 * @author bikash.panda
 */
public class EHChargeClassMapBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static ChargeClassMapCacheLoader chargeClassMapCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (chargeClassMapCacheLoader == null)
			chargeClassMapCacheLoader = new ChargeClassMapCacheLoader();

		return chargeClassMapCacheLoader;
	}
}
