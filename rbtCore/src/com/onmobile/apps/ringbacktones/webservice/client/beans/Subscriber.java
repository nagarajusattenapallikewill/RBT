package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Subscriber 
{
	private String subscriberID = null;
	private boolean isValidPrefix;
	private boolean canAllow;
	private boolean isPrepaid;
	private int accessCount;
	private String status = null;
	private String subscriptionState = null;
	private String language = null;
	private String circleID = null;
	private String userType = null;
	private String pack = null;
	private String subscriptionClass = null;
	private String cosID = null;
	private String activatedBy = null;
	private String activationInfo = null;
	private String deactivatedBy = null;
	private String lastDeactivationInfo = null;
	private Date startDate = null;
	private Date endDate = null;
	private Date nextChargingDate = null;
	private Date lastDeactivationDate = null;
	private Date activationDate = null;
	private HashMap<String, String> userInfoMap = null;
	private boolean isNewsLetterOn;
	private int daysAfterDeactivation;
	private Date nextBillingDate;
	private String chargeDetails;
	private int totalDownloads;
	private boolean isUdsOn;
	private String refID = null;
	private String operatorUserInfo = null;
	private String pca = null;
	private String nextChargeClass;
	private String nextSelectionAmount;
	private String lastChargeAmount;
	private String lastTransactionType;
   
	private boolean isSubConsentInserted = false;
    private String protocolNo = null;
    private String protocolStaticText = null;
    private boolean isFreemiumSubscriber = false;
    private boolean isFreemium = false;
	private int numOfFreeSongsLeft;
	private int numMaxSelections;
	private String operatorUserType;
	private String subscriberType;
	private String operatorName;
    
	/**
	 * 
	 */
	public Subscriber()
	{

	}

	/**
	 * @param subscriberID
	 * @param isValidPrefix
	 * @param canAllow
	 * @param isPrepaid
	 * @param accessCount
	 * @param status
	 * @param subscriptionState
	 * @param language
	 * @param circleID
	 * @param userType
	 * @param pack
	 * @param subscriptionClass
	 * @param cosID
	 * @param activatedBy
	 * @param activationInfo
	 * @param deactivatedBy
	 * @param lastDeactivationInfo
	 * @param startDate
	 * @param endDate
	 * @param nextChargingDate
	 * @param lastDeactivationDate
	 * @param activationDate
	 * @param userInfoMap
	 * @param isNewsLetterOn
	 * @param daysAfterDeactivation
	 * @param nextBillingDate
	 * @param totalDownloads
	 * @param isUdsOn
	 * @param refID
	 * @param operatorUserInfo
	 * @param chargeDetails
	 */
	public Subscriber(String subscriberID, boolean isValidPrefix,
			boolean canAllow, boolean isPrepaid, int accessCount,
			String status, String subscriptionState, String language,
			String circleID, String userType, String pack,
			String subscriptionClass, String cosID, String activatedBy,
			String activationInfo, String deactivatedBy,
			String lastDeactivationInfo, Date startDate, Date endDate,
			Date nextChargingDate, Date lastDeactivationDate,
			Date activationDate, HashMap<String, String> userInfoMap,
			boolean isNewsLetterOn, int daysAfterDeactivation,
			Date nextBillingDate, int totalDownloads, boolean isUdsOn,
			String refID, String operatorUserInfo, String nextSelectionAmount, String nextChargeClass, String chargeDetails) {
		this.subscriberID = subscriberID;
		this.isValidPrefix = isValidPrefix;
		this.canAllow = canAllow;
		this.isPrepaid = isPrepaid;
		this.accessCount = accessCount;
		this.status = status;
		this.subscriptionState = subscriptionState;
		this.language = language;
		this.circleID = circleID;
		this.userType = userType;
		this.pack = pack;
		this.subscriptionClass = subscriptionClass;
		this.cosID = cosID;
		this.activatedBy = activatedBy;
		this.activationInfo = activationInfo;
		this.deactivatedBy = deactivatedBy;
		this.lastDeactivationInfo = lastDeactivationInfo;
		this.startDate = startDate;
		this.endDate = endDate;
		this.nextChargingDate = nextChargingDate;
		this.lastDeactivationDate = lastDeactivationDate;
		this.activationDate = activationDate;
		this.userInfoMap = userInfoMap;
		this.isNewsLetterOn = isNewsLetterOn;
		this.daysAfterDeactivation = daysAfterDeactivation;
		this.nextBillingDate = nextBillingDate;
		this.totalDownloads = totalDownloads;
		this.isUdsOn = isUdsOn;
		this.refID = refID;
		this.operatorUserInfo = operatorUserInfo;
		this.nextSelectionAmount = nextSelectionAmount;
		this.nextChargeClass = nextChargeClass;
		this.chargeDetails = chargeDetails;
	}


	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	public String getOperatorUserType() {
		return operatorUserType;
	}

	public void setOperatorUserType(String operatorUserType) {
		this.operatorUserType = operatorUserType;
	}

	public boolean isSubConsentInserted() {
		return isSubConsentInserted;
	}

	public void setSubConsentInserted(boolean isSubConsentInserted) {
		this.isSubConsentInserted = isSubConsentInserted;
	}

	/**
	 * @return the isValidPrefix
	 */
	public boolean isValidPrefix()
	{
		return isValidPrefix;
	}

	/**
	 * @return the canAllow
	 */
	public boolean isCanAllow()
	{
		return canAllow;
	}

	/**
	 * @return the isPrepaid
	 */
	public boolean isPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @return the accessCount
	 */
	public int getAccessCount()
	{
		return accessCount;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @return the subscriptionState
	 */
	public String getSubscriptionState()
	{
		return subscriptionState;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @return the userType
	 */
	public String getUserType()
	{
		return userType;
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
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
	}

	/**
	 * @return the cosID
	 */
	public String getCosID()
	{
		return cosID;
	}

	/**
	 * @return the activatedBy
	 */
	public String getActivatedBy()
	{
		return activatedBy;
	}

	/**
	 * @return the activationInfo
	 */
	public String getActivationInfo()
	{
		return activationInfo;
	}

	/**
	 * @return the deactivatedBy
	 */
	public String getDeactivatedBy()
	{
		return deactivatedBy;
	}

	/**
	 * @return the lastDeactivationInfo
	 */
	public String getLastDeactivationInfo()
	{
		return lastDeactivationInfo;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @return the nextChargingDate
	 */
	public Date getNextChargingDate()
	{
		return nextChargingDate;
	}

	/**
	 * @return the lastDeactivationDate
	 */
	public Date getLastDeactivationDate()
	{
		return lastDeactivationDate;
	}

	/**
	 * @return the activationDate
	 */
	public Date getActivationDate()
	{
		return activationDate;
	}

	/**
	 * @return the userInfoMap
	 */
	public HashMap<String, String> getUserInfoMap()
	{
		return userInfoMap;
	}

	/**
	 * @return the isNewsLetterOn
	 */
	public boolean isNewsLetterOn()
	{
		return isNewsLetterOn;
	}

	/**
	 * @return the daysAfterDeactivation
	 */
	public int getDaysAfterDeactivation()
	{
		return daysAfterDeactivation;
	}

	/**
	 * @return the nextBillingDate
	 */
	public Date getNextBillingDate()
	{
		return nextBillingDate;
	}

	/**
	 * @return the chargeDetails
	 */
	public String getChargeDetails() {
		return chargeDetails;
	}

	/**
	 * @return the totalDownloads
	 */
	public int getTotalDownloads()
	{
		return totalDownloads;
	}

	/**
	 * @return the isUdsOn
	 */
	public boolean isUdsOn()
	{
		return isUdsOn;
	}

	/**
	 * @return the operatorUserInfo
	 */
	public String getOperatorUserInfo()
	{
		return operatorUserInfo;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param isValidPrefix the isValidPrefix to set
	 */
	public void setValidPrefix(boolean isValidPrefix)
	{
		this.isValidPrefix = isValidPrefix;
	}

	/**
	 * @param canAllow the canAllow to set
	 */
	public void setCanAllow(boolean canAllow)
	{
		this.canAllow = canAllow;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setPrepaid(boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @param accessCount the accessCount to set
	 */
	public void setAccessCount(int accessCount)
	{
		this.accessCount = accessCount;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @param subscriptionState the subscriptionState to set
	 */
	public void setSubscriptionState(String subscriptionState)
	{
		this.subscriptionState = subscriptionState;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(String cosID)
	{
		this.cosID = cosID;
	}


	/**
	 * @param activatedBy the activatedBy to set
	 */
	public void setActivatedBy(String activatedBy)
	{
		this.activatedBy = activatedBy;
	}

	/**
	 * @param activationInfo the activationInfo to set
	 */
	public void setActivationInfo(String activationInfo)
	{
		this.activationInfo = activationInfo;
	}

	/**
	 * @param deactivatedBy the deactivatedBy to set
	 */
	public void setDeactivatedBy(String deactivatedBy)
	{
		this.deactivatedBy = deactivatedBy;
	}

	/**
	 * @param lastDeactivationInfo the lastDeactivationInfo to set
	 */
	public void setLastDeactivationInfo(String lastDeactivationInfo)
	{
		this.lastDeactivationInfo = lastDeactivationInfo;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @param nextChargingDate the nextChargingDate to set
	 */
	public void setNextChargingDate(Date nextChargingDate)
	{
		this.nextChargingDate = nextChargingDate;
	}

	/**
	 * @param lastDeactivationDate the lastDeactivationDate to set
	 */
	public void setLastDeactivationDate(Date lastDeactivationDate)
	{
		this.lastDeactivationDate = lastDeactivationDate;
	}

	/**
	 * @param activationDate the activationDate to set
	 */
	public void setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
	}

	/**
	 * @param userInfoMap the userInfoMap to set
	 */
	public void setUserInfoMap(HashMap<String, String> userInfoMap)
	{
		this.userInfoMap = userInfoMap;
	}

	/**
	 * @param isNewsLetterOn the isNewsLetterOn to set
	 */
	public void setNewsLetterOn(boolean isNewsLetterOn)
	{
		this.isNewsLetterOn = isNewsLetterOn;
	}

	/**
	 * @param daysAfterDeactivation the daysAfterDeactivation to set
	 */
	public void setDaysAfterDeactivation(int daysAfterDeactivation)
	{
		this.daysAfterDeactivation = daysAfterDeactivation;
	}

	/**
	 * @param nextBillingDate the nextBillingDate to set
	 */
	public void setNextBillingDate(Date nextBillingDate)
	{
		this.nextBillingDate = nextBillingDate;
	}

	/**
	 * @param totalDownloads the totalDownloads to set
	 */
	public void setTotalDownloads(int totalDownloads)
	{
		this.totalDownloads = totalDownloads;
	}

	/**
	 * @param chargeDetails the chargeDetails to set
	 */
	public void setChargeDetails(String chargeDetails) {
		this.chargeDetails = chargeDetails;
	}

	/**
	 * @param isUdsOn the isUdsOn to set
	 */
	public void setUdsOn(boolean isUdsOn)
	{
		this.isUdsOn = isUdsOn;
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
	 * @param operatorUserInfo the operatorUserInfo to set
	 */
	public void setOperatorUserInfo(String operatorUserInfo)
	{
		this.operatorUserInfo = operatorUserInfo;
	}

	/**
	 * @return the pca
	 */
	public String getPca() {
		return pca;
	}

	/**
	 * @param pca the pca to set
	 */
	public void setPca(String pca) {
		this.pca = pca;
	}

	/**
	 * @return the nextChargeClass
	 */
	public String getNextChargeClass() {
		return nextChargeClass;
	}

	/**
	 * @param nextChargeClass the nextChargeClass to set
	 */
	public void setNextChargeClass(String nextChargeClass) {
		this.nextChargeClass = nextChargeClass;
	}

	/**
	 * @return the nextSelectionAmount
	 */
	public String getNextSelectionAmount() {
		return nextSelectionAmount;
	}

	/**
	 * @param nextSelectionAmount the nextSelectionAmount to set
	 */
	public void setNextSelectionAmount(String nextSelectionAmount) {
		this.nextSelectionAmount = nextSelectionAmount;
	}

	/**
	 * @return
	 */
	public String getLastChargeAmount() {
		return lastChargeAmount;
	}

	/**
	 * @param lastChargeAmount
	 */
	public void setLastChargeAmount(String lastChargeAmount) {
		this.lastChargeAmount = lastChargeAmount;
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

	public boolean isFreemiumSubscriber() {
		return isFreemiumSubscriber;
	}

	public void setFreemiumSubscriber(boolean isFreemiumSubscriber) {
		this.isFreemiumSubscriber = isFreemiumSubscriber;
	}
	
	public boolean isFreemium() {
		return isFreemium;
	}

	public void setFreemium(boolean isFreemium) {
		this.isFreemium = isFreemium;
	}

	public int getNumOfFreeSongsLeft() {
		return numOfFreeSongsLeft;
	}

	public void setNumOfFreeSongsLeft(int numOfFreeSongsLeft) {
		this.numOfFreeSongsLeft = numOfFreeSongsLeft;
	}
	

	public String getSubscriberType() {
		return subscriberType;
	}

	public void setSubscriberType(String subscriberType) {
		this.subscriberType = subscriberType;
	}





	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + accessCount;
		result = prime * result
				+ ((activatedBy == null) ? 0 : activatedBy.hashCode());
		result = prime * result
				+ ((activationDate == null) ? 0 : activationDate.hashCode());
		result = prime * result
				+ ((activationInfo == null) ? 0 : activationInfo.hashCode());
		result = prime * result + (canAllow ? 1231 : 1237);
		result = prime * result
				+ ((chargeDetails == null) ? 0 : chargeDetails.hashCode());
		result = prime * result
				+ ((circleID == null) ? 0 : circleID.hashCode());
		result = prime * result + ((cosID == null) ? 0 : cosID.hashCode());
		result = prime * result + daysAfterDeactivation;
		result = prime * result
				+ ((deactivatedBy == null) ? 0 : deactivatedBy.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (isFreemium ? 1231 : 1237);
		result = prime * result + (isFreemiumSubscriber ? 1231 : 1237);
		result = prime * result + (isNewsLetterOn ? 1231 : 1237);
		result = prime * result + (isPrepaid ? 1231 : 1237);
		result = prime * result + (isSubConsentInserted ? 1231 : 1237);
		result = prime * result + (isUdsOn ? 1231 : 1237);
		result = prime * result + (isValidPrefix ? 1231 : 1237);
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime
				* result
				+ ((lastChargeAmount == null) ? 0 : lastChargeAmount.hashCode());
		result = prime
				* result
				+ ((lastDeactivationDate == null) ? 0 : lastDeactivationDate
						.hashCode());
		result = prime
				* result
				+ ((lastDeactivationInfo == null) ? 0 : lastDeactivationInfo
						.hashCode());
		result = prime * result
				+ ((nextBillingDate == null) ? 0 : nextBillingDate.hashCode());
		result = prime * result
				+ ((nextChargeClass == null) ? 0 : nextChargeClass.hashCode());
		result = prime
				* result
				+ ((nextChargingDate == null) ? 0 : nextChargingDate.hashCode());
		result = prime
				* result
				+ ((nextSelectionAmount == null) ? 0 : nextSelectionAmount
						.hashCode());
		result = prime * result + numMaxSelections;
		result = prime * result + numOfFreeSongsLeft;
		result = prime
				* result
				+ ((operatorUserInfo == null) ? 0 : operatorUserInfo.hashCode());
		result = prime
				* result
				+ ((operatorUserType == null) ? 0 : operatorUserType.hashCode());
		result = prime * result + ((pack == null) ? 0 : pack.hashCode());
		result = prime * result + ((pca == null) ? 0 : pca.hashCode());
		result = prime * result
				+ ((protocolNo == null) ? 0 : protocolNo.hashCode());
		result = prime
				* result
				+ ((protocolStaticText == null) ? 0 : protocolStaticText
						.hashCode());
		result = prime * result + ((refID == null) ? 0 : refID.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result
				+ ((subscriberType == null) ? 0 : subscriberType.hashCode());
		result = prime
				* result
				+ ((subscriptionClass == null) ? 0 : subscriptionClass
						.hashCode());
		result = prime
				* result
				+ ((subscriptionState == null) ? 0 : subscriptionState
						.hashCode());
		result = prime * result + totalDownloads;
		result = prime * result
				+ ((userInfoMap == null) ? 0 : userInfoMap.hashCode());
		result = prime * result
				+ ((userType == null) ? 0 : userType.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Subscriber))
			return false;
		Subscriber other = (Subscriber) obj;
		if (accessCount != other.accessCount)
			return false;
		if (activatedBy == null) {
			if (other.activatedBy != null)
				return false;
		} else if (!activatedBy.equals(other.activatedBy))
			return false;
		if (activationDate == null) {
			if (other.activationDate != null)
				return false;
		} else if (!activationDate.equals(other.activationDate))
			return false;
		if (activationInfo == null) {
			if (other.activationInfo != null)
				return false;
		} else if (!activationInfo.equals(other.activationInfo))
			return false;
		if (canAllow != other.canAllow)
			return false;
		if (chargeDetails == null) {
			if (other.chargeDetails != null)
				return false;
		} else if (!chargeDetails.equals(other.chargeDetails))
			return false;
		if (circleID == null) {
			if (other.circleID != null)
				return false;
		} else if (!circleID.equals(other.circleID))
			return false;
		if (cosID == null) {
			if (other.cosID != null)
				return false;
		} else if (!cosID.equals(other.cosID))
			return false;
		if (daysAfterDeactivation != other.daysAfterDeactivation)
			return false;
		if (deactivatedBy == null) {
			if (other.deactivatedBy != null)
				return false;
		} else if (!deactivatedBy.equals(other.deactivatedBy))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (isFreemium != other.isFreemium)
			return false;
		if (isFreemiumSubscriber != other.isFreemiumSubscriber)
			return false;
		if (isNewsLetterOn != other.isNewsLetterOn)
			return false;
		if (isPrepaid != other.isPrepaid)
			return false;
		if (isSubConsentInserted != other.isSubConsentInserted)
			return false;
		if (isUdsOn != other.isUdsOn)
			return false;
		if (isValidPrefix != other.isValidPrefix)
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (lastChargeAmount == null) {
			if (other.lastChargeAmount != null)
				return false;
		} else if (!lastChargeAmount.equals(other.lastChargeAmount))
			return false;
		if (lastDeactivationDate == null) {
			if (other.lastDeactivationDate != null)
				return false;
		} else if (!lastDeactivationDate.equals(other.lastDeactivationDate))
			return false;
		if (lastDeactivationInfo == null) {
			if (other.lastDeactivationInfo != null)
				return false;
		} else if (!lastDeactivationInfo.equals(other.lastDeactivationInfo))
			return false;
		if (nextBillingDate == null) {
			if (other.nextBillingDate != null)
				return false;
		} else if (!nextBillingDate.equals(other.nextBillingDate))
			return false;
		if (nextChargeClass == null) {
			if (other.nextChargeClass != null)
				return false;
		} else if (!nextChargeClass.equals(other.nextChargeClass))
			return false;
		if (nextChargingDate == null) {
			if (other.nextChargingDate != null)
				return false;
		} else if (!nextChargingDate.equals(other.nextChargingDate))
			return false;
		if (nextSelectionAmount == null) {
			if (other.nextSelectionAmount != null)
				return false;
		} else if (!nextSelectionAmount.equals(other.nextSelectionAmount))
			return false;
		if (numMaxSelections != other.numMaxSelections)
			return false;
		if (numOfFreeSongsLeft != other.numOfFreeSongsLeft)
			return false;
		if (operatorUserInfo == null) {
			if (other.operatorUserInfo != null)
				return false;
		} else if (!operatorUserInfo.equals(other.operatorUserInfo))
			return false;
		if (operatorUserType == null) {
			if (other.operatorUserType != null)
				return false;
		} else if (!operatorUserType.equals(other.operatorUserType))
			return false;
		if (pack == null) {
			if (other.pack != null)
				return false;
		} else if (!pack.equals(other.pack))
			return false;
		if (pca == null) {
			if (other.pca != null)
				return false;
		} else if (!pca.equals(other.pca))
			return false;
		if (protocolNo == null) {
			if (other.protocolNo != null)
				return false;
		} else if (!protocolNo.equals(other.protocolNo))
			return false;
		if (protocolStaticText == null) {
			if (other.protocolStaticText != null)
				return false;
		} else if (!protocolStaticText.equals(other.protocolStaticText))
			return false;
		if (refID == null) {
			if (other.refID != null)
				return false;
		} else if (!refID.equals(other.refID))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (subscriberID == null) {
			if (other.subscriberID != null)
				return false;
		} else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (subscriberType == null) {
			if (other.subscriberType != null)
				return false;
		} else if (!subscriberType.equals(other.subscriberType))
			return false;
		if (subscriptionClass == null) {
			if (other.subscriptionClass != null)
				return false;
		} else if (!subscriptionClass.equals(other.subscriptionClass))
			return false;
		if (subscriptionState == null) {
			if (other.subscriptionState != null)
				return false;
		} else if (!subscriptionState.equals(other.subscriptionState))
			return false;
		if (totalDownloads != other.totalDownloads)
			return false;
		if (userInfoMap == null) {
			if (other.userInfoMap != null)
				return false;
		} else if (!userInfoMap.equals(other.userInfoMap))
			return false;
		if (userType == null) {
			if (other.userType != null)
				return false;
		} else if (!userType.equals(other.userType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Subscriber [subscriberID=");
		builder.append(subscriberID);
		builder.append(", isValidPrefix=");
		builder.append(isValidPrefix);
		builder.append(", canAllow=");
		builder.append(canAllow);
		builder.append(", isPrepaid=");
		builder.append(isPrepaid);
		builder.append(", accessCount=");
		builder.append(accessCount);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscriptionState=");
		builder.append(subscriptionState);
		builder.append(", language=");
		builder.append(language);
		builder.append(", circleID=");
		builder.append(circleID);
		builder.append(", userType=");
		builder.append(userType);
		builder.append(", pack=");
		builder.append(pack);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", cosID=");
		builder.append(cosID);
		builder.append(", activatedBy=");
		builder.append(activatedBy);
		builder.append(", activationInfo=");
		builder.append(activationInfo);
		builder.append(", deactivatedBy=");
		builder.append(deactivatedBy);
		builder.append(", lastDeactivationInfo=");
		builder.append(lastDeactivationInfo);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", nextChargingDate=");
		builder.append(nextChargingDate);
		builder.append(", lastDeactivationDate=");
		builder.append(lastDeactivationDate);
		builder.append(", activationDate=");
		builder.append(activationDate);
		builder.append(", userInfoMap=");
		builder.append(userInfoMap);
		builder.append(", isNewsLetterOn=");
		builder.append(isNewsLetterOn);
		builder.append(", daysAfterDeactivation=");
		builder.append(daysAfterDeactivation);
		builder.append(", nextBillingDate=");
		builder.append(nextBillingDate);
		builder.append(", chargeDetails=");
		builder.append(chargeDetails);
		builder.append(", totalDownloads=");
		builder.append(totalDownloads);
		builder.append(", isUdsOn=");
		builder.append(isUdsOn);
		builder.append(", refID=");
		builder.append(refID);
		builder.append(", operatorUserInfo=");
		builder.append(operatorUserInfo);
		builder.append(", pca=");
		builder.append(pca);
		builder.append(", nextChargeClass=");
		builder.append(nextChargeClass);
		builder.append(", nextSelectionAmount=");
		builder.append(nextSelectionAmount);
		builder.append(", lastChargeAmount=");
		builder.append(lastChargeAmount);
		builder.append(", isSubConsentInserted=");
		builder.append(isSubConsentInserted);
		builder.append(", protocolNo=");
		builder.append(protocolNo);
		builder.append(", protocolStaticText=");
		builder.append(protocolStaticText);
		builder.append(", isFreemiumSubscriber=");
		builder.append(isFreemiumSubscriber);
		builder.append(", isFreemium=");
		builder.append(isFreemium);
		builder.append(", numOfFreeSongsLeft=");
		builder.append(numOfFreeSongsLeft);
		builder.append(", numMaxSelections=");
		builder.append(numMaxSelections);
		builder.append(", operatorUserType=");
		builder.append(operatorUserType);
		builder.append(", subscriberType=");
		builder.append(subscriberType);
		builder.append(", operatorName=");
		builder.append(operatorName);
		builder.append("]");
		return builder.toString();
	}

	public int getNumMaxSelections() {
		return numMaxSelections;
	}
	
	public void setNumMaxSelections(int numMaxSelections) {
		this.numMaxSelections = numMaxSelections;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getLastTransactionType() {
		return lastTransactionType;
	}

	public void setLastTransactionType(String lastTransactionType) {
		this.lastTransactionType = lastTransactionType;
	}

}
