package com.onmobile.apps.ringbacktones.provisioning.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "RBT_IDEA_RTO_INFO")
public class RBTRto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ID")
	@GeneratedValue(strategy=GenerationType.AUTO) 
	private String id;

	@Column(name = "SUBSCRIBER_ID")
	private String subscriberId;

	@Column(name = "URL")
	private String url;

	@Column(name = "SYSTEM")
	private String system;

	@Column(name = "RETRY")
	private int retry;

	@Column(name = "RETRY_TIME", columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date retryTime;
	
	@Column(name = "SDPOMTXNID")
	private String sdpomtxnid;

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public Date getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(Date retryTime) {
		this.retryTime = retryTime;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSdpomtxnid() {
		return sdpomtxnid;
	}

	public void setSdpomtxnid(String sdpomtxnid) {
		this.sdpomtxnid = sdpomtxnid;
	}

	@Override
	public String toString() {
		return "RBTRto [id=" + id + ", subscriberId=" + subscriberId + ", url="
				+ url + ", system=" + system + ", retry=" + retry
				+ ", retryTime=" + retryTime + ", sdpomtxnid=" + sdpomtxnid
				+ "]";
	}

}
