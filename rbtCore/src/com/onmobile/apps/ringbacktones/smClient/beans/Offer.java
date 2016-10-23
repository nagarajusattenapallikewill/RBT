package com.onmobile.apps.ringbacktones.smClient.beans;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;

/**
 * This is the wrapper class for the Offer object provided by the SM Client.
 * 
 * @author Sreekar
 * @since 2009-11-04
 */
public class Offer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5092509662972997585L;

	private static final Logger LOGGER = Logger.getLogger(Offer.class);

	// public static final int OFFER_TYPE_COMBO = 3;
	public static final int OFFER_TYPE_ADVANCE_RENTAL = 4;
	public static final int OFFER_TYPE_CHURN = 5;
	public static final int OFFER_TYPE_PACK = 6;
	public static final int OFFER_TYPE_SELECTION = 2;
	public static final int OFFER_TYPE_SUBSCRIPTION = 1;

	private double amount;
	// Credit available added for Vf-Spain, which is free songs available for users
	private int creditsAvailable = -1;
	private String metaInfo;
	private String offerDesc;
	private String offerID;
	private String offerStatus;
	private String smOfferType;
	private String smRate;
	private String srvKey;
	private int type;
	private int validityDays;
	private int offerTypeValue;

	public Offer() {

	}

	public Offer(com.onmobile.prism.client.core.Offer offer) {
		this(offer, OFFER_TYPE_SUBSCRIPTION);
	}

	public Offer(com.onmobile.prism.client.core.Offer offer, int type) {
		offerID = offer.getOfferID();
		offerDesc = offer.getOfferDesc();
		amount = offer.getAmount();
		srvKey = offer.getSrvKey();
		validityDays = offer.getValidityDays();
		this.type = type;
		metaInfo = offer.getMetaInfo();
		offerStatus = offer.getStatus();
		smOfferType = offer.getOfferType();
		smRate = offer.getRateAsString();

		// Credit changes
		if (offer.getOfferType() != null && offer.getOfferType().startsWith("CREDIT")) {
			try {
				String[] tempArray = offer.getOfferType().split("/");
				creditsAvailable = Integer.parseInt(tempArray[1]);
				smOfferType = "CREDIT";
			}
			catch (Exception e) {
				creditsAvailable = -1;
				LOGGER.error("Invalid credit information. SM Offer type is " + offer.getOfferType());
			}
		}

		updateSrvKey();
		if (RBTDeploymentFinder.isRRBTSystem())
			updateRRBTSrvKey();
	}

	public int getOfferTypeValue() {
		return offerTypeValue;
	}

	public void setOfferTypeValue(int offerTypeValue) {
		this.offerTypeValue = offerTypeValue;
	}

	public double getAmount() {
		return amount;
	}

	public int getCreditsAvailable() {
		return creditsAvailable;
	}

	public String getMetaInfo() {
		return metaInfo;
	}

	public void setOfferID(String offerID) {
		this.offerID = offerID;
	}

	public String getOfferDescription() {
		return offerDesc;
	}

	public String getOfferID() {
		return offerID;
	}

	/**
	 * @return the offerStatus
	 */
	public String getOfferStatus() {
		return offerStatus;
	}

	public int getOfferType() {
		return type;
	}

	public String getSmOfferType() {
		return smOfferType;
	}

	public String getSmRate() {
		return smRate;
	}

	public String getSrvKey() {
		return srvKey;
	}

	public int getValidityDays() {
		return validityDays;
	}

	public void setOfferDesc(String offerDesc) {
		this.offerDesc = offerDesc;
	}

	/**
	 * @param offerStatus
	 *            the offerStatus to set
	 */
	public void setOfferStatus(String offerStatus) {
		this.offerStatus = offerStatus;
	}

	public void setSrvKey(String srvKey) {
		this.srvKey = srvKey;
	}

	@Override
	public String toString() {
		return "Offer [offerID=" + offerID + ", offerDesc=" + offerDesc + ", amount=" + amount + ", srvKey=" + srvKey + ", validityDays="
				+ validityDays + ", offerStatus=" + offerStatus + ", metaInfo=" + metaInfo + ", type=" + type + ", smOfferType="
				+ smOfferType + ", smRate=" + smRate + "]";
	}

	private void updateRRBTSrvKey() {
		srvKey = srvKey.replaceAll("_RRBT", "");
	}

	private void updateSrvKey() {
		switch (this.type) {
		case OFFER_TYPE_SUBSCRIPTION:
			srvKey = srvKey.replaceAll("RBT_ACT_", "");
			break;
		case OFFER_TYPE_SELECTION:
			srvKey = srvKey.replaceAll("RBT_SEL_", "");
			break;
		case OFFER_TYPE_PACK:
			srvKey = srvKey.replaceAll("RBT_PACK_", "");
			break;
		}
	}
}