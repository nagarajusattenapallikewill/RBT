package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.Gatherer.MigrateUserExecutor;
import com.onmobile.apps.ringbacktones.common.RBTContestUtils;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.SDRUtility;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;
import com.onmobile.apps.ringbacktones.content.database.SubscriberDownloadsImpl;
import com.onmobile.apps.ringbacktones.daemons.grbt.GrbtLogger;
import com.onmobile.apps.ringbacktones.daemons.reminder.ReminderTool;
import com.onmobile.apps.ringbacktones.features.airtel.UserSelectionRestrictionBasedOnSubClass;
import com.onmobile.apps.ringbacktones.freemium.FreemiumMemcacheClient;
import com.onmobile.apps.ringbacktones.genericcache.BulkPromoSMSCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.CosDetailsCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SubscriptionClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.logger.ContestInfluencerWhilelistLogger;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.promotions.contest.ContestUtils;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.command.FeatureListRestrictionCommandList;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTDaemonService;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTOperatorUserDetailsMappingBean;
import com.onmobile.apps.ringbacktones.rbt2.service.ServiceMappingBean;
import com.onmobile.apps.ringbacktones.rbt2.service.impl.OperatorUserDetailsCallbackServiceImpl;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbt2.thread.ProcessingClipTransfer;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSubscriberDAO;
import com.onmobile.apps.ringbacktones.srbt.db.BeanFactory;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.service.ClipUtilService;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CallbackRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.features.MpNonMpFeature;
import com.onmobile.apps.ringbacktones.webservice.features.MpNonMpFeatureBean;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.common.exception.OnMobileException;

public class RBTDaemonHelper implements iRBTConstant {
	private static Logger logger = Logger.getLogger(RBTDaemonHelper.class);
	// private static Logger upgradeTransLogger =
	// Logger.getLogger(RBTDaemonHelper.class.getName() +
	// ".upgradeTransLogger");

	String _class = "RBTDaemonHelper";
	
	//RBT-12494
	private boolean isUseProxy = false;
	private String proxyHostname = null;
	private int proxyPort = 80;
	private int connectionTimeout = 6000;
	
	private RBTDeamonHelperUtil rbtDeamonHelperUtil;

	String m_statusSuccess = "SUCCESS";
	String m_statusFailure = "FAILURE";
	String m_statusError = "ERROR";
	String m_statusActGrace = "GRACE";
	boolean m_socialRBTAllowed = false;
	boolean m_socialRBTAllUpdateInOneModel = false;
	String rbtSystemType = "RBT";

	String m_strActionActivation = "ACT";
	String m_strActionDeactivation = "DCT";
	String m_strActionRental = "REN";
	String m_strActionEvent = "EVT";
	String m_strUpgradeSubscription = "UPG";
	String m_strSuspendSubscription = "SUS";
	String m_strResumeSubscription = "RES";
	String m_strActionTrigger = "TRG";
	String SUBSCRIPTION = "Subscription";
	String UNSUBSCRIPTION = "Unsubscription";
	String CUSTOMIZATION = "Customization";
	String PURCHASE = "Purchase";
	public static final String OLD_CLASS_TYPE = "OLD_CLASS_TYPE";
	public static final String DELAY_DEACT = "DELAY_DEACT";

	private static String m_success = "SUCCESS";
	private static String m_failure = "FAILURE";

	RBTDBManager m_rbtDBManager = null;
	RBTCacheManager m_rbtCacheManager = null;
	ParametersCacheManager m_rbtParamCacheManager = null;
	ChargeClassCacheManager m_rbtChargeClassCacheManager = null;
	SubscriptionClassCacheManager m_rbtSubClassCacheManager = null;
	CosDetailsCacheManager m_rbtCosCacheManager = null;
	BulkPromoSMSCacheManager m_rbtBulkPromoCacheManager = null;

	public static final char SUSPENDED_INIT = 'z';
	public static final char SUSPENDED = 'Z';
	private ArrayList<String> m_actStatus;
	private ArrayList<String> m_actPendingStatus;
	private ArrayList<Integer> m_packActStatus;
	private ArrayList<Integer> m_packActPendingStatus;
	private ArrayList<String> m_deActStatus;
	private ArrayList<Integer> m_packdeActStatus;

	private List<String> zoominSupportedModesList = null;

	private HashMap<String, String> m_ugcCreditMap = new HashMap<String, String>();
	// RBT-14301: Uninor MNP changes.
	private static Map<String, String> SiteIdTocircleIdMapping = new HashMap<String, String>();
	private static Map<String, String> circleIdToSiteIdMapping = null;
	// CategoryTypes for which start date needs to be updated to
	// CategoryStartTime.
	// private List<String> festivalAndBoxOfficeShuffles = new
	// ArrayList<String>();

	private static WriteDailyTrans m_callBackTrans = null;
	String[] m_validIP = null;
	HttpClient m_httpClient = null;
	private int m_timeOutSec = 1;
	private String m_subClassMoveOptIn = "";
	private HashMap<String, String> contentTypeChargeClassMap = new HashMap<String, String>();
	private List<String> azzanConfModeList = new ArrayList<String>();
	private String contentTypeCosIDMapString;
	private String modeForFreeAzaan;
	private String modeBasedPackActConfig;

	static Object m_init = new Object();
//	SimpleDateFormat sdf = null;
//	SimpleDateFormat m_timeSdf = null;

	String m_sdrWorkingDir = ".";
	String m_smsTextForAll = "ALL";
	String m_countryPrefix = "91";
	String m_overrideShuffleSMS = "%S has been activated for %C , the service would be activate from %L to %T";

	protected static final String sqlTimeSpec = "YYYY/MM/DD HH24:MI:SS";
	protected static final DateFormat sqlTimeFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	private static RBTDaemonHelper rbtDaemonHelper = null;

	public static final String m_INVALID = "INVALID";
	public static final String m_SUCCESS = "SUCCESS";
	public static final String m_FAILURE = "FAILURE";
	public static final String SUBSCRIPTION_DOES_NOT_EXIST = m_INVALID
			+ "|SUBSCRIPTION DOES NOT EXIST";
	public static final String CALLBACK_ALREADY_RECEIVED = m_INVALID
			+ "|CALLBACK ALREADY RECEIVED";

	public static final String SUBSCRIPTION_ALREADY_ACTIVE = m_INVALID
			+ "|SUBSCRIPTION ALREADY ACTIVE";
	public static final String SUBSCRIPTION_ACTIVE = m_INVALID
			+ "|SUBSCRIPTION ACTIVE";
	public static final String SUBSCRIPTION_ACT_PENDING = m_INVALID
			+ "|SUBSCRIPTION PENDING ACTIVATION";

	public static final String SUBSCRIPTION_DEACTIVE = m_INVALID
			+ "|SUBSCRIPTION DEACTIVE";
	public static final String SUBSCRIPTION_ALREADY_DEACTIVE = m_INVALID
			+ "|SUBSCRIPTION ALREADY DEACTIVE";

	public static final String SUBSCRIPTION_ALREADY_ON_GRACE = m_INVALID
			+ "|SUBSCRIPTION ALREADY ON GRACE";
	public static final String SUBSCRIPTION_ACT_GRACE = m_INVALID
			+ "|SUBSCRIPTION ACTIVATION GRACE";

	public static final String UPGRADATION_PENDING = m_INVALID
			+ "|UPGRADATION PENDING";

	public static final String SUBSCRIPTION_ALREADY_SUSPENDED = m_INVALID
			+ "|SUBSCRIBER ALREADY SUSPENDED";
	public static final String SUBSCRIPTION_SUSPENDED = m_INVALID
			+ "|SUBSCRIPTION SUSPENDED";

	public static final String SELECTION_REFID_NOT_EXISTS = m_INVALID
			+ "|SELECTION REFID NOT FOUND";
	public static final String SELECTION_NOT_EXISTS = m_INVALID
			+ "|SELECTION DOES NOT EXIST";
	public static final String SELECTION_ALREADY_DEACTIVE = m_INVALID
			+ "|SELECTION ALREADY DEACTIVE";
	public static final String SELECTION_ALREADY_UPGRADED = m_INVALID
			+ "|SELECTION ALREADY UPGRADED";
	public static final String SELECTION_DEACTIVE = m_INVALID
			+ "|SELECTION DEACTIVE";
	public static final String SELECTION_ALREADY_ACTIVE = m_INVALID
			+ "|SELECTION ALREADY ACTIVE";
	public static final String SELECTION_ACT_PENDING = m_INVALID
			+ "|SELECTION PENDING ACTIVATION";
	public static final String SELECTION_DCT_PENDING = m_INVALID
			+ "|SELECTION PENDING DEACTIVATION";
	public static final String SELECTION_ALREADY_SUSPENDED = m_INVALID
			+ "|SELECTION ALREADY SUSPENDED";
	public static final String SELECTION_ACTIVE = m_INVALID
			+ "|SELECTION ACTIVE";
	public static final String SELECTION_ALREADY_ON_GRACE = m_INVALID
			+ "|SELECTION ALREADY ON GRACE";
	public static final String SELECTION_SUSPENSION = m_INVALID
			+ "|SELECTION SUSPENDED";
	public static final String COULD_NOT_PROCESS = "Could not process. ";

	public short evtType = 0;

	Integer statusCode = new Integer(-1);
	private String hashLikeKeys = null;//RBT-14671 - # like
	private List<String> m_tnbSubClassesList = new ArrayList<String>();
	private Set<String> directActPreRbtSupportedModesSet = null;
	private Set<String> preRbtSupportedModesSet = null;
	private boolean isModesSupportedForPreRbt = true;

	private Map<String, String> subscriptionClassAndCosIdMap = null;

	// Set configured song as setting when there is no selections set
	// for the subscriber.
	private boolean isEnablePrePromptIfNoSel = false;
	private Map<String, String> freeSongForCirclesMap = null;
	private Map<String, String> prePromptWavIfNoSel = null;
	private String modeForFreeSongIfNoSel = null;
	public static String SYSTEM_MODE = "SYSTEM";
	public static HashSet<String> smInitiatedDeactAllowedModes = new HashSet<String>();
	public static HashMap<String, String> reactivateDownloadChargeClassMap = null;
	private List<String> confAzaanCosIdList = new ArrayList<String>();
	private List<String> confAzaanCategoryIdList = new ArrayList<String>();
	private List<String> deregistraionModeList = new ArrayList<String>();
	private static Map<String, List> copyContestIDsTimeValidityMap = new HashMap<String, List>();
	public static HashSet<String> supportedModesForFG1Act = new HashSet<String>();
	
	private boolean toUpdateSelectionGraceInCombo = true;

	private static Map<String, String> subMgrUrlStaticParamsMap = new HashMap<String, String>();
	private static String chargePerCallCosType = null;

	private boolean isContestOnCopyInfluencerAllowed = false;
	private List<String> copyContestModesList = new ArrayList<String>();

	private MemCachedClient memCachedClient = null;
	
	private List<String> onlineCallbackUrlModesList = new ArrayList<String>();
	
	private List<String> migratedUserSubClassesList = new ArrayList<String>();
	
	private Map<String, String> migratedUserOldToNewSubClassMap = new HashMap<String, String>();
	private List<String> subClassesForBaseDeactOnMpFailureList = new ArrayList<String>();
	private List<String> freemiumSubClassList = null;
	private List<String> normalSubClassListForFreemiumUpgrd = null;
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.;
	private static Map<String,String> confAzaanCopticDoaaCosIdSubTypeMap = null;
	public static List<String> cosTypesForMultiPack = null;
	private ExecutorService executor = null;
	private int threadPoolCount = 5;
	
	public static RBTDaemonHelper init() {
		if (rbtDaemonHelper == null) {
			synchronized (m_init) {
				if (rbtDaemonHelper == null) {
					try {
						rbtDaemonHelper = new RBTDaemonHelper();
					} catch (Throwable e) {
						rbtDaemonHelper = null;
						e.printStackTrace();
						logger.error("Exception", e);

					}
				}
			}
		}
		return rbtDaemonHelper;
	}

	private RBTDaemonHelper() throws Exception {
		logger.info("RBT::Initializing Params");
//		sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//		m_timeSdf = new SimpleDateFormat("ddMMMyy");
		
		m_rbtCacheManager = RBTCacheManager.getInstance();
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		m_rbtChargeClassCacheManager = CacheManagerUtil
				.getChargeClassCacheManager();
		m_rbtSubClassCacheManager = CacheManagerUtil
				.getSubscriptionClassCacheManager();
		m_rbtCosCacheManager = CacheManagerUtil.getCosDetailsCacheManager();
		m_rbtBulkPromoCacheManager = CacheManagerUtil
				.getBulkPromoSMSCacheManager();
		String supportedContentTypes = getParamAsString("DAEMON",
				"LITE_UPGRADE_CHARGE_CLASS_MAPPING");
		if (supportedContentTypes != null) {

			List<String> contentChargeClassList = Arrays
					.asList(supportedContentTypes.split(","));
			for (int j = 0; j < contentChargeClassList.size(); j++) {
				String tempContentTypeChargeclass[] = contentChargeClassList
						.get(j).split(":");
				contentTypeChargeClassMap.put(tempContentTypeChargeclass[0],
						tempContentTypeChargeclass[1]);
			}
			logger.debug("The supported content type charge class map is "
					+ contentTypeChargeClassMap);
		}
		
		modeBasedPackActConfig = getParamAsString("DAEMON", "MODE_BASED_PACK_ACTIVATION","");
		azzanConfModeList = Arrays.asList(modeBasedPackActConfig.toUpperCase().split(","));
		 
		contentTypeCosIDMapString = getParamAsString("DAEMON","CONTENT_TYPE_PACK_COS_ID_MAPPING", "");
		modeForFreeAzaan = getParamAsString("DAEMON","MODE_FOR_MODE_BASED_PACK_ACTIVATION", "DAEMON");

		try {
			BeanFactory.getInstance();
		} catch (Throwable e) {
			logger.error("SRBTException", e);
		}

		m_socialRBTAllowed = getParamAsBoolean("COMMON", "SOCIAL_RBT_ALLOWED",
				"false");
		m_socialRBTAllUpdateInOneModel = getParamAsBoolean(SRBT,
				"ALL_IN_ONE_UPDATE_MODEL", "false");

		// boolean isRRBTSystem = getParamAsBoolean("COMMON", "RRBT_SYSTEM",
		// "FALSE");
		// boolean isPRECALLSystem = getParamAsBoolean("COMMON",
		// "PRECALL_SYSTEM", "FALSE");
		if (RBTDeploymentFinder.isRRBTSystem()) {
			rbtSystemType = "RRBT";
		} else if (RBTDeploymentFinder.isPRECALLSystem()) {
			rbtSystemType = "PRECALL";
		}

		m_smsTextForAll = getParamAsString("SMS", "SMS_TEXT_FOR_ALL", "ALL");

		String tmp = null;
		m_statusSuccess = getParamAsString("DAEMON", "STATUS_SUCCESS",
				"SUCCESS");
		m_statusFailure = getParamAsString("DAEMON", "STATUS_FAILURE", "FAILURE");
		m_statusError = getParamAsString("DAEMON", "STATUS_ERROR", "ERROR");
		m_strActionActivation = getParamAsString("DAEMON", "ACTION_ACTIVATION",
				"ACT");
		m_strActionRental = getParamAsString("DAEMON", "ACTION_RENTAL", "REN");
		m_strActionEvent = getParamAsString("DAEMON", "ACTION_EVENT", "EVT");
		m_sdrWorkingDir = getParamAsString("DAEMON", "SDR_WORKING_DIR", ".");

		// CategoryTypes for which start date needs to be updated to
		// CategoryStartTime.
		// festivalAndBoxOfficeShuffles =
		// Arrays.asList(getParamAsString("COMMON",
		// "OVERRIDE_SHUFFLE_CATEGORY_TYPES","10").trim().split(","));

		SDRUtility.initSocialRBTTransactionFile();
		tmp = getParamAsString("SUB_ONLY_CHRG_CLASS");
		if (tmp != null && tmp.length() > 0) {
			m_subClassMoveOptIn = "";
			StringTokenizer stk = new StringTokenizer(tmp, ";");
			while (stk.hasMoreTokens()) {
				String tmp1 = stk.nextToken();
				StringTokenizer stk1 = new StringTokenizer(tmp1, ",");
				m_subClassMoveOptIn = "," + stk1.nextToken();
			}

			m_subClassMoveOptIn = m_subClassMoveOptIn.substring(1);
		}

		ArrayList<String> headers = new ArrayList<String>();
		headers.add("TYPE");
		headers.add("REQUEST");
		headers.add("RESPONSE");
		headers.add("TIME DELAY");

		m_callBackTrans = new WriteDailyTrans(m_sdrWorkingDir, "SM_CALLBACK",
				headers);
		m_rbtDBManager = RBTDBManager.getInstance();

		m_actStatus = new ArrayList<String>();
		m_actPendingStatus = new ArrayList<String>();
		m_packActStatus = new ArrayList<Integer>();
		m_packActPendingStatus = new ArrayList<Integer>();
		m_packdeActStatus = new ArrayList<Integer>();

		m_actStatus.add("W"); // Selection waiting act callback
		m_actStatus.add("A"); // Activation request to be sent
		m_actStatus.add("N"); // Awaiting activation callback
		m_actStatus.add("B"); // Active
		m_actStatus.add("R"); // Renewal request to be sent
		m_actStatus.add("C"); // Change request to be sent
		m_actStatus.add("E"); // Activation Error
		m_actStatus.add("G"); // Activation Grace

		m_actPendingStatus.add("W");	// Selection waiting act callback
		m_actPendingStatus.add("A");	// Activation request to be sent
		m_actPendingStatus.add("N");	// Awaiting activation callback
		m_actPendingStatus.add("E");	// Activation Error
		
		m_packActStatus.add(PACK_BASE_ACTIVATION_PENDING);
		m_packActStatus.add(PACK_TO_BE_ACTIVATED);
		m_packActStatus.add(PACK_ACTIVATION_PENDING);
		m_packActStatus.add(PACK_ACTIVATED);
		m_packActStatus.add(PACK_ACTIVATION_ERROR);
		m_packActStatus.add(PACK_GRACE);
		
		m_packActPendingStatus.add(PACK_BASE_ACTIVATION_PENDING);
		m_packActPendingStatus.add(PACK_TO_BE_ACTIVATED);
		m_packActPendingStatus.add(PACK_ACTIVATION_PENDING);
		m_packActPendingStatus.add(PACK_ACTIVATION_ERROR);

		m_deActStatus = new ArrayList<String>();

		m_deActStatus.add("D"); // Deactivation request to be sent
		m_deActStatus.add("P"); // Awaiting deactivation callback
		m_deActStatus.add("X"); // Deactive
		m_deActStatus.add("F"); // Deactivation Error

		m_packdeActStatus.add(PACK_TO_BE_DEACTIVATED);
		m_packdeActStatus.add(PACK_DEACTIVATION_PENDING);
		m_packdeActStatus.add(PACK_DEACTIVATED);
		m_packdeActStatus.add(PACK_DEACTIVATION_ERROR);

		getUGCCreditTable();

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setStaleCheckingEnabled(true);
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(40);
		connectionManager.getParams().setMaxTotalConnections(40);
		m_httpClient = new HttpClient(connectionManager);
		logger.info(" Initialized HttpClient: " + m_httpClient);

		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				0, false);
		m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		m_httpClient.getParams().setSoTimeout(m_timeOutSec * 1000);
		m_httpClient.setTimeout(1 * 1000);
		ReminderTool.init();
		logger.info(" Initialized ReminderTool successfully. ");

		String tnbSubscriptionClasses = getParamAsString("COMMON",
				"TNB_SUBSCRIPTION_CLASSES", "ZERO");
		m_tnbSubClassesList = Arrays.asList(tnbSubscriptionClasses
				.toUpperCase().split(","));

		String directActPreRbtSupportedModes = RBTParametersUtils
				.getParamAsString(COMMON,
						"PRE_RBT_SUPPORTED_MODES_FOR_DIRECT_ACT", null);
		if (directActPreRbtSupportedModes != null) {
			String[] supportedModes = directActPreRbtSupportedModes.split(",");
			directActPreRbtSupportedModesSet = new HashSet<String>(
					Arrays.asList(supportedModes));
		}

		String preRbtSupportedModes = RBTParametersUtils.getParamAsString(
				COMMON, "SUPPORTED_MODES_FOR_PRE_RBT", null);
		if (preRbtSupportedModes != null) {
			isModesSupportedForPreRbt = true;
			if (preRbtSupportedModes.startsWith("!")) {
				isModesSupportedForPreRbt = false;
				preRbtSupportedModes = preRbtSupportedModes.substring(1);
			}

			String[] supportedModes = preRbtSupportedModes.split(",");
			preRbtSupportedModesSet = new HashSet<String>(
					Arrays.asList(supportedModes));
		}

		subscriptionClassAndCosIdMap = getParamAsMap("DAEMON",
				"SUBCLASS_COS_MAP_FOR_CHANGE_ON_RENEWAL", "");

		isEnablePrePromptIfNoSel = getParamAsBoolean("COMMON",
				"ENABLE_FREE_SONG_IF_NO_SEL", "false");
		// circleId=clipId,chargeClass:circleId=clipId,chageClass:
		freeSongForCirclesMap = getParamAsMap("COMMON",
				"FREE_SEL_CIRCLE_MAP_IF_NO_SEL", "");
		prePromptWavIfNoSel = getParamAsMap("COMMON",
				"FREE_SEL_PRE_PROMPT_WAV_IF_NO_SEL", "");
		modeForFreeSongIfNoSel = getParamAsString("COMMON",
				"FREE_SEL_MODE_IF_NO_SEL", "FREE_SEL");
		String smInittedDeactAllowedModes = getParamAsString(
				iRBTConstant.DAEMON, "SM_INIT_DEACT_ALLOWED_MODES", SYSTEM_MODE);
		smInitiatedDeactAllowedModes.addAll(Arrays
				.asList(smInittedDeactAllowedModes.split(",")));
		smInitiatedDeactAllowedModes.add(SYSTEM_MODE);
		parseReactivateChargeClass();

		// List of Cos Ids which are used for Azaan feature.
		// Azaan is the call for prayer in Islam which happens 5 times a day.
		String cosIds = getParamAsString(DAEMON, AZAAN_COS_ID_LIST);
		confAzaanCosIdList = ListUtils.convertToList(cosIds, ",");

		
		toUpdateSelectionGraceInCombo = getParamAsBoolean(iRBTConstant.DAEMON,
				"TO_UPDATE_SELECTION_GRACE_IN_COMBO", "TRUE");

		// List of Category Ids which are used for Azaan feature.
		// Azaan is the call for prayer in Islam which happens 5 times a day.
		String catIds = getParamAsString(DAEMON, AZAAN_CATEGORY_ID_LIST);
		confAzaanCategoryIdList = ListUtils.convertToList(catIds, ",");
		// RBT-8472 :Du Client - Webservice Changes
		Parameters modeListforDeregistration = CacheManagerUtil
				.getParametersCacheManager().getParameter(
						iRBTConstant.WEBSERVICE, "MODE_FOR_DEREGISTRAION");
		logger.info("modeListforDeregistration: " + modeListforDeregistration);
		if (modeListforDeregistration != null
				&& modeListforDeregistration.getValue() != null) {
			String[] modeArr = modeListforDeregistration.getValue().split(",");
			deregistraionModeList = Arrays.asList(modeArr);
		}

		String url = RBTParametersUtils.getParamAsString("DAEMON",
				"SUB_MGR_URL_FOR_CHARGE_PER_CALL_SERVICE", null);
		if (null != url) {
			subMgrUrlStaticParamsMap = MapUtils.convertToMap(url, "&", "=",
					null);
		}

		chargePerCallCosType = RBTParametersUtils.getParamAsString("DAEMON",
				"CHARGE_PER_CALL_COS_TYPE", "");
		
		isContestOnCopyInfluencerAllowed = getParamAsBoolean(
				"CONTEST_ON_COPY_INFLUENCER_ALLOWED", "FALSE");
		String copyContestModes = getParamAsString("COMMON",
				"MODES_SUPPORTING_FOR_CONTEST_ON_COPY", null);
		if (null != copyContestModes) {
			copyContestModesList = Arrays.asList(copyContestModes.split(","));
		}
		
		String onlineCallbackUrlModesStr = RBTParametersUtils.getParamAsString(
				COMMON, "ONLINE_CALLBACK_URL_MODES", null);
		if (null != onlineCallbackUrlModesStr) {
			onlineCallbackUrlModesList = ListUtils.convertToList(
					onlineCallbackUrlModesStr.toUpperCase(), ",");
			logger.info("Configured ONLINE_CALLBACK_URL_MODES: "
					+ onlineCallbackUrlModesList);
		}
		
		String migratedUserSubClasses = getParamAsString("WEBSERVICE",
				"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);
		if (null != migratedUserSubClasses) {
			migratedUserSubClassesList = ListUtils.convertToList(
					migratedUserSubClasses, ",");
		}

		String migratedUserOldToNewSubClassMapStr = getParamAsString("WEBSERVICE",
				"MIGRATED_USER_OLD_TO_NEW_SUBSCRIPTION_CLASS_MAP", null);
		if (null != migratedUserSubClasses) {
			migratedUserOldToNewSubClassMap = MapUtils.convertToMap(
					migratedUserOldToNewSubClassMapStr, ",","=",null);
		}
		
		

		logger.info("Configured MIGRATED_USER_SUBSCRIPTION_CLASSES as migratedUserSubClassesList: "
				+ migratedUserSubClassesList);

		memCachedClient = RBTCache.getMemCachedClient();

		logger.info("deregistraionModeList: " + deregistraionModeList);
		logger.info("Initialized RBTDaemonHelper successfully. ");
		
		String subClassesForBaseDeactOnMpFailureStr = getParamAsString(DAEMON, SUB_CLASSES_FOR_BASE_DEACT_ON_MP_FAILURE, null);
		logger.info("subClassesForBaseDeactOnMpFailureStr: " + subClassesForBaseDeactOnMpFailureStr);
		if (subClassesForBaseDeactOnMpFailureStr != null) {
			subClassesForBaseDeactOnMpFailureList = ListUtils.convertToList(subClassesForBaseDeactOnMpFailureStr, ",");
			logger.info("subClassesForBaseDeactOnMpFailureList: " + subClassesForBaseDeactOnMpFailureList);
		}
		
		freemiumSubClassList = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
				"FREEMIUM_SUB_CLASSES", "").split(","));
		logger.info("freemiumSubClassList = "+freemiumSubClassList);
		
		normalSubClassListForFreemiumUpgrd = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
				"NORMAL_SUB_CLASSES_FOR_FREEMIUM_UPGRD", "").split(","));
		logger.info("normalSubClassListForFreemiumUpgrd = "+normalSubClassListForFreemiumUpgrd);
		
		//RBT-12494
		isUseProxy = RBTParametersUtils.getParamAsBoolean("DAEMON",
				"CONSENT_EXPIRED_URL_IS_USE_PROXY", "false");
		proxyHostname = RBTParametersUtils.getParamAsString("DAEMON",
				"CONSENT_EXPIRED_URL_PROXY_HOST", null);
		proxyPort = RBTParametersUtils.getParamAsInt("DAEMON", "CONSENT_URL_PROXY_PORT",
				80);
		connectionTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
				"CONSENT_EXPIRED_URL_CONNECTION_TIMEOUT", 6000);
		// RBT-14301: Uninor MNP changes.
		circleIdToSiteIdMapping = MapUtils.convertToMap(
				CacheManagerUtil.getParametersCacheManager().getParameterValue(
						"COMMON", "CIRCLEID_TO_SITEID_MAPPING", null), ";",
				":", null);
		logger.info("circleIdToSiteIdMapping= " + circleIdToSiteIdMapping);
		for (String siteId : circleIdToSiteIdMapping.keySet()) {
			SiteIdTocircleIdMapping.put(circleIdToSiteIdMapping.get(siteId),
					siteId);
		}
		logger.info("SiteIdTocircleIdMapping= " + SiteIdTocircleIdMapping);
		// RBT-14671 - # like
		hashLikeKeys = getParamAsString("COMMON", TOLIKE_KEY, null);
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.		
		String azaanCopticDoaaCosIds = CacheManagerUtil.getParametersCacheManager().getParameterValue(COMMON, COSID_SUBTYPE_MAPPING_FOR_AZAAN, "");
		confAzaanCopticDoaaCosIdSubTypeMap = MapUtils.convertIntoMap(azaanCopticDoaaCosIds, ";",":",","); 
		String cosTypesForEnableMultiPack = RBTParametersUtils
				.getParamAsString("COMMON",
						"COS_TYPE_FOR_ALLOWING_MULTIPLE_PACK", null);
		cosTypesForMultiPack = ListUtils.convertToList(
				cosTypesForEnableMultiPack, ",");
	
		threadPoolCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"D2C_MIGRATION_EXECUTOR_POOL_SIZE", 5);

		try {
			executor = Executors.newFixedThreadPool(threadPoolCount);
			logger.debug("Success Exceutor");
		} catch (Throwable e) {
			logger.error("executor Exception", e);
		}
		try {
			rbtDeamonHelperUtil = (RBTDeamonHelperUtil) ConfigUtil.getBean(BeanConstant.RBT_DEAMEON_HELPER_UTIL);
		} catch (Throwable e) {
			logger.error("Exception : "+e.getMessage(), e);
		}
	}

	public boolean writeTrans(String type, String request, String resp,
			String diff) {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("TYPE", type);
		h.put("REQUEST", request);
		h.put("RESPONSE", resp);
		h.put("TIME DELAY", diff);

		if (m_callBackTrans != null) {
			m_callBackTrans.writeTrans(h);
			return true;
		}
		return false;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processActivation
	 * (com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	private void writeEventLog(String extrainfo, String subscriberID,
			String mode, String errorNo, String action, Clip clip,
			String criteria) {
		if (getParamAsBoolean("COMMON", "ALLOW_ACTIVITY_LOGGING", "FALSE")) {
			logger.info("Writing event logger" + subscriberID + mode + errorNo
					+ action + clip);
			/*
			 * Log Format: subscriberID,action,actionResponse,URL,urResponse|
			 * statusCode|_status,hitTime,responseTime
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSS0");
			if (mode != null
					&& (mode.equalsIgnoreCase("PRESSSTAR") || mode
							.equalsIgnoreCase("COPY")))
				action = "Clonation";
			if (extrainfo != null) {
				if (extrainfo.contains("GIFTER")) {
					action = "GIFT";
				}
			}
			StringBuilder builder = new StringBuilder();
			builder.append(subscriberID).append(
					"|" + sdf.format(new Date()) + "|" + getFinalAction(action)
							+ "|");
			if (clip != null)
				builder.append(clip.getClipId() + "|");
			else
				builder.append("|");
			if (mode != null)
				builder.append(getFinalMode(mode));
			else
				builder.append("");

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
	
	// RBT-14301: Uninor MNP changes.
	public String getMappedCircleIdFromSiteId(String siteId) {
		String mappedCircleId = (SiteIdTocircleIdMapping != null && !SiteIdTocircleIdMapping
				.isEmpty()) ? SiteIdTocircleIdMapping.get(siteId) : null;
		return mappedCircleId;
	}
	
	public String subscription(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String failureInfo,
			String reason, String reasonCode, String currentSubStatus,
			String requestType, String rtKey, String retry, String mode,String insuspension, String circleIDFromPrism, String strNextBillingDate) {
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";
		RBTNode node = RBTMonitorManager.getInstance().startNode(strSubID,
				RBTNode.NODE_SM_CALLBACK_SUB);
		try {
			logger.info("Recieved subscription callback. Status: " + status
					+ ", action: " + action + ", subID: " + strSubID
					+ ", refID: " + refID + ", type: " + type
					+ ", amountCharged: " + amountCharged+", circleIDFromPrism:"+circleIDFromPrism);

			if (status == null || action == null || strSubID == null
					|| type == null) {
				logger.info("Failed to process Callback, missing mandatory"
						+ " parameters.");
				writeEventLog(null, strSubID, null, "101", "Subscription",
						null, null);
				return FAILURE;
			}

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null) {
				writeEventLog(null, strSubID, null, "102", "Subscription",
						null, null);
				return FAILURE;
			}

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(
					conn, strSubID);

			Date nextChargeDate = null;
			Date actDate = null;
			SubscriptionClass subClass = null;
			boolean isPeriodicCharging = true;
			String subscriptionYes = null;
			String oldSubClass = null;
			String extraInfo = null;

			String createTime = null;
			if (subscriber != null) {
				subscriptionYes = subscriber.subYes();
				oldSubClass = subscriber.oldClassType();
				if (subscriber.activationInfo() != null
						&& subscriber.activationInfo().indexOf("BULK:") != -1) {
					createTime = getCreateTime();
				}
				extraInfo = subscriber.extraInfo();
			}

			if (subscriptionYes == null) {
				logger.info("No subscription found for subscriber: " + strSubID);
				writeEventLog(extraInfo, strSubID, null, "101", "Subscription",
						null, null);
				return SUBSCRIPTION_DOES_NOT_EXIST;
			}

			if (reason != null && reason.trim().length() > 0
					&& !reason.trim().equalsIgnoreCase("null")) {
				extraInfo = DBUtility.setXMLAttribute(extraInfo,
						EXTRA_INFO_FAILURE_MESSAGE, reason);
			}
			/*// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleIDFromPrism != null && circleId != null
						&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true, subscriber.subID(),
							circleIDFromPrism, subscriber.refID(), null);
				}
			}*/
			if (classType != null) {
				if (subscriber != null) {
					subClass = m_rbtSubClassCacheManager
							.getSubscriptionClassByLanguage(classType,
									subscriber.language());
				} else {
					subClass = m_rbtSubClassCacheManager
							.getSubscriptionClassByLanguage(classType, null);
				}
				if (subClass != null) {
					classType = subClass.getSubscriptionClass();
					isPeriodicCharging = subClass.getSubscriptionRenewal()
							.equalsIgnoreCase("y");
				} else
					classType = null;
			}

			ChargeSMS chargeSms = null;
			if (subscriber != null) {
				chargeSms = CacheManagerUtil.getChargeSMSCacheManager()
						.getChargeSMS(classType, "SUB", subscriber.language());
			} else {
				chargeSms = CacheManagerUtil.getChargeSMSCacheManager()
						.getChargeSMS(classType, "SUB");
			}

			if (m_strActionDeactivation.equalsIgnoreCase(action)
					&& m_statusSuccess.equalsIgnoreCase(status)) {
				
				//RBT - 15848 Callback handler changes for toneplayer integration
				String subsriptionDeactSuccessResp = processSubscriptionDeactvationSuccess(strSubID, status,
						type, classType, reasonCode, subscriber, extraInfo,
						mode,circleIDFromPrism);
				if(subsriptionDeactSuccessResp != null && subsriptionDeactSuccessResp.equalsIgnoreCase(SUCCESS)) {
					try{
						ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
						if(serviceMappingBean != null) {
							RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
							if(rbtDaemonService != null) {
								rbtDaemonService.deactivateUsersInPlayerDB(subscriber);
							}
						}						
					}
					catch(Exception e){
						logger.error("Bean is not configured: "+e,e);
					}
					
					try{
						RBTOperatorUserDetailsMappingBean mappingBean = (RBTOperatorUserDetailsMappingBean) ConfigUtil.getBean(BeanConstant.RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN);
							
						if(mappingBean != null) {
							OperatorUserDetailsCallbackServiceImpl callbackServiceImpl = (OperatorUserDetailsCallbackServiceImpl) mappingBean.getCallbackService();
							if(callbackServiceImpl != null) {
								callbackServiceImpl.removeOperatorUserInfo(subscriber.subID());
							}
						}
					}catch(Throwable e){
						logger.error("Bean is not configured: "+e,e);
					}
				}
				return subsriptionDeactSuccessResp;
			}

			double renAmt = -1;
			double amtCharged = -1;
			try {
				if (null != subClass) {
					renAmt = Double.parseDouble(subClass.getRenewalAmount());
				}
				if (null != amountCharged) {
					amtCharged = Double.parseDouble(amountCharged);
				}
			} catch (Exception e) {
				logger.error(
						"Invalid Renewal amount. Exception: " + e.getMessage(),
						e);
			}

			String activationInfo = null;
			String activatedBy = null;
			if (subscriber != null) {
				activationInfo = subscriber.activationInfo();
				activatedBy = subscriber.activatedBy();
			}
			if (activationInfo == null)
				activationInfo = "";
			String finalActInfo = activationInfo;
			if (activationInfo.indexOf("|AMT:") > -1
					&& activationInfo.indexOf(":AMT|") > -1) {
				int firstIndex = activationInfo.indexOf("|AMT:");
				int secondIndex = activationInfo.indexOf(":AMT|");
				String firstPart = activationInfo.substring(0, firstIndex);
				String secondPart = activationInfo.substring(secondIndex + 5,
						activationInfo.length());
				finalActInfo = firstPart + secondPart + "|AMT:" + amtCharged
						+ ":AMT|";
			} else
				finalActInfo += "|AMT:" + amtCharged + ":AMT|";

			if (m_statusSuccess.equalsIgnoreCase(status)) {

				/*
				 * Changed by Senthilraja from upgrade retry callback. cleaning
				 * the upgrade failure parameters from extra info, if success
				 * callback comes
				 */
				String retryCosId = null;
				String retryOldSubClass = null;
				String retryInitSubClass = null;
				String retryRbtType = null;
				boolean isProfilePack = false;
				String upgradeFailure_offerId = null;

				if (subscriber != null && extraInfo != null) {
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					// Cleaning extainfo for SM retry callback
					if (extraInfoMap.containsKey(EXTRA_INFO_RETRY_COS_ID)) {
						retryCosId = extraInfoMap
								.remove(EXTRA_INFO_RETRY_COS_ID);
					}
					if (extraInfoMap
							.containsKey(EXTRA_INFO_UPGRADE_FAILURE_OLD_SUB_CLASS)) {
						retryOldSubClass = extraInfoMap
								.remove(EXTRA_INFO_UPGRADE_FAILURE_OLD_SUB_CLASS);
					}
					if (extraInfoMap
							.containsKey(EXTRA_INFO_UPGRADE_FAILURE_SUB_CLASS)) {
						retryInitSubClass = extraInfoMap
								.remove(EXTRA_INFO_UPGRADE_FAILURE_SUB_CLASS);
					}
					if (extraInfoMap
							.containsKey(EXTRA_INFO_UPGRADE_FAILURE_RBTTYPE)) {
						retryRbtType = extraInfoMap
								.remove(EXTRA_INFO_UPGRADE_FAILURE_RBTTYPE);
					}
					if (extraInfoMap
							.containsKey(WebServiceConstants.param_upgradeFailuer_OfferId)) {
						upgradeFailure_offerId = extraInfoMap
								.remove(WebServiceConstants.param_upgradeFailuer_OfferId);
					}
					if (extraInfoMap.containsKey("PACK")) {
						isProfilePack = true;
						logger.info("Subscriber has a profile pack ");
					}
					if (extraInfoMap.containsKey(EXTRA_INFO_TRANS_ID)) {
						extraInfoMap.remove(EXTRA_INFO_TRANS_ID);
						logger.info("TRANS ID has been removed from extr info of subscriber: "
								+ strSubID);
					}
					if (extraInfoMap.containsKey(EXTRA_INFO_TPCGID)) {
						extraInfoMap.remove(EXTRA_INFO_TPCGID);
						logger.info("TPCG ID has been removed from extr info of subscriber: "
								+ strSubID);
					}
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
				}
				// Activation success
				if (m_strActionActivation.equalsIgnoreCase(action)) {
					String subsriptionActSuccessResp = processSubscriptionActivationSuccess(
							strSubID, type, classType, reason, SUCCESS,
							FAILURE, INVALID, subscriber, nextChargeDate,
							actDate, subClass, isPeriodicCharging,
							subscriptionYes, oldSubClass, extraInfo,
							createTime, chargeSms, activatedBy, finalActInfo,
							isProfilePack, action, amountCharged,circleIDFromPrism, strNextBillingDate);
					//Airtel online url
					hitOnlineURL(strSubID, classType, status, mode, amountCharged, null, "ACT", refID);
					
					//RBT - 15848 Callback handler changes for toneplayer integration					
					if(subsriptionActSuccessResp != null && subsriptionActSuccessResp.equalsIgnoreCase(SUCCESS)) {
						try{
							ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
							if(serviceMappingBean != null) {
								RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
								if(rbtDaemonService != null) {
									rbtDaemonService.updateSubscribersInPlayer(subscriber, false);
								}
							}
							
						}
						catch(Exception e){
							logger.error("Bean is not configured: "+e,e);
						}
						
						//change for activate B2B user in DTOC service class
						try{
							RBTOperatorUserDetailsMappingBean mappingBean = (RBTOperatorUserDetailsMappingBean) ConfigUtil.getBean(BeanConstant.RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN);
								
							if(mappingBean != null) {
								OperatorUserDetailsCallbackServiceImpl callbackServiceImpl = (OperatorUserDetailsCallbackServiceImpl) mappingBean.getCallbackService();
								if(callbackServiceImpl != null) {
									callbackServiceImpl.setOperatorUserInfo(subscriber.subID(), OperatorUserTypes.LEGACY.getDefaultValue(), subscriber.subYes(), ConsentPropertyConfigurator.getOperatorFormConfig(), subscriber.circleID());
								}
							}
						}catch(Throwable e){
							logger.error("Bean is not configured: "+e,e);
						}
						if(rbtDeamonHelperUtil!=null){
							rbtDeamonHelperUtil.addTransDataOfAdPartner(strSubID, refID);
						}
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						if (extraInfoMap != null) {
							addTransDataOfAdPartner(extraInfoMap, strSubID);
						}
					}
					return subsriptionActSuccessResp;
					
				} else if (m_strUpgradeSubscription.equalsIgnoreCase(action)) {
					String retVal =  processSubscriptionUpgradeSuccess(strSubID, type,
							classType, reason, retry, subscriber,
							nextChargeDate, actDate, subClass,
							isPeriodicCharging, subscriptionYes, extraInfo,
							createTime, chargeSms, finalActInfo, retryCosId,
							retryOldSubClass, retryInitSubClass, retryRbtType,
							upgradeFailure_offerId,circleIDFromPrism, strNextBillingDate);
					
					if(retVal.equalsIgnoreCase(m_SUCCESS)) {
						//RBT - 15848 Callback handler changes for toneplayer integration
						try{
							ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
							if(serviceMappingBean != null) {
								RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
								if(rbtDaemonService != null) {
									rbtDaemonService.updateSubscribersInPlayer(subscriber, false);
								}
							}
						}
						catch(Exception e){
							logger.error("Bean is not configured: "+e,e);
						}
					}
					
					return retVal;
				}// m_strResumeSubscription
				else if (m_strResumeSubscription.equalsIgnoreCase(action)) {
					boolean updateNcd = getParamAsBoolean("UPDATE_NCD_ON_RES",
							"FALSE");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					Date ncd = null;
					if (updateNcd) {
						if (chargedDate != null) {
							ncd = sdf.parse(chargedDate);
						}
					}
					//RBT- 12494 Call RRBT deactivation
					String response=processResumeSubscription(subscriber, reasonCode,
							currentSubStatus, extraInfo, ncd, circleIDFromPrism,type,classType,finalActInfo);
					if(response!=null && response.equals(SUCCESS)){
						callRRBTDeactivation(subscriber.subID());
						
						//RBT - 15848 Callback handler changes for toneplayer integration
						try{
							ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
							if(serviceMappingBean != null) {
								RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
								if(rbtDaemonService != null) {
									rbtDaemonService.updateSubscribersInPlayer(subscriber, false);
								}
							}
						}
						catch(Exception e){
							logger.error("Bean is not configured: "+e,e);
						}
					}
					return response;
							
				} else {
					
					//RBT- 12494 Call RRBT deactivation
					String response = processRenewalActivationSuccess(strSubID, type,
							classType, reason, rtKey, SUCCESS, FAILURE,
							INVALID, subscriber, nextChargeDate, subClass,
							subscriptionYes, oldSubClass, extraInfo, chargeSms,
							renAmt, amountCharged, activatedBy, finalActInfo,
							isProfilePack, action,circleIDFromPrism, strNextBillingDate);
					if(response!=null && response.equals(SUCCESS)){
						callRRBTDeactivation(subscriber.subID());
						
						//Added for feature restriction
						try{
							FeatureListRestrictionCommandList featureListRestrictionCommandList = (FeatureListRestrictionCommandList) ConfigUtil.getBean(BeanConstant.FEATURE_LIST_RESTRICTION_COMMAND_LIST);
							if(featureListRestrictionCommandList != null) {
								featureListRestrictionCommandList.executeCallbackCommands(subscriber.subID(), classType);
							}
						}
						catch(Exception e){
							//Ignore id bean is not configured
						}
						
					}
					return response;
				}

			}// Failure Callbacks
			else if (m_statusActGrace.equalsIgnoreCase(status)
					&& m_strActionActivation.equalsIgnoreCase(action)) {
				String subscriptionGraceActResp = processSubscriptionGraceActivation(strSubID, type,
						reason, requestType, SUCCESS, FAILURE, INVALID,
						subscriber, subscriptionYes, oldSubClass, extraInfo,
						createTime,circleIDFromPrism);

				//Airtel online url
				hitOnlineURL(strSubID, classType, status, mode, amountCharged, null, "ACT", refID);

				if(subscriptionGraceActResp != null && subscriptionGraceActResp.equalsIgnoreCase(SUCCESS)) {
					try{
						ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
						if(serviceMappingBean != null) {
							RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
							if(rbtDaemonService != null) {
								rbtDaemonService.updateSubscribersInPlayer(subscriber, false);
							}
						}
					}
					catch(Exception e){
						logger.error("Bean is not configured: "+e,e);
					}
				}	
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null) {
					addTransDataOfAdPartner(extraInfoMap, strSubID);
				}
				boolean isAdPartner = updateExtraInfoAdPartner(strSubID , extraInfo);
				logger.info("isAdPartner info removed  from extraInfo :" + isAdPartner) ;
				
				return subscriptionGraceActResp;

			} else if (status.equalsIgnoreCase(m_statusActGrace) && m_strActionRental.equalsIgnoreCase(action)) {
				
				logger.info(":---> Renewal grace callback");
				
				if (rtKey != null)
					return SUCCESS;
				
				//Renewal grace callback
				//<RBT-12942>
				//Handles renewal grace callback. If the parameter, IS_RENEWAL_GRACE_ENABLED, is configured subscriber extraInfo will be updated
				//with RENEWAL_GRACE = true and SUCCESS response will be returned. If not, SUCCESS response is sent back directly.
				
				boolean isRenewalGraceConfigured = RBTParametersUtils.getParamAsBoolean(COMMON,
						IS_RENEWAL_GRACE_ENABLED,
						"FALSE");
				logger.debug("isRenewalGraceConfigured: " + isRenewalGraceConfigured);
				if (isRenewalGraceConfigured) {
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap == null) {
						extraInfoMap = new HashMap<String, String>();									
					}
					extraInfoMap.put(WebServiceConstants.RENEWAL_GRACE,"TRUE");
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					boolean updateStatus = m_rbtDBManager.updateExtraInfo(strSubID, extraInfo);
					logger.debug("SubscriberId: " + strSubID + ", extraInfo: " + extraInfo + ", updateStatus: " + updateStatus);
					return SUCCESS;
				}
				//RBT- 12494 Call RRBT activation
				callRRBTActivation(subscriber.subID(),false);
				//v 
				//RBT-18483 & RBT-18121 
				upgradegrace(subscriber,extraInfo);
				//line 11753
				
				
				return SUCCESS;
			} else {
	/*failure callback?*/if (m_strActionDeactivation.equalsIgnoreCase(action)) {
					return processUnsubscriptionFailure(subscriber,
							oldSubClass, reasonCode, type, circleIDFromPrism);
				}
				// Added to take care of FAILURE status from SM for 'C' records
				// in RBT_SUBSCRIBER table
				else if (m_strUpgradeSubscription.equalsIgnoreCase(action)) {
					String upgradeFailureResponse = processUpgradeFailure(
							subscriptionYes, strSubID, subClass, createTime,
							subscriber, reason, circleIDFromPrism);
					if (upgradeFailureResponse.equalsIgnoreCase(m_SUCCESS)) {
						
						
						//This for D2C RBT2.0 implementation change
						try{
							FeatureListRestrictionCommandList featureListRestrictionCommandList = (FeatureListRestrictionCommandList) ConfigUtil.getBean(BeanConstant.FEATURE_LIST_RESTRICTION_COMMAND_LIST);
							if(featureListRestrictionCommandList != null) {
								featureListRestrictionCommandList.executeCallbackCommands(subscriber.subID(), subscriber.oldClassType());
							}
						}
						catch(Exception e){
							//Ignore id bean is not configured
						}						
						
						processUpgradeTransaction(subscriber);
						if (freemiumSubClassList.contains(subscriber.oldClassType())) {
							updateStateBaseActPendingSelection(strSubID, subscriber.rbtType(),
									FAILURE);
						}
					} else {
						removeAllUpgradeTransactions(subscriber, "UPGRADE FAILURE");
					}
					return upgradeFailureResponse;
				}
				// Removed Status check for suspension
				else if (m_strSuspendSubscription.equalsIgnoreCase(action)) {
					String suspensionResponse = processSuspendSubscription(subscriber, createTime, reason, reasonCode,
							extraInfo, oldSubClass, circleIDFromPrism);
					
					//v
		/*			if (subscriber != null) {
						logger.info(":---> subscriptionClass :" + subscriber.subscriptionClass());
						if (suspensionResponse.equalsIgnoreCase("SUCCESS")) {
							Map<String, String> map = getParamAsMap("DAEMON", "RENEWAL_FAILURE_SUB_CLASS_CHANGE", null);
							Boolean toSuspendSelection = map.size() > 0
									&& map.containsKey(subscriber.subscriptionClass());
							logger.info(":---> toSuspendSelection :" + toSuspendSelection);
						//	RBT-18125 To suspend selection which are not ephemeral or Cool tune
							if (toSuspendSelection) {
								String CategoryId = ConsentPropertyConfigurator.getFreeClipCategoryID();
								logger.info(":---> FreeClipCategoryIdconfigured :" + CategoryId);
								RBTDBManager.getInstance().suspendSubscriberRecordsByNotCategoryIdNotStatus(
										subscriber.subID(), Integer.parseInt(CategoryId), 200);
							}
						}
					}
					
			*/		
			
						

					if (suspensionResponse.equalsIgnoreCase("SUCCESS") && RBTParametersUtils.getParamAsBoolean(COMMON,
							"ALLOW_UPGRADE_FOR_SUSPENDED_USERS", "FALSE")) {
						processUpgradeTransaction(subscriber);

					}

					// RBT- 12494 Call RRBT activation
					if (insuspension != null && insuspension.equalsIgnoreCase("true")) {
						callRRBTActivation(subscriber.subID(), true);

					}

					// Airtel online url
					hitOnlineURL(strSubID, classType, status, mode, amountCharged, null, "ACT", refID);

					// RBT - 15848 Callback handler changes for toneplayer
					// integration
					if (suspensionResponse != null && suspensionResponse.equalsIgnoreCase(SUCCESS)) {
						try {
							ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil
									.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
							if (serviceMappingBean != null) {
								RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
								if (rbtDaemonService != null) {
									rbtDaemonService.updateSubscribersInPlayer(subscriber, true);
								}
							}

						} catch (Exception e) {
							logger.error("Bean is not configured: " + e, e);
						}
					}
					return suspensionResponse;
				} else {
					if (m_strActionActivation.equalsIgnoreCase(action)) {
						removeAllUpgradeTransactions(subscriber,
								"ACTIVATION FAILURE");
						
						if (!subscriptionYes.equals("N")
								&& !subscriptionYes.equals("A")
								&& !subscriptionYes.equals("G")) {
							if (subscriptionYes.equals("B")) {
								writeEventLog(extraInfo, subscriber.subID(),
										subscriber.activatedBy(), "103",
										"Subscription", null, null);
								return SUBSCRIPTION_ALREADY_ACTIVE;
							} else if (subscriptionYes.equals("C")
									|| (subscriptionYes.equals("E") && oldSubClass != null)) {
								logger.info("Subscriber status is change or "
										+ "activation error. subscriptionYes: "
										+ subscriptionYes + " SubscriberId: "
										+ strSubID);
								writeEventLog(extraInfo, subscriber.subID(),
										subscriber.activatedBy(), "103",
										"Subscription", null, null);
								return SUBSCRIPTION_ACTIVE;
							} else if (subscriptionYes.equals("X"))
								return SUBSCRIPTION_ALREADY_DEACTIVE;
						}

						// if (isChargePerCallCallback(classType)) {
						// processSubscriptionDeactivation(subscriber,
						// classType, reason);
						// }
					} else if (m_strActionRental.equalsIgnoreCase(action)) {
						// Renewal trigger failure callback. RBT doesn't need
						// any action in this case
						if (rtKey != null)
							return SUCCESS;
						
						//Renewal grace callback
						//<RBT-12942>
						//Handles renewal grace callback. If the parameter, IS_RENEWAL_GRACE_ENABLED, is configured subscriber extraInfo will be updated
						//with RENEWAL_GRACE = true and SUCCESS response will be returned. If not, SUCCESS response is sent back directly.
//						if (status.equalsIgnoreCase(m_statusActGrace)) {
//							boolean isRenewalGraceConfigured = RBTParametersUtils.getParamAsBoolean(COMMON,
//									IS_RENEWAL_GRACE_ENABLED,
//									"FALSE");
//							logger.debug("isRenewalGraceConfigured: " + isRenewalGraceConfigured);
//							if (isRenewalGraceConfigured) {
//								HashMap<String, String> extraInfoMap = DBUtility
//										.getAttributeMapFromXML(extraInfo);
//								if (extraInfoMap == null) {
//									extraInfoMap = new HashMap<String, String>();									
//								}
//								extraInfoMap.put(WebServiceConstants.RENEWAL_GRACE,"TRUE");
//								extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
//								boolean updateStatus = m_rbtDBManager.updateExtraInfo(strSubID, extraInfo);
//								//RBT- 12494 Call RRBT activation
//								callRRBTActivation(subscriber.subID(),false);
//								logger.debug("SubscriberId: " + strSubID + ", extraInfo: " + extraInfo + ", updateStatus: " + updateStatus);
//								return SUCCESS;
//							}
//							return SUCCESS;
//						}
						
						if (!subscriptionYes.equals("B")) {
							if (subscriptionYes.equals("A"))
								return SUBSCRIPTION_ACT_PENDING;
							else if (subscriptionYes.equals("D"))
								return CALLBACK_ALREADY_RECEIVED;
							else if (subscriptionYes.equals("X"))
								return SUBSCRIPTION_DEACTIVE;
						}
					}

					String deactBy = "AF";
					boolean bRenewal = false;
					if (mode != null && !mode.equals(SYSTEM_MODE)
							&& m_strActionRental.equalsIgnoreCase(action)
							&& m_statusFailure.equalsIgnoreCase(status)
							&& smInitiatedDeactAllowedModes.contains(mode))
						deactBy = mode;
					else if (m_strActionRental.equalsIgnoreCase(action)) {
						bRenewal = true;
						createTime = getCreateTime();
						if (getParamAsBoolean("NEF_DEACTIVATIONS", "FALSE")
								&& (type != null && type.trim()
										.equalsIgnoreCase("P")))
							deactBy = "NEF";
						else
							deactBy = "RF";

					} else if (m_strActionActivation.equalsIgnoreCase(action)) {
						if (getParamAsBoolean("NEF_DEACTIVATIONS", "FALSE")
								&& (type != null && type.trim()
										.equalsIgnoreCase("P")))
							deactBy = "NA";
					}

					// subscription renewal failure
					Map<String,String> map = getParamAsMap("DAEMON","RENEWAL_FAILURE_SUB_CLASS_CHANGE",null);
					Boolean toActivateSubsciber = map.size() > 0 && map.containsKey(classType);
					Boolean isToNotDeativateDirectly = toActivateSubsciber ? false: !normalSubClassListForFreemiumUpgrd.contains(classType);
				/*	String ret = smSubscriptionRenewalFailure(strSubID,
							deactBy, type, classType, bRenewal, extraInfo,
							false,isToNotDeativateDirectly ,circleIDFromPrism);*/
					logger.info(":---> toActivateSubsciber"+toActivateSubsciber);
					//v
					String ret=m_FAILURE;
					if(toActivateSubsciber)
					{
						//RBT-18125 & RBT-18483 :Renewal failure callback,only base deactivation.Selection to be restriction while base Activation callback(line-2055)
						ret = smSubscriptionRenewalFailureOnlyBaseDeactivation(strSubID,
								deactBy, type, classType, bRenewal, extraInfo,
								false,isToNotDeativateDirectly ,circleIDFromPrism);
					}
					else
					{ ret = smSubscriptionRenewalFailure(strSubID,
							deactBy, type, classType, bRenewal, extraInfo,
							false,isToNotDeativateDirectly ,circleIDFromPrism);
					}
					
					if (ret.equalsIgnoreCase(m_success)) {
						if(toActivateSubsciber) {							
							SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.subID());
							subscriptionRequest.setMode(mode);
							subscriptionRequest.setSubscriptionClass(map.get(subscriber.subscriptionClass()));
							RBTClient.getInstance().activateSubscriber(subscriptionRequest);
								
						}
					}
					boolean isAdPartner = updateExtraInfoAdPartner(strSubID , extraInfo);
					logger.info("isAdPartner info removed  from extraInfo :" + isAdPartner) ;
					// RBT-8472 :Du Client - Webservice Changes
					if (ret.equalsIgnoreCase(m_success)
							&& deregistraionModeList.contains(mode)) {
						m_rbtDBManager.deleteRBTLoginUserBySubscriberID(
								strSubID, null);
					}
					//RBT-14671 - # like
					if (ret.equalsIgnoreCase("success") && hashLikeKeys != null
							&& !hashLikeKeys.isEmpty()) {
						try {
							m_rbtDBManager.deleteSubscriberLikedSong(strSubID,
									-1, -1);
						} catch (Exception e) {
							logger.info("Table is not there in the data base for like: "
									+ e.getMessage());
						}
					}
					
					/*Freemium Model Changes*/
					String freemiumSubClassClipCatId = RBTParametersUtils.getParamAsString(COMMON,
							"FREEMIUM_COS_CLIP_CAT_ID", null);
					String supportedModeForFG1Act = RBTParametersUtils.getParamAsString(DAEMON,
							"MODE_SUPPORT_FG1_ACTIVATION", null);
					if (supportedModeForFG1Act != null) {
						supportedModesForFG1Act.addAll(Arrays
								.asList(supportedModeForFG1Act.toUpperCase().split(",")));
					}
					if (m_strActionRental.equalsIgnoreCase(action)
							&& mode != null
							&& ret.equalsIgnoreCase(m_success)
							&& smInitiatedDeactAllowedModes.contains(mode)
							&& normalSubClassListForFreemiumUpgrd
									.contains(classType)
							&& freemiumSubClassClipCatId != null
							&& (supportedModesForFG1Act.isEmpty() || supportedModesForFG1Act
									.contains(mode.toUpperCase()))) {
						String[] split = freemiumSubClassClipCatId.split(",");
						if (split.length == 2) {
							Integer freemiumReactivateCosId = Integer.parseInt(split[0]);
							SelectionRequest selectionRequest = new SelectionRequest(strSubID);
							selectionRequest.setCosID(freemiumReactivateCosId);
							if (split[1].startsWith("S")) {
								selectionRequest.setCategoryID(split[1].substring(1));
							} else {
								selectionRequest.setClipID(split[1]);
								selectionRequest.setCategoryID("3");
							}
							// RBT-13703-Activation Failure at Prism is trying
							// to activate on FG1 by RBT
							String freemiumSystemModeStr = RBTParametersUtils
									.getParamAsString(
											COMMON,
											"MODE_MAP_FOR_FREEMIUM_SYSTEM_MODE",
											null);
							Map<String, String> systemModeMap = new HashMap<String, String>();
							systemModeMap = MapUtils.convertToMap(
									freemiumSystemModeStr, ",", "=", null);
							if (!systemModeMap.isEmpty()
									&& systemModeMap.containsKey(mode)) {
								mode = systemModeMap.get(mode);
							}
							selectionRequest.setMode(mode);
							RBTClient.getInstance().addSubscriberSelection(selectionRequest);
						}
					}
					// Add code for Announcement feature
					// Announcement status to be activated, if subscriber is
					// active in announcement
					if (ret.equalsIgnoreCase(m_success)
							&& getParamAsBoolean(COMMON, PROCESS_ANNOUNCEMENTS,
									"false")) {
						// call smAnnouncementToBeActivated
						smAnnouncementToBeActivated(subscriber.subID());
					}
					if (ret.equalsIgnoreCase(m_success)
							&& getParamAsBoolean("SRBT",
									"SRBT_ACCOUNT_EXPIRY_FLAG", "false")) {
						evtType = evtTypeForAccountExpiry;
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(subscriber.extraInfo());
						RbtSocialSubscriberDAO.deleteSocialSubscriber(Long
								.parseLong(strSubID));
						SRBTUtility.updateSocialSubscriberForSuccess(
								m_socialRBTAllowed,
								m_socialRBTAllUpdateInOneModel, extraInfoMap,
								strSubID, type, classType,
								subscriber.deactivatedBy(), evtType);
						// SRBTUtility.deactivateSocialSiteUser(strSubID);
						logger.info("Subscriber :: "
								+ strSubID
								+ " is deactivated successfully from Social RBT.");
						writeEventLog(extraInfo, subscriber.subID(),
								subscriber.deactivatedBy(), "0",
								"Subscription", null, null);
						evtType = 0;
					}

					if (ret.equalsIgnoreCase(m_success) && failureInfo != null
							&& !failureInfo.equalsIgnoreCase("null")
							&& reason == null)
						m_rbtDBManager.updateExtraInfo(strSubID,
								EXTRA_INFO_FAILURE_MESSAGE, failureInfo);
					if (ret.equalsIgnoreCase(m_success) && reason != null
							&& !reason.equalsIgnoreCase("null"))
						m_rbtDBManager.updateExtraInfo(strSubID,
								EXTRA_INFO_FAILURE_MESSAGE, reason);
					m_rbtDBManager.removeViralSMSOfCaller(strSubID, "GIFTED");
					
					
					if (ret.equalsIgnoreCase(m_success)) {
						
						if (m_strActionActivation.equalsIgnoreCase(action)) {
							// IBM-Integration
							if (getParamAsBoolean("SUPPORT_IBM_INTEGRATION", "FALSE")) {
								RBTCallBackEvent.update(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
										strSubID, subscriber.refID(),
										RBTCallBackEvent.SM_FAILURE_CALLBACK_RECEIVED, classType);
							}
						}

						m_rbtDBManager
								.updateConsentStatusOfConsentRecordBySubscriberId(
										strSubID, "4");

						
						if (deactBy.equalsIgnoreCase("NA")) {
							if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
									&& subClass != null) {
								if (chargeSms == null) {
									if (subClass.getSmsOnSubscriptionFailure() != null
											&& !subClass
													.getSmsOnSubscriptionFailure()
													.equalsIgnoreCase("null"))
										sendSMS(strSubID,
												getReasonSMS(
														subClass.getSmsOnSubscriptionFailure(),
														reason), createTime);
								} else {
									if (type != null
											&& type.equalsIgnoreCase("p"))
										sendSMS(strSubID,
												getReasonSMS(chargeSms
														.getPrepaidFailure(),
														reason), createTime);
									else
										sendSMS(strSubID,
												getReasonSMS(chargeSms
														.getPostpaidFailure(),
														reason), createTime);
								}
							}
						} else if (deactBy.equalsIgnoreCase("NEF")) {
							if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
									&& subClass != null) {
								if (chargeSms == null) {
									if (subClass.getSmsRenewalFailure() != null
											&& !subClass.getSmsRenewalFailure()
													.equalsIgnoreCase("null"))
										sendSMS(strSubID,
												getReasonSMS(
														subClass.getSmsRenewalFailure(),
														reason), createTime);
								} else {
									if (type != null
											&& type.equalsIgnoreCase("p"))
										sendSMS(strSubID,
												getReasonSMS(
														chargeSms
																.getPrepaidRenewalFailure(),
														reason), createTime);
									else
										sendSMS(strSubID,
												getReasonSMS(
														chargeSms
																.getPostpaidRenewalFailure(),
														reason), createTime);
								}
							}
						}
						if (isTnbReminderEnabled(subscriber)) {
							deleteTNBSubscriber(subscriber.subID());
						}
						if (isTrialReminderEnabled(subscriber)) {
							deleteTrialSelection(subscriber.subID());
						}
						
						//Airtel online url call
						hitOnlineURL(strSubID, classType, status, mode, amountCharged, null, "ACT", refID);
						writeEventLog(extraInfo, subscriber.subID(),
								subscriber.activatedBy(), "0", "Subscription",
								null, null);
						try{
							RBTOperatorUserDetailsMappingBean mappingBean = (RBTOperatorUserDetailsMappingBean) ConfigUtil.getBean(BeanConstant.RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN);
								
							if(mappingBean != null) {
								OperatorUserDetailsCallbackServiceImpl callbackServiceImpl = (OperatorUserDetailsCallbackServiceImpl) mappingBean.getCallbackService();
								if(callbackServiceImpl != null) {
									callbackServiceImpl.removeOperatorUserInfo(subscriber.subID());
								}
							}
						}catch(Throwable e){
							logger.error("Bean is not configured: "+e,e);
						}
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						writeEventLog(extraInfo, subscriber.subID(),
								subscriber.activatedBy(), "101",
								"Subscription", null, null);
						return INVALID;
					} else {
						writeEventLog(extraInfo, subscriber.subID(),
								subscriber.activatedBy(), "101",
								"Subscription", null, null);
						return FAILURE;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception when processing subscription callback. "
					+ "Exception: " + e.getMessage(), e);
			return FAILURE;
		} finally {
			RBTMonitorManager.getInstance().endNode(strSubID, node, status);
		}

	}

	private void upgradegrace(Subscriber subscriber,String extraInfo ) {
		// TODO Auto-generated method stub 
		//v
		//RBT-18483 & RBT-18121
		String dtocSuspendedFreeUserClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_APP_SUSPENDED_FREE_USER_SERVICE_CLASS", null);
		logger.info(":---> dtocSuspendedFreeUserClass : "+dtocSuspendedFreeUserClass);
		String subscriptionClass = subscriber.subscriptionClass();
		String operatorUserType = null;
		IUserDetailsService operatorUserDetailsService = null;
		OperatorUserDetails operatorUserDetails = null;

		try {
			// Getting user details from B2B db cache
			operatorUserDetailsService = (IUserDetailsService) ConfigUtil.getBean(BeanConstant.USER_DETAIL_BEAN);


			if (operatorUserDetailsService != null) {
				operatorUserDetails = (OperatorUserDetailsImpl) operatorUserDetailsService
						.getUserDetails(subscriber.subID());
			}

			if (operatorUserDetails != null) {
				operatorUserType = operatorUserDetails.serviceKey();
			}
		} catch (Exception e) {
			operatorUserType = com.onmobile.apps.ringbacktones.v2.common.Constants.NON_LEGACY_OPT;
			logger.error("Exception getting operatorUserType so setting operator type as :: " + operatorUserType);
		}
		
		logger.info(":---> operatorUserType"+operatorUserType);

		boolean allowUpgradation = false;
		if (dtocSuspendedFreeUserClass != null && operatorUserType != null
				&& operatorUserType.equalsIgnoreCase(OperatorUserTypes.PAID_APP_USER.getDefaultValue())) {
					allowUpgradation = true;
		}
		logger.info(":---> allowupgradation : "+allowUpgradation);
		String ret = m_failure;
		if (allowUpgradation) {
			Map<String, String> attributeMap = new HashMap<String, String>();
			extraInfo = DBUtility.setXMLAttribute(extraInfo, com.onmobile.apps.ringbacktones.v2.common.Constants.PAID_UNDER_LOWBAL, "TRUE");
			attributeMap.put("SUBSCRIPTION_CLASS", dtocSuspendedFreeUserClass);
			attributeMap.put("EXTRA_INFO", extraInfo);
			attributeMap.put("PLAYER_STATUS", "A");
			ret = m_rbtDBManager.updateSubscriber(subscriber.subID(), attributeMap);
			
		}
		logger.info(":--> RET :"+ret);
	}

	private String processSubscriptionUpgradeSuccess(String strSubID,
			String type, String classType, String reason, String retry,
			Subscriber subscriber, Date nextChargeDate, Date actDate,
			SubscriptionClass subClass, boolean isPeriodicCharging,
			String subscriptionYes, String extraInfo, String createTime,
			ChargeSMS chargeSms, String finalActInfo, String retryCosId,
			String retryOldSubClass, String retryInitSubClass,
			String retryRbtType, String upgradeFailure_offerId, String circleIdParam, String strNextBillingDate) {
		logger.debug("Processing Upgradation callback. SubscriberId: "
				+ strSubID);
		String act = null;
		String oldSub = null;
		if (subscriber != null) {
			act = subscriber.activatedBy();
			oldSub = subscriber.oldClassType();
			logger.debug("Setting old sub class as " + oldSub);
		}

		String upgradingCosID = null;
		// If upgrade is SUCCESS , remove REFUND=TRUE from extraInfo
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);

		int rbtType = -1;

		if (extraInfoMap != null) {
			extraInfoMap.remove(EXTRA_INFO_OLD_ACT_BY);
			extraInfoMap.remove(REFUND);
			extraInfoMap.remove("IS_SUSPENDED");
			upgradingCosID = extraInfoMap.remove(EXTRA_INFO_COS_ID);
			if (extraInfoMap.containsKey("BI_OFFER")) {
				String biBlackListUrl = getParamAsString(COMMON,
						"BLACKLIST_URL_FOR_BI_OFFER", null);
				if (biBlackListUrl != null) {
					biBlackListUrl = biBlackListUrl.replaceAll("%MSISDN%",
							strSubID);
					biBlackListUrl = biBlackListUrl.replaceAll("%SRVKEY%",
							subscriber.subscriptionClass());
					makeHttpRequest(biBlackListUrl);
				}
				extraInfoMap.remove("BI_OFFER");
			}
			/*
			 * Changed by Senthilraja from upgrade retry callback.
			 */
			if ("TRUE".equalsIgnoreCase(retry)) {
				if (retryCosId != null) {
					upgradingCosID = retryCosId;
				}
				if (oldSub == null && retryOldSubClass != null) {
					oldSub = retryOldSubClass;
				}
				if (classType == null && retryInitSubClass != null) {
					classType = retryInitSubClass;
				}
				if (retryRbtType != null) {
					rbtType = Integer.parseInt(retryRbtType);
				}
				if (upgradeFailure_offerId != null) {
					extraInfoMap
							.put(WebServiceConstants.param_old_offerid,
									extraInfoMap
											.get(WebServiceConstants.param_offerID));
					extraInfoMap.put(WebServiceConstants.param_offerID,
							upgradeFailure_offerId);
				}
			}
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		}

		String retVal = processUpgradeSuccess(subscriptionYes, strSubID,
				nextChargeDate, actDate, type, classType, isPeriodicCharging,
				subClass, chargeSms, createTime, act, oldSub, finalActInfo,
				reason, extraInfo, upgradingCosID, retry, rbtType, strNextBillingDate);
		/*
		 * if(subscriber.subYes().equals(STATE_SUSPENDED) ||
		 * subscriber.subYes().equals(STATE_SUSPENDED_INIT)) {
		 * smUnsuspendSelections(strSubID);
		 * smUpdateSelStatusSubscriptionSuccess(strSubID); }
		 */
		// RBT-14301: Uninor MNP changes.
		if (retVal.equalsIgnoreCase(m_SUCCESS) && subscriber != null) {
			String circleId = subscriber.circleID();
			if (circleId != null && circleIdParam != null && !circleId.equalsIgnoreCase(circleIdParam)) {
				smUpdateCircleIdForSubscriber(true, subscriber.subID(), circleIdParam, subscriber.refID(), null);
			}

			// This for D2C RBT2.0 implementation change
			try {
				FeatureListRestrictionCommandList featureListRestrictionCommandList = (FeatureListRestrictionCommandList) ConfigUtil
						.getBean(BeanConstant.FEATURE_LIST_RESTRICTION_COMMAND_LIST);
				if (featureListRestrictionCommandList != null) {
					featureListRestrictionCommandList.executeCallbackCommands(subscriber.subID(), classType);
				}
			} catch (Exception e) {
				// Ignore id bean is not configured
				logger.error(e);
			}
			String b2bServiceClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
					"D2C_MIGRATION_SERVICE_KEYS", "");
			boolean isB2BSrvKey = false;

			if (b2bServiceClass != null && !b2bServiceClass.isEmpty()) {
				for (String srvKey : b2bServiceClass.split(",")) {
					if (srvKey.equalsIgnoreCase(subscriber.subscriptionClass()))
						isB2BSrvKey = true;
				}
			}

			if (isB2BSrvKey) {

				try {

					Runnable migrateExecutor = new MigrateUserExecutor(subscriber.subID());
					executor.execute(migrateExecutor);

				} catch (Exception e) {
					logger.info("Exception Occured in MigrateUserExecutor" + e.getMessage());
				}
			}

		}
		/*FOR FREEMIUM MODEL CHANGES*/
		if (freemiumSubClassList.contains(oldSub)) {
			updateStateBaseActPendingSelection(strSubID, rbtType, retVal);
		}

		// checks if any upgrade requests are pending
		processUpgradeTransaction(subscriber);

		logger.info("returns: " + retVal);
		return retVal;
	}
	
	private boolean isCorpSubscriber(Subscriber subscriber) {
		SubscriberStatus[] settings = RBTDBManager.getInstance()
				.getAllActiveSubscriberSettings(subscriber.subID());
		
		if(settings == null) return false;
		for (SubscriberStatus subscriberStatus : settings) {
			if (subscriberStatus.selType() == 2) {
				return true;
			}
		}

		logger.info("Returning false, as  the subscriber is not corporate subscriber");
		return false;
	}

	private String processSubscriptionActivationSuccess(String strSubID,
			String type, String classType, String reason, String SUCCESS,
			String FAILURE, String INVALID, Subscriber subscriber,
			Date nextChargeDate, Date actDate, SubscriptionClass subClass,
			boolean isPeriodicCharging, String subscriptionYes,
			String oldSubClass, String extraInfo, String createTime,
			ChargeSMS chargeSms, String activatedBy, String finalActInfo,
			boolean isProfilePack, String action, String amountCharged, String circleIDFromPrism, String strNextBillingDate) throws OnMobileException {
		logger.info("Processing subscription activation success."
				+ " SubscriberId: " + strSubID + ", activatedBy: "
				+ activatedBy + ", amountCharged: " + amountCharged);
		if (!subscriptionYes.equals("N") && !subscriptionYes.equals("A")
				&& !subscriptionYes.equals("G")) {
			if (subscriptionYes.equals("B")) {
				if (isProfilePack) {
					List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
							.getBySubscriberIDAndStatus(strSubID);
					ProvisioningRequests provisioningRequest = null;
					if (provisioningRequestsList != null
							&& provisioningRequestsList.size() > 0) {
						provisioningRequest = provisioningRequestsList.get(0);
						int cosID = provisioningRequest.getType();
						boolean isProfile = false;
						String cosType = null;
						CosDetails cosdetail = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										cosID + "");
						if (cosdetail != null)
							cosType = cosdetail.getCosType();

						if (cosdetail == null
								|| cosType == null
								|| (!cosType.equalsIgnoreCase(SONG_PACK)
										&& !cosType
												.equalsIgnoreCase(LIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(AZAAN)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS_OVERWRITE)
										&& !cosType
												.equalsIgnoreCase(LIMITED_SONG_PACK_OVERLIMIT)
										&& !cosType
												.equalsIgnoreCase(PROFILE_COS_TYPE) && !cosType
											.equalsIgnoreCase(COS_TYPE_AUTO_DOWNLOAD))) {
							return FAILURE;
						}

						if (cosdetail.getCosType().equalsIgnoreCase(
								PROFILE_COS_TYPE)) {
							isProfile = true;
						}
						if (provisioningRequest != null) {
							if (provisioningRequest.getStatus() == PACK_ACTIVATED) {
								if (!smUpdateProfileSelStatusOnSubscriptionSuccess(
										strSubID, isProfile)) {
									return FAILURE;
								}
							} else { // Base act pending state pack
										// state needs to be made to be
										// active
								if (!ProvisioningRequestsDao.updateRequest(
										strSubID,
										provisioningRequest.getTransId(),
										PACK_TO_BE_ACTIVATED + "", false, -1))
									return FAILURE;
							}
						}
					}
				}
				if (!smUpdateSelStatusSubscriptionSuccess(strSubID,
						isProfilePack, false, null)) {// RBT-14301: Uninor MNP changes.
					writeEventLog(extraInfo, subscriber.subID(),
							subscriber.activatedBy(), "101", "Subscription",
							null, null);
					return FAILURE;
				}
				logger.info("Subscriber is already Active. SubscriberId: "
						+ subscriber.subID());
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.activatedBy(), "103", "Subscription", null,
						null);
				return SUBSCRIPTION_ALREADY_ACTIVE;
			} else if (subscriptionYes.equalsIgnoreCase("X")) {
				logger.info("Subscriber is Deactive. SubscriberId: "
						+ subscriber.subID());
				return SUBSCRIPTION_DEACTIVE;
			} else if (subscriptionYes.equalsIgnoreCase("D")
					|| subscriptionYes.equalsIgnoreCase("P")
					|| subscriptionYes.equalsIgnoreCase("F")
					|| subscriptionYes.equalsIgnoreCase("C")
					|| (subscriptionYes.equalsIgnoreCase("E") && oldSubClass != null)) {
				logger.info("Callback already received. SubscriberId: "
						+ subscriber.subID());
				return CALLBACK_ALREADY_RECEIVED;
			}
		}
		try {
			if (m_rbtDBManager.m_isLTPOnForBaseAct
					&& m_rbtDBManager.m_ltpActMap != null
					&& activatedBy != null
					&& m_rbtDBManager.m_ltpActMap.containsKey(activatedBy
							.toUpperCase())) {
				int ltpPoints = ((Integer) m_rbtDBManager.m_ltpActMap
						.get(activatedBy.toUpperCase())).intValue();
				if (ltpPoints > 0)
					finalActInfo = m_rbtDBManager.addLTPPoints(finalActInfo,
							ltpPoints);
			}
		} catch (Exception e) {
			logger.error("Exception. message: " + e.getMessage(), e);
		}
		boolean jingleSubscriber = false;
		List<String> jingleSubscriptionClassList = getJingleSubClassList();
		if (jingleSubscriptionClassList != null
				&& jingleSubscriptionClassList.size() > 0 && classType != null
				&& jingleSubscriptionClassList.contains(classType)) {
			jingleSubscriber = true;
			extraInfo = DBUtility.setXMLAttribute(extraInfo,
					EXTRA_INFO_JINGLE_FLAG, "true");
		}

		// Added extraInfo
		boolean updatePlayerStatus = jingleSubscriber
				|| getParamAsBoolean("NOT_PLAY_SONG_INACT_USER", "FALSE");
		if (subscriber != null
				&& subscriber.rbtType() == 1
				& subscriber.subscriptionClass() != null
				&& subscriber.subscriptionClass().equalsIgnoreCase(
						getParamAsString(COMMON, "ADRBT_SUB_CLASS", "ADRBT")))
			updatePlayerStatus = true;

		String finalSubscriptionYes = null;
		extraInfo = DBUtility.removeXMLAttribute(extraInfo,
				iRBTConstant.EXTRA_INFO_OFFER_ID);
		String ret = smSubscriptionSuccess(strSubID, nextChargeDate, actDate,
				type, classType, isPeriodicCharging, finalActInfo,
				updatePlayerStatus, extraInfo, finalSubscriptionYes, strNextBillingDate);
		logger.info("Subscription activation status: " + ret);

		/**
		 * @author Sreekar For BSNL ADRBT, we accept ADRBT activation even if
		 *         user's deactivation is in pending.
		 */
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (extraInfoMap != null
				&& extraInfoMap.containsKey(EXTRA_INFO_ADRBT_DEACTIVATION))
			m_rbtDBManager.updateRBTTypeAndPlayerStatus(strSubID, 0, "A");
		// End of BSNL Ad RBT changes

		if (ret.equalsIgnoreCase(m_success) && reason != null
				&& !reason.equalsIgnoreCase("null"))
			m_rbtDBManager.updateExtraInfo(strSubID,
					EXTRA_INFO_FAILURE_MESSAGE, reason);

		// Subscription success
		if (ret.trim().equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {

				// v
				//RBT-18483 & RBT-18121 Restricting while activating
				try {
					FeatureListRestrictionCommandList featureListRestrictionCommandList = (FeatureListRestrictionCommandList) ConfigUtil
							.getBean(BeanConstant.FEATURE_LIST_RESTRICTION_COMMAND_LIST);
					if (featureListRestrictionCommandList != null) {
						featureListRestrictionCommandList.executeCallbackCommands(subscriber.subID(), classType);
					}
				} catch (Exception e) {
					// Ignore id bean is not configured
					logger.error(e);
				}

				String circleId = subscriber.circleID();

				if (circleId != null && circleIDFromPrism != null && !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true, subscriber.subID(), circleIDFromPrism, subscriber.refID(),
							null);
				}
			}
			
			if (isEnablePrePromptIfNoSel) {
				addFreeSongBasedOnCircle(subscriber);
			}
			
			boolean isCorpSubBlockedForContest = RBTParametersUtils.getParamAsBoolean("DAEMON",
						"BLOCK_CORP_SUBSCRIBER_FOR_CONTEST", "FALSE");
			logger.info("test for : isCorpSubBlockedForContest =" + isCorpSubBlockedForContest +" and isCorpSub(strSubID) = " +isCorpSubscriber(subscriber));
			boolean contestDisabledForCorpUser = false;
			if(isCorpSubBlockedForContest && isCorpSubscriber(subscriber)){
				contestDisabledForCorpUser = true;
			}
			// To hit contest url
			String circleId = subscriber.circleID();
			if (!contestDisabledForCorpUser) {
				RBTContestUtils.hitContestUrl(strSubID, circleId, activatedBy,
						amountCharged, "ACT");

				int pointsEarned = RBTContestUtils.getPointsEarned("ACT",
						action, classType, amountCharged);
				if (pointsEarned > 0) {
					RBTContestUtils.pointsContest(strSubID, pointsEarned);
				}
			}
			if (getParamAsBoolean("SUPPORT_IBM_INTEGRATION", "FALSE")) {
				// IBM-Integration
				RBTCallBackEvent.update(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION, strSubID,
						subscriber.refID(), RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED,
						classType);
			}
			// RBT-10785: OI deployements.
			// Incase subscriber has any selections hit UMP
			String umpUrl = RBTParametersUtils.getParamAsString("DAEMON",
					"UMP_URL_SUB_ACT", null);
			if (null != umpUrl) {
				checkSubSelectionAndCallUmp(strSubID, umpUrl);
			}

			// Feature for Peru to add a download to the subscriber
			// on Act & Renewal
			addDownloadForTheDay(strSubID, "ACT");

			// Add a selection configured for subscriptionClass in
			// OperatorCode2 field
			addSelectionBasedOnSubClassOpCode(strSubID, subClass,
					subscriber.activatedBy());

			SRBTUtility.updateSocialSubscriberForSuccess(m_socialRBTAllowed,
					m_socialRBTAllUpdateInOneModel, extraInfoMap, strSubID,
					type, classType, activatedBy, evtType);
			if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
					&& subClass != null) {
				if (getParamAsBoolean("SEND_SMS_PRE_CHRG", "FALSE")
						&& activatedBy != null
						&& Arrays.asList(
								getParamAsString("DAEMON",
										"ACTIVATED_PRE_CHRG", " ").split(","))
								.contains(activatedBy) // HERE
						&& subClass.getSmsOnSubscription() != null
						&& !subClass.getSmsOnSubscription().equalsIgnoreCase(
								"null")) {
					sendSMS(strSubID,
							getReasonSMS(subClass.getSmsOnSubscription(),
									reason), createTime);
				} else if (chargeSms == null) {
					if (subClass.getSmsOnSubscription() != null
							&& !subClass.getSmsOnSubscription()
									.equalsIgnoreCase("null"))
						sendSMS(strSubID,
								getReasonSMS(subClass.getSmsOnSubscription(),
										reason), createTime);
				} else {
					if (type != null && type.equalsIgnoreCase("p"))
						sendSMS(strSubID,
								getReasonSMS(chargeSms.getPrepaidSuccess(),
										reason), createTime);
					else
						sendSMS(strSubID,
								getReasonSMS(chargeSms.getPostpaidSuccess(),
										reason), createTime);
				}
			}

			// if the callback request is for songpack or limited
			// downloads then dont update the selections
			if (!STATE_CHANGE.equals(finalSubscriptionYes)) {
				if (isProfilePack) {
					List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
							.getBySubscriberIDAndStatus(strSubID);
					ProvisioningRequests provisioningRequest = null;
					if (provisioningRequestsList != null
							&& provisioningRequestsList.size() > 0) {
						provisioningRequest = provisioningRequestsList.get(0);
						int cosID = provisioningRequest.getType();
						boolean isProfile = false;
						String cosType = null;
						CosDetails cosdetail = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										cosID + "");
						if (cosdetail != null)
							cosType = cosdetail.getCosType();

						if (cosdetail == null
								|| cosType == null
								|| (!cosType.equalsIgnoreCase(SONG_PACK)
										&& !cosType
												.equalsIgnoreCase(LIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(AZAAN)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS_OVERWRITE)
										&& !cosType
												.equalsIgnoreCase(LIMITED_SONG_PACK_OVERLIMIT)
										&& !cosType
												.equalsIgnoreCase(PROFILE_COS_TYPE) && !cosType
											.equalsIgnoreCase(COS_TYPE_AUTO_DOWNLOAD)&& !cosType
											.equalsIgnoreCase(MUSIC_POUCH))) { 
							return FAILURE;
						}

						if (cosdetail.getCosType().equalsIgnoreCase(
								PROFILE_COS_TYPE)) {
							isProfile = true;
						}
						if (provisioningRequest != null) {
							if (provisioningRequest.getStatus() == PACK_ACTIVATED) {
								if (!smUpdateProfileSelStatusOnSubscriptionSuccess(
										strSubID, isProfile)) {
									return FAILURE;
								}
							} else { // Base act pending state pack
										// state needs to be made to be
										// active
								if (!ProvisioningRequestsDao.updateRequest(
										strSubID,
										provisioningRequest.getTransId(),
										PACK_TO_BE_ACTIVATED + "", false, -1))
									return FAILURE;
							}
						}
					}
				} else {
					if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
							"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
						logger.info("Received Base Subscription Success Callback. Going to update the ODA Pack if any");
						List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
								.getBySubscriberIDAndStatus(strSubID);
						if (provisioningRequestsList != null && provisioningRequestsList.size() > 0) {
							for (ProvisioningRequests provisioningRequest : provisioningRequestsList) {
								int status = provisioningRequest.getStatus();
								int odaCategory = provisioningRequest.getType();
								Category category = m_rbtCacheManager.getCategory(odaCategory);
								if (category != null && category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE
										&& status == 30) {
									ProvisioningRequestsDao.updateRequest(strSubID,
											provisioningRequest.getTransId(), PACK_TO_BE_ACTIVATED
													+ "", false, -1);
								}
							}
						}
					}
				}
				// smUpdateSelStatus(strSubID);
				if (!smUpdateSelStatusSubscriptionSuccess(strSubID,
						isProfilePack, false, null))// RBT-14301: Uninor MNP changes.
					return FAILURE;

				// checks if any upgradation requests are pending
				processUpgradeTransaction(subscriber);
			}
			if (getParamAsBoolean("SEND_SMS_TO_RETAILER", "FALSE")) {
				String selectedBy = subscriber.activatedBy();
				String retailerMode = getParamAsString(iRBTConstant.DAEMON,
						"MODE_FOR_RETAILER_DT", "RET");
				if (selectedBy.equalsIgnoreCase(retailerMode)) {
					String selectInfo = subscriber.activationInfo();
					String[] selectInfoArr = selectInfo.split("\\|");
					String retailerNumber = null;
					for (String value : selectInfoArr) {
						if (value.startsWith("RET:"))
							retailerNumber = value
									.substring(value.indexOf(":") + 1);
					}
					String smsText = CacheManagerUtil.getSmsTextCacheManager()
							.getSmsText("RETAILER_BASE_CONFIRM", "SUCCESS",
									subscriber.language());
					smsText = getRetailerFinalSMS(smsText, subscriber.subID(),
							null, null);
					Tools.sendSMS(getSenderNumber(subscriber.circleID()),
							retailerNumber, smsText,
							getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"),
							createTime);
				}
			}

			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "0", "Subscription", null, null);
			return SUCCESS;
		} else if (ret.equalsIgnoreCase(m_failure)) {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return INVALID;
		} else {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return FAILURE;
		}
	}

	/**
	 * Base Renewal success Callback
	 * @param strSubID
	 * @param type
	 * @param classType
	 * @param reason
	 * @param rtKey
	 * @param SUCCESS
	 * @param FAILURE
	 * @param INVALID
	 * @param subscriber
	 * @param nextChargeDate
	 * @param subClass
	 * @param subscriptionYes
	 * @param oldSubClass
	 * @param extraInfo
	 * @param chargeSms
	 * @param renAmt
	 * @param amountCharged
	 * @param activatedBy
	 * @param finalActInfo
	 * @param isProfilePack
	 * @param action
	 * @return
	 */
	private String processRenewalActivationSuccess(String strSubID,
			String type, String classType, String reason, String rtKey,
			String SUCCESS, String FAILURE, String INVALID,
			Subscriber subscriber, Date nextChargeDate,
			SubscriptionClass subClass, String subscriptionYes,
			String oldSubClass, String extraInfo, ChargeSMS chargeSms,
			double renAmt, String amountCharged, String activatedBy,
			String finalActInfo, boolean isProfilePack, String action, String circleIDFromPrism, String strNextBillingDate) {
		logger.info("Processing renewal activation success callback."
				+ " SubscriberId:" + strSubID);
		String createTime;
		// RENEWAL CASE
		boolean isRenewalCallback = true;
		//<RBT-12942>
		//If the subscriber is in renewal grace, the key-value pair indicating the same would be removed from
		//subscriber extraInfo
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (extraInfoMap != null && extraInfoMap.containsKey(WebServiceConstants.RENEWAL_GRACE)) {
			extraInfoMap.remove(WebServiceConstants.RENEWAL_GRACE);
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		}
		if (!subscriptionYes.equals("B")) {
			if (subscriptionYes.equals("A")
					|| ((subscriptionYes.equals("N") || subscriptionYes
							.equals("E")) && oldSubClass == null)) {				
				// Activating the user
				String response = m_rbtDBManager.smRenewalSuccessActivateUser(
						strSubID, nextChargeDate, type, classType, extraInfo, strNextBillingDate);
				if (response.equals(m_success)) {
					// RBT-14301: Uninor MNP changes.
					if (subscriber != null) {
						String circleId = subscriber.circleID();
						if (circleId != null && circleIDFromPrism != null
								&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
							smUpdateCircleIdForSubscriber(true,
									subscriber.subID(), circleIDFromPrism,
									subscriber.refID(), null);
						}
					}
					if (isProfilePack) {
						List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
								.getBySubscriberIDAndStatus(strSubID);
						ProvisioningRequests provisioningRequest = null;
						if (provisioningRequestsList != null
								&& provisioningRequestsList.size() > 0)
							provisioningRequest = provisioningRequestsList
									.get(0);
						int cosID = provisioningRequest.getType();
						boolean isProfile = false;
						String cosType = null;
						CosDetails cosdetail = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										cosID + "");
						if (cosdetail != null)
							cosType = cosdetail.getCosType();

						if (cosdetail == null
								|| cosType == null
								|| (!cosType.equalsIgnoreCase(SONG_PACK)
										&& !cosType
												.equalsIgnoreCase(LIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(AZAAN)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS)
										&& !cosType
												.equalsIgnoreCase(UNLIMITED_DOWNLOADS_OVERWRITE)
										&& !cosType
												.equalsIgnoreCase(LIMITED_SONG_PACK_OVERLIMIT)
										&& !cosType
												.equalsIgnoreCase(PROFILE_COS_TYPE) && !cosType
											.equalsIgnoreCase(COS_TYPE_AUTO_DOWNLOAD))) {
							return FAILURE;
						}

						if (cosdetail.getCosType().equalsIgnoreCase(
								PROFILE_COS_TYPE)) {
							isProfile = true;
						}
						if (provisioningRequest != null) {
							if (provisioningRequest.getStatus() == PACK_ACTIVATED) {
								if (!smUpdateProfileSelStatusOnSubscriptionSuccess(
										strSubID, isProfile)) {
									return FAILURE;
								}
							} else { // Base act pending state pack
										// state needs to be made to
										// be active
								if (!ProvisioningRequestsDao.updateRequest(
										strSubID,
										provisioningRequest.getTransId(),
										PACK_TO_BE_ACTIVATED + "", false, -1))
									return FAILURE;
							}
						}
					}
					writeEventLog(extraInfo, subscriber.subID(),
							subscriber.activatedBy(), "0", "Subscription",
							null, null);
					smUpdateSelStatusSubscriptionSuccess(strSubID, false, false, null);// RBT-14301: Uninor MNP changes.
					
					
					
				}
				return response;
			} else if (subscriptionYes.equals("X")) {
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.activatedBy(), "101", "Subscription", null,
						null);
				return SUBSCRIPTION_DEACTIVE;
			} else if (subscriptionYes.equals("C")
					|| ((subscriptionYes.equals("E") || subscriptionYes
							.equals("N")) && oldSubClass != null))
				isRenewalCallback = false;

		} else if (subscriber.nextChargingDate() != null
				&& subscriber.nextChargingDate().getTime() > System
						.currentTimeMillis()) {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "103", "Subscription", null, null);
			return INVALID + "|" + "SUBSCRIPTION RENWAL ALREADY UPDATED";
		}

		try {
			if (m_rbtDBManager.m_isLTPOnForBaseRen
					&& m_rbtDBManager.m_ltpActMap != null
					&& activatedBy != null
					&& m_rbtDBManager.m_ltpActMap.containsKey(activatedBy
							.toUpperCase())) {
				int ltpPoints = ((Integer) m_rbtDBManager.m_ltpActMap
						.get(activatedBy.toUpperCase())).intValue();
				if (ltpPoints > 0)
					finalActInfo = m_rbtDBManager.addLTPPoints(finalActInfo,
							ltpPoints);
			}
		} catch (Throwable e) {
			logger.error("", e);
			logger.info(e.getMessage());
		}
		String ret = m_success;
		if (isRenewalCallback) {
			addDownloadForTheDay(strSubID, "REN");
			// Renewal trigger callback. If rtkey exists in
			// callback, update the extraInfo.
			if (rtKey != null) {
				if (rtKey.trim().startsWith("RBT_ACT_"))
					rtKey = rtKey.substring(8);
				if (extraInfoMap == null)
					extraInfoMap = new HashMap<String, String>();
				extraInfoMap.put("RT_KEY", rtKey);
			} else if (extraInfoMap != null
					&& extraInfoMap.containsKey("RT_KEY")) {
				extraInfoMap.remove("RT_KEY");
			}
			boolean playstatusA=false;
			if(extraInfoMap != null && extraInfoMap.containsKey(com.onmobile.apps.ringbacktones.v2.common.Constants.PAID_UNDER_LOWBAL)){
				extraInfoMap.remove(com.onmobile.apps.ringbacktones.v2.common.Constants.PAID_UNDER_LOWBAL);
				playstatusA=true;
			}
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

			// subscription renewal activation success
			ret = smRenewalSuccess(strSubID, nextChargeDate, type, classType,
					finalActInfo, extraInfo, strNextBillingDate,playstatusA);
			//v
			/*		
			if (subscriber != null) {
				//If after suspension user get Renewal success callback , then we will activate suspended selection
				Map<String, String> map = getParamAsMap("DAEMON", "RENEWAL_FAILURE_SUB_CLASS_CHANGE", null);
				Boolean toActivateSuspendSelection = map.size() > 0 && map.containsKey(subscriber.subscriptionClass());
				logger.info(":---> toactivateSuspendSelection :" + toActivateSuspendSelection);
				if (toActivateSuspendSelection) {
					String CategoryId = ConsentPropertyConfigurator.getFreeClipCategoryID();
					logger.info("FreeclipCategoryId" + CategoryId);
					RBTDBManager.getInstance().activateSubscriberSuspendedRecordsByNotCategoryIdNotStatus(strSubID,
							Integer.parseInt(CategoryId), 200);
				}
			}*/
			logger.info("smRenewalSuccess status: " + ret + ", action: " + action + ", classType: " + classType);

			if (m_success.equals(ret)) {
				// RBT-14301: Uninor MNP changes.
				if (subscriber != null) {
					String circleId = subscriber.circleID();
					if (circleId != null && circleIDFromPrism != null
							&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
						smUpdateCircleIdForSubscriber(true,
								subscriber.subID(), circleIDFromPrism,
								subscriber.refID(), null);
					}
				}
				int pointsEarned = RBTContestUtils.getPointsEarned("ACT", action, classType, amountCharged);
				if(pointsEarned > 0) {
					RBTContestUtils.pointsContest(strSubID, pointsEarned);
				}
				
				// RBT-10785: OI deployements
				// Incase subscriber has any selections hit UMP
				String umpUrl = RBTParametersUtils.getParamAsString("DAEMON",
						"UMP_URL_SUB_REN", null);
				if (null != umpUrl) {
					checkSubSelectionAndCallUmp(strSubID, umpUrl);
				}
				
				// remove PROMPT flag from subscriber's extra_info
				// based on configurations
				disablePromptForSubscriber(subscriber, classType);
				if (!subscriptionClassAndCosIdMap.isEmpty()) {
					String renewalCosId = subscriptionClassAndCosIdMap
							.get(classType);
					// No need to update for the same type of cos
					// id.
					if (renewalCosId != null
							&& !subscriber.cosID().equals(renewalCosId)) {

						CosDetails cosDetails = m_rbtCosCacheManager
								.getCosDetail(renewalCosId);
						if (null != cosDetails) {
							Date endDate = null;
							boolean result = m_rbtDBManager
									.updateSubscriberCosId(strSubID,
											renewalCosId, endDate);
							logger.info("Updated CosId. result: " + result);
						} else {
							logger.warn("Invalid CosId, could not update");
						}
					} else {
						logger.info("Cos Id is not found from the configuration of SUBCLASS_COS_MAP_FOR_CHANGE_ON_RENEWAL");
					}
				} else {
					logger.info("No CosIds found in the configuration of SUBCLASS_COS_MAP_FOR_CHANGE_ON_RENEWAL");
				}
				
				
				//RBT - 15848 Callback handler changes for toneplayer integration
				try{
					ServiceMappingBean serviceMappingBean = (ServiceMappingBean) ConfigUtil.getBean(BeanConstant.SERVICE_MAPPING_BEAN);
					if(serviceMappingBean != null) {
						RBTDaemonService rbtDaemonService = serviceMappingBean.getRbtDaemonService();
						if(rbtDaemonService != null) {
							rbtDaemonService.updateSubscribersInPlayer(subscriber, false);
						}
					}
				}
				catch(Exception e){
					logger.error("Bean is not configured: "+e,e);
				}
				
				//RBT-16337 Vodafone-Greece - RBT PPD User- Monthly Free song offer
				String cosId = subscriber.activatedCosID();
				
				String monthlyFirstSongFreeCosId  = RBTParametersUtils.getParamAsString("DAEMON", "MONTHLY_FIRST_SONG_COSID", "");
				
				List<String> freeCosIdList = (List<String>) ListUtils.convertToList(monthlyFirstSongFreeCosId, ":" );  
				
				if(freeCosIdList.contains(cosId)){
					m_rbtDBManager.updateNumMaxSelections(subscriber.subID(),0);
				}
				
			} else {

				logger.info("smRenewalSuccess failed");

			}

			// Added for Aircel Fix
			if (ret.equalsIgnoreCase(m_success)
					&& subscriptionYes.equalsIgnoreCase("z")
					&& getParamAsBoolean("DAEMON",
							"SUSPEND_BASE_FOR_LOW_AMOUNT", "FALSE")) {// RBT-14301: Uninor MNP changes.
				smUpdateSelStatusSubscriptionSuccess(false, strSubID, null);
			}
		} else {
			ret = m_rbtDBManager.smRenewalSuccessUpgradePending(strSubID,
					nextChargeDate, type, classType, finalActInfo, extraInfo);
			if (extraInfoMap != null
					&& extraInfoMap.containsKey("IS_SUSPENDED")) {
				m_rbtDBManager.updatePlayerStatus(strSubID, "A");
				m_rbtDBManager.updateExtraInfo(strSubID, DBUtility
						.removeXMLAttribute(subscriber.extraInfo(),
								"IS_SUSPENDED"));
			}
		}
		if (ret.equalsIgnoreCase(m_success) && reason != null
				&& !reason.equalsIgnoreCase("null"))
			m_rbtDBManager.updateExtraInfo(strSubID,
					EXTRA_INFO_FAILURE_MESSAGE, reason);
		if (ret.equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleId != null && circleIDFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true,
							subscriber.subID(), circleIDFromPrism,
							subscriber.refID(), null);
				}
			}
			if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
					&& subClass != null) {
				createTime = getCreateTime();
				if (chargeSms == null) {
					if (subClass.getSmsRenewalSuccess() != null
							&& !subClass.getSmsRenewalSuccess()
									.equalsIgnoreCase("null"))
						sendSMS(strSubID,
								getReasonSMS(subClass.getSmsRenewalSuccess(),
										reason), createTime);
				} else {
					if (type != null && type.equalsIgnoreCase("p")) {
						double amtCharged = -1;
						if (null != amountCharged) {
							amtCharged = Double.parseDouble(amountCharged);
						}
						if (renAmt != -1 && amtCharged != -1
								&& amtCharged < renAmt)
							sendSMS(strSubID,
									getReasonSMS(
											getParseSMS(chargeSms
													.getPrepaidNEFSuccess(),
													amtCharged,
													(renAmt - amtCharged)),
											reason), createTime);
						else
							sendSMS(strSubID,
									getReasonSMS(chargeSms
											.getPrepaidRenewalSuccess(), reason),
									createTime);
					} else
						sendSMS(strSubID,
								getReasonSMS(
										chargeSms.getPostpaidRenewalSuccess(),
										reason), createTime);
				}
			}
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "0", "Subscription", null, null);
			return SUCCESS;
		} else if (ret.equalsIgnoreCase(m_failure)) {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return INVALID;
		} else {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return FAILURE;
		}
	}

	private void checkSubSelectionAndCallUmp(String strSubID, String umpUrl) {
		logger.info("Checking selections for subscriber: " + strSubID
				+ ", umpUrl: " + umpUrl);
		SubscriberStatus[] subscriberStatus = m_rbtDBManager
				.getAllActiveSubscriberSettings(strSubID);

		if (null == subscriberStatus) {
			umpUrl = umpUrl.replace("%MSISDN%", strSubID);
			String response = makeHttpRequest(umpUrl);
			logger.info("Successfully made UMP hit. UMP url: " + umpUrl
					+ ", httpResponse: " + response);
		} else {
			logger.warn("Not making UMP hit, subscriber: " + strSubID
					+ " has '"+subscriberStatus.length+"' selections");
		}
	}
	/**
	 * Grace base activation callback
	 * @param strSubID
	 * @param type
	 * @param reason
	 * @param requestType
	 * @param SUCCESS
	 * @param FAILURE
	 * @param INVALID
	 * @param subscriber
	 * @param subscriptionYes
	 * @param oldSubClass
	 * @param extraInfo
	 * @param createTime
	 * @return
	 */
	private String processSubscriptionGraceActivation(String strSubID,
			String type, String reason, String requestType, String SUCCESS,
			String FAILURE, String INVALID, Subscriber subscriber,
			String subscriptionYes, String oldSubClass, String extraInfo,
			String createTime,String circleIDFromPrism) {
		logger.info("Processing grace activation callback. SubscriberId: "
				+ strSubID);
		if (!subscriptionYes.equals("N") && !subscriptionYes.equals("A")
				&& !subscriptionYes.equals("E")) {
			if (subscriptionYes.equals("G")) {
				logger.info("Subscription on Grace. SubscriberId: " + strSubID);
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.activatedBy(), "103", "Subscription", null,
						null);
				return SUBSCRIPTION_ALREADY_ON_GRACE;
			} else if (subscriptionYes.equals("B")) {
				logger.info("Subscription already active. SubscriberId: "
						+ strSubID);
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.activatedBy(), "103", "Subscription", null,
						null);
				return SUBSCRIPTION_ALREADY_ACTIVE;
			} else if ((subscriptionYes.equals("E") || subscriptionYes
					.equals("C")) && oldSubClass != null) {
				logger.info("Subscription active. SubscriberId: " + strSubID);
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.activatedBy(), "103", "Subscription", null,
						null);
				return SUBSCRIPTION_ACTIVE;
			} else if (subscriptionYes.equals("X")) {
				logger.info("Subscription deactive. SubscriberId: " + strSubID);
				return SUBSCRIPTION_DEACTIVE;
			} else if (subscriptionYes.equalsIgnoreCase("Z")) {
				logger.info("Subscription suspended. SubscriberId: " + strSubID);
				return SUBSCRIPTION_SUSPENDED;
			} else if (m_deActStatus.contains(subscriptionYes)) {
				logger.info("Callback already received. SubscriberId: "
						+ strSubID);
				return CALLBACK_ALREADY_RECEIVED;
			}
		}
		String ret = smSubscriptionGrace(strSubID, type);

		if (ret.equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleId != null && circleIDFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true,
							subscriber.subID(), circleIDFromPrism,
							subscriber.refID(), null);
				}
			}
			if (toUpdateSelectionGraceInCombo && requestType != null
					&& requestType.equalsIgnoreCase("combo")) {
				m_rbtDBManager.smUpdateSelStatus(strSubID,
						STATE_ACTIVATION_PENDING, STATE_GRACE);
			}

			String baseGraceSms = getParamAsString("BASE_GRACE_SMS");
			if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
					&& baseGraceSms != null && baseGraceSms.length() > 0)
				sendSMS(subscriber.subID(), getReasonSMS(baseGraceSms, reason),
						createTime);

			logger.info("Subscriptin grace database update is Success."
					+ ", SubscriberId: " + strSubID);
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "0", "Subscription", null, null);
			return SUCCESS;
		} else if (ret.equalsIgnoreCase(m_failure)) {
			logger.info("Subscriptin grace database update is Fail."
					+ ", SubscriberId: " + strSubID);
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return INVALID;
		} else {
			logger.info("Subscriptin grace database update is Fail."
					+ ", SubscriberId: " + strSubID);
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.activatedBy(), "101", "Subscription", null, null);
			return FAILURE;
		}
	}

	private String processSubscriptionDeactvationSuccess(String strSubID,
			String status, String type, String classType, String reasonCode,
			Subscriber subscriber, String extraInfo, String mode, String circleIDFromPrism) {
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		logger.info("Processing deactivation success callback. SubId: "
				+ strSubID + ", status: " + status);
		String response = processSubscriptionDeactivation(subscriber,
				classType, reasonCode,circleIDFromPrism);
		// RBT-8472 :Du Client - Webservice Changes
		if (response.equalsIgnoreCase("success")
				&& deregistraionModeList.contains(mode)) {
			m_rbtDBManager.deleteRBTLoginUserBySubscriberID(strSubID, null);
		}//RBT-14671 - # like
		if (response.equalsIgnoreCase("success") && hashLikeKeys != null
				&& !hashLikeKeys.isEmpty()) {
			try {
				m_rbtDBManager.deleteSubscriberLikedSong(strSubID, -1, -1);
			} catch (Exception e) {
				logger.info("Table is not there in the data base for like: "
						+ e.getMessage());
			}

		}
		String freemiumGroupCosId = RBTParametersUtils.getParamAsString(COMMON,
				"FREEMIUM_GROUP_COSID", null);
		List<String> freemiumGroupCosIdList = new ArrayList<String>();
		if (freemiumGroupCosId != null && !freemiumGroupCosId.isEmpty()) {
			freemiumGroupCosIdList = Arrays.asList(freemiumGroupCosId
					.split(","));
		}
		
		//RBT-17084
		if (response.equalsIgnoreCase("success")){
			String categoryIdstr="";
			boolean removeTStatus=getParamAsBoolean("DAEMON", "REMOVE_DOWNLOAD_WITH_T_STATUS", "false");
			if((RBTDBManager.catTypesForAutoRenewal != null && RBTDBManager.catTypesForAutoRenewal.size() > 0) || removeTStatus){			
				categoryIdstr=Utility.listToStringWithQuots(RBTDBManager.catTypesForAutoRenewal);
				m_rbtDBManager.deleteDownloadwithTstatusAndCategoryType(strSubID,categoryIdstr);
			}
		}
		if (response.equalsIgnoreCase("success")
				&& ((!freemiumGroupCosIdList.isEmpty() && freemiumGroupCosIdList
						.contains(subscriber.cosID())) || (extraInfoMap != null && extraInfoMap
						.containsKey("FR_GROUP2_USER")))) {
			//Put into memcache also(SubscriberId,date format)
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(strSubID).append(",");
			strBuilder.append(sdf.format(new Date()));
			if(FreemiumMemcacheClient.getInstance().isCacheInitialized()) {
				logger.info("Freemium cache is initialized. So, putting into memcache for blacklisting");
			    FreemiumMemcacheClient.getInstance().addSubscriberBlacklistTime(strSubID,
					 sdf.format(new Date()));
			}
			RBTEventLogger.logEvent(RBTEventLogger.Event.FREEMIUM_BLACKLIST,
					strBuilder.toString());
		}
		removeAllUpgradeTransactions(subscriber, "DEACTIVATION_SUCCESS");
		// code added to insert in rbt_social_subscriber,and delete from
		// rbt_social_site_user table
		if (getParamAsBoolean("SRBT", "SRBT_ACCOUNT_EXPIRY_FLAG", "false")) {
			if (response != null && response.equalsIgnoreCase(m_success)) {
				evtType = evtTypeForAccountExpiry;
				logger.info("evt type for deactivation :: " + evtType);
				RbtSocialSubscriberDAO.deleteSocialSubscriber(Long
						.parseLong(strSubID));
				logger.info("evt type for deactivation :: " + strSubID);
				SRBTUtility.updateSocialSubscriberForSuccess(
						m_socialRBTAllowed, m_socialRBTAllUpdateInOneModel,
						extraInfoMap, strSubID, type, classType,
						subscriber.deactivatedBy(), evtType);
				// SRBTUtility.deactivateSocialSiteUser(strSubID);
				logger.info("Successfully deactivated Subscriber:" + strSubID
						+ " from Social RBT.");
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.deactivatedBy(), "0", "Subscription", null,
						null);
				evtType = 0;
			}
		}

		/*
		 * When the customer deactivate the service, give an option about taking
		 * his consent as to why did the customer do a churn action, was he not
		 * satisfied with the service.
		 */
		String umpRedirectUrl = getParamAsString("DAEMON",
				"CHURN_MANAGEMENT_UMP_URL", null);
		if (null != umpRedirectUrl) {
			umpRedirectUrl = umpRedirectUrl.replaceAll("<msisdn>",
					subscriber.subID());
			String umpResponse = makeHttpRequest(umpRedirectUrl);
			logger.info("Sent request to " + umpRedirectUrl + ", response: "
					+ umpResponse);
		}

		logger.info(" response: " + response);
		return (response == null) ? m_FAILURE : response;
	}

	private void addFreeSongBasedOnCircle(Subscriber subscriber) {
		try {
			if (null != freeSongForCirclesMap
					&& 0 < freeSongForCirclesMap.size()) {
				String subscriberId = subscriber.subID();
				String circleId = subscriber.circleID();
				logger.info("Setting default song as selection based on circle."
						+ " SubscriberId: "
						+ subscriberId
						+ ", CircleId: "
						+ circleId);
				SubscriberStatus selections[] = m_rbtDBManager
						.getAllSubscriberSelectionRecords(subscriberId, null);
				logger.info("Subscriber active selections: " + selections);
				if (null == selections) {
					int categoryId = 6;

					// circleId=clipId,chargeClass:circleId=clipId,chageClass:
					String clipAndChargeClass = freeSongForCirclesMap
							.get(circleId);
					int commaIndex = clipAndChargeClass.lastIndexOf(",");
					String clipId = clipAndChargeClass.substring(0, commaIndex)
							.trim();
					Clips clips = m_rbtDBManager.getClip(clipId);
					if (null == clips) {
						String error = "Invalid clip configured. clipId: "
								+ clipId + " for circleId: " + circleId;
						logger.error(error);
						throw new Exception(error);
					}
					String chargeClass = clipAndChargeClass.substring(
							commaIndex + 1, clipAndChargeClass.length()).trim();
					if (null == chargeClass || "".equals(chargeClass)) {
						String error = "Wrong configuration for chargeClass."
								+ " circleId: " + circleId;
						logger.error(error);
						throw new Exception(error);
					}
					HashMap<String, String> selectionInfoMap = new HashMap<String, String>();

					selectionInfoMap.put(
							iRBTConstant.EXTRA_INFO_INTRO_PROMPT_FLAG,
							ENABLE_PRESS_STAR_INTRO);

					String promptWaveFile = prePromptWavIfNoSel.get(circleId);
					if (null != promptWaveFile) {
						selectionInfoMap.put(
								iRBTConstant.EXTRA_INFO_PRE_RBT_WAV,
								promptWaveFile);
					}

					selectionInfoMap.put(iRBTConstant.EXTRA_INFO_FREE_SONG,
							"true");

					logger.info("Subscriber Extra Info map contains: "
							+ selectionInfoMap);

					SelectionRequest selRequest = new SelectionRequest(
							subscriberId, String.valueOf(categoryId),
							String.valueOf(clips.id()));
					selRequest.setMode(modeForFreeSongIfNoSel);
					selRequest.setUseUIChargeClass(true);
					selRequest.setChargeClass(chargeClass);
					selRequest.setSelectionInfoMap(selectionInfoMap);

					RBTClient.getInstance().addSubscriberSelection(selRequest);

					logger.info("Successfully added selection. clipId: "
							+ clipId);
				} else {
					logger.warn("Not setting default song based on circle. Because, "
							+ " subscriber has pending or active selections");
				}
			}
		} catch (Exception e) {
			logger.error("Unable to add free song based on circle. Exception: "
					+ e.getMessage(), e);
		}
	}

	private void resetFreeSongBasedOnCircle(Subscriber subscriber, String refID) {
		// No need to update player status.
		// This to be called from selection
		// 1. deactivation success callback or
		// 2. activation failure callback or
		// 3. renewal failure callback
		if (null != subscriber) {
			try {
				SubscriberStatus selection = m_rbtDBManager.getRefIDSelection(
						subscriber.subID(), refID);

				logger.info("Subscriber selection: " + selection);
				if (null != selection && null != selection.extraInfo()) {
					String extraInfoStr = selection.extraInfo();
					logger.info("Subcriber: " + subscriber.subID()
							+ ", extraInfo: " + extraInfoStr);
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfoStr);
					String freeSong = extraInfoMap.get(EXTRA_INFO_FREE_SONG);
					if ("true".equals(freeSong)) {
						String subPrompt = extraInfoMap
								.get(EXTRA_INFO_INTRO_PROMPT_FLAG);
						if (null != subPrompt) {
							logger.info("Prompt is present in ExtraInfo. prompt: "
									+ subPrompt);
							String preCallPrompt = getParamAsString(COMMON,
									"PRE_CALL_PROMPT");
							if (null != preCallPrompt) {
								extraInfoStr = DBUtility.setXMLAttribute(
										extraInfoStr,
										EXTRA_INFO_INTRO_PROMPT_FLAG,
										preCallPrompt);
							} else {
								extraInfoStr = DBUtility.setXMLAttribute(
										extraInfoStr,
										EXTRA_INFO_INTRO_PROMPT_FLAG,
										DISABLE_PRESS_STAR_INTRO);
							}
						}

						String configRbtWav = prePromptWavIfNoSel
								.get(subscriber.circleID());
						String subRbtWav = extraInfoMap
								.get(EXTRA_INFO_PRE_RBT_WAV);
						logger.info("Configured RbtWav: " + configRbtWav
								+ ", Subscriber ExtraInfo RbtWav file: "
								+ subRbtWav);
						if (null != configRbtWav
								&& configRbtWav.equals(subRbtWav)) {
							extraInfoStr = DBUtility.removeXMLAttribute(
									extraInfoStr, EXTRA_INFO_PRE_RBT_WAV);
						}

						extraInfoStr = DBUtility.removeXMLAttribute(
								extraInfoStr, EXTRA_INFO_FREE_SONG);

						logger.info("Updating ExtraInfo with: " + extraInfoStr);
						ArrayList<String> list = new ArrayList<String>();
						list.add(refID);
						m_rbtDBManager.updateSelectionExtraInfo(
								subscriber.subID(), list, extraInfoStr);
					}

				}
			} catch (Exception e) {
				logger.error("Unable to reset freeSongBasedOnCircle,"
						+ " Exception: " + e.getMessage(), e);
			}
		}
	}

	private void addDownloadForTheDay(String subId, String type) {
		if (getParamAsBoolean("IS_DOWNLOAD_OF_THE_DAY_ENABLED", "FALSE")) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date currentDate = new Date();
			String playDate = sdf.format(currentDate);
			PickOfTheDay pick = m_rbtDBManager.getDownloadOfTheDay(playDate,
					type);
			if (pick != null) {
				Clip clip = m_rbtCacheManager.getClip(pick.clipID());
				if (clip != null) {

					com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = m_rbtCacheManager
							.getCategory(pick.categoryID());
					Categories categoriesObj = CategoriesImpl
							.getCategory(category);
					if (category != null && categoriesObj != null) {
						logger.info("Calling Add downloads to add the download of the Day ");
						String selBy = getParamAsString("DAEMON",
								"DEFAULT_SELBY_FOR_DOWNLOAD_OF_THE_DAY",
								"SYSTEM");
						m_rbtDBManager.addSubscriberDownloadRW(subId,
								clip.getClipRbtWavFile(), categoriesObj, null,
								true, category.getClassType(), selBy, selBy,
								null, true, true, false, null, null);
					}
				}
			} else
				logger.warn("Please configure the pick of the day corresponding to : "
						+ playDate);
		}
	}

	private void processUpgradeTransaction(Subscriber subscriber) {
		boolean isActPendingUsersAllowed = CacheManagerUtil
				.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON,
						"ALLOW_ACT_PENDING_USERS_FOR_UPGRADATION", "FALSE")
				.equalsIgnoreCase("TRUE");
		if (!isActPendingUsersAllowed)
			return;

		String subscriberID = subscriber.subID();
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getBySubscriberIDAndType(subscriberID,
						Type.BASE_UPGRADATION.getTypeCode());
		ProvisioningRequests provisioningRequest = null;
		if (provisioningRequestsList != null
				&& provisioningRequestsList.size() > 0)
			provisioningRequest = provisioningRequestsList.get(0);

		if (provisioningRequest != null) {
			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(provisioningRequest.getExtraInfo());
			if (extraInfoMap == null)
				extraInfoMap = new HashMap<String, String>();

			int oldRbtType = subscriber.rbtType();
			int newRbtType = subscriber.rbtType();
			if (extraInfoMap.containsKey(ExtraInfoKey.RBT_TYPE.toString())) {
				newRbtType = Integer.parseInt(extraInfoMap
						.get(ExtraInfoKey.RBT_TYPE.toString()));
				extraInfoMap.remove(ExtraInfoKey.RBT_TYPE.toString());
			}

			if (oldRbtType != newRbtType) // If AdRbt upgradation
			{
				extraInfoMap
						.put((newRbtType == 1 ? iRBTConstant.EXTRA_INFO_ADRBT_ACTIVATION
								: iRBTConstant.EXTRA_INFO_ADRBT_DEACTIVATION),
								"TRUE");
			}

			String extraInfo = subscriber.extraInfo();
			HashMap<String, String> subExtraInfo = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (subExtraInfo == null)
				subExtraInfo = new HashMap<String, String>();

			if (!extraInfoMap.containsKey("SCRN")) {
				subExtraInfo.remove("SCRS");
				subExtraInfo.remove("SCRN");
			}

			subExtraInfo.putAll(extraInfoMap);
			extraInfo = DBUtility.getAttributeXMLFromMap(subExtraInfo);

			String activationInfo = provisioningRequest.getModeInfo();
			String newActivationInfo = activationInfo;
			boolean concatActivationInfo = true;
			String subscriberActInfo = subscriber.activationInfo();
			if (subscriberActInfo.contains("scratchcard")) {
				newActivationInfo = subscriberActInfo;
				int noOfScratchCardUsed = 0;
				int index = 0;
				while (true) {
					index = subscriberActInfo.indexOf("scratchcard", index);
					if (index < 0)
						break;

					index++;
					noOfScratchCardUsed++;
					if (noOfScratchCardUsed > 2)
						newActivationInfo = newActivationInfo.replaceFirst(
								"scratchcard:[0-9]*\\|refid:[0-9]*\\|", "");
				}

				newActivationInfo += "|" + activationInfo;

				concatActivationInfo = false;
			}

			boolean isConversionSuccess = m_rbtDBManager
					.convertSubscriptionType(subscriberID,
							subscriber.subscriptionClass(),
							provisioningRequest.getChargingClass(),
							provisioningRequest.getMode(), newActivationInfo,
							concatActivationInfo, newRbtType, true, extraInfo,
							subscriber);

			String status = isConversionSuccess ? "SUCCESS" : "FAILURE";
			ProvisioningRequestsDao.removeByRequestId(subscriberID,
					provisioningRequest.getRequestId());

			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(provisioningRequest).append(", ").append(status);
			RBTEventLogger.logEvent(RBTEventLogger.Event.UPGRADETRANSACTION,
					logBuilder.toString());
			if (!isConversionSuccess)
				processUpgradeTransaction(subscriber);
		}
	}
	
	private String getOnlineURL (String msisdn, String mode) {

		if (mode == null
				|| !onlineCallbackUrlModesList.contains(mode.toUpperCase())) {
			logger.warn("Not making hit to online url, mode is not configured. subscriberId: "
					+ msisdn + ", mode: " + mode);
			return null;
		}

		String url = RBTParametersUtils.getParamAsString(COMMON, "ONLINE_CALLBACK_URL_" + mode.toUpperCase(), null);
		
		if (null == url || "".equalsIgnoreCase(url)) {
			url = RBTParametersUtils.getParamAsString(COMMON, "ONLINE_CALLBACK_URL", null);
		}
		return url;
	}
	
	private String hitOnlineURL(String msisdn, String serviceKey, String status,
			String mode, String amount, String vcode, String type, String selRefId) {		
		String response = null;
		String url = getOnlineURL(msisdn, mode);
		
		if (null != url) {
			HashMap<String, String> extraInfoMap = null;
			String extraInfo = null;
			
			if("ACT".equalsIgnoreCase(type)) {
				Subscriber subscriber = m_rbtDBManager.getSubscriber(msisdn);
				extraInfo = subscriber.extraInfo();
			} else if("SEL".equalsIgnoreCase(type)) {
				SubscriberStatus subStatus = m_rbtDBManager.getSelectionByRefId(msisdn, selRefId);
				extraInfo = subStatus.extraInfo();
			}
			if(null != extraInfo) {
				extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				if(extraInfoMap.containsKey("HIT_ONLINE_URL")) {
					logger.debug("Alredy hit the callback url once. Not going to hit for the second time.");
					return null;
				}
			}
			
			if (url.contains("%MSISDN%")) {
				url = url.replace("%MSISDN%", msisdn);
			}
			if (url.contains("%SERVICEKEY%")) {
				if(type.equalsIgnoreCase("ACT")) {
					serviceKey = "RBT_ACT_" + serviceKey;
				}
				else {
					serviceKey = "RBT_SEL_" + serviceKey;
				}
				url = url.replace("%SERVICEKEY%", serviceKey);
			}
			if(url.contains("%AMOUNT%")) {
				amount = amount != null ? amount:"";
				url = url.replace("%AMOUNT%", amount);
			}
			if(url.contains("%VCODE%")) {
				if(vcode != null) {
					vcode = vcode.substring(4, vcode.length() - 4);
				} else {
					vcode = "";
				}
				url = url.replace("%VCODE%", vcode);
			} 
			if(url.contains("%STATUS%")) {
				url = url.replace("%STATUS%", status);
			}
			if(url.contains("%MODE%")) {
				url = url.replace("%MODE%", mode);
			}
			if(url.contains("%TIMESTAMP%")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String date = null;
				try {
					date = sdf.format(new Date());
				} catch (Exception e) {
					logger.error("Error while formatting the date " + e.getMessage());
					e.printStackTrace();
				}
				url = url.replace("%TIMESTAMP%", date);
			}
			
			if(extraInfoMap == null) {
				extraInfoMap = new HashMap<String, String>();
			}
			
			extraInfoMap.put("HIT_ONLINE_URL", "TRUE");
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			
			logger.info("Going to hit the url after replacing the parameters: " + url);
			response = makeHttpRequest(url);
			
			if("ACT".equalsIgnoreCase(type)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("EXTRA_INFO", extraInfo);
				m_rbtDBManager.updateSubscriber(msisdn, map);
			} else if ("SEL".equalsIgnoreCase(type)) {
				ArrayList<String> refIdList = new ArrayList<String>();
				refIdList.add(selRefId);
				m_rbtDBManager.updateSelectionExtraInfo(msisdn, refIdList, extraInfo);
			}
		} else {
			logger.debug("Not making hit to online url, url is not configured. subscriberId: "
					+ msisdn + ", mode: " + mode);
		}
		return response;
	}

	private void removeAllUpgradeTransactions(Subscriber subscriber,
			String response) {
		String subscriberID = subscriber.subID();
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getBySubscriberIDAndType(subscriberID,
						Type.BASE_UPGRADATION.getTypeCode());
		if (provisioningRequestsList == null)
			return;

		for (ProvisioningRequests provisioningRequest : provisioningRequestsList) {
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(provisioningRequest).append(", ")
					.append(response);
			RBTEventLogger.logEvent(RBTEventLogger.Event.UPGRADETRANSACTION,
					logBuilder.toString());
		}
		ProvisioningRequestsDao.removeBySubscriberIDAndType(subscriberID,
				Type.BASE_UPGRADATION.getTypeCode());
	}

	public String processGift(String strSubID, String status, String refID,
			String amountCharged, String srvKey, String eventKey,
			String transID, String circleIDFromPrism) {// RBT-14301: Uninor MNP changes.

		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";

		try {
			StringTokenizer stk = new StringTokenizer(refID, ":");
			if (stk.hasMoreTokens()) {
				stk.nextToken();
				String gifter = strSubID;
				String giftedTo = null;
				String clipID = null;
				String giftTime = null;
				if (stk.hasMoreTokens())
					giftedTo = stk.nextToken();
				if (stk.hasMoreTokens())
					clipID = stk.nextToken();
				if (stk.hasMoreTokens())
					giftTime = stk.nextToken();

				String amt = null;
				if (amountCharged != null)
					amt = ":" + amountCharged;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date giftSentTime = sdf.parse(giftTime);

				if (srvKey != null && srvKey.contains("RBT_ACT_"))
					srvKey = srvKey.substring(8);
				if (eventKey != null && eventKey.contains("RBT_SEL_")) {
					eventKey = eventKey.substring(8);
					if (eventKey.indexOf("_GIFT") != -1)
						eventKey = eventKey.substring(0,
								eventKey.indexOf("_GIFT"));
				}

				ViralSMSTable viralSMSTable = m_rbtDBManager.getViralPromotion(
						gifter, giftedTo, giftSentTime, "GIFTCHRGPENDING");
				if (viralSMSTable == null)
					return INVALID + "|" + "GIFT ENTRY NOT FOUND";

				String mode = viralSMSTable.selectedBy();
				String extraInfo = null;
				extraInfo = DBUtility.setXMLAttribute(
						viralSMSTable.extraInfo(), SUBSCRIPTION_CLASS, srvKey);
				extraInfo = DBUtility.setXMLAttribute(extraInfo, CHARGE_CLASS,
						eventKey);
				if (transID != null)
					extraInfo = DBUtility.setXMLAttribute(extraInfo,
							GIFT_TRANSACTION_ID, transID);

				if (gifter != null && giftedTo != null && giftTime != null) {
					if (!status.equalsIgnoreCase("SUCCESS")) {
						int giftLimit = 0;
						if (clipID == null || clipID.equalsIgnoreCase("NULL")) {
							// Service Gift
							giftLimit = getParamAsInt(iRBTConstant.COMMON,
									"SERVICE_GIFT_LIMIT", 0);
						} else {
							// Tone Gift
							giftLimit = getParamAsInt(iRBTConstant.COMMON,
									"TONE_GIFT_LIMIT", 0);
						}

						if (giftLimit > 0) {
							SubscriberActivityCounts subscriberActivityCounts = SubscriberActivityCountsDAO
									.getSubscriberActivityCountsForDate(gifter,
											giftSentTime);
							if (subscriberActivityCounts != null) {
								if (clipID == null
										|| clipID.equalsIgnoreCase("NULL"))
									subscriberActivityCounts
											.decrementServiceGiftsCount();
								else
									subscriberActivityCounts
											.decrementToneGiftsCount();

								subscriberActivityCounts.update();
							}
						}
					}

					// checks if the gifted mode is supported for direct copy or
					// not
					String directGiftSupportedModes = RBTParametersUtils
							.getParamAsString(COMMON,
									"DIRECT_GIFT_SUPPORTED_MODES", "");
					List<String> directGiftModesList = Arrays
							.asList(directGiftSupportedModes.split(","));
					if (directGiftModesList.contains(mode))
						extraInfo = DBUtility.setXMLAttribute(extraInfo,
								GIFTTYPE_ATTR, "direct");
					
					boolean isToUpdateAmtInSelBy = RBTParametersUtils.getParamAsString(COMMON, "TO_UPDATE_GIFT_AMT_IN_SEL_BY_COL", "TRUE").equalsIgnoreCase("TRUE");
					
					if(!isToUpdateAmtInSelBy) {
						extraInfo = DBUtility.setXMLAttribute(extraInfo, "aountCharged", amt);
						amt = null;						
					}
					// RBT-14301: Uninor MNP changes.
					if (m_rbtDBManager.updateGiftCharge(gifter, giftedTo,
							clipID, giftTime, status, amt, extraInfo, circleIDFromPrism)) {
						int clipId = -1;
						if (clipID != null) {
							try {
								clipId = Integer.parseInt(clipID);
							} catch (Exception e) {
								clipId = -1;
								e.printStackTrace();
							}
						}
						String toneType = getToneType(clipId);
						SRBTUtility.updateSocialGiftForSuccess(
								m_socialRBTAllowed, extraInfo, giftedTo,
								gifter, giftSentTime, giftTime, amt, srvKey,
								directGiftSupportedModes, toneType, clipId);
						return SUCCESS;
					} else
						return FAILURE;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return INVALID + "|" + "DATABASE EXCEPTION";
		}

		return FAILURE;
	}

	public String getCriteria(int status, String callerId) {
		return getCriteria(status, callerId, null);
	}

	public String getCriteria(int status, String callerId,
			SubscriberStatus subStatus) {
		String criteria = "Default";
		if (callerId != null && callerId.toUpperCase().startsWith("G"))
			criteria = "Group";
		else if (subStatus != null && subStatus.selInterval() != null
				&& subStatus.selInterval().toUpperCase().startsWith("Y"))
			criteria = "Aniversary";
		else if (status == 80)
			criteria = "Schedule";
		else if (callerId != null)
			criteria = "MSISDN";
		return criteria;

	}

	public static void checkIfClipExistsAndUpdate(String subscriberID,
			String rbtWavFile, int categoryType, int categoryId) {
		if(com.onmobile.apps.ringbacktones.services.common.Utility.isD2CModel()) {
			logger.info("It's a D2C model, clip transfer will be done through D2C model..." + subscriberID + rbtWavFile);
			return;
		}
		ClipUtilService clipUtilServicBean = null;
		try {
			clipUtilServicBean = (ClipUtilService) ConfigUtil
					.getBean(BeanConstant.CLIP_UTIL_SERVICE_MAPPING_BEAN);
		} catch (Exception e) {
			logger.error("Bean is not configured: ", e);
			clipUtilServicBean = new ClipUtilService();
		}
		if (clipUtilServicBean != null) {
			clipUtilServicBean.checkIfClipExistsAndUpdate(subscriberID,
					rbtWavFile, categoryType, categoryId);
		}
	}

	public String selection(CallbackRequest callbackRequest) {// RBT-14301: Uninor MNP changes.
		
		String strSubID = callbackRequest.getStrSubID();
		String action = callbackRequest.getAction();
		String chargedDate = callbackRequest.getChargedDate(); 
		String status = callbackRequest.getStatus();
		String refID = callbackRequest.getRefID();
		String type = callbackRequest.getType();
		String amountCharged = callbackRequest.getAmountCharged();
		String classType = callbackRequest.getClassType();
		String reason = callbackRequest.getReason();
		String reasonCode = callbackRequest.getReasonCode();
		String sbnID = callbackRequest.getSbnID();
		String sys_mode = callbackRequest.getSys_mode();
		String circleIDFromPrism = callbackRequest.getCircleIDFromPrism();
		String strNextBillingDate = callbackRequest.getStrNextBillingDate();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat m_timeSdf = new SimpleDateFormat("ddMMMyy");
		logger.info("Received selection callback. subId: " + strSubID
				+ ", action: " + action + ", status: " + status
				+ ", classType: " + classType + ", refID: " + refID
				+ ", circleIDFromPrism: " + circleIDFromPrism);

		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";
		RBTNode node = RBTMonitorManager.getInstance().startNode(strSubID,
				RBTNode.NODE_SM_CALLBACK_SEL);
		SubscriberStatus subscriberStatus = null;
		String finalResponse = FAILURE;
		boolean isImmediate = true;
		
		try {
			Date nextChargeDate = null;
			String callerID = null;
			int songStatus = -1;
			String setTime = null;
			int fromTime = 0;
			int toTime = 2359;
			int ClipID = -1;
			String extraInfo = null;
			boolean validRefID = false;
			ChargeClass chargeClass = null;
			String selStatus = null;

			boolean canProcessClipStatus = getParamAsBoolean("COMMON",
					"CHECK_CLIP_STATUS_AND_PROCESS", "FALSE");
			boolean isAddToDownloads = getParamAsBoolean("COMMON",
					"ADD_TO_DOWNLOADS", "FALSE");
			logger.debug("Processing callback. isAddToDownloads: "
					+ isAddToDownloads);
			if (refID != null) {
				// added to update download callbacks from sub mgr
				if (refID.startsWith("RBTDOWNLOAD") || isAddToDownloads) {
					StringTokenizer stk = new StringTokenizer(refID, ":");
					int clipID = -1;
					String songName = null;
					String promoId = null;
					if (stk.hasMoreTokens()) {
						stk.nextToken();
						String downTime = null;
						if (stk.hasMoreTokens()) {
							String clip = stk.nextToken();
							try {
								clipID = Integer.parseInt(clip);
							} catch (Exception e) {
								clipID = -1;
							}
						}
						if (stk.hasMoreTokens())
							downTime = stk.nextToken();
						String movieName = null;
						String wav = null;
						if (clipID > 0) {
							Clip clip = m_rbtCacheManager.getClip(clipID);
							if (clip != null)
								wav = clip.getClipRbtWavFile();
							if (wav != null) {
								Clip songclip = getClipRBT(wav, -1);
								if (songclip != null) {
									songName = songclip.getClipName();
									movieName = songclip.getAlbum();
									promoId = songclip.getClipPromoId();
								}
								songName = (songName == null ? "N/A" : songName);
								movieName = (movieName == null ? "N/A"
										: movieName);
							}
						}

						Connection conn = m_rbtDBManager.getConnection();
						if (conn == null)
							return FAILURE;

						SubscriberDownloads download = m_rbtDBManager
								.getSMDownloadForCallback(conn, strSubID, refID);
						logger.debug("Successfully fetch the download from db. refID: "
								+ refID
								+ ", subscriberId: "
								+ strSubID
								+ ", download: " + download);
						if (download == null && wav != null && downTime != null
								&& status != null) {
							conn = m_rbtDBManager.getConnection();
							if (conn == null)
								return FAILURE;

							download = m_rbtDBManager
									.getSMDownloadForCallbackOldLogic(conn,
											strSubID, wav, downTime);

							logger.info("Getting download using old refID"
									+ " with status: " + status + ", action: "
									+ action + ", strSubID: " + strSubID
									+ ", refID: " + refID + ", downTime: "
									+ downTime);

							if (download == null) {
								logger.info("Couldn't getting download using"
										+ " old refID. Returning false"
										+ " as status: " + status
										+ ", action: " + action
										+ ", strSubID: " + strSubID
										+ ", refID: " + refID + ", type: "
										+ type);
							} else {
								conn = m_rbtDBManager.getConnection();
								if (conn == null)
									return FAILURE;

								m_rbtDBManager
										.updateSMDownloadForCallbackOldLogic(
												conn, strSubID, wav, downTime,
												refID);
							}
						}
						if (download != null) {
							wav = download.promoId();
							String modeOfActivation = download.selectedBy();
							Subscriber subscriber = m_rbtDBManager
									.getSubscriber(strSubID);

							Clip downloadCLip = m_rbtCacheManager
									.getClipByRbtWavFileName(wav);
							int clipId = -1;
							String contentType = null;
							if (downloadCLip != null) {
								clipId = downloadCLip.getClipId();
								contentType = downloadCLip.getContentType();
								songName = downloadCLip.getClipName();
								movieName = downloadCLip.getAlbum();
								promoId = downloadCLip.getClipPromoId();

								songName = (songName == null ? "N/A" : songName);
								movieName = (movieName == null ? "N/A"
										: movieName);
							}
							char downloadStat = download.downloadStatus();
							if (download.categoryID() == 104)
								songName = "Record My Own";
							String ret = null;
							//Download GRACE callback
							if (action.equals(m_strActionActivation)
									&& m_statusActGrace
											.equalsIgnoreCase(status)) {
								if (downloadStat != STATE_DOWNLOAD_ACTIVATION_PENDING
										&& downloadStat != STATE_DOWNLOAD_TO_BE_ACTIVATED
										&& downloadStat != STATE_DOWNLOAD_ACT_ERROR) {
									if (downloadStat == STATE_DOWNLOAD_ACTIVATED) {
										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "207",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"0",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										logger.warn(COULD_NOT_PROCESS
												+ " refId: " + refID
												+ ", download status: "
												+ downloadStat);
										return SELECTION_ALREADY_ACTIVE;
									} else if (downloadStat == STATE_DOWNLOAD_GRACE) {
										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "206",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										logger.warn(COULD_NOT_PROCESS
												+ " refId: " + refID
												+ ", download status: "
												+ downloadStat);
										return SELECTION_ALREADY_ON_GRACE;
									} else if (downloadStat == STATE_DOWNLOAD_SUSPENSION) {
										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "206",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										logger.warn(COULD_NOT_PROCESS
												+ " refId: " + refID
												+ ", download status: "
												+ downloadStat);
										return SELECTION_SUSPENSION;
									} else if (downloadStat == STATE_DOWNLOAD_DEACTIVATED)  {
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(),
												"203", "Purchase",
												downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										logger.warn(COULD_NOT_PROCESS
												+ " refId: " + refID
												+ ", download status: "
												+ downloadStat);
										return SELECTION_DEACTIVE;
									} else {
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(),
												"203", "Purchase",
												downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										logger.warn(COULD_NOT_PROCESS
												+ " refId: " + refID
												+ ", download status: "
												+ downloadStat);
										return SELECTION_DCT_PENDING;
									}
										
								}
								ret = m_rbtDBManager
										.smUpdateDownloadGraceCallback(
												strSubID, refID, status);
								logger.info("Processed Update Download Grace"
										+ " callback. Response: " + ret);
							} else {
								boolean noDownloadDeactSub = isNoDownloadDeactSub(subscriber);
								if (action.equals(m_strActionActivation)) {					// download activation success/failure callback
									if (downloadStat == STATE_DOWNLOAD_ACTIVATED) {
										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "207",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"0",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										return SELECTION_ALREADY_ACTIVE;
									} else if (downloadStat == STATE_DOWNLOAD_DEACTIVATED) {
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(),
												"203", "Purchase",
												downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										return SELECTION_ALREADY_DEACTIVE;
									} else if (downloadStat == STATE_DOWNLOAD_DEACTIVATION_PENDING		
											|| downloadStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED
											|| downloadStat == STATE_DOWNLOAD_DEACT_ERROR) {
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(),
												"203", "Purchase",
												downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										return SELECTION_DCT_PENDING;
									}

									if (subscriber == null) {
										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "204",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"404",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										return INVALID;
									}
									boolean isPrepaid = subscriber.prepaidYes();
									HashMap<String, String> xtraInfoMap = DBUtility
											.getAttributeMapFromXML(download
													.extraInfo());
									boolean isBISong = xtraInfoMap != null ? xtraInfoMap
											.containsKey("BI_SONG") : false;
									ret = m_rbtDBManager
											.smUpdateDownloadActivationCallback(
													strSubID, wav, refID,
													status, noDownloadDeactSub,
													type, classType, isPrepaid,
													xtraInfoMap,strNextBillingDate);
									logger.info("Download activation success "
											+ "callback response: " + ret);
									if (ret.indexOf(m_success) != -1) {	

										if (m_strActionActivation
												.equalsIgnoreCase(action)
												&& m_statusSuccess
														.equalsIgnoreCase(status)) {
											logger.debug("Successfully updated DB for download activation success "
													+ "Callback for subscriber: "+ strSubID);

											
											//RBT-13163: MP Non MP TEF/VF/OR Spain
											MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
											if (mpNonMpFeature != null) {
												logger.debug("MP Non MP feature enabled.");
												MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
												mpNonMpFeatureBean.setSubscriber(subscriber);
												mpNonMpFeatureBean.setChargeClass(classType);
												mpNonMpFeatureBean.setMode(download.selectedBy());
												mpNonMpFeature.checkAndActivateMusicPack(mpNonMpFeatureBean);
											}
											
											// RBT-10785: OI deployements
											String biUrl = RBTParametersUtils.getParamAsString(
													"DAEMON", "BI_URL_SEL_ACT", null);
											// replace msisdn, clipid, promoid, songname 
											callBI(strSubID, songName,
													promoId, clipId, biUrl);
											
											String subscriptionClass = subscriber.subscriptionClass();
											boolean isMigratedUser = migratedUserSubClassesList.contains(subscriptionClass);
											if(isMigratedUser && subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
												String migratedSubClass = migratedUserOldToNewSubClassMap.get(subscriptionClass);
												if (null != migratedSubClass) {

													boolean updated = m_rbtDBManager
															.convertSubscriptionType(
																	subscriber
																			.subID(),
																	subscriptionClass,
																	migratedSubClass,
																	null, 0,
																	false,
																	extraInfo,
																	subscriber);
													
													logger.debug("Upgraded subscriber. "
															+ "updated: "
															+ updated
															+ ", subscriberId: "
															+ strSubID
															+ ", from subscriptionClass: "
															+ subscriptionClass
															+ ", to subscriptionClass: "
															+ migratedSubClass);

												} else {
													logger.warn("No mapping subscriptionClass: "+subscriptionClass);
												}
											} else {
												logger.debug("Subscriber is"
														+ " not migrated user. "
														+ "subscriberId: "
														+ strSubID
														+ ", subscriptionYes: "
														+ subscriber.subYes());
											}
											
											String downloadMode = (null != download) ? download.selectedBy() : "";
											String downloadInfo = (null != download) ? download.selectionInfo() : "";
											boolean isCopyContestMode = copyContestModesList
													.contains(downloadMode);
											if(isCopyContestMode) {
												logger.info("Download mode is copy contest mode: "
														+ downloadMode
														+ ", downloadInfo: "
														+ downloadInfo);
												String contestCallerID = getMsisdnFromSelectionInfo(downloadInfo);
												if (null != contestCallerID) {
													logger.info("Selection info contains MSISDN: "
															+ contestCallerID);
													String whitelistedSubscriber = contestCallerID
															.concat("_WHITELISTED");
													boolean isWhitelisted = (null != memCachedClient
															.get(whitelistedSubscriber)) ? true
																	: false;
													logger.debug("Checking download for copy contest for "
															+ "subscriber: "
															+ strSubID
															+ ", isWhitelisted: "
															+ isWhitelisted
															+ ", isContestOnCopyInfluencerAllowed: "
															+ isContestOnCopyInfluencerAllowed);
													if (isContestOnCopyInfluencerAllowed
															&& isWhitelisted) {
														copyInfluencerContest(strSubID,
																contestCallerID);
													}
												} 
											}

											// Points for contest
											int pointsEarned = RBTContestUtils.getPointsEarned(
													"SEL", action, classType, amountCharged);
											if(pointsEarned > 0) {
												RBTContestUtils.pointsContest(strSubID, pointsEarned);
											}


											if (GrbtLogger.downloadsLogger != null
													&& getParamAsBoolean(
															"GRBT",
															"UPLOAD_DATA",
															"FALSE")
													&& getParamAsBoolean(
															"GRBT",
															"UPLOAD_DATA_PURCHASE",
															"FALSE"))
												GrbtLogger.downloadsLogger.info(strSubID + "," + clipId + "," + download.promoId() + "," + download.categoryID() + ","+ download.categoryType() + ","+ getChargeInfo(strSubID,clipId,download.categoryID()));
											// RBT Like feature starts
											String mode = download.selectedBy();
											String confMode = getParamAsString(
													"GATHERER",
													Constants.RBT_LIKE_MODE,
													"RBT_LIKE");
											logger.debug("Checking mode for like feature. "
													+ "download mode: "
													+ mode
													+ ", like mode: "
													+ confMode);
											if (confMode.equalsIgnoreCase(mode)) {
												checkAndSendSmsForLike(
														strSubID,
														callerID,
														subscriber,
														download.selectionInfo(),
														songName);
											}
											// RBT Like feature ends

											// Azaan for selections
											// int selectionCatType =
											// download.categoryType();
											// int selectionCatId =
											// download.categoryID();
											// logger.debug("Download category type: "
											// + selectionCatType
											// + ", category Id: "
											// + selectionCatId);
											// if (selectionCatType ==
											// iRBTConstant.SHUFFLE
											// &&
											// confAzaanCategoryIdList.contains(String
											// .valueOf(selectionCatId))) {
											// String subExtraInfo =
											// subscriber.extraInfo();
											// HashMap<String, String>
											// subExtraInfoMap = DBUtility
											// .getAttributeMapFromXML(subExtraInfo);
											// addAzaanSubType(strSubID,
											// subExtraInfoMap);
											// }
											
											
											//upgrade classtype to Download limit tMusic Pack classtype
											upgradeDownloadForDownloadLimitMusicPackOnDownloadCallback(download, subscriber);
										}

										if (isBISong
												&& status != null
												&& status
														.equalsIgnoreCase(SUCCESS)) {
											String biSongUrl = RBTParametersUtils
													.getParamAsString(
															COMMON,
															"BI_SONG_BLACKLIST_URL",
															null);
											if (biSongUrl != null) {
												biSongUrl = biSongUrl
														.replaceAll("%MSISDN%",
																strSubID);
												makeHttpRequest(biSongUrl);
											}
										}
										String toneType = getToneType(-1,
												download.categoryType());
										if (status != null
												&& status
														.equalsIgnoreCase("success"))
											SRBTUtility
													.updateSocialDownloadForSuccess(
															m_socialRBTAllowed,
															download,
															toneType,
															m_socialRBTAllUpdateInOneModel,
															strSubID, clipId,
															type, callerID,
															chargeClass,
															modeOfActivation,
															ret);

										writeEventLog(extraInfo, strSubID,
												download.selectedBy(), "0",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.selectedBy(),
												"0",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));

										if (status.equals("SUCCESS")
												&& !Utility
														.isShuffleCategory(download
																.categoryType()))
											addDownloadForMappedClip(strSubID,
													download, downloadCLip);

										if (status.equals("SUCCESS")) {
											String lotteryModes = RBTParametersUtils
													.getParamAsString(
															"COMMON",
															"LOTTERY_SUPPORTED_MODES",
															null);
											if (null != lotteryModes) {
												String[] lotteryModesAry = lotteryModes
														.split(",");
												for (String lotteryMode : lotteryModesAry) {
													if (lotteryMode
															.equalsIgnoreCase(modeOfActivation)) {
														m_rbtDBManager
																.insertRbtLotteryEntry(
																		-1,
																		strSubID,
																		new Date(),
																		null,
																		downloadCLip
																				.getClipId());
													}
												}
											} else {
												String chargeClassesStr = RBTParametersUtils
														.getParamAsString(
																"COMMON",
																"LOTTERY_SUPPORTED_CHARGE_CLASSES",
																null);
												if (chargeClassesStr != null) {
													Set<String> chargeClassesSet = new HashSet<String>(
															Arrays.asList(chargeClassesStr
																	.split(",")));
													if (chargeClassesStr
															.equalsIgnoreCase("ALL")
															|| chargeClassesSet
																	.contains(classType)) {
														String configStr = RBTParametersUtils
																.getParamAsString(
																		"COMMON",
																		"LOTTERY_CONTENT_CONFIG",
																		null);
														boolean isSupported = true;
														if (configStr != null) {
															String[] configs = configStr
																	.split(";");
															for (String eachConfig : configs) {
																String[] tokens = eachConfig
																		.split(":");
																Set<String> allowedSet = new HashSet<String>(
																		Arrays.asList(tokens[1]
																				.trim()
																				.split(",")));
																if (tokens[0]
																		.trim()
																		.equalsIgnoreCase(
																				"CP")) {
																	String cp = getCPForClip(downloadCLip);
																	isSupported = isSupported
																			&& allowedSet
																					.contains(cp);
																	if (!isSupported) {
																		if (logger
																				.isDebugEnabled())
																			logger.debug("CP not supported, so not inserting into lottery table : "
																					+ cp);
																		break;
																	}
																} else if (tokens[0]
																		.trim()
																		.equalsIgnoreCase(
																				"ARTIST")) {
																	String artist = downloadCLip
																			.getArtist();
																	isSupported = isSupported
																			&& allowedSet
																					.contains(artist);
																	if (!isSupported) {
																		if (logger
																				.isDebugEnabled())
																			logger.debug("Artist not supported, so not inserting into lottery table : "
																					+ artist);
																		break;
																	}
																}
															}
														}
														if (isSupported)
															m_rbtDBManager
																	.insertRbtLotteryEntry(
																			-1,
																			strSubID,
																			new Date(),
																			null,
																			downloadCLip
																					.getClipId());
													}

												}

											}
										}

										/*
										 * Added by SenthilRaja Change TNB Optin
										 * to Optout service
										 */
										if (status != null
												&& status
														.equalsIgnoreCase("success")) {
											chageTNBUserOptinModeToOptOut(
													action, null, download,
													subscriber);
											Clip clip = RBTCacheManager
													.getInstance()
													.getClipByRbtWavFileName(
															wav);
											if (clip != null) {
												hitBIForSongPurchase(
														subscriber.subID(),
														refID,
														download.selectedBy(),
														clip.getClipId() + "");
											}
										}
										if (status != null
												&& status
														.equalsIgnoreCase("success")) {
											String randomizeKeyword = getParamAsString(
													iRBTConstant.SMS,
													"RANDOMIZE_KEYWORD");
											if (randomizeKeyword != null) {
												String subExtraInfo = subscriber
														.extraInfo();
												HashMap<String, String> extraInfoMap = DBUtility
														.getAttributeMapFromXML(subExtraInfo);
												if (extraInfoMap == null
														|| (extraInfoMap != null && (!extraInfoMap
																.containsKey("UDS_OPTIN") || extraInfoMap
																.get("UDS_OPTIN")
																.equalsIgnoreCase(
																		"FALSE")))) {
													SubscriberDownloads subDownloads[] = m_rbtDBManager
															.getActiveSubscriberDownloads(strSubID);
													if (subDownloads != null
															&& subDownloads.length > 1) {
														String smsText = CacheManagerUtil
																.getSmsTextCacheManager()
																.getSmsText(
																		"SEND_SMS_FOR_RANDOMIZATION",
																		"SUCCESS",
																		subscriber
																				.language());
														if (smsText != null) {
															smsText = smsText
																	.replaceAll(
																			"%RANDOMIZE_KEYWORD%",
																			randomizeKeyword);
															Tools.sendSMS(
																	getSenderNumber(subscriber.circleID()),
																	strSubID,
																	smsText,
																	getParamAsBoolean(
																			"SEND_SMS_MASS_PUSH",
																			"FALSE"));
														}
													}
												}
											}
										}

										/*
										 * Winditaly press * copy feature
										 * RBT-6282
										 */
										Map<String, String> downloadMap = DBUtility
												.getAttributeMapFromXML(download
														.extraInfo());
										String freeCopySubClassAndChargeClass = RBTConnector
												.getInstance()
												.getRbtGenericCache()
												.getParameter(
														"COMMON",
														"FREE_COPY_SUB_CHARGE_CLASS",
														null);
										if (status != null
												&& status
														.equalsIgnoreCase("success")
												&& freeCopySubClassAndChargeClass != null
												&& downloadMap != null
												&& downloadMap
														.containsKey(iRBTConstant.FREE_COPY_AVAILED)) {
											logger.debug("PRESS * COPY FREE SELECTION ENABLED value of FREE_COPY_SUB_CHARGE_CLASS parameter: "
													+ freeCopySubClassAndChargeClass);
											String calledId = download
													.selectionInfo();
											if (calledId.indexOf("|CP:") != -1) {
												calledId = calledId
														.substring("|CP:"
																.length());
												if (calledId.indexOf("-") != -1) {
													calledId = calledId
															.substring(calledId
																	.indexOf("-")
																	+ "-".length());
												}
												if (calledId.indexOf(":CP|") != -1) {
													calledId = calledId
															.substring(
																	0,
																	calledId.indexOf(":CP|"));
												}

											}
											SubscriberDownloads[] downloads = m_rbtDBManager
													.getActiveSubscriberDownloads(calledId);
											if (downloads != null) {
												for (SubscriberDownloads tempDownload : downloads) {
													if (!tempDownload
															.promoId()
															.equalsIgnoreCase(
																	download.promoId())) {
														continue;
													}
													String downloadExtraInfo = tempDownload
															.extraInfo();
													downloadMap = DBUtility
															.getAttributeMapFromXML(downloadExtraInfo);
													if (downloadMap != null
															&& downloadMap
																	.containsKey(iRBTConstant.COPY_CONTENT_VALIDITY_AVAILED)) {
														logger.info("Download "
																+ tempDownload
																		.subscriberId()
																+ "-"
																+ tempDownload
																		.promoId()
																+ " already got validity extension. Ignoring this time");
														break;
													}
													// Call content validity url
													Map<String, Object> taskMap = new HashMap<String, Object>();
													taskMap.put(
															WebServiceConstants.param_subscriberID,
															calledId);
													taskMap.put(
															WebServiceConstants.param_preCharged,
															"y");
													taskMap.put(
															WebServiceConstants.param_refID,
															tempDownload
																	.refID());
													taskMap.put(
															WebServiceConstants.param_subscriberDownloads,
															tempDownload);
													WebServiceContext task = new WebServiceContext(
															taskMap);
													String response = Utility
															.sendRenewalRequestToSubMgr(task);
													if (response
															.equalsIgnoreCase("SUCCESS")) {
														if (downloadMap == null)
															downloadMap = new HashMap<String, String>();
														downloadMap
																.put(iRBTConstant.COPY_CONTENT_VALIDITY_AVAILED,
																		"TRUE");
														downloadExtraInfo = DBUtility
																.getAttributeXMLFromMap(downloadMap);
														m_rbtDBManager
																.updateDownloads(
																		calledId,
																		tempDownload
																				.refID(),
																		tempDownload
																				.downloadStatus(),
																		downloadExtraInfo,
																		tempDownload
																				.classType());
													}
													break;
												}
											}
										}

										if (m_strActionActivation.equalsIgnoreCase(action)
												&& m_statusSuccess.equalsIgnoreCase(status)) {
											String downloadImageUrl  = CacheManagerUtil
													.getParametersCacheManager()
													.getParameterValue("DEAMON",
															"DOWNLOAD_SONG_IMAGE_URL",null);
											//if the song is downloaded from the configured mode
											logger.info("configured download image url is :" + downloadImageUrl);
											String mode = download.selectedBy();
											String confMode = getParamAsString("DAEMON", "DOWNLOAD_SONG_IMAGE_URL_MODE","");
											//msisdn=<msisdn>&toneId=<toneId>&promoId=<promoId>&songName=<songName>&mode=<mode>
											logger.info("mode is :" + mode +" confMode :" + confMode);
											List<String> confModeList =  Arrays.asList(confMode.split(","));
										if (clipID > 0 && mode != null && confModeList.contains(mode)) {
											Clip clip = m_rbtCacheManager.getClip(clipID);
											downloadImageUrl = downloadImageUrl.replaceAll("<msisdn>", strSubID);
											downloadImageUrl = downloadImageUrl.replaceAll("<toneId>", String.valueOf(clipID));
											downloadImageUrl = downloadImageUrl.replaceAll("<promoId>", clip.getClipPromoId());
											downloadImageUrl = downloadImageUrl.replaceAll("<songName>", songName);
											downloadImageUrl = downloadImageUrl.replaceAll("<mode>", mode);
											
											if(downloadImageUrl != null){
												logger.info("download image url after setting the params : "
														+ downloadImageUrl);
												
												HttpParameters httpParameters = new HttpParameters(downloadImageUrl);	
												RBTHttpClient rbtHttpClient = new RBTHttpClient(httpParameters);
												HashMap<String, String> contestRequestParams = new HashMap<String, String>();
												rbtHttpClient.makeRequestByGet(downloadImageUrl,
														contestRequestParams);
												// transfer request to other circle.
											}
										  }
										
										//RBT-12585 RBT Bundling Feature with other services
										activateFreeAzaanForContent(subscriber, downloadCLip, mode);
										
										}

									}
								} else if (m_strUpgradeSubscription
										.equalsIgnoreCase(action)) {				//Download Upgrade success/failure callback
									logger.debug("Processing download upgradation");
									HashMap<String, String> extraInfoMap = DBUtility
											.getAttributeMapFromXML(download
													.extraInfo());
									String oldSub = null;
									if (extraInfoMap != null) {
										oldSub = extraInfoMap
												.get("OLD_CLASS_TYPE");
									}
									logger.info("old sub class is " + oldSub);
									if (downloadStat == STATE_DOWNLOAD_ACTIVATED && oldSub != null && classType != null && oldSub.equals(classType))		
										return CALLBACK_ALREADY_RECEIVED;
									else if (downloadStat == STATE_DOWNLOAD_ACTIVATED || downloadStat == STATE_DOWNLOAD_TO_BE_ACTIVATED)
										return SELECTION_ACTIVE;
									else if ((downloadStat == STATE_DOWNLOAD_ACTIVATION_PENDING || downloadStat == STATE_DOWNLOAD_ACT_ERROR)
											&& oldSub == null)
										return SELECTION_ACT_PENDING;
									else if (downloadStat == STATE_DOWNLOAD_DEACTIVATION_PENDING
											|| downloadStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED
											|| downloadStat == STATE_DOWNLOAD_DEACT_ERROR)
										return SELECTION_DCT_PENDING;
									else if (downloadStat == STATE_DOWNLOAD_DEACTIVATED)
										return SELECTION_DEACTIVE;

									boolean biSong = false;
									if (extraInfoMap != null) {
										biSong= extraInfoMap.containsKey("BI_OFFER") ? extraInfoMap
												.get("BI_OFFER").equalsIgnoreCase(
														"TRUE") : false;
									}
									ret = m_rbtDBManager
											.smUpdateDownloadUpgradationCallback(
													strSubID, wav, refID,
													status, contentType,
													classType, extraInfoMap,strNextBillingDate);
									// IF EXTRA_INFO CONTAINS BI_OFFER,HIT
									// BLACKLIST URL
									if (biSong && ret.indexOf("SUCCESS") != -1
											&& status != null
											&& status.equalsIgnoreCase(SUCCESS)) {
										String downloadBlackListUrl = RBTParametersUtils
												.getParamAsString(
														"COMMON",
														"DOWNLOAD_BI_BLACKLIST_URL",
														null);
										if (downloadBlackListUrl != null) {
											downloadBlackListUrl = downloadBlackListUrl
													.replaceAll("%MSISDN%",
															strSubID);
											makeHttpRequest(downloadBlackListUrl);
										}

									}
									
									if(ret.indexOf("SUCCESS") != -1	&& status != null && status.equalsIgnoreCase(SUCCESS)) {
										//upgrade classtype to Download limit tMusic Pack classtype
										upgradeDownloadForDownloadLimitMusicPackOnDownloadCallback(download, subscriber);
										
										//RBT-13163: MP Non MP TEF/VF/OR Spain 
										MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
										if (mpNonMpFeature != null) {
											logger.debug("MP Non MP feature enabled.");
											MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
											mpNonMpFeatureBean.setSubscriber(subscriber);
											mpNonMpFeatureBean.setDownloadsModel(true);
											mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
										}
									}

									logger.debug("Processed download upgradation"
											+ ". Response: " + ret);
									return ret;
								} else if (action
										.equals(m_strActionDeactivation)) {
									//Download De-activation success/failure callback
									if (downloadStat == STATE_DOWNLOAD_DEACTIVATED) {
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(),
												"203", "Purchase",
												downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"403",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));
										return SELECTION_ALREADY_DEACTIVE;
									}

									// download deactivation success/failure
									ret = m_rbtDBManager
											.smUpdateDownloadDeactivationCallback(
													strSubID, wav, refID,
													status, noDownloadDeactSub,
													type);
									logger.info("Updated DB for download deactivation callback. "
											+ ", strSubID: "
											+ strSubID
											+ ", refID: "
											+ refID
											+ ", status: "
											+ status
											+ ", update status: " + ret);

									if (ret.indexOf(m_success) != -1) {

										logger.debug("Successfully updated download status for"
												+ " download activation. subscriber: "
												+ strSubID);
										
										if (m_statusSuccess
												.equalsIgnoreCase(status)) {

											//RBT-13163: MP Non MP TEF/VF/OR Spain 
											MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
											if (mpNonMpFeature != null) {
												logger.debug("MP Non MP feature enabled.");
												MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
												mpNonMpFeatureBean.setSubscriber(subscriber);
												mpNonMpFeatureBean.setDownloadsModel(true);
												mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
											}
											// int selectionCatType = download
											// .categoryType();
											// int selectionCatId = download
											// .categoryID();
											// // Azaan for selections
											// if (selectionCatType ==
											// iRBTConstant.SHUFFLE
											// && confAzaanCategoryIdList
											// .contains(String
											// .valueOf(selectionCatId))) {
											// String subExtraInfo = subscriber
											// .extraInfo();
											// HashMap<String, String>
											// subextraInfoMap = DBUtility
											// .getAttributeMapFromXML(subExtraInfo);
											// removeAzaanSubType(strSubID,
											// subextraInfoMap);
											// }
										}

										changeTNBUserToNormalUser(clipId,
												strSubID);

										String toneType = getToneType(-1,
												download.categoryType());
										SRBTUtility
												.updateSocialDownloadForDeactivation(
														m_socialRBTAllowed,
														download,
														toneType,
														strSubID,
														m_socialRBTAllUpdateInOneModel,
														clipId, type, callerID,
														chargeClass, ret);
										writeEventLog(extraInfo, strSubID,
												download.deactivatedBy(), "0",
												"Purchase", downloadCLip, null);
										writeEventLog(
												extraInfo,
												strSubID,
												download.deactivatedBy(),
												"0",
												"Customization",
												downloadCLip,
												getCriteria(songStatus,
														callerID));

									}
								} else if (action
										.equals(m_strSuspendSubscription)) {
									if (downloadStat != STATE_DOWNLOAD_ACTIVATED
											&& downloadStat != STATE_DOWNLOAD_ACT_ERROR) {
										if (downloadStat == STATE_DOWNLOAD_ACTIVATION_PENDING
												|| downloadStat == STATE_DOWNLOAD_TO_BE_ACTIVATED
												|| downloadStat == STATE_DOWNLOAD_GRACE) {
											logger.warn(COULD_NOT_PROCESS
													+ " Download "
													+ " is in pending. refID: "
													+ refID + ", subcriberId: "
													+ strSubID + ", status: "
													+ downloadStat);
											return SELECTION_ACT_PENDING;
										} else if (downloadStat == STATE_DOWNLOAD_SUSPENSION) {
											logger.warn(COULD_NOT_PROCESS
													+ "Download "
													+ " is already suspended. refID: "
													+ refID + ", subcriberId: "
													+ strSubID + ", status: "
													+ downloadStat);
											return SELECTION_ALREADY_SUSPENDED;
										} else {
											logger.warn(COULD_NOT_PROCESS
													+ "Download "
													+ "is deactive. refID: "
													+ refID + ", subcriberId: "
													+ strSubID + ", status: "
													+ downloadStat);
											return SELECTION_DEACTIVE;
										}
									}
									extraInfo = download.extraInfo();
									if (extraInfo != null
											&& extraInfo.contains(VOLUNTARY)) // If
																				// we
																				// get
																				// two
																				// successive
																				// requests
																				// returning
																				// success
										return m_SUCCESS;
									if (reasonCode != null
											&& reasonCode.contains("710")) // User
																			// Initiated
																			// suspension
									{
										extraInfo = DBUtility.setXMLAttribute(
												extraInfo, VOLUNTARY, "TRUE");
									}
									if (getParamAsString(COMMON, SUSPEND_INTRO_PRE_PROMPT_FLAG_IN_CALLBACK,
													"FALSE").equalsIgnoreCase("TRUE")) {
										// VDE-2730 Start : Remove Suspended
										// pre-prompt flag
										boolean updateUserInfo = false;
										if (subscriber != null) {
											Map<String, String> extraInfoMap = DBUtility
													.getAttributeMapFromXML(subscriber.extraInfo());
											if (extraInfoMap == null) {
												extraInfoMap = new HashMap<String, String>();
												updateUserInfo = true;
											} else if (null == extraInfoMap
													.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
													|| (null != extraInfoMap
															.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
															&& !extraInfoMap
																	.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
																	.equalsIgnoreCase(
																			iRBTConstant.ENABLE_PRESS_STAR_INTRO_SUSPEND))) {
												updateUserInfo = true;
											}
											if (updateUserInfo) {
												extraInfoMap.put(iRBTConstant.EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG,
														iRBTConstant.ENABLE_PRESS_STAR_INTRO_SUSPEND);
												String extrInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
												m_rbtDBManager.updateExtraInfoAndPlayerStatus(subscriber.subID(),
														extrInfo, "A");
											}
										}
										// VDE-2730 End : Remove Suspended
										// pre-prompt flag
									}
									ret = m_rbtDBManager
											.smUpdateDownloadSuspensionCallback(
													strSubID, wav, refID,
													status, type, extraInfo);
									logger.info("Successfully updated DB for"
											+ " download suspension."
											+ " refID: " + refID
											+ ", subscriberId: " + strSubID
											+ ", db update status: " + ret);
									if (ret.equalsIgnoreCase(m_success)) {
										m_rbtDBManager
												.updateSettingsForDownloadCallback(
														strSubID, wav, true);

										// int selectionCatType =
										// download.categoryType();
										// int selectionCatId =
										// download.categoryID();
										// logger.debug("Successfully updated download status for"
										// +
										// " download suspension. subscriber: "
										// + strSubID + ", selection refId: " +
										// refID
										// + ", categoryType: " +
										// selectionCatType
										// + ", categoryId: " + selectionCatId);
										// // Azaan for selections
										// if (selectionCatType ==
										// iRBTConstant.SHUFFLE
										// &&
										// confAzaanCategoryIdList.contains(String
										// .valueOf(selectionCatId))) {
										// String subExtraInfo =
										// subscriber.extraInfo();
										// HashMap<String, String>
										// subextraInfoMap = DBUtility
										// .getAttributeMapFromXML(subExtraInfo);
										// removeAzaanSubType(strSubID,
										// subextraInfoMap);
										// }

										return SUCCESS;
									}
								} else if (action
										.equals(m_strResumeSubscription)) {
									extraInfo = download.extraInfo();
									if (extraInfo != null
											&& !extraInfo.contains(VOLUNTARY))
										return INVALID
												+ "|"
												+ "SELECTION IS NOT VOLUNTARILY SUSPENDED";

									if ("710".equals(reasonCode)) // User
																	// Initiated
																	// suspension
									{
										extraInfo = DBUtility
												.removeXMLAttribute(extraInfo,
														VOLUNTARY);
									} else {
										return m_INVALID
												+ "|SELECTION IS NOT VOLUNTARILY SUSPENDED";
									}
									ret = m_rbtDBManager
											.smUpdateDownloadResumptionCallback(
													strSubID, wav, refID,
													status, type, extraInfo);
									logger.debug("Processed download resumption "
											+ "callback. Response: " + ret);

									if (ret.equalsIgnoreCase(m_success)) {
										m_rbtDBManager
												.updateSettingsForDownloadCallback(
														strSubID, wav, false);
										return SUCCESS;
									}
								} else if (action.equals(m_strActionRental)) {
									// download renewal success/failure
									// RBT-12906 - Resubscription callback.
									String mode="NEF";
									if (null == subscriber
											|| subscriber.subYes().equalsIgnoreCase("X")) {
										mode = sys_mode;
									}
									
									ret = m_rbtDBManager
											.smUpdateDownloadRenewalCallback(
													strSubID, wav, refID,
													status, noDownloadDeactSub,
													type, classType, mode);
									logger.debug("Processed download renewal "
											+ "callback. status: " + status
											+ ", Response: " + ret);

									if (ret.equalsIgnoreCase(m_success)) {

										logger.debug("Successfully updated db for"
												+ " renewal success"
												+ " callback for subscriberId: "
												+ strSubID
												+ ", for download status: "
												+ downloadStat);
										
										//RBT-13163: MP Non MP TEF/VF/OR Spain 
										MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
										if (mpNonMpFeature != null) {
											logger.debug("MP Non MP feature enabled.");
											MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
											mpNonMpFeatureBean.setSubscriber(subscriber);
											mpNonMpFeatureBean.setDownloadsModel(true);
											mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
										}
										
										if (m_statusSuccess
												.equalsIgnoreCase(status)) {
											
											// RBT-10785: OI deployements starts
											String biUrl = RBTParametersUtils.getParamAsString(
													"DAEMON", "BI_URL_SEL_REN", null);
											
											callBI(strSubID, songName,
													promoId, clipId, biUrl);
											// RBT-10785: OI deployements ends
											
											// Points for contest
											int pointsEarned = RBTContestUtils
													.getPointsEarned("SEL",
															action, classType, amountCharged);
											if (pointsEarned > 0) {
												RBTContestUtils.pointsContest(
														strSubID, pointsEarned);
											}
										}

										if (!m_statusSuccess
												.equalsIgnoreCase(status)
												&& (SUSPENDED == downloadStat || SUSPENDED_INIT == downloadStat)) {

											// Azaan for selections
											// int downloadCatType = download
											// .categoryType();
											// int downloadCatId = download
											// .categoryID();
											// if (downloadCatType ==
											// iRBTConstant.SHUFFLE
											// && confAzaanCategoryIdList
											// .contains(String
											// .valueOf(downloadCatId))) {
											// String subExtraInfo = subscriber
											// .extraInfo();
											// HashMap<String, String>
											// subExtraInfoMap = DBUtility
											// .getAttributeMapFromXML(subExtraInfo);
											// addAzaanSubType(strSubID,
											// subExtraInfoMap);
											// }
										}
									}

									changeTNBUserToNormalUser(clipId, strSubID);
								}
							}

							conn = m_rbtDBManager.getConnection();
							if (conn == null)
								return FAILURE;
							// Subscriber subscriber = m_rbtDBManager
							// .getSubscriberForSMCallbacks(conn, strSubID);
							ChargeSMS chargeSms = null;
							if (subscriber != null) {
								chargeSms = CacheManagerUtil
										.getChargeSMSCacheManager()
										.getChargeSMS(classType, "SEL",
												subscriber.language());
							} else {
								chargeSms = CacheManagerUtil
										.getChargeSMSCacheManager()
										.getChargeSMS(classType, "SEL");
							}
							String callerNo = m_smsTextForAll;
							String createTime = null;

							if (ret.indexOf(m_success) != -1) {

								chargeClass = CacheManagerUtil
										.getChargeClassCacheManager()
										.getChargeClass(classType);

								if (getParamAsBoolean("SEND_SMS_ON_CHARGE",
										"FALSE")
										&& chargeClass != null
										&& !action
												.equals(m_strActionDeactivation)) {
									if (chargeSms == null) {
										String sms = chargeClass
												.getSmschargeSuccess();
										if (action
												.equals(m_strActionActivation)
												&& !status.equals("SUCCESS"))
											sms = chargeClass
													.getSmschargeFailure();
										else if (action
												.equals(m_strActionRental)
												&& !status.equals("SUCCESS"))
											sms = chargeClass
													.getSmsrenewalFailure();
										else if (action
												.equals(m_strActionRental)
												&& status.equals("SUCCESS"))
											sms = chargeClass
													.getSmsrenewalSuccess();

										if (sms != null
												&& !sms.equalsIgnoreCase("null"))
											sendSelectionSMS(strSubID, sms,
													createTime, songName,
													callerNo, null, null,
													reason, movieName);
									} else {
										String sms = chargeSms
												.getPostpaidSuccess();
										if (action
												.equals(m_strActionActivation)
												&& status.equals("SUCCESS")
												&& type != null
												&& type.equalsIgnoreCase("p"))
											sms = chargeSms.getPrepaidSuccess();
										else if (action
												.equals(m_strActionActivation)
												&& !status.equals("SUCCESS")
												&& type != null
												&& type.equalsIgnoreCase("p"))
											sms = chargeSms.getPrepaidFailure();
										else if (action
												.equals(m_strActionActivation)
												&& !status.equals("SUCCESS")
												&& type != null
												&& !type.equalsIgnoreCase("p"))
											sms = chargeSms
													.getPostpaidFailure();
										else if (action
												.equals(m_strActionRental)
												&& !status.equals("SUCCESS")
												&& type != null
												&& type.equalsIgnoreCase("p"))
											sms = chargeSms
													.getPrepaidRenewalFailure();
										else if (action
												.equals(m_strActionRental)
												&& !status.equals("SUCCESS")
												&& type != null
												&& !type.equalsIgnoreCase("p"))
											sms = chargeSms
													.getPostpaidRenewalFailure();
										else if (action
												.equals(m_strActionRental)
												&& status.equals("SUCCESS")
												&& type != null
												&& type.equalsIgnoreCase("p"))
											sms = chargeSms
													.getPrepaidRenewalSuccess();
										else if (action
												.equals(m_strActionRental)
												&& status.equals("SUCCESS")
												&& type != null
												&& !type.equalsIgnoreCase("p"))
											sms = chargeSms
													.getPostpaidRenewalSuccess();

										if (type != null
												&& type.equalsIgnoreCase("p"))
											sendSelectionSMS(strSubID, sms,
													createTime, songName,
													callerNo, null, null,
													reason, movieName);
										else
											sendSelectionSMS(strSubID, sms,
													createTime, songName,
													callerNo, null, null,
													reason, movieName);
									}
								}

								logger.info("canProcessClipStatus: "
										+ canProcessClipStatus);
								if (canProcessClipStatus) {
									checkIfClipExistsAndUpdate(
											download.subscriberId(),
											download.promoId(),
											download.categoryType(),
											download.categoryID());
								}

								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "0", "Purchase",
										downloadCLip, null);
								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "0",
										"Customization", downloadCLip,
										getCriteria(songStatus, callerID));
								return SUCCESS;

							} else if (ret.equalsIgnoreCase(m_failure)) {
								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "201",
										"Purchase", downloadCLip, null);
								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "404",
										"Customization", downloadCLip,
										getCriteria(songStatus, callerID));
								return INVALID;
							} else {
								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "201",
										"Purchase", downloadCLip, null);
								writeEventLog(extraInfo, strSubID,
										download.selectedBy(), "404",
										"Customization", downloadCLip,
										getCriteria(songStatus, callerID));
								return FAILURE;
							}
						}
					}
				}
				StringTokenizer stk = new StringTokenizer(refID, ":");
				String tmp;
				if (stk.hasMoreTokens()) {
					tmp = stk.nextToken().trim();
					if (!tmp.equalsIgnoreCase("null")
							&& !tmp.equalsIgnoreCase("all"))
						callerID = tmp;
				}

				if (stk.hasMoreTokens()) {
					tmp = stk.nextToken().trim();
					try {
						songStatus = Integer.parseInt(tmp);
						if (songStatus >= 0)
							validRefID = true;
					} catch (Exception e) {

					}
				}

				if (validRefID && stk.hasMoreTokens()) {
					setTime = stk.nextToken().trim();
					if (setTime == null)
						validRefID = false;
				}

				if (songStatus == 80 && validRefID && stk.hasMoreTokens()) {
					tmp = stk.nextToken().trim();
					try {
						fromTime = Integer.parseInt(tmp);
					} catch (Exception e) {
						validRefID = false;
					}
				}

				if (songStatus == 80 && validRefID && stk.hasMoreTokens()) {
					tmp = stk.nextToken().trim();
					try {
						toTime = Integer.parseInt(tmp);
					} catch (Exception e) {
						validRefID = false;
					}
				}

				if (validRefID && stk.hasMoreTokens()) {
					tmp = stk.nextToken().trim();
					try {
						ClipID = Integer.parseInt(tmp);
					} catch (Exception e) {
						ClipID = -1;
					}
				}

			}

			Clip tempClip = m_rbtCacheManager.getClip(ClipID);
			String wavFile = null;
			if (ClipID > 0) {
				Clip m = m_rbtCacheManager.getClip(ClipID);
				if (m != null)
					wavFile = m.getClipRbtWavFile();
			}
			if (status == null || action == null || strSubID == null
					|| type == null) {
				logger.info("RBT::Returning false as status " + status
						+ " action " + action + " strSubID " + strSubID
						+ " refID " + refID + " type " + type);
				writeEventLog(extraInfo, strSubID, null, "402",
						"Customization", tempClip, null);
				writeEventLog(extraInfo, strSubID, null, "404",
						"Customization", tempClip,
						getCriteria(songStatus, callerID));
				return FAILURE;
			}

			logger.info("ClipID: " + ClipID + ", wavFile: " + wavFile);

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(
					conn, strSubID);
			if (classType != null) {
				String language = (subscriber != null ? subscriber.language()
						: null);
				chargeClass = m_rbtChargeClassCacheManager
						.getChargeClassByLanguage(classType, language);

				if (chargeClass != null)
					classType = chargeClass.getChargeClass();
				else {
					logger.warn("Unable to process. chargeClass: "
							+ chargeClass + ", is not configured.");
					writeEventLog(extraInfo, strSubID, null, "404",
							"Customization", tempClip,
							getCriteria(songStatus, callerID));
					return FAILURE;
				}
				logger.info("chargeClass: " + chargeClass);
			}

			conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			subscriberStatus = m_rbtDBManager
					.getRefIDSelection(conn, strSubID, refID);

			//RBT-14138: TEF ES - Premium channel renewal flow correction
			if (action != null && action.equals(m_strActionRental)
					&& RBTDBManager.catTypesForAutoRenewal != null) {
				SubscriberDownloads download =  m_rbtDBManager.getSubscriberDownloadByRefID(strSubID, refID);
				if (download != null && RBTDBManager.catTypesForAutoRenewal.contains(String.valueOf(download.categoryType()))) {
					if (download.downloadStatus() == STATE_DOWNLOAD_SEL_TRACK) {
						//Download active
						if (m_statusSuccess.equalsIgnoreCase(status)) {
							m_rbtDBManager.updateDownloadClassType(strSubID,refID, classType);
							m_rbtDBManager
									.updateSettingsForDownloadRenewalSuccessCallback(
											strSubID, null,
											download.categoryID(),
											download.categoryType());
							finalResponse = SUCCESS;
							return SUCCESS;
						} else {
							String deactBy = "AF";
							
							if (sys_mode != null
									&& !sys_mode.equals(SYSTEM_MODE)
									&& m_strActionRental.equalsIgnoreCase(action)
									&& m_statusFailure.equalsIgnoreCase(status)
									&& (smInitiatedDeactAllowedModes.contains(sys_mode)
											||  null == subscriber || subscriber.subYes().equalsIgnoreCase("X")))
								deactBy = sys_mode;
							else {
								if (getParamAsBoolean("NEF_DEACTIVATIONS", "FALSE")
										&& (type != null && type.trim()
												.equalsIgnoreCase("P")))
									deactBy = "NEF";
								else
									deactBy = "RF";
							}
							m_rbtDBManager.deactSelectionsAndDeleteDownloadForRenewalFlow(null, deactBy, download);
							finalResponse = SUCCESS;
							return SUCCESS;
						}
					} else {
						if (m_statusSuccess.equalsIgnoreCase(status)) {
							return SELECTION_ALREADY_DEACTIVE;
						} else {
							if (download.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED) {
								return INVALID;
							}
						}
					}
				}
			}
			
			if (subscriberStatus == null && validRefID) {
				conn = m_rbtDBManager.getConnection();
				if (conn == null)
					return FAILURE;

				subscriberStatus = m_rbtDBManager.getRefIDSelectionOldLogic(
						conn, strSubID, callerID, songStatus, setTime,
						fromTime, toTime, wavFile);

				logger.info("Trying to get selection using old logic."
						+ " status: " + status + ", action " + action
						+ ", strSubID: " + strSubID + ", refID: " + refID
						+ ", type: " + type);

				if (subscriberStatus == null) {
					logger.info("No selection found using old refID RBT");
					writeEventLog(extraInfo, strSubID, null, "404",
							"Customization", tempClip,
							getCriteria(songStatus, callerID));
					return SELECTION_REFID_NOT_EXISTS;
				} else {
					conn = m_rbtDBManager.getConnection();
					if (conn == null)
						return FAILURE;

					m_rbtDBManager.updateRefIDSelectionOldLogic(conn, strSubID,
							callerID, songStatus, setTime, fromTime, toTime,
							wavFile, refID);
					logger.info("Successfully updated selection refID");
				}
			}
			if (subscriberStatus == null) {
				logger.info("No selection found for refID: " + refID);
				writeEventLog(extraInfo, strSubID, null, "404",
						"Customization", tempClip,
						getCriteria(songStatus, callerID));

				return SELECTION_REFID_NOT_EXISTS;
			}
			if (!validRefID && subscriberStatus != null) {
				callerID = subscriberStatus.callerID();
				songStatus = subscriberStatus.status();
				setTime = getFormattedDate(sdf, subscriberStatus.setTime());
				fromTime = subscriberStatus.fromTime();
				toTime = subscriberStatus.toTime();
				wavFile = subscriberStatus.subscriberFile();
			}
			Clip clip = null;
			if (subscriberStatus.subscriberFile() != null
					&& subscriberStatus.subscriberFile().indexOf("rbt_slice_") != -1) {
				String str[] = subscriberStatus.subscriberFile().split("rbt_slice_");
				String clipId = str[1].substring(0, str[1].indexOf("_"));
				clip = m_rbtCacheManager.getClip(clipId);
			} else {
				clip = m_rbtCacheManager.getClipByRbtWavFileName(subscriberStatus.subscriberFile());
			}
			String clipId = null;
			int iClipId = -1;
			String clipPromoId = null;
			if (clip != null) {
				clipId = "" + clip.getClipId();
				clipPromoId = clip.getClipPromoId();
				iClipId = clip.getClipId();
			}
			char oldLoopStatus = LOOP_STATUS_ERROR;
			String createTime = null;
			String songName = null;
			String movieName = "N/A";
			String callerNo = m_smsTextForAll;
			String start = null;
			String end = null;
			String selectionInfo = null;
			String modeOfActivation = null;
			Clip songclip = null;
			int catType = -1;
			int catID = -1;
			String selBy = null;
			boolean isPrepaid = true;
			int rbtType = 0;
			if (subscriberStatus != null) {
				extraInfo = subscriberStatus.extraInfo();
				selStatus = subscriberStatus.selStatus();
				modeOfActivation = subscriberStatus.selectedBy();
				selectionInfo = subscriberStatus.selectionInfo();
				if (selectionInfo != null
						&& selectionInfo.indexOf("BULK:") != -1)
					createTime = getCreateTime();
				oldLoopStatus = subscriberStatus.loopStatus();
				if (getParamAsBoolean("COMMON", "IS_RRBT_ON", "FALSE"))
					rbtType = subscriberStatus.selType();
				if (subscriberStatus.callerID() != null)
					callerNo = subscriberStatus.callerID();
				if (subscriberStatus.categoryID() == 104)
					songName = "Record My Own";
				//RBT-12192	SMS not going for override shuffle
				else if (subscriberStatus.categoryType() == SHUFFLE
						|| subscriberStatus.categoryType() == 9
						|| Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES", "10")
										.split(",")).contains(subscriberStatus.categoryType()+"")) {
					try {
						
						Category category = m_rbtCacheManager
								.getCategory(subscriberStatus.categoryID());
						if (category != null)
							songName = category.getCategoryName();
						//RBT-12192	SMS not going for override shuffle
						if (Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES", "10")
								.split(",")).contains(subscriberStatus.categoryType()+"")) {
							start = getFormattedDate(m_timeSdf,
									category.getCategoryStartTime());
							end = getFormattedDate(m_timeSdf,
									category.getCategoryEndTime());
						}
					} catch (Throwable e) {
					}
				} else if (subscriberStatus.subscriberFile() != null) {
					songclip = getClipRBT(subscriberStatus.subscriberFile(),
							subscriberStatus.status());
					if (songclip != null) {
						songName = songclip.getClipName();
						movieName = songclip.getAlbum();
					}
					songName = (songName == null ? "N/A" : songName);
					movieName = (movieName == null ? "N/A" : movieName);

					if (songName.equals("N/A")
							&& subscriberStatus.status() == 99)
						songName = "RecordMyOwn Profile";

				}

				selBy = subscriberStatus.selectedBy();
				catType = subscriberStatus.categoryType();
				catID = subscriberStatus.categoryID();
				isPrepaid = subscriberStatus.prepaidYes();
				/*// RBT-14301: Uninor MNP changes.
				if (subscriber != null) {
					String circleId = subscriberStatus.circleId();
					if (circleIDFromPrism != null && circleId != null
							&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
						smUpdateCircleIdForSubscriber(false,
								subscriber.subID(), circleIDFromPrism,
								subscriberStatus.refID(), null);
					}
				}*/
			}

			//SELECTION De-activation success callback
			if (m_strActionDeactivation.equalsIgnoreCase(action)
					&& m_statusSuccess.equalsIgnoreCase(status)) {
				logger.info("Deactivation request received. SubscriberId: "
						+ strSubID + ", status: " + status + ", refId: "
						+ refID + ", selection status: " + selStatus);

				if (selStatus == null) {
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.selectedBy(), "404",
							"Customization", tempClip,
							getCriteria(songStatus, callerID, subscriberStatus));
					logger.warn(COULD_NOT_PROCESS
							+ " Selection status is null." + " SubscriberId: "
							+ strSubID + ", refId: " + refID
							+ ", selection status: " + selStatus);
					return SELECTION_NOT_EXISTS;
				} else if (!selStatus.equals("D") && !selStatus.equals("P")
						&& !selStatus.equals("F")) {
					if (selStatus.equalsIgnoreCase("X")) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						logger.warn(COULD_NOT_PROCESS
								+ " Selection status is already deactive."
								+ " SubscriberId: " + strSubID + ", refId: "
								+ refID + ", selection status: " + selStatus);
						return SELECTION_ALREADY_DEACTIVE;
					} else if(m_actPendingStatus.contains(selStatus)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						logger.warn(COULD_NOT_PROCESS
								+ " Selection status is in activation pending state."
								+ " SubscriberId: " + strSubID + ", refId: "
								+ refID + ", selection status: " + selStatus);
						return SELECTION_ACT_PENDING;
					} else if (m_actStatus.contains(selStatus)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						logger.warn(COULD_NOT_PROCESS
								+ " Selection status is active."
								+ " SubscriberId: " + strSubID + ", refId: "
								+ refID + ", selection status: " + selStatus);
						return INVALID + "|" + "SELECTION ACTIVE";
					}
				}

				circleIDFromPrism = isCircleIdMatchedWithSub(circleIDFromPrism,
						subscriberStatus);
				// Selection deactivation success
				String ret = smSelectionDectivationSuccess(strSubID, refID,
						oldLoopStatus, rbtType, clip, circleIDFromPrism);
				if (ret == null
						|| (!ret.equalsIgnoreCase(m_success) && !ret
								.equalsIgnoreCase(m_failure))) {
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.deSelectedBy(), "402",
							"Customization", clip, null);
					return FAILURE;
				}

				if (ret.equalsIgnoreCase(m_success)) {//RBT-14671 - # like
					if (hashLikeKeys != null && !hashLikeKeys.isEmpty()) {
						try {
							m_rbtDBManager.deleteSubscriberLikedSong(strSubID,
									(!Utility.isShuffleCategory(subscriberStatus
											.categoryType()) ? clip.getClipId()
											: -1), subscriberStatus
											.categoryID());
						} catch (Exception e) {
							logger.info("Table is not there in the data base for like: "
									+ e.getMessage());
						}
					}
					logger.debug("Successfully updated selection status for"
							+ " selection deactivation success. subscriber: "
							+ strSubID + ", selection refId: " + refID);
					
					if (!isAddToDownloads) {
						//RBT-13163: MP Non MP TEF/VF/OR Spain
						MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
						if (mpNonMpFeature != null) {
							logger.debug("MP Non MP feature enabled.");
							MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
							mpNonMpFeatureBean.setSubscriber(subscriber);
							mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
						}
						int selectionCatType = subscriberStatus.categoryType();
						int selectionCatId = subscriberStatus.categoryID();

						logger.debug(" categoryType: " + selectionCatType
								+ ", categoryId: " + selectionCatId);
						// Azaan for selections
						if (selectionCatType == iRBTConstant.SHUFFLE
								&& confAzaanCategoryIdList.contains(String
										.valueOf(selectionCatId))) {
							String subExtraInfo = subscriber.extraInfo();
							HashMap<String, String> subextraInfoMap = DBUtility
									.getAttributeMapFromXML(subExtraInfo);
							removeAzaanSubType(strSubID, subextraInfoMap);
						}
						
						//RBT-13544 TEF ES - Mi Playlist functionality need to update normal selections in downloads table
						
					}
					//RBT-13544 TEF ES - Mi Playlist functionality need to update normal selections in downloads table
					m_rbtDBManager.updateDownloadStatusByDownloadStatus(strSubID,
							subscriberStatus.subscriberFile(),subscriberStatus.deSelectedBy(),subscriberStatus.callerID(),subscriberStatus.status(), "x", "t",subscriberStatus.categoryID(),subscriberStatus.categoryType());
					// Do the base deactivation - RBT-7908
					deactiveSubscriberNoActiveSelection(subscriber, type);

					if (isEnablePrePromptIfNoSel) {
						resetFreeSongBasedOnCircle(subscriber, refID);
					}

					if (RBTEventLogger
							.isEventLoggingEnabled(RBTEventLogger.Event.TLOG)) {
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.S");
						Date curDate = new Date();
						String subscriberType = subscriberStatus.prepaidYes() ? "P"
								: "B";

						StringBuilder tLogBuilder = new StringBuilder();
						tLogBuilder.append(subscriber.circleID()).append("|")
								.append(strSubID).append("|")
								.append(subscriberType);
						tLogBuilder.append("|").append(sbnID).append("|")
								.append("RBT_SEL_").append(classType);
						tLogBuilder
								.append("|")
								.append("D")
								.append("||0|")
								.append(subscriberStatus.deSelectedBy())
								.append("|mmp|")
								.append(format.format(subscriberStatus
										.setTime()));
						tLogBuilder.append("|").append(format.format(curDate))
								.append("|").append(format.format(curDate))
								.append("|0|C|0|||D|||||");

						RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG,
								tLogBuilder.toString());
					}

					String toneType = getToneType(subscriberStatus.status(),
							subscriberStatus.categoryType());
					int categoryID = subscriberStatus.categoryID();
					SRBTUtility.updateSocialSelectionForDeactivation(
							m_socialRBTAllowed, m_socialRBTAllUpdateInOneModel,
							strSubID, callerID, iClipId, categoryID, type,
							chargeClass, toneType);
				}
				if (getParamAsBoolean("SHUFFLE_CHNG_SERVICE", "FALSE")
						&& catType == 0) {
					Subscriber sub = m_rbtDBManager.getSubscriber(strSubID);
					String albumSubClass = getParamAsString("COMMON",
							"ALBUM_RENTAL_PACKS");
					if (sub.subscriptionClass().equals(albumSubClass)
							&& doesSubscriberHaveNoMoreAlbums(strSubID)) {
						convertSubscriptionType(strSubID, albumSubClass,
								"DEFAULT", sub);
					}
				}
				if (getParamAsBoolean("NO_SEL_DEACT_SUB", "FALSE")
						|| getParamAsBoolean("NO_SEL_DEACT_SUB_IN_DAYS",
								"FALSE")) {
					SubscriberStatus[] subscriberStatus1 = smSubscriberRecords(
							strSubID, rbtType);
					if (subscriberStatus1 == null) {
						if (getParamAsBoolean("NO_SEL_DEACT_SUB_IN_DAYS",
								"FALSE")) {
							Date next = getNextDate("M2");
							updateEndDate(strSubID, next, m_subClassMoveOptIn);
						} else
							deactivateSubscriber(strSubID, "DAEMON", type);
					}
				}
				if (isTrialReminderEnabled(subscriber)
						&& isTrialReminderPack(subscriberStatus.classType()))
					m_rbtDBManager
							.deleteTrialSelectionReminder(subscriberStatus
									.subID());
				writeEventLog(extraInfo, strSubID,
						subscriberStatus.selectedBy(), "0", "Customization",
						clip,
						getCriteria(songStatus, callerID, subscriberStatus));
				finalResponse = SUCCESS;
				return SUCCESS;
			}

			ChargeSMS chargeSms = null;
			if (subscriber != null) {
				chargeSms = CacheManagerUtil.getChargeSMSCacheManager()
						.getChargeSMS(classType, "SEL", subscriber.language());
			} else {
				chargeSms = CacheManagerUtil.getChargeSMSCacheManager()
						.getChargeSMS(classType, "SEL");
			}

			if (m_statusSuccess.equalsIgnoreCase(status)) {
				if (subscriberStatus != null && !isAddToDownloads
						&& canProcessClipStatus) {

					checkIfClipExistsAndUpdate(subscriberStatus.subID(),
							subscriberStatus.subscriberFile(),
							subscriberStatus.categoryType(),
							subscriberStatus.categoryID());
				}
				String selInfoAmt = null;
				if (subscriberStatus != null
						&& subscriberStatus.subscriberFile() != null
						&& subscriberStatus.subscriberFile().indexOf("_ugc_") != -1) {
					try {
						String selInfoOrig = subscriberStatus.selectionInfo();
						if (selInfoOrig == null)
							selInfoOrig = "";
						selInfoAmt = selInfoOrig;
						if (amountCharged != null)
							amountCharged = amountCharged.trim();
						if (amountCharged != null
								&& amountCharged.indexOf(".") != -1)
							amountCharged = amountCharged.substring(0,
									amountCharged.indexOf("."));
						int amtSel = Integer.parseInt(amountCharged);
						if (!m_ugcCreditMap.containsKey(amountCharged)) {
							logger.info("The following charged amount is not configured for credit. hence not mariking it. Amount is "
									+ amountCharged);
						} else if (selInfoOrig.indexOf("|AMT:") > -1
								&& selInfoOrig.indexOf(":AMT|") > -1) {
							int firstIndex = selInfoOrig.indexOf("|AMT:");
							int secondIndex = selInfoOrig.indexOf(":AMT|");
							String firstPart = selInfoOrig.substring(0,
									firstIndex);
							String secondPart = selInfoOrig.substring(
									secondIndex + 5, selInfoOrig.length());
							selInfoAmt = firstPart + secondPart + "|AMT:"
									+ amtSel + ":AMT|";
						} else
							selInfoAmt += "|AMT:" + amtSel + ":AMT|";
					} catch (Exception e) {
						logger.error("", e);
					}

				}
				if (m_strActionActivation.equalsIgnoreCase(action)
						|| m_strActionEvent.equalsIgnoreCase(action)) {
					Date startTime = null;
					if (Utility.isNavCat(catType)) {
						Category category = m_rbtCacheManager
								.getCategory(catID);
						if (category != null) {
							startTime = category.getCategoryStartTime();
							start = getFormattedDate(m_timeSdf,
									category.getCategoryStartTime());
							end = getFormattedDate(m_timeSdf,
									category.getCategoryEndTime());
						}
					} else if (subscriberStatus.status() == 2) {
						startTime = subscriberStatus.startTime();
					} else if ((subscriberStatus.status() == 95 || subscriberStatus
							.status() == 99)
							&& subscriberStatus.selInterval() == null) {
						Date date = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse("2004-01-01 00:00:00");
						if(!date.equals(subscriberStatus.startTime()))
						    startTime = subscriberStatus.startTime();
					}
					if (selStatus == null) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_NOT_EXISTS;
					} else if (!selStatus.equals("N") && !selStatus.equals("A")
							&& !selStatus.equals("G")) {
						if (selStatus.equals("B")) {
							writeEventLog(
									extraInfo,
									strSubID,
									subscriberStatus.selectedBy(),
									"402",
									"Customization",
									clip,
									getCriteria(songStatus, callerID,
											subscriberStatus));
							return SELECTION_ALREADY_ACTIVE;
						} else if (m_deActStatus.contains(selStatus)) {
							if (m_deActStatus.contains(selStatus)
									&& getFormattedDate(sdf,
											subscriberStatus.startTime())
											.startsWith("20040101")) {
								char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
								if (oldLoopStatus == LOOP_STATUS_EXPIRED)
									newLoopStatus = oldLoopStatus;

								// Added setTime , extraInfo parameters
								String ret = smUpdateDeactiveSelectionSuccess(
										strSubID, callerID, refID, type,
										songStatus, setTime, startTime,
										classType, newLoopStatus, fromTime,
										toTime, rbtType, extraInfo);

								if (ret == null
										|| (!ret.equalsIgnoreCase(m_success) && !ret
												.equalsIgnoreCase(m_failure))) {
									writeEventLog(
											extraInfo,
											strSubID,
											subscriberStatus.deSelectedBy(),
											"402",
											"Customization",
											clip,
											getCriteria(songStatus, callerID,
													subscriberStatus));
									return FAILURE;
								}
								boolean isOLAContent = false;
								if (clip != null && clip.getContentType() != null) {
									isOLAContent = clip.getContentType().equalsIgnoreCase(CONTENT_TYPE_OLA);
								}
								boolean isSelectionTrackAllowed = getParamAsBoolean("SONG_REPRICING_SELECTION_TRACK_ALLOWED", "FALSE");
								//For TEF-Spain song re-pricing. If selection success callback and not download model, then make entry in subscriber downloads table as status as STATE_DOWNLOAD_SEL_TRACK.
								if (!isOLAContent && isSelectionTrackAllowed && !(subscriberStatus.status() == 90 || subscriberStatus.status() == 99 || subscriberStatus.selType() == 2 )
										&& null == m_rbtDBManager.getActiveSubscriberDownloadByStatus(subscriberStatus.subID(),subscriberStatus.subscriberFile(),"t", subscriberStatus.categoryID(), subscriberStatus.categoryType())) {
									Category cat = m_rbtCacheManager.getCategory(subscriberStatus.categoryID());
									Categories categoriesObj = CategoriesImpl.getCategory(cat);
									HashMap<String, String> responseParams = null;
									
									if (RBTDBManager.catTypesForAutoRenewal != null && RBTDBManager.catTypesForAutoRenewal.contains(String.valueOf(categoriesObj.type()))) {
										responseParams = new HashMap<String, String>();
										responseParams.put("SELECTION_REF_ID", subscriberStatus.refID());
									}
									m_rbtDBManager.addSubscriberDownloadRW(subscriberStatus.subID(), subscriberStatus.subscriberFile(), categoriesObj, null, true, 
											classType, selBy, selectionInfo, DBUtility.getAttributeMapFromXML(extraInfo), false, false, false, responseParams, null, subscriberStatus.status(),subscriberStatus.callerID(), null);
								}

								writeEventLog(
										extraInfo,
										strSubID,
										subscriberStatus.deSelectedBy(),
										"0",
										"Customization",
										clip,
										getCriteria(songStatus, callerID,
												subscriberStatus));
								finalResponse = SUCCESS;
								return SUCCESS;
							} else {
								writeEventLog(
										extraInfo,
										strSubID,
										subscriberStatus.deSelectedBy(),
										"402",
										"Customization",
										clip,
										getCriteria(songStatus, callerID,
												subscriberStatus));
								return SELECTION_ALREADY_DEACTIVE;
							}
						}
					}
					if (subscriberStatus.subscriberFile() != null
							&& subscriberStatus.subscriberFile().indexOf(
									"_ugc_") != -1)
						if (selInfoAmt != null
								&& selInfoAmt.indexOf("AMT") != -1
								&& m_ugcCreditMap.containsKey(amountCharged))
							selInfoAmt += ":UGC";
					if (selStatus.equals("G")) {
						if (oldLoopStatus == LOOP_STATUS_LOOP_FINAL
								|| oldLoopStatus == LOOP_STATUS_LOOP_INIT
								|| oldLoopStatus == LOOP_STATUS_LOOP)
							oldLoopStatus = LOOP_STATUS_LOOP_INIT;
						else
							oldLoopStatus = LOOP_STATUS_OVERRIDE_INIT;
					}

					if (sbnID != null)
						extraInfo = DBUtility.setXMLAttribute(extraInfo,
								"SBN_ID", sbnID);
					extraInfo = DBUtility.removeXMLAttribute(extraInfo,
							iRBTConstant.EXTRA_INFO_OFFER_ID);

					/*
					 * songWavFilesList will contains, all de-activation
					 * selection song rbt wav file name.
					 */

					List<String> songWavFilesList = new ArrayList<String>();
					if (preRbtSupportedModesSet != null
							&& directActPreRbtSupportedModesSet != null
							&& directActPreRbtSupportedModesSet
									.contains(subscriber.activatedBy())) {
						if (preRbtSupportedModesSet.contains(subscriberStatus
								.selectedBy()) != isModesSupportedForPreRbt) {
							if (subscriber != null) {
								String subExtraInfo = subscriber.extraInfo();
								HashMap<String, String> subExtraInfoMap = DBUtility
										.getAttributeMapFromXML(subExtraInfo);

								if (subExtraInfoMap != null
										&& subExtraInfoMap
												.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG)
										&& subExtraInfoMap
												.get(EXTRA_INFO_INTRO_PROMPT_FLAG)
												.equals(ENABLE_PRESS_STAR_INTRO)) {
									subExtraInfo = DBUtility.setXMLAttribute(
											subExtraInfo,
											EXTRA_INFO_INTRO_PROMPT_FLAG,
											DISABLE_PRESS_STAR_INTRO);
									m_rbtDBManager
											.updateExtraInfoAndPlayerStatus(
													strSubID, subExtraInfo, "A");
								}
							}
						}
					}

					// check extrainfo CLASS_TYPE_UPGRADE present get the class
					// type and overwrite the classtype and update SEL_STATUS C
					boolean updateSelStatus = false;
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (null != extraInfoMap
							&& extraInfoMap.containsKey("CLASS_TYPE_UPGRADE")) {
						updateSelStatus = true;
						classType = extraInfoMap.get("CLASS_TYPE_UPGRADE");
						logger.info("Updated classType: " + classType);
					}
					if (null != extraInfo && extraInfoMap.containsKey(EXTRA_INFO_TRANS_ID)) {
						extraInfoMap.remove(EXTRA_INFO_TRANS_ID);
						logger.info("TRANS ID has been removed from extr info of subscriber: "
								+ strSubID);
					}
					if (null != extraInfoMap && extraInfoMap.containsKey(EXTRA_INFO_TPCGID)) {
						extraInfoMap.remove(EXTRA_INFO_TPCGID);
						logger.info("TPCG ID has been removed from extr info of subscriber: "
								+ strSubID);
					}
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					// selection activation success
					String ret = smSelectionActivationSuccess(strSubID,
							callerID, songStatus, setTime, nextChargeDate,
							startTime, type, fromTime, toTime, classType,
							oldLoopStatus, isPrepaid, selInfoAmt, rbtType,
							subscriberStatus.selInterval(), refID, extraInfo,
							clip, songWavFilesList, updateSelStatus, circleIDFromPrism);

					logger.info("Updated db for selection activation "
							+ "success callback. " + "strSubID: " + strSubID
							+ ", status updated: " + ret + ", action: "
							+ action + ", classType: " + classType
							+ ", selBy: " + selBy);
					
					if (ret.equalsIgnoreCase(m_success)) {
						
						//RBT-13163: MP Non MP TEF/VF/OR Spain
						if (!isAddToDownloads) {
							MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
							if (mpNonMpFeature != null) {
								logger.debug("MP Non MP feature enabled.");
								MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
								mpNonMpFeatureBean.setSubscriber(subscriber);
								mpNonMpFeatureBean.setChargeClass(classType);
								mpNonMpFeatureBean.setMode(subscriberStatus.selectedBy());
								mpNonMpFeature.checkAndActivateMusicPack(mpNonMpFeatureBean);
							}
						}
						if( RBTParametersUtils.getParamAsBoolean("COMMON",
								"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
							m_rbtDBManager.deactivateOldODAPackOnSuccessCallback(strSubID, refID, callerID,
									subscriberStatus.categoryType(), subscriberStatus,false, null);
						}
						boolean isCorpSubBlockedForContest = RBTParametersUtils.getParamAsBoolean("DAEMON",
								"BLOCK_CORP_SUBSCRIBER_FOR_CONTEST", "FALSE");
						boolean contestDisabledForCorpUser = false;
						if(isCorpSubBlockedForContest && isCorpSubscriber(subscriber)){
							 contestDisabledForCorpUser = true;
						}
						
						if(!contestDisabledForCorpUser){
							int pointsEarned = RBTContestUtils.getPointsEarned(
									"SEL", action, classType, amountCharged);
							if(pointsEarned > 0) {
								RBTContestUtils.pointsContest(strSubID, pointsEarned);
							}
							// To hit contest url
							String circleId = subscriber.circleID();
							RBTContestUtils.hitContestUrl(strSubID, circleId, selBy, amountCharged,
									"DOWNLOAD");
						}
						
						// In case RBT like feature changes, send an sms to the
						// B'party.
						// And it should not send any sms if it is download
						// model.
						if (!isAddToDownloads) {
							if (m_strActionActivation.equalsIgnoreCase(action)
									&& m_statusSuccess.equalsIgnoreCase(status)) {
								logger.debug("Received Selection activation success"
										+ " callback for subscriberId: "
										+ strSubID);

								// RBT Like feature starts
								String mode = subscriberStatus.selectedBy();
								String confMode = getParamAsString("GATHERER",
										Constants.RBT_LIKE_MODE, "RBT_LIKE");
								logger.debug("Checking mode for like feature. "
										+ "selection mode: " + mode
										+ ", like mode: " + confMode);
								if (confMode.equalsIgnoreCase(mode)) {
									checkAndSendSmsForLike(strSubID, callerID,
											subscriber,
											subscriberStatus.selectionInfo(),
											songName);
								}
								// RBT Like feature ends

								// Azaan for selections
								int selectionCatType = subscriberStatus
										.categoryType();
								int selectionCatId = subscriberStatus
										.categoryID();
								if (selectionCatType == iRBTConstant.SHUFFLE
										&& confAzaanCategoryIdList
												.contains(String
														.valueOf(selectionCatId))) {
									String subExtraInfo = subscriber
											.extraInfo();
									HashMap<String, String> subExtraInfoMap = DBUtility
											.getAttributeMapFromXML(subExtraInfo);
									addAzaanSubType(strSubID, subExtraInfoMap);
								}
								if (GrbtLogger.downloadsLogger != null
										&& getParamAsBoolean("GRBT",
												"UPLOAD_DATA", "FALSE")
										&& getParamAsBoolean("GRBT",
												"UPLOAD_DATA_PURCHASE", "FALSE"))
									GrbtLogger.downloadsLogger.info(strSubID
											+ "," + iClipId + ","
											+ subscriberStatus.subscriberFile()
											+ ","
											+ subscriberStatus.categoryID()
											+ ","
											+ subscriberStatus.categoryType()
											+ ","
											+ getChargeInfo(strSubID, Integer.parseInt(clipId), subscriberStatus.categoryID()));
								
								//RBT-13544 TEF ES - Mi Playlist functionality need to deactivate normal selections 
								m_rbtDBManager.addOldMiplayListSelections(subscriberStatus);
							}
							boolean isOLAContent = false;
							if (clip != null && clip.getContentType() != null) {
								isOLAContent = clip.getContentType().equalsIgnoreCase(CONTENT_TYPE_OLA);
							}
							boolean isSelectionTrackAllowed = getParamAsBoolean("SONG_REPRICING_SELECTION_TRACK_ALLOWED", "FALSE");
							//For TEF-Spain song re-pricing. If selection success callback and not download model, then make entry in subscriber downloads table as status as STATE_DOWNLOAD_SEL_TRACK.
							if (!isOLAContent && isSelectionTrackAllowed 
									&& !(subscriberStatus.status() == 90 
									|| subscriberStatus.status() == 99 
									|| subscriberStatus.selType() == 2 )
									&& null == m_rbtDBManager.getActiveSubscriberDownloadByStatus(subscriberStatus.subID(),subscriberStatus.subscriberFile(),"t", subscriberStatus.categoryID(), subscriberStatus.categoryType())) {
								Category cat = m_rbtCacheManager.getCategory(subscriberStatus.categoryID());
								Categories categoriesObj = CategoriesImpl.getCategory(cat);
								HashMap<String, String> responseParams = null;
								
								if (RBTDBManager.catTypesForAutoRenewal != null && RBTDBManager.catTypesForAutoRenewal.contains(String.valueOf(categoriesObj.type()))) {
									responseParams = new HashMap<String, String>();
									responseParams.put("SELECTION_REF_ID", subscriberStatus.refID());
								}
								m_rbtDBManager.addSubscriberDownloadRW(subscriberStatus.subID(), subscriberStatus.subscriberFile(), categoriesObj, null, true, 
										classType, selBy, selectionInfo, extraInfoMap, false, false, false, responseParams, null, subscriberStatus.status(),subscriberStatus.callerID(), null);
							}
						}

						logger.debug("Checking subscriber extra info contains BI_OFFER. subExtraInfoMap: "
								+ subscriberStatus.extraInfo());
						String selExtraInfo = subscriberStatus.extraInfo();
						Map<String, String> selExtraInfoMap = DBUtility
								.getAttributeMapFromXML(selExtraInfo);
						if (m_strActionActivation.equalsIgnoreCase(action)
								&& null != selExtraInfoMap
								&& selExtraInfoMap.containsKey("BI_OFFER")) {
							String biBlackListUrl = getParamAsString(COMMON,
									"BLACKLIST_URL_FOR_BI_OFFER", null);
							logger.debug("Configured bi url: " + biBlackListUrl);
							if (biBlackListUrl != null) {
								biBlackListUrl = biBlackListUrl.replaceAll(
										"%MSISDN%", strSubID);
								biBlackListUrl = biBlackListUrl.replaceAll(
										"%SRVKEY%",
										subscriberStatus.classType());
								logger.debug("Making a http hit with bi url: "
										+ biBlackListUrl);
								makeHttpRequest(biBlackListUrl);
							}
							selExtraInfoMap.remove("BI_OFFER");
						}

						// ADDED BY DEEPAK KUMAR FOR IBM SM INTEGRATION
						if (getParamAsBoolean("IBM_SM_INTEGRATION", "FALSE")) {
							String currSelExtraInfo = subscriberStatus
									.extraInfo();
							Map<String, String> currSelExtraInfoMap = DBUtility
									.getAttributeMapFromXML(currSelExtraInfo);
							if (currSelExtraInfoMap != null
									&& currSelExtraInfoMap
											.containsKey("OLD_REF_ID")) {
								ArrayList<String> refIDList = new ArrayList<String>();
								refIDList.add(refID);
								String oldRefID = currSelExtraInfoMap
										.get("OLD_REF_ID");
								currSelExtraInfoMap.remove("OLD_REF_ID");
								String modExtraInfo = DBUtility
										.getAttributeXMLFromMap(currSelExtraInfoMap);
								m_rbtDBManager.updateSelectionExtraInfo(
										strSubID, refIDList, modExtraInfo);
								refIDList.remove(refID);
								SubscriberStatus subStatus = m_rbtDBManager
										.getRefIDSelection(strSubID, oldRefID);
								refIDList.add(oldRefID);
								String prevSelExtraInfo = subStatus.extraInfo();
								Map<String, String> prevSelExtraInfoMap = DBUtility
										.getAttributeMapFromXML(prevSelExtraInfo);
								if (prevSelExtraInfoMap == null)
									prevSelExtraInfoMap = new HashMap<String, String>();
								prevSelExtraInfoMap.put("DCT_DONE", "TRUE");
								String newPrevSelExtraInfo = DBUtility
										.getAttributeXMLFromMap(prevSelExtraInfoMap);
								m_rbtDBManager.updateSelectionExtraInfo(
										strSubID, refIDList,
										newPrevSelExtraInfo);
							}
						}
						Calendar tmpDate = Calendar.getInstance();
						tmpDate.add(Calendar.MONTH, -1);
						if (subscriber != null
								&& subscriber.startDate() != null
								&& subscriber.startDate().before(
										tmpDate.getTime()))
							ContestInfluencerWhilelistLogger
									.writeWhiteListedNumber(subscriber);
						if (RBTEventLogger
								.isEventLoggingEnabled(RBTEventLogger.Event.TLOG)) {
							DateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss.S");
							Date curDate = new Date();
							String subscriberType = subscriberStatus
									.prepaidYes() ? "P" : "B";

							StringBuilder tLogBuilder = new StringBuilder();
							tLogBuilder.append(subscriber.circleID())
									.append("|").append(strSubID).append("|")
									.append(subscriberType);
							tLogBuilder.append("|").append(sbnID).append("|")
									.append("RBT_SEL_")
									.append(subscriberStatus.classType());
							tLogBuilder.append("|").append("A").append("||")
									.append(amountCharged).append("|")
									.append(subscriberStatus.selectedBy());
							tLogBuilder
									.append("|mmp|")
									.append(format.format(subscriberStatus
											.setTime())).append("|")
									.append(format.format(curDate));
							tLogBuilder.append("||0|C|0|||P|||")
									.append("[CHG=1,").append(amountCharged)
									.append(",,,,,Already Charged]")
									.append("||");

							RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG,
									tLogBuilder.toString());
						}

						String toneType = getToneType(
								subscriberStatus.status(),
								subscriberStatus.categoryType());
						SRBTUtility.updateSocialSelectionForSuccess(
								m_socialRBTAllowed,
								m_socialRBTAllUpdateInOneModel, strSubID,
								callerID, iClipId, subscriberStatus,
								rbtSystemType, extraInfo, chargeClass,
								modeOfActivation, toneType);
						String result = "SUCCESS";

						if (songStatus == 99) {
							start = null;
							SimpleDateFormat formatter = new SimpleDateFormat(
									"dd/MM/yyyy hh:mm a");
							Date endTime = subscriberStatus.endTime();
							end = formatter.format(endTime);
						}

						if (isAddToDownloads)
							m_rbtDBManager.updateDownloadStatus(strSubID,
									subscriberStatus.subscriberFile(), 'y');
						if ((getParamAsBoolean("SHUFFLE_CHNG_SERVICE", "FALSE") && catType == 0)) {
							Subscriber sub = m_rbtDBManager
									.getSubscriber(strSubID);
							if (sub.subscriptionClass().equals("DEFAULT")) {
								convertSubscriptionType(
										strSubID,
										"DEFAULT",
										getParamAsString("COMMON",
												"ALBUM_RENTAL_PACKS"), sub);
							}
						}
						try {
							if (m_rbtDBManager.m_isLTPOnForSelAct) {
								String selectedBy = subscriberStatus
										.selectedBy();
								String finalActInfo = null;
								if (selectedBy != null) {
									Subscriber sub = m_rbtDBManager
											.getSubscriber(strSubID);
									if (catType == 0
											&& m_rbtDBManager.m_ltpAlbumMap != null
											&& m_rbtDBManager.m_ltpAlbumMap
													.containsKey(selectedBy)) {
										int ltpPoints = ((Integer) m_rbtDBManager.m_ltpAlbumMap
												.get(selectedBy.toUpperCase()))
												.intValue();
										if (ltpPoints > 0)
											finalActInfo = m_rbtDBManager
													.addLTPPoints(sub
															.activationInfo(),
															ltpPoints);
									} else if (catType != 0
											&& m_rbtDBManager.m_ltpSelMap != null
											&& m_rbtDBManager.m_ltpSelMap
													.containsKey(selectedBy)) {
										int ltpPoints = ((Integer) m_rbtDBManager.m_ltpSelMap
												.get(selectedBy.toUpperCase()))
												.intValue();
										if (ltpPoints > 0)
											finalActInfo = m_rbtDBManager
													.addLTPPoints(sub
															.activationInfo(),
															ltpPoints);
									}
									if (finalActInfo != null
											&& (sub.activationInfo() == null || !finalActInfo
													.equalsIgnoreCase(sub
															.activationInfo())))
										m_rbtDBManager.setActivationInfo(
												sub.subID(), finalActInfo);
								}
							}
						} catch (Exception e) {
							logger.error("", e);
						}
						if (getParamAsBoolean("HIT_RT_PROMO_URL", "FALSE")) {
							RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent(
									strSubID, 1, 1, Integer.parseInt(clipId),
									classType, subscriberStatus.selectedBy(),
									selectionInfo, "dummy message");
							rbtCallBackEvent
									.createCallbackEvent(rbtCallBackEvent);
						}

						//<RBT-10520>
						if (getParamAsBoolean("ENABLE_AD2C_FEATURE", "FALSE")) {
							if (amountCharged == null) {
								if (null == chargeClass) {
									amountCharged = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType).getAmount();
								} else {
									amountCharged = chargeClass.getAmount();
								}
							}
							String classTypeForRBTCallBackEvent = "price=" + amountCharged; 
							boolean updateStatus = RBTCallBackEvent.update(
									RBTCallBackEvent.MODULE_ID_AD2C,
									subscriber.subID(),
									refID,		//refId is passed as the selectionInfo
									RBTCallBackEvent.AD2C_TO_BE_SENT,
									classTypeForRBTCallBackEvent);
							logger.debug("updateStatus: " +updateStatus +". SubscriberId: "
									+ subscriber.subID()
									+ ", refID: "
									+ refID);
						}
						//</RBT-10520>
						
						// JIRA:RBT-7443 :: CONTEST ON COPY INFLUENCER FEATURE
						// BEGINS
						boolean isCopyContestMode = copyContestModesList
								.contains(subscriberStatus.selectedBy());
						String contestCallerID = getMsisdnFromSelectionInfo(subscriberStatus.selectionInfo());
						logger.debug("Checking selection for copy contest. subscriber: "
								+ strSubID
								+ ", contestCallerID: "
								+ contestCallerID
								+ "isCopyContestMode: "
								+ isCopyContestMode
								+ ", isContestOnCopyInfluencerAllowed: "
								+ isContestOnCopyInfluencerAllowed);
						if (isCopyContestMode
								&& isContestOnCopyInfluencerAllowed) {
							copyInfluencerContest(strSubID, contestCallerID);

						}
						// JIRA:RBT-7443 :: CONTEST ON COPY INFLUENCER FEATURE
						// ENDS

						if ((getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE") || getParamAsBoolean(
								"SEND_SMS_CROSS_PROMO", "FALSE"))
								&& chargeClass != null) {
							// SELCTION ACTIVATION SUCCESS
							boolean isRenewalRequest = false;
							String selectedBy = subscriberStatus.selectedBy();
							if (getParamAsBoolean("SEND_SMS_PRE_CHRG", "FALSE")
									&& selBy != null
									&& Arrays.asList(
											getParamAsString("DAEMON",
													"SELECTED_PRE_CHRG", "")
													.split(","))
											.contains(selBy)
									&& chargeClass.getSmschargeSuccess() != null
									&& !chargeClass.getSmschargeSuccess()
											.equalsIgnoreCase("null")) {
								sendSelectionSMSCrossPromo(subscriber,
										chargeClass.getSmschargeSuccess(),
										createTime, songName, callerNo, start,
										end, clipId, clipPromoId, result,
										songStatus, catType, selectedBy,
										movieName, isRenewalRequest,
										chargeClass.getChargeClass());
							} else if (chargeSms == null) {
								if (chargeClass.getSmschargeSuccess() != null
										&& !chargeClass.getSmschargeSuccess()
												.equalsIgnoreCase("null")) {
									sendSelectionSMSCrossPromo(subscriber,
											chargeClass.getSmschargeSuccess(),
											createTime, songName, callerNo,
											start, end, clipId, clipPromoId,
											result, songStatus, catType,
											selectedBy, movieName,
											isRenewalRequest,
											chargeClass.getChargeClass());
								}
								//RBT-12192	SMS not going for override shuffle
								else if(Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES", "10")
                                         .split(",")).contains(subscriberStatus.categoryType()+ "")
										&& getParamAsBoolean("SEND_SMS_FOR_OVERRIDE_SHUFFLE",
												"FALSE") && start != null && end != null) {
									String smsText = "";
									logger.info("SEND_SMS_FOR_OVERRIDE_SHUFFLE is TRUE..sending sms for override shuffle...");
									sendSelectionSMSCrossPromo(subscriber,
											smsText,createTime, songName, callerNo,
											start, end, clipId, clipPromoId,
											result, songStatus, catType,
											selectedBy, movieName,
											isRenewalRequest,
											chargeClass.getChargeClass());
									
								}
							} else {
								String smsText = (type != null
										&& type.equalsIgnoreCase("p") ? chargeSms
										.getPrepaidSuccess() : chargeSms
										.getPostpaidSuccess());
								sendSelectionSMSCrossPromo(subscriber, smsText,
										createTime, songName, callerNo, start,
										end, clipId, clipPromoId, result,
										songStatus, catType, selectedBy,
										movieName, isRenewalRequest,
										chargeClass.getChargeClass());
							}
						} else if (getParamAsBoolean("SEND_NOTIFICATION_GIFT",
								"FALSE")
								&& selBy.equals("GIFT")
								&& chargeClass != null) {
							String selectedBy = subscriberStatus.selectedBy();
							boolean isRenewalRequest = false;
							if (chargeSms == null) {
								if (chargeClass.getSmschargeSuccess() != null
										&& !chargeClass.getSmschargeSuccess()
												.equalsIgnoreCase("null"))
									sendSelectionSMSCrossPromo(subscriber,
											chargeClass.getSmschargeSuccess(),
											createTime, songName, callerNo,
											null, null, clipId, clipPromoId,
											result, songStatus, catType,
											selectedBy, movieName,
											isRenewalRequest,
											chargeClass.getChargeClass());
							} else {
								String smsText = (type != null
										&& type.equalsIgnoreCase("p") ? chargeSms
										.getPrepaidSuccess() : chargeSms
										.getPostpaidSuccess());
								sendSelectionSMSCrossPromo(subscriber, smsText,
										createTime, songName, callerNo, null,
										null, clipId, clipPromoId, result,
										songStatus, catType, selectedBy,
										movieName, isRenewalRequest,
										chargeClass.getChargeClass());
							}
						}
						if (getParamAsBoolean("SEND_SMS_TO_RETAILER", "FALSE")) {
							String selectedBy = subscriberStatus.selectedBy();
							String retailerMode = getParamAsString(
									iRBTConstant.DAEMON,
									"MODE_FOR_RETAILER_DT", "RET");
							if (selectedBy.equalsIgnoreCase(retailerMode)) {
								String selectInfo = subscriberStatus
										.selectionInfo();
								String[] selectInfoArr = selectInfo
										.split("\\|");
								String retailerNumber = null;
								for (String value : selectInfoArr) {
									if (value.startsWith("RET:"))
										retailerNumber = value.substring(value
												.indexOf(":") + 1);
								}
								String smsText = CacheManagerUtil
										.getSmsTextCacheManager().getSmsText(
												"RETAILER_SELECTON_CONFIRM",
												"SUCCESS",
												subscriber.language());
								smsText = getRetailerFinalSMS(smsText,
										subscriber.subID(), songName,
										clipPromoId);
								Tools.sendSMS(
										getSenderNumber(subscriber.circleID()),
										retailerNumber,
										smsText,
										getParamAsBoolean("SEND_SMS_MASS_PUSH",
												"FALSE"), createTime);
							}
						}

						if (getParamAsBoolean("SEND_ZOOMIN_SMS", "FALSE")) {
							if (isModeSupportedForZoominSMS(subscriberStatus
									.selectionInfo())) {
								String subExtraInfo = (subscriber != null) ? subscriber
										.extraInfo() : null;
								if (subExtraInfo == null
										|| !subExtraInfo
												.contains(EXTRA_INFO_ZOOMIN)) {
									String smsText = CacheManagerUtil
											.getSmsTextCacheManager()
											.getSmsText(
													TYPE_ZOOMIN,
													"OFFER_CODE_TEXT",
													(subscriber != null) ? subscriber
															.language() : null);
									if (smsText != null) {
										TransData transData = m_rbtDBManager
												.getFirstTransDataByType(TYPE_ZOOMIN);
										if (transData != null) {
											String transID = transData
													.transID();
											smsText = smsText.replaceAll(
													"%ZOOMIN", transID);
											boolean success = Tools
													.sendSMS(
															getSenderNumber(subscriber.circleID()),
															strSubID,
															smsText,
															getParamAsBoolean(
																	"SEND_SMS_MASS_PUSH",
																	"FALSE"),
															createTime);

											if (success) {
												subExtraInfo = DBUtility
														.setXMLAttribute(
																subExtraInfo,
																EXTRA_INFO_ZOOMIN,
																transID);
												m_rbtDBManager.updateExtraInfo(
														strSubID, subExtraInfo);
												m_rbtDBManager.removeTransData(
														transID, TYPE_ZOOMIN);
											} else {
												m_rbtDBManager
														.updateTransData(
																transID,
																TYPE_ZOOMIN,
																transData
																		.subscriberID(),
																transData
																		.transDate(),
																"0");
											}

											StringBuilder logBuilder = new StringBuilder();
											logBuilder.append(strSubID)
													.append(", ")
													.append(transID);
											RBTEventLogger
													.logEvent(
															RBTEventLogger.Event.ZOOMIN,
															logBuilder
																	.toString());
										} else {
											logger.warn("No ZOOMIN trans data available in RBT_TRANS_DATA table");
										}
									} else {
										logger.warn("ZOOMIN smsText not configured, so not sending the SMS to the user");
									}
								} else {
									logger.info("Subscriber has been already sent the ZOOMIN offer code, so not sending now");
								}
							}
						}

						/*
						 * Added by SenthilRaja Change TNB Optin to Optout
						 * service
						 */
						chageTNBUserOptinModeToOptOut(action, subscriberStatus,
								null, subscriber);

						// Hits the contest url for the subscriber
						CosDetails cos = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										subscriber.cosID());
						if ((getParamAsBoolean("HIT_CONTEST_URL_FOR_LITE_USER",
								"TRUE") || (cos != null && !LITE
								.equalsIgnoreCase(cos.getCosType()))))
							ContestUtils.hitContestUrlForSpecificContent(
									strSubID, clip);

						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"0",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));

						/*
						 * Sms will not send to subscriber, if sms text is not
						 * configured or there is not song for deactivation
						 * Sending sms to user for his previous song is also
						 * available in his inbox until the expiry period of
						 * that song
						 */
						try {
							String smsText = Utility
									.getSmsTextForDeactivationSelection(
											subscriber.language(),
											songWavFilesList);
							if (smsText != null)
								Tools.sendSMS(
										getSenderNumber(subscriber.circleID()),
										subscriber.subID(),
										smsText,
										getParamAsBoolean("SEND_SMS_MASS_PUSH",
												"FALSE"));
						} catch (Exception e) {
							logger.error(e);
						}

						if (status.equals("SUCCESS")) {
							String chargeClassesStr = RBTParametersUtils
									.getParamAsString("COMMON",
											"LOTTERY_SUPPORTED_CHARGE_CLASSES",
											null);
							if (chargeClassesStr != null) {
								Set<String> chargeClassesSet = new HashSet<String>(
										Arrays.asList(chargeClassesStr
												.split(",")));
								if (chargeClassesSet.contains(classType)) {
									m_rbtDBManager.insertRbtLotteryEntry(-1,
											strSubID, new Date(), null,
											clip.getClipId());
								}
							}
							
							//Airtel online url
							hitOnlineURL(strSubID, classType, status,subscriberStatus.selectedBy(),
									amountCharged, subscriberStatus.subscriberFile(), "SEL", refID);
						}
						
						//RBT-12585 RBT Bundling Feature with other service
						String mode = subscriberStatus.selectedBy();
						activateFreeAzaanForContent(subscriber, clip, mode);

						finalResponse = SUCCESS;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						// activation failure
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return INVALID;
					} else {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return FAILURE;
					}

				} else if (m_strActionTrigger.equalsIgnoreCase(action)) {
					if (selStatus.equalsIgnoreCase("Z")
							|| selStatus.equalsIgnoreCase("E")) {
						String loopStatus = null;
						if (subscriberStatus.loopStatus() == 'B')
							loopStatus = "O";
						else if (subscriberStatus.loopStatus() == 'A')
							loopStatus = "L";
						circleIDFromPrism = isCircleIdMatchedWithSub(
								circleIDFromPrism, subscriberStatus);
						String ret = smSelectionRenewalSuccess(strSubID, refID,
								nextChargeDate, type, classType, selInfoAmt,
								rbtType, loopStatus, circleIDFromPrism);

						logger.debug("Successfully updated DB"
								+ " for selection renewal success. ret: " + ret
								+ "subscriberId: " + strSubID + ", action: "
								+ action + ", classType: " + classType);
						
						if (ret.equalsIgnoreCase(m_success)) {
							
							int pointsEarned = RBTContestUtils.getPointsEarned("SEL",action, classType, amountCharged);
							if(pointsEarned > 0) {
								RBTContestUtils.pointsContest(strSubID, pointsEarned);
							}
							
							
							if (!isAddToDownloads) {

								if (selStatus.equalsIgnoreCase(STATE_SUSPENDED)) {
									// Azaan for selections
									int selectionCatType = subscriberStatus
											.categoryType();
									int selectionCatId = subscriberStatus
											.categoryID();
									if (selectionCatType == iRBTConstant.SHUFFLE
											&& confAzaanCategoryIdList
													.contains(String
															.valueOf(selectionCatId))) {
										String subExtraInfo = subscriber
												.extraInfo();
										HashMap<String, String> extraInfoMap = DBUtility
												.getAttributeMapFromXML(subExtraInfo);
										logger.debug("Updating subscriber extraInfo."
												+ ", selExtraInfo: "
												+ extraInfo);
										addAzaanSubType(strSubID, extraInfoMap);
									}
								}
							}
						}

						if (!ret.equalsIgnoreCase(m_success)){
							return FAILURE;
						}
					}
					finalResponse = SUCCESS;
					return SUCCESS;
				} else if (m_strUpgradeSubscription.equalsIgnoreCase(action)) {	//Selection Upgrade success callback
					isImmediate = false;
					logger.debug("Processing selection upgradation ");
					String oldClassType = subscriberStatus.oldClassType();
					logger.info("oldClassType : " + oldClassType);
					if (selStatus.equalsIgnoreCase("B") && oldClassType != null && classType != null && oldClassType.equals(classType))			
						return CALLBACK_ALREADY_RECEIVED;
					else if (selStatus.equalsIgnoreCase("A") || selStatus.equalsIgnoreCase("B"))
						return SELECTION_ACTIVE;
					else if ((selStatus.equalsIgnoreCase("N") || selStatus
							.equalsIgnoreCase("E")) && oldClassType == null)
						return SELECTION_ACT_PENDING;
					else if (selStatus.equalsIgnoreCase("D")
							|| selStatus.equalsIgnoreCase("P")
							|| selStatus.equalsIgnoreCase("F"))
						return SELECTION_DCT_PENDING;
					else if (selStatus.equalsIgnoreCase("X"))
						return SELECTION_DEACTIVE;
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					String loopStatus = null;
					if (subscriberStatus.loopStatus() == 'B')
						loopStatus = "O";
					else if (subscriberStatus.loopStatus() == 'A')
						loopStatus = "L";
					String ret = m_rbtDBManager.smSelectionUpgradationCallback(
							strSubID, refID, classType,
							subscriberStatus.oldClassType(), status, circleIDFromPrism, loopStatus);

					if (ret.equalsIgnoreCase(m_success)) {
						//RBT-13163: MP Non MP TEF/VF/OR Spain 
						if (!isAddToDownloads) {
							MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
							if (mpNonMpFeature != null) {
								logger.debug("MP Non MP feature enabled.");
								MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
								mpNonMpFeatureBean.setSubscriber(subscriber);
								mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
							}
						}
					}
					return ret;
				} else {
					isImmediate = false;
					//selection renewal success callback
					if (selStatus == null) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_NOT_EXISTS;
					} else if (!selStatus.equals("B")) {
						if (selStatus.equals("W") || selStatus.equals("A")
								|| selStatus.equals("N")
								|| selStatus.equals("G")) {
							writeEventLog(
									extraInfo,
									strSubID,
									subscriberStatus.selectedBy(),
									"402",
									"Customization",
									clip,
									getCriteria(songStatus, callerID,
											subscriberStatus));
							logger.warn(COULD_NOT_PROCESS + " Selection "
									+ " is in pending. refID: " + refID
									+ ", subcriberId: " + strSubID
									+ ", status: " + selStatus);
							return SELECTION_ACT_PENDING;
						} else if (selStatus.equals("X")) {
							writeEventLog(
									extraInfo,
									strSubID,
									subscriberStatus.deSelectedBy(),
									"402",
									"Customization",
									clip,
									getCriteria(songStatus, callerID,
											subscriberStatus));
							logger.warn(COULD_NOT_PROCESS + " Selection "
									+ " is deactive. refID: " + refID
									+ ", subcriberId: " + strSubID
									+ ", status: " + selStatus);
							return SELECTION_DEACTIVE;
						}
					} else if (subscriberStatus.nextChargingDate() != null
							&& subscriberStatus.nextChargingDate().getTime() > System
									.currentTimeMillis()) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return INVALID + "|"
								+ "SELECTION RENWAL ALREADY UPDATED";
					}

					// String selectionInfo = subscriberStatus.selectionInfo();
					if (selInfoAmt != null && selInfoAmt.indexOf("AMT") != -1
							&& m_ugcCreditMap.containsKey(amountCharged))
						selInfoAmt += ":UGC";
					String loopStatus = null;
					if (selStatus.equals("Z")) {
						if (subscriberStatus.loopStatus() == 'B')
							loopStatus = "O";
						else if (subscriberStatus.loopStatus() == 'A')
							loopStatus = "L";
					}

					String giftChargeClasses = getParamAsString(
							iRBTConstant.COMMON, "OPTIN_GIFT_CHARGE_CLASS",
							null);
					List<String> giftChargeClassList = null;
					String giftClassType = subscriberStatus.classType();
					boolean isSelectedByGift = subscriberStatus.selectedBy()
							.equalsIgnoreCase("GIFT");
					// update loop status O for gift song
					if (giftChargeClasses != null) {
						giftChargeClassList = Arrays.asList(giftChargeClasses
								.split("\\,"));
						if (isSelectedByGift && giftChargeClassList != null
								&& giftChargeClassList.contains(giftClassType)) {
							loopStatus = "O";
						}
					}
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					// no need to update player on renewal success
					String ret = smSelectionRenewalSuccess(strSubID, refID,
							nextChargeDate, type, classType, selInfoAmt,
							rbtType, loopStatus, circleIDFromPrism);

					if (ret.equalsIgnoreCase(m_success)) {

						logger.debug("Successfully updated DB "
								+ " for selection renewal success"
								+ " callback. subscriberId: " + strSubID);
						
						// Deactiate other normal song, if receives gift renewal
						// callback
						if (isSelectedByGift && giftChargeClassList != null
								&& giftChargeClassList.contains(giftClassType)) {
							// Deactivate all normal song
							m_rbtDBManager.smDeactivateOldSelection(strSubID,
									callerID, subscriberStatus.status(),
									setTime, fromTime, toTime, rbtType,
									subscriberStatus.selInterval(), refID,false);
						}

						if (!isAddToDownloads) {
							//RBT-13163: MP Non MP TEF/VF/OR Spain 

							MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
							if (mpNonMpFeature != null) {
								logger.debug("MP Non MP feature enabled.");
								MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
								mpNonMpFeatureBean.setSubscriber(subscriber);
								mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
							}

							// Azaan for selections
							int selectionCatType = subscriberStatus
									.categoryType();
							int selectionCatId = subscriberStatus.categoryID();
							if (selectionCatType == iRBTConstant.SHUFFLE
									&& confAzaanCategoryIdList.contains(String
											.valueOf(selectionCatId))) {
								String subExtraInfo = subscriber.extraInfo();
								HashMap<String, String> extraInfoMap = DBUtility
										.getAttributeMapFromXML(subExtraInfo);
								logger.debug("Updating subscriber extraInfo."
										+ ", selExtraInfo: " + extraInfo);
								addAzaanSubType(strSubID, extraInfoMap);
							}
						}

						if (RBTEventLogger
								.isEventLoggingEnabled(RBTEventLogger.Event.TLOG)) {
							DateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss.S");
							Date curDate = new Date();
							String subscriberType = subscriberStatus
									.prepaidYes() ? "P" : "B";

							StringBuilder tLogBuilder = new StringBuilder();
							tLogBuilder.append(subscriber.circleID())
									.append("|").append(strSubID).append("|")
									.append(subscriberType);
							tLogBuilder.append("|").append(sbnID).append("|")
									.append("RBT_SEL_").append(classType);
							tLogBuilder
									.append("|")
									.append("R")
									.append("||0|")
									.append(subscriberStatus.deSelectedBy())
									.append("|mmp|")
									.append(format.format(subscriberStatus
											.setTime()));
							tLogBuilder.append("|")
									.append(format.format(curDate))
									.append("||0|C|0|||R|||").append("[CHG=1,")
									.append(amountCharged)
									.append(",,,,,Already Charged]")
									.append("||");

							RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG,
									tLogBuilder.toString());
						}
						try {
							if (m_rbtDBManager.m_isLTPOnForSelRen) {
								String selectedBy = subscriberStatus
										.selectedBy();
								String finalActInfo = null;
								if (selectedBy != null) {
									Subscriber sub = m_rbtDBManager
											.getSubscriber(strSubID);
									if (catType == 0
											&& m_rbtDBManager.m_ltpAlbumMap != null
											&& m_rbtDBManager.m_ltpAlbumMap
													.containsKey(selectedBy)) {
										int ltpPoints = ((Integer) m_rbtDBManager.m_ltpAlbumMap
												.get(selectedBy.toUpperCase()))
												.intValue();
										if (ltpPoints > 0)
											finalActInfo = m_rbtDBManager
													.addLTPPoints(sub
															.activationInfo(),
															ltpPoints);
									} else if (catType != 0
											&& m_rbtDBManager.m_ltpSelMap != null
											&& m_rbtDBManager.m_ltpSelMap
													.containsKey(selectedBy)) {
										int ltpPoints = ((Integer) m_rbtDBManager.m_ltpSelMap
												.get(selectedBy.toUpperCase()))
												.intValue();
										if (ltpPoints > 0)
											finalActInfo = m_rbtDBManager
													.addLTPPoints(sub
															.activationInfo(),
															ltpPoints);
									}
									if (finalActInfo != null
											&& (sub.activationInfo() == null || !finalActInfo
													.equalsIgnoreCase(sub
															.activationInfo())))
										m_rbtDBManager.setActivationInfo(
												sub.subID(), finalActInfo);
									m_rbtDBManager.setActivationInfo(
											sub.subID(), finalActInfo);
								}
							}
						} catch (Exception e) {
							logger.error("", e);
						}

						if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
								&& chargeClass != null) {
							createTime = getCreateTime();
							if (chargeSms == null) {
								// timepass
								if (chargeClass.getSmsrenewalSuccess() != null
										&& !chargeClass.getSmsrenewalSuccess()
												.equalsIgnoreCase("null")) {
									// sendSelectionSMS(strSubID,
									// chargeClass.getSmsrenewalSuccess(),
									// createTime, songName, callerNo, null,
									// null, reason,movieName);
									String result = "SUCCESS";
									boolean isRenewalRequest = false;
									Parameters param = CacheManagerUtil
											.getParametersCacheManager()
											.getParameter("DEAMON",
													"RENEWAL_SMS_CROSS_PROMO_ALLOWED");
									if (param != null
											&& param.getValue() != null
											&& param.getValue()
													.equalsIgnoreCase("TRUE")) {
										isRenewalRequest = true;
									}
									String selectedBy = subscriberStatus
											.selectedBy();
									sendSelectionSMSCrossPromo(subscriber,
											chargeClass.getSmschargeSuccess(),
											createTime, songName, callerNo,
											start, end, clipId, clipPromoId,
											result, songStatus, catType,
											selectedBy, movieName,
											isRenewalRequest,
											chargeClass.getChargeClass());
								}
							} else {
								double renAmt = -1;
								double amtCharged = -1;
								try {
									renAmt = Double.parseDouble(chargeClass
											.getRenewalAmount());
									amtCharged = Double
											.parseDouble(amountCharged);
								} catch (Exception e) {
								}
								if (type != null && type.equalsIgnoreCase("p")) {
									if (renAmt != -1 && amtCharged != -1
											&& amtCharged < renAmt)
										sendSelectionSMS(
												strSubID,
												getParseSMS(
														chargeSms
																.getPrepaidNEFSuccess(),
														amtCharged,
														(renAmt - amtCharged)),
												createTime, songName, callerNo,
												null, null, reason, movieName);
									else
										sendSelectionSMS(
												strSubID,
												chargeSms
														.getPrepaidRenewalSuccess(),
												createTime, songName, callerNo,
												null, null, reason, movieName);
								} else
									sendSelectionSMS(
											strSubID,
											chargeSms
													.getPostpaidRenewalSuccess(),
											createTime, songName, callerNo,
											null, null, reason, movieName);
							}
						}
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"0",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						finalResponse = SUCCESS;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return INVALID;
					} else {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return FAILURE;
					}
				}
			} else if (m_statusActGrace.equalsIgnoreCase(status)
					&& m_strActionActivation.equalsIgnoreCase(action)) {		//SELECTION Activation GRACE callback
				isImmediate = false;
				logger.info("Processing Grace Activation callback."
						+ " SubscriberId: " + strSubID);
				if (selStatus == null) {
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.selectedBy(), "402",
							"Customization", clip,
							getCriteria(songStatus, callerID, subscriberStatus));
					return SELECTION_NOT_EXISTS;
				} else if (!selStatus.equals("N") && !selStatus.equals("A")) {
					if (selStatus.equals("G")) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_ALREADY_ON_GRACE;
					} else if (selStatus.equals("B")) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_ALREADY_ACTIVE;
					} else if (selStatus.equals("X")) { 
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.deSelectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_DEACTIVE;
					} else if (m_deActStatus.contains(selStatus)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.deSelectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_DCT_PENDING;
					}
				}

				if (oldLoopStatus == LOOP_STATUS_LOOP_FINAL
						|| oldLoopStatus == LOOP_STATUS_LOOP_INIT
						|| oldLoopStatus == LOOP_STATUS_LOOP)
					oldLoopStatus = LOOP_STATUS_LOOP_INIT;
				else
					oldLoopStatus = LOOP_STATUS_OVERRIDE_INIT;

				// If the latest selections goes into GRACE, then deactivate
				// previous suspended selections.
				if (RBTParametersUtils.getParamAsBoolean("DAEMON",
						"DEACT_SUSPENDED_SEL_ON_GRACE_CALLBACK", "FALSE")
						&& oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT) {
					m_rbtDBManager.smDeactivateOldSuspendedSelections(strSubID,
							callerID, songStatus, setTime, fromTime, toTime,
							rbtType, subscriberStatus.selInterval(), refID);
				}
				circleIDFromPrism = isCircleIdMatchedWithSub(circleIDFromPrism,
						subscriberStatus);
				String ret = smSelectionGrace(strSubID, refID, type, rbtType,
						oldLoopStatus,circleIDFromPrism);
				logger.info("Selection Grace database update status: " + ret);
				if (ret.equalsIgnoreCase(m_success)) {
					if (RBTEventLogger
							.isEventLoggingEnabled(RBTEventLogger.Event.TLOG)) {
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.S");
						Date curDate = new Date();
						String subscriberType = subscriberStatus.prepaidYes() ? "P"
								: "B";

						StringBuilder tLogBuilder = new StringBuilder();
						tLogBuilder.append(subscriber.circleID()).append("|")
								.append(strSubID).append("|")
								.append(subscriberType);
						tLogBuilder.append("|").append(sbnID).append("|")
								.append("RBT_SEL_").append(classType);
						tLogBuilder
								.append("|")
								.append("A")
								.append("||0|")
								.append(subscriberStatus.deSelectedBy())
								.append("|mmp|")
								.append(format.format(subscriberStatus
										.setTime()));
						tLogBuilder.append("|").append(format.format(curDate))
								.append("||0|C|0|||N|||||");

						RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG,
								tLogBuilder.toString());
					}

					String selGraceSms = getParamAsString("SEL_GRACE_SMS");
					if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
							&& selGraceSms != null && selGraceSms.length() > 0)
						sendSelectionSMS(strSubID, selGraceSms, createTime,
								songName, callerNo, null, null, reason,
								movieName);
					
					//Airtel online url
					hitOnlineURL(strSubID, classType, status, subscriberStatus.selectedBy(), 
							amountCharged, subscriberStatus.subscriberFile(), "SEL", refID);
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.selectedBy(), "0",
							"Customization", clip, null);
					finalResponse = SUCCESS;
					return SUCCESS;
				}

				else if (ret.equalsIgnoreCase(m_failure)) {
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.selectedBy(), "402",
							"Customization", clip,
							getCriteria(songStatus, callerID, subscriberStatus));
					return INVALID;
				} else {
					writeEventLog(extraInfo, strSubID,
							subscriberStatus.selectedBy(), "402",
							"Customization", clip,
							getCriteria(songStatus, callerID, subscriberStatus));
					return FAILURE;
				}
			} else if (m_strSuspendSubscription.equalsIgnoreCase(action)
					&& !m_statusSuccess.equalsIgnoreCase(status)) {
				logger.info("Processing Suspend Success callback. "
						+ "subscriberID: " + strSubID + ", refID: " + refID);
				if (selStatus == null)
					return SELECTION_NOT_EXISTS;
				else if (!selStatus.equals("B")) {
					if (selStatus.equals("Z"))
						return SELECTION_ALREADY_SUSPENDED;
					if (selStatus.equals("A") || selStatus.equals("N")
							|| selStatus.equals("G"))
						return SELECTION_ACT_PENDING;
					else if (selStatus.equals("X"))
						return SELECTION_ALREADY_DEACTIVE;
				}
				createTime = getCreateTime();
				circleIDFromPrism = isCircleIdMatchedWithSub(circleIDFromPrism,
						subscriberStatus);
				String ret = smSelectionSuspend(strSubID, refID, oldLoopStatus,
						rbtType,circleIDFromPrism);
				if (ret.equalsIgnoreCase(m_success)) {
					if (!isAddToDownloads && getParamAsString(COMMON, SUSPEND_INTRO_PRE_PROMPT_FLAG_IN_CALLBACK, "FALSE")
							.equalsIgnoreCase("TRUE")) {
						// VDE-2730 Start : Remove Suspended pre-prompt flag
						boolean updateUserInfo = false;
						if (subscriber != null) {
							Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
							if (extraInfoMap == null) {
								extraInfoMap = new HashMap<String, String>();
								updateUserInfo = true;
							} else if (null == extraInfoMap.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
									|| (null != extraInfoMap.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
											&& !extraInfoMap.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
													.equalsIgnoreCase(iRBTConstant.ENABLE_PRESS_STAR_INTRO_SUSPEND))) {
								updateUserInfo = true;
							}
							if (updateUserInfo) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG,
										iRBTConstant.ENABLE_PRESS_STAR_INTRO_SUSPEND);
								String extrInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
								m_rbtDBManager.updateExtraInfoAndPlayerStatus(subscriber.subID(), extrInfo, "A");
							}
						}
						// VDE-2730 End : Remove Suspended pre-prompt flag
					}
					logger.debug("Successfully updated selection status for"
							+ " selection suspend success. subscriber: "
							+ strSubID + ", selection refId: " + refID);

					if (!isAddToDownloads) {
						int selectionCatType = subscriberStatus.categoryType();
						int selectionCatId = subscriberStatus.categoryID();

						logger.debug(" categoryType: " + selectionCatType
								+ ", categoryId: " + selectionCatId);
						// Azaan for selections
						if (selectionCatType == iRBTConstant.SHUFFLE
								&& confAzaanCategoryIdList.contains(String
										.valueOf(selectionCatId))) {
							String subExtraInfo = subscriber.extraInfo();
							HashMap<String, String> subextraInfoMap = DBUtility
									.getAttributeMapFromXML(subExtraInfo);
							removeAzaanSubType(strSubID, subextraInfoMap);
						}
					}

					if (RBTEventLogger
							.isEventLoggingEnabled(RBTEventLogger.Event.TLOG)) {
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.S");
						Date curDate = new Date();
						String subscriberType = subscriberStatus.prepaidYes() ? "P"
								: "B";

						StringBuilder tLogBuilder = new StringBuilder();
						tLogBuilder.append(subscriber.circleID()).append("|")
								.append(strSubID).append("|")
								.append(subscriberType);
						tLogBuilder.append("|").append(sbnID).append("|")
								.append("RBT_SEL_").append(classType);
						tLogBuilder
								.append("|")
								.append("R")
								.append("||0|")
								.append(subscriberStatus.deSelectedBy())
								.append("|mmp|")
								.append(format.format(subscriberStatus
										.setTime()));
						tLogBuilder.append("|").append(format.format(curDate))
								.append("||0|C|0|||M|||||");

						RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG,
								tLogBuilder.toString());
					}

					String selSuspendSms = getParamAsString("SEL_SUSPEND_SMS");
					if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
							&& selSuspendSms != null
							&& selSuspendSms.length() > 0)
						sendSelectionSMS(strSubID, selSuspendSms, createTime,
								songName, callerNo, null, null, reason,
								movieName);
					
					//Airtel online url
					hitOnlineURL(strSubID, classType, status, subscriberStatus.selectedBy(), 
							amountCharged, subscriberStatus.subscriberFile(), "SEL", refID);
					finalResponse = SUCCESS;
					return SUCCESS;
				} else if (ret.equalsIgnoreCase(m_failure))
					return INVALID;
				else
					return FAILURE;
			} else {
				isImmediate = false;
				if (m_strActionDeactivation.equalsIgnoreCase(action)) {
					logger.info("Processing Deactivation callback. refID: "
							+ refID + ", subscriberId: " + strSubID);
					if (selStatus == null) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_NOT_EXISTS;
					} else if (!selStatus.equals("D") && !selStatus.equals("P")) {
						if (selStatus.equals("X")) {
							writeEventLog(
									extraInfo,
									strSubID,
									subscriberStatus.deSelectedBy(),
									"402",
									"Customization",
									clip,
									getCriteria(songStatus, callerID,
											subscriberStatus));
							return SELECTION_ALREADY_DEACTIVE;
						} else if (m_actStatus.contains(selStatus)) {
							writeEventLog(
									extraInfo,
									strSubID,
									subscriberStatus.selectedBy(),
									"402",
									"Customization",
									clip,
									getCriteria(songStatus, callerID,
											subscriberStatus));
							return SELECTION_ACTIVE;
						}
					}
					// here we dont need to update player
					if (reason != null && reason.trim().length() > 0
							&& !reason.trim().equalsIgnoreCase("null"))
						extraInfo = DBUtility.setXMLAttribute(extraInfo,
								EXTRA_INFO_FAILURE_MESSAGE, reason);
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					String ret = smSelectionDeactivationFailure(strSubID,
							refID, type, rbtType, extraInfo, circleIDFromPrism);
					if (ret.equalsIgnoreCase(m_success)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.deSelectedBy(),
								"0",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						finalResponse = SUCCESS;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.deSelectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return INVALID;
					} else {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.deSelectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return FAILURE;
					}
				} else if (m_strActionTrigger.equalsIgnoreCase(action)) {
					isImmediate = true;
					if (selStatus == null)
						return SELECTION_NOT_EXISTS;
					else if (!selStatus.equals("B")) {
						if (selStatus.equals("Z"))
							return SELECTION_ALREADY_SUSPENDED;
						if (selStatus.equals("A") || selStatus.equals("N")
								|| selStatus.equals("G"))
							return SELECTION_ACT_PENDING;
						else if (selStatus.equals("X"))
							return SELECTION_ALREADY_DEACTIVE;
					}
					createTime = getCreateTime();
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					String ret = smSelectionSuspend(strSubID, refID,
							oldLoopStatus, rbtType,circleIDFromPrism);
					if (ret.equalsIgnoreCase(m_success)) {
						String selSuspendSms = getParamAsString("SEL_SUSPEND_SMS");
						if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
								&& selSuspendSms != null
								&& selSuspendSms.length() > 0)
							sendSelectionSMS(strSubID, selSuspendSms,
									createTime, songName, callerNo, null, null,
									reason, movieName);
						finalResponse = SUCCESS;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure))
						return INVALID;
					else
						return FAILURE;
				} else if (m_strUpgradeSubscription.equalsIgnoreCase(action)) {
					logger.debug("Processing Upgrade Selection Failure callback. refID: "
							+ refID + ", subscriberId: " + strSubID);
					String oldClassType = subscriberStatus.oldClassType();
					logger.info("oldClassType : " + oldClassType);
					if (selStatus.equalsIgnoreCase("B"))
						return CALLBACK_ALREADY_RECEIVED;
					else if (selStatus.equalsIgnoreCase("A"))
						return SELECTION_ACTIVE;
					else if ((selStatus.equalsIgnoreCase("N") || selStatus
							.equalsIgnoreCase("E")) && oldClassType == null)
						return SELECTION_ACT_PENDING;
					else if (selStatus.equalsIgnoreCase("D")
							|| selStatus.equalsIgnoreCase("P")
							|| selStatus.equalsIgnoreCase("F"))
						return SELECTION_DCT_PENDING;
					else if (selStatus.equalsIgnoreCase("X"))
						return SELECTION_DEACTIVE;
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					String loopStatus = null;
					if (subscriberStatus.loopStatus() == 'B')
						loopStatus = "O";
					else if (subscriberStatus.loopStatus() == 'A')
						loopStatus = "L";
					String ret = m_rbtDBManager.smSelectionUpgradationCallback(
							strSubID, refID, classType,
							subscriberStatus.oldClassType(), status, circleIDFromPrism,loopStatus);
					finalResponse = ret;
					return ret;
				} else {
					boolean bRenewal = false;
					String deactBy = "AF";
					//RBT-12906- Resubscription callback
					if (sys_mode != null
							&& !sys_mode.equals(SYSTEM_MODE)
							&& m_strActionRental.equalsIgnoreCase(action)
							&& m_statusFailure.equalsIgnoreCase(status)
							&& (smInitiatedDeactAllowedModes.contains(sys_mode)
									||  null == subscriber || subscriber.subYes().equalsIgnoreCase("X")))
						deactBy = sys_mode;
					else if (m_strActionRental.equalsIgnoreCase(action)) {
						bRenewal = true;
						if (getParamAsBoolean("NEF_DEACTIVATIONS", "FALSE")
								&& (type != null && type.trim()
										.equalsIgnoreCase("P")))
							deactBy = "NEF";
						else
							deactBy = "RF";
					} else if (m_strActionActivation.equalsIgnoreCase(action)
							|| m_strActionEvent.equalsIgnoreCase(action)) {
						if (getParamAsBoolean("NEF_DEACTIVATIONS", "FALSE")
								&& (type != null && type.trim()
										.equalsIgnoreCase("P")))
							deactBy = "NA";
					}

					if (selStatus == null) {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return SELECTION_NOT_EXISTS;
					} else if (m_strActionActivation.equalsIgnoreCase(action)
							|| m_strActionEvent.equalsIgnoreCase(action)) {					//Selection Activation Failure Callback
						if (!selStatus.equals("N") && !selStatus.equals("A")
								&& !selStatus.equals("G")) {
							if (selStatus.equals("B")) {
								writeEventLog(
										extraInfo,
										strSubID,
										subscriberStatus.selectedBy(),
										"402",
										"Customization",
										clip,
										getCriteria(songStatus, callerID,
												subscriberStatus));
								return SELECTION_ALREADY_ACTIVE;
							} else if (m_deActStatus.contains(selStatus)) {
								if (m_deActStatus.contains(selStatus)
										&& getFormattedDate(sdf,
												subscriberStatus.startTime())
												.startsWith("20040101")) {
									char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
									if (oldLoopStatus == LOOP_STATUS_EXPIRED)
										newLoopStatus = oldLoopStatus;
									else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT
											|| oldLoopStatus == LOOP_STATUS_LOOP_INIT)
										newLoopStatus = LOOP_STATUS_EXPIRED;
									String ret = m_rbtDBManager
											.smUpdateDeactiveSelectionFailure(
													strSubID, refID, type,
													classType, deactBy,
													newLoopStatus, rbtType);

									if (ret == null
											|| (!ret.equalsIgnoreCase(m_success) && !ret
													.equalsIgnoreCase(m_failure))) {
										writeEventLog(
												extraInfo,
												strSubID,
												subscriberStatus.deSelectedBy(),
												"402",
												"Customization",
												clip,
												getCriteria(songStatus,
														callerID,
														subscriberStatus));
										return FAILURE;
									}
									writeEventLog(
											extraInfo,
											strSubID,
											subscriberStatus.deSelectedBy(),
											"0",
											"Customization",
											clip,
											getCriteria(songStatus, callerID,
													subscriberStatus));
									finalResponse = SUCCESS;
									return SUCCESS;
								} else if (selStatus.equals("X")) {
									return SELECTION_ALREADY_DEACTIVE;
								} else {
									return SELECTION_DCT_PENDING;
								}
								
							}
						}
					} else if (m_strActionRental.equalsIgnoreCase(action)) {				//Selection Renewal Failure Callback
						isImmediate = true;
						logger.info("Processing Rental callback.");
						bRenewal = true;
						if (!selStatus.equals("B")) {
							if ((selStatus.equals("A") || selStatus.equals("N"))
									&& !getParamAsBoolean(
											"DAEMON",
											"DCT_SYSTEM_CALLBACK_FOR_ACT_PENDING_ALLOWED",
											"FALSE"))
								return SELECTION_ACT_PENDING;
							else if (selStatus.equals("G")) {
								if (!getParamAsBoolean(
										"DAEMON",
										"DCT-SYSTEM_CALLBACK_FOR_GRACE_ALLOWED",
										"FALSE")) {
									return SELECTION_ACT_PENDING;
								}
							} else if (selStatus.equals("X"))
								return SELECTION_ALREADY_DEACTIVE;
						}
						
						createTime = getCreateTime();
					}
					// Added boolean isRenewal and extraInfo params
					if (!bRenewal && extraInfo != null
							&& extraInfo.contains(REFUND))
						extraInfo = DBUtility.removeXMLAttribute(extraInfo,
								REFUND);
					if (reason != null && reason.trim().length() > 0
							&& !reason.trim().equalsIgnoreCase("null"))
						extraInfo = DBUtility.setXMLAttribute(extraInfo,
								EXTRA_INFO_FAILURE_MESSAGE, reason);
					circleIDFromPrism = isCircleIdMatchedWithSub(
							circleIDFromPrism, subscriberStatus);
					String ret = smSelectionActivationRenewalFailure(strSubID,
							refID, deactBy, type, classType, oldLoopStatus,
							rbtType, extraInfo, clip, circleIDFromPrism);

					if (catType == 11)
						smDeactivateOtherUGSSelections(strSubID, callerID,
								type, rbtType);
					logger.info("Processed selection activation renewal failure."
							+ ", refID: "
							+ refID
							+ ", subscriberId: "
							+ strSubID + " Response: " + ret);
					if (ret.equalsIgnoreCase(m_success)) {//RBT-14671 - # like
						if (hashLikeKeys != null && !hashLikeKeys.isEmpty()) {
							try {
								m_rbtDBManager.deleteSubscriberLikedSong(strSubID,
										(!Utility.isShuffleCategory(subscriberStatus
												.categoryType()) ? clip.getClipId()
												: -1), subscriberStatus
												.categoryID());
							} catch (Exception e) {
								logger.info("Table is not there in the data base for like: "
										+ e.getMessage());
							}
						}
						logger.debug("Successfully updated selection status for"
								+ " selection renewal failure. refID: "
								+ refID
								+ ", subscriberId: " + strSubID);
						
						if (!isAddToDownloads) {
							//RBT-13163: MP Non MP TEF/VF/OR Spain 
							MpNonMpFeature mpNonMpFeature = MpNonMpFeature.getMpNonMpFeatureClassInstance();
							if (mpNonMpFeature != null) {
								logger.debug("MP Non MP feature enabled.");
								MpNonMpFeatureBean mpNonMpFeatureBean = new MpNonMpFeatureBean(strSubID);
								mpNonMpFeatureBean.setSubscriber(subscriber);
								mpNonMpFeature.checkAndDeactivateMusicPack(mpNonMpFeatureBean);
							}
							
							int selectionCatType = subscriberStatus
									.categoryType();
							int selectionCatId = subscriberStatus.categoryID();
							logger.debug("categoryType: " + selectionCatType
									+ ", categoryId: " + selectionCatId);
							// Azaan for selections
							if (selectionCatType == iRBTConstant.SHUFFLE
									&& confAzaanCategoryIdList.contains(String
											.valueOf(selectionCatId))) {
								String subExtraInfo = subscriber.extraInfo();
								HashMap<String, String> subextraInfoMap = DBUtility
										.getAttributeMapFromXML(subExtraInfo);
								removeAzaanSubType(strSubID, subextraInfoMap);
							}
						}

						// Do the base deactivation - RBT-7908
						deactiveSubscriberNoActiveSelection(subscriber, type);

						if (isEnablePrePromptIfNoSel) {
							resetFreeSongBasedOnCircle(subscriber, refID);
						}

						String result = "FAILURE";
						if (m_strActionActivation.equalsIgnoreCase(action)
								|| m_strActionEvent.equalsIgnoreCase(action))
							if (getParamAsBoolean("COMMON", "ADD_TO_DOWNLOADS"))
								m_rbtDBManager.updateDownloadStatus(strSubID,
										subscriberStatus.subscriberFile(), 'x');
						SubscriberStatus[] subscriberStatus1 = null;
						if (getParamAsBoolean("NO_SEL_DEACT_SUB", "FALSE")
								|| getParamAsBoolean(
										"NO_SEL_DEACT_SUB_IN_DAYS", "FALSE"))
							subscriberStatus1 = smSubscriberRecords(strSubID,
									rbtType);
						if ((getParamAsBoolean("NO_SEL_DEACT_SUB_IN_DAYS",
								"FALSE") || getParamAsBoolean(
								"NO_SEL_DEACT_SUB", "FALSE"))
								&& subscriberStatus1 == null) {
							if (getParamAsBoolean("NO_SEL_DEACT_SUB_IN_DAYS",
									"FALSE")) {
								Date next = getNextDate("M2");
								updateEndDate(strSubID, next,
										m_subClassMoveOptIn);
							} else

								deactivateSubscriber(strSubID, deactBy, type);
						}

						if (deactBy.equalsIgnoreCase("NA")
								|| deactBy.equalsIgnoreCase("AF")) {
							String selectedBy = subscriberStatus.selectedBy();
							if ((getParamAsBoolean("SEND_SMS_ON_CHARGE",
									"FALSE") || getParamAsBoolean(
									"SEND_SMS_CROSS_PROMO", "FALSE"))
									&& chargeClass != null) {
								// RENEWAL FAILURE
								boolean isRenewalRequest = false;
								if (chargeSms == null) {

									if (chargeClass.getSmschargeFailure() != null
											&& !chargeClass
													.getSmschargeFailure()
													.equalsIgnoreCase("null"))
										sendSelectionSMSCrossPromo(subscriber,
												chargeClass
														.getSmschargeFailure(),
												createTime, songName, callerNo,
												null, null, clipId,
												clipPromoId, result,
												songStatus, catType,
												selectedBy, movieName,
												isRenewalRequest,
												chargeClass.getChargeClass());
								} else {
									String smsText = (type != null
											&& type.equalsIgnoreCase("p") ? chargeSms
											.getPrepaidFailure() : chargeSms
											.getPostpaidFailure());
									sendSelectionSMSCrossPromo(subscriber,
											smsText, createTime, songName,
											callerNo, null, null, clipId,
											clipPromoId, result, songStatus,
											catType, selectedBy, movieName,
											isRenewalRequest,
											chargeClass.getChargeClass());
								}
							}

						} else if (deactBy.equalsIgnoreCase("NEF")
								|| deactBy.equalsIgnoreCase("RF")) {
							if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
									&& chargeClass != null) {
								if (chargeSms == null) {
									if (chargeClass.getSmsrenewalFailure() != null
											&& !chargeClass
													.getSmsrenewalFailure()
													.equalsIgnoreCase("null"))
										sendSelectionSMS(
												strSubID,
												chargeClass
														.getSmsrenewalFailure(),
												createTime, songName, callerNo,
												null, null, reason, movieName);
								} else {
									String smsText = (type != null
											&& type.equalsIgnoreCase("p") ? chargeSms
											.getPrepaidRenewalFailure()
											: chargeSms
													.getPostpaidRenewalSuccess());
									sendSelectionSMS(strSubID, smsText,
											createTime, songName, callerNo,
											null, null, reason, movieName);
								}
							}
						}
						if (isTrialReminderEnabled(subscriber)
								&& isTrialReminderPack(subscriberStatus
										.classType()))
							m_rbtDBManager
									.deleteTrialSelectionReminder(subscriberStatus
											.subID());

						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"0",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						finalResponse = SUCCESS;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {						
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return INVALID;
					} else {
						writeEventLog(
								extraInfo,
								strSubID,
								subscriberStatus.selectedBy(),
								"402",
								"Customization",
								clip,
								getCriteria(songStatus, callerID,
										subscriberStatus));
						return FAILURE;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		} finally {
			RBTMonitorManager.getInstance().endNode(strSubID, node, status);
			try {
				if(subscriberStatus != null)
					performInlineAction(subscriberStatus, finalResponse, SUCCESS, isImmediate);
			} catch(Throwable t) {
				logger.warn("Exception while performing inline action: " + t);
			}
		}
	}
	
	public void performInlineAction(SubscriberStatus subscriberStatus, String finalResponse, String success, boolean isImmediate) {
		String inlineFlow = com.onmobile.apps.ringbacktones.services.common.Utility.getInlineFlow(subscriberStatus.classType(), 3);
		if(inlineFlow != null) {
			m_rbtDBManager.updateSubscriberSelectionInlineDaemonFlag(subscriberStatus.subID(), subscriberStatus.refID(), null);
			if(!(isImmediate && finalResponse.equalsIgnoreCase(success)))
				return;
			
			SubscriberStatus ss = m_rbtDBManager.getSelectionBySubIdRefId(subscriberStatus.subID(), subscriberStatus.refID());
			if(ss == null) {
				logger.info("No selection found for inline: " + subscriberStatus);
				return;
			}
			String tpFlow = null;
			char loopStatus = ss.loopStatus();
			if(loopStatus == LOOP_STATUS_EXPIRED_INIT)
				tpFlow = WebServiceConstants.PROVISIONING_TPDCT;
			else if(loopStatus == LOOP_STATUS_LOOP || loopStatus == LOOP_STATUS_OVERRIDE)
				tpFlow = WebServiceConstants.PROVISIONING_TPACT;
			
			Subscriber sub = m_rbtDBManager.getSubscriber(subscriberStatus.subID());
			if((loopStatus == LOOP_STATUS_LOOP || loopStatus == LOOP_STATUS_OVERRIDE) && com.onmobile.apps.ringbacktones.services.common.Utility.isD2CModel())
				initiateClipTransfer(sub, ss);

			//TODO: STATE_ACTIVATED check is required? check all possible callbacks from SM, if state changed in db then subscriberStatus will help instead of ss?
			//or do flag null irrespective of Sel status state
			if(tpFlow == null)
				return;
			
			if (sub.subYes().equals(iRBTConstant.STATE_ACTIVATED)) {
				com.onmobile.apps.ringbacktones.services.common.Utility.sendInlineMessage(ss.subID(), ss.refID(), null, null, tpFlow);
				if(logger.isDebugEnabled()) {
					logger.debug("Inline TP update is initiated for: " + subscriberStatus);
				}
			}
		} else if(com.onmobile.apps.ringbacktones.services.common.Utility.isD2CModel() && isImmediate && finalResponse.equalsIgnoreCase(success)) {
			SubscriberStatus ss = m_rbtDBManager.getSelectionBySubIdRefId(subscriberStatus.subID(), subscriberStatus.refID());
			if(ss == null) {
				logger.info("No selection found for immediate TP/Clip transfer: " + subscriberStatus);
				return;
			}
			if(ss.loopStatus() == LOOP_STATUS_LOOP || ss.loopStatus() == LOOP_STATUS_OVERRIDE) {
				Subscriber sub = m_rbtDBManager.getSubscriber(subscriberStatus.subID());
				initiateClipTransfer(sub, ss);
			}
		}
	}
	
 	private void initiateClipTransfer(Subscriber sub, SubscriberStatus ss) {
		try {
			String operatorName = ServiceUtil.getOperatorName(sub).trim();
			String circleId = ServiceUtil.getCircleId(sub).trim();
			Map<String, Integer> map = ProcessingClipTransfer.getOperatorcirclemap();
			Integer operatorId = map.get((operatorName+"_"+circleId).toUpperCase());
			if(operatorId == null)
				throw new OnMobileException("Operator circlemapping " + (operatorName+"_"+circleId).toUpperCase() + "not exist");

			ClipUtilService clipUtilServicBean = null;
			try {
				clipUtilServicBean = (ClipUtilService) ConfigUtil
						.getBean(BeanConstant.CLIP_UTIL_SERVICE_MAPPING_BEAN);
			} catch (Exception e) {
				logger.error("Bean is not configured: ", e);
				clipUtilServicBean = new ClipUtilService();
			}
			if (clipUtilServicBean != null) {
				clipUtilServicBean.checkIfClipExistsAndUpdateMapping(sub, ss.subscriberFile(), ss.categoryType(), ss.categoryID(), operatorId);
			}
		} catch(Throwable t) {
			logger.error("Clip transfer via D2C model is not done: " + t);
		}
	}

	public String isCircleIdMatchedWithSub(String circleIDFromPrism,
			SubscriberStatus subscriberStatus) {
		// RBT-14301: Uninor MNP changes.
		if (subscriberStatus != null) {
			String circleId = subscriberStatus.circleId();
			if (circleId != null
					&& circleId
							.equalsIgnoreCase(circleIDFromPrism)) {
				circleIDFromPrism = null;
			}
		}
		return circleIDFromPrism;
	}

	/**
	 * Based on contenty type pack will be activated, if activation mode, and content type - cosid mapping is configured.
	 * pack will activated with configured mode by default DAEMON
	 * if activation mode is not configure / content type - cosid mapping is not configured / cos is not exists / pack already activated then pack will not be active   
	 * @param subscriber
	 * @param clip
	 * @param mode
	 */
	private void activateFreeAzaanForContent(Subscriber subscriber, Clip clip, String mode) {
		if (clip == null) {
			return;
		}
		logger.info("calling the activateFreeAzaanForContent method with clipId"+ clip.getClipId() +" mode :" + mode);
		logger.info("mode is :" + mode +" azzanConfMode :" + modeBasedPackActConfig);
		
		//Mode is not configured, will not activate the pack based on content type
		if (!azzanConfModeList.contains(mode.toUpperCase())) {
			return;
		}
		logger.info("azzanConf is configured for this mode so entering condition:");

		List<String> contentTypeList = Arrays.asList(contentTypeCosIDMapString.toUpperCase().split(";"));//ISLAMIC,1;ARABIC,2
		logger.info("contentTypeCosIDMapString got with uppercase is:" + contentTypeCosIDMapString);
		HashMap<String, String> contentTypeCosIDMap = new HashMap<String, String>();
		for (String string : contentTypeList) {
			String[] mapEntry = string.split(",");// null
			contentTypeCosIDMap.put(mapEntry[0], mapEntry[1]);//ISLAMIC,1
		}
		String downloadContentType = clip.getContentType().toUpperCase();
		logger.info("got the contentTypeCosIDMap size :" + contentTypeCosIDMap.size() +" and downloadContentType is:" +downloadContentType);
		if(contentTypeCosIDMap.containsKey(downloadContentType)){
			CosDetails cosDetails = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(
							contentTypeCosIDMap.get(downloadContentType));
			logger.info("cosDetails got for id :" + contentTypeCosIDMap.get(downloadContentType) + " is :" +cosDetails);
			if(cosDetails != null) {
				SelectionRequest selectionRequest = new SelectionRequest(subscriber.subID());
				selectionRequest.setCosID(Integer.parseInt(cosDetails.getCosId()));
				selectionRequest.setMode(modeForFreeAzaan);
				RBTClient.getInstance().upgradeSelectionPack(selectionRequest);
				logger.info("the azzan activation response :" + selectionRequest.getResponse());
			}
		}
	}

	private void callBI(String strSubID, String songName, String promoId,
			int clipId, String biUrl) {
		if (null != biUrl) {
			logger.info("Calling BI with parameters. strSubID: " + strSubID
					+ ", clipId: " + clipId + ", songName: " + songName
					+ ", promoId: " + promoId + ", biUrl: " + biUrl);

			biUrl = biUrl.replaceAll("%MSISDN%", strSubID);

			if (clipId != -1) {
				biUrl = biUrl.replaceAll("%CLIPID%", String.valueOf(clipId));
			} else {
				biUrl = biUrl.replaceAll("%CLIPID%", "");
			}

			if (null != songName) {
				songName = songName.replaceAll("\\s", "+");
				biUrl = biUrl.replaceAll("%CLIPNAME%", songName);
			} else {
				biUrl = biUrl.replaceAll("%CLIPNAME%", "");
			}

			if (null != promoId) {
				biUrl = biUrl.replaceAll("%PROMOID%", promoId);
			} else {
				biUrl = biUrl.replaceAll("%PROMOID%", "");
			}

			String response = makeHttpRequest(biUrl);

			logger.info("Successfully made BI hit for url: " + biUrl
					+ ", response: " + response);
		}
	}

	private boolean isNoDownloadDeactSub(Subscriber subscriber) {
		
		return Utility.isNoDownloadDeactSub(subscriber);
		
	}

	private String getChargeInfo(String strSubID, int clipId, int categoryId) {

		SelectionRequest selectionRequestObj =  new SelectionRequest(strSubID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeclass = null;
		if(selectionRequestObj!=null){
			selectionRequestObj.setClipID(""+clipId);
			selectionRequestObj.setCategoryID(""+categoryId);
			chargeclass = RBTClient.getInstance().getNextChargeClass(selectionRequestObj);
		}
		
		RbtDetailsRequest rbtDetReq = new RbtDetailsRequest(null);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = null;
		
		if(rbtDetReq!=null){
			rbtDetReq.setMode("CCC");
			rbtDetReq.setSubscriberID(strSubID);

			subscriber = RBTClient.getInstance().getSubscriber(rbtDetReq);
		}
		
		SubscriptionClass subscriptionclass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriber.getSubscriptionClass());
				
		String chargeClassStr = null;
		String chargePeriod = null;
		String chargeAmount = null;
		String subscriptionAmount = null;
		String subscriptionPeriod = null;
		Date nextChargingDate = null;
		Date nextBillingDate = null;
		if(chargeclass!=null){
			chargeClassStr = chargeclass.getChargeClass();
			chargePeriod = chargeclass.getPeriod();
			chargeAmount = chargeclass.getAmount();
		}
		if(subscriptionclass!=null){
			subscriptionAmount = subscriptionclass.getSubscriptionAmount();
			subscriptionPeriod = subscriptionclass.getSubscriptionPeriod();
		}
		DateFormat dfmt1  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String lastChargingDateStr = null;
		String nextChargingDateStr = null;
		if(subscriber!=null){
			nextChargingDate = subscriber.getNextChargingDate();
			nextBillingDate = subscriber.getNextBillingDate(); 
			try {
				if(nextChargingDate!=null)	
					lastChargingDateStr = dfmt1.format(nextChargingDate);
				if(nextBillingDate!=null)
					nextChargingDateStr = dfmt1.format(nextBillingDate);
			} catch(Exception e) {
				logger.error("Got exception ", e);
			}
		}
		
		String chargeInfo = "chargeClass="+ chargeClassStr +"|"+"chargePeriod="+ chargePeriod +"|"+"chargeAmount="+ chargeAmount+"|"+"subAmount="+ subscriptionAmount+"|"+"subPeriod="+subscriptionPeriod+"|"+"lastChargingDate=" + lastChargingDateStr +"|"+"nextChargingDate=" + nextChargingDateStr;
		
		return chargeInfo;
	}

	private void copyInfluencerContest(String strSubID,
			String contestCallerID)
			throws OnMobileException, IOException, HttpException {
		logger.info("Input parameters. strSubID: " + strSubID
				+ ", contestCallerID: " + contestCallerID);
		if(null == contestCallerID) {
			logger.warn("Not processing copy influencer contest. contestCallerId is null.");
			return;
		}
		if (copyContestIDsTimeValidityMap.size() == 0) {
			initCopyContestIDsTimeMap();
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				contestCallerID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber contestCaller = RBTClient
				.getInstance().getSubscriber(rbtDetailsRequest);
		RbtDetailsRequest rbtDetailsRequest1 = new RbtDetailsRequest(strSubID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber contestSubscriber = RBTClient
				.getInstance().getSubscriber(rbtDetailsRequest1);
		String contestCallerCircleID = null;
		String contestCallerIDSiteUrl = null;
		boolean isValidCallerID = false;
		if (contestCaller != null) {
			contestCallerCircleID = contestCaller.getCircleID();
			isValidCallerID = contestCaller.isValidPrefix();
		}
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager()
				.getSitePrefixes(contestCallerCircleID);
		contestCallerIDSiteUrl = (sitePrefix != null) ? sitePrefix.getSiteUrl()
				: null;
		if (contestCallerIDSiteUrl!=null && contestCallerIDSiteUrl.indexOf("/rbt/") != -1) {
			contestCallerIDSiteUrl = contestCallerIDSiteUrl != null ? contestCallerIDSiteUrl
					.substring(0, contestCallerIDSiteUrl.indexOf("/rbt/"))
					: null;
		}
		logger.info("RBTDaemonHelper:: isValidCallerID: " + isValidCallerID
				+ ", contestCallerCircleID: " + contestCallerCircleID
				+ ", contestCallerIDSiteUrl: " + contestCallerIDSiteUrl);
		if (isValidCallerID) {
			if (contestCaller != null
					&& Utility.isUserActive(contestCaller.getStatus())) {
				HashMap<String, String> contestCallerExtraInfoMap = contestCaller
						.getUserInfoMap();
				int noOfTimesSongCopied = 0;
				if (contestCallerExtraInfoMap != null
						&& contestCallerExtraInfoMap
								.containsKey("NO_OF_COPIES")) {
					String noOfCopies = contestCallerExtraInfoMap
							.get("NO_OF_COPIES");
					noOfTimesSongCopied = Integer.parseInt(noOfCopies);
				} else if (contestCallerExtraInfoMap == null) {
					contestCallerExtraInfoMap = new HashMap<String, String>();
				}
				String contestID = contestCallerExtraInfoMap.get("CONTEST_ID");
				boolean isValidContestID = true;
				boolean isCircleLevelContest = false;
				String nationalContestIDs = RBTParametersUtils
						.getParamAsString("COMMON", "ALL_CONTEST_IDS", null);
				List<String> nationalContestIDsList = null;
				if (nationalContestIDs != null) {
					nationalContestIDsList = Arrays.asList(nationalContestIDs
							.split(","));
				}
				if (nationalContestIDsList == null
						|| !nationalContestIDsList.contains(contestID)) {
					String circleContestIDs = RBTParametersUtils
							.getParamAsString("COMMON", contestCallerCircleID
									+ "_CONTEST_IDS", null);
					List<String> circleContestIDsList = null;
					if (circleContestIDs != null)
						circleContestIDsList = Arrays.asList(circleContestIDs
								.split(","));
					if (circleContestIDsList == null
							|| !circleContestIDsList.contains(contestID)) {
						isValidContestID = false;
					}
					if (circleContestIDsList != null
							&& circleContestIDsList.contains(contestID))
						isCircleLevelContest = true;
				}
				if (isCircleLevelContest) {
					if (!contestSubscriber.getCircleID().equalsIgnoreCase(
							contestCaller.getCircleID()))
						isValidContestID = false;
				}
				boolean isValidPeriod = false;
				List<Date> contestValidPeriodList = copyContestIDsTimeValidityMap
						.get(contestID);
				if (contestValidPeriodList != null
						&& contestValidPeriodList.get(0).before(new Date())
						&& contestValidPeriodList.get(1).after(new Date())) {
					isValidPeriod = true;
				}
				boolean update = false;
				logger.info("RBTDaemonHelper:: isValidContestID = "
						+ isValidContestID + " isValidPeriod = "
						+ isValidPeriod);
				if (isValidContestID && isValidPeriod) {
					noOfTimesSongCopied = noOfTimesSongCopied + 1;
					contestCallerExtraInfoMap.put("NO_OF_COPIES",
							noOfTimesSongCopied + "");
					contestCallerExtraInfoMap.put("CONTEST_ID", contestID);
					String contestCallerExtraInfo = DBUtility
							.getAttributeXMLFromMap(contestCallerExtraInfoMap);
					update = m_rbtDBManager.updateExtraInfo(contestCallerID,
							contestCallerExtraInfo);
				} else {
					contestID = getCurrentLiveContestID(contestCallerCircleID);
					if (contestID != null) {
						contestCallerExtraInfoMap.put("NO_OF_COPIES", 1 + "");
						noOfTimesSongCopied = 1;
						contestCallerExtraInfoMap.put("CONTEST_ID", contestID);
						String contestCallerExtraInfo = DBUtility
								.getAttributeXMLFromMap(contestCallerExtraInfoMap);
						update = m_rbtDBManager.updateExtraInfo(
								contestCallerID, contestCallerExtraInfo);
					}
				}

				if (update) {
					int thresholdNoOfCopies = getParamAsInt("COMMON",
							"THRESHOLD_NUMBER_OF_COPIES_OF_COPY_CONTEST", 0);
					int finalNoOfCopies = getParamAsInt("COMMON",
							"FINAL_NO_OF_COPIES_OF_COPY_CONTEST", 0);
					String smsTextForBelowThres = CacheManagerUtil
							.getSmsTextCacheManager().getSmsText(
									Constants.COPY_CONTEST_THRESHOLD_BELOW_SMS);
					String smsTextForAboveThres = CacheManagerUtil
							.getSmsTextCacheManager().getSmsText(
									Constants.COPY_CONTEST_THRESHOLD_ABOVE_SMS);
					String smsTextForFinalSms = CacheManagerUtil
							.getSmsTextCacheManager().getSmsText(
									Constants.COPY_CONTEST_FINAL_SMS);
					logger.info("CopyInfulencer SMS configurations."
							+ " smsTextForBelowThres: " + smsTextForBelowThres
							+ ", SmsTextForAboveThres: " + smsTextForAboveThres
							+ ", smsTextForFinalSms: " + smsTextForFinalSms
							+ ", finalNoOfCopies: " + finalNoOfCopies);
					if (smsTextForFinalSms != null
							&& noOfTimesSongCopied >= finalNoOfCopies) {
						smsTextForFinalSms = smsTextForFinalSms.replaceAll(
								"%NO_OF_COPY%", noOfTimesSongCopied + "");
						// DND implementation in copy influsiencer feature.Check for DND
						// Subscriber and SEND SMS via Voice Portal. - RBT-11688,
						// RBT-11816 . sendSMS will be called inside the checkDNDSubscriberAndSendSMS function.
						// SMS text send as a final SMS, once the user copies
						// the specified number of copies.						
						Tools.checkDNDSubscriberAndSendSMS(getSenderNumber(contestSubscriber.getCircleID()),
								contestCallerID, smsTextForFinalSms, false);
					} else if (smsTextForBelowThres != null
							&& noOfTimesSongCopied < thresholdNoOfCopies) {
						smsTextForBelowThres = smsTextForBelowThres.replaceAll(
								"%NO_OF_COPY%", noOfTimesSongCopied + "");
						smsTextForBelowThres = smsTextForBelowThres.replaceAll(
								"%NO_OF_COPY_LEFT%",
								(thresholdNoOfCopies - noOfTimesSongCopied)
										+ "");
						// DND implementation in copy influsiencer feature.Check for DND
						// Subscriber and SEND SMS via Voice Portal. - RBT-11688,
						// RBT-11816 . sendSMS will be called inside the checkDNDSubscriberAndSendSMS function.
						Tools.checkDNDSubscriberAndSendSMS(getSenderNumber(contestSubscriber.getCircleID()),
								contestCallerID, smsTextForBelowThres, false);

					} else if (smsTextForAboveThres != null
							&& noOfTimesSongCopied >= thresholdNoOfCopies) {
						smsTextForAboveThres = smsTextForAboveThres.replaceAll(
								"%NO_OF_COPY%", noOfTimesSongCopied + "");
						smsTextForAboveThres = smsTextForAboveThres.replaceAll(
								"%THRESHOLD_NUMBER%", thresholdNoOfCopies + "");
						smsTextForAboveThres = smsTextForAboveThres.replaceAll(
								"%NO_OF_COPY_LEFT%",
								(finalNoOfCopies - noOfTimesSongCopied)
										+ "");
						// DND implementation in copy influsiencer feature.Check for DND
						// Subscriber and SEND SMS via Voice Portal. - RBT-11688,
						// RBT-11816 . sendSMS will be called inside the checkDNDSubscriberAndSendSMS function.
						Tools.checkDNDSubscriberAndSendSMS(getSenderNumber(contestSubscriber.getCircleID()),
								contestCallerID, smsTextForAboveThres, false);
					}
				} else {
					logger.info("RBTDaemonHelper::Not Updating NO_OF_COPIES and CONTEST_ID.");
				}
			}
		} else {
			if(contestCallerIDSiteUrl==null){
				logger.debug("Cross Operator request for the callerID = "+contestCallerID);
				return;
			}
				
			contestCallerIDSiteUrl += "/rbt/Subscription.do?action=copy_contest&subscriberID="
					+ contestCallerID;
			logger.info("Copy Contest :: Forwarding Request to "
					+ contestCallerIDSiteUrl);
			HttpParameters httpParameters = new HttpParameters(
					contestCallerIDSiteUrl);
			RBTHttpClient rbtHttpClient = new RBTHttpClient(httpParameters);
			HashMap<String, String> contestRequestParams = new HashMap<String, String>();
			rbtHttpClient.makeRequestByGet(contestCallerIDSiteUrl,
					contestRequestParams);
			// transfer request to other circle.
		}
	}
	
	public boolean isChargePerCallCallback(String classType,
			Subscriber subscriber) {
		boolean isClassTypeIsChargePerCall = false;
		boolean isCosTypeChargePerCallCosType = false;
		boolean srvKeyExists = false;
		logger.debug("Checking callback for chargepercallback."
				+ " classType: " + classType + ", subscriber: " + subscriber
				+ ", configured subMgrUrlStaticParamsMap: "
				+ subMgrUrlStaticParamsMap
				+ ", configured chargePerCallCosType: " + chargePerCallCosType);

		if (null != subscriber) {
			String cosId = subscriber.cosID();
			CosDetails cosDetails = m_rbtCosCacheManager.getCosDetail(cosId);
			boolean isCosDetailsExists = (null != cosDetails);
			if (isCosDetailsExists) {
				String cosDetailCosType = cosDetails.getCosType();
				isCosTypeChargePerCallCosType = chargePerCallCosType
						.equalsIgnoreCase(cosDetailCosType);
				srvKeyExists = subMgrUrlStaticParamsMap.containsKey("srvkey");

				// subscriber cos costype belongs to configured costype and
				// srvkey in the configured sub mgr url
				// is matched with the given srvclass then it returns true.
				if (isCosTypeChargePerCallCosType && srvKeyExists) {
					String value = subMgrUrlStaticParamsMap.get("srvkey");
					value = value.replaceAll("RBT_SEL_", "").trim();
					isClassTypeIsChargePerCall = (value
							.equalsIgnoreCase(classType)) ? true : false;
				}
			}
			logger.debug(" subscriberId: " + subscriber.subID()
					+ ", srvKeyExists: " + srvKeyExists
					+ ", isCosTypeChargePerCallCosType: "
					+ isCosTypeChargePerCallCosType);
		}
		logger.info("Returning isClassTypeIsChargePerCall: "
				+ isClassTypeIsChargePerCall);
		return isClassTypeIsChargePerCall;
	}

	/**
	 * In RBT Like feature, send an SMS to inform B-party that A likes B' song
	 * and set the same.
	 * 
	 * @param strSubID
	 * @param callerID
	 * @param subscriber
	 * @param extraInfo
	 */
	private void checkAndSendSmsForLike(String strSubID, String callerID,
			Subscriber subscriber, String selectionInfo, String songName) {
		String song = (songName == null) ? "" : songName;
		logger.info("Checking Selection Info for Like to send sms. strSubID: "
				+ strSubID + ", selectionInfo: " + selectionInfo);
		if (selectionInfo != null) {

			String subscriberId = getMsisdnFromSelectionInfo(selectionInfo);

			logger.debug("SelectionInfo contains subscriberId: " + subscriberId);

			if (null != subscriberId) {

				String smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText("SMS", Constants.RBT_LIKE_SUCCESS_MESSAGE,
								subscriber.language());
				if (null != smsText) {
					smsText = smsText.replaceAll("%caller%", strSubID);
					smsText = smsText.replaceAll("%song%", song);
				} else {
					smsText = "The subscriber " + subscriberId
							+ " liked and set the song from you";
				}

				// send a configured sms to the B'party.
				Tools.sendSMS(getSenderNumber(subscriber.circleID()), subscriberId,
						smsText);
				logger.info("Successfully sent sms from CallerId: "
						+ getSenderNumber(subscriber.circleID()) + ", to subscriberId: "
						+ subscriberId + ", smsText: " + smsText);
			}
		}
	}

	/**
	 * Extract and return MSISDN from |CP:PRESSSTAR-9886010929:CP|
	 * 
	 * @param selectionInfo
	 * @return MSISDN in string format
	 */
	public String getMsisdnFromSelectionInfo(String selectionInfo) {
		if (selectionInfo.indexOf("|CP:") > -1
				&& selectionInfo.indexOf(":CP|") > -1) {
			int stIndex = selectionInfo.indexOf("-");
			int endIndex = selectionInfo.lastIndexOf(":");
			if (stIndex > -1 && endIndex > -1) {
				return selectionInfo.substring(stIndex + 1, endIndex);
			}
		}
		return null;
	}

	public String packODASelection(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String reason,
            String reasonCode, String sbnID) {

		logger.info("ODA Pack Selection Callback");
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";
		RBTNode node = RBTMonitorManager.getInstance().startNode(strSubID,
				RBTNode.NODE_SM_CALLBACK_PACK);
		boolean addToDownloads =  getParamAsBoolean("ADD_TO_DOWNLOADS", "false");
		ProvisioningRequests pack = m_rbtDBManager.getProvisioningRequestFromRefId(strSubID, refID);
		try {			
			int selStatus = 0;
			if (pack == null) {
				logger.info("Getting selection using old refID with params:" + " status " + status
						+ " action " + action + " strSubID " + strSubID + " refID " + refID
						+ " type " + type);
				return SELECTION_REFID_NOT_EXISTS;
			} else {
				selStatus = pack.getStatus();
			}
			
			String callerId = null;
			String xtraInfo = pack.getExtraInfo();
			HashMap<String, String> attributeMapFromXML = DBUtility
					.getAttributeMapFromXML(xtraInfo);
			if (attributeMapFromXML != null && attributeMapFromXML.containsKey("CALLER_ID")) {
				callerId = attributeMapFromXML.get("CALLER_ID");
			}
			
			logger.info("Processing Pack Callback  : strSubID >" + strSubID + " action >" + action
					+ " Status >" + status + " & classType> " + classType + " & refID >" + refID);

			if (status == null || action == null || strSubID == null || type == null) {
				logger.info("RBT::Returning false as status " + status + " action " + action
						+ " strSubID " + strSubID + " refID " + refID + " type " + type);
				return FAILURE;
			}

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(conn, strSubID);
			if (classType == null) {
				logger.info("classType is null");
				return FAILURE;
			}

			int categoryId = pack.getType();
			Category category = RBTCacheManager.getInstance().getCategory(categoryId);
			int categoryType = -1;
			if (category != null) {
				categoryType = category.getCategoryTpe();
			}
			if (category == null || categoryType != PLAYLIST_ODA_SHUFFLE) {
				return SELECTION_NOT_EXISTS;
			}
			//ODA Pack De-activation success callback
			if (m_strActionDeactivation.equalsIgnoreCase(action)
					&& m_statusSuccess.equalsIgnoreCase(status)) {
				// Pack needs to be deactivated and selections to be put in
				// TO_BE_DEACTIVE state
				logger.info("RBT::ODA Pack Deactivation request received for Subscriber:"
						+ strSubID + " with status " + status);

				if (selStatus == 0) {
					logger.info("Selection does not exist");
					return SELECTION_NOT_EXISTS;
				} else if (selStatus != PACK_TO_BE_DEACTIVATED
						&& selStatus != PACK_DEACTIVATION_PENDING
						&& selStatus != PACK_DEACTIVATION_ERROR) {
					if (selStatus == PACK_DEACTIVATED) {
						return SELECTION_ALREADY_DEACTIVE;
					} else if (m_packActPendingStatus.contains(selStatus)) {
						return SELECTION_DCT_PENDING;
					} else if (m_packActStatus.contains(selStatus)) {
						return INVALID + "|" + "SELECTION ACTIVE";
					}
				}
				String ret = smPackDectivationSuccess(strSubID, refID);

				if (ret == null
						|| (!ret.equalsIgnoreCase(m_success) && !ret.equalsIgnoreCase(m_failure))) {
					logger.info("Pack Deactivation response " + ret);
					return FAILURE;
				}

				if (ret.equalsIgnoreCase(m_success)) {
					if (addToDownloads) {
						List<ProvisioningRequests> activeProvisioningRequests = m_rbtDBManager.getActiveODAPackBySubscriberIDAndType(
								strSubID, categoryId);
						if (activeProvisioningRequests != null && !activeProvisioningRequests.isEmpty()) {
							HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();

							packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, "DAEMON");
							packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME, new Date().toString());
							for (ProvisioningRequests activeProvisioningRequest : activeProvisioningRequests) {
								String extraInfo = activeProvisioningRequest.getExtraInfo();
								HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
								if (xtraInfoMap == null)
									xtraInfoMap = new HashMap<String, String>();
								xtraInfoMap.putAll(packExtraInfoMap);
								String extraInfoString = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
								logger.debug("provisioningRequest: " + activeProvisioningRequest);
								m_rbtDBManager.updateProvisioningRequestsStatusAndExtraInfo(strSubID,
										activeProvisioningRequest.getTransId(), 43, extraInfoString);

								deactivateSelectionsUnderODAPack(subscriber, category,activeProvisioningRequest.getTransId());

							}
						}
						SubscriberDownloads[] subscriberDownloads = m_rbtDBManager
								.getSubscriberActiveDownloadsByDownloadStatusAndCategory(strSubID, categoryId, categoryType);
						if (subscriberDownloads != null && subscriberDownloads.length > 0) {
							for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
								m_rbtDBManager.expireSubscriberDownload(subscriber.subID(), subscriberDownload.refID(), "DAEMON");
							}

						}
						SubscriberDownloads[] trackedSubscriberDownload = m_rbtDBManager
								.getSubscriberDownloadsByDownloadStatusAndCategory(subscriber.subID(), categoryId, categoryType,
										"t");
						if (trackedSubscriberDownload != null && trackedSubscriberDownload.length > 0) {
							m_rbtDBManager.updateDownloads(subscriber.subID(), trackedSubscriberDownload[0].refID(), 'x',
									trackedSubscriberDownload[0].extraInfo(), null);
						}

					} else {
						deactivateSelectionsUnderODAPack(subscriber, category, callerId, refID);
					}
				}

				return SUCCESS;
			}

			if (m_statusSuccess.equalsIgnoreCase(status)) {
				// Activation success
				if (m_strActionActivation.equalsIgnoreCase(action)) {
					logger.info("ODA Pack Selection Activation Callback Received");
					if (selStatus == 0) {
						logger.info("The selection does not exist ");
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_TO_BE_ACTIVATED
							&& selStatus != PACK_ACTIVATION_PENDING && selStatus != PACK_GRACE) {
						if (selStatus == PACK_ACTIVATED) {
							logger.info("Selection already active");
							return SELECTION_ALREADY_ACTIVE;
						} else if (selStatus == PACK_DEACTIVATED)
							return SELECTION_DEACTIVE;
						else
							return CALLBACK_ALREADY_RECEIVED;
					}
					String extraInfo = pack.getExtraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);

					if (xtraInfoMap != null && xtraInfoMap.containsKey("ODA_REFRESH")) {
						xtraInfoMap.remove("ODA_REFRESH");
						selStatus = PACK_ODA_REFRESH;
					} else {
						selStatus = PACK_ACTIVATED;
					}
					
					String callerID = xtraInfoMap!=null ? xtraInfoMap.get("CALLER_ID"):null;
					extraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
					boolean success = smPackODAActivationSuccess(strSubID, refID, selStatus,
							extraInfo);
					if (success) {
						m_rbtDBManager.deactivateOldODAPackOnSuccessCallback(strSubID, refID,callerID,category.getCategoryTpe(),null,true, extraInfo);
						// Convert selections W to A
						m_rbtDBManager.smUpdateSelStatusODAPackSubscriptionSuccess(strSubID, false,
								false, category.getCategoryId(), false, callerId, refID,pack);
						//RBT-13544 TEF ES - Mi Playlist functionality need to deactivate normal selections 
						String selectionStatus = xtraInfoMap!=null ? xtraInfoMap.get("STATUS"):null;
						if(callerID == null && (selectionStatus == null || selectionStatus.trim().equals("1"))) {
						SubscriberStatus[] activeNormalSel = m_rbtDBManager.getActiveNormalSelByCallerIdAndByStatus(strSubID,null,1);
						  if(activeNormalSel != null && activeNormalSel.length>0) {
								for(int i = 0;i<activeNormalSel.length;i++) {
									m_rbtDBManager.deactivateSubscriberRecordsByRefId(strSubID, "SM", activeNormalSel[i].refID());
								}
						   }
					    }
					    boolean isSelectionTrackAllowed = getParamAsBoolean("SONG_REPRICING_SELECTION_TRACK_ALLOWED", "FALSE");
						//For TEF-Spain song re-pricing. If selection success callback and not download model, then make entry in subscriber downloads table as status as STATE_DOWNLOAD_SEL_TRACK.
						if (isSelectionTrackAllowed) {
								Clip[] clips = m_rbtCacheManager.getActiveClipsInCategory(category.getCategoryId());
								Categories categoriesObj = CategoriesImpl.getCategory(category);
								if(clips!=null && clips.length>0) {
								HashMap<String, String> extrInfoMap = new HashMap<String, String>();
								extrInfoMap.put("UPDATE_DOWNLOAD", "TRUE");
								extrInfoMap.put("PROV_REF_ID", pack.getTransId());
								m_rbtDBManager.addSubscriberDownloadRW(strSubID, clips[0].getClipRbtWavFile(), categoriesObj, null, true, 
										classType, pack.getMode(), pack.getModeInfo(), extrInfoMap , false, false, false, null, null);
								}
						}
						
						return SUCCESS;
						
					} else {
						return FAILURE;
					}
				} else {
					// Renewal Case
					logger.info("Renewal Callback received for ODA Pack ");
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_ACTIVATED && selStatus != PACK_ACTIVATION_ERROR
							&& selStatus != PACK_SUSPENDED) {
						if (selStatus == PACK_BASE_ACTIVATION_PENDING
								|| selStatus == PACK_ACTIVATION_PENDING
								|| selStatus == PACK_TO_BE_ACTIVATED || selStatus == PACK_GRACE) {
							return SELECTION_ACT_PENDING;
						} else if (selStatus == PACK_DEACTIVATION_PENDING
								|| selStatus == PACK_DEACTIVATED
								|| selStatus == PACK_TO_BE_DEACTIVATED
								|| selStatus == PACK_DEACTIVATION_ERROR) {
							return SELECTION_DEACTIVE;
						}
					} else if (pack.getNextRetryTime() != null
							&& pack.getNextRetryTime().getTime() > System.currentTimeMillis()) {
						return INVALID + "|" + "SELECTION RENWAL ALREADY UPDATED";
					}

					int newCategoryID = -1;
					String prepaidYes = "y";
					if (!subscriber.prepaidYes())
						prepaidYes = "n";
					CosDetails newCos = CacheManagerUtil.getCosDetailsCacheManager()
							.getSmsKeywordCosDetail(classType, subscriber.circleID(), prepaidYes);
					if (category != null && category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE)
						newCategoryID = category.getCategoryId();

					String ret = smODAPackRenewalSuccess(strSubID, refID, pack.getNextRetryTime(),
							classType, newCategoryID);

					if (ret.equalsIgnoreCase(m_success)) {
						logger.info("Successfully renewed pack, updating player "
								+ "status to A for subscriberId: " + strSubID);
						// update subscriber player status to A since pack
						// renewal is success
						m_rbtDBManager.updatePlayerStatus(strSubID, "A");
						if (pack.getStatus() == PACK_SUSPENDED) { // Activate
																	// suspended
																	// selections
							m_rbtDBManager.activateSuspendedSettingsForPack(strSubID, false);
						}
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				}
			} else if (m_statusActGrace.equalsIgnoreCase(status)
					&& m_strActionActivation.equalsIgnoreCase(action)) {
				//ODA Pack Grace callback
				logger.info("ODA Pack Selection Grace Callback received");
				if (selStatus == 0) {
					return SELECTION_NOT_EXISTS;
				} else if (selStatus != PACK_ACTIVATION_PENDING
						&& selStatus != PACK_TO_BE_ACTIVATED) {
					if (selStatus == PACK_GRACE) {
						return SELECTION_ALREADY_ON_GRACE;
					} else if (selStatus == PACK_ACTIVATED) {
						return SELECTION_ALREADY_ACTIVE;
					} else if (selStatus == PACK_DEACTIVATED) {
						return SELECTION_DEACTIVE;
					} else if (m_packdeActStatus.contains(selStatus)) {
						return SELECTION_DCT_PENDING;
					}
				}

				String ret = packSelectionGrace(strSubID, refID);
				if (ret.equalsIgnoreCase(m_success)) {
					return SUCCESS;
				}

				else if (ret.equalsIgnoreCase(m_failure)) {
					return INVALID;
				} else {
					return FAILURE;
				}
			} else if (m_strSuspendSubscription.equalsIgnoreCase(action)) {
				// Suspend failure callback
				logger.info("Suspended Failure Callback received");
				if (selStatus == 0)
					return SELECTION_NOT_EXISTS;
				else if (selStatus != PACK_ACTIVATED && selStatus != PACK_ACTIVATION_ERROR) {
					if (selStatus == PACK_SUSPENDED)
						return SELECTION_ALREADY_SUSPENDED;
					if (selStatus == PACK_TO_BE_ACTIVATED || selStatus == PACK_ACTIVATION_PENDING
							|| selStatus == PACK_GRACE || selStatus == PACK_BASE_ACTIVATION_PENDING)
						return SELECTION_ACT_PENDING;
					else
						return SELECTION_ALREADY_DEACTIVE;
				}
				String ret = packSelectionSuspend(strSubID, refID);
				if (ret.equalsIgnoreCase(m_success)) {
					logger.info("Suspended pack selection, updating player "
							+ "status to A for subscriberId: " + strSubID);
					// update player status to A since pack selection
					// is suspended
					m_rbtDBManager.updatePlayerStatus(strSubID, "A");
					return SUCCESS;
				} else if (ret.equalsIgnoreCase(m_failure))
					return INVALID;
				else
					return FAILURE;
			} else {

				// Deactivation Failure Case
				logger.info("Deactivation Failure Callback received for ODA Pack Selection");
				if (m_strActionDeactivation.equalsIgnoreCase(action)) {
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_DEACTIVATION_PENDING
							&& selStatus != PACK_DEACTIVATION_ERROR && selStatus != PACK_SUSPENDED
							&& selStatus != PACK_TO_BE_DEACTIVATED) {
						if (selStatus == PACK_DEACTIVATED) {
							return SELECTION_ALREADY_DEACTIVE;
						} else if (m_packActStatus.contains(selStatus)) {
							return SELECTION_ACTIVE;
						}
					}

					String ret = smPackDeactivationFailure(strSubID, refID);
					if (ret.equalsIgnoreCase(m_success)) {
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				} else {
					//ODA Pack Activation/Renewal Failure Callback
					logger.info("Renewal Failure Callback received for ODA Pack Selection");
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					}

					if (m_strActionActivation.equals(action) && selStatus == PACK_ACTIVATED)
						return SELECTION_ALREADY_ACTIVE;
					if (selStatus == PACK_DEACTIVATED)
						return SELECTION_DEACTIVE;

					String ret = smPackRenewalFailure(strSubID, refID, null);

					if (ret.equalsIgnoreCase(m_success)) {
						categoryId = pack.getType();
						SubscriberStatus[] activeSelections = m_rbtDBManager
								.getActiveSelectionBasedOnCallerId(strSubID, callerId);
						List<String> refIDList = new ArrayList<String>();
						if (activeSelections != null) {
							for (SubscriberStatus subscriberStatus : activeSelections) {
								int catId = subscriberStatus.categoryID();
								Category category2 = RBTCacheManager.getInstance().getCategory(
										catId);
								String extraInfo = subscriberStatus.extraInfo();
								HashMap<String, String> extraInfoMap = DBUtility
										.getAttributeMapFromXML(extraInfo);
								String provRefId = extraInfoMap.get("PROV_REF_ID");
								if (subscriberStatus.selStatus().equals(
										STATE_BASE_ACTIVATION_PENDING)
										&& catId == categoryId && refID.equalsIgnoreCase(provRefId)) {
									refIDList.add(subscriberStatus.refID());
								}
							}
						}

						if (refIDList.size() > 0) {
							m_rbtDBManager.deactivateNewSelections(strSubID, "Daemon", null, null,
									false, 0, refIDList, null);
						}
											
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					}
				}

			}

		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		} finally {
			RBTMonitorManager.getInstance().endNode(strSubID, node, status);
		}
		return FAILURE;

	}

	public String packSelection(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String reason,
			String reasonCode, String sbnID, String circleIDFromPrism) {// RBT-14301: Uninor MNP changes.
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";
		boolean isProfilePack = false;
		boolean isAutoDownloadPack = false;
		RBTNode node = RBTMonitorManager.getInstance().startNode(strSubID,
				RBTNode.NODE_SM_CALLBACK_PACK);
		try {
			int selStatus = 0;

			logger.info("Processing Pack Callback  : strSubID >" + strSubID
					+ " action >" + action + " Status >" + status
					+ " & classType> " + classType + " & refID >" + refID);

			if (status == null || action == null || strSubID == null
					|| type == null) {
				logger.info("RBT::Returning false as status " + status
						+ " action " + action + " strSubID " + strSubID
						+ " refID " + refID + " type " + type);
				return FAILURE;
			}

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(
					conn, strSubID);
			if (classType == null) {
				logger.info("classType is null");
				return FAILURE;
			}

			ProvisioningRequests pack = m_rbtDBManager
					.getProvisioningRequestFromRefId(strSubID, refID);
			// SubscriberStatus subscriberStatus =
			// m_rbtDBManager.getActiveProfileSelections(strSubID, 99);

			if (pack == null) {
				logger.info("Getting selection using old refID with params:"
						+ " status " + status + " action " + action
						+ " strSubID " + strSubID + " refID " + refID
						+ " type " + type);

				return SELECTION_REFID_NOT_EXISTS;
			} else {
				selStatus = pack.getStatus();
			}

			int cosID = pack.getType();
			String cosType = null;
			CosDetails cosdetail = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosID + "");
			if (cosdetail != null)
				cosType = cosdetail.getCosType();

			if (cosdetail == null
					|| cosType == null
					|| (!cosType.equalsIgnoreCase(SONG_PACK)
							&& !cosType.equalsIgnoreCase(LIMITED_DOWNLOADS)
							&& !cosType.equalsIgnoreCase(AZAAN)
							&& !cosType.equalsIgnoreCase(UNLIMITED_DOWNLOADS)
							&& !cosType.equalsIgnoreCase(UNLIMITED_DOWNLOADS_OVERWRITE)
							&& !cosType.equalsIgnoreCase(LIMITED_SONG_PACK_OVERLIMIT)
							&& !cosType.equalsIgnoreCase(PROFILE_COS_TYPE) 
							&& !cosType.equalsIgnoreCase(COS_TYPE_AUTO_DOWNLOAD)
							&& !cosType.equalsIgnoreCase(MUSIC_POUCH))) {
				return SELECTION_NOT_EXISTS; 
			}

			if (cosdetail.getCosType().equalsIgnoreCase(PROFILE_COS_TYPE)) {
				isProfilePack = true;
			} else if (cosdetail.getCosType().equalsIgnoreCase(
					COS_TYPE_AUTO_DOWNLOAD)) {
				isAutoDownloadPack = true;
			}

			String cosIds = RBTParametersUtils.getParamAsString(COMMON,
					"COSIDS_NOT_ALLOWED_PACK_SELECTION_SUSPENSION", null);
			boolean isPackSelSuspensionAllowed = true;
			if (cosIds != null) {
				String str[] = cosIds.split(",");
				List<String> packCosIdsList = Arrays.asList(str);
				isPackSelSuspensionAllowed = packCosIdsList.contains(String
						.valueOf(cosID)) ? false : true;
			}
			//Pack De-activation success callback
			if (m_strActionDeactivation.equalsIgnoreCase(action)
					&& m_statusSuccess.equalsIgnoreCase(status)) {
				// Pack needs to be deactivated and selections to be put in
				// TO_BE_DEACTIVE state
				logger.info("RBT::Deactivation request received for Subscriber:"
						+ strSubID + " with status " + status);

				if (selStatus == 0) {
					logger.info("Selection does not exist");
					return SELECTION_NOT_EXISTS;
				} else if (selStatus != PACK_TO_BE_DEACTIVATED
						&& selStatus != PACK_DEACTIVATION_PENDING
						&& selStatus != PACK_DEACTIVATION_ERROR) {
					if (selStatus == PACK_DEACTIVATED) {
						return SELECTION_ALREADY_DEACTIVE;
					} else if (m_packActPendingStatus.contains(selStatus)) {
						return SELECTION_ACT_PENDING;
					} else if (m_packActStatus.contains(selStatus)) { 
						return INVALID + "|" + "SELECTION ACTIVE";
					}
				}
				String ret = smPackDectivationSuccess(strSubID, refID);

				if (ret == null
						|| (!ret.equalsIgnoreCase(m_success) && !ret
								.equalsIgnoreCase(m_failure))) {
					logger.info("Pack Deactivation response " + ret);
					return FAILURE;
				}

				if (ret.equalsIgnoreCase(m_success)) {

					// Azaan feature. Once the subscriber is deactivated from
					// the
					// azaan feature removed cos ids from subscriber extra info
					// and set the player status to A, so that tone player
					// daemon
					// will update Tone Player.
					if (confAzaanCosIdList.contains(String.valueOf(cosID))) {
						boolean deactivatedNow = m_rbtDBManager
								.noDownloadDeactSub(
										getParamAsBoolean(
												"NO_DOWNLOAD_DEACT_SUB",
												"FALSE"), subscriber.subID(),
										type, true);
						// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
						if (deactivatedNow == false)
							/*removeAzaanCosFromPackExtraInfo(strSubID,
									subscriber);*/
							removeAzaanCosFromPackExtraInfo(strSubID,
									subscriber, null);
					} else {

						// Get all selections for the subscriber status 99 &
						// deact
						// selections
						// m_rbtDBManager.deactivateSubscriberRecordsByStatus(strSubID,
						// 99, "SM");
						// remove that extrainfo in subsciber

						removePackExtraInfoFromSubscriber(subscriber, cosID);
					}

//					Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//							.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
					
//					List<String> musicPackCosIdList = null;
					
					
//					if(muiscPackCosIdParam != null) {
//						musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//					}
					
					if(UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosdetail.getCosType()) 
							|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosdetail.getCosType())) {
						deactivateMusicPackDownloads(subscriber, cosdetail);
					}
					else {
						
						// Deactivate all pack related personalized settings and
						// downloads
						deactivateDownloadsUnderPack(subscriber, cosdetail);
					}
					updatePlayerStatus(subscriber, "A");
				}
				
				//Added for Active B2B subscriber as D2C App user (Free trial user)
				// making status FREE_TRIAL
				String freeTrialCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,"DTOC_FREE_TRIAL_COS_ID", null);
				if(freeTrialCosId != null && pack.getType() == Integer.parseInt(freeTrialCosId)){
				 updatingOperatorUserInfo(subscriber, OperatorUserTypes.LEGACY.getDefaultValue());
				}
				
				return SUCCESS;
			}

			if (m_statusSuccess.equalsIgnoreCase(status)) {
				// Activation success
				if (m_strActionActivation.equalsIgnoreCase(action)) {

					if (selStatus == 0) {
						logger.info("The selection does not exist ");
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_TO_BE_ACTIVATED
							&& selStatus != PACK_ACTIVATION_PENDING
							&& selStatus != PACK_GRACE) {
						if (selStatus == PACK_ACTIVATED) {
							logger.info("Selection already active");
							return SELECTION_ALREADY_ACTIVE;
						} else if (selStatus == PACK_DEACTIVATED)
							return SELECTION_DEACTIVE;
						else
							return CALLBACK_ALREADY_RECEIVED;
					}

					// passing numMaxSel as '-1' so that num_max_sel won't be
					// updated for profile packs
					int numMaxSelsToUpdate = -1;
					if (isProfilePack)
						numMaxSelsToUpdate = -1;

					String ret = smPackActivationSuccess(strSubID, refID,
							numMaxSelsToUpdate);

					if (ret.equalsIgnoreCase(m_success)) {
						// Convert selections W to A
						m_rbtDBManager
								.smUpdateSelStatusPackSubscriptionSuccess(
										strSubID, false, false, cosID,
										isProfilePack);
						

						// reactivate the previous deactivated downloads and
						// settings under music pack.
						updateSmSubscriptionValidityStatus(pack);
						reactivateDownloadsAndSettingsUnderPack(subscriber,
								cosdetail);

						// Azaan feature: The PACK attribute of subscriber
						// contains the Cos Id for which the subscriber is
						// activated on.So, get the Cos Ids and check the cos id
						// is of type Azaan cos id.Azaan cos ids are
						// configurable. If the cos id is
						// matching then update the player status to A, so that
						// the tone player will getinformed by player daemon.
						// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
						String extraInfo = subscriber.extraInfo();
						HashMap<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						if (null != cosTypesForMultiPack
								&& !cosTypesForMultiPack.isEmpty()) {
							if (extraInfoMap != null
									&& extraInfoMap
											.containsKey(EXTRA_INFO_PACK)) {
								String packStr = extraInfoMap
										.get(EXTRA_INFO_PACK);
								String[] packs = (packStr != null) ? packStr
										.trim().split(",") : null;
								// Jira :RBT-15026: Changes done for allowing
								// the multiple Azaan pack.Convert the already
								// activated pack into list to validate the
								// requesting Cosid is there or not.
								List<String> packCosIdLst = (packs != null && packs.length > 0) ? new ArrayList<String>(
										Arrays.asList(packs)) : null;
								if (null != packCosIdLst) {
									packCosIdLst.remove(cosdetail.getCosId());
									for (String activePackCosId : packCosIdLst) {
										String oldAzaanSubType = confAzaanCopticDoaaCosIdSubTypeMap
												.get(activePackCosId);
										String tobeActiveAzaanCosSubType = confAzaanCopticDoaaCosIdSubTypeMap
												.get(cosdetail.getCosId());
										CosDetails oldCosDetails = CacheManagerUtil
												.getCosDetailsCacheManager()
												.getCosDetail(activePackCosId);
										oldAzaanSubType = (oldAzaanSubType == null) ? oldCosDetails
												.getCosType() : oldAzaanSubType;
										tobeActiveAzaanCosSubType = (tobeActiveAzaanCosSubType == null) ? cosdetail
												.getCosType()
												: tobeActiveAzaanCosSubType;

										if (oldAzaanSubType
												.equalsIgnoreCase(tobeActiveAzaanCosSubType)
												&& cosTypesForMultiPack
														.contains(oldAzaanSubType)) {
											removeOldAzaanPackOnSubscriber(
													strSubID, subscriber,
													activePackCosId,
													oldCosDetails);
										}
									}
								}
							}
						}
						addAzaanCosToPackExtraInfo(strSubID, subscriber,
								extraInfoMap,cosType);
						
						
						updateDownloadOnDownloadSongPackAct(subscriber, cosdetail);
                       
						//RBT-12835 - Loop Feature required in Song Pack- ZM
						Boolean musicPackCosTypesForSelectionsLoop = Utility.isCosTypeConfiguredForSelectionLoop(cosdetail.getCosType());
						logger.debug("musicPackCosTypesForSelectionsLoop: " + musicPackCosTypesForSelectionsLoop);
						if (musicPackCosTypesForSelectionsLoop) {
							SubscriberDownloads[] downloads = m_rbtDBManager.getActiveSubscriberDownloads(subscriber.subID());
							if (downloads != null && downloads.length > 0 ) {
								for (SubscriberDownloads download : downloads) {
									String categoryId = String.valueOf(download.categoryID());
									String clipId = null;
									Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(download.promoId());
									if (clip != null) {
										clipId = String.valueOf(clip.getClipId());
									}
									SelectionRequest selRequest = new SelectionRequest(
											strSubID, true, "ALL", "DAEMON",
											categoryId, clipId,
											null, null, true);
									selRequest.setModeInfo("DAEMON");
									RBTClient.getInstance().addSubscriberSelection(selRequest);
									String response = selRequest.getResponse();
									if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
										logger.warn("Selection failed for selRequest: " + selRequest);
									}
									logger.debug("Response: " +  response + " on adding selection for download: " + download);
								}
							}
						}
						
						if (isAutoDownloadPack) {
							String maxAllowed = "-1";
							String[] mappings = RBTParametersUtils
									.getParamAsString(
											COMMON,
											"AMOUNT_TO_MAX_ALLOWED_SEL_MAPPING",
											"").split(";");
							for (String mapping : mappings) {
								String[] tokens = mapping.split(":");
								if (tokens[0].trim().equalsIgnoreCase(
										amountCharged)) {
									maxAllowed = tokens[1].trim();
									break;
								}
							}
							extraInfoMap.put(EXTRA_INFO_PACK_MAX_ALLOWED,
									maxAllowed);
							extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);
							ProvisioningRequestsDao.updateExtraInfo(strSubID,
									refID, extraInfo);
						}
						
						//Added for Active B2B subscriber as D2C App user (Free trial user)
						// making status FREE_TRIAL   pack cos id check
						String freeTrialCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,"DTOC_FREE_TRIAL_COS_ID", null);
						if(freeTrialCosId != null && pack.getType() == Integer.parseInt(freeTrialCosId)){
							updatingOperatorUserInfo(subscriber, OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue());
							updatePlayerStatus(subscriber, "A");
						}
						
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				} else {
					// Renewal Case
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_ACTIVATED
							&& selStatus != PACK_ACTIVATION_ERROR
							&& selStatus != PACK_SUSPENDED) {
						if (selStatus == PACK_BASE_ACTIVATION_PENDING
								|| selStatus == PACK_ACTIVATION_PENDING
								|| selStatus == PACK_TO_BE_ACTIVATED
								|| selStatus == PACK_GRACE) {
							return SELECTION_ACT_PENDING;
						} else if (selStatus == PACK_DEACTIVATION_PENDING
								|| selStatus == PACK_DEACTIVATED
								|| selStatus == PACK_TO_BE_DEACTIVATED
								|| selStatus == PACK_DEACTIVATION_ERROR) {
							return SELECTION_DEACTIVE;
						}
					} else if (pack.getNextRetryTime() != null
							&& pack.getNextRetryTime().getTime() > System
									.currentTimeMillis()) {
						return INVALID + "|"
								+ "SELECTION RENWAL ALREADY UPDATED";
					}

					int newCosID = -1;
					String prepaidYes = "y";
					if (!subscriber.prepaidYes())
						prepaidYes = "n";
					CosDetails newCos = CacheManagerUtil
							.getCosDetailsCacheManager()
							.getSmsKeywordCosDetail(classType,
									subscriber.circleID(), prepaidYes);
					if (newCos != null
							&& cosdetail.getCosType().equalsIgnoreCase(
									newCos.getCosType()))
						newCosID = Integer.parseInt(newCos.getCosId());

					String ret = smPackRenewalSuccess(strSubID, refID,
							pack.getNextRetryTime(), classType, newCosID);

					if (ret.equalsIgnoreCase(m_success)) {

						logger.info("Successfully renewed pack, updating player "
								+ "status to A for subscriberId: " + strSubID);
						// update subscriber player status to A since pack
						// renewal is success
						m_rbtDBManager.updatePlayerStatus(strSubID, "A");

						if (pack.getStatus() == PACK_SUSPENDED) { // Activate
																	// suspended
																	// selections
							m_rbtDBManager.activateSuspendedSettingsForPack(
									strSubID, isProfilePack);
						}
						if (newCosID > -1)
							updatePackExtraInfoFromSubscriber(subscriber,
									cosID, newCosID);

						if (isAutoDownloadPack) {
							if (RBTDBManager.getInstance()
									.isSubscriberSuspended(subscriber)) {
								String isPrepaid = (subscriber.prepaidYes()) ? "p"
										: "b";
								RBTDBManager.getInstance().smRenewalSuccess(
										strSubID, null, isPrepaid,
										subscriber.subscriptionClass(), null,
										null, null,false);
							}

							String maxAllowed = "-1";
							String[] mappings = RBTParametersUtils
									.getParamAsString(
											COMMON,
											"AMOUNT_TO_MAX_ALLOWED_SEL_MAPPING",
											"").split(";");
							for (String mapping : mappings) {
								String[] tokens = mapping.split(":");
								if (tokens[0].trim().equalsIgnoreCase(
										amountCharged)) {
									maxAllowed = tokens[1].trim();
									break;
								}
							}
							String extraInfo = pack.getExtraInfo();
							Map<String, String> extraInfoMap = DBUtility
									.getAttributeMapFromXML(pack.getExtraInfo());
							extraInfoMap.put(EXTRA_INFO_PACK_MAX_ALLOWED,
									maxAllowed);
							extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);
							ProvisioningRequestsDao.updateExtraInfo(strSubID,
									refID, extraInfo);

							m_rbtDBManager
									.deactivateSubscriberRecordsByStatusAndAllCaller(
											strSubID, 1, "SM");

							int catID = Integer
									.parseInt(DBUtility.getAttributeMapFromXML(
											pack.getExtraInfo()).get(
											iRBTConstant.EXTRA_INFO_PACK_CATID));
							SelectionRequest selRequest = new SelectionRequest(
									strSubID, String.valueOf(catID), null);
							selRequest.setMode("DAEMON");

							RBTClient.getInstance().addSubscriberSelection(
									selRequest);
						}
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				}
			} else if (m_statusActGrace.equalsIgnoreCase(status)
					&& m_strActionActivation.equalsIgnoreCase(action)) {
				//Pack GRACE callback
				if (selStatus == 0) {
					return SELECTION_NOT_EXISTS;
				} else if (selStatus != PACK_ACTIVATION_PENDING
						&& selStatus != PACK_TO_BE_ACTIVATED) {
					if (selStatus == PACK_GRACE) {
						return SELECTION_ALREADY_ON_GRACE;
					} else if (selStatus == PACK_ACTIVATED) {
						return SELECTION_ALREADY_ACTIVE;
					} else if (selStatus == PACK_DEACTIVATED) {		
						return SELECTION_DEACTIVE;
					} else if (m_packdeActStatus.contains(selStatus)) {
						return SELECTION_DCT_PENDING;
					}
				}

				String ret = packSelectionGrace(strSubID, refID);
				if (ret.equalsIgnoreCase(m_success)) {
					return SUCCESS;
				}

				else if (ret.equalsIgnoreCase(m_failure)) {
					return INVALID;
				} else {
					return FAILURE;
				}
			} else if (m_strSuspendSubscription.equalsIgnoreCase(action)) {

				// Suspend failure callback
				if (selStatus == 0)
					return SELECTION_NOT_EXISTS;
				else if (selStatus != PACK_ACTIVATED
						&& selStatus != PACK_ACTIVATION_ERROR) {
					if (selStatus == PACK_SUSPENDED)
						return SELECTION_ALREADY_SUSPENDED;
					if (selStatus == PACK_TO_BE_ACTIVATED
							|| selStatus == PACK_ACTIVATION_PENDING
							|| selStatus == PACK_GRACE
							|| selStatus == PACK_BASE_ACTIVATION_PENDING)
						return SELECTION_ACT_PENDING;
					else
						return SELECTION_ALREADY_DEACTIVE;
				}
				String ret = packSelectionSuspend(strSubID, refID);
				if (ret.equalsIgnoreCase(m_success)) {

					logger.info("Suspended pack selection, updating player "
							+ "status to A for subscriberId: " + strSubID);
					// update player status to A since pack selection
					// is suspended
					m_rbtDBManager.updatePlayerStatus(strSubID, "A");

					if (isAutoDownloadPack)
						m_rbtDBManager.processSuspendSubscription(strSubID,
								subscriber.extraInfo(), false);
					else if (isPackSelSuspensionAllowed)
						m_rbtDBManager.updateSettingsForPackSuspensionCallback(
								strSubID, isProfilePack);

					return SUCCESS;
				} else if (ret.equalsIgnoreCase(m_failure))
					return INVALID;
				else
					return FAILURE;
			} else {
				// Deactivation Failure Case
				if (m_strActionDeactivation.equalsIgnoreCase(action)) {
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					} else if (selStatus != PACK_DEACTIVATION_PENDING
							&& selStatus != PACK_DEACTIVATION_ERROR
							&& selStatus != PACK_SUSPENDED
							&& selStatus != PACK_TO_BE_DEACTIVATED) {
						if (selStatus == PACK_DEACTIVATED) {
							return SELECTION_ALREADY_DEACTIVE;
						} else if (m_packActStatus.contains(selStatus)) {
							return SELECTION_ACTIVE;
						}
					}

					String ret = smPackDeactivationFailure(strSubID, refID);
					if (ret.equalsIgnoreCase(m_success)) {
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				} else {
					//Pack Activation/Renewal Failure Callback
					if (selStatus == 0) {
						return SELECTION_NOT_EXISTS;
					}

					if (m_strActionActivation.equals(action)
							&& selStatus == PACK_ACTIVATED)
						return SELECTION_ALREADY_ACTIVE;
					if (selStatus == PACK_DEACTIVATED)
						return SELECTION_DEACTIVE;

					String ret = smPackRenewalFailure(strSubID, refID, null);

					if (ret.equalsIgnoreCase(m_success)) {
						boolean deactivatedNow = m_rbtDBManager
								.noDownloadDeactSub(
										getParamAsBoolean(
												"NO_DOWNLOAD_DEACT_SUB",
												"FALSE"), subscriber.subID(),
										type, true);
						if (deactivatedNow == false)// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
							removeAzaanCosFromPackExtraInfo(strSubID,
									subscriber, null);
						if (deactivatedNow == true)
							return SUCCESS;
					}
					
					//Added for Active B2B subscriber as D2C App user (Free trial user)
					// making status FREE_TRIAL
					if (ret.equalsIgnoreCase(m_success)) {
						String freeTrialCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,"DTOC_FREE_TRIAL_COS_ID", null);
						if(freeTrialCosId != null && pack.getType() == Integer.parseInt(freeTrialCosId) && m_strActionRental.equals(action)){
							updatingOperatorUserInfo(subscriber, OperatorUserTypes.LEGACY.getDefaultValue());
							updatePlayerStatus(subscriber, "A");
						}	
					}
					

					if (ret.equalsIgnoreCase(m_success) && isAutoDownloadPack) {
						smSubscriptionRenewalFailure(strSubID, "SM", type,
								subscriber.subscriptionClass(), true,
								subscriber.extraInfo(), true, true, circleIDFromPrism);
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_success)) {
						int cosId = pack.getType();
						if (isProfilePack) {
							m_rbtDBManager.deactivateSubscriberRecordsByStatus(
									strSubID, 99, "SM");
						} else {
							if (getParamAsBoolean("COMMON", "ADD_TO_DOWNLOADS",
									"FALSE")) {
								SubscriberDownloads[] activeDownloads = m_rbtDBManager
										.getActivateNActPendingDownloads(strSubID);
								List<String> refIDList = new ArrayList<String>();
								if (activeDownloads != null) {
									for (SubscriberDownloads subscriberDownload : activeDownloads) {
										if (subscriberDownload.downloadStatus() == STATE_DOWNLOAD_BASE_ACT_PENDING) {
											Map<String, String> extraInfoMap = DBUtility
													.getAttributeMapFromXML(subscriberDownload
															.extraInfo());
											String packId = (extraInfoMap != null) ? extraInfoMap
													.get(EXTRA_INFO_PACK)
													: null;
											if (packId != null
													&& packId.equals(String
															.valueOf(cosId))) {
												refIDList
														.add(subscriberDownload
																.refID());
											}
										}
									}
								}

								if (refIDList.size() > 0) {
									m_rbtDBManager
											.expireAllSubscriberPendingDownload(
													strSubID, "PackActFail",
													refIDList);
								}
							}

							SubscriberStatus[] activeSelections = m_rbtDBManager
									.getAllActiveSubscriberSettings(strSubID);
							List<String> refIDList = new ArrayList<String>();
							if (activeSelections != null) {
								for (SubscriberStatus subscriberStatus : activeSelections) {
									if (subscriberStatus.selStatus().equals(
											STATE_BASE_ACTIVATION_PENDING)) {
										Map<String, String> extraInfoMap = DBUtility
												.getAttributeMapFromXML(subscriberStatus
														.extraInfo());
										String packId = (extraInfoMap != null) ? extraInfoMap
												.get(EXTRA_INFO_PACK) : null;
										if (packId != null
												&& packId.equals(String
														.valueOf(cosId))) {
											refIDList.add(subscriberStatus
													.refID());
										}
									}
								}
							}

							if (refIDList.size() > 0) {
								m_rbtDBManager.deactivateNewSelections(
										strSubID, "Daemon", null, null, false,
										0, refIDList, null);
							}
						}

						removePackExtraInfoFromSubscriber(subscriber, cosId);
						
//						Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//								.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
						
//						List<String> musicPackCosIdList = null;
						
						
//						if(muiscPackCosIdParam != null) {
//							musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//						}
						
						if(UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosdetail.getCosType()) 
								|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosdetail.getCosType())) {
							deactivateMusicPackDownloads(subscriber, cosdetail);
						}
						else {
							// Deactivate all pack related personalized settings and
							// downloads
							deactivateDownloadsUnderPack(subscriber, cosdetail);
						}

						//<RBT-12066>
						String subscriptionClass = subscriber
								.subscriptionClass();
						if (cosdetail.getCosType() != null 
								&& cosdetail.getCosType().equalsIgnoreCase(MUSIC_POUCH)
								&& subClassesForBaseDeactOnMpFailureList != null
								&& subscriptionClass != null
								&& subClassesForBaseDeactOnMpFailureList.contains(subscriptionClass)) {
							boolean deactSelections = RBTParametersUtils.getParamAsBoolean("COMMON", "DEL_SELECTION_ON_DEACT", "TRUE");
							logger.info("subscriberId: "
									+ strSubID
									+ ", subscriptionClass: "
									+ subscriptionClass
									+ "deactSelections: " 
									+ deactSelections
									+ ". SubscriptionClass configured for base deactivation in "
									+ SUB_CLASSES_FOR_BASE_DEACT_ON_MP_FAILURE);
							
							boolean subscriberDeactStatus = m_rbtDBManager.smDeactivateSubscriber(strSubID, "DAEMON",
									null, deactSelections, true, true, type);
							logger.info("subscriberId: " + strSubID
									+ ", subscriberDeactStatus: "
									+ subscriberDeactStatus);
						}
						//</RBT-12066>
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure)) {
						return INVALID;
					} else {
						return FAILURE;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		} finally {
			RBTMonitorManager.getInstance().endNode(strSubID, node, status);
		}
	}
	
	// Jira :RBT-15026: Changes done for allowing
	// the multiple Azaan pack.
	private void removeOldAzaanPackOnSubscriber(String strSubID,
			Subscriber subscriber, String oldAzaanCosId,
			CosDetails oldCosDetails) {
		// Remove CosId from Extra info & Update
		// the provising table entry.
		List<ProvisioningRequests> provRequests = m_rbtDBManager
				.getBySubscriberIDAndType(subscriber.subID(),
						Integer.parseInt(oldAzaanCosId));
		if (null != provRequests && provRequests.size() > 0) {
			logger.info("Size for PacksToBeActivatedBy subscriber: "
					+ provRequests.size());
			logger.info("Deactivating the old azaan pack which is :"
					+ oldAzaanCosId);
			for (ProvisioningRequests provRequest : provRequests) {
				int packStatus = provRequest.getStatus();
				if (packStatus == iRBTConstant.PACK_ACTIVATION_PENDING
						|| packStatus == iRBTConstant.PACK_ACTIVATED) {
					logger.info("Deactivating the old azaan pack which is :"
							+ oldAzaanCosId);
					m_rbtDBManager.deactivatePack(subscriber, oldCosDetails,
							provRequest.getTransId(), DBUtility
									.getAttributeMapFromXML(provRequest
											.getExtraInfo()));
				} else {
					logger.info("Deactivating the old azaan pack which is :"
							+ oldAzaanCosId);
					boolean isDeactivated = m_rbtDBManager
							.directDeactivatePack(subscriber.subID(),
									provRequest.getTransId(),
									provRequest.getExtraInfo());
					logger.info("Removing the Pack from extra info");
					if (isDeactivated)
						removeAzaanCosFromPackExtraInfo(strSubID, subscriber,
								oldAzaanCosId);
				}
			}
		}
	}

	public void removePackExtraInfoFromSubscriber(Subscriber subscriber,
			int cosId) {
		String subExtraInfo = (subscriber != null) ? subscriber.extraInfo()
				: null;
		logger.info("Subscriber extra info is " + subExtraInfo);
		HashMap<String, String> extraMap = new HashMap<String, String>();
		if (subExtraInfo != null && subExtraInfo.contains(EXTRA_INFO_PACK)) {
			extraMap = DBUtility.getAttributeMapFromXML(subExtraInfo);
			String packs = extraMap.get(EXTRA_INFO_PACK);
			String finalPacks = "";
			if (packs != null) {
				// 1,2,3 or 3,2,1
				String[] packsplit = packs.split(",");
				if (logger.isDebugEnabled())
					logger.debug("The extra Info parameter Pack is :"
							+ packsplit);

				for (String eachPack : packsplit) {
					eachPack = eachPack.trim();
					if (eachPack.length() > 0
							&& Integer.parseInt(eachPack) != cosId) {
						if (finalPacks.length() != 0)
							finalPacks += ",";
						finalPacks += eachPack;
					}
				}

				if (finalPacks.length() == 0)
					extraMap.remove(EXTRA_INFO_PACK);
				else
					extraMap.put(EXTRA_INFO_PACK, finalPacks);

				String finalExtraInfo = DBUtility
						.getAttributeXMLFromMap(extraMap);
				updateExtraInfo(subscriber.subID(), finalExtraInfo);
			}
		}
	}


	private void addAzaanSubType(String subId,
			HashMap<String, String> extraInfoMap) {
		logger.debug("Adding SUB_TYPE to extra info. extraInfoMap: "
				+ extraInfoMap + ", subscriberId: " + subId);
		if (null == extraInfoMap || 0 == extraInfoMap.size()) {
			extraInfoMap = new HashMap<String, String>();
		}
		extraInfoMap.put("SUB_TYPE", "AZAAN");

		String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

		boolean updated = m_rbtDBManager.updateExtraInfoAndPlayerStatus(subId,
				extraInfo, "A");

		logger.info("Updated subscriber extra info. subscriberId: " + subId
				+ ", extraInfo: " + extraInfoMap + ", update status: "
				+ updated);
	}

	private void addAzaanCosToPackExtraInfo(String strSubID,
			Subscriber subscriber, Map<String, String> extraInfoMap,String cosType) {
		logger.debug("Checking the subscriber is activated on Azaan Cos. subscriberId: "
				+ strSubID);
		if (extraInfoMap.containsKey(EXTRA_INFO_PACK)) {
			String subscriberCosIds = extraInfoMap.get(EXTRA_INFO_PACK);
			List<String> subscriberCosIdList = ListUtils.convertToList(
					subscriberCosIds, ",");
			Set<String> commonCosIds = ListUtils.intersection(
					confAzaanCosIdList, subscriberCosIdList);
			
			boolean isAzaanCosType = false;
			if(cosType!=null && cosType.equalsIgnoreCase(iRBTConstant.AZAAN)){
				isAzaanCosType = true;
			}
			// One of the subscriber cos id is Azaan cos. So, update the player
			// status.
			if (commonCosIds.size() > 0 || isAzaanCosType) {
				logger.debug("Subscriber: "
						+ strSubID
						+ " is configured on configured Azaan CosIds. So, updating"
						+ " player status to A.");
				updatePlayerStatus(subscriber, "A");
			}
		} else {
			logger.debug("Could not add Azaan Cos. ExtraInfo does not contain"
					+ " PACK attribute. subscriberId: " + strSubID
					+ ", extraInfo: " + extraInfoMap);
		}
	}
	
	// Modified the method for Jira - RBT-15026 Passed the CosIds list which
	// need to be removed from the extra info
	private void removeAzaanCosFromPackExtraInfo(String strSubID,
			Subscriber subscriber, String azaanCosIds) {
		logger.debug("Removing Azaan cos id from extra info pack "
				+ " Subscriber: " + strSubID);

		String extraInfo = subscriber.extraInfo();
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (null == extraInfoMap) {
			logger.warn("Failed to remove cos ids from pack, extra info is null.");
		} else if (!extraInfoMap.containsKey(EXTRA_INFO_PACK)) {
			logger.warn("Failed to remove cos ids from pack, extra info does not contain pack."
					+ extraInfoMap);
		} else {
			String packInfo = extraInfoMap.get(EXTRA_INFO_PACK);
			String updatedExtraInfo = null;
			List<String> removeAzaanCosIds = null;
			if (null != azaanCosIds) {
				removeAzaanCosIds = Arrays.asList(azaanCosIds.split(","));
			}
			updatedExtraInfo = removeCosIdsFromExraInfo(packInfo, removeAzaanCosIds);
			if (null != updatedExtraInfo) {
				extraInfoMap.put(EXTRA_INFO_PACK, updatedExtraInfo);

				boolean updated = m_rbtDBManager.updateSubscriber(strSubID,
						null, null, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null,
						null, null, "A", extraInfoMap);
				logger.info("Removed Azaan Cos and updated" + " database. "
						+ "Updated subscriber: " + strSubID
						+ ", update status: " + updated);
			}

		}
	}

	private void removeAzaanSubType(String subId,
			HashMap<String, String> extraInfoMap) {
		logger.debug("Removing SUB_TYPE from extra info. extraInfoMap: "
				+ extraInfoMap);
		if (null != extraInfoMap && extraInfoMap.size() > 0
				&& extraInfoMap.containsKey("SUB_TYPE")) {
			extraInfoMap.remove("SUB_TYPE");
			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

			boolean updated = m_rbtDBManager.updateExtraInfoAndPlayerStatus(
					subId, extraInfo, "A");

			logger.info("Updated subscriber extra info. subscriberId: " + subId
					+ ", extraInfo: " + extraInfoMap + ", remove status: "
					+ updated);
		} else {
			logger.warn("Unable to remove SUB_TYPE from extra info. extraInfoMap: "
					+ extraInfoMap);
		}
	}
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	private String removeCosIdsFromExraInfo(String cosIdsStr,
			List<String> removeAzaanCosIds) {
		String packStr = null;
		if (null != cosIdsStr && cosIdsStr.length() > 0) {
			List<String> subscriberCosIds = ListUtils.convertToList(cosIdsStr,
					",");
			List<String> list = new ArrayList<String>();
			if (null != removeAzaanCosIds && !removeAzaanCosIds.isEmpty()) {
				list = ListUtils.removeCommonInLHS(subscriberCosIds,
						removeAzaanCosIds);
			} else {
				list = ListUtils.removeCommonInLHS(subscriberCosIds,
						confAzaanCosIdList);
			}
			if (subscriberCosIds.size() > list.size()) {
				packStr = ListUtils.convertToString(list, ",");
				logger.debug("Removed configured CosIds from subscriber extra info. "
						+ " packStr: " + packStr);
			}
		}
		logger.info("Removed CosIds from Extra info. " + " packStr: " + packStr);
		return packStr;
	}

	private boolean updatePlayerStatus(Subscriber subscriber, String state) {
		if (null == subscriber) {
			logger.warn("Could not update player status. subscriber is null.");
			return false;
		}

		boolean updated = m_rbtDBManager.updatePlayerStatus(subscriber.subID(),
				state);
		logger.debug("Tried to updated subscriber player status. subscriberId: "
				+ subscriber.subID()
				+ ", to state: "
				+ state
				+ ", updated: "
				+ updated);
		return updated;
	}

	public void updatePackExtraInfoFromSubscriber(Subscriber subscriber,
			int oldCosId, int newCosId) {
		if (oldCosId == newCosId) {
			logger.info("Both new and old cosIds are same, so not executing the query, oldCosId : "
					+ oldCosId + ", newCosId : " + newCosId);
			return;
		}

		String subExtraInfo = (subscriber != null) ? subscriber.extraInfo()
				: null;
		logger.info("Subscriber extra info is " + subExtraInfo);
		HashMap<String, String> extraMap = new HashMap<String, String>();
		if (subExtraInfo != null && subExtraInfo.contains(EXTRA_INFO_PACK)) {
			extraMap = DBUtility.getAttributeMapFromXML(subExtraInfo);
			String packs = extraMap.get(EXTRA_INFO_PACK);
			String finalPacks = "";
			if (packs != null) {
				String[] packsplit = packs.split(",");
				if (logger.isDebugEnabled())
					logger.debug("The extra Info parameter Pack is :"
							+ packsplit);

				for (String eachPack : packsplit) {
					eachPack = eachPack.trim();
					if (eachPack.length() > 0
							&& Integer.parseInt(eachPack) != oldCosId) {
						finalPacks += eachPack + ",";
					}

				}

				finalPacks += String.valueOf(newCosId);
				extraMap.put(EXTRA_INFO_PACK, finalPacks);

				String finalExtraInfo = DBUtility
						.getAttributeXMLFromMap(extraMap);
				updateExtraInfo(subscriber.subID(), finalExtraInfo);
			}
		}
	}

	// RBT-14301: Uninor MNP changes.
	public String refundSelection(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String reason, String circleIDFromPrism) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";

		try {
			String callerID = null;
			int songStatus = -1;
			String setTime = null;
			int fromTime = 0;
			int toTime = 2359;
			// String wavFile = null;
			String selStatus = null;
			String extraInfo = null;
			char oldLoopStatus = LOOP_STATUS_ERROR;
			int rbtType = 0;

			if (status == null || action == null || strSubID == null
					|| type == null) {
				logger.info("RBT::Returning false as status " + status
						+ " action " + action + " strSubID " + strSubID
						+ " refID " + refID + " type " + type);
				return FAILURE;
			}

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			SubscriberStatus subscriberStatus = m_rbtDBManager
					.getRefIDSelection(conn, strSubID, refID);

			if (subscriberStatus == null) {
				logger.info("Couldn't get selection using refID RBT::Returning false as status "
						+ status
						+ " action "
						+ action
						+ " strSubID "
						+ strSubID + " refID " + refID + " type " + type);

				return "INVALID|SELECTION NOT FOUND";
			}

			callerID = subscriberStatus.callerID();
			songStatus = subscriberStatus.status();
			setTime = getFormattedDate(sdf, subscriberStatus.setTime());
			fromTime = subscriberStatus.fromTime();
			toTime = subscriberStatus.toTime();
			selStatus = subscriberStatus.selStatus();
			extraInfo = subscriberStatus.extraInfo();
			oldLoopStatus = subscriberStatus.loopStatus();
			if (getParamAsBoolean("COMMON", "IS_RRBT_ON", "FALSE"))
				rbtType = subscriberStatus.selType();

			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap == null || !extraInfoMap.containsKey(REFUND))
				return INVALID + "|" + "NON REFUNDABLE SELECTION";

			if (m_statusSuccess.equalsIgnoreCase(status)) {
				if (selStatus == null)
					return SELECTION_NOT_EXISTS;
				/*// RBT-14301: Uninor MNP changes.
				if (subscriberStatus != null) {
					String circleId = subscriberStatus.circleId();
					if (circleIDFromPrism != null && circleId != null
							&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
						smUpdateCircleIdForSubscriber(false, subscriberStatus.subID(),
								circleIDFromPrism, subscriberStatus.refID(), null);
					}
				}*/
				if (selStatus.equals(STATE_ACTIVATED)) {
					logger.info("Selection is in active state");
					extraInfo = DBUtility.setXMLAttribute(extraInfo, REFUNDED,
							"TRUE");
					smDeactivateRefundedSelection(strSubID, refID,
							oldLoopStatus, callerID, rbtType, extraInfo);

					/*
					 * If present selection is in loop , then no need to
					 * reactivate the song Else i) Get all selections whose
					 * deactRefID is same as refID of present selection ii)
					 * reactivate the song whose deactRefId is same as refid of
					 * present selection and startTime not equal to 20040101.
					 * update extraInfo column of reactivate song (remove
					 * deactRefID and put param RECTIVE= TRUE so that daemon
					 * hits diff API when it hits SM) iii) For selections with
					 * deactRefID same as that of refID of refund song and
					 * startTime equal to 20040101 update sel_status to A
					 */

					if (oldLoopStatus == LOOP_STATUS_OVERRIDE
							|| oldLoopStatus == LOOP_STATUS_OVERRIDE_FINAL
							|| oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT)
						smReactivatePrevActiveSelection(strSubID, callerID,
								refID, setTime, songStatus, fromTime, toTime,
								rbtType);

					return SUCCESS;
				} else {
					extraInfo = DBUtility.setXMLAttribute(extraInfo, REFUNDED,
							"TRUE");
					smDeactivateRefundedSelection(strSubID, refID,
							oldLoopStatus, callerID, rbtType, extraInfo);
					return SUCCESS;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		}

		return null;
	}

	public String refundSubscription(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String failureInfo,
			String reason, String circleIDFromPrism) {// RBT-14301: Uninor MNP changes.
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";

		logger.info("RBT:: status " + status + " strSubID " + strSubID
				+ " type " + type);

		if (status == null || strSubID == null || type == null) {
			logger.info("RBT::Returning false as status " + status
					+ " strSubID " + strSubID + " type " + type);
			return FAILURE;
		}

		try {
			String subscriptionYes = null;
			String extraInfo = null;
			boolean deactSelOnRefund = true;

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(
					conn, strSubID);

			if (subscriber == null)
				return SUBSCRIPTION_DOES_NOT_EXIST;

			subscriptionYes = subscriber.subYes();
			extraInfo = subscriber.extraInfo();

			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap == null || !extraInfoMap.containsKey(REFUND))
				return INVALID + "|" + "NON REFUNDABLE SUBSCRIPTION";

			if (m_statusSuccess.equalsIgnoreCase(status)) {
				// RBT-14301: Uninor MNP changes.
				if (subscriber != null) {
					String circleId = subscriber.circleID();
					if (circleId != null && circleIDFromPrism != null
							&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
						smUpdateCircleIdForSubscriber(true, subscriber.subID(),
								circleIDFromPrism, subscriber.refID(), null);
					}
				}
				// If the subscriber is in B, N, A state, deactivate the
				// subscription and update extraInfo with REFUNDED=TRUE
				if (subscriptionYes.equals(STATE_ACTIVATED)
						|| subscriptionYes.equals(STATE_ACTIVATION_PENDING)
						|| subscriptionYes.equals(STATE_TO_BE_ACTIVATED)
						|| subscriptionYes.equals(STATE_SUSPENDED)
						|| subscriptionYes.equals(STATE_CHANGE)) {
					extraInfo = DBUtility.setXMLAttribute(extraInfo, REFUNDED,
							"TRUE");
					// Added
					// boolean param - deactSelOnRefund (Deativate Selection on
					// Refund)
					// String extraInfo - extraInfo
					String deactStatusStr = smDeactivationSuccess(strSubID,
							subscriptionYes, classType, reason,
							deactSelOnRefund, extraInfo, subscriber.language(),
							true);
					if (deactStatusStr.equalsIgnoreCase(m_success)) {
						SRBTUtility.updateSocialSubscriberForDeactivation(
								m_socialRBTAllowed,
								m_socialRBTAllUpdateInOneModel, strSubID,
								rbtSystemType, classType);
					}
					m_rbtDBManager.removeViralSMSOfCaller(strSubID, "GIFTED");

				} else {
					extraInfo = DBUtility.setXMLAttribute(extraInfo, REFUNDED,
							"TRUE");
					m_rbtDBManager.updateExtraInfo(strSubID, extraInfo);

				}

				return SUCCESS;
			}
		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		}
		return null;
	}

	public String packSubscription(String strSubID, String action,
			String chargedDate, String status, String refID, String type,
			String amountCharged, String classType, String failureInfo,
			String reason, String reasonCode, String currentSubStatus,
			String cosID, String offerID) {
		String SUCCESS = "SUCCESS";
		String FAILURE = "FAILURE";
		String INVALID = "INVALID";

		try {
			String subscriptionYes = null;
			String extraInfo = null;
			String activatedCosID = null;
			int noMaxSelections = 0;

			logger.info("RBT:: status " + status + " action " + action
					+ " strSubID " + strSubID + " refID " + refID + " type "
					+ type + " cosID " + cosID);

			if (status == null || action == null || strSubID == null
					|| type == null) {
				return FAILURE;
			}

			Connection conn = m_rbtDBManager.getConnection();
			if (conn == null)
				return FAILURE;

			Subscriber subscriber = m_rbtDBManager.getSubscriberForSMCallbacks(
					conn, strSubID);
			if (subscriber == null)
				return SUBSCRIPTION_DOES_NOT_EXIST;

			CosDetails cosDetails = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(cosID);

			subscriptionYes = subscriber.subYes();
			extraInfo = subscriber.extraInfo();
			cosID = subscriber.cosID();
			activatedCosID = subscriber.activatedCosID();
			noMaxSelections = subscriber.maxSelections();

			if (m_strActionActivation.equalsIgnoreCase(action)) {
				if (m_statusSuccess.equalsIgnoreCase(status)) {
					// ACTIVATION SUCCESS
					if (subscriptionYes == null)
						return INVALID;

					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap == null)
						extraInfoMap = new HashMap<String, String>();

					if (cosDetails != null
							&& UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails
									.getCosType())) {
						// Unlimited Downloads callback request
						logger.info("processing unlimited downloads activation request for subscriber: "
								+ strSubID);
						if (!extraInfoMap.containsKey(EXTRA_INFO_COS_ID))
							return INVALID + "|" + "NO UPGRADATION FOUND";
						if (!extraInfoMap
								.containsKey(EXTRA_INFO_TOBE_ACT_OFFER_ID))
							return INVALID + "|" + "NO UPGRADATION FOUND";

						extraInfoMap.put(EXTRA_INFO_OFFER_ID,
								extraInfoMap.get(EXTRA_INFO_TOBE_ACT_OFFER_ID));
						extraInfoMap.remove(EXTRA_INFO_TOBE_ACT_OFFER_ID);
					} else {
						// Songpack/limited download callback request
						if (subscriptionYes.equals("B"))
							return SUCCESS;
						if (!subscriptionYes.equalsIgnoreCase(STATE_CHANGE)
								&& !subscriptionYes
										.equalsIgnoreCase(STATE_ACTIVATION_PENDING))
							return INVALID + "|" + "NO UPGRADATION FOUND";
						extraInfoMap.remove("MAX_SEL");
					}

					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					String ret = smPackSubscriptionSuccess(strSubID, extraInfo);

					if (ret.trim().equalsIgnoreCase(m_success)) {// RBT-14301: Uninor MNP changes.
						if (!smUpdateSelStatusSubscriptionSuccess(false, strSubID, null))
							return FAILURE;
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure))
						return INVALID;
					else
						return FAILURE;
				} else {
					// ACTIVATION FAILURE

					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (extraInfoMap == null)
						extraInfoMap = new HashMap<String, String>();

					if (cosDetails != null
							&& UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails
									.getCosType())) {
						if (!extraInfoMap.containsKey(EXTRA_INFO_COS_ID))
							return INVALID;
						else {
							extraInfoMap.remove(EXTRA_INFO_COS_ID);
							extraInfoMap.remove(EXTRA_INFO_TOBE_ACT_OFFER_ID);
						}
					} else {
						if (subscriptionYes.equals("B"))
							return SUCCESS;
						if (!subscriptionYes.equalsIgnoreCase(STATE_CHANGE)
								&& !subscriptionYes
										.equalsIgnoreCase(STATE_ACTIVATION_PENDING))
							return INVALID + "|" + "NO UPGRADATION FOUND";

						noMaxSelections = 0;
						if (extraInfoMap.containsKey("MAX_SEL")) {
							noMaxSelections = Integer.parseInt(extraInfoMap
									.get("MAX_SEL"));
							extraInfoMap.remove("MAX_SEL");
						}

						if (cosID != null
								&& !cosID.equalsIgnoreCase(activatedCosID))
							cosID = activatedCosID;
						else {
							String isPrepaid = (subscriber.prepaidYes()) ? "y"
									: "n";
							CosDetails defaultCos = CacheManagerUtil
									.getCosDetailsCacheManager()
									.getDefaultCosDetail(subscriber.circleID(),
											isPrepaid);
							if (defaultCos != null)
								cosID = defaultCos.getCosId();
						}
					}
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					String ret = smPackSubscriptionFailure(strSubID, cosID,
							noMaxSelections, extraInfo);

					if (ret.trim().equalsIgnoreCase(m_success)) {
						if (getParamAsBoolean(
								"UPDATE_SONGS_TO_DEFAULT_ON_PACK_FAILURE",
								"FALSE")) {
							m_rbtDBManager
									.smUpdateSongsToDefaultOnPackActivationFailure(strSubID);
						} else {
							if (getParamAsBoolean("COMMON", "ADD_TO_DOWNLOADS",
									"FALSE"))
								m_rbtDBManager
										.expireAllSubscriberPendingDownload(
												strSubID, "PackActFail", null);

							deactivateBaseActPendingSelections(strSubID);
						}
						return SUCCESS;
					} else if (ret.equalsIgnoreCase(m_failure))
						return INVALID;
					else
						return FAILURE;
				}
			} else if ((m_strActionDeactivation.equalsIgnoreCase(action) && m_statusSuccess
					.equalsIgnoreCase(status))
					|| (m_strActionRental.equalsIgnoreCase(action) && m_statusFailure
							.equalsIgnoreCase(status))) {
				logger.info("Renewal/Dct request CosType :::"
						+ cosDetails.getCosType());
				if (cosDetails != null
						&& (AZAAN.equalsIgnoreCase(cosDetails
								.getCosType()) ||LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails
								.getCosType()) || LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails
										.getCosType()) || UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails
												.getCosType()))) {
					int numMaxSelections = 0;
					// If renewal is failed, update the user to default cos
					String subType = subscriber.prepaidYes() ? "y" : "b";
					cosDetails = CacheManagerUtil
							.getCosDetailsCacheManager()
							.getDefaultCosDetail(subscriber.circleID(), subType);

					return m_rbtDBManager.smPackSubscriptionRenewalCallback(
							strSubID, cosDetails.getCosId(), numMaxSelections,
							extraInfo);

				} else if (cosDetails != null
						&& UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails
								.getCosType())) {
					if (extraInfo == null)
						return INVALID;

					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					extraInfoMap.remove(EXTRA_INFO_COS_ID);
					extraInfoMap.remove(EXTRA_INFO_TOBE_ACT_OFFER_ID);
					extraInfoMap.remove(EXTRA_INFO_OFFER_ID);

					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					boolean success = m_rbtDBManager.updateExtraInfo(strSubID,
							extraInfo);

					if (success)
						return SUCCESS;
					else
						return FAILURE;
				}
			} else if (m_strActionRental.equalsIgnoreCase(action)
					&& m_statusSuccess.equalsIgnoreCase(status)) {
				if (cosDetails != null
						&& (AZAAN.equalsIgnoreCase(cosDetails
								.getCosType()) ||LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails
								.getCosType()) || LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails
										.getCosType()) || UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails
												.getCosType()))) {
					int numMaxSelections = 0; // Pack renewal call back,
												// resetting numMaxSelections to
												// Zero
					m_rbtDBManager.smPackSubscriptionRenewalCallback(strSubID,
							cosDetails.getCosId(), numMaxSelections, extraInfo);
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return FAILURE;
		}
		return SUCCESS;

	}

	private String getCreateTime() {
		String createTime = null;
		String blackoutSMSPeriod = RBTParametersUtils.getParamAsString(
				iRBTConstant.SMS, "BLACKOUT_SMS_PERIOD", null);
		if (blackoutSMSPeriod != null) {
			String[] start_end = blackoutSMSPeriod.split("-");
			if (start_end.length == 2) {
				String startTime = start_end[0];
				String endTime = start_end[1];

				StringTokenizer start = new StringTokenizer(startTime, ":");
				int startHour = Integer.parseInt(start.nextToken());
				int startMin = 0;
				int startSec = 0;
				if (start.hasMoreTokens())
					startMin = Integer.parseInt(start.nextToken());
				if (start.hasMoreTokens())
					startSec = Integer.parseInt(start.nextToken());

				long startTimeInMillis = (startHour * 3600 + startMin * 60 + startSec) * 1000;

				StringTokenizer end = new StringTokenizer(endTime, ":");
				int endHour = Integer.parseInt(end.nextToken());
				int endMin = 0;
				int endSec = 0;
				if (end.hasMoreTokens())
					endMin = Integer.parseInt(end.nextToken());
				if (end.hasMoreTokens())
					endSec = Integer.parseInt(end.nextToken());

				long endTimeInMillis = (endHour * 3600 + endMin * 60 + endSec) * 1000;

				Calendar cal = Calendar.getInstance();
				int curHour = cal.get(Calendar.HOUR_OF_DAY);
				int curMin = cal.get(Calendar.MINUTE);
				int curSec = cal.get(Calendar.SECOND);

				long curTimeInMillis = (curHour * 3600 + curMin * 60 + curSec) * 1000;
				long createTimeInMillis = 0;
				Date endDate = null;
				if (startTimeInMillis < endTimeInMillis) {
					if (curTimeInMillis > startTimeInMillis
							&& curTimeInMillis < endTimeInMillis) {
						createTimeInMillis = cal.getTimeInMillis()
								+ endTimeInMillis - curTimeInMillis;
						endDate = new Date(createTimeInMillis);
						createTime = toSQLDate(endDate, sqlTimeSpec,
								sqlTimeFormat);
					}

				} else {
					if (curTimeInMillis > startTimeInMillis
							&& curTimeInMillis < (24 * 3600 * 1000)) {
						createTimeInMillis = cal.getTimeInMillis()
								+ endTimeInMillis + 24 * 3600 * 1000
								- curTimeInMillis;
						endDate = new Date(createTimeInMillis);
						createTime = toSQLDate(endDate, sqlTimeSpec,
								sqlTimeFormat);
					} else if (curTimeInMillis > 0
							&& curTimeInMillis < endTimeInMillis) {
						createTimeInMillis = cal.getTimeInMillis()
								+ endTimeInMillis - curTimeInMillis;
						endDate = new Date(createTimeInMillis);
						createTime = toSQLDate(endDate, sqlTimeSpec,
								sqlTimeFormat);
					}
				}
			}
		}

		if (createTime == null
				&& getParamAsBoolean("START_PLAYER_DAEMON", "FALSE")
				&& getParamAsInt("SLEEP_INTERVAL_MINUTES", -1) > 0) {
			Calendar curCal = Calendar.getInstance();
			curCal.add(Calendar.MINUTE,
					getParamAsInt("SLEEP_INTERVAL_MINUTES", -1) + 1);
			createTime = toSQLDate(curCal.getTime(), sqlTimeSpec, sqlTimeFormat);
		}
		return createTime;
	}

	private String toSQLDate(Date date, String spec, DateFormat format) {
		return "TO_DATE('" + format.format(date) + "', '" + spec + "')";
	}

	private String getParseSMS(String smsText, double amtCharged,
			double amtPending) {
		if (smsText == null)
			return null;
		String returnedSMS = smsText.replaceAll("%S", "" + amtCharged);
		returnedSMS = returnedSMS.replaceAll("%C", "" + amtPending);
		return returnedSMS;
	}

	private String smSubscriptionSuccess(String strSubID, Date nextDate,
			Date actDate, String type, String classType, boolean isPeriodic,
			String finalActInfo, boolean updatePlayStatus, String extraInfo,
			String subscriptionYes, String strNextBillingDate) {
		return smSubscriptionSuccess(strSubID, nextDate, actDate, type,
				classType, isPeriodic, finalActInfo, false, updatePlayStatus,
				extraInfo, null, -1, subscriptionYes, null, -1, strNextBillingDate);
	}

	private String smSubscriptionSuccess(String strSubID, Date nextDate,
			Date actDate, String type, String classType, boolean isPeriodic,
			String finalActInfo, boolean updateEndtime,
			boolean updatePlayStatus, String extraInfo, String upgradingCosID,
			int validity, String subscriptionYes, String oldSub, int rbtType, String strNextBillingDate) {
		if (extraInfo != null && extraInfo.length() == 0) {
			extraInfo = null;
		}
		return (m_rbtDBManager.smSubscriptionSuccess(strSubID, nextDate,
				actDate, type, classType, isPeriodic, finalActInfo,
				updateEndtime, updatePlayStatus, extraInfo, upgradingCosID,
				validity, subscriptionYes, strNextBillingDate));
	}

	// Added deActSelOnRefund and extraInfo - TRAI changes
	private String smDeactivationSuccess(String strSubID, String subYes,
			String classType, String reason, boolean deactSelOnRefund,
			String extraInfo, String subscriberLanguage, boolean updatePlayer) {

		if (!getParamAsBoolean("COMMON", "DEL_SELECTIONS", "TRUE")
				|| deactSelOnRefund) {
			m_rbtDBManager.deactivateSubscriberRecords(strSubID);
		}
		// Added for RBT-16795 Deactivation of songs from downloads not happening when the Base is deactivated
		if(getParamAsBoolean("COMMON","DEL_DOWNLOADS","FALSE") && getParamAsBoolean("COMMON",
				"ADD_TO_DOWNLOADS", "FALSE")){
			String deactivatedBy = getParamAsString(iRBTConstant.COMMON,
					"CALLBACKS_DEACTIVATE_MODE", "DAEMON");
			m_rbtDBManager.expireAllSubscriberDownloadBaseDct(strSubID, deactivatedBy);
		}
		
		String smsText = getSubClassSMSText(classType, false,
				subscriberLanguage);
		if (smsText != null)
			smsText = getReasonSMS(smsText, reason);
		String retVal = m_rbtDBManager.smDeactivationSuccess(strSubID, subYes,
				extraInfo, updatePlayer);
		if (getParamAsBoolean("SEND_DEACT_SMS", "FALSE")
				&& m_success.equalsIgnoreCase(retVal) && smsText != null) {
			sendSMS(strSubID, smsText, null);
		}
		return retVal;
	}

	private boolean updateEndDate(String strSubID, Date next, String subClasses) {
		return (m_rbtDBManager.updateEndDate(strSubID, next, subClasses));
	}

	private boolean updateExtraInfo(String strSubID, String extraInfo) {
		return (m_rbtDBManager.updateExtraInfo(strSubID, extraInfo));
	}

	private String smRenewalSuccess(String strSubID, Date nextDate,
			String type, String classType, String actInfo, String extraInfo, String strNextBillingDate,boolean playstatusA) {
		return (m_rbtDBManager.smRenewalSuccess(strSubID, nextDate, type,
				classType, actInfo, extraInfo, strNextBillingDate,playstatusA));
	}

	/*
	 * private String smUnsuspendSelections(String strSubID) { return
	 * (m_rbtDBManager .smUnsuspendSelections(strSubID)); }
	 */

	private String smSelectionActivationSuccess(String strSubID,
			String callerID, int status, String setTime, Date nextChargingDate,
			Date startDate, String type, int fromTime, int toTime,
			String classType, char oldLoopStatus, boolean isPrepaid,
			String selInfo, int rbtType, String selInterval, String refID,
			String extraInfo, Clip clip, List<String> songWavFilesList,
			boolean updateSelStatus, String circleId) {

		boolean deativateSelections = true;
		String allowStatuses = RBTParametersUtils.getParamAsString("COMMON",
				"ALLOW_SELECTIONS_WITH_SAME_STATUS", null);
		if (allowStatuses != null) {
			String statuses[] = allowStatuses.split(",");
			if (statuses != null && statuses.length > 0) {

				for (int i = 0; i < statuses.length; i++) {
					if (statuses[i].equals(String.valueOf(status)))
						deativateSelections = false;
				}
			}

		}

		// Checks if the subscriber is having any override shuffle selections or
		// not
		if (RBTParametersUtils.getParamAsBoolean(COMMON,
				"OVERRIDE_SHUFFLE_UPON_NEW_SELECTION", "FALSE") && status == 1) {
			SubscriberStatus[] activeSelections = m_rbtDBManager
					.getAllActiveSubscriberSettings(strSubID);
			if (activeSelections != null && activeSelections.length > 0) {
				ArrayList<String> toDeactRefIDList = new ArrayList<String>();
				for (SubscriberStatus selection : activeSelections) {
					if (refID.equals(selection.refID()))
						continue;

					if (!Utility.isShuffleCategory(selection.categoryType()))
						continue;

					// gets the shuffle categoryIDs for which override is
					// supported
					String catIDs = RBTParametersUtils.getParamAsString(COMMON,
							"SHUFFLE_CAT_IDS_TO_BE_OVERRIDDEN", "");
					if (catIDs == null || catIDs.equals(""))
						continue;

					List<String> catIDsList = Arrays.asList(catIDs.split(","));
					if (catIDsList.contains(String.valueOf(selection
							.categoryID()))) {
						toDeactRefIDList.add(selection.refID());
						songWavFilesList.add(selection.subscriberFile());
					}
				}
				if (toDeactRefIDList.size() > 0)
					m_rbtDBManager.smDeactivateOldSelectionBasedOnRefID(
							strSubID, callerID, setTime, fromTime, toTime,
							rbtType, selInterval, refID, toDeactRefIDList);
			}
		}

		char newLoopStatus = m_rbtDBManager.getLoopStatusToUpateSelection(
				oldLoopStatus, strSubID, isPrepaid);

		SubscriberStatus[] subscriberStatus = null;
		logger.info(" new Loop Status  > " + newLoopStatus + " & extraInfo >"
				+ extraInfo);

		String giftChargeClasses = getParamAsString(iRBTConstant.COMMON,
				"OPTIN_GIFT_CHARGE_CLASS", null);
		List<String> giftChargeClassList = null;
		if (giftChargeClasses != null) {
			giftChargeClassList = Arrays.asList(giftChargeClasses.split("\\,"));
		}

		/*
		 * If extraInfo of selection contains REFUND = TRUE i.e copy/OBD
		 * selection i) get all selections whose setTime < setTime of current
		 * Selection ii)Deactivate song and update extraInfo column with
		 * deactRefID (RefID of currentSelection)
		 */
		if (newLoopStatus == LOOP_STATUS_OVERRIDE
				|| newLoopStatus == LOOP_STATUS_OVERRIDE_INIT
				|| newLoopStatus == LOOP_STATUS_OVERRIDE_FINAL) {
			if (getParamAsBoolean("COMMON", "IS_TRAI_REGULATION_ACTIVE",
					"FALSE")) {
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null && extraInfoMap.containsKey(REFUND)) {
					ArrayList<String> addList = new ArrayList<String>();
					ArrayList<String> createList = new ArrayList<String>();
					ArrayList<String> addLoopList = new ArrayList<String>();
					ArrayList<String> createLoopList = new ArrayList<String>();
					subscriberStatus = m_rbtDBManager
							.getAllSubSelectionRecordsForReactivation(strSubID,
									callerID, null, setTime, rbtType, status,
									fromTime, toTime, refID);
					for (int i = 0; subscriberStatus != null
							&& i < subscriberStatus.length; i++) {
						String deactiveSelStatus = subscriberStatus[i]
								.selStatus();
						logger.info("deactiveSelStatus > " + deactiveSelStatus);
						if (isSelectionInActCategory(deactiveSelStatus)) {
							String selExtraInfo = subscriberStatus[i]
									.extraInfo();
							HashMap<String, String> selExtraInfoMap = DBUtility
									.getAttributeMapFromXML(selExtraInfo);
							logger.info("selExtraInfoMap > " + selExtraInfoMap);

							char deactLoopStatus = subscriberStatus[i]
									.loopStatus();
							if ((deactLoopStatus == LOOP_STATUS_LOOP
									|| deactLoopStatus == LOOP_STATUS_LOOP_FINAL || deactLoopStatus == LOOP_STATUS_LOOP_INIT)) {
								if (selExtraInfoMap == null) {
									createLoopList.add(subscriberStatus[i]
											.refID());
									songWavFilesList.add(subscriberStatus[i]
											.subscriberFile());
								} else if (selExtraInfoMap != null
										&& !selExtraInfoMap
												.containsKey(DEACT_REFID)) {
									addLoopList
											.add(subscriberStatus[i].refID());
									songWavFilesList.add(subscriberStatus[i]
											.subscriberFile());
								}
							} else {
								if (selExtraInfoMap == null) {
									createList.add(subscriberStatus[i].refID());
									songWavFilesList.add(subscriberStatus[i]
											.subscriberFile());
								} else if (selExtraInfoMap != null
										&& !selExtraInfoMap
												.containsKey(DEACT_REFID)) {
									addList.add(subscriberStatus[i].refID());
									songWavFilesList.add(subscriberStatus[i]
											.subscriberFile());
								}
							}
						}
					}
					logger.info("createList >" + createList + " & addList >"
							+ addList);
					if (createList.size() > 0 && deativateSelections) {
						String extraInfoStr = getExtraInfoQueryString("create",
								refID, null, false);
						m_rbtDBManager.smUpdateAndDeactivateOldSelection(
								strSubID, createList, extraInfoStr, setTime,
								rbtType);
					}
					if (addList.size() > 0 && deativateSelections) {
						String extraInfoStr = getExtraInfoQueryString("add",
								refID, null, false);
						m_rbtDBManager.smUpdateAndDeactivateOldSelection(
								strSubID, addList, extraInfoStr, setTime,
								rbtType);
					}
					if (createLoopList.size() > 0 && deativateSelections) {
						String extraInfoStr = getExtraInfoQueryString("create",
								refID, null, true);
						m_rbtDBManager.smUpdateAndDeactivateOldSelection(
								strSubID, createLoopList, extraInfoStr,
								setTime, rbtType);
					}
					if (addLoopList.size() > 0 && deativateSelections) {
						String extraInfoStr = getExtraInfoQueryString("add",
								refID, null, true);
						m_rbtDBManager.smUpdateAndDeactivateOldSelection(
								strSubID, addLoopList, extraInfoStr, setTime,
								rbtType);
					}
				} else {
					logger.info(" Its normal selection check if the user is activated through *Copy and if yes, remove REFUND = TRUE from extraInfo");
					Subscriber sub = m_rbtDBManager.getSubscriber(strSubID);
					if (sub != null) {
						String subExtraInfo = sub.extraInfo();
						HashMap<String, String> subExtraInfoMap = DBUtility
								.getAttributeMapFromXML(subExtraInfo);

						if (subExtraInfoMap != null
								&& subExtraInfoMap.containsKey(REFUND))
							subExtraInfo = DBUtility.removeXMLAttribute(
									subExtraInfo, REFUND);

						m_rbtDBManager.updateExtraInfo(strSubID, subExtraInfo);
					}

					List<String> rbtWavFilesList = m_rbtDBManager
							.smGetAllDeactivateOldSelection(strSubID, callerID,
									status, setTime, fromTime, toTime, rbtType,
									selInterval, refID);

					if (rbtWavFilesList != null && rbtWavFilesList.size() > 0) {
						songWavFilesList.addAll(rbtWavFilesList);
					}

					if (deativateSelections) {
						String ret = m_rbtDBManager.smDeactivateOldSelection(
								strSubID, callerID, status, setTime, fromTime,
								toTime, rbtType, selInterval, refID,false);
						if (ret != null && ret.equals(m_failure))
							return "FAILURE";
					}
				}
			} else {
				List<String> rbtWavFilesList = m_rbtDBManager
						.smGetAllDeactivateOldSelection(strSubID, callerID,
								status, setTime, fromTime, toTime, rbtType,
								selInterval, refID);

				if (rbtWavFilesList != null && rbtWavFilesList.size() > 0) {
					songWavFilesList.addAll(rbtWavFilesList);
				}

				if (deativateSelections) {
					Set<String> tobeDeactivatedDownloads = null;
					if (getParamAsBoolean("DEACTIVATE_OLDER_SHUFFLE_DOWNLOADS",
							"FALSE")) {
						SubscriberStatus[] activeSelections = m_rbtDBManager
								.getAllActiveSubscriberSettings(strSubID);
						SubscriberStatus currSelection = m_rbtDBManager
								.getSelectionByRefId(strSubID, refID);
						if (activeSelections != null) {
							tobeDeactivatedDownloads = new HashSet<String>();
							for (int i = 0; i < activeSelections.length; i++) {

								if ("GIFT".equalsIgnoreCase(activeSelections[i]
										.selectedBy())
										&& giftChargeClassList
												.contains(activeSelections[i]
														.classType())) {
									continue;
								}

								if (Utility
										.isShuffleCategory(activeSelections[i]
												.categoryType())
										&& Utility.isCallerIDSame(
												activeSelections[i].callerID(),
												callerID)) {
									String subWavFile = activeSelections[i]
											.subscriberFile();
									if (!tobeDeactivatedDownloads
											.contains(subWavFile))
										tobeDeactivatedDownloads
												.add(subWavFile);
									// tobeDeactivatedDownloads.put(subWavFile,tobeDeactivatedDownloads.get(subWavFile)+1);
								}
								if (Utility
										.isShuffleCategory(activeSelections[i]
												.categoryType())
										&& !(Utility.isCallerIDSame(
												activeSelections[i].callerID(),
												callerID))) {
									String subWavFile = activeSelections[i]
											.subscriberFile();
									if (tobeDeactivatedDownloads
											.contains(subWavFile))
										tobeDeactivatedDownloads
												.remove(subWavFile);
								}
							}
							tobeDeactivatedDownloads.remove(currSelection
									.subscriberFile());
						}
					}

					String ret = m_rbtDBManager.smDeactivateOldSelection(
							strSubID, callerID, status, setTime, fromTime,
							toTime, rbtType, selInterval, refID,false);
					if (ret != null && ret.equals(m_success)
							&& tobeDeactivatedDownloads != null
							&& tobeDeactivatedDownloads.size() > 0) {
						logger.info("Deactivating Downloads on deactivating Selections....");
						for (String promoId : tobeDeactivatedDownloads) {
							m_rbtDBManager.deactivateSubscriberDownload(
									strSubID, promoId, "SM");
						}
					}
					if (ret != null && ret.equals(m_failure))
						return "FAILURE";
				}
			}

		} else if (getParamAsBoolean("IBM_SM_INTEGRATION", "FALSE")) {
			if (deativateSelections) {
				SubscriberStatus subStatus[] = m_rbtDBManager
						.getAllActiveSubscriberSettings(strSubID);
				if (subStatus != null && subStatus.length > 1) {
					for (int i = subStatus.length - 1; i > 0
							&& isSameCaller(subStatus[i - 1].callerID(),
									callerID); i--) {
						if (refID.equalsIgnoreCase(subStatus[i].refID())
								&& i > 0) {
							List<String> refIdList = new ArrayList<String>();
							if (!(subStatus[i - 1].loopStatus() == 'l'
									|| subStatus[i - 1].loopStatus() == 'L' || subStatus[i - 1]
									.loopStatus() == 'A')) {
								refIdList.add(subStatus[i - 1].refID());
							}
							String retVal = null;
							if (refIdList != null && refIdList.size() > 0) {
								retVal = m_rbtDBManager
										.smDeactivateOldSelectionBasedOnRefID(
												strSubID, callerID, setTime,
												fromTime, toTime, rbtType,
												selInterval, refID, refIdList);
							}
							if (retVal != null && retVal.equals(m_failure))
								return "FAILURE";
						}
					}
				}
			}
		}
		deactivateExistingEmotionSelectionIfExists(strSubID, clip,
				songWavFilesList);

		return (m_rbtDBManager.smSelectionActivationSuccess(strSubID, callerID,
				status, setTime, nextChargingDate, startDate, type, fromTime,
				toTime, classType, newLoopStatus, selInfo, rbtType,
				selInterval, refID, extraInfo, updateSelStatus, circleId));
	}

	private void deactivateExistingEmotionSelectionIfExists(String strSubID,
			Clip clip, List<String> songWavFileList) {
		if (clip != null
				&& ("EMOTION_RBT".equalsIgnoreCase(clip.getContentType()) || "EMOTION_UGC"
						.equalsIgnoreCase(clip.getContentType()))) {
			String emotionConfig = getParamAsString("COMMON",
					"EMOTION_RBT_DEFAULT_CONFIG");
			if (emotionConfig != null) {
				int clipID = 0;
				String[] tokens = emotionConfig.split(",");
				clipID = Integer.parseInt(tokens[0]);

				// If clip is Default emotion clip.. no need to process
				if (clip.getClipId() == clipID)
					return;

				SubscriberStatus[] subscriberSelections = m_rbtDBManager
						.getAllActiveSubscriberSettings(strSubID);
				if (subscriberSelections == null
						|| subscriberSelections.length == 0)
					return;

				for (SubscriberStatus subStatus : subscriberSelections) {
					Clip subscriberClip = m_rbtCacheManager
							.getClipByRbtWavFileName(subStatus.subscriberFile());

					// Clip is not emotion clip .. continue
					if (subscriberClip == null
							|| (!"EMOTION_RBT".equalsIgnoreCase(subscriberClip
									.getContentType()) && !"EMOTION_UGC"
									.equalsIgnoreCase(subscriberClip
											.getContentType())))
						continue;

					// ClipID is same as default clip or clipID same as call
					// back clip ... continue
					if (subscriberClip.getClipId() == clip.getClipId()
							|| subscriberClip.getClipId() == clipID)
						continue;
					else {
						m_rbtDBManager
								.deactivateSubscriberSelections(strSubID,
										Collections.singletonMap(
												"DESELECTED_BY", "SM"),
										Collections.singletonMap(
												"SUBSCRIBER_WAV_FILE",
												subscriberClip
														.getClipRbtWavFile()));
						songWavFileList.add(subStatus.subscriberFile());
					}
				}
			}
		}
	}

	private String smSelectionDectivationSuccess(String strSubID, String refID,
			char oldLoopStatus, int rbtType, Clip clip,String circleIDFromPrism) {
		char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
		if (oldLoopStatus == LOOP_STATUS_EXPIRED)
			newLoopStatus = LOOP_STATUS_EXPIRED;
		if (clip != null
				&& ("EMOTION_RBT".equalsIgnoreCase(clip.getContentType()) || "EMOTION_UGC"
						.equalsIgnoreCase(clip.getContentType()))) {
			String emotionConfig = getParamAsString("COMMON",
					"EMOTION_RBT_DEFAULT_CONFIG");
			if (emotionConfig != null) {
				String categoryType = "19"; // Emotion default content type
				int clipID = 0;

				String[] tokens = emotionConfig.split(",");
				clipID = Integer.parseInt(tokens[0]);
				if (tokens.length >= 4)
					categoryType = tokens[3];

				if (clip.getClipId() == clipID) {
					m_rbtDBManager.deactivateSubscriberSelections(strSubID,
							Collections.singletonMap("DESELECTED_BY", "SM"),
							Collections.singletonMap("CATEGORY_TYPE",
									categoryType));
				}
			}
		}

		return (m_rbtDBManager.smSelectionDeactivationSuccess(strSubID, refID,
				newLoopStatus, rbtType, circleIDFromPrism));
	}

	// Pack Deactivation
	private String smPackDectivationSuccess(String strSubID, String refID) {
		return (m_rbtDBManager.smPackUpdationSuccess(strSubID, refID, "SM",
				PACK_DEACTIVATED + "", false, -1));
	}

	private String smPackRenewalFailure(String strSubID, String refID,
			String extraInfo) {
		HashMap<String, String> packExtraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (packExtraInfoMap == null)
			packExtraInfoMap = new HashMap<String, String>();

		packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
				"SM");
		packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
				new Date().toString());

		extraInfo = DBUtility.getAttributeXMLFromMap(packExtraInfoMap);
		return (m_rbtDBManager.smPackRenewalFailure(strSubID, refID, "SM",
				PACK_DEACTIVATED + "", extraInfo));
	}

	// Pack Activation
	private String smPackActivationSuccess(String strSubID, String refID,
			int numMaxSels) {
		return (m_rbtDBManager.smPackUpdationSuccess(strSubID, refID, "SM",
				PACK_ACTIVATED + "", true, numMaxSels));
	}
     
	//packODAActivation
	private boolean smPackODAActivationSuccess(String strSubID, String refID, int status,
			String extraInfo) {
		return (m_rbtDBManager.updateProvisioningRequestsStatusAndExtraInfo(strSubID, refID,
				status,extraInfo));
	}

	// smUpdateDeactiveSelectionSuccess
	public String smUpdateDeactiveSelectionSuccess(String subscriberID,
			String callerID, String refID, String type, int songStatus,
			String setTime, Date startDate, String classType,
			char newLoopStatus, int fromTime, int toTime, int rbtType,
			String extraInfo) {

		if (getParamAsBoolean("COMMON", "IS_TRAI_REGULATION_ACTIVE", "FALSE")) {
			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap != null && extraInfoMap.containsKey(REFUND)) {
				if (extraInfoMap.containsKey(DEACT_REFID)) {
					String deactRefID = (String) extraInfoMap.get(DEACT_REFID);
					SubscriberStatus[] allSubSel = m_rbtDBManager
							.getAllSubSelectionRecordsForReactivation(
									subscriberID, callerID, null, setTime,
									rbtType, songStatus, fromTime, toTime,
									refID);
					ArrayList<String> replaceList = new ArrayList<String>();

					for (int i = 0; allSubSel != null && i < allSubSel.length; i++) {
						String selExtraInfo = allSubSel[i].extraInfo();
						HashMap<String, String> selExtraInfoMap = DBUtility
								.getAttributeMapFromXML(selExtraInfo);
						if (selExtraInfoMap != null
								&& selExtraInfoMap.containsKey(DEACT_REFID)
								&& selExtraInfoMap.get(DEACT_REFID).equals(
										deactRefID)) {
							replaceList.add(allSubSel[i].refID());
						}
					}
					if (replaceList.size() > 0) {
						String extraInfoQueryStr = getExtraInfoQueryString(
								"replace", refID, deactRefID, false);
						m_rbtDBManager.updateSelectionExtraInfo(subscriberID,
								replaceList, extraInfoQueryStr);
					}
				}
			}
		}

		return m_rbtDBManager.smUpdateDeactiveSelectionSuccess(subscriberID,
				refID, type, setTime, startDate, classType, newLoopStatus,
				rbtType, extraInfo);

	}

	private String smDeactivateRefundedSelection(String strSubID, String refID,
			char oldLoopStatus, String callerID, int rbtType, String extraInfo) {
		char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;

		return (m_rbtDBManager.smDeactivateRefundedSelection(strSubID, refID,
				newLoopStatus, callerID, rbtType, extraInfo));
	}

	private void smReactivatePrevActiveSelection(String subscriberID,
			String callerID, String refID, String setDate, int songStatus,
			int fromTime, int toTime, int rbtType) {

		boolean isSongReset = false;
		String parentRefID = refID;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		SubscriberStatus[] subscriberStatus = null;
		subscriberStatus = m_rbtDBManager
				.getAllSubSelectionRecordsForReactivation(subscriberID,
						callerID, "SM", setDate, rbtType, songStatus, fromTime,
						toTime, refID);

		for (int i = 0; subscriberStatus != null && i < subscriberStatus.length; i++) {
			char loopStatus = LOOP_STATUS_OVERRIDE_INIT;
			String selExtraInfo = subscriberStatus[i].extraInfo();
			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(selExtraInfo);

			if (extraInfoMap != null && extraInfoMap.containsKey(DEACT_REFID)
					&& extraInfoMap.get(DEACT_REFID).equals(parentRefID)) {
				String startTime = getFormattedDate(sdf,
						subscriberStatus[i].startTime());
				logger.info("RBT::DeactRefID is same as RefID of current selection.. StartTime > "
						+ startTime);

				if (startTime != null && !startTime.startsWith("20040101")) {
					logger.info("StartTime not equal to 2004.. RefID > "
							+ subscriberStatus[i].refID());
					if (!isSongReset) {
						if (extraInfoMap.containsKey(REFUNDED)) {
							if (extraInfoMap.containsKey(LOOP)) {
								continue;
							} else {
								logger.info(" > This selection is Refunded changing ParentRefID to >"
										+ subscriberStatus[i].refID());
								parentRefID = subscriberStatus[i].refID();
								continue;
							}
						}
						if (!extraInfoMap.containsKey(REACTIVE))
							extraInfoMap.put(REACTIVE, "TRUE");
						if (extraInfoMap.containsKey(DEACT_REFID))
							extraInfoMap.remove(DEACT_REFID);
						if (extraInfoMap.containsKey(LOOP)) {
							extraInfoMap.remove(LOOP);
							loopStatus = LOOP_STATUS_LOOP_INIT;
							isSongReset = false;
						} else {
							isSongReset = true;
						}
						selExtraInfo = DBUtility
								.getAttributeXMLFromMap(extraInfoMap);
						logger.info("final selExtraInfo1 >" + selExtraInfo);
						m_rbtDBManager.smReactivateSelection(
								subID(subscriberID),
								subscriberStatus[i].refID(),
								subscriberStatus[i].callerID(), loopStatus,
								rbtType, selExtraInfo, null);
					}
				} else if (startTime != null
						&& startTime.startsWith("20040101")) {
					logger.info("StartTime is equal to 2004.. RefID > "
							+ subscriberStatus[i].refID());
					// If selection is in N state and it got Refund call back ,
					// we just update it to REFUND = TRUE, hence we don't need
					// to reactivate this song
					if (extraInfoMap.containsKey(REFUNDED))
						continue;
					// update SelStatus to A . Daemon will pick these selections
					// and sends to subMgr . If its already charged subMgr will
					// not charge again.
					if (extraInfoMap.containsKey(LOOP)) {
						extraInfoMap.remove(LOOP);
						loopStatus = LOOP_STATUS_LOOP_INIT;
					}
					if (extraInfoMap.containsKey(DEACT_REFID))
						extraInfoMap.remove(DEACT_REFID);

					selExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);
					logger.info("final selExtraInfo2 >" + selExtraInfo);
					m_rbtDBManager.smReactivateSelection(subID(subscriberID),
							subscriberStatus[i].refID(),
							subscriberStatus[i].callerID(), loopStatus,
							rbtType, selExtraInfo, null);
				}
			}
		}
	}

	private String smSelectionGrace(String strSubID, String refID, String type,
			int rbtType, char loopStatus, String circleId) {

		return (m_rbtDBManager.smSelectionGrace(strSubID, refID, type, rbtType,
				loopStatus,circleId));
	}

	private String packSelectionGrace(String strSubID, String refID) {

		return (m_rbtDBManager.smPackUpdationSuccess(strSubID, refID, "SM",
				PACK_GRACE + "", false, -1));
	}

	public void deactivateBaseActPendingSelections(String strSubID) {
		m_rbtDBManager.deactivateNewSelections(strSubID, "Daemon", null, null,
				false, null); // callerId, setTime, boolean checkCallerId
	}
	// RBT-14301: Uninor MNP changes.
	public boolean smUpdateSelStatusSubscriptionSuccess(boolean isUpgradeFailure, String strSubID, String circleId) {

		return (smUpdateSelStatusSubscriptionSuccess(strSubID, false, isUpgradeFailure, circleId));
	}

	public boolean smUpdateSelStatusSubscriptionSuccess(String strSubID,
			boolean isPack, boolean isUpgradeFailure, String circleId) {// RBT-14301: Uninor MNP changes.

		return (m_rbtDBManager.smUpdateSelStatusSubscriptionSuccess(strSubID,
				getParamAsBoolean("REAL_TIME_SELECTIONS", "FALSE"), isPack, isUpgradeFailure, circleId));
	}

	// RBT-14301: Uninor MNP changes.
	public boolean smUpdateCircleIdForSubscriber(boolean isSubscriberRec,
			String strSubID, String circleId, String refId, String status) {

		return (m_rbtDBManager.updateCircleIdForSubscriber(isSubscriberRec,
				strSubID, circleId, refId, status));
	}
	
	public boolean smUpdateSelStatusSubscriptionFailure(String strSubID,
			String type,String consentId) {
		return (m_rbtDBManager
				.smDeactivateSelection(strSubID, null, true, type,consentId));
	}
	
	public boolean smUpdateProfileSelStatusOnSubscriptionSuccess(
			String strSubID, boolean isProfilePack) {

		return (m_rbtDBManager.smUpdateSelStatusForPackOnSubscriptionSuccess(
				strSubID, isProfilePack));
	}

	private String smSelectionRenewalSuccess(String strSubID, String refID,
			Date nextChargingDate, String type, String classType,
			String selectionInfo, int rbtType, String loopStatus,String circleId) {
		return (m_rbtDBManager.smSelectionRenewalSuccess(strSubID, refID,
				nextChargingDate, type, classType, selectionInfo, rbtType,
				loopStatus, circleId));
	}

	private String smPackRenewalSuccess(String strSubID, String refID,
			Date nextChargingDate, String classType, int cosID) {
		return (m_rbtDBManager.smPackRenewalSuccess(strSubID, refID,
				nextChargingDate, classType, cosID));
	}

	private String smODAPackRenewalSuccess(String strSubID, String refID,
			Date nextChargingDate, String classType, int categoryID) {
		return (m_rbtDBManager.smODAPackRenewalSuccess(strSubID, refID,
				nextChargingDate, classType, categoryID));
	}

	// If Activation call back is failure remove REFUND=TRUE from extraInfo
	private String smSelectionActivationRenewalFailure(String strSubID,
			String refID, String deactBy, String type, String classType,
			char oldLoopStatus, int rbtType, String extraInfo, Clip clip, String circleId) {
		char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
		if (oldLoopStatus == LOOP_STATUS_EXPIRED)
			newLoopStatus = oldLoopStatus;
		else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT
				|| oldLoopStatus == LOOP_STATUS_LOOP_INIT)
			newLoopStatus = LOOP_STATUS_EXPIRED;

		if (clip != null
				&& ("EMOTION_RBT".equalsIgnoreCase(clip.getContentType()) || "EMOTION_UGC"
						.equalsIgnoreCase(clip.getContentType()))) {
			String emotionConfig = getParamAsString("COMMON",
					"EMOTION_RBT_DEFAULT_CONFIG");
			if (emotionConfig != null) {
				String categoryType = "19"; // Emotion default content type
				int clipID = 0;

				String[] tokens = emotionConfig.split(",");
				clipID = Integer.parseInt(tokens[0]);
				if (tokens.length >= 4)
					categoryType = tokens[3];

				if (clip.getClipId() == clipID) {
					m_rbtDBManager.deactivateSubscriberSelections(strSubID,
							Collections.singletonMap("DESELECTED_BY", "SM"),
							Collections.singletonMap("CATEGORY_TYPE",
									categoryType));
				}
			}
		}

		return (m_rbtDBManager.smSelectionActivationRenewalFailure(strSubID,
				refID, deactBy, type, classType, newLoopStatus, rbtType,
				extraInfo,circleId));
	}

	private String smDeactivateOtherUGSSelections(String strSubID,
			String callerID, String type, int rbtType) {
		return (m_rbtDBManager.smDeactivateOtherUGSSelections(strSubID,
				callerID, type, rbtType));
	}

	private String smSelectionDeactivationFailure(String strSubID,
			String refID, String type, int rbtType, String extraInfo, String circleId) {
		return (m_rbtDBManager.smSelectionDeactivationFailure(strSubID, refID,
				type, rbtType, extraInfo, circleId));
	}

	private String smPackDeactivationFailure(String strSubID, String refID) {
		return (m_rbtDBManager.smPackUpdationSuccess(strSubID, refID, "SM",
				PACK_TO_BE_DEACTIVATED + "", false, -1));
	}

	private String smUnsubscriptonFailure(String strSubID, String type) {
		String ret = null;
		if (!getParamAsBoolean("COMMON", "DEL_SELECTIONS", "TRUE")) {
			ret = m_rbtDBManager.reactivateSubscriber(strSubID);
		} else {
			ret = m_rbtDBManager.smUnsubscriptionFailure(strSubID, type);
		}

		return ret;
	}

	private String smSubscriptionRenewalFailure(String strSubID,
			String deactBy, String type, String classType, boolean isRenewal,
			String extraInfo, boolean updateSM, boolean isToBeDeactiveDirectly, String circleIdFromPrism) {
		return (m_rbtDBManager.smSubscriptionRenewalFailure(strSubID, deactBy,
				type, classType, isRenewal, extraInfo, updateSM, isToBeDeactiveDirectly, circleIdFromPrism));
	}


	private String smSubscriptionRenewalFailureOnlyBaseDeactivation(String strSubID,
			String deactBy, String type, String classType, boolean isRenewal,
			String extraInfo, boolean updateSM, boolean isToBeDeactiveDirectly, String circleIdFromPrism) {
		return (m_rbtDBManager.smSubscriptionRenewalFailureOnlyBaseDeactivation(strSubID, deactBy,
				type, classType, isRenewal, extraInfo, updateSM, isToBeDeactiveDirectly, circleIdFromPrism));
	}


	private String smPackSubscriptionFailure(String strSubID, String cosID,
			int noMaxSelections, String extraInfo) {
		return (m_rbtDBManager.smPackSubscriptionFailure(strSubID, cosID,
				noMaxSelections, extraInfo));
	}

	private String smPackSubscriptionSuccess(String strSubID, String extraInfo) {
		return (m_rbtDBManager.smPackSubscriptionSuccess(strSubID, extraInfo));
	}

	private boolean doesSubscriberHaveNoMoreAlbums(String strSubID) {
		SubscriberStatus[] status = m_rbtDBManager
				.getAllSubscriberSelectionRecords(strSubID, "GUI");

		if (status != null) {
			for (int i = 0; i < status.length; i++) {
				if (status[i].categoryType() == 0)
					return false;
			}
		}

		return true;
	}

	private SubscriberStatus[] smSubscriberRecords(String strSubID, int rbtType) {
		return (m_rbtDBManager.smSubscriberRecords(strSubID, "1000", false,
				rbtType));
	}

	private boolean deactivateSubscriber(String strSubID, String deactivate,
			String type) {
		return (m_rbtDBManager.smDeactivateSubscriber(strSubID, deactivate,
				null, true, true, true, type));
	}

	private String smSubscriptionGrace(String strSubID, String type) {
		return (m_rbtDBManager.smSubscriptionGrace(strSubID, type));
	}

	public boolean isValidSub(String subscriber) {
		return (m_rbtDBManager.isValidPrefix(subscriber));
	}

	private void sendSMS(String subscriberID, String sms, String createTime) {
		try {
			Tools.sendSMS(getSenderNumber(subscriberID), subscriberID, sms,
					getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"),
					createTime);
		} catch (Throwable e) {
			logger.error("", e);
			logger.info(e.getMessage());
		}
	}

	private void sendSelectionSMS(String subscriberID, String sms,
			String createTime, String songName, String callerNo, String start,
			String end, String reason, String movieName) {

		if (sms == null || songName == null || callerNo == null)
			return;

		sms = getFinalSMS(sms, callerNo, songName, start, end, reason,
				movieName);

		try {
			Tools.sendSMS(getSenderNumber(subscriberID), subscriberID, sms,
					getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"),
					createTime);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void sendSelectionSMSCrossPromo(Subscriber subscriber, String sms,
			String createTime, String songName, String callerNo, String start,
			String end, String clipID, String clipPromoId, String result,
			int songStatus, int categoryType, String selectedBy,
			String movieName, boolean isRenewalRequest, String chargeClass) {

		if (sms == null || songName == null || callerNo == null) {
			return;
		}
		sms = getFinalSMS(sms, callerNo, songName, start, end, null, movieName);
		String subscriberID = subscriber.subID();
		String circleId = subscriber.circleID();

		if (getParamAsBoolean("MAKE_HTTP_HIT_BEFORE_SEND_SMS", "FALSE")) {

			logger.info("song status " + songStatus + " category type "
					+ categoryType);

			if (!(songStatus == 90 || songStatus == 99 || songStatus == 0
					|| categoryType == 0 || categoryType == 4
					|| categoryType == 10 || categoryType == 12 || categoryType == 20)) {
				Date requestTimeStamp = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyyMMddHHmmss");
				String requestTimeString = formatter.format(requestTimeStamp);

				String strURL = getParamAsString("GATHERER",
						"CROSS_PROMO_SMS_URL");
				if (strURL != null) {
					strURL = strURL.replaceAll("<msisdn>", subscriberID);
					strURL = strURL.replaceAll("<rbtid>", clipID);
					strURL = strURL.replaceAll("<rbtpromoid>", clipPromoId);
					strURL = strURL.replaceAll("<status>", result);
					strURL = strURL.replaceAll("<channel>", selectedBy);
					try {
						strURL = strURL.replaceAll("<msg>",
								URLEncoder.encode(sms, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error("Exception encoding the msg: " + sms, e);
					}
					if (isRenewalRequest) {
						if (chargeClass != null
								&& chargeClass.indexOf("MONTHLY") != -1) {
							strURL = strURL.replaceAll("<charge_class>",
									"MONTHLY");
						} else if (chargeClass != null
								&& chargeClass.indexOf("WEEKLY") != -1) {
							strURL = strURL.replaceAll("<charge_class>",
									"WEEKLY");
						}
					}
					strURL = strURL.replaceAll("<sendernumber>",
							getSenderNumber(subscriber.subID()));
					strURL = strURL.replaceAll("<circleid>", circleId);
					strURL = strURL.replaceAll(" ", "%20");

				}

				String r = makeHttpRequest(strURL);

				logger.info("CROSS_PROMO_SMS_URL after replacing the parameters: "
						+ strURL + " Response: " + r);

				Date responseTimeStamp = new Date();
				long responseTimeInMillis = responseTimeStamp.getTime()
						- requestTimeStamp.getTime();

				String response = (r != null ? r : "ERROR");
				String successOrFailure = (r != null ? "SUCCESS" : "FAILURE");
				WriteSDR.addToAccounting(
						getParamAsString("GATHERER", "CROSS_PROMO_SMS_LOGPATH"),
						getParamAsInt("GATHERER",
								"CROSS_PROMO_SMS_ROTATIONSIZE", 24),
						"RBT_CROSS_PROMO_SMS_SENDER", subscriberID, null,
						"CROSS PROMO RT sms send", successOrFailure,
						requestTimeString, "" + responseTimeInMillis, null,
						strURL, response);

				if (r == null) {
					if (!getParamAsBoolean("SEND_SMS_CROSS_PROMO", "FALSE"))
						sendSelectionSMS(subscriberID, sms, createTime,
								songName, callerNo, start, end, null, movieName);
				}
			} else {
				List<String> statusList = Arrays.asList(getParamAsString(
						"DAEMON", "CROSS_PROMO_SUPPORTED_STATUSES", "").split(
						","));
				List<String> catTypesList = Arrays.asList(getParamAsString(
						"DAEMON", "CROSS_PROMO_SUPPORTED_CATEGORY_TYPES", "")
						.split(","));

				if (!getParamAsBoolean("SEND_SMS_CROSS_PROMO", "FALSE")
						|| statusList.contains(String.valueOf(songStatus))
						|| catTypesList.contains(String.valueOf(categoryType))) {
					sendSelectionSMS(subscriberID, sms, createTime, songName,
							callerNo, start, end, null, movieName);
				}
			}
		} else {
			if (!getParamAsBoolean("SEND_SMS_CROSS_PROMO", "FALSE"))
				sendSelectionSMS(subscriberID, sms, createTime, songName,
						callerNo, start, end, null, movieName);
		}
	}

	private Clip getClipRBT(String strWavFile, int status) {

		if(strWavFile!=null && strWavFile.indexOf("rbt_slice_")!=-1){
			String str[] = strWavFile.split("rbt_slice_");
			String clipId = str[1].substring(0, str[1].indexOf("_"));
			return m_rbtCacheManager.getClip(clipId);
		}
		Clip clip = m_rbtCacheManager.getClipByRbtWavFileName(strWavFile);
		return clip;
	}

	private Date getNextDate(String chargeperiod) {
		if (chargeperiod == null)
			chargeperiod = "M1";
		int type = 0;
		int number = 0;
		Calendar calendar1 = Calendar.getInstance();
		if (chargeperiod.startsWith("D"))
			type = 0;
		else if (chargeperiod.startsWith("W"))
			type = 1;
		else if (chargeperiod.startsWith("M"))
			type = 2;
		else if (chargeperiod.startsWith("Y"))
			type = 3;
		else if (chargeperiod.startsWith("B"))
			type = 4;
		else if (chargeperiod.startsWith("O"))
			type = 5;

		logger.info("*** getNextDate::type " + type + " for " + chargeperiod);

		if (type != 4 && type != 5) {
			try {
				number = Integer.parseInt(chargeperiod.substring(1));
			} catch (Exception e) {
				type = 2;
				number = 1;
			}
		}

		switch (type) {
		case 0:
			calendar1.add(Calendar.DAY_OF_YEAR, number);
			break;
		case 1:
			calendar1.add(Calendar.WEEK_OF_YEAR, number);
			break;
		case 2:
			calendar1.add(Calendar.MONTH, number);
			break;
		case 3:
			calendar1.add(Calendar.YEAR, number);
			break;
		case 4:
			calendar1.add(Calendar.YEAR, 50);
			break;
		case 5:
			calendar1.add(Calendar.YEAR, 50);
			break;
		default:
			calendar1.add(Calendar.MONTH, 1);
			break;
		}

		calendar1.add(Calendar.DAY_OF_YEAR, -1);
		logger.info("*** getNextDate::type " + calendar1.getTime());
		return calendar1.getTime();
	}

	private void updateDownloads(String extraInfo,
			SubscriberDownloads downloads, String strSubID, String contenttype,
			boolean isLite) {
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (extraInfoMap == null)
			extraInfoMap = new HashMap<String, String>();
		if (isLite)
			extraInfoMap.put(OLD_CLASS_TYPE, downloads.classType());
		else
			extraInfoMap.put(DELAY_DEACT, "TRUE");
		String updatedExtraInfo = DBUtility
				.getAttributeXMLFromMap(extraInfoMap);
		if (contenttype != null)
			m_rbtDBManager.updateDownloads(strSubID, downloads.refID(), 'c',
					updatedExtraInfo, contenttype);
		else
			m_rbtDBManager.updateDownloads(strSubID, downloads.refID(), 'c',
					updatedExtraInfo, null);

	}

	/**
	 * Base upgradation callback
	 * Method to process records for change of Subscription pack on call back
	 * from SM and if SUCCESS OperatorCode3 in RBT_SUBSCRIPTION_CLASS is used as
	 * upgrade SUCCESS SMS
	 * 
	 * @param subscriptionYes
	 * @param strSubID
	 * @param nextChargeDate
	 * @param actDate
	 * @param type
	 * @param classType
	 * @param isPeriodicCharging
	 * @param subClass
	 * @param chargeSms
	 * @param createTime
	 * @param actBy
	 * @param oldSub
	 * @param finalActInfo
	 * @param reason
	 * @param extraInfo
	 * @param upgradingCosID
	 * @return
	 */
	private String processUpgradeSuccess(String subscriptionYes,
			String strSubID, Date nextChargeDate, Date actDate, String type,
			String classType, boolean isPeriodicCharging,
			SubscriptionClass subClass, ChargeSMS chargeSms, String createTime,
			String actBy, String oldSub, String finalActInfo, String reason,
			String extraInfo, String upgradingCosID, String retry, int rbtType, String strNextBillingDate) {
		logger.debug("Processing upgrade success the upgrading cos id = "
				+ upgradingCosID);
		String strStatus = m_FAILURE;
		if (subscriptionYes == null)
			return SUBSCRIPTION_DOES_NOT_EXIST;
		else if (subscriptionYes.equals("B") && !"TRUE".equalsIgnoreCase(retry)) {
			if (oldSub != null && classType != null && oldSub.equals(classType)) {
				return CALLBACK_ALREADY_RECEIVED;
			} else {
				return SUBSCRIPTION_ACTIVE;
			}
		}
		else if (subscriptionYes.equals("A")) // Success case
			return SUBSCRIPTION_ACTIVE;
		else if ((subscriptionYes.equals("N") || subscriptionYes.equals("E"))
				&& oldSub == null)
			return SUBSCRIPTION_ACT_PENDING;
		else if (subscriptionYes.equals("D") || subscriptionYes.equals("P")
				|| subscriptionYes.equals("F")) {
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("SUBSCRIPTION_CLASS", classType);
			attributeMap.put("OLD_CLASS_TYPE", null);

			return m_rbtDBManager.updateSubscriber(strSubID, attributeMap);
		} else if (subscriptionYes.equalsIgnoreCase("X"))
			return SUBSCRIPTION_DEACTIVE;

		boolean updateEndtime = false;
		if ("TNB".equals(actBy) && "ZERO".equals(oldSub))
			updateEndtime = true;
		int validity = -1;
		CosDetails cosDetail = null;
		if (upgradingCosID != null) {
			cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(upgradingCosID);
			if (cosDetail != null) {
				validity = cosDetail.getValidDays();
			}
		}

		String ret = smSubscriptionSuccess(strSubID, nextChargeDate, actDate,
				type, classType, isPeriodicCharging, finalActInfo,
				updateEndtime, true, extraInfo, upgradingCosID, validity, null,
				oldSub, rbtType, strNextBillingDate);

		if (ret.equalsIgnoreCase(m_success)) {
			logger.info("Upgradation success" + cosDetail);
			
			//<RBT-12942>
			//If the subscriber is in renewal grace, the key-value pair indicating the same would be removed from
			//subscriber extraInfo
			
			HashMap<String, String> extraInfoMap1 = DBUtility
					.getAttributeMapFromXML(extraInfo);
			
			if (extraInfoMap1 != null && extraInfoMap1.containsKey(WebServiceConstants.RENEWAL_GRACE)) {
				extraInfoMap1.remove(WebServiceConstants.RENEWAL_GRACE);
				extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap1);
				boolean updateStatus = RBTDBManager.getInstance().updateExtraInfo(strSubID, extraInfo);
				logger.debug("subscriberId: " + strSubID + ". ExtraInfo update status: " + updateStatus);
			}
			// RBT-15143-Implementation of Pack Upgrade From Old Model to 36DT
			// Model
			Map<String, String> confPackUpgradeSubChargeClass = MapUtils
					.convertIntoMap(
							getParamAsString("COMMON",
									"BASE_UPGRADTION_SUBCLASSES_TO_CHARGECLASS_MAPPING"),
							";", "=", null);
			logger.debug("BASE_UPGRADTION_SUBCLASSES_TO_CHARGECLASS_MAPPING: "
					+ confPackUpgradeSubChargeClass);
			boolean isAddToDownloads = getParamAsBoolean("COMMON",
					"ADD_TO_DOWNLOADS", "FALSE");
			String upgradeChargeClass = null;
			if (!isAddToDownloads && confPackUpgradeSubChargeClass != null
					&& !confPackUpgradeSubChargeClass.isEmpty()) {
				for (String key : confPackUpgradeSubChargeClass.keySet()) {
					logger.debug("confPackUpgradeSubChargeClass Key: " + key);
					if (key != null && !key.isEmpty() && classType != null) {
						if (key.equalsIgnoreCase(classType)) {
							upgradeChargeClass = confPackUpgradeSubChargeClass
									.get(key);
							logger.debug("upgradeChargeClass for "
									+ upgradeChargeClass + " Key: " + key);
						}
					}
				}
				if (upgradeChargeClass != null) {
					SubscriberStatus subscriberStatusLst[] = m_rbtDBManager
							.getAllActiveSubSelectionRecords(subID(strSubID));
					if (subscriberStatusLst != null
							&& subscriberStatusLst.length == 1) {
						SubscriberStatus subscriberStatus = subscriberStatusLst[0];
						logger.info("callerID: " + subscriberStatus.callerID()
								+ " status: " + subscriberStatus.status()
								+ " categoryType: "
								+ subscriberStatus.categoryType());
						if (subscriberStatus.callerID() == null
								&& subscriberStatus.status() == 1
								&& !Utility.isShuffleCategory(subscriberStatus
										.categoryType())) {
							if(!isUpgradationWithIn36DT(confPackUpgradeSubChargeClass,subClass,oldSub)){
								String isdeactivated = m_rbtDBManager
										.deactivateSubscriberRecordsByRefId(
												subscriberStatus.subID(), "SM",
												subscriberStatus.refID());
								logger.debug("isdeactivated: " + isdeactivated);
								Subscriber subscriber = m_rbtDBManager.getSubscriber(strSubID);
								String upgradedMode = subscriberStatus.selectedBy();
								if (subscriber != null) {
									upgradedMode = subscriber.activatedBy();
								}
								HashMap<String, String> extraInfoMap = m_rbtDBManager
										.getExtraInfoMap(subscriber);
								if (extraInfoMap != null
										&& extraInfoMap
												.containsKey(AUTO_UPGRADE_MODE)) {
									upgradedMode = (null != extraInfoMap
											.get(AUTO_UPGRADE_MODE)) ? extraInfoMap
											.remove(AUTO_UPGRADE_MODE)
											: upgradedMode;
									String extraInfoStr = DBUtility
											.getAttributeXMLFromMap(extraInfoMap);
									boolean updateStatus = RBTDBManager
											.getInstance().updateExtraInfo(
													strSubID, extraInfoStr);
									logger.debug("subscriberId: " + strSubID
											+ ". ExtraInfo update status: "
											+ updateStatus);
								}
								logger.debug("Updating with auto upgradedMode: "
										+ upgradedMode);
								if (isdeactivated.equalsIgnoreCase(m_success)) {
									HashMap<String, String> extraInfoMapSel = DBUtility
											.getAttributeMapFromXML(subscriberStatus
													.extraInfo());
									String prepaid = "n";
									if (subscriberStatus.prepaidYes())
										prepaid = "y";
									m_rbtDBManager.smCreateSubscriberStatus(
											subscriberStatus.subID(),
											subscriberStatus.callerID(),
											subscriberStatus.categoryID(),
											subscriberStatus.subscriberFile(),
											subscriberStatus.setTime(),
											subscriberStatus.startTime(),
											subscriberStatus.endTime(),
											subscriberStatus.status(),
											upgradedMode,
											subscriberStatus.selectionInfo(),
											subscriberStatus.nextChargingDate(),
											prepaid, upgradeChargeClass, false,
											subscriberStatus.fromTime(),
											subscriberStatus.toTime(), "A", true,
											null, subscriberStatus.categoryType(),
											false, LOOP_STATUS_OVERRIDE_INIT,
											false, 0, subscriberStatus.selType(),
											subscriberStatus.selInterval(),
											null, null,
											subscriberStatus.circleId());
									logger.debug("smCreateSubscriberStatus inserted with charge class: "
											+ upgradeChargeClass);
								}
							}
							
						}
					}
				}
			}
		Map<String, String> confSubChargeClass = MapUtils.
				convertIntoMap(getParamAsString("SMS","BASE_SONG_UPGRADTION_KEYWORD_SUBCLASS_CHARGECLASS_MAPPING"),";",":",null);
			if (confSubChargeClass!=null && !confSubChargeClass.isEmpty()) {
				String upgradeClassType = null;
				if (confSubChargeClass.containsKey(classType)) {
					upgradeClassType = confSubChargeClass.get(classType);
				}				
				if (upgradeClassType != null) {
					ChargeClass chargeClass = m_rbtChargeClassCacheManager
							.getChargeClass(upgradeClassType);
					if (chargeClass != null) {
						SubscriberDownloads[] downloads = m_rbtDBManager
								.getActiveSubscriberDownloads(strSubID);
						for (int i = 0; i < downloads.length; i++) {
							char status = downloads[i].downloadStatus();
							if (downloads[i].classType().equals(
									upgradeClassType)
									|| m_rbtDBManager
											.isDownloadActivationPending(downloads[i])
									|| status == iRBTConstant.STATE_DOWNLOAD_GRACE
									|| status == iRBTConstant.STATE_DOWNLOAD_SUSPENSION) {
								continue;
							}
							HashMap<String, String> extraInfoMap = DBUtility
									.getAttributeMapFromXML(downloads[i]
											.extraInfo());
							if (extraInfoMap == null)
								extraInfoMap = new HashMap<String, String>();
							extraInfoMap.put(OLD_CLASS_TYPE,
									downloads[i].classType());
							String updatedExtraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);
							String wavfile =downloads[i].promoId();
							Clip clip= RBTCacheManager.getInstance().getClipByRbtWavFileName(wavfile);
							String tempUpgradeClassType=upgradeClassType;	               
							if(clip!=null && clip.getClipInfo() != null && clip.getClipInfo().contains("EXPIRYRENEWAL=TRUE")){
								tempUpgradeClassType =  confSubChargeClass.containsKey(classType+"_SONY") ?  confSubChargeClass.get(classType+"_SONY") : upgradeClassType ;     
							}								
							
							m_rbtDBManager.updateDownloads(strSubID,
										downloads[i].refID(), 'c',
										updatedExtraInfo, tempUpgradeClassType);							
						}
					} else {
						logger.info("upgrade class type ( "
								+ upgradeClassType
								+ " ) is not configured in charge class subscriberId: "
								+ strSubID);
					}
				}
			}
			String cosBasedPackMapping = getParamAsString("COMMON",
					"BASE_SONG_UPGRADTION_KEYWORD_COS_CHARGECLASS_MAPPING",
					null);
			logger.info("BASE_SONG_UPGRADTION_KEYWORD_COS_CHARGECLASS_MAPPING cosBasedPackMapping = "
					+ cosBasedPackMapping);
			if (cosBasedPackMapping != null) {
				StringTokenizer stk = new StringTokenizer(cosBasedPackMapping,
						",");
				String toBeUpgradingClassType = null;
				while (stk.hasMoreTokens()) {
					String str = stk.nextToken();
					StringTokenizer st = new StringTokenizer(str, ":");
					if (st.nextToken().equalsIgnoreCase(upgradingCosID))
						toBeUpgradingClassType = st.nextToken();
				}
				logger.info("BASE_SONG_UPGRADTION_KEYWORD_COS_CHARGECLASS_MAPPING toBeUpgradingClassType = "
						+ toBeUpgradingClassType);
				Subscriber subscriber = m_rbtDBManager.getSubscriber(strSubID);
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				boolean isP2PUpgrade = false;
				String premiumChargeClass = null;
				boolean isUDSUser = false;
				if (null != subExtraInfoMap
						&& subExtraInfoMap.get("P2P_UPGRADE") != null
						&& subExtraInfoMap.get("P2P_UPGRADE").equalsIgnoreCase(
								"true")) {
					isP2PUpgrade = true;
				}
				boolean isSameChargeClassUpgradeAllowed = getParamAsBoolean(
						"COMMON", "SAME_CHARGE_CLASS_UPGRADATION_ALLOWED",
						"FALSE");
				ChargeClass chargeClass = m_rbtChargeClassCacheManager
						.getChargeClass(toBeUpgradingClassType);
				SubscriberStatus subStatus[] = m_rbtDBManager
						.getAllActiveSubscriberSettings(strSubID);
				if (subStatus != null && chargeClass != null) {
					if (isP2PUpgrade) {
						for (SubscriberStatus setting : subStatus) {
							if (setting.status() != 90
									&& setting.status() != 99
									&& setting.selType() != 2
									&& !Utility.isShuffleCategory(setting
											.categoryType())) {
								logger.info("Going to update Selection. "
										+ "selection status: "
										+ setting.selStatus()
										+ ", selection classType: "
										+ setting.classType() + ", setting: "
										+ setting);
								String selExtraInfo = setting.extraInfo();
								HashMap<String, String> selectionInfoMap = DBUtility
										.getAttributeMapFromXML(selExtraInfo);
								premiumChargeClass = Utility.isUDSUser(
										subExtraInfoMap, false,
										selectionInfoMap);
								isUDSUser = (premiumChargeClass != null);
								if (null != selectionInfoMap
										&& selectionInfoMap.get("P2P_UPGRADE") != null
										&& selectionInfoMap.get("P2P_UPGRADE")
												.equalsIgnoreCase("true")) {
									Map<String, String> whereClauseMap = new HashMap<String, String>();
									whereClauseMap.put("CALLER_ID",
											setting.callerID());
									if (isUDSUser) {
										List<SubscriberStatus> subscriberStatusList = m_rbtDBManager
												.getSubscriberActiveSelections(
														strSubID,
														whereClauseMap);
										if (subscriberStatusList.size() > 0) {
											subStatus = (SubscriberStatus[]) subscriberStatusList
													.toArray(new SubscriberStatus[0]);
										}
										ArrayList<String> refIdList = new ArrayList<String>();
										refIdList.add(setting.refID());
										selectionInfoMap.remove("P2P_UPGRADE");
										String extraInfoQueryStr = DBUtility
												.getAttributeXMLFromMap(selectionInfoMap);
										m_rbtDBManager
												.updateSelectionExtraInfo(
														setting.subID(),
														refIdList,
														extraInfoQueryStr);
									} else {
										if (setting.subscriberFile() != null
												&& setting.subscriberFile() != null) {
											whereClauseMap.put(
													"SUBSCRIBER_WAV_FILE",
													setting.subscriberFile());
										}
										whereClauseMap.put("CATEGORY_ID",
												setting.categoryID() + "");
										subStatus[0] = m_rbtDBManager
												.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
														strSubID,
														whereClauseMap);
										ArrayList<String> refIdList = new ArrayList<String>();
										refIdList.add(setting.refID());
										selectionInfoMap.remove("P2P_UPGRADE");
										String extraInfoQueryStr = DBUtility
												.getAttributeXMLFromMap(selectionInfoMap);
										m_rbtDBManager
												.updateSelectionExtraInfo(
														setting.subID(),
														refIdList,
														extraInfoQueryStr);
									}
									m_rbtDBManager
									.updateExtraInfo(
											strSubID,
											DBUtility
											.getAttributeXMLFromMap(subExtraInfoMap));
								}
							}
						}
					}
					for (SubscriberStatus setting : subStatus) {
						if (setting.status() != 90
								&& setting.status() != 99
								&& setting.selType() != 2
								&& !Utility.isShuffleCategory(setting
										.categoryType())) {
							logger.info("Going to update Selection. "
									+ "selection status: "
									+ setting.selStatus()
									+ ", selection classType: "
									+ setting.classType() + ", setting: "
									+ setting);
							if (!isSameChargeClassUpgradeAllowed
									&& setting.classType().equalsIgnoreCase(
											chargeClass.getChargeClass())) {
								logger.debug("Not updating as the charge class is same as that of configured and the config i false to allow");
								continue;
							}

							String selChargeClass = chargeClass
									.getChargeClass();
							String selStatus = setting.selStatus();
							// If sel_status is A update only class type.
							// In case of TO_BE_ACTIVATED update only the charge
							// class.
							// In case of ACTIVATION_PENDING, update the charge
							// class in selection extra info.
							if (selStatus.equals("A")) {
								m_rbtDBManager.upgradeSelectionClassType2(
										strSubID, setting.classType(),
										chargeClass.getChargeClass(), rbtType,
										setting.refID(), actBy);
								logger.info("Updated selection classType: "
										+ chargeClass.getChargeClass()
										+ " strSubID: " + strSubID
										+ ", setting refID: " + setting.refID());
							} else if (selStatus.equals("N")) {
								// Add update extra info with update extrainfo
								// CLASS_TYPE_UPGRADE="chargeClass.getChargeClass()"
								String selExtraInfo = setting.extraInfo();
								HashMap<String, String> map = DBUtility
										.getAttributeMapFromXML(selExtraInfo);
								if (null == map) {
									map = new HashMap<String, String>();
								}
								map.put("CLASS_TYPE_UPGRADE", selChargeClass);
								String extraInfoQueryStr = DBUtility
										.getAttributeXMLFromMap(map);
								ArrayList<String> refIdList = new ArrayList<String>();
								refIdList.add(setting.refID());
								m_rbtDBManager.updateSelectionExtraInfo(
										setting.subID(), refIdList,
										extraInfoQueryStr);
								logger.info("Updated selection extra info for selection status N."
										+ " extraInfo: " + extraInfoQueryStr);
							} else {
								m_rbtDBManager.upgradeSelectionClassType(
										strSubID, setting.classType(),
										chargeClass.getChargeClass(), rbtType,
										setting.refID(), actBy);
								logger.info("Updated selection status: "
										+ setting.selStatus());
							}

						}
					}
				}
			}

			if (cosDetail != null && contentTypeChargeClassMap.size() > 0) {
				boolean isLiteContent = false;
				validity = cosDetail.getValidDays();
				String contentType = cosDetail.getContentTypes();
				if (contentType != null) {
					List<String> userContentTypeList = Arrays
							.asList(contentType.split(","));// null
					logger.debug("The content type of the cos is "
							+ contentType
							+ " and the content charge class map = "
							+ contentTypeChargeClassMap);

					for (int k = 0; k < userContentTypeList.size(); k++) {
						if (contentTypeChargeClassMap
								.containsKey(userContentTypeList.get(k)))
							isLiteContent = true;
					}
					if (isLiteContent) {
						SubscriberDownloads[] downloads = m_rbtDBManager
								.getActiveSubscriberDownloads(strSubID);
						logger.debug("The no of downloads for the user are "
								+ downloads.length);
						for (int i = 0; i < downloads.length; i++) {
							char status = downloads[i].downloadStatus();
							if (m_rbtDBManager
									.isDownloadActivationPending(downloads[i])
									|| status == iRBTConstant.STATE_DOWNLOAD_GRACE
									|| status == iRBTConstant.STATE_DOWNLOAD_SUSPENSION) {
								m_rbtDBManager.expireSubscriberDownload(
										strSubID, downloads[i].refID(), "LITE");
								continue;
							}
							Clip clip = m_rbtCacheManager
									.getClipByRbtWavFileName(downloads[i]
											.promoId());
							logger.info("Clip corresponding to the download is "
									+ clip);
							if (clip != null) {
								String contenttype = contentTypeChargeClassMap
										.get(clip.getContentType());
								if (contenttype != null) {
									updateDownloads(extraInfo, downloads[i],
											strSubID, contenttype, true);
								} else {
									updateDownloads(extraInfo, downloads[i],
											strSubID, null, false);

								}
							}
						}
					}
				}
			}

			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			SRBTUtility.updateSocialSubscriberForSuccess(m_socialRBTAllowed,
					m_socialRBTAllUpdateInOneModel, extraInfoMap, strSubID,
					rbtSystemType, classType, actBy, evtType);
			smUpdateSelStatusSubscriptionSuccess(false, strSubID, null);// RBT-14301: Uninor MNP changes.

			if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
					&& subClass != null && subClass.getOperatorCode3() != null)
				sendSMS(strSubID,
						getReasonSMS(subClass.getOperatorCode3(), reason),
						createTime);

			// Add a selection configured for subscriptionClass in OperatorCode2
			// field
			addSelectionBasedOnSubClassOpCode(strSubID, subClass, actBy);

			return m_SUCCESS;
		} else if (ret.equalsIgnoreCase(m_failure))
			strStatus = m_INVALID;
		return strStatus;
	}

	/**
	 * Method checks if the upgradation is happening within 36DT Charge class or not.
	 * @param confPackUpgradeSubChargeClass
	 * @param New subscription class
	 * @param Old subscription class
	 * @return true if old and new subscription class is configured in 36DT subscription class list
	 */
	private boolean isUpgradationWithIn36DT(
			Map<String, String> confPackUpgradeSubChargeClass,
			SubscriptionClass subClass, String oldSub) {
		if(null != subClass.getSubscriptionClass() && confPackUpgradeSubChargeClass.containsKey(subClass.getSubscriptionClass())){
			if( confPackUpgradeSubChargeClass.containsKey(oldSub) ){
				logger.info("Old and New subscription class belongs to 36DT subscription Class!!");
				return true;
			}
		}
		
		return false;
	}

	/**
	 * This function updates the RBT_SUBSCRIBER table if call back for
	 * Upgradation of service is failure OperatorCode4 in RBT_SUBSCRIPTION_CLASS
	 * is used as upgrade failed SMS
	 * 
	 * @param subscriptionYes
	 * @return status
	 */
	private String processUpgradeFailure(String subscriptionYes,
			String strSubID, SubscriptionClass subClass, String createTime,
			Subscriber sub, String reason, String circleIDFromPrism) {
		String strStatus = m_FAILURE;

		String actBy = null;
		String oldSubClass = null;
		String extraInfo = null;
		String newStatus = "B";
		int rbtType = 0;
		String newSubClass = sub.subscriptionClass();

		if (sub != null) {
			actBy = sub.activatedBy();
			oldSubClass = sub.oldClassType();
			extraInfo = sub.extraInfo();
			rbtType = sub.rbtType();
		}

		if (subscriptionYes == null)
			return SUBSCRIPTION_DOES_NOT_EXIST;
		else if (subscriptionYes.equals("B"))
			return CALLBACK_ALREADY_RECEIVED;
		else if (subscriptionYes.equals("A"))
			return SUBSCRIPTION_ACTIVE;
		else if ((subscriptionYes.equals("N") || subscriptionYes.equals("E"))
				&& oldSubClass == null)
			return SUBSCRIPTION_ACT_PENDING;
		else if (subscriptionYes.equals("D") || subscriptionYes.equals("P")
				|| subscriptionYes.equals("F")) {
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("SUBSCRIPTION_CLASS", oldSubClass);
			attributeMap.put("OLD_CLASS_TYPE", null);

			return m_rbtDBManager.updateSubscriber(strSubID, attributeMap);
		} else if (subscriptionYes.equalsIgnoreCase("X"))
			return SUBSCRIPTION_DEACTIVE;

		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		String oldActBy = null;
		if (extraInfoMap != null) {
			if (extraInfoMap.containsKey("IS_SUSPENDED")) {
				newStatus = "Z";
				extraInfoMap.remove("IS_SUSPENDED");
			}

			if (extraInfoMap.containsKey(WebServiceConstants.param_old_offerid)) {

				/*
				 * Changed by Senthilraja from upgrade retry callback.
				 */
				if (extraInfoMap.containsKey(WebServiceConstants.param_offerID)) {
					extraInfoMap
							.put(WebServiceConstants.param_upgradeFailuer_OfferId,
									extraInfoMap
											.get(WebServiceConstants.param_offerID));
				}

				extraInfoMap
						.put(WebServiceConstants.param_offerID, extraInfoMap
								.get(WebServiceConstants.param_old_offerid));
			}

			extraInfoMap.put(EXTRA_INFO_UPGRADE_FAILURE_RBTTYPE, rbtType + "");

			if (extraInfoMap.containsKey(EXTRA_INFO_ADRBT_ACTIVATION)) // AdRbt
																		// upgradation
																		// is
																		// failed.
																		// setting
																		// rbtType
																		// back
																		// to
																		// RBT
			{
				rbtType = 0;
				extraInfoMap.remove(EXTRA_INFO_ADRBT_ACTIVATION);
			}
			else if (extraInfoMap.containsKey(EXTRA_INFO_ADRBT_DEACTIVATION))
				rbtType = 1;

			/*
			 * Changed by Senthilraja from upgrade retry callback.
			 */
			String cosId = extraInfoMap.remove(EXTRA_INFO_COS_ID);
			if (cosId != null)
				extraInfoMap.put(EXTRA_INFO_RETRY_COS_ID, cosId);

			if (sub != null) {
				extraInfoMap.put(EXTRA_INFO_UPGRADE_FAILURE_SUB_CLASS,
						sub.subscriptionClass());
				extraInfoMap.put(EXTRA_INFO_UPGRADE_FAILURE_OLD_SUB_CLASS,
						sub.oldClassType());
			}
			if (extraInfoMap.containsKey(EXTRA_INFO_OLD_ACT_BY)) {
				oldActBy = extraInfoMap.get(EXTRA_INFO_OLD_ACT_BY);
				extraInfoMap.remove(EXTRA_INFO_OLD_ACT_BY);
			}
			if (freemiumSubClassList.contains(oldSubClass)) {
				extraInfoMap.remove(EXTRA_INFO_OFFER_ID);
			}
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			if (extraInfo == null)
				extraInfo = "NULL";
		}
		String ConsentTransId = null;
		UserSelectionRestrictionBasedOnSubClass userRestriction = null;
		try {
			userRestriction = (UserSelectionRestrictionBasedOnSubClass) ConfigUtil
					.getBean(BeanConstant.AIRTEL_USER_SELECTION_RESTRICT_BASED_ON_SUBCLASS);
		} catch (Exception e) {
			logger.error("Exception Occurred while initialising "
					+ BeanConstant.AIRTEL_USER_SELECTION_RESTRICT_BASED_ON_SUBCLASS
					+ " " + e.toString());
		}
		//boolean beanActive = userRestriction != null ? true : false;
		String ret = m_rbtDBManager.updateUpgradeFailure(strSubID, actBy,
				oldSubClass, rbtType, true, newStatus, extraInfo, oldActBy,
				userRestriction);

		if (getParamAsBoolean("TRIAL_CHANGE_SUB_ON_SEL", "FALSE")
				&& sub != null && actBy.equals("TNB")
				&& (oldSubClass != null && oldSubClass.equals("ZERO")))
			m_rbtDBManager.deactivateNewSelections(strSubID, "Daemon", null,
					null, false, null);
		if (ret.equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			
 			
			if (sub != null) {
				String subID = sub.subID();
				SubscriberStatus[] subscriberSelections = m_rbtDBManager.getAllSubscriberSelectionRecordsBasedOnSelStatus(subID, STATE_BASE_ACTIVATION_PENDING);
				if(subscriberSelections != null && subscriberSelections.length >0 )
				{
				for(SubscriberStatus selections : subscriberSelections){
					HashMap<String, String> extraInfoMapSel = DBUtility
							.getAttributeMapFromXML(selections.extraInfo());
					if(extraInfoMapSel != null && extraInfoMapSel.containsKey("IS_SDP_COMBO_SELECTION")){
					String deactivationResponse = 	m_rbtDBManager.deactivateSubscriberRecordsByRefId(sub.subID(), "Daemon", selections.refID(), null, null, null,STATE_DEACTIVATED);
					logger.info("Deactivating reaponse for selections for SDP combo request for subscriber : " + subID + "refId :" + selections.refID() + "response : " 
							+deactivationResponse) ;
					}
				}
				}
				String circleId = sub.circleID();
				if (circleId != null && circleIDFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true, sub.subID(),
							circleIDFromPrism, sub.refID(), null);
				}
			}
			if (!freemiumSubClassList.contains(oldSubClass)) {
				String subscriptionClass = subClass.getSubscriptionClass();
				if(subscriptionClass != null){
				processXBIUpgradeFailure(strSubID, subscriptionClass); 
				}
				smUpdateSelStatusSubscriptionSuccess(true, strSubID, null);// RBT-14301:
																				// Uninor
																			// MNP
																			// changes.
			}
			String consentId= null; 
			if (extraInfoMap.containsKey(EXTRA_INFO_UPGRADE_CONSENT)) {
				if (extraInfoMap.containsKey(EXTRA_INFO_UPGRADE_CONSENT)) {
					ConsentTransId = extraInfoMap.remove(EXTRA_INFO_TRANS_ID);
					SubscriberStatus[] results = RBTDBManager.getInstance()
							.getAllSubscriberSelectionRecords(strSubID, null);
					if(null!=results && results.length > 0){
						for(SubscriberStatus selection:results){
							if(null!=selection.extraInfo()){
								HashMap<String, String> selExtraInfoMap = DBUtility
									.getAttributeMapFromXML(selection.extraInfo());
								String transIdInSelection = selExtraInfoMap.get(EXTRA_INFO_TRANS_ID);
								if (selExtraInfoMap
										.containsKey(EXTRA_INFO_UPGRADE_CONSENT)
										&& transIdInSelection
												.equalsIgnoreCase(ConsentTransId)) {
									consentId = selection.refID();
									String prepaid = "n";
									if (sub.prepaidYes())
										prepaid = "y";
									if (!smUpdateSelStatusSubscriptionFailure(
											strSubID, prepaid, consentId))
										logger.info("Received upgradation Subscription failure Callback. deactivate selection failed");
									break;
								}
							}
						}
					}
				}
			}
			
			if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
					&& subClass != null)
				sendSMS(strSubID,
						getReasonSMS(subClass.getOperatorCode4(), reason),
						createTime);
			return m_SUCCESS;
		} else if (ret.equalsIgnoreCase(m_failure))
			strStatus = m_INVALID;
		return strStatus;
	}

	private void processXBIUpgradeFailure(String strSubID,
			String subClass) {
		String xbiChargeSubClassMap = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(SMS,
						XBI_CHARGE_SUB_CLASS_MAPPING, null);
		if (subClass != null && xbiChargeSubClassMap != null
				&& xbiChargeSubClassMap.trim().length() != 0) {
			logger.info("inside xbi rejection block");
			HashMap<String, String> catIdRentalPack = (HashMap<String, String>) MapUtils
					.convertIntoMap(xbiChargeSubClassMap, ";", ":",
							null);
			Boolean isSubClassMapped = false;
			String mappedChargeClass = null;
			for (String sClass : catIdRentalPack.keySet()) {
				if (null != catIdRentalPack.get(sClass)
						&& catIdRentalPack.get(sClass).equalsIgnoreCase(
								subClass)) {
					mappedChargeClass = sClass;
					isSubClassMapped = true;
				}
			}
			if (isSubClassMapped) {
				logger.info("inside xbi  block");
				smUpdateSelStatusXbiSubscription(strSubID,
						mappedChargeClass, null);
			}
		}
	}

	/*
	 * If suspension is userInitiated (Identified by reasonCode 710) , add
	 * VOLUNTARY=TRUE in extraInfo column
	 */
	private String processSuspendSubscription(Subscriber subscriber,
			String createTime, String reason, String reasonCode,
			String extraInfo, String oldSubClass, String circleIdFromPrism) {
		String strStatus = m_FAILURE;

		if (subscriber != null) {
			if (subscriber.subYes() == null)
				return SUBSCRIPTION_DOES_NOT_EXIST;
			if (subscriber.subYes().equalsIgnoreCase("Z")
					&& getParamAsBoolean("ACCEPT_MULTIPLE_SUSPENSION_REQUEST",
							"FALSE"))
				return m_SUCCESS;
			else if (subscriber.subYes().equalsIgnoreCase("Z")
					&& !getParamAsBoolean(iRBTConstant.COMMON,
							"CHARGING_FOR_VOLUNTARY_SUSPENSION_ALLOWED",
							"FALSE"))
				return SUBSCRIPTION_ALREADY_SUSPENDED;
			else if (subscriber.subYes().equals("D")
					|| subscriber.subYes().equals("P")
					|| subscriber.subYes().equals("F"))
				return CALLBACK_ALREADY_RECEIVED;
			else if (subscriber.subYes().equalsIgnoreCase("x"))
				return SUBSCRIPTION_DEACTIVE;
			// else if (subscriber.subYes().equalsIgnoreCase("G"))
			// return SUBSCRIPTION_ACT_GRACE;
			else if (!getParamAsBoolean("DAEMON",
					"SUSPEND_BASE_FOR_LOW_AMOUNT", "FALSE")) {
				if (subscriber.subYes().equalsIgnoreCase("E")
						&& oldSubClass == null)
					return SUBSCRIPTION_ACT_PENDING;
			}

			boolean updateActivationDateForSuspendedSub = false;
			//<RBT-12942>
			//If the subscriber is in renewal grace, the key-value pair indicating the same would be removed from
			//subscriber extraInfo
			HashMap<String, String> extraInfoMap1 = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap1 != null && extraInfoMap1.containsKey(WebServiceConstants.RENEWAL_GRACE)) {
				extraInfoMap1.remove(WebServiceConstants.RENEWAL_GRACE);
				extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap1);
			}
			if (reasonCode != null && reasonCode.contains("710")) // User
																	// Initiated
																	// suspension
			{
				extraInfo = DBUtility.setXMLAttribute(extraInfo, VOLUNTARY,
						"TRUE");
			} else if (getParamAsBoolean(iRBTConstant.COMMON,
					"CHARGING_FOR_VOLUNTARY_SUSPENSION_ALLOWED", "FALSE")) {
				HashMap<String, String> extraInfoMap = m_rbtDBManager
						.getExtraInfoMap(subscriber);
				updateActivationDateForSuspendedSub = true;
				if (extraInfoMap != null && extraInfoMap.get(VOLUNTARY) != null
						&& extraInfoMap.get(VOLUNTARY).equalsIgnoreCase("TRUE")) {
					extraInfoMap.put(VOLUNTARY, "SM_SUSPENDED");
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
				} else if (extraInfoMap != null
						&& extraInfoMap.get(VOLUNTARY) != null
						&& extraInfoMap.get(VOLUNTARY).equalsIgnoreCase(
								"SM_SUSPENDED")) {
					return SUBSCRIPTION_ALREADY_SUSPENDED;
				} else if (subscriber.subYes().equalsIgnoreCase("Z")) {
					return "ALREADY_SUSPENDED";
				}
			}

			boolean updateActivationDate = false;
			if ((subscriber.subYes().equalsIgnoreCase("N")
					|| subscriber.subYes().equalsIgnoreCase("E")
					|| subscriber.subYes().equalsIgnoreCase("A") || updateActivationDateForSuspendedSub)
					&& getParamAsBoolean("DAEMON",
							"SUSPEND_BASE_FOR_LOW_AMOUNT", "FALSE"))
				updateActivationDate = true;
			//RBT-18483 & RBT-18121 Removed reactivating while suspended
			//v commented
			/*String dtocSuspendedFreeUserClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_APP_SUSPENDED_FREE_USER_SERVICE_CLASS", null);
			String subscriptionClass = subscriber.subscriptionClass();
			*/
		/*	String operatorUserType = null;
			IUserDetailsService operatorUserDetailsService = null;
			OperatorUserDetails operatorUserDetails = null;

			try {
				// Getting user details from B2B db cache
				operatorUserDetailsService = (IUserDetailsService) ConfigUtil.getBean(BeanConstant.USER_DETAIL_BEAN);


				if (operatorUserDetailsService != null) {
					operatorUserDetails = (OperatorUserDetailsImpl) operatorUserDetailsService
							.getUserDetails(subscriber.subID());
				}

				if (operatorUserDetails != null) {
					operatorUserType = operatorUserDetails.serviceKey();
				}
			} catch (Exception e) {
				operatorUserType = com.onmobile.apps.ringbacktones.v2.common.Constants.NON_LEGACY_OPT;
				logger.error("Exception getting operatorUserType so setting operator type as :: " + operatorUserType);
			}*/
  /*
			boolean allowUpgradation = false;
			if (dtocSuspendedFreeUserClass != null && operatorUserType != null
					&& operatorUserType.equalsIgnoreCase(OperatorUserTypes.PAID_APP_USER.getDefaultValue())) {
						allowUpgradation = true;
			}*/
			String ret = m_failure;
			
			
		/*	if (allowUpgradation) {
				Map<String, String> attributeMap = new HashMap<String, String>();
				extraInfo = DBUtility.setXMLAttribute(extraInfo, com.onmobile.apps.ringbacktones.v2.common.Constants.PAID_UNDER_LOWBAL, "TRUE");
				attributeMap.put("SUBSCRIPTION_CLASS", dtocSuspendedFreeUserClass);
				attributeMap.put("EXTRA_INFO", extraInfo);
				
				attributeMap.put("PLAYER_STATUS", "A");
				ret = m_rbtDBManager.updateSubscriber(subscriber.subID(), attributeMap);
			} else {
				
			}*/
			ret = m_rbtDBManager.processSuspendSubscription(subscriber.subID(), extraInfo, updateActivationDate);
			if (ret.equalsIgnoreCase(m_success)) {
				// RBT-14301: Uninor MNP changes.
				if (subscriber != null) {
					String circleId = subscriber.circleID();
					if (circleId != null && circleIdFromPrism != null
							&& !circleId.equalsIgnoreCase(circleIdFromPrism)) {
						smUpdateCircleIdForSubscriber(true,
								subscriber.subID(), circleIdFromPrism,
								subscriber.refID(), null);
					}
				}
				String suspendSubSms = getParamAsString("SUSPEND_SMS");
				if (getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE")
						&& suspendSubSms != null && suspendSubSms.length() > 0) {
					String smsMessage = suspendSubSms;
					if (reasonCode != null && reasonCode.contains("710"))
						smsMessage = getParamAsString("VOLUNTARY_SUSPEND_SMS");

					if (createTime == null)
						createTime = getCreateTime();
					sendSMS(subscriber.subID(),
							getReasonSMS(smsMessage, reason), createTime);
				}
				return m_SUCCESS;
			} else if (ret.equalsIgnoreCase(m_failure))
				strStatus = m_INVALID;
		}
		return strStatus;
	}

	// Added for Idea voluntary suspension
	private String processResumeSubscription(Subscriber subscriber,
			String reasonCode, String currentSubStatus, String extraInfo,
			Date ncd,String circleIDFromPrism,String type, String classType,String finalActInfo) {
		String strStatus = m_FAILURE;
		String ret = null;
		if (!subscriber.subYes().equalsIgnoreCase("z")
				&& !subscriber.subYes().equalsIgnoreCase("Z"))
			return m_INVALID + "|SUBSCRIBER IS NOT SUSPENDED";

		String voluntaryValue = DBUtility.getAttributeMapFromXML(extraInfo) != null ? DBUtility
				.getAttributeMapFromXML(extraInfo).get(VOLUNTARY) : null;
		if (voluntaryValue == null || voluntaryValue.equalsIgnoreCase("SM_SUSPENDED")) {
			ret = smRenewalSuccess(subscriber.subID(), ncd, type, classType,
					finalActInfo, extraInfo,null,false);
		} else  {
			if ((reasonCode != null && reasonCode.contains("710"))
					&& (extraInfo == null || !extraInfo.contains(VOLUNTARY)))
				return m_INVALID + "|SUBSCRIBER IS NOT VOLUNTARILY SUSPENDED";
			if ((reasonCode == null || !reasonCode.equalsIgnoreCase("710"))
					&& (extraInfo != null && extraInfo.contains(VOLUNTARY))) {
				return m_INVALID + "|SUBSCRIBER IS VOLUNTARILY SUSPENDED";
			}

			logger.info(" reasonCode  >" + reasonCode + " & extraInfo >"
					+ extraInfo);
			if (reasonCode != null && reasonCode.contains("710")) // User
																	// Initiated
																	// suspension
			{
				extraInfo = DBUtility.removeXMLAttribute(extraInfo, VOLUNTARY);
			}
			ret = m_rbtDBManager.processResumeSubscription(
					subscriber.subID(),
					getSubscriptionStatus(currentSubStatus), extraInfo, ncd);
		}

		if (ret.equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleId != null && circleIDFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIDFromPrism)) {
					smUpdateCircleIdForSubscriber(true, subscriber.subID(),
							circleIDFromPrism, subscriber.refID(), null);
				}
			}
			String resumeSubscriptionSMS = getParamAsString("VOLUNTARY_RESUME_SMS");
			if (resumeSubscriptionSMS != null
					&& resumeSubscriptionSMS.length() > 0) {
				String createTime = getCreateTime();
				sendSMS(subscriber.subID(), resumeSubscriptionSMS, createTime);
			}
			return m_SUCCESS;
		}

		return strStatus;
	}

	private boolean convertSubscriptionType(String sub, String init,
			String last, Subscriber subscriber) {
		return (m_rbtDBManager.convertSubscriptionType(sub, init, last,
				subscriber));
	}

	/**
	 * @author Sreekar
	 * @added for Virgin to send deactivation SMS
	 */
	private String getSubClassSMSText(String classType, boolean isActivation,
			String subscriberLanguage) {
		String ret = null;
		// SubscriptionClass subClass =
		// m_rbtSubClassCacheManager.getSubscriptionClass(classType);
		SubscriptionClass subClass = m_rbtSubClassCacheManager
				.getSubscriptionClassByLanguage(classType, subscriberLanguage);
		if (subClass != null) {
			if (isActivation)
				ret = subClass.getSmsOnSubscription();
			else
				ret = subClass.getSmsDeactivationSuccess();
		}
		return ret;
	}

	public String subID(String strSubID) {
		return (m_rbtDBManager.subID(strSubID));
	}

	private String smSelectionSuspend(String strSubID, String refID,
			char loopStatus, int rbtType,String circleId) {
		char newLoopStatus = LOOP_STATUS_OVERRIDE;
		if (loopStatus == LOOP_STATUS_LOOP_FINAL)
			newLoopStatus = LOOP_STATUS_LOOP;

		return (m_rbtDBManager.smSelectionSuspend(strSubID, refID,
				newLoopStatus, rbtType, circleId));
	}

	private String packSelectionSuspend(String strSubID, String refID) {
		return (m_rbtDBManager.smPackUpdationSuccess(strSubID, refID, "SM",
				PACK_SUSPENDED + "", false, -1));
	}

	private boolean isSelectionInActCategory(String selStatus) {
		if (selStatus.equals(STATE_ACTIVATED)
				|| selStatus.equals(STATE_TO_BE_ACTIVATED)
				|| selStatus.equals(STATE_ACTIVATION_PENDING)
				|| selStatus.equals(STATE_ACTIVATION_ERROR)
				|| selStatus.equals(STATE_BASE_ACTIVATION_PENDING)
				|| selStatus.equals(STATE_REQUEST_RENEWAL)
				|| selStatus.equals(STATE_ACTIVATION_GRACE)
				|| selStatus.equals(STATE_SUSPENDED)
				|| selStatus.equals(STATE_CHANGE))
			return true;
		else
			return false;
	}

	private String getFinalSMS(String sms, String callerNo, String songName,
			String start, String end, String reason, String albumName) {
		// Three conditions are handled here
		// 1)if %S%A we will append a hyphen S - A will be replaced
		// 2)if %A comes in the end
		// 3)if %A comes in the middle (one space will be removed)

		if (albumName.equals("N/A")) {
			if (sms.contains("%S%A")) {
				sms = sms.replaceAll("%A", "");
			}

			if (sms.contains(" %A ")) {
				sms = sms.replaceAll("%A ", "");
			}

			if (sms.contains("%A")) {
				sms = sms.replaceAll("%A", "");
			}
		}

		if (sms.contains("%S%A"))
			sms = sms.replaceAll("%A", " (" + albumName + ")");

		sms = sms.replaceAll("%S", songName);
		sms = sms.replaceAll("%A", albumName);
		sms = sms.replaceAll("%C", callerNo);

		if (start != null && end != null) {
			sms = getParamAsString("DAEMON", "OVERRIDE_SHUFFLE_SMS",
					m_overrideShuffleSMS);
			sms = sms.replaceAll("%S", songName);
			sms = sms.replaceAll("%A", albumName);
			sms = sms.replaceAll("%C", callerNo);
			sms = sms.replaceAll("%L", start);
			sms = sms.replaceAll("%T", end);
		}

		if (start == null && end != null) {
			sms = sms.replaceAll("%S", songName);
			sms = sms.replaceAll("%A", albumName);
			sms = sms.replaceAll("%C", callerNo);
			sms = sms.replaceAll("%T", end);
		}

		return getReasonSMS(sms, reason);
	}

	private String getReasonSMS(String sms, String reason) {
		if (sms == null) {
			return sms;
		}

		if (reason == null) {
			sms = sms.replace(" %R", "");
			return sms;
		}
		sms = sms.replaceAll("%R", reason);

		return sms.trim();
	}

	private synchronized String getFormattedDate(SimpleDateFormat sdf, Date d) {
		if (sdf != null)
			return sdf.format(d);

		return null;
	}

	private void getUGCCreditTable() {
		Parameters p = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "UGC_CREDIT_AMT_MAP");
		String ugcCreditMap = null;
		if (p != null && p.getValue() != null)
			ugcCreditMap = p.getValue().trim();
		if (ugcCreditMap != null && ugcCreditMap.length() > 0) {
			StringTokenizer stUgc = new StringTokenizer(ugcCreditMap, ",");
			String chargeAmt = null;
			String creditAmt = null;
			while (stUgc.hasMoreTokens()) {
				chargeAmt = stUgc.nextToken().trim();
				if (stUgc.hasMoreTokens())
					creditAmt = stUgc.nextToken().trim();
				if (chargeAmt != null && creditAmt != null)
					m_ugcCreditMap.put(chargeAmt, creditAmt);
				chargeAmt = null;
				creditAmt = null;
			}
		}
		logger.info("m_ugcCreditMap is " + m_ugcCreditMap);
	}

	private String makeHttpRequest(String url) {
		logger.info("Making Http call. Url: " + url);
		String response = null;
		HostConfiguration hcfg = new HostConfiguration();
		PostMethod postMethod = null;
		try {
			HttpURL httpURL = new HttpURL(url);
			hcfg.setHost(httpURL);
			postMethod = new PostMethod(url);

			int statusCode = m_httpClient.executeMethod(hcfg, postMethod);
			response = postMethod.getResponseBodyAsString();
			response = (response != null ? response.trim() : response);
			logger.info("RBT:: HTTP status code: " + statusCode + ", response: "
					+ response + ", Url: " + url);
		} catch (Throwable t) {
			if (t instanceof SocketTimeoutException) {
				String temp = getParamAsString("DAEMON",
						"CROSS_PROMO_URL_TIMEDOUT_RESPONSE", null);
				// response = "READ TIMED OUT";
				response = temp;
			}
			logger.error("Exception hitting the URL: ", t);
		} finally {
			if (postMethod != null)
				postMethod.releaseConnection();
		}
		return response;
	}

	// Added for TRAI changes
	private String getExtraInfoQueryString(String info, String refID,
			String replaceRefID, boolean isSelInLoop) {
		String queryStr = null;
		if (info.equals("add")) {
			if (isSelInLoop)
				queryStr = "EXTRA_INFO = replace(EXTRA_INFO,'/>',' DEACT_REFID=\""
						+ refID + "\" LOOP=\"TRUE\"/>')";
			else
				queryStr = "EXTRA_INFO = replace(EXTRA_INFO,'/>',' DEACT_REFID=\""
						+ refID + "\"/>')";
		} else if (info.equals("replace")) {
			if (isSelInLoop)
				queryStr = "EXTRA_INFO = replace(EXTRA_INFO,'DEACT_REFID=\""
						+ replaceRefID + "\"','DEACT_REFID=\"" + refID
						+ "\" LOOP=\"TRUE\"')";
			else
				queryStr = "EXTRA_INFO = replace(EXTRA_INFO,'DEACT_REFID=\""
						+ replaceRefID + "\"','DEACT_REFID=\"" + refID + "\"')";

		} else if (info.equals("create")) {
			String extraInfo = null;
			extraInfo = DBUtility.setXMLAttribute(null, DEACT_REFID, refID);
			if (isSelInLoop) {
				extraInfo = DBUtility.setXMLAttribute(extraInfo, LOOP, "TRUE");
			}
			queryStr = "EXTRA_INFO = '" + extraInfo + "'";
		}
		logger.info("ExtraInfo query: " + queryStr);
		return queryStr;
	}

	// public boolean isNavCat(int categoryType)
	// {
	// return
	// festivalAndBoxOfficeShuffles.contains(String.valueOf(categoryType));
	// }

	private String getSubscriptionStatus(String curSubStatus) {
		boolean found = false;
		String[] subMgrSuspendedCases = null;
		String subMgrSusCasesStr = getParamAsString("SUB_MGR_SUSPENDED_CASES");
		if (subMgrSusCasesStr != null) {
			subMgrSuspendedCases = subMgrSusCasesStr.split(",");
		}
		if (subMgrSuspendedCases == null || subMgrSuspendedCases.length == 0) {
			subMgrSuspendedCases = new String[2];
			subMgrSuspendedCases[0] = "RENEWAL_SUSPENSION";
			subMgrSuspendedCases[1] = "NETWORK_INITIATED_SUSPENSION";
		}
		for (int i = 0; i < subMgrSuspendedCases.length; i++) {
			if (subMgrSuspendedCases[i].equals(curSubStatus)) {
				found = true;
				break;
			}
		}
		if (found) {
			return STATE_SUSPENDED_INIT;
		} else {
			return STATE_ACTIVATED;
		}
	}

	private String getParamAsString(String param) {
		return getParamAsString("DAEMON", param);
	}

	private String getParamAsString(String type, String param) {
		return getParamAsString(type, param, null);
	}

	private String getParamAsString(String type, String param, String defaultVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal)
					.getValue();
		} catch (Exception e) {
			logger.warn("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal;
		}
	}

	private Map<String, String> getParamAsMap(String type, String param,
			String defaultValue) {
		String result = getParamAsString(type, param, defaultValue);
		Map<String, String> map = new HashMap<String, String>();

		if (result == null || result.equals(defaultValue)) {
			logger.info(" Returning empty map for : " + param);
			return map;
		}
		String[] keyValuePairs = result.split(":");
		for (String keyValuePair : keyValuePairs) {
			String[] keyValue = keyValuePair.split("=");
			if (keyValue.length == 2) {
				map.put(keyValue[0], keyValue[1]);
			}
		}
		logger.info(" Param: " + param + ", Map: " + map);
		return map;
	}

	private int getParamAsInt(String param, int defaultVal) {
		return getParamAsInt("DAEMON", param, defaultVal);
	}

	private int getParamAsInt(String type, String param, int defaultVal) {
		try {
			String paramVal = m_rbtParamCacheManager.getParameter(type, param,
					defaultVal + "").getValue();
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.warn("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal;
		}
	}

	private boolean getParamAsBoolean(String param, String defaultVal) {
		return getParamAsBoolean("DAEMON", param, defaultVal);
	}

	private boolean getParamAsBoolean(String type, String param,
			String defaultVal) {
		try {
			boolean value = m_rbtParamCacheManager
					.getParameter(type, param, defaultVal).getValue()
					.equalsIgnoreCase("TRUE");
			logger.debug("Found param: " + param + ", value: " + value);
			return value;
		} catch (Exception e) {
			logger.warn("Unable to get param ->" + param + "  type ->" + type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}

	}
	
	public static String getSenderNumber(String circleID) {
		String senderNumber = RBTParametersUtils.getParamAsString("DAEMON", "SENDER_NO", null);
		if(circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = RBTParametersUtils.getParamAsString("DAEMON", operatorName +"_SENDER_NO", senderNumber);
			}
		}
		logger.info("senderNumber :" + senderNumber);
		return senderNumber;
	}


	private boolean isSameCaller(String firstCallerId, String secondCallerId) {
		if (firstCallerId == null && secondCallerId == null)
			return true;
		else if (firstCallerId != null && firstCallerId.equals(secondCallerId))
			return true;
		return false;
	}

	private List<String> getJingleSubClassList() {
		String subClassStr = getParamAsString("JINGLE_SUBSCRIPTION_CLASS");
		if (subClassStr != null && subClassStr.length() > 0) {
			subClassStr = subClassStr.trim();
			String[] jingleSubClassTokens = subClassStr.split(",");
			return Arrays.asList(jingleSubClassTokens);
		}
		return null;
	}

	// Added by Sreekar for airtel comes with music opt in feature
	public String processConfirmCharge(String strSubID, String strRefID) {
		if (m_rbtDBManager.confirmCharge(strSubID, strRefID))
			return "SUCCESS";
		return "FAILURE";
	}

	public String smProcessSelection(String strSubID, String strAction,
			String chargedDate, String strStatus, String strRefID,
			String strType, String strAmount, String classType,
			String strFailureInfo, String strReason, String strReasonCode,
			String strCurrentSubStatus, String offerID) {
		
		CallbackRequest callbackReqObj=new CallbackRequest();
			callbackReqObj.setStrSubID(strSubID);
			callbackReqObj.setAction(strAction);
			callbackReqObj.setChargedDate(chargedDate);
			callbackReqObj.setStatus(strStatus);
			callbackReqObj.setRefID(strRefID);
			callbackReqObj.setType(strType);
			callbackReqObj.setAmountCharged(strAmount);
			callbackReqObj.setClassType(classType);
			callbackReqObj.setReason(strReason);
			callbackReqObj.setReasonCode(strReasonCode);
			
		if (CacheManagerUtil.getChargeClassCacheManager().getChargeClass(
				classType) == null) {
			String circleID = RbtServicesMgr.getSubscriberDetail(
					new MNPContext(strSubID, "SMDaemon")).getCircleID();
			CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
					.getSmsKeywordCosDetail(classType, circleID, strType); // pass
																			// type,circle
																			// ID
			if (cosDetail != null
					&& ("SONG_PACK".equalsIgnoreCase(cosDetail.getCosType()) || "UNLIMITED_DOWNLOADS"
							.equalsIgnoreCase(cosDetail.getCosType())))
				return packSubscription(strSubID, strAction, chargedDate,
						strStatus, strRefID, strType, strAmount, classType,
						strFailureInfo, strReason, strReasonCode,
						strCurrentSubStatus, cosDetail.getCosId(), offerID);
			else{
				/*return selection(strSubID, strAction, chargedDate, strStatus,
						strRefID, strType, strAmount, classType, strReason,
						strReasonCode, null, null, null);*/
				// RBT-14301: Uninor MNP changes.
				return selection(callbackReqObj);
			}
		} else {
			/*return selection(strSubID, strAction, chargedDate, strStatus,
					strRefID, strType, strAmount, classType, strReason,
					strReasonCode, null, null, null);*/
			// RBT-14301: Uninor MNP changes.
			return selection(callbackReqObj);
		}
	}

	public String smProcessPack(String strSubID, String strAction,
			String chargedDate, String strStatus, String strRefID,
			String strType, String strAmount, String classType,
			String strFailureInfo, String strReason, String strReasonCode,
			String strCurrentSubStatus, String offerID) {

		 CallbackRequest callbackReqObj=new CallbackRequest();
			callbackReqObj.setStrSubID(strSubID);
			callbackReqObj.setAction(strAction);
			callbackReqObj.setChargedDate(chargedDate);
			callbackReqObj.setStatus(strStatus);
			callbackReqObj.setRefID(strRefID);
			callbackReqObj.setType(strType);
			callbackReqObj.setAmountCharged(strAmount);
			callbackReqObj.setClassType(classType);
			callbackReqObj.setReason(strReason);
			callbackReqObj.setReasonCode(strReasonCode);
			
		/*return selection(strSubID, strAction, chargedDate, strStatus, strRefID,
				strType, strAmount, classType, strReason, strReasonCode, null,
				null, null);*/
			// RBT-14301: Uninor MNP changes.
			return selection(callbackReqObj);
	}

	public String dircectAct(String strSubID, String strInfo, String strType,
			String strMode) throws Exception {
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = Processor
				.getSubscriber(strSubID);
		if (subscriber == null) {
			throw new Exception("SubscriberId not exist");
		}
		String prepaidYes = null;
		String cosID = null;
		String X_ONMOBILE_REASON;
		String winresponse = null;
		String strIP = "";
		strSubID = subscriber.getSubscriberID();
		HashMap<String, Object> subscriberDetail = new HashMap<String, Object>();
		if (strType != null) {
			if (strType.equalsIgnoreCase("p")) {
				prepaidYes = "y";
			} else if (strType.equalsIgnoreCase("b")) {
				prepaidYes = "n";
			}
		}
		if (prepaidYes == null) {
			String defaultSubType = getParamAsString("SMS", "DEFAULT_SUB_TYPE",
					"POSTPAID").toUpperCase();
			prepaidYes = (defaultSubType.startsWith("PRE") ? "y" : "n");
		}

		if (strSubID != null) {
			if (subscriber.isValidPrefix()) {
				if (subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
					X_ONMOBILE_REASON = "ALREADYACTIVE";
				} else {
					if (strInfo != null) {
						StringTokenizer stk = new StringTokenizer(strInfo, "|");
						while (stk.hasMoreTokens()) {
							String next = stk.nextToken();
							if (next.startsWith("cosid")) {
								cosID = next.substring(6);
							}
							if (next.startsWith("winresponse")) {
								winresponse = next.substring(12);
							}

						}
					}
					String circleID = subscriber.getCircleID();
					if (cosID != null) {
						CosDetails cosDetail = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										cosID, circleID);
						if (cosDetail != null) {
							subscriberDetail.put("SUBSCRIBER_ID", strSubID);
							subscriberDetail.put("ACT_BY", strMode);
							subscriberDetail.put("ACT_INFO", strIP);
							subscriberDetail.put("COS_DETAIL", cosDetail);
							subscriberDetail.put("IS_PREPAID", prepaidYes);
							subscriberDetail.put("WINRESPONSE", winresponse);
							boolean res = m_rbtDBManager
									.activateSubscriber(subscriberDetail);
							if (res) {
								boolean realTime = getParamAsBoolean("DAEMON",
										"REAL_TIME_SELECTIONS", "false");
								m_rbtDBManager
										.smUpdateSelStatusSubscriptionSuccess(
												strSubID, realTime);
								X_ONMOBILE_REASON = "SUCCESS";
							} else {
								X_ONMOBILE_REASON = "FAILURE";
							}
						} else {
							X_ONMOBILE_REASON = "INVALID_PARAMETER";
						}
					} else {
						X_ONMOBILE_REASON = "MISSING_PARAMETER";
					}
				}
			} else {
				X_ONMOBILE_REASON = "INVALID_PARAMETER";
			}
		} else {
			X_ONMOBILE_REASON = "MISSING_PARAMETER";
		}
		return X_ONMOBILE_REASON;
	}

	private boolean smAnnouncementToBeActivated(String subscriberId) {
		logger.info("RBT::inside smAnnouncementToBeActivated");
		SubscriberAnnouncements[] subscriberAnnouncements = m_rbtDBManager
				.smGetActiveAndCallbackPendingSubAnnouncemets(subscriberId);
		if (subscriberAnnouncements == null
				|| subscriberAnnouncements.length <= 0) {
			logger.info("No records found for active announcement and Base Callback pending");
			return false;
		}
		boolean status = m_rbtDBManager
				.smAnnouncementsToBeActivated(subscriberId);
		return status;
	}

	private String processSubscriptionDeactivation(Subscriber subscriber,
			String classType, String reason, String circleIdFromPrism) {
		String result = null;

		String subscriptionYes = subscriber.subYes();
		String oldSubClass = subscriber.oldClassType();
		String extraInfo = subscriber.extraInfo();

		if (subscriptionYes == null) {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.deactivatedBy(), "303", "Unsubscription", null,
					null);
			return SUBSCRIPTION_DOES_NOT_EXIST;
		} else if (!subscriptionYes.equals("D") && !subscriptionYes.equals("P")
				&& !subscriptionYes.equals("F") && !subscriptionYes.equals("Z")) {
			if (subscriptionYes.equalsIgnoreCase("X")) {
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.deactivatedBy(), "303", "Unsubscription",
						null, null);
				return SUBSCRIPTION_ALREADY_DEACTIVE;
			} else if (subscriptionYes.equals("A")) {
				writeEventLog(extraInfo, subscriber.subID(),
						subscriber.deactivatedBy(), "303", "Unsubscription",
						null, null);
				return CALLBACK_ALREADY_RECEIVED;
			} else if ((subscriptionYes.equals("E") || subscriptionYes
					.equals("N")) && oldSubClass == null)
				return SUBSCRIPTION_ACT_PENDING;
			else if (subscriptionYes.equals("B"))
				return SUBSCRIPTION_ACTIVE;
			else if (subscriptionYes.equals("C")
					|| (subscriptionYes.equals("E") && oldSubClass != null))
				return UPGRADATION_PENDING;
		}
		if (subscriber.rbtType() == 1
				&& getParamAsString(ADRBT_DEACT_URL) != null) {
			// updating extraInfo with AdRBTDeactivation=true so that we hit
			// comviva confirm unsubscription url (TTML)
			extraInfo = DBUtility.setXMLAttribute(extraInfo,
					EXTRA_INFO_ADRBT_DEACTIVATION, "true");
		}
		// update announcement to be activated in announcement table
		boolean announcementDeactivation = false;
		if (getParamAsBoolean(COMMON, PROCESS_ANNOUNCEMENTS, "false"))
			announcementDeactivation = smAnnouncementToBeActivated(subscriber
					.subID());
		// RBT-14536-Vodafone In:- Deactivated subscriber with UDS true in
		// Extra_info when request for song RBT is rejecting request
		if (extraInfo != null) {
			HashMap<String, String> xtraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfo.indexOf("UNSUB_DELAY") != -1) {
				xtraInfoMap.remove("UNSUB_DELAY");
			}
			if (extraInfo.indexOf("UDS_OPTIN") != -1) {
				xtraInfoMap.remove("UDS_OPTIN");
			}
			if (extraInfo.indexOf("USER_CONSENT") != -1) {
				xtraInfoMap.remove("USER_CONSENT");
			}
			extraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
		}
		result = smDeactivationSuccess(subscriber.subID(), subscriber.subYes(),
				classType, reason, false, extraInfo, subscriber.language(),
				!announcementDeactivation);
		if (result != null && result.equalsIgnoreCase("SUCCESS")) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleId != null && circleIdFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIdFromPrism)) {
					smUpdateCircleIdForSubscriber(true, subscriber.subID(),
							circleIdFromPrism, subscriber.refID(), null);
				}
			}
			if (isTnbReminderEnabled(subscriber)) {
				deleteTNBSubscriber(subscriber.subID());
			}
			if (isTrialReminderEnabled(subscriber)) {
				deleteTrialSelection(subscriber.subID());
			}
		}
		/**
		 * @author Sreekar For BSNL ADRBT, we accept ADRBT activation even if
		 *         user's deactivation is in pending.
		 */
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		// We will need to activate the user on ADRBT again now
		if (extraInfoMap != null
				&& extraInfoMap.containsKey(EXTRA_INFO_ADRBT_ACTIVATION)) {
			String activatedBy = "ADRBT";
			String activationInfo = activatedBy;
			if (extraInfoMap.containsKey(EXTRA_INFO_ADRBT_MODE)) {
				activatedBy = extraInfoMap.get(EXTRA_INFO_ADRBT_MODE);
				if (activatedBy.indexOf(":") != -1)
					activatedBy = activatedBy.substring(0,
							activatedBy.indexOf(":"));
			}
			//RBT-9873 Added null for xtraParametersMap for CG flow
			m_rbtDBManager.activateSubscriber(subscriber.subID(), activatedBy,
					null, null, subscriber.prepaidYes(), 0, 0, activationInfo,
					"DEFAULT", true, null, false, 1, extraInfoMap, null, null,
					false, null);
		}
		m_rbtDBManager.removeViralSMSOfCaller(subscriber.subID(), "GIFTED");
		if (result.equalsIgnoreCase("SUCCESS")) {
			writeEventLog(extraInfo, subscriber.subID(),
					subscriber.deactivatedBy(), "0", "Unsubscription", null,
					null);
		}
		return result;
	}

	private void deleteTrialSelection(String subID) {
		try {
			m_rbtDBManager.deleteTrialSelection(subID);
		} catch (OnMobileException oe) {
			logger.error("Exception deleting the trial selection: ", oe);
		}
	}

	private void deleteTNBSubscriber(String subID) {
		try {
			m_rbtDBManager.deleteTNBSubscriber(subID);
		} catch (OnMobileException oe) {
			logger.error("Exception deleting the TnB subscriber: ", oe);
		}

	}

	private boolean isTnbReminderEnabled(Subscriber subscriber) {
		if (subscriber == null || subscriber.subscriptionClass() == null)
			return false;
		if (ReminderTool.reminderDaemonModes == null
				|| ReminderTool.reminderDaemonModes.size() == 0)
			return false;
		if (ReminderTool.reminderDaemonModes.contains("SUB")
				&& ReminderTool.tnbOptinMap.containsKey(subscriber
						.subscriptionClass()))
			return true;
		if (ReminderTool.reminderDaemonModes.contains("SUB_CLASS")
				&& ReminderTool.tnbOptoutMap.containsKey(subscriber
						.subscriptionClass()))
			return true;
		return false;
	}

	private boolean isTrialReminderEnabled(Subscriber subscriber) {
		if (subscriber == null || subscriber.subscriptionClass() == null)
			return false;
		if (ReminderTool.reminderDaemonModes == null
				|| ReminderTool.reminderDaemonModes.size() == 0)
			return false;
		if (ReminderTool.reminderDaemonModes.contains("TRIAL"))
			return true;
		return false;
	}

	private boolean isTrialReminderPack(String chargeClass) {
		if (chargeClass == null || ReminderTool.esiaTrialMap == null
				|| ReminderTool.esiaTrialMap.size() == 0)
			return false;
		if (ReminderTool.esiaTrialMap.containsKey(chargeClass))
			return true;
		return false;
	}

	private String processUnsubscriptionFailure(Subscriber subscriber,
			String oldSubClass, String reason, String type, String circleIdFromPrism) {
		String subscriptionYes = subscriber.subYes();
		String strSubID = subscriber.subID();

		if (subscriptionYes == null)
			return SUBSCRIPTION_DOES_NOT_EXIST;
		else if (!subscriptionYes.equals("D") && !subscriptionYes.equals("P")
				&& !subscriptionYes.equals("F")) {
			if (subscriptionYes.equals("X"))
				return SUBSCRIPTION_ALREADY_DEACTIVE;
			else if (subscriptionYes.equals("E")) {
				if (oldSubClass == null)
					return SUBSCRIPTION_ACTIVE;
				else
					return UPGRADATION_PENDING;
			} else if (subscriptionYes.equals("C"))
				return CALLBACK_ALREADY_RECEIVED;
			else if (subscriptionYes.equalsIgnoreCase("Z"))
				return m_SUCCESS;
			else if (m_actStatus.contains(subscriptionYes))
				return SUBSCRIPTION_ACTIVE;
		}

		String ret = smUnsubscriptonFailure(strSubID, type);
		if (reason != null && !reason.equalsIgnoreCase("null"))
			m_rbtDBManager.updateExtraInfo(strSubID,
					EXTRA_INFO_FAILURE_MESSAGE, reason);
		if (ret.equalsIgnoreCase(m_success)) {
			// RBT-14301: Uninor MNP changes.
			if (subscriber != null) {
				String circleId = subscriber.circleID();
				if (circleId != null && circleIdFromPrism != null
						&& !circleId.equalsIgnoreCase(circleIdFromPrism)) {
					smUpdateCircleIdForSubscriber(true, subscriber.subID(),
							circleIdFromPrism, subscriber.refID(), null);
				}
			}
			return m_SUCCESS;
		}
		else if (ret.equalsIgnoreCase(m_failure))
			return m_INVALID;
		else
			return m_FAILURE;
	}

	private String getToneType(int status, int categoryType) {
		String toneType = null;
		if (status == 99)
			toneType = WebServiceConstants.PROFILE;
		else if (status == 90)
			toneType = CRICKET;
		else if (Utility.isShuffleCategory(categoryType))
			toneType = WebServiceConstants.CATEGORY_SHUFFLE;
		else if (categoryType == iRBTConstant.DYNAMIC_SHUFFLE)
			toneType = WebServiceConstants.CATEGORY_DYNAMIC_SHUFFLE;
		else if (categoryType == iRBTConstant.RECORD)
			toneType = WebServiceConstants.CATEGORY_RECORD;
		else if (categoryType == iRBTConstant.KARAOKE)
			toneType = WebServiceConstants.CATEGORY_KARAOKE;
		else if (categoryType == iRBTConstant.FEED_CATEGORY)
			toneType = WebServiceConstants.CATEGORY_FEED;
		else
			toneType = WebServiceConstants.CLIP;
		return toneType;
	}

	private String getToneType(int clipId) {
		String toneType = null;
		if (clipId == -1) {
			toneType = WebServiceConstants.CATEGORY_SHUFFLE;
		} else {
			toneType = WebServiceConstants.CLIP;
		}
		return toneType;
	}

	/*
	 * Remove TNB_USER=TRUE from extrainfo column in RBT_SUBSCRIBER table.
	 */
	private void changeTNBUserToNormalUser(int clipId, String strSubID) {
		Parameters parameter = m_rbtParamCacheManager.getParameter(
				iRBTConstant.COMMON, "TNB_DEFAULT_CLIP_ID", null);
		int iClipId = -1;
		if (parameter != null) {
			iClipId = Integer.parseInt(parameter.getValue());
		}
		if (clipId != iClipId) {
			return;
		}
		Subscriber subscriber = m_rbtDBManager.getSubscriber(strSubID);
		if (subscriber != null) {
			String userInfo = subscriber.extraInfo();
			Map<String, String> userInfoMap = DBUtility
					.getAttributeMapFromXML(userInfo);
			if (userInfoMap != null
					&& userInfoMap.containsKey(iRBTConstant.TNB_USER)
					&& userInfoMap.get(iRBTConstant.TNB_USER).equalsIgnoreCase(
							"TRUE")) {
				userInfoMap.remove(iRBTConstant.TNB_USER);
				userInfo = DBUtility.getAttributeXMLFromMap(userInfoMap);
				m_rbtDBManager.updateExtraInfo(strSubID, userInfo);
			}
		}
	}

	/**
	 * @param subId
	 * @param download
	 * @param clip
	 */
	private void addDownloadForMappedClip(String subId,
			SubscriberDownloads download, Clip clip) {
		String configParameters = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "MAPPED_CLIPID_DOWNLOAD_CONFIG", null);
		if (configParameters == null) {
			logger.warn("The config Parameters are not configured, so not adding the download");
			return;
		}

		if (clip == null || clip.getClipInfo() == null) {
			logger.info("Not making the download as clip is null or clipInfo is null for clipWavFile : "
					+ download.promoId());
			return;
		}

		try {
			int parentClipID = clip.getClipId();
			int mappedClipID = getMappedClipID(clip);
			if (mappedClipID == -1)
				return;

			String classType = null;
			String selectedBy = download.selectedBy();
			int categoryID = download.categoryID();

			String[] tokens = configParameters.split(",");
			if (tokens.length >= 1)
				classType = tokens[0];
			if (tokens.length >= 2)
				selectedBy = tokens[1];
			if (tokens.length >= 3) {
				try {
					categoryID = Integer.parseInt(tokens[2]);
				} catch (NumberFormatException e) {
					logger.error(e.getMessage(), e);
				}
			}

			if (mappedClipID == -1)
				return;

			clip = m_rbtCacheManager.getClip(mappedClipID);
			if (clip != null) {
				String selectionInfo = "PCID:" + parentClipID;
				com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = m_rbtCacheManager
						.getCategory(categoryID);
				Categories categoriesObj = CategoriesImpl.getCategory(category);
				if (category != null && categoriesObj != null) {
					String response = m_rbtDBManager.addSubscriberDownloadRW(
							subId, clip.getClipRbtWavFile(), categoriesObj,
							null, true, classType, selectedBy, selectionInfo,
							null, true, true, false, null, null);

					logger.debug("Response while adding the download : "
							+ response);
				}
			} else {
				logger.warn("Clip is null for the clipID : " + mappedClipID);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private int getMappedClipID(Clip clip) {
		String clipInfo = clip.getClipInfo();
		int pcidIndex = clipInfo.indexOf("PCID=");
		if (pcidIndex == -1) {
			logger.info("PCID not configured for the clipID : "
					+ clip.getClipId());
			return -1;
		}

		String subStr = clipInfo.substring(pcidIndex + 5);
		int mappedClipID = -1;
		try {
			int index = subStr.indexOf("|");
			if (index != -1)
				mappedClipID = Integer.parseInt(subStr.substring(0, index));
			else
				mappedClipID = Integer.parseInt(subStr);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return mappedClipID;
	}

	private String getRetailerFinalSMS(String smsText, String subscriberId,
			String songName, String clipPromoId) {
		if (songName != null) {
			smsText = smsText.replaceAll("%S", songName);
		}
		if (clipPromoId != null) {
			smsText = smsText.replaceAll("%P", clipPromoId);
		}
		smsText = smsText.replaceAll("%SUBID%", subscriberId);
		return smsText;
	}

	private boolean isModeSupportedForZoominSMS(String selectionInfo) {
		if (selectionInfo == null)
			return false;

		if (zoominSupportedModesList == null) {
			String modesStr = getParamAsString(DAEMON,
					"ZOOMIN_SUPPORTED_MODES", "");
			String[] modes = modesStr.toUpperCase().split(",");
			zoominSupportedModesList = Arrays.asList(modes);
		}

		if (zoominSupportedModesList != null) {
			for (String eachMode : zoominSupportedModesList) {
				if (selectionInfo.contains(eachMode))
					return true;
			}
		}

		return false;
	}

	/*
	 * Added by SenthilRaja Change TNB Optin to Optout service
	 */
	private void chageTNBUserOptinModeToOptOut(String action,
			SubscriberStatus subscriberStatus, SubscriberDownloads download,
			Subscriber subscriber) throws OnMobileException {
		boolean isTNBNewFlow = getParamAsBoolean(
				ConstantsTools.SUPPORT_TNB_NEW_FLOW, "FALSE");

		if (!isTNBNewFlow) {
			// Not support TNB new flow
			logger.debug("Not support new TNB Flow");
			return;
		}
		if (!m_strActionActivation.equalsIgnoreCase(action)) {
			// Not Selection or Download activation callback
			logger.debug("Not Selection or Download activation success callback");
			return;
		}
		String subClassType = subscriber.subscriptionClass();
		if (!m_tnbSubClassesList.contains(subClassType)) {
			// Suscriber is not a TNB Subscriber
			logger.debug("Subscriber is not a TNB Subscriber "
					+ subscriber.subID());
			return;
		}
		String classType = null;
		String refId = null;
		if (subscriberStatus != null) {
			classType = subscriberStatus.classType();
			refId = subscriberStatus.refID();
		} else if (download != null) {
			classType = download.classType();
			refId = download.refID();
		}
		ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools.getParameter(
				"COMMON", "TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO", ",");
		
		ArrayList<String> tnbFreeChargeClass = DBConfigTools.getParameter("DAEMON","TNB_FREE_CHARGE_CLASS",ConstantsTools.FREE,",");
		if (tnbFreeChargeClass.contains(classType) || classType.startsWith(ConstantsTools.FREE)) {
			// Free Selection or Download Callback
			logger.debug("Free selection classType: " + classType + " tnbFreeChargeClass: " + tnbFreeChargeClass);
			return;
		}
		
		
		String newSubClassType = null;
		String mode = null;
		if (subscriberStatus != null) {
			mode = subscriberStatus.selectedBy();
		} else if (download != null) {
			mode = download.selectedBy();
		}
		
		String modeAndSubClass = mode + "_" + subClassType;
		for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
			String[] split = tnbUpgradeSubClass.split("\\:");
			if (split == null || split.length != 2) {
				continue;
			}

			if (modeAndSubClass.equalsIgnoreCase(split[0])) {
				newSubClassType = split[1];
				logger.debug("Mode based new subscription class is found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
						+ modeAndSubClass + ", newSubClassType: " + newSubClassType);
				break;
			} else {
				logger.debug("Mode based new subscription class is not found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
						+ modeAndSubClass + " from " + tnbUpgradeSubClass);
			}
		}

		if (null == newSubClassType) {
			logger.debug("Fetching old to new subscription class wise configuration");
			for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
				String[] split = tnbUpgradeSubClass.split("\\:");
				if (split == null || split.length != 2) {
					continue;
				}

				if (subClassType.equalsIgnoreCase(split[0])) {
					newSubClassType = split[1];
					break;
				}
			}
			logger.debug("Fetched old to new subscription class wise configuration. newSubClassType: "
					+ newSubClassType);
		}
		
		//JIRA-ID:9964-121 Campaign
		String extraInfo = null;
		if (subscriber != null) {
			Map<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			if(xtraInfoMap == null) {
				xtraInfoMap = new HashMap<String,String>();
			}
			
			if(subscriberStatus != null) {
				xtraInfoMap.put("SELECTION_MODE", subscriberStatus.selectedBy());
			}
			else if(download != null) {
				xtraInfoMap.put("SELECTION_MODE", download.selectedBy());
			}			
			extraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
		}
		
		if (m_rbtDBManager.convertSubscriptionType(subscriber.subID(), subClassType, newSubClassType, null,
				0, false, extraInfo, subscriber)) {
			String unsubscriptionMsg = DBConfigTools.getSmsText(SMS,
					ConstantsTools.UPGRADE_TNB_MSG, subscriber.language());
			Tools.sendSMS(DBConfigTools.getParameter(SMS,
					ConstantsTools.SMS_NO, "123456"), subscriber.subID(),
					unsubscriptionMsg, false);
			if (!isTNBNewFlow)
				m_rbtDBManager.deleteTNBSubscriber(subscriber.subID());
			// If active downloads exits for subscriber, Deactivating the free
			// downloads
			m_rbtDBManager.smUpdateDownloadTNBCallback(subscriber, refId);
		}
	}

	/**
	 * @added sridhar.sindiri
	 * 
	 *        <p>
	 *        Deactivate all the personalized settings and their corresponding
	 *        downloads which are under music pack
	 * 
	 * @see RBTDaemonHelper#reactivateDownloadsAndSettingsUnderPack
	 * 
	 * @param subscriber
	 * @param cosdetail
	 */
	private void deactivateDownloadsUnderPack(Subscriber subscriber,
			CosDetails cosdetail) {
		if (!RBTParametersUtils.getParamAsBoolean(COMMON, "DEACT_DOWNLOADS_ON_PACK_DEACT", "FALSE")
				|| (cosdetail != null && MUSIC_POUCH.equalsIgnoreCase(cosdetail.getCosType()))) {
			logger.debug("DEACT_DOWNLOADS_ON_PACK_DEACT parameter is configured as false, so not deactivating any downloads");
			return;
		}

		List<String> allCallerWavFilesList = new ArrayList<String>();
		SubscriberStatus[] settings = m_rbtDBManager
				.getAllActiveSubscriberSettings(subscriber.subID());
		if (settings != null && settings.length > 0) {
			for (SubscriberStatus setting : settings) {
				if (setting.callerID() == null)
					allCallerWavFilesList.add(setting.subscriberFile());
			}
		}

		SubscriberDownloads[] downloads = m_rbtDBManager
				.getActiveSubscriberDownloads(subscriber.subID());
		if (downloads != null && downloads.length > 0) {
			for (SubscriberDownloads download : downloads) {
				String downloadExtraInfo = download.extraInfo();
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(downloadExtraInfo);
				if (extraInfoMap == null)
					continue;

				String packCosID = extraInfoMap.get(PACK);
				CosDetails packCos = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(packCosID);
				if (packCos != null
						&& packCos.getCosType().equalsIgnoreCase(
								cosdetail.getCosType())
						&& !allCallerWavFilesList.contains(download.promoId())) {
					// deactivate the download.
					m_rbtDBManager.expireSubscriberDownload(subscriber.subID(),
							download.refID(), "DAEMON");
				}
			}
		}
	}
    /*
     * For deactivating the selections under the ODA Pack
     * category type=16
     */
	private void deactivateSelectionsUnderODAPack(Subscriber subscriber,
                  Category category,String callerID,String refID) {
		logger.info("Going to deactivate Selections under ODA Pack for Subscriber = "+subscriber);
		SubscriberStatus[] settings = m_rbtDBManager.getActiveSelectionBasedOnCallerId(subscriber
				.subID(),callerID);
		if (settings != null && settings.length > 0) {
			for (SubscriberStatus setting : settings) {
				String extraInfo = setting.extraInfo();
				HashMap<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				String provRefId = null;
				if(xtraInfoMap!=null && xtraInfoMap.containsKey("PROV_REF_ID")){
					provRefId = xtraInfoMap.get("PROV_REF_ID");
				}
				if (category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE
						&& category.getCategoryId() == setting.categoryID() && provRefId != null
						&& provRefId.equalsIgnoreCase(refID)) { 
					m_rbtDBManager.expireSubscriberSelection(subscriber.subID(), setting.refID(),
							"DAEMON");
				}
			}
		}
	}

	/**
	 * @added sridhar.sindiri
	 * 
	 *        <p>
	 *        Reactivate all the deactive settings and downloads which are under
	 *        pack. The settings are reactivated in such a way that the existing
	 *        selections are not affected.
	 * 
	 * @see RBTDaemonHelper#deactivateDownloadsUnderPack(Subscriber, CosDetails)
	 * 
	 * @param subscriber
	 * @param cosdetail
	 */
	private void reactivateDownloadsAndSettingsUnderPack(Subscriber subscriber,
			CosDetails cosdetail) {
		if (!RBTParametersUtils.getParamAsBoolean(COMMON,
				"DEACT_DOWNLOADS_ON_PACK_DEACT", "FALSE")) {
			logger.debug("DEACT_DOWNLOADS_ON_PACK_DEACT parameter is configured as false, so not deactivating any downloads");
			return;
		}

		SubscriberDownloads[] subDownloads = m_rbtDBManager
				.getDeactiveSubscriberDownloads(subscriber.subID());
		if (subDownloads == null || subDownloads.length == 0) {
			logger.info("No deactive downloads for the subscriber, so not reactivating");
			return;
		}

		List<String> reactivatedDownloadsList = new ArrayList<String>();
		for (SubscriberDownloads download : subDownloads) {
			Map<String, String> downloadExtraInfoMap = DBUtility
					.getAttributeMapFromXML(download.extraInfo());
			if (downloadExtraInfoMap == null
					|| !downloadExtraInfoMap.containsKey(PACK))
				continue;

			String cosID = downloadExtraInfoMap.get(PACK);
			CosDetails downloadCosDetail = CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(cosID);

			if (downloadCosDetail.getCosType().equalsIgnoreCase(
					cosdetail.getCosType())) {
				reactivatedDownloadsList.add(download.promoId());
				String newChargeClass = download.classType();
				if (reactivateDownloadChargeClassMap != null
						&& download.classType() != null
						&& reactivateDownloadChargeClassMap
								.containsKey(download.classType()))
					newChargeClass = reactivateDownloadChargeClassMap
							.get(download.classType());

				m_rbtDBManager.reactivateDownload(subscriber.subID(),
						download.promoId(), download.categoryID(),
						download.categoryType(), newChargeClass,
						download.selectedBy(), download.extraInfo());
			}
		}

		Map<String, SubscriberStatus> callerIDSettingMap = new HashMap<String, SubscriberStatus>();
		SubscriberStatus[] settings = m_rbtDBManager
				.getAllSubSelectionRecordsForTonePlayer(subscriber.subID());
		if (settings != null && settings.length > 0) {
			for (SubscriberStatus setting : settings) {
				callerIDSettingMap.put(setting.callerID(), setting);
			}
		}

		Set<Entry<String, SubscriberStatus>> callerIDSettingEntrySet = callerIDSettingMap
				.entrySet();
		for (Entry<String, SubscriberStatus> eachEntry : callerIDSettingEntrySet) {
			SubscriberStatus setting = eachEntry.getValue();
			if (!m_rbtDBManager.isSelectionDeactivated(setting)
					&& !m_rbtDBManager.isSelectionDeactivationPending(setting))
				continue;

			if (reactivatedDownloadsList.contains(setting.subscriberFile()))
				m_rbtDBManager.smReactivateSelection(setting.subID(),
						setting.refID(), setting.callerID(), 'o', 0,
						setting.extraInfo(), STATE_BASE_ACTIVATION_PENDING);
		}
	}

	private void parseReactivateChargeClass() {
		try {
			String reactivateChargeClassMap = RBTParametersUtils
					.getParamAsString(DAEMON, "REACTIVATE_CHARGE_CLASS_MAP",
							null);
			if (reactivateChargeClassMap == null)
				return;
			reactivateDownloadChargeClassMap = new HashMap<String, String>();
			String[] chargeClasses = reactivateChargeClassMap.split(",");
			for (String chargeClassMap : chargeClasses) {
				String chargeClassesMap[] = chargeClassMap.split(":");
				if (chargeClassesMap != null && chargeClassesMap.length == 2)
					reactivateDownloadChargeClassMap.put(
							chargeClassesMap[0].trim(),
							chargeClassesMap[1].trim());
				else {
					logger.error("Configuration REACTIVATE_CHARGE_CLASS_MAP is not proper. Please include the new and old charge class");
				}
			}
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		logger.info("reactivateDownloadChargeClassMap="
				+ reactivateDownloadChargeClassMap);
	}

	/**
	 * @added sridhar.sindiri
	 * 
	 *        <p>
	 *        This method adds a selection to the subscriber if he/she is
	 *        activated on a subscription pack which has a configured clipID in
	 *        the operatorCode2
	 * 
	 *        <p>
	 *        The operatorCode2 can be configured as
	 *        <b>"ClipID,ChargeClass,SelectedBy,CategoryID"</b> Except clipID,
	 *        rest all fields are optional.
	 * 
	 * @param strSubID
	 * @param subClass
	 */
	private void addSelectionBasedOnSubClassOpCode(String strSubID,
			SubscriptionClass subClass, String mode) {
		if (subClass != null && subClass.getOperatorCode2() != null) {
			String opCode = subClass.getOperatorCode2();
			String[] tokens = opCode.split(",");
			Clip clip = null;
			try {
				clip = m_rbtCacheManager.getClip(tokens[0].trim());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			if (clip == null) {
				logger.info("ClipID configured is invalid, so not adding the song");
				return;
			}

			String chargeClass = null;
			if (tokens.length > 1)
				chargeClass = tokens[1].trim();

			String catID = "3";
			if (tokens.length > 2)
				catID = tokens[2].trim();

			String selBy = mode;
			if (tokens.length > 3)
				selBy = tokens[3].trim();

			SelectionRequest selRequest = new SelectionRequest(strSubID, catID,
					String.valueOf(clip.getClipId()));
			selRequest.setMode(selBy);
			if (chargeClass != null && chargeClass.length() > 0) {
				selRequest.setChargeClass(chargeClass);
				selRequest.setUseUIChargeClass(true);
			}
			RBTClient.getInstance().addSubscriberSelection(selRequest);
			logger.info("Response while adding the selection : "
					+ selRequest.getResponse());
		}
	}

	private void hitBIForSongPurchase(String subscriberId, String refId,
			String mode, String toneId) {
		String className = RBTContentJarParameters.getInstance().getParameter(
				"BI_CLASS_IMPL");
		if (className == null) {
			logger.debug("Implementation class is not configured. configuration name "
					+ "BI_CLASS_IMPL");
			return;
		}
		BIInterface bi = null;
		try {
			bi = (BIInterface) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			logger.error("Exception: ", e);
		} catch (IllegalAccessException e) {
			logger.error("Exception: ", e);
		} catch (ClassNotFoundException e) {
			logger.error("Exception: ", e);
		}
		if (subscriberId == null) {
			logger.info("Taking subscriberId from Configuration, because client doesn't pass subscriberId");
			subscriberId = RBTContentJarParameters.getInstance().getParameter(
					"BI_DEFAULT_SUBSCRIBERID");
		}
		if (bi == null) {
			return;
		}

		bi.processHitBIForPurchase(subscriberId, refId, mode, toneId);
	}

	/**
	 * <p>
	 * This method disables the intro prompt on renewal success callback if the
	 * subscriptionClass of the subscriber is configured in the parameter and
	 * the parameter DISABLE_PROMPT_ON_RENEWAL is configured as TRUE
	 * 
	 * @param subscriber
	 * @param classType
	 */
	private void disablePromptForSubscriber(Subscriber subscriber,
			String classType) {
		if (!RBTParametersUtils.getParamAsBoolean(DAEMON,
				"DISABLE_PROMPT_ON_RENEWAL", "FALSE")) {
			if (logger.isDebugEnabled())
				logger.debug("The parameter DISABLE_PROMPT_ON_RENEWAL is configured as FALSE, so not disabling intro prompt");
			return;
		}

		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		if (extraInfoMap != null
				&& extraInfoMap
						.containsKey(iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT)) {
			if (Arrays.asList(
					RBTParametersUtils.getParamAsString("COMMON",
							"PRE_PROMPT_SUPPORTED_SUB_CLASSES", "").split(","))
					.contains(classType)) {
				boolean success = m_rbtDBManager
						.disablePressStarIntro(subscriber);
				if (logger.isDebugEnabled())
					logger.debug("Disabling intro prompt for subscriberID : "
							+ subscriber.subID() + ", status : " + success);
			}
		}
	}

	/**
	 * @param clip
	 * @return
	 */
	private static String getCPForClip(Clip clip) {
		if (clip == null || clip.getClipInfo() == null) {
			logger.info("Clip or clipInfo is null, so not getting the CP");
			return null;
		}

		String clipInfo = clip.getClipInfo();
		int cpIndex = clipInfo.indexOf("CP=");
		if (cpIndex == -1) {
			logger.info("CP not configured for the clipID : "
					+ clip.getClipId());
			return null;
		}

		String subStr = clipInfo.substring(cpIndex + 3);
		String cpName = null;
		try {
			int index = subStr.indexOf("|");
			if (index != -1)
				cpName = subStr.substring(0, index);
			else
				cpName = subStr;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return cpName.trim();
	}

	private static void initCopyContestIDsTimeMap() {
		String confParam = RBTParametersUtils.getParamAsString("COMMON",
				"CONTEST_ID_TIME_VALIDITY_MAP", null);
		if (confParam == null)
			return;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String str[] = confParam.split(";");
			for (String contestIdAndTime : str) {
				String aa[] = contestIdAndTime.split("=");
				if (aa.length == 2) {
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
		} catch (Exception ex) {
			logger.info("Exception while initializing Copy Contest ID and Time Duration Mapping");
		}

	}

	private static String getCurrentLiveContestID(String circleID) {
		String nationalContestIDs = RBTParametersUtils.getParamAsString(
				"COMMON", "ALL_CONTEST_IDS", null);
		String contestIDs = RBTParametersUtils.getParamAsString("COMMON",
				circleID + "_CONTEST_IDS", null);

		if (contestIDs == null && nationalContestIDs == null)
			return null;
		String currentContestID = null;
		if (nationalContestIDs != null) {
			String ids[] = nationalContestIDs.split(",");
			for (String id : ids) {
				List<Date> list = copyContestIDsTimeValidityMap.get(id);
				if (list != null && list.get(0).before(new Date())
						&& list.get(1).after(new Date())) {
					currentContestID = id;
					break;
				}
			}
		}
		if (contestIDs != null && currentContestID == null) {
			String ids[] = contestIDs.split(",");
			for (String id : ids) {
				List<Date> list = copyContestIDsTimeValidityMap.get(id);
				if (list != null && list.get(0).before(new Date())
						&& list.get(1).after(new Date())) {
					currentContestID = id;
					break;
				}
			}
		}
		return currentContestID;
	}

	private void deactiveSubscriberNoActiveSelection(Subscriber subscriber,
			String type) {
		String songBaseCosId = getParamAsString(iRBTConstant.COMMON,
				"SONG_BASED_COS_ID", null);
		String deactivatedBy = getParamAsString(iRBTConstant.COMMON,
				"CALLBACKS_DEACTIVATE_MODE", "DAEMON");
		if (songBaseCosId != null) {
			logger.info("Song based cos id is enabled. subscriber: "
					+ subscriber + ", type: " + type);
			List<String> cosIdList = Arrays.asList(songBaseCosId.split("\\,"));
			if (cosIdList.contains(subscriber.cosID())) {
				SubscriberStatus[] activeSubscriberStatus = m_rbtDBManager
						.getAllActiveSubscriberSettings(subscriber.subID());
				boolean toBaseDeactive = true;
				if (activeSubscriberStatus != null) {
					for (SubscriberStatus activeSelection : activeSubscriberStatus) {
						logger.info("Checking selection status and caller. subscriber: "
								+ subscriber
								+ ", activeSelection: "
								+ activeSelection);
						if (activeSelection.status() == 1
								&& (activeSelection.callerID() == null || activeSelection
										.callerID().equalsIgnoreCase("all"))) {
							toBaseDeactive = false;
						}
					}
				}
				logger.info("Checking to deactivate base or not. subscriber: "
						+ subscriber + ", type: " + type + ", toBaseDeactive: "
						+ toBaseDeactive+", deactivatedBy: "+deactivatedBy);
				if (toBaseDeactive) {
					m_rbtDBManager.smDeactivateSubscriber(subscriber.subID(),
							deactivatedBy, null, true, true, true, type);
				}
			}
		}
	}
	
	private void deactivateMusicPackDownloads(Subscriber subscriber,
			CosDetails cosdetail) {

		SubscriberDownloads[] downloads = m_rbtDBManager
				.getActiveSubscriberDownloads(subscriber.subID());
		if (downloads != null && downloads.length > 0) {
			for (SubscriberDownloads download : downloads) {
				String downloadExtraInfo = download.extraInfo();
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(downloadExtraInfo);
				if (extraInfoMap == null)
					continue;

				String packCosID = extraInfoMap.get(PACK);
				CosDetails packCos = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(packCosID);
				if (packCos != null
						&& packCos.getCosType().equalsIgnoreCase(
								cosdetail.getCosType()) && packCosID.equalsIgnoreCase(cosdetail.getCosId())) {
					// deactivate the download.
					m_rbtDBManager.expireSubscriberDownload(subscriber.subID(),
							download.refID(), "DAEMON");
				}
			}
		}
	}
	
	private void updateDownloadOnDownloadSongPackAct(Subscriber subscriber, CosDetails cosdetail) {
		
//		Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//				.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
		
//		List<String> musicPackCosIdList = null;
		
//		if(muiscPackCosIdParam != null) {
//			musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//		}
		
		if ((cosdetail != null && !MUSIC_POUCH.equalsIgnoreCase(cosdetail.getCosType()) 
				&& !UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosdetail.getCosType())
				&& !LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosdetail.getCosType()))) {
			return;
		}

		logger.info("move existing active downloads to song pack downlod");
		SubscriberDownloads[] downloads = m_rbtDBManager.getActiveSubscriberDownloads(subscriber.subID());
		if(downloads != null && downloads.length > 0 && !MUSIC_POUCH.equalsIgnoreCase(cosdetail.getCosType())) {
			for (SubscriberDownloads download : downloads) {
				
				if (download.downloadStatus() != 'y') {
					continue;
				}
//				Map<String, String> downloadExtraInfoMap = DBUtility
//						.getAttributeMapFromXML(download.extraInfo());
//				
//				String classType = cosdetail.getFreechargeClass();			
//				
//				if (downloadExtraInfoMap == null) {
//					downloadExtraInfoMap = new HashMap<String, String>();				
//				}
//	
//				String cosID = downloadExtraInfoMap.get(PACK);
//				CosDetails downloadCosDetail = null;
//				if(cosID != null) {
//					downloadCosDetail = CacheManagerUtil
//						.getCosDetailsCacheManager().getCosDetail(cosID);
//				}
//	
//				if ((downloadCosDetail == null || 
//						(downloadCosDetail.getCosType().equalsIgnoreCase(
//						cosdetail.getCosType()) && 
//						!downloadCosDetail.getCosId().equalsIgnoreCase(
//								cosdetail.getCosId())))) {
//					downloadExtraInfoMap.put(PACK, cosdetail.getCosId());
//					
//					char downloadStatus = download.downloadStatus();
//					if((!download.classType().equals(classType))) {
//						downloadStatus = 'c';
//						downloadExtraInfoMap.put("OLD_CLASS_TYPE", 	download.classType());
//					}
//					String extraInfo = DBUtility.getAttributeXMLFromMap(downloadExtraInfoMap);
//					m_rbtDBManager.updateDownloads(subscriber.subID(), download.refID(), downloadStatus, extraInfo, classType);
//					
//					logger.debug("Download upgrace successfully downlad refid: " + download.refID() + " OldClassType: " + download.classType() + " new classtype: " + classType + " subscriberId: " + subscriber.subID());
//				}
				
				upgradeDownloadForDownloadLimitSongPack(download, cosdetail);
			}
		}
		
		
		//Deacivate old pack
		Map<String, String> subscriberExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		if(subscriberExtraInfoMap == null || !subscriberExtraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
			return;
		}
		
		String activePacks = subscriberExtraInfoMap.get(iRBTConstant.EXTRA_INFO_PACK);
		String[] arrActivePacks = activePacks.split("\\,");
		for(String pack : arrActivePacks) {
			CosDetails packCosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(pack);
			if (!pack.equalsIgnoreCase(cosdetail.getCosId()) && packCosDetails != null 
					&& ((MUSIC_POUCH.equalsIgnoreCase(cosdetail.getCosType())
						&& MUSIC_POUCH.equalsIgnoreCase(packCosDetails.getCosType())) 
					|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(packCosDetails.getCosType())
					|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(packCosDetails.getCosType()))) {
				List<ProvisioningRequests> provRequests = m_rbtDBManager.getProvisioningRequests(subscriber.subID(), Integer.parseInt(packCosDetails.getCosId()));
				if(provRequests == null || provRequests.size() == 0) {
					continue;
				}
				
				//Setting Active Pack
				ProvisioningRequests provRequest = null;
				for(ProvisioningRequests provReq : provRequests){
					if(provReq.getStatus()==PACK_ACTIVATED){
						provRequest = provReq;
						break;
					}
				}
				m_rbtDBManager.deactivatePack(subscriber, packCosDetails, provRequest.getTransId(), DBUtility.getAttributeMapFromXML(provRequest.getExtraInfo()));
			}
		}		
	}
	
	private void upgradeDownloadForDownloadLimitMusicPackOnDownloadCallback(SubscriberDownloads download, Subscriber subscriber) {
		
//		Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//				.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
		
//		if(muiscPackCosIdParam == null) {
//			return;
//		}
		
//		List<String> musicPackCosIdList = null;
		
//		if(muiscPackCosIdParam != null) {
//			musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//		}
		
		Map<String,String> subscriberExtraInfo = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		if(subscriberExtraInfo == null || !subscriberExtraInfo.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
			return;
		}
		CosDetails cosdetail = null;
		String activePacks = subscriberExtraInfo.get(iRBTConstant.EXTRA_INFO_PACK);
		String[] arrActivePacks = activePacks.split("\\,");
		for(String pack : arrActivePacks) {
			CosDetails tempCosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(pack); 
			if(tempCosDetail != null && (UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(tempCosDetail.getCosType()) 
					|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(tempCosDetail.getCosType()))) {
				cosdetail = tempCosDetail;				
			}
		}
		
		if(cosdetail == null) {
			return;
		}
		
		upgradeDownloadForDownloadLimitSongPack(download, cosdetail);
	}
	
	private void upgradeDownloadForDownloadLimitSongPack(SubscriberDownloads download, CosDetails cosdetail) {
		Map<String, String> downloadExtraInfoMap = DBUtility
				.getAttributeMapFromXML(download.extraInfo());
		
		String classType = cosdetail.getFreechargeClass();			
		
		if (downloadExtraInfoMap == null) {
			downloadExtraInfoMap = new HashMap<String, String>();				
		}

		String cosID = downloadExtraInfoMap.get(PACK);
		CosDetails downloadCosDetail = null;
		if(cosID != null) {
			downloadCosDetail = CacheManagerUtil
				.getCosDetailsCacheManager().getCosDetail(cosID);
		}

		if ((downloadCosDetail == null || 
				(downloadCosDetail.getCosType().equalsIgnoreCase(
				cosdetail.getCosType()) && 
				!downloadCosDetail.getCosId().equalsIgnoreCase(
						cosdetail.getCosId())))) {
			downloadExtraInfoMap.put(PACK, cosdetail.getCosId());
			
			char downloadStatus = download.downloadStatus();
			if((!download.classType().equals(classType))) {
				downloadStatus = 'c';
				downloadExtraInfoMap.put("OLD_CLASS_TYPE", 	download.classType());
			}
			String extraInfo = DBUtility.getAttributeXMLFromMap(downloadExtraInfoMap);
			m_rbtDBManager.updateDownloads(download.subscriberId(), download.refID(), downloadStatus, extraInfo, classType);
			
			logger.debug("Download upgrace successfully downlad refid: " + download.refID() + " OldClassType: " + download.classType() + " new classtype: " + classType + " subscriberId: " + download.subscriberId());
		}
	}
	
	public void updateStateBaseActPendingSelection(String strSubID, int rbtType, String response) {
		/* FOR FREEMIUM MODEL CHANGES */
		String freemiumChrgClassesNumMaxMapStr = RBTParametersUtils.getParamAsString(COMMON,
				"FREEMIUM_CHARGE_CLASSES_NUM_MAX_MAPPING", null);
		if (freemiumChrgClassesNumMaxMapStr == null)
			return;
		SubscriberStatus[] subscriberSelections = m_rbtDBManager
				.getAllActiveSubscriberSettings(strSubID);
		Set<String> provRefIdList = new HashSet<String>();
		if (subscriberSelections != null) {
			logger.info("updateStateBaseActPendingSelection = " + subscriberSelections);
			int numMaxTobeDecrement = 0;
			for (SubscriberStatus subscriberStatus : subscriberSelections) {
				if (subscriberStatus.selStatus().equals("W")) {
					HashMap<String, String> attributeMapFromXML = DBUtility
							.getAttributeMapFromXML(subscriberStatus.extraInfo());
					if (response.equalsIgnoreCase(m_SUCCESS)) {
						if(subscriberStatus.categoryType() != PLAYLIST_ODA_SHUFFLE){
						    String success = m_rbtDBManager.updateSelectionStatusNExtraInfo(strSubID,
								subscriberStatus.refID(), subscriberStatus.extraInfo(), "A", null);
							logger.info("Updated selection to A state REF_ID ="
									+ subscriberStatus.refID() + " , Result = " + success); 
						}
						if (attributeMapFromXML != null
								&& attributeMapFromXML.containsKey("PROV_REF_ID")
								&& subscriberStatus.categoryType() == PLAYLIST_ODA_SHUFFLE) {
							String refId = attributeMapFromXML.get("PROV_REF_ID");
							if (!provRefIdList.contains(refId)) {
								boolean result = ProvisioningRequestsDao.updateRequest(strSubID,
										refId, PACK_TO_BE_ACTIVATED + "", false, -1);
								logger.info("Provisioning activation response for playlist = "
										+ result);
								if (result) {
									provRefIdList.add(refId);
								}
							}
						}
					} else {
						List<String> refIdList = new ArrayList<String>();
						refIdList.add(subscriberStatus.refID());
						m_rbtDBManager.deactivateNewSelections(strSubID,
								subscriberStatus.selectedBy(), null, null, false, rbtType,
								refIdList, null);
						if (subscriberStatus.categoryType() != 16
								&& subscriberStatus.categoryType() != PLAYLIST_ODA_SHUFFLE) {
							numMaxTobeDecrement++;
						}
						if (attributeMapFromXML != null
								&& attributeMapFromXML.containsKey("PROV_REF_ID")
								&& subscriberStatus.categoryType() == PLAYLIST_ODA_SHUFFLE) {
							String refId = attributeMapFromXML.get("PROV_REF_ID");
							if (!provRefIdList.contains(refId)) {
								boolean result = ProvisioningRequestsDao.updateRequest(strSubID,
										refId, PACK_DEACTIVATED + "", false, -1);
								numMaxTobeDecrement++;
								logger.info("Provisioning Deactivation response for playlist = "
										+ result);
								if (result) {
									provRefIdList.add(refId);
								}
							}
						}

					}
				}
			}
			if(numMaxTobeDecrement > 0){
				Subscriber subscriber = m_rbtDBManager.getSubscriber(strSubID);
				if(subscriber!=null){
					int noOfSel = subscriber.maxSelections();
					int noOfCurrentSel = noOfSel - numMaxTobeDecrement;
					if(noOfCurrentSel >= 0){
				 	       m_rbtDBManager.updateNumMaxSelections(strSubID, noOfCurrentSel);
					}
				}
			}
		}
	}
	/**
	 * Method for calling RRBT activation  for grace and suspended user
	 * for grace renewal and suspension callback
	 * 
	 * @param subscriberId
	 * @param inSuspension
	 * @return
	 */
	
	private boolean callRRBTActivation(String subscriberId, boolean inSuspension) {

		logger.debug(" callRRBTActivation starts...");
		boolean isProcessed = false;
		// http://<IP:PORT>/rbt/Subscription.do?action=activate&subscriberID=%MSISDN%&isPrepaid=n&language=eng&calledNo=123&mode=TIPPS
		//&subscriptionClass=CONSENT&userInfo_rrbt_type=CONSENT&userInfo_rrbt_type_suspension=init&subscriptionPeriod=%SUBSCRIPTIONPERIOD%
		String rrbtSupportUrl = RBTParametersUtils.getParamAsString("DAEMON",
				"RRBT_ACTIVATION_URL_FOR_SUSPENSION_INIT", null); 

		if (inSuspension) {
			// http://<IP:PORT>/rbt/Subscription.do?action=activate&subscriberID=%MSISDN%&isPrepaid=n&language=eng&calledNo=123&mode=TIPPS
			//&subscriptionClass=CONSENT&userInfo_rrbt_type=CONSENT&userInfo_rrbt_type_suspension=suspended
			rrbtSupportUrl = RBTParametersUtils.getParamAsString("DAEMON",
					"RRBT_ACTIVATION_URL_FOR_SUSPENSION_SUSPENDED", null); 
		}

		if (rrbtSupportUrl == null) {
			logger.info("The Parameter RRBT_ACTIVATION_URL_FOR_SUSPENSION_INIT  or RRBT_ACTIVATION_URL_FOR_SUSPENSION_SUSPENDED is not configured");
			return false;
		}
		HttpParameters httpParameters = new HttpParameters();
		rrbtSupportUrl = rrbtSupportUrl.replaceAll("%MSISDN%", subscriberId);
		
		String subscriptionPeriod=RBTParametersUtils.getParamAsString("DAEMON", "RRBT_ACTIVATION_SUSPENSION_INIT_SUBSCRIPTION_PERIOD", null);
		if(subscriptionPeriod!=null && !subscriptionPeriod.trim().equals("")) {
		 rrbtSupportUrl = rrbtSupportUrl.replace("%SUBSCRIPTIONPERIOD%", subscriptionPeriod);
		}
		
		httpParameters.setUrl(rrbtSupportUrl);
		httpParameters.setUseProxy(isUseProxy);
		httpParameters.setProxyHost(proxyHostname);
		httpParameters.setProxyPort(proxyPort);
		httpParameters.setConnectionTimeout(connectionTimeout);
		Map<String, String> requestParams = new HashMap<String, String>();
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, requestParams);
			if (httpResponse != null
					&& httpResponse.getResponseCode() == 200
					&& httpResponse.getResponse() != null
					&& (httpResponse.getResponse().indexOf("success") != -1 || httpResponse
							.getResponse().indexOf("already") != -1)) {
				// alreadySentRequestForRRBTList.add(bean.getSubscriberID());
				isProcessed = true;
			}
		} catch (HttpException e) {
			logger.debug("Exception occured while activating RRBT "+e);
			logger.error(e);
		} catch (IOException e) {
			logger.debug("Exception occured while deactivating RRBT "+e);
			logger.error(e);
		}
		logger.debug(" callRRBTActivation ends with isProcessed=="+isProcessed);
		return isProcessed;

	}
	/**
	 * Method for deactivating RRBT for grace and suspended user for 
	 * resume and renewal succes callback
	 * 
	 * @param subscriberId
	 * @return
	 */
	
	private boolean callRRBTDeactivation(String subscriberId) {
		
		logger.debug("callRRBTDeactivation starts...");
		boolean isProcessed = false;
		//http://<IP:PORT>/rbt/Subscription.do?action=rrbt_consent_suspension_deactivate&mode=DEACT&subscriberID=%MSISDN%
		String rrbtSupportUrl = RBTParametersUtils.getParamAsString("DAEMON",
			        	"RRBT_DEACTIVATION_URL_FOR_GRACE_SUSPENDED_USER", null);
		if(rrbtSupportUrl == null){
			logger.info("The Parameter RRBT_DEACTIVATION_URL_FOR_GRACE_SUSPENDED_USER is not configured");
			return false; 
		}
		HttpParameters httpParameters =new HttpParameters();
		rrbtSupportUrl = rrbtSupportUrl.replaceAll("%MSISDN%", subscriberId);
		httpParameters.setUrl(rrbtSupportUrl);
		httpParameters.setUseProxy(isUseProxy);
		httpParameters.setProxyHost(proxyHostname);
		httpParameters.setProxyPort(proxyPort);
		httpParameters.setConnectionTimeout(connectionTimeout);
		Map<String,String> requestParams = new HashMap<String,String>();
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, requestParams);
			if (httpResponse != null && httpResponse.getResponseCode() == 200
					&& httpResponse.getResponse() != null
					&&  (httpResponse.getResponse().indexOf("success")!=-1 || httpResponse.getResponse().indexOf("already")!=-1)) {
				//alreadySentRequestForRRBTList.add(bean.getSubscriberID());
				isProcessed = true;
			}
		} catch (HttpException e) {
			logger.debug("Exception occured while deactivating RRBT "+e);
			logger.error(e);
		} catch (IOException e) {
			logger.debug("Exception occured while deactivating RRBT "+e);
			logger.error(e);
		}
		logger.debug("callRRBTDeactivation ends with isProcessed==="+isProcessed);
		return isProcessed;
	
	}

	public static class TestInnerClass {
		static RBTDaemonHelper obj = null;
		
		static {
			obj = RBTDaemonHelper.init();
		}
		public static String testHitOnlineUrl(String msisdn, String serviceKey, String status,
				String mode, String amount, String vcode, String type, String selRefId) {
			return obj.hitOnlineURL(null, null, null, mode, null, null, null, null);
		}
	}
	
	

	public boolean smUpdateSelStatusXbiSubscription(String subscriberID,
			String mappedChargeClass, String circleId) {// RBT-14301: Uninor MNP changes.

		return (m_rbtDBManager.smUpdateSelStatusXbiSubscription( subscriberID,
				 mappedChargeClass,  circleId));
	}
	
	
	private void updatingOperatorUserInfo(Subscriber subscriber, String servicekey){
		//Added for Active B2B subscriber as D2C App user (Free trial user)
		// making status FREE_TRIAL
		try{
			RBTOperatorUserDetailsMappingBean mappingBean = (RBTOperatorUserDetailsMappingBean) ConfigUtil.getBean(BeanConstant.RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN);
				
			if(mappingBean != null) {
				OperatorUserDetailsCallbackServiceImpl callbackServiceImpl = (OperatorUserDetailsCallbackServiceImpl) mappingBean.getCallbackService();
				if(callbackServiceImpl != null) {
					callbackServiceImpl.updateOperatorUserInfo(subscriber.subID(), servicekey, subscriber.subYes(), ConsentPropertyConfigurator.getOperatorFormConfig(), subscriber.circleID());
				}
			}
		}catch(Throwable e){
			logger.error("Bean is not configured: "+e,e);
		}
	}
	
	private boolean updateExtraInfoAdPartner(String subId, String extraInfo) {
		String adPartnerKeys = RBTParametersUtils.getParamAsString("DAEMON",
				"AD_PARTNER_KEY_VALUES", "kp=KIMIA;Token=DMG");
		Set<String> adPartnerKeysLst = new TreeSet<String>();
		Map<String, String> adPartnerKeysMap = MapUtils.convertIntoMap(
				adPartnerKeys, ";", "=", null);
		adPartnerKeysLst = adPartnerKeysMap.keySet();
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		boolean response = false;
		logger.info("subscriberId: " + subId + " adPartnerKeysLst : "
				+ adPartnerKeysLst + " , adPartnerKeysMap:" + adPartnerKeysMap);
		for (String adPartnerKey : adPartnerKeysLst) {
			if (extraInfoMap != null && extraInfoMap.containsKey(adPartnerKey)) {
				extraInfoMap.remove(adPartnerKey);
				extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
				response = m_rbtDBManager.updateExtraInfo(subId, extraInfo);
				break;
			}
		}
		return response;
	}

	private void addTransDataOfAdPartner(HashMap<String, String> extraInfoMap,
			String strSubID) {
		String adPartnerKeys = RBTParametersUtils.getParamAsString("DAEMON",
				"AD_PARTNER_KEY_VALUES", "kp=KIMIA;Token=DMG");
		Set<String> adPartnerKeysLst = new TreeSet<String>();
		Map<String, String> adPartnerKeysMap = MapUtils.convertIntoMap(
				adPartnerKeys, ";", "=", null);
		adPartnerKeysLst = adPartnerKeysMap.keySet();
		logger.info("subscriberId: " + strSubID + " adPartnerKeysLst : "
				+ adPartnerKeysLst + " , adPartnerKeysMap:" + adPartnerKeysMap);
		for (String adPartnerKey : adPartnerKeysLst) {
			if (extraInfoMap != null && extraInfoMap.containsKey(adPartnerKey)) {
				m_rbtDBManager.addTransData(extraInfoMap.remove(adPartnerKey),
						strSubID, adPartnerKeysMap.get(adPartnerKey));
				logger.info("subscriberId: " + strSubID + "key : "
						+ adPartnerKey + ". type: "
						+ adPartnerKeysMap.get(adPartnerKey));
				String extraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);
				boolean updateStatus = RBTDBManager.getInstance()
						.updateExtraInfo(strSubID, extraInfo);
				logger.debug("subscriberId: " + strSubID + "ExtraInfo map : "
						+ extraInfo + ". ExtraInfo update status: "
						+ updateStatus);
				break;
			}
		}
	}
	
	private void updateSmSubscriptionValidityStatus(ProvisioningRequests pack ) {
		String dtocAppClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_FREE_TRIAL_COS_ID", null);
		String cosId = null;
		if(pack == null){
			logger.info("updateSmSubscriptionValidityStatus returning as pack is null");
			return;
		}
		try {
			cosId = String.valueOf(pack.getType());
			logger.info("updateSmSubscriptionValidityStatus returning as invalid type::"+pack.getType());
		} catch (NumberFormatException e) {
			return;
		}
		if (dtocAppClass != null && cosId != null && !cosId.isEmpty()) {
			String[] dtocServiceClassArr = dtocAppClass.split(",");
			if (dtocServiceClassArr.length > 0) {
				List<String> serviceClass = Arrays.asList(dtocServiceClassArr);
				if (serviceClass.contains(cosId)) {
					int smSubStatus = RBTParametersUtils.getParamAsInt("DAEMON", "SM_SUBCRIPTION_ACTIVATION_SUCCESS_STATUS_FOR_PROVISIONING_REQUEST",1);
					if(m_rbtDBManager.updateSmStatusRetryCountAndTime(pack.getSubscriberId(), pack.getTransId(), null, pack.getNextRetryTime(), smSubStatus)){
						logger.info("sm_subcription status updated in provisioning request successfully");
					}else{
						logger.info("sm_subcription status updated in provisioning request falied");
					}
				}
			}
		}
	}
	
	
	private void deactivateSelectionsUnderODAPack(Subscriber subscriber,
            Category category,String refID) {
	logger.info("Going to deactivate Selections under ODA Pack for Subscriber = "+subscriber);
	Map<String, String> whereClauseMap = new HashMap<String, String>();
	whereClauseMap.put("CATEGORY_ID", category.getCategoryId() + "") ;
	List<SubscriberStatus> settings = m_rbtDBManager.getSubscriberActiveSelections(subscriber
			.subID(),whereClauseMap);
	if (settings != null && settings.size() > 0) {
		for (SubscriberStatus setting : settings) {
			String extraInfo = setting.extraInfo();
			HashMap<String,String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			String provRefId = null;
			if(xtraInfoMap!=null && xtraInfoMap.containsKey("PROV_REF_ID")){
				provRefId = xtraInfoMap.get("PROV_REF_ID");
			}
			if (category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE
					&& category.getCategoryId() == setting.categoryID() && provRefId != null
					&& provRefId.equalsIgnoreCase(refID)) { 
				m_rbtDBManager.expireSubscriberSelection(subscriber.subID(), setting.refID(),
						"DAEMON");
			}
		}
	}
}

}