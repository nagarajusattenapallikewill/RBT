package com.onmobile.apps.ringbacktones.v2.dto;

public class RBTViralPromotion {

	private String callerId = null;
	private String calledId = null;
	private long calledTime;
	private short callDuration;
	private String rbtWavFile = null;
	private String messageType;
	private boolean validationRequired = true;
	private String circleId = null;
	private String callerLanguage = null;
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
	public String getRbtWavFile() {
		return rbtWavFile;
	}
	public void setRbtWavFile(String rbtWavFile) {
		this.rbtWavFile = rbtWavFile;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public boolean isValidationRequired() {
		return validationRequired;
	}
	public void setValidationRequired(boolean validationRequired) {
		this.validationRequired = validationRequired;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	public String getCallerLanguage() {
		return callerLanguage;
	}
	public void setCallerLanguage(String callerLanguage) {
		this.callerLanguage = callerLanguage;
	}
	
	
	
}
