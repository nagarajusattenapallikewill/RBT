package com.onmobile.apps.ringbacktones.service.configuration;

import java.util.List;
import java.util.Map;

public class Config
{
	public enum ConfigValueType
	{
		STRING, INT, BOOLEAN, STRING_LIST, SIMPLE_STRING_MAP, STRING_TO_STRINGLIST_MAP;
	}
	
	
	ConfigValueType configValueType;
	String paramType;
	String paramName;
	String paramValueString;
	int paramValueInt;
	boolean paramValueBoolean;
	List<String> paramValueList;
	Map<String, String> paramValueSimpleStringMap;
	Map<String, List<String>> paramValueStringToStringListMap;
	
	public Config(ConfigValueType configValueType, String paramType, String paramName)
	{
		this.configValueType = configValueType;
		this.paramType = paramType;
		this.paramName = paramName;
	}

	/**
	 * @return the configValueType
	 */
	public ConfigValueType getConfigValueType() {
		return configValueType;
	}

	/**
	 * @param configValueType the configValueType to set
	 */
	public void setConfigValueType(ConfigValueType configValueType) {
		this.configValueType = configValueType;
	}

	/**
	 * @return the paramType
	 */
	public String getParamType() {
		return paramType;
	}

	/**
	 * @param paramType the paramType to set
	 */
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName the paramName to set
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * @return the paramValueString
	 */
	public String getParamValueString() {
		return paramValueString;
	}

	/**
	 * @param paramValueString the paramValueString to set
	 */
	public void setParamValueString(String paramValueString) {
		this.paramValueString = paramValueString;
	}

	/**
	 * @return the paramValueInt
	 */
	public int getParamValueInt() {
		return paramValueInt;
	}

	/**
	 * @param paramValueInt the paramValueInt to set
	 */
	public void setParamValueInt(int paramValueInt) {
		this.paramValueInt = paramValueInt;
	}

	/**
	 * @return the paramValueBoolean
	 */
	public boolean getParamValueBoolean() {
		return paramValueBoolean;
	}

	/**
	 * @param paramValueBoolean the paramValueBoolean to set
	 */
	public void setParamValueBoolean(boolean paramValueBoolean) {
		this.paramValueBoolean = paramValueBoolean;
	}

	/**
	 * @return the paramValueList
	 */
	public List<String> getParamValueList() {
		return paramValueList;
	}

	/**
	 * @param paramValueList the paramValueList to set
	 */
	public void setParamValueList(List<String> paramValueList) {
		this.paramValueList = paramValueList;
	}

	/**
	 * @return the paramValueSimpleStringMap
	 */
	public Map<String, String> getParamValueSimpleStringMap() {
		return paramValueSimpleStringMap;
	}

	/**
	 * @param paramValueSimpleStringMap the paramValueSimpleStringMap to set
	 */
	public void setParamValueSimpleStringMap(
			Map<String, String> paramValueSimpleStringMap) {
		this.paramValueSimpleStringMap = paramValueSimpleStringMap;
	}

	/**
	 * @return the paramValueStringToStringListMap
	 */
	public Map<String, List<String>> getParamValueStringToStringListMap() {
		return paramValueStringToStringListMap;
	}

	/**
	 * @param paramValueStringToStringListMap the paramValueStringToStringListMap to set
	 */
	public void setParamValueStringToStringListMap(
			Map<String, List<String>> paramValueStringToStringListMap) {
		this.paramValueStringToStringListMap = paramValueStringToStringListMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Config [configValueType=").append(configValueType)
				.append(", paramType=").append(paramType)
				.append(", paramName=").append(paramName)
				.append(", paramValueString=").append(paramValueString)
				.append(", paramValueInt=").append(paramValueInt)
				.append(", paramValueBoolean=").append(paramValueBoolean)
				.append(", paramValueList=").append(paramValueList)
				.append(", paramValueSimpleStringMap=")
				.append(paramValueSimpleStringMap)
				.append(", paramValueStringToStringListMap=")
				.append(paramValueStringToStringListMap).append("]");
		return builder.toString();
	}
	
	public boolean equals(Object object)
	{
		Config config = (Config)object;
		if(config.getConfigValueType() == this.getConfigValueType() && config.getParamType().equals(this.getParamType()) && config.getParamName().equals(this.getParamName()))
			return true;
		return false;
	}
	
	public int hashCode()
	{
		return (this.getConfigValueType().toString().hashCode() + this.getParamType().hashCode() + this.getParamName().hashCode());
	}
}
