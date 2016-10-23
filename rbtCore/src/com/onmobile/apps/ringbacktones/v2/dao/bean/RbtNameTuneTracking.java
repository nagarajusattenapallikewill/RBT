package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the rbt_name_tune_tracking database table.
 * 
 */
@Entity
@Table(name="rbt_name_tune_tracking")
@NamedQuery(name="RbtNameTuneTracking.findAll", query="SELECT r FROM RbtNameTuneTracking r")
public class RbtNameTuneTracking implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		NEW_REQUEST,REQUEST_SENT,TOBE_PROCESSED,COMPLETED,TOBE_RETRIED,FAILURE;
	}
	
	@Id
	@Column(name="TRANSACTION_ID", unique=true, nullable=false, length=50)
	private String transactionId;

	@Column(name="CLIP_ID", length=20)
	private String clipId;

	@Column(name="CREATED_DATE")
	private Timestamp createdDate;

	@Column(length=20)
	private String language;

	@Column(name="MODIFIED_DATE")
	private Timestamp modifiedDate;

	@Column(nullable=false, length=20)
	private String msisdn;

	@Column(name="NAME_TUNE", length=50)
	private String nameTune;

	@Column(name="RETRY_COUNT")
	private int retryCount;

	@Column(length=50)
	private String status;

	public RbtNameTuneTracking() {
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

	public Timestamp getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
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
				+ createdDate + "\",\"language\":\"" + language + "\",\"modifiedDate\":\"" + modifiedDate
				+ "\",\"msisdn\":\"" + msisdn + "\",\"nameTune\":\"" + nameTune + "\",\"retryCount\":\"" + retryCount
				+ "\",\"status\":\"" + status + "\"}";
	}

}