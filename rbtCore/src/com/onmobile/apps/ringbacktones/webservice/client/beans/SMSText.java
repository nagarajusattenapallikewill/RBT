/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class SMSText
{
	private String type = null;
	private HashMap<String, String> smsConditionMap = null;

	/**
	 * 
	 */
	public SMSText()
	{

	}

	/**
	 * @param type
	 * @param smsConditionMap
	 */
	public SMSText(String type, HashMap<String, String> smsConditionMap)
	{
		this.type = type;
		this.smsConditionMap = smsConditionMap;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the smsConditionMap
	 */
	public HashMap<String, String> getSmsConditionMap()
	{
		return smsConditionMap;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @param smsConditionMap the smsConditionMap to set
	 */
	public void setSmsConditionMap(HashMap<String, String> smsConditionMap)
	{
		this.smsConditionMap = smsConditionMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((smsConditionMap == null) ? 0 : smsConditionMap.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SMSText))
			return false;
		SMSText other = (SMSText) obj;
		if (smsConditionMap == null)
		{
			if (other.smsConditionMap != null)
				return false;
		}
		else if (!smsConditionMap.equals(other.smsConditionMap))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SMSText[smsConditionMap = ");
		builder.append(smsConditionMap);
		builder.append(", type = ");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
