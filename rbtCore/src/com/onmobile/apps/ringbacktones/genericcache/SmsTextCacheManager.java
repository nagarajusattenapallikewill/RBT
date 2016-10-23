/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;
import org.apache.log4j.Logger;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They should
 * get the instance from CacheManagerUtil class.
 * 
 * This is wrapper class for the RBTTextCacheManager and represents the SMS Texts.
 * 
 * @author vinayasimha.patil
 *
 */
public class SmsTextCacheManager
{
	
	private static Logger logger = Logger.getLogger(SmsTextCacheManager.class);
	/**
	 * 
	 */
	private SmsTextCacheManager()
	{

	}

	/**
	 * @param key is the pair of type and subType in 'type_subType' format
	 * @return
	 */
	public String getSmsText(String key)
	{
		return getSmsText(key, null, null, null);
	}

	public String getSmsText(String key, String language)
	{
		return getSmsText(key, null, language, null);
	}
	
	public String getSmsText(String type, String subType, String language)
	{
		String smsText = getSmsText(type, subType, language, null);
		logger.debug("Type: " + type + " subType: " + subType + " language: " + language + " SmsText: " + smsText);
		return smsText;
	}

	public String getSmsText(String type, String subType, String language, String circleID)
	{
		type = "SMS_" + type;
		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, subType, language, circleID);
		if (rbtText != null)
			return rbtText.getText();

		return null;
	}

	public List<RBTText> getAllSmsTexts(String language)
	{
		if (language == null || language.length() == 0)
			return null;

		language = language.toLowerCase();

		List<RBTText> smsTextList = new ArrayList<RBTText>();

		List<RBTText> rbtTextList = CacheManagerUtil.getRbtTextCacheManager().getAllRBTTexts();
		if (rbtTextList != null)
		{
			for (RBTText rbtText : rbtTextList)
			{
				if (rbtText.getType().startsWith("SMS_") && rbtText.getLanguage().equals(language))
				{
					try
					{
						RBTText newRBTText = rbtText.clone();
						newRBTText.setType(newRBTText.getType().substring(4));
						smsTextList.add(newRBTText);
					}
					catch (CloneNotSupportedException e)
					{
					}
				}
			}
		}

		return smsTextList;
	}

	public List<RBTText> getAllSmsTexts()
	{
		List<RBTText> smsTextList = new ArrayList<RBTText>();

		List<RBTText> rbtTextList = CacheManagerUtil.getRbtTextCacheManager().getAllRBTTexts();
		if (rbtTextList != null)
		{
			for (RBTText rbtText : rbtTextList)
			{
				if (rbtText.getType().startsWith("SMS_"))
				{
					try
					{
						RBTText newRBTText = rbtText.clone();
						newRBTText.setType(newRBTText.getType().substring(4));
						smsTextList.add(newRBTText);
					}
					catch (CloneNotSupportedException e)
					{
					}
				}
			}
		}

		return smsTextList;
	}

	public boolean addSmsText(String type, String subType, String language, String text)
	{
		RBTText rbtText = new RBTText("SMS_" + type, subType, language, text);
		return CacheManagerUtil.getRbtTextCacheManager().addRBTText(rbtText);
	}

	public boolean updateSmsText(String type, String subType, String text)
	{
		return updateSmsText(type, subType, null, text);
	}

	public boolean updateSmsText(String type, String subType, String language, String text)
	{
		type = "SMS_" + type;
		return CacheManagerUtil.getRbtTextCacheManager().updateRBTText(type, subType, language, text);
	}

	public boolean removeSmsText(String type, String subType)
	{
		return removeSmsText(type, subType, null);
	}

	public boolean removeSmsText(String type, String subType, String language)
	{
		type = "SMS_" + type;
		return CacheManagerUtil.getRbtTextCacheManager().removeRBTText(type, subType, language);
	}
}
