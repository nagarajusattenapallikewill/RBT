package com.onmobile.apps.ringbacktones.wrappers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.StringUtil;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubClassBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;

/**
 * @author abhinav.anand@onmobile.com
 */

/**
 * This is wrapper class for all API being supported by genericCache.
 * 1) Any component needing genericCache APIs should use APIs of this class only, which, in turn, internally 
 *    calls genericCache APIs.
 * 2) If any API needed is not available, create one in this class and then use APIs of this class. Please make
 *    sure there is one and only one API in this class corresponding to each and every genericCache APIs.
 * 3) Any business logic should be in the class calling APIs of this class. e.g.- for adding multiple parameters 
 *    in rbt_parameter DB table, do an iteration on 'addParameter(Parameters parameter)' of this class from 
 *    calling class. One should not create a new API in this class doing the same iteration as business logic 
 *    should be present only in calling class
 */
public class RbtGenericCacheWrapper 
{
	private static final String CLASSNAME = "RbtGenericCacheWrapper";
	
	public static final String CCC = "CCC";
	public static final String WEB = "WEB";
	public static final String WAP = "WAP";
	public static final String USSD = "USSD";
	public static final String SMS = "SMS";
	public static final String COMMON = "COMMON";
	public static final String GATHERER = "GATHERER";
	public static final String VIRAL = "VIRAL";
	public static final String WEBSERVICE = "WEBSERVICE";
	public static final String RDC = "RDC";
	public static final String SOCIAL = "SOCIAL";
	private static RbtGenericCacheWrapper rbtGenericCache = null;
	
	static Logger logger = Logger.getLogger(RbtGenericCacheWrapper.class);

	private RbtGenericCacheWrapper()
	{

	}
	
	private void init(){
		String method="init";
		try {
			Class.forName("com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil");
		} catch (ClassNotFoundException e) {
			logger.info(method+"->ClassnotFoundException while initializing CacheManagerUtil for Generic cahce");
			e.printStackTrace();
		}
	}
	
	/* 1) Any component which need ALL three of genericCache,memCache,rbtClient MUST call 
	 *    'getInstance()' of 'RBTConnector' class. That must not call 'getInstance()' of this class
	 * 2) This API should be called only by a component which need genericCache APIs but DON'T need APIs of both 
	 * 	  of memCache as well as rbtClient.
	 */
	public static RbtGenericCacheWrapper getInstance()
	{
		if(rbtGenericCache == null){
				rbtGenericCache=new RbtGenericCacheWrapper();
				rbtGenericCache.init();
		}
		return rbtGenericCache;
	}

	//rbt_charge_class APIs
	
