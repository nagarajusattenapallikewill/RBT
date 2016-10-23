package com.onmobile.apps.ringbacktones.service.configuration;

public class ConfigurationService
{
	static ConfigProvider configProvider = new RealConfigProvider();
	
	public static void setFileConfigProvider(FileConfigProvider fileConfigProvider)
	{
		configProvider = fileConfigProvider;
	}
	
	public static void setRealConfigProvider(RealConfigProvider realConfigProvider)
	{
		configProvider = realConfigProvider;
	}
	
	public static String getParamAsString(String paramType, String paramName, String defaultValue)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	
	public static String getParamAsInteger(String paramType, String paramName, String defaultValue)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	
	public static String getParamAsBoolean(String paramType, String paramName, String defaultValue)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	
	public static String getParamAsStringList(String paramType, String paramName, String defaultValue, String delimiter)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	public static String getParamAsSimpleStringMap(String paramType, String paramName, String defaultValue, String pairDelimiter, String keyValueDelimiter)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
	public static String getParamAsStringToStringListMap(String paramType, String paramName, String defaultValue, String tupleDelimiter, String keyValueDelimiter, String valuesDelimiter)
	{
		return configProvider.getParamAsString(paramType, paramName, defaultValue);
	}
}
