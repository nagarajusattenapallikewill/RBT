package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans;

import java.io.Serializable;
import java.util.Date;

public class RBTChargePerCallPrismRetry extends RBTChargePerCall implements
		Serializable {
	private static final long serialVersionUID = 509085074839192964L;
	private String wavFile;
	private Date retryTime;
	private String refId;
	private long retryCount = 0;
	private short callDuration = -1;
	public RBTChargePerCallPrismRetry() {
		super();
	}

	public RBTChargePerCallPrismRetry(String callerId, String calledId,
			Date calledTime, String wavFile, Date retryTime, String refId, long retryCount) {
		super(callerId, calledId, calledTime);
		this.wavFile = wavFile;
		this.retryTime = retryTime;
		this.refId = refId;
		this.retryCount = retryCount;
	}
 
	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}

	public Date getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(Date retryTime) {
		this.retryTime = retryTime;
	}

	public String getRefId() {
		return refId;
	}

	public short getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(short callDuration) {
		this.callDuration = callDuration;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public long getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(long retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public String toString() {
		return "RBTChargePerCallPrismRetry [wavFile=" + wavFile
				+ ", retryTime=" + retryTime + ", refId=" + refId
				+ ", retryCount=" + retryCount + ", " + super.toString()
				+ ", callDuration=" + callDuration + "]";
	}
}