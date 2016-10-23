package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * Parameters bean used by hibernate to persist into the RBT_PARAMETERS table.
 * 
 * @author manish.shringarpure
 */
public class Parameters implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 786543562670987L;

	private String type;
	private String param;
	private String value;
	private String info;

	/**
	 * 
	 */
	public Parameters()
	{
		
	}
	
	/**
	 * @param type
	 * @param param
	 * @param value
	 * @param info
	 */
	public Parameters(String type, String param, String value, String info)
	{
		this.type = type;
		this.param = param;
		this.value = value;
		this.info = info;
	}
	
	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the param
	 */
	public String getParam()
	{
		return param;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(String param)
	{
		this.param = param;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Parameters[info = ");
		builder.append(info);
		builder.append(", param = ");
		builder.append(param);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", value = ");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
