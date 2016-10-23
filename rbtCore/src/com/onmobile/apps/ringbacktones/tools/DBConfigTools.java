package com.onmobile.apps.ringbacktones.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.common.exception.OnMobileException;

public class DBConfigTools
{
	private static Logger logger = Logger.getLogger(DBConfigTools.class);

	public static RBTDBManager rbtDBManager = null;
	public static RBTCacheManager rbtCacheManager = null;
	
	static
	{
		rbtCacheManager = RBTCacheManager.getInstance();
		rbtDBManager = initDBManager();
	}
	
	public static RBTDBManager initDBManager()
	{
		rbtDBManager = RBTDBManager.getInstance();
		return rbtDBManager;
	}
	
	public static String getParameter(String type, String paramName, String defaultValue)
	{
		try
		{
			return CacheManagerUtil.getParametersCacheManager().getParameterValue(type, paramName, defaultValue);
		}
		catch(Exception e)
		{
			logger.error("", e);
			return defaultValue;
		}
	}

	public static ArrayList<String> getParameter(String type, String paramName, String defaultValue, String delimiter)
	{
		String strValue = getParameter(type, paramName, defaultValue);
		if(strValue != null)
		{
			ArrayList<String> list = StringTools.tokenizeAsArraylist(strValue, delimiter);
			return list;
		}
		return null;
	}
	
	public static int getParameter(String type, String paramName, int defaultValue)
	{
		try
		{
			String strValue = getParameter(type, paramName, null);
			int paramvalue = IntegerTools.getInteger(strValue, defaultValue);
			return paramvalue;
		}
		catch(Exception e)
		{
			logger.error("", e);
			return defaultValue;
		}
	}
	
	public static boolean getParameter(String type, String paramName, boolean defaultValue)
	{
		String strValue = getParameter(type, paramName, defaultValue+"");
		boolean paramvalue = StringTools.isTrue(strValue, defaultValue);
		return paramvalue;
	}
	
	public static SubscriptionClass getSubscriptionClass(String subClass)
	{
		if(subClass == null)
			return null;
		return CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subClass);
	}
	
	public static ChargeClass getChargeClass(String chargeClass)
	{
		if(chargeClass == null)
			return null;
		return CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
	}
	
	public static String getSmsText(String type, String subType, String language)
	{
		if(type == null)
			return null;
		return CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, subType, language);
	}
	
	public static Subscriber getSubscriber(String subId)
	{
		if(subId == null)
			return null;
		return rbtDBManager.getSubscriber(subId);
	}

	public static SubscriberStatus getTrialSelection(String subID,String chargepack)
	{
		if(subID == null || chargepack == null)
			return null;
		SubscriberStatus[] subscriberStatuses = rbtDBManager.getActiveSelectionsByType(subID, 0);
		if(subscriberStatuses == null || subscriberStatuses.length == 0)
			return null;
		for(SubscriberStatus subscriberStatus : subscriberStatuses)
		{
			if(subscriberStatus.endTime().getTime() < System.currentTimeMillis())
				continue;
			if(subscriberStatus.classType().equalsIgnoreCase(chargepack))
				return subscriberStatus;
		}
		return null;
	}

	public static List<ChargeClass> getTrialChargeClasses()
	{
		return CacheManagerUtil.getChargeClassCacheManager().getTrialChargeClasses();
	}

	public static void bulkDeleteTrialSelection() throws OnMobileException
	{
		rbtDBManager.bulkDeleteTrialSelection();
		
	}

	public static void bulkDeleteTnbSubscriber() throws OnMobileException
	{
		rbtDBManager.bulkDeleteTnbSubscriber();		
	}
	
}
