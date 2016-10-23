package com.onmobile.apps.ringbacktones.webservice.features.RN;


public class RNBean {
	private String responseString;
	private String api;
	private String action;
	private String info;
	private String subscriberId;
	private String clipId;
	private String mode;
	
	public RNBean() {
		super();
	}
	
	public RNBean(String responseString, String api, String action, String info, String subscriberId, String clipId, String mode) {
		super();
		this.responseString = responseString;
		this.api = api;
		this.action = action;
		this.info = info;
		this.subscriberId = subscriberId;
		this.clipId = clipId;
		this.mode = mode;
	}

	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getClipId() {
		return clipId;
	}
	public void setClipId(String clipId) {
		this.clipId = clipId;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RNBean [responseString=");
		builder.append(responseString);
		builder.append(", api=");
		builder.append(api);
		builder.append(", action=");
		builder.append(action);
		builder.append(", info=");
		builder.append(info);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", clipId=");
		builder.append(clipId);
		builder.append(", mode=");
		builder.append(mode);
		builder.append("]");
		return builder.toString();
	}
}