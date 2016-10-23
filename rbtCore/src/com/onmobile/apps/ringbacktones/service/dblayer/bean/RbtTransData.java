package com.onmobile.apps.ringbacktones.service.dblayer.bean;

import java.util.Date;

public class RbtTransData
{
	private String subscriberId;
	private String transId;
	private Date transDate;
	private String type;
	private String accessCount;
	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}
	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	/**
	 * @return the transId
	 */
	public String getTransId() {
		return transId;
	}
	/**
	 * @param transId the transId to set
	 */
	public void setTransId(String transId) {
		this.transId = transId;
	}
	/**
	 * @return the transDate
	 */
	public Date getTransDate() {
		return transDate;
	}
	/**
	 * @param transDate the transDate to set
	 */
	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the accessCount
	 */
	public String getAccessCount() {
		return accessCount;
	}
	/**
	 * @param accessCount the accessCount to set
	 */
	public void setAccessCount(String accessCount) {
		this.accessCount = accessCount;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RbtTransData [subscriberId=").append(subscriberId)
				.append(", transId=").append(transId).append(", transDate=")
				.append(transDate).append(", type=").append(type)
				.append(", accessCount=").append(accessCount).append("]");
		return builder.toString();
	}
}
