package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class SelectionRequest extends SubscriptionRequest
{
	private String callerID = null;
	private String categoryID = null;
	private String clipID = null;
	private String rbtFile = null;
	private Integer fromTime = null;
	private Integer fromTimeMinutes = null;
	private Integer toTime = null;
	private Integer toTimeMinutes = null;
	private Integer status = null;
	private Integer selectionType = null;
	private String interval = null;
	private String chargeClass = null;
	private Boolean useUIChargeClass = null;
	private Boolean inLoop = null;
	private String profileHours = null;
	private String cricketPack = null;
	private Date selectionStartTime = null;
	private Date selectionEndTime = null;
	private Date setTime = null;
	private String chargingModel = null;
	private String optInOutModel = null;
	private Boolean ignoreActiveUser = null;
	private Boolean removeExistingSetting = null;
	private String transID = null;
	private String refID = null;
	private HashMap<String, String> selectionInfoMap = null;
	private String frequency = null;
	private List<Integer> clipIDs = null;
	private boolean consentInd = false;
	private String consentTransId = null;
	private String tpcgID = null;
	private String linkedRefId = null;
	private boolean makeEntryInDB = true;
	private boolean allowPremiumContent= false;
	private String ad2cUrl = null;
	private Boolean shuffleChargeClass = null;
	private String slice_duration = null;
	private Boolean allowDirectPremiumSelection = null;
	private Boolean generateRefId = null;
	private Boolean isSelDirectActivation = null;
	private String agentId = null;
	private String operatorUserType = null;
	
	/**
	 * Added by Deepak K for capturing SMS_SENT in Downloads Table. 
	 */
    private String smsSent = null;
	

	/**
	 * Added by Sreekar for Offer management
	 */
	private String subscriptionOfferID = null;
	
	/**
	 * Added by Roshan for Referral Program
	 */
	private String promoID = null;
	
	/**
	 * Added by Sharique for UDP set and unset
	 */
	private String udpId = null;

	/**
	 * @param subscriberID
	 */
	public SelectionRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param clipID
	 */
	public SelectionRequest(String subscriberID, String clipID)
	{
		super(subscriberID);
		this.clipID = clipID;
	}

	/**
	 * @param subscriberID
	 * @param categoryID
	 * @param clipID
	 */
	public SelectionRequest(String subscriberID, String categoryID, String clipID)
	{
		super(subscriberID);
		this.categoryID = categoryID;
		this.clipID = clipID;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param categoryID
	 * @param clipID
	 * @param language
	 * @param calledNo
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String categoryID, String clipID, String language, String calledNo)
	{
		super(subscriberID, isPrepaid, language, calledNo);
		this.categoryID = categoryID;
		this.clipID = clipID;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param categoryID
	 * @param clipID
	 * @param language
	 * @param calledNo
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String callerID, String mode, String categoryID, String clipID,
			String language, String calledNo)
	{
		super(subscriberID, isPrepaid, language, calledNo, mode);
		this.callerID = callerID;
		this.categoryID = categoryID;
		this.clipID = clipID;
	}

	/**
	 * @param subscriberID
	 * @param callerID
	 * @param clipID
	 * @param fromTime
	 * @param toTime
	 * @param interval
	 * @param setTime
	 */
	public SelectionRequest(String subscriberID, String callerID,
			String clipID, Integer fromTime, Integer toTime, String interval,
			Date setTime)
	{
		super(subscriberID);
		this.callerID = callerID;
		this.clipID = clipID;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.interval = interval;
		this.setTime = setTime;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param categoryID
	 * @param clipID
	 * @param language
	 * @param calledNo
	 * @param fromTime
	 * @param toTime
	 * @param status
	 * @param interval
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String callerID, String mode, String categoryID, String clipID,
			String language, String calledNo, Integer fromTime, Integer toTime,
			Integer status, String interval)
	{
		super(subscriberID, isPrepaid, language, calledNo, mode);
		this.callerID = callerID;
		this.categoryID = categoryID;
		this.clipID = clipID;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.status = status;
		this.interval = interval;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param categoryID
	 * @param clipID
	 * @param language
	 * @param calledNo
	 * @param interval
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String callerID, String mode, String categoryID, String clipID,
			String language, String calledNo, String interval)
	{
		super(subscriberID, isPrepaid, language, calledNo, mode);
		this.callerID = callerID;
		this.categoryID = categoryID;
		this.clipID = clipID;
		this.interval = interval;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param categoryID
	 * @param clipID
	 * @param language
	 * @param calledNo
	 * @param inLoop
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String callerID, String mode, String categoryID, String clipID,
			String language, String calledNo, Boolean inLoop)
	{
		super(subscriberID, isPrepaid, language, calledNo, mode);
		this.callerID = callerID;
		this.categoryID = categoryID;
		this.clipID = clipID;
		this.inLoop = inLoop;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param callerID
	 * @param mode
	 * @param modeInfo
	 * @param categoryID
	 * @param clipID
	 * @param fromTime
	 * @param toTime
	 * @param status
	 * @param interval
	 * @param subscriptionClass
	 * @param chargeClass
	 * @param inLoop
	 * @param profileHours
	 * @param cricketPack
	 * @param ignoreActiveUser
	 * @param removeExistingSetting
	 * @param subscriptionPeriod
	 */
	public SelectionRequest(String subscriberID, Boolean isPrepaid,
			String callerID, String mode, String modeInfo, String categoryID,
			String clipID, Integer fromTime, Integer toTime, Integer status,
			String interval, String subscriptionClass, String chargeClass,
			Boolean inLoop, String profileHours, String cricketPack,
			Boolean ignoreActiveUser, Boolean removeExistingSetting,
			String subscriptionPeriod)
	{
		super(subscriberID, isPrepaid, mode, modeInfo, subscriptionClass, subscriptionPeriod);
		this.callerID = callerID;
		this.categoryID = categoryID;
		this.clipID = clipID;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.status = status;
		this.interval = interval;
		this.chargeClass = chargeClass;
		this.inLoop = inLoop;
		this.profileHours = profileHours;
		this.cricketPack = cricketPack;
		this.ignoreActiveUser = ignoreActiveUser;
		this.removeExistingSetting = removeExistingSetting;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @return the categoryID
	 */
	public String getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(String categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @return the clipID
	 */
	public String getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(String clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @return the rbtFile
	 */
	public String getRbtFile()
	{
		return rbtFile;
	}

	/**
	 * @param rbtFile the rbtFile to set
	 */
	public void setRbtFile(String rbtFile)
	{
		this.rbtFile = rbtFile;
	}

	/**
	 * @return the fromTime
	 */
	public Integer getFromTime()
	{
		return fromTime;
	}

	/**
	 * @param fromTime the fromTime to set
	 */
	public void setFromTime(Integer fromTime)
	{
		this.fromTime = fromTime;
	}

	/**
	 * @return the fromTimeMinutes
	 */
	public Integer getFromTimeMinutes()
	{
		return fromTimeMinutes;
	}

	/**
	 * @param fromTimeMinutes the fromTimeMinutes to set
	 */
	public void setFromTimeMinutes(Integer fromTimeMinutes)
	{
		this.fromTimeMinutes = fromTimeMinutes;
	}

	/**
	 * @return the toTime
	 */
	public Integer getToTime()
	{
		return toTime;
	}

	/**
	 * @param toTime the toTime to set
	 */
	public void setToTime(Integer toTime)
	{
		this.toTime = toTime;
	}

	/**
	 * @return the toTimeMinutes
	 */
	public Integer getToTimeMinutes()
	{
		return toTimeMinutes;
	}

	/**
	 * @param toTimeMinutes the toTimeMinutes to set
	 */
	public void setToTimeMinutes(Integer toTimeMinutes)
	{
		this.toTimeMinutes = toTimeMinutes;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status)
	{
		this.status = status;
	}

	/**
	 * @return the selectionType
	 */
	public Integer getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType the selectionType to set
	 */
	public void setSelectionType(Integer selectionType)
	{
		this.selectionType = selectionType;
	}

	/**
	 * @return the interval
	 */
	public String getInterval()
	{
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(String interval)
	{
		this.interval = interval;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the useUIChargeClass
	 */
	public Boolean getUseUIChargeClass()
	{
		return useUIChargeClass;
	}

	/**
	 * @param useUIChargeClass the useUIChargeClass to set
	 */
	public void setUseUIChargeClass(Boolean useUIChargeClass)
	{
		this.useUIChargeClass = useUIChargeClass;
	}

	
	/**
	 * @return the chargeClassForShuffle
	 */
	public Boolean getShuffleChargeClass()
	{
		return shuffleChargeClass;
	}

	/**
	 * @param chargeClassForShuffle the chargeClassForShuffle to set
	 */
	public void setShuffleChargeClass(Boolean shuffleChargeClass)
	{
		this.shuffleChargeClass = shuffleChargeClass;
	}
	
	
	/**
	 * @return the inLoop
	 */
	public Boolean getInLoop()
	{
		return inLoop;
	}

	/**
	 * @param inLoop the inLoop to set
	 */
	public void setInLoop(Boolean inLoop)
	{
		this.inLoop = inLoop;
	}

	/**
	 * @return the profileHours
	 */
	public String getProfileHours()
	{
		return profileHours;
	}

	/**
	 * @param profileHours the profileHours to set
	 */
	public void setProfileHours(String profileHours)
	{
		this.profileHours = profileHours;
	}

	/**
	 * @return the cricketPack
	 */
	public String getCricketPack()
	{
		return cricketPack;
	}

	/**
	 * @param cricketPack the cricketPack to set
	 */
	public void setCricketPack(String cricketPack)
	{
		this.cricketPack = cricketPack;
	}

	/**
	 * @return the selectionStartTime
	 */
	public Date getSelectionStartTime()
	{
		return selectionStartTime;
	}

	/**
	 * @param selectionStartTime the selectionStartTime to set
	 */
	public void setSelectionStartTime(Date selectionStartTime)
	{
		this.selectionStartTime = selectionStartTime;
	}

	/**
	 * @return the selectionEndTime
	 */
	public Date getSelectionEndTime()
	{
		return selectionEndTime;
	}

	/**
	 * @param selectionEndTime the selectionEndTime to set
	 */
	public void setSelectionEndTime(Date selectionEndTime)
	{
		this.selectionEndTime = selectionEndTime;
	}

	/**
	 * @return the setTime
	 */
	public Date getSetTime()
	{
		return setTime;
	}

	/**
	 * @param setTime the setTime to set
	 */
	public void setSetTime(Date setTime)
	{
		this.setTime = setTime;
	}

	/**
	 * @return the chargingModel
	 */
	public String getChargingModel()
	{
		return chargingModel;
	}

	/**
	 * @param chargingModel the chargingModel to set
	 */
	public void setChargingModel(String chargingModel)
	{
		this.chargingModel = chargingModel;
	}

	/**
	 * @return the optInOutModel
	 */
	public String getOptInOutModel()
	{
		return optInOutModel;
	}

	/**
	 * @param optInOutModel the optInOutModel to set
	 */
	public void setOptInOutModel(String optInOutModel)
	{
		this.optInOutModel = optInOutModel;
	}

	/**
	 * @return the ignoreActiveUser
	 */
	public Boolean getIgnoreActiveUser()
	{
		return ignoreActiveUser;
	}

	/**
	 * @param ignoreActiveUser the ignoreActiveUser to set
	 */
	public void setIgnoreActiveUser(Boolean ignoreActiveUser)
	{
		this.ignoreActiveUser = ignoreActiveUser;
	}

	/**
	 * @return the removeExistingSetting
	 */
	public Boolean getRemoveExistingSetting()
	{
		return removeExistingSetting;
	}

	/**
	 * @param removeExistingSetting the removeExistingSetting to set
	 */
	public void setRemoveExistingSetting(Boolean removeExistingSetting)
	{
		this.removeExistingSetting = removeExistingSetting;
	}

	/**
	 * @return the transID
	 */
	public String getTransID()
	{
		return transID;
	}

	/**
	 * @param transID the transID to set
	 */
	public void setTransID(String transID)
	{
		this.transID = transID;
	}

	/**
	 * @return the refID
	 */
	public String getRefID()
	{
		return refID;
	}

	public String getPromoID() {
		return promoID;
	}

	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}

	/**
	 * @param refID the refID to set
	 */
	public void setRefID(String refID)
	{
		this.refID = refID;
	}

	/**
	 * @return the selectionInfoMap
	 */
	public HashMap<String, String> getSelectionInfoMap()
	{
		return selectionInfoMap;
	}

	/**
	 * @param selectionInfoMap the selectionInfoMap to set
	 */
	public void setSelectionInfoMap(HashMap<String, String> selectionInfoMap)
	{
		this.selectionInfoMap = selectionInfoMap;
	}

	/**
	 * @return the subscriptionOfferID
	 */
	public String getSubscriptionOfferID()
	{
		return subscriptionOfferID;
	}

	/**
	 * @param subscriptionOfferID the subscriptionOfferID to set
	 */
	public void setSubscriptionOfferID(String subscriptionOfferID)
	{
		this.subscriptionOfferID = subscriptionOfferID;
	}
	
	/**
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the clipIDs
	 */
	public List<Integer> getClipIDs()
	{
		return clipIDs;
	}

	/**
	 * @param clipIDs the clipIDs to set
	 */
	public void setClipIDs(List<Integer> clipIDs)
	{
		this.clipIDs = clipIDs;
	}
	
	
	public String getLinkedRefId() {
		return linkedRefId;
	}

	public void setLinkedRefId(String linkedRefId) {
		this.linkedRefId = linkedRefId;
	}

	
	public boolean isMakeEntryInDB() {
		return makeEntryInDB;
	}

	public void setMakeEntryInDB(boolean makeEntryInDB) {
		this.makeEntryInDB = makeEntryInDB;
	}

	public String getAd2cUrl() {
		return ad2cUrl;
	}

	public void setAd2cUrl(String ad2cUrl) {
		this.ad2cUrl = ad2cUrl;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#prepareRequestParams()
	 */
	@Override
	public void prepareRequestParams(WebServiceContext task)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		super.prepareRequestParams(task);
		
		if(task.containsKey(param_callerID))
		{
			callerID = task.getString(param_callerID);
		}
		if(task.containsKey(param_categoryID))
		{
			categoryID = task.getString(param_categoryID);
		}
		if(task.containsKey(param_promoID))
		{
			promoID = task.getString(param_promoID);
		}
		if (task.containsKey(param_clipID)) 
		{
			clipID = task.getString(param_clipID);
		}
		if (task.containsKey(param_rbtFile)) 
		{
			rbtFile = task.getString(param_rbtFile);
		}
		if (task.containsKey(param_fromTime)) 
		{
			fromTime = Integer.parseInt(task.getString(param_fromTime).trim());
		}
		if (task.containsKey(param_fromTimeMinutes)) 
		{
			fromTimeMinutes = Integer.parseInt(task.getString(
					param_fromTimeMinutes).trim());
		}
		if (task.containsKey(param_toTime)) 
		{
			toTime = Integer.parseInt(task.getString(param_toTime));
		}
		if (task.containsKey(param_toTimeMinutes)) 
		{
			toTimeMinutes = Integer.parseInt(task
					.getString(param_toTimeMinutes));
		}
		if (task.containsKey(param_status)) 
		{
			status = Integer.parseInt(task.getString(param_status));
		}
		if (task.containsKey(param_selectionType)) 
		{
			selectionType = Integer.parseInt(task
					.getString(param_selectionType));
		}
		if (task.containsKey(param_interval)) 
		{
			interval = task.getString(param_interval);
		}
		if (task.containsKey(param_chargeClass)) 
		{
			chargeClass = task.getString(param_chargeClass);
		}
		if (task.containsKey(param_useUIChargeClass)) 
		{
			useUIChargeClass = task.getString(param_useUIChargeClass)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_shuffleChargeClass)) 
		{
			shuffleChargeClass = task.getString(param_shuffleChargeClass)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_inLoop)) 
		{
			inLoop = task.getString(param_inLoop).equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_profileHours)) 
		{
			profileHours = task.getString(param_profileHours);
		}
		if (task.containsKey(param_cricketPack)) 
		{
			cricketPack = task.getString(param_cricketPack);
		}
		try 
		{
			if (task.containsKey(param_selectionStartTime)) {
				selectionStartTime = dateFormat.parse(task
						.getString(param_selectionStartTime));
			}
			if (task.containsKey(param_selectionEndTime)) {
				selectionEndTime = dateFormat.parse(task
						.getString(param_selectionEndTime));
			}
			if (task.containsKey(param_setTime)) {
				setTime = dateFormat.parse(task.getString(param_setTime));
			}
		} 
		catch (ParseException pe) 
		{

		}
		if (task.containsKey(param_chargingModel)) 
		{
			chargingModel = task.getString(param_chargingModel);
		}
		if (task.containsKey(param_optInOutModel)) 
		{
			optInOutModel = task.getString(param_optInOutModel);
		}
		if (task.containsKey(param_ignoreActiveUser)) 
		{
			ignoreActiveUser = task.getString(param_ignoreActiveUser).equals(
					YES);
		}
		if (task.containsKey(param_removeExistingSetting)) 
		{
			removeExistingSetting = task.getString(param_removeExistingSetting)
					.equalsIgnoreCase(YES);
		}
		if (task.containsKey(param_transID)) 
		{
			transID = task.getString(param_transID);
		}
		if (task.containsKey(param_refID)) 
		{
			refID = task.getString(param_refID);
		}
		if (task.containsKey(param_subscriptionOfferID)) 
		{
			subscriptionOfferID = task.getString(param_subscriptionOfferID);
		}
		if (task.containsKey(param_frequency)) 
		{
			frequency = task.getString(param_frequency);
		}
		if(task.containsKey(param_smsSent)){
			smsSent = task.getString(param_smsSent);
		}

		Set<String> keys = task.keySet();
		for(String key : keys)
		{
			if(key.startsWith(param_selectionInfo + "_"))
			{
				String temp = key.substring((param_selectionInfo + "_").length());
				if(selectionInfoMap == null){
					selectionInfoMap = new HashMap<String, String>();
				}
				selectionInfoMap.put(temp,task.getString(key));
			}
		}
		
		if(task.containsKey(param_linkedRefId)) {
			linkedRefId = task.getString(param_linkedRefId);
		}
		
		if(task.containsKey(param_makeEntryInDB)) {
			makeEntryInDB = Boolean.parseBoolean(task.get(param_makeEntryInDB).toString());
		}
		
		if(task.containsKey(param_ad2cUrl)) {
			ad2cUrl = task.getString(param_ad2cUrl);
		}
		
		if(task.containsKey(param_allowPremiumContent)){
			allowPremiumContent = true;
		}
		
		if(task.containsKey(param_slice_duration)) {
			slice_duration = task.getString(param_slice_duration);
		}
		if (task.containsKey(param_allowDirectPremiumSelection)) {
			allowDirectPremiumSelection = Boolean.parseBoolean(task.getString(param_allowDirectPremiumSelection));
		}
		
		if (task.containsKey(param_generateRefId)) {
			generateRefId = Boolean.parseBoolean(task.getString(param_generateRefId));
		} 
		
		if (task.containsKey(param_udpId))
			udpId = task.getString(param_udpId);
		if (task.containsKey(param_selDirectActivation))
			isSelDirectActivation = task.getString(param_selDirectActivation).equalsIgnoreCase(YES);
	}


	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (callerID != null) requestParams.put(param_callerID, callerID);
		if (categoryID != null) requestParams.put(param_categoryID, categoryID);
		if (clipID != null) requestParams.put(param_clipID, clipID);
		if (rbtFile != null) requestParams.put(param_rbtFile, rbtFile);
		if (promoID != null) requestParams.put(param_promoID, promoID);
		if (fromTime != null) requestParams.put(param_fromTime, String.valueOf(fromTime));
		if (fromTimeMinutes != null) requestParams.put(param_fromTimeMinutes, String.valueOf(fromTimeMinutes));
		if (toTime != null) requestParams.put(param_toTime, String.valueOf(toTime));
		if (toTimeMinutes != null) requestParams.put(param_toTimeMinutes, String.valueOf(toTimeMinutes));
		if (status != null) requestParams.put(param_status, String.valueOf(status));
		if (selectionType != null) requestParams.put(param_selectionType, String.valueOf(selectionType));
		if (agentId != null) requestParams.put(param_agentId, String.valueOf(agentId));
		if (interval != null) requestParams.put(param_interval, interval);
		if (chargeClass != null) requestParams.put(param_chargeClass, chargeClass);
		if (useUIChargeClass != null) requestParams.put(param_useUIChargeClass, (useUIChargeClass ? YES : NO));
		if (inLoop != null) requestParams.put(param_inLoop, (inLoop ? YES : NO));
		if (profileHours != null) requestParams.put(param_profileHours, profileHours);
		if (cricketPack != null) requestParams.put(param_cricketPack, cricketPack);
		if (selectionStartTime != null) requestParams.put(param_selectionStartTime, dateFormat.format(selectionStartTime));
		if (selectionEndTime != null) requestParams.put(param_selectionEndTime, dateFormat.format(selectionEndTime));
		if (setTime != null) requestParams.put(param_setTime, dateFormat.format(setTime));
		if (chargingModel != null) requestParams.put(param_chargingModel, chargingModel);
		if (optInOutModel != null) requestParams.put(param_optInOutModel, optInOutModel);
		if (ignoreActiveUser != null) requestParams.put(param_ignoreActiveUser, (ignoreActiveUser ? YES : NO));
		if (removeExistingSetting != null) requestParams.put(param_removeExistingSetting, (removeExistingSetting ? YES : NO));
		if (transID != null) requestParams.put(param_transID, transID);
		if (refID != null) requestParams.put(param_refID, refID);
		if (frequency != null) requestParams.put(param_frequency, frequency);
		if (consentInd) requestParams.put(param_preConsent, "true");
		if (consentTransId != null) requestParams.put(param_transID, consentTransId);
		if (shuffleChargeClass != null) requestParams.put(param_shuffleChargeClass, (shuffleChargeClass ? YES : NO));
		if (selectionInfoMap != null)
		{
			Set<Entry<String, String>> entrySet = selectionInfoMap.entrySet();
			for (Entry<String, String> entry : entrySet)
				requestParams.put(param_selectionInfo + "_" + entry.getKey(), entry.getValue());
		}
		if (subscriptionOfferID != null) requestParams.put(param_subscriptionOfferID, subscriptionOfferID);

		if (clipIDs != null && clipIDs.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			for (Integer clipID : clipIDs)
			{
				builder.append(clipID).append(",");
			}

			requestParams.put(param_clipID, builder.substring(0, builder.length() - 1));
		}
		
		if(smsSent!=null)requestParams.put(param_smsSent,smsSent);
        if(tpcgID != null) requestParams.put(iRBTConstant.EXTRA_INFO_TPCGID,tpcgID);
        if(linkedRefId != null) requestParams.put(param_linkedRefId, linkedRefId);
        if(allowPremiumContent) requestParams.put(param_allowPremiumContent, "y");
        requestParams.put(param_makeEntryInDB, String.valueOf(makeEntryInDB));
        if(ad2cUrl != null) requestParams.put(param_ad2cUrl, ad2cUrl);
        if(slice_duration != null) requestParams.put(param_slice_duration, slice_duration);
        if(allowDirectPremiumSelection != null) requestParams.put(param_allowDirectPremiumSelection, allowDirectPremiumSelection.toString());
        if(generateRefId != null) requestParams.put(param_generateRefId, generateRefId.toString());
        if(udpId != null) requestParams.put(param_udpId, udpId);
        //RBT-16238 Getting NullPointerException while doing song selection
        if(isSelDirectActivation != null && isSelDirectActivation.booleanValue()) requestParams.put(param_selDirectActivation, YES);
        if(operatorUserType!=null) requestParams.put(param_operatorUserType, operatorUserType);
        
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String superString = super.toString();
		superString = superString.substring(superString.indexOf('[') + 1);

		StringBuilder builder = new StringBuilder();
		builder.append("SelectionRequest[callerID = ");
		builder.append(callerID);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", chargingModel = ");
		builder.append(chargingModel);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", cricketPack = ");
		builder.append(cricketPack);
		builder.append(", fromTime = ");
		builder.append(fromTime);
		builder.append(", fromTimeMinutes = ");
		builder.append(fromTimeMinutes);
		builder.append(", ignoreActiveUser = ");
		builder.append(ignoreActiveUser);
		builder.append(", inLoop = ");
		builder.append(inLoop);
		builder.append(", promoID = ");
		builder.append(promoID);
		builder.append(", interval = ");
		builder.append(interval);
		builder.append(", optInOutModel = ");
		builder.append(optInOutModel);
		builder.append(", profileHours = ");
		builder.append(profileHours);
		builder.append(", rbtFile = ");
		builder.append(rbtFile);
		builder.append(", refID = ");
		builder.append(refID);
		builder.append(", removeExistingSetting = ");
		builder.append(removeExistingSetting);
		builder.append(", selectionEndTime = ");
		builder.append(selectionEndTime);
		builder.append(", selectionInfoMap = ");
		builder.append(selectionInfoMap);
		builder.append(", selectionStartTime = ");
		builder.append(selectionStartTime);
		builder.append(", selectionType = ");
		builder.append(selectionType);
		builder.append(", setTime = ");
		builder.append(setTime);
		builder.append(", status = ");
		builder.append(status);
		builder.append(", subscriptionOfferID = ");
		builder.append(subscriptionOfferID);
		builder.append(", toTime = ");
		builder.append(toTime);
		builder.append(", toTimeMinutes = ");
		builder.append(toTimeMinutes);
		builder.append(", transID = ");
		builder.append(transID);
		builder.append(", useUIChargeClass = ");
		builder.append(useUIChargeClass);
		builder.append(", shuffleChargeClass = ");
		builder.append(shuffleChargeClass);
		builder.append(", frequency = ");
		builder.append(frequency);
		builder.append(", clipIDs = ");
		builder.append(clipIDs);
		builder.append(", SMSKEY = ");
		builder.append(smsSent);
		builder.append(", linkedRefId = ");
		builder.append(linkedRefId);
		builder.append(", makeEntryInDB = ");
		builder.append(makeEntryInDB);
		builder.append(",allowPremiumContent");
		builder.append(allowPremiumContent);
		builder.append(", ad2cUrl = ");
		builder.append(ad2cUrl);
		builder.append(", slice_duration = ");
		builder.append(slice_duration);
		builder.append(", allowDirectPremiumSelection = ");
		builder.append(allowDirectPremiumSelection);
		builder.append(", generateRefId = ");
		builder.append(generateRefId);
		builder.append("] ");

		builder.append(superString);
		return builder.toString();
	}

	public String getSmsSent() {
		return smsSent;
	}

	public void setSmsSent(String smsSent) {
		this.smsSent = smsSent;
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

	public String getTpcgID() {
		return tpcgID;
	}

	public void setTpcgID(String tpcgID) {
		this.tpcgID = tpcgID;
	}
	
	public boolean isAllowPremiumContent() {
		return allowPremiumContent;
	}

	public void setAllowPremiumContent(boolean allowPremiumContent) {
		this.allowPremiumContent = allowPremiumContent;
	}

	public String getSlice_duration() {
		return slice_duration;
	}

	public void setSlice_duration(String slice_duration) {
		this.slice_duration = slice_duration;
	}

	public Boolean getAllowDirectPremiumSelection() {
		return allowDirectPremiumSelection;
	}

	public void setAllowDirectPremiumSelection(Boolean allowDirectPremiumSelection) {
		this.allowDirectPremiumSelection = allowDirectPremiumSelection;
	}

	public Boolean getGenerateRefId() {
		return generateRefId;
	}

	public void setGenerateRefId(Boolean generateRefId) {
		this.generateRefId = generateRefId;
	}

	public String getUdpId() {
		return udpId;
	}

	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}

	public Boolean getIsSelDirectActivation() {
		return isSelDirectActivation;
	}

	public void setIsSelDirectActivation(Boolean isSelDirectActivation) {
		this.isSelDirectActivation = isSelDirectActivation;
	}

	public String getOperatorUserType() {
		return operatorUserType;
	}

	public void setOperatorUserType(String operatorUserType) {
		this.operatorUserType = operatorUserType;
	}
	
}
