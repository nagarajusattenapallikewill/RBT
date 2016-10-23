package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * 
 * @author Sreekar
 * 
 * Offer DAO for the offer returned by RBTSMClient
 *
 */
public class Offer {
	public static final String CATEGORY_CHARGE_CLASS = "CATEGORY_CHARGE_CLASS";
	public static final String CLIP_CHARGE_CLASS = "CLIP_CHARGE_CLASS";
	public static final String CLIP_ID = "CLIP_ID";
	// Constants for Extra info HashMap
	// CONTENT_TYPE, RBT_TYPE, SUBSCRIPTION_CLASS,
	// CLIP_CHARGE_CLASS, CATEGORY_CHARGE_CLASS, UI_CHARGE_CLASS, LITE_USER
	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String LITE_USER = "LITE_USER";
	public static final int OFFER_TYPE_ADVANCE_RENTAL = 4;
	
	
	public static final String OFFER_TYPE_ADVANCE_RENTAL_STR = "adv";
	public static final int OFFER_TYPE_CHURN = 5;
	public static final String OFFER_TYPE_CHURN_STR = "churn";
	public static final int OFFER_TYPE_COMBO = 3;
	public static final String OFFER_TYPE_COMBO_STR = "combo";
	
	
	public static final int OFFER_TYPE_MUSIC_PACK = 7;
	public static final int OFFER_TYPE_SELECTION = 2;
	public static final String OFFER_TYPE_SELECTION_STR = "sel";
	public static final int OFFER_TYPE_SUBSCRIPTION = 1;
	// These constants were added as only int constants were exposed to UI's initially. These string
	// constants were introduced for amoeba calls
	public static final String OFFER_TYPE_SUBSCRIPTION_STR = "sub";
	public static final String RBT_TYPE = "RBT_TYPE";
	public static final String SUBSCRIPTION_CLASS = "SUBSCRIPTION_CLASS";
	public static final String UI_CHARGE_CLASS = "UI_CHARGE_CLASS";
	
	private double amount;
	private int creditsAvailable = -1;
	private HashMap<String, String> metaInfo = new HashMap<String, String>();
	private String offerDesc;
	private String offerID;
	private String smOfferType;
	private String smRate;
	private String srvKey;
	private int type;
	private int validityDays;
	private int offerTypeValue;
	//RBT-14540: Added to support offer validity, offer renewal amout and offer renewal validity.
	private String offerValidity;
	private String offerRenewalAmount;
	private String offerRenewalValidity;
	
	public Offer() {
		
	}

	public Offer(com.onmobile.apps.ringbacktones.smClient.beans.Offer offer) {
		offerID = offer.getOfferID();
		offerDesc = offer.getOfferDescription();
		amount = offer.getAmount();
		srvKey = offer.getSrvKey();
		validityDays = offer.getValidityDays();
		initMetaInfo(offer.getMetaInfo());
		type = offer.getOfferType();
		smOfferType = offer.getSmOfferType();
		smRate = offer.getSmRate();
		creditsAvailable = offer.getCreditsAvailable();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Offer other = (Offer) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount)) {
			return false;
		}
		if (metaInfo == null) {
			if (other.metaInfo != null) {
				return false;
			}
		}
		else if (!metaInfo.equals(other.metaInfo)) {
			return false;
		}
		if (offerDesc == null) {
			if (other.offerDesc != null) {
				return false;
			}
		}
		else if (!offerDesc.equals(other.offerDesc)) {
			return false;
		}
		if (offerID == null) {
			if (other.offerID != null) {
				return false;
			}
		}
		else if (!offerID.equals(other.offerID)) {
			return false;
		}
		if (smOfferType == null) {
			if (other.smOfferType != null) {
				return false;
			}
		}
		else if (!smOfferType.equals(other.smOfferType)) {
			return false;
		}
		if (smRate == null) {
			if (other.smRate != null) {
				return false;
			}
		}
		else if (!smRate.equals(other.smRate)) {
			return false;
		}
		if (srvKey == null) {
			if (other.srvKey != null) {
				return false;
			}
		}
		else if (!srvKey.equals(other.srvKey)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (validityDays != other.validityDays) {
			return false;
		}
		return true;
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
	
	public String getMetaInfo(String mode) {
		return metaInfo.get(mode);
	}

	public String getOfferDesc() {
		return offerDesc;
	}

	public String getOfferID() {
		return offerID;
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

	public int getType() {
		return type;
	}

	public int getValidityDays() {
		return validityDays;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((metaInfo == null) ? 0 : metaInfo.hashCode());
		result = prime * result + ((offerDesc == null) ? 0 : offerDesc.hashCode());
		result = prime * result + ((offerID == null) ? 0 : offerID.hashCode());
		result = prime * result + ((smOfferType == null) ? 0 : smOfferType.hashCode());
		result = prime * result + ((smRate == null) ? 0 : smRate.hashCode());
		result = prime * result + ((srvKey == null) ? 0 : srvKey.hashCode());
		result = prime * result + type;
		result = prime * result + validityDays;
		return result;
	}

	private void initMetaInfo(String metaInfoStr) {
		metaInfo = new HashMap<String, String>();
		if (metaInfoStr == null)
			return;
		StringTokenizer stk = new StringTokenizer(metaInfoStr, ",");
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken();
			if (token.indexOf("=") == -1)
				continue;
			metaInfo.put(token.substring(0, token.indexOf("=")), token
					.substring(token.indexOf("=") + 1));
		}
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setCreditsAvailable(int creditsAvailable) {
		this.creditsAvailable = creditsAvailable;
	}

	public void setCreditsAvailable(String creditsAvailable) {
		try {
			this.creditsAvailable = Integer.parseInt(creditsAvailable);
		}
		catch(Exception e) {
			this.creditsAvailable = -1;
		}
	}

	public void setMetaInfo(String metaInfo) {
		initMetaInfo(metaInfo);
	}

	public void setOfferDesc(String offerDesc) {
		this.offerDesc = offerDesc;
	}
	
	public void setOfferID(String offerID) {
		this.offerID = offerID;
	}

	public void setSmOfferType(String smOfferType) {
		this.smOfferType = smOfferType;
	}

	public void setSmRate(String smRate) {
		this.smRate = smRate;
	}

	public void setSrvKey(String srvKey) {
		this.srvKey = srvKey;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setValidityDays(int validityDays) {
		this.validityDays = validityDays;
	}

	@Override
	public String toString() {
		return "Offer [offerID=" + offerID + ", offerDesc=" + offerDesc + ", amount=" + amount + ", srvKey=" + srvKey + ", validityDays="
				+ validityDays + ", metaInfo=" + metaInfo + ", type=" + type + ", smOfferType=" + smOfferType + ", smRate=" + smRate + ", offerValidity=" +offerValidity
				+ ", offerRenewalAmount+"+ offerRenewalAmount	+ ", offerRenewalValidity+"+ offerRenewalValidity +"]";
	}
}