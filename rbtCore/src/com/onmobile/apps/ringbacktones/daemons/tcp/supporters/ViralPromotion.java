/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp.supporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.CurrencyUtil;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.ViralPromotionEventLogger;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.grbt.GrbtLogger;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionRequest;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.promotions.contest.ContestUtils;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.exception.OnMobileException;

/**
 * @author vinayasimha.patil
 * 
 */
public class ViralPromotion
{
	private static Logger logger = Logger.getLogger(ViralPromotion.class);

	private static RBTDBManager rbtDBManager = null;
	private static RBTCacheManager rbtCacheManager = null;

	private static Set<String> testNumers = null;
	private static Set<String> blockedClips = null;
	private static Set<String> blockedMappingClips = null;
	private static WriteDailyTrans writeTrans = null;
	private static String logPath = null;
	private static ViralPromotionEventLogger promotionLogger = null;

	private static List<List<Integer>> blackoutTimesList = null;
	private static Set<String> allowedPrefixesSet = null;

	private static int delayTimeInMins = 0;
	private static MemCachedClient mc = null;

	private static Object lock = new Object();
	private static volatile long intervalStart = System.currentTimeMillis();
	
	public static  Object getLock() {
		return lock;
	}


	public static long getIntervalStart() {
			return intervalStart;
	}

	
	private static int tps = 0;
	private static volatile int requestCounter = 0;
	
	public static int getRequestCounter() {
			return requestCounter;
	}

	private static List<String> migratedUserSubClassesList = new ArrayList<String>();
	private static boolean isArtistBasedSmsEnabled = false;
	public static boolean isArtistBasedSmsEnabled() {
		return isArtistBasedSmsEnabled;
	}

	public static void setArtistBasedSmsEnabled(boolean isArtistBasedSmsEnabled) {
		ViralPromotion.isArtistBasedSmsEnabled = isArtistBasedSmsEnabled;
	}

	private static boolean isAddInLoop = false;
	private static Logger viralPromotionLogger = Logger.getLogger("VIRAL_LOGGER");
	static
	{
		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		mc = RBTCache.getMemCachedClient();

		logPath = RBTParametersUtils.getParamAsString("COMMON",
				"VIRAL_PROMOTION_LOG_PATH", null);
		if (logPath != null)
		{
			try
			{
				promotionLogger = new ViralPromotionEventLogger(
						new com.onmobile.reporting.framework.capture.api.Configuration(
								logPath));
			}
			catch (Exception e)
			{
				promotionLogger = null;
			}
		}
		isArtistBasedSmsEnabled = RBTParametersUtils.getParamAsBoolean("VIRAL",
				"ENABLE_ARTIST_BASED_SMS", "false");
		initializeTestNumbers();
		initializeBlockedContents();
		initTrans();
		initializeBlackOut();
		initAllowedPrefixes();

		tps = RBTParametersUtils.getParamAsInt("VIRAL", "INCOMING_TPS", 0);
		delayTimeInMins = RBTParametersUtils.getParamAsInt("VIRAL",
				"SMS_DELAY_TIME_IN_MINUTES", 0);
		String migratedUserSubClasses = RBTParametersUtils.getParamAsString("WEBSERVICE",
				"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);

		if (null != migratedUserSubClasses) {
			migratedUserSubClassesList = ListUtils.convertToList(
					migratedUserSubClasses, ",");
		}
		isAddInLoop = RBTParametersUtils.getParamAsBoolean("VIRAL",
				"ADD_SEL_TO_LOOP", "FALSE");
	}

	public static void sendPromotion(ViralPromotionRequest promotionRequest)
	{
		String viralPromotionType = RBTParametersUtils.getParamAsString(
				"VIRAL", "VIRAL_PROMO_TYPE", null);
		Boolean blockViralSms = false ;
		List<String> nameTunes = new ArrayList<String>();
		String nameTune = RBTParametersUtils.getParamAsString("COMMON",
				"BLOCKED_ALBUMS_FOR_PROMPT", null);
		if(nameTune!=null){
			nameTunes =  Arrays.asList(((nameTune.toLowerCase()).trim().split("\\s*,\\s*")));
		}
		blockViralSms= RBTParametersUtils.getParamAsBoolean("VIRAL", "BLOCK_VIRAL_SMS_FOR_NAME_OR_CORPARATE_TUNE", "FALSE");
		String callerID = promotionRequest.getCallerID();
		String calledID = promotionRequest.getCalledID();
		String rbtWavFile = promotionRequest.getRbtWavFile();
		long calledTime = promotionRequest.getCalledTime();
		short callDuration = promotionRequest.getCallDuration();
		DateFormat viraldateFormat = new SimpleDateFormat(
				RBTParametersUtils.getParamAsString("VIRAL",
						"VIRAL_PROMOTION_SDR_LOG_DATE_FORMAT",
						"yyyy-MM-dd HH:mm:ss"));
		StringBuffer artistName = new StringBuffer();
		Clip clip = rbtCacheManager.getClipByRbtWavFileName(rbtWavFile);
		String circleID = promotionRequest.getCircleID();
		
		if(GrbtLogger.tpLogger != null && RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_DATA", "FALSE") && RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_DATA_TP", "FALSE"))
		GrbtLogger.tpLogger.info(calledID+","+rbtWavFile+","+callDuration);

		if (viralPromotionType == null)
		{
			logger.warn("VIRAL-VIRAL_PROMO_TYPE is configured");
			writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
					viraldateFormat,isArtistBasedSmsEnabled, "no");
			return;
		}

		String corporateTunes =  WebServiceConstants.CORPORATE_TUNES;
		if (clip!=null && clip.getAlbum() != null) {
			String album = clip.getAlbum();
			if (album != null
					&& blockViralSms
					&& (nameTunes.contains(album.toLowerCase().trim()) || (album
							.toLowerCase()).contains(corporateTunes
							.toLowerCase()))) {
				logger.info("Viral sms is blocked for name tune & coporate tune:"
						+ album);
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}
		}
		//RBT 12928 -Changes done for allow viral default song selection
		//Default Clip, not make song sel, active user (A,N,B,G,Z,D,P) -- reject.
		String defaultClipId = RBTParametersUtils.getParamAsString("COMMON",
				"DEFAULT_CLIP", null);
		boolean makeDefaultSong = RBTParametersUtils.getParamAsBoolean(
				"GATHERER", "INSERT_DEFAULT_SEL", "false");
		boolean isDefaultSong = false;
		if (rbtWavFile.contains("default") && null != defaultClipId
				&& !defaultClipId.isEmpty()) {
			clip = rbtCacheManager.getClip(defaultClipId);
			if (null != clip) {
				rbtWavFile = clip.getClipRbtWavFile();
				isDefaultSong =true;
			}


		}

//		String circleID = promotionRequest.getCircleID();
		String callerLanguage = promotionRequest.getCallerLanguage();

		if (logger.isDebugEnabled())
		{
			logger.debug("System Time: " + new Date() + ", Called Time: "
					+ new Date(calledTime));
		}

