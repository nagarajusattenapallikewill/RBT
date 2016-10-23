package com.onmobile.apps.ringbacktones.v2.dto;

public class CallLogDTO {

	private static final long serialVersionUID = -3621507206135574992L;
	private String callerId;
	private String calledId;
	private String wavFile;
	private int categoryId;
	private String type;
	private boolean validationRequired = true;
	private String callerLanguage = null;
	private long calledTime;
	private short callDuration;

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

	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isValidationRequired() {
		return validationRequired;
	}

	public void setValidationRequired(boolean validationRequired) {
		this.validationRequired = validationRequired;
	}

	public String getCallerLanguage() {
		return callerLanguage;
	}

	public void setCallerLanguage(String callerLanguage) {
		this.callerLanguage = callerLanguage;
	}

	public long getCalledTime() {
		return calledTime;
	}

	public void setCalledTime(long calledTime) {
		this.calledTime = calledTime;
	}

	public short getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(short callDuration) {
		this.callDuration = callDuration;
	}
	
	

}
