package com.onmobile.android.beans;

public class OfferBean {
	
	private String subscriberId;
	private String offerId;
	private String srvKey;
	private double amount;
	private String description;
	private String offerValidity;
	private String offerRenewalAmount;
	private String offerRenewalValidity;
	//RBT-15120 VF Spain
	private String smOfferType;
	
	
	
	public String getSmOfferType() {
		return smOfferType;
	}
	public void setSmOfferType(String smOfferType) {
		this.smOfferType = smOfferType;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getSrvKey() {
		return srvKey;
	}
	public void setSrvKey(String srvKey) {
		this.srvKey = srvKey;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOfferValidity() {
		return offerValidity;
	}
	public void setOfferValidity(String offerValidity) {
		this.offerValidity = offerValidity;
	}
	public String getOfferRenewalAmount() {
		return offerRenewalAmount;
	}
	public void setOfferRenewalAmount(String offerRenewalAmount) {
		this.offerRenewalAmount = offerRenewalAmount;
	}
	public String getOfferRenewalValidity() {
		return offerRenewalValidity;
	}
	public void setOfferRenewalValidity(String offerRenewalValidity) {
		this.offerRenewalValidity = offerRenewalValidity;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferBean [subscriberId=");
		builder.append(subscriberId);
		builder.append(", offerId=");
		builder.append(offerId);
		builder.append(", srvKey=");
		builder.append(srvKey);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", description=");
		builder.append(description);
		builder.append(", offerValidity=");
		builder.append(offerValidity);
		builder.append(", offerRenewalAmount=");
		builder.append(offerRenewalAmount);
		builder.append(", offerRenewalValidity=");
		builder.append(offerRenewalValidity);
		builder.append(", smOfferType=");
		builder.append(smOfferType);
		builder.append("]");
		return builder.toString();
	}
	
}
