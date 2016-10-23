package com.onmobile.apps.ringbacktones.genericcache.interfaces;

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

/**
 * This interface should be implemented by the classes which need to provide the
 * caching objects for any of the caching mechanisms to cache in their
 * respective cache objects. The implementing classes could either fetch the
 * data from DB or any file or any other source if required.
 * 
 * @author manish.shringarpure
 */
public interface ICacheGenerator
{
	/**
	 * This API gives a map with key as "type:param" and value as Parameters
	 * bean
	 * 
	 * @return Map<String, Map<String, Parameters>>
	 */
	public Map<String, Map<String, Parameters>> getParametersMapForCaching();

	/**
	 *This API gives a map with key as "subscriptionClass" and value as
	 * SubscriptionClass Bean
	 * 
	 * @return Map<String, SubscriptionClass>
	 */
	public Map<String, SubscriptionClass> getSubscriptionClassMapForCaching();

	/**
	 * This API gives a map with key as CosID and value as CosDetails bean List
	 * 
	 * @return Map<String, List<CosDetails>>
	 */
	public Map<String, List<CosDetails>> getCosDetailsMapForCaching();

	/**
	 * This API gives a map with key as "ChargeClass" and value as CosDetails
	 * bean
	 * 
	 * @return Map<String, ChargeClass>>
	 */
	public Map<String, ChargeClass> getChargeClassMapForCaching();

	/**
	 * This API gives a map with key as "bulkPromoID:smsDate" and value as BulkPromoSMS bean
	 * 
	 * @return Map<String, BulkPromoSMS>
	 */
	public Map<String, BulkPromoSMS> getBulkPromoSMSMapForCaching();

	/**
	 * This API gives a map with key as "ChargeClass" and value as ChargeClassMap
	 * bean
	 * 
	 * @return Map<String, ChargeClassMap>>
	 */
	public Map<String, ChargeClassMap> getChargeClassMapMapForCaching();

	/**
	 * This API gives a map with key as "circleID" and value as SitePrefix
	 * bean
	 * @return Map<String, SitePrefix>
	 */
	public Map<String, SitePrefix> getSitePrefixMapForCaching();

	/**
	 * This API gives a map with key as "type_subType_language" and value as RBTText
	 * bean
	 * 
	 * @return Map<String, RBTText>
	 */
	public Map<String, RBTText> getRBTTextMapForCaching();

	/**
	 * This API gives a map with key as "preGroupID" and value as PredefinedGroup
	 * bean
	 * @return Map<String, PredefinedGroup>
	 */
	public Map<String, PredefinedGroup> getPredefinedGroupMapForCaching();
}
