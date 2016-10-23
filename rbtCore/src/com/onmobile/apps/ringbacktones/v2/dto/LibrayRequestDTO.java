package com.onmobile.apps.ringbacktones.v2.dto;

/**
 * 
 * @author md.alam
 *
 */

public class LibrayRequestDTO {

	private String toneId;
	private String type;
	private String categoryId;
	private String chargeClass;
	private boolean addToLibrary = true;
	private String subtype;
	private String wavFileName;
	
	
	public LibrayRequestDTO() {

	}
	
	public boolean getAddToLibrary() {
		return addToLibrary;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public void setAddToLibrary(boolean addToLibrary) {
		this.addToLibrary = addToLibrary;
	}

	public String getToneId() {
		return toneId;
	}

	public void setToneId(String toneId) {
		this.toneId = toneId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getChargeClass() {
		return chargeClass;
	}

	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}

	public String getWavFileName() {
		return wavFileName;
	}

	public void setWavFileName(String wavFileName) {
		this.wavFileName = wavFileName;
	}

	@Override
	public String toString() {
		return "LibrayRequestDTO [toneId=" + toneId + ", type=" + type
				+ ", categoryId=" + categoryId + ", chargeClass=" + chargeClass
				+ ", addToLibrary=" + addToLibrary + ", subtype=" + subtype
				+ ", wavFileName=" + wavFileName + "]";
	}

	
}
