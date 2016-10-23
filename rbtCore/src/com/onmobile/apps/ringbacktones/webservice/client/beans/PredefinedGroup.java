/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class PredefinedGroup implements Comparable<PredefinedGroup>
{
	private String groupID = null;
	private String groupName = null;
	private String groupNamePrompt = null;

	/**
	 * 
	 */
	public PredefinedGroup()
	{

	}

	/**
	 * @param groupID
	 * @param groupName
	 * @param groupNamePrompt
	 */
	public PredefinedGroup(String groupID, String groupName, String groupNamePrompt)
	{
		this.groupID = groupID;
		this.groupName = groupName;
		this.groupNamePrompt = groupNamePrompt;
	}

	/**
	 * @return the groupID
	 */
	public String getGroupID()
	{
		return groupID;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName()
	{
		return groupName;
	}

	/**
	 * @return the groupNamePrompt
	 */
	public String getGroupNamePrompt()
	{
		return groupNamePrompt;
	}

	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID)
	{
		this.groupID = groupID;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	/**
	 * @param groupNamePrompt the groupNamePrompt to set
	 */
	public void setGroupNamePrompt(String groupNamePrompt)
	{
		this.groupNamePrompt = groupNamePrompt;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((groupNamePrompt == null) ? 0 : groupNamePrompt.hashCode());
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
		if (!(obj instanceof PredefinedGroup))
			return false;
		PredefinedGroup other = (PredefinedGroup) obj;
		if (groupID == null)
		{
			if (other.groupID != null)
				return false;
		}
		else if (!groupID.equals(other.groupID))
			return false;
		if (groupName == null)
		{
			if (other.groupName != null)
				return false;
		}
		else if (!groupName.equals(other.groupName))
			return false;
		if (groupNamePrompt == null)
		{
			if (other.groupNamePrompt != null)
				return false;
		}
		else if (!groupNamePrompt.equals(other.groupNamePrompt))
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
		builder.append("PredefinedGroup[groupID = ");
		builder.append(groupID);
		builder.append(", groupName = ");
		builder.append(groupName);
		builder.append(", groupNamePrompt = ");
		builder.append(groupNamePrompt);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(PredefinedGroup other) {
		if (this.groupName != null && other.groupName != null) {
			return this.groupName.compareToIgnoreCase(other.groupName);
		}
		return 0;
	}
}
