package com.onmobile.apps.ringbacktones.daemons.genericftp.beans;

/**
 * @author sridhar.sindiri
 *
 */
public class CampaignRequest
{
	private String msisdn;
	private String subClass;
	private String activationMode;
	private String contentID;
	private String chargeClass;
	private String songMode;
	private String retailerID;

	/**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the subClass
	 */
	public String getSubClass() {
		return subClass;
	}

	/**
	 * @param subClass the subClass to set
	 */
	public void setSubClass(String subClass) {
		this.subClass = subClass;
	}

	/**
	 * @return the activationMode
	 */
	public String getActivationMode() {
		return activationMode;
	}

	/**
	 * @param activationMode the activationMode to set
	 */
	public void setActivationMode(String activationMode) {
		this.activationMode = activationMode;
	}

	/**
	 * @return the contentID
	 */
	public String getContentID() {
		return contentID;
	}

	/**
	 * @param contentID the contentID to set
	 */
	public void setContentID(String contentID) {
		this.contentID = contentID;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass() {
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the songMode
	 */
	public String getSongMode() {
		return songMode;
	}

	/**
	 * @param songMode the songMode to set
	 */
	public void setSongMode(String songMode) {
		this.songMode = songMode;
	}

	/**
	 * @return the retailerID
	 */
	public String getRetailerID() {
		return retailerID;
	}

	/**
	 * @param retailerID the retailerID to set
	 */
	public void setRetailerID(String retailerID) {
		this.retailerID = retailerID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CampaignRequest [msisdn=");
		builder.append(msisdn);
		builder.append(", subClass=");
		builder.append(subClass);
		builder.append(", activationMode=");
		builder.append(activationMode);
		builder.append(", contentID=");
		builder.append(contentID);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", songMode=");
		builder.append(songMode);
		builder.append(", retailerID=");
		builder.append(retailerID);
		builder.append("]");
		return builder.toString();
	}
}
