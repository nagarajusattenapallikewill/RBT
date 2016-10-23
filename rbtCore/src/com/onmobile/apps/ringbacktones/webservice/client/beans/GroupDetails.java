/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class GroupDetails
{
	private int noOfActiveGroups;
	private Group[] groups = null;

	/**
	 * 
	 */
	public GroupDetails()
	{

	}

	/**
	 * @param noOfActiveGroups
	 * @param groups
	 */
	public GroupDetails(int noOfActiveGroups, Group[] groups)
	{
		this.noOfActiveGroups = noOfActiveGroups;
		this.groups = groups;
	}

	/**
	 * @return the noOfActiveGroups
	 */
	public int getNoOfActiveGroups()
	{
		return noOfActiveGroups;
	}

	/**
	 * @return the groups
	 */
	public Group[] getGroups()
	{
		return groups;
	}

	/**
	 * @param noOfActiveGroups the noOfActiveGroups to set
	 */
	public void setNoOfActiveGroups(int noOfActiveGroups)
	{
		this.noOfActiveGroups = noOfActiveGroups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Group[] groups)
	{
		this.groups = groups;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(groups);
		result = prime * result + noOfActiveGroups;
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
		if (!(obj instanceof GroupDetails))
			return false;
		GroupDetails other = (GroupDetails) obj;
		if (!Arrays.equals(groups, other.groups))
			return false;
		if (noOfActiveGroups != other.noOfActiveGroups)
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
		builder.append("GroupDetails[groups = ");
		builder.append(Arrays.toString(groups));
		builder.append(", noOfActiveGroups = ");
		builder.append(noOfActiveGroups);
		builder.append("]");
		return builder.toString();
	}
}
