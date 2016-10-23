package com.onmobile.android.beans;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class ConsentProcessBean {

	Consent consent; 
	String response;
	String subscriberId;
	Subscriber subscriber;
	// comviva circle id change
	String circleID;
	
	public String getCircleID() {
		return circleID;
	}
	public void setCircleID(String circleID) {
		this.circleID = circleID;
	}
	public Consent getConsent() {
		return consent;
	}
	public void setConsent(Consent consent) {
		this.consent = consent;
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
	public Subscriber getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConsentProcessBean [consent=");
		builder.append(consent);
		builder.append(", response=");
		builder.append(response);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", subscriber=");
		builder.append(subscriber);
		builder.append("]");
		return builder.toString();
	}
}
