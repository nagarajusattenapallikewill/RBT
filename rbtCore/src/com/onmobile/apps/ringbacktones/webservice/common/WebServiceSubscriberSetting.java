/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Arrays;
import java.util.Date;


/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceSubscriberSetting
{
	private String subscriberID = null;
	private String callerID = null;
	private int toneID;
	private String shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String[] previewFiles = null;
	private String[] rbtFiles = null;
	private int fromTime;
	private int fromTimeMinutes;
	private int toTime;
	private int toTimeMinutes;
	private int status;
	private String chargeClass = null;
	private String selInterval = null;
	private int categoryID;
	private String selectionStatus = null;
	private String selectionStatusID = null;
	private String selectionType = null;
	private String selectedBy = null;
	private String selectionInfo = null;
	private String deselectedBy = null;
	private Date setTime = null;
	private Date endTime = null;
	private Date nextChargingDate = null;
	private String refID = null;
	private String selectionExtraInfo = null;
	private Date startTime = null;
	//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
	private String artistName = null;
	private String albumName = null;
	private String categoryName = null;
	private String tonePrice = null;
	private boolean defaultMusic = false;
	private String renewalDate = null;
	private String loopStatus;
    private String clipVcode = null;
    private Boolean isCurrentSetting = null;
    private String udpId = null;
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
	public WebServiceSubscriberSetting()
	{

	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param toneID
	 * @param shuffleID
	 * @param toneName
	 * @param toneType
	 * @param previewFiles
	 * @param rbtFiles
	 * @param fromTime
	 * @param fromTimeMinutes
	 * @param toTime
	 * @param toTimeMinutes
	 * @param status
	 * @param chargeClass
	 * @param selInterval
	 * @param categoryID
	 * @param selectionStatus
	 * @param selectionStatusID
	 * @param selectionType
	 * @param selectedBy
	 * @param selectionInfo
	 * @param deselectedBy
	 * @param setTime
	 * @param endTime
	 * @param nextChargingDate
	 * @param refID
	 * @param selectionExtraInfo
	 */
	public WebServiceSubscriberSetting(String subscriberID, String callerID,
			int toneID, String shuffleID, String toneName, String toneType,
			String[] previewFiles, String[] rbtFiles, int fromTime,
			int fromTimeMinutes, int toTime, int toTimeMinutes, int status,
			String chargeClass, String selInterval, int categoryID,
			String selectionStatus, String selectionStatusID,
			String selectionType, String selectedBy, String selectionInfo,
			String deselectedBy, Date setTime, Date endTime,
			Date nextChargingDate, String refID, String selectionExtraInfo)
	{
		this.subscriberID = subscriberID;
		this.callerID = callerID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFiles = previewFiles;
		this.rbtFiles = rbtFiles;
		this.fromTime = fromTime;
		this.fromTimeMinutes = fromTimeMinutes;
		this.toTime = toTime;
		this.toTimeMinutes = toTimeMinutes;
		this.status = status;
		this.chargeClass = chargeClass;
		this.selInterval = selInterval;
		this.categoryID = categoryID;
		this.selectionStatus = selectionStatus;
		this.selectionStatusID = selectionStatusID;
		this.selectionType = selectionType;
		this.selectedBy = selectedBy;
		this.selectionInfo = selectionInfo;
		this.deselectedBy = deselectedBy;
		this.setTime = setTime;
		this.endTime = endTime;
		this.nextChargingDate = nextChargingDate;
		this.refID = refID;
		this.selectionExtraInfo = selectionExtraInfo;
	}

	
	public String getLoopStatus() {
		return loopStatus;
	}

	public void setLoopStatus(String loopStatus) {
		this.loopStatus = loopStatus;
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
	 * @return the previewFiles
	 */
	public String[] getPreviewFiles()
	{
		return previewFiles;
	}

	/**
	 * @param previewFiles the previewFiles to set
	 */
	public void setPreviewFiles(String[] previewFiles)
	{
		this.previewFiles = previewFiles;
	}

	/**
	 * @return the rbtFiles
	 */
	public String[] getRbtFiles()
	{
		return rbtFiles;
	}

	/**
	 * @param rbtFiles the rbtFiles to set
	 */
	public void setRbtFiles(String[] rbtFiles)
	{
		this.rbtFiles = rbtFiles;
	}

	/**
	 * @return the fromTime
	 */
	public int getFromTime()
	{
		return fromTime;
	}

	/**
	 * @param fromTime the fromTime to set
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
	 * @param fromTimeMinutes the fromTimeMinutes to set
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
	 * @param toTime the toTime to set
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
	 * @param toTimeMinutes the toTimeMinutes to set
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
	 * @param status the status to set
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
	 * @param chargeClass the chargeClass to set
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
	 * @param selInterval the selInterval to set
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
	 * @param categoryID the categoryID to set
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
	 * @param selectionStatus the selectionStatus to set
	 */
	public void setSelectionStatus(String selectionStatus)
	{
		this.selectionStatus = selectionStatus;
	}

	/**
	 * @return the selectionStatusID
	 */
	public String getSelectionStatusID()
	{
		return selectionStatusID;
	}

	/**
	 * @param selectionStatusID the selectionStatusID to set
	 */
	public void setSelectionStatusID(String selectionStatusID)
	{
		this.selectionStatusID = selectionStatusID;
	}

	/**
	 * @return the selectionType
	 */
	public String getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType the selectionType to set
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
	 * @param selectedBy the selectedBy to set
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
	 * @param selectionInfo the selectionInfo to set
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
	 * @return the selectionExtraInfo
	 */
	public String getSelectionExtraInfo()
	{
		return selectionExtraInfo;
	}

	/**
	 * @param selectionExtraInfo the selectionExtraInfo to set
	 */
	public void setSelectionExtraInfo(String selectionExtraInfo)
	{
		this.selectionExtraInfo = selectionExtraInfo;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	
	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getTonePrice() {
		return tonePrice;
	}

	public void setTonePrice(String tonePrice) {
		this.tonePrice = tonePrice;
	}

	public boolean isDefaultMusic() {
		return defaultMusic;
	}

	public void setDefaultMusic(boolean defaultMusic) {
		this.defaultMusic = defaultMusic;
	}

	public String getRenewalDate() {
		return renewalDate;
	}

	public void setRenewalDate(String renewalDate) {
		this.renewalDate = renewalDate;
	}

	public String getClipVcode() {
		return clipVcode;
	}

	public void setClipVcode(String clipVcode) {
		this.clipVcode = clipVcode;
	}

	
	public Boolean getIsCurrentSetting() {
		return isCurrentSetting;
	}

	public void setIsCurrentSetting(Boolean isCurrentSetting) {
		this.isCurrentSetting = isCurrentSetting;
	}
	
	public String getUdpId() {
		return udpId;
	}

	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceSubscriberSetting[callerID = ");
		builder.append(callerID);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", deselectedBy = ");
		builder.append(deselectedBy);
		builder.append(", endTime = ");
		builder.append(endTime);
		builder.append(", fromTime = ");
		builder.append(fromTime);
		builder.append(", fromTimeMinutes = ");
		builder.append(fromTimeMinutes);
		builder.append(", nextChargingDate = ");
		builder.append(nextChargingDate);
		builder.append(", previewFiles = ");
		builder.append(Arrays.toString(previewFiles));
		builder.append(", rbtFiles = ");
		builder.append(Arrays.toString(rbtFiles));
		builder.append(", refID = ");
		builder.append(refID);
		builder.append(", selectedBy = ");
		builder.append(selectedBy);
		builder.append(", selectionExtraInfo = ");
		builder.append(selectionExtraInfo);
		builder.append(", selectionInfo = ");
		builder.append(selectionInfo);
		builder.append(", selectionStatus = ");
		builder.append(selectionStatus);
		builder.append(", selectionStatusID = ");
		builder.append(selectionStatusID);
		builder.append(", selectionType = ");
		builder.append(selectionType);
		builder.append(", selInterval = ");
		builder.append(selInterval);
		builder.append(", setTime = ");
		builder.append(setTime);
		builder.append(", shuffleID = ");
		builder.append(shuffleID);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toneID = ");
		builder.append(toneID);
		builder.append(", toneName = ");
		builder.append(toneName);
		builder.append(", toneType = ");
		builder.append(toneType);
		builder.append(", toTime = ");
		builder.append(toTime);
		builder.append(", toTimeMinutes = ");
		builder.append(toTimeMinutes);
		builder.append(", startTime = ");
		builder.append(startTime);
		builder.append(", artistName = ");
		builder.append(artistName);
		builder.append(", albumName = ");
		builder.append(albumName);
		builder.append(", categoryName = ");
		builder.append(categoryName);
		builder.append(", tonePrice = ");
		builder.append(tonePrice);
		builder.append(", defaultMusic = ");
		builder.append(defaultMusic);
		builder.append(", renewalDate = ");
		builder.append(renewalDate);
		builder.append(", clipVcode = ");
		builder.append(clipVcode);
		builder.append(", isCurrentSetting = ");
		builder.append(isCurrentSetting);
		builder.append(", udpId = ");
		builder.append(udpId);
		builder.append("]");
		return builder.toString();
	}

}
