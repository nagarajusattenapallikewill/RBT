package com.onmobile.apps.ringbacktones.daemons.interoperator.bean;

import java.util.Date;

public class InterOperatorCopyRequestBean
{
	private long copyId = 0;
	private int status = 0;
	private long copierMdn = 0;
	private int copierOperatorId = 0;
	private String targetContentId = null;
	private Date requestTime = null;
	private long copieeMdn = 0;
	private int copieeOperatorId = 0;
	private String sourceContentId = null;
	private String copyType = null;
	private String keyPressed = null;
	private Date mnpRequestTime = null;
	private Date mnpResponseTime = null;
	private Date contentResolveTime = null;
	private Date requestTransferTime = null;
	private int transferRetryCount = 0;
	private String mnpRequestType = null;
	private String mnpResponseType = null;
	private String sourceContentDetails = null;
	private String sourceSongName = null;
	private String sourcePromoCode = null;
	private String sourceMode = null;
	private String info = null;
	
	public long getCopyId() {
		return copyId;
	}
	public void setCopyId(long copyId) {
		this.copyId = copyId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}
	public long getCopierMdn() {
		return copierMdn;
	}
	public void setCopierMdn(long copierMdn) {
		this.copierMdn = copierMdn;
	}
	public int getCopierOperatorId() {
		return copierOperatorId;
	}
	public void setCopierOperatorId(int copierOperatorId) {
		this.copierOperatorId = copierOperatorId;
	}
	public String getSourceContentId() {
		return sourceContentId;
	}
	public void setSourceContentId(String sourceContentId) {
		this.sourceContentId = sourceContentId;
	}
	public String getTargetContentId() {
		return targetContentId;
	}
	public void setTargetContentId(String targetContentId) {
		this.targetContentId = targetContentId;
	}
	public long getCopieeMdn() {
		return copieeMdn;
	}
	public void setCopieeMdn(long copieeMdn) {
		this.copieeMdn = copieeMdn;
	}
	public int getCopieeOperatorId() {
		return copieeOperatorId;
	}
	public void setCopieeOperatorId(int copieeOperatorId) {
		this.copieeOperatorId = copieeOperatorId;
	}
	public String getCopyType() {
		return copyType;
	}
	public void setCopyType(String copyType) {
		this.copyType = copyType;
	}
	public String getKeyPressed() {
		return keyPressed;
	}
	public void setKeyPressed(String keyPressed) {
		this.keyPressed = keyPressed;
	}
	public Date getMnpRequestTime() {
		return mnpRequestTime;
	}
	public void setMnpRequestTime(Date mnpRequestTime) {
		this.mnpRequestTime = mnpRequestTime;
	}
	public Date getMnpResponseTime() {
		return mnpResponseTime;
	}
	public void setMnpResponseTime(Date mnpResponseTime) {
		this.mnpResponseTime = mnpResponseTime;
	}
	public Date getContentResolveTime() {
		return contentResolveTime;
	}
	public void setContentResolveTime(Date contentResolveTime) {
		this.contentResolveTime = contentResolveTime;
	}
	public Date getRequestTransferTime() {
		return requestTransferTime;
	}
	public void setRequestTransferTime(Date requestTransferTime) {
		this.requestTransferTime = requestTransferTime;
	}
	public int getTransferRetryCount() {
		return transferRetryCount;
	}
	public void setTransferRetryCount(int transferRetryCount) {
		this.transferRetryCount = transferRetryCount;
	}
	public String getMnpRequestType() {
		return mnpRequestType;
	}
	public void setMnpRequestType(String mnpRequestType) {
		this.mnpRequestType = mnpRequestType;
	}
	public String getMnpResponseType() {
		return mnpResponseType;
	}
	public void setMnpResponseType(String mnpResponseType) {
		this.mnpResponseType = mnpResponseType;
	}
	public String getSourceContentDetails() {
		return sourceContentDetails;
	}
	public void setSourceContentDetails(String sourceContentDetails) {
		this.sourceContentDetails = sourceContentDetails;
	}
	public String getSourceSongName() {
		return sourceSongName;
	}
	public void setSourceSongName(String sourceSongName) {
		this.sourceSongName = sourceSongName;
	}
	public String getSourcePromoCode() {
		return sourcePromoCode;
	}
	public void setSourcePromoCode(String sourcePromoCode) {
		this.sourcePromoCode = sourcePromoCode;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getSourceMode() {
		return sourceMode;
	}
	public void setSourceMode(String sourceMode) {
		this.sourceMode = sourceMode;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InterOperatorCopyRequestBean [copyId=").append(copyId)
				.append(", status=").append(status).append(", copierMdn=")
				.append(copierMdn).append(", copierOperatorId=").append(
						copierOperatorId).append(", targetContentId=").append(
						targetContentId).append(", requestTime=").append(
						requestTime).append(", copieeMdn=").append(copieeMdn)
				.append(", copieeOperatorId=").append(copieeOperatorId).append(
						", sourceContentId=").append(sourceContentId).append(
						", copyType=").append(copyType).append(", keyPressed=")
				.append(keyPressed).append(", mnpRequestTime=").append(
						mnpRequestTime).append(", mnpResponseTime=").append(
						mnpResponseTime).append(", transferRetryCount=")
				.append(transferRetryCount).append(", contentResolveTime=")
				.append(contentResolveTime).append(", requestTransferTime=")
				.append(requestTransferTime).append(", mnpRequestType=")
				.append(mnpRequestType).append(", mnpResponseType=").append(
						mnpResponseType).append(", sourceContentDetails=")
				.append(sourceContentDetails).append(", sourcePromoCode=")
				.append(sourcePromoCode).append(", sourceSongName=").append(
						sourceSongName).append(", sourceMode=").append(
						sourceMode).append(", info=").append(info).append("]");
		return builder.toString();
	}
	
}
