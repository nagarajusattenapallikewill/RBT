package com.onmobile.android.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.android.utils.StringConstants;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.sun.org.apache.xpath.internal.operations.Bool;



/**
 * Caches all the parameters of the type MOBILEAPP and provides API's to access the 
 * parameters
 * @author manish.shringarpure
 *
 */

public class PropertyConfigurator {


	public static Logger logger = Logger.getLogger(PropertyConfigurator.class);
	protected static Map<String, Parameter> paramMap = new HashMap<String, Parameter>();
	private static boolean isDownloadsModel = false;

	static ResourceBundle resourceBundle = null;

	/**
	 * Cache the params from the DB with type MOBILEAPP
	 */
	static{
		RBTClient client = RBTClient.getInstance();
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType(StringConstants.PARAM_TYPE);
		Parameter[] parameters = client.getParameters(request);
		for(Parameter parameter: parameters){
			paramMap.put(parameter.getName(),parameter);
		}
		logger.info("parameter======================: "+parameters);
		request = new ApplicationDetailsRequest();
		request.setType("COMMON");
		request.setName("ADD_TO_DOWNLOADS");
		Parameter downloadsParam = client.getParameter(request);
		logger.info("downloadsParam======================: "+downloadsParam);
		if (downloadsParam != null && downloadsParam.getValue() != null)
			isDownloadsModel = downloadsParam.getValue().equalsIgnoreCase("TRUE");


		try {
			resourceBundle = ResourceBundle.getBundle(StringConstants.MOBILEAPP_CONFIG_FILE);
			if (resourceBundle != null) {
				logger.info("Loading properties from " + StringConstants.MOBILEAPP_CONFIG_FILE + ".properties file.");
				Enumeration<String> keys = resourceBundle.getKeys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (paramMap.containsKey(key)) {
						logger.warn("Duplicate property in DB and in properties file. Preference given to the property in the latter. Key: " + key);
					}
					Parameter param = new Parameter(StringConstants.PARAM_TYPE, key, resourceBundle.getString(key));
					paramMap.put(key, param);
				}
			}
		} catch(MissingResourceException e) {
			logger.warn(StringConstants.MOBILEAPP_CONFIG_FILE+".properties file not found in classpath.", e);
		}
	}


	/**
	 * 
	 * @return int 
	 * PARAM_NAME = MOBILE_PROMOTIONAL_CATEGORY
	 */
	public static int getPromotionalCategoryId(){
		//-- define a CONSTANT if there is no value configured in the DB
		int promotionalCategoryId = StringConstants.defaultCategoryId;
		Parameter param = paramMap.get(StringConstants.PARAM_PROMOTIONAL_MOBILE_CATEGORY);
		if(param!=null){
			try {
				promotionalCategoryId = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				promotionalCategoryId = StringConstants.defaultCategoryId;
			}

		}
		return promotionalCategoryId;
	}

	public static int getClipRowCount(){
		//-- define a CONSTANT if there is no value configured in the DB
		int defaultClipRowCount = StringConstants.defaultRowCount;
		Parameter param = paramMap.get(StringConstants.PARAM_CLIP_ROW_COUNT);
		if(param!=null){
			try {
				defaultClipRowCount = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				defaultClipRowCount = StringConstants.defaultRowCount;
			}

		}
		return defaultClipRowCount;
	}


	public static String getProfileParentCategoryId(){
		String profileParent = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PROFILE_CATEGORY_ID);
		if(param!=null){
			profileParent = param.getValue();
		}
		return profileParent;
	}

	public static String getProfileCosId(){
		String profileCosId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PROFILE_COS_ID);
		if(param!=null){
			profileCosId = param.getValue();
		}
		return profileCosId;
	}

	public static String getNormalProfileCosId(){
		String normalProfileCosId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_NORMAL_PROFILE_COS_ID);
		if(param!=null){
			normalProfileCosId = param.getValue();
		}
		return normalProfileCosId;
	}

	public static String getDefaultChargeClass(String defValue){
		String defaultChargeClass = defValue;
		Parameter param = paramMap.get(StringConstants.PARAM_DEFAULT_CHARGE_CLASS);
		if(param!=null){
			defaultChargeClass = param.getValue();
		}
		return defaultChargeClass;
	}

	public static String getMusicPackCosId(){
		String musicPackCosId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MUSIC_PACK_COS_ID);
		if(param!=null){
			musicPackCosId = param.getValue();
		}
		return musicPackCosId;
	}

	public static String getMusicPackContentTypes(){
		String musicPackContentTypes = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MUSIC_PACK_CONTENT_TYPES);
		if(param!=null){
			musicPackContentTypes = param.getValue();
		}
		return musicPackContentTypes;
	}

	public static String getDefaultPriceAmount(){
		String defaultAmount = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DEFAULT_PRICE_AMOUNT);
		if(param!=null){
			defaultAmount = param.getValue();
		}
		return defaultAmount;
	}

	public static String getMainCategoryId(){
		return getMainCategoryId(null);
	}

	public static String getMainCategoryId(String language){
		String maincategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MAIN_CATEGORY_ID);
		Parameter paramwithLang = null;
		if(language != null && language.length() != 0 && !language.equalsIgnoreCase("NULL")) {
			paramwithLang = paramMap.get(StringConstants.PARAM_MAIN_CATEGORY_ID + "." + language);
			if(paramwithLang != null) {
				maincategoryId = paramwithLang.getValue();
			}
		}

		if(param!=null && maincategoryId == null){
			maincategoryId = param.getValue();
		}
		return maincategoryId;
	}

	public static int getDefaultParentCategoryId(String language){
		int parentCategoryId = -1;
		Parameter param = paramMap.get(StringConstants.PARAM_PARENT_CATEGORY_ID + "." + language);
		if (param != null){
			try {
				parentCategoryId = Integer.parseInt(param.getValue());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return parentCategoryId;
	}

	public static String getRecordedFilePath() {
		String vpRecordingFolder = StringConstants.defaultVPRecordingFolder;
		Parameter param = paramMap
				.get(StringConstants.PARAM_VP_RECORDING_FOLDER);
		logger.info("param:" + param);
		if (param != null) {
			vpRecordingFolder = param.getValue();
		}
		return vpRecordingFolder;
	}


	public static Map<String, String> getCircleIdLanguageMap(){
		Map<String, String> circleIdLanguageMap = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CIRCLEID_LANGUAGE_MAP);
		if (param != null) {
			circleIdLanguageMap = new HashMap<String, String>(); 
			String configStr = param.getValue();
			String[] mappings = configStr.split(",");
			for (String mapping : mappings) {
				String[] tokens = mapping.split(":");
				circleIdLanguageMap.put(tokens[0].trim(), tokens[1].trim());
			}
		}
		return circleIdLanguageMap;
	}

	public static int getDefaultParentCategoryId(){
		int parentCategoryId = 0;
		Parameter param = paramMap.get(StringConstants.PARAM_PARENT_CATEGORY_ID);
		if (param != null){
			try {
				parentCategoryId = Integer.parseInt(param.getValue());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return parentCategoryId;
	}

	public static String getHomeCategoryId(){
		return getHomeCategoryId(null);
	}

	public static String getHomeCategoryId(String language){
		String homecategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_HOME_CATEGORY_IDS);

		Parameter paramWithLang = null;
		if(language != null && language.length() != 0 && !language.equalsIgnoreCase("NULL")) {
			paramWithLang = paramMap.get(StringConstants.PARAM_HOME_CATEGORY_IDS + "." + language);
			if(paramWithLang != null) {
				homecategoryId = paramWithLang.getValue();
			}
		}

		if(param!=null && homecategoryId == null){
			homecategoryId = param.getValue();
		}
		return homecategoryId;
	}

	public static int getRowCountForBI(){
		int rowcountforBI = 100;
		Parameter param = paramMap.get(StringConstants.PARAM_CLIP_ROW_COUNT_FOR_BI);
		if(param != null){
			try {
				rowcountforBI = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				rowcountforBI = 100;
			}
		}
		return rowcountforBI;
	}

	public static int getRowCountForBlackberry(){
		int rowcountforBlackberry = 10;
		Parameter param = paramMap.get(StringConstants.PARAM_CLIP_ROW_COUNT_FOR_BLACKBERRY);
		if(param != null){
			try {
				rowcountforBlackberry = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				rowcountforBlackberry = 10;
			}
		}
		return rowcountforBlackberry;
	}

	public static String getMusicPackAmount(){
		String packAmount = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MUSIC_PACK_AMOUNT);
		if(param!=null){
			packAmount = param.getValue();
		}
		return packAmount;
	}

	public static String getCategoryImagePath(String catId){
		String catImgPath = null;
		if(isHidePath()) {
			return catId;
		}
		Parameter param = paramMap.get(StringConstants.PARAM_IMG_PATH+"_"+catId);
		if(param!=null){
			catImgPath = param.getValue();
		}
		return catImgPath;
	}

	public static Boolean isHidePath() {
		Boolean isHidePath = false;
		Parameter param = paramMap.get(StringConstants.PARAM_HIDE_PATH);
		if (param != null){
			isHidePath = Boolean.valueOf(param.getValue());
		}
		return isHidePath;
	}

	public static String getImagePath(){
		String imgPath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_IMAGE_PATH);
		if(param!=null){
			imgPath = param.getValue();
		}
		return imgPath;
	}

	public static String getImageWithResolutionsPath(){
		String imgPath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_IMAGE_WITH_RESOLUTIONS_PATH);
		if(param!=null){
			imgPath = param.getValue();
		}
		logger.info("param :==>" + param);
		return imgPath;
	}

	public static String getCategoryImageWithResolutionsPath(){
		String imgPath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CATEGORY_IMAGE_WITH_RESOLUTIONS_PATH);
		if(param!=null){
			imgPath = param.getValue();
		}
		logger.info("param :==>" + param);
		return imgPath;
	}

	public static String getPreviewPath(){
		String previewPath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PREVIEW_PATH);
		if(param!=null){
			previewPath = param.getValue();
		}
		logger.info("param :==>" + param);
		return previewPath;
	}

	public static String getNewReleaseCategoryId(){
		String newReleasecategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_NEW_RELEASE_CATEGORY_ID);
		if(param!=null){
			newReleasecategoryId = param.getValue();
		}
		logger.info("param :==>" + param);
		return newReleasecategoryId;
	}

	public static String getClipROwCount(){
		String maincategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CLIP_ROW_COUNT);
		if(param!=null){
			maincategoryId = param.getValue();
		}
		logger.info("param :==>" + param);
		return maincategoryId;
	}

	public static String getPromotionalClipIds(){
		//-- define a CONSTANT if there is no value configured in the DB
		String promotionalClipIds = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PROMOTIONAL_MOBILE_CLIPS);
		logger.info("param:"+param);
		if(param!=null){
			try {
				promotionalClipIds = param.getValue();
				logger.error("promotionalClip Ids "+  promotionalClipIds);
			} catch (NumberFormatException nfe) {
				logger.error("Number Format Exception in promotionalClip Ids");
				promotionalClipIds = null;
			}

		}
		return promotionalClipIds;
	}


	public static String getPromotionalClipIdsForCategory(String catId){
		//-- define a CONSTANT if there is no value configured in the DB
		String promotionalClipIds = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PROMOTIONAL_MOBILE_CLIPS_FOR_CATEGORY+catId);
		logger.info("param:"+param);
		if(param!=null){
			try {
				promotionalClipIds = param.getValue();
				logger.error("promotionalClip Ids for category"+  promotionalClipIds);
			} catch (NumberFormatException nfe) {
				logger.error("Number Format Exception in promotionalClip Ids");
				promotionalClipIds = null;
			}

		}
		return promotionalClipIds;
	}

	public static String getParameterValue(String param)
	{
		Parameter parameter = paramMap.get(param);
		logger.info("param:"+param);
		if (parameter != null) {
			return parameter.getValue();
		}
		return null;
	}

	public static List<String> getBlockedCategories() {
		List<String> catIdList = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_BLOCKED_CATEGORY_IDS);
		if(param != null && param.getValue() != null && param.getValue().length() > 0) {
			catIdList = Arrays.asList(param.getValue().split(","));
		}
		return catIdList;
	}


	// RBT-6497:-Handset Client- First song download via app for free
	public static String getFirstFreeChargeClass(){
		String freeChargeCalss = null;
		Parameter param = paramMap.get(StringConstants.PARAM_FREE_CHARGE_CLASS);
		if(param!=null){
			freeChargeCalss = param.getValue();
		}
		return freeChargeCalss;
	}

	public static List<String> getFirstFreeSelModeList(){
		List<String> freeChargeCalssModeList = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_FREE_SEL_MODE);
		String temp=null;
		if(param!=null){
			StringTokenizer st1 = new StringTokenizer(param.getValue(),",");
			while(st1.hasMoreElements()) {
				temp = st1.nextToken();
				freeChargeCalssModeList.add(temp);
			}
		}
		return freeChargeCalssModeList;
	}

	public static boolean isDownloadsModel() {
		return isDownloadsModel;
	}

	public static String getDefaultCircleId(){
		String circleId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DEFAULT_CIRCLE_ID);
		if(param!=null){
			circleId = param.getValue();
		}
		return circleId;
	}

	public static String getDayFormat(){
		String circleId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DAY_FORMAT);
		if(param!=null){
			circleId = param.getValue();
		}
		return circleId;
	}

	public static String getMonthFormat(){
		String circleId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MONTH_FORMAT);
		if(param!=null){
			circleId = param.getValue();
		}
		return circleId;
	}

	public static String getTopSongMessage(){
		String msg = null;
		Parameter param = paramMap.get(StringConstants.PARAM_TOPSONG_MSG);
		if(param!=null){
			msg = param.getValue();
		}
		return msg;
	}
	public static String getGiftAmount(){
		String amount = null;
		Parameter param = paramMap.get(StringConstants.PARAM_GIFT_AMOUNT);
		if(param!=null){
			amount = param.getValue();
		}
		return amount;
	}
	public static String getChargeClassForProfile(){
		String chargeCls = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CHARGE_CLASS_PROFILE);
		logger.info("param :==>"+param);
		if(param!=null){
			chargeCls = param.getValue();
		}
		return chargeCls;
	}

	public static String getDateFormat(){
		String dateformat = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DATE_FORMAT);
		logger.info("param :==>"+param);
		if(param!=null){
			dateformat = param.getValue();
		}
		return dateformat;
	}	

	public static String getNameTuneText(){
		String nameTune= null;
		Parameter param = paramMap.get(StringConstants.PARAM_NAME_TUNE_TXT);
		logger.info("param :==>"+param);
		if(param!=null){
			nameTune = param.getValue();
		}
		return nameTune;
	}

	public static String isImageUrl(){
		String imageUrl = null;
		Parameter param = paramMap.get(StringConstants.IS_IMAGE_URL);
		logger.info("param :==>"+param);
		if(param!=null){
			imageUrl = param.getValue();
		}
		return imageUrl;
	}	

	//RBT-13982
	public static String isRelativePath(){
		String relativePath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_IS_RELATIVE_PATH);
		logger.info("param :==>"+param);
		if(param!=null){
			relativePath = param.getValue();
		}
		return relativePath;
	}	

	public static String allowedFileExtension(){
		String allowedExtensions = null;
		Parameter param = paramMap.get(StringConstants.FILE_EXTENSION_ALLOWED);
		logger.info("param :==>"+param);
		if(param!=null){
			allowedExtensions = param.getValue();
		}
		return allowedExtensions;
	}	

	public static String getSharedPath(){
		String nameTune= null;
		Parameter param = paramMap.get(StringConstants.PARAM_TUNE_FILE_PATH);
		logger.info("param :==>"+param);
		if(param!=null){
			nameTune = param.getValue();
		}
		return nameTune;
	}	

	public static String getCatIdForChargeClass(){
		String catId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CHARGECLASS_CAT_ID);
		logger.info("param :==>"+param);
		if(param!=null){
			catId = param.getValue();
		}
		return catId;
	}	

	public static List<String> getValidAreaCodeList(){
		List<String> validAreaCodeList = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_VALID_AREA_CODE);
		String temp=null;
		if(param!=null){
			StringTokenizer st1 = new StringTokenizer(param.getValue(),",");
			while(st1.hasMoreElements()) {
				temp = st1.nextToken();
				validAreaCodeList.add(temp);
			}
		}
		return validAreaCodeList;
	}

	public static List<String> getValidMobileDigitList(){
		List<String> validMobileDigitList = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_VALID_MOBILE_DIGIT);
		String temp=null;
		if(param!=null){
			StringTokenizer st1 = new StringTokenizer(param.getValue(),",");
			while(st1.hasMoreElements()) {
				temp = st1.nextToken();
				validMobileDigitList.add(temp);
			}
		}
		return validMobileDigitList;
	}

	public static Boolean isEncryptionEnabled() {
		Boolean isEncryptionEnabled = false;
		Parameter param = paramMap.get(StringConstants.PARAM_ENABLE_ENCRYPTION);
		logger.info("param :==>" + param);
		if(param != null) {
			isEncryptionEnabled = Boolean.valueOf(param.getValue());
		}
		return isEncryptionEnabled;
	}

	public static String getRequestSubscriberIdEncryptionKey() {
		String requestSubscriberIdEncryptionKey = null;
		Parameter param = paramMap.get(StringConstants.PARAM_REQUEST_SUBSCRIBERID_ENCRYPTION_KEY);
		logger.info("param :==>" + param);
		if(param != null) {
			requestSubscriberIdEncryptionKey = param.getValue();
		}
		return requestSubscriberIdEncryptionKey;
	}

	public static String getRequestUniqueIdEncryptionKey() {
		String requestUniqueIdEncryptionKey = null;
		Parameter param = paramMap.get(StringConstants.PARAM_REQUEST_UNIQUEID_ENCRYPTION_KEY);
		logger.info("param :==>" + param);
		if(param != null) {
			requestUniqueIdEncryptionKey = param.getValue();
		}
		return requestUniqueIdEncryptionKey;
	}

	public static String getResponseSubscriberIdEncryptionKey() {
		String responseSubscriberIdEncryptionKey = null;
		Parameter param = paramMap.get(StringConstants.PARAM_RESPONSE_SUBSCRIBERID_ENCRYPTION_KEY);
		logger.info("param :==>" + param);
		if(param != null) {
			responseSubscriberIdEncryptionKey = param.getValue();
		}
		return responseSubscriberIdEncryptionKey;
	}


	public static List<String> getMsisdnHeadersName() {		
		List<String> headerNamesList = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_MSISDN_HEADER_NAMES);
		logger.info("param :==>" + param);
		if(param != null && param.getValue() != null && param.getValue().length() > 0) {
			headerNamesList = Arrays.asList(param.getValue().split(","));
		}
		return headerNamesList;
	}

	public static String getConsentURL() {
		String consentCgUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_CONSENT_CG_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentCgUrl = param.getValue();
		}
		return consentCgUrl;
	}
	
	//VD-106918 , CG url based on modes
	public static String getConsentURL(String mode) {
		String consentCgUrl = null;
		Parameter param = null;
		if (mode != null) {
			param = paramMap.get(StringConstants.PARAM_MOBILEAPP_CONSENT_CG_URL
					+ "." + mode.toLowerCase());
		}
		logger.info("param :==>" + param);
		if (param != null) {
			consentCgUrl = param.getValue();
		} else {
			consentCgUrl = getConsentURL();
		}
		return consentCgUrl;
	}
	
	public static String getComvivaConsentURL() {
		String consentCgUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_CONSENT_CONSENT_CG_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentCgUrl = param.getValue();
		}
		return consentCgUrl;
	}

	public static String getRURL() {
		String consentCgUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_R_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentCgUrl = param.getValue();
		}
		return consentCgUrl;
	}
	
	
	public static String getConsentURLTimeStampFormat() {
		String consentCgUrlTimestampFormat = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_CONSENT_CG_URL_TIMESTAMP_FORMAT);
		logger.info("param :==>" + param);
		if(param != null) {
			consentCgUrlTimestampFormat = param.getValue();
		}
		return consentCgUrlTimestampFormat;
	}

	public static String getConsentURLTimeStampTimeZone() {
		String consentCgUrlTimestampFormat = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_CONSENT_CG_URL_TIMESTAMP_TIME_ZONE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentCgUrlTimestampFormat = param.getValue();
		}
		return consentCgUrlTimestampFormat;
	}
	/**
	 * RBT-10475
	 * @author rony.gregory
	 * @return OsType:mode map. Returns null if the parameter is not present.
	 */
	public static Map<String,String> getOsTypeModeMap() {
		Map<String,String> osTypeModeMap = null;
		Parameter param = paramMap.get(StringConstants.PARAM_OSTYPE_MODE_MAP);
		logger.info("param :==>" + param);
		if(param != null) {
			osTypeModeMap = MapUtils.convertToMap(param.getValue(), ",", ":", null);
		}
		return osTypeModeMap;
	}

	public static String getLocaleConfig() {
		String localeConfig = null;
		Parameter param = paramMap.get(StringConstants.PARAM_LOCALE_CONFIG);
		logger.info("param :==>" + param);
		if(param != null) {
			localeConfig = param.getValue();
		}
		return localeConfig;
	}

	public static String getCurrentVersion() {
		String currentVersion = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CURRENT_VERSION);
		logger.info("param :==>" + param);
		if(param != null) {
			currentVersion = param.getValue();
		}
		return currentVersion;
	}

	public static String getMandatoryToUpgrade() {
		String mandatoryToUpgrade = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MANDATORY_TO_UPGRADE);
		logger.info("param :==>" + param);
		if(param != null) {
			mandatoryToUpgrade = param.getValue();
		}
		return mandatoryToUpgrade;
	}

	public static String getConsentReturnYesUrl() {
		String consentReturnYesUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_YES_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnYesUrl = param.getValue();
		}
		return consentReturnYesUrl;
	}

	public static String getConsentReturnNoUrl() {
		String consentReturnNoUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_NO_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnNoUrl = param.getValue();
		}
		return consentReturnNoUrl;
	}

	public static String getPackageOfferSupported() {
		String consentReturnNoUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_PACKAGE_OFFER_SUPPORTED);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnNoUrl = param.getValue();
		}
		return consentReturnNoUrl;
	}

	public static String getOfferDescription(String offerType, String srvKey , String browsingLanguage , String appName) {
		String offerDescription = null;
		Parameter param = null;
		String paramName = null;
		if(browsingLanguage != null){
			browsingLanguage = browsingLanguage.toLowerCase();
			paramName = StringConstants.PARAM_OFFER_DESCRIPTION + ".offerType_" + offerType + ".srvkey_" + srvKey + ".browsingLanguage_" + browsingLanguage ;
			param = paramMap.get(paramName);
		}
		
		if( param == null && appName != null){
			appName = appName.toLowerCase();
			paramName = StringConstants.PARAM_OFFER_DESCRIPTION + ".offerType_" + offerType + ".srvkey_" + srvKey + ".appName_" + appName ;	
			param = paramMap.get(paramName);
		}
		
		if(param == null){
			paramName = StringConstants.PARAM_OFFER_DESCRIPTION + ".offerType_" + offerType + ".srvkey_" + srvKey  ;	
			param = paramMap.get(paramName);
		}
		
		logger.info("param :==>" + param);
		
		if(param != null) {
			offerDescription = param.getValue();
		}
		return offerDescription;
	}



	public static boolean isMergeDownloadAndSelection() {
		Parameter param = paramMap.get(StringConstants.PARAM_TO_BE_MERGE_DOWNLOAD_SELECTION);
		logger.info("param :==>" + param);
		if(param != null) {
			return param.getValue().equalsIgnoreCase("TRUE");
		}
		return false;
	}


	public static String getSubscriptionAmount() {
		String subscriptionAmount = null;
		Parameter param = paramMap.get(StringConstants.PARAM_SUBSCRIPTION_AMOUNT);
		logger.info("param :==>" + param);
		if(param != null) {
			subscriptionAmount = param.getValue();
		}
		return subscriptionAmount;
	}

	public static String getSubscriptionPeriod() {
		String subscriptionPeriod = null;
		Parameter param = paramMap.get(StringConstants.PARAM_SUBSCRIPTION_PERIOD);
		logger.info("param :==>" + param);
		if(param != null) {
			subscriptionPeriod = param.getValue();
		}
		return subscriptionPeriod;
	}

	public static String getDefaultSelectionPeriod() {
		String selectionPeriod = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DEFAULT_SELECTION_PERIOD);
		logger.info("param :==>" + param);
		if(param != null) {
			selectionPeriod = param.getValue();
		}
		return selectionPeriod;
	}

	public static String getOperatorName() {
		String operatorName = null;
		Parameter param = paramMap.get(StringConstants.PARAM_OPERATOR_NAME);
		logger.info("param :==>" + param);
		if(param != null) {
			operatorName = param.getValue();
		}
		return operatorName;
	}

	public static String getConsentRefIdPrefixRbtAct() {
		String consentRefIfPrefixRbtAct = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_REFID_PREFIX_RBT_ACT);
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIfPrefixRbtAct = param.getValue();
		}
		return consentRefIfPrefixRbtAct;
	}

	public static String getConsentRefIdPrefixRbtSel() {
		String consentRefIfPrefixRbtSel = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_REFID_PREFIX_RBT_SEL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIfPrefixRbtSel = param.getValue();
		}
		return consentRefIfPrefixRbtSel;
	}

	public static String getConsentRefIdPrefixCPId() {
		String consentRefIdPrefixCPId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_REFID_PREFIX_CP_ID);
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIdPrefixCPId = param.getValue();
		}
		return consentRefIdPrefixCPId;
	}

	public static String getCgCpId(String atlantisCpId) {
		String cgCpId = null;
		String paramName = "mobileapp.cpid." + atlantisCpId + ".id";
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			cgCpId = param.getValue();
		}
		return cgCpId;
	}

	public static String getCgCpName(String atlantisCpId) {
		String cgCpName = null;
		String paramName = "mobileapp.cpid." + atlantisCpId + ".name";
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			cgCpName = param.getValue();
		}
		return cgCpName;
	}

	public static String getConsentInfoCombo() {
		String consentInfoCombo = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_INFO_COMBO);
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoCombo = param.getValue();
		}
		return consentInfoCombo;
	}

	public static String getConsentInfoBase() {
		String consentInfoBase = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_INFO_BASE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoBase = param.getValue();
		}
		return consentInfoBase;
	}

	public static String getConsentInfoSel() {
		String consentInfoSel = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_INFO_SEL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoSel = param.getValue();
		}
		return consentInfoSel;
	}

	public static String getConsentPrecharge() {
		String consentPrecharge = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_CONSTANT_PRECHARGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentPrecharge = param.getValue();
		}
		return consentPrecharge;
	}

	public static String getConsentOriginator() {
		String consentOriginator = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_CONSTANT_ORIGINATOR);
		logger.info("param :==>" + param);
		if(param != null) {
			consentOriginator = param.getValue();
		}
		return consentOriginator;
	}

	public static String getConsentImagePrefixUrl() {
		String consentImagePrefixUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_IMAGE_PREFIX_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			consentImagePrefixUrl = param.getValue();
		}
		return consentImagePrefixUrl;
	}

	public static String getConsentDefaultImagePath() {
		String consentDefaultImagePath = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_DEAFULT_IMAGE_PATH);
		logger.info("param :==>" + param);
		if(param != null) {
			consentDefaultImagePath = param.getValue();
		}
		return consentDefaultImagePath;
	}

	public static String getConsentUserId() {
		String consentUserId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_USERID);
		logger.info("param :==>" + param);
		if(param != null) {
			consentUserId = param.getValue();
		}
		return consentUserId;
	}

	public static String getConsentPassword() {
		String consentPassword = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_PASSWORD);
		logger.info("param :==>" + param);
		if(param != null) {
			consentPassword = param.getValue();
		}
		return consentPassword;
	}

	public static String getConsentReturnMessage() {
		String consentReturnMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_MESSAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnMessage = param.getValue();
		}
		return consentReturnMessage;
	}

	public static String getConsentReturnSuccessMessage() {
		String consentReturnSuccessMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_SUCCESS_MESSAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnSuccessMessage = param.getValue();
		}
		return consentReturnSuccessMessage;
	}

	public static String getConsentReturnLowBalanceMessage() {
		String consentReturnLowBalanceMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_LOW_BALANCE_MESSAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnLowBalanceMessage = param.getValue();
		}
		return consentReturnLowBalanceMessage;
	}

	public static String getConsentReturnTechnicalDifficultyMessage() {
		String consentReturnTechnicalDifficultyMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_TECHNICAL_DIFFICULTY_MESSAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnTechnicalDifficultyMessage = param.getValue();
		}
		return consentReturnTechnicalDifficultyMessage;
	}

	public static String getConsentReturnFailureMessage() {
		String consentReturnFailureMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnFailureMessage = param.getValue();
		}
		return consentReturnFailureMessage;
	}

	public static String getConsentReturnFailureMessage(String respCode) {
		String consentReturnFailureMessage = null;
		String paramName = StringConstants.PARAM_CONSENT_RETURN_FAILURE_MESSAGE + "." + respCode;
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			consentReturnFailureMessage = param.getValue();
		}
		return consentReturnFailureMessage;
	}

	public static String getHTConsentUrl() {
		String htConsentUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_HT_CONSENT_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			htConsentUrl = param.getValue();
		}
		return htConsentUrl;
	}

	public static String getTopPlaylistsCategoryId() {
		String topPlaylistsCategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_TOP_PLAYLISTS_CATEGORY_ID);
		logger.info("param :==>" + param);
		if(param != null) {
			topPlaylistsCategoryId = param.getValue();
		}
		return topPlaylistsCategoryId;
	}

	public static String getOtherPlaylistsCategoryId() {
		String otherPlaylistsCategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_OTHER_PLAYLISTS_CATEGORY_ID);
		logger.info("param :==>" + param);
		if(param != null) {
			otherPlaylistsCategoryId = param.getValue();
		}
		return otherPlaylistsCategoryId;
	}

	public static String getOstypesForEncryption() {
		String osTypesForEncryption = null;
		Parameter param = paramMap.get(StringConstants.PARAM_OSTYPES_FOR_ENCRYPTION);
		logger.info("param :==>" + param);
		if(param != null) {
			osTypesForEncryption = param.getValue();
		}
		return osTypesForEncryption;
	}

	public static String getRequestEncryptionKey() {
		String requestEncryptionKey = null;
		Parameter param = paramMap.get(StringConstants.PARAM_REQUEST_ENCRYPTION_KEY);
		logger.info("param :==>" + param);
		if(param != null) {
			requestEncryptionKey = param.getValue();
		}
		return requestEncryptionKey;
	}

	public static Boolean isQueryStringEncryptionEnabled() {
		Boolean isQueryStringEncryptionEnabled = false;
		Parameter param = paramMap.get(StringConstants.PARAM_IS_QUERY_STRING_ENCRYPTION_ENABLED);
		logger.info("param :==>" + param);
		if(param != null) {
			isQueryStringEncryptionEnabled = Boolean.valueOf(param.getValue());
		}
		return isQueryStringEncryptionEnabled;
	}

	public static String getDefaultCgCpId() {
		String defaultCgCpId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CPID_DEFAULT_ID);
		logger.info("param :==>" + param);
		if(param != null) {
			defaultCgCpId = param.getValue();
		}
		return defaultCgCpId;
	}

	public static String getDefaultCgCpName() {
		String defaultCgCpName = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CPID_DEFAULT_NAME);
		logger.info("param :==>" + param);
		if(param != null) {
			defaultCgCpName = param.getValue();
		}
		return defaultCgCpName;
	}

	public static String getThirdPartyCircleIdMapping() {
		String thirdPartyCircleIdMapping = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_THIRD_PARTY_CIRCLE_ID_MAPPING);
		logger.info("param :==>" + param);
		if(param != null) {
			thirdPartyCircleIdMapping = param.getValue();
		}
		return thirdPartyCircleIdMapping;
	}

	public static String getConsentMsisdnPrefix() {
		String consentMsisdnPrefix = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_MSISDN_PREFIX);
		logger.info("param :==>" + param);
		if(param != null) {
			consentMsisdnPrefix = param.getValue();
		}
		return consentMsisdnPrefix;
	}

	public static String getResponseForGetSubscriberIdErrorCase(String rbtResponse) {
		String responseForGetSubscriberIdErrorCase = null;
		String paramName = StringConstants.PARAM_RESPONSE_FOR_GET_SUBSCRIBER_ID + "." + rbtResponse;
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			responseForGetSubscriberIdErrorCase = param.getValue();
		}
		return responseForGetSubscriberIdErrorCase;	
	}

	public static String getResponseForGetRegistrationErrorCase(String rbtResponse) {
		String responseForGetRegistrationErrorCase = null;
		String paramName = StringConstants.PARAM_RESPONSE_FOR_REGISTRATION + "." + rbtResponse;
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			responseForGetRegistrationErrorCase = param.getValue();
		}
		return responseForGetRegistrationErrorCase;	
	}

	public static String getSelectionPeriodDescription(String period) {
		String selecitionPeriodMessage = null;
		String paramName = StringConstants.PARAM_SELECTION_PERIOD_DESCRIPTION + "." + period;
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			selecitionPeriodMessage = param.getValue();
		}
		return selecitionPeriodMessage;	
	}

	public static String getSuggestionSearchUrl() {
		String suggestionSearchUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_SUGGESTION_SEARCH_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			suggestionSearchUrl = param.getValue();
		}
		return suggestionSearchUrl;
	}
	
	public static int getCategoryRowCount() {
		int categoryRowCount = StringConstants.defaultRowCount;
		Parameter param = paramMap.get(StringConstants.PARAM_CATEGORY_ROW_COUNT);
		if(param != null){
			try {
				categoryRowCount = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				categoryRowCount = StringConstants.defaultRowCount;
			}
		}
		return categoryRowCount;
	}

	public static String getFreemiumCategoryId() {
		String freemiumCategoryId = null;
		Parameter param = paramMap.get(StringConstants.PARAM_FREEMIUM_CATEGORY_ID);
		logger.info("param :==>" + param);
		if(param!=null){
			freemiumCategoryId = param.getValue();
		}
		return freemiumCategoryId;
	}

	public static String getFreemiumCategoryIdForClips() {
		String freemiumCategoryIdForClips = null;
		Parameter param = paramMap.get(StringConstants.PARAM_FREEMIUM_CATEGORY_ID_FOR_CLIPS);
		logger.info("param :==>" + param);
		if (param!=null) {
			freemiumCategoryIdForClips = param.getValue();
		}
		return freemiumCategoryIdForClips;
	}

	public static boolean isConsentFlowMakeEntryInDB() {
		boolean consentFlowMakeEntryInDB = false;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_FLOW_MAKE_ENTRY_IN_DB);
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			consentFlowMakeEntryInDB = true;
		}
		logger.debug("consentFlowMakeEntryInDB: " + consentFlowMakeEntryInDB);
		return consentFlowMakeEntryInDB;
	}

	public static Boolean getIsSolrNameTuneSearch() {
		boolean isSolrNameTuneSearch = false;
		Parameter param = paramMap.get(StringConstants.PARAM_IS_SOLR_NAMETUNE_SEARCH);
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			isSolrNameTuneSearch = true;
		}
		logger.debug("isSolrNameTuneSearch: " + isSolrNameTuneSearch);
		return isSolrNameTuneSearch;
	}

	public static String getSubscriptionAmount(String circleId) {
		String subscriptionAmount = null;
		String paramName = StringConstants.PARAM_SUBSCRIPTION_AMOUNT + "." + circleId;
		logger.debug("paramName: " + paramName); 
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			subscriptionAmount = param.getValue();
		}
		return subscriptionAmount;
	}

	public static String getSubscriptionPeriod(String circleId) {
		String subscriptionPeriod = null;
		String paramName = StringConstants.PARAM_SUBSCRIPTION_PERIOD + "." + circleId;
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			subscriptionPeriod = param.getValue();
		}
		return subscriptionPeriod;
	}
	
	public static boolean isConsentFlowIsGenerateRefId() {
		boolean ConsentFlowIsGenerateRefId = false;
		Parameter param = paramMap.get(StringConstants.PARAM_CONSENT_FLOW_IS_GENERATE_REFID);
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			ConsentFlowIsGenerateRefId = true;
		}
		logger.debug("ConsentFlowIsGenerateRefId: " + ConsentFlowIsGenerateRefId);
		return ConsentFlowIsGenerateRefId;	
	}
	
	public static Map<String,String> getCategoryTypeMapping() {
		Map<String,String> categoryTypeMap = null;
		Parameter param = paramMap.get(StringConstants.PARAM_CATEGORY_TYPE_MAPPING);
		logger.info("param :==>" + param);
		if(param != null) {
			categoryTypeMap = MapUtils.convertToMap(param.getValue(), ";", "=", ",");
		}
		return categoryTypeMap;
	}
	
	public static int getArtistRowCount() {
		int artistRowCount = StringConstants.defaultRowCount;
		Parameter param = paramMap.get(StringConstants.PARAM_ARTIST_ROW_COUNT);
		if(param != null){
			try {
				artistRowCount = Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				artistRowCount = StringConstants.defaultRowCount;
			}
		}
		return artistRowCount;
	}
	

	public static boolean isCircleIdRequiredInGetSubscriberIdResp() {
		boolean isCircleIdRequiredInGetSubscriberIdAPIResp = false;
		Parameter param = paramMap.get(StringConstants.IS_CIRCLE_ID_REQUIRED_IN_GET_SUBSCRIBER_ID_RESP);
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			isCircleIdRequiredInGetSubscriberIdAPIResp = true;
		}
		logger.debug("isCircleIdRequiredInGetSubscriberIdAPIResp: " + isCircleIdRequiredInGetSubscriberIdAPIResp);
		return isCircleIdRequiredInGetSubscriberIdAPIResp;			
	}
	
	public static boolean isCircleIdRequiredInLoginUserResp() {
		boolean isCircleIdRequiredInLoginUserResp = false;
		Parameter param = paramMap.get(StringConstants.IS_CIRCLE_ID_REQUIRED_IN_LOGIN_USER_RESP);
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			isCircleIdRequiredInLoginUserResp = true;
		}
		logger.debug("isCircleIdRequiredInGetSubscriberIdAPIResp: " + isCircleIdRequiredInLoginUserResp);
		return isCircleIdRequiredInLoginUserResp;			
	}
	
	public static List<String> getClipTypes(){
		List<String> configuredTypes  =  new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.REALTIME_SONG_NOTIFICATION_CONTENT_TYPES);
		if(param == null){
			configuredTypes = Arrays.asList(("AZAAN,COPTIC,DUA,CRICKET,FOOTBALL,ADRBT,UGC").toLowerCase().split(","));
		}else{
			configuredTypes = Arrays.asList((param.getValue()+"").toLowerCase().split(","));
		}
		return configuredTypes;
	}
	
	
	//RBT-14540
	public static String getDatePattern(){
		String dateFormat = null;
		Parameter param = paramMap.get(StringConstants.PARAM_DELTA_CONTENT_HEADER_DATE_FORMAT);
		if(param!=null){
			dateFormat = param.getValue();
		}
		logger.info("param :==>" + param);
		return dateFormat;
	}
	
	public static List<String> getSupportedParamTypes(){
		List<String> supportedParamTypes = new ArrayList<String>();
		Parameter param = paramMap.get(StringConstants.PARAM_SUPPORTED_PARAM_TYPES);
		if(param != null){
			supportedParamTypes = Arrays.asList((param.getValue()+"").split(","));
		}
		logger.info("supportedParamTypes: " + supportedParamTypes);
		return supportedParamTypes;
	}
	
	public static String getNameTuneUrl(){
		String nameTuneUrl = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_NAME_TUNE_CREATION_URL);
		logger.info("param :==>" + param);
		if(param != null) {
			nameTuneUrl = param.getValue();
		}
		return nameTuneUrl;
	}

	public static String getLanguage() {
		// TODO Auto-generated method stub
		String laguage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_LANGUAGE);
		logger.info("param :==>" + param);
		if(param != null) {
			laguage = param.getValue();
		}
		return laguage;
	}
	
	public static Boolean isNameTuneApiRequired() {
		// TODO Auto-generated method stub
		Boolean apiResponseRequired= false;
		Parameter param = paramMap.get(StringConstants.PARAM_MOBILEAPP_NAME_TUNE_ONLINE_API_REQUIRED);
		logger.info("param :==>" + param);
		if(param != null) {
			apiResponseRequired = Boolean.parseBoolean(param.getValue().toLowerCase());
		}
		logger.info("param apiResponseRequired:==>" + param);
		return apiResponseRequired;
	}
	
	public static String getSelectionsMode() {
		String mode = "CCC";
		Parameter param = paramMap
				.get(StringConstants.PARAM_MODE_FOR_GET_SELECTIONS);
		logger.info("param :==>" + param);
		if (param != null) {
			mode = param.getValue();
		}
		return mode;
	}
	
	//RBT-16263	Unable to remove selection for local/site RBT user
	public static boolean getRDCParam(){
		Parameter param = paramMap
				.get(StringConstants.PARAM_MOBILEAPP_RDC);
		logger.info("param :==>" + param);
		if (param != null && param.getValue().equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	public static String getErrorCodeBasedMessage(String errorCode) {
		String cgCancelledMessage = null;
		Parameter param = paramMap.get(StringConstants.PARAM_ERROR_CODE_MESSAGE+"."+errorCode);
		logger.info("param :==>" + param);
		if(param != null) {
			cgCancelledMessage = param.getValue();
		}
		return cgCancelledMessage;
	}
	
	public static String getMappedLanguage(String language) {
		String languageMapping = null;
		Parameter param = paramMap.get(StringConstants.PARAM_UBONA_LANGUAGE_MAPPING);
		logger.info("param :==>" + param);
		if(param != null) {
			languageMapping = param.getValue();
		}
		if(languageMapping != null){
			HashMap<String, String> langagueMap = (HashMap<String, String>) MapUtils.convertIntoMap(languageMapping, ",", "=", null);
			if(langagueMap != null && langagueMap.containsKey(language)){
				return langagueMap.get(language);
			}
		}
		return language;
	}
	
	public static String getMappedCircleId(String circleId) {
		String circleIdMapping = null;
		Parameter param = paramMap.get(StringConstants.PARAM_UBONA_CIRCLEID_MAPPING);
		logger.info("param :==>" + param);
		if(param != null) {
			circleIdMapping = param.getValue();
		}
		if(circleIdMapping != null){
			HashMap<String, String> langagueMap = (HashMap<String, String>) MapUtils.convertIntoMap(circleIdMapping, ",", "=", null);
			if(langagueMap != null && langagueMap.containsKey(circleId)){
				return langagueMap.get(circleId);
			}
		}
		return circleId;
	}
	
	public static String getUbonaFeedbackUrl() {
		String url = null;
		Parameter param = paramMap
				.get(StringConstants.PARAM_UBONA_FEEDBACK_URL);
		logger.info("param :==>" + param);
		if (param != null) {
			url = param.getValue();
		}
		return url;
	}
}