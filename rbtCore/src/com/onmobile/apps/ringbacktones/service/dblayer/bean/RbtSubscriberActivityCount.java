package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import com.onmobile.apps.ringbacktones.service.dblayer.bean.primaryKey.SubscriberActivityCountPK;

public class RbtSubscriberActivityCount
{
	private SubscriberActivityCountPK subscriberActivityCountPK = null;
	private String counts = null;
	
	/**
	 * @return the counts
	 */
	public String getCounts() {
		return counts;
	}
	/**
	 * @return the subscriberActivityCountPK
	 */
	public SubscriberActivityCountPK getSubscriberActivityCountPK() {
		return subscriberActivityCountPK;
	}
	/**
	 * @param subscriberActivityCountPK the subscriberActivityCountPK to set
	 */
	public void setSubscriberActivityCountPK(
			SubscriberActivityCountPK subscriberActivityCountPK) {
		this.subscriberActivityCountPK = subscriberActivityCountPK;
	}
	/**
	 * @param counts the counts to set
	 */
	public void setCounts(String counts) {
		this.counts = counts;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtSubscriberActivityCount [subscriberActivityCountPK=")
				.append(subscriberActivityCountPK).append(", counts=")
				.append(counts).append("]");
		return builder.toString();
	}
}
