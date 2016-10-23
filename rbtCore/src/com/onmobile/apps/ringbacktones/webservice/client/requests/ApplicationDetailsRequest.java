/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @author vinayasimha.patil
 *
 */
public class ApplicationDetailsRequest extends Request
{
	private String info = null;
	private String type = null;
	private String name = null;
	private String value = null;
	private String range = null;
	private Date playDate = null;
	private Date startDate = null;
	private Date endDate = null;
	private String clipID = null;
	private Integer categoryID = null;
	private String userType = null;
	private String profile = null;
	private String userID = null;
	private String newUserID = null;
	private String password = null;
	private HashMap<String, String> userInfo = null;
	private String siteName = null;
	private String[] sitePrefixes = null;
	private String siteURL = null;
	private Boolean accessAllowed;
	private String[] supportedLanguages = null;
	private String playerURL = null;
	private String playUnchargedFor = null;
	private String prepaidSuccessSms = null;
	private String prepaidFailureSms = null;
	private String postpaidSuccessSms = null;
	private String postpaidFailureSms = null;
	private String prepaidNEFSuccessSms = null;
	private String prepaidRenewalSuccessSms = null;
	private String prepaidRenewalFailureSms = null;
	private String postpaidRenewalSuccessSms = null;
	private String postpaidRenewalFailureSms = null;
	private String pack = null;
	private Boolean isPrepaid = null;
	private String retailerID = null;
	private Integer cosID = null;
	private Boolean encryptPassword = null;
	private Boolean doSubscriberValidation = null;
	private String language = null;
	private String smsText = null;
	private String tittle = null;
	private String pageNo = null;
	private String oldPassword = null;
	private String osType = null;
	private boolean redirectionRequired = false;
	private String callerID = null;

	/**
	 * 
	 */
	public ApplicationDetailsRequest()
	{
		super(null);
	}

	/**
	 * @param type
	 */
	public ApplicationDetailsRequest(String type)
	{
		super(null);
		this.type = type;
	}

	/**
	 * @param type
	 * @param name
	 * @param value
	 */
	public ApplicationDetailsRequest(String type, String name, String value)
	{
		super(null);
		this.type = type;
		this.name = name;
		this.value = value;
	}

	/**
	 * @param range
	 * @param circleID
	 */
	public ApplicationDetailsRequest(String range, String circleID)
	{
		super(null, circleID);
		this.range = range;
	}

