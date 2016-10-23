/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class WebServiceSubscriber implements WebServiceConstants
{
	private String subscriberID = null;
	private boolean isValidPrefix;
	private boolean canAllow;
	private boolean isPrepaid;
	private int accessCount;
	private String subscriptionYes;
	private String status = null;
	private String language = null;
	private String circleID = null;
	private String subscriptionClass = null;
	private String userType = null;
	private String pack = null;
	private String activatedBy = null;
	private String activationInfo = null;
	private String deactivatedBy = null;
	private String lastDeactivationInfo = null;
	private Date startDate = null;
	private Date endDate = null;
	private Date nextChargingDate = null;
	private Date lastDeactivationDate = null;
	private Date activationDate = null;
	private String cosID = null;
	private String refID = null;
	private String userInfo = null;
	private String operatorUserInfo = null;
	private String pca = null;
	private String voluntary = null;
	private boolean isSubConsentInserted = false;
	private String protocolNo = null;
	private String protocolStaticText = null;
	private int numMaxSelections = 0;
	private String subscriberType = null;
	private String operatorUserType;
	private String operatorName = null;
	/**
	 * 
	 */
	public WebServiceSubscriber()
	{

	}

	/**
	 * @param subscriberID
	 * @param isValidPrefix
	 * @param canAllow
	 * @param isPrepaid
	 * @param accessCount
	 * @param subscriptionYes
	 * @param status
	 * @param language
	 * @param circleID
	 * @param subscriptionClass
	 * @param userType
	 * @param activatedBy
	 * @param activationInfo
	 * @param deactivatedBy
	 * @param lastDeactivationInfo
	 * @param startDate
	 * @param endDate
	 * @param nextChargingDate
	 * @param lastDeactivationDate
	 * @param activationDate
	 * @param cosID
	 * @param refID
	 * @param userInfo
	 * @param operatorUserInfo
	 */
	public WebServiceSubscriber(String subscriberID, boolean isValidPrefix,
			boolean canAllow, boolean isPrepaid, int accessCount,
			String subscriptionYes, String status, String language,
			String circleID, String subscriptionClass, String userType,
			String activatedBy, String activationInfo, String deactivatedBy,
			String lastDeactivationInfo, Date startDate, Date endDate,
			Date nextChargingDate, Date lastDeactivationDate,
			Date activationDate, String cosID, String refID, String userInfo,
			String operatorUserInfo)
	{
		this.subscriberID = subscriberID;
		this.isValidPrefix = isValidPrefix;
		this.canAllow = canAllow;
		this.isPrepaid = isPrepaid;
		this.accessCount = accessCount;
		this.subscriptionYes = subscriptionYes;
		this.status = status;
		this.language = language;
		this.circleID = circleID;
		this.subscriptionClass = subscriptionClass;
		this.userType = userType;
		this.activatedBy = activatedBy;
		this.activationInfo = activationInfo;
		this.deactivatedBy = deactivatedBy;
		this.lastDeactivationInfo = lastDeactivationInfo;
		this.startDate = startDate;
		this.endDate = endDate;
		this.nextChargingDate = nextChargingDate;
		this.lastDeactivationDate = lastDeactivationDate;
		this.activationDate = activationDate;
		this.refID = refID;
		this.cosID = cosID;
		this.userInfo = userInfo;
		this.operatorUserInfo = operatorUserInfo;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the isValidPrefix
	 */
	public boolean isValidPrefix()
	{
		return isValidPrefix;
	}

	/**
	 * @param isValidPrefix the isValidPrefix to set
	 */
	public void setValidPrefix(boolean isValidPrefix)
	{
		this.isValidPrefix = isValidPrefix;
	}

	public boolean isSubConsentInserted() {
		return isSubConsentInserted;
	}
	
	public void setSubConsentInserted(boolean isSubConsentInserted) {
		this.isSubConsentInserted = isSubConsentInserted;
	}
	
	/**
	 * @return the canAllow
	 */
	public boolean isCanAllow()
	{
		return canAllow;
	}

	/**
	 * @param canAllow the canAllow to set
	 */
	public void setCanAllow(boolean canAllow)
	{
		this.canAllow = canAllow;
	}

	/**
	 * @return the isPrepaid
	 */
	public boolean isPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setPrepaid(boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @return the accessCount
	 */
	public int getAccessCount()
	{
		return accessCount;
	}

	/**
	 * @param accessCount the accessCount to set
	 */
	public void setAccessCount(int accessCount)
	{
		this.accessCount = accessCount;
	}

	/**
	 * @return the subscriptionYes
	 */
	public String getSubscriptionYes()
	{
		return subscriptionYes;
	}

	/**
	 * @param subscriptionYes the subscriptionYes to set
	 */
	public void setSubscriptionYes(String subscriptionYes)
	{
		this.subscriptionYes = subscriptionYes;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
	}

	/**
	 * @return the userType
	 */
	public String getUserType()
	{
		return userType;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(String userType)
	{
		this.userType = userType;
	}
	
	/**
	 * @return the pack
	 */
	public String getPack() {
		return pack;
	}

	/**
	 * @param pack the pack to set
	 */
	public void setPack(String pack) {
		this.pack = pack;
	}

	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy()
	{
		return activatedBy;
	}

	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy)
	{
		this.activatedBy = activatedBy;
	}

	/**
	 * @return the activationInfo
	 */
	public String getActivationInfo()
	{
		return activationInfo;
	}

	/**
	 * @param activationInfo the activationInfo to set
	 */
	public void setActivationInfo(String activationInfo)
	{
		this.activationInfo = activationInfo;
	}

	/**
	 * @return the deactivatedBy
	 */
	public String getDeactivatedBy()
	{
		return deactivatedBy;
	}

	/**
	 * @param deactivatedBy the deactivatedBy to set
	 */
	public void setDeactivatedBy(String deactivatedBy)
	{
		this.deactivatedBy = deactivatedBy;
	}

	/**
	 * @return the lastDeactivationInfo
	 */
	public String getLastDeactivationInfo()
	{
		return lastDeactivationInfo;
	}

	/**
	 * @param lastDeactivationInfo the lastDeactivationInfo to set
	 */
	public void setLastDeactivationInfo(String lastDeactivationInfo)
	{
		this.lastDeactivationInfo = lastDeactivationInfo;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @return the nextChargingDate
	 */
	public Date getNextChargingDate()
	{
		return nextChargingDate;
	}

	/**
	 * @param nextChargingDate the nextChargingDate to set
	 */
	public void setNextChargingDate(Date nextChargingDate)
	{
		this.nextChargingDate = nextChargingDate;
	}

	/**
	 * @return the lastDeactivationDate
	 */
	public Date getLastDeactivationDate()
	{
		return lastDeactivationDate;
	}

	/**
	 * @param lastDeactivationDate the lastDeactivationDate to set
	 */
	public void setLastDeactivationDate(Date lastDeactivationDate)
	{
		this.lastDeactivationDate = lastDeactivationDate;
	}

	/**
	 * @return the activationDate
	 */
	public Date getActivationDate()
	{
		return activationDate;
	}

	/**
	 * @param activationDate the activationDate to set
	 */
	public void setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
	}

	/**
	 * @return the cosID
	 */
	public String getCosID()
	{
		return cosID;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(String cosID)
	{
		this.cosID = cosID;
	}
	
	/**
	 * @return the refID
	 */
	public String getRefID() {
		return refID;
	}

	/**
	 * @param refID the refID to set
	 */
	public void setRefID(String refID) {
		this.refID = refID;
	}

	/**
	 * @return the userInfo
	 */
	public String getUserInfo()
	{
		return userInfo;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(String userInfo)
	{
		this.userInfo = userInfo;
	}

	/**
	 * @return the operatorUserInfo
	 */
	public String getOperatorUserInfo()
	{
		return operatorUserInfo;
	}

	/**
	 * @param operatorUserInfo the operatorUserInfo to set
	 */
	public void setOperatorUserInfo(String operatorUserInfo)
	{
		this.operatorUserInfo = operatorUserInfo;
	}
	
	/**
	 * @return PCA
	 */
	public String getPca() {
		return pca;
	}

	/**
	 * @param pca the PCA to set
	 */
	public void setPca(String PCA) {
		pca = PCA;
	}


	public String getVoluntary() {
		return voluntary;
	}

	public void setVoluntary(String voluntary) {
		this.voluntary = voluntary;
	}
	
	public String getProtocolNo() {
		return protocolNo;
	}

	public void setProtocolNo(String protocolNo) {
		this.protocolNo = protocolNo;
	}

	public String getProtocolStaticText() {
		return protocolStaticText;
	}

	public void setProtocolStaticText(String protocolStaticText) {
		this.protocolStaticText = protocolStaticText;
	}
	
	public int getNumMaxSelections() {
		return numMaxSelections;
	}

	public void setNumMaxSelections(int numMaxSelections) {
		this.numMaxSelections = numMaxSelections;
	}

	public String getSubscriberType() {
		return subscriberType;
	}
	
	public void setSubscriberType(String subscriberType) {
		this.subscriberType = subscriberType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WebServiceSubscriber[accessCount=");
		builder.append(accessCount);
		builder.append(", activatedBy=");
		builder.append(activatedBy);
		builder.append(", activationDate=");
		builder.append(activationDate);
		builder.append(", activationInfo=");
		builder.append(activationInfo);
		builder.append(", canAllow=");
		builder.append(canAllow);
		builder.append(", circleID=");
		builder.append(circleID);
		builder.append(", cosID=");
		builder.append(cosID);
		builder.append(", deactivatedBy=");
		builder.append(deactivatedBy);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", isPrepaid=");
		builder.append(isPrepaid);
		builder.append(", isValidPrefix=");
		builder.append(isValidPrefix);
		builder.append(", language=");
		builder.append(language);
		builder.append(", lastDeactivationDate=");
		builder.append(lastDeactivationDate);
		builder.append(", lastDeactivationInfo=");
		builder.append(lastDeactivationInfo);
		builder.append(", nextChargingDate=");
		builder.append(nextChargingDate);
		builder.append(", operatorUserInfo=");
		builder.append(operatorUserInfo);
		builder.append(", pack=");
		builder.append(pack);
		builder.append(", refID=");
		builder.append(refID);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", subscriptionYes=");
		builder.append(subscriptionYes);
		builder.append(", userInfo=");
		builder.append(userInfo);
		builder.append(", userType=");
		builder.append(userType);
		builder.append(", pca=");
		builder.append(pca);
		builder.append(", voluntary=");
		builder.append(voluntary);
		builder.append(", isSubConsentInserted=");
		builder.append(isSubConsentInserted);
		builder.append(", protocolNo=");
		builder.append(protocolNo);
		builder.append(", protocolStaticText=");
		builder.append(protocolStaticText);
		builder.append(", subscriberType=");
		builder.append(subscriberType);
		builder.append(", operatorUserType=");
		builder.append(operatorUserType);
		builder.append(", operatorName=");
		builder.append(operatorName);
		builder.append("]");
		return builder.toString();
	}

	public String getOperatorUserType() {
		return operatorUserType;
	}

	public void setOperatorUserType(String operatorUserType) {
		this.operatorUserType = operatorUserType;
	}
	
	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}


}
