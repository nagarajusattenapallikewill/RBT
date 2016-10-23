package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.dao.ChargeClassDao;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.CacheNamesEnum;
import com.onmobile.apps.ringbacktones.genericcache.interfaces.IGenericCache;

/**
 * A singleton class which is injected into the CacheManagerUtil class by spring
 * injection. Users cannot directly use this class to access the cache. They
 * should get the instance from CacheManagerUtil class.
 * 
 * @author bikash.panda
 */
public class ChargeClassCacheManager
{
	private static Logger logger = Logger.getLogger(SubscriptionClassCacheManager.class);

	private Hashtable<String, String> chargePeriodMonthMap = new Hashtable<String, String>();
	private IGenericCache genericCache;

	private ChargeClassCacheManager(IGenericCache genericCache)
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
	 * Get chargeClass for specific charge class type
	 * 
	 * @param chargeClass
	 * @return ChargeClass
	 */
	public ChargeClass getChargeClass(String chargeClass)
	{
		if (chargeClass == null)
			return null;

		chargeClass = chargeClass.toUpperCase();

		ArrayList<ChargeClass> chargeClassList = (ArrayList<ChargeClass>) getAllChargeClass();
		for (ChargeClass chargeClassObj : chargeClassList)
		{
			if (chargeClass.equalsIgnoreCase(chargeClassObj.getChargeClass()))
				return chargeClassObj;
		}

		return null;
	}

	/**
	 * @param chargeClass
	 * @param language
	 * @return ChargeClass
	 */
	public ChargeClass getChargeClassByLanguage(String chargeClass, String language)
	{
		ChargeClass chargeClassObj = getChargeClass(chargeClass);
		chargeClassObj = updateLanguageSpecificInfo(chargeClassObj, language);

		return chargeClassObj;
	}

	public ChargeClass getChargeClass(String chargeClass, String circleID)
	{
		chargeClass = chargeClass.toUpperCase();

		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		Object object = genericCache.getFromCache(cacheName, chargeClass + ":" + circleID.toUpperCase());
		ChargeClass chargeClassObj = (ChargeClass) object;

		return chargeClassObj;
	}

	/**
	 * @param chargeClass
	 * @param circleID
	 * @param language
	 * @return ChargeClass
	 */
	public ChargeClass getChargeClassByLanguage(String chargeClass, String circleID, String language)
	{
		ChargeClass chargeClassObj = getChargeClass(chargeClass, circleID);
		chargeClassObj = updateLanguageSpecificInfo(chargeClassObj, language);

		return chargeClassObj;
	}

	/**
	 * Get All chargeClasses
	 * 
	 * @param
	 * @return List<ChargeClass>
	 */
	public List<ChargeClass> getAllChargeClass()
	{
		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		List<ChargeClass> chargeClassList = new ArrayList<ChargeClass>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key.toUpperCase());
			ChargeClass chargeClass = (ChargeClass) object;

