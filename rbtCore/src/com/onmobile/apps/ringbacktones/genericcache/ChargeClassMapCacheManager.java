package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.genericcache.dao.ChargeClassMapDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They
 * should get the instance from CacheManagerUtil class.
 * 
 * @author bikash
 */

public class ChargeClassMapCacheManager
{
	private static Logger logger = Logger.getLogger(SubscriptionClassCacheManager.class);

	private IGenericCache genericCache;

	private ChargeClassMapCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * @param chargeClass
	 * @return ChargeClassMap
	 */
	public ChargeClassMap getChargeClassMap(String chargeClass)
	{
		logger.info("chargeClass: " + chargeClass);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, chargeClass);
		ChargeClassMap chargeClassMap = (ChargeClassMap) object;

		return chargeClassMap;
	}

	/**
	 * Find chargeClassMap for given regexSmSorVoiceTypes
	 * 
	 * @param
	 * @return List<ChargeClassMap>
	 */
	public List<ChargeClassMap> getAllChargeClassMap()
	{
		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			chargeClassMapList.add(chargeClassMap);
		}

		return chargeClassMapList;
	}

	/**
	 * Find chargeClassMap for given regexSmSorVoiceTypes
	 * 
	 * @param regexTypes
	 * @return List<ChargeClassMap>
	 */
	public List<ChargeClassMap> getChargeClassMapsForType(String regexTypes)
	{
		logger.info("regexType : " + regexTypes);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			if (chargeClassMap != null
					&& chargeClassMap.getRegexSmsorVoice() != null
					&& chargeClassMap.getRegexSmsorVoice().startsWith(regexTypes))
			{
				chargeClassMapList.add(chargeClassMap);
			}
		}

		return chargeClassMapList;
	}

	/**
	 * Find chargeClassMap for given accessModes and regexTypes
	 * 
	 * @param modes
	 * @param types
	 * @return List<ChargeClassMap>
	 */
	public List<ChargeClassMap> getChargeClassMapsForModeType(String modes, String types)
	{
		logger.info("modes: " + modes + ", types: " + types);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			if (chargeClassMap != null
					&& chargeClassMap.getAccessMode() != null)
			{
				if (modes.equalsIgnoreCase("VUI"))
				{
					if (chargeClassMap.getRegexSmsorVoice() != null
							&& chargeClassMap.getRegexSmsorVoice().startsWith(types) 
							&& (chargeClassMap.getAccessMode() != null && chargeClassMap.getAccessMode().equals(modes)))
					{
						chargeClassMapList.add(chargeClassMap);
					}
				}
				else
				{
					if (chargeClassMap.getRegexSmsorVoice() != null
							&& chargeClassMap.getRegexSmsorVoice().startsWith(types)
							&& (chargeClassMap.getAccessMode().equals(modes) || chargeClassMap.getAccessMode().equalsIgnoreCase("ALL")))
					{
						chargeClassMapList.add(chargeClassMap);
					}
				}
			}
		}

		return chargeClassMapList;
	}

	/**
	 * Find chargeClassMap for given finalclassTypes and accessMode
	 * 
	 * @param finalClassType
	 * @param accessMode
	 * @return List<ChargeClassMap>
	 */
	public List<ChargeClassMap> getChargeClassMapsForFinalClassType(String finalClassType, String accessMode)
	{
		logger.info("finalClassType: " + finalClassType + ", accessMode: " + accessMode);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			if (chargeClassMap != null
					&& chargeClassMap.getAccessMode() != null)
			{
				if (accessMode.equalsIgnoreCase("VUI"))
				{
					if (chargeClassMap.getFinalClasstype() != null
							&& chargeClassMap.getFinalClasstype().equalsIgnoreCase(finalClassType)
							&& chargeClassMap.getAccessMode().equals(accessMode))
					{
						chargeClassMapList.add(chargeClassMap);
					}
				}
				else
				{
					if (chargeClassMap.getFinalClasstype() != null
							&& chargeClassMap.getFinalClasstype().equalsIgnoreCase(finalClassType)
							&& (chargeClassMap.getAccessMode().equals(accessMode) || chargeClassMap.getAccessMode().equalsIgnoreCase("ALL")))
					{
						chargeClassMapList.add(chargeClassMap);
					}
				}
			}

		}

		return chargeClassMapList;
	}

	/**
	 * Find chargeClassMap for given classTypes and regextype
	 * 
	 * @param classTypes
	 * @param types
	 * @return List<ChargeClassMap>
	 */
	public List<ChargeClassMap> getChargeClassMapsForClassTypeType(String classTypes, String types)
	{
		logger.info("classTypes: " + classTypes + ", types: " + types);
		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			if (chargeClassMap != null)
			{
				if (chargeClassMap.getRegexSmsorVoice() != null
						&& chargeClassMap.getRegexSmsorVoice().startsWith(types)
						&& chargeClassMap.getChargeClass().equalsIgnoreCase(classTypes))
				{
					chargeClassMapList.add(chargeClassMap);
				}
			}

		}

		return chargeClassMapList;
	}

	/**
	 * Find chargeClassMap for given accessMode,classType and regexType
	 * 
	 * @param mode
	 * @param regexType
	 * @param classType
	 * @return ChargeClassMap
	 */
	public ChargeClassMap getChargeClassMapsForModeRegexTypeAndClassType(String mode, String regexType, String classType)
	{
		logger.info("mode: " + mode + ", regexType: " + regexType + ", classType: " + classType);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);
			ChargeClassMap chargeClassMap = (ChargeClassMap) object;
			if (chargeClassMap != null
					&& chargeClassMap.getAccessMode() != null
					&& chargeClassMap.getRegexSmsorVoice() != null)
			{
				if (mode.equalsIgnoreCase("VUI"))
				{
					logger.info("mode is VUI");
					if (chargeClassMap.getChargeClass().equalsIgnoreCase(classType)
							&& chargeClassMap.getRegexSmsorVoice().equalsIgnoreCase(regexType)
							&& (chargeClassMap.getAccessMode().equals(mode)))
					{
						return chargeClassMap;
					}
				}
				else
				{
					logger.info("mode is not VUI");
					if (chargeClassMap.getChargeClass().equalsIgnoreCase(classType)
							&& chargeClassMap.getRegexSmsorVoice().equalsIgnoreCase(regexType)
							&& (chargeClassMap.getAccessMode().equals(mode) || chargeClassMap.getAccessMode().equalsIgnoreCase("ALL")))
					{
						return chargeClassMap;
					}
				}
			}
		}

		return null;
	}

	public boolean addChargeClassMap(ChargeClassMap chargeClassMap)
	{
		logger.info("chargeClassMap: " + chargeClassMap);

		boolean response = false;
		if (chargeClassMap == null)
		{
			logger.info("chargeClassMap is null");
			response = false;
		}
		else
		{
			try
			{
				List<ChargeClassMap> chargeClassMapList = getAllChargeClassMap();
				if (chargeClassMapList != null)
				{
					for (ChargeClassMap chargeClassMapObj : chargeClassMapList)
					{
						if (chargeClassMap.getChargeClass().equalsIgnoreCase(chargeClassMapObj.getChargeClass()))
							return false;
					}
				}

				ChargeClassMapDao chargeClassMapDao = new ChargeClassMapDao();
				chargeClassMapDao.insertChargeClassMap(chargeClassMap);

				String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
				genericCache.updateToCache(cacheName, chargeClassMap.getChargeClass(), chargeClassMap);

				logger.info(" addChargeClassMap added");
				response = true;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				response = false;
			}
		}

		return response;
	}

	public boolean removeChargeClassMap(String chargeClass)
	{
		logger.info("chargeClass: " + chargeClass);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		ChargeClassMap chargeClassMapObj = (ChargeClassMap) genericCache.getFromCache(cacheName, chargeClass);
		if (chargeClassMapObj == null)
		{
			logger.info("This chargeclassmap does not exists ");
			return false;
		}
		try
		{
			ChargeClassMapDao chargeClassMapDao = new ChargeClassMapDao();
			chargeClassMapDao.removeChargeClassMap(chargeClassMapObj);

			genericCache.removeFromCache(cacheName, chargeClass);

			logger.info("chargeClassMap removed successfully ");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean updateChargeClassMap(ChargeClassMap chargeClassMap)
	{
		logger.info("chargeClassMap: " + chargeClassMap);
		if (chargeClassMap == null)
			return false;

		boolean updated = false;

		String cacheName = CacheNamesEnum.CHARGE_CLASS_MAP_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, chargeClassMap.getChargeClass());
		try
		{
			if (object != null)
			{
				if (object instanceof ChargeClassMap)
				{
					ChargeClassMapDao chargeClassMapDao = new ChargeClassMapDao();
					chargeClassMapDao.updateChargeClassMap(chargeClassMap);

					genericCache.updateToCache(cacheName, chargeClassMap.getChargeClass(), chargeClassMap);

					updated = true;
				}
			}
			else
			{
				updated = false;
				logger.info("  updateChargeClassMap does not exist in the Cache. Could'nt update  ChargeClassMap ");
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			updated = false;
		}

		return updated;
	}
}
