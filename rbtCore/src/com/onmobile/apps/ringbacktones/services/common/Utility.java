package com.onmobile.apps.ringbacktones.services.common;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.messaging.support.GenericMessage;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.ConsentTableImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.inline.IMessageSender;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IClipStatusMappingDAO;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class Utility
{
	private static Logger logger = Logger.getLogger(Utility.class);
	
	// Third Party confirmation chages 
	public static int tpcgModesListSize = 0;
	public static List<String> tpcgModesList = new ArrayList<String>();
	private static Map<String, String> tpcgSubClassMap = new HashMap<String, String>();
	private static Map<String, String> tpcgChargeClassMap = new HashMap<String, String>();
	private static Map<String, String> circleIdMap = null;
	//RBT-9873
	public static List<String> tpcgSubscriptionClassList = new ArrayList<String>();
	public static List<String> tpcgChargeClassList = new ArrayList<String>();
	public static List<String> vfUpgradeNonCheckModesList = new ArrayList<String>();
	private static IMessageSender messageSender;
	static {				
		String thirdPartyConfirmationModes = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_MODES",
						null);
		tpcgModesList = ListUtils.convertToList(
				thirdPartyConfirmationModes, ",");
		tpcgModesListSize = tpcgModesList.size();
		
		String thirdPartyConfirmationSubClass = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_SUB_CLASS_MAP",
						null);
		
		tpcgSubClassMap = MapUtils.convertToMap(thirdPartyConfirmationSubClass,
				";", "=", ",");
		
		String thirdPartyConfirmationChargeClass = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_CHARGE_CLASS_MAP",
						null);
		
		tpcgChargeClassMap = MapUtils.convertToMap(thirdPartyConfirmationChargeClass,
				";", "=", ",");
		circleIdMap = MapUtils.convertToMap(CacheManagerUtil.getParametersCacheManager().
				getParameterValue(iRBTConstant.DOUBLE_CONFIRMATION, "CONSENT_GATEWAY_CIRCLE_MAPS",null), ",", ":", null);
		
		String VfUpgradeFeatureClass = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.COMMON,
						"CREATE_CLASS_FOR_VF_UPGRADE_FEATURE", null);
		if (VfUpgradeFeatureClass != null && !VfUpgradeFeatureClass.isEmpty()) {
			String upgradeModes = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON,
							"VODAFONE_UPGRADE_CONSENT_MODES", null);
			if (upgradeModes != null)
				vfUpgradeNonCheckModesList = Arrays.asList(upgradeModes
						.split(","));
		}
		
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false")) {
			try {
				messageSender = (IMessageSender) ConfigUtil.getBean(BeanConstant.INLINE_MESSAGE_SENDER_BEAN);
			} catch (Exception e) {
				logger.error("Inline message sender bean is not found !!");
			}
		}
	}

	public static String trimCountryPrefix(String subscriberID)
	{
		if (subscriberID != null)
		{
			try
			{
				String countryCodePrefix = "91";
				int minPhoneNumberLen = 10;

				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
				if (parameter != null)
					countryCodePrefix = parameter.getValue();

				parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "MIN_PHONE_NUMBER_LEN", "10");
				if (parameter != null)
				{
					try
					{
						minPhoneNumberLen = Integer.parseInt(parameter.getValue());
					}
					catch (Exception e)
					{
						minPhoneNumberLen = 10;
					}
				}

				if (countryCodePrefix == null || countryCodePrefix.trim().equals(""))
					countryCodePrefix = "91";

				String[] countryCodePrefixes = countryCodePrefix.split(",");
				for (String prefix : countryCodePrefixes)
				{
					if (subscriberID.startsWith("00"))
						subscriberID = subscriberID.substring(2);
					if (subscriberID.startsWith("+")
							|| subscriberID.startsWith("0")
							|| subscriberID.startsWith("-"))
						subscriberID = subscriberID.substring(1);
					if (subscriberID.startsWith(prefix)
							&& (subscriberID.length() >= (minPhoneNumberLen + prefix.length())))
					{
						subscriberID = subscriberID.substring(prefix.length());
						break;
					}
				}
			}
			finally
			{
				if (subscriberID.startsWith("00"))
					subscriberID = subscriberID.substring(2);
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("0")
						|| subscriberID.startsWith("-"))
					subscriberID = subscriberID.substring(1);
			}
		}

		return subscriberID;
	}

	public static SitePrefix getPrefix(String subscriberID)
	{
		SitePrefix userSitePrefix = null;
		List<SitePrefix> sitePrefixList = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (sitePrefixList == null)
			return null;

		for (SitePrefix sitePrefix : sitePrefixList)
		{
			String prefix = sitePrefix.getSitePrefix();
			String[] prefixes = prefix.split(",");
			for (String prefixToken : prefixes)
			{
				if (subscriberID.startsWith(prefixToken))
				{
					if (sitePrefix.getAccessAllowed().equalsIgnoreCase("true"))
						userSitePrefix = sitePrefix;
				}
			}
		}

		return userSitePrefix;
	}

	public static String getMappedCircleID(String circleID)
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "THIRD_PARTY_CIRCLE_MAPS");
		if (parameter != null)
		{
			String[] circleIDMaps = parameter.getValue().split(",");
			for (String circleIDMap : circleIDMaps)
			{
				int index = circleIDMap.indexOf(":");

				String thirdPartyCircleID = circleIDMap;
				String rbtCircleID = circleIDMap;
				if (index != -1)
				{
					thirdPartyCircleID = circleIDMap.substring(0, index);
					rbtCircleID = circleIDMap.substring(index + 1);
				}

				if (thirdPartyCircleID.equals(circleID))
					return rbtCircleID;
			}
		}

		return null;
	}

	
	public static String getThirdPartyMappedCircleID(String operator_circleID)
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "THIRD_PARTY_OPERATORNAME_CIRCLE_MAPS");
		if (parameter != null)
		{
			String[] circleIDMaps = parameter.getValue().split(",");
			for (String circleIDMap : circleIDMaps)
			{
				int index = circleIDMap.indexOf(":");

				String thirdPartyCircleID = circleIDMap;
				String rbtCircleID = circleIDMap;
				if (index != -1)
				{
					thirdPartyCircleID = circleIDMap.substring(0, index);
					rbtCircleID = circleIDMap.substring(index + 1);
				}

				if (thirdPartyCircleID.equals(operator_circleID))
					return rbtCircleID;
			}
		}

		return null;
	}
	public static String getMappedCircleIDCached(String circleID)
	{
		if(circleIdMap.containsKey(circleID))
			return circleIdMap.get(circleID);
		return circleID;
	}

	public static boolean isValidTypePrefix(String subscriberID, String paramName)
	{
		HashMap<Integer, ArrayList<String>> prefixMap = initializeTypePrefix(paramName);

		if (subscriberID == null || subscriberID.length() < 7
				|| subscriberID.length() > 15 || prefixMap == null
				|| prefixMap.size() <= 0)
		{
			return false;
		}

		try
		{
			Long.parseLong(trimCountryPrefix(subscriberID));
		}
		catch (Throwable e)
		{
			logger.error("", e);
			return false;
		}

		Set<Integer> keySet = prefixMap.keySet();
		for (Integer key : keySet)
		{
			int prefixLength = key.intValue();
			String thisPrefix = subscriberID.substring(0, prefixLength);
			ArrayList<String> prefixList = prefixMap.get(key);
			if (prefixList.contains(thisPrefix))
			{
				logger.info("RBT: Valid prefix");
				return true;
			}
		}

		return false;
	}

	public static HashMap<Integer, ArrayList<String>> initializeTypePrefix(String paramName)
	{
		String prefixes = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.GATHERER, paramName);
		if (parameter != null)
			prefixes = parameter.getValue();

		HashMap<Integer, ArrayList<String>> prefixMap = null;
		if (prefixes != null && prefixes.length() > 0)
		{
			prefixMap = new HashMap<Integer, ArrayList<String>>();

			String[] prefixTokens = prefixes.split(",");
			for (String prefix : prefixTokens)
			{
				prefix = prefix.trim();
				int prefixLength = prefix.length();

				if (prefixLength <= 0)
					continue;

				ArrayList<String> prefixList = null;
				if (prefixMap.containsKey(prefixLength))
					prefixList = prefixMap.get(prefixLength);
				else
				{
					prefixList = new ArrayList<String>();
					prefixMap.put(prefixLength, prefixList);
				}

				prefixList.add(prefix);
			}
		}

		logger.info("RBT:: paramName: " + paramName + " prefixMap: " + prefixMap);
		return prefixMap;
	}

	public static boolean isValidNumber(String subscriberID)
	{
		subscriberID = trimCountryPrefix(subscriberID);

		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.GATHERER, "PHONE_NUMBER_LENGTH_MIN", "10");
		int minPhoneNumberLength = Integer.parseInt(parameter.getValue()); 

		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.GATHERER, "PHONE_NUMBER_LENGTH_MAX", "10");
		int maxPhoneNumberLength = Integer.parseInt(parameter.getValue());
		
		//Area Code to be in the form of 11:9,12:8,13:10 etc. i.e. areaCode:length
		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "AREA_CODE_FOR_PHONE_NUMBER_LENGTH", null);
		if(parameter!=null && subscriberID != null){
		  String paramVal = parameter.getValue();
		  if(paramVal!=null){
			 String token[] = paramVal.split(",");
               for(int i=0;i<token.length;i++){
					String areaCodeToken[] = token[i].split(":");
					if(subscriberID.startsWith(areaCodeToken[0])){
						try {
							int areaCodeLength = Integer.parseInt(areaCodeToken[1]);
							if (subscriberID.length() == (areaCodeLength + areaCodeToken[0].length())) {
								subscriberID = subscriberID.substring(areaCodeToken[0].length());
								if (subscriberID.length() == Integer.parseInt(areaCodeToken[1]))
									return true;
								else
									return false;
							}
						}catch(Throwable e){
					         return false;	    	
					   }
 				  }
              }
		   }
		}

		if (subscriberID == null || subscriberID.length() < minPhoneNumberLength || subscriberID.length() > maxPhoneNumberLength)
			return false;

		try
		{
			Long.parseLong(subscriberID);
		}
		catch (Throwable e)
		{
			return false;
		}

		return true;
	}
	
	/**
	 * Third Party confirmation chages 
	 * 
	 * @param mode
	 * @return
	 */
	public static boolean isThirdPartyConfirmationRequired(String mode,
			String extraInfo) {
		HashMap<String, String> map = DBUtility
				.getAttributeMapFromXML(extraInfo);
		return isThirdPartyConfirmationRequired(mode, map);
	}

	public static boolean isThirdPartyConfirmationRequired(String mode,
			HashMap<String, String> map) {
		
		logger.info("Checking third pary confirmation is required, mode: "
				+ mode + ", extraInfo map: " + map);
		boolean isTpcgidNotExists = true;
		boolean isModeExists = false;
		if (null != map && map.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
			isTpcgidNotExists = false;
		}
		
		if (tpcgModesListSize > 0
				&& (mode != null && (tpcgModesList.contains(mode.toUpperCase()) || tpcgModesList
						.contains("ALL")))) {
			isModeExists = true;
		}

		// If mode is present and tpcgid is not present then confirmation
		// is required. 
		if (isModeExists && isTpcgidNotExists) {
			logger.info("For the mode: " + mode
					+ ", third party confirmation is required.");
			return true;
		}
		logger.debug("Third party confirmation is NOT required. isModeExists: "
				+ isModeExists + ", isTpcgidNotExists: " + isTpcgidNotExists+", returning false.");
		return false;
	}
	
	/**
	 * @param subClass
	 * @param param. The param value 1 means validity and 2 means price. 
	 * @return
	 */
	private static String getTPCGConsentDetails(Map<String, String> map, String subClass, int param) {
		if(null != map && null != subClass && (param == 1 || param == 2)) {
			String details = map.get(subClass);
			if (null != details) {
				String values[] = details.split(",");
				if (values.length == 2) {
					if (param == 1) {
						return values[0].trim();
					} else {
						return values[1].trim();
					}
				}
			} else {
				logger.warn("Subscription /Charge class: " + subClass
						+ " is not present in configurtion. map: " + map);
			}
		}
		return "";
	}
	
	public static String getTPCGSubClassPrice(String subClass) {
		return getTPCGConsentDetails(tpcgSubClassMap, subClass, 1);
	}
	
	public static String getTPCGSubClassValidity(String subClass) {
		return getTPCGConsentDetails(tpcgSubClassMap, subClass, 2);
	}
	
	public static String getTPCGChargeClassPrice(String classType) {
		return getTPCGConsentDetails(tpcgChargeClassMap, classType, 1);
	}
	
	public static String getTPCGChargeClassValidity(String classType) {
		return getTPCGConsentDetails(tpcgChargeClassMap, classType, 2);
	}
	
	
	public static String generateConsentIdRandomNumber(String msisdn) {
		String isShortenConsentTransIdStr = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.DOUBLE_CONFIRMATION, iRBTConstant.IS_SHORTEN_CONSENT_TRANID, "false");
		String transId = null;		
		boolean isShortenConsentTransId = Boolean.valueOf(isShortenConsentTransIdStr);
		logger.debug("Checking to shorten consent trans id. isShortenConsentTransId: "
				+ isShortenConsentTransId);
		if (isShortenConsentTransId) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Connection conn = RBTDBManager.getInstance().getConnection();
			try {
				String sq_sequence = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.COMMON, "SQUENCE_NAME",
								"sq_sequence");
				transId = ConsentTableImpl.getSquenceNumber(conn, sq_sequence);
				logger.info("15 digit Consent trans Id after updated with msisdn no==="
						+ transId);
				// CG Integration Flow - Jira -12806
				boolean checkCGFlowForBSNL = RBTParametersUtils
						.getParamAsBoolean(iRBTConstant.DOUBLE_CONFIRMATION,
								"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
				String configuredFormatForBsnl = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.DOUBLE_CONFIRMATION,
								iRBTConstant.CPID_FORMAT_FOR_BSNL, null);
				String configuredFormat = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.DOUBLE_CONFIRMATION,
								iRBTConstant.CPID_FORMAT,
								configuredFormatForBsnl);
				if (null != configuredFormat) {
					transId = configuredFormat + transId;
				}
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				RBTDBManager.getInstance().releaseConnection(conn);
			}
		}
		
		return transId;
	}
	public static boolean isModeConfiguredForConsent(String mode) {
		if(mode == null) {
			return false;
		}
		return tpcgModesList.contains(mode.toUpperCase())
				|| tpcgModesList.contains("ALL");

	}
	
	//RBT-9873 Added for getting configured subscription class for CG flow
	public static boolean isSubscriptionClassConfiguredForNotCGFlow(String subscriptionClass) {
		if(subscriptionClass==null) {
			return false;
		}
		String thirdPartyConfirmNotReqSubsClass = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.DOUBLE_CONFIRMATION, "SUB_CLASSES_FOR_NOT_CG_FLOW",
						null);
		
		if(thirdPartyConfirmNotReqSubsClass!=null && tpcgSubscriptionClassList.size()==0) {
				tpcgSubscriptionClassList = ListUtils.convertToList(
						thirdPartyConfirmNotReqSubsClass, ",");
		}	
	
		return tpcgSubscriptionClassList.contains(subscriptionClass);
	}
	//RBT-9873 Added for getting configured charge class for CG flow
	public static boolean isChargeClassConfiguredForNotCGFlow(String chargeClass) {
		if(chargeClass==null) {
			return false;
		}
		String thirdPartyConfirmNotChargeClass = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.DOUBLE_CONFIRMATION, "CHARGE_CLASSES_FOR_NOT_CG_FLOW",
						null);
		if(thirdPartyConfirmNotChargeClass!=null && tpcgChargeClassList.size()==0) {
		    tpcgChargeClassList = ListUtils.convertToList(thirdPartyConfirmNotChargeClass, ",");
		}		
		
		return tpcgChargeClassList.contains(chargeClass);
	}
	
	/**
	 * Generates Unique protocol number from "SEQUENCE_DATE" table
	 * @return protocol number
	 */
	public static String generatePortocolNumber() {
		Connection conn = RBTDBManager.getInstance().getConnection();
		String sq_sequence = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON, "SQUENCE_NAME",
						"sq_sequence");
		return ConsentTableImpl.getNextProtocolNumber(conn, sq_sequence);
		
	}
	
	public static void sendInlineMessage(String subscriberId, String refId, String api, String action, String inlineFlow) {
		//For selection & activation:
		//action: activate, deactivate
		//api: Selection, Activation
		
		//For CG:
		//action: consent
		//api: Selection, Activation
		
		//For TP or CG To Selection:
		//action: null
		//api: null
		Object obj = null;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String type = null;
		HashMap<String, String> paramMap = new HashMap<String, String>();

		try {
			paramMap.put(WebServiceConstants.param_action, action);
			paramMap.put(WebServiceConstants.param_api, api);
			paramMap.put(WebServiceConstants.param_provisioning, inlineFlow);
			
			if(inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_CGR) || inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_CGC)) {
				type = inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_CGR) ? WebServiceConstants.TYPE_REALTIME : WebServiceConstants.TYPE_CALLBACK;
				obj = rbtDBManager.getConsentRecordForMsisdnNTransId(subscriberId, refId);
				paramMap.put(WebServiceConstants.param_mode, ((DoubleConfirmationRequestBean)obj).getMode());
			} else if(inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_SMR) || inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_SMC)) {
				type = inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_SMR) ? WebServiceConstants.TYPE_REALTIME : WebServiceConstants.TYPE_CALLBACK;
				obj = rbtDBManager.getSelectionBySubIdRefId(subscriberId, refId);
				paramMap.put(WebServiceConstants.param_mode, ((SubscriberStatus)obj).selectedBy());
			}  else if(inlineFlow.equalsIgnoreCase(WebServiceConstants.PROVISIONING_CONSENT_TO_SELECTION)) {
				obj = rbtDBManager.getConsentRecordForMsisdnNTransId(subscriberId, refId);
			} else
				obj = rbtDBManager.getSubscriber(subscriberId);
			
			paramMap.put(WebServiceConstants.param_provisioning_type, type);
			WebServiceContext parameters = new WebServiceContext(paramMap);
			messageSender.send(obj, parameters);
		} catch (Throwable t) {
			logger.error("Unable to send spring message..." + t);
			if(Utility.resetInlineFlag(obj))
				logger.info("Falling back to daemon approach is successful for : " + subscriberId + paramMap);
			else
				logger.info("Falling back to daemon approach is failed for: " + subscriberId + paramMap);
		}
	}
	
	public static String getInlineFlow(String classType, int place) {
		if(!RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false"))
			return null;
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE"))
			return null;
		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
		String provisioningFlow = StringUtils.trimToNull(chargeClass.getProvisioningFlow());
		if(provisioningFlow == null)
			return null;
		
		String realtime = null;
		String callback = null;
		
		switch(place) {
			case 1:
				realtime = WebServiceConstants.PROVISIONING_CGR;
				callback = WebServiceConstants.PROVISIONING_CGC;
				break;
			case 2:
				realtime = WebServiceConstants.PROVISIONING_SMR;
				callback = WebServiceConstants.PROVISIONING_SMC;
				break;
			case 3:
				realtime = WebServiceConstants.PROVISIONING_TP;
				break;
			default:
				return null;
		}
			
		StringTokenizer token = new StringTokenizer(provisioningFlow, ":");
		if(token.countTokens() == 3) {
			String flow = null;
			for(int i=0; i<place;i++) {
				flow = token.nextToken();
			}
			if(flow.equalsIgnoreCase(realtime) || flow.equalsIgnoreCase(callback)) {
				return flow;
			} else if(flow.equalsIgnoreCase("0")) {
				return flow;
			}
		} 
		return null;
	}
	
	public static boolean resetInlineFlag(Object obj) {
		if(obj == null)
			return false;
		
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		boolean result = false;
		
		try {
			if(obj instanceof GenericMessage<?>) {
				obj = ((GenericMessage<?>) obj).getPayload();
			}
			if(obj instanceof SubscriberStatus) {
				SubscriberStatus ss = (SubscriberStatus) obj;
				result = rbtDBManager.updateSubscriberSelectionInlineDaemonFlag(ss.subID(), ss.refID(), null);
			} else if(obj instanceof DoubleConfirmationRequestBean) {
				DoubleConfirmationRequestBean reqBean = (DoubleConfirmationRequestBean)obj;
				//sub id + trans id in the query is not using index, so using only ref id
				result = rbtDBManager.updateConsentInlineDaemonFlag(reqBean.getTransId(), null);
			} else {
				logger.warn("Resetting inline flag failed, object type mismatch: " + obj);
			}
		} catch(Throwable t) {
			logger.error("Exception while resetting inline flag: " + t + obj);
			result = false;
		}
		
		return result;
	}
	
	public static boolean isD2CModel() {
		boolean result = false;
		//have one rbt parameter for this
		try {
			IClipStatusMappingDAO statusMappingDAO = (IClipStatusMappingDAO) ConfigUtil.getBean(BeanConstant.CLIP_STATUS_MAPPING_DAO);
			if(statusMappingDAO != null)
				result = true;
		} catch (Throwable t) {
		}
		return result;
	}
}
