package com.onmobile.apps.ringbacktones.v2.dto;

import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO.OfferType;

public class OfferDTOBuilder {

	OfferDTO offerDto;
	
	public OfferDTOBuilder(String offerId){
		offerDto = new OfferDTO();
		offerDto.setOfferId(offerId);
	}
	
	public OfferDTO buildOfferDTO(){
		return offerDto;
	}
	
	public OfferDTOBuilder setOfferId(String offerId) {
		offerDto.setOfferId(offerId);
		return this;
	}
	public OfferDTOBuilder setOfferDesc(String offerDesc) {
		offerDto.setOfferDesc(offerDesc);
		return this;
	}
	public OfferDTOBuilder setValidityDays(String validityDays) {
		offerDto.setValidityDays(validityDays);
		return this;
	}
	public OfferDTOBuilder setServiceKey(String serviceKey) {
		offerDto.setServiceKey(serviceKey);
		return this;
	}
	public OfferDTOBuilder setAmount(double amount) {
		offerDto.setAmount(amount);
		return this;
	}
	public OfferDTOBuilder setOfferStatus(String offerStatus) {
		offerDto.setOfferStatus(offerStatus);
		return this;
	}
	public OfferDTOBuilder setOfferType(int offerType) {
		if(offerType == Offer.OFFER_TYPE_SUBSCRIPTION) {
			offerDto.setOfferType(OfferType.subscription);
		}
		else if(offerType == Offer.OFFER_TYPE_SELECTION) {
			offerDto.setOfferType(OfferType.selection);
		}
		return this;
	}
	
}
