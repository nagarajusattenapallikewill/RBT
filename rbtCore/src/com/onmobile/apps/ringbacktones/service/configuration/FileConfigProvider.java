package com.onmobile.apps.ringbacktones.service.configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.onmobile.apps.ringbacktones.service.configuration.Config.ConfigValueType;

public class FileConfigProvider implements ConfigProvider
{
	static HashMap<String, Config> configMap = new HashMap<String, Config>();
	static Logger logger = Logger.getLogger(FileConfigProvider.class);
	static Gson gson;
	static RealConfigProvider realConfigProvider = new RealConfigProvider();
	public FileConfigProvider()
	{
		try
		{
			gson = new Gson();
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("config.bundle")));
			String lineStr = null;
			while((lineStr = reader.readLine()) != null)
			{
				Config config = (Config)gson.fromJson(lineStr, Config.class);
				configMap.put(config.getConfigValueType().toString()+"_"+config.getParamType()+"_"+config.getParamName(), config);
			}
			if(logger.isInfoEnabled())
				logger.info("configSet="+configMap);
		}
		catch(Exception e)
		{
			logger.error("Exception while readiung config.bundle", e);
		}
	} 
	
	public String getParamAsString(String paramType, String paramName, String defaultValue)
	{
		String configKey = ConfigValueType.STRING.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueString();
		return realConfigProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	
	public int getParamAsInteger(String paramType, String paramName, int defaultValue)
	{
		String configKey = ConfigValueType.INT.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueInt();
		return realConfigProvider.getParamAsInteger(paramType, paramName, defaultValue);
	}
	
	public boolean getParamAsBoolean(String paramType, String paramName, boolean defaultValue)
	{
		String configKey = ConfigValueType.BOOLEAN.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueBoolean();
		return realConfigProvider.getParamAsBoolean(paramType, paramName, defaultValue);
	}
	
	public List<String> getParamAsStringList(String paramType, String paramName, String defaultValue, String delimiter)
	{
		String configKey = ConfigValueType.STRING_LIST.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueList();
		return realConfigProvider.getParamAsStringList(paramType, paramName, defaultValue, delimiter);
	}
	
	public Map<String, String> getParamAsSimpleStringMap(String paramType, String paramName, String defaultValue, String pairDelimiter, String keyValueDelimiter)
	{
		String configKey = ConfigValueType.SIMPLE_STRING_MAP.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueSimpleStringMap();
		return realConfigProvider.getParamAsSimpleStringMap(paramType, paramName, defaultValue, pairDelimiter, keyValueDelimiter);
	}
	
	public Map<String, List<String>> getParamAsStringToStringListMap(String paramType, String paramName, String defaultValue, String tupleDelimiter, String keyValueDelimiter, String valuesDelimiter)
	{
		String configKey = ConfigValueType.STRING_TO_STRINGLIST_MAP.toString()+"_"+paramType+"_"+paramName;
		if(configMap.containsKey(configKey))
			return configMap.get(configKey).getParamValueStringToStringListMap();
		return realConfigProvider.getParamAsStringToStringListMap(paramType, paramName, defaultValue, tupleDelimiter, keyValueDelimiter, valuesDelimiter);
	}
  
}
