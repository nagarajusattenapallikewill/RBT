package com.onmobile.android.beans;

public class CallerIdGroupBean {
	private String callerId;
	private Integer status;
	private String fromTime;
	private String toTime;
	private String interval;
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getFromTime() {
		return fromTime;
	}
	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}
	public String getToTime() {
		return toTime;
	}
	public void setToTime(String toTime) {
		this.toTime = toTime;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CallerIdGroupBean [callerId=");
		builder.append(callerId);
		builder.append(", status=");
		builder.append(status);
		builder.append(", fromTime=");
		builder.append(fromTime);
		builder.append(", toTime=");
		builder.append(toTime);
		builder.append(", interval=");
		builder.append(interval);
		builder.append("]");
		return builder.toString();
	}
}
