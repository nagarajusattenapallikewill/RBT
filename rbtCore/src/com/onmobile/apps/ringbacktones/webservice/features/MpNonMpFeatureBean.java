package com.onmobile.apps.ringbacktones.webservice.features;

import com.onmobile.apps.ringbacktones.content.Subscriber;

public class MpNonMpFeatureBean {
	private Subscriber subscriber;
	private String subscriberId;
	private String chargeClass;
	private String mode;
	private boolean isDownloadsModel;
	
	public MpNonMpFeatureBean(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public Subscriber getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getChargeClass() {
		return chargeClass;
	}
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public boolean isDownloadsModel() {
		return isDownloadsModel;
	}
	public void setDownloadsModel(boolean isDownloadsModel) {
		this.isDownloadsModel = isDownloadsModel;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MpNonMpFeatureBean [subscriber=");
		builder.append(subscriber);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", isDownloadsModel=");
		builder.append(isDownloadsModel);
		builder.append("]");
		return builder.toString();
	}
}