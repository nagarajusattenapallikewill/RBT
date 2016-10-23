package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import java.util.Date;

public class RbtSubscriberAnnouncement
{
	private long sequenceId;
	private String subscriberId;
	private int clipId;
	private int status;
	private Date activationDate;
	private Date deactivationDate;
	private String timeInterval;
	private String frequency;
	/**
	 * @return the sequenceId
	 */
	public long getSequenceId() {
		return sequenceId;
	}
	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
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
	 * @return the clipId
	 */
	public int getClipId() {
		return clipId;
	}
	/**
	 * @param clipId the clipId to set
	 */
	public void setClipId(int clipId) {
		this.clipId = clipId;
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
	 * @return the activationDate
	 */
	public Date getActivationDate() {
		return activationDate;
	}
	/**
	 * @param activationDate the activationDate to set
	 */
	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}
	/**
	 * @return the deactivationDate
	 */
	public Date getDeactivationDate() {
		return deactivationDate;
	}
	/**
	 * @param deactivationDate the deactivationDate to set
	 */
	public void setDeactivationDate(Date deactivationDate) {
		this.deactivationDate = deactivationDate;
	}
	/**
	 * @return the timeInterval
	 */
	public String getTimeInterval() {
		return timeInterval;
	}
	/**
	 * @param timeInterval the timeInterval to set
	 */
	public void setTimeInterval(String timeInterval) {
		this.timeInterval = timeInterval;
	}
	/**
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}
	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtSubscriberAnnouncement [sequenceId=")
				.append(sequenceId).append(", subscriberId=")
				.append(subscriberId).append(", clipId=").append(clipId)
				.append(", status=").append(status).append(", activationDate=")
				.append(activationDate).append(", deactivationDate=")
				.append(deactivationDate).append(", timeInterval=")
				.append(timeInterval).append(", frequency=").append(frequency)
				.append("]");
		return builder.toString();
	}
}
