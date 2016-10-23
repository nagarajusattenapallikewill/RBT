package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean;

import java.util.Date;

public class DoubleConfirmationRequestBean {
 
	private String transId = null;
	private String subscriberID = null;
	private String callerID = null;
    private int categoryID = -1;
    private String mode = null;
    private Date startTime = null;
    private Date endTime = null;
    private int status = -1;
    private String classType = null;
    private String subscriptionClass = null;
    private Integer cosId = null;
    private Integer packCosID = null;
    private Integer clipID = null;
    private String selInterval = null;
    private int toTime = -1;
    private int fromTime = -1;
    private String selectionInfo = null;
    private int selType = -1;
    private String inLoop = null;
    private String purchaseType = null;
    private String useUIChargeClass = null;
    private String categoryType = null;
    private String profileHrs = null;
    private String prepaidYes = null;
    private String feedType = null;
    private String wavFileName = null;
    private int rbtType = -1;
    private String circleId = null;
    private String language = null;
    private Date requestTime = null;
    private String extraInfo = null;
    private String requestType = null;
    private int consentStatus = -1;
    private Integer inlineFlag = null;
    
	public String getTransId() {
		return transId;
	}
	public void setTransId(String transId) {
		this.transId = transId;
	}
	public String getSubscriberID() {
		return subscriberID;
	}
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getCallerID() {
		return callerID;
	}
	public void setCallerID(String callerID) {
		this.callerID = callerID;
	}
	public int getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	public String getSubscriptionClass() {
		return subscriptionClass;
	}
	public void setSubscriptionClass(String subscriptionClass) {
		this.subscriptionClass = subscriptionClass;
	}
	public String getSelInterval() {
		return selInterval;
	}
	public void setSelInterval(String selInterval) {
		this.selInterval = selInterval;
	}
	public int getToTime() {
		return toTime;
	}
	public void setToTime(int toTime) {
		this.toTime = toTime;
	}
	public int getFromTime() {
		return fromTime;
	}
	public void setFromTime(int fromTime) {
		this.fromTime = fromTime;
	}
	public String getSelectionInfo() {
		return selectionInfo;
	}
	public void setSelectionInfo(String selectionInfo) {
		this.selectionInfo = selectionInfo;
	}
	public int getSelType() {
		return selType;
	}
	public void setSelType(int selType) {
		this.selType = selType;
	}
	public String getInLoop() {
		return inLoop;
	}
	public void setInLoop(String inLoop) {
		this.inLoop = inLoop;
	}
	public String getPurchaseType() {
		return purchaseType;
	}
	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}
	public String getUseUIChargeClass() {
		return useUIChargeClass;
	}
	public void setUseUIChargeClass(String useUIChargeClass) {
		this.useUIChargeClass = useUIChargeClass;
	}
	public String getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	public String getProfileHrs() {
		return profileHrs;
	}
	public void setProfileHrs(String profileHrs) {
		this.profileHrs = profileHrs;
	}
	public String getPrepaidYes() {
		return prepaidYes;
	}
	public void setPrepaidYes(String prepaidYes) {
		this.prepaidYes = prepaidYes;
	}
	public String getFeedType() {
		return feedType;
	}
	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}
	public String getWavFileName() {
		return wavFileName;
	}
	public void setWavFileName(String wavFileName) {
		this.wavFileName = wavFileName;
	}
	public int getRbtType() {
		return rbtType;
	}
	public void setRbtType(int rbtType) {
		this.rbtType = rbtType;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public int getConsentStatus() {
		return consentStatus;
	}
	public void setConsentStatus(int consentStatus) {
		this.consentStatus = consentStatus;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DoubleConfirmationRequestBean [transId=");
		builder.append(transId);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", callerID=");
		builder.append(callerID);
		builder.append(", categoryID=");
		builder.append(categoryID);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", status=");
		builder.append(status);
		builder.append(", classType=");
		builder.append(classType);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", cosId=");
		builder.append(cosId);
		builder.append(", packCosID=");
		builder.append(packCosID);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", selInterval=");
		builder.append(selInterval);
		builder.append(", toTime=");
		builder.append(toTime);
		builder.append(", fromTime=");
		builder.append(fromTime);
		builder.append(", selectionInfo=");
		builder.append(selectionInfo);
		builder.append(", selType=");
		builder.append(selType);
		builder.append(", inLoop=");
		builder.append(inLoop);
		builder.append(", purchaseType=");
		builder.append(purchaseType);
		builder.append(", useUIChargeClass=");
		builder.append(useUIChargeClass);
		builder.append(", categoryType=");
		builder.append(categoryType);
		builder.append(", profileHrs=");
		builder.append(profileHrs);
		builder.append(", prepaidYes=");
		builder.append(prepaidYes);
		builder.append(", feedType=");
		builder.append(feedType);
		builder.append(", wavFileName=");
		builder.append(wavFileName);
		builder.append(", rbtType=");
		builder.append(rbtType);
		builder.append(", circleId=");
		builder.append(circleId);
		builder.append(", language=");
		builder.append(language);
		builder.append(", requestTime=");
		builder.append(requestTime);
		builder.append(", extraInfo=");
		builder.append(extraInfo);
		builder.append(", requestType=");
		builder.append(requestType);
		builder.append(", consentStatus=");
		builder.append(consentStatus);
		builder.append(", inlineFlag=");
		builder.append(inlineFlag);
		builder.append("]");
		return builder.toString();
	}
	public Integer getCosId() {
		return cosId;
	}
	public void setCosId(Integer cosId) {
		this.cosId = cosId;
	}
	public Integer getPackCosID() {
		return packCosID;
	}
	public void setPackCosID(Integer packCosID) {
		this.packCosID = packCosID;
	}
	public Integer getClipID() {
		return clipID;
	}
	public void setClipID(Integer clipID) {
		this.clipID = clipID;
	}
	public Integer getInlineFlag() {
		return inlineFlag;
	}
	public void setInlineFlag(Integer inlineFlag) {
		this.inlineFlag = inlineFlag;
	}
}
