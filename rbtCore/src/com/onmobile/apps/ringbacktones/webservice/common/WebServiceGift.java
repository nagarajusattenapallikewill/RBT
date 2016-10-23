/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Date;


/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceGift
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
	private String giftExtraInfo = null;
	private String selectedBy = null;
	private Date validity = null;
    private String clipVcode = null;
	/**
	 * 
	 */
	public WebServiceGift()
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
	public WebServiceGift(String sender, String receiver, int categoryID,
			int toneID, String toneName, String toneType, String previewFile,
			String rbtFile, Date sentTime, String status)
	{
		this(sender, receiver, categoryID, toneID, toneName,
				toneType, previewFile, rbtFile, sentTime, status, null);
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
	public WebServiceGift(String sender, String receiver, int categoryID,
			int toneID, String toneName, String toneType, String previewFile,
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
	 * @return the giftExtraInfo
	 */
	public String getGiftExtraInfo() {
		return giftExtraInfo;
	}

	/**
	 * @param giftExtraInfo the giftExtraInfo to set
	 */
	public void setGiftExtraInfo(String giftExtraInfo) {
		this.giftExtraInfo = giftExtraInfo;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceGift[categoryID=");
		builder.append(categoryID);
		builder.append(", giftExtraInfo=");
		builder.append(giftExtraInfo);
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
		builder.append(", clipVcode=");
		builder.append(clipVcode);
		builder.append("]");
		return builder.toString();
	}

	public String getClipVcode() {
		return clipVcode;
	}

	public void setClipVcode(String clipVcode) {
		this.clipVcode = clipVcode;
	}
	
}
