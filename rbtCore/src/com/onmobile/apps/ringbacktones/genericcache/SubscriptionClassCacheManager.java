package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubClassBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.genericcache.dao.SubscriptionClassDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They shd
 * get the instance from CacheManagerUtil class. @author manish.shringarpure
 */
public class SubscriptionClassCacheManager
{
	private static Logger logger = Logger.getLogger(SubscriptionClassCacheManager.class);

	private IGenericCache genericCache;

	private Hashtable<String, String> chargePeriodMonthMap = new Hashtable<String, String>();

	private SubscriptionClassCacheManager(IGenericCache genericCache)
	{
		this.genericCache = genericCache;
		fillChargePeriodMonthMap();
	}

	public void fillChargePeriodMonthMap()
	{
		chargePeriodMonthMap.put("O", "One Time");
		chargePeriodMonthMap.put("B", "Billing Cycle");
		chargePeriodMonthMap.put("D", "Day(s)");
		chargePeriodMonthMap.put("M", "Month(s)");
	}

	/**
	 * @param subscriptionClass
	 * @return SubscriptionClass
	 */
	public SubscriptionClass getSubscriptionClass(String subscriptionClass)
	{
		subscriptionClass = (subscriptionClass!=null)?subscriptionClass.toUpperCase():null;
		logger.info("subscriptionClass : " + subscriptionClass);

		ArrayList<SubscriptionClass> subscriptionClassList = (ArrayList<SubscriptionClass>) getAllSubscriptionClasses();
		for (SubscriptionClass subscriptionClassObj : subscriptionClassList)
		{
			if (subscriptionClassObj != null
					&& subscriptionClassObj.getSubscriptionClass().equalsIgnoreCase(subscriptionClass))
			{
				return subscriptionClassObj;
			}
		}

		return null;
	}

	/**
	 * @param subscriptionClass
	 * @param language
	 * @return SubscriptionClass
	 */
	public SubscriptionClass getSubscriptionClassByLanguage(String subscriptionClass, String language)
	{
		SubscriptionClass subscriptionClassObj = getSubscriptionClass(subscriptionClass);
		subscriptionClassObj = updateLanguageSpecificInfo(subscriptionClassObj, language);

		return subscriptionClassObj;
	}

	/**
	 * @param subscriptionClass
	 * @param circleID
	 * @return SubscriptionClass
	 */
	public SubscriptionClass getSubscriptionClass(String subscriptionClass, String circleID)
	{
		subscriptionClass = subscriptionClass.toUpperCase();

		logger.info("subscriptionClass: " + subscriptionClass + ", circleID: "+ circleID);

		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, subscriptionClass + ":" + circleID.toUpperCase());
		SubscriptionClass subClass = (SubscriptionClass) object;
		logger.info("subscription class obj : " + subClass);

