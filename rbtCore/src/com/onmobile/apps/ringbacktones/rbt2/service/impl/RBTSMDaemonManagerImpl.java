package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.ClipStatusDaemon;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager.MappedSiteIdNotFoundException;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager.WDSInfoNotFoundException;
import com.onmobile.apps.ringbacktones.daemons.VodaPrismDaemon;
import com.onmobile.apps.ringbacktones.daemons.interfaces.PlayerThread;
import com.onmobile.apps.ringbacktones.daemons.reminder.ReminderDaemon;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.logger.SMHitLogger;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTSMDaemonService;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.snmp.agentx.client.ManagedObjectCallback;

public class RBTSMDaemonManagerImpl implements RBTSMDaemonService, iRBTConstant {

	private static Logger logger = Logger.getLogger(RBTSMDaemonManagerImpl.class);
	private static ParametersCacheManager m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
	private static RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	private static RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
	private static final String REFID = "INT_REF_ID";
	private static final String REQUEST_TYPE = "REQUEST_TYPE";
	private static final String URL = "URL";
	private static final String REFID_CREATED = "REF_CREATED";
	public static final String DAEMON = "DAEMON";
	HttpClient m_httpClient = null;
	URLCodec m_urlEncoder = new URLCodec();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	SimpleDateFormat actOrSelFailSdf = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat m_timeSdf = new SimpleDateFormat("ddMMMyy");
	SimpleDateFormat m_clipExpirySdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
	public static ArrayList<String> m_lowPriorityModes = null;
	static String m_sdrWorkingDir = ".";
	static int m_sdrSize = 1000;
	static long m_sdrInterval = 24;
	static String m_sdrRotation = "size";
	static boolean m_sdrBillingOn = true;
	boolean m_combinedCharging = false;
	public static WriteDailyTrans m_writeTrans = null;
	public static WriteDailyTrans smErrorCasesTrans = null;
	Integer statusCode = new Integer(-1);
	StringBuffer response = new StringBuffer();
	List<String> supportedLangList = null;
	PlayerThread _playerThread = null;
	static ReminderDaemon reminderDaemon = null;
	static VodaPrismDaemon vodaPrismDaemon = null;
	static ClipStatusDaemon clipStatusDaemon = null;
	public static boolean isFcapsEnabled = false;
	public static List<ManagedObjectCallback> managedObjectsList = null;
	private static List<String> shuffleCategoryTypesForSendingClipInfoList = null;
	// RBT-14301: Uninor MNP changes.
	private static Map<String, String> circleIdToSiteIdMapping = null;
	// RBT-14497 - Tone Status Check
	
	
	public RBTSMDaemonManagerImpl() {
		supportedLangList = Arrays.asList(getParamAsString("COMMON", "SUPPORTED_LANGUAGES", "eng").split(","));
	}
	

