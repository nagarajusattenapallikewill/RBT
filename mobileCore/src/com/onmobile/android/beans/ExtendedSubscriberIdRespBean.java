package com.onmobile.android.beans;

public class ExtendedSubscriberIdRespBean {
	private String response;
	private String subscriberId;
	private String circleId;

	public ExtendedSubscriberIdRespBean() {
		super();
	}

	public ExtendedSubscriberIdRespBean(String response, String subscriberId, String circleId) {
		super();
		this.response = response;
		this.subscriberId = subscriberId;
		this.circleId = circleId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
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
		builder.append("ExtendedSubscriberIdRespBean [subscriberId=");
		builder.append(subscriberId);
		builder.append(", circleId=");
		builder.append(circleId);
		builder.append("]");
		return builder.toString();
	}
}
