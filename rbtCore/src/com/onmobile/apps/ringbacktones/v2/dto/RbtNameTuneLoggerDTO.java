package com.onmobile.apps.ringbacktones.v2.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class RbtNameTuneLoggerDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		NEW_REQUEST, TOBE_PROCESSED, COMPLETED, TOBE_RETRIED, FAILURE;
	}

	private String transactionId;

	private String clipId;

	private Timestamp createdDate;

	private String language;

	private String msisdn;

	private String nameTune;

	private int retryCount;

	private String status;

	public RbtNameTuneLoggerDTO() {
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getClipId() {
		return this.clipId;
	}

	public void setClipId(String clipId) {
		this.clipId = clipId;
	}

	public Timestamp getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMsisdn() {
		return this.msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getNameTune() {
		return this.nameTune;
	}

	public void setNameTune(String nameTune) {
		this.nameTune = nameTune;
	}

	public int getRetryCount() {
		return this.retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return " {\"transactionId\":\"" + transactionId + "\",\"clipId\":\"" + clipId + "\",\"createdDate\":\""
				+ createdDate + "\",\"language\":\"" + language + "\",\"msisdn\":\"" + msisdn + "\",\"nameTune\":\""
				+ nameTune + "\",\"retryCount\":\"" + retryCount + "\",\"status\":\"" + status + "\"}";
	}

	public String getNewRequestLogFormat() {
		return createdDate + "," + nameTune + "," + msisdn + "," + language + "," + transactionId;
	}

	public String getStatusLogFormat() {
		return createdDate + "," + nameTune + "," + msisdn + "," + language + "," + transactionId+","+clipId+","+status;
	}
}