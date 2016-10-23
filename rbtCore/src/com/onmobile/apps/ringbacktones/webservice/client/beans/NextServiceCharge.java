package com.onmobile.apps.ringbacktones.webservice.client.beans;



public class NextServiceCharge {

	private NewChargeClass chargeClass = null;
	private NewSubscriptionClass subscriptionClass = null;
	private String subscriberStatus = "";
	public String getSubscriberStatus() {
		return subscriberStatus;
	}

	public void setSubscriberStatus(String subscriberStatus) {
		this.subscriberStatus = subscriberStatus;
	}

	public NewChargeClass getChargeClass() {
		return chargeClass;
	}

	public void setChargeClass(NewChargeClass chargeClass) {
		this.chargeClass = chargeClass;
	}

	public NewSubscriptionClass getSubscriptionClass() {
		return subscriptionClass;
	}

	public void setSubscriptionClass(NewSubscriptionClass subscriptionClass) {
		this.subscriptionClass = subscriptionClass;
	}

	public NextServiceCharge(NewChargeClass chargeClassNew,
			NewSubscriptionClass subClass,String subscriberStatus) {
		this.chargeClass = chargeClassNew;
		this.subscriptionClass = subClass;
		this.subscriberStatus = subscriberStatus;
	}

	public NextServiceCharge() {
		
	}
}
