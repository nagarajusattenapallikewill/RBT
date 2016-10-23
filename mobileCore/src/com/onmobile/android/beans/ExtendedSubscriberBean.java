package com.onmobile.android.beans;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class ExtendedSubscriberBean extends Subscriber {

	private int noOfSpecialSettings;
	
	public int getNoOfSpecialSettings() {
		return noOfSpecialSettings;
	}

	public void setNoOfSpecialSettings(int noOfSpecialSettings) {
		this.noOfSpecialSettings = noOfSpecialSettings;
	}

	public ExtendedSubscriberBean() {
		super();
	}

	public ExtendedSubscriberBean(Subscriber subscriber, int  noOfSpecialSettings) {

		if (subscriber != null) {
			this.setSubscriberID(subscriber.getSubscriberID());
			this.setValidPrefix(subscriber.isValidPrefix());
			this.setCanAllow(subscriber.isCanAllow());
			this.setPrepaid(subscriber.isPrepaid());
			this.setAccessCount(subscriber.getAccessCount());
			this.setStatus(subscriber.getStatus()); 
			this.setSubscriptionState(subscriber.getSubscriptionState());
			this.setLanguage(subscriber.getLanguage());
			this.setCircleID(subscriber.getCircleID());
			this.setUserType(subscriber.getUserType());
			this.setPack(subscriber.getPack()); 
			this.setSubscriptionClass(subscriber.getSubscriptionClass()); 
			this.setCosID(subscriber.getCosID());
			this.setActivatedBy(subscriber.getActivatedBy()); 
			this.setActivationInfo(subscriber.getActivationInfo()); 
			this.setDeactivatedBy(subscriber.getDeactivatedBy());
			this.setLastDeactivationInfo(subscriber.getLastDeactivationInfo()); 
			this.setStartDate(subscriber.getStartDate()); 
			this.setEndDate(subscriber.getEndDate()); 
			this.setNextChargingDate(subscriber.getNextChargingDate()); 
			this.setLastDeactivationDate(subscriber.getLastDeactivationDate()); 
			this.setActivationDate(subscriber.getActivationDate()); 
			this.setUserInfoMap(subscriber.getUserInfoMap()); 
			this.setNewsLetterOn(subscriber.isNewsLetterOn()); 
			this.setDaysAfterDeactivation(subscriber.getDaysAfterDeactivation()); 
			this.setNextBillingDate(subscriber.getNextBillingDate()); 
			this.setTotalDownloads(subscriber.getTotalDownloads()); 
			this.setUdsOn(subscriber.isUdsOn()); 
			this.setRefID(subscriber.getRefID()); 
			this.setOperatorUserInfo(subscriber.getOperatorUserInfo()); 
			this.setNextSelectionAmount(subscriber.getNextSelectionAmount()); 
			this.setNextChargeClass(subscriber.getNextChargeClass()) ;
			this.setChargeDetails(subscriber.getChargeDetails());
			this.setFreemium(subscriber.isFreemium());
			this.setFreemiumSubscriber(subscriber.isFreemiumSubscriber());
			this.setNumOfFreeSongsLeft(subscriber.getNumOfFreeSongsLeft());
		}

		if (noOfSpecialSettings != 0) {
			this.setNoOfSpecialSettings(noOfSpecialSettings);
		}
	}
}
