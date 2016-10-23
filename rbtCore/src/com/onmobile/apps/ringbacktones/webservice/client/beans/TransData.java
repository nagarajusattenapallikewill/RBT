/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class TransData
{
	private String subscriberID = null;
	private String transID = null;
	private String type = null;
	private Date date = null;
	private String accessCount = null;

	/**
	 * 
	 */
	public TransData()
	{

	}

	/**
	 * @param subscriberID
	 * @param transID
	 * @param type
	 * @param date
	 * @param accessCount
	 */
	public TransData(String subscriberID, String transID, String type,
			Date date, String accessCount)
	{
		this.subscriberID = subscriberID;
		this.transID = transID;
		this.type = type;
		this.date = date;
		this.accessCount = accessCount;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the transID
	 */
	public String getTransID()
	{
		return transID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @return the accessCount
	 */
	public String getAccessCount()
	{
		return accessCount;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param transID the transID to set
	 */
	public void setTransID(String transID)
	{
		this.transID = transID;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

	/**
	 * @param accessCount the accessCount to set
	 */
	public void setAccessCount(String accessCount)
	{
		this.accessCount = accessCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessCount == null) ? 0 : accessCount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + ((transID == null) ? 0 : transID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (!(obj instanceof TransData))
			return false;
		TransData other = (TransData) obj;
		if (accessCount == null)
		{
			if (other.accessCount != null)
				return false;
		}
		else if (!accessCount.equals(other.accessCount))
			return false;
		if (date == null)
		{
			if (other.date != null)
				return false;
		}
		else if (!date.equals(other.date))
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (transID == null)
		{
			if (other.transID != null)
				return false;
		}
		else if (!transID.equals(other.transID))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
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
		builder.append("TransData[accessCount = ");
		builder.append(accessCount);
		builder.append(", date = ");
		builder.append(date);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", transID = ");
		builder.append(transID);
		builder.append(", type = ");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
