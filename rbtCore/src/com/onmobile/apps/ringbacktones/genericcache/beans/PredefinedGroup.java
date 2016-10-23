/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * PredefinedGroup bean used by hibernate to persist into the RBT_PREDEFINED_GROUPS table.
 * 
 * @author vinayasimha.patil
 *
 */
public class PredefinedGroup implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4224406568376271998L;

	private String preGroupID;
	private String preGroupName;

	/**
	 * 
	 */
	public PredefinedGroup()
	{

	}

	/**
	 * @param preGroupID
	 * @param preGroupName
	 */
	public PredefinedGroup(String preGroupID, String preGroupName)
	{
		this.preGroupID = preGroupID;
		this.preGroupName = preGroupName;
	}

	/**
	 * @return the preGroupID
	 */
	public String getPreGroupID()
	{
		return preGroupID;
	}

	/**
	 * @param preGroupID the preGroupID to set
	 */
	public void setPreGroupID(String preGroupID)
	{
		this.preGroupID = preGroupID;
	}

	/**
	 * @return the preGroupName
	 */
	public String getPreGroupName()
	{
		return preGroupName;
	}

	/**
	 * @param preGroupName the preGroupName to set
	 */
	public void setPreGroupName(String preGroupName)
	{
		this.preGroupName = preGroupName;
	}

	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PredefinedGroup clone() throws CloneNotSupportedException
	{
		return (PredefinedGroup) super.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PredefinedGroup[preGroupID = ");
		builder.append(preGroupID);
		builder.append(", preGroupName = ");
		builder.append(preGroupName);
		builder.append("]");
		return builder.toString();
	}
}
