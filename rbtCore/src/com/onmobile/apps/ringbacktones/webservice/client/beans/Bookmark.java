package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Bookmark 
{
	private String subscriberID = null;
	private int toneID;
	private int shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String previewFile = null;
	private String rbtFile = null;
	private int categoryID;
	private Date endDate;
	private String amount = null;

	/**
	 * 
	 */
	public Bookmark()
	{

	}

	/**
	 * @param subscriberID
	 * @param toneID
	 * @param shuffleID
	 * @param toneName
	 * @param toneType
	 * @param previewFile
	 * @param rbtFile
	 * @param categoryID
	 * @param endDate
	 * @param amount
	 */
	public Bookmark(String subscriberID, int toneID, int shuffleID,
			String toneName, String toneType, String previewFile,
			String rbtFile, int categoryID, Date endDate, String amount)
	{
		this.subscriberID = subscriberID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFile = previewFile;
		this.rbtFile = rbtFile;
		this.categoryID = categoryID;
		this.endDate = endDate;
		this.amount = amount;
	}


	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
	}

	/**
	 * @return the shuffleID
	 */
	public int getShuffleID()
	{
		return shuffleID;
	}

	/**
	 * @return the toneName
	 */
	public String getToneName()
	{
		return toneName;
	}

	/**
	 * @return the toneType
	 */
	public String getToneType()
	{
		return toneType;
	}

	/**
	 * @return the previewFile
	 */
	public String getPreviewFile()
	{
		return previewFile;
	}

	/**
	 * @return the rbtFile
	 */
	public String getRbtFile()
	{
		return rbtFile;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @return the amount
	 */
	public String getAmount()
	{
		return amount;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(int toneID)
	{
		this.toneID = toneID;
	}

	/**
	 * @param shuffleID the shuffleID to set
	 */
	public void setShuffleID(int shuffleID)
	{
		this.shuffleID = shuffleID;
	}

	/**
	 * @param toneName the toneName to set
	 */
	public void setToneName(String toneName)
	{
		this.toneName = toneName;
	}

	/**
	 * @param toneType the toneType to set
	 */
	public void setToneType(String toneType)
	{
		this.toneType = toneType;
	}

	/**
	 * @param previewFile the previewFile to set
	 */
	public void setPreviewFile(String previewFile)
	{
		this.previewFile = previewFile;
	}

	/**
	 * @param rbtFile the rbtFile to set
	 */
	public void setRbtFile(String rbtFile)
	{
		this.rbtFile = rbtFile;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + categoryID;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((previewFile == null) ? 0 : previewFile.hashCode());
		result = prime * result + ((rbtFile == null) ? 0 : rbtFile.hashCode());
		result = prime * result + shuffleID;
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + toneID;
		result = prime * result + ((toneName == null) ? 0 : toneName.hashCode());
		result = prime * result + ((toneType == null) ? 0 : toneType.hashCode());
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
		if (!(obj instanceof Bookmark))
			return false;
		Bookmark other = (Bookmark) obj;
		if (amount == null)
		{
			if (other.amount != null)
				return false;
		}
		else if (!amount.equals(other.amount))
			return false;
		if (categoryID != other.categoryID)
			return false;
		if (endDate == null)
		{
			if (other.endDate != null)
				return false;
		}
		else if (!endDate.equals(other.endDate))
			return false;
		if (previewFile == null)
		{
			if (other.previewFile != null)
				return false;
		}
		else if (!previewFile.equals(other.previewFile))
			return false;
		if (rbtFile == null)
		{
			if (other.rbtFile != null)
				return false;
		}
		else if (!rbtFile.equals(other.rbtFile))
			return false;
		if (shuffleID != other.shuffleID)
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (toneID != other.toneID)
			return false;
		if (toneName == null)
		{
			if (other.toneName != null)
				return false;
		}
		else if (!toneName.equals(other.toneName))
			return false;
		if (toneType == null)
		{
			if (other.toneType != null)
				return false;
		}
		else if (!toneType.equals(other.toneType))
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
		builder.append("Bookmark[amount = ");
		builder.append(amount);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", previewFile = ");
		builder.append(previewFile);
		builder.append(", rbtFile = ");
		builder.append(rbtFile);
		builder.append(", shuffleID = ");
		builder.append(shuffleID);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append("]");
		return builder.toString();
	}
}
