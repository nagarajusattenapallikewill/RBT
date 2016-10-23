/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class CopyDetails
{
	private String subscriberID = null;
	private String fromSubscriber = null;
	private boolean userHasMultipleSelections;
	private CopyData[] copyData = null;

	/**
	 * 
	 */
	public CopyDetails()
	{

	}

	/**
	 * @param subscriberID
	 * @param fromSubscriber
	 * @param userHasMultipleSelections
	 * @param copyData
	 */
	public CopyDetails(String subscriberID, String fromSubscriber,
			boolean userHasMultipleSelections, CopyData[] copyData)
	{
		this.subscriberID = subscriberID;
		this.fromSubscriber = fromSubscriber;
		this.userHasMultipleSelections = userHasMultipleSelections;
		this.copyData = copyData;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the fromSubscriber
	 */
	public String getFromSubscriber()
	{
		return fromSubscriber;
	}

	/**
	 * @param fromSubscriber the fromSubscriber to set
	 */
	public void setFromSubscriber(String fromSubscriber)
	{
		this.fromSubscriber = fromSubscriber;
	}

	/**
	 * @return the userHasMultipleSelections
	 */
	public boolean isUserHasMultipleSelections()
	{
		return userHasMultipleSelections;
	}

	/**
	 * @param userHasMultipleSelections the userHasMultipleSelections to set
	 */
	public void setUserHasMultipleSelections(boolean userHasMultipleSelections)
	{
		this.userHasMultipleSelections = userHasMultipleSelections;
	}

	/**
	 * @return the copyData
	 */
	public CopyData[] getCopyData()
	{
		return copyData;
	}

	/**
	 * @param copyData the copyData to set
	 */
	public void setCopyData(CopyData[] copyData)
	{
		this.copyData = copyData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(copyData);
		result = prime * result + ((fromSubscriber == null) ? 0 : fromSubscriber.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + (userHasMultipleSelections ? 1231 : 1237);
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
		if (!(obj instanceof CopyDetails))
			return false;
		CopyDetails other = (CopyDetails) obj;
		if (!Arrays.equals(copyData, other.copyData))
			return false;
		if (fromSubscriber == null)
		{
			if (other.fromSubscriber != null)
				return false;
		}
		else if (!fromSubscriber.equals(other.fromSubscriber))
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (userHasMultipleSelections != other.userHasMultipleSelections)
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
		builder.append("CopyDetails[copyData = ");
		builder.append(Arrays.toString(copyData));
		builder.append(", fromSubscriber = ");
		builder.append(fromSubscriber);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", userHasMultipleSelections = ");
		builder.append(userHasMultipleSelections);
		builder.append("]");
		return builder.toString();
	}
}
