package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import java.util.Date;

public class RbtSubscriberDownloads
{
	private String subscriberId;
	private String promoId;
	private char downloadStatus;
	private Date setTime;
	private Date startTime;
	private Date endTime;
	private int categoryID;
	private String deactivatedBy;
	private int categoryType;
	private String classType;
	private String selectedBy;
	private String deselectedBy;
	private String refId;
	private String extraInfo;
	private String selectionInfo;
	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}
	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	/**
	 * @return the promoId
	 */
	public String getPromoId() {
		return promoId;
	}
	/**
	 * @param promoId the promoId to set
	 */
	public void setPromoId(String promoId) {
		this.promoId = promoId;
	}
	/**
	 * @return the downloadStatus
	 */
	public char getDownloadStatus() {
		return downloadStatus;
	}
	/**
	 * @param downloadStatus the downloadStatus to set
	 */
	public void setDownloadStatus(char downloadStatus) {
		this.downloadStatus = downloadStatus;
	}
	/**
	 * @return the setTime
	 */
	public Date getSetTime() {
		return setTime;
	}
	/**
	 * @param setTime the setTime to set
	 */
	public void setSetTime(Date setTime) {
		this.setTime = setTime;
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
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return the categoryID
	 */
	public int getCategoryID() {
		return categoryID;
	}
	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	/**
	 * @return the deactivatedBy
	 */
	public String getDeactivatedBy() {
		return deactivatedBy;
	}
	/**
	 * @param deactivatedBy the deactivatedBy to set
	 */
	public void setDeactivatedBy(String deactivatedBy) {
		this.deactivatedBy = deactivatedBy;
	}
	/**
	 * @return the categoryType
	 */
	public int getCategoryType() {
		return categoryType;
	}
	/**
	 * @param categoryType the categoryType to set
	 */
	public void setCategoryType(int categoryType) {
		this.categoryType = categoryType;
	}
	/**
	 * @return the classType
	 */
	public String getClassType() {
		return classType;
	}
	/**
	 * @param classType the classType to set
	 */
	public void setClassType(String classType) {
		this.classType = classType;
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
	 * @return the deselectedBy
	 */
	public String getDeselectedBy() {
		return deselectedBy;
	}
	/**
	 * @param deselectedBy the deselectedBy to set
	 */
	public void setDeselectedBy(String deselectedBy) {
		this.deselectedBy = deselectedBy;
	}
	/**
	 * @return the refId
	 */
	public String getRefId() {
		return refId;
	}
	/**
	 * @param refId the refId to set
	 */
	public void setRefId(String refId) {
		this.refId = refId;
	}
	/**
	 * @return the extraInfo
	 */
	public String getExtraInfo() {
		return extraInfo;
	}
	/**
	 * @param extraInfo the extraInfo to set
	 */
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtSubscriberDownloads [subscriberId=")
				.append(subscriberId).append(", promoId=").append(promoId)
				.append(", downloadStatus=").append(downloadStatus)
				.append(", setTime=").append(setTime).append(", startTime=")
				.append(startTime).append(", endTime=").append(endTime)
				.append(", categoryID=").append(categoryID)
				.append(", deactivatedBy=").append(deactivatedBy)
				.append(", categoryType=").append(categoryType)
				.append(", classType=").append(classType)
				.append(", selectedBy=").append(selectedBy)
				.append(", deselectedBy=").append(deselectedBy)
				.append(", refId=").append(refId).append(", extraInfo=")
				.append(extraInfo).append(", selectionInfo=")
				.append(selectionInfo).append("]");
		return builder.toString();
	}
	
	
	
}