		return subClass;
	}

	/**
	 * @param subscriptionClass
	 * @param circleID
	 * @param language
	 * @return SubscriptionClass
	 */
	public SubscriptionClass getSubscriptionClassByLanguage(String subscriptionClass, String circleID, String language)
	{
		SubscriptionClass subscriptionClassObj = getSubscriptionClass(subscriptionClass, circleID);
		subscriptionClassObj = updateLanguageSpecificInfo(subscriptionClassObj, language);

		return subscriptionClassObj;
	}

	/**
	 * @return List<SubscriptionClass>
	 */
	public List<SubscriptionClass> getAllSubscriptionClasses()
	{
		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		List<SubscriptionClass> subsClassList = new ArrayList<SubscriptionClass>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key.toUpperCase());
			SubscriptionClass subsClass = (SubscriptionClass) object;

			subsClassList.add(subsClass);
		}

		return subsClassList;
	}

	/**
	 * @return List<SubscriptionClass>
	 */
	public List<SubscriptionClass> getSubscriptionClassesGUI(boolean showOnGui)
	{
		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		List<SubscriptionClass> subsClassList = new ArrayList<SubscriptionClass>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key.toUpperCase());
			SubscriptionClass subsClass = (SubscriptionClass) object;
			if (subsClass.showOnGui() == showOnGui)
				subsClassList.add(subsClass);
		}

		return subsClassList;
	}

	/**
	 * Add a Subscription class to RBT_SUBSCRIPTION_CLASS table and cache
	 * 
	 * @param subscriptionClass
	 * @return
	 */
	public boolean addSubscriptionClass(SubscriptionClass subscriptionClass)
	{
		if (subscriptionClass == null)
			return false;

		subscriptionClass.setSubscriptionClass(subscriptionClass.getSubscriptionClass().toUpperCase());
		logger.info("add Subscription class : " + subscriptionClass.getSubscriptionClass());
		try
		{
			String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
			SubscriptionClassDao subClassDao = new SubscriptionClassDao();

			SubscriptionClass dbSubClass = subClassDao.getSubscriptionClass(subscriptionClass.getSubscriptionClass());
			if (dbSubClass == null)
			{
				subClassDao.insertSubscriptionClass(subscriptionClass);
				String[] newCircleIDs = subscriptionClass.getCircleID().split(",");
				for (String newCircleID : newCircleIDs)
				{
					SubscriptionClass tmpSubClass = subscriptionClass.clone();
					tmpSubClass.setCircleID(newCircleID);
					genericCache.updateToCache(cacheName, tmpSubClass.getSubscriptionClass() + ":" + newCircleID.toUpperCase(), tmpSubClass);
				}
			}
			else
			{
				String[] circleIDs = subscriptionClass.getCircleID().split(",");
				String[] dbCircleIDs = dbSubClass.getCircleID().split(",");
				List<String> circleIDList = Arrays.asList(dbCircleIDs);
				StringBuilder stringBuilder = new StringBuilder();

				for (String circleID : circleIDs)
				{
					if (circleIDList.contains(circleID)
							|| circleIDList.contains(circleID.toUpperCase()))
					{
						logger.error("Duplicate entry: " + subscriptionClass.getSubscriptionClass() + "-" + circleID);
						return false;
					}
					stringBuilder.append(circleID + ",");
				}

				String newCircleIDStr = stringBuilder.toString() + dbSubClass.getCircleID();
				subscriptionClass.setCircleID(newCircleIDStr);
				subClassDao.updateSubscriptionClass(subscriptionClass);

				String[] newCircleIDs = newCircleIDStr.split(",");
				for (String newCircleID : newCircleIDs)
				{
					SubscriptionClass tmpSubClass = subscriptionClass.clone();
					tmpSubClass.setCircleID(newCircleID);

					genericCache.updateToCache(cacheName, tmpSubClass.getSubscriptionClass() + ":" + newCircleID.toUpperCase(), tmpSubClass);
				}
			}

			logger.info("Subscription class added");
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error while adding subscription class", e);
			return false;
		}
	}

	/**
	 * Update Subscription class table and cache
	 * 
	 * @param SubscriptionClass
	 * @return
	 */
	public boolean updateSubscriptionClass(SubscriptionClass subClass)
	{
		if (subClass == null)
			return false;

		subClass.setSubscriptionClass(subClass.getSubscriptionClass().toUpperCase());
		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, subClass.getSubscriptionClass() + ":" + subClass.getCircleID().toUpperCase());
		try
		{
			if (object != null)
			{
				if (object instanceof SubscriptionClass)
				{
					SubscriptionClassDao subClassDao = new SubscriptionClassDao();
					SubscriptionClass dbSubClass = subClassDao.getSubscriptionClass(subClass.getSubscriptionClass());
					subClass.setCircleID(dbSubClass.getCircleID());
					subClassDao.updateSubscriptionClass(subClass);

					String[] circleIDs = subClass.getCircleID().split(",");
					for (String circleID : circleIDs)
					{
						SubscriptionClass tmpSubClass = subClass.clone();
						tmpSubClass.setCircleID(circleID);

						genericCache.updateToCache(cacheName, tmpSubClass.getSubscriptionClass() + ":" + circleID.toUpperCase(), tmpSubClass);
					}

					return true;
				}
			}
			else
			{
				logger.info("The SubcriptionClass does not exist in the Cache. Couldnt update ChargeClass ");
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return false;
	}

	/**
	 * Remove a charge class from subscription class table and cache
	 * 
	 * @param SubcriptionClass
	 * @return
	 */
	public boolean removeSubscriptionClass(String subscriptionClass)
	{
		subscriptionClass = subscriptionClass.toUpperCase();
		logger.info("subscriptionClass: " + subscriptionClass);
		SubscriptionClass subClass = getSubscriptionClass(subscriptionClass);

		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		SubscriptionClass subClassObject = (SubscriptionClass) genericCache.getFromCache(cacheName, subscriptionClass + ":" + subClass.getCircleID().toUpperCase());
		if (subClassObject == null)
			return false;
		try
		{
			String[] circleIDs = getCircleIDs(subscriptionClass);

			SubscriptionClassDao subClassDao = new SubscriptionClassDao();
			subClassDao.removeSubscriptionClass(subClassObject);
			if (circleIDs != null && circleIDs.length != 0)
			{
				for (String circleID : circleIDs)
				{
					genericCache.removeFromCache(cacheName, subscriptionClass + ":" + circleID.toUpperCase());
				}
			}
			else
			{
				genericCache.removeFromCache(cacheName, subscriptionClass + ":" + subClass.getCircleID().toUpperCase());
			}

			logger.info("SubscriptionClass removed successfully");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean removeSubscriptionClass(String subClass, String circleID)
	{
		subClass = subClass.toUpperCase();

		logger.info(" removeSubscriptionClass : subclass : " + subClass);
		String cacheName = CacheNamesEnum.SUBSCRIPTION_CLASS_CACHE.toString();
		SubscriptionClass subClassObject = (SubscriptionClass) genericCache.getFromCache(cacheName, subClass + ":" + circleID.toUpperCase());
		if (subClassObject == null)
			return false;
		try
		{
			SubscriptionClassDao subClassDao = new SubscriptionClassDao();
			SubscriptionClass dbSubClass = subClassDao.getSubscriptionClass(subClass);

			if (dbSubClass != null
					&& (dbSubClass.getCircleID().equalsIgnoreCase(circleID)))
			{
				return removeSubscriptionClass(subClass);
			}
			else
			{
				String[] circleIDs = dbSubClass.getCircleID().split(",");
				StringBuilder stringBuilder = new StringBuilder();
				for (String cirID : circleIDs)
				{
					if (!cirID.equalsIgnoreCase(circleID))
						stringBuilder.append(cirID + ",");
				}

				String csvCircleIDs = stringBuilder.toString();
				// to trim extra comma at the end
				csvCircleIDs = csvCircleIDs.substring(0, csvCircleIDs.length() - 1);
				dbSubClass.setCircleID(csvCircleIDs);

				subClassDao.updateSubscriptionClass(dbSubClass);

				genericCache.removeFromCache(cacheName, subClass + ":" + circleID.toUpperCase());
			}

			logger.info("SubscriptionClass removed successfully ");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * getSubscriptionClassDetails
	 * 
	 * @return Subclass object list
	 * @author bikash panda
	 */
	public List<SubClassBean> getCCCSubscriptionClassDetails(int mode, HashMap<String, String> subscripPeriod)
	{
		List<SubClassBean> classList = new ArrayList<SubClassBean>();

		ParametersCacheManager paramsCacheManager = CacheManagerUtil.getParametersCacheManager();

		String currency = null;
		Parameters parameter = paramsCacheManager.getParameter("CCC", "CURRENCY_TYPE");
		if (parameter != null)
			currency = parameter.getValue();
		if (currency == null)
			currency = "Rs.";

		String subClassLifeTime = null;
		parameter = paramsCacheManager.getParameter("CCC", "SUBCLASS_LIFETIME_PERIOD");
		if (parameter != null)
			subClassLifeTime = parameter.getValue();


		List<String> tnbList = null;
		if (mode == 1)
		{
			String tnbSubClasses = null;

			tnbList = new ArrayList<String>();
			parameter = paramsCacheManager.getParameter("COMMON", "TNB_SUBSCRIPTION_CLASSES");
			if (parameter != null)
				tnbSubClasses = parameter.getValue();
			if (tnbSubClasses != null)
			{
				tnbList = Arrays.asList(tnbSubClasses.split(","));
			}
		}

		ArrayList<SubscriptionClass> subClassList = (ArrayList<SubscriptionClass>) getAllSubscriptionClasses();
		if (subClassList == null)
			return null;

		for (SubscriptionClass subscriptionClass : subClassList)
		{
			StringBuilder subClassDetails = new StringBuilder();

			if (subscriptionClass.getShowOnGui() != null
					&& subscriptionClass.getShowOnGui().equalsIgnoreCase("y"))
			{
				String subClassName = subscriptionClass.getSubscriptionClass();
				if (subClassName == null)
					return null;
				if (subClassLifeTime != null
						&& subscriptionClass.getRenewalPeriod().substring(1).equalsIgnoreCase(subClassLifeTime))
				{
					subClassDetails.append(currency);
					subClassDetails.append(subscriptionClass.getRenewalAmount());
					subClassDetails.append(" ");
					subClassDetails.append(currency);
				}
				else
				{
					subClassDetails.append(subClassName);
					subClassDetails.append(" [");
					subClassDetails.append(subClassName);
					subClassDetails.append(": ");
					subClassDetails.append(currency);
					subClassDetails.append(subscriptionClass.getRenewalAmount());
					subClassDetails.append("(");
					if (subscriptionClass.getRenewalPeriod().substring(0, 1).equals("M")
							|| subscriptionClass.getRenewalPeriod().substring(0, 1).equals("D"))
					{
						subClassDetails.append("First ");
						subClassDetails.append(subscriptionClass.getRenewalPeriod().substring(1));
						subClassDetails.append(" ");
						subClassDetails.append(chargePeriodMonthMap.get(subscriptionClass.getRenewalPeriod().substring(0, 1)));
						subClassDetails.append(")");
					}

					// renewal amt to be added here
					if (subscriptionClass.getRenewalAmount() != null)
					{
						subClassDetails.append(" / ");
						subClassDetails.append(" ");
						subClassDetails.append(currency);
						subClassDetails.append(subscriptionClass.getRenewalAmount());
						subClassDetails.append("(");

						if (subscriptionClass.getRenewalPeriod().substring(0, 1).equals("M")
								|| subscriptionClass.getRenewalPeriod().substring(0, 1).equals("D"))
						{
							subClassDetails.append("Every ");
							subClassDetails.append(subscriptionClass.getRenewalPeriod().substring(1));
							subClassDetails.append(" ");
							subClassDetails.append(chargePeriodMonthMap.get(subscriptionClass.getRenewalPeriod().substring(0, 1)));
							subClassDetails.append(")");
						}
					}
					else
					{
						subClassDetails.append(" / No Renewal");
					}
					subClassDetails.append("]");
				}

				if (mode == 1)
				{
					if (tnbList.contains(subClassName))
					{
						subscripPeriod.put(subClassName, subscriptionClass.getRenewalPeriod());
						SubClassBean subClassBean = new SubClassBean(subClassDetails.toString(), subClassName);
						classList.add(subClassBean);
					}
				}
				else
				{
					subscripPeriod.put(subClassName, subscriptionClass.getRenewalPeriod());
					SubClassBean subClassBean = new SubClassBean(subClassDetails.toString(), subClassName);
					classList.add(subClassBean);

				}
			}
		}

		return classList;
	}

	/**
	 * getSubscriptionClassDetails
	 * 
	 * @param mode
	 * @param subscripPeriod
	 * @param circleID
	 * @return
	 * 
	 * @author bikash panda
	 */
	public List<SubClassBean> getCCCSubscriptionClassDetails(int mode, HashMap<String, String> subscripPeriod, String circleID)
	{
		logger.debug("mode: " + mode + ", circleID: " + circleID);
		if (circleID != null)
			circleID = circleID.toUpperCase();

		List<SubClassBean> classList = new ArrayList<SubClassBean>();

		ParametersCacheManager paramsCacheManager = CacheManagerUtil.getParametersCacheManager();

		String currency = null;
		Parameters parameter = paramsCacheManager.getParameter("CCC", "CURRENCY_TYPE");
		if (parameter != null)
			currency = parameter.getValue();
		if (currency == null)
			currency = "Rs.";

		String subClassLifeTime = null;
		parameter = paramsCacheManager.getParameter("CCC", "SUBCLASS_LIFETIME_PERIOD");
		if (parameter != null)
			subClassLifeTime = parameter.getValue();

		List<String> tnbList = null;
		if (mode == 1)
		{
			String tnbSubClasses = null;
			tnbList = new ArrayList<String>();
			parameter = paramsCacheManager.getParameter("COMMON", "TNB_SUBSCRIPTION_CLASSES");
			if (parameter != null)
				tnbSubClasses = parameter.getValue();
			if (tnbSubClasses != null)
			{
				tnbList = Arrays.asList(tnbSubClasses.split(","));
			}
		}

		ArrayList<SubscriptionClass> subClassList = (ArrayList<SubscriptionClass>) getAllSubscriptionClasses();
		if (subClassList == null)
			return null;

		for (SubscriptionClass subscriptionClass : subClassList)
		{
			StringBuilder subClassDetails = new StringBuilder();
			if (subscriptionClass.getShowOnGui() != null
					&& subscriptionClass.getShowOnGui().equalsIgnoreCase("y")
					&& subscriptionClass.getCircleID() != null
					&& (subscriptionClass.getCircleID().equalsIgnoreCase("ALL")
							|| subscriptionClass.getCircleID().equalsIgnoreCase(circleID)))
			{
				String subClassName = subscriptionClass.getSubscriptionClass();
				if (subClassName == null)
					return null;
				if (subClassLifeTime != null
						&& subscriptionClass.getRenewalPeriod().substring(1).equalsIgnoreCase(subClassLifeTime))
				{
					subClassDetails.append(currency);
					subClassDetails.append(subscriptionClass.getRenewalAmount());
					subClassDetails.append(" ");
					subClassDetails.append(currency);
				}
				else
				{
					subClassDetails.append(subClassName);
					subClassDetails.append(" [");
					subClassDetails.append(subClassName);
					subClassDetails.append(": ");
					subClassDetails.append(currency);
					subClassDetails.append(subscriptionClass.getSubscriptionAmount());

					subClassDetails.append("(");
					if (subscriptionClass.getSubscriptionPeriod().substring(0, 1).equals("M")
							|| subscriptionClass.getSubscriptionPeriod().substring(0, 1).equals("D"))
					{
						//System.out.println(" subperid "+subscriptionClass.getSubscriptionPeriod());
						subClassDetails.append("First ");
						subClassDetails.append(subscriptionClass.getSubscriptionPeriod().substring(1));
						subClassDetails.append(" ");
						subClassDetails.append(chargePeriodMonthMap.get(subscriptionClass.getSubscriptionPeriod().substring(0, 1)));
						subClassDetails.append(")");
					}
					// renewal amt to be added here
					if (subscriptionClass.getRenewalAmount() != null)
					{
						subClassDetails.append(" / ");
						subClassDetails.append(" ");
						subClassDetails.append(currency);
						subClassDetails.append(subscriptionClass.getRenewalAmount());
						subClassDetails.append("(");

						if (subscriptionClass.getRenewalPeriod().substring(0, 1).equals("M")
								|| subscriptionClass.getRenewalPeriod().substring(0, 1).equals("D"))
						{
							subClassDetails.append("Every ");
							subClassDetails.append(subscriptionClass.getRenewalPeriod().substring(1));
							subClassDetails.append(" ");
							subClassDetails.append(chargePeriodMonthMap.get(subscriptionClass.getRenewalPeriod().substring(0, 1)));
							subClassDetails.append(")");
						}
					}
					else
					{
						subClassDetails.append(" / No Renewal");
					}

					subClassDetails.append("]");
				}

				if (mode == 1)
				{
					if (tnbList.contains(subClassName))
					{
						subscripPeriod.put(subClassName, subscriptionClass.getRenewalPeriod());
						SubClassBean subClassBean = new SubClassBean(subClassDetails.toString(), subClassName);
						classList.add(subClassBean);
					}
				}
				else
				{
					subscripPeriod.put(subClassName, subscriptionClass.getRenewalPeriod());
					SubClassBean subClassBean = new SubClassBean(subClassDetails.toString(), subClassName);
					classList.add(subClassBean);
				}
			}
		}

		logger.debug("classList: " + classList);
		return classList;
	}

	private SubscriptionClass updateLanguageSpecificInfo(SubscriptionClass subscriptionClass, String language)
	{
		if (subscriptionClass == null)
			return null;

		if (language == null || language.length() == 0)
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue();
		}
		
		SubscriptionClass newSubscriptionClass = subscriptionClass;
		String className = subscriptionClass.getSubscriptionClass();

		try
		{
			newSubscriptionClass = subscriptionClass.clone();
		}
		catch (CloneNotSupportedException e)
		{
		}

		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "ON_SUBSCRIPTION", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsOnSubscription(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "ON_SUBSCRIPTION_FAILURE", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsOnSubscriptionFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "ALERT_BEFORE_RENEWAL", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsAlertBeforeRenewal(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "RENEWAL_SUCCESS", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsRenewalSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "RENEWAL_FAILURE", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsRenewalFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "ALERT_RETRY", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsAlertRetry(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "ALERT_GRACE", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsAlertGrace(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "DEACT_FAILURE", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsDeactFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("SUBSCRIPTION_CLASS_SMS_" + className, "DEACT_SUCCESS", language);
		if (rbtText != null)
			newSubscriptionClass.setSmsDeactivationSuccess(rbtText.getText());

		return newSubscriptionClass;
	}

	private String[] getCircleIDs(String subscriptionClass)
	{
		String circleID = "";
		subscriptionClass = subscriptionClass.toUpperCase();

		logger.info("subscriptionClass: " + subscriptionClass);

		ArrayList<SubscriptionClass> subscriptionClassList = (ArrayList<SubscriptionClass>) getAllSubscriptionClasses();
		for (SubscriptionClass subscriptionClassObj : subscriptionClassList)
		{
			if (subscriptionClassObj != null
					&& subscriptionClassObj.getSubscriptionClass().equalsIgnoreCase(subscriptionClass))
			{
				circleID = circleID + subscriptionClassObj.getCircleID() + ",";
			}
		}

		logger.info("subscription class obj circleID : " + circleID);
		if (circleID.length() == 0)
			return null;

		return circleID.split(",");
	}
}
