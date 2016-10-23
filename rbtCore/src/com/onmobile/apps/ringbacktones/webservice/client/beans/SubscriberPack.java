package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

public class SubscriberPack {
	
	private String intRefId;
	private String packChargeClass;
	private Date creationTime;
	private String packModeInfo;
	private String packMode;
	private String cosId;
	private String cosType;
	private String deactivateMode;
	private String deactivateModeInfo;
	private Date deactivateDate;
	private Date lastChargingDate;
	private String status;
	private String extraInfo;
	private int numMaxSelections;
	private Date nextChargingDate;
	private String amountCharged;
	private String lastTransactionType;
	
	public SubscriberPack() {
		
	}
	
	public SubscriberPack(String cosId, String packMode, String status, String packChargeClass, Date creationTime, String extraInfo, String intRefId, String deactivateMode,
			String deactivateModeInfo, Date deactivateDate, Date lastChargingDate, String cosType, String packModeInfo) {
		this.cosId = cosId;
		this.packMode = packMode;
		this.status = status;
		this.packChargeClass = packChargeClass;
		this.extraInfo = extraInfo;
		this.creationTime = creationTime;
		this.intRefId = intRefId;
		this.deactivateMode = deactivateMode;
		this.deactivateModeInfo = deactivateModeInfo;
		this.deactivateDate = deactivateDate;
		this.lastChargingDate = lastChargingDate;
		this.cosType = cosType;
		this.packModeInfo = packModeInfo;
	}

	public String getCosId() {
		return cosId;
	}

	public void setCosId(String cosId) {
		this.cosId = cosId;
	}

	public String getPackMode() {
		return packMode;
	}

	public void setPackMode(String packMode) {
		this.packMode = packMode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPackChargeClass() {
		return packChargeClass;
	}

	public void setPackChargeClass(String packChargeClass) {
		this.packChargeClass = packChargeClass;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	
	public String getIntRefId() {
		return intRefId;
	}

	public void setIntRefId(String intRefId) {
		this.intRefId = intRefId;
	}

	public String getDeactivateMode() {
		return deactivateMode;
	}

	public void setDeactivateMode(String deactivateMode) {
		this.deactivateMode = deactivateMode;
	}

	public String getDeactivateModeInfo() {
		return deactivateModeInfo;
	}

	public void setDeactivateModeInfo(String deactivateModeInfo) {
		this.deactivateModeInfo = deactivateModeInfo;
	}

	public Date getDeactivateDate() {
		return deactivateDate;
	}

	public void setDeactivateDate(Date deactivateDate) {
		this.deactivateDate = deactivateDate;
	}

	public Date getLastChargingDate() {
		return lastChargingDate;
	}

	public void setLastChargingDate(Date lastChargingDate) {
		this.lastChargingDate = lastChargingDate;
	}

	public String getCosType() {
		return cosType;
	}

	public void setCosType(String cosType) {
		this.cosType = cosType;
	}

	public String getPackModeInfo() {
		return packModeInfo;
	}

	public void setPackModeInfo(String packModeInfo) {
		this.packModeInfo = packModeInfo;
	}

	/**
	 * @return the numMaxSelections
	 */
	public int getNumMaxSelections() {
		return numMaxSelections;
	}

	/**
	 * @param numMaxSelections the numMaxSelections to set
	 */
	public void setNumMaxSelections(int numMaxSelections) {
		this.numMaxSelections = numMaxSelections;
	}
	
	public Date getNextChargingDate() {
		return nextChargingDate;
	}

	public void setNextChargingDate(Date nextChargingDate) {
		this.nextChargingDate = nextChargingDate;
	}

	public String getAmountCharged() {
		return amountCharged;
	}

	public void setAmountCharged(String amountCharged) {
		this.amountCharged = amountCharged;
	}
	
	public String getLastTransactionType() {
		return lastTransactionType;
	}

	public void setLastTransactionType(String lastTransactionType) {
		this.lastTransactionType = lastTransactionType;
	}


	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("cosId: " + cosId + ", ");
		builder.append("packMode: " + packMode + ", ");
		builder.append("status: " + status + ", ");
		builder.append("packChargeClass: " + packChargeClass + ", ");
		builder.append("creationTime: " + creationTime + ", ");
		builder.append("extraInfo: " + extraInfo + ", ");
		builder.append("intRefId: " + intRefId + ", ");
		builder.append("deactivateMode: " + deactivateMode + ", ");
		builder.append("deactivateModeInfo: " + deactivateModeInfo + ", ");
		builder.append("deactivateDate: " + deactivateDate + ", ");
		builder.append("lastChargingDate: " + lastChargingDate + ", ");
		builder.append("cosType: " + cosType + ", ");
		builder.append("packModeInfo: " + packModeInfo + " , ");
		builder.append("numMaxSelections: " + numMaxSelections + " , ");
		builder.append("nextChargingDate: " + nextChargingDate + " , ");
		builder.append("amountCharged: " + amountCharged + " , ");
		builder.append("transactionType: " + lastTransactionType);
		return builder.toString();
	}

}
