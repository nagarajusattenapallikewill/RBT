package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans;

import java.io.Serializable;
import java.util.Date;

public class RBTChargePerCall implements Serializable {

	private static final long serialVersionUID = 2649697066229773460L;
	private long id;
	private String callerId;
	private String calledId;
	private Date calledTime;
	private short callDuration = -1;// RBT-14123:TataDocomo Changes.

	public RBTChargePerCall() {
		super();
	}

	/**
	 * @param callerId
	 * @param calledId
	 * @param calledTime
	 */
	public RBTChargePerCall(String callerId, String calledId, Date calledTime) {
		super();
		this.callerId = callerId;
		this.calledId = calledId;
		this.calledTime = calledTime;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public String getCalledId() {
		return calledId;
	}

	public void setCalledId(String calledId) {
		this.calledId = calledId;
	}

	public Date getCalledTime() {
		return calledTime;
	}

	public void setCalledTime(Date calledTime) {
		this.calledTime = calledTime;
	}

	// RBT-14123:TataDocomo Changes.
	public short getCallDuration() {
		return callDuration;
	}
	// RBT-14123:TataDocomo Changes.
	public void setCallDuration(short callDuration) {
		this.callDuration = callDuration;
	}

	@Override
	public String toString() {
		return "calledId=" + calledId + ", callerId=" + callerId + ""
				+ ", calledTime=" + calledTime + ", id=" + id
				+ ", callDuration=" + callDuration;
	}
}
