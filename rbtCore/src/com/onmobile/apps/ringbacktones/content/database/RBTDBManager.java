package com.onmobile.apps.ringbacktones.content.database;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMap;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.ConsentHitFailureException;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.BulkActivation;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ChargeClassMap;
import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.DeactivatedSubscribers;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.GCMRegistration;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.Monitoring;
import com.onmobile.apps.ringbacktones.content.OnVoxUser;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.Poll;
import com.onmobile.apps.ringbacktones.content.PromoMaster;
import com.onmobile.apps.ringbacktones.content.PromoTable;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.RBTLogin;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.RBTLotteries;
import com.onmobile.apps.ringbacktones.content.RBTLotteryEntries;
import com.onmobile.apps.ringbacktones.content.RBTLotteryNumber;
import com.onmobile.apps.ringbacktones.content.RDCGroupMembers;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.RbtBulkSelectionTask;
import com.onmobile.apps.ringbacktones.content.RbtTempGroupMembers;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Scratchcard;
import com.onmobile.apps.ringbacktones.content.StatusType;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberCDR;
import com.onmobile.apps.ringbacktones.content.SubscriberCharging;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.UserRights;
import com.onmobile.apps.ringbacktones.content.ViralBlackListTable;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.daemons.reminder.ReminderTool;
import com.onmobile.apps.ringbacktones.features.airtel.UserSelectionRestrictionBasedOnSubClass;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.genericcache.dao.RbtSupportDao;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.SubscriberSelectionImpl;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.Encryption128BitsAES;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking.Status;
import com.onmobile.apps.ringbacktones.v2.dao.impl.RbtNameTuneTrackingDaoImpl;
import com.onmobile.apps.ringbacktones.v2.util.TPTransactionLogger;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RSSFeedScheduler;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSong;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TopLikeSubscriberSong;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocol;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocolDao;
import com.onmobile.apps.ringbacktones.webservice.implementation.vodafoneqatar.DownloadSetTimeComparator;
import com.onmobile.common.cjni.BootStrap;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public class RBTDBManager implements iRBTConstant {
	private static Logger logger = Logger.getLogger(RBTDBManager.class);

	public static String m_connectionError = "Connection Error";
	protected static String m_success = "SUCCESS";
	protected static String m_failure = "FAILURE";
	private String m_dbURL = null;
	private String m_smsdbURL = null;
	private String m_smsdbPoolName = null;
	private int m_nConn = 4;
	private static RBTDBManager dbManager = null;
	private String m_countryCodePrefix = "91";
	private int m_minPhoneNumberLen = 10;
	private int m_minPhoneNumberLenGlobal = 10;
	private int m_maxPhoneNumberLenGlobal = 10;
	private String lotteryDBURL = null;
	private String lotteryDBPoolName = null;

	/** All variables releated to retailer implementation. Tata */
	static HashMap m_retailerHash = null;
	static boolean retailerInRefresh = false;
	private static int retRefreshInterval = 30;
	private static Calendar m_retCal = Calendar.getInstance();

	/** All variables related to trail subscribers. Tata */
	static HashMap m_trailSubsHash = null;
	static boolean trailSubsInRefresh = false;
	static boolean m_isMemCachePlayer = false;

	private static int trailSubsRefreshInterval = 30;
	private static Calendar m_trailSubsCal = Calendar.getInstance();
	public boolean m_trialChangeSubTypeOnSelection = false;
	protected static Object m_obj = new Object();

	private Hashtable m_subClasses = new Hashtable();

	public Hashtable m_subOnlyChargeClass = null;
	public Hashtable m_corporateDiscountChargeClass = null;
	public Hashtable m_modeChargeClass = null;
	public static Date m_endDate = null;
	private String allowLooping = null;
	public String m_comboSubClass = "DEFAULT";
	private String defaultLoopOn = null;
	private String allowedSubClass = null;
	private HashMap _wdsCircleIDMap = null;
	public boolean m_doRetailerCheck = false;

	private List<String> m_activeSubStatus = null;
	private List<String> m_deActiveSubStatus = null;

	private String m_advancePacksList = null;
	private String m_lightPacksList = null;
	private String m_lifeTimePacksList = null;
	private String m_samplingPacksList = null;
	private String m_lowRentalPacksList = null;
	private String m_albumSubClass = null;

	private String m_operatorPrefix = null;
	private String m_redirectNationalURL = null;

	public boolean m_isLTPOnForBaseAct = false;
	public boolean m_isLTPOnForBaseRen = false;
	public boolean m_isLTPOnForSelAct = false;
	public boolean m_isLTPOnForSelRen = false;
	public Hashtable m_ltpActMap = null;
	public Hashtable m_ltpSelMap = null;
	public Hashtable m_ltpAlbumMap = null;

	private String m_tollFreeMMNos = null;
	private String m_baseNumbers = null;
	private String m_callbackBaseNumbers = null;
	private HashMap m_callBackCategoriesMap = null;

	private static String m_smsDBURL = null;
	private String m_smsSenderID = null;
	ArrayList m_TrialWithActivations = new ArrayList();
	Hashtable m_trialClassDaysMap = new Hashtable();
	List<String> m_overrideChargeClasses = null;
	List<String> m_overridableSelectionStatus = new ArrayList<String>();
	List<String> m_overridableCategoryTypes = new ArrayList<String>();
	public List<String> tnbSubscriptionClasses = new ArrayList<String>();
	private int prefixIndex = 0;
	public boolean m_addToDownloads = false;
	public boolean m_retainDownloadsSubDct = false;
	public boolean m_checkForSuspendedSelection = false;
	public boolean m_putSGSInUGS = false;
	public boolean addDownloadToBookmarkOnLowBalance = false;

	private boolean _allowFeedUpgrade = false;
	public String _preCallPrompt = null;
	public static String m_validateMsisdnURL = null;
	public static int m_selectionLimit = 0;
	public static int maxCallerIDSelectionsAllowed = 0;
	public static int maxLoopSelectionPerCallerID = 5;
	public static boolean mobileNumLengthMigration = false;
	public static long[][] migratedNumberRange = null;
	public static HashMap<String, String> migratedPrefixMap = null;
	private static String[] monthsList = { "JAN", "FEB", "MAR", "APR", "MAY",
			"JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };
	protected static HashSet<String> affiliateModeSet = null;
	public HashSet<String> confAzaanCosIdList = null;
	public String azaanDefaultCosId = null;
	private static Map<String, String> confAzaanCopticDoaaCosIdSubTypeMap = null;
	// Idea-Combo DT New Model RBT-14087
	private static Map<String, String> chargeClassMapForAllSubInLoop = null;
	private static Map<String, String> chargeClassMapForSpecialSub = null;
	private static Map<String, String> chargeClassMapForSpecialSubInLoop = null;

	/**
	 * @added Sreekar for Cache implementation
	 */
	private static RBTCacheManager _rcm = null;
	private static com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager rbtCacheManager = null;

	protected List<String> viralSmsTypeListForNewTable = null;

	/**
	 * @added SenthilRaja for TNB change to normal
	 */
	boolean isTNBNewFlow = false;

	boolean allowAllCallerSelectionForSplCaller = false;
	boolean allowAllDaySelForTimeSel = false;
	public HashSet<String> unblockProvisiongRequestModes = null;
	protected static List<String> m_FreemiumUpgradeChargeClass = null;
	protected List<String> freemiumSubClassList = null;
	private static RBTHttpClient rbtHttpClient = null;
	public static List<String> catTypesForAutoRenewal = null;
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public static List<String> cosTypesForMultiPack = null;
	
	public static boolean encryptionModel = false;

	public static boolean isEncryptionModel() {
		return encryptionModel;
	}

	public static void setEncryptionModel(boolean encryptionModel) {
		RBTDBManager.encryptionModel = encryptionModel;
	}

	// RBT-13642
	static {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(RBTParametersUtils.getParamAsInt(
				"COMMON", "BSNL_CG_MAX_NO_CONNECTION", 5));
		httpParameters.setMaxHostConnections(RBTParametersUtils.getParamAsInt(
				"COMMON", "BSNL_CG_MAX_NO_HOST_CONNECTION", 5));
		httpParameters.setConnectionTimeout(RBTParametersUtils.getParamAsInt(
				"COMMON", "BSNL_CG_CONNECTION_TIMEOUT", 6) * 1000);
		httpParameters.setSoTimeout(RBTParametersUtils.getParamAsInt("COMMON",
				"BSNL_CG_CONNECTION_TIMEOUT", 6) * 1000);
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public void initializePrefixes() throws Exception {
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		Parameters daemonParam = parametersCacheManager.getParameter("DAEMON",
				"SUB_ONLY_CHRG_CLASS");
		if (daemonParam != null) {
			m_subOnlyChargeClass = new Hashtable();
			String[] subChargeClasses = daemonParam.getValue().split(";");
			for (String subChargeClass : subChargeClasses) {
				String[] values = subChargeClass.split(",");
				if (values.length > 1)
					m_subOnlyChargeClass.put(values[0], values[1]);
			}
		}

		daemonParam = parametersCacheManager.getParameter("COMMON",
				"COMBO_SUB_CLASS");
		if (daemonParam != null)
			m_comboSubClass = daemonParam.getValue();

		daemonParam = parametersCacheManager.getParameter("COMMON",
				"MODE_CHRG_CLASS");
		if (daemonParam != null) {
			m_modeChargeClass = new Hashtable();
			String[] chargeClasses = daemonParam.getValue().split(";");
			for (String chargeClass : chargeClasses) {
				String[] values = chargeClass.split(",");
				if (values.length > 1)
					m_modeChargeClass.put(values[0], values[1]);
			}
		}

		daemonParam = parametersCacheManager.getParameter("COMMON",
				"CORP_DISCOUNT_CHARGE_CLASS");
		if (daemonParam != null) {
			m_corporateDiscountChargeClass = new Hashtable();
			String[] modeChargeClasses = daemonParam.getValue().split(";");
			for (String modeChargeClass : modeChargeClasses) {
				String[] values = modeChargeClass.split("#");
				if (values.length > 1) {
					String mode = values[0];
					String chargeClasses = values[1];

					HashMap discountedChargeClassMap = new HashMap();
					String[] chargeClassTokens = chargeClasses.split(":");
					for (String chargeClass : chargeClassTokens) {
						String[] tokens = modeChargeClass.split(",");
						if (tokens.length > 1)
							discountedChargeClassMap.put(tokens[0], tokens[1]);
					}

					m_corporateDiscountChargeClass.put(mode,
							discountedChargeClassMap);
				}
			}
		}

		Parameters parameter = parametersCacheManager.getParameter("COMMON",
				"MIN_PHONE_NUMBER_LEN", "10");
		m_minPhoneNumberLen = Integer.parseInt(parameter.getValue());

		parameter = parametersCacheManager.getParameter("GATHERER",
				"PHONE_NUMBER_LENGTH_MIN", "10");
		m_minPhoneNumberLenGlobal = Integer.parseInt(parameter.getValue());

		parameter = parametersCacheManager.getParameter("GATHERER",
				"PHONE_NUMBER_LENGTH_MAX", "10");
		m_maxPhoneNumberLenGlobal = Integer.parseInt(parameter.getValue());

		parameter = parametersCacheManager.getParameter("COMMON",
				"OVERRIDE_CHARGE_CLASSES");
		if (parameter != null && parameter.getValue() != null)
			m_overrideChargeClasses = Arrays.asList(parameter.getValue().trim()
					.toLowerCase().split(","));

		parameter = parametersCacheManager.getParameter("COMMON",
				"OVER_RIDABLE_SELECTION_STATUS");
		if (parameter != null && parameter.getValue() != null)
			m_overridableSelectionStatus = Arrays.asList(parameter.getValue()
					.trim().toLowerCase().split(","));

		parameter = parametersCacheManager.getParameter("COMMON",
				"OVER_RIDABLE_CATEGORY_TYPES");
		if (parameter != null && parameter.getValue() != null)
			m_overridableCategoryTypes = Arrays.asList(parameter.getValue()
					.trim().toLowerCase().split(","));
		else
			m_overridableCategoryTypes.add("0");

		parameter = parametersCacheManager.getParameter("COMMON",
				"TNB_SUBSCRIPTION_CLASSES", "ZERO");
		if (parameter != null && parameter.getValue() != null)
			tnbSubscriptionClasses = Arrays.asList(parameter.getValue().trim()
					.toUpperCase().split(","));
		else
			tnbSubscriptionClasses.add("ZERO");

		parameter = parametersCacheManager.getParameter(COMMON,
				"ALLOW_FEED_UPGRADE", "false");
		_allowFeedUpgrade = parameter.getValue().equalsIgnoreCase("true");

		parameter = parametersCacheManager.getParameter(COMMON,
				"PRE_CALL_PROMPT");
		if (parameter != null && parameter.getValue() != null) {
			_preCallPrompt = parameter.getValue().trim();
			if (!_preCallPrompt.equalsIgnoreCase("0")
					&& !_preCallPrompt.equalsIgnoreCase("1"))
				_preCallPrompt = null;
		}

		parameter = parametersCacheManager.getParameter(COMMON,
				"MAX_ALLOWED_SELECTION", "0");
		m_selectionLimit = Integer.parseInt(parameter.getValue().trim());

		parameter = parametersCacheManager.getParameter(COMMON,
				"MAX_LOOP_CALLERID_SELECTION", "5");
		maxLoopSelectionPerCallerID = Integer.parseInt(parameter.getValue()
				.trim());

		parameter = parametersCacheManager.getParameter(DAEMON,
				ConstantsTools.SUPPORT_TNB_NEW_FLOW, "FALSE");
		isTNBNewFlow = Boolean.parseBoolean(parameter.getValue());

		initTrialWithActivations();

		allowAllCallerSelectionForSplCaller = CacheManagerUtil
				.getParametersCacheManager()
				.getParameter("COMMON", "ALLOW_ALL_CALLER_SEL_FOR_SPL_SEL",
						"FALSE").getValue().equalsIgnoreCase("TRUE");
		allowAllDaySelForTimeSel = CacheManagerUtil
				.getParametersCacheManager()
				.getParameter("COMMON", "ALLOW_ALL_DAY_SEL_TIME_SPEC_SEL",
						"FALSE").getValue().equalsIgnoreCase("TRUE");

	}

	private void initGeneralParams() {
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		Parameters parameter = parametersCacheManager.getParameter("DAEMON",
				"TRIAL_CHANGE_SUB_ON_SEL", "false");
		m_trialChangeSubTypeOnSelection = parameter.getValue()
				.equalsIgnoreCase("true");

		parameter = parametersCacheManager.getParameter("COMMON",
				"CHECK_FOR_SUSPENDED_SEL", "FALSE");
		m_checkForSuspendedSelection = (parameter.getValue().equalsIgnoreCase(
				"TRUE") || parameter.getValue().equalsIgnoreCase("ON"));

		parameter = parametersCacheManager.getParameter("COMMON",
				"PUT_SGS_IN_UGS", "FALSE");
		m_putSGSInUGS = (parameter.getValue().equalsIgnoreCase("TRUE") || parameter
				.getValue().equalsIgnoreCase("ON"));

		parameter = parametersCacheManager.getParameter("COMMON",
				"ADD_DOWNLOAD_TO_BOOKMARK_ON_LOWBAL", "FALSE");
		addDownloadToBookmarkOnLowBalance = (parameter.getValue()
				.equalsIgnoreCase("TRUE") || parameter.getValue()
				.equalsIgnoreCase("ON"));

		parameter = parametersCacheManager.getParameter("COMMON",
				"VALIDATE_MSISDN_URL");
		if (parameter != null && parameter.getValue() != null)
			m_validateMsisdnURL = parameter.getValue().trim();

		String affiliateMode = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(SMS, SMS_AFFILIATED_CONTENT_MODES, null);
		affiliateModeSet = new HashSet<String>(ListUtils.convertToList(
				affiliateMode, ","));

		String cosIds = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(DAEMON, AZAAN_COS_ID_LIST, null);
		confAzaanCosIdList = new HashSet<String>(ListUtils.convertToList(
				cosIds, ","));

		azaanDefaultCosId = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(DAEMON, AZAAN_DEFAULT_COS_ID, null);
		CosDetails azaanCos = CacheManagerUtil.getCosDetailsCacheManager()
				.getCosDetail(azaanDefaultCosId);
		if (azaanCos == null)
			azaanDefaultCosId = null;

		String unblockedModesStr = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(DOUBLE_CONFIRMATION,
						UNBLOCK_PROVISIONING_REQUEST_MODES, null);
		unblockProvisiongRequestModes = new HashSet<String>(
				ListUtils.convertToList(unblockedModesStr, ","));

		Parameters viralSmsTypeForNewTable = CacheManagerUtil
				.getParametersCacheManager().getParameter("VIRAL",
						"VIRAL_SMS_TYPE_FOR_SMS_FLOW", null);
		if (viralSmsTypeForNewTable != null) {
			viralSmsTypeListForNewTable = ListUtils.convertToList(
					viralSmsTypeForNewTable.getValue(), ",");
		}

		String azaanCopticDoaaCosIds = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(COMMON,
						COSID_SUBTYPE_MAPPING_FOR_AZAAN, "");
		confAzaanCopticDoaaCosIdSubTypeMap = MapUtils.convertIntoMap(
				azaanCopticDoaaCosIds, ";", ":", ",");

		String chrgClassNumMaxMappingStr = RBTParametersUtils.getParamAsString(
				"COMMON", "FREEMIUM_CHARGE_CLASSES_NUM_MAX_MAPPING", null);
		m_FreemiumUpgradeChargeClass = ListUtils.convertToList(
				chrgClassNumMaxMappingStr, ",");

		freemiumSubClassList = Arrays.asList(RBTParametersUtils
				.getParamAsString("COMMON", "FREEMIUM_SUB_CLASSES", "").split(
						","));
		logger.info("freemiumSubClassList = " + freemiumSubClassList);
		// Start:Idea-Combo DT New Model RBT-14087
		String chrgClassForAllSubLoop = RBTParametersUtils.getParamAsString(
				"COMMON", "ALL_CALLER_COS_CHARGE_CLASS_MAP_FOR_INLOOP", null);

		String chrgClassForSpecialSub = RBTParametersUtils.getParamAsString(
				"COMMON", "SPECIAL_CALLER_COS_CHARGE_CLASS_MAP", null);

		String chrgClassForSpecialSubLoop = RBTParametersUtils
				.getParamAsString("COMMON",
						"SPECIAL_CALLER_COS_CHARGE_CLASS_MAP_FOR_INLOOP", null);

		chargeClassMapForAllSubInLoop = MapUtils.convertIntoMap(
				chrgClassForAllSubLoop, ";", "=", null);

		logger.info("chargeClassMapForAllSubInLoop = "
				+ chargeClassMapForAllSubInLoop);

		chargeClassMapForSpecialSub = MapUtils.convertIntoMap(
				chrgClassForSpecialSub, ";", "=", null);

		logger.info("chargeClassMapForSpecialSub = "
				+ chargeClassMapForSpecialSub);

		chargeClassMapForSpecialSubInLoop = MapUtils.convertIntoMap(
				chrgClassForSpecialSubLoop, ";", "=", null);

		logger.info("chargeClassMapForSpecialSubInLoop = "
				+ chargeClassMapForSpecialSubInLoop);
		// End:Idea-Combo DT New Model RBT-14087
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		String cosTypesForEnableMultiPack = RBTParametersUtils
				.getParamAsString("COMMON",
						"COS_TYPE_FOR_ALLOWING_MULTIPLE_PACK", null);
		cosTypesForMultiPack = ListUtils.convertToList(
				cosTypesForEnableMultiPack, ",");
	}

	/**
	 * All components except voice can call this.
	 * 
	 * @param dbURL
	 * @param usePool
	 * @param countryPrefix
	 * @throws Exception
	 */
	public void initialize(String dbURL, boolean usePool, int nConn)
			throws Exception {
		m_dbURL = dbURL;
		m_nConn = nConn;
		checkDBURL();
		init();
	}

	public void init() {

	}

	/**
	 * Append/Remove jdbc:sapdb based on usePool
	 * 
	 * @throws Exception
	 */
	private void checkDBURL() throws Exception {
		logger.info("DB_URL for rbt: "+m_dbURL);
		String poolName = ResourceReader
				.getString("rbt", "DB_POOL_NAME", "rbt");
		String providerClass = ResourceReader.getString("rbt", "DB_PPROVIDER",
				null);

		String timeOut = ResourceReader.getString("rbt", "DB_TIME_OUT", "120");

		initOzoneConnectionPool(m_dbURL, m_nConn, poolName, true, timeOut,
				providerClass);

		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "COUNTRY_PREFIX", "91");
		m_countryCodePrefix = parameter.getValue();

		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(
				"COMMON", "SMS_DB_URL");
		if (parameter != null)
			m_smsdbURL = parameter.getValue();

		lotteryDBURL = ResourceReader.getString("rbt", "LOTTERY_DB_URL", null);

		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(
				"COMMON", "MAX_SPECIAL_SETTINGS", "0");
		maxCallerIDSelectionsAllowed = Integer.parseInt(parameter.getValue());

		parameter = CacheManagerUtil.getParametersCacheManager().getParameter(
				"COMMON", "MOBILE_NUM_LENGTH_MIGRATION", "FALSE");
		if (parameter.getValue() != null) {
			mobileNumLengthMigration = parameter.getValue().trim()
					.equalsIgnoreCase("TRUE");
			if (mobileNumLengthMigration)
				initializeMigratedPrefixes();
		}

		// cache full site prefixes
		initializePrefixes();

		Calendar endCal = Calendar.getInstance();
		endCal.clear();
		endCal.set(2037, 0, 1);
		m_endDate = endCal.getTime();

		m_doRetailerCheck = doRetailerCheck();
		m_isMemCachePlayer = isMemCachePlayer();

		m_activeSubStatus = new ArrayList<String>();
		m_activeSubStatus.add(STATE_TO_BE_ACTIVATED);// (STATE_ACTIVATION_REQUEST_TO_SEND);
		m_activeSubStatus.add(STATE_ACTIVATION_PENDING); // (STATE_ACTIVATION_CALLBACK_AWAITED);
		m_activeSubStatus.add(STATE_ACTIVATED); // (STATE_ACTIVE);
		m_activeSubStatus.add(STATE_ACTIVATION_ERROR);
		m_activeSubStatus.add(STATE_CHANGE); // (STATE_CHANGE_REQUEST_TO_SEND);
		m_activeSubStatus.add(STATE_ACTIVATION_GRACE); // (STATE_ACTIVATION_GRACE);
		m_activeSubStatus.add(STATE_SUSPENDED);
		m_activeSubStatus.add(STATE_SUSPENDED_INIT);
		m_activeSubStatus.add(STATE_EVENT);

		m_deActiveSubStatus = new ArrayList<String>();
		m_deActiveSubStatus.add(STATE_TO_BE_DEACTIVATED); // (STATE_DEACTIVATION_REQUEST_TO_SEND);
		m_deActiveSubStatus.add(STATE_DEACTIVATION_PENDING); // (STATE_DEACTIVATION_CALLBACK_AWAITED);
		m_deActiveSubStatus.add(STATE_DEACTIVATED); // (STATE_DEACTIVE);
		m_deActiveSubStatus.add(STATE_DEACTIVATION_ERROR);
		m_deActiveSubStatus.add(STATE_DEACTIVATED_INIT);

		// LoyaltyPoints Initialization
		initializeLTP();
		initNationalUrl();

		// Added by Sreekar
		initGeneralParams();
	}

	private void initializeMigratedPrefixes() {
		try {
			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter("COMMON", "MIGRATED_NUMBER_RANGE", null);
			if (parameter.getValue() != null) {
				String parameterStr = parameter.getValue().trim();
				StringTokenizer stk = new StringTokenizer(parameterStr, ";");
				int countTokens = stk.countTokens();
				migratedNumberRange = new long[countTokens][2];
				int count = 0;
				while (stk.hasMoreTokens()) {
					StringTokenizer stk1 = new StringTokenizer(stk.nextToken(),
							",");
					if (stk1.countTokens() != 2)
						continue;
					String num1 = stk1.nextToken();
					String num2 = stk1.nextToken();
					long numLong1 = Long.parseLong(num1);
					long numLong2 = Long.parseLong(num2);
					migratedNumberRange[count][0] = numLong1;
					migratedNumberRange[count][1] = numLong2;
					count++;
				}
			}
			parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter("COMMON", "MIGRATED_PREFIX_MAP", null);
			if (parameter.getValue() != null) {
				String parameterStr = parameter.getValue().trim();
				StringTokenizer stk = new StringTokenizer(parameterStr, ";");
				int countTokens = stk.countTokens();
				migratedPrefixMap = new HashMap<String, String>();
				while (stk.hasMoreTokens()) {
					StringTokenizer stk1 = new StringTokenizer(stk.nextToken(),
							",");
					if (stk1.countTokens() != 2)
						continue;
					String num1 = stk1.nextToken();
					String num2 = stk1.nextToken();
					migratedPrefixMap.put(num1, num2);
				}
			}
		} catch (Exception e) {
			logger.error(
					"Exception initializing 9 to 10 digit migrated number list ",
					e);
		}
		logger.info("migratedNumberRange=" + migratedNumberRange
				+ ", migratedPrefixMap=" + migratedPrefixMap);
	}

	public static synchronized RBTDBManager init(String dbURL, int nConn) {
		if (dbManager == null && dbURL != null) {
			try {
				String rbtDBImpl = ResourceReader.getString("rbt", "DB_IMPL",
						null);
				if (rbtDBImpl == null) {
					dbManager = new RBTDBManager();
				} else {
					Class implClass = Class.forName(rbtDBImpl);
					dbManager = (RBTDBManager) implClass.newInstance();

				}
				dbManager.initialize(dbURL, true, nConn);
				SubscriberStatusImpl.setRRBT(dbManager.isRRBTOn());
				_rcm = RBTCacheManager.getInstance();

				if (!_rcm.isCategoryCacheInitialized()
						|| !_rcm.isClipCacheInitialized())
					rbtCacheManager = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
							.getInstance();
				logger.info("rbtCacheManager " + rbtCacheManager);
			} catch (Exception e) {
				logger.error("", e);
				dbManager = null;
			}
		}
		return dbManager;
	}

	private static void initDBManager() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		String m_dbURL = resourceBundle.getString("DB_URL");
		// Changes done for URL Encryption and Decryption
		int poolSize = 4;
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				m_dbURL = URLEncryptDecryptUtil.decryptAndMerge(m_dbURL);
				setEncryptionModel(true);
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		try {
			String poolSizeStr = resourceBundle.getString("DB_POOL_SIZE");
			// End of URL Encryption and Decryption
			poolSize = Integer.parseInt(poolSizeStr);
		} catch (NumberFormatException e) {
			poolSize = 4;
		}
		dbManager = init(m_dbURL, poolSize);
	}

	public static RBTDBManager getInstance() {
		if (dbManager == null) {
			synchronized (RBTDBManager.class) {
				if (dbManager == null) {
					initDBManager();
				}
			}
			if (dbManager == null) {
				logger.fatal("Could not initialize the DBManager properly ");
			}
		}
		return dbManager;
	}

	public Connection getConnection() {

		try {
			return OnMobileDBServices.getDBConnection();
		} catch (Throwable e) {
			logger.error("Exception while getting connection", e);
		}
		return null;
	}

	/**
	 * This API can be used for standalone Daemons and SMS sending code. Call
	 * init(dbURL,numConnection) of this class before calling this API. Ensure
	 * init is called with minimal number of connections say 2 if you dont plan
	 * to sue those conenctions. This can return null value if ozone connection
	 * pool is not initialized.
	 * 
	 * @param strDBUrl
	 *            DB Connection String
	 * @param iPoolSize
	 *            Pool size
	 * @return
	 */
	public Connection getSMSConnection() {
		if (m_smsdbPoolName == null) {
			m_smsdbPoolName = RBTParametersUtils.getParamAsString(COMMON,
					"SMS_DB_POOL_NAME", null);
			String timeOut = RBTParametersUtils.getParamAsString(COMMON,
					"SMS_DB_TIME_OUT", "120");
			logger.info("Initializing the SMS DB pool, m_smsdbPoolName: "
					+ m_smsdbPoolName + " timeOut: " + timeOut);
			initOzoneConnectionPool(m_smsdbURL, 4, m_smsdbPoolName, false,
					timeOut, null);
		}
		try {
			return OnMobileDBServices.getDBConnection(m_smsdbPoolName);
		} catch (Throwable e) {
			logger.error("Exception while getting connection", e);
		}
		return null;
	}

	public Connection getLotteryDBConnection() {
		if (lotteryDBPoolName == null) {
			lotteryDBPoolName = ResourceReader.getString("rbt",
					"LOTTERY_DB_POOL_NAME", "lottery");
			String timeOut = ResourceReader.getString("rbt",
					"LOTTERY_DB_TIME_OUT", "120");
			int nConn = Integer.parseInt(ResourceReader.getString("rbt",
					"LOTTERY_DB_N_CONNECTIONS", "4"));
			logger.info("Initializing the lotteryDB pool, lotteryDBPoolName: "
					+ lotteryDBPoolName + " timeOut: " + timeOut);
			initOzoneConnectionPool(lotteryDBURL, nConn, lotteryDBPoolName,
					false, timeOut, null);
		}
		try {
			return OnMobileDBServices.getDBConnection(lotteryDBPoolName);
		} catch (Throwable e) {
			logger.error("Exception while getting connection", e);
		}
		return null;
	}

	public boolean releaseConnection(Connection conn) {
		try {
			OnMobileDBServices.releaseConnection(conn);
			return true;
		} catch (Exception e) {
			logger.error("Exception while releasing connection", e);
		}
		return false;
	}

	public static String getLanguageString(String langCode) {
		String langConfig = RBTParametersUtils.getParamAsString("ALL",
				"LANGUAGE_CODE_TO_LANGUAGE_MAPPING", "eng=ENGLISH");
		Map<String, String> languageMapping = MapUtils.convertToMap(langConfig,
				":", "=", null);
		String language = "ENGLISH";
		if (languageMapping != null && !languageMapping.isEmpty()) {
			language = languageMapping.get(langCode);
		}
		return (language != null && !language.isEmpty()) ? language : "ENGLISH";
	}

	/* All methods related to categories */
	/**
	 * Use this for copying a category from an existing site. It wont use the
	 * auto generated id.Uses exixting ID.
	 * 
	 * @param categoryID
	 * @param name
	 * @param nameFile
	 * @param previewFile
	 * @param grammar
	 * @param type
	 * @param index
	 * @param askMobileNumber
	 * @param greeting
	 * @param startTime
	 * @param endTime
	 * @param parentID
	 * @param classType
	 * @param promoID
	 * @param circleID
	 * @param prepaidYes
	 * @param alias
	 * @return
	 */
	public Categories createCategoryWithId(int categoryID, String name,
			String nameFile, String previewFile, String grammar, int type,
			int index, String askMobileNumber, String greeting, Date startTime,
			Date endTime, int parentID, String classType, String promoID,
			String circleID, char prepaidYes, String alias, String mmNumber,
			String language) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		Categories categories = null;
		try {
			categories = getCategory(categoryID, circleID, prepaidYes);
			if (categories == null)
				categories = CategoriesImpl.insertWithId(conn, categoryID,
						name, nameFile, previewFile, grammar, type, index,
						askMobileNumber, greeting, startTime, endTime,
						parentID, classType, promoID, circleID, prepaidYes,
						alias, mmNumber, language);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return categories;
	}

	public boolean removeCategory(Categories categories) {
		if (categories == null)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			success = CategoriesImpl.remove(conn, categories.id(),
					categories.circleID(), categories.prepaidYes());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public SubscriberStatus getSelection(String subscriberId, String callerId,
			String subWavFile, Date setTime) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getSelection(conn,
					subID(subscriberId), subID(callerId), subWavFile, setTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public SubscriberStatus getSelection(String subscriberId, String subWavFile) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getSelection(conn,
					subID(subscriberId), subWavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public SubscriberStatus[] getSubscriberSelectionsNotDeactivated(
			String subscriberID, String subWavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus[] subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl
					.getSubscriberSelectionsNotDeactivated(conn,
							subID(subscriberID), subWavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;

	}

	public SubscriberStatus getSelectionByRefId(String subscriberId,
			String refId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getSelectionByRefId(conn,
					subID(subscriberId), refId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public SubscriberStatus getSelectionBySubIdRefId(String subscriberId,
			String refId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getSelectionBySubIdRefId(conn,
					subID(subscriberId), refId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}
	
	public boolean updateSelectionType(String subscriberId, String callerId,
			String subWavFile, Date setTime, Date endDate, int tStatus,
			int fromTime, int toTime, String selInterval, char loopStatus) {

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			success = SubscriberStatusImpl
					.updateSelectionType(conn, subID(subscriberId),
							subID(callerId), subWavFile, setTime, endDate,
							tStatus, fromTime, toTime, selInterval, loopStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public Categories[] getAllCategoriesForCircle(String circleID) {
		return _rcm.getAllCategoriesForCircle(circleID, true);
	}

	public Categories[] getAllCategoriesForCircle(String circleID, boolean disp) {
		return _rcm.getAllCategoriesForCircle(circleID, disp);
	}

	public boolean isCacheInitialized() {
		return _rcm.isCacheInitialized();
	}

	/**
	 * Get all categories without time check
	 * 
	 * @param circleID
	 * @param prepaidYes
	 * @return
	 */
	public Categories[] getAllCategories(String circleID, char prepaidYes) {
		return _rcm.getAllCategories(circleID, prepaidYes);
	}

	public Categories[] getAllCategories() {
		return _rcm.getAllCategories();
	}

	/**
	 * Get all circles with time check
	 * 
	 * @param circleID
	 * @param prepaidYes
	 * @return
	 */
	public Categories[] getActiveCategories(String circleID, char prepaidYes) {
		return getActiveCategories(circleID, prepaidYes, null);
	}

	public Categories[] getActiveCategories(String circleID, char prepaidYes,
			String language) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getActiveCategories(circleID, prepaidYes, language);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category[] tmpCategories = rbtCacheManager
					.getActiveCategoriesInCircle(circleID, 0, prepaidYes,
							language);
			if (tmpCategories == null)
				return null;

			Categories[] categories = new Categories[tmpCategories.length];
			for (int i = 0; i < tmpCategories.length; i++) {
				categories[i] = CategoriesImpl.getCategory(tmpCategories[i]);
			}
			return categories;
		}
	}

	public Categories[] getActiveCategoriesbyCircleID() {
		return _rcm.getActiveCategoriesbyCircleId();
	}

	public Categories[] getSubCategories(int parentID, String circleID,
			char prepaidYes) {
		return getSubCategories(parentID, circleID, prepaidYes, null);
	}

	public Categories[] getSubCategories(int parentID, String circleID,
			char prepaidYes, String language) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getSubCategories(circleID, prepaidYes, parentID,
					language);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category[] tmpCategories = rbtCacheManager
					.getActiveCategoriesInCircle(circleID, parentID,
							prepaidYes, language);
			if (tmpCategories == null)
				return null;

			Categories[] categories = new Categories[tmpCategories.length];
			for (int i = 0; i < tmpCategories.length; i++) {
				categories[i] = CategoriesImpl.getCategory(tmpCategories[i]);
			}
			return categories;
		}
	}

	public Categories[] getGUISubCategories(int parentID, String circleID,
			char prepaidYes) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getGUISubCategories(circleID, prepaidYes, parentID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category[] tmpCategories = rbtCacheManager
					.getCategoriesInCircle(circleID, parentID, prepaidYes);
			if (tmpCategories == null)
				return null;

			Categories[] categories = new Categories[tmpCategories.length];
			for (int i = 0; i < tmpCategories.length; i++) {
				categories[i] = CategoriesImpl.getCategory(tmpCategories[i]);
			}
			return categories;
		}
	}

	public Categories[] getActiveBouquet(int parentID, String circleID,
			char prepaidYes) {
		return getActiveBouquet(parentID, circleID, prepaidYes, null);
	}

	public Categories[] getActiveBouquet(int parentID, String circleID,
			char prepaidYes, String language) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getActiveBouquet(parentID, circleID, prepaidYes,
					language);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category[] tmpCategories = rbtCacheManager
					.getActiveCategoriesInCircle(circleID, parentID,
							prepaidYes, language);
			if (tmpCategories == null)
				return null;

			Categories[] categories = new Categories[tmpCategories.length];
			for (int i = 0; i < tmpCategories.length; i++) {
				categories[i] = CategoriesImpl.getCategory(tmpCategories[i]);
			}
			return categories;
		}
	}

	public Categories[] getBouquet(String circleID, char prepaidYes) {
		return _rcm.getBouquet(circleID, prepaidYes);
	}

	public Categories getCategoryPromoID(String promoID, String circleId,
			char prepaidYes) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategoryPromoID(promoID, circleId, prepaidYes);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategoryByPromoId(promoID);
			if (tmpCategory == null)
				return null;

			Categories category = CategoriesImpl.getCategory(tmpCategory);
			return category;
		}
	}

	public Category getCategoryPromoID(String promoID) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategoryPromoID(promoID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategoryByPromoId(promoID);
			if (tmpCategory == null)
				return null;

			Category category = new Category(tmpCategory.getCategoryId(),
					tmpCategory.getCategoryName(),
					tmpCategory.getCategoryNameWavFile(),
					tmpCategory.getCategoryPreviewWavFile(),
					tmpCategory.getCategoryGrammar(),
					tmpCategory.getCategoryTpe(),
					tmpCategory.getCategoryAskMobileNumber(),
					tmpCategory.getCategoryGreeting(),
					tmpCategory.getCategoryStartTime(),
					tmpCategory.getCategoryEndTime(),
					tmpCategory.getClassType(),
					tmpCategory.getCategoryPromoId(),
					tmpCategory.getCategorySmsAlias(),
					tmpCategory.getMmNumber(), null);
			return category;
		}
	}

	public Categories getCategoryMMNumber(String mmNumber, String circleId,
			char prepaidYes) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategoryMMNumber(mmNumber, circleId, prepaidYes);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategoryByMmNumber(mmNumber);
			if (tmpCategory == null)
				return null;

			Categories category = CategoriesImpl.getCategory(tmpCategory);
			return category;
		}
	}

	public Categories getCategory(int categoryID, String circleID,
			char prepaidYes) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategory(categoryID, circleID, prepaidYes);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategory(categoryID);
			if (tmpCategory == null)
				return null;

			Categories category = CategoriesImpl.getCategory(tmpCategory);
			return category;
		}
	}

	public Categories getCategoryAlias(String alias, String circleId,
			char prepaidYes) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategoryAlias(alias, circleId, prepaidYes);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategoryBySMSAlias(alias);
			if (tmpCategory == null)
				return null;

			Categories category = CategoriesImpl.getCategory(tmpCategory);
			return category;
		}
	}

	/**
	 * This method should be used only by daemons, no UI componet should use
	 * this
	 * 
	 * @param categoryID
	 * @return
	 */
	public Category getCategory(int categoryID) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategory(categoryID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategory(categoryID);
			if (tmpCategory == null)
				return null;

			Category category = new Category(tmpCategory.getCategoryId(),
					tmpCategory.getCategoryName(),
					tmpCategory.getCategoryNameWavFile(),
					tmpCategory.getCategoryPreviewWavFile(),
					tmpCategory.getCategoryGrammar(),
					tmpCategory.getCategoryTpe(),
					tmpCategory.getCategoryAskMobileNumber(),
					tmpCategory.getCategoryGreeting(),
					tmpCategory.getCategoryStartTime(),
					tmpCategory.getCategoryEndTime(),
					tmpCategory.getClassType(),
					tmpCategory.getCategoryPromoId(),
					tmpCategory.getCategorySmsAlias(),
					tmpCategory.getMmNumber(), null);
			return category;
		}
	}

	public Category getCategoryByName(String name) {
		if (_rcm.isCategoryCacheInitialized())
			return _rcm.getCategoryByName(name);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category tmpCategory = rbtCacheManager
					.getCategoryByName(name);
			if (tmpCategory == null)
				return null;

			Category category = new Category(tmpCategory.getCategoryId(),
					tmpCategory.getCategoryName(),
					tmpCategory.getCategoryNameWavFile(),
					tmpCategory.getCategoryPreviewWavFile(),
					tmpCategory.getCategoryGrammar(),
					tmpCategory.getCategoryTpe(),
					tmpCategory.getCategoryAskMobileNumber(),
					tmpCategory.getCategoryGreeting(),
					tmpCategory.getCategoryStartTime(),
					tmpCategory.getCategoryEndTime(),
					tmpCategory.getClassType(),
					tmpCategory.getCategoryPromoId(),
					tmpCategory.getCategorySmsAlias(),
					tmpCategory.getMmNumber(), null);
			return category;
		}
	}

	public ArrayList getAllShuffleCategoryIDs() {
		return _rcm.getShuffleCategoryIDs(false);
	}

	public ArrayList getActiveShuffleCategoryIDs() {
		return _rcm.getShuffleCategoryIDs(true);
	}

	// For Tata
	public Clips createClip(String name, String nameFile, String previewFile,
			String wavFile, String grammar, String alias, String addAccess,
			String promoID, String classType, Date startTime, Date endTime,
			Date smsTime, String album, String clipDemoWavFile, String artist) {
		return createClip(name, nameFile, previewFile, wavFile, grammar, alias,
				addAccess, promoID, classType, startTime, endTime, smsTime,
				album, null, clipDemoWavFile, artist);
	}

	public Clips createClip(String name, String nameFile, String previewFile,
			String wavFile, String grammar, String alias, String addAccess,
			String promoID, String classType, Date startTime, Date endTime,
			Date smsTime, String album, String language, String demoFile,
			String artist) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Clips clips = null;
		try {
			clips = ClipsImpl.insert(conn, name, nameFile, previewFile,
					wavFile, grammar, alias, addAccess, promoID, classType,
					startTime, endTime, smsTime, album, language, demoFile,
					artist, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return clips;
	}

	// For Tata
	public ClipMinimal createClipWithID(int clipID, String name,
			String nameFile, String previewFile, String wavFile,
			String grammar, String alias, String addAccess, String promoID,
			String classType, Date startTime, Date endTime, Date smsTime,
			String album, String clipDemoWavFile, String artist, String clipInfo) {
		return createClipWithID(clipID, name, nameFile, previewFile, wavFile,
				grammar, alias, addAccess, promoID, classType, startTime,
				endTime, smsTime, album, null, clipDemoWavFile, artist,
				clipInfo);
	}

	public ClipMinimal createClipWithID(int clipID, String name,
			String nameFile, String previewFile, String wavFile,
			String grammar, String alias, String addAccess, String promoID,
			String classType, Date startTime, Date endTime, Date smsTime,
			String album, String language, String clipDemoWavFile,
			String artist, String clipInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		ClipMinimal clips = null;
		try {
			clips = ClipsImpl.insertWithID(conn, clipID, name, nameFile,
					previewFile, wavFile, grammar, alias, addAccess, promoID,
					classType, startTime, endTime, smsTime, album, language,
					clipDemoWavFile, artist, clipInfo);
			if (clips.getPromoID() != null)
				PromoMasterImpl.insertWithSequence(conn, clips.getPromoID(), ""
						+ clips.getClipId());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return clips;
	}

	public int[] getClipIDsInCategory(int catID) {
		return _rcm.getClipIDsInCategory(catID);
	}

	public int getCatIDsForClipId(int clipID) {
		return _rcm.getCatIDsForClipId(clipID);
	}

	public boolean removeCategoryClips(int categoryID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			success = ClipsImpl.removeCategoryClips(conn, categoryID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	// For Tata
	public ClipMinimal updateClip(int clipID, String name, String nameFile,
			String previewFile, String wavFile, String grammar, String alias,
			String addAccess, String promoID, String classType, Date startTime,
			Date endTime, Date smsTime, String album, String clipDemoWavFile,
			String artist, String clipInfo) {
		return updateClip(clipID, name, nameFile, previewFile, wavFile,
				grammar, alias, addAccess, promoID, classType, startTime,
				endTime, smsTime, album, null, clipDemoWavFile, artist,
				clipInfo);
	}

	public ClipMinimal updateClip(int clipID, String name, String nameFile,
			String previewFile, String wavFile, String grammar, String alias,
			String addAccess, String promoID, String classType, Date startTime,
			Date endTime, Date smsTime, String album, String language,
			String demoFile, String artist, String clipInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		ClipMinimal clips = null;
		try {
			ClipsImpl.update(conn, clipID, name, nameFile, previewFile,
					wavFile, grammar, alias, addAccess, promoID, classType,
					startTime, endTime, smsTime, album, language, demoFile,
					artist, clipInfo);
			clips = new ClipMinimal(clipID, promoID, name, wavFile, nameFile,
					previewFile, demoFile, grammar, classType, smsTime,
					endTime, album, language, artist, alias, clipInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return clips;
	}

	public ClipMinimal getClipMinimal(int clipID, boolean checkMap) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClip(clipID, checkMap);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClip(clipID);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public ClipMinimal getClipMinimalPromoID(String promoID, boolean checkMap) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClip(promoID, checkMap);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClipByPromoId(promoID);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public SortedMap getSMSPromoClips() {
		return _rcm.getSMSPromoClips();
	}

	public boolean convertSubscriptionType(String subscriberID,
			String initType, String finalType, Subscriber subscriber) {
		return convertSubscriptionType(subscriberID, initType, finalType, null,
				0, false, null, subscriber);
	}

	public boolean convertSubscriptionType(String subscriberID,
			String initType, String finalType, String strActBy, int rbtType,
			boolean useRbtType, String extraInfo, Subscriber subscriber) {
		return convertSubscriptionType(subscriberID, initType, finalType,
				strActBy, null, true, rbtType, useRbtType, extraInfo,
				subscriber);
	}

	public boolean checkUserStatuskForConsentFlow(Subscriber sub) {
		return true;
	}

	public boolean convertSubscriptionType(String subscriberID,
			String initType, String finalType, String strActBy,
			String strActInfo, boolean concatActInfo, int rbtType,
			boolean useRbtType, String extraInfo, Subscriber subscriber) {
		/*
		 * This parameter is added for Uninor for the ELOAD subscription update
		 * feature.
		 */
		if (null == finalType) {
			logger.warn("Returning false, new subscriptionClass is null.");
			return false;
		}

		String allowSubscriptionUpdateParam = CacheManagerUtil
				.getParametersCacheManager().getParameterValue("DAEMON",
						"ALLOW_SAME_SUBSCRIPTION_CLASS_UPDATE", "FALSE");
		boolean allowSubscriptionUpdate = Boolean
				.valueOf(allowSubscriptionUpdateParam);

		logger.debug("Updating subscription class. subscriptionClass: "
				+ initType + ", new subscriptionClass: " + finalType
				+ ", allowSubscriptionUpdate: " + allowSubscriptionUpdate);

		if (initType != null && finalType != null && finalType.equals(initType)
				&& !allowSubscriptionUpdate) {
			logger.debug("Returning false. Not allowing to update same subscription class ");
			return false;
		}

		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean success = false;
		try {
			success = SubscriberImpl.convertSubscriptionType(conn,
					subID(subscriberID), initType, finalType, strActBy,
					strActInfo, concatActInfo, rbtType, useRbtType, extraInfo,
					subscriber);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Returning response: " + success + ", subscriberId: "
				+ subscriberID);
		return success;
	}

	public boolean convertSubscriptionTypeAndEndDate(String subscriberID,
			String initType, String finalType, String strActBy,
			String extraInfo, String endDate) {
		/*
		 * This parameter is added for Uninor for the ELOAD subscription update
		 * feature.
		 */
		String allowSubscriptionUpdateParam = CacheManagerUtil
				.getParametersCacheManager().getParameterValue("DAEMON",
						"ALLOW_SAME_SUBSCRIPTION_CLASS_UPDATE", "FALSE");
		boolean allowSubscriptionUpdate = Boolean
				.valueOf(allowSubscriptionUpdateParam);

		logger.debug(" initType: " + initType + ", finalType: " + finalType
				+ ", allowSubscriptionUpdate: " + allowSubscriptionUpdate);

		if (initType != null && finalType != null && finalType.equals(initType)
				&& !allowSubscriptionUpdate) {
			logger.debug(" Returning false. Not allowing to update same subscription class ");
			return false;
		}

		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean success = false;
		try {
			success = SubscriberImpl.convertSubscriptionTypeAndEndDate(conn,
					subID(subscriberID), initType, finalType, strActBy,
					extraInfo, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public boolean convertSubscriptionTypeConsentUpgrde(String subscriberID,
			String activate, Date startDate, Date endDate,
			String activationInfo, boolean prepaid, String cosID, int rbtType,
			String extraInfo, String circleID, String refID,
			String consentStatus, String oldSubscriptionClass,
			String newSubscriptionClass) {
		/*
		 * This parameter is added for Uninor for the ELOAD subscription update
		 * feature.
		 */
		String allowSubscriptionUpdateParam = CacheManagerUtil
				.getParametersCacheManager().getParameterValue("DAEMON",
						"ALLOW_SAME_SUBSCRIPTION_CLASS_UPDATE", "FALSE");
		boolean allowSubscriptionUpdate = Boolean
				.valueOf(allowSubscriptionUpdateParam);

		logger.debug(" initType: " + oldSubscriptionClass + ", finalType: "
				+ newSubscriptionClass + ", allowSubscriptionUpdate: "
				+ allowSubscriptionUpdate);

		if (oldSubscriptionClass != null && newSubscriptionClass != null
				&& newSubscriptionClass.equals(oldSubscriptionClass)
				&& !allowSubscriptionUpdate) {
			logger.debug(" Returning false. Not allowing to update same subscription class ");
			return false;
		}
		String prepaidYes = prepaid ? "y" : "n";
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean success = false;
		try {
			Date requestTime = null;
			int secondsToBeAddedInRequestTime = DBUtility
					.secondsToBeAddedInRequestTime(circleID, activate);
			if (secondsToBeAddedInRequestTime != -1) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, secondsToBeAddedInRequestTime);
				requestTime = cal.getTime();
			}
			success = ConsentTableImpl.convertSubscriptionTypeConsentUpgrde(
					conn, subscriberID, activate, startDate, endDate,
					activationInfo, prepaidYes, newSubscriptionClass, cosID,
					rbtType, extraInfo, circleID, refID, consentStatus,
					requestTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public String smSelectionGrace(String strSubID, String refID, String type,
			int rbtType, char newStatus, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionGrace(conn,
					subID(strSubID), refID, type, rbtType, newStatus, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean convertSubscriptionTypeTrial(String subscriberID,
			String initType, String finalType, Subscriber subscriber) {
		return convertSubscriptionTypeTrial(subID(subscriberID), initType,
				finalType, 0, false, subscriber);
	}

	public boolean convertSubscriptionTypeTrial(String subscriberID,
			String initType, String finalType, int rbtType, boolean useRBTType,
			Subscriber subscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean success = false;
		try {
			success = SubscriberImpl.convertSubscriptionType(conn,
					subID(subscriberID), initType, finalType, null, null, true,
					rbtType, useRBTType, null, subscriber);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public String deactivateSubscriberRecordsByRefId(String subscriberID,
			String deactBy, String refId) {
		return deactivateSubscriberRecordsByRefId(subscriberID, deactBy, refId,
				null, null, null, null);
	}

	public String deactivateSubscriberRecordsByRefId(String subscriberID,
			String deactBy, String refId, String newExtraInfo, String newRefId,
			Character newLoopStatus, String selStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.deactivateSubscriberRecordsByRefId(
					conn, subID(subscriberID), deactBy, refId, newExtraInfo,
					newRefId, newLoopStatus, selStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/**
	 * This API coverts back an SUBSCRIBER as B if users state was C and SM call
	 * back failed.
	 * 
	 * @param oldActBy
	 *            TODO
	 */
	public String updateUpgradeFailure(String subscriberID, String actBy,
			String oldSub, int rbtType, boolean updateRbtType,
			String newStatus, String extraInfo, String oldActBy,
			Object beanActive) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		try {
			if (newStatus == null)
				newStatus = "B";

			success = SubscriberImpl.updateUpgradeFailure(conn,
					subID(subscriberID), actBy, oldSub, rbtType, updateRbtType,
					newStatus, extraInfo, oldActBy);
			if (success
					&& (actBy != null && actBy.equals("TNB") && oldSub != null && oldSub
							.equals("ZERO")) || beanActive != null)
				SubscriberStatusImpl.deactivateSubscriberRecordsTrial(conn,
						subID(subscriberID), rbtType, beanActive);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean updateGiftCharge(String subscriberID, String callerID,
			String clipID, String setTime, String status, String selectedBy,
			String extraInfo, String circleIDFromPrism) {// RBT-14301: Uninor
															// MNP changes.
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			String smsType = "GIFTCHRGPENDING";
			if (viralSmsTypeListForNewTable != null
					&& viralSmsTypeListForNewTable.contains(smsType)) {
				success = ViralSMSNewImpl.updateGiftCharge(conn,
						subID(subscriberID), callerID, clipID, setTime, status,
						selectedBy, extraInfo, circleIDFromPrism);// RBT-14301:
																	// Uninor
																	// MNP
																	// changes.

			} else {
				success = ViralSMSTableImpl.updateGiftCharge(conn,
						subID(subscriberID), callerID, clipID, setTime, status,
						selectedBy, extraInfo, circleIDFromPrism);// RBT-14301:
																	// Uninor
																	// MNP
																	// changes.
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public String updateSubscriberId(String newSubscriberId, String subscriberId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			Subscriber sub = getSubscriber(subID(newSubscriberId));
			if (sub != null)
				return "FAILURE:NEW MSISDN ALREADY EXISTS";
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if (subscriber == null)
				return "FAILURE:MSISDN DOESN'T EXIST";
			if (!isValidPrefix(newSubscriberId))
				return "FAILURE:NEW MSISDN INVALID";

			boolean success = SubscriberImpl.updateSubscriberId(conn,
					subID(newSubscriberId), subID(subscriberId));
			SubscriberStatusImpl.updateSubscriberId(conn,
					subID(newSubscriberId), subID(subscriberId));
			List<String> smsTypeList = Arrays
					.asList("GIFT,GIFTED,GIFT_CHARGED,ACCEPT_ACK,REJECT_ACK,ACCEPT_PRE,COPY,COPYCONFIRM,COPYCONFIRMED,COPYCONFPENDING,COPYSTAR"
							.split(","));
			if (isViralSmsTypeListForNewTable(smsTypeList)) {
				ViralSMSNewImpl.updateSubscriberId(conn,
						subID(newSubscriberId), subID(subscriberId));
			} else {
				ViralSMSTableImpl.updateSubscriberId(conn,
						subID(newSubscriberId), subID(subscriberId));
			}
			GroupsImpl.updateSubscriberId(conn, subID(newSubscriberId),
					subID(subscriberId));
			ProvisioningRequestsDao.updateSubscriberID(conn,
					subID(newSubscriberId), subID(subscriberId));

			if (success) {
				List<String> changeMsisdnSmsList = new ArrayList<String>();
				changeMsisdnSmsList.add("CHANGEMSISDN");
				if (isViralSmsTypeListForNewTable(changeMsisdnSmsList)) {
					ViralSMSNewImpl.insert(conn, subscriberId, new Date(),
							"CHANGEMSISDN", newSubscriberId, null, 0, null,
							null, null);

				} else {
					ViralSMSTableImpl.insert(conn, subscriberId, new Date(),
							"CHANGEMSISDN", newSubscriberId, null, 0, null,
							null, null);
				}
			}
			return success ? "SUCCESS" : "FAILURE:TECHNICAL FAULT";
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL FAULT";
	}

	public boolean updateSelStatusBasedOnRefID(String subID, String refID,
			String selStatus) {
		boolean success = false;
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			success = SubscriberStatusImpl.updateSelStatusBasedOnRefID(conn,
					subID, refID, selStatus);
		} catch (Exception e) {
			logger.error("exception in updateSelStatusBasedOnRefID", e);
		}

		return success;
	}

	public String reactivateSubscriber(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean tmp = false;
		try {
			tmp = SubscriberImpl
					.reactivateSubscriber(conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return tmp ? m_success : m_failure;
	}

	public ClipMinimal[] getClipsByName(String start) {
		return _rcm.getClipsByName(start);
	}

	public ClipMinimal getClipByName(String name) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClipByName(name, false);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClip(name);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public ClipMinimal getClipById(int clipId) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClip(clipId, false);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClip(clipId);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public void getAllClipsForCachingGui(Hashtable clips, HashMap map) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			ClipsImpl.getAllClipsForCachingGui(conn, clips, map);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public ClipMinimal[] getAllActiveClips() {
		return _rcm.getAllActiveClips();
	}

	public String[] getClipsNotInCategories(String category) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			String[] clips = ClipsImpl.getClipsNotInCategories(conn, category);
			if (clips == null)
				return null;

			ArrayList<String> list = new ArrayList<String>();
			Date curDate = new Date();
			for (int i = 0; i < clips.length; i++) {
				int clipIDInt = Integer.parseInt(clips[i]);
				ClipMinimal clipMinimal = getClipMinimal(clipIDInt, true);
				if (clipMinimal != null
						&& clipMinimal.getEndTime().after(curDate))
					list.add(clipMinimal.getClipId() + ","
							+ clipMinimal.getClipName() + ","
							+ clipMinimal.getAlbum() + ","
							+ clipMinimal.getArtist());
				else if (clipMinimal == null)
					logger.info("RBT::clipMinimal null for clip id "
							+ clipIDInt);
			}
			if (list.size() > 0)
				return list.toArray(new String[0]);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ClipMinimal[] getClipsNotInCategories1(String category) {
		String[] clips = _rcm.getClipsNotInCategories(category);
		if (clips == null)
			return null;

		ArrayList<ClipMinimal> list = new ArrayList<ClipMinimal>();
		Date curDate = new Date();
		for (int i = 0; i < clips.length; i++) {
			int clipIDInt = Integer.parseInt(clips[i]);
			ClipMinimal clipMinimal = getClipMinimal(clipIDInt, true);
			if (clipMinimal != null && clipMinimal.getEndTime().after(curDate))
				list.add(clipMinimal);
			else if (clipMinimal == null)
				logger.info("RBT::clipMinimal null for clip id " + clipIDInt);
		}
		if (list.size() > 0)
			return list.toArray(new ClipMinimal[0]);

		return null;
	}

	public Clips[] getClipsInCategory(String category) {
		return _rcm.getClipsInCategory(category);
	}

	public Clips getClip(int id) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClip(id);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClip(id);
			if (tmpClip == null)
				return null;

			Clips clip = new ClipsImpl(tmpClip.getClipId(),
					tmpClip.getClipName(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClipSmsAlias(), String.valueOf(tmpClip
							.getAddToAccessTable()), tmpClip.getClipPromoId(),
					tmpClip.getClassType(), tmpClip.getClipStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getSmsStartTime(),
					tmpClip.getAlbum(), tmpClip.getLanguage(),
					tmpClip.getClipDemoWavFile(), tmpClip.getArtist(),
					tmpClip.getClipInfo());
			return clip;
		}
	}

	public Clips getClip(String name) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClipByName(name);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClip(name);
			if (tmpClip == null)
				return null;

			Clips clip = new ClipsImpl(tmpClip.getClipId(),
					tmpClip.getClipName(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClipSmsAlias(), String.valueOf(tmpClip
							.getAddToAccessTable()), tmpClip.getClipPromoId(),
					tmpClip.getClassType(), tmpClip.getClipStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getSmsStartTime(),
					tmpClip.getAlbum(), tmpClip.getLanguage(),
					tmpClip.getClipDemoWavFile(), tmpClip.getArtist(),
					tmpClip.getClipInfo());
			return clip;
		}
	}

	public ClipMinimal getClipRBT(String rbt) {
		if (rbt == null)
			return null;

		if (_rcm.isClipCacheInitialized())
			return _rcm.getClipByWavFile(rbt, false);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClipByRbtWavFileName(rbt);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public Clips getClipPromoID(String promotionID) {
		Clips clips = null;
		if (_rcm.isClipCacheInitialized())
			clips = _rcm.getClipPromoID(promotionID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClipByPromoId(promotionID);
			if (tmpClip != null) {
				clips = new ClipsImpl(tmpClip.getClipId(),
						tmpClip.getClipName(), tmpClip.getClipNameWavFile(),
						tmpClip.getClipPreviewWavFile(),
						tmpClip.getClipRbtWavFile(), tmpClip.getClipGrammar(),
						tmpClip.getClipSmsAlias(), String.valueOf(tmpClip
								.getAddToAccessTable()),
						tmpClip.getClipPromoId(), tmpClip.getClassType(),
						tmpClip.getClipStartTime(), tmpClip.getClipEndTime(),
						tmpClip.getSmsStartTime(), tmpClip.getAlbum(),
						tmpClip.getLanguage(), tmpClip.getClipDemoWavFile(),
						tmpClip.getArtist(), tmpClip.getClipInfo());
			}
		}

		if (clips != null
				&& clips.endTime().getTime() < System.currentTimeMillis())
			clips = null;
		return clips;
	}

	public Clips getClipByPromoID(String promoID) {
		if (_rcm.isClipCacheInitialized())
			return getClipPromoID(promoID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClipByPromoId(promoID);
			if (tmpClip == null)
				return null;

			Clips clip = new ClipsImpl(tmpClip.getClipId(),
					tmpClip.getClipName(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClipSmsAlias(), String.valueOf(tmpClip
							.getAddToAccessTable()), tmpClip.getClipPromoId(),
					tmpClip.getClassType(), tmpClip.getClipStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getSmsStartTime(),
					tmpClip.getAlbum(), tmpClip.getLanguage(),
					tmpClip.getClipDemoWavFile(), tmpClip.getArtist(),
					tmpClip.getClipInfo());
			return clip;
		}
	}

	public ClipMinimal getClipSMSAlias(String alias) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClipSMSAlias(alias);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip tmpClip = rbtCacheManager
					.getClipBySMSAlias(alias);
			if (tmpClip == null)
				return null;

			ClipMinimal clip = new ClipMinimal(tmpClip.getClipId(),
					tmpClip.getClipPromoId(), tmpClip.getClipName(),
					tmpClip.getClipRbtWavFile(), tmpClip.getClipNameWavFile(),
					tmpClip.getClipPreviewWavFile(),
					tmpClip.getClipDemoWavFile(), tmpClip.getClipGrammar(),
					tmpClip.getClassType(), tmpClip.getSmsStartTime(),
					tmpClip.getClipEndTime(), tmpClip.getAlbum(),
					tmpClip.getLanguage(), tmpClip.getArtist(),
					tmpClip.getClipSmsAlias(), tmpClip.getClipInfo());
			return clip;
		}
	}

	public boolean removeClip(Clips clips) {
		if (clips == null)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			Clips temp = ClipsImpl.getClip(conn, clips.id());
			success = temp == null ? false : ClipsImpl.remove(conn, clips.id());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public int getClipCategoryId(int clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		int categoryId = -1;
		try {
			categoryId = ClipsImpl.getCategoryIDFromClipMap(conn, clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return categoryId;
	}

	public Clips[] getClipsToBeUpdated(int clipStartRange, int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Clips[] clips = null;
		try {
			clips = ClipsImpl.getClipsToBeUpdated(conn, clipStartRange,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return clips;
	}

	public boolean updateClipEndDateForTATA(int clipID, Date endTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean update = false;
		try {
			update = ClipsImpl.updateClipEndDateForTATA(conn, clipID, endTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return update;
	}

	/* all methods related to category clip map */
	public Clips createCategoryClip(Category category, Clips clips,
			String inList, int order, String playTime) {
		if ((category == null) || (clips == null))
			return null;

		Connection conn = getConnection();
		if (conn == null)
			return null;

		Clips temp = null;
		try {
			temp = ClipsImpl.insertCategoryClip(conn, category.getID(),
					clips.id(), inList, order, playTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return temp;
	}

	public boolean removeCategoryClip(Categories categories, Clips clips) {
		if ((categories == null) || (clips == null))
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			success = ClipsImpl.removeCategoryClip(conn, categories.id(),
					clips.id());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public Map<String, String> getCirclesOfUnprocessedClips(int startFrom,
			int limit) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipStatusImpl.getCirclesOfUnprocessedClips(conn, startFrom,
					limit);
		} catch (Throwable e) {
			logger.error("Exception while retreviewing clip status", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public Map<String, String> getPendingCirclesOfCategoryEntries() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipStatusImpl.getPendingCirclesOfCategoryEntries(conn);
		} catch (Throwable e) {
			logger.error("Exception while retreviewing clip status", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public Clips[] getAllClips(int categoryID) {
		return getAllClips(categoryID, null);
	}

	public Clips[] getAllClipsCCC(int categoryID) {
		return getAllClipsCCC(categoryID, null);
	}

	public Clips[] getAllClips(int categoryID, String chargeClasses) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getActiveCategoryClips(categoryID, chargeClasses, 'b');
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip[] tmpClips = rbtCacheManager
					.getActiveClipsInCategory(categoryID);
			if (tmpClips == null)
				return null;

			Clips[] clips = new Clips[tmpClips.length];
			for (int i = 0; i < tmpClips.length; i++) {
				clips[i] = new ClipsImpl(tmpClips[i].getClipId(),
						tmpClips[i].getClipName(),
						tmpClips[i].getClipNameWavFile(),
						tmpClips[i].getClipPreviewWavFile(),
						tmpClips[i].getClipRbtWavFile(),
						tmpClips[i].getClipGrammar(),
						tmpClips[i].getClipSmsAlias(),
						String.valueOf(tmpClips[i].getAddToAccessTable()),
						tmpClips[i].getClipPromoId(),
						tmpClips[i].getClassType(),
						tmpClips[i].getClipStartTime(),
						tmpClips[i].getClipEndTime(),
						tmpClips[i].getSmsStartTime(), tmpClips[i].getAlbum(),
						tmpClips[i].getLanguage(),
						tmpClips[i].getClipDemoWavFile(),
						tmpClips[i].getArtist(), tmpClips[i].getClipInfo());
			}
			return clips;
		}
	}

	public Clips[] getAllClipsCCC(int categoryID, String chargeClasses) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Clips[] list = null;
		try {
			list = ClipsImpl.getActiveCategoryClipsCCC(conn, categoryID,
					chargeClasses);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return list;
	}

	public Clips[] getInListCategoryClips(int categoryID) {
		return _rcm.getActiveCategoryClips(categoryID, null, 'y');
	}

	public Clips getClipByUniqueKey(String uniqueKey) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Clips clips = null;
		try {
			PromoMaster promoMaster = (PromoMaster) PromoMasterImpl
					.getPromoByCode(conn, uniqueKey);
			if (promoMaster == null)
				return null;
			clips = getClipByPromoID(promoMaster.clipID());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return clips;
	}

	/* all methods related to subscriber selections */

	/* it returns the number of selections already made by a subscriber */
	public int countSelectionsBySubscriber(String subscriberID, String callerID) {
		return countSelectionsBySubscriber(subID(subscriberID),
				subID(callerID), 0);
	}

	public int countSelectionsBySubscriber(String subscriberID,
			String callerID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		int count = 0;
		try {
			count = SubscriberStatusImpl.countSelectionsBySubscriber(conn,
					subID(subscriberID), subID(callerID), rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return count;
	}

	public boolean moreSelectionsAllowed(String subscriberID, String callerID) {
		return moreSelectionsAllowed(subID(subscriberID), subID(callerID), 0);
	}

	public boolean moreSelectionsAllowed(String subscriberID, String callerID,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return true;

		boolean check = true;
		try {
			check = SubscriberStatusImpl.moreSelectionsAllowed(conn,
					subID(subscriberID), subID(callerID), m_selectionLimit,
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return check;
	}

	public boolean isRRBTOn() {
		return RBTParametersUtils.getParamAsBoolean("COMMON", "IS_RRBT_ON",
				"false");
	}

	public boolean isMemCachePlayer() {
		boolean ret = false;
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter("DAEMON", "START_PLAYER_DAEMON");
		if (param != null)
			ret = param.getValue().equalsIgnoreCase("TRUE");
		param = CacheManagerUtil.getParametersCacheManager().getParameter(
				"DAEMON", "PLAYER_START_CLASS");
		if (param != null)
			ret = ret
					&& param.getValue()
							.equalsIgnoreCase(
									"com.onmobile.apps.ringbacktones.daemons.RBTPlayerUpdateDaemon");
		return ret;
	}

	/**
	 * This is called by addSubscriberSelection API for making entry into the
	 * selections table.
	 */
	public int createSubscriberStatus(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime,
			String sel_status, boolean smActivation, HashMap clipMap,
			int categoryType, boolean useDate, char loopStatus, boolean isTata,
			int nextPlus, int rbtType, String selInterval,
			HashMap extraInfoMap, String refID, boolean isDirectActivation,
			String circleId) {
		return createSubscriberStatus(subscriberID, callerID, categoryID,
				subscriberWavFile, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, nextChargingDate, prepaid,
				classType, changeSubType, fromTime, toTime, sel_status,
				smActivation, clipMap, categoryType, useDate, loopStatus,
				isTata, nextPlus, rbtType, selInterval, extraInfoMap, refID,
				isDirectActivation, circleId, null, false, false);
	}

	/**
	 * This is called by addSubscriberSelection API for making entry into the
	 * selections table.
	 */
	public int createSubscriberStatus(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime,
			String sel_status, boolean smActivation, HashMap clipMap,
			int categoryType, boolean useDate, char loopStatus, boolean isTata,
			int nextPlus, int rbtType, String selInterval,
			HashMap extraInfoMap, String refID, boolean isDirectActivation,
			String circleId, Subscriber sub, boolean useUIChargeClass,
			boolean isFromDownload) {
		logger.info("Adding subscriber selections, subscriberId: "
				+ subscriberID + ", classType: " + classType
				+ ", extraInfoMap: " + extraInfoMap + ",clipMap =" + clipMap);

		// Added for UDP
		String udpId = null;
		String selectedNewSubscriptionClass = null;
		String upgradedNewSubscriptionClass = null;
		Subscriber subscriber = getSubscriber(subscriberID);
		if (extraInfoMap.containsKey("UDP_ID"))
			udpId = (String) extraInfoMap.remove("UDP_ID");
		if (extraInfoMap.containsKey("SELECTED_SUBSCRIPTION_CLASS"))
			selectedNewSubscriptionClass = (String ) extraInfoMap.remove("SELECTED_SUBSCRIPTION_CLASS");
		if(subscriber != null && isSubscriberActivated(subscriber) && selectedNewSubscriptionClass!=null && !selectedNewSubscriptionClass.equals(subscriber.subscriptionClass())){
			extraInfoMap.put(iRBTConstant.EXTRA_INFO_REQUEST_TYPE, "UPGRADE");
		}
	
		/*	if (extraInfoMap.containsKey("UPGRADED_SUBSCRIPTION_CLASS"))
		{
			upgradedNewSubscriptionClass = (String ) extraInfoMap.remove("UPGRADED_SUBSCRIPTION_CLASS");
			logger.info(":--> upgradedNewSubscriptionClass :"+upgradedNewSubscriptionClass);
			
		}*/
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		int count = 0;
		SubscriberStatus subscriberStatus = null;
		boolean isInlineReq = false;
		Integer inlineFlag = null;
		String inlineFlow = null;
		
		try {
			if (!isTata) {
				subscriberID = subID(subscriberID);
				callerID = subID(callerID);
			}
			if (isTata)
				smActivation = false;

			if (isDirectActivation) {
				Date curDate = new Date();
				nextChargingDate = curDate;
				
				// commented for supporting future date selection
				//startTime = curDate;
				nextPlus = 0;

				sel_status = STATE_ACTIVATED;
				loopStatus = getLoopStatusToUpateSelection(loopStatus,
						subscriberID, prepaid.equalsIgnoreCase("y"));
			}

			SubscriptionRequest subscriberRequest = null;
			String makeActUserEntryConsentTable = RBTParametersUtils
					.getParamAsString(COMMON,
							"MAKE_ENTRY_CONSENT_ACT_USER_SEL", "TRUE");
			String makeConsentForConfigChargeClass = RBTParametersUtils
					.getParamAsString(COMMON, "CHARGE_CLASS_FOR_CONSENT", null);
			List<String> consentChargeClass = null;
			boolean toMakeConsentForSel = true;
			if (makeConsentForConfigChargeClass != null) {
				consentChargeClass = Arrays
						.asList(makeConsentForConfigChargeClass.split(","));
			}
			if (consentChargeClass != null
					&& !consentChargeClass.contains(classType)) {
				toMakeConsentForSel = false;
			}

			boolean isMakeActUserEntryConsentTable = true;
			subscriber = getSubscriber(subscriberID);
			if (makeActUserEntryConsentTable.equalsIgnoreCase("FALSE")) {
				isMakeActUserEntryConsentTable = isSubscriberDeactivated(subscriber);
			}
			String campaignCode = null;
			String treatmentCode = null;
			String offerCode = null;
			if (extraInfoMap != null
					&& extraInfoMap.containsKey(iRBTConstant.CAMPAIGN_CODE)
					&& extraInfoMap.containsKey(iRBTConstant.OFFER_CODE)
					&& extraInfoMap.containsKey(iRBTConstant.TREATMENT_CODE)) {
				campaignCode = (String) extraInfoMap
						.get(iRBTConstant.CAMPAIGN_CODE);
				treatmentCode = (String) extraInfoMap
						.get(iRBTConstant.TREATMENT_CODE);
				offerCode = (String) extraInfoMap.get(iRBTConstant.OFFER_CODE);
				extraInfoMap.remove(iRBTConstant.CAMPAIGN_CODE);
				extraInfoMap.remove(iRBTConstant.TREATMENT_CODE);
				extraInfoMap.remove(iRBTConstant.OFFER_CODE);
			}
			// Jira RBT - 13221 - This will be used on the for RbtDbManager
			// class to check the
			// Upgrade flow and make a consent entry.
			String requestType = null;
			if (extraInfoMap != null
					&& extraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_REQUEST_TYPE)) {
				requestType = (String) extraInfoMap
						.remove(iRBTConstant.EXTRA_INFO_REQUEST_TYPE);
			}

			String selExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfoMap);

			String feedSubType = null;
			if (extraInfoMap != null) {
				feedSubType = (String) extraInfoMap.get("FEED_SUB_TYPE");
				extraInfoMap.remove("FEED_SUB_TYPE");
				logger.info("Feed sub type for Consent = " + feedSubType);
			}
			boolean ibmSEDefaultSel = false;
			if (extraInfoMap != null && extraInfoMap.containsKey("IBM_SE")) {
				ibmSEDefaultSel = true;
			}
			String slice_duration = null;
			if (clipMap.containsKey(WebServiceConstants.param_slice_duration)) {
				slice_duration = (String) clipMap
						.get(WebServiceConstants.param_slice_duration);
			}
			// Jira RBT - 13221.
			String VfUpgradeFeatureClass = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.COMMON,
							"CREATE_CLASS_FOR_VF_UPGRADE_FEATURE", null);
			String upgradeModes = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON,
							"VODAFONE_UPGRADE_CONSENT_MODES", null);
			boolean modeCheckForVfUpgrade = false;
			if (VfUpgradeFeatureClass != null) {
				List<String> modesList = upgradeModes != null ? Arrays
						.asList(upgradeModes.split(",")) : null;
				modeCheckForVfUpgrade = (modesList == null
						|| modesList.isEmpty() || !modesList
						.contains(selectedBy));
			}
			// Third Party confirmation chages
			boolean isMakeUpgradeUserEntryConsentTable = requestType != null ? requestType
					.equalsIgnoreCase("UPGRADE") && modeCheckForVfUpgrade
					: false;
			if (!isFromDownload
					&& ((isMakeActUserEntryConsentTable
							|| isMakeUpgradeUserEntryConsentTable || (makeConsentForConfigChargeClass != null && toMakeConsentForSel)))
					&& !ibmSEDefaultSel) {
				String subConsentId = null;
				if ((isSubscriberDeactivated(subscriber) && sub != null)
						|| isMakeUpgradeUserEntryConsentTable) {
					subConsentId = sub.refID();
					// if(subConsentId != null) {
					// if(extraInfoMap == null) {
					// extraInfoMap = new HashMap();
					// }
					// extraInfoMap.put("TRANS_ID", subConsentId);
					// selExtraInfo =
					// DBUtility.getAttributeXMLFromMap(extraInfoMap);
					// }
				}

				String subscriptionClass = null;
				String oldSubscriptionClass = null;
				if (sub != null) {
					oldSubscriptionClass = sub.subscriptionClass();
				}
				if (consentChargeClass != null && toMakeConsentForSel) {
					subscriptionClass = oldSubscriptionClass;
				}
				// RBT-13221
				String newSubClassType = null;
				boolean isTNBUser = false;
				if (oldSubscriptionClass != null
						&& tnbSubscriptionClasses
								.contains(oldSubscriptionClass)) {
					ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools
							.getParameter("COMMON",
									"TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO",
									",");
					ArrayList<String> tnbFreeChargeClass = DBConfigTools
							.getParameter("DAEMON", "TNB_FREE_CHARGE_CLASS",
									ConstantsTools.FREE, ",");
					if (clipMap
							.containsKey(WebServiceConstants.param_consent_subscriptionClass)) {
						String ConsentsubscriptionClass = (String) clipMap
								.get(WebServiceConstants.param_consent_subscriptionClass);
						logger.info("ConsentsubscriptionClass = "
								+ ConsentsubscriptionClass);
						if (null != ConsentsubscriptionClass)
							oldSubscriptionClass = ConsentsubscriptionClass;
					}
					if (tnbFreeChargeClass.contains(classType)
							|| classType.startsWith(ConstantsTools.FREE)) {
						// Free Selection or Download Callback
						logger.debug("Free selection classType: " + classType
								+ " tnbFreeChargeClass: " + tnbFreeChargeClass);
					} else {
						for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
							String[] split = tnbUpgradeSubClass.split("\\:");
							if (split == null || split.length != 2) {
								continue;
							}
							if (oldSubscriptionClass.equalsIgnoreCase(split[0])) {
								newSubClassType = split[1];
								logger.debug("Mode based new subscription class is found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
										+ oldSubscriptionClass
										+ ", newSubClassType: "
										+ newSubClassType);
								break;
							} else {
								logger.debug("Mode based new subscription class is not found from TNB_UPGRADE_SUBSCRIPTION_CLASSES. modeAndSubClass key: "
										+ oldSubscriptionClass
										+ " from "
										+ tnbUpgradeSubClass);
							}
						}
						isTNBUser = true;
						if (VfUpgradeFeatureClass != null) {
							isMakeUpgradeUserEntryConsentTable = true;
						}
					}
				}
				// End
				// RBT-9873 Added isConsentActRecordInserted for CG flow
				boolean isConsentActRecordInserted = false;
				boolean isAllowPremiumContent = false;
				boolean isUdsOnRequest = false;
				if (clipMap.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
					isConsentActRecordInserted = true;
				}
				if (clipMap
						.containsKey(WebServiceConstants.param_allowPremiumContent)) {
					isAllowPremiumContent = true;
				}
				if (clipMap.containsKey(WebServiceConstants.param_isUdsOn)) {
					String udson = (String) clipMap
							.get(WebServiceConstants.param_isUdsOn);
					if (udson.equalsIgnoreCase("true")) {
						isUdsOnRequest = true;
					}
				}

				if (isMakeUpgradeUserEntryConsentTable && isTNBUser) {
					String cosId = null;
					if (null != subscriberRequest
							&& null != subscriberRequest.getCosID())
						cosId = String.valueOf(subscriberRequest.getCosID());
					HashMap<String, String> xtraInfoMap = new HashMap<String, String>();
					if (extraInfoMap != null
							&& extraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						String tpcgId = (String) extraInfoMap
								.get(iRBTConstant.EXTRA_INFO_TPCGID);
						xtraInfoMap.put(EXTRA_INFO_TPCGID, tpcgId);
					}
					String subExtraInfo = DBUtility
							.getAttributeXMLFromMap(xtraInfoMap);
					String consentUniqueId = Utility
							.generateConsentIdRandomNumber(subscriberID);
					String referenceID = null;
					if (consentUniqueId != null) {
						referenceID = consentUniqueId;
					}
					if (null == referenceID) {
						referenceID = UUID.randomUUID().toString();
					}
					subConsentId = referenceID;
					String selectedByMode = VfRBTUpgardeConsentFeatureImpl
							.getMappedModeForUpgrade(oldSubscriptionClass,
									newSubClassType, selectedBy);
					boolean success = false;
					String subscriptionclass;
					if (subscriber != null) {
						subscriptionclass = subscriber.subscriptionClass();
					} else {
						subscriptionclass = sub.subscriptionClass();

					}
					logger.info("Subscriptionclass is :" + subscriptionclass);
					if (!modeCheckForVfUpgrade) {
						if (requestType == null
								|| !requestType.equalsIgnoreCase("UPGRADE")) {
							success = convertSubscriptionType(subscriberID,
									subscriptionclass, newSubClassType,
									selectedByMode, selectedByMode, true,
									rbtType, true, subExtraInfo, subscriber);
							logger.info("Updated status changed, update "
									+ "status: " + success
									+ " for subscriber: " + subscriberID);
							if (!success) {
								logger.warn("Subscription is not updated  into consent table in DB, subscriberID: "
										+ subscriberID + ". Returning count: 0");
								return 0;
							}
						}
						isMakeUpgradeUserEntryConsentTable = modeCheckForVfUpgrade;
					} else {
						Date requestTime = null;
						int secondsToBeAddedInRequestTime = DBUtility
								.secondsToBeAddedInRequestTime(circleId,
										selectedBy); // original mode is passed
														// for obtaining the
														// config
						if (secondsToBeAddedInRequestTime != -1) {
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.SECOND,
									secondsToBeAddedInRequestTime);
							requestTime = cal.getTime();
						}
						success = ConsentTableImpl.insertSubscriptionRecord(
								conn, subID(subscriberID), selectedByMode,
								startTime, endTime, selectedByMode, prepaid,
								newSubClassType, cosId, rbtType,
								isDirectActivation, subExtraInfo, circleId,
								referenceID, 0, true, requestTime,null);
						if (!success) {
							logger.warn("Subscription is not updated  into consent table in DB, subscriberID: "
									+ subscriberID + ". Returning count: 0");
							return 0;
						}
					}

				}

				try {
					if (clipMap.containsKey(Constants.param_LANG_CODE)) {
						String language = (String) clipMap
								.get(Constants.param_LANG_CODE);
						if (sub != null && language != null
								&& !language.isEmpty())
							sub.setLanguage(language);
					}
					extraInfoMap.put("SELECTED_SUBSCRIPTION_CLASS", selectedNewSubscriptionClass);
					//extraInfoMap.put("UPGRADED_SUBSCRIPTION_CLASS",upgradedNewSubscriptionClass);
					subscriberStatus = checkModeAndInsertIntoConsent(
							subscriberID, callerID, categoryID,
							subscriberWavFile, setTime, startTime, endTime,
							status, selectedBy, selectionInfo,
							nextChargingDate, prepaid, classType, fromTime,
							toTime, sel_status, categoryType, loopStatus,
							rbtType, selInterval, refID, circleId, conn,
							extraInfoMap, useUIChargeClass, subConsentId,
							feedSubType, subscriptionClass,
							isConsentActRecordInserted, isAllowPremiumContent,
							isUdsOnRequest, slice_duration,
							isMakeUpgradeUserEntryConsentTable,
							oldSubscriptionClass, smActivation, nextPlus, sub);
					extraInfoMap.remove("SELECTED_SUBSCRIPTION_CLASS");
					//extraInfoMap.remove("UPGRADED_SUBSCRIPTION_CLASS");
					selExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("COMBO_TRANS_ID")) {
						subConsentId = extraInfoMap.remove("COMBO_TRANS_ID")
								.toString();
					}
					
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("CONSENTSTATUS"))
					{
					clipMap.put("CONSENTSTATUS",extraInfoMap.remove("CONSENTSTATUS"));
					
					}
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("COMBO"))
					{
					clipMap.put("COMBO",extraInfoMap.remove("COMBO"));
					
					}
					
					
					
				} catch (ConsentHitFailureException e) {
					logger.info(e.getMessage());
					return 0;
				}

				// Change for bsnl east cg integration.
				boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
						.getParamAsBoolean(COMMON,
								"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
				// For BSNL East CG flow consent record should be inserted .
				if (isCGIntegrationFlowForBsnlEast && null != subscriberStatus) {
					String consentTransID = subscriberStatus.refID();
					Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
							.getInstance().getClipByRbtWavFileName(
									subscriberWavFile);
					if(isSubscriberActivated(subscriber) || isSubscriberActivationPending(subscriber)){
						clipMap.put("CONSENTSTATUS", "0");
					}
					Map<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriberStatus
									.extraInfo());
					String comboTransID = subConsentId;
					String baseServiceID = null;
					String selectionServiceID = null;
					if (null != comboTransID) {
						subscriberRequest = getSubscriptionRecordForTransID(comboTransID);
						baseServiceID = DoubleConfirmationConsentPushThread
								.getServiceValue("SERVICE_ID",
										subscriberRequest
												.getSubscriptionClass(),
										classType, circleId, true, false, false);
					} else {
						baseServiceID = DoubleConfirmationConsentPushThread
								.getServiceValue("SERVICE_ID",
										subscriber.subscriptionClass(),
										classType, circleId, true, false, false);

					}
					selectionServiceID = DoubleConfirmationConsentPushThread
							.getServiceValue("SERVICE_ID", null, classType,
									circleId, false, true, false);
					String charge = "0";
					ChargeClass catCharge = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					SubscriptionClass subClass = null;
					if (null != comboTransID) {
						subClass = CacheManagerUtil
								.getSubscriptionClassCacheManager()
								.getSubscriptionClass(
										subscriberRequest
												.getSubscriptionClass());
					}
					String catAmount = "0";
					String subAmount = "0";
					if (null != catCharge) {
						catAmount = catCharge.getAmount();
					}
					if (null != subClass) {
						subAmount = subClass.getSubscriptionAmount();
					}
					charge = String.valueOf(Integer.parseInt(catAmount)
							+ Integer.parseInt(subAmount));
					Category category = getCategory(categoryID);
					HttpResponse httpResponse = null;
					if (null != comboTransID) {
						httpResponse = constructConsentUrlandHit(comboTransID,
								subscriberStatus.selectedBy(),
								subscriberStatus.selectedBy(),
								subscriberStatus.subID(), clipObj,
								baseServiceID, selectionServiceID, charge,
								circleId, subscriberWavFile, loopStatus,
								subscriberRequest.getSubscriptionClass(),
								classType, "ACTSEL",
								subscriberRequest.getRequestTime(), clipMap,
								category,subscriberStatus.callerID());
					} else {
						httpResponse = constructConsentUrlandHit(
								subscriberStatus.refID(),
								subscriberStatus.selectedBy(),
								subscriberStatus.selectedBy(),
								subscriberStatus.subID(), clipObj,
								baseServiceID, selectionServiceID, charge,
								circleId, subscriberWavFile, loopStatus,
								subscriptionClass, classType, "SEL",
								subscriberStatus.getRequestTime(), clipMap,
								category,subscriberStatus.callerID());
					}
					try {
						if (httpResponse != null
								&& httpResponse.getResponseCode() == 200) {
							String response = httpResponse.getResponse();
							String subcriberExtraInfo = null;
							String[] tokens = response.split(":");
							boolean isValidCGResponse = false; 
							if (null == tokens || tokens.length < 3) {
								tokens = response.split("\\|");
							}
							if(tokens != null && tokens.length >= 3){
								String cgResStatus = tokens[2];
								try{
								Long.parseLong(cgResStatus);
								isValidCGResponse = true;
								clipMap.put("BSNL_CONSENT_SESSION_ID", cgResStatus);
								logger.info("vaild cg response" + cgResStatus);
								}catch(NumberFormatException e){
									logger.error("invaild cg response" + cgResStatus);
								}
							}
							if (null != tokens
									&& tokens.length >= 3
									&& (tokens[1].equalsIgnoreCase("0") || tokens[1]
											.equalsIgnoreCase("Accepted") || (isValidCGResponse) )) {
								if (null != comboTransID) {
									String subscription = STATE_TO_BE_ACTIVATED;
									if (isDirectActivation) {
										subscription = STATE_ACTIVATED;
									}
									if (null != subscriberRequest) {
										Date startDate = new Date(
												System.currentTimeMillis());
										xtraInfoMap = DBUtility
												.getAttributeMapFromXML(subscriberRequest
														.getExtraInfo());
										if (xtraInfoMap != null) {
											if (tokens[1]
													.equalsIgnoreCase("Accepted")) {
												xtraInfoMap.put(
														EXTRA_INFO_TRANS_ID,
														tokens[2]);
											} else {
												xtraInfoMap.put(
														EXTRA_INFO_TRANS_ID,
														tokens[0]);
											}
											xtraInfoMap
													.put(EXTRA_INFO_CG_SUBSCRIPTION_ID,
															comboTransID);
										}
										subcriberExtraInfo = DBUtility
												.getAttributeXMLFromMap(xtraInfoMap);
										String cosId = null;
										if (null != String
												.valueOf(subscriberRequest
														.getCosID())) {
											cosId = String
													.valueOf(subscriberRequest
															.getCosID());

										}
										if (subscriber != null) {
											boolean success = SubscriberImpl
													.update(conn,
															subID(subscriberID),
															subscriberRequest
																	.getMode(),
															null,
															startDate,
															subscriberRequest
																	.getSubscriberEndDate(),
															prepaid,
															null,
															null,
															0,
															subscriberRequest
																	.getModeInfo(),
															subscriberRequest
																	.getSubscriptionClass(),
															null,
															null,
															null,
															subscription,
															0,
															cosId,
															cosId,
															rbtType,
															subscriber
																	.language(),
															subcriberExtraInfo,
															isDirectActivation,
															circleId,
															UUID.randomUUID()
																	.toString());
											if (!success) {
												logger.warn("Subscription is not updated  into DB, subscriberID: "
														+ subscriberID
														+ ". Returning count: 0");
												return 0;
											}
											subscriber = SubscriberImpl
													.getSubscriber(conn,
															subID(subscriberID));

										} else {
											subscriber = SubscriberImpl
													.insert(conn,
															subscriberRequest
																	.getSubscriberID(),
															subscriberRequest
																	.getMode(),
															null,
															startDate,
															subscriberRequest
																	.getSubscriberEndDate(),
															prepaid,
															null,
															null,
															0,
															subscriberRequest
																	.getModeInfo(),
															subscriberRequest
																	.getSubscriptionClass(),
															null, null, null,
															subscription, 0,
															cosId, cosId,
															rbtType, null,
															isDirectActivation,
															subcriberExtraInfo,
															circleId, null);
										}

										if (subscriber == null) {
											logger.warn("Subscription is not populated into DB, refId: "
													+ comboTransID
													+ ". Returning count: 0");
											return 0;
										} else {
											clipMap.put("SUBSCRIBER",
													subscriber);
											boolean success = deleteConsentRequestByTransIdAndMSISDN(
													comboTransID, subscriberID);
										}
									} else {
										logger.warn("Subscription is not populated into DB, refId: "
												+ comboTransID
												+ ". Returning count: 0");
										return 0;
									}
								}
								String selectionExtraInfo = null;
								if (null != subscriberStatus) {
									xtraInfoMap = DBUtility
											.getAttributeMapFromXML(subscriberStatus
													.extraInfo());
									if (xtraInfoMap != null) {
										if (null != comboTransID) {
											xtraInfoMap
													.put(EXTRA_INFO_CG_SUBSCRIPTION_ID,
															comboTransID);
										} else {
											xtraInfoMap.put(
													EXTRA_INFO_TRANS_ID,
													subscriberStatus.refID());
											xtraInfoMap
													.put(EXTRA_INFO_CG_SUBSCRIPTION_ID,
															subscriberStatus
																	.refID());
										}
									} else {
										xtraInfoMap = new HashMap<String, String>();
										xtraInfoMap.put(EXTRA_INFO_TRANS_ID,
												subscriberStatus.refID());
										xtraInfoMap.put(
												EXTRA_INFO_CG_SUBSCRIPTION_ID,
												subscriberStatus.refID());
									}
								}
								selectionExtraInfo = DBUtility
										.getAttributeXMLFromMap(xtraInfoMap);
								subscriberStatus = SubscriberStatusImpl.insert(
										conn, subID(subscriberID), callerID,
										categoryID, subscriberWavFile, setTime,
										startTime, endTime, status, classType,
										selectedBy,
										subscriberStatus.selectionInfo(),
										nextChargingDate, prepaid, fromTime,
										toTime, smActivation, sel_status, null,
										null, categoryType, loopStatus,
										nextPlus, rbtType, selInterval,
										selectionExtraInfo, null, circleId,
										udpId, null);

								if (subscriberStatus == null) {
									logger.warn("Selection is not populated into DB, refId: "
											+ refID + ". Returning count: 0");
									return 0;
								} else {
									TPTransactionLogger tpTransactionLogger = TPTransactionLogger
											.getTPTransactionLoggerObject("selection");
									tpTransactionLogger.writeTPTransLog(
											circleId, subscriberID,
											subscriberStatus.callerID(),
											subscriberStatus.selType(),
											subscriberStatus.fromTime(),
											subscriberStatus.toTime(),
											subscriberStatus.selInterval(),
											subscriberStatus.categoryType(),
											subscriberStatus.status(),
											subscriberStatus.subscriberFile(),
											subscriberStatus.categoryID(), 1,
											subscriberStatus.classType(),
											subscriberStatus.startTime(),
											subscriberStatus.endTime(),
											subscriberStatus.loopStatus() + "");
									// count will be incremented when it is
									// successfully inserted into
									// subscriber selections table.delete the
									// consent record
									boolean success = deleteConsentRequestByTransIdAndMSISDN(
											consentTransID, subscriberID);
									if (!clipMap
											.containsKey("RECENT_CLASS_TYPE")) {
										logger.info("Adding Recent class type for selection = "
												+ classType);
										clipMap.put("RECENT_CLASS_TYPE",
												classType);
									}
									logger.info("Selection is inserted into Selections table"
											+ ". Returning count: 1");
									clipMap.put("SELECTION_REF_ID",
											subscriberStatus.refID());
									clipMap.put("DESCRIPTION", tokens[2]);
									count = 1;
								}
							} else {
								deleteConsentRequestByTransIdAndMSISDN(
										consentTransID, subscriberID);
								deleteConsentRequestByTransIdAndMSISDN(
										comboTransID, subscriberID);
								logger.warn("Response from cg is :" + tokens
										+ " refId: " + refID
										+ ". Returning count: 0");
								return 0;
							}
						} else {
							deleteConsentRequestByTransIdAndMSISDN(
									consentTransID, subscriberID);
							deleteConsentRequestByTransIdAndMSISDN(
									comboTransID, subscriberID);
							logger.warn("Response from cg is failed and  refId: "
									+ refID + ". Returning count: 0");
							return 0;
						}

					} catch (Exception e) {
						logger.error("RBT:: " + e.getMessage(), e);
						return 0;
					}
					
				}

				// For consent
				if (clipMap != null && subscriberStatus != null) {
					clipMap.put(param_isSelConsentInserted, "true");
					String consentId = null;
					Map<String, String> selExtraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriberStatus
									.extraInfo());

					if (selExtraInfoMap != null) {
						consentId = selExtraInfoMap.get(EXTRA_INFO_TRANS_ID);
					}

					if (consentId == null) {
						consentId = subscriberStatus.refID();
					}

					clipMap.put("CONSENTID", consentId);
					// RBT-15014
					// Included to pass mode from Web Service
					clipMap.put("CONSENT_MODE", selectedBy);

					if (isMakeUpgradeUserEntryConsentTable) {
						subscriberRequest = getSubscriptionRecordForTransID(consentId);
						if (subscriberRequest != null) {
							clipMap.put("CONSENTSUBCLASS",
									subscriberRequest.getSubscriptionClass());
							clipMap.put(
									"CONSENT_SERVICE_ID",
									DoubleConfirmationConsentPushThread
											.getServiceValue(
													"SERVICE_ID",
													subscriberRequest
															.getSubscriptionClass(),
													classType,
													subscriberRequest
															.getCircleID(),
													false, false, true));
							clipMap.put(
									"CONSENT_SERVICE_CLASS",
									DoubleConfirmationConsentPushThread
											.getServiceValue(
													"SERVICE_CLASS",
													subscriberRequest
															.getSubscriptionClass(),
													classType,
													subscriberRequest
															.getCircleID(),
													false, false, true));
							clipMap.put("CONSENT_MODE",
									subscriberRequest.getMode());
						}
					} else if (subConsentId != null
							&& isSubscriberDeactivated(subscriber)) {
						clipMap.put("CONSENTID", subConsentId);
						if (sub != null)
							clipMap.put("CONSENTSUBCLASS",
									sub.subscriptionClass());
						clipMap.put("CONSENT_SERVICE_ID",
								DoubleConfirmationConsentPushThread
										.getServiceValue("SERVICE_ID",
												sub.subscriptionClass(),
												classType, sub.circleID(),
												false, true, true));
						clipMap.put("CONSENT_SERVICE_CLASS",
								DoubleConfirmationConsentPushThread
										.getServiceValue("SERVICE_CLASS",
												sub.subscriptionClass(),
												classType, sub.circleID(),
												false, true, true));
					} else if (subscriber != null) {
						clipMap.put("CONSENT_SERVICE_ID",
								DoubleConfirmationConsentPushThread
										.getServiceValue("SERVICE_ID",
												subscriber.subscriptionClass(),
												classType,
												subscriber.circleID(), false,
												true, false));
						clipMap.put("CONSENT_SERVICE_CLASS",
								DoubleConfirmationConsentPushThread
										.getServiceValue("SERVICE_CLASS",
												subscriber.subscriptionClass(),
												classType,
												subscriber.circleID(), false,
												true, false));
					}
					clipMap.put("CONSENTCLASSTYPE", classType);

					if (null != sub) {
						String language = com.onmobile.apps.ringbacktones.webservice.common.Utility
								.getLanguageCode(sub != null ? sub.language()
										: null);
						clipMap.put("LANGUAGE_ID", language);

						String planId = com.onmobile.apps.ringbacktones.webservice.common.Utility
								.getPlanId(sub != null ? sub
										.subscriptionClass() : null);
						clipMap.put("PLAN_ID", planId);
					}
					clipMap.put("EVENT_TYPE", "1");

				}

				if (makeActUserEntryConsentTable.equalsIgnoreCase("FALSE")
						&& subConsentId == null && !toMakeConsentForSel) {
					clipMap.remove("CONSENTID");
				}
			}

			if (null == subscriberStatus) {
				String sliceRBTWavFile = null;
				if (slice_duration != null) {
					Clip clip = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
							.getInstance().getClipByRbtWavFileName(
									subscriberWavFile);
					if (clip != null)
						sliceRBTWavFile = "rbt_slice_" + clip.getClipId() + "_"
								+ slice_duration + "_rbt";
				}
				if (sliceRBTWavFile != null) {
					subscriberWavFile = sliceRBTWavFile;
				}
				HashMap<String, String> selExtraInfoTmp = DBUtility
						.getAttributeMapFromXML(selExtraInfo);
				if (selExtraInfoTmp != null
						&& selExtraInfoTmp.containsKey(UDS_OPTIN)) {
					selExtraInfoTmp.remove(UDS_OPTIN);
					selExtraInfo = DBUtility
							.getAttributeXMLFromMap(selExtraInfoTmp);
				}
				if(isSubscriberStrictlyActive(sub)) {
					inlineFlow = Utility.getInlineFlow(classType, 2);
					if(inlineFlow != null && sel_status.equalsIgnoreCase(STATE_TO_BE_ACTIVATED)) {
						isInlineReq = true;
						inlineFlag = 0;
					} else {
						logger.debug("Inline flag is set to null for:" + classType + " " + subscriberID + " " + sel_status);
					}
				}
				subscriberStatus = SubscriberStatusImpl.insert(conn,
						subID(subscriberID), callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status,
						classType, selectedBy, selectionInfo, nextChargingDate,
						prepaid, fromTime, toTime, smActivation, sel_status,
						null, null, categoryType, loopStatus, nextPlus,
						rbtType, selInterval, selExtraInfo, refID, circleId,
						udpId, inlineFlag);

				if (extraInfoMap.containsKey(UDS_OPTIN)
						&& subscriberStatus != null) {
					HashMap<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriber.extraInfo());
					if (xtraInfoMap == null) {
						xtraInfoMap = new HashMap<String, String>();
					}
					xtraInfoMap.put(UDS_OPTIN,
							(String) extraInfoMap.get(UDS_OPTIN));
					updateExtraInfo(subscriberID,
							DBUtility.getAttributeXMLFromMap(xtraInfoMap));
				}

				if (subscriberStatus == null) {
					logger.warn("Selection is not populated into DB, refId: "
							+ refID + ". Returning count: 0");
					isInlineReq = false;
					return 0;
				} else {
					TPTransactionLogger tpTransactionLogger = TPTransactionLogger
							.getTPTransactionLoggerObject("selection");
					tpTransactionLogger.writeTPTransLog(circleId, subscriberID,
							subscriberStatus.callerID(),
							subscriberStatus.selType(),
							subscriberStatus.fromTime(),
							subscriberStatus.toTime(),
							subscriberStatus.selInterval(),
							subscriberStatus.categoryType(),
							subscriberStatus.status(),
							subscriberStatus.subscriberFile(),
							subscriberStatus.categoryID(), 1,
							subscriberStatus.classType(),
							subscriberStatus.startTime(),
							subscriberStatus.endTime(),
							subscriberStatus.loopStatus() + "");

					// count will be incremented when it is successfully
					// inserted into
					// subscriber selections table.
					if (!clipMap.containsKey("RECENT_CLASS_TYPE")) {
						logger.info("Adding Recent class type for selection = "
								+ classType);
						clipMap.put("RECENT_CLASS_TYPE", classType);
					}
					logger.info("Selection is inserted into Selections table"
							+ ". Returning count: 1");
					clipMap.put("SELECTION_REF_ID", subscriberStatus.refID());
					count = 1;
				}

			} else {
				logger.info("Selection is inserted into Consent table"
						+ ". Returning count: 2");
				// This change is for bsnl east for all other flows it will pass
				// 2
				// for bsnl it will pass 1
				if (count == 0) {
					count = 2;
				}
			}

			// count = 1;

			int clipID = -1;
			String clipName = null;
			if (clipMap != null) {
				String s = (String) clipMap.get("CLIP_ID");
				try {
					clipID = Integer.parseInt(s);
				} catch (Exception e) {
					clipID = -1;
				}
				clipName = (String) clipMap.get("CLIP_NAME");
			}
			if (clipID != -1) {
				Date currentDate = null;
				if (useDate) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					currentDate = calendar.getTime();
				}
				Date date = new Date(System.currentTimeMillis());

				DateFormat timeFormatYYYY = new SimpleDateFormat("yyyy");
				String year = timeFormatYYYY.format(date);
				if (currentDate != null)
					year = timeFormatYYYY.format(currentDate);

				DateFormat timeFormatMM = new SimpleDateFormat("MM");
				String month = timeFormatMM.format(date);
				if (currentDate != null)
					month = timeFormatMM.format(currentDate);

				Access access = AccessImpl.getAccess(conn, clipID, year, month,
						currentDate);
				if (access == null)
					access = AccessImpl.insert(conn, clipID, clipName, year,
							month, 0, 0, 0, currentDate);
				else {
					access.incrementNoOfAccess();
					if (subscriberWavFile != null
							&& subscriberWavFile.indexOf("rbt_ugc_") != -1)
						access.incrementNoOfPlays();
					access.update(conn);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
			if(subscriberStatus != null) {
				Map<String, String> selExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());
				if(selExtraInfoMap != null && selExtraInfoMap.get("INLINE_CG") != null) {
					Utility.sendInlineMessage(subscriberStatus.subID(), subscriberStatus.refID(), WebServiceConstants.api_Selection, WebServiceConstants.CONSENT, selExtraInfoMap.get("INLINE_CG"));
				} else if(isInlineReq) {
					Utility.sendInlineMessage(subscriberStatus.subID(), subscriberStatus.refID(), WebServiceConstants.api_Selection, WebServiceConstants.action_activate, inlineFlow);
				}
			}
		}
		logger.info("Returning count: " + count);
		return count;
	}
	//In CG API RBT to pass optional parameter namey 'param1' incases where user adds additional songs
	private String getLoopStatusForSelection(String subID,String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			SubscriberStatus[] subscriberStatus= SubscriberStatusImpl.getSubscriberSelections( conn, subID(subID), subID(callerID), 0);
			for(SubscriberStatus selection:subscriberStatus){
				if(selection.callerID()!=null && selection.callerID().equalsIgnoreCase(callerID)){
					//If Caller is already exists
					//For RBT-18484
					return "param1=2";
				}
			}
			return "param1=1";
		} catch (Throwable e) {
			logger.info("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	// RBT-13642
	private HttpResponse constructConsentUrlandHit(String tid, String source,
			String consent_source, String msisdn, Clip clipObj,
			String baseServiceID, String selectionServiceID, String charge,
			String circleId, String wavFile, char loopStatus, String subClass,
			String classType, String consentType, Date requestTime, Map map,
			Category category, String callerId) {

		logger.info("constructConsentUrlandHit is called...");
		String bsnlEastCGUrl = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION",
				"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL_EAST", null);
		
		
		//Get mode wise return url for BSNL East.
		String bsnlEastCommonRUrl = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION",
				"DOUBLE_CONFIRMATION_CONSENT_RURL", null);
		
		//If mode wise rurl is not configured then fallback to bsnl east conmmon rurl. 
		String bsnlEastRUrl = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION",
				"DOUBLE_CONFIRMATION_CONSENT_RURL_" + source.toUpperCase(), bsnlEastCommonRUrl);
		
		
		List<String> nameTunes = new ArrayList<String>();
		String nameTune = RBTParametersUtils.getParamAsString("COMMON",
				"BLOCKED_ALBUMS_FOR_PROMPT", null);
		List<String> configuredModes = new ArrayList<String>();
		String configuredMode = RBTParametersUtils.getParamAsString("COMMON",
				"MODES_TO_REPLACE_METHOD_NAME_IN_CG_URL", null);
		if (nameTune != null && !nameTune.trim().isEmpty()) {
			nameTunes = Arrays.asList(((nameTune.trim().toLowerCase())
					.split("\\s*,\\s*")));
		}
		boolean isModeConfigured = false;
		if (configuredMode != null && !configuredMode.trim().isEmpty()) {
			configuredModes = Arrays.asList(((configuredMode.trim()
					.toLowerCase()).split("\\s*,\\s*")));
			isModeConfigured = (source != null && configuredModes
					.contains(source.toLowerCase()));
		}
		String bsnlSouthCGUrl = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION",
				"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL_SOUTH", null);
		String modeMappingStr = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "TPCG_MODES_MAP", null);
		String methodDefaultValue = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION",
				"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL_METHOD_DEFAULT_VALUE",
				null);
		logger.info("modeMappingStr=" + modeMappingStr);
		String response = null;
		String vcode = null;
		String cgURL = null;
		HttpResponse httpResponse = null;
		// HttpParameters httpParameters = null;
		boolean isActRequest = false;
		boolean isSelRequest = false;
		boolean isActAndSelRequest = false;
		boolean isCategoryShuffle = false;
		if (category != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getType())) {
			isCategoryShuffle = true;
		}
		if (consentType.equalsIgnoreCase("ACT")) {
			isActRequest = true;
		} else if (consentType.equalsIgnoreCase("SEL")) {
			isSelRequest = true;
		} else if (consentType.equalsIgnoreCase("ACTSEL")) {
			isActAndSelRequest = true;
		}
		map.put("CONSENTSUBCLASS", subClass);
		String languageCode = null;
		String language = null;
		if (map.containsKey("LANGUAGE_CODE")) {
			languageCode = (String) map.get("LANGUAGE_CODE");
			map.remove("LANGUAGE_CODE");
		}
		logger.info("LANGUAGE_CODE=" + languageCode);
		language = getLanguageString(languageCode);
		logger.info("language from mapping =" + language);
		consentType = "ACT";
		if (null != modeMappingStr) {
			Map<String, String> modeMapping = MapUtils.convertToMap(
					modeMappingStr, ";", "=", ",");
			source = modeMapping.get(source);
			consent_source = source;
		}
		if (null != bsnlEastCGUrl) {
			bsnlEastCGUrl = replaceParam("%subscription_id%", tid,
					bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%tid%", tid, bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%source%", source, bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%consent_source%", consent_source,
					bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%msisdn%", msisdn, bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%service_id%", baseServiceID,
					bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%content_service_id%",
					selectionServiceID, bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%charge%", charge, bsnlEastCGUrl);
			bsnlEastCGUrl = replaceParam("%language%", language, bsnlEastCGUrl);
			if (null != clipObj) {
				String albumName = clipObj.getAlbum();
				if (category != null && isCategoryShuffle) {
					logger.info("Clip is not null and category is shuffle so replacing content_id with : "
							+ category.getPromoID()
							+ "  and content_name with : " + category.getName());

					bsnlEastCGUrl = replaceParam("%content_id%",
							String.valueOf(category.getPromoID()),
							bsnlEastCGUrl);
					bsnlEastCGUrl = replaceParam("%content_name%",
							String.valueOf(category.getName()), bsnlEastCGUrl);

				} else {
					bsnlEastCGUrl = replaceParam("%content_id%",
							String.valueOf(clipObj.getClipPromoId()),
							bsnlEastCGUrl);
					bsnlEastCGUrl = replaceParam("%content_name%",
							String.valueOf(clipObj.getClipName()),
							bsnlEastCGUrl);
				}
				if (isModeConfigured
						&& albumName != null
						&& (nameTunes.contains(albumName.toLowerCase().trim()) || (albumName
								.toLowerCase())
								.contains(WebServiceConstants.CORPORATE_TUNES
										.toLowerCase()))) {
					bsnlEastCGUrl = replaceParam("%new_subscription%",
							WebServiceConstants.NAME_TUNES, bsnlEastCGUrl);
				} else {
					bsnlEastCGUrl = replaceParam("%new_subscription%",
							methodDefaultValue, bsnlEastCGUrl);
				}
			} else {

				bsnlEastCGUrl = replaceParam("%content_id%", "", bsnlEastCGUrl);
				bsnlEastCGUrl = replaceParam("%content_name%", "",
						bsnlEastCGUrl);
				bsnlEastCGUrl = replaceParam("%new_subscription%",
						methodDefaultValue, bsnlEastCGUrl);
			}
			
			
			//Replace rulr replace holder by configured rurl.
			bsnlEastCGUrl = replaceParam("%rurl%",bsnlEastRUrl, bsnlEastCGUrl);
			
//			String bsnlEastWapRedirectionUrl = RBTParametersUtils
//					.getParamAsString("COMMON",
//							"BSNL_EAST_WAP_REDIRECTION_URL", null);
//			String bsnlEastWebRedirectionUrl = RBTParametersUtils
//					.getParamAsString("COMMON",
//							"BSNL_EAST_WEB_REDIRECTION_URL", null);
//			if (null != source && source.equalsIgnoreCase("WAP"))
//				bsnlEastCGUrl = replaceParam("%url%",
//						bsnlEastWapRedirectionUrl, bsnlEastCGUrl);
//			else if (null != source && source.equalsIgnoreCase("WEB"))
//				bsnlEastCGUrl = replaceParam("%url%",
//						bsnlEastWebRedirectionUrl, bsnlEastCGUrl);

			if (bsnlEastCGUrl != null) {
				map.put("CGURL", bsnlEastCGUrl);
				cgURL = bsnlEastCGUrl;
			}

			logger.info("Consent url: " + bsnlEastCGUrl);
		} else if (null != bsnlSouthCGUrl) {
			bsnlSouthCGUrl = replaceParam("%MSISDN%", msisdn, bsnlSouthCGUrl);
			bsnlSouthCGUrl = replaceParam("%MODE%", consent_source,
					bsnlSouthCGUrl);
			bsnlSouthCGUrl = replaceParam("%TRANS_ID%", tid, bsnlSouthCGUrl);
			bsnlSouthCGUrl = replaceParam("%CIRCLE_ID%", circleId,
					bsnlSouthCGUrl);
			bsnlSouthCGUrl = replaceParam("%REQ_TYPE%", consentType,
					bsnlSouthCGUrl);
			if (null != clipObj) {
				bsnlSouthCGUrl = replaceParam("%CONTENT_ID%",
						String.valueOf(clipObj.getClipPromoId()),
						bsnlSouthCGUrl);
				bsnlSouthCGUrl = replaceParam("%SONG_NAME%",
						String.valueOf(clipObj.getClipName()), bsnlSouthCGUrl);
				if (clipObj.getClipName() != null) {
					bsnlSouthCGUrl = replaceParam("%SONG_NAME%",
							String.valueOf(clipObj.getClipName()),
							bsnlSouthCGUrl);
					bsnlSouthCGUrl += "&Songname="
							+ clipObj.getClipName().replace(" ", "+");
				}
				vcode = getVcode(clipObj, wavFile);
				if (vcode != null) {
					bsnlSouthCGUrl = replaceParam("%VCODE%", vcode,
							bsnlSouthCGUrl);
				}
				String albumName = clipObj.getAlbum();
				if (isModeConfigured
						&& albumName != null
						&& (nameTunes.contains(albumName.toLowerCase()) || (albumName
								.toLowerCase())
								.contains(WebServiceConstants.CORPORATE_TUNES
										.toLowerCase()))) {
					bsnlSouthCGUrl = replaceParam("%new_subscription%",
							WebServiceConstants.NAME_TUNES, bsnlSouthCGUrl);
				} else {
					bsnlSouthCGUrl = replaceParam("%new_subscription%",
							methodDefaultValue, bsnlSouthCGUrl);
				}

			} else {
				bsnlSouthCGUrl = replaceParam("%new_subscription%",
						methodDefaultValue, bsnlSouthCGUrl);
				bsnlSouthCGUrl = replaceParam("%CONTENT_ID%", "NA",
						bsnlSouthCGUrl);
				bsnlSouthCGUrl = replaceParam("%SONG_NAME%", "NA",
						bsnlSouthCGUrl);

			}
			String param1 = "";
			String loop_Status = String.valueOf(loopStatus);
			if(isActRequest){
				param1="";
			}else if(isActAndSelRequest){
				param1="param1=0";
			}else if (null != loop_Status && loop_Status.equalsIgnoreCase("l")) {
				param1 = "param1=2";
			} else if (null != loop_Status && loop_Status.equalsIgnoreCase("o")) {
				param1 = getLoopStatusForSelection(msisdn, callerId);
			}
			bsnlSouthCGUrl = replaceParam("%PARAM1%", param1, bsnlSouthCGUrl);
			String serviceId = DoubleConfirmationConsentPushThread
					.getServiceValue("SERVICE_ID", subClass, classType,
							circleId, isActRequest, isSelRequest,
							isActAndSelRequest);
			bsnlSouthCGUrl = replaceParam("%SERVICE_ID%", serviceId,
					bsnlSouthCGUrl);
			String requestDateFormat = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "DATE_FORMAT_OF_REQUEST_TIME",
					"yyMMddHHmmss");
			SimpleDateFormat RequestTimeDateFormat = null;
			RequestTimeDateFormat = new SimpleDateFormat(requestDateFormat);
			String requestTimeStr = null;
			if (null != requestTime) {
				requestTimeStr = RequestTimeDateFormat.format(requestTime);
			}
			bsnlSouthCGUrl = replaceParam("%REQUESTTIMESTAMP%", requestTimeStr,
					bsnlSouthCGUrl);
			logger.info("Consent url: " + bsnlSouthCGUrl);

			if (bsnlSouthCGUrl != null) {
				map.put("CGURL", bsnlSouthCGUrl);
				cgURL = bsnlSouthCGUrl;
			}
		}
		if (rbtHttpClient != null) {
			try {
				httpResponse = rbtHttpClient.makeRequestByGet(cgURL, null);
				logger.info("RBT:: httpResponse: " + httpResponse);
				logger.info("rbtHttpClient... " + rbtHttpClient.hashCode());
				response = httpResponse.getResponse();
				logger.info("RBT::response Consent url is:--> " + response);
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				map.put("CGHttpResponse", "Connection/Socket TimeOut Error");
			}
		}
		if (httpResponse != null)
			map.put("CGHttpResponse", httpResponse.getResponse());

		return httpResponse;
	}

	private String getVcode(Clip clipObj, String wavFileName) {
		if (clipObj == null)
			return null;
		String vcode = null;
		String wavFile = null;
		if (clipObj.getClipId() != -1) {
			if (clipObj != null) {
				wavFile = clipObj.getClipRbtWavFile();
			}
		}
		if (wavFile == null) {
			wavFile = wavFileName;
		}
		if (wavFile != null) {
			vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
		}
		return vcode;
	}

	private String replaceParam(String param, String target,
			String bsnlEastCGUrl) {
		if (null != target)
			bsnlEastCGUrl = bsnlEastCGUrl.replace(param, target);
		else
			bsnlEastCGUrl = bsnlEastCGUrl.replace(param, "");
		return bsnlEastCGUrl;
	}

	/**
	 * Third Party confirmation chages
	 * 
	 * @param subscriberID
	 * @param mode
	 * @param startDate
	 * @param endDate
	 * @param isDirectActivation
	 * @param rbtType
	 * @param conn
	 * @param prepaid
	 * @param subscription
	 * @param activationInfo
	 * @param cosID
	 * @param subscriptionClass
	 * @param refId
	 * @param isComboRequest
	 *            TODO
	 * @return
	 */
	protected Subscriber checkModeAndInsertIntoConsent(String subscriberID,
			String mode, Date startDate, Date endDate,
			boolean isDirectActivation, int rbtType, Connection conn,
			String prepaid, String subscription, String activationInfo,
			String cosID, String subscriptionClass, String refId,
			HashMap<String, String> extraInfoMap, String circleId,
			boolean isComboRequest, Map<String, String> xtraParametersMap)
			throws OnMobileException {
		Subscriber subscriber = null;
		String language = com.onmobile.apps.ringbacktones.webservice.common.Utility
				.getLanguageCode(null);
		logger.info("Checking mode for third party confirmation. subscriber: "
				+ subscriberID + ", mode: " + mode);
		if (Utility.isThirdPartyConfirmationRequired(mode, extraInfoMap)) {

			// RBT-9873 Added for bypassing CG flow
			if (Utility
					.isSubscriptionClassConfiguredForNotCGFlow(subscriptionClass)) {
				return null;
			}

			// For Enabling UDS_OPTIN through Consent Model for New user
			if (xtraParametersMap != null) {
				if (xtraParametersMap
						.containsKey(WebServiceConstants.param_isUdsOn)) {
					String udsOn = xtraParametersMap
							.get(WebServiceConstants.param_isUdsOn);
					if (udsOn.equalsIgnoreCase("true")) {
						if (extraInfoMap == null) {
							extraInfoMap = new HashMap<String, String>();
						}
						extraInfoMap.put(UDS_OPTIN, "TRUE");
					}
				} else if (xtraParametersMap.containsKey("LANGUAGE_CODE")) {
					language = xtraParametersMap.get("LANGUAGE_CODE");
				}
			}

			logger.info("Diverting normal flow for subscription, insert into Consent"
					+ " table for subscriber: " + subscriberID);

			String consentUniqueId = Utility
					.generateConsentIdRandomNumber(subscriberID);
			if (consentUniqueId != null) {
				refId = consentUniqueId;
			}

			if (null == refId) {
				refId = UUID.randomUUID().toString();
			}
			boolean isPresent = false;

			// For Uninor flow, if the configuration is configured as FALSE and
			// user already sends request for Base, then consent id will not
			// share
			String makeActUserEntryConsentTable = RBTParametersUtils
					.getParamAsString(COMMON,
							"MAKE_ENTRY_CONSENT_ACT_USER_SEL", "TRUE");
			if (!RBTParametersUtils.getParamAsBoolean(DOUBLE_CONFIRMATION,
					"ACCEPT_MULTIPLE_BASE_CONSENT", "FALSE"))
				isPresent = ConsentTableImpl.isSameConsentActivationRequest(
						conn, subscriberID, null, "0,1,2");

			// if("FALSE".equalsIgnoreCase(makeActUserEntryConsentTable) &&
			// isPresent) {
			// refId = null;
			// }
			if (isPresent) {
				refId = null;
			}

			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			if (!isPresent) {
				int consentStatus = 0;
				List<String> modesForNotConsentHit = Arrays
						.asList(RBTParametersUtils.getParamAsString(
								"DOUBLE_CONFIRMATION",
								"MODES_FOR_NOT_CONSENT_HIT", "").split(","));
				
				
				List<String> subsrvKeyConsentHit = Arrays
						.asList(RBTParametersUtils.getParamAsString(
								"DOUBLE_CONFIRMATION", "SUB_SRV_KEY_CONSENT_HIT",
								"").split(","));
				logger.info("--> content in SUB_SRV_KEY_CONSENT_HIT "+subsrvKeyConsentHit);
				if (mode != null
						&& modesForNotConsentHit.contains(mode.toUpperCase())) {
					consentStatus = 1;
					if(subsrvKeyConsentHit.contains(subscriptionClass.toUpperCase())) //reset consent status if free srv key present
					{
						consentStatus = 0;
						//baseConsentStatus = 0;
						logger.info("-->  RESETING CONSENT STATUS , subscription class : " + subscriptionClass.toUpperCase());
					}
					
				}
				xtraParametersMap.put("CONSENTSTATUS", String.valueOf(consentStatus));


				if (isComboRequest) {
					consentStatus = -1;
					Map<String, String> mappedModeMap = MapUtils.convertToMap(
							RBTParametersUtils.getParamAsString(
									"DOUBLE_CONFIRMATION",
									"SWAPPED_MODES_MAPPING_FOR_CONSENT", ""),
							";", "=", ",");
					if (mode != null && mappedModeMap != null
							&& mappedModeMap.containsKey(mode.toUpperCase())) {
						mode = mappedModeMap.get(mode.toUpperCase());
					}

				}
				Date requestTime = null;
				int secondsToBeAddedInRequestTime = DBUtility
						.secondsToBeAddedInRequestTime(circleId, mode);
				if (secondsToBeAddedInRequestTime != -1) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, secondsToBeAddedInRequestTime);
					requestTime = cal.getTime();
				}
				String agentId = null;
				if (xtraParametersMap != null
						&& xtraParametersMap
								.containsKey(Constants.param_agentId)) {
					agentId = xtraParametersMap.get(Constants.param_agentId);
				}
				boolean success = ConsentTableImpl.insertSubscriptionRecord(
						conn, subscriberID, mode, startDate, endDate,
						activationInfo, prepaid, subscriptionClass, cosID,
						rbtType, isDirectActivation, extraInfo, circleId,
						refId, consentStatus, false, requestTime, agentId,
						language);
				boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
						.getParamAsBoolean(COMMON,
								"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
				if (isCGIntegrationFlowForBsnlEast && !isComboRequest) {
					logger.info("Checking mode cg integration flow: "
							+ subscriberID + ", mode: " + mode);
					String baseServiceID = DoubleConfirmationConsentPushThread
							.getServiceValue("SERVICE_ID", subscriptionClass,
									null, circleId, true, false, false);
					SubscriptionClass subClass = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(subscriptionClass);
					SubscriptionRequest subscriberRequest = getSubscriptionRecordForTransID(refId);
					HttpResponse httpResponse = constructConsentUrlandHit(
							refId, mode, mode, subscriberID, null,
							baseServiceID, null,
							subClass.getSubscriptionAmount(), circleId, null,
							's', subscriptionClass, null, "ACT",
							subscriberRequest.getRequestTime(),
							xtraParametersMap, null,null);
					if (httpResponse != null
							&& httpResponse.getResponseCode() == 200) {
						String response = httpResponse.getResponse();
						logger.info("response value is " + response);
						String[] tokens = response.split(":");
						if (null == tokens || tokens.length < 3) {
							tokens = response.split("\\|");
						}
						if (null != tokens
								&& tokens.length >= 3
								&& (tokens[1].equalsIgnoreCase("0") || tokens[1]
										.equalsIgnoreCase("Accepted"))) {
							subscriber = SubscriberImpl.getSubscriber(conn,
									subID(subscriberID));
							if (extraInfo != null) {
								HashMap<String, String> xtraInfoMap = DBUtility
										.getAttributeMapFromXML(extraInfo);
								xtraInfoMap.put(EXTRA_INFO_CG_SUBSCRIPTION_ID,
										refId);
								if (tokens[1].equalsIgnoreCase("Accepted")) {
									xtraInfoMap.put(EXTRA_INFO_TRANS_ID,
											tokens[2]);
								} else {
									xtraInfoMap.put(EXTRA_INFO_TRANS_ID,
											tokens[0]);
								}
								extraInfo = DBUtility
										.getAttributeXMLFromMap(xtraInfoMap);
							} else {
								logger.info("extraInfo is null : " + extraInfo);
								HashMap<String, String> xtraInfoMap = new HashMap<String, String>();
								xtraInfoMap.put(EXTRA_INFO_CG_SUBSCRIPTION_ID,
										refId);
								if (tokens[1].equalsIgnoreCase("Accepted")) {
									xtraInfoMap.put(EXTRA_INFO_TRANS_ID,
											tokens[2]);
								} else {
									xtraInfoMap.put(EXTRA_INFO_TRANS_ID,
											tokens[0]);
								}
								extraInfo = DBUtility
										.getAttributeXMLFromMap(xtraInfoMap);
								logger.info("extraInfo is : " + extraInfo);
							}

							if (subscriber != null) {
								success = SubscriberImpl.update(conn,
										subID(subscriberID), mode, null,
										startDate, m_endDate, prepaid, null,
										null, 0, activationInfo,
										subscriptionClass, null, null, null,
										subscription, 0, cosID, cosID, rbtType,
										subscriber.language(), extraInfo,
										isDirectActivation, circleId, UUID
												.randomUUID().toString());
								subscriber = SubscriberImpl.getSubscriber(conn,
										subID(subscriberID));
							} else {
								subscriber = SubscriberImpl.insert(conn,
										subID(subscriberID), mode, null,
										startDate, m_endDate, prepaid, null,
										null, 0, activationInfo,
										subscriptionClass, null, null, null,
										subscription, 0, cosID, cosID, rbtType,
										null, isDirectActivation, extraInfo,
										circleId, null);
							}

							success = deleteConsentRequestByTransIdAndMSISDN(
									refId, subscriberID);
							if (success && xtraParametersMap != null) {
								xtraParametersMap.put(
										"CONSENT_SUBSCRIPTION_INSERT", "TRUE");
								xtraParametersMap.put("DESCRIPTION", tokens[2]);
							}
							return subscriber;
						} else {
							success = deleteConsentRequestByTransIdAndMSISDN(
									refId, subscriberID);
							logger.info("cg hit failed and response is  "
									+ response);
							xtraParametersMap
									.put("CONSENT_HIT_FAILURE_FOR_BSNL_EAST",
											"TRUE");
							return null;
						}
					} else {
						success = deleteConsentRequestByTransIdAndMSISDN(refId,
								subscriberID);
						logger.info("cg hit failed ");
						xtraParametersMap.put(
								"CONSENT_HIT_FAILURE_FOR_BSNL_EAST", "TRUE");
						return null;
					}
				}
				if (success && xtraParametersMap != null) {
					xtraParametersMap
							.put("CONSENT_SUBSCRIPTION_INSERT", "TRUE");
					if (extraInfo != null) {
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						extraInfo = DBUtility
								.getAttributeXMLFromMap(xtraInfoMap);
						logger.info("extraInfo is : " + extraInfo);
						String consentId = (String) xtraInfoMap
								.remove("CONSENTID");
						String consentClassType = (String) xtraInfoMap
								.remove("CONSENTCLASSTYPE");
						xtraParametersMap.put("CONSENTID", consentId);
						xtraParametersMap.put("CONSENTCLASSTYPE",
								consentClassType);
					}
					language = com.onmobile.apps.ringbacktones.webservice.common.Utility
							.getLanguageCode(language);
					String planId = com.onmobile.apps.ringbacktones.webservice.common.Utility
							.getPlanId(subscriptionClass);
					
					xtraParametersMap.put("EVENT_TYPE", "2");
					xtraParametersMap.put("CONSENTSUBCLASS", subscriptionClass);
					xtraParametersMap.put("LANGUAGE_ID", language);
					xtraParametersMap.put("PLAN_ID", planId);
					xtraParametersMap.put("CONSENTSTATUS",String.valueOf(consentStatus));
					logger.info("--> consentstatus at end "+consentStatus);	
					
				}
			}
			subscriber = new SubscriberImpl(subID(subscriberID), mode, null,
					startDate, endDate, prepaid, null, null, 0, activationInfo,
					subscriptionClass, subscription, null, null, null, 0,
					cosID, null, rbtType, null, null, extraInfo, circleId,
					refId);
		}
		return subscriber;
	}

	/**
	 * @param subscriberId
	 * @param subscriberWavFile
	 * @param endDate
	 * @param isSubActive
	 * @param mode
	 * @param selectionInfo
	 * @param isSmClientModel
	 * @param conn
	 * @param categoryID
	 * @param categoryType
	 * @param nextClass
	 * @param extraInfo
	 * @return
	 * @throws OnMobileException
	 */
	protected SubscriberDownloads checkModeAndInsertIntoConsent(
			String subscriberId, String subscriberWavFile, Date endDate,
			boolean isSubActive, String mode, String selectionInfo,
			boolean isSmClientModel, Connection conn, int categoryID,
			int categoryType, String nextClass,
			HashMap<String, String> extraInfoMap, String baseConsentId,
			boolean useUIChargeClass) throws OnMobileException {
		SubscriberDownloads subscriberDownloads = null;
		logger.info("Checking mode for third party confirmation. subscriber: "
				+ subscriberId + ", mode: " + mode);

		String callerId = null, interval = null, loopStatus = null;
		int status = 1, fromTime = 0, toTime = 2359;

		if (null != extraInfoMap) {
			callerId = extraInfoMap.remove("CALLER_ID");
			status = Integer
					.parseInt(extraInfoMap.get("STATUS") != null ? extraInfoMap
							.remove("STATUS") : "1");
			fromTime = Integer
					.parseInt(extraInfoMap.get("FROM_TIME") != null ? extraInfoMap
							.remove("FROM_TIME") : "0");
			toTime = Integer
					.parseInt(extraInfoMap.get("TO_TIME") != null ? extraInfoMap
							.remove("TO_TIME") : "2359");
			interval = extraInfoMap.remove("INTERVAL");
			loopStatus = extraInfoMap.remove("LOOP_STATUS");
		}

		boolean inLoop = "L".equalsIgnoreCase(loopStatus);

		String parentTransId = null;

		if (Utility.isThirdPartyConfirmationRequired(mode, extraInfoMap)
				&& getInstance().checkUserStatuskForConsentFlow(
						getSubscriber(subscriberId))) {
			logger.info("Diverting normal flow for download, insert into Consent"
					+ " table for subscriber: " + subscriberId);
			String refId = Utility.generateConsentIdRandomNumber(subscriberId);
			if (refId == null) {
				refId = UUID.randomUUID().toString();
			}

			parentTransId = refId;

			Calendar calendar = Calendar.getInstance();
			Date startDate = calendar.getTime();

			if (endDate == null) {
				calendar.set(2037, 11, 31, 0, 0, 0);
				endDate = calendar.getTime();
			}

			int consentStatus = 0;
			int baseConsentStatus = 0;
			List<String> modesForNotConsentHit = Arrays
					.asList(RBTParametersUtils.getParamAsString(
							"DOUBLE_CONFIRMATION", "MODES_FOR_NOT_CONSENT_HIT",
							"").split(","));
			List<String> songsrvKeyConsentHit = Arrays
					.asList(RBTParametersUtils.getParamAsString(
							"DOUBLE_CONFIRMATION", "SONG_SRV_KEY_CONSENT_HIT",
							"").split(","));
			logger.info("--> VALUE IN SONG_SRV_KEY_CONSENT_HIT PARAMTER"+songsrvKeyConsentHit+"NEXT CLASS"+nextClass.toUpperCase());
			if (mode != null
					&& modesForNotConsentHit.contains(mode.toUpperCase())) {
				consentStatus = 1;
				baseConsentStatus = 1;
				
				if(songsrvKeyConsentHit.contains(nextClass.toUpperCase())) //reset consent status if free srv key present
				{
					consentStatus = 0;
					logger.info("--> RESETING CONSENT STATUS SONG");
					baseConsentStatus = 0;
				}
				
				
			}
			extraInfoMap.put("CONSENTSTATUS", String.valueOf(consentStatus));
			if (baseConsentId != null) {
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = getDoubleConfirmationRequestBeanForStatus(
						null, baseConsentId, subscriberId, null, true);
				if (doubleConfirmReqBeans != null
						&& doubleConfirmReqBeans.size() > 0) {
					consentStatus = 1;
					DoubleConfirmationRequestBean doubleConfirmReqBean = doubleConfirmReqBeans
							.get(0);
					Map<String, String> subExtraInfoMap = DBUtility
							.getAttributeMapFromXML(doubleConfirmReqBean
									.getExtraInfo());
					if (subExtraInfoMap == null) {
						subExtraInfoMap = new HashMap<String, String>();
					}
					subExtraInfoMap.put("TRANS_ID", refId);
					String extraInfoXml = DBUtility
							.getAttributeXMLFromMap(subExtraInfoMap);
					updateConsentExtrInfoAndStatus(
							doubleConfirmReqBean.getSubscriberID(),
							baseConsentId, extraInfoXml, baseConsentStatus + "");
					Map<String, String> mappedModeMap = MapUtils.convertToMap(
							RBTParametersUtils.getParamAsString(
									"DOUBLE_CONFIRMATION",
									"SWAPPED_MODES_MAPPING_FOR_CONSENT", ""),
							";", "=", ",");
					if (mode != null
							&& mappedModeMap.containsKey(mode.toUpperCase())) {
						mode = mappedModeMap.get(mode.toUpperCase());
					}

					parentTransId = baseConsentId;
				}
			}

			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			boolean consentInsertedSuccessfull = ConsentTableImpl
					.insertSelectionRecord(conn, refId, subscriberId, callerId,
							String.valueOf(categoryID), null, mode, startDate,
							endDate, status, nextClass, null, null, null,
							interval, fromTime, toTime, selectionInfo, 0,
							inLoop, null, useUIChargeClass, categoryType, null,
							false, null, subscriberWavFile, 0, null, null,
							null, extraInfo, "DWN", consentStatus,null,null);
			logger.info("Successfully inserted download into consent "
					+ " table for subscriber: " + subscriberId + ", refId: "
					+ refId);

			String downloadStatus = "w";
			if (isSubActive) {
				downloadStatus = "n";
			}
			if (isSmClientModel) {
				downloadStatus = "p";
			}

			subscriberDownloads = new SubscriberDownloadsImpl(subscriberId,
					subscriberWavFile, downloadStatus.charAt(0), startDate,
					startDate, endDate, categoryID, null, categoryType,
					nextClass, mode, selectionInfo, parentTransId, extraInfo);

		}
		return subscriberDownloads;
	}

	/**
	 * Third Party confirmation chages
	 * 
	 * @param subscriberID
	 * @param callerID
	 * @param categoryID
	 * @param subscriberWavFile
	 * @param setTime
	 * @param startTime
	 * @param endTime
	 * @param status
	 * @param selectedBy
	 * @param selectionInfo
	 * @param nextChargingDate
	 * @param prepaid
	 * @param classType
	 * @param fromTime
	 * @param toTime
	 * @param sel_status
	 * @param categoryType
	 * @param loopStatus
	 * @param rbtType
	 * @param selInterval
	 * @param refID
	 * @param circleId
	 * @param conn
	 * @param extraInfo
	 * @param feedSubType
	 * @param nextPlus
	 * @param smActivation
	 * @param sub2
	 * @return
	 * @throws OnMobileException
	 */
	protected SubscriberStatus checkModeAndInsertIntoConsent(
			String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime,
			Date endTime, int status, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, String classType,
			int fromTime, int toTime, String sel_status, int categoryType,
			char loopStatus, int rbtType, String selInterval, String refID,
			String circleId, Connection conn,
			HashMap<String, String> selXtraInfoMap, boolean useUIChargeClass,
			String baseConsentId, String feedSubType, String subscriptionClass,
			boolean isConsentActRecordInserted, boolean isAllowPremiumContent,
			boolean isUdsOnRequest, String slice_duration,
			boolean modeCheckForVfUpgrade, String oldSubscriptionClass,
			boolean smActivation, int nextPlus, Subscriber sub2)
			throws OnMobileException {

		SubscriberStatus subscriberStatus = null;
		boolean isConsentByPassForOnlyActive = false;
		String selectedNewSubscriptionClass = null ;
	//	String upgradeNewSubscriptionClass = null ;
		boolean combo=false;
	//String SELECTED_SUBSCRIPTION_CLASS=null;
		
		if (selXtraInfoMap.containsKey("SELECTED_SUBSCRIPTION_CLASS")){
			selectedNewSubscriptionClass = (String ) selXtraInfoMap.remove("SELECTED_SUBSCRIPTION_CLASS");
			logger.info("-->Base subscription class"+selectedNewSubscriptionClass);
			
		}
		
		/*if (selXtraInfoMap.containsKey("UPGRADED_SUBSCRIPTION_CLASS"))
		{
			upgradeNewSubscriptionClass = (String ) selXtraInfoMap.remove("UPGRADED_SUBSCRIPTION_CLASS");
			logger.info("-->Upgrade Base subscription class"+upgradeNewSubscriptionClass);
		}*/
		if (Arrays.asList(
				RBTParametersUtils.getParamAsString(COMMON,
						"MODES_BYPASSING_CONSENT_GATEWAY_FOR_SELECTION", "")
						.split(",")).contains(selectedBy)) {
			Subscriber sub = getSubscriber(subscriberID);
			isConsentByPassForOnlyActive = sub != null ? sub.subYes()
					.equalsIgnoreCase("B") : false;
		}
		/*
		 * if(Utility.isThirdPartyConfirmationRequired(selectedBy, extraInfo) &&
		 * isConsentByPassForOnlyActive){ int baseConsentStatus = 0;
		 * List<String> modesForNotConsentHit =
		 * Arrays.asList(RBTParametersUtils.
		 * getParamAsString("DOUBLE_CONFIRMATION", "MODES_FOR_NOT_CONSENT_HIT",
		 * "").split(",")); if(selectedBy != null &&
		 * modesForNotConsentHit.contains(selectedBy.toUpperCase())) {
		 * baseConsentStatus = 1; } if(baseConsentId != null) {
		 * List<DoubleConfirmationRequestBean> doubleConfirmReqBeans =
		 * getDoubleConfirmationRequestBeanForStatus(null, baseConsentId,
		 * subscriberID, null, true); if(doubleConfirmReqBeans != null &&
		 * doubleConfirmReqBeans.size() > 0) { DoubleConfirmationRequestBean
		 * doubleConfirmReqBean = doubleConfirmReqBeans.get(0);
		 * updateConsentStatus(doubleConfirmReqBean.getSubscriberID(),
		 * baseConsentId, baseConsentStatus+""); } }
		 * 
		 * }else
		 */
		boolean isThirdPartyConfirmationReq = Utility.isThirdPartyConfirmationRequired(selectedBy, selXtraInfoMap);
		boolean isInlineReq = false;
		boolean isConsentReq = true;
		Integer inlineFlag = null;
		String inlineFlow = null;
		
		try {
			if(isThirdPartyConfirmationReq && isSubscriberStrictlyActive(sub2)) {
				if( !(RBTParametersUtils.getParamAsBoolean(iRBTConstant.DOUBLE_CONFIRMATION,
						"CG_INTEGRATION_FLOW_FOR_BSNL", "false") || RBTParametersUtils
						.getParamAsBoolean(COMMON,
								"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE")) ) {
					inlineFlow = Utility.getInlineFlow(classType, 1);
					if(inlineFlow != null) {
						if(inlineFlow.equalsIgnoreCase("0")) {
							isConsentReq = false;
						} else {
							inlineFlag = 0;
							isInlineReq = true;
							if(logger.isDebugEnabled()) {
								logger.debug("Inline set to true for: " + subscriberID + subscriberWavFile + classType);
							}
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Exception while doing consent check for " + subscriberID + " " + classType + ", falling back to daemon approach: " + e);
			inlineFlag = null;
			isInlineReq = false;
		}
		
		if ( isConsentReq && (((isThirdPartyConfirmationReq && !isConsentByPassForOnlyActive) || modeCheckForVfUpgrade)
				&& getInstance().checkUserStatuskForConsentFlow(
						getSubscriber(subscriberID))) ) {
			String consentUniqueId = Utility
					.generateConsentIdRandomNumber(subscriberID);
			if (consentUniqueId != null) {
				refID = consentUniqueId;
			}

			if (null == refID) {
				refID = UUID.randomUUID().toString();
			}

			boolean isInLoop = false;
			if (loopStatus == LOOP_STATUS_LOOP_INIT
					|| loopStatus == LOOP_STATUS_LOOP
					|| loopStatus == LOOP_STATUS_LOOP_FINAL) {
				isInLoop = true;
			}
			if (selXtraInfoMap == null) {
				selXtraInfoMap = new HashMap<String, String>();
			}
			if (isAllowPremiumContent) {
				selXtraInfoMap.put(
						WebServiceConstants.param_allowPremiumContent, "y");
			}

			if (isUdsOnRequest && !isConsentActRecordInserted) {
				selXtraInfoMap.put(UDS_OPTIN, "TRUE");
			}
			if (slice_duration != null) {
				selXtraInfoMap.put("slice_duration", slice_duration);
			}
			int consentStatus = 0;
			int baseConsentStatus = 0;
			List<String> modesForNotConsentHit = Arrays
					.asList(RBTParametersUtils.getParamAsString(
							"DOUBLE_CONFIRMATION", "MODES_FOR_NOT_CONSENT_HIT",
							"").split(","));
			
			List<String> songsrvKeyConsentHit = Arrays
					.asList(RBTParametersUtils.getParamAsString(
							"DOUBLE_CONFIRMATION", "SONG_SRV_KEY_CONSENT_HIT",
							"").split(","));
			
			
			List<String> subsrvKeyConsentHit = Arrays
					.asList(RBTParametersUtils.getParamAsString(
							"DOUBLE_CONFIRMATION", "SUB_SRV_KEY_CONSENT_HIT",
							"").split(","));
			
			logger.info("--> VALUES IN SONG_SRV_KEY_CONSENT_HIT PARAMTER  "+songsrvKeyConsentHit+"SELECTEDBY"+selectedBy);
			
			if(subsrvKeyConsentHit.contains(selectedNewSubscriptionClass))
			{
				combo=true;
				logger.info("Base will get priority");
			}
			
			
			
			if (selectedBy != null
					&& modesForNotConsentHit.contains(selectedBy.toUpperCase())) {
				consentStatus = 1;
				baseConsentStatus = 1;
				inlineFlag = null;
				isInlineReq = false;
				if(logger.isDebugEnabled()) {
					logger.debug("Inline set to false during modes for not consent for: " + subscriberID + subscriberWavFile + classType);
				}
				logger.info("--> CLASSTYPE "+classType);
				if(songsrvKeyConsentHit.contains(classType.toUpperCase())) //reset consent status if free srv key present
				{
					logger.info("-->RESETING CONSENT STATUS SONG");
					consentStatus = 0;
					baseConsentStatus = 0;
				}
				if(combo)
				{
					logger.info("--> Reseting as its a combo request");
					baseConsentStatus=0;
				}
				else
				{
					baseConsentStatus=1;
				}
				
			}
		//	selXtraInfoMap.put("CONSENTSTATUS", String.valueOf(consentStatus));
			// CG Integration Flow - Jira -12806
			boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.DOUBLE_CONFIRMATION,
					"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
			boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
					.getParamAsBoolean(COMMON,
							"ADD_BASE_CONSENT_ID_IN_EXTRA_INFO", "FALSE");
			boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
					.getParamAsBoolean(COMMON,
							"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
	
			int secondsToBeAddedInRequestTime = DBUtility
					.secondsToBeAddedInRequestTime(circleId, selectedBy);
			//Paid to paid upgrade - song selection to be enabled - RBT-17520
 		
			if (sub2 != null
					&& isSubscriberActive(sub2)
					&& selectedNewSubscriptionClass != null
					&& !(sub2.subscriptionClass())
							.equals(selectedNewSubscriptionClass)) {
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = getDoubleConfirmationRequestBeanByType(subscriberID, "UPGRADE", true);
				if (doubleConfirmReqBeans != null
						&& doubleConfirmReqBeans.size() > 0) {
					logger.info("--> Insideif before reset constant status to 1"+consentStatus);
					consentStatus = 1;
					DoubleConfirmationRequestBean doubleConfirmReqBean = doubleConfirmReqBeans
							.get(0);
					HashMap<String ,String > extraInfoMap  = getExtraInfoMap(doubleConfirmReqBean);
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("COS_ID")
							&& !useUIChargeClass) {
						String cosId = (String) extraInfoMap.get("COS_ID");
						CosDetails cos = CacheManagerUtil
								.getCosDetailsCacheManager()
								.getCosDetail(cosId);
						String cosClassType = getChargeClassFromCos(cos,
								sub2.maxSelections());
						if (cosClassType != null && !cosClassType.isEmpty()) {
							classType = cosClassType;
						}
						useUIChargeClass =true ;
					}
					if (extraInfoMap == null) {
						extraInfoMap = new HashMap<String, String>();
					}
					extraInfoMap.put("P2P_UPGRADE", "TRUE");
					String attributeXMLFromMap = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);
					doubleConfirmReqBean.setExtraInfo(attributeXMLFromMap);
					selectedBy = updateConsentBaseExtraInfo(doubleConfirmReqBean, refID, selectedBy, modeCheckForVfUpgrade, oldSubscriptionClass,baseConsentStatus + "");
					if (checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast
							|| addBaseConsentInSelExtraInfo) {
						selXtraInfoMap.put("TRANS_ID", doubleConfirmReqBean.getTransId());
						selXtraInfoMap.put("P2P_UPGRADE","TRUE");
					}
					isInlineReq = false;
					inlineFlag = null;
					if(logger.isDebugEnabled()) {
						logger.debug("Inline set to false during upgrade for: " + subscriberID + subscriberWavFile + classType);
					}
				}
				

			}else if (baseConsentId != null) {
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = getDoubleConfirmationRequestBeanForStatus(
						null, baseConsentId, subscriberID, null, true);
				if (doubleConfirmReqBeans != null
						&& doubleConfirmReqBeans.size() > 0) {
					logger.info("-->Insideif before reset constant status to 1"+consentStatus+"->"+doubleConfirmReqBeans);
					
					consentStatus = 1;
					
					DoubleConfirmationRequestBean doubleConfirmReqBean = doubleConfirmReqBeans
							.get(0);
					selectedBy = updateConsentBaseExtraInfo(doubleConfirmReqBean, refID, selectedBy, modeCheckForVfUpgrade, oldSubscriptionClass, baseConsentStatus + ""); 


					isInlineReq = false;
					inlineFlag = null;
					if(logger.isDebugEnabled()) {
						logger.debug("Inline set to false during base consent for: " + subscriberID + subscriberWavFile + classType);
					}
				}
				if (checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast
						|| addBaseConsentInSelExtraInfo) {
					selXtraInfoMap.put("TRANS_ID", baseConsentId);
				}
			}
			// CG Integration Flow - Jira -12806
			// else {
			// }
			// RBT-9873 Added for bypassing CG flow
			if (Utility.isChargeClassConfiguredForNotCGFlow(classType)
					&& !isConsentActRecordInserted) {
				return null;
			}
			String extraInfo = DBUtility.getAttributeXMLFromMap(selXtraInfoMap);
			Date requestTime = null;
			if (secondsToBeAddedInRequestTime != -1) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, secondsToBeAddedInRequestTime);
				requestTime = cal.getTime();
			}
			
			String agentId = null;
			if (selXtraInfoMap != null
					&& selXtraInfoMap
							.containsKey(Constants.param_agentId)) {
				agentId = selXtraInfoMap.get(Constants.param_agentId);
			}
			
			// rbt_type has to be send it to sel_type column- Jira RBT-
			// RBT-12645
			logger.info("-->  extraInfo"+extraInfo);
			
			
			boolean isConsentInserted = ConsentTableImpl.insertSelectionRecord(
					conn, refID, subscriberID, callerID,
					String.valueOf(categoryID), subscriptionClass, selectedBy,
					startTime, endTime, status, classType, null, null, null,
					selInterval, fromTime, toTime, selectionInfo, rbtType,
					isInLoop, null, useUIChargeClass, categoryType, null, true,
					feedSubType, subscriberWavFile, 0, circleId, null,
					requestTime, extraInfo, "SEL", consentStatus,agentId, inlineFlag);

			logger.info("Diverting normal flow for selection, "
					+ "inserted into Consent table for subscriber: "
					+ subscriberID + ", refId: " + refID
					+ ", isConsentInserted: " + isConsentInserted);

			if(isInlineReq && isConsentInserted) {
				extraInfo = DBUtility.setXMLAttribute(extraInfo, "INLINE_CG", inlineFlow);
			}
			
			subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID,
					categoryID, subscriberWavFile, setTime, startTime, endTime,
					status, classType, selectedBy, selectionInfo,
					nextChargingDate, prepaid, fromTime, toTime, sel_status,
					null, null, categoryType, loopStatus, rbtType, selInterval,
					refID, extraInfo, circleId, null);
			if (isCGIntegrationFlowForBsnlEast) {
				DoubleConfirmationRequestBean doubleConfirmReqBean = ConsentTableImpl
						.getRequestsByTransIdNMsisdnNStatus(conn,
								String.valueOf(consentStatus), subscriberID,
								refID);
				if (doubleConfirmReqBean != null) {
					subscriberStatus.setRequestTime(doubleConfirmReqBean
							.getRequestTime());
				}
			}
			logger.info("--> consentstatus at end "+consentStatus);
			selXtraInfoMap.put("CONSENTSTATUS", String.valueOf(consentStatus));
			selXtraInfoMap.put("COMBO", String.valueOf(combo));
			

			
		}
		return subscriberStatus;
	}

	public String updateSubscriberSelection(String subscriberId,
			String callerId, String subWavFile, Date setTime, int fromTime,
			int toTime, String selInterval, String selectedBy) {
		return "SUCCESS";
	}

	public SubscriberStatus[] getFreeActiveStatusSubscribers(int days, int day,
			String trial, int noOfRows) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus[] subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl
					.getFreeActiveStatusSubscribers(conn, days, day, trial,
							noOfRows);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public SubscriberDownloads[] getActivateNActPendingDownloads(
			String subscriberId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads[] subscriberDownloads = null;
		try {
			subscriberDownloads = SubscriberDownloadsImpl
					.getActivateNActPendingDownloads(conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberDownloads;
	}

	public SubscriberDownloads[] getAllVoluntarySuspendedDownloads(
			String subscriberId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads[] subscriberDownloads = null;
		try {
			subscriberDownloads = SubscriberDownloadsImpl
					.getAllVoluntarySuspendedDownloads(conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberDownloads;
	}

	public boolean upgradeToSongPack(Subscriber subscriber, String newCosId,
			String validity, String mode) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			if (subscriber == null)
				return false;
			String subscriberId = subscriber.subID();
			String numMaxSelection = subscriber.maxSelections() + "";
			String extraInfo = DBUtility.setXMLAttribute(
					subscriber.extraInfo(), "MAX_SEL", numMaxSelection);
			if (mode != null)
				extraInfo = DBUtility.setXMLAttribute(subscriber.extraInfo(),
						"PACK_MODE", mode);

			success = SubscriberImpl.upgradeToSongPack(conn, subscriberId,
					newCosId, extraInfo, validity);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return success;
	}

	public boolean isDownloadAllowed(String subscriberId) {
		return isDownloadAllowed(subscriberId, null);
	}

	public boolean isDownloadAllowed(String subscriberId, WebServiceContext task) {
		int maxDownloadsAllowed = 0;
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "MAX_DOWNLOADS_ALLOWED");

		if (param != null) {
			try {
				maxDownloadsAllowed = Integer.parseInt(param.getValue());
			} catch (NumberFormatException e) {
				logger.error("", e);
			}
		}

		if (maxDownloadsAllowed > 0) {
			SubscriberDownloads[] subDownloads = getNonDeactiveSubscriberDownloads(subscriberId);
			if (subDownloads != null
					&& subDownloads.length >= maxDownloadsAllowed)
				return false;
		}

		return true;
	}

	public SubscriberDownloads[] getDeactiveSubscriberDownloads(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getDeactiveSubscriberDownloads(conn,
					subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] getNonDeactiveSubscriberDownloads(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getNonDeactiveSubscriberDownloads(
					conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean smsSentForTrialSubscriber(String subscriberID,
			Date startDate, int day) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.smsSentForTrialSubscriber(conn,
					subID(subscriberID), startDate, day);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean setSelectionInfo(String subscriberID, String selinfo) {
		boolean success = false;
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriberID != null)
				success = SubscriberStatusImpl.setSelectionInfo(conn,
						subID(subscriberID), null, selinfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public boolean setSelectionInfo(String subscriberID, Date setTime,
			String selinfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			if (subscriberID != null)
				success = SubscriberStatusImpl.setSelectionInfo(conn,
						subID(subscriberID), setTime, selinfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public SubscriberStatus[] getNonFreeSelections(String subscriberID,
			String chargeClass) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getNonFreeSelections(conn,
					subID(subscriberID), chargeClass);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* time of the day changes */
	public void getActiveSubscriberStatus(String subscriberID, String callerID,
			String directory, int fromTime, int toTime) {
		getActiveSubscriberStatus(subID(subscriberID), callerID, directory,
				fromTime, toTime, null);
	}

	public void getActiveSubscriberStatus(String subscriberID, String callerID,
			String directory, int fromTime, int toTime, String circleID) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			String code = "0,90,99";
			StatusType[] statusTypes = StatusTypeImpl.getStatusTypes(conn);
			if (statusTypes != null) {
				String statusCode = "";
				for (int i = 0; i < statusTypes.length; i++)
					if (!statusTypes[i].showVUI())
						statusCode = statusTypes[i].code() + "," + statusCode;

				statusCode = statusCode.substring(0,
						statusCode.lastIndexOf(","));
				if (statusCode.length() > 0)
					code = statusCode;
			}
			if (directory != null) {
				SubscriberStatus subscriberStatus = SubscriberStatusImpl
						.getActiveSubscriberStatus(conn, subID(subscriberID),
								subID(callerID), code, fromTime, toTime);
				if (subscriberStatus != null) {
					if (subscriberStatus.categoryType() == RECORD) {
						File msg = new File(directory + File.separator
								+ subscriberStatus.subscriberFile());
						if (msg.exists())
							msg.delete();
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public SubscriberStatus[] getActiveSubscriberStatus(String subscriberID,
			String callerID, int fromTime, int toTime) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		SubscriberStatus[] subStatus = null;
		try {
			subStatus = SubscriberStatusImpl
					.getActiveSelOnCallerIdFromTimeToTime(conn, subscriberID,
							callerID, fromTime, toTime);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
		return subStatus;
	}

	public String getMonth(int month) {
		if (month < 0 || month > 11)
			month = 11;
		return monthsList[month];
	}

	public SubscriberStatus[] getSubscriberRecords(String subscriberID,
			String statusType, boolean smActivation) {
		return getSubscriberRecords(subID(subscriberID), statusType,
				smActivation, 0);
	}

	public SubscriberStatus[] getSubscriberRecords(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getSubscriberRecords(conn, subID(subID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public SubscriberStatus[] getSubscriberRecords(String subscriberID,
			String statusType, boolean smActivation, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			boolean showVUI = true;
			if (statusType != null && statusType.equalsIgnoreCase("GUI"))
				showVUI = false;

			String code = "0,90,99";
			StatusType[] statusTypes = StatusTypeImpl.getStatusTypes(conn);
			if (statusTypes != null) {
				String statusCode = "-1";
				for (int i = 0; i < statusTypes.length; i++) {
					if (showVUI && !statusTypes[i].showVUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
					if (!showVUI && !statusTypes[i].showGUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
				}

				if (statusCode.indexOf(",") > -1) {
					statusCode = statusCode.substring(0,
							statusCode.lastIndexOf(","));
					if (statusCode.length() > 0)
						code = statusCode;
				} else
					code = statusCode;
			}
			return SubscriberStatusImpl.getSubscriberRecords(conn,
					subID(subscriberID), code, smActivation, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getSubscriberRecords(String subscriberID,
			String statusType, String callerID, boolean smActivation,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			boolean showVUI = true;
			if (statusType != null && statusType.equalsIgnoreCase("GUI"))
				showVUI = false;

			String code = "0,90,99";
			StatusType[] statusTypes = StatusTypeImpl.getStatusTypes(conn);
			if (statusTypes != null) {
				String statusCode = "";
				for (int i = 0; i < statusTypes.length; i++) {
					if (showVUI && !statusTypes[i].showVUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
					if (!showVUI && !statusTypes[i].showGUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
				}
				statusCode = statusCode.substring(0,
						statusCode.lastIndexOf(","));
				if (statusCode.length() > 0)
					code = statusCode;
			}
			return SubscriberStatusImpl.getSubscriberRecords(conn,
					subID(subscriberID), callerID, code, smActivation, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getActiveSelectionBasedOnCallerId(
			String subscriberID, String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getActiveSelectionBasedOnCallerId(conn,
					subID(subscriberID), callerID);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	/* ADDED FOR TATA */
	public int getSubscriberMusicboxSelections(String subscriberID) {
		return getSubscriberMusicboxSelections(subID(subscriberID), 0);
	}

	public int getSubscriberMusicboxSelections(String subscriberID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		int result = 0;
		try {
			SubscriberStatus[] results = SubscriberStatusImpl
					.getAllSubscriberSelectionRecords(conn,
							subID(subscriberID), null, rbtType);
			if (results != null)
				for (int i = 0; i < results.length; i++)
					if (results[i].categoryType() == SHUFFLE)
						result++;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return result;
	}

	/* added for separate player db feature */
	public SubscriberStatus[] getAllActiveSubSelectionRecords(
			String subscriberID) {
		return getAllActiveSubSelectionRecords(subID(subscriberID), 0);
	}

	public SubscriberStatus[] getAllActiveSubSelectionRecords(
			String subscriberID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getAllActiveSubSelectionRecords(conn,
					subID(subscriberID), rbtType, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getAllActiveSelToUpdatePlayer(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getAllActiveSubSelectionRecords(conn,
					subID(subscriberID), 0, true);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* added for tone player for getting all subscriber selections by Sreenadh */
	public SubscriberStatus[] getAllSubSelectionRecordsForTonePlayer(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getAllSubSelectionRecordsForTonePlayer(
					conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	// TODO getAllSubSelectionRecordsForReactivation by Sreenadh
	public SubscriberStatus[] getAllSubSelectionRecordsForReactivation(
			String subscriberID, String callerID, String deSelectedBy,
			String setTime, int rbtType, int songStatus, int fromTime,
			int toTime, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getAllSubSelectionRecordsForReactivation(conn,
							subID(subscriberID), subID(callerID), deSelectedBy,
							setTime, rbtType, songStatus, fromTime, toTime,
							refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* For the latest Selection of the Subscriber */
	public SubscriberStatus getSubscriberLatestActiveSelection(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getSubscriberLatestActiveSelection(
					conn, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* Orders the result by CALLER_ID, STATUS, SET TIME */
	public SubscriberStatus[] getAllActiveSubscriberSettings(String subscriberID) {
		return getAllActiveSubscriberSettings(subID(subscriberID), 0);
	}

	public SubscriberStatus[] getAllActiveSubscriberSettings(
			String subscriberID, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getAllActiveSubscriberSettings(conn,
					subID(subscriberID), rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getAllActiveSubscriberSettingsbyStatus(
			String subscriberID, String rbtType,String  selstatus,String id) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getAllActiveSubscriberSettingsbyStatus(conn, subID(subscriberID), rbtType,selstatus,id);
					} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	
	
	
	
	public SubscriberStatus[] getAllNonDeactivatedSelectionRecords(
			String subscriberID) {
		return getAllNonDeactivatedSelectionRecords(subID(subscriberID), 0);
	}

	public SubscriberStatus[] getAllNonDeactivatedSelectionRecords(
			String subscriberID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getAllSubscriberSelectionRecordsNotDeactivated(conn,
							subID(subscriberID), null, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getAllSubscriberSelectionRecords(
			String subscriberID, String statusType) {
		return getAllSubscriberSelectionRecords(subID(subscriberID),
				statusType, 0);
	}

	public SubscriberStatus[] getAllSubscriberSelectionRecords(
			String subscriberID, String statusType, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			boolean showVUI = true;
			if (statusType != null && statusType.equalsIgnoreCase("GUI"))
				showVUI = false;

			String code = "0,90,99";
			StatusType[] statusTypes = StatusTypeImpl.getStatusTypes(conn);
			if (statusTypes != null) {
				String statusCode = "";
				for (int i = 0; i < statusTypes.length; i++) {
					if (showVUI && !statusTypes[i].showVUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
					if (!showVUI && !statusTypes[i].showGUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
				}
				statusCode = statusCode.substring(0,
						statusCode.lastIndexOf(","));
				if (statusCode.length() > 0)
					code = statusCode;
			}
			return SubscriberStatusImpl.getAllSubscriberSelectionRecords(conn,
					subID(subscriberID), code, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] smSubscriberRecords(String subscriberID,
			String code, boolean smActivation, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSubscriberRecords(conn,
					subID(subscriberID), code, smActivation, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getSubscriberDeactiveRecords(String subscriberID,
			String statusType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			boolean showVUI = true;
			if (statusType != null && statusType.equalsIgnoreCase("GUI"))
				showVUI = false;

			String code = "0,90,99";
			StatusType[] statusTypes = StatusTypeImpl.getStatusTypes(conn);
			if (statusTypes != null) {
				String statusCode = "";
				for (int i = 0; i < statusTypes.length; i++) {
					if (showVUI && !statusTypes[i].showVUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
					if (!showVUI && !statusTypes[i].showGUI())
						statusCode = statusTypes[i].code() + "," + statusCode;
				}
				statusCode = statusCode.substring(0,
						statusCode.lastIndexOf(","));
				if (statusCode.length() > 0)
					code = statusCode;
			}
			return SubscriberStatusImpl.getSubscriberDeactiveRecords(conn,
					subID(subscriberID), code);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int getCurrentSelections(String subscriberID, String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		try {
			String selectionType = null;
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(classType);
			if (chargeClass != null)
				selectionType = chargeClass.getSelectionType();

			if (selectionType != null) {
				if (selectionType.equalsIgnoreCase("SELECTIONS"))
					return SubscriberStatusImpl.getSubscriberCurrentSelections(
							conn, subID(subscriberID), classType);
				else
					return SubscriberStatusImpl.getSubscriberBouquetCount(conn,
							subID(subscriberID), classType)
							+ SubscriberStatusImpl.getSubscriberCount(conn,
									subID(subscriberID), classType);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	/* time of the day changes */
	public SubscriberStatus getActiveSubscriberRecord(String subscriberID,
			String callerID, int status, int fromTime, int toTime) {
		return getActiveSubscriberRecord(subID(subscriberID), callerID, status,
				fromTime, toTime, 0);
	}

	public SubscriberStatus getActiveSubscriberRecord(String subscriberID,
			String callerID, int status, int fromTime, int toTime, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getActiveSubscriberRecord(conn,
					subID(subscriberID), subID(callerID), status, fromTime,
					toTime, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus getActiveProfileSelections(String subscriberID,
			int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getActiveSubscriberRecordByStatus(conn,
					subscriberID, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getActiveSelectionsByType(String subscriberID,
			int selectionType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getActiveSelectionsByType(conn,
					subID(subscriberID), selectionType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean deactivateRealTimeSubscriberRecords(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			String setDate, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateRealTimeSubscriberRecords(
					conn, subID(subscriberID), subID(callerID), status,
					fromTime, toTime, "REAL", setDate, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriberRecords(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy) {
		return deactivateSubscriberRecords(subID(subscriberID), callerID,
				status, fromTime, toTime, smDeactivation, deactBy, 0);
	}

	public String deactivateSubscriberRecordsByStatus(String subscriberID,
			int status, String deSelectedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.deactivateSubscriberRecordsByStatus(
					conn, subscriberID, status, deSelectedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String deactivateSubscriberRecordsByStatusAndAllCaller(
			String subscriberID, int status, String deSelectedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl
					.deactivateSubscriberRecordsByStatusAndAllCaller(conn,
							subscriberID, status, deSelectedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String deactivateSubscriberRecordsNotInStatus(String subscriberID,
			String status, String deSelectedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl
					.deactivateSubscriberRecordsNotInStatus(conn, subscriberID,
							status, deSelectedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean deactivateSubscriberRecords(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberRecords(conn,
					subID(subscriberID), subID(callerID), status, fromTime,
					toTime, smDeactivation, deactBy, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriberSelections(String subscriberID,
			Map<String, String> updateClauseMap,
			Map<String, String> whereClauseMap) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberSelections(conn,
					subscriberID, updateClauseMap, whereClauseMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriberRecordWavFile(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy, String wavFile,
			String selInterval) {
		return deactivateSubscriberRecordWavFile(subID(subscriberID), callerID,
				status, fromTime, toTime, smDeactivation, deactBy, wavFile,
				selInterval, 0);
	}

	public boolean deactivateSubscriberRecordWavFile(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy, String wavFile,
			String selInterval, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberRecordWavFile(conn,
					subID(subscriberID), subID(callerID), status, fromTime,
					toTime, smDeactivation, deactBy, wavFile, selInterval,
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public void deactivateSubscriberRecords(String subscriberID, String callerID) {
		deactivateSubscriberRecords(subscriberID, callerID, 0);
	}

	public void deactivateSubscriberRecords(String subscriberID,
			String callerID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberStatusImpl.deactivateSubscriberRecords(conn,
					subID(subscriberID), subID(callerID), rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public Subscriber activateSubscriber(Connection conn, String subscriberID,
			String activate, Date date, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, boolean isDirectActivation,
			int rbtType) {
		if (conn == null)
			return null;

		logger.info("Activating subscriber, subscriberId: " + subscriberID);

		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";

		Date endDate = null;
		Subscriber subscriber = null;
		try {
			if (activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO")) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, -1);
				endDate = endCal.getTime();
			} else {
				if (endDate == null)
					endDate = m_endDate;
			}

			Date nextChargingDate = null;
			Date lastAccessDate = null;
			Date activationDate = null;
			String subscription = "A";
			if (isDirectActivation) {
				subscription = "B";
			}
			String activationInfo = actInfo;

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = null;
				List<String> viralSmsList = Arrays.asList("BASIC,CRICKET"
						.split(","));
				if (isViralSmsTypeListForNewTable(viralSmsList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null) {
					activationInfo = activationInfo + ":" + "viral";
				}
			}

			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
			if (subscriber != null) {
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {

						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;

							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);
						}
					}

					return subscriber;
				}

				if (!isDirectActivation
						&& (subsciptionYes.equals("D")
								|| subsciptionYes.equals("P")
								|| subsciptionYes.equals("F")
								|| subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes
									.equals("z"))) {
					// releaseConnection(conn);
					return null;
				}
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				String refID = UUID.randomUUID().toString();

				SubscriberImpl.update(conn, subID(subscriberID), activate,
						null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						deactivatedBy, deactivationDate, activationDate,
						subscription, 0, rbtType, isDirectActivation, refID);
				Date startDate = date;
				if (date == null)
					startDate = new Date(System.currentTimeMillis());

				subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate,
						activationDate, 0, null, null, rbtType, null,
						subscriber.oldClassType(), null, null, refID);
			} else {

				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						null, null, activationDate, subscription, 0, null,
						null, rbtType, null, isDirectActivation, null, null,
						null);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	/* ADDED FOR TATA */
	public boolean checkMBSettingExistsForCallerId(String subscriberID,
			String callerId) {
		return checkMBSettingExistsForCallerId(subID(subscriberID), callerId, 0);
	}

	public boolean checkMBSettingExistsForCallerId(String subscriberID,
			String callerId, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.checkMBSettingExistsForCallerId(conn,
					subID(subscriberID), callerId, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean clipToBeUpdatedForTATA(SubscriberStatus cliptobeadded)// ,
																			// Date
																			// endTime)
	{
		return clipToBeUpdatedForTATA(cliptobeadded, 0);
	}

	public boolean clipToBeUpdatedForTATA(SubscriberStatus cliptobeadded,
			int rbtType)// , Date endTime)
	{

		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.clipToBeUpdatedForTATA(conn,
					cliptobeadded, rbtType);// , Date endTime)
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* subscription manager changes */
	public boolean smDeactivateSubscriber(String subscriberID,
			String deactivate, Date date, boolean delSelections,
			boolean sendToHLR, boolean smDeactivation, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (delSelections)
				SubscriberStatusImpl.smDeactivate(conn, subID(subscriberID),
						date, smDeactivation, type, null);

			return SubscriberImpl.smDeactivate(conn, subID(subscriberID),
					deactivate, date, sendToHLR, smDeactivation, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smDeactivateSelection(String subscriberID, Date date,
			boolean smDeactivation, String type, String consentId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.smDeactivate(conn, subID(subscriberID),
					date, smDeactivation, type, consentId);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriberForTATA(String subscriberId) {
		return deactivateSubscriberForTATA(subscriberId, 0);
	}

	public boolean deactivateSubscriberForTATA(String subscriberId, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			boolean count = SubscriberImpl.remove(conn, subscriberId);
			boolean selectionsCount = SubscriberStatusImpl.removeAllSelections(
					conn, subID(subscriberId), rbtType);
			boolean downloadCount = SubscriberDownloadsImpl
					.deleteSubscriberDownloads(conn, subID(subscriberId));
			List<String> viralSMSList = Arrays.asList("SERACH".split(","));
			boolean viralSMS = false;
			if (isViralSmsTypeListForNewTable(viralSMSList)) {
				viralSMS = ViralSMSNewImpl.remove(conn, subscriberId, "SEARCH");
			} else {
				viralSMS = ViralSMSTableImpl.remove(conn, subscriberId,
						"SEARCH");
			}
			boolean promo = PromoTableImpl.remove(conn, subscriberId);
			return count && selectionsCount && downloadCount && viralSMS
					&& promo;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void deactivateSubscriberRecords(String subscriberID) {
		deactivateSubscriberRecords(subID(subscriberID), 0);
	}

	public void deactivateSubscriberRecords(String subscriberID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberStatusImpl.deactivate(conn, subID(subscriberID), null,
					true, false, "DAEMON", rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public SubscriberStatus[] getAllPendingSettings(String subscriberId) {
		return getAllPendingSettings(subID(subscriberId), 0);
	}

	/* ADDED FOR TATA */
	public SubscriberStatus[] getAllPendingSettings(String subscriberID,
			int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getAllPendingSettings(conn,
					subID(subscriberID), rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	public boolean checkClipSettingForDefaultPending(
			SubscriberStatus[] subscriberStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriberStatus != null)
				for (int i = 0; i < subscriberStatus.length; i++)
					if (subscriberStatus[i].callerID() == null)
						if (subscriberStatus[i].categoryType() == DTMF_CATEGORY)
							return true;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean checkMBSettingForDefaultPending(
			SubscriberStatus[] subscriberStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			boolean settingExist = false;
			if (subscriberStatus != null)
				for (int i = 0; i < subscriberStatus.length; i++)
					if (subscriberStatus[i].callerID() == null)
						if (subscriberStatus[i].categoryType() == BOUQUET)
							return true;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/*
	 * added for airtel get subscriber vcode checks only in local db return
	 * string will be ALBUM for shuffle,vcode:catId for proper case,ERROR for
	 * any error DEFAULT in case subscriber registered but doesn't have a rbt(or
	 * default)
	 */
	public String getSubscriberDefaultVcode(String subID) {
		return getSubscriberDefaultVcode(subID, 0);
	}

	public String getSubscriberDefaultVcode(String subID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return "ERROR";
		try {
			String ret = SubscriberImpl.isSubscriberActivated(conn,
					subID(subID));
			if (ret != null && ret.equalsIgnoreCase("true")) {
				SubscriberStatus[] subscriberStatus = getAllActiveSubSelectionRecords(
						subID, rbtType);
				SubscriberStatus everyoneSetting = null;
				for (int i = 0; subscriberStatus != null
						&& i < subscriberStatus.length; i++) {
					if (subscriberStatus[i].callerID() == null
							&& subscriberStatus[i].status() == 1) {
						everyoneSetting = subscriberStatus[i];
						break;
					}
				}

				if (everyoneSetting != null
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(everyoneSetting
										.categoryType()))
					return "ALBUM";
				else {
					if (everyoneSetting != null)
						return everyoneSetting.subscriberFile() + ":"
								+ everyoneSetting.categoryID();
					else if (everyoneSetting == null)
						return "DEFAULT";
				}

			} else if (ret == null || ret.equalsIgnoreCase("false"))
				return "NOT_FOUND";
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "ERROR";
	}

	/*
	 * added for airtel get subscriber vcode checks in localdb, onmobile circles
	 * and finally in north. return string will be ALBUM for shuffle,vcode:catId
	 * for proper case,ERROR for any error DEFAULT in case subscriber registered
	 * but doesn't have a rbt(or default)
	 */

	public String getSubscriberVcode(String sub_id, String src_sub_id,
			boolean useProxy, String proxyServerPort) {
		return getSubscriberVcode(sub_id, src_sub_id, useProxy,
				proxyServerPort, 0);
	}

	public String getSubscriberVcode(String sub_id, String src_sub_id,
			boolean useProxy, String proxyServerPort, int rbtType) {
		String vcode = "ERROR";
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(sub_id, "COPY"));
		if (subscriberDetail.isValidSubscriber())
			vcode = getSubscriberDefaultVcode(sub_id, rbtType);
		else {
			if (subscriberDetail != null
					&& subscriberDetail.getCircleID() != null
					&& subscriberDetail.getCircleID().equalsIgnoreCase(
							"NON_ONMOBILE")) {
				Parameters param = CacheManagerUtil.getParametersCacheManager()
						.getParameter("COMMON", "NORTHDB_URL");

				StringBuffer response = new StringBuffer();
				String jsp = "/tonecopy.jsp?dstSubscriberID=" + sub_id
						+ "&srcSubscriberID=" + src_sub_id;
				if (Tools.callURL(param.getValue() + jsp, new Integer(0),
						response, useProxy, proxyServerPort)) {
					String resp = response.toString().trim();
					if (resp.indexOf(":") > 0) {
						StringTokenizer st = new StringTokenizer(resp, ":");
						while (st.hasMoreTokens()) {
							String mssg = st.nextToken();
							if (mssg.equalsIgnoreCase("SUCCESS"))
								return "rbt_" + st.nextToken() + "_rbt" + ":"
										+ "26";
							else if (mssg.equalsIgnoreCase("ERROR")) {
								String ecode = st.nextToken();
								if (ecode.equalsIgnoreCase("130")
										|| ecode.equalsIgnoreCase("145"))
									return "NOT_FOUND";
								else
									return "ERROR";
							}
						}
					}
					return "ERROR";
				} else
					return "ERROR";
			} else if (subscriberDetail.getCircleID() == null)
				return "NOT_VALID";
			else {
				if (m_redirectNationalURL != null) {
					StringBuffer response = new StringBuffer();
					String strUrl = m_redirectNationalURL
							+ "rbt_redirect.jsp?request_value=vCode&SUB_ID="
							+ sub_id;
					if (Tools.callURL(strUrl, new Integer(0), response,
							useProxy, proxyServerPort))
						vcode = response.toString().trim();
					else
						return "ERROR";
				}
			}
		}
		return vcode;
	}

	public String addSubscriberSelectionsChannel(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval,
			HashMap extraInfo) {
		subscriberID = subID(subscriberID);
		// String circleID = dbManager.getCircleId(subscriberID);
		// Subscriber sub = dbManager.getSubscriber(subscriberID);
		char prepaidYes = 'n';
		if (subscriber != null && subscriber.prepaidYes())
			prepaidYes = 'y';
		Categories categories = getCategory(categoryID, subscriber.circleID(),
				prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		logger.info("leaving");
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				subscriber.circleID(), incrSelCount, false, null, OptIn, false,
				inLoop, subClass, subscriber, 0, selInterval, extraInfo, false,
				null, false);
		if (ret != null)
			return ret;

		return null;

	}

	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval,
			HashMap extraInfo) {

		logger.info("entered 2");
		subscriberID = subID(subscriberID);
		/*
		 * String circleID = dbManager.getCircleId(subscriberID); Subscriber sub
		 * = dbManager.getSubscriber(subscriberID);
		 */
		char prepaidYes = 'n';
		if (subscriber != null && subscriber.prepaidYes()) {
			prepaidYes = 'y';
			subYes = subscriber.subYes();
		}
		Categories categories = getCategory(categoryID, subscriber.circleID(),
				prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		logger.info("leaving");
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				subscriber.circleID(), incrSelCount, false, null, OptIn, false,
				inLoop, subClass, subscriber, 0, selInterval, extraInfo, false,
				null, false);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;

	}

	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval,
			HashMap extraInfo, boolean isUIChargeClass) {

		logger.info("entered 2");
		subscriberID = subID(subscriberID);
		/*
		 * String circleID = dbManager.getCircleId(subscriberID); Subscriber sub
		 * = dbManager.getSubscriber(subscriberID);
		 */
		char prepaidYes = 'n';
		if (subscriber != null && subscriber.prepaidYes()) {
			prepaidYes = 'y';
			subYes = subscriber.subYes();
		}
		Categories categories = getCategory(categoryID, subscriber.circleID(),
				prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		logger.info("leaving");
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				subscriber.circleID(), incrSelCount, false, null, OptIn, false,
				inLoop, subClass, subscriber, 0, selInterval, extraInfo,
				isUIChargeClass, null, false);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;

	}

	/* RW */// 1
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval) {
		logger.info("entering 1");
		return addSubscriberSelections(subscriberID, callerID, categoryID,
				subscriberWavFile, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				incrSelCount, OptIn, inLoop, subClass, subscriber, selInterval,
				null);

	}

	/* RW */// 2
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, boolean incrSelCount, boolean OptIn,
			boolean inLoop, String subClass, Subscriber subscriber,
			String selInterval) {
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, 0, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, null,
				incrSelCount, false, null, OptIn, false, inLoop, subClass,
				subscriber, 0, selInterval);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}

	/*
	 * Tata Tasks to be done changeSubType not needed.
	 */// 3
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID,
			boolean incrSelCount, boolean isTATA, boolean inLoop,
			Subscriber sub, String selInterval) {
		Categories categories = getCategory(categoryID, sub.circleID(),
				isPrepaid ? 'y' : 'n');
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				circleID, incrSelCount, true, null, false, isTATA, inLoop,
				sub.subscriptionClass(), sub, 0, selInterval);

		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}

	// TATA //4
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean isTATA, boolean inLoop, Subscriber sub, String selInterval) {
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, 0, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, circleID,
				incrSelCount, true, null, false, isTATA, inLoop, null, sub, 0,
				selInterval);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}

	public String insertPreConsentSubscriptionRequest(String subscriberID,
			String activate, Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo,
			String circleId, String refId, WebServiceContext task) {
		// Overriden in VirginDbMgrImpl.java: this feature for IDEA.

		Connection conn = getConnection();
		if (conn == null)
			return null;

		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";
		String response = "failure";
		Subscriber subscriber = null;
		boolean success = false;
		try {

			if (!isTNBNewFlow && classType != null
					&& tnbSubscriptionClasses.contains(classType)
					&& endDate == null) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, +1);
				endDate = endCal.getTime();
			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());

			}
			if (cos == null)
				cos = getCos(null, subscriberID, subscriber, circleId,
						isPrepaid ? "y" : "n", activate, classType);

			if (cos != null && !cos.isDefaultCos()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays() - 1);
				endDate = cal.getTime();
				if (endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}

			String cosID = null;

			String subscriptionClass = classType;
			if (cos != null) {
				cosID = cos.getCosId();
				if ((subscriptionClass == null || subscriptionClass
						.equalsIgnoreCase("DEFAULT"))
						&& cos.getSubscriptionClass() != null
						&& !cos.getSubscriptionClass().equalsIgnoreCase(
								"DEFAULT"))
					subscriptionClass = cos.getSubscriptionClass();
			}

			if (subscriptionClass == null)
				subscriptionClass = "DEFAULT";

			// update ExtraInfo
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			task.put(WebServiceConstants.srvkey, subscriptionClass);
			task.put(WebServiceConstants.param_mode, activate);
			task.put(WebServiceConstants.param_cosID, cosID);
			if (task.containsKey(WebServiceConstants.param_isPreConsentBaseRequest)) {
				Date requestTime = null;
				int secondsToBeAddedInRequestTime = DBUtility
						.secondsToBeAddedInRequestTime(circleId, activate);
				if (secondsToBeAddedInRequestTime != -1) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, secondsToBeAddedInRequestTime);
					requestTime = cal.getTime();
				}
				success = ConsentTableImpl.insertSubscriptionRecord(conn,
						subID(subscriberID), activate, startDate, endDate,
						actInfo, prepaid, subscriptionClass, cosID, rbtType,
						isDirectActivation, subExtraInfo, circleId, refId, 0,
						false, requestTime,null);
			}
			if (success)
				response = "success";
		} catch (Throwable e) {
			response = "error";
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return response;

	}

	public String addSelectionConsent(String tranID, String subscriberID,
			String callerID, String categoryId, String subClass,
			String selectedBy, Date startTime, Date endTime, int status,
			String chargeClassType, String cosID, String packCosId,
			String clipId, String selInterval, int fromTime, int toTime,
			String selectionInfo, int selType, boolean inLoop,
			String purchageTpe, boolean useUIChargeClass, int promoType,
			String profileHours, boolean isPrepaid, String feedType,
			String waveFile, int rbtType, String circleID, String language,
			Date requestDate, String extraInfo, String requestType,
			int consentSatus, Subscriber sub, WebServiceContext task,
			Categories categories, boolean doTODCheck) {

		logger.info("Adding selection consent. tranID: " + tranID
				+ ", subscriberID: " + subscriberID + ", categoryId: "
				+ categoryId + ", selectedBy: " + selectedBy
				+ ", requestType: " + requestType + ", useUIChargeClass: "
				+ useUIChargeClass + ", subClass: " + subClass
				+ ", chargeClassType: " + chargeClassType);
		
		String clipClassType = "";
		Clip clipObj = null;
		if (waveFile != null) {
			clipObj = rbtCacheManager.getClipByRbtWavFileName(waveFile);
			if (clipObj != null) {
				clipClassType = clipObj.getClassType();
			}
		}
		
		Connection conn = null;
		String nullConnection = null;
		String response = "error";
		conn = getConnection();
		if (conn == null) {
			return nullConnection;
		}
		String classType = null;
		if (status == 90) {
			classType = chargeClassType;
		} else {
			classType = "DEFAULT";
			if (useUIChargeClass) {
				classType = chargeClassType;
				logger.debug("Considering the requested chargeClass: "
						+ classType + " since useUIChargeClass"
						+ " is set for subscriberID: " + subscriberID);
			} else {
				// If useUIChargeClass is not passed in the request, it fetches
				// classType based on cos.
				// Idea-Combo DT New Model RBT-14087
				boolean specialSelection = isSpecialSelection(callerID, status);
				Map<String, String> whereClauseMap = new HashMap<String, String>();
				whereClauseMap.put("CALLER_ID", ((callerID == null || callerID
						.equalsIgnoreCase(WebServiceConstants.ALL)) ? null
						: callerID));
				whereClauseMap.put("STATUS", String.valueOf(status));
				whereClauseMap.put("FROM_TIME", String.valueOf(fromTime));
				whereClauseMap.put("TO_TIME", String.valueOf(toTime));
				if (selInterval != null)
					whereClauseMap.put("SEL_INTERVAL", selInterval);
				int selectionCount = getSelcetionCountForSubcriber(
						subscriberID, whereClauseMap);
				// Idea-Combo DT New Model RBT-14087
				boolean isSpecialCallerSetting  =  isSpecialCallerSelection(callerID);
				String nextClass = getNextChargeClass(sub, specialSelection,
						inLoop, selectionCount , clipClassType , isSpecialCallerSetting);
				CosDetails cosObject = null;
				// For new and Deactived user nextClass will come as null so it
				// should pick the cos Charge class for task object.modified the
				// code as below.
				if (nextClass == null) {
					cosObject = DataUtils.getCos(task, sub);
					if (cosObject != null) {
						nextClass = getChargeClassForSpecificCosUser(cosObject,
								sub, inLoop, specialSelection, selectionCount, clipClassType , isSpecialCallerSetting);
						if (nextClass == null) {
							nextClass = getChargeClassFromCos(cosObject, 0);
						}
					}
				}
				if (nextClass != null && !nextClass.equalsIgnoreCase("DEFAULT")) {
					classType = nextClass;
					logger.debug("Not considering the requested "
							+ "chargeClass: " + chargeClassType
							+ " since useUIChargeClass is not set. "
							+ "Updated chargeClass: " + nextClass
							+ " for subscriberID: " + subscriberID);
				}
			}
		}
		if (sub != null && sub.subYes() != null
				&& (sub.subYes().equals("D") || sub.subYes().equals("P"))) {
			return "deact_pending";
		} else if (sub != null && sub.subYes() != null
				&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
			Map<String, String> subscriberExtraInfo = getExtraInfoMap(sub);
			boolean isVoluntarySuspendedSub = false;
			if (subscriberExtraInfo != null
					&& subscriberExtraInfo.containsKey(VOLUNTARY)) {
				isVoluntarySuspendedSub = ("" + subscriberExtraInfo
						.get(VOLUNTARY)).equalsIgnoreCase("true");
			}
			if (!isVoluntarySuspendedSub) {
				logger.info(subscriberID + " is suspended. Returning false.");
				return "suspended";
			}
		}
		if (task.containsKey(WebServiceConstants.param_rentalPack)) {
			subClass = task.getString(WebServiceConstants.param_rentalPack);
			requestType = "UPGRADE";
		}
		logger.info("SubClass === " + subClass + " requestType = "
				+ requestType);

		if (task.containsKey(Constants.param_allowPremiumContent)) {
			Map<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap == null)
				extraInfoMap = new HashMap<String, String>();

			extraInfoMap.put(Constants.param_allowPremiumContent, "y");
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		}

		/**
		 * Since Sprint 4 RBT 2.0, RBT 15670 One more parameter udpId has been
		 * added in getSubscriberSelections method. If udpId is present then
		 * query will filter it with udpId also otherwise old flow.
		 */
		String udpId = null;
		/*
		 * if(extraInfo.containsKey(WebServiceConstants.param_udpId)) udpId =
		 * (String) extraInfo.get("UDP_ID");
		 */

		SubscriberStatus[] subscriberSelections = SubscriberStatusImpl
				.getSubscriberSelections(conn, subID(subscriberID),
						subID(callerID), rbtType, udpId);

		logger.info("Verifying existing selections. subscriberID: "
				+ subscriberID + ", subscriberSelections: "
				+ subscriberSelections);

		SubscriberStatus subscriberStatus = getAvailableSelection(conn,
				subID(subscriberID), subID(callerID), subscriberSelections,
				categories, waveFile, status, fromTime, toTime, startTime,
				endTime, doTODCheck, inLoop, rbtType, selInterval, selectedBy);

		boolean isFetchAndValidateConsent = RBTParametersUtils
				.getParamAsBoolean("WEBSERVICE", "FETCH_AND_VALIDATE_CONSENT",
						"FALSE");

		if (subscriberStatus == null && isFetchAndValidateConsent) {

			logger.info("Same selection is not found, now checking in consent table. subscriberID: "
					+ subscriberID);

			// CONSENT STATUS 0, 1 AND 2
			SubscriberStatus[] consentSubscriberStatus = ConsentTableImpl
					.getSubscriberSelections(conn, subID(subscriberID),
							subID(callerID), rbtType);
			logger.debug("Got selection requests from consent table. subscriberID: "
					+ subscriberID
					+ ", consentSubscriberStatus: "
					+ consentSubscriberStatus);

			subscriberStatus = getAvailableConsentSelection(conn,
					subID(subscriberID), subID(callerID),
					consentSubscriberStatus, categories, waveFile, status,
					fromTime, toTime, startTime, endTime, doTODCheck, inLoop,
					rbtType, selInterval);

			logger.debug("Selection requests verified in consent table. "
					+ "subscriberID: " + subscriberID + ", availableConsent: "
					+ subscriberStatus);
		} else {
			logger.debug("Selection already exists in selection table "
					+ "OR parameter FETCH_AND_VALIDATE_CONSENT is set as false. subscriberID: "
					+ subscriberID);
		}

		if (subscriberStatus != null) {
			logger.debug("Returning selection overlap, Selection is already exists. "
					+ "subscriberID: " + subscriberID);
			return SELECTION_FAILED_SELECTION_OVERLAP;
		}

		task.put(WebServiceConstants.param_chargeClass, classType);
		boolean ret = false;
		String inlineFlow = null;
		Integer inlineFlag = null;

		// Idea RBT-9929
		if (task.containsKey(WebServiceConstants.param_makeEntryInDB)
				&& task.get(WebServiceConstants.param_makeEntryInDB).toString()
						.equalsIgnoreCase("false")) {
			logger.debug("param: " + WebServiceConstants.param_makeEntryInDB
					+ "= false.");
			String linkedRefId = null;
			if (isSubscriberDeactivated(sub)) {
				linkedRefId =  com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber("");
				if(linkedRefId==null){
					linkedRefId = UUID.randomUUID().toString();
				}						
				task.put(WebServiceConstants.param_linkedRefId, linkedRefId);
			}
			
			String refId = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber("");
			if(refId==null){
				refId = UUID.randomUUID().toString();
			}
			task.put(WebServiceConstants.param_refID, refId);
			
			if (clipObj != null) {
				String clipInfo = clipObj.getClipInfo();
				if (clipInfo != null) {
					task.put(WebServiceConstants.param_clipInfo, clipInfo);
				}
			}
			
			ret = true;
		} else {
			if (Boolean.parseBoolean(task
					.getString(WebServiceConstants.param_generateRefId))) {
				if (isSubscriberDeactivated(sub)) {
					String linkedRefId = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber("");
					if(linkedRefId==null){
						linkedRefId = UUID.randomUUID().toString();
					}
					task.put(WebServiceConstants.param_linkedRefId, linkedRefId);
				}
				String refId = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber("");
				if(refId==null){
					 refId = UUID.randomUUID().toString();
				}
				task.put(WebServiceConstants.param_refID, refId);
			}
			task.put(WebServiceConstants.param_transID, tranID);
			if(isSubscriberStrictlyActive(sub)) {
				inlineFlow = Utility.getInlineFlow(classType, 1);
				if(inlineFlow != null) {
					if(inlineFlow.equalsIgnoreCase("0")) {
//						//not cg, create ss and pass to queue & return success or failure.
//						ret = true;
//						return "success";
					} else {
						inlineFlag = 0;
					}
				}
			}
			
			try {
			ret = ConsentTableImpl.insertSelectionRecord(conn, tranID,
					subscriberID, callerID, categoryId, subClass, selectedBy,
					startTime, endTime, status, classType, cosID, packCosId,
					clipId, selInterval, fromTime, toTime, selectionInfo,
					selType, inLoop, purchageTpe, useUIChargeClass, promoType,
					profileHours, isPrepaid, feedType, waveFile, rbtType,
					circleID, language, requestDate, extraInfo, requestType,
					consentSatus,null, inlineFlag);
			if (ret) {
				task.put(WebServiceConstants.IS_RECENT_SEL_CONSENT, "true");
					if(inlineFlow != null) {
						Utility.sendInlineMessage(subscriberID, tranID, WebServiceConstants.api_Selection, WebServiceConstants.CONSENT, inlineFlow);
					}
				}  
			} catch (Throwable t) {
				if(inlineFlow != null) {
					updateConsentInlineDaemonFlag(tranID, null);
				}
			}
		}
		if (ret) {
			response = "success";
		}
		return response;
	}

	// public String addSubscriberSelections(String subscriberID, String
	// callerID,
	// Categories categories, HashMap clipMap, Date setTime,
	// Date startTime, Date endTime, int status, String selectedBy,
	// String selectionInfo, int freePeriod, boolean isPrepaid,
	// boolean changeSubType, String messagePath, int fromTime,
	// int toTime, String chargeClassType, boolean smActivation,
	// boolean doTODCheck, String mode, String regexType, String subYes,
	// String promoType, String circleID, boolean incrSelCount,
	// boolean useDate, String transID, boolean OptIn, boolean isTata,
	// boolean inLoop, String subClass, Subscriber sub, int rbtType,
	// String selInterval, HashMap extraInfo, boolean useUIChargeClass,
	// String refID, boolean isDirectActivation) {
	//
	// logger.info("Adding selection for subscriberId: " + subscriberID
	// + ", categories: " + categories + ", clipMap: " + clipMap
	// + ", refID: " + refID + ", status: " + status + ", rbtType: "
	// + rbtType);
	// StringBuilder responseBuilder = new StringBuilder();
	// int count = 0;
	// Connection conn = null;
	// String clipId = null;
	// String clipType = null;
	// String classType = "DEFAULT";
	// String sel_status = STATE_BASE_ACTIVATION_PENDING;
	// char loopStatus = LOOP_STATUS_OVERRIDE_INIT;
	// Date endDate = endTime;
	// String prepaid = "n";
	// try {
	// String nullConnection = null;
	// conn = getConnection();
	// if (conn == null)
	// return nullConnection;
	// Date nextChargingDate = null;
	// Date startDate = startTime;
	// String selectInfo = selectionInfo;
	// int nextPlus = -1;
	// boolean updateEndDate = false;
	// subscriberID = subID(subscriberID);
	// callerID = subID(callerID);
	// if (subscriberID != null && callerID != null
	// && subscriberID.equals(callerID))
	// return responseBuilder.append(SELECTION_FAILED_OWN_NUMBER)
	// .toString();
	//
	// if (categories != null
	// && com.onmobile.apps.ringbacktones.webservice.common.Utility
	// .isShuffleCategory(categories.type())) {
	// if (categories.endTime().before(new Date())) {
	// logger.warn("Category: " + categories.id()
	// + " is expired. Category end time: "
	// + categories.endTime());
	// return responseBuilder.append(
	// SELECTION_FAILED_CATEGORY_EXPIRED).toString();
	// }
	// }
	//
	// if (selInterval != null && selInterval.indexOf(",") != -1) {
	// List days = new ArrayList();
	// StringTokenizer stk = new StringTokenizer(selInterval, ",");
	// while (stk.hasMoreTokens())
	// days.add(stk.nextToken());
	//
	// if (days.size() == 7) {
	// selInterval = null;
	// } else {
	// Collections.sort(days);
	// selInterval = "";
	// for (int i = 0; i < days.size(); i++) {
	// selInterval = selInterval + days.get(i);
	// if (i != days.size() - 1)
	// selInterval = selInterval + ",";
	// }
	// }
	// }
	//
	// if (callerID != null) {
	// Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
	// subscriberID);
	// if (groups != null && groups.length > 0) {
	// int[] groupIDs = new int[groups.length];
	// for (int i = 0; i < groups.length; i++) {
	// groupIDs[i] = groups[i].groupID();
	// }
	// GroupMembers groupMember = GroupMembersImpl
	// .getMemberFromGroups(conn, callerID, groupIDs);
	// if (groupMember != null) {
	// for (Groups group : groups) {
	// if (groupMember.groupID() == group.groupID()) {
	// logger.info("Pre group id for the group is :"
	// + group.preGroupID());
	// if (group.preGroupID() != null
	// && group.preGroupID().equals("99")) // Blocked
	// // Caller
	// return responseBuilder.append(
	// SELECTION_FAILED_CALLER_BLOCKED)
	// .toString();
	// else if (group.preGroupID() == null
	// || !group.preGroupID().equals("98"))
	// return responseBuilder
	// .append(
	// SELECTION_FAILED_CALLER_ALREADY_IN_GROUP)
	// .toString();
	// }
	// }
	// }
	// }
	// }
	//
	// if (sub != null && rbtType != 2) {
	// rbtType = sub.rbtType();
	// }
	// if (sub != null && sub.subYes() != null
	// && (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
	// logger.warn("Could not add subscriber. SubscriberID: " + subscriberID
	// + " is suspended.");
	// return responseBuilder.append(
	// SELECTION_FAILED_SUBSCRIBER_SUSPENDED).toString();
	// }
	// boolean isSelSuspended = false;
	// if (m_checkForSuspendedSelection) {
	// isSelSuspended = isSelSuspended(subID(subscriberID),
	// subID(callerID));
	// }
	// if (isSelSuspended) {
	// logger.warn("Could not add subscriber. SubscriberID: " + subscriberID
	// + " for callerID: "+callerID+" is suspended.");
	// return responseBuilder.append(
	// SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED)
	// .toString();
	// }
	//
	// if (endDate == null)
	// endDate = m_endDate;
	//
	// // If chargeClassType is null, then useUIChargeClass parameter will
	// // be ignored
	// useUIChargeClass = useUIChargeClass && chargeClassType != null;
	//
	// if (useUIChargeClass)
	// classType = chargeClassType;
	// else if (categories != null)
	// classType = categories.classType();
	//
	// Date clipEndTime = null;
	// String clipGrammar = null;
	// String clipClassType = null;
	// String subscriberWavFile = null;
	//
	// if (clipMap != null) {
	// if (clipMap.containsKey("CLIP_CLASS"))
	// clipClassType = (String) clipMap.get("CLIP_CLASS");
	// if (clipMap.containsKey("CLIP_END"))
	// clipEndTime = (Date) clipMap.get("CLIP_END");
	// if (clipMap.containsKey("CLIP_GRAMMAR"))
	// clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
	// if (clipMap.containsKey("CLIP_WAV"))
	// subscriberWavFile = (String) clipMap.get("CLIP_WAV");
	// }
	//
	// if (subscriberWavFile == null) {
	// if (status != 90) {
	// logger.warn("Could not add subscriber. SubscriberID: " + subscriberID
	// + " subscriberWavFile is null");
	// return responseBuilder.append(
	// SELECTION_FAILED_NULL_WAV_FILE).toString();
	// }
	//
	// subscriberWavFile = "CRICKET";
	// }
	//
	// if (subYes != null
	// && (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
	// .equalsIgnoreCase(STATE_EVENT))) {
	// if (!isPackActivationPendingForContent(sub, categories,
	// subscriberWavFile, status, callerID))
	// sel_status = STATE_TO_BE_ACTIVATED;
	// }
	//
	// if (subClass != null && m_subOnlyChargeClass != null
	// && m_subOnlyChargeClass.containsKey(subClass)) {
	// chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
	// updateEndDate = true;
	// }
	// if (clipEndTime != null) {
	// if (clipEndTime.getTime() < System.currentTimeMillis()) {
	// return responseBuilder
	// .append(SELECTION_FAILED_CLIP_EXPIRED).toString();
	// }
	// if (categories != null
	// && (categories.type() == DAILY_SHUFFLE || categories
	// .type() == MONTHLY_SHUFFLE)) {
	// endDate = categories.endTime();
	// status = 79;
	// }
	//
	// if (rbtType == 1) {
	// if (status == 99 || categories.id() == 1)
	// return responseBuilder
	// .append(
	// SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE)
	// .toString();
	// if (com.onmobile.apps.ringbacktones.webservice.common.Utility
	// .isShuffleCategory(categories.type()))
	// return responseBuilder.append(
	// SELECTION_FAILED_ADRBT_FOR_SHUFFLES).toString();
	// }
	//
	// if (!useUIChargeClass && clipClassType != null
	// && !clipClassType.equalsIgnoreCase("DEFAULT")
	// && classType != null
	// && !clipClassType.equalsIgnoreCase(classType)) {
	// ChargeClass catCharge = CacheManagerUtil
	// .getChargeClassCacheManager().getChargeClass(
	// classType);
	// ChargeClass clipCharge = CacheManagerUtil
	// .getChargeClassCacheManager().getChargeClass(
	// clipClassType);
	//
	// if (catCharge != null && clipCharge != null
	// && catCharge.getAmount() != null
	// && clipCharge.getAmount() != null) {
	// try {
	// int firstAmount = Integer.parseInt(catCharge
	// .getAmount());
	// int secondAmount = Integer.parseInt(clipCharge
	// .getAmount());
	//
	// if ((firstAmount < secondAmount)
	// || (m_overrideChargeClasses != null && m_overrideChargeClasses
	// .contains(clipClassType
	// .toLowerCase())))
	// classType = clipClassType;
	// } catch (Throwable e) {
	// }
	// }
	// if (clipClassType.startsWith("TRIAL") && categories != null
	// && categories.id() != 26 && categories.id() != 23)
	// classType = clipClassType;
	// }
	// }
	//
	// if (!useUIChargeClass && chargeClassType != null) {
	// ChargeClass first = CacheManagerUtil
	// .getChargeClassCacheManager().getChargeClass(classType);
	// ChargeClass second = CacheManagerUtil
	// .getChargeClassCacheManager().getChargeClass(
	// chargeClassType);
	//
	// if (first != null && second != null
	// && first.getAmount() != null
	// && second.getAmount() != null) {
	// try {
	// int firstAmount = Integer.parseInt(first.getAmount());
	// int secondAmount = Integer.parseInt(second.getAmount());
	//
	// if (firstAmount <= secondAmount
	// || secondAmount == 0
	// || chargeClassType
	// .equalsIgnoreCase("YOUTHCARD")
	// || chargeClassType
	// .equalsIgnoreCase("DEFAULT_10")
	// || (m_overrideChargeClasses != null && m_overrideChargeClasses
	// .contains(chargeClassType.toLowerCase())))
	// classType = chargeClassType;
	// } catch (Throwable e) {
	// classType = chargeClassType;
	// }
	// } else {
	// classType = chargeClassType;
	// }
	//
	// if (first != null && first.getChargeClass().startsWith("TRIAL")
	// && categories != null && categories.id() != 26
	// && categories.id() != 23) {
	// classType = first.getChargeClass();
	// }
	// }
	//
	// /* Esia specific Regex changes */
	// if (!useUIChargeClass && mode != null && regexType != null
	// && !classType.startsWith("TRIAL")) {
	// // TODO: We should check for valid promoType
	// /*
	// * ChargePromoTypeMap[] chargePromoTypeMaps =
	// * ChargePromoTypeMapImpl .getChargePromoTypeMapsForType(conn,
	// * promoType, "SEL", mode);
	// */
	//
	// ChargeClassMap chargeClassMap = ChargeClassMapImpl
	// .getChargeClassMapsForModeRegexTypeAndClassType(conn,
	// mode, regexType, classType);
	// if (chargeClassMap != null) {
	// classType = chargeClassMap.finalClassType();
	// }
	// }
	// if (!useUIChargeClass && categories != null
	// && categories.type() == 10 && m_modeChargeClass != null
	// && m_modeChargeClass.containsKey(selectedBy)) {
	// classType = (String) m_modeChargeClass.get(selectedBy);
	// }
	//
	// if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
	// ViralSMSTable viralSMS = null;
	// List<String> viralSMSList = Arrays.asList("BASIC,CRICKET".split(","));
	// if(isViralSmsTypeListForNewTable(viralSMSList)){
	// viralSMS = ViralSMSNewImpl.getViralPromotion(
	// conn, subID(subscriberID), null);
	// }else{
	// viralSMS = ViralSMSTableImpl.getViralPromotion(
	// conn, subID(subscriberID), null);
	// }
	// if (viralSMS != null) {
	// selectInfo = selectInfo + ":" + "viral";
	// }
	// }
	//
	// if (isPrepaid)
	// prepaid = "y";
	//
	// String oldClassType = null;
	// Date oldNextChargeDate = null;
	//
	// String afterTrialClassType = "DEFAULT";
	// if (OptIn)
	// afterTrialClassType = "DEFAULT_OPTIN";
	//
	// SubscriberStatus[] subscriberSelections = SubscriberStatusImpl
	// .getSubscriberSelections(conn, subID(subscriberID),
	// subID(callerID), rbtType);
	//
	// if (!inLoop && status == 1) // If user opted for UDS
	// {
	// HashMap<String, String> subExtraInfoMap = DBUtility
	// .getAttributeMapFromXML(sub.extraInfo());
	// if (subExtraInfoMap != null
	// && subExtraInfoMap.containsKey(UDS_OPTIN))
	// inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN))
	// .equalsIgnoreCase("TRUE");
	// if (inLoop) {
	// if (isShufflePresentSelection(subID(subscriberID),
	// callerID, 0))
	// inLoop = false;
	// else if (categories.type() == 0 || categories.type() == 10
	// || categories.type() == 11
	// || categories.type() == 12
	// || categories.type() == 20)
	// return responseBuilder.append(
	// SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN)
	// .toString();
	// }
	// }
	//
	// if (selInterval != null && status != 80) {
	//
	// if (selInterval.startsWith("W") || selInterval.startsWith("M")) {
	//
	// status = 75;
	// }
	//
	// if (selInterval.startsWith("Y")) {
	//
	// status = 95;
	// String date = selInterval.substring(1);
	// Date parseDate = null;
	// if (date.length() == 8) {
	//
	// SimpleDateFormat dateFormat = new SimpleDateFormat(
	// "ddMMyyyy");
	// Date currentDate = new Date();
	// parseDate = dateFormat.parse(date);
	// if (parseDate.before(currentDate)
	// || parseDate.equals(currentDate)) {
	// return responseBuilder.append(
	// SELECTION_FAILED_INVALID_PARAMETER)
	// .toString();
	// }
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(parseDate);
	// // parseDate.setDate(parseDate.getDate()+1);
	// cal.add(Calendar.DAY_OF_YEAR, 1);
	// endDate = cal.getTime();
	// }
	//
	// if (date.length() == 4) {
	//
	// endDate = m_endDate;
	// }
	// }
	// }
	//
	// // Added for checking the selection limit
	//
	// /* time of the day changes */
	// SubscriberStatus subscriberStatus = null;
	// logger
	// .info("The status :- " + status + " and rbtType - "
	// + rbtType);
	// if (isTata) {
	// subscriberStatus = this.getSubWavFileForCaller(
	// subID(subscriberID), callerID, subscriberWavFile);
	// if (subscriberStatus != null
	// && !(subscriberStatus.selStatus().equals(
	// STATE_ACTIVATED)
	// || subscriberStatus.selStatus().equals(
	// STATE_TO_BE_ACTIVATED)
	// || subscriberStatus.selStatus().equals(
	// STATE_ACTIVATION_PENDING) || subscriberStatus
	// .selStatus().equals(
	// STATE_BASE_ACTIVATION_PENDING)))
	// subscriberStatus = null;
	// } else {
	// subscriberStatus = getAvailableSelection(conn,
	// subID(subscriberID), subID(callerID),
	// subscriberSelections, categories, subscriberWavFile,
	// status, fromTime, toTime, startDate, endDate,
	// doTODCheck, inLoop, rbtType, selInterval, selectedBy);
	// }
	// if (subscriberStatus == null) {
	// logger.info("No selections found for subscriberID: "+subscriberID);
	// if (inLoop && (status == 90 || status == 99 || status == 0))
	// inLoop = false;
	// if (inLoop && categories.type() == SHUFFLE && !m_putSGSInUGS)
	// inLoop = false;
	// if (fromTime == 0 && toTime == 2359 && status == 80)
	// status = 1;
	//
	// subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(
	// conn, subID(subscriberID), subID(callerID), status,
	// rbtType);
	// if (subscriberStatus != null) {
	// oldClassType = subscriberStatus.classType();
	// oldNextChargeDate = subscriberStatus.nextChargingDate();
	// if (categories != null && categories.id() == 3
	// && classType != null
	// && m_TrialWithActivations != null
	// && m_TrialWithActivations.contains(classType)) {
	// if (!oldClassType.startsWith("TRIAL")) {
	// SubscriberStatusImpl.smDeactivateOldSelection(conn,
	// subID(subscriberID), subID(callerID),
	// status, null, fromTime, toTime, rbtType,
	// selInterval, null);
	//
	// }
	// if (oldNextChargeDate != null) {
	// // nextPlus = new Long((oldNextChargeDate.getTime()
	// // -
	// // System.currentTimeMillis())/3600*1000*24).intValue();
	//
	// if (oldClassType.startsWith("TRIAL")) {
	// SubscriberStatusImpl
	// .smDeactivateOldTrialSelection(conn,
	// subID(subscriberID),
	// subID(callerID), status,
	// fromTime, toTime, rbtType);
	// deleteTrialSelectionReminder(subID(subscriberID));
	// }
	// }
	// oldClassType = null;
	// oldNextChargeDate = null;
	// }
	//
	// if (inLoop && subscriberStatus.categoryType() == SHUFFLE
	// && !m_putSGSInUGS)
	// inLoop = false;
	// }
	// // else // this else will make all first callerID selection as
	// // override :), not needed actually
	// // inLoop = false;
	//
	// if (oldClassType == null && classType != null
	// && classType.startsWith("TRIAL")) {
	// ChargeClass chargeClass = CacheManagerUtil
	// .getChargeClassCacheManager().getChargeClass(
	// classType);
	// if (chargeClass != null
	// && chargeClass.getSelectionPeriod() != null
	// && chargeClass.getSelectionPeriod().startsWith("D")) {
	// String selectionPeriod = chargeClass
	// .getSelectionPeriod().substring(1);
	// if (nextPlus < 0)
	// nextPlus = Integer.parseInt(selectionPeriod);
	// }
	// addTrialSelectionReminder(subID(subscriberID), sub
	// .circleID(), classType, Calendar.getInstance()
	// .getTime(), 0);
	// }
	// if (oldClassType != null && classType != null
	// && classType.startsWith("TRIAL")
	// && oldClassType.startsWith("TRIAL")) {
	// if (oldClassType.equalsIgnoreCase(classType)) {
	// if (oldNextChargeDate != null
	// && oldNextChargeDate.after(new Date(System
	// .currentTimeMillis()))) {
	// nextChargingDate = oldNextChargeDate;
	// nextPlus = new Long(
	// (oldNextChargeDate.getTime() - System
	// .currentTimeMillis())
	// / (3600 * 1000 * 24)).intValue() + 1;
	// // startDate = Calendar.getInstance().getTime();
	// // classType = "TRIAL";
	// }
	// if (oldNextChargeDate != null
	// && oldNextChargeDate.before(new Date(System
	// .currentTimeMillis()))) {
	// nextChargingDate = null;
	// startDate = null;
	// if (chargeClassType != null
	// && !chargeClassType.startsWith("TRIAL")) {
	// classType = chargeClassType;
	// } else {
	// // classType = "DEFAULT";
	// classType = afterTrialClassType;
	// }
	// }
	// SubscriberStatusImpl.smDeactivateOldTrialSelection(
	// conn, subID(subscriberID), subID(callerID),
	// status, fromTime, toTime, rbtType);
	// } else {
	// // classType = "DEFAULT";
	// classType = afterTrialClassType;
	// }
	// }
	// if (oldClassType != null && oldClassType.startsWith("TRIAL")
	// && classType != null && !classType.startsWith("TRIAL")) {
	// SubscriberStatusImpl.smDeactivateOldTrialSelection(conn,
	// subID(subscriberID), subID(callerID), status,
	// fromTime, toTime, rbtType);
	// }
	// if (oldClassType != null && !oldClassType.startsWith("TRIAL")
	// && classType != null && classType.startsWith("TRIAL")) {
	// // classType = "DEFAULT";
	// classType = afterTrialClassType;
	// }
	// /**
	// * @added by sreekar if user's last selection is a trail
	// * selection his next selection should override the old
	// * one
	// */
	// if (inLoop && oldClassType != null
	// && (oldClassType.indexOf("TRIAL") != -1))
	// inLoop = false;
	// loopStatus = getLoopStatusForNewSelection(inLoop,
	// subID(subscriberID), isPrepaid);
	// if (classType != null && classType.startsWith("TRIAL")) {
	// sel_status = STATE_ACTIVATED;
	// // added by sreekar
	// loopStatus = getLoopStatusToUpateSelection(loopStatus,
	// subID(subscriberID), isPrepaid);
	// }
	//
	// if (transID != null) {
	// selectInfo += ":transid:" + transID + ":";
	// if (sel_status.equals(STATE_TO_BE_ACTIVATED))
	// sel_status = STATE_UN;
	// }
	// String actBy = null;
	// // String oldSubClass = null;
	// if (sub != null) {
	// actBy = sub.activatedBy();
	// // oldSubClass = sub.oldClassType();
	// }
	// if (m_trialChangeSubTypeOnSelection && actBy != null
	// && actBy.equals("TNB")
	// && (subClass != null && subClass.equals("ZERO"))) {
	// if (classType != null && classType.equals("FREE")) {
	// sel_status = STATE_BASE_ACTIVATION_PENDING;
	//
	// if (!convertSubscriptionTypeTrial(subID(subscriberID),
	// subClass, "DEFAULT", sub))
	// return responseBuilder.append(
	// SELECTION_FAILED_TNB_TO_DEFAULT_FAILED)
	// .toString();
	// }
	// }
	//
	// if (!useUIChargeClass) {
	// if (status == 80 && rbtType == 2) {
	// classType = clipClassType;
	// } else {
	// for (int i = 0; subscriberSelections != null
	// && i < subscriberSelections.length; i++) {
	// if (subscriberSelections[i].selType() == 2) {
	// HashMap selectionExtraInfo = DBUtility
	// .getAttributeMapFromXML(subscriberSelections[i]
	// .extraInfo());
	// int campaignId = -1;
	//
	// if (selectionExtraInfo != null
	// && selectionExtraInfo
	// .containsKey(iRBTConstant.CAMPAIGN_ID)
	// && selectionExtraInfo
	// .get(iRBTConstant.CAMPAIGN_ID) != null) {
	//
	// try {
	// campaignId = Integer
	// .parseInt(""
	// + selectionExtraInfo
	// .get(iRBTConstant.CAMPAIGN_ID));
	// } catch (Exception e) {
	// campaignId = -1;
	// }
	// }
	// logger.info("The value of campaign id - "
	// + campaignId);
	// if (campaignId != -1) {
	// RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO
	// .getRBTBulkUploadTask(campaignId);
	//
	// if (m_corporateDiscountChargeClass != null
	// && bulkUploadTask.getTaskMode() != null
	// && m_corporateDiscountChargeClass
	// .containsKey(bulkUploadTask
	// .getTaskMode())) {
	// logger
	// .info("The value of m_corporateDiscountChargeClass id - "
	// + m_corporateDiscountChargeClass
	// .toString());
	// HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass
	// .get(bulkUploadTask
	// .getTaskMode());
	// if (discountClassMap != null
	// && discountClassMap
	// .containsKey(classType))
	// classType = (String) discountClassMap
	// .get(classType);
	// }
	// }
	// break;
	// }
	//
	// }
	// }
	// }
	//
	// String checkSelStatus = checkSelectionLimit(
	// subscriberSelections, subID(callerID), inLoop);
	// if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
	// return responseBuilder.append(checkSelStatus).toString();
	//
	// // Added the grace selection deact mode for JIRA-RBT-6338
	// String graceDeselectedBy = selectedBy;
	// Parameters parameter = CacheManagerUtil
	// .getParametersCacheManager().getParameter("COMMON",
	// "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
	// if (parameter != null && parameter.getValue() != null)
	// graceDeselectedBy = parameter.getValue();
	//
	// SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn,
	// subID(subscriberID), subID(callerID), status, fromTime,
	// toTime, graceDeselectedBy, rbtType);
	//
	// count = createSubscriberStatus(subscriberID, callerID,
	// categories.id(), subscriberWavFile, setTime, startDate,
	// endDate, status, selectedBy, selectInfo,
	// nextChargingDate, prepaid, classType, changeSubType,
	// fromTime, toTime, sel_status, true, clipMap, categories
	// .type(), useDate, loopStatus, isTata, nextPlus,
	// rbtType, selInterval, extraInfo, refID,
	// isDirectActivation, circleID);
	//
	// logger.info("Checking to update num max selections or not."
	// + " count: " + count + " incrSelCount: " + incrSelCount);
	//
	// if (incrSelCount && count == 1)
	// SubscriberImpl.setSelectionCount(conn, subID(subscriberID));
	//
	// if (updateEndDate) {
	// SubscriberImpl.updateEndDate(conn, subID(subscriberID),
	// endDate, null);
	// }
	// } else {
	// return responseBuilder.append(
	// SELECTION_FAILED_SELECTION_OVERLAP).toString();
	// }
	// if (count > 0)
	// return responseBuilder.append(SELECTION_SUCCESS).toString();
	// else
	// return responseBuilder.append(SELECTION_FAILED_INTERNAL_ERROR)
	// .toString();
	// } catch (Throwable e) {
	// logger.error("Exception before release connection", e);
	// } finally {
	// BasicLogger.logSelection(sub, responseBuilder.toString(),
	// selectedBy, clipId, clipType, categories, loopStatus,
	// classType, callerID, status, sel_status, rbtType,
	// selInterval, prepaid);
	// releaseConnection(conn);
	//
	// }
	// //
	// TIMESTAMP,MSISDN,CIRCLE_ID,RESPONSE,MODE,CLIP_ID,CLIP_TYPE,CAT_ID,CAT_TYPE,LOOP,CHARGE_CLASS,CALLER_ID,
	// // STATUS,SEL_STATUS,SUB_YES,SEL_TYPE,SEL_INTERVAL,END_TIME,SUB_TYPE
	// return responseBuilder.append(SELECTION_FAILED_INTERNAL_ERROR)
	// .toString();
	// }

	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType,
			String selInterval, HashMap extraInfo, boolean useUIChargeClass,
			String refID, boolean isDirectActivation) {
		logger.info("Adding selection. subscriberID: " + subscriberID
				+ ", selectedBy: " + selectedBy + ", circleID: " + circleID
				+ ", refID: " + refID);

		Connection conn = getConnection();
		if (conn == null)
			return null;
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		HashMap subscriberExtraInfo = new HashMap();
		boolean updateEndDate = false;
		boolean isVoluntarySuspendedSub = false;
		try {
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null
					&& subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (categories != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(categories.type())) {
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;
			}

			if (selInterval != null && selInterval.indexOf(",") != -1) {
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while (stk.hasMoreTokens())
					days.add(stk.nextToken());

				if (days.size() == 7) {
					selInterval = null;
				} else {
					Collections.sort(days);
					selInterval = "";
					for (int i = 0; i < days.size(); i++) {
						selInterval = selInterval + days.get(i);
						if (i != days.size() - 1)
							selInterval = selInterval + ",";
					}
				}
			}

			if (sub != null && rbtType != 2) {
				rbtType = sub.rbtType();
			}

			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				subscriberExtraInfo = getExtraInfoMap(sub);
				if (subscriberExtraInfo != null
						&& subscriberExtraInfo.containsKey(VOLUNTARY)) {
					isVoluntarySuspendedSub = ("" + subscriberExtraInfo
							.get(VOLUNTARY)).equalsIgnoreCase("true");
				}
				if (!isVoluntarySuspendedSub) {
					logger.info(subscriberID
							+ " is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				} else {
					sel_status = STATE_TO_BE_ACTIVATED;
				}
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection) {
				isSelSuspended = isSelSuspended(subscriberID, callerID);
			}
			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}

			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); selectInfo = "free:" +
			 * selectInfo; }
			 */
			Date endDate = endTime;
			if (endDate == null)
				endDate = m_endDate;

			// If chargeClassType is null, then useUIChargeClass parameter will
			// be ignored
			useUIChargeClass = useUIChargeClass && chargeClassType != null;

			String classType = "DEFAULT";
			if (useUIChargeClass)
				classType = chargeClassType;
			else if (categories != null)
				classType = categories.classType();

			Date clipEndTime = null;
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
			}

			if (subscriberWavFile == null) {
				if (status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;

				subscriberWavFile = "CRICKET";
			}

			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT))) {
				if (!isPackActivationPendingForContent(sub, categories,
						subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(subClass)) {
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				if (clipEndTime.getTime() < System.currentTimeMillis()) {
					return SELECTION_FAILED_CLIP_EXPIRED;
				}
				/*
				 * if (freePeriod == 0 && status != 99 && clipEndTime != null) {
				 * endDate = clipEndTime; }
				 */
				if (categories != null
						&& (categories.type() == DAILY_SHUFFLE || categories
								.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				/*
				 * if (clipGrammar != null &&
				 * clipGrammar.equalsIgnoreCase("UGC")) if (selectInfo == null)
				 * selectInfo = "UGC"; else selectInfo += ":UGC";
				 */
				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& classType != null
						&& !clipClassType.equalsIgnoreCase(classType)) {
					ChargeClass catCharge = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					ChargeClass clipCharge = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									clipClassType);

					if (catCharge != null && clipCharge != null
							&& catCharge.getAmount() != null
							&& clipCharge.getAmount() != null) {
						try {
							int firstAmount = Integer.parseInt(catCharge
									.getAmount());
							int secondAmount = Integer.parseInt(clipCharge
									.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType
													.toLowerCase())))
								classType = clipClassType;
						} catch (Throwable e) {
						}
					}
					if (clipClassType.startsWith("TRIAL") && categories != null
							&& categories.id() != 26)
						classType = clipClassType;
				}
			}

			if (!useUIChargeClass && chargeClassType != null) {
				ChargeClass first = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(
								chargeClassType);

				if (first != null && second != null
						&& first.getAmount() != null
						&& second.getAmount() != null) {
					try {
						int firstAmount = Integer.parseInt(first.getAmount());
						int secondAmount = Integer.parseInt(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType
										.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType
										.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses
										.contains(chargeClassType.toLowerCase())))
							classType = chargeClassType;
					} catch (Throwable e) {
						classType = chargeClassType;
					}
				} else {
					classType = chargeClassType;
				}

				if (first != null && first.getChargeClass().startsWith("TRIAL")
						&& categories != null && categories.id() != 26) {
					classType = first.getChargeClass();
				}
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy)) {
				classType = (String) m_modeChargeClass.get(selectedBy);
			}

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null) {
					selectInfo = selectInfo + ":" + "viral";
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			// int count = 0;

			String afterTrialClassType = "DEFAULT";
			if (OptIn)
				afterTrialClassType = "DEFAULT_OPTIN";

			/**
			 * If user enabled UDS , then all his selections should go in Loop
			 */
			/**
			 * Since Sprint 4 RBT 2.0, RBT 15670 One more parameter udpId has
			 * been added in getSubscriberSelections method. If udpId is present
			 * then query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*
			 * if(extraInfo.containsKey(WebServiceConstants.param_udpId)) udpId
			 * = (String) extraInfo.get("UDP_ID");
			 */
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl
					.getSubscriberSelections(conn, subID(subscriberID),
							subID(callerID), rbtType, udpId);

			if (!inLoop && status == 1) // If user opted for UDS
			{
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(sub.extraInfo());
				if (subExtraInfoMap != null
						&& subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN))
							.equalsIgnoreCase("TRUE");
				if (inLoop) {
					if (isShufflePresentSelection(subID(subscriberID),
							callerID, 0))
						inLoop = false;
					else if (categories.type() == 0 || categories.type() == 10
							|| categories.type() == 11
							|| categories.type() == 12
							|| categories.type() == 20)
						return SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN;
				}
			}

			if (selInterval != null && status != 80) {

				if (selInterval.startsWith("W") || selInterval.startsWith("M")) {

					status = 75;
				}

				if (selInterval.startsWith("Y")) {

					status = 95;
					String date = selInterval.substring(1);
					Date parseDate = null;
					if (date.length() == 8) {

						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"ddMMyy");
						Date currentDate = new Date();
						parseDate = dateFormat.parse(date);
						if (parseDate.before(currentDate)
								|| parseDate.equals(currentDate)) {
							return SELECTION_FAILED_INVALID_PARAMETER;
						}
						Calendar cal = Calendar.getInstance();
						cal.setTime(parseDate);
						// parseDate.setDate(parseDate.getDate()+1);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						endDate = cal.getTime();
					}

					if (date.length() == 4) {

						endDate = m_endDate;
					}
				}
			}

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;

			subscriberStatus = getAvailableSelection(conn, subID(subscriberID),
					subID(callerID), subscriberSelections, categories,
					subscriberWavFile, status, fromTime, toTime, startDate,
					endDate, doTODCheck, inLoop, rbtType, selInterval,
					selectedBy);

			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
				// System.out.println("111111111111111111");
				if (inLoop
						&& (categories.type() == SHUFFLE || status == 90
								|| status == 99 || status == 0))
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(
						conn, subID(subscriberID), subID(callerID), status,
						rbtType);
				if (subscriberStatus != null) {
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
				}
				// else // this else will make all first callerID selection as
				// override :), not needed actually
				// inLoop = false;

				/**
				 * @added by sreekar if user's last selection is a trail
				 *        selection his next selection should override the old
				 *        one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop,
						subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null) {
					actBy = sub.activatedBy();
					// oldSubClass = sub.oldClassType();
				}
				if (m_trialChangeSubTypeOnSelection && actBy != null
						&& actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if (!convertSubscriptionTypeTrial(subID(subscriberID),
								subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				boolean isPackSel = false;
				String packCosID = null;
				if (m_overridableCategoryTypes.contains("" + categories.type())
						|| m_overridableSelectionStatus.contains("" + status))
					incrSelCount = false;
				else if (!useUIChargeClass) {
					String subPacks = null;
					HashMap<String, String> subExtraInfoMap = DBUtility
							.getAttributeMapFromXML(sub.extraInfo());
					if (subExtraInfoMap != null
							&& subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;
					if (subPacks != null) {
						com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getCategory(categories.id());
						Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getClipByRbtWavFileName(
										subscriberWavFile);
						CosDetails cosDetail = getCosDetailsForContent(
								subscriberID, subPacks, category, clipObj,
								status, callerID);
						List<ProvisioningRequests> packList = null;
						if (cosDetail != null) {
							packList = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(
											subscriberID, Integer
													.parseInt(cosDetail
															.getCosId()));
						}
						if (packList != null
								&& (isSubscriberPackActivated(packList.get(0)) || isSubscriberPackActivationPending(packList
										.get(0)))) {
							int selCount = sub.maxSelections();
							if (isPackRequest(cosDetail)) {
								selCount = packList.get(0)
										.getNumMaxSelections();
								if (cosDetail.getFreeSongs() > selCount)
									isPackSel = true;
							}

							nextClass = getChargeClassFromCos(cosDetail,
									selCount);
							packCosID = cosDetail.getCosId();
						} else {
							nextClass = getNextChargeClass(sub);
						}
					} else {
						nextClass = getNextChargeClass(sub);
					}

					if (nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if (!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}

				if (!useUIChargeClass
						&& m_overrideChargeClasses != null
						&& chargeClassType != null
						&& m_overrideChargeClasses.contains(chargeClassType
								.toLowerCase()))
					classType = chargeClassType;

				if (!useUIChargeClass) {
					if (status == 80 && rbtType == 2) {
						classType = clipClassType;
					} else {
						for (int i = 0; subscriberSelections != null
								&& i < subscriberSelections.length; i++) {
							if (subscriberSelections[i].selType() == 2) {
								HashMap selectionExtraInfo = DBUtility
										.getAttributeMapFromXML(subscriberSelections[i]
												.extraInfo());
								int campaignId = -1;

								if (selectionExtraInfo != null
										&& selectionExtraInfo
												.containsKey(iRBTConstant.CAMPAIGN_ID)
										&& selectionExtraInfo
												.get(iRBTConstant.CAMPAIGN_ID) != null) {

									try {
										campaignId = Integer
												.parseInt(""
														+ selectionExtraInfo
																.get(iRBTConstant.CAMPAIGN_ID));
									} catch (Exception e) {
										campaignId = -1;
									}
								}
								logger.info("The value of campaign id - "
										+ campaignId);
								if (campaignId != -1) {
									RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO
											.getRBTBulkUploadTask(campaignId);

									if (m_corporateDiscountChargeClass != null
											&& null != bulkUploadTask
											&& bulkUploadTask.getTaskMode() != null
											&& m_corporateDiscountChargeClass
													.containsKey(bulkUploadTask
															.getTaskMode())) {
										logger.info("The value of m_corporateDiscountChargeClass id - "
												+ m_corporateDiscountChargeClass
														.toString());
										HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass
												.get(bulkUploadTask
														.getTaskMode());
										if (discountClassMap != null
												&& classType != null
												&& discountClassMap
														.containsKey(classType))
											classType = (String) discountClassMap
													.get(classType);
									}
								}
								break;
							}

						}
					}
				}

				String checkSelStatus = checkSelectionLimit(
						subscriberSelections, subID(callerID), inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				// Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil
						.getParametersCacheManager().getParameter("COMMON",
								"SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if (parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn,
						subID(subscriberID), subID(callerID), status, fromTime,
						toTime, graceDeselectedBy, rbtType);
				// //End
				count = createSubscriberStatus(subscriberID, callerID,
						categories.id(), subscriberWavFile, setTime, startDate,
						endDate, status, selectedBy, selectInfo,
						nextChargingDate, prepaid, classType, changeSubType,
						fromTime, toTime, sel_status, true, clipMap,
						categories.type(), useDate, loopStatus, isTata,
						nextPlus, rbtType, selInterval, extraInfo, refID,
						isDirectActivation, circleID, sub, useUIChargeClass,
						false);
				logger.info("Checking to update num max selections or not."
						+ " count: " + count + ", isPackSel: " + isPackSel
						+ " incrSelCount: " + incrSelCount);
				if (incrSelCount && isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn,
							subscriberID, packCosID);
				else if (incrSelCount && count == 1)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));

				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID),
							endDate, null);
				}

			} else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return count > 0 ? SELECTION_SUCCESS : SELECTION_FAILED_INTERNAL_ERROR;
	}

	public void addTrialSelectionReminder(String subID, String circleID,
			String classType, Date time, int i) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");

		try {
			TrialSelectionImpl.insert(conn, subID, circleID, classType,
					Calendar.getInstance().getTime(), i);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void deleteTrialSelectionReminder(String subID)
			throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			TrialSelectionImpl.deleteSubscriber(conn, subID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	// RW and Tata. //5
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType,
			String selInterval) {
		return addSubscriberSelections(subscriberID, callerID, categories,
				clipMap, setTime, startTime, endTime, status, selectedBy,
				selectionInfo, freePeriod, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, circleID,
				incrSelCount, true, null, false, isTata, inLoop,
				sub.subscriptionClass(), sub, 0, selInterval, null, false,
				null, false);
	}

	/**
	 * Returns the loop status to be populated for the new selection
	 * 
	 * @param inLoop
	 * @param subID
	 * @return
	 */
	public char getLoopStatusForNewSelection(boolean inLoop,
			String subscriberID, boolean isPrepaid) {
		SitePrefix userPrefix = Utility.getPrefix(subscriberID);
		char loopStatus = LOOP_STATUS_OVERRIDE_INIT;
		if (userPrefix != null && userPrefix.playUncharged(isPrepaid))
			loopStatus = LOOP_STATUS_OVERRIDE;

		if (inLoop) {
			loopStatus = LOOP_STATUS_LOOP_INIT;
			if (userPrefix != null && userPrefix.playUncharged(isPrepaid))
				loopStatus = LOOP_STATUS_LOOP;
		}

		return loopStatus;
	}

	public char getLoopStatusToUpateSelection(char oldLoopStatus,
			String subscriberID, boolean isPrepaid) {
		boolean playUncharged = playUncharged(subscriberID, isPrepaid);
		char newLoopStatus;
		if (oldLoopStatus == LOOP_STATUS_LOOP_FINAL
				|| oldLoopStatus == LOOP_STATUS_OVERRIDE_FINAL)
			newLoopStatus = oldLoopStatus;
		else if (playUncharged
				&& !(oldLoopStatus == LOOP_STATUS_LOOP_INIT || oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT))
			newLoopStatus = oldLoopStatus;
		else {
			if (oldLoopStatus == LOOP_STATUS_LOOP_INIT)
				newLoopStatus = LOOP_STATUS_LOOP;
			else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT)
				newLoopStatus = LOOP_STATUS_OVERRIDE;
			else {// here oldLoopStatus == LOOP_STATUS_JUNK
				newLoopStatus = LOOP_STATUS_OVERRIDE;
				if (allowLooping())
					newLoopStatus = LOOP_STATUS_LOOP;
			}
		}
		return newLoopStatus;
	}

	public boolean playUncharged(String subscriberID, boolean isPrepaid) {
		SitePrefix userPrefix = Utility.getPrefix(subID(subscriberID));
		boolean playUncharged = false;
		if (userPrefix != null)
			playUncharged = userPrefix.playUncharged(isPrepaid);

		return playUncharged;
	}

	/* ADDED FOR TATA */
	public boolean addActiveSubSelections(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, boolean isPrepaid, int fromTime, int toTime,
			Date nextChargingDate, String classType, boolean smActivation,
			int categoryType) {
		return (addActiveSubSelections(subID(subscriberID), callerID,
				categoryID, subscriberWavFile, setTime, startTime, endTime,
				status, selectedBy, selectionInfo, isPrepaid, fromTime, toTime,
				nextChargingDate, classType, smActivation, categoryType, 0));
	}

	public boolean addActiveSubSelections(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, boolean isPrepaid, int fromTime, int toTime,
			Date nextChargingDate, String classType, boolean smActivation,
			int categoryType, int rbtType) {

		if (nextChargingDate == null)
			return false;

		Connection conn = getConnection();

		try {
			SubscriberStatus subSel = SubscriberStatusImpl
					.addActiveSubSelections(conn, subID(subscriberID),
							subID(callerID), categoryID, subscriberWavFile,
							setTime, startTime, endTime, status, classType,
							selectedBy, selectionInfo, nextChargingDate,
							isPrepaid ? "y" : "n", fromTime, toTime,
							smActivation, STATE_ACTIVATED, null, null,
							categoryType, LOOP_STATUS_OVERRIDE_FINAL, rbtType);
			return (subSel != null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* weekly monthly changes */
	public boolean convertSelectionClassType(String subscriberID,
			String initType, String finalType, String mode) {
		return convertSelectionClassType(subID(subscriberID), initType,
				finalType, mode, 0);
	}

	public boolean convertSelectionClassType(String subscriberID,
			String initType, String finalType, String mode, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			ChargePromoTypeMap[] chargePromoTypeMaps = getChargePromoTypeMapsForType(
					finalType, "SEL", mode);
			if (chargePromoTypeMaps == null)
				return true;
			chargePromoTypeMaps = getChargePromoTypeMapsForType(initType,
					"SEL", mode);
			if (chargePromoTypeMaps == null)
				return true;
			ChargeClassMap[] initMaps = null;
			ChargeClassMap[] finalMaps = ChargeClassMapImpl
					.getChargeClassMapsForModeType(conn, mode, finalType);
			if (finalMaps != null) {
				for (int i = 0; i < finalMaps.length; i++) {
					String initTypeRegex = "";
					StringTokenizer finalMapRegex = new StringTokenizer(
							finalMaps[i].regexClass(), " ");
					while (finalMapRegex.hasMoreTokens()) {
						String token = finalMapRegex.nextToken();
						if (token.equals(finalType))
							initTypeRegex += "%";
						else
							initTypeRegex += token;
					}
					initMaps = ChargeClassMapImpl
							.getChargeClassMapsForClassTypeType(conn,
									finalMaps[i].classType(), initTypeRegex);
					if (initMaps != null)
						for (int j = 0; j < initMaps.length; j++)
							SubscriberStatusImpl.convertSelectionClassType(
									conn, subID(subscriberID),
									initMaps[j].finalClassType(),
									finalMaps[i].finalClassType(), null, null,
									rbtType);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	/* time of the day changes */
	public boolean isSelectionOverlap(String subscriberID, String callerID,
			Categories category, String wavFile, int status, int fromTime,
			int toTime, boolean doTODCheck) {
		return isSelectionOverlap(subID(subscriberID), callerID, category,
				wavFile, status, fromTime, toTime, doTODCheck, 0);
	}

	public boolean isSelectionOverlap(String subscriberID, String callerID,
			Categories category, String wavFile, int status, int fromTime,
			int toTime, boolean doTODCheck, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberStatus subscriberStatus = getAvailableSelection(conn,
					subID(subscriberID), subID(callerID), null, category,
					wavFile, status, fromTime, toTime, null, null, doTODCheck,
					false, rbtType, null, null);
			return subscriberStatus != null;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus[] getUnProcessedNormalSelections(
			String subscriberID, String status, boolean doQueryDesc) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getUnProcessedNormalSelections(conn,
					subID(subscriberID), status, doQueryDesc);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean smUpdateSelStatusForPackOnSubscriptionSuccess(
			String subscriberID, boolean isProfile) {
		Connection conn = getConnection();
		if (conn == null)
			return true;

		try {
			ArrayList<String> callers = null;
			SubscriberStatus[] selections = SubscriberStatusImpl
					.getUnProcessedProfileSelections(conn, subscriberID,
							isProfile);
			if (selections != null && selections.length > 1) {
				String curCall = "";
				callers = new ArrayList<String>();
				Date selTime = null;
				for (int i = 0; i < selections.length; i++) {
					String tmp = selections[i].callerID();
					if (!callers.contains(tmp)) {
						if ((curCall == null && tmp == null)
								|| (curCall != null && tmp != null && tmp
										.equalsIgnoreCase(curCall))) {
							SubscriberStatusImpl
									.deactivateOldProfileSelectionsForCaller(
											conn, subscriberID, tmp, selTime,
											isProfile);
							callers.add(selections[i].callerID());
						} else {
							curCall = tmp;
							selTime = selections[i].setTime();
						}

					} else
						curCall = "";
				}

			}

			if (!SubscriberStatusImpl.smUpdateSelStatusProfileSelections(conn,
					subscriberID, STATE_BASE_ACTIVATION_PENDING,
					STATE_TO_BE_ACTIVATED, isProfile))
				return false;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean smUpdateSelStatusSubscriptionSuccess(String subscriberID,
			boolean bRealTimeCharge) {
		return smUpdateSelStatusSubscriptionSuccess(subscriberID,
				bRealTimeCharge, false, false, null);// RBT-14301: Uninor MNP
														// changes.
	}

	// RBT-14301: Uninor MNP changes.
	public boolean smUpdateSelStatusSubscriptionSuccess(String subscriberID,
			boolean bRealTimeCharge, String circleId) {
		return smUpdateSelStatusSubscriptionSuccess(subscriberID,
				bRealTimeCharge, false, false, circleId);
	}

	// RBT-14301: Uninor MNP changes.
	public boolean updateCircleIdForSubscriber(boolean isSubscriberRec,
			String subscriberID, String circleId, String refId, String status) {
		boolean updated = false;
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (!isSubscriberRec) {
				updated = SubscriberStatusImpl.updateCircleId(conn,
						subscriberID, circleId, refId, status);
				return updated;
			}
			return SubscriberImpl.updateCircleId(conn, subscriberID, circleId,
					refId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// RBT-14301: Uninor MNP changes.
	public boolean smUpdateSelStatusSubscriptionSuccess(String subscriberID,
			boolean bRealTimeCharge, boolean isPack, boolean isUpgradeFailure,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return true;

		try {
			if (circleId != null) {
				SubscriberStatusImpl.updateCircleId(conn, subscriberID,
						circleId, null, STATE_BASE_ACTIVATION_PENDING);
			}
			if (m_addToDownloads) {

				boolean updateDownloads = SubscriberDownloadsImpl
						.smUpdateDownloadtoBeActivated(conn,
								subID(subscriberID), isPack, null);
				// Cricket and Profile selections are not being added into
				// Downloads table.
				// So in this case we have to update those selections which are
				// in base pending state to To beActivated
				SubscriberStatusImpl
						.smUpdateSelStatusOfCricketAndCorporateAndProfileSelections(
								conn, subscriberID,
								STATE_BASE_ACTIVATION_PENDING,
								STATE_TO_BE_ACTIVATED);
				return updateDownloads;
			}
			boolean isShuffle = false;
			ArrayList<String> callers = null;
			SubscriberStatus[] selections = SubscriberStatusImpl
					.getUnProcessedNormalSelections(conn, subscriberID);
			if (selections != null && selections.length > 1) {
				String curCall = "";
				callers = new ArrayList<String>();
				Date selTime = null;
				for (int i = 0; i < selections.length; i++) {
					if (selections[i].extraInfo() != null
							&& selections[i].extraInfo().contains("PACK"))
						continue;

					String tmp = selections[i].callerID();
					if (!callers.contains(tmp)) {
						if ((curCall == null && tmp == null)
								|| (curCall != null && tmp != null && tmp
										.equalsIgnoreCase(curCall))) {
							SubscriberStatusImpl
									.deactivateOldNormalSelectionsForCaller(
											conn, subscriberID, tmp, selTime);
							callers.add(selections[i].callerID());
						} else {
							curCall = tmp;
							selTime = selections[i].setTime();
							isShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isShuffleCategory(selections[i]
											.categoryType());
						}

					} else
						curCall = "";
				}

			}
			if (selections != null && selections.length == 1) {
				isShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(selections[0].categoryType());
			}
			String chargeClass = null;
			if (isUpgradeFailure && !isShuffle) {
				chargeClass = getNextChargeClass(subscriberID);
			}
			if (bRealTimeCharge) {
				if (!SubscriberStatusImpl.smUpdateSelStatusWithNoTransID(conn,
						subscriberID, STATE_BASE_ACTIVATION_PENDING,
						STATE_TO_BE_ACTIVATED, chargeClass))
					return false;
				if (!SubscriberStatusImpl.smUpdateSelStatusWithTransID(conn,
						subscriberID, STATE_BASE_ACTIVATION_PENDING, STATE_UN))
					return false;
			} else {

				if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
						"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
					if (!SubscriberStatusImpl.smUpdateSelStatusOnBaseSuccess(
							conn, subscriberID, STATE_BASE_ACTIVATION_PENDING,
							STATE_TO_BE_ACTIVATED, isPack, null, chargeClass))
						return false;
				} else if (!SubscriberStatusImpl.smUpdateSelStatus(conn,
						subscriberID, STATE_BASE_ACTIVATION_PENDING,
						STATE_TO_BE_ACTIVATED, isPack, null, chargeClass))
					return false;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean smUpdateSelStatusXbiSubscription(String subscriberID,
			String mappedChargeClass, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return true;
		if (m_addToDownloads) {
			logger.info("inside xbi  block");
			SubscriberDownloads[] subscriberDownloads = SubscriberDownloadsImpl
					.getSubscriberDownloadByClassType(conn, subscriberID,
							mappedChargeClass);
			if (subscriberDownloads != null && subscriberDownloads.length != 0) {
				boolean updateDownloads = SubscriberDownloadsImpl
						.smUpdateXbiDownloadtoBeDeActivated(conn, subscriberID,
								"w", STATE_DEACTIVATED_INIT, mappedChargeClass);
				if (subscriberDownloads != null
						&& subscriberDownloads.length != 0) {
					for (SubscriberDownloads suDownloads : subscriberDownloads) {

						SubscriberStatusImpl
								.smUpdateSelStatusOfXbiSelections(conn,
										subscriberID,
										STATE_BASE_ACTIVATION_PENDING,
										LOOP_STATUS_EXPIRED + "",
										suDownloads.promoId());
					}
				}
				return updateDownloads;
			}
		}
		return false;
	}

	public boolean deactivateActiveODAPack(String subscriberID, String refId,
			String packExtraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deactivateActiveODAPack = ProvisioningRequestsDao
				.deactivateActiveODAPack(subscriberID, refId, packExtraInfo);
		return deactivateActiveODAPack;
	}

	public boolean directDeactivateActiveODAPack(String subscriberID,
			String callerId, String refId, String packExtraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deactivateActiveODAPack = ProvisioningRequestsDao
				.directDeactivateODAPack(subscriberID, refId, packExtraInfo);
		if (deactivateActiveODAPack) {
			SubscriberStatusImpl.deactivateOldODASelectionsForCaller(conn,
					subscriberID, callerId, new Date(), refId);
		}
		return deactivateActiveODAPack;
	}

	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public boolean directDeactivatePack(String subscriberID, String refId,
			String packExtraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deactivateActiveODAPack = ProvisioningRequestsDao
				.directDeactivatePack(subscriberID, refId, packExtraInfo);
		return deactivateActiveODAPack;
	}

	public List<ProvisioningRequests> getActiveODAPackBySubscriberID(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		if (subscriberID == null)
			return null;

		List<ProvisioningRequests> packs = ProvisioningRequestsDao
				.getActiveODAPackBySubscriberID(subscriberID);
		List<ProvisioningRequests> activeODAPackList = new ArrayList<ProvisioningRequests>();
		if (packs != null && packs.size() > 0) {
			for (ProvisioningRequests provReq : packs) {
				int categoryId = provReq.getType();
				com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = rbtCacheManager
						.getCategory(categoryId);
				if (category != null
						&& category.getCategoryTpe() == PLAYLIST_ODA_SHUFFLE) {
					activeODAPackList.add(provReq);
				}
			}
		}
		return activeODAPackList;
	}

	public boolean smUpdateSelStatusODAPackSubscriptionSuccess(
			String subscriberID, boolean bRealTimeCharge, boolean isPack,
			int categoryID, boolean isProfilePack, String callerId, String refId , ProvisioningRequests pack) {
		Connection conn = getConnection();
		Category category = getCategory(categoryID);
		if (conn == null)
			return false;

		if (m_addToDownloads) {
			List<ProvisioningRequests> provisioningRequests = getBySubscriberIdTypeAndStatus(subscriberID, categoryID, 30, 0);
			if(provisioningRequests != null  && !provisioningRequests.isEmpty()){
			for (ProvisioningRequests provisioningRequest : provisioningRequests) {
				RBTDBManager.getInstance().updateProvisioningRequestsStatusAndExtraInfo(subscriberID, provisioningRequest.getTransId(), 33,
						provisioningRequest.getExtraInfo());
			}
			}
			SubscriberDownloads[] activeDownloads = getSubscriberDownloadsByDownloadStatusAndCategory(subscriberID, categoryID,
					category.getType(), "w");
			List<String> refIDList = new ArrayList<String>();
			int cosId = pack.getType();
			if (activeDownloads != null) {
				for (SubscriberDownloads subscriberDownload : activeDownloads) {

					refIDList.add(subscriberDownload.refID());
				}

			}

			boolean updateDownloads = false;
			if (refIDList.size() > 0) {
				updateDownloads = SubscriberDownloadsImpl.smUpdateDownloadtoBeActivated(conn, subID(subscriberID), isPack,
						refIDList);
			}

			// Cricket and Profile selections are not being added into
			// Downloads table.
			// So in this case we have to update those selections which are
			// in base pending state to To beActivated
			return updateDownloads;
		} else {
		SubscriberStatus[] activeSelections = getActiveSelectionBasedOnCallerId(
				subID(subscriberID), callerId);
		List<String> refIDList = new ArrayList<String>();
		if (activeSelections != null) {
			for (SubscriberStatus subscriberStatus : activeSelections) {
				if (subscriberStatus.selStatus().equals(
						STATE_BASE_ACTIVATION_PENDING)) {
					String extraInfo = subscriberStatus.extraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					String provRefId = null;
					if (xtraInfoMap != null
							&& xtraInfoMap.containsKey("PROV_REF_ID")) {
						provRefId = xtraInfoMap.get("PROV_REF_ID");
					}
					if (categoryID == subscriberStatus.categoryID()
							&& provRefId != null
							&& provRefId.equalsIgnoreCase(refId)) {
						refIDList.add(subscriberStatus.refID());
					}
				}
			}
		}

		logger.info("Number of records to be Updated for ODA Pack Subscription success = "
				+ refIDList.size());

		if (refIDList.size() > 0) {
			logger.info("Going to update ODA Pack Selection");
			if (!SubscriberStatusImpl.smUpdateSelStatus(conn, subscriberID,
					STATE_BASE_ACTIVATION_PENDING, STATE_TO_BE_ACTIVATED,
					isPack, refIDList, null))
				return false;
		}
		}
		return true;
	}

	public boolean smUpdateSelStatusPackSubscriptionSuccess(
			String subscriberID, boolean bRealTimeCharge, boolean isPack,
			int cosID, boolean isProfilePack) {
		Connection conn = getConnection();
		if (conn == null)
			return true;

		try {
			if (m_addToDownloads) {
				SubscriberDownloads[] activeDownloads = getActivateNActPendingDownloads(subID(subscriberID));
				List<String> refIDList = new ArrayList<String>();
				if (activeDownloads != null) {
					for (SubscriberDownloads subscriberDownload : activeDownloads) {
						if (subscriberDownload.downloadStatus() == STATE_DOWNLOAD_BASE_ACT_PENDING) {
							Map<String, String> extraInfoMap = DBUtility
									.getAttributeMapFromXML(subscriberDownload
											.extraInfo());
							String packId = (extraInfoMap != null) ? extraInfoMap
									.get(EXTRA_INFO_PACK) : null;
							if (packId == null
									|| packId.equals(String.valueOf(cosID))) {
								refIDList.add(subscriberDownload.refID());
							}
						}
					}
				}

				boolean updateDownloads = false;
				if (refIDList.size() > 0) {
					updateDownloads = SubscriberDownloadsImpl
							.smUpdateDownloadtoBeActivated(conn,
									subID(subscriberID), isPack, refIDList);
				}

				// Cricket and Profile selections are not being added into
				// Downloads table.
				// So in this case we have to update those selections which are
				// in base pending state to To beActivated
				SubscriberStatusImpl
						.smUpdateSelStatusOfCricketAndCorporateAndProfileSelections(
								conn, subscriberID,
								STATE_BASE_ACTIVATION_PENDING,
								STATE_TO_BE_ACTIVATED);
				return updateDownloads;
			}

			ArrayList<String> callers = null;
			SubscriberStatus[] selections = SubscriberStatusImpl
					.getUnProcessedNormalSelections(conn, subscriberID);
			if (selections != null && selections.length > 1) {
				String curCall = "";
				callers = new ArrayList<String>();
				Date selTime = null;
				for (int i = 0; i < selections.length; i++) {
					String tmp = selections[i].callerID();
					if (!callers.contains(tmp)) {
						if ((curCall == null && tmp == null)
								|| (curCall != null && tmp != null && tmp
										.equalsIgnoreCase(curCall))) {
							SubscriberStatusImpl
									.deactivateOldNormalSelectionsForCaller(
											conn, subscriberID, tmp, selTime);
							callers.add(selections[i].callerID());
						} else {
							curCall = tmp;
							selTime = selections[i].setTime();
						}

					} else
						curCall = "";
				}

			}

			if (bRealTimeCharge) {
				if (!SubscriberStatusImpl.smUpdateSelStatusWithNoTransID(conn,
						subscriberID, STATE_BASE_ACTIVATION_PENDING,
						STATE_TO_BE_ACTIVATED, null))
					return false;
				if (!SubscriberStatusImpl.smUpdateSelStatusWithTransID(conn,
						subscriberID, STATE_BASE_ACTIVATION_PENDING, STATE_UN))
					return false;
			} else {
				SubscriberStatus[] activeSelections = getAllActiveSubscriberSettings(subID(subscriberID));
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
							if (packId == null
									|| packId.equals(String.valueOf(cosID))) {
								refIDList.add(subscriberStatus.refID());
							}
						}
					}
				}

				if (refIDList.size() > 0) {
					if (!SubscriberStatusImpl.smUpdateSelStatus(conn,
							subscriberID, STATE_BASE_ACTIVATION_PENDING,
							STATE_TO_BE_ACTIVATED, isPack, refIDList, null))
						return false;

					// Update num_max_selections to 1 only if the user has more
					// than one pending selection.
					// Since the user has status=0 selection by default,
					// checking for size > 1
					if (!isProfilePack && refIDList.size() > 1)
						ProvisioningRequestsDao.updateNumMaxSelections(conn,
								subscriberID, String.valueOf(cosID));
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public void smUpdateSelStatus(String subscriberID, String fStatus,
			String tStatus, boolean isPack, List<String> refIDList) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberStatusImpl.smUpdateSelStatus(conn, subscriberID, fStatus,
					tStatus, isPack, refIDList, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public void smUpdateSelStatus(String subscriberID, String fStatus,
			String tStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberStatusImpl.smUpdateSelStatus(conn, subscriberID, fStatus,
					tStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public void smUpdateSelStatus(String subscriberID, String callerID,
			String subFile, Date setTime, String fStatus, String tStatus,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			SubscriberStatusImpl.smUpdateSelStatus(conn, subscriberID,
					callerID, subFile, setTime, fStatus, tStatus, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	/*
	 * The inLoop parametr has been added, as for the looped selections there is
	 * no need to check time overlap as we allow duplicate selections in the
	 * same time setting
	 */
	public SubscriberStatus getAvailableSelection(Connection conn,
			String subscriberID, String callerID, Categories category,
			String wavFile, int status, int fromTime, int toTime,
			boolean doTODCheck, boolean inLoop, String interval) {
		return getAvailableSelection(conn, subID(subscriberID), callerID, null,
				category, wavFile, status, fromTime, toTime, null, null,
				doTODCheck, inLoop, 0, interval, null);
	}

	public SubscriberStatus getAvailableSelection(Connection conn,
			String subscriberID, String callerID,
			SubscriberStatus[] subscriberStatus, Categories category,
			String wavFile, int status, int fromTime, int toTime,
			Date startDate, Date endDate, boolean doTODCheck, boolean inLoop,
			int rbtType, String interval, String mode) {
		boolean bShouldConnBeReleased = false;
		try {
			logger.info("Validating the selections. subscriberID: "
					+ subscriberID + ", wavFile: " + wavFile + ", status: "
					+ status + ", rbtType: " + rbtType);
			if (subscriberStatus == null || subscriberStatus.length == 0) {
				logger.info("Retruning null, since subscriber selections are null");
				return null;
			}

			if (conn == null) {
				bShouldConnBeReleased = true;
				conn = getConnection();
			}
			SubscriberStatus subStatus = null;
			if (status == 90) {
				if (_allowFeedUpgrade) {
					logger.info("Returning null, allowFeedUpgrade is true and status is 90 for subscriberID: "
							+ subscriberID);
					return null;
				}
				subStatus = SubscriberStatusImpl.smSubscriberSelections(conn,
						subID(subscriberID), subID(callerID), status, rbtType);
				if (subStatus != null && subStatus.status() == status) {
					logger.info("Returning subStatus: " + subStatus
							+ ", and status is 90 for subscriberID: "
							+ subscriberID);
					return subStatus;
				}
			} else {
				ArrayList<SubscriberStatus> subList = new ArrayList<SubscriberStatus>();
				String corpUserSelectionBlocked = CacheManagerUtil
						.getParametersCacheManager().getParameterValue("SMS",
								"CORP_CHANGE_SELECTION_ALL_BLOCK", "TRUE");

				String corpUserSelectionNotBlockedMode = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								"COMMON",
								"CORP_CHANGE_SELECTION_ALL_NOT_BLOCK_MODE",
								null);

				List<String> corpUserSelectonNotBlockedModeList = new ArrayList<String>();
				boolean isCorpUserSelectionBlockedMode = true;
				if (corpUserSelectionNotBlockedMode != null) {
					corpUserSelectonNotBlockedModeList = Arrays
							.asList(corpUserSelectionNotBlockedMode
									.split("\\,"));
					if (mode != null) {
						isCorpUserSelectionBlockedMode = !corpUserSelectonNotBlockedModeList
								.contains(mode);
					}
				}

				String corpUserProfileSelectionAllowed = CacheManagerUtil
						.getParametersCacheManager().getParameterValue("SMS",
								"CORP_ALLOWED_PROFILE_SELECTION", "FALSE");
				boolean isSelBlockedForCorpUser = isCorpUserSelectionBlockedMode
						&& corpUserSelectionBlocked.equalsIgnoreCase("TRUE");
				for (SubscriberStatus subscriberStatus2 : subscriberStatus) {

					if (!(status == 99 && corpUserProfileSelectionAllowed
							.equalsIgnoreCase("TRUE"))) {
						if (isSelBlockedForCorpUser
								&& subscriberStatus2.selType() == 2
								&& rbtType != 2) {
							logger.info("Returning available selection for the corporate user");
							return subscriberStatus2;
						}
					}

					if (subscriberStatus2.callerID() == null
							|| subscriberStatus2.callerID().equalsIgnoreCase(
									callerID))
						subList.add(subscriberStatus2);
				}
				subscriberStatus = subList.toArray(new SubscriberStatus[0]);

				if (category != null && subscriberStatus != null) {
					// RBT-5675 : To allow future date selection for different
					// song on same day.
					// if(status == 95) //future date
					// {
					// subStatus = isFutureDateOverlap(subscriberStatus,
					// subID(callerID), interval);
					// if(subStatus != null)
					// return subStatus;
					// }

					if ((doTODCheck && status == 75) || rbtType == 2) {
						subStatus = isCorpOverlap(subscriberStatus, category,
								wavFile, status, subID(callerID), rbtType,
								fromTime, toTime, interval);
						if (subStatus != null)
							return subStatus;
					}

					/*
					 * When TOD check is being skipped, the below check should
					 * not be skipped in order to block the selection for the
					 * same song, fromTime, toTime and selInterval
					 */
					if (!doTODCheck) {
						subscriberStatus = DataUtils.getRecentActiveSettings(
								RBTDBManager.getInstance(), subscriberStatus,
								"VP", null);
						for (SubscriberStatus setting : subscriberStatus) {
							if (areValuesEqual(callerID, setting.callerID())
									&& ((setting.status() == 80 && status == 80))) {
								// If fromTime, toTime ,selInterval are same,
								// allow Selection
								// UI should pass selInterval in same order i.e
								// W1,W2...
								if (setting.fromTime() == fromTime
										&& setting.toTime() == toTime
										&& areValuesEqual(
												setting.selInterval(), interval)) {
									if (sameSongOrCategory(category, wavFile,
											setting))
										return setting;
								}
							}
						}
					}

					if (doTODCheck) {
						subStatus = isTODOverlap(subscriberStatus, category,
								wavFile, status, subID(callerID), fromTime,
								toTime, inLoop, rbtType, interval);
						if (subStatus != null)
							return subStatus;
					}
					if (callerID != null) {
						subStatus = getCountSameSongSameCallerIDStatus1(
								subscriberStatus, category, wavFile,
								subID(callerID), inLoop, interval);
						if (subStatus != null)
							return subStatus;
					} else {
						subStatus = getCountSameSongNullCallerIDStatus1(
								subscriberStatus, category, wavFile, interval);
						if (subStatus != null)
							return subStatus;
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			if (bShouldConnBeReleased)
				releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus getAvailableConsentSelection(Connection conn,
			String subscriberID, String callerID,
			SubscriberStatus[] subscriberStatus, Categories category,
			String wavFile, int status, int fromTime, int toTime,
			Date startDate, Date endDate, boolean doTODCheck, boolean inLoop,
			int rbtType, String interval) {
		boolean bShouldConnBeReleased = false;
		try {
			logger.info("Verifying consent selections. subscriberID: "
					+ subscriberID + ", callerID: " + callerID + ", wavFile: "
					+ wavFile + ", status: " + status);
			if (subscriberStatus == null || subscriberStatus.length == 0) {
				logger.info("Retruning null, consent selections are null. "
						+ "subscriberID: " + subscriberID + ", callerID: "
						+ callerID + ", wavFile: " + wavFile);
				return null;
			}

			if (conn == null) {
				bShouldConnBeReleased = true;
				conn = getConnection();
			}
			SubscriberStatus subStatus = null;
			if (status == 90) {
				if (_allowFeedUpgrade)
					return null;
				subStatus = ConsentTableImpl.smSubscriberSelections(conn,
						subID(subscriberID), subID(callerID), status, rbtType);
				if (subStatus != null && subStatus.status() == status)
					return subStatus;
			} else {
				ArrayList<SubscriberStatus> subList = new ArrayList<SubscriberStatus>();
				String corpUserSelectionBlocked = CacheManagerUtil
						.getParametersCacheManager().getParameterValue("SMS",
								"CORP_CHANGE_SELECTION_ALL_BLOCK", "TRUE");
				String corpUserProfileSelectionAllowed = CacheManagerUtil
						.getParametersCacheManager().getParameterValue("SMS",
								"CORP_ALLOWED_PROFILE_SELECTION", "FALSE");
				boolean isSelBlockedForCorpUser = corpUserSelectionBlocked
						.equalsIgnoreCase("TRUE");
				for (SubscriberStatus subscriberStatus2 : subscriberStatus) {

					if (!(status == 99 && corpUserProfileSelectionAllowed
							.equalsIgnoreCase("TRUE"))) {
						if (isSelBlockedForCorpUser
								&& subscriberStatus2.selType() == 2
								&& rbtType != 2) {
							logger.info("Returning available selection for the corporate user. subscriberStatus2: "
									+ subscriberStatus2);
							return subscriberStatus2;
						}
					}

					if (subscriberStatus2.callerID() == null
							|| subscriberStatus2.callerID().equalsIgnoreCase(
									callerID))
						subList.add(subscriberStatus2);
				}
				subscriberStatus = subList.toArray(new SubscriberStatus[0]);

				if (category != null && subscriberStatus != null) {

					if ((doTODCheck && status == 75) || rbtType == 2) {
						subStatus = isCorpOverlap(subscriberStatus, category,
								wavFile, status, subID(callerID), rbtType,
								fromTime, toTime, interval);
						if (subStatus != null) {
							logger.info("Returning subStatus: " + subStatus
									+ ", subscriberID: " + subscriberID
									+ ", callerID: " + callerID + ", wavFile: "
									+ wavFile);
							return subStatus;
						}
					}

					/*
					 * When TOD check is being skipped, the below check should
					 * not be skipped in order to block the selection for the
					 * same song, fromTime, toTime and selInterval
					 */
					if (!doTODCheck) {
						subscriberStatus = DataUtils.getRecentActiveSettings(
								RBTDBManager.getInstance(), subscriberStatus,
								"VP", null);
						for (SubscriberStatus setting : subscriberStatus) {
							if (areValuesEqual(callerID, setting.callerID())
									&& ((setting.status() == 80 && status == 80))) {
								// If fromTime, toTime ,selInterval are same,
								// allow Selection
								// UI should pass selInterval in same order i.e
								// W1,W2...
								if (setting.fromTime() == fromTime
										&& setting.toTime() == toTime
										&& areValuesEqual(
												setting.selInterval(), interval)) {
									if (sameSongOrCategory(category, wavFile,
											setting)) {
										logger.info("Returning subStatus: "
												+ subStatus
												+ ", subscriberID: "
												+ subscriberID + ", callerID: "
												+ callerID + ", wavFile: "
												+ wavFile);
										return setting;
									}
								}
							}
						}
					}

					if (doTODCheck) {
						subStatus = isTODOverlap(subscriberStatus, category,
								wavFile, status, subID(callerID), fromTime,
								toTime, inLoop, rbtType, interval);
						if (subStatus != null) {
							logger.info("Returning subStatus: " + subStatus
									+ ", subscriberID: " + subscriberID
									+ ", callerID: " + callerID + ", wavFile: "
									+ wavFile);
							return subStatus;
						}
					}
					if (callerID != null) {
						subStatus = getCountSameSongSameCallerIDStatus1(
								subscriberStatus, category, wavFile,
								subID(callerID), inLoop, interval);
						if (subStatus != null) {
							logger.info("Returning subStatus: " + subStatus
									+ ", subscriberID: " + subscriberID
									+ ", callerID: " + callerID + ", wavFile: "
									+ wavFile);
							return subStatus;
						}
					} else {
						subStatus = getCountSameSongNullCallerIDStatus1(
								subscriberStatus, category, wavFile, interval);
						if (subStatus != null) {
							logger.info("Returning subStatus: " + subStatus
									+ ", subscriberID: " + subscriberID
									+ ", callerID: " + callerID + ", wavFile: "
									+ wavFile);
							return subStatus;
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			if (bShouldConnBeReleased)
				releaseConnection(conn);
		}
		logger.error("Returning null, subscriberID: " + subscriberID
				+ ", callerID: " + callerID + ", wavFile: " + wavFile);
		return null;
	}

	private SubscriberStatus isFutureDateOverlap(
			SubscriberStatus[] subscriberStatus, String callerID,
			String selInterval) {
		logger.info("inside isFutureDateOverlap()");

		for (SubscriberStatus setting : subscriberStatus) {
			if (sameCallerID(callerID, setting) && setting.status() == 95) {
				if (selInterval.equalsIgnoreCase(setting.selInterval())) {
					return setting;
				}
			}
		}
		return null;
	}

	private SubscriberStatus isCorpOverlap(SubscriberStatus[] subscriberStatus,
			Categories category, String wavFile, int status, String callerID,
			int rbtType, int fromTime, int toTime, String selInterval) {
		logger.info("Checking for overlap selections: " + subscriberStatus);

		for (SubscriberStatus setting : subscriberStatus) {

			String result = isIntervalOverlap(setting, selInterval);

			if (setting.selType() == 2 || rbtType == 2) {
				if (result != null && result.equalsIgnoreCase("overlap"))
					return setting;
			} else if (setting.selType() == 2 && status == 75) {
				if (setting.fromTime() == 0 && setting.toTime() == 2359) {
					if (result != null && result.equalsIgnoreCase("overlap"))
						return setting;
				}

			} else if (setting.status() == 75 && status == 75) {
				if (result != null && result.equalsIgnoreCase("overlap"))
					return setting;
			} else if (setting.status() == 75 && rbtType == 2) {
				if (fromTime == 0 && toTime == 2359) {
					if (result != null && result.equalsIgnoreCase("overlap"))
						return setting;
				}

			}
		}
		return null;
	}

	private String isIntervalOverlap(SubscriberStatus setting,
			String selInterval) {
		logger.info("New request selInterval: " + selInterval
				+ ", Selection CategoryID: " + setting.categoryID()
				+ ", callerId: " + setting.callerID() + ", wavFile: "
				+ setting.subscriberFile() + ", selInterval: "
				+ setting.selInterval());

		List settingDays = new ArrayList();
		List days = new ArrayList();

		if (setting.selInterval() != null) {
			settingDays = Arrays.asList(setting.selInterval().split(","));
		}

		if (selInterval != null) {
			days = Arrays.asList(selInterval.split(","));
		}

		int found = 0;
		int notFound = 0;

		for (int i = 0; i < days.size(); i++) {
			if (settingDays.contains(days.get(i))) {
				found++;
			} else {
				notFound++;
			}
		}

		if (found > 0 && found < days.size()) {
			logger.info("returning overlap");
			return "overlap";
		} else if (found == days.size() && found != settingDays.size()) {
			logger.info("returning overlap");
			return "overlap";
		} else if (found == days.size() && found == settingDays.size()) {
			logger.info("returning same_slot");
			return "same_slot";
		}

		logger.info("returning null ");
		return null;
	}

	private SubscriberStatus isTODOverlap(SubscriberStatus[] subscriberStatus,
			Categories category, String wavFile, int status, String callerID,
			int fromTime, int toTime, boolean inLoop, int rbtType,
			String selInterval) {

		HashSet<String> callerIdAndTimes = new HashSet<String>();
		int remainingMins = 1440;
		SubscriberStatus overrlapSelection = null;
		subscriberStatus = DataUtils.getRecentActiveSettings(
				RBTDBManager.getInstance(), subscriberStatus, "VP", null);

		logger.info("Verifiying request with the existing selections. subscriberStatus: "
				+ subscriberStatus
				+ ", selInterval: "
				+ selInterval
				+ ", wavFile: " + wavFile + ", status: " + status);

		if (null != subscriberStatus) {

			for (SubscriberStatus setting : subscriberStatus) {
				if (areValuesEqual(callerID, setting.callerID())
						&& ((setting.status() == 80 && status == 80))) {
					// If fromTime, toTime ,selInterval are same, allow
					// Selection
					// UI should pass selInterval in same order i.e W1,W2...
					if (setting.fromTime() == fromTime
							&& setting.toTime() == toTime
							&& areValuesEqual(setting.selInterval(),
									selInterval)) {
						if (sameSongOrCategory(category, wavFile, setting))
							return setting;
						else
							return null;
					} else if (checkSelectionOverlaping(setting.selInterval(),
							setting.fromTime(), setting.toTime(), selInterval,
							fromTime, toTime)) {
						return setting;
					}
				} else {
					// RBT-10315 Adding to check monthly overlap
					if (com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isMonthBasedInterval(selInterval)
							&& areValuesEqual(callerID, setting.callerID())
							&& (setting.status() == 75 && status == 75)) {

						if (areValuesEqual(setting.selInterval(), selInterval)) {
							if (sameSongOrCategory(category, wavFile, setting))
								return setting;
							else
								return null;
						} else if (isMonthlyIntervalOverlapping(selInterval,
								setting.selInterval())) {
							logger.info("Monthly selection is overlapping.");
							return setting;
						}
					}
					// end
				}

				if (setting.selInterval() == null && setting.status() == 80
						&& sameCallerID(callerID, setting)) {
					String selTimeKey = new StringBuilder()
							.append(setting.callerID() == null ? "" : setting
									.callerID()).append("_")
							.append(setting.fromTime()).append("_")
							.append(setting.toTime()).toString();
					if (callerIdAndTimes.contains(selTimeKey))
						continue;
					callerIdAndTimes.add(selTimeKey);
					overrlapSelection = setting;
					int fromTimeInMins = ((setting.fromTime() / 100) * 60)
							+ (setting.fromTime() % 100);
					int toTimeInMins = ((setting.toTime() / 100) * 60)
							+ (setting.toTime() % 100);
					if (setting.fromTime() > setting.toTime()) {
						remainingMins = remainingMins - (toTimeInMins - 0) - 1;
						remainingMins = remainingMins - (1439 - fromTimeInMins)
								- 1; // 23:59
						// corresponds
						// to
						// 1439
						// mins
					} else {
						remainingMins = remainingMins
								- (toTimeInMins - fromTimeInMins) - 1;
					}
				}
			}
		} else {
			logger.warn("Not performing validation, subscriberStatus is null.");
		}

		if (remainingMins <= 0) {
			logger.info("Returning overrlapSelection: " + overrlapSelection);
			return overrlapSelection;
		}

		logger.info("Returning null, none of the selections are overlapping");
		return null;
	}

	private boolean areValuesEqual(String selInterval, String selInterval2) {
		if (selInterval == null && selInterval2 == null)
			return true;
		if (selInterval != null && selInterval.equalsIgnoreCase(selInterval2))
			return true;
		return false;
	}

	private boolean checkSelectionOverlaping(String dbDOW, int dbFromHours,
			int dbToHours, String inDOW, int inFromHours, int inToHours) {
		boolean isOverlaping = false;

		if (dbDOW == null || dbDOW.equals(""))
			dbDOW = "W1,W2,W3,W4,W5,W6,W7";
		if (inDOW == null || inDOW.equals(""))
			inDOW = "W1,W2,W3,W4,W5,W6,W7";

		String[] dbDays = dbDOW.split(",");
		for (int i = 0; i < dbDays.length; i++) {
			int dayOfWeek = Integer.parseInt(dbDays[i].substring(1));
			int dbFromHoursInMinutes = getTime(dayOfWeek, dbFromHours);
			int dbToHoursInMinutes = getTime(dayOfWeek, dbToHours);
			if (dbFromHours > dbToHours) {
				dayOfWeek++;
				dbToHoursInMinutes = getTime(dayOfWeek, dbToHours);
			}

			String[] inDays = inDOW.split(",");
			for (int j = 0; j < inDays.length; j++) {
				int extra = 7 * 24 * 60;
				;
				int dayOfWeek1 = Integer.parseInt(inDays[j].substring(1));
				int inFromHoursInMinutes = getTime(dayOfWeek1, inFromHours);
				int inToHoursInMinutes = getTime(dayOfWeek1, inToHours);

				inFromHoursInMinutes += (dbDays[i].equalsIgnoreCase("W7")
						&& inDays[j].equalsIgnoreCase("W1") ? extra : 0);
				inToHoursInMinutes += (dbDays[i].equalsIgnoreCase("W7")
						&& inDays[j].equalsIgnoreCase("W1") ? extra : 0);

				dbFromHoursInMinutes += (dbDays[i].equalsIgnoreCase("W1")
						&& inDays[j].equalsIgnoreCase("W7") ? extra : 0);
				dbToHoursInMinutes += (dbDays[i].equalsIgnoreCase("W1")
						&& inDays[j].equalsIgnoreCase("W7") ? extra : 0);

				if (inFromHours > inToHours) {
					dayOfWeek1++;
					inToHoursInMinutes = getTime(dayOfWeek1, inToHours)
							+ (dbDays[i].equalsIgnoreCase("W7")
									&& inDays[j].equalsIgnoreCase("W1") ? extra
									: 0);
					;
				}

				logger.info("dbFromHoursInMinutes: " + dbFromHoursInMinutes
						+ ", dbToHoursInMinutes:" + dbToHoursInMinutes);
				logger.info("inFromHoursInMinutes:" + inFromHoursInMinutes
						+ ", inToHoursInMinutes" + inToHoursInMinutes);

				if ((inFromHoursInMinutes > dbFromHoursInMinutes && inFromHoursInMinutes <= dbToHoursInMinutes)
						|| (inToHoursInMinutes > dbFromHoursInMinutes && inToHoursInMinutes <= dbToHoursInMinutes)
						|| (inFromHoursInMinutes <= dbFromHoursInMinutes && inToHoursInMinutes >= dbToHoursInMinutes)) {
					logger.info("Selections are Overlaping");
					isOverlaping = true;
					return isOverlaping;
				}
			}
		}
		return isOverlaping;
	}

	private int getTime(int dayOfWeek, int inHoursNMinutes) {
		int result = 0;
		int hours = 0;
		int selHours = 0;
		int selMinutes = 0;

		if (String.valueOf(inHoursNMinutes).length() > 0) {
			DecimalFormat decimalFormat = new DecimalFormat("0000");
			String strSelHoursNMinutes = decimalFormat.format(inHoursNMinutes);
			selHours = Integer.parseInt(strSelHoursNMinutes.substring(0, 2));
			selMinutes = Integer.parseInt(strSelHoursNMinutes.substring(2, 4));
		}

		switch (dayOfWeek) {
		case 1:
			hours = 0 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 2:
			hours = 1 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 3:
			hours = 2 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 4:
			hours = 3 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 5:
			hours = 4 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 6:
			hours = 5 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 7:
			hours = 6 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		case 8:
			hours = 7 * 24;
			result = (hours + selHours) * 60 + selMinutes;
			break;
		}
		return result;
	}

	private boolean sameCallerID(String callerID,
			SubscriberStatus subscriberStatus) {
		if ((subID(callerID) == null && subscriberStatus.callerID() == null)
				|| subID(callerID).equals(subscriberStatus.callerID()))
			return true;
		else
			return false;
	}

	private boolean sameSongOrCategory(Categories category, String wavFile,
			SubscriberStatus subscriberStatus) {
		if ((category.type() == SHUFFLE || category.type() == 9 || category
				.type() == 10)
				&& subscriberStatus.categoryID() == category.id())
			return true;
		else if (subscriberStatus.subscriberFile() != null && wavFile != null
				&& subscriberStatus.subscriberFile().equals(wavFile))
			return true;

		return false;
	}

	public SubscriberStatus[] getTimeOfTheDaySelections(String subID,
			String callID) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getTimeOfTheDaySelections(conn, subID,
					callID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	private SubscriberStatus getCountSameSongSameCallerIDStatus1(
			SubscriberStatus[] subscriberStatus, Categories category,
			String wavFile, String callerID, boolean inLoop, String interval) {
		logger.info("Verifying existing selections for special caller. wavFile: "
				+ wavFile
				+ ", callerID: "
				+ callerID
				+ ", interval: "
				+ interval);
		boolean isNewCategoryShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
				.isShuffleCategory(category.type());
		for (int i = 0; i < subscriberStatus.length; i++) {
			String selCallerID = subscriberStatus[i].callerID();
			int selStatus = subscriberStatus[i].status();
			int selCategoryType = subscriberStatus[i].categoryType();
			int selCategoryID = subscriberStatus[i].categoryID();
			String selSelInterval = subscriberStatus[i].selInterval();
			String selSubscriberFile = subscriberStatus[i].subscriberFile();
			logger.info("Selection details are: selStatus: " + selStatus
					+ ", selCategoryID: " + selCategoryID
					+ ", selCategoryType: " + selCategoryType
					+ ", selSelInterval: " + selSelInterval
					+ ", selSubscriberFile: " + selSubscriberFile);
			boolean normalSelectionForThisCallerIDPresent = false;
			if (subID(callerID).equals(subscriberStatus[i].callerID())
					&& (selStatus == 1 || selStatus == 75 || selStatus == 95
							|| selStatus == 94 || selStatus == 94)) {
				boolean isExistingCategoryShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(selCategoryType);
				normalSelectionForThisCallerIDPresent = true;
				if (isNewCategoryShuffle || isExistingCategoryShuffle) {
					logger.info("Selection categoryType is of "
							+ "shuffle type. categoryID: " + selCategoryID
							+ ", categoryType: " + selCategoryType
							+ ", interval: " + interval + ", selSelInterval: "
							+ selSelInterval);
					if (selCategoryID == category.id()) {
						if (interval != null && selSelInterval != null) {
							if (interval.equalsIgnoreCase(selSelInterval)) {
								logger.info("Selection wavFile: " + wavFile
										+ " is already exists. interval: "
										+ interval + ", selection interval: "
										+ selSelInterval + " are same");
								return subscriberStatus[i];
							}

						} else if (interval == null && selSelInterval == null) {
							logger.info("Selection wavFile: " + wavFile
									+ " is already exists. interval: "
									+ interval + ", selection interval: "
									+ selSelInterval + " are null");
							return subscriberStatus[i];
						} else if (interval != null && selSelInterval == null) {
							logger.info("Considering selection wavFile: "
									+ wavFile
									+ " is already exists. interval: "
									+ interval + ", selection interval: "
									+ selSelInterval + " are null");
							return subscriberStatus[i];
						} else {
							return null;
						}
					}
				} else if (selSubscriberFile != null && wavFile != null
						&& selSubscriberFile.equals(wavFile)) {
					logger.info("The requested wavFile is already exists in "
							+ "selections. " + ", interval: " + interval
							+ ", selection interval: " + selSelInterval);
					if (interval != null && selSelInterval != null) {
						if (interval.equalsIgnoreCase(selSelInterval)) {
							logger.info("Intervals are same, returning selection: "
									+ subscriberStatus
									+ "request interval: "
									+ interval
									+ ", selection interval: "
									+ selSelInterval);
							return subscriberStatus[i];
						}

					} else if (interval == null && selSelInterval == null) {
						logger.info("Intervals are null, returning selection: "
								+ subscriberStatus);
						return subscriberStatus[i];
					} else if (!allowAllDaySelForTimeSel
							&& (interval != null && selSelInterval == null)) {
						logger.info("Considering selection wavFile: " + wavFile
								+ " is already exists. interval: " + interval
								+ ", selection interval: " + selSelInterval
								+ " are null. ");
						return subscriberStatus[i];
					} else {
						logger.info("Retruning null, only wavFile: " + wavFile
								+ " is same");
						return null;
					}
				}
			}

			if (!normalSelectionForThisCallerIDPresent
					&& (!inLoop && !allowAllCallerSelectionForSplCaller))
				return getCountSameSongNullCallerIDStatus1(subscriberStatus,
						category, wavFile, interval);
		}

		logger.info("Returning null, subscriberStatus: " + subscriberStatus
				+ " wavFile: " + wavFile + ", callerID: " + callerID
				+ ", interval: " + interval);
		return null;
	}

	private SubscriberStatus getCountSameSongNullCallerIDStatus1(
			SubscriberStatus[] subscriberStatus, Categories category,
			String wavFile, String interval) {
		logger.info("Verifying existing selections for all caller. wavFile: "
				+ wavFile + ", interval: " + interval + ", category: "
				+ category.id());
		boolean isNewCategoryShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
				.isShuffleCategory(category.type());

		for (int i = 0; i < subscriberStatus.length; i++) {
			String selCallerID = subscriberStatus[i].callerID();
			int selStatus = subscriberStatus[i].status();
			int selCategoryType = subscriberStatus[i].categoryType();
			int selCategoryID = subscriberStatus[i].categoryID();
			String selSelInterval = subscriberStatus[i].selInterval();
			String selSubscriberFile = subscriberStatus[i].subscriberFile();
			logger.info("Selection details are: selStatus: " + selStatus
					+ ", selCategoryID: " + selCategoryID
					+ ", selCategoryType: " + selCategoryType
					+ ", selSelInterval: " + selSelInterval
					+ ", selSubscriberFile: " + selSubscriberFile);

			if (selCallerID == null
					&& (selStatus == 1 || selStatus == 75 || selStatus == 95
							|| selStatus == 94 || selStatus == 79 || selStatus == 92)) {
				boolean isExistingCategoryShuffle = com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(selCategoryType);
				if (isNewCategoryShuffle || isExistingCategoryShuffle) {
					logger.info("Selection categoryType is of "
							+ "shuffle type. categoryID: " + selCategoryID
							+ ", categoryType: " + selCategoryType
							+ ", interval: " + interval + ", selSelInterval: "
							+ selSelInterval);
					if (selCategoryID == category.id()) {
						if (interval != null && selSelInterval != null) {
							if (interval.equalsIgnoreCase(selSelInterval)) {
								logger.info("Selection wavFile: " + wavFile
										+ " is already exists. interval: "
										+ interval + ", selection interval: "
										+ selSelInterval + " are same");
								return subscriberStatus[i];
							}

						} else if (interval == null && selSelInterval == null) {
							logger.info("Selection wavFile: " + wavFile
									+ " is already exists. interval: "
									+ interval + ", selection interval: "
									+ selSelInterval + " are null");
							return subscriberStatus[i];
						} else if (!allowAllDaySelForTimeSel
								&& (interval != null && selSelInterval == null)) {
							logger.info("Considering selection wavFile: "
									+ wavFile
									+ " is already exists. interval: "
									+ interval + ", selection interval: "
									+ selSelInterval + " are null. ");
							return subscriberStatus[i];
						} else {
							logger.info("Retruning null, only wavFile: "
									+ wavFile + " is same");
							return null;
						}
					}
				} else {
					logger.debug("Selection categoryType is not of "
							+ "shuffle type. categoryID: " + selCategoryID
							+ ", categoryType: " + selCategoryType
							+ ", interval: " + interval + ", selSelInterval: "
							+ selSelInterval);
					if (selSubscriberFile != null && wavFile != null
							&& selSubscriberFile.equals(wavFile)) {
						if (interval != null && selSelInterval != null) {
							if (interval.equalsIgnoreCase(selSelInterval)) {
								logger.info("Selection wavFile: " + wavFile
										+ " is already exists. interval: "
										+ interval + ", selection interval: "
										+ selSelInterval + " are same");
								return subscriberStatus[i];
							}

						} else if (interval == null && selSelInterval == null) {
							logger.info("Selection wavFile: " + wavFile
									+ " is already exists. request interval"
									+ " and selection interval are null");
							return subscriberStatus[i];
						} else if (interval != null && selSelInterval == null) {
							logger.info("Considering selection wavFile: "
									+ wavFile
									+ " is already exists. interval: "
									+ interval + ", selection interval: "
									+ selSelInterval + " are null. ");
							return subscriberStatus[i];
						} else {
							logger.info("Retruning null, only wavFile: "
									+ wavFile + " is same");
							return null;
						}
					}
				}
			}
		}

		logger.info("Returning null, none of the selections are overlapping "
				+ "for all caller. wavFile: " + wavFile + ", interval: "
				+ interval);
		return null;
	}

	public SubscriberStatus[] getActiveStatus(String subscriberID,
			boolean checkStartTime) {
		return getActiveStatus(subID(subscriberID), checkStartTime, 0);
	}

	public SubscriberStatus[] getActiveStatus(String subscriberID,
			boolean checkStartTime, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getActiveStatusSubscribers(conn, 90,
					checkStartTime, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getActiveFeedSubscribers(String feedType,
			boolean checkStartTime) {
		return getActiveFeedSubscribers(feedType, checkStartTime, 0);
	}

	public SubscriberStatus[] getActiveFeedSubscribers(String feedType,
			boolean checkStartTime, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		int status = 90;
		StatusType[] statusType = StatusTypeImpl.getStatusTypes(conn);
		try {
			if (statusType != null) {
				for (int i = 0; i < statusType.length; i++) {
					if (statusType[i].desc().toLowerCase()
							.indexOf(feedType.toLowerCase()) != -1) {
						status = statusType[i].code();
						break;
					}
				}
			}
			return SubscriberStatusImpl.getActiveStatusSubscribers(conn,
					status, checkStartTime, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus getViralSelection(String subscriberID,
			String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getSubscriberFile(conn,
					subID(subscriberID), subID(callerID), false, "ALL", null);
			if (subscriberStatus != null) {
				// String circleID = dbManager.getCircleId(subscriberID);
				Subscriber sub = dbManager.getSubscriber(subscriberID);
				char prepaidYes = 'n';
				if (sub.prepaidYes())
					prepaidYes = 'y';
				Categories category = this.getCategory(
						subscriberStatus.categoryID(), sub.circleID(),
						prepaidYes);
				if (category != null
						&& (subscriberStatus.categoryType() == SHUFFLE || subscriberStatus
								.categoryType() == RECORD))
					subscriberStatus = null;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public boolean deactiveSubscriberFeedStatus(int status) {
		return deactiveSubscriberFeedStatus(status, 0);
	}

	public boolean deactiveSubscriberFeedStatus(int status, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberFeedStatus(conn,
					status, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* all methods related to subscriber cdr */
	public SubscriberCDR createSubscriberCDR(String subscriberID,
			Date selectionTime, String callerID, int categoryID,
			String subscriberWavFile, int status, String prepaid,
			String classType, String selectedBy, String selectionInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberCDRImpl.insert(conn, subID(subscriberID),
					selectionTime, subID(callerID), categoryID,
					subscriberWavFile, status, prepaid, classType, selectedBy,
					selectionInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberCDR[] getSubscriberCDR(String date) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberCDRImpl.getSubscriberCDR(conn, date);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeSubscriberCDR(String subscriberID, Date selectionTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberCDRImpl.remove(conn, subID(subscriberID),
					selectionTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberCDR[] getCDR(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberCDRImpl.getCDR(conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* all methods related to subscriber charging */
	public SubscriberCharging createSubscriberCharging(String subscriberID,
			String classType, int maxSelections) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberChargingImpl.insert(conn, subID(subscriberID),
					classType, maxSelections);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSubscriberCharging(String subscriberID,
			String classType, int maxSelections) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberChargingImpl.update(conn, subID(subscriberID),
					classType, maxSelections);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberCharging[] getSubscriberCharging(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberChargingImpl.getSubscriberCharging(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberCharging[] getAllCharging() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberChargingImpl.getAllCharging(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSelection(String subscriberID,
			String subscriberWavFile, int fromTime, int toTime,
			String selInterval, String internalRefId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.updateSelection(conn,
					subID(subscriberID), subscriberWavFile, fromTime, toTime,
					selInterval, internalRefId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeSubscriberCharging(String subscriberID,
			String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberChargingImpl.removeType(conn, subID(subscriberID),
					classType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int addSubscriberCharging(String subscriberID, String prepaid,
			String classType, boolean changeSubType, int freePeriod) {
		if (freePeriod != 0)
			return 0;

		Connection conn = getConnection();
		if (conn == null)
			return 0;

		try {
			boolean isPrepaid = false;
			if (prepaid != null & prepaid.equalsIgnoreCase("y"))
				isPrepaid = true;
			Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
					subID(subscriberID));
			if (subscriber != null && isPrepaid != subscriber.prepaidYes()
					&& changeSubType) {
				logger.info("RBT::prepaid column is changing for subscriber "
						+ subscriberID);
				SubscriberImpl.setPrepaidYes(conn, subscriber.subID(),
						isPrepaid);
			}

			int maxSelections = 0;
			int freeSelections = 0;
			String selectionType = null;

			int currentSelections = getCurrentSelections(subID(subscriberID),
					classType);

			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(classType);
			if (chargeClass != null) {
				freeSelections = chargeClass.getFreeSelection();
				selectionType = chargeClass.getSelectionType();
			}

			SubscriberCharging subscriberCharging = null;
			SubscriberCharging[] subscriberChargings = SubscriberChargingImpl
					.getSubscriberCharging(conn, subID(subscriberID));
			if (subscriberChargings != null) {
				for (int i = 0; i < subscriberChargings.length; i++) {
					if (classType != null
							&& subscriberChargings[i].classType() != null
							&& classType
									.equalsIgnoreCase(subscriberChargings[i]
											.classType())) {
						subscriberCharging = subscriberChargings[i];
						maxSelections = subscriberChargings[i].maxSelections();
						break;
					}
				}
			}

			if (subscriberCharging != null) {
				if (currentSelections > freeSelections
						&& (maxSelections - currentSelections) < 0
						&& ((selectionType != null && selectionType
								.equalsIgnoreCase("SELECTIONS")) || (freeSelections != 0)))
					return -1;
			} else {
				if (currentSelections > freeSelections
						&& ((selectionType != null && selectionType
								.equalsIgnoreCase("SELECTIONS")) || (freeSelections != 0))) {
					logger.info("Current Selection is greater than Free selections "
							+ currentSelections + " > " + freeSelections);
					return -1;
				}
			}
			logger.info("Current Selection is Not greater than Free selections before if "
					+ currentSelections + " < " + freeSelections);

			if (freeSelections > currentSelections) {
				logger.info("Current Selection is greater than Free selections  inside if "
						+ currentSelections + " > " + freeSelections);
				return (freeSelections - currentSelections);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	/* all methods related to subscriber */
	public Subscriber createSubscriber(String subscriberID, String activate,
			String deactivate, Date startDate, Date endDate, String prepaid,
			Date accessDate, Date nextChargingDate, int access, String actInfo,
			String subscriptionClass, String lastDeactivationInfo,
			Date lastDeactivationDate, Date activationDate,
			String subscription, String cosID, String activatedCosID,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.insert(conn, subID(subscriberID), activate,
					deactivate, startDate, endDate, prepaid, accessDate,
					nextChargingDate, access, actInfo, subscriptionClass,
					lastDeactivationInfo, lastDeactivationDate, activationDate,
					subscription, 0, cosID, activatedCosID, 0, null, false,
					null, circleId, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean changeSubscriberType(String subscriberID, boolean prepaidYes) {
		return changeSubscriberType(subscriberID, prepaidYes, 0);
	}

	public boolean changeSubscriberType(String subscriberID,
			boolean prepaidYes, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriberID != null) {
				boolean success = SubscriberImpl.setPrepaidYes(conn,
						subID(subscriberID), prepaidYes);
				success = SubscriberStatusImpl.setPrepaidYes(conn,
						subID(subscriberID), prepaidYes, rbtType);
				return success;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean setActivationInfo(String subscriberID, String actinfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriberID != null)
				return SubscriberImpl.setActivationInfo(conn,
						subID(subscriberID), actinfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean setDeActivationInfo(String subscriberID, String deactinfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriberID != null)
				return SubscriberImpl.setDeActivationInfo(conn,
						subID(subscriberID), deactinfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String getNextActSeq() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		long transid = 0;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "SELECT RBT_ACTTRANS_SEQ.nextval  FROM DUAL";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next())
				transid = rs.getInt(1);
			return "" + transid;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}
		return null;
	}

	private void closeStatement(Statement stmt) {
		try {
			stmt.close();
		} catch (Throwable e) {
			logger.error("Exception in closing statement", e);
		}
	}

	public Subscriber getSubscriberForSMCallbacks(Connection conn,
			String subscriberID) {
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getSubscriber(conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber getSubscriber(String subscriberID) {
		try {
			return getSubscriber(subscriberID, false);
		} catch (RBTException e) {
			logger.error("Exception in getting subscriber", e);
		}
		return null;
	}

	public RBTProtocol getSubscriberByProtocolId(String protocolId,
			String subscriberId) {
		Connection conn = null;
		Subscriber subscriber = null;
		logger.info("Fetching subscriber details. protocolId: " + protocolId
				+ ", subscriberId: " + subscriberId);
		RBTProtocol rbtProtocol = null;
		try {

			conn = getConnection();
			if (conn == null) {
				logger.info("Returning null, not able to get db connection.");
				return null;
			}

			rbtProtocol = RBTProtocolDao.getInstance().get(protocolId,
					subscriberId);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		logger.info("Returning subscriber: " + subscriber + ", rbtProtocol: "
				+ rbtProtocol);
		return rbtProtocol;
	}

	public Subscriber getSubscriber(String subscriberID,
			boolean throwErrorOnFailure) throws RBTException {
		Connection conn = null;
		try {
			if (subscriberID == null) {
				logger.info("Since subscriberID is null, not querying the DB");
				return null;
			}

			conn = getConnection();
			if (conn == null) {
				if (throwErrorOnFailure) {
					RBTException e = new RBTException("DB Connection not found");
					logger.error("", e);
					throw e;
				}
				return null;
			}
			Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
					subID(subscriberID));
			return subscriber;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] getSubscribersForDeactivation(int thresholdPeriod,
			String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getSubscribersforDeactivation(conn,
					thresholdPeriod, classType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] getSubscribersForDeactivationAlert(Date lastTime1,
			Date lastTime2, String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getSubscribersForDeactivationAlert(conn,
					lastTime1, lastTime2, classType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber getSubscriberWithActivatedBy(String subscriberID,
			String activatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
					subID(subscriberID));
			if (subscriber != null
					&& activatedBy.equalsIgnoreCase(subscriber.activatedBy()))
				return subscriber;
			else
				return null;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] getSubsTobeDeactivated(int numRows) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getSubsTobeDeactivated(conn, numRows);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int cleanOldSubscribers(float duration, boolean m_useSM) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			return SubscriberImpl.removeOldSubscribers(conn, duration, m_useSM);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public int cleanOldSelections(float duration, boolean m_useSM) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			return SubscriberStatusImpl.removeOldSelections(conn, duration,
					m_useSM);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public int cleanOldDownloads(float duration) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			return SubscriberDownloadsImpl.deleteDeactivatedDownloads(conn,
					duration);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public int removeOldBookMarks(float duration) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			return SubscriberDownloadsImpl.removeOldBookMarks(conn, duration);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public boolean removeSubscriber(Subscriber subscriber) {
		if (subscriber == null)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.getSubscriber(conn, subscriber.subID()) == null ? false
					: SubscriberImpl.remove(conn, subscriber.subID());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date date, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, String circleId) {
		return activateSubscriber(subID(subscriberID), activate, date, null,
				isPrepaid, activationTimePeriod, freePeriod, actInfo,
				classType, smActivation, null, false, 0, circleId);
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date date, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, HashMap extraInfo) {
		return activateSubscriber(subID(subscriberID), activate, null, null,
				isPrepaid, activationTimePeriod, freePeriod, actInfo,
				classType, smActivation, null, false, 0, extraInfo, null, null,
				false, null); // Added
		// extraInfo
		// here,
		// both
		// startDate
		// and
		// endDate
		// is
		// null
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, String circleId) {
		return activateSubscriber(subID(subscriberID), activate, startDate,
				endDate, isPrepaid, activationTimePeriod, freePeriod, actInfo,
				classType, smActivation, null, false, 0, circleId);
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date date, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, boolean isDirectAct, int rbtType) {
		return activateSubscriber(subID(subscriberID), activate, date, null,
				isPrepaid, activationTimePeriod, freePeriod, actInfo,
				classType, smActivation, null, isDirectAct, rbtType, null);
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date date, boolean isPrepaid, int activationTimePeriod,
			int freePeriod, String actInfo, String classType,
			boolean smActivation, CosDetails cos, int rbtType, String circleId) {
		return activateSubscriber(subscriberID, activate, date, null,
				isPrepaid, activationTimePeriod, freePeriod, actInfo,
				classType, smActivation, cos, false, rbtType, circleId);
	}

	public boolean activateSubscriber(HashMap subscriberDetail) {
		String subscriberID = (String) subscriberDetail.get("SUBSCRIBER_ID");
		String activatedBy = (String) subscriberDetail.get("ACT_BY");
		String activationInfo = (String) subscriberDetail.get("ACT_INFO");
		CosDetails cos = (CosDetails) subscriberDetail.get("COS_DETAIL");
		String prepaidYes = (String) subscriberDetail.get("IS_PREPAID");
		String circleId = (String) subscriberDetail.get("CIRCLE_ID");
		String winresponse = null;
		if (subscriberDetail.get("WINRESPONSE") != null)
			winresponse = (String) subscriberDetail.get("WINRESPONSE");
		if (winresponse != null)
			winresponse = winresponse.replaceAll("#", "|");

		boolean isPrepaid = false;
		if (prepaidYes.equalsIgnoreCase("y"))
			isPrepaid = true;

		int activationPeriod = 0;
		String classType = cos.getSubscriptionClass();
		boolean smActivation = false;
		HashMap extraInfo = new HashMap();
		if (winresponse != null)
			extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, winresponse);

		Subscriber sub = activateSubscriber(subscriberID, activatedBy, null,
				null, isPrepaid, activationPeriod, 0, activationInfo,
				classType, smActivation, cos, true, 0, extraInfo, circleId,
				null, false, null);

		return sub != null ? true : false;
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, String circleId) {
		return activateSubscriber(subscriberID, activate, null, null,
				isPrepaid, activationTimePeriod, 0, actInfo, classType,
				smActivation, cos, isDirectActivation, rbtType, null, circleId,
				null, false, null);
	}

	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo,
			String circleID, String refId, boolean isComboRequest,
			Map<String, String> xtraParametersMap) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		Subscriber subscriber = null;
		try {
			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			if (!isTNBNewFlow && classType != null
					&& tnbSubscriptionClasses.contains(classType)
					&& endDate == null) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, +1);
				endDate = endCal.getTime();
			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
			}

			if (cos != null && !cos.isDefaultCos())
				endDate = cos.getEndDate();
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;

			if (isDirectActivation)
				subscription = STATE_ACTIVATED;

			String cosID = null;
			if (cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();
				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = null;
				List<String> viralSMSList = Arrays.asList("BASIC,CRICKET"
						.split(","));
				if (isViralSmsTypeListForNewTable(viralSMSList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}

			if (_preCallPrompt != null && extraInfo != null
					&& !extraInfo.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG))
				extraInfo.put(EXTRA_INFO_INTRO_PROMPT_FLAG, _preCallPrompt);

			if (cosID != null
					&& cosID.equalsIgnoreCase(RBTParametersUtils
							.getParamAsString(COMMON, "FREEMIUM_GROUP2_COSID",
									null))) {
				extraInfo.put("FR_GROUP2_USER", cosID);
			}
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			// Added for JIRA-RBT-6321
			if (isDirectActivation && refId != null)
				finalRefID = refId;

			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
			if (subscriber != null) {
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {
						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);
						}
					}
					return subscriber;
				}
				if (!isDirectActivation
						&& (subsciptionYes.equals("D")
								|| subsciptionYes.equals("P")
								|| subsciptionYes.equals("F")
								|| subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes
									.equals("z")))
					return null;

				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();

				boolean success = SubscriberImpl.update(conn,
						subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo,
						subscriptionClass, deactivatedBy, deactivationDate,
						null, subscription, 0, cosID, cosID, rbtType,
						subscriber.language(), subExtraInfo, circleID,
						finalRefID, isDirectActivation);
				if (startDate == null)
					startDate = new Date(System.currentTimeMillis());
				if (success) {
					subscriber = new SubscriberImpl(subID(subscriberID),
							activate, null, startDate, m_endDate, prepaid,
							null, null, 0, activationInfo, subscriptionClass,
							subscription, deactivatedBy, deactivationDate,
							null, 0, cosID, cosID, rbtType,
							subscriber.language(), subscriber.oldClassType(),
							subExtraInfo, circleID, finalRefID);
				} else
					subscriber = null;
			} else {

				// RBT-9873 Added xtraParametersMap for CG flow
				subscriber = checkModeAndInsertIntoConsent(subscriberID,
						activate, startDate, endDate, isDirectActivation,
						rbtType, conn, prepaid, subscription, activationInfo,
						cosID, subscriptionClass, finalRefID, extraInfo,
						circleID, isComboRequest, xtraParametersMap);
				subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);

				if (subscriber == null) {
					subscriber = SubscriberImpl.insert(conn,
							subID(subscriberID), activate, null, startDate,
							endDate, prepaid, null, null, 0, activationInfo,
							subscriptionClass, null, null, null, subscription,
							0, cosID, cosID, rbtType, null, isDirectActivation,
							subExtraInfo, circleID, finalRefID);
				}
			}

			if (isDirectActivation) {
				boolean isRealTime = false;
				Parameters param = CacheManagerUtil.getParametersCacheManager()
						.getParameter("DAEMON", "REAL_TIME_SELECTIONS");
				if (param != null && param.getValue().equalsIgnoreCase("true"))
					isRealTime = true;
				smUpdateSelStatusSubscriptionSuccess(subID(subscriberID),
						isRealTime);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	public boolean deleteConsentRecord(String transid, String clipID,
			String categoryID, String mode, String subscriberID,
			boolean isShuffle) {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			logger.info("No Connection to DB");
			return false;
		}
		try {
			success = ConsentTableImpl.deleteConsentTableRecord(conn, transid,
					clipID, categoryID, subscriberID, mode, isShuffle);
		} catch (Exception ex) {
			logger.error("Exception before release connection", ex);
		} finally {
			releaseConnection(conn);
		}

		return success;
	}

	public boolean updateConsentRecordForDownload(String subscriberId,
			String transID, String fromTime, String toTime, String selInterval,
			int status, String loopStatus, String callerId)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateConsentRecordForDownload(conn,
				subscriberId, transID, fromTime, toTime, selInterval, status,
				loopStatus, callerId);
		releaseConnection(conn);

		return success;
	}

	public boolean updateConsentStatusOfConsentRecordBySubscriberId(
			String subscriberId, String consentStatus) throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateConsentStatusBySubscriberId(conn,
				subscriberId, consentStatus);
		releaseConnection(conn);

		return success;
	}

	public boolean updateConsentStatusOfConsentRecord(String subscriberID,
			String transId, String consentStatus) throws OnMobileException {
		return updateConsentStatusOfConsentRecord(subscriberID, transId,
				consentStatus, null, null);
	}

	public boolean updateConsentStatusOfConsentRecord(String subscriberID,
			String transId, String consentStatus, String oldConsentStatus)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateConsentStatus(conn, null,
				subscriberID, transId, consentStatus, oldConsentStatus, null,
				null, null, null);
		releaseConnection(conn);

		return success;
	}

	public boolean updateConsentStatusOfConsentRecord(String subscriberID,
			String transId, String consentStatus, String oldConsentStatus,
			Integer flag) throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateConsentStatus(conn, null,
				subscriberID, transId, consentStatus, oldConsentStatus, null,
				null, null, flag);
		releaseConnection(conn);

		return success;
	}

	public boolean updateConsentStatusAndModeOfConsentRecord(
			String selectionInfo, String subscriberID, String transId,
			String consentStatus, String oldConsentStatus, String mode,
			String extraInfo, String circleId, Integer flag) throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateConsentStatus(conn, selectionInfo,
				subscriberID, transId, consentStatus, oldConsentStatus, mode,
				extraInfo, circleId, flag);
		releaseConnection(conn);

		return success;
	}

	public boolean updateModeOfConsentRecord(String trxid, String subscriberID,
			String transId, String mode, String circleId)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateMode(conn, trxid, subscriberID,
				transId, mode, circleId);
		releaseConnection(conn);

		return success;
	}

	public DoubleConfirmationRequestBean getFirstSelOrActRecord(
			String subscriberID, String type) {
		Connection conn = getConnection();
		DoubleConfirmationRequestBean dwnReqBean = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		dwnReqBean = ConsentTableImpl.getFirstSelectionRequest(conn,
				subscriberID, type);
		releaseConnection(conn);
		return dwnReqBean;

	}

	public List<DoubleConfirmationRequestBean> getSelectionConsentRequests(
			String subscriberID, String type, String status,
			boolean isUniqueMsisdnSupported) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> dwnReqBean = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		dwnReqBean = ConsentTableImpl.getSelectionRequest(conn, subscriberID,
				type, status, isUniqueMsisdnSupported);
		releaseConnection(conn);
		return dwnReqBean;

	}

	public boolean updateConsentExtrInfo(String subscriberID, String transId,
			String extraInfo, String circleId) throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateExtraInfo(conn, subscriberID, transId,
				extraInfo, circleId);
		releaseConnection(conn);
		return success;

	}

	// RBT-14675- Tata Docomo | Instead of Consent ID we are populating
	// Transaction Id in system
	public boolean updateConsentExtrInfo(String subscriberID, String transId,
			String extraInfo, String circleId, String consentStatus)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateExtraInfo(conn, subscriberID, transId,
				extraInfo, circleId, consentStatus);
		releaseConnection(conn);
		return success;
	}

	public boolean updateConsentExtrInfoAndStatus(String subscriberID,
			String transId, String extraInfo, String consentStatus)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateExtraInfoAndStatus(conn, subscriberID,
				transId, extraInfo, consentStatus);
		releaseConnection(conn);
		return success;

	}

	public SubscriptionRequest getSubscriptionRecordForTransID(String transID)
			throws OnMobileException {
		Connection conn = getConnection();
		SubscriptionRequest subscriptionRequest = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			subscriptionRequest = ConsentTableImpl
					.getSubscriptionRecordForTransID(conn, transID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return subscriptionRequest;
	}

	public SelectionRequest getMatchingSelectionRecordForConsent(String clipID,
			String categoryID, String subscriberID, String mode,
			String transid, boolean isRMOClip, String requestType)
			throws OnMobileException {
		Connection conn = getConnection();
		SelectionRequest selectionRequest = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			selectionRequest = ConsentTableImpl.getSelectionRecord(conn,
					clipID, categoryID, subscriberID, mode, transid, isRMOClip,
					requestType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return selectionRequest;
	}

	public SubscriptionRequest getMatchingSubscriptionRecordForConsent(
			String subscriberID, String timestamp, String mode, String transid,
			String requestType) throws OnMobileException {

		return getMatchingSubscriptionRecordForConsent(subscriberID, timestamp,
				mode, transid, null, requestType);
	}

	public SubscriptionRequest getMatchingSubscriptionRecordForConsent(
			String subscriberID, String timestamp, String mode, String transid,
			String consentStatus, String requestType) throws OnMobileException {
		Connection conn = getConnection();
		SubscriptionRequest subscriptionRequest = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			subscriptionRequest = ConsentTableImpl.getSubscriptionRecord(conn,
					subscriberID, timestamp, mode, transid, consentStatus,
					requestType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return subscriptionRequest;
	}

	public List<DoubleConfirmationRequestBean> getConsentRecordListForStatusNMsisdnMode(
			String consentStatus, String subscriberID, String mode,
			boolean descOrderEnabled) throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getRequestsByModeNMsisdnNStatus(conn, consentStatus,
							subscriberID, mode, descOrderEnabled);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;
	}

	public List<DoubleConfirmationRequestBean> getConsentRecordListForStatusNMsisdnNType(
			String consentStatus, String subscriberID, String type)
			throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getRequestsByModeNMsisdnNType(conn, consentStatus,
							subscriberID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;
	}

	public List<DoubleConfirmationRequestBean> getConsentRecordListBySongID(
			String subscriberID, String type, String rbtWaveFileName,
			String mode) throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getConsentRecordListBySongID(conn, subscriberID, type,
							rbtWaveFileName, mode);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;
	}

	public List<DoubleConfirmationRequestBean> getPendingRequestsByMsisdnNTypeNRequestTime(
			String subscriberID, String type, String requestFromTime,
			String requestToTime) throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getPendingRequestsByMsisdnNTypeNRequestTime(conn,
							subscriberID, type, requestFromTime, requestToTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;

	}

	public List<DoubleConfirmationRequestBean> getConsentPendingRecordListByMsisdnNType(
			String subscriberID, String type) throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getPendingRequestsByMsisdnNType(conn, subscriberID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;
	}

	public List<DoubleConfirmationRequestBean> getConsentPendingRecordListByAgentId(String agentId,String subscriberID) throws OnMobileException {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBeans = ConsentTableImpl
					.getPendingRequestsByAgentId(conn, subscriberID, agentId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBeans;
	}
	
	public DoubleConfirmationRequestBean getConsentRecordForMsisdnNTransId(String subscriberID, String transId)
			throws OnMobileException {
		Connection conn = getConnection();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBean = ConsentTableImpl
					.getRequestsByTransIdNMsisdn(conn, subscriberID, transId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBean;
	}

	public DoubleConfirmationRequestBean getConsentRecordForStatusNMsisdnNTransId(
			String consentStatus, String subscriberID, String transId)
			throws OnMobileException {
		Connection conn = getConnection();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			doubleConfirmationRequestBean = ConsentTableImpl
					.getRequestsByTransIdNMsisdnNStatus(conn, consentStatus,
							subscriberID, transId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return doubleConfirmationRequestBean;
	}

	public boolean deleteConsentRequestOfMsisdnNModeNClass(String msisdn,
			String mode, String subClass, String chargeClass,
			String consentStatus) {
		Connection conn = getConnection();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		if (conn == null)
			return false;
		try {
			return ConsentTableImpl.deleteRequestOfMsisdnNModeNClass(conn,
					msisdn, mode, subClass, chargeClass, consentStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteConsentRequestByTransIdAndMSISDN(String transId,
			String subscriberID) {
		Connection conn = getConnection();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		if (conn == null)
			return false;
		try {
			return ConsentTableImpl.deleteRequestByTransIdAndMSISDN(conn,
					transId, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	public boolean deleteConsentRecordByAgentID(String subscriberId, String agentId){
		Connection con = getConnection();
		if(con == null){
			return false;
		}		
		try{
			return ConsentTableImpl.deleteLatestPendingConsentRecordsByAgentId(con, subscriberId, agentId);
		}catch(Throwable e){
			logger.error("Exception before release connection", e);
		}finally{
			releaseConnection(con);
		}
		return false;
	}
	
	public boolean createNewNameTuneRequest(String subscriberId, String extraInfo){
		Map extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		String nt_name = null;
		String nt_language = null;
		String nt_clipId = null;
		if(extraInfoMap!=null){
			nt_name = (String)extraInfoMap.get("NT_NAME");
			nt_language = (String)extraInfoMap.get("NT_LANG");
			nt_clipId = (String)extraInfoMap.get("NT_CLIPID");
		}			
		RbtNameTuneTracking nameTuneObject = new RbtNameTuneTracking();		
		nameTuneObject.setClipId(null);
		nameTuneObject.setLanguage(nt_language);
		nameTuneObject.setCreatedDate(null);
		nameTuneObject.setMsisdn(subscriberId);
		nameTuneObject.setNameTune(nt_name);
		nameTuneObject.setRetryCount(0);
		nameTuneObject.setStatus(Status.NEW_REQUEST.toString());
		nameTuneObject.setTransactionId(UUID.randomUUID().toString());
		RbtNameTuneTrackingDao genericDao=RbtNameTuneTrackingDaoImpl.getInstance();
		try {
			genericDao.saveOrUpdateEntity(nameTuneObject);
		} catch (com.onmobile.apps.ringbacktones.v2.dao.DataAccessException e) {		
		} catch(Throwable e){
		}
		
		return true;
	}

	public List<DoubleConfirmationRequestBean> getConsentRequestForCallBack(
			String transId, String subscriberId) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl.getConsentRequestForCallBack(
					conn, transId, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}

	public List<DoubleConfirmationRequestBean> getConsentPendingRequests(
			String transId, String subscriberId) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl.getConsentPendingRequests(
					conn, transId, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}
	
	public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanForStatus(
			String recordStatus, String transId, String subscriberID,
			String type, boolean isUniqueMsisdnSupported,
			boolean isRequestTimeCheckRequired) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl
					.getDoubleConfirmationRequestBean(conn, recordStatus,
							transId, subscriberID, type,
							isUniqueMsisdnSupported, isRequestTimeCheckRequired, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}
	
public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanForStatusWithInlineFlag(
			String recordStatus, String transId, String subscriberID,
			String type, boolean isUniqueMsisdnSupported,
			boolean isRequestTimeCheckRequired, Integer flag) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl
					.getDoubleConfirmationRequestBean(conn, recordStatus,
							transId, subscriberID, type,
							isUniqueMsisdnSupported, isRequestTimeCheckRequired, flag);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}
	public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanByType( String subscriberID,
			String type, boolean isUniqueMsisdnSupported,
			boolean isRequestTimeCheckRequired) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl
					.getDoubleConfirmationRequestBeanByRequestType(conn, subscriberID, type,
							isUniqueMsisdnSupported, isRequestTimeCheckRequired);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}

	public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanForStatus(
			String recordStatus, String transId, String subscriberID,
			String type, boolean isUniqueMsisdnSupported) {
		return getDoubleConfirmationRequestBeanForStatus(recordStatus, transId,
				subscriberID, type, isUniqueMsisdnSupported, false);
	}
	
	
	public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanByType(String subscriberID,
			String type, boolean isUniqueMsisdnSupported) {
		return getDoubleConfirmationRequestBeanByType(subscriberID, type, isUniqueMsisdnSupported, false);
	}

	public List<DoubleConfirmationRequestBean> getRecordsBeforeConfRequestTime(
			String hours) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl.getRecordsBeforeConfRequestTime(
					conn, hours);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;

	}

	public boolean updateConsentStatus(String subscriberId, String transId,
			String consentStatus) {
		logger.info("Updating consent status. subscriberId: " + subscriberId
				+ ", transId: " + transId + " consentStatus: " + consentStatus);
		Connection conn = getConnection();
		boolean isUpdated = false;
		if (conn == null) {
			logger.error("Ruturning false, no connection to DB");
			return false;
		}
		try {
			isUpdated = ConsentTableImpl.updateConsentStatus(conn,
					subscriberId, transId, consentStatus);
		} catch (Throwable e) {
			logger.error(
					"Unalbe to get expired records." + " Throwable: "
							+ e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Updated consent status. isUpdated: " + isUpdated);
		return isUpdated;
	}

	public boolean insertRSSFeedSchedulerRecord(String feedDay,
			String feedWeekId, String feedCircleGroup, String feedGroupId,
			String feedModule, String moduleId, String feedCategory,
			String feedCategoryId, String feedCPName, String feedCPId,
			String feedPosition, String feedTimeSlot, String feedTimeSlotId,
			String feedOMCategoryId, String feedOMContentName, String feedType,
			String feedPubDate, String feedReleaseDate) {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			logger.error("Ruturning null, no connection to DB");
			return success;
		}
		try {
			success = RSSFeedScheduleImpl.insert(conn, feedDay, feedWeekId,
					feedCircleGroup, feedGroupId, feedModule, moduleId,
					feedCategory, feedCategoryId, feedCPName, feedCPId,
					feedPosition, feedTimeSlot, feedTimeSlotId,
					feedOMCategoryId, feedOMContentName, feedType, feedPubDate,
					feedReleaseDate);
		} catch (Throwable e) {
			logger.error(
					"Unalbe to get expired records." + " Throwable: "
							+ e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public List<RSSFeedScheduler> getRSSFeedRecord(String feedType) {
		Connection conn = getConnection();
		List<RSSFeedScheduler> rssFeedSchedulerList = null;
		if (conn == null) {
			logger.error("Ruturning null, no connection to DB");
			return rssFeedSchedulerList;
		}
		try {
			rssFeedSchedulerList = RSSFeedScheduleImpl.getRSSFeedRecords(conn,
					feedType);
		} catch (Throwable e) {
			logger.error(
					"Unalbe to get expired records." + " Throwable: "
							+ e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return rssFeedSchedulerList;
	}

	public List<DoubleConfirmationRequestBean> getExpiredConsentRecords(
			String hours, String consentStatus, int startingFrom, int limit,
			String mode) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.error("Ruturning null, no connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl.getExpiredConsentRecords(conn,
					hours, consentStatus, startingFrom, limit, mode);
		} catch (Throwable e) {
			logger.error(
					"Unalbe to get expired records." + " Throwable: "
							+ e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}

	public void insertTNBSubscriber(String subID, String circleID,
			String classType, Date time, int i) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");

		try {
			TnbSubscriberImpl.insert(conn, subID, circleID, classType, Calendar
					.getInstance().getTime(), i);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public TnbSubscriberImpl getTNBSubscriber(String subID, String circleID)
			throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");

		try {
			return TnbSubscriberImpl.getTNBSubscriber(conn, subID, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean isTnbReminderEnabled(Subscriber subscriber) {
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

	public boolean isTnbReminderEnabled(String subClass) {
		if (subClass == null)
			return false;
		if (ReminderTool.reminderDaemonModes == null
				|| ReminderTool.reminderDaemonModes.size() == 0)
			return false;
		if (ReminderTool.reminderDaemonModes.contains("SUB")
				&& ReminderTool.tnbOptinMap.containsKey(subClass))
			return true;
		if (ReminderTool.reminderDaemonModes.contains("SUB_CLASS")
				&& ReminderTool.tnbOptoutMap.containsKey(subClass))
			return true;
		return false;
	}

	public Subscriber trialActivateSubscriber(String subscriberID,
			String activate, Date date, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, String selClass,
			String subscriptionType, String circleId) {
		int rbtType = 0;

		Connection conn = getConnection();
		if (conn == null)
			return null;

		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";

		Date endDate = null;
		Subscriber subscriber = null;
		try {

			if (activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO")) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, -1);
				endDate = endCal.getTime();

			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());

			} else if (selClass != null && selClass.startsWith("TRIAL")) {
				ChargeClass chargeClass = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(selClass);
				if (chargeClass != null
						&& chargeClass.getSelectionPeriod() != null
						&& chargeClass.getSelectionPeriod().startsWith("D")) {
					String selectionPeriod = chargeClass.getSelectionPeriod()
							.substring(1);
					if ("OPTIN".equalsIgnoreCase(subscriptionType)) {
						Calendar endCal = Calendar.getInstance();
						endCal.add(Calendar.DATE,
								Integer.parseInt(selectionPeriod) - 1);
						endDate = endCal.getTime();
						if (actInfo == null)
							actInfo = ":optin:";
						else
							actInfo = actInfo + ":optin:";
					} else if ("OPTOUT".equalsIgnoreCase(subscriptionType))
						endDate = m_endDate;
				}
			} else {
				if (endDate == null)
					endDate = m_endDate;
			}

			Date nextChargingDate = null;
			Date lastAccessDate = null;
			Date activationDate = null;
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;
			if (circleId == null)
				circleId = RbtServicesMgr.getSubscriberDetail(
						new MNPContext(subID(subscriberID), activate))
						.getCircleID();

			String cosID = null;
			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = null;
				List<String> viralSMSList = Arrays.asList("BASIC,CRICKET"
						.split(","));
				if (isViralSmsTypeListForNewTable(viralSMSList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}

			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
			if (subscriber != null) {
				String subsciptionYes = subscriber.subYes();
				if (subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {
						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);
						}
					}
					return subscriber;
				}

				if (subsciptionYes.equals("D") || subsciptionYes.equals("P")
						|| subsciptionYes.equals("F")
						|| subsciptionYes.equals("x")
						|| subsciptionYes.equals("Z")
						|| subsciptionYes.equals("z"))
					return null;
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				String refID = UUID.randomUUID().toString();

				SubscriberImpl.update(conn, subID(subscriberID), activate,
						null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						deactivatedBy, deactivationDate, activationDate,
						subscription, 0, cosID, cosID, 0,
						subscriber.language(), null, circleId, refID, false);
				Date startDate = date;
				if (date == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, m_endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate,
						activationDate, 0, cosID, cosID, rbtType,
						subscriber.language(), subscriber.oldClassType(), null,
						circleId, refID);
			} else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						null, null, activationDate, subscription, 0, cosID,
						cosID, rbtType, null, false, null, circleId, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	public boolean clearAccessTable(int backupDays) {
		if (backupDays == 0)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return AccessImpl.clearAccessTable(conn, backupDays);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String[] getOldSubscribers(float duration, boolean useSM) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getOldSubscribers(conn, duration, useSM);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean cleanOldSubscriber(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.remove(conn, subID(subID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Subscriber updateSubscriber(String subID, String activate,
			String deactivate, Date startDate, Date endDate, String prepaidYes,
			Date accessDate, Date nextChargingDate, int maxSelections,
			int access, String activationInfo, String subscriptionClass,
			Date activationDate, String subYes, String lastDeactivationInfo,
			Date lastDeactivationDate, String cosID, String activatedCosID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Subscriber sub = null;
		try {
			sub = getSubscriber(subID);
			String language = null;
			String strOldClassType = null;

			if (sub != null) {
				language = sub.language();
				strOldClassType = sub.oldClassType();
			}
			int rbtType = 0;
			if (SubscriberImpl.update(conn, subID, activate, deactivate,
					startDate, endDate, prepaidYes, accessDate,
					nextChargingDate, access, activationInfo,
					subscriptionClass, lastDeactivationInfo,
					lastDeactivationDate, activationDate, subYes,
					maxSelections, cosID, activatedCosID, rbtType, language,
					null, null, null, false))
				sub = new SubscriberImpl(subID, activate, deactivate,
						startDate, endDate, prepaidYes, accessDate,
						nextChargingDate, access, activationInfo,
						subscriptionClass, subYes, lastDeactivationInfo,
						lastDeactivationDate, activationDate, maxSelections,
						cosID, activatedCosID, rbtType, language,
						strOldClassType, null, null, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return sub;
	}

	public boolean updateSubscriber(String subscriberID, String activatedBy,
			String deactivatedBy, Date startDate, Date endDate,
			String prepaidYes, Date lastAccessDate, Date nextChargingDate,
			Integer noOfAccess, String activationInfo,
			String subscriptionClass, String subscriptionYes,
			String lastDeactivationInfo, Date lastDeactivationDate,
			Date activationDate, Integer maxSelections, String cosID,
			String activatedCosID, String oldClassType, Integer rbtType,
			String language, String playerStatus,
			HashMap<String, String> extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			Subscriber subscriber = getSubscriber(subscriberID);
			if (subscriber == null)
				return false;

			String extraInfoXML = null;
			if (extraInfo != null && extraInfo.size() > 0) {
				HashMap<String, String> subscriberExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (subscriberExtraInfoMap != null)
					subscriberExtraInfoMap.putAll(extraInfo);
				else
					subscriberExtraInfoMap = extraInfo;

				extraInfoXML = DBUtility
						.getAttributeXMLFromMap(subscriberExtraInfoMap);
			}
			return SubscriberImpl.update(conn, subscriberID, activatedBy,
					deactivatedBy, startDate, endDate, prepaidYes,
					lastAccessDate, nextChargingDate, noOfAccess,
					activationInfo, subscriptionClass, subscriptionYes,
					lastDeactivationInfo, lastDeactivationDate, activationDate,
					maxSelections, cosID, activatedCosID, oldClassType,
					rbtType, language, playerStatus, extraInfoXML, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean checkAndInsertClipWithStatus(String rbtWavFile,
			String circleID, int status) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipStatusImpl.checkAndInsertClipWithStatus(conn,
					rbtWavFile, circleID, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public boolean updateStatusAndCircleIds(String status,
			String pendingCircles, String transferredCircles, String clipWavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			ClipStatusImpl.updateStatusAndCircleIds(conn, status,
					pendingCircles, transferredCircles, clipWavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean updateClipStatusByWavFile(String status, String clipWavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean success = false;
		try {
			success = ClipStatusImpl.updateClipStatusByWavFile(conn, status,
					clipWavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;

	}

	public boolean updateSubscriberCosId(String subscriberId,
			String renewalCosId, Date endDate) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateSubscriberCosId(conn, subscriberId,
					renewalCosId, endDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriber(String subscriberID, String deactivate,
			Date date, boolean delSelections, boolean sendToHLR,
			boolean smDeactivation) {
		String ret = (deactivateSubscriber(subscriberID, deactivate, date,
				delSelections, sendToHLR, smDeactivation, false, true));
		return (ret != null && ret.equalsIgnoreCase("SUCCESS")) ? true : false;
	}

	public String deactivateSubscriber(String subscriberID, String deactivate,
			Date date, boolean delSelections, boolean sendToHLR,
			boolean smDeactivation, boolean checkSubClass) {
		return (deactivateSubscriber(subscriberID, deactivate, date,
				delSelections, sendToHLR, smDeactivation, false, checkSubClass));
	}

	public String deactivateSubscriber(String subscriberID, String deactivate,
			Date date, boolean delSelections, boolean sendToHLR,
			boolean smDeactivation, boolean isDirectReact, boolean checkSubClass) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		return deactivateSubscriber(conn, subscriberID, deactivate, date,
				delSelections, sendToHLR, smDeactivation, isDirectReact,
				checkSubClass);
	}

	public String deactivateSubscriber(Connection conn, String subscriberID,
			String deactivate, Date date, boolean delSelections,
			boolean sendToHLR, boolean smDeactivation, boolean isDirectDeact,
			boolean checkSubClass) {
		Subscriber sub = getSubscriber(subscriberID);
		return deactivateSubscriber(conn, subscriberID, deactivate, date,
				delSelections, sendToHLR, smDeactivation, isDirectDeact,
				checkSubClass, sub.rbtType(), sub, null, null);
	}

	public String deactivateSubscriber(String subscriberID, String deactivate,
			Date date, boolean delSelections, boolean sendToHLR,
			boolean smDeactivation, boolean isDirectDeact,
			boolean checkSubClass, int rbtType, Subscriber sub, String dctInfo,
			String userInfoXML) {
		Connection conn = getConnection();
		if (conn == null)
			return m_failure;

		return deactivateSubscriber(conn, subID(subscriberID), deactivate,
				date, delSelections, sendToHLR, smDeactivation, isDirectDeact,
				checkSubClass, rbtType, sub, dctInfo, userInfoXML);
	}

	public String deactivateSubscriber(Connection conn, String subscriberID,
			String deactivate, Date date, boolean delSelections,
			boolean sendToHLR, boolean smDeactivation, boolean isDirectDeact,
			boolean checkSubClass, int rbtType, Subscriber sub, String dctInfo,
			String userInfoXml) {
		logger.debug("Deactivating the subscriber: " + subscriberID
				+ ", deactivate: " + deactivate + ", date: " + date
				+ ", delSelections: " + delSelections + ", userInfoXml: "
				+ userInfoXml);
		String ret = null;
		if (conn == null)
			return null;
		try {
			subscriberID = subID(subscriberID);
			boolean deact = true;
			boolean success = false;
			if (!isDirectDeact && checkSubClass) {
				if (sub != null) {
					SubscriptionClass temp = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(sub.subscriptionClass());
					if (temp != null && temp.isDeactivationNotAllowed())
						ret = "DCT_NOT_ALLOWED";
				}
			}

			boolean deactSub = true;
			if (ret == null) {
				logger.debug("Subscriber deactivation is allowed. subscriber: "
						+ subscriberID + ", smDeactivation: " + smDeactivation);
				if (smDeactivation) {
					deact = false;
					if (sub != null) {
						if (isDirectDeact
								|| (sub.subYes() != null && (sub.subYes()
										.equalsIgnoreCase("B")
										|| sub.subYes().equalsIgnoreCase("O")
										|| sub.subYes().equalsIgnoreCase("z")
										|| sub.subYes().equalsIgnoreCase("Z") || sub
										.subYes().equalsIgnoreCase("G")))) {
							if (sub.rbtType() == TYPE_RBT_RRBT
									|| sub.rbtType() == TYPE_SRBT_RRBT) {
								convertSubscriptionType(subscriberID,
										sub.subscriptionClass(), "DEFAULT",
										null, rbtType, true, null, sub);
								deactSub = false;
							}
							deact = true;
						}
						boolean isAllowDeactForPendingUser = CacheManagerUtil
								.getParametersCacheManager()
								.getParameter("COMMON",
										"IS_DEACT_ALLOWED_FOR_ACT_PENDING",
										"FALSE").getValue()
								.equalsIgnoreCase("TRUE");
						if (!deact
								&& isAllowDeactForPendingUser
								&& (sub.subYes() != null && (sub.subYes()
										.equalsIgnoreCase("A")
										|| sub.subYes().equalsIgnoreCase("N") || sub
										.subYes().equalsIgnoreCase("E")))) {
							deact = true;
						}
					}
					if (deact) {
						boolean isNewSubscriber = false;
						logger.debug("subscriber: " + sub);
						if (sub != null) {
							logger.debug("subscriber: " + sub
									+ ", extra info: " + sub.extraInfo());
							// userInfoXml = sub.extraInfo();
							HashMap<String, String> userInfoMap = DBUtility
									.getAttributeMapFromXML(userInfoXml);
							logger.debug("subscriber: " + sub
									+ ", extra info map: " + userInfoMap);
							if (userInfoMap != null
									&& userInfoMap.containsKey("UNSUB_DELAY")) {
								deactivate = userInfoMap.get("UNSUB_DELAY");
								userInfoMap.remove("UNSUB_DELAY");
								userInfoXml = DBUtility
										.getAttributeXMLFromMap(userInfoMap);
								if (userInfoXml == null)
									userInfoXml = "";
								logger.debug("Deactivate mode from UNSUB_DELAY: "
										+ deactivate);
							}

						}
						if (deactSub) {
							logger.debug("Deactivating subscriber: "
									+ subscriberID + " on mode: " + deactivate);
							success = SubscriberImpl.deactivate(conn,
									subscriberID, deactivate, date, sendToHLR,
									smDeactivation, isNewSubscriber,
									isDirectDeact, m_isMemCachePlayer, dctInfo,
									sub, userInfoXml);
						}
						if (success) {
							if (!smDeactivation)
								SubscriberChargingImpl.remove(conn,
										subscriberID);
							if (delSelections || isDirectDeact) {
								SubscriberStatusImpl.deactivate(conn,
										subscriberID, date, smDeactivation,
										isNewSubscriber, deactivate, rbtType);

								Groups[] groups = GroupsImpl
										.getGroupsForSubscriberID(conn,
												subscriberID);
								if (groups != null) {
									int[] groupIDs = new int[groups.length];
									for (int i = 0; i < groups.length; i++)
										groupIDs[i] = groups[i].groupID();
									GroupMembersImpl
											.deleteGroupMembersOfGroups(conn,
													groupIDs);
									GroupsImpl.deleteGroupsOfSubscriber(conn,
											subscriberID);
								}
							}
						}
					} else
						ret = "ACT_PENDING";
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		String returnStr = ret == null ? "SUCCESS" : ret;
		logger.debug("Status: " + returnStr
				+ " of deactivating the subscriber: " + subscriberID);
		return returnStr;
	}

	public String getRequestTypeForConsent(String transId) throws RBTException {
		Connection conn = getConnection();
		if (conn == null)
			throw new RBTException("DB Connection not found");

		String requestType = ConsentTableImpl.getRequestType(conn, transId);
		return requestType;
	}

	public Scratchcard getScratchcard(String scratchno) throws RBTException {
		Connection conn = getConnection();
		if (conn == null)
			throw new RBTException("DB Connection not found");

		try {
			return Scratchcard.getDetailsForScratchCard(conn, scratchno);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateScratchCard(String scratchNo, String state) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return Scratchcard.updateScratchCard(conn, scratchNo, state);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int removeOldScratchCard(String state, int duration) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			return Scratchcard.removeOldScratchCard(conn, duration, state);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public UserRights[] getUserRights() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return UserRightsImpl.getUserRights(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RBTLogin[] getLogins() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RBTLoginImpl.getAllUsers(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RBTLogin[] checkGUIPwd(String userName, String Password) {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		RBTLogin[] logins = RBTLoginImpl.checkGUIPwd(conn, userName, Password);
		releaseConnection(conn);
		return logins;
	}

	public Subscriber[] getFreeActivatedSubscribers(int days) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getFreeActivatedSubscribers(conn, days);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean setNextChargingDateAndActDate(String subscriberID,
			Date date, Date startTime) {
		return setNextChargingDateAndActDate(subID(subscriberID), date,
				startTime, 0);
	}

	public boolean setNextChargingDateAndActDate(String subscriberID,
			Date date, Date startTime, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			SubscriberImpl.setNextChargingDateAndActDate(conn,
					subID(subscriberID), date);
			SubscriberStatusImpl.setSetTime(conn, subID(subscriberID),
					startTime, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean setPrepaidYes(String subscriberID, boolean isPrepaid) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.setPrepaidYes(conn, subID(subscriberID),
					isPrepaid);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean setSubscriptionYes(String subscriberID,
			String subscriptionYes) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberImpl.setSubscriptionYes(conn, subID(subscriberID),
					subscriptionYes);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean setAccessCount(String subscriberID, int accessCount) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberImpl.setAccessCount(conn, subID(subscriberID),
					accessCount);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public void setSubscriberLanguage(String subscriberID, String language) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberImpl.setSubscriberLanguage(conn, subID(subscriberID),
					language);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void setSubscriberGender(String subscriberID, String gender) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			SubscriberImpl.setSubscriberGender(conn, subID(subscriberID),
					gender);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void setSubscriberAge(String subscriberID, int ageCategory) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			SubscriberImpl.setSubscriberAge(conn, subID(subscriberID),
					ageCategory);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void setSubscriberCOS(String subscriberID, String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			SubscriberImpl.setSubscriberCOS(conn, subID(subscriberID), cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public boolean updateEndDate(String subscriberID, Date endDate,
			String subClasses) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate,
					subClasses);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public String updateEndDateAndExtraInfo(String subscriberID, Date endDate,
			String extraInfo) {
		boolean response = false;
		Connection conn = getConnection();
		if (conn == null)
			return m_failure;
		try {
			response = SubscriberImpl.updateEndDateAndExtraInfo(conn,
					subID(subscriberID), endDate, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return response ? m_success : m_failure;
	}

	// RBT-13415 - Nicaragua Churn Management.
	public String updateEndDateAndExtraInfoOnlyBySubId(String subscriberID,
			Date endDate, String extraInfo) {
		boolean response = false;
		Connection conn = getConnection();
		if (conn == null)
			return m_failure;
		try {
			response = SubscriberImpl.updateEndDateAndExtraInfoOnlyBySubId(
					conn, subID(subscriberID), endDate, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return response ? m_success : m_failure;
	}

	public boolean setAccessDate(Connection conn, String subscriberID, Date date) {
		if (conn == null)
			return false;

		if (subscriberID != null)
			return SubscriberImpl
					.setAccessDate(conn, subID(subscriberID), date);
		return false;
	}

	public boolean setAccessDate(String subscriberID, Date date) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return setAccessDate(conn, subscriberID, date);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean isSubscriberActivated(String subscriberID,
			boolean smDeactivation) {
		return isSubscriberActivated(subID(subscriberID), smDeactivation, 0);
	}

	public boolean isSubscriberActivated(String subscriberID,
			boolean smDeactivation, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		String ret = null;
		try {
			ret = SubscriberImpl.isSubscriberActivated(conn,
					subID(subscriberID));
			if (ret == null)
				SubscriberStatusImpl.deactivate(conn, subID(subscriberID),
						null, smDeactivation, false, "DAEMON", rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return ret == null ? false : ret.equalsIgnoreCase("true");
	}

	public boolean isSubscriberDeActivated(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.isSubscriberDeActivated(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String smSubscriptionSuccess(String subscriberID,
			Date nextChargingDate, Date activationDate, String type,
			String classType, boolean isPeriodic, String finalActInfo,
			boolean updatePlayStatus, String extraInfo) {
		return smSubscriptionSuccess(subscriberID, nextChargingDate,
				activationDate, type, classType, isPeriodic, finalActInfo,
				false, updatePlayStatus, extraInfo, null, -1, null, null);
	}

	public String smSubscriptionSuccess(String subscriberID,
			Date nextChargingDate, Date activationDate, String type,
			String classType, boolean isPeriodic, String finalActInfo,
			boolean updateEndtime, boolean updatePlayStatus, String extraInfo,
			String upgradingCosID, int validity, String subscriptionYes,
			String strNextBillingDate) {
		return smSubscriptionSuccess(subscriberID, nextChargingDate,
				activationDate, type, classType, isPeriodic, finalActInfo,
				updateEndtime, updatePlayStatus, extraInfo, upgradingCosID,
				validity, subscriptionYes, null, -1, strNextBillingDate);
	}

	public String smSubscriptionSuccess(String subscriberID,
			Date nextChargingDate, Date activationDate, String type,
			String classType, boolean isPeriodic, String finalActInfo,
			boolean updateEndtime, boolean updatePlayStatus, String extraInfo,
			String upgradingCosID, int validity, String subscriptionYes,
			String oldSub, int rbtType, String strNextBillingDate) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smSubscriptionSuccess(conn,
					subID(subscriberID), nextChargingDate, activationDate,
					type, classType, isPeriodic, null, finalActInfo,
					updateEndtime, updatePlayStatus, extraInfo, upgradingCosID,
					validity, subscriptionYes, oldSub, rbtType,
					strNextBillingDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDownloadUpgradationCallback(String subscriberID,
			String wavFile, String refID, String status, String contentType,
			String classType, HashMap<String, String> extraInfoMap,String strNextBillingDate) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		boolean isUpgradableLiteContent = false;
		boolean isError = true;
		String downloadStatus = m_failure;
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		try {
			if (!status.equals("SUCCESS")) {
				// UPGRADE FAILURE CASES
				HashMap<String, String> contentTypeChargeClassMap = new HashMap<String, String>();
				Parameters param = CacheManagerUtil.getParametersCacheManager()
						.getParameter("DAEMON",
								"LITE_UPGRADE_CHARGE_CLASS_MAPPING");
				if (param != null) {
					String supportedContentTypes = param.getValue();
					if (supportedContentTypes != null) {
						List<String> contentChargeClassList = Arrays
								.asList(supportedContentTypes.split(","));
						for (int j = 0; j < contentChargeClassList.size(); j++) {
							String tempContentTypeChargeclass[] = contentChargeClassList
									.get(j).split(":");
							contentTypeChargeClassMap.put(
									tempContentTypeChargeclass[0],
									tempContentTypeChargeclass[1]);
						}
						logger.info("The supported content type charge class map is "
								+ contentTypeChargeClassMap);
					}
					if (contentTypeChargeClassMap.containsKey(contentType))
						isUpgradableLiteContent = true;
					String extraInfo = null;
					if (extraInfoMap != null && !isUpgradableLiteContent) {
						classType = extraInfoMap.remove("OLD_CLASS_TYPE");
						extraInfo = DBUtility
								.getAttributeXMLFromMap(extraInfoMap);
						if (extraInfo == null)
							extraInfo = "NULL";
						isError = false;
					}
					success = smURLDownloadActivation(subscriberID, false,
							isError, refID, classType, extraInfo);
				}
			} else {
				if (extraInfoMap != null
						&& extraInfoMap.containsKey("BI_OFFER")) {
					extraInfoMap.remove("BI_OFFER");
				}

				String extraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);
				success = SubscriberDownloadsImpl.smDownloadActivationSuccess(
						conn, subscriberID, refID, downStat, classType,
						extraInfo,strNextBillingDate); // ACTIVATION SUCCESS CASES

			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : downloadStatus;
	}

	public String smSelectionUpgradationCallback(String subscriberID,
			String refID, String classType, String oldClassType, String status,
			String circleId, String loopStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			if (!status.equalsIgnoreCase("SUCCESS"))
				classType = oldClassType;
			success = SubscriberStatusImpl.smSelectionUpgrade(conn,
					subscriberID, null, classType, refID, circleId, loopStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDownloadActivationCallback(String subscriberID,
			String wavFile, String refID, String status,
			boolean m_noDownloadDeactSub, String type, String classType,
			boolean isPrepaid, HashMap<String, String> xtraInfoMap,String strNextBillingDate) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		boolean selStatus = false;
		String downloadStatus = m_failure;
		String selUpdateStatus = m_failure;
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		try {
			if (!status.equals("SUCCESS")) {
				// ACTIVATION FAILURE CASES
				if (addDownloadToBookmarkOnLowBalance)
					success = SubscriberDownloadsImpl
							.updateActPendingDownloadToBookMark(conn,
									subscriberID, wavFile);
				else {
					downStat = STATE_DOWNLOAD_DEACTIVATED;
					success = SubscriberDownloadsImpl
							.smDownloadActivationFailure(conn, subscriberID,
									refID, downStat);
				}
				if (success) {
					// Decrementing the num_max_selection count
					SubscriberImpl.decrementSelectionCount(conn, subscriberID);
					SitePrefix userPrefix = Utility.getPrefix(subscriberID);
					char loopStatus = LOOP_STATUS_EXPIRED_INIT;
					if (userPrefix != null
							&& !userPrefix.playUncharged(isPrepaid))
						loopStatus = LOOP_STATUS_EXPIRED;

					SubscriberStatusImpl.deactivateSettingDownloadFailure(conn,
							subscriberID, wavFile, loopStatus);

					// RBT-14044 VF ES - MI Playlist functionality for RBT core
					deleteDownloadwithTstatus(subscriberID, wavFile);

					if (RBTParametersUtils.getParamAsBoolean("DAEMON",
							"SUPPORT_IBM_INTEGRATION", "FALSE")) {
						// IBM-Integration
						RBTCallBackEvent.update(
								RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								subscriberID, refID,
								RBTCallBackEvent.SM_FAILURE_CALLBACK_RECEIVED,
								classType);
					}
					noDownloadDeactSub(conn, m_noDownloadDeactSub,
							subscriberID, type, false);
				}
			} else {

				if (xtraInfoMap != null) {
					xtraInfoMap.remove(EXTRA_INFO_OFFER_ID);
				}

				String extraInfo = DBUtility
						.getAttributeXMLFromMap(xtraInfoMap);
				try {

					// ACTIVATION SUCCESS CASES
					selStatus = SubscriberStatusImpl
							.activateSettingDownloadSuccess(conn, subscriberID,
									wavFile);
					if (xtraInfoMap != null
							&& xtraInfoMap.containsKey("BI_SONG")) {
						xtraInfoMap.remove("BI_SONG");
						extraInfo = DBUtility
								.getAttributeXMLFromMap(xtraInfoMap);
					}

				} catch (Exception e) {
					logger.error("Exception in act success case", e);
					throw new Exception(e);
				}
				success = SubscriberDownloadsImpl.smDownloadActivationSuccess(
						conn, subscriberID, refID, downStat, classType,
						extraInfo,strNextBillingDate);
				if (success) {
					if (RBTParametersUtils.getParamAsBoolean("DAEMON",
							"SUPPORT_IBM_INTEGRATION", "FALSE")) {
						// IBM-Integration
						RBTCallBackEvent.update(
								RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								subscriberID, refID,
								RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED,
								classType);
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		if (success) {
			downloadStatus = m_success;
			if (selStatus)
				selUpdateStatus = "sel" + m_success;
			return downloadStatus + "_" + selUpdateStatus;
		} else
			return downloadStatus + "_" + selUpdateStatus;
	}

	public String updateDownloadClassType(String subscriberID, String refID,
			String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		try {
			success = SubscriberDownloadsImpl.updateDownloadClassType(conn,
					subscriberID, refID, classType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean noDownloadDeactSub(Connection conn,
			boolean noDownloadDeactSub, String subscriberID, String type,
			boolean isPackCallback) {
		if (noDownloadDeactSub == false)
			return false;

		boolean deactivatedNow = false;
		// If active downloads not exits for subscriber,
		// Deactivating the base subscription
		SubscriberDownloads[] subDownloads = SubscriberDownloadsImpl
				.getSubscriberAllActiveDownloads(conn, subscriberID);
		deactivatedNow = noDownloadDeactSub(conn, noDownloadDeactSub,
				subscriberID, type, subDownloads, isPackCallback);
		return deactivatedNow;
	}

	public boolean noDownloadDeactSub(boolean noDownloadDeactSub,
			String subscriberID, String type, boolean isPackCallback) {
		if (noDownloadDeactSub == false)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deactivatedNow = false;
		// If active downloads not exits for subscriber,
		// Deactivating the base subscription
		try {
			SubscriberDownloads[] subDownloads = SubscriberDownloadsImpl
					.getSubscriberAllActiveDownloads(conn, subscriberID);
			deactivatedNow = noDownloadDeactSub(conn, noDownloadDeactSub,
					subscriberID, type, subDownloads, isPackCallback);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deactivatedNow;
	}

	public boolean noDownloadDeactSub(Connection conn,
			boolean noDownloadDeactSub, String subscriberID, String type,
			SubscriberDownloads[] subDownloads, boolean isPackCallback) {
		if (noDownloadDeactSub == false)
			return false;

		boolean deactivatedNow = false;
		// If active downloads not exits for subscriber,
		// Deactivating the base subscription
		if (subDownloads == null) {
			Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
					subscriberID);
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(iRBTConstant.PROMOTION,
							"MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION", null);
			if (tempParam != null && subscriber != null
					&& subscriber.rbtType() == 1) {
				logger.info("Subscriber is an ADRBT subscriber, adrbt mode config is present in DB. Hence not deactivating the subscriber.");
				return true;
			}
			if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
					"DELAY_DEACT_ON_DEACTIVATION", "FALSE")) {
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (subExtraInfoMap == null)
					subExtraInfoMap = new HashMap<String, String>();
				if (!subExtraInfoMap.containsKey("DELAY_DEACT")
						|| (subExtraInfoMap.get("DELAY_DEACT") != null && !subExtraInfoMap
								.get("DELAY_DEACT").equalsIgnoreCase("TRUE"))) {
					subExtraInfoMap.put("DELAY_DEACT", "TRUE");
					subExtraInfoMap.put("SUB_YES", subscriber.subYes());
					String extraInfo = DBUtility
							.getAttributeXMLFromMap(subExtraInfoMap);
					updateExtraInfoNStatusNDeactBy(subscriberID, extraInfo,
							"C", "DAEMON");
				}
			} else {
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subscriber.extraInfo());
				if (confAzaanCosIdList.size() == 0 || subExtraInfoMap == null
						|| subExtraInfoMap.size() == 0) {
					smDeactivateSubscriber(subscriberID, "DAEMON", null, true,
							true, true, type);
					deactivatedNow = true;
				} else {
					String packCosIds = subExtraInfoMap
							.get(iRBTConstant.EXTRA_INFO_PACK);
					HashSet<String> packSet = new HashSet<String>(
							ListUtils.convertToList(packCosIds, ","));
					packSet.retainAll(confAzaanCosIdList);
					if ((isPackCallback && packSet.size() > 0)
							|| (!isPackCallback && packSet.size() == 0)) {
						smDeactivateSubscriber(subscriberID, "DAEMON", null,
								true, true, true, type);
						deactivatedNow = true;
					}
				}
			}
		}
		return deactivatedNow;
	}

	public void decrementNumMaxSelections(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberImpl.decrementSelectionCount(conn, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void decrementNumMaxSelectionsForPack(String subscriberID,
			String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			ProvisioningRequestsDao.decrementNumMaxSelections(conn,
					subscriberID, cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public List<ProvisioningRequests> getProvReqByStatus(int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		List<ProvisioningRequests> pendingRecords = null;
		try {
			pendingRecords = ProvisioningRequestsDao.getByStatus(conn, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return pendingRecords;
	}

	public String smUpdateDownloadtoBeActivated(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberDownloadsImpl.smUpdateDownloadtoBeActivated(
					conn, subID(subscriberID), false, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDownloadGraceCallback(String subscriberID,
			String refID, String status) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = true;
		try {
			char downStat = STATE_DOWNLOAD_GRACE;
			success = SubscriberDownloadsImpl.smDownloadGraceSuccess(conn,
					subscriberID, refID, downStat);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDownloadRenewalCallback(String subscriberID,
			String wavFile, String refID, String status,
			boolean m_noDownloadDeactSub, String type, String classType,
			String mode) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		try {
			if (!status.equals("SUCCESS")) {
				downStat = STATE_DOWNLOAD_DEACTIVATED;
				success = SubscriberDownloadsImpl.smDownloadRenewalFailure(
						conn, subscriberID, refID, downStat, mode);
				if (success) {
					// RBT-14044 VF ES - MI Playlist functionality for RBT core
					deleteDownloadwithTstatus(subscriberID, wavFile);

					SubscriberStatusImpl
							.deactivateSettingDownloadRenewalFailure(conn,
									subscriberID, wavFile);
					noDownloadDeactSub(conn, m_noDownloadDeactSub,
							subscriberID, classType, false);
				}
			} else {
				success = SubscriberDownloadsImpl.updateDownloadRenewalSuccess(
						conn, subscriberID, refID, classType);
				updateSettingsForDownloadRenewalSuccessCallback(conn,
						subscriberID, wavFile);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean updateSettingsForDownloadRenewalSuccessCallback(
			Connection conn, String subscriberID, String wavFile) {
		return SubscriberStatusImpl
				.activateSuspendedSettingsForDownloadRenewalCallback(conn,
						subscriberID, wavFile, null, null);
	}

	public boolean updateSettingsForDownloadRenewalSuccessCallback(
			String subscriberID, String wavFile, Integer categoryId,
			Integer categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl
					.activateSuspendedSettingsForDownloadRenewalCallback(conn,
							subscriberID, wavFile, categoryId, categoryType);
		} catch (Throwable t) {
			logger.error("Exception before release connection", t);
			return false;
		} finally {
			releaseConnection(conn);
		}
	}

	public void smUpdateDownloadTNBCallback(Subscriber subscriber) {
		smUpdateDownloadTNBCallback(subscriber, "");
	}

	public void smUpdateDownloadTNBCallback(Subscriber subscriber, String refId) {
		logger.info("Inside smUpdateDownloadTNBCallback");
		Connection conn = getConnection();
		if (conn == null)
			return;
		boolean success = false;
		try {
			if (!RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
					"ADD_TO_DOWNLOADS", "FALSE")
					|| !RBTParametersUtils.getParamAsBoolean(
							iRBTConstant.COMMON, "DEACT_FREE_TNB_DOWNLOADS",
							"FALSE"))
				return;

			SubscriberDownloads[] subDownloads = SubscriberDownloadsImpl
					.getSubscriberAllActiveDownloads(conn, subscriber.subID());
			if (subDownloads == null || subDownloads.length < 1)
				return;
			logger.info("Subscriber has " + subDownloads.length
					+ " active selections ");
			for (int i = 0; i < subDownloads.length; i++) {
				if (subDownloads[i].classType().startsWith("FREE")
						&& !subDownloads[i].refID().equalsIgnoreCase(refId))
					SubscriberDownloadsImpl.smDownloadTNBActivation(conn,
							subscriber.subID(), subDownloads[i].refID(),
							STATE_DOWNLOAD_TO_BE_DEACTIVATED);
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return;
	}

	public SubscriberDownloads getSubscriberDownloadByRefID(
			String subscriberId, String refId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		return SubscriberDownloadsImpl.getSubscriberDownloadByRefID(conn,
				subscriberId, refId);
	}

	public String smUpdateDownloadDeactivationCallback(String subscriberID,
			String wavFile, String refID, String status,
			boolean m_noDownloadDeactSub, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		boolean selDeactStatus = false;
		String downloadStatusStr = m_failure;
		String selStatusStr = m_failure;
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		SubscriberDownloads subDownloadObj = null;
		String newChargeClass = null;
		boolean freeChargeIndc = false;
		try {
			if (status.equals("SUCCESS")) {
				// JIRA-ID - RBT-7933 : VFQ related changes
				Parameters parameterObj = CacheManagerUtil
						.getParametersCacheManager().getParameter("WEBSERVICE",
								"UPGRADE_CHARGE_CLASS_FOR_NEXT_SELECTION",
								"FALSE");
				if (parameterObj.getValue().equalsIgnoreCase("TRUE")) {
					subDownloadObj = SubscriberDownloadsImpl
							.getSubscriberDownloadByRefID(conn, subscriberID,
									refID);
					Subscriber subscriberObj = SubscriberImpl.getSubscriber(
							conn, subscriberID);

					CosDetails cosDetails = CacheManagerUtil
							.getCosDetailsCacheManager().getCosDetail(
									subscriberObj.cosID());
					if (cosDetails != null
							&& cosDetails.getFreechargeClass() != null) {
						if (cosDetails.getFreechargeClass().contains(",")) {
							newChargeClass = cosDetails.getFreechargeClass()
									.split(",")[0];
						} else {
							newChargeClass = cosDetails.getFreechargeClass();
						}
					}
					if (subDownloadObj.classType().equalsIgnoreCase(
							newChargeClass)) {
						freeChargeIndc = true;
					}
				}
				downStat = STATE_DOWNLOAD_DEACTIVATED;
				success = SubscriberDownloadsImpl.smDownloadDeActivation(conn,
						subscriberID, refID, downStat);
				if (success) {
					// RBT-14044 VF ES - MI Playlist functionality for RBT core
					deleteDownloadwithTstatus(subscriberID, wavFile);
					selDeactStatus = SubscriberStatusImpl
							.deactivateSettingDownloadDeact(conn, subscriberID,
									wavFile);
				}
				if (success && m_noDownloadDeactSub) {
					SubscriberDownloads[] subDownloads = SubscriberDownloadsImpl
							.getSubscriberAllActiveDownloads(conn, subscriberID);
					if (subDownloads == null) {
						noDownloadDeactSub(conn, m_noDownloadDeactSub,
								subscriberID, type, subDownloads, false);
						// JIRA-ID - RBT-7933 : VFQ related changes
					} else if ((parameterObj.getValue()
							.equalsIgnoreCase("TRUE")) && freeChargeIndc) {
						if (subDownloads != null) {
							if (subDownloads.length > 1) {
								Arrays.sort(subDownloads,
										new DownloadSetTimeComparator());
							}
							HashMap<String, String> oldExtraInfoMap = DBUtility
									.getAttributeMapFromXML(subDownloads[0]
											.extraInfo());
							if (oldExtraInfoMap == null) {
								oldExtraInfoMap = new HashMap<String, String>();
							}

							oldExtraInfoMap.put("OLD_CLASS_TYPE",
									subDownloads[0].classType());
							String oldSelExtraInfo = DBUtility
									.getAttributeXMLFromMap(oldExtraInfoMap);
							if (newChargeClass != null) {
								updateDownloads(subscriberID,
										subDownloads[0].refID(), 'c',
										oldSelExtraInfo, newChargeClass);
							}
						}
					}
				}
			} else
				success = SubscriberDownloadsImpl.smDownloadDeActivation(conn,
						subscriberID, refID, STATE_DOWNLOAD_TO_BE_DEACTIVATED);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		if (success) {
			downloadStatusStr = m_success;
			if (selDeactStatus)
				selStatusStr = "sel" + m_success;
			return downloadStatusStr + "_" + selStatusStr;
		} else
			return downloadStatusStr + "_" + selStatusStr;
	}

	public String smUpdateDownloadSuspensionCallback(String subscriberID,
			String wavFile, String refID, String status, String type,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberDownloadsImpl.suspendDownload(conn,
					subscriberID, refID, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDownloadResumptionCallback(String subscriberID,
			String wavFile, String refID, String status, String type,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberDownloadsImpl.resumeDownload(conn,
					subscriberID, refID, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/**
	 * API used for updating the Selections when either download resume or
	 * suspension call back comes isSuspension param indicates download call
	 * back type (i.e Suspension. Resume) Suspension callback -> updates the
	 * selections selstatus to z and loop status to either O or L Resumption
	 * callback -> updates the selections selstatus to B and loop status to
	 * either O or L
	 */
	public String updateSettingsForDownloadCallback(String subscriberID,
			String wavFile, boolean isSupension) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;

		String selStatus = "B";
		if (isSupension)
			selStatus = "Z";

		try {
			success = SubscriberStatusImpl.updateSettingsForDownloadCallback(
					conn, subscriberID, wavFile, selStatus, isSupension);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/**
	 * API used for updating the Selections when suspension call back comes
	 * Suspension callback -> updates the selections selstatus to z and loop
	 * status to either O or L
	 */
	public String updateSettingsForPackSuspensionCallback(String subscriberID,
			boolean isProfile) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl
					.updateSettingsForPackSuspensionCallback(conn,
							subscriberID, "Z", isProfile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/**
	 * API used for activating suspended selections and loop status to either O
	 * or L
	 */
	public String activateSuspendedSettingsForPack(String subscriberID,
			boolean isProfile) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		try {
			success = SubscriberStatusImpl.activateSuspendedSettingsForPack(
					conn, subscriberID, isProfile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public SubscriberDownloads getSMDownloadForCallback(Connection conn,
			String subscriberID, String refID) {
		try {
			return SubscriberDownloadsImpl.getSMDownloadForCallback(conn,
					subscriberID, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads getSMDownloadForCallbackOldLogic(
			Connection conn, String subscriberID, String promoID, String time) {
		try {
			return SubscriberDownloadsImpl.getSMDownloadForCallbackOldLogic(
					conn, subscriberID, promoID, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSMDownloadForCallbackOldLogic(Connection conn,
			String subscriberID, String promoID, String time, String refID) {
		try {
			return SubscriberDownloadsImpl.updateSMDownloadForCallbackOldLogic(
					conn, subscriberID, promoID, time, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberDownloads[] getNonFreeDownloads(String subscriberID,
			String chargeClass) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getNonFreeDownloads(conn,
					subID(subscriberID), chargeClass);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String smSubscriptionSuspend(String subscriberID, String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smSubscriptionSuspend(conn,
					subID(subscriberID), classType);
			logger.debug("Suspended subscriber: " + subscriberID
					+ ", suspension status: " + success);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String suspendSubscription(String subscriberID, String classType,
			boolean isSuspendInit) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smSubscriptionSuspend(conn,
					subID(subscriberID), classType, isSuspendInit);
			logger.info("Suspended subscriber: " + subscriberID
					+ ", suspension status: " + success);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSubscriptionGrace(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smSubscriptionGrace(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSubscriptionSuccess(String subscriberID,
			Date nextChargingDate, Date activationDate, String type,
			String classType, boolean isPeriodic, CosDetails cos, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smSubscriptionSuccess(conn,
					subID(subscriberID), nextChargingDate, activationDate,
					type, classType, isPeriodic, cos, null, false, false, null,
					null, -1, null, null, -1, null);
			SubscriberStatusImpl.smUpdateSelStatus(conn, subID(subscriberID),
					STATE_BASE_ACTIVATION_PENDING, STATE_TO_BE_ACTIVATED);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smDeactivationSuccess(String subscriberID, String subYes,
			String extraInfo) {
		return smDeactivationSuccess(subscriberID, subYes, 0, extraInfo,
				m_isMemCachePlayer);
	}

	public String smDeactivationSuccess(String subscriberID, String subYes,
			String extraInfo, boolean updatePlayer) {
		return smDeactivationSuccess(subscriberID, subYes, 0, extraInfo,
				updatePlayer);
	}

	public String smDeactivationSuccess(String subscriberID, String subYes,
			int rbtType, String extraInfo, boolean updatePlayer) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			if (subYes != null && subYes.equalsIgnoreCase("O"))
				SubscriberStatusImpl.deactivate(conn, subID(subscriberID),
						null, true, false, "SM", rbtType);
			success = SubscriberImpl.smDeactivationSuccess(conn,
					subID(subscriberID), subYes, updatePlayer
							&& m_isMemCachePlayer, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smRenewalSuccess(String subscriberID, Date nextChargingDate,
			String type, String classType, String actInfo, String extraInfo,
			String strNextBillingDate,boolean playstatusA) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		boolean isUserVoluntarySuspended = false;
		HashMap <String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo); 
		if(extraInfoMap!=null && extraInfoMap.containsKey(VOLUNTARY)){
		   if(extraInfoMap.get(VOLUNTARY)!=null && extraInfoMap.get(VOLUNTARY).equalsIgnoreCase("SM_SUSPENDED")){
        	  extraInfo = DBUtility.setXMLAttribute(extraInfo, VOLUNTARY, "TRUE");      
        	  isUserVoluntarySuspended = true;
		   }		   
		}
		try {
			success = SubscriberImpl.smRenewalSuccess(conn, subscriberID,
					nextChargingDate, type, classType, actInfo, extraInfo,
					strNextBillingDate, playstatusA);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smRenewalSuccessUpgradePending(String subscriberID,
			Date nextChargingDate, String type, String classType,
			String actInfo, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smRenewalSuccessUpgradePending(conn,
					subID(subscriberID), nextChargingDate, type, classType,
					actInfo, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smRenewalSuccessActivateUser(String subscriberID,
			Date nextChargingDate, String type, String classType,
			String extraInfo, String strNextBillingDate) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		try {
			success = SubscriberImpl.smRenewalSuccessActivateUser(conn,
					subID(subscriberID), nextChargingDate, type, classType,
					extraInfo, strNextBillingDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String updateSubscriber(String subscriberID,
			Map<String, String> attributeMap) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		try {
			success = SubscriberImpl.updateSubscriber(conn,
					subID(subscriberID), attributeMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String processSuspendSubscription(String subscriberID,
			String extraInfo, boolean updateActivationDate) {
		Connection conn = getConnection();
		if (conn == null) {
			return m_connectionError;
		}

		boolean success = false;
		try {
			success = SubscriberImpl.processSuspendSubscription(conn,
					subID(subscriberID), extraInfo, updateActivationDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String processResumeSubscription(String subscriberID,
			String subStatus, String extraInfo, Date ncd) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.processResumeSubscription(conn,
					subID(subscriberID), subStatus, extraInfo, ncd);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	// Added extraInfo - TRAI changes
	public String smSubscriptionRenewalFailure(String subscriberID,
			String deactivatedBy, String type, String classType,
			boolean isRenewal, String extraInfo, boolean updateSM,
			boolean isToBeDeactivateDirectly, String circleIdFromPrism) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			if (!m_retainDownloadsSubDct || !m_addToDownloads)
				SubscriberStatusImpl.smSubscriptionRenewalFailure(conn,
						subID(subscriberID), deactivatedBy, type,
						circleIdFromPrism);
			// even if retain downloads is true all 'W' selections to be
			// deactivated
			else if (m_addToDownloads && m_retainDownloadsSubDct) {
				SubscriberStatusImpl.deactivateNewSelections(conn,
						subID(subscriberID), deactivatedBy, null, null, false,
						1, null, null, circleIdFromPrism);
			}

			if (m_addToDownloads) {
				// even if retain downloads is true, deactivate all 'w'
				// downloads
				if (m_retainDownloadsSubDct) {
					SubscriberDownloadsImpl.expireAllSubscriberPendingDownload(
							conn, subscriberID, "SM", null);
				} else {
					// RBT-14529 Webservice updating both download status (y and
					// t) to x status after getting base deactivation request
					removeDownloadsWithTStatus(subID(subscriberID));
					SubscriberDownloadsImpl.expireAllSubscriberDownloadBaseDct(
							conn, subID(subscriberID), "SM");
				}
			}

			if (!isRenewal && extraInfo != null && extraInfo.contains(REFUND))
				extraInfo = DBUtility.removeXMLAttribute(extraInfo, REFUND);
			// RBT-14536-Vodafone In:- Deactivated subscriber with UDS true in
			// Extra_info when request for song RBT is rejecting request
			if (extraInfo != null && extraInfo.contains(UDS_OPTIN)) {
				extraInfo = DBUtility.removeXMLAttribute(extraInfo, UDS_OPTIN);
			}

			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(
					subscriberID, true);
			HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
					deactivatedBy);
			packExtraInfoMap.put(
					iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
					new Date().toString());
			RBTDBManager.getInstance().deactivateAllPack(subscriber,
					packExtraInfoMap);
			success = SubscriberImpl.smSubscriptionRenewalFailure(conn,
					subID(subscriberID), deactivatedBy, type, classType,
					m_isMemCachePlayer && isToBeDeactivateDirectly, extraInfo,
					updateSM, circleIdFromPrism);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}
	//vikrant-om-1
		public String smSubscriptionRenewalFailureOnlyBaseDeactivation(String subscriberID,
				String deactivatedBy, String type, String classType,
				boolean isRenewal, String extraInfo, boolean updateSM,
				boolean isToBeDeactivateDirectly, String circleIdFromPrism) 
		{
			Connection conn = getConnection();
			if (conn == null)
				return m_connectionError;
			boolean success = false;
			try {
				if (!isRenewal && extraInfo != null && extraInfo.contains(REFUND))
					extraInfo = DBUtility.removeXMLAttribute(extraInfo, REFUND);
				// RBT-14536-Vodafone In:- Deactivated subscriber with UDS true in
				// Extra_info when request for song RBT is rejecting request
				if (extraInfo != null && extraInfo.contains(UDS_OPTIN)) {
					extraInfo = DBUtility.removeXMLAttribute(extraInfo, UDS_OPTIN);
				}

				Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID, true);
				HashMap<String, String> packExtraInfoMap = new HashMap<String, String>();
				packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, deactivatedBy);
				packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME, new Date().toString());
				RBTDBManager.getInstance().deactivateAllPack(subscriber, packExtraInfoMap);
				success = SubscriberImpl.smSubscriptionRenewalFailure(conn, subID(subscriberID), deactivatedBy, type,
						classType, m_isMemCachePlayer && isToBeDeactivateDirectly, extraInfo, updateSM, circleIdFromPrism);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			return success ? m_success : m_failure;
		}
	//vikrant	
	public String smPackSubscriptionFailure(String subscriberID, String cosId,
			int noMaxSelections, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smPackSubscriptionFailure(conn,
					subID(subscriberID), cosId, noMaxSelections, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smPackSubscriptionSuccess(String subscriberID,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smPackSubscriptionSuccess(conn,
					subID(subscriberID), extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smPackSubscriptionRenewalCallback(String subscriberID,
			String cosId, int numMaxSelections, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smPackSubscriptionRenewalCallback(conn,
					subID(subscriberID), cosId, numMaxSelections, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/**
	 * @param subscriberID
	 * @return
	 * 
	 *         Updates the CHARGE_CLASS of downloads/selections to DEFAULT
	 *         chargeclass & Download/Selection status to TO_BE_ACTIVATED on
	 *         Pack activation failure. Currently updating only the chargeclass
	 *         of downloads. For selection model operators, same operation has
	 *         to be done on SELECTION table in stead of DOWNLOADS table
	 */
	public String smUpdateSongsToDefaultOnPackActivationFailure(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberDownloadsImpl
					.smUpdateSongsToDefaultOnPackActivationFailure(conn,
							subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUnsubscriptionFailure(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.smUnsubscriptionFailure(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/* ADDED FOR TATA */
	public boolean deactivationFailedForTATA(String subscriberId) {
		return deactivationFailedForTATA(subID(subscriberId), 0);
	}

	public boolean deactivationFailedForTATA(String subscriberId, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			boolean subResult = SubscriberImpl.updateDeactivationFailed(conn,
					subID(subscriberId));
			boolean subStatusResult = SubscriberStatusImpl
					.updateDeactivationFailed(conn, subID(subscriberId),
							rbtType);
			return subResult & subStatusResult;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public ArrayList getUpdateToDeactivateSubscribers(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberImpl.getUpdateToDeactivateSubscribers(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String smSelectionActivationSuccess(String subscriberID,
			String callerID, int status, String setTime, Date nextChargingDate,
			Date startDate, String type, int fromTime, int toTime,
			String classType, char newLoopStatus, String selInfo, int rbtType,
			String selInterval, String refID, String extraInfo,
			boolean updateSelStatus, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smActivateNewSelection2(conn,
					subID(subscriberID), refID, nextChargingDate, startDate,
					type, classType, newLoopStatus, selInfo, rbtType,
					selInterval, extraInfo, updateSelStatus, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smDeactivateOtherUGSSelections(String subscriberID,
			String callerID, String type, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smDeactivateOtherUGSSelections(conn,
					subID(subscriberID), subID(callerID), type, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSelectionDeactivationSuccess(String subscriberID,
			String refID, char newLoopStatus, int rbtType,
			String circleIDFromPrism) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionDeactivationSuccess(conn,
					subID(subscriberID), refID, newLoopStatus, rbtType,
					circleIDFromPrism);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smPackUpdationSuccess(String subscriberID, String refID,
			String deselectedBy, String status, boolean appendNextDate,
			int numMaxSelections) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.updateRequest(subscriberID,
					refID, status, appendNextDate, numMaxSelections);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public boolean updateProvisioningRequestsStatusAndExtraInfo(
			String subscriberID, String refID, int status, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.info("Connection Error occurred");
			return false;
		}

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.updateRequestStatusAndExtraInfo(
					conn, subscriberID, refID, status, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;

	}

	public String smActFailureSuccess(String subscriberID, String cosID,
			String status, boolean appendNextDate) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.updateOnActFailure(subscriberID,
					cosID, status, appendNextDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smPackRenewalFailure(String subscriberID, String refID,
			String deselectedBy, String status, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.deactivateRequest(subscriberID,
					refID, status, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smDeactivateRefundedSelection(String subscriberID,
			String refID, char newLoopStatus, String callerID, int rbtType,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smDeactivateRefundedSelection(conn,
					subID(subscriberID), refID, newLoopStatus, callerID,
					rbtType, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smReactivateSelection(String subscriberID, String refID,
			String callerID, char loopStatus, int rbtType, String selExtraInfo,
			String selStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smReactivateSelection(conn,
					subID(subscriberID), refID, subID(callerID), loopStatus,
					rbtType, selExtraInfo, selStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smDeactivateOldSelection(String subscriberID,
			String callerID, int status, String setTime, int fromTime,
			int toTime, int rbtType, String selInterval, String refID,boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smDeactivateOldSelection(conn,
					subID(subscriberID), subID(callerID), status, setTime,
					fromTime, toTime, rbtType, selInterval, refID,isDirectDeactivation);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smDeactivateOldSuspendedSelections(String subscriberID,
			String callerID, int status, String setTime, int fromTime,
			int toTime, int rbtType, String selInterval, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smDeactivateOldSuspendedSelections(
					conn, subID(subscriberID), subID(callerID), status,
					setTime, fromTime, toTime, rbtType, selInterval, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public List<String> smGetAllDeactivateOldSelection(String subscriberID,
			String callerID, int status, String setTime, int fromTime,
			int toTime, int rbtType, String selInterval, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.smGetAllDeactivateOldSelection(conn,
					subID(subscriberID), subID(callerID), status, setTime,
					fromTime, toTime, rbtType, selInterval, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String smDeactivateOldSelectionBasedOnRefID(String subscriberID,
			String callerID, String setTime, int fromTime, int toTime,
			int rbtType, String selInterval, String refID,
			List<String> refIDList) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl
					.smDeactivateOldSelectionBasedOnRefID(conn,
							subID(subscriberID), subID(callerID), setTime,
							fromTime, toTime, rbtType, selInterval, refID,
							refIDList);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateAndDeactivateOldSelection(String subscriberID,
			ArrayList<String> refIdList, String extraInfoStr, String setTime,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smUpdateAndDeactivateOldSelection(
					conn, subID(subscriberID), refIdList, setTime, rbtType,
					extraInfoStr);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSelectionRenewalSuccess(String subscriberID, String refID,
			Date nextChargingDate, String type, String classType,
			String selectionInfo, int rbtType, String loopStatus,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionRenewalSuccess(conn,
					subID(subscriberID), refID, nextChargingDate, type,
					classType, selectionInfo, rbtType, loopStatus, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smPackRenewalSuccess(String subscriberID, String refID,
			Date nextChargingDate, String classType, int cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.updateNextRetryTime(subscriberID,
					refID, nextChargingDate, classType, cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smODAPackRenewalSuccess(String subscriberID, String refID,
			Date nextChargingDate, String classType, int categoryID) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = ProvisioningRequestsDao.updateNextRetryTime(subscriberID,
					refID, nextChargingDate, classType, categoryID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSelectionActivationRenewalFailure(String subscriberID,
			String refID, String deactivatedBy, String type, String classType,
			char newLoopStatus, int rbtType, String extraInfo, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionActivationRenewalFailure(
					conn, subID(subscriberID), refID, deactivatedBy, type,
					classType, newLoopStatus, rbtType, extraInfo, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smSelectionDeactivationFailure(String subscriberID,
			String refID, String type, int rbtType, String extraInfo,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionDeactivationFailure(conn,
					subID(subscriberID), refID, type, rbtType, extraInfo,
					circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	/* subscription manager daemon */
	public boolean smURLSubscription(String subscriberID, boolean isSuccess,
			boolean isError, String prevDelayDeactSubYes) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.smURLSubscription(conn, subID(subscriberID),
					isSuccess, isError, prevDelayDeactSubYes);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smUpdatePackStatusOnBaseAct(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ProvisioningRequestsDao.smUpdatePackStatusOnBaseAct(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smUpdateSpecificPackStatusOnBaseAct(String subscriberID,
			String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ProvisioningRequestsDao.smURLPackActivationOnBaseAct(conn,
					subID(subscriberID), cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLPackActivation(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ProvisioningRequestsDao.smURLPackActivation(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLUnSubscription(String subscriberID, boolean isSuccess,
			boolean isError) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.smURLUnSubscription(conn,
					subID(subscriberID), isSuccess, isError);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smUpdatePackStatusOnBaseDeact(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ProvisioningRequestsDao.smUpdatePackStatusOnBaseDeact(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLPackDeactivation(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ProvisioningRequestsDao.smURLPackDeactivation(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Subscriber[] smGetActivatedSubscribers(int fetchSize,
			ArrayList lowModes, boolean getLowPriority) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return (Subscriber[]) SubscriberImpl.smGetActivatedSubscribers(
					conn, fetchSize, false, lowModes, getLowPriority);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetActivatedSubscribersAsList(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return (ArrayList) SubscriberImpl.smGetActivatedSubscribers(conn,
					fetchSize, true, null, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetActivationPendingSubscribersPost(int fetchSize) {
		return smGetActivationPendingSubscribersInternal(fetchSize, "n");
	}

	public ArrayList smGetActivationPendingSubscribersPre(int fetchSize) {
		return smGetActivationPendingSubscribersInternal(fetchSize, "y");
	}

	private ArrayList smGetActivationPendingSubscribersInternal(int fetchSize,
			String prepaidYes) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.smGetActivationPendingSubscribers(conn,
					prepaidYes, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] smGetDeactivatedSubscribers(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return (Subscriber[]) SubscriberImpl.smGetDeactivatedSubscribers(
					conn, fetchSize, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] smGetDirectActivatedSelections(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.smGetDirectActivatedSelections(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetDeactivatedSubscribersAsList(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return (ArrayList) SubscriberImpl.smGetDeactivatedSubscribers(conn,
					fetchSize, true);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetDeactivationPendingSubscribersPost(int fetchSize) {
		return smGetDeactivationPendingSubscribersInternal(fetchSize, "n");
	}

	public ArrayList smGetDeactivationPendingSubscribersPre(int fetchSize) {
		return smGetDeactivationPendingSubscribersInternal(fetchSize, "y");
	}

	private ArrayList smGetDeactivationPendingSubscribersInternal(
			int fetchSize, String prepaidYes) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberImpl.smGetDeactivationPendingSubscribers(conn,
					prepaidYes, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean smURLSelectionActivationRetry(String subscriberID,
			String callerID, int status, Date setDate, int fromTime,
			int toTime, Date startTime, int rbtType, String wavFile,
			String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.smURLSelectionActivationRetry(conn,
					subID(subscriberID), subID(callerID), status, setDate,
					startTime, rbtType, wavFile, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLSelectionActivation(String subscriberID,
			String callerID, int status, Date setDate, int fromTime,
			int toTime, boolean isSuccess, boolean isError, String setTime,
			char newLoopStatus, Date startTime, int rbtType, String wavFile,
			boolean updateRefID, String refID, String interval,
			String extraInfo, List<String> songWavFilesList) throws Exception {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (!isError
					&& !isSuccess
					&& (newLoopStatus == LOOP_STATUS_OVERRIDE
							|| newLoopStatus == LOOP_STATUS_OVERRIDE_INIT || newLoopStatus == LOOP_STATUS_OVERRIDE_FINAL)) {
				if (songWavFilesList != null)
					songWavFilesList.addAll(SubscriberStatusImpl
							.smGetDeactivateOldSelection(conn, subscriberID,
									callerID, status, setTime, fromTime,
									toTime, rbtType, interval, refID));

				SubscriberStatusImpl.smDeactivateOldSelection(conn,
						subID(subscriberID), subID(callerID), status, setTime,
						fromTime, toTime, rbtType, interval, refID,false);
			}
			return SubscriberStatusImpl.smURLSelectionActivation(conn,
					subID(subscriberID), subID(callerID), status, setDate,
					isSuccess, isError, newLoopStatus, startTime, rbtType,
					updateRefID, wavFile, refID, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLDownloadActivation(String subscriberID,
			boolean isSuccess, boolean isError, String refID, String classType,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			char downStat = STATE_DOWNLOAD_ACTIVATION_PENDING;
			if (isError)
				downStat = STATE_DOWNLOAD_ACT_ERROR;
			else if (!isSuccess)
				downStat = STATE_DOWNLOAD_ACTIVATED;
			return SubscriberDownloadsImpl.smURLDownloadActivation(conn,
					subID(subscriberID), refID, downStat, classType, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLDownloadDeActivation(String subscriberID,
			boolean isSuccess, boolean isError, String refID, String wavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			boolean isAlreadyDct = false;
			char downStat = STATE_DOWNLOAD_DEACTIVATION_PENDING;
			if (isError)
				downStat = STATE_DOWNLOAD_DEACT_ERROR;
			else if (!isSuccess) {
				isAlreadyDct = true;
				downStat = STATE_DOWNLOAD_DEACTIVATED;
			}
			success = SubscriberDownloadsImpl.smURLDownloadDeActivation(conn,
					subID(subscriberID), refID, downStat);
			if (isAlreadyDct && success)
				SubscriberStatusImpl.deactivateSettingDownloadDeact(conn,
						subscriberID, wavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public boolean smURLSelectionDeactivation(String subscriberID,
			String refID, boolean isSuccess, boolean isError, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.smURLSelectionDeactivation(conn,
					subID(subscriberID), refID, isSuccess, isError, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smURLSelectionNotSendSMDeactivation(String subscriberID,
			String callerID, int status, Date setDate, boolean isSuccess,
			boolean isError, String subscriberFile, int rbtType,
			char oldLoopStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
			if (oldLoopStatus == LOOP_STATUS_EXPIRED)
				newLoopStatus = oldLoopStatus;
			return SubscriberStatusImpl.smURLSelectionNotSendSMDeactivation(
					conn, subID(subscriberID), subID(callerID), status,
					setDate, isSuccess, isError, rbtType, newLoopStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus[] smGetActivatedSelections(int fetchSize,
			ArrayList lowModes, boolean getLowReq) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return (SubscriberStatus[]) SubscriberStatusImpl
					.smGetActivatedSelections(conn, fetchSize, null, false,
							lowModes, getLowReq);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetActivatedSelectionsPostAsList(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return (ArrayList) SubscriberStatusImpl.smGetActivatedSelections(
					conn, fetchSize, "n", true, null, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetActivatedSelectionsPreAsList(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return (ArrayList) SubscriberStatusImpl.smGetActivatedSelections(
					conn, fetchSize, "y", true, null, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList smGetSettingsToBeDeleted(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return (ArrayList) SubscriberStatusImpl.smGetSettingsToBeDeleted(
					conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean isSubscriberActivationPending(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_TO_BE_ACTIVATED)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_ACTIVATION_ERROR))
			return true;
		return false;
	}

	public boolean isSubscriberActive(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
				|| subscriber.subYes().equals(iRBTConstant.STATE_CHANGE)
				|| subscriber.subYes().equals(iRBTConstant.STATE_EVENT))
			return true;
		return false;
	}

	public boolean isSubscriberStrictlyActive(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED))
			return true;
		return false;
	}
	
	public boolean isSubscriberDeactivationPending(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATION_PENDING)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_TO_BE_DEACTIVATED)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_DEACTIVATED_INIT)
				|| subscriber.subYes().equals(
						iRBTConstant.STATE_DEACTIVATION_ERROR)
				|| (subscriber.endDate().before(new Date()) && subscriber
						.subYes().equals(iRBTConstant.STATE_EVENT)))
			return true;
		return false;
	}

	public boolean isSubscriberActivated(Subscriber subscriber, int rbtType) {
		if (subscriber != null) {
			if ((rbtType == 0 && (subscriber.rbtType() == 0 || subscriber
					.rbtType() == TYPE_RBT_RRBT))
					|| (rbtType == 2 && (subscriber.rbtType() == 2
							|| subscriber.rbtType() == TYPE_RBT_RRBT || subscriber
							.rbtType() == TYPE_SRBT_RRBT))
					|| (rbtType == 1 && (subscriber.rbtType() == 1 || subscriber
							.rbtType() == TYPE_SRBT_RRBT))) {
				if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
						|| subscriber.subYes().equals(iRBTConstant.STATE_EVENT)
						|| subscriber.subYes()
								.equals(iRBTConstant.STATE_CHANGE))
					return true;
			}
		}
		return false;
	}

	public boolean isSubscriberActivated(Subscriber subscriber) {
		if (subscriber != null
				&& (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
						|| subscriber.subYes().equals(iRBTConstant.STATE_EVENT) || subscriber
						.subYes().equals(iRBTConstant.STATE_CHANGE)))
			return true;
		return false;
	}

	public boolean isSubscriberActivated(String subscriberID) {
		Subscriber subscriber = getSubscriber(subscriberID);
		if (subscriber != null
				&& (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
						|| subscriber.subYes().equals(iRBTConstant.STATE_EVENT) || subscriber
						.subYes().equals(iRBTConstant.STATE_CHANGE)))
			return true;
		return false;
	}

	public boolean isSubscriberDeactivated(Subscriber subscriber) {
		if (subscriber == null
				|| subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			return true;
		return false;
	}

	public boolean isSubscriberRenewalPending(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.endDate().before(new Date())
				&& subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED))
			return true;
		return false;
	}

	public boolean isSubscriberSuspended(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_SUSPENDED)
				|| subscriber.subYes()
						.equals(iRBTConstant.STATE_SUSPENDED_INIT))
			return true;
		return false;
	}

	public boolean isSubscriberInGrace(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_GRACE))
			return true;
		return false;
	}

	public boolean isSelectionActivationPending(
			SubscriberStatus subscriberStatus) {
		if (subscriberStatus == null)
			return false;
		if (subscriberStatus.selStatus().equals(
				iRBTConstant.STATE_ACTIVATION_PENDING)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_TO_BE_ACTIVATED)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_ACTIVATION_ERROR)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_BASE_ACTIVATION_PENDING)
				|| subscriberStatus.selStatus().equals(iRBTConstant.STATE_UN))
			return true;
		return false;
	}

	public boolean isSelectionActivated(SubscriberStatus subscriberStatus) {
		if (subscriberStatus != null
				&& (subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_ACTIVATED)
						|| subscriberStatus.selStatus().equals(
								iRBTConstant.STATE_EVENT)
						|| subscriberStatus.selStatus().equals(
								iRBTConstant.STATE_REQUEST_RENEWAL) || subscriberStatus
						.selStatus().equals(iRBTConstant.STATE_CHANGE)))
			return true;
		return false;
	}

	public boolean isSelectionDeactivationPending(
			SubscriberStatus subscriberStatus) {
		if (subscriberStatus == null)
			return false;
		if (subscriberStatus.selStatus().equals(
				iRBTConstant.STATE_DEACTIVATION_PENDING)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_TO_BE_DEACTIVATED)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_DEACTIVATED_INIT)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_DEACTIVATION_ERROR))
			return true;
		return false;
	}

	public boolean isSelectionDeactivated(SubscriberStatus subscriberStatus) {
		if (subscriberStatus == null
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_DEACTIVATED))
			return true;
		return false;
	}

	public boolean isSelectionSuspended(SubscriberStatus subscriberStatus) {
		if (subscriberStatus == null)
			return false;
		if (subscriberStatus.selStatus().equals(iRBTConstant.STATE_SUSPENDED)
				|| subscriberStatus.selStatus().equals(
						iRBTConstant.STATE_SUSPENDED_INIT))
			return true;
		return false;
	}

	public boolean isSelectionGrace(SubscriberStatus subscriberStatus) {
		if (subscriberStatus == null)
			return false;
		if (subscriberStatus.selStatus().equals(iRBTConstant.STATE_GRACE))
			return true;
		return false;
	}

	public boolean isDownloadActivationPending(
			SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null)
			return false;
		if (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACTIVATION_PENDING
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_TO_BE_ACTIVATED
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_BASE_ACT_PENDING)
			return true;
		return false;
	}

	public boolean isDownloadActivated(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads != null
				&& (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACTIVATED
						|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_CHANGE || subscriberDownloads
						.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_SEL_TRACK))
			return true;
		return false;
	}

	public boolean isDownloadDeactivationPending(
			SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null)
			return false;
		if (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACTIVATION_PENDING
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_TO_BE_DEACTIVATED)
			return true;
		return false;
	}

	public boolean isDownloadErrorState(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null)
			return false;
		if (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACT_ERROR
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACT_ERROR)
			return true;
		return false;
	}

	public boolean isDownloadDeactivated(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACTIVATED)
			return true;
		return false;
	}

	public boolean isDownloadSuspended(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null
				|| subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_SUSPENSION)
			return true;
		return false;
	}

	public boolean isDownloadGrace(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads == null)
			return false;
		if (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_GRACE)
			return true;
		return false;
	}

	public boolean checkCanAddSetting(Subscriber subscriber) {
		if (subscriber == null
				|| subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			return false;
		if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
				|| subscriber.subYes().equals(iRBTConstant.STATE_EVENT))
			return true;
		return false;
	}

	public boolean checkCanAddSetting(String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return checkCanAddSetting(SubscriberImpl.getSubscriber(conn,
					subID(subscriberId)));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus[] smGetRenewalSelections(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.smGetRenewalSelections(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] smGetDeactivatedSelections(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.smGetDeactivatedSelections(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean smTrialSelectionCharging(String subscriberID,
			String callerID, int status, Date setDate, boolean OptIn) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.smTrialSelectionCharging(conn,
					subID(subscriberID), subID(callerID), status, setDate,
					OptIn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus[] smGetTrialSelections(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.smGetTrialSelections(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	public boolean deactivateSubWavFile(String subscriberID, String subWavFile,
			String selStaus, String deselectedBy, String sendSMS) {
		return deactivateSubWavFile(subID(subscriberID), subWavFile, selStaus,
				deselectedBy, sendSMS, 0);
	}

	public boolean deactivateSubWavFile(String subscriberID, String subWavFile,
			String selStaus, String deselectedBy, String sendSMS, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.deactivateSubWavFile(conn,
					subID(subscriberID), subWavFile, selStaus, deselectedBy,
					sendSMS, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean deactivateSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile, String selStaus,
			String deselectedBy, String sendSMS) {
		return deactivateSubWavFileForCaller(subID(subscriberID),
				subID(callerID), subWavFile, selStaus, deselectedBy, sendSMS, 0);
	}

	public boolean deactivateSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile, String selStaus,
			String deselectedBy, String sendSMS, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.deactivateSubWavFileForCaller(conn,
					subID(subscriberID), callerID, subWavFile, selStaus,
					deselectedBy, sendSMS, true, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean deactivateSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile, String selStaus,
			String deselectedBy, String sendSMS, boolean checkSelStatus) {
		return deactivateSubWavFileForCaller(subID(subscriberID),
				subID(callerID), subWavFile, selStaus, deselectedBy, sendSMS,
				checkSelStatus, 0);
	}

	public boolean deactivateSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile, String selStaus,
			String deselectedBy, String sendSMS, boolean checkSelStatus,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubWavFileForCaller(conn,
					subID(subscriberID), callerID, subWavFile, selStaus,
					deselectedBy, sendSMS, checkSelStatus, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus getSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile) {
		return getSubWavFileForCaller(subID(subscriberID), subID(callerID),
				subWavFile, 0);
	}

	public SubscriberStatus getSubWavFileForCaller(String subscriberID,
			String callerID, String subWavFile, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getSubWavFileForCaller(conn,
					subID(subscriberID), callerID, subWavFile, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int getPromoFreeDays(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		try {
			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID), type);
			return subscriberPromo == null ? 0 : subscriberPromo.freedays();
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public SubscriberPromo getSubscriberPromo(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberPromoImpl.getActiveSubscriberPromo(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean endPromo(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberPromoImpl
					.endPromo(conn, subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberPromo getSubscriberPromo(String subscriberID,
			String activatedBy, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberPromoImpl.getSubscriberPromo(conn,
					subID(subscriberID), activatedBy, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberPromo createSubscriberPromo(String subscriberID,
			int freedays, boolean isPrepaid, String activatedBy, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberPromoImpl.insert(conn, subID(subscriberID),
					freedays, isPrepaid, activatedBy, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean changeSubscriberPromoActivatedBy(String subscriberID,
			String activatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberPromoImpl.changeActivatedBy(conn,
					subID(subscriberID), activatedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeSubscriberPromo(String subscriberID,
			String activatedBy, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberPromoImpl.remove(conn, subID(subscriberID),
					activatedBy, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean changeSubscriberPromoFreeDays(String subscriberID,
			int freeDays) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberPromoImpl.changeFreeDays(conn,
					subID(subscriberID), freeDays);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Access getAccess(int clipID, String name, String year, String month,
			Date currentDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Access access = null;
		try {
			access = AccessImpl.getAccess(conn, clipID, year, month,
					currentDate);
			if (access == null)
				access = AccessImpl.insert(conn, clipID, name, year, month, 0,
						0, 0, currentDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return access;
	}

	public Integer[] getMostAccesses(int fetchSize, int noOfhotSongsDays) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return AccessImpl
					.getMostAccesses(conn, fetchSize, noOfhotSongsDays);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeAccessToDate(int noOfhotSongsDaysToRemove) {
		if (noOfhotSongsDaysToRemove == 0)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return AccessImpl
					.removeAccessToDate(conn, noOfhotSongsDaysToRemove);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeAccess(Access access) {
		if (access == null)
			return false;

		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			Access temp = AccessImpl.getAccess(conn, access.clipID(),
					access.year(), access.month(), access.accessDate());
			success = (temp == null) ? false : AccessImpl.remove(conn,
					access.clipID(), access.year(), access.month(),
					access.accessDate());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public UserRights insertUserRights(String type, String rights) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			UserRightsImpl.remove(conn, type.trim());
			return UserRightsImpl.insert(conn, type.trim(), rights.trim());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public UserRights getUserRights(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return UserRightsImpl.getUserRights(conn, type.trim());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList<String> getAllDownloadOfTheDayDates(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PickOfTheDayImpl.getAllDownloadOfTheDayDates(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay getDownloadOfTheDay(String playDate,
			String chargeClass, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PickOfTheDayImpl.getDownloadfTheDay(conn, playDate, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID,
			String playDate) {
		return insertPickOfTheDay(categoryID, clipID, playDate, null, 'b', null);
	}

	public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID,
			String playDate, String circleId, char prepaidYes, String profile) {
		return insertPickOfTheDay(categoryID, clipID, playDate, circleId,
				prepaidYes, profile, null);
	}

	public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID,
			String playDate, String circleId, char prepaidYes, String profile,
			String language) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.insert(conn, categoryID, clipID, playDate,
					circleId, prepaidYes, profile, language);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removePickOfTheDay(String playDate, String circleID,
			char prepaidYes, String profile, String language) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return PickOfTheDayImpl.remove(conn, playDate, circleID,
					prepaidYes, profile, language);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updatePickOfTheDay(int categoryID, int clipID,
			String playDate, String circleID, char prepaidYes, String profile) {
		return updatePickOfTheDay(categoryID, clipID, playDate, circleID,
				prepaidYes, profile, null);
	}

	public boolean updatePickOfTheDay(int categoryID, int clipID,
			String playDate, String circleID, char prepaidYes, String profile,
			String language) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return PickOfTheDayImpl.update(conn, categoryID, clipID, playDate,
					circleID, prepaidYes, profile, language);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public PickOfTheDay getPickOfTheDay(String playDate, String circleId,
			char prepaidYes) {
		return getPickOfTheDay(playDate, circleId, prepaidYes, null, null);
	}

	public PickOfTheDay getPickOfTheDay(String playDate, String circleId,
			char prepaidYes, String profile, boolean profileSpecific) {
		return getPickOfTheDay(playDate, circleId, prepaidYes, profile,
				profileSpecific, null, false);
	}

	public PickOfTheDay getPickOfTheDay(String playDate, String circleId,
			char prepaidYes, String profile, boolean profileSpecific,
			String language, boolean languageSpecific) {
		PickOfTheDay pickOfTheDay = getPickOfTheDay(playDate, circleId,
				prepaidYes, profile, language);
		if (pickOfTheDay != null
				&& ((profileSpecific && profile != null && pickOfTheDay
						.profile() == null) || (languageSpecific
						&& language != null && pickOfTheDay.language() == null)))
			pickOfTheDay = null;
		return pickOfTheDay;
	}

	public PickOfTheDay getDownloadOfTheDay(String playDate, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getDownloadfTheDay(conn, playDate, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getDownloadOfTheDays(String startDate,
			String endDate, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PickOfTheDayImpl.getPickOfTheDays(conn, startDate, endDate,
					circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public PickOfTheDay getPickOfTheDay(String playDate, String circleId,
			char prepaidYes, String profile, String language) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		PickOfTheDay pickOfTheDay = null;
		try {
			PickOfTheDay[] pickOfTheDays = PickOfTheDayImpl.getPickOfTheDay(
					conn, playDate, circleId, prepaidYes, profile, true,
					language, true);

			if (pickOfTheDays != null) {
				boolean foundCircleSpecific = false;
				boolean foundProfileSpecific = false;
				for (int i = 0; i < pickOfTheDays.length; i++) {
					if ((!foundCircleSpecific && !foundProfileSpecific)
							|| (pickOfTheDays[i].circleID() != null && !pickOfTheDays[i]
									.circleID().equalsIgnoreCase("ALL")))
						pickOfTheDay = pickOfTheDays[i];
					else
						continue;

					if (pickOfTheDay.circleID() != null
							&& pickOfTheDay.circleID().equalsIgnoreCase("ALL")) {
						if (pickOfTheDay.profile() != null)
							foundProfileSpecific = true;
					} else if (pickOfTheDay.circleID() != null) {
						if (pickOfTheDay.profile() != null)
							break;
						foundCircleSpecific = true;
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return pickOfTheDay;
	}

	public PickOfTheDay[] getPickOfTheDays(String playDate, String circleID,
			char prepaidYes, String profile, boolean checkProfile) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getPickOfTheDay(conn, playDate, circleID,
					prepaidYes, profile, checkProfile, null, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getPickOfTheDayForTATAPrepaid(String playDate,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getPickOfTheDayForTATAPrepaid(conn,
					playDate, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getPickOfTheDays(String range, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PickOfTheDayImpl.getPickOfTheDays(conn, range, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getAllPickOfTheDays(String circleId, char isPrepaid,
			String playDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getAllPickOfTheDays(conn, circleId,
					isPrepaid, playDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removePickOfTheday(int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return PickOfTheDayImpl.remove(conn, clipId, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removePickOfTheday(int clipId, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return PickOfTheDayImpl.remove(conn, clipId, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public PickOfTheDay getPickOfTheDay(String playDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			PickOfTheDay[] pickOfTheDays = PickOfTheDayImpl.getPickOfTheDay(
					conn, playDate, null, 'b', null, false, null, false);

			PickOfTheDay pickOfTheDay = null;
			if (pickOfTheDays != null)
				pickOfTheDay = pickOfTheDays[pickOfTheDays.length - 1];
			return pickOfTheDay;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay getPickOfTheDay(String playDate, String profile) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getPickOfTheDay(conn, playDate, profile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getPickOfTheDays(String range) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getPickOfTheDays(conn, range, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PickOfTheDay[] getPickOfTheDayForTATAForAllCircle(String playDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PickOfTheDayImpl.getPickOfTheDayForTATAForAllCircle(conn,
					playDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean isValidPrefix(String subscriberID) {
		subscriberID = subID(subscriberID);
		if (Utility.isValidNumber(subscriberID)) {
			SitePrefix sitePrefix = Utility.getPrefix(subscriberID);
			return (sitePrefix != null && sitePrefix.getSiteUrl() == null);
		}
		return false;
	}

	public boolean isValidOperatorPrefix(String subscriberID) {
		if (subscriberID == null
				|| subscriberID.length() < m_minPhoneNumberLenGlobal
				|| subscriberID.length() > m_maxPhoneNumberLenGlobal)
			return false;
		else {
			try {
				Long.parseLong(subID(subscriberID));
			} catch (Throwable e) {
				logger.error("Non-numeric subscriberID : " + subscriberID, e);
				return false;
			}
		}

		if (m_operatorPrefix == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter("GATHERER", "OPERATOR_PREFIX");
			if (tempParam == null)
				m_operatorPrefix = "";
			else
				m_operatorPrefix = tempParam.getValue();
		}
		String subPrefix = (subID(subscriberID)).substring(0, getPrefixIndex());
		return (m_operatorPrefix.indexOf(subPrefix) != -1);
	}

	/* ADDED FOR TATA */
	@Deprecated
	public String getCircleId(String subscriberID) {
		subscriberID = subID(subscriberID);
		SitePrefix prefix = Utility.getPrefix(subscriberID);
		String circleID = null;
		if (prefix != null)
			circleID = prefix.getCircleID();
		else
			logger.info("Could not get circle for " + subscriberID);

		return circleID;
	}

	public Subscriber[] getBulkPromoSubscribers(String bulkPromoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberImpl.getBulkPromoSubscribers(conn, bulkPromoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String[] getBulkPromoAvailedSubscribers(String bulkPromoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberImpl.getBulkPromoAvailedSubscribers(conn,
					bulkPromoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateNumMaxSelections(String subscriberId, int maxSelections) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateNumMaxSelections(conn,
					subID(subscriberId), maxSelections);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public StatusType insertStatusType(int code, String desc, boolean showGUI,
			boolean showVUI) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			String gui = "n";
			if (showGUI)
				gui = "y";

			String vui = "n";
			if (showVUI)
				vui = "y";

			StatusTypeImpl.remove(conn, code);
			return StatusTypeImpl.insert(conn, code, desc, gui, vui);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public StatusType getStatusType(int code) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return StatusTypeImpl.getStatusType(conn, code);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public StatusType[] getStatusTypes() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return StatusTypeImpl.getStatusTypes(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedStatus insertFeedStatus(String type, String status, String file,
			String smsKeyword, String subKeyword, String smsFeedOnSuccess,
			String smsFeedOnFailure, String smsFeedOffSuccess,
			String smsFeedOffFailure, String smsFeedFailure,
			String smsFeedNonActiveSub) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			FeedStatusImpl.remove(conn, type);
			return FeedStatusImpl.insert(conn, type, status, file, smsKeyword,
					subKeyword, smsFeedOnSuccess, smsFeedOnFailure,
					smsFeedOffSuccess, smsFeedOffFailure, smsFeedFailure,
					smsFeedNonActiveSub);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedStatus getFeedStatus(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedStatusImpl.getFeedStatus(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean setStatus(String type, String status) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			FeedStatusImpl.setStatus(conn, type, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean setFile(String type, String file) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			FeedStatusImpl.setFile(conn, type, file);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public ChargePromoTypeMap[] getChargePromoTypeMaps() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargePromoTypeMapImpl.getChargePromoTypeMaps(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Integer getParentCategoryIdfcategoryCatId(int catID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return CategoriesImpl.getParentCategoryId(conn, catID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargePromoTypeMap[] getChargePromoTypeMapsByLevel(int level,
			String chargeType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargePromoTypeMapImpl.getChargePromoTypeMapsByLevel(conn,
					level, chargeType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargePromoTypeMap[] getChargePromoTypeMapsForType(String promoType,
			String chargeType, String accessedFrom) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargePromoTypeMapImpl.getChargePromoTypeMapsForType(conn,
					promoType, chargeType, accessedFrom);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargePromoTypeMap[] getChargePromoTypeMapsForLevelAndType(
			String accessedFrom, int level, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargePromoTypeMapImpl
					.getChargePromoTypeMapsForLevelAndType(conn, accessedFrom,
							level, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargeClassMap[] getChargeClassMaps() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargeClassMapImpl.getChargeClassMaps(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargeClassMap[] getChargeClassMapsForType(String type,
			String chargeType, String accessedFrom) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			ChargePromoTypeMap[] chargePromoTypeMaps = getChargePromoTypeMapsForType(
					type, chargeType, accessedFrom);
			if (chargePromoTypeMaps == null)
				return null;
			return ChargeClassMapImpl.getChargeClassMapsForType(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargeClassMap[] getChargeClassMapsForFinalClassType(
			String finalClassType, String accessedMode) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ChargeClassMapImpl.getChargeClassMapsForFinalClassType(conn,
					finalClassType, accessedMode);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedSchedule insertFeedSchedule(int feedID, String type,
			String name, String subKeyword, Date startTime, Date endTime,
			String classType, String smsFeedOnSuccess, String smsFeedOnFailure,
			String packType, int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedScheduleImpl.insert(conn, feedID, type, name,
					subKeyword, startTime, endTime, classType,
					smsFeedOnSuccess, smsFeedOnFailure, packType, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedSchedule getFeedSchedule(String type, String subKeyword) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedScheduleImpl.getFeedSchedule(conn, type, subKeyword);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedSchedule[] getFeedSchedules(String type, String subKeyword,
			int period) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedScheduleImpl.getFeedSchedules(conn, type, subKeyword,
					period);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedSchedule[] getActiveFeedSchedules(String type,
			String subKeyword, int period) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return FeedScheduleImpl.getActiveFeedSchedules(conn, type,
					subKeyword, period);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeFeedSchedule(String type, String name,
			String subKeyword, Date startTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return FeedScheduleImpl.remove(conn, type, name, subKeyword,
					startTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean setFeedScheduleEndTime(String type, Date endTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			FeedScheduleImpl.setEndTime(conn, type, endTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public ViralSMSTable insertViralSMSTableMap(String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, HashMap extraInfoMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			String finalExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfoMap);

			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter("VIRAL", "VIRAL_SMS_TYPE_FOR_SMS_FLOW", null);
			if (viralSmsTypeListForNewTable != null) {

				if (type != null && viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.insert(conn, subID(subscriberID),
							sentTime, type, subID(callerID), clipID, count,
							selectedBy, setTime, finalExtraInfo);
				}
			}

			return ViralSMSTableImpl.insert(conn, subID(subscriberID),
					sentTime, type, subID(callerID), clipID, count, selectedBy,
					setTime, finalExtraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable insertViralSMSTableMap(String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, HashMap extraInfoMap,
			String circleID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			String finalExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfoMap);

			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter("VIRAL", "VIRAL_SMS_TYPE_FOR_SMS_FLOW", null);
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.insert(conn, subID(subscriberID),
							sentTime, type, subID(callerID), clipID, count,
							selectedBy, setTime, finalExtraInfo, circleID);
				}
			}

			return ViralSMSTableImpl.insert(conn, subID(subscriberID),
					sentTime, type, subID(callerID), clipID, count, selectedBy,
					setTime, finalExtraInfo, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PendingConfirmationsReminderTableImpl insertPendingConfirmationRemainder(
			String subscriberId, int remaindersLeft, Date lastRemainderSent,
			Date smsReceivedTime, String remainderText, String sender,
			long smsId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PendingConfirmationsReminderTableImpl.insert(conn,
					subID(subscriberId), remaindersLeft, lastRemainderSent,
					smsReceivedTime, remainderText, sender, smsId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int updatePendingConfirmationReminder(String subscriberId,
			int remaindersLeft, Date lastReminderSent) {
		int updateCount = 0;
		Connection conn = getConnection();
		if (conn != null) {
			try {
				updateCount = PendingConfirmationsReminderTableImpl.update(
						conn, subscriberId, remaindersLeft, lastReminderSent);
			} catch (Throwable e) {
				logger.error("Unable to update. Exception: " + e.getMessage(),
						e);
			} finally {
				releaseConnection(conn);
			}
		}
		return updateCount;
	}

	public PendingConfirmationsReminderTableImpl[] getPendingConfirmationReminders(
			String delayInSentTime, String limitFrom, String limitTo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PendingConfirmationsReminderTableImpl.get(conn,
					delayInSentTime, limitFrom, limitTo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int deletePendingConfirmationRemainder(int limit) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		try {
			return PendingConfirmationsReminderTableImpl.delete(conn, limit);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public FileDetailsImpl getFileDetailsImpl(String name) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			logger.info("Fetching file details. file name: " + name);
			return FileDetailsImpl.retrieve(conn, name);
		} catch (Throwable e) {
			logger.error(
					"Unable to fetch file: " + name + ", Exception: "
							+ e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean insertFileDetailsImpl(String name) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			logger.info("Inserting file name: " + name);
			return FileDetailsImpl.insert(conn, name);
		} catch (Throwable e) {
			logger.error("Unable to insert details of file: " + name
					+ ", Exception: " + e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ViralSMSTable insertViralSMSTable(String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.insert(conn, subID(subscriberID),
							sentTime, type, subID(callerID), clipID, count,
							selectedBy, setTime, extraInfo);
				}
			}
			return ViralSMSTableImpl.insert(conn, subID(subscriberID),
					sentTime, type, subID(callerID), clipID, count, selectedBy,
					setTime, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateViralPromotion1(String subscriberID, String callerID,
			Date sentTime, String oldType, String newType, Date setTime,
			String selectedBy, String extraInfo, String clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					return ViralSMSNewImpl.updateViralPromotion1(conn,
							subID(subscriberID), subID(callerID), null,
							sentTime, oldType, newType, setTime, selectedBy,
							extraInfo, clipID);
				}
			}

			return ViralSMSTableImpl.updateViralPromotion1(conn,
					subID(subscriberID), subID(callerID), null, sentTime,
					oldType, newType, setTime, selectedBy, extraInfo, clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateCircleIdForType(String copyType) {
		boolean updated = false;
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && copyType != null) {
				if (viralSmsTypeListForNewTable.contains(copyType)) {
					return ViralSMSNewImpl
							.updateCircleIdForType(conn, copyType);
				}
			}
			return ViralSMSTableImpl.updateCircleIdForType(conn, copyType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateViralSMSTable(String subscriberID, Date sentTime,
			String type, String callerID, String clipID, int count,
			String selectedBy, Date setTime, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.update(conn, subID(subscriberID),
							sentTime, type, subID(callerID), clipID, count,
							selectedBy, setTime, extraInfo);
				}
			}

			return ViralSMSTableImpl.update(conn, subID(subscriberID),
					sentTime, type, subID(callerID), clipID, count, selectedBy,
					setTime, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ViralSMSTable getViralSMS(long smsID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null) {
				return ViralSMSNewImpl.getViralSMS(conn, smsID);
			}
			return ViralSMSTableImpl.getViralSMS(conn, smsID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMS(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null) {
				return ViralSMSNewImpl.getViralSMS(conn, subID(subscriberID));
			}
			return ViralSMSTableImpl.getViralSMS(conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getGiftInboxToBeCleared(float time, String smsType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && smsType != null) {
				if (viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl.getGiftInboxToBeCleared(conn, time,
							smsType);
				}
			}
			return ViralSMSTableImpl.getGiftInboxToBeCleared(conn, time,
					smsType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypeForCaller(String callerID,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeForCaller(conn,
							subID(callerID), type);
				}
			}
			return ViralSMSTableImpl.getViralSMSByTypeForCaller(conn,
					subID(callerID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByCaller(String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null) {
				return ViralSMSNewImpl.getViralSMSByCaller(conn,
						subID(callerID));
			}
			return ViralSMSTableImpl.getViralSMSByCaller(conn, subID(callerID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypesForSubscriber(String subId,
			String[] smsTypes) {
		return getViralSMSByTypesForSubscriber(subId, smsTypes, null);
	}

	public ViralSMSTable[] getViralSMSByTypesForSubscriber(String subId,
			String[] smsTypes, String mode) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null) {
				boolean queryNewTable = false;
				for (String smsType : smsTypes) {
					if (viralSmsTypeListForNewTable.contains(smsType)) {
						queryNewTable = true;
					}
				}
				if (queryNewTable) {
					if (mode == null) {
						return ViralSMSNewImpl
								.getViralSMSesByTypesForSubscriber(conn,
										subID(subId), smsTypes);
					} else {
						return ViralSMSNewImpl
								.getViralSMSesByTypesForSubscriber(conn,
										subID(subId), smsTypes, mode);
					}
				}
			}

			if (mode == null) {
				return ViralSMSTableImpl.getViralSMSesByTypesForSubscriber(
						conn, subID(subId), smsTypes);
			} else {
				return ViralSMSTableImpl.getViralSMSesByTypesForSubscriber(
						conn, subID(subId), smsTypes, mode);
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable getViralSMSByType(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByType(conn,
							subID(subscriberID), type);
				}
			}
			return ViralSMSTableImpl.getViralSMSByType(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeViralSMSesByTypeForCopyConsent(String subscriberID,
			String[] type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ViralSMSNewImpl.removeViralSMSesByType(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ViralSMSTable[] getViralSMSesByTypesForCopyConsent(
			String subscriberID, String[] type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ViralSMSNewImpl.getViralSMSesByTypes(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSesByTypes(String subscriberID,
			String[] type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSesByTypes(conn,
							subID(subscriberID), type);
				}
			}
			return ViralSMSTableImpl.getViralSMSesByTypes(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSesByType(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSesByType(conn,
							subID(subscriberID), type);
				}
			}
			return ViralSMSTableImpl.getViralSMSesByType(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSes(String subscriberID, String callerID,
			String type, String clipID, Date sentTime) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSes(conn,
							subID(subscriberID), callerID, type, clipID,
							sentTime);
				}
			}

			return ViralSMSTableImpl.getViralSMSes(conn, subID(subscriberID),
					callerID, type, clipID, sentTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int removeOldViralSMS(String type, float duration) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.removeOldViralSMS(conn, type,
							duration, false);
				}
			}
			return ViralSMSTableImpl.removeOldViralSMS(conn, type, duration,
					false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public int removeOldViralSMS(String type, float duration,
			boolean isDurationInHours) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.removeOldViralSMS(conn, type,
							duration, isDurationInHours);
				}
			}
			return ViralSMSTableImpl.removeOldViralSMS(conn, type, duration,
					isDurationInHours);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public boolean removeViralSMS(String subscriberID, String type) {
		return removeViralSMS(subscriberID, type, null);
	}

	public boolean removeViralSMS(String subscriberID, String type,
			Date sentTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.remove(conn, subID(subscriberID),
							type, sentTime);
				}
			}
			return ViralSMSTableImpl.remove(conn, subID(subscriberID), type,
					sentTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeViralSMSOfCaller(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.removeViralSMSOfCaller(conn,
							subID(subscriberID), type);
				}
			}
			return ViralSMSTableImpl.removeViralSMSOfCaller(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeCopyPendingViralSMSOfCaller(String subscriberID,
			String smsType, int time) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && smsType != null) {
				if (viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl.removeCopyPendingViralSMSOfCaller(
							conn, subID(subscriberID), smsType, time);
				}
			}
			return ViralSMSTableImpl.removeCopyPendingViralSMSOfCaller(conn,
					subID(subscriberID), smsType, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateViralSMSTypeOfCaller(String subscriberID,
			String oldType, String newType, int time) {
		return updateViralSMSTypeOfCaller(subscriberID, oldType, newType, time,
				null);
	}

	public boolean updateViralSMSTypeOfCaller(String subscriberID,
			String oldType, String newType, int time,
			HashMap<String, String> extrainfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					return ViralSMSNewImpl.updateLatestViralSMSTypeOfCaller(
							conn, subID(subscriberID), oldType, newType, time,
							extrainfo);
				}
			}
			return ViralSMSTableImpl.updateLatestViralSMSTypeOfCaller(conn,
					subID(subscriberID), oldType, newType, time, extrainfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void setSearchCount(String subscriberID, String type, int count) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			if (viralSmsTypeListForNewTable != null && type != null
					&& viralSmsTypeListForNewTable.contains(type)) {
				ViralSMSNewImpl.setSearchCount(conn, subID(subscriberID), type,
						count);
			} else {
				ViralSMSTableImpl.setSearchCount(conn, subID(subscriberID),
						type, count);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public void setSearchCountCopy(String subscriberID, String type, int count,
			Date sent, String caller) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			logger.info(" In setSearchCountCopy type:" + type);
			if (viralSmsTypeListForNewTable != null && type != null
					&& viralSmsTypeListForNewTable.contains(type)) {
				ViralSMSNewImpl.setSearchCountCopy(conn, subID(subscriberID),
						type, count, sent, caller);
			} else {
				ViralSMSTableImpl.setSearchCountCopy(conn, subID(subscriberID),
						type, count, sent, caller);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public boolean updateViralSearchCount(String subscriberID, String callerID,
			String type, Date sentTime, int count, String clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.updateViralSearchCount(conn,
							subID(subscriberID), callerID, type, sentTime,
							count, clipID);
				}
			}
			return ViralSMSTableImpl.updateViralSearchCount(conn,
					subID(subscriberID), callerID, type, sentTime, count,
					clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void setSearchCountRTCopy(String subscriberID, String type,
			int count, Date sent, String caller) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					ViralSMSNewImpl.setSearchCountCopy(conn,
							subID(subscriberID), type, count, sent, caller);
					return;
				}
			}
			ViralSMSTableImpl.setSearchCountCopy(conn, subID(subscriberID),
					type, count, sent, caller);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public ViralSMSTable[] getViralSMSByType(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByType(conn, type);
				}
			}
			return ViralSMSTableImpl.getViralSMSByType(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypeAndCircle(String type, int count,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeAndCircle(conn,
							type, count, circleId);
				}
			}
			return ViralSMSTableImpl.getViralSMSByTypeAndCircle(conn, type,
					count, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String oldType, String newType, Date setTime,
			String selectedBy, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					return ViralSMSNewImpl.updateViralPromotion(conn,
							subID(subscriberID), subID(callerID), null,
							sentTime, oldType, newType, setTime, selectedBy,
							extraInfo, false);
				}
			}
			return ViralSMSTableImpl.updateViralPromotion(conn,
					subID(subscriberID), subID(callerID), null, sentTime,
					oldType, newType, setTime, selectedBy, extraInfo, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String oldType, String newType, Date setTime,
			String selectedBy, String extraInfo, String clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					return ViralSMSNewImpl.updateViralPromotion(conn,
							subID(subscriberID), subID(callerID), null,
							sentTime, oldType, newType, setTime, selectedBy,
							extraInfo, clipID);
				}
			}
			return ViralSMSTableImpl.updateViralPromotion(conn,
					subID(subscriberID), subID(callerID), null, sentTime,
					oldType, newType, setTime, selectedBy, extraInfo, clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateViralPromotion(String subscriberID, String callerID,
			String newCallerID, Date sentTime, String oldType, String newType,
			Date setTime, String selectedBy, String extraInfo,
			boolean updateSmsid) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					return ViralSMSNewImpl.updateViralPromotion(conn,
							subID(subscriberID), subID(callerID),
							subID(newCallerID), sentTime, oldType, newType,
							setTime, selectedBy, extraInfo, updateSmsid);
				}
			}
			return ViralSMSTableImpl.updateViralPromotion(conn,
					subID(subscriberID), subID(callerID), subID(newCallerID),
					sentTime, oldType, newType, setTime, selectedBy, extraInfo,
					updateSmsid);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void updateCopyViralPromotion(String subscriberID, String callerID,
			Date sentTime, String newType, Date setTime, String selectedBy,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			if (viralSmsTypeListForNewTable != null && newType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)) {
					ViralSMSNewImpl.updateCopyViralPromotion(conn,
							subID(subscriberID), subID(callerID), sentTime,
							newType, setTime, selectedBy, extraInfo);
					return;
				}
			}
			ViralSMSTableImpl.updateCopyViralPromotion(conn,
					subID(subscriberID), subID(callerID), sentTime, newType,
					setTime, selectedBy, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String oldType, String newType, String clipId,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& oldType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(oldType)) {
					ViralSMSNewImpl.updateViralPromotion(conn,
							subID(subscriberID), subID(callerID), sentTime,
							oldType, newType, clipId, extraInfo);
					return;
				}
			}
			ViralSMSTableImpl.updateViralPromotion(conn, subID(subscriberID),
					subID(callerID), sentTime, oldType, newType, clipId,
					extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public boolean updateViralData(long smsID, String smsType, String circleID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && smsType != null) {
				if (viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl.update(conn, smsID, smsType,
							circleID);
				}
			}
			return ViralSMSTableImpl.update(conn, smsID, smsType, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public HashMap<String, Integer> getCountForViralSmsTypes(String[] smsTypes) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			List<String> viralSMSTypeList = Arrays.asList(smsTypes);
			if (isViralSmsTypeListForNewTable(viralSMSTypeList)) {
				return ViralSMSNewImpl.getCountForSmsTypes(conn, smsTypes);
			}
			return ViralSMSTableImpl.getCountForSmsTypes(conn, smsTypes);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int getCountForViralSmsType(String smsType, int waitTime) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;
		try {
			if (viralSmsTypeListForNewTable != null && smsType != null) {
				if (viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl.getCountForSmsType(conn, smsType,
							waitTime);
				}
			}
			return ViralSMSTableImpl
					.getCountForSmsType(conn, smsType, waitTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public ViralSMSTable getViralPromotion(String subscriberID,
			String callerID, Date sentTime, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), subID(callerID), sentTime,
							type);
				}
			}
			return ViralSMSTableImpl.getViralPromotion(conn,
					subID(subscriberID), subID(callerID), sentTime, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable getViralPromotion(String subscriberID,
			String callerID, Date sentTime, String type, String clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), subID(callerID), sentTime,
							type, clipID);
				}
			}
			return ViralSMSTableImpl.getViralPromotion(conn,
					subID(subscriberID), subID(callerID), sentTime, type,
					clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeViralPromotion(String subscriberID, String callerID,
			Date sentTime, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.removeViralPromotion(conn,
							subID(subscriberID), callerID, sentTime, type);
				}
			}
			return ViralSMSTableImpl.removeViralPromotion(conn,
					subID(subscriberID), callerID, sentTime, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteViralPromotion(String subscriberID, String callerID,
			String type, Date sentTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		List<String> typeList = new ArrayList<String>();
		StringTokenizer stk = new StringTokenizer(type, ",");
		while (stk.hasMoreTokens())
			typeList.add(stk.nextToken());

		try {
			if (viralSmsTypeListForNewTable != null) {
				boolean queryNewTable = false;
				for (String smsType : typeList) {
					if (smsType != null
							&& viralSmsTypeListForNewTable.contains(smsType)) {
						queryNewTable = true;
					}
				}
				if (queryNewTable) {
					return ViralSMSNewImpl.deleteViralPromotion(conn,
							subscriberID, callerID, typeList, sentTime);
				}
			}
			return ViralSMSTableImpl.deleteViralPromotion(conn,
					subID(subscriberID), callerID, typeList, sentTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteViralPromotionBySMSID(long smsID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (viralSmsTypeListForNewTable != null) {
				return ViralSMSNewImpl.deleteViralPromotionBySMSID(conn, smsID);
			}

			return ViralSMSTableImpl.deleteViralPromotionBySMSID(conn, smsID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteViralPromotionBySMSID(long smsID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.deleteViralPromotionBySMSID(conn,
							smsID);
				}
			}
			return ViralSMSTableImpl.deleteViralPromotionBySMSID(conn, smsID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeCopyViralPromotion(String subscriberID,
			String callerID, Date sentTime) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			List<String> smsTypeList = Arrays
					.asList("COPY,COPYSTAR,COPYCONFIRM,COPYCONFIRMED"
							.split(","));
			if (isViralSmsTypeListForNewTable(smsTypeList)) {
				return ViralSMSNewImpl.removeCopyViralPromotion(conn,
						subID(subscriberID), callerID, sentTime);
			}
			return ViralSMSTableImpl.removeCopyViralPromotion(conn,
					subID(subscriberID), callerID, sentTime);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ViralBlackListTable insertViralBlackList(String subscriberID,
			Date startTime, Date endTime, String subType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ViralBlackListTableImpl.insert(conn, subID(subscriberID),
					startTime, endTime, subType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateViralBlackList(String subscriberID, Date startTime,
			Date endTime, String subType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ViralBlackListTableImpl.update(conn, subID(subscriberID),
					startTime, endTime, subType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean isViralBlackListSub(String subscriberID) {
		ViralBlackListTable viralBlackListTable = getViralBlackList(
				subID(subscriberID), "VIRAL");
		return (viralBlackListTable != null);
	}

	public boolean isTotalBlackListSub(String subscriberID) {
		ViralBlackListTable viralBlackListTable = getViralBlackList(
				subID(subscriberID), "TOTAL");
		return (viralBlackListTable != null);
	}

	// RBT-12195 - User block - unblock feature.
	public boolean isBlackListSub(String subscriberID) {
		ViralBlackListTable viralBlackListTable = getViralBlackList(
				subID(subscriberID), "BLOCK");
		return (viralBlackListTable != null);
	}

	public ViralBlackListTable getViralBlackList(String subscriberID,
			String subType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ViralBlackListTableImpl.getViralBlackList(conn,
					subID(subscriberID), subType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralBlackListTable[] getAllViralBlackLists() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ViralBlackListTableImpl.getAllViralBlackLists(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeViralBlackList(String subscriberID, String subType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ViralBlackListTableImpl.remove(conn, subID(subscriberID),
					subType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean insertRemainderSmsData(long smsID, String smsType,
			String circleID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			if (viralSmsTypeListForNewTable != null && smsType != null) {
				if (viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl.update(conn, smsID, smsType,
							circleID);
				}
			}
			return ViralSMSTableImpl.update(conn, smsID, smsType, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Retailer insertRetailer(String subscriberID, String type, String name) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RetailerImpl.insert(conn, subID(subscriberID), type, name);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Retailer getRetailer(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RetailerImpl.getRetailer(conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Retailer[] getRetailers() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RetailerImpl.getRetailers(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removeRetailer(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return RetailerImpl.remove(conn, subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Hashtable getShuffleCategories() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Hashtable shuffleTable = new Hashtable();
		try {
			ArrayList allShuffles = getAllShuffleCategoryIDs();
			for (int i = 0; allShuffles != null && i < allShuffles.size(); i++) {
				Clips[] clips = _rcm.getActiveCategoryClips(
						((Integer) allShuffles.get(i)).intValue(), null, 'b');
				if (clips != null)
					shuffleTable.put(allShuffles.get(i), clips);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return shuffleTable;
	}

	public SubscriberStatus getSubscriberFile(String subscriberID,
			String callerID, String type, Hashtable shuffleTable,
			String feedFile) {
		return getSubscriberFile(subID(subscriberID), callerID, type,
				shuffleTable, feedFile, 0);
	}

	public SubscriberStatus getSubscriberFile(String subscriberID,
			String callerID, String type, Hashtable shuffleTable,
			String feedFile, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			boolean isPrepaid = false;
			if (type != null && !type.equalsIgnoreCase("ALL")) {
				Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
						subID(subscriberID));
				if (subscriber != null)
					isPrepaid = subscriber.prepaidYes();
			}
			subscriberStatus = SubscriberStatusImpl.getSubscriberFile(conn,
					subID(subscriberID), subID(callerID), isPrepaid, type,
					true, feedFile, rbtType);
			if (subscriberStatus != null) {
				if (shuffleTable != null
						&& shuffleTable.containsKey(new Integer(
								subscriberStatus.categoryID()))) {
					Clips[] clips = (Clips[]) shuffleTable.get(new Integer(
							subscriberStatus.categoryID()));
					if (clips != null) {
						DateFormat timeFormat = new SimpleDateFormat("HH");
						Date date = new Date(System.currentTimeMillis());
						int currentTime = Integer.parseInt(timeFormat
								.format(date));
						int index = -1;
						for (int i = 0; i < clips.length; i++) {
							if (clips[i].playTime() != null)
								if (currentTime >= Integer.parseInt(clips[i]
										.playTime()))
									index = i;
						}
						if (index != -1)
							subscriberStatus.setSubscriberFile(clips[index]
									.wavFile());
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public SubscriberStatus getSubscriberFile(String subscriberID,
			String callerID, String type, boolean isMemCacheModel,
			Hashtable shuffleTable, String feedFile) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		try {
			boolean isPrepaid = false;
			if (type != null && !type.equalsIgnoreCase("ALL")) {
				Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
						subID(subscriberID));
				if (subscriber != null)
					isPrepaid = subscriber.prepaidYes();
			}
			subscriberStatus = SubscriberStatusImpl.getSubscriberFile(conn,
					subID(subscriberID), subID(callerID), isPrepaid, type,
					isMemCacheModel, feedFile, 0);
			if (subscriberStatus != null) {
				if (shuffleTable != null
						&& shuffleTable.containsKey(new Integer(
								subscriberStatus.categoryID()))) {
					Clips[] clips = (Clips[]) shuffleTable.get(new Integer(
							subscriberStatus.categoryID()));
					if (clips != null) {
						DateFormat timeFormat = new SimpleDateFormat("HH");
						Date date = new Date(System.currentTimeMillis());
						int currentTime = Integer.parseInt(timeFormat
								.format(date));
						int index = -1;
						for (int i = 0; i < clips.length; i++) {
							if (clips[i].playTime() != null)
								if (currentTime >= Integer.parseInt(clips[i]
										.playTime()))
									index = i;
						}
						if (index != -1)
							subscriberStatus.setSubscriberFile(clips[index]
									.wavFile());
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public void doPlayerHangup(String subscriberID,
			SubscriberStatus subscriberStatus, Hashtable shuffleTable) {
		doPlayerHangup(subID(subscriberID), subscriberStatus, shuffleTable,
				true);
	}

	public void doPlayerHangup(String subscriberID,
			SubscriberStatus subscriberStatus, Hashtable shuffleTable,
			boolean lastAccessUpdate) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			doPlayerHangup(conn, subID(subscriberID), subscriberStatus,
					shuffleTable, lastAccessUpdate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public void doPlayerHangup(Connection conn, String subscriberID,
			SubscriberStatus subscriberStatus, Hashtable shuffleTable,
			boolean doLastAccessUpdate) throws SQLException {
		try {
			if (subscriberStatus != null) {
				if (shuffleTable != null
						&& shuffleTable.containsKey(new Integer(
								subscriberStatus.categoryID()))) {
					Clips[] clips = (Clips[]) shuffleTable.get(new Integer(
							subscriberStatus.categoryID()));
					if (clips != null) {
						boolean found = false;
						for (int i = 0; i < clips.length; i++) {
							if ((clips[i].wavFile())
									.equalsIgnoreCase(subscriberStatus
											.subscriberFile())) {
								found = true;
								if (i == (clips.length - 1))
									i = 0;
								else
									i++;
								SubscriberStatusImpl.setSubscriberFile(conn,
										subscriberStatus.subID(),
										subscriberStatus.callerID(),
										subscriberStatus.setTime(),
										clips[i].wavFile(), 0);

								logger.info("RBT::next shuffle song has been set for "
										+ subscriberStatus.subID());
								break;
							}
						}
						if (!found) {
							SubscriberStatusImpl.setSubscriberFile(conn,
									subscriberStatus.subID(),
									subscriberStatus.callerID(),
									subscriberStatus.setTime(),
									clips[0].wavFile(), 0);
							logger.info("RBT::next shuffle song has been set for "
									+ subscriberStatus.subID());
						}
					}
				}
			}
			if (doLastAccessUpdate)
				SubscriberImpl.setAccessDate(conn, subID(subscriberID), null,
						true);
		} catch (SQLException se) {
			logger.error("", se);
			throw se;
		}
		return;
	}

	public Subscriber[] getActiveSubsToSendTNBSms(String classType,
			Date beginDate, Date lastDate, int days, int subscriptionPeriod) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getActiveSubsToSendTNBSms(conn, classType,
					beginDate, lastDate, days, subscriptionPeriod);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] getActiveTNBSubsToDeactivate(String classType,
			int subscriptionPeriod) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getActiveTNBSubsToDeactivate(conn, classType,
					subscriptionPeriod);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateTNBSubscribertoNormal(String subId, boolean useSM,
			int subscriptionPeriod) {
		boolean value = false;
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.updateTNBSubscribertoNormal(conn,
					subID(subId), useSM, subscriptionPeriod);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateTNBSubscriberIterId(long seqId, int iterId)
			throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");

		try {
			return TnbSubscriberImpl.updateIterId(conn, seqId, iterId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteTNBSubscriber(long seqId) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			return TnbSubscriberImpl.delete(conn, seqId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void deleteTNBSubscriber(String subID) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			TnbSubscriberImpl.deleteSubscriber(conn, subID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void deleteTrialSelection(String subID) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			TrialSelectionImpl.deleteSubscriber(conn, subID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public boolean allowLooping() {
		if (allowLooping == null) {
			Parameters parameters = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"ALLOW_LOOPING");
			if (parameters != null)
				allowLooping = parameters.getValue();
			else
				allowLooping = "false";
		}
		return allowLooping.equalsIgnoreCase("true");
	}

	public boolean isDefaultLoopOn() {
		if (defaultLoopOn == null) {
			Parameters parameters = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"DEFAULT_LOOP_ON");
			if (parameters != null)
				defaultLoopOn = parameters.getValue();
			else
				defaultLoopOn = "false";
		}
		return defaultLoopOn.equalsIgnoreCase("true");
	}

	// Added for TATA to allow specific class of users
	public String getAllowedSubscriberClass() {
		if (allowedSubClass == null) {
			allowedSubClass = "cmo,fwp,ws,walky10,fwt";
			Parameters param = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "ALLOWED_SUBSCRIBER_CLASSES");
			if (param != null)
				allowedSubClass = param.getValue();
		}
		return allowedSubClass;
	}

	// Added for TATA to read circle id from WDS
	public String getMappedCircleID(String wdsCircleID) {
		String retVal = "Default";
		if (_wdsCircleIDMap == null) {
			_wdsCircleIDMap = new HashMap();
			Parameters param = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "WDS_CIRCLE_MAPS");
			if (param != null) {
				StringTokenizer stk = new StringTokenizer(param.getValue(), ",");
				while (stk.hasMoreTokens()) {
					String token = stk.nextToken();
					int index = token.indexOf(":");

					String wdsCID = token;
					String omCID = token;
					if (index != -1) {
						wdsCID = token.substring(0, index);
						omCID = token.substring(index + 1);
					}
					_wdsCircleIDMap.put(wdsCID, omCID);
				}
			}
		}

		if (_wdsCircleIDMap.containsKey(wdsCircleID))
			retVal = (String) _wdsCircleIDMap.get(wdsCircleID);
		return retVal;
	}

	public String subID(String subscriberID) {
		if (subscriberID != null) {
			try {
				if (m_countryCodePrefix == null
						|| m_countryCodePrefix.trim().equals(""))
					m_countryCodePrefix = "91";
				if (m_countryCodePrefix != null) {
					String[] countryCodePrefixes = m_countryCodePrefix
							.split(",");
					for (String prefix : countryCodePrefixes) {
						if (subscriberID.startsWith("00"))
							subscriberID = subscriberID.substring(2);
						if (subscriberID.startsWith("+")
								|| subscriberID.startsWith("0")
								|| subscriberID.startsWith("-"))
							subscriberID = subscriberID.substring(1);
						if (subscriberID.startsWith(prefix)
								&& (subscriberID.length() >= (m_minPhoneNumberLen + prefix
										.length()))) {
							subscriberID = subscriberID.substring(prefix
									.length());
							break;
						}
					}
				}
			} finally {
				if (subscriberID.startsWith("00"))
					subscriberID = subscriberID.substring(2);
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("0")
						|| subscriberID.startsWith("-"))
					subscriberID = subscriberID.substring(1);
			}
			if (mobileNumLengthMigration && migratedNumberRange.length > 0
					&& isMigratedNumber(subscriberID))
				subscriberID = getMigratedNumber(subscriberID);

			// Area Code to be in the form of 11:9,12:8,13:10 etc. i.e.
			// areaCode:length
			Parameters areaParameter = CacheManagerUtil
					.getParametersCacheManager().getParameter(
							iRBTConstant.COMMON,
							"AREA_CODE_FOR_PHONE_NUMBER_LENGTH", null);
			if (areaParameter != null && subscriberID != null) {
				String paramVal = areaParameter.getValue();
				if (paramVal != null) {
					String token[] = paramVal.split(",");
					for (int i = 0; i < token.length; i++) {
						String areaCodeToken[] = token[i].split(":");
						if (subscriberID.startsWith(areaCodeToken[0])) {
							try {
								int areaCodeLength = Integer
										.parseInt(areaCodeToken[1]);
								if (subscriberID.length() == (areaCodeLength + areaCodeToken[0]
										.length()))
									subscriberID = subscriberID
											.substring(areaCodeToken[0]
													.length());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		return subscriberID;
	}

	private String getMigratedNumber(String subscriberID) {
		for (int i = 1; i < subscriberID.length(); i++) {
			String prefix = subscriberID.substring(0, i);
			if (migratedPrefixMap.containsKey(prefix))
				return migratedPrefixMap.get(prefix)
						+ subscriberID.substring(i);
		}
		return subscriberID;
	}

	private boolean isMigratedNumber(String subscriberID) {
		try {
			long subId = Long.parseLong(subscriberID);
			for (int i = 0; i < migratedNumberRange.length; i++) {
				long num1 = migratedNumberRange[i][0];
				long num2 = migratedNumberRange[i][1];
				if (num1 <= subId && subId <= num2)
					return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/* all methods related to bulk promo SMS */
	public BulkPromoSMS addBulkPromoSMS(String bulkPromoID, int smsDay,
			String smsText) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.insert(conn, bulkPromoID, smsDay, smsText);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS getBulkPromoSMSForDaemon(String bulkPromoID, int smsDay) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.getBulkPromoSMSForDaemon(conn, bulkPromoID,
					smsDay);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS[] getBulkPromoSmses() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.getBulkPromoSMSes(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS[] getBulkPromoSmses(String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return BulkPromoSMSImpl.getBulkPromoSMSes(conn, promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean changeSmsText(String promoId, String smsDate,
			String smsText, String smsSent) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return BulkPromoSMSImpl.update(conn, promoId, smsDate, smsText,
					smsSent);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public BulkPromoSMS[] getDistinctPromoIds() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return BulkPromoSMSImpl.getDistinctPromoIds(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS[] getAllPromoIDSMSes(String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.getAllPromoIDSMSes(conn, promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RBTLogin getLogin(String user, String passwd) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RBTLoginImpl.getUser(conn, user.trim(), passwd.trim());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PromoMaster[] getPromoForCode(String code) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PromoMasterImpl.getPromoForCode(conn, code.trim());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PromoMaster getPromoForTypeAndCode(String type, String code) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PromoMasterImpl.getPromoForTypeAndCode(conn, type.trim(),
					code.trim());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo getBulkPromo(String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getBulkPromo(conn, promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean expireAllSubscriberDownload(String subscriberId,
			String deactBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireAllSubscriberDownload(conn,
					subID(subscriberId), deactBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireAllSubscriberDownloadBaseDct(String subscriberId,
			String deactBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireAllSubscriberDownloadBaseDct(
					conn, subID(subscriberId), deactBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberDownloads getActiveSubscriberDownload(String subscriberId,
			String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownload(conn,
					subID(subscriberId), promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] getActiveSubscriberDownloads(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownloads(conn,
					subID(subscriberId));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] getActiveSubscriberDownloads(
			String subscriberId, String protocolNo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownloads(conn,
					subID(subscriberId), protocolNo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	public String addSubscriberDownload(String subscriberId,
			String subscriberWavFile, int categoryID, Date endDate,
			boolean downloaded, int categoryType, String classType,
			String selBy, String selectionInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads results = null;
		try {
			subscriberId = subID(subscriberId);
			String nextClass = getNextChargeClass(subscriberId);
			if (nextClass == null || nextClass.equalsIgnoreCase("DEFAULT"))
				nextClass = classType;
			if (nextClass == null)
				nextClass = "DEFAULT";

			SubscriberDownloads downLoad = getSubscriberDownload(
					subID(subscriberId), subscriberWavFile, categoryID,
					categoryType);
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				if (downStat == STATE_DOWNLOAD_DEACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED)
					return "FAILURE:DOWNLOAD_DEACT_PENDING";
				if (downStat == STATE_DOWNLOAD_ACT_ERROR
						|| downStat == STATE_DOWNLOAD_DEACT_ERROR)
					return "FAILURE:DOWNLOAD_ERROR";
				if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					deleteSubscriberDownload(subID(subscriberId),
							subscriberWavFile, categoryID, categoryType);
					SubscriberDownloadsImpl.insert(conn, subID(subscriberId),
							subscriberWavFile, categoryID, endDate, downloaded,
							categoryType, nextClass, selBy, selectionInfo);
					if (!downloaded)
						SubscriberImpl.setSelectionCount(conn, subscriberId);
					return "SUCCESS:DOWNLOAD_ADDED";
				}
				if (downStat == STATE_DOWNLOAD_ACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_ACTIVATED)
					return "SUCCESS:DOWNLOAD_PENDING_ACTIAVTION";
				if (downStat == STATE_DOWNLOAD_DEACTIVATED) {
					SubscriberDownloadsImpl.reactivate(conn,
							subID(subscriberId), subscriberWavFile, categoryID,
							endDate, categoryType, downloaded, nextClass,
							selBy, selectionInfo);
					if (!downloaded)
						SubscriberImpl.setSelectionCount(conn, subscriberId);
					return "SUCCESS:DOWNLOAD_REACTIVATED";
				}
			} else {
				SubscriberDownloadsImpl.insert(conn, subID(subscriberId),
						subscriberWavFile, categoryID, endDate, downloaded,
						categoryType, nextClass, selBy, selectionInfo);
				if (!downloaded)
					SubscriberImpl.setSelectionCount(conn, subscriberId);

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
	}

	protected String isContentExpired(ClipMinimal clip, Categories categories) {
		if (categories != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(categories.type())) {
			if (categories.endTime().getTime() < System.currentTimeMillis())
				return DOWNLOAD_FAILED_CATEGORY_EXPIRED;
		} else if (clip != null) {
			/* Allow selection from expired UGC content */
			try {
				boolean allowUGCContentRecentlyCreated = RBTParametersUtils
						.getParamAsBoolean("COMMON",
								"ALLOW_UGC_CONTENT_RECENTLY_CREATED", "FALSE");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				if (clip != null
						&& clip.getGrammar().equalsIgnoreCase("UGC")
						&& allowUGCContentRecentlyCreated
						&& clip.getEndTime().getTime() == sdf.parse(
								"2004/01/01").getTime()) {
					return null;
				}
			} catch (Exception e) {
			}
			if (clip.getEndTime().getTime() < System.currentTimeMillis())
				return DOWNLOAD_FAILED_CLIP_EXPIRED;
		}
		return null;
	}

	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams,
			String downloadStatus) {

		return addSubscriberDownloadRW(subscriberId, subscriberWavFile,
				categories, endDate, isSubActive, classType, selBy,
				selectionInfo, extraInfo, incrSelCount, useUIChargeClass,
				isSmClientModel, responseParams, null, downloadStatus);

	}

	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams,
			int status, String callerId, String downloadStatus) {

		return addSubscriberDownloadRW(subscriberId, subscriberWavFile,
				categories, endDate, isSubActive, classType, selBy,
				selectionInfo, extraInfo, incrSelCount, useUIChargeClass,
				isSmClientModel, responseParams, null, status, callerId,
				downloadStatus);

	}

	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, String downloadStatus) {

		return addSubscriberDownloadRW(subscriberId, subscriberWavFile,
				categories, endDate, isSubActive, classType, selBy,
				selectionInfo, extraInfo, incrSelCount, useUIChargeClass,
				isSmClientModel, responseParams, null, -1, null, downloadStatus);
	}

	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, int status, String callerId,
			String downloadStatus) {
		Connection conn = getConnection();
		boolean incrSelCountParamForGift = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.COMMON,
						"INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");

		if (conn == null)
			return null;

		try {
			SubscriberDownloads results = null;
			ClipMinimal clip = getClipRBT(subscriberWavFile);
			int categoryID = categories.id();
			int categoryType = categories.type();
			if (endDate == null)
				endDate = m_endDate;
			subscriberId = subID(subscriberId);
			String nextClass = null;

			boolean isPackSel = false;
			String packCosID = null;
			String[] chargeClassStr;
			if (useUIChargeClass)
				nextClass = classType;
			else {
				if (com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(categoryType)) {
					chargeClassStr = getChargeClassForShuffleCatgory(
							subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel,
							packCosID, selBy, extraInfo, nextClass, classType);
				} else {
					chargeClassStr = getChargeClassForNonShuffleCatgory(
							subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel,
							packCosID, selBy, extraInfo, nextClass, classType);
				}
				if (chargeClassStr != null) {
					if (chargeClassStr.length > 4) {
						nextClass = chargeClassStr[2];
						if (nextClass != null
								&& (nextClass
										.equalsIgnoreCase(SELECTION_FAILED_INTERNAL_ERROR) || nextClass
										.equalsIgnoreCase("FAILURE:TECHNICAL_FAULT"))) {
							return nextClass;
						}
						incrSelCount = (chargeClassStr[0]
								.equalsIgnoreCase("true") ? true : false);
						isPackSel = (chargeClassStr[1].equalsIgnoreCase("true") ? true
								: false);
						classType = chargeClassStr[3];
						packCosID = chargeClassStr[4];
					}

				}

				if (m_overrideChargeClasses != null
						&& classType != null
						&& m_overrideChargeClasses.contains(classType
								.toLowerCase()))
					nextClass = classType;
			}

			SubscriberDownloads downLoad = getSubscriberDownload(
					subID(subscriberId), subscriberWavFile, categoryID,
					categoryType);

			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad
							.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				}
			}
			String campaignCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.CAMPAIGN_CODE) : null;
			String treatmentCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.TREATMENT_CODE) : null;
			String offerCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.OFFER_CODE) : null;

			String downloadExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfo);

			String refId = null;
			if (consentSubscriber != null) {
				refId = consentSubscriber.refID();
			}
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED
						|| downStat == STATE_DOWNLOAD_CHANGE)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_DEACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED)
					return "FAILURE:DOWNLOAD_DEACT_PENDING";
				else if (downStat == STATE_DOWNLOAD_ACT_ERROR
						|| downStat == STATE_DOWNLOAD_DEACT_ERROR)
					return "FAILURE:DOWNLOAD_ERROR";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId),
							subscriberWavFile, categoryID, categoryType);
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
							subscriberId, subscriberWavFile, endDate,
							isSubActive, selBy, selectionInfo, isSmClientModel,
							conn, categoryID, categoryType, nextClass,
							extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {
						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY",
								"SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());
					

					}

					downloadExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);

					if (null == subscriberDownloads) {
						if (null != extraInfo) {
							extraInfo.remove("CALLER_ID");
							extraInfo.remove("STATUS");
							extraInfo.remove("FROM_TIME");
							extraInfo.remove("TO_TIME");
							extraInfo.remove("INTERVAL");
							extraInfo.remove("LOOP_STATUS");
						}

						subscriberDownloads = SubscriberDownloadsImpl.insertRW(
								conn, subID(subscriberId), subscriberWavFile,
								categoryID, endDate, isSubActive, categoryType,
								nextClass, selBy, selectionInfo,
								downloadExtraInfo, isSmClientModel,
								downloadStatus, null);

						// RBT2.0 changes
						if (subscriberDownloads != null
								&& subscriberDownloads.downloadStatus() == 'y') {
							Subscriber subscriber = getSubscriber(subID(subscriberId));

							TPTransactionLogger.getTPTransactionLoggerObject(
									"download").writeTPTransLog(
									subscriber.circleID(), subID(subscriberId),
									"NA", -1, -1, -1, "NA", categoryType, -1,
									subscriberWavFile, categoryID, -1,
									nextClass, subscriberDownloads.startTime(),
									subscriberDownloads.endTime(), "NA");
						}
					}

					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID",
								subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
							&& incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn,
								subscriberId, packCosID);
					else if (selBy != null
							&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
							&& incrSelCount)
						SubscriberImpl.setSelectionCount(conn,
								subID(subscriberId));

					return "SUCCESS:DOWNLOAD_ADDED";
				} else if (downStat == STATE_DOWNLOAD_ACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_ACTIVATED
						|| downStat == STATE_DOWNLOAD_BASE_ACT_PENDING)
					return "SUCCESS:DOWNLOAD_PENDING_ACTIAVTION";
				else if (downStat == STATE_DOWNLOAD_SUSPENSION)
					return "FAILURE:DOWNLOAD_SUSPENDED";
				else if (downStat == STATE_DOWNLOAD_GRACE)
					return "SUCCESS:DOWNLOAD_GRACE";
				else if (downStat == STATE_DOWNLOAD_DEACTIVATED) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
							subscriberId, subscriberWavFile, endDate,
							isSubActive, selBy, selectionInfo, isSmClientModel,
							conn, categoryID, categoryType, nextClass,
							extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {

						// Add following fields to extraInfo

						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY",
								"SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());
						extraInfo.put("EVENTYPE", "1");
						extraInfo.put("CONSENTCLASSTYPE",
								subscriberDownloads.classType());
					}

					downloadExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);

					if (subscriberDownloads == null) {
						subscriberDownloads = SubscriberDownloadsImpl
								.reactivateRW(conn, subID(subscriberId),
										subscriberWavFile, categoryID, endDate,
										categoryType, isSubActive, nextClass,
										selBy, selectionInfo,
										downloadExtraInfo, isSmClientModel,
										downloadStatus);

						// RBT2.0 changes
						if (subscriberDownloads != null
								&& subscriberDownloads.downloadStatus() == 'y') {
							Subscriber subscriber = getSubscriber(subID(subscriberId));

							TPTransactionLogger.getTPTransactionLoggerObject(
									"download").writeTPTransLog(
									subscriber.circleID(), subID(subscriberId),
									"NA", -1, -1, -1, "NA", categoryType, -1,
									subscriberWavFile, categoryID, -1,
									nextClass, subscriberDownloads.startTime(),
									subscriberDownloads.endTime(), "NA");
						}

						if (subscriberDownloads != null && campaignCode != null
								&& treatmentCode != null && offerCode != null) {
							String msg = iRBTConstant.CAMPAIGN_CODE + "="
									+ campaignCode + ","
									+ iRBTConstant.TREATMENT_CODE + "="
									+ treatmentCode + ","
									+ iRBTConstant.OFFER_CODE + "=" + offerCode
									+ "," + iRBTConstant.RETRY_COUNT + "=0";
							RBTCallBackEvent.insert(subscriberId,
									subscriberDownloads.refID(), msg,
									RBTCallBackEvent.SM_CALLBACK_PENDING,
									RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
									clip.getClipId(), selBy);
						}

					}
					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID",
								subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
							&& incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn,
								subscriberId, packCosID);
					else if (selBy != null
							&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
							&& incrSelCount)
						SubscriberImpl.setSelectionCount(conn,
								subID(subscriberId));

					return "SUCCESS:DOWNLOAD_REACTIVATED";
				}
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null)
					return response;
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(downloadExtraInfo);

				SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
						subscriberId, subscriberWavFile, endDate, isSubActive,
						selBy, selectionInfo, isSmClientModel, conn,
						categoryID, categoryType, nextClass, extraInfo, refId,
						useUIChargeClass);

				if (null != subscriberDownloads) {
					extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY", "SUCCESS");
					extraInfo.put("CONSENTID", subscriberDownloads.refID());
					extraInfo.put("EVENTYPE", "1");
					extraInfo.put("CONSENTCLASSTYPE",
							subscriberDownloads.classType());
				}

				downloadExtraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);

				if (subscriberDownloads == null) {
					subscriberDownloads = SubscriberDownloadsImpl.insertRW(
							conn, subID(subscriberId), subscriberWavFile,
							categoryID, endDate, isSubActive, categoryType,
							nextClass, selBy, selectionInfo, downloadExtraInfo,
							isSmClientModel, downloadStatus, null);

					// RBT2.0 changes
					if (subscriberDownloads != null
							&& subscriberDownloads.downloadStatus() == 'y') {
						Subscriber subscriber = getSubscriber(subID(subscriberId));

						TPTransactionLogger.getTPTransactionLoggerObject(
								"download").writeTPTransLog(
								subscriber.circleID(), subID(subscriberId),
								"NA", -1, -1, -1, "NA", categoryType, -1,
								subscriberWavFile, categoryID, -1, nextClass,
								subscriberDownloads.startTime(),
								subscriberDownloads.endTime(), "NA");
					}

					logger.info("Campaign Code=" + campaignCode
							+ ",TreatmentCode=" + treatmentCode
							+ ",OfferCount=" + offerCode);
					if (subscriberDownloads != null && campaignCode != null
							&& treatmentCode != null && offerCode != null) {
						String msg = iRBTConstant.CAMPAIGN_CODE + "="
								+ campaignCode + ","
								+ iRBTConstant.TREATMENT_CODE + "="
								+ treatmentCode + "," + iRBTConstant.OFFER_CODE
								+ "=" + offerCode + ","
								+ iRBTConstant.RETRY_COUNT + "=0";
						RBTCallBackEvent.insert(subscriberId,
								subscriberDownloads.refID(), msg,
								RBTCallBackEvent.SM_CALLBACK_PENDING,
								RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								clip.getClipId(), selBy);
					}
				}
				if (subscriberDownloads == null)
					return "FAILURE:INSERTION_FAILED";

				if (subscriberDownloads != null && responseParams != null) {
					responseParams.put("REF_ID", subscriberDownloads.refID());
					responseParams.put("CLASS_TYPE", nextClass);
				}

				if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
						&& incrSelCount && isPackSel)
					ProvisioningRequestsDao.updateNumMaxSelections(conn,
							subscriberId, packCosID);
				else if (selBy != null
						&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
						&& incrSelCount)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberId));

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
	}

	public String addDownload(String subscriberId, String subscriberWavFile,
			int categoryID, int categoryType, String actBy,
			String activationInfo, boolean active) {
		com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = rbtCacheManager
				.getCategory(categoryID);
		Categories categoriesObj = CategoriesImpl.getCategory(category);
		return addSubscriberDownloadRW(subID(subscriberId), subscriberWavFile,
				categoriesObj, null, active, null, actBy, activationInfo, null,
				true, false, false, null, null);
	}

	// Idea-Combo DT New Model RBT-14087
	public boolean isSpecialSelection(String callerID, int status) {
		if( isSpecialCallerSelection( callerID)) {
			return true;
		}
		if (status != 1 && status != 90 && status != 99) {
			return true;
		}
		return false;
	}
	
	public boolean isSpecialCallerSelection(String callerID) {
		if ((callerID != null) && (!callerID.isEmpty())
				&& (!callerID.equalsIgnoreCase("all"))) {
			return true;
		}
		return false;

	}

	public String getNextChargeClass(Subscriber subscriber,
			boolean specialSelection, boolean inLoop,int selectionCount, String clipClassType, boolean isSpecialCallerSetting) {
		if ((!isSubActive(subscriber)) || (subscriber.cosID() == null)) {
			return null;
		}
		CosDetails cosObject = getCosForActiveSubscriber(null, subscriber);
		int selCount = subscriber.maxSelections();
		String chargeClass = getChargeClassForSpecificCosUser(cosObject,
				subscriber, inLoop, specialSelection, selectionCount, clipClassType, isSpecialCallerSetting);
		if (chargeClass != null) {
			return chargeClass;
		}
		return getChargeClassFromCos(cosObject, selCount);
	}

	public String getChargeClassForSpecificCosUser(CosDetails cosObject,
			Subscriber subscriber, boolean inLoop, boolean specialSelection,
			int selCount , String clipClassType, boolean isSpecialCallerSetting) {
		if (cosObject == null
				|| (chargeClassMapForAllSubInLoop.isEmpty()
						&& chargeClassMapForSpecialSub.isEmpty() && chargeClassMapForSpecialSubInLoop
							.isEmpty())) {
			return null;
		}
		
		boolean isComboDTUser = false ;
		String cosId = cosObject.getCosId();
		String cosFreeChargeClass =  RBTDBManager.getInstance().getChargeClassFromCos(cosObject,selCount);
		boolean isPremium = false ; 
		if(clipClassType != null && !clipClassType.isEmpty() && !clipClassType.equalsIgnoreCase("DEFAULT")){
			isPremium = true ;
		}
		if(chargeClassMapForAllSubInLoop.containsKey(cosId)
				|| chargeClassMapForSpecialSub.containsKey(cosId) || chargeClassMapForSpecialSubInLoop
				.containsKey(cosId)){
				if(isPremium && cosFreeChargeClass.equalsIgnoreCase("FREE") && (inLoop || isSpecialCallerSetting)){
					return cosFreeChargeClass;
				}else if(isPremium){
					return clipClassType; 
				}						
		}
		
		Boolean isUdsUser = Boolean.valueOf(false);
		String subscriberExtraInfo = null;
		String chargeClass = null;
		if (null != subscriber) {
			subscriberExtraInfo = subscriber.extraInfo();
		}
		Map<String, String> subExtraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriberExtraInfo);
		if ((subExtraInfoMap != null)
				&& (subExtraInfoMap.containsKey("UDS_OPTIN"))) {
			isUdsUser = Boolean.valueOf(((String) subExtraInfoMap
					.get("UDS_OPTIN")).equalsIgnoreCase("TRUE"));
		}
		if (!specialSelection) {
			if (selCount == 0) {
				return null;
			}
			if ((selCount > 0) && (isUdsUser || inLoop)) {
				chargeClass = chargeClassMapForAllSubInLoop.get(cosObject
						.getCosId());
			}
		} else {
			if ((selCount == 0) || ((selCount > 0) && (!isUdsUser && !inLoop))) {
				chargeClass = chargeClassMapForSpecialSub.get(cosObject
						.getCosId());
			} else if ((selCount > 0) && (isUdsUser || inLoop)) {
				chargeClass = chargeClassMapForSpecialSubInLoop.get(cosObject
						.getCosId());
			}
		}
		return (chargeClass == null || (chargeClass = chargeClass.trim())
				.isEmpty()) ? null : chargeClass.toUpperCase();
	}

	public String getCosChargeClass(
			Subscriber subscriber,
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category,
			Clip clip, CosDetails cos, WebServiceContext webServiceContext) {
		String classType = null;

		if (((category == null) || (!com.onmobile.apps.ringbacktones.webservice.common.Utility
				.isShuffleCategory(category.getCategoryTpe())))
				&& (!getInstance().isCosToBeIgnored(cos, category, clip))) {
			int selectionCount = 0;
			if (RBTDBManager.getInstance().isSubActive(subscriber)) {
				selectionCount = subscriber.maxSelections();
			}
			
			String clipClassType = "" ;
			if(clip != null ){
				clipClassType = clip.getClassType();
			}
			
			classType = getChargeforSpecificCosUser(subscriber, cos,
					webServiceContext , clipClassType);

			if (classType != null) {
				return classType;
			}

			classType = RBTDBManager.getInstance().getChargeClassFromCos(cos,
					selectionCount);
		}
		return classType;
	}

	private String getChargeforSpecificCosUser(Subscriber subscriber,
			CosDetails cos, WebServiceContext webServiceContext, String clipClassType) {
		if (webServiceContext == null
				|| cos == null
				|| (chargeClassMapForAllSubInLoop.isEmpty()
						&& chargeClassMapForSpecialSub.isEmpty() && chargeClassMapForSpecialSubInLoop
							.isEmpty())) {
			return null;
		}
		String subscriberID = webServiceContext
				.getString(WebServiceConstants.param_subscriberID);
		String callerID = webServiceContext
				.getString(WebServiceConstants.param_callerID);
		String selInterval = webServiceContext
				.getString(WebServiceConstants.param_interval);
		int fromTime = 0;
		int toTime = 2359;
		int fromHrs = 0;
		int toHrs = 23;
		int fromMinutes = 0;
		int toMinutes = 59;
		boolean inLoop = false;
		int status = 1;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date startTime = null;
		Date endTime = null;
		Map<String, String> whereClauseMap = new HashMap<String, String>();
		try {
			if ((webServiceContext != null)) {
				if (webServiceContext
						.containsKey(WebServiceConstants.param_status)) {
					status = Integer.parseInt(webServiceContext
							.getString(WebServiceConstants.param_status));
					whereClauseMap.put("STATUS", String.valueOf(status));
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_fromTime)) {
					fromHrs = Integer.parseInt(webServiceContext
							.getString(WebServiceConstants.param_fromTime));
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_toTime)) {
					toHrs = Integer.parseInt(webServiceContext
							.getString(WebServiceConstants.param_toTime));
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_toTimeMinutes)) {
					toMinutes = Integer
							.parseInt(webServiceContext
									.getString(WebServiceConstants.param_toTimeMinutes));
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_fromTimeMinutes)) {
					fromMinutes = Integer
							.parseInt(webServiceContext
									.getString(WebServiceConstants.param_fromTimeMinutes));
				}

				DecimalFormat decimalFormat = new DecimalFormat("00");
				fromTime = Integer.parseInt(fromHrs
						+ decimalFormat.format(fromMinutes));
				toTime = Integer.parseInt(toHrs
						+ decimalFormat.format(toMinutes));

				if ((webServiceContext
						.containsKey(WebServiceConstants.param_inLoop))
						&& (webServiceContext
								.getString(WebServiceConstants.param_inLoop)
								.equalsIgnoreCase("y"))) {
					inLoop = true;
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_selectionStartTime)) {
					String startTimeStr = webServiceContext
							.getString(WebServiceConstants.param_selectionStartTime);
					if (startTimeStr != null && startTimeStr.length() == 8)
						startTimeStr += "000000000";
					startTime = dateFormat.parse(startTimeStr);
				}
				if (webServiceContext
						.containsKey(WebServiceConstants.param_selectionEndTime)) {
					String endTimeStr = webServiceContext
							.getString(WebServiceConstants.param_selectionEndTime);
					if (endTimeStr != null && endTimeStr.length() == 8)
						endTimeStr += "235959000";
					endTime = dateFormat.parse(endTimeStr);
				}
				if ((fromTime >= 0) && (toTime < 2359)) {
					status = 80;
				}
				if ((startTime != null) && (endTime != null)) {
					status = 80;
				}
				if (selInterval != null && !selInterval.isEmpty()) {
					status = 80;
				}
			}
		} catch (ParseException e) {
			logger.debug("Execption occured in getting the task object values: "
					+ e.getMessage());
		}
		boolean isSpecialSelection = isSpecialSelection(callerID, status);
		whereClauseMap.put("CALLER_ID", ((callerID == null || callerID
				.equalsIgnoreCase(WebServiceConstants.ALL)) ? null : callerID));
		whereClauseMap.put("FROM_TIME", String.valueOf(fromTime));
		whereClauseMap.put("TO_TIME", String.valueOf(toTime));
		if (selInterval != null)
			whereClauseMap.put("SEL_INTERVAL", selInterval);
		int selectionCnt = getSelcetionCountForSubcriber(subscriberID,
				whereClauseMap);
		boolean isSpecilaCallerSelection = isSpecialCallerSelection(callerID);
		String chargeClass = getChargeClassForSpecificCosUser(cos, subscriber,
				inLoop, isSpecialSelection, selectionCnt, clipClassType , isSpecilaCallerSelection);
		return chargeClass;
	}

	public String getNextChargeClass(Subscriber subscriber) {
		if (!isSubActive(subscriber) || subscriber.cosID() == null)
			return null;

		CosDetails cosObject = getCosForActiveSubscriber(null, subscriber);

		int selCount = subscriber.maxSelections();
		return (getChargeClassFromCos(cosObject, selCount));
	}

	public final String getNextChargeClass(String subscriberID) {
		return (getNextChargeClass(getSubscriber(subID(subscriberID))));
	}

	public String getChargeClassFromCos(CosDetails cosObject, int selCount) {
		if (cosObject == null || cosObject.getFreechargeClass() == null)
			return null;

		int repeatCount = cosObject.getFreeSongs();

		List<String> chargeClassList = new ArrayList<String>();
		String[] chargeClassTokens = cosObject.getFreechargeClass().split(",");
		for (String chargeClassToken : chargeClassTokens) {
			int startIndex = chargeClassToken.indexOf('*');
			if (startIndex != -1) {
				String chargeClass = chargeClassToken.substring(0, startIndex);
				int chargeClassCount = Integer.parseInt(chargeClassToken
						.substring(startIndex + 1));
				for (int i = 0; i < chargeClassCount; i++) {
					chargeClassList.add(chargeClass);
				}
			} else {
				chargeClassList.add(chargeClassToken);
			}
		}

		int chargeClassCount = chargeClassList.size();
		if (repeatCount != 0 && selCount >= repeatCount * chargeClassCount) {
			// If repeatCount is zero then its unlimited. Thats why if checking
			// repeatCount != 0.
			return "DEFAULT";
		}

		selCount = selCount % chargeClassCount;
		return chargeClassList.get(selCount);
	}

	public String addSubscriberDownload(String subscriberId,
			String subscriberWavFile, int categoryID, boolean downloaded,
			int categoryType) {
		return addSubscriberDownload(subscriberId, subscriberWavFile,
				categoryID, null, downloaded, categoryType, null, null, null);
	}

	/* ADDED FOR AIRCEL LITE FEATURE SUPPORT */
	public boolean updateDownloads(String subscriberId, String refID,
			char downloadStatus, String extraInfo, String chargeClass) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl
					.updateDownloadStatusExtrainfoNChargeclass(conn,
							subscriberId, refID, downloadStatus, extraInfo,
							chargeClass);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean updateDownloadStatus(String subscriberId, String promoId,
			char downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.updateDownloadStatus(conn,
					subID(subscriberId), promoId, downloadStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public boolean updateDownloadStatusToDownloaded(String subscriberID,
			String promoID, Date startTime, int validity) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.updateDownloadStatusToDownloaded(
					conn, subID(subscriberID), promoID, startTime, validity);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public BulkPromo getActiveBulkPromo(String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getActiveBulkPromo(conn, promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* All methods related to RBT_DEACTIVATED_SUBSCRIBERS */
	/* ADDED FOR TATA */
	public boolean addSubscriberToDeactivatedSubscribersTable(
			String subscriberId, String deactivatedBy, String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return DeactivatedSubscribersImpl.insert(conn, subID(subscriberId),
					deactivatedBy, cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public DeactivatedSubscribers getLastPromoDeactivatedDetail(
			String subscriberId, String promoKey) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return DeactivatedSubscribersImpl.getLastPromoDeactivatedDetail(
					conn, subID(subscriberId), promoKey);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public DeactivatedSubscribers[] getUserCosDeactDetail(String subscriberId,
			String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return DeactivatedSubscribersImpl.getUserCosDeactDetail(conn,
					subID(subscriberId), cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* All methods related to BRT_PROMO_TABLE */
	/* ADDED FOR TATA */
	public PromoTable insertIntoPromoTable(String subscriberId, String promoId,
			String promoClipsSMS) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PromoTableImpl.insert(conn, subID(subscriberId), promoId,
					promoClipsSMS);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public PromoTable getPromoTable(String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return PromoTableImpl.getPromoTable(conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean removePromoTable(String subscriberId) {
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		boolean promoTable = PromoTableImpl.remove(conn, subscriberId);
		releaseConnection(conn);
		return promoTable;
	}

	/**
	 * @param subscriberID
	 * @param circleID
	 * @param prepaidYes
	 * @param mode
	 * @return CosDetails
	 */
	public CosDetails getCos(String subscriberID, String circleID,
			String prepaidYes, String mode) {
		Subscriber subscriber = getSubscriber(subscriberID);
		return getCos(null, subscriberID, subscriber, circleID, prepaidYes,
				mode, null);
	}

	/**
	 * @param task
	 * @param subscriberID
	 * @param subscriber
	 * @param circleID
	 * @param prepaidYes
	 * @param mode
	 * @param subscriptionClass
	 * @return CosDetails
	 */
	public CosDetails getCos(WebServiceContext task, String subscriberID,
			Subscriber subscriber, String circleID, String prepaidYes,
			String mode, String subscriptionClass) {
		logger.info("starting with circle id " + circleID + " prepaid yes "
				+ prepaidYes);
		if (isSubActive(subscriber))
			return getCosForActiveSubscriber(task, subscriber);

		CosDetails subscriberCos = getCosForMode(circleID, mode, prepaidYes,
				subscriptionClass);
		if (subscriberCos == null)
			subscriberCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getDefaultCosDetail(circleID, prepaidYes);

		if (!subscriberCos.isDefaultCos()) {
			DeactivatedSubscribers[] userDeactRecord = getUserCosDeactDetail(
					subID(subscriberID), subscriberCos.getCosId());

			if (userDeactRecord != null
					&& subscriberCos.getNumsubscriptionAllowed() <= userDeactRecord.length)
				subscriberCos = CacheManagerUtil.getCosDetailsCacheManager()
						.getDefaultCosDetail(circleID, prepaidYes);
		}
		logger.info("leaving with " + subscriberCos != null ? subscriberCos
				.getCosId() : "null");
		return subscriberCos;
	}

	public CosDetails getSubscriberCos(String subscriberID, String circleID,
			String prepaidYes, String mode, boolean returnDefaultAlways) {

		Subscriber subscriber = getSubscriber(subscriberID);
		if (subscriber != null)
			return CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(
					subscriber.cosID(), circleID);

		CosDetails subscriberCos = null;
		if (returnDefaultAlways)
			subscriberCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getDefaultCosDetail(circleID, prepaidYes);
		else {
			List<CosDetails> cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getAllActiveCosDetails(circleID, prepaidYes);
			for (int i = 0; i < cos.size(); i++) {
				if (cos.get(i).getAccessMode().indexOf(mode) >= 0) {
					if (!cos.get(i).isDefaultCos()) {
						if (cos.get(i).getAccessMode().indexOf("TRIAL") >= 0) {
							if (isSubAllowedForTrailCos(subID(subscriberID),
									cos.get(i).getCosId())) {
								subscriberCos = cos.get(i);
								break;
							} else
								continue;
						} else {
							subscriberCos = cos.get(i);
							break;
						}
					} else
						// if(cos[i].isDefault())
						subscriberCos = cos.get(i);
				} // end of mode if
			}// end of for llop of all cos'
		}

		if (subscriberCos == null) {
			logger.info("RBT::got null COS for " + subscriberID);
			return null;
		}

		if (!subscriberCos.isDefaultCos()) {
			DeactivatedSubscribers[] userDeactRecord = getUserCosDeactDetail(
					subscriberID, subscriberCos.getCosId());
			if (userDeactRecord != null
					&& subscriberCos.getNumsubscriptionAllowed() <= userDeactRecord.length)
				subscriberCos = CacheManagerUtil.getCosDetailsCacheManager()
						.getDefaultCosDetail(circleID, prepaidYes);
		}

		return subscriberCos;
	}

	private CosDetails getDefaultCos(CosDetails[] cos, String circleID) {
		for (int i = 0; i < cos.length; i++)
			if (cos[i].isDefaultCos())
				return cos[i];
		logger.info("RBT::got null default COS for circle " + circleID);
		return null;
	}

	public CosDetails getCosForActiveSubscriber(WebServiceContext task,
			Subscriber subscriber) {
		if (isSubscriberDeactivated(subscriber)) {
			logger.warn("Retruning null, subscriber is deactive. subscriber: "
					+ subscriber);
			return null;
		}

		boolean allowUpgradeForActPendingUsers = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.COMMON,
						"ALLOW_ACT_PENDING_USERS_FOR_UPGRADATION", "FALSE");
		boolean subscriberActivationPending = isSubscriberActivationPending(subscriber);
		String subscriberId = null;
		String subYes = "";
		String circleId = null;
		String subscriberExtraInfo = null;

		if (null != subscriber) {
			subscriberId = subscriber.subID();
			subYes = subscriber.subYes();
			circleId = subscriber.circleID();
			subscriberExtraInfo = subscriber.extraInfo();
		}

		Map<String, String> subExtraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriberExtraInfo);
		String subscriberPacks = (subExtraInfoMap != null) ? subExtraInfoMap
				.get(iRBTConstant.EXTRA_INFO_PACK) : null;

		logger.info("Configured allowUpgradeForActPendingUsers: "
				+ allowUpgradeForActPendingUsers
				+ ", subscriberActivationPending: "
				+ subscriberActivationPending + ", subscriberId: "
				+ subscriberId);

		if (allowUpgradeForActPendingUsers
				&& (subscriberActivationPending || subYes
						.equals(iRBTConstant.STATE_CHANGE))) {
			List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
					.getBySubscriberIDAndType(subscriberId,
							Type.BASE_UPGRADATION.getTypeCode());
			logger.info("Checking provisioning requests. "
					+ " provisioningRequestsList: " + provisioningRequestsList
					+ ", subscriberId: " + subscriberId);
			if (provisioningRequestsList != null) {
				Collections.reverse(provisioningRequestsList);
				for (ProvisioningRequests provisioningRequest : provisioningRequestsList) {
					Map<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(provisioningRequest
									.getExtraInfo());
					if (extraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)) {
						CosDetails cosDetail = CacheManagerUtil
								.getCosDetailsCacheManager()
								.getCosDetail(
										extraInfoMap
												.get(iRBTConstant.EXTRA_INFO_COS_ID),
										circleId);
						logger.info("Provisioning request extra info contains "
								+ "COS_ID. " + " Returning cosDetail: "
								+ cosDetail + " for subscriberID: "
								+ subscriberId);
						return cosDetail;
					} else {
						logger.warn("Not returning PACK cos, extra info does not"
								+ " contain COS_ID. extraInfoMap: "
								+ extraInfoMap
								+ ", prov req cosId: "
								+ provisioningRequest.getType());
					}
				}
			}
		}

		logger.info("Checking subscriber's COS_ID in extra info. "
				+ " subscriberId: " + subscriberId + ", subcriptionYes: "
				+ subYes + ", subExtraInfoMap: " + subExtraInfoMap);
		if (subscriberActivationPending
				|| subYes.equals(iRBTConstant.STATE_CHANGE)) {
			if (subExtraInfoMap != null
					&& subExtraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)) {
				String cosID = subExtraInfoMap
						.get(iRBTConstant.EXTRA_INFO_COS_ID);
				CosDetails cosDetail = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID,
								subscriber.circleID());
				logger.info("Subscriber is in activation pending and"
						+ " subscriber extra info contains cosId: " + cosID
						+ ". Returning cosDetail: " + cosDetail
						+ " for subscriberID: " + subscriber.subID());
				return cosDetail;
			}
		}

		logger.info("Checking subscriber's subscriberPacks in extra info. "
				+ " subscriberId: " + subscriberId + ", subExtraInfoMap: "
				+ subExtraInfoMap);

		if (subscriberPacks != null && subscriberPacks.trim().length() > 0
				&& task != null) {
			String categoryId = task
					.getString(WebServiceConstants.param_categoryID);
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = null;

			if (categoryId != null) {
				category = rbtCacheManager.getCategory(Integer
						.parseInt(categoryId));
			}

			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip clip = null;
			if (task.containsKey(WebServiceConstants.param_clipID)) {
				String clipID = task
						.getString(WebServiceConstants.param_clipID);
				try {
					clip = rbtCacheManager.getClip(clipID);
				} catch (NumberFormatException e) {
					logger.debug(e.getMessage(), e);
				}
			}

			int status = 1;
			if (task.containsKey(WebServiceConstants.param_status))
				status = Integer.parseInt(task
						.getString(WebServiceConstants.param_status));

			if (task.containsKey(WebServiceConstants.param_cricketPack))
				status = 90;

			if (task.containsKey(WebServiceConstants.param_profileHours))
				status = 99;

			if (status != 99
					&& task.containsKey(WebServiceConstants.param_profileHours)
					&& task.getString(WebServiceConstants.param_profileHours)
							.equals("99")) {
				status = 99;
			}

			String callerID = task
					.getString(WebServiceConstants.param_callerID);

			logger.info("Validated the requested category and clip. subscriberId: "
					+ subscriberId + ", callerID: " + callerID);

			CosDetails cosDetail = getCosDetailsForContent(subscriberId,
					subscriberPacks, category, clip, status, callerID);
			if (cosDetail != null) {
				logger.info("Returning  cosDetail: "
						+ cosDetail.getCosId()
						+ " for subscriber: "
						+ subscriber.subID()
						+ ", since subscriber extra info contains PACK attribute");
				return cosDetail;
			}
		}

		/*
		 * Added by sridhar.sindiri
		 * 
		 * If an active user does not have any active packs, and in the url if
		 * pack cosID is passed, we are not considering the pack COS while
		 * returning the amount for getNextChargeClass API.
		 */
		if (subscriberPacks == null && task != null
				&& task.containsKey(WebServiceConstants.param_cosID)) {
			String cosID = task.getString(WebServiceConstants.param_cosID);
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosID, subscriber.circleID());
			boolean isPackRequest = isPackRequest(cos);
			logger.info("Subscriber extra info contains PACK and request "
					+ "contains cosID: " + cosID + ", cos: " + cos
					+ ", isPackRequest: " + isPackRequest);
			if (isPackRequest) {
				int status = 1;
				if (task.containsKey(WebServiceConstants.param_status)) {
					status = Integer.parseInt(task
							.getString(WebServiceConstants.param_status));
				}

				if (PROFILE_COS_TYPE.equalsIgnoreCase(cos.getCosType())
						&& (task.containsKey(WebServiceConstants.param_profileHours) || status == 99))
					return cos;
				else if (task.containsKey(WebServiceConstants.param_categoryID)) {
					String categoryID = task
							.getString(WebServiceConstants.param_categoryID);
					com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = null;

					if (categoryID != null) {
						category = rbtCacheManager.getCategory(Integer
								.parseInt(categoryID));
					}

					com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip clip = null;
					if (task.containsKey(WebServiceConstants.param_clipID)) {
						String clipID = task
								.getString(WebServiceConstants.param_clipID);
						try {
							clip = rbtCacheManager.getClip(clipID);
						} catch (NumberFormatException e) {
							logger.debug(e.getMessage(), e);
						}
					}

					boolean isContentAllowed = DataUtils
							.isContentAllowedForCos(cos, category, clip);
					logger.info("Checking content is allowed since the request"
							+ " contains categoryId: " + categoryID
							+ ", isContentAllowed: " + isContentAllowed);
					if (isContentAllowed) {
						logger.info("Content is valid. returning  cos: " + cos);
						return cos;
					}
				} else {
					logger.info("Request is pack request, returning  cos: "
							+ cos);
					return cos;
				}
			}
		}

		CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
				.getCosDetail(subscriber.cosID(), subscriber.circleID());
		logger.warn("Retruning subcriber default COS, cosDetail: " + cosDetail
				+ " for subscriber: " + subscriber);
		return cosDetail;
	}

	// Added for Voice Presence APP
	public ProvisioningRequests getProvisioningRequestFromRefId(String subId,
			String refid) {
		ProvisioningRequests provisioningRequest = ProvisioningRequestsDao
				.getByTransId(subId, refid);
		return provisioningRequest;
	}

	/**
	 * @param subscriberID
	 * @param type
	 * @return
	 */
	public List<ProvisioningRequests> getProvisioningRequests(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getBySubscriberIDAndType(subscriberID, type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	public List<ProvisioningRequests> getAciveProvisioningRequests(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getActiveProvisioningBySubscriberIDAndType(subscriberID, type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	public List<ProvisioningRequests> getAciveProvisioningRequests(
			String subscriberID) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getActiveProvisioningBySubscriberID(subscriberID);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public List<ProvisioningRequests> getActAndActPendingProvisioningBySubIDAndType(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getActAndActPendingProvisioningBySubIDAndType(subscriberID,
						type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	public List<ProvisioningRequests> getPacksToBeActivatedBySubscriberIDAndType(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getPacksToBeActivatedBySubscriberIDAndType(subscriberID, type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	public List<ProvisioningRequests> getBySubscriberIDAndType(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getBySubscriberIDAndType(subscriberID, type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}

	/* ADDED FOR TATA */
	public SubscriberDownloads[] getSubscriberActiveDownloads(
			String subscriberID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getSubscriberActiveDownloads(conn,
					subID(subscriberID), categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* All Methods */
	public SubscriberDownloads[] getSubscriberAllActiveDownloads(
			String subscriberID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberAllActiveDownloads(
					conn, subID(subscriberID), categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	// ADDED FOR VODACOMM TO CHECK IF ACTIVE DOWNLOAD EXISTS FOR SUBSCRIBER
	public SubscriberDownloads[] getSubscriberAllActiveDownloads(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberAllActiveDownloads(
					conn, subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	public SubscriberDownloads[] getSubscriberDownloads(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownloads(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads getSubscriberBookMark(String subscriberID,
			String promoID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getSubscriberBookMark(conn,
					subID(subscriberID), promoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] getSubscriberBookMarks(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberBookMarks(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String addSubscriberBookMark(String subscriberID, String promoID,
			int categoryID, int categoryType, String selectedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return "FAILED";

		boolean result = false;
		try {
			SubscriberDownloads[] downloads = SubscriberDownloadsImpl
					.getSubscriberDownloadsByPromoID(conn, subID(subscriberID),
							promoID);
			boolean isUpdated = false;
			if (downloads != null && downloads.length > 0) {
				boolean isBookmarkPresent = false;
				boolean isDownloadPresent = false;
				for (SubscriberDownloads download : downloads) {
					if (download.downloadStatus() == 'b')
						isBookmarkPresent = true;
					else if (download.downloadStatus() != 'x')
						isDownloadPresent = true;
					else if (download.downloadStatus() == 'x') {
						logger.info("Since download status is x, updating "
								+ "existing bookmark. subscriberID: "
								+ subscriberID + ", promoID: " + promoID
								+ ", categoryID: " + categoryID
								+ ", selectedBy: " + selectedBy);
						isUpdated = true;
						result = SubscriberDownloadsImpl.updateBookMark(conn,
								subscriberID, promoID, categoryID,
								categoryType, selectedBy, "x", "b");
					}
				}
				if (isBookmarkPresent) {
					logger.info("Returning result: ALREADY_EXISTS");
					return "ALREADY_EXISTS";
				}
				if (isDownloadPresent) {
					logger.info("Returning result: ALREADY_DOWNLOADED");
					return "ALREADY_DOWNLOADED";
				}
			}

			if (!isUpdated) {

				result = SubscriberDownloadsImpl.addSubscriberBookMark(conn,
						subID(subscriberID), promoID, categoryID, categoryType,
						selectedBy);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		String resultStr = result ? "SUCCESS" : "FAILED";
		logger.info("Returning result: " + resultStr);
		return resultStr;
	}

	public boolean removeSubscriberBookMark(String subscriberID, String promoID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberDownloadsImpl.removeSubscriberBookMark(conn,
					subID(subscriberID), promoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean removeSubscriberBookMark(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.removeAllSubscriberBookMarks(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberDownloads getSubscriberDownload(String subscriberId,
			String wavFile, int categoryID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownload(conn,
					subID(subscriberId), wavFile, categoryID, categoryType,
					false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads getOldestActiveSubscriberDownload(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getOldestActiveSubscriberDownload(
					conn, subID(subscriberID), -1);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	public SubscriberDownloads getOldestActiveSubscriberDownload(
			String subscriberID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getOldestActiveSubscriberDownload(
					conn, subID(subscriberID), categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean deactivateSubscriberDownload(String subscriberId,
			String promoId, String deactivateBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deactivateSubscriberDownload(conn,
					subID(subscriberId), promoId, deactivateBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateSubscriberDownload(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deactivateSubscriberDownload(conn,
					subID(subscriberId), wavFile, categoryId, categoryType,
					deactivateBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireSubscriberDownload(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo,
			boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireSubscriberDownload(conn,
					subID(subscriberId), wavFile, deactivateBy, categoryId,
					categoryType, deselectionInfo, null, false,
					isDirectDeactivation);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireSubscriberDownloadAndUpdateExtraInfo(
			String subscriberId, String wavFile, int categoryId,
			int categoryType, String deactivateBy, String deselectionInfo,
			String extraInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireSubscriberDownload(conn,
					subID(subscriberId), wavFile, deactivateBy, categoryId,
					categoryType, deselectionInfo, extraInfo, false,
					isDirectDeactivation);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberDownloads getDownloadToBeDeactivated(String subscriberID,
			String promoID, int categoryId, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getDownloadToBeDeactivated(conn,
					subID(subscriberID), promoID, categoryId, categoryType,
					false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	// JIRA-RBT-6194:Search based on songs in Query Gallery API
	public SubscriberDownloads getSubscriberDownloadsByPromoId(
			String subscriberID, String promoID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getSubscriberDownloadByPromoID(conn,
					subID(subscriberID), promoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean expireSubscriberDownload(String subscriberId, String refID,
			String deactivateBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireSubscriberDownload(conn,
					subID(subscriberId), refID, deactivateBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireSubscriberSelection(String subscriberId, String refID,
			String deactivatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberRecordsByRefId(
					conn, subscriberId, deactivatedBy, refID, null, null, null,
					null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ArrayList getSelectionsToBeDeleted(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getSelectionsToBeDeleted(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] smGetDownloadsToBeActivated(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.smGetDownloadsToBeActivated(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] smGetBaseActivationPendingDownloads(
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.smGetBaseActivationPendingDownloads(
					conn, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] smGetDownloadsToBeDeactivated(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.smGetDownloadsToBeDeactivated(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean deleteClipForSubscriber(String subscriberID,
			String subWavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return deleteClipForSubscriber(subID(subscriberID), subWavFile,
					false, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteClipForSubscriber(String subscriberID,
			String subWavFile, String callerID) {
		return deleteClipForSubscriber(subID(subscriberID), subWavFile, true,
				callerID);
	}

	public boolean deleteClipForSubscriber(String subscriberID,
			String subWavFile, boolean b, String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deleteClipForSubscriber(conn,
					subID(subscriberID), subWavFile, b, callerID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteFromToBeDeletedSelections(
			SubscriberDownloads selectionTobeDeleted) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deleteFromToBeDeletedSelections(
					conn, selectionTobeDeleted);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteSubscriberDownloads(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deleteSubscriberDownloads(conn,
					subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteSubscriberDownload(String subscriberID,
			String wavFile, int categoryID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deleteSubscriberDownload(conn,
					subID(subscriberID), wavFile, categoryID, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* all methods related to RBT_BULKACTIVATION table */

	public BulkActivation addBulkActivation(String fileName,
			String bulkPromoID, String status, String promoID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkActivationImpl.insert(conn, fileName, bulkPromoID,
					status, promoID, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkActivation[] getAllBulkActivation() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkActivationImpl.getAllBulkActivation(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkActivation getPendingBulkActivation() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return BulkActivationImpl.getPendingBulkActivation(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateBulkActivationStatus(String fileName, String status) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return BulkActivationImpl.updateBulkActivationStatus(conn,
					fileName, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public BulkPromo addBulkPromo(String bulkPromoId, Date startDate,
			Date endDate, String cosID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.insert(conn, bulkPromoId, startDate, endDate,
					cosID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo[] getBulkPromosByStartDate(Date startDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getBulkPromosByStartDate(conn, startDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo[] getBulkPromosByEndDate(Date endDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getBulkPromosByEndDate(conn, endDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo[] getActiveBulkPromos() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getActiveBulkPromos(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo[] getBulkPromos() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getBulkPromos(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public OnVoxUser getOnVoxUser(String userName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return OnVoxUserImpl.getOnVoxUser(conn, userName);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/* ADDED FOR TATA */
	/* All methods related to RBT_BULK_PROMO_SMS */
	public BulkPromoSMS addBulkPromoSMS(String promoId, Date smsDate,
			String smsText, String sentSMS) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			String smsDateString = format.format(smsDate);
			return BulkPromoSMSImpl.insert(conn, promoId, smsDateString,
					smsText, sentSMS);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS addBulkPromoSMS(String promoId, String smsDate,
			String smsText, String sentSMS) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.insert(conn, promoId, smsDate, smsText,
					sentSMS);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS getBulkPromoSMSForDay(String promoId, int smsDay) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.getBulkPromoSMS(conn, promoId, smsDay);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS updateBulkPromoSMSForDay(String promoId, int smsDay,
			String smsText) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.updateBulkPromoSMS(conn, promoId, smsDay,
					smsText);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSMSSent(String promoId, int smsDay, String smsSent) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return BulkPromoSMSImpl.updateSMSSent(conn, promoId, smsDay,
					smsSent);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateSMSSent(String promoId, String smsDay, String smsSent) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return BulkPromoSMSImpl.updateSMSSent(conn, promoId, smsDay,
					smsSent);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public BulkPromoSMS[] getBulkPromoSMSForDate(Date smsDay) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoSMSImpl.getBulkPromoSMSForDate(conn, smsDay);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS getBulkPromoSMSForDate(String bulkPromoID, Date smsDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (smsDate == null)
				smsDate = new Date();
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			String smsDateString = format.format(smsDate);
			BulkPromoSMS promoSMS = BulkPromoSMSImpl.getBulkPromoSMSForDate(
					conn, bulkPromoID, smsDateString);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromoSMS getBulkPromoSMSForDate(String bulkPromoID,
			String smsDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return BulkPromoSMSImpl.getBulkPromoSMSForDate(conn, bulkPromoID,
					smsDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public BulkPromo[] getPromosToDeactivateSubscribers() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return BulkPromoImpl.getPromosToDeactivateSubscribers(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateProcessedDeactivation(String bulkPromoId,
			String processedDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return BulkPromoImpl.updateProcessedDeactivation(conn, bulkPromoId,
					processedDeactivation);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus getRefIDSelectionOldLogic(Connection conn,
			String subscriberID, String callerID, int status, String setTime,
			int fTime, int tTime, String wavFile) {
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getRefIDSelectionOldLogic(conn,
					subID(subscriberID), callerID, status, setTime, fTime,
					tTime, wavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateRefIDSelectionOldLogic(Connection conn,
			String subscriberID, String callerID, int status, String setTime,
			int fTime, int tTime, String wavFile, String refID) {
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateRefIDSelectionOldLogic(conn,
					subID(subscriberID), callerID, status, setTime, fTime,
					tTime, wavFile, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus getRefIDSelection(String subscriberID, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {

			return SubscriberStatusImpl.getRefIDSelection(conn,
					subID(subscriberID), refID);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus getRefIDSelection(Connection conn,
			String subscriberID, String refID) {
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getRefIDSelection(conn,
					subID(subscriberID), refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String smUpdateDeactiveSelectionSuccess(String subscriberID,
			String refID, String type, String setTime, Date startDate,
			String classType, char newLoopStatus, int rbtType, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smUpdateDeactiveSelectionSuccess(
					conn, subID(subscriberID), refID, type, startDate,
					classType, newLoopStatus, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String smUpdateDeactiveSelectionFailure(String subscriberID,
			String refID, String type, String classType, String deactBy,
			char newLoopStatus, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smUpdateDeactiveSelectionFailure(
					conn, subID(subscriberID), refID, type, classType, deactBy,
					newLoopStatus, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String updateSelectionExtraInfo(String subscriberID,
			ArrayList<String> refIdList, String extraInfoQueryStr) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.updateSelectionExtraInfo(conn,
					subID(subscriberID), refIdList, extraInfoQueryStr);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String updateSelectionStatusNExtraInfo(String subscriberID,
			String refId, String extraInfo, String status, String classType) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.updateSelectionStatusNExtraInfo(
					conn, subID(subscriberID), refId, extraInfo, status,
					classType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public String updateExtraInfoNStatusNDeactBy(String subscriberID,
			String extraInfo, String status, String deactBy) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberImpl.updateExtraInfoNStatusNDeactBy(conn,
					subscriberID, extraInfo, status, deactBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	}

	public int removeOldTransData(float duration) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;
		try {
			return TransDataImpl.removeOldTransData(conn, duration);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public TransData getTransData(String transID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.getTransData(conn, transID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public List<TransData> getTransData(String type, int limit) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return TransDataImpl.getTransDataByType(conn, type, limit);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public List<TransData> getTransDataByTypeAndTransDate(String type, int limit,Date transDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return TransDataImpl.getTransDataByTypeAndTransDate(conn, type, limit, transDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public TransData getTransDataAndUpdateAccessCount(String transID,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.getTransDataAndUpdateAccessCount(conn,
					transID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public TransData[] getTransDataBySubscriberID(String subscriberID,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.getTransDataBySubscriberID(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public TransData getFirstTransDataByType(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.getFirstTransDataByType(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public TransData addTransData(String transID, String subscriberID,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.insert(conn, transID, subID(subscriberID),
					type,null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public TransData addTransData(String transID, String subscriberID,
			String type,Date transDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return TransDataImpl.insert(conn, transID, subID(subscriberID),
					type,transDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RbtSupport addRbtSupport(String subscriberId, String callerId,
			int clipId, Map<String, String> extraInfoMap, int status, int type) {
		RbtSupport rbtSupport = new RbtSupport();
		rbtSupport.setCallerId(Long.parseLong(callerId));
		rbtSupport.setClipId(clipId);
		rbtSupport.setRequestDate(new Date());
		rbtSupport.setStatus(status);
		rbtSupport.setSubscriberId(Long.parseLong(subscriberId));
		rbtSupport.setType(type);
		String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		rbtSupport.setExtraInfo(extraInfo);

		try {
			rbtSupport = RbtSupportDao.save(rbtSupport);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
			rbtSupport = null;
		}
		return rbtSupport;

	}

	public boolean removeTransData(String transID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return TransDataImpl.removeTransData(conn, transID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	public boolean removeListOfTransData(String transIDs) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		
		try {
			return TransDataImpl.removeListOfTransData(conn, transIDs);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateTransData(String transID, String type,
			String subscriberID, Date transDate, String accessCount) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return TransDataImpl.update(conn, transID, type,
					subID(subscriberID), transDate, accessCount);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/* ADDED FOR TATA */
	public SubscriberStatus[] getSubscriberStatus(String subscriberID,
			String subWavFile) {
		return getSubscriberStatus(subID(subscriberID), subWavFile, 0);
	}

	public SubscriberStatus[] getSubscriberStatus(String subscriberID,
			String subWavFile, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSubscriberStatus(conn,
					subID(subscriberID), subWavFile, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable getViralSMSByTypeOrderedByTimeDesc(
			String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeOrderedByTimeDesc(
							conn, subID(subscriberID), type);
				}
			}
			return ViralSMSTableImpl.getViralSMSByTypeOrderedByTimeDesc(conn,
					subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypeAndLimit(String type, int count) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeAndLimit(conn,
							type, count);
				}
			}
			return ViralSMSTableImpl.getViralSMSByTypeAndLimit(conn, type,
					count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypeAndLimitAndTime(String type,
			int time, int count) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeAndLimitAndTime(
							conn, type, time, count);
				}
			}

			return ViralSMSTableImpl.getViralSMSByTypeAndLimitAndTime(conn,
					type, time, count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getViralSMSByTypeAndTime(String type, int time) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getViralSMSByTypeAndTime(conn, type,
							time);
				}
			}
			return ViralSMSTableImpl.getViralSMSByTypeAndTime(conn, type, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable getLatestViralSMSByTypeSubscriberAndTime(
			String subscriberID, String type, int time) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl
							.getLatestViralSMSByTypeSubscriberAndTime(conn,
									subscriberID, type, time);
				}
			}
			return ViralSMSTableImpl.getLatestViralSMSByTypeSubscriberAndTime(
					conn, subscriberID, type, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable[] getLatestViralSMSesByTypeSubscriberAndTime(
			String subscriberID, String type, int time) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl
							.getLatestViralSMSesByTypeSubscriberAndTime(conn,
									subscriberID, type, time);
				}
			}
			return ViralSMSTableImpl
					.getLatestViralSMSesByTypeSubscriberAndTime(conn,
							subscriberID, type, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ViralSMSTable getLatestViralSMSByTypeAndTime(String callerID,
			String type, int time) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getLatestViralSMSByTypeAndTime(conn,
							callerID, type, time);
				}
			}
			return ViralSMSTableImpl.getLatestViralSMSByTypeAndTime(conn,
					callerID, type, time);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	// Start:RBT-14671 - # like
	public long getLikedSongCount(int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;
		try {
			return LikeSongCountTableImpl.getLikedSongCount(conn, clipId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public List<TopLikeSong> getLikedSongDetails(int limit) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return LikeSongCountTableImpl.getLikedSongDetails(conn, limit);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean deleteSubscriberLikedSong(String subscriberId, int clipId,
			int catId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return LikeSubscriberSongCountImpl.deleteSubscriberLikedSong(conn,
					subscriberId, clipId, catId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateLikedSongCount(int clipId, long count) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return LikeSongCountTableImpl.updateLikedSongCount(conn, clipId,
					count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean insertLikedSong(int clipId, long count) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return LikeSongCountTableImpl.insertLikedSong(conn, clipId, count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public long getLikedSubsciberSongCount(String subscriberId, int clipId,
			int catId) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;
		try {
			return LikeSubscriberSongCountImpl.getLikedSubsciberSongCount(conn,
					subscriberId, clipId, catId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public List<TopLikeSubscriberSong> getLikedSubscriberSongDetails(int limit,
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return LikeSubscriberSongCountImpl.getLikedSubscriberSongDetails(
					conn, limit, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSubscriberLikedSongCount(String subscriberId,
			int clipId, int catId, long count) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return LikeSubscriberSongCountImpl.updateSubscriberLikedSongCount(
					conn, subscriberId, clipId, catId, count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean insertSubscriberLikedSongCount(String subscriberId,
			int clipId, int catId, long count) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return LikeSubscriberSongCountImpl.insertSubscriberLikedSongCount(
					conn, subscriberId, clipId, catId, count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// End:RBT-14671 - # like
	public long getDbTime(Connection conn) {
		Statement stmt = null;
		Date d = new Date();
		try {
			stmt = conn.createStatement();
			String query = null;
			if (RBTPrimitive.getDBSelectionString().equals(
					RBTPrimitive.DB_SAPDB))
				query = "SELECT SYSDATE FROM DUAL";
			else
				query = "SELECT SYSDATE() FROM DUAL";

			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
				d = rs.getTimestamp(1);
		} catch (Throwable e) {
			logger.error("Exception in getting time", e);
		}
		return (d.getTime());
	}

	// retailer implementation
	void refreshRetailerList() {
		if (retailerInRefresh)
			return;
		else if (m_retCal.before(Calendar.getInstance())) {
			synchronized (m_obj) {
				if (retailerInRefresh)
					return;
				retailerInRefresh = true;
				m_retCal.add(Calendar.MINUTE, retRefreshInterval);
			}
			new RefreshRetailer(m_dbURL).start();
		}
	}

	// retailer implementation
	void refreshTrailSubsList() {
		if (trailSubsInRefresh)
			return;
		else if (m_retCal.before(Calendar.getInstance())) {
			synchronized (m_obj) {
				if (trailSubsInRefresh)
					return;
				trailSubsInRefresh = true;
				m_trailSubsCal.add(Calendar.MINUTE, trailSubsRefreshInterval);
			}
			new RefreshTrailSubs(m_dbURL).start();
		}
	}

	public String isRetailer(String subID) {
		String retailerType = null;
		if (m_retailerHash != null && m_retailerHash.containsKey(subID))
			retailerType = (String) m_retailerHash.get(subID);
		if (m_retailerHash == null) {
			Connection conn = getConnection();
			if (conn == null)
				return null;
			try {
				Retailer ret = RetailerImpl.getRetailer(conn, subID);
				if (ret != null)
					retailerType = ret.type();
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
		}

		refreshRetailerList();
		return retailerType;
	}

	public SubscriberPromo[] getAllActiveSubscriberPromo() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberPromoImpl.getAllActiveSubscriberPromo(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public void deleteOldSubscriberPromos() {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberPromoImpl.deleteOldSubscriberPromos(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	private boolean isSubAllowedForTrailCos(String subscriberID, String cosID) {
		boolean allowed = false;

		if (m_trailSubsHash != null
				&& m_trailSubsHash.containsKey(subscriberID + cosID))
			allowed = true;
		if (m_trailSubsHash == null) {
			Connection conn = getConnection();
			if (conn == null)
				return false;

			try {
				SubscriberPromo subPromo = SubscriberPromoImpl
						.getActiveSubscriberPromoSubType(conn,
								subID(subscriberID), cosID);
				if (subPromo != null)
					allowed = true;
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
		}

		refreshTrailSubsList();
		return allowed;
	}

	public Clips addUGC(String subID, String regionName, String classType,
			String clipName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.addUGC(conn, subID, regionName, classType,
					clipName);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public SubscriberStatus[] getSelectionsForUGCCharging(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSelectionsForUGCCharging(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus smSubscriberSelections(String subID, String callID,
			int st) {
		return smSubscriberSelections(subID, callID, st, 0);
	}

	public SubscriberStatus smSubscriberSelections(String subID, String callID,
			int st, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.smSubscriberSelections(conn, subID,
					callID, st, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Clips[] getClipsByAlbum(String subscriberID) {
		if (_rcm.isClipCacheInitialized())
			return _rcm.getClipsByAlbum(subscriberID);
		else {
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip[] tmpClips = rbtCacheManager
					.getClipsByAlbum(subscriberID);
			if (tmpClips == null)
				return null;

			Clips[] clips = new Clips[tmpClips.length];
			for (int i = 0; i < tmpClips.length; i++) {
				clips[i] = new ClipsImpl(tmpClips[i].getClipId(),
						tmpClips[i].getClipName(),
						tmpClips[i].getClipNameWavFile(),
						tmpClips[i].getClipPreviewWavFile(),
						tmpClips[i].getClipRbtWavFile(),
						tmpClips[i].getClipGrammar(),
						tmpClips[i].getClipSmsAlias(),
						String.valueOf(tmpClips[i].getAddToAccessTable()),
						tmpClips[i].getClipPromoId(),
						tmpClips[i].getClassType(),
						tmpClips[i].getClipStartTime(),
						tmpClips[i].getClipEndTime(),
						tmpClips[i].getSmsStartTime(), tmpClips[i].getAlbum(),
						tmpClips[i].getLanguage(),
						tmpClips[i].getClipDemoWavFile(),
						tmpClips[i].getArtist(), tmpClips[i].getClipInfo());
			}
			return clips;
		}
	}

	// for RW
	public Access getAccessifPresent(int clipID, String name, String year,
			String month) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return AccessImpl.getAccess(conn, clipID, year, month, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Access insertAccess(int clipID, String name, String year,
			String month, int noOfPreviews, int noOfAccess, int noOfPlays) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return AccessImpl.insert(conn, clipID, name, year, month,
					noOfPreviews, noOfAccess, noOfPlays, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateAccess(int clipID, String name, String year,
			String month, int noOfPreviews, int noOfAccess, int noOfPlays) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return AccessImpl.update(conn, clipID, name, year, month,
					noOfPreviews, noOfAccess, noOfPlays, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Poll insertPoll(String pollID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PollImpl.insert(conn, pollID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updatePoll(Poll poll) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return PollImpl.update(conn, poll);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Poll getPoll(String pollID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PollImpl.getPoll(conn, pollID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Iterator<Integer> getUGCClipIDsFromAccessTable(String year,
			String month) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			AccessImpl[] allAccess = AccessImpl.getAllUGCAccess(conn, year,
					month);
			if (allAccess != null && allAccess.length > 0) {
				Hashtable<Integer, String> clipIds = new Hashtable<Integer, String>();
				for (int i = 0; i < allAccess.length; i++)
					clipIds.put(new Integer(allAccess[i].clipID()), "DUMMY");
				if (!clipIds.isEmpty())
					return (clipIds.keySet().iterator());
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public Access[] getTopUGCAccesses(String year, String month) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return AccessImpl.getTopUGCAccesses(conn, year, month);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public boolean clearCategoryClipMaps(String categoryID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipsImpl.clearCategoryClipMaps(conn, categoryID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;

	}

	public void fillCategoryClipMaps(String categoryID, int clipId,
			String clipInList, int clipIndex, String playTime) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			ClipsImpl.fillCategoryClipMaps(conn, categoryID, clipId, "y",
					clipIndex, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;

	}

	public Clips[] getLatestUGCClips(int count) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getLatestUGCClips(conn, count);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean expireUGCClipsForPromoIDs(String promoIDsList) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			ArrayList clipPromoIDList = Tools.tokenizeArrayList(promoIDsList,
					null);
			for (int i = 0; i < clipPromoIDList.size(); i++) {
				String promoID = ((String) clipPromoIDList.get(i)).trim();
				Clips clip = getClipPromoID(promoID);
				if (clip == null || clip.grammar() == null
						|| !clip.grammar().equalsIgnoreCase("UGC"))
					continue;
				success = ClipsImpl.expireUGCClipsForPromoIDs(conn, promoID);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}

	public boolean expireUGCClipsOfCreator(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipsImpl.expireUGCClipsOfCreator(conn, subID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireUGCSelections(String wavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.expireUGCSelections(conn, wavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean unmarkUGCExpiredClip(int clipID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipsImpl.unmarkUGCExpiredClip(conn, clipID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Clips[] getExpiredUGCClips() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getExpiredUGCClips(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Integer[] getClipsInMostAccessOrder(int[] clips, int accessDays) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return AccessImpl
					.getClipsInMostAccessOrder(conn, clips, accessDays);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean makeUGCClipLive(String promoID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipsImpl.makeUGCClipLive(conn, promoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean makeUGCClipSemiLive(String promoID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return ClipsImpl.makeUGCClipSemiLive(conn, promoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public List<Clip> getUGCFilesToTransferToTelephonyServers() {
		if (RBTPrimitive.getDBSelectionString().equals(RBTPrimitive.DB_SAPDB))
			return getUGCFilesToTransfer("TO_DATE('01-01-2004','DD-MM-YYYY')");
		else
			return getUGCFilesToTransfer("TIMESTAMP('2004-01-01')");

	}

	public List<Clip> getUGCFilesToTransferToContentWebServers() {
		if (RBTPrimitive.getDBSelectionString().equals(RBTPrimitive.DB_SAPDB))
			return getUGCFilesToTransfer("TO_DATE('02-01-2004','DD-MM-YYYY')");
		else
			return getUGCFilesToTransfer("TIMESTAMP('2004-01-02')");
	}

	public List<Clip> getUGCFilesToTransfer(String endTime) {
		String selectUGCClips = "from Clip where clipGrammar='UGC' and clipEndTime = "
				+ endTime + "";
		try {
			return ClipsDAO.getClips(selectUGCClips);
		} catch (DataAccessException e) {
			logger.error("", e);
			return null;
		}
	}

	public HashMap getClipMapByStartTime(Date startDate, Date endDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getClipMapByStartTime(conn, startDate, endDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap<String, ArrayList<String>> getClipMapForArtistByStartTime(
			Date startDate, Date endDate) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getClipMapForArtistByStartTime(conn, startDate,
					endDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap<String, ArrayList<String>> getRecommendationByCategoryFromSelections(
			String[] categoryIDs, int recDownloadCount, int fetchDays,
			int maxRecommendationForSubscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getRecommendationByCategoryFromSelections(conn,
							categoryIDs, recDownloadCount, fetchDays,
							maxRecommendationForSubscriber);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap<String, ArrayList<String>> getRecommendationByCategoryFromDownloads(
			String[] categoryIDs, int recDownloadCount, int fetchDays,
			int maxRecommendationForSubscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			HashMap<String, ArrayList<String>> subscriberMap = SubscriberDownloadsImpl
					.getRecommendationByCategoryFromDownloads(conn,
							categoryIDs, recDownloadCount, fetchDays,
							maxRecommendationForSubscriber);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap<String, ArrayList<String>> getRecommendationByArtistsFromSelections(
			HashMap<String, ArrayList<String>> artistClipNameMap,
			int fetchDays, int maxRecommendationForSubscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getRecommendationByArtistsFromSelections(conn,
							artistClipNameMap, fetchDays,
							maxRecommendationForSubscriber, this);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap<String, ArrayList<String>> getRecommendationByArtistsFromDownloads(
			HashMap<String, ArrayList<String>> artistClipNameMap,
			int fetchDays, int maxRecommendationForSubscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl
					.getRecommendationByArtistsFromDownloads(conn,
							artistClipNameMap, fetchDays,
							maxRecommendationForSubscriber, this);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean isSelectionAllowed(Subscriber subscriber, String callerID) {
		return isSelectionAllowed(subscriber, callerID, 0);
	}

	public boolean isSelectionAllowed(Subscriber subscriber, String callerID,
			int rbtType) {
		if (subscriber == null || isSubDeactive(subscriber))
			return true;

		if (subscriber.rbtType() == 0
				&& !subscriber.subscriptionClass().equalsIgnoreCase(
						"DEFAULT_CORP"))
			return true;

		if (maxCallerIDSelectionsAllowed > 0 && callerID != null) {
			SubscriberStatus[] ssCallerId = getPersonalCallerIDSelections(
					subscriber.subID(), callerID, rbtType);
			String callerIDNumbers = "";
			int countCaller = 0;
			for (int i = 0; ssCallerId != null && i < ssCallerId.length; i++) {
				String callerI = ssCallerId[i].callerID();
				if (callerI != null && callerIDNumbers.indexOf(callerI) == -1) {
					callerIDNumbers += ", " + ssCallerId[i].callerID();
					countCaller++;
				}
			}
			if (countCaller >= maxCallerIDSelectionsAllowed)
				return false;
		}

		int count = getCountSelectionsOtherCallerID(subscriber.subID(),
				callerID, rbtType);
		SubscriptionClass sc = CacheManagerUtil
				.getSubscriptionClassCacheManager().getSubscriptionClass(
						subscriber.subscriptionClass());

		if (sc == null)
			return true;
		if (sc.getFreeSelections() <= 0)
			return true;
		if (count < sc.getFreeSelections())
			return true;

		return false;
	}

	public boolean updateRBTType(String subscriberId, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateRBTType(conn, subID(subscriberId),
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateRBTTypeAndPlayerStatus(String subscriberId,
			int rbtType, String playerStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateRBTTypeAndPlayerStatus(conn,
					subID(subscriberId), rbtType, playerStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void deactivateNewSelections(String subscriberId,
			String deselectedBy, String callerId, Date setDate,
			boolean checkCallerId, String status) {
		deactivateNewSelections(subID(subscriberId), deselectedBy, callerId,
				setDate, checkCallerId, 0, null, status);
	}

	public void deactivateNewSelections(String subscriberId,
			String deselectedBy, String callerId, Date setDate,
			boolean checkCallerId, int rbtType, List<String> refIDList,
			String status) {

		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberStatusImpl.deactivateNewSelections(conn,
					subID(subscriberId), deselectedBy, callerId, setDate,
					checkCallerId, rbtType, refIDList, status, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public boolean expireAllSubscriberPendingDownload(String subscriberID,
			String deactivationInfo, List<String> refIDList) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberDownloadsImpl.expireAllSubscriberPendingDownload(
					conn, subscriberID, deactivationInfo, refIDList);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean checkMaxCallerIDSelections(Subscriber subscriber,
			String callerID) {
		if (subscriber == null || isSubscriberDeactivated(subscriber))
			return true;
		if (maxCallerIDSelectionsAllowed > 0 && callerID != null) {
			SubscriberStatus[] ssCallerId = getPersonalCallerIDSelections(
					subscriber.subID(), callerID);
			String callerIDNumbers = "";
			int countCaller = 0;
			for (int i = 0; ssCallerId != null && i < ssCallerId.length; i++) {
				String callerI = ssCallerId[i].callerID();
				if (callerI != null && callerIDNumbers.indexOf(callerI) == -1) {
					callerIDNumbers += ", " + ssCallerId[i].callerID();
					countCaller++;
				}
			}
			if (countCaller >= maxCallerIDSelectionsAllowed)
				return false;
		}
		return true;
	}

	public SubscriberStatus[] getAllAirtelSubscriberSelectionRecords(
			String subscriberID, String startDate, String endDate) {
		return getAllAirtelSubscriberSelectionRecords(subID(subscriberID),
				startDate, endDate, 0);
	}

	public SubscriberStatus[] getAllAirtelSubscriberSelectionRecords(
			String subscriberID, String startDate, String endDate, int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getAllAirtelSubscriberSelectionRecords(
					conn, subID(subscriberID), startDate, endDate, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public List getClipDetails(int from, int to, String parentId,
			String subCat, String searchOption, String searchText, String sorter) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getClipDetails(conn, from, to, searchText,
					searchOption, sorter);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public HashMap initialiseCategoriesMap() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return CategoriesImpl.cacheCategories(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	private int getCountSelectionsOtherCallerID(String subscriberID,
			String callerID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;

		try {
			return SubscriberStatusImpl.getCountSelectionsOtherCallerID(conn,
					subID(subscriberID), callerID, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 0;

	}

	public boolean smDeactivateAllSelections(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			SubscriberStatusImpl.smDeactivate(conn, subID(subscriberID), null,
					true, null, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public ArrayList<String> getSelectionsToAddToPlayer(int fetchSize,
			String circleID, boolean isRBT2) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			// RBT-16004 isRBT2 Added for checking rbt 2
			return SubscriberStatusImpl.getSelectionsToAddToPlayer(conn,
					fetchSize, circleID, isRBT2);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public ArrayList<String> getSelectionsToRemoveFromPlayer(int fetchSize,
			String circleID, boolean isRBT2) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			// RBT-16004 isRBT2 Added for checking rbt 2
			return SubscriberStatusImpl.getSelectionsToRemoveFromPlayer(conn,
					fetchSize, circleID, isRBT2);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateAddedSelectionsInPlayer(String subscriberID,
			char loopStatus, ArrayList<String> refIdList) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateAddedSelectionsInPlayer(conn,
					subID(subscriberID), loopStatus, refIdList);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateRemovedSelectionsFromPlayer(String subscriberID,
			ArrayList<String> refIdList) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateRemovedSelectionsFromPlayer(conn,
					subscriberID, refIdList);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public List<Subscriber> smGetSubscriberToDeactivateInPlayer(int fetchSize,
			boolean suspend, String circleID, boolean isRBT2) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			// RBT-16004 isRBT2 Added for checking rbt 2
			return SubscriberImpl.smGetSubscriberToDeactivateInPlayer(conn,
					fetchSize, suspend, circleID, isRBT2);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateDeactivatedAtPlayer(String subscriberID, String state) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateDeactivatedAtPlayer(conn,
					subID(subscriberID), state);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public HashMap getMBMapByStartTime(Date startDate, Date endDate) {
		return _rcm.getMBMapByStartTime(startDate, endDate);
	}

	public String getDefaultClipId() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "DEFAULT_CLIP");
			if (parameter != null && parameter.getValue() != null)
				return (String) parameter.getValue();
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public String[] getAdvancedRentalPacksForUSSD() {
		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter(USSD, "USSD_ADVANCE_PACKS");
		if (parameter != null && parameter.getValue() != null) {
			StringTokenizer strToken = new StringTokenizer(
					parameter.getValue(), ",");
			ArrayList values = new ArrayList();
			String tmp = null;
			while (strToken.hasMoreTokens()) {
				tmp = strToken.nextToken().trim();
				values.add(tmp);
			}
			if (values.size() > 0) {
				return (String[]) values.toArray(new String[0]);
			}
		}
		return null;
	}

	public String[] getAdvancedRentalValuesDB() {
		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter(COMMON, "VOICE_ADVANCE_PACKS");
		if (parameter != null && parameter.getValue() != null) {
			StringTokenizer strToken = new StringTokenizer(
					parameter.getValue(), ",");
			ArrayList values = new ArrayList();
			String tmp = null;
			while (strToken.hasMoreTokens()) {
				tmp = strToken.nextToken().trim();
				values.add(tmp);
			}
			if (values.size() > 0) {
				return (String[]) values.toArray(new String[0]);
			}
		}

		return null;
	}

	public String getDefaultClipFromParametersDB() {
		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter(COMMON, "DEFAULT_CLIP");
		if (parameter != null && parameter.getValue() != null) {
			String value = parameter.getValue();
			return value;
		}

		return null;
	}

	public String getRBTUserType(String subscriberID) {
		Subscriber sub = getSubscriber(subscriberID);
		return getRBTUserType(sub);
	}

	public String getRBTUserType(Subscriber sub) {
		if (sub == null)
			return USER_TYPE_UNKNOWN;

		switch (sub.rbtType()) {
		case 0:
			return USER_TYPE_RBT;
		case 1:
			return USER_TYPE_SRBT;
		case 2:
			return USER_TYPE_RRBT;
		case 3:
			return USER_TYPE_RBT_RRBT;
		case 4:
			return USER_TYPE_SRBT_RRBT;
		}
		return USER_TYPE_UNKNOWN;
	}

	public boolean checkCanAddSelection(Subscriber subscriber) {
		if (subscriber == null)
			return true;

		String subYes = subscriber.subYes();
		if (subYes.equals(STATE_ACTIVATED)
				|| subYes.equals(STATE_ACTIVATION_PENDING)
				|| subYes.equals(STATE_TO_BE_ACTIVATED)
				|| subYes.equals(STATE_DEACTIVATED)
				|| subYes.equals(STATE_EVENT) || subYes.equals(STATE_CHANGE))
			return true;

		return false;
	}

	/**
	 * @author Sreekar 2008-05-03
	 * @param Wave
	 *            file name
	 * @return V-CDOE If wavFile is of format rbt_AABBCDDXXXXYYYY_rbt then VCode
	 *         will be AABBCDDXXXXYYYY
	 */
	public static String getVCodeFromWavFile(String wavFile) {
		String strVCode = null;
		if (wavFile != null) {
			strVCode = Tools.findNReplaceAll(wavFile, "rbt_", "");
			strVCode = Tools.findNReplaceAll(strVCode, "_rbt", "");
		}
		return strVCode;
	}

	public static String getWavFileFromVCode(String strVCode) {
		if (strVCode != null)
			return "rbt_" + strVCode + "_rbt";
		return null;
	}

	public boolean isShufflePresentSelection(String subID, String callerID) {
		return isShufflePresentSelection(subID, callerID, 0);
	}

	public boolean isShufflePresentSelection(String subID, String callerID,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			SubscriberStatus shuffleSel = SubscriberStatusImpl
					.isShufflePresentSelection(conn, subID, callerID, rbtType);
			if (shuffleSel == null)
				return false;
			Category category = getCategory(shuffleSel.categoryID());
			if (category == null || category.getType() != SHUFFLE)
				return false;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	private boolean doRetailerCheck() {
		return false;
	}

	public Date getNextDate(String chargeperiod) {
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
		return calendar1.getTime();
	}

	public SubscriberStatus[] getSubscriberCallerSelectionsInLoop(String subID,
			String callerID) {
		return getSubscriberCallerSelectionsInLoop(subID, callerID, 0);
	}

	public SubscriberStatus[] getSubscriberCallerSelectionsInLoop(String subID,
			String callerID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSubscriberCallerSelectionsInLoop(
					conn, subID, callerID, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public SubscriberStatus[] getPersonalCallerIDSelections(String subID,
			String callerID) {
		return getPersonalCallerIDSelections(subID, callerID, 0);
	}

	public SubscriberStatus[] getPersonalCallerIDSelections(String subID,
			String callerID, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getPersonalCallerIDSelections(conn,
					subID, callerID, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ChargeClassMap[] getChargeClassMapsForModeType(String mode,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ChargeClassMapImpl.getChargeClassMapsForModeType(conn, mode,
					type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public boolean convertWeeklySelectionsClassTypeToMonthly(
			String subscriberID, String initClass, String finalClass) {
		return convertWeeklySelectionsClassTypeToMonthly(subID(subscriberID),
				initClass, finalClass, 0);
	}

	public boolean convertWeeklySelectionsClassTypeToMonthly(
			String subscriberID, String initClass, String finalClass,
			int rbtType) {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.convertSelectionClassType(conn,
					subID(subscriberID), initClass, finalClass, null, null,
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean upgradeSelectionClassType(String subscriberID,
			String initClass, String finalClass, int rbtType, String refID,
			String mode) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.convertSelectionClassType(conn,
					subID(subscriberID), initClass, finalClass, refID, mode,
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean upgradeSelectionClassType2(String subscriberID,
			String initClass, String finalClass, int rbtType, String refID,
			String mode) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateSelectionClassType(conn,
					subID(subscriberID), initClass, finalClass, refID, mode,
					rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean isSubActive(Subscriber sub) {
		if (sub == null)
			return false;

		int rbtType = 0;

		if (sub != null)
			rbtType = sub.rbtType();

		if ((rbtType == 0 && (sub.rbtType() == 0 || sub.rbtType() == TYPE_RBT_RRBT))
				|| (rbtType == 2 && (sub.rbtType() == 2
						|| sub.rbtType() == TYPE_RBT_RRBT || sub.rbtType() == TYPE_SRBT_RRBT))
				|| (rbtType == 1 && (sub.rbtType() == 1 || sub.rbtType() == TYPE_SRBT_RRBT)))
			if (m_activeSubStatus.contains(sub.subYes()))
				return true;
		return false;
	}

	public boolean isSubDeactive(Subscriber sub) {
		if (sub != null && sub.subYes() != null)
			if (m_deActiveSubStatus.contains(sub.subYes()))
				return true;
		return false;
	}

	public int getAirtelSubscriberType(Subscriber subscriber) {
		if (subscriber != null) {
			String subClass = subscriber.subscriptionClass();
			if (isAdvanceRentalSubClass(subClass))
				return RBT_USER_TYPE_RENTAL;
			if (isLightPackSubClass(subClass))
				return RBT_USER_TYPE_LIGHT;
			if (isSamplingSubClass(subClass))
				return RBT_USER_TYPE_SAMPLING;
			if (isLifeTimeSubClass(subClass))
				return RBT_USER_TYPE_LIFE_TIME;
			if (isLowRentalSubClass(subClass))
				return RBT_USER_TYPE_LOW_RENTAL;
			if (isAlbumRentalSubClass(subClass))
				return RBT_USER_TYPE_ALBUM;
		}
		return RBT_USER_TYPE_NORMAL;
	}

	public boolean isAdvanceRentalSubClass(String subClass) {
		if (m_advancePacksList == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "ADVANCE_PACKS");
			if (tempParam != null)
				m_advancePacksList = tempParam.getValue();
			else
				m_advancePacksList = "";
		}
		if (m_advancePacksList != null)
			return (m_advancePacksList.indexOf(subClass) != -1);
		return false;
	}

	public boolean isLightPackSubClass(String subClass) {
		if (m_lightPacksList == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "LIGHT_PACKS");
			if (tempParam != null)
				m_lightPacksList = tempParam.getValue();
			else
				m_lightPacksList = "";
		}
		if (m_lightPacksList != null)
			return (m_lightPacksList.indexOf(subClass) != -1);
		return false;
	}

	public boolean isSamplingSubClass(String subClass) {
		if (m_samplingPacksList == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "SAMPLING_PACKS");
			if (tempParam != null)
				m_samplingPacksList = tempParam.getValue();
			else
				m_samplingPacksList = "";
		}
		if (m_samplingPacksList != null)
			return (m_samplingPacksList.indexOf(subClass) != -1);
		return false;
	}

	public boolean isLifeTimeSubClass(String subClass) {
		if (m_lifeTimePacksList == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "LIFE_TIME_PACKS");
			if (tempParam != null)
				m_lifeTimePacksList = tempParam.getValue();
			else
				m_lifeTimePacksList = "";
		}
		if (m_lifeTimePacksList != null)
			return (m_lifeTimePacksList.indexOf(subClass) != -1);
		return false;
	}

	public boolean isLowRentalSubClass(String subClass) {
		if (m_lowRentalPacksList == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "LOW_RENTAL_PACKS");
			if (tempParam != null)
				m_lowRentalPacksList = tempParam.getValue();
			else
				m_lowRentalPacksList = "";
		}
		if (m_lowRentalPacksList != null)
			return (m_lowRentalPacksList.indexOf(subClass) != -1);
		return false;
	}

	public boolean isAlbumRentalSubClass(String subClass) {
		getAlbumSubClass();
		if (m_albumSubClass != null)
			return (m_albumSubClass.indexOf(subClass) != -1);
		return false;
	}

	public String getAlbumSubClass() {
		if (m_albumSubClass == null) {
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "ALBUM_RENTAL_PACKS");
			if (tempParam != null)
				m_albumSubClass = tempParam.getValue();
			else
				m_albumSubClass = "";
		}
		return m_albumSubClass;
	}

	public Categories[] getGUIActiveCategories(String circleID, char prepaidYes) {
		return _rcm.getGUIActiveCategories(circleID, prepaidYes);
	}

	public String getSubscriberVcodeCCC(String sub_id, String src_sub_id,
			boolean useProxy, String proxyServerPort, String testStatus,
			ArrayList testNumbers, String testCircleId, HashMap urlDetails,
			int rbtType) {
		String vcode = "ERROR";
		if (sub_id != null && sub_id.length() == 10) {
			SitePrefix pr = Utility.getPrefix(subID(sub_id));
			Parameters nonOMPrefixParam = CacheManagerUtil
					.getParametersCacheManager().getParameter("GATHERER",
							"NON_ONMOBILE_PREFIX");
			String telPrefix = subID(sub_id).substring(0, getPrefixIndex());
			if (nonOMPrefixParam != null
					&& nonOMPrefixParam.getValue().indexOf(telPrefix) >= 0) {
				Parameters param = CacheManagerUtil.getParametersCacheManager()
						.getParameter("COMMON", "NORTHDB_URL");

				if (testStatus != null && testStatus.equalsIgnoreCase("true")
						&& testNumbers != null && testNumbers.contains(sub_id)
						&& testCircleId != null) {

					StringBuffer response = new StringBuffer();

					SiteURLDetails destUrlDetail = (SiteURLDetails) (urlDetails
							.get(testCircleId));
					String url = destUrlDetail.URL;
					url = Tools
							.findNReplace(url, "rbt_sms.jsp?", "rbt_gui.jsp");
					url = Tools.findNReplace(url, "rbt_sms.jsp", "rbt_gui.jsp");
					String params = "?request_value=vCode&SUB_ID=" + sub_id;

					if (Tools.callURL(url + params, new Integer(0), response,
							useProxy, proxyServerPort, true))
						vcode = response.toString().trim();
					else
						return "ERROR";

				} else {
					StringBuffer response = new StringBuffer();
					String jsp = "/tonecopy.jsp?dstSubscriberID=" + sub_id
							+ "&srcSubscriberID=" + src_sub_id;
					if (Tools.callURL(param.getValue() + jsp, new Integer(0),
							response, useProxy, proxyServerPort, true)) {
						String resp = response.toString().trim();
						if (resp.indexOf(":") > 0) {
							StringTokenizer st = new StringTokenizer(resp, ":");
							while (st.hasMoreTokens()) {
								String mssg = st.nextToken();
								if (mssg.equalsIgnoreCase("SUCCESS")) {
									return "rbt_" + st.nextToken() + "_rbt"
											+ ":" + "26";
								} else if (mssg.equalsIgnoreCase("ERROR")) {
									String ecode = st.nextToken();
									if (ecode.equalsIgnoreCase("130")
											|| ecode.equalsIgnoreCase("145")) {
										return "NOT_FOUND";
									} else
										return "ERROR";
								}
							}
						}
						return "ERROR";
					} else
						return "ERROR";
				}
			} else if (pr != null) {
				if (pr.getSiteUrl() == null) {
					vcode = getSubscriberDefaultVcode(sub_id, rbtType);
				} else {
					StringBuffer response = new StringBuffer();
					String url = pr.getSiteUrl();
					url = Tools
							.findNReplace(url, "rbt_sms.jsp?", "rbt_gui.jsp");
					url = Tools.findNReplace(url, "rbt_sms.jsp", "rbt_gui.jsp");
					String params = "?request_value=vCode&SUB_ID=" + sub_id;

					if (Tools.callURL(url + params, new Integer(0), response,
							useProxy, proxyServerPort, true))
						vcode = response.toString().trim();
					else
						return "ERROR";
				}
			} else
				return "NOT_VALID";
		} else {
			vcode = "NOT_VALID";
		}
		return vcode;
	}

	public String getBaseNumbers() {
		if (m_baseNumbers == null) {
			Parameters baseNoParameter = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"BASENUMBERS");
			if (baseNoParameter != null && baseNoParameter.getValue() != null)
				m_baseNumbers = baseNoParameter.getValue().trim();
		}

		return m_baseNumbers;
	}

	public String getCallbackBaseNumbers() {
		if (m_callbackBaseNumbers == null) {
			Parameters callBackBaseNoParameter = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"CALLBACK_BASENUMBERS");
			if (callBackBaseNoParameter != null
					&& callBackBaseNoParameter.getValue() != null)
				m_callbackBaseNumbers = callBackBaseNoParameter.getValue()
						.trim();
		}

		return m_callbackBaseNumbers;
	}

	public String getTollFreeMMNumner() {
		if (m_tollFreeMMNos == null) {
			Parameters tollFreeParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"TOLL_FREE_MM_NUMBERS");
			if (tollFreeParam != null && tollFreeParam.getValue() != null)
				m_tollFreeMMNos = tollFreeParam.getValue().trim();
		}

		return m_tollFreeMMNos;
	}

	public int getCallBackCategoryID(String baseNumber) {
		if (m_callBackCategoriesMap == null) {
			m_callBackCategoriesMap = new HashMap();
			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter(iRBTConstant.COMMON, "CALLBACK_CATEGORIES");
			if (parameter != null) {
				StringTokenizer tokenizer = new StringTokenizer(
						parameter.getValue(), ",");
				while (tokenizer.hasMoreTokens()) {
					String elem = tokenizer.nextToken();
					StringTokenizer tokenizer2 = new StringTokenizer(elem, ":");
					String number = tokenizer2.nextToken();
					Integer categoryID = new Integer(tokenizer2.nextToken());
					m_callBackCategoriesMap.put(number, categoryID);
				}
			}
		}

		if (m_callBackCategoriesMap.containsKey(baseNumber))
			return ((Integer) m_callBackCategoriesMap.get(baseNumber))
					.intValue();

		return -1;
	}

	public boolean isBlackListedForTNB(String subscriberID) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			String sql = "SELECT * FROM RBT_TNB_BLACKLIST_TABLE WHERE SUBSCRIBER_ID = '"
					+ subscriberID + "'";
			logger.info("RBT:: query = " + sql);

			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				logger.info("RBT:: returning true");
				return true;
			}
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			if (connection != null && statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("", e);
				}
				releaseConnection(connection);
			}
		}
		return false;
	}

	public void addToTNBBlackList(String subscriberID) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			String sql = "INSERT INTO RBT_TNB_BLACKLIST_TABLE VALUES('"
					+ subscriberID + "')";
			logger.info("RBT:: query = " + sql);
			int updateCnt = statement.executeUpdate(sql);
			logger.info("RBT:: updateCount = " + updateCnt);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			if (connection != null && statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("", e);
				}
				releaseConnection(connection);
			}
		}
	}

	private void initializeLTP() {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			Parameters param = CacheManagerUtil.getParametersCacheManager()
					.getParameter(COMMON, "LTP_ON_BASE_ACT");
			if (param != null && param.getValue() != null)
				m_isLTPOnForBaseAct = param.getValue().trim()
						.equalsIgnoreCase("true");

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_ON_BASE_REN");
			if (param != null && param.getValue() != null)
				m_isLTPOnForBaseRen = param.getValue().trim()
						.equalsIgnoreCase("true");

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_ON_SEL_ACT");
			if (param != null && param.getValue() != null)
				m_isLTPOnForSelAct = param.getValue().trim()
						.equalsIgnoreCase("true");

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_ON_SEL_REN");
			if (param != null && param.getValue() != null)
				m_isLTPOnForSelRen = param.getValue().trim()
						.equalsIgnoreCase("true");
			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "ADD_TO_DOWNLOADS");
			if (param != null && param.getValue() != null)
				m_addToDownloads = param.getValue().trim()
						.equalsIgnoreCase("true");

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					DAEMON, "RETAIN_DOWNLOAD_DCT");
			if (param != null && param.getValue() != null)
				m_retainDownloadsSubDct = param.getValue().trim()
						.equalsIgnoreCase("true");

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_ACT_MAP");
			if (param != null && param.getValue() != null) {
				m_ltpActMap = new Hashtable();
				StringTokenizer stkOut = new StringTokenizer(param.getValue()
						.trim(), ";");
				while (stkOut != null && stkOut.hasMoreTokens()) {
					StringTokenizer stkIn = new StringTokenizer(stkOut
							.nextToken().trim(), ",");
					String actBy = null;
					int ltpPoints = -1;
					if (stkIn != null && stkIn.hasMoreTokens())
						actBy = stkIn.nextToken().trim().toUpperCase();
					if (stkIn != null && stkIn.hasMoreTokens()) {
						try {
							ltpPoints = Integer.parseInt(stkIn.nextToken()
									.trim());
						} catch (Exception e) {
							ltpPoints = -1;
						}
					}
					if (actBy != null && ltpPoints > 0)
						m_ltpActMap.put(actBy, new Integer(ltpPoints));
				}
			}
			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_SEL_MAP");
			if (param != null && param.getValue() != null) {
				m_ltpSelMap = new Hashtable();
				StringTokenizer stkOut = new StringTokenizer(param.getValue()
						.trim(), ";");
				while (stkOut != null && stkOut.hasMoreTokens()) {
					StringTokenizer stkIn = new StringTokenizer(stkOut
							.nextToken().trim(), ",");
					String selBy = null;
					int ltpPoints = -1;
					if (stkIn != null && stkIn.hasMoreTokens())
						selBy = stkIn.nextToken().trim().toUpperCase();
					if (stkIn != null && stkIn.hasMoreTokens()) {
						try {
							ltpPoints = Integer.parseInt(stkIn.nextToken()
									.trim());
						} catch (Exception e) {
							ltpPoints = -1;
						}
					}
					if (selBy != null && ltpPoints > 0)
						m_ltpSelMap.put(selBy, new Integer(ltpPoints));
				}
			}

			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					COMMON, "LTP_ALBUM_MAP");
			if (param != null && param.getValue() != null) {
				m_ltpAlbumMap = new Hashtable();
				StringTokenizer stkOut = new StringTokenizer(param.getValue()
						.trim(), ";");
				while (stkOut != null && stkOut.hasMoreTokens()) {
					StringTokenizer stkIn = new StringTokenizer(stkOut
							.nextToken().trim(), ",");
					String selBy = null;
					int ltpPoints = -1;
					if (stkIn != null && stkIn.hasMoreTokens())
						selBy = stkIn.nextToken().trim().toUpperCase();
					if (stkIn != null && stkIn.hasMoreTokens()) {
						try {
							ltpPoints = Integer.parseInt(stkIn.nextToken()
									.trim());
						} catch (Exception e) {
							ltpPoints = -1;
						}
					}
					if (selBy != null && ltpPoints > 0)
						m_ltpAlbumMap.put(selBy, new Integer(ltpPoints));
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public String getLTPPoints(String activationInfo) {
		try {
			if (activationInfo == null)
				return "0";
			if (activationInfo.indexOf("|LTP:") > -1
					&& activationInfo.indexOf(":LTP|") > -1) {
				int firstIndex = activationInfo.indexOf("|LTP:");
				int secondIndex = activationInfo.indexOf(":LTP|");
				String ltpPoints = "0";
				if (firstIndex > -1 && secondIndex > firstIndex)
					ltpPoints = activationInfo.substring(firstIndex + 5,
							secondIndex);
				return ltpPoints;
			} else
				return "0";
		} catch (Exception e) {
			return "0";
		}
	}

	public String addLTPPoints(String activationInfo, int ltpPoints) {
		try {
			if (activationInfo == null)
				return "|LTP:" + ltpPoints + ":LTP|";
			if (activationInfo.indexOf("|LTP:") > -1
					&& activationInfo.indexOf(":LTP|") > -1) {
				int firstIndex = activationInfo.indexOf("|LTP:");
				int secondIndex = activationInfo.indexOf(":LTP|");
				String firstPart = activationInfo.substring(0, firstIndex);
				String secondPart = activationInfo.substring(secondIndex + 5,
						activationInfo.length());
				int initLtpPoints = 0;
				String strLtpPoints = null;
				if (firstIndex > -1 && secondIndex > firstIndex) {
					strLtpPoints = activationInfo.substring(firstIndex + 5,
							secondIndex);
					try {
						initLtpPoints = Integer.parseInt(strLtpPoints);
						initLtpPoints += ltpPoints;
					} catch (Exception e) {
						initLtpPoints = ltpPoints;
					}
				}
				return firstPart + secondPart + "|LTP:" + initLtpPoints
						+ ":LTP|";
			} else
				return activationInfo + "|LTP:" + ltpPoints + ":LTP|";
		} catch (Exception e) {
			return activationInfo;
		}
	}

	public String redeemLTPPoints(String subID, String ltpPointsToDeductStr) {
		try {
			if (subID == null)
				return "FAILURE:LTP_FORMAT_ERROR";
			Subscriber sub = getSubscriber(subID);
			if (sub == null || !isSubActive(sub))
				return "FAILURE:DEACTIVE_SUBSCRIBER";
			String activationInfo = sub.activationInfo();
			if (activationInfo == null)
				return "FAILURE:NOT_ENOUGH_LTP";
			if (activationInfo.indexOf("|LTP:") > -1
					&& activationInfo.indexOf(":LTP|") > -1) {
				int firstIndex = activationInfo.indexOf("|LTP:");
				int secondIndex = activationInfo.indexOf(":LTP|");
				String firstPart = activationInfo.substring(0, firstIndex);
				String secondPart = activationInfo.substring(secondIndex + 5,
						activationInfo.length());
				int initLtpPoints = 0;
				int ltpPointsToDeduct = 0;
				String strLtpPoints = null;
				if (firstIndex > -1 && secondIndex > firstIndex) {
					strLtpPoints = activationInfo.substring(firstIndex + 5,
							secondIndex);
					try {
						initLtpPoints = Integer.parseInt(strLtpPoints);
						ltpPointsToDeduct = Integer
								.parseInt(ltpPointsToDeductStr);
						if (initLtpPoints >= ltpPointsToDeduct)
							initLtpPoints -= ltpPointsToDeduct;
						else
							return "FAILURE:NOT_ENOUGH_LTP";
					} catch (Exception e) {
						return "FAILURE:LTP_FORMAT_ERROR";
					}
				}
				setActivationInfo(subID, firstPart + secondPart + "|LTP:"
						+ initLtpPoints + ":LTP|");
				return "SUCCESS:" + initLtpPoints;
			} else
				return "FAILURE:NOT_ENOUGH_LTP";
		} catch (Exception e) {
			return "FAILURE:LTP_FORMAT_ERROR";
		}

	}

	private void initNationalUrl() {
		Parameters p = CacheManagerUtil.getParametersCacheManager()
				.getParameter("DAEMON", "NATIONAL_URL");
		if (p != null && p.getValue() != null) {
			m_redirectNationalURL = p.getValue().trim();
			m_redirectNationalURL = Tools.findNReplaceAll(
					m_redirectNationalURL, "rbt_sms.jsp", "");
			m_redirectNationalURL = Tools.findNReplaceAll(
					m_redirectNationalURL, "?", "");

		}
	}

	public void getClipCache(Hashtable<String, String> promoIDMap,
			Hashtable<String, ClipMinimal> clipMinimalMap,
			Hashtable<String, String> clipIDMap,
			Hashtable<String, ArrayList<ClipMap>> clipMap,
			Hashtable<String, String> clipWavFileMap) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			ClipsImpl.getClipMapCache(conn, clipIDMap, clipMap);
			ClipsImpl.getClipMinimalCache(conn, promoIDMap, clipMinimalMap,
					clipWavFileMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

	}

	public boolean getClipCacheForAttribute(int type, String attr,
			Hashtable<String, String> promoIDMap,
			Hashtable<String, ClipMinimal> clipMinimalMap,
			Hashtable<String, String> clipIDMap,
			Hashtable<String, ArrayList<ClipMap>> clipMap,
			Hashtable<String, String> clipWavFileMap) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean result = false;
		try {
			int id = ClipsImpl.getClipMinimalCacheForAttribute(conn, type,
					attr, promoIDMap, clipMinimalMap, clipWavFileMap);
			if (id != -1)
				result = ClipsImpl.getClipMapCacheForID(conn, id, clipIDMap,
						clipMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return result;
	}

	public void getTataClipPromoIdCache(Hashtable<String, String> promoIDMap) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			PromoMasterImpl.getPromoIDClipIDMap(conn, promoIDMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Date getMaxClipStartTime() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ClipsImpl.getMaxStartDate(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Date getMaxCategoryStartTime() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return CategoriesImpl.getMaxStartDate(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public static int getSubscriptionPeriod(String subPeriodStr, String subClass) {
		int multFactor = 30;
		char ch = subPeriodStr.charAt(0);
		if (ch == 'D' || ch == 'd')
			multFactor = 1;
		else if (ch == 'M' || ch == 'm')
			multFactor = 30;
		else if (ch == 'Y' || ch == 'y')
			multFactor = 365;
		else {
			logger.info("RBT::subscription period not configured properly for "
					+ subClass);
			multFactor = 30;
		}

		int mult = Integer.parseInt(subPeriodStr.substring(1));
		int subPeriod = multFactor * mult;
		return subPeriod;
	}

	public String getSMSDBURL() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		if (m_smsDBURL == null) {
			Parameters smsDBURLParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(SMS, "DB_URL");
			if (smsDBURLParam != null){
				m_smsDBURL = smsDBURLParam.getValue().trim();
				//Changes done for URL Encryption and Decryption
				if (isEncryptionModel()) {
					m_smsDBURL = URLEncryptDecryptUtil
							.decryptAndMerge(m_smsDBURL);
				}
			}
		}

		return m_smsDBURL;
	}

	public String getSMSSenderID() {
		if (m_smsSenderID == null) {
			Parameters smsSenderIDParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(SMS, "SENDER_ID");
			if (smsSenderIDParam != null)
				m_smsSenderID = smsSenderIDParam.getValue().trim();
		}

		return m_smsSenderID;
	}

	private void initTrialWithActivations() {
		String trialStr = null;
		Parameters p = CacheManagerUtil.getParametersCacheManager()
				.getParameter(COMMON, "TRIAL_WITH_ACT");
		if (p == null || p.getValue() == null)
			return;
		trialStr = p.getValue().trim();
		if (trialStr == null || trialStr.length() <= 0)
			return;
		StringTokenizer stkParent = new StringTokenizer(trialStr, ";");
		if (stkParent == null)
			return;
		while (stkParent.hasMoreTokens()) {

			try {
				StringTokenizer stkChild = new StringTokenizer(stkParent
						.nextToken().trim(), ",");
				if (stkChild == null)
					continue;
				String trialClass = null;
				Integer trialInt = null;
				if (stkChild.hasMoreTokens())
					trialClass = stkChild.nextToken().trim().toUpperCase();
				if (stkChild.hasMoreTokens())
					trialInt = new Integer(stkChild.nextToken().trim());
				if (trialClass != null && trialInt != null) {
					m_TrialWithActivations.add(trialClass);
					m_trialClassDaysMap.put(trialClass, trialInt);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public boolean disablePressStarIntro(Subscriber subscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean result = updateExtraInfoAndPlayerStatus(subscriber,
				EXTRA_INFO_INTRO_PROMPT_FLAG, DISABLE_PRESS_STAR_INTRO, "A");
		releaseConnection(conn);
		return result;
	}

	public boolean updateSubUpdatedAtPlayer(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.updateSubUpdatedAtPlayer(conn, subID(subID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// Modified for MNP
	public List<Subscriber> getSubsToUpdatePlayer(int fetchSize, String circleID, boolean isRBT2) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			// RBT-16004 isRBT2 Added for checking rbt 2
			return SubscriberImpl.getSubsToUpdatePlayer(conn, fetchSize,
					circleID , isRBT2);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	private static boolean initOzoneConnectionPool(String dbUrl, int nConn,
			String poolName, boolean isDefault, String timeOut,
			String providerClass) {

		String config = getConfigString(dbUrl, nConn, poolName, isDefault,
				timeOut, providerClass);
		BootStrap.initDBServices(config);
		return true;
	}

	public boolean enablePressStarIntro(Subscriber subscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return updateExtraInfoAndPlayerStatus(subscriber,
					EXTRA_INFO_INTRO_PROMPT_FLAG, ENABLE_PRESS_STAR_INTRO, "A");
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updatePressStarIntro(String subscriberID,
			Subscriber subscriber, boolean isEnabled) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return updateExtraInfoAndPlayerStatus(subscriber,
					EXTRA_INFO_INTRO_PROMPT_FLAG,
					(isEnabled ? ENABLE_PRESS_STAR_INTRO
							: DISABLE_PRESS_STAR_INTRO), "A");
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public static String getConfigString(String dbUrl, int nConn,
			String poolName, boolean isDefault, String timeOut,
			String providerClass) {
		String trackLeaksAndQueries = "false";
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
			trackLeaksAndQueries = resourceBundle
					.getString("TRACK_LEAKS_AND_QUERIES").toLowerCase().trim();
		} catch (Exception e) {
		}

		String tempConn = "false";
		String pool = "name =\"" + poolName + "\" default=\"true\"";
		if (!isDefault) {
			tempConn = "true";
			pool = "name =\"" + poolName + "\"";
		}
		String config = "<Database trackleaksandqueries=\""
				+ trackLeaksAndQueries + "\" enabletempconnections=\""
				+ tempConn + "\">" + "<Instance ";
		if (providerClass != null) {
			config += "driver=\"" + providerClass + "\" ";
		}
		config += "connectionstring=\"" + dbUrl + "\" db-pool-max-size=\""
				+ nConn + "\" " + pool + " db-query-timeout-sec=\"" + timeOut
				+ "\" thread-affinity=\"true\"/>" + "</Database>";
		return config;
	}

	public void cacheCategories(Hashtable categoryTable, Hashtable mapTable) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			CategoriesImpl.cacheCategoriesTable(conn, categoryTable);
			CategoriesImpl.cacheCategoryMapTable(conn, mapTable);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public Categories getActiveCategoriesForCatSearch(String smsAlias,
			String circleId, char prepaidYes) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return CategoriesImpl.getActiveCategoriesForCatSearch(conn,
					smsAlias, circleId, prepaidYes);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Categories[] getChildCategories(String categoryID, String circleId,
			char prepaidYes) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return CategoriesImpl.getChildCategories(conn, categoryID,
					circleId, prepaidYes);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public void setViralSMS(String subscriberID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					ViralSMSNewImpl.updateSetTime(conn, subID(subscriberID),
							type);
					return;
				}
			}
			ViralSMSTableImpl.updateSetTime(conn, subID(subscriberID), type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public boolean updateViralPromotionTypeBySubscriberIDAndType(
			String newType, String subscriberID, String smsType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (viralSmsTypeListForNewTable != null && newType != null
					&& smsType != null) {
				if (viralSmsTypeListForNewTable.contains(newType)
						&& viralSmsTypeListForNewTable.contains(smsType)) {
					return ViralSMSNewImpl
							.updateViralPromotionTypeBySubscriberIDAndType(
									conn, newType, subID(subscriberID), smsType);
				}
			}

			return ViralSMSTableImpl
					.updateViralPromotionTypeBySubscriberIDAndType(conn,
							newType, subID(subscriberID), smsType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void setViralSMS(String subscriberID, String type, String clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					ViralSMSNewImpl.setClipId(conn, subID(subscriberID), type,
							clipId);
					return;
				}
			}
			ViralSMSTableImpl
					.setClipId(conn, subID(subscriberID), type, clipId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public int getPrefixIndex() {
		if (prefixIndex == 0) {
			Parameters prefixIndexParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(COMMON,
							"PREFIX_INDEX");
			if (prefixIndexParam != null) {
				try {
					prefixIndex = Integer.parseInt(prefixIndexParam.getValue()
							.trim());
				} catch (Exception e) {
					prefixIndex = 4;
				}
			} else
				prefixIndex = 4;
		}
		return prefixIndex;
	}

	public PromoMaster getPromoCodeByPromoType(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return PromoMasterImpl.getPromoCodeByPromoType(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Categories[] getOverrideShuffles() {
		return _rcm.getOverrideShuffles();
	}

	public boolean refreshClipCache() {
		return _rcm.refreshCacheForModule("ClipCache", false);
	}

	public void refreshCache() {
		try {
			_rcm.refreshCacheNoCheck();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public SubscriberStatus[] getAllSubscriberSelectionRecordsNotDeactivated() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl
					.getAllSubscriberSelectionRecordsNotDeactivated(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads[] getAllSubscriberDownloadRecordsNotDeactivated() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl
					.getAllSubscriberDownloadRecordsNotDeactivated(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList getLocalPlayerIP() {

		List<SitePrefix> allSitePrefixList = CacheManagerUtil
				.getSitePrefixCacheManager().getAllSitePrefix();

		ArrayList playerIPs = new ArrayList();
		ArrayList allPlayerIPConfig = new ArrayList();
		for (int i = 0; i < allSitePrefixList.size(); i++) {
			if (allSitePrefixList.get(i).getSiteUrl() == null
					&& allSitePrefixList.get(i).getPlayerUrl() != null) {
				StringTokenizer diffIpStk = new StringTokenizer(
						allSitePrefixList.get(i).getPlayerUrl(), ",");
				while (diffIpStk.hasMoreTokens()) {
					String thisIPToken = diffIpStk.nextToken();
					StringTokenizer ipStk = new StringTokenizer(thisIPToken);
					String thisIP = ipStk.nextToken();
					if (!playerIPs.contains(thisIP)) {
						playerIPs.add(thisIP);
						allPlayerIPConfig.add(thisIPToken);
					}
				}
			}
		}
		if (allPlayerIPConfig.size() > 0)
			return allPlayerIPConfig;
		else
			return null;

	}

	public ArrayList getUpdateToToBeDeletedDownloads(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getUpdateToToBeDeletedDownloads(
					conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String getCountryPrefix() {
		return m_countryCodePrefix;
	}

	public boolean deactivateSubscriberRecords(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy, String wavFile) {
		return deactivateSubscriberRecords(subscriberID, callerID, status,
				fromTime, toTime, smDeactivation, deactBy, wavFile, 0);
	}

	public boolean deactivateSubscriberRecords(String subscriberID,
			String callerID, int status, int fromTime, int toTime,
			boolean smDeactivation, String deactBy, String wavFile, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deactivateSubscriberRecords(conn,
					subID(subscriberID), subID(callerID), status, fromTime,
					toTime, smDeactivation, deactBy, wavFile, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String smSelectionSuspend(String strSubID, String refID,
			char newLoopStatus, int rbtType, String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			success = SubscriberStatusImpl.smSelectionSuspend(conn,
					subID(strSubID), refID, newLoopStatus, rbtType, circleId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return success ? m_success : m_failure;
	}

	public boolean isSelSuspended(String subscriberID, String callerID) {
		return isSelSuspended(subscriberID, callerID, 0);
	}

	public boolean isSelSuspended(String subscriberID, String callerID,
			int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.isSelSuspended(conn,
					subID(subscriberID), subID(callerID), rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateSubscriberToGrace(String subscriberID,
			Date nextRetryDate, String actInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberImpl.updateSubscriberToGrace(conn,
					subID(subscriberID), nextRetryDate, actInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ArrayList getActivationGraceRecords(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getActivationGraceRecords(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateSelectionToGrace(String subscriberID, String callerID,
			String subscriberWavFile, Date nextRetryDate, String selInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateSelecionToGrace(conn,
					subID(subscriberID), callerID, subscriberWavFile,
					nextRetryDate, selInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ArrayList getSelectionGraceRecords(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSelectionGraceRecords(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String insertBulkSelectionTask(String filename, String actBy,
			String subStrClass, String selStrClass, String actInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl.insert(conn, filename, actBy,
					subStrClass, selStrClass, actInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RbtBulkSelectionTask[] getBulkSelectionTasks() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RbtBulkSelectionTaskImpl.getBulkSelectionTasks(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String deleteBulkSelectionTask(int fileID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl.delete(conn, fileID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String updateProcessedTimeForTask(String filename) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl.updateProcessTime(conn, filename);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String updateBulkSelectionTaskStatus(String filename, String status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl
					.updateStatus(conn, filename, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String getFileFromFileID(int fileID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RbtBulkSelectionTaskImpl.getFilename(conn, fileID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String getBulkSelectionTaskStatus(String filename) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RbtBulkSelectionTaskImpl.getStatus(conn, filename);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String getActivationInfoTask(String filename) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl.getActInfo(conn, filename);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String updateActivationInfoTask(String filename, String actInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RbtBulkSelectionTaskImpl.updateActInfo(conn, filename,
					actInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Subscriber[] getSubsTosendSMS(String subClass, int smsDay,
			int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberImpl.getSubsTosendSMS(conn, subClass, smsDay,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String canBeGifted(String subscriberId, String callerId,
			String contentID) {
		if (isValidOperatorPrefix(callerId))
			return GIFT_SUCCESS;

		return GIFT_FAILURE_GIFTEE_INVALID;
	}

	public HashMap<String, String> getExtraInfoMap(Subscriber subscriber) {
		if (subscriber == null) {
			logger.warn("subscriber is null");
			return null;
		}
		HashMap<String, String> attributeMapFromXML = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		logger.info("Subscriber: " + subscriber.subID() + ", ExtraInfo: "
				+ attributeMapFromXML);
		return attributeMapFromXML;
	}

	public boolean updateExtraInfo(String subId, String name, String value) {
		Connection conn = null;
		try {
			conn = getConnection();
			if (conn == null)
				return false;
			if (name == null || subId == null)
				return false;
			Subscriber subscriber = getSubscriber(subId);
			if (subscriber == null)
				return false;
			String extraInfo = DBUtility.setXMLAttribute(
					subscriber.extraInfo(), name, value);
			boolean result = SubscriberImpl.updateExtraInfo(conn, subId,
					extraInfo);
			return result;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateExtraInfo(String subId, String extraInfo) {
		Connection conn = null;
		try {
			conn = getConnection();
			if (conn == null)
				return false;
			boolean result = SubscriberImpl.updateExtraInfo(conn, subId,
					extraInfo);
			return result;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateExtraInfoAndPlayerStatus(Subscriber subscriber,
			String name, String value, String playerStatus) {
		Connection conn = null;
		try {
			if (subscriber == null)
				return false;

			conn = getConnection();
			if (conn == null)
				return false;

			if (name == null)
				return false;

			String extraInfo = DBUtility.setXMLAttribute(
					subscriber.extraInfo(), name, value);
			if (value.equals(ENABLE_PRESS_STAR_INTRO))
				extraInfo = DBUtility.removeXMLAttribute(extraInfo,
						iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT);

			boolean result = SubscriberImpl.updateExtraInfoAndPlayerStatus(
					conn, subscriber.subID(), extraInfo, playerStatus);
			return result;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateExtraInfoAndPlayerStatus(String subId,
			String extraInfo, String playerStatus) {
		Connection conn = null;
		try {
			conn = getConnection();
			if (conn == null)
				return false;

			boolean result = SubscriberImpl.updateExtraInfoAndPlayerStatus(
					conn, subId, extraInfo, playerStatus);
			return result;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updatePlayerStatus(String subId, String playerStatus) {

		Connection conn = null;
		try {
			conn = getConnection();
			if (conn == null)
				return false;
			if (subId == null)
				return false;
			boolean result = SubscriberImpl.updatePlayerStatus(conn, subId,
					playerStatus);
			return result;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	private CosDetails getCosForMode(String circleID, String mode,
			String prepaidYes, String subscriptionClass) {
		List<CosDetails> cosList = CacheManagerUtil.getCosDetailsCacheManager()
				.getAllActiveCosDetails(circleID, prepaidYes);
		if (cosList == null || cosList.size() == 0)
			return null;

		CosDetails cos = null;

		// checks for the COS with same subClass, in the given circleID and
		// circleID="ALL"
		if (subscriptionClass != null) {
			cos = getCosDetailsForSubClassInternal(cosList, circleID, mode,
					subscriptionClass);
			if (cos != null)
				return cos;

			cos = getCosDetailsForSubClassInternal(cosList, "ALL", mode,
					subscriptionClass);
			if (cos != null)
				return cos;
		}

		// checks for the COS with same circleID and same mode, if not
		// mode="ALL" and same circleID
		cos = getCosDetailsForModeInternal(cosList, circleID, mode);
		if (cos != null)
			return cos;

		// checks for the COS with same mode and circleID="ALL", if not
		// mode="ALL" and circleID="ALL"
		cos = getCosDetailsForModeInternal(cosList, "ALL", mode);

		logger.info("RBT:: COS = " + cos);
		return cos;
	}

	/**
	 * @param cosList
	 * @param circleId
	 * @param mode
	 * @return CosDetails
	 * 
	 */
	private CosDetails getCosDetailsForModeInternal(List<CosDetails> cosList,
			String circleId, String mode) {
		CosDetails cos = null;
		for (CosDetails cosDetails : cosList) {
			if (!cosDetails.isDefaultCos()
					&& cosDetails.getCircleId().equalsIgnoreCase(circleId)
					&& cosDetails.getCosType() == null) {
				String accessMode = cosDetails.getAccessMode();
				if (accessMode.equalsIgnoreCase("ALL")) {
					cos = cosDetails;
					continue;
				}

				String[] modes = accessMode.split(",");
				for (String cosMode : modes) {
					if (cosMode.equalsIgnoreCase(mode))
						return cosDetails;
				}
			}
		}
		logger.info("RBT:: cos = " + cos);
		return cos;
	}

	/**
	 * @param cosList
	 * @param circleId
	 * @param mode
	 * @param subscriptionClass
	 * @return CosDetails
	 */
	private CosDetails getCosDetailsForSubClassInternal(
			List<CosDetails> cosList, String circleId, String mode,
			String subscriptionClass) {
		CosDetails cos = null;
		for (CosDetails cosDetails : cosList) {
			if (!cosDetails.isDefaultCos()
					&& cosDetails.getCircleId().equalsIgnoreCase(circleId)
					&& "SUB_CLASS".equalsIgnoreCase(cosDetails.getCosType())
					&& cosDetails.getSubscriptionClass().equalsIgnoreCase(
							subscriptionClass)) {
				String accessMode = cosDetails.getAccessMode();
				if (accessMode.equalsIgnoreCase("ALL")) {
					cos = cosDetails;
					continue;
				}

				String[] modes = accessMode.split(",");
				for (String cosMode : modes) {
					if (cosMode.equalsIgnoreCase(mode))
						return cosDetails;
				}
			}
		}
		logger.info("RBT:: cos = " + cos);
		return cos;
	}

	public boolean enableOverlay(Subscriber subscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return updateExtraInfoAndPlayerStatus(subscriber,
					EXTRA_INFO_INTRO_OVERLAY_FLAG, ENABLE_INTRO_OVERLAY, "A");
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean disableOverlay(Subscriber subscriber) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return updateExtraInfoAndPlayerStatus(subscriber,
					EXTRA_INFO_INTRO_OVERLAY_FLAG, DISABLE_INTRO_OVERLAY, "A");
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int cleanSubscribers(float period, boolean useSM) {
		return cleanOldSubscribers(period, useSM);
	}

	public void setNextChargingDate(String subscriberID, Date date) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberImpl.setNextChargingDate(conn, subID(subscriberID), date);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	public HashMap<String, String> getSubscriberInfo(String subscriberID) {
		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();
		subscriberInfoMap.put("STATUS", "VALID");
		logger.info("RBT:: subscriberInfoMap = " + subscriberInfoMap);
		return subscriberInfoMap;
	}

	public HashMap<String, String> getSubscriberInfo(String subscriberID,
			String result) {
		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();
		subscriberInfoMap.put("STATUS", "VALID");
		logger.info("RBT:: subscriberInfoMap = " + subscriberInfoMap);
		return subscriberInfoMap;
	}

	/**
	 * Subscribers with PLAYER_STATUS=A and SUBSCRIPTION_YES=B
	 * 
	 * @param fetchSize
	 * @return
	 */
	public ArrayList<Subscriber> playerGetActivatedSubs(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberImpl.playerGetActivatedSubs(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/**
	 * Returns
	 * 
	 * @param fetchSize
	 * @return ArrayList of SubscriberStatus
	 */
	public ArrayList<SubscriberStatus> playerGetActivatedSels(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.playerGetActivatedSels(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList<SubscriberStatus> playerGetRemovedSels(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.playerGetRemovedSels(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateLoopStatus(SubscriberStatus selection, char newStatus,
			String selInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.updateLoopStatus(conn, selection,
					newStatus, selInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String addCallerInGroup(String subscriberID, int groupID,
			String callerID, String callerName, String preGroupID,
			String preGroupName) {
		String result = null;
		String res = null;
		subscriberID = subID(subscriberID);
		if (groupID == -1) {
			if (preGroupID != null)
				result = addGroupForSubscriberID(preGroupID, preGroupName,
						subscriberID, null);
			else
				return CALLER_NOT_ADDED_INTERNAL_ERROR;

			if (result.equals(SAME_PREGROUP_EXISTS_FOR_CALLER)
					|| result.equals(GROUP_ADDED_SUCCESFULLY)) {
				Groups group = getGroupByPreGroupID(preGroupID, subscriberID);
				if (group != null
						&& group.status() != null
						&& !(group.status().equals("X") || group.status()
								.equals("D")))

					res = addCallerInGroup(subscriberID, group.groupID(),
							callerID, callerName);
			} else {
				return result;
			}
		} else {
			res = addCallerInGroup(subscriberID, groupID, callerID, callerName);
		}

		return res;
	}

	public String addMultipleCallerInGroup(String subscriberID, int groupID,
			String callerID, String callerName, String preGroupID,
			String preGroupName) {
		String result = null;
		String res = null;
		subscriberID = subID(subscriberID);
		if (groupID == -1) {
			if (preGroupID != null)
				result = addGroupForSubscriberID(preGroupID, preGroupName,
						subscriberID, null);
			else
				return CALLER_NOT_ADDED_INTERNAL_ERROR;

			if (result.equals(SAME_PREGROUP_EXISTS_FOR_CALLER)
					|| result.equals(GROUP_ADDED_SUCCESFULLY)) {
				Groups group = getGroupByPreGroupID(preGroupID, subscriberID);
				if (group != null
						&& group.status() != null
						&& !(group.status().equals("X") || group.status()
								.equals("D")))
					res = addMultipleCallerInGroup(group.groupID(),
							subscriberID, callerID, callerName);
			} else {
				return result;
			}
		} else {
			res = addMultipleCallerInGroup(groupID, subscriberID, callerID,
					callerName);
		}

		return res;
	}

	public String addCallerInGroup(int groupID, String callerID,
			String callerName, String preGroupID, String preGroupName) {
		String result = null;
		String res = null;
		logger.info("Adding caller into group. groupId: " + groupID
				+ ", callerID: " + callerID + ", callerName: " + callerName
				+ ", preGroupID: " + preGroupID + ", preGroupName: "
				+ preGroupName);
		if (groupID == -1) {
			if (preGroupID != null)
				result = addGroup(preGroupID, preGroupName, null);
			else
				return CALLER_NOT_ADDED_INTERNAL_ERROR;

			if (result.equals(SAME_PREGROUP_EXISTS_FOR_CALLER)
					|| result.equals(GROUP_ADDED_SUCCESFULLY)) {
				String subscriberID = null;
				Groups group = getGroupByPreGroupID(preGroupID, subscriberID);
				if (group != null
						&& group.status() != null
						&& !(group.status().equals("X") || group.status()
								.equals("D"))) {
					String[] callerIds = callerID.split(",");
					for (String callerId : callerIds) {
						res = addCallerInGroup(group.groupID(), callerId,
								callerName);
					}
				}
			} else {
				return result;
			}
		} else {
			String[] callerIds = callerID.split(",");
			for (String callerId : callerIds) {
				res = addCallerInGroup(groupID, callerId, callerName);
			}
		}
		logger.info("Added caller into group. groupId: " + groupID
				+ ", callerID: " + callerID + ", res: " + res);
		return res;
	}

	public String addAffiliateCallerInGroup(int groupID, String callerID,
			String callerName, String preGroupID, String preGroupName,
			String optName) {
		String result = null;
		String res = null;
		logger.info("Adding caller into group. groupId: " + groupID
				+ ", callerID: " + callerID + ", callerName: " + callerName
				+ ", preGroupID: " + preGroupID + ", preGroupName: "
				+ preGroupName);
		if (groupID == -1) {
			if (preGroupID != null)
				result = addGroup(preGroupID, preGroupName, null);
			else
				return CALLER_NOT_ADDED_INTERNAL_ERROR;

			if (result.equals(SAME_PREGROUP_EXISTS_FOR_CALLER)
					|| result.equals(GROUP_ADDED_SUCCESFULLY)) {
				String subscriberID = null;
				RDCGroups group = getRDCGroupByPreGroupID(preGroupID,
						subscriberID);
				if (group != null
						&& group.status() != null
						&& !(group.status().equals("X") || group.status()
								.equals("D"))) {
					String[] callerIds = callerID.split(",");
					for (String callerId : callerIds) {
						res = addAffiliateCallerInGroup(group.groupID(),
								callerId, callerName, optName);
					}
				}
			} else {
				return result;
			}
		} else {
			String[] callerIds = callerID.split(",");
			for (String callerId : callerIds) {
				res = addAffiliateCallerInGroup(groupID, callerId, callerName,
						optName);
			}
		}
		logger.info("Added caller into group. groupId: " + groupID
				+ ", callerID: " + callerID + ", res: " + res);
		return res;
	}

	public String addCallerInGroup(String subscriberID, int groupID,
			String callerID, String callerName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			subscriberID = subID(subscriberID);

			Groups group = GroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return CALLER_NOT_ADDED_INTERNAL_ERROR;

			// Checking Max callers in Group
			GroupMembers[] groupMembers = GroupMembersImpl
					.getMembersForGroupID(conn, groupID);
			if (groupMembers != null) {
				Parameters maxCallersInGroupParam = CacheManagerUtil
						.getParametersCacheManager().getParameter(COMMON,
								"MAX_CALLER_ALLOWED_IN_GROUPS", "30");
				int maxCallersInGroup = Integer.parseInt(maxCallersInGroupParam
						.getValue());
				if (groupMembers.length >= maxCallersInGroup)
					return MAX_CALLER_PRESENT_IN_GROUP;
			}

			// Checking callerID is already member of a group
			Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
					subscriberID);
			if (groups != null && groups.length > 0) {
				int[] groupIDs = new int[groups.length];
				for (int i = 0; i < groups.length; i++) {
					groupIDs[i] = groups[i].groupID();
				}
				GroupMembers groupMember = GroupMembersImpl
						.getMemberFromGroups(conn, callerID, groupIDs);
				if (groupMember != null) {
					for (Groups groupObj : groups) {
						if (groupMember.groupID() == groupObj.groupID()) {
							if (group.preGroupID() != null
									&& groupObj.preGroupID().equals("99")) // Blocked
																			// Caller
								return ALREADY_BLOCKED;
							else
								return CALLER_ALREADY_PRESENT_IN_GROUP;
						}
					}
				}
			}

			// Checking personal selection exists for callerID
			SubscriberStatus[] subscriberStatus = SubscriberStatusImpl
					.getAllSubscriberSelectionRecordsNotDeactivated(conn,
							subscriberID, null, 0);
			if (subscriberStatus != null) {
				for (SubscriberStatus selection : subscriberStatus) {
					if (callerID.equals(selection.callerID())) {
						if (group.preGroupID() != null
								&& group.preGroupID().equals("99")) {
							Parameters removeSelectionParam = CacheManagerUtil
									.getParametersCacheManager()
									.getParameter(
											COMMON,
											"REMOVE_SELECTION_FOR_BLOCKED_CALLER",
											"TRUE");
							if (removeSelectionParam.getValue()
									.equalsIgnoreCase("TRUE")) {
								// Adding caller to Blocked Callers Group, so
								// removing selections of callerID
								HashMap<String, String> whereClauseMap = new HashMap<String, String>();
								whereClauseMap.put("CALLER_ID", callerID);
								SubscriberStatusImpl
										.deactivateSubscriberSelections(conn,
												subscriberID,
												Collections.singletonMap(
														"DESELECTED_BY",
														"BLOCK"),
												whereClauseMap);
								break;
							}
						} else if (group.preGroupID() == null
								|| !group.preGroupID().equals("98")) {
							return ALREADY_PERSONALIZED_SELECTION_FOR_CALLER;
						}
					}
				}
			}

			String status = STATE_ACTIVATED;
			GroupMembers groupMember = GroupMembersImpl.insert(conn, groupID,
					callerID, callerName, status);
			if (groupMember != null) {
				if (group.preGroupID() != null
						&& group.preGroupID().equals("99")) {
					// Caller added to Blocked Callers Group and updating the
					// player status of Subscriber
					SubscriberImpl.updatePlayerStatus(conn, subscriberID, "A");
				} else if (subscriberStatus != null) {
					// Updating Subscriber Player staus if slection exists for
					// this group
					String groupIDStr = "G" + groupID;
					for (SubscriberStatus selection : subscriberStatus) {
						if (groupIDStr.equals(selection.callerID())) {
							SubscriberImpl.updatePlayerStatus(conn,
									subscriberID, "A");
							break;
						}
					}
				}

				return CALLER_ADDED_TO_GROUP;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return CALLER_NOT_ADDED_INTERNAL_ERROR;
	}

	public String addCallerInGroup(int groupID, String callerID,
			String callerName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {

			Groups group = GroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return CALLER_NOT_ADDED_INTERNAL_ERROR;
			// Checking Max callers in Group
			GroupMembers[] groupMembers = GroupMembersImpl
					.getMembersForGroupID(conn, groupID);
			if (groupMembers != null) {
				Parameters maxCallersInGroupParam = CacheManagerUtil
						.getParametersCacheManager().getParameter(COMMON,
								"MAX_CALLER_ALLOWED_IN_GROUPS", "30");
				int maxCallersInGroup = Integer.parseInt(maxCallersInGroupParam
						.getValue());
				if (groupMembers.length >= maxCallersInGroup)
					return MAX_CALLER_PRESENT_IN_GROUP;
			}

			String status = STATE_ACTIVATED;
			GroupMembers groupMember = GroupMembersImpl.insert(conn, groupID,
					callerID, callerName, status);

			if (null != groupMember) {
				logger.info("Added group member. groupID: " + groupID
						+ ", callerID: " + callerID
						+ ", returning: CALLER_ADDED_TO_GROUP");
				return CALLER_ADDED_TO_GROUP;
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return CALLER_NOT_ADDED_INTERNAL_ERROR;
	}

	public String addAffiliateCallerInGroup(int groupID, String callerID,
			String callerName, String optName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {

			RDCGroups group = RDCGroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return CALLER_NOT_ADDED_INTERNAL_ERROR;
			// Checking Max callers in Group
			RDCGroupMembers[] groupMembers = RDCGroupMembersImpl
					.getMembersForGroupID(conn, groupID);
			if (groupMembers != null) {
				Parameters maxCallersInGroupParam = CacheManagerUtil
						.getParametersCacheManager().getParameter(COMMON,
								"MAX_CALLER_ALLOWED_IN_GROUPS", "30");
				int maxCallersInGroup = Integer.parseInt(maxCallersInGroupParam
						.getValue());
				if (groupMembers.length >= maxCallersInGroup)
					return MAX_CALLER_PRESENT_IN_GROUP;
			}

			String status = STATE_ACTIVATED;
			RDCGroupMembers groupMember = RDCGroupMembersImpl.insert(conn,
					groupID, callerID, callerName, status, optName);

			if (null != groupMember) {
				logger.info("Added group member. groupID: " + groupID
						+ ", callerID: " + callerID
						+ ", returning: CALLER_ADDED_TO_GROUP");
				return CALLER_ADDED_TO_GROUP;
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return CALLER_NOT_ADDED_INTERNAL_ERROR;
	}

	public String addMultipleCallerInGroup(int groupID, String subscriberId,
			String callerID, String callerName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			String status = STATE_ACTIVATED;
			// 1 for add member to group
			boolean result = RbtTempGroupMembersImpl.batchInsert(conn, groupID,
					callerID, callerName, status, subscriberId, 1);

			if (result) {
				logger.info("Added group member. groupID: " + groupID
						+ ", callerID: " + callerID
						+ ", returning: CALLER_ADDED_TO_GROUP");
				return CALLER_ADDED_TO_GROUP;
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return CALLER_NOT_ADDED_INTERNAL_ERROR;
	}

	public boolean removeCallerFromGroup(String subscriberID, int groupID,
			String callerID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = false;
		try {
			deleted = GroupMembersImpl.deleteCallerFromGroup(conn, groupID,
					callerID);
			if (deleted) {
				Groups group = GroupsImpl.getGroup(conn, groupID);
				if (group.preGroupID() != null
						&& group.preGroupID().equals("99")) {
					// Caller removed from Blocked Callers Group and updating
					// the player status of Subscriber
					SubscriberImpl.updatePlayerStatus(conn, subscriberID, "A");
				} else {
					SubscriberStatus[] subscriberStatus = SubscriberStatusImpl
							.getAllSubscriberSelectionRecordsNotDeactivated(
									conn, subscriberID, null, 0);
					if (subscriberStatus != null) {
						// Updating Subscriber Player staus if slection exists
						// for this group
						String groupIDStr = "G" + groupID;
						for (SubscriberStatus selection : subscriberStatus) {
							if (groupIDStr.equals(selection.callerID())) {
								SubscriberImpl.updatePlayerStatus(conn,
										subscriberID, "A");
								break;
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deleted;
	}

	public List<RbtTempGroupMembers> getGroupMembersByGroupMemberStatus() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		return RbtTempGroupMembersImpl.getGroupMembersByGroupMemberStatus(conn);
	}

	public boolean removeMultipleCallerFromGroup(String subscriberID,
			int groupID, String callerID, String callerName) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = false;
		try {
			String status = STATE_ACTIVATED;
			// 2 - for delete memberfrom group
			deleted = RbtTempGroupMembersImpl.batchInsert(conn, groupID,
					callerID, callerName, status, subscriberID, 2);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deleted;
	}

	public boolean removeCallerFromRbtTempGroupMember(int groupID,
			String callerID, String subscriberId) {
		logger.info("Remove caller from group. groupID: " + groupID
				+ ", callerID: " + callerID + " subscriberId: " + subscriberId);
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deleted = false;
		try {
			deleted = RbtTempGroupMembersImpl.deleteCallerFromGroup(conn,
					groupID, callerID, subscriberId);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Removed caller from group. groupID: " + groupID
				+ ", callerID: " + callerID + " subscriberId: " + subscriberId
				+ ", deleted: " + deleted);
		return deleted;
	}

	public boolean removeCallerFromGroup(int groupID, String callerID) {
		logger.info("Remove caller from group. groupID: " + groupID
				+ ", callerID: " + callerID);
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deleted = false;
		try {
			String[] callerIds = callerID.split(",");
			for (String callerId : callerIds) {
				deleted = GroupMembersImpl.deleteCallerFromGroup(conn, groupID,
						callerId);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Removed caller from group. groupID: " + groupID
				+ ", callerID: " + callerID + ", deleted: " + deleted);
		return deleted;
	}

	public boolean removeCallerFromAffiliateGroup(int groupID, String callerID) {
		logger.info("Remove caller from group. groupID: " + groupID
				+ ", callerID: " + callerID);
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean deleted = false;
		try {
			String[] callerIds = callerID.split(",");
			for (String callerId : callerIds) {
				deleted = RDCGroupMembersImpl.removeCallerFromGroup(conn,
						groupID, callerId);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Removed caller from group. groupID: " + groupID
				+ ", callerID: " + callerID + ", deleted: " + deleted);
		return deleted;
	}

	public boolean changeGroupForCaller(String subscriberID, String callerID,
			int fromGroupID, int toGroupID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean changed = false;
		try {
			GroupMembers[] groupMembers = GroupMembersImpl
					.getMembersForGroupID(conn, toGroupID);
			if (groupMembers != null) {
				Parameters maxCallersInGroupParam = CacheManagerUtil
						.getParametersCacheManager().getParameter("COMMON",
								"MAX_CALLER_ALLOWED_IN_GROUPS", "30");
				int maxCallersInGroup = Integer.parseInt(maxCallersInGroupParam
						.getValue());

				if (callerID == null) {
					GroupMembers[] fromGroupMembers = GroupMembersImpl
							.getMembersForGroupID(conn, fromGroupID);
					if (fromGroupMembers != null
							&& (fromGroupMembers.length + groupMembers.length) >= maxCallersInGroup)
						return false;
				}

				else {
					if (groupMembers.length >= maxCallersInGroup)
						return false;
				}
			}

			changed = GroupMembersImpl.changeGroupForCaller(conn, callerID,
					fromGroupID, toGroupID);
			if (changed) {
				SubscriberStatus[] subscriberStatus = SubscriberStatusImpl
						.getAllSubscriberSelectionRecordsNotDeactivated(conn,
								subscriberID, null, 0);
				if (subscriberStatus != null) {
					String fromGroupIDStr = "G" + fromGroupID;
					String toGroupIDStr = "G" + toGroupID;
					for (SubscriberStatus selection : subscriberStatus) {
						if (fromGroupIDStr.equals(selection.callerID())
								|| toGroupIDStr.equals(selection.callerID())) {
							SubscriberImpl.updatePlayerStatus(conn,
									subscriberID, "A");
							break;
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return changed;
	}

	public Groups[] getGroupsForSubscriberID(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return GroupsImpl.getGroupsForSubscriberID(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroups[] getAffiliateGroupsForSubscriberID(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupsImpl.getGroupsForSubscriberID(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Groups[] getActiveGroupsForSubscriberID(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupsImpl
					.getActiveGroupsForSubscriberID(conn, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public GroupMembers[] getMembersForGroupID(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupMembersImpl.getMembersForGroupID(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroupMembers[] getAffilateMembersForGroupID(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RDCGroupMembersImpl.getMembersForGroupID(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public GroupMembers[] getActiveMembersForGroupID(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupMembersImpl.getActiveMembersForGroupID(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String addGroupForSubscriberID(String preGroupID, String groupName,
			String subscriberID, String groupPromoID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			Subscriber sub = SubscriberImpl.getSubscriber(conn, subscriberID);
			if (sub == null || !isSubActive(sub))
				return USER_NOT_ACTIVE;

			if (preGroupID != null) {
				Groups preGroup = GroupsImpl.getGroupByPreGroupID(conn,
						preGroupID, subscriberID);
				if (preGroup != null && preGroup.status() != null
						&& !preGroup.status().equals("X"))
					return SAME_PREGROUP_EXISTS_FOR_CALLER;
			}

			if (groupName == null)
				return GROUP_ADD_FAILED_GROUPNAME_NULL;

			Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
					subscriberID);
			if (groups != null) {
				for (int i = 0; i < groups.length; i++) {
					if (groups[i].groupName() != null
							&& groups[i].groupName()
									.equalsIgnoreCase(groupName))
						return SAME_GROUP_NAME_EXISTS_FOR_CALLER;
				}
			}

			String status = STATE_ACTIVATED;
			boolean added = GroupsImpl.insert(conn, preGroupID, groupName,
					subscriberID, null, status);
			if (added)
				return GROUP_ADDED_SUCCESFULLY;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return GROUP_ADD_FAILED_INTERNAL_ERROR;
	}

	public String addGroup(String groupId, String groupName,
			WebServiceContext task) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		logger.info("Adding group. groupId: " + groupId + ", groupName: "
				+ groupName);
		try {
			if (groupName == null) {
				return GROUP_ADD_FAILED_GROUPNAME_NULL;
			}
			String status = STATE_ACTIVATED;
			String preGroupID = null;
			String subscriberID = null;
			boolean added = GroupsImpl.insert(conn, groupId, preGroupID,
					groupName, subscriberID, null, status);
			if (added) {
				logger.info("Added group. groupId: " + groupId
						+ ", groupName: " + groupName
						+ ", returning added: GROUP_ADDED_SUCCESFULLY");
				return GROUP_ADDED_SUCCESFULLY;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Added group. groupId: " + groupId + ", groupName: "
				+ groupName
				+ ", returning added: GROUP_ADD_FAILED_INTERNAL_ERROR");
		return GROUP_ADD_FAILED_INTERNAL_ERROR;
	}

	public String addAffiliateGroup(String groupId, String groupName,
			String optName, WebServiceContext task) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		logger.info("Adding group. groupId: " + groupId + ", groupName: "
				+ groupName);
		try {
			if (groupName == null) {
				return GROUP_ADD_FAILED_GROUPNAME_NULL;
			}
			String status = STATE_ACTIVATED;
			String preGroupID = null;
			String subscriberID = null;
			String refId = UUID.randomUUID().toString();
			task.put("groupRefID", refId);
			boolean added = RDCGroupsImpl.insert(conn, groupId, preGroupID,
					groupName, subscriberID, null, status, refId, optName);
			if (added) {
				logger.info("Added group. groupId: " + groupId
						+ ", groupName: " + groupName
						+ ", returning added: GROUP_ADDED_SUCCESFULLY");
				return GROUP_ADDED_SUCCESFULLY;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Added group. groupId: " + groupId + ", groupName: "
				+ groupName
				+ ", returning added: GROUP_ADD_FAILED_INTERNAL_ERROR");
		return GROUP_ADD_FAILED_INTERNAL_ERROR;
	}

	public boolean deleteGroup(String subscriberID, int groupID,
			String deactivatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = false;
		try {
			Groups group = GroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return false;

			GroupMembersImpl.deleteGroupMembersOfGroup(conn, groupID);

			deleted = GroupsImpl.deleteGroup(conn, groupID);

			if (deleted) {
				if (group.preGroupID() != null
						&& group.preGroupID().equals("99")) {
					// Updating player status for Blocked callers group
					SubscriberImpl.updatePlayerStatus(conn, subscriberID, "A");
				} else {
					// Deactivating all selections of this group for Blocked
					// callers group
					HashMap<String, String> whereClauseMap = new HashMap<String, String>();
					whereClauseMap.put("CALLER_ID", "G" + groupID);
					SubscriberStatusImpl.deactivateSubscriberSelections(conn,
							subscriberID, Collections.singletonMap(
									"DESELECTED_BY", deactivatedBy),
							whereClauseMap);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deleted;
	}

	public boolean deleteAffiliateGroup(String subscriberID, int groupID,
			String deactivatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = false;
		try {
			RDCGroups group = RDCGroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return false;

			RDCGroupMembersImpl.deleteGroupMembersOfGroup(conn, groupID);

			deleted = RDCGroupsImpl.deactivateGroup(conn, groupID);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deleted;
	}

	public boolean deleteGroup(int groupID, String deactivatedBy) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = false;
		try {
			Groups group = GroupsImpl.getGroup(conn, groupID);
			if (group == null)
				return false;

			GroupMembersImpl.deleteGroupMembersOfGroup(conn, groupID);

			deleted = GroupsImpl.deleteGroup(conn, groupID);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return deleted;
	}

	public boolean deleteGroupsOfSubscriber(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupsImpl.deleteGroupsOfSubscriber(conn, subID(subID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateGroupNameForGroupID(int groupID, String groupName,
			String subscriberID) {
		Connection conn = getConnection();

		if (conn == null || subscriberID == null)
			return false;
		try {

			return GroupsImpl.updateGroupNameForGroupID(conn, groupID,
					groupName, subscriberID);

		} catch (Throwable e) {

			logger.error("Exception before releasing connection", e);

		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public boolean deleteGroupMembersOfSubscriber(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean result = false;
		try {
			Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
					subID(subID));
			if (groups != null)
				for (int i = 0; i < groups.length; i++)
					result = GroupMembersImpl.deleteGroupMembersOfGroup(conn,
							groups[i].groupID());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return result;

	}

	public Groups[] getPredefinedGroupsAddedForSubscriber(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupsImpl.getPredefinedGroupsAddedForSubscriber(conn,
					subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Groups getGroupByPreGroupID(String preGroupID, String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupsImpl.getGroupByPreGroupID(conn, preGroupID,
					subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroups getRDCGroupByPreGroupID(String preGroupID,
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RDCGroupsImpl.getGroupByPreGroupID(conn, preGroupID,
					subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Groups getActiveGroupByGroupName(String groupName,
			String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupsImpl.getActiveGroupByGroupName(conn, groupName,
					subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateGroupStatus(int groupID, String newStatus) {
		return updateGroupStatus(groupID, newStatus, null);
	}

	public boolean updateAffiliateGroupStatus(int groupID, int groupStatus,
			String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return RDCGroupsImpl.updateGroupStatus(conn, groupID, groupStatus,
					extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAffiliateGroupMemberStatus(int groupID,
			int groupMemberStatus, String extraInfo, String callerId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return RDCGroupMembersImpl.updateGroupMemberStatus(conn, groupID,
					groupMemberStatus, extraInfo, callerId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public List<RDCGroups> getAffiliateGroupsByGroupStatus(String operatorName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupsImpl.getGroupsByGroupStatus(conn, operatorName);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public List<RDCGroupMembers> getAffiliateGroupMembersByGroupMemberStatus(
			String operatorName) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupMembersImpl.getGroupMembersByGroupMemberStatus(conn,
					operatorName);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateGroupStatus(int groupID, String newStatus,
			String groupPromoID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupsImpl.updateGroupStatus(conn, groupID, newStatus,
					groupPromoID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireAllGroupsForSubscriber(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupsImpl.expireAllGroupsForSubscriber(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Groups getGroup(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return GroupsImpl.getGroup(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroups getRDCGroup(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupsImpl.getGroup(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroups getRDCGroupByRefID(String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupsImpl.getGroupByRefID(conn, refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RDCGroups getAffiliateGroup(int groupID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return RDCGroupsImpl.getGroup(conn, groupID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateGroupMemberStatus(int groupID, String callerID,
			String newStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupMembersImpl.updateGroupMemberStatus(conn, groupID,
					callerID, newStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAllGroupsStatusForSubscriber(String subscriberID,
			String newStatus, String oldStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupsImpl.updateAllGroupsStatusForSubscriber(conn,
					subID(subscriberID), newStatus, oldStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateGroupMembersStatusForGroup(int groupID,
			String newStatus, String oldStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return GroupMembersImpl.updateGroupMembersStatusForGroup(conn,
					groupID, newStatus, oldStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public ArrayList<Groups> playerGetAddGroups() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupsImpl.playerGetAddGroups(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList<Groups> playerGetDelGroups() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return GroupsImpl.playerGetDelGroups(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList<GroupMembers> playerGetAddGroupMembers() {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return GroupMembersImpl.playerGetAddGroupMembers(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ArrayList<GroupMembers> playerGetDelGroupMembers() {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return GroupMembersImpl.playerGetDelGroupMembers(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean retailerCheck(String retailerID) {
		return false;
	}

	public String getBackEndSubscriberStatus(String subscriber,
			boolean prepaidYes) {
		return "SUCCESS";
	}

	public List<String> getOverrideChargeClassestList() {
		return m_overrideChargeClasses;
	}

	public String queryWDS(String subscriberID) {
		return null;
	}

	public RBTLoginUser addRBTLoginUser(String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo,
			boolean encryptPassword) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			if (encryptPassword) {
				String authPass = (String) userInfo.get("AUTH_PASS");
				if (authPass != null && authPass.length() > 0
						&& !authPass.equalsIgnoreCase("")) {
					authPass = Encryption128BitsAES.encryptAES128Bits(authPass);
					userInfo.put("AUTH_PASS", authPass);
				}

				String superPass = (String) userInfo.get("SUPER_PASS");
				if (superPass != null && superPass.length() > 0
						&& !superPass.equalsIgnoreCase("")) {
					superPass = Encryption128BitsAES
							.encryptAES128Bits(superPass);
					userInfo.put("SUPER_PASS", superPass);
				}

				if (password != null && password.length() > 0
						&& !password.equalsIgnoreCase("")) {
					password = Encryption128BitsAES.encryptAES128Bits(password);
				}
			}
			// Changes done for URL Encryption and Decryption
			if (isEncryptionModel()) {
				logger.info("Encryption Model is enabled");
				logger.info("before encrypting userId: "+userID+" and password: "+password);
				userID = URLEncryptDecryptUtil.encryptUserNamePassword(userID);
				password = URLEncryptDecryptUtil.encryptUserNamePassword(password);
				logger.info("after encrypting userId: "+userID+" and password: "+password);
			}
			// End of URL Encryption and Decryption
			return RBTLoginUserImpl
					.insert(conn, userID, password, subID(subscriberID), type,
							userInfo, new Date(), new Date());
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateRBTLoginUser(String userID, String newUserID,
			String password, String subscriberID, String type,
			HashMap<String, String> existingUserInfo,
			HashMap<String, String> userInfo, boolean encryptPassword,
			Date creationTime, String oldPassword) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (encryptPassword) {
				String authPass = (String) userInfo.get("AUTH_PASS");
				if (authPass != null && authPass.length() > 0
						&& !authPass.equalsIgnoreCase("")) {
					authPass = Encryption128BitsAES.encryptAES128Bits(authPass);
					userInfo.put("AUTH_PASS", authPass);
				}

				String superPass = (String) userInfo.get("SUPER_PASS");
				if (superPass != null && superPass.length() > 0
						&& !superPass.equalsIgnoreCase("")) {
					superPass = Encryption128BitsAES
							.encryptAES128Bits(superPass);
					userInfo.put("SUPER_PASS", superPass);
				}

				if (password != null && password.length() > 0
						&& !password.equalsIgnoreCase("")) {
					password = Encryption128BitsAES.encryptAES128Bits(password);
				}

				if (oldPassword != null && oldPassword.length() > 0
						&& !oldPassword.equalsIgnoreCase("")) {
					oldPassword = Encryption128BitsAES
							.encryptAES128Bits(oldPassword);
				}
			}
			// Changes done for URL Encryption and Decryption
			if (isEncryptionModel()) {
				logger.info("Encryption Model is enabled");
				logger.info("before encrypting userId: "+userID+" and password: "+oldPassword);
				logger.info("before encrypting new userId: "+newUserID+" and new password: "+password);
				oldPassword = URLEncryptDecryptUtil
						.encryptUserNamePassword(oldPassword);
				newUserID = URLEncryptDecryptUtil
						.encryptUserNamePassword(newUserID);
				password = URLEncryptDecryptUtil
						.encryptUserNamePassword(password);
				logger.info("after encrypting new userId: "+newUserID+" and new password: "+password);
			}
			// End of URL Encryption and Decryption
			return RBTLoginUserImpl.update(conn, userID, newUserID, password,
					subID(subscriberID), type, existingUserInfo, userInfo,
					creationTime, oldPassword);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean expireUserPIN(String userID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return RBTLoginUserImpl.expireUserPIN(conn, userID, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deleteRBTLoginUserByUserID(String userID, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean deleted = RBTLoginUserImpl.deleteByUserID(conn, userID, type);
		releaseConnection(conn);
		return deleted;
	}

	public boolean deleteRBTLoginUserBySubscriberID(String subscriberID,
			String type) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return RBTLoginUserImpl.deleteBySubscriberID(conn, subscriberID,
					type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public RBTLoginUser[] getRBTLoginUsers(String type) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RBTLoginUserImpl.getRBTLoginUsers(conn, type);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	// Changes are done for handling the voldemort issues.
	public RBTLoginUser[] getRBTLoginUsers(String type, String initial) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RBTLoginUserImpl.getRBTLoginUsersByLimit(conn, type,
					LIMIT_TO_FETCH_LOGIN_USER_DATA, initial);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RBTLoginUser[] getRBTLoginUsers(String userID, String password,
			String subscriberID, String type, boolean encryptPassword) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return RBTLoginUserImpl.getRBTLoginUsers(conn, userID, password,
					subscriberID, type, encryptPassword);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public RBTLoginUser getRBTLoginUser(String userID, String password,
			String subscriberID, String type, HashMap<String, String> userInfo,
			boolean encryptPassword) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		RBTLoginUser rbtLoginUser = null;
		try {
			rbtLoginUser = RBTLoginUserImpl.getRBTLoginUser(conn, userID,
					password, subID(subscriberID), type, encryptPassword);
			if (rbtLoginUser != null) {
				if (userInfo != null) {
					HashMap<String, String> loginUserInfo = rbtLoginUser
							.userInfo();

					Set<String> keySet = userInfo.keySet();
					for (String key : keySet) {
						if (!loginUserInfo.containsKey(key)
								|| !loginUserInfo.get(key).equals(
										userInfo.get(key)))
							return null;
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return rbtLoginUser;
	}

	public FeedSchedule[] getActiveFeedSchedule() {
		return getActiveFeedSchedule(-1);
	}

	public FeedSchedule[] getActiveFeedSchedule(int interval) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedScheduleImpl.getActiveFeedSchedule(conn, interval);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public FeedSchedule[] getActiveFeedSchedulesByStatus(int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return FeedScheduleImpl
					.getActiveFeedSchedulesByStatus(conn, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateTriggerStatus(int feedID, int status) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return FeedScheduleImpl.updateStatus(conn, feedID, status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public FeedSchedule[] getAvailableFeedSchedule(int interval) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return FeedScheduleImpl.getAvailableFeedSchedule(conn, interval);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public String checkSelectionLimit(SubscriberStatus[] subscriberStatus,
			String callerID, boolean inLoop) {
		if (subscriberStatus == null || subscriberStatus.length == 0)
			return "SUCCESS";

		if (!inLoop) {
			for (SubscriberStatus subStatus : subscriberStatus) {
				// If new selection is in override mode and if same callerId
				// selection exists , no need to check limit.
				if ((callerID == null && subStatus.callerID() == null)
						|| (callerID != null && callerID.equals(subStatus
								.callerID())))
					return "SUCCESS";
			}
		}

		if (m_selectionLimit > 0 && subscriberStatus.length >= m_selectionLimit) // Checks
																					// the
																					// Total
																					// Selection
																					// Limit
			return SELECTION_FAILED_SELECTION_LIMIT_REACHED;

		if (maxCallerIDSelectionsAllowed > 0) {
			HashMap<String, Integer> subSelectionMap = new HashMap<String, Integer>();
			for (SubscriberStatus subsStatus : subscriberStatus) {

				if (!subSelectionMap.containsKey(subsStatus.callerID())) {
					subSelectionMap.put(subsStatus.callerID(), new Integer(1));
				} else {
					int count = subSelectionMap.get(subsStatus.callerID());
					count = count + 1;
					subSelectionMap.put(subsStatus.callerID(), new Integer(
							count));
				}
			}
			if (subSelectionMap.containsKey(callerID)) {
				if (subSelectionMap.get(callerID) >= maxLoopSelectionPerCallerID) {
					return SELECTION_FAILED_LOOP_SELECTION_LIMIT_REACHED;
				}
			} else if (callerID != null) {
				// Check for unique callerID's... and if it's greater than
				// maxCallerID limit .. return response
				if (subSelectionMap.containsKey(null))
					subSelectionMap.remove(null);
				if (subSelectionMap.size() >= maxCallerIDSelectionsAllowed)
					return SELECTION_FAILED_CALLERID_LIMIT_REACHED;

			}
		}
		return "SUCCESS";
	}

	public static String getSysdateString() {
		String query = "";
		if (RBTPrimitive.getDBSelectionString().equals(RBTPrimitive.DB_SAPDB))
			query = RBTPrimitive.SAPDB_SYSDATE;
		else
			query = RBTPrimitive.MYSQL_SYSDATE;

		return query;
	}

	public boolean allowFeedUpgrade() {
		return _allowFeedUpgrade;
	}

	public boolean confirmCharge(String subID, String refID) {
		if (refID == null) {
			Subscriber subscriber = getSubscriber(subID);
			if (subscriber == null || subscriber.extraInfo() == null)
				return false;
			HashMap<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(subscriber.extraInfo());
			if (extraInfoMap == null
					|| !extraInfoMap.containsKey(EXTRA_INFO_OFFER_ID))
				return false;
			extraInfoMap.remove(EXTRA_INFO_OFFER_ID);
			return updateExtraInfo(subID,
					DBUtility.getAttributeXMLFromMap(extraInfoMap));
		} else {
			// TODO here add to update selections table based on requirement
		}
		return false;
	}

	public boolean insertMonitor(String msisdn, Date createTime,
			String traceType, char status, String traceResult) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (createTime == null)
				createTime = new Date();
			return MonitoringImpl.insert(conn, msisdn, createTime, traceType,
					status, traceResult);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean endActiveMonitor(String msisdn, String traceType) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return MonitoringImpl.endActiveMonitor(conn, msisdn, traceType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public Monitoring getPendingSubscriberOrTypeMonitor(String msisdn,
			String traceType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return MonitoringImpl.getPendingSubscriberOrTypeMonitor(conn,
					msisdn, traceType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Monitoring getPendingSubscriberMonitor(String msisdn) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return MonitoringImpl.getPendingSubscriberMonitor(conn, msisdn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateActiveMonitor(String msisdn, String traceResult) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return MonitoringImpl
					.updateActiveMonitor(conn, msisdn, traceResult);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean concatToActiveMonitor(String msisdn, String traceResult) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return MonitoringImpl.concatToActiveMonitor(conn, msisdn,
					traceResult);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/*
	 * Added by SenthilRaja This api will be update the status as Actiation
	 * pending while activate the subscriber implementation for Idea only
	 */
	public Subscriber smActivateSubscriber(String subscriberID,
			String activate, Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo,
			String circleID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";

		Subscriber subscriber = null;
		try {
			if (activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO") && endDate == null) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, -1);
				endDate = endCal.getTime();
			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());

			}

			if (cos != null && !cos.isDefaultCos()) {
				endDate = cos.getEndDate();
			}
			String subscription = STATE_ACTIVATION_PENDING;
			String activationInfo = actInfo;

			if (isDirectActivation) {
				// subscription = "S";
				subscription = STATE_ACTIVATED;
			}

			String cosID = null;

			if (cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = null;
				List<String> viralSMSList = Arrays.asList("BASIC,CRICKET"
						.split(","));
				if (isViralSmsTypeListForNewTable(viralSMSList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null) {
					activationInfo = activationInfo + ":" + "viral";
				}
			}

			// update ExtraInfo
			if (_preCallPrompt != null && extraInfo != null
					&& !extraInfo.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG))
				extraInfo.put(EXTRA_INFO_INTRO_PROMPT_FLAG, _preCallPrompt);

			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);

			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
			if (subscriber != null) {

				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {

						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;

							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);

						}
					}
					return subscriber;
				}
				if (!isDirectActivation
						&& (subsciptionYes.equals("D")
								|| subsciptionYes.equals("P")
								|| subsciptionYes.equals("F")
								|| subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes
									.equals("z"))) {
					// releaseConnection(conn);
					return null;
				}
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();
				String refID = UUID.randomUUID().toString();

				SubscriberImpl.update(conn, subID(subscriberID), activate,
						null, startDate, endDate, prepaid, null, null, 0,
						activationInfo, subscriptionClass, deactivatedBy,
						deactivationDate, null, subscription, 0, cosID, cosID,
						rbtType, subscriber.language(), subExtraInfo, circleID,
						refID, isDirectActivation);
				if (startDate == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, m_endDate, prepaid, null, null, 0,
						activationInfo, subscriptionClass, subscription,
						deactivatedBy, deactivationDate, null, 0, cosID, cosID,
						rbtType, subscriber.language(),
						subscriber.oldClassType(), subExtraInfo, circleID,
						refID);
			} else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, startDate, endDate, prepaid, null,
						null, 0, activationInfo, subscriptionClass, null, null,
						null, subscription, 0, cosID, cosID, rbtType, null,
						isDirectActivation, subExtraInfo, circleID, null);

			if (isDirectActivation) {
				boolean isRealTime = false;
				Parameters param = CacheManagerUtil.getParametersCacheManager()
						.getParameter("DAEMON", "REAL_TIME_SELECTIONS");
				if (param != null && param.getValue().equalsIgnoreCase("true"))
					isRealTime = true;

				smUpdateSelStatusSubscriptionSuccess(subID(subscriberID),
						isRealTime);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	public String smAddSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID,
			boolean incrSelCount, boolean useDate, String transID,
			boolean OptIn, boolean isTata, boolean inLoop, String subClass,
			Subscriber sub, int rbtType, String selInterval, HashMap extraInfo,
			HashMap<String, String> responseParams) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		logger.info("The value of status: " + status + ", rbtType: " + rbtType);
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_ACTIVATION_PENDING;
		int nextPlus = -1;
		boolean updateEndDate = false;
		try {
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null
					&& subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (selInterval != null && selInterval.indexOf(",") != -1) {
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while (stk.hasMoreTokens())
					days.add(stk.nextToken());

				if (days.size() == 7) {
					selInterval = null;
				} else {
					Collections.sort(days);
					selInterval = "";
					for (int i = 0; i < days.size(); i++) {
						selInterval = selInterval + days.get(i);
						if (i != days.size() - 1)
							selInterval = selInterval + ",";
					}
				}
			}

			if (callerID != null) {
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
						subscriberID);
				if (groups != null && groups.length > 0) {
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++) {
						groupIDs[i] = groups[i].groupID();
					}
					GroupMembers groupMember = GroupMembersImpl
							.getMemberFromGroups(conn, callerID, groupIDs);
					if (groupMember != null) {
						for (Groups group : groups) {
							if (groupMember.groupID() == group.groupID()) {
								logger.info("Pre group id for the group is :"
										+ group.preGroupID());
								if (group.preGroupID() != null
										&& group.preGroupID().equals("99")) // Blocked
																			// Caller
									return SELECTION_FAILED_CALLER_BLOCKED;
								else if (group.preGroupID() == null
										|| !group.preGroupID().equals("98"))
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}

			if (sub != null && rbtType != 2) {
				rbtType = sub.rbtType();
			}
			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				logger.info(subscriberID + " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection) {
				isSelSuspended = isSelSuspended(subID(subscriberID),
						subID(callerID));
			}
			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}

			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT)))
				sel_status = STATE_ACTIVATION_PENDING;
			Date endDate = endTime;
			if (endDate == null)
				endDate = m_endDate;

			String classType = chargeClassType;

			Date clipEndTime = null;
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
			}

			if (subscriberWavFile == null) {
				if (status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;

				subscriberWavFile = "CRICKET";
			}
			if (subClass != null && m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(subClass)) {
				// chargeClassType = (String)
				// m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				if (clipEndTime.getTime() < System.currentTimeMillis()) {
					return SELECTION_FAILED_CLIP_EXPIRED;
				}
				if (categories != null
						&& (categories.type() == DAILY_SHUFFLE || categories
								.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				if (rbtType == 1) {
					if (status == 99 || categories.id() == 1)
						return SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE;
					if (categories.type() == 10 || categories.type() == 12
							|| categories.type() == 0
							|| categories.type() == 11
							|| categories.type() == 20)
						return SELECTION_FAILED_ADRBT_FOR_SHUFFLES;
				}

			}

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = null;
				List<String> viralSMSList = Arrays.asList("BASIC,CRICKET"
						.split(","));
				if (isViralSmsTypeListForNewTable(viralSMSList)) {
					viralSMS = ViralSMSNewImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				} else {
					viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
							subID(subscriberID), null);
				}
				if (viralSMS != null) {
					selectInfo = selectInfo + ":" + "viral";
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			String oldClassType = null;
			Date oldNextChargeDate = null;

			String afterTrialClassType = "DEFAULT";
			if (OptIn)
				afterTrialClassType = "DEFAULT_OPTIN";

			/**
			 * Since Sprint 4 RBT 2.0, RBT 15670 One more parameter udpId has
			 * been added in getSubscriberSelections method. If udpId is present
			 * then query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*
			 * if(extraInfo.containsKey(WebServiceConstants.param_udpId)) udpId
			 * = (String) extraInfo.get("UDP_ID");
			 */
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl
					.getSubscriberSelections(conn, subID(subscriberID),
							subID(callerID), rbtType, udpId);

			if (!inLoop && status == 1) // If user opted for UDS
			{
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(sub.extraInfo());
				if (subExtraInfoMap != null
						&& subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN))
							.equalsIgnoreCase("TRUE");
				if (inLoop) {
					if (isShufflePresentSelection(subID(subscriberID),
							callerID, 0))
						inLoop = false;
					else if (categories.type() == 0 || categories.type() == 10
							|| categories.type() == 11
							|| categories.type() == 12
							|| categories.type() == 20)
						return SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN;
				}
			}

			if (selInterval != null && status != 80) {

				if (selInterval.startsWith("W") || selInterval.startsWith("M")) {

					status = 75;
				}

				if (selInterval.startsWith("Y")) {

					status = 95;
					String date = selInterval.substring(1);
					Date parseDate = null;
					if (date.length() == 8) {

						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"ddMMyy");
						Date currentDate = new Date();
						parseDate = dateFormat.parse(date);
						if (parseDate.before(currentDate)
								|| parseDate.equals(currentDate)) {
							return SELECTION_FAILED_INVALID_PARAMETER;
						}
						Calendar cal = Calendar.getInstance();
						cal.setTime(parseDate);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						endDate = cal.getTime();
					}

					if (date.length() == 4) {

						endDate = m_endDate;
					}
				}
			}

			// Added for checking the selection limit

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;
			if (isTata) {
				subscriberStatus = this.getSubWavFileForCaller(
						subID(subscriberID), callerID, subscriberWavFile);
				if (subscriberStatus != null
						&& !(subscriberStatus.selStatus().equals(
								STATE_ACTIVATED)
								|| subscriberStatus.selStatus().equals(
										STATE_TO_BE_ACTIVATED)
								|| subscriberStatus.selStatus().equals(
										STATE_ACTIVATION_PENDING) || subscriberStatus
								.selStatus().equals(
										STATE_BASE_ACTIVATION_PENDING)))
					subscriberStatus = null;
			} else {
				subscriberStatus = getAvailableSelection(conn,
						subID(subscriberID), subID(callerID),
						subscriberSelections, categories, subscriberWavFile,
						status, fromTime, toTime, startDate, endDate,
						doTODCheck, inLoop, rbtType, selInterval, selectedBy);
			}
			if (subscriberStatus == null) {
				logger.info("No selections found for subscriberID: "
						+ subscriberID);

				if (inLoop && (status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (inLoop && categories.type() == SHUFFLE && !m_putSGSInUGS)
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(
						conn, subID(subscriberID), subID(callerID), status,
						rbtType);
				if (subscriberStatus != null) {
					oldClassType = subscriberStatus.classType();
					oldNextChargeDate = subscriberStatus.nextChargingDate();
					if (categories != null && categories.id() == 3
							&& classType != null
							&& m_TrialWithActivations != null
							&& m_TrialWithActivations.contains(classType)) {
						if (!oldClassType.startsWith("TRIAL")) {
							SubscriberStatusImpl.smDeactivateOldSelection(conn,
									subID(subscriberID), subID(callerID),
									status, null, fromTime, toTime, rbtType,
									selInterval, null,false);

						}
						if (oldNextChargeDate != null) {

							if (oldClassType.startsWith("TRIAL"))
								SubscriberStatusImpl
										.smDeactivateOldTrialSelection(conn,
												subID(subscriberID),
												subID(callerID), status,
												fromTime, toTime, rbtType);

						}
						oldClassType = null;
						oldNextChargeDate = null;
					}

					if (inLoop && subscriberStatus.categoryType() == SHUFFLE
							&& !m_putSGSInUGS)
						inLoop = false;
				} else
					// this else will make all first callerID selection as
					// override :), not needed actually
					inLoop = false;

				if (oldClassType == null && classType != null
						&& classType.startsWith("TRIAL")) {
					ChargeClass chargeClass = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					if (chargeClass != null
							&& chargeClass.getSelectionPeriod() != null
							&& chargeClass.getSelectionPeriod().startsWith("D")) {
						String selectionPeriod = chargeClass
								.getSelectionPeriod().substring(1);
						if (nextPlus < 0)
							nextPlus = Integer.parseInt(selectionPeriod);
					}
				}
				if (oldClassType != null && classType != null
						&& classType.startsWith("TRIAL")
						&& oldClassType.startsWith("TRIAL")) {
					if (oldClassType.equalsIgnoreCase(classType)) {
						if (oldNextChargeDate != null
								&& oldNextChargeDate.after(new Date(System
										.currentTimeMillis()))) {
							nextChargingDate = oldNextChargeDate;
							nextPlus = new Long(
									(oldNextChargeDate.getTime() - System
											.currentTimeMillis())
											/ (3600 * 1000 * 24)).intValue() + 1;
						}
						if (oldNextChargeDate != null
								&& oldNextChargeDate.before(new Date(System
										.currentTimeMillis()))) {
							nextChargingDate = null;
							startDate = null;
						}
						SubscriberStatusImpl.smDeactivateOldTrialSelection(
								conn, subID(subscriberID), subID(callerID),
								status, fromTime, toTime, rbtType);
					}
				}
				if (oldClassType != null && oldClassType.startsWith("TRIAL")
						&& classType != null && !classType.startsWith("TRIAL")) {
					SubscriberStatusImpl.smDeactivateOldTrialSelection(conn,
							subID(subscriberID), subID(callerID), status,
							fromTime, toTime, rbtType);
				}
				/**
				 * @added by sreekar if user's last selection is a trail
				 *        selection his next selection should override the old
				 *        one
				 */
				if (inLoop && oldClassType != null
						&& (oldClassType.indexOf("TRIAL") != -1))
					inLoop = false;
				char loopStatus = getLoopStatusForNewSelection(inLoop,
						subID(subscriberID), isPrepaid);
				if (classType != null && classType.startsWith("TRIAL")) {
					sel_status = STATE_ACTIVATION_PENDING;
					// added by sreekar
					loopStatus = getLoopStatusToUpateSelection(loopStatus,
							subID(subscriberID), isPrepaid);
				}

				if (transID != null) {
					selectInfo += ":transid:" + transID + ":";
					if (sel_status.equals(STATE_TO_BE_ACTIVATED))
						sel_status = STATE_ACTIVATION_PENDING;
				}
				String actBy = null;
				if (sub != null) {
					actBy = sub.activatedBy();
				}
				if (m_trialChangeSubTypeOnSelection && actBy != null
						&& actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						// sel_status = STATE_BASE_ACTIVATION_PENDING;

						if (!convertSubscriptionTypeTrial(subID(subscriberID),
								subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				String checkSelStatus = checkSelectionLimit(
						subscriberSelections, subID(callerID), inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				// Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil
						.getParametersCacheManager().getParameter("COMMON",
								"SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if (parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn,
						subID(subscriberID), subID(callerID), status, fromTime,
						toTime, graceDeselectedBy, rbtType);

				count = smCreateSubscriberStatus(subscriberID, callerID,
						categories.id(), subscriberWavFile, setTime, startDate,
						endDate, status, selectedBy, selectInfo,
						nextChargingDate, prepaid, classType, changeSubType,
						fromTime, toTime, sel_status, true, clipMap,
						categories.type(), useDate, loopStatus, isTata,
						nextPlus, rbtType, selInterval, extraInfo,
						responseParams, circleID);
				count++;
				if (incrSelCount)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));

				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID),
							endDate, null);
				}
			} else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		if (count > 0)
			return SELECTION_SUCCESS;
		else
			return SELECTION_FAILED_INTERNAL_ERROR;
	}

	public void setSelectionCount(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberImpl.setSelectionCount(conn, subID(subscriberID));
		} catch (Throwable t) {
			logger.error("Exception before release connection", t);
		} finally {
			releaseConnection(conn);
		}
	}

	public int smCreateSubscriberStatus(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime,
			String sel_status, boolean smActivation, HashMap clipMap,
			int categoryType, boolean useDate, char loopStatus, boolean isTata,
			int nextPlus, int rbtType, String selInterval,
			HashMap extraInfoMap, HashMap<String, String> responseParams,
			String circleId) {
		Connection conn = getConnection();
		if (conn == null)
			return 0;
		try {
			// insertion into the table
			if (!isTata) {
				subscriberID = subID(subscriberID);
				callerID = subID(callerID);
			}

			if (isTata)
				smActivation = false;

			String selExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfoMap);

			SubscriberStatus subscriberSelection = SubscriberStatusImpl.insert(
					conn, subID(subscriberID), callerID, categoryID,
					subscriberWavFile, setTime, startTime, endTime, status,
					classType, selectedBy, selectionInfo, nextChargingDate,
					prepaid, fromTime, toTime, smActivation, sel_status, null,
					null, categoryType, loopStatus, nextPlus, rbtType,
					selInterval, selExtraInfo, null, circleId, null, null);

			if (subscriberSelection != null && responseParams != null) {
				responseParams.put("REF_ID", subscriberSelection.refID());
			}

			int clipID = -1;
			String clipName = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_ID")) {
					String s = (String) clipMap.get("CLIP_ID");
					try {
						clipID = Integer.parseInt(s);
					} catch (Exception e) {
						clipID = -1;
					}
				}

				clipName = (String) clipMap.get("CLIP_NAME");
			}
			// if (clips != null && clips.addAccess())
			if (clipID != -1) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				Date currentDate = null;
				if (useDate)
					currentDate = calendar.getTime();

				DateFormat timeFormat = new SimpleDateFormat("yyyy");
				Date date = new Date(System.currentTimeMillis());
				String year = timeFormat.format(date);
				if (currentDate != null)
					year = timeFormat.format(currentDate);
				timeFormat = new SimpleDateFormat("MM");

				String month = timeFormat.format(date);
				if (currentDate != null)
					month = timeFormat.format(currentDate);

				Access access = AccessImpl.getAccess(conn, clipID, year, month,
						currentDate);

				if (access == null) {
					access = AccessImpl.insert(conn, clipID, clipName, year,
							month, 0, 0, 0, currentDate);
				}
				if (access != null) {
					access.incrementNoOfAccess();
					if (subscriberWavFile != null
							&& subscriberWavFile.indexOf("rbt_ugc_") != -1)
						access.incrementNoOfPlays();
					access.update(m_dbURL, m_nConn);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return 1;
	}

	public boolean removeSelection(String subscriberID, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			subscriberID = subID(subscriberID);
			SubscriberStatusImpl.removeSelectionByRefID(conn, subscriberID,
					refID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public String smDeactivateSubscriber(String subscriberID,
			String deactivate, Date date, boolean delSelections,
			boolean sendToHLR, boolean smDeactivation, boolean isDirectDeact,
			boolean checkSubClass, int rbtType, Subscriber sub, String dctInfo) {
		String ret = null;
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			subscriberID = subID(subscriberID);
			boolean deact = true;
			boolean success = false;
			if (!isDirectDeact && checkSubClass) {
				if (sub != null) {
					SubscriptionClass temp = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(sub.subscriptionClass());
					if (temp != null && temp.isDeactivationNotAllowed()) {
						ret = "DCT_NOT_ALLOWED";
					}
				}
			}

			boolean deactSub = true;
			if (ret == null) {
				if (smDeactivation) {
					deact = false;
					if (sub != null) {
						if (isDirectDeact
								|| (sub.subYes() != null && (sub.subYes()
										.equalsIgnoreCase("B")
										|| sub.subYes().equalsIgnoreCase("O")
										|| sub.subYes().equalsIgnoreCase("z")
										|| sub.subYes().equalsIgnoreCase("Z") || sub
										.subYes().equalsIgnoreCase("G")))) {
							if (sub.rbtType() == TYPE_RBT_RRBT
									|| sub.rbtType() == TYPE_SRBT_RRBT) {
								convertSubscriptionType(subscriberID,
										sub.subscriptionClass(), "DEFAULT",
										null, rbtType, true, null, sub);
								deactSub = false;
							}
							deact = true;
						}
					}
					if (deact) {
						boolean isNewSubscriber = false;
						if (deactSub) {
							success = SubscriberImpl.smDeactivationPending(
									conn, subscriberID, deactivate, date,
									sendToHLR, smDeactivation, isNewSubscriber,
									isDirectDeact, m_isMemCachePlayer, dctInfo,
									sub);
						}
						if (success) {
							if (!smDeactivation)
								SubscriberChargingImpl.remove(conn,
										subscriberID);
							if (delSelections || isDirectDeact) {
								SubscriberStatusImpl.deactivate(conn,
										subscriberID, date, smDeactivation,
										isNewSubscriber, deactivate, rbtType);

								Groups[] groups = GroupsImpl
										.getGroupsForSubscriberID(conn,
												subscriberID);
								if (groups != null) {
									int[] groupIDs = new int[groups.length];
									for (int i = 0; i < groups.length; i++) {
										groupIDs[i] = groups[i].groupID();
									}
									GroupMembersImpl
											.deleteGroupMembersOfGroups(conn,
													groupIDs);
									GroupsImpl.deleteGroupsOfSubscriber(conn,
											subscriberID);
								}
							}
						}
					} else
						ret = "ACT_PENDING";
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return ret == null ? "SUCCESS" : ret;
	}

	public boolean smDeactivateSubscriberSelections(String subscriberID,
			String deselectedBy, Map<String, String> whereClauseMap) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.smDeactivateSubscriberSelections(conn,
					subscriberID, deselectedBy, whereClauseMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smConvertSubscriptionType(String subscriberID,
			String initType, String finalType, String strActBy,
			String strActInfo, int rbtType, boolean useRbtType,
			HashMap<String, String> extraInfoMap, Subscriber subscriber) {
		if (initType != null && finalType != null && finalType.equals(initType))
			return false;
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			String extraInfo = null;
			if (extraInfoMap != null) {
				extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
				;
			}

			return SubscriberImpl.smConvertSubscriptionType(conn,
					subID(subscriberID), initType, finalType, strActBy,
					strActInfo, rbtType, useRbtType, extraInfo, subscriber);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberStatus getSelectionRefID(String subscriberID,
			String deselectedBy, Map<String, String> whereClauseMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.smGetSelectionRefID(conn, subscriberID,
					deselectedBy, whereClauseMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberStatus[] getSelectionsToBeDeactivated(String subscriberID,
			Map<String, String> whereClauseMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getSelectionsToBeDeactivated(conn,
					subscriberID, whereClauseMap);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public int getSelcetionCountForSubcriber(String subscriberID,
			Map<String, String> whereClauseMap) {
		if (chargeClassMapForAllSubInLoop.isEmpty()
				&& chargeClassMapForSpecialSub.isEmpty()
				&& chargeClassMapForSpecialSubInLoop.isEmpty()) {
			return 0;
		}
		SubscriberStatus[] subSelections = getSelectionsToBeDeactivated(
				subscriberID, whereClauseMap);
		return (subSelections != null ? subSelections.length : 0);
	}

	public boolean smUpgradeToSongPack(Subscriber subscriber, String newCosId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			if (subscriber == null)
				return false;
			String subscriberId = subscriber.subID();
			String numMaxSelection = subscriber.maxSelections() + "";
			String extraInfo = DBUtility.setXMLAttribute(
					subscriber.extraInfo(), "MAX_SEL", numMaxSelection);
			return SubscriberImpl.smUpgradeToSongPack(conn, subscriberId,
					newCosId, extraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean smAddSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval,
			HashMap extraInfo, HashMap<String, String> responseParams) {

		logger.info("entered 2");
		subscriberID = subID(subscriberID);
		/*
		 * String circleID = dbManager.getCircleId(subscriberID); Subscriber sub
		 * = dbManager.getSubscriber(subscriberID);
		 */
		char prepaidYes = 'n';
		if (subscriber != null && subscriber.prepaidYes()) {
			prepaidYes = 'y';
			subYes = subscriber.subYes();
		}
		Categories categories = getCategory(categoryID, subscriber.circleID(),
				prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		logger.info("leaving");
		String ret = smAddSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				null, incrSelCount, false, null, OptIn, false, inLoop,
				subClass, subscriber, 0, selInterval, extraInfo, responseParams);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;

	}

	public SubscriberStatus[] getSubscriberSelections(String subscriberID,
			String initClass, String finalClass, int rbtType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getSubscriberSelection(conn,
					subID(subscriberID), initClass, finalClass, rbtType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean convertWeeklySelectionsClassTypeToMonthly(
			String subscriberID, String initClass, String finalClass,
			int rbtType, String extraInfo, String selInterval) {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.convertSelectionClassType(conn,
					subID(subscriberID), initClass, finalClass, rbtType,
					extraInfo, selInterval);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateSubscriberSelection(String subscriberID,
			String initClass, String finalClass, String status, int rbtType,
			String extraInfo, String selInterval) {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl.updateSubscriberSelection(conn,
					subID(subscriberID), initClass, finalClass, status,
					rbtType, extraInfo, selInterval);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateSubscriberSelectionInlineDaemonFlag(
			String subscriberID, String refId, Integer flag) {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberStatusImpl
					.updateSubscriberSelectionInlineDaemonFlag(conn,
							subID(subscriberID), refId, flag);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateConsentInlineDaemonFlag(String refId, Integer flag) {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return ConsentTableImpl.updateConsentInlineDaemonFlag(conn,
					refId, flag);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean resetInlineDaemonFlag() {

		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberStatusImpl.resetSubscriberSelectionInlineDaemonFlag(conn);
			ConsentTableImpl.resetConsentInlineDaemonFlag(conn);
			return true;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	/**
	 * API is written specifically for RBTCopyProcessor. Dont use this API any
	 * where else. This API is overriden for VodacomDBImpl as on 17/06/2010.
	 */
	public String getChargeClass(String subscriberID, String circleID,
			String subscriberWavFile, String classType,
			boolean useUIChargeClass, int categoryID, int categoryType,
			String categoryClassType, Date clipEndTime, String selectedBy,
			String clipClassType) {
		if (classType == null)
			classType = "DEFAULT";
		String chargeClass = getNextChargeClass(subscriberID);
		if (chargeClass == null) {
			String prepaidYes = "b";
			Parameters parameter = CacheManagerUtil.getParametersCacheManager()
					.getParameter("GATHERER", "DEFAULT_SUBTYPE", "pre");
			if (parameter != null)
				prepaidYes = parameter.getValue().equalsIgnoreCase("pre") ? "y"
						: "n";

			CosDetails cosDetails = getCos(subscriberID, circleID, prepaidYes,
					selectedBy);
			chargeClass = getChargeClassFromCos(cosDetails, 0);
		}

		logger.info("RBT:CHARGE Class = " + chargeClass + " CLASS Type = "
				+ classType + " Subscriber id = " + subscriberID);
		if (chargeClass == null || chargeClass.equalsIgnoreCase("DEFAULT"))
			chargeClass = classType;
		if (chargeClass == null)
			chargeClass = "DEFAULT";
		return chargeClass;
	}

	public String getChargeClassFromCos(Cos cosObject, int selCount) {
		if (cosObject == null || cosObject.getChargeClass() == null)
			return null;
		StringTokenizer stk = new StringTokenizer(cosObject.getChargeClass(),
				",");
		int countTokens = stk.countTokens();
		selCount = selCount % countTokens;
		for (int i = 0; i < selCount; i++)
			stk.nextToken();
		return stk.nextToken();
	}

	/*
	 * Following api for Announcement feature
	 */
	public SubscriberAnnouncements activateAnnouncement(String subId,
			int clipId, Date startTime, Date endTime, String timeInterval,
			String frequency) {
		Connection conn = getConnection();
		SubscriberAnnouncements subscriberAnnouncements = null;
		try {
			if (conn == null)
				return null;
			subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			int status = ANNOUNCEMENT_TO_BE_ACTIVED; // To be activated
			String timeIntervals = "0-23";

			if (null == startTime)
				startTime = new Date();
			if (null == endTime) {
				try {
					endTime = new SimpleDateFormat("yyyy-MM-dd")
							.parse("2037-01-01");
				} catch (ParseException e) {
					logger.error("", e);
				}
			}

			if (null == subscriberAnnouncements) {
				logger.info("RBT: new subscriber to announcement");
				subscriberAnnouncements = new SubscriberAnnouncementsImpl(0,
						subId, clipId, status, startTime, endTime,
						timeIntervals, frequency);
				subscriberAnnouncements = SubscriberAnnouncementsImpl.insert(
						conn, subscriberAnnouncements);
				if (null != subscriberAnnouncements)
					logger.info("RBT: successfully activated SubscriberAnnouncement "
							+ subscriberAnnouncements.toString());
			} else {
				subscriberAnnouncements.setActivationDate(startTime);
				subscriberAnnouncements.setDeactivationDate(endTime);
				subscriberAnnouncements.setStatus(status);
				boolean ret = SubscriberAnnouncementsImpl
						.updateToActiveAnnouncement(conn,
								subscriberAnnouncements);
				if (ret) {
					subscriberAnnouncements = SubscriberAnnouncementsImpl
							.getAnnouncementsRecord(conn,
									subscriberAnnouncements.subscriberId(),
									subscriberAnnouncements.clipId());
					logger.info("RBT: successfully updated SubscriberAnnouncement "
							+ subscriberAnnouncements.toString());
				} else
					return null;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberAnnouncements;
	}

	public boolean deactivateAnnouncement(String subId, int clipId) {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return false;

			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements.setDeactivationDate(new Date());
			subscriberAnnouncements.setStatus(ANNOUNCEMENT_TO_BE_DEACTIVED);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements, true);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivateAnnouncements(String subId) {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return false;
			return SubscriberAnnouncementsImpl.deactivateAnnouncements(conn,
					subId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncement(
			SubscriberAnnouncements subscriberAnnouncement) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncement);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncementToDeactive(String subId, int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements.setStatus(ANNOUNCEMENT_DEACTIVE);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncementToActive(String subId, int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements.setStatus(ANNOUNCEMENT_ACTIVE);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncementToBeActivatedAtPlayer(String subId,
			int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements
					.setStatus(ANNOUNCEMENT_TO_BE_ACTIVED_PLAYER);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncementsToBeDeactivatedAtPlayer(String subId,
			int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements
					.setStatus(ANNOUNCEMENT_TO_BE_DEACTIVED_PLAYER);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public List<SubscriberAnnouncements> getAnnouncementSubscribers(int status,
			long sequenceId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl.getAnnouncementsRecords(conn,
					status, sequenceId);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public List<SubscriberAnnouncements> getExpiredAnnouncementSubscribers(
			String fetchsize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl.getExpiredAnnouncementsRecords(
					conn, fetchsize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberAnnouncements getAnnouncementsSubscriber(
			String subscriberId, int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl.getAnnouncementsRecord(conn,
					subscriberId, clipId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberAnnouncements[] getSubscriberAnnouncemets(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl.getSubscriberAnnouncemets(conn,
					subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberAnnouncements[] getActiveSubscriberAnnouncemets(
			String subscriberId) {
		logger.info("RBT: inside getActiveSubscriberAnnouncemets");
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl.getActiveSubscriberAnnouncemets(
					conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberAnnouncements[] getActiveSubscriberAnnouncemetsForCallback(
			String subscriberId) {
		logger.info("RBT: inside getActiveSubscriberAnnouncemetsFroCallback");
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl
					.getActiveSubscriberAnnouncemetsForCallback(conn,
							subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean smAnnouncementsToBeActivated(String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			return SubscriberAnnouncementsImpl.smUpdateAnnounceToBeActivated(
					conn, subscriberId, ANNOUNCEMENT_TO_BE_ACTIVED);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateAnnouncementsBaseCallbackPending(String subscriberId,
			int clipId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			SubscriberAnnouncements subscriberAnnouncements = SubscriberAnnouncementsImpl
					.getAnnouncementsRecord(conn, subscriberId, clipId);
			if (null == subscriberAnnouncements) {
				logger.info("RBT: This subscriber not an announcement subscriber subscriberid : "
						+ subscriberId + " Clipid : " + clipId);
				return false;
			}
			subscriberAnnouncements
					.setStatus(ANNOUNCEMENT_BASE_DEACTIVATION_PENDING);
			return SubscriberAnnouncementsImpl.update(conn,
					subscriberAnnouncements);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public SubscriberAnnouncements[] smGetActiveAndCallbackPendingSubAnnouncemets(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberAnnouncementsImpl
					.smGetActiveAndCallbackPendingSubAnnouncemets(conn,
							subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean updateTrialSelectionIterId(long seqId, int iterId)
			throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");

		try {
			return TrialSelectionImpl.updateIterId(conn, seqId, iterId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void bulkDeleteTrialSelection() throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			TrialSelectionImpl.bulkDelete(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void bulkDeleteTnbSubscriber() throws OnMobileException {
		Connection conn = getConnection();

		if (conn == null)
			throw new OnMobileException("Conn Null");
		try {
			TnbSubscriberImpl.bulkDelete(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public int deleteOldPickOfTheDayEntries(int days) {
		Connection conn = getConnection();
		if (conn == null)
			return -1;

		try {
			Calendar cal = Calendar.getInstance();
			String day = days + "";
			day = "-" + day;
			cal.add(cal.DAY_OF_MONTH, Integer.parseInt(day));
			Date removeDate = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String removeBeforeDate = sdf.format(removeDate);
			return PickOfTheDayImpl.removeOldEntries(conn, removeBeforeDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return -1;
	}

	public boolean isPackRequest(CosDetails cosDetails) {
		if (cosDetails == null)
			return false;
		if (SONG_PACK.equalsIgnoreCase(cosDetails.getCosType())
				|| UNLIMITED_DOWNLOADS
						.equalsIgnoreCase(cosDetails.getCosType())
				|| LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType())
				|| AZAAN.equalsIgnoreCase(cosDetails.getCosType())
				|| PROFILE_COS_TYPE.equalsIgnoreCase(cosDetails.getCosType())
				|| COS_TYPE_AUTO_DOWNLOAD.equalsIgnoreCase(cosDetails
						.getCosType())
				|| MUSIC_POUCH.equalsIgnoreCase(cosDetails.getCosType())
				|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails
						.getCosType())
				|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails
						.getCosType()))
			return true;
		else
			return false;
	}

	// Added by Sreekar for Reliance ARBT implementation
	public boolean deleteSubscriber(String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberImpl.deleteSubscriber(conn, subscriberId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// Added by Sreekar for Reliance ARBT implementation
	public boolean deleteSubscriberSelectionSForWavFile(String subscriberId,
			String wavFile) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberStatusImpl.deleteSubscriberSlectionsForWavFIle(
					conn, subscriberId, wavFile);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public void processShufflePromo(String dummyCatId) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			ShufflePromoImpl.deactivateShuffleAndSetSelection(conn, dummyCatId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void addShufflePromoEntry(String categoryId, String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			Category category = getCategory(Integer.parseInt(categoryId));
			Subscriber subscriber = getSubscriber(subscriberId);
			Date endDate = null;
			String endDateStr = null;
			if (category != null && subscriber != null) {
				String chargeClass = category.getClassType();
				if (CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(chargeClass) != null) {
					String chargePeriod = CacheManagerUtil
							.getChargeClassCacheManager()
							.getChargeClass(chargeClass).getSelectionPeriod();
					endDate = getNextDate(chargePeriod);
					endDateStr = (new SimpleDateFormat("yyyy-MM-dd")
							.format(endDate));
				} else {
					logger.error("ChargeClass object for category(category id="
							+ categoryId
							+ ") is null. Please configure valid CLASS_TYPE for the ctegory.");
					return;
				}
			} else {
				logger.error("shuffle promo entry not added: category or subscriber or both are null");
				return;
			}
			ShufflePromoImpl.activateShuffle(conn, category, subscriber,
					endDate);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
	}

	public boolean isCosToBeIgnored(Categories categories,
			ClipMinimal clipMinimal, Subscriber subscriber) {
		if (subscriber != null) {
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(subscriber.cosID());
			if (cos != null && cos.getContentTypes() != null) {
				com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = null;
				if (categories != null)
					category = rbtCacheManager.getCategory(categories.id());

				if (category != null
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(category.getCategoryTpe())) {
					Clip[] clips = rbtCacheManager.getActiveClipsInCategory(
							category.getCategoryId(), null);
					return !(DataUtils.isContentAllowed(cos, clips));
				} else if (clipMinimal != null) {
					Clip clip = rbtCacheManager
							.getClip(clipMinimal.getClipId());
					return !(DataUtils.isContentAllowed(cos, clip));
				}
			}
		}
		return false;
	}

	public boolean isCosToBeIgnored(
			CosDetails cos,
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category,
			Clip clip) {
		if (cos != null && cos.getContentTypes() != null) {
			if (category != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(category.getCategoryTpe())) {
				Clip[] clips = rbtCacheManager.getActiveClipsInCategory(
						category.getCategoryId(), null);
				return !(DataUtils.isContentAllowed(cos, clips));
			} else if (clip != null) {
				return !(DataUtils.isContentAllowed(cos, clip));
			}
		}
		return false;
	}

	// Methods for RBT_PROVISIONING_REQUESTS table
	public ProvisioningRequests insertProvisioningRequestsTable(
			ProvisioningRequests provisioningRequests) throws OnMobileException {
		logger.info("Inserting into ProvisioningRequestsTable: "
				+ provisioningRequests);
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return ProvisioningRequestsDao
					.createProvisioningRequest(provisioningRequests);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	/*
	 * It is overridden in TelefonicaSelectionDBMgrImpl.java For ODA Pack
	 * Selection(Category Type = 16)
	 */
	public boolean isODAPackRequest(String subscriberId, String refId) {
		return false;
	}

	/*
	 * public boolean isODAPackActivated(Subscriber subscriber,String
	 * categoryID){ Connection conn = getConnection(); if (conn == null) return
	 * false; try{ HashMap<String, String> extraInfoMap =
	 * getExtraInfoMap(subscriber); if (extraInfoMap != null &&
	 * extraInfoMap.containsKey(EXTRA_INFO_PLAYLIST) && categoryID != null) {
	 * String playlist = extraInfoMap.get(EXTRA_INFO_PLAYLIST); if (playlist !=
	 * null) { boolean isODAActived =
	 * Arrays.asList(playlist.split(",")).contains( categoryID);
	 * List<ProvisioningRequests> response = ProvisioningRequestsDao
	 * .getBySubscriberIDTypeAndNonDeactivatedStatus(subscriber.subID(),
	 * Integer.parseInt(categoryID)); return isODAActived && response != null; }
	 * } }catch(Exception e){ e.printStackTrace(); } return false; }
	 */
	// checks whether the subscriber has already subscribed pack corresponding
	// to the given cosDetails
	public boolean isPackActivated(Subscriber subscriber,
			CosDetails packCosDetails) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			HashMap<String, String> extraInfoMap = getExtraInfoMap(subscriber);
			if (extraInfoMap != null
					&& extraInfoMap.containsKey(EXTRA_INFO_PACK)
					&& packCosDetails != null) {
				String packCosId = packCosDetails.getCosId();
				String packCosType = packCosDetails.getCosType();
				String packStr = extraInfoMap.get(EXTRA_INFO_PACK);
				String[] packs = (packStr != null) ? packStr.trim().split(",")
						: null;
				// Parameters muiscPackCosIdParam =
				// CacheManagerUtil.getParametersCacheManager()
				// .getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
				//
				// List<String> musicPackCosIdList = null;
				// if(muiscPackCosIdParam != null) {
				// musicPackCosIdList =
				// ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
				// }

				for (int i = 0; packs != null && i < packs.length; i++) {
					String activePackCosId = packs[i];
					CosDetails activeCosDet = CacheManagerUtil
							.getCosDetailsCacheManager().getCosDetail(
									activePackCosId);
					String activeCosType = activeCosDet.getCosType();
					String activeAzaanCosSubType = confAzaanCopticDoaaCosIdSubTypeMap
							.get(activePackCosId);
					String tobeActiveAzaanCosSubType = confAzaanCopticDoaaCosIdSubTypeMap
							.get(packCosDetails.getCosId());
					String activeAzaanCosType = (activeAzaanCosSubType == null || activeAzaanCosSubType
							.isEmpty()) ? activeCosType : activeAzaanCosSubType;
					String tobeActiveAzaanCosType = (tobeActiveAzaanCosSubType == null || tobeActiveAzaanCosSubType
							.isEmpty()) ? packCosDetails.getCosType()
							: tobeActiveAzaanCosSubType;
					// Jira :RBT-15026: Changes done for allowing the multiple
					// Azaan pack.
					// Check the allow multiple azaan pack is configured or not.
					if (null != cosTypesForMultiPack
							&& !cosTypesForMultiPack.isEmpty()
							&& !activePackCosId.equalsIgnoreCase(packCosDetails
									.getCosId())
							&& (activeAzaanCosType != null
									&& tobeActiveAzaanCosType != null && activeAzaanCosType
										.equalsIgnoreCase(tobeActiveAzaanCosType))) {
						// Check the requested cosType is present in the allow
						// multiple pack or not.
						return (!cosTypesForMultiPack
								.contains(tobeActiveAzaanCosType));
					}

					if (!(MUSIC_POUCH.equalsIgnoreCase(activeCosType)
							&& MUSIC_POUCH.equalsIgnoreCase(packCosDetails
									.getCosType()) && !activePackCosId
								.equals(packCosDetails.getCosId()))
							&& !(UNLIMITED_DOWNLOADS_OVERWRITE
									.equalsIgnoreCase(activeCosType)
									&& UNLIMITED_DOWNLOADS_OVERWRITE
											.equalsIgnoreCase(packCosDetails
													.getCosType()) && !activePackCosId
										.equals(packCosDetails.getCosId()))
							&& !(LIMITED_SONG_PACK_OVERLIMIT
									.equalsIgnoreCase(activeCosType)
									&& LIMITED_SONG_PACK_OVERLIMIT
											.equalsIgnoreCase(packCosDetails
													.getCosType()) && !activePackCosId
										.equals(packCosDetails.getCosId()))
							&& (activePackCosId.equalsIgnoreCase(packCosId) || ((activeCosType != null && activeCosType
									.equalsIgnoreCase(packCosType)) && !AZAAN
									.equalsIgnoreCase(activeCosType))
									&& (isPackRequest(activeCosDet) && isPackRequest(packCosDetails)))
							|| (AZAAN.equalsIgnoreCase(activeCosType)
									&& activeAzaanCosSubType != null && activeAzaanCosSubType
										.equalsIgnoreCase(tobeActiveAzaanCosSubType))) {

						List<ProvisioningRequests> response = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriber.subID(),
										Integer.parseInt(activePackCosId));
						return response != null;
					}
				}
				return false;
			} else
				return false;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean isAutoDownloadPackActivated(Subscriber subscriber)
			throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			HashMap<String, String> extraInfoMap = getExtraInfoMap(subscriber);
			if (extraInfoMap != null
					&& extraInfoMap.containsKey(EXTRA_INFO_PACK)) {
				String packStr = extraInfoMap.get(EXTRA_INFO_PACK);
				String[] packs = (packStr != null) ? packStr.trim().split(",")
						: null;
				for (int i = 0; packs != null && i < packs.length; i++) {
					String activePackCosId = packs[i];
					CosDetails activeCosDet = CacheManagerUtil
							.getCosDetailsCacheManager().getCosDetail(
									activePackCosId);
					String activeCosType = activeCosDet.getCosType();
					if (activeCosType != null
							&& activeCosType
									.equalsIgnoreCase(COS_TYPE_AUTO_DOWNLOAD)) {
						List<ProvisioningRequests> response = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriber.subID(),
										Integer.parseInt(activePackCosId));
						return response != null;
					}
				}
				return false;
			} else
				return false;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// gives pack status with which pack is inserted in the
	// provisioning_requests table
	public int getPackStatusToInsert(Subscriber subscriber) {
		if (isSubscriberActivationPending(subscriber)
				|| (null != subscriber && subscriber.subYes().equals(
						iRBTConstant.STATE_ACTIVATION_GRACE)))
			return iRBTConstant.BASE_ACTIVATION_PENDING;
		else
			return iRBTConstant.PACK_TO_BE_ACTIVATED;
	}

	public boolean deactivateAllPack(Subscriber subscriber,
			HashMap<String, String> packExtraInfoMap) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			HashMap<String, String> extraInfoMap = getExtraInfoMap(subscriber);
			boolean isDeactivated = false;
			if (extraInfoMap != null && !extraInfoMap.isEmpty()
					&& extraInfoMap.containsKey(EXTRA_INFO_PACK)) {
				String packExtraInfo = DBUtility
						.getAttributeXMLFromMap(packExtraInfoMap);
				if (!RBTParametersUtils.getParamAsBoolean("COMMON",
						"DEL_SELECTION_ON_DEACT", "TRUE")) {
					isDeactivated = ProvisioningRequestsDao.deactivateAllPacks(
							subscriber.subID(), packExtraInfo, true, true);
				} else {
					isDeactivated = ProvisioningRequestsDao.deactivateAllPacks(
							subscriber.subID(), packExtraInfo, false, true);
				}
				if (isDeactivated)
					updateExtraInfo(subscriber.subID(), EXTRA_INFO_PACK, null);
			}
			return isDeactivated;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean deactivatePack(Subscriber subscriber,
			CosDetails packCosDetails, String internalRefId,
			HashMap<String, String> packExtraInfoMap) {
		HashMap<String, String> extraInfoMap = getExtraInfoMap(subscriber);
		boolean isDeactivated = false;
		String subscriberId = subscriber.subID();
		String packCosId = packCosDetails.getCosId();
		String packExtraInfo = DBUtility
				.getAttributeXMLFromMap(packExtraInfoMap);
		isDeactivated = ProvisioningRequestsDao.deactivatePack(subscriberId,
				packCosId, internalRefId, packExtraInfo);
		logger.info("deactivatePack response: isDeactivated=" + isDeactivated);
		return isDeactivated;
	}

	public boolean deactivateODAPack(String subscriberId, String categoryID,
			String internalRefId, HashMap<String, String> packExtraInfoMap,
			String callerId) {
		boolean isDeactivated = false;
		// String subscriberId = subscriber.subID();
		String packExtraInfo = DBUtility
				.getAttributeXMLFromMap(packExtraInfoMap);
		isDeactivated = ProvisioningRequestsDao.deactivateODAPack(subscriberId,
				categoryID, internalRefId, packExtraInfo, callerId);
		logger.info("deactivateODAPack response: isDeactivated="
				+ isDeactivated);
		return isDeactivated;
	}

	public ProvisioningRequests[] smGetActivatedPacks(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ProvisioningRequestsDao.smGetActivatedPacks(conn, fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public ProvisioningRequests[] smGetDeactivatedPacks(int fetchSize) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return ProvisioningRequestsDao.smGetDeactivatedPacks(conn,
					fetchSize);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean isSubscriberPackActivationPending(
			ProvisioningRequests provReq) {
		if (provReq == null)
			return false;
		if (provReq.getStatus() == iRBTConstant.PACK_ACTIVATION_PENDING
				|| provReq.getStatus() == iRBTConstant.PACK_TO_BE_ACTIVATED
				|| provReq.getStatus() == iRBTConstant.PACK_ACTIVATION_ERROR
				|| provReq.getStatus() == iRBTConstant.BASE_ACTIVATION_PENDING)
			return true;
		return false;
	}

	public boolean isSubscriberPackActivated(ProvisioningRequests provReq) {
		if (provReq != null
				&& (provReq.getStatus() == iRBTConstant.PACK_ACTIVATED))
			return true;
		return false;
	}

	public boolean isSubscriberPackRenewalPending(ProvisioningRequests provReq) {
		if (provReq == null)
			return false;
		return false;
	}

	public boolean isSubscriberPackDeactivationPending(
			ProvisioningRequests provReq) {
		if (provReq == null)
			return false;
		if (provReq.getStatus() == iRBTConstant.PACK_DEACTIVATION_PENDING
				|| provReq.getStatus() == iRBTConstant.PACK_TO_BE_DEACTIVATED
				|| provReq.getStatus() == iRBTConstant.PACK_DEACTIVATION_ERROR)
			return true;
		return false;
	}

	public boolean isSubscriberPackDeactivated(ProvisioningRequests provReq) {
		if (provReq == null
				|| provReq.getStatus() == iRBTConstant.PACK_DEACTIVATED)
			return true;
		return false;
	}

	public boolean isSubscriberPackInGrace(ProvisioningRequests provReq) {
		if (provReq == null)
			return false;
		if (provReq.getStatus() == iRBTConstant.PACK_GRACE)
			return true;
		return false;
	}

	public boolean isSubscriberPackSuspended(ProvisioningRequests provReq) {
		if (provReq == null)
			return false;
		if (provReq.getStatus() == iRBTConstant.PACK_SUSPEND)
			return true;
		return false;
	}

	public CosDetails getCosDetailsForContent(
			String subscriberID,
			String subscriberPacks,
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category,
			Clip clip, int status, String callerID) {
		boolean allowPremiumContent = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON, "DIRECT_ALLOW_LITE_USER_PREMIUM_CONTENT",
				"FALSE");

		// Parameters muiscPackCosIdParam =
		// CacheManagerUtil.getParametersCacheManager()
		// .getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
		//
		// List<String> musicPackCosIdList = null;
		//
		// if(muiscPackCosIdParam != null) {
		// musicPackCosIdList =
		// ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
		// }

		CosDetails cos = null;
		String[] packs = subscriberPacks.split(",");
		// boolean isDownloadLimitMusicPack = false;
		if (packs != null && packs.length > 0) {
			// String finalPackCosID = null;
			CosDetails finalCos = null;
			for (String eachPackCosID : packs) {
				finalCos = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(eachPackCosID);
				if ((finalCos != null && (UNLIMITED_DOWNLOADS_OVERWRITE
						.equalsIgnoreCase(finalCos.getCosType())
						|| MUSIC_POUCH.equalsIgnoreCase(finalCos.getCosType()) || LIMITED_SONG_PACK_OVERLIMIT
							.equalsIgnoreCase(finalCos.getCosType())))) {
					// finalPackCosID = eachPackCosID;
					cos = finalCos;
				}
			}
		}

		if (cos != null) {
			List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
					.getBySubscriberIDAndTypeOrderByCreationTime(subscriberID,
							Integer.parseInt(cos.getCosId()));

			int size = provReqList.size();
			ProvisioningRequests pack = (size > 0) ? provReqList.get(size - 1)
					: null;

			boolean isContentAllowed = DataUtils.isContentAllowedForCos(cos,
					category, clip);
			if (isContentAllowed
					&& isCosAllowedForNumMaxSels(cos,
							pack.getNumMaxSelections())) {
				logger.info("Returning cos: " + cos + " for subscriberID: "
						+ subscriberID + ", since the pack is active");
				return cos;
			}
		}

		if (cos == null && packs != null && packs.length > 0) {
			logger.info("To get cosDetails checking packs of Subscriber. packs: "
					+ packs);
			for (String eachPackCosID : packs) {
				try {
					cos = CacheManagerUtil.getCosDetailsCacheManager()
							.getCosDetail(eachPackCosID);
					int type = Integer.parseInt(eachPackCosID);
					List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
							.getBySubscriberIDAndTypeOrderByCreationTime(
									subscriberID, type);
					int size = provReqList.size();
					ProvisioningRequests pack = (size > 0) ? provReqList
							.get(size - 1) : null;
					logger.info("Verifying pack: " + pack + ", subscriberID: "
							+ subscriberID + ", cosID: " + type
							+ ", provReqList: " + provReqList);
					if (pack != null) {
						int packStatus = pack.getStatus();
						logger.info("The pack status is:  " + packStatus
								+ ", cosID: " + type + ", for subscriberID: "
								+ subscriberID);

						if (com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isPackActive(packStatus)) {
							if (status == 99) {
								if (cos.getCosType().equalsIgnoreCase(
										iRBTConstant.PROFILE_COS_TYPE)
										&& isCosAllowedForNumMaxSels(cos,
												pack.getNumMaxSelections()))
									logger.info("Returning cos:  " + cos
											+ ", Cos Type is PROFILE TYPE "
											+ "for subscriberID: "
											+ subscriberID);
								return cos;
							} else if (cos.getCosType().equalsIgnoreCase(
									COS_TYPE_AUTO_DOWNLOAD)) {
								logger.info("Cos Type is AUTO_DOWNLOAD "
										+ "for subscriberID: " + subscriberID
										+ ", verifying callerID: " + callerID
										+ " and status: " + status);
								if (status != 90
										&& (callerID == null || callerID
												.equalsIgnoreCase("all"))) {
									int maxAllowed = 0;
									Map<String, String> extraInfoMap = DBUtility
											.getAttributeMapFromXML(pack
													.getExtraInfo());
									if (extraInfoMap != null
											&& extraInfoMap
													.containsKey(EXTRA_INFO_PACK_MAX_ALLOWED))
										maxAllowed = Integer
												.parseInt(extraInfoMap
														.get(EXTRA_INFO_PACK_MAX_ALLOWED));

									boolean isContentAllowed = DataUtils
											.isContentAllowedForCos(cos,
													category, clip);
									logger.info("isContentAllowed: "
											+ isContentAllowed
											+ "for subscriberID: "
											+ subscriberID + " and status: "
											+ status);
									if (isContentAllowed
											&& (maxAllowed == 0 || (pack
													.getNumMaxSelections() < maxAllowed))) {
										logger.info("Returning cos: " + cos
												+ ", content is allowed "
												+ "for subscriberID: "
												+ subscriberID
												+ " and status: " + status);
										return cos;
									}
								}
							} else if (cos.getCosType().equalsIgnoreCase(
									PROFILE_COS_TYPE)) {
								cos = null;
							} else {
								boolean isContentAllowed = DataUtils
										.isContentAllowedForCos(cos, category,
												clip);
								if (isContentAllowed
										&& isCosAllowedForNumMaxSels(cos,
												pack.getNumMaxSelections())) {
									logger.info("Returning cos: " + cos
											+ " for subscriberID: "
											+ subscriberID
											+ ", since the pack is active");
									return cos;
								}
							}
						} else {
							logger.warn("The pack status is in active."
									+ " packStatus: " + +packStatus
									+ ", cosID: " + type
									+ ", for subscriberID: " + subscriberID);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		if (!allowPremiumContent && cos != null) {
			if (!Arrays.asList(
					RBTParametersUtils.getParamAsString(COMMON,
							"FALLBACK_TO_DEFAULT_COS_SUPPORTED_COSIDS", "")
							.split(",")).contains(cos.getCosId()))
				return cos;
		}

		logger.warn("Returning null, subscriberPack is null, subscriberId: "
				+ subscriberID + ", cos: " + cos + ", clip: " + clip
				+ ", category: " + category);
		return null;
	}

	public boolean isPackActivationPendingForContent(Subscriber subscriber,
			Categories categories, String wavFile, int status, String callerID) {
		if (categories == null || wavFile == null) {
			logger.info("Returning false, categories or wavFile is null");
			return false;
		}

		com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
				.getInstance().getCategory(categories.id());
		Clip clip = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
				.getInstance().getClipByRbtWavFileName(wavFile);

		return isPackActivationPendingForContent(subscriber, category, clip,
				status, callerID);
	}

	public boolean isPackActivationPendingForContent(
			Subscriber subscriber,
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category,
			Clip clip, int status, String callerID) {
		if (subscriber == null || subscriber.extraInfo() == null) {
			logger.info("No packs activated for the subscriber, so returning false");
			return false;
		}

		HashMap<String, String> subExtraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		String subPacks = null;
		if (subExtraInfoMap != null
				&& subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
			subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

		boolean isPackActivationPending = false;
		if (subPacks != null) {
			CosDetails cosDetail = getCosDetailsForContent(subscriber.subID(),
					subPacks, category, clip, status, callerID);
			if (cosDetail != null) {
				List<ProvisioningRequests> provisioningRequests = ProvisioningRequestsDao
						.getBySubscriberIDTypeAndNonDeactivatedStatus(
								subscriber.subID(),
								Integer.parseInt(cosDetail.getCosId()));
				if (provisioningRequests.size() > 0)
					isPackActivationPending = isSubscriberPackActivationPending(provisioningRequests
							.get(0));
			}
		}

		return isPackActivationPending;
	}

	public boolean isCosAllowedForNumMaxSels(CosDetails cosObject, int selCount) {
		if (cosObject == null || cosObject.getFreechargeClass() == null) {
			logger.warn("Returning false, cosObject or freechargeclass is null. "
					+ "cosObject: " + cosObject);
			return false;
		}

		int repeatCount = cosObject.getFreeSongs();

		List<String> chargeClassList = new ArrayList<String>();
		String[] chargeClassTokens = cosObject.getFreechargeClass().split(",");
		for (String chargeClassToken : chargeClassTokens) {
			int startIndex = chargeClassToken.indexOf('*');
			if (startIndex != -1) {
				String chargeClass = chargeClassToken.substring(0, startIndex);
				int chargeClassCount = Integer.parseInt(chargeClassToken
						.substring(startIndex + 1));
				for (int i = 0; i < chargeClassCount; i++) {
					chargeClassList.add(chargeClass);
				}
			} else {
				chargeClassList.add(chargeClassToken);
			}
		}

		int chargeClassCount = chargeClassList.size();
		if (repeatCount == 0 || (selCount < repeatCount * chargeClassCount)) {
			logger.info("Returning true, cosAllowed for max selections. "
					+ "chargeClassCount: " + chargeClassCount);
			return true;
		}

		logger.info("Returning false, repeatCount: " + repeatCount
				+ ", selCount: " + selCount + ", chargeClassCount: "
				+ chargeClassCount);
		return false;
	}

	public void reactivateDownload(String subscriberId,
			String subscriberWavFile, int categoryID, int categoryType,
			String chargeClass, String selBy, String extraInfo) {
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			SubscriberDownloadsImpl.reactivateRW(conn, subID(subscriberId),
					subscriberWavFile, categoryID, null, categoryType, true,
					chargeClass, selBy, null, extraInfo, false);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return;
	}

	// Implementation for Grameen
	public String processSuspendSubscription(String subscriberId,
			String status, boolean updatePlayerStatus, String extraInfo) {
		return null;
	}

	public boolean insertRbtLotteryEntry(int lotteryID, String subscriberID,
			Date entryTime, String lotteryNumber, int clipID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return false;
		}

		try {
			return RBTLotteryEntriesImpl.insert(conn, lotteryID, subscriberID,
					entryTime, lotteryNumber, clipID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public RBTLotteryEntries[] getUnProcessedLotteryEntries() {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return null;
		}

		try {
			return RBTLotteryEntriesImpl.getUnProcessedLotteryEntries(conn,
					RBTParametersUtils.getParamAsInt(COMMON,
							"LOTTERY_ENTRIES_FETCH_SIZE", 5000));
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public boolean updateRBTLotteryEntryDetails(int lotteryID,
			String lotteryNumber, long sequenceID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return false;
		}

		try {
			return RBTLotteryEntriesImpl.updateLotteryIdAndLotteryNumber(conn,
					lotteryID, lotteryNumber, sequenceID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public RBTLotteryEntries[] getLotteryEntriesBySubscriberID(
			String subscriberID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return null;
		}

		try {
			return RBTLotteryEntriesImpl.getLotteryEntriesBySubscriberID(conn,
					subscriberID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public boolean insertRbtLotteryNumber(int lotteryID, String lotteryNumber) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return false;
		}

		try {
			return RBTLotteryNumberImpl.insert(conn, lotteryID, lotteryNumber);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public long getCountByLotteryID(int lotteryID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return -1;
		}

		try {
			return RBTLotteryNumberImpl.getCountByLotteryID(conn, lotteryID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return -1;
	}

	public RBTLotteryNumber getOldestLotteryNumberUnderLotteryID(int lotteryID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return null;
		}

		try {
			return RBTLotteryNumberImpl.getOldestLotteryNumberUnderLotteryID(
					conn, lotteryID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public RBTLotteryNumber getOldestLotteryNumberNotUnderLotteryID(
			int lotteryID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return null;
		}

		try {
			return RBTLotteryNumberImpl
					.getOldestLotteryNumberNotUnderLotteryID(conn, lotteryID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public boolean updateRBTLotteryNumberAccessCount(long sequenceID,
			int accessCount) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return false;
		}

		try {
			return RBTLotteryNumberImpl.updateAccessCount(conn, accessCount,
					sequenceID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public boolean deleteRBTLotteryNumberBySequenceID(long sequenceID) {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return false;
		}

		try {
			return RBTLotteryNumberImpl.deleteBySequenceID(conn, sequenceID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public RBTLotteries[] getAllLotteries() {
		String lotteryDBUrl = ResourceReader.getString("rbt", "LOTTERY_DB_URL",
				null);
		RBTDBManager rbtManager = RBTDBManager.init(lotteryDBUrl, 10);
		Connection conn = rbtManager.getLotteryDBConnection();
		if (conn == null) {
			logger.debug("Connections not available for lottery DB");
			return null;
		}

		try {
			return RBTLotteriesImpl.getAllLotteries(conn);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public GCMRegistration[] getAllGCMRegistrations() {
		GCMRegistration[] gcmRegistration = getAllGCMRegistrations(null);
		return gcmRegistration;
	}

	public GCMRegistration[] getAllGCMRegistrations(String os_type) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return null;
		}

		try {
			return GCMRegistrationImpl.getAllGCMRegistrationIDs(conn, os_type);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public GCMRegistration[] getAllGCMRegistrations(int offSet, int pageCount,
			String osType) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return null;
		}

		try {
			return GCMRegistrationImpl.getAllGCMRegistrationIDs(conn, offSet,
					pageCount, osType);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean deleteGCMRegistrationBySubIDAndRegID(String regID,
			String subID) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return GCMRegistrationImpl.deleteByRegistrationIDAndSubscriberID(
					conn, regID, subID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public boolean insertGCMRegistration(String regID, String subID, String type) {
		return insertGCMRegistration(regID, subID, type, null);
	}

	public boolean insertGCMRegistration(String regID, String subID,
			String type, String notificationEnabled) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return GCMRegistrationImpl.insert(conn, regID, subID, type,
					notificationEnabled);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return false;
	}

	public Boolean getNotificationStatus(String subscriberID, String osType) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return GCMRegistrationImpl.getNotificationStatus(conn,
					subscriberID, osType);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public Boolean setNotificationStatus(String subscriberID,
			String notificationEnabled, String osType, boolean toUpdateRegId,
			String regID) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return GCMRegistrationImpl.setNotificationStatus(conn,
					subscriberID, notificationEnabled, osType, toUpdateRegId,
					regID);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public String getRegistrationIdBySubscriberIdAndType(String subscriberID,
			String type) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return null;
		}

		try {
			return GCMRegistrationImpl.getRegistrationIdBySubscriberIdAndType(
					conn, subscriberID, type);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public Boolean updateRegistrationIdBySubscriberIdAndType(
			String subscriberId, String type, String registrationId) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return GCMRegistrationImpl
					.updateRegistrationIdBySubscriberIdAndType(conn,
							subscriberId, type, registrationId);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}

	public boolean updateGroupMemberName(String memberName, int groupID,
			String memberID) {
		Connection conn = null;
		try {
			conn = getConnection();
			return GroupMembersImpl.updateGroupMemberName(conn, groupID,
					memberID, memberName);
		} finally {
			releaseConnection(conn);
		}
	}

	public boolean updateRetryCountAndTimeForSubscriber(String subscriberID,
			String retryCount, Date retryTime) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return SubscriberImpl.updateRetryCountAndTime(conn, subscriberID,
					retryCount, retryTime);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return true;
	}

	public boolean updateRetryCountAndTimeForDownload(String subscriberID,
			String refID, String retryCount, Date retryTime) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return SubscriberDownloadsImpl.updateRetryCountAndTime(conn,
					subscriberID, refID, retryCount, retryTime);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateRetryCountAndTimeForSelection(String subscriberID,
			String refID, String retryCount, Date retryTime) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return SubscriberStatusImpl.updateRetryCountAndTime(conn,
					subscriberID, refID, retryCount, retryTime);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean updateRetryCountAndTimeForPack(String subscriberID,
			String refID, String retryCount, Date retryTime) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}

		try {
			return ProvisioningRequestsDao.updateRetryCountAndTime(conn,
					subscriberID, refID, retryCount, retryTime);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean isunBlockedOrDoubleConsentMode(String mode) {
		// RBT-12199
		if (unblockProvisiongRequestModes.size() > 0
				&& !unblockProvisiongRequestModes.contains(mode)
				&& !unblockProvisiongRequestModes.contains("ALL"))
			return false;
		return true;
	}

	public boolean isViralSmsTypeListForNewTable(List<String> smsTypeList) {
		boolean isNewViralSMSTableFlow = false;
		if (smsTypeList == null || viralSmsTypeListForNewTable == null) {
			return isNewViralSMSTableFlow;
		}
		for (String type : smsTypeList) {
			if (viralSmsTypeListForNewTable.contains(type)) {
				isNewViralSMSTableFlow = true;
				break;
			}
		}
		return isNewViralSMSTableFlow;
	}

	// RBT-10315 checking monthly overlapping
	private boolean isMonthlyIntervalOverlapping(String selIntervalnew,
			String selIntervalold) {
		logger.info("isMonthlyIntervalOverlapping() for selIntervalnew and selIntervalold :"
				+ selIntervalnew + " and " + selIntervalold);
		try {
			if (selIntervalnew != null && selIntervalold != null) {
				String[] oldintervals = selIntervalold.split(",");
				String[] newintervals = selIntervalnew.split(",");
				int oldDaystart = 0;
				int oldDayend = 0;
				int newDaystart = 0;
				int newDayend = 0;
				if (oldintervals != null && oldintervals.length == 2) {
					oldDaystart = dayofyear(getmonth(oldintervals[0]),
							Integer.parseInt(oldintervals[0]
									.substring(oldintervals[0].length() - 2)));
					oldDayend = dayofyear(getmonth(oldintervals[1]),
							Integer.parseInt(oldintervals[1]
									.substring(oldintervals[1].length() - 2)));
				}
				if (newintervals != null && newintervals.length == 2) {
					newDaystart = dayofyear(getmonth(newintervals[0]),
							Integer.parseInt(newintervals[0]
									.substring(newintervals[0].length() - 2)));
					newDayend = dayofyear(getmonth(newintervals[1]),
							Integer.parseInt(newintervals[1]
									.substring(newintervals[1].length() - 2)));
				}

				if ((newDaystart >= oldDaystart && newDaystart <= oldDayend)
						|| (newDayend >= oldDaystart && newDayend <= oldDayend)
						|| (newDaystart <= oldDaystart && newDayend >= oldDayend)) {
					return true;
				}
				if (oldDaystart >= oldDayend) {
					if ((newDaystart >= oldDaystart && newDayend >= oldDaystart)
							|| (newDaystart <= oldDayend && newDayend <= oldDayend)
							|| (newDaystart >= oldDaystart && newDayend <= oldDayend)
							|| (newDaystart <= oldDaystart && newDayend >= oldDaystart)
							|| (newDaystart <= oldDayend && newDayend >= oldDayend)
							|| (newDaystart >= oldDayend && newDayend >= oldDayend)
							|| (newDaystart <= oldDaystart && newDayend <= oldDaystart)) {
						return true;
					}
				}

			}
		} catch (Exception e) {
			logger.info("Exception occured in isMonthlyIntervalOverlapping(): "
					+ e);
			return false;
		}
		return false;

	}

	// RBT-10315 Returning month
	private int getmonth(String interval) {
		int month = -1;
		if (interval != null) {

			if (interval.length() == 5) {
				month = Integer.parseInt(interval.substring(1, 3));
			} else if (interval.length() == 4) {
				month = Integer.parseInt(interval.substring(1, 2));
			}
		}
		logger.info("Returning month for interval: " + month);
		return month;
	}

	// RBT-10315 returning day of year
	public int dayofyear(int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DATE, day);
		SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
		day = convertToDay(df.format(c.getTime()));
		logger.info("Returning day for interval: " + day);
		return day;
	}

	// RBT-10315 converting date to day
	private int convertToDay(String unformattedDate) {
		/* Unformatted Date: ddmmyyyy */
		int resultday = 0;
		if (unformattedDate.length() > 0) {
			/* Days of month */
			int[] monthValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
					31 };
			String dayS, monthS, yearS;
			dayS = unformattedDate.substring(0, 2);
			monthS = unformattedDate.substring(2, 4);
			yearS = unformattedDate.substring(4, 8);
			/* Convert to Integer */
			int day = Integer.valueOf(dayS);
			int month = Integer.valueOf(monthS);
			int year = Integer.valueOf(yearS);

			// Leap year check
			if (year % 4 == 0) {
				monthValues[1] = 29;
			}
			for (int i = 0; i < month - 1; i++) {
				resultday += monthValues[i];
			}
			resultday += day;

		}
		logger.info("Returning day:" + resultday);
		return resultday;
	}

	public boolean isOverwirteSongPack(String subscriberID,
			WebServiceContext task) {
		String cosId = null;
		Subscriber subscriber = null;
		if (task != null
				&& task.containsKey(WebServiceConstants.param_subscriber))
			subscriber = (Subscriber) task
					.get(WebServiceConstants.param_subscriber);
		else
			subscriber = getSubscriber(subscriberID);

		HashMap<String, String> extraInfo = getExtraInfoMap(subscriber);
		if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PACK)) {
			String subscriberCosIds = extraInfo.get(EXTRA_INFO_PACK);

			// Get the entries from provisioning requests table by
			// subscriber id and type i.e. cosid if status is 33
			// then update sub_type to azaan.
			List<String> subscriberCosIdList = ListUtils.convertToList(
					subscriberCosIds, ",");
			logger.debug("subscriber Extrainfo cosIds: " + subscriberID);
			// One of the subscriber cos id is MusicPack cos. So, update the
			// player status.
			Iterator<String> iterator = subscriberCosIdList.iterator();
			while (iterator.hasNext()) {
				String packCosId = iterator.next();
				int cos = Integer.parseInt(packCosId);
				CosDetails finalCos = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(packCosId);
				if (finalCos != null
						&& finalCos.getCosType() != null
						&& finalCos.getCosType().equalsIgnoreCase(
								iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)) {
					cosId = finalCos.getCosId();
				}
			}
		}

		if (task != null && isSubscriberDeactivated(subscriber)
				&& task.containsKey(WebServiceConstants.param_cosID)) {
			String tempCosId = task.getString(WebServiceConstants.param_cosID);
			CosDetails finalCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(tempCosId);
			if (finalCos != null
					&& finalCos.getCosType() != null
					&& finalCos.getCosType().equalsIgnoreCase(
							iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)) {
				cosId = tempCosId;
			}
		}

		if (task != null && isSubscriberDeactivated(subscriber)
				&& task.containsKey(WebServiceConstants.param_packCosId)) {
			String tempCosId = task
					.getString(WebServiceConstants.param_packCosId);
			CosDetails finalCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(tempCosId);
			if (finalCos != null
					&& finalCos.getCosType() != null
					&& finalCos.getCosType().equalsIgnoreCase(
							iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)) {
				cosId = tempCosId;
			}
		}

		CosDetails cosDetails = null;
		if (cosId != null) {
			cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosId);
			if (cosDetails != null
					&& cosDetails.getCosType() != null
					&& cosDetails.getCosType().equalsIgnoreCase(
							iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)) {
				logger.info("User activated UNLIMITED_DOWNLOADS_OVERWRITE pack, then old song will get overwrite subscriberId: "
						+ subscriberID + " cosDetail: " + cosDetails);
				return true;
			}
		}

		return false;
	}

	public void deactivateActiveSubscriberDownloads(String subID) {
	}

	public void removeDeactivateSubscriberDownloads(String subID) {
	}

	public HashMap<String, HashMap<String, List<String>>> getAccessDetails(
			String userID, String passWord) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return null;
		}
		try {
			return UserApiAccessImpl.getAccessDetails(conn, userID, passWord);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public boolean makeEntryInConsent(String transID, String subscriberID,
			String callerID, String categoryId, String subClass,
			String selectedBy, Date startTime, Date endTime, int status,
			String chargeClassType, String cosID, String packCosId,
			String clipId, String selInterval, int fromTime, int toTime,
			String selectionInfo, int selType, boolean inLoop,
			String purchageType, boolean useUIChargeClass, int categoryType,
			String profileHours, boolean isPrepaid, String feedType,
			String waveFile, int rbtType, String circleID, String language,
			Date requestDate, String extraInfoMap, String requestType,
			int consentSatus) {
		Connection conn = getConnection();
		if (conn == null) {
			logger.debug("Connections not available");
			return false;
		}
		try {
			return ConsentTableImpl.insertSelectionRecord(conn, transID,
					subscriberID, callerID, categoryId, subClass, selectedBy,
					startTime, endTime, status, chargeClassType, cosID,
					packCosId, clipId, selInterval, fromTime, toTime,
					selectionInfo, selType, inLoop, purchageType,
					useUIChargeClass, categoryType, profileHours, isPrepaid,
					feedType, waveFile, rbtType, circleID, language,
					requestDate, extraInfoMap, requestType, consentSatus,null, null);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	// RBT-11752
	public ArrayList<SubscriberStatus> getSelectionBySubsIdAndCatIdAndCallerIdAndRefId(
			String subId, int catId, String callerId, String refId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		ArrayList<SubscriberStatus> subscriberStatusListByRefId = null;
		try {
			subscriberStatusListByRefId = SubscriberStatusImpl
					.getSelectionBySubsIdAndCatIdAndCallerIdAndRefId(conn,
							subId, catId, callerId, refId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatusListByRefId;
	}

	public boolean updateProvisioningRequestsStatus(String subscriberID,
			String refId, int status) {

		Connection conn = getConnection();
		boolean updateStatus = false;
		if (conn == null)
			return false;

		try {
			updateStatus = ProvisioningRequestsDao.updateStatus(conn,
					subscriberID, refId, status);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return updateStatus;
	}

	public boolean insertNewSelectionsforODA(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String classType, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, int fromTime, int toTime,
			boolean smActivation, String sel_status, String deSelectedBy,
			String oldClassType, int categoryType, char loopStatus,
			int nextPlus, int rbtType, String selInterval, String extraInfo,
			String refID, String circleId) {

		Connection conn = getConnection();
		SubscriberStatus updateStatus;
		if (conn == null)
			return false;

		try {
			updateStatus = SubscriberStatusImpl.insert(conn,
					subID(subscriberID), callerID, categoryID,
					subscriberWavFile, null, startTime, endTime, status,
					classType, selectedBy, selectionInfo, nextChargingDate,
					prepaid, fromTime, toTime, true, sel_status, null, null,
					categoryType, loopStatus, nextPlus, rbtType, selInterval,
					extraInfo, refID, circleId, null, null);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return true;

	}

	// RBT-12419
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
			String subscriberID, Map<String, String> whereClauseMap) {

		Connection conn = getConnection();
		SubscriberStatus subscriberStatus = null;
		if (conn == null)
			return null;

		try {
			subscriberStatus = SubscriberStatusImpl
					.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
							conn, subscriberID, whereClauseMap);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;

	}

	// RBT-2.0
	public int getSubActDwnldsCount(String subscriberId,
			Map<String, String> whereClauseMap) {
		int rowCount = 0;
		SubscriberDownloads subscriberDownloads = null;
		Connection conn = getConnection();
		if (conn == null)
			return rowCount;
		try {
			rowCount = SubscriberDownloadsImpl
					.getActiveSubscriberDownloadsCount(subscriberId, conn,
							whereClauseMap);
		} catch (Throwable t) {
			logger.error("Exception Occured: " + t, t);
		} finally {
			releaseConnection(conn);
		}
		return rowCount;
	}

	// RBT-12419
	public boolean removeSubscriberDownloadBySubIdAndWavFileAndCatId(
			String subscriberID, String promoID, int categoryId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean subscriberStatus = false;
		try {
			subscriberStatus = SubscriberDownloadsImpl
					.removeSubscriberDownloadBySubIdAndWavFileAndCatId(conn,
							subscriberID, promoID, categoryId);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	// RBT-13544 TEF ES - Mi Playlist functionality
	public SubscriberStatus[] getActiveNormalSelByCallerIdAndByStatus(
			String subscriberID, String callerID, int status) {
		return null;
	}

	public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(
			String subscriberID, String downloadStatus) {
		return null;
	}

	public boolean updateDownloadStatusByDownloadStatus(String subscriberID,
			String promoID, String deselectedBy, String callerId, int status,
			String downloadStatus, String oldDownloadStatus, int catID,
			int catType) {
		return false;
	}

	public SubscriberDownloads getActiveSubscriberDownloadByStatus(
			String subscriberId, String promoId, String downloadStatus,
			int categoryID, int categoryType) {
		return null;
	}

	public String[] getChargeClassForShuffleCatgory(String subscriberId,
			Subscriber consentSubscriber, Categories categories,
			ClipMinimal clip, boolean incrSelCount, String subscriberWavFile,
			boolean isPackSel, String packCosID, String selBy,
			HashMap<String, String> extraInfo, String nextClass,
			String classType) {
		incrSelCount = false;
		String[] chargeClassStr = new String[5];
		Category category = getCategory(categories.id());
		String catChargeClass = null;
		if (category != null && category.getClassType() != null)
			catChargeClass = category.getClassType();
		if (catChargeClass == null || catChargeClass.equalsIgnoreCase("null")
				|| catChargeClass.equals(""))
			catChargeClass = "DEFAULT";
		ChargeClass charge = CacheManagerUtil.getChargeClassCacheManager()
				.getChargeClass(classType);
		ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager()
				.getChargeClass(catChargeClass);

		if ((classType == null || classType.equalsIgnoreCase("DEFAULT"))
				&& charge != null && catCharge != null
				&& charge.getAmount() != null && catCharge.getAmount() != null) {
			try {
				String firstAmountStr = charge.getAmount();
				String secondAmountStr = catCharge.getAmount();
				firstAmountStr = firstAmountStr.replace(",", ".");
				secondAmountStr = secondAmountStr.replace(",", ".");

				float firstAmount = Float.parseFloat(firstAmountStr);
				float secondAmount = Float.parseFloat(secondAmountStr);
				if ((firstAmount < secondAmount)
						|| (m_overrideChargeClasses != null && m_overrideChargeClasses
								.contains(catChargeClass.toLowerCase())))
					classType = catChargeClass;
			} catch (Throwable e) {
			}
		}
		nextClass = classType;
		chargeClassStr[0] = (incrSelCount == true ? "true" : "false");
		chargeClassStr[1] = (isPackSel == true ? "true" : "false");
		chargeClassStr[2] = nextClass;
		chargeClassStr[3] = classType;
		chargeClassStr[4] = packCosID;

		return chargeClassStr;
	}

	public String[] getChargeClassForNonShuffleCatgory(String subscriberId,
			Subscriber consentSubscriber, Categories categories,
			ClipMinimal clip, boolean incrSelCount, String subscriberWavFile,
			boolean isPackSel, String packCosID, String selBy,
			HashMap<String, String> extraInfo, String nextClass,
			String classType) {
		String[] chargeClassStr = new String[5];
		int categoryID = categories.id();
		Subscriber subscriber = getSubscriber(subID(subscriberId));
		if (consentSubscriber != null) {
			subscriber = consentSubscriber;
		}
		if (isCosToBeIgnored(categories, clip, subscriber)) {
			incrSelCount = false;
			nextClass = "DEFAULT";
		} else {
			String subPacks = null;
			HashMap<String, String> subExtraInfoMap = null;
			if (subscriber != null) {
				subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber
						.extraInfo());
			}
			if (subExtraInfoMap != null
					&& subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
				subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

			if (subPacks != null) {
				com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
						.getInstance().getCategory(categories.id());
				Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
						.getInstance().getClipByRbtWavFileName(
								subscriberWavFile);
				CosDetails cosDetail = getCosDetailsForContent(subscriberId,
						subPacks, category, clipObj, 1, null);
				if (extraInfo != null && extraInfo.containsKey("OFFER_ID")
						&& cosDetail != null
						&& MUSIC_POUCH.equalsIgnoreCase(cosDetail.getCosType())) {
					extraInfo.remove("PACK");
					cosDetail = null;
				}
				List<ProvisioningRequests> packList = null;
				if (cosDetail != null) {

					if ((iRBTConstant.LIMITED_DOWNLOADS
							.equalsIgnoreCase(cosDetail.getCosType()) || iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
							.equalsIgnoreCase(cosDetail.getCosType()))
							&& affiliateModeSet.contains(selBy)
							&& RBTParametersUtils.getParamAsBoolean("COMMON",
									"IS_MP_BYPASSED_FOR_AFFILIATE", "FALSE")) {
						// implies music pack user, and mode is affiliate and
						// affiliate requests have to bypass mp charging logic,
						// let packList remain null
					} else
						packList = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriberId,
										Integer.parseInt(cosDetail.getCosId()));
				}
				if (packList != null
						&& (isSubscriberPackActivated(packList.get(0)) || isSubscriberPackActivationPending(packList
								.get(0)))) {
					int selCount = subscriber.maxSelections();
					if (isPackRequest(cosDetail)) {
						selCount = packList.get(0).getNumMaxSelections();
						if (cosDetail.getFreeSongs() > selCount)
							isPackSel = true;
					}

					nextClass = getChargeClassFromCos(cosDetail, selCount);
					packCosID = cosDetail.getCosId();
				} else {
					nextClass = getNextChargeClass(subscriber);
				}
			} else {
				nextClass = getNextChargeClass(subscriber);
			}

			if (nextClass == null) {
				chargeClassStr[2] = SELECTION_FAILED_INTERNAL_ERROR;
				return chargeClassStr;
			}
			if (!nextClass.equalsIgnoreCase("DEFAULT"))
				classType = nextClass;
		}

		if (nextClass == null) {
			chargeClassStr[2] = "FAILURE:TECHNICAL_FAULT";
			return chargeClassStr;
		}
		if (nextClass.equalsIgnoreCase("DEFAULT"))
			nextClass = classType;
		if (nextClass == null || nextClass.equalsIgnoreCase("DEFAULT")) {

			Category category = getCategory(categoryID);
			if ((classType == null || classType.equalsIgnoreCase("DEFAULT"))
					&& category != null && category.getClassType() != null)
				classType = category.getClassType();

			if (classType == null || classType.equalsIgnoreCase("null")
					|| classType.equals(""))
				classType = "DEFAULT";
			ChargeClass charge = CacheManagerUtil.getChargeClassCacheManager()
					.getChargeClass(classType);
			ChargeClass clipCharge = null;
			String clipClassType = null;
			if (clip != null && clip.getClassType() != null
					&& !clip.getClassType().equalsIgnoreCase("DEFAULT")) {
				clipClassType = clip.getClassType();
				clipCharge = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(clipClassType);
			}

			if (charge != null && clipCharge != null
					&& charge.getAmount() != null
					&& clipCharge.getAmount() != null) {
				try {
					String firstAmountStr = charge.getAmount();
					String secondAmountStr = clipCharge.getAmount();
					firstAmountStr = firstAmountStr.replace(",", ".");
					secondAmountStr = secondAmountStr.replace(",", ".");

					float firstAmount = Float.parseFloat(firstAmountStr);
					float secondAmount = Float.parseFloat(secondAmountStr);
					if ((firstAmount < secondAmount)
							|| (m_overrideChargeClasses != null && m_overrideChargeClasses
									.contains(clipClassType.toLowerCase())))
						classType = clipClassType;
				} catch (Throwable e) {
				}
			}
			nextClass = classType;
		}
		chargeClassStr[0] = (incrSelCount == true ? "true" : "false");
		chargeClassStr[1] = (isPackSel == true ? "true" : "false");
		chargeClassStr[2] = nextClass;
		chargeClassStr[3] = classType;
		chargeClassStr[4] = packCosID;

		return chargeClassStr;
	}

	public String getCosChargeClass(
			Subscriber subscriber,
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category,
			Clip clip, CosDetails cos) {
		// String classType = null;
		// if (((category == null) ||
		// (!com.onmobile.apps.ringbacktones.webservice.common.Utility
		// .isShuffleCategory(category.getCategoryTpe())))
		// && (!getInstance().isCosToBeIgnored(cos, category, clip))) {
		// int selectionCount = 0;
		// if (RBTDBManager.getInstance().isSubActive(subscriber)) {
		// selectionCount = subscriber.maxSelections();
		// }
		// classType = RBTDBManager.getInstance().getChargeClassFromCos(cos,
		// selectionCount);
		// }
		// return classType;
		return getCosChargeClass(subscriber, category, clip, cos, null);
	}

	public String directDeactivateSubscriberRecordsByRefId(String subscriberID,
			String deactBy, String refId, Character newLoopStatus) {
		return null;
	}

	public String updateSelectionExtraInfoAndRefId(String subscriberId,
			String newExtraInfo, String oldRefId, String newRefId) {
		return null;
	}

	public boolean deactSelectionsAndDeleteDownloadForRenewalFlow(
			Connection conn, String deactivateBy, SubscriberDownloads download) {
		return false;
	}

	// RBT-14044 VF ES - MI Playlist functionality for RBT core
	public String removeMiPlaylistDownloadTrack(String subscriberID,
			String promoID, int categoryID, int categoryType, String callerId,
			int status) {
		return null;
	}

	public String addDownloadForTrackingMiPlaylist(String subID,
			String promoID, int catID, int catType, String refID,
			String classType, String selBy, int status, int selType) {
		return null;
	}

	public char getLoopStatusForNewMiPlayListSelection(String subscriberID,
			int status, String callerID, int catType, char loopStatus) {
		return loopStatus;
	}

	public void addOldMiplayListSelections(SubscriberStatus subscriberStatus) {

	}

	public String deleteDownloadwithTstatus(String subscriberID, String wavFile) {
		return null;
	}

	public SubscriberStatus[] getPendingDefaultSubscriberSelections(
			String subID, String callerID, int status, String shuffleSetTime) {
		return null;
	}

	public void addTrackingOfPendingSelections(SubscriberStatus subscriberStatus) {

	}

	public boolean removeDownloadsWithTStatus(String subscriberID) {
		return false;
	}

	public void deactivateOldODAPackOnSuccessCallback(String strSubID,
			String refID, String callerID, int categoryType,
			SubscriberStatus subStatus, boolean odaPackSelectionCallback,
			String extraInfo) {
		logger.info("deactivateOldODAPackOnSuccessCallback = SubscriberID = "
				+ strSubID + " ,RefId = " + refID + " , callerID = " + callerID
				+ ", extraInfo = " + extraInfo);
		if (!odaPackSelectionCallback && categoryType == PLAYLIST_ODA_SHUFFLE) {
			return;
		}

		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		int fromTime = 0;
		int toTime = 2359;
		int subSelstatus = 1;
		String selInterval = null;
		if (extraInfoMap != null) {
			if (extraInfoMap.containsKey("FROM_TIME")) {
				fromTime = Integer.parseInt(extraInfoMap.get("FROM_TIME"));
			}
			if (extraInfoMap.containsKey("TO_TIME")) {
				toTime = Integer.parseInt(extraInfoMap.get("TO_TIME"));
			}
			if (extraInfoMap.containsKey("STATUS")) {
				subSelstatus = Integer.parseInt(extraInfoMap.get("STATUS"));
			}
			if (extraInfoMap.containsKey("SEL_INTERVAL")) {
				selInterval = extraInfoMap.get("SEL_INTERVAL");
			}
		}

		if (extraInfo == null && subStatus != null) {
			fromTime = subStatus.fromTime();
			toTime = subStatus.toTime();
			subSelstatus = subStatus.status();
			selInterval = subStatus.selInterval();
		}
		ProvisioningRequests currPack = RBTDBManager.getInstance()
				.getProvisioningRequestFromRefId(strSubID, refID);
		List<ProvisioningRequests> activeODAPackBySubscriberID = RBTDBManager
				.getInstance().getActiveODAPackBySubscriberID(strSubID);
		if (activeODAPackBySubscriberID != null
				&& activeODAPackBySubscriberID.size() > 0) {
			for (ProvisioningRequests provReq : activeODAPackBySubscriberID) {
				String refid = provReq.getTransId();
				if (!refid.equalsIgnoreCase(refID)
						&& (currPack == null || (currPack != null && provReq
								.getCreationTime().before(
										currPack.getCreationTime())))) {
					if (subStatus != null) {
						String xtraInfo = subStatus.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(xtraInfo);
						if (xtraInfoMap != null
								&& xtraInfoMap.containsKey("PROV_REF_ID")) {
							String provRefId = xtraInfoMap.get("PROV_REF_ID");
							if (provRefId.equalsIgnoreCase(refid))
								continue;
						}
					}
					String packExtraInfo = provReq.getExtraInfo();
					HashMap<String, String> packExtraInfoMap = DBUtility
							.getAttributeMapFromXML(packExtraInfo);
					int status = provReq.getStatus();
					logger.info("ODA Pack status = " + status);
					String packCallerId = null;
					if (packExtraInfoMap != null) {
						packCallerId = packExtraInfoMap.get("CALLER_ID");
					}

					int packFromTime = 0;
					int packToTime = 2359;
					int packSelStatus = 1;
					String packSelInterval = null;

					if (packExtraInfoMap != null) {
						if (packExtraInfoMap.containsKey("FROM_TIME")) {
							packFromTime = Integer.parseInt(packExtraInfoMap
									.get("FROM_TIME"));
						}
						if (packExtraInfoMap.containsKey("TO_TIME")) {
							packToTime = Integer.parseInt(packExtraInfoMap
									.get("TO_TIME"));
						}
						if (packExtraInfoMap.containsKey("STATUS")) {
							packSelStatus = Integer.parseInt(packExtraInfoMap
									.get("STATUS"));
						}
						if (packExtraInfoMap.containsKey("SEL_INTERVAL")) {
							packSelInterval = packExtraInfoMap
									.get("SEL_INTERVAL");
						}
					}

					if (!(fromTime == packFromTime && toTime == packToTime
							&& subSelstatus == packSelStatus && ((selInterval == null && packSelInterval == null) || (selInterval != null
							&& packSelInterval != null && selInterval
								.equalsIgnoreCase(packSelInterval))))) {
						continue;
					}

					if (packExtraInfoMap == null) {
						packExtraInfoMap = new HashMap<String, String>();
					}
					packExtraInfoMap.put(
							iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE,
							"SM");
					packExtraInfoMap.put(
							iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME,
							new Date().toString());
					packExtraInfo = DBUtility
							.getAttributeXMLFromMap(packExtraInfoMap);
					if ((packCallerId == null && callerID == null)
							|| (packCallerId != null && packCallerId
									.equalsIgnoreCase(callerID))) {
						if (odaPackSelectionCallback
								&& (status == 32 || status == 33
										|| status == 34 || status == 35 || status == 50)) {
							RBTDBManager.getInstance().deactivateActiveODAPack(
									strSubID, refid, packExtraInfo);
						} else if (status == 30 || status == 31) {
							RBTDBManager.getInstance()
									.directDeactivateActiveODAPack(strSubID,
											packCallerId, refid, packExtraInfo);
						} else {
							RBTDBManager.getInstance().deactivateActiveODAPack(
									strSubID, refid, packExtraInfo);
						}
					}
				}
			}
		}

	}

	/**
	 * For RBT - 14835
	 * 
	 * @param cosIdOrUdsType
	 * @param contentTypes
	 * @author ajay.kanwal This API will check whether content type is blocked
	 *         for this cosid
	 * 
	 * @param cosId
	 * @param contentType
	 * 
	 */
	public boolean isContentTypeBlockedForCosIdorUdsType(String cosIdOrUdsType,
			List<String> contentTypes) {
		boolean isBlocked = false;
		logger.info("cosid/uds type :: " + cosIdOrUdsType + " Content type :: "
				+ contentTypes);

		if (null == cosIdOrUdsType || contentTypes == null
				|| contentTypes.isEmpty()) {
			return isBlocked;
		}

		String configFromParamaters = WebServiceConstants.PREFIX_PRIMIUM_CONTENT_NOTALLOWED_CONFIG
				+ cosIdOrUdsType.toUpperCase();

		String blockedContentTypeCosIdOrUdsStr = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.COMMON, configFromParamaters, null);
		if (null == blockedContentTypeCosIdOrUdsStr) {
			return isBlocked;
		} else {
			blockedContentTypeCosIdOrUdsStr = blockedContentTypeCosIdOrUdsStr
					.toUpperCase();
			List<String> contentTypesBlockedForCosIdorUds = ListUtils
					.convertToList(blockedContentTypeCosIdOrUdsStr, ",");
			for (String type : contentTypes) {
				if (contentTypesBlockedForCosIdorUds.contains(type
						.toUpperCase())) {
					isBlocked = true;
					break;
				}
			}
		}
		return isBlocked;
	}

	public boolean isFirstProfileSong(List<String> overridableSelectionStatus,
			int status, int selcount) {
		boolean result = false;
		if (overridableSelectionStatus.contains("" + status)) {
			result = true;
		}
		return result;
	}

	// RBT-14652
	public List<DoubleConfirmationRequestBean> getDoubleConfirmationRequestBeanForSAT() {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl
					.getDoubleConfirmationRequestBeanForSAT(conn);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}

	public List<DoubleConfirmationRequestBean> getAllDoubleConfirmationRequestBeanForSATUpgrade(
			String subscriberID, String consentStatus, String transId) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> contentRequests = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			contentRequests = ConsentTableImpl
					.getAllDoubleConfirmationRequestBeanForSATUpgrade(conn,
							subscriberID, consentStatus, transId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return contentRequests;
	}

	public boolean updateExtraInfoAndStatusWithReqTime(String subscriberID,
			String transId, String extraInfo, String consentStatus, Date date)
			throws OnMobileException {
		Connection conn = getConnection();
		boolean success = false;
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}
		success = ConsentTableImpl.updateExtraInfoAndStatusWithReqTime(conn,
				subscriberID, transId, extraInfo, consentStatus, date);
		releaseConnection(conn);
		return success;

	}

	public DoubleConfirmationRequestBean getLatestDoubleConfirmationRequestBeanForSAT(
			String subscriberID) {
		Connection conn = getConnection();
		List<DoubleConfirmationRequestBean> consentRequestBeans = null;
		DoubleConfirmationRequestBean consentRequestBean = null;
		if (conn == null) {
			logger.info("No Connection to DB");
			return null;
		}
		try {
			consentRequestBeans = ConsentTableImpl
					.getLatestDoubleConfirmationRequestBeanForSAT(conn,
							subscriberID);
			if (consentRequestBeans == null) {
				logger.info("consentRequestBeans is null:"
						+ consentRequestBeans);
				return null;
			}
			for (DoubleConfirmationRequestBean requestBean : consentRequestBeans) {
				Subscriber subscriber = getSubscriber(subscriberID);
				String status = com.onmobile.apps.ringbacktones.webservice.common.Utility
						.getSubscriberStatus(subscriber);
				boolean isSubActive = com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isUserActive(status);
				if ("SEL".equalsIgnoreCase(requestBean.getRequestType())
						&& !isSubActive) {
					continue;
				} else {
					consentRequestBean = requestBean;
					break;
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("consentRequestBean:" + consentRequestBean);
		return consentRequestBean;
	}

	// RBT-15149
	public ViralSMSTable getAllViralSMS(String subscriberID, String[] type,
			int duration, boolean order) {
		Connection conn = getConnection();
		ViralSMSTable viralSMS = null;
		if (conn == null)
			return null;
		try {
			if (viralSmsTypeListForNewTable != null && type != null) {
				if (viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.getAllViralSMS(conn, subscriberID,
							type, duration, order);

				}
			}
			return ViralSMSTableImpl.getAllViralSMS(conn, subscriberID, type,
					duration, order);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return null;
	}
	
	//RBT-16453
	   public List<SubscriberStatus> getSubscriberActiveSelections(
	           String subscriberID, Map<String, String> whereClauseMap) {
	 
	       Connection conn = getConnection();
	       List<SubscriberStatus> subscriberStatus = null;
	       if (conn == null)
	           return null;
	 
	       try {
	           subscriberStatus = SubscriberStatusImpl.getSubscriberActiveSelections(
	                           conn, subID(subscriberID), whereClauseMap);
	 
	       } catch (Throwable e) {
	           logger.error("Exception before release connection", e);
	       } finally {
	           releaseConnection(conn);
	       }
	       return subscriberStatus;
	 
	   }

	 //added for rbt 2
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(String subscriberID, String id, boolean isCatId){
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownloadByCatIdOrPromoId(conn,
					subID(subscriberID), id, isCatId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	 public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id, String key){

			Connection conn = getConnection();
			if (conn == null)
				return null;

			try {
				return SubscriberStatusImpl.getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(conn, subscriberID, id, key);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			return null;
	 }
	 
	 public List<SubscriberStatus> getDistinctActiveSelections(Map<String, String> whereClauseMap) {
		 Connection connection = getConnection();
		 if(connection == null)
			 return null;
		 
		 List<SubscriberStatus> subscriberStatusList = null;
		 try {
			 subscriberStatusList = SubscriberStatusImpl.getDistinctActiveSelections(connection, whereClauseMap);
		 } catch (Throwable e) {
			 logger.error("Exception before release connection", e);
		 } finally {
			 releaseConnection(connection);
		 }
		 return subscriberStatusList;
	 }
	
	//RBT-17084
	public boolean deleteDownloadwithTstatusAndCategoryType(String subscriberId,
			String categoryIdstr) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.deleteDownloadwithTstatusAndCategoryType(conn,
							subID(subscriberId),categoryIdstr);		
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	//Added for selection model
	public SubscriberDownloads[] getSubscriberDownloadsWithoutTrack(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownloadsWithoutTrack(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	private String updateConsentBaseExtraInfo(
			DoubleConfirmationRequestBean doubleConfirmReqBean, String refID,
			String selectedBy, Boolean modeCheckForVfUpgrade,
			String oldSubscriptionClass ,String  baseConsentStatus) throws OnMobileException {
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(doubleConfirmReqBean.getExtraInfo());
		if (extraInfoMap == null) {
			extraInfoMap = new HashMap<String, String>();
		}
		extraInfoMap.put("TRANS_ID", refID);
		String extraInfoXml = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		updateConsentExtrInfoAndStatus(doubleConfirmReqBean.getSubscriberID(),
				doubleConfirmReqBean.getTransId(), extraInfoXml,
				baseConsentStatus + "");
		Map<String, String> mappedModeMap = MapUtils
				.convertToMap(RBTParametersUtils.getParamAsString(
						"DOUBLE_CONFIRMATION",
						"SWAPPED_MODES_MAPPING_FOR_CONSENT", ""), ";", "=", ",");
		if (selectedBy != null && mappedModeMap != null
				&& mappedModeMap.containsKey(selectedBy.toUpperCase())) {
			selectedBy = mappedModeMap.get(selectedBy.toUpperCase());
		}
		if (modeCheckForVfUpgrade) {
			selectedBy = VfRBTUpgardeConsentFeatureImpl
					.getMappedModeForUpgrade(oldSubscriptionClass,
							doubleConfirmReqBean.getSubscriptionClass(),
							selectedBy);
		}
		return selectedBy;
	}
	
	public OperatorUserDetails getUserDetails(String subscriberID) throws RBTException {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return null;
			return OperatorUserDetailsImpl.getUserDetails(conn, subscriberID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public List<OperatorUserDetails> getAllUserDetails(int limit) throws RBTException {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return null;
			return OperatorUserDetailsImpl.getAllUserDetails(conn, limit);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Object createUserDetails(String msisdn, String serviceKey, String status, String operatorName,
			String circleID) throws RBTException {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return null;
			return OperatorUserDetailsImpl.insert(conn, msisdn, serviceKey, status, operatorName, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Object updateUserDetails(String msisdn, String serviceKey, String status, String operatorName,
			String circleID) throws RBTException {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return null;
			return OperatorUserDetailsImpl.update(conn, msisdn, serviceKey, status, operatorName, circleID);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public Object removeUserDetails(String msisdn) throws RBTException {
		Connection conn = getConnection();
		try {
			if (conn == null)
				return null;
			if (OperatorUserDetailsImpl.remove(conn, msisdn)) {
				return "success";
			} else {
				return "failure";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "failure";
	}
	
	public HashMap<String, String> getExtraInfoMap(DoubleConfirmationRequestBean doubleConfirmationRequestBean) {
		if (doubleConfirmationRequestBean == null) {
			logger.warn("subscriber is null");
			return null;
		}
		HashMap<String, String> attributeMapFromXML = DBUtility
				.getAttributeMapFromXML(doubleConfirmationRequestBean.getExtraInfo());
		logger.info("doubleConfirmationRequestBean: " + doubleConfirmationRequestBean.getSubscriberID() + ", ExtraInfo: "
				+ attributeMapFromXML);
		return attributeMapFromXML;
	 
	}

	public String deactivateSubscriberRecordsByNotCategoryIdNotStatus(String subscriberID, int categoryId, int status,String deSelectedBy) {
		// TODO Auto-generated method stub

		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			
			success = SubscriberStatusImpl.deactivateSubscriberRecordsByNotCategoryIdNotStatus(
					conn, subscriberID, categoryId,status, deSelectedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	
	}
	/*
	public String deactivateSubscriberCutRbtRecords(String subscriberID,String deSelectedBy) {
		// TODO Auto-generated method stub

		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			
			success = SubscriberStatusImpl.deactivateSubscriberCutRbtRecords(
					conn, subscriberID, deSelectedBy);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	
	}
	
*/
	//vikrant-om
	
	public String suspendSubscriberRecordsByNotCategoryIdNotStatus(String subscriberID, int categoryId, int status) {
		// TODO Auto-generated method stub

		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			
			success = SubscriberStatusImpl.suspendSubscriberRecordsByNotCategoryIdNotStatus(
					conn, subscriberID, categoryId,status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	
	}
	
	
	public String activateSubscriberSuspendedRecordsByNotCategoryIdNotStatus(String subscriberID, int categoryId, int status) {
		// TODO Auto-generated method stub

		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;

		boolean success = false;
		try {
			
			success = SubscriberStatusImpl.activateSubscriberSuspendedRecordsByNotCategoryIdNotStatus(
					conn, subscriberID, categoryId,status);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? m_success : m_failure;
	
	}
	
	
	public SubscriberStatus[] getAllSubscriberSelectionRecordsBasedOnSelStatus(String subscriberID , String selStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberStatusImpl.getAllSubscriberSelectionRecordsBasedOnSelStatus(conn,selStatus,
					subID(subscriberID) );
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public List<ProvisioningRequests> getActiveProvisioningByType(String type[], int retryCount, int smSubStatus) {
		if(type == null && type.length < 1){
			logger.info("provisioning requests type cant be empty");
			return null;
		}
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao.getActiveProvisioningByType(type,
				retryCount, smSubStatus);
		logger.info("Fetched provisioning requests: " + provisioningRequestsList);
		return provisioningRequestsList;
	}

	public boolean updateSmStatusRetryCountAndTime(String subscriberID, String refID, String retryCount, Date retryTime, int smStatus){
		return ProvisioningRequestsDao.updateSmStatusRetryCountAndTime( subscriberID, refID, retryCount, retryTime, smStatus);
		
	}

	//Added for RBT-18249
		public boolean checkRtoinBase(String subscriberID, String sdpomtxnId)
				throws OnMobileException {
			Connection conn = getConnection();
			boolean isExists = false;
			if (conn == null)
				throw new OnMobileException("Conn Null");
			try {
				isExists = ConsentTableImpl.checkRtoinBase(conn, subscriberID,
						sdpomtxnId);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}

			return isExists;
		}
		public boolean checkRtoinSelection(String wavFile,
				String categoryID, String subscriberID, String sdpomtxnId)
						throws OnMobileException {
			Connection conn = getConnection();
			boolean isExists = false;
			if (conn == null)
				throw new OnMobileException("Conn Null");
			try {
				isExists = ConsentTableImpl.checkRtoinSelection(conn,
						wavFile, categoryID, subscriberID, sdpomtxnId);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			
			return isExists;
		}
		public boolean checkRtoforCombo(String wavFile,
				String categoryID, String subscriberID, String sdpomtxnId)
						throws OnMobileException {
			Connection conn = getConnection();
			boolean isExists = false;
			if (conn == null)
				throw new OnMobileException("Conn Null");
			try {
				isExists = ConsentTableImpl.checkRtoforCombo(conn,
						wavFile, categoryID, subscriberID, sdpomtxnId);
			} catch (Throwable e) {
				logger.error("Exception before release connection", e);
			} finally {
				releaseConnection(conn);
			}
			
			return isExists;
		}
		//Ended for RBT-18249

	public boolean deleteSubscriberRecords(String subscriberId) throws OnMobileException {
		Connection conn = getConnection();
		if (conn == null) {
			throw new OnMobileException("Conn Null");
		}

		try {
			// Delete Downloads
			SubscriberDownloadsImpl.deleteSubscriberDownloads(conn, subscriberId);
			logger.info("Deleted Subscriber Downloads for subscriberId: " + subscriberId);

			// Delete Selection
			SubscriberSelectionImpl.deleteSubscriberSelections(conn, subscriberId);
			logger.info("Deleted Subscriber Selections for subscriberId: " + subscriberId);

			// Delete Provisioning Requests
			ProvisioningRequestsDao.removeBySubscriberID(subscriberId);
			logger.info("Deleted Subscriber Provisioning Requests for subscriberId: " + subscriberId);

			// Delete Subscriber
			SubscriberImpl.deleteSubscriber(conn, subscriberId);
			logger.info("Deleted Subscriber with subscriberId: " + subscriberId);
		}
		catch (SQLException e) {
			logger.error("Exception Occured: " + e, e);
		}
		finally {
			releaseConnection(conn);
		}

		return true;
	}

public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatusAndCategory(
			String subscriberID, int categoryID, int categoryType,
			String downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl
					.getSubscriberDownloadsByDownloadStatusAndCategory(conn,
							subID(subscriberID), categoryID, categoryType,
							downloadStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	public boolean smDownloadDeActivation(
			String subscriberId, String refId ,char downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		return SubscriberDownloadsImpl.smDownloadDeActivation(conn,
				subscriberId, refId , downloadStatus);
	}
	
	public SubscriberDownloads[] getSubscriberActiveDownloadsByDownloadStatusAndCategory(String subscriberID, int categoryID,
			int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberActiveDownloadsByDownloadStatusAndCategory(conn, subscriberID,
					categoryID, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	


	public List<Clip> clipstoAddAfterLimitCheck(String subscriberId, int categoryID, String subscriberWavFile) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public SubscriberDownloads insertSubscriberDownloadRow(String subscriberID, String promoID, int categoryID, Date endTime,
			boolean isSubActive, int categoryType, String classType,
			String selBy, String selectionInfo, String extraInfo,
			boolean isSmClientModel, String downloadStatus, String refID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.insertRW(conn, subID(subscriberID), promoID,
					categoryID, endTime, isSubActive, categoryType, classType, selBy, selectionInfo,
					extraInfo, isSmClientModel, downloadStatus, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	
	public List<ProvisioningRequests> getPacksToBeActivatedBySubscriberIDAndActpendingType	(
			String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao
				.getPacksToBeActivatedBySubscriberIDAndActpendingType(subscriberID, type);
		logger.info("Fetched provisioning requests: "
				+ provisioningRequestsList);
		return provisioningRequestsList;
	}	
	
	
	public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, int categoryID,int categoryType, String downloadStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberDownloads subscriberDownload= null;
		try {
			subscriberDownload = SubscriberDownloadsImpl.getSubscriberDownloadsByDownloadStatus(conn,
					subID(subscriberID),categoryID,categoryType,downloadStatus);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberDownload;
	}
	
	public List<ProvisioningRequests> getBySubscriberIdTypeAndStatus(String subscriberID, int type, int status, int fetchSize) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao.getBySubscriberIdTypeAndStatus(
				subscriberID, type, status, fetchSize);

		logger.info("Fetched provisioning requests: " + provisioningRequestsList);
		return provisioningRequestsList;
	}
	
	
	public List<ProvisioningRequests> getActiveODAPackBySubscriberIDAndType(String subscriberID, int type) {
		List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao.getActiveODAPackBySubscriberIDAndType(
				subscriberID, type);

		logger.info("Fetched provisioning requests: " + provisioningRequestsList);
		return provisioningRequestsList;
	}	
	
	public String deleteDownloadwithTstatus(String subscriberID, String wavFile, String categoryId) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.deleteDownloadwithTstatusAndCategoryId(conn, subscriberID, wavFile, categoryId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}
	
	public SubscriberDownloads getSubscriberActiveDownloadsByDownloadStatusAndCategoryAndPromoId(String subscriberID,
			int categoryID, int categoryType, String downloadStatus, String promoId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberActiveDownloadsByDownloadStatusAndCategoryAndPromoId(conn, subscriberID,
					categoryID, categoryType, downloadStatus, promoId);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
}

class RefreshRetailer extends Thread {
	private String m_dbUrl = null;
	private static Logger logger = Logger.getLogger(RefreshRetailer.class);

	protected RefreshRetailer(String dbUrl) {
		m_dbUrl = dbUrl;
	}

	public void run() {
		Retailer[] allRet = RBTDBManager.getInstance().getRetailers();
		HashMap retMap = new HashMap();
		for (int i = 0; allRet != null && i < allRet.length; i++) {
			retMap.put(allRet[i].subID(), allRet[i].type());
		}

		synchronized (RBTDBManager.m_obj) {
			RBTDBManager.m_retailerHash = retMap;
			RBTDBManager.retailerInRefresh = false;
			logger.info("RBT::Retailer Map : " + RBTDBManager.m_retailerHash);
		}
		logger.info("RBT::Retailer initialization/reinitialization finished******");
	}

}

class RefreshTrailSubs extends Thread {
	private String m_dbUrl = null;

	private static Logger logger = Logger.getLogger(RefreshTrailSubs.class);

	protected RefreshTrailSubs(String dbUrl) {
		m_dbUrl = dbUrl;
	}

	public void run() {
		SubscriberPromo[] allSubPromo = RBTDBManager.getInstance()
				.getAllActiveSubscriberPromo();
		HashMap retMap = new HashMap();
		for (int i = 0; allSubPromo != null && i < allSubPromo.length; i++)
			retMap.put(allSubPromo[i].subID() + allSubPromo[i].subType(),
					new Integer(1));

		synchronized (RBTDBManager.m_obj) {
			RBTDBManager.m_trailSubsHash = retMap;
			RBTDBManager.trailSubsInRefresh = false;
			logger.info("RBT::Trail Subscriber Map : "
					+ RBTDBManager.m_trailSubsHash);
		}
		logger.info("RBT::Trail subscriber initialization/reinitialization finished******");
	}
}
