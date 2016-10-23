package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 * 
 */
public class Setting
{
	private String subscriberID = null;
	private String callerID = null;
	private int toneID;
	private String shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String previewFile = null;
	private String rbtFile = null;
	private String ugcRbtFile = null;
	private int fromTime;
	private int fromTimeMinutes;
	private int toTime;
	private int toTimeMinutes;
	private int status;
	private String chargeClass = null;
	private String selInterval = null;
	private int categoryID;
	private String selectionStatus = null;
	private String selectionType = null;
	private String selectedBy = null;
	private String selectionInfo = null;
	private String deselectedBy = null;
	private Date setTime = null;
	private Date endTime = null;
	private Date nextChargingDate = null;
	private String chargingModel = null;
	private String optInOutModel = null;
	private String refID = null;
	private Date nextBillingDate;
	private Date startTime = null;
	private String deselectionInfo = null;
	private HashMap<String, String> selectionInfoMap = null;
	private String lastChargeAmount = null;
	private String loopStatus = null;
	private String selectionStatusID = null;
	private Boolean isCurrentSetting = null;
	
	private String transactionStatus = null;
	/**
	 *Added as a foreign key from rbt_udp table	 * 
	 */
	private int udpId = -1;
	
	// Added for cut rbt start time dtoc
	private String cutRBTStartTime = null;
	
	public String getCutRBTStartTime() {
		return cutRBTStartTime;
	}

	public void setCutRBTStartTime(String cutRBTStartTime) {
		this.cutRBTStartTime = cutRBTStartTime;
	}

