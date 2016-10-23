/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceGroup
{
	private String subscriberID = null;
	private String groupID;
	private String groupName = null;
	private String groupPromoID = null;
	private String predefinedGroupID = null;
	private String groupStatus = null;
	private String groupNamePrompt = null;
	private int noOfMembers;

	/**
	 * 
	 */
	public WebServiceGroup()
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
	 * @param noOfMembers
	 */
	public WebServiceGroup(String subscriberID, String groupID,
			String groupName, String groupPromoID, String predefinedGroupID,
			String groupStatus, String groupNamePrompt, int noOfMembers)
	{
		this.subscriberID = subscriberID;
		this.groupID = groupID;
		this.groupName = groupName;
		this.groupPromoID = groupPromoID;
		this.predefinedGroupID = predefinedGroupID;
		this.groupStatus = groupStatus;
		this.groupNamePrompt = groupNamePrompt;
		this.noOfMembers = noOfMembers;
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
	 * @return the noOfMembers
	 */
	public int getNoOfMembers()
	{
		return noOfMembers;
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
	 * @param noOfMembers the noOfMembers to set
	 */
	public void setNoOfMembers(int noOfMembers)
	{
		this.noOfMembers = noOfMembers;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceGroup[groupID = ");
		builder.append(groupID);
		builder.append(", groupName = ");
		builder.append(groupName);
		builder.append(", groupNamePrompt = ");
		builder.append(groupNamePrompt);
		builder.append(", groupPromoID = ");
		builder.append(groupPromoID);
		builder.append(", groupStatus = ");
		builder.append(groupStatus);
		builder.append(", noOfMembers = ");
		builder.append(noOfMembers);
		builder.append(", predefinedGroupID = ");
		builder.append(predefinedGroupID);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append("]");
		return builder.toString();
	}
}
