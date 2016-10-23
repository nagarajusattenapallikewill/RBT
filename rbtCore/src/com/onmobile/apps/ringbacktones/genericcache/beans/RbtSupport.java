package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;
import java.util.Date;

public class RbtSupport implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5002002753342211750L;
	
	public static final int HASH_DOWNLOAD = 1;
	
	public static final int PROCESS_PENDING = 21;
	
	private long id;
	private long subscriberId;
	private long callerId;
	private int clipId;
	private Date requestDate;
	private int retryCount = 0;
	private int type;
	private int status;
	private String extraInfo;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(long subscriberId) {
		this.subscriberId = subscriberId;
	}	
	public long getCallerId() {
		return callerId;
	}
	public void setCallerId(long callerId) {
		this.callerId = callerId;
	}
	public int getClipId() {
		return clipId;
	}
	public void setClipId(int clipId) {
		this.clipId = clipId;
	}
	public Date getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}	
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "RbtSupport [id=" + id + ", subscriberId=" + subscriberId
				+ ", callerId=" + callerId + ", clipId=" + clipId
				+ ", requestDate=" + requestDate + ", retryCount=" + retryCount
				+ ", type=" + type + ", status=" + status + ", extraInfo="
				+ extraInfo + "]";
	}
	
	
	
}
