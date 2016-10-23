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
public class WebServiceSubscriberDownload
{
	private String subscriberID = null;
	private int toneID;
	private String shuffleID;
	private String toneName = null;
	private String toneType = null;
	private String[] previewFiles = null;
	private String[] rbtFiles = null;
	private String downloadStatus = null;
	private char downloadStatusID;
	private String downloadType = null;
	private int categoryID;
	private String chargeClass = null;
	private String selectedBy = null;
	private String deselectedBy = null;
	private Date setTime = null;
	private Date endTime = null;
	private String refID = null;
	private String downloadInfo = null;
	private Date startTime = null;
	private String selectionInfo = null;
	//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
	private String artistName = null;
	private String albumName = null;
	private String categoryName = null;
	private String tonePrice = null;
	private boolean defaultMusic = false;
	private String renewalDate = null;
    private String clipVcode = null;
    private Date lastChargedDate = null;
    private Date nextBillingDate = null;
	/**
	 * 
	 */
	public WebServiceSubscriberDownload()
	{

	}

	/**
	 * @param subscriberID
	 * @param toneID
	 * @param shuffleID
	 * @param toneName
	 * @param toneType
	 * @param previewFiles
	 * @param rbtFiles
	 * @param downloadStatus
	 * @param downloadStatusID
	 * @param downloadType
	 * @param categoryID
	 * @param chargeClass
	 * @param selectedBy
	 * @param deselectedBy
	 * @param setTime
	 * @param endTime
	 * @param refID
	 * @param downloadInfo
	 */
	public WebServiceSubscriberDownload(String subscriberID, int toneID,
			String shuffleID, String toneName, String toneType,
			String[] previewFiles, String[] rbtFiles, String downloadStatus,
			char downloadStatusID, String downloadType, int categoryID,
			String chargeClass, String selectedBy, String deselectedBy,
			Date setTime, Date endTime, String refID, String downloadInfo)
	{
		this.subscriberID = subscriberID;
		this.toneID = toneID;
		this.shuffleID = shuffleID;
		this.toneName = toneName;
		this.toneType = toneType;
		this.previewFiles = previewFiles;
		this.rbtFiles = rbtFiles;
		this.downloadStatus = downloadStatus;
		this.downloadStatusID = downloadStatusID;
		this.downloadType = downloadType;
		this.categoryID = categoryID;
		this.chargeClass = chargeClass;
		this.selectedBy = selectedBy;
		this.deselectedBy = deselectedBy;
		this.setTime = setTime;
		this.endTime = endTime;
		this.refID = refID;
		this.downloadInfo = downloadInfo;
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
	 * @return the downloadStatusID
	 */
	public char getDownloadStatusID()
	{
		return downloadStatusID;
	}

	/**
	 * @param downloadStatusID the downloadStatusID to set
	 */
	public void setDownloadStatusID(char downloadStatusID)
	{
		this.downloadStatusID = downloadStatusID;
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
	 * @return the downloadInfo
	 */
	public String getDownloadInfo()
	{
		return downloadInfo;
	}

	/**
	 * @param downloadInfo the downloadInfo to set
	 */
	public void setDownloadInfo(String downloadInfo)
	{
		this.downloadInfo = downloadInfo;
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

	/**
	 * @return the selectionInfo
	 */
	public String getSelectionInfo() {
		return selectionInfo;
	}

	/**
	 * @param selectionInfo the selectionInfo to set
	 */
	public void setSelectionInfo(String selectionInfo) {
		this.selectionInfo = selectionInfo;
	}

	
	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
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
	
	public Date getLastChargedDate() {
		return lastChargedDate;
	}

	public void setLastChargedDate(Date lastChargedDate) {
		this.lastChargedDate = lastChargedDate;
	}

	public Date getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(Date nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceSubscriberDownload[categoryID = ");
		builder.append(categoryID);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", deselectedBy = ");
		builder.append(deselectedBy);
		builder.append(", downloadInfo = ");
		builder.append(downloadInfo);
		builder.append(", downloadStatus = ");
		builder.append(downloadStatus);
		builder.append(", downloadStatusID = ");
		builder.append(downloadStatusID);
		builder.append(", downloadType = ");
		builder.append(downloadType);
		builder.append(", endTime = ");
		builder.append(endTime);
		builder.append(", previewFiles = ");
		builder.append(Arrays.toString(previewFiles));
		builder.append(", rbtFiles = ");
		builder.append(Arrays.toString(rbtFiles));
		builder.append(", refID = ");
		builder.append(refID);
		builder.append(", selectedBy = ");
		builder.append(selectedBy);
		builder.append(", setTime = ");
		builder.append(setTime);
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
		builder.append(", startTime = ");
		builder.append(startTime);
		builder.append(", selectionInfo = ");
		builder.append(selectionInfo);
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
		builder.append(", lastChargedDate = ");
		builder.append(lastChargedDate);
		builder.append(", nextBillingDate = ");
		builder.append(nextBillingDate);
		builder.append("]");
		return builder.toString();
	}

}
