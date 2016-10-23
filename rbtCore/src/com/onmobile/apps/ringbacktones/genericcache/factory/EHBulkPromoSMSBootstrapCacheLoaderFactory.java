package com.onmobile.apps.ringbacktones.genericcache.factory;

import java.util.Properties;

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;

import com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh.BulkPromoSMSCacheLoader;

/**
 * This factory creates a reference of BootstrapCacheLoader, which is used by
 * EHCache to load the RBT_BULK_PROMO_SMS cache.
 * 
 * @author bikash.panda
 */
public class EHBulkPromoSMSBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory
{
	private static BulkPromoSMSCacheLoader bulkPromoSMSCacheLoader;

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory#createBootstrapCacheLoader(java.util.Properties)
	 */
	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties)
	{
		if (bulkPromoSMSCacheLoader == null)
			bulkPromoSMSCacheLoader = new BulkPromoSMSCacheLoader();

		return bulkPromoSMSCacheLoader;
	}
}