	/**
	 * @param playDate
	 * @param clipID
	 * @param categoryID
	 * @param circleID
	 * @param userType
	 * @param profile
	 */
	public ApplicationDetailsRequest(Date playDate, String clipID,
			Integer categoryID, String circleID, String userType, String profile)
	{
		super(null, circleID);
		this.playDate = playDate;
		this.clipID = clipID;
		this.categoryID = categoryID;
		this.userType = userType;
		this.profile = profile;
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @param clipID
	 * @param categoryID
	 * @param circleID
	 * @param userType
	 * @param profile
	 */
	public ApplicationDetailsRequest(Date startDate, Date endDate,
			String clipID, Integer categoryID, String circleID,
			String userType, String profile)
	{
		super(null, circleID);
		this.startDate = startDate;
		this.endDate = endDate;
		this.clipID = clipID;
		this.categoryID = categoryID;
		this.userType = userType;
		this.profile = profile;
	}

	/**
	 * @param subscriberID
	 * @param type
	 * @param userID
	 * @param password
	 * @param userInfo
	 */
	public ApplicationDetailsRequest(String subscriberID, String type,
			String userID, String password, HashMap<String, String> userInfo)
	{
		super(subscriberID);
		this.type = type;
		this.userID = userID;
		this.password = password;
		this.userInfo = userInfo;
	}

	/**
	 * @param circleID
	 * @param siteName
	 * @param sitePrefixes
	 * @param siteURL
	 * @param accessAllowed
	 * @param supportedLanguages
	 * @param playerURL
	 * @param playUncharged
	 */
	public ApplicationDetailsRequest(String circleID, String siteName,
			String[] sitePrefixes, String siteURL, Boolean accessAllowed,
			String[] supportedLanguages, String playerURL, String playUnchargedFor)
	{
		super(null, circleID);
		this.siteName = siteName;
		this.sitePrefixes = sitePrefixes;
		this.siteURL = siteURL;
		this.accessAllowed = accessAllowed;
		this.supportedLanguages = supportedLanguages;
		this.playerURL = playerURL;
		this.playUnchargedFor = playUnchargedFor;
	}

	/**
	 * @param type
	 * @param name
	 * @param prepaidSuccessSms
	 * @param prepaidFailureSms
	 * @param postpaidSuccessSms
	 * @param postpaidFailureSms
	 * @param prepaidNEFSuccessSms
	 * @param prepaidRenewalSuccessSms
	 * @param prepaidRenewalFailureSms
	 * @param postpaidRenewalSuccessSms
	 * @param postpaidRenewalFailureSms
	 */
	public ApplicationDetailsRequest(String type, String name,
			String prepaidSuccessSms, String prepaidFailureSms,
			String postpaidSuccessSms, String postpaidFailureSms,
			String prepaidNEFSuccessSms, String prepaidRenewalSuccessSms,
			String prepaidRenewalFailureSms, String postpaidRenewalSuccessSms,
			String postpaidRenewalFailureSms)
	{
		super(null);
		this.type = type;
		this.name = name;
		this.prepaidSuccessSms = prepaidSuccessSms;
		this.prepaidFailureSms = prepaidFailureSms;
		this.postpaidSuccessSms = postpaidSuccessSms;
		this.postpaidFailureSms = postpaidFailureSms;
		this.prepaidNEFSuccessSms = prepaidNEFSuccessSms;
		this.prepaidRenewalSuccessSms = prepaidRenewalSuccessSms;
		this.prepaidRenewalFailureSms = prepaidRenewalFailureSms;
		this.postpaidRenewalSuccessSms = postpaidRenewalSuccessSms;
		this.postpaidRenewalFailureSms = postpaidRenewalFailureSms;
	}

	/**
	 * @param circleID
	 * @param pack
	 * @param isPrepaid
	 */
	public ApplicationDetailsRequest(String circleID, String pack, Boolean isPrepaid)
	{
		super(null, circleID);
		this.pack = pack;
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @param circleID
	 * @param cosID
	 */
	public ApplicationDetailsRequest(String circleID, Integer cosID)
	{
		super(null, circleID);
		this.cosID = cosID;
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
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return the range
	 */
	public String getRange()
	{
		return range;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(String range)
	{
		this.range = range;
	}

	/**
	 * @return the playDate
	 */
	public Date getPlayDate()
	{
		return playDate;
	}

	/**
	 * @param playDate the playDate to set
	 */
	public void setPlayDate(Date playDate)
	{
		this.playDate = playDate;
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
	 * @return the categoryID
	 */
	public Integer getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(Integer categoryID)
	{
		this.categoryID = categoryID;
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
	 * @return the profile
	 */
	public String getProfile()
	{
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile)
	{
		this.profile = profile;
	}

	/**
	 * @return the userID
	 */
	public String getUserID()
	{
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID)
	{
		this.userID = userID;
	}
	
	/**
	 * @return the newUserID
	 */
	public String getNewUserID()
	{
		return newUserID;
	}
	
	/**
	 * @param newUserID the newUserID to set
	 */
	public void setNewUserID(String newUserID)
	{
		this.newUserID = newUserID;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the userInfo
	 */
	public HashMap<String, String> getUserInfo()
	{
		return userInfo;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(HashMap<String, String> userInfo)
	{
		this.userInfo = userInfo;
	}

	/**
	 * @return the siteName
	 */
	public String getSiteName()
	{
		return siteName;
	}

	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName)
	{
		this.siteName = siteName;
	}

	/**
	 * @return the sitePrefixes
	 */
	public String[] getSitePrefixes()
	{
		return sitePrefixes;
	}

	/**
	 * @param sitePrefixes the sitePrefixes to set
	 */
	public void setSitePrefixes(String[] sitePrefixes)
	{
		this.sitePrefixes = sitePrefixes;
	}

	/**
	 * @return the siteURL
	 */
	public String getSiteURL()
	{
		return siteURL;
	}

	/**
	 * @param siteURL the siteURL to set
	 */
	public void setSiteURL(String siteURL)
	{
		this.siteURL = siteURL;
	}

	/**
	 * @return the accessAllowed
	 */
	public Boolean getAccessAllowed()
	{
		return accessAllowed;
	}

	/**
	 * @param accessAllowed the accessAllowed to set
	 */
	public void setAccessAllowed(Boolean accessAllowed)
	{
		this.accessAllowed = accessAllowed;
	}

	/**
	 * @return the supportedLanguages
	 */
	public String[] getSupportedLanguages()
	{
		return supportedLanguages;
	}

	/**
	 * @param supportedLanguages the supportedLanguages to set
	 */
	public void setSupportedLanguages(String[] supportedLanguages)
	{
		this.supportedLanguages = supportedLanguages;
	}

	/**
	 * @return the playerURL
	 */
	public String getPlayerURL()
	{
		return playerURL;
	}

	/**
	 * @param playerURL the playerURL to set
	 */
	public void setPlayerURL(String playerURL)
	{
		this.playerURL = playerURL;
	}

	/**
	 * @return the playUnchargedFor
	 */
	public String getPlayUnchargedFor()
	{
		return playUnchargedFor;
	}

	/**
	 * @param playUnchargedFor the playUnchargedFor to set
	 */
	public void setPlayUnchargedFor(String playUnchargedFor)
	{
		this.playUnchargedFor = playUnchargedFor;
	}

	/**
	 * @return the prepaidSuccessSms
	 */
	public String getPrepaidSuccessSms()
	{
		return prepaidSuccessSms;
	}

	/**
	 * @param prepaidSuccessSms the prepaidSuccessSms to set
	 */
	public void setPrepaidSuccessSms(String prepaidSuccessSms)
	{
		this.prepaidSuccessSms = prepaidSuccessSms;
	}

	/**
	 * @return the prepaidFailureSms
	 */
	public String getPrepaidFailureSms()
	{
		return prepaidFailureSms;
	}

	/**
	 * @param prepaidFailureSms the prepaidFailureSms to set
	 */
	public void setPrepaidFailureSms(String prepaidFailureSms)
	{
		this.prepaidFailureSms = prepaidFailureSms;
	}

	/**
	 * @return the postpaidSuccessSms
	 */
	public String getPostpaidSuccessSms()
	{
		return postpaidSuccessSms;
	}

	/**
	 * @param postpaidSuccessSms the postpaidSuccessSms to set
	 */
	public void setPostpaidSuccessSms(String postpaidSuccessSms)
	{
		this.postpaidSuccessSms = postpaidSuccessSms;
	}

	/**
	 * @return the postpaidFailureSms
	 */
	public String getPostpaidFailureSms()
	{
		return postpaidFailureSms;
	}

	/**
	 * @param postpaidFailureSms the postpaidFailureSms to set
	 */
	public void setPostpaidFailureSms(String postpaidFailureSms)
	{
		this.postpaidFailureSms = postpaidFailureSms;
	}

	/**
	 * @return the prepaidNEFSuccessSms
	 */
	public String getPrepaidNEFSuccessSms()
	{
		return prepaidNEFSuccessSms;
	}

	/**
	 * @param prepaidNEFSuccessSms the prepaidNEFSuccessSms to set
	 */
	public void setPrepaidNEFSuccessSms(String prepaidNEFSuccessSms)
	{
		this.prepaidNEFSuccessSms = prepaidNEFSuccessSms;
	}

	/**
	 * @return the prepaidRenewalSuccessSms
	 */
	public String getPrepaidRenewalSuccessSms()
	{
		return prepaidRenewalSuccessSms;
	}

	/**
	 * @param prepaidRenewalSuccessSms the prepaidRenewalSuccessSms to set
	 */
	public void setPrepaidRenewalSuccessSms(String prepaidRenewalSuccessSms)
	{
		this.prepaidRenewalSuccessSms = prepaidRenewalSuccessSms;
	}

	/**
	 * @return the prepaidRenewalFailureSms
	 */
	public String getPrepaidRenewalFailureSms()
	{
		return prepaidRenewalFailureSms;
	}

	/**
	 * @param prepaidRenewalFailureSms the prepaidRenewalFailureSms to set
	 */
	public void setPrepaidRenewalFailureSms(String prepaidRenewalFailureSms)
	{
		this.prepaidRenewalFailureSms = prepaidRenewalFailureSms;
	}

	/**
	 * @return the postpaidRenewalSuccessSms
	 */
	public String getPostpaidRenewalSuccessSms()
	{
		return postpaidRenewalSuccessSms;
	}

	/**
	 * @param postpaidRenewalSuccessSms the postpaidRenewalSuccessSms to set
	 */
	public void setPostpaidRenewalSuccessSms(String postpaidRenewalSuccessSms)
	{
		this.postpaidRenewalSuccessSms = postpaidRenewalSuccessSms;
	}

	/**
	 * @return the postpaidRenewalFailureSms
	 */
	public String getPostpaidRenewalFailureSms()
	{
		return postpaidRenewalFailureSms;
	}

	/**
	 * @param postpaidRenewalFailureSms the postpaidRenewalFailureSms to set
	 */
	public void setPostpaidRenewalFailureSms(String postpaidRenewalFailureSms)
	{
		this.postpaidRenewalFailureSms = postpaidRenewalFailureSms;
	}

	/**
	 * @return the pack
	 */
	public String getPack()
	{
		return pack;
	}

	/**
	 * @param pack the pack to set
	 */
	public void setPack(String pack)
	{
		this.pack = pack;
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
	 * @return the retailerID
	 */
	public String getRetailerID()
	{
		return retailerID;
	}

	/**
	 * @param retailerID the retailerID to set
	 */
	public void setRetailerID(String retailerID)
	{
		this.retailerID = retailerID;
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
	 * @return the encryptPassword
	 */
	public Boolean getEncryptPassword()
	{
		return encryptPassword;
	}

	/**
	 * @param encryptPassword the encryptPassword to set
	 */
	public void setEncryptPassword(Boolean encryptPassword)
	{
		this.encryptPassword = encryptPassword;
	}

	/**
	 * @return the doSubscriberValidation
	 */
	public Boolean getDoSubscriberValidation() 
	{
		return doSubscriberValidation;
	}

	/**
	 * @param doSubscriberValidation the doSubscriberValidation to set
	 */
	public void setDoSubscriberValidation(Boolean doSubscriberValidation) 
	{
		this.doSubscriberValidation = doSubscriberValidation;
	}
	
	public String getCallerId() {
		return callerID;
	}

	public void setCallerID(String callerId) {
		this.callerID = callerId;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (info != null) requestParams.put(param_info, info);
		if (type != null) requestParams.put(param_type, type);
		if (name != null) requestParams.put(param_name, name);
		if (value != null) requestParams.put(param_value, value);
		if (range != null) requestParams.put(param_range, range);
		if (playDate != null) requestParams.put(param_playDate, dateFormat.format(playDate));
		if (startDate != null) requestParams.put(param_startDate, dateFormat.format(startDate));
		if (endDate != null) requestParams.put(param_endDate, dateFormat.format(endDate));
		if (clipID != null) requestParams.put(param_clipID, clipID);
		if (categoryID != null) requestParams.put(param_categoryID, String.valueOf(categoryID));
		if (userType != null) requestParams.put(param_userType, userType);
		if (profile != null) requestParams.put(param_profile, profile);
		if (userID != null) requestParams.put(param_userID, userID);
		if (newUserID != null) requestParams.put(param_newUserID, newUserID);
		if (password != null) requestParams.put(param_password, password);

		if (userInfo != null)
		{
			Set<String> keySet = userInfo.keySet();
			for (String key : keySet)
				requestParams.put(param_userInfo + "_" + key, userInfo.get(key));
		}

		if (siteName != null) requestParams.put(param_siteName, siteName);
		if (siteURL != null) requestParams.put(param_siteURL, siteURL);
		if (accessAllowed != null) requestParams.put(param_accessAllowed, (accessAllowed ? YES : NO));
		if (playerURL != null) requestParams.put(param_playerURL, playerURL);
		if (playUnchargedFor != null) requestParams.put(param_playUnchargedFor, playUnchargedFor);

		if (sitePrefixes != null && sitePrefixes.length > 0)
		{
			String sitePrefixesStr = "";
			if (sitePrefixes != null && sitePrefixes.length > 0)
			{
				for (String prefix : sitePrefixes)
					sitePrefixesStr += prefix + ",";
				sitePrefixesStr = sitePrefixesStr.substring(0, sitePrefixesStr.length() - 1);
			}

			requestParams.put(param_sitePrefix, sitePrefixesStr);
		}

		if (supportedLanguages != null && supportedLanguages.length > 0)
		{
			String supportedLanguagesStr = "[";
			if (supportedLanguages != null && supportedLanguages.length > 0)
			{
				for (String language : supportedLanguages)
					supportedLanguagesStr += language + ",";
				supportedLanguagesStr = supportedLanguagesStr.substring(0, supportedLanguagesStr.length() - 1);
			}

			requestParams.put(param_supportedLanguage, supportedLanguagesStr);
		}

		if (prepaidSuccessSms != null) requestParams.put(param_prepaidSuccessSms, prepaidSuccessSms);
		if (prepaidFailureSms != null) requestParams.put(param_prepaidFailureSms, prepaidFailureSms);
		if (postpaidSuccessSms != null) requestParams.put(param_postpaidSuccessSms, postpaidSuccessSms);
		if (postpaidFailureSms != null) requestParams.put(param_postpaidFailureSms, postpaidFailureSms);
		if (prepaidNEFSuccessSms != null) requestParams.put(param_prepaidNEFSuccessSms, prepaidNEFSuccessSms);
		if (prepaidRenewalSuccessSms != null) requestParams.put(param_prepaidRenewalSuccessSms, prepaidRenewalSuccessSms);
		if (prepaidRenewalFailureSms != null) requestParams.put(param_prepaidRenewalFailureSms, prepaidRenewalFailureSms);
		if (postpaidRenewalSuccessSms != null) requestParams.put(param_postpaidRenewalSuccessSms, postpaidRenewalSuccessSms);
		if (postpaidRenewalFailureSms != null) requestParams.put(param_postpaidRenewalFailureSms, postpaidRenewalFailureSms);
		if (pack != null) requestParams.put(param_pack, pack);
		if (isPrepaid != null) requestParams.put(param_isPrepaid, (isPrepaid ? YES : NO));
		if (retailerID != null) requestParams.put(param_retailerID, retailerID);
		if (cosID != null) requestParams.put(param_cosID, String.valueOf(cosID));
		if (encryptPassword != null) requestParams.put(param_encryptPassword, (encryptPassword ? YES : NO));
		if (doSubscriberValidation != null) requestParams.put(param_doSubscriberValidation, (doSubscriberValidation ? YES : NO));
		if (language != null) requestParams.put(param_language, language);
		if (smsText != null) requestParams.put(param_smsText, smsText);
		if (tittle != null) requestParams.put(param_title, tittle);
		if (pageNo != null) requestParams.put(param_pageNo, pageNo);
		if (osType != null) requestParams.put(param_os_Type, osType);
		if (oldPassword != null) requestParams.put(param_oldPassword, oldPassword);
		if (redirectionRequired) requestParams.put(param_redirectionRequired, "true");
		if (callerID != null) requestParams.put(param_callerID, callerID);
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ApplicationDetailsRequest[browsingLanguage = ");
		builder.append(browsingLanguage);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", modeInfo = ");
		builder.append(modeInfo);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", accessAllowed = ");
		builder.append(accessAllowed);
		builder.append(", categoryID = ");
		builder.append(categoryID);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", cosID = ");
		builder.append(cosID);
		builder.append(", doSubscriberValidation = ");
		builder.append(doSubscriberValidation);
		builder.append(", encryptPassword = ");
		builder.append(encryptPassword);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", info = ");
		builder.append(info);
		builder.append(", isPrepaid = ");
		builder.append(isPrepaid);
		builder.append(", name = ");
		builder.append(name);
		builder.append(", newUserID = ");
		builder.append(newUserID);
		builder.append(", pack = ");
		builder.append(pack);
		builder.append(", password = ");
		builder.append(password);
		builder.append(", playDate = ");
		builder.append(playDate);
		builder.append(", playerURL = ");
		builder.append(playerURL);
		builder.append(", playUnchargedFor = ");
		builder.append(playUnchargedFor);
		builder.append(", postpaidFailureSms = ");
		builder.append(postpaidFailureSms);
		builder.append(", postpaidRenewalFailureSms = ");
		builder.append(postpaidRenewalFailureSms);
		builder.append(", postpaidRenewalSuccessSms = ");
		builder.append(postpaidRenewalSuccessSms);
		builder.append(", postpaidSuccessSms = ");
		builder.append(postpaidSuccessSms);
		builder.append(", prepaidFailureSms = ");
		builder.append(prepaidFailureSms);
		builder.append(", prepaidNEFSuccessSms = ");
		builder.append(prepaidNEFSuccessSms);
		builder.append(", prepaidRenewalFailureSms = ");
		builder.append(prepaidRenewalFailureSms);
		builder.append(", prepaidRenewalSuccessSms = ");
		builder.append(prepaidRenewalSuccessSms);
		builder.append(", prepaidSuccessSms = ");
		builder.append(prepaidSuccessSms);
		builder.append(", profile = ");
		builder.append(profile);
		builder.append(", range = ");
		builder.append(range);
		builder.append(", retailerID = ");
		builder.append(retailerID);
		builder.append(", siteName = ");
		builder.append(siteName);
		builder.append(", sitePrefixes = ");
		builder.append(Arrays.toString(sitePrefixes));
		builder.append(", siteURL = ");
		builder.append(siteURL);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append(", supportedLanguages = ");
		builder.append(Arrays.toString(supportedLanguages));
		builder.append(", type = ");
		builder.append(type);
		builder.append(", userID = ");
		builder.append(userID);
		builder.append(", userInfo = ");
		builder.append(userInfo);
		builder.append(", userType = ");
		builder.append(userType);
		builder.append(", value = ");
		builder.append(value);
		builder.append(", smsText = ");
		builder.append(smsText);
		builder.append(", tittle = ");
		builder.append(tittle);
		builder.append(", pageNo = ");
		builder.append(pageNo);
		builder.append(", oldPassword = ");
		builder.append(oldPassword);
		builder.append(", redirectionRequired = ");
		builder.append(redirectionRequired);
		builder.append(", callerID = ");
		builder.append(callerID);
		builder.append("]");
		return builder.toString();
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public String getSmsText() {
		return smsText;
	}

	public void setSmsText(String smsText) {
		this.smsText = smsText;
	}

	public String getTittle() {
		return tittle;
	}

	public void setTittle(String tittle) {
		this.tittle = tittle;
	}

	public String getPageNo() {
		return pageNo;
	}

	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public boolean isRedirectionRequired() {
		return redirectionRequired;
	}

	public void setRedirectionRequired(boolean redirectionRequired) {
		this.redirectionRequired = redirectionRequired;
	}
	
}
