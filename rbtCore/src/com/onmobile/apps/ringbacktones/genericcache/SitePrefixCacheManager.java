package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.dao.SitePrefixDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They
 * should get the instance from CacheManagerUtil class.
 * 
 * @author bikash.panda
 */

public class SitePrefixCacheManager
{
	private static Logger logger = Logger.getLogger(SitePrefixCacheManager.class);

	private IGenericCache genericCache;

	private SitePrefixCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * Get SitePrefix for specific circle ID
	 * 
	 * @param circleID
	 * @return SitePrefix
	 */
	public SitePrefix getSitePrefixes(String circleID)
	{
		logger.info("circleID: " + circleID);
		String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();
		Object object = null;
		try
		{
			object = genericCache.getFromCache(cacheName, circleID);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		SitePrefix sitePrefix = (SitePrefix) object;

		logger.info("sitePrefix: " + sitePrefix);
		return sitePrefix;
	}

	/**
	 * @param circleID
	 * @param language
	 * @return SitePrefix
	 */
	public SitePrefix getSitePrefixes(String circleID, String language)
	{
		SitePrefix sitePrefix = getSitePrefixes(circleID);
		sitePrefix = updateLanguageSpecificInfo(sitePrefix, language);

		return sitePrefix;
	}

	/**
	 * Get All SitePrefixes
	 * 
	 * @param
	 * @return List<SitePrefix>
	 */
	public List<SitePrefix> getAllSitePrefix()
	{
		String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();
		List<SitePrefix> prefixList = new ArrayList<SitePrefix>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			SitePrefix sitePrefix = (SitePrefix) object;
			prefixList.add(sitePrefix);
		}

		return prefixList;
	}

	/**
	 * @param language
	 * @return List<SitePrefix>
	 */
	public List<SitePrefix> getAllSitePrefix(String language)
	{
		List<SitePrefix> langSpecificPrefixList = new ArrayList<SitePrefix>(); 

		List<SitePrefix> prefixList = getAllSitePrefix();
		for (SitePrefix sitePrefix : prefixList)
		{
			langSpecificPrefixList.add(updateLanguageSpecificInfo(sitePrefix, language));
		}

		return langSpecificPrefixList;
	}
	
	/**
	 * @return List<SitePrefix>
	 */
	public List<SitePrefix> getLocalSitePrefixes()
	{
		String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();
		List<SitePrefix> prefixList = new ArrayList<SitePrefix>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			SitePrefix sitePrefix = (SitePrefix) object;
			if (sitePrefix.getSiteUrl() == null)
				prefixList.add(sitePrefix);
		}

		return prefixList;
	}
	
	/**
	 * @param language
	 * @return List<SitePrefix>
	 */
	public List<SitePrefix> getLocalSitePrefixes(String language)
	{
		List<SitePrefix> langSpecificPrefixList = new ArrayList<SitePrefix>(); 

		List<SitePrefix> prefixList = getLocalSitePrefixes();
		for (SitePrefix sitePrefix : prefixList)
		{
			langSpecificPrefixList.add(updateLanguageSpecificInfo(sitePrefix, language));
		}

		return langSpecificPrefixList;
	}

	public boolean addSitePrefix(SitePrefix sitePrefix)
	{
		if (sitePrefix == null)
			return false;

		logger.info("sitePrefix : " + sitePrefix);

		try
		{
			String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();
			SitePrefixDao sitePrefixDao = new SitePrefixDao();
			sitePrefixDao.insertSitePrefix(sitePrefix);

			genericCache.updateToCache(cacheName, sitePrefix.getCircleID(), sitePrefix);

			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Update Site Prefix table table and cache
	 * 
	 * @param SitePrefix
	 * @return
	 */
	public boolean updateSitePrefix(SitePrefix sitePrefix)
	{
		if (sitePrefix == null)
		{
			logger.info("SitePrefix is null ");
			return false;
		}

		String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, sitePrefix.getCircleID());
		try
		{
			if (object != null)
			{
				if (object instanceof SitePrefix)
				{
					SitePrefixDao sitePrefixDao = new SitePrefixDao();
					sitePrefixDao.updateSitePrefix(sitePrefix);

					genericCache.updateToCache(cacheName, sitePrefix.getCircleID(), sitePrefix);
					return true;
				}
			}
			else
			{
				logger.info("The Siteprefix does not exist in the Cache. Couldnt update Siteprefix");
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return false;
	}

	/**
	 * Remove a SitePrefix from RBT_SITE_PREFIX table and cache
	 * 
	 * @param CircleID
	 * @return
	 */
	public boolean removeSitePrefix(String circleID)
	{

		logger.info("circleID: " + circleID);
		String cacheName = CacheNamesEnum.SITE_PREFIX_CACHE.toString();

		SitePrefix sitePrefixObject = (SitePrefix) genericCache.getFromCache(cacheName, circleID);
		if (sitePrefixObject == null)
		{
			logger.info("SitePrefix does not exist");
			return false;
		}
		try
		{
			SitePrefixDao sitePrefixDao = new SitePrefixDao();
			sitePrefixDao.removeSitePrefix(sitePrefixObject);

			genericCache.removeFromCache(cacheName, circleID);

			logger.info(" removeSitePrefix : siteprefix removed successfully");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private SitePrefix updateLanguageSpecificInfo(SitePrefix sitePrefix, String language)
	{
		SitePrefix newSitePrefix = sitePrefix;
		if (sitePrefix != null)
		{
			
			String circleID = sitePrefix.getCircleID();

			RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SITE_NAME", circleID, language);
			if (rbtText != null)
			{
				try
				{
					newSitePrefix = sitePrefix.clone();
					newSitePrefix.setSiteName(rbtText.getText());
				}
				catch (CloneNotSupportedException e)
				{
				}
			}
		}

		return newSitePrefix;
	}
}