	/**
	 * 
	 */
	public Setting()
	{

	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param toneID
	 * @param shuffleID
	 * @param toneName
	 * @param toneType
	 * @param previewFile
	 * @param rbtFile
	 * @param ugcRbtFile
	 * @param fromTime
	 * @param fromTimeMinutes
	 * @param toTime
	 * @param toTimeMinutes
	 * @param status
	 * @param chargeClass
	 * @param selInterval
	 * @param categoryID
	 * @param selectionStatus
	 * @param selectionType
	 * @param selectedBy
	 * @param selectionInfo
	 * @param deselectedBy
	 * @param setTime
	 * @param endTime
	 * @param nextChargingDate
	 * @param chargingModel
	 * @param optInOutModel
	 * @param refID
	 * @param nextBillingDate
	 * @param selectionInfoMap
	 */
	public Setting(String subscriberID, String callerID, int toneID,
			String shuffleID, String toneName, String toneType,
			String previewFile, String rbtFile, String ugcRbtFile,
			int fromTime, int fromTimeMinutes, int toTime, int toTimeMinutes,
			int status, String chargeClass, String selInterval, int categoryID,
			String selectionStatus, String selectionType, String selectedBy,
			String selectionInfo, String deselectedBy, Date setTime,
			Date endTime, Date nextChargingDate, String chargingModel,
			String optInOutModel, String refID, Date nextBillingDate,
			HashMap<String, String> selectionInfoMap)
	{
		this.subscriberID = subscriberID;
		this.callerID = callerID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFile = previewFile;
		this.rbtFile = rbtFile;
		this.ugcRbtFile = ugcRbtFile;
		this.fromTime = fromTime;
		this.fromTimeMinutes = fromTimeMinutes;
		this.toTime = toTime;
		this.toTimeMinutes = toTimeMinutes;
		this.status = status;
		this.chargeClass = chargeClass;
		this.selInterval = selInterval;
		this.categoryID = categoryID;
		this.selectionStatus = selectionStatus;
		this.selectionType = selectionType;
		this.selectedBy = selectedBy;
		this.selectionInfo = selectionInfo;
		this.deselectedBy = deselectedBy;
		this.setTime = setTime;
		this.endTime = endTime;
		this.nextChargingDate = nextChargingDate;
		this.chargingModel = chargingModel;
		this.optInOutModel = optInOutModel;
		this.refID = refID;
		this.nextBillingDate = nextBillingDate;
		this.selectionInfoMap = selectionInfoMap;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID
	 *            the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID
	 *            the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
	}

	/**
	 * @param toneID
	 *            the toneID to set
	 */
	public void setToneID(int toneID)
	{
		this.toneID = toneID;
	}

	/**
	 * @return the shuffleID
	 */
	public String getShuffleID()
	{
		return shuffleID;
	}

	/**
	 * @param shuffleID
	 *            the shuffleID to set
	 */
	public void setShuffleID(String shuffleID)
	{
		this.shuffleID = shuffleID;
	}

	/**
	 * @return the toneName
	 */
	public String getToneName()
	{
		return toneName;
	}

	/**
	 * @param toneName
	 *            the toneName to set
	 */
	public void setToneName(String toneName)
	{
		this.toneName = toneName;
	}

	/**
	 * @return the toneType
	 */
	public String getToneType()
	{
		return toneType;
	}

	/**
	 * @param toneType
	 *            the toneType to set
	 */
	public void setToneType(String toneType)
	{
		this.toneType = toneType;
	}

	/**
	 * @return the previewFile
	 */
	public String getPreviewFile()
	{
		return previewFile;
	}

	/**
	 * @param previewFile
	 *            the previewFile to set
	 */
	public void setPreviewFile(String previewFile)
	{
		this.previewFile = previewFile;
	}

	/**
	 * @return the rbtFile
	 */
	public String getRbtFile()
	{
		return rbtFile;
	}

	/**
	 * @param rbtFile
	 *            the rbtFile to set
	 */
	public void setRbtFile(String rbtFile)
	{
		this.rbtFile = rbtFile;
	}

	/**
	 * @return the ugcRbtFile
	 */
	public String getUgcRbtFile()
	{
		return ugcRbtFile;
	}

	/**
	 * @param ugcRbtFile
	 *            the ugcRbtFile to set
	 */
	public void setUgcRbtFile(String ugcRbtFile)
	{
		this.ugcRbtFile = ugcRbtFile;
	}

	/**
	 * @return the fromTime
	 */
	public int getFromTime()
	{
		return fromTime;
	}

	/**
	 * @param fromTime
	 *            the fromTime to set
	 */
	public void setFromTime(int fromTime)
	{
		this.fromTime = fromTime;
	}

	/**
	 * @return the fromTimeMinutes
	 */
	public int getFromTimeMinutes()
	{
		return fromTimeMinutes;
	}

	/**
	 * @param fromTimeMinutes
	 *            the fromTimeMinutes to set
	 */
	public void setFromTimeMinutes(int fromTimeMinutes)
	{
		this.fromTimeMinutes = fromTimeMinutes;
	}

	/**
	 * @return the toTime
	 */
	public int getToTime()
	{
		return toTime;
	}

	/**
	 * @param toTime
	 *            the toTime to set
	 */
	public void setToTime(int toTime)
	{
		this.toTime = toTime;
	}

	/**
	 * @return the toTimeMinutes
	 */
	public int getToTimeMinutes()
	{
		return toTimeMinutes;
	}

	/**
	 * @param toTimeMinutes
	 *            the toTimeMinutes to set
	 */
	public void setToTimeMinutes(int toTimeMinutes)
	{
		this.toTimeMinutes = toTimeMinutes;
	}

	/**
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param chargeClass
	 *            the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the selInterval
	 */
	public String getSelInterval()
	{
		return selInterval;
	}

	/**
	 * @param selInterval
	 *            the selInterval to set
	 */
	public void setSelInterval(String selInterval)
	{
		this.selInterval = selInterval;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID
	 *            the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @return the selectionStatus
	 */
	public String getSelectionStatus()
	{
		return selectionStatus;
	}

	/**
	 * @param selectionStatus
	 *            the selectionStatus to set
	 */
	public void setSelectionStatus(String selectionStatus)
	{
		this.selectionStatus = selectionStatus;
	}

	/**
	 * @return the selectionType
	 */
	public String getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType
	 *            the selectionType to set
	 */
	public void setSelectionType(String selectionType)
	{
		this.selectionType = selectionType;
	}

	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy()
	{
		return selectedBy;
	}

	/**
	 * @param selectedBy
	 *            the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy)
	{
		this.selectedBy = selectedBy;
	}

	/**
	 * @return the selectionInfo
	 */
	public String getSelectionInfo()
	{
		return selectionInfo;
	}

	/**
	 * @param selectionInfo
	 *            the selectionInfo to set
	 */
	public void setSelectionInfo(String selectionInfo)
	{
		this.selectionInfo = selectionInfo;
	}

	/**
	 * @return the deselectedBy
	 */
	public String getDeselectedBy()
	{
		return deselectedBy;
	}

	/**
	 * @param deselectedBy
	 *            the deselectedBy to set
	 */
	public void setDeselectedBy(String deselectedBy)
	{
		this.deselectedBy = deselectedBy;
	}

	/**
	 * @return the setTime
	 */
	public Date getSetTime()
	{
		return setTime;
	}

	/**
	 * @param setTime
	 *            the setTime to set
	 */
	public void setSetTime(Date setTime)
	{
		this.setTime = setTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime()
	{
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * @return the nextChargingDate
	 */
	public Date getNextChargingDate()
	{
		return nextChargingDate;
	}

	/**
	 * @param nextChargingDate
	 *            the nextChargingDate to set
	 */
	public void setNextChargingDate(Date nextChargingDate)
	{
		this.nextChargingDate = nextChargingDate;
	}

	/**
	 * @return the chargingModel
	 */
	public String getChargingModel()
	{
		return chargingModel;
	}

	/**
	 * @param chargingModel
	 *            the chargingModel to set
	 */
	public void setChargingModel(String chargingModel)
	{
		this.chargingModel = chargingModel;
	}

	/**
	 * @return the optInOutModel
	 */
	public String getOptInOutModel()
	{
		return optInOutModel;
	}

	/**
	 * @param optInOutModel
	 *            the optInOutModel to set
	 */
	public void setOptInOutModel(String optInOutModel)
	{
		this.optInOutModel = optInOutModel;
	}

	/**
	 * @return the refID
	 */
	public String getRefID()
	{
		return refID;
	}

	/**
	 * @param refID
	 *            the refID to set
	 */
	public void setRefID(String refID)
	{
		this.refID = refID;
	}

	/**
	 * @return the nextBillingDate
	 */
	public Date getNextBillingDate()
	{
		return nextBillingDate;
	}

	/**
	 * @param nextBillingDate
	 *            the nextBillingDate to set
	 */
	public void setNextBillingDate(Date nextBillingDate)
	{
		this.nextBillingDate = nextBillingDate;
	}

	/**
	 * @return the selectionInfoMap
	 */
	public HashMap<String, String> getSelectionInfoMap()
	{
		return selectionInfoMap;
	}

	/**
	 * @param selectionInfoMap
	 *            the selectionInfoMap to set
	 */
	public void setSelectionInfoMap(HashMap<String, String> selectionInfoMap)
	{
		this.selectionInfoMap = selectionInfoMap;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the deselectionInfo
	 */
	public String getDeselectionInfo()
	{
		return deselectionInfo;
	}
	
	/**
	 * @param deselectionInfo the deselectionInfo to set
	 */
	public void setDeselectionInfo(String deselectionInfo)
	{
		this.deselectionInfo = deselectionInfo;
	}

	/**
	 * @return
	 */
	public String getLastChargeAmount() {
		return lastChargeAmount;
	}

	
	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	/**
	 * @param lastChargeAmount
	 */
	public void setLastChargeAmount(String lastChargeAmount) {
		this.lastChargeAmount = lastChargeAmount;
	}
	public String getLoopStatus() {
		return loopStatus;
	}

	public void setLoopStatus(String loopStatus) {
		this.loopStatus = loopStatus;
	}

	public String getSelectionStatusID() {
		return selectionStatusID;
	}

	public void setSelectionStatusID(String selectionStatusID) {
		this.selectionStatusID = selectionStatusID;
	}

	public Boolean getIsCurrentSetting() {
		return isCurrentSetting;
	}

	public void setIsCurrentSetting(Boolean isCurrentSetting) {
		this.isCurrentSetting = isCurrentSetting;
	}
	
	public int getUdpId() {
		return udpId;
	}

	public void setUdpId(int udpId) {
		this.udpId = udpId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callerID == null) ? 0 : callerID.hashCode());
		result = prime * result + categoryID;
		result = prime * result
				+ ((chargeClass == null) ? 0 : chargeClass.hashCode());
		result = prime * result
				+ ((chargingModel == null) ? 0 : chargingModel.hashCode());
		result = prime * result
				+ ((deselectedBy == null) ? 0 : deselectedBy.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + fromTime;
		result = prime * result + fromTimeMinutes;
		result = prime * result
				+ ((nextBillingDate == null) ? 0 : nextBillingDate.hashCode());
		result = prime
				* result
				+ ((nextChargingDate == null) ? 0 : nextChargingDate.hashCode());
		result = prime * result
				+ ((optInOutModel == null) ? 0 : optInOutModel.hashCode());
		result = prime * result
				+ ((previewFile == null) ? 0 : previewFile.hashCode());
		result = prime * result + ((rbtFile == null) ? 0 : rbtFile.hashCode());
		result = prime * result + ((refID == null) ? 0 : refID.hashCode());
		result = prime * result
				+ ((selInterval == null) ? 0 : selInterval.hashCode());
		result = prime * result
				+ ((selectedBy == null) ? 0 : selectedBy.hashCode());
		result = prime * result
				+ ((selectionInfo == null) ? 0 : selectionInfo.hashCode());
		result = prime
				* result
				+ ((selectionInfoMap == null) ? 0 : selectionInfoMap.hashCode());
		result = prime * result
				+ ((selectionStatus == null) ? 0 : selectionStatus.hashCode());
		result = prime * result
				+ ((selectionStatusID == null) ? 0 : selectionStatusID.hashCode());
		result = prime * result
				+ ((selectionType == null) ? 0 : selectionType.hashCode());
		result = prime * result + ((setTime == null) ? 0 : setTime.hashCode());
		result = prime * result
				+ ((shuffleID == null) ? 0 : shuffleID.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + status;
		result = prime * result
				+ ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + toTime;
		result = prime * result + toTimeMinutes;
		result = prime * result + toneID;
		result = prime * result
				+ ((toneName == null) ? 0 : toneName.hashCode());
		result = prime * result
				+ ((toneType == null) ? 0 : toneType.hashCode());
		result = prime * result
				+ ((ugcRbtFile == null) ? 0 : ugcRbtFile.hashCode());
		result = prime * result
				+ ((deselectionInfo == null) ? 0 : deselectionInfo.hashCode());
		result = prime * result
				+ ((lastChargeAmount == null) ? 0 : lastChargeAmount.hashCode());
		result = prime * result + udpId;
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
		if (getClass() != obj.getClass())
			return false;
		Setting other = (Setting) obj;
		if (callerID == null)
		{
			if (other.callerID != null)
				return false;
		}
		else if (!callerID.equals(other.callerID))
			return false;
		if (categoryID != other.categoryID)
			return false;
		if (chargeClass == null)
		{
			if (other.chargeClass != null)
				return false;
		}
		else if (!chargeClass.equals(other.chargeClass))
			return false;
		if (chargingModel == null)
		{
			if (other.chargingModel != null)
				return false;
		}
		else if (!chargingModel.equals(other.chargingModel))
			return false;
		if (deselectedBy == null)
		{
			if (other.deselectedBy != null)
				return false;
		}
		else if (!deselectedBy.equals(other.deselectedBy))
			return false;
		if (endTime == null)
		{
			if (other.endTime != null)
				return false;
		}
		else if (!endTime.equals(other.endTime))
			return false;
		if (fromTime != other.fromTime)
			return false;
		if (fromTimeMinutes != other.fromTimeMinutes)
			return false;
		if (nextBillingDate == null)
		{
			if (other.nextBillingDate != null)
				return false;
		}
		else if (!nextBillingDate.equals(other.nextBillingDate))
			return false;
		if (nextChargingDate == null)
		{
			if (other.nextChargingDate != null)
				return false;
		}
		else if (!nextChargingDate.equals(other.nextChargingDate))
			return false;
		if (optInOutModel == null)
		{
			if (other.optInOutModel != null)
				return false;
		}
		else if (!optInOutModel.equals(other.optInOutModel))
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
		if (refID == null)
		{
			if (other.refID != null)
				return false;
		}
		else if (!refID.equals(other.refID))
			return false;
		if (selInterval == null)
		{
			if (other.selInterval != null)
				return false;
		}
		else if (!selInterval.equals(other.selInterval))
			return false;
		if (selectedBy == null)
		{
			if (other.selectedBy != null)
				return false;
		}
		else if (!selectedBy.equals(other.selectedBy))
			return false;
		if (selectionInfo == null)
		{
			if (other.selectionInfo != null)
				return false;
		}
		else if (!selectionInfo.equals(other.selectionInfo))
			return false;
		if (selectionInfoMap == null)
		{
			if (other.selectionInfoMap != null)
				return false;
		}
		else if (!selectionInfoMap.equals(other.selectionInfoMap))
			return false;
		if (selectionStatus == null)
		{
			if (other.selectionStatus != null)
				return false;
		}
		else if (!selectionStatus.equals(other.selectionStatus))
			return false;
		if (selectionStatusID == null)
		{
			if (other.selectionStatusID != null)
				return false;
		}
		else if (!selectionStatusID.equals(other.selectionStatusID))
			return false;
		if (selectionType == null)
		{
			if (other.selectionType != null)
				return false;
		}
		else if (!selectionType.equals(other.selectionType))
			return false;
		if (setTime == null)
		{
			if (other.setTime != null)
				return false;
		}
		else if (!setTime.equals(other.setTime))
			return false;
		if (shuffleID == null)
		{
			if (other.shuffleID != null)
				return false;
		}
		else if (!shuffleID.equals(other.shuffleID))
			return false;
		if (startTime == null)
		{
			if (other.startTime != null)
				return false;
		}
		else if (!startTime.equals(other.startTime))
			return false;
		if (status != other.status)
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (toTime != other.toTime)
			return false;
		if (toTimeMinutes != other.toTimeMinutes)
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
		if (ugcRbtFile == null)
		{
			if (other.ugcRbtFile != null)
				return false;
		}
		else if (!ugcRbtFile.equals(other.ugcRbtFile))
			return false;
		if (deselectionInfo == null)
		{
			if (other.deselectionInfo != null)
				return false;
		}
		else if (!deselectionInfo.equals(other.deselectionInfo))
			return false;
		if (lastChargeAmount == null)
		{
			if (other.lastChargeAmount != null)
				return false;
		}
		else if (!lastChargeAmount.equals(other.lastChargeAmount))
			return false;
		
		if (udpId != other.udpId)
			return false;
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Setting[callerID = ");
		builder.append(callerID);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", chargingModel = ");
		builder.append(chargingModel);
		builder.append(", deselectedBy = ");
		builder.append(deselectedBy);
		builder.append(", endTime = ");
		builder.append(endTime);
		builder.append(", fromTime = ");
		builder.append(fromTime);
		builder.append(", fromTimeMinutes = ");
		builder.append(fromTimeMinutes);
		builder.append(", loopStatus = ");
		builder.append(loopStatus);
		builder.append(", nextBillingDate = ");
		builder.append(nextBillingDate);
		builder.append(", nextChargingDate = ");
		builder.append(nextChargingDate);
		builder.append(", optInOutModel = ");
		builder.append(optInOutModel);
		builder.append(", previewFile = ");
		builder.append(previewFile);
		builder.append(", rbtFile = ");
		builder.append(rbtFile);
		builder.append(", refID = ");
		builder.append(refID);
		builder.append(", selInterval = ");
		builder.append(selInterval);
		builder.append(", selectedBy = ");
		builder.append(selectedBy);
		builder.append(", selectionInfo = ");
		builder.append(selectionInfo);
		builder.append(", selectionInfoMap = ");
		builder.append(selectionInfoMap);
		builder.append(", selectionStatus = ");
		builder.append(selectionStatus);
		builder.append(", selectionType = ");
		builder.append(selectionType);
		builder.append(", setTime = ");
		builder.append(setTime);
		builder.append(", shuffleID = ");
		builder.append(shuffleID);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toTime = ");
		builder.append(toTime);
		builder.append(", toTimeMinutes = ");
		builder.append(toTimeMinutes);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append(", ugcRbtFile = ");
		builder.append(ugcRbtFile);
		builder.append(", startTime = ");
		builder.append(startTime);
		builder.append(", deselectionInfo=");
		builder.append(deselectionInfo);
		builder.append(", lastChargeAmount=");
		builder.append(lastChargeAmount);
		builder.append(", selectionStatusID=");
		builder.append(selectionStatusID);
		builder.append(", isCurrentSetting=");
		builder.append(isCurrentSetting);
		builder.append(", udpId=");
		builder.append(udpId);
		builder.append("]");
		return builder.toString();
	}

}
