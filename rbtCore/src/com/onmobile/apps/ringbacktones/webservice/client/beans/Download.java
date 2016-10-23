package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Download
{
	private String subscriberID = null;
	private int toneID;
	private String shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String previewFile = null;
	private String rbtFile = null;
	private String ugcRbtFile = null;
	private String downloadStatus = null;
	private String downloadType = null;
	private int categoryID;
	private String chargeClass = null;
	private String selectedBy = null;
	private String deselectedBy = null;
	private Date setTime = null;
	private Date endTime = null;
	private HashMap<String, String> downloadInfoMap = null;
	private boolean isSetForAll;
	private Date nextBillingDate;
	private String refID = null;
	private String selectionInfo = null;
	private String deselectionInfo = null;
	private Date nextChargingDate;
	private String lastChargeAmount = null;
	private Date lastChargedDate = null;
	private String transactionStatus = null;

	/**
	 * 
	 */
	public Download()
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
	 * @param ugcRbtFile
	 * @param downloadStatus
	 * @param downloadType
	 * @param categoryID
	 * @param chargeClass
	 * @param selectedBy
	 * @param deselectedBy
	 * @param setTime
	 * @param endTime
	 * @param downloadInfoMap
	 * @param isSetForAll
	 * @param nextBillingDate
	 * @param refID
	 */
	public Download(String subscriberID, int toneID, String shuffleID,
			String toneName, String toneType, String previewFile,
			String rbtFile, String ugcRbtFile, String downloadStatus,
			String downloadType, int categoryID, String chargeClass,
			String selectedBy, String deselectedBy, Date setTime, Date endTime,
			HashMap<String, String> downloadInfoMap, boolean isSetForAll,
			Date nextBillingDate, String refID)
	{
		super();
		this.subscriberID = subscriberID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFile = previewFile;
		this.rbtFile = rbtFile;
		this.ugcRbtFile = ugcRbtFile;
		this.downloadStatus = downloadStatus;
		this.downloadType = downloadType;
		this.categoryID = categoryID;
		this.chargeClass = chargeClass;
		this.selectedBy = selectedBy;
		this.deselectedBy = deselectedBy;
		this.setTime = setTime;
		this.endTime = endTime;
		this.downloadInfoMap = downloadInfoMap;
		this.isSetForAll = isSetForAll;
		this.nextBillingDate = nextBillingDate;
		this.refID = refID;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the toneID
	 */
	public int getToneID()
	{
		return toneID;
	}

	/**
	 * @param toneID the toneID to set
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
	 * @param shuffleID the shuffleID to set
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
	 * @param toneName the toneName to set
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
	 * @param toneType the toneType to set
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
	 * @param previewFile the previewFile to set
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
	 * @param rbtFile the rbtFile to set
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
	 * @param ugcRbtFile the ugcRbtFile to set
	 */
	public void setUgcRbtFile(String ugcRbtFile)
	{
		this.ugcRbtFile = ugcRbtFile;
	}

	/**
	 * @return the downloadStatus
	 */
	public String getDownloadStatus()
	{
		return downloadStatus;
	}

	/**
	 * @param downloadStatus the downloadStatus to set
	 */
	public void setDownloadStatus(String downloadStatus)
	{
		this.downloadStatus = downloadStatus;
	}

	/**
	 * @return the downloadType
	 */
	public String getDownloadType()
	{
		return downloadType;
	}

	/**
	 * @param downloadType the downloadType to set
	 */
	public void setDownloadType(String downloadType)
	{
		this.downloadType = downloadType;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy()
	{
		return selectedBy;
	}

	/**
	 * @param selectedBy the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy)
	{
		this.selectedBy = selectedBy;
	}

	/**
	 * @return the deselectedBy
	 */
	public String getDeselectedBy()
	{
		return deselectedBy;
	}

	/**
	 * @param deselectedBy the deselectedBy to set
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
	 * @param setTime the setTime to set
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
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * @return the downloadInfoMap
	 */
	public HashMap<String, String> getDownloadInfoMap()
	{
		return downloadInfoMap;
	}

	/**
	 * @param downloadInfoMap the downloadInfoMap to set
	 */
	public void setDownloadInfoMap(HashMap<String, String> downloadInfoMap)
	{
		this.downloadInfoMap = downloadInfoMap;
	}

	/**
	 * @return the isSetForAll
	 */
	public boolean isSetForAll()
	{
		return isSetForAll;
	}

	/**
	 * @param isSetForAll the isSetForAll to set
	 */
	public void setSetForAll(boolean isSetForAll)
	{
		this.isSetForAll = isSetForAll;
	}

	/**
	 * @return the nextBillingDate
	 */
	public Date getNextBillingDate()
	{
		return nextBillingDate;
	}

	/**
	 * @param nextBillingDate the nextBillingDate to set
	 */
	public void setNextBillingDate(Date nextBillingDate)
	{
		this.nextBillingDate = nextBillingDate;
	}

	/**
	 * @return the refID
	 */
	public String getRefID()
	{
		return refID;
	}

	/**
	 * @param refID the refID to set
	 */
	public void setRefID(String refID)
	{
		this.refID = refID;
	}

	/**
	 * @return the selectionInfo
	 */
	public String getSelectionInfo()
	{
		return selectionInfo;
	}

	/**
	 * @param selectionInfo the selectionInfo to set
	 */
	public void setSelectionInfo(String selectionInfo)
	{
		this.selectionInfo = selectionInfo;
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
	 * @return the nextChargingDate
	 */
	public Date getNextChargingDate()
	{
		return nextChargingDate;
	}

	/**
	 * @param nextChargingDate the nextChargingDate to set
	 */
	public void setNextChargingDate(Date nextChargingDate)
	{
		this.nextChargingDate = nextChargingDate;
	}
	
	/**
	 * @return the lastChargeAmount
	 */
	public String getLastChargeAmount() {
		return lastChargeAmount;
	}

	/**
	 * @param lastChargeAmount the lastChargeAmount to set
	 */
	public void setLastChargeAmount(String lastChargeAmount) {
		this.lastChargeAmount = lastChargeAmount;
	}
	
	public Date getLastChargedDate() {
		return lastChargedDate;
	}

	public void setLastChargedDate(Date lastChargedDate) {
		this.lastChargedDate = lastChargedDate;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryID;
		result = prime * result
				+ ((chargeClass == null) ? 0 : chargeClass.hashCode());
		result = prime * result
				+ ((deselectedBy == null) ? 0 : deselectedBy.hashCode());
		result = prime * result
				+ ((downloadInfoMap == null) ? 0 : downloadInfoMap.hashCode());
		result = prime * result
				+ ((downloadStatus == null) ? 0 : downloadStatus.hashCode());
		result = prime * result
				+ ((downloadType == null) ? 0 : downloadType.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + (isSetForAll ? 1231 : 1237);
		result = prime * result
				+ ((nextBillingDate == null) ? 0 : nextBillingDate.hashCode());
		result = prime * result
				+ ((previewFile == null) ? 0 : previewFile.hashCode());
		result = prime * result + ((rbtFile == null) ? 0 : rbtFile.hashCode());
		result = prime * result + ((refID == null) ? 0 : refID.hashCode());
		result = prime * result
				+ ((selectedBy == null) ? 0 : selectedBy.hashCode());
		result = prime * result + ((setTime == null) ? 0 : setTime.hashCode());
		result = prime * result
				+ ((shuffleID == null) ? 0 : shuffleID.hashCode());
		result = prime * result
				+ ((subscriberID == null) ? 0 : subscriberID.hashCode());
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
				+ ((selectionInfo == null) ? 0 : selectionInfo.hashCode());
		result = prime * result
				+ ((nextChargingDate == null) ? 0 : nextChargingDate.hashCode());
		result = prime * result
				+ ((lastChargeAmount == null) ? 0 : lastChargeAmount.hashCode());
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
		Download other = (Download) obj;
		if (categoryID != other.categoryID)
			return false;
		if (chargeClass == null)
		{
			if (other.chargeClass != null)
				return false;
		}
		else if (!chargeClass.equals(other.chargeClass))
			return false;
		if (deselectedBy == null)
		{
			if (other.deselectedBy != null)
				return false;
		}
		else if (!deselectedBy.equals(other.deselectedBy))
			return false;
		if (downloadInfoMap == null)
		{
			if (other.downloadInfoMap != null)
				return false;
		}
		else if (!downloadInfoMap.equals(other.downloadInfoMap))
			return false;
		if (downloadStatus == null)
		{
			if (other.downloadStatus != null)
				return false;
		}
		else if (!downloadStatus.equals(other.downloadStatus))
			return false;
		if (downloadType == null)
		{
			if (other.downloadType != null)
				return false;
		}
		else if (!downloadType.equals(other.downloadType))
			return false;
		if (endTime == null)
		{
			if (other.endTime != null)
				return false;
		}
		else if (!endTime.equals(other.endTime))
			return false;
		if (isSetForAll != other.isSetForAll)
			return false;
		if (nextBillingDate == null)
		{
			if (other.nextBillingDate != null)
				return false;
		}
		else if (!nextBillingDate.equals(other.nextBillingDate))
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
		if (selectedBy == null)
		{
			if (other.selectedBy != null)
				return false;
		}
		else if (!selectedBy.equals(other.selectedBy))
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
		if (selectionInfo == null)
		{
			if (other.selectionInfo != null)
				return false;
		}
		else if (!selectionInfo.equals(other.selectionInfo))
			return false;
		if (nextChargingDate == null)
		{
			if (other.nextChargingDate != null)
				return false;
		}
		else if (!nextChargingDate.equals(other.nextChargingDate))
			return false;
		if (lastChargeAmount == null)
		{
			if (other.lastChargeAmount != null)
				return false;
		}
		else if (!lastChargeAmount.equals(other.lastChargeAmount))
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
		builder.append("Download [subscriberID=");
		builder.append(subscriberID);
		builder.append(", toneID=");
		builder.append(toneID);
		builder.append(", shuffleID=");
		builder.append(shuffleID);
		builder.append(", toneName=");
		builder.append(toneName);
		builder.append(", toneType=");
		builder.append(toneType);
		builder.append(", previewFile=");
		builder.append(previewFile);
		builder.append(", rbtFile=");
		builder.append(rbtFile);
		builder.append(", ugcRbtFile=");
		builder.append(ugcRbtFile);
		builder.append(", downloadStatus=");
		builder.append(downloadStatus);
		builder.append(", downloadType=");
		builder.append(downloadType);
		builder.append(", categoryID=");
		builder.append(categoryID);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", selectedBy=");
		builder.append(selectedBy);
		builder.append(", deselectedBy=");
		builder.append(deselectedBy);
		builder.append(", setTime=");
		builder.append(setTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", downloadInfoMap=");
		builder.append(downloadInfoMap);
		builder.append(", isSetForAll=");
		builder.append(isSetForAll);
		builder.append(", nextBillingDate=");
		builder.append(nextBillingDate);
		builder.append(", refID=");
		builder.append(refID);
		builder.append(", selectionInfo=");
		builder.append(selectionInfo);
		builder.append(", deselectionInfo=");
		builder.append(deselectionInfo);
		builder.append(", nextChargingDate=");
		builder.append(nextChargingDate);
		builder.append(", lastChargeAmount=");
		builder.append(lastChargeAmount);
		builder.append("]");
		return builder.toString();
	}
}
