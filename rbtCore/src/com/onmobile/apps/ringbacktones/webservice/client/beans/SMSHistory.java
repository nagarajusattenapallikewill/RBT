/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

/**
 * @author vinayasimha.patil
 *
 */
public class SMSHistory
{
	private String smsType = null;
	private String smsText = null;
	private String sentTime = null;

	/**
	 * 
	 */
	public SMSHistory()
	{

	}

	/**
	 * @param smsType
	 * @param smsText
	 * @param sentTime
	 */
	public SMSHistory(String smsType, String smsText, String sentTime)
	{
		this.smsType = smsType;
		this.smsText = smsText;
		this.sentTime = sentTime;
	}

	/**
	 * @return the smsType
	 */
	public String getSmsType()
	{
		return smsType;
	}

	/**
	 * @return the smsText
	 */
	public String getSmsText()
	{
		return smsText;
	}

	/**
	 * @return the sentTime
	 */
	public String getSentTime()
	{
		return sentTime;
	}

	/**
	 * @param smsType the smsType to set
	 */
	public void setSmsType(String smsType)
	{
		this.smsType = smsType;
	}

	/**
	 * @param smsText the smsText to set
	 */
	public void setSmsText(String smsText)
	{
		this.smsText = smsText;
	}

	/**
	 * @param sentTime the sentTime to set
	 */
	public void setSentTime(String sentTime)
	{
		this.sentTime = sentTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sentTime == null) ? 0 : sentTime.hashCode());
		result = prime * result + ((smsText == null) ? 0 : smsText.hashCode());
		result = prime * result + ((smsType == null) ? 0 : smsType.hashCode());
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
		if (!(obj instanceof SMSHistory))
			return false;
		SMSHistory other = (SMSHistory) obj;
		if (sentTime == null)
		{
			if (other.sentTime != null)
				return false;
		}
		else if (!sentTime.equals(other.sentTime))
			return false;
		if (smsText == null)
		{
			if (other.smsText != null)
				return false;
		}
		else if (!smsText.equals(other.smsText))
			return false;
		if (smsType == null)
		{
			if (other.smsType != null)
				return false;
		}
		else if (!smsType.equals(other.smsType))
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
		builder.append("SMSHistory[sentTime = ");
		builder.append(sentTime);
		builder.append(", smsText = ");
		builder.append(smsText);
		builder.append(", smsType = ");
		builder.append(smsType);
		builder.append("]");
		return builder.toString();
	}
}
