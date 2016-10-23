/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class Feed
{
	private String feedKeyword = null;
	private String feedName = null;
	private String feedType = null;
	private Date startDate = null;
	private Date endDate = null;
	private String chargeClass = null;
	private String feedOnSuccessSms = null;
	private String feedOnFailureSms = null;

	/**
	 * 
	 */
	public Feed()
	{

	}

	/**
	 * @param feedKeyword
	 * @param feedName
	 * @param feedType
	 * @param startDate
	 * @param endDate
	 * @param chargeClass
	 * @param feedOnSuccessSms
	 * @param feedOnFailureSms
	 */
	public Feed(String feedKeyword, String feedName, String feedType,
			Date startDate, Date endDate, String chargeClass,
			String feedOnSuccessSms, String feedOnFailureSms)
	{
		this.feedKeyword = feedKeyword;
		this.feedName = feedName;
		this.feedType = feedType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.chargeClass = chargeClass;
		this.feedOnSuccessSms = feedOnSuccessSms;
		this.feedOnFailureSms = feedOnFailureSms;
	}

	/**
	 * @return the feedKeyword
	 */
	public String getFeedKeyword()
	{
		return feedKeyword;
	}

	/**
	 * @return the feedName
	 */
	public String getFeedName()
	{
		return feedName;
	}

	/**
	 * @return the feedType
	 */
	public String getFeedType()
	{
		return feedType;
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
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @return the feedOnSuccessSms
	 */
	public String getFeedOnSuccessSms()
	{
		return feedOnSuccessSms;
	}

	/**
	 * @return the feedOnFailureSms
	 */
	public String getFeedOnFailureSms()
	{
		return feedOnFailureSms;
	}

	/**
	 * @param feedKeyword the feedKeyword to set
	 */
	public void setFeedKeyword(String feedKeyword)
	{
		this.feedKeyword = feedKeyword;
	}

	/**
	 * @param feedName the feedName to set
	 */
	public void setFeedName(String feedName)
	{
		this.feedName = feedName;
	}

	/**
	 * @param feedType the feedType to set
	 */
	public void setFeedType(String feedType)
	{
		this.feedType = feedType;
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
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @param feedOnSuccessSms the feedOnSuccessSms to set
	 */
	public void setFeedOnSuccessSms(String feedOnSuccessSms)
	{
		this.feedOnSuccessSms = feedOnSuccessSms;
	}

	/**
	 * @param feedOnFailureSms the feedOnFailureSms to set
	 */
	public void setFeedOnFailureSms(String feedOnFailureSms)
	{
		this.feedOnFailureSms = feedOnFailureSms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chargeClass == null) ? 0 : chargeClass.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((feedKeyword == null) ? 0 : feedKeyword.hashCode());
		result = prime * result + ((feedName == null) ? 0 : feedName.hashCode());
		result = prime * result + ((feedOnFailureSms == null) ? 0 : feedOnFailureSms.hashCode());
		result = prime * result + ((feedOnSuccessSms == null) ? 0 : feedOnSuccessSms.hashCode());
		result = prime * result + ((feedType == null) ? 0 : feedType.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
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
		if (!(obj instanceof Feed))
			return false;
		Feed other = (Feed) obj;
		if (chargeClass == null)
		{
			if (other.chargeClass != null)
				return false;
		}
		else if (!chargeClass.equals(other.chargeClass))
			return false;
		if (endDate == null)
		{
			if (other.endDate != null)
				return false;
		}
		else if (!endDate.equals(other.endDate))
			return false;
		if (feedKeyword == null)
		{
			if (other.feedKeyword != null)
				return false;
		}
		else if (!feedKeyword.equals(other.feedKeyword))
			return false;
		if (feedName == null)
		{
			if (other.feedName != null)
				return false;
		}
		else if (!feedName.equals(other.feedName))
			return false;
		if (feedOnFailureSms == null)
		{
			if (other.feedOnFailureSms != null)
				return false;
		}
		else if (!feedOnFailureSms.equals(other.feedOnFailureSms))
			return false;
		if (feedOnSuccessSms == null)
		{
			if (other.feedOnSuccessSms != null)
				return false;
		}
		else if (!feedOnSuccessSms.equals(other.feedOnSuccessSms))
			return false;
		if (feedType == null)
		{
			if (other.feedType != null)
				return false;
		}
		else if (!feedType.equals(other.feedType))
			return false;
		if (startDate == null)
		{
			if (other.startDate != null)
				return false;
		}
		else if (!startDate.equals(other.startDate))
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
		builder.append("Feed[chargeClass = ");
		builder.append(chargeClass);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", feedKeyword = ");
		builder.append(feedKeyword);
		builder.append(", feedName = ");
		builder.append(feedName);
		builder.append(", feedOnFailureSms = ");
		builder.append(feedOnFailureSms);
		builder.append(", feedOnSuccessSms = ");
		builder.append(feedOnSuccessSms);
		builder.append(", feedType = ");
		builder.append(feedType);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append("]");
		return builder.toString();
	}
}
