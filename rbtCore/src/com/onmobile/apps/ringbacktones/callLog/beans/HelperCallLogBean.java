package com.onmobile.apps.ringbacktones.callLog.beans;

import java.util.Date;

public class HelperCallLogBean {

	private int categoryId;
	private String callerId;
	private String calledId;
	private Date timeOfCall;
	private String wavFileName;
	private String callType;

	public HelperCallLogBean() {

	}

	public HelperCallLogBean(int categoryId, String callerId, String calledId,
			Date timeOfCall, String wavFileName,String callType) {
		super();
		this.categoryId = categoryId;
		this.callerId = callerId;
		this.calledId = calledId;
		this.timeOfCall = timeOfCall;
		this.wavFileName = wavFileName;
		this.callType = callType;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public String getCalledId() {
		return calledId;
	}

	public void setCalledId(String calledId) {
		this.calledId = calledId;
	}

	public Date getTimeOfCall() {
		return timeOfCall;
	}

	public void setTimeOfCall(Date timeOfCall) {
		this.timeOfCall = timeOfCall;
	}

	public String getWavFileName() {
		return wavFileName;
	}

	public void setWavFileName(String wavFileName) {
		this.wavFileName = wavFileName;
	}

	@Override
	public String toString() {
		return "EntityBean [categoryId=" + categoryId + ", callerId="
				+ callerId + ", calledId=" + calledId + ", timeOfCall="
				+ timeOfCall + ", wavFileName=" + wavFileName + ", callType="
				+ callType + "]";
	}

}
