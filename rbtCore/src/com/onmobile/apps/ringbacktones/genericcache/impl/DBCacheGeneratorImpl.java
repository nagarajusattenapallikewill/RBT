package com.onmobile.apps.ringbacktones.genericcache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.genericcache.dao.BulkPromoSMSDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.ChargeClassDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.ChargeClassMapDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.CosDetailsDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.ParametersDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.PredefinedGroupDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.RBTTextDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.SitePrefixDao;
import com.onmobile.apps.ringbacktones.genericcache.dao.SubscriptionClassDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator;

/**
 * This class is an implementation of the ICacheGenerator, this implementation
 * gets the data from the DAO classes and formats the data required for the
 * caching.
 * 
 * @author manish.shringarpure
 */
public class DBCacheGeneratorImpl implements ICacheGenerator
{
	private DBCacheGeneratorImpl()
	{

	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getParametersMapForCaching()
	 */
	public Map<String, Map<String, Parameters>> getParametersMapForCaching()
	{
		Map<String, Map<String, Parameters>> parametersCacheMap = null;
		ParametersDao parametersDao = new ParametersDao();

		List<Parameters> parametersList = parametersDao.getAllParameters();
		if (parametersList != null)
		{
			parametersCacheMap = new HashMap<String, Map<String, Parameters>>();

			for (Parameters parameter : parametersList)
			{
				String key = parameter.getType();

				Map<String, Parameters> tempMap = parametersCacheMap.get(key);
				if (tempMap == null)
					tempMap = new HashMap<String, Parameters>();

				tempMap.put(parameter.getParam(), parameter);
				parametersCacheMap.put(key, tempMap);
			}
		}

		return parametersCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getSubscriptionClassMapForCaching()
	 */
	public Map<String, SubscriptionClass> getSubscriptionClassMapForCaching()
	{
		Map<String, SubscriptionClass> subscriptionClassCacheMap = null;
		SubscriptionClassDao subscriptionClassDao = new SubscriptionClassDao();

		List<SubscriptionClass> subscriptionClassList = subscriptionClassDao.getAllSubscriptionClasses();
		if (subscriptionClassList != null)
		{
			subscriptionClassCacheMap = new HashMap<String, SubscriptionClass>();

			for (SubscriptionClass subscriptionClass : subscriptionClassList)
			{
				subscriptionClassCacheMap.put(subscriptionClass.getSubscriptionClass().toUpperCase()
						+ ":" + subscriptionClass.getCircleID().toUpperCase(), subscriptionClass);
			}
		}

		return subscriptionClassCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getCosDetailsMapForCaching()
	 */
	public Map<String, List<CosDetails>> getCosDetailsMapForCaching()
	{
		Map<String, List<CosDetails>> cosDetailsCacheMap = null;
		CosDetailsDao cosDetailsDao = new CosDetailsDao();

		List<CosDetails> cosDetailsList = cosDetailsDao.getAllCosDetails();
		if (cosDetailsList != null)
		{
			cosDetailsCacheMap = new HashMap<String, List<CosDetails>>();

			for (CosDetails cosDetails : cosDetailsList)
			{
				if (cosDetails != null)
				{
					String key = cosDetails.getCosId();
					List<CosDetails> cosDetailsListInMap = null;
					if (cosDetailsCacheMap.containsKey(key))
					{
						cosDetailsListInMap = cosDetailsCacheMap.get(key);
						cosDetailsListInMap.add(cosDetails);
					}
					else
					{
						cosDetailsListInMap = new ArrayList<CosDetails>();
						cosDetailsListInMap.add(cosDetails);
					}

					cosDetailsCacheMap.put(key, cosDetailsListInMap);
				}

			}
		}

		return cosDetailsCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getChargeClassMapForCaching()
	 */
	public Map<String, ChargeClass> getChargeClassMapForCaching()
	{
		Map<String, ChargeClass> chargeClassCacheMap = null;
		ChargeClassDao chargeClassDao = new ChargeClassDao();

		List<ChargeClass> chargeClassList = chargeClassDao.getAllChargeClass();
		if (chargeClassList != null)
		{
			chargeClassCacheMap = new HashMap<String, com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass>();

			for (ChargeClass chargeClass : chargeClassList)
			{
				chargeClassCacheMap.put(chargeClass.getChargeClass().toUpperCase()
						+ ":" + chargeClass.getCircleID().toUpperCase(), chargeClass);
			}
		}

		return chargeClassCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getBulkPromoSMSMapForCaching()
	 */
	public Map<String, BulkPromoSMS> getBulkPromoSMSMapForCaching()
	{
		Map<String, BulkPromoSMS> bulkPromoSMSCacheMap = null;
		BulkPromoSMSDao bulkPromoSMSDao = new BulkPromoSMSDao();

		List<BulkPromoSMS> smsList = bulkPromoSMSDao.getAllBulkPromoSMS();
		if (smsList != null)
		{
			bulkPromoSMSCacheMap = new HashMap<String, BulkPromoSMS>();

			for (BulkPromoSMS bulkPromoSMS : smsList)
			{
				bulkPromoSMSCacheMap.put(bulkPromoSMS.getBulkpromoID() + ":" + bulkPromoSMS.getSmsDate(), bulkPromoSMS);
			}
		}

		return bulkPromoSMSCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getChargeClassMapMapForCaching()
	 */
	public Map<String, ChargeClassMap> getChargeClassMapMapForCaching()
	{
		Map<String, ChargeClassMap> chargeClassMapCacheMap = null;
		ChargeClassMapDao chargeClassMapDao = new ChargeClassMapDao();

		List<ChargeClassMap> chargeClassMapList = chargeClassMapDao.getAllChargeClassMap();
		if (chargeClassMapList != null)
		{
			chargeClassMapCacheMap = new HashMap<String, ChargeClassMap>();

			for (ChargeClassMap chargeClassMap : chargeClassMapList)
			{
				chargeClassMapCacheMap.put(chargeClassMap.getChargeClass(), chargeClassMap);
			}
		}

		return chargeClassMapCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getSitePrefixMapForCaching()
	 */
	public Map<String, SitePrefix> getSitePrefixMapForCaching()
	{
		Map<String, SitePrefix> sitePrefixCacheMap = null;
		SitePrefixDao sitePrefixDao = new SitePrefixDao();

		List<SitePrefix> sitePrefixList = sitePrefixDao.getAllSitePrefixes();
		if (sitePrefixList != null)
		{
			sitePrefixCacheMap = new HashMap<String, SitePrefix>();

			for (SitePrefix sitePrefix : sitePrefixList)
			{
				sitePrefixCacheMap.put(sitePrefix.getCircleID(), sitePrefix);
			}
		}

		return sitePrefixCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getRBTTextMapForCaching()
	 */
	public Map<String, RBTText> getRBTTextMapForCaching()
	{
		Map<String, RBTText> rbtTextCacheMap = null;
		RBTTextDao rbtTextDao = new RBTTextDao();

		List<RBTText> rbtTextList = rbtTextDao.getAllRBTTexts();
		if (rbtTextList != null)
		{
			rbtTextCacheMap = new HashMap<String, RBTText>();

			for (RBTText rbtText : rbtTextList)
			{
				String key = null;
				if(rbtText.getSubType()!=null&&rbtText.getSubType().length()!=0)
				     key=rbtText.getType() + "_" + rbtText.getSubType() + "_" +rbtText.getLanguage().toLowerCase();
				else
					key=rbtText.getType() +  "_" +rbtText.getLanguage().toLowerCase();

				rbtTextCacheMap.put(key, rbtText);
			}
		}

		return rbtTextCacheMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.genericcache.interfaces.ICacheGenerator#getPredefinedGroupMapForCaching()
	 */
	public Map<String, PredefinedGroup> getPredefinedGroupMapForCaching()
	{
		Map<String, PredefinedGroup> predefinedGroupCacheMap = null;
		PredefinedGroupDao predefinedGroupDao = new PredefinedGroupDao();

		List<PredefinedGroup> predefinedGroupList = predefinedGroupDao.getAllPredefinedGroups();
		if (predefinedGroupList != null)
		{
			predefinedGroupCacheMap = new HashMap<String, PredefinedGroup>();

			for (PredefinedGroup predefinedGroup : predefinedGroupList)
			{
				predefinedGroupCacheMap.put(predefinedGroup.getPreGroupID(), predefinedGroup);
			}
		}

		return predefinedGroupCacheMap;
	}
}
