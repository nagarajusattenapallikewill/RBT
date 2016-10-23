package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans;

import java.io.Serializable;
import java.util.Date;

public class RBTChargePerCallTxn extends RBTChargePerCall implements
		Serializable {
	private static final long serialVersionUID = 509085074839192964L;
	private String wavFile;

	public RBTChargePerCallTxn() {
		super();
	}

	/**
	 * @param callerId
	 * @param calledId
	 * @param calledTime
	 * @param wavFile
	 */
	public RBTChargePerCallTxn(String callerId, String calledId,
			Date calledTime, String wavFile) {
		super(callerId, calledId, calledTime);
		this.wavFile = wavFile;
	}

	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}
	// RBT-14123:TataDocomo Changes.
	@Override
	public String toString() {
		return "RBTChargePerCallTxn [callerId: " + super.getCallerId()
				+ ", calledId: " + super.getCalledId() + ", toneId: " + wavFile
				+ ", calledTime: " + super.getCalledTime() + ", callDuration: "
				+ super.getCallDuration() + "]";
	}
	
	// RBT-14123:TataDocomo Changes.
	public String getLogString(boolean accepted, String reason,
			int callDuration, boolean chargingInitiated) {
		StringBuilder sb = new StringBuilder(reason);
		if (sb.length() > 0) {
			sb.insert(0, ":");
		}
		String logString = (accepted ? "Accepted" : "Rejected") + sb.toString()
				+ ",[callerId: " + super.getCallerId() + "|calledId: "
				+ super.getCalledId() + "|wavFile: " + wavFile
				+ "|calledTime: " + super.getCalledTime();
		if (callDuration > -2) {
			logString = logString + "|duration: " + super.getCallDuration()
					+ "|charging initiated: " + chargingInitiated;
		}
		return (logString) + "]";
	}
	
	public String getLogString(boolean accepted) {
		return (accepted ? "Accepted" : "Rejected") + ",[callerId: " + super.getCallerId() + "|calledId: "
				+ super.getCalledId() + "|wavFile: " + wavFile + "|calledTime: "
				+ super.getCalledTime() + "]";
	}
}