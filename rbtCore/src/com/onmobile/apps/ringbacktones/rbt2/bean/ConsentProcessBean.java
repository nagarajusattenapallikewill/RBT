package com.onmobile.apps.ringbacktones.rbt2.bean;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class ConsentProcessBean {

	private Consent consent; 
	private String response;
	private String subscriberId;
	private Subscriber subscriber;
	// comviva circle id change
	private String circleID;
	private String callerId;
	
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
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	
	@Override
	public String toString() {
		return "ConsentProcessBean [consent=" + consent + ", response="
				+ response + ", subscriberId=" + subscriberId + ", subscriber="
				+ subscriber + ", circleID=" + circleID + ", callerId="
				+ callerId + "]";
	}	
}