	/* This add a new Entry in rbt_charge_class DB table. */
	public void addChargeClass(ChargeClass chargeClass){
		CacheManagerUtil.getChargeClassCacheManager().addChargeClass(chargeClass);
	}
	/* This remove an existing Entry in rbt_charge_class DB table. Input argument is 'chargeClass'(String) name */
	public void removeChargeClass(String chargeClassName){
		CacheManagerUtil.getChargeClassCacheManager().removeChargeClass(chargeClassName);
	}
	/* This update an existing Entry in rbt_charge_class DB table. Input argument is an 'ChargeClass'Object */
	public void updateChargeClass(ChargeClass chargeClass){
		CacheManagerUtil.getChargeClassCacheManager().updateChargeClass(chargeClass);
	}
	/* This returns all entries in rbt_charge_class DB table for which 'showOnGIU=='y'/'n'.
	 * Input argument is boolean bShowOnGUI*/
	public List<ChargeClass> getGUIChargeClassList(boolean bShowOnGUI){
		return CacheManagerUtil.getChargeClassCacheManager().getChargeClassesGUI(bShowOnGUI);
	}
	/* This returns all entries in rbt_charge_class DB table for which is for trial.
	 */
	public List<ChargeClass> getTrialChargeClassList(){
		return CacheManagerUtil.getChargeClassCacheManager().getTrialChargeClasses();
	}
	public ChargeClass getChargeClass(String chargeclass){
		return CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeclass);
	}
	public List<ChargeClass> getAllChargeClassList(int mode){
		return CacheManagerUtil.getChargeClassCacheManager().getAllChargeClass();
	}
	public List<ChargeClassBean> getCCCChargeClassBeanList(int mode){
		return CacheManagerUtil.getChargeClassCacheManager().getCCCChargeClassDetails(mode);
	}
	
	//rbt_subscription_class APIs
	public void addSubscriptionClass(SubscriptionClass subClass){
		 CacheManagerUtil.getSubscriptionClassCacheManager().addSubscriptionClass(subClass);
	}
	public void removeSubscriptionClass(String subClassName){
		 CacheManagerUtil.getSubscriptionClassCacheManager().removeSubscriptionClass(subClassName);
	}
	public void updateSubscriptionClass(SubscriptionClass subClass){
		 CacheManagerUtil.getSubscriptionClassCacheManager().updateSubscriptionClass(subClass);
	}
	public SubscriptionClass getSubscriptionClassByName(String subClassName){
		return CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subClassName);
	}
	public List<SubscriptionClass> getAllSubscriptionClassList(){
		return CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses();
	}
	public List<SubClassBean> getCCCSubscriptionClassBeanList(int mode,HashMap<String, String> subscriptionPeriod){
		return CacheManagerUtil.getSubscriptionClassCacheManager().getCCCSubscriptionClassDetails(mode,subscriptionPeriod);
	}
	
	//rbt_parameters APIs
	public List<String> getAllParamatersTypes()
	{
		return CacheManagerUtil.getParametersCacheManager().getAllParametersTypes();
	}
	
	public int getIntParameter(String channelType, String name, int defaultVal)
    {
	    String value = getParameter(channelType, name, String.valueOf(defaultVal));
	    if(value == null)
	    {
	        return defaultVal;
	    }
        int intValue = StringUtil.getInteger(value);
        if(intValue == -1)
        {
            return defaultVal;
        }
        return intValue;
    }
	
	public String getParameter(String channelType,String name, String defaultVal){
		Parameters param= CacheManagerUtil.getParametersCacheManager().getParameter(channelType, name,defaultVal);
		if(param!=null){
			return param.getValue();
		}
		return null;
	}
	public String getParameter(String channelType,String name)
	{
		Parameters param= CacheManagerUtil.getParametersCacheManager().getParameter(channelType, name);
		if(param!=null){
			return param.getValue();
		}
		return null;
	}
	public List<Parameters> getAllParamaters(String channelType)
	{
		return CacheManagerUtil.getParametersCacheManager().getParameters(channelType);
	}
	public List<Parameters> getAllParamaters()
	{
		return CacheManagerUtil.getParametersCacheManager().getAllParameters();
	}
	public boolean insertOrEditParameter(String type,String paramName,String value)
	{
		boolean success=CacheManagerUtil.getParametersCacheManager().updateParameter(type,paramName,value);
		if(!success){
			success=CacheManagerUtil.getParametersCacheManager().addParameter(type,paramName,value);
		}
		return success;
	}
	public boolean updateParameter(String type,String paramName,String value)
	{
		return CacheManagerUtil.getParametersCacheManager().updateParameter(type,paramName,value);
	}
	public boolean addParameter(String type,String paramName,String value)
	{
		return CacheManagerUtil.getParametersCacheManager().addParameter(type,paramName,value);
	}
	public boolean removeParameter(String type, String name)
	{
		if(type == null || name == null){
			return false;
		}
		boolean success=CacheManagerUtil.getParametersCacheManager().removeParameter(type,name);
		return success;
	}
	
	
	//rbt_charge_class_map APIs
	public List<ChargeClassMap> getAllChargeClassMaps()
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getAllChargeClassMap();
	}
	public List<ChargeClassMap> getAllChargeClassMapsForClassTypeNType(String classType,String type)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForClassTypeType(classType, type);
	}
	public List<ChargeClassMap> getAllChargeClassMapsForFinalClassType(String finalClassType,String accessedMode)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForFinalClassType(finalClassType, accessedMode);
	}
	public List<ChargeClassMap> getAllChargeClassMapsForMode(String mode,String type)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForModeType(mode, type);
	}
	public List<ChargeClassMap> getAllChargeClassMapsForType(String regexTypes)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForType(regexTypes);
	}
	public ChargeClassMap getChargeClassMap(String chargeclass)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMap(chargeclass);
	}
	public ChargeClassMap getChargeClassMapForModeRegexTypeAndClassType(String mode,String regexType,String classType)
	{
		return CacheManagerUtil.getChargeClassMapCacheManager().getChargeClassMapsForModeRegexTypeAndClassType(mode, regexType, classType);
	}
	
	//rbt_cos_detail APIs
	public void addCosDetails(CosDetails cosDetails)
	{
		 CacheManagerUtil.getCosDetailsCacheManager().addCosDetail(cosDetails);
	}
	public List<CosDetails> getAllCosDetails()
	{
		 return CacheManagerUtil.getCosDetailsCacheManager().getAllCosDetails();
	}
	public CosDetails getCosDetail(String cosID,String circleID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID, circleID);
	}
	public void getActiveCosDetail(String cosID,String circleID)
	{
		 CacheManagerUtil.getCosDetailsCacheManager().getActiveCosDetail(cosID, circleID);
	}
	public List<CosDetails> getAllCosDetails(String prepaidYes,String circleID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getAllCosDetails(circleID, prepaidYes);
	}
	public List<CosDetails> getCosDetails(String cosID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getCosDetails(cosID);
	}
	public List<CosDetails> getCosForPromoUpdate(String cosID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getCosForPromoUpdate();
	}
	public CosDetails getDefaultCosDetail(String prepaidYes,String circleID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(circleID, prepaidYes);
	}
	public CosDetails getSmsKeywordCosDetail(String prepaidYes,String circleID,String smsKeyword)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().getSmsKeywordCosDetail(smsKeyword, circleID, prepaidYes);
	}
	public boolean isTrialAvailable(String prepaidYes,String circleID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().isTrialAvailable(circleID, prepaidYes);
	}
	public boolean isWTPacksAvailable(String prepaidYes,String circleID)
	{
		return CacheManagerUtil.getCosDetailsCacheManager().isWTPacksAvailable(circleID, prepaidYes);
	}
	
	//rbt_bulk_promo_sms APIs
	public boolean addBulkPromoSmS(String promoID,String smsDate,String smsText,String smsSent)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().addBulkPromoSmS(promoID, smsDate, smsText, smsSent);
	}
	public boolean update(String promoID,String smsDate,String smsText,String smsSent)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().update(promoID, smsDate, smsText, smsSent);
	}
	public boolean updateSMSSent(String promoID,String smsDay,String smsSent)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().updateSMSSent(promoID, smsDay, smsSent);
	}
	public void updateSMSSent(String promoID,int smsDay,String smsSent)
	{
		 CacheManagerUtil.getBulkPromoSMSCacheManager().updateSMSSent(promoID, smsDay, smsSent);
	}
	public void removeBulkPromoSMS(String promoID,String smsDate)
	{
		 CacheManagerUtil.getBulkPromoSMSCacheManager().removeBulkPromoSMS(promoID, smsDate);
	}
	public List<BulkPromoSMS> getAllBulkPromoSMS()
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getAllBulkPromoSMS();
	}
	public List<BulkPromoSMS> getBulkPromoSMSes()
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSes();
	}
	public List<BulkPromoSMS> getBulkPromoSMSes(String promoId)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSes(promoId);
	}
	public List<BulkPromoSMS> getAllPromoIDSMSes(String promoID)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getAllPromoIDSMSes(promoID);
	}
	public BulkPromoSMS getBulkPromoSMS(String promoID,int smsDay)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMS(promoID, smsDay);
	}
	public BulkPromoSMS getBulkPromoSMS(String promoID,String smsDate)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMS(promoID, smsDate);
	}
	public BulkPromoSMS getBulkPromoSMSForDate(String bulkPromoID,String smsDate)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSForDate(bulkPromoID, smsDate);
	}
	public List<BulkPromoSMS> getBulkPromoSMSForDate(Date smsDate)
	{
		return CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSForDate(smsDate);
	}
}
