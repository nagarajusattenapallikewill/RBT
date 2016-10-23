package com.onmobile.apps.ringbacktones.genericcache.cacheloaders.eh;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.factory.CacheGeneratorFactory;
import com.onmobile.apps.ringbacktones.genericcache.impl.EHCacheImpl;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator;

/**
 * This class implements the BootstrapCacheLoader which is an interface defined
 * in EHCache. This class gets instantiated from
 * EHBulkPromoSMSBootstrapCacheLoaderFactory The class has a load method which
 * loads the parameters cache and is called by the EHCache when EHCache is
 * configured via a the RBT-EHCACHE.xml.
 * 
 * @author bikash.panda
 */
public class BulkPromoSMSCacheLoader implements BootstrapCacheLoader
{
	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoader#isAsynchronous()
	 */
	public boolean isAsynchronous()
	{
		return false;
	}

	/**
	 * This method loads the cache. The data for the ehCache is returned by the
	 * cacheGeneratorFactory. The CacheGeneratorFactory returns the data from a
	 * DB or any other source. This depends on the implementation of the
	 * ICacheGenerator.
	 */
	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoader#load(net.sf.ehcache.Ehcache)
	 */
	public void load(Ehcache ehCache) throws CacheException
	{
		ICacheGenerator cacheGenerator = CacheGeneratorFactory.getCacheGenerator();
		Map<String, BulkPromoSMS> cacheMap = cacheGenerator.getBulkPromoSMSMapForCaching();
		if (cacheMap != null)
		{
			Set<Entry<String, BulkPromoSMS>> entrySet = cacheMap.entrySet();
			for (Entry<String, BulkPromoSMS> entry : entrySet)
			{
				if (ehCache != null)
					ehCache.put(new Element(entry.getKey(), entry.getValue()));
			}
			
			Logger.getLogger(BulkPromoSMSCacheLoader.class).info(" BulkPromoSMS Cache Loaded ");
		}
		/*Enable replicatePuts to master update new parameter in slave*/
		EHCacheImpl.updateRMICacheRepicatorFactory(ehCache);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException("This class cannot be cloned.");
	}
}
