package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="RBT_UDP",
uniqueConstraints=@UniqueConstraint(columnNames={"SUBSCRIBER_ID","UDP_NAME"}))
public class UDPBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="UDP_ID")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int udpId;

	@Column(name="SUBSCRIBER_ID")
	private String subscriberId;

	@Column(name="UDP_NAME")
	private String udpName;

	@Column(name="EXRA_INFO",length=200)
	private String extraInfo;

	@Column(name="MODE", updatable=false)
	private String mode;

	@Column(name="CREATION_TIME",columnDefinition="TIMESTAMP NULL DEFAULT NULL",updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime;

	@Column(name="UPDATION_TIME",columnDefinition="TIMESTAMP NULL DEFAULT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updationTime;
	
	@Column(name="SELECTION_STATUS")
	private boolean isSelActivated;
	
	public int getUdpId() {
		return udpId;
	}
	public void setUdpId(int udpId) {
		this.udpId = udpId;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getUdpName() {
		return udpName;
	}
	public void setUdpName(String udpName) {
		this.udpName = udpName;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public Date getUpdationTime() {
		return updationTime;
	}
	public void setUpdationTime(Date updationTime) {
		this.updationTime = updationTime;
	}
	public boolean isSelActivated() {
		return isSelActivated;
	}
	public void setSelActivated(boolean isSelActivated) {
		this.isSelActivated = isSelActivated;
	}
	/*@Override
	public String toString() {
		return "UDPBean [udpId=" + udpId + ", subscriberId=" + subscriberId
				+ ", udpName=" + udpName + ", extraInfo=" + extraInfo
				+ ", mode=" + mode + ", creationTime=" + creationTime
				+ ", updationTime=" + updationTime + ", isSelActivated="
				+ isSelActivated + ", udpContentMap=" + udpContentMap + "]";
	}*/
	
}
