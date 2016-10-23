/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class SubscriberPromo
{
	private String subscriberID = null;
	private boolean isPrepaid;
	private int freeDays;
	private Date startDate = null;
	private Date endDate = null;
	private String activatedBy = null;
	private String subscriptionType = null;

	/**
	 * 
	 */
	public SubscriberPromo()
	{

	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param freeDays
	 * @param startDate
	 * @param endDate
	 * @param activatedBy
	 * @param subscriptionType
	 */
	public SubscriberPromo(String subscriberID, boolean isPrepaid,
			int freeDays, Date startDate, Date endDate, String activatedBy,
			String subscriptionType)
	{
		this.subscriberID = subscriberID;
		this.isPrepaid = isPrepaid;
		this.freeDays = freeDays;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activatedBy = activatedBy;
		this.subscriptionType = subscriptionType;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the isPrepaid
	 */
	public boolean isPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @return the freeDays
	 */
	public int getFreeDays()
	{
		return freeDays;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy()
	{
		return activatedBy;
	}

	/**
	 * @return the subscriptionType
	 */
	public String getSubscriptionType()
	{
		return subscriptionType;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setPrepaid(boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @param freeDays the freeDays to set
	 */
	public void setFreeDays(int freeDays)
	{
		this.freeDays = freeDays;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy)
	{
		this.activatedBy = activatedBy;
	}

	/**
	 * @param subscriptionType the subscriptionType to set
	 */
	public void setSubscriptionType(String subscriptionType)
	{
		this.subscriptionType = subscriptionType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activatedBy == null) ? 0 : activatedBy.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (isPrepaid ? 1231 : 1237);
		result = prime * result + freeDays;
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + ((subscriptionType == null) ? 0 : subscriptionType.hashCode());
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
		if (!(obj instanceof SubscriberPromo))
			return false;
		SubscriberPromo other = (SubscriberPromo) obj;
		if (activatedBy == null)
		{
			if (other.activatedBy != null)
				return false;
		}
		else if (!activatedBy.equals(other.activatedBy))
			return false;
		if (endDate == null)
		{
			if (other.endDate != null)
				return false;
		}
		else if (!endDate.equals(other.endDate))
			return false;
		if (isPrepaid != other.isPrepaid)
			return false;
		if (freeDays != other.freeDays)
			return false;
		if (startDate == null)
		{
			if (other.startDate != null)
				return false;
		}
		else if (!startDate.equals(other.startDate))
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (subscriptionType == null)
		{
			if (other.subscriptionType != null)
				return false;
		}
		else if (!subscriptionType.equals(other.subscriptionType))
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
		builder.append("SubscriberPromo[activatedBy = ");
		builder.append(activatedBy);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", isPrepaid = ");
		builder.append(isPrepaid);
		builder.append(", freeDays = ");
		builder.append(freeDays);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", subscriptionType = ");
		builder.append(subscriptionType);
		builder.append("]");
		return builder.toString();
	}
}