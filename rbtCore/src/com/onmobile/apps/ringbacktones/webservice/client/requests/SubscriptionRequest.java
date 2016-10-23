package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 */
public class SubscriptionRequest extends Request
{
	private Boolean isPrepaid = null;
	private String language = null;
	private String calledNo = null;
	private String mmContext = null;
	private String subscriptionClass = null;
	private String rentalPack = null;
	private String gifterID = null;
	private Date giftSentTime = null;
	private String scratchCardNo = null;
	private Integer freePeriod = null;
	private Integer rbtType = null;
	private Boolean isDirectActivation = null;
	private Boolean isDeactCorporateUser = null;
	private Boolean isDirectDeactivation = null;
	private Boolean isCorporateDeactivation = null;
	private String subscriptionPeriod = null;
	private Boolean checkSubscriptionClass = null;
	private Boolean dontSMSInBlackOut = null;
	private String info = null;
	private Integer cosID = null;
	private Date lastAccessDate = null;
	private String playerStatus = null;
	private Boolean preCharged = null;
	private String operatorUserInfo = null;
	private String type = null;
	private Integer freeDays = null;
	private HashMap<String, String> userInfoMap = null;

	// Added to support ACT_MODE 
	private String activationMode = null;
	private boolean isBIOffer = false; 
	private boolean isDelayDeactForUpgardation = false;
	/**
	 * Added by Sreekar for Offer management
	 */
	private String offerID = null;
	private String packOfferID = null;

	//Added by SenthilRaja for automation deactivation
	private Date subscriberEndDate = null;
	//Added by Sandeep for voicePresence pack
	private Integer packCosId = null;
	private String internalRefId = null;
	private Boolean isAllowSmOffer = false;
	
	//Added for JIRA-RBT-6321
	private String refId = null;
	private boolean upgradeGraceAndSuspended  = false;
	private boolean suspendedUsersAllowed  = false;
	private boolean consentInd = false;
	private String consentTransId = null;
	private String tpcgID = null;
	private boolean isUnsubDelayDct = false;
	private boolean redirectionRequired = false;
	private boolean isUdsOn = false;
	private String extraInfo = null;
	//Added by Rony for RBT-12163 for mobileAppServer
	private boolean isResetPassword = false;
	private Date requestTime = null;
	private Boolean generateRefId = null;
	private boolean delayDctUI = false;
	
	public boolean isDelayDctUI() {
		return delayDctUI;
	}

	public void setDelayDctUI(boolean delayDctUI) {
		this.delayDctUI = delayDctUI;
	}

	public boolean isUdsOn() {
		return isUdsOn;
	}

	public void setUdsOn(boolean isUdsOn) {
		this.isUdsOn = isUdsOn;
	}

	public boolean isUnsubDelayDct() {
		return isUnsubDelayDct;
	}

	public void setUnsubDelayDct(boolean isUnsubDelayDct) {
		this.isUnsubDelayDct = isUnsubDelayDct;
	}

	public String getTpcgID() {
		return tpcgID;
	}

	public void setTpcgID(String tpcgID) {
		this.tpcgID = tpcgID;
	}

	public boolean isDelayDeactForUpgardation() {
		return isDelayDeactForUpgardation;
	}
	
