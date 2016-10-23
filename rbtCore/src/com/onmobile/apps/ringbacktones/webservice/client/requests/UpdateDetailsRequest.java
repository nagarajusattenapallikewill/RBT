package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class UpdateDetailsRequest extends Request
{
	private String language = null;
	private Integer age = null;
	private String gender = null;
	private Boolean isNewsLetterOn = null;
	private Boolean isPrepaid = null;
	private Boolean isPressStarIntroEnabled = null;
	private Boolean isPollOn = null;
	private Boolean isOverlayOn = null;
	private Boolean isUdsOn = null;
	private Boolean isBlacklisted = null;
	private String blacklistType = null;
	private String type = null;
	private Boolean isUserLocked = null; 
	private String newSubscriberID = null;
	private Integer clipID = null;
	private String dtmfInputKeys = null;
	private String udsType=null;
	
	public String getDtmfInputKeys() {
		return dtmfInputKeys;
	}

	public void setDtmfInputKeys(String dtmfInputKeys) {
		this.dtmfInputKeys = dtmfInputKeys;
	}

	/**
	 * @param subscriberID
	 */
	public UpdateDetailsRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param language
	 */
	public UpdateDetailsRequest(String subscriberID, String language)
	{
		super(subscriberID);
		this.language = language;
	}

	/**
	 * @param subscriberID
	 * @param age
	 */
	public UpdateDetailsRequest(String subscriberID, Integer age)
	{
		super(subscriberID);
		this.age = age;
	}

	/**
	 * @param subscriberID
	 * @param isNewsLetterOn
	 */
	public UpdateDetailsRequest(String subscriberID, Boolean isNewsLetterOn)
	{
		super(subscriberID);
		this.isNewsLetterOn = isNewsLetterOn;
	}

	/**
	 * @param subscriberID
	 * @param isPrepaid
	 * @param isPressStarIntroEnabled
	 */
	public UpdateDetailsRequest(String subscriberID, Boolean isPrepaid, Boolean isPressStarIntroEnabled)
	{
		super(subscriberID);
		this.isPrepaid = isPrepaid;
		this.isPressStarIntroEnabled = isPressStarIntroEnabled;
	}

	/**
	 * @param subscriberID
	 * @param isBlacklisted
	 * @param blacklistType
	 */
	public UpdateDetailsRequest(String subscriberID, Boolean isBlacklisted,
			String blacklistType)
	{
		super(subscriberID);
		this.isBlacklisted = isBlacklisted;
		this.blacklistType = blacklistType;
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
	 * @return the age
	 */
	public Integer getAge()
	{
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(Integer age)
	{
		this.age = age;
	}

	/**
	 * @return the gender
	 */
	public String getGender()
	{
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender)
	{
		this.gender = gender;
	}

	/**
	 * @return the isNewsLetterOn
	 */
	public Boolean getIsNewsLetterOn()
	{
		return isNewsLetterOn;
	}

	/**
	 * @param isNewsLetterOn the isNewsLetterOn to set
	 */
	public void setIsNewsLetterOn(Boolean isNewsLetterOn)
	{
		this.isNewsLetterOn = isNewsLetterOn;
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
	 * @return the isPressStarIntroEnabled
	 */
	public Boolean getIsPressStarIntroEnabled()
	{
		return isPressStarIntroEnabled;
	}

	/**
	 * @param isPressStarIntroEnabled the isPressStarIntroEnabled to set
	 */
	public void setIsPressStarIntroEnabled(Boolean isPressStarIntroEnabled)
	{
		this.isPressStarIntroEnabled = isPressStarIntroEnabled;
	}

	/**
	 * @return the isPollOn
	 */
	public Boolean getIsPollOn()
	{
		return isPollOn;
	}

	/**
	 * @param isPollOn the isPollOn to set
	 */
	public void setIsPollOn(Boolean isPollOn)
	{
		this.isPollOn = isPollOn;
	}

	/**
	 * @return the isOverlayOn
	 */
	public Boolean getIsOverlayOn()
	{
		return isOverlayOn;
	}

	/**
	 * @param isOverlayOn the isOverlayOn to set
	 */
	public void setIsOverlayOn(Boolean isOverlayOn)
	{
		this.isOverlayOn = isOverlayOn;
	}

	/**
	 * @return the isUdsOn
	 */
	public Boolean getIsUdsOn()
	{
		return isUdsOn;
	}

	/**
	 * @param isUdsOn the isUdsOn to set
	 */
	public void setIsUdsOn(Boolean isUdsOn)
	{
		this.isUdsOn = isUdsOn;
	}

	/**
	 * @return the isBlacklisted
	 */
	public Boolean getIsBlacklisted()
	{
		return isBlacklisted;
	}

	/**
	 * @param isBlacklisted the isBlacklisted to set
	 */
	public void setIsBlacklisted(Boolean isBlacklisted)
	{
		this.isBlacklisted = isBlacklisted;
	}

	/**
	 * @return the blacklistType
	 */
	public String getBlacklistType()
	{
		return blacklistType;
	}

	/**
	 * @param blacklistType the blacklistType to set
	 */
	public void setBlacklistType(String blacklistType)
	{
		this.blacklistType = blacklistType;
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
	 * @return the isUserLocked
	 */
	public Boolean getIsUserLocked()
	{
		return isUserLocked;
	}

	/**
	 * @param isUserLocked the isUserLocked to set
	 */
	public void setIsUserLocked(Boolean isUserLocked)
	{
		this.isUserLocked = isUserLocked;
	}

	/**
	 * @return the newSubscriberID
	 */
	public String getNewSubscriberID()
	{
		return newSubscriberID;
	}

	/**
	 * @param newSubscriberID the newSubscriberID to set
	 */
	public void setNewSubscriberID(String newSubscriberID)
	{
		this.newSubscriberID = newSubscriberID;
	}

	/**
	 * @return the clipID
	 */
	public Integer getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(Integer clipID)
	{
		this.clipID = clipID;
	}
	
	public String getUdsType() {
		return udsType;
	}

	public void setUdsType(String udsType) {
		this.udsType = udsType;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (language != null) requestParams.put(param_language, language);
		if (age != null) requestParams.put(param_age, String.valueOf(age));
		if (gender != null) requestParams.put(param_gender, gender);
		if (isNewsLetterOn != null) requestParams.put(param_isNewsLetterOn, (isNewsLetterOn ? YES : NO));
		if (isPrepaid != null) requestParams.put(param_isPrepaid, (isPrepaid ? YES : NO));
		if (isPressStarIntroEnabled != null) requestParams.put(param_isPressStarIntroEnabled, (isPressStarIntroEnabled ? YES : NO));
		if (isPollOn != null) requestParams.put(param_isPollOn, (isPollOn ? YES : NO));
		if (isOverlayOn != null) requestParams.put(param_isOverlayOn, (isOverlayOn ? YES : NO));
		if (isUdsOn != null) requestParams.put(param_isUdsOn, (isUdsOn ? YES : NO));
		if (isBlacklisted != null) requestParams.put(param_isBlacklisted, (isBlacklisted ? YES : NO));
		if (blacklistType != null) requestParams.put(param_blacklistType, blacklistType);
		if (type != null) requestParams.put(param_type, type);
		if (isUserLocked != null) requestParams.put(param_userLocked, isUserLocked ? YES : NO);
		if (newSubscriberID != null) requestParams.put(param_newSubscriberID, newSubscriberID);
		if (clipID != null) requestParams.put(param_clipID, String.valueOf(clipID));
		if (dtmfInputKeys != null) requestParams.put(param_dtmfInputKeys, String.valueOf(dtmfInputKeys));
		if (udsType != null) requestParams.put(param_udsType, udsType);
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("UpdateDetailsRequest [browsingLanguage=");
		builder.append(browsingLanguage);
		builder.append(", circleID=");
		builder.append(circleID);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", modeInfo=");
		builder.append(modeInfo);
		builder.append(", onlyResponse=");
		builder.append(onlyResponse);
		builder.append(", operatroID=");
		builder.append(operatroID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", age=");
		builder.append(age);
		builder.append(", blacklistType=");
		builder.append(blacklistType);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", gender=");
		builder.append(gender);
		builder.append(", isBlacklisted=");
		builder.append(isBlacklisted);
		builder.append(", isNewsLetterOn=");
		builder.append(isNewsLetterOn);
		builder.append(", isOverlayOn=");
		builder.append(isOverlayOn);
		builder.append(", isPollOn=");
		builder.append(isPollOn);
		builder.append(", isPrepaid=");
		builder.append(isPrepaid);
		builder.append(", isPressStarIntroEnabled=");
		builder.append(isPressStarIntroEnabled);
		builder.append(", isUdsOn=");
		builder.append(isUdsOn);
		builder.append(", isUserLocked=");
		builder.append(isUserLocked);
		builder.append(", language=");
		builder.append(language);
		builder.append(", newSubscriberID=");
		builder.append(newSubscriberID);
		builder.append(", type=");
		builder.append(type);
		builder.append(", dtmfInputKeys=");
		builder.append(dtmfInputKeys);
		builder.append(", udsType=");
		builder.append(udsType);		
		builder.append("]");
		return builder.toString();
	}
}
