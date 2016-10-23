package com.onmobile.apps.ringbacktones.rbt2.bean;

import java.util.Date;

public class SelectionReqBean {
	
	private String subscriberId = null;
	private Boolean isDirectActivation = null;
	private Boolean isDeactCorporateUser = null;
	private Boolean isDirectDeactivation = null;
	private String mode = null;
	private String udpId = null;
	private String callerID = null;
	private String categoryID = null;
	private String clipID = null;
	private String refID = null;
	private Boolean isDtoCRequest = false;
	private Boolean isSelDirectActivation = null;
	private Date selectionStartTime = null;
	private Integer selectionType = null;
	private String profileHours = null;
	private Date selectionEndTime = null;
	private Integer fromTime = null;
	private Integer fromTimeMinutes = null;
	private Integer toTime = null;
	private Integer toTimeMinutes = null;
	private String toneID = null;
	private String playCount = null;
	private int status;
	private String firstName = null;
	private String lastName = null;
	
	// Added for OI brazil inloop workaround soln
	private Boolean inLoop = null;
	
	public Boolean getInLoop() {
		return inLoop;
	}
	public void setInLoop(Boolean inLoop) {
		this.inLoop = inLoop;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getPlayCount() {
		return playCount;
	}
	public void setPlayCount(String playCount) {
		this.playCount = playCount;
	}
	public String getToneID() {
		return toneID;
	}
	public void setToneID(String toneID) {
		this.toneID = toneID;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public Boolean getIsDirectActivation() {
		return isDirectActivation;
	}
	public void setIsDirectActivation(Boolean isDirectActivation) {
		this.isDirectActivation = isDirectActivation;
	}
	public Boolean getIsDeactCorporateUser() {
		return isDeactCorporateUser;
	}
	public void setIsDeactCorporateUser(Boolean isDeactCorporateUser) {
		this.isDeactCorporateUser = isDeactCorporateUser;
	}
	public Boolean getIsDirectDeactivation() {
		return isDirectDeactivation;
	}
	public void setIsDirectDeactivation(Boolean isDirectDeactivation) {
		this.isDirectDeactivation = isDirectDeactivation;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getUdpId() {
		return udpId;
	}
	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}
	public String getCallerID() {
		return callerID;
	}
	public void setCallerID(String callerID) {
		this.callerID = callerID;
	}
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	public String getClipID() {
		return clipID;
	}
	public void setClipID(String clipID) {
		this.clipID = clipID;
	}
	public String getRefID() {
		return refID;
	}
	public void setRefID(String refID) {
		this.refID = refID;
	}
	public Boolean getIsDtoCRequest() {
		return isDtoCRequest;
	}
	public void setIsDtoCRequest(Boolean isDtoCRequest) {
		this.isDtoCRequest = isDtoCRequest;
	}
	public Boolean getIsSelDirectActivation() {
		return isSelDirectActivation;
	}
	public void setIsSelDirectActivation(Boolean isSelDirectActivation) {
		this.isSelDirectActivation = isSelDirectActivation;
	}
	public Date getSelectionStartTime() {
		return selectionStartTime;
	}
	public void setSelectionStartTime(Date selectionStartTime) {
		this.selectionStartTime = selectionStartTime;
	}
	public Integer getSelectionType() {
		return selectionType;
	}
	public void setSelectionType(Integer selectionType) {
		this.selectionType = selectionType;
	}
	public String getProfileHours() {
		return profileHours;
	}
	public void setProfileHours(String profileHours) {
		this.profileHours = profileHours;
	}
	public Date getSelectionEndTime() {
		return selectionEndTime;
	}
	public void setSelectionEndTime(Date selectionEndTime) {
		this.selectionEndTime = selectionEndTime;
	}
	public Integer getFromTime() {
		return fromTime;
	}
	public void setFromTime(Integer fromTime) {
		this.fromTime = fromTime;
	}
	public Integer getFromTimeMinutes() {
		return fromTimeMinutes;
	}
	public void setFromTimeMinutes(Integer fromTimeMinutes) {
		this.fromTimeMinutes = fromTimeMinutes;
	}
	public Integer getToTime() {
		return toTime;
	}
	public void setToTime(Integer toTime) {
		this.toTime = toTime;
	}
	public Integer getToTimeMinutes() {
		return toTimeMinutes;
	}
	public void setToTimeMinutes(Integer toTimeMinutes) {
		this.toTimeMinutes = toTimeMinutes;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
