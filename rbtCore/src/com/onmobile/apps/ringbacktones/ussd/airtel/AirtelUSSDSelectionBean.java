package com.onmobile.apps.ringbacktones.ussd.airtel;

public class AirtelUSSDSelectionBean {
	private String subscriberId;
	private String callerId;
	private String chargeClass;
	private String operatorUserInfo;
	private String catID;
	private String circleId;
	private String clipId;
	private boolean isPrepaid;
	private boolean isUdsOn;
	private boolean isLoop;
	private int status;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	public String getChargeClass() {
		return chargeClass;
	}
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}
	public String getOperatorUserInfo() {
		return operatorUserInfo;
	}
	public void setOperatorUserInfo(String operatorUserInfo) {
		this.operatorUserInfo = operatorUserInfo;
	}
	public String getCatID() {
		return catID;
	}
	public void setCatID(String catID) {
		this.catID = catID;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	public String getClipId() {
		return clipId;
	}
	public void setClipId(String clipId) {
		this.clipId = clipId;
	}
	public boolean isPrepaid() {
		return isPrepaid;
	}
	public void setPrepaid(boolean isPrepaid) {
		this.isPrepaid = isPrepaid;
	}
	public boolean isUdsOn() {
		return isUdsOn;
	}
	public void setUdsOn(boolean isUdsOn) {
		this.isUdsOn = isUdsOn;
	}
	public boolean isLoop() {
		return isLoop;
	}
	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}
}