	@Override
	public HashMap<String, String> getSMURL(Subscriber subscriber, SubscriberStatus subscriberStatus,
			SubscriberDownloads subDownload, ProvisioningRequests provisioningRequests, String requestType,
			boolean sendSubTypeUnknown, HashMap<String, String> loggingInfoMap) {
		logger.info("Getting sm URL");

		String subID = null;
		String refID = null;
		String type = "P";
		String srvkey = null;
		String oldSrvKey = null;
		String srvDefault = "RBT_ACT_DEFAULT";
		String user_info = "";
		String trxid = null;
		String gifterNo = null;
		String cosID = null;
		String wdsInfo = null;
		String giftTransactionID = null;
		String expiryDate = null;
		String offerID = null;
		String language = null;
		String reactRefID = null;
		String useMode = null;
		boolean song_type_change = false;
		String old_ref_id = null;
		// Offer parameters to be passed to SubMgr. Add all the parameters into
		// map and append all offer values to SubMgr Url
		// format : offparams=key1:value1|key2:value2|IMEI_NO:1234567890123456|
		HashMap<String, String> offerParams = new HashMap<String, String>();

		HashMap<String, String> result = new HashMap<String, String>();
		boolean isChange = false;
		boolean isRenewal = false;
		boolean isDelayedDeact = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String smReqType = null;
		boolean isSelReactive = false; // Added for TRAI regulation
		boolean isCombinedChrg = getParamAsBoolean("COMBINED_CHARGING", "FALSE");

		Category categoryObj = null;

		String devProvDone = null;
		String basePrice = null;
		String childPrice = null;

		String extraInfo = null;
		String baseConsentParams = "";
		String selConsentParams = "";
		String reqMode = null;
		if (subscriber != null) {
			extraInfo = subscriber.extraInfo();
			smReqType = "BASE";
			subID = subscriber.subID();
			refID = subscriber.refID();
			reqMode = subscriber.activatedBy();
			result.put(REFID, refID);
			loggingInfoMap.put(SMHitLogger.REFID, refID);
			loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_BASE);
			loggingInfoMap.put(SMHitLogger.MSISDN, subscriber.subID());
			loggingInfoMap.put(SMHitLogger.CIRCLE_ID, subscriber.circleID());
			if (sendSubTypeUnknown) {
				type = "U";
			} else {
				type = RBTParametersUtils.getParamAsString("DAEMON", "SEND_SUB_TYPE", "p/b");
				if ("p/b".equalsIgnoreCase(type)) {
					if (subscriber.prepaidYes()) {
						type = "P";
					} else {
						type = "B";
					}
					if(subscriber.extraInfo()!=null && subscriber.extraInfo().contains(HYBRID_SUBSCRIBER_TYPE)){
						type = "H";
					}
				}
			}
			loggingInfoMap.put(SMHitLogger.SUB_TYPE, type);
			language = subscriber.language();
			trxid = getTransID(subscriber.activationInfo());
			srvkey = "RBT_ACT_" + subscriber.subscriptionClass();
			loggingInfoMap.put(SMHitLogger.SRVKEY, subscriber.subscriptionClass());
			HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			boolean updateMode = true;
			if (subscriber.subYes().equalsIgnoreCase("C")) {
				CosDetails cosDetail = null;
				loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_UPGRADE);
				if (getParamAsBoolean("IS_SONGPACK_ENABLED", "FALSE")
						|| getParamAsBoolean("LIMITED_DOWNLOAD_ENABLED", "FALSE")) {
					// User opts for songpack / limited download pack
					cosID = subscriber.cosID();
					cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
				}

				if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)
						&& extraInfoMap.containsKey(EXTRA_INFO_TOBE_ACT_OFFER_ID)) {
					cosID = extraInfoMap.get(iRBTConstant.EXTRA_INFO_COS_ID);
					cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
				}

				if (extraInfoMap != null && extraInfoMap.containsKey("PACK_MODE")) {
					logger.info("Extra info map is not null and contains pack mode " + extraInfoMap
							+ " SETTING logging info map " + extraInfoMap.get("PACK_MODE"));
					loggingInfoMap.put(SMHitLogger.MODE, extraInfoMap.get("PACK_MODE"));
					updateMode = false;
					useMode = extraInfoMap.get("PACK_MODE");
				}

				if (extraInfoMap != null && extraInfoMap.containsKey("PACK") && isCombinedChrg && cosDetail != null
						&& (SONG_PACK.equalsIgnoreCase(cosDetail.getCosType())
								|| UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
								|| LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
								|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetail.getCosType())
								|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetail.getCosType()))) {
					logger.info("Feature is song pack or limited down and pack is present in extrainfo ");
					String[] cosId = extraInfoMap.get("PACK").split(",");
					logger.info("Checking provision request for " + subID + " and " + cosId[0]);
					List<ProvisioningRequests> pr = ProvisioningRequestsDao
							.getBySubscriberIDTypeAndNonDeactivatedStatus(subID, Integer.parseInt(cosId[0]));
					if (pr != null && pr.size() > 0) {
						srvkey = "RBT_PACK_" + cosDetail.getSmsKeyword().toUpperCase();
						refID = pr.get(0).getTransId();
						loggingInfoMap.put(SMHitLogger.COS_ID, cosDetail.getCosId());
						loggingInfoMap.put(SMHitLogger.COS_TYPE, cosDetail.getCosType());
						loggingInfoMap.put(SMHitLogger.REFID, refID);
						loggingInfoMap.put(SMHitLogger.SRVKEY, cosDetail.getSmsKeyword().toUpperCase());
					}
				} else if (cosDetail != null && (SONG_PACK.equalsIgnoreCase(cosDetail.getCosType())
						|| UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
						|| LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
						|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetail.getCosType())
						|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetail.getCosType()))) {
					srvkey = "RBT_SEL_" + cosDetail.getSmsKeyword().toUpperCase();
					refID = subscriber.subID() + ":" + cosDetail.getCosId();
					loggingInfoMap.put(SMHitLogger.COS_ID, cosDetail.getCosId());
					loggingInfoMap.put(SMHitLogger.COS_TYPE, cosDetail.getCosType());
					loggingInfoMap.put(SMHitLogger.REFID, refID);
					loggingInfoMap.put(SMHitLogger.SRVKEY, cosDetail.getSmsKeyword().toUpperCase());
				} else {
					isChange = true;
					oldSrvKey = "RBT_ACT_" + subscriber.oldClassType();
					loggingInfoMap.put(SMHitLogger.OLD_SRVKEY, subscriber.oldClassType());
				}

				if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.DELAY_DEACT)
						&& extraInfoMap.get(iRBTConstant.DELAY_DEACT).equalsIgnoreCase("TRUE")) {
					loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_DELAYED_DEACTIVATION);
					isDelayedDeact = true;
					isChange = !isDelayedDeact;
				}

			}
			if (updateMode)
				loggingInfoMap.put(SMHitLogger.MODE, subscriber.activatedBy());
			if (subscriber.activatedBy() != null && subscriber.activatedBy().equalsIgnoreCase("GIFT")
					&& subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":") > 0) {
				gifterNo = subscriber.activationInfo().substring(subscriber.activationInfo().indexOf(":") + 1);
				if (gifterNo.indexOf("|") > -1)
					gifterNo = subscriber.activationInfo().substring(0, subscriber.activationInfo().indexOf("|"));
			}
			if (requestType.trim().equalsIgnoreCase("DCT")
					&& ((!getParamAsBoolean(iRBTConstant.COMMON, "IS_DEACT_ALLOWED_FOR_ACT_PENDING", "FALSE")
							&& subscriber.activationDate() == null))) {
				return null;
			}

			if (getParamAsBoolean("SEND_ACTIVE_EASY", "FALSE")
					&& (subscriber.subYes().equalsIgnoreCase("C") || subscriber.subYes().equalsIgnoreCase("A"))) {
				String selected = subscriber.activatedBy();
				if (selected != null
						&& Arrays.asList(getParamAsString("DAEMON", "ACTIVATED_PRE_CHRG", " ").toLowerCase().split(","))
								.contains(selected.toLowerCase())) {
					type = type + "&precharge=true";
					loggingInfoMap.put(SMHitLogger.PRECHARGE, "1");
				}
			}

			if (extraInfoMap != null) {
				if (extraInfoMap.containsKey(GIFT_TRANSACTION_ID) && subscriber.activatedBy() != null
						&& subscriber.activatedBy().equalsIgnoreCase("GIFT"))
					giftTransactionID = extraInfoMap.get(GIFT_TRANSACTION_ID);
				// added by Sreekar for ACWM feature
				if (extraInfoMap.containsKey(EXTRA_INFO_OFFER_ID))
					offerID = "&offerid=" + extraInfoMap.get(EXTRA_INFO_OFFER_ID);
				if (extraInfoMap.containsKey(EXTRA_INFO_TOBE_ACT_OFFER_ID))
					offerID = "&offerid=" + extraInfoMap.get(EXTRA_INFO_TOBE_ACT_OFFER_ID);
				if (extraInfoMap.containsKey(EXTRA_INFO_IMEI_NO))
					offerParams.put(EXTRA_INFO_IMEI_NO, extraInfoMap.get(EXTRA_INFO_IMEI_NO));

				// RBT-2951 remote DCT to be disabled for configurable modes,
				// Feature basically from Telefonica Spain operator
				if (extraInfoMap.containsKey(HLR_PROV)) {
					String value = extraInfoMap.get(HLR_PROV);
					if (value != null && value.equalsIgnoreCase(NO)) {
						devProvDone = "&is_deprov_done=true";
					}
				}
				// CG Integration Flow - Jira -12806
				boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(iRBTConstant.DOUBLE_CONFIRMATION,
						"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
				boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils.getParamAsBoolean(COMMON,
						"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
				if (checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast) {
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						user_info += "|cgId:" + extraInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
					}
					// if (extraInfoMap != null
					// &&
					// extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID))
					// {
					// user_info += "|transId:"
					// + extraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
					// }
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						trxid = extraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
					}

				} else {
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						user_info += "|CGID:" + extraInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
					}
					if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						user_info += "|TRANSID:" + extraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
					}
				}
				if (extraInfoMap != null && extraInfoMap.containsKey(Constants.param_baseprice)) {
					basePrice = "&baseprice=" + extraInfoMap.get(Constants.param_baseprice);
				}

				String baseMappedStr = getParamAsString(COMMON, "BASE_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (baseMappedStr != null) {
					String str[] = baseMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2 && extraInfoMap.containsKey(s[1]))
							baseConsentParams += "&" + s[1] + "=" + extraInfoMap.get(s[1]);
					}
				}

				// SDP Direct : rbt-9213
				String sdpInfoParams = getSdpInfoParam(subscriber, null);
				if (sdpInfoParams != null) {
					user_info += "|" + sdpInfoParams;
				}

			}

		} else if (subscriberStatus != null) {
			logger.info("Subscriber status is not null ");
			smReqType = "SEL";

			extraInfo = subscriberStatus.extraInfo();
			reqMode = subscriberStatus.selectedBy();
			subID = subscriberStatus.subID();
			Subscriber subscribers = getSubscriber(subID);
			loggingInfoMap.put(SMHitLogger.MSISDN, subscribers.subID());
			loggingInfoMap.put(SMHitLogger.CIRCLE_ID, subscribers.circleID());
			loggingInfoMap.put(SMHitLogger.REFID, subscriberStatus.refID());
			loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_CONTENT);
			loggingInfoMap.put(SMHitLogger.CAT_TYPE, String.valueOf(subscriberStatus.categoryType()));
			loggingInfoMap.put(SMHitLogger.CAT_ID, String.valueOf(subscriberStatus.categoryID()));
			loggingInfoMap.put(SMHitLogger.MODE, subscriberStatus.selectedBy());
			if (!getParamAsBoolean("RETAIN_DOWNLOAD_DCT", "FALSE") && !isCombinedChrg
					&& (subscribers == null || !(subscribers.subYes().equalsIgnoreCase("B")
							|| subscribers.subYes().equalsIgnoreCase("O")
							|| subscribers.subYes().equalsIgnoreCase("Z")))) {
				if (subscribers == null || subscribers.subYes().equalsIgnoreCase("D")
						|| subscribers.subYes().equalsIgnoreCase("P") || subscribers.subYes().equalsIgnoreCase("X")
						|| subscribers.subYes().equalsIgnoreCase("F")) {
					rbtDBManager.deactivateSubscriberRecords(subID);
				}
				logger.info("Selection not processed as subscription status is " + subscribers.subYes()
						+ " for subcriber " + subID);
				return null;
			}
			language = subscribers.language();
			String caller = subscriberStatus.callerID();
			Date setTime = subscriberStatus.setTime();
			int status = subscriberStatus.status();
			int fromTime = subscriberStatus.fromTime();
			int toTime = subscriberStatus.toTime();
			String selStatus = subscriberStatus.selStatus();
			loggingInfoMap.put(SMHitLogger.CONTENT_STATUS, String.valueOf(status));
			// Added for song reactivation. TRAI changes
			HashMap<String, String> selExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());

			String tpCgid = null;

			if (selExtraInfoMap != null) {
				if (selExtraInfoMap.containsKey(REACTIVE))
					isSelReactive = true;
				if (selExtraInfoMap.containsKey("GIFTER"))
					gifterNo = selExtraInfoMap.get("GIFTER");
				if (selExtraInfoMap.containsKey(GIFT_TRANSACTION_ID) && subscriberStatus.selectedBy() != null
						&& subscriberStatus.selectedBy().equalsIgnoreCase("GIFT"))
					giftTransactionID = selExtraInfoMap.get(GIFT_TRANSACTION_ID);
				// added by Sreekar for ACWM feature
				if (selExtraInfoMap.containsKey(EXTRA_INFO_OFFER_ID)) {
					offerID = "&offerid=" + selExtraInfoMap.get(EXTRA_INFO_OFFER_ID);
				}
				if (selExtraInfoMap.containsKey(EXTRA_INFO_IMEI_NO))
					offerParams.put(EXTRA_INFO_IMEI_NO, selExtraInfoMap.get(EXTRA_INFO_IMEI_NO));
				if (selExtraInfoMap.containsKey(EXTRA_INFO_REACT_REFID))
					reactRefID = "&reactrefid=" + selExtraInfoMap.get(EXTRA_INFO_REACT_REFID)
							+ "&rejectifnovalidity=true";

				if (selExtraInfoMap != null && selExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
					tpCgid = selExtraInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
				}

				if (selExtraInfoMap != null && selExtraInfoMap.containsKey(Constants.param_childprice)) {
					childPrice = "&childprice=" + selExtraInfoMap.get(Constants.param_childprice);
				}

				String selMappedStr = getParamAsString(COMMON, "SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (selMappedStr != null) {
					String str[] = selMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2 && selExtraInfoMap.containsKey(s[1])) {
							selConsentParams += "&" + s[1] + "=" + selExtraInfoMap.get(s[1]);
						}
					}
				}

			}

			String rbt_wav = subscriberStatus.subscriberFile();
			user_info = "";
			Clip clipObj = getClipRBT(rbt_wav);

			if (clipObj != null) {
				loggingInfoMap.put(SMHitLogger.CLIP_ID, String.valueOf(clipObj.getClipId()));
				loggingInfoMap.put(SMHitLogger.CLIP_TYPE, clipObj.getContentType());
				try {
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
					if (clipObj != null && clipObj.getClipGrammar() != null
							&& clipObj.getClipGrammar().equalsIgnoreCase("UGC")
							&& clipObj.getClipEndTime().getTime() == sdf1.parse("2004/01/01").getTime()) {
						return null;
					}
				} catch (Exception e) {
					logger.warn("", e);
				}
			}
			categoryObj = getCategory(subscriberStatus.categoryID());
			if (clipObj == null) {
				String categoryType = Utility.getCategoryType(categoryObj.getCategoryTpe());
				/*
				 * Clip can be null for 1. Cricket selections 2. Record my own
				 * selections 3. Karaoke selections
				 */
				if (subscriberStatus.status() != 90 && subscriberStatus.status() != 99
						&& !(categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE)
								|| categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD))) {
					Logger loggerValidityCapture = Logger.getLogger(RBTDaemonManager.class.getName() + ".captureClip");
					loggerValidityCapture.info(new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date()) + ","
							+ subID + "," + subscriberStatus.refID() + "," + "" + "," + rbt_wav + ","
							+ categoryObj.getCategoryId() + "," + categoryObj.getCategoryTpe());
					return null;
				}
			}
			try {
				user_info = getUserInfo(clipObj, categoryObj, subscribers, subscriberStatus, null);
			} catch (WDSInfoNotFoundException e) {
				logger.warn("", e);
				return null;
			}

			trxid = getTransID(subscriberStatus.selectionInfo());

			// CG Integration Flow - Jira -12806
			boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(iRBTConstant.DOUBLE_CONFIRMATION,
					"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
			boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils.getParamAsBoolean(COMMON,
					"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
			if (checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast) {
				if (tpCgid != null && user_info.indexOf("CGID") == -1) {
					user_info += "|cgId:" + tpCgid;
				}
				// if (selExtraInfoMap != null
				// && selExtraInfoMap
				// .containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
				// user_info += "|transId:"
				// + selExtraInfoMap
				// .get(iRBTConstant.EXTRA_INFO_TRANS_ID);
				// }
				if (selExtraInfoMap != null && selExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
					trxid = selExtraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
				}

			} else {
				if (tpCgid != null && user_info.indexOf("CGID") == -1) {
					user_info += "|CGID:" + tpCgid;
				}
				if (selExtraInfoMap != null && selExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
					user_info += "|TRANSID:" + selExtraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
				}
			}
			SimpleDateFormat clipExpirySdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
			expiryDate = (clipObj != null && clipObj.getClipEndTime() != null)
					? clipExpirySdf.format(clipObj.getClipEndTime()) : null;

			refID = (caller == null) ? "all" : caller;
			user_info += ("|cli:" + refID);

			refID = refID + ":" + subscriberStatus.status();
			if (setTime != null) {
				refID = refID + ":" + sdf.format(setTime);
			}
			if (status == 80) {
				refID = refID + ":" + fromTime + ":" + toTime;
			}
			// trxid = getTransID(subscriberStatus.selectionInfo());
			if (requestType != null && subscriberStatus.selectionInfo() != null
					&& (requestType.trim().equalsIgnoreCase("REALTIME") || requestType.trim().equalsIgnoreCase("ACT")
							|| subscriberStatus.selectionInfo().indexOf("#NEWREF#") > 0)
					&& !getClipIDRBTWav(rbt_wav, null).equals("MISSING")) {
				refID = refID + ":" + getClipIDRBTWav(rbt_wav, null);
			}
			if (subscriberStatus.refID() != null)
				refID = subscriberStatus.refID();
			else
				result.put(REFID_CREATED, "TRUE");

			result.put(REFID, refID);

			type = RBTParametersUtils.getParamAsString("DAEMON", "SEND_SUB_TYPE", "p/b");
			if ("p/b".equalsIgnoreCase(type)) {
				if (subscribers.prepaidYes()) {
					type = "P";
				} else {
					type = "B";
				}
				if(subscribers.extraInfo()!=null && subscribers.extraInfo().contains(HYBRID_SUBSCRIBER_TYPE)){
					type = "H";
				}
			}
			loggingInfoMap.put(SMHitLogger.SUB_TYPE, type);
			if (getParamAsBoolean("SEND_ACTIVE_EASY", "FALSE") && (selStatus.equalsIgnoreCase("C")
					|| selStatus.equalsIgnoreCase("A") || selStatus.equalsIgnoreCase("W"))) {
				String selected = subscriberStatus.selectedBy().trim();
				if (selected != null
						&& Arrays.asList(getParamAsString("DAEMON", "SELECTED_PRE_CHRG", "").toLowerCase().split(","))
								.contains(selected.toLowerCase())) {
					type = type + "&precharge=true";
					loggingInfoMap.put(SMHitLogger.PRECHARGE, "1");

				}
			}

			String song = subscriberStatus.subscriberFile();
			if (song != null) {
				if (song.indexOf(" ") >= 1)
					song = song.substring(0, song.indexOf(" "));
			}

			String classTypePre = "RBT_SEL_";
			if (getParamAsBoolean("SEND_SEL_SETTING", "FALSE"))
				classTypePre = "RBT_SET_";
			srvkey = classTypePre + subscriberStatus.classType();
			loggingInfoMap.put(SMHitLogger.SRVKEY, subscriberStatus.classType());
			if (subscriberStatus.selStatus().equalsIgnoreCase("A")) {
				String selected = subscriberStatus.selectedBy();
				if (selected != null && selected.equals("GIFT"))
					refID = refID + "&substatus=A";
			} else if (subscriberStatus.selStatus().equalsIgnoreCase("C")) {

				isDelayedDeact = (selExtraInfoMap != null
						&& "TRUE".equalsIgnoreCase(selExtraInfoMap.get("DELAY_DEACT")));
				isChange = !isDelayedDeact;
				if (isChange) {
					oldSrvKey = classTypePre + subscriberStatus.oldClassType();
					loggingInfoMap.put(SMHitLogger.OLD_SRVKEY, subscriberStatus.oldClassType());
				}
				loggingInfoMap.put(SMHitLogger.TYPE,
						isChange ? SMHitLogger.TYPE_UPGRADE : SMHitLogger.TYPE_DELAYED_DEACTIVATION);
			}

			if (getParamAsBoolean("ADD_ACT_CLASS_TO_SER_KEY", "FALSE")) {
				if (subscribers != null) {
					srvkey = srvkey + "_" + "RBT_ACT_" + subscribers.subscriptionClass();
					srvDefault = "RBT_ACT_" + subscribers.subscriptionClass();
					oldSrvKey = oldSrvKey + "_" + "RBT_ACT_" + subscribers.subscriptionClass();
				}
			}

			// SDP Direct : rbt-9213
			String sdpInfoParams = getSdpInfoParam(null, subscriberStatus);
			if (sdpInfoParams != null) {
				user_info += "|" + sdpInfoParams;
			}

			// ADDED BY DEEPAK KUMAR FOR IBM SM INTEGRATION
			if (getParamAsBoolean("IBM_SM_INTEGRATION", "FALSE")) {
				if (subscribers != null && subscriberStatus != null) {
					SubscriberStatus subStatus[] = rbtDBManager.getAllActiveSubscriberSettings(subID);
					if (subStatus != null && subStatus.length > 1) {
						for (int i = subStatus.length - 1; i > 0
								&& isSameCaller(subStatus[i - 1].callerID(), subscriberStatus.callerID()); i--) {
							if (refID.equalsIgnoreCase(subStatus[i].refID()) && i > 0) {
								old_ref_id = subStatus[i - 1].refID();
								boolean currSelCategoryType = Utility
										.isShuffleCategory(subscriberStatus.categoryType());
								boolean oldSelCategoryType = Utility.isShuffleCategory(subStatus[i - 1].categoryType());
								if (currSelCategoryType != oldSelCategoryType) {
									song_type_change = true;
									break;
								} else if (currSelCategoryType) // implies both
																// selections
																// are of type
																// shuffle
								{
									song_type_change = false;
									break;
								} else {
									char currSelLoopStatus = subscriberStatus.loopStatus();
									char prevSelLoopStatus = subStatus[i - 1].loopStatus();
									if ((prevSelLoopStatus == 'l' || prevSelLoopStatus == 'L'
											|| prevSelLoopStatus == 'A')
											&& (currSelLoopStatus == 'l' || currSelLoopStatus == 'L'
													|| currSelLoopStatus == 'A')) {
										song_type_change = false;
										break;
									} else if ((prevSelLoopStatus == 'o' || prevSelLoopStatus == 'O'
											|| prevSelLoopStatus == 'B')
											&& (currSelLoopStatus == 'o' || currSelLoopStatus == 'O'
													|| currSelLoopStatus == 'B')) {
										song_type_change = false;
										break;
									} else if (currSelLoopStatus != prevSelLoopStatus) {
										song_type_change = true;
										break;
									}
								}
							}
						}
						ArrayList<String> refIDList = new ArrayList<String>();
						refIDList.add(refID);
						String currSelExtraInfo = subscriberStatus.extraInfo();
						Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(currSelExtraInfo);
						if (extraInfoMap == null)
							extraInfoMap = new HashMap<String, String>();
						if (old_ref_id != null)
							if (song_type_change || !(subscriberStatus.loopStatus() == 'l'
									|| subscriberStatus.loopStatus() == 'L' || subscriberStatus.loopStatus() == 'A'))
								extraInfoMap.put("OLD_REF_ID", old_ref_id);
						String newExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
						rbtDBManager.updateSelectionExtraInfo(subID, refIDList, newExtraInfo);
					}
				}
			}

		} else if (subDownload != null) {

			smReqType = "DOWNLOAD";
			categoryObj = getCategory(subDownload.categoryID());

			extraInfo = subDownload.extraInfo();
			reqMode = subDownload.selectedBy();
			subID = subDownload.subscriberId();
			Subscriber subscribers = getSubscriber(subID);
			loggingInfoMap.put(SMHitLogger.MSISDN, subscribers.subID());
			loggingInfoMap.put(SMHitLogger.CIRCLE_ID, subscribers.circleID());
			loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_CONTENT);
			loggingInfoMap.put(SMHitLogger.REFID, subDownload.refID());
			loggingInfoMap.put(SMHitLogger.CAT_ID, String.valueOf(subDownload.categoryID()));
			loggingInfoMap.put(SMHitLogger.CAT_TYPE, String.valueOf(subDownload.categoryType()));
			loggingInfoMap.put(SMHitLogger.MODE, subDownload.selectedBy());
			if (!getParamAsBoolean("RETAIN_DOWNLOAD_DCT", "FALSE") && !isCombinedChrg
					&& (subscribers == null || !(subscribers.subYes().equalsIgnoreCase("B")
							|| subscribers.subYes().equalsIgnoreCase("O")
							|| subscribers.subYes().equalsIgnoreCase("Z")))) {
				if (subscribers == null || subscribers.subYes().equalsIgnoreCase("D")
						|| subscribers.subYes().equalsIgnoreCase("P") || subscribers.subYes().equalsIgnoreCase("X")
						|| subscribers.subYes().equalsIgnoreCase("F")) {
					rbtDBManager.expireAllSubscriberDownloadBaseDct(subID, "SMDaemon");
				}
				logger.info("Download not processed as subscription status is " + subscribers.subYes()
						+ " for subcriber " + subID);
				return null;
			}

			trxid = getTransID(subDownload.selectionInfo());

			language = subscribers.language();
			Date setTime = subDownload.setTime();
			String rbt_wav = subDownload.promoId();
			Clip clips = getClipRBT(rbt_wav);
			String clipID = null;
			if (clips != null) {
				loggingInfoMap.put(SMHitLogger.CLIP_ID, String.valueOf(clips.getClipId()));
				loggingInfoMap.put(SMHitLogger.CLIP_TYPE, clips.getContentType());
				clipID = "" + clips.getClipId();
				try {
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
					if (clips != null && clips.getClipGrammar() != null
							&& clips.getClipGrammar().equalsIgnoreCase("UGC")
							&& clips.getClipEndTime().getTime() == sdf1.parse("2004/01/01").getTime()) {
						return null;
					}
				} catch (Exception e) {
					logger.warn("", e);
				}

			} else {
				String categoryType = Utility.getCategoryType(categoryObj.getCategoryTpe());
				if (!(categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE)
						|| categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD))) {
					Logger loggerValidityCapture = Logger.getLogger(RBTDaemonManager.class.getName() + ".captureClip");
					loggerValidityCapture.info(new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date()) + ","
							+ subID + "," + subscriberStatus.refID() + "," + "NA" + "," + rbt_wav + ","
							+ categoryObj.getCategoryId() + "," + categoryObj.getCategoryTpe());
					return null;
				}
			}

			try {
				user_info = getUserInfo(clips, categoryObj, subscribers, null, subDownload);
			} catch (WDSInfoNotFoundException e) {
				logger.warn("", e);
				return null;
			}

			SimpleDateFormat clipExpirySdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
			expiryDate = (clips != null && clips.getClipEndTime() != null)
					? clipExpirySdf.format(clips.getClipEndTime()) : null;

			HashMap<String, String> downloadExtraInfoMap = DBUtility.getAttributeMapFromXML(subDownload.extraInfo());
			if (downloadExtraInfoMap != null) {
				if (downloadExtraInfoMap.containsKey(GIFT_TRANSACTION_ID) && subDownload.selectedBy() != null
						&& subDownload.selectedBy().equalsIgnoreCase("GIFT")) {
					giftTransactionID = downloadExtraInfoMap.get(GIFT_TRANSACTION_ID);
				}
				if (downloadExtraInfoMap.containsKey(EXTRA_INFO_OFFER_ID)) {
					offerID = "&offerid=" + downloadExtraInfoMap.get(EXTRA_INFO_OFFER_ID);
				}

				if (downloadExtraInfoMap != null && downloadExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
					user_info += "|CGID:" + downloadExtraInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
				}

				if (downloadExtraInfoMap != null
						&& downloadExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
					trxid = downloadExtraInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
				}

				String selMappedStr = getParamAsString(COMMON, "SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (selMappedStr != null) {
					String str[] = selMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2 && downloadExtraInfoMap.containsKey(s[1])) {
							selConsentParams += "&" + s[1] + "=" + downloadExtraInfoMap.get(s[1]);
						}
					}
				}

			}
			refID = "RBTDOWNLOAD";

			if (clipID != null && !clipID.equals("MISSING")) {
				refID = refID + ":" + clipID;
			} else {
				logger.info("Download clipID not received " + rbt_wav);
			}

			if (setTime != null) {
				refID = refID + ":" + sdf.format(setTime);
			}

			if (subDownload.refID() != null)
				refID = subDownload.refID();
			else
				result.put(REFID_CREATED, "TRUE");

			result.put(REFID, refID);

			type = RBTParametersUtils.getParamAsString("DAEMON", "SEND_SUB_TYPE", "p/b");
			if ("p/b".equalsIgnoreCase(type)) {
				if (subscribers.prepaidYes()) {
					type = "P";
				} else {
					type = "B";
				}
				if(subscribers.extraInfo()!=null && subscribers.extraInfo().contains(HYBRID_SUBSCRIBER_TYPE)){
					type = "H";
				}
			}
			loggingInfoMap.put(SMHitLogger.SUB_TYPE, type);
			srvkey = "RBT_SEL_" + subDownload.classType();
			loggingInfoMap.put(SMHitLogger.SRVKEY, subDownload.classType());
			if (subDownload.downloadStatus() == STATE_DOWNLOAD_CHANGE) {
				// If download status is C and ExtraInfo contains
				// DELAY_DEACT="TRUE" then it is delayed deactivation request
				isDelayedDeact = (downloadExtraInfoMap != null
						&& "TRUE".equalsIgnoreCase(downloadExtraInfoMap.get("DELAY_DEACT")));

				isChange = !isDelayedDeact;
				if (isChange && downloadExtraInfoMap != null)
					oldSrvKey = "RBT_SEL_" + downloadExtraInfoMap.get("OLD_CLASS_TYPE");
				loggingInfoMap.put(SMHitLogger.TYPE,
						isChange ? SMHitLogger.TYPE_UPGRADE : SMHitLogger.TYPE_DELAYED_DEACTIVATION);
			}

			if (getParamAsBoolean("ADD_ACT_CLASS_TO_SER_KEY", "FALSE")) {
				if (subscribers != null) {
					srvkey = srvkey + "_" + "RBT_ACT_" + subscribers.subscriptionClass();
					srvDefault = "RBT_ACT_" + subscribers.subscriptionClass();
				}
			}
		} else if (provisioningRequests != null) {
			smReqType = "PACK";

			extraInfo = provisioningRequests.getExtraInfo();
			reqMode = provisioningRequests.getMode();
			subID = provisioningRequests.getSubscriberId();
			cosID = provisioningRequests.getType() + "";
			String smsKeyWord = null;
			if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "ENABLE_ODA_PACK_PLAYLIST_FEATURE",
					"FALSE")) {
				// treating type as categoryId for ODA Pack Playlist Shuffle.
				Category category = rbtCacheManager.getCategory(provisioningRequests.getType());
				if (category != null && category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE) {
					smsKeyWord = provisioningRequests.getChargingClass();
				}
			}
			if (smsKeyWord == null) {
				CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
				smsKeyWord = (cosDetail != null) ? cosDetail.getSmsKeyword() : null;
			}
			Subscriber subscriberObj = rbtDBManager.getSubscriber(subID);
			refID = provisioningRequests.getTransId();
			result.put(REFID, refID);
			loggingInfoMap.put(SMHitLogger.REFID, refID);
			loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_BASE);
			loggingInfoMap.put(SMHitLogger.MSISDN, provisioningRequests.getSubscriberId());
			loggingInfoMap.put(SMHitLogger.CIRCLE_ID, subscriberObj.circleID());
			if (sendSubTypeUnknown) {
				type = "U";
			} else {
				type = RBTParametersUtils.getParamAsString("DAEMON", "SEND_SUB_TYPE", "p/b");
				if ("p/b".equalsIgnoreCase(type)) {
					if (subscriberObj.prepaidYes()) {
						type = "P";
					} else {
						type = "B";
					}
					if(subscriberObj.extraInfo()!=null && subscriberObj.extraInfo().contains(HYBRID_SUBSCRIBER_TYPE)){
						type = "H";
					}
				}
			}
			loggingInfoMap.put(SMHitLogger.SUB_TYPE, type);
			language = subscriberObj.language();
			trxid = getTransID(subscriberObj.activationInfo());
			srvkey = "RBT_PACK_" + smsKeyWord;
			loggingInfoMap.put(SMHitLogger.SRVKEY, smsKeyWord);

			loggingInfoMap.put(SMHitLogger.MODE, subscriberObj.activatedBy());

			Map<String, String> packExtraInfoMap = DBUtility
					.getAttributeMapFromXML(provisioningRequests.getExtraInfo());
			if (packExtraInfoMap != null && packExtraInfoMap.containsKey(EXTRA_INFO_OFFER_ID)) {
				offerID = "&offerid=" + packExtraInfoMap.get(EXTRA_INFO_OFFER_ID);
			}

			if (requestType.trim().equalsIgnoreCase("DCT")
					&& (subscriberObj == null || subscriberObj.activationDate() == null)) {
				return null;
			}

			// Changes for PACK- Jira 12810
			List<String> azaanCosIdList = getAzaanCosIdList();
			if (azaanCosIdList.contains(cosID)) {
				String paramName = PACK_REQ_INFO_COSID + cosID;
				String packCosID = getParamAsString(DAEMON, paramName, null);
				if (null != packCosID) {
					user_info = packCosID;
				}
			}
		}

		String url = null;
		String reqRef = "refid";
		String srvKey = "srvkey";
		String msisdn = "msisdn";

		if (requestType.trim().equalsIgnoreCase("ACT") && isSelReactive) {
			smReqType += "_ACT";
			url = getParamAsString("REACTIVATION_URL");
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_REACTIVATION);
		} else if (requestType.trim().equalsIgnoreCase("ACT") && reactRefID != null) {
			smReqType += "_ACT";
			url = getParamAsString("RETAILER_REACTIVATE_URL");
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_REACTIVATION);
		} else if (requestType.trim().equalsIgnoreCase("ACT")
				&& getParamAsString("MODES_FOR_ACTIVATION_URL_FOR_CONSENT") != null
				&& Arrays.asList(getParamAsString("MODES_FOR_ACTIVATION_URL_FOR_CONSENT").split(","))
						.contains(reqMode)) {
			smReqType += "_ACT";
			url = getParamAsString("MODE_WISE_ACTIVATION_URL");
			if (!loggingInfoMap.containsKey(SMHitLogger.TYPE))
				loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_ACTIVATION);
		} else if (requestType.trim().equalsIgnoreCase("ACT")) {
			smReqType += "_ACT";
			url = getParamAsString("ACTIVATION_URL");
			if (!loggingInfoMap.containsKey(SMHitLogger.TYPE))
				loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_ACTIVATION);
		} else if (requestType.trim().equalsIgnoreCase("DCT")) {
			smReqType += "_DCT";
			url = getParamAsString("DEACTIVATION_URL");
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_DEACTIVATION);
			if (subDownload != null && getParamAsString("BLOCK_RENEW_URL") != null)
				url = getParamAsString("BLOCK_RENEW_URL");
		} else if (requestType.trim().equalsIgnoreCase("EVT")) {
			smReqType += "_EVT";
			url = getParamAsString("EVENT_URL") + "srvkey=" + srvDefault + "&";
			srvKey = "eventkey";
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_EVENT);
		} else if (requestType.trim().equalsIgnoreCase("REN")) {
			smReqType += "_REN";
			url = getParamAsString("RENEWAL_URL");
			isRenewal = true;
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_RENEWAL);
		} else if (requestType.trim().equalsIgnoreCase("REALTIME")) {
			smReqType += "_REAL";
			url = getParamAsString("REAL_TIME_CHARGING_URL");
			String txnID = null;
			if (subscriberStatus != null) {
				String temp = subscriberStatus.selectionInfo();
				if (temp != null) {
					String trans = temp.substring(temp.indexOf(":transid:") + 9);
					txnID = trans.substring(0, trans.indexOf(":"));
				}
			}
			reqRef = "txnid=" + txnID + "&reqrefid";
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_REALTIME);
		} else if (requestType.trim().equalsIgnoreCase("DIRECTDCT")) {
			smReqType += "_DIRDCT";
			url = getParamAsString("DIRECT_DEACT_URL");
			msisdn = "cellno";
			subID = "91" + subID;
			loggingInfoMap.put(SMHitLogger.TYPE, SMHitLogger.TYPE_DIRECT_DEACT);
		}

		if (url != null) {
			/** Modified for Telfonica, attach countryCode prefix to MSISDN */
			String countryPrefix = getParamAsString("SM_MSISDN_PREFIX");
			if (countryPrefix != null)
				subID = countryPrefix + subID;

			/**
			 * If System is RRBT, adding suffix "_RRBT" to srvkey
			 */
			if (RBTDeploymentFinder.isRRBTSystem()) {
				srvkey = srvkey + "_RRBT";
				oldSrvKey = (oldSrvKey == null ? oldSrvKey : oldSrvKey + "_RRBT");
			} else if (RBTDeploymentFinder.isPRECALLSystem()) {
				srvkey = srvkey + "_PRECALL";
				oldSrvKey = (oldSrvKey == null ? oldSrvKey : oldSrvKey + "_PRECALL");
			} else if (RBTDeploymentFinder.isBGMSystem()) {
				srvkey = srvkey + "_BGM";
				oldSrvKey = (oldSrvKey == null ? oldSrvKey : oldSrvKey + "_BGM");
			}

			if (isChange) {
				boolean useUpgradeUrl = getParamAsBoolean("USE_UPGRADE_URL_FOR_SELECTIONS", "FALSE");
				smReqType += "_UPG";
				if (subscriberStatus != null && getParamAsString("CHANGE_URL") == null && !useUpgradeUrl) {
					logger.warn("RBT:: CHANGE_URL MISSING");
					return null;
				} else if ((subscriber != null || subDownload != null || useUpgradeUrl)
						&& getParamAsString("UPGRADE_URL") == null) {
					logger.warn("RBT:: UPGRADE URL MISSING");
					return null;
				}

				String change = getParamAsString("CHANGE_URL");
				if (subscriber != null || subDownload != null || useUpgradeUrl)
					change = getParamAsString("UPGRADE_URL");
				url = change + msisdn + "=" + subID + "&type=" + type + "&change_srvkey=" + srvkey + "&" + reqRef + "="
						+ refID + "&srvkey=" + oldSrvKey;

				if (subscriber != null)
					url += "&trans_refid=" + subID;
				else if (subDownload != null)
					url += "&trans_refid=" + subDownload.refID();
				else if (subscriberStatus != null)
					url += "&trans_refid=" + subscriberStatus.refID();
			} else if (isRenewal) {
				url = url + msisdn + "=" + subID + "&srvkey=" + srvkey + "&refid=" + refID;
			} else {
				url = url + msisdn + "=" + subID + "&type=" + type + "&" + srvKey + "=" + srvkey + "&" + reqRef + "="
						+ refID;
				if (trxid != null && !trxid.equalsIgnoreCase("null"))
					url = url + "&trxid=" + trxid;
				if (gifterNo != null && !gifterNo.equalsIgnoreCase("null"))
					url = url + "&gifterno=" + gifterNo;
				if (expiryDate != null)
					url = url + "&expiry=" + expiryDate;
			}
			url += "&info=";

			// Added by Sreekar on 2013-01-26 for Vf-Spain to send offertype to
			// SM
			if (extraInfo != null) {
				Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				// Added the following into the info column to update the sr_id
				// and originator info
				// vender as per the jira id RBT-11962
				if (extraInfoMap != null) {
					if (extraInfoMap.containsKey(EXTRA_INFO_OFFER_TYPE)) {
						user_info += "|OFFERTYPE:" + extraInfoMap.get(EXTRA_INFO_OFFER_TYPE);
					}
					if (extraInfoMap.containsKey(Constants.param_SR_ID)) {
						user_info += "|sr_id:" + extraInfoMap.get(Constants.param_SR_ID);
					}
					if (extraInfoMap.containsKey(Constants.param_ORIGINATOR)) {
						user_info += "|originator:" + extraInfoMap.get(Constants.param_ORIGINATOR);
					}
					if (extraInfoMap.containsKey(Constants.param_vendor.toUpperCase())) {
						user_info += "|vendor:" + extraInfoMap.get(Constants.param_vendor.toUpperCase());
					}
				}

			}

			if (getParamAsBoolean("APPEND_USERINFO_TO_SM_REQUEST", "TRUE"))
				url += user_info;

			if (requestType.trim().equalsIgnoreCase("DCT") && getParamAsBoolean("IBM_SM_INTEGRATION", "FALSE")) {
				if (subscriberStatus != null) {
					String extraInfoLocal = subscriberStatus.extraInfo();
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfoLocal);
					if (extraInfoMap != null && extraInfoMap.containsKey("DCT_DONE")) {
						url += "|dct_done:true";
					}
				}
			} else if (requestType.trim().equalsIgnoreCase("ACT") && getParamAsBoolean("IBM_SM_INTEGRATION", "FALSE")) {
				if (song_type_change) {
					url += "|song_type_change:true|old_ref_id:" + old_ref_id;
				} else if (old_ref_id != null && !(subscriberStatus.loopStatus() == 'l'
						|| subscriberStatus.loopStatus() == 'L' || subscriberStatus.loopStatus() == 'A')) {
					url += "|old_ref_id:" + old_ref_id;
				}
			}

			if (requestType.trim().equalsIgnoreCase("ACT") || requestType.trim().equalsIgnoreCase("EVT")
					|| requestType.trim().equalsIgnoreCase("REALTIME")) {
				String str = null;
				String content_id = "";
				if (provisioningRequests != null) {
					String actInfo = "";
					logger.info("Pack actInfo " + provisioningRequests.getModeInfo());
					if (provisioningRequests.getModeInfo() != null) {
						actInfo = provisioningRequests.getModeInfo().replaceAll("\\|", "/");
						actInfo = actInfo.replaceAll(":", ";");
					}
					content_id = "actinfo=" + actInfo;
					content_id +=",packid:" + provisioningRequests.getType();
					str = provisioningRequests.getMode();

					// RBT-12877: Playlist name in activation SMS
					if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "ENABLE_ODA_PACK_PLAYLIST_FEATURE",
							"FALSE")) {
						// treating type as categoryId for ODA Pack Playlist
						// Shuffle.
						Category category = rbtCacheManager.getCategory(provisioningRequests.getType());
						if (category != null && category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE) {
							String clipId = null;
							String categoryName = getCatNameID(category);
							String callerId = "ALL";
							String wavFile = null;
							String categoryId = String.valueOf(category.getCategoryId());

							Clip[] clips = RBTCacheManager.getInstance()
									.getActiveClipsInCategory(category.getCategoryId());
							if (clips != null && clips.length > 0 && clips[0] != null) {
								Clip clip = clips[0];
								clipId = String.valueOf(clip.getClipId());
								wavFile = clip.getClipRbtWavFile();
							}
							HashMap<String, String> extraInfoMap = DBUtility
									.getAttributeMapFromXML(provisioningRequests.getExtraInfo());
							if (extraInfoMap != null && extraInfoMap.get("CALLER_ID") != null) {
								callerId = extraInfoMap.get("CALLER_ID");
							}
							content_id = content_id + ",contentid=" + clipId + ",catname=" + categoryName + ",callerid="
									+ callerId + ",wavfile=" + wavFile + ",catid=" + categoryId;
							content_id = content_id + "|catname:" + categoryName;
							content_id = content_id + "|cat_type:" + category.getCategoryTpe();
						}
					}

				} else if (subscriber != null) {
					str = subscriber.activatedBy();
					if (useMode != null)
						str = useMode;
					String actInfo = "";
					if (subscriber.activationInfo() != null) {
						actInfo = subscriber.activationInfo().replaceAll("\\|", "/");
						actInfo = actInfo.replaceAll(":", ";");
					}
					content_id = "actinfo=" + actInfo;
					HashMap<String, String> extraInfoMap = rbtDBManager.getExtraInfoMap(subscriber);
					if (extraInfoMap != null && extraInfoMap.containsKey(SELECTION_MODE)) {
						str = extraInfoMap.get(SELECTION_MODE);
					}
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_COPY_TYPE)) {
						String copyType = extraInfoMap.get(EXTRA_INFO_COPY_TYPE);
						if (copyType != null && copyType.equalsIgnoreCase(EXTRA_INFO_COPY_TYPE_OPTIN))
							content_id = content_id + ",copy_type=optin";
					}
					if (extraInfoMap != null && extraInfoMap.containsKey("SCRN")) {
						String scratchNo = extraInfoMap.get("SCRN");
						content_id = content_id + ",scratch_no=" + scratchNo;

					}
					// Added for RBT-17883
					if (extraInfoMap != null
							&& extraInfoMap.get(EXTRA_INFO_CHARGE_MDN) != null) {
						content_id = content_id + ",userMDN=" + subID
								+ ",chargeMDN="
								+ extraInfoMap.get(EXTRA_INFO_CHARGE_MDN);
					}
					content_id = content_id + ",cosid:" + subscriber.cosID() + "|cosid:" + subscriber.cosID();

					if (getParamAsBoolean("IS_TATA_GSM_IMPL", "FALSE")) {
						if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)) {
							wdsInfo = extraInfoMap.get(EXTRA_INFO_WDS_QUERY_RESULT);
						} else
							return null;
						content_id += "|" + EXTRA_INFO_WDS_QUERY_RESULT + ":" + wdsInfo;
					}

					if (extraInfoMap != null && extraInfoMap.containsKey("RET")) {
						content_id += "|" + "ret:" + extraInfoMap.get("RET");
					}

				} else if (subscriberStatus != null) {
					str = subscriberStatus.selectedBy();
					String caller = "ALL";
					if (subscriberStatus.callerID() != null)
						caller = subscriberStatus.callerID();
					String actInfo = "";
					if (subscriberStatus.selectionInfo() != null) {
						actInfo = subscriberStatus.selectionInfo().replaceAll("\\|", "/");
						actInfo = actInfo.replaceAll(":", ";");
					}
					String wavFile = null;
					if (categoryObj != null && Utility.isShuffleCategory(categoryObj.getCategoryTpe())) {
						wavFile = "rbt_" + categoryObj.getCategoryPromoId() + "_rbt";
					} else {
						wavFile = subscriberStatus.subscriberFile();
					}
					content_id = "contentid=" + getClipIDRBTWav(subscriberStatus.subscriberFile(), categoryObj)
							+ ",catname=" + getCatNameID(categoryObj) + ",actinfo=" + actInfo + ",callerid=" + caller
							+ ",wavfile=" + wavFile + ",catid=" + subscriberStatus.categoryID();
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriberStatus.extraInfo());
					//Added for RBT-17883
					if(extraInfoMap != null && extraInfoMap.get(EXTRA_INFO_CHARGE_MDN) != null){
						content_id = content_id+",userMDN="+subID+",chargeMDN="+extraInfoMap.get(EXTRA_INFO_CHARGE_MDN);
					}
					//Ended for RBT-17883
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_COPY_TYPE)) {
						String copyType = extraInfoMap.get(EXTRA_INFO_COPY_TYPE);
						if (copyType != null && copyType.equalsIgnoreCase(EXTRA_INFO_COPY_TYPE_OPTIN))
							content_id = content_id + ",copy_type=optin";
					}
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_COPY_MODE)) {
						String copyMode = extraInfoMap.get(EXTRA_INFO_COPY_MODE);
						if (copyMode != null)
							content_id = content_id + ",copy_conf_mode=" + copyMode;
					}
					if (extraInfoMap != null && extraInfoMap.containsKey("SCRN")) {
						String scratchNo = extraInfoMap.get("SCRN");
						content_id = content_id + ",scratch_no=" + scratchNo;

					}
					if (extraInfoMap != null
							&& extraInfoMap.get("wifiwap") != null
							&& "true".equalsIgnoreCase(extraInfoMap
									.get("wifiwap"))) {
						content_id += content_id + ",wifiwap=true";
					}
					content_id = content_id + "|catname:" + getCatNameID(categoryObj) + "|cat_type:"
							+ subscriberStatus.categoryType();

					if (extraInfoMap != null && extraInfoMap.containsKey("RET")) {
						content_id += "|" + "ret:" + extraInfoMap.get("RET");
					}
					// RBT-14177 - Profile deactivation msg to be sent on expiry
					if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
							"ENABLE_VALITY_DET_FOR_PROFILE_SEL_FEATURE", "FALSE") && subscriberStatus.status() == 99) {
						String[] getValidityInfo = getValidityUnitForProfileDCT(subscriberStatus);
						content_id += "|" + "validityInMillis:" + getValidityInfo[0];
					}
				} else if (subDownload != null) {
					String actInfo = "";
					logger.info("Download selectionInfo " + subDownload.selectionInfo());
					if (subDownload.selectionInfo() != null) {
						actInfo = subDownload.selectionInfo().replaceAll("\\|", "/");
						actInfo = actInfo.replaceAll(":", ";");
					}

					str = subDownload.selectedBy();
					content_id = "contentid=" + getClipIDRBTWav(subDownload.promoId(), categoryObj) + ",catname="
							+ getCatNameID(categoryObj) + ",actinfo=" + actInfo + ",wavfile=" + subDownload.promoId()
							+ ",catid=" + subDownload.categoryID();
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subDownload.extraInfo());
					//Added for RBT-17883
					if(extraInfoMap != null && extraInfoMap.get(EXTRA_INFO_CHARGE_MDN) != null){
						content_id = content_id+",userMDN="+subID+",chargeMDN="+extraInfoMap.get(EXTRA_INFO_CHARGE_MDN);
					}
					//Ended for RBT-17883
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_COPY_TYPE)) {
						String copyType = extraInfoMap.get(EXTRA_INFO_COPY_TYPE);
						if (copyType != null && copyType.equalsIgnoreCase(EXTRA_INFO_COPY_TYPE_OPTIN))
							content_id = content_id + ",copy_type=optin";
					}
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_COPY_MODE)) {
						String copyMode = extraInfoMap.get(EXTRA_INFO_COPY_MODE);
						if (copyMode != null)
							content_id = content_id + ",copy_conf_mode=" + copyMode;
					}
					if (extraInfoMap != null
							&& extraInfoMap.get("wifiwap") != null
							&& "true".equalsIgnoreCase(extraInfoMap
									.get("wifiwap"))) {
						content_id += content_id + ",wifiwap=true";
					}
					if (getParamAsBoolean("ADD_CAT_NAME_INFO", "TRUE"))
						content_id += "|catname:" + getCatNameID(categoryObj);

					if (extraInfoMap != null && extraInfoMap.containsKey("RET")) {
						content_id += "|" + "ret:" + extraInfoMap.get("RET");
					}

				}

				if (str != null) {
					if (getParamAsBoolean("MODIFY_SEARCH_MODE", "FALSE")) {
						if (str.equals("VP"))
							str = "VOICE";
						else if (str.equals("WAP"))
							str = "GPRS";
						else if (str.toLowerCase().endsWith("search"))
							str = "SEARCH";
					}
					String supportedModes = getParamAsString("DAEMON", "SG_OCG_SUPPORTED_MODES", " ").toLowerCase();
					if (supportedModes.equals(" ")
							|| Arrays.asList(supportedModes.split(",")).contains(str.toLowerCase()))
						url = url + ("|CONTENT_ID:" + content_id) + "&mode=" + str.toUpperCase();
					else
						url = url + "&mode=OTHER";

					if (giftTransactionID != null && requestType.trim().equalsIgnoreCase("ACT"))
						url = url + "&trans_id=" + giftTransactionID;
				}
				if (language == null) {
					String lang = getParamAsString("COMMON", "DEFAULT_LANGUAGE", "eng").toLowerCase();
					url = url + "&language=" + lang;
				}
				if (language != null)
					url = url + "&language=" + language;

				// Offer parameters being passed to Submgr.
				// format :
				// offparams=key1:value1|key2:value2|IMEI_NO:1234567890123456|
				String offparams = "";
				Set<String> keySet = offerParams.keySet();
				for (String key : keySet) {
					offparams += key + ":" + offerParams.get(key) + "|";
				}
				if (offparams.length() > 0)
					url = url + "&offparams=" + (offparams);

				// Idea 121
				if (basePrice != null) {
					url = url + basePrice;
				}

				if (childPrice != null) {
					url = url + childPrice;
				}

			} else if (requestType.trim().equalsIgnoreCase("DCT") || requestType.trim().equalsIgnoreCase("DIRECTDCT")) {
				String str = null;
				if (provisioningRequests != null) {
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(provisioningRequests.getExtraInfo());
					logger.info("It is a deactivation case & provisioning req is not null and extra info map is  "
							+ extraInfoMap);
					if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_PACK_DEACTIVATION_MODE))
						str = extraInfoMap.get(EXTRA_INFO_PACK_DEACTIVATION_MODE);
				} else if (subscriber != null)
					str = subscriber.deactivatedBy();
				else if (subscriberStatus != null)
					str = subscriberStatus.deSelectedBy();
				else if (subDownload != null)
					str = subDownload.deactivatedBy();

				if (str != null) {
					if (getParamAsBoolean("MODIFY_SEARCH_MODE", "FALSE")) {
						if (str.equals("VP"))
							str = "VOICE";
						else if (str.equals("WAP"))
							str = "GPRS";
						else if (str.toLowerCase().endsWith("search"))
							str = "SEARCH";
					}
					String content_id = "";
					if (subscriber != null) {
						String actInfo = "";
						if (subscriber.activationInfo() != null) {
							actInfo = subscriber.activationInfo().replaceAll("\\|", "/");
							actInfo = actInfo.replaceAll(":", ";");
						}
						content_id = "actinfo=" + actInfo + ",cosid:" + subscriber.cosID() + "|cosid:"
								+ subscriber.cosID();
					} else if (subscriberStatus != null) {
						String caller = "ALL";
						if (subscriberStatus.callerID() != null)
							caller = subscriberStatus.callerID();
						String actInfo = "";
						if (subscriberStatus.selectionInfo() != null) {
							actInfo = subscriberStatus.selectionInfo().replaceAll("\\|", "/");
							actInfo = actInfo.replaceAll(":", ";");
						}
						content_id = "contentid=" + getClipIDRBTWav(subscriberStatus.subscriberFile(), categoryObj)
								+ ",catname=" + getCatNameID(categoryObj) + ",actinfo=" + actInfo + ",callerid="
								+ caller + ",wavfile=" + subscriberStatus.subscriberFile() + ",catid="
								+ subscriberStatus.categoryID() + "|catname:" + getCatNameID(categoryObj) + "|cat_type:"
								+ subscriberStatus.categoryType();

					} else if (subDownload != null) {
						String actInfo = "";
						if (subDownload.selectionInfo() != null) {
							actInfo = subDownload.selectionInfo().replaceAll("\\|", "/");
							actInfo = actInfo.replaceAll(":", ";");
						}
						content_id = "contentid=" + getClipIDRBTWav(subDownload.promoId(), categoryObj) + ",catname="
								+ getCatNameID(categoryObj) + ",actinfo=" + actInfo + ",wavfile="
								+ subDownload.promoId() + ",catid=" + subDownload.categoryID();
						if (getParamAsBoolean("ADD_CAT_NAME_INFO", "TRUE"))
							content_id += "|catname:" + getCatNameID(categoryObj);

					} else if (provisioningRequests != null) {
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(provisioningRequests.getExtraInfo());
						String actInfo = "";
						if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO)) {
							actInfo = extraInfoMap.get(EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO).replaceAll("\\|", "/");
							actInfo = actInfo.replaceAll(":", ";");
						}
						content_id = content_id + "actinfo=" + actInfo;

						// RBT-12877: Playlist name in activation SMS
						if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
								"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
							// treating type as categoryId for ODA Pack Playlist
							// Shuffle.
							Category category = rbtCacheManager.getCategory(provisioningRequests.getType());
							if (category != null && category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE) {
								String clipId = null;
								String categoryName = getCatNameID(category);
								String callerId = "ALL";
								String wavFile = null;
								String categoryId = String.valueOf(category.getCategoryId());

								Clip[] clips = RBTCacheManager.getInstance()
										.getActiveClipsInCategory(category.getCategoryId());
								if (clips != null && clips.length > 0 && clips[0] != null) {
									Clip clip = clips[0];
									clipId = String.valueOf(clip.getClipId());
									wavFile = clip.getClipRbtWavFile();
								}
								if (extraInfoMap != null && extraInfoMap.get("CALLER_ID") != null) {
									callerId = extraInfoMap.get("CALLER_ID");
								}
								content_id = content_id + ",contentid=" + clipId + ",catname=" + categoryName
										+ ",callerid=" + callerId + ",wavfile=" + wavFile + ",catid=" + categoryId;
								//Added for RBT-17883
								if(extraInfoMap != null && extraInfoMap.get(EXTRA_INFO_CHARGE_MDN) != null){
									content_id = content_id+",userMDN="+subID+",chargeMDN="+extraInfoMap.get(EXTRA_INFO_CHARGE_MDN);
								}
								//Ended for RBT-17883
								content_id = content_id + "|catname:" + categoryName;
								content_id = content_id + "|cat_type:" + category.getCategoryTpe();
							}
						}
					}
					String supportedModes = getParamAsString("DAEMON", "SG_OCG_SUPPORTED_MODES", " ").toLowerCase();
					if (supportedModes.equals(" ")
							|| Arrays.asList(supportedModes.split(",")).contains(str.toLowerCase()))
						url = url + ("|CONTENT_ID:" + content_id) + "&mode=" + str.toUpperCase();
					else
						url = url + "&mode=OTHER";

					// Changes made of immidiate deactivation of corporate
					// selection
					String delayDeact = getParamAsString("DAEMON", "DELAY_DEACTIVATION", " ");
					if (delayDeact != null && delayDeact.equalsIgnoreCase("false")) {
						logger.info("RBT::getSMURL delay deact is false");
						HashMap<String, String> extraInfoMap = null;
						if (subDownload != null) {
							logger.info("RBT::getSMURL download model");
							extraInfoMap = DBUtility.getAttributeMapFromXML(subDownload.extraInfo());
						} else if (subscriberStatus != null) {
							logger.info("RBT::getSMURL selection model ");
							extraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());

						}
						if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.CAMPAIGN_ID)) {
							logger.info("RBT::getSMURL delay_dct is added in sm url ");
							url = url + "&delay_dct=false";
						}
					}

					// Changes end
				}

				if (isDelayedDeact)
					url += "&delay_dct=true";

				// RBT-2951 remote DCT to be disabled for configurable modes,
				// Feature basically from Telefonica Spaing operator
				if (devProvDone != null)
					url = url + devProvDone;

			}
		}
		// RBT-14301: Uninor MNP changes.
		try {
			if (subscriber != null) {
				url = appendSiteId(subscriber.circleID(), url);
			} else if (subscriberStatus != null) {
				url = appendSiteId(subscriberStatus.circleId(), url);
			} else if (subDownload != null) {
				String subId = subDownload.subscriberId();
				Subscriber subscriberForDownload = getSubscriber(subId);
				url = appendSiteId(subscriberForDownload.circleID(), url);
			}
		} catch (MappedSiteIdNotFoundException e) {
			logger.warn("", e);
			return null;
		}
		if (subscriber != null && subscriber.subYes().equalsIgnoreCase("S")) {
			url = url + "&substatus=A";
		}
		if (offerID != null) {
			url = url + offerID;
			loggingInfoMap.put(SMHitLogger.OFFER_ID, offerID);
		}
		if (reactRefID != null) {
			url = url + reactRefID;
			loggingInfoMap.put(SMHitLogger.REACT_REFID, reactRefID);
		}

		if (baseConsentParams != null && !baseConsentParams.equalsIgnoreCase("")
				&& requestType.trim().equalsIgnoreCase("ACT")) {
			url = url + baseConsentParams;
			loggingInfoMap.put(SMHitLogger.BASE_CONSENT_PARAM, baseConsentParams);
		}

		if (selConsentParams != null && !selConsentParams.equalsIgnoreCase("")
				&& requestType.trim().equalsIgnoreCase("ACT")) {
			url = url + selConsentParams;
			loggingInfoMap.put(SMHitLogger.SEL_CONSENT_PARAM, selConsentParams);
		}

		result.put(URL, url);
		loggingInfoMap.put(SMHitLogger.URL, url);
		result.put(REQUEST_TYPE, smReqType);

		return result;

	}


	private String getParamAsString(String param) {
		try {
			return m_rbtParamCacheManager.getParameter("DAEMON", param, null).getValue();
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}

	private String getParamAsString(String type, String param, String defualtVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defualtVal;
		}
	}

	public static int getParamAsInt(String param, int defaultVal) {
		try {
			String paramVal = m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal + "").getValue();
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return defaultVal;
		}
	}

	public static boolean getParamAsBoolean(String param, String defaultVal) {
		try {
			boolean value = m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal).getValue()
					.equalsIgnoreCase("TRUE");
			logger.debug("Configured param: " + param + ", value: " + value);
			return value;
		} catch (Exception e) {
			logger.info("Unable to get param: " + param + ", returning default value: " + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private boolean getParamAsBoolean(String type, String param, String defaultVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private String getCatNameID(Category category) {
		String catName = "";
		if (category != null) {
			catName = category.getCategoryName();
		}
		if (catName.length() > 20)
			catName = catName.substring(0, 20);
		catName = convertWindow1252(catName);
		return catName;
	}

	private String getClipIDRBTWav(String strWavFile, Category categoryObj) {
		String clipID = "MISSING";
		Clip clip = getClipRBT(strWavFile);
		if (clip != null) {
			clipID = "" + clip.getClipId();
		}

		if (categoryObj != null) {
			String categoryType = Utility.getCategoryType(categoryObj.getCategoryTpe());
			Parameters clipIdParam = null;
			if (categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE)) {
				clipIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "KARAOKE_CLIP_ID", null);
			} else if (categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
				clipIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_ID", null);
			}
			if (clipIdParam != null) {
				clipID = clipIdParam.getValue();
			}
		}

		return clipID;
	}

	private Subscriber getSubscriber(String subscriberID) {
		return rbtDBManager.getSubscriber(subscriberID);
	}

	private Clip getClipRBT(String rbt_wav) {
		if (rbt_wav != null && rbt_wav.indexOf("rbt_slice_") != -1) {
			String str[] = rbt_wav.split("rbt_slice_");
			String clipId = str[1].substring(0, str[1].indexOf("_"));
			return rbtCacheManager.getClip(clipId);
		}
		return rbtCacheManager.getClipByRbtWavFileName(rbt_wav, "ALL");
	}

	private Category getCategory(int catID) {
		return rbtCacheManager.getCategory(catID);
	}

	public String appendSiteId(String circleId, String url) throws MappedSiteIdNotFoundException {
		String siteId = getParamAsString(COMMON, "CONFIGURED_DEFAULT_SITE_ID", "0");
		String tempSiteId = null;
		if (!url.contains("%siteid%")) {
			return url;
		}
		logger.info("appendSiteId:circleId " + circleId);
		logger.info("appendSiteId:circleIdToSiteIdMapping " + circleIdToSiteIdMapping);
		if (circleIdToSiteIdMapping != null && !circleIdToSiteIdMapping.isEmpty()) {
			if (circleId != null) {
				tempSiteId = circleIdToSiteIdMapping.get(circleId);
			}
			logger.info("appendSiteId: tempSiteId " + tempSiteId);
			if (tempSiteId != null && !tempSiteId.equalsIgnoreCase(siteId)) {
				siteId = tempSiteId;
			} else if ((tempSiteId == null || tempSiteId.trim().isEmpty())) {
				throw new MappedSiteIdNotFoundException(
						"For " + circleId + " siteId info not found in CIRCLEID_TO_SITEID_MAPPING for subscriber.");
			}
			url = url.replaceAll("%siteid%", siteId);
		} else {
			url = url.replaceAll("%siteid%", siteId);
		}
		logger.info("appendSiteId: siteId " + siteId);
		return url;
	}

	private String convertWindow1252(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		String retValue = value;
		try {
			if (checkForPattern(value)) {
				logger.debug("Value before replacing Special Characters " + value);
				value = Normalizer.normalize(value, Normalizer.Form.NFD);
				Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
				value = pattern.matcher(value).replaceAll("");
				logger.debug("Value after replacing Special Characters " + value);
			}
			if (getParamAsBoolean("COMMON", "SUPPORT_ENCODE_WIN1252_CONTENTFIELD", "FALSE")) {
				retValue = new String(value.getBytes(), Charset.forName("Windows-1252"));
			}
			return value;
		} catch (Exception e) {
			logger.error("Exception while encoding Windows-1252", e);
		}
		return retValue;
	}

	private boolean checkForPattern(String value) {
		String regex = RBTParametersUtils.getParamAsString("COMMON", "SPECIAL_CHAR_PATTERN", null);
		if (regex == null || regex.trim().isEmpty()) {
			return false;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		return matcher.find();
	}

	public static String[] getValidityUnitForProfileDCT(SubscriberStatus subscriberStatus) {
		// HH converts hour in 24 hours format (0-23), day calculation
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		String dateStart = format.format(subscriberStatus.endTime());
		String dateStop = format.format(date);
		String[] returnValue = new String[2];
		returnValue[0] = "0";
		returnValue[1] = "MILLISECOND";
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = format.parse(dateStart);
			d2 = format.parse(dateStop);
			DateTime dateTimeFrom = new DateTime(d2.getTime());
			DateTime dateTimeTo = new DateTime(d1.getTime());
			Period period = new Period(dateTimeFrom, dateTimeTo);
			logger.info(period.getYears() + " years," + period.getMonths() + " months," + period.getWeeks() + " weeks,"
					+ period.getDays() + " days," + period.getHours() + " hours," + period.getMinutes() + " minutes,"
					+ period.getSeconds() + " seconds," + period.getMillis() + "milliseconds");
			Duration duration = new Duration(dateTimeFrom, dateTimeTo);
			long millis = duration.getMillis();
			returnValue[0] = String.valueOf(millis);
			returnValue[1] = "MILLISECOND";

			logger.info("returnValue:  " + returnValue);
		} catch (Exception e) {
			logger.debug("Exception Occured in parsing:" + e.getMessage());
		}
		return returnValue;
	}

	private String getTransID(String info) {
		if (info == null || info.trim().length() == 0)
			return null;
		if (info.lastIndexOf(":trxid:") > -1) {
			int index1 = info.lastIndexOf(":trxid:");
			int index2 = info.indexOf(":", index1 + 7);
			if (index2 > index1 + 7) {
				String returnVal = info.substring(index1 + 7, index2);
				if (returnVal != null && returnVal.trim().length() > 0 && !returnVal.trim().equalsIgnoreCase("null"))
					return returnVal.trim();
			}
		}
		return null;
	}

	private String getSdpInfoParam(Subscriber subscriber, SubscriberStatus subscriberStatus) {
		// changed for bug
		if (subscriber == null && subscriberStatus == null)
			return null;
		boolean isSdpParamsToBeSent = getParamAsBoolean("SEND_SDP_PARAMS", "FALSE");
		if (!isSdpParamsToBeSent) {
			return null;
		}

		HashMap<String, String> selExtraInfoMap = null;
		if (subscriberStatus != null) {
			selExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());
		}

		HashMap<String, String> extraInfoMap = null;
		if (subscriber != null) {
			extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		}

		if (extraInfoMap == null && selExtraInfoMap == null)
			return null;
		String sdpInfo = null;
		String info = "";

		if (selExtraInfoMap != null && selExtraInfoMap.containsKey("seapitype")) {
			info += "seapitype:" + selExtraInfoMap.get("seapitype") + "|";
		} else if (selExtraInfoMap == null && extraInfoMap != null && extraInfoMap.containsKey("seapitype")) {
			info += "seapitype:" + extraInfoMap.get("seapitype") + "|";
		}

		if (selExtraInfoMap != null && selExtraInfoMap.containsKey("sdpomtxnid")) {
			info += "sdpomtxnid:" + selExtraInfoMap.get("sdpomtxnid") + "|";
		} else if (selExtraInfoMap == null && extraInfoMap != null && extraInfoMap.containsKey("sdpomtxnid")) {
			info += "sdpomtxnid:" + extraInfoMap.get("sdpomtxnid") + "|";
		}

		if (!info.equalsIgnoreCase("")) {
			info += "statuscode:200";
			sdpInfo = info;
		}
		logger.info("SDP Info = " + sdpInfo);
		return sdpInfo;
	}

	private String getUserInfo(Clip clipObj, Category categoryObj, Subscriber subscribers, SubscriberStatus selection,
			SubscriberDownloads download) throws WDSInfoNotFoundException {
		String user_info = "";
		boolean isShuffleCategory = Utility.isShuffleCategory(categoryObj.getCategoryTpe())
				&& (categoryObj.getCategoryTpe() != iRBTConstant.AUTO_DOWNLOAD_SHUFFLE);
		if (isShuffleCategory && getParamAsBoolean(COMMON, "SENDING_CATEGORY_INFO_FOR_SHUFFLE", "FALSE")) {

			String grammar = categoryObj.getCategoryGrammar();
			if (grammar != null && grammar.equalsIgnoreCase("UGC")) {
				user_info += ("|songtype:UGC");
			}
			String promoId = categoryObj.getCategoryPromoId();
			String name = convertWindow1252(getCategoryName(categoryObj));
			String movieName = convertWindow1252(categoryObj.getCategoryName());

			String categoryInfo = convertWindow1252(getCategoryInfo(categoryObj));
			if (promoId == null) {
				promoId = getParamAsString(COMMON, "CONFIGURED_CATEGORY_PROMOID_FOR_SHUFFLE", null);
			}
			promoId = convertWindow1252(promoId);
			user_info += name + "songcode:" + promoId;
			if (categoryInfo != null && !categoryInfo.equalsIgnoreCase("null"))
				user_info = user_info + "|" + categoryInfo;
			user_info = user_info + "|moviename:" + movieName;

		} else if (clipObj != null) {
			String grammar = clipObj.getClipGrammar();
			if (grammar != null && grammar.equalsIgnoreCase("UGC")) {
				user_info += ("|songtype:UGC");
			}
			String promoId = clipObj.getClipPromoId();
			String artist = convertWindow1252(getArtistName(clipObj));
			String name = convertWindow1252(getSongName(clipObj));
			String movieName = convertWindow1252(getMovieName(clipObj));

			// Added for Tef-Spain
			// Auto_download check done so as to consider that shuffl's song as
			// normal song.
			boolean isShuffle = Utility.isShuffleCategory(categoryObj.getCategoryTpe())
					&& (categoryObj.getCategoryTpe() != iRBTConstant.AUTO_DOWNLOAD_SHUFFLE);
			String clipInfo = null;

			boolean isShuffleCategoryTypeConfiguredForSendingClipInfo = false; // Config
																				// added
																				// for
																				// RBT-12877
			if (shuffleCategoryTypesForSendingClipInfoList == null
					|| shuffleCategoryTypesForSendingClipInfoList.isEmpty()
					|| shuffleCategoryTypesForSendingClipInfoList
							.contains(String.valueOf(categoryObj.getCategoryTpe()))) {
				isShuffleCategoryTypeConfiguredForSendingClipInfo = true;
			}
			logger.debug("isShuffleCategoryTypesConfiguredForSendingClipInfo: "
					+ isShuffleCategoryTypeConfiguredForSendingClipInfo);
			if (!isShuffle || (isShuffle && getParamAsBoolean("SEND_CLIP_INFO_FOR_SHUFFLE", "TRUE")
					&& isShuffleCategoryTypeConfiguredForSendingClipInfo)) {
				clipInfo = convertWindow1252(getClipInfo(clipObj));
			}

			if (categoryObj != null && isShuffle) {
				promoId = categoryObj.getCategoryPromoId();
			}
			promoId = convertWindow1252(promoId);
			user_info += name + "songcode:" + promoId;

			String selOrDownloadExtraInfo = null;
			HashMap<String, String> selOrDownloadExtraInfoMap = null;
			String packRefId = null ;
			String chargeClass = null;
			String subId  = null;
			if (selection != null) {
				selOrDownloadExtraInfo = selection.extraInfo();
				selOrDownloadExtraInfoMap = DBUtility.getAttributeMapFromXML(selection.extraInfo());
				chargeClass = selection.classType();
				if(selOrDownloadExtraInfoMap!=null && !selOrDownloadExtraInfoMap.isEmpty() && selOrDownloadExtraInfoMap.size()>0){
				packRefId = selOrDownloadExtraInfoMap.get("PROV_REF_ID");
				}
				subId = selection.subID();
			} else if (download != null) {
				selOrDownloadExtraInfo = download.extraInfo();
				chargeClass = download.classType();
				subId = download.subscriberId();
				selOrDownloadExtraInfoMap = DBUtility.getAttributeMapFromXML(download.extraInfo());
				if(selOrDownloadExtraInfoMap!=null && !selOrDownloadExtraInfoMap.isEmpty() && selOrDownloadExtraInfoMap.size()>0){
				packRefId = selOrDownloadExtraInfoMap.get("PROV_REF_ID");
				}
			}

			ProvisioningRequests provisioningRequests =  null;
			if(subId != null && !subId.isEmpty() && packRefId != null && !packRefId.isEmpty()){
			 provisioningRequests = ProvisioningRequestsDao.getByTransId(subId, packRefId);
			}
			// param=:CP:TF-Spain-Onmobile|ISRC:|UPC:|AUTHOR:|CPC:32349|CPCF:32347|CC:35446|CCF:35444|SPI:4200805|RBY:TME
			// This parameter will replace the values configured in the
			// parameters table while sending it to SM.
			String packSelectionReplacableInfo = getParamAsString("PACK_SELECTION_CLIP_INFO");
			if (chargeClass != null && getParamAsString("PACK_SELECTION_CLIP_INFO_" + chargeClass) != null)
				packSelectionReplacableInfo = getParamAsString("PACK_SELECTION_CLIP_INFO_" + chargeClass);
			
			
			boolean isFreemium  = false ; 
			String freemiumChargeClass = RBTParametersUtils.getParamAsString(COMMON,
					"FREEMIUM_CHARGE_CLASSES", null);
					
			List<String> freemiumChargeClasses = new ArrayList<String>();
			Map<String, String> clipInfoMap = new HashMap<String, String>();
					
			
			if (chargeClass != null && freemiumChargeClass != null
					&& !freemiumChargeClass.isEmpty()) {

				freemiumChargeClasses = Arrays.asList(freemiumChargeClass
						.split(","));
				if (provisioningRequests != null
						&& freemiumChargeClasses.contains(provisioningRequests
								.getChargingClass())) {
					isFreemium = true;

				} else if (provisioningRequests == null && freemiumChargeClasses.contains(chargeClass)) {
					isFreemium = true;
				}
			}

			if (clipInfo != null && !clipInfo.equalsIgnoreCase("null") && packSelectionReplacableInfo != null
					&& selOrDownloadExtraInfo != null && selOrDownloadExtraInfo.contains("PACK") ) {
				
				clipInfoMap = parseClipInfo(clipInfo);
				Map<String, String> toBeReplacedClipInfoMap = parseClipInfo(packSelectionReplacableInfo);

				for (Entry<String, String> entry : toBeReplacedClipInfoMap.entrySet()) {
					clipInfoMap.put(entry.getKey(), entry.getValue());
				}
				clipInfo = parseClipInfoMap(clipInfoMap);
			}
			
			
			if (clipInfo != null && !clipInfo.equalsIgnoreCase("null") && isFreemium ) {
				
				clipInfoMap = parseClipInfo(clipInfo);
				for (Entry<String, String> entry : clipInfoMap.entrySet()) {
					String value = entry.getValue();
					String key = entry.getKey();
					Map<String, String> codesMapping = MapUtils
							.convertIntoMap(
									getParamAsString("SELECTION_CLIP_INFO_MAPPED_CODES_"
											+ key), ";", "=", null);

					if (isFreemium && codesMapping != null && !codesMapping.isEmpty()) {
						if (codesMapping.containsKey(entry.getValue())) {
							value = codesMapping.get(entry.getValue());
						}
					}
					clipInfoMap.put(entry.getKey(), value);
				}
				clipInfo = parseClipInfoMap(clipInfoMap);
			}
			
			

			if (clipInfo != null && !clipInfo.equalsIgnoreCase("null"))
				user_info = user_info + "|" + clipInfo;
			user_info = user_info + "|moviename:" + movieName + "|artist:" + artist;
			if (clipObj.getContentType() != null)
				user_info += "|contentType:" + convertWindow1252(clipObj.getContentType());
		} else if (categoryObj.getCategoryTpe() == iRBTConstant.RECORD) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_INFO", null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_PROMOID", null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_MOVIENAME", null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_ARTISTNAME", null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam, promoIdParam, movieNameParam, artistNameParam);
		} else if (categoryObj.getCategoryTpe() == iRBTConstant.KARAOKE) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter("DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_INFO",
					null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_PROMOID",
					null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter("DAEMON",
					"MERIDHUN_OR_KARAOKE_CLIP_MOVIENAME", null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter("DAEMON",
					"MERIDHUN_OR_KARAOKE_CLIP_ARTISTNAME", null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam, promoIdParam, movieNameParam, artistNameParam);
		} else if (categoryObj.getCategoryId() == 99) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter("DAEMON", "PROFILE_RMO_CLIP_INFO", null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "PROFILE_RMO_CLIP_PROMOID", null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter("DAEMON", "PROFILE_RMO_CLIP_MOVIENAME",
					null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter("DAEMON", "PROFILE_RMO_CLIP_ARTISTNAME",
					null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam, promoIdParam, movieNameParam, artistNameParam);
		} else {
			user_info += ("songname:|songcode:|moviename:|artist:");
		}

		if (getParamAsBoolean("IS_TATA_GSM_IMPL", "FALSE")) {
			String cosID = subscribers != null ? subscribers.cosID() : "";
			String wdsInfo = "";
			HashMap<String, String> extraInfoMap = rbtDBManager.getExtraInfoMap(subscribers);
			if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)) {
				wdsInfo = extraInfoMap.get(EXTRA_INFO_WDS_QUERY_RESULT);
			} else {
				throw new WDSInfoNotFoundException("WDS info not found for subscriber " + subscribers.subID());
			}

			user_info += ("|cosid:" + cosID + "|" + EXTRA_INFO_WDS_QUERY_RESULT + ":" + wdsInfo);
		}

		if (download != null) {
			if (download.selectionInfo() != null
					&& download.selectionInfo().toLowerCase().indexOf("refund:true") != -1) {
				user_info += "|REFUND:true";
			}
		}
		return user_info;
	}

	private String getUserInfoForKarokeAndRMO(Parameters clipInfoParam, Parameters promoIdParam,
			Parameters movieNameParam, Parameters artistNameParam) {
		String clipInfo = "";
		if (clipInfoParam != null) {
			clipInfo = clipInfoParam.getValue();
			clipInfo = (clipInfo != null) ? clipInfo.replaceAll("=", ":") : "";
			if (clipInfo.length() > 0) {
				clipInfo += "|";
			}
		}
		String clipPromoId = "";
		if (promoIdParam != null) {
			clipPromoId = promoIdParam.getValue();
			clipPromoId = (clipPromoId != null) ? clipPromoId : "";
		}
		String clipMovieId = "";
		if (movieNameParam != null) {
			clipMovieId = movieNameParam.getValue();
			clipMovieId = (clipMovieId != null) ? clipMovieId : "";
		}
		String clipArtistId = "";
		if (artistNameParam != null) {
			clipArtistId = artistNameParam.getValue();
			clipArtistId = (clipArtistId != null) ? clipArtistId : "";
		}
		return ("songname:|songcode:" + convertWindow1252(clipPromoId) + "|" + convertWindow1252(clipInfo)
				+ "moviename:" + convertWindow1252(clipMovieId) + "|artist:" + convertWindow1252(clipArtistId));
	}

	private String getClipInfo(Clip clip) {
		String clipInfo = clip.getClipInfo();
		if (clipInfo != null) {
			clipInfo = clipInfo.replaceAll("=", ":");
			clipInfo = clipInfo.replaceAll("&", " ");
		}
		return clipInfo;
	}

	private String getArtistName(Clip clip) {
		String artist = clip.getArtist();
		artist = (artist != null ? artist.replaceAll("&", "%26") : artist);
		return artist;
	}

	private String getMovieName(Clip clip) {
		String movieName = clip.getAlbum();
		if (movieName != null && movieName.length() > 20)
			movieName = movieName.substring(0, 20);
		movieName = (movieName != null ? movieName.replaceAll("&", "%26") : movieName);
		return movieName;
	}

	private Map<String, String> parseClipInfo(String clipInfo) {
		Map<String, String> clipInfoMap = new HashMap<String, String>();
		StringTokenizer stk = new StringTokenizer(clipInfo, "|");
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken();
			String[] split = token.split(":", -1);
			clipInfoMap.put(split[0], split[1]);
		}
		return clipInfoMap;
	}

	private String parseClipInfoMap(Map<String, String> clipInfoMap) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : clipInfoMap.entrySet()) {
			sb.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		return sb.substring(0, sb.length() - 1);
	}

	private List<String> getAzaanCosIdList() {
		List<String> azaanCosIdList = new ArrayList<String>();
		String cosIds = getParamAsString(DAEMON, AZAAN_COS_ID_LIST, null);
		azaanCosIdList = ListUtils.convertToList(cosIds, ",");
		return azaanCosIdList;
	}

	private boolean isSameCaller(String firstCallerId, String secondCallerId) {
		if (firstCallerId == null && secondCallerId == null)
			return true;
		else if (firstCallerId != null && firstCallerId.equals(secondCallerId))
			return true;
		return false;
	}

	private String getSongName(Clip clip) {
		String songName = clip.getClipName();
		int clipLengthLimit = RBTParametersUtils.getParamAsInt("DAEMON", "SM_URL_CLIP_NAME_LENGTH_LIMIT", -1);
		if (clipLengthLimit != -1 && songName.length() > clipLengthLimit) {
			songName = songName.substring(0, clipLengthLimit);
		}
		String songNameStr = "songname:" + songName + "|";

		for (String language : supportedLangList) {
			if (clip.getClipName(language) != null) {
				songName = clip.getClipName(language);
				if (clipLengthLimit != -1 && songName.length() > clipLengthLimit) {
					songName = songName.substring(0, clipLengthLimit);
				}
				songNameStr += "songname_" + language + ":" + songName + "|";
			}
		}
		songNameStr = songNameStr.replaceAll("'", "");
		songNameStr = songNameStr.replaceAll("&", "%26");
		return songNameStr;
	}

	private String getCategoryName(Category category) {
		String catNameStr = "songname:" + category.getCategoryName() + "|";

		for (String language : supportedLangList) {
			if (category.getCategoryName(language) != null)
				catNameStr += "songname_" + language + ":" + category.getCategoryName(language) + "|";
		}
		catNameStr = catNameStr.replaceAll("'", "");
		catNameStr = catNameStr.replaceAll("&", "%26");
		return catNameStr;
	}

	private String getCategoryInfo(Category category) {
		String categoryInfo = category.getCategoryInfo();
		if (categoryInfo != null) {
			categoryInfo = categoryInfo.replaceAll("=", ":");
			categoryInfo = categoryInfo.replaceAll("&", " ");
		}
		return categoryInfo;
	}
}
