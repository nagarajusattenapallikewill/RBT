package com.onmobile.apps.ringbacktones.genericcache.beans;

public class SubClassBean
{
	private String subName;
	private String subVal;

	/**
	 * 
	 */
	public SubClassBean()
	{

	}

	/**
	 * @param subName
	 * @param subVal
	 */
	public SubClassBean(String subName, String subVal)
	{
		this.subName = subName;
		this.subVal = subVal;
	}

	/**
	 * @return the subName
	 */
	public String getSubName()
	{
		return subName;
	}

	/**
	 * @param subName the subName to set
	 */
	public void setSubName(String subName)
	{
		this.subName = subName;
	}

	/**
	 * @return the subVal
	 */
	public String getSubVal()
	{
		return subVal;
	}

	/**
	 * @param subVal the subVal to set
	 */
	public void setSubVal(String subVal)
	{
		this.subVal = subVal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SubClassBean[subName = ");
		builder.append(subName);
		builder.append(", subVal = ");
		builder.append(subVal);
		builder.append("]");
		return builder.toString();
	}
}