		if (isPromotionRequestExpired(calledTime))
		{
			logger.info("Request expired");
			writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
					viraldateFormat,isArtistBasedSmsEnabled, "no");
			return;
		}
		String biResponse = null;
		Subscriber caller = null;
		
		String operatorUserType = null;
		OperatorUserDetails operatorUserDetails = null;
		try {
			// Getting user details from B2B db cache
			IUserDetailsService operatorUserDetailsService = (IUserDetailsService) ConfigUtil
					.getBean(BeanConstant.USER_DETAIL_BEAN);


			if (operatorUserDetailsService != null && callerID != null) {
				operatorUserDetails = (OperatorUserDetailsImpl) operatorUserDetailsService
						.getUserDetails(callerID);
			}

			if (operatorUserDetails != null && operatorUserDetails.serviceKey() != null) {
				operatorUserType = operatorUserDetails.serviceKey();
			}else{
				operatorUserType = OperatorUserTypes.NEW_USER.getDefaultValue();
				operatorUserDetails = new OperatorUserDetailsImpl(callerID, operatorUserType, null, null, null);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		if (promotionRequest.isValidationRequired()) {

			if (isBlackOutPeriodNow()) {
				logger.info("Blackout Period, so not processing the request");
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}

			if (!isCallerListnedRBTForMinDuration(callDuration)){
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}
			if (!isClipPromotable(clip, rbtWavFile)){
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}	
			//RBT 12928 -Changes done for allow viral default song selection
			caller = rbtDBManager.getSubscriber(callerID);
			//Default Clip, not make song sel, active user (A,N,B,G,Z,D,P) -- reject.
			logger.debug("isDefaultSong: " + isDefaultSong
					+ " makeDefaultSong : " + makeDefaultSong + "ActiveUser: "
					+ isUserActive(caller));
			if (isDefaultSong && isUserActive(caller) && !makeDefaultSong){
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}
			if (alreadyPromotedMaxTimesOrPromotedClip(callerID,
					clip.getClipId(), caller))
			{
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}
			callerLanguage = (caller != null) ? caller.language() : null;

			circleID = validateCallerAndGetCircleID(promotionRequest, caller, operatorUserDetails);
			if (circleID == null) {
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID, viraldateFormat, isArtistBasedSmsEnabled,
						"no");
				return;
			}

			//			only if the bi response is null or true , we will insert in database
			biResponse = makeAHitToBI(calledID, clip,caller, callerID);
			if(biResponse == null || biResponse.indexOf("false")!= -1)
			{
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				return;
			}
			if (isTpsReached())
			{
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				logger.info("tps of " + tps + " is reached, ignoring request of "+promotionRequest.getCallerID());
				return;
			}

			if (delayTimeInMins > 0
					&& (System.currentTimeMillis() - calledTime) < (delayTimeInMins * 60 * 1000))
			{

				// Now validation is already done, so no need to do it again
				// when its ready for promotion. This information will be
				// persisted in extra info as VALIDATED="TRUE"
				promotionRequest.setValidationRequired(false);
				promotionRequest.setCallerLanguage(callerLanguage);

				//for deleting the previous info if BI-URL is configured
				if(biResponse!=null && biResponse.indexOf("true")!=-1){
					deleteViralData(promotionRequest, "VIRAL_PENDING,BASIC", circleID, null);
				}
				// Promotion message has to be sent after configured number of
				// minutes, so inserting in RBT_VIRAL_SMS_TABLE
				if(!ViralPromotionRequest.isTpsReached()) {
					addViralData(promotionRequest, "VIRAL_PENDING", null, circleID, null);
//					ViralPromotionRequest.rejectRequestCounter++;
					logger.debug("Promotion delayed, so inserted in to RBT_VIRAL_SMS_TABLE");
				}
				return;
			}
		}
		else
		{
			if (isTpsReached())
			{
				writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
						viraldateFormat,isArtistBasedSmsEnabled, "no");
				logger.info("tps of " + tps + " is reached, ignoring request of "+promotionRequest.getCallerID());
				return;
			}
			caller = rbtDBManager.getSubscriber(callerID);
		}

		String clipPromotionLanguage = RBTParametersUtils.getParamAsString("VIRAL", "CLIP_PROMOTION_LANGUAGE", null);
		if(clipPromotionLanguage != null) {
			callerLanguage = clipPromotionLanguage;
		}
		//Getting clip object again because of the above parameter
		clip = rbtCacheManager.getClipByRbtWavFileName(rbtWavFile, callerLanguage);

		String tempViralPromotionType = getViralSMSType(callerID, promotionRequest);
		if(tempViralPromotionType != null) {
			viralPromotionType = tempViralPromotionType;
		}

		logger.info("ViralPromotionType: " + viralPromotionType);

		String biUrl = RBTParametersUtils.getParamAsString("VIRAL",
				"VIRAL_BI_URL", null);

		if(biUrl!=null){
			deleteViralData(promotionRequest, "VIRAL_PENDING,BASIC", circleID, viralPromotionType);
		}


		boolean allowGetOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("VIRAL", "ALLOW_GET_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		boolean allowOnlyBaseOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("VIRAL", "ALLOW_ONLY_BASE_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		String subscriptionClass = null;
		String chargeClass = null;

		/*
		 * Below parameter consists of
		 * <ViralConfirmationKeyword>,<ActivatedBy>,<SelectedBy>,<SubscriptionClass>,<CategoryID>.
		 * 
		 * Only configuring <ViralConfirmationKeyword> is mandatory, rest are optional.
		 */
		String confSubClass = null;
		String actMode = null;
		String selMode = null;
		String[] tokens = RBTParametersUtils.getParamAsString(iRBTConstant.SMS,
				Constants.VIRAL_KEYWORD, "").split(",");
		if (tokens.length > 1) {
			actMode = tokens[1].trim();
		}
		if (tokens.length > 2) {
			selMode = tokens[2].trim();
		}
		if (tokens.length > 3) {
			confSubClass = tokens[3].trim();
		}

		String userType = null;
		if(!isUserActive(caller) && (allowGetOffer || allowOnlyBaseOffer)) {
			subscriptionClass = getBaseOffer(callerID, actMode);
			if (subscriptionClass != null) {
				userType = "free";
			} else {
				userType = "no_free";
			}
		}
		if(null == subscriptionClass) {
			subscriptionClass = confSubClass;
		}

		if (allowGetOffer) {
			chargeClass = getSelOffer(callerID, clip.getClipId(), selMode);
		}
		// If the offer is enabled and it returns null then it has to hit the
		// get next charge class api.otherwise it should not hit that to bring
		// the charge class.
		if (null == chargeClass) {
			// subscribrid, clipid
			SelectionRequest selectionRequest = new SelectionRequest(callerID);
			selectionRequest.setClipID(String.valueOf(clip.getClipId()));
			com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass nextChargeClass = RBTClient
					.getInstance().getNextChargeClass(selectionRequest);
			if (null != nextChargeClass) {
				chargeClass = nextChargeClass.getChargeClass();
			}
		}

		addViralData(promotionRequest, "BASIC", clip, null, viralPromotionType);
		//RBT 12928 -Changes done for allow viral default song selection  
		
		if (viralPromotionType.equalsIgnoreCase("SMS")) {
			promoteThroughSMS(callerID, calledID, clip, callerLanguage, circleID, caller, subscriptionClass,
					chargeClass, isDefaultSong, userType, artistName, operatorUserDetails);
		} else if (viralPromotionType.equalsIgnoreCase("USSD")) {
			promoteThroughUSSD(callerID, calledID, clip, callerLanguage,
					circleID, caller, subscriptionClass, chargeClass,isDefaultSong, userType, artistName);
		} else if (viralPromotionType.equalsIgnoreCase("OBD")) {
			promoteThroughOBD(callerID, calledID, clip, callerLanguage,
					circleID, subscriptionClass, chargeClass, artistName);
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMDDHHmmss");

		if (promotionLogger != null) {
			try {
				promotionLogger.ViralPromotionLogger(callerID, calledID,
						clip.getClipId() + "", new Date());
			} catch (Exception e) {
				logger.info("Error while writing event log");
			}
		}
		writeSDRLogs(callerID, calledID, calledTime, clip, circleID,
				viraldateFormat,isArtistBasedSmsEnabled, "yes");
		
		if (viralPromotionLogger == null
				|| !viralPromotionLogger.isInfoEnabled()) {
			writeTrans(callerID, calledID, clip.getClipId(),
					dateFormat.format(new Date(calledTime)), artistName);
		}

		artistName = null;
	}

	public static void writeSDRLogs(String callerID, String calledID,
			long calledTime, Clip clip, String circleID,
			DateFormat viraldateFormat,Boolean isArtistNameEnabled,String isPromotionsent) {
		if (viralPromotionLogger != null
				&& viralPromotionLogger.isInfoEnabled()) {
			try {
				String artistName = null;
				int clipId = -1;
				if (null != clip) {
					artistName = clip.getArtist();
					clipId = clip.getClipId();
				}
			if(isArtistNameEnabled && artistName != null){
				viralPromotionLogger.info(viraldateFormat.format(new Date(
						calledTime))
						+ ","
						+ circleID
						+ ","
						+ callerID
						+ ","
						+ calledID + "," + clipId + "," + isPromotionsent + "," + artistName );
			}else{
				viralPromotionLogger.info(viraldateFormat.format(new Date(
						calledTime))
						+ ","
						+ circleID
						+ ","
						+ callerID
						+ ","
						+ calledID + "," + clipId + "," + isPromotionsent  );
				
			}
			} catch (Exception e) {
				logger.info("Error while writing viralPromotionLogger log");
			}
		}
	}

	private static boolean isTpsReached()
	{
		if(tps == 0)			// TPS restriction not enabled, accept all requests
			return false;

		synchronized (lock)
		{
			if(intervalStart + 1000 > System.currentTimeMillis())
			{
				requestCounter++;
				if(requestCounter > tps)
					return true;
				else
					return false;
			}
			else
			{
				intervalStart = System.currentTimeMillis();
				requestCounter = 1;
				return false;
			}	
		}
	}

	private static boolean isPromotionRequestExpired(long calledTime)
	{
		int validityPeriod = RBTParametersUtils.getParamAsInt("VIRAL",
				"REQUEST_VALIDITY_PERIOD_IN_MINUTES", 0);

		if (logger.isDebugEnabled())
			logger.debug("validityPeriod: " + validityPeriod);

		if (validityPeriod == 0)
			return false;

		long validityPeriodInMillis = validityPeriod * 60 * 1000;
		return ((System.currentTimeMillis() - calledTime) >= validityPeriodInMillis);
	}

	/**
	 * @param callerID
	 */
	private static boolean alreadyPromotedMaxTimesOrPromotedClip(
			String callerID, int clipID, Subscriber caller)
	{
		String[] dataTypes = { "BASIC", "VIRAL_EXPIRED", "VIRAL_OPTOUT" };
		ViralSMSTable[] viralDatas = rbtDBManager
				.getViralSMSByTypesForSubscriber(callerID, dataTypes);
		if (viralDatas != null)
		{
			int viralPeriod = RBTParametersUtils.getParamAsInt("VIRAL",
					"OLDVIRAL_CLEANING_PERIOD_IN_HRS", 336);
			
			int viralPeriodWeek = RBTParametersUtils.getParamAsInt("VIRAL",
					"VIRAL_CLEANING_PERIOD_IN_HRS_FOR_WEEK", viralPeriod);
			
			long viralPeriodInMillis = viralPeriod * 60 * 60 * 1000L;
			long viralPeriodWeekInMillis = viralPeriodWeek * 60 * 60 * 1000L;
			
			String clipIDStr = String.valueOf(clipID);
			int noOfTimesPromoted = 0;
			for (ViralSMSTable viralData : viralDatas)
			{
				if ((System.currentTimeMillis() - viralData.sentTime()
						.getTime()) < viralPeriodInMillis)
				{
					if (clipIDStr.equals(viralData.clipID()))
					{
						logger.info("caller " + callerID
								+ " already promoted with the clip " + clipID);
						return true;
					}
					
					
					if ((System.currentTimeMillis() - viralData.sentTime()
							.getTime()) < viralPeriodWeekInMillis){
						noOfTimesPromoted++;
					}
				}
			}

			boolean isActive = isUserActive(caller);

			int maxPromotionsAllowed = RBTParametersUtils.getParamAsInt(
					"VIRAL", "MAX_VIRAL_PROMOTION_FOR_CALLER_PER_CYCLE", 1);

			if(isActive) {
				maxPromotionsAllowed = RBTParametersUtils.getParamAsInt(
						"VIRAL", "MAX_VIRAL_PROMOTION_FOR_CALLER_PER_CYCLE_ACTIVE_USER", maxPromotionsAllowed);
			}

			boolean alreadyPromoted = noOfTimesPromoted >= maxPromotionsAllowed;
			if (alreadyPromoted)
			{
				logger.info("caller " + callerID
						+ " already promoted maximum times");
				return true;
			}
		}

		return false;
	}

	/**
	 * @param calledID
	 * @param clip
	 * @param callerLanguage
	 * @param circleID
	 * @param caller
	 * @param userType 
	 * @return
	 */
	// RBT 12928 -Changes done for allow viral default song selection
	private static String getViralSMS(String calledID, Clip clip, String callerLanguage, String circleID,
			Subscriber caller, String subscriptionClass, String chargeClass, boolean isDefaultSong, String userType,
			StringBuffer artistName, String callerID, String operatorUserType) {
			boolean isActive = isUserActive(caller);
			String sms = null;
			logger.debug("Received params. calledID: " + calledID + ", clip: " + clip + ", callerLanguage: "
					+ callerLanguage + ", circleID: " + circleID + ", subscriptionClass: " + subscriptionClass
					+ ", chargeClass: " + chargeClass);

			if (operatorUserType != null) {
				String param = null;
				if (operatorUserType.equalsIgnoreCase(OperatorUserTypes.LEGACY.toString())) {
					param = "MESSAGE_" + OperatorUserTypes.LEGACY.toString().toUpperCase() + "_USER";
				} else if (operatorUserType.equalsIgnoreCase(OperatorUserTypes.NEW_USER.toString())) {
					param = "MESSAGE_" + OperatorUserTypes.NEW_USER.toString().toUpperCase() + "_USER";
				} else if (operatorUserType.equalsIgnoreCase(OperatorUserTypes.TRADITIONAL.toString())) {
					param = "MESSAGE_" + OperatorUserTypes.TRADITIONAL.toString().toUpperCase() + "_USER";
				} else if (operatorUserType.equalsIgnoreCase(OperatorUserTypes.FREE_APP_USER.toString())
						|| operatorUserType.equalsIgnoreCase(OperatorUserTypes.PAID_APP_USER_LOW_BALANCE.toString())) {
					param = "MESSAGE_" + OperatorUserTypes.FREE_APP_USER.toString().toUpperCase() + "_USER";
				} else {
					param = "MESSAGE_" + OperatorUserTypes.PAID_APP_USER.toString().toUpperCase() + "_USER";
				}
				sms = getSmsTextForViral("VIRAL", param, callerLanguage, circleID, isDefaultSong, clip, artistName);
				logger.debug("Configured sms text for param: " + param + " is: " + sms);

			} else if (sms == null) {
				if (circleID != null) {
					if (isActive) {

						// RBT-10785: subscriber is migrated user. get sms text.
						String subClass = caller.subscriptionClass();
						if (migratedUserSubClassesList.contains(subClass)) {

							String param = "MESSAGE_" + circleID.toUpperCase() + "_" + subClass.toUpperCase()
									+ "_ACTIVE_USER";

							sms = getSmsTextForViral("VIRAL", param, callerLanguage, circleID, isDefaultSong, clip,
									artistName);

							logger.debug("Configured sms text for param: " + param + " is: " + sms + ", caller language: "
									+ caller.language() + ", caller circle id: " + caller.circleID());
						}

						if (null == sms && chargeClass != null) {
							sms = getSmsTextForViral("VIRAL",
									"MESSAGE_" + circleID.toUpperCase() + "_" + chargeClass.toUpperCase() + "_ACTIVE_USER",
									callerLanguage, circleID, isDefaultSong, clip, artistName);
						}

						if (sms == null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE_" + circleID.toUpperCase() + "_ACTIVE_USER",
									callerLanguage, circleID, isDefaultSong, clip, artistName);
						}
					}

					if (sms == null) {
						sms = getSmsTextForViral("VIRAL", "MESSAGE_" + circleID.toUpperCase() + "_" + userType,
								callerLanguage, circleID, isDefaultSong, clip, artistName);
						if (subscriptionClass != null && sms == null) {
							sms = getSmsTextForViral("VIRAL",
									"MESSAGE_" + circleID.toUpperCase() + "_" + subscriptionClass.toUpperCase(),
									callerLanguage, circleID, isDefaultSong, clip, artistName);
						}

						if (sms == null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE_" + circleID.toUpperCase(), callerLanguage, circleID,
									isDefaultSong, clip, artistName);
						}
					}
				}

				if (sms == null) {
					if (isActive) {

						// RBT-10785: subscriber is migrated user. get sms text.
						String subClass = caller.subscriptionClass();
						if (migratedUserSubClassesList.contains(subClass)) {

							String param = "MESSAGE_ACTIVE_USER_" + subClass.toUpperCase();

							sms = getSmsTextForViral("VIRAL", param, callerLanguage, circleID, isDefaultSong, clip,
									artistName);

							logger.debug("Configured sms text for param: " + param + " is: " + sms + ", caller language: "
									+ caller.language() + ", caller circle id: " + caller.circleID());
						}

						if (sms == null && chargeClass != null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE_ACTIVE_USER_" + chargeClass.toUpperCase(),
									callerLanguage, circleID, isDefaultSong, clip, artistName);
						}
						if (sms == null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE_ACTIVE_USER", callerLanguage, circleID,
									isDefaultSong, clip, artistName);
						}
					}

					if (sms == null) {
						sms = getSmsTextForViral("VIRAL", "MESSAGE" + "_" + userType, callerLanguage, circleID,
								isDefaultSong, clip, artistName);
						if (subscriptionClass != null && sms == null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE_" + subscriptionClass.toUpperCase(), callerLanguage,
									circleID, isDefaultSong, clip, artistName);
						}
						if (sms == null) {
							sms = getSmsTextForViral("VIRAL", "MESSAGE", callerLanguage, circleID, isDefaultSong, clip,
									artistName);
						}
					}
				}
			}

			// RBT-10785: OI deployment
			if (null != subscriptionClass) {
				SubscriptionClass subscriptionClassDB = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(subscriptionClass);
				if (null != subscriptionClassDB) {
					String baseAmount = subscriptionClassDB.getSubscriptionAmount();
					if (null != baseAmount) {
						baseAmount = getInLocalCurrencyFormat(baseAmount);
						while (sms.indexOf("%SUB_PRICE") != -1) {
							sms = sms.substring(0, sms.indexOf("%SUB_PRICE")) + baseAmount
									+ sms.substring(sms.indexOf("%SUB_PRICE") + 10);
						}
						logger.debug("Replaced subscription amount: " + baseAmount + " for %SUB_PRICE%. sms: " + sms);
					}
				} else {
					logger.warn("Offer SrvKey is not configured in " + "SubscriptionClass table. subscriptionClass: "
							+ subscriptionClass + ", not replacing subscription amount for" + " %SUB_PRICE%");
				}
			}

			if (null != chargeClass) {
				ChargeClass chargeClassDB = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
				if (null != chargeClassDB) {
					String selAmount = chargeClassDB.getAmount();
					String renewalAmount = null, renewalPeriod = null, freePeriodText = null, specialAmtChar = null;
					if (null != selAmount) {
						specialAmtChar = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
								"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
						if (null != sms && sms.contains("%FREE_CHARGE_TEXT%")
								&& Double.parseDouble(selAmount.replace(specialAmtChar, ".")) == 0) {
							renewalAmount = chargeClassDB.getRenewalAmount();
							renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility
									.getSubscriptionPeriodInDays(chargeClassDB.getSelectionPeriod());
							freePeriodText = CacheManagerUtil.getParametersCacheManager()
									.getParameterValue(iRBTConstant.COMMON, "FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
							freePeriodText = freePeriodText.replace("DD", renewalPeriod);
							if (renewalAmount != null) {
								renewalAmount = getInLocalCurrencyFormat(renewalAmount);
							}
							while (sms.indexOf("%PRICE") != -1) {
								sms = sms.substring(0, sms.indexOf("%PRICE")) + renewalAmount
										+ sms.substring(sms.indexOf("%PRICE") + 6);
							}
							sms = sms.replaceFirst("%FREE_CHARGE_TEXT%", freePeriodText);
						} else {
							selAmount = getInLocalCurrencyFormat(selAmount);
							while (sms.indexOf("%PRICE") != -1) {
								sms = sms.substring(0, sms.indexOf("%PRICE")) + selAmount
										+ sms.substring(sms.indexOf("%PRICE") + 6);
							}
							sms = sms.replaceFirst("%FREE_CHARGE_TEXT%", "");
							logger.debug("Replaced selection amount: " + selAmount + " for %PRICE%. sms: " + sms);
						}
					}
				} else {
					logger.warn("Offer SrvKey is not configured in " + "ChargeClass table. chargeClass: " + chargeClass
							+ ", not replacing selection amount for %PRICE%");
				}
			}
			// RBT-10785: OI deployment ends
			String clipArtistName = "";
			String songName = clip.getClipName();
			if (clip.getArtist() != null) {
				clipArtistName = clip.getArtist();
			}

			int configuredArtistLength = Integer.parseInt(CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.SMS, "ARTIST_NAME_LENGTH", "0"));
			int configuredSongLength = Integer.parseInt(CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.SMS, "SONG_NAME_LENGTH", "0"));
			if (configuredArtistLength > 0 && clipArtistName != null && !clipArtistName.isEmpty()
					&& configuredArtistLength <= clipArtistName.length()) {
				clipArtistName = clipArtistName.substring(0, configuredArtistLength);
				clipArtistName = clipArtistName.trim();
			}

			if (configuredSongLength > 0 && songName != null && !songName.isEmpty()
					&& configuredSongLength <= songName.length()) {
				songName = songName.substring(0, configuredSongLength);
				songName = songName.trim();
			}

			sms = sms.replaceFirst("%ARTIST%", clipArtistName);
			sms = sms.replaceFirst("%SONG%", songName);
			sms = sms.replaceFirst("%CALLED%", calledID);
			String promoId = clip.getClipPromoId();
			sms = sms.replaceFirst("%PROMO_ID%", promoId == null ? "" : promoId);
			String album = clip.getAlbum();
			sms = sms.replaceFirst("%ALBUM%", album == null ? "" : album);

			String contestEndTime = ContestUtils.getContestEndTime();
			if (contestEndTime != null)
				sms = sms.replaceFirst("%CONTEST_HOUR%", contestEndTime);

			logger.debug("Returning sms: " + sms + ", calledID: " + calledID + ", clip: " + clip + ", callerLanguage: "
					+ callerLanguage + ", circleID: " + circleID + ", subscriptionClass: " + subscriptionClass
					+ ", chargeClass: " + chargeClass);
			return sms;
		}

	// RBT 12928 -Changes done for allow viral default song selection
	private static String getSmsTextForViral(String type, String param,
			String callerLanguage, String circleID, boolean isDefaultSong,
			Clip clip,StringBuffer artistNameToLog) {
		String oldparam = param;
		if (isDefaultSong) {
			param = param + "_FOR_DEFAULT_SONG_SELECTION";
		}
		// Start:RBT-14152- TMCO. RBT. Custom Viral SMS.Artist based sms enabled
		// for the user.
		String artistNames = "";
		int artistBasedSMSCount = 0;
		logger.info("oldparam: " + oldparam + " newparam: " + param);
		String sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type,
				param, callerLanguage, circleID);
		logger.info("sms Text" + sms + " for param : " + param);
		String separater = RBTParametersUtils.getParamAsString("VIRAL",
				"SEPARATOR_FOR_ARTIST_INFO", ",");
		logger.info("isArtistBasedSmsEnabled" + isArtistBasedSmsEnabled);
		if (isArtistBasedSmsEnabled && clip != null && clip.getArtist() != null
				&& !clip.getArtist().isEmpty()) {
			String artistSmsParam = param;
			artistNames = clip.getArtist();
			String[] artist = artistNames.split(separater);
			logger.debug("artist list" + artist);
			String smsText = null;
			for (String artistName : artist) {
				artistSmsParam = param + "_" + artistName.trim().toUpperCase();
				String smsTextByArtist = CacheManagerUtil
						.getSmsTextCacheManager().getSmsText(type,
								artistSmsParam, callerLanguage, circleID);
				if (smsTextByArtist != null && !smsTextByArtist.isEmpty()) {
					artistBasedSMSCount++;
					smsText = smsTextByArtist;
					artistNameToLog.append(artistName);
				}
			}
			logger.debug("artistBasedSMSCount: " + artistBasedSMSCount
					+ " smsText: " + smsText);
			if (artistBasedSMSCount == 1) {
				sms = smsText;
			} else {
				artistNameToLog = (artistNameToLog.length() > 0) ? artistNameToLog
						.delete(0, artistNameToLog.length()) : artistNameToLog;
			}
		}
		// End:RBT-14152- TMCO. RBT. Custom Viral SMS.Artist based sms enabled
		// for the user.
		if (sms == null) {
			param = oldparam;
			sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type,
					oldparam, callerLanguage, circleID);
			logger.debug("sms" + sms);
		}
		logger.debug("Returning sms: " + sms + " param : " + param
				+ " isDefaultSong : " + isDefaultSong);
		return sms;
	}

	/**
	 * @param callerID
	 * @param calledID
	 * @param clip
	 * @param callerLanguage
	 * @param circleID
	 * @param caller
	 * @param userType 
	 */
	private static void promoteThroughSMS(String callerID, String calledID, Clip clip, String callerLanguage,
			String circleID, Subscriber caller, String subscriptionClass, String chargeClass, boolean isDefaultSong,
			String userType, StringBuffer artistName, OperatorUserDetails operatorUserDetails) {
		
		String operatorUserType = operatorUserDetails == null ? null : operatorUserDetails.serviceKey();
		
		String senderNo = RBTParametersUtils.getParamAsString("VIRAL", "SMS_SENDER_NO", "123");
		String smsUrl = RBTParametersUtils.getParamAsString("VIRAL", "SMS_DND_URL", null);
		String sms = getViralSMS(calledID, clip, callerLanguage, circleID, caller, subscriptionClass, chargeClass,
				isDefaultSong, userType, artistName, callerID, operatorUserType);
		if (smsUrl == null) {
			try {
				boolean smsSent = false;
				if(operatorUserDetails != null) {
	 				String[] operator_circle = ServiceUtil.getOperatorAndCircleId(circleID, operatorUserDetails);
					if (operator_circle != null && operator_circle.length == 2) {
						Tools.sendSMS(senderNo, callerID, sms, operator_circle[1], operator_circle[0], false);
						smsSent = true;
					}
				}
								
				if(!smsSent){
					Tools.sendSMS(senderNo, callerID, sms,false);
				}
			} catch (OnMobileException e) {
			}
		}
		else
		{
			smsUrl = smsUrl.replaceFirst("\\$sender\\$", senderNo);
			smsUrl = smsUrl.replaceFirst("\\$receiver\\$", callerID);
			smsUrl = smsUrl.replaceFirst("\\$smstext\\$",
					getEncodedUrlString(sms));
			Tools.callURL(smsUrl, new Integer(-1), new StringBuffer(),
					false, null, -1, false, 2000);
		}
	}

	/**
	 * @param callerID
	 * @param calledID
	 * @param clip
	 * @param callerLanguage
	 * @param circleID
	 * @param caller
	 * @param userType 
	 */
	private static void promoteThroughUSSD(String callerID, String calledID,
			Clip clip, String callerLanguage, String circleID, Subscriber caller, String subscriptionClass, String chargeClass,boolean isDefaultSong, String userType, StringBuffer artistName)
	{
		String ussdUrl = RBTParametersUtils.getParamAsString("VIRAL",
				"USSD_URL", null);
		if (ussdUrl == null)
		{
			logger.warn("VIRAL-USSD_URL not configured !!!!!");
			return;
		}

		String senderNo = RBTParametersUtils.getParamAsString("VIRAL", "SMS_SENDER_NO", "123");
		String sms = getViralSMS(calledID, clip, callerLanguage, circleID, caller, subscriptionClass, chargeClass,
				isDefaultSong, userType, artistName, callerID , null);

		ussdUrl = ussdUrl.replaceFirst("\\$sender\\$", senderNo);
		ussdUrl = ussdUrl.replaceFirst("\\$receiver\\$", callerID);
		ussdUrl = ussdUrl.replaceFirst("\\$smstext\\$",
				getEncodedUrlString(sms));
		Tools.callURL(ussdUrl, new Integer(-1), new StringBuffer(), false,
				null, -1, false, 2000);
	}

	/**
	 * @param callerID
	 * @param calledID
	 * @param clip
	 * @param callerLanguage
	 * @param circleID
	 */
	private static void promoteThroughOBD(String callerID, String calledID,
			Clip clip, String callerLanguage, String circleID, String subscriptionClass, String chargeClass,StringBuffer artistName)
	{
		String obdUrl = RBTParametersUtils.getParamAsString("VIRAL", "OBD_URL",
				null);
		if (obdUrl == null)
		{
			logger.warn("VIRAL-OBD_URL not configured, ignoring");
			return;
		}

		obdUrl = obdUrl.replace("$dialingNumber$", calledID);
		obdUrl = obdUrl.replace("$called$", calledID);
		obdUrl = obdUrl.replace("$receiver$", callerID);
		obdUrl = obdUrl.replace("$clipId$", String.valueOf(clip.getClipId()));
		obdUrl = obdUrl.replace("$clipName$", encodeParam(clip.getClipName()));
		obdUrl = obdUrl.replace("$clipWavFile$",
				encodeParam(clip.getClipRbtWavFile()));

		String language = callerLanguage;
		if (language == null)
		{
			// getting language from site prefix table
			SitePrefix sitePrefix = CacheManagerUtil
					.getSitePrefixCacheManager().getSitePrefixes(circleID);
			if (sitePrefix != null)
				language = sitePrefix.getSiteLanguage();
		}

		if (language != null)
			obdUrl = obdUrl.replace("$language$", language);
		else
			logger.warn("Couldn't get language for caller-" + callerID);

		Integer status = new Integer(-1);
		StringBuffer response = new StringBuffer();
		Tools.callURL(obdUrl, status, response, false, null, -1, false, 2000);

		logger.debug("url-" + obdUrl + ", status-" + status + ", response-"
				+ response.toString());
	}

	private static void initializeTestNumbers()
	{
		String testNoConfig = RBTParametersUtils.getParamAsString("VIRAL",
				"TEST_ON", null);
		if (testNoConfig == null)
		{
			logger.info("Test Numbers parameter VIRAL-TEST_ON is not defined, so dissabled testing mode.");
			return;
		}

		String[] configTokeks = testNoConfig.split(",");
		if (configTokeks.length < 2)
		{
			logger.info("Test Numbers parameter VIRAL-TEST_ON is not defined properly, so dissabled testing mode.");
			return;
		}

		if (configTokeks[0].equalsIgnoreCase("TRUE"))
		{
			testNumers = new HashSet<String>();
			File testNumberFile = new File(configTokeks[1]);
			if (testNumberFile.exists())
			{
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new FileReader(testNumberFile));
					while (reader.ready())
						testNumers.add(Utility.trimCountryPrefix(reader
								.readLine().trim()));

					logger.info("testNumers: " + testNumers);
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
				finally
				{
					if (reader != null)
					{
						try
						{
							reader.close();
						}
						catch (IOException e)
						{
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	private static void initializeBlockedContents()
	{
		String blockedClipIDs = RBTParametersUtils.getParamAsString("VIRAL",
				"COPY_BLOCKED_CLIP_IDS", null);

		blockedClips = new HashSet<String>();
		if (blockedClipIDs != null)
		{
			String[] clipIDs = blockedClipIDs.split(",");
			for (String clipID : clipIDs)
			{
				blockedClips.add(clipID);
			}
		}

		String blockedCategoryIDs = RBTParametersUtils.getParamAsString(
				"VIRAL", "COPY_BLOCKED_CATEGORY_IDS", "1,99");
		blockedMappingClips = new HashSet<String>();
		String[] categoryIDs = blockedCategoryIDs.split(",");
		for (String categoryID : categoryIDs)
		{
			try
			{
				int catID = Integer.parseInt(categoryID);
				Clip[] clips = rbtCacheManager.getClipsInCategory(catID);
				if (clips != null)
				{
					for (Clip clip : clips)
					{
						blockedMappingClips.add(clip.getClipRbtWavFile());
					}
				}
			}
			catch (Exception e)
			{
			}
		}

		logger.info("blockedClips: " + blockedClips);
		logger.info("blockedMappingClips: " + blockedMappingClips);
	}

	private static void initTrans()
	{

		String sdrWorkingDir = RBTParametersUtils.getParamAsString("VIRAL",
				"VIRAL_TRANS_PATH", ".");

		ArrayList<String> headers = new ArrayList<String>();
		headers.add("CALLER");
		headers.add("CALLED");
		headers.add("CLIP");
		headers.add("TIME OF CALL");
		if (isArtistBasedSmsEnabled) {
			headers.add("ARTIST NAME");
		}
		writeTrans = new WriteDailyTrans(sdrWorkingDir, "VIRAL_REQUEST",
				headers);
	}

	private static void initializeBlackOut()
	{
		blackoutTimesList = new ArrayList<List<Integer>>();
		for (int i = 0; i <= 7; i++)
			blackoutTimesList.add(new ArrayList<Integer>());

		String blackoutTimes = RBTParametersUtils.getParamAsString("VIRAL",
				"BLACK_OUT_PERIOD", null);
		if (blackoutTimes == null)
		{
			logger.info("No BlackOut Configured");
			return;
		}

		String[] blackoutTokens = blackoutTimes.split(",");
		for (String blackout : blackoutTokens)
		{
			if (!blackout.contains("["))
			{
				logger.info("No BlackOut Time Configured" + blackout);
				continue;
			}

			List<Integer> days = getDays(blackout.substring(0,
					blackout.indexOf("[")));
			if (days != null && days.size() > 0)
			{
				List<Integer> times = getTimes(blackout.substring(blackout
						.indexOf("[")));
				for (int j = 0; j < days.size(); j++)
					blackoutTimesList.set(days.get(j).intValue(), times);
			}
		}

		logger.info("blackoutTimesList initialized " + blackoutTimesList);
	}

	private static List<Integer> getDays(String string)
	{
		List<Integer> daysList = new ArrayList<Integer>();

		Map<String, Integer> days = new HashMap<String, Integer>();
		days.put("SUN", 1);
		days.put("MON", 2);
		days.put("TUE", 3);
		days.put("WED", 4);
		days.put("THU", 5);
		days.put("FRI", 6);
		days.put("SAT", 7);

		if (string.contains("-"))
		{
			try
			{
				String day1 = string.substring(0, string.indexOf("-"));
				String day2 = string.substring(string.indexOf("-") + 1);
				if (!days.containsKey(day1) || !days.containsKey(day2))
				{
					logger.info("Invalid week specified !!!!" + string);
					return null;
				}

				int startDay = days.get(day1);
				int endDay = days.get(day2);

				if (endDay > startDay)
				{
					for (int t = startDay; t <= endDay; t++)
						daysList.add(t);
				}
				else
				{
					for (int t = startDay; t <= 7; t++)
						daysList.add(t);

					for (int t = 1; t <= endDay; t++)
						daysList.add(t);
				}
			}
			catch (Throwable e)
			{
				logger.debug(e.getMessage(), e);
			}
		}
		else
		{
			if (days.containsKey(string))
				daysList.add(days.get(string));
			else
				logger.info("Invalid week specified !!!!" + string);
		}

		logger.info("DaysList" + daysList);
		return daysList;
	}

	private static List<Integer> getTimes(String string)
	{
		List<Integer> timesList = new ArrayList<Integer>();

		string = string.substring(1, string.length() - 1);
		String[] tokens = string.split(";");
		for (String token : tokens)
		{
			if (token.contains("-"))
			{
				try
				{
					int startTime = Integer.parseInt(token.substring(0,
							token.indexOf("-")));
					int endTime = Integer.parseInt(token.substring(token
							.indexOf("-") + 1));

					if (startTime > 23 || endTime > 23)
					{
						logger.info("Invalid time specified !!!!" + string);
						continue;
					}
					else if (endTime > startTime)
						for (int t = startTime; t <= endTime; t++)
							timesList.add(t);
					else
					{
						for (int t = startTime; t <= 23; t++)
							timesList.add(t);

						for (int t = 0; t <= endTime; t++)
							timesList.add(t);
					}
				}
				catch (Throwable e)
				{
					logger.debug(e.getMessage(), e);
				}
			}
			else
			{
				try
				{
					int n = Integer.parseInt(token);
					if (n >= 0 && n <= 23)
						timesList.add(n);
					else
						logger.info("Invalid time specified !!!!" + string);
				}
				catch (Throwable t)
				{
					logger.info("Invalid time specified !!!!" + string);
				}
			}
		}

		return timesList;
	}

	public static boolean isBlackOutPeriodNow()
	{
		Calendar calendar = Calendar.getInstance();
		List<Integer> blackout = blackoutTimesList.get(calendar
				.get(Calendar.DAY_OF_WEEK));

		if (logger.isDebugEnabled())
			logger.debug("BlackOut checked against " + blackout);

		return (blackout.contains(calendar.get(Calendar.HOUR_OF_DAY)));
	}

	private static void initAllowedPrefixes()
	{
		String allowedPrefixesStr = RBTParametersUtils.getParamAsString(
				"VIRAL", "ALLOWED_PREFIXES", null);
		if (allowedPrefixesStr != null)
		{
			allowedPrefixesSet = new HashSet<String>();
			String[] prefixes = allowedPrefixesStr.split(",");
			for (String prefix : prefixes)
			{
				prefix = prefix.trim();
				if (!prefix.equals(""))
					allowedPrefixesSet.add(prefix);
			}
		}
	}

	private static String validateCallerAndGetCircleID(ViralPromotionRequest promotionRequest, Subscriber caller, OperatorUserDetails operatorUserDetails) {
		String callerID = promotionRequest.getCallerID();
		String calledID = promotionRequest.getCalledID();
		String rbtWavFile = promotionRequest.getRbtWavFile();

		if (testNumers != null && !testNumers.contains(callerID))
		{
			logger.info("Testing mode is enabled and caller " + callerID
					+ " is not in the test number list.");
			return null;
		}

		String umpDNDUrl = RBTParametersUtils.getParamAsString("VIRAL", "UMP_DND_URL_FOR_VIRAL_PROMOTION", null);
		if (umpDNDUrl != null)
		{
			umpDNDUrl = umpDNDUrl.replaceFirst("%SUBSCRIBER_ID%", callerID);
			StringBuffer response = new StringBuffer();
			boolean success = Tools.callURL(umpDNDUrl, new Integer(-1), response, false, null,
					-1, false, 2000);

			if (response.toString().trim().equalsIgnoreCase("TRUE") || !success)
			{
				// if UMP url returns 'TRUE' or error status code or if UMP server is down, number is considered as DND 
				logger.info("Not promoting as the number is DND in UMP");
				return null;
			}
		}
		else if (RBTParametersUtils.getParamAsBoolean("DAEMON",
				"PROCESS_VIRAL_WHITE_LIST", "FALSE"))
		{
			// To allow only white listed subscribers check the subscriber
			// is in cache or not. All the white listed subscribers are 
			// loaded in cache during initialization.
			if (null == mc.get(callerID)) {
				logger.debug("Since Caller: " + callerID
						+ " is not whitelisted, drop the request");
				return null;
			}
		}
		else if (RBTParametersUtils.getParamAsBoolean("VIRAL",
				"CHK_RBT_DND", "TRUE") && (rbtDBManager.isViralBlackListSub(callerID)
						|| rbtDBManager.isTotalBlackListSub(callerID)))
		{
			logger.info("Caller " + callerID
					+ " is blacklisted in RBT for viral or total.");
			return null;

		}


		String circleID = null;
		String operatorUserType = operatorUserDetails != null ? operatorUserDetails.serviceKey() : null;
		if (caller != null) {
			circleID = caller.circleID();

			if (!caller.subYes().equalsIgnoreCase(
					iRBTConstant.STATE_DEACTIVATED))
			{
				boolean checkCallerStatus = RBTParametersUtils
						.getParamAsBoolean("VIRAL", "CHK_RBT_INACTIVE", "TRUE");
				if (checkCallerStatus)
				{
					logger.info("Caller " + callerID
							+ " already subscriber, so not promoting.");
					return null;
				}
				else
				{
					// Checking whether caller has already set the song and same
					// check is done only if caller is not deactive.
					SubscriberStatus[] settings = rbtDBManager
							.getAllActiveSubscriberSettings(callerID);
					if (settings != null)
					{
						for (SubscriberStatus setting : settings)
						{
							if (setting.callerID() == null
									&& rbtWavFile.equalsIgnoreCase(setting
											.subscriberFile()))
							{
								logger.info("Caller " + callerID
										+ " is already set the clip "
										+ rbtWavFile);
								return null;
							}
						}
					}
				}
			}
		}else if(operatorUserType  != null && (operatorUserType.equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue())
				|| operatorUserType.equalsIgnoreCase(OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue()))) {
			circleID = operatorUserDetails.circleId();
			boolean checkCallerStatus = RBTParametersUtils.getParamAsBoolean("VIRAL", "CHK_RBT_INACTIVE", "TRUE");
			boolean getAllActiveSubscriberSelection = RBTParametersUtils.getParamAsBoolean("VIRAL", "CHK_ALL_ACTIVE_SELECTION_FOR_B2B_USER", "FALSE");
			if (checkCallerStatus ) {
				logger.info("Caller " + callerID + " already subscriber, so not promoting.");
				return null;
			} else if(getAllActiveSubscriberSelection){
				// Checking whether caller has already set the song and same
				// check is done only if caller is not deactive.
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerID);
				Settings settings = RBTClient.getInstance().getSettings(rbtDetailsRequest);
				if (settings != null) {
					for (Setting setting : settings.getSettings()) {
						if ((setting.getCallerID() == null
								|| setting.getCallerID().equalsIgnoreCase(WebServiceConstants.ALL))
								&& rbtWavFile.equalsIgnoreCase(setting.getRbtFile())) {
							logger.info("Caller " + callerID + " is already set the clip " + rbtWavFile);
							return null;
						}
					}
				}
			}

		}

		boolean checkStatus = RBTParametersUtils.getParamAsBoolean("VIRAL",
				"CHK_RBT_LITE_USER", "FALSE");
		if (checkStatus)
		{
			Subscriber called = rbtDBManager.getSubscriber(calledID);

			boolean isCallerLiteUser = isLiteUser(caller);
			boolean isCalledLiteUser = isLiteUser(called);
			if (isCallerLiteUser && !isCalledLiteUser)
			{
				logger.info("Caller " + callerID
						+ " is LITE user but called " + calledID
						+ " is not LITE user.");
				return null;
			}
		}

		boolean isValidateReqAllSubscriber = RBTParametersUtils.getParamAsBoolean("VIRAL",
				"VALIDATE_SUBSCRIBER_FOR_VIRAL_PROMOTION", "FALSE");

		if (circleID == null || isValidateReqAllSubscriber)
		{
			if (allowedPrefixesSet != null)
			{
				boolean isPrefixAllowed = false;
				for (int i = callerID.length(); i > 0; i--)
				{
					String callerIDSubStr = callerID.substring(0, i);
					if (allowedPrefixesSet.contains(callerIDSubStr))
					{
						isPrefixAllowed = true;
						break;
					}
				}

				if (!isPrefixAllowed)
				{
					logger.info("Caller " + callerID + " prefix is not allowed");
					return null;
				}
			}

			SubscriberDetail subscriberDetail = RbtServicesMgr
					.getSubscriberDetail(new MNPContext(callerID, "VIRAL"));
			if (subscriberDetail == null
					|| !subscriberDetail.isValidSubscriber())
			{
				logger.info("Caller " + callerID + " is not valid");
				return null;
			}
			circleID = subscriberDetail.getCircleID();
		}


		return circleID;
	}

	private static boolean isLiteUser(Subscriber subscriber)
	{
		if (subscriber != null && subscriber.cosID() != null)
		{
			String cosType = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(subscriber.cosID()).getCosType();
			if (cosType != null && cosType.equalsIgnoreCase("LITE"))
			{
				logger.info("subscriber " + subscriber.subID()
						+ " is a LITE user");
				return true;
			}
		}

		return false;
	}

	private static boolean isClipPromotable(Clip clip, String rbtWavFile)
	{
		if (clip == null
				|| clip.getClipEndTime().getTime() < System.currentTimeMillis())
		{
			logger.info("Clip " + rbtWavFile + " does not exist or expired");
			return false;
		}

		if (blockedMappingClips.contains(clip.getClipRbtWavFile()))
		{
			logger.info("Clip " + clip.getClipRbtWavFile()
					+ " is mapped to blocked category so returning false");
			return false;
		}

		if (blockedClips.contains(String.valueOf(clip.getClipId())))
		{
			logger.info("Clip " + clip.getClipRbtWavFile()
					+ " is blocked clip so returning false");
			return false;
		}

		return true;
	}

	private static boolean isCallerListnedRBTForMinDuration(short callDuration)
	{
		String minSongDuration = RBTParametersUtils.getParamAsString("VIRAL",
				"MIN_DURATION_SONG", null);
		if (minSongDuration == null)
		{
			logger.debug("VIRAL-MIN_DURATION_SONG parameter is not defined.");
			return true;
		}

		try
		{
			short songDuration = Short.parseShort(minSongDuration);
			if (callDuration < songDuration)
			{
				logger.info("Caller heard song only for " + callDuration
						+ " seconds as against minimum " + songDuration);
				return false;
			}
		}
		catch (Exception e)
		{

		}

		return true;
	}

	private static String getEncodedUrlString(String param)
	{
		String ret = null;
		try
		{
			ret = new URLCodec().encode(param, "UTF-8");
		}
		catch (Throwable t)
		{
			ret = null;
		}
		return ret;
	}

	private static String encodeParam(String param)
	{
		try
		{
			param = URLEncoder.encode(param, "UTF-8");
		}
		catch (Exception e)
		{

		}

		return param;
	}

	private static boolean writeTrans(String caller, String called, int clip,
			String time, StringBuffer artistName)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("CALLER", caller);
		h.put("CALLED", called);
		h.put("CLIP", "" + clip);
		h.put("TIME OF CALL", time);
		if (isArtistBasedSmsEnabled && artistName != null) {
			h.put("ARTIST NAME", artistName.toString().trim());
		}
		if (writeTrans != null)
		{
			writeTrans.writeTrans(h);
			return true;
		}

		return false;
	}

	public static ViralSMSTable addViralData(
			ViralPromotionRequest promotionRequest, String type, Clip clip,
			String circleID, String viralPromotionType)
	{
		String callerID = promotionRequest.getCallerID();
		String calledID = promotionRequest.getCalledID();
		String clipID = promotionRequest.getRbtWavFile();

		if (clip != null)
			clipID = String.valueOf(clip.getClipId());

		int count = 0;
		String selectedBy = null;
		Date sentTime = new Date();
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();;

		if (type.equals("VIRAL_PENDING"))
		{
			count = promotionRequest.getCallDuration();
			sentTime.setTime(promotionRequest.getCalledTime());

			if (!promotionRequest.isValidationRequired())
			{
				extraInfoMap.put("VALIDATED", "TRUE");

				if (promotionRequest.getCallerLanguage() != null)
				{
					extraInfoMap.put("CALLER_LANG",
							promotionRequest.getCallerLanguage());
				}
			}
		}
		
		if (type.equals("BASIC") && isAddInLoop) {
			extraInfoMap.put("inLoop", String.valueOf(isAddInLoop));
		}

		if(viralPromotionType != null) {
			extraInfoMap.put("VIRAL_PROMOTION_TYPE", viralPromotionType);
		}

		logger.info("ExtraINfo:: " + extraInfoMap);

		ViralSMSTable viralData = rbtDBManager.insertViralSMSTableMap(callerID,
				sentTime, type, calledID, clipID, count, selectedBy, null,
				extraInfoMap, circleID);

		return viralData;
	}

	public static boolean deleteViralData(
			ViralPromotionRequest promotionRequest, String type,
			String circleID, String viralPromotionType)
	{
		String callerID = promotionRequest.getCallerID();
		String calledID = promotionRequest.getCalledID();

		boolean isViralDataDeleted = rbtDBManager.deleteViralPromotion(callerID, null, type, null);
		return isViralDataDeleted;
	}


	/**
	 * @return the delayTimeInMins
	 */
	public static int getDelayTimeInMins()
	{
		return delayTimeInMins;
	}

	private static String getViralSMSType(String callerID, ViralPromotionRequest promotionRequest) {
		List<String> subscriberStatusForSMS = new ArrayList<String>();
		List<String> subscriberStatusForUSSD = new ArrayList<String>();
		String viralPromotionType = null;
		Subscriber subscriber = null;
		String enableAlternativeViralSmsType = RBTParametersUtils.getParamAsString("VIRAL", "ENABLE_ALTERNATIVE_VIRAL_SMS_TYPE", "FALSE");
		String subStatuViralPromotionSms = RBTParametersUtils.getParamAsString("VIRAL", "SUB_STATUS_VIRAL_PROMOTION_SMS", null);
		String subStatuViralPromotionUssd = RBTParametersUtils.getParamAsString("VIRAL", "SUB_STATUS_VIRAL_PROMOTION_USSD", null);		

		if(enableAlternativeViralSmsType.equalsIgnoreCase("FALSE") && subStatuViralPromotionSms == null && subStatuViralPromotionUssd == null) {			
			return null;
		}


		subscriber = rbtDBManager.getSubscriber(callerID);

		if(subStatuViralPromotionSms != null) {
			subscriberStatusForSMS = Arrays.asList(subStatuViralPromotionSms.split("\\,"));
		}

		if(subStatuViralPromotionUssd != null) {
			subscriberStatusForUSSD = Arrays.asList(subStatuViralPromotionUssd.split("\\,"));
		}

		String subStatus = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriberStatus(subscriber);

		boolean isSmsUssdHavingSameStatus = false;
		if(subscriberStatusForSMS.contains(subStatus) && subscriberStatusForUSSD.contains(subStatus) ) {
			isSmsUssdHavingSameStatus = true;			
		}
		else if(subscriberStatusForSMS.contains(subStatus)) {
			viralPromotionType = "SMS";
		}		
		else if(subscriberStatusForUSSD.contains(subStatus)) {
			viralPromotionType = "USSD";
		}


		if(isSmsUssdHavingSameStatus || (viralPromotionType == null && enableAlternativeViralSmsType.equalsIgnoreCase("TRUE"))) {
			String[] dataTypes = { "BASIC", "VIRAL_EXPIRED" };
			ViralSMSTable[] latestViralSMSTable = rbtDBManager.getViralSMSByTypesForSubscriber(promotionRequest.getCallerID(), dataTypes);
			if(latestViralSMSTable != null && latestViralSMSTable.length > 0) {
				for(ViralSMSTable viralSms : latestViralSMSTable) {	
					String viralExtraInfo = viralSms.extraInfo();
					String tempSmsType = null;
					if(viralExtraInfo != null) {
						HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(viralExtraInfo);
						tempSmsType = extraInfoMap.get("VIRAL_PROMOTION_TYPE");
					}
					if(tempSmsType == null) {
						continue;
					}
					if(tempSmsType.equals("SMS")) {
						viralPromotionType = "USSD";
					}
					else if(tempSmsType.equals("USSD")){
						viralPromotionType = "SMS";
					}
					break;
				}
			}
		}
		else if(viralPromotionType == null && enableAlternativeViralSmsType.equalsIgnoreCase("FALSE")) {
			if(logger.isDebugEnabled()) {
				logger.debug("Sutus is not configured in parameters table, fall back to old model subscriberid: " + callerID + " status: " + subStatus);
				return null;
			}
		}
		logger.info("viralPromotionType: " + viralPromotionType + " subscriberId: " + callerID + " status: " + subStatus);
		return viralPromotionType;
	}

	/**
	 * @param caller
	 * @return
	 */
	private static boolean isUserActive(Subscriber caller)
	{
		if (caller != null && !caller.subYes().equalsIgnoreCase(
				iRBTConstant.STATE_DEACTIVATED)) {
			return true;
		}

		return false;
	}

	private static String makeAHitToBI(String calledID, Clip clip,
			Subscriber caller, String callerID) {
		// http://ip:port/GRecoUtils/getReco?rd=rec35&ft=f35&ds=ds35&Aparty=&lt;msisdn&gt;&BParty=&lt;msisdn&gt;
		//&songid=&lt;songid&gt;&songname=&lt;songname>
		String response = null;
		String biUrl = RBTParametersUtils.getParamAsString("VIRAL",
				"VIRAL_BI_URL", null);

		if (biUrl == null) {
			logger.warn("VIRAL-BI_URL not configured !!!!!");
			return "";
		}

		if(isUserDeactiveInLastNDays(caller)){
			logger.info("Caller deactive in last N days");
			return null;
		}

		String senderNo = RBTParametersUtils.getParamAsString("VIRAL",
				"SMS_SENDER_NO", "123");

		biUrl = biUrl.replaceFirst("%aparty%", callerID);
		biUrl = biUrl.replaceFirst("%bparty%", calledID);
		biUrl = biUrl.replaceFirst("%songid%", clip.getClipId()+"");
		biUrl = biUrl.replaceFirst("%songname%", encodeParam(clip.getClipName()));

		logger.info("Inside makeAHitToBI methods ......Making a hit to BI Url :"+biUrl);
		StringBuffer strBuffer = new StringBuffer();
		boolean hitSuccess = Tools.callURL(biUrl, new Integer(-1), strBuffer, false, null,
				-1, false, 2000);

		if(hitSuccess) {
			response = strBuffer.toString();
		}        
		logger.info("Response from BI = "+response);
		return response;
	}

	private static boolean isUserDeactiveInLastNDays(Subscriber caller) {
		//if user is deactivated before N days, then it returns false
		String noOfDaysBeforeDeactivation = RBTParametersUtils.getParamAsString("VIRAL",
				"NO_OF_DAYS_LAST_DEACTIVE", "10");
		if (caller == null)
			return false;

		if (caller!=null && !isUserActive(caller)) {
			int noOfDays = 0;
			try {
				noOfDays = Integer.parseInt(noOfDaysBeforeDeactivation);
			} catch (Exception ex) {
				logger.info("Exception while Parsing No of Days");
			}
			Date endDate = caller.endDate();
			Date date = new Date();
			long newEndDateMillis = (date.getTime()
					- (noOfDays * 24 * 60 * 60 * 1000L));
			Date dte = new Date(newEndDateMillis);
			logger.info("Viral Caller End date = "+endDate + "Threshold date = "+dte);
			if (!endDate.before(dte))
				return true;
		}
		return false;
	}

	public static Set<String> getTestNumber(){
		return testNumers;
	}


	private static String getBaseOffer(String subscriberID, String mode) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		int offerType = Offer.OFFER_TYPE_SUBSCRIPTION;
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setType(offerType + "");
		Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
		if (offers != null && offers.length > 0
				&& offers[0].getOfferID() != null) {
			if (offers[0].getSrvKey() != null) {
				logger.info("Returning offer: " + offers[0].getSrvKey()
						+ ", subscriberId: " + subscriberID + ", mode: " + mode);
				return offers[0].getSrvKey();
			}
		}
		logger.info("Returning offer: null, subscriberId: " + subscriberID
				+ ", mode: " + mode);
		return null;
	}

	private static String getSelOffer(String subscriberID, int clipId,
			String mode) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		int offerType = Offer.OFFER_TYPE_SELECTION;
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setType(offerType + "");
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		extraInfoMap.put(Offer.CLIP_ID, clipId + "");
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
		if (offers != null && offers.length > 0
				&& offers[0].getOfferID() != null) {
			if (offers[0].getSrvKey() != null) {
				logger.info("Returning offer: " + offers[0].getSrvKey()
						+ ", subscriberId: " + subscriberID + ", clipId: "
						+ clipId + ", mode: " + mode);
				return offers[0].getSrvKey();
			}
		}
		logger.info("Returning offer: null, subscriberId: " + subscriberID
				+ ", clipId: " + clipId + ", mode: " + mode);
		return null;
	}

	public static String getSMSText(String type, String subType, String defaultValue,
			String language, String circleId) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				type, subType, language, circleId);
		if (smsText != null)
			return smsText;
		else
			return defaultValue;
	}
	
	private static String getInLocalCurrencyFormat(String selAmt) {
		String returnValue = selAmt;
		try {
			//double amount = Double.parseDouble(selAmt.replace(",","."));
			returnValue = CurrencyUtil.getFormattedCurrency(null, selAmt);
		} catch (Exception e) {
			logger.warn("In correct value of charge class amout", e);
		}
		return returnValue;
	}
}
