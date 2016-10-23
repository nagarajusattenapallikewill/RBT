package com.onmobile.apps.ringbacktones.callLog.beans;

import java.util.Date;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;

public class CallLog {

	private Date timeOfCall;
	private String callerId;
	private String calledId;
	private String callType;
	private Clip clip;
	private Category category;
	

	public CallLog() {

	}

	public CallLog(Date timeOfCall, String callerId, String calledId,
			String callType, Clip clip, Category category) {
		
		this.timeOfCall = timeOfCall;
		this.callerId = callerId;
		this.calledId = calledId;
		this.callType = callType;
		this.clip = clip;
		this.category = category;
	}

	public Date getTimeOfCall() {
		return timeOfCall;
	}

	public void setTimeOfCall(Date timeOfCall) {
		this.timeOfCall = timeOfCall;
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

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public Clip getClip() {
		return clip;
	}

	public void setClip(Clip clip) {
		this.clip = clip;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	

	@Override
	public String toString() {
		return "CallLog [timeOfCall=" + timeOfCall + ", callerId=" + callerId
				+ ", calledId=" + calledId + ", callType=" + callType
				+ ", clip=" + clip + ", category=" + category + "]";
	}


}