	public void setDelayDeactForUpgardation(boolean isDelayDeactForUpgardation) {
		this.isDelayDeactForUpgardation = isDelayDeactForUpgardation;
	}
	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	/**
	 * @param subscriberID
	 */
	public SubscriptionRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param mode
	 */
	public SubscriptionRequest(String subscriberID, Boolean isPrepaid,
			String mode)
	{
		super(subscriberID);
		this.isPrepaid = isPrepaid;
		this.mode = mode;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param language
	 * @param calledNo
	 */
	public SubscriptionRequest(String subscriberID, Boolean isPrepaid, String language, String calledNo)
	{
		super(subscriberID);
		this.isPrepaid = isPrepaid;
		this.language = language;
		this.calledNo = calledNo;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param language
	 * @param calledNo
	 * @param mode
	 */
	public SubscriptionRequest(String subscriberID, Boolean isPrepaid, String language, String calledNo, String mode)
	{
		super(subscriberID);
		this.isPrepaid = isPrepaid;
		this.language = language;
		this.calledNo = calledNo;
		this.mode = mode;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param mode
	 * @param modeInfo
	 * @param subscriptionClass
	 * @param subscriptionPeriod
	 */
	public SubscriptionRequest(String subscriberID, Boolean isPrepaid,
			String mode, String modeInfo, String subscriptionClass,
			String subscriptionPeriod)
	{
		super(subscriberID);
		this.isPrepaid = isPrepaid;
		this.mode = mode;
		this.modeInfo = modeInfo;
		this.subscriptionClass = subscriptionClass;
		this.subscriptionPeriod = subscriptionPeriod;
	}


	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param mode
	 * @param modeInfo
	 * @param subscriptionClass
	 * @param rentalPack
	 * @param freePeriod
	 * @param rbtType
	 * @param isDirectActivation
	 * @param subscriptionPeriod
	 */
	public SubscriptionRequest(String subscriberID, Boolean isPrepaid,
			String mode, String modeInfo, String subscriptionClass,
			String rentalPack, Integer freePeriod, Integer rbtType,
			Boolean isDirectActivation, String subscriptionPeriod)
	{
		this(subscriberID, isPrepaid, mode, modeInfo, subscriptionClass, subscriptionPeriod);
		this.rentalPack = rentalPack;
		this.freePeriod = freePeriod;
		this.rbtType = rbtType;
		this.isDirectActivation = isDirectActivation;
	}

	/**
	 * @param subscriberID
	 * @param mode
	 * @param modeInfo
	 * @param checkSubscriptionClass
	 */
	public SubscriptionRequest(String subscriberID, String mode,
			String modeInfo, Boolean checkSubscriptionClass)
	{
		super(subscriberID);
		this.mode = mode;
		this.modeInfo = modeInfo;
		this.checkSubscriptionClass = checkSubscriptionClass;
	}

	/**
	 * @param subscriberID
	 * @param info
	 */
	public SubscriptionRequest(String subscriberID, String info)
	{
		super(subscriberID);
		this.info = info;
	}

	/**
	 * @return the isPrepaid
	 */
	public Boolean getIsPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setIsPrepaid(Boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
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
	 * @return the calledNo
	 */
	public String getCalledNo()
	{
		return calledNo;
	}

	/**
	 * @param calledNo the calledNo to set
	 */
	public void setCalledNo(String calledNo)
	{
		this.calledNo = calledNo;
	}

	/**
	 * @return the mmContext
	 */
	public String getMmContext()
	{
		return mmContext;
	}

	/**
	 * @param mmContext the mmContext to set
	 */
	public void setMmContext(String mmContext)
	{
		this.mmContext = mmContext;
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

	public String getActivationMode() {
		return activationMode;
	}

	public Boolean getIsDeactCorporateUser() {
		return isDeactCorporateUser;
	}

	public void setIsDeactCorporateUser(Boolean isDeactCorporateUser) {
		this.isDeactCorporateUser = isDeactCorporateUser;
	}

	public void setActivationMode(String activationMode) {
		this.activationMode = activationMode;
	}

	/**
	 * @return the rentalPack
	 */
	public String getRentalPack()
	{
		return rentalPack;
	}

	/**
	 * @param rentalPack the rentalPack to set
	 */
	public void setRentalPack(String rentalPack)
	{
		this.rentalPack = rentalPack;
	}

	/**
	 * @return the gifterID
	 */
	public String getGifterID()
	{
		return gifterID;
	}

	/**
	 * @param gifterID the gifterID to set
	 */
	public void setGifterID(String gifterID)
	{
		this.gifterID = gifterID;
	}

	/**
	 * @return the giftSentTime
	 */
	public Date getGiftSentTime()
	{
		return giftSentTime;
	}

	/**
	 * @param giftSentTime the giftSentTime to set
	 */
	public void setGiftSentTime(Date giftSentTime)
	{
		this.giftSentTime = giftSentTime;
	}

	/**
	 * @return the scratchCardNo
	 */
	public String getScratchCardNo()
	{
		return scratchCardNo;
	}

	/**
	 * @param scratchCardNo the scratchCardNo to set
	 */
	public void setScratchCardNo(String scratchCardNo)
	{
		this.scratchCardNo = scratchCardNo;
	}

	/**
	 * @return the freePeriod
	 */
	public Integer getFreePeriod()
	{
		return freePeriod;
	}

	/**
	 * @param freePeriod the freePeriod to set
	 */
	public void setFreePeriod(Integer freePeriod)
	{
		this.freePeriod = freePeriod;
	}

	/**
	 * @return the rbtType
	 */
	public Integer getRbtType()
	{
		return rbtType;
	}

	/**
	 * @param rbtType the rbtType to set
	 */
	public void setRbtType(Integer rbtType)
	{
		this.rbtType = rbtType;
	}

	/**
	 * @return the isDirectActivation
	 */
	public Boolean getIsDirectActivation()
	{
		return isDirectActivation;
	}

	/**
	 * @param isDirectActivation the isDirectActivation to set
	 */
	public void setIsDirectActivation(Boolean isDirectActivation)
	{
		this.isDirectActivation = isDirectActivation;
	}

	/**
	 * @return the isDirectDeactivation
	 */
	public Boolean getIsDirectDeactivation()
	{
		return isDirectDeactivation;
	}

	/**
	 * @param isDirectDeactivation the isDirectDeactivation to set
	 */
	public void setIsDirectDeactivation(Boolean isDirectDeactivation)
	{
		this.isDirectDeactivation = isDirectDeactivation;
	}

	/**
	 * @return the isCorporateDeactivation
	 */
	public Boolean getIsCorporateDeactivation()
	{
		return isCorporateDeactivation;
	}

	/**
	 * @param isCorporateDeactivation the isCorporateDeactivation to set
	 */
	public void setIsCorporateDeactivation(Boolean isCorporateDeactivation)
	{
		this.isCorporateDeactivation = isCorporateDeactivation;
	}

	/**
	 * @return the subscriptionPeriod
	 */
	public String getSubscriptionPeriod()
	{
		return subscriptionPeriod;
	}

	/**
	 * @param subscriptionPeriod the subscriptionPeriod to set
	 */
	public void setSubscriptionPeriod(String subscriptionPeriod)
	{
		this.subscriptionPeriod = subscriptionPeriod;
	}

	/**
	 * @return the checkSubscriptionClass
	 */
	public Boolean getCheckSubscriptionClass()
	{
		return checkSubscriptionClass;
	}

	/**
	 * @param checkSubscriptionClass the checkSubscriptionClass to set
	 */
	public void setCheckSubscriptionClass(Boolean checkSubscriptionClass)
	{
		this.checkSubscriptionClass = checkSubscriptionClass;
	}

	/**
	 * @return the dontSMSInBlackOut
	 */
	public Boolean getDontSMSInBlackOut()
	{
		return dontSMSInBlackOut;
	}

	/**
	 * @param dontSMSInBlackOut the dontSMSInBlackOut to set
	 */
	public void setDontSMSInBlackOut(Boolean dontSMSInBlackOut)
	{
		this.dontSMSInBlackOut = dontSMSInBlackOut;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * @return the cosID
	 */
	public Integer getCosID()
	{
		return cosID;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(Integer cosID)
	{
		this.cosID = cosID;
	}

	/**
	 * @return the lastAccessDate
	 */
	public Date getLastAccessDate()
	{
		return lastAccessDate;
	}

	/**
	 * @param lastAccessDate the lastAccessDate to set
	 */
	public void setLastAccessDate(Date lastAccessDate)
	{
		this.lastAccessDate = lastAccessDate;
	}

	/**
	 * @return the playerStatus
	 */
	public String getPlayerStatus()
	{
		return playerStatus;
	}

	/**
	 * @param playerStatus the playerStatus to set
	 */
	public void setPlayerStatus(String playerStatus)
	{
		this.playerStatus = playerStatus;
	}

	/**
	 * @return the preCharged
	 */
	public Boolean getPreCharged()
	{
		return preCharged;
	}

	/**
	 * @param preCharged the preCharged to set
	 */
	public void setPreCharged(Boolean preCharged)
	{
		this.preCharged = preCharged;
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
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the freeDays
	 */
	public Integer getFreeDays()
	{
		return freeDays;
	}

	/**
	 * @param freeDays the freeDays to set
	 */
	public void setFreeDays(Integer freeDays)
	{
		this.freeDays = freeDays;
	}

	/**
	 * @return the userInfoMap
	 */
	public HashMap<String, String> getUserInfoMap()
	{
		return userInfoMap;
	}

	/**
	 * @param userInfoMap the userInfoMap to set
	 */
	public void setUserInfoMap(HashMap<String, String> userInfoMap)
	{
		this.userInfoMap = userInfoMap;
	}

	/**
	 * @return the offerID
	 */
	public String getOfferID()
	{
		return offerID;
	}

	/**
	 * @param offerID the offerID to set
	 */
	public void setOfferID(String offerID)
	{
		this.offerID = offerID;
	}

	/**
	 * @return
	 */
	public String getPackOfferID()
	{
		return packOfferID;
	}

	/**
	 * @param packOfferID
	 */
	public void setPackOfferID(String packOfferID)
	{
		this.packOfferID = packOfferID;
	}

	/**
	 * @return the subscriberEndDate
	 */
	public Date getSubscriberEndDate()
	{
		return subscriberEndDate;
	}

	/**
	 * @param subscriberEndDate
	 *            the subscriberEndDate to set
	 */
	public void setSubscriberEndDate(Date subscriberEndDate)
	{
		this.subscriberEndDate = subscriberEndDate;
	}
	
	/**
	 * @return the pack CosId
	 * 
	 */
	public Integer getPackCosId() {
		return packCosId;
	}
	
	/**
	 * 
	 * @param packCosId
	 */
	public void setPackCosId(Integer packCosId) {
		this.packCosId = packCosId;
	}

	public String getInternalRefId() {
		return internalRefId;
	}

	public void setInternalRefId(String internalRefId) {
		this.internalRefId = internalRefId;
	}

	public Boolean getIsAllowSmOffer() {
		return isAllowSmOffer;
	}

	public void setIsAllowSmOffer(Boolean isAllowSmOffer) {
		this.isAllowSmOffer = isAllowSmOffer;
	}
	
	public boolean isUpgradeGraceAndSuspended() {
		return upgradeGraceAndSuspended;
	}

	public void setUpgradeGraceAndSuspended(Boolean upgradeGraceAndSuspended) {
		this.upgradeGraceAndSuspended = upgradeGraceAndSuspended;
	}

	public boolean isSuspendedUsersAllowed() {
		return suspendedUsersAllowed;
	}

	public void setSuspendedUsersAllowed(Boolean suspendedUsersAllowed) {
		this.suspendedUsersAllowed = suspendedUsersAllowed;
	}
	
	public boolean isResetPassword() {
		return isResetPassword;
	}

	public void setResetPassword(boolean isResetPassword) {
		this.isResetPassword = isResetPassword;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#prepareRequestParams()
	 */
	@Override
	public void prepareRequestParams(WebServiceContext task)
	{
		super.prepareRequestParams(task);

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		if (task.containsKey(param_isPrepaid)) {
			isPrepaid = task.getString(param_isPrepaid).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_language)) {
			language = task.getString(param_language);
		}
		if (task.containsKey(param_calledNo)) {
			calledNo = task.getString(param_calledNo);
		}
		if (task.containsKey(param_mmContext)) {
			mmContext = task.getString(param_mmContext);
		}
		if (task.containsKey(param_subscriptionClass)) {
			subscriptionClass = task.getString(param_subscriptionClass);
		}
		if (task.containsKey(param_rentalPack)) {
			rentalPack = task.getString(param_rentalPack);
		}
		if (task.containsKey(param_gifterID)) {
			gifterID = task.getString(param_gifterID);
		}
		try
		{
			if (task.containsKey(param_giftSentTime)) {
				giftSentTime = dateFormat.parse(task
						.getString(param_giftSentTime));
			}
			if (task.containsKey(param_lastAccessDate)) {
				lastAccessDate = dateFormat.parse(task
						.getString(param_lastAccessDate));
			}
		}
		catch(ParseException pe )
		{
			
		}
		if (task.containsKey(param_scratchCardNo)) {
			scratchCardNo = task.getString(param_scratchCardNo);
		}
		if (task.containsKey(param_freePeriod)) {
			freePeriod = Integer.parseInt(task.getString(param_freePeriod));
		}
		if (task.containsKey(param_rbtType)) {
			rbtType = Integer.parseInt(task.getString(param_rbtType));
		}
		if (task.containsKey(param_isDirectActivation)) {
			isDirectActivation = task.getString(param_isDirectActivation)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_isDirectDeactivation)) {
			isDirectDeactivation = task.getString(param_isDirectDeactivation)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_isCorporateDeactivation)) {
			isCorporateDeactivation = task.getString(
					param_isCorporateDeactivation).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_isDeactivateCorporateUser)) {
			isDeactCorporateUser = task.getString(
					param_isDeactivateCorporateUser).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_subscriptionPeriod)) {
			subscriptionPeriod = task.getString(param_subscriptionPeriod);
		}
		if (task.containsKey(param_checkSubscriptionClass)) {
			checkSubscriptionClass = task.getString(
					param_checkSubscriptionClass).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_dontSMSInBlackOut)) {
			dontSMSInBlackOut = task.getString(param_dontSMSInBlackOut)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_info)) {
			info = task.getString(param_info);
		}
		if (task.containsKey(param_cosID)) {
			cosID = Integer.parseInt(task.getString(param_cosID));
		}
		if (task.containsKey(param_playerStatus)) {
			playerStatus = task.getString(param_playerStatus);
		}
		if (task.containsKey(param_preCharged)) {
			preCharged = task.getString(param_preCharged).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_operatorUserInfo)) {
			operatorUserInfo = task.getString(param_operatorUserInfo);
		}
		if (task.containsKey(param_type)) {
			type = task.getString(param_type);
		}
		if (task.containsKey(param_freeDays)) {
			freeDays = Integer.parseInt(task.getString(param_freeDays));
		}
		if (task.containsKey(param_actMode)) {
			activationMode = task.getString(param_actMode);
		}
		if (task.containsKey(param_offerID)) {
			offerID = task.getString(param_offerID);
		}
		if (task.containsKey(param_packOfferID)) {
			packOfferID = task.getString(param_packOfferID);
		}
		if (task.containsKey(param_subscriberEndDate))
		{
			try
			{
				subscriberEndDate = dateFormat.parse(task
						.getString(param_subscriberEndDate));
			}
			catch (ParseException e)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.set(2037, 0, 1, 0, 0, 0);
				subscriberEndDate = calendar.getTime();
			}
		}
		if (task.containsKey(param_packCosId)){
			packCosId = Integer.parseInt(task.getString(param_packCosId));
		}
		if (task.containsKey(param_internalRefId)){
			packCosId = Integer.parseInt(task.getString(param_internalRefId));
		}
		if(task.containsKey(param_isAllowSmOffer)){
			String allowSmOffer = task.getString(param_isAllowSmOffer).trim();
			isAllowSmOffer = allowSmOffer.equalsIgnoreCase(YES);
		}
		
		if(task.containsKey(param_upgradeGraceAndSuspended)){
			String upgradeGraceAndSuspendedStr = task.getString(param_upgradeGraceAndSuspended).trim();
			upgradeGraceAndSuspended = Boolean.valueOf(upgradeGraceAndSuspendedStr);
		}
		
		if(task.containsKey(param_suspendedUsersAllowed)){
			String suspendedUsersAllowedStr = task.getString(param_suspendedUsersAllowed).trim();
			suspendedUsersAllowed = Boolean.valueOf(suspendedUsersAllowedStr);
		}
        if(task.containsKey(param_bIOffer)){
        	 String biOffer = task.getString(param_bIOffer).trim();
        	 isBIOffer = Boolean.valueOf(biOffer);
        }
        if(task.containsKey(param_isUnsubDelayDctReq)){
        	 String unsubDelayReq = task.getString(param_isUnsubDelayDctReq).trim();
        	 isUnsubDelayDct = Boolean.valueOf(unsubDelayReq);
        }
        
		if (task.containsKey(param_isUdsOn)) {
			String udsOn = task.getString(param_isUdsOn).trim();
			isUdsOn = Boolean.valueOf(udsOn);
		}
		
		if (task.containsKey(param_isResetPassword)) {
			String isResetPassword = task.getString(param_isResetPassword).trim();
			this.isResetPassword = Boolean.valueOf(isResetPassword);
		}
		
		if (task.containsKey(param_generateRefId)) {
			generateRefId = Boolean.parseBoolean(task.getString(param_generateRefId));
		}
		
		if (task.containsKey(param_delayDct_UI)) {
			delayDctUI = Boolean.parseBoolean(task.getString(param_delayDct_UI));
		}
		
		Set<String> keys = task.keySet();
		for (String key : keys)
		{
			if (key.startsWith(param_userInfo + "_"))
			{
				String temp = key
						.substring((param_userInfo + "_").length() + 1);
				if(userInfoMap == null){
					userInfoMap = new HashMap<String, String>();
				}
				userInfoMap.put(temp, task.getString(key));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (isPrepaid != null) requestParams.put(param_isPrepaid, (isPrepaid ? YES : NO));
		if (language != null) requestParams.put(param_language, language);
		if (calledNo != null) requestParams.put(param_calledNo, calledNo);
		if (mmContext != null) requestParams.put(param_mmContext, mmContext);
		if (subscriptionClass != null) requestParams.put(param_subscriptionClass, subscriptionClass);
		if (rentalPack != null) requestParams.put(param_rentalPack, rentalPack);
		if (gifterID != null) requestParams.put(param_gifterID, gifterID);
		if (giftSentTime != null) requestParams.put(param_giftSentTime, dateFormat.format(giftSentTime));
		if (scratchCardNo != null) requestParams.put(param_scratchCardNo, scratchCardNo);
		if (freePeriod != null) requestParams.put(param_freePeriod, String.valueOf(freePeriod));
		if (rbtType != null) requestParams.put(param_rbtType, String.valueOf(rbtType));
		if (isDirectActivation != null) requestParams.put(param_isDirectActivation, (isDirectActivation ? YES : NO));
		//Added for RBT-6321
		if (refId != null) requestParams.put(param_refID, refId);
		if (isDirectDeactivation != null) requestParams.put(param_isDirectDeactivation, (isDirectDeactivation ? YES : NO));
		if (isCorporateDeactivation != null) requestParams.put(param_isCorporateDeactivation, (isCorporateDeactivation ? YES : NO));
		if (isDeactCorporateUser != null) requestParams.put(param_isDeactivateCorporateUser, (isDeactCorporateUser ? YES : NO));	
		if (subscriptionPeriod != null) requestParams.put(param_subscriptionPeriod, subscriptionPeriod);
		if (checkSubscriptionClass != null) requestParams.put(param_checkSubscriptionClass, (checkSubscriptionClass ? YES : NO));
		if (dontSMSInBlackOut != null) requestParams.put(param_dontSMSInBlackOut, (dontSMSInBlackOut ? YES : NO));
		if (info != null) requestParams.put(param_info, info);
		if (cosID != null) requestParams.put(param_cosID, String.valueOf(cosID));
		if (lastAccessDate != null) requestParams.put(param_lastAccessDate, dateFormat.format(lastAccessDate));
		if (playerStatus != null) requestParams.put(param_playerStatus, playerStatus);
		if (preCharged != null) requestParams.put(param_preCharged, (preCharged ? YES : NO));
		if (operatorUserInfo != null) requestParams.put(param_operatorUserInfo, operatorUserInfo);
		if (type != null) requestParams.put(param_type, type);
		if (freeDays != null) requestParams.put(param_freeDays, String.valueOf(freeDays));
		if(activationMode != null) requestParams.put(param_actMode,activationMode);

		if (userInfoMap != null)
		{
			Set<Entry<String, String>> entrySet = userInfoMap.entrySet();
			for (Entry<String, String> entry : entrySet)
				requestParams.put(param_userInfo + "_" + entry.getKey(), entry.getValue());
		}
		if (offerID != null) requestParams.put(param_offerID, offerID);
		if (packOfferID != null) requestParams.put(param_packOfferID, packOfferID);
		
		if (subscriberEndDate != null) requestParams.put(param_subscriberEndDate, dateFormat.format(subscriberEndDate));
		if (packCosId != null) requestParams.put(param_packCosId, String.valueOf(packCosId));
		if (internalRefId != null) requestParams.put(param_internalRefId, internalRefId);
		if (isAllowSmOffer != null) requestParams.put(param_isAllowSmOffer, (isAllowSmOffer ? YES : NO));
		if (upgradeGraceAndSuspended) requestParams.put(param_upgradeGraceAndSuspended, "true");
		if (suspendedUsersAllowed) requestParams.put(param_suspendedUsersAllowed, "true");
		if (isBIOffer) requestParams.put(param_bIOffer,"true");
		if (consentInd) requestParams.put(param_preConsent, "true");
		if (consentTransId != null) requestParams.put(param_transID, consentTransId);
		if (tpcgID != null) requestParams.put(iRBTConstant.EXTRA_INFO_TPCGID, tpcgID);
		if(isUnsubDelayDct) requestParams.put(param_isUnsubDelayDctReq, "true");
		if(isDelayDeactForUpgardation)requestParams.put(param_isDelayDeactForUpgardation, "true");
		if (redirectionRequired) requestParams.put(param_redirectionRequired, "true");
		if(isUdsOn)requestParams.put(param_isUdsOn, "true");
		if(isResetPassword)requestParams.put(param_isResetPassword, "true");
		if(generateRefId != null) requestParams.put(param_generateRefId, generateRefId.toString());
		if (delayDctUI) requestParams.put(param_delayDct_UI, "true");
		return requestParams;
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", calledNo = ");
		builder.append(calledNo);
		builder.append(", checkSubscriptionClass = ");
		builder.append(checkSubscriptionClass);
		builder.append(", cosID = ");
		builder.append(cosID);
		builder.append(", dontSMSInBlackOut = ");
		builder.append(dontSMSInBlackOut);
		builder.append(", freeDays = ");
		builder.append(freeDays);
		builder.append(", freePeriod = ");
		builder.append(freePeriod);
		builder.append(", gifterID = ");
		builder.append(gifterID);
		builder.append(", giftSentTime = ");
		builder.append(giftSentTime);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", isCorporateDeactivation = ");
		builder.append(isCorporateDeactivation);
		builder.append(", isDeactCorporateUser = ");
		builder.append(isDeactCorporateUser);
		builder.append(", isDirectActivation = ");
		builder.append(isDirectActivation);
		builder.append(", isDirectDeactivation = ");
		builder.append(isDirectDeactivation);
		builder.append(", isPrepaid = ");
		builder.append(isPrepaid);
		builder.append(", language = ");
		builder.append(language);
		builder.append(", lastAccessDate = ");
		builder.append(lastAccessDate);
		builder.append(", mmContext = ");
		builder.append(mmContext);
		builder.append(", offerID = ");
		builder.append(offerID);
		builder.append(", packOfferID = ");
		builder.append(packOfferID);
		builder.append(", operatorUserInfo = ");
		builder.append(operatorUserInfo);
		builder.append(", playerStatus = ");
		builder.append(playerStatus);
		builder.append(", preCharged = ");
		builder.append(preCharged);
		builder.append(", rbtType = ");
		builder.append(rbtType);
		builder.append(", rentalPack = ");
		builder.append(rentalPack);
		builder.append(", refId = ");
		builder.append(refId);
		builder.append(", scratchCardNo = ");
		builder.append(scratchCardNo);
		builder.append(", subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append(", subscriptionPeriod = ");
		builder.append(subscriptionPeriod);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", userInfoMap = ");
		builder.append(userInfoMap);
		builder.append(", activationMode = ");
		builder.append(activationMode);
		builder.append(", subscriberEndDate = ");
		builder.append(subscriberEndDate);
		builder.append(", packCosId = ");
		builder.append(packCosId);
		builder.append(", internalRefId = ");
		builder.append(internalRefId);
		builder.append(", isAllowSmOffer = ");
		builder.append(isAllowSmOffer);
		builder.append(", upgradeGraceAndSuspended = ");
		builder.append(upgradeGraceAndSuspended);
		builder.append(", suspendedUsersAllowed = ");
		builder.append(suspendedUsersAllowed);
		builder.append(", bIOffer = ");
		builder.append(isBIOffer);;
		builder.append(", redirectionRequired = ");
		builder.append(redirectionRequired);
		builder.append(", isResetPassword = ");
		builder.append(isResetPassword);
		builder.append(", generateRefId = ");
		builder.append(generateRefId);
		builder.append(", delayDctUI = ");
		builder.append(delayDctUI);
		builder.append("]");
		return builder.toString();
	}

	public boolean isBIOffer() {
		return isBIOffer;
	}

	public void setBIOffer(boolean isBIOffer) {
		this.isBIOffer = isBIOffer;
	}
	
	public boolean isConsentInd() {
		return consentInd;
	}

	public void setConsentInd(boolean consentInd) {
		this.consentInd = consentInd;
	}
	
	public String getConsentTransId() {
		return consentTransId;
	}

	public void setConsentTransId(String consentTransId) {
		this.consentTransId = consentTransId;
	}

	public boolean isRedirectionRequired() {
		return redirectionRequired;
	}

	public void setRedirectionRequired(boolean redirectionRequired) {
		this.redirectionRequired = redirectionRequired;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public Boolean getGenerateRefId() {
		return generateRefId;
	}

	public void setGenerateRefId(Boolean generateRefId) {
		this.generateRefId = generateRefId;
	}
	
	
}