package com.onmobile.apps.ringbacktones.service.dblayer.bean.primaryKey;

import java.util.Date;

public class SubscriberActivityCountPK
{
	private String subscriberID = null;
	private Date date = null;
	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID() {
		return subscriberID;
	}
	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriberActivityCountPK [subscriberID=")
				.append(subscriberID).append(", date=").append(date)
				.append("]");
		return builder.toString();
	}
}
