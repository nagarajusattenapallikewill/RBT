/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class CopyData
{
	private int categoryID;
	private int toneID;
	private String toneName;
	private String toneType = null;
	private int status;
	private String previewFile = null;
	private Date endDate = null;
	private String amount = null;
	private String period = null;
	private boolean isShuffleOrLoop = false;

	

	/**
	 * 
	 */
	public CopyData()
	{

	}

	/**
	 * @param categoryID
	 * @param toneID
	 * @param toneName
	 * @param toneType
	 * @param status
	 * @param previewFile
	 * @param endDate
	 * @param amount
	 * @param period
	 */
	public CopyData(int categoryID, int toneID, String toneName,
			String toneType, int status, String previewFile, Date endDate,
			String amount, String period)
	{
		super();
		this.categoryID = categoryID;
		this.toneID = toneID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.status = status;
		this.previewFile = previewFile;
		this.endDate = endDate;
		this.amount = amount;
		this.period = period;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	public boolean isShuffleOrLoop() {
		return isShuffleOrLoop;
	}

	
	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
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
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * @return the previewFile
	 */
	public String getPreviewFile()
	{
		return previewFile;
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
	 * @return the period
	 */
	public String getPeriod()
	{
		return period;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	public void setShuffleOrLoop(boolean isShuffleOrLoop) {
		this.isShuffleOrLoop = isShuffleOrLoop;
	}
	
	
	/**
	 * @param toneID the toneID to set
	 */
	public void setToneID(int toneID)
	{
		this.toneID = toneID;
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
	 * @param status the status to set
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * @param previewFile the previewFile to set
	 */
	public void setPreviewFile(String previewFile)
	{
		this.previewFile = previewFile;
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

	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period)
	{
		this.period = period;
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
		result = prime * result + ((period == null) ? 0 : period.hashCode());
		result = prime * result + ((previewFile == null) ? 0 : previewFile.hashCode());
		result = prime * result + status;
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
		if (!(obj instanceof CopyData))
			return false;
		CopyData other = (CopyData) obj;
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
		if (period == null)
		{
			if (other.period != null)
				return false;
		}
		else if (!period.equals(other.period))
			return false;
		if (previewFile == null)
		{
			if (other.previewFile != null)
				return false;
		}
		else if (!previewFile.equals(other.previewFile))
			return false;
		if (status != other.status)
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
		builder.append("CopyData[amount = ");
		builder.append(amount);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", period = ");
		builder.append(period);
		builder.append(", previewFile = ");
		builder.append(previewFile);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append(", isShuffleOrLoop = ");
		builder.append(isShuffleOrLoop);
		builder.append("]");
		return builder.toString();
	}
}
