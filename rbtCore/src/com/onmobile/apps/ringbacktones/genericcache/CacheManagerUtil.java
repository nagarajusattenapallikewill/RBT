package com.onmobile.apps.ringbacktones.genericcache;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A utility class for all the caches. It has API's to convert the default cache
 * beans to RBT specific beans. It also gives APIs to get the various cache
 * managers. All the beans for the cache managers are defined in
 * rbt-cache-beans.xml file. Springs take care of injecting these cache manager
 * beans into CacheManagerUtil object.
 * 
 * @author manish.shringarpure
 */
public class CacheManagerUtil
{
	private static BeanFactory beanFactory = new ClassPathXmlApplicationContext("/rbt-cache-beans.xml");

	private static BulkPromoSMSCacheManager bulkPromoSMSCacheManager;
	private static ChargeClassCacheManager chargeClassCacheManager;
	private static ChargeClassMapCacheManager chargeClassMapCacheManager;
	private static ChargeSMSCacheManager chargeSMSCacheManager;
	private static CosDetailsCacheManager cosDetailsCacheManager;
	private static ParametersCacheManager parametersCacheManager;
	private static PredefinedGroupCacheManager predefinedGroupCacheManager;
	private static RBTTextCacheManager rbtTextCacheManager;
	private static SitePrefixCacheManager sitePrefixCacheManager;
	private static SmsTextCacheManager smsTextCacheManager;
	private static SubscriptionClassCacheManager subscriptionClassCacheManager;

	/**
	 * The API gives the springs BeanFactory to inject beans.
	 */
	public static BeanFactory getBeanFactory()
	{
		return beanFactory;
	}

	/**
	 * Gives the BulkPromoSMSCacheManager to access the BulkPromoSMS cache.
	 * 
	 * @return BulkPromoSMSCacheManager
	 */
	public static BulkPromoSMSCacheManager getBulkPromoSMSCacheManager()
	{
		return bulkPromoSMSCacheManager;
	}

	/**
	 * Sets the BulkPromoSMSCacheManager. Users are not supposed to call this
	 * API directly. Springs will take care of injecting it in CacheManagerUtil
	 * 
	 * @param BulkPromoSMSCacheManager
	 */
	public void setBulkPromoSMSCacheManager(BulkPromoSMSCacheManager bulkPromoSMSCacheManager)
	{
		CacheManagerUtil.bulkPromoSMSCacheManager = bulkPromoSMSCacheManager;
	}

	public static ChargeClassCacheManager getChargeClassCacheManager()
	{
		return chargeClassCacheManager;
	}

	public void setChargeClassCacheManager(ChargeClassCacheManager chargeClassCacheManager)
	{
		CacheManagerUtil.chargeClassCacheManager = chargeClassCacheManager;
	}

	public static ChargeClassMapCacheManager getChargeClassMapCacheManager()
	{
		return chargeClassMapCacheManager;
	}

	public void setChargeClassMapCacheManager(ChargeClassMapCacheManager chargeClassMapCacheManager)
	{
		CacheManagerUtil.chargeClassMapCacheManager = chargeClassMapCacheManager;
	}

	public static ChargeSMSCacheManager getChargeSMSCacheManager()
	{
		return chargeSMSCacheManager;
	}

	public void setChargeSMSCacheManager(ChargeSMSCacheManager chargeSMSCacheManager)
	{
		CacheManagerUtil.chargeSMSCacheManager = chargeSMSCacheManager;
	}

	/**
	 * Gives the CosDetailsCacheManager to access the CosDetails cache.
	 * 
	 * @return CosDetailsCacheManager
	 */
	public static CosDetailsCacheManager getCosDetailsCacheManager()
	{
		return cosDetailsCacheManager;
	}

	/**
	 * Sets the CosDetailsCacheManager. Users are not supposed to call this API
	 * directly. Springs will take care of injecting it in CacheManagerUtil
	 * 
	 * @param CosDetailsCacheManager
	 */
	public void setCosDetailsCacheManager(CosDetailsCacheManager cosDetailsCacheManager)
	{
		CacheManagerUtil.cosDetailsCacheManager = cosDetailsCacheManager;
	}

