package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.io.Serializable;

public class CurrentPlayingSongWSResponseBean implements Serializable {

	private static final long serialVersionUID = 7800674886880115176L;
	private String wavFileName;
	private int categoryId;
	private String responseStr;
	private String responseCode;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseStr() {
		return responseStr;
	}

	public void setResponseStr(String responseStr) {
		this.responseStr = responseStr;
	}

	public CurrentPlayingSongWSResponseBean(String callerId, String calledId,
			String wavFile, int categoryId) {
		this.wavFileName = wavFile;
		this.categoryId = categoryId;
	}

	public CurrentPlayingSongWSResponseBean() {
	}
	
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
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
		builder.append("CurrentPlayingSongBean [categoryId=");
		builder.append(categoryId);
		builder.append(", wavFileName=");
		builder.append(wavFileName);
		builder.append("]");
		return builder.toString();
}
}