			chargeClassList.add(chargeClass);
		}

		return chargeClassList;
	}

	/**
	 * Find a list of chargeClass having showOnGUI field y
	 * 
	 * @param showOnGui
	 * @return List<ChargeClass>
	 */
	public List<ChargeClass> getChargeClassesGUI(boolean showOnGui)
	{
		logger.info("shownOnGui: " + showOnGui);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		List<ChargeClass> chargeClassList = new ArrayList<ChargeClass>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key.toUpperCase());
			ChargeClass chargeClass = (ChargeClass) object;
			if (chargeClass.showOnGui() == showOnGui)
				chargeClassList.add(chargeClass);
		}

		return chargeClassList;
	}

	/**
	 * find a list of trial charge classes
	 * 
	 * @return List<ChargeClass>
	 */
	public List<ChargeClass> getTrialChargeClasses()
	{
		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		List<ChargeClass> chargeClassList = new ArrayList<ChargeClass>();

		List<String> keyList = genericCache.getAllKeysFromCache(cacheName);
		for (String key : keyList)
		{
			Object object = genericCache.getFromCache(cacheName, key.toUpperCase());
			ChargeClass chargeClass = (ChargeClass) object;
			if (chargeClass.getChargeClass().startsWith("TRIAL"))
				chargeClassList.add(chargeClass);
		}

		return chargeClassList;
	}

	/**
	 * Add a charge class to RBT_CHARGE_CLASS table and cache
	 * 
	 * @param ChargeClass
	 * @return void
	 */
	public boolean addChargeClass(ChargeClass chargeClass)
	{
		if (chargeClass == null)
			return false;

		chargeClass.setChargeClass(chargeClass.getChargeClass().toUpperCase());
		logger.info("Add Charge class : " + chargeClass.getChargeClass());
		try
		{
			String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();

			ChargeClassDao chargeClassDao = new ChargeClassDao();
			ChargeClass dbChargeClass = chargeClassDao.getChargeClass(chargeClass.getChargeClass());
			if (dbChargeClass == null)
			{
				chargeClassDao.insertChargeClass(chargeClass);

				String[] circleIDs = chargeClass.getCircleID().split(",");
				for (String circleID : circleIDs)
				{
					ChargeClass tmpChargeClass = chargeClass.clone();
					tmpChargeClass.setCircleID(circleID);
					genericCache.updateToCache(cacheName, tmpChargeClass.getChargeClass() + ":" + circleID.toUpperCase(), tmpChargeClass);
				}
			}
			else
			{
				String[] circleIDs = chargeClass.getCircleID().split(",");
				String[] dbCircleIDs = dbChargeClass.getCircleID().split(",");

				StringBuilder stringBuilder = new StringBuilder();

				List<String> circleIDList = Arrays.asList(dbCircleIDs);
				for (String circleID : circleIDs)
				{
					if (circleIDList.contains(circleID) || circleIDList.contains(circleID.toUpperCase()))
					{
						logger.error("Duplicate entry: " + chargeClass.getChargeClass() + "-" + circleID);
						return false;
					}

					stringBuilder.append(circleID + ",");
				}

				String newCircleIDs = stringBuilder.toString() + dbChargeClass.getCircleID();
				String[] newCirIDs = newCircleIDs.split(",");
				chargeClass.setCircleID(newCircleIDs);
				chargeClassDao.updateChargeClass(chargeClass);

				for (String newCircleID : newCirIDs)
				{
					chargeClass.setCircleID(newCircleID);
					ChargeClass tmpChargeClass = chargeClass.clone();
					tmpChargeClass.setCircleID(newCircleID);
					genericCache.updateToCache(cacheName, tmpChargeClass.getChargeClass() + ":" + newCircleID.toUpperCase(), tmpChargeClass);
				}
			}

			logger.info("charge class added ");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Update charge class table and cache
	 * 
	 * @param ChargeClass
	 * @return
	 */
	public boolean updateChargeClass(ChargeClass chargeClass)
	{
		if (chargeClass == null)
		{
			logger.info("chargeClass is null ");
			return false;
		}

		try
		{
			chargeClass.setChargeClass(chargeClass.getChargeClass().toUpperCase());
			String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
			Object object = genericCache.getFromCache(cacheName, chargeClass.getChargeClass().toUpperCase() + ":" + chargeClass.getCircleID().toUpperCase());

			if (object != null)
			{
				if (object instanceof ChargeClass)
				{
					ChargeClassDao chargeClassDao = new ChargeClassDao();
					ChargeClass dbChargeClass = chargeClassDao.getChargeClass(chargeClass.getChargeClass());
					chargeClass.setCircleID(dbChargeClass.getCircleID());
					chargeClassDao.updateChargeClass(chargeClass);
					String[] circleIDs = chargeClass.getCircleID().split(",");
					for (String circleID : circleIDs)
					{
						ChargeClass tmpChargeClass = chargeClass.clone();
						tmpChargeClass.setCircleID(circleID);
						genericCache.updateToCache(cacheName, tmpChargeClass.getChargeClass() + ":" + circleID.toUpperCase(), tmpChargeClass);
					}

					return true;
				}
			}
			else
			{
				logger.info("The chargeClass does not exist in the Cache. Couldnt update ChargeClass " + chargeClass.getChargeClass());
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
	 * Remove a charge class from charge class table and cache
	 * 
	 * @param ChargeClass
	 * @return
	 */
	public boolean removeChargeClass(String chargeClass)
	{
		logger.info("chargeclass : " + chargeClass);

		chargeClass = chargeClass.toUpperCase();
		ChargeClass chargeClassObj = getChargeClass(chargeClass);

		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		ChargeClass chargeClassObject = (ChargeClass) genericCache.getFromCache(cacheName, chargeClass + ":" + chargeClassObj.getCircleID().toUpperCase());

		if (chargeClassObject == null)
		{
			logger.info("chargeclass does not exist");
			return false;
		}
		try
		{

			ChargeClassDao chargeClassDao = new ChargeClassDao();
			chargeClassDao.removeChargeClass(chargeClassObject);

			genericCache.removeFromCache(cacheName, chargeClass + ":" + chargeClassObj.getCircleID().toUpperCase());
			logger.info("chargeclass removed successfully");

			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean removeChargeClass(String chargeClass, String circleID)
	{
		logger.info("charClass : " + chargeClass);

		chargeClass = chargeClass.toUpperCase();

		String cacheName = CacheNamesEnum.CHARGE_CLASS_CACHE.toString();
		ChargeClass chargeClassObject = (ChargeClass) genericCache.getFromCache(cacheName, chargeClass + ":" + circleID.toUpperCase());
		if (chargeClassObject == null)
			return false;
		try
		{
			ChargeClassDao chargeClassDao = new ChargeClassDao();
			ChargeClass dbChargeClass = chargeClassDao.getChargeClass(chargeClass);

			if (dbChargeClass != null && (dbChargeClass.getCircleID().equalsIgnoreCase(circleID)))
			{
				return removeChargeClass(chargeClass);
			}
			else
			{
				String[] circleIDs = dbChargeClass.getCircleID().split(",");
				StringBuilder stringBuilder = new StringBuilder();
				for (String cirID : circleIDs)
				{
					if (!cirID.equalsIgnoreCase(circleID))
						stringBuilder.append(cirID + ",");
				}

				String csvCircleIDs = stringBuilder.toString();
				// to trim extra comma at the end
				csvCircleIDs = csvCircleIDs.substring(0, csvCircleIDs.length() - 1);
				dbChargeClass.setCircleID(csvCircleIDs);
				chargeClassDao.updateChargeClass(dbChargeClass);

				genericCache.removeFromCache(cacheName, chargeClass + ":" + circleID.toUpperCase());
			}

			logger.info("chargeclass removed successfully");
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * getChargeClassDetails
	 * 
	 * @return chargeclass object list
	 * @author bikash panda
	 */
	public ArrayList<ChargeClassBean> getCCCChargeClassDetails(int mode, String circleID)
	{
		if (circleID != null)
			circleID = circleID.toUpperCase();

		ArrayList<ChargeClassBean> chargeClassBeanList = new ArrayList<ChargeClassBean>();

		ParametersCacheManager paramsCacheManager = CacheManagerUtil.getParametersCacheManager();

		String currency = null;
		Parameters parameter = paramsCacheManager.getParameter("CCC", "CURRENCY_TYPE");
		if (parameter != null)
			currency = parameter.getValue();
		if (currency == null)
			currency = "Rs.";

		String chargeClassLifeTime = null;
		parameter = paramsCacheManager.getParameter("CCC", "SUBCLASS_LIFETIME_PERIOD");
		if (parameter != null)
			chargeClassLifeTime = parameter.getValue();

		List<String> tnbList = null;
		if (mode == 1)
		{
			String tnbChargeClasses = null;
			parameter = paramsCacheManager.getParameter("COMMON", "TNB_CHARGE_CLASSES");
			if (parameter != null)
				tnbChargeClasses = parameter.getValue();
			logger.info(tnbChargeClasses);

			tnbList = new ArrayList<String>();
			if (tnbChargeClasses != null)
				tnbList = Arrays.asList(tnbChargeClasses.split(","));
		}
		String normalChargeClasses = null;
		parameter = paramsCacheManager.getParameter("COMMON", "NORMAL_CHARGE_CLASSES");
		if (parameter != null){
			if(parameter.getValue()!=null){
				normalChargeClasses = parameter.getValue().trim();
			}
		}
		logger.info(normalChargeClasses);
		List<String> normalList = null;
		if (normalChargeClasses != null)
			normalList = Arrays.asList(normalChargeClasses.split(","));

		
		ArrayList<ChargeClass> chargeClassList = (ArrayList<ChargeClass>) getAllChargeClass();
		for (ChargeClass chargeClass : chargeClassList)
		{
			StringBuilder chargeClassDetails = new StringBuilder();

			if (chargeClass.getShowonGui() != null
					&& chargeClass.getShowonGui().equalsIgnoreCase("y")
					&& (chargeClass.getCircleID().equalsIgnoreCase("ALL")
							|| chargeClass.getCircleID().equalsIgnoreCase(circleID)))
			{
				String chargeClassName = chargeClass.getChargeClass();
				if(chargeClassName!=null){
					if (mode == 1)
					{
						if (tnbList.contains(chargeClassName))
						{
							ChargeClassBean chargeClassBean=getChargeClass( chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}
					}
					else
					{
						if(normalList!=null && normalList.size()>0 ){
							if(normalList.contains(chargeClassName)){
								ChargeClassBean chargeClassBean=getChargeClass(  chargeClass, chargeClassLifeTime, currency);
								chargeClassBeanList.add(chargeClassBean);
							}
						}else{
							ChargeClassBean chargeClassBean=getChargeClass(  chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}

					}
				}
			}
		}

		return chargeClassBeanList;
	}

	/**
	 * getChargeClassDetails
	 * 
	 * @return chargeclass object list
	 * @author bikash panda
	 */
	public ArrayList<ChargeClassBean> getCCCCorporateChargeClassDetails(String circleID)
	{
		if (circleID != null)
			circleID = circleID.toUpperCase();

		ArrayList<ChargeClassBean> chargeClassBeanList = new ArrayList<ChargeClassBean>();

		ParametersCacheManager paramsCacheManager = CacheManagerUtil.getParametersCacheManager();

		String currency = null;
		Parameters parameter = paramsCacheManager.getParameter("CCC", "CURRENCY_TYPE");
		if (parameter != null)
			currency = parameter.getValue();
		if (currency == null)
			currency = "Rs.";

		String chargeClassLifeTime = null;
		parameter = paramsCacheManager.getParameter("CCC", "SUBCLASS_LIFETIME_PERIOD");
		if (parameter != null)
			chargeClassLifeTime = parameter.getValue();

		List<String> corporateList = null;
		String corporateChargeClasses = null;
		parameter = paramsCacheManager.getParameter("COMMON", "CORPORATE_CHARGE_CLASSES");
		if (parameter != null){
			if(parameter.getValue()!=null){
				corporateChargeClasses = parameter.getValue().trim();
			}
		}
		logger.info(corporateChargeClasses);
		String normalChargeClasses = null;
		parameter = paramsCacheManager.getParameter("COMMON", "NORMAL_CHARGE_CLASSES");
		if (parameter != null){
			if(parameter.getValue()!=null){
				normalChargeClasses = parameter.getValue().trim();
			}
		}
		logger.info(normalChargeClasses);
		List<String> normalList = null;
		if (normalChargeClasses != null)
			normalList = Arrays.asList(normalChargeClasses.split(","));

		corporateList = new ArrayList<String>();
		if (corporateChargeClasses != null)
			corporateList = Arrays.asList(corporateChargeClasses.split(","));

		ArrayList<ChargeClass> chargeClassList = (ArrayList<ChargeClass>) getAllChargeClass();
		for (ChargeClass chargeClass : chargeClassList)
		{
			StringBuilder chargeClassDetails = new StringBuilder();

			if (chargeClass.getShowonGui() != null
					&& chargeClass.getShowonGui().equalsIgnoreCase("y")
					&& (chargeClass.getCircleID().equalsIgnoreCase("ALL")
							|| chargeClass.getCircleID().equalsIgnoreCase(circleID)))
			{
				String chargeClassName = chargeClass.getChargeClass();
				if (chargeClassName!=null )
				{
					if(corporateList!=null && corporateList.size()>0 ){
						if(corporateList.contains(chargeClassName)){
							ChargeClassBean chargeClassBean=getChargeClass( chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}
					}else if(normalList!=null && normalList.size()>0 ){
						if(normalList.contains(chargeClassName)){
							ChargeClassBean chargeClassBean=getChargeClass(  chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}
					}else{
						ChargeClassBean chargeClassBean=getChargeClass( chargeClass, chargeClassLifeTime, currency);
						chargeClassBeanList.add(chargeClassBean);
					}
				}

			}
		}
		return chargeClassBeanList;
	}

	private ChargeClassBean getChargeClass(ChargeClass chargeClass,String chargeClassLifeTime,String currency){
		StringBuilder chargeClassDetails = new StringBuilder();
		String chargeClassName=null;
		if(chargeClass!=null){
			chargeClassName=chargeClass.getChargeClass();
		}
		if (chargeClassLifeTime != null
				&& chargeClass.getRenewalPeriod().substring(1).equalsIgnoreCase(chargeClassLifeTime))
		{
			chargeClassDetails.append(currency);
			chargeClassDetails.append(chargeClass.getAmount());
			chargeClassDetails.append(" ");
			chargeClassDetails.append(currency);
		}
		else
		{
			chargeClassDetails.append(chargeClassName);
			chargeClassDetails.append(" [");
			chargeClassDetails.append(chargeClassName);
			chargeClassDetails.append(": ");
			chargeClassDetails.append(currency);
			chargeClassDetails.append(chargeClass.getAmount());
			chargeClassDetails.append("(");

			if (chargeClass.getSelectionPeriod().substring(0, 1).equals("M")
					|| chargeClass.getSelectionPeriod().substring(0, 1).equals("D"))
			{
				chargeClassDetails.append("First ");
				chargeClassDetails.append(chargeClass.getSelectionPeriod().substring(1));
				chargeClassDetails.append(" ");
				chargeClassDetails.append(chargePeriodMonthMap.get(chargeClass.getSelectionPeriod().substring(0, 1)));
				chargeClassDetails.append(")");
			}

			if (chargeClass.getRenewalAmount() != null)
			{
				chargeClassDetails.append(" / ");
				chargeClassDetails.append(" ");
				chargeClassDetails.append(currency);
				chargeClassDetails.append(chargeClass.getRenewalAmount());
				chargeClassDetails.append("(");

				if (chargeClass.getRenewalPeriod().substring(0, 1).equals("M")
						|| chargeClass.getRenewalPeriod().substring(0, 1).equals("D"))
				{
					chargeClassDetails.append("Every ");
					chargeClassDetails.append(chargeClass.getRenewalPeriod().substring(1));
					chargeClassDetails.append(" ");
					chargeClassDetails.append(chargePeriodMonthMap.get(chargeClass.getRenewalPeriod().substring(0, 1)));
					chargeClassDetails.append(")");
				}
			}
			else
			{
				chargeClassDetails.append(" / No Renewal");
			}

			chargeClassDetails.append("]");
		}
		ChargeClassBean chargeClassBean = new ChargeClassBean(chargeClassDetails.toString(), chargeClassName);
		return chargeClassBean;
	}

	/**
	 * getChargeClassDetails
	 * 
	 * @return chargeclass object list
	 * @author bikash panda
	 */
	public ArrayList<ChargeClassBean> getCCCChargeClassDetails(int mode)
	{
		ArrayList<ChargeClassBean> chargeClassBeanList = new ArrayList<ChargeClassBean>();

		ParametersCacheManager paramsCacheManager = CacheManagerUtil.getParametersCacheManager();

		String currency = null;
		Parameters parameter = paramsCacheManager.getParameter("CCC", "CURRENCY_TYPE");
		if (parameter != null)
			currency = parameter.getValue();
		if (currency == null)
			currency = "Rs.";

		String chargeClassLifeTime = null;
		parameter = paramsCacheManager.getParameter("CCC", "SUBCLASS_LIFETIME_PERIOD");
		if (parameter != null)
			chargeClassLifeTime = parameter.getValue();

		List<String> tnbList = null;
		if (mode == 1)
		{
			String tnbChargeClasses = null;
			parameter = paramsCacheManager.getParameter("COMMON", "TNB_CHARGE_CLASSES");
			if (parameter != null)
				tnbChargeClasses = parameter.getValue();
			logger.info(tnbChargeClasses);

			tnbList = new ArrayList<String>();
			if (tnbChargeClasses != null)
				tnbList = Arrays.asList(tnbChargeClasses.split(","));
		}
		String normalChargeClasses = null;
		parameter = paramsCacheManager.getParameter("COMMON", "NORMAL_CHARGE_CLASSES");
		if (parameter != null){
			if(parameter.getValue()!=null){
				normalChargeClasses = parameter.getValue().trim();
			}
		}
		logger.info(normalChargeClasses);
		List<String> normalList = null;
		if (normalChargeClasses != null)
			normalList = Arrays.asList(normalChargeClasses.split(","));

		ArrayList<ChargeClass> chargeClassList = (ArrayList<ChargeClass>) getAllChargeClass();
		for (ChargeClass chargeClass : chargeClassList)
		{

			if (chargeClass.getShowonGui() != null
					&& chargeClass.getShowonGui().equalsIgnoreCase("y"))
			{
				String chargeClassName = chargeClass.getChargeClass();
				if(chargeClassName!=null){
					if (mode == 1)
					{
						if (tnbList.contains(chargeClassName))
						{
							ChargeClassBean chargeClassBean=getChargeClass( chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}
					}
					else
					{
						if(normalList!=null && normalList.size()>0 ){
							if(normalList.contains(chargeClassName)){
								ChargeClassBean chargeClassBean=getChargeClass(  chargeClass, chargeClassLifeTime, currency);
								chargeClassBeanList.add(chargeClassBean);
							}
						}else{
							ChargeClassBean chargeClassBean=getChargeClass(  chargeClass, chargeClassLifeTime, currency);
							chargeClassBeanList.add(chargeClassBean);
						}

					}
				}
			}
		}
		
		return chargeClassBeanList;
	}

	private ChargeClass updateLanguageSpecificInfo(ChargeClass chargeClass, String language)
	{
		if (chargeClass == null)
			return null;

		if (language == null || language.length() == 0)
		{
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_LANGUAGE", "eng");
			language = parameter.getValue();
		}

		ChargeClass newChargeClass = chargeClass;
		String className = chargeClass.getChargeClass();

		try
		{
			newChargeClass = chargeClass.clone();
		}
		catch (CloneNotSupportedException e)
		{
		}

		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("CHARGE_CLASS_SMS_" + className, "CHARGE_SUCCESS", language);
		if (rbtText != null)
			newChargeClass.setSmschargeSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("CHARGE_CLASS_SMS_" + className, "CHARGE_FAILURE", language);
		if (rbtText != null)
			newChargeClass.setSmschargeFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("CHARGE_CLASS_SMS_" + className, "RENEWAL_SUCCESS", language);
		if (rbtText != null)
			newChargeClass.setSmsrenewalSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText("CHARGE_CLASS_SMS_" + className, "RENEWAL_FAILURE", language);
		if (rbtText != null)
			newChargeClass.setSmsrenewalFailure(rbtText.getText());


		return newChargeClass;
	}
}
