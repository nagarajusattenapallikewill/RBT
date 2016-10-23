package com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans;

import java.util.HashMap;

public class SubscriptionBean
{
	private String subId = null;
	private String subcriptionClass = null;
	private HashMap<String, String> extraInfo = null;
	private String language = null;
	private Boolean disableIntroPrompt = null;
	private Boolean prepaidToPostpaidFlag = null;
	private Boolean isPrepaid = null;
	private Boolean pollOn = null;
	private int age = -1;
	private String gender = null;
	private Boolean blackListed = null;
	private Boolean newsletterOn = null;
	private Boolean overlayOn = null;
	private String type = null;
	private String blackListType = null;
	private String cosId = null;
	private String circleID = null;
	private String operatorUserInfo = null;
	private String subOfferId = null;

	public String getBlackListType()
	{
		return blackListType;
	}

	public void setBlackListType(String blackListType)
	{
		this.blackListType = blackListType;
	}

	public String getCosId()
	{
		return cosId;
	}

	public void setCosId(String cosId)
	{
		this.cosId = cosId;
	}

	public Boolean getDisableIntroPrompt()
	{
		return disableIntroPrompt;
	}

	public void setDisableIntroPrompt(Boolean disableIntroPrompt)
	{
		this.disableIntroPrompt = disableIntroPrompt;
	}

	public Boolean getPrepaidToPostpaidFlag()
	{
		return prepaidToPostpaidFlag;
	}

	public void setPrepaidToPostpaidFlag(Boolean prepaidToPostpaidFlag)
	{
		this.prepaidToPostpaidFlag = prepaidToPostpaidFlag;
	}

	public Boolean getIsPrepaid()
	{
		return isPrepaid;
	}

	public void setIsPrepaid(Boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	public Boolean getPollOn()
	{
		return pollOn;
	}

	public void setPollOn(Boolean pollOn)
	{
		this.pollOn = pollOn;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public String getGender()
	{
		return gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public Boolean getBlackListed()
	{
		return blackListed;
	}

	public void setBlackListed(Boolean blackListed)
	{
		this.blackListed = blackListed;
	}

	public Boolean getNewsletterOn()
	{
		return newsletterOn;
	}

	public void setNewsletterOn(Boolean newsletterOn)
	{
		this.newsletterOn = newsletterOn;
	}

	public Boolean getOverlayOn()
	{
		return overlayOn;
	}

	public void setOverlayOn(Boolean overlayOn)
	{
		this.overlayOn = overlayOn;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getSubId()
	{
		return subId;
	}

	public void setSubId(String subId)
	{
		this.subId = subId;
	}

	public String getSubcriptionClass()
	{
		return subcriptionClass;
	}

	public void setSubcriptionClass(String subcriptionClass)
	{
		this.subcriptionClass = subcriptionClass;
	}

	public HashMap<String, String> getExtraInfo()
	{
		return extraInfo;
	}

	public void setExtraInfo(HashMap<String, String> extraInfo)
	{
		this.extraInfo = extraInfo;
	}

	public String getLanguage()
	{
		return language;
	}

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
	 * @param circleID
	 *            the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the operatorUserInfo
	 */
	public String getOperatorUserInfo()
	{
		return operatorUserInfo;
	}

	/**
	 * @param operatorUserInfo
	 *            the operatorUserInfo to set
	 */
	public void setOperatorUserInfo(String operatorUserInfo)
	{
		this.operatorUserInfo = operatorUserInfo;
	}

	public String getSubOfferId() {
		return subOfferId;
	}

	public void setSubOfferId(String subOfferId) {
		this.subOfferId = subOfferId;
	}
	
	
}
