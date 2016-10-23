/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: rony.gregory $
 * $Id: BasicRBTProcessor.java,v 1.769 2015/05/26 11:26:47 rony.gregory Exp $
 * $Revision: 1.769 $
 * $Date: 2015/05/26 11:26:47 $
 */
package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.PromoMaster;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Status;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadSubscriber;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.Scratchcard;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.UpgradeObject;
import com.onmobile.apps.ringbacktones.content.VfRBTUpgradeConsent;
import com.onmobile.apps.ringbacktones.content.ViralBlackListTable;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.PendingConfirmationsReminderTableImpl;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadSubscriberDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTBulkUploadTaskDAO;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.reminder.ReminderTool;
import com.onmobile.apps.ringbacktones.freemium.FreemiumMemcacheClient;
import com.onmobile.apps.ringbacktones.genericcache.BulkPromoSMSCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SmsTextCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.builder.impl.UGCAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.SubscriberSelectionImpl;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbt2.webservice.service.SelectionService;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientResponse;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.Encryption128BitsAES;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.v2.converter.UDPDOToResponseBeanConverter;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IUDPService;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.DuplicateRequestHandler;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.PPLContentRejectionLogger;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.SocialRBTEventLogger;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.filters.RbtFilterParser;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.RBTHibernateDBImplementationWrapper;
import com.onmobile.apps.ringbacktones.wrappers.SRBTDaoWrapper;
import com.onmobile.common.exception.OnMobileException;
import com.onmobile.reporting.framework.capture.api.Configuration;

/**
 * @author vinayasimha.patil
 * 
 */
public class BasicRBTProcessor implements RBTProcessor, WebServiceConstants {
	private static Logger logger = Logger.getLogger(BasicRBTProcessor.class);

	protected RBTDBManager rbtDBManager = null;
	protected RBTCacheManager rbtCacheManager = null;
	protected ParametersCacheManager parametersCacheManager = null;
	protected SmsTextCacheManager smsTextCacheManager = null;
	
	protected static final Object syncObj = new Object();

	private static final String VP = "VP";
	// private static HashMap<String, HashSet<String>> directCopyKeysMap = null;
	// private static HashMap<String, HashSet<String>> optinCopyKeysMap = null;
	public static PPLContentRejectionLogger pplContentRejectionLogger = null;

	private static Map<String, List> copyContestIDsTimeValidityMap = new HashMap<String, List>();
	
	private Map<String, String> offerTypeSubClassMap = new HashMap<String, String>();
	
	private Map<String,String> subClassPreRBTWavFileMap = new HashMap<String,String>();
	
	protected Map<String,String> overrideSubscriptionClassMap = new HashMap<String,String>();
	
	protected Map<String,String> overrideChargeClassMap = new HashMap<String,String>();
	
	private int offerTypeSubClassMapSize = 0;
	
	private static final String defaultOtpSmsText = "Your OTP passcode is <otppasscode>";
	protected static final String preConsentBaseSelSuccess = "preConsentBaseSelSuccess"; 
	
	private static String otpLowerRange = "1000";
	private static String otpHigherRange = "9999";
	protected String mnpUrl = null;
	protected String otpSenderId = null;
	
	protected static List<String> affiliatedContentModeList = null;
	protected static boolean isMpByPassedForAffiliate = false;
	
	protected static Map<String, String> packCosIdCosIdMap = new HashMap<String, String>();
	
	protected static HashMap<String,String> m_modeSubClassMap = null;
	protected static List<String> allowedFreemiumBlackListMode = null;
	protected static int noOfBlackListHrs = -1;
	protected static String freemiumGroup2CosId = null; 
	protected static String shuffleAlwaysEnabledDisabledFlag = null;
	private static MemCachedClient  mc = null;
	
	/**
	 * 
	 */
	public BasicRBTProcessor() {
		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		smsTextCacheManager = CacheManagerUtil.getSmsTextCacheManager();
		mc = MemcacheClientForCurrentPlayingSong.getInstance().getMemcache();
		
		String path = getParamAsString(iRBTConstant.WEBSERVICE,
				"PPL_CONTENT_REJECTION_LOG_PATH", null);
		if (path != null) {
			File file = new File(path);
			if (!file.exists())
				file.mkdirs();
			try {
				pplContentRejectionLogger = new PPLContentRejectionLogger(
						new Configuration(path));
			} catch (IOException e) {
				logger.error("Error while creating PPL Content Rejection logs",
						e);
			}
		}

		String offerTypeSubClassMapStr = parametersCacheManager.getParameterValue(iRBTConstant.COMMON,
				"OFFER_TYPE_SUB_CLASS_MAP", "");
		offerTypeSubClassMap = MapUtils.convertToMap(offerTypeSubClassMapStr, ";", "=", ",");
		offerTypeSubClassMapSize = offerTypeSubClassMap.size();
		
		otpLowerRange = parametersCacheManager.getParameterValue(iRBTConstant.WEBSERVICE,
				"OTP_LOWER_RANGE", otpLowerRange);
		otpHigherRange = parametersCacheManager.getParameterValue(iRBTConstant.WEBSERVICE,
				"OTP_HIGHER_RANGE", otpHigherRange);
		
		mnpUrl = parametersCacheManager.getParameterValue(
				"COMMON", "MNP_URL", null);
		otpSenderId = parametersCacheManager.getParameterValue(
				"COMMON", "OTP_SENDER", null);
		
		
		String  affliatedContentModes = parametersCacheManager.getParameterValue(
				"SMS", "SMS_AFFILIATED_CONTENT_MODES", "");
		affiliatedContentModeList = ListUtils.convertToList(affliatedContentModes, ",");
		
		String isMpByPassed = parametersCacheManager.getParameterValue(
				"COMMON", "IS_MP_BYPASSED_FOR_AFFILIATE", "FALSE");
		
		isMpByPassedForAffiliate = Boolean.parseBoolean(isMpByPassed);
		String blackListMode = RBTParametersUtils.getParamAsString("COMMON",
				"FREEMIUM_BLACKLIST_MODES",  "");
		allowedFreemiumBlackListMode = Arrays.asList(blackListMode.split(",")); 
		noOfBlackListHrs = RBTParametersUtils.getParamAsInt("COMMON",
				"FREEMIUM_BLACKLIST_HOURS",  -1);
		freemiumGroup2CosId = RBTParametersUtils.getParamAsString("COMMON",
				"FREEMIUM_GROUP2_COSID", ""); 

		fillShuffleAlwaysEnabledDisabledFlag();
		
		fillPackCosIDCosIDMap();
		doOverrideSubscriptionClassMap();
		doOverrideChargeClassMap();
		makeGiftSubscriptionClassMap();
		
		ReminderTool.init();
		// initCopyKeys();
	}

	private void fillShuffleAlwaysEnabledDisabledFlag() {
		shuffleAlwaysEnabledDisabledFlag = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
				iRBTConstant.SHUFFLE_ALWAYS_ENABLED_DISABLED_FLAG, null);
		if (shuffleAlwaysEnabledDisabledFlag != null) {
			logger.info("shuffleAlwaysEnabledDisabledFlag: " + shuffleAlwaysEnabledDisabledFlag);
			if (!shuffleAlwaysEnabledDisabledFlag.equalsIgnoreCase("TRUE") && !shuffleAlwaysEnabledDisabledFlag.equalsIgnoreCase("FALSE")) {
				logger.info("Invalid value configured in shuffleAlwaysEnabledDisabledFlag. Hence not considering the parameter");
				shuffleAlwaysEnabledDisabledFlag = null;
			}
		}
	}

	/*
	 * private void initCopyKeys() { logger.info("Entering"); String
	 * normalCopyKeys =
	 * getParamAsString("COMMON","CIRCLEWISE_NORMALCOPY_KEY",null);
	 * logger.info("parameter normalCopyKeys="+normalCopyKeys);
	 * if(normalCopyKeys != null) { directCopyKeysMap = new HashMap<String,
	 * HashSet<String>>(); //circle1:1,2,3;circle2:4,5 String[]
	 * circleIdAndKeyPairs = normalCopyKeys.split(";"); for(int i=0;
	 * i<circleIdAndKeyPairs.length; i++) { String circleIdAndKeyPair =
	 * circleIdAndKeyPairs[i]; String[] circleIdAndKeys =
	 * circleIdAndKeyPair.split(":"); String circleId = circleIdAndKeys[0];
	 * String keys = circleIdAndKeys[1]; String[] keyArray = keys.split(",");
	 * List<String> keyList = Arrays.asList(keyArray); HashSet<String> keySet =
	 * new HashSet<String>(); keySet.addAll(keyList);
	 * directCopyKeysMap.put(circleId, keySet); } }
	 * logger.info("directCopyKeysMap="+directCopyKeysMap); String optinCopyKeys
	 * = getParamAsString("COMMON","CIRCLEWISE_STARCOPY_KEY",null);
	 * logger.info("parameter optinCopyKeys="+optinCopyKeys); if(optinCopyKeys
	 * != null) { optinCopyKeysMap = new HashMap<String, HashSet<String>>();
	 * //circle1:1,2,3;circle2:4,5 String[] circleIdAndKeyPairs =
	 * optinCopyKeys.split(";"); for(int i=0; i<circleIdAndKeyPairs.length; i++)
	 * { String circleIdAndKeyPair = circleIdAndKeyPairs[i]; String[]
	 * circleIdAndKeys = circleIdAndKeyPair.split(":"); String circleId =
	 * circleIdAndKeys[0]; String keys = circleIdAndKeys[1]; String[] keyArray =
	 * keys.split(","); List<String> keyList = Arrays.asList(keyArray);
	 * HashSet<String> keySet = new HashSet<String>(); keySet.addAll(keyList);
	 * optinCopyKeysMap.put(circleId, keySet); } }
	 * 
	 * logger.info("parameter optinCopyKeysMap="+optinCopyKeysMap); }
	 */

	public static HashSet<String> tokenizeHashSet(String stringToTokenize,
			String delimiter) {
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		HashSet<String> result = new HashSet<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());
		return result;
	}

	// Added by Roshan to get the spanish translation of the actions& Access
	// Mode for TEF Spain
	private String getFinalAction(String action) {
		if (action.equalsIgnoreCase(SUBSCRIPTION))
			return "Alta";
		if (action.equalsIgnoreCase(UNSUBSCRIPTION))
			return "Baja";
		if (action.equalsIgnoreCase(PURCHASE))
			return "Compra";
		if (action.equalsIgnoreCase(CUSTOMIZATION))
			return "Personalizacion";
		if (action.equalsIgnoreCase("Gift"))
			return "Regalo";
		if (action.equalsIgnoreCase("Clonation"))
			return "Copia";

		return action;
	}

	private String getFinalMode(String mode) {
		if (mode.equalsIgnoreCase("VOICE") || mode.equalsIgnoreCase("VP"))
			return "Voz";
		if (mode.equalsIgnoreCase("PRESSSTAR"))
			return "PulsaAsterisco";
		if (mode.equalsIgnoreCase("COPY"))
			return "Copia";

		return mode;
	}

	protected void writeEventLog(String subscriberID, String mode,
			String errorNo, String action, Clip clip) {
		writeEventLog(subscriberID, mode, errorNo, action, clip, null);
	}

	protected void writeEventLog(String subscriberID, String mode,
			String errorNo, String action, Clip clip, String criteria) {
		if (getParamAsBoolean("COMMON", "ALLOW_ACTIVITY_LOGGING", "FALSE")) {
			logger.info("Writing event logger" + subscriberID + mode + errorNo
					+ action + clip);
			/*
			 * Log Format: subscriberID,action,actionResponse,URL,urResponse|
			 * statusCode|_status,hitTime,responseTime
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSS0");
			if (mode.equalsIgnoreCase("PRESSSTAR")
					|| mode.equalsIgnoreCase("COPY"))
				action = "Clonation";
			StringBuilder builder = new StringBuilder();
			builder.append(subscriberID).append(
					"|" + sdf.format(new Date()) + "|" + getFinalAction(action)
							+ "|");
			if (clip != null)
				builder.append(clip.getClipId() + "|");
			else
				builder.append("|");

			builder.append(getFinalMode(mode));

			if (action.equalsIgnoreCase("Gift"))
				builder.append("|regalo|");
			else
				builder.append("|contentRateNormal|");

			if (action.equalsIgnoreCase(CUSTOMIZATION))
				builder.append("SI|" + criteria + "|" + errorNo);
			else
				builder.append("NO||" + errorNo);

			RBTEventLogger.logEvent(RBTEventLogger.Event.DAILYREPORTACTIVITY,
					builder.toString());
		}
	}

	@Override
	public String processActivation(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = null;
		logger.info("Processing activation for task: " + task);
		try {
			subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(task);
			boolean isLimitedPackRequest = false;

			response = isValidUser(task, subscriber);
			if (!response.equals(VALID)) {
				logger.warn("Unable to process activation, invalid subscriber: "
						+ subscriberID);
				writeEventLog(subscriberID, getMode(task), "104", SUBSCRIPTION,
						null);
				return response;
			}
			
			if(rbtDBManager.isSubscriberDeactivated(subscriber)) {
				logger.info("Updated megaPromo_newuser in request, subscriber is null"
						+ " or deactive. subscriberID: " + subscriberID);
				task.put("megaPromo_newuser","true");
			}

			HashMap<String, String> userInfoMap = getUserInfoMap(task);
			if (task.containsKey(param_scratchNo)) {
				userInfoMap.put("SCRN", task.getString(param_scratchNo));
				userInfoMap.put("SCRS", "2");

			}

			Boolean isCosIdPresentInRequestAndIsAPackCosId = false;
			Boolean isCosIdPresentInRequest = false;
			
			if (task.containsKey(param_subscriptionClass)
					&& overrideSubscriptionClassMap.containsKey(task.getString(param_subscriptionClass))) {
				task.put(param_subscriptionClass, overrideSubscriptionClassMap.get(task.getString(param_subscriptionClass)));
			}
			
			if (task.containsKey(param_cosID)) {
				String cosID = task.getString(param_cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);

				if (cosDetails == null) {
					logger.error("COSID: " + cosID + 
							" is not configured in rbt_cos_details. Returning " + COS_NOT_EXISTS);
					return COS_NOT_EXISTS;
				}
				
				isCosIdPresentInRequest = true;
				isCosIdPresentInRequestAndIsAPackCosId = cosDetails != null
						&& cosDetails.getCosType() != null 
						&& (cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.SONG_PACK)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_DOWNLOADS)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT));
				
				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
								.equalsIgnoreCase(cosDetails.getCosType()) ||iRBTConstant.AZAAN
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					isLimitedPackRequest = true; 
				}
				 
				logger.info("Validating requested cos: " + cosID
						+ ", subscriberID: " + subscriberID
						+ ", isLimitedPackRequest: " + isLimitedPackRequest
						+ ", cosDetails: " + cosDetails);

				/*
				 * If Cos Id contains in the Task, it will be assumed as music
				 * pack and add it to the task with pack Cos id.
				 */
				if (cosDetails != null 
						&& cosDetails.getCosType() != null
						&& (cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.SONG_PACK) 
								|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
								||cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_DOWNLOADS) || cosDetails
								.getCosType().equalsIgnoreCase(
										iRBTConstant.UNLIMITED_DOWNLOADS) || cosDetails
										.getCosType().equalsIgnoreCase(
												iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || cosDetails
												.getCosType().equalsIgnoreCase(
														iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
					task.put(param_packCosId, cosDetails.getCosId());
				}
			}

			// added Active B2B subscriber as D2C App user (Free trial user)
			activateDTOCServiceClass(task,subscriber);
			
			// Added by Sandeep... Verifying packCosID is valid or not
			CosDetails cosDetails = null;
			if (task.containsKey(param_packCosId)) {
				String cosID = task.getString(param_packCosId);
				cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(cosID, DataUtils.getUserCircle(task));
				if (cosDetails == null
						|| !cosDetails.getEndDate().after(new Date())) {
					logger.warn("Returning Invalid Pack Cos. pack cos is expired. cos: "
							+ cosID + ", subscriberID: " + subscriberID);
					return INVALID_PACK_COS_ID;
				}
			}

			// Added by Deepak Kumar for UNSUB_DELAY: Only If the request is for
			// UPgradation , then we allow to process in this case
			// Otherwise for all other cases, it should not allow.

			String subXtraInfo = (subscriber != null) ? subscriber.extraInfo()
					: null;
			HashMap<String, String> subXtraInfoMap = DBUtility
					.getAttributeMapFromXML(subXtraInfo);
			boolean isUnsubDelayedTimeConfiguredForSms = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.SMS,
							"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
							null) != null;
			if (!isUnsubDelayedTimeConfiguredForSms && subXtraInfoMap != null
					&& subXtraInfoMap.containsKey("UNSUB_DELAY") && subscriber.endDate().after(new Date())
					&& !task.containsKey(param_isPreConsentBaseSelRequest)) {
				if (!task.containsKey(param_rentalPack))
					return NOT_ALLOWED;
				boolean isUpgradAllowedForUnsubDelayed = CacheManagerUtil
						.getParametersCacheManager()
						.getParameterValue(iRBTConstant.COMMON,
								"ALLOW_UPGRADATION_FOR_UNSUB_DELAYED_USER",
								"FALSE").equalsIgnoreCase("TRUE");
				boolean isUnsubDelayedTimeConfigured = CacheManagerUtil
						.getParametersCacheManager()
						.getParameterValue(
								iRBTConstant.COMMON,
								"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
								null) != null;

				if (isUnsubDelayedTimeConfigured
						&& !isUpgradAllowedForUnsubDelayed) {
					return NOT_ALLOWED;
				}
			}

						
			
			if (noOfBlackListHrs != -1 && allowedFreemiumBlackListMode.contains(task.getString(param_mode))
					&& freemiumGroup2CosId != null) {
				logger.info("Freemium Memcache Initialized = "
						+ FreemiumMemcacheClient.getInstance().isCacheInitialized());
				String blackListTime = FreemiumMemcacheClient.getInstance()
						.getSubscriberBlacklistTime(subscriberID);
				CosDetails freemiumCosDetails = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(freemiumGroup2CosId);
				String freemiumSubClass = freemiumCosDetails.getSubscriptionClass();
				logger.info("Subscriber_ID = " + subscriberID + " , BLACKLIST_TIME = "
						+ blackListTime);
				if (FreemiumMemcacheClient.getInstance().isCacheInitialized()) {
					if (blackListTime != null) {
						if(noOfBlackListHrs == 0){ 
							logger.info("Returning response as " + NOT_ALLOWED
									+ " as the Subscriber is blacklisted");
							return NOT_ALLOWED; 
						}
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						Date blackListDate = sdf.parse(blackListTime);
						long blacklistTimeInMillis = blackListDate.getTime();
						long finalTime = blacklistTimeInMillis + noOfBlackListHrs * 24 * 3600
								* 1000l;
						if (new Date().getTime() > finalTime) { 
							logger.info("Returning response as " + NOT_ALLOWED
									+ " as the Subscriber is blacklisted");
							return NOT_ALLOWED;
						}
					}
				} else if(freemiumGroup2CosId.equalsIgnoreCase(task.getString(param_cosID))
						||freemiumSubClass.equalsIgnoreCase(task.getString(param_subscriptionClass))
						||freemiumSubClass.equalsIgnoreCase(task.getString(param_rentalPack))){
					logger.info("Freemium Memcache is not up and the subscriber is activating on " +
							"group 2 freemium cos id So, Not Allowing to activate for the Configured Mode");
					return NOT_ALLOWED;
				}

			}
			// added by sridhar.sindiri
			// adds "PCA=TRUE" in extraInfo when a RRBT request comes for a
			// subscriber which has already active announcements
			if (RBTDeploymentFinder.isRRBTSystem()
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.PROCESS_ANNOUNCEMENTS, "FALSE")) {
				SubscriberAnnouncements[] subscriberAnnouncements = rbtDBManager
						.getActiveSubscriberAnnouncemets(task
								.getString(param_subscriberID));
				if (subscriberAnnouncements != null
						&& subscriberAnnouncements.length != 0)
					userInfoMap.put(iRBTConstant.EXTRA_INFO_PCA_FLAG, "TRUE");
			}

			boolean useSubManager = true;

			// Upgrade ADRBT user to a charged subscription class
			if (getParamAsBoolean(iRBTConstant.COMMON,
					"UPGRADE_ADRBT_ON_RBT_ACT", "false")
					&& !task.containsKey(param_isPreConsentBaseSelRequest)) {
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					if (subscriber.rbtType() == 1
							&& subscriber.subscriptionClass() != null
							&& subscriber.subscriptionClass().equalsIgnoreCase(
									getParamAsString(iRBTConstant.COMMON,
											"ADRBT_SUB_CLASS", "ADRBT"))) {
						task.put(
								param_rentalPack,
								getParamAsString(iRBTConstant.COMMON,
										"ADRBT_SUB_CLASS", "ADRBT"));
						task.put(param_rbtType, "1");
					}

				}
			}
			
			if (task.containsKey(param_rentalPack)) {
				logger.debug("Processing rental pack. subscriberID: "
						+ subscriberID);

				boolean suspendedUsersAllowed = false;
				if (task.containsKey(param_suspendedUsersAllowed)) {
					suspendedUsersAllowed = Boolean.valueOf(task
							.getString(param_suspendedUsersAllowed));
				}

				if (DataUtils.isSubscriberAllowedForUpgradation(subscriber,
						suspendedUsersAllowed)) {
					if (!task.containsKey(param_scratchNo)) {
						if (userInfoMap.containsKey("SCRS"))
							userInfoMap.remove("SCRS");
						if (userInfoMap.containsKey("SCRN"))
							userInfoMap.remove("SCRN");
					}

					String subscriptionClass = task.getString(param_rentalPack);
					String activatedBy = task.getString(param_mode);
					String activationInfo = task.getString(param_modeInfo);
					
					boolean success = false;
					if (isSupportSMClientModel(task, BASE_OFFERTYPE)) {
						success = smConvertSubscription(task,
								subscriptionClass, subscriberID, subscriber,
								activatedBy, activationInfo,
								isLimitedPackRequest);
					} else {
						int newRbtType = 0;
						if (task.containsKey(param_rbtType))
							newRbtType = Integer.parseInt(task
									.getString(param_rbtType));

						@SuppressWarnings("null")
						int oldRbtType = subscriber.rbtType();

						HashMap<String, String> extraInfoMap = new HashMap<String, String>();
						if (task.containsKey(param_scratchNo)) {
							extraInfoMap.put("SCRN",
									task.getString(param_scratchNo));
							extraInfoMap.put("SCRS", "2");
						}

						if (isLimitedPackRequest) {
							if (task.containsKey(param_mode)) {
								logger.info("Request contains mode and it is limited pack request");
								extraInfoMap.put("PACK_MODE",
										task.getString(param_mode));
								activatedBy = null;
							}
						}

						String cosID = task.getString(param_cosID);
						if (cosID != null) {
							CosDetails cos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											cosID);
							if (cos == null) {
								logger.info("Invalid cosID. Returning response: "
										+ COS_NOT_EXISTS);
								return COS_NOT_EXISTS;
							}
							
							//
							if(task.containsKey(param_cosID)) {
								String songBasedCosId = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "SONG_BASED_COS_ID", null);
								if(songBasedCosId != null) {
									List<String> cosIdsList = Arrays.asList(songBasedCosId.split(","));
									String cosId = task.getString(param_cosID);
									if(cosIdsList.contains(cosId)) {
										SubscriberStatus[] activeSubscriberStatus = rbtDBManager.getAllActiveSubscriberSettings(subscriber.subID());
										boolean hasNoSelection = true;
										if(activeSubscriberStatus != null) {
											for(SubscriberStatus activeSelection : activeSubscriberStatus) {
												if(activeSelection.status() == 1) {
													hasNoSelection = false;
												}
											}
										}
										if(hasNoSelection) {
											return COS_NOT_UPGRADE_USER_NO_SELECTION;
										}
//										extraInfoMap.put(iRBTConstant.UDS_OPTIN,"TRUE");
									}
									
									if(cosIdsList.contains(cosID) && !cosIdsList.contains(cosId)) {
										extraInfoMap.put(iRBTConstant.UDS_OPTIN,"FALSE");
									}
								}
							}
							
							
							extraInfoMap.put(iRBTConstant.EXTRA_INFO_COS_ID,
									cosID);
						} else {
							String cosForSubClass = getParamAsString(
									iRBTConstant.COMMON,
									iRBTConstant.SUBCLASS_COS_MAPPING, null);
							if (cosForSubClass != null) {
								StringTokenizer stkParent = new StringTokenizer(
										cosForSubClass, ";");
								while (stkParent.hasMoreTokens()) {
									StringTokenizer stkChild = new StringTokenizer(
											stkParent.nextToken(), ",");
									{
										if (stkChild.countTokens() == 2) {
											String pack = stkChild.nextToken()
													.trim();
											String mappedCos = stkChild
													.nextToken().trim();
											if (pack.equalsIgnoreCase(subscriptionClass)) {
												CosDetails cosObj = CacheManagerUtil
														.getCosDetailsCacheManager()
														.getCosDetail(
																mappedCos,
																subscriber
																		.circleID());
												if (cosObj != null
														&& cosObj.getCosId() != null) {
													extraInfoMap
															.put(iRBTConstant.EXTRA_INFO_COS_ID,
																	cosObj.getCosId());
												}
												break;
											}
										}
									}
								}
							}
						}

						boolean canUpgradeGraceAndSuspended = false;
						if (task.containsKey(param_upgradeGraceAndSuspended)) {
							canUpgradeGraceAndSuspended = Boolean.valueOf(task
									.getString(param_upgradeGraceAndSuspended));
						}

						logger.info("Checking to upgrade subscriber. "
								+ "Subscriber status: " + subscriber.subYes()
								+ ", canUpgradeGraceAndSuspended: "
								+ canUpgradeGraceAndSuspended);
						if (subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)
								|| canUpgradeGraceAndSuspended) {
							if (oldRbtType != newRbtType) // If AdRbt
															// upgradation
							{
								newRbtType = ((oldRbtType != newRbtType) ? newRbtType
										: oldRbtType);
								extraInfoMap
										.put((newRbtType == 1 ? iRBTConstant.EXTRA_INFO_ADRBT_ACTIVATION
												: iRBTConstant.EXTRA_INFO_ADRBT_DEACTIVATION),
												"TRUE");
							}

							String extraInfo = subscriber.extraInfo();
							HashMap<String, String> subExtraInfo = DBUtility
									.getAttributeMapFromXML(extraInfo);
							//Fix for RBT-12391,RBT-12394
							if (subExtraInfo == null) {
								subExtraInfo = new HashMap<String, String>();
							} else {
								if (subExtraInfo
										.containsKey(Constants.param_SR_ID)) {
									subExtraInfo.remove(Constants.param_SR_ID);
								}
								if (subExtraInfo
										.containsKey(Constants.param_vendor
												.toUpperCase())) {
									subExtraInfo.remove(Constants.param_vendor
											.toUpperCase());
								}
								if (subExtraInfo
										.containsKey(Constants.param_ORIGINATOR)) {
									subExtraInfo
											.remove(Constants.param_ORIGINATOR);
								}
							}
							if (!task.containsKey(param_scratchNo)
									&& extraInfo != null) {
								subExtraInfo.remove("SCRS");
								subExtraInfo.remove("SCRN");
							}
							
							subExtraInfo.remove(iRBTConstant.EXTRA_INFO_TPCGID);
							
                            if (userInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
										userInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID));
							} else if(task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
										task.getString(iRBTConstant.EXTRA_INFO_TPCGID));
							}
                            //RBT-9213
                            if (userInfoMap.containsKey(Constants.param_sdpomtxnid)) {
								extraInfoMap.put(Constants.param_sdpomtxnid,
										userInfoMap.get(Constants.param_sdpomtxnid));
							}
                            if (userInfoMap.containsKey(Constants.param_seapitype)) {
								extraInfoMap.put(Constants.param_seapitype,
										userInfoMap.get(Constants.param_seapitype));
							}
                            //Added extra info column to update the sr_id and originator info 
        					// as per the jira id RBT-11962
                            if (userInfoMap.containsKey(Constants.param_SR_ID)) {
                            	extraInfoMap.put(Constants.param_SR_ID,
            							userInfoMap.get(Constants.param_SR_ID));
            				}
            				if (userInfoMap.containsKey(Constants.param_ORIGINATOR)) {
            					extraInfoMap.put(Constants.param_ORIGINATOR,
            							userInfoMap.get(Constants.param_ORIGINATOR));
            				}
            				if (userInfoMap.containsKey(Constants.param_vendor.toUpperCase())) {
            					extraInfoMap.put(Constants.param_vendor.toUpperCase(),
            							userInfoMap.get(Constants.param_vendor.toUpperCase()));
            				}
                            //end                            
							subExtraInfo.putAll(extraInfoMap);

							if (task.containsKey(param_bIOffer)) {
								subExtraInfo.put("BI_OFFER", "TRUE");
							}
							if (subExtraInfo.containsKey("TRANS_ID")) {
								subExtraInfo.remove("TRANS_ID");
							}
							if (task.containsKey(param_userInfo + "_TRANS_ID")) {
								subExtraInfo.put("TRANS_ID",
										task.getString(param_userInfo + "_TRANS_ID"));
							}
							if (task.containsKey(param_userInfo + "_UPGRADE_CONSENT")) {
								subExtraInfo.put("UPGRADE_CONSENT",
										task.getString(param_userInfo + "_UPGRADE_CONSENT"));
							}
							if (task.containsKey(param_userInfo + "_P2P_UPGRADE")) {
								subExtraInfo.put("P2P_UPGRADE",
										task.getString(param_userInfo + "_P2P_UPGRADE"));
							}
							
							String oldActBy = subscriber.activatedBy();
							subExtraInfo.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, oldActBy);
							extraInfo = DBUtility
									.getAttributeXMLFromMap(subExtraInfo);

							boolean concatActivationInfo = true;
							//make the changes for consent flow.RBT-13221
							if (!task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								String VfUpgradeFeatureClass = CacheManagerUtil
										.getParametersCacheManager()
										.getParameterValue(
												iRBTConstant.COMMON,
												"CREATE_CLASS_FOR_VF_UPGRADE_FEATURE",
												null);
								List<String> tnbSubscriptionClasses = new ArrayList<String>();
								String tnbSubscriptionClassesStr = CacheManagerUtil
										.getParametersCacheManager()
										.getParameterValue("COMMON",
												"TNB_SUBSCRIPTION_CLASSES",
												"ZERO");
								if (tnbSubscriptionClassesStr != null)
									tnbSubscriptionClasses = Arrays
											.asList(tnbSubscriptionClassesStr
													.trim().toUpperCase()
													.split(","));
							
								String oldSubscriptionClass=subscriber.subscriptionClass();
								//For Normal user allow this 
								//this for normal user upgradation. RBT - 13221.
								Class findClass = null;
									if (VfUpgradeFeatureClass != null
											&& !VfUpgradeFeatureClass.isEmpty()
											&& ((!tnbSubscriptionClasses
													.contains(oldSubscriptionClass)) || (tnbSubscriptionClasses
													.contains(oldSubscriptionClass) && !task
												.containsKey(param_requestFromSelection)))) {
									String circleID = DataUtils
											.getUserCircle(task);
									boolean isPrepaid = DataUtils
											.isUserPrepaid(task);
									String upgradeRefID = UUID.randomUUID()
											.toString();
									
									String consentStatus = "0";
									List<String> modesForNotConsentHit = Arrays
											.asList(RBTParametersUtils.getParamAsString(
													"DOUBLE_CONFIRMATION",
													"MODES_FOR_NOT_CONSENT_HIT", "").split(","));
									
									List<String> subsrvKeyConsentHit = Arrays
											.asList(RBTParametersUtils.getParamAsString(
													"DOUBLE_CONFIRMATION", "SUB_SRV_KEY_CONSENT_HIT",
													"").split(","));
									if (activatedBy != null
											&& modesForNotConsentHit.contains(activatedBy.toUpperCase())) {
										consentStatus = "1";
										
										if(subsrvKeyConsentHit.contains(subscriptionClass.toUpperCase()))
										{
											consentStatus = "0";
											logger.info("--> Reseting consent status . subscriptionClass : "+subscriptionClass);
											
										}
										
										task.put("CONSENTSTATUS",consentStatus);
										logger.info("--> Putting consent status in task "+consentStatus);
										
									}
									
									UpgradeObject upgradeObject = new UpgradeObject(
											subscriberID, activatedBy, null,
											null, activationInfo, isPrepaid,
											cosID, newRbtType, extraInfo,
											circleID, upgradeRefID, consentStatus,
											subscriber.subscriptionClass(),
											subscriptionClass);
									try {
										findClass = Class
												.forName(VfUpgradeFeatureClass);
										VfRBTUpgradeConsent upgradeImplObj = (VfRBTUpgradeConsent) findClass
												.newInstance();
										Subscriber consentSubscriber = upgradeImplObj
												.consentUpgradeFlow(upgradeObject);
										if (null != consentSubscriber) {
											response = SUCCESS;
											task.put(param_subscriber_consent,
													consentSubscriber);
											task.put(
													param_upgrade_consent_flow,
													"true");
											logger.info("Processed rental pack, response: "
													+ response);
											return response;
										} else {
											logger.info("Mode check for upgrade flow failed");
											task.put(
													param_upgrade_consent_flow,
													"true");
										}
									} catch (ClassNotFoundException e) {
										logger.error("class cast exception in upgrade flow", e);
									}
								} else if (VfUpgradeFeatureClass != null
										&& !VfUpgradeFeatureClass.isEmpty()
										&& tnbSubscriptionClasses
												.contains(oldSubscriptionClass)
										&& task.containsKey(param_requestFromSelection)) {
									findClass = Class
											.forName(VfUpgradeFeatureClass);
									VfRBTUpgradeConsent upgradeImplObj = (VfRBTUpgradeConsent) findClass
											.newInstance();
									boolean upgradeFlow = upgradeImplObj
											.CheckUpgradeModeIsConfigured(activatedBy);
									task.put(param_upgrade_consent_flow,
											"true");
									if (upgradeFlow) {
										task.put(param_consent_subscriptionClass,
												subscriptionClass);
										response = SUCCESS;
										return response;
									}
								}
							}
//							ganesh added it
								if(task.containsKey(WebServiceConstants.param_isPreConsentBaseRequest)) {
									// please check these parameters which all
									// required and which need to be ignored.
									String circleID = DataUtils.getUserCircle(task);
									boolean isPrepaid = DataUtils.isUserPrepaid(task);
									String upgradeRefID = com.onmobile.apps.ringbacktones.services.common.Utility
											.generateConsentIdRandomNumber("");
									if (upgradeRefID == null) {
										upgradeRefID = UUID.randomUUID().toString();
									}
									success = rbtDBManager
											.convertSubscriptionTypeConsentUpgrde(
													subscriberID, activatedBy,
													null, null, activationInfo,
													isPrepaid, cosID, newRbtType,extraInfo, circleID,
													upgradeRefID, "0", subscriber.subscriptionClass(), subscriptionClass);
									if (success) {
										task.put(srvkey, subscriptionClass);
										task.put(param_transID, upgradeRefID);
									}
									logger.info("Updated status changed, update "
											+ "status: " + success
											+ " for subscriber: " + subscriberID +"subscriptionClass: " +subscriptionClass);
								}else if(!task.containsKey(WebServiceConstants.param_isPreConsentBaseSelRequest)){
									success = rbtDBManager.convertSubscriptionType(
											subscriberID,
											subscriber.subscriptionClass(),
											subscriptionClass, activatedBy,
											activationInfo, concatActivationInfo,
											newRbtType, true, extraInfo, subscriber);
									logger.info("Updated status changed, update "
											+ "status: " + success
											+ " for subscriber: "
											+ subscriberID);
								}
						} else {
							// Subscriber is in pending state, so request will
							// be stored in transaction table.
							SubscriptionClass subClass = CacheManagerUtil
									.getSubscriptionClassCacheManager()
									.getSubscriptionClass(subscriptionClass);
							if (subClass == null)
								return INVALID_SUBSCRIPTION_CLASS;

							extraInfoMap.put(ExtraInfoKey.RBT_TYPE.toString(),
									String.valueOf(newRbtType));

							if (task.containsKey(param_bIOffer)) {
								extraInfoMap.put("BI_OFFER", "TRUE");
							}
							
                            if (userInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
										userInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID));
							}
                            
							String oldActBy = subscriber.activatedBy();
							extraInfoMap.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, oldActBy);

							String extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);

							ProvisioningRequests provisioningRequest = new ProvisioningRequests(
									subscriptionClass, new Date(), extraInfo,
									activatedBy, activationInfo, null, 0, null,
									Status.TOBE_PROCESSED.getStatusCode(),
									subscriberID, null,
									Type.BASE_UPGRADATION.getTypeCode());

							provisioningRequest = ProvisioningRequestsDao
									.createProvisioningRequest(provisioningRequest);
							logger.info("Added provisioning request: "
									+ provisioningRequest + " for subscriber: "
									+ subscriberID);
							success = (provisioningRequest != null);
						}
					}
					if (success) {
						response = SUCCESS;
						subscriber = rbtDBManager.getSubscriber(subscriberID);

						// Updated Subscriber object is storing in taskSession &
						// it will be used to build the response element
												
						boolean isUpgradAllowedForUnsubDelayed = CacheManagerUtil
								.getParametersCacheManager()
								.getParameterValue(
										iRBTConstant.COMMON,
										"ALLOW_UPGRADATION_FOR_UNSUB_DELAYED_USER",
										"FALSE").equalsIgnoreCase("TRUE");
						boolean isUnsubDelayedTimeConfigured = CacheManagerUtil
								.getParametersCacheManager()
								.getParameterValue(
										iRBTConstant.COMMON,
										"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
										null) != null;
						String xtraInfo = subscriber.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(xtraInfo);
						if (xtraInfoMap != null
								&& xtraInfoMap.containsKey("UNSUB_DELAY")
								&& isUpgradAllowedForUnsubDelayed
								&& isUnsubDelayedTimeConfigured) {
							xtraInfoMap.remove("UNSUB_DELAY");
							xtraInfo = DBUtility
									.getAttributeXMLFromMap(xtraInfoMap);
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyyMMdd HH:mm:ss");
							Date defaultEndDate = null;
							try {
								defaultEndDate = sdf.parse("20371231 00:00:00");
							} catch (Exception ex) {
								logger.info("exception in processResubscriptionRequest() while parsing");
							}
							rbtDBManager.updateEndDateAndExtraInfo(
									subscriberID, defaultEndDate, xtraInfo);
							subscriber = rbtDBManager
									.getSubscriber(subscriberID);
						}
						task.put(param_subscriber, subscriber);

					} else {
						response = FAILED;
					}
					logger.info("Processed rental pack, response: " + response);
					return response;
				}
			}

			// Below if block processes the acceptance Gift Service request for
			// already active users.
			// It just updates the Gift(ViralSMSTable) entry.
			// boolean acceptGift =
			// task.getString(param_action).equalsIgnoreCase(action_acceptGift);
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift)) {
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					String gifterID = task.getString(param_gifterID);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task
							.getString(param_giftSentTime));
					String acceptStatus = "ACCEPT_PRE";

					ViralSMSTable gift = rbtDBManager.getViralPromotion(
							gifterID, subscriberID, sentTime, "GIFTED");
					if (gift.clipID() == null) {
						rbtDBManager.updateViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED", acceptStatus,
								new Date(), null, null);

						logger.info("Processed gift request, response: "
								+ SUCCESS);
						return SUCCESS;
					}
				}
			}

			String packExtraInfoXml = null;
			boolean isDirectActivation = false;
			if (task.containsKey(param_isDirectActivation)
					&& task.getString(param_isDirectActivation)
							.equalsIgnoreCase(YES))
				isDirectActivation = true;

			String circleID = DataUtils.getUserCircle(task);
			boolean isPrepaid = DataUtils.isUserPrepaid(task);

			CosDetails cos = null;
			boolean isUpgradeDownloadLimitSongPack = RBTParametersUtils.getParamAsBoolean("COMMON","UPGRAGE_DOWNLOAD_LIMIT_SONG_PACK", "FALSE");
			if(task.containsKey(param_cosID)) {				
				CosDetails tempCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_cosID));
				String cosType = (null != tempCos) ? tempCos.getCosType() : null;
				
				if (iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
						.equalsIgnoreCase(cosType)
						|| (isUpgradeDownloadLimitSongPack && iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosType))) {
					cos = tempCos;
				}
				logger.info("Since request contains cosID, cosType is validated. Final cos: " + cos
						+ ", subscriberID: " + subscriberID);
			}
//			if(cos == null && task.containsKey(param_cosID) && RBTParametersUtils.getParamAsBoolean("COMMON","UPGRAGE_DOWNLOAD_LIMIT_SONG_PACK", "FALSE")) {
//				Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//						.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
//				
//				List<String> musicPackCosIdList = null;
//				
//				if(muiscPackCosIdParam != null) {
//					musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//					if(musicPackCosIdList.contains(task.getString(param_cosID))) {
//						cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_cosID));
//					}
//				}				 
//			}
			
			if(cos == null) {
				cos = getCos(task, subscriber);
			}
			if (cos != null
					&& cos.getCosType() != null
					&& (cos.getCosType().equalsIgnoreCase(
							iRBTConstant.SONG_PACK)
							|| cos.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
							|| cos.getCosType().equalsIgnoreCase(
									iRBTConstant.LIMITED_DOWNLOADS) || cos
							.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS) || cos
									.getCosType().equalsIgnoreCase(
											iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || cos
											.getCosType().equalsIgnoreCase(
													iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
				logger.info("Since CosType is: " + cos.getCosType()
						+ ", updated cos: " + cos+", in request");
				cosDetails = cos;
				task.put(param_packCosId, cos.getCosId());
			}

			if (task.containsKey(param_packCosId)) {
				/*
				 * If packOfferId is present in the task, then create and put
				 * offerId into the extra info map and again put extra info map
				 * into the task.
				 */
				String offerId = task.getString(param_packOfferID);
				if (offerId != null) {
					Map<String, String> packExtraInfoMap = new HashMap<String, String>();
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							offerId);
					packExtraInfoXml = DBUtility
							.getAttributeXMLFromMap(packExtraInfoMap);
				}				

				if (packCosIdCosIdMap.containsKey(task.getString(param_packCosId))) {
					if (!isCosIdPresentInRequest || isCosIdPresentInRequestAndIsAPackCosId) {
						String cosId = packCosIdCosIdMap.get(task.getString(param_packCosId));
						cos = CacheManagerUtil
								.getCosDetailsCacheManager()
								.getCosDetail(cosId);

						logger.info("cosId replaced with DB config. New cosId: " + cosId);
					} 
				} else {
					cos = CacheManagerUtil
							.getCosDetailsCacheManager()
							.getDefaultCosDetail(circleID, ((isPrepaid) ? YES : NO));
					logger.info("Since request contains packCosId, fetched cos by circleID: "
							+ circleID + ", cos: " + cos);
				}
			}
			// If user entry is already there in DB, then current status of the
			// user will be returned.
			if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {

				if (task.containsKey(param_isPreConsentBaseSelRequest)) {
					String status = Utility.getSubscriberStatus(subscriber);
					task.put("IS_PREPAID_CONSENT", subscriber.prepaidYes());
					task.put("CIRCLE_ID_CONSENT", subscriber.circleID());
					task.put("SUB_CLASS_CONSENT",
							subscriber.subscriptionClass());
					task.put("RBT_TYPE_CONSENT", subscriber.rbtType());
					logger.warn("Retuning status: " + status + ", subscriber: "
							+ subscriberID + ", is non-deactive and "
							+ "request is pre-consent base selection request");
					return status;
				}

				/*
				 * If user is already active and purchasing first song thru
				 * COPY, then there should be one update query for putting the
				 * below flag in subscriber's extraInfo
				 */
				String subExtraInfo = subscriber.extraInfo();
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subExtraInfo);
				if (userInfoMap != null
						&& (userInfoMap
								.containsKey(iRBTConstant.FREE_COPY_AVAILED) || userInfoMap
								.containsKey(iRBTConstant.MOBILE_APP_FREE))
						&& (subExtraInfoMap == null
								|| !subExtraInfoMap
										.containsKey(iRBTConstant.FREE_COPY_AVAILED) || !subExtraInfoMap
									.containsKey(iRBTConstant.MOBILE_APP_FREE))) {
					if (subExtraInfoMap == null)
						subExtraInfoMap = new HashMap<String, String>();
					if (userInfoMap.containsKey(iRBTConstant.MOBILE_APP_FREE)) {
						subExtraInfoMap.put(iRBTConstant.MOBILE_APP_FREE,
								userInfoMap.get(iRBTConstant.MOBILE_APP_FREE));
					}
					if (userInfoMap.containsKey(iRBTConstant.FREE_COPY_AVAILED)) {
						subExtraInfoMap
								.put(iRBTConstant.FREE_COPY_AVAILED,
										userInfoMap
												.get(iRBTConstant.FREE_COPY_AVAILED));
					}
					rbtDBManager.updateExtraInfo(subscriberID,
							DBUtility.getAttributeXMLFromMap(subExtraInfoMap));
				}

				String status = Utility.getSubscriberStatus(subscriber);
				
				// added by Sandeep for profile pack when user not deactivated
				if (task.containsKey(param_packCosId)) {
					String cosID = (String) task.get(param_packCosId);
					logger.info("Checking for proifile pack entry into provisioning_requests table for subscriberID "
							+ subscriberID + ", cosID: " + cosID);
					if (cosDetails != null
							&& cosDetails.getEndDate().after(new Date())) {
						if (rbtDBManager
								.isPackActivated(subscriber, cosDetails)) {
							logger.warn("Pack is already active. subscriberID "
									+ subscriberID + ", cosID: " + cosID);
							return PACK_ALREADY_ACTIVE;
						} else if (!rbtDBManager.isSubscriberDeactivationPending(subscriber)
								&& !rbtDBManager.isSubscriberSuspended(subscriber)) {							
							
							String packDeactPending = isSongPackDeactivationPending(subscriberID, subExtraInfoMap);
							if(packDeactPending != null) {
								return packDeactPending;
							}
							
							int packNumMaxSelection = -1;
							
							if(isDownloadSongPackOverLimitReached(cosID, subscriber.subID(), task)) {
								return OVERLIMIT;
							}
							
							if(task.containsKey("PACK_NUM_MAX_SELECTON")) {
								packNumMaxSelection = Integer.parseInt((String)task.remove("PACK_NUM_MAX_SELECTON"));
							}
							
							HashMap<String, String> existingExtraInfoMap = rbtDBManager
									.getExtraInfoMap(subscriber);
							String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
									.get(iRBTConstant.EXTRA_INFO_PACK) : null;
							String newPack = task.getString(param_packCosId);
							String updatedPacks = (existingPacks != null) ? existingPacks
									+ "," + newPack
									: newPack;
							rbtDBManager.updateExtraInfo(subscriber.subID(),
									iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
							task.remove(param_subscriber);
							task.put(param_subscriber,
									DataUtils.getSubscriber(task));

							String chargingClass = (cosDetails != null) ? cosDetails
									.getSmsKeyword() : null;
							int type = Integer.parseInt(cosID);
							int packStatus = rbtDBManager
									.getPackStatusToInsert(subscriber);
							String mode = getMode(task);
							String modeInfo = getModeInfo(task);
							String transId = UUID.randomUUID().toString();
							ProvisioningRequests provisioningReqs = new ProvisioningRequests(
									subscriber.subID(), type, mode, modeInfo,
									transId, chargingClass, packStatus);
							provisioningReqs.setExtraInfo(packExtraInfoXml);
							if(packNumMaxSelection != -1) {
								provisioningReqs.setNumMaxSelections(packNumMaxSelection);
							}
							if (rbtDBManager
									.insertProvisioningRequestsTable(provisioningReqs) != null) {
								task.put(param_activatedPackNow, YES);
								logger.info("Returning success, inserted into"
										+ " provisioning request. "
										+ "subscriberID " + subscriberID
										+ ", cosID: " + cosID
										+ ", provisioningReqs: "
										+ provisioningReqs);
								return SUCCESS;
							}
							logger.info("Returning status: " + status
									+ " cosID: " + cosID + ", subscriberID "
									+ subscriberID);
							return status;
						}
					} else {
						logger.warn("Returning INVALID_PACK_COS_ID, cosID: "
								+ cosID + ", subscriberID " + subscriberID);
						return INVALID_PACK_COS_ID;
					}

				}

				// If request is Direct Activation then user will be allowed to
				// activate directly
				logger.info("Checking direct activation request or not. isDirectActivation: "
						+ isDirectActivation + ", task: " + task);
				if (!isDirectActivation) {
					logger.warn("Not processing activation, returning " + status
							+ ", request is not direct activation");
					
					String packDeactPending = isSongPackDeactivationPending(subscriberID, subExtraInfoMap);
					if(packDeactPending != null) {
						return packDeactPending;
					}
					
					return status;
				}
			}

			String activatedBy = getMode(task);
			if (task.containsKey(param_actMode)) {
				if (task.getString(param_actMode) != null
						|| !(task.getString(param_actMode).equals(""))) {
					activatedBy = task.getString(param_actMode);
				}
			}
			String activationInfo = getModeInfo(task);

			int activationPeriod = 0;

			int freePeriod = 0;
			if (task.containsKey(param_freePeriod))
				freePeriod = Integer.parseInt(task.getString(param_freePeriod));

			int rbtType = 0;
			if (task.containsKey(param_rbtType))
				rbtType = Integer.parseInt(task.getString(param_rbtType));

			Date startDate = null;

			Date endtDate = null;
			if (task.containsKey(param_subscriptionPeriod)) {
				String subscriptionPeriod = task
						.getString(param_subscriptionPeriod);
				int validityPeriod = Utility
						.getValidityPeriod(subscriptionPeriod);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, validityPeriod - 1);
				endtDate = calendar.getTime();
			}

			// Get the subscription class to activate subscriber 
			String subscriptionClass = getSubscriptionClass(task, cos);

			if ((task.containsKey(param_isPreConsentBaseRequest)
					|| task.containsKey(param_isPreConsentBaseSelRequest))
					&& Arrays.asList(iRBTConstant.COMMON, "ADRBT_SUB_CLASS", "")
							.contains(subscriptionClass)) {
                 rbtType = 1;
			}
			// add code for SRBT
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
			if (selectionInfoMap.get("ALLOW_PUBLISH") != null) {
				userInfoMap.put("ALLOW_PUBLISH",
						selectionInfoMap.get("ALLOW_PUBLISH"));
			}

			/*
			 * Added by SenthilRaja Logic for activate the subscriber in SM
			 * model Get the offer from UI, activate the subscriber as P, invoke
			 * the SM request If request fails, remove the subscriber.
			 */
			// if(getParamAsBoolean(iRBTConstant.COMMON,
			// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
			int offerType;
			if (task.containsKey(param_requestFromSelection)) {
				offerType = COMBO_SUB_OFFERTYPE;
			} else {
				offerType = BASE_OFFERTYPE;
			}

			// for putting retailer id in extra info :
			if (task.containsKey(param_retailerID)) {
				userInfoMap.put("RET", task.getString(param_retailerID));
			}

			if (Arrays.asList(
					RBTParametersUtils.getParamAsString("COMMON",
							"PRE_PROMPT_SUPPORTED_SUB_CLASSES", "").split(","))
					.contains(subscriptionClass)) {
				userInfoMap.put(iRBTConstant.EXTRA_INFO_INTRO_PROMPT_FLAG,
						iRBTConstant.ENABLE_PRESS_STAR_INTRO);
				userInfoMap.put(iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT,
						iRBTConstant.YES);
			}else if(RBTParametersUtils.getParamAsString("COMMON",
							"SUB_CLASSES_AND_PRE_RBT_WAV_MAPPING", null)!=null){
				String preRbtWavFile = getPreRBTWavFileFromSubClassConfig(subscriptionClass);
				userInfoMap.put(iRBTConstant.EXTRA_INFO_PRE_RBT_WAV, preRbtWavFile);
			}
			
			
			//Get Offer for Gift Tef-Spain new RBT re-pricing
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift) &&((!task.containsKey(param_requestFromSelection)
							&& !task.containsKey(param_offerID)) || (task.containsKey(param_requestFromSelection)
									&& !task.containsKey(param_subscriptionOfferID)))) {
				
				boolean allowBaseOffer = RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_GET_OFFER, "FALSE") || RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_ONLY_BASE_OFFER, "FALSE");
				if(allowBaseOffer) {
					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, activatedBy, 
							Offer.OFFER_TYPE_SUBSCRIPTION, null, cos.getSmsKeyword(), null); 
					if(offer != null && offer.length > 0) {
						subscriptionClass = offer[0].getSrvKey();
						
						if(task.containsKey(param_requestFromSelection) ){
							task.put(param_subscriptionOfferID, offer[0].getOfferID());
						}
						else {
							task.put(param_offerID, offer[0].getOfferID());
						}
						
						userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offer[0].getOfferID());
					}
				}
			}
			
			if (RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") || RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER,
							"FALSE")) {
				
				
				if((!task.containsKey(param_requestFromSelection)
							&& !task.containsKey(param_offerID)) || (task.containsKey(param_requestFromSelection)
									&& !task.containsKey(param_subscriptionOfferID))) {
					
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
					rbtDetailsRequest.setMode(activatedBy);
					rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);
					Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
					
//					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, activatedBy, 
//							Offer.OFFER_TYPE_SUBSCRIPTION, null, cos.getSmsKeyword(), null); 
					if(offers != null && offers.length > 0) {
						subscriptionClass = offers[0].getSrvKey();
						
						if(task.containsKey(param_requestFromSelection) ){
							task.put(param_subscriptionOfferID, offers[0].getOfferID());
						}
						else {
							task.put(param_offerID, offers[0].getOfferID());
						}		
						
						userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offers[0].getOfferID());
					}					
				}
				
				String offerId = task.getString(param_subscriptionOfferID);
				if(offerId == null){
					offerId = task.getString(param_offerID);
				}
				logger.info("Going for Subscription Offer....");
				logger.info("SubscriberID = " + subscriberID + " , Mode = "
						+ task.getString(param_mode) + " , OFFER_ID = " + offerId
						+ " , Offer Type = 2(Subscription) , SubscriptionClass = " + subscriptionClass);

				String offerIdForBlacklisted = RBTParametersUtils.getParamAsString("COMMON",
						"BASE_OFFER_ID_FOR_BLACKLISTED_SUBSCRIBER", "");
				if (offerId != null && Arrays.asList(offerIdForBlacklisted.split(",")).contains(offerId)) {
					String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
							"ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriberID);
					String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("OFFER",
							"BLACKLISTED_OFFER_BASE_TEXT", null);
					task.put(param_smsText, smsText);
					sendSMS(task);
					return SUCCESS;
				}
                 if(offerId!=null && !offerId.equalsIgnoreCase("-1")){
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				}
			}
			

			if (task.containsKey(param_isPreConsentBaseRequest)
					|| task.containsKey(param_isPreConsentBaseSelRequest)) {
				String consentTransID = com.onmobile.apps.ringbacktones.services.common.Utility
						.generateConsentIdRandomNumber("");
				if (consentTransID == null) {
					consentTransID = UUID.randomUUID().toString();
				}
				// idea
				if (cos != null)
					task.put("SUB_COS_CONSENT", cos);
				task.put("IS_PREPAID_CONSENT", isPrepaid);
				task.put("CIRCLE_ID_CONSENT", circleID);
				task.put("SUB_CLASS_CONSENT", subscriptionClass);
				task.put("RBT_TYPE_CONSENT", rbtType);
				response = rbtDBManager.insertPreConsentSubscriptionRequest(
						subscriberID, activatedBy, startDate, endtDate,
						isPrepaid, activationPeriod, freePeriod,
						activationInfo, subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, userInfoMap, circleID,
						consentTransID, task);
				if (task.containsKey(param_isPreConsentBaseSelRequest) && !response.equalsIgnoreCase(SUCCESS)) {
					response = preConsentBaseSelSuccess;
				}
				if (response.equalsIgnoreCase(SUCCESS))
					task.put(param_transID, consentTransID);

				task.put(param_response, response);
				return response;
			}
			if (isSupportSMClientModel(task, offerType)) {
				subscriber = smActivateSubscriber(task, userInfoMap,
						subscriber, subscriberID, activatedBy, startDate,
						endtDate, isPrepaid, activationPeriod, freePeriod,
						activationInfo, subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, circleID);
			} else {
				if (!task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_offerID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_offerID));
				else if (task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_subscriptionOfferID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_subscriptionOfferID));
				
				if(task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
					userInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
							task.getString(iRBTConstant.EXTRA_INFO_TPCGID));
				}
				boolean isComboReq = false;
                if(task.containsKey(IS_COMBO_REQUEST)){
                	isComboReq = true;
                }
				// Christmas promotions
				subscriptionClass = checkChristmasPeriod(subscriptionClass,
						task, null, null, null);
				logger.debug("Processing subscriber activation. subscriberId: "
						+ subscriberID + ", subscrptionclass: "
						+ subscriptionClass + ", cos: " + cos
						+ ", userInfoMap: " + userInfoMap);
				
				String refId = task.getString(param_linkedRefId);
				if(refId == null) {
					refId = task.getString(param_refID);
				}
				
				//RBT-9873 Added xtraParametersMap for CG flow
				Map<String,String> xtraParametersMap = new HashMap<String,String>();
				
				// RBT-10785
				boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
						"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
				if(addProtocolNumber) {
					activationInfo = appendProtocolNumber(subscriberID, activationInfo);
				}
				if(task.containsKey(param_isUdsOn)){
					xtraParametersMap.put(param_isUdsOn, task.getString(param_isUdsOn)); 
				}
				// Added extra info column to update the sr_id and originator
				// info
				// as per the jira id RBT-11962
				final String strSR_ID = param_selectionInfo + "_"
						+ Constants.param_SR_ID;
				final String strORIGINATOR = param_selectionInfo + "_"
						+ Constants.param_ORIGINATOR;
				final String strVENDOR = param_selectionInfo + "_"
						+ Constants.param_vendor.toUpperCase();
				if (task.containsKey(strSR_ID)) {
					userInfoMap.put(Constants.param_SR_ID,
							task.getString(strSR_ID));
				}
				if (task.containsKey(strORIGINATOR)) {
					userInfoMap.put(Constants.param_ORIGINATOR,
							task.getString(strORIGINATOR));
				}
				if (task.containsKey(strVENDOR)) {
					userInfoMap.put(Constants.param_vendor.toUpperCase(),
							task.getString(strVENDOR));
				}
				if (task.containsKey(param_language)) {
					xtraParametersMap.put(Constants.param_LANG_CODE,
							task.getString(param_language));
				}
				if (task.containsKey(param_agentId)) {
					xtraParametersMap.put(Constants.param_agentId,
							task.getString(param_agentId));
				}
				// end 
				subscriber = rbtDBManager.activateSubscriber(subscriberID,
						activatedBy, startDate, endtDate, isPrepaid,
						activationPeriod, freePeriod, activationInfo,
						subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, userInfoMap, circleID,
						refId, isComboReq, xtraParametersMap);
				
				if(xtraParametersMap.containsKey("CONSENTSTATUS"))
				{
					logger.info("-->  xtraParametersMap.get.consentstatus"+xtraParametersMap.get("CONSENTSTATUS"));
					task.put("CONSENTSTATUS", xtraParametersMap.remove("CONSENTSTATUS"));
					logger.info("--> xtraParametersMap.get.combo "+xtraParametersMap.get("COMBO"));

					
				}
				
				if(xtraParametersMap.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
					response= SUCCESS;
					task.put("CONSENT_SUBSCRIPTION_INSERT", "true");
					task.put(param_activatedNow, YES);
					logger.info("Subscription Consent Record Inserted");
				}
				if(xtraParametersMap.containsKey("DESCRIPTION")) {
					task.put("DESCRIPTION",xtraParametersMap.remove("DESCRIPTION"));
					logger.info("CG hit is success for bsnl east");
				}
				// RBT-13642
				task.put("CGURL", xtraParametersMap.remove("CGURL"));
				// RBT-13642
				task.put("CGHttpResponse",
						xtraParametersMap.remove("CGHttpResponse"));
				if (xtraParametersMap.containsKey("CONSENTID")) {
					task.put("CONSENTID", xtraParametersMap.remove("CONSENTID"));
				}
				if (xtraParametersMap.containsKey("CONSENTCLASSTYPE")) {
					task.put("CONSENTCLASSTYPE",
							xtraParametersMap.remove("CONSENTCLASSTYPE"));
				}
				if (xtraParametersMap.containsKey("CONSENTSUBCLASS")) {
					task.put("CONSENTSUBCLASS",
							xtraParametersMap.remove("CONSENTSUBCLASS"));
				}
				if (xtraParametersMap.containsKey("EVENT_TYPE")) {
					task.put("EVENT_TYPE",
							xtraParametersMap.remove("EVENT_TYPE"));
				}
				if (xtraParametersMap.containsKey("LANGUAGE_ID")) {
					task.put("LANGUAGE_ID",
							xtraParametersMap.remove("LANGUAGE_ID"));
				}
				if (xtraParametersMap.containsKey("PLAN_ID")) {
					task.put("PLAN_ID", xtraParametersMap.remove("PLAN_ID"));
				}
				boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
				if (rbtDBManager.isTnbReminderEnabled(subscriber) && !is121TnbEnabled)
					rbtDBManager.insertTNBSubscriber(subscriber.subID(),
							subscriber.circleID(),
							subscriber.subscriptionClass(),
							subscriber.startDate(), 0);
			}

			// Activated Subscriber object is storing in taskSession & it will
			// be used to build the response element
			task.put(param_subscriber, subscriber);

			if (!task.containsKey(param_isPreConsentBaseRequest)
					&& !task.containsKey(param_isPreConsentBaseSelRequest) && !task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {

				if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
					response = SUCCESS;
					task.put(param_activatedNow, YES);

					String language = task.getString(param_language);
					if (language != null
							&& (subscriber.language() == null || !subscriber
									.language().equalsIgnoreCase(language))) {
						rbtDBManager.setSubscriberLanguage(subscriberID,
								language);
						subscriber.setLanguage(language);
					}

					if (task.getString(param_action).equalsIgnoreCase(
							action_acceptGift)) {
						String gifterID = task.getString(param_gifterID);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Date sentTime = dateFormat.parse(task
								.getString(param_giftSentTime));
						String acceptStatus = "ACCEPT_ACK";

						ViralSMSTable gift = rbtDBManager.getViralPromotion(
								gifterID, subscriberID, sentTime, "GIFTED");

						// Clip ID null means Gift Service Request & only in
						// case of
						// Gift Service request gift entry will be updated
						if (gift.clipID() == null)
							rbtDBManager.updateViralPromotion(gifterID,
									subscriberID, sentTime, "GIFTED",
									acceptStatus, new Date(), null, null);
					}
					String campaignCode = task.getString(iRBTConstant.CAMPAIGN_CODE);
					String treatmentCode = task.getString(iRBTConstant.TREATMENT_CODE);
					String offerCode =  task.getString(iRBTConstant.OFFER_CODE);
					if (campaignCode != null && treatmentCode != null && offerCode != null
							&& !task.containsKey(IS_COMBO_REQUEST)) {
						String msg = iRBTConstant.CAMPAIGN_CODE + "=" + campaignCode + ","
								+ iRBTConstant.TREATMENT_CODE + "=" + treatmentCode + ","
								+ iRBTConstant.OFFER_CODE + "=" + offerCode + ","
								+ iRBTConstant.RETRY_COUNT + "=0";
						RBTCallBackEvent.insert(subscriberID, subscriber.refID(), msg,
								RBTCallBackEvent.SM_CALLBACK_PENDING, 
								RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								-1, activatedBy);
					}


					// added by Sandeep for profile Pack
					if (task.containsKey(param_packCosId) && cosDetails != null
							&& !rbtDBManager.isSubscriberDeactivationPending(subscriber)
							&& !rbtDBManager.isSubscriberSuspended(subscriber)) {	

						HashMap<String, String> existingExtraInfoMap = rbtDBManager
								.getExtraInfoMap(subscriber);
						String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
								.get(iRBTConstant.EXTRA_INFO_PACK) : null;
						String newPack = task.getString(param_packCosId);
						String updatedPacks = (existingPacks != null) ? existingPacks
								+ "," + newPack
								: newPack;
						rbtDBManager.updateExtraInfo(subscriber.subID(),
								iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
						task.remove(param_subscriber);
						task.put(param_subscriber,
								DataUtils.getSubscriber(task));

						String cosID = (String) task.get(param_packCosId);
						int type = Integer.parseInt(cosID);
						logger.info("Request contains packCosId, making entry into "
								+ "provisioning_requests table. cosID: "
								+ cosID + ", subscriberID: " + subscriberID);
						String chargingClass = cosDetails.getSmsKeyword();
						int packStatus = rbtDBManager
								.getPackStatusToInsert(subscriber);
						String mode = getMode(task);
						String modeInfo = getModeInfo(task);
						String transId = UUID.randomUUID().toString();
						ProvisioningRequests provisioningReqs = new ProvisioningRequests(
								subscriber.subID(), type, mode, modeInfo,
								transId, chargingClass, packStatus);
						provisioningReqs.setExtraInfo(packExtraInfoXml);
						ProvisioningRequests provisioningRequests = rbtDBManager
								.insertProvisioningRequestsTable(provisioningReqs);
						logger.info("Inserted pack request into provisioning"
								+ " requests table. subscriberId: "
								+ subscriberID + ", requestId: "
								+ provisioningRequests.getRequestId());
					}
					
					//Tef-Spain yavoy new RBT service re-pricing RBT-11113 : to remove expired donwloads with status STATE_DOWNLOAD_SEL_TRACK(t).
					rbtDBManager.removeDeactivateSubscriberDownloads(subscriber.subID());
					
				} else
					response = FAILED;

				if (response.equals(SUCCESS)) {
					sendAcknowledgementSMS(task, "ACTIVATION");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Processed subscriber activation, response: " + response
				+ ", subscriberId: " + subscriberID);
		return response;
	}

	
	private void fillPackCosIDCosIDMap() {
		String paramValue = RBTParametersUtils.getParamAsString("COMMON","AZZAN_PACKCOSID_COSID_MAPPING", null);
		if (paramValue != null) {
			try {
				for (String packCosIDCosID : paramValue.split(",")) {
					String[] packCosIDCosIDSplit = packCosIDCosID.split(":");
					String packCosID = packCosIDCosIDSplit[0];
					String cosID = packCosIDCosIDSplit[1];
					packCosIdCosIdMap.put(packCosID, cosID);
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				logger.error("Invalid parameter value : " + paramValue, e);
			}
		}
	}

	private void doOverrideSubscriptionClassMap() {
		
		String paramValue = RBTParametersUtils.getParamAsString("COMMON","OVERRIDE_SUBSCRIPTION_CLASS", null);
		if (null != paramValue) {
			try {
				for (String subsClassStr : paramValue.split(",")) {
					String[] subsClassStrSplit = subsClassStr.split(":");
					String subsClass = subsClassStrSplit[0];
					String overrideSubsClass = subsClassStrSplit[1];
					overrideSubscriptionClassMap.put(subsClass, overrideSubsClass);
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				logger.error("Invalid parameter value : " + paramValue, e);
			}
		}
	}
	
	private void doOverrideChargeClassMap() {
		
		String paramValue = RBTParametersUtils.getParamAsString("COMMON","OVERRIDE_CHARGE_CLASS", null);
		if (null != paramValue) {
			try {
				for (String chargeClassStr : paramValue.split(",")) {
					String[] chargeClassStrSplit = chargeClassStr.split(":");
					String chargeClass = chargeClassStrSplit[0];
					String overrideChargeClass = chargeClassStrSplit[1];
					overrideChargeClassMap.put(chargeClass, overrideChargeClass);
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				logger.error("Invalid parameter value : " + paramValue, e);
			}
		}
	}
	
	private void makeGiftSubscriptionClassMap() {
		String tmp = RBTParametersUtils.getParamAsString("DAEMON","MODE_GIFT_SUB_CLASSES", null);
		if (tmp != null && tmp.length() > 0)
		{
			m_modeSubClassMap = new HashMap();
			StringTokenizer stk = new StringTokenizer(tmp, ";");
			while(stk.hasMoreTokens())
			{
				String token = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(token, ",");
				if(stk1.hasMoreTokens())
				{
					String mode = stk1.nextToken();
					String chrg = null;
					if(stk1.hasMoreTokens())
						chrg = stk1.nextToken();
					if(mode != null && chrg != null)
						m_modeSubClassMap.put(mode, chrg);
				}
			}
		}
	}
	
	/**
	 * //TODO This is not proper implementation. Change this. check
	 * JIRA-RBT-6528
	 */
	protected String checkChristmasPeriod(String subscriptionClass,
			WebServiceContext task, Subscriber subscriber, Category category,
			Clip clip) {
		// Implementation for Christmas promotions.
		String christmasPeriod = parametersCacheManager.getParameterValue(
				"COMMON", "CHRISTMAS_PERIOD", null);
		
		
		String userStatusMegaPromo = parametersCacheManager.getParameterValue(
				"COMMON", "USER_STATUS_MEGA_PROMO", null);
		
		String megaPromoMode = parametersCacheManager.getParameterValue(
				"COMMON", "MEGA_PROMO_MODES", null);
		
		List<String> megaPromoModeList = null;
		if(megaPromoMode != null) {
			megaPromoModeList = Arrays.asList(megaPromoMode.split(","));
		}
		
		logger.debug("christmasPeriod: " + christmasPeriod);
		if (null != christmasPeriod) {
			String[] times = christmasPeriod.split("-");
			if (times.length == 2) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
					Date stDate = sdf.parse(times[0]);
					Date endDate = sdf.parse(times[1]);

					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);

					Date cdate = c.getTime();

					if ((cdate.after(stDate) || cdate.equals(stDate))
							&& (cdate.before(endDate) || cdate.equals(endDate))) {
						
						if(userStatusMegaPromo != null && userStatusMegaPromo.equalsIgnoreCase("INACTIVE") && !task.containsKey("megaPromo_newuser")) {
							logger.debug("USER_STATUS_MEGA_PROMO configured " + userStatusMegaPromo + " but user is already active. srvKey: " + subscriptionClass);
							return subscriptionClass;
						}
						
						String mode = task.getString(param_mode);
						
						if(mode == null) {
							mode = "VP";
						}
						if(megaPromoModeList != null && !megaPromoModeList.contains(mode.toUpperCase())) {
							logger.debug(mode + " not configured in MEGA_PROMO_MODES. srvKey: " + subscriptionClass);
							return subscriptionClass;
						}
						
						String christmasSubClass = parametersCacheManager
								.getParameterValue("COMMON",
										"CHRISTMAS_SUB_CLASS", null);

						if (null == subscriptionClass) {
							if (clip != null) {
								ChargeClass nextChargeClass = DataUtils
										.getNextChargeClassForSubscriber(task,
												subscriber, category, clip);
								nextChargeClass = (nextChargeClass == null) ? CacheManagerUtil
										.getChargeClassCacheManager()
										.getChargeClass("DEFAULT")
										: nextChargeClass;

								subscriptionClass = nextChargeClass
										.getChargeClass();
							} else {
								subscriptionClass = "DEFAULT"
										.concat(christmasSubClass);
							}
						} else {
							subscriptionClass = subscriptionClass
									.concat(christmasSubClass);
						}

						logger.debug("subscriptionClass: " + subscriptionClass);
					}
				} catch (ParseException pe) {
					logger.error(
							"Unable to parse  CHRISTMAS_PERIOD. pe: "
									+ pe.getMessage(), pe);
				}
			} else {
				logger.debug("Wrong configuration for CHRISTMAS_PERIOD");
			}
		}
		logger.debug("final subscriptionClass: " + subscriptionClass);
		return subscriptionClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#updateSubscription
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String updateSubscription(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = null;
		logger.info("--> updateSubscription");
		try {
			subscriberID = task.getString(param_subscriberID);

			boolean updated = false;
			String info = task.getString(param_info);
			if (info != null && info.equalsIgnoreCase(TNB_TO_NORMAL)) {
				String subClass = "ZERO";
				int subscriptionPeriod = 40;
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				if (subscriber != null)
					subClass = subscriber.subscriptionClass();

				String strTNBNewFlow = getParamAsString("DAEMON",
						ConstantsTools.SUPPORT_TNB_NEW_FLOW, "FALSE");
				boolean isTNBNewFlowSupport = Boolean
						.parseBoolean(strTNBNewFlow);

				if (!isTNBNewFlowSupport) {

					SubscriptionClass subscriptionClass = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(subClass);
					if (subscriptionClass != null)
						subscriptionPeriod = subscriptionClass
								.getSubscriptionPeriodInDays();

					boolean useSubManager = true;
					updated = rbtDBManager.updateTNBSubscribertoNormal(
							subscriberID, useSubManager, subscriptionPeriod);
					// if(rbtDBManager.isTnbReminderEnabled(subClass))
					// rbtDBManager.deleteTNBSubscriber(subscriberID);
				} else {
					ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools
							.getParameter("COMMON",
									"TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO",
									",");
					String newSubClassType = null;
					for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
						String[] split = tnbUpgradeSubClass.split("\\:");
						if (split == null || split.length != 2) {
							continue;
						}
						if (subClass.equalsIgnoreCase(split[0])) {
							newSubClassType = split[1];
							break;
						}
					}
					if(null != newSubClassType) {
						updated = rbtDBManager.convertSubscriptionType(
								subscriber.subID(), subClass, newSubClassType,
								subscriber);
					} else {
						logger.warn("Not updating subscriber, no mapping TNB subscription class is found. subscriberId: "
								+ subscriberID + ", subClass: " + subClass
								+ ", newSubClassType: " + newSubClassType);
					}
				}
				boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
				if (rbtDBManager.isTnbReminderEnabled(subClass) || is121TnbEnabled)
					rbtDBManager.deleteTNBSubscriber(subscriberID);
			} else if (info != null && info.equalsIgnoreCase(UPGRADE_VALIDITY)) {
				logger.info("Processing upgrade_validity request. ");
				Parameters param = parametersCacheManager.getParameter(
						iRBTConstant.COMMON,
						"ALLOW_VALIDITY_EXTENSION_FOR_NEW_USER", "TRUE");

				if (!param.getValue().equalsIgnoreCase("TRUE")) {
					logger.info("parameter ALLOW_VALIDITY_EXTENSION_FOR_NEW_USER is false");
					Subscriber subscriber = DataUtils.getSubscriber(task);
					if (rbtDBManager.isSubscriberDeactivated(subscriber)) {
						logger.info("Returning new_user, validity Extension is not supported for New User");
						return NEW_USER;
					}
				}
				logger.info("parameter ALLOW_VALIDITY_EXTENSION_FOR_NEW_USER is true");
				response = processActivation(task);
				if (!response.equalsIgnoreCase(SUCCESS)
						&& !Utility.isUserActive(response)) {
					logger.info("Returning response: " + response
							+ ", user is not active");
					return response;
				}
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser)
								.equalsIgnoreCase(YES)) {
					logger.info("Returning response: " + response
							+ ", user is active, but ignore user");
					return response;
				}

				if (!task.containsKey(param_activatedNow)) {
					Subscriber subscriber = (Subscriber) task
							.get(param_subscriber);
					response = Utility.upgradeSubscriptionValidity(task,
							subscriber);
					logger.info("upgradeSubscriptionValidity reponse: "
							+ response);
					/*
					 * The response for validity extension is success, it needs
					 * to update the subscription end date if the subscriber is
					 * a TNB subscriber.
					 */
					boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
					if (response.equalsIgnoreCase(SUCCESS)
							&& rbtDBManager.isTnbReminderEnabled(subscriber) && !is121TnbEnabled) {
						logger.info("Since upgradeSubscriptionValidity is Success,"
								+ " updating end date of subscriber: "
								+ subscriber.subID());
						String subClass = subscriber.subscriptionClass();
						SubscriptionClass subscriptionClass = CacheManagerUtil
								.getSubscriptionClassCacheManager()
								.getSubscriptionClass(subClass);
						logger.debug(" Subscription class : "
								+ subscriptionClass);
						Date currentDate = new Date();
						int subscriptionPeriod = subscriptionClass
								.getSubscriptionPeriodInDays();
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(currentDate);
						calendar.add(Calendar.DAY_OF_YEAR, subscriptionPeriod);
						Date updatedDate = calendar.getTime();
						logger.info("Subscriber renewal date: " + currentDate
								+ ", subscriptionPeriod: " + subscriptionPeriod
								+ " is extending to end date: " + updatedDate);
						boolean updatedSubscriber = rbtDBManager
								.updateSubscriber(subscriberID, null, null,
										null, updatedDate, null, null, null,
										null, null, null, null, null, null,
										null, null, null, null, null, null,
										null, null, null);

						logger.info(" Subcriber: " + subscriberID
								+ " base endDate update status: "
								+ updatedSubscriber);
					}
				}

				logger.info("Returning response: " + response+" for upgrade_validity");
				return response;
			} else if (info != null && info.equalsIgnoreCase(CONFIRM_CHARGE)) {
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				response = ERROR;
				if (subscriber != null) {
					if (getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
						RBTSMClientResponse rbtSMClientResponse = RBTSMClientHandler
								.getInstance().confirmSubscription(
										subscriberID, subscriber.prepaidYes(),
										subscriber.subscriptionClass(),
										subscriber.activatedBy());

						if (rbtSMClientResponse.getResponse().equalsIgnoreCase(
								RBTSMClientResponse.SUCCESS))
							response = SUCCESS;
						else if (rbtSMClientResponse.getResponse().equals(
								RBTSMClientResponse.FAILURE))
							response = FAILED;
					} else {
						String mode = subscriber.activatedBy();
						if (task.containsKey(param_mode))
							mode = task.getString(param_mode);
						response = makeConfirmSubscriptionHitToSM(subscriberID,
								mode);
					}
				} else
					logger.info("received confirmCharge request for invalid subscriber "
							+ subscriberID);

				logger.info("response: " + response);
				// added by Sreekar to remove offer id in the extra info so that
				// ACWM user will be normal user once he opts in
				if (response.equals(SUCCESS)) {
					HashMap<String, String> extraInfoMap = rbtDBManager
							.getExtraInfoMap(subscriber);
					Parameters param = parametersCacheManager.getParameter(
							iRBTConstant.COMMON, iRBTConstant.ACWM_OFFER_ID,
							"-100");
					if (extraInfoMap != null
							&& extraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_OFFER_ID)
							&& extraInfoMap.get(
									iRBTConstant.EXTRA_INFO_OFFER_ID).equals(
									param.getValue())) {
						extraInfoMap.remove(iRBTConstant.EXTRA_INFO_OFFER_ID);
						rbtDBManager.updateExtraInfo(subscriberID,
								DBUtility.getAttributeXMLFromMap(extraInfoMap));
					}
				}
				logger.info("Returning response: " + response+" for confirm_charge");
				return response;
			} else if (info == null) {

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");

				String activatedBy = task.getString(param_mode);
				String deactivatedBy = null;
				Date startDate = null;
				Date endDate = task.containsKey(param_subscriberEndDate) ? dateFormat
						.parse(task.getString(param_subscriberEndDate)) : null;
				String prepaidYes = task.getString(param_isPrepaid);
				Date lastAccessDate = task.containsKey(param_lastAccessDate) ? dateFormat
						.parse(task.getString(param_lastAccessDate)) : null;
				Date nextChargingDate = null;
				Integer noOfAccess = null;
				String activationInfo = task.getString(param_modeInfo);
				String subscriptionClass = task
						.getString(param_subscriptionClass);
				String subscriptionYes = null;
				String lastDeactivationInfo = null;
				Date lastDeactivationDate = null;
				Date activationDate = null;
				Integer maxSelections = null;
				String cosID = null;
				String activatedCosID = null;
				String oldClassType = null;
				Integer rbtType = task.containsKey(param_rbtType) ? new Integer(
						task.getString(param_rbtType)) : null;
				String language = null;
				String playerStatus = task.getString(param_playerStatus);
				HashMap<String, String> extraInfo = getUserInfoMap(task);
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				String subStatus = subscriber.subYes();

				/*
				 * If the subscriber status is D, P, F, x and X, it should not
				 * update the DB.
				 */
				if (!"D".equals(subStatus) && !"P".equals(subStatus)
						&& !"F".equals(subStatus) && !"x".equals(subStatus)
						&& !"X".equals(subStatus)) {
					Parameters modeIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "MODE_IP_MAPPING_FOR_CONSENT", null);
					if(modeIPParam != null){
						activatedBy = null;
					}
					updated = rbtDBManager.updateSubscriber(subscriberID,
							activatedBy, deactivatedBy, startDate, endDate,
							prepaidYes, lastAccessDate, nextChargingDate,
							noOfAccess, activationInfo, subscriptionClass,
							subscriptionYes, lastDeactivationInfo,
							lastDeactivationDate, activationDate,
							maxSelections, cosID, activatedCosID, oldClassType,
							rbtType, language, playerStatus, extraInfo);
				} else {
					logger.warn("Failed to update subscriber, "
							+ "since subscriber status is: " + subStatus);
				}

			}
			logger.info("Updated subscriber. isUpdated: " + updated
					+ ", subscriberID: " + subscriberID);

			if (updated)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Returning response: " + response + ", subscriberID: "
				+ subscriberID);
		return response;
	}
  
	public String processRRBTConsentDeactivation(WebServiceContext webServiceContext){
		logger.info("Received processRRBTConsentDeactivation Request");
		String response = ERROR;
		try{
			String subscriberID = webServiceContext
					 .getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			if(subscriber == null){
				return INVALID_RRBT_CONSENT_DEACT_REQ;
			}else{
				String action = webServiceContext.getString(param_action);
            	String xtraInfo = subscriber.extraInfo();
            	HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
            	if(xtraInfoMap == null || (action.equalsIgnoreCase(action_rrbt_consent_deactivate)  && !xtraInfoMap.containsKey("rrbt_type")) || (action.equalsIgnoreCase(action_rrbt_consent_suspension_deactivate) && !xtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_RRBT_TYPE_SUSPENSION_FLAG))){
            		 return INVALID_RRBT_CONSENT_DEACT_REQ;
            	}
//            	xtraInfoMap.remove("rrbt_type");
//            	String extraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
//            	boolean isRRBTDeactivated = rbtDBManager.updateExtraInfoAndPlayerStatus(subscriberID, extraInfo, "A");
//            	if(isRRBTDeactivated)
//            	     response = SUCCESS;
            	response = processDeactivation(webServiceContext);
            	logger.info("Response from processDeactivation in processRRBTConsentDeactivation== "+response);
            }
		}catch(Exception ex){
			logger.info("Exception while processing RRBT Consent Deactivation");
			ex.printStackTrace();
		}
		return response;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processDeactivation
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processDeactivation(WebServiceContext webServiceContext) {
		String response = ERROR;
		logger.info("Received processDeactivation Request");
		try {
			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);

			boolean isDirectDeactivation = false;
			//RBT-13415 - Nicaragua Churn Management.
			boolean isDelayDct = false;
			//Added for VF Greece, delayed deactivation support for different the UI's
			//All the UI's should send this param explicitly to process delayed deactivation.
			boolean isDelayDctUI = false;
			
			if (webServiceContext.containsKey(param_isDirectDeactivation)
					&& webServiceContext.getString(param_isDirectDeactivation)
							.equalsIgnoreCase(YES))
				isDirectDeactivation = true;
			//RBT-13415 - Nicaragua Churn Management.
			if (webServiceContext.containsKey(param_delayDct)
					&& webServiceContext.getString(param_delayDct)
							.equalsIgnoreCase("true"))
				isDelayDct = true;
			//Added for VF Greece, delayed deactivation support for different the UI's
			//All the UI's should send this param explicitly to process delayed deactivation.
			if (webServiceContext.containsKey(param_delayDct_UI)
					&& webServiceContext.getString(param_delayDct_UI)
							.equalsIgnoreCase("true"))
				isDelayDctUI = true;
						
			boolean isActPendingDeactAllowed = RBTParametersUtils
					.getParamAsBoolean("COMMON",
							"IS_DEACT_ALLOWED_FOR_ACT_PENDING", "FALSE");

			String status = USER_NOT_EXISTS;
			if (subscriber != null)
				status = Utility.getSubscriberStatus(subscriber);
			if (subscriber == null || status.equalsIgnoreCase(DEACTIVE)) {
				logger.info("response: " + status);
				writeEventLog(subscriberID, getMode(webServiceContext), "303",
						UNSUBSCRIPTION, null);
				return status;
			} else if (!isDirectDeactivation) {
				// If request is Direct Deactivation then user will be allowed
				// to deactivate directly

				if (status.equalsIgnoreCase(GRACE)) {
					Parameters parameter = parametersCacheManager.getParameter(
							iRBTConstant.COMMON,
							"ALLOW_DEACTIVATION_FOR_GRACE_USERS", "TRUE");
					boolean allowDeactivationForGraceUsers = parameter
							.getValue().equalsIgnoreCase("TRUE");
					if (!allowDeactivationForGraceUsers) {
						logger.info("response: " + NOT_ALLOWED_FOR_GRACE_USER);
						writeEventLog(subscriberID, getMode(webServiceContext),
								"301", UNSUBSCRIPTION, null);
						return NOT_ALLOWED_FOR_GRACE_USER;
					}
				} else if ((status.equalsIgnoreCase(ACT_PENDING) && !isActPendingDeactAllowed)
						|| status.equalsIgnoreCase(DEACT_PENDING)) {
					logger.info("response: " + status);
					writeEventLog(subscriberID, getMode(webServiceContext),
							"301", UNSUBSCRIPTION, null);
					return status;
				}
			}

			String deactivatedBy = getMode(webServiceContext);
			String deactivationInfo = webServiceContext
					.getString(param_modeInfo);

			SubscriptionClass subscriptionClass = CacheManagerUtil
					.getSubscriptionClassCacheManager().getSubscriptionClass(
							subscriber.subscriptionClass());
			if (subscriptionClass != null
					&& subscriptionClass.getOperatorCode2() != null
					&& subscriptionClass.getOperatorCode2().length() > 0) {
				String opCode = subscriptionClass.getOperatorCode2();
				String[] tokens = opCode.split(",");
				Clip clip = null;
				try {
					clip = rbtCacheManager.getClip(tokens[0].trim());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				if (clip != null) {
					String deactModesStr = RBTParametersUtils.getParamAsString(
							iRBTConstant.COMMON,
							"FOOTBALL_COMBO_DEACT_ALLOWED_MODES", null);
					String[] deactModes = null;
					if (deactModesStr != null)
						deactModes = deactModesStr.split(",");

					List<String> deactModesList = null;
					if (deactModes != null)
						deactModesList = Arrays.asList(deactModes);

					if (deactModesList != null
							&& !deactModesList.contains(deactivatedBy))
						return NOT_ALLOWED;
				}
			}

			if (subscriber != null
					&& (getParamAsBoolean(iRBTConstant.COMMON,
							"DELAY_DEACT_ON_DEACTIVATION", "FALSE") || isDelayDctUI)) {
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (subExtraInfoMap == null)
					subExtraInfoMap = new HashMap<String, String>();
				if (subExtraInfoMap.containsKey("DELAY_DEACT")
						&& subExtraInfoMap.get("DELAY_DEACT").equalsIgnoreCase(
								"TRUE")) {
					logger.info("subExtraInfo contains delay deact so sending back ALREADY_DELAY_DEACT");
					return ALREADY_DELAY_DEACT;
				}

				subExtraInfoMap.put("DELAY_DEACT", "TRUE");
				subExtraInfoMap.put("SUB_YES", subscriber.subYes());
				String extraInfo = DBUtility
						.getAttributeXMLFromMap(subExtraInfoMap);
				response = rbtDBManager.updateExtraInfoNStatusNDeactBy(
						subscriberID, extraInfo, "C", deactivatedBy);
				return response;
			}

			String confDelayTime = getParamAsString(iRBTConstant.COMMON,
					"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION", null);
			boolean immediateDeactivation = false;
			//RBT-13415 - Nicaragua Churn Management.
			if (subscriber != null && (confDelayTime != null || isDelayDct)) {
				String updateUnsubSuccess = "FAILURE";
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (subExtraInfoMap == null)
					subExtraInfoMap = new HashMap<String, String>();

				// To change the configuration name from
				// DEACTIVE_ON_THE_BASIS_OF_NEXT_BILL_DATE to
				// TRAI_UNSUB_DELAY_DEACT
				boolean isTraiDeactConfig = getParamAsBoolean(
						iRBTConstant.COMMON, "TRAI_UNSUB_DELAY_DEACT", "FALSE");
				if (subExtraInfoMap.containsKey("UNSUB_DELAY")
						&& (!webServiceContext
								.containsKey(param_isUnsubDelayDctReq) && !isTraiDeactConfig)) {
					logger.info("isTraiDeactConfig is not configured and ALREADY_UNSUB_DELAY");
					return ALREADY_UNSUB_DELAY;
				}
				logger.info("isTraiDeactConfig : " + isTraiDeactConfig);
				if (isTraiDeactConfig) {

					// Get the next billing date from prism and check with
					// param(COMMON, "ALLOW_DEACTIVATION_BEFORE_N_DAYS", 0);
					String daysAllowed = getParamAsString(iRBTConstant.COMMON,
							"ALLOW_DEACTIVATION_BEFORE_N_DAYS", "0");
					long allowedDays = 0;
					if (daysAllowed != null){
						allowedDays = Integer.parseInt(daysAllowed.trim());
					}
					// if next billing date is less than configured days do the
					// immediate deactivation
					// if next billing date is grater then configured days, then
					// send sms and update the end date syste date +
					// CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION
					// if unsub delay is exist in subscriber extrainfo then do
					// the deactivate immediately

					immediateDeactivation = subExtraInfoMap
							.containsKey("UNSUB_DELAY");
					if (!subExtraInfoMap.containsKey("UNSUB_DELAY")) {

						Map<String, String> nextBillDateMap = com.onmobile.apps.ringbacktones.webservice.common.Utility
								.getNextBillingDateOfServices(webServiceContext);
						String subRefID = subscriber.refID();
						SimpleDateFormat rbtDateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						String nextBillDateString = nextBillDateMap
								.get(subRefID);
						if (nextBillDateString == null) {
							nextBillDateString = nextBillDateMap.get(subscriber
									.subID());
						}
						logger.info("nextBillDateString value :"
								+ nextBillDateString);
						Date nextBillDate = null;
						if (nextBillDateString != null)
							nextBillDate = rbtDateFormat
									.parse(nextBillDateString);

						// if next billing date is less than configured days do
						// the immediate deactivation
						long daysLeft = 0;
						if (nextBillDate != null) {
							daysLeft = (nextBillDate.getTime() - System
									.currentTimeMillis())
									/ (1000 * 24 * 60 * 60);
							logger.info("days left after checking with billing date :"
									+ daysLeft);
						}

						if ((nextBillDate != null && nextBillDate
								.before(new Date()))
								|| (daysLeft < allowedDays)) {
							immediateDeactivation = true;
						} else {
							HashMap<String, String> hashmap = new HashMap<String, String>();
							String language = subscriber.language();
							String daysKeyword = CacheManagerUtil
									.getSmsTextCacheManager().getSmsText(
											"DAYS", language);
							daysKeyword = daysKeyword == null ? "days"
									: daysKeyword;

							String hoursKeyword = CacheManagerUtil
									.getSmsTextCacheManager().getSmsText(
											"HOURS", language);
							hoursKeyword = hoursKeyword == null ? "hours"
									: hoursKeyword;

							if (daysLeft <= 0 && nextBillDate != null) {
								long hoursLeft = (nextBillDate.getTime() - System
										.currentTimeMillis())
										/ (1000 * 60 * 60);
								hashmap.put("DAYS_LEFT", hoursLeft + " "
										+ hoursKeyword);
							} else
								hashmap.put("DAYS_LEFT", daysLeft + " "
										+ daysKeyword);

							hashmap.put(
									"DEACT_CONFIRM_DAYS",
									""
											+ CacheManagerUtil
													.getParametersCacheManager()
													.getParameter(
															SMS,
															Constants.DEACTIVATION_CONFIRM_CLEAR_DAYS,
															String.valueOf(5)));
							
							logger.info("SubscriberId: " + subscriber.subID() + " circleId: " + subscriber.circleID());
							
							String smsText = CacheManagerUtil
									.getSmsTextCacheManager().getSmsText(
											Constants.DEACTIVATION_CONFIRM +"_" +subscriber.circleID() ,
											language);
							
							if(smsText == null) {
							    smsText = CacheManagerUtil
									.getSmsTextCacheManager().getSmsText(
											Constants.DEACTIVATION_CONFIRM,
											language);
							}
							smsText = smsText == null ? Constants.m_deactivationConfirmTextDefault
									: smsText;

							hashmap.put("SMS_TEXT", smsText);
							hashmap.put("CIRCLE_ID", subscriber.circleID());
							smsText = SmsProcessor.finalizeSmsText(hashmap);

							long extendedDate = System.currentTimeMillis();
							extendedDate += Double.parseDouble(confDelayTime) * 60 * 1000L;
							logger.info("extendedDate :"
									+ new Date(extendedDate));

							subExtraInfoMap.put("UNSUB_DELAY",
									getMode(webServiceContext));
							String extraInfo = DBUtility
									.getAttributeXMLFromMap(subExtraInfoMap);

							updateUnsubSuccess = rbtDBManager
									.updateEndDateAndExtraInfo(subscriberID,
											new Date(extendedDate), extraInfo);

							String senderID = RBTParametersUtils
									.getParamAsString(iRBTConstant.WEBSERVICE,
											"ACK_CHURN_SMS_SENDER_NO", null);
							if (senderID == null) {
								logger.info("SENDER_NO is not configured, so not sending the SMS");
								return FAILURE;
							}

							boolean sendSMSResponse = false;
							try {
								sendSMSResponse = Tools.sendSMS(senderID,
										subscriberID, smsText, false);
							} catch (OnMobileException e) {
								logger.error(e.getMessage(), e);
							}

							if (sendSMSResponse) {
								return SUCCESS;
							} else {
								return FAILURE;
							}

						}

					}
				}
				logger.info("immediateDeactivation :" + immediateDeactivation);
				if (!subExtraInfoMap.containsKey("UNSUB_DELAY")
						&& !immediateDeactivation) {
					// RBT-13415 - Nicaragua Churn Management.
					subExtraInfoMap.put("UNSUB_DELAY",
							getMode(webServiceContext));
					String extraInfo = DBUtility
							.getAttributeXMLFromMap(subExtraInfoMap);
					long endDate = System.currentTimeMillis();
					try {
						//RBT-13415 - Nicaragua Churn Management.
						if (null == confDelayTime) {
							confDelayTime = "30";
						}
						String unSubDelayTime = getParamAsString(
								iRBTConstant.COMMON,
								"UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
								confDelayTime);
						endDate += Double.parseDouble(unSubDelayTime) * 60 * 1000L;
					} catch (NumberFormatException ex) {
						logger.info("Error in Parsing the Configured time for UNSUB_DELAY ON DEACTIVATION");
						return updateUnsubSuccess;
					}
					Date date = new Date(endDate);
					boolean isSMSChurnAllowed = getParamAsString(
							iRBTConstant.SMS,
							"SMS_CHURN_PORTAL_USING_UNSUB_DELAY", "FALSE")
							.trim().equalsIgnoreCase("TRUE");
					logger.info("isSMSChurnAllowed = " + isSMSChurnAllowed
							+ "confDelayTime = " + confDelayTime);
					if (isSMSChurnAllowed) {
						String biUrl = getParamAsString(iRBTConstant.COMMON,
								"CHURN_BI_OFFER_URL", null);
						if (biUrl != null) {
							String subClass = webServiceContext
									.getString(param_subscriptionClass);
							biUrl = biUrl.replaceAll("%SUB_CLASS%", subClass);
							biUrl = biUrl.replaceAll("%MSISDN%",
									subscriber.subID());
							biUrl = biUrl.replaceAll("%CIRCLE_ID%",
									subscriber.circleID());
						}
						HttpParameters httpParameters = new HttpParameters(
								biUrl);
						HashMap<String, String> requestParams = null;
						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, requestParams);
						logger.info("BI HIT FOR CHURN httpParameters= "
								+ httpParameters + "RESPONSE = "
								+ httpResponse.getResponse());
						if (httpResponse.getResponseCode() == 200) {
							String offerResponse = httpResponse.getResponse();
							if (offerResponse != null
									&& (offerResponse.indexOf(",") != -1 || offerResponse
											.split(",").length == 1)) {
								String offer[] = offerResponse.split(",");
								String churnOfferFromBI = "";
								String churnOffer = "";
								for (int i = 0; (offer != null && i < offer.length); i++) {
									if (i == 3)
										break;
									SubscriptionClass subClass = CacheManagerUtil
											.getSubscriptionClassCacheManager()
											.getSubscriptionClass(offer[i]);
									logger.info("Subscription Class = "
											+ subClass);
									if (subClass != null) {
										churnOfferFromBI += (i + 1) + "."
												+ offer[i] + ",";
										churnOffer += offer[i] + ",";
									}
								}
								if (churnOffer != null
										&& !churnOffer.equals("")) {
									churnOfferFromBI = churnOfferFromBI
											.substring(0, churnOfferFromBI
													.lastIndexOf(","));
									churnOffer = churnOffer.substring(0,
											churnOffer.lastIndexOf(","));
								}
								logger.info("Offer Obtained from BI HIT = "
										+ churnOffer);
								if (churnOffer != null
										&& !churnOffer.equals("")) {
									webServiceContext.put(
											param_churnOfferFromBI,
											churnOfferFromBI);
									webServiceContext.put(param_churnOffer,
											churnOffer);
								}
							}
						}
					}
					if (!isSMSChurnAllowed
							|| webServiceContext.containsKey(param_churnOffer)) {
						updateUnsubSuccess = rbtDBManager
								.updateEndDateAndExtraInfo(subscriberID, date,
										extraInfo);
						if (isDelayDct) {//RBT-13415 - Nicaragua Churn Management.
							subscriber = RBTDBManager.getInstance()
									.getSubscriber(subscriberID, true);
							webServiceContext.put(param_subscriber, subscriber);
						}
					}
					if ((updateUnsubSuccess != null && updateUnsubSuccess
							.equalsIgnoreCase("SUCCESS"))) {

						boolean sendSMS = sendAcknowledgementSMS(
								webServiceContext, "UNSUB_DELAY");
						if (webServiceContext.containsKey(param_churnOffer)) {
							String callerID = webServiceContext
									.getString(param_callerID);
							rbtDBManager.insertViralSMSTable(subscriberID,
									new Date(), "SMS_CHURN_OFFER", callerID,
									webServiceContext
											.getString(param_churnOffer), 1,
									"SMS", null, null);
						}
						return updateUnsubSuccess;
					}
				}
			}

			Set<String> corpRbtFileSet = new HashSet<String>();
			Set<String> nonCorpRbtFileSet = new HashSet<String>();

			SubscriberStatus[] settings = rbtDBManager
					.getAllActiveSubscriberSettings(subscriberID);
			if (settings != null) {
				for (SubscriberStatus setting : settings) {
					if (setting.selType() == 2)
						corpRbtFileSet.add(setting.subscriberFile());
					else
						nonCorpRbtFileSet.add(setting.subscriberFile());
				}
			}
			String browsingLanguage = webServiceContext
					.getString(param_browsingLanguage);
			boolean deactivateCorporateUser = webServiceContext
					.containsKey(param_isDeactivateCorporateUser)
					&& webServiceContext.getString(
							param_isDeactivateCorporateUser).equalsIgnoreCase(
							YES);
			boolean isCorporateUser = corpRbtFileSet.size() != 0;
			boolean isUserHasNonCorpSelection = nonCorpRbtFileSet.size() != 0;
			logger.info("deactivateCorporateUser = " + deactivateCorporateUser
					+ " isCorporateUser " + isCorporateUser
					+ " isUserHasNonCorpSelection " + isUserHasNonCorpSelection
					+ " corpRbtFileSet " + corpRbtFileSet
					+ " nonCorpRbtFileSet" + nonCorpRbtFileSet);

			Parameters delelectionsOnDeactParam = parametersCacheManager
					.getParameter(iRBTConstant.COMMON,
							"DEL_SELECTION_ON_DEACT", "TRUE");
			boolean delelectionsOnDeact = delelectionsOnDeactParam.getValue()
					.trim().equalsIgnoreCase("TRUE");

			if (isCorporateUser && !isDirectDeactivation
					&& !deactivateCorporateUser) {
				boolean isCorporateDeactivation = webServiceContext
						.containsKey(param_isCorporateDeactivation)
						&& webServiceContext.getString(
								param_isCorporateDeactivation)
								.equalsIgnoreCase(YES);

				if (isCorporateDeactivation) {
					// Corporate initiated deactivation.
					if (isUserHasNonCorpSelection) {
						// If user is corporate user and having non-corporate
						// selections,
						// then all corporate selections/downloads will be
						// removed.
						// otherwise user will be deactivated from the service.
						response = deleteSettingsInSet(corpRbtFileSet,
								subscriberID, deactivatedBy, browsingLanguage,
								"2");
						logger.info("response: " + response);
						return response;
					}
				} else {
					// User initiated deactivation.
					if (isUserHasNonCorpSelection) {
						// If user is corporate user and having non-corporate
						// selections,
						// then non-corporate selections/downloads will be
						// removed.
						response = deleteSettingsInSet(nonCorpRbtFileSet,
								subscriberID, deactivatedBy, browsingLanguage,
								null);
						logger.info("response: " + response);
						return response;
					} else {
						// If user is corporate user and not having any non
						// corporate selection, then no need to do anything.
						logger.info("response: " + SUCCESS);
						return SUCCESS;
					}
				}
			}

			boolean useSubManager = true;

			boolean checkSubscriptionClass = true;
			if (webServiceContext.containsKey(param_checkSubscriptionClass))
				checkSubscriptionClass = webServiceContext.getString(
						param_checkSubscriptionClass).equalsIgnoreCase(YES);

			String dctResponse = null;
			if (getParamAsBoolean(iRBTConstant.COMMON,
					iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
				boolean isBulkTask = webServiceContext
						.containsKey(param_fromBulkTask);

				HashMap<String, String> extraParams = new HashMap<String, String>();
				extraParams.put(
						RBTSMClientHandler.EXTRA_PARAM_USERINFO,
						getDeactivationUserInfo(subscriber.activationInfo(),
								subscriber.cosID()));

				StringBuilder builder = new StringBuilder(
						"SM client request for Deactive Subscriber ");
				builder.append("[ subID " + subscriberID);
				builder.append(", prepaidYes " + subscriber.prepaidYes());
				builder.append(", Old subClass "
						+ subscriber.subscriptionClass());
				builder.append(", deactivatedBy - " + deactivatedBy);
				builder.append(", extraParams - " + extraParams);
				builder.append(", isBulkTask - " + isBulkTask);
				logger.info(builder.toString());

				RBTSMClientResponse smClientResponse = RBTSMClientHandler
						.getInstance().deactivateSubscriber(subscriberID,
								subscriber.prepaidYes(), deactivatedBy,
								subscriber.subscriptionClass(), extraParams,
								isBulkTask);

				logger.info("SMClient Response for DeactivateSubscriber"
						+ smClientResponse.toString());
				
				dctResponse = smClientResponse.getResponse();
				if (dctResponse.equalsIgnoreCase(RBTSMClientResponse.SUCCESS)) {
					dctResponse = rbtDBManager.smDeactivateSubscriber(
							subscriberID, deactivatedBy, null,
							delelectionsOnDeact, true, useSubManager,
							isDirectDeactivation, checkSubscriptionClass,
							subscriber.rbtType(), subscriber, deactivationInfo);
				}
			} else {
				// Get UserInofMap from Task
				Map<String, String> userInfoMap = getUserInfoMap(webServiceContext);

				String extraInfoXML = null;
				// RBT-14185,RBT-14089- Vodafone In:-Only activation in promotion.jsp is inserting
				// record into rbt_subscriber table DB directly
				Map<String, String> subscriberExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (subscriberExtraInfoMap != null) {
					if (subscriberExtraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						subscriberExtraInfoMap
								.remove(iRBTConstant.EXTRA_INFO_TPCGID);
					}
					if (subscriberExtraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						subscriberExtraInfoMap
								.remove(iRBTConstant.EXTRA_INFO_TRANS_ID);
					}
				}
				if (userInfoMap != null && userInfoMap.size() > 0) {
					if (subscriberExtraInfoMap != null)
						subscriberExtraInfoMap.putAll(userInfoMap);
					else
						subscriberExtraInfoMap = userInfoMap;
				}
				//Removed the TPCGID if the user is deactivating.
				extraInfoXML = DBUtility
						.getAttributeXMLFromMap(subscriberExtraInfoMap);

				// RBT-10785
				boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
						"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
				if(addProtocolNumber) {
					deactivationInfo = appendProtocolNumber(subscriberID, deactivationInfo);
				}
				
				dctResponse = rbtDBManager.deactivateSubscriber(subscriberID,
						deactivatedBy, null, delelectionsOnDeact, true,
						useSubManager, isDirectDeactivation,
						checkSubscriptionClass, subscriber.rbtType(),
						subscriber, deactivationInfo, extraInfoXML);
			}

			if (dctResponse.equalsIgnoreCase(SUCCESS)) {
				subscriber = RBTDBManager.getInstance().getSubscriber(
						subscriberID, true);
				webServiceContext.put(param_subscriber, subscriber);

				rbtDBManager.removeViralBlackList(subscriberID, "CHURN_OFFER");
				// Added by Sandeep. Deactivating the pack.
				if (getParamAsBoolean("COMMON", "PACK_DEACT_ON_BASE_DEACT", "TRUE")) {
					HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
					packExtraInfoMap.put(
							iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
							deactivatedBy);
					packExtraInfoMap.put(
							iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO,
							deactivationInfo);
					packExtraInfoMap.put(
							iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
							new Date().toString());
					rbtDBManager.deactivateAllPack(subscriber, packExtraInfoMap);
				}
				if (!delelectionsOnDeact && isCorporateUser) {
					SubscriberStatus[] activeSettings = rbtDBManager
							.getAllActiveSubscriberSettings(subscriberID);
					if (activeSettings != null && activeSettings.length > 0) {
						for (SubscriberStatus setting : activeSettings) {
							if (setting.selType() == iRBTConstant.TYPE_CORPORATE) {
								if (RBTParametersUtils.getParamAsBoolean(
										iRBTConstant.COMMON,
										"ADD_TO_DOWNLOADS", "FALSE"))
									rbtDBManager.expireSubscriberDownload(
											subscriberID,
											setting.subscriberFile(), -1,
											iRBTConstant.SONGS, deactivatedBy,
											null, false);
								else
									rbtDBManager
											.deactivateSubscriberRecordsByRefId(
													subscriberID,
													deactivatedBy,
													setting.refID());
							}
						}
					}
				}

				rbtDBManager.updateConsentStatusOfConsentRecordBySubscriberId(
						subscriberID, "4");
				
				rbtDBManager.deactivateActiveSubscriberDownloads(subscriberID);
			}

			response = Utility.getResponseString(dctResponse);
			if (dctResponse.equalsIgnoreCase(SUCCESS)) {
				sendAcknowledgementSMS(webServiceContext, "DEACTIVATION");
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
	//RBT-13415 - Nicaragua Churn Management.
	@Override
	public String processRejectDelayDeactivation(
			WebServiceContext webServiceContext) {
		logger.info("Process Reject Delay Deactivation");
		String subscriberID = webServiceContext.getString(param_subscriberID);
		String response = INVALID;
		Subscriber subscriber;
		try {
			subscriber = DataUtils.getSubscriber(webServiceContext);
			String status = Utility.getSubscriberStatus(subscriber);
			String extraInfo = (subscriber != null) ? subscriber.extraInfo()
					: null;
			HashMap<String, String> userInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			Date endDate= subscriber.endDate();
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse("31/12/2037");
			if (null == endDate || endDate.before(new Date())) {
				return response;
			}
			if (userInfoMap != null && userInfoMap.containsKey("UNSUB_DELAY")) {
				userInfoMap.remove("UNSUB_DELAY");
			} else {
				return status;
			}
			extraInfo = DBUtility.getAttributeXMLFromMap(userInfoMap);
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			response = rbtDBManager.updateEndDateAndExtraInfo(
					subscriberID, date, extraInfo);
			subscriber = RBTDBManager.getInstance().getSubscriber(
					subscriberID, true);
			webServiceContext.put(param_subscriber, subscriber);
		} catch (RBTException e1) {
			logger.error("", e1);
			response = ERROR;
		} catch (ParseException e) {
			logger.error("", e);
			response = ERROR;
		}
		logger.info("response: " + response);
		return response;
	}
	
	private String deleteSettingsInSet(Set<String> selectionsSet,
			String subscriberID, String deactivatedBy, String browsingLanguage,
			String selType) {
		return deleteSettingsInSet(selectionsSet, subscriberID, deactivatedBy,
				browsingLanguage, selType, null);
	}

	protected String deleteSettingsInSet(Set<String> selectionsSet,
			String subscriberID, String deactivatedBy, String browsingLanguage,
			String selType, String status) {
		// Sel Type will be null in case of user initiated , 2 in case of
		// corporate initiated

		String response = ERROR;

		WebServiceContext tempTask = new WebServiceContext();
		tempTask.put(param_subscriberID, subscriberID);
		tempTask.put(param_mode, deactivatedBy);
		tempTask.put(param_action, action_deleteSetting);
		if (selType != null) {
			tempTask.put(param_selectionType, selType);
			response = deleteSetting(tempTask);
			if (!response.equals(SUCCESS))
				return response;

		}
		// Added by Sandeep for Profile Pack deactivation
		else if (status != null) {
			tempTask.put(param_status, status);
			response = deleteSetting(tempTask);
		} else {
			for (String rbtFile : selectionsSet) {
				tempTask.put(param_rbtFile, rbtFile);

				response = deleteSetting(tempTask);
				if (!response.equals(SUCCESS))
					return response;
			}
		}

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE");
		boolean isDownloadsModel = parameter.getValue()
				.equalsIgnoreCase("TRUE");

		if (isDownloadsModel) {
			HashMap<String, Integer> downloadCategoryMap = new HashMap<String, Integer>();
			SubscriberDownloads[] downloads = rbtDBManager
					.getActiveSubscriberDownloads(subscriberID);
			if (downloads != null) {
				for (SubscriberDownloads download : downloads) {
					downloadCategoryMap.put(download.promoId(),
							download.categoryID());
				}
			}

			tempTask = new WebServiceContext();
			tempTask.put(param_subscriberID, subscriberID);
			tempTask.put(param_mode, deactivatedBy);
			tempTask.put(param_action, action_deleteTone);

			for (String rbtFile : selectionsSet) {
				if (!downloadCategoryMap.containsKey(rbtFile))
					continue;

				Clip clip = rbtCacheManager.getClipByRbtWavFileName(rbtFile,
						browsingLanguage);
				if (clip == null)
					continue;

				tempTask.put(param_rbtFile, rbtFile);
				tempTask.put(param_categoryID, downloadCategoryMap.get(rbtFile)
						.toString());

				response = deleteTone(tempTask);
				if (!response.equals(SUCCESS))
					return response;
			}
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processSubscriberPromoRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSubscriberPromoRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			if (!com.onmobile.apps.ringbacktones.services.common.Utility
					.isValidNumber(subscriberID)) {
				logger.info("Invalid subscriberID. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			String activatedBy = task.getString(param_mode);
			String type = task.getString(param_type);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_addSubscriberPromo)) {
				int freeDays = 0;
				if (task.containsKey(param_freeDays))
					freeDays = Integer.parseInt(task.getString(param_freeDays));

				boolean isPrepaid = DataUtils.isUserPrepaid(task);

				SubscriberPromo subscriberPromo = rbtDBManager
						.createSubscriberPromo(subscriberID, freeDays,
								isPrepaid, activatedBy, type);
				if (subscriberPromo != null) {
					response = SUCCESS;
					task.put(param_subscriberPromo, subscriberPromo);
				} else
					response = FAILED;
			} else if (action.equalsIgnoreCase(action_removeSubscriberPromo)) {
				boolean removed = rbtDBManager.removeSubscriberPromo(
						subscriberID, activatedBy, type);
				if (removed)
					response = SUCCESS;
				else
					response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	public String getCriteria(WebServiceContext task) {
		String criteria = "Default";
		if (task.containsKey(param_callerID)) {
			criteria = "MSISDN";
			if (task.getString(param_callerID) != null
					&& task.getString(param_callerID).toUpperCase()
							.startsWith("G"))
				criteria = "Group";
		}

		if (task.containsKey(param_fromTime) || task.containsKey(param_toTime)
				|| task.containsKey(param_toTimeMinutes)
				|| task.containsKey(param_fromTimeMinutes)
				|| task.containsKey(param_profileHours)
				|| task.containsKey(param_interval))
			criteria = "Schedule";

		if (task.containsKey(param_interval)
				&& task.getString(param_interval) != null)
			criteria = "Aniversary";
		return criteria;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processSelection
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext task) {
		String response = ERROR;
		boolean isAnyResponseSuccess = false;
		logger.info("Processing selection for task: "+task);
		String subscriberID = task.getString(param_subscriberID);
		try {
			
			if(task.containsKey(param_chargeClass) 
					&& overrideChargeClassMap.containsKey(task.getString(param_chargeClass))) {
				task.put(param_chargeClass, overrideChargeClassMap.get(task.getString(param_chargeClass)));
			}
			
			if (getParamAsString(iRBTConstant.COMMON,
					"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION", null) != null) {
				Subscriber sub = DataUtils.getSubscriber(task);
				String extraInfo = (sub != null) ? sub.extraInfo() : null;
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null && extraInfoMap.containsKey("UNSUB_DELAY")
						&& sub.endDate().after(new Date())) {
					return NOT_ALLOWED;
				}
			}


			String callerId = (!task.containsKey(param_callerID) || task
					.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
					: task.getString(param_callerID);
			// array of comma seperated caller Ids
			String[] validCallerIds = (callerId != null) ? DataUtils
					.getValidCallerIds(callerId) : null;
			Map<String, String> originalAndTrimmedCallerId = new HashMap<String, String>(); 
			originalAndTrimmedCallerId = getoriginalAndTrimmedCallerIds(callerId);
			if (validCallerIds == null) {
				validCallerIds = new String[] { null };
			} else if (validCallerIds.length == 0) {
				logger.info("Invalid callerID. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			/*
			 * if (callerID != null && !callerID.startsWith("G")) { // callerID
			 * null means for ALL callers and if starts with 'G' // means
			 * groupID.
			 * 
			 * Parameters parameter = parametersCacheManager.getParameter(
			 * iRBTConstant.COMMON, "MINIMUM_CALLER_ID_LENGTH", "7"); int
			 * minCallerIDLength = Integer.parseInt(parameter.getValue());
			 * 
			 * boolean validCallerID = false; if (callerID.length() >=
			 * minCallerIDLength) { try { Long.parseLong(callerID);
			 * validCallerID = true; } catch (NumberFormatException e) { } }
			 * 
			 * if (!validCallerID) {
			 * logger.info("Invalid callerID. Returning response: " +
			 * INVALID_PARAMETER); writeEventLog(subscriberID, getMode(task),
			 * "404", CUSTOMIZATION, getClip(task), getCriteria(task)); return
			 * INVALID_PARAMETER; } }
			 */

			int status = 1;
			int fromHrs = 0;
			int toHrs = 23;
			int fromMinutes = 0;
			int toMinutes = 59;

			// Time based selection
			if (task.containsKey(param_fromTime) || task.containsKey(param_toTime)) {
				status = 80;
			}
			
			if (task.containsKey(param_fromTime))
				fromHrs = Integer.parseInt(task.getString(param_fromTime));
			if (task.containsKey(param_toTime))
				toHrs = Integer.parseInt(task.getString(param_toTime));
			if (task.containsKey(param_toTimeMinutes))

				toMinutes = Integer.parseInt(task
						.getString(param_toTimeMinutes));
			if (task.containsKey(param_fromTimeMinutes))
				fromMinutes = Integer.parseInt(task
						.getString(param_fromTimeMinutes));

			if (fromHrs < 0 || fromHrs > 23 || toHrs < 0 || toHrs > 23
					|| fromMinutes < 0 || fromMinutes > 59 || toMinutes < 0
					|| toMinutes > 59) {
				logger.info("Invalid fromTime or toTime. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			DecimalFormat decimalFormat = new DecimalFormat("00");
			int fromTime = Integer.parseInt(fromHrs
					+ decimalFormat.format(fromMinutes));
			int toTime = Integer.parseInt(toHrs
					+ decimalFormat.format(toMinutes));

			String interval = task.getString(param_interval);
			if (interval != null)
				interval = interval.toUpperCase();
			if (!Utility.isValidSelectionInterval(interval)) {
				logger.warn("Invalid interval. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			// Added By Sandeep for profile selection
			int selType = -1;
			if (task.containsKey(param_selectionType)) {
				String strSelType = task.getString(param_selectionType);
				try {
					selType = Integer.parseInt(strSelType);
				} catch (NumberFormatException ne) {
				}
			}

			Calendar endCal = Calendar.getInstance();
			endCal.set(2037, 0, 1);
			Date endDate = endCal.getTime();
			Date startDate = null;

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			if (task.containsKey(param_selectionStartTime)) {
				String startTimeStr = task.getString(param_selectionStartTime);
				if (startTimeStr.length() != 8 && startTimeStr.length() != 17) {
					logger.info("Invalid selectionStartTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				if (startTimeStr.length() == 8)
					startTimeStr += "000000000";

				startDate = dateFormat.parse(startTimeStr);
			}

			if (task.containsKey(param_selectionEndTime)) {
				String endTimeStr = task.getString(param_selectionEndTime);
				if (endTimeStr.length() != 8 && endTimeStr.length() != 17) {
					logger.info("Invalid selectionEndTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				if (endTimeStr.length() == 8)
					endTimeStr += "235959000";
				else if (endTimeStr.endsWith("000000000"))
					endTimeStr = endTimeStr.substring(0, 8) + "235959000";

				endDate = dateFormat.parse(endTimeStr);
			}

			if (task.containsKey(param_selectionStartTime)
					&& task.containsKey(param_selectionEndTime)) {
				if (startDate != null && startDate.compareTo(endDate) >= 0) {
					logger.info("selectionStartTime is not less than selectionEndTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				// If selectionStartTime & selectionEndTime passed, then
				// selection interval will be ignored.
				interval = null;
			}

			if (!task.containsKey(param_categoryID)
					&& !task.containsKey(param_categoryPromoID)
					&& !task.containsKey(param_categorySmsAlias)) {
				logger.info("categoryID parameter not passed. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			String browsingLanguage = task.getString(param_browsingLanguage);
			Category category = null;
			if (task.containsKey(param_categoryID))
				category = rbtCacheManager.getCategory(
						Integer.parseInt(task.getString(param_categoryID)),
						browsingLanguage);
			else if (task.containsKey(param_categoryPromoID))
				category = rbtCacheManager
						.getCategoryByPromoId(
								task.getString(param_categoryPromoID),
								browsingLanguage);
			else if (task.containsKey(param_categorySmsAlias))
				category = RBTCacheManager.getInstance().getCategoryBySMSAlias(
						task.getString(param_categorySmsAlias));

			// Added for VD-109292
			boolean isShuffleCheck = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.COMMON,
					"ENABLE_SHUFFLE_CIRCLE_MAP_CHECKING_BLOCKED", "FALSE");
			boolean isShuffleCat = Utility.isShuffleCategory(category
					.getCategoryTpe());
			if (isShuffleCheck && isShuffleCat) {
				String circleId = DataUtils.getUserCircle(task);
				String prepaid = DataUtils.isUserPrepaid(task) ? "p" : "b";
				char prepaidS = prepaid.charAt(0);
				boolean categoryCircleMatch = false;
				String catId = task.getString(param_categoryID);
				Category[] categories = RBTCacheManager.getInstance()
						.getCategoryByType(circleId, prepaidS,
								String.valueOf(category.getCategoryTpe()));
				if (categories != null && categories.length > 0)
					for (Category categoryObj : categories) {
						if (Integer.parseInt(catId) == categoryObj
								.getCategoryId()) {
							categoryCircleMatch = true;
							break;
						} else {
							categoryCircleMatch = false;
						}
					}
				if (!categoryCircleMatch)
					category = null;
			}
			// Ended for VD-109292
						
			Clip clip = getClip(task);
			String contentNotExists = DataUtils.isContentExists(task, category,
					clip);
			logger.info("Got the clip from cache. " + clip
					+ ", contentNotExists: " + contentNotExists);
			if (contentNotExists != null) {
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, clip, getCriteria(task));
				return contentNotExists;
			}

			// If categoryPromoID or categorySmsAlias is passed, then populate
			// the categoryID parameter for further references.
			if (category != null)
				task.put(param_categoryID,
						String.valueOf(category.getCategoryId()));
			// If clipPromoID or clipSmsAlias is passed, then populate the
			// clipID parameter for further references.
			// Added for cut rbt wav file name
			String cutRBTWavFileName = null; 
			if (clip != null){
				if((String)task.get(param_clipID) != null && ((String)task.get(param_clipID)).contains("_cut_")){
					cutRBTWavFileName = (String)task.get(param_clipID);
				}
				task.put(param_clipID, String.valueOf(clip.getClipId()));
			}

			String contentExpired = DataUtils.isContentExpired(task, category,
					clip, selType);
			boolean activateEvenContentExpired = RBTParametersUtils
					.getParamAsBoolean(iRBTConstant.COMMON,
							"ACTIVATE_EVEN_CONTENT_EXPIRED", "TRUE");
			if (contentExpired != null
					&& (!activateEvenContentExpired || category
							.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE)) {
				logger.info("response: " + contentExpired);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, clip, getCriteria(task));
				return contentExpired;
			}

			String action = task.getString(param_action);
			
			if(rbtDBManager.isOverwirteSongPack(subscriberID, task)) {
				action = action_overwrite;
			}
			
			if (action.equalsIgnoreCase(action_overwrite)
					|| action.equalsIgnoreCase(action_overwriteGift)) {
				@SuppressWarnings("null")
				// Clip will not be null here exception Record My own or karaoke
				String subscriberWavFile = null;
				if (!task.containsKey(param_cricketPack)) {
					if (task.containsKey(param_profileHours)
							|| selType == iRBTConstant.PROFILE_SEL_TYPE
							|| category.getCategoryTpe() == iRBTConstant.RECORD
							|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
						String rbtFile = task.getString(param_clipID);
						if (rbtFile.toLowerCase().endsWith(".wav"))
							rbtFile = rbtFile
									.substring(0, rbtFile.length() - 4);

						subscriberWavFile = rbtFile;
					}
				}
				if (clip != null) {
					subscriberWavFile = clip.getClipRbtWavFile();
				}

				if (subscriberWavFile == null) {
					throw new Exception(
							"Wavfile is null, not able to overite selection");
				}
				SubscriberDownloads subscriberDownload = rbtDBManager
						.getActiveSubscriberDownload(subscriberID,
								subscriberWavFile);
				if (subscriberDownload == null
						&& !rbtDBManager.isDownloadAllowed(subscriberID, task)) {
					if(task.containsKey("MUSIC_PACK_DOWNLOAD_REACHED")) {
						return OVERLIMIT;
					}
					response = deleteTone(task);
					if (!response.equalsIgnoreCase(SUCCESS))
						return response;
				}

				if (action.equalsIgnoreCase(action_overwriteGift)) {
					action = action_acceptGift;
					task.put(param_action, action_acceptGift);
				}
			}
			
			task.put(param_requestFromSelection, "true");

			boolean isLimitedPackRequest = false;
			if (task.containsKey(param_cosID)) {
				/*
				 * Limited pack requests flow.
				 */
				String cosID = task.getString(param_cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);
				logger.info("Checking cosType for "
						+ "LIMITED_DOWNLOADS. cosID: " + cosID
						+ ", cosDetails: " + cosDetails);

				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType()) ||iRBTConstant.AZAAN
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					isLimitedPackRequest = true;
				}
			}

			//RBT-13864
			if (shuffleAlwaysEnabledDisabledFlag != null) {
				logger.debug("shuffleAlwaysEnabledDisabledFlag: " + shuffleAlwaysEnabledDisabledFlag);
				boolean inLoop = Boolean.valueOf(shuffleAlwaysEnabledDisabledFlag);
				if (inLoop) {
					task.put(param_inLoop, YES);
				} else {
					task.put(param_inLoop, NO);
				}
			}
			
			boolean isContentAllowed = DataUtils.isContentAllowed(task,
					category, clip);
			logger.info("Checking content allowed or not. "
					+ "isContentAllowed: " + isContentAllowed);

			if (!isContentAllowed) {
				// When a LITE user tries to buy a premium content and the below
				// parameter is configured,
				// then the base is upgraded to DEFAULT COS configured.
				String upgrdCosID = RBTParametersUtils.getParamAsString(
						iRBTConstant.COMMON,
						"UPGRADE_COSID_FOR_LITE_USER_PREMIUM_SELECTION", null);
				logger.info("The content is not allowed, checking upgradeCosID "
						+ " is configured or not. upgradeCosID: " + upgrdCosID);
				
				Subscriber subscriber = DataUtils.getSubscriber(task);
				if (subscriber != null) {
					Map<String, String> upgradeCodIdMap = MapUtils.convertToMap(upgrdCosID, ";",
						                                   	"=", ",");
					if (upgradeCodIdMap != null && upgradeCodIdMap.containsKey(subscriber.cosID())) {
					  String upgradeCosID = upgradeCodIdMap.get(subscriber.cosID());
					  if (upgradeCosID != null) {
						String subscriberStatus = subscriber.subYes();
						logger.info("Since the content is not allowed,"
								+ " checking upgradeCosID: " + upgradeCosID
								+ ", status: " + subscriberStatus
								+ " for subscriberId: " + subscriber.subID());
						if (subscriberStatus
								.equals(iRBTConstant.STATE_ACTIVATED)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_CHANGE)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_GRACE)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_ACTIVATION_PENDING)) {
							CosDetails upgradeCos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											upgradeCosID);
							if (upgradeCos != null) {
								logger.info("Upgrading cos id. upgradeCos: "
										+ upgradeCos + " for subscriberId: "
										+ subscriber.subID());
								task.put(param_rentalPack,
										upgradeCos.getSubscriptionClass());
								task.put(param_cosID, upgradeCos.getCosId());
							} else {
								logger.warn("Not upgrading cos id, upgradeCos "
										+ "is not found." + " upgradeCos: "
										+ upgradeCos + " for subscriberId: "
										+ subscriber.subID());
							}
						}
					}
				  }
				}
			}

			/*
			 * Below if-else block will do the following actions. 1. If a
			 * selection request for AUTO_DOWNLOAD_SHUFFLE category comes, it is
			 * accepted only if the user is new user or user already active on
			 * some other AUTO_DOWNLOAD_SHUFFLE category.
			 * 
			 * 2. If a normal selection request comes for a user who is active
			 * on AUTO_DOWNLOAD pack, shuffle and loop selections are blocked if
			 * it is for all caller.
			 */
			if (category != null
					&& category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE) {
				logger.info("Category type is AUTO_DOWNLOAD_SHUFFLE i.e 21, for categoryId: "
						+ category.getCategoryId());
				Subscriber subscriber = DataUtils.getSubscriber(task);
				if (!rbtDBManager.isSubscriberDeactivated(subscriber)
						&& !rbtDBManager
								.isAutoDownloadPackActivated(subscriber)) {
					logger.warn("Not processing activation. Subscriber is non deactive"
							+ " and auto download pack is not activated. subscriberID: "
							+ subscriberID);
					return ACTIVATION_BLOCKED;
				}

				String circleID = DataUtils.getUserCircle(task);
				String isPrepaid = DataUtils.isUserPrepaid(task) ? YES : NO;
				List<CosDetails> cosList = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetailsByCosType(
								iRBTConstant.COS_TYPE_AUTO_DOWNLOAD, circleID,
								isPrepaid);
				if (cosList == null || cosList.size() == 0) {
					logger.warn("Not processing activation, cos not exists. subscriberID: "
							+ subscriberID);
					return COS_NOT_EXISTS;
				}

				task.put(param_packCosId, cosList.get(0).getCosId());
				// task.put(param_status, "0");
				status = 0;

				boolean useUIChargeClass = false;
				if (task.containsKey(param_useUIChargeClass))
					useUIChargeClass = task.getString(param_useUIChargeClass)
							.equalsIgnoreCase(YES)
							&& (task.containsKey(param_chargeClass));

				if (!useUIChargeClass)
					task.put(param_chargeClass, category.getClassType());

				if (!task.containsKey(param_subscriptionClass))
					task.put(param_subscriptionClass, cosList.get(0)
							.getSubscriptionClass());

				task.put(param_useUIChargeClass, YES);
			} else {
				logger.info("Category is null or category type is non "
						+ "AUTO_DOWNLOAD_SHUFFLE i.e 21, for categoryId: "
						+ category.getCategoryId());
				if (task.containsKey(param_status))
					status = Integer.parseInt(task.getString(param_status));

				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				
				if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
					List<ProvisioningRequests> provList = null;
					boolean isAutoDownloadPackActivated = false;
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriber.extraInfo());
					logger.info("Checking for the PACK attribute of subscriber. "
							+ subscriberID + ", extraInfoMap: " + extraInfoMap);
					if (extraInfoMap != null
							&& extraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
						String packStr = extraInfoMap
								.get(iRBTConstant.EXTRA_INFO_PACK);
						String[] packs = (packStr != null && packStr.trim().length() > 0) ? packStr.trim()
								.split(",") : null;
						for (int i = 0; packs != null && i < packs.length; i++) {
							String activePackCosId = packs[i];
							CosDetails activeCosDet = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											activePackCosId);
							String activeCosType = activeCosDet.getCosType();
							
							if (activeCosType != null
									&& activeCosType
											.equalsIgnoreCase(iRBTConstant.COS_TYPE_AUTO_DOWNLOAD)) {
								logger.info("Checking pack status. "
										+ "activePackCosId: " + activePackCosId);
								provList = ProvisioningRequestsDao
										.getBySubscriberIDTypeAndNonDeactivatedStatus(
												subscriber.subID(),
												Integer.parseInt(activePackCosId));
								isAutoDownloadPackActivated = provList != null;
								logger.info("Verified pack status. subscriberID: "
										+ subscriberID
										+ ", activePackCosId: "
										+ activePackCosId
										+ ", isAutoDownloadPackActivated: "
										+ isAutoDownloadPackActivated);
								
							} else {
								logger.info("Cos type is not AUTO_DOWNLOAD. "
										+ "activePackCosId: " + activePackCosId
										+ ", activeCosType: " + activeCosType
										+ ", subscriberId: " + subscriberID);
							}
						}
					}

					if (isAutoDownloadPackActivated) {
						if (callerId == null
								&& !task.containsKey(param_profileHours)
								&& !task.containsKey(param_cricketPack)) {
							if (Utility.isShuffleCategory(category
									.getCategoryTpe()))
								return NOT_ALLOWED;

							task.put(param_inLoop, NO); // For ALL caller
														// selections, loop
														// selections are to be
														// added in override
														// mode as per the
														// requirement.

							Map<String, String> packExtraInfoMap = DBUtility
									.getAttributeMapFromXML(provList.get(0)
											.getExtraInfo());
							if (packExtraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_PACK_MAX_ALLOWED)) {
								int maxAllowed = Integer
										.parseInt(packExtraInfoMap
												.get(iRBTConstant.EXTRA_INFO_PACK_MAX_ALLOWED));
								if (maxAllowed != 0
										&& maxAllowed <= provList.get(0)
												.getNumMaxSelections()) {
									return LIMIT_EXCEEDED;
								}
							}
						}
					}
				} else {
					logger.warn("Not checking pack details, subscriber is deactive or not exists. "
							+ "subscriber: " + subscriber);
				}
			}

			// RBT-7725 : Rejecting new purchase request if base already pending
			// request in queue
			String baseStatusForSelBlock = RBTParametersUtils.getParamAsString(
					"COMMON", "BASE_STATUS_FOR_BLOCKING_SELECTION", null);
			if (baseStatusForSelBlock != null) {
				List<String> baseStatusForSelBlockList = Arrays
						.asList(baseStatusForSelBlock.split(","));
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				String baseStatus = Utility.getSubscriberStatus(subscriber);
				
				//RBT-12942 - For Renewal grace pending users (subscriber extraInfo contains "renewal_grace=true") the status to be returned as GRACE.
				Map<String, String> extraInfoMap = rbtDBManager.getExtraInfoMap(subscriber);
				if (getParamAsBoolean("COMMON",iRBTConstant.IS_RENEWAL_GRACE_ENABLED, "FALSE") 
						&& extraInfoMap != null
						&& extraInfoMap.containsKey(RENEWAL_GRACE)
						&& ((String)extraInfoMap.get(RENEWAL_GRACE)).equalsIgnoreCase("TRUE")
						&& !baseStatus.equals(DEACT_ERROR)
						&& !baseStatus.equals(DEACT_PENDING)
						&& !baseStatus.equals(DEACTIVE)) {
					baseStatus = GRACE;
				}
				if (baseStatusForSelBlockList != null
						&& baseStatusForSelBlockList.contains(baseStatus)) {
					return SELECTIONS_BLOCKED + "_" + baseStatus;
				}
			}

			// VF-Spain changes for Resubscription RBT-7448
			String deactivatedWithSameSong = checkIfDeactivatedWithSameSongActive(task);
			if (deactivatedWithSameSong != null) {
				return deactivatedWithSameSong;
			}

			// Selection offer check
			if (getParamAsBoolean(iRBTConstant.COMMON,
					iRBTConstant.ALLOW_GET_OFFER, "FALSE")
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.IS_SEL_OFFER_MANDATORY, "FALSE")) {
				boolean downloadExists = true;
				if (getParamAsBoolean(iRBTConstant.COMMON, "ADD_TO_DOWNLOADS",
						"FALSE")) {
					SubscriberDownloads download = rbtDBManager
							.getActiveSubscriberDownload(subscriberID,
									clip.getClipRbtWavFile());
					if (download == null)
						downloadExists = false;

					if (!downloadExists && !task.containsKey(param_offerID))
						return WebServiceConstants.OFFER_NOT_FOUND;
				} else {
					if (!task.containsKey(param_offerID))
						return WebServiceConstants.OFFER_NOT_FOUND;
				}
			}

			if (task.containsKey(param_cosID)) {
				String songBasedCosId = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "SONG_BASED_COS_ID", null);
				if(songBasedCosId != null) {
					List<String> cosIdsList = Arrays.asList(songBasedCosId.split(","));
					String cosId = task.getString(param_cosID);
					Subscriber subscriber = DataUtils.getSubscriber(task);
					if(cosIdsList.contains(cosId) && !rbtDBManager.isSubActive(subscriber)) {
						boolean isShuffleCategory = false;
						if (null != category) {
							isShuffleCategory = Utility
									.isShuffleCategory(category
											.getCategoryTpe());
						}
						logger.info("Performing COS validation. cosId: "
								+ cosId + ", category: " + category
								+ ", isShuffleCategory: " + isShuffleCategory);
						if(task.containsKey(param_profileHours) || task.containsKey(param_cricketPack) || 
								(task.getString(param_status) != null && task.getString(param_status).equalsIgnoreCase("99")) || 
								(task.containsKey(param_callerID) && !task.getString(param_callerID).equalsIgnoreCase("ALL")) || 
								(task.containsKey(param_interval) || (fromTime != 0 || toTime != 2359))
								|| isShuffleCategory) {
							return COSID_BLOCKED_CIRCKET_PROFILE;
						}
//						task.put(param_userInfo + "_"  + iRBTConstant.UDS_OPTIN, "TRUE");
					}
					
					
				}
			}
			boolean isSubscriberAlreadyNotDeactive = rbtDBManager.isSubscriberActivated(subscriberID);  
			logger.info("isSubscriberAlreadyNotDeactive = "+isSubscriberAlreadyNotDeactive);
			if (Arrays.asList(getParamAsString("COMMON", "MODES_FOR_CHECKING_IF_COMBO_REQUEST_PENDING", "")
							.split(",")).contains(task.getString(param_mode))) {
				List<DoubleConfirmationRequestBean> baseConsentPendingRecordList = rbtDBManager
						.getConsentPendingRecordListByMsisdnNType(subscriberID, "ACT");
				List<DoubleConfirmationRequestBean> selConsentPendingRecordList = rbtDBManager
						.getConsentPendingRecordListByMsisdnNType(subscriberID, "SEL");
				if (baseConsentPendingRecordList != null && selConsentPendingRecordList != null
						&& selConsentPendingRecordList.size() > 0
						&& baseConsentPendingRecordList.size() > 0) {
					return ACT_SEL_CONSENT_PENDING;
				}
			}
			
			task.put(IS_COMBO_REQUEST, true);
			if (isLimitedPackRequest) {
				response = upgradeSelectionPack(task);
			} else {
				response = processActivation(task);
				logger.info("Processed activation. response: " + response
						+ ", subscriberId: " + subscriberID);
			}
            boolean isBlackListed = false;
            if(response!=null&&response.equalsIgnoreCase("black_listed")){
            	isBlackListed = true;
            }
			if (YES.equals(task.getString(param_songAlreadyAdded)))
				return response;
			
			/*if(response.equalsIgnoreCase(Constants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE))
				return response;*/
			
			String classType = null;
			Parameters contentTypeParameter = parametersCacheManager
					.getParameter("COMMON", "OFFER_CONTENT_TYPES", null);
			if (contentTypeParameter != null && clip != null) {
				logger.info("The clip is " + clip);
				String offerContentTypes = contentTypeParameter.getValue();
				List<String> contentTypeList = Arrays.asList(offerContentTypes
						.split(","));
				if (contentTypeList.contains(clip.getContentType())) {
					logger.info("The content type matches with the offer content type "
							+ clip.getContentType());
					try {
						HashMap<String, String> offerExtraInfo = new HashMap<String, String>();
						offerExtraInfo.put("CLIP_CONTENT_TYPE",
								clip.getContentType());
						com.onmobile.apps.ringbacktones.smClient.beans.Offer[] allOffers = RBTSMClientHandler
								.getInstance()
								.getOffer(
										subscriberID,
										getMode(task),
										com.onmobile.apps.ringbacktones.smClient.beans.Offer.OFFER_TYPE_SELECTION,
										DataUtils.isUserPrepaid(task) ? "p"
												: "b", clip.getClassType(),
										offerExtraInfo);
						if (allOffers == null || allOffers.length < 1)
							return OFFER_NOT_FOUND;

						classType = allOffers[0].getSrvKey();
						task.put(param_offerID, allOffers[0].getOfferID());

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			if (!(task.containsKey(param_isPreConsentBaseSelRequest) && response.equalsIgnoreCase(preConsentBaseSelSuccess))) {
				if (!response.equalsIgnoreCase(SUCCESS)
						&& !response.equalsIgnoreCase(PACK_ALREADY_ACTIVE)
						&& !Utility.isUserActive(response))
					return response;
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser)
								.equalsIgnoreCase(YES))
					return response;
			}
			// preprocesses the request to check if any content is blocked for
			// any particular requests
			String filterResponse = RbtFilterParser.getRbtFilter()
					.filterSelection(task);
			if (filterResponse != null)
				return filterResponse;


			// For Wind Italy: If any previous download is in pending state and
			// the new selection's charge class is configured, then block the
			// new selection
			try {
				String tmpResponse = Utility.isPreviousSelPending(task,
						subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}

			// RBT-5442
			try {
				String tmpResponse = Utility
						.isPreviousSelPendingWithSameChargeClass(task,
								subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}
			// Subscriber object is stored in task by processActivation method.
			Subscriber subscriber = DataUtils.getSubscriber(task);
			CosDetails cos = null;
			if (subscriber == null
					&& task.containsKey(param_isPreConsentBaseSelRequest)) {
				cos = (CosDetails) task.get("SUB_COS_CONSENT");
			} else {
				if (subscriber.cosID() != null) {
					cos = rbtDBManager.getCosForActiveSubscriber(task,
							subscriber);
					logger.info("Updated subscriber Cos. subscriber cosId: "
							+ subscriber.cosID() + ", returned cos: " + cos
							+ " for subscriberID: " + subscriberID);
				}
			}
			if (task.containsKey(param_removeExistingSetting)
					&& task.getString(param_removeExistingSetting)
							.equalsIgnoreCase(YES)) {
				for (String callerID : validCallerIds) {
					HashMap<String, String> requestParams = new HashMap<String, String>();
					requestParams.put(param_action, action_deleteSetting);
					requestParams.put(param_subscriberID, subscriberID);
					requestParams.put(param_callerID, callerID);
					requestParams.put(param_status, String.valueOf(1));
					requestParams.put(param_fromTime, String.valueOf(0));
					requestParams.put(param_toTime, String.valueOf(23));

					WebServiceContext tempTask = Utility.getTask(requestParams);
					deleteSetting(tempTask);
				}
			}

			String circleID = DataUtils.getUserCircle(task);

			String language = task.getString(param_language);
			String subYes = null;
			boolean isPrepaid = false;
			String subClass = null;
			int rbtType = 0;
			if (subscriber == null
					&& task.containsKey(param_isPreConsentBaseSelRequest)) {
				isPrepaid = task.get("IS_PREPAID_CONSENT")!=null?(Boolean) task.get("IS_PREPAID_CONSENT"):false;
				subClass = (String) task.get("SUB_CLASS_CONSENT");
				try{
				     rbtType = Integer.parseInt(task.getString(param_rbtType));
				}catch(Exception ex){
					rbtType = 0;
				}
			} else {
				if (!task.containsKey(param_activatedNow)
						&& language != null
						&& subscriber!=null && (subscriber.language() == null || !subscriber
								.language().equalsIgnoreCase(language))) {
					rbtDBManager.setSubscriberLanguage(subscriberID, language);
					subscriber.setLanguage(language);
				}

				subYes = subscriber.subYes();
				isPrepaid = subscriber.prepaidYes();
				subClass = subscriber.subscriptionClass();
				try{
				     rbtType = Integer.parseInt(task.getString(param_rbtType));
				}catch(Exception ex){
					rbtType = subscriber.rbtType();
				}

			}

			Categories categoriesObj = CategoriesImpl.getCategory(category);

			String selectedBy = getMode(task);
			String selectionInfo = getModeInfo(task);
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
			
			logger.info("selectedBy: " + selectedBy + ", selectionInfo: "
					+ selectionInfo + ", selectionInfoMap: " + selectionInfoMap);
			
			if(task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				selectionInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
						task.getString(iRBTConstant.EXTRA_INFO_TPCGID));
			}
			
			//RBT-18975
			if(subscriber!=null && subscriber.subYes()!=null && subscriber.subYes().equalsIgnoreCase("C")){
				selectionInfoMap.put("UPGRADE_PENDING",
						"true");
				}
			
			boolean useSubManager = true;

			String messagePath = null;
			Parameters messagePathParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "MESSAGE_PATH", null);
			if (messagePathParam != null)
				messagePath = messagePathParam.getValue().trim();

			boolean changeSubType = true;
			boolean inLoop = false;
			String transID = null;
			String feedType = null;
			String feedSubType = null;
			if (task.containsKey(param_cricketPack)) {
				String cricketPack = task.getString(param_cricketPack);
				logger.info("Request is cricket pack. cricketPack: "
						+ cricketPack);
				if (!cricketPack.equalsIgnoreCase("DEFAULT")) {
					FeedSchedule schedule = rbtDBManager.getFeedSchedule(
							"CRICKET", cricketPack);
					if (schedule == null) {
						Parameters cricketIntervalParm = parametersCacheManager
								.getParameter(iRBTConstant.COMMON,
										"CRICKET_INTERVAL", "2");
						int cricketInterval = Integer
								.parseInt(cricketIntervalParm.getValue().trim());

						FeedSchedule[] schedules = rbtDBManager
								.getFeedSchedules("CRICKET", cricketPack,
										cricketInterval);
						if (schedules != null && schedules.length > 0)
							schedule = schedules[0];
					}
					feedType = schedule.type();
					startDate = schedule.startTime();
					endDate = schedule.endTime();
					classType = schedule.classType();	
					task.put(param_useUIChargeClass, YES);
				}
				//Changes for RBT-14074	Sports Pack for RRBT
				feedSubType = task.getString(param_cricketPack);
				String thirdPartyConfirmationModes = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_MODES",
								null);
                 if (thirdPartyConfirmationModes != null
						&& !task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)){
					selectionInfoMap.put("FEED_SUB_TYPE", feedSubType);
                 } 
				status = 90;
			} else if (task.containsKey(param_profileHours)) {
				String profileHours = task.getString(param_profileHours);
				logger.info("Request is for profile hours. profileHours: "
						+ profileHours);
				/*
				 * When profileHours is not followed by 'D' or 'M' and the clip
				 * sms alias is configured under days category, then 'D' is
				 * added before profileHours to ensure the profile selection is
				 * made for n days instead of n hours.
				 */
				if (!profileHours.startsWith("D")
						&& !profileHours.startsWith("M")
						&& isDurationDays((clip != null) ? clip
								.getClipSmsAlias() : null)) {
					profileHours = "D" + profileHours;
				}

				int minutes;
				if (profileHours.startsWith("D")) {
					minutes = Integer.parseInt(profileHours.substring(1));
					minutes *= 24 * 60;
				} else if (profileHours.startsWith("M")) {
					minutes = Integer.parseInt(profileHours.substring(1));
				} else
					minutes = Integer.parseInt(profileHours) * 60;

				endCal = Calendar.getInstance();
				endCal.add(Calendar.MINUTE, minutes);

				if (endCal.getTime().before(endDate)) {
					// Making sure that endDate will not exceed 2037-01-01
					endDate = endCal.getTime();
				}
				status = 99;
			}

			// ADDED BY SANDEEP FOR PROFILE SELECTION
			else if (task.containsKey(param_selectionType)) {
				int selectionType = Integer.parseInt(task
						.getString(param_selectionType));
				logger.info("Request is for selectionType. selectionType: "
						+ selectionType);
				if (selectionType == iRBTConstant.PROFILE_SEL_TYPE) {
					if (!task.containsKey(param_selectionStartTime)
							&& !task.containsKey(param_selectionEndTime)) {
						int minutes = 60;
						endCal = Calendar.getInstance();
						endCal.add(Calendar.MINUTE, minutes);
						if (endCal.getTime().before(endDate)) {
							endDate = endCal.getTime();
						}
					} else if (task.containsKey(param_selectionStartTime)
							&& !task.containsKey(param_selectionEndTime)) {
						int minutes = 60;
						endCal = Calendar.getInstance();
						endCal.setTime(startDate);
						endCal.add(Calendar.MINUTE, minutes);
						if (endCal.getTime().before(endDate)) {
							endDate = endCal.getTime();
						}
					}
					status = 99;
				}
			}
			
			logger.info("Verifed all the features. status: " + status
					+ ", subscriber: " + subscriber);
			
			// Pack cosid should be two digits.
			if (status == 99) {
				HashMap<String, String> existingExtraInfoMap = rbtDBManager
						.getExtraInfoMap(subscriber);
				String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
						.get(iRBTConstant.EXTRA_INFO_PACK) : null;
				if (existingPacks != null) { // User has packs
					String[] packCosIds = existingPacks.split("\\,");
					for (String packCosId : packCosIds) {
						CosDetails cosDetails = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										packCosId, subscriber.circleID());
						String cosType = (cosDetails != null) ? cosDetails
								.getCosType() : null;
						if (cosType == null
								|| !cosType
										.equalsIgnoreCase(iRBTConstant.PROFILE_COS_TYPE))
							continue;
						int type = Integer.parseInt(packCosId);
						List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
								.getBySubscriberIDAndTypeOrderByCreationTime(
										subscriberID, type);
						int size = provReqList.size();
						ProvisioningRequests pack = (size > 0) ? provReqList
								.get(size - 1) : null;
						if (pack != null) {
							int packStatus = pack.getStatus();
							if (!Utility.isPackActive(packStatus)) {
								return Utility.getSubscriberPackStatus(pack);
							}
						}
						subClass = pack.getChargingClass();
					}
				}

			}
			
			//For Idea Consent. If active user makes profile selection, then mode will be passed as configured mode
			String profileMode = RBTParametersUtils.getParamAsString("COMMON", "PROFILE_MODE_FOR_ACTIVE_USER", null);
			boolean isSubActivated = iRBTConstant.STATE_ACTIVATED.equals(subYes);
			logger.info("Verifying profile mode. profileMode: " + profileMode
					+ ", isSubActivated: " + isSubActivated + ", status: "
					+ status);
			if(status == 99 && profileMode != null && isSubActivated) {
				selectedBy = profileMode;
			}


			// for populating retailer id:
			if (task.containsKey(param_retailerID)) {
				selectionInfoMap.put("RET", task.getString(param_retailerID));
			}

			boolean useUIChargeClass = task.containsKey(param_useUIChargeClass)
					&& task.getString(param_useUIChargeClass).equalsIgnoreCase(
							YES);

			String mode = getMode(task);
			boolean isModeAffiliated = affiliatedContentModeList.contains(mode);
			boolean isNotAllowedMPForAffMode = !(isModeAffiliated && isMpByPassedForAffiliate);

			// to distinguish the selections/downloads made through different
			// packs
			boolean isNotShuffleCategory = !Utility.isShuffleCategory(category
					.getCategoryTpe());
			boolean isPackRequest = rbtDBManager.isPackRequest(cos);
			logger.debug("Checking the values to update pack attribute "
					+ "in selection extrainfo. isPackRequest: " + isPackRequest
					+ ", isNotShuffleCategory: " + isNotShuffleCategory
					+ ", isNotAllowedMPForAffMode: " + isNotAllowedMPForAffMode
					+ ", isMpByPassedForAffiliate: " + isMpByPassedForAffiliate
					+ ", isModeAffiliated: " + isModeAffiliated);
			
			if ((status == 0 && category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE)
					|| (isPackRequest && !useUIChargeClass
							&& isNotShuffleCategory && isNotAllowedMPForAffMode)) {

				selectionInfoMap.put(iRBTConstant.PACK, cos.getCosId());
			}

			if (category.getCategoryTpe() == iRBTConstant.BOX_OFFICE_SHUFFLE) {
				endDate = category.getCategoryEndTime();
				if (endDate.before(new Date())) {
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return CATEGORY_EXPIRED;
				}

				status = 92;
			} else if (category.getCategoryTpe() == iRBTConstant.FESTIVAL_SHUFFLE) {
				endDate = category.getCategoryEndTime();
				if (endDate.before(new Date())) {
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return CATEGORY_EXPIRED;
				}

				status = 93;
			}
			if (task.containsKey(param_status))
				status = Integer.parseInt(task.getString(param_status));

			/*
			 * Added by SenthilRaja Get the offerid if SUPPORT_SMCLIENT_API
			 */
			String baseOfferID = null;
			String selOfferID = null;
			if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
				baseOfferID = getOfferID(task, COMBO_SUB_OFFERTYPE);
				selOfferID = getOfferID(task, SELECTION_OFFERTYPE);
			}

			// For gift selections no need to increment the selection count
			boolean incrSelCountParam = getParamAsBoolean(iRBTConstant.COMMON,
					"INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");
			boolean incrSelCount = incrSelCountParam
					|| !action.equalsIgnoreCase(action_acceptGift);

			boolean allowPremiumContent = task
					.getString(param_allowPremiumContent) != null
					&& task.getString(param_allowPremiumContent)
							.equalsIgnoreCase(YES);

			if (!allowPremiumContent) {
				allowPremiumContent = RBTParametersUtils.getParamAsBoolean(
						iRBTConstant.COMMON,
						"DIRECT_ALLOW_LITE_USER_PREMIUM_CONTENT", "FALSE");
			}
			
			/*
			 * If below parameter is TRUE, we allow overlapping
			 * Time of Day and Day of Week selections.
			 * 
			 * If we set doTODCheck to false, then at DB layer,
			 * we don't check for the overlapping selections.
			 */
			boolean isOverlapAllowed = RBTParametersUtils
					.getParamAsBoolean(
							"COMMON",
							"IS_OVERLAP_ALLOWED_FOR_TIME_AND_DAY_SELECTIONS",
							"FALSE");
			boolean doTODCheck = !isOverlapAllowed;

			String packExtraInfo = null;
			String packRefID = null;
			String refId = null;
			
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift) && !task.containsKey(param_offerID)) {
				
				boolean allowBaseOffer = RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_GET_OFFER, "FALSE");
				if(allowBaseOffer) {
					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, task.getString(param_mode), 
							Offer.OFFER_TYPE_SELECTION, null, null, null); 
					if(offer != null && offer.length > 0) {
						useUIChargeClass = true;
						task.put(param_offerID, offer[0].getOfferID());
						classType = offer[0].getSrvKey();
						selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offer[0].getOfferID());
					}
				}
			}
			
			/**
			 * Ger offer from backed. If SF pass offer, then webservice don't hit offer prism api.Once get the offer id, then validate with blacklisted configured offer id
			 * then webservice will send configured sms and send response as success.
			*/
			if (RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") || RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_SEL_OFFER, "FALSE")) {
				
				// RBT-14504 - My downloads change not working when free friday
				// offer enabled.if the same wav file is already available then
				// no need to hot the offer.
				boolean isAddToDownloads = getParamAsBoolean("COMMON",
						"ADD_TO_DOWNLOADS", "FALSE");
				boolean isNewHitForSongOffer = true;
				SubscriberDownloads[] activeSubscriberDownloads = null;
				Clip clipObj = getClip(task);
				if (isAddToDownloads) {
					activeSubscriberDownloads = rbtDBManager
							.getActiveSubscriberDownloads(subscriberID);
					if (clipObj != null && activeSubscriberDownloads != null) {
						for (SubscriberDownloads subDwn : activeSubscriberDownloads) {
							if (subDwn.promoId().equalsIgnoreCase(
									clipObj.getClipRbtWavFile())) {
								isNewHitForSongOffer = false;
								break;
							}
						}
					}
				}
				logger.info("isNewHitForSongOffer: " + isNewHitForSongOffer);
				if (!task.containsKey(param_offerID) && isNewHitForSongOffer) {
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
					rbtDetailsRequest.setMode(task.getString(param_mode));
					rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
					Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
					
					if(offers != null && offers.length != 0) {
						useUIChargeClass = true;
						task.put(param_useUIChargeClass,YES);
						classType = offers[0].getSrvKey();
						task.put(param_offerID, offers[0].getOfferID());
						selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offers[0].getOfferID());
					}
				}
				
				String offerId = task.getString(param_offerID);
				String offerDaysLimit = RBTParametersUtils.getParamAsString("COMMON",
						iRBTConstant.OFFER_DAYS_LIMIT, "");
				Date date = new Date();
				logger.info("Going for Selection Offer....");
				logger.info("SubscriberID = " + subscriberID + " , Mode = "
						+ task.getString(param_mode) + " , Offer Type = 2(Selection) , ClassType = "
						+ classType);
				
				boolean isActPendingRecord = false;
				if(isAddToDownloads) {
					if (activeSubscriberDownloads != null && offerId!=null && !offerId.equalsIgnoreCase("-1")) {
						for (SubscriberDownloads subDwn : activeSubscriberDownloads) {
							String xtraInfo = subDwn.extraInfo();
							HashMap<String, String> xtraInfoMap = DBUtility
									.getAttributeMapFromXML(xtraInfo);
							char dwnStatus = subDwn.downloadStatus();
							if ((dwnStatus == 'n' || dwnStatus == 'p') && xtraInfoMap != null
									&& xtraInfoMap.containsKey("OFFER_ID")
									&& Arrays.asList(offerDaysLimit.split(","))
									.contains(date.getDay() + "")) { 
								isActPendingRecord = true;
								break;
							}
						}
					}
				}
				else {
					SubscriberStatus[] subscriberSelections = rbtDBManager.getAllActiveSubSelectionRecords(subscriberID);
					if (subscriberSelections != null && offerId!=null && !offerId.equalsIgnoreCase("-1")) {
						for (SubscriberStatus subscriberSelection : subscriberSelections) {
							String xtraInfo = subscriberSelection.extraInfo();
							HashMap<String, String> xtraInfoMap = DBUtility
									.getAttributeMapFromXML(xtraInfo);
							String selStatus = subscriberSelection.selStatus();
							if ((selStatus.equals("A") || selStatus.equals("W") || selStatus.equals("N")) && xtraInfoMap != null
									&& xtraInfoMap.containsKey("OFFER_ID")
									&& Arrays.asList(offerDaysLimit.split(","))
									.contains(date.getDay() + "")) { 
								isActPendingRecord = true;
								break;
							}
						}
					}
				}
				if (isActPendingRecord) {
					logger.info("Already one download is pending to be charged.So, Not allowing to download/selection the song");
					return NOT_ALLOWED;
				}

				String offerIdForBlacklisted = RBTParametersUtils.getParamAsString("COMMON",
						"SEL_OFFER_ID_FOR_BLACKLISTED_SUBSCRIBER", "");

				if (offerId != null && Arrays.asList(offerIdForBlacklisted.split(",")).contains(
						offerId)) {
					String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
							"ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriberID);
					String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("OFFER",
							"BLACKLISTED_OFFER_SEL_TEXT", language);
					task.put(param_smsText, smsText);
					sendSMS(task);
					return SUCCESS;
				}
				
				if(offerId!=null && !offerId.equalsIgnoreCase("-1")){
				    selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				}
				
			}
			
			
			
			// outer for loop added by Sandeep for multiple callerIDs
			for (String callerID : validCallerIds) {
				boolean displayCallerIdWithPreix = RBTParametersUtils.getParamAsBoolean("WEBSERVICE",
						"DISPLAY_CALLER_ID_WITH_PREFIX", "FALSE");
				if (displayCallerIdWithPreix && originalAndTrimmedCallerId != null) {
					
					String callerIdForDisplay = getCallerIdForDisplay(originalAndTrimmedCallerId.get(callerID),callerID);
					selectionInfoMap.put("CALLER_ID", callerIdForDisplay);
				}
				if (category.getCategoryTpe() == iRBTConstant.DYNAMIC_SHUFFLE) {
					String chargingPackage = getChargingPackage(task,
							subscriber, category, null);

					String clipIDStr = task.getString(param_clipID);
					String[] clipIDs = clipIDStr.split(",");

					response = SUCCESS;

					Clip[] clips = new Clip[clipIDs.length];
					for (int i = 0; i < clipIDs.length; i++) {
						task.put(param_clipID, clipIDs[i]);
						clips[i] = getClip(task);
					}
					if (!DataUtils.isContentAllowed(cos, clips)
							&& !allowPremiumContent) {
						if(null != cos && clips.length>0){
							List<String> types = new ArrayList<String>();
							for (Clip clipItem : clips) {
								types.add(clipItem.getContentType());
							}
							if(rbtDBManager.isContentTypeBlockedForCosIdorUdsType(cos.getCosId(), types)){
								logger.info("Response from process selection :: "+LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED );
								return LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
							}	
						}
						writeEventLog(subscriberID, getMode(task), "404",
								CUSTOMIZATION, getClip(task), getCriteria(task));
						if (RBTParametersUtils.getParamAsBoolean(
								iRBTConstant.COMMON,
								"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
								"FALSE")
								&& !(cos != null && PROFILE1
										.equalsIgnoreCase(cos.getCosType()))) {
							task.put(param_info, VIRAL_DATA);
							task.put(param_type, "SELCONFPENDING");
							task.put(param_info + "_CATEGORY_ID",
									task.getString(param_categoryID));
							if (!RBTParametersUtils
									.getParamAsBoolean(
											iRBTConstant.COMMON,
											"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
											"FALSE")) {
								removeData(task);
							}

							addData(task);
						}
						if (pplContentRejectionLogger != null)
							pplContentRejectionLogger
									.PPLContentRejectionTransaction(
											subscriberID, getMode(task), "-1",
											category.getCategoryId() + "",
											new Date());
						return DataUtils
								.getUnAllowedContentResponse(cos, clips);
					}
					String selResponse = "";
					boolean isNoConsentRequired = false;
					isNoConsentRequired = isNoConsentRequired(task, subscriber, isSubscriberAlreadyNotDeactive, classType, mode);
//					logger.info("isSubscriberAlreadyNotDeactive:"+isSubscriberAlreadyNotDeactive+ " classType :" + classType + " mode:"+mode + "isNoConsentRequired :" + isNoConsentRequired);
					if(isNoConsentRequired){
						task.put(param_byPassConsent, "true");
					}
					
					for (int i = 0; i < clips.length; i++) {
						clip = clips[i];

						HashMap<String, Object> clipMap = new HashMap<String, Object>();
						clipMap.put("CLIP_CLASS", clip.getClassType());
						clipMap.put("CLIP_END", clip.getClipEndTime());
						clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
						clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
						clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
						clipMap.put("CLIP_NAME", clip.getClipName());
						if (task.containsKey(param_language)) {
							clipMap.put(Constants.param_LANG_CODE,
									task.getString(param_language));
						}
						classType = null;
						if (i > 0) {
							classType = "FREE";
							inLoop = true;
						}

						
						// IDEA consent
						if (task.containsKey(param_isPreConsentBaseSelRequest) && !isNoConsentRequired) {
							if(isBlackListed){
								return response;
							}
							String extraInfoMap = null;
							if (task.containsKey("userInfo_UDS_OPTIN")) {
								HashMap<String,String> xtraInfoMap = new HashMap<String, String>(); 
								xtraInfoMap.put("UDS_OPTIN", task.getString("userInfo_UDS_OPTIN"));
								extraInfoMap = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
							}
							String requestType = "SEL";
							if (Utility.getSubscriberStatus(subscriber)
									.equalsIgnoreCase(NEW_USER)
									|| Utility.getSubscriberStatus(subscriber)
											.equalsIgnoreCase(DEACTIVE)) {
								requestType = "ACT_SEL";
							}
							Clip clipObj = null;
							if (task.containsKey(param_clipID))
								clipObj = rbtCacheManager.getClip(task
										.getString(param_clipID));

							if (clipObj != null)
								task.put(param_promoID, clip.getClipPromoId());

							String consentTransID = com.onmobile.apps.ringbacktones.services.common.Utility
									.generateConsentIdRandomNumber("");
							if (consentTransID == null) {
								consentTransID = UUID.randomUUID().toString();
							}


							selResponse = rbtDBManager.addSelectionConsent(
									consentTransID, subscriberID, callerID,
									task.getString(param_categoryID),
									task.getString(srvkey), selectedBy,
									startDate, endDate, status, classType,
									task.getString(param_cosID),
									task.getString(param_packCosId),
									task.getString(param_clipID), interval,
									fromTime, toTime, selectionInfo, selType,
									inLoop, "selection", useUIChargeClass,
									category.getCategoryTpe(),
									task.getString(param_profileHours),
									isPrepaid, feedSubType,
									(String) clipMap.get("CLIP_WAV"), rbtType,
									circleID, language, new Date(),
									extraInfoMap, requestType, 0, subscriber,
									task, categoriesObj, doTODCheck);


							return selResponse;
						}
						if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
							selectionInfoMap.put(param_offerID, selOfferID);
							if (selOfferID.equals("-2") && i == 0)
								classType = task.getString(param_chargeClass);
							else if (i == 0)
								classType = task.getString(param_chargeClass);
							else if (i > 0)
								selOfferID = "-2";

							HashMap<String, String> responseParams = new HashMap<String, String>();

							selResponse = rbtDBManager
									.smAddSubscriberSelections(subscriberID,
											callerID, categoriesObj, clipMap,
											null, startDate, endDate, status,
											selectedBy, selectionInfo, 0,
											isPrepaid, changeSubType,
											messagePath, fromTime, toTime,
											classType, useSubManager, true,
											"VUI", chargingPackage, subYes,
											null, circleID, incrSelCount,
											false, transID, false, false,
											inLoop, subClass, subscriber, 0,
											interval, selectionInfoMap,
											responseParams);

							if (selResponse
									.equalsIgnoreCase(iRBTConstant.SELECTION_SUCCESS)) {
								HashMap<String, String> xtraInfoMap = rbtDBManager
										.getExtraInfoMap(subscriber);
								if (task.containsKey(param_scratchNo)) {
									if (xtraInfoMap.containsKey("SRCS")) {
										logger.info("Updating extra info");
										rbtDBManager.updateExtraInfo(
												subscriberID, "SRCS", "1");
										rbtDBManager
												.updateScratchCard(
														task.getString(param_scratchNo),
														"1");
									}
								}
								HashMap<String, String> extraParams = getSelectionExtraParams(
										subscriber, clip, category, callerID,
										selectionInfo, selectionInfoMap);

								String selectionRefID = "";
								if (responseParams.containsKey("REF_ID")) {
									selectionRefID = responseParams.get(REF_ID);
								}
								boolean isSuccess = smClientRquestForSelection(
										task, subscriberID, subscriber,
										classType, baseOfferID, selOfferID,
										selectedBy, selectionRefID, isPrepaid,
										i, extraParams);

								if (!isSuccess)
									break;
							} else {
								if (selResponse
										.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
									writeEventLog(subscriberID, getMode(task),
											"207", PURCHASE, getClip(task));
								} else if (selResponse
										.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
									writeEventLog(subscriberID, getMode(task),
											"204", PURCHASE, getClip(task));
									writeEventLog(subscriberID, getMode(task),
											"402", CUSTOMIZATION,
											getClip(task), getCriteria(task));
								} else {
									writeEventLog(subscriberID, getMode(task),
											"201", PURCHASE, getClip(task));
									writeEventLog(subscriberID, getMode(task),
											"402", CUSTOMIZATION,
											getClip(task), getCriteria(task));
								}
							}

						} else {
							
							logger.info("Adding selection for subscriberID: "
									+ subscriberID + ", selectedBy: "
									+ selectedBy + ", classType: " + classType
									+ ", subClass: " + subClass
									+ ", selectionInfoMap: " + selectionInfoMap);
							
							refId = null;
							if(task.containsKey(param_refID) && !Utility.isModeConfiguredForIdeaConsent(selectedBy)) {
								refId = task.getString(param_refID);
							}
							if (refId == null) {
								refId = UUID.randomUUID().toString();
							}
							
	                        if(task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
	                        	clipMap.put("CONSENT_SUBSCRIPTION_INSERT", "true"); 
	                        }
	                        if(task.containsKey(param_allowPremiumContent)){
	                        	clipMap.put(param_allowPremiumContent, "y");
	                        }
                            if(task.containsKey(param_isUdsOn)){
                            	clipMap.put(param_isUdsOn, task.getString(param_isUdsOn));
                            }
							if (task.containsKey(param_language)) {
								clipMap.put(Constants.param_LANG_CODE,
										task.getString(param_language));
							}
                            // Added the code for direct check RBT 2
							boolean isDirectActivation = false;
							if(task.containsKey(param_isDirectActivation) && task.get(param_isDirectActivation).equals(YES)){
								isDirectActivation = true;
							}
							if (task.containsKey(param_udpId))
								selectionInfoMap.put("UDP_ID", task.getString(param_udpId));
							
							selResponse = rbtDBManager.addSubscriberSelections(
									subscriberID, callerID, categoriesObj,
									clipMap, null, startDate, endDate, status,
									selectedBy, selectionInfo, 0, isPrepaid,
									changeSubType, messagePath, fromTime,
									toTime, classType, useSubManager,
									doTODCheck, "VUI", chargingPackage, subYes,
									null, circleID, incrSelCount, false,
									transID, false, false, inLoop, subClass,
									subscriber, 0, interval, selectionInfoMap,
									useUIChargeClass, refId, isDirectActivation);
							logger.info("Added selection, response: "
									+ selResponse + ", for subscriberID: "
									+ subscriberID + ", selectionInfo: "
									+ selectionInfo + ", classType: "
									+ classType + ", selectionInfoMap: "
									+ selectionInfoMap);
						}
                        if(clipMap.containsKey("RECENT_CLASS_TYPE")){
                        	task.put("RECENT_CLASS_TYPE", clipMap.remove("RECENT_CLASS_TYPE"));
                        }
						response = Utility.getResponseString(selResponse);
						//RBT-13642
						task.put("CGURL", clipMap.remove("CGURL"));
						//RBT-13642
						task.put("CGHttpResponse",clipMap.remove("CGHttpResponse"));
						if (response.equalsIgnoreCase(SUCCESS)) {
							logger.info("Selection is success");
							
							//For consent
							task.put("CONSENTID",clipMap.remove("CONSENTID"));
							task.put("CONSENTCLASSTYPE", clipMap.remove("CONSENTCLASSTYPE"));
							task.put("CONSENTSUBCLASS", clipMap.remove("CONSENTSUBCLASS"));
							task.put("CONSENT_SERVICE_ID",clipMap.remove("CONSENT_SERVICE_ID"));
							task.put("CONSENT_SERVICE_CLASS",clipMap.remove("CONSENT_SERVICE_CLASS"));
							task.put("DESCRIPTION",clipMap.remove("DESCRIPTION"));
							
							task.put("LANGUAGE_ID", clipMap.remove("LANGUAGE_ID"));
							task.put("EVENT_TYPE", clipMap.remove("EVENT_TYPE"));
							task.put("PLAN_ID", clipMap.remove("PLAN_ID"));
							
							task.put("CONSENTSTATUS", clipMap.remove("CONSENTSTATUS"));
							logger.info("--> clipMap.get.combo "+clipMap.get("COMBO"));
							task.put("COMBO", clipMap.remove("COMBO"));

							if(clipMap.containsKey("BSNL_CONSENT_SESSION_ID")) {
								task.put("BSNL_CONSENT_SESSION_ID", clipMap.get("BSNL_CONSENT_SESSION_ID"));
							}
							
							if(clipMap.containsKey("SUBSCRIBER")) {
								task.put(param_subscriber, (Subscriber)clipMap.get("SUBSCRIBER"));
							}
								
							
							HashMap<String, String> xtraInfoMap = rbtDBManager
									.getExtraInfoMap(subscriber);
							if (task.containsKey(param_scratchNo)) {
								if (xtraInfoMap.containsKey("SRCS")) {
									logger.info("Updating extra info");
									rbtDBManager.updateExtraInfo(subscriberID,
											"SRCS", "1");
									rbtDBManager.updateScratchCard(
											task.getString(param_scratchNo),
											"1");
								}
							}
						} else {
							if (selResponse
									.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
								writeEventLog(subscriberID, getMode(task),
										"207", PURCHASE, getClip(task));
							} else if (selResponse
									.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
								writeEventLog(subscriberID, getMode(task),
										"204", PURCHASE, getClip(task));
								writeEventLog(subscriberID, getMode(task),
										"402", CUSTOMIZATION, getClip(task),
										getCriteria(task));
							} else {
								writeEventLog(subscriberID, getMode(task),
										"201", PURCHASE, getClip(task));
								writeEventLog(subscriberID, getMode(task),
										"402", CUSTOMIZATION, getClip(task),
										getCriteria(task));
							}

						}
						if (!response.equalsIgnoreCase(SUCCESS))
							break;
					}
				} else {
					HashMap<String, Object> clipMap = new HashMap<String, Object>();
					if (Utility.isShuffleCategory(category.getCategoryTpe())) {
						Clip[] clips = rbtCacheManager
								.getActiveClipsInCategory(
										category.getCategoryId(),
										browsingLanguage);
						if (!DataUtils.isContentAllowed(cos, clips)
								&& !allowPremiumContent) {
							writeEventLog(subscriberID, getMode(task), "404",
									CUSTOMIZATION, getClip(task),
									getCriteria(task));
							if (RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
									"FALSE")
									&& !(cos != null && PROFILE1
											.equalsIgnoreCase(cos.getCosType()))) {
								task.put(param_info, VIRAL_DATA);
								task.put(param_type, "SELCONFPENDING");
								task.put(param_info + "_CATEGORY_ID",
										task.getString(param_categoryID));
								if (!RBTParametersUtils
										.getParamAsBoolean(
												iRBTConstant.COMMON,
												"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
												"FALSE")) {
									removeData(task);
								}

								addData(task);
							}
							if (pplContentRejectionLogger != null)
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID, getMode(task),
												"-1", category.getCategoryId()
														+ "", new Date());
							return DataUtils.getUnAllowedContentResponse(cos,
									clips);
						}

						int index = 0;
						if (category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE) {
							List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(
											subscriberID,
											Integer.parseInt(cos.getCosId()));
							if (provReqList != null && provReqList.size() > 0) {
								packRefID = provReqList.get(0).getTransId();
								packExtraInfo = provReqList.get(0)
										.getExtraInfo();
								Map<String, String> extraInfoMap = DBUtility
										.getAttributeMapFromXML(packExtraInfo);
								if (extraInfoMap == null)
									extraInfoMap = new HashMap<String, String>();

								String catID = extraInfoMap
										.get(iRBTConstant.EXTRA_INFO_PACK_CATID);
								if (catID == null
										|| !catID.equals(String
												.valueOf(category
														.getCategoryId()))) {
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_CATID,
											String.valueOf(category
													.getCategoryId()));
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_INDEX,
											"0");
								} else {
									index = Integer
											.parseInt(extraInfoMap
													.get(iRBTConstant.EXTRA_INFO_PACK_INDEX));
									index = (index + 1) % clips.length;
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_INDEX,
											String.valueOf(index));
								}
								packExtraInfo = DBUtility
										.getAttributeXMLFromMap(extraInfoMap);
							}
						}

						clip = clips[index];
					} else if (!task.containsKey(param_cricketPack)) {
						if (task.containsKey(param_profileHours)
								|| selType == iRBTConstant.PROFILE_SEL_TYPE
								|| category.getCategoryTpe() == iRBTConstant.RECORD
								|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
							String rbtFile = task.getString(param_clipID);
							if (rbtFile.toLowerCase().endsWith(".wav"))
								rbtFile = rbtFile.substring(0,
										rbtFile.length() - 4);

							clipMap.put("CLIP_WAV", rbtFile);
						}
					}

					if (clip != null) {
						if (!DataUtils.isContentAllowed(cos, clip)
								&& !allowPremiumContent) {
							if(null != cos && null != clip){
								List<String> types = new ArrayList<String>();
								types.add(clip.getContentType());
								if(rbtDBManager.isContentTypeBlockedForCosIdorUdsType(cos.getCosId(), types)){
									logger.info("Response from process selection :: "+LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED );
									return LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
								}	
							}
							writeEventLog(subscriberID, getMode(task), "404",
									CUSTOMIZATION, getClip(task),
									getCriteria(task));
							if (RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
									"FALSE")
									&& !(cos != null && PROFILE1
											.equalsIgnoreCase(cos.getCosType()))) {
								task.put(param_info, VIRAL_DATA);
								task.put(param_type, "SELCONFPENDING");
								task.put(param_info + "_CATEGORY_ID",
										task.getString(param_categoryID));
								if (!RBTParametersUtils
										.getParamAsBoolean(
												iRBTConstant.COMMON,
												"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
												"FALSE")) {
									removeData(task);
								}

								addData(task);
							}
							if (pplContentRejectionLogger != null)
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID, getMode(task),
												clip.getClipId() + "", "-1",
												new Date());
							return DataUtils.getUnAllowedContentResponse(cos,
									clip);
						}

						/*
						 * if (clip.getContentType() != null &&
						 * !clip.getContentType
						 * ().equalsIgnoreCase(COS_TYPE_LITE) && cosType != null
						 * && .equalsIgnoreCase(COS_TYPE_LITE)) return
						 * LITE_USER_PREMIUM_BLOCKED;
						 */

						task.put(session_clip, clip);

						clipMap.put("CLIP_CLASS", clip.getClassType());
						clipMap.put("CLIP_END", clip.getClipEndTime());
						clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
						//added for cut rbt wav file name
						if(cutRBTWavFileName != null){
							clipMap.put("CLIP_WAV", cutRBTWavFileName);
						}else{
						    clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
						}
						
						clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
						clipMap.put("CLIP_NAME", clip.getClipName());
						if (task.containsKey(param_language)) {
							clipMap.put(Constants.param_LANG_CODE,
									task.getString(param_language));
						}
						if (clip.getContentType() != null
								&& clip.getContentType().equalsIgnoreCase(
										CONTENT_TYPE_FEED) && !Utility.isShuffleCategory(category.getCategoryTpe())) {
							// Changed for RBT-1058 (Infotainment RRBT and
							// PreCall)
							// If clip content type is FEED, get CategoryID from
							// configuration.
							// Pass categoryInfo corresponding to that
							// categoryID
							String feedCategoryID = CacheManagerUtil
									.getParametersCacheManager()
									.getParameter(iRBTConstant.COMMON,
											"FEED_CATEGORY_ID", "3").getValue();
							Category feedCategory = rbtCacheManager
									.getCategory(Integer
											.parseInt(feedCategoryID));

							if (feedCategory != null)
								categoriesObj = CategoriesImpl
										.getCategory(feedCategory);
						}
					}

					if (classType == null)
						classType = getChargeClass(task, subscriber, category,
								clip);

					//RBT-12158 Premium content charging priority over cosid
					String tempClassType = Utility.getCosOverrideClass(clip,
							useUIChargeClass, subscriber);
					if (tempClassType != null) {
						classType = tempClassType;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
						incrSelCount = false;
					}
					
					
					// Christmas promotions
					String christmasClassType = checkChristmasPeriod(classType,
							task, subscriber, category, clip);
					if (christmasClassType != null
							&& !christmasClassType.equals(classType)) {
						classType = christmasClassType;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
					}
					
					String contentBaseClasstype = getContentBasedClassType(task, clip);
					if(contentBaseClasstype != null) {
						classType = contentBaseClasstype;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
					}

					String chargingPackage = getChargingPackage(task,
							subscriber, category, clip);

					if (task.containsKey(param_inLoop)
							&& task.getString(param_inLoop).equalsIgnoreCase(
									YES))
						inLoop = true;

					//RBT-12835 - Loop Feature required in Song Pack- ZM
					if (!inLoop && isCosTypeConfiguredForSelectionLoop(subscriber)) {
						inLoop = true;
					}
					transID = task.getString(param_transID);

					int selectionType = 0;
					if (task.containsKey(param_selectionType))
						selectionType = Integer.parseInt(task
								.getString(param_selectionType));

					useUIChargeClass = task.containsKey(param_useUIChargeClass)
							&& task.getString(param_useUIChargeClass)
									.equalsIgnoreCase(YES);

					String selResponse = null;
					boolean isNoConsentRequired = false;
					isNoConsentRequired = isNoConsentRequired(task, subscriber, isSubscriberAlreadyNotDeactive, classType, mode);
//					logger.info("isSubscriberAlreadyNotDeactive:"+isSubscriberAlreadyNotDeactive+ " classType :" + classType + " mode:"+mode + "isNoConsentRequired :" + isNoConsentRequired);
					if(isNoConsentRequired){
						task.put(param_byPassConsent, "true");
					}
					// RBT-10785
					boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
							"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
					if(addProtocolNumber) {
						String wavFile = null;
						if (clip != null) {
							wavFile = clip.getClipRbtWavFile();
						}	
						SubscriberDownloads subscriberDownload = rbtDBManager
								.getActiveSubscriberDownload(subscriberID,
										wavFile);
						if (null == subscriberDownload
								|| (null != subscriberDownload && (subscriberDownload
										.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACTIVATED || subscriberDownload
										.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_BOOKMARK || subscriberDownload
										.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACTIVATION_PENDING || subscriberDownload
										.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_TO_BE_DEACTIVATED))) {
							selectionInfo = appendProtocolNumber(subscriberID,
									selectionInfo);
						} else {
							logger.warn("Since download already exists, not appending protocol number. subscriberID: "
									+ subscriberID + ", wavFile: " + wavFile);
						}
					
					}
					
					if (task.containsKey(param_isPreConsentBaseSelRequest) && !isNoConsentRequired) {
						if(isBlackListed){
							return response;
						}
						String extraInfoMap = null;
						if (task.containsKey("userInfo_UDS_OPTIN")) {
							HashMap<String,String> xtraInfoMap = new HashMap<String, String>(); 
							xtraInfoMap.put("UDS_OPTIN", task.getString("userInfo_UDS_OPTIN"));
							extraInfoMap = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
						}
						String requestType = "Selection";
						if (Utility.getSubscriberStatus(subscriber)
								.equalsIgnoreCase(NEW_USER)
								|| Utility.getSubscriberStatus(subscriber)
										.equalsIgnoreCase(DEACTIVE)) {
							requestType = "BaseSelection";
						}
						Clip clipObj = null;
						if (task.containsKey(param_clipID))
							clipObj = rbtCacheManager.getClip(task
									.getString(param_clipID));

						if (clipObj != null)
							task.put(param_promoID, clip.getClipPromoId());
						String consentTransID = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber("");
						if(consentTransID==null){
							consentTransID = UUID.randomUUID().toString();
						}	
						
						// idea
						selResponse = rbtDBManager.addSelectionConsent(
								consentTransID, subscriberID, callerID,
								task.getString(param_categoryID),
								task.getString(srvkey), selectedBy, startDate,
								endDate, status, classType,
								task.getString(param_cosID),
								task.getString(param_packCosId),
								task.getString(param_clipID), interval,
								fromTime, toTime, selectionInfo, selectionType,
								inLoop, "selection", useUIChargeClass,
								category.getCategoryTpe(),
								task.getString(param_profileHours), isPrepaid,
								feedSubType, (String) clipMap.get("CLIP_WAV"),
								rbtType, circleID, language, new Date(),
								extraInfoMap, requestType, 0, subscriber, task, categoriesObj, doTODCheck);

						return selResponse;
					}
					// if (getParamAsBoolean(iRBTConstant.COMMON,
					// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
					if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
						logger.info("Support client API. Adding selection"
								+ " for subscriberID: " + subscriberID
								+ ", selectionInfoMap: " + selectionInfo);
						selResponse = smAddSubScriberSelection(
								selectionInfoMap, selOfferID, classType, task,
								selResponse, subscriberID, callerID,
								categoriesObj, startDate, endDate, status,
								selectedBy, selectionInfo, isPrepaid,
								messagePath, fromTime, toTime, useSubManager,
								chargingPackage, subYes, circleID, transID,
								subscriber, interval, baseOfferID, -1,
								changeSubType, inLoop, clipMap, clip, category);
					} else {
						if (task.containsKey(param_offerID)) {
							selectionInfoMap.put(
									iRBTConstant.EXTRA_INFO_OFFER_ID,
									task.getString(param_offerID));
						}

						logger.info("Adding selection for subscriberID: "
								+ subscriberID + ", selectedBy: " + selectedBy
								+ ", classType: " + classType + ", subClass: "
								+ subClass + ", selectionInfoMap: " + selectionInfoMap);

						refId = null;
						if(task.containsKey(param_refID) && !Utility.isModeConfiguredForIdeaConsent(selectedBy)) {
							refId = task.getString(param_refID);
						}
						if (refId == null) {
							refId = UUID.randomUUID().toString();
						}
						//RBT-9873 Added for bypassing CG flow
                        if(task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
                        	clipMap.put("CONSENT_SUBSCRIPTION_INSERT", "true"); 
                        }
                        if(task.containsKey(param_allowPremiumContent)){
                        	clipMap.put(param_allowPremiumContent, "y");
                        }
                        if(task.containsKey(param_isUdsOn)){
                        	clipMap.put(param_isUdsOn, task.getString(param_isUdsOn));
                        }
                        if(task.containsKey(param_slice_duration)){
                        	clipMap.put(param_slice_duration, task.getString(param_slice_duration));
                        }
                        String VfUpgradeFeatureClass = CacheManagerUtil
								.getParametersCacheManager()
								.getParameterValue(iRBTConstant.COMMON,
										"CREATE_CLASS_FOR_VF_UPGRADE_FEATURE",
										null);
						if (VfUpgradeFeatureClass != null
								&& !VfUpgradeFeatureClass.isEmpty()) {
							if (task.containsKey(param_subscriber_consent)) {
								Subscriber consentSubscriber = (Subscriber) task
										.get(param_subscriber_consent);
								subscriber.setRefID(consentSubscriber.refID());
							}
							if (task.containsKey(param_consent_subscriptionClass)) {
								clipMap.put(
										param_consent_subscriptionClass,
										task.getString(param_consent_subscriptionClass));
							}
							if (task.containsKey(param_upgrade_consent_flow)) {
								selectionInfoMap.put(
										iRBTConstant.EXTRA_INFO_REQUEST_TYPE,
										"UPGRADE");
							}
						}
						
						// Added the code for direct check RBT 2
						boolean isDirectActivation = false;
						if(task.containsKey(param_selDirectActivation) && task.get(param_selDirectActivation).equals(YES)){
							isDirectActivation = true;
						}
						if (task.containsKey(param_udpId))
							selectionInfoMap.put("UDP_ID", task.getString(param_udpId));
						
						
						selectionInfoMap.put("SELECTED_SUBSCRIPTION_CLASS", task.getString(param_subscriptionClass));
						
						if (task.containsKey(param_rentalPack)){
						      logger.info(":--> task.getString(param_rentalPack) : "+task.getString(param_rentalPack));
							selectionInfoMap.put("SELECTED_SUBSCRIPTION_CLASS", task.getString(param_rentalPack));
							}
						if (task.containsKey(param_agentId)) {
							selectionInfoMap.put(Constants.param_agentId,
									task.getString(param_agentId));
						}
                        selResponse = rbtDBManager.addSubscriberSelections(
								subscriberID, callerID, categoriesObj, clipMap,
								null, startDate, endDate, status, selectedBy,
								selectionInfo, 0, isPrepaid, changeSubType,
								messagePath, fromTime, toTime, classType,
								useSubManager, doTODCheck, "VUI",
								chargingPackage, subYes, null, circleID,
								incrSelCount, false, transID, false, false,
								inLoop, subClass, subscriber, selectionType,
								interval, selectionInfoMap, useUIChargeClass,
								refId, isDirectActivation);
					}
					
					selectionInfoMap.remove("SELECTED_SUBSCRIPTION_CLASS");
					//selectionInfoMap.remove("UPGRADED_SUBSCRIPTION_CLASS");
					if (selResponse
							.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
						writeEventLog(subscriberID, getMode(task), "207",
								PURCHASE, getClip(task));
					} else if (selResponse
							.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
						writeEventLog(subscriberID, getMode(task), "204",
								PURCHASE, getClip(task));
						writeEventLog(subscriberID, getMode(task), "402",
								CUSTOMIZATION, getClip(task), getCriteria(task));
					} else {
						writeEventLog(subscriberID, getMode(task), "201",
								PURCHASE, getClip(task));
						writeEventLog(subscriberID, getMode(task), "402",
								CUSTOMIZATION, getClip(task), getCriteria(task));
					}

					// Allow corporate user update as total black list user.
					if (selectionType == 2
							&& subscriber != null
							&& getParamAsBoolean("COMMON",
									"ALLOW_CORP_USER_AS_TOTAL_BLKLIST_USER",
									"FALSE")) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(2037, 0, 1);
						Date viralEndDate = calendar.getTime();
						rbtDBManager.insertViralBlackList(subscriberID, null,
								viralEndDate, "TOTAL");
					}

					response = Utility.getResponseString(selResponse);

					if(clipMap.containsKey("RECENT_CLASS_TYPE")){
                    	task.put("RECENT_CLASS_TYPE", clipMap.remove("RECENT_CLASS_TYPE"));
                    }

					if(clipMap.containsKey(iRBTConstant.param_isSelConsentInserted)){
						task.put(iRBTConstant.param_isSelConsentInserted,
								clipMap.remove(iRBTConstant.param_isSelConsentInserted));
                    }
					

					//RBT-13642
					task.put("CGURL", clipMap.remove("CGURL"));
					//RBT-13642
					task.put("CGHttpResponse",
							clipMap.remove("CGHttpResponse"));
					if(response.equalsIgnoreCase(SUCCESS)) {
						//For consent
						task.put("CONSENTID",clipMap.remove("CONSENTID"));
						task.put("CONSENTCLASSTYPE", clipMap.remove("CONSENTCLASSTYPE"));
						task.put("CONSENTSUBCLASS", clipMap.remove("CONSENTSUBCLASS"));
						task.put("CONSENT_SERVICE_ID",clipMap.remove("CONSENT_SERVICE_ID"));
						task.put("CONSENT_SERVICE_CLASS",clipMap.remove("CONSENT_SERVICE_CLASS"));
						task.put("DESCRIPTION",clipMap.remove("DESCRIPTION"));
						if (clipMap.containsKey("CONSENT_MODE")) {
							task.put("CONSENT_MODE",
									clipMap.remove("CONSENT_MODE"));
						}
						if(clipMap.containsKey("SUBSCRIBER")) {
							task.put(param_subscriber, (Subscriber)clipMap.get("SUBSCRIBER"));
						}
						logger.info("--> getting value from clipMap");
						logger.info("--> taskput : clipmap.get.consentstatus"+clipMap.get("CONSENTSTATUS"));
						logger.info("--> taskput : clipmap.get.combo"+clipMap.get("COMBO"));
						task.put("CONSENTSTATUS", clipMap.remove("CONSENTSTATUS"));
						task.put("COMBO", clipMap.remove("COMBO"));
						if(clipMap.containsKey("BSNL_CONSENT_SESSION_ID")) {
							task.put("BSNL_CONSENT_SESSION_ID", clipMap.get("BSNL_CONSENT_SESSION_ID"));
						}
			
						task.put("LANGUAGE_ID", clipMap.remove("LANGUAGE_ID"));
						task.put("EVENT_TYPE", clipMap.remove("EVENT_TYPE"));
						task.put("PLAN_ID", clipMap.remove("PLAN_ID"));
						
					}
				}

				if (!task.containsKey(param_isPreConsentBaseSelRequest)) {
					if (response.equalsIgnoreCase(SUCCESS)) {

						task.put("CURRENT_REF_ID", refId);//Done for RBT-12247
						HashMap<String, String> xtraInfoMap = rbtDBManager
								.getExtraInfoMap(subscriber);
						if (xtraInfoMap != null && !xtraInfoMap.isEmpty())
							logger.info("Inf map" + xtraInfoMap.toString());

						if (task.containsKey(param_scratchNo)) {
							logger.info("contains scratch no");

							if (xtraInfoMap.containsKey("SCRS")) {
								logger.info("Updating extra info");
								rbtDBManager.updateExtraInfo(subscriberID,
										"SCRS", "1");
								rbtDBManager.updateScratchCard(
										task.getString(param_scratchNo), "1");
							}
						}

						if (category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE
								&& packExtraInfo != null) {
							ProvisioningRequestsDao.updateExtraInfo(
									subscriberID, packRefID, packExtraInfo);
						}
						//<RBT-10520>
						String ad2cUrl = task.getString(param_ad2cUrl); 
						if (ad2cUrl != null && !ad2cUrl.trim().isEmpty()) {
							logger.info("Parameter " + param_ad2cUrl + " = " +  ad2cUrl + " present in request.");
							RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent();
							rbtCallBackEvent.setClipID(clip.getClipId());
							rbtCallBackEvent.setEventType(RBTCallBackEvent.AD2C_PENDING_CALLBACK);
							rbtCallBackEvent.setMessage(ad2cUrl);
							rbtCallBackEvent.setModuleID(RBTCallBackEvent.MODULE_ID_AD2C);
							rbtCallBackEvent.setSelectedBy(mode);
							rbtCallBackEvent.setSelectionInfo(refId);
							rbtCallBackEvent.setSubscriberID(subscriberID);
							logger.info("Inserting rbtCallBackEvent: " + rbtCallBackEvent);
							rbtCallBackEvent.createCallbackEvent(rbtCallBackEvent);
						}
						//</RBT-10520>
					}

					boolean removeGiftIfAlreadyExists = RBTParametersUtils
							.getParamAsBoolean(iRBTConstant.COMMON,
									"REMOVE_GIFT_IF_SAME_SONG_EXISTS", "true");
					if (response.contains(SUCCESS)
							|| (removeGiftIfAlreadyExists && response
									.equals(ALREADY_EXISTS))) {
						if (action.equalsIgnoreCase(action_acceptGift)) {
							String gifterID = task.getString(param_gifterID);
							Date sentTime = dateFormat.parse(task
									.getString(param_giftSentTime));

							rbtDBManager.updateViralPromotion(gifterID,
									subscriberID, sentTime, "GIFTED",
									"ACCEPT_ACK", new Date(), null, null);
						}
					}
					Parameters corpSelSusAllowed = parametersCacheManager
							.getParameter(iRBTConstant.COMMON,
									"VOL_SUS_NON_CORP", null);
					if (corpSelSusAllowed != null) {
						if (corpSelSusAllowed.getValue() != null
								&& corpSelSusAllowed.getValue()
										.equalsIgnoreCase("true")) {
							String[] corpCatIDsArr = null;
							Parameters corpCatIdsParam = parametersCacheManager
									.getParameter(iRBTConstant.WEBSERVICE,
											"CORP_CAT_ID_LIST", "1");
							if (corpCatIdsParam != null
									&& corpCatIdsParam.getValue() != null)
								corpCatIDsArr = corpCatIdsParam.getValue()
										.trim().split(",");

							ArrayList<String> corpCatIDs = null;
							if (corpCatIDsArr != null
									&& corpCatIDsArr.length > 0) {
								corpCatIDs = new ArrayList<String>();
								for (int count = 0; count < corpCatIDsArr.length; count++) {
									corpCatIDs.add(corpCatIDsArr[count]);
								}
							}
							if (corpCatIDs == null) {
								corpCatIDs = new ArrayList<String>();
								corpCatIDs.add("1");
							}
							if (!corpCatIDs.contains(""
									+ category.getCategoryId())) {
								task.put(param_suspend, "n");
								processSuspension(task);
								task.remove(param_suspend);
							}
						}
					}
					if (response.equalsIgnoreCase(SUCCESS)) {
						isAnyResponseSuccess = true;
					}

					if (response.startsWith(SUCCESS)) {
						sendAcknowledgementSMS(task, "SELECTION");
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Processed selection request, response: " + response
				+ ", subscriberID: " + subscriberID);
		if (isAnyResponseSuccess) {
			return SUCCESS;
		}
		return response;
	}

	//RBT-12835 - Loop Feature required in Song Pack- ZM
	protected boolean isCosTypeConfiguredForSelectionLoop(Subscriber subscriber) {
		boolean isCosTypeConfiguredForSelectionLoop = false;
		HashMap<String, String> extraInfo = rbtDBManager.getExtraInfoMap(subscriber);
		if (extraInfo != null && extraInfo.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
			String subscriberCosIds = extraInfo.get(iRBTConstant.EXTRA_INFO_PACK);

			List<String> subscriberCosIdList = ListUtils.convertToList(
					subscriberCosIds, ",");
			logger.debug("subscriber Extrainfo cosIds: " + subscriberCosIdList);
			Iterator<String> iterator = subscriberCosIdList.iterator();
			while (iterator.hasNext()) {
				String packCosId = iterator.next();
				CosDetails finalCos = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(packCosId);
				if (finalCos != null
						&& Utility.isCosTypeConfiguredForSelectionLoop(finalCos.getCosType())) {
					isCosTypeConfiguredForSelectionLoop = true;
					logger.info("Found cosType for which selection loop is configured. cosType: "
							+ finalCos.getCosType());
					break;
				}
			}
		}
		logger.info("isCosTypeConfiguredForSelectionLoop: " + isCosTypeConfiguredForSelectionLoop);
		return isCosTypeConfiguredForSelectionLoop;
	}

	protected boolean isNoConsentRequired(WebServiceContext task, Subscriber subscriber,
			boolean isSubscriberAlreadyNotDeactive, String classType, String mode) {
		
		boolean useUIChargeClass = task.containsKey(param_useUIChargeClass) && task.getString(param_useUIChargeClass).equalsIgnoreCase(YES);
		if(useUIChargeClass && classType == null && task.containsKey(param_chargeClass)){
			classType = task.getString(param_chargeClass);
		} else if(classType == null){
			classType = RBTDBManager.getInstance().getNextChargeClass(subscriber);
		}
		
		boolean isNoConsentRequired = false;
		if (subscriber != null
				&& Arrays.asList(
						getParamAsString(iRBTConstant.COMMON,
								"SUB_CLASS_FOR_BYPASSING_CONSENT_"+mode, "").split(",")).contains(subscriber.subscriptionClass())
				&& isSubscriberAlreadyNotDeactive) {
			isNoConsentRequired = true;
		}else if (subscriber != null
				&& Arrays.asList(
						 getParamAsString(iRBTConstant.COMMON,
								"SUB_CLASS_FOR_BYPASSING_CONSENT", "").split(",")).contains(subscriber.subscriptionClass())
				&& isSubscriberAlreadyNotDeactive) {
			isNoConsentRequired = true;
		}else if (subscriber != null
				&& Arrays.asList(
						getParamAsString(iRBTConstant.COMMON,
								"CHARGE_CLASS_FOR_BYPASSING_CONSENT_" + mode,"").split(",")).contains(classType)
				&& isSubscriberAlreadyNotDeactive) {
			isNoConsentRequired = true;
		}
		return isNoConsentRequired;
	}

	/**
	 * If the parameter COMMON,SAME_SONG_REACTIVATION_NOT_ALLOWED is true, check
	 * if the same selection/download already exists
	 * 
	 * @param task
	 * @return null if the selection doesn't exist,
	 *         REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED if selection/download
	 *         exists
	 * @throws RBTException
	 */
	protected String checkIfDeactivatedWithSameSongActive(WebServiceContext task)
			throws RBTException {
		// For a deactive user if the same song is already active, dont accept
		// the selection
		if (getParamAsBoolean(iRBTConstant.COMMON,
				iRBTConstant.SAME_SONG_REACTIVATION_NOT_ALLOWED, "FALSE")) {
			Clip clip = getClip(task);
			Subscriber sub = DataUtils.getSubscriber(task);
			String subscriberID = task.getString(param_subscriberID);
			if (rbtDBManager.isSubscriberDeactivated(sub)) {
				boolean activeSelectionExists = false;
				if (getParamAsBoolean(iRBTConstant.COMMON, "ADD_TO_DOWNLOADS",
						"FALSE")) {
					SubscriberDownloads download = rbtDBManager
							.getActiveSubscriberDownload(subscriberID,
									clip.getClipRbtWavFile());
					activeSelectionExists = (download != null);
				} else {
					SubscriberStatus selection = rbtDBManager.getSelection(
							subscriberID, clip.getClipRbtWavFile());
					activeSelectionExists = (selection != null);
				}
				if (activeSelectionExists) {
					return REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#deleteSetting
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String deleteSetting(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			subscriberID = rbtDBManager.subID(subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			String subStatus = Utility.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return subStatus;
			}

			String refID = task.getString(param_refID);

			if (refID != null) {
				SelectionService selectionService = (SelectionService) ConfigUtil.getBean(BeanConstant.SELECTION_WEB_SERVICE);

				selectionService.deleteSettingByRefId(task, subscriberID);
			}		

			Map<String, String> whereClauseMap = new HashMap<String, String>();

			String browsingLanguage = task.getString(param_browsingLanguage);
			if (task.containsKey(param_clipID)) {
				int clipID = Integer.parseInt(task.getString(param_clipID));
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
				if (clip == null) {
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, null, getCriteria(task));
					logger.info("response: " + CLIP_NOT_EXISTS);
					return CLIP_NOT_EXISTS;
				}

				whereClauseMap.put("SUBSCRIBER_WAV_FILE",
						clip.getClipRbtWavFile());
			} else if (task.containsKey(param_rbtFile)) {
				String rbtFile = task.getString(param_rbtFile);
				if (rbtFile.toLowerCase().endsWith(".wav"))
					rbtFile = rbtFile.substring(0, rbtFile.length() - 4);

				whereClauseMap.put("SUBSCRIBER_WAV_FILE", rbtFile);
			}

			if (task.containsKey(param_categoryID)) {
				whereClauseMap.put("CATEGORY_ID",
						task.getString(param_categoryID));
			}
			/*
			 * if (task.containsKey(param_callerID)) { String callerID =
			 * task.getString(param_callerID); whereClauseMap.put("CALLER_ID",
			 * ((callerID == null || callerID .equalsIgnoreCase(ALL)) ? null :
			 * callerID)); }
			 */
			if (task.containsKey(param_status))
				whereClauseMap.put("STATUS", task.getString(param_status));

			DecimalFormat decimalFormat = new DecimalFormat("00");
			if (task.containsKey(param_fromTime)) {
				String fromHrs = task.getString(param_fromTime);
				int fromTimeMinutes = 0;
				if (task.containsKey(param_fromTimeMinutes))
					fromTimeMinutes = Integer.parseInt(task
							.getString(param_fromTimeMinutes));

				whereClauseMap.put("FROM_TIME",
						fromHrs + decimalFormat.format(fromTimeMinutes));
			}
			if (task.containsKey(param_toTime)) {
				String toTimeHrs = task.getString(param_toTime);
				int toTimeMinutes = 59;
				if (task.containsKey(param_toTimeMinutes))
					toTimeMinutes = Integer.parseInt(task
							.getString(param_toTimeMinutes));
				whereClauseMap.put("TO_TIME",
						toTimeHrs + decimalFormat.format(toTimeMinutes));
			}
			if (task.containsKey(param_interval))
				whereClauseMap.put("SEL_INTERVAL",
						task.getString(param_interval));
			if (task.containsKey(param_selectionType))
				whereClauseMap.put("SEL_TYPE",
						task.getString(param_selectionType));

			if (task.containsKey(param_selectionStartTime))
				whereClauseMap.put("START_TIME",
						task.getString(param_selectionStartTime));
			if (task.containsKey(param_selectionEndTime))
				whereClauseMap.put("END_TIME",
						task.getString(param_selectionEndTime));
			// Added for UDP
			if (task.containsKey(param_udpId))
				whereClauseMap.put("UDP_ID", task.getString(param_udpId));

			// Added for Grameen
			if (task.containsKey(param_refID)) {
				whereClauseMap.put("REF_ID", task.getString(param_refID));
			}

			String deselectedBy = getMode(task);
			String deselectionInfo = task.getString(param_modeInfo);

			// Added by Sandeep to support deleteSetting with comma separated
			// callerIds
			String[] callerIds = new String[] { null };
			if (task.containsKey(param_callerID)) {
				String callerId = task.getString(param_callerID);
				callerId = ((callerId == null || callerId.equalsIgnoreCase(ALL)) ? null
						: callerId);
				callerIds = (callerId != null) ? callerId.split(",")
						: callerIds;
			}
			boolean isAnyDeleted = false;
			boolean isAnySelectionPending = false;
			String packCosID = null;
			for (String callerID : callerIds) {
				if (task.containsKey(param_callerID)) {
					whereClauseMap.put("CALLER_ID",
							((callerID == null || callerID
							.equalsIgnoreCase(ALL)) ? null : callerID));
				}
				boolean deleted = false;
				if (getParamAsBoolean(iRBTConstant.COMMON,
						iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
					SubscriberStatus selection = rbtDBManager
							.getSelectionRefID(subscriberID, deselectedBy,
									whereClauseMap);
					String refId = null;
					if (selection != null)
						refId = selection.refID();
					deleted = (refId != null);
					if (deleted) {
						boolean isBulkTask = task
								.containsKey(param_fromBulkTask);
						Clip clip = rbtCacheManager.getClipByRbtWavFileName(
								selection.subscriberFile(), browsingLanguage);
						Category category = rbtCacheManager.getCategory(
								selection.categoryID(), browsingLanguage);

						HashMap<String, String> extraParams = new HashMap<String, String>();
						extraParams
						.put(RBTSMClientHandler.EXTRA_PARAM_USERINFO,
								getSelectionUserInfo(
										subscriberID,
										clip,
										category,
										selection.selectionInfo(),
										null,
										selection.callerID(),
										DBUtility
										.getAttributeMapFromXML(selection
												.extraInfo())));

						StringBuilder builder = new StringBuilder(
								"SM client request for deactivate selection ");
						builder.append("[ subID " + subscriberID);
						builder.append(", prepaidYes "
								+ subscriber.prepaidYes());
						builder.append(", deselectedBy - " + deselectedBy);
						builder.append(", refID - " + refId);
						builder.append(", isBulkTask - " + isBulkTask);
						builder.append(", refId - " + refId);
						builder.append(", chargeClass - "
								+ selection.classType());
						builder.append(", extraParams - " + extraParams);
						logger.info(builder.toString());

						RBTSMClientResponse smClientResponse = RBTSMClientHandler
								.getInstance().deactivateSelection(
										subscriberID, subscriber.prepaidYes(),
										deselectedBy, isBulkTask, refId,
										selection.classType(), extraParams);

						logger.info("SMClient Response for Deactive Subscriber selection"
								+ smClientResponse.toString());
						deleted = smClientResponse.getResponse()
								.equalsIgnoreCase(RBTSMClientResponse.SUCCESS);
						if (deleted)
							deleted = rbtDBManager
							.smDeactivateSubscriberSelections(
									subscriberID, deselectedBy,
									whereClauseMap);
					}
				} else {

					Map<String, String> updateClauseMap = new HashMap<String, String>();
					updateClauseMap.put("DESELECTED_BY", deselectedBy);

					if (deselectionInfo != null)
						updateClauseMap
						.put("DESELECTION_INFO", deselectionInfo);
					
					//RBT-15403 
					boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
							"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
					if (addProtocolNumber) {
						deselectionInfo = appendProtocolNumber(subscriberID,
								deselectionInfo);
					}
					
					if (deselectionInfo != null)
						updateClauseMap
								.put("DESELECTION_INFO", deselectionInfo);
					
					//Added extra info column to update the sr_id and originator info 
					// as per the jira id RBT-11962
					HashMap<String, String> userInfMap = new HashMap<String, String>();
					Set<String> keySet = task.keySet();
					for (String key : keySet) {
						logger.info("Extra Info values:" + key + " value "
								+ task.getString(key));
						if (key.startsWith(param_userInfo + "_")) {
							userInfMap.put(
									key.replace(param_userInfo + "_", ""),
									task.getString(key));
						}
					}
					logger.info("userInfoMap values:" + userInfMap);
					String strExtraInfoMap = DBUtility
							.getAttributeXMLFromMap(userInfMap);
					updateClauseMap.put("EXTRA_INFO", strExtraInfoMap);
					SubscriberStatus[] subSelections = null;
					if (!RBTParametersUtils.getParamAsBoolean(
							iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE")) {
						subSelections = rbtDBManager
								.getSelectionsToBeDeactivated(subscriberID,
										whereClauseMap);
						if (subSelections != null && subSelections.length > 0) {
							for (SubscriberStatus selection : subSelections) {
								if (rbtDBManager
										.isSelectionActivationPending(selection)
										|| rbtDBManager
										.isSelectionGrace(selection)) {
									isAnySelectionPending = true;
									Map<String, String> extraInfoMap = DBUtility
											.getAttributeMapFromXML(selection
													.extraInfo());
									if (extraInfoMap != null
											&& extraInfoMap
											.containsKey(iRBTConstant.PACK)) {
										String cosID = extraInfoMap
												.get(iRBTConstant.PACK);
										List<ProvisioningRequests> provRequest = ProvisioningRequestsDao
												.getBySubscriberIDTypeAndNonDeactivatedStatus(
														subscriber.subID(),
														Integer.parseInt(cosID));
										if (provRequest != null) {
											packCosID = cosID;
										}
									}
								}
							}
						}
					}

					if (RBTParametersUtils.getParamAsBoolean(
							iRBTConstant.COMMON, "ADD_TO_DOWNLOADS", "FALSE")
							|| (subSelections != null && subSelections.length > 0)) {
						if (RBTParametersUtils.getParamAsBoolean("DAEMON",
								"DEACTIVATE_OLDER_SHUFFLE_DOWNLOADS", "FALSE")) {
							logger.info("Inside Deactivate Older Shuffle........");
							if (subSelections == null)
								subSelections = rbtDBManager
								.getSelectionsToBeDeactivated(
										subscriberID, whereClauseMap);

							Set<String> tobeDeactDownload = new HashSet<String>();
							SubscriberStatus[] activeSelections = rbtDBManager
									.getAllActiveSubscriberSettings(subscriberID);

							if (subSelections != null) {
								for (int i = 0; i < subSelections.length; i++) {
									if (Utility
											.isShuffleCategory(subSelections[i]
													.categoryType())) {
										String wavFile = subSelections[i]
												.subscriberFile();
										tobeDeactDownload.add(wavFile);
									}
								}
							}

							if (activeSelections != null) {
								for (SubscriberStatus activeSubStatus : activeSelections) {
									if (Utility
											.isShuffleCategory(activeSubStatus
													.categoryType())
													&& !(Utility.isCallerIDSame(
															activeSubStatus.callerID(),
															callerID))) {
										if (tobeDeactDownload
												.contains(activeSubStatus
														.subscriberFile()))
											tobeDeactDownload
											.remove(activeSubStatus
													.subscriberFile());
									}
								}
							}

							if (tobeDeactDownload.size() > 0) {
								logger.info("Deactivating Older Shuffles.....:No of downloads to be deactivated="
										+ tobeDeactDownload.size());
								for (String promoId : tobeDeactDownload) {
									rbtDBManager
									.deactivateSubscriberDownload(
											subscriberID, promoId,
											deselectedBy);
								}
							}
						}


						if (task.containsKey(param_isDirectDeactivation)
								&& task.getString(param_isDirectDeactivation)
								.equalsIgnoreCase(YES)) {
							updateClauseMap.put("SEL_STATUS", "X");
							updateClauseMap.put("LOOP_STATUS", "x");
						}
						deleted = rbtDBManager.deactivateSubscriberSelections(
								subscriberID, updateClauseMap, whereClauseMap);
					}
				}

				if (deleted) {
					isAnyDeleted = true;
				}
			}

			// if any of the callerId is deleted then response is success
			if (isAnyDeleted) {
				writeEventLog(subscriberID, getMode(task), "0", CUSTOMIZATION,
						getClip(task), getCriteria(task));
				response = SUCCESS;

				if (isAnySelectionPending) {
					if (packCosID != null) {
						CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(packCosID);
						//						Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
						//								.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
						//						
						//						List<String> musicPackCosIdList = null;
						//						
						//						if(muiscPackCosIdParam != null) {
						//							musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
						//						}

						if(packCos == null || !packCos.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
							rbtDBManager.decrementNumMaxSelectionsForPack(
									subscriberID, packCosID);
						}
					} else {
						rbtDBManager.decrementNumMaxSelections(subscriberID);
					}
				}
			} else {
				writeEventLog(subscriberID, getMode(task), "402",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				response = FAILED;
			}

			if (response.equalsIgnoreCase(SUCCESS)) {
				sendAcknowledgementSMS(task, "DELETE_SETTING");

				if (refID != null) {

					SubscriberSelectionImpl selectionImpl = (SubscriberSelectionImpl) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
					if(task.getString(param_udpId) != null) {
						int count = selectionImpl.getSubSelectionCountByUDPId(task.getString(param_udpId));
						logger.info("Selections_Active_Count : "+count);
						if (count == 0) { 
							IUDPService udpService = (IUDPService) ConfigUtil.getBean(BeanConstant.UDP_RBT_SERVICE_IMPL);
							UDPResponseBean responseBean = (UDPResponseBean) udpService.getContentsFromUDP(subscriberID, task.getString(param_udpId), 0, 1);
							UDPBean udpBean = UDPDOToResponseBeanConverter.getUDPBeanFromUDPResponseBean(responseBean);
							if (udpBean != null) {
								IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
								udpBean.setSelActivated(false);
								udpDao.updateUDP(udpBean);
							}
						}
					}
				}	
			}

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			response = e.getBeanName();
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	protected String appendProtocolNumber(String subscriberId,
			String selectionInfo) {
		try {
			selectionInfo = (null == selectionInfo) ? "" : selectionInfo;
			String id = RBTProtocolNumberGenerator.INSTANCE
					.generateDBNo(subscriberId);
			selectionInfo = selectionInfo + "|protocolnumber:" + id + "|";
		} catch (Exception e) {
			logger.error("Unable to create protocol number for subscriberId: "
					+ subscriberId + ", Exception: " + e.getMessage(), e);
		}
		logger.info("Successfully created protocol number. Returning: "
				+ selectionInfo + " for subscriberId: " + subscriberId);
		return selectionInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#updateSelection
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String updateSelection(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);

			String info = task.getString(param_info);
			if (info != null && info.equalsIgnoreCase(RENEW)) {
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				if (!rbtDBManager.isSubActive(subscriber)) {
					logger.info("response: " + USER_NOT_ACTIVE);
					return USER_NOT_ACTIVE;
				}
				String subStatus = Utility.getSubscriberStatus(subscriber);
				if (subStatus.equals(LOCKED))
					return subStatus;

				int rbtType = 0;
				if (task.containsKey(param_rbtType))
					rbtType = Integer.parseInt(task.getString(param_rbtType));

				SubscriberStatus[] settings = rbtDBManager
						.getAllActiveSubscriberSettings(subscriberID, rbtType);
				if (settings == null) {
					logger.info("response: " + NO_SELECTIONS);
					return NO_SELECTIONS;
				}

				Clip clip = null;
				String clipID = task.getString(param_clipID);
				if (clipID == null && task.containsKey(param_clipPromoID)) {
					String promoID = task.getString(param_clipPromoID);
					PromoMaster[] promoMasters = rbtDBManager
							.getPromoForCode(promoID);
					if (promoMasters != null) {
						if (promoMasters.length == 1)
							clipID = promoMasters[0].clipID();
						else {
							logger.info("response: " + TECHNICAL_DIFFICULTIES);
							return TECHNICAL_DIFFICULTIES;
						}
					} else {
						clip = rbtCacheManager.getClipByPromoId(promoID,
								browsingLanguage);
					}
				}

				if (clipID != null)
					clip = rbtCacheManager.getClip(clipID, browsingLanguage);

				if (clipID != null && clip == null) {
					logger.info("response: " + INVALID_CODE);
					return INVALID_CODE;
				}
				if (clip != null
						&& clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					logger.info("response: " + CLIP_EXPIRED);
					return CLIP_EXPIRED;
				}

				String clipWavFile = null;
				if (clip != null)
					clipWavFile = clip.getClipRbtWavFile();

				boolean foundSelection = false;
				for (SubscriberStatus setting : settings) {
					ChargeClass chargeClassObj = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									setting.classType());
					if ((clipWavFile == null || clipWavFile.equals(setting
							.subscriberFile()))
							&& setting.selStatus().equals("B")
							&& (chargeClassObj.getSelectionPeriod()
									.equalsIgnoreCase("O"))) {
						foundSelection = true;
						if (getParamAsBoolean(iRBTConstant.COMMON,
								iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
							rbtDBManager.smUpdateSelStatus(subscriberID,
									setting.callerID(),
									setting.subscriberFile(),
									setting.setTime(), setting.selStatus(),
									"B", rbtType);

							// Invoke upgrade services
							HashMap<String, String> extraInfoMap = DBUtility
									.getAttributeMapFromXML(setting.extraInfo());
							String offerID = extraInfoMap.get(param_offerID);
							String chargeClass = setting.classType();

							StringBuilder builder = new StringBuilder(
									"SM client request for update selection ");
							builder.append("[ subID " + subscriberID);
							builder.append(", prepaidYes "
									+ subscriber.prepaidYes());
							builder.append(", mode - " + setting.deSelectedBy());
							builder.append(", chargeClass - " + chargeClass);
							builder.append(", offerID - " + offerID);
							builder.append(", refID - " + setting.refID());
							logger.info(builder.toString());

							RBTSMClientResponse smClientResponse = RBTSMClientHandler
									.getInstance().renewSelection(subscriberID,
											subscriber.prepaidYes(),
											chargeClass, offerID,
											setting.deSelectedBy(),
											setting.refID());

							logger.info("SMClient Response for Selection renewal "
									+ smClientResponse.toString());
							if (!smClientResponse.getResponse()
									.equalsIgnoreCase(
											RBTSMClientResponse.SUCCESS)) {
								rbtDBManager.smUpdateSelStatus(subscriberID,
										setting.callerID(),
										setting.subscriberFile(),
										setting.setTime(), "B",
										setting.selStatus(), rbtType);
								return smClientResponse.getResponse();
							}
						} else {
							rbtDBManager.smUpdateSelStatus(subscriberID,
									setting.callerID(),
									setting.subscriberFile(),
									setting.setTime(), setting.selStatus(),
									"R", rbtType);
						}
					}
				}

				if (foundSelection)
					response = SUCCESS;
				else
					return INVALID_CODE;
			} else if (info != null && info.equalsIgnoreCase(MODIFY)) {
				String refID = null;
				String interval = null;
				int fromHrs = 0;
				int toHrs = 23;
				int fromMinutes = 0;
				int toMinutes = 59;

				if (task.containsKey(param_fromTime))
					fromHrs = Integer.parseInt(task.getString(param_fromTime));
				if (task.containsKey(param_toTime))
					toHrs = Integer.parseInt(task.getString(param_toTime));
				if (task.containsKey(param_toTimeMinutes))
					toMinutes = Integer.parseInt(task
							.getString(param_toTimeMinutes));
				if (task.containsKey(param_fromTimeMinutes))
					fromMinutes = Integer.parseInt(task
							.getString(param_fromTimeMinutes));

				DecimalFormat decimalFormat = new DecimalFormat("00");
				int fromTime = Integer.parseInt(fromHrs
						+ decimalFormat.format(fromMinutes));
				int toTime = Integer.parseInt(toHrs
						+ decimalFormat.format(toMinutes));

				String rbtFile = task.getString(param_clipID);
				try {
					int clipID = Integer.parseInt(rbtFile);
					Clip clip = rbtCacheManager.getClip(clipID,
							browsingLanguage);
					rbtFile = clip.getClipRbtWavFile();
				} catch (NumberFormatException e) {

				}
				refID = task.getString(param_refID);
				interval = task.getString(param_interval);
				logger.info("interval :" + interval + " & refID >" + refID);
				boolean updateResponse = rbtDBManager.updateSelection(
						subscriberID, rbtFile, fromTime, toTime, interval,
						refID);

				if (updateResponse) {
					response = SUCCESS;
					rbtDBManager.updatePlayerStatus(subscriberID, "A");
				} else
					response = ERROR;
			} else if (info != null && info.equalsIgnoreCase(DEACT_DELAY)) {
				SubscriberStatus settings[] = rbtDBManager
						.getAllActiveSubscriberSettings(subscriberID);
				if (settings != null && settings.length > 0) {
					for (int i = 0; i < settings.length; i++) {
						HashMap<String, String> selExtraInfoMap = DBUtility
								.getAttributeMapFromXML(settings[i].extraInfo());
						if (selExtraInfoMap == null)
							selExtraInfoMap = new HashMap<String, String>();
						selExtraInfoMap.put("DELAY_DEACT", "TRUE");
						String extraInfo = DBUtility
								.getAttributeXMLFromMap(selExtraInfoMap);
						response = rbtDBManager
								.updateSelectionStatusNExtraInfo(subscriberID,
										settings[i].refID(), extraInfo, "C",null);
					}
				} else
					response = NO_SELECTIONS;

			} else if (info != null && info.equalsIgnoreCase(UPGRADE_SEL_PACK)) {
				String initClassType = task.getString(param_initClassType);
				String finalClassType = task.getString(param_subscriptionClass);
				String refID = task.getString(param_refID);
				if (finalClassType == null || initClassType == null
						|| refID == null) {
					logger.info("response: " + INVALID_PARAMETER);
					return INVALID_PARAMETER;
				}

				String mode = getMode(task);
				boolean success = rbtDBManager.upgradeSelectionClassType(
						subscriberID, initClassType, finalClassType, 0, refID,
						mode);
				if (success)
					response = SUCCESS;
				else
					response = FAILED;
			} else if (info != null && info.equalsIgnoreCase(UPGRADE_VALIDITY)) {
				String refID = task.getString(param_refID);
				if (refID == null) {
					logger.info("Response: " + INVALID_PARAMETER);
					return INVALID_PARAMETER;
				}
				SubscriberStatus subscriberStatus = rbtDBManager
						.getSelectionByRefId(subscriberID, refID);
				if (subscriberStatus == null) {
					logger.info("Response: " + NO_SELECTIONS_TO_UPGRADE);
					return NO_SELECTIONS_TO_UPGRADE;
				}

				if (subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_ACTIVATED)) {
					task.put(param_subscriberStatus, subscriberStatus);
					response = Utility.sendRenewalRequestToSubMgr(task);
				} else {
					response = Utility
							.getSubscriberSettingStatus(subscriberStatus);
					if (response.equalsIgnoreCase(ACTIVE)) {
						// Pack upgradation pending(STATE_CHANGE) and
						// voluntarily suspended users are considered as active
						// users.
						// But not allowed to for subscription upgradation,
						// that's why returning response as ACT_PENDING.
						response = ACT_PENDING;
					}
				}

				logger.info("upgradeSelectionValidity reponse: " + response);
				return response;
			} else {
				String callerID = (!task.containsKey(param_callerID) || task
						.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
						: task.getString(param_callerID);

				String rbtFile = task.getString(param_clipID);
				try {
					int clipID = Integer.parseInt(rbtFile);
					Clip clip = rbtCacheManager.getClip(clipID,
							browsingLanguage);
					rbtFile = clip.getClipRbtWavFile();
				} catch (NumberFormatException e) {

				}

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				Date setTime = dateFormat.parse(task.getString(param_setTime));

				int fromTime = Integer.parseInt(task.getString(param_fromTime));
				int toTime = Integer.parseInt(task.getString(param_toTime));
				String interval = task.getString(param_interval);
				String selectedBy = getMode(task);

				String updateResponse = rbtDBManager.updateSubscriberSelection(
						subscriberID, callerID, rbtFile, setTime, fromTime,
						toTime, interval, selectedBy);

				response = Utility.getResponseString(updateResponse);
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#downloadTone(
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String downloadTone(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = null;
		try {
			subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);

			logger.info("Downloading tone for subscriberID: " + subscriberID);

			if (getParamAsString(iRBTConstant.COMMON,
					"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION", null) != null) {
				Subscriber sub = DataUtils.getSubscriber(task);
				String extraInfo = (sub != null) ? sub.extraInfo() : null;
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null
						&& extraInfoMap.containsKey("UNSUB_DELAY")) {
					return NOT_ALLOWED;
				}
			}

			
			int categoryID = -1;
			if(task.containsKey(param_categoryID)) {
				categoryID = Integer.parseInt(task.getString(param_categoryID));
			}
			Category category = rbtCacheManager.getCategory(categoryID,
					browsingLanguage);
			if (category == null) {
				logger.info("response: " + CATEGORY_NOT_EXISTS);
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE,
						getClip(task));
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return CATEGORY_NOT_EXISTS;
			}
			

			Categories categoriesObj = CategoriesImpl.getCategory(category);

			boolean isLimitedPackRequest = false;
			if (task.containsKey(param_cosID)) {
				/*
				 * Limited pack requests flow.
				 */
				String cosID = task.getString(param_cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);

				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType())
								||iRBTConstant.AZAAN
								  .equalsIgnoreCase(cosDetails.getCosType())
								|| iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
										.equalsIgnoreCase(cosDetails
												.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
									.equalsIgnoreCase(cosDetails.getCosType()))) {
					isLimitedPackRequest = true;
				}
			}
			task.put(param_requestFromSelection, "true");

			// VF-Spain changes for Resubscription RBT-7448
			String deactivatedWithSameSong = checkIfDeactivatedWithSameSongActive(task);
			if (deactivatedWithSameSong != null) {
				return deactivatedWithSameSong;
			}

			// Selection offer check
			if (getParamAsBoolean(iRBTConstant.COMMON,
					iRBTConstant.ALLOW_GET_OFFER, "FALSE")
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.IS_SEL_OFFER_MANDATORY, "FALSE")) {
				if (!task.containsKey(param_offerID))
					return WebServiceConstants.OFFER_NOT_FOUND;
			}

			if (isLimitedPackRequest) {
				response = upgradeSelectionPack(task);
			} else {
				response = processActivation(task);
			}
			if (!response.equalsIgnoreCase(SUCCESS)
					&& !response.equalsIgnoreCase(PACK_ALREADY_ACTIVE)
					&& !Utility.isUserActive(response))
				return response;

			String action = task.getString(param_action);
			if (rbtDBManager.isOverwirteSongPack(subscriberID, task)) {
				action = action_overwriteDownload;
			}
			if (action.equalsIgnoreCase(action_overwriteDownload)
					|| action.equalsIgnoreCase(action_overwriteDownloadGift)) {
				Clip clip = getClip(task);
				if (clip == null) {
					logger.info("response: " + CLIP_NOT_EXISTS);
					writeEventLog(subscriberID, getMode(task), "201", PURCHASE,
							getClip(task));
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return CLIP_NOT_EXISTS;
				}

				SubscriberDownloads subscriberDownload = rbtDBManager
						.getActiveSubscriberDownload(subscriberID,
								clip.getClipRbtWavFile());
				if (subscriberDownload == null) {
					if (!rbtDBManager.isDownloadAllowed(subscriberID, task)) {
						if (task.containsKey("MUSIC_PACK_DOWNLOAD_REACHED")) {
							return OVERLIMIT;
						}
						response = deleteTone(task);
						if (!response.equalsIgnoreCase(SUCCESS))
							return response;
					}
				} else {
					if (task.getString(param_action).equalsIgnoreCase(
							action_overwriteDownloadGift)) {
						task.put(param_action, action_acceptGift);

						String gifterID = task.getString(param_gifterID);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Date sentTime = dateFormat.parse(task
								.getString(param_giftSentTime));

						rbtDBManager.updateViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED", "ACCEPT_ACK",
								new Date(), null, null);
					}

					logger.info("response: " + ALREADY_ACTIVE);
					writeEventLog(subscriberID, getMode(task), "207", PURCHASE,
							getClip(task));
					writeEventLog(subscriberID, getMode(task), "0",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return ALREADY_ACTIVE;
				}
			}

			if (action.equalsIgnoreCase(action_downloadGift)
					|| action.equalsIgnoreCase(action_overwriteDownloadGift))
				task.put(param_action, action_acceptGift);

			response = processActivation(task);
			if (!response.equalsIgnoreCase(SUCCESS)
					&& !response.equalsIgnoreCase(PACK_ALREADY_ACTIVE)
					&& !Utility.isUserActive(response))
				return response;

			Subscriber subscriber = (Subscriber) task.get(param_subscriber);
			if (subscriber != null
					&& Utility.getSubscriberStatus(subscriber)
							.equalsIgnoreCase(SUSPENDED)) {
				return ALREADY_SUSPENDED;
			}
			String language = task.getString(param_language);
			if (!task.containsKey(param_activatedNow)
					&& language != null
					&& (subscriber.language() == null || !subscriber.language()
							.equalsIgnoreCase(language))) {
				rbtDBManager.setSubscriberLanguage(subscriberID, language);
				subscriber.setLanguage(language);
			}

			Clip clip = null;
			if (Utility.isShuffleCategory(category.getCategoryTpe())) {
				Clip[] clips = rbtCacheManager.getActiveClipsInCategory(
						categoryID, browsingLanguage);
				clip = clips[0];
			} 
			//RBT 2.0 changes for UGC
			else if(category.getCategoryTpe() == iRBTConstant.RECORD) {
				long toneId = Long.parseLong(task.getString(param_clipID));
				clip = ServiceUtil.getClip(toneId, "RBTUGC");
			}
			else {
				clip = getClip(task);
			}

			if (clip == null) {
				logger.info("response: " + CLIP_NOT_EXISTS);
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE,
						getClip(task));
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return CLIP_NOT_EXISTS;
			}

			// For Wind Italy: If any previous download is in pending state and
			// the new selection's charge class is configured, then block the
			// new selection
			try {
				String tmpResponse = Utility.isPreviousSelPending(task,
						subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}

			// RBT-5442
			try {
				String tmpResponse = Utility
						.isPreviousSelPendingWithSameChargeClass(task,
								subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}

			String selectedBy = getMode(task);
			String selectionInfo = getModeInfo(task);

			// RBT-10785
			boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
					"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
			if (addProtocolNumber) {
				selectionInfo = appendProtocolNumber(subscriberID,
						selectionInfo);
			}

			String classType = getChargeClass(task, subscriber, category, clip);
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);

			if (task.containsKey(param_offerID)) {
				selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
						(String) task.get(param_offerID));
			}

			boolean isSubActive = false;
			String subYes = subscriber.subYes();
			if (subYes != null
					&& (subYes.equals(iRBTConstant.STATE_ACTIVATED) || subYes
							.equals(iRBTConstant.STATE_EVENT))) {
				if (!rbtDBManager.isPackActivationPendingForContent(subscriber,
						category, clip, 1, null))
					isSubActive = true;
			}

			// For gift selections no need to increment the selection count
			action = task.getString(param_action);
			boolean incrSelCountParam = getParamAsBoolean(iRBTConstant.COMMON,
					"INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");
			boolean incrSelCount = incrSelCountParam
					|| !action.equalsIgnoreCase(action_acceptGift);

			boolean useUIChargeClass = task.containsKey(param_useUIChargeClass)
					&& task.getString(param_useUIChargeClass).equalsIgnoreCase(
							YES);

			// for populating retailer id in selection info:
			if (task.containsKey(param_retailerID)) {
				selectionInfoMap.put("RET", task.getString(param_retailerID));
			}

			// to distinguish the selections/downloads made through different
			// packs
			CosDetails cos = null;
			if (subscriber.cosID() != null)
				cos = rbtDBManager.getCosForActiveSubscriber(task, subscriber);

			if (rbtDBManager.isPackRequest(cos) && !useUIChargeClass
					&& !Utility.isShuffleCategory(category.getCategoryTpe()))
				selectionInfoMap.put(iRBTConstant.PACK, cos.getCosId());
			
			
			if (RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") || RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_SEL_OFFER, "FALSE")) {
				// RBT-14504 - My downloads change not working when free friday
				// offer enabled.if the same wav file is already available then
				// no need to hot the offer.
				boolean isNewHitForSongOffer = true;
				SubscriberDownloads[] activeSubscriberDownloads = rbtDBManager
						.getActiveSubscriberDownloads(subscriberID);
				Clip clipObj = getClip(task);
				if (clipObj != null && activeSubscriberDownloads != null) {
					for (SubscriberDownloads subDwn : activeSubscriberDownloads) {
						if (subDwn.promoId().equalsIgnoreCase(
								clipObj.getClipRbtWavFile())) {
							isNewHitForSongOffer = false;
							break;
						}
					}
				}
				logger.info("isNewHitForSongOffer: " + isNewHitForSongOffer);
				if (!task.containsKey(param_offerID) && isNewHitForSongOffer) {
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
					rbtDetailsRequest.setMode(task.getString(param_mode));
					rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
					Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
					
					if(offers != null && offers.length != 0) {
						useUIChargeClass = true;						
						classType = offers[0].getSrvKey();
						task.put(param_offerID, offers[0].getOfferID());
						task.put(param_useUIChargeClass,YES);
						selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offers[0].getOfferID());
					}
				}
				
				String offerId = task.getString(param_offerID);
				String offerDaysLimit = RBTParametersUtils.getParamAsString("COMMON",
						iRBTConstant.OFFER_DAYS_LIMIT, "");
				Date date = new Date();
				logger.info("Going for Selection Offer....");
				logger.info("SubscriberID = " + subscriberID + " , Mode = "
						+ task.getString(param_mode) + " , Offer Type = 2(Selection) , ClassType = "
						+ classType);
				boolean isActPendingRecord = false;
				if (activeSubscriberDownloads != null && offerId!=null && !offerId.equalsIgnoreCase("-1")) {
					for (SubscriberDownloads subDwn : activeSubscriberDownloads) {
						String xtraInfo = subDwn.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(xtraInfo);
						char dwnStatus = subDwn.downloadStatus();
						if ((dwnStatus == 'n' || dwnStatus == 'p') && xtraInfoMap != null
								&& xtraInfoMap.containsKey("OFFER_ID")
								&& Arrays.asList(offerDaysLimit.split(","))
								.contains(date.getDay() + "")) { 
							isActPendingRecord = true;
							break;
						}
					}
				}
				if (isActPendingRecord) {
					logger.info("Already one download is pending to be charged.So, Not allowing to download/selection the song");
					return NOT_ALLOWED;
				}

				String offerIdForBlacklisted = RBTParametersUtils.getParamAsString("COMMON",
						"SEL_OFFER_ID_FOR_BLACKLISTED_SUBSCRIBER", "");

				if (offerId != null && Arrays.asList(offerIdForBlacklisted.split(",")).contains(
						offerId)) {
					String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
							"ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriberID);
					String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("OFFER",
							"BLACKLISTED_OFFER_SEL_TEXT", language);
					task.put(param_smsText, smsText);
					sendSMS(task);
					return SUCCESS;
				}
				
				if(offerId != null && !offerId.equalsIgnoreCase("-1")){
				    selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				}
				
			}
			
			
			//RBT2.0 changes
			String downloadResponse =null;
			String downloadStatus = null;
			if ((task.containsKey(param_isDirectActivation) && task.get(param_isDirectActivation).equals(YES))
					|| (task.containsKey(param_selDirectActivation)
							&& task.get(param_selDirectActivation).equals(YES))) {
				downloadStatus = "y";
			}
	 
			downloadResponse = rbtDBManager.addSubscriberDownloadRW(
						subscriberID, clip.getClipRbtWavFile(), categoriesObj,
						null, isSubActive, classType, selectedBy, selectionInfo,
						selectionInfoMap, incrSelCount, useUIChargeClass, false,
						null, downloadStatus);
			

			

			response = Utility.getResponseString(downloadResponse);

			boolean removeGiftIfAlreadyExists = RBTParametersUtils
					.getParamAsBoolean(iRBTConstant.COMMON,
							"REMOVE_GIFT_IF_SAME_SONG_EXISTS", "true");
			if (response.equals(SUCCESS)
					|| (removeGiftIfAlreadyExists && response
							.equals(ALREADY_ACTIVE))) {
				if (action.equalsIgnoreCase(action_acceptGift)) {
					String gifterID = task.getString(param_gifterID);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task
							.getString(param_giftSentTime));

					rbtDBManager.updateViralPromotion(gifterID, subscriberID,
							sentTime, "GIFTED", "ACCEPT_ACK", new Date(), null,
							null);
				}
			}

			if (response.equals(SUCCESS)) {
				sendAcknowledgementSMS(task, "DOWNLOAD");
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Download tone response: " + response + ", for subscriberID: "
				+ subscriberID);

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#deleteTone(com
	 * .onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String deleteTone(WebServiceContext task) {
		String response = ERROR;

		try {
			String action = task.getString(param_action);
			String subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);
			
			boolean isDirectDeactivation = YES.equalsIgnoreCase(task.getString(param_isDirectDeactivation));
			
			String promoID = null;
			int categoryID;
			int categoryType;

			Subscriber subscriber = null;
			if (task.containsKey(param_subscriber))
				subscriber = (Subscriber) task.get(param_subscriber);
			else
				subscriber = rbtDBManager.getSubscriber(subscriberID);
			String subStatus = Utility.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE,
						getClip(task), getCriteria(task));
				return subStatus;
			}

			if (action.equalsIgnoreCase(action_deleteTone)
					|| action.equalsIgnoreCase(action_deleteMultipleTones)) {
				
				
				boolean  toBeSupportDctReqForActPendingUser = RBTParametersUtils.getParamAsBoolean(iRBTConstant.WEBSERVICE, "TO_BE_SUPPORT_SONG_DCT_REQ_FOR_ACT_PENDING_USER", "false");
				
				if(toBeSupportDctReqForActPendingUser && subscriber != null && (rbtDBManager.isSubscriberActivationPending(subscriber) || 
						rbtDBManager.isSubscriberInGrace(subscriber))) {
					logger.info("Dont accept deactivaion record becuase subscriberId: " + subscriberID + " subscriber is in : " + subStatus);
					return FAILED;
				}
				
				
				if (task.containsKey(param_clipID)) {
					int clipID = Integer.parseInt(task.getString(param_clipID));
					Clip clip = rbtCacheManager.getClip(clipID,
							browsingLanguage);
					if (clip == null) {
						logger.info("Clip not in download or wrong clip ID: response : "
								+ FAILED);
						return FAILED;
					}
					promoID = clip.getClipRbtWavFile();
				} else if (task.containsKey(param_rbtFile)) {
					String rbtFile = task.getString(param_rbtFile);
					if (rbtFile.toLowerCase().endsWith(".wav"))
						promoID = rbtFile.substring(0, rbtFile.length() - 4);
					else
						promoID = rbtFile;
				}

				categoryID = (task.containsKey(param_categoryID)) ? Integer
						.parseInt(task.getString(param_categoryID)) : -1;
			} else {
				SubscriberDownloads subscriberDownload = rbtDBManager
						.getOldestActiveSubscriberDownload(subscriberID);
				if (subscriberDownload == null) {
					logger.info("response: " + NO_DOWNLOADS);
					writeEventLog(subscriberID, getMode(task), "203", PURCHASE,
							getClip(task), getCriteria(task));
					return NO_DOWNLOADS;
				}

				promoID = subscriberDownload.promoId();
				categoryID = subscriberDownload.categoryID();
			}

			Category category = rbtCacheManager.getCategory(categoryID,
					browsingLanguage);
			categoryType = (category != null) ? category.getCategoryTpe() : -1;
			String deactivatedBy = getMode(task);
			String deselectionInfo = task.getString(param_modeInfo);

			String selType = task.getString(param_selectionType);
			if (selType == null || !selType.equals("2")) {
				SubscriberStatus[] subscriberStatus = rbtDBManager
						.getActiveSelectionsByType(subscriberID, 2);
				if (subscriberStatus != null) {
					for (SubscriberStatus selection : subscriberStatus) {
						if (selection.subscriberFile()
								.equalsIgnoreCase(promoID))
							return FAILED;
					}
				}
			}

			SubscriberDownloads subDownload = rbtDBManager
					.getDownloadToBeDeactivated(subscriberID, promoID,
							categoryID, categoryType);
			String dwnExtraInfo  = null;
			if(subDownload!=null)
			     dwnExtraInfo = subDownload.extraInfo();
			HashMap<String, String> attributeMapFromXML = DBUtility.getAttributeMapFromXML(dwnExtraInfo);
			if(attributeMapFromXML == null){
				attributeMapFromXML = new HashMap<String,String>();
			}
			Set<Entry<String, Object>> entrySet = task.entrySet();
			for (Entry<String, Object> entry : entrySet){
				if (entry.getKey().startsWith(param_selectionInfo + "_")){
					attributeMapFromXML.put(entry.getKey().substring(
									entry.getKey().indexOf('_') + 1),
							(String) entry.getValue());
				}
			}
			dwnExtraInfo = DBUtility.getAttributeXMLFromMap(attributeMapFromXML);
			
			// RBT-10785
			boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
					"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
			if (addProtocolNumber) {
				deselectionInfo = appendProtocolNumber(subscriberID,
						deselectionInfo);
			}
			
			boolean result = false;
			if (subDownload != null) {
				if (dwnExtraInfo != null) {
					result = rbtDBManager.expireSubscriberDownloadAndUpdateExtraInfo(subscriberID, promoID,
							categoryID, categoryType, deactivatedBy, deselectionInfo,dwnExtraInfo,isDirectDeactivation);
				} else {
					result = rbtDBManager.expireSubscriberDownload(subscriberID, promoID,
							categoryID, categoryType, deactivatedBy, deselectionInfo, isDirectDeactivation);
				}
			}

			if (result) {
				writeEventLog(subscriberID, getMode(task), "0", PURCHASE,
						getClip(task), getCriteria(task));
				response = SUCCESS;
				if (rbtDBManager.isDownloadActivationPending(subDownload)
						|| rbtDBManager.isDownloadGrace(subDownload)) {
					Map<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(subDownload.extraInfo());
					if (extraInfoMap != null
							&& extraInfoMap.containsKey(iRBTConstant.PACK)) {
						String cosID = extraInfoMap.get(iRBTConstant.PACK);
						CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
//						Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//								.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
//						
//						List<String> musicPackCosIdList = null;
//						
//						if(muiscPackCosIdParam != null) {
//							musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//						}
						
						List<ProvisioningRequests> provRequest = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriber.subID(),
										Integer.parseInt(cosID));
						if (provRequest != null && (packCos == null || !packCos.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
							rbtDBManager.decrementNumMaxSelectionsForPack(
									subscriberID, cosID);
						}
					} else {
						rbtDBManager.decrementNumMaxSelections(subscriberID);
					}
				}
			} else {
				writeEventLog(subscriberID, getMode(task), "201", PURCHASE,
						getClip(task), getCriteria(task));
				response = FAILED;
			}

			if (response.equalsIgnoreCase(SUCCESS)) {
				sendAcknowledgementSMS(task, "DELETE_TONE");
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processNormalScratchCard(WebServiceContext task) {
		String response = ERROR;

		try {

			String subscriberID = task.getString(param_subscriberID);
			logger.info("Subscriberid is" + subscriberID);
			Date sysdate = new Date();
			String scratchno = task.getString(param_scratchNo);
			logger.info("scratch is" + scratchno);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			logger.info("subscriber is" + subscriber);
			Scratchcard scratch = rbtDBManager.getScratchcard(scratchno);
			// if scratch card not available return error,or user invalid ,
			// failed upgradation failed , expired , INvalid_predix , suspended,
			// blacklisted
			if (scratch != null) {
				task.put(param_subscriptionClass, scratch.getSubClass());
				Date end = scratch.getEndDate();
				task.put(param_ScratchCard, scratch);
				// String circleId = rbtDBManager.getCircleId(subscriberID);
				String circleId = DataUtils.getUserCircle(task);
				task.put(param_circleID, circleId);
				response = isValidUser(task, subscriber);
				if (!response.equals(VALID)) {
					logger.info("response: " + response);
					return response;
				}
				response = ERROR;

				if (sysdate.after(end)) {
					rbtDBManager.updateScratchCard(scratchno, "1");
					scratch.setState("1");
					task.put(param_ScratchCard, scratch);

				}
				if (subscriberID != null) {

					logger.info("circleid is" + circleId);

				}
				if (scratch.getState().equals("0")) {
					logger.info("im in here");

					// the subscriber info has to be set
					if (subscriber != null && !subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED)) {						
						logger.info("im in here1");

						String upgrade = RBTParametersUtils.getParamAsString("COMMON", "SCRATCHCARD_SUPPORTS_BASE_UPGRADE", "TRUE");
						if(upgrade.equalsIgnoreCase("TRUE")) {
							task.put(param_rentalPack, "");
							response = processActivation(task);
						}
						else {
							response = "SUCCESS";
						}
					} else {
						logger.info("im in here2");
						response = processActivation(task);
					}
				}
			}

			if (response.equalsIgnoreCase("SUCCESS")) {
				rbtDBManager.updateScratchCard(scratchno, "2");
				scratch.setState("2");
				task.put(param_ScratchCard, scratch);

			}

		} catch (Exception e) {
			logger.error("", e);
			response = TECHNICAL_DIFFICULTIES;
		}
		return response;

	}

	@Override
	public String processSngActivation(WebServiceContext task) {
		String response = ERROR;
		try {
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);
			String userID = task.getString(param_userId);
			String sngId = task.getString(param_sngId);
			String rbtType = task.getString(param_rbtType);
			boolean allInOndeUpdateModel = false;
			String paramVal = RBTConnector.getInstance().getRbtGenericCache()
					.getParameter("SRBT", "ALL_IN_ONE_UPDATE_MODEL", "false");
			if (paramVal != null && paramVal.equalsIgnoreCase("true")) {
				allInOndeUpdateModel = true;
			}
			if (rbtType == null || rbtType.equalsIgnoreCase("")
					|| rbtType.equalsIgnoreCase("null")) {
				rbtType = RBT;
			}
			boolean validSNGRequest = false;
			validSNGRequest = isValidSNGRequest(task, allInOndeUpdateModel);
			if (!validSNGRequest) {
				// return ERROR for invalid request
				response = FAILED;
				logger.info("response: " + response);
				return response;
			}

			int modeVal = 1;
			if (allInOndeUpdateModel) {
				modeVal = SRBTUtility.getSocialRBTMode(mode);
			}
			userID = userID.trim();
			subscriberID = subscriberID.trim();

			String activatedBy = getMode(task);

			boolean result = false;
			if (allInOndeUpdateModel) {
				result = RBTHibernateDBImplementationWrapper
						.getInstance()
						.activateSNGUser(userID, subscriberID, rbtType, modeVal);
			} else {

				result = SRBTDaoWrapper.activateSNGUser(userID, subscriberID,
						sngId, activatedBy);
			}
			if (result) {
				response = SUCCESS;
				String circleId = SRBTUtility.getCircleId(subscriberID);
				SocialRBTEventLogger.sngActivationEventLog(userID,
						subscriberID, RBT, activatedBy, modeVal, circleId);
			} else {
				response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processSngUserUpdate(WebServiceContext task) {
		String response = ERROR;
		logger.info("entering: processSngUserUpdate");
		try {
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);
			String userID = task.getString(param_userId);
			String rbtType = task.getString(param_rbtType);
			if (rbtType == null || rbtType.equalsIgnoreCase("")
					|| rbtType.equalsIgnoreCase("null")) {
				rbtType = RBT;
			}
			boolean allInOndeUpdateModel = false;
			String paramVal = RBTConnector.getInstance().getRbtGenericCache()
					.getParameter("SRBT", "ALL_IN_ONE_UPDATE_MODEL", "false");
			if (paramVal != null && paramVal.equalsIgnoreCase("true")) {
				allInOndeUpdateModel = true;
			}
			boolean validSNGRequest = false;
			validSNGRequest = isValidSNGRequest(task, allInOndeUpdateModel);
			if (!validSNGRequest) {
				// return ERROR for invalid request
				response = FAILED;
				logger.info("response: " + response);
				return response;
			}

			int modeVal = SRBTUtility.getSocialRBTMode(mode);

			userID = userID.trim();
			subscriberID = subscriberID.trim();

			String activatedBy = getMode(task);

			activatedBy = activatedBy + "update MSISDN";
			boolean result = false;

			result = RBTHibernateDBImplementationWrapper.getInstance()
					.updateSNGUser(userID, subscriberID, rbtType, modeVal);

			if (result) {
				response = SUCCESS;
				String circleId = SRBTUtility.getCircleId(subscriberID);
				SocialRBTEventLogger.sngActivationEventLog(userID,
						subscriberID, RBT, activatedBy, modeVal, circleId);
			} else {
				response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processSngDeactivation(WebServiceContext task) {
		String response = ERROR;
		try {
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);
			String userID = task.getString(param_userId);
			String sngId = task.getString(param_sngId);
			boolean allInOndeUpdateModel = false;
			String paramVal = RBTConnector.getInstance().getRbtGenericCache()
					.getParameter("SRBT", "ALL_IN_ONE_UPDATE_MODEL", "false");
			if (paramVal != null && paramVal.equalsIgnoreCase("true")) {
				allInOndeUpdateModel = true;
			}
			boolean validSNGRequest = false;
			validSNGRequest = isValidSNGRequest(task, allInOndeUpdateModel);
			if (!validSNGRequest) {
				// return ERROR for invalid request
				logger.info("!validSNGRequest");
				response = FAILED;
				logger.info("response: " + response);
				return response;
			}
			int modeVal = 1;
			if (allInOndeUpdateModel) {
				modeVal = SRBTUtility.getSocialRBTMode(mode);
			}
			userID = userID.trim();
			subscriberID = subscriberID.trim();

			String deactivatedBy = getMode(task);

			boolean result = false;
			if (allInOndeUpdateModel) {
				result = RBTHibernateDBImplementationWrapper.getInstance()
						.deactivateSNGUser(userID, modeVal);
			} else {

				result = SRBTDaoWrapper.deactivateSNGUser(userID, subscriberID,
						sngId, deactivatedBy);
			}
			if (result) {
				response = SUCCESS;
				String circleId = SRBTUtility.getCircleId(subscriberID);
				SocialRBTEventLogger.sngDeactivationEventLog(userID,
						subscriberID, RBT, deactivatedBy, modeVal, circleId);
			} else {
				response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processSngAllDeativation(WebServiceContext task) {
		String response = ERROR;
		try {
			String mode = task.getString(param_mode);
			String userID = task.getString(param_userId);
			String sngId = task.getString(param_sngId);
			boolean allInOndeUpdateModel = false;
			boolean validSNGRequest = false;
			validSNGRequest = isValidSNGRequest(task, allInOndeUpdateModel);
			if (!validSNGRequest) {
				// return ERROR for invalid request
				logger.info("!validSNGRequest");
				response = FAILED;
				logger.info("response: " + response);
				return response;
			}
			int modeVal = 1;
			if (allInOndeUpdateModel) {
				modeVal = SRBTUtility.getSocialRBTMode(mode);
			}
			userID = userID.trim();

			String deactivatedBy = getMode(task);

			boolean result = false;
			result = SRBTDaoWrapper.deactivateAllSNGUser(userID, sngId,
					deactivatedBy);
			if (result) {
				response = SUCCESS;
				SocialRBTEventLogger.sngDeactivationEventLog(userID, "ALL",
						RBT, deactivatedBy, modeVal, "ALL");
			} else {
				response = FAILED;
			}
		} catch (Exception e) {
			logger.fatal("got exception", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#shuffleDownloads
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String shuffleDownloads(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);

			SubscriberStatus[] settings = rbtDBManager
					.getAllActiveSubscriberSettings(subscriberID);
			SubscriberDownloads[] subscriberDownloads = rbtDBManager
					.getActiveSubscriberDownloads(subscriberID);
			if (subscriberDownloads == null || subscriberDownloads.length == 0)
				return NO_DOWNLOADS;

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			task.put(param_subscriber, subscriber);

			String circleID = DataUtils.getUserCircle(task);
			boolean isPrepaid = DataUtils.isUserPrepaid(task);
			String subYes = subscriber.subYes();

			String callerID = null;
			Calendar endCal = Calendar.getInstance();
			endCal.set(2037, 0, 1);
			Date endDate = endCal.getTime();
			Date startDate = null;

			int status = 1;
			int fromTime = 0;
			int toTime = 2359;

			String selectedBy = getMode(task);
			String selectionInfo = getModeInfo(task);
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);

			boolean useSubManager = true;

			String messagePath = null;
			Parameters messagePathParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "MESSAGE_PATH", null);
			if (messagePathParam != null)
				messagePath = messagePathParam.getValue().trim();

			boolean changeSubType = true;

			boolean inLoop = true;
			String classType = null;
			String chargingPackage = null;
			String interval = null;
			boolean useUIChargeClass = false;

			/*
			 * RBT-6570: Reject the randomization request if the user has set
			 * his corporate selection for full day.
			 */
			boolean isCorpSongSetForFullDay = false;
			if (!RBTParametersUtils.getParamAsBoolean("COMMON",
					"ALLOW_RANDOMIZATION_FOR_FULL_DAY_CORP_USER", "TRUE")) {
				SubscriberStatus[] selections = rbtDBManager
						.getAllActiveSubscriberSettings(subscriberID);
				for (SubscriberStatus subscriberStatus : selections) {
					if (subscriberStatus.selType() == 2
							&& subscriberStatus.status() == 1) {
						isCorpSongSetForFullDay = true;
						break;
					}
				}
			}

			if (isCorpSongSetForFullDay) {
				logger.info("Corporate song is set for full day, so rejecting the randomization request : "
						+ NOT_ALLOWED);
				return NOT_ALLOWED;
			}

			for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
				if (subscriberDownload.downloadStatus() == 'y') {
					String promoID = subscriberDownload.promoId();

					boolean isDefault = false;
					boolean isPersonalized = false;
					if (settings != null) {
						for (SubscriberStatus setting : settings) {
							if (promoID.equalsIgnoreCase(setting
									.subscriberFile())) {
								if (setting.callerID() == null) {
									isDefault = true;
									// break;
								} else {
									isPersonalized = true;
								}
							}
						}
					}

					if (!isDefault) {
						// Added for RBT-6380
						String ignorePersonalizedSettings = parametersCacheManager
								.getParameterValue(
										iRBTConstant.WEBSERVICE,
										"IGNORE_PERSONALIZED_SETTINGS_IN_SHUFFLE",
										"FALSE");
						if (ignorePersonalizedSettings.equalsIgnoreCase("true")
								&& isPersonalized) {
							logger.debug("Download "
									+ subscriberDownload.promoId()
									+ " has personalized selecction, not adding in shuffle");
							continue;
						}

						String browsingLanguage = task
								.getString(param_browsingLanguage);
						Category category = rbtCacheManager.getCategory(
								subscriberDownload.categoryID(),
								browsingLanguage);
						Categories categoriesObj = CategoriesImpl
								.getCategory(category);

						Clip clip = rbtCacheManager.getClipByRbtWavFileName(
								promoID, browsingLanguage);
						HashMap<String, Object> clipMap = new HashMap<String, Object>();
						if (clip != null) {
							clipMap.put("CLIP_CLASS", clip.getClassType());
							clipMap.put("CLIP_END", clip.getClipEndTime());
							clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
							clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
							clipMap.put("CLIP_ID",
									String.valueOf(clip.getClipId()));
							clipMap.put("CLIP_NAME", clip.getClipName());
						}

						String selResponse = null;
						// if (getParamAsBoolean(iRBTConstant.COMMON,
						// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
						if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
							HashMap<String, String> responseParams = new HashMap<String, String>();
							String offerID = getOfferID(task,
									SELECTION_OFFERTYPE);
							selectionInfoMap.put(param_offerID, offerID);
							classType = task.getString(param_chargeClass);
							selResponse = rbtDBManager
									.smAddSubscriberSelections(subscriberID,
											callerID, categoriesObj, clipMap,
											null, startDate, endDate, status,
											selectedBy, selectionInfo, 0,
											isPrepaid, changeSubType,
											messagePath, fromTime, toTime,
											classType, useSubManager, true,
											"VUI", chargingPackage, subYes,
											null, circleID, true, false, null,
											false, false, inLoop,
											subscriber.subscriptionClass(),
											subscriber, 0, interval,
											selectionInfoMap, responseParams);

							if (selResponse
									.equals(iRBTConstant.SELECTION_SUCCESS)) {
								RBTSMClientResponse smClientResponse = null;
								boolean isBulkTask = task
										.containsKey(param_fromBulkTask);

								HashMap<String, String> extraParams = new HashMap<String, String>();
								extraParams
										.put(RBTSMClientHandler.EXTRA_PARAM_USERINFO,
												getSelectionUserInfo(
														subscriberID, clip,
														category,
														selectionInfo,
														subscriber.cosID(),
														callerID,
														selectionInfoMap));
								String selectionRefID = "";
								if (responseParams.containsKey("REF_ID")) {
									selectionRefID = responseParams
											.get("REF_ID");
								}

								StringBuilder builder = new StringBuilder(
										"SM client request for add selection ");
								builder.append("[ subID " + subscriberID);
								builder.append(", prepaidYes "
										+ subscriber.prepaidYes());
								builder.append(", chargeClass " + classType);
								builder.append(", subscription class "
										+ subscriber.subscriptionClass());
								builder.append(", Selection offerID " + offerID);
								builder.append(", selectedBy - " + selectedBy);
								builder.append(", refId - " + selectionRefID);
								builder.append(", extraParams - " + extraParams);
								builder.append(", isBulkTask - " + isBulkTask);
								logger.info(builder.toString());

								smClientResponse = RBTSMClientHandler
										.getInstance().activateSelection(
												subscriberID, isPrepaid,
												classType, offerID, selectedBy,
												selectionRefID, extraParams,
												isBulkTask);
								logger.info("SMClient Response for activate Request"
										+ smClientResponse.toString());
								if (!smClientResponse.getResponse()
										.equalsIgnoreCase(
												RBTSMClientResponse.SUCCESS))
									rbtDBManager.removeSelection(subscriberID,
											selectionRefID);
							}
						} else {
							selResponse = rbtDBManager.addSubscriberSelections(
									subscriberID, callerID, categoriesObj,
									clipMap, null, startDate, endDate, status,
									selectedBy, selectionInfo, 0, isPrepaid,
									changeSubType, messagePath, fromTime,
									toTime, classType, useSubManager, true,
									"VUI", chargingPackage, subYes, null,
									circleID, true, false, null, false, false,
									inLoop, subscriber.subscriptionClass(),
									subscriber, 0, interval, selectionInfoMap,
									useUIChargeClass, null, false);
						}

						response = Utility.getResponseString(selResponse);
						
						if (!response.equalsIgnoreCase(SUCCESS)
								&& !response
										.equalsIgnoreCase(SUCCESS_DOWNLOAD_EXISTS))
							break;
					}
				}
			}
		
			// RBT-6645:-SMS CONFIRMATION POST RANDOMIZATION AND WC TUNE ON/OFF
			if (response.equalsIgnoreCase(SUCCESS)
					|| response.equalsIgnoreCase(SUCCESS_DOWNLOAD_EXISTS)) {
				sendAcknowledgementSMS(task, "RANDOMIZED");
			}
		} catch (Exception e) {
			logger.error("", e);
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String upgradeSpecialSelectionPack(WebServiceContext task) {
		String response = ERROR;
		logger.info(" In upgrade special selection pack ");
		try {
			String cosID = task.getString(param_cosID);
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosID);
			if (cos == null) {
				logger.info("Invalid cosID. Returning response: "
						+ COS_NOT_EXISTS);
				return COS_NOT_EXISTS;
			}

			response = processActivation(task);
			if (!response.equals(SUCCESS) && !Utility.isUserActive(response))
				return response;
			else if (Utility.isUserActive(response)
					&& task.containsKey(param_ignoreActiveUser)
					&& task.getString(param_ignoreActiveUser).equalsIgnoreCase(
							YES))
				return response;

			if (!task.containsKey(param_activatedNow)) {
				if (getParamAsBoolean("SMS", "ALLOW_SONG_PACK_UPGRADE", "FALSE")) {
					logger.info("calling processactivation to upgrade subscriber");
					task.put(param_rentalPack, cos.getSubscriptionClass());
					task.put(param_packCosId, cos.getCosId());
					task.remove(param_cosID);
					response = processActivation(task);
				} else {
					response = NOT_ALLOWED;

				}
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#upgradeSelectionPack
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String upgradeSelectionPack(WebServiceContext task) {
		String response = ERROR;

		try {
			String cosID = task.getString(param_cosID);
			String mode = task.getString(param_mode);
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosID);
			if (cos == null
					|| cos.getCosType() == null
					|| (!cos.getCosType().equalsIgnoreCase(
							iRBTConstant.SONG_PACK)
							&&!cos.getCosType().equalsIgnoreCase(
									iRBTConstant.AZAAN)
							&& !cos.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS) && !cos
							.getCosType().equalsIgnoreCase(
									iRBTConstant.LIMITED_DOWNLOADS)
							&&!cos.getCosType().equalsIgnoreCase(iRBTConstant.MUSIC_POUCH) && !cos.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) && !cos.getCosType().equalsIgnoreCase(
											iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
				logger.info("Invalid cosID. Returning response: "
						+ COS_NOT_EXISTS);
				return COS_NOT_EXISTS;
			}

			String cosType = cos.getCosType();
			if (cosType.equalsIgnoreCase(iRBTConstant.SONG_PACK)
					|| cosType.equalsIgnoreCase(iRBTConstant.AZAAN)
					|| cos.getCosType().equalsIgnoreCase(
							iRBTConstant.LIMITED_DOWNLOADS)
					|| cos.getCosType().equalsIgnoreCase(
							iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)
					|| cos.getCosType().equalsIgnoreCase(
							iRBTConstant.MUSIC_POUCH)
					|| cos.getCosType().equalsIgnoreCase(
							iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
				response = processActivation(task);
				if (!response.equals(SUCCESS)
						&& !Utility.isUserActive(response))
					return response;
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser)
								.equalsIgnoreCase(YES))
					return response;

				task.put(param_packCosId, cos.getCosId() );
				if (!task.containsKey(param_activatedNow) && !task.containsKey(param_activatedPackNow)) {
					String cosValidity = cos.getValidDays() + "";
					Subscriber subscriber = (Subscriber) task
							.get(param_subscriber);
					// Song pack upgradation will happen only if sent cos is
					// different than subscriber cos
					if (subscriber.subYes()
							.equals(iRBTConstant.STATE_ACTIVATED)
							&& !cosID.equals(subscriber.cosID())) {
						if (cos != null && cos.getEndDate().after(new Date())) {
							if (rbtDBManager.isPackActivated(subscriber, cos)) {
								return PACK_ALREADY_ACTIVE;
							} else {
								
								String newPack = task.getString(param_packCosId);
								int packNumMaxSelection = -1;								
								if(isDownloadSongPackOverLimitReached(newPack, subscriber.subID(), task)) {
									return OVERLIMIT;
								}
								
								if(task.containsKey("PACK_NUM_MAX_SELECTON")) {
									packNumMaxSelection = Integer.parseInt((String)task.remove("PACK_NUM_MAX_SELECTON"));
								}
								
								
								HashMap<String, String> existingExtraInfoMap = rbtDBManager
										.getExtraInfoMap(subscriber);
								String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
										.get(iRBTConstant.EXTRA_INFO_PACK)
										: null;
								
								String updatedPacks = (existingPacks != null) ? existingPacks
										+ "," + newPack
										: newPack;
								rbtDBManager.updateExtraInfo(
										subscriber.subID(),
										iRBTConstant.EXTRA_INFO_PACK,
										updatedPacks);
								task.remove(param_subscriber);
								task.put(param_subscriber,
										DataUtils.getSubscriber(task));

								String chargingClass = (cos != null) ? cos
										.getSmsKeyword() : null;
								int type = Integer.parseInt(cosID);
								int packStatus = rbtDBManager
										.getPackStatusToInsert(subscriber);
								String packMode = getMode(task);
								String modeInfo = getModeInfo(task);
								String transId = UUID.randomUUID().toString();
								/*
								 * If offerId is present in the task, then
								 * create and put offerId into the extra info
								 * map and again put extra info map into the
								 * task. Remove the offer id from the task.
								 */
								String offerId = task
										.getString(param_packOfferID);
								String packExtraInfoXml = null;
								if (offerId != null) {
									Map<String, String> packExtraInfoMap = new HashMap<String, String>();
									packExtraInfoMap.put(
											iRBTConstant.EXTRA_INFO_OFFER_ID,
											offerId);
									packExtraInfoXml = DBUtility
											.getAttributeXMLFromMap(packExtraInfoMap);
								}
								ProvisioningRequests provisioningReqs = new ProvisioningRequests(
										subscriber.subID(), type, packMode,
										modeInfo, transId, chargingClass,
										packStatus);
								provisioningReqs.setExtraInfo(packExtraInfoXml);
								if(packNumMaxSelection != -1) {
									provisioningReqs.setNumMaxSelections(packNumMaxSelection);
								}
								if (rbtDBManager
										.insertProvisioningRequestsTable(provisioningReqs) != null) {
									return SUCCESS;
								}
								return FAILED;
							}
						}

						// boolean status = rbtDBManager.upgradeToSongPack(
						// subscriber, cosID, cosValidity , mode);
						// if (status) {
						// // Updating the subscriber object so that same will
						// // be reflected while adding selection
						// subscriber.setCosID(cosID);
						// subscriber.setSubYes(iRBTConstant.STATE_CHANGE);
						// task.put(param_subscriber, subscriber);
						//
						// response = SUCCESS;
						// } else
						// response = FAILED;
					} else if (response.equals(ACTIVE)) {
						// Pack upgradation pending(STATE_CHANGE) and
						// voluntarily suspended users are considered as active
						// users.
						// But not allowed to for subscription upgradation,
						// that's why returning response as ACT_PENDING.
						response = ACT_PENDING;
					}
				}
			} else if (cosType
					.equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS)) {
				// If user is inactive the cosID has to be put into the
				// EXTRA_INFO and user has to be activated normally
				task.remove(param_cosID);
				task.put(param_userInfo + "_" + iRBTConstant.EXTRA_INFO_COS_ID,
						cosID);

				if (task.containsKey(param_offerID)) {
					task.put(param_userInfo + "_"
							+ iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID,
							task.getString(param_offerID));
					task.remove(param_offerID);
				} else {
					// If offerID is not passed, then web service will get the
					// offerID from SM for unlimited downloads pack (offerType
					// 6)
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
							task.getString(param_subscriberID));
					rbtDetailsRequest.setOfferType("6");
					Offer[] offers = RBTClient.getInstance().getOffers(
							rbtDetailsRequest);
					if (offers == null || offers.length == 0) {
						logger.info("No offers, returning " + FAILED);
						return FAILED;
					}

					task.put(param_userInfo + "_"
							+ iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID,
							offers[0].getOfferID());
				}

				response = processActivation(task);
				if (!response.equals(SUCCESS)
						&& !Utility.isUserActive(response))
					return response;
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser)
								.equalsIgnoreCase(YES))
					return response;

				if (!task.containsKey(param_activatedNow)) {
					Subscriber subscriber = (Subscriber) task
							.get(param_subscriber);
					if (subscriber.subYes()
							.equals(iRBTConstant.STATE_ACTIVATED)) {
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(subscriber.extraInfo());
						if (extraInfoMap != null
								&& extraInfoMap
										.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)) {
							cos = CacheManagerUtil
									.getCosDetailsCacheManager()
									.getCosDetail(
											extraInfoMap
													.get(iRBTConstant.EXTRA_INFO_COS_ID));
							if (cos != null
									&& cos.getCosType() != null
									&& cos.getCosType().equalsIgnoreCase(
											iRBTConstant.UNLIMITED_DOWNLOADS)) {
								logger.info("response: " + ALREADY_ACTIVE);
								return ALREADY_ACTIVE;
							}
						}

						if (extraInfoMap == null)
							extraInfoMap = new HashMap<String, String>();

						extraInfoMap.put(iRBTConstant.EXTRA_INFO_COS_ID, cosID);
						extraInfoMap
								.put(iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID,
										task.getString(param_userInfo
												+ "_"
												+ iRBTConstant.EXTRA_INFO_TOBE_ACT_OFFER_ID));
						String extraInfo = DBUtility
								.getAttributeXMLFromMap(extraInfoMap);

						Map<String, String> attributeMap = new HashMap<String, String>();
						attributeMap.put("SUBSCRIPTION_YES",
								iRBTConstant.STATE_CHANGE);
						attributeMap.put("EXTRA_INFO", extraInfo);
						String status = rbtDBManager.updateSubscriber(
								subscriber.subID(), attributeMap);
						if (status.equalsIgnoreCase("SUCCESS"))
							response = SUCCESS;
						else
							response = FAILED;
					} else if (response.equals(ACTIVE)) {
						// Pack upgradation pending(STATE_CHANGE) and
						// voluntarily suspended users are considered as active
						// users.
						// But not allowed to for subscription upgradation,
						// that's why returning response as ACT_PENDING.
						response = ACT_PENDING;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#deactivateOffer
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String deactivateOffer(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (rbtDBManager.isSubscriberActivated(subscriber)) {
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (extraInfoMap != null
						&& extraInfoMap
								.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)) {
					String cosID = extraInfoMap
							.get(iRBTConstant.EXTRA_INFO_COS_ID);
					CosDetails cos = CacheManagerUtil
							.getCosDetailsCacheManager().getCosDetail(cosID);
					if (cos != null
							&& cos.getCosType() != null
							&& cos.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS)) {
						String url = parametersCacheManager.getParameterValue(
								iRBTConstant.DAEMON, "DEACTIVATION_URL", null);

						String info = "songname:|songcode:|null|moviename:|cli:all|CONTENT_ID:contentid=MISSING,catname=MISSING,actinfo=null,callerid=ALL,catid=MISSING|catname:MISSING";

						HashMap<String, String> requestParams = new HashMap<String, String>();
						requestParams.put("msisdn", subscriberID);
						requestParams.put("type",
								(subscriber.prepaidYes() ? "P" : "B"));
						requestParams.put("srvkey", "RBT_SEL_"
								+ cos.getSmsKeyword().toUpperCase());
						requestParams.put("refid", subscriberID + ":" + cosID);
						requestParams.put("expiry", "01-01-2037 00-00-00");
						requestParams.put("info", info);
						requestParams.put("mode", getMode(task));

						HttpParameters httpParameters = new HttpParameters(url);
						Utility.setSubMgrProxy(httpParameters);
						logger.info("httpParameters: " + httpParameters);

						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, requestParams);
						logger.info("httpResponse: " + httpResponse);

						if (httpResponse.getResponse().contains("SUCCESS"))
							response = SUCCESS;
						else
							response = FAILED;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#addBookMark(com
	 * .onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String addBookMark(WebServiceContext task) {
		String response = ERROR;

		try {
			String action = task.getString(param_action);
			String subscriberID = task.getString(param_subscriberID);
			String browsingLanguage = task.getString(param_browsingLanguage);

			int clipID = Integer.parseInt(task.getString(param_clipID));
			int categoryID = Integer.parseInt(task.getString(param_categoryID));

			if (action.equalsIgnoreCase(action_overwrite)) {
				response = removeBookMark(task);
				if (!response.equals(SUCCESS) && !response.equals(NO_BOOKMARKS)) {
					logger.info("response: " + response);
					return response;
				}
			}

			Category category = rbtCacheManager.getCategory(categoryID,
					browsingLanguage);

			int categoryType = category.getCategoryTpe();
			Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
			String promoID = clip.getClipRbtWavFile();

			String result = rbtDBManager.addSubscriberBookMark(subscriberID,
					promoID, categoryID, categoryType, getMode(task));
			response = Utility.getResponseString(result);
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#removeBookMark
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String removeBookMark(WebServiceContext task) {
		String response = ERROR;

		try {
			String action = task.getString(param_action);
			String subscriberID = task.getString(param_subscriberID);

			String promoID = null;
			if (action.equalsIgnoreCase(action_overwrite)) {
				SubscriberDownloads[] bookmarks = rbtDBManager
						.getSubscriberBookMarks(subscriberID);
				if (bookmarks == null || bookmarks.length == 0) {
					logger.info("response: " + NO_BOOKMARKS);
					return NO_BOOKMARKS;
				}

				promoID = bookmarks[0].promoId();
			} else {
				int clipID = Integer.parseInt(task.getString(param_clipID));
				String browsingLanguage = task
						.getString(param_browsingLanguage);
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);
				promoID = clip.getClipRbtWavFile();
			}

			boolean removed = rbtDBManager.removeSubscriberBookMark(
					subscriberID, promoID);
			if (removed)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processGroupRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processGroupRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_add)) {
				String groupName = task.getString(param_groupName);
				String predefinedGroupID = task
						.getString(param_predefinedGroupID);

				String addGroupResponse = null;
				if (null == subscriberID) {
					String groupId = task.getString(param_groupID);
					if(groupId != null && groupId.startsWith("G")) {
						groupId = groupId.substring(1);
					}
					addGroupResponse = rbtDBManager.addGroup(groupId, groupName, task);
				} else {
					addGroupResponse = rbtDBManager.addGroupForSubscriberID(
							predefinedGroupID, groupName, subscriberID, null);
				}
				response = Utility.getResponseString(addGroupResponse);
			} else if (action.equalsIgnoreCase(action_update)) {
				// Future use
				String groupName = task.getString(param_groupName);
				String groupIDStr = task.getString(param_groupID);
				if (groupName == null
						|| groupIDStr == null
						|| (groupName != null && groupName.equalsIgnoreCase("")))
					return response;
				int groupID = Integer.parseInt(groupIDStr.substring(1)); // Trimming
																			// 'G'
																			// from
																			// GroupId

				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}
				
				boolean updatedGroupName = rbtDBManager
						.updateGroupNameForGroupID(groupID, groupName,
								subscriberID);
				if (updatedGroupName)
					response = SUCCESS;
				else
					response = FAILED;
			} else if (action.equalsIgnoreCase(action_remove)) {
				String groupIDStr = task.getString(param_groupID);
				int groupID = Integer.parseInt(groupIDStr.substring(1)); // Trimming
																			// 'G'
																			// from
																			// groupID
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}

				String deactivatedBy = getMode(task);

				boolean remoovedGroup = rbtDBManager.deleteGroup(subscriberID,
						groupID, deactivatedBy);
				if (remoovedGroup)
					response = SUCCESS;
				else
					response = FAILED;
			}
			
			if(response.equalsIgnoreCase(SUCCESS)) {
				rbtDBManager.updatePlayerStatus(subscriberID, "A");
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processGroupRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processAffiliateGroupRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_add)) {
				String groupName = task.getString(param_groupName);
				String groupId = task.getString(param_groupID);
				String optName = task.getString(param_msisdn_operator);
				if(groupId != null && groupId.startsWith("G")) {
					groupId = groupId.substring(1);
				}
				String addGroupResponse = null;
				addGroupResponse = rbtDBManager.addAffiliateGroup(groupId, groupName, optName, task);
				response = Utility.getResponseString(addGroupResponse);
			} else if (action.equalsIgnoreCase(action_remove)) {
				String groupIDStr = task.getString(param_groupID);
				int groupID = Integer.parseInt(groupIDStr.substring(1)); // Trimming
																			// 'G'
																			// from
																			// groupID
				// groupId validating against user
				if (!isAffiliateGroupBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}

				String deactivatedBy = getMode(task);

				boolean remoovedGroup = rbtDBManager.deleteAffiliateGroup(subscriberID,
						groupID, deactivatedBy);
				if (remoovedGroup)
					response = SUCCESS;
				else
					response = FAILED;
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}


	/**
	 * Checking provided groupId is belongs to subscriber or not 
	 * @param subscriberID
	 * @param groupID
	 * @return
	 */
	private boolean isBelongsToSubscriber(String subscriberID,
			int groupID) {
		Groups group = rbtDBManager.getGroup(groupID);
		if (null != subscriberID && !subscriberID.equals(group.subID())) {
			logger.error("GroupId " + groupID
					+ " is not belongs to user " + subscriberID
					+ ". Actual user is " + group.subID());
			return false;
		}
		return true;
	}
	
	/**
	 * Checking provided groupId is belongs to subscriber or not 
	 * @param subscriberID
	 * @param groupID
	 * @return
	 */
	private boolean isAffiliateGroupBelongsToSubscriber(String subscriberID,
			int groupID) {
		RDCGroups group = rbtDBManager.getAffiliateGroup(groupID);
		if (null != subscriberID && !subscriberID.equals(group.subID())) {
			logger.error("GroupId " + groupID
					+ " is not belongs to user " + subscriberID
					+ ". Actual user is " + group.subID());
			return false;
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processGroupMemberRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processGroupMultipleMemberRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String groupIDStr = task.getString(param_groupID);
			String memberID = task.getString(param_memberID);
			String predefinedGroupIdStr = task
					.getString(param_predefinedGroupID);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_addMultipleMember)) {
				logger.info("Adding member into group. groupId: " + groupIDStr
						+ ", subscriberID: " + subscriberID);
				int groupID = -1;
				try {
					// Trimming 'G' from groupID
					groupID = Integer.parseInt(groupIDStr.substring(1));

					// if it is valid groupId then only validating against user
					if (!isBelongsToSubscriber(subscriberID, groupID)) {
						return response;
					}

				} catch (Exception e) {
					groupID = -1;
				}

				String memberName = task.getString(param_memberName);
				String groupName = task.getString(param_groupName);
				String predefinedGroupID = task
						.getString(param_predefinedGroupID);

				
				String addMemberResponse = null;
				addMemberResponse = rbtDBManager.addMultipleCallerInGroup(
						subscriberID, groupID, memberID, memberName,
						predefinedGroupID, groupName);

				response = Utility.getResponseString(addMemberResponse);
			} else if (action.equalsIgnoreCase(action_updateMultipleMember)) {
				
			} else if (action.equalsIgnoreCase(action_moveMultipleMember)) {
				
			} else if (action.equalsIgnoreCase(action_removeMultipleMember)) {
				if (groupIDStr == null && predefinedGroupIdStr != null) {
					Groups group = rbtDBManager.getGroupByPreGroupID(
							predefinedGroupIdStr, subscriberID);
					if (group != null)
						groupIDStr = group.groupID() + "";
				}

				int groupID = Integer.parseInt(groupIDStr.replace("G", ""));
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}
				boolean removedMember = false;
				
				removedMember = rbtDBManager.removeMultipleCallerFromGroup(subscriberID, groupID, memberID, null);
				
				if (removedMember) {
					response = SUCCESS;

				} else
					response = FAILED;
			}
			
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processGroupMemberRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processGroupMemberRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String groupIDStr = task.getString(param_groupID);
			String memberID = task.getString(param_memberID);
			String predefinedGroupIdStr = task
					.getString(param_predefinedGroupID);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_addMember)) {
				logger.info("Adding member into group. groupId: " + groupIDStr
						+ ", subscriberID: " + subscriberID);
				
				//RBT-15204 Added for member id check for List of issue in addMultipleContacts api
				if(memberID == null || memberID.trim().equals("")){
					return Utility.getResponseString(iRBTConstant.CALLER_NOT_ADDED_INTERNAL_ERROR);
				}
				
				int groupID = -1;
				try {
					// Trimming 'G' from groupID
					groupID = Integer.parseInt(groupIDStr.substring(1));

					// if it is valid groupId then only validating against user
					if (!isBelongsToSubscriber(subscriberID, groupID)) {
						return response;
					}

				} catch (Exception e) {
					groupID = -1;
				}

				String memberName = task.getString(param_memberName);
				String groupName = task.getString(param_groupName);
				String predefinedGroupID = task
						.getString(param_predefinedGroupID);

				
				String addMemberResponse = null;
				if (null == subscriberID) {
					addMemberResponse = rbtDBManager.addCallerInGroup(groupID,
							memberID, memberName, predefinedGroupID, groupName);
				} else {
					addMemberResponse = rbtDBManager.addCallerInGroup(
							subscriberID, groupID, memberID, memberName,
							predefinedGroupID, groupName);
				}
				response = Utility.getResponseString(addMemberResponse);
			} else if (action.equalsIgnoreCase(action_updateMember)) {
				// updates the name of the group member
				if (groupIDStr == null && predefinedGroupIdStr != null) {
					Groups group = rbtDBManager.getGroupByPreGroupID(
							predefinedGroupIdStr, subscriberID);
					if (group != null)
						groupIDStr = group.groupID() + "";
				}

				int groupID = Integer.parseInt(groupIDStr.replace("G", ""));
				
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}
				String memberName = task.getString(param_memberName);
				if (rbtDBManager.updateGroupMemberName(memberName, groupID,
						memberID)) {
					response = SUCCESS;
				} else {
					response = FAILED;
				}
				// Future use
			} else if (action.equalsIgnoreCase(action_moveMember)) {
				// Trimming 'G' from
				int groupID = Integer.parseInt(groupIDStr.substring(1));
				
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}

				// Trimming 'G' from dstGroupID
				int dstGroupID = Integer.parseInt(task.getString(
						param_dstGroupID).substring(1));
				
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, dstGroupID)) {
					return response;
				}
				boolean movedMember = rbtDBManager.changeGroupForCaller(
						subscriberID, memberID, groupID, dstGroupID);
				if (movedMember) {
					response = SUCCESS;

					String mode = getMode(task);
					deactivateSelectionsForGroupID(subscriberID, groupID, mode);
				} else
					response = FAILED;
			} else if (action.equalsIgnoreCase(action_removeMember)) {
				if (groupIDStr == null && predefinedGroupIdStr != null) {
					Groups group = rbtDBManager.getGroupByPreGroupID(
							predefinedGroupIdStr, subscriberID);
					if (group != null)
						groupIDStr = group.groupID() + "";
				}

				int groupID = Integer.parseInt(groupIDStr.replace("G", ""));
				// groupId validating against user
				if (!isBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}
				boolean removedMember = false;
				
				if (null == subscriberID) {
					removedMember = rbtDBManager.removeCallerFromGroup(groupID,
							memberID);
				} else {
					removedMember = rbtDBManager.removeCallerFromGroup(
							subscriberID, groupID, memberID);
				}

				if (removedMember) {
					response = SUCCESS;

					// TODO [Sreekar - I think this is not needed]
					/*
					 * String mode = getMode(task);
					 * deactivateSelectionsForGroupID(subscriberID, groupID,
					 * mode);
					 */
				} else
					response = FAILED;
			}
			
			if(response.equalsIgnoreCase(SUCCESS)) {
				rbtDBManager.updatePlayerStatus(subscriberID, "A");
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processGroupMemberRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processAffiliateGroupMemberRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String groupIDStr = task.getString(param_groupID);
			String memberID = task.getString(param_memberID);
			String predefinedGroupIdStr = task
					.getString(param_predefinedGroupID);

			String action = task.getString(param_action);
			if (action.equalsIgnoreCase(action_addMember)) {
				logger.info("Adding member into group. groupId: " + groupIDStr
						+ ", subscriberID: " + subscriberID);
				int groupID = -1;
				try {
					// Trimming 'G' from groupID
					groupID = Integer.parseInt(groupIDStr.substring(1));

					// if it is valid groupId then only validating against user
					if (!isAffiliateGroupBelongsToSubscriber(subscriberID, groupID)) {
						return response;
					}

				} catch (Exception e) {
					groupID = -1;
				}
				
				String memberName = task.getString(param_memberName);
				String groupName = task.getString(param_groupName);
				String optName = task.getString(param_msisdn_operator);
				String predefinedGroupID = task
						.getString(param_predefinedGroupID);

				if(groupID == -1) {
					rbtDBManager.addAffiliateGroup(null, groupName, optName, task);
					RDCGroups group = rbtDBManager.getRDCGroupByRefID(task.getString("groupRefID"));
					groupID = group.groupID();
				}
				
				String addMemberResponse = null;
				addMemberResponse = rbtDBManager.addAffiliateCallerInGroup(groupID,
							memberID, memberName, predefinedGroupID, groupName, optName);
				response = Utility.getResponseString(addMemberResponse);
			} else if (action.equalsIgnoreCase(action_removeMember)) {
				if (groupIDStr == null && predefinedGroupIdStr != null) {
					RDCGroups group = rbtDBManager.getRDCGroupByPreGroupID(
							predefinedGroupIdStr, subscriberID);
					if (group != null)
						groupIDStr = group.groupID() + "";
				}

				int groupID = Integer.parseInt(groupIDStr.replace("G", ""));
				// groupId validating against user
				if (!isAffiliateGroupBelongsToSubscriber(subscriberID, groupID)) {
					return response;
				}
				boolean removedMember = false;
				
				removedMember = rbtDBManager.removeCallerFromAffiliateGroup(groupID,
							memberID);
				
				if (removedMember) {
					response = SUCCESS;

					// TODO [Sreekar - I think this is not needed]
					/*
					 * String mode = getMode(task);
					 * deactivateSelectionsForGroupID(subscriberID, groupID,
					 * mode);
					 */
				} else
					response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processCopyRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processCopyRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			String fromSubscriber = task.getString(param_fromSubscriber);
			int categoryID = Integer.parseInt(task.getString(param_categoryID));
			int clipID = Integer.parseInt(task.getString(param_clipID));

			Parameters defaultClipParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "DEFAULT_CLIP", "-1");
			int defaultClipID = Integer.parseInt(defaultClipParam.getValue());

			String copyClipID = null;
			if (clipID != defaultClipID) {
				String browsingLanguage = task
						.getString(param_browsingLanguage);
				Category category = rbtCacheManager.getCategory(categoryID,
						browsingLanguage);
				Clip clip = rbtCacheManager.getClip(clipID, browsingLanguage);

				copyClipID = clip.getClipRbtWavFile() + ":";
				if (Utility.isShuffleCategory(category.getCategoryTpe()))
					copyClipID += "S";
				copyClipID += task.getString(param_categoryID) + ":"
						+ task.getString(param_status);

				String callerID = task.getString(param_callerID);
				if (callerID != null && !callerID.equalsIgnoreCase(ALL))
					copyClipID += "|" + callerID;
			}

			ViralSMSTable copyRequest = rbtDBManager.insertViralSMSTable(
					fromSubscriber, new Date(), "COPY", subscriberID,
					copyClipID, 0, getMode(task), null, null);

			if (copyRequest != null)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processGiftRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processGiftRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String gifterID = task.getString(param_gifterID);
			String gifteeID = task.getString(param_gifteeID);
			String toneID = task.getString(param_toneID);

			boolean isServiceGift = !task.containsKey(param_toneID)
					&& !task.containsKey(param_clipPromoID)
					&& !task.containsKey(param_clipVcode);

			logger.debug("Gifting success for gifterID: " + gifterID + ", gifteeID: " + gifteeID 
					+ ", toneID: " + toneID + ", isServiceGift: " + isServiceGift);
			
			if (gifteeID == null || gifteeID.trim().isEmpty()) {
				logger.info("Invalid gifteeID. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}
			if (gifteeID.indexOf(",") != -1) {
				logger.debug("gifterId: " + gifterID + ", gifteeIds: " + gifteeID);
				boolean isSuccessAtleastOnce = false;
				String[] gifteeIDs = gifteeID.split(",");
				for (String localGifteeID : gifteeIDs) {
					response = processGiftForAGiftee(task, gifterID, localGifteeID, toneID,
							isServiceGift);
					if (!response.equalsIgnoreCase(SUCCESS)) {
						logger.warn("Gifting failed for gifterID: " + gifterID + ", gifteeID: " + localGifteeID 
								+ ", toneID: " + toneID + ", isServiceGift: " + isServiceGift + ", response: " + response);
					} else {
						logger.debug("Gifting successful for gifterID: " + gifterID + ", gifteeID: " + localGifteeID 
								+ ", toneID: " + toneID + ", isServiceGift: " + isServiceGift);
						isSuccessAtleastOnce=true;
					}
				}
				if (isSuccessAtleastOnce) {
					response = SUCCESS;
				} else {
					logger.error("Gifting failed for all gifteeIds. gifterID: " + gifterID + ", gifteeID: " + gifteeID 
							+ ", toneID: " + toneID + ", isServiceGift: " + isServiceGift);
					response = FAILURE;
				}
			} else {
				response = processGiftForAGiftee(task, gifterID, gifteeID, toneID,
					isServiceGift);
			}
		} catch (Exception e) {
			logger.error("GIFT_ERROR", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	private String processGiftForAGiftee(WebServiceContext task,
			String gifterID, String gifteeID, String toneID,
			boolean isServiceGift) {
		String response = "ERROR";
		if (!com.onmobile.apps.ringbacktones.services.common.Utility
				.isValidNumber(gifterID) 
				|| !com.onmobile.apps.ringbacktones.services.common.Utility
						.isValidNumber(gifteeID)) {
			logger.info("Invalid gifterID or gifteeID. Returning response: "
					+ INVALID_PARAMETER);
			return INVALID_PARAMETER;
		}

		if (rbtDBManager.isTotalBlackListSub(gifteeID)) {
			return BLACK_LISTED;
		}

		Parameters giftLimitParam = null;
		Parameters giftLimitDaysParam = null;
		if (isServiceGift) {
			// Service Gift
			giftLimitParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "SERVICE_GIFT_LIMIT");
			giftLimitDaysParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "SERVICE_GIFT_LIMIT_DAYS", "1");
		} else {
			// Tone Gift
			giftLimitParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "TONE_GIFT_LIMIT");
			giftLimitDaysParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "TONE_GIFT_LIMIT_DAYS", "1");
		}

		int giftLimit = 0;
		int giftLimitDays = 1;
		if (giftLimitParam != null) {
			giftLimit = Integer.parseInt(giftLimitParam.getValue());
			giftLimitDays = Integer.parseInt(giftLimitDaysParam.getValue());
		}

		if (giftLimit > 0) {
			List<SubscriberActivityCounts> subscriberActivityCounts = SubscriberActivityCountsDAO
					.getSubscriberActivityCountsForDays(gifterID,
							giftLimitDays);
			if ((isServiceGift && DataUtils.isServiceGiftLimitExceeded(
					subscriberActivityCounts, giftLimit))
					|| (!isServiceGift && DataUtils
							.isToneGiftLimitExceeded(
									subscriberActivityCounts, giftLimit))) {
				logger.info("response: " + LIMIT_EXCEEDED);
				return LIMIT_EXCEEDED;
			}
		}

		if (task.containsKey(param_categoryID)) {
			int categoryID = Integer.parseInt(task
					.getString(param_categoryID));
			Category category = rbtCacheManager.getCategory(categoryID);
			if (category == null) {
				logger.info("response: " + CATEGORY_NOT_EXISTS);
				return CATEGORY_NOT_EXISTS;
			}

			if (Utility.isShuffleCategory(category.getCategoryTpe()))
				toneID = "C" + categoryID;
		} else if (task.containsKey(param_categoryPromoID)) {
			String categoryPromoID = task.getString(param_categoryPromoID);
			Category category = rbtCacheManager
					.getCategoryByPromoId(categoryPromoID);
			if (category == null){
				logger.info("response: " + CATEGORY_NOT_EXISTS);
				return CATEGORY_NOT_EXISTS;
			}

			if (Utility.isShuffleCategory(category.getCategoryTpe()))
				toneID = "C" + category.getCategoryId();
		}

		if (!isServiceGift && (toneID == null || !toneID.startsWith("C"))) {
			task.put(param_clipID, toneID);
			Clip clip = getClip(task);
			if (clip == null) {
				logger.info("response: " + CLIP_NOT_EXISTS);
				return CLIP_NOT_EXISTS;
			} else if (clip.getClipEndTime() != null && clip.getClipEndTime().before(new Date())) {
				logger.info("response : " + CLIP_EXPIRED);
				return CLIP_EXPIRED;
			}

			toneID = String.valueOf(clip.getClipId());
		}

		// Get the offerID, subscription class, chargeClass
		// if (getParamAsBoolean(iRBTConstant.COMMON,
		// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
		if (isSupportSMClientModel(task, GIFT_OFFERTYPE)) {
			ViralSMSTable gift = rbtDBManager.insertViralSMSTableMap(
					gifterID, new Date(), "GIFTCHRGPENDING", gifteeID,
					toneID, 0, getMode(task), null, getInfoMap(task));

			response = doesSubHaveGiftAmount(gifterID, gifteeID, toneID,
					getMode(task), task, gift.setTime());
			if (!response.equalsIgnoreCase(RBTSMClientResponse.SUCCESS)) {
				logger.info("Removing entry from viral sms table for subID : "
						+ gifterID);
				rbtDBManager.removeViralSMS(gifterID, "GIFTCHRGPENDING");
			}
		} else {
			String smsType = "GIFT";
			if (task.containsKey(param_isGifterConfRequired)
					&& task.getString(param_isGifterConfRequired)
							.equalsIgnoreCase(YES)) {
				smsType = "INIT_GIFT";
			} else if (task.containsKey(param_isGifteeConfRequired)
					&& task.getString(param_isGifteeConfRequired)
							.equalsIgnoreCase(YES)) {
				smsType = "PRE_GIFT";
			}

			if(task.containsKey(param_isGifterCharged) && task.getString(param_isGifterCharged).equalsIgnoreCase(YES)) {
					smsType = "GIFT_CHARGED";
			}
			
			ViralSMSTable gift = rbtDBManager.insertViralSMSTableMap(
					gifterID, new Date(), smsType, gifteeID, toneID, 0,
					getMode(task), null, getInfoMap(task));

			if (gift != null)
				response = SUCCESS;
			else
				response = FAILED;
		}

		if (response.equals(SUCCESS) && giftLimit > 0) {
			SubscriberActivityCounts subscriberActivityCounts = SubscriberActivityCountsDAO
					.getSubscriberActivityCountsForDate(gifterID,
							new Date());
			if (subscriberActivityCounts == null) {
				subscriberActivityCounts = new SubscriberActivityCounts(
						gifterID, new Date());
				if (toneID == null)
					subscriberActivityCounts.incrementServiceGiftsCount();
				else
					subscriberActivityCounts.incrementToneGiftsCount();

				subscriberActivityCounts.create();
			} else {
				if (toneID == null)
					subscriberActivityCounts.incrementServiceGiftsCount();
				else
					subscriberActivityCounts.incrementToneGiftsCount();

				subscriberActivityCounts.update();
			}
		}
		logger.info("Response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processGiftRejectRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processGiftRejectRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			String gifterID = task.getString(param_gifterID);
			String gifteeID = task.getString(param_gifteeID);

			if (!com.onmobile.apps.ringbacktones.services.common.Utility
					.isValidNumber(gifterID)
					|| !com.onmobile.apps.ringbacktones.services.common.Utility
							.isValidNumber(gifteeID)) {
				logger.info("Invalid gifterID or gifteeID. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			Date sentTime = dateFormat
					.parse(task.getString(param_giftSentTime));

			boolean rejected = rbtDBManager.updateViralPromotion(gifterID,
					gifteeID, sentTime, "GIFTED", "REJECT_ACK", new Date(),
					null, null);
			if (rejected)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#setSubscriberDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String setSubscriberDetails(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (rbtDBManager.isSubscriberDeactivated(subscriber)
					&& !task.containsKey(param_isBlacklisted)) {
				logger.info("response: " + USER_NOT_EXISTS);
				return USER_NOT_EXISTS;
			}

			boolean result = true;

			if (task.containsKey(param_language)) {
				String language = task.getString(param_language);
				Parameters parameters = parametersCacheManager.getParameter(
						iRBTConstant.COMMON, "SUBMGR_URL_FOR_UPDATE_LANGUAGE");
				if (parameters == null)
					rbtDBManager.setSubscriberLanguage(subscriberID,
							task.getString(param_language));
				else {
					String url = parameters.getValue().trim();
					url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
					url = url.replaceAll("%LANGUAGE%", language);

					try {
						HttpParameters httpParameters = new HttpParameters(url);
						Utility.setSubMgrProxy(httpParameters);
						logger.info("httpParameters: " + httpParameters);

						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, null);

						String smResponse = httpResponse.getResponse().trim();
						String responseCode = smResponse.split("\\|")[0];

						if (responseCode.equalsIgnoreCase("0"))
							rbtDBManager.setSubscriberLanguage(subscriberID,
									task.getString(param_language));
						else if (responseCode.equalsIgnoreCase("1")) {
							logger.info("response: " + USER_NOT_EXISTS);
							return USER_NOT_EXISTS;
						} else {
							logger.info("response: " + ERROR);
							return ERROR;
						}
					} catch (Exception e) {
						logger.error("", e);
						logger.info("response: " + ERROR);
						return ERROR;
					}
				}
			}
			if (task.containsKey(param_age))
				rbtDBManager.setSubscriberAge(subscriberID,
						Integer.parseInt(task.getString(param_age)));
			if (task.containsKey(param_gender))
				rbtDBManager.setSubscriberGender(subscriberID,
						task.getString(param_gender));
			if (task.containsKey(param_isNewsLetterOn)) {
				String isNewsLetterOn = task.getString(param_isNewsLetterOn)
						.equalsIgnoreCase(YES) ? iRBTConstant.NEWSLETTER_ON
						: iRBTConstant.NEWSLETTER_OFF;
				result = rbtDBManager.updateExtraInfo(subscriberID,
						iRBTConstant.IS_NEWSLETTER_ON, isNewsLetterOn);
			}
			if (task.containsKey(param_isUdsOn)) {
				subscriber = DataUtils.getSubscriber(task);
				if (!rbtDBManager.isAutoDownloadPackActivated(subscriber)) {
					String isUdsOn = task.getString(param_isUdsOn)
							.equalsIgnoreCase(YES) ? iRBTConstant.UDS_OPTIN_ON
							: iRBTConstant.UDS_OPTIN_OFF;
					if (task.containsKey(param_udsType)
							&& task.getString(param_udsType) != null) {
						isUdsOn = task.getString(param_udsType);
					}
					result = rbtDBManager.updateExtraInfo(subscriberID,
							iRBTConstant.UDS_OPTIN, isUdsOn);
				}
			}
			if (task.containsKey(param_isPrepaid)) {
				boolean isPrepaid = task.getString(param_isPrepaid)
						.equalsIgnoreCase(YES);
				result = rbtDBManager.changeSubscriberType(subscriberID,
						isPrepaid);
			}
			if (task.containsKey(param_isPressStarIntroEnabled)
					&& subscriber != null) {
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (extraInfoMap == null)
					extraInfoMap = new HashMap<String, String>();

				boolean isPressStarIntroEnabled = task.getString(
						param_isPressStarIntroEnabled).equalsIgnoreCase(YES);
				String value = (isPressStarIntroEnabled ? iRBTConstant.ENABLE_PRESS_STAR_INTRO
						: iRBTConstant.DISABLE_PRESS_STAR_INTRO);
				extraInfoMap.put(iRBTConstant.EXTRA_INFO_INTRO_PROMPT_FLAG,
						value);

				if (isPressStarIntroEnabled
						&& extraInfoMap
								.containsKey(iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT))
					extraInfoMap
							.remove(iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT);

				if (isPressStarIntroEnabled) {
					if (task.containsKey(param_clipID)) {
						Clip clip = rbtCacheManager.getClip(task
								.getString(param_clipID));
						if (clip != null)
							extraInfoMap.put(
									iRBTConstant.EXTRA_INFO_PRE_RBT_WAV,
									clip.getClipRbtWavFile());
					}
					if(task.containsKey(param_dtmfInputKeys)) {
						extraInfoMap.put(iRBTConstant.EXTRA_INFO_DTMF_KEYS,
								task.getString(param_dtmfInputKeys));
					}
				} else {
					extraInfoMap.remove(iRBTConstant.EXTRA_INFO_PRE_RBT_WAV);
					extraInfoMap.remove(iRBTConstant.EXTRA_INFO_DTMF_KEYS);
				}

				String extraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);
				result = rbtDBManager.updateExtraInfoAndPlayerStatus(
						subscriberID, extraInfo, "A");
			}
			if (task.containsKey(param_isPollOn)) {
				if (task.getString(param_isPollOn).equalsIgnoreCase(YES)) {
					HashMap<String, String> extraInfoMap = rbtDBManager
							.getExtraInfoMap(subscriber);
					String subscriberPollStatus = null;
					if (extraInfoMap != null)
						subscriberPollStatus = extraInfoMap
								.get(iRBTConstant.PLAY_POLL_STATUS);
					if (subscriberPollStatus == null
							|| subscriberPollStatus
									.equals(iRBTConstant.PLAY_POLL_STATUS_OFF))
						result = rbtDBManager.updateExtraInfoAndPlayerStatus(
								subscriber, iRBTConstant.PLAY_POLL_STATUS,
								iRBTConstant.PLAY_POLL_STATUS_ON, "A");
				} else
					result = rbtDBManager.updateExtraInfoAndPlayerStatus(
							subscriber, iRBTConstant.PLAY_POLL_STATUS,
							iRBTConstant.PLAY_POLL_STATUS_OFF, "A");
			}
			if (task.containsKey(param_isOverlayOn)) {
				String isOverlayOn = task.getString(param_isOverlayOn)
						.equalsIgnoreCase(YES) ? iRBTConstant.ENABLE_INTRO_OVERLAY
						: iRBTConstant.DISABLE_INTRO_OVERLAY;
				result = rbtDBManager.updateExtraInfoAndPlayerStatus(
						subscriber, iRBTConstant.EXTRA_INFO_INTRO_OVERLAY_FLAG,
						isOverlayOn, "A");
			}
			if (task.containsKey(param_userLocked)) {
				// Lock/unlock the user only if he is an active user i.e. the
				// status is B
				if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
					String isUserLocked = task.getString(param_userLocked)
							.equalsIgnoreCase(YES) ? iRBTConstant.EXTRA_INFO_LOCK_USER_TRUE
							: iRBTConstant.EXTRA_INFO_LOCK_USER_FALSE;
					HashMap<String, String> userInfoMap = null;
					if (subscriber.extraInfo() != null) {
						userInfoMap = DBUtility
								.getAttributeMapFromXML(subscriber.extraInfo());
						// If the USER_LOCK is already set to the value passed,
						// do not update the DB
						if (userInfoMap
								.containsKey(iRBTConstant.EXTRA_INFO_LOCK_USER)) {
							if (!(userInfoMap
									.get(iRBTConstant.EXTRA_INFO_LOCK_USER))
									.equalsIgnoreCase(isUserLocked)) {
								String extraInfo = DBUtility.setXMLAttribute(
										subscriber.extraInfo(),
										iRBTConstant.EXTRA_INFO_LOCK_USER,
										isUserLocked);
								result = rbtDBManager.updateExtraInfo(
										subscriberID, extraInfo);
							}
						} else {
							// Nothing to be updated in the DB, just return true
							// i.e. status LOCKED if locked and UNLOCKED if
							// unlocked
							String extraInfo = DBUtility.setXMLAttribute(
									subscriber.extraInfo(),
									iRBTConstant.EXTRA_INFO_LOCK_USER,
									isUserLocked);
							result = rbtDBManager.updateExtraInfo(subscriberID,
									extraInfo);
						}

					} else {
						String extraInfo = DBUtility
								.setXMLAttribute(subscriber.extraInfo(),
										iRBTConstant.EXTRA_INFO_LOCK_USER,
										isUserLocked);
						result = rbtDBManager.updateExtraInfo(subscriberID,
								extraInfo);
					}
				} else {
					// If the user is not in ACTIVE state, its normal status
					// will be returned.
					response = Utility.getSubscriberStatus(subscriber);

					logger.info("response: " + response);
					return response;
				}
			}
			if (task.containsKey(param_isBlacklisted)) {
				boolean isBlacklisted = task.getString(param_isBlacklisted)
						.equalsIgnoreCase(YES);

				String blacklistType = task.getString(param_blacklistType);
				if (blacklistType == null)
					blacklistType = "TOTAL";
				blacklistType = blacklistType.toUpperCase();

				if (isBlacklisted) {
					String validUser = isValidUser(task, subscriber);
					if (validUser.equals(VALID) || validUser.equals(SUSPENDED)) {
						// Suspended user also allowed to blacklist
						Calendar calendar = Calendar.getInstance();
						calendar.set(2037, 0, 1);
						Date endDate = calendar.getTime();
						ViralBlackListTable viralBlackListTable = rbtDBManager
								.insertViralBlackList(subscriberID, null,
										endDate, blacklistType);
						if (viralBlackListTable != null)
							result = true;
					} else {
						logger.info("response: " + validUser);
						return validUser;
					}
				} else {
					result = rbtDBManager.removeViralBlackList(subscriberID,
							blacklistType);
				}
			}
			
			//Added condition for VDE-2730
			if (task.containsKey(param_isPressStarIntroSuspendEnabled)
					&& subscriber != null) {
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (extraInfoMap == null)
					extraInfoMap = new HashMap<String, String>();

				boolean isPressStarIntroSuspendEnabled = task.getString(
						param_isPressStarIntroSuspendEnabled).equalsIgnoreCase(
						YES);
				String value = (isPressStarIntroSuspendEnabled ? iRBTConstant.ENABLE_PRESS_STAR_INTRO_SUSPEND
						: iRBTConstant.DISABLE_PRESS_STAR_INTRO_SUSPEND);
				extraInfoMap.put(
						iRBTConstant.EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG,
						value);
				if (isPressStarIntroSuspendEnabled) {
					if (task.containsKey(param_clipID)) {
						Clip clip = rbtCacheManager.getClip(task
								.getString(param_clipID));
						if (clip != null) {
							extraInfoMap.put(
									iRBTConstant.EXTRA_INFO_PRE_RBT_WAV,
									clip.getClipRbtWavFile());
						} else {
							extraInfoMap.put(iRBTConstant.EXTRA_INFO_PRE_RBT_WAV,
									parametersCacheManager.getParameterValue(iRBTConstant.COMMON,
											"SUSPEND_PRE_RBT_WAV_FILE", ""));
						}
					}
				} else {
					extraInfoMap.remove(iRBTConstant.EXTRA_INFO_PRE_RBT_WAV);
				}

				String extraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);
				result = rbtDBManager.updateExtraInfoAndPlayerStatus(
						subscriberID, extraInfo, "A");
			}
			//Ended Condition for VDE-2730

			if (result)
				response = SUCCESS;
			else
				response = FAILED;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#setApplicationDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String setApplicationDetails(WebServiceContext task) {
		String response = ERROR;
		
		try {
			String info = task.getString(param_info);
			if (info == null) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			boolean result = false;
			response = FAILED;
			logger.info("Received request for info: " + info);

			if (info.equalsIgnoreCase(PARAMETERS)) {
				String type = task.getString(param_type);
				String name = task.getString(param_name);
				String value = task.getString(param_value);

				if (type != null && name != null && value != null) {
					Parameters parameter = parametersCacheManager.getParameter(
							type, name, null);
					if (parameter == null)
						result = parametersCacheManager.addParameter(type,
								name, value);
					else
						result = parametersCacheManager.updateParameter(type,
								name, value);
				} else
					response = INVALID_PARAMETER;
			} else if (info.equalsIgnoreCase(SUBSCRIPTION_CLASSES)) {
				// Future use
			} else if (info.equalsIgnoreCase(CHARGE_CLASSES)) {
				// Future use
			} else if (info.equalsIgnoreCase(SMS_TEXTS)) {
				String type = task.getString(param_type);
				String name = task.getString(param_name);
				String value = task.getString(param_value);

				if (type != null && value != null) {
					BulkPromoSMSCacheManager bulkPromoSMSCacheManager = CacheManagerUtil
							.getBulkPromoSMSCacheManager();
					BulkPromoSMS bulkPromoSMS = bulkPromoSMSCacheManager
							.getBulkPromoSMSForDate(type, name);
					if (bulkPromoSMS == null)
						result = bulkPromoSMSCacheManager.addBulkPromoSmS(type,
								name, value, null);
					else
						result = bulkPromoSMSCacheManager.update(type, name,
								value, null);

					task.remove(param_name);
				} else
					response = INVALID_PARAMETER;
			} else if (info.equalsIgnoreCase(PICK_OF_THE_DAYS)) {
				String playDate = task.getString(param_playDate);
				int clipID = Integer.parseInt(task.getString(param_clipID));
				int categoryID = Integer.parseInt(task
						.getString(param_categoryID));
				String circleID = task.getString(param_circleID);
				String profile = task.getString(param_profile);
				String language = task.getString(param_language);

				char prepaidYes = 'b';
				if (task.containsKey(param_userType)) {
					String userType = task.getString(param_userType);
					if (userType.equalsIgnoreCase(PREPAID))
						prepaidYes = 'y';
					else if (userType.equalsIgnoreCase(POSTPAID))
						prepaidYes = 'n';
				}

				PickOfTheDay pickOfTheDay = rbtDBManager.getPickOfTheDay(
						playDate, circleID, prepaidYes, profile, true,
						language, true);
				if (pickOfTheDay == null)
					result = (rbtDBManager.insertPickOfTheDay(categoryID,
							clipID, playDate, circleID, prepaidYes, profile,
							language) != null);
				else
					result = rbtDBManager.updatePickOfTheDay(categoryID,
							clipID, playDate, circleID, prepaidYes, profile,
							language);

				if (playDate != null)
					task.put(param_range, playDate.substring(3));
			} else if (info.equalsIgnoreCase(RBT_LOGIN_USER)) {
				String userID = task.getString(param_userID);
				String newUserID = task.getString(param_newUserID);
				String password = task.getString(param_password);
				String subscriberID = task.getString(param_subscriberID);
				String type = task.getString(param_type);
				String oldPassowrd = task.getString(param_oldPassword);
				boolean encryptPassword = task
						.containsKey(param_encryptPassword)
						&& task.getString(param_encryptPassword)
								.equalsIgnoreCase(YES);
				//Added for handset client server
				String skipSendingSMS = task.getString(param_userInfo+"_skipSendingSMS");

				if (userID != null && type != null) {
					String doSubValidationStr = task
							.getString(param_doSubscriberValidation);
					if (doSubValidationStr != null
							&& doSubValidationStr.equalsIgnoreCase(YES)) {
						Subscriber subscriber = rbtDBManager
								.getSubscriber(subscriberID);
						String validityResponse = isValidUser(task, subscriber);
						if (!validityResponse.equalsIgnoreCase(VALID)
								&& !validityResponse
										.equalsIgnoreCase(SUSPENDED)) {
							logger.info("Returning response after subscriber validation : "
									+ validityResponse);
							return validityResponse;
						}
					}

					HashMap<String, String> userInfo = new HashMap<String, String>();
					Set<String> keySet = task.keySet();
					for (String key : keySet) {
						if (key.startsWith(param_userInfo))
							userInfo.put(key.substring(key.indexOf('_') + 1),
									task.getString(key));
					}

					RBTLoginUser rbtLoginUser = rbtDBManager.getRBTLoginUser(
							userID, null, null, type, null, encryptPassword);
					if (rbtLoginUser == null) {
						result = (rbtDBManager.addRBTLoginUser(userID,
								password, subscriberID, type, userInfo,
								encryptPassword) != null);
						// This is to check whether the user is first time registering MSISDN.
						task.put("IS_NEW_USER", "true");
					} else {
						result = rbtDBManager.updateRBTLoginUser(userID,
								newUserID, password, subscriberID, type,
								rbtLoginUser.userInfo(), userInfo,
								encryptPassword, null, oldPassowrd);
						if (result) {
							// This is to check whether the user is first time registering MSISDN.
							if (null != subscriberID && !subscriberID.equalsIgnoreCase(rbtLoginUser.subscriberID())) {
								task.put("IS_NEW_USER", "true");
							}
							if (newUserID != null) {
								// If userID updated with newUserID then userID is
								// changed to to newUserID,
								// so that newUserID will be user Information class
								// to build the xml.
								task.put(param_userID, newUserID);
							}
						}
					}
					// sending sms for registration
					task.put("password", password);
					if(!Boolean.valueOf(skipSendingSMS)) {
						sendAcknowledgementSMS(task, "REGISTER");
					}
				} else
					response = INVALID_PARAMETER;
			} else if (info.equalsIgnoreCase(RBT_OTP_LOGIN)) {

				String subscriberID = task.getString(param_subscriberID);
				String regeneratePassword = task
						.getString(param_regeneratePassword);
				String encryptPassword = task.getString(param_encryptPassword);
				String type = task.getString(param_type);
				String userID = task.getString(param_userID);
                String operator_name = task.getString(param_msisdn_operator);
				logger.debug("Received OTP login request. subscriberID: "
						+ subscriberID + ", type: " + type
						+ ", regeneratePassword: " + regeneratePassword
						+ ", encryptPassword: " + encryptPassword);
				
				String otpSupportedOperator = parametersCacheManager
						.getParameterValue(iRBTConstant.COMMON,
								"OTP_SUPPORTED_OPERATOR", null);
				SubscriberDetail subscriberDetail = null;
				if(operator_name == null || otpSupportedOperator == null){
				     subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID, "OTP_LOGIN"));
				}
				
				if (operator_name == null && subscriberDetail != null && !subscriberDetail.isValidOperator()) {
					return OPERATOR_NOT_CONFIGURED;
				}
				
				if(operator_name == null && subscriberDetail != null && !subscriberDetail.isValidSubscriber()) {
					return INVALID;
				}
				List<String> otpSupportedOperatorList = null;
				if (otpSupportedOperator != null) {
					otpSupportedOperatorList = Arrays
							.asList(otpSupportedOperator.split(","));
				}

				if (otpSupportedOperatorList != null
					  && ((operator_name != null && !otpSupportedOperatorList
							.contains(operator_name)) || (subscriberDetail != null && !otpSupportedOperatorList
							.contains(subscriberDetail.getCircleID())))) {
					return OPERATOR_NOT_CONFIGURED;
				}
				
				// By default, generate password and encrypt password are true.
				boolean isRegeneratePassword = ("n"
						.equalsIgnoreCase(regeneratePassword)) ? false : true;
				boolean isEncryptPassword = ("n"
						.equalsIgnoreCase(encryptPassword)) ? false : true;

				// If userId is not recieved as a part of requset, replace userId
				// with subscriberId. It is mandatory param in db table.
				userID = (null != userID) ? userID : subscriberID;

				String password = null;
				HashMap<String, String> userInfo = new HashMap<String, String>();

				RBTLoginUser rbtLoginUser = rbtDBManager.getRBTLoginUser(
						userID, password, subscriberID, type, userInfo,
						isEncryptPassword);
				
				String otpPasswordStr = null;
				boolean sendSms = false;
		
				if (null != rbtLoginUser) {
					
					// User is already exists in database, if
					// regenerate password is true, generate the password again and 
					// update the existing
					if (isRegeneratePassword) {
						String newUserID = null;
						HashMap<String, String> existingUserInfo = null;
						Date creationTime = null;

						int passcode = generateRandomNumber();
						otpPasswordStr = String.valueOf(passcode);
						boolean updated = rbtDBManager.updateRBTLoginUser(userID,
								newUserID, otpPasswordStr, subscriberID, type,
								existingUserInfo, userInfo, isEncryptPassword,
								creationTime, null);
						logger.debug("Updated the regenerated password. subscriberID: "
								+ subscriberID + ", updated: " + result);
						if(updated) {
//							result = sendOtpSms(subscriberID, otpPasswordStr);
							sendSms = true;
						}
					} else {
						otpPasswordStr = rbtLoginUser.password();						
						if(isEncryptPassword) {
							otpPasswordStr = Encryption128BitsAES.decryptAES128Bits(otpPasswordStr);
						}
						logger.debug("Login details are already exists in"
								+ " database. subscriberID: " + subscriberID
								+", otpPasswordStr: "+otpPasswordStr);
//						result = sendOtpSms(subscriberID, otpPasswordStr);
						sendSms = true;
					}
				} else {
					int passcode = generateRandomNumber();
					otpPasswordStr = String.valueOf(passcode);
					rbtLoginUser = rbtDBManager.addRBTLoginUser(userID,
							otpPasswordStr, subscriberID, type, userInfo,
							isEncryptPassword);
					logger.debug("Added login user. subscriberID: "
							+ subscriberID + ", rbtLoginUser: " + rbtLoginUser);
					if (null != rbtLoginUser) {
//						result = sendOtpSms(subscriberID, otpPasswordStr);
						sendSms = true;
					}
				}
				
				if(sendSms) {
					if(mnpUrl == null) {
						result = true;
						task.put("password", otpPasswordStr);
						sendAcknowledgementSMS(task, "OTP_LOGIN");
					}
					else {
						if(subscriberDetail!=null){
							operator_name = subscriberDetail.getCircleID();
						}
						result = sendOtpSms(subscriberID, otpPasswordStr, operator_name);
					}
				}
				
				logger.debug("Processed OTP login request. subscriberID: "
						+ subscriberID + ", result: " + result);

			} else if (info.equalsIgnoreCase(SITES)) {
				// Future use
			} else if (info.equalsIgnoreCase(CHARGE_SMS)) {
				String type = task.getString(param_type);
				String name = task.getString(param_name);

				if (type != null && name != null) {
					String prepaidSuccessSms = task
							.getString(param_prepaidSuccessSms);
					String prepaidFailureSms = task
							.getString(param_prepaidFailureSms);
					String postpaidSuccessSms = task
							.getString(param_postpaidSuccessSms);
					String postpaidFailureSms = task
							.getString(param_postpaidFailureSms);
					String prepaidNEFSuccessSms = task
							.getString(param_prepaidNEFSuccessSms);
					String prepaidRenewalSuccessSms = task
							.getString(param_prepaidRenewalSuccessSms);
					String prepaidRenewalFailureSms = task
							.getString(param_prepaidRenewalFailureSms);
					String postpaidRenewalSuccessSms = task
							.getString(param_postpaidRenewalSuccessSms);
					String postpaidRenewalFailureSms = task
							.getString(param_postpaidRenewalFailureSms);

					ChargeSMS chargeSMS = new ChargeSMS(name, type, null,
							prepaidSuccessSms, prepaidFailureSms,
							postpaidSuccessSms, postpaidFailureSms,
							prepaidNEFSuccessSms, prepaidRenewalSuccessSms,
							prepaidRenewalFailureSms,
							postpaidRenewalSuccessSms,
							postpaidRenewalFailureSms);

					result = CacheManagerUtil.getChargeSMSCacheManager()
							.updateChargeSMS(chargeSMS);
				} else
					response = INVALID_PARAMETER;
			}

			if (result)
				response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/**
	 * @param subscriberID
	 * @param otpPasswordStr
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private boolean sendOtpSms(String subscriberID, String otpPasswordStr, String operatorName) {
		boolean result = false;
		try {
			if (null != mnpUrl) {
				
				String operatorUrl = parametersCacheManager
						.getParameterValue("COMMON", operatorName.toUpperCase()
								+ "_OTP_URL", null);
				if (operatorUrl == null) {
					logger.info("Url configured in DB is null . So going for Site prefix url for OTP");
					operatorUrl = ContentInterOperatorUtility.operatorNameUrlMap
							.get(operatorName);
				}
				logger.debug("Configured operatorUrl: " + operatorUrl
						+ " for rbtOperatorName: " + operatorName);

				if (operatorUrl != null) {

					if (!operatorName.equals("RELIANCE")
							&& !operatorName.equals("UNINOR_COMVIVA")) {
						int doubleSlashIndex = operatorUrl.indexOf("//");
						if (operatorUrl != null
								&& doubleSlashIndex != -1
								&& operatorUrl.indexOf("/",
										doubleSlashIndex + 2) != -1) {
							operatorUrl = operatorUrl.substring(0, operatorUrl
									.indexOf("/", doubleSlashIndex + 2));
						}
						operatorUrl = operatorUrl + "/rbt/Utils.do?";
					}
					String language = null;
					String smsText = smsTextCacheManager.getSmsText(
							"WEBSERVICE_OTP_LOGIN", "SUCCESS", language);

					String finalOtpSmsText = (null != smsText) ? smsText
							: defaultOtpSmsText;

					finalOtpSmsText = (null != otpPasswordStr) ? finalOtpSmsText
							.replace("<otppasscode>", otpPasswordStr)
							: finalOtpSmsText;

					HashMap<String, String> requestParameters = new HashMap<String, String>();
					requestParameters.put(param_action, action_sendSMS);
					requestParameters.put(param_senderID, otpSenderId);
					requestParameters.put(param_receiverID, subscriberID);
					requestParameters.put(param_smsText, finalOtpSmsText);

					logger.debug("Making a hit to send OTP SMS. operatorUrl: "
							+ operatorUrl + " requestParameters: "
							+ requestParameters);

					ContentInterOperatorHttpResponse ioHttpResponse = ContentInterOperatorHttpUtils
							.getResponse(operatorUrl, requestParameters, null);
					logger.debug("Successfully made a hit to send OTP SMS."
							+ " operatorUrl: " + operatorUrl
							+ " requestParameters: " + requestParameters
							+ ", ioHttpResponse: " + ioHttpResponse);

					if (ioHttpResponse.getHttpResponseCode() == 200) {

						String responseXml = ioHttpResponse
								.getHttpResponseString();

						Document responseDoc = getDocumentFromResponse(responseXml);
						String responseStr = getValueFomDoc(responseDoc,
								"response");

						if ("SUCCESS".equalsIgnoreCase(responseStr)) {
							logger.debug("Successfully sent OTP SMS."
									+ " responseStr: " + responseStr);
							result = true;
						}
					}
				}
			} else {
				logger.error("Could not send OTP SMS, MNP_URL is not configured");
			}
		} catch (ParserConfigurationException pce) {
			logger.error("Could not send OTP SMS."
					+ " ParserConfigurationException: " + pce.getMessage(), pce);
		} catch (SAXException saxe) {
			logger.error(
					"Could not send OTP SMS." + " SAXException: "
							+ saxe.getMessage(), saxe);
		} catch (IOException ioe) {
			logger.error(
					"Could not send OTP SMS." + " IOException: "
							+ ioe.getMessage(), ioe);
		} catch (Exception e) {
			logger.error(
					"Could not send OTP SMS." + " Exception: " + e.getMessage(),
					e);
		}
		return result;
	}

	private int generateRandomNumber() {
		RandomDataImpl r = new RandomDataImpl();
		int passcode = r.nextInt(
				Integer.parseInt(otpLowerRange),
				Integer.parseInt(otpHigherRange));
		return passcode;
	}

	private String getValueFomDoc(Document mnpResponseDoc, String tagName) {
		if (null != mnpResponseDoc) {
			return mnpResponseDoc.getElementsByTagName(tagName).item(0)
					.getFirstChild().getNodeValue();
		}
		return null;
	}

	private Document getDocumentFromResponse(String mnpResponse)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		if (null != mnpResponse) {
			InputStream is = new ByteArrayInputStream(mnpResponse.getBytes());
			Document mnpResponseDoc = builder.parse(is);
			return mnpResponseDoc;
		}
		return builder.newDocument();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * removeApplicationDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String removeApplicationDetails(WebServiceContext task) {
		String response = ERROR;

		try {
			String info = task.getString(param_info);
			if (info == null) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			boolean result = false;
			response = FAILED;

			if (info.equalsIgnoreCase(PARAMETERS)) {
				String type = task.getString(param_type);
				String name = task.getString(param_name);

				if (type != null && name != null)
					result = parametersCacheManager.removeParameter(type, name);
				else
					response = INVALID_PARAMETER;
			} else if (info.equalsIgnoreCase(SUBSCRIPTION_CLASSES)) {
				// Future use
			} else if (info.equalsIgnoreCase(CHARGE_CLASSES)) {
				// Future use
			} else if (info.equalsIgnoreCase(SMS_TEXTS)) {
				// Future use
			} else if (info.equalsIgnoreCase(PICK_OF_THE_DAYS)) {
				String playDate = task.getString(param_playDate);
				String circleID = task.getString(param_circleID);
				String profile = task.getString(param_profile);
				String language = task.getString(param_language);

				char prepaidYes = 'b';
				if (task.containsKey(param_userType)) {
					String userType = task.getString(param_userType);
					if (userType.equalsIgnoreCase(PREPAID))
						prepaidYes = 'y';
					else if (userType.equalsIgnoreCase(POSTPAID))
						prepaidYes = 'n';
				}

				result = rbtDBManager.removePickOfTheDay(playDate, circleID,
						prepaidYes, profile, language);

				if (playDate != null)
					task.put(param_range, playDate.substring(3));
			} else if (info.equalsIgnoreCase(RBT_LOGIN_USER)) {
				String userID = task.getString(param_userID);
				String type = task.getString(param_type);
				// Changes done for URL Encryption and Decryption
				if (RBTDBManager.isEncryptionModel()) {
					logger.info("Encryption Model is enabled");
					logger.info("before encrypting userId: "+userID);
					userID = URLEncryptDecryptUtil.encryptUserNamePassword(userID);
					logger.info("after encrypting userId: "+userID);
				}
				// End of URL Encryption and Decryption
				result = rbtDBManager.deleteRBTLoginUserByUserID(userID, type);
			} else if (info.equalsIgnoreCase(SITES)) {
				// Future use
			}

			if (result)
				response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
	
	
	public String getApplicationDetails(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String encryptPassword = task.getString(param_encryptPassword);
		String type = task.getString(param_type);
		String userID = task.getString(param_userID);
		String password = task.getString(param_password);

		logger.debug("Received OTP login request. subscriberID: "
				+ subscriberID + ", type: " + type				
				+ ", encryptPassword: " + encryptPassword);
		
		// By default, generate password and encrypt password are true.
		boolean isEncryptPassword = ("n"
				.equalsIgnoreCase(encryptPassword)) ? false : true;

		HashMap<String, String> userInfo = new HashMap<String, String>();

		RBTLoginUser rbtLoginUser = rbtDBManager.getRBTLoginUser(
				userID, password, subscriberID, type, userInfo,
				isEncryptPassword);
		
		if(rbtLoginUser != null) {
			return SUCCESS;
		}
		
		return FAILURE;
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkActivation
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkActivation(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			String allowSmOffer = task.getString(param_isAllowSmOffer);
			String subscriptionClass = null;
			boolean isAllowSmOffer = false;
			if (allowSmOffer.equalsIgnoreCase(YES)
					&& task.containsKey(param_subscriptionClass)) {
				isAllowSmOffer = true;
				subscriptionClass = task.getString(param_subscriptionClass);
			}

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			String threadName = Thread.currentThread().getName();
			while ((line = bufferedReader.readLine()) != null) {
				boolean offerSuccess = true;

				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				tempTask.put(param_fromBulkTask, "true");
				String actResponse = ERROR;
				boolean isRequestPending = false;

				if (isAllowSmOffer) {
					offerSuccess = getOfferForBulkActivation(subscriberID,
							tempTask);
					if (!offerSuccess) {
						actResponse = "OFFER NOT AVAILABLE";
					}
				}

				try {
					if (!DuplicateRequestHandler.addPendingRequestToMap(
							subscriberID, threadName)) {
						logger.warn("Request pending for the subscriber : "
								+ subscriberID
								+ ", ThreadName : "
								+ DuplicateRequestHandler
										.getThreadNameForRequestPendingSubscriber(subscriberID));
						actResponse = REQUEST_PENDING;
					} else {
						isRequestPending = true;
						if (offerSuccess) {
							actResponse = processActivation(tempTask);
						}
					}
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				} finally {
					if (isRequestPending)
						DuplicateRequestHandler
								.removePendingRequestFromMap(subscriberID);
				}

				if (actResponse.equals(SUCCESS)) {
					success.append("Activated ").append(subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Activation failed for ");
					failure.append(subscriberID).append("(")
							.append(actResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Activation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String getDownloadOfTheDayEntries(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedWriter bufferedWriter = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyy");
			String filePath = getParamAsString("COMMON",
					"DOWNLOAD_OF_DAY_FILE_PATH", null);
			Date startDate = dateFormat.parse(task
					.getString(param_selectionStartTime));
			Date endDate = dateFormat.parse(task
					.getString(param_selectionEndTime));
			logger.info("File path : " + filePath + "  startDAte : "
					+ startDate + " EndDate : " + endDate);
			if (filePath == null || startDate == null || endDate == null)
				return response;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String start = sdf.format(startDate);
			String end = sdf.format(endDate);
			String circleId = getParamAsString("COMMON", "DEFAULT_CIRCLE_ID",
					"ALL");
			PickOfTheDay[] picks = rbtDBManager.getDownloadOfTheDays(start,
					end, circleId);
			file = new File(filePath);
			FileWriter fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			if (picks == null || picks.length < 1)
				return response;
			for (int i = 0; i < picks.length; i++) {
				Date playDate = sdf1.parse(picks[i].playDate());
				String finalDate = sdf2.format(playDate);
				int clipid = picks[i].clipID();
				Clip clip = rbtCacheManager.getClip(clipid);
				if (clip == null || clip.getClipEndTime().before(new Date()))
					bufferedWriter.write(finalDate
							+ ", Clip cannot be found or is expired ,"
							+ picks[i].categoryID() + "," + picks[i].profile());
				else
					bufferedWriter.write(finalDate + ","
							+ clip.getClipPromoId() + ","
							+ picks[i].categoryID() + "," + picks[i].profile());
				bufferedWriter.newLine();
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return ERROR;
		} finally {
			try {
				if (bufferedWriter != null)
					bufferedWriter.close();
				if (file != null)
					task.put(param_bulkTaskResultFile, file.getAbsolutePath());
			} catch (IOException e) {
				logger.error("", e);
			}
		}

		return response;
	}

	@Override
	public String processDownloadOfDayInsertion(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			// Get the file path
			String filePath = task.getString(param_bulkTaskFile);
			String circleId = getParamAsString("COMMON", "DEFAULT_CIRCLE_ID",
					"ALL");
			logger.info("File path is " + filePath + " CircleId is " + circleId);
			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			ArrayList<String> activationDODDateList = rbtDBManager
					.getAllDownloadOfTheDayDates("ACT");
			ArrayList<String> renewalDODDateList = rbtDBManager
					.getAllDownloadOfTheDayDates("REN");
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
			SimpleDateFormat dbsdf = new SimpleDateFormat("dd/MM/yyyy");
			String line = null;
			// File format
			// Date(mmddyyyy),clippromoid/clipid,categoryid,activation/renewal)
			while ((line = bufferedReader.readLine()) != null) {
				boolean updated = false;
				line = line.trim();
				if (line.length() == 0)
					continue;

				String[] fileParameters = line.split(",");
				logger.info(fileParameters);
				if (fileParameters == null || fileParameters.length < 4)
					continue;

				String date = fileParameters[0].trim();
				Date fileDate = sdf.parse(date);
				String finalDate = dbsdf.format(fileDate);
				String promoId = fileParameters[1].trim();
				Clip clip = rbtCacheManager.getClipByPromoId(promoId);
				logger.info("Clip corresponding to the promoId is " + clip);
				if (clip == null || clip.getClipEndTime().before(new Date())) {
					failureCount++;
					failure.append(
							"Clip does not exist or is expired for date ")
							.append(finalDate);
					failure.append(System.getProperty("line.separator"));
					continue;
				}
				int clipId = clip.getClipId();
				int categId = Integer.parseInt(fileParameters[2].trim());
				String type = fileParameters[3].trim();
				try {
					if (type.equalsIgnoreCase("REN")) {
						if (renewalDODDateList.contains(finalDate)) {
							if (rbtDBManager.updatePickOfTheDay(categId,
									clipId, finalDate, circleId, 'b', type)) {
								successCount++;
								success.append("Updated entry for ").append(
										finalDate);
								success.append(System
										.getProperty("line.separator"));
							} else {
								failureCount++;
								failure.append("Update failed for entry ")
										.append(finalDate);
								failure.append(System
										.getProperty("line.separator"));
							}
							updated = true;
						}
					} else {
						if (activationDODDateList.contains(finalDate)) {
							if (rbtDBManager.updatePickOfTheDay(categId,
									clipId, finalDate, circleId, 'b', type)) {
								successCount++;
								success.append("Updated entry for ").append(
										finalDate);
								success.append(System
										.getProperty("line.separator"));
							} else {
								failureCount++;
								failure.append("Update failed for entry ")
										.append(finalDate);
								failure.append(System
										.getProperty("line.separator"));
							}
							updated = true;
						}
					}

					if (!updated) {
						PickOfTheDay pick = rbtDBManager
								.insertPickOfTheDay(categId, clipId, finalDate,
										circleId, 'b', type);
						if (pick != null) {
							successCount++;
							success.append("Inserted entry for ").append(
									finalDate);
							success.append(System.getProperty("line.separator"));
						} else {
							failureCount++;
							failure.append("Insert failed for entry ").append(
									finalDate);
							failure.append(System.getProperty("line.separator"));
						}
					}
				} catch (Exception e) {
					response = ERROR;
					failureCount++;
					failure.append("Insert failed for entry ")
							.append(finalDate);
					failure.append(System.getProperty("line.separator"));

				}

			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter
							.write("Download Of The Day Upload Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processBulkDeactivation
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkDeactivation(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			String threadName = Thread.currentThread().getName();
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				tempTask.put(param_fromBulkTask, "true");
				String deactResponse = ERROR;
				boolean isRequestPending = false;
				try {
					if (!DuplicateRequestHandler.addPendingRequestToMap(
							subscriberID, threadName)) {
						logger.warn("Request pending for the subscriber : "
								+ subscriberID
								+ ", ThreadName : "
								+ DuplicateRequestHandler
										.getThreadNameForRequestPendingSubscriber(subscriberID));
						deactResponse = REQUEST_PENDING;
					} else {
						isRequestPending = true;
						deactResponse = processDeactivation(tempTask);
					}
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				} finally {
					if (isRequestPending)
						DuplicateRequestHandler
								.removePendingRequestFromMap(subscriberID);
				}

				if (deactResponse.equals(SUCCESS)) {
					success.append("Deactivated ").append(subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Deactivation failed for ");
					failure.append(subscriberID).append("(")
							.append(deactResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Deactivation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkSelection
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkSelection(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);
			boolean isShuffleSelection = task
					.containsKey(param_isShuffleSelection)
					&& task.getString(param_isShuffleSelection)
							.equalsIgnoreCase(YES);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;

			HashMap<String, List<String>> circleBaseHashMap = new HashMap<String, List<String>>();

			boolean isPromoID = task.containsKey(param_isPromoID)
					&& task.getString(param_isPromoID).equalsIgnoreCase(YES);
			boolean isSmsAlias = task.containsKey(param_isSmsAlias)
					&& task.getString(param_isSmsAlias).equalsIgnoreCase(YES);
			String threadName = Thread.currentThread().getName();
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = null;
				String toneID = null;
				String subscriberType = null;
				String freePeriod = null;

				String clipID = null;
				String shuffleID = null;

				String[] tokens = line.split(",");
				if (tokens.length >= 1)
					subscriberID = tokens[0].trim();
				if (tokens.length >= 2)
					toneID = tokens[1].trim();
				if (tokens.length >= 3)
					subscriberType = tokens[2].trim();
				if (tokens.length >= 4)
					freePeriod = tokens[3].trim();

				// Check subscriber is local or remote
				// if subscriber is local put the line in hash map
				tempTask.clear();
				tempTask.put(param_subscriberID, subscriberID);
				SubscriberDetail subscriberDetail = DataUtils
						.getSubscriberDetail(tempTask);
				if (subscriberDetail == null
						|| (!subscriberDetail.isValidSubscriber() && subscriberDetail
								.getCircleID() == null)) {
					failure.append("Selection failed for ");
					failure.append(subscriberID).append("(")
							.append(INVALID_PREFIX).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
					continue;
				} else if (!subscriberDetail.isValidSubscriber()
						&& subscriberDetail.getCircleID() != null) {
					String circleId = subscriberDetail.getCircleID();
					List<String> list = circleBaseHashMap.get(circleId);
					if (list == null) {
						list = new ArrayList<String>();
						circleBaseHashMap.put(circleId, list);
					}
					list.add(line);
					continue;
				}

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				if (toneID != null) {
					if (isShuffleSelection) {
						shuffleID = toneID;

						if (isPromoID) {
							tempTask.put(param_categoryPromoID, shuffleID);
							tempTask.remove(param_categoryID);
						} else if (isSmsAlias) {
							tempTask.put(param_categorySmsAlias, shuffleID);
							tempTask.remove(param_categoryID);
						} else
							tempTask.put(param_categoryID, shuffleID);
					} else {
						if (isPromoID)
							tempTask.put(param_clipPromoID, toneID);
						else if (isSmsAlias)
							tempTask.put(param_clipSmsAlias, toneID);
						else
							tempTask.put(param_clipID, toneID);
					}
				}
				// If promoID starts with the configured String, Removes the
				// leading string
				removeLeadingString(tempTask);

				if (tempTask.containsKey(param_clipID))
					clipID = tempTask.getString(param_clipID);
				else if (tempTask.containsKey(param_clipPromoID))
					clipID = tempTask.getString(param_clipPromoID);
				else if (tempTask.containsKey(param_clipSmsAlias))
					clipID = tempTask.getString(param_clipSmsAlias);

				if (clipID != null) {
					Clip clip = getClip(tempTask);
					if (clip != null)
						tempTask.put(session_clip, clip);
				}

				if (freePeriod != null)
					tempTask.put(param_freePeriod, freePeriod);
				if (subscriberType != null)
					tempTask.put(param_isPrepaid, (subscriberType
							.equalsIgnoreCase("PREPAID") ? YES : NO));

				tempTask.put(param_fromBulkTask, "true");
				String selResponse = ERROR;
				boolean isRequestPending = false;
				try {
					if (!DuplicateRequestHandler.addPendingRequestToMap(
							subscriberID, threadName)) {
						logger.warn("Request pending for the subscriber : "
								+ subscriberID
								+ ", ThreadName : "
								+ DuplicateRequestHandler
										.getThreadNameForRequestPendingSubscriber(subscriberID));
						selResponse = REQUEST_PENDING;
					} else {
						isRequestPending = true;
						selResponse = processSelection(tempTask);
					}
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				} finally {
					if (isRequestPending)
						DuplicateRequestHandler
								.removePendingRequestFromMap(subscriberID);
				}

				if (selResponse.contains(SUCCESS)) {
					if (tempTask.containsKey(param_activatedNow))
						success.append("Activated ").append(subscriberID)
								.append(" and added selection");
					else
						success.append("Added selection for ").append(
								subscriberID);

					if (shuffleID != null)
						success.append(" with shuffle ").append(shuffleID);
					else if (clipID != null)
						success.append(" with clip ").append(clipID);
					else if (task.containsKey(param_cricketPack))
						success.append(" with ckricket pack ").append(
								task.getString(param_cricketPack));

					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Selection failed for ");
					failure.append(subscriberID).append("(")
							.append(selResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;

					// Send the error SMS for failure
					Utility.sendErrorSMSForBulkRequestFailure(tempTask,
							subscriberID, selResponse, this);
				}
			}

			if (!circleBaseHashMap.isEmpty()) {
				int count[] = DataUtils.processBulkTaskForRemoteSubscriber(
						circleBaseHashMap, success, failure, task);
				successCount = successCount + count[0];
				failureCount = failureCount + count[1];

			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Selection Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("--------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("--------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	// JiraID-RBT-4187:Song upgradation through bulk process
	/*
	 * It will upgrade the song selection class through bulk process
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkSelection
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkSelectionUpgradation(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);
			boolean isShuffleSelection = task
					.containsKey(param_isShuffleSelection)
					&& task.getString(param_isShuffleSelection)
							.equalsIgnoreCase(YES);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;

			HashMap<String, List<String>> circleBaseHashMap = new HashMap<String, List<String>>();

			boolean isPromoID = task.containsKey(param_isPromoID)
					&& task.getString(param_isPromoID).equalsIgnoreCase(YES);
			boolean isSmsAlias = task.containsKey(param_isSmsAlias)
					&& task.getString(param_isSmsAlias).equalsIgnoreCase(YES);
			String threadName = Thread.currentThread().getName();
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = null;
				String toneID = null;
				String subscriberType = null;
				String freePeriod = null;

				String clipID = null;
				String shuffleID = null;

				String[] tokens = line.split(",");
				if (tokens.length >= 1)
					subscriberID = tokens[0].trim();
				if (tokens.length >= 2)
					toneID = tokens[1].trim();
				if (tokens.length >= 3)
					subscriberType = tokens[2].trim();
				if (tokens.length >= 4)
					freePeriod = tokens[3].trim();

				// Check subscriber is local or remote
				// if subscriber is local put the line in hash map
				tempTask.clear();
				tempTask.put(param_subscriberID, subscriberID);
				SubscriberDetail subscriberDetail = DataUtils
						.getSubscriberDetail(tempTask);
				if (subscriberDetail == null
						|| (!subscriberDetail.isValidSubscriber() && subscriberDetail
								.getCircleID() == null)) {
					failure.append("Selection failed for ");
					failure.append(subscriberID).append("(")
							.append(INVALID_PREFIX).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
					continue;
				} else if (!subscriberDetail.isValidSubscriber()
						&& subscriberDetail.getCircleID() != null) {
					String circleId = subscriberDetail.getCircleID();
					List<String> list = circleBaseHashMap.get(circleId);
					if (list == null) {
						list = new ArrayList<String>();
						circleBaseHashMap.put(circleId, list);
					}
					list.add(line);
					continue;
				}

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				if (toneID != null) {
					if (isShuffleSelection) {
						shuffleID = toneID;

						if (isPromoID) {
							tempTask.put(param_categoryPromoID, shuffleID);
							tempTask.remove(param_categoryID);
						} else if (isSmsAlias) {
							tempTask.put(param_categorySmsAlias, shuffleID);
							tempTask.remove(param_categoryID);
						} else
							tempTask.put(param_categoryID, shuffleID);
					} else {
						if (isPromoID)
							tempTask.put(param_clipPromoID, toneID);
						else if (isSmsAlias)
							tempTask.put(param_clipSmsAlias, toneID);
						else
							tempTask.put(param_clipID, toneID);
					}
				}
				// If promoID starts with the configured String, Removes the
				// leading string
				removeLeadingString(tempTask);

				if (tempTask.containsKey(param_clipID))
					clipID = tempTask.getString(param_clipID);
				else if (tempTask.containsKey(param_clipPromoID))
					clipID = tempTask.getString(param_clipPromoID);
				else if (tempTask.containsKey(param_clipSmsAlias))
					clipID = tempTask.getString(param_clipSmsAlias);

				if (clipID != null) {
					Clip clip = getClip(tempTask);
					if (clip != null)
						tempTask.put(session_clip, clip);
				}

				if (freePeriod != null)
					tempTask.put(param_freePeriod, freePeriod);
				if (subscriberType != null)
					tempTask.put(param_isPrepaid, (subscriberType
							.equalsIgnoreCase("PREPAID") ? YES : NO));

				tempTask.put(param_fromBulkTask, "true");
				String selResponse = ERROR;
				boolean isRequestPending = false;
				try {
					if (!DuplicateRequestHandler.addPendingRequestToMap(
							subscriberID, threadName)) {
						logger.warn("Request pending for the subscriber : "
								+ subscriberID
								+ ", ThreadName : "
								+ DuplicateRequestHandler
										.getThreadNameForRequestPendingSubscriber(subscriberID));
						selResponse = REQUEST_PENDING;
					} else {
						isRequestPending = true;
						selResponse = processUpgradeAllSelections(tempTask);
					}
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				} finally {
					if (isRequestPending)
						DuplicateRequestHandler
								.removePendingRequestFromMap(subscriberID);
				}

				if (selResponse.contains(SUCCESS)) {
					success.append("Upgrade selection for ").append(
							subscriberID);

					if (shuffleID != null)
						success.append(" with shuffle ").append(shuffleID);
					else if (clipID != null)
						success.append(" with clip ").append(clipID);
					else if (task.containsKey(param_cricketPack))
						success.append(" with ckricket pack ").append(
								task.getString(param_cricketPack));

					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Selection failed for ");
					failure.append(subscriberID).append("(")
							.append(selResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;

					// Send the error SMS for failure
					Utility.sendErrorSMSForBulkRequestFailure(tempTask,
							subscriberID, selResponse, this);
				}
			}

			if (!circleBaseHashMap.isEmpty()) {
				int count[] = DataUtils.processBulkTaskForRemoteSubscriber(
						circleBaseHashMap, success, failure, task);
				successCount = successCount + count[0];
				failureCount = failureCount + count[1];

			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Upgradation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("--------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("--------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processBulkDeleteSelection
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkDeleteSelection(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				tempTask.put(param_fromBulkTask, "true");
				String delSettingResponse = deleteSetting(tempTask);
				if (delSettingResponse.equals(SUCCESS)) {
					success.append("Deleted selection for ").append(
							subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Failed to delete selection for ");
					failure.append(subscriberID).append("(")
							.append(delSettingResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Delete Selection Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processBulkSetSubscriberDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkSetSubscriberDetails(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				String updateResponse = setSubscriberDetails(tempTask);
				if (updateResponse.equals(SUCCESS)) {
					success.append("Updated ").append(subscriberID)
							.append(" status");
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Failed to update the status for ");
					failure.append(subscriberID).append("(")
							.append(updateResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Updation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("-------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("-------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processBulkGetSubscriberDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkGetSubscriberDetails(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();
		StringBuilder unprocessed = new StringBuilder();
		String info = task.getString(param_info);
		int successCount = 0;
		int failureCount = 0;
		int unprocessedCount = 0;
		try {
			if (info.equalsIgnoreCase(LOGIN_USER)) {
				String type = task.getString(param_type);
				RBTLoginUser[] rbtLoginUsers = rbtDBManager
						.getRBTLoginUsers(type);

				success.append("USER NAME,USER TYPE,PARENT ADMIN,REQUEST MODE,PRIVILEGE USER,ASK PASSWORD,READ ONLY USER,MAIN MENU ORDER,SUB MENU SUBSCRIBER,SUB MENU BULK,SUB MENU SETTINGS,FAILED LOGIN COUNT,TEMPORARY PASSWORD,STATUS,CIRCLES ALLOWED");
				if (rbtLoginUsers != null) {
					for (RBTLoginUser rbtLoginUser : rbtLoginUsers) {
						HashMap<String, String> userInfoMap = rbtLoginUser
								.userInfo();

						String userID = rbtLoginUser.userID();
						String userType = "Normal";
						String parentAdmin = "Admin";
						String requestMode = "--";
						String privilegeUser = "FALSE";
						String askPassword = "TRUE";
						String readOnlyUser = "FALSE";
						String mainMenuOrder = "--";
						String subMenuSubscriber = "--";
						String subMenuBulk = "--";
						String subMenuSettings = "--";
						String failedLoginCount = "0";
						String tempPass = "FALSE";
						String status = "Active";
						String circlesAllowed = "--";

						if (userInfoMap != null) {
							if (userInfoMap.containsKey("USER_TYPE"))
								userType = userInfoMap.get("USER_TYPE");

							if (userInfoMap.containsKey("PARENT_ADMIN"))
								parentAdmin = userInfoMap.get("PARENT_ADMIN");

							if (userInfoMap.containsKey("REQUEST_MODE"))
								requestMode = userInfoMap.get("REQUEST_MODE");

							if (userInfoMap.containsKey("PRIVILEGE_USER"))
								privilegeUser = userInfoMap.get(
										"PRIVILEGE_USER").toUpperCase();

							if (userInfoMap.containsKey("ASK_PWD"))
								askPassword = userInfoMap.get("ASK_PWD")
										.toUpperCase();

							if (userInfoMap.containsKey("READ_ONLY_USER"))
								readOnlyUser = userInfoMap
										.get("READ_ONLY_USER").toUpperCase();

							if (userInfoMap.containsKey("MAIN_MENU_ORDER")) {
								mainMenuOrder = "";
								String[] menuTokens = userInfoMap.get(
										"MAIN_MENU_ORDER").split(",");
								for (String menu : menuTokens) {
									Parameters menuParam = parametersCacheManager
											.getParameter("CCC", "MAIN_MENU_"
													+ menu, null);

									if (menuParam != null) {
										String[] values = menuParam.getValue()
												.split(":");
										mainMenuOrder += values[values.length - 1]
												+ "|";
									}

									if (userInfoMap
											.containsKey("SUB_MENU_ORDER_"
													+ menu)) {
										String subMenuOrder = "";
										String[] subMenuTokens = userInfoMap
												.get("SUB_MENU_ORDER_" + menu)
												.split(",");
										for (String subMenu : subMenuTokens) {
											Parameters subMenuParam = parametersCacheManager
													.getParameter("CCC",
															"SUB_MENU_" + menu
																	+ "_"
																	+ subMenu,
															null);
											if (subMenuParam != null) {
												String[] values = subMenuParam
														.getValue().split(":");
												subMenuOrder += values[values.length - 1]
														+ "|";
											}
										}
										subMenuOrder = subMenuOrder.substring(
												0, subMenuOrder.length() - 1);

										if (menu.equals("2"))
											subMenuSubscriber = subMenuOrder;
										else if (menu.equals("7"))
											subMenuBulk = subMenuOrder;
										else if (menu.equals("8"))
											subMenuSettings = subMenuOrder;
									}
								}
								mainMenuOrder = mainMenuOrder.substring(0,
										mainMenuOrder.length() - 1);
							}

							if (userInfoMap.containsKey("FAILED_LOGIN_COUNT"))
								failedLoginCount = userInfoMap
										.get("FAILED_LOGIN_COUNT");

							if (userInfoMap.containsKey("TEMP_PASS"))
								tempPass = userInfoMap.get("TEMP_PASS")
										.toUpperCase();

							if (userInfoMap.containsKey("STATUS"))
								status = userInfoMap.get("STATUS");

							if (userInfoMap.containsKey("CIRCLE_ID_ALLOWED"))
								circlesAllowed = userInfoMap
										.get("CIRCLE_ID_ALLOWED");
						}

						success.append(System.getProperty("line.separator"));
						success.append(userID).append(",");
						success.append(userType).append(",");
						success.append(parentAdmin).append(",");
						success.append(requestMode).append(",");
						success.append(privilegeUser).append(",");
						success.append(askPassword).append(",");
						success.append(readOnlyUser).append(",");
						success.append(mainMenuOrder).append(",");
						success.append(subMenuSubscriber).append(",");
						success.append(subMenuBulk).append(",");
						success.append(subMenuSettings).append(",");
						success.append(failedLoginCount).append(",");
						success.append(tempPass).append(",");
						success.append(status).append(",");
						success.append(circlesAllowed);
					}
				}
			} else {
				String filePath = task.getString(param_bulkTaskFile);

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");

				file = new File(filePath);
				FileReader fileReader = new FileReader(file);
				bufferedReader = new BufferedReader(fileReader);
				if (info.equalsIgnoreCase(SUBSCRIBER_STATUS)) {
					success.append("SUBSCRIBER_ID,ACT_STATUS,SUBSCRIBER_TYPE,ACTIVATED_BY,ACTIVATED_ON,DEACTIVATED_BY,DEACTIVATED_ON,LAST_ACCESS_DATE,"
							+ "CALLER_ID,CATEGORY,CLIP_NAME,PROMO_ID,CLASS_TYPE,SET_TIME,END_TIME,SELECTION_STATUS");
				}
				String line = null;
				int count = 0;
				while ((line = bufferedReader.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0)
						continue;

					String subscriberID = line;

					if (count >= 10000) {
						unprocessed.append(subscriberID);
						unprocessed
								.append(System.getProperty("line.separator"));
						unprocessedCount++;
						continue;
					}
					count++;

					if (info.equalsIgnoreCase(BLACKLIST)) {
						String blacklistType = task
								.getString(param_blacklistType);
						if (blacklistType == null)
							blacklistType = "TOTAL";
						blacklistType = blacklistType.toUpperCase();

						ViralBlackListTable viralBlackList = rbtDBManager
								.getViralBlackList(subscriberID, blacklistType);
						if (viralBlackList != null) {
							success.append(subscriberID)
									.append(": ")
									.append(dateFormat.format(viralBlackList
											.startTime()));
							success.append(System.getProperty("line.separator"));
						} else {
							failure.append(subscriberID).append(" is not a ")
									.append(blacklistType)
									.append(" blacklist subscriber");
							failure.append(System.getProperty("line.separator"));
						}
					}
					if (info.equalsIgnoreCase(SUBSCRIBER_STATUS)) {
						Subscriber subscriber = rbtDBManager
								.getSubscriber(subscriberID);
						SubscriberStatus[] subscriberStatus = rbtDBManager
								.getSubscriberRecords(subscriberID);

						if (subscriber != null) {
							success.append(System.getProperty("line.separator"));
							success.append(subscriberID).append(",");
							String status = Utility.getSubscriberStatus(
									subscriber, true);
							if (subscriber.subYes().equalsIgnoreCase(
									iRBTConstant.STATE_ACTIVATION_PENDING)) {
								String configuredModesToStatus = null;
								if (subscriber.oldClassType() == null) {
									configuredModesToStatus = RBTParametersUtils
											.getParamAsString(
													iRBTConstant.COMMON,
													"MODES_TO_STATUS_MAPPING_FOR_NULL_OLDCLASS",
													null);
								} else {
									configuredModesToStatus = RBTParametersUtils
											.getParamAsString(
													iRBTConstant.COMMON,
													"MODES_TO_STATUS_MAPPING_FOR_NOTNULL_OLDCLASS",
													null);
								}
								if (configuredModesToStatus != null
										&& configuredModesToStatus.indexOf(":") != -1) {
									String mode = task.getString(param_mode);
									HashMap<String, String> map = new HashMap<String, String>();
									String[] modeStatusArray = configuredModesToStatus
											.split(",");
									if (modeStatusArray != null) {
										for (int i = 0; i < modeStatusArray.length; i++) {
											String str[] = modeStatusArray[i]
													.split(":");
											if (str.length == 2)
												map.put(str[0], str[1]);
										}
									}
									if (map.containsKey(mode))
										status = map.get(mode);
								}
							}

							success.append(status).append(",");
							success.append(
									subscriber.prepaidYes() ? "Prepaid"
											: "Postpaid").append(",");
							success.append(
									getValidOne(subscriber.activatedBy()))
									.append(",");
							success.append(
									getValidOne(subscriber.activationDate()))
									.append(",");
							success.append(
									getValidOne(subscriber.deactivatedBy()))
									.append(",");

							Date lastDeactivationDate = subscriber
									.lastDeactivationDate();
							if (rbtDBManager
									.isSubscriberDeactivationPending(subscriber)
									|| rbtDBManager
											.isSubscriberDeactivated(subscriber)) {
								lastDeactivationDate = subscriber.endDate();
							}
							success.append(getValidOne(lastDeactivationDate))
									.append(",");
							success.append(subscriber.accessDate()).append(",");
							if (subscriberStatus != null
									&& subscriberStatus.length > 0) {
								for (int i = 0; i < subscriberStatus.length; i++) {
									// in case of multiple selections,
									// subscriber details required only in one
									// row
									if (i > 0) {
										success.append(System
												.getProperty("line.separator"));
										success.append(",,,,,,,,");
									}
									success.append(
											getValidOne(subscriberStatus[i]
													.callerID())).append(",");
									int catID = subscriberStatus[i]
											.categoryID();
									Category category = rbtCacheManager
											.getCategory(catID);
									if (category != null) {
										String categoryName = category
												.getCategoryName();
										success.append(categoryName)
												.append(",");
									} else {
										success.append(",");
									}
									if (Utility
											.isShuffleCategory(subscriberStatus[i]
													.categoryType())) {
										if (category != null) {
											String categoryName = category
													.getCategoryName();
											success.append(categoryName)
													.append(",");
											String promoId = category
													.getCategoryPromoId();
											if (promoId != null)
												promoId = promoId.replaceAll(
														",", "|");
											success.append(promoId).append(",");
										} else {
											success.append(",,");
										}
									} else {
										String rbtWavFileName = subscriberStatus[i]
												.subscriberFile();
										Clip clip = rbtCacheManager
												.getClipByRbtWavFileName(rbtWavFileName);
										if (clip != null) {
											String clipName = clip
													.getClipName();
											success.append(clipName)
													.append(",");
											String promoId = clip
													.getClipPromoId();
											if (promoId != null)
												promoId = promoId.replaceAll(
														",", "|");
											success.append(promoId).append(",");
										} else {
											success.append(",,");
										}
									}
									success.append(
											subscriberStatus[i].classType())
											.append(",");
									success.append(
											getValidOne(subscriberStatus[i]
													.setTime())).append(",");
									success.append(
											getValidOne(subscriberStatus[i]
													.endTime())).append(",");
									success.append(
											Utility.getSubscriberSettingStatus(
													subscriberStatus[i], true))
											.append(",");
									successCount++;
								}
							} else {
								success.append(",,,,,,,,");
							}
						} else {
							failure.append(System.getProperty("line.separator"));
							failure.append(subscriberID).append(
									",does not exists ");
							failureCount++;
						}

					}
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				if (info.equalsIgnoreCase(LOGIN_USER)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					String resultFileName = "LoginUsers_"
							+ dateFormat.format(new Date()) + ".csv";
					File resultFile = new File(resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write(success.toString());
				} else if (info.equalsIgnoreCase(SUBSCRIBER_STATUS)) {
					if (bufferedReader != null)
						bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.csv";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);

					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());
					bufferedWriter.close();
					fileWriter.close();

				} else {

					if (bufferedReader != null)
						bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);

					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write(info.toUpperCase() + " Details");
					bufferedWriter.newLine();
					bufferedWriter.write("-------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());
					bufferedWriter.close();
					fileWriter.close();
				}
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	private static Object getValidOne(Object input) {
		if (input == null) {
			return "";
		}
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkUploadTask
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String uploadNprocessBulkTask(WebServiceContext task) {
		String response = processBulkUpload(task);

		if (response.equals(SUCCESS))
			response = processBulkTask(task);

		return response;
	}

	/*
	 * gets the Bulk file, Creates the new task (RBT_BULK_UPLOAD_TASKS), parses
	 * the file and make entries into RBT_BULK_UPLOAD_SUBSCRIBERS table
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkUpload
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkUpload(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;
		StringBuilder result = new StringBuilder();

		RBTBulkUploadTask rbtBulkUploadTask = null;

		int successCount = 0;
		int failureCount = 0;
		int taskID = -1;
		String action = action_upload;

		String filePath = task.getString(param_bulkTaskFile);
		String circleID = task.getString(param_circleID);
		action = task.getString(param_action);

		try {
			if (filePath == null || circleID == null) {
				response = INVALID_PARAMETER;
			} else {
				file = new File(filePath);
				bufferedReader = new BufferedReader(new FileReader(file));

				// check if there is any task being processed
				List<RBTBulkUploadTask> inProgressTaskList = RBTBulkUploadTaskDAO
						.getRBTBulkTasks(BULKTASK_STATUS_INIT, null, null, null);

				if (inProgressTaskList.size() > 0) {
					// can process only one task at any time
					response = ANOTHER_TASK_IN_PROGRESS;
				} else {
					rbtBulkUploadTask = new RBTBulkUploadTask();

					int selectionType = 1; // Normal selection
					if (task.containsKey(param_status))
						selectionType = Integer.valueOf(task
								.getString(param_status));

					rbtBulkUploadTask.setTaskName(task
							.getString(param_taskName));
					rbtBulkUploadTask.setCircleId(circleID);
					rbtBulkUploadTask.setActivationClass(task
							.getString(param_subscriptionClass));
					rbtBulkUploadTask.setSelectionClass(task
							.getString(param_chargeClass));
					rbtBulkUploadTask.setSelectionType(selectionType);
					rbtBulkUploadTask.setTaskType(task
							.getString(param_taskType));
					rbtBulkUploadTask
							.setActivatedBy(task.getString(param_mode));
					rbtBulkUploadTask
							.setActInfo(task.getString(param_modeInfo));
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_INIT); // taskStatus
																			// to
																			// -1
					rbtBulkUploadTask.setTaskMode(task
							.getString(param_taskMode));

					// If subscriptionClass is not passed for corporate bulk
					// upload,
					// then subscriptionClass is read from RBT_PARAMETERS table.
					if (!task.containsKey(param_subscriptionClass)
							&& task.containsKey(param_taskType)
							&& task.getString(param_taskType).equalsIgnoreCase(
									BULKACTION_CORPORATE)) {
						String subClass = RBTParametersUtils.getParamAsString(
								iRBTConstant.COMMON,
								"DEFAULT_CORPORATE_SUB_CLASS", null);
						if (subClass != null)
							rbtBulkUploadTask.setActivationClass(subClass);
					}

					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");

					if (task.containsKey(param_selectionStartTime)) {
						Date processTime = dateFormat.parse(task
								.getString(param_selectionStartTime));
						if (task.containsKey(param_taskType)
								&& task.getString(param_taskType)
										.equalsIgnoreCase(BULKACTION_CORPORATE)) {
							// If taskType is CORPORATE, adding (fromTime-1)
							// hours to the campaign startTime
							// This Change is made to avoid sending activation
							// sms to the users on 12 AM.
							// If fromTime is 7 AM, campaign startTime will be
							// -> (0 AM + 6 Hours)
							// Currently CCC GUI has no provision to select
							// startTime (GUI Calendar has provision to select
							// StartDate only)
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(processTime);

							int fromTimeHours = task
									.containsKey(param_fromTime) ? Integer
									.parseInt(task.getString(param_fromTime))
									: 1;
							calendar.add(Calendar.HOUR_OF_DAY,
									(calendar.get(Calendar.HOUR_OF_DAY)
											+ fromTimeHours - 1));

							processTime = calendar.getTime();
						}
						rbtBulkUploadTask.setProcessTime(processTime);
					}
					if (task.containsKey(param_selectionEndTime)) {
						String endTime = task.getString(param_selectionEndTime);
						if (endTime.endsWith("000000000")) {
							endTime = endTime.substring(0, 8);
							endTime = endTime + "235959000";
						}
						rbtBulkUploadTask.setEndTime(dateFormat.parse(endTime));
					}

					// prepare taskInfo from parameters
					HashMap<String, String> taskInfoMap = new HashMap<String, String>();
					if (task.containsKey(param_isShuffleSelection))
						taskInfoMap.put(param_isShuffleSelection,
								task.getString(param_isShuffleSelection));
					if (task.containsKey(param_isDeactivateCorporateUser))
						taskInfoMap
								.put(param_isDeactivateCorporateUser,
										task.getString(param_isDeactivateCorporateUser));
					if (task.containsKey(param_callerID))
						taskInfoMap.put(param_callerID,
								task.getString(param_callerID));
					if (task.containsKey(param_categoryID))
						taskInfoMap.put(param_categoryID,
								task.getString(param_categoryID));
					if (task.containsKey(param_ignoreActiveUser))
						taskInfoMap.put(param_ignoreActiveUser,
								task.getString(param_ignoreActiveUser));
					if (task.containsKey(param_dontSMSInBlackOut))
						taskInfoMap.put(param_dontSMSInBlackOut,
								task.getString(param_dontSMSInBlackOut));
					if (task.containsKey(param_removeExistingSetting))
						taskInfoMap.put(param_removeExistingSetting,
								task.getString(param_removeExistingSetting));
					if (task.containsKey(param_cricketPack))
						taskInfoMap.put(param_cricketPack,
								task.getString(param_cricketPack));
					if (task.containsKey(param_useUIChargeClass))
						taskInfoMap.put(param_useUIChargeClass,
								task.getString(param_useUIChargeClass));
					if (task.containsKey(param_cosID))
						taskInfoMap.put(param_cosID,
								task.getString(param_cosID));
					if (task.containsKey(param_isPromoID))
						taskInfoMap.put(param_isPromoID,
								task.getString(param_isPromoID));
					if (task.containsKey(param_isSmsAlias))
						taskInfoMap.put(param_isSmsAlias,
								task.getString(param_isSmsAlias));
					if (task.containsKey(param_inLoop))
						taskInfoMap.put(param_inLoop,
								task.getString(param_inLoop));
					if (task.containsKey(param_optInOutModel))
						taskInfoMap.put(param_optInOutModel,
								task.getString(param_optInOutModel));
					if (task.containsKey(param_chargingModel))
						taskInfoMap.put(param_chargingModel,
								task.getString(param_chargingModel));

					if (task.containsKey(param_rbtFile))
						taskInfoMap.put(param_rbtFile,
								task.getString(param_rbtFile));
					if (task.containsKey(param_clipID)) {
						String clipids[] = task.getString(param_clipID).split(
								",");
						taskInfoMap.put(param_clipID, clipids[0]);
					}
					if (task.containsKey(param_fromTime))
						taskInfoMap.put(param_fromTime,
								task.getString(param_fromTime));
					if (task.containsKey(param_fromTimeMinutes))
						taskInfoMap.put(param_fromTimeMinutes,
								task.getString(param_fromTimeMinutes));
					if (task.containsKey(param_toTime))
						taskInfoMap.put(param_toTime,
								task.getString(param_toTime));
					if (task.containsKey(param_toTimeMinutes))
						taskInfoMap.put(param_toTimeMinutes,
								task.getString(param_toTimeMinutes));
					if (task.containsKey(param_interval))
						taskInfoMap.put(param_interval,
								task.getString(param_interval));
					if (task.containsKey(param_rentalPack))
						taskInfoMap.put(param_rentalPack,
								task.getString(param_rentalPack));

					// Added to support only deactivating
					if (task.containsKey(param_isCorporateDeactivation))
						taskInfoMap.put(param_isCorporateDeactivation,
								task.getString(param_isCorporateDeactivation));

					Set<String> keySet = task.keySet();
					for (String key : keySet) {
						if (key.startsWith(param_userInfo + "_")
								|| key.startsWith(param_selectionInfo + "_"))
							taskInfoMap.put(key, task.getString(key));
					}

					if (task.containsKey(param_isAllowSmOffer)) {
						taskInfoMap.put(param_isAllowSmOffer,
								task.getString(param_isAllowSmOffer));
					}

					String taskInfo = DBUtility
							.getAttributeXMLFromMap(taskInfoMap);
					rbtBulkUploadTask.setTaskInfo(taskInfo); // Task info

					// create the bulk task with status -1 i.e upload processing
					taskID = RBTBulkUploadTaskDAO
							.createBulkUploadTask(rbtBulkUploadTask);

					if (taskID != -1) {
						int maxBulkCountPerCircle = Integer.MAX_VALUE;
						Parameters bulkActivationCCCLimit = parametersCacheManager
								.getParameter(
										"CCC",
										"BULK_ACTIVATION_CCC_LIMIT_"
												+ circleID.toUpperCase());
						if (bulkActivationCCCLimit == null)
							bulkActivationCCCLimit = parametersCacheManager
									.getParameter("CCC",
											"BULK_ACTIVATION_CCC_LIMIT");

						if (bulkActivationCCCLimit != null)
							maxBulkCountPerCircle = Integer
									.parseInt(bulkActivationCCCLimit.getValue());

						HashMap<String, Integer> bulkCircleCountMap = null;
						if (!task.getString(param_taskType).equalsIgnoreCase(
								BULKACTION_CORPORATE)) {
							// get all corporate taskIDs
							Integer[] taskIDs = RBTBulkUploadTaskDAO
									.getTaskIDsByTaskType(BULKACTION_CORPORATE);
							// current status of the upload from DB
							bulkCircleCountMap = RBTBulkUploadSubscriberDAO
									.getBulkSubscriberCount(taskIDs);
						}
						boolean useSelectedClip = false;
						if (task.containsKey(param_taskType)
							&& task.getString(param_taskType).equalsIgnoreCase(
								BULKACTION_CORPORATE) && task.containsKey(param_clipID)) {
							useSelectedClip = true;
						}

						boolean isConsiderAllCircle = getParamAsBoolean(
								"CCC",
								"CONSIDER_ALL_CIRCLE_IN_SAME_HUB_FOR_BULK_TASK",
								"false");
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							try {
								line = line.trim();
								if (line.length() == 0)
									continue;

								String subscriberID = null;
								String toneID = null;
								String subscriberType = null;

								String[] tokens = line.split(",");
								if (tokens.length >= 1)
									subscriberID = tokens[0].trim();

								if (task.getString(param_taskType)
										.equalsIgnoreCase(BULKACTION_ACTIVATION)) {
									if (tokens.length >= 2)
										subscriberType = tokens[1].trim();
								} else {
									if(useSelectedClip)
										toneID = task.getString(param_clipID).split(",")[0];
									else if (tokens.length >= 2)
										toneID = tokens[1].trim();
									if (tokens.length >= 3)
										subscriberType = tokens[2].trim();
								}

								String subCircleID = null;
								SubscriberDetail subscriberDetail = RbtServicesMgr
										.getSubscriberDetail(new MNPContext(
												subscriberID, "BULK"));

								if (subscriberDetail != null)
									subCircleID = subscriberDetail
											.getCircleID();

								RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
								rbtBulkUploadSubscriber.setTaskId(taskID);
								rbtBulkUploadSubscriber
										.setSubscriberId(subscriberID);
								rbtBulkUploadSubscriber
										.setCircleId(subCircleID);
								rbtBulkUploadSubscriber.setContentId(toneID);
								rbtBulkUploadSubscriber
										.setStatus(BULKTASK_STATUS_NEW);

								String isPrepaid = NO;
								if (subscriberType != null)
									isPrepaid = subscriberType
											.equalsIgnoreCase("PREPAID") ? YES
											: NO;
								else if (task.containsKey(param_isPrepaid))
									isPrepaid = task.getString(param_isPrepaid);
								else {
									Parameters userTypeParam = parametersCacheManager
											.getParameter(iRBTConstant.COMMON,
													"DEFAULT_USER_TYPE",
													"POSTPAID");
									isPrepaid = userTypeParam.getValue().trim()
											.equalsIgnoreCase("PREPAID") ? YES
											: NO;
								}
								rbtBulkUploadSubscriber
										.setSubscriberType(isPrepaid.charAt(0));

								if (!isConsiderAllCircle
										&& !circleID
												.equalsIgnoreCase(subCircleID)) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO GIVEN CIRCLE ID")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								if (isConsiderAllCircle
										&& (subscriberDetail == null || !subscriberDetail
												.isValidSubscriber())) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO CURRENT SITE")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								int processedSubscribersForCircle = 0;
								if (bulkCircleCountMap != null) {
									// Dont check max day count for Corporate
									// selections
									if (bulkCircleCountMap
											.containsKey(subCircleID))
										processedSubscribersForCircle = bulkCircleCountMap
												.get(subCircleID).intValue();

									if (processedSubscribersForCircle >= maxBulkCountPerCircle) {
										failureCount++;
										result.append(line)
												.append(",CIRCLE LIMIT REACHED")
												.append(System
														.getProperty("line.separator"));
										continue;
									}
								}

								RBTBulkUploadSubscriberDAO
										.createBulkSubscriber(rbtBulkUploadSubscriber);
								successCount++;
								result.append(line)
										.append(",SUCCESS")
										.append(System
												.getProperty("line.separator"));

								if (bulkCircleCountMap != null) {
									// Don't check max day count for Corporate
									// selections
									processedSubscribersForCircle++;
									bulkCircleCountMap.put(subCircleID,
											processedSubscribersForCircle);
								}
							} catch (Exception e) {
								// Exception in creating bulk subscriber ..
								// continue
								logger.error("", e);
							}
						}
						response = SUCCESS;

						rbtBulkUploadTask.setTaskId(taskID);
						rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_NEW);
						// task is processed completely hence updating the
						// status from processing to new
						RBTBulkUploadTaskDAO
								.updateRBTBulkUploadTask(rbtBulkUploadTask);

						task.put(param_taskID, String.valueOf(taskID));
						task.put(param_task, rbtBulkUploadTask);
					} else {
						response = FAILED;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				if (bufferedReader != null)
					bufferedReader.close();

				File resultFile = null;

				if (action.equalsIgnoreCase(action_uploadNprocess)
						&& response.equals(SUCCESS)) {
				} else {
					if (!response.equals(SUCCESS)) {
						resultFile = new File((String) null, ERROR
								+ "_result.txt");
					} else {
						String resultFileName = file.getName().substring(0,
								file.getName().indexOf(".txt"))
								+ "_result.txt";
						resultFile = new File(file.getParentFile(),
								resultFileName);
					}
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					bufferedWriter = new BufferedWriter(new FileWriter(
							resultFile));

					bufferedWriter.write("Bulk Activation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("Response :" + response);
					bufferedWriter.newLine();
					bufferedWriter.write("TaskID :" + taskID);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write(result.toString());
				}

				if (file != null)
					file.delete();
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}
		return response;
	}

	/*
	 * Gets the new task, updates the taskStatus to processing, Gets all
	 * subscribers for the given task and processes, returns the response file.
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processBulkTask
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processBulkTask(WebServiceContext task) {
		String response = ERROR;
		RBTBulkUploadTask rbtBulkUploadTask = null;
		boolean canProcessRequest = false;
		try {
			String taskID = task.getString(param_taskID);
			synchronized (syncObj) {
				rbtBulkUploadTask = RBTBulkUploadTaskDAO
						.getRBTBulkUploadTask(Integer.valueOf(taskID));
				if (rbtBulkUploadTask == null) {
					response = taskID + ":" + TASK_DOES_NOT_EXIST;
				} else if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_SUCCESS) {
					response = taskID + ":" + TASK_ALREADY_PROCESSED;
				} else if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_PROCESSING) {
					response = taskID + ":" + TASK_BEING_PROCESSED;
				} else {
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_PROCESSING);
					RBTBulkUploadTaskDAO
							.updateRBTBulkUploadTask(rbtBulkUploadTask);
					canProcessRequest = true;
				}
			}

			if (canProcessRequest) {

				boolean isCorporateSelDeactRequest = false;
				Map<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap != null
						&& taskInfoMap
								.containsKey(param_isCorporateDeactivation)) {
					isCorporateSelDeactRequest = taskInfoMap.get(
							param_isCorporateDeactivation)
							.equalsIgnoreCase(YES);
				}

				// currently handling the activation, de-activation, selection
				// and deletion
				if (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
						BULKACTION_ACTIVATION))
					response = processBulkActivationTask(task,
							rbtBulkUploadTask);

				else if (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
						BULKACTION_DEACTIVATION))
					response = processBulkDeActivationTask(task,
							rbtBulkUploadTask);

				else if (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
						BULKACTION_SELECTION)
						|| (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
								BULKACTION_CORPORATE) && !isCorporateSelDeactRequest))
					response = processBulkSelectionTask(task, rbtBulkUploadTask);

				else if (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
						BULKACTION_DELETION)
						|| (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
								BULKACTION_CORPORATE) && isCorporateSelDeactRequest))
					response = processBulkDeleteSelectionTask(task,
							rbtBulkUploadTask);

				// Updating the process task status to SUCCESS/FAILURE
				if (response.equals(SUCCESS))
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_SUCCESS);
				else if (response.equals(ERROR))
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_FAILURE);

				RBTBulkUploadTaskDAO.updateRBTBulkUploadTask(rbtBulkUploadTask);

			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (!canProcessRequest) {
				BufferedWriter bufferedWriter = null;
				try {
					String resultFileName = ERROR + "_result.txt";
					File resultFile = new File((String) null, resultFileName); // check
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Activation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("Response ::" + response);
					bufferedWriter.newLine();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		return response;
	}

	/*
	 * Processes Activation tasks
	 */
	public String processBulkActivationTask(WebServiceContext task,
			RBTBulkUploadTask rbtBulkUploadTask) {
		String response = ERROR;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String taskID = task.getString(param_taskID);
			List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
					.getRBTBulkUploadSubscribers(Integer.valueOf(taskID));

			HashMap<String, String> taskInfoMap = DBUtility
					.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());

			WebServiceContext tempTask = new WebServiceContext();

			for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
				boolean offerSuccess = true;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID,
						rbtBulkUploadSubscriber.getSubscriberId());
				if (rbtBulkUploadTask.getActivationClass() != null)
					tempTask.put(param_subscriptionClass,
							rbtBulkUploadTask.getActivationClass());

				tempTask.put(param_mode, rbtBulkUploadTask.getActivatedBy());
				tempTask.put(param_modeInfo, rbtBulkUploadTask.getActInfo());
				tempTask.put(param_isPrepaid, String
						.valueOf(rbtBulkUploadSubscriber.getSubscriberType()));

				// Copies all params in taskInfo into tempTask
				tempTask.putAll(taskInfoMap);

				if (tempTask.containsKey(param_subscriptionClass)
						&& (tempTask.containsKey(param_isAllowSmOffer) && tempTask
								.getString(param_isAllowSmOffer)
								.equalsIgnoreCase(YES))) {
					offerSuccess = getOfferForBulkActivation(
							rbtBulkUploadSubscriber.getSubscriberId(), tempTask);
				}

				if (taskInfoMap != null) {
					Set<String> keySet = taskInfoMap.keySet();
					for (String key : keySet) {
						if (key.startsWith(param_userInfo + "_"))
							tempTask.put(key, taskInfoMap.get(key));
					}
				}
				tempTask.put(param_fromBulkTask, "true");
				String actResponse = "OFFER NOT AVAILABLE";
				if (offerSuccess)
					actResponse = processActivation(tempTask);
				int status = 1; // SUCCESS
				if (actResponse.equals(SUCCESS)) {
					success.append(rbtBulkUploadSubscriber.getSubscriberId())
							.append(",");
					success.append(rbtBulkUploadSubscriber.getSubscriberType());
					success.append("  Activated");
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					status = 2; // FAILURE

					failure.append(rbtBulkUploadSubscriber.getSubscriberId())
							.append(",");
					failure.append(rbtBulkUploadSubscriber.getSubscriberType());
					failure.append("  Activation failed. Reason: ");
					failure.append("(").append(actResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}

				rbtBulkUploadSubscriber.setStatus(status);
				rbtBulkUploadSubscriber.setReason(actResponse);
				// Updating the subscriber task status
				RBTBulkUploadSubscriberDAO
						.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);

			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String resultFileName = rbtBulkUploadTask.getTaskId()
						+ "_result.txt";
				File resultFile = new File((String) null, resultFileName); // check
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write("Bulk Activation Statistics");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write("Total: " + (successCount + failureCount));
				bufferedWriter.newLine();
				bufferedWriter.write("Success: " + successCount);
				bufferedWriter.newLine();
				bufferedWriter.write("Failure: " + failureCount);
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Success Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(success.toString());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Failure Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(failure.toString());
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		return response;
	}

	/*
	 * Processes the deactivation tasks
	 */
	public String processBulkDeActivationTask(WebServiceContext task,
			RBTBulkUploadTask rbtBulkUploadTask) {
		String response = ERROR;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String taskID = task.getString(param_taskID);
			List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
					.getRBTBulkUploadSubscribers(Integer.valueOf(taskID));
			WebServiceContext tempTask = new WebServiceContext();
			HashMap<String, String> taskInfoMap = DBUtility
					.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
			boolean deactCorpUser = false;
			if (taskInfoMap != null
					&& taskInfoMap.containsKey(param_isDeactivateCorporateUser))
				deactCorpUser = true;

			for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID,
						rbtBulkUploadSubscriber.getSubscriberId());
				tempTask.put(param_mode, rbtBulkUploadTask.getActivatedBy());
				tempTask.put(param_modeInfo, rbtBulkUploadTask.getActInfo());
				if (deactCorpUser)
					tempTask.put(param_isDeactivateCorporateUser,
							taskInfoMap.get(param_isDeactivateCorporateUser));
				tempTask.put(param_fromBulkTask, "true");
				String deactResponse = processDeactivation(tempTask);
				int status = 1; // SUCCESS
				if (deactResponse.equals(SUCCESS)) {
					success.append(rbtBulkUploadSubscriber.getSubscriberId())
							.append(",");
					success.append("  Deactivated ");
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					status = 2; // FAILURE
					failure.append(rbtBulkUploadSubscriber.getSubscriberId())
							.append(",");
					failure.append("  Deactivation failed. Reason: ");
					failure.append("(").append(deactResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}

				rbtBulkUploadSubscriber.setStatus(status);
				rbtBulkUploadSubscriber.setReason(deactResponse);

				RBTBulkUploadSubscriberDAO
						.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String resultFileName = rbtBulkUploadTask.getTaskId()
						+ "_result.txt";
				File resultFile = new File((String) null, resultFileName); // check
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write("Bulk Deactivation Statistics");
				bufferedWriter.newLine();
				bufferedWriter.write("-----------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write("Total: " + (successCount + failureCount));
				bufferedWriter.newLine();
				bufferedWriter.write("Success: " + successCount);
				bufferedWriter.newLine();
				bufferedWriter.write("Failure: " + failureCount);
				bufferedWriter.newLine();
				bufferedWriter.write("-----------------------------");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Success Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(success.toString());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Failure Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(failure.toString());
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		return response;

	}

	/*
	 * Processes the Selection tasks
	 */
	public String processBulkSelectionTask(WebServiceContext task,
			RBTBulkUploadTask rbtBulkUploadTask) {
		String response = ERROR;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		boolean isShuffleSelection = false;

		try {
			String taskID = task.getString(param_taskID);
			WebServiceContext tempTask = new WebServiceContext();

			List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
					.getRBTBulkUploadSubscribers(Integer.valueOf(taskID));

			if (bulkSubscriberList == null || bulkSubscriberList.size() == 0) {
				response = SUCCESS;
			} else {
				HashMap<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap != null
						&& taskInfoMap.containsKey(param_isShuffleSelection))
					isShuffleSelection = taskInfoMap.get(
							param_isShuffleSelection).equalsIgnoreCase(YES);

				for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
					String clipID = null;
					String shuffleID = null;

					tempTask.clear();
					tempTask.putAll(task);

					tempTask.put(param_subscriberID,
							rbtBulkUploadSubscriber.getSubscriberId());
					tempTask.put(param_mode, rbtBulkUploadTask.getActivatedBy());
					tempTask.put(param_modeInfo, rbtBulkUploadTask.getActInfo());
					if (rbtBulkUploadTask.getSelectionClass() != null)
						tempTask.put(param_chargeClass,
								rbtBulkUploadTask.getSelectionClass());

					if (rbtBulkUploadTask.getActivationClass() != null)
						tempTask.put(param_subscriptionClass,
								rbtBulkUploadTask.getActivationClass());

					tempTask.put(param_status, String.valueOf(rbtBulkUploadTask
							.getSelectionType()));
					tempTask.put(param_isPrepaid, String
							.valueOf(rbtBulkUploadSubscriber
									.getSubscriberType()));

					// Copies all the params in taskInfo into Task
					tempTask.putAll(taskInfoMap);

					if (taskInfoMap != null) {
						Set<String> keySet = taskInfoMap.keySet();
						for (String key : keySet) {
							if (key.startsWith(param_selectionInfo + "_")
									|| key.startsWith(param_userInfo + "_"))
								tempTask.put(key, taskInfoMap.get(key));
						}
					}

					if (rbtBulkUploadTask.getTaskType().equalsIgnoreCase(
							BULKACTION_CORPORATE)) {
						// Set rbtType to 2 if taskType is CORPORATE
						tempTask.put(param_selectionType, String.valueOf(2));
						// Set camapign id in extraInfo of selection
						tempTask.put(param_selectionInfo + "_" + CAMPAIGN_ID,
								taskID);

					}
					clipID = rbtBulkUploadSubscriber.getContentId();
					if (clipID == null) {
						clipID = taskInfoMap.get(param_clipID);
					}
					if (clipID != null) {
						boolean isPromoID = false;
						if (taskInfoMap != null
								&& taskInfoMap.containsKey(param_isPromoID))
							isPromoID = taskInfoMap.get(param_isPromoID)
									.equalsIgnoreCase(YES);

						boolean isSmsAlias = false;
						if (taskInfoMap != null
								&& taskInfoMap.containsKey(param_isSmsAlias))
							isSmsAlias = taskInfoMap.get(param_isSmsAlias)
									.equalsIgnoreCase(YES);

						if (isShuffleSelection) {
							shuffleID = clipID;

							if (isPromoID) {
								tempTask.put(param_categoryPromoID, shuffleID);
								tempTask.remove(param_categoryID);
							} else if (isSmsAlias) {
								tempTask.put(param_categorySmsAlias, shuffleID);
								tempTask.remove(param_categoryID);
							} else
								tempTask.put(param_categoryID, shuffleID);
						} else {
							if (isPromoID) {
								tempTask.put(param_clipPromoID, clipID);
								tempTask.remove(param_clipID);
							} else if (isSmsAlias) {
								tempTask.put(param_clipSmsAlias, clipID);
								tempTask.remove(param_clipID);
							} else
								tempTask.put(param_clipID, clipID);
						}
					}

					removeLeadingString(tempTask);

					if (tempTask.containsKey(param_clipID)) {
						Clip clip = getClip(tempTask);
						if (clip != null)
							tempTask.put(session_clip, clip);
					}

					tempTask.put(param_fromBulkTask, "true");
					String selResponse = processSelection(tempTask);
					int status = 1;
					if (selResponse.contains(SUCCESS)) {
						success.append(
								rbtBulkUploadSubscriber.getSubscriberId())
								.append(",");
						success.append(
								rbtBulkUploadSubscriber.getSubscriberType())
								.append(",");
						success.append(rbtBulkUploadSubscriber.getContentId());

						if (tempTask.containsKey(param_activatedNow))
							success.append("  Activated ").append(
									" and added selection");
						else
							success.append("  Added selection ");

						if (shuffleID != null)
							success.append(" with shuffle ").append(shuffleID);
						else if (clipID != null)
							success.append(" with clip ").append(clipID);
						else if (task.containsKey(param_cricketPack))
							success.append(" with cricket pack ").append(
									task.getString(param_cricketPack));

						success.append(System.getProperty("line.separator"));
						successCount++;
					} else {
						status = 2;
						failure.append(
								rbtBulkUploadSubscriber.getSubscriberId())
								.append(",");
						failure.append(
								rbtBulkUploadSubscriber.getSubscriberType())
								.append(",");
						failure.append(rbtBulkUploadSubscriber.getContentId());
						failure.append("  Selection failed. Reason :");
						failure.append("(").append(selResponse).append(")");
						failure.append(System.getProperty("line.separator"));
						failureCount++;

						Utility.sendErrorSMSForBulkRequestFailure(tempTask,
								rbtBulkUploadSubscriber.getSubscriberId(),
								selResponse, this);
					}

					rbtBulkUploadSubscriber.setStatus(status);
					rbtBulkUploadSubscriber.setReason(selResponse);

					RBTBulkUploadSubscriberDAO
							.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String resultFileName = rbtBulkUploadTask.getTaskId()
						+ "_result.txt";
				File resultFile = new File((String) null, resultFileName); // check
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write("Bulk Selection Statistics");
				bufferedWriter.newLine();
				bufferedWriter.write("--------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write("Total: " + (successCount + failureCount));
				bufferedWriter.newLine();
				bufferedWriter.write("Success: " + successCount);
				bufferedWriter.newLine();
				bufferedWriter.write("Failure: " + failureCount);
				bufferedWriter.newLine();
				bufferedWriter.write("--------------------------");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Success Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(success.toString());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Failure Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(failure.toString());
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}
		return response;
	}

	/**
	 * Gets the subscribers list of given taskID, deletes the setting for
	 * subscribers, returns the response File
	 */
	public String processBulkDeleteSelectionTask(WebServiceContext task,
			RBTBulkUploadTask rbtBulkUploadTask) {
		String response = ERROR;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String taskID = task.getString(param_taskID);
			List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
					.getRBTBulkUploadSubscribers(Integer.valueOf(taskID));
			WebServiceContext tempTask = new WebServiceContext();

			boolean isCorporateSelDeactRequest = false;
			HashMap<String, String> taskInfoMap = DBUtility
					.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
			if (taskInfoMap != null
					&& taskInfoMap.containsKey(param_isCorporateDeactivation)) {
				isCorporateSelDeactRequest = taskInfoMap.get(
						param_isCorporateDeactivation).equalsIgnoreCase(YES);
			}

			for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID,
						rbtBulkUploadSubscriber.getSubscriberId());
				if (!isCorporateSelDeactRequest) {
					tempTask.put(param_status, String.valueOf(rbtBulkUploadTask
							.getSelectionType()));
				} else {
					tempTask.put(param_selectionType, String
							.valueOf(rbtBulkUploadTask.getSelectionType()));
				}
				
				//RBT-13126 Added for supporting no option to delete corporate clips in bulk which were already added in loop
				if(rbtBulkUploadSubscriber.getContentId() != null && !rbtBulkUploadSubscriber.getContentId().equalsIgnoreCase("NULL") && !(rbtBulkUploadSubscriber.getContentId().trim().length() == 0)) {
					tempTask.put(param_clipID, rbtBulkUploadSubscriber.getContentId());
				}

				// Copies all params in taskInfo into tempTask
				tempTask.putAll(taskInfoMap);

				tempTask.put(param_fromBulkTask, "true");
				String delSettingResponse = deleteSetting(tempTask);
				int status = 1; // SUCCESS
				if (delSettingResponse.equals(SUCCESS)) {
					success.append("Deleted selection for ").append(
							rbtBulkUploadSubscriber.getSubscriberId());
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					status = 2; // FAILURE
					failure.append("Failed to delete selection for ");
					failure.append(rbtBulkUploadSubscriber.getSubscriberId())
							.append("(").append(delSettingResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}

				rbtBulkUploadSubscriber.setStatus(status);
				rbtBulkUploadSubscriber.setReason(delSettingResponse);

				RBTBulkUploadSubscriberDAO
						.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String resultFileName = rbtBulkUploadTask.getTaskId()
						+ "_result.txt";
				File resultFile = new File((String) null, resultFileName);
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write("Bulk Delete Selection Statistics");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write("Total: " + (successCount + failureCount));
				bufferedWriter.newLine();
				bufferedWriter.write("Success: " + successCount);
				bufferedWriter.newLine();
				bufferedWriter.write("Failure: " + failureCount);
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------------");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Success Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(success.toString());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.write("Failure Result");
				bufferedWriter.newLine();
				bufferedWriter.write("---------------");
				bufferedWriter.newLine();
				bufferedWriter.write(failure.toString());
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#editBulkTask(
	 * com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String editBulkTask(WebServiceContext task) {
		String response = ERROR;
		try {
			int taskID = Integer.parseInt(task.getString(param_taskID));
			RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO
					.getRBTBulkUploadTask(taskID);

			if (rbtBulkUploadTask == null) {
				logger.info("Task doesn't exists");
				response = TASK_DOES_NOT_EXIST;
				return response;
			}

			if (!rbtBulkUploadTask.getTaskType().equals(BULKACTION_CORPORATE)) {
				// Edit option is only for CORPORATE Tasks
				logger.info("Edit only allowed for Corporate tasks");
				response = TASK_EDIT_NOT_ALLOWED;
				return response;
			}

			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_EDITED
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_CAMPAIGN_ENDED
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_FAILURE
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_PROCESSING) {
				logger.info("Edit is not allowed. Already deactivated or edited");
				response = TASK_EDIT_NOT_ALLOWED;
			}

			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_SUCCESS) // Live
																				// compaign
			{
				HashMap<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap == null)
					taskInfoMap = new HashMap<String, String>();

				int editValue = 0;

				if (task.containsKey(param_action)
						&& task.getString(param_action).equals(
								action_deleteTask)) {
					editValue |= CAMPAIGN_DELETE;
				}
				if (task.containsKey(param_selectionEndTime)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					String endTime = task.getString(param_selectionEndTime);
					if (endTime.endsWith("000000000")) {
						endTime = endTime.substring(0, 8);
						endTime = endTime + "235959000";
					}
					Date endDate = dateFormat.parse(endTime);
					if (endDate.getTime() != rbtBulkUploadTask.getEndTime()
							.getTime()) {
						rbtBulkUploadTask.setEndTime(endDate);
					}
				}
				if (task.containsKey(param_fromTime)) {
					if ((!taskInfoMap.containsKey(param_fromTime) && !task
							.getString(param_fromTime).equals("0"))
							|| !task.getString(param_fromTime).equals(
									taskInfoMap.get(param_fromTime))) {
						taskInfoMap.put(param_fromTime,
								task.getString(param_fromTime));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_fromTimeMinutes)) {
					if ((!taskInfoMap.containsKey(param_fromTimeMinutes) && !task
							.getString(param_fromTimeMinutes).equals("0"))
							|| !task.getString(param_fromTimeMinutes).equals(
									taskInfoMap.get(param_fromTimeMinutes))) {
						taskInfoMap.put(param_fromTimeMinutes,
								task.getString(param_fromTimeMinutes));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_toTime)) {
					if ((!taskInfoMap.containsKey(param_toTime) && !task
							.getString(param_toTime).equals("23"))
							|| !task.getString(param_toTime).equals(
									taskInfoMap.get(param_toTime))) {
						taskInfoMap.put(param_toTime,
								task.getString(param_toTime));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_toTimeMinutes)) {
					if ((!taskInfoMap.containsKey(param_toTimeMinutes) && !task
							.getString(param_toTimeMinutes).equals("59"))
							|| !task.getString(param_toTimeMinutes).equals(
									taskInfoMap.get(param_toTimeMinutes))) {
						taskInfoMap.put(param_toTimeMinutes,
								task.getString(param_toTimeMinutes));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_interval)) {
					if (!taskInfoMap.containsKey(param_interval)
							|| !task.getString(param_interval).equals(
									taskInfoMap.get(param_interval))) {
						taskInfoMap.put(param_interval,
								task.getString(param_interval));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_clipID)) {
					if (!task.getString(param_clipID).equals(
							taskInfoMap.get(param_clipID))) {
						taskInfoMap.put(param_clipID,
								task.getString(param_clipID));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_editNumbersInTask)) {
					String addOrDeleteNumbers = task
							.getString(param_editNumbersInTask);
					boolean addNumbers = addOrDeleteNumbers
							.equalsIgnoreCase("add");

					String filePath = task.getString(param_bulkTaskFile);
					File file = null;
					BufferedReader bufferedReader = null;
					try {
						String circleID = rbtBulkUploadTask.getCircleId();
						file = new File(filePath);
						bufferedReader = new BufferedReader(
								new FileReader(file));
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							line = line.trim();
							if (line.length() == 0)
								continue;

							String subscriberID = null;
							String subCircleID = null;
							String subscriberType = null;

							String[] tokens = line.split(",");
							if (tokens.length >= 1)
								subscriberID = tokens[0].trim();
							if (tokens.length >= 2)
								subscriberType = tokens[1].trim();

							SubscriberDetail subscriberDetail = RbtServicesMgr
									.getSubscriberDetail(new MNPContext(
											subscriberID, "BULK"));
							if (subscriberDetail != null)
								subCircleID = subscriberDetail.getCircleID();

							String isPrepaid = NO;
							if (subscriberType != null)
								isPrepaid = subscriberType
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							else if (task.containsKey(param_isPrepaid))
								isPrepaid = task.getString(param_isPrepaid);
							else {
								Parameters userTypeParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"DEFAULT_USER_TYPE", "POSTPAID");
								isPrepaid = userTypeParam.getValue().trim()
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							}
							try {
								if (!circleID.equalsIgnoreCase(subCircleID)) {
									logger.info("Subscriber :" + subscriberID
											+ "doesn't belong to circleID :"
											+ circleID);
									continue;
								}
								RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
								rbtBulkUploadSubscriber.setTaskId(taskID);
								rbtBulkUploadSubscriber
										.setSubscriberId(subscriberID);
								rbtBulkUploadSubscriber
										.setCircleId(subCircleID);
								rbtBulkUploadSubscriber
										.setSubscriberType(isPrepaid.charAt(0));

								if (addNumbers) {
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_NEW);
									RBTBulkUploadSubscriberDAO
											.createBulkSubscriber(rbtBulkUploadSubscriber);
								} else {
									// rbtBulkUploadSubscriber.setReason("Deleted From Campaign");
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_SUBSCRIBER_DELETE);
									RBTBulkUploadSubscriberDAO
											.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
								}
							} catch (Exception e) {
								logger.info("Exception in processing bulkSubcriber .. Continue");
							}
						}
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						if (bufferedReader != null)
							bufferedReader.close();
						if (file != null)
							file.delete();
					}
					if (addNumbers)
						editValue |= CAMPAIGN_ADD_SUBSCRIBERS;
					else
						editValue |= CAMPAIGN_DELETE_SUBSCRIBERS;
				}

				if (editValue != 0) {
					taskInfoMap.put(EDIT_TASK, String.valueOf(editValue));
					String taskInfo = DBUtility
							.getAttributeXMLFromMap(taskInfoMap);
					rbtBulkUploadTask.setTaskInfo(taskInfo);
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_EDITED); // Setting
																				// Task
																				// Status
																				// to
																				// Edited
				}
				boolean updated = RBTBulkUploadTaskDAO
						.updateRBTBulkUploadTask(rbtBulkUploadTask);
				if (updated)
					response = SUCCESS;
				else
					response = FAILED;

			} else if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_NEW) // New
																					// compaign
			{
				if (task.containsKey(param_action)
						&& task.getString(param_action).equals(
								action_deleteTask)) {
					rbtBulkUploadTask
							.setTaskStatus(BULKTASK_STATUS_CAMPAIGN_ENDED);
					RBTBulkUploadTaskDAO
							.updateRBTBulkUploadTask(rbtBulkUploadTask);
					response = SUCCESS;
					return response;

				}
				// Check what happens if it goes live at the same time
				HashMap<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap == null)
					taskInfoMap = new HashMap<String, String>();

				if (task.containsKey(param_fromTime))
					taskInfoMap.put(param_fromTime,
							task.getString(param_fromTime));
				if (task.containsKey(param_fromTimeMinutes))
					taskInfoMap.put(param_fromTimeMinutes,
							task.getString(param_fromTimeMinutes));
				if (task.containsKey(param_toTime))
					taskInfoMap.put(param_toTime, task.getString(param_toTime));
				if (task.containsKey(param_toTimeMinutes))
					taskInfoMap.put(param_toTimeMinutes,
							task.getString(param_toTimeMinutes));
				if (task.containsKey(param_interval))
					taskInfoMap.put(param_interval,
							task.getString(param_interval));
				if (task.containsKey(param_clipID))
					taskInfoMap.put(param_clipID, task.getString(param_clipID));

				if (task.containsKey(param_subscriptionClass))
					taskInfoMap.put(param_subscriptionClass,
							task.getString(param_subscriptionClass));
				if (task.containsKey(param_chargeClass))
					taskInfoMap.put(param_chargeClass,
							task.getString(param_chargeClass));
				if (task.containsKey(param_mode))
					taskInfoMap.put(param_mode, task.getString(param_mode));
				if (task.containsKey(param_modeInfo))
					taskInfoMap.put(param_modeInfo,
							task.getString(param_modeInfo));

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				if (task.containsKey(param_selectionStartTime))
					rbtBulkUploadTask.setProcessTime(dateFormat.parse(task
							.getString(param_selectionStartTime)));
				if (task.containsKey(param_selectionEndTime)) {
					String endTime = task.getString(param_selectionEndTime);
					if (endTime.endsWith("000000000")) {
						endTime = endTime.substring(0, 8);
						endTime = endTime + "235959000";
					}
					rbtBulkUploadTask.setEndTime(dateFormat.parse(endTime));
				}
				if (task.containsKey(param_editNumbersInTask)) {
					String circleID = rbtBulkUploadTask.getCircleId();
					String addOrDeleteNumbers = task
							.getString(param_editNumbersInTask);
					boolean addNumbers = addOrDeleteNumbers
							.equalsIgnoreCase("add");
					String filePath = task.getString(param_bulkTaskFile);
					File file = null;
					BufferedReader bufferedReader = null;
					try {
						file = new File(filePath);
						bufferedReader = new BufferedReader(
								new FileReader(file));
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							line = line.trim();
							if (line.length() == 0)
								continue;

							String subscriberID = null;
							String subCircleID = null;
							String subscriberType = null;

							String[] tokens = line.split(",");
							if (tokens.length >= 1)
								subscriberID = tokens[0].trim();
							if (tokens.length >= 2)
								subscriberType = tokens[1].trim();

							SubscriberDetail subscriberDetail = RbtServicesMgr
									.getSubscriberDetail(new MNPContext(
											subscriberID, "BULK"));
							if (subscriberDetail != null)
								subCircleID = subscriberDetail.getCircleID();

							String isPrepaid = NO;
							if (subscriberType != null)
								isPrepaid = subscriberType
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							else if (task.containsKey(param_isPrepaid))
								isPrepaid = task.getString(param_isPrepaid);
							else {
								Parameters userTypeParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"DEFAULT_USER_TYPE", "POSTPAID");
								isPrepaid = userTypeParam.getValue().trim()
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							}

							try {
								if (!circleID.equalsIgnoreCase(subCircleID)) {
									logger.info("Subscriber :" + subscriberID
											+ "doesn't belong to circleID :"
											+ circleID);
									continue;
								}

								RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
								rbtBulkUploadSubscriber.setTaskId(taskID);
								rbtBulkUploadSubscriber
										.setSubscriberId(subscriberID);
								rbtBulkUploadSubscriber
										.setCircleId(subCircleID);
								rbtBulkUploadSubscriber
										.setSubscriberType(isPrepaid.charAt(0));

								if (addNumbers) {
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_NEW);
									RBTBulkUploadSubscriberDAO
											.createBulkSubscriber(rbtBulkUploadSubscriber);
								} else {
									rbtBulkUploadSubscriber
											.setReason("Deleted From Campaign");
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_FAILURE);
									RBTBulkUploadSubscriberDAO
											.deleteRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
								}

							} catch (Exception e) {
								logger.info("Exception in processing bulkSubcriber .. Continue");
							}
						}
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						if (bufferedReader != null)
							bufferedReader.close();
						if (file != null)
							file.delete();
					}
				}
				String taskInfo = DBUtility.getAttributeXMLFromMap(taskInfoMap);
				rbtBulkUploadTask.setTaskInfo(taskInfo);

				boolean updated = RBTBulkUploadTaskDAO
						.updateRBTBulkUploadTask(rbtBulkUploadTask);
				if (updated)
					response = SUCCESS;
				else
					response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
		}
		return response;
	}

	/**
	 * This method is added to return a file after adding/deleting numbers from
	 * a CORPORATE campaign. This method is similar to
	 * editBulkTask(WebServiceContext task) API except for returning the file in
	 * this method.
	 * 
	 * @param task
	 * @return
	 */
	@Override
	public String editBulkTaskForCorporate(WebServiceContext task) {
		String response = ERROR;
		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder result = new StringBuilder();
		int successCount = 0;
		int failureCount = 0;

		String addOrDeleteNumbers = task.getString(param_editNumbersInTask);
		boolean addNumbers = "add".equalsIgnoreCase(addOrDeleteNumbers);
		try {
			int taskID = Integer.parseInt(task.getString(param_taskID));
			RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO
					.getRBTBulkUploadTask(taskID);

			if (rbtBulkUploadTask == null) {
				logger.info("Task doesn't exists");
				response = TASK_DOES_NOT_EXIST;
				return response;
			}

			if (!rbtBulkUploadTask.getTaskType().equals(BULKACTION_CORPORATE)) {
				// Edit option is only for CORPORATE Tasks
				logger.info("Edit only allowed for Corporate tasks");
				response = TASK_EDIT_NOT_ALLOWED;
				return response;
			}

			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_EDITED
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_CAMPAIGN_ENDED
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_FAILURE
					|| rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_PROCESSING) {
				logger.info("Edit is not allowed. Already deactivated or edited");
				response = TASK_EDIT_NOT_ALLOWED;
			}

			boolean isConsiderAllCircle = getParamAsBoolean("CCC",
					"CONSIDER_ALL_CIRCLE_IN_SAME_HUB_FOR_BULK_TASK", "false");
			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_SUCCESS) // Live
																				// compaign
			{
				HashMap<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap == null)
					taskInfoMap = new HashMap<String, String>();

				int editValue = 0;

				if (task.containsKey(param_action)
						&& task.getString(param_action).equals(
								action_deleteTask)) {
					editValue |= CAMPAIGN_DELETE;
				}
				if (task.containsKey(param_selectionEndTime)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					String endTime = task.getString(param_selectionEndTime);
					if (endTime.endsWith("000000000")) {
						endTime = endTime.substring(0, 8);
						endTime = endTime + "235959000";
					}
					Date endDate = dateFormat.parse(endTime);
					if (endDate.getTime() != rbtBulkUploadTask.getEndTime()
							.getTime()) {
						rbtBulkUploadTask.setEndTime(endDate);
					}
				}
				if (task.containsKey(param_fromTime)) {
					if ((!taskInfoMap.containsKey(param_fromTime) && !task
							.getString(param_fromTime).equals("0"))
							|| !task.getString(param_fromTime).equals(
									taskInfoMap.get(param_fromTime))) {
						taskInfoMap.put(param_fromTime,
								task.getString(param_fromTime));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_fromTimeMinutes)) {
					if ((!taskInfoMap.containsKey(param_fromTimeMinutes) && !task
							.getString(param_fromTimeMinutes).equals("0"))
							|| !task.getString(param_fromTimeMinutes).equals(
									taskInfoMap.get(param_fromTimeMinutes))) {
						taskInfoMap.put(param_fromTimeMinutes,
								task.getString(param_fromTimeMinutes));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_toTime)) {
					if ((!taskInfoMap.containsKey(param_toTime) && !task
							.getString(param_toTime).equals("23"))
							|| !task.getString(param_toTime).equals(
									taskInfoMap.get(param_toTime))) {
						taskInfoMap.put(param_toTime,
								task.getString(param_toTime));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_toTimeMinutes)) {
					if ((!taskInfoMap.containsKey(param_toTimeMinutes) && !task
							.getString(param_toTimeMinutes).equals("59"))
							|| !task.getString(param_toTimeMinutes).equals(
									taskInfoMap.get(param_toTimeMinutes))) {
						taskInfoMap.put(param_toTimeMinutes,
								task.getString(param_toTimeMinutes));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_interval)) {
					if (!taskInfoMap.containsKey(param_interval)
							|| !task.getString(param_interval).equals(
									taskInfoMap.get(param_interval))) {
						taskInfoMap.put(param_interval,
								task.getString(param_interval));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_clipID)) {
					if (!task.getString(param_clipID).equals(
							taskInfoMap.get(param_clipID))) {
						taskInfoMap.put(param_clipID,
								task.getString(param_clipID));
						editValue |= CAMPAIGN_UPDATED;
					}
				}
				if (task.containsKey(param_editNumbersInTask)) {

					String filePath = task.getString(param_bulkTaskFile);
					try {
						String circleID = rbtBulkUploadTask.getCircleId();
						file = new File(filePath);
						bufferedReader = new BufferedReader(
								new FileReader(file));
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							line = line.trim();
							if (line.length() == 0)
								continue;

							String subscriberID = null;
							String subCircleID = null;
							String subscriberType = null;
							String toneID = null;

							String[] tokens = line.split(",");
							if (tokens.length >= 1)
								subscriberID = tokens[0].trim();
							if (tokens.length >= 2)
								toneID = tokens[1].trim();
							if (tokens.length >= 3)
								subscriberType = tokens[2].trim();

							SubscriberDetail subscriberDetail = RbtServicesMgr
									.getSubscriberDetail(new MNPContext(
											subscriberID, "BULK"));
							if (subscriberDetail != null)
								subCircleID = subscriberDetail.getCircleID();

							String isPrepaid = NO;
							if (subscriberType != null)
								isPrepaid = subscriberType
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							else if (task.containsKey(param_isPrepaid))
								isPrepaid = task.getString(param_isPrepaid);
							else {
								Parameters userTypeParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"DEFAULT_USER_TYPE", "POSTPAID");
								isPrepaid = userTypeParam.getValue().trim()
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							}
							try {
								if (!isConsiderAllCircle
										&& !circleID
												.equalsIgnoreCase(subCircleID)) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO GIVEN CIRCLE ID")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								if (isConsiderAllCircle
										&& (subscriberDetail == null || !subscriberDetail
												.isValidSubscriber())) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO CURRENT SITE")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
								rbtBulkUploadSubscriber.setTaskId(taskID);
								rbtBulkUploadSubscriber
										.setSubscriberId(subscriberID);
								rbtBulkUploadSubscriber
										.setCircleId(subCircleID);
								rbtBulkUploadSubscriber
										.setSubscriberType(isPrepaid.charAt(0));
								rbtBulkUploadSubscriber.setContentId(toneID);

								if (addNumbers) {
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_NEW);
									RBTBulkUploadSubscriberDAO
											.createBulkSubscriber(rbtBulkUploadSubscriber);
								} else {
									// rbtBulkUploadSubscriber.setReason("Deleted From Campaign");
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_SUBSCRIBER_DELETE);
									RBTBulkUploadSubscriberDAO
											.updateRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
								}

								successCount++;
								result.append(line)
										.append(",SUCCESS")
										.append(System
												.getProperty("line.separator"));

							} catch (Exception e) {
								logger.info("Exception in processing bulkSubcriber .. Continue");
							}
						}
					} catch (Exception e) {
						logger.error("", e);
					} finally {

					}
					if (addNumbers)
						editValue |= CAMPAIGN_ADD_SUBSCRIBERS;
					else
						editValue |= CAMPAIGN_DELETE_SUBSCRIBERS;
				}

				if (editValue != 0) {
					taskInfoMap.put(EDIT_TASK, String.valueOf(editValue));
					String taskInfo = DBUtility
							.getAttributeXMLFromMap(taskInfoMap);
					rbtBulkUploadTask.setTaskInfo(taskInfo);
					rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_EDITED); // Setting
																				// Task
																				// Status
																				// to
																				// Edited
				}
				boolean updated = RBTBulkUploadTaskDAO
						.updateRBTBulkUploadTask(rbtBulkUploadTask);
				if (updated)
					response = SUCCESS;
				else
					response = FAILED;

			} else if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_NEW) // New
																					// compaign
			{
				if (task.containsKey(param_action)
						&& task.getString(param_action).equals(
								action_deleteTask)) {
					rbtBulkUploadTask
							.setTaskStatus(BULKTASK_STATUS_CAMPAIGN_ENDED);
					RBTBulkUploadTaskDAO
							.updateRBTBulkUploadTask(rbtBulkUploadTask);
					response = SUCCESS;
					return response;

				}
				// Check what happens if it goes live at the same time
				HashMap<String, String> taskInfoMap = DBUtility
						.getAttributeMapFromXML(rbtBulkUploadTask.getTaskInfo());
				if (taskInfoMap == null)
					taskInfoMap = new HashMap<String, String>();

				if (task.containsKey(param_fromTime))
					taskInfoMap.put(param_fromTime,
							task.getString(param_fromTime));
				if (task.containsKey(param_fromTimeMinutes))
					taskInfoMap.put(param_fromTimeMinutes,
							task.getString(param_fromTimeMinutes));
				if (task.containsKey(param_toTime))
					taskInfoMap.put(param_toTime, task.getString(param_toTime));
				if (task.containsKey(param_toTimeMinutes))
					taskInfoMap.put(param_toTimeMinutes,
							task.getString(param_toTimeMinutes));
				if (task.containsKey(param_interval))
					taskInfoMap.put(param_interval,
							task.getString(param_interval));
				if (task.containsKey(param_clipID))
					taskInfoMap.put(param_clipID, task.getString(param_clipID));

				if (task.containsKey(param_subscriptionClass))
					taskInfoMap.put(param_subscriptionClass,
							task.getString(param_subscriptionClass));
				if (task.containsKey(param_chargeClass))
					taskInfoMap.put(param_chargeClass,
							task.getString(param_chargeClass));
				if (task.containsKey(param_mode))
					taskInfoMap.put(param_mode, task.getString(param_mode));
				if (task.containsKey(param_modeInfo))
					taskInfoMap.put(param_modeInfo,
							task.getString(param_modeInfo));

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				if (task.containsKey(param_selectionStartTime))
					rbtBulkUploadTask.setProcessTime(dateFormat.parse(task
							.getString(param_selectionStartTime)));
				if (task.containsKey(param_selectionEndTime)) {
					String endTime = task.getString(param_selectionEndTime);
					if (endTime.endsWith("000000000")) {
						endTime = endTime.substring(0, 8);
						endTime = endTime + "235959000";
					}
					rbtBulkUploadTask.setEndTime(dateFormat.parse(endTime));
				}
				if (task.containsKey(param_editNumbersInTask)) {
					String circleID = rbtBulkUploadTask.getCircleId();
					String filePath = task.getString(param_bulkTaskFile);
					try {
						file = new File(filePath);
						bufferedReader = new BufferedReader(
								new FileReader(file));
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							line = line.trim();
							if (line.length() == 0)
								continue;

							String subscriberID = null;
							String subCircleID = null;
							String subscriberType = null;
							String toneID = null;

							String[] tokens = line.split(",");
							if (tokens.length >= 1)
								subscriberID = tokens[0].trim();
							if (tokens.length >= 2)
								toneID = tokens[1].trim();
							if (tokens.length >= 3)
								subscriberType = tokens[2].trim();

							SubscriberDetail subscriberDetail = RbtServicesMgr
									.getSubscriberDetail(new MNPContext(
											subscriberID, "BULK"));
							if (subscriberDetail != null)
								subCircleID = subscriberDetail.getCircleID();

							String isPrepaid = NO;
							if (subscriberType != null)
								isPrepaid = subscriberType
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							else if (task.containsKey(param_isPrepaid))
								isPrepaid = task.getString(param_isPrepaid);
							else {
								Parameters userTypeParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"DEFAULT_USER_TYPE", "POSTPAID");
								isPrepaid = userTypeParam.getValue().trim()
										.equalsIgnoreCase("PREPAID") ? YES : NO;
							}

							try {
								if (!isConsiderAllCircle
										&& !circleID
												.equalsIgnoreCase(subCircleID)) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO GIVEN CIRCLE ID")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								if (isConsiderAllCircle
										&& (subscriberDetail == null || !subscriberDetail
												.isValidSubscriber())) {
									failureCount++;
									result.append(line)
											.append(",SUBSCRIBER NOT BELONG TO CURRENT SITE")
											.append(System
													.getProperty("line.separator"));
									continue;
								}

								RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
								rbtBulkUploadSubscriber.setTaskId(taskID);
								rbtBulkUploadSubscriber
										.setSubscriberId(subscriberID);
								rbtBulkUploadSubscriber
										.setCircleId(subCircleID);
								rbtBulkUploadSubscriber
										.setSubscriberType(isPrepaid.charAt(0));
								rbtBulkUploadSubscriber.setContentId(toneID);

								if (addNumbers) {
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_NEW);
									RBTBulkUploadSubscriberDAO
											.createBulkSubscriber(rbtBulkUploadSubscriber);
								} else {
									rbtBulkUploadSubscriber
											.setReason("Deleted From Campaign");
									rbtBulkUploadSubscriber
											.setStatus(BULKTASK_STATUS_FAILURE);
									RBTBulkUploadSubscriberDAO
											.deleteRBTBulkUploadSubscriber(rbtBulkUploadSubscriber);
								}

								successCount++;
								result.append(line)
										.append(",SUCCESS")
										.append(System
												.getProperty("line.separator"));

							} catch (Exception e) {
								logger.info("Exception in processing bulkSubcriber .. Continue");
							}
						}
					} catch (Exception e) {
						logger.error("", e);
					} finally {

					}
				}
				String taskInfo = DBUtility.getAttributeXMLFromMap(taskInfoMap);
				rbtBulkUploadTask.setTaskInfo(taskInfo);

				boolean updated = RBTBulkUploadTaskDAO
						.updateRBTBulkUploadTask(rbtBulkUploadTask);
				if (updated)
					response = SUCCESS;
				else
					response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				if (bufferedReader != null)
					bufferedReader.close();

				File resultFile = null;

				if (!response.equals(SUCCESS)) {
					resultFile = new File((String) null, ERROR + "_result.txt");
				} else {
					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					resultFile = new File(file.getParentFile(), resultFileName);
				}
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());

				bufferedWriter = new BufferedWriter(new FileWriter(resultFile));

				if (addNumbers)
					bufferedWriter
							.write("Bulk Task Add subscribers Statistics");
				else
					bufferedWriter
							.write("Bulk Task Delete subscribers Statistics");

				bufferedWriter.newLine();
				bufferedWriter.write("Response :" + response);
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write("Total: " + (successCount + failureCount));
				bufferedWriter.newLine();
				bufferedWriter.write("Success: " + successCount);
				bufferedWriter.newLine();
				bufferedWriter.write("Failure: " + failureCount);
				bufferedWriter.newLine();
				bufferedWriter.write("---------------------------");
				bufferedWriter.newLine();
				bufferedWriter.write(result.toString());

				if (file != null)
					file.delete();
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * checkBulkSubscribersStatus
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String checkBulkSubscribersStatus(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder corporate = new StringBuilder();
		StringBuilder nonCorporate = new StringBuilder();

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				List<RBTBulkUploadSubscriber> rbtBulkUploadSubscribers = RBTBulkUploadSubscriberDAO
						.getBulkUploadSubscriber(subscriberID);
				boolean isSubscriberCorporateUser = false;
				String status = null;
				for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : rbtBulkUploadSubscribers) {
					if (rbtBulkUploadSubscriber.getStatus() == BULKTASK_STATUS_NEW
							|| rbtBulkUploadSubscriber.getStatus() == BULKTASK_STATUS_SUCCESS) {
						isSubscriberCorporateUser = true;
						status = (rbtBulkUploadSubscriber.getStatus() == BULKTASK_STATUS_NEW ? "CorporateUser Activation Pending"
								: "Corporate User");
					}
				}

				if (isSubscriberCorporateUser) {
					corporate.append(subscriberID + " - " + status);
					corporate.append(System.getProperty("line.separator"));
				} else {
					nonCorporate.append(subscriberID + " - "
							+ "Not a corporate User");
					nonCorporate.append(System.getProperty("line.separator"));
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Corporate Subscriber Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("-------------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Corporate Users");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(corporate.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Non Corporate Users");
					bufferedWriter.newLine();
					bufferedWriter.write("-------------------");
					bufferedWriter.newLine();
					bufferedWriter.write(nonCorporate.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#sendSMS(com.onmobile
	 * .apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String sendSMS(WebServiceContext task) {
		String response = ERROR;

		try {
			String senderID = task.getString(param_senderID);
			String receiverID = task.getString(param_receiverID);
			String smsText = task.getString(param_smsText);
			String mode = task.getString(param_mode);
			String userID = task.getString(param_userId);
			int modeVal = SRBTUtility.getSocialRBTMode(mode);

			if (senderID == null
					|| receiverID == null
					|| smsText == null
					|| !com.onmobile.apps.ringbacktones.services.common.Utility
							.isValidNumber(receiverID)) {
				response = INVALID_PARAMETER;
			} else {
				int index = Integer.parseInt(RBTParametersUtils
						.getParamAsString(iRBTConstant.SMS, "SMS_TEXT_LENGTH",
								"154"));
				List<String> smsTextList = new ArrayList<String>();
				String brokenSmsText = null;
				while (smsText.length() != 0) {
					// index = 154;
					if (smsText.length() <= index) {
						brokenSmsText = smsText;
						smsText = "";
					} else {
						while (index >= 0 && smsText.charAt(index) != ' ')
							index--;
						brokenSmsText = smsText.substring(0, index);
						smsText = smsText.substring(index + 1);
					}

					smsTextList.add(brokenSmsText);
				}

				Parameters addPageNoAtBeginingParam = parametersCacheManager
						.getParameter(iRBTConstant.COMMON,
								"ADD_PAGE_NO_AT_BEGINING", "FALSE");
				boolean addPageNoAtBegining = addPageNoAtBeginingParam
						.getValue().trim().equalsIgnoreCase("TRUE");

				boolean sendSMSResponse = false;
				for (int i = 0; i < smsTextList.size(); i++) {
					smsText = smsTextList.get(i);

					if (smsTextList.size() > 1) {
						if (addPageNoAtBegining)
							smsText = (i + 1) + "/" + smsTextList.size() + " "
									+ smsText;
						else
							smsText = smsText + " " + (i + 1) + "/"
									+ smsTextList.size();
					}

					sendSMSResponse = Tools.sendSMS(senderID, receiverID,
							smsText, false);
				}

				if (sendSMSResponse)
					response = SUCCESS;
				else
					response = FAILED;
			}
			if (response.equalsIgnoreCase(SUCCESS) && mode != null
					&& MODE.indexOf("social_") != -1) {
				SocialRBTEventLogger.accessEventLog(userID, receiverID,
						senderID, "", modeVal);
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processHLRRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String processHLRRequest(WebServiceContext webServiceContext) {
		String response = ERROR;

		try {
			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			if (!com.onmobile.apps.ringbacktones.services.common.Utility
					.isValidNumber(subscriberID)) {
				logger.info("Invalid subscriberID. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			response = Utility.getSubscriberStatus(subscriber);

			boolean isHLRTickRequest = webServiceContext
					.getString(param_action).equalsIgnoreCase(action_tickHLR);

			if ((isHLRTickRequest && !response.equals(ACTIVE) && !response
					.equals(RENEWAL_PENDING))
					|| (!isHLRTickRequest && !response.equals(NEW_USER) && !response
							.equals(DEACTIVE))) {
				logger.info("response: " + response);
				return response;
			}

			Parameters hlrTickURLParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "HLR_TICK_URL", null);
			String url = hlrTickURLParam.getValue().trim();
			url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
			url = url.replaceAll("%SERVICE_KEY%",
					"RBT_ACT_" + subscriber.subscriptionClass());
			String action = isHLRTickRequest ? "ACT" : "DCT";
			url = url.replaceAll("%ACTION%", action);
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("httpResponse: " + httpResponse);

			String[] status = httpResponse.getResponse().trim().split("\\|");
			response = status[0];
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processSuspension
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSuspension(WebServiceContext task) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			if (!com.onmobile.apps.ringbacktones.services.common.Utility
					.isValidNumber(subscriberID)) {
				logger.info("Invalid subscriberID. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			boolean suspend = task.getString(param_suspend).equalsIgnoreCase(
					YES);

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

			String subscriptionClass = null;
			boolean subscriptionClassFound = false;

			if (subscriber != null && subscriber.subscriptionClass() != null)
				subscriptionClass = subscriber.subscriptionClass();

			Parameters voluntarySusSubClassParam = parametersCacheManager
					.getParameter(iRBTConstant.COMMON,
							"VOLUNTARY_SUSPENSION_SUB_CLASSES");
			String[] voluntarySusSubClasses = null;

			if (voluntarySusSubClassParam != null
					&& voluntarySusSubClassParam.getValue() != null) {
				voluntarySusSubClasses = voluntarySusSubClassParam.getValue()
						.trim().split(",");
			}

			if (voluntarySusSubClasses != null
					&& voluntarySusSubClasses.length > 0) {
				for (String voluntarySusSubClass : voluntarySusSubClasses) {
					if (voluntarySusSubClass
							.equalsIgnoreCase(subscriptionClass)) {
						subscriptionClassFound = true;
						break;
					}
				}

				if (!subscriptionClassFound) {
					return SUSPENSION_NOT_ALLOWED;
				}
			}

			boolean isCorporateSelSuspensionAllowed = true;
			Parameters corpSelSusAllowed = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "VOL_SUS_NON_CORP", null);
			if (corpSelSusAllowed != null) {
				if (corpSelSusAllowed.getValue() != null
						&& corpSelSusAllowed.getValue()
								.equalsIgnoreCase("true"))
					isCorporateSelSuspensionAllowed = false;
			}

			if (isCorporateSelSuspensionAllowed) {
				HashMap<String, String> extraInfoMap = rbtDBManager
						.getExtraInfoMap(subscriber);
				boolean isAlreadySuspended = extraInfoMap != null
						&& extraInfoMap.containsKey(iRBTConstant.VOLUNTARY);

				if (suspend && isAlreadySuspended) {
					logger.info("response: " + ALREADY_VOLUNTARILY_SUSPENDED);
					return ALREADY_VOLUNTARILY_SUSPENDED;
				}

				if (!suspend && !isAlreadySuspended) {
					logger.info("response: " + NOT_VOLUNTARILY_SUSPENDED);
					return NOT_VOLUNTARILY_SUSPENDED;
				}
			}

			response = Utility.getSubscriberStatus(subscriber);
			if (response.equals(ACTIVE) || response.equals(SUSPENDED)) {
				String[] corpCatIDsArr = null;
				Parameters corpCatIdsParam = parametersCacheManager
						.getParameter(iRBTConstant.WEBSERVICE,
								"CORP_CAT_ID_LIST", "1");
				if (corpCatIdsParam != null
						&& corpCatIdsParam.getValue() != null) {
					logger.info("corpCatIdsParam !=null ");
					corpCatIDsArr = corpCatIdsParam.getValue().trim()
							.split(",");
				}
				ArrayList<String> corpCatIDs = null;
				if (corpCatIDsArr != null && corpCatIDsArr.length > 0) {
					corpCatIDs = new ArrayList<String>();
					for (int count = 0; count < corpCatIDsArr.length; count++) {
						corpCatIDs.add(corpCatIDsArr[count]);
					}
				}

				if (corpCatIDs == null) {
					corpCatIDs = new ArrayList<String>();
					corpCatIDs.add("1");
					for (int count = 0; count < corpCatIDs.size(); count++) {
						logger.info("corpCatId 123: " + corpCatIDs.get(count));
					}
				} else {
					for (int count = 0; count < corpCatIDs.size(); count++) {
						logger.info("corpCatId :" + corpCatIDs.get(count));
					}
				}

				SubscriberDownloads[] downloads = null;
				boolean isActPendingDownloads = false;
				boolean isNoCrporateDownloadPresent = true;
				boolean isOnlyCorporateDownloadsPresent = true;
				if (!isCorporateSelSuspensionAllowed) {
					downloads = rbtDBManager
							.getActivateNActPendingDownloads(subscriber.subID());
					if (downloads != null && downloads.length > 0) {
						for (int count = 0; count < downloads.length; count++) {
							SubscriberDownloads tempDownloads = downloads[count];
							if (tempDownloads != null
									&& (tempDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_TO_BE_ACTIVATED
											|| tempDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACTIVATION_PENDING || tempDownloads
											.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_BASE_ACT_PENDING)) {
								isActPendingDownloads = true;
							}
							String tempId = "" + tempDownloads.categoryID();
							if (corpCatIDs.contains((tempId))) {
								isNoCrporateDownloadPresent = false;
								logger.info("isNoCrporateDownloadPresent: "
										+ isNoCrporateDownloadPresent);
							} else {
								isOnlyCorporateDownloadsPresent = false;
								logger.info("isOnlyCorporateDownloadsPresent: "
										+ isOnlyCorporateDownloadsPresent);
							}
						}

						if (isOnlyCorporateDownloadsPresent && suspend) {
							logger.info("response: "
									+ CORPORATE_SUSPENSION_NOT_ALLOWED);
							return CORPORATE_SUSPENSION_NOT_ALLOWED;
						}
					}
					if (!suspend) {
						downloads = rbtDBManager
								.getAllVoluntarySuspendedDownloads(subscriber
										.subID());
					}

					boolean isCorporateUser = false;
					SubscriberStatus[] selections = rbtDBManager
							.getAllActiveSubscriberSettings(subscriberID);
					if (selections != null) {
					 for (SubscriberStatus subscriberStatus : selections) {
						if (subscriberStatus.selType() == 2) {
							isCorporateUser = true;
							break;
						}
					 }
					}
					boolean isAlreadySuspended = false;
					if (isCorporateUser) {
						SubscriberDownloads[] subscriberDownloads = rbtDBManager
								.getActiveSubscriberDownloads(subscriberID);
						if (downloads != null) {
							for (SubscriberDownloads download : subscriberDownloads) {
								HashMap<String, String> extraInfoMap = DBUtility
										.getAttributeMapFromXML(download
												.extraInfo());
								if (extraInfoMap != null
										&& extraInfoMap
												.containsKey(iRBTConstant.VOLUNTARY)) {
									isAlreadySuspended = true;
									break;
								}
							}
						}
					} else {
						HashMap<String, String> extraInfoMap = rbtDBManager
								.getExtraInfoMap(subscriber);
						isAlreadySuspended = extraInfoMap != null
								&& extraInfoMap
										.containsKey(iRBTConstant.VOLUNTARY);
					}
					if (suspend && isAlreadySuspended) {
						logger.info("response: "
								+ ALREADY_VOLUNTARILY_SUSPENDED);
						return ALREADY_VOLUNTARILY_SUSPENDED;
					}

					if (!suspend && !isAlreadySuspended) {
						logger.info("response: " + NOT_VOLUNTARILY_SUSPENDED);
						return NOT_VOLUNTARILY_SUSPENDED;
					}
				}
				if (suspend && isActPendingDownloads) {
					logger.info("Pending downloads are present");
					response = DOWNLOADS_PENDING;
					logger.info("response: " + response);
					return response;
				}
				if (getParamAsBoolean(iRBTConstant.COMMON,
						iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")) {
					// logger.info("processSuspension:Selections",
					// "httpParameters:iRBTConstant.SUPPORT_SMCLIENT_API==true ");
					StringBuilder builder = new StringBuilder(
							"SM client request for add selection ");
					builder.append("[ subID " + subscriberID);
					builder.append(", prepaidYes " + subscriber.prepaidYes());
					builder.append(", subscription class "
							+ subscriber.subscriptionClass());
					builder.append(", mode - " + getMode(task));
					logger.info(builder.toString());

					RBTSMClientResponse smClientResponse = null;
					if (isCorporateSelSuspensionAllowed) {
						smClientResponse = RBTSMClientHandler.getInstance()
								.suspendSubscription(subscriberID,
										subscriber.prepaidYes(),
										subscriber.subscriptionClass(),
										getMode(task));
					} else {

						if (downloads != null && downloads.length > 0
								&& !isNoCrporateDownloadPresent) {
							response = ERROR;
							for (int count = 0; count < downloads.length
									&& downloads[count] != null; count++) {
								// isNoCrporateDownloadPresent=false;
								// there is no seperate API to resume RBT
								// service.
								if (Utility.isValidSuspensionRequestDownloads(
										downloads[count], suspend, corpCatIDs)) {
									RBTSMClientResponse smClientResponseTemp = RBTSMClientHandler
											.getInstance().suspendSelections(
													subscriberID,
													subscriber.prepaidYes(),
													downloads[count]
															.classType(),
													getMode(task),
													downloads[count].refID());
									if (smClientResponse == null) {
										smClientResponse = smClientResponseTemp;
									} else if (smClientResponse.getMessage() != null
											&& (smClientResponse.getMessage()
													.indexOf("Failure") != -1
													|| smClientResponse
															.getMessage()
															.indexOf("FAILURE") != -1 || smClientResponse
													.getMessage().indexOf(
															"failure") != -1)) {
										smClientResponse = smClientResponseTemp;
									}

								}
							}
						}
						if (isNoCrporateDownloadPresent) {
							smClientResponse = RBTSMClientHandler.getInstance()
									.suspendSubscription(subscriberID,
											subscriber.prepaidYes(),
											subscriber.subscriptionClass(),
											getMode(task));
						}
					}
					logger.info("SMClient Response for Suspend "
							+ smClientResponse.toString());
					response = smClientResponse.getMessage();
				} else {
					// RBTLogger
					// .logDetail(CLASSNAME, "processSuspension:Selections",
					// "httpParameters:iRBTConstant.SUPPORT_SMCLIENT_API==false ");
					Parameters suspentionUrlParam = null;
					if (suspend)
						suspentionUrlParam = parametersCacheManager
								.getParameter(iRBTConstant.COMMON,
										"VOLUNTARY_SUSPENSION_ON_URL");
					else
						suspentionUrlParam = parametersCacheManager
								.getParameter(iRBTConstant.COMMON,
										"VOLUNTARY_SUSPENSION_OFF_URL");

					if (isCorporateSelSuspensionAllowed) {
						logger.info("httpParameters:isCorporateSelSuspensionAllowed==true ");
						response = processSubscriptionSuspension(task,
								suspentionUrlParam.getValue().trim(),
								subscriber);
					} else {
						logger.info("httpParameters:isCorporateSelSuspensionAllowed==false ");
						if (downloads != null && downloads.length > 0
								&& !isNoCrporateDownloadPresent) {
							response = ERROR;
							logger.info("httpParameters:  isNoCrporateDownloadPresent==false ");
							for (int count = 0; count < downloads.length
									&& downloads[count] != null; count++) {
								if (Utility.isValidSuspensionRequestDownloads(
										downloads[count], suspend, corpCatIDs)) {
									String intRefId = downloads[count].refID();
									String url = suspentionUrlParam.getValue()
											.trim();
									url = url.replaceAll("%SUBSCRIBER_ID%",
											subscriberID);
									url = url.replaceAll(
											"%SERVICE_KEY%",
											"RBT_SEL_"
													+ downloads[count]
															.classType());
									url = url.replaceAll("%MODE%",
											getMode(task));
									url = url.replaceAll("%REFID%", intRefId);

									HttpParameters httpParameters = new HttpParameters(
											url);
									Utility.setSubMgrProxy(httpParameters);
									logger.info("httpParameters: "
											+ httpParameters);

									HttpResponse httpResponse = RBTHttpClient
											.makeRequestByGet(httpParameters,
													null);
									logger.info("httpResponse: " + httpResponse);
									if (httpResponse.getResponseCode() == 724) {
										response = ALREADY_VOLUNTARILY_SUSPENDED;
									} else {
										String[] status = httpResponse
												.getResponse().trim()
												.split("\\|");
										String responseTemp = null;
										responseTemp = status[0];
										if (response == null) {
											response = responseTemp;
										} else if (response.indexOf("Failure") != -1
												|| response.indexOf("FAILURE") != -1
												|| response.indexOf("failure") != -1) {
											response = responseTemp;
										} else
											response = SUCCESS;
									}
								}
							}
						}
						if (isNoCrporateDownloadPresent) {
							logger.info("httpParameters:  isNoCrporateDownloadPresent==true ");
							response = processSubscriptionSuspension(task,
									suspentionUrlParam.getValue().trim(),
									subscriber);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	private String processSubscriptionSuspension(WebServiceContext task,
			String suspensionUrl, Subscriber subscriber) {
		String response = ERROR;

		try {
			String subscriberID = task.getString(param_subscriberID);
			suspensionUrl = suspensionUrl.replaceAll("%SUBSCRIBER_ID%",
					subscriberID);
			suspensionUrl = suspensionUrl.replaceAll("%SERVICE_KEY%",
					"RBT_ACT_" + subscriber.subscriptionClass());
			suspensionUrl = suspensionUrl.replaceAll("%MODE%", getMode(task));
			suspensionUrl = suspensionUrl.replaceAll("%REFID%", "null");

			HttpParameters httpParameters = new HttpParameters(suspensionUrl);
			Utility.setSubMgrProxy(httpParameters);
			logger.info("httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("httpResponse: " + httpResponse);

			if (httpResponse.getResponseCode() == 724) {
				response = ALREADY_VOLUNTARILY_SUSPENDED;
			} else {
				String[] status = httpResponse.getResponse().trim()
						.split("\\|");
				response = status[0];
			}
		} catch (Exception e) {
			logger.error("", e);
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processThirdPartyRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processThirdPartyRequest(WebServiceContext task) {
		String response = ERROR;

		try {
			if (!task.containsKey(param_info)) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			String info = task.getString(param_info);
			String url = parametersCacheManager
					.getParameter("THIRDPARTY_REQUEST", info).getValue().trim();

			if (task.containsKey(param_subscriberID))
				url = url.replaceAll("%SUBSCRIBER_ID%",
						task.getString(param_subscriberID));

			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("httpResponse: " + httpResponse);

			if (httpResponse.getResponse() != null)
				response = httpResponse.getResponse().trim();
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#addData(com.onmobile
	 * .apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String addData(WebServiceContext task) {
		String response = ERROR;

		try {
			String info = task.getString(param_info);
			if (info == null) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			if (info.equalsIgnoreCase(VIRAL_DATA)) {
				String subscriberID = task.getString(param_subscriberID);
				String callerID = task.getString(param_callerID);
				String type = task.getString(param_type);
				String clipID = task.getString(param_clipID);
				int count = task.containsKey(param_count) ? Integer
						.parseInt(task.getString(param_count)) : 0;
				String selectedBy = task.getString(param_mode);
				HashMap<String, String> infoMap = getInfoMap(task);
				logger.info("infoMap=" + infoMap);
				String keyPressed = null;
				if (infoMap != null && infoMap.containsKey("KEY"))
					keyPressed = infoMap.get("KEY");
				
//				infoMap.put("cosid", "")
				// Added to enable copystar only for configured circle
				/*
				 * if("COPY".equals(type) || "COPYSTAR".equals(type)) {
				 * if(selectedBy == null && keyPressed == null) insertinDB =
				 * false; }
				 */
				/*
				 * if(keyPressed != null ) { if(directCopyKeysMap != null ||
				 * optinCopyKeysMap != null) { String circleID = null;
				 * SubscriberDetail subscriberDetail =
				 * RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID,
				 * "COPY")); if (subscriberDetail != null) circleID =
				 * subscriberDetail.getCircleID();
				 * if(subscriberDetail.isValidSubscriber() && circleID != null)
				 * { if(directCopyKeysMap != null &&
				 * directCopyKeysMap.containsKey(circleID)) {
				 * 
				 * circleConfigPresent = true; HashSet<String> hashSet =
				 * directCopyKeysMap.get(circleID); for (String key : hashSet) {
				 * if(keyPressed.indexOf(key) != -1) { type="COPY"; foundMatch =
				 * true; break; } } } if(!foundMatch && optinCopyKeysMap != null
				 * && optinCopyKeysMap.containsKey(circleID)) {
				 * circleConfigPresent = true; HashSet<String> hashSet =
				 * optinCopyKeysMap.get(circleID); for (String key : hashSet) {
				 * if(keyPressed.indexOf(key) != -1) { type="COPYSTAR";
				 * foundMatch = true; break; } } } }
				 * 
				 * } }
				 */
				/*
				 * if (type.equalsIgnoreCase("COPYSTAR")) { String circleID =
				 * null; SubscriberDetail subscriberDetail =
				 * RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID,
				 * "COPY")); if (subscriberDetail != null) circleID =
				 * subscriberDetail.getCircleID();
				 * 
				 * Parameters copyStartEnabledCirclesParam =
				 * parametersCacheManager.getParameter("COPY",
				 * "COPYSTAR_ENABLED_CIRCLES"); if (copyStartEnabledCirclesParam
				 * != null) { type = "COPY"; List<String>
				 * copystarEnabledCirclesList =
				 * Arrays.asList(copyStartEnabledCirclesParam
				 * .getValue().split(",")); if
				 * (copystarEnabledCirclesList.contains(circleID)) type =
				 * "COPYSTAR"; } }
				 * 
				 * if(circleConfigPresent && !foundMatch) insertinDB = false;
				 */
				ViralSMSTable viralData = rbtDBManager.insertViralSMSTableMap(
						subscriberID, new Date(), type, callerID, clipID,
						count, selectedBy, null, infoMap);

				if (viralData != null) {
					task.put(param_viralData, viralData);
				}
				response = SUCCESS;
			} else if (info
					.equalsIgnoreCase(PENDING_CONFIRMATIONS_REMINDER_DATA)) {
				String subscriberID = task.getString(param_subscriberID);
				int remindersLeft = 0;
				Date lastReminderSent = null;
				Date smsReceivedTime = null;
				String reminderText = task.getString(param_reminderText);
				String sender = null;
				long smsId = 0;

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				try {
					sender = task.getString(param_sender);
					remindersLeft = (task.containsKey(param_remindersLeft)) ? Integer
							.parseInt(task.getString(param_remindersLeft)) : 0;
					if (task.containsKey(param_lastReminderSent)) {
						lastReminderSent = dateFormat.parse(task
								.getString(param_lastReminderSent));
					}
					if (task.containsKey(param_smsReceivedTime)) {
						smsReceivedTime = dateFormat.parse(task
								.getString(param_smsReceivedTime));
					}
					if (task.containsKey(param_smsID)) {
						smsId = Long.parseLong(task.getString(param_smsID));
					}

					logger.debug("Inserting subscriberID: " + subscriberID
							+ ", remindersLeft: " + remindersLeft
							+ ", lastReminderSent: " + lastReminderSent
							+ ", smsReceivedTime: " + smsReceivedTime
							+ ", reminderText: " + reminderText + ", sender: "
							+ sender + ", smsId: " + smsId);
					PendingConfirmationsReminderTableImpl pendingConfirmationsRemainder = rbtDBManager
							.insertPendingConfirmationRemainder(subscriberID,
									remindersLeft, lastReminderSent,
									smsReceivedTime, reminderText, sender,
									smsId);

					if (pendingConfirmationsRemainder != null) {
						task.put(param_pendingConfirmationsReminderData,
								pendingConfirmationsRemainder);
						response = SUCCESS;
					}

				} catch (ParseException pe) {
					logger.error(
							"Unable to parse date. ParseException: "
									+ pe.getMessage(), pe);
				} catch (Exception e) {
					logger.error(
							"Unable to transform request from task object. Exception"
									+ e.getMessage(), e);
				}

			} else if (info.equalsIgnoreCase(TRANS_DATA)) {
				String transID = task.getString(param_transID);
				String subscriberID = task.getString(param_subscriberID);
				String type = task.getString(param_type);

				TransData transData = rbtDBManager.addTransData(transID,
						subscriberID, type);

				if (transData != null) {
					response = SUCCESS;
					task.put(param_transData, transData);
				} else
					response = FAILED;
			} else if (info.equalsIgnoreCase(RBTSUPPORT_DATA)) {
				String subscriberID = task.getString(param_subscriberID);
				String callerID = task.getString(param_callerID);
				int clipId = Integer.parseInt(task.getString(param_clipID));
				HashMap<String, String> infoMap = getInfoMap(task);
				logger.info("infoMap=" + infoMap);

				int status = RbtSupport.PROCESS_PENDING;
				int type = RbtSupport.HASH_DOWNLOAD;

				RbtSupport rbtSupport = rbtDBManager.addRbtSupport(
						subscriberID, callerID, clipId, infoMap, status, type);
				if (rbtSupport != null) {
					response = SUCCESS;
					task.put(param_rbtSupportData, rbtSupport);
				} else
					response = FAILED;

			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processData(com
	 * .onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processData(WebServiceContext task) {
		String response = ERROR;

		// Future use

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#updateData(com
	 * .onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String updateData(WebServiceContext task) {
		String response = ERROR;
		logger.info("Inside update data");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		try {
			String info = task.getString(param_info);
			if (info == null) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			if (info.equalsIgnoreCase(VIRAL_DATA)) {
				String subscriberID = task.getString(param_subscriberID);
				String callerID = task.getString(param_callerID);
				String type = task.getString(param_type);

				Date sentTime = null;
				if (task.containsKey(param_sentTime)) {
					try {
						sentTime = dateFormat.parse(task
								.getString(param_sentTime));
					} catch (ParseException e) {
						logger.error("", e);
					}
				}

				HashMap<String, String> infomap = new HashMap<String, String>();

				if (task.containsKey(param_mode)) {
					infomap.put("COPY_TYPE", "OPTIN");
					infomap.put("COPY_MODE", task.getString(param_mode));
					infomap.putAll(getInfoMap(task));

				} else {
					infomap.putAll(getInfoMap(task));
				}

				boolean updated = false;
				if (task.containsKey(param_duration)) {
					int duration = Integer.parseInt(task
							.getString(param_duration));
					if (task.getString(param_duration).equals("-1")) {
						Parameters param = parametersCacheManager.getParameter(
								iRBTConstant.GATHERER,
								"WAIT_TIME_DOUBLE_CONFIRMATION");
						if (param != null) {
							duration = Integer.parseInt(param.getValue());
						}
					}
					String newType = task.getString(param_newType);
					updated = rbtDBManager.updateViralSMSTypeOfCaller(callerID,
							type, newType, duration, infomap);
					task.put(param_type, newType);
				} else if (task.containsKey(param_newType)) {
					String newType = task.getString(param_newType);

					if (newType.equals("VIRAL_EXPIRED")
							|| newType.equals("VIRAL_OPTOUT")
							|| newType.equals("VIRAL_OPTIN")) {
						updated = rbtDBManager
								.updateViralPromotionTypeBySubscriberIDAndType(
										newType, subscriberID, type);
						task.put(param_type, newType);
					} else {
						String newCallerID = task.getString(param_newCallerID);
						String selectedBy = task.getString(param_mode);
						boolean updateSmsID = task
								.containsKey(param_updateSmsID)
								&& task.getString(param_updateSmsID)
										.equalsIgnoreCase(YES);

						String finalExtraInfo = DBUtility
								.getAttributeXMLFromMap(infomap);
						updated = rbtDBManager.updateViralPromotion(
								subscriberID, callerID, newCallerID, sentTime,
								type, newType, new Date(), selectedBy,
								finalExtraInfo, updateSmsID);
						task.put(param_type, newType);
					}
				} else if (task.containsKey(param_count)) {
					int count = Integer.parseInt(task.getString(param_count));
					String clipID = task.getString(param_clipID);
					updated = rbtDBManager.updateViralSearchCount(subscriberID,
							callerID, type, sentTime, count,clipID);
				} else if (task.containsKey(param_smsID)) {
					long smsID = Long.parseLong(task.getString(param_smsID));
					String circleID = task.getString(param_circleID);
					updated = rbtDBManager.updateViralData(smsID, type,
							circleID);
				}

				if (updated)
					response = SUCCESS;
				else
					response = FAILED;
			} else if (info
					.equalsIgnoreCase(PENDING_CONFIRMATIONS_REMINDER_DATA)) {
				String subscriberId = task.getString(param_subscriberID);
				int remindersLeft = Integer.parseInt(task
						.getString(param_remindersLeft));
				Date lastReminderSent = dateFormat.parse(task
						.getString(param_lastReminderSent));
				int count = rbtDBManager.updatePendingConfirmationReminder(
						subscriberId, remindersLeft, lastReminderSent);
				if (count > 0) {
					response = SUCCESS;
				} else {
					response = FAILED;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#removeData(com
	 * .onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String removeData(WebServiceContext task) {
		String response = ERROR;

		try {
			String info = task.getString(param_info);
			if (info == null) {
				logger.info("Invalid info. Returning response: "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			}

			if (info.equalsIgnoreCase(VIRAL_DATA)) {
				String subscriberID = task.getString(param_subscriberID);
				String callerID = task.getString(param_callerID);
				String type = task.getString(param_type);

				Date sentTime = null;
				if (task.containsKey(param_sentTime)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					try {
						sentTime = dateFormat.parse(task
								.getString(param_sentTime));
					} catch (ParseException e) {
						logger.error("", e);
					}
				}

				boolean removed = false;
				if (task.containsKey(param_smsID)) {
					long smsID = Long.parseLong(task.getString(param_smsID));
					removed = rbtDBManager.deleteViralPromotionBySMSID(smsID, type);
				} else // Added to delete all copyconfpending entries in Viral
						// SMS table
				if (type != null && type.equals("XCOPYCONFPENDING")) {
					removed = rbtDBManager.removeViralSMSOfCaller(callerID,
							"COPYCONFPENDING");
				} else if (type != null
						&& (type.equals("COPYCONFIRM") || type
								.equals("COPYCONFPENDING"))) {
					if (task.containsKey(param_duration)) {
						int duration = Integer.parseInt(task
								.getString(param_duration));
						removed = rbtDBManager
								.removeCopyPendingViralSMSOfCaller(callerID,
										type, duration);
					} else
						removed = rbtDBManager.removeViralPromotion(
								subscriberID, callerID, sentTime, type);
				} else {
					removed = rbtDBManager.deleteViralPromotion(subscriberID,
							callerID, type, sentTime);
				}

				if (removed)
					response = SUCCESS;
				else
					response = FAILED;
			} else if (info.equalsIgnoreCase(TRANS_DATA)) {
				String transID = task.getString(param_transID);
				String type = task.getString(param_type);

				boolean removed = rbtDBManager.removeTransData(transID, type);

				if (removed)
					response = SUCCESS;
				else
					response = FAILED;
			} else if (info
					.equalsIgnoreCase(PENDING_CONFIRMATIONS_REMINDER_DATA)) {
				int deleteLimit = 0;
				if (task.containsKey(param_deleteLimit)) {
					deleteLimit = Integer.parseInt(task
							.getString(param_deleteLimit));
				}
				int count = rbtDBManager
						.deletePendingConfirmationRemainder(deleteLimit);
				if (count > 0) {
					response = SUCCESS;
				} else {
					response = FAILED;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	protected String isValidUser(WebServiceContext task, Subscriber subscriber) {
		return DataUtils.isValidUser(task, subscriber);
	}

	protected Clip getClip(WebServiceContext task) {
		Clip clip = null;

		if (task.containsKey(session_clip)) {
			Clip clipFromSession = (Clip) task.get(session_clip);
			logger.debug("Getting clip from session. Clip: " + clipFromSession);
			return clipFromSession;
		}

		String browsingLanguage = task.getString(param_browsingLanguage);
		if (task.getString(param_clipPromoID) != null) {
			clip = rbtCacheManager.getClipByPromoId(
					task.getString(param_clipPromoID), browsingLanguage);
			logger.debug("Getting clip by promoId. Clip: " + clip);
			return clip;
		}

		if (task.getString(param_clipSmsAlias) != null) {
			clip = rbtCacheManager.getClipBySMSAlias(
					task.getString(param_clipSmsAlias), browsingLanguage);
			logger.debug("Getting clip by Sms Alias. Clip: " + clip);
			return clip;
		}

		String clipIDStr = task.getString(param_clipID);

		if (clipIDStr == null)
			return null;

		try {
			int clipID = Integer.parseInt(clipIDStr);
			clip = rbtCacheManager.getClip(clipID, browsingLanguage);
		} catch (NumberFormatException e) {

		}

		if (clip == null)
			clip = rbtCacheManager
					.getClipByPromoId(clipIDStr, browsingLanguage);
		if (clip == null)
			clip = rbtCacheManager.getClipByRbtWavFileName(clipIDStr,
					browsingLanguage);

		return clip;
	}

	protected String getMode(WebServiceContext task) {
		String mode = VP;
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);

		if (task.getString(param_action) != null && task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			mode = "GIFT";

		logger.info("Request mode: " + mode);
		return mode;
	}

	protected String getModeInfo(WebServiceContext task) {
		String modeInfo = VP;
		if (task.containsKey(param_modeInfo))
			modeInfo = task.getString(param_modeInfo);

		if (task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			modeInfo += ":" + task.getString(param_gifterID);
		else if (task.containsKey(param_calledNo))
			modeInfo += ":" + task.getString(param_calledNo);
		else if (task.containsKey(param_ipAddress))
			modeInfo += ":" + task.getString(param_ipAddress);

		if (task.containsKey(param_retailerID))
			modeInfo += ":ret:" + task.getString(param_retailerID);

		if (task.containsKey(param_dontSMSInBlackOut)
				&& task.getString(param_dontSMSInBlackOut)
						.equalsIgnoreCase(YES))
			modeInfo = "BULK:" + modeInfo;

		if (task.containsKey(param_smsSent)) {
			modeInfo += ":SMSKEY=" + task.getString(param_smsSent);
		}
		//RBT-12693
//		if (task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
			if (task.containsKey(param_selectionInfo + "_ua")) {
				String ua = task.getString(param_selectionInfo + "_ua");
				modeInfo += ":ua=" + ua;
				task.remove(param_selectionInfo + "_ua");
				task.put("ua", ua);
			} else if (task.containsKey("ua")) {
				modeInfo += ":ua=" + task.getString("ua");
				task.remove("ua");
			}
			if (task.containsKey(param_selectionInfo + "_ct")) {
				String rc = task.getString(param_selectionInfo + "_rc");
				modeInfo += ":rc=" + rc;
				task.remove(param_selectionInfo + "_rc");
				task.put("rc", rc);
			} else if (task.containsKey("rc")) {
				modeInfo += ":rc=" + task.getString("rc");
				task.remove("rc");
			}
			if (task.containsKey(param_selectionInfo + "_ct")) {
				String ct = task.getString(param_selectionInfo + "_ct");
				modeInfo += ":ct=" + ct;
				task.remove(param_selectionInfo + "_ct");
				task.put("ct", ct);
			} else if (task.containsKey("ct")) {
				modeInfo += ":ct=" + task.getString("ct");
				task.remove("ct");
			}
			if (task.containsKey(param_selectionInfo + "_dc")) {
				String dc = task.getString(param_selectionInfo + "_dc");
				modeInfo += ":dc=" + dc;
				task.remove(param_selectionInfo + "_dc");
				task.put("dc", dc);
			} else if (task.containsKey("dc")) {
				modeInfo += ":dc=" + task.getString("dc");
				task.remove("dc");
			}

			if (task.containsKey(param_selectionInfo + "_br")) {
				String br = task.getString(param_selectionInfo + "_br");
				modeInfo += ":br="
						+ task.getString(param_selectionInfo + "_br");
				task.remove(param_selectionInfo + "_br");
				task.put("br", br);
			} else if (task.containsKey("br")) {
				modeInfo += ":br=" + task.getString("br");
				task.remove("br");
			}

			if (task.containsKey(param_selectionInfo + "_sname")) {
				String sname = task.getString(param_selectionInfo + "_sname");
				modeInfo += ":sname="
						+ task.getString(param_selectionInfo + "_sname");
				task.remove(param_selectionInfo + "_sname");
				task.put("sname", sname);
			} else if (task.containsKey("sname")) {
				modeInfo += ":sname=" + task.getString("sname"); 
				task.remove("sname");
			}

//		}

		logger.info("modeInfo: " + modeInfo);
		return modeInfo;
	}

	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber) {
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	protected String getSubscriptionClass(WebServiceContext task, CosDetails cos) {
		String subscriptionClass = null;
		logger.info("Getting the subscription class. task: " + task + ", cos: "
				+ cos);
		
		String action = task.getString(param_action);
		if (task.containsKey(param_subscriptionClass)) {
			subscriptionClass = task.getString(param_subscriptionClass);
			logger.info("Returning subscriptionClass: " + subscriptionClass
					+ ", request contains subscriptionClass");
		} else if (task.containsKey(param_rentalPack)) {
			subscriptionClass = task.getString(param_rentalPack);
			logger.info("Returning subscriptionClass: " + subscriptionClass
					+ ", request contains rental pack");
		} else if (action.equalsIgnoreCase(action_acceptGift)) {
			try {
				if (task.getString(param_action).equalsIgnoreCase(
						action_acceptGift)) {
					ViralSMSTable gift = null;
					if (task.containsKey(param_viralData))
						gift = (ViralSMSTable) task.get(param_viralData);
					else {
						String gifterID = task.getString(param_gifterID);
						String subscriberID = task
								.getString(param_subscriberID);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Date sentTime = dateFormat.parse(task
								.getString(param_giftSentTime));

						gift = rbtDBManager.getViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED");
					}

					if (gift != null) {
						task.put(param_viralData, gift);

						HashMap<String, String> giftExtraInfoMap = DBUtility
								.getAttributeMapFromXML(gift.extraInfo());
						if (giftExtraInfoMap != null) {
							subscriptionClass = giftExtraInfoMap
									.get(iRBTConstant.SUBSCRIPTION_CLASS);
							if (subscriptionClass == null) {
								Parameters giftSubClassParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"GIFT_SUBSCRIPTION_CLASS",
												"GIFT");
								subscriptionClass = giftSubClassParam
										.getValue().trim();
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			logger.info("Fetched subscriptionClass: " + subscriptionClass
					+ ", for action: gift");
		} else if (task.containsKey(param_mmContext)) {
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_PROMOTION"))
				subscriptionClass = mmContext[1];
			else if (mmContext[0].equalsIgnoreCase("RBT_CATEGORY")
					&& mmContext.length > 2)
				subscriptionClass = mmContext[2];
			else if (mmContext[0].equalsIgnoreCase("RBT_CLIP")
					&& mmContext.length > 3)
				subscriptionClass = mmContext[3];
			logger.info("Fetched subscriptionClass: " + subscriptionClass
					+ ", request contains mmContext");
		}

		if (cos != null && subscriptionClass == null) {
			subscriptionClass = cos.getSubscriptionClass();
			logger.info("Fetched cos subscriptionClass: " + subscriptionClass
					+ ", subscriptionClass is null");
		}

		// Overriding the Subscription Class if ContentType is supported
		if (cos != null && cos.getContentTypes() != null) {
			subscriptionClass = cos.getSubscriptionClass();
			logger.info("Fetched subscriptionClass: " + subscriptionClass
					+ ", cos content types are exists");
		}

		// RBT subscription charge class management
		String offerTypeKey = param_selectionInfo + "_" + offerType;
		if (task.containsKey(offerTypeKey) && (offerTypeSubClassMapSize > 0)) {
			String offerType = task.getString(offerTypeKey);
			subscriptionClass = offerTypeSubClassMap.get(offerType);
			logger.debug("Since the request contains: " + offerTypeKey
					+ ", Updating subscriptionClass from map: "
					+ offerTypeSubClassMap);
		} else {
			logger.debug(offerTypeKey + " is not present in task object");
		}
		
		if (subscriptionClass == null) {
			logger.warn("Since subscriptionClass is null, assigning DEFAULT.");
			subscriptionClass = "DEFAULT";
		}

		logger.info("Returning subscriptionClass: " + subscriptionClass);
		return subscriptionClass;
	}

	protected String getChargeClass(WebServiceContext task,
			Subscriber subscriber, Category category, Clip clip) {
		String chargeClass = null;

		String action = task.getString(param_action);

		if (task.containsKey(param_chargeClass))
			chargeClass = task.getString(param_chargeClass);
		else if (action.equalsIgnoreCase(action_acceptGift)) {
			try {
				if (task.getString(param_action).equalsIgnoreCase(
						action_acceptGift)) {
					ViralSMSTable gift = null;
					if (task.containsKey(param_viralData))
						gift = (ViralSMSTable) task.get(param_viralData);
					else {
						String gifterID = task.getString(param_gifterID);
						String subscriberID = task
								.getString(param_subscriberID);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Date sentTime = dateFormat.parse(task
								.getString(param_giftSentTime));

						gift = rbtDBManager.getViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED");
					}

					if (gift != null) {
						task.put(param_viralData, gift);

						HashMap<String, String> giftExtraInfoMap = DBUtility
								.getAttributeMapFromXML(gift.extraInfo());
						if (giftExtraInfoMap != null) {
							chargeClass = giftExtraInfoMap
									.get(iRBTConstant.CHARGE_CLASS);
							if (chargeClass == null) {
								Parameters giftChargeClassParam = parametersCacheManager
										.getParameter(iRBTConstant.COMMON,
												"GIFT_CHARGE_CLASS", "FREE");
								chargeClass = giftChargeClassParam.getValue()
										.trim();
							}
							// Added by Sreekar as fix for RBT-4598
							task.put(param_useUIChargeClass, YES);
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		} else if (task.containsKey(param_mmContext)) {
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_PROMOTION")
					&& mmContext.length > 2)
				chargeClass = mmContext[2];
			else if (mmContext[0].equalsIgnoreCase("RBT_CATEGORY")
					&& mmContext.length > 3)
				chargeClass = mmContext[3];
			else if (mmContext[0].equalsIgnoreCase("RBT_CLIP")
					&& mmContext.length > 4)
				chargeClass = mmContext[4];
		} else if (subscriber != null) {
			SubscriptionClass subscriptionClass = CacheManagerUtil
					.getSubscriptionClassCacheManager().getSubscriptionClass(
							subscriber.subscriptionClass());
			if (subscriptionClass != null
					&& subscriptionClass.getFreeSelections() > 0
					&& subscriber.maxSelections() < subscriptionClass
							.getFreeSelections())
				chargeClass = "FREE";
		}

		logger.info("chargeClass: " + chargeClass);
		return chargeClass;
	}

	protected String getChargingPackage(WebServiceContext task,
			Subscriber subscriber, Category category, Clip clip) {
		String chargingPackage = null;

		logger.info("response: " + chargingPackage);
		return chargingPackage;
	}

	protected HashMap<String, String> getUserInfoMap(WebServiceContext task) {
		if (task.containsKey(param_userInfo+"_"+iRBTConstant.CAMPAIGN_CODE)) { 
			String campaignCode = task.getString(param_userInfo+"_"+iRBTConstant.CAMPAIGN_CODE);
			task.put(iRBTConstant.CAMPAIGN_CODE, campaignCode);
			task.remove(param_userInfo+"_"+iRBTConstant.CAMPAIGN_CODE) ;
		}
		
		if (task.containsKey(param_userInfo+"_"+iRBTConstant.TREATMENT_CODE)) { 
			String treatmentCode = task.getString(param_userInfo+"_"+iRBTConstant.TREATMENT_CODE);
			task.put(iRBTConstant.TREATMENT_CODE, treatmentCode); 
			task.remove(param_userInfo+"_"+iRBTConstant.TREATMENT_CODE) ;
		}

		if (task.containsKey(param_userInfo+"_"+iRBTConstant.OFFER_CODE)) { 
			String offerCode = task.getString(param_userInfo+"_"+iRBTConstant.OFFER_CODE);
			task.put(iRBTConstant.OFFER_CODE, offerCode);
			task.remove(param_userInfo+"_"+iRBTConstant.OFFER_CODE) ;
		}

		HashMap<String, String> userInfoMap = new HashMap<String, String>();

		Set<Entry<String, Object>> entrySet = task.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().startsWith(param_userInfo + "_"))
				userInfoMap.put(
						entry.getKey().substring(
								entry.getKey().indexOf('_') + 1),
						(String) entry.getValue());
		}

		if (task.containsKey(param_viralData)
				&& task.get(param_viralData) != null) {
			ViralSMSTable gift = (ViralSMSTable) task.get(param_viralData);

			HashMap<String, String> giftExtraInfoMap = DBUtility
					.getAttributeMapFromXML(gift.extraInfo());
			if (giftExtraInfoMap != null
					&& giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID) != null)
				userInfoMap.put(iRBTConstant.GIFT_TRANSACTION_ID,
						giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID));
		}

		logger.info("response: " + userInfoMap);
		return userInfoMap;
	}

	protected HashMap<String, String> getSelectionInfoMap(WebServiceContext task) {
		HashMap<String, String> selectionInfoMap = new HashMap<String, String>();

		Set<Entry<String, Object>> entrySet = task.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().startsWith(param_selectionInfo + "_"))
				selectionInfoMap.put(
						entry.getKey().substring(
								entry.getKey().indexOf('_') + 1),
						(String) entry.getValue());
		}

		if (task.getString(param_action).equalsIgnoreCase(action_acceptGift))
			selectionInfoMap.put("GIFTER", task.getString(param_gifterID));

		if (task.containsKey(param_viralData)
				&& task.get(param_viralData) != null) {
			ViralSMSTable gift = (ViralSMSTable) task.get(param_viralData);

			HashMap<String, String> giftExtraInfoMap = DBUtility
					.getAttributeMapFromXML(gift.extraInfo());
			if (giftExtraInfoMap != null
					&& giftExtraInfoMap.get(iRBTConstant.GIFT_TRANSACTION_ID) != null)
				selectionInfoMap.put(
						iRBTConstant.GIFT_TRANSACTION_ID,
						""
								+ giftExtraInfoMap
										.get(iRBTConstant.GIFT_TRANSACTION_ID));
		}

		logger.info("selectionInfoMap: " + selectionInfoMap);
		return selectionInfoMap;
	}

	protected HashMap<String, String> getInfoMap(WebServiceContext task) {
		HashMap<String, String> infoMap = new HashMap<String, String>();

		Set<Entry<String, Object>> entrySet = task.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().startsWith(param_info + "_"))
				infoMap.put(
						entry.getKey().substring(
								entry.getKey().indexOf('_') + 1),
						(String) entry.getValue());
		}

		logger.info("response: " + infoMap);
		return infoMap;
	}

	protected boolean getParamAsBoolean(String type, String param,
			String defaultVal) {
		try {
			return parametersCacheManager.getParameter(type, param, defaultVal)
					.getValue().equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	/**
	 * @param task
	 * @return String It will return base offerid or selection offerid
	 */
	protected static int BASE_OFFERTYPE = 1;
	protected static int SELECTION_OFFERTYPE = 2;
	protected static int COMBO_SUB_OFFERTYPE = 3;
	protected static int GIFT_OFFERTYPE = 3;

	protected String getOfferID(WebServiceContext task, int offerType) {
		String offerId = null;
		if (offerType == BASE_OFFERTYPE || offerType == SELECTION_OFFERTYPE) {
			if (task.containsKey(param_offerID)) {
				offerId = task.getString(param_offerID);
			}
			if (offerId == null || (offerId = offerId.trim()).equals("")) {
				String classType = null;
				if (offerType == BASE_OFFERTYPE) {
					classType = task.getString(param_subscriptionClass);
				} else {
					classType = task.getString(param_chargeClass);
				}
				if (classType != null
						&& !(classType = classType.trim()).equals("")) {
					offerId = "-2";
				} else {
					offerId = "-1";
				}
			}
		} else if (offerType == COMBO_SUB_OFFERTYPE) {
			if (task.containsKey(param_subscriptionOfferID)) {
				offerId = task.getString(param_subscriptionOfferID);
			}
			if (offerId == null || (offerId = offerId.trim()).equals("")) {
				String classType = task.getString(param_subscriptionClass);
				if (classType != null
						&& !(classType = classType.trim()).equals("")) {
					offerId = "-2";
				} else {
					offerId = "-1";
				}
			}
		}
		return offerId;
	}

	private String doesSubHaveGiftAmount(String gifter, String giftedTo,
			String song, String selectedBy, WebServiceContext task,
			Date sentTime) {
		try {
			Subscriber subscriber = null;
			subscriber = rbtDBManager.getSubscriber(gifter);
			if (subscriber == null) {
				logger.info("Subscriber object is null for subscriberId "
						+ gifter);
				return ERROR;
			}

			Clip clip = null;

			boolean bGiftSrv = false;

			if (song == null
					&& getParamAsString(iRBTConstant.DAEMON,
							"GIFT_SERVICE_ACCEPT_URL", null) != null)
				bGiftSrv = true;

			String browsingLanguage = task.getString(param_browsingLanguage);
			if (!song.startsWith("C")) {
				try {
					clip = rbtCacheManager.getClip(Integer.parseInt(song),
							browsingLanguage);
				} catch (Throwable e1) {
				}
			}

			String baseOfferId = getOfferID(task, COMBO_SUB_OFFERTYPE);
			String selOfferId = getOfferID(task, SELECTION_OFFERTYPE);
			String subscriptionClass = task.getString(param_subscriptionClass);
			String chargeClass = task.getString(param_chargeClass);
			SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String refId = "RBTGIFT:" + giftedTo + ":" + song + ":"
					+ m_sdf.format(sentTime);

			String gifteeStatus = "D";
			Hashtable<String, String> userInfo = new Hashtable<String, String>();
			SubscriberDetail subscriberDetail = RbtServicesMgr
					.getSubscriberDetail(new MNPContext(giftedTo, "GIFT"));
			boolean isNonOnmobilePrefix = subscriberDetail.getCircleID()
					.equalsIgnoreCase("NON_ONMOBILE");
			if (bGiftSrv || chargeClass != null) {
				logger.info(" isNonOnmobilePrefix(giftedTo) "
						+ isNonOnmobilePrefix + " gifter " + gifter
						+ " giftedTo " + giftedTo);

				// String url =
				// getParamAsString(iRBTConstant.DAEMON,"GIFT_CHARGE_URL",null);
				if (getParamAsBoolean("DAEMON", "CHECK_SUBSCRIPTION_STATUS",
						"FALSE") && isNonOnmobilePrefix) {
					String checkURL = getParamAsString(iRBTConstant.DAEMON,
							"CHECK_SUBSCRIPTION_STATUS_URL", null);
					checkURL = checkURL + "srcSubscriberID=" + giftedTo;
					logger.info("m_checkSubscriptionStatusURL " + checkURL);
					StringBuffer strBuf = new StringBuffer();
					StringBuffer httpStatus = new StringBuffer();
					String result = callURL(checkURL, true, strBuf, httpStatus);
					if (result == null || result.trim().equals("STATUS_RETRY")) {
						return "STATUS_RETRY";
					} else if (result.trim().startsWith("SUCCESS")) {
						gifteeStatus = "A";
					} else if (result.trim().equals("STATUS_ERROR")) {
						gifteeStatus = "D";
					} else
						return "STATUS_RETRY";
				}

				// user_info = "";
				if (clip != null) {
					String id = clip.getClipPromoId();
					String name = clip.getClipName();
					int clipID = clip.getClipId();
					String movieName = clip.getAlbum();
					String clipInfo = clip.getClipInfo();
					if (movieName != null && movieName.length() > 20)
						movieName = movieName.substring(0, 20);
					if (name != null)
						name = name.replaceAll("'", "");
					if (clipInfo != null && !clipInfo.equalsIgnoreCase("null"))
						clipInfo = clipInfo.replaceAll("=", ":");
					else
						clipInfo = "";

					if (clip.getClipGrammar() != null
							&& clip.getClipGrammar().equalsIgnoreCase("ugc"))
						userInfo.put("ugccreator", clip.getAlbum());

					// user_info += "CONTENT_ID:contentid="+clipID+"|songname:"
					// + name + "|songcode:" +
					// id+"|"+clipInfo+"|moviename:"+movieName;
					userInfo.put("contentid", Integer.toString(clipID));
					userInfo.put("songname", name);
					userInfo.put("songcode", id);
					userInfo.put("moviename", movieName);
				} else {
					// user_info += "songname:|songcode:|moviename:";
				}
				StringBuilder builder = new StringBuilder(
						"SMClient Request for Caharge Gift : ");
				builder.append("[ SubID - ");
				builder.append(gifter);
				builder.append("[ PrepaidYes - ");
				builder.append(subscriber.prepaidYes());
				builder.append("[ subscriptionClass - ");
				builder.append(subscriptionClass);
				builder.append("[ mode - ");
				builder.append(selectedBy);
				builder.append("[ Giftee - ");
				builder.append(giftedTo);
				builder.append("[ GifteeStatus - ");
				builder.append(gifteeStatus);
				builder.append("[ CharegeClass - ");
				builder.append(chargeClass);
				builder.append("[ refID - ");
				builder.append(refId);
				builder.append("[ SubOfferID - ");
				builder.append(baseOfferId);
				builder.append("[ selOfferID - ");
				builder.append(selOfferId);
				logger.info(" SMClient Request for Caharge Gift : "
						+ builder.toString());
				RBTSMClientResponse smClientResponse = RBTSMClientHandler
						.getInstance().chargeGift(gifter,
								subscriber.prepaidYes(), subscriptionClass,
								selectedBy, giftedTo, gifteeStatus,
								chargeClass, refId, baseOfferId, selOfferId,
								userInfo);
				String result = smClientResponse.getResponse();
				logger.info(" SMClient Response for Caharge Gift : "
						+ smClientResponse.toString());
				return result;
			}
		} catch (Throwable e) {

		}
		return "STATUS_ERROR";
	}

	private String callURL(String url, boolean isChargededuction,
			StringBuffer strBuf, StringBuffer httpStatusBuf) {
		String response = null;
		int statusCode = 0;

		try {
			logger.info("url to be called " + url);
			if (url != null)
				url.replaceAll(" ", "%20");
			HttpParameters httpParameters = new HttpParameters(url);
			httpParameters.setConnectionTimeout(60 * 1000);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			statusCode = httpResponse.getResponseCode();

			response = httpResponse.getResponse().trim();
			if (response != null)
				response = response.trim();
			if (strBuf != null)
				strBuf.append(response);
			if (httpStatusBuf != null)
				httpStatusBuf.append(statusCode);
			logger.info("response " + response);

			logger.info("statusCode recieved " + statusCode);
			if (!isChargededuction) {
				if (statusCode == 200)
					return response;
				else
					return "STATUS_ERROR";
			} else {
				if (response != null && response.startsWith("SUCCESS")) {
					if (response.equals("SUCCESS:DEACTIVE")
							|| response.equals("SUCCESS:NEWUSER"))
						return "STATUS_ERROR";
					else
						return response;
				} else if (response != null && response.startsWith("RETRY"))
					return "STATUS_RETRY";
				else
					return "STATUS_ERROR";
			}

		} catch (Throwable e) {
			logger.error("", e);
			return null;
		} finally {
		}
	}

	protected String getParamAsString(String type, String param, String defualtVal) {
		try {
			return parametersCacheManager.getParameter(type, param, defualtVal)
					.getValue();
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defualtVal;
		}
	}

	// private boolean isNonOnmobilePrefix(String subID)
	// {
	// if(subID != null && subID.length() > 3)
	// {
	// int prefixIndex = rbtDBManager.getPrefixIndex();
	// if(m_nonOnmobilePrefix.contains(subID.substring(0, prefixIndex)))
	// return true;
	// }
	// return false;
	// }

	protected String makeConfirmSubscriptionHitToSM(String subscriberID,
			String mode) {
		Parameters confirmSubscriptionURL = parametersCacheManager
				.getParameter(iRBTConstant.COMMON, "CONFIRM_SUBSCRIPTION_URL",
						null);

		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);

		String url = confirmSubscriptionURL.getValue().trim();
		url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
		url = url.replaceAll("%SERVICE_KEY%",
				"RBT_ACT_" + subscriber.subscriptionClass());
		url = url.replaceAll("%MODE%", mode);

		HttpParameters httpParameters = new HttpParameters(url);
		logger.info("httpParameters: " + httpParameters);

		String response = FAILED;
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("httpResponse: " + httpResponse);
			String urlResponse = httpResponse.getResponse();
			if (urlResponse != null)
				urlResponse = urlResponse.trim();
			if ("success".equalsIgnoreCase(urlResponse))
				response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
		}
		return response;
	}

	protected boolean smClientRquestForSelection(WebServiceContext task,
			String subscriberID, Subscriber subscriber, String classType,
			String baseOfferID, String selOfferID, String selectedBy,
			String refId, boolean isPrepaid, int i,
			HashMap<String, String> extraParams) throws Exception {
		RBTSMClientResponse smClientResponse = null;
		boolean isBulkTask = task.containsKey(param_fromBulkTask);

		StringBuilder builder = new StringBuilder(
				"SM client request for add selection ");
		builder.append("[ subID " + subscriberID);
		builder.append(", prepaidYes " + subscriber.prepaidYes());
		builder.append(", chargeClass " + classType);
		builder.append(", subscription class " + subscriber.subscriptionClass());
		builder.append(", Subscription offerID " + baseOfferID);
		builder.append(", Selection offerID " + selOfferID);
		builder.append(", selectedBy - " + selectedBy);
		builder.append(", extraParams - " + extraParams);
		builder.append(", isBulkTask - " + isBulkTask);
		builder.append(", refId - " + refId);
		logger.info(builder.toString());
		// boolean acceptGift =
		// task.getString(param_action).equalsIgnoreCase(action_acceptGift);
		if (task.containsKey(param_newUser) && (i == -1 || i == 0)) {
			smClientResponse = RBTSMClientHandler.getInstance().comboRequest(
					subscriberID, isPrepaid, selectedBy, classType,
					subscriber.subscriptionClass(), selOfferID, baseOfferID,
					extraParams, isBulkTask, refId);
			logger.info("SMClient Response for combo subscribe"
					+ smClientResponse.toString());
		} else {
			smClientResponse = RBTSMClientHandler.getInstance()
					.activateSelection(subscriberID, isPrepaid, classType,
							selOfferID, selectedBy, refId.toString(),
							extraParams, isBulkTask);
			logger.info("SMClient Response for activateSelection"
					+ smClientResponse.toString());
		}
		// String selResponse = smClientResponse.getResponse();
		if (!smClientResponse.getResponse().equalsIgnoreCase(
				RBTSMClientResponse.SUCCESS)) {
			if (task.containsKey(param_newUser) && (i == -1 || i == 0)) {
				rbtDBManager.removeSubscriber(subscriber);
			}
			rbtDBManager.removeSelection(subscriberID, refId.toString());

			String response = Utility.getResponseString(smClientResponse
					.getResponse());
			if (!response.equalsIgnoreCase(SUCCESS))
				return false;
		}
		return true;
	}

	protected HashMap<String, String> getSelectionExtraParams(
			Subscriber subscriber, Clip clip, Category category,
			String callerID, String selectionInfo,
			HashMap<String, String> selectionInfoMap) {
		HashMap<String, String> extraParams = new HashMap<String, String>();
		HashMap<String, String> extraInfoMap = rbtDBManager
				.getExtraInfoMap(subscriber);

		String selectionUserInfo = getSelectionUserInfo(subscriber.subID(),
				clip, category, selectionInfo, subscriber.cosID(), callerID,
				selectionInfoMap);
		extraParams.put(RBTSMClientHandler.EXTRA_PARAM_USERINFO,
				selectionUserInfo);

		if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
				|| subscriber.subYes().equals(iRBTConstant.STATE_CHANGE))
			extraParams.put(
					RBTSMClientHandler.EXTRA_PARAM_LINKED_USERINFO,
					getActivationUserInfo(subscriber.activationInfo(),
							subscriber.cosID(), extraInfoMap));

		if (extraInfoMap != null
				&& !extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_IMEI_NO)
				&& selectionInfoMap
						.containsKey(iRBTConstant.EXTRA_INFO_IMEI_NO)) {
			// If subscriberExtraInfo doesn't contain IMEI, update it by reading
			// from SelectionInfo.(Nokia HandSet promo)
			rbtDBManager.updateExtraInfo(subscriber.subID(),
					iRBTConstant.EXTRA_INFO_IMEI_NO,
					selectionInfoMap.get(iRBTConstant.EXTRA_INFO_IMEI_NO));
		}
		return extraParams;
	}

	/**
	 * Added by Sreekar as it is from RBTDaemonManager for activation request
	 * 
	 * @param actInfo
	 * @param cosID
	 * @param extraInfo
	 * @return
	 */
	protected String getActivationUserInfo(String actInfo, String cosID,
			HashMap<String, String> extraInfo) {
		String contentID = "|CONTENT_ID:";
		contentID += "actinfo="
				+ actInfo.replaceAll("\\|", "/").replaceAll(":", ";");
		if (extraInfo != null
				&& extraInfo.containsKey(iRBTConstant.EXTRA_INFO_COPY_TYPE)) {
			String copyType = extraInfo.get(iRBTConstant.EXTRA_INFO_COPY_TYPE);
			contentID += (copyType
					.equalsIgnoreCase(iRBTConstant.EXTRA_INFO_COPY_TYPE_OPTIN)) ? ",copy_type=optin"
					: "";
		}
		contentID += "|cosid:" + cosID;
		if (extraInfo != null
				&& extraInfo
						.containsKey(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT))
			contentID += "|" + iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT + ":"
					+ extraInfo.get(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT);

		if (extraInfo != null
				&& extraInfo.containsKey(iRBTConstant.EXTRA_INFO_IMEI_NO))
			contentID += "|" + "imei_no:"
					+ extraInfo.get(iRBTConstant.EXTRA_INFO_IMEI_NO);
		return contentID;
	}

	/**
	 * As it is from RBTDaemonManager
	 * 
	 * @param clip
	 * @param category
	 * @param actInfo
	 * @param cosID
	 * @param callerID
	 * @param extraInfo
	 * @return
	 */
	protected String getSelectionUserInfo(String subscriberID, Clip clip,
			Category category, String actInfo, String cosID, String callerID,
			HashMap<String, String> extraInfo) {
		String userInfo = "";
		boolean isUGC = false;
		if (clip != null) {
			String grammar = clip.getClipGrammar();
			if (grammar != null && grammar.equalsIgnoreCase("UGC"))
				isUGC = true;
			String id = clip.getClipPromoId();
			String name = clip.getClipName();
			String movieName = clip.getAlbum();
			String clipInfo = clip.getClipInfo();
			if (movieName != null && movieName.length() > 20)
				movieName = movieName.substring(0, 20);
			if (name != null)
				name = name.replaceAll("'", "");
			if (clipInfo != null)
				clipInfo = clipInfo.replaceAll("=", ":");
			userInfo += "songname:" + name + "|songcode:" + id + "|" + clipInfo
					+ "|moviename:" + movieName;
		} else
			userInfo += "songname:|songcode:|moviename:";

		if (callerID == null)
			callerID = "all";
		if (isUGC)
			userInfo += "|cli:" + callerID + "|songtype:UGC";
		else
			userInfo += "|cli:" + callerID;

		if (getParamAsBoolean(iRBTConstant.DAEMON, "IS_TATA_GSM_IMPL", "FALSE")) {
			HashMap<String, String> extraInfoMap = rbtDBManager
					.getExtraInfoMap(rbtDBManager.getSubscriber(subscriberID));
			String wdsInfo = "";
			if (extraInfoMap != null
					&& extraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT))
				wdsInfo = extraInfoMap
						.get(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT);
			userInfo += "|cosid:" + cosID + "|"
					+ iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT + ":" + wdsInfo;
		}

		String contentID = "|CONTENT_ID:";
		contentID += "contentid=" + clip.getClipId() + ",catname="
				+ category.getCategoryName() + ",actinfo="
				+ actInfo.replaceAll("\\|", "/").replaceAll(":", ";")
				+ ",callerid=" + callerID + ",catid="
				+ category.getCategoryId();
		if (extraInfo.containsKey(iRBTConstant.EXTRA_INFO_COPY_TYPE)) {
			String copyType = extraInfo.get(iRBTConstant.EXTRA_INFO_COPY_TYPE);
			contentID += (copyType
					.equalsIgnoreCase(iRBTConstant.EXTRA_INFO_COPY_TYPE_OPTIN)) ? ",copy_type=optin"
					: "";
		}
		contentID += "|catname:" + category.getCategoryName() + "|cat_type:"
				+ category.getCategoryTpe();
		if (extraInfo.containsKey(iRBTConstant.EXTRA_INFO_IMEI_NO))
			contentID += "|imei_no:"
					+ extraInfo.get(iRBTConstant.EXTRA_INFO_IMEI_NO);
		return userInfo + contentID;
	}

	protected String getDeactivationUserInfo(String actInfo, String cosID) {
		String contentID = "|CONTENT_ID:";
		actInfo = actInfo.replaceAll("\\|", "/");
		actInfo = actInfo.replaceAll(":", ";");
		contentID = "actinfo=" + actInfo + "|cosid:" + cosID;
		return contentID;
	}

	@Override
	public String processUSSD(WebServiceContext task) {
		// ResourceBundle resourceBundle;
		String subID = task.getString(param_subscriberID);
		Subscriber subscriber = rbtDBManager.getSubscriber(subID);
		if (subscriber == null
				|| subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED)) {
			logger.info(NEW_USER);
			return processActivation(task);
		} else if (subscriber.subYes().equals(
				iRBTConstant.STATE_DEACTIVATION_PENDING)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_TO_BE_DEACTIVATED)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_DEACTIVATED_INIT)) {
			logger.info(DEACT_PENDING);
			return DEACT_PENDING;
		} else if (subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED)
				|| subscriber.subYes()
						.equals(iRBTConstant.STATE_SUSPENDED_INIT)) {
			logger.info(SUSPENDED);
			return SUSPENDED;
		} else {
			// resourceBundle = ResourceBundle.getBundle("ussdconfig");
			String responseText = getParamAsString("USSD", "USSD_MENU_LIST",
					null);
			// String responseText = resourceBundle.getString("USSD_MENU_LIST");
			logger.info(responseText);
			String activeClipIds = getParamAsString("COMMON",
					"ACTIVATED_CLIP_IDS", null);
			String[] clipIds = activeClipIds.split(",");
			int i = 1;
			String browsingLanguage = task.getString(param_browsingLanguage);
			for (String clipId : clipIds) {
				Clip clip = rbtCacheManager.getClip(clipId, browsingLanguage);
				String songName;
				if (clip != null) {
					songName = clip.getClipName();
					responseText = responseText.replaceAll("%SONG" + i,
							songName);
					responseText = responseText.replaceAll(
							"<%CLIPID" + i + ">", clipId);
					responseText = responseText.replaceAll("<msisdn>", subID);
					i++;
				}
			}
			logger.info(responseText);
			return responseText;
		}
	}

	protected boolean smConvertSubscription(WebServiceContext task,
			String subscriptionClass, String subscriberID,
			Subscriber subscriber, String activatedBy, String activationInfo,
			boolean isLimitedDownload) throws Exception {
		logger.info("Before Send SM Client Request");
		String offerId = getOfferID(task, BASE_OFFERTYPE);
		if (offerId.equals("-2")) {
			subscriptionClass = task.getString(param_subscriptionClass);
		}
		HashMap<String, String> subExtraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		String oldOfferId = subExtraInfoMap.get(param_offerID);
		if (oldOfferId == null) {
			oldOfferId = "-1";
		}
		if (isLimitedDownload) {
			if (activatedBy != null)
				subExtraInfoMap.put("PACK_MODE", activatedBy);
		}
		subExtraInfoMap.put(param_offerID, offerId);
		subExtraInfoMap.put(param_old_offerid, oldOfferId);
		boolean success = rbtDBManager.smConvertSubscriptionType(subscriberID,
				subscriber.subscriptionClass(), subscriptionClass, activatedBy,
				activationInfo, 0, false, subExtraInfoMap, subscriber);
		logger.info("RBT Made the entry with Subscriber table");
		StringBuilder builder = new StringBuilder(
				"SM client request for upgrade subscription ");

		HashMap<String, String> extraParams = new HashMap<String, String>();
		extraParams.put(
				RBTSMClientHandler.EXTRA_PARAM_USERINFO,
				getActivationUserInfo(subscriber.activationInfo(),
						subscriber.cosID(), subExtraInfoMap));

		builder.append("[ subID " + subscriberID);
		builder.append(", prepaidYes " + subscriber.prepaidYes());
		builder.append(", Old subClass " + subscriber.subscriptionClass());
		builder.append(", subClass - " + subscriptionClass);
		builder.append(", offerID - " + offerId);
		builder.append(", Old offerID - " + oldOfferId);
		builder.append(", activatedBy - " + activatedBy);
		builder.append(", extraParams - " + extraParams);
		logger.info(builder.toString());
		RBTSMClientResponse smClientResponse = RBTSMClientHandler
				.getInstance()
				.upgradeSubscription(subscriberID, subscriber.prepaidYes(),
						activatedBy, subscriber.subscriptionClass(),
						subscriptionClass, offerId, oldOfferId, subExtraInfoMap);
		success = smClientResponse.getResponse().equalsIgnoreCase(
				RBTSMClientResponse.SUCCESS);
		logger.info("SMClient Response for Upgrade Supscription : "
				+ smClientResponse.toString());
		if (!success) {
			subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber
					.extraInfo());
			rbtDBManager.updateSubscriber(subscriberID, activatedBy, null,
					null, null, null, null, null, null, activationInfo,
					subscriber.subscriptionClass(), subscriber.subYes(), null,
					null, null, null, null, null, subscriber.oldClassType(),
					null, null, null, subExtraInfoMap);
		}
		return success;
	}

	protected Subscriber smActivateSubscriber(WebServiceContext task,
			HashMap<String, String> userInfoMap, Subscriber subscriber,
			String subscriberID, String activatedBy, Date startDate,
			Date endtDate, boolean isPrepaid, int activationPeriod,
			int freePeriod, String activationInfo, String subscriptionClass,
			boolean useSubManager, CosDetails cos, boolean isDirectActivation,
			int rbtType, String circleID) throws Exception {
		String offerID = "-1";
		if (task.getString(param_action).equalsIgnoreCase(action_acceptGift)) {
			offerID = "-2";
		} else {
			subscriptionClass = task.getString(param_subscriptionClass);
			if (task.containsKey(param_requestFromSelection)) {
				offerID = getOfferID(task, COMBO_SUB_OFFERTYPE);
			} else {
				offerID = getOfferID(task, SELECTION_OFFERTYPE);
			}
			if (offerID.equals("-2")) {
				subscriptionClass = task.getString(param_subscriptionClass);
			}
		}

		userInfoMap.put(param_offerID, offerID);
		subscriber = rbtDBManager.smActivateSubscriber(subscriberID,
				activatedBy, startDate, endtDate, isPrepaid, activationPeriod,
				freePeriod, activationInfo, subscriptionClass, useSubManager,
				cos, isDirectActivation, rbtType, userInfoMap, circleID);
		if (subscriber != null
				&& (isDirectActivation || subscriber.subYes().equals(
						iRBTConstant.STATE_ACTIVATION_PENDING))) {
			task.put(param_newUser, "true");
			if (!task.containsKey(param_requestFromSelection)) {
				// invoke smclient request for activate subscriber
				boolean isBulkTask = task.containsKey(param_fromBulkTask);

				HashMap<String, String> extraParams = new HashMap<String, String>();
				String cosID = "";
				if (cos != null)
					cosID = cos.getCosId();
				extraParams.put(
						RBTSMClientHandler.EXTRA_PARAM_USERINFO,
						getActivationUserInfo(activationInfo, cosID,
								userInfoMap));

				StringBuilder builder = new StringBuilder(
						"SMClient Request for ActivateSubscriber ");
				builder.append("[ subID " + subscriberID);
				builder.append(", prepaidYes " + isPrepaid);
				builder.append(", subClass - " + subscriptionClass);
				builder.append(", offerID - " + offerID);
				builder.append(", activatedBy - " + activatedBy);
				builder.append(", extraParams - " + extraParams);
				builder.append(", isBulkTask - " + isBulkTask);
				logger.info(builder.toString());
				RBTSMClientResponse smClientResponse = RBTSMClientHandler
						.getInstance().activateSubscriber(subscriberID,
								isPrepaid, subscriptionClass, offerID,
								activatedBy, extraParams, isBulkTask);
				logger.info("SMClient Response for ActivateSubscriber"
						+ smClientResponse.toString());
				if (!smClientResponse.getResponse().equalsIgnoreCase(
						RBTSMClientResponse.SUCCESS)) {
					// remove the subscriber
					rbtDBManager.removeSubscriber(subscriber);
					subscriber = null;
				}
			}
		}
		return subscriber;
	}

	protected String smAddSubScriberSelection(
			HashMap<String, String> selectionInfoMap, String selOfferID,
			String classType, WebServiceContext task, String selResponse,
			String subscriberID, String callerID, Categories categoriesObj,
			Date startDate, Date endDate, int status, String selectedBy,
			String selectionInfo, boolean isPrepaid, String messagePath,
			int fromTime, int toTime, boolean useSubManager,
			String chargingPackage, String subYes, String circleID,
			String transID, Subscriber subscriber, String interval,
			String baseOfferID, int i, boolean changeSubType, boolean inLoop,
			HashMap<String, Object> clipMap, Clip clip, Category category)
			throws Exception {

		HashMap<String, String> responseParams = new HashMap<String, String>();
		selectionInfoMap.put(param_offerID, selOfferID);
		if (task.getString(param_action).equalsIgnoreCase(action_acceptGift)) {
			selOfferID = "-2";
		} else if (selOfferID.equals("-2")) {
			classType = task.getString(param_chargeClass);
		} else {
			classType = task.getString(param_chargeClass);
		}
		selResponse = rbtDBManager.smAddSubscriberSelections(subscriberID,
				callerID, categoriesObj, clipMap, null, startDate, endDate,
				status, selectedBy, selectionInfo, 0, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, classType, useSubManager, true,
				"VUI", chargingPackage, subYes, null, circleID, true, false,
				transID, false, false, inLoop, subscriber.subscriptionClass(),
				subscriber, 0, interval, selectionInfoMap, responseParams);

		if (selResponse.equals(iRBTConstant.SELECTION_SUCCESS)) {
			HashMap<String, String> extraParams = getSelectionExtraParams(
					subscriber, clip, category, callerID, selectionInfo,
					selectionInfoMap);
			String selectionRefID = "";
			if (responseParams.containsKey("REF_ID")) {
				selectionRefID = responseParams.get("REF_ID");
			}
			smClientRquestForSelection(task, subscriberID, subscriber,
					classType, baseOfferID, selOfferID, selectedBy,
					selectionRefID, isPrepaid, -1, extraParams);
		}
		return selResponse;
	}

	// Gets the details of all the msisdns of the given campaign/taskID
	@Override
	public String processBulkGetCorporateDetails(WebServiceContext task) {
		String response = ERROR;

		StringBuilder responseBuilder = new StringBuilder();
		try {
			int taskId = Integer.parseInt(task.getString(param_taskID));
			RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO
					.getRBTBulkUploadTask(taskId);

			if (rbtBulkUploadTask == null
					|| !rbtBulkUploadTask.getTaskType().equals(
							BULKACTION_CORPORATE)) {
				logger.info("Task doesn't exists");
				responseBuilder.append(System.getProperty("line.separator"));
				responseBuilder.append(taskId).append(" doesn't exists ");
			} else {
				// HEADER of the output file
				responseBuilder
						.append("CAMPAIGN START DATE,CAMPAIGN END DATE,MSISDN,STATUS");
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String campStartDate = dateFormat.format(rbtBulkUploadTask
						.getProcessTime());
				String campEndDate = dateFormat.format(rbtBulkUploadTask
						.getEndTime());

				List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
						.getRBTBulkUploadSubscribers(Integer.valueOf(taskId));
				for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
					String subscriberId = rbtBulkUploadSubscriber
							.getSubscriberId();
					Subscriber subscriber = rbtDBManager
							.getSubscriber(subscriberId);
					String status = Utility.getSubscriberStatus(subscriber);

					if (Utility.isUserActive(status)) {
						SubscriberStatus[] selections = rbtDBManager
								.getAllActiveSubscriberSettings(subscriberId);

						boolean isCorporateUser = false;
						if (selections != null) {
							for (SubscriberStatus selection : selections) {
								if (selection.selType() == 2) {
									isCorporateUser = true;
									break;
								}
							}
						} else
							status = "Not a corporate subscriber";

						if (!isCorporateUser)
							status = "Not a corporate subscriber";
					}

					responseBuilder
							.append(System.getProperty("line.separator"));
					responseBuilder.append(campStartDate).append(",");
					responseBuilder.append(campEndDate).append(",");
					responseBuilder.append(subscriberId).append(",");
					responseBuilder.append(status);
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String tempPath = System.getProperty("java.io.tmpdir");
				if (!tempPath.endsWith(File.separator)) {
					tempPath += File.separator;
				}
				String resultFileName = tempPath + "CorporateDetails_"
						+ task.getString(param_taskID) + ".csv";
				File resultFile = new File((String) null, resultFileName);
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());
				logger.info("Result File path: " + resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write(responseBuilder.toString());
				bufferedWriter.newLine();
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * Gets the status of all the msisdns of the given taskID
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processBulkGetTaskDetails
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 * 
	 * @author laxmankumar
	 */
	@Override
	public String processBulkGetTaskDetails(WebServiceContext task) {

		String response = ERROR;

		StringBuilder responseBuilder = new StringBuilder();
		try {
			int taskId = Integer.parseInt(task.getString(param_taskID));
			RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO
					.getRBTBulkUploadTask(taskId);

			if (rbtBulkUploadTask == null) {
				logger.info("Task doesn't exists");
				responseBuilder.append(System.getProperty("line.separator"));
				responseBuilder.append(taskId).append(" doesn't exists ");
			} else {
				// HEADER of the output file
				responseBuilder.append("MSISDN,STATUS,REASON");

				List<RBTBulkUploadSubscriber> bulkSubscriberList = RBTBulkUploadSubscriberDAO
						.getRBTBulkUploadSubscribers(Integer.valueOf(taskId));
				logger.info("no. of subscribers " + bulkSubscriberList.size());
				for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : bulkSubscriberList) {
					String subscriberId = rbtBulkUploadSubscriber
							.getSubscriberId();
					int status = rbtBulkUploadSubscriber.getStatus();
					String reason = rbtBulkUploadSubscriber.getReason();
					responseBuilder
							.append(System.getProperty("line.separator"));
					responseBuilder.append(subscriberId).append(",");
					responseBuilder.append(status).append(",");
					responseBuilder.append(reason);
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			BufferedWriter bufferedWriter = null;
			try {
				String tempPath = System.getProperty("java.io.tmpdir");
				if (!tempPath.endsWith(File.separator)) {
					tempPath += File.separator;
				}
				String resultFileName = tempPath + "TaskDetails_"
						+ task.getString(param_taskID) + ".csv";
				File resultFile = new File((String) null, resultFileName);
				task.put(param_bulkTaskResultFile, resultFile.getAbsolutePath());
				logger.info("Result File path: " + resultFile.getAbsolutePath());

				FileWriter fileWriter = new FileWriter(resultFile);
				bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(responseBuilder.toString());
				bufferedWriter.newLine();
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}

		logger.info("response: " + response);
		return response;
	}

	/*
	 * added by sridhar.sindiri
	 * 
	 * @Announcement activation and deactivation requests for a single
	 * subscriber and for bulk subscribers
	 */
	// Processes the announcement activation request for both RRBT and Non-RRBT
	// Users
	@Override
	public String processAnnouncementActivation(WebServiceContext task) {
		String response = ERROR;

		String subscriberID = task.getString(param_subscriberID);
		try {
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID,
					true);
			task.put(param_subscriber, subscriber);

			response = isValidUser(task, subscriber);
			// allowing suspended users also to get activated for announcements
			if (!response.equals(VALID) && !response.equals(SUSPENDED)) {
				logger.info("response: " + response);
				return response;
			}
			subscriberID = rbtDBManager.subID(subscriberID);
			task.put(param_subscriberID, subscriberID);
		} catch (RBTException e) {
			e.printStackTrace();
		}

		if (!getParamAsBoolean(iRBTConstant.COMMON,
				iRBTConstant.PROCESS_ANNOUNCEMENTS, "FALSE"))
			return ANNOUNCEMENTS_NOT_SUPPORTED;

		int clipID = 0;
		if (task.containsKey(param_clipID)
				&& task.getString(param_clipID) != null)
			clipID = Integer.parseInt(task.getString(param_clipID));
		else
			return INVALID_PARAMETER;

		String frequency = task.getString(param_frequency);

		Date endDate = null;
		if (task.containsKey(param_subscriberEndDate)) {
			try {
				DateFormat dateFormatter = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				endDate = dateFormatter.parse(task
						.getString(param_subscriberEndDate));
			} catch (ParseException e) {
				logger.error(
						"subscriberEndDate should be in date format like yyyyMMddHHmmssSSS",
						e);
			}
		}
		if (endDate == null) {
			Calendar endCal = Calendar.getInstance();
			endCal.set(2037, 0, 1, 0, 0, 0);
			endDate = endCal.getTime();
		}

		SubscriberAnnouncements subAnnouncement = rbtDBManager
				.activateAnnouncement(task.getString(param_subscriberID),
						clipID, new Date(), endDate, null, frequency);

		if (subAnnouncement != null
				&& subAnnouncement.status() == iRBTConstant.ANNOUNCEMENT_TO_BE_ACTIVED)
			response = SUCCESS;
		else
			response = FAILED;

		logger.info("response: " + response);
		return response;
	}

	// Processes the announcement deactivation request for both RRBT and
	// Non-RRBT Users
	@Override
	public String processAnnouncementDeactivation(WebServiceContext task) {
		String response = ERROR;

		String subscriberID = task.getString(param_subscriberID);
		try {
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID,
					true);
			task.put(param_subscriber, subscriber);

			response = isValidUser(task, subscriber);
			// allowing suspended users also to get activated for announcements
			if (!response.equals(VALID) && !response.equals(SUSPENDED)) {
				logger.info("response: " + response);
				return response;
			}
			subscriberID = rbtDBManager.subID(subscriberID);
			task.put(param_subscriberID, subscriberID);
		} catch (RBTException e) {
			e.printStackTrace();
		}

		if (!getParamAsBoolean(iRBTConstant.COMMON,
				iRBTConstant.PROCESS_ANNOUNCEMENTS, "FALSE"))
			return ANNOUNCEMENTS_NOT_SUPPORTED;

		boolean isDeactivated = false;
		if (task.containsKey(param_clipID)
				&& task.getString(param_clipID) != null) {
			int clipID = Integer.parseInt(task.getString(param_clipID));
			isDeactivated = rbtDBManager.deactivateAnnouncement(
					task.getString(param_subscriberID), clipID);
		} else
			isDeactivated = rbtDBManager.deactivateAnnouncements(task
					.getString(param_subscriberID));

		if (isDeactivated)
			response = SUCCESS;
		else
			response = FAILED;

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processBulkAnnouncementActivation(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;
		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				tempTask.put(param_fromBulkTask, "true");
				String actResponse = processAnnouncementActivation(tempTask);
				if (actResponse.equals(SUCCESS)) {
					success.append("Announcement Activated for ").append(
							subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Announcement Activation failed for ");
					failure.append(subscriberID).append("(")
							.append(actResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter
							.write("Bulk Announcement Activation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("---------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}
		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processBulkAnnouncementDeactivation(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);

				tempTask.put(param_fromBulkTask, "true");
				String deactResponse = processAnnouncementDeactivation(tempTask);
				if (deactResponse.equals(SUCCESS)) {
					success.append("Announcement Deactivated for ").append(
							subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Announcement Deactivation failed for ");
					failure.append(subscriberID).append("(")
							.append(deactResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter
							.write("Bulk Announcement Deactivation Statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}
		logger.info("response: " + response);
		return response;
	}

	@Override
	public String processBulkUpdateSubscription(WebServiceContext task) {
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;

		StringBuilder success = new StringBuilder();
		StringBuilder failure = new StringBuilder();

		int successCount = 0;
		int failureCount = 0;

		try {
			String filePath = task.getString(param_bulkTaskFile);

			file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			// Removing circleID from task to support different circle's numbers
			// in single file for central servers.
			task.remove(param_circleID);

			WebServiceContext tempTask = new WebServiceContext();

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line;

				tempTask.clear();
				tempTask.putAll(task);

				tempTask.put(param_subscriberID, subscriberID);
				tempTask.put(param_fromBulkTask, "true");

				String updateResponse = updateSubscription(tempTask);
				if (updateResponse.equalsIgnoreCase(SUCCESS)) {
					success.append("Updated validity successfully for ")
							.append(subscriberID);
					success.append(System.getProperty("line.separator"));
					successCount++;
				} else {
					failure.append("Updating validity failed for ");
					failure.append(subscriberID).append("(")
							.append(updateResponse).append(")");
					failure.append(System.getProperty("line.separator"));
					failureCount++;
				}
			}
			response = SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		} finally {
			if (bufferedReader != null) {
				BufferedWriter bufferedWriter = null;
				try {
					bufferedReader.close();

					String resultFileName = file.getName().substring(0,
							file.getName().indexOf(".txt"))
							+ "_result.txt";
					File resultFile = new File(file.getParentFile(),
							resultFileName);
					task.put(param_bulkTaskResultFile,
							resultFile.getAbsolutePath());

					FileWriter fileWriter = new FileWriter(resultFile);
					bufferedWriter = new BufferedWriter(fileWriter);

					bufferedWriter.write("Bulk Validity Updation statistics");
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.write("Total: "
							+ (successCount + failureCount));
					bufferedWriter.newLine();
					bufferedWriter.write("Success: " + successCount);
					bufferedWriter.newLine();
					bufferedWriter.write("Failure: " + failureCount);
					bufferedWriter.newLine();
					bufferedWriter.write("-----------------------------");
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Success Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(success.toString());
					bufferedWriter.newLine();
					bufferedWriter.newLine();
					bufferedWriter.write("Failure Result");
					bufferedWriter.newLine();
					bufferedWriter.write("---------------");
					bufferedWriter.newLine();
					bufferedWriter.write(failure.toString());

					file.delete();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}
		logger.info("response: " + response);
		return response;
	}

	/**
	 * Removes the leading string from PromoID/clipID, if it starts with
	 * configured leading string
	 * 
	 * @param task
	 * 
	 */
	private void removeLeadingString(WebServiceContext task) {
		if (!task.containsKey(param_clipID))
			return;

		String clipID = task.getString(param_clipID);
		Parameters parameters = CacheManagerUtil.getParametersCacheManager()
				.getParameter(iRBTConstant.SMS, "LEADING_STRING");
		if (parameters != null && clipID != null) {
			List<String> list = Arrays.asList(parameters.getValue().split(","));
			for (String leadingString : list) {
				if (clipID.startsWith(leadingString)) {
					clipID = clipID.substring(leadingString.length());
					break;
				}
			}
		}
		logger.info(clipID);
		task.put(param_clipID, clipID);
	}

	private boolean isValidSNGRequest(WebServiceContext task,
			boolean allInOndeUpdateModel) {
		String subscriberID = task.getString(param_subscriberID);
		String mode = task.getString(param_mode);
		String sngId = task.getString(param_sngId);
		String userID = task.getString(param_userId);
		logger.info("subscriberID=" + subscriberID + ",mode=" + mode
				+ ",sngId=" + sngId + ",userID" + userID);
		boolean returnFlag = true;

		if (allInOndeUpdateModel) {
			if (!(mode != null && !mode.equalsIgnoreCase("null") && !mode
					.trim().equalsIgnoreCase(""))) {
				returnFlag = false;
			}
			if (mode != null && mode.indexOf("social_") == -1) {
				returnFlag = false;
			}
		} else {
			if (!(sngId != null && !sngId.equalsIgnoreCase("null") && !sngId
					.trim().equalsIgnoreCase(""))) {
				return false;
			}
		}
		if (!(userID != null && !userID.equalsIgnoreCase("null") && !userID
				.trim().equalsIgnoreCase(""))) {
			returnFlag = false;
		}
		if (!(subscriberID != null && !subscriberID.equalsIgnoreCase("null") && !subscriberID
				.trim().equalsIgnoreCase(""))) {
			returnFlag = false;
		}
		logger.info("returning value =" + returnFlag);
		return returnFlag;
	}

	protected boolean isSupportSMClientModel(WebServiceContext task,
			int offerType) {
		boolean result = false;
		if (getParamAsBoolean(iRBTConstant.COMMON,
				iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE")
				|| isSupportOfferId(task, offerType)) {
			result = true;
		}
		return result;
	}

	protected boolean isSupportOfferId(WebServiceContext task, int offerType) {
		boolean result = false;
		if (getParamAsBoolean(iRBTConstant.COMMON,
				iRBTConstant.SUPPORT_SMCLIENT_FOR_OFFERID, "FALSE")) {
			if ((offerType == BASE_OFFERTYPE || offerType == SELECTION_OFFERTYPE)
					&& task.containsKey(param_offerID))
				result = true;
			else if (offerType == COMBO_SUB_OFFERTYPE
					&& task.containsKey(param_subscriptionOfferID))
				result = true;
			else if (offerType == GIFT_OFFERTYPE
					&& task.containsKey(param_subscriptionOfferID)
					&& task.containsKey(param_offerID))
				result = true;
		}
		return result;
	}

	@Override
	public String processChangeMsisdn(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String newSubscriberID = task.getString(param_newSubscriberID);

		if (subscriberID == null || newSubscriberID == null)
			return "FAILURE:MISSING PARAMETER";

		String updateResponse = rbtDBManager.updateSubscriberId(
				newSubscriberID.trim(), subscriberID.trim());
		if (updateResponse == null)
			return ERROR;

		if (updateResponse.equalsIgnoreCase("SUCCESS")) {
			response = SUCCESS;
			boolean m_socialRBTAllowed = getParamAsBoolean("COMMON",
					"SOCIAL_RBT_ALLOWED", "false");
			boolean m_socialRBTAllUpdateInOneModel = getParamAsBoolean("SRBT",
					"ALL_IN_ONE_UPDATE_MODEL", "false");
			SRBTUtility.updateSocialSubscriberForChangeMsisdnSuccess(
					m_socialRBTAllowed, m_socialRBTAllUpdateInOneModel,
					subscriberID, newSubscriberID, task.getString(param_mode),
					iRBTConstant.evtTypeForChangeMsidn);

		} else
			response = updateResponse;

		logger.info("response: " + response);
		return response;
	}


	@Override
	public String processDeleteConsentRecords(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String agentName = task.getString(param_agentId);
		String extraInfo = task.getString(param_extraInfo);
		if (agentName != null) {
			boolean deleted = rbtDBManager.deleteConsentRecordByAgentID(
					subscriberID, agentName);
			if (deleted) {
				response = SUCCESS;
			}
		} else {
			logger.info("In basic rbt processor" + response);
			boolean deleted = rbtDBManager.createNewNameTuneRequest(
					subscriberID, extraInfo);
			if (deleted) {
				response = SUCCESS;
			}
		}
		logger.info("response: " + response);
		return response;
	}

	
	@Override
	public String processSendChangeMsisdnRequestToSM(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String newSubscriberID = task.getString(param_newSubscriberID);

		if (subscriberID == null || newSubscriberID == null)
			return "FAILURE:MISSING PARAMETER";

		Subscriber newSubscriber = rbtDBManager.getSubscriber(newSubscriberID
				.trim());
		if (newSubscriber != null) {
			return "FAILURE:NEW MSISDN ALREADY EXISTS";
		}

		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID.trim());
		task.put(param_subscriber, subscriber);
		if (subscriber == null) {
			return "FAILURE:MSISDN DOESN'T EXIST";
		}

		if (!rbtDBManager.isValidPrefix(newSubscriberID)) {
			return "FAILURE:NEW MSISDN INVALID";
		}

		response = Utility.sendChangeMsisdnRequestToSubMgr(task);

		logger.info("response: " + response);
		return response;
	}

	@Override
	public String removeBulkTask(WebServiceContext task) {
		String response = ERROR;
		try {
			int taskID = Integer.parseInt(task.getString(param_taskID));
			RBTBulkUploadTask rbtBulkUploadTask = RBTBulkUploadTaskDAO
					.getRBTBulkUploadTask(taskID);

			if (rbtBulkUploadTask == null) {
				logger.info("Task doesn't exists");
				return TASK_DOES_NOT_EXIST;
			}

			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_REMOVED) {
				logger.info("Task already deleted. Task Status : "
						+ rbtBulkUploadTask.getTaskStatus());
				return TASK_ALREADY_REMOVED;
			}

			if (rbtBulkUploadTask.getTaskStatus() != BULKTASK_STATUS_SUCCESS
					&& rbtBulkUploadTask.getTaskStatus() != BULKTASK_STATUS_NEW) {
				logger.info("Task cannot be deleted. Task Status : "
						+ rbtBulkUploadTask.getTaskStatus());
				return TASK_DELETION_NOT_ALLOWED;
			}

			if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_SUCCESS) // Live
																				// compaign
			{
				rbtBulkUploadTask.setTaskStatus(BULKTASK_STATUS_REMOVED);
				boolean taskUpdated = RBTBulkUploadTaskDAO
						.updateRBTBulkUploadTask(rbtBulkUploadTask);
				if (taskUpdated)
					response = SUCCESS;
				else
					response = FAILED;

			} else if (rbtBulkUploadTask.getTaskStatus() == BULKTASK_STATUS_NEW) // New
																					// compaign
			{
				boolean taskDeleted = RBTBulkUploadTaskDAO
						.deleteTaskByTaskID(taskID);
				if (taskDeleted) {
					RBTBulkUploadSubscriberDAO.deleteSubscriberByTaskID(taskID);
					response = SUCCESS;
				} else
					response = FAILED;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}
		logger.info("Response : " + response);
		return response;
	}

	public String processUpgradeAllDownloads(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			if (subscriberID == null) {
				logger.info("subscriberID not passed");
				return INVALID_PARAMETER;
			}

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber != null
					&& subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
				SubscriberDownloads[] downloads = rbtDBManager
						.getActiveSubscriberDownloads(subscriberID);
				if (downloads == null) {
					logger.info("No selections for the subscriber");
					return NO_DOWNLOADS;
				}
				String newClassType = webServiceContext
						.getString(param_chargeClass);
				if (newClassType == null) {
					newClassType = webServiceContext
							.getString(param_chargeclass);
				}
				Map<String, String> upgradeChargeClassMap = new HashMap<String, String>();
				if (newClassType == null) {
					String chargeClassMapStr = RBTParametersUtils
							.getParamAsString(iRBTConstant.COMMON,
									"UPGRADE_CHARGE_CLASS_MAP", null);
					if (chargeClassMapStr != null) {
						String[] mappings = chargeClassMapStr.split(",");
						for (String eachMapping : mappings) {
							String[] tokens = eachMapping.split(":");
							upgradeChargeClassMap.put(tokens[0].trim(),
									tokens[1].trim());
						}
					}
				}
				boolean isDownloadPresent = false;
				for (SubscriberDownloads subDownloads : downloads) {
					if (subDownloads != null
							&& subDownloads.downloadStatus() == 'y') {
						isDownloadPresent = true;
						newClassType = newClassType == null ? upgradeChargeClassMap
								.get(subDownloads.classType()) : newClassType;
						if (newClassType != null) {
							newClassType = newClassType.replaceAll("RBT_SEL_",
									"");
						}
						String refID = subDownloads.refID();
						String extraInfo = subDownloads.extraInfo();
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						if (extraInfoMap == null) {
							extraInfoMap = new HashMap<String, String>();
						}
						if (webServiceContext.containsKey(param_bIOffer)) {
							// HashMap<String,String> extraInfoMap =
							// DBUtility.getAttributeMapFromXML(extraInfo);
							// if(extraInfoMap == null)
							// extraInfoMap = new HashMap<String,String>();
							extraInfoMap.put("BI_OFFER", "TRUE");
							extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);
						}
						extraInfoMap.put("OLD_CLASS_TYPE",
								subDownloads.classType());
						extraInfo = DBUtility
								.getAttributeXMLFromMap(extraInfoMap);
						if (newClassType != null) {
							rbtDBManager.updateDownloads(subscriberID, refID,
									'c', extraInfo, newClassType);
						}
					}
				}
				response = SUCCESS;
				if (!isDownloadPresent) {
					return NO_DOWNLOADS_TO_UPGRADE;
				}
			} else {
				response = USER_NOT_ACTIVE;
			}

		} catch (Exception ex) {
			logger.info("Exception While Upgrading Selection:" + ex);
		}
		return response;
	}

	@Override
	public String processUpgradeAllSelections(
			WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			if (subscriberID == null) {
				logger.info("subscriberID not passed");
				return INVALID_PARAMETER;
			}

			Clip clip = null;
			if (webServiceContext.containsKey(param_clipID)) {
				String clipID = webServiceContext.getString(param_clipID);
				clip = rbtCacheManager.getClip(clipID);
				if (clip == null) {
					logger.info("Clip not exists");
					return INVALID_PARAMETER;
				}
			}

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber != null
					&& subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
				SubscriberStatus[] settings = rbtDBManager
						.getAllActiveSubscriberSettings(subscriberID);
				if (settings == null) {
					logger.info("No selections for the subscriber");
					return NO_SELECTIONS;
				}

				response = SUCCESS;
				boolean isUpgradableSelPresent = false;
				for (SubscriberStatus setting : settings) {
					if (clip != null
							&& !setting.subscriberFile().equalsIgnoreCase(
									clip.getClipRbtWavFile()))
						continue;

					Map<String, String> upgradeChargeClassMap = new HashMap<String, String>();
					String chargeClassMapStr = RBTParametersUtils
							.getParamAsString(iRBTConstant.COMMON,
									"UPGRADE_CHARGE_CLASS_MAP", null);
					if (chargeClassMapStr != null) {
						String[] mappings = chargeClassMapStr.split(",");
						for (String eachMapping : mappings) {
							String[] tokens = eachMapping.split(":");
							upgradeChargeClassMap.put(tokens[0].trim(),
									tokens[1].trim());
						}
					}

					String newClassType = upgradeChargeClassMap.get(setting
							.classType());
					if (newClassType != null) {
						isUpgradableSelPresent = true;

						webServiceContext.put(param_info, UPGRADE_SEL_PACK);
						webServiceContext.put(param_refID, setting.refID());
						webServiceContext.put(param_initClassType,
								setting.classType());
						webServiceContext.put(param_subscriptionClass,
								newClassType);

						String upgradeResponse = updateSelection(webServiceContext);

						if (!upgradeResponse.equalsIgnoreCase(SUCCESS))
							response = FAILED;
					}
				}

				if (!isUpgradableSelPresent)
					response = NO_SELECTIONS_TO_UPGRADE;
			} else {
				response = USER_NOT_ACTIVE;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		logger.info("Upgrade response : " + response);
		return response;
	}

	
	@Override
	public String subscribeUser(WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);
		String reqType = task.getString(param_type);
		String password = task.getString(param_info);
		Boolean isResetPassword = Boolean.parseBoolean(task.getString(param_isResetPassword));
		String uid = task.getString(param_userInfo+"_uid");
		String appName = task.getString(param_userInfo + "_" + param_appName);
		
		logger.info("subscriberID: " + subscriberID + ", reqType: " + reqType
				+ ", password: " + password + ", isResetPassword: "
				+ isResetPassword + ", uid: " + uid + ", appName: "
				+ appName);
		Subscriber subscriber = null;
		try {
			subscriber = DataUtils.getSubscriber(task);
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		}

		String response = isValidUser(task, subscriber);
		if (!response.equals(VALID)) {
			logger.info("Response: " + response);
			return response;
		}

		String type = "MOBILECLIENT";
		if (appName != null && !appName.isEmpty()) {
			type = Utility.getMobileClientTypeWithAppName(appName);
			logger.debug("type: " + type);
		}
		boolean encryptPassword = task.containsKey(param_encryptPassword)
				&& task.getString(param_encryptPassword).equalsIgnoreCase(YES);
		String senderID = getParamAsString("MOBILEAPP",
				"MOBILECLIENT_SMS_SENDER_ID", "111");

		HashMap<String, String> userInfo = new HashMap<String, String>();

		RBTLoginUser rbtLoginUser = rbtDBManager.getRBTLoginUser(subscriberID,
				password, subscriberID, type, null, encryptPassword);
		logger.info("rbtLoginUser: " + rbtLoginUser);
		int otpLength = 6;
		
		try {
			otpLength = Integer.parseInt(getParamAsString("COMMON","OTP_LENGTH", "6"));
		}
		catch(Exception e) {}
		
		
		if (rbtLoginUser == null) {
			logger.info("no entry found for user (i.e. a new user). subscriberId: " + subscriberID);
			String pass = null;
			if (getParamAsBoolean("MOBILEAPP", "NUMERIC_OTP_HC", "false")) {
				Random rand = new Random();
				double a = Math.pow(10, otpLength);
				pass = String.format("%0" + otpLength + "d",rand.nextInt((int) Math.round(a)));
			} else {
				pass = UUID.randomUUID().toString().substring(0, otpLength);
			}
			if (reqType.equalsIgnoreCase("login")) {
				logger.error("RBTLoginUser entry missing and a login request receieved. Returning FAILURE.");
				logger.info("Response: " + FAILURE);
				return FAILURE;
			} else if (reqType.equalsIgnoreCase("unregister")) {
				logger.error("RBTLoginUser entry missing and a unregister request receieved. Returning FAILURE.");
				logger.info("Response: " + FAILURE);
				return FAILURE;
			} else if (rbtDBManager.addRBTLoginUser(subscriberID, pass,
					subscriberID, type, userInfo, encryptPassword) != null) {
				String loginSmsText = getParamAsString("MOBILEAPP",
						"MOBILECLIENT_REGISTRATION_SMS", "Your password is %P");
				loginSmsText = loginSmsText.replaceAll("%P", pass);
				logger.info("Sending sms to " + subscriberID + ". SmsText: " + loginSmsText);
				Tools.sendSMS(senderID, subscriberID, loginSmsText);
				updatePlayerStatusIfActiveSubscriber(subscriberID, appName,
						subscriber);
				logger.info("Response: " + SUCCESS);
				return SUCCESS;
			} else {
				logger.error("Insertion in RBTLoginUser Table failed. Returning FAILURE. userID: " + subscriberID + "subscriberId: " + subscriberID);
				logger.info("Response: " + FAILURE);
				return FAILURE;
			}
		} else {
			if (reqType.equalsIgnoreCase("login")) {
				logger.info("Response: " + SUCCESS);
				if (uid != null) {
					if (rbtDBManager.updateRBTLoginUser(rbtLoginUser.userID(),
							uid, null, null, null, rbtLoginUser.userInfo(),
							rbtLoginUser.userInfo(), encryptPassword, null,
							password)) {
						logger.info("UID successfully updated. subscriberId: " + subscriberID + ", UID: " + uid);
						try {//Changes are done for handling the voldemort issues.
							MemcacheClientForCurrentPlayingSong.getInstance()
									.checkCacheInitialized();
							boolean isCallLogMemCacheIsUp = MemcacheClientForCurrentPlayingSong
									.getInstance().isCacheAlive();
							if (isCallLogMemCacheIsUp) {
								boolean isAdded = mc.add(rbtLoginUser.userID(),
										true);
								logger.info("Login user successfully added into memcache. subscriberId: "
										+ subscriberID
										+ ", UID: "
										+ uid
										+ " ,isAdded: " + isAdded);
							}
						} catch (Exception e) {
							logger.error("Exception occured while putting the data into the memcache for Login user data");
						}
					} else {
						logger.error("Updation failed! subscriberId: " + subscriberID + ", UID: " + uid);
						return FAILURE;
					}
				}
				return SUCCESS;
			} else if (reqType.equalsIgnoreCase("register")) {
				if (getParamAsBoolean("MOBILEAPP",
						"SNED_ALREADY_REGISTERED_SMS", "FALSE") || getParamAsBoolean("MOBILEAPP",
								"SEND_ALREADY_REGISTERED_SMS", "FALSE")) {
					String pass = null;
					if (isResetPassword) {
						logger.debug("Password to be reset. subscriberId: " + subscriberID);						
						if (getParamAsBoolean("MOBILEAPP", "NUMERIC_OTP_HC", "false")) {
							Random rand = new Random();
							double a = Math.pow(10, otpLength);
							pass = String.format("%0" + otpLength + "d",rand.nextInt((int) Math.round(a)));
						} else {
							pass = UUID.randomUUID().toString().substring(0, otpLength);
						}
						if (rbtDBManager.updateRBTLoginUser(rbtLoginUser.userID(),
								null, pass, null, null, rbtLoginUser.userInfo(),
								rbtLoginUser.userInfo(), encryptPassword, null,
								null)) {
							logger.info("Password successfully reset. subscriberId: " + subscriberID);
						} else {
							logger.error("Updation of RBTLoginUser Table failed. userID: " + rbtLoginUser.userID() + "subscriberId: " + rbtLoginUser.subscriberID());
							logger.info("Response: " + ERROR);
							return FAILURE;
						}
					} else {
						logger.debug("Password not to be reset. Using the existing password. subscriberId: " + subscriberID);
						pass = rbtLoginUser.password();
					}	
					String loginSmsText = getParamAsString("MOBILEAPP",
							"MOBILECLIENT_ALREADY_REGISTERED_SMS",
							"Your password is %P");
					loginSmsText = loginSmsText.replaceAll("%P",
							pass);
					logger.info("Sending sms to " + subscriberID + ". SmsText: " + loginSmsText);
					Tools.sendSMS(senderID, subscriberID, loginSmsText);
				} 
				logger.info("Response: " + ALREADY_EXISTS);
				return ALREADY_EXISTS;
			} else if (reqType.equalsIgnoreCase("unregister")) {
				boolean isDeleted = rbtDBManager.deleteRBTLoginUserByUserID(rbtLoginUser.userID(), type);
				if (isDeleted) {
					logger.info("Successfully deleted the rbtLoginUser entry. subscriberId: " + subscriberID);
					updatePlayerStatusIfActiveSubscriber(subscriberID, appName,
							subscriber);
					return SUCCESS;
				} else {
					logger.info("Deletion failed for the rbtLoginUser entry. subscriberId: " + subscriberID);
					return FAILURE;
				}
			}
		}
		logger.info("Response: " + ERROR);
		return ERROR;
	}

	private void updatePlayerStatusIfActiveSubscriber(String subscriberID,
			String appName, Subscriber subscriber) {
		if (appName != null && RBTDBManager.getInstance().isSubActive(subscriber)) {
			boolean playerUpdateStatus = RBTDBManager.getInstance().updatePlayerStatus(subscriberID, "A");
			logger.info("subscriberId: "
					+ subscriberID
					+ ". Updating playerStatus as the user is already active and is a singal user now. playerUpdateStatus: "
					+ playerUpdateStatus);
		}
	}

	// JIRAID:7443 = Added by Deepak Kumar For Contest on Copy Influencer
	@Override
	public String updateCopyContestInfo(WebServiceContext task) {
		String response = ERROR;
		String contestCallerID = task.getString(param_subscriberID);
		Subscriber contestCaller = rbtDBManager.getSubscriber(contestCallerID);
		if (contestCaller == null)
			return response;
		String contestCallerCircleID = contestCaller.circleID();
		String subscriberStatus = Utility.getSubscriberStatus(contestCaller);
		String validUser = DataUtils.isValidUser(task, contestCaller);
		boolean isActive = Utility.isUserActive(subscriberStatus);
		logger.debug(" Valid User = " + validUser + " isActive = " + isActive);
		if (isActive && validUser != null && (validUser.indexOf(VALID) != -1)) {
			if (copyContestIDsTimeValidityMap.size() == 0) {
			     initCopyContestIDsTimeMap();
		    }
			String extraInfo = contestCaller.extraInfo();
			HashMap<String,String> contestCallerExtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			int noOfTimesSongCopied = 0;
		    if(contestCallerExtraInfoMap!=null && contestCallerExtraInfoMap.containsKey("NO_OF_COPIES")){
		    	String noOfCopies = contestCallerExtraInfoMap.get("NO_OF_COPIES");
		    	noOfTimesSongCopied = Integer.parseInt(noOfCopies); 
		    }else if(contestCallerExtraInfoMap == null){
		    	contestCallerExtraInfoMap = new HashMap<String,String>();
		    }
		    String contestID = contestCallerExtraInfoMap.get("CONTEST_ID");
		    boolean isValidContestID = false;
		    String nationalContestIDs = RBTParametersUtils.getParamAsString("COMMON","ALL_CONTEST_IDS", null);
		    logger.debug("Configured National Contest IDs = " + nationalContestIDs); 
		    List<String> nationalContestIDsList = null;
		    if(nationalContestIDs!=null){
		    	nationalContestIDsList = Arrays.asList(nationalContestIDs.split(","));
		    }
		    if(nationalContestIDsList!= null && nationalContestIDsList.contains(contestID)){
		    	isValidContestID = true;
		    }
	        boolean isValidPeriod = false;
		    List<Date> contestValidPeriodList = copyContestIDsTimeValidityMap.get(contestID);
		    if(contestValidPeriodList!=null && contestValidPeriodList.get(0).before(new Date())
		    		&& contestValidPeriodList.get(1).after(new Date())){
		    	 isValidPeriod = true;
		    }
		    boolean update = false;
		    logger.info("isValidContestID = "+isValidContestID + " isValidPeriod = " + isValidPeriod); 
		    if(isValidContestID && isValidPeriod){
		    	noOfTimesSongCopied = noOfTimesSongCopied + 1;
			    contestCallerExtraInfoMap.put("NO_OF_COPIES", noOfTimesSongCopied+"");
			    contestCallerExtraInfoMap.put("CONTEST_ID", contestID);
			    String contestCallerExtraInfo = DBUtility.getAttributeXMLFromMap(contestCallerExtraInfoMap);
			    update = rbtDBManager.updateExtraInfo(contestCallerID, contestCallerExtraInfo);
		    }else{
		    	contestCallerExtraInfoMap.put("NO_OF_COPIES", 1+"");
		    	contestID = getCurrentLiveNationalContestID();
		    	logger.info("New Contest ID = " + contestID);
		    	if(contestID!=null){
		    	   noOfTimesSongCopied = noOfTimesSongCopied + 1;
		    	   contestCallerExtraInfoMap.put("CONTEST_ID", contestID);
		    	   String contestCallerExtraInfo = DBUtility.getAttributeXMLFromMap(contestCallerExtraInfoMap);
		    	   update = rbtDBManager.updateExtraInfo(contestCallerID, contestCallerExtraInfo);
		    	}
		    }

		    if(update){
		    	response = SUCCESS; 
		    	int thresholdNoOfCopies = RBTParametersUtils.getParamAsInt("COMMON", "THRESHOLD_NUMBER_OF_COPIES_OF_COPY_CONTEST", 0);
				String smsTextForBelowThres = CacheManagerUtil
				             .getSmsTextCacheManager().getSmsText(Constants.COPY_CONTEST_THRESHOLD_BELOW_SMS);
				String smsTextForAboveThres = CacheManagerUtil
			                  .getSmsTextCacheManager().getSmsText(Constants.COPY_CONTEST_THRESHOLD_ABOVE_SMS);
			    String smsText = null;
				if(smsTextForBelowThres!=null && noOfTimesSongCopied < thresholdNoOfCopies){	
					smsTextForBelowThres = smsTextForBelowThres.replaceAll("%NO_OF_COPY%",noOfTimesSongCopied+"");
					smsTextForBelowThres = smsTextForBelowThres.replaceAll("%NO_OF_COPY_LEFT%", (thresholdNoOfCopies - noOfTimesSongCopied)+"");
					smsText = smsTextForBelowThres;
				}else if(smsTextForAboveThres!=null && noOfTimesSongCopied >= thresholdNoOfCopies){
					smsTextForAboveThres = smsTextForAboveThres.replaceAll("%THRESHOLD_NUMBER%", thresholdNoOfCopies+"");
					smsText = smsTextForAboveThres;
					//Tools.sendSMS(RBTParametersUtils.getParamAsString("DAEMON","SENDER_NO","54321"), contestCallerID, smsTextForBelowThres,false);
				}
				try{
					// DND implementation in copy influsiencer feature.Check for DND
					// Subscriber and SEND SMS via Voice Portal. - RBT-11688,
					// RBT-11816 . sendSMS will be called inside the checkDNDSubscriberAndSendSMS function.
					//Tools.sendSMS(RBTParametersUtils.getParamAsString("DAEMON","SENDER_NO","54321"), contestCallerID, smsText,false);
					Tools.checkDNDSubscriberAndSendSMS(getSenderNumber(contestCaller.circleID() , RBTParametersUtils
							.getParamAsString("DAEMON", "SENDER_NO", "54321")),
							contestCallerID, smsText, false);
				   }catch(Exception ex){
                      logger.info("Exception while Sending SMS");					
				   }
		     }else{
		    	logger.info("Not Updating  NO_OF_COPIES and CONTEST_ID");
		     }
	        }
		return response;
	}
	
	public static String getSenderNumber(String circleID, String senderNumber) {
		if(circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = RBTParametersUtils.getParamAsString("GATHERER", operatorName +"_SENDER_NO", senderNumber);
			}
		}
		logger.info("senderNumber :" + senderNumber);
		return senderNumber;
	}

	
	protected String deactivateODAPack(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String internalRefId = null;
		Subscriber subscriber;
		String categoryID = task.getString(param_categoryID);
//        String fromTime = task.getString(param_fromTime);
//        String fromTimeMinutes = task.getString(param_fromTimeMinutes);
//        if(fromTime == null){
//			fromTime = "0";
//        }else if(fromTime.startsWith("0")){
//        	fromTime = fromTime.substring(1);
//        }
        

//		String toTime = task.getString(param_toTime);
//        String toTimeMinutes = task.getString(param_toTimeMinutes);
//        if(toTime == null){
//			toTime = "2359";
//        }else if(toTime.startsWith("0")){
//        	toTime = toTime.substring(1);
//        }
        
		int fromHrs = 0;
		int toHrs = 23;
		int fromMinutes = 0;
		int toMinutes = 59;
		
		boolean toBeConsiderFromTimeBased = false;
		boolean toBeConsiderToTimeBased = false;
		boolean toBeConsiderStatus = false;
		boolean toBeConsiderInterval = false;
		

		if (task.containsKey(param_fromTime)) {
			toBeConsiderFromTimeBased = true;
			fromHrs = Integer.parseInt(task.getString(param_fromTime));
		}
		if (task.containsKey(param_toTime)) {
			toBeConsiderToTimeBased = true;
			toHrs = Integer.parseInt(task.getString(param_toTime));
		}
		if (task.containsKey(param_toTimeMinutes)) {
			toMinutes = Integer.parseInt(task.getString(param_toTimeMinutes));
		}
		if (task.containsKey(param_fromTimeMinutes)){
			fromMinutes = Integer.parseInt(task.getString(param_fromTimeMinutes));
		}
		
		int reqStatus = -1;
		String interval = null;
		
		if (task.containsKey(param_status)){
			toBeConsiderStatus = true;
			reqStatus = Integer.parseInt(task.getString(param_status));
		}
		
		if (task.containsKey(param_interval)){
			toBeConsiderInterval = true;
			interval = task.getString(param_interval);
		}
		
		DecimalFormat decimalFormat = new DecimalFormat("00");
		int fromTime = Integer.parseInt(fromHrs
				+ decimalFormat.format(fromMinutes));
		int toTime = Integer.parseInt(toHrs
				+ decimalFormat.format(toMinutes));
		
		
        if (task.containsKey(param_internalRefId)) {
			internalRefId = task.getString(param_internalRefId);
		}
        
		try {
			subscriber = DataUtils.getSubscriber(task);
			String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility
					.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriber.subID(), getMode(task), "404", CUSTOMIZATION,
						getClip(task), getCriteria(task));
				return subStatus;
			}
			Set<String> refIdList = new HashSet<String>();
			String callerId = task.getString(param_callerID);
			SubscriberStatus[] selectionRecords = rbtDBManager.getAllActiveSubSelectionRecords(
					subscriberID, 0);
			logger.debug("refIdList: " + refIdList);
			if (selectionRecords != null && selectionRecords.length > 0) {
				for (SubscriberStatus selection : selectionRecords) {
					logger.debug("Selection: fromTime: " + selection.fromTime() + ", toTime: " + selection.toTime() + ", callerID: " + selection.callerID() + ", extraInfo: " + selection.extraInfo());
					int selFromTime = selection.fromTime();
					int selToTime = selection.toTime();
					String selCallerId = selection.callerID();
					if (selCallerId == null)
						selCallerId = "ALL";
					String xtraInfo = selection.extraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(xtraInfo);
					String provRefId = null;
					if (xtraInfoMap != null) {
						provRefId = xtraInfoMap.get("PROV_REF_ID");
					}
					logger.debug("Selection: fromTime: " + selection.fromTime() + ", toTime: " + selection.toTime() + ", callerID: " + selCallerId + ", extraInfo: " + selection.extraInfo()
							+ ", provRefId: " + provRefId + ", requestCallerId: " + callerId);
					if ((callerId == null || callerId.equalsIgnoreCase(selCallerId))
							&& provRefId != null && (!toBeConsiderFromTimeBased || fromTime == selFromTime)
							&& (!toBeConsiderToTimeBased || toTime == selToTime)
							&& (!toBeConsiderInterval ||(interval != null && interval.equals(selection.selInterval())))
							&& (!toBeConsiderStatus ||(reqStatus != -1 && reqStatus == selection.status()))){
						refIdList.add(provRefId);
					}
				}
			}
			
			logger.debug("refIdList: " + refIdList);
            
			Set<String> refIdToCheckList = new HashSet<String>();
            refIdToCheckList.addAll(refIdList);
            logger.debug("refIdToCheckList: " + refIdToCheckList);
			if (refIdList.size() > 0) {
				for (String refId : refIdList) {
					ProvisioningRequests provisioningRequest= rbtDBManager
							.getProvisioningRequestFromRefId(subscriberID, refId);
					logger.debug("provisioningRequest: " + provisioningRequest);
					int status = provisioningRequest.getStatus();
					if (!(status == 30 || status == 31 || status == 32 || status == 33
							|| status == 50)) {
						refIdToCheckList.remove(refId);			
					}
					if (refIdToCheckList.size() == 0) {
						logger.info("ODA Pack is already deactivated");
						return PACK_ALREADY_DEACTIVE;
					}
				}
			}
			logger.debug("refIdToCheckList: " + refIdToCheckList);
			
			String deactivationMode = getMode(task);
			String deactivationModeInfo = getModeInfo(task);
			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
			
			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, deactivationMode);
			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO,
					deactivationModeInfo);
			packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
					new Date().toString());
			boolean isPackDeactivated = false;
			
			if (refIdToCheckList.size() > 0) {
				for (String refId : refIdToCheckList) {
					ProvisioningRequests provisioningRequest= rbtDBManager
							.getProvisioningRequestFromRefId(subscriberID, refId);
					String extraInfo = provisioningRequest.getExtraInfo();
					HashMap<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
					if(xtraInfoMap == null)
						xtraInfoMap = new HashMap<String,String>();
					xtraInfoMap.putAll(packExtraInfoMap);
					logger.debug("provisioningRequest: " + provisioningRequest);
					isPackDeactivated = rbtDBManager.deactivateODAPack(subscriber.subID(), categoryID,
							refId, xtraInfoMap, null);
					logger.debug("isPackDeactivated: " + isPackDeactivated);
					if (isPackDeactivated) {
						response = "SUCCESS";
					}
				}
			}
			response = Utility.getResponseString(response);

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;

	}
	
	// Added by Sandeep for profile Pack Deactivation
	@Override
	public String deactivatePack(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String packCosId = null;
		String internalRefId = null;
		Subscriber subscriber;
		CosDetails packCosDetails = null;
		if (task.containsKey(param_packCosId)) {
			packCosId = task.getString(param_packCosId);
			packCosDetails = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(packCosId, DataUtils.getUserCircle(task));
			if (packCosDetails == null) {
				return INVALID_PACK_COS_ID;
			}
		}else if(task.containsKey(param_categoryID)){
			String categoryID = task.getString(param_categoryID);
			if (categoryID != null
					&& rbtCacheManager.getCategory(Integer.parseInt(categoryID)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				String deactivateODAPackResponse = deactivateODAPack(task); 
				return deactivateODAPackResponse;
			}

		}

		if (task.containsKey(param_internalRefId)) {
			internalRefId = task.getString(param_internalRefId);
		}

		if (packCosId == null && internalRefId == null) {
			return INVALID_PARAMETER;
		}

		try {
			subscriber = DataUtils.getSubscriber(task);
			if (rbtDBManager.isAutoDownloadPackActivated(subscriber)) {
				return DCT_NOT_ALLOWED;
			}

			String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility
					.getSubscriberStatus(subscriber);
			if (subStatus.equals(LOCKED)) {
				writeEventLog(subscriber.subID(), getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return subStatus;
			}

			if (!rbtDBManager.isPackActivated(subscriber, packCosDetails)) {
				return PACK_ALREADY_DEACTIVE;
			}

			String deactivationMode = getMode(task);
			String deactivationModeInfo = getModeInfo(task);
			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
			Set<String> fileSet = new HashSet<String>();

			String browsingLanguage = task.getString(param_browsingLanguage);

			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
					deactivationMode);
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO,
					deactivationModeInfo);
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
					new Date().toString());
			boolean isPackDeactivated = rbtDBManager.deactivatePack(subscriber,
					packCosDetails, internalRefId, packExtraInfoMap);

			if (isPackDeactivated)
				deleteSettingsInSet(fileSet, subscriberID, deactivationMode,
						browsingLanguage, null, "99");

			if (isPackDeactivated) {
				response = "SUCCESS";
			}
			response = Utility.getResponseString(response);

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	private boolean getOfferForBulkActivation(String subscriberId,
			WebServiceContext task) {
		boolean result = false;
		String prePaid = task.getString(param_isPrepaid);
		String userType = prePaid.equalsIgnoreCase(YES) ? "p" : "b";

		com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offers = null;

		String mode = task.getString(param_mode);
		if (mode == null) {
			mode = "RBT";
		}
		try {
			offers = RBTSMClientHandler.getInstance().getOffer(subscriberId,
					mode, Offer.OFFER_TYPE_SUBSCRIPTION, userType,
					task.getString(param_subscriptionClass), null);

			if (offers != null && offers.length > 0) {
				task.put(param_subscriptionClass, offers[0].getSrvKey());
				task.put(param_offerID, offers[0].getOfferID());
				result = true;
			}

		} catch (RBTException e) {
			// TODO Auto-generated catch block
			logger.error("", e);

		}
		return result;
	}

	private boolean deactivateSelectionsForGroupID(String subscriberID,
			int groupID, String mode) {
		boolean deleted = false;
		GroupMembers[] groupMembers = rbtDBManager
				.getActiveMembersForGroupID(groupID);
		if (groupMembers == null || groupMembers.length == 0) {
			String callerID = "G" + groupID;
			Map<String, String> updateClauseMap = new HashMap<String, String>();
			Map<String, String> whereClauseMap = new HashMap<String, String>();

			updateClauseMap.put("DESELECTED_BY", mode);
			whereClauseMap.put("CALLER_ID", callerID);
			deleted = rbtDBManager.deactivateSubscriberSelections(subscriberID,
					updateClauseMap, whereClauseMap);
		}

		if (logger.isDebugEnabled())
			logger.debug("Status of deactivation of selections with the group caller ID : "
					+ deleted);

		return deleted;
	}

	/**
	 * <p>
	 * If clipSmsAlias is present in the configuration, returns true.
	 * <p>
	 * If multiple clipSmsAlias are present for the clip and if any of the
	 * clipSmsAlias is present in the configuration, returns true.
	 * 
	 * @param clipSmsAlias
	 * @return
	 */
	protected boolean isDurationDays(String clipSmsAlias) {
		if (clipSmsAlias == null)
			return false;

		List<String> profileDaysClipList = Arrays.asList(RBTParametersUtils
				.getParamAsString("SMS", "PROFILE_DAYS_ALIAS", "")
				.toLowerCase().split(","));
		String[] clipAliasTokens = clipSmsAlias.split(",");
		for (String eachAlias : clipAliasTokens) {
			if (profileDaysClipList.contains(eachAlias.toLowerCase()))
				return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processAddMultipleSelections
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 * 
	 * <p>For adding multiple selections at a time when comma-separated clipIds
	 * are passed.
	 */
	public String processAddMultipleSelections(WebServiceContext task) {
		String response = FAILED;
		String clipID = task.getString(param_clipID);
		if (clipID == null) {
			logger.info("clipID not passed, no returning response : "
					+ INVALID_PARAMETER);
			return INVALID_PARAMETER;
		}

		String[] clipIDs = clipID.split(",");
		for (String eachClipID : clipIDs) {
			task.put(param_clipID, eachClipID);
			String selResponse = processSelection(task);
			if (logger.isDebugEnabled())
				logger.debug("Response while setting the selection : "
						+ selResponse);

			task.remove(session_clip);
			if (selResponse.startsWith(SUCCESS))
				response = SUCCESS;
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processAddMultipleDownloads
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 * 
	 * <p>For adding multiple downloads at a time when comma-separated clipIds
	 * are passed.
	 */
	public String processAddMultipleDownloads(WebServiceContext task) {
		String response = FAILED;
		String clipID = task.getString(param_clipID);
		if (clipID == null) {
			logger.info("clipID not passed, no returning response : "
					+ INVALID_PARAMETER);
			return INVALID_PARAMETER;
		}

		String[] clipIDs = clipID.split(",");
		for (String eachClipID : clipIDs) {
			task.put(param_clipID, eachClipID);
			String downloadResponse = downloadTone(task);
			if (logger.isDebugEnabled())
				logger.debug("Response while adding the download : "
						+ downloadResponse);

			task.remove(session_clip);
			if (downloadResponse.equals(SUCCESS))
				response = SUCCESS;
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processDeleteMultipleSelections
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 * 
	 * <p>For deleting multiple selections at a time when comma-separated
	 * clipIds are passed.
	 */
	public String processDeleteMultipleSelections(WebServiceContext task) {
		String response = FAILED;
		String clipID = task.getString(param_clipID);
		if (clipID == null) {
			logger.info("clipID not passed, no returning response : "
					+ INVALID_PARAMETER);
			return INVALID_PARAMETER;
		}

		String[] clipIDs = clipID.split(",");
		for (String eachClipID : clipIDs) {
			task.put(param_clipID, eachClipID);
			String delResponse = deleteSetting(task);
			if (logger.isDebugEnabled())
				logger.debug("Response while deleting the selection : "
						+ delResponse);

			task.remove(session_clip);
			if (delResponse.equals(SUCCESS))
				response = SUCCESS;
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processDeleteMultipleDownloads
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 * 
	 * <p>For deleting multiple downloads at a time when comma-separated clipIds
	 * are passed.
	 */
	public String processDeleteMultipleDownloads(WebServiceContext task) {
		String response = FAILED;
		String clipID = task.getString(param_clipID);
		String isDeactivateAllClips = task.getString(param_deactivateAllClips);

		String[] clipIDs = null;
		// RBT-12466-Elsalvador & Nicaragua: New Churn Flow
		if (isDeactivateAllClips != null
				&& isDeactivateAllClips.equalsIgnoreCase("true")) {
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] subscriberDownloads = rbtDBManager
					.getSubscriberAllActiveDownloads(subscriberID);
			int count = 0;
			if (null != subscriberDownloads) {
				clipIDs = new String[subscriberDownloads.length];
				for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
					Clip tmpClip = rbtCacheManager
							.getClipByRbtWavFileName(subscriberDownload
									.promoId());
					if (tmpClip == null)
						continue;
					clipIDs[count++] = String.valueOf(tmpClip.getClipId());
					logger.info("clipID not passed,so all the songs in download will be deactivated : "
							+ clipIDs.toString());
				}
			} else {
				logger.info("no clip in downloads");
			}

		} else {
			if (clipID == null) {
				logger.info("clipID not passed, no returning response : "
						+ INVALID_PARAMETER);
				return INVALID_PARAMETER;
			} else {
				clipIDs = clipID.split(",");
			}
		}
		if (null != clipIDs && clipIDs.length > 0) {
			for (String eachClipID : clipIDs) {
				task.put(param_clipID, eachClipID);
				String delResponse = deleteTone(task);
				if (logger.isDebugEnabled())
					logger.debug("Response while deleting the download : "
							+ delResponse);

				task.remove(session_clip);
				if (delResponse.equals(SUCCESS))
					response = SUCCESS;
			}
		} else {
			return FAILED;
		}

		return response;
	}

	public String reset(WebServiceContext task) {
		String response = SUCCESS;
		String subscriberId = task.getString(param_subscriberID);
		SubscriberStatus subscriberSettings[] = rbtDBManager
				.getAllActiveSubscriberSettings(subscriberId);
		if (subscriberSettings != null) {
			Set<String> selectionRefIds = new HashSet<String>();
			for (int i = 0; i < subscriberSettings.length; i++) {
				if (subscriberSettings[i] != null) {
					if (subscriberSettings[i].callerID() != null
							|| Utility.isShuffleCategory(subscriberSettings[i]
									.categoryType())
							|| subscriberSettings[i].status() == 80) {
						selectionRefIds.add(subscriberSettings[i].refID());
					}
				}
			}
			if (selectionRefIds.size() > 0) {
				for (String refId : selectionRefIds) {
					String updateResponse = rbtDBManager
							.deactivateSubscriberRecordsByRefId(subscriberId,
									"RESET", refId);
					if (!updateResponse.equalsIgnoreCase(SUCCESS)) {
						response = FAILED;
						break;
					}
				}
			}
		}
		// if(response.equalsIgnoreCase(SUCCESS)){
		// response = shuffleDownloads(task);
		// }

		return response;
	}

	/*
	 * @Deepak Kumar (non-Javadoc)JIRA-ID:5349
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#disableRandomization
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public String disableRandomization(WebServiceContext task) {
		logger.debug("Inside method disable Randomization.....");
		String response = FAILED;
		String subscriberId = task.getString(param_subscriberID);
		SubscriberStatus subscriberSettings[] = rbtDBManager
				.getAllActiveSubscriberSettings(subscriberId);
		if (subscriberSettings != null) {
			Map<String, SubscriberStatus> callerIDSettingsMap = new HashMap<String, SubscriberStatus>();
			String deactivateBy = "UNRANDOMIZATION";
			// Latest one not deactivating
			for (int i = 0; i < subscriberSettings.length; i++) {
				String callerID = subscriberSettings[i].callerID();
				callerIDSettingsMap.put(callerID, subscriberSettings[i]);
			}
			for (int i = 0; i < subscriberSettings.length; i++) {
				String callerID = subscriberSettings[i].callerID();
				String refID = subscriberSettings[i].refID();
				SubscriberStatus subStatus = callerIDSettingsMap.get(callerID);
				if (!refID.equalsIgnoreCase(subStatus.refID())
						&& subscriberSettings[i].selType() != 2) {
					rbtDBManager.deactivateSubscriberRecordsByRefId(
							subscriberId, deactivateBy, refID);
				}
			}
		}

		HashMap<String, String> attributeMap = null;// new
													// HashMap<String,String>();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
		String extraInfo = subscriber.extraInfo();
		attributeMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if (attributeMap != null && attributeMap.containsKey("UDS_OPTIN")) {
			attributeMap.remove("UDS_OPTIN");
		}
		extraInfo = DBUtility.getAttributeXMLFromMap(attributeMap);
		if (attributeMap == null) {
			attributeMap = new HashMap<String, String>();
		}
		attributeMap.put("EXTRA_INFO", extraInfo);
		response = rbtDBManager.updateSubscriber(subscriberId, attributeMap);
		logger.info("Response from DisableRandomization===" + response);
		return response;
	}

	/**
	 * This API sends an SMS based on the action passed.
	 * 
	 * @param subscriberID
	 * @param action
	 */
	public boolean sendAcknowledgementSMS(WebServiceContext webServiceContext,
			String action) {
		String subscriberID = webServiceContext.getString(param_subscriberID);
		String mode = getMode(webServiceContext);

		String type = "WEBSERVICE_" + action;
		String defSubType = "SUCCESS";
		String subType = "SUCCESS_" + mode;
		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(
				type, subType);
		logger.debug(subType + ":subType " + type
				+ "SMS text not configured for type: " + rbtText
				+ ", subType: " + action);
		if (rbtText == null) {
			rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(
					type, defSubType);
			if (rbtText == null) {
				if (logger.isDebugEnabled())
					logger.debug("SMS text not configured for type: " + type
							+ ", subType: " + defSubType);
				return false;
			}
		}

		String text = rbtText.getText();
		if (text == null) {
			if (logger.isInfoEnabled())
				logger.info("SMS text is configured as null for type: " + type
						+ ", subType: " + subType);
			return false;
		}

		if( action!=null && (action.equalsIgnoreCase("REGISTER") || action.equalsIgnoreCase("OTP_LOGIN"))) {
			text = text.replaceAll("%P", webServiceContext.getString("password"));
		}
		
		if(action!=null && action.equalsIgnoreCase("UNSUB_DELAY") && text.indexOf("%DAYS_LEFT%")>= 0)
		{
			try {
				Subscriber subscriber = DataUtils
						.getSubscriber(webServiceContext);
				Map<String, String> nextBillDateMap = Utility
						.getNextBillingDateOfServices(webServiceContext);
				String subRefID = subscriber.refID();
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				String nextBillDate = nextBillDateMap.get(subRefID);
				Date dateObj = null;
				if (nextBillDate != null)
					dateObj = rbtDateFormat.parse(nextBillDate);

				if (dateObj != null) {
					int daysLeft = (int) ((dateObj.getTime() - System
							.currentTimeMillis()) / (1000 * 24 * 60 * 60));
					if (daysLeft > 0) {
						RBTText daystxt = CacheManagerUtil
								.getRbtTextCacheManager().getRBTText(
										"SMS_DAYS", null);
						String daysKeyword = (daystxt != null) ? daystxt
								.getText() : "days";
						text = text.replaceAll("%DAYS_LEFT%",
								String.valueOf(daysLeft) + " " + daysKeyword);
					} else {
						RBTText hourstxt = CacheManagerUtil
								.getRbtTextCacheManager().getRBTText(
										"SMS_HOURS", null);
						String hoursKeyword = (hourstxt != null) ? hourstxt
								.getText() : "hours";
						long hoursLeft = (dateObj.getTime() - System
								.currentTimeMillis()) / (1000 * 60 * 60);
						text = text.replaceAll("%DAYS_LEFT%",
								String.valueOf(hoursLeft) + " " + hoursKeyword);
					}

				}
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}

		}

		String churnOfferFromBI = webServiceContext.getString(param_churnOfferFromBI);
		churnOfferFromBI = churnOfferFromBI!=null?churnOfferFromBI:"";
		text = text.replaceAll("%CHURN_OFFER%", churnOfferFromBI);

		String senderID = RBTParametersUtils.getParamAsString(
				iRBTConstant.WEBSERVICE, "ACK_SMS_SENDER_NO", null);
		logger.debug(senderID + "SMS text not configured for type: " + rbtText
				+ ", subType: " + action);
		if (senderID == null) {
			logger.info("SENDER_NO is not configured, so not sending the SMS");
			return false;
		}

		boolean sendSMSResponse = false;
		try {
			sendSMSResponse = Tools
					.sendSMS(senderID, subscriberID, text, false);
		} catch (OnMobileException e) {
			logger.error(e.getMessage(), e);
		}

		if (sendSMSResponse)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#
	 * processDirectCopyRequest
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public String processDirectCopyRequest(WebServiceContext task) {
		String response = ERROR;
		try {
			String subscriberID = task.getString(param_subscriberID);
			String fromSubscriber = task.getString(param_fromSubscriber);

			if (subscriberID == null || fromSubscriber == null)
				return INVALID_PARAMETER;

			Subscriber subscriber = DataUtils.getSubscriber(task);
			response = isValidUser(task, subscriber);
			if (!response.equals(VALID)) {
				logger.info("Not a valid user, so not processing : "
						+ subscriberID);
				return response;
			}

			if (rbtDBManager.subID(subscriberID).equals(
					rbtDBManager.subID(fromSubscriber)))
				return OWN_NUMBER;

			String wavFile = null;
			int categoryID = -1;

			boolean isDownloadsModel = RBTParametersUtils.getParamAsBoolean(
					"DAEMON", "PROCESS_DOWNLOADS", "TRUE");
			if (isDownloadsModel) {
				SubscriberDownloads[] subDownloads = rbtDBManager
						.getActiveSubscriberDownloads(fromSubscriber);
				if (subDownloads == null || subDownloads.length == 0)
					return NO_DOWNLOADS;

				SubscriberDownloads latestValidDownload = null;
				for (int i = subDownloads.length - 1; i >= 0; i--) {
					int catID = subDownloads[i].categoryID();
					Category category = rbtCacheManager.getCategory(catID);
					if (!RBTParametersUtils.getParamAsBoolean(
							iRBTConstant.COMMON,
							"DIRECT_COPY_SHUFFLES_SUPPORTED", "FALSE")
							&& category != null
							&& Utility.isShuffleCategory(category
									.getCategoryTpe()))
						continue;

					latestValidDownload = subDownloads[i];
					break;
				}

				if (latestValidDownload == null)
					return NO_DOWNLOADS;
				wavFile = latestValidDownload.promoId();
				categoryID = latestValidDownload.categoryID();
			}
			// selections model
			else {
				SubscriberStatus[] allActiveSelections = rbtDBManager
						.getAllActiveSubscriberSettings(fromSubscriber);
				if (allActiveSelections == null
						|| allActiveSelections.length == 0)
					return NO_SELECTIONS;

				SubscriberStatus latestCallerSelection = null;
				SubscriberStatus latestAllSelection = null;
				for (SubscriberStatus selection : allActiveSelections) {
					int catID = selection.categoryID();
					Category category = rbtCacheManager.getCategory(catID);
					if (!RBTParametersUtils.getParamAsBoolean(
							iRBTConstant.COMMON,
							"DIRECT_COPY_SHUFFLES_SUPPORTED", "FALSE")
							&& category != null
							&& Utility.isShuffleCategory(category
									.getCategoryTpe()))
						continue;

					if (latestAllSelection == null
							&& selection.callerID() == null) {
						latestAllSelection = selection;
					}

					if (selection.status() == 1 && selection.callerID() == null) {
						latestAllSelection = selection;
					} else if (selection.status() == 1
							&& selection.callerID().equals(subscriberID)) {
						latestCallerSelection = selection;
					}
				}

				if (latestCallerSelection != null) {
					wavFile = latestCallerSelection.subscriberFile();
					categoryID = latestCallerSelection.categoryID();
				} else if (latestAllSelection != null) {
					wavFile = latestAllSelection.subscriberFile();
					categoryID = latestAllSelection.categoryID();
				} else {
					return NO_SELECTIONS;
				}
			}

			Clip clip = rbtCacheManager.getClipByRbtWavFileName(wavFile);
			if (clip == null)
				return CLIP_NOT_EXISTS;

			if (clip.getClipEndTime().before(new Date()))
				return CLIP_EXPIRED;

			Category category = rbtCacheManager.getCategory(categoryID);
			if (category == null)
				return CATEGORY_NOT_EXISTS;

			if (Utility.isShuffleCategory(category.getCategoryTpe())
					&& category.getCategoryEndTime().before(new Date()))
				return CATEGORY_EXPIRED;

			task.put(param_clipID, String.valueOf(clip.getClipId()));
			task.put(param_categoryID, String.valueOf(categoryID));

			response = processSelection(task);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	private static void initCopyContestIDsTimeMap(){
		String confParam = RBTParametersUtils.getParamAsString("COMMON", "CONTEST_ID_TIME_VALIDITY_MAP", null);
        if(confParam == null)
        	return;
        try{
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        String str[] =   confParam.split(";");
	        for(String contestIdAndTime : str){
	        	String aa[] = contestIdAndTime.split("=");
	        	if(aa.length == 2){
	        		String contestID = aa[0];
	        	    String timeduration = aa[1];
	        	    String time[] = timeduration.split(":");
	        	    Date startTime = sdf.parse(time[0]);
	        	    Date endTime = sdf.parse(time[1]);
	        	    List<Date> list = new ArrayList<Date>();
	        	    list.add(startTime);
	        	    list.add(endTime);
        	        copyContestIDsTimeValidityMap.put(contestID, list);
	        	}
           }
        }catch(Exception ex){
        	logger.info("Exception while initializing Copy Contest ID and Time Duration Mapping");
        }
        
	}
	
	private static String getCurrentLiveNationalContestID(){
		String nationalWiseContestIDs = RBTParametersUtils.getParamAsString("COMMON",
											"ALL_CONTEST_IDS", null);
		if(nationalWiseContestIDs == null)
			return null;
		String contestIDs[] = nationalWiseContestIDs.split(",");
		String currentContestID = null;
		for(String id : contestIDs){
			List<Date> list = copyContestIDsTimeValidityMap.get(id);
			if(list!=null && list.get(0).before(new Date()) && list.get(1).after(new Date())){
				currentContestID = id;
				break;
			}
		}
		return currentContestID;
	}

	protected String getContentBasedClassType(WebServiceContext task,	Clip clip) {
		String classType = null;
		if((task.containsKey(param_useUIChargeClass) && task.getString(param_useUIChargeClass).equalsIgnoreCase("y") )|| clip == null || clip.getContentType() == null) {
			return null;
		}
		String mode = task.getString(param_mode);
		if(mode!=null){
			mode = mode.toUpperCase();
		}
		String modeBasedContentTypeChageClass = parametersCacheManager.getParameterValue("COMMON", "CLIP_" + clip.getContentType().toUpperCase() + "_CHARGE_CLASS_"+mode, null);
		if(modeBasedContentTypeChageClass != null)
			return modeBasedContentTypeChageClass;
		String contentTypeChageClass = parametersCacheManager.getParameterValue("COMMON", "CLIP_" + clip.getContentType().toUpperCase() + "_CHARGE_CLASS", null);
		return contentTypeChageClass;
	}
	
	protected String getPreRBTWavFileFromSubClassConfig(String subscriptionClass){
		if(subClassPreRBTWavFileMap.size()>0){
			return subClassPreRBTWavFileMap.get(subscriptionClass);
		}
	    String preRBTWavFile = null;
		String subClassPreRBTStr = RBTParametersUtils.getParamAsString("COMMON",
				"SUB_CLASSES_AND_PRE_RBT_WAV_MAPPING", null);
		if(subClassPreRBTStr!=null){
			String str[] = subClassPreRBTStr.split(";");
			for(int i=0;i<str.length;i++){
				String ss[] = str[i].split(":");
				if(ss.length == 2)
					subClassPreRBTWavFileMap.put(ss[0], ss[1]);
			}
		}
		
		preRBTWavFile = subClassPreRBTWavFileMap.get(subscriptionClass);
	    return preRBTWavFile;
	}
	
	protected boolean isDownloadSongPackOverLimitReached(String cosId, String subscriberId, WebServiceContext task) {
		
		
		CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
		
//		Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//				.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
//		
//		
//		
//		List<String> musicPackCosIdList = null;
//		
//		if(muiscPackCosIdParam != null) {
//			musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//		}
//		
		int packNumMaxSelection = 0;
		
		boolean isComboSelectonRequest = task.containsKey(param_requestFromSelection);
		
		
		if(packCos != null && packCos.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
			SubscriberDownloads[] downloads = rbtDBManager.getActiveSubscriberDownloads(subscriberId);
			CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
			if(downloads != null) {
				packNumMaxSelection = downloads.length;
			}
			task.put("PACK_NUM_MAX_SELECTON", packNumMaxSelection + "");
			if((isComboSelectonRequest && packNumMaxSelection >= cosDetails.getFreeSongs()) || packNumMaxSelection > cosDetails.getFreeSongs()) {
				return true;
			}									
		}
		
		return false;
	}
	
	protected String isSongPackDeactivationPending(String subscriberID, HashMap<String, String> subExtraInfoMap) {
		String activePacks = null;
		if (subExtraInfoMap != null && subExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
			activePacks = subExtraInfoMap.get(iRBTConstant.EXTRA_INFO_PACK);
			String[] activePack = activePacks.split("\\,");
			
			String finalPackCosId = null;
			for(String packCosId : activePack) {
				CosDetails packCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(packCosId);
				if(packCos != null && packCos.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
					finalPackCosId = packCosId;
				}
			}
			
			if(finalPackCosId != null) {
				List<ProvisioningRequests> provisioningRequest = rbtDBManager.getProvisioningRequests(subscriberID, Integer.parseInt(finalPackCosId));
				if(provisioningRequest != null && provisioningRequest.size() > 0) {
					int packStatus = provisioningRequest.get(0).getStatus();
					if(packStatus == iRBTConstant.PACK_TO_BE_DEACTIVATED || packStatus == iRBTConstant.PACK_DEACTIVATION_PENDING || packStatus == iRBTConstant.PACK_DEACTIVATION_ERROR) {
						return PACK_DEACT_PENDING;
					}
				}
			}
			
//			Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//					.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
//			
//			List<String> musicPackCosIdList = null;
//			
//			if(muiscPackCosIdParam != null) {
//				musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//				String finalPackCosId = null;
//				for(String packCosId : activePack) {
//					if(musicPackCosIdList.contains(packCosId)) {
//						finalPackCosId = packCosId;
//					}
//				}
//				if(finalPackCosId != null) {
//					List<ProvisioningRequests> provisioningRequest = rbtDBManager.getProvisioningRequests(subscriberID, Integer.parseInt(finalPackCosId));
//					if(provisioningRequest != null && provisioningRequest.size() > 0) {
//						int packStatus = provisioningRequest.get(0).getStatus();
//						if(packStatus == iRBTConstant.PACK_TO_BE_DEACTIVATED || packStatus == iRBTConstant.PACK_DEACTIVATION_PENDING || packStatus == iRBTConstant.PACK_DEACTIVATION_ERROR) {
//							return PACK_DEACT_PENDING;
//						}
//					}
//				}
//			}
			
		}
		
		return null;
	}

	@Override
	public String processUDPSelections(WebServiceContext task) {
		logger.info("processUDPSelections method invoked");
		String response = ERROR;
		String subscriberID = task.getString(param_subscriberID);
		String udpId = task.getString(param_udpId);
		
		logger.info("SUBSCRIBER_ID: "+subscriberID+" UDP_ID: "+udpId);
		
		IUDPService udpService = null;
		try {
			if (subscriberID != null && udpId != null && !subscriberID.trim().isEmpty() && !udpId.trim().isEmpty()) {
				udpService = (IUDPService) ConfigUtil.getBean(BeanConstant.UDP_RBT_SERVICE_IMPL);
				
				UDPResponseBean responseBean = (UDPResponseBean) udpService.getContentsFromUDP(subscriberID, udpId, -1, -1);
				List<Clip> clips = responseBean.getClips();
				task.put(SKIP_CONTENT_CHECK, true);
				if (clips != null && !clips.isEmpty()) {
					for (int i=0;i<clips.size();i++) {
						WebServiceContext tempTask = (WebServiceContext) task.clone();
						if(i == 0)
							tempTask.put(param_inLoop, NO);
						else
							tempTask.put(param_inLoop, YES);
						
						if(clips.get(i).getAlbum() != null && clips.get(i).getAlbum().equalsIgnoreCase("RBTUGC")) {
							UGCAssetUtilBuilder ugcAssetUtilBuilder = (UGCAssetUtilBuilder) ConfigUtil.getBean(BeanConstant.UGC_ASSET_UTIL_BUILDER);
							tempTask.put(param_categoryID, ugcAssetUtilBuilder.getCategoryId());
							tempTask.put(param_clipID, clips.get(i).getClipRbtWavFile());
						} else {
							tempTask.put(param_categoryID, "3");
							tempTask.put(param_clipID, clips.get(i).getClipId()+"");
						}
						
						response = processSelection(tempTask);

						if (!response.equalsIgnoreCase(SUCCESS))
							return response;
					}
					
					UDPBean udpBean = UDPDOToResponseBeanConverter.getUDPBeanFromUDPResponseBean(responseBean);
					if (udpBean != null) {
						IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
						udpBean.setSelActivated(true);
						udpDao.updateUDP(udpBean);
					}
				} else {
					response = com.onmobile.apps.ringbacktones.v2.common.Constants.CONTENT_NOT_FOUND;
				}
				if(response.equalsIgnoreCase(SUCCESS)) {
					SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
					ExtendedSubStatus subStatus = new ExtendedSubStatus();
					subStatus.setSubId(subscriberID);
					List<ExtendedSubStatus> extendedSubStatus = subscriberSelection.getSelections(subStatus);
					if(extendedSubStatus != null && !extendedSubStatus.isEmpty()) {
						for(ExtendedSubStatus status : extendedSubStatus) {
							if(status.udpId() != null && !status.udpId().equalsIgnoreCase(udpId)) {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put(param_udpId, status.udpId());
								map.put(param_subscriberID, subscriberID);
								map.put(param_mode, task.getString(param_mode));
								map.put(param_dtocRequest, task.getString(param_dtocRequest));
								WebServiceContext deactUDPTask = Utility.getTask(map);
								processUDPDeactivation(deactUDPTask);
								break;
							}

						}
					}
				}
			}
		} catch (UserException e) {
			logger.error("Exception Occured: "+e,e);
			response = e.getMessage();
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			response = e.getBeanName();
		} catch (Exception e) {
			logger.error("Exception Occured: "+e,e);
		}			

		logger.info("processUDPSelections Response : "+response);
		return response;
	}

		@Override
		public String processUDPDeactivation(WebServiceContext task) {
			logger.info("processUDPDeactivation method invoked");
			String response = ERROR;
			String subscriberID = task.getString(param_subscriberID);
			String udpId = task.getString(param_udpId);
			IUDPService udpService = null;
			logger.info("SUBSCRIBER_ID: "+subscriberID+" UDP_ID: "+udpId);
			try {
				if (subscriberID != null && udpId != null && !subscriberID.trim().isEmpty() && !udpId.trim().isEmpty()) {
					udpService = (IUDPService) ConfigUtil.getBean(BeanConstant.UDP_RBT_SERVICE_IMPL);
					
					response = deleteSetting(task);
					if (response.equalsIgnoreCase(SUCCESS)) {
						SubscriberSelectionImpl selectionImpl = (SubscriberSelectionImpl) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);

						int count = selectionImpl.getSubSelectionCountByUDPId(udpId);
						logger.info("Selections_Active_Count : "+count);
						if (count == 0) { 
							UDPResponseBean responseBean = (UDPResponseBean) udpService.getContentsFromUDP(subscriberID, udpId, 0, 1);
							UDPBean udpBean = UDPDOToResponseBeanConverter.getUDPBeanFromUDPResponseBean(responseBean);
							if (udpBean != null) {
								IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
								udpBean.setSelActivated(false);
								udpDao.updateUDP(udpBean);
							}
						}
					}
				} 
			} catch (UserException e) {
				logger.error("Exception Occured: "+e,e);
				response = e.getMessage();
			} catch (DataAccessException e) {
				logger.error("Exception Occured: "+e,e);
				response = e.getMessage();
			} catch (IllegalArgumentException e) {
				logger.error("Exception Occured: "+e,e);
			} catch (NoSuchBeanDefinitionException e) {
				logger.error("Exception Occured: "+e,e);
				response = e.getBeanName();
			} catch (Exception e) {
				logger.error("Exception Occured: "+e,e);
			}	
			logger.info("processUDPDeactivation Response : "+response);
			return response;
		}
		
	public String processUpgradeDownload(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String wavFileName = webServiceContext.getString(param_clipName);
			String newClassType = webServiceContext.getString(param_classType);
			String clipId		=	 webServiceContext.getString(param_clipID);
			
			if (subscriberID==null || newClassType==null || (wavFileName==null && clipId==null)) {
				logger.info("PARAMETER IS NULL VALUE , SUBSCRIBER ID="+subscriberID+" , NEW CLASS TYPE="+newClassType+" , WAV FILE NAME/PROMO ID="+wavFileName+" ,SONG ID:"+clipId+" , WAV_FILE_NAME/SONGID ANY ONE OF THE PARAMETER IS MANDATORY");
				return INVALID_PARAMETER;
			}
			
			Clip clip = null;
			if(wavFileName==null){
				try {
					clip = rbtCacheManager.getClip(clipId);
					if(clip!=null){
						wavFileName = clip.getClipRbtWavFile();
					}else{
						logger.error("CLIP NOT FOUND IN RBT CACHE FOR THE CLIPID:"+clipId);
						return INVALID_PARAMETER;
					}
				} catch (Exception e) {
					logger.error("CLIP NOT FOUND FOR THE CLIPID:"+clipId+", "+e.getMessage(), e);
					return INVALID_PARAMETER;
				}
			}
			
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			if (subscriber != null
					&& subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
				SubscriberDownloads subDownloads = rbtDBManager.getActiveSubscriberDownload(subscriberID, wavFileName);
				if (subDownloads == null) {
					logger.info("No selections for the subscriber :"
							+ subscriberID + " , Wav File Name:" + wavFileName);
					return NO_DOWNLOADS;
				}
			

				if (subDownloads != null
						&& subDownloads.downloadStatus() == 'y') {
					if (newClassType != null) {
						newClassType = newClassType.replaceAll("RBT_SEL_", "");
					}
					String refID = subDownloads.refID();
					String extraInfo = subDownloads.extraInfo();
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap == null) {
						extraInfoMap = new HashMap<String, String>();
					}

					extraInfoMap
							.put("OLD_CLASS_TYPE", subDownloads.classType());
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					if (newClassType != null) {
						rbtDBManager.updateDownloads(subscriberID, refID, 'c',
								extraInfo, newClassType);
					}
					response = SUCCESS;
				} else {
					return NO_DOWNLOADS_TO_UPGRADE;
				}
			} else {
				response = USER_NOT_ACTIVE;
			}

		} catch (Exception ex) {
			logger.error("Exception While Upgrading Selection:" + ex);
		}
		return response;
	}
	
	private void activateDTOCServiceClass(WebServiceContext task, Subscriber subscriber) {
		boolean allowUpgradation = false;
		String dtocAppClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_APP_SERVICE_CLASS", null);
		String operatorUserType = task.getString(param_operatorUserType);
		String offerId = task.getString(param_offerID);
		if (dtocAppClass != null && task.containsKey(param_subscriptionClass)) {
			String[] dtocServiceClassArr = dtocAppClass.split(",");
			if (dtocServiceClassArr.length > 0) {
				List<String> serviceClass = Arrays.asList(dtocServiceClassArr);
				if (serviceClass.contains(task.get(param_subscriptionClass))) {
					allowUpgradation = true;
				}
			}
		}

		String freeTrialCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_FREE_TRIAL_COS_ID",null);
		if (allowUpgradation && !task.containsKey(param_packCosId)
				&& RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DTOC_DEPLOYED", "FALSE")
				&& rbtDBManager.isSubActive(subscriber)) {
			if (freeTrialCosId != null) {
				task.put(param_packCosId, freeTrialCosId);
				task.put(param_packOfferID , offerId);
			}
		} else if (allowUpgradation && operatorUserType != null && !task.containsKey(param_packCosId)
				&& rbtDBManager.isSubActive(subscriber)
				&& operatorUserType.equalsIgnoreCase(OperatorUserTypes.TRADITIONAL.getDefaultValue())) {
			if (freeTrialCosId != null) {
				task.put(param_packCosId, freeTrialCosId);
				task.put(param_packOfferID , offerId);
			}
		}
	}
	
	
	public static Map<String, String> getoriginalAndTrimmedCallerIds(String callerIdStr) {
		Map<String, String> originalAndTrimmedCallerId = new HashMap<String, String>();

		if (callerIdStr != null) {
			String[] callerIds = callerIdStr.trim().split(",");
			for (int i = 0; i < callerIds.length; i++) {
				String originalCallerId = callerIds[i];
				String callerID = callerIds[i];

				// Added for RBT-15167 Fail to do the selection with
				// caller=PRIVATE
				if (callerID != null && callerID.equalsIgnoreCase("PRIVATE")) {
					originalAndTrimmedCallerId.put(callerID, originalCallerId);
				} else if (callerID != null && !callerID.startsWith("G")) {
					// callerID null means for ALL callers and if starts with
					// 'G'
					// means groupID.

					Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON,
							"MINIMUM_CALLER_ID_LENGTH", "7");
					int minCallerIDLength = Integer.parseInt(parameter.getValue());

					boolean isValidCallerId = false;
					if (callerID.length() >= minCallerIDLength) {
						try {
							callerID = RBTDBManager.getInstance().subID(callerID);
							Long.parseLong(callerID);
							isValidCallerId = true;
						} catch (NumberFormatException e) {
						}
					}

					if (!isValidCallerId) {
						/*
						 * logger.info("Invalid callerID. Returning response: "+
						 * INVALID_PARAMETER); writeEventLog(subscriberID,
						 * getMode(task), "404", CUSTOMIZATION, getClip(task),
						 * getCriteria(task)); return INVALID_PARAMETER;
						 */
					} else {
						originalAndTrimmedCallerId.put(callerID, originalCallerId);
					}
				} else if (callerID.startsWith("G")) {
					originalAndTrimmedCallerId.put(callerID, originalCallerId);
				}
			}

		}
		return originalAndTrimmedCallerId;
	}
	
	private String getCallerIdForDisplay(String originalcallerId , String trimmedCallerId){
		String originalId   = originalcallerId ; 
		if(originalId.length() > trimmedCallerId.length()){
			originalId = "0"+trimmedCallerId ;
		}
		return originalId ;
	}
}
