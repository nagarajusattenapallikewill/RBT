package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.io.Serializable;

public class CurrentPlayingSongBean implements Serializable {

	private static final long serialVersionUID = 7800674886880115176L;
	private String callerId;
	private String calledId;
	private String wavFileName;
	private int categoryId;

	public CurrentPlayingSongBean(String callerId, String calledId,
			String wavFile, int categoryId) {
		this.callerId = callerId;
		this.calledId = calledId;
		this.wavFileName = wavFile;
		this.categoryId = categoryId;
	}

	public CurrentPlayingSongBean() {
	}
	
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
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
	public String getWavFileName() {
		return wavFileName;
	}
	public void setWavFileName(String wavFileName) {
		this.wavFileName = wavFileName;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CurrentPlayingSongBean [callerId=");
		builder.append(callerId);
		builder.append(", calledId=");
		builder.append(calledId);
		builder.append(", wavFileName=");
		builder.append(wavFileName);
		builder.append("]");
		return builder.toString();
}
}
