/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceGroupMember
{
	private String groupID = null;
	private String memberID = null;
	private String memberName = null;
	private String memberStatus = null;

	/**
	 * 
	 */
	public WebServiceGroupMember()
	{

	}

	/**
	 * @param groupID
	 * @param memberID
	 * @param memberName
	 * @param memberStatus
	 */
	public WebServiceGroupMember(String groupID, String memberID, String memberName,
			String memberStatus)
	{
		this.groupID = groupID;
		this.memberID = memberID;
		this.memberName = memberName;
		this.memberStatus = memberStatus;
	}

	/**
	 * @return the groupID
	 */
	public String getGroupID()
	{
		return groupID;
	}

	/**
	 * @return the memberID
	 */
	public String getMemberID()
	{
		return memberID;
	}

	/**
	 * @return the memberName
	 */
	public String getMemberName()
	{
		return memberName;
	}

	/**
	 * @return the memberStatus
	 */
	public String getMemberStatus()
	{
		return memberStatus;
	}

	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(String groupID)
	{
		this.groupID = groupID;
	}

	/**
	 * @param memberID the memberID to set
	 */
	public void setMemberID(String memberID)
	{
		this.memberID = memberID;
	}

	/**
	 * @param memberName the memberName to set
	 */
	public void setMemberName(String memberName)
	{
		this.memberName = memberName;
	}

	/**
	 * @param memberStatus the memberStatus to set
	 */
	public void setMemberStatus(String memberStatus)
	{
		this.memberStatus = memberStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceGroupMember[groupID = ");
		builder.append(groupID);
		builder.append(", memberID = ");
		builder.append(memberID);
		builder.append(", memberName = ");
		builder.append(memberName);
		builder.append(", memberStatus = ");
		builder.append(memberStatus);
		builder.append("]");
		return builder.toString();
	}
}
