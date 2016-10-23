package com.onmobile.apps.ringbacktones.webservice.client.beans;

public class NewSubscriptionClass {
	private String serviceKey;
	private String amount;
	private String validitiy;
	private boolean isRenewal;
	private String renewalAmount;
	private int offerId;
	private String renewalValidity;

	public String getRenewalValidity() {
		return renewalValidity;
	}

	public void setRenewalValidity(String renewalValidity) {
		this.renewalValidity = renewalValidity;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public int getOfferID() {
		return offerId;
	}

	public void setOfferID(int offerID) {
		this.offerId = offerID;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getValiditiy() {
		return validitiy;
	}

	public void setValiditiy(String validitiy) {
		this.validitiy = validitiy;
	}

	public boolean getIsRenewal() {
		return isRenewal;
	}

	public void setIsRenewal(boolean isRenewal) {
		this.isRenewal = isRenewal;
	}

	public String getRenewalAmount() {
		return renewalAmount;
	}

	public void setRenewalAmount(String renewalAmount) {
		this.renewalAmount = renewalAmount;
	}

	public NewSubscriptionClass(boolean isRenewal, String renewalAmount,
			String validitiy, String amount, String serviceKey, int offerId,
			String renewalValidity) {
		this.amount = amount;
		this.isRenewal = isRenewal;
		this.renewalAmount = renewalAmount;
		this.validitiy = validitiy;
		this.serviceKey = serviceKey;
		this.offerId = offerId;
		this.renewalValidity = renewalValidity;
	}

	public NewSubscriptionClass() {

	}

}
