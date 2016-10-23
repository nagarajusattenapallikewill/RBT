package com.onmobile.apps.ringbacktones.service.configuration;

import java.util.List;
import java.util.Map;

public interface ConfigProvider
{
	public String getParamAsString(String paramType, String paramName, String defaultValue);
	public int getParamAsInteger(String paramType, String paramName, int defaultValue);
	public boolean getParamAsBoolean(String paramType, String paramName, boolean defaultValue);
	public List<String> getParamAsStringList(String paramType, String paramName, String defaultValue, String delimiter);
	public Map<String, String> getParamAsSimpleStringMap(String paramType, String paramName, String defaultValue, String pairDelimiter, String keyValueDelimiter);
	public Map<String, List<String>> getParamAsStringToStringListMap(String paramType, String paramName, String defaultValue, String tupleDelimiter, String keyValueDelimiter, String valuesDelimiter);
}
