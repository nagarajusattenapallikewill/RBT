/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class GroupRequest extends Request
{
	private String groupID = null;
	private String groupName = null;
	private String predefinedGroupID = null;
	private String memberID = null;
	private String memberName = null;
	private String dstGroupID = null;

	/**
	 * @param subscriberID
	 */
	public GroupRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param groupID
	 */
	public GroupRequest(String subscriberID, String groupID)
	{
		super(subscriberID);
		this.groupID = groupID;
	}

	/**
	 * @param subscriberID
	 * @param groupName
	 * @param predefinedGroupID
	 */
	public GroupRequest(String subscriberID, String groupName, String predefinedGroupID)
	{
		super(subscriberID);
		this.groupName = groupName;
		this.predefinedGroupID = predefinedGroupID;
	}

	/**
	 * @param subscriberID
	 * @param groupID
	 * @param memberID
	 * @param memberName
	 */
	public GroupRequest(String subscriberID, String groupID, String memberID,
			String memberName)
	{
		super(subscriberID);
		this.groupID = groupID;
		this.memberID = memberID;
		this.memberName = memberName;
	}

	/**
	 * @param subscriberID
	 * @param groupName
	 * @param predefinedGroupID
	 * @param memberID
	 * @param memberName
	 */
	public GroupRequest(String subscriberID, String groupName,
			String predefinedGroupID, String memberID, String memberName)
	{
		super(subscriberID);
		this.groupName = groupName;
		this.predefinedGroupID = predefinedGroupID;
		this.memberID = memberID;
		this.memberName = memberName;
	}

	/**
	 * @param subscriberID
	 * @param groupID
	 * @param groupName
	 * @param predefinedGroupID
	 * @param memberID
	 * @param memberName
	 * @param dstGroupID
	 */
	public GroupRequest(String subscriberID, String groupID, String groupName,
			String predefinedGroupID, String memberID, String memberName,
			String dstGroupID)
	{
		super(subscriberID);
		this.groupID = groupID;
		this.groupName = groupName;
		this.predefinedGroupID = predefinedGroupID;
		this.memberID = memberID;
		this.memberName = memberName;
		this.dstGroupID = dstGroupID;
	}

	/**
	 * @return the groupID
	 */
	public String getGroupID()
	{
		return groupID;
	}

	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID)
	{
		this.groupID = groupID;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName()
	{
		return groupName;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	/**
	 * @return the predefinedGroupID
	 */
	public String getPredefinedGroupID()
	{
		return predefinedGroupID;
	}

	/**
	 * @param predefinedGroupID the predefinedGroupID to set
	 */
	public void setPredefinedGroupID(String predefinedGroupID)
	{
		this.predefinedGroupID = predefinedGroupID;
	}

	/**
	 * @return the memberID
	 */
	public String getMemberID()
	{
		return memberID;
	}

	/**
	 * @param memberID the memberID to set
	 */
	public void setMemberID(String memberID)
	{
		this.memberID = memberID;
	}

	/**
	 * @return the memberName
	 */
	public String getMemberName()
	{
		return memberName;
	}

	/**
	 * @param memberName the memberName to set
	 */
	public void setMemberName(String memberName)
	{
		this.memberName = memberName;
	}

	/**
	 * @return the dstGroupID
	 */
	public String getDstGroupID()
	{
		return dstGroupID;
	}

	/**
	 * @param dstGroupID the dstGroupID to set
	 */
	public void setDstGroupID(String dstGroupID)
	{
		this.dstGroupID = dstGroupID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (groupID != null) requestParams.put(param_groupID, groupID);
		if (groupName != null) requestParams.put(param_groupName, groupName);
		if (predefinedGroupID != null) requestParams.put(param_predefinedGroupID, predefinedGroupID);
		if (memberID != null) requestParams.put(param_memberID, memberID);
		if (memberName != null) requestParams.put(param_memberName, memberName);
		if (dstGroupID != null) requestParams.put(param_dstGroupID, dstGroupID);

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("GroupRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", dstGroupID = ");
		builder.append(dstGroupID);
		builder.append(", groupID = ");
		builder.append(groupID);
		builder.append(", groupName = ");
		builder.append(groupName);
		builder.append(", memberID = ");
		builder.append(memberID);
		builder.append(", memberName = ");
		builder.append(memberName);
		builder.append(", predefinedGroupID = ");
		builder.append(predefinedGroupID);
		builder.append("]");
		return builder.toString();
	}
}
