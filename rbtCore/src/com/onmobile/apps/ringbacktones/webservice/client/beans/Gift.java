package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Gift 
{
	private String sender = null;
	private String receiver = null;
	private int categoryID;
	private int toneID;
	private String toneName = null;
	private String toneType = null;
	private String previewFile = null;
	private String rbtFile = null;
	private Date sentTime = null;
	private String status = null;
	private String selectedBy = null;
	private Date validity = null;
	private HashMap<String, String> giftExtraInfoMap = null;

	/**
	 * 
	 */
	public Gift()
	{

	}

	/**
	 * @param sender
	 * @param receiver
	 * @param categoryID
	 * @param toneID
	 * @param toneName
	 * @param toneType
	 * @param previewFile
	 * @param rbtFile
	 * @param sentTime
	 * @param status
	 */
	public Gift(String sender, String receiver, int categoryID, int toneID,
			String toneName, String toneType, String previewFile,
			String rbtFile, Date sentTime, String status)
	{
		this(sender, receiver, categoryID, toneID, toneName, toneType,
				previewFile, rbtFile, sentTime, status, null);
	}

	/**
	 * @param sender
	 * @param receiver
	 * @param categoryID
	 * @param toneID
	 * @param toneName
	 * @param toneType
	 * @param previewFile
	 * @param rbtFile
	 * @param sentTime
	 * @param status
	 * @param validity
	 */
	public Gift(String sender, String receiver, int categoryID, int toneID,
			String toneName, String toneType, String previewFile,
			String rbtFile, Date sentTime, String status, Date validity)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.categoryID = categoryID;
		this.toneID = toneID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFile = previewFile;
		this.rbtFile = rbtFile;
		this.sentTime = sentTime;
		this.status = status;
		this.validity = validity;
	}
	
	/**
	 * @return the sender
	 */
	public String getSender()
	{
		return sender;
	}

	/**
	 * @return the receiver
	 */
	public String getReceiver()
	{
		return receiver;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
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
	 * @return the sentTime
	 */
	public Date getSentTime()
	{
		return sentTime;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender)
	{
		this.sender = sender;
	}

	/**
	 * @param receiver the receiver to set
	 */
	public void setReceiver(String receiver)
	{
		this.receiver = receiver;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
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
	 * @param sentTime the sentTime to set
	 */
	public void setSentTime(Date sentTime)
	{
		this.sentTime = sentTime;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy() {
		return selectedBy;
	}

	/**
	 * @param selectedBy the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy) {
		this.selectedBy = selectedBy;
	}

	/**
	 * @return the validity
	 */
	public Date getValidity() {
		return validity;
	}
	
	/**
	 * @param validity the validity to set
	 */
	public void setValidity(Date validity) {
		this.validity = validity;
	}
	
	/**
	 * @return the giftExtraInfoMap
	 */
	public HashMap<String, String> getGiftExtraInfoMap() 
	{
		return giftExtraInfoMap;
	}

	/**
	 * @param giftExtraInfoMap the giftExtraInfoMap to set
	 */
	public void setGiftExtraInfoMap(HashMap<String, String> giftExtraInfoMap) 
	{
		this.giftExtraInfoMap = giftExtraInfoMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryID;
		result = prime
				* result
				+ ((giftExtraInfoMap == null) ? 0 : giftExtraInfoMap.hashCode());
		result = prime * result
				+ ((previewFile == null) ? 0 : previewFile.hashCode());
		result = prime * result + ((rbtFile == null) ? 0 : rbtFile.hashCode());
		result = prime * result
				+ ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result
				+ ((sentTime == null) ? 0 : sentTime.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + toneID;
		result = prime * result
				+ ((toneName == null) ? 0 : toneName.hashCode());
		result = prime * result
				+ ((toneType == null) ? 0 : toneType.hashCode());
		result = prime * result
				+ ((selectedBy == null) ? 0 : selectedBy.hashCode());
		result = prime * result
				+ ((validity == null) ? 0 : validity.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gift other = (Gift) obj;
		if (categoryID != other.categoryID)
			return false;
		if (giftExtraInfoMap == null) {
			if (other.giftExtraInfoMap != null)
				return false;
		} else if (!giftExtraInfoMap.equals(other.giftExtraInfoMap))
			return false;
		if (previewFile == null) {
			if (other.previewFile != null)
				return false;
		} else if (!previewFile.equals(other.previewFile))
			return false;
		if (rbtFile == null) {
			if (other.rbtFile != null)
				return false;
		} else if (!rbtFile.equals(other.rbtFile))
			return false;
		if (receiver == null) {
			if (other.receiver != null)
				return false;
		} else if (!receiver.equals(other.receiver))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (sentTime == null) {
			if (other.sentTime != null)
				return false;
		} else if (!sentTime.equals(other.sentTime))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (toneID != other.toneID)
			return false;
		if (toneName == null) {
			if (other.toneName != null)
				return false;
		} else if (!toneName.equals(other.toneName))
			return false;
		if (toneType == null) {
			if (other.toneType != null)
				return false;
		} else if (!toneType.equals(other.toneType))
			return false;
		if (selectedBy == null) {
			if (other.selectedBy != null)
				return false;
		} else if (!selectedBy.equals(other.selectedBy))
			return false;
		if (validity == null) {
			if (other.validity != null)
				return false;
		} else if (!validity.equals(other.validity))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Gift[categoryID=");
		builder.append(categoryID);
		builder.append(", giftExtraInfoMap=");
		builder.append(giftExtraInfoMap);
		builder.append(", previewFile=");
		builder.append(previewFile);
		builder.append(", rbtFile=");
		builder.append(rbtFile);
		builder.append(", receiver=");
		builder.append(receiver);
		builder.append(", sender=");
		builder.append(sender);
		builder.append(", sentTime=");
		builder.append(sentTime);
		builder.append(", status=");
		builder.append(status);
		builder.append(", toneID=");
		builder.append(toneID);
		builder.append(", toneName=");
		builder.append(toneName);
		builder.append(", toneType=");
		builder.append(toneType);
		builder.append(", selectedBy=");
		builder.append(selectedBy);
		builder.append(", validity=");
		builder.append(validity);
		builder.append("]");
		return builder.toString();
	}
	
}