	/**
	 * Gives the ParametersCacheManager to access the parameters cache.
	 * 
	 * @return ParametersCacheManager
	 */
	public static ParametersCacheManager getParametersCacheManager()
	{
		return parametersCacheManager;
	}

	/**
	 * Sets the ParametersCacheManager. Users are not supposed to call this API
	 * directly. Springs will take care of injecting it in CacheManagerUtil
	 * 
	 * @param parametersCacheManager
	 */
	public void setParametersCacheManager(ParametersCacheManager parametersCacheManager)
	{
		CacheManagerUtil.parametersCacheManager = parametersCacheManager;
	}

	/**
	 * Gives the PredefinedGroupCacheManager to access the parameters cache.
	 * 
	 * @return PredefinedGroupCacheManager
	 */
	public static PredefinedGroupCacheManager getPredefinedGroupCacheManager()
	{
		return predefinedGroupCacheManager;
	}

	/**
	 * Sets the PredefinedGroupCacheManager. Users are not supposed to call this API
	 * directly. Springs will take care of injecting it in CacheManagerUtil
	 * 
	 * @param predefinedGroupCacheManager
	 */
	public void setPredefinedGroupCacheManager(PredefinedGroupCacheManager predefinedGroupCacheManager)
	{
		CacheManagerUtil.predefinedGroupCacheManager = predefinedGroupCacheManager;
	}

	/**
	 * Gives the RBTTextCacheManager to access the RBTText cache.
	 * 
	 * @return RBTTextCacheManager
	 */
	public static RBTTextCacheManager getRbtTextCacheManager()
	{
		return rbtTextCacheManager;
	}

	/**
	 * Sets the RBTTextCacheManager. Users are not supposed to call
	 * this API directly. Springs will take care of injecting it in
	 * CacheManagerUtil
	 * 
	 * @param rbtTextCacheManager
	 */
	public void setRbtTextCacheManager(RBTTextCacheManager rbtTextCacheManager)
	{
		CacheManagerUtil.rbtTextCacheManager = rbtTextCacheManager;
	}

	public static SitePrefixCacheManager getSitePrefixCacheManager()
	{
		return sitePrefixCacheManager;
	}

	public void setSitePrefixCacheManager(SitePrefixCacheManager sitePrefixCacheManager)
	{
		CacheManagerUtil.sitePrefixCacheManager = sitePrefixCacheManager;
	}

	/**
	 * Gives the SmsTextCacheManager to access the SMS text cache.
	 * 
	 * @return SmsTextCacheManager
	 */
	public static SmsTextCacheManager getSmsTextCacheManager()
	{
		return smsTextCacheManager;
	}

	/**
	 * Sets the SmsTextCacheManager. Users are not supposed to call
	 * this API directly. Springs will take care of injecting it in
	 * CacheManagerUtil
	 * 
	 * @param smsTextCacheManager
	 */
	public void setSmsTextCacheManager(SmsTextCacheManager smsTextCacheManager)
	{
		CacheManagerUtil.smsTextCacheManager = smsTextCacheManager;
	}

	/**
	 * Gives the SubscriptionClassCacheManager to access the Subscription Class cache.
	 * 
	 * @return SubscriptionClassCacheManager
	 */
	public static SubscriptionClassCacheManager getSubscriptionClassCacheManager()
	{
		return subscriptionClassCacheManager;
	}

	/**
	 * Sets the SubscriptionClassCacheManager. Users are not supposed to call
	 * this API directly. Springs will take care of injecting it in
	 * CacheManagerUtil
	 * 
	 * @param SubscriptionClassCacheManager
	 */
	public void setSubscriptionClassCacheManager(SubscriptionClassCacheManager subscriptionClassCacheManager)
	{
		CacheManagerUtil.subscriptionClassCacheManager = subscriptionClassCacheManager;
	}
}
