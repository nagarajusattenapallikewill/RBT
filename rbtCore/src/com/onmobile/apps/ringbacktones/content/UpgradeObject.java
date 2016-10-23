package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

public class UpgradeObject {
	String subscriberID;
	String activate;
	Date startDate;
	Date endDate;
	String activationInfo;
	boolean prepaid;
	String cosID;
	int rbtType;
	String extraInfo;
	String circleID;
	String refID;
	String consentStatus;
	String oldSubscriptionClass;
	String newSubscriptionClass;

	public UpgradeObject(String subscriberID, String activate, Date startDate,
			Date endDate, String activationInfo, boolean prepaid, String cosID,
			int rbtType, String extraInfo, String circleID, String refID,
			String consentStatus, String oldSubscriptionClass,
			String newSubscriptionClass) {
		super();
		this.subscriberID = subscriberID;
		this.activate = activate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activationInfo = activationInfo;
		this.prepaid = prepaid;
		this.cosID = cosID;
		this.rbtType = rbtType;
		this.extraInfo = extraInfo;
		this.circleID = circleID;
		this.refID = refID;
		this.consentStatus = consentStatus;
		this.oldSubscriptionClass = oldSubscriptionClass;
		this.newSubscriptionClass = newSubscriptionClass;
	}

	public String getSubscriberID() {
		return subscriberID;
	}

	public String getMode() {
		return activate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getActivationInfo() {
		return activationInfo;
	}

	public boolean isPrepaid() {
		return prepaid;
	}

	public String getCosID() {
		return cosID;
	}

	public int getRbtType() {
		return rbtType;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public String getCircleID() {
		return circleID;
	}

	public String getRefID() {
		return refID;
	}

	public String getConsentStatus() {
		return consentStatus;
	}

	public String getOldSubscriptionClass() {
		return oldSubscriptionClass;
	}

	public String getNewSubscriptionClass() {
		return newSubscriptionClass;
	}

	@Override
	public String toString() {
		return "UpgradeObject [subscriberID=" + subscriberID + ", activate="
				+ activate + ", startDate=" + startDate + ", endDate="
				+ endDate + ", activationInfo=" + activationInfo + ", prepaid="
				+ prepaid + ", cosID=" + cosID + ", rbtType=" + rbtType
				+ ", extraInfo=" + extraInfo + ", circleID=" + circleID
				+ ", refID=" + refID + ", consentStatus=" + consentStatus
				+ ", oldSubscriptionClass=" + oldSubscriptionClass
				+ ", newSubscriptionClass=" + newSubscriptionClass + "]";
	}

}
