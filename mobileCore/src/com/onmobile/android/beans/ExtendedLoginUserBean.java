package com.onmobile.android.beans;

public class ExtendedLoginUserBean {
	
	private String responseString;
	private String circleId;
	
	public ExtendedLoginUserBean() {
		super();
	}
	public ExtendedLoginUserBean(String responseString, String circleId) {
		super();
		this.responseString = responseString;
		this.circleId = circleId;
	}
	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExtendedLoginUserBean [responseString=");
		builder.append(responseString);
		builder.append(", circleId=");
		builder.append(circleId);
		builder.append("]");
		return builder.toString();
	}	
}
