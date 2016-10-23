/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class GroupMember implements Comparable<GroupMember>
{
	private String groupID = null;
	private String memberID = null;
	private String memberName = null;
	private String memberStatus = null;

	/**
	 * 
	 */
	public GroupMember()
	{

	}

	/**
	 * @param groupID
	 * @param memberID
	 * @param memberName
	 * @param memberStatus
	 */
	public GroupMember(String groupID, String memberID, String memberName,
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
		result = prime * result + ((memberID == null) ? 0 : memberID.hashCode());
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((memberStatus == null) ? 0 : memberStatus.hashCode());
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
		if (!(obj instanceof GroupMember))
			return false;
		GroupMember other = (GroupMember) obj;
		if (groupID == null)
		{
			if (other.groupID != null)
				return false;
		}
		else if (!groupID.equals(other.groupID))
			return false;
		if (memberID == null)
		{
			if (other.memberID != null)
				return false;
		}
		else if (!memberID.equals(other.memberID))
			return false;
		if (memberName == null)
		{
			if (other.memberName != null)
				return false;
		}
		else if (!memberName.equals(other.memberName))
			return false;
		if (memberStatus == null)
		{
			if (other.memberStatus != null)
				return false;
		}
		else if (!memberStatus.equals(other.memberStatus))
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
		builder.append("GroupMember[groupID = ");
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

	@Override
	public int compareTo(GroupMember other) {
		if (this.memberName != null && other.memberName != null) {
			return this.memberName.compareToIgnoreCase(other.memberName);
		}
		return 0;
	}
}
