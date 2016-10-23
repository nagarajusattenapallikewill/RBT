package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.dao.ParametersDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They
 * should get the instance from CacheManagerUtil class.
 * 
 * @author manish.shringarpure
 */
public class ParametersCacheManager
{
	private static Logger logger = Logger.getLogger(ParametersCacheManager.class);
	private IGenericCache genericCache;

	private ParametersCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
	}

	/**
	 * @param type
	 * @return List<Parameters>
	 */
	public List<Parameters> getParameters(String type)
	{
		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
		List<Parameters> parametersList = null;

		Object object = genericCache.getFromCache(cacheName, type);
		if (object == null)
			return null;

		if (object instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;
			parametersList = new ArrayList<Parameters>(parametersMap.values());
		}

		return parametersList;
	}

	/**
	 * @return List<Parameters>
	 */
	public List<Parameters> getAllParameters()
	{
		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
		List<Parameters> parametersList = new ArrayList<Parameters>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key);

			if (object instanceof Map<?, ?>)
			{
				@SuppressWarnings("unchecked")
				Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;

				Set<String> keySet = parametersMap.keySet();
				for (String paramKey : keySet)
					parametersList.add(parametersMap.get(paramKey));
			}
		}

		return parametersList;
	}

	/**
	 * @return List<String>
	 */
	public List<String> getAllParametersTypes()
	{
		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		return keyList;
	}

	/**
	 * @param type
	 * @param paramName
	 * @param defaultVal
	 * @return Parameres
	 */
	public Parameters getParameter(String type, String paramName, String defaultVal)
	{
		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
		Parameters parameter = null;

		Object object = genericCache.getFromCache(cacheName, type);
		if (object == null)
		{
			logger.info("type: " + type + " param: " + paramName + " is not configured. We consider default value " + defaultVal);
			if(defaultVal != null) {
				parameter = new Parameters(type, paramName, defaultVal, null);
			}
			
			return parameter;
		}

		if (object instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;

			parameter = parametersMap.get(paramName);
			if (parameter == null) {
				logger.info("type: " + type + " param: " + paramName + " is not configured. We consider default value " + defaultVal);
				if(defaultVal != null) {
					parameter = new Parameters(type, paramName, defaultVal, null);
				}
			}
			else {
				logger.info("type: " + type + " param: " + paramName + " value: " + parameter.getValue() + " is configured");
			}
		}

		return parameter;
	}

   
   /**
	 * @param type
	 * @param paramName
	 * @param defaultVal
	 * @return value
	 */
	public String getParameterValue(String type, String paramName, String defaultVal)
	{
//		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
//		Parameters parameter = null;
//		Object object = genericCache.getFromCache(cacheName, type);
//		if (object == null)
//		{
//			return defaultVal;
//		}
//
//		if (object instanceof Map<?, ?>)
//		{
//			@SuppressWarnings("unchecked")
//			Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;
//			parameter = parametersMap.get(paramName);
//			if (parameter == null )
//				return defaultVal;
//		}
//
//		return parameter.getValue();
		Parameters parameter = getParameter(type, paramName, defaultVal);
		return parameter != null ? parameter.getValue() : defaultVal;
	}

	/**
	 * @param type
	 * @param paramName
	 * @return Parameres
	 */
	public Parameters getParameter(String type, String paramName)
	{
//		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
//		Parameters parameter = null;
//
//		Object object = genericCache.getFromCache(cacheName, type);
//		if (object == null)
//			return null;
//
//		if (object instanceof Map<?, ?>)
//		{
//			@SuppressWarnings("unchecked")
//			Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;
//			parameter = parametersMap.get(paramName);
//		}
//
//		return parameter;
		return getParameter(type, paramName, null);
	}

	/**
	 * Update Parameter table and cache with given values
	 * 
	 * @param type
	 * @param paramName
	 * @param value
	 * @return
	 */
	public boolean updateParameter(String type, String paramName, String value)
	{
		return updateParameter(type, paramName, value, null);
	}

	/**
	 * Update Parameter table and cache with given values
	 * 
	 * @param type
	 * @param paramName
	 * @param value
	 * @param paramInfo
	 * @return
	 */
	public boolean updateParameter(String type, String paramName, String value, String paramInfo)
	{
		logger.info("type: " + type + ", paramName: " + paramName + ", value: " + value + ", paramInfo: " + paramInfo);

		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();
		boolean updated = false;

		Object object = genericCache.getFromCache(cacheName, type);
		if (object != null)
		{
			if (object instanceof Map<?, ?>)
			{
				@SuppressWarnings("unchecked")
				Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;

				Parameters parameter = new Parameters(type, paramName, value, paramInfo);

				parametersMap.put(paramName, parameter);
				genericCache.updateToCache(cacheName, type, parametersMap);

				ParametersDao parametersDao = new ParametersDao();
				parametersDao.updateParameters(parameter);

				updated = true;
			}
		}
		else
		{
			updated = false;
			logger.info("The parameter does not exist in the Cache. Couldnt update Param");
		}

		return updated;
	}

	/**
	 *  Add a parameter to Parameter table and cache with given values
	 *  
	 * @param type
	 * @param paramName
	 * @param value
	 * @return
	 */
	public boolean addParameter(String type, String paramName, String value)
	{
		return addParameter(type, paramName, value, null);
	}

	/**
	 * Add a parameter to Parameter table and cache with given values
	 * 
	 * @param type
	 * @param paramName
	 * @param value
	 * @param paramInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean addParameter(String type, String paramName, String value, String paramInfo)
	{
		logger.info("type: " + type + ", paramName: " + paramName + ", value: " + value + ", paramInfo: " + paramInfo);

		if (type == null || paramName == null || value == null)
			return false;

		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();

		Parameters parameter = new Parameters(type, paramName, value, paramInfo);

		ParametersDao parametersDao = new ParametersDao();
		parametersDao.insertParameters(parameter);

		Object object = genericCache.getFromCache(cacheName, type);

		Map<String, Parameters> parametersMap = null;
		// If the type already exists in the cache
		if (object != null)
		{
			if (object instanceof Map<?, ?>)
				parametersMap = (Map<String, Parameters>) object;
		}
		else
		{ 
			// if the type does not exist in the cache
			parametersMap = new HashMap<String, Parameters>();
		}

		parametersMap.put(paramName, parameter);
		genericCache.updateToCache(cacheName, type, parametersMap);

		return true;
	}

	/**
	 * Remove a parameter from rbt_Parameters table and cache
	 * 
	 * @param type
	 * @param paramName
	 * @return
	 */
	public boolean removeParameter(String type, String paramName)
	{
		logger.info("type: " + type + ", paramName: " + paramName);

		String cacheName = CacheNamesEnum.PARAMETERS_CACHE.toString();

		Object object = genericCache.getFromCache(cacheName, type);
		if (object == null)
		{
			logger.info("Param doesnot exists");
			return false;
		}

		if (object instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, Parameters> parametersMap = (Map<String, Parameters>) object;

			Parameters parameter = new Parameters(type, paramName, null, null);

			parametersMap.remove(paramName);
			genericCache.updateToCache(cacheName, type, parametersMap);

			ParametersDao parametersDao = new ParametersDao();
			parametersDao.removeParameter(parameter);

			return true;
		}
		
		return false;
	}
}
