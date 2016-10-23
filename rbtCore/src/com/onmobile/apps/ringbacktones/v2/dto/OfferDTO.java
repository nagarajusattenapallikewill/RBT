package com.onmobile.apps.ringbacktones.v2.dto;

public class OfferDTO {

	public enum OfferType {
		subscription, selection
	}
	
	private String offerId;
	private String offerDesc;
	private String validityDays;
	private String serviceKey;
	private double amount;
	private String offerStatus;
	private OfferType offerType;
	
	
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getOfferDesc() {
		return offerDesc;
	}
	public void setOfferDesc(String offerDesc) {
		this.offerDesc = offerDesc;
	}
	public String getValidityDays() {
		return validityDays;
	}
	public void setValidityDays(String validityDays) {
		this.validityDays = validityDays;
	}
	public String getServiceKey() {
		return serviceKey;
	}
	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getOfferStatus() {
		return offerStatus;
	}
	public void setOfferStatus(String offerStatus) {
		this.offerStatus = offerStatus;
	}
	public OfferType getOfferType() {
		return offerType;
	}
	public void setOfferType(OfferType offerType) {
		this.offerType = offerType;
	}
	
}
