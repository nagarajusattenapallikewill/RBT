/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class Group
{
	private String subscriberID = null;
	private String groupID = null;
	private String groupName = null;
	private String groupPromoID = null;
	private String predefinedGroupID = null;
	private String groupStatus = null;
	private String groupNamePrompt = null;
	private int noOfActiveMembers;
	private GroupMember[] groupMembers = null;

	/**
	 * 
	 */
	public Group()
	{

	}

	/**
	 * @param subscriberID
	 * @param groupID
	 * @param groupName
	 * @param groupPromoID
	 * @param predefinedGroupID
	 * @param groupStatus
	 * @param groupNamePrompt
	 * @param noOfActiveMembers
	 * @param groupMembers
	 */
	public Group(String subscriberID, String groupID, String groupName,
			String groupPromoID, String predefinedGroupID, String groupStatus,
			String groupNamePrompt, int noOfActiveMembers, GroupMember[] groupMembers)
	{
		this.subscriberID = subscriberID;
		this.groupID = groupID;
		this.groupName = groupName;
		this.groupPromoID = groupPromoID;
		this.predefinedGroupID = predefinedGroupID;
		this.groupStatus = groupStatus;
		this.groupNamePrompt = groupNamePrompt;
		this.noOfActiveMembers = noOfActiveMembers;
		this.groupMembers = groupMembers;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
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
	 * @return the groupPromoID
	 */
	public String getGroupPromoID()
	{
		return groupPromoID;
	}

	/**
	 * @return the predefinedGroupID
	 */
	public String getPredefinedGroupID()
	{
		return predefinedGroupID;
	}

	/**
	 * @return the groupStatus
	 */
	public String getGroupStatus()
	{
		return groupStatus;
	}

	/**
	 * @return the groupNamePrompt
	 */
	public String getGroupNamePrompt()
	{
		return groupNamePrompt;
	}

	/**
	 * @return the noOfActiveMembers
	 */
	public int getNoOfActiveMembers()
	{
		return noOfActiveMembers;
	}

	/**
	 * @return the groupMembers
	 */
	public GroupMember[] getGroupMembers()
	{
		return groupMembers;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
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
	 * @param groupPromoID the groupPromoID to set
	 */
	public void setGroupPromoID(String groupPromoID)
	{
		this.groupPromoID = groupPromoID;
	}

	/**
	 * @param predefinedGroupID the predefinedGroupID to set
	 */
	public void setPredefinedGroupID(String predefinedGroupID)
	{
		this.predefinedGroupID = predefinedGroupID;
	}

	/**
	 * @param groupStatus the groupStatus to set
	 */
	public void setGroupStatus(String groupStatus)
	{
		this.groupStatus = groupStatus;
	}

	/**
	 * @param groupNamePrompt the groupNamePrompt to set
	 */
	public void setGroupNamePrompt(String groupNamePrompt)
	{
		this.groupNamePrompt = groupNamePrompt;
	}

	/**
	 * @param noOfActiveMembers the noOfActiveMembers to set
	 */
	public void setNoOfActiveMembers(int noOfActiveMembers)
	{
		this.noOfActiveMembers = noOfActiveMembers;
	}

	/**
	 * @param groupMembers the groupMembers to set
	 */
	public void setGroupMembers(GroupMember[] groupMembers)
	{
		this.groupMembers = groupMembers;
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
		result = prime * result + Arrays.hashCode(groupMembers);
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((groupNamePrompt == null) ? 0 : groupNamePrompt.hashCode());
		result = prime * result + ((groupPromoID == null) ? 0 : groupPromoID.hashCode());
		result = prime * result + ((groupStatus == null) ? 0 : groupStatus.hashCode());
		result = prime * result + noOfActiveMembers;
		result = prime * result + ((predefinedGroupID == null) ? 0 : predefinedGroupID.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
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
		if (!(obj instanceof Group))
			return false;
		Group other = (Group) obj;
		if (groupID == null)
		{
			if (other.groupID != null)
				return false;
		}
		else if (!groupID.equals(other.groupID))
			return false;
		if (!Arrays.equals(groupMembers, other.groupMembers))
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
		if (groupPromoID == null)
		{
			if (other.groupPromoID != null)
				return false;
		}
		else if (!groupPromoID.equals(other.groupPromoID))
			return false;
		if (groupStatus == null)
		{
			if (other.groupStatus != null)
				return false;
		}
		else if (!groupStatus.equals(other.groupStatus))
			return false;
		if (noOfActiveMembers != other.noOfActiveMembers)
			return false;
		if (predefinedGroupID == null)
		{
			if (other.predefinedGroupID != null)
				return false;
		}
		else if (!predefinedGroupID.equals(other.predefinedGroupID))
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
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
		builder.append("Group[groupID = ");
		builder.append(groupID);
		builder.append(", groupMembers = ");
		builder.append(Arrays.toString(groupMembers));
		builder.append(", groupName = ");
		builder.append(groupName);
		builder.append(", groupNamePrompt = ");
		builder.append(groupNamePrompt);
		builder.append(", groupPromoID = ");
		builder.append(groupPromoID);
		builder.append(", groupStatus = ");
		builder.append(groupStatus);
		builder.append(", noOfActiveMembers = ");
		builder.append(noOfActiveMembers);
		builder.append(", predefinedGroupID = ");
		builder.append(predefinedGroupID);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append("]");
		return builder.toString();
	}
}
