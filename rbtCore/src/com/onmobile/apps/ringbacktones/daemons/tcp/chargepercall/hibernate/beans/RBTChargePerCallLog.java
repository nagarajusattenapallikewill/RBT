package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans;

import java.io.Serializable;
import java.util.Date;

public class RBTChargePerCallLog extends RBTChargePerCall implements Serializable {

	private static final long serialVersionUID = -2097756130909540671L;

	private int noOfCallsMade = 1;
	
	public RBTChargePerCallLog() {
		super();
	}

	/**
	 * @param callerId
	 * @param calledId
	 * @param calledTime
	 */
	public RBTChargePerCallLog(String callerId, String calledId, Date calledTime) {
		super(callerId, calledId, calledTime);
	}


	public int getNoOfCallsMade() {
		return noOfCallsMade;
	}

	public void setNoOfCallsMade(int noOfCallsMade) {
		this.noOfCallsMade = noOfCallsMade;
	}

	@Override
	public String toString() {
		return "RBTChargePerCallLog [noOfCallsMade=" + noOfCallsMade
				+ ", " + super.toString() + "]";
	}

}
