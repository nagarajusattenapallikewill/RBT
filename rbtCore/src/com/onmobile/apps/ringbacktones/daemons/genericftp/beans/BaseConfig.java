package com.onmobile.apps.ringbacktones.daemons.genericftp.beans;

/**
 * @author sridhar.sindiri
 *
 */
public class BaseConfig
{
	private String subscriptionClass;
	private String activatedBy;
	private String cosID;
	private String accept;

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass() {
		return subscriptionClass;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass) {
		this.subscriptionClass = subscriptionClass;
	}

	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy() {
		return activatedBy;
	}

	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}

	/**
	 * @return the cosID
	 */
	public String getCosID() {
		return cosID;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(String cosID) {
		this.cosID = cosID;
	}

	/**
	 * @return the accept
	 */
	public String getAccept() {
		return accept;
	}

	/**
	 * @param accept the accept to set
	 */
	public void setAccept(String accept) {
		this.accept = accept;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("BaseConfig [subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append(", activatedBy = ");
		builder.append(activatedBy);
		builder.append(", cosID = ");
		builder.append(cosID);
		builder.append(", accept = ");
		builder.append(accept);
		builder.append("] ");

		return builder.toString();
	}
}
