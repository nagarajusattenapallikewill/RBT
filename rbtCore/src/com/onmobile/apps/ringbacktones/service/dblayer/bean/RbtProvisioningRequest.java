package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import java.util.Date;

public class RbtProvisioningRequest
{
	private String chargingClass;
	private Date creationTime;
	private String extraInfo;
	private String mode;
	private String modeInfo;
	private Date nextRetryTime;
	private long requestId;
	private int retryCount;
	private int status;
	private String subscriberId;
	private String transId;
	private int type;
	private int numMaxSelections;
	/**
	 * @return the chargingClass
	 */
	public String getChargingClass() {
		return chargingClass;
	}
	/**
	 * @param chargingClass the chargingClass to set
	 */
	public void setChargingClass(String chargingClass) {
		this.chargingClass = chargingClass;
	}
	/**
	 * @return the creationTime
	 */
	public Date getCreationTime() {
		return creationTime;
	}
	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
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
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	/**
	 * @return the modeInfo
	 */
	public String getModeInfo() {
		return modeInfo;
	}
	/**
	 * @param modeInfo the modeInfo to set
	 */
	public void setModeInfo(String modeInfo) {
		this.modeInfo = modeInfo;
	}
	/**
	 * @return the nextRetryTime
	 */
	public Date getNextRetryTime() {
		return nextRetryTime;
	}
	/**
	 * @param nextRetryTime the nextRetryTime to set
	 */
	public void setNextRetryTime(Date nextRetryTime) {
		this.nextRetryTime = nextRetryTime;
	}
	/**
	 * @return the requestId
	 */
	public long getRequestId() {
		return requestId;
	}
	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	/**
	 * @return the retryCount
	 */
	public int getRetryCount() {
		return retryCount;
	}
	/**
	 * @param retryCount the retryCount to set
	 */
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
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
	 * @return the transId
	 */
	public String getTransId() {
		return transId;
	}
	/**
	 * @param transId the transId to set
	 */
	public void setTransId(String transId) {
		this.transId = transId;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the numMaxSelections
	 */
	public int getNumMaxSelections() {
		return numMaxSelections;
	}
	/**
	 * @param numMaxSelections the numMaxSelections to set
	 */
	public void setNumMaxSelections(int numMaxSelections) {
		this.numMaxSelections = numMaxSelections;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtProvisioningRequest [chargingClass=")
				.append(chargingClass).append(", creationTime=")
				.append(creationTime).append(", extraInfo=").append(extraInfo)
				.append(", mode=").append(mode).append(", modeInfo=")
				.append(modeInfo).append(", nextRetryTime=")
				.append(nextRetryTime).append(", requestId=").append(requestId)
				.append(", retryCount=").append(retryCount).append(", status=")
				.append(status).append(", subscriberId=").append(subscriberId)
				.append(", transId=").append(transId).append(", type=")
				.append(type).append(", numMaxSelections=")
				.append(numMaxSelections).append("]");
		return builder.toString();
	}
}
