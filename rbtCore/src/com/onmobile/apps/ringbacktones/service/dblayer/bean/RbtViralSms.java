package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import java.util.Date;

public class RbtViralSms
{
	 private String subscriberId;
	 private Date sentTime;
	 private String smsType;
	 private String callerId;
	 private String clipId;
	 private int searchCount;
	 private String selectedBy;
	 private Date setTime;
	 private String extraInfo;
	 private String circleId;
	 private long smsId;
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
	 * @return the sentTime
	 */
	public Date getSentTime() {
		return sentTime;
	}
	/**
	 * @param sentTime the sentTime to set
	 */
	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}
	/**
	 * @return the smsType
	 */
	public String getSmsType() {
		return smsType;
	}
	/**
	 * @param smsType the smsType to set
	 */
	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}
	/**
	 * @return the callerId
	 */
	public String getCallerId() {
		return callerId;
	}
	/**
	 * @param callerId the callerId to set
	 */
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	/**
	 * @return the clipId
	 */
	public String getClipId() {
		return clipId;
	}
	/**
	 * @param clipId the clipId to set
	 */
	public void setClipId(String clipId) {
		this.clipId = clipId;
	}
	/**
	 * @return the searchCount
	 */
	public int getSearchCount() {
		return searchCount;
	}
	/**
	 * @param searchCount the searchCount to set
	 */
	public void setSearchCount(int searchCount) {
		this.searchCount = searchCount;
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
	 * @return the circleId
	 */
	public String getCircleId() {
		return circleId;
	}
	/**
	 * @param circleId the circleId to set
	 */
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	/**
	 * @return the smsId
	 */
	public long getSmsId() {
		return smsId;
	}
	/**
	 * @param smsId the smsId to set
	 */
	public void setSmsId(long smsId) {
		this.smsId = smsId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtViralSms [subscriberId=").append(subscriberId)
				.append(", sentTime=").append(sentTime).append(", smsType=")
				.append(smsType).append(", callerId=").append(callerId)
				.append(", clipId=").append(clipId).append(", searchCount=")
				.append(searchCount).append(", selectedBy=").append(selectedBy)
				.append(", setTime=").append(setTime).append(", extraInfo=")
				.append(extraInfo).append(", circleId=").append(circleId)
				.append(", smsId=").append(smsId).append("]");
		return builder.toString();
	}
}
