/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: rajesh.karavadi $
 * $Id: DataRequest.java,v 1.15 2012/07/11 09:11:36 rajesh.karavadi Exp $
 * $Revision: 1.15 $
 * $Date: 2012/07/11 09:11:36 $
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author vinayasimha.patil
 *
 */
public class DataRequest extends Request
{
	private String info = null;
	private String callerID = null;
	private String type = null;
	private String clipID = null;
	private Date sentTime = null;
	private Integer count = null;
	private String newCallerID = null;
	private String newType = null;
	private Integer duration = null;
	private String transID = null;
	private Long smsID = null;
	private Boolean updateSmsID = false;
	private HashMap<String, String> infoMap = null;
	// Pending confirmations request parameters
	private String remindersLeft = null;
	private Date lastReminderSent = null;
	private Date smsReceivedTime = null;
	private String reminderText = null;
	private Integer delayInSentTime = null;
	private Integer recordFrom;
	private Integer numOfRecords;
	private Integer deleteLimit;
	private String sender;

	/**
	 * @param callerID
	 * @param type
	 */
	public DataRequest(String callerID, String type)
	{
		super(null);
		this.callerID = callerID;
		this.type = type;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 */
	public DataRequest(String subscriberID, String callerID, String type)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param clipID
	 * @param mode
	 */
	public DataRequest(String subscriberID, String callerID, String type,
			String clipID, String mode)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
		this.clipID = clipID;
		this.mode = mode;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param clipID
	 * @param mode
	 * @param count
	 */
	public DataRequest(String subscriberID, String callerID, String type,
			String clipID, String mode, Integer count)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
		this.clipID = clipID;
		this.mode = mode;
		this.count = count;
	}


	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param clipID
	 * @param sentTime
	 * @param mode
	 */
	public DataRequest(String subscriberID, String callerID, String type,
			String clipID, Date sentTime, String mode)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
		this.clipID = clipID;
		this.sentTime = sentTime;
		this.mode = mode;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param sentTime
	 * @param mode
	 * @param newType
	 */
	public DataRequest(String subscriberID, String callerID, String type,
			Date sentTime, String mode, String newType)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
		this.sentTime = sentTime;
		this.mode = mode;
		this.newType = newType;
	}


	/**
	 * @param subscriberID
	 * @param type
	 * @param count
	 */
	public DataRequest(String subscriberID, String type, Integer count)
	{
		super(subscriberID);
		this.type = type;
		this.count = count;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param sentTime
	 * @param count
	 */
	public DataRequest(String subscriberID, String callerID, String type,
			Date sentTime, Integer count)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.type = type;
		this.sentTime = sentTime;
		this.count = count;
	}

	/**
	 * @param transID
	 */
	public DataRequest(String transID)
	{
		super(null);
		this.transID = transID;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the clipID
	 */
	public String getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(String clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @return the sentTime
	 */
	public Date getSentTime()
	{
		return sentTime;
	}

	/**
	 * @param sentTime the sentTime to set
	 */
	public void setSentTime(Date sentTime)
	{
		this.sentTime = sentTime;
	}

	/**
	 * @return the count
	 */
	public Integer getCount()
	{
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(Integer count)
	{
		this.count = count;
	}

	/**
	 * @return the newCallerID
	 */
	public String getNewCallerID()
	{
		return newCallerID;
	}

	/**
	 * @param newCallerID the newCallerID to set
	 */
	public void setNewCallerID(String newCallerID)
	{
		this.newCallerID = newCallerID;
	}

	/**
	 * @return the newType
	 */
	public String getNewType()
	{
		return newType;
	}

	/**
	 * @param newType the newType to set
	 */
	public void setNewType(String newType)
	{
		this.newType = newType;
	}

	/**
	 * @return the duration
	 */
	public Integer getDuration()
	{
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Integer duration)
	{
		this.duration = duration;
	}

	/**
	 * @return the transID
	 */
	public String getTransID()
	{
		return transID;
	}

	/**
	 * @param transID the transID to set
	 */
	public void setTransID(String transID)
	{
		this.transID = transID;
	}

	/**
	 * @return the smsID
	 */
	public Long getSmsID()
	{
		return smsID;
	}

	/**
	 * @param smsID the smsID to set
	 */
	public void setSmsID(Long smsID)
	{
		this.smsID = smsID;
	}

	/**
	 * @return the updateSmsID
	 */
	public Boolean getUpdateSmsID()
	{
		return updateSmsID;
	}

	/**
	 * @param updateSmsID the updateSmsID to set
	 */
	public void setUpdateSmsID(Boolean updateSmsID)
	{
		this.updateSmsID = updateSmsID;
	}

	/**
	 * @return the infoMap
	 */
	public HashMap<String, String> getInfoMap()
	{
		return infoMap;
	}

	/**
	 * @param infoMap the infoMap to set
	 */
	public void setInfoMap(HashMap<String, String> infoMap)
	{
		this.infoMap = infoMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (info != null) requestParams.put(param_info, info);
		if (callerID != null) requestParams.put(param_callerID, callerID);
		if (type != null) requestParams.put(param_type, type);
		if (clipID != null) requestParams.put(param_clipID, clipID);
		if (sentTime != null) requestParams.put(param_sentTime, dateFormat.format(sentTime));
		if (count != null) requestParams.put(param_count, String.valueOf(count));
		if (newCallerID != null) requestParams.put(param_newCallerID, newCallerID);
		if (newType != null) requestParams.put(param_newType, newType);
		if (duration != null) requestParams.put(param_duration, String.valueOf(duration));
		if (transID != null) requestParams.put(param_transID, transID);
		if (smsID != null) requestParams.put(param_smsID, String.valueOf(smsID));
		if (updateSmsID != null) requestParams.put(param_updateSmsID, (updateSmsID ? YES : NO));
		if (remindersLeft != null) requestParams.put(param_remindersLeft, remindersLeft);
		if (lastReminderSent != null) requestParams.put(param_lastReminderSent, dateFormat.format(lastReminderSent));
		if (smsReceivedTime != null) requestParams.put(param_smsReceivedTime, dateFormat.format(smsReceivedTime));
		if (reminderText != null) requestParams.put(param_reminderText, reminderText);
		if (delayInSentTime != null) requestParams.put(param_delayInSentTime, String.valueOf(delayInSentTime));
		if (recordFrom != null) requestParams.put(param_recordsFrom, String.valueOf(recordFrom));
		if (numOfRecords != null) requestParams.put(param_numOfRecords, String.valueOf(numOfRecords));
		if (deleteLimit != null) requestParams.put(param_deleteLimit, String.valueOf(deleteLimit));
		if (sender != null) requestParams.put(param_sender, sender);

		if (infoMap != null)
		{
			Set<Entry<String, String>> entryMap = infoMap.entrySet();
			for (Entry<String, String> entry : entryMap)
				requestParams.put(param_info + "_" + entry.getKey(), entry.getValue());
		}

		return requestParams;
	}

	public String getRemindersLeft() {
		return remindersLeft;
	}

	public void setRemindersLeft(String remindersLeft) {
		this.remindersLeft = remindersLeft;
	}

	public Date getLastReminderSent() {
		return lastReminderSent;
	}

	public void setLastReminderSent(Date lastReminderSent) {
		this.lastReminderSent = lastReminderSent;
	}

	public Date getSmsReceivedTime() {
		return smsReceivedTime;
	}

	public void setSmsReceivedTime(Date smsReceivedTime) {
		this.smsReceivedTime = smsReceivedTime;
	}

	public String getReminderText() {
		return reminderText;
	}

	public void setReminderText(String reminderText) {
		this.reminderText = reminderText;
	}

	public Integer getDelayInSentTime() {
		return delayInSentTime;
	}

	public void setDelayInSentTime(Integer delayInSentTime) {
		this.delayInSentTime = delayInSentTime;
	}


	public Integer getRecordFrom() {
		return recordFrom;
	}

	public void setRecordFrom(Integer recordFrom) {
		this.recordFrom = recordFrom;
	}

	public Integer getNumOfRecords() {
		return numOfRecords;
	}

	public void setNumOfRecords(Integer numOfRecords) {
		this.numOfRecords = numOfRecords;
	}

	public Integer getDeleteLimit() {
		return deleteLimit;
	}

	public void setDeleteLimit(Integer deleteLimit) {
		this.deleteLimit = deleteLimit;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DataRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", onlyResponse = ");
		builder.append(onlyResponse);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", callerID = ");
		builder.append(callerID);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", count = ");
		builder.append(count);
		builder.append(", duration = ");
		builder.append(duration);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", infoMap = ");
		builder.append(infoMap);
		builder.append(", newCallerID = ");
		builder.append(newCallerID);
		builder.append(", newType = ");
		builder.append(newType);
		builder.append(", sentTime = ");
		builder.append(sentTime);
		builder.append(", smsID = ");
		builder.append(smsID);
		builder.append(", transID = ");
		builder.append(transID);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", updateSmsID = ");
		builder.append(updateSmsID);
		builder.append(", remindersLeft = ");
		builder.append(remindersLeft);
		builder.append(", lastReminderSent = ");
		builder.append(lastReminderSent);
		builder.append(", smsReceivedTime = ");
		builder.append(smsReceivedTime);
		builder.append(", reminderText = ");
		builder.append(reminderText);
		builder.append(", delayInSentTime = ");
		builder.append(delayInSentTime);
		builder.append(", recordFrom = ");
		builder.append(recordFrom);
		builder.append(", numOfRecords = ");
		builder.append(numOfRecords);
		builder.append(", deleteLimit = ");
		builder.append(deleteLimit);
		builder.append(", sender = ");
		builder.append(sender);
		builder.append("]");
		return builder.toString();
	}
}
