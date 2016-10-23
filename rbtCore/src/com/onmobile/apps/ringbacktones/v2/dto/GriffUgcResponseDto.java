package com.onmobile.apps.ringbacktones.v2.dto;

public class GriffUgcResponseDto {

	private String status;
	private String code;
	private String subCode;
	private String summary;
	private String description;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getSubCode() {
		return subCode;
	}
	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "GriffUgcResponseDto [status=" + status + ", code=" + code
				+ ", subCode=" + subCode + ", summary=" + summary
				+ ", description=" + description + "]";
	}
	
	
}
