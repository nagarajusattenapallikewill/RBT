package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.Date;

public class RBTMultiOpCopyRequest {
	private long copyId = 0;
	private int status = 0;
	private long copierMdn = 0;
	private String copierOperatorIds = null;
	private String targetContentIds = null;
	private Date requestTime = null;
	private long copieeMdn = 0;
	private String copieeOperatorIds = null;
	private String sourceContentId = null;
	private String copyType = null;
	private String keyPressed = null;
	private int contentResolveRetryCount = 0;
	private Date contentResolveTime = null;
	private String transferedOperatorIds = null;
	private Date requestTransferTime = null;
	private int transferRetryCount = 0;
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

	public long getCopierMdn() {
		return copierMdn;
	}

	public void setCopierMdn(long copierMdn) {
		this.copierMdn = copierMdn;
	}

	public String getCopierOperatorIds() {
		return copierOperatorIds;
	}

	public void setCopierOperatorIds(String copierOperatorIds) {
		this.copierOperatorIds = copierOperatorIds;
	}

	public String getTargetContentIds() {
		return targetContentIds;
	}

	public void setTargetContentIds(String targetContentIds) {
		this.targetContentIds = targetContentIds;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public long getCopieeMdn() {
		return copieeMdn;
	}

	public void setCopieeMdn(long copieeMdn) {
		this.copieeMdn = copieeMdn;
	}

	public String getCopieeOperatorIds() {
		return copieeOperatorIds;
	}

	public void setCopieeOperatorIds(String copieeOperatorIds) {
		this.copieeOperatorIds = copieeOperatorIds;
	}

	public String getSourceContentId() {
		return sourceContentId;
	}

	public void setSourceContentId(String sourceContentId) {
		this.sourceContentId = sourceContentId;
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

	public int getContentResolveRetryCount() {
		return contentResolveRetryCount;
	}

	public void setContentResolveRetryCount(int contentResolveRetryCount) {
		this.contentResolveRetryCount = contentResolveRetryCount;
	}

	public Date getContentResolveTime() {
		return contentResolveTime;
	}

	public void setContentResolveTime(Date contentResolveTime) {
		this.contentResolveTime = contentResolveTime;
	}

	public String getTransferedOperatorIds() {
		return transferedOperatorIds;
	}

	public void setTransferedOperatorIds(String transferedOperatorIds) {
		this.transferedOperatorIds = transferedOperatorIds;
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

	public String getSourceMode() {
		return sourceMode;
	}

	public void setSourceMode(String sourceMode) {
		this.sourceMode = sourceMode;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(" RBTMultiOpCopyRequest: [");
		sb.append("copyId: ").append(copyId);
		sb.append(", status: ").append(status);
		sb.append(", copierMdn: ").append(copierMdn);
		sb.append(", copierOperatorIds: ").append(copierOperatorIds);
		sb.append(", targetContentIds: ").append(targetContentIds);
		sb.append(", requestTime: ").append(requestTime);
		sb.append(", copieeMdn: ").append(copieeMdn);
		sb.append(", copieeOperatorIds: ").append(targetContentIds);
		sb.append(", sourceContentId: ").append(sourceContentId);
		sb.append(", copyType: ").append(copyType);
		sb.append(", keyPressed: ").append(keyPressed);
		sb.append(", contentResolveRetryCount: ").append(contentResolveRetryCount);
		sb.append(", contentResolveTime: ").append(contentResolveTime);
		sb.append(", transferedOperatorIds: ").append(transferedOperatorIds);
		sb.append(", requestTransferTime: ").append(requestTransferTime);
		sb.append(", transferRetryCount: ").append(transferRetryCount);
		sb.append(", sourceContentDetails: ").append(sourceContentDetails);
		sb.append(", sourceSongName: ").append(sourceSongName);
		sb.append(", sourcePromoCode: ").append(sourcePromoCode);
		sb.append(", sourceMode: ").append(sourceMode);
		sb.append(", info: ").append(info);
		sb.append(" ]");
		return sb.toString();
	}
	
	public String writeTxnLog(String status, String statusReason,
			boolean madeHit, Integer statusCode, String url) {
		StringBuilder sb = new StringBuilder();
		sb.append(" Txn status: ").append(status);
		sb.append(", reason: ").append(statusReason);
		sb.append(", isRequestedForContent: ").append(madeHit);
		sb.append(", httpResponseCode: ").append(statusCode);
		sb.append(", url: ").append(url);
		sb.append(", ");
		sb.append(toString());
		return sb.toString();
	}

}
