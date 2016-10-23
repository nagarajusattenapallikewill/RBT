/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class FeedStatus
{
	private String type = null;
	private String status = null;
	private String feedFile = null;
	private String smsKeywords = null;
	private String subKeywords = null;
	private String feedOnSuccessSms = null;
	private String feedOnFailureSms = null;
	private String feedOffSuccessSms = null;
	private String feedOffFailureSms = null;
	private String feedFailureSms = null;
	private String feedNonActiveUserSms = null;

	/**
	 * 
	 */
	public FeedStatus()
	{

	}

	/**
	 * @param type
	 * @param status
	 * @param feedFile
	 * @param smsKeywords
	 * @param subKeywords
	 * @param feedOnSuccessSms
	 * @param feedOnFailureSms
	 * @param feedOffSuccessSms
	 * @param feedOffFailureSms
	 * @param feedFailureSms
	 * @param feedNonActiveUserSms
	 */
	public FeedStatus(String type, String status, String feedFile,
			String smsKeywords, String subKeywords, String feedOnSuccessSms,
			String feedOnFailureSms, String feedOffSuccessSms,
			String feedOffFailureSms, String feedFailureSms,
			String feedNonActiveUserSms)
	{
		this.type = type;
		this.status = status;
		this.feedFile = feedFile;
		this.smsKeywords = smsKeywords;
		this.subKeywords = subKeywords;
		this.feedOnSuccessSms = feedOnSuccessSms;
		this.feedOnFailureSms = feedOnFailureSms;
		this.feedOffSuccessSms = feedOffSuccessSms;
		this.feedOffFailureSms = feedOffFailureSms;
		this.feedFailureSms = feedFailureSms;
		this.feedNonActiveUserSms = feedNonActiveUserSms;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @return the feedFile
	 */
	public String getFeedFile()
	{
		return feedFile;
	}

	/**
	 * @return the smsKeywords
	 */
	public String getSmsKeywords()
	{
		return smsKeywords;
	}

	/**
	 * @return the subKeywords
	 */
	public String getSubKeywords()
	{
		return subKeywords;
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
	 * @return the feedOffSuccessSms
	 */
	public String getFeedOffSuccessSms()
	{
		return feedOffSuccessSms;
	}

	/**
	 * @return the feedOffFailureSms
	 */
	public String getFeedOffFailureSms()
	{
		return feedOffFailureSms;
	}

	/**
	 * @return the feedFailureSms
	 */
	public String getFeedFailureSms()
	{
		return feedFailureSms;
	}

	/**
	 * @return the feedNonActiveUserSms
	 */
	public String getFeedNonActiveUserSms()
	{
		return feedNonActiveUserSms;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @param feedFile the feedFile to set
	 */
	public void setFeedFile(String feedFile)
	{
		this.feedFile = feedFile;
	}

	/**
	 * @param smsKeywords the smsKeywords to set
	 */
	public void setSmsKeywords(String smsKeywords)
	{
		this.smsKeywords = smsKeywords;
	}

	/**
	 * @param subKeywords the subKeywords to set
	 */
	public void setSubKeywords(String subKeywords)
	{
		this.subKeywords = subKeywords;
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

	/**
	 * @param feedOffSuccessSms the feedOffSuccessSms to set
	 */
	public void setFeedOffSuccessSms(String feedOffSuccessSms)
	{
		this.feedOffSuccessSms = feedOffSuccessSms;
	}

	/**
	 * @param feedOffFailureSms the feedOffFailureSms to set
	 */
	public void setFeedOffFailureSms(String feedOffFailureSms)
	{
		this.feedOffFailureSms = feedOffFailureSms;
	}

	/**
	 * @param feedFailureSms the feedFailureSms to set
	 */
	public void setFeedFailureSms(String feedFailureSms)
	{
		this.feedFailureSms = feedFailureSms;
	}

	/**
	 * @param feedNonActiveUserSms the feedNonActiveUserSms to set
	 */
	public void setFeedNonActiveUserSms(String feedNonActiveUserSms)
	{
		this.feedNonActiveUserSms = feedNonActiveUserSms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((feedFailureSms == null) ? 0 : feedFailureSms.hashCode());
		result = prime * result + ((feedFile == null) ? 0 : feedFile.hashCode());
		result = prime * result + ((feedNonActiveUserSms == null) ? 0 : feedNonActiveUserSms.hashCode());
		result = prime * result + ((feedOffFailureSms == null) ? 0 : feedOffFailureSms.hashCode());
		result = prime * result + ((feedOffSuccessSms == null) ? 0 : feedOffSuccessSms.hashCode());
		result = prime * result + ((feedOnFailureSms == null) ? 0 : feedOnFailureSms.hashCode());
		result = prime * result + ((feedOnSuccessSms == null) ? 0 : feedOnSuccessSms.hashCode());
		result = prime * result + ((smsKeywords == null) ? 0 : smsKeywords.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((subKeywords == null) ? 0 : subKeywords.hashCode());
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
		if (!(obj instanceof FeedStatus))
			return false;
		FeedStatus other = (FeedStatus) obj;
		if (feedFailureSms == null)
		{
			if (other.feedFailureSms != null)
				return false;
		}
		else if (!feedFailureSms.equals(other.feedFailureSms))
			return false;
		if (feedFile == null)
		{
			if (other.feedFile != null)
				return false;
		}
		else if (!feedFile.equals(other.feedFile))
			return false;
		if (feedNonActiveUserSms == null)
		{
			if (other.feedNonActiveUserSms != null)
				return false;
		}
		else if (!feedNonActiveUserSms.equals(other.feedNonActiveUserSms))
			return false;
		if (feedOffFailureSms == null)
		{
			if (other.feedOffFailureSms != null)
				return false;
		}
		else if (!feedOffFailureSms.equals(other.feedOffFailureSms))
			return false;
		if (feedOffSuccessSms == null)
		{
			if (other.feedOffSuccessSms != null)
				return false;
		}
		else if (!feedOffSuccessSms.equals(other.feedOffSuccessSms))
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
		if (smsKeywords == null)
		{
			if (other.smsKeywords != null)
				return false;
		}
		else if (!smsKeywords.equals(other.smsKeywords))
			return false;
		if (status == null)
		{
			if (other.status != null)
				return false;
		}
		else if (!status.equals(other.status))
			return false;
		if (subKeywords == null)
		{
			if (other.subKeywords != null)
				return false;
		}
		else if (!subKeywords.equals(other.subKeywords))
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
		builder.append("FeedStatus[feedFailureSms = ");
		builder.append(feedFailureSms);
		builder.append(", feedFile = ");
		builder.append(feedFile);
		builder.append(", feedNonActiveUserSms = ");
		builder.append(feedNonActiveUserSms);
		builder.append(", feedOffFailureSms = ");
		builder.append(feedOffFailureSms);
		builder.append(", feedOffSuccessSms = ");
		builder.append(feedOffSuccessSms);
		builder.append(", feedOnFailureSms = ");
		builder.append(feedOnFailureSms);
		builder.append(", feedOnSuccessSms = ");
		builder.append(feedOnSuccessSms);
		builder.append(", smsKeywords = ");
		builder.append(smsKeywords);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", subKeywords = ");
		builder.append(subKeywords);
		builder.append(", type = ");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
