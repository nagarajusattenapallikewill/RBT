package com.onmobile.apps.ringbacktones.service.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.service.configuration.Config.ConfigValueType;

public class RealConfigProvider implements ConfigProvider
{
	static ParametersCacheManager paramCacheManager = CacheManagerUtil.getParametersCacheManager();
	static Logger logger = Logger.getLogger(RealConfigProvider.class);
	static BufferedWriter bufferedWriter;
	static Gson gson;
	static HashSet<Config> configSet = new HashSet<Config>();
	
	static
	{
		String tempDirStr = null;
		try
		{
			gson = new Gson();
			tempDirStr = System.getProperty("java.io.tmpdir");
			File tempDir = new File(tempDirStr);
			if(!tempDir.exists())
				tempDir.mkdirs();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
			String fileName = "configDump_"+sdf.format(Calendar.getInstance().getTime())+".txt";
			bufferedWriter = new BufferedWriter(new FileWriter(new File(tempDir, fileName)));
		}
		catch(Exception e)
		{
			logger.error("Exception in recording configs to java.io.tmpdir folder "+ tempDirStr, e);
		}
	}
	
	private static Parameters getParameter(String paramType, String paramName, String defaultValue)
	{
		return paramCacheManager.getParameter(paramType, paramName, defaultValue);
	}
	
	public String getParamAsString(String paramType, String paramName, String defaultValue)
	{
		Parameters parameter = getParameter(paramType, paramName, defaultValue);
		String parameterValue = parameter.getValue();
		if(parameterValue != null)
			parameterValue = parameterValue.trim();
		else
			parameterValue = defaultValue;
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueString(parameterValue);
			dumpConfigToTempFile(config);
		}
		return parameterValue;
	}
	
	public String getStrParam(String paramType, String paramName, String defaultValue)
	{
		Parameters parameter = getParameter(paramType, paramName, defaultValue);
		String parameterValue = parameter.getValue();
		if(parameterValue != null)
			parameterValue = parameterValue.trim();
		else
			parameterValue = defaultValue;
		return parameterValue;
	}
	
	public int getParamAsInteger(String paramType, String paramName, int defaultValue)
	{
		int parameterValue = defaultValue;
		String parameterValueStr = getStrParam(paramType, paramName, defaultValue+"");
		try
		{
			if(parameterValueStr != null)
				parameterValue = Integer.parseInt(parameterValueStr);
		}
		catch(Exception e)
		{
			logger.error("Exception in parsing config as integer", e);
			parameterValue = defaultValue;
		}
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueInt(parameterValue);
			dumpConfigToTempFile(config);
		}
		return parameterValue;
	}
	
	public boolean getParamAsBoolean(String paramType, String paramName, boolean defaultValue)
	{
		boolean parameterValue = defaultValue;
		String parameterValueStr = getStrParam(paramType, paramName, defaultValue+"");
		if(parameterValueStr != null)
			parameterValue = parameterValueStr.equalsIgnoreCase("yes") || parameterValueStr.equalsIgnoreCase("true") || parameterValueStr.equalsIgnoreCase("on"); 
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueBoolean(parameterValue);
			dumpConfigToTempFile(config);
		}
		return parameterValue;
	}
	
	public List<String> getParamAsStringList(String paramType, String paramName, String defaultValue, String delimiter)
	{
		ArrayList<String> paramValueList = new ArrayList<String>();
		
		if(delimiter == null)
			delimiter = ",";
		
		String parameterValueStr = getStrParam(paramType, paramName, defaultValue+"");
		if(parameterValueStr != null)
			parameterValueStr = parameterValueStr.trim();
		else
			parameterValueStr = defaultValue;
		
		
		if(parameterValueStr != null)
		{	
			StringTokenizer stk = new StringTokenizer(parameterValueStr, delimiter);
			while (stk.hasMoreTokens())
				paramValueList.add(stk.nextToken());
		}
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueList(paramValueList);
			dumpConfigToTempFile(config);
		}
		return paramValueList;
	}
	
	public Map<String, String> getParamAsSimpleStringMap(String paramType, String paramName, String defaultValue, String pairDelimiter, String keyValueDelimiter)
	{
		HashMap<String, String> paramValueMap = new HashMap<String, String>();
		
		if(pairDelimiter == null)
			pairDelimiter = ";";
		if(keyValueDelimiter == null)
			keyValueDelimiter = ":";
		
		String parameterValueStr = getStrParam(paramType, paramName, defaultValue+"");
		if(parameterValueStr != null)
			parameterValueStr = parameterValueStr.trim();
		else
			parameterValueStr = defaultValue;
		
		if(parameterValueStr != null)
		{	
			StringTokenizer stkPairs = new StringTokenizer(parameterValueStr, pairDelimiter);
			while (stkPairs.hasMoreTokens())
			{
				StringTokenizer stkKeyValue = new StringTokenizer(stkPairs.nextToken(), keyValueDelimiter);
				if( stkKeyValue.countTokens() != 2)
					continue;
				paramValueMap.put(stkKeyValue.nextToken(), stkKeyValue.nextToken());
			}
		}
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueSimpleStringMap(paramValueMap);
			dumpConfigToTempFile(config);
		}
		return paramValueMap;
	}
	public Map<String, List<String>> getParamAsStringToStringListMap(String paramType, String paramName, String defaultValue, String tupleDelimiter, String keyValueDelimiter, String valuesDelimiter)
	{
		HashMap<String, List<String>> paramValueMap = new HashMap<String, List<String>>();
		
		if(tupleDelimiter == null)
			tupleDelimiter = ";";
		if(keyValueDelimiter == null)
			keyValueDelimiter = ":";
		if(valuesDelimiter == null)
			valuesDelimiter = ",";
		
		String parameterValueStr = getStrParam(paramType, paramName, defaultValue+"");
		if(parameterValueStr != null)
			parameterValueStr = parameterValueStr.trim();
		else
			parameterValueStr = defaultValue;
		
		if(parameterValueStr != null)
		{
			StringTokenizer stkTuples = new StringTokenizer(parameterValueStr, tupleDelimiter);
			while (stkTuples.hasMoreTokens())
			{
				StringTokenizer stkKeyValue = new StringTokenizer(stkTuples.nextToken(), keyValueDelimiter);
				if( stkKeyValue.countTokens() != 2)
					continue;
				String key = stkKeyValue.nextToken();
				String values = stkKeyValue.nextToken();
				StringTokenizer stkValues = new StringTokenizer(values, valuesDelimiter);
				ArrayList<String> valueList = new ArrayList<String>();
				while(stkValues.hasMoreTokens())
					valueList.add(stkValues.nextToken());
				paramValueMap.put(key, valueList);
			}
		}
		Config config = new Config(ConfigValueType.STRING, paramType, paramName);
		if(!configSet.contains(config))
		{
			config.setParamValueStringToStringListMap(paramValueMap);
			dumpConfigToTempFile(config);
		}
		return paramValueMap;
	}

	synchronized public void dumpConfigToTempFile(Config config)
	{
		try
		{
			String gsonStr = gson.toJson(config);
			bufferedWriter.write(gsonStr);bufferedWriter.newLine();
		}
		catch (Exception e)
		{
			logger.error("Exception while dumping config " + config + " to temp file", e);
		}
	}
}
