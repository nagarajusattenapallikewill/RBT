/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.dao.RBTTextDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTTextCacheManager
{
	private static Logger logger = Logger.getLogger(RBTTextCacheManager.class);
	private IGenericCache genericCache;

	private RBTTextCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	public RBTText getRBTText(String type, String subType)
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
		String defaultLanguage = parameter.getValue().toLowerCase();

		return getRBTText(type, subType, defaultLanguage, null);
	}

	public RBTText getRBTText(String type, String subType, String language)
	{
		return getRBTText(type, subType, language, null);
	}
	//This change is for nemo Plugin.
	public RBTText getRBTText(String type, String subType, String language, String circleID)
	{
		return getRBTText(type, subType, language, circleID, false);
	}
	
	public RBTText getRBTText(String type, String subType, String language, String circleID, boolean checkOnlyMyKey)
	{
		boolean queriedSiteLanguageText = false;
		boolean queriedDefaultLanguageText = false;
		if (language == null || language.length() == 0)
		{
			if (circleID != null)
			{
				SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
				if (sitePrefix != null)
				{
					language = sitePrefix.getSiteLanguage();
					queriedSiteLanguageText = true;
				}
			}

			if (language == null)
			{
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
				language = parameter.getValue();
				queriedDefaultLanguageText = true;
			}
		}
		language = language.toLowerCase();

		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		String key = type;
		if (subType != null && subType.length() != 0)
			key += "_" + subType;
		key += "_" + language;

		RBTText rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
		if(checkOnlyMyKey) { //This change is for nemo Plugin.
			return rbtText;
		}
		if (rbtText == null && circleID != null && !queriedSiteLanguageText)
		{
			// If text is not defined for the required language, then site language text will be returned
			SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
			if (sitePrefix != null)
				language = sitePrefix.getSiteLanguage();

			if (language != null)
			{
				language = language.toLowerCase();

				key = type;
				if (subType != null && subType.length() != 0)
					key += "_" + subType;
				key += "_" + language;
				rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
			}
		}

		if (rbtText == null && !queriedDefaultLanguageText)
		{
			// If text is not defined for the required language or for site language, then default language text will be returned
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue().toLowerCase();

			key = type;
			if (subType != null && subType.length() != 0)
				key += "_" + subType;
			key += "_" + language;
			rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
		}

		return rbtText;
	}

	public List<RBTText> getRBTTextsByType(String type)
	{
		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		List<RBTText> rbtTextList = new ArrayList<RBTText>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			if (key.startsWith(type))
			{
				RBTText rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
				if (rbtText == null)
					continue;

				rbtTextList.add(rbtText);
			}
		}

		return rbtTextList;
	}

	public List<RBTText> getRBTTextsByTypeAndLanguage(String type, String language)
	{
		if (language == null || language.length() == 0)
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue();
		}

		language = language.toLowerCase();

		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		List<RBTText> rbtTextList = new ArrayList<RBTText>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			if (key.startsWith(type) && key.endsWith("_" + language))
			{
				RBTText rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
				if (rbtText == null)
					continue;

				rbtTextList.add(rbtText);
			}
		}

		return rbtTextList;
	}

	public List<RBTText> getAllRBTTexts(String language)
	{
		if (language == null || language.length() == 0)
			return null;

		language = language.toLowerCase();

		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		List<RBTText> rbtTextList = new ArrayList<RBTText>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			if (key.endsWith("_" + language))
			{
				RBTText rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
				if (rbtText == null)
					continue;

				rbtTextList.add(rbtText);
			}
		}

		return rbtTextList;
	}

	public List<RBTText> getAllRBTTexts()
	{
		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		List<RBTText> rbtTextList = new ArrayList<RBTText>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			RBTText rbtText = (RBTText) genericCache.getFromCache(cacheName, key);
			if (rbtText == null)
				continue;

			rbtTextList.add(rbtText);
		}

		return rbtTextList;
	}
	
	public boolean addRBTText(RBTText rbtText)
	{
		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		boolean added = false;

		String key = rbtText.getType();
		String subType = rbtText.getSubType();
		if (subType != null && subType.length() != 0)
			key += "_" + subType;
		key += "_" + rbtText.getLanguage();

		Object object = genericCache.getFromCache(cacheName, key);
		if (object == null)
		{
			genericCache.updateToCache(cacheName, key, rbtText);

			RBTTextDao rbtTextDao = new RBTTextDao();
			rbtTextDao.insertRBTText(rbtText);

			added = true;
		}
		else
		{
			added = false;
			logger.info("The RBTText already exists in the Cache. Couldnt add RBTText");
		}
		
		return added;
	}

	public boolean updateRBTText(String type, String subType, String text)
	{
		return updateRBTText(type, subType, null, text);
	}

	public boolean updateRBTText(String type, String subType, String language, String text)
	{
		if (language == null || language.length() == 0)
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue();
		}
		language = language.toLowerCase();

		logger.info("type: " + type + ", subType: " + subType + ", text: " + text + ", language: " + language);

		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();
		boolean updated = false;

		String key = type;
		if (subType != null && subType.length() != 0)
			key += "_" + subType;
		key += "_" + language;

		Object object = genericCache.getFromCache(cacheName, key);
		if (object != null)
		{
			RBTText rbtText = new RBTText(type, subType, language, text);

			genericCache.updateToCache(cacheName, key, rbtText);

			RBTTextDao rbtTextDao = new RBTTextDao();
			rbtTextDao.updateRBTText(rbtText);

			updated = true;
		}
		else
		{
			updated = false;
			logger.info("The RBTText does not exist in the Cache. Couldnt update RBTText");
		}

		return updated;
	}

	public boolean removeRBTText(String type, String subType)
	{
		return removeRBTText(type, subType, null);
	}

	public boolean removeRBTText(String type, String subType, String language)
	{
		if (language == null || language.length() == 0)
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue();
		}
		language = language.toLowerCase();

		logger.info("type: " + type + ", subType: " + subType + ", language: " + language);

		String cacheName = CacheNamesEnum.RBT_TEXT_CACHE.toString();

		String key = type;
		if (subType != null && subType.length() != 0)
			key += "_" + subType;
		key += "_" + language;

		Object object = genericCache.getFromCache(cacheName, key);
		if (object == null)
		{
			logger.info("RBTText doesnot exist");
			return false;
		}

		RBTText rbtText = new RBTText(type, subType, language, null);

		genericCache.removeFromCache(cacheName, key);

		RBTTextDao rbtTextDao = new RBTTextDao();
		rbtTextDao.removeRBTText(rbtText);

		return true;
	}
}
