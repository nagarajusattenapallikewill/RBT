/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 * 
 */
public class Transaction {
	private String type = null;
	private String amount = null;
	private String mode = null;
	private Date date = null;
	private String refundAmount = null;
	private String agentId = null;
	private Date nextChargeDate = null;
	private String reason = null;
	private String service = null;
	private String validity = null;
	private String songName = null;
	private Long protocolNum = null;
	private String categoryId = null;
	private String clipId = null;
	private boolean shuffleCategory = false;
	private int cosId;

	/**
	 * 
	 */
	public Transaction() {

	}

	/**
	 * @param type
	 * @param amount
	 * @param mode
	 * @param date
	 */
	public Transaction(String type, String amount, String mode, Date date,
			String refundAmount) {
		this.type = type;
		this.amount = amount;
		this.mode = mode;
		this.date = date;
		this.refundAmount = refundAmount;
	}

	public Transaction(String type, String amount, String mode, Date date,
			String refundAmount, String agentId, Date nextChargeDate,
			String reason, String service) {
		this.type = type;
		this.amount = amount;
		this.mode = mode;
		this.date = date;
		this.refundAmount = refundAmount;
		this.agentId = agentId;
		this.nextChargeDate = nextChargeDate;
		this.reason = reason;
		this.service = service;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Date getNextChargeDate() {
		return nextChargeDate;
	}

	public void setNextChargeDate(Date nextChargeDate) {
		this.nextChargeDate = nextChargeDate;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	public String getValidity() {
		return validity;
	}

	public void setValidity(String validity) {
		this.validity = validity;
	}
	
	public int getCosId() {
		return cosId; 
	}

	public void setCosId(int cosId) {
		this.cosId = cosId;  
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((refundAmount == null) ? 0 : refundAmount.hashCode());
		result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
		result = prime * result
				+ ((nextChargeDate == null) ? 0 : nextChargeDate.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result
				+ ((validity == null) ? 0 : validity.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Transaction))
			return false;
		Transaction other = (Transaction) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (refundAmount == null) {
			if (other.refundAmount != null)
				return false;
		} else if (!refundAmount.equals(other.refundAmount))
			return false;
		if (agentId == null) {
			if (other.agentId != null)
				return false;
		} else if (!agentId.equals(other.agentId))
			return false;
		if (nextChargeDate == null) {
			if (other.nextChargeDate != null)
				return false;
		} else if (!nextChargeDate.equals(other.nextChargeDate))
			return false;

		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;

		if (validity == null) {
			if (other.validity != null)
				return false;
		} else if (!validity.equals(other.validity))
			return false;

		return true;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getAgentId() {
		return agentId;
	}

	/**
	 * @param agentId
	 *            the agentId to set
	 */
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public Long getProtocolNum() {
		return protocolNum;
	}

	public void setProtocolNum(Long protocolNum) {
		this.protocolNum = protocolNum;
	}

	public String getClipId() {
		return clipId;
	}

	public void setClipId(String clipId) {
		this.clipId = clipId;
	}
	
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public boolean isShuffleCategory() {
		return shuffleCategory;
	}

	public void setShuffleCategory(boolean shuffleCategory) {
		this.shuffleCategory = shuffleCategory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction[amount = ");
		builder.append(amount);
		builder.append(", date = ");
		builder.append(date);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", refundAmount = ");
		builder.append(refundAmount);
		builder.append(", agentId = ");
		builder.append(agentId);
		builder.append(", nextChargeDate = ");
		builder.append(nextChargeDate);
		builder.append(", reason = ");
		builder.append(reason);
		builder.append(", service = ");
		builder.append(service);
		builder.append(", validity = ");
		builder.append(validity);
		builder.append(", songName = ");
		builder.append(songName);
		builder.append("]");
		return builder.toString();
	}

}
