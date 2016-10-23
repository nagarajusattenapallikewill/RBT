package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans;

import java.io.Serializable;
import java.util.Date;

public class RBTCurrentPlayingSongBean extends RBTChargePerCall implements
		Serializable {

	private static final long serialVersionUID = 509085074839192975L;
	private String wavFile;

	public RBTCurrentPlayingSongBean() {
		super();
	}

	public RBTCurrentPlayingSongBean(String callerId, String calledId,
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

	@Override
	public String toString() {
		return "RBTRequestSongInfoTxn[callerId: " + super.getCallerId()
				+ ", calledId: " + super.getCalledId() + ", toneId: " + wavFile
				+ ", calledTime: " + super.getCalledTime() + "]";
	}

	public String getLogString(boolean accepted, String reason) {
		StringBuilder sb = new StringBuilder(reason);
		if (sb.length() > 0) {
			sb.insert(0, ":");
		}
		return (accepted ? "Accepted" : "Rejected") + sb.toString()
				+ ",[callerId: " + super.getCallerId() + "|calledId: "
				+ super.getCalledId() + "|wavFile: " + wavFile
				+ "|calledTime: " + super.getCalledTime() + "]";
	}

	public String getLogString(boolean accepted) {
		return (accepted ? "Accepted" : "Rejected") + ",[callerId: "
				+ super.getCallerId() + "|calledId: " + super.getCalledId()
				+ "|wavFile: " + wavFile + "|calledTime: "
				+ super.getCalledTime() + "]";
	}
}
