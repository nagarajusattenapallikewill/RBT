package com.onmobile.apps.ringbacktones.Gatherer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.Gatherer.threadMonitor.ThreadInfo;
import com.onmobile.apps.ringbacktones.Gatherer.threadMonitor.ThreadMonitor;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.TransFileWriter;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.daemons.multioperator.RBTMultiOpCopyHibernateDao;
import com.onmobile.apps.ringbacktones.daemons.multioperator.RBTMultiOpCopyParams;
import com.onmobile.apps.ringbacktones.daemons.multioperator.RBTMultiOpCopyRequest;
import com.onmobile.apps.ringbacktones.eventlogging.EventLogger;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PickOfTheDay;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.PPLContentRejectionLogger;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SelectionRequestBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SubscriptionBean;
import com.onmobile.reporting.framework.capture.api.Configuration;
import com.onmobile.reporting.framework.capture.api.ReportingException;

public class RBTCopyProcessor extends Thread implements iRBTConstant,
		ThreadInfo {
	private static Logger logger = Logger.getLogger(RBTCopyProcessor.class);
	private static String _class = "RBTCopyProcessor";

	public RBTDBManager rbtDBManager = null;
	private RBTConnector rbtConnector = null;
	public RBTGatherer m_parentGathererThread = null;
	public Feed schedule = null;
	//RBT-14671 - # like
	private RBTCopyLikeUtils m_rbtCopyLikeUtils = null;
	
	private HashMap<String, String> m_operatorPrefixMap = new HashMap<String, String>();
	private HashMap<String, String> m_RdcNovaOperatorMap = new HashMap<String, String>();

	private HashMap<String, String> m_operatorPrefixArrayListMap = new HashMap<String, String>();
	
	private Map<String, List<String>> m_vrbtCatIdSubSongSrvKeyMap = null;


	String m_localType = "INCIRCLE";
	String m_virtualType = "VIRTUAL_NUMBER";
	String m_nationalType = "OPERATOR";
	String m_nonOnmobileType = "NON_ONMOBILE";
	String m_crossOperatorType = "CROSS_OPERATOR";
	List<String> starCopyKeys = new ArrayList<String>();

	String m_copyClassType = null;
	int copyAmount = 10;
	String defaultClipWavName = null;
	HashMap<String, String> copyChargeClassMap = new HashMap<String, String>();

	HashMap<String, List<RBTCopyThread>> m_copyThreadPoolMap = new HashMap<String, List<RBTCopyThread>>();
	public static HashMap<String, Vector<ViralSMSTable>> m_ViralSMSRecordsListMap = new HashMap<String, Vector<ViralSMSTable>>();
	public static ArrayList<String> m_circleList = new ArrayList<String>();
	// public static HashMap m_circleQueueSizeMap = new HashMap();

	private String m_transDir = "./Trans";
	private String m_copyStats = "./Trans";
	private String m_eventLoggingDir = "./EventLogs";
	public EventLogger eventLogger = null;

	String m_copySelSMS = "The selection %S copied from %C has been set as your RingBackTone";
	String m_copyActSMS = "You will be activated on RingBackTones in the next 24 hrs. The selection %S copied from %C has been set as your RingBackTone";
	String m_pressStarConfirmationSMS = "You have pressed star to copy the selection %S. If you don't want to copy send RBT CANCEL within %C min";
	String m_optInMigratedUserSMS = "Your are migrated user";
	String m_optInConfirmationActSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Subscription charge is %ACT_AMT Rs. And Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationDefaultSongActSMS = "You have pressed star to copy the default song selection. If you want to copy send %RBT_CONFIRM within %C min. The Subscription charge is %ACT_AMT Rs. And Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationSelSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationDWNSelSMS = "Your Download is already present.You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	String likeConfirmationSMS = "You have liked the selection %S. If you want to set the song send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	String likeActiveUserConfirmationSMS = "You have subscriber to RBT and liked the selection %S. If you want to set the song send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	String smsTextBlockCopyForShuffleSubscriber = "You have a shuffle as your present selection. So copy is not allowed";
	String m_crossCopyContentMissingSmsText = "The song copied from subscriber %CALLED% belonging to a different operator is not available with this operator";
	String m_nonCopyContentSMS = "Sorry this selection cannot be copied";
	String m_nonCopyExpiredClipSMS = "Sorry this selection cannot be copied";
	String m_corpCopyContentSMS = "Song Selection cannot be changed on your number";
	String m_nonCopyNonCircleSMS = "RingBacktones can be copied only from your own circle";
	String m_crossCopyNotSupportedSMS = "You are not allowed to use this service";
	String m_optInSameSongSms = "Hi! You have requested to set %S as your Caller tune bt this song is already active as your Caller tune. Hence your request is not being processed. Thank You!";
	String m_defaultCopyContentSetSMS = "Sorry default selection is set";
	String m_expiredCopyClipSMS = "Sorry the requested content is expired";
	String m_copyContentSetClipSMS = "Sorry the requested content is already set";
	String m_copyContentMaxDwnLimitSMS = "Sorry your maximum download limit has been reached";
	String m_subStatusBlockedForCopySMS = "Sorry this song, %CONTENT_NAME, cannot be copied";
	String m_noBaseOfferCopySMS = "Sorry No base offer found.";
	
	String COPY = "COPY";
	String SOURCE_OPERATOR = "SOURCE_OPERATOR";
	String COPIED = "COPIED";
	String COPYCONFIRM = "COPYCONFIRM";
	String COPYFAILED = "COPYFAILED";
	String COPYCONFIRMED = "COPYCONFIRMED";
	String RRBTCOPYFAILED = "RRBTCOPYFAILED";
	String RRBTCOPYREQUESTED = "RRBTCOPYREQUESTED";
	String COPYSTAR = "COPYSTAR";
	String COPYCONFPENDING = "COPYCONFPENDING";
	String COPYEXPIRED = "COPYEXPIRED";
	String DUPLICATE = "DUPLICATE";
	String PREMIUM_CONTENT = "PREMIUM_CONTENT";
	String OFFER_NOT_FOUND = "OFFER_NOT_FOUND";
	String COPY_CONFIRM_MODE_KEY = "COPY_MODE";
	String LOCAL_OPERATOR = "LOCAL_OPERATOR";
	String PICK_OF_THE_DAY_NOT_FOUND = "PICK_OF_THE_DAY_NOT_FOUND";
	
	String m_nonCopyExpiredCategorySMS = "Sorry this selection cannot be copied";

	URLCodec m_urlEncoder = new URLCodec();

	static TransFileWriter copyTransactionWriter = null;
	static PPLContentRejectionLogger pplContentRejectionLogger = null;

	boolean m_redirectNational = false;
	String m_nationalUrl = null;
	boolean m_nationalUseProxy = false;
	String m_nationalProxyHost = null;
	int m_nationalProxyPort = -1;

	boolean m_redirectBuddyNet = false;
	String m_buddynetUrl = null;
	boolean m_buddynetUseProxy = false;
	String m_buddynetProxyHost = null;
	int m_buddynetProxyPort = -1;

	boolean m_redirectNonOnmobile = false;
	String m_nonOnmobileUrl = null;
	boolean m_nonOnmobileUseProxy = false;
	String m_nonOnmobileProxyHost = null;
	int m_nonOnmobileProxyPort = -1;

	boolean m_redirectCrossOperator = false;
	String m_crossOperatorUrl = null;
	boolean m_crossOperatorUseProxy = false;
	String m_crossOperatorProxyHost = null;
	int m_crossOperatorProxyPort = -1;
	int runCount = 0;
	String virtualnos = null;
	static Thread rbtExpiredCopyThread = null;
	int loopCount = 0;
	boolean isSleeping = false;
	long sleepInterval = 0;
	Date copyThreadStartTime = Calendar.getInstance().getTime();
	String threadName = null;
	SimpleDateFormat sdfLog = new SimpleDateFormat("yyyyMMdd");
	TransFileWriter copyStatsWriter = null;
	SimpleDateFormat statsDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	boolean isDbImplForPrefix = false;
	List<Integer> categoryTypeList = null;
	HashSet<String> virtualNumbers = null;
	public static String DIRECTCOPY = "D";
	public static String OPTINCOPY = "N";
	public static String DEFAULTCOPY = "-";

	HashSet<String> copyVirtualNumbers = null;

	// Added by Sreekar
	private RBTHttpClient rbtHttpClient = null;
	private RBTHttpClient nationalRbtHttpClient = null;
	private RBTHttpClient nonOnmobileRbtHttpClient = null;
	private RBTHttpClient crossOperatorRbtHttpClient = null;
	String activeclassTypeParam = null;
	String inactiveclassTypeParam = null;
	private String azaanWavFileName = null;
	private String azaanContentName = null;
	private String subscriptionClassOperatorNameMap = null;
	// private String virtualNumberCopyMode = null;
	private String azaanCategoryId = null;
	private HashMap<String, String> baseModeMap = new HashMap<String, String>();
	private String pickOfTheDayKeys = null;
	private List<String> pickOfTheDayKeysList = null;
	private List<String> migratedUserSubClassesList = new ArrayList<String>();
	
	
	private Map<String,String> confAzaanCopticDoaaCosIdSubTypeMap = null;
	private Map<String,String> confAzaanCopticDoaaSubTpeContentNameMap = null;
	protected static List<String> m_FreemiumUpgradeChargeClass = null;
	protected static List<String> freemiumSubClassList = null;
	public static List<String> m_blockedCategoryInfoList = null;
	public static List<String> subStatusesBlockedForCopy = null;
	public  HashMap<String, HashSet<String>> directCopyKeysMap = null;
	public  HashMap<String, HashSet<String>> optinCopyKeysMap = null;
	public  ArrayList<String> toLikeKeys=null;
	public  ArrayList<String> crossCopy=null;
	public  ArrayList<String> starCopyKey = null;
	public  ArrayList<String> normalCopyKeys = null;
	public enum ResponseEnum {
	    SUCCESS,
	    FAILURE,
	    RETRY
	}
	
	
	protected RBTCopyProcessor(RBTGatherer m_gathererThread) throws Exception {
		logger.info("Constructing " + _class);
		m_parentGathererThread = m_gathererThread;
		if (init())
			logger.info("RBTCopyProcessor init() done");
		// start();
		else
			throw new Exception(" In RBTCopyProcessor: Cannot init Parameters");
	}

	public boolean init() {
		logger.info("Entering");
		//RBT-14671 - # like
		m_rbtCopyLikeUtils = new RBTCopyLikeUtils();
		rbtConnector = RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();
		virtualnos = getParamAsString("RRBT", "VIRTUAL_NUMBERS", null);
		virtualNumbers = new HashSet<String>();
		String[] Virtual_no = null;
		if (virtualnos != null) {
			Virtual_no = virtualnos.split(",");
		}
		if (Virtual_no != null && Virtual_no.length > 0) {
			for (int z = 0; z < Virtual_no.length; z++)
				virtualNumbers.add(Virtual_no[z]);
		}
		initCopyKeys();
		activeclassTypeParam = getParamAsString("GATHERER",
				"DEFAULT_COPY_CHARGECLASS_FOR_ACTIVE_SUBS", null);
		inactiveclassTypeParam = getParamAsString("GATHERER",
				"DEFAULT_COPY_CHARGECLASS_FOR_INACTIVE_SUBS", null);
		subscriptionClassOperatorNameMap = getParamAsString(COMMON, 
				"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);

		copyVirtualNumbers = new HashSet<String>();
		normalCopyKeys = tokenizeArrayList(
				getParamAsString("COMMON", "NORMALCOPY_KEY", null), ",");
		starCopyKey = tokenizeArrayList(
				getParamAsString("COMMON", "STARCOPY_KEY",null), ",");
		crossCopy = tokenizeArrayList(getParamAsString("COMMON", CROSSCOPY_KEY,null), ",");
		toLikeKeys = tokenizeArrayList(getParamAsString("COMMON",TOLIKE_KEY , null), ",");
		List<Parameters> virtualNoParameters = CacheManagerUtil
				.getParametersCacheManager().getParameters("VIRTUAL_NUMBERS");
		if (virtualNoParameters != null) {
			for (Parameters virtualNoParameter : virtualNoParameters) {
				copyVirtualNumbers.add(virtualNoParameter.getParam());
			}
			logger.info("The set of copy virtual numbers are : "
					+ copyVirtualNumbers);
		}

		if (getParamAsBoolean("IS_RRBT_COPY_ON", "FALSE")) {
			DIRECTCOPY = "RRBT_D";
			OPTINCOPY = "RRBT_N";
			DEFAULTCOPY = "RRBT-";
		}

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
		initCopyChargeClassMap();
		initNovaRdcOperatorMap();
		getDefaultClip();
		initCopyAmountAndChargeClass();
		initViralSmsRecordsListMap();
		initCrossOperatorUrlConfig();
		initNonOnmobileUrlConfig();
		initNationalUrlConfig();
		initBuddyNetUrlConfig();
		if (getParamAsBoolean("COPY_CRICKET_SEL", "FALSE"))
			initializeFeed();
		String dbImplForPrefixStr = getParamAsString("SERVICE", "MNP_SERVICE",
				null);
		if (dbImplForPrefixStr == null
				|| dbImplForPrefixStr.indexOf("DbImpl") != -1)
			isDbImplForPrefix = true;
		if (getParamAsString("GATHERER_PATH") != null
				&& getParamAsBoolean("WRITE_TRANS", "FALSE")) {
			m_transDir = getParamAsString("GATHERER_PATH") + "/Trans";
			new File(m_transDir).mkdirs();
		}
		initTransactionFile();
		if (getParamAsString("GATHERER_PATH") != null
				&& getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
			m_eventLoggingDir = getParamAsString("GATHERER_PATH")
					+ "/EventLogs";
			new File(m_eventLoggingDir).mkdirs();
		}
		if (getParamAsString("GATHERER_PATH") != null) {
			m_copyStats = getParamAsString("GATHERER_PATH") + "/CopyStats";
			new File(m_copyStats).mkdirs();
		}
		if (getParamAsBoolean("WRITE_COPY_STATS", "FALSE"))
			initCopyStats();

		categoryTypeList = new ArrayList<Integer>();
		categoryTypeList.add(SHUFFLE);
		categoryTypeList.add(WEEKLY_SHUFFLE);
		categoryTypeList.add(MONTHLY_SHUFFLE);
		categoryTypeList.add(OVERRIDE_MONTHLY_SHUFFLE);
		categoryTypeList.add(DAILY_SHUFFLE);
		categoryTypeList.add(DYNAMIC_SHUFFLE);
		categoryTypeList.add(ODA_SHUFFLE);
		categoryTypeList.add(TIME_OF_DAY_SHUFFLE);
		categoryTypeList.add(BOX_OFFICE_SHUFFLE);
		categoryTypeList.add(FESTIVAL_SHUFFLE);
		categoryTypeList.add(FEED_SHUFFLE);
		categoryTypeList.add(MONTHLY_ODA_SHUFFLE);
		categoryTypeList.add(PLAYLIST_ODA_SHUFFLE);

		HttpPerformanceMonitor httpPerformanceMonitor = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter("GATHERER", "pir.httpHits.enable", "false");
		if (parameter.getValue().equalsIgnoreCase("true")) {
			String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
			httpPerformanceMonitor = PerformanceMonitorFactory
					.newHttpPerformanceMonitor(componentName,
							"Http Performance Monitor",
							PerformanceDataType.LONG, "Milliseconds");
		}

		initRBTHttpClient(httpPerformanceMonitor);
		initNationalRBTHttpClient(httpPerformanceMonitor);
		initNonOnmobileRBTHttpClient(httpPerformanceMonitor);
		initCrossOperatorRBTHttpClient(httpPerformanceMonitor);
		azaanWavFileName = getParamAsString(AZAAN_WAV_FILE_NAME);
		azaanContentName = getParamAsString(AZAAN_CONTENT_NAME);
		// virtualNumberCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE);
		azaanCategoryId = getParamAsString(AZAAN_CATEGORY_ID);
		

		String baseModeMapStr = getParamAsString(BASE_MODE_MAP_FOR_COPY);
		if (baseModeMapStr != null) {
			baseModeMapStr = baseModeMapStr.trim();
			StringTokenizer stk = new StringTokenizer(baseModeMapStr, ";");
			while (stk.hasMoreTokens()) {
				String modePair = stk.nextToken().trim();
				StringTokenizer stkChild = new StringTokenizer(modePair, ":");
				if (stkChild.countTokens() != 2)
					continue;
				String initMode = stkChild.nextToken().trim();
				String finalMode = stkChild.nextToken().trim();
				baseModeMap.put(initMode, finalMode);
			}
		}
		
		pickOfTheDayKeys = getParamAsString("PICK_OF_THE_DAY_KEYS");
		if (null != pickOfTheDayKeys) {
			pickOfTheDayKeysList = ListUtils.convertToList(pickOfTheDayKeys,
					",");
		}
		
		String migratedUserSubClasses = getParamAsString("WEBSERVICE",
				"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);

		if (null != migratedUserSubClasses) {
			migratedUserSubClassesList = ListUtils.convertToList(
					migratedUserSubClasses, ",");
		}

		logger.info("Configured MIGRATED_USER_SUBSCRIPTION_CLASSES as migratedUserSubClassesList: "
				+ migratedUserSubClassesList);

		logger.info("baseModeMap=" + baseModeMap);
		String cosIdSubTypeMapStr = getParamAsString(COMMON, COSID_SUBTYPE_MAPPING_FOR_AZAAN, "");
		confAzaanCopticDoaaCosIdSubTypeMap = MapUtils.convertToMap(cosIdSubTypeMapStr, ";", ":", ","); 
		
		String subTypeContentNameMapStr = getParamAsString(COMMON, SUBTYPE_CONTENT_NAME_MAPPING_FOR_AZAAN, "");
		confAzaanCopticDoaaSubTpeContentNameMap = MapUtils.convertToMap(subTypeContentNameMapStr, ";", ":", ",");
	
		String chrgClassNumMaxMappingStr = RBTParametersUtils.getParamAsString("COMMON", "FREEMIUM_CHARGE_CLASSES_NUM_MAX_MAPPING", null);
		m_FreemiumUpgradeChargeClass = ListUtils.convertToList(chrgClassNumMaxMappingStr, ","); //MapUtils.convertIntoMap(chrgClassNumMaxMappingStr, ";", ":", ","); 
        logger.info("FreemiumUpgradeChargeClass ="+m_FreemiumUpgradeChargeClass);
        
		freemiumSubClassList = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
				"FREEMIUM_SUB_CLASSES", "").split(","));
		logger.info("freemiumSubClassList = "+freemiumSubClassList);
		
		String blockedCatTypeStr = RBTParametersUtils.getParamAsString(
				"GATHERER", "BLOCKED_CATEGORY_INFO", null);
		m_blockedCategoryInfoList = ListUtils.convertToList(blockedCatTypeStr,
				",");
		logger.info("m_blockedCategoryInfoList = " + m_blockedCategoryInfoList);
		
		m_vrbtCatIdSubSongSrvKeyMap = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getVrbtCatSubSongSrvMap();
		
		String subStatusesBlockedForCopyString = RBTParametersUtils.getParamAsString(
				"GATHERER", "SUB_STATUSES_BLOCKED_FOR_COPY", null);
		logger.info("subStatusesBlockedForCopyString = " + subStatusesBlockedForCopyString);
		subStatusesBlockedForCopy = ListUtils.convertToList(subStatusesBlockedForCopyString,
				",");
		logger.info("subStatusesBlockedForCopy = " + subStatusesBlockedForCopy);
		
				return true;
	}

	private void initRBTHttpClient(HttpPerformanceMonitor httpPerformanceMonitor) {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);

		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	private void initNationalRBTHttpClient(
			HttpPerformanceMonitor httpPerformanceMonitor) {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setUseProxy(m_nationalUseProxy);
		httpParameters.setProxyHost(m_nationalProxyHost);
		httpParameters.setProxyPort(m_nationalProxyPort);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);

		nationalRbtHttpClient = new RBTHttpClient(httpParameters);
	}

	private void initNonOnmobileRBTHttpClient(
			HttpPerformanceMonitor httpPerformanceMonitor) {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setUseProxy(m_nonOnmobileUseProxy);
		httpParameters.setProxyHost(m_nonOnmobileProxyHost);
		httpParameters.setProxyPort(m_nonOnmobileProxyPort);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);

		nonOnmobileRbtHttpClient = new RBTHttpClient(httpParameters);
	}

	private void initCrossOperatorRBTHttpClient(
			HttpPerformanceMonitor httpPerformanceMonitor) {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setUseProxy(m_crossOperatorUseProxy);
		httpParameters.setProxyHost(m_crossOperatorProxyHost);
		httpParameters.setProxyPort(m_crossOperatorProxyPort);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);

		crossOperatorRbtHttpClient = new RBTHttpClient(httpParameters);
	}

	private void initializeEventLogger() {
		try {
			Configuration cfg = new Configuration(m_eventLoggingDir);
			eventLogger = new EventLogger(cfg);
			logger.info("*** RBT::writing COPY EVENT LOGS (append) in directory : "
					+ m_eventLoggingDir);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void initCopyChargeClassMap() {
		logger.info("Entering");
		String strCopyChargeClassMap = getParamAsString("COPY_CHARGE_CLASS_MAP");
		if (strCopyChargeClassMap != null && strCopyChargeClassMap.length() > 0) {
			strCopyChargeClassMap = strCopyChargeClassMap.trim().toUpperCase();
			StringTokenizer stkMap = new StringTokenizer(strCopyChargeClassMap,
					";");
			while (stkMap.hasMoreTokens()) {
				String singleMapping = stkMap.nextToken().trim();
				StringTokenizer singleClassMap = new StringTokenizer(
						singleMapping, ",");
				String classType = null;
				if (singleClassMap.hasMoreTokens())
					classType = singleClassMap.nextToken().trim();
				if (classType == null
						&& rbtConnector.getRbtGenericCache().getChargeClass(
								classType) == null) {
					continue;
				}
				while (singleClassMap.hasMoreTokens())
					copyChargeClassMap.put(singleClassMap.nextToken().trim(),
							classType);
			}
		}
		logger.info("Exiting. copyChargeClassMape is " + copyChargeClassMap);
	}

	public void initNovaRdcOperatorMap() {
		logger.info("Entering initNovaRdcOperatorMap");
		String operatorMap = getParamAsString("RDC_NOVA_OPERATOR_MAP");
		if (operatorMap != null && operatorMap.length() > 0) {
			operatorMap = operatorMap.trim().toUpperCase();
			StringTokenizer stkMap = new StringTokenizer(operatorMap, ";");
			while (stkMap.hasMoreTokens()) {
				String rdcNovaOperator = stkMap.nextToken().trim();
				StringTokenizer singleClassMap = new StringTokenizer(
						rdcNovaOperator, ",");
				String rdcOperator = null;
				if (singleClassMap.hasMoreTokens())
					rdcOperator = singleClassMap.nextToken().trim();

				while (singleClassMap.hasMoreTokens())
					m_RdcNovaOperatorMap.put(rdcOperator, singleClassMap
							.nextToken().trim());
			}
		}
		logger.info("Exiting. operatorMap is " + m_RdcNovaOperatorMap);
	}

	public void run() {
		logger.info("Entering");
		startFailedCopyThread();
		if (getParamAsBoolean("IS_OPT_IN", "FALSE")
				|| getParamAsBoolean("IS_STAR_OPT_IN_ALLOWED", "FALSE")) {
			logger.info("IS_OPT_IN or IS_STAR_OPT_IN_ALLOWED is true. So, going to start ExpiredCopyThread.");
			// this will remove entries with type copyConfPending and
			// smsSentTime < sysdate - waitTime
			startExpiredCopyThread();
		}

		if (getParamAsBoolean("RUN_COPY_DAEMON_OLD", "FALSE")) {
			makeThreads();
			if (getParamAsBoolean("IS_COPY_THREAD_MONITORING_ON", "FALSE"))
				ThreadMonitor.getMonitor().start();
			while (m_parentGathererThread.isAlive()) {
				try {
					loopCount++;
					isSleeping = false;
					logger.info("Entering while loop of copy");
					checkThreads();
					if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE"))
						initializeEventLogger();
					populateArrayListBasedOnCircle();
				} catch (Throwable e) {
					logger.error("", e);
				}
				try {
					Date next_run_time = m_parentGathererThread
							.roundToNearestInterVal(getParamAsInt(
									"GATHERER_SLEEP_INTERVAL", 5));
					long sleeptime = m_parentGathererThread
							.getSleepTime(next_run_time);
					if (sleeptime < 100)
						sleeptime = 500;
					logger.info(_class + " Thread : sleeping for " + sleeptime
							+ " mSecs.");
					isSleeping = true;
					sleepInterval = sleeptime;
					Thread.sleep(sleeptime);
					logger.info(_class + " Thread : waking up.");
					// Thread.sleep(sleeptime);
				} catch (Throwable E) {
					logger.error("", E);
				}
			}
		}
		logger.info("Exiting");
	}

	private boolean isNonCopyContent(String clipID, String catID, Clip clip,
			int status, String wavFile, boolean isVirtualNo,
			Subscriber callerSub, Subscriber calledSub, String keyPressed,
			int catType, boolean isThirdPartyAllowedKeys, String catInf, String selectedBy) {
		logger.info("Entered with params: clipID = " + clipID + ", catID = "
				+ catID + ", status = " + status + ", wavFile = " + wavFile
				+ ", clip = " + clip + ",catInf= " + catInf
				+ ",isThirdPartyAllowedKeys= " + isThirdPartyAllowedKeys
				+ ",catType= " + catType);
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String currentTime = sdf.format(currentDate);
		// If copy shuffle is enabled it won't do any validation.
		// Logic: If copy shuffle is false then check the allowed keys and based
		// on that set the boolean value as true or false;
		// If copy shuffle is true check the cat type .if it is ODA shuffle check the allowed key
		// and set it as true so that it won't allow u to copy. if it is not a ODA
		// shuffle then allow to copy so that the non copy content return as false.
		boolean isNotCopyShuffle = !getParamAsBoolean("COPY_SHUFFLE", "FALSE");
		String catTypes = getParamAsString("GATHERER",
				"COPY_BLOCKED_SHUFFLE_CATEGORY_TYPES", null);
		//RBT-14671 - # like
		isNotCopyShuffle = m_rbtCopyLikeUtils.isBlockedCatTypeOrCatInfo(isNotCopyShuffle,
				catTypes, catType, catInf);
		String subscriptionClass=callerSub.getSubscriptionClass();
		if (isNotCopyShuffle) {
			isNotCopyShuffle = (!isThirdPartyAllowedKeys);
		}
		//End.
		if (wavFile != null && wavFile.equalsIgnoreCase("MISSING")) {
			logger.info("Missing content.");
			return true;
		}//RBT-14671 - # like
		if(subscriptionClass!=null && m_rbtCopyLikeUtils.isTNBuser(subscriptionClass) ){
			List<String> modes=ListUtils.convertToList(CacheManagerUtil
					.getParametersCacheManager().getParameterValue(	iRBTConstant.COMMON, "VODAFONE_UPGRADE_CONSENT_MODES","").toUpperCase(),",");
			if(modes.contains("COPY")){
				return true;
			}
			return false;
		}	
		if (clipID != null
				&& clipID.toUpperCase().indexOf("DEFAULT_" + currentTime) != -1) {
			if (!getParamAsBoolean("ALLOW_POLL_COPY", "FALSE")) {
				logger.info("Polling RBT copy failed");
				return true;
			} else {
				return false;
			}
		}
		//RBT-14671 - # like
		if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1)
				&& !(m_rbtCopyLikeUtils.allowDefaultCopyUserHasSelection(callerSub, calledSub))) {
			logger.info("default song copy failed");
			return true;
		}

		if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1)
				&& !(getParamAsBoolean("COPY_DEFAULT", "FALSE") || getParamAsBoolean(
						"INSERT_DEFAULT_SEL", "FALSE"))) {
			logger.info("default song copy failed");
			return true;
		}
		// Jira :RBT-15026: Changes done for allowing the multiple
		// Azaan pack.
		String contentName = null;
		String[] waveFileArray = wavFile.split("_");
		contentName = confAzaanCopticDoaaSubTpeContentNameMap.get(wavFile);
		if (contentName == null && waveFileArray.length > 0) {
			contentName = confAzaanCopticDoaaSubTpeContentNameMap
					.get(waveFileArray[0]);
		}
		
		if (azaanWavFileName != null
				&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
				&& rbtDBManager.azaanDefaultCosId != null) {
			logger.info("user copied azaan in cos model, activating on azaan cos="
					+ rbtDBManager.azaanDefaultCosId);
			return false;
		}else if(contentName!=null
				&& confAzaanCopticDoaaCosIdSubTypeMap.get(contentName)!=null){
			logger.info("user copied either COPTIC in cos model ,activating on coptic cos ="
					+ confAzaanCopticDoaaCosIdSubTypeMap.get(contentName));
		    return false;
		}

		if (azaanWavFileName != null
				&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
				&& null != azaanCategoryId) {

			logger.info("Content is valid, subscriber copied azaan"
					+ " in category model. Calling SubscriberId: "
					+ callerSub.getSubscriberID() + ", Called SubscriberId: "
					+ calledSub.getSubscriberID());
			return false;
		}
		
		if ("RADIO".equalsIgnoreCase(wavFile)) {

			logger.info("Content is valid, subscriber copied RADIO"
					+ " in category model. Calling SubscriberId: "
					+ callerSub.getSubscriberID() + ", Called SubscriberId: "
					+ calledSub.getSubscriberID());
			return false;
		}

		if (clip == null && status != 90) {
			logger.info("Clip is null for wavFile " + wavFile);
			return true;
		}
		if (Arrays.asList(
				getParamAsString("GATHERER", "COPY_BLOCKED_CATEGORY_IDS",
						"1,99").split(",")).contains(catID)) {
			return (!isThirdPartyAllowedKeys);
		}
		if (clip != null
				&& Arrays.asList(
						getParamAsString("GATHERER", "COPY_BLOCKED_CLIP_IDS",
								"").split(",")).contains("" + clip.getClipId()))
			return true;
		else if (clip != null && clip.getClipEndTime() != null
				&& clip.getClipEndTime().getTime() < System.currentTimeMillis()
				&& !getParamAsBoolean("ALLOW_COPY_FOR_EXPIRED_CLIP", "FALSE"))
			return true;
		// else if (clipID != null && !getParamAsBoolean("COPY_SHUFFLE","FALSE")
		// && clipID.indexOf(":S") != -1)	//RBT-14671 - # like
		else if (clipID != null && isNotCopyShuffle
				&& m_rbtCopyLikeUtils.isShuffleCategory(catID) && !isVirtualNo)
			return true;
		else if (clipID != null && ! m_rbtCopyLikeUtils.isShuffleCategory(catID)//RBT-14671 - # like
				&& !getParamAsBoolean("IS_NORMAL_COPY_ALLOWED", "TRUE"))
			return true;
		else if (clip != null
				&& "EMOTION_UGC".equalsIgnoreCase(clip.getContentType())) {
			logger.info("User tried to copy EmotionUgc content, Not allowed");
			return true;
		} else if (status != 1 && status != 75 && status != 79 && status != 80
				&& status != 90 && status != 91 && status != 92 && status != 95
				&& status != 81)
			return true;
		return false;
	}
	
	//This function will check the categoryType and catInfo 
	public boolean isBlockedCatTypeOrCatInfo(boolean isNotCopyShuffle,
			String catTypes, int catType, String catInf) {
		if (!isNotCopyShuffle && null != catTypes
				&& !catTypes.equalsIgnoreCase("")) {
			isNotCopyShuffle = (Arrays.asList(catTypes.split(","))
					.contains(String.valueOf(catType)));
			if (isNotCopyShuffle && null != m_blockedCategoryInfoList
					&& !m_blockedCategoryInfoList.isEmpty() && null != catInf) {
				catInf = catInf.trim().toUpperCase();
				isNotCopyShuffle = m_blockedCategoryInfoList.contains(catInf);
			}
		}
		return isNotCopyShuffle;
	}
	
	private void getCategoryCharge(String categoryID, StringBuffer classType,
			StringBuffer catName) {
		int amount = 0;
		String amt = null;
		Category category = null;
		if (categoryID != null) {
			try {
				category = rbtConnector.getMemCache().getCategory(
						Integer.valueOf(categoryID));
			} catch (Exception e) {
				category = null;
			}
		}
		
		if (category == null)
			return;
		if (category.getCategoryTpe() == 5 || category.getCategoryTpe() == 7) {
			ChargeClass chargeClass = rbtConnector.getRbtGenericCache()
					.getChargeClass(category.getClassType());

			if (chargeClass != null)
				amt = chargeClass.getAmount();

			try {
				amount = Integer.parseInt(amt.trim());
			} catch (Exception e) {
				amount = 0;
			}
			if (amount > copyAmount)
				classType.append(category.getClassType());
		}
		return;
	}

	private void sendPressStarSMS(Subscriber sub1, String sms) {
		try {
			if (sms != null) {
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", sub1.getCircleID(), "STAR_OBTAIN_SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(sub1.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%NO_SENDER", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
				Tools.sendSMS(senderNumber,
						sub1.getSubscriberID(), sms, false);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void sendSMS(Subscriber subscriber, String sms) {
		try {
			if (sms != null){
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(subscriber.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%NO_SENDER", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
				Tools.sendSMS(senderNumber, subscriber.getSubscriberID(), sms,
						false);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void sendSMSviaPromoTool(Subscriber subscriber, String sms) {
		try {
			if (sms != null){
				String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "STAR_OBTAIN_SENDER_NO");
				String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(subscriber.getCircleID());
				if(subscriptionClassOperatorNameMap != null){
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%NO_SENDER", senderNumber);
					sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(sms, "%BRAND_NAME", brandName);
				}
				
				Tools.sendSMS(senderNumber,
						subscriber.getSubscriberID(), sms);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void initViralSmsRecordsListMap() {
		logger.info("Entering initViralSmsRecordsListMap()");
		m_ViralSMSRecordsListMap.put("OPERATOR", new Vector<ViralSMSTable>());
		m_ViralSMSRecordsListMap.put("RRBT", new Vector<ViralSMSTable>());
		m_ViralSMSRecordsListMap.put("NON_ONMOBILE",
				new Vector<ViralSMSTable>());
		m_ViralSMSRecordsListMap.put("CROSS_OPERATOR",
				new Vector<ViralSMSTable>());
		m_ViralSMSRecordsListMap.put("LOCAL", new Vector<ViralSMSTable>());
		List<SitePrefix> prefixes = CacheManagerUtil
				.getSitePrefixCacheManager().getAllSitePrefix();
		for (int i = 0; prefixes != null && prefixes.size() > 0
				&& i < prefixes.size(); i++) {
			String circleId = prefixes.get(i).getCircleID();
			if (prefixes.get(i).getSiteUrl() != null
					&& prefixes.get(i).getSiteUrl().trim().length() > 0)
				m_ViralSMSRecordsListMap.put(circleId,
						new Vector<ViralSMSTable>());
		}
		logger.info("m_ViralSMSRecordsListMap ::" + m_ViralSMSRecordsListMap);
		updateOperatorPrefixes(prefixes);
	}

	private void updateOperatorPrefixes(List<SitePrefix> prefixes) {
		logger.info("Entering updateOperatorPrefixes()");

		m_circleList.add("LOCAL");

		String urlInfoString = getParamAsString("NON_ONMOBILE_URL");
		if (urlInfoString != null) {
			StringTokenizer stk = new StringTokenizer(urlInfoString, ",");
			if (stk.hasMoreTokens())
				m_operatorPrefixMap.put("NON_ONMOBILE", stk.nextToken().trim());
			m_circleList.add("NON_ONMOBILE");
			String nonOnmobilePrefix = getParamAsString("NON_ONMOBILE_PREFIX");
			if (nonOnmobilePrefix != null) {
				StringTokenizer stkNon = new StringTokenizer(nonOnmobilePrefix,
						",");
				while (stkNon.hasMoreTokens()) {
					m_operatorPrefixArrayListMap.put(stkNon.nextToken(),
							"NON_ONMOBILE");
				}
			}
		}

		urlInfoString = getParamAsString("RDC_URL");
		if (urlInfoString != null) {
			StringTokenizer stk = new StringTokenizer(urlInfoString, ",");
			if (stk.hasMoreTokens())
				stk.nextToken();
			if (stk.hasMoreTokens())
				m_operatorPrefixMap.put("CROSS_OPERATOR", stk.nextToken()
						.trim());
		}
		m_circleList.add("CROSS_OPERATOR");

		urlInfoString = getParamAsString("REDIRECT_NATIONAL_COPY");
		if (urlInfoString != null) {
			StringTokenizer stk = new StringTokenizer(urlInfoString, ",");
			if (stk.hasMoreTokens())
				stk.nextToken();
			if (stk.hasMoreTokens())
				m_operatorPrefixMap.put("OPERATOR", stk.nextToken().trim());
			m_circleList.add("OPERATOR");
			String operatorPrefix = getParamAsString("OPERATOR_PREFIX");
			if (operatorPrefix != null) {
				StringTokenizer stkNon = new StringTokenizer(operatorPrefix,
						",");
				while (stkNon.hasMoreTokens()) {
					m_operatorPrefixArrayListMap.put(stkNon.nextToken(),
							"OPERATOR");
				}
			}
		}

		for (int i = 0; prefixes != null && prefixes.size() > 0
				&& i < prefixes.size(); i++) {
			if (prefixes.get(i).getSiteUrl() == null
					|| prefixes.get(i).getSiteUrl().length() <= 0) {
				if (prefixes.get(i).getSitePrefix() != null) {
					StringTokenizer stkNon = new StringTokenizer(prefixes
							.get(i).getSitePrefix(), ",");
					while (stkNon.hasMoreTokens()) {
						m_operatorPrefixArrayListMap.put(stkNon.nextToken(),
								"LOCAL");
					}
				}
			} else if (!m_circleList.contains("OPERATOR")) {
				m_operatorPrefixMap.put(prefixes.get(i).getCircleID(), prefixes
						.get(i).getSiteUrl());
				m_circleList.add(prefixes.get(i).getCircleID());
				if (prefixes.get(i).getSitePrefix() != null) {
					StringTokenizer stkNon = new StringTokenizer(prefixes
							.get(i).getSitePrefix(), ",");
					while (stkNon.hasMoreTokens()) {
						m_operatorPrefixArrayListMap.put(stkNon.nextToken(),
								prefixes.get(i).getCircleID());
					}
				}
			}
		}

		logger.info("m_circleList ::" + m_circleList);
		logger.info("m_operatorPrefixMap ::" + m_operatorPrefixMap);
	}

	private void initCopyAmountAndChargeClass() {
		logger.info("Entering");
		String amt = null;
		Category category = rbtConnector.getMemCache().getCategory(26);
		if (category == null)
			return;

		ChargeClass chargeClass = rbtConnector.getRbtGenericCache()
				.getChargeClass(category.getClassType());
		if (chargeClass != null) {
			amt = chargeClass.getAmount();
			m_copyClassType = category.getClassType();
			try {
				copyAmount = Integer.parseInt(amt.trim());
			} catch (Exception e) {
				copyAmount = 10;
			}

		}
		logger.info("Exiting. m_copyClassType = " + m_copyClassType
				+ ", copyAmount = " + copyAmount);
	}

	private boolean isInvalidCopy(Subscriber subscriber, ViralSMSTable vst,
			boolean isProcess) {
		boolean isInvalid = false;
		if (subscriber.getSubscriberID().equalsIgnoreCase(vst.subID())
				|| subscriber.getSubscriberID().length() < 7
				|| subscriber.getSubscriberID().length() < 7)
			isInvalid = true;
		else if (!isSubActive(subscriber)
				&& !getParamAsBoolean("IS_ACT_OPTIONAL_PRESSSTAR", "TRUE"))
			isInvalid = true;
		else if (!subscriber.isCanAllow())
			isInvalid = true;
		else if (getParamAsBoolean("CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")
				&& subscriber.getUserType().equalsIgnoreCase(
						WebServiceConstants.CORPORATE)) {
			String language = subscriber.getLanguage();//RBT-14671 - # like
			Subscriber callerSub = m_rbtCopyLikeUtils.getSubscriber(vst.callerID());
			if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE") && !isProcess)
				sendSMSviaPromoTool(
						callerSub,
						RBTCopyLikeUtils.getSMSText("GATHERER", "CORP_COPY_CONTENT_SMS",
								m_corpCopyContentSMS, language));
			else
				sendSMS(callerSub,
						RBTCopyLikeUtils.getSMSText("GATHERER", "CORP_COPY_CONTENT_SMS",
								m_corpCopyContentSMS, language));
			isInvalid = true;
		}
		if (vst.type().equalsIgnoreCase(CROSSCOPY)){
			logger.info("For subscriber " + vst.callerID() + ", isInvalidCopy is "
					+ isInvalid +"because the type is :"+vst.type());
			isInvalid = true;
		}

		logger.info("For subscriber " + vst.callerID() + ", isInvalidCopy is "
				+ isInvalid);
		return isInvalid;
	}

	private String prepareCrossOperatorContentMissingSmsText(String called,
			String crossOperatorName, String clipName, String language) {//RBT-14671 - # like
		String sms = RBTCopyLikeUtils.getSMSText("GATHERER",
				"CROSS_COPY_CONTENT_MISSING_SMS_TEXT",
				m_crossCopyContentMissingSmsText, language);
		if (called == null || called.length() <= 0)
			called = "";
		sms = Tools.findNReplace(sms, "%CALLED%", called);
		if (clipName != null)
			sms = Tools.findNReplace(sms, "%SONG_NAME%", clipName);
		else
			sms = Tools.findNReplace(sms, "%SONG_NAME%", "");
		if (crossOperatorName != null)
			sms = Tools.findNReplace(sms, "%OPERATOR%", crossOperatorName);
		else
			sms = sms.replace(" %OPERATOR%", "");
		return sms;
	}

	public void getDefaultClip() {
		logger.info("Entering");
		int defaultClipId = -1;
		Clip clip = null;
		defaultClipId = getParamAsInt("COMMON", "DEFAULT_CLIP", -1);
		if (defaultClipId > -1)
			clip = rbtConnector.getMemCache().getClip(defaultClipId);
		if (clip != null)
			defaultClipWavName = clip.getClipRbtWavFile();
		logger.info("Exiting. defaultClipWavName is " + defaultClipWavName);
	}

	private void initializeFeed() {
		logger.info("Entering");
		schedule = rbtConnector.getSubscriberRbtclient().getFeed("CRICKET",
				"SP", "GATHERER");
		if (schedule == null) {
			/*
			 * int criInt = 2; criInt = getParamAsInt("COMMON",
			 * "CRICKET_INTERVAL", 2);
			 */
			ArrayList<Feed> fs = rbtConnector.getSubscriberRbtclient()
					.getFeeds("CRICKET", "SP");
			if (fs != null && fs.size() > 0)
				schedule = fs.get(0);
		}
		logger.info("Exiting. schedule is " + schedule);
	}

	private boolean isShufflePresentSelection(String subID, String callerID) {
		return rbtDBManager.isShufflePresentSelection(subID, callerID);
	}

	public ViralSMSTable[] getViralSMSTableLimit(String type, int count) {
		return rbtDBManager.getViralSMSByTypeAndLimit(type, count);
	}

	public ViralSMSTable[] getViralSMSByType(String type) {
		return rbtDBManager.getViralSMSByType(type);
	}

	public void removeViralPromotion(String subscriberID, String callerID,
			Date sentTime, String type) {
		rbtConnector.getSubscriberRbtclient().removeViralData(subscriberID,
				callerID, type, sentTime);
	}

	private void removeCopyViralPromotion(String subscriberID, String callerID,
			Date sentTime) {
		rbtDBManager.removeCopyViralPromotion(subscriberID, callerID, sentTime);
	}

	public void updateViralPromotion(String subscriberID, String callerID,
			Date sentTime, String fType, String tType, String extraInfo) {
		rbtConnector.getSubscriberRbtclient().updateViralData(subscriberID,
				callerID, null, sentTime, fType, tType, null, null, extraInfo);
	}

	private void updateCopyViralPromotion(String subscriberID, String callerID,
			Date sentTime, String tType, String extraInfo) {
		rbtDBManager.updateCopyViralPromotion(subscriberID, callerID, sentTime,
				tType, new Date(System.currentTimeMillis()), null, extraInfo);
	}

	private void setSearchCountCopy(String strSubID, int count, String type,
			Date sent, String callerID) {
		logger.info("tryCount=" + count);
		rbtDBManager.setSearchCountCopy(strSubID, type, count, sent, callerID);
	}

	private ResponseEnum processNonLocalCopy(ViralSMSTable vst, String subType,
			Subscriber subscriber) {
		RBTHttpClient rbtHttpClient = this.rbtHttpClient;
		logger.info("Processing non local copy. subID: " + vst.subID()
				+ ", callerID: " + vst.callerID() + ", clipID: "
				+ vst.clipID() + ", sentTime: " + vst.sentTime());
		String selBy = vst.selectedBy();
		String url = null;
		List<String> successCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_SUCCESS_CODES","0,2,3,4,5,6,7,8,9"), ",");
		List<String> retryCodes = tokenizeArrayList(getParamAsString("GATHERER" , "NON_LOCAL_COPY_RETRY_CODES",null), ",");
		/*
		 * boolean useProxy = false; String proxyHost = null; int proxyPort =
		 * -1;
		 */
		String extraInfoStr = vst.extraInfo();
		String circleId = subscriber.getCircleID();
		if (circleId != null) {
			if (circleId.equalsIgnoreCase("CENTRAL"))
				url = getURL("OPERATOR");
			else
				url = getURL(circleId);
		} else
			url = getURL("CROSS_OPERATOR");
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);

		if (subType.equalsIgnoreCase(m_nationalType)) {

			if (circleId.equals("CENTRAL")) {
				
				rbtHttpClient = nationalRbtHttpClient;
				/*
				 * useProxy = m_nationalUseProxy; proxyHost =
				 * m_nationalProxyHost; proxyPort = m_nationalProxyPort;
				 */
			}

			url = Tools.findNReplaceAll(url, "rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			url = url + "rbt_cross_copy.jsp?subscriber_id=" + vst.subID()
					+ "&caller_id=" + vst.callerID() + "&clip_id="
					+ vst.clipID() + "&sel_by=" + selBy + "&sms_type="
					+ vst.type();
		} else if (subType.equalsIgnoreCase(m_nonOnmobileType)) {
			rbtHttpClient = nonOnmobileRbtHttpClient;
			/*
			 * useProxy = m_nonOnmobileUseProxy; proxyHost =
			 * m_nonOnmobileProxyHost; proxyPort = m_nonOnmobileProxyPort;
			 */

			String vcode = vst.clipID();
			if (vcode != null && vcode.indexOf(":") != -1)
				vcode = vcode.substring(0, vcode.indexOf(":"));
			Clip clip = null;
			if (vcode != null)
				clip = getClipRBT(vcode);
			String clipName = "";
			if (clip != null)
				clipName = clip.getClipName();

			url = url + "startcopy.jsp?called=" + vst.subID() + "&caller="
					+ vst.callerID() + "&clip_id=" + vcode + "&sms_type="
					+ vst.type();
			if (url != null && clipName != null
					&& !clipName.equalsIgnoreCase(""))
				url = url + "&songname=" + clipName;
		} else if (subType.equalsIgnoreCase(m_crossOperatorType)) {
			rbtHttpClient = crossOperatorRbtHttpClient;
			/*
			 * useProxy = m_crossOperatorUseProxy; proxyHost =
			 * m_crossOperatorProxyHost; proxyPort = m_crossOperatorProxyPort;
			 */

			boolean isCentral = getParamAsBoolean("IS_CENTRAL_SITE", "FALSE");
			String wavFile = null;
			if (vst.clipID() != null)
				wavFile = new StringTokenizer(vst.clipID(), ":").nextToken()
						.trim();
			Clip clip = null;
			if (wavFile != null)
				clip = getClipRBT(wavFile);
			int clipID = -1;
			if (clip != null)
				clipID = clip.getClipId();
			String finalClipID = vst.clipID();
			String clipName = "";
			if (clip != null)
				clipName = clip.getClipName();
			String promoCode = "";
			if (clip != null)
				promoCode = clip.getClipPromoId();
			if (isCentral) {
				if(vst.type().equalsIgnoreCase(PROMOTE) ){
					logger.info("Rejecting request as it is not an onmobile request for : " + vst.callerID());
					return ResponseEnum.FAILURE ;
				}
				/*
				 * Integer statusInt = new Integer(-1); StringBuffer result =
				 * new StringBuffer();
				 */
				finalClipID = clipID + ":" + wavFile;
				String sourceOp = getParamAsString(GATHERER, SOURCE_OPERATOR,
						null);
				finalClipID = finalClipID + "&source_op=" + sourceOp;
				url = url + "rbt_rdc_copy_transfer.jsp?subscriber_id="
						+ vst.subID() + "&caller_id=" + vst.callerID()
						+ "&clip_id=" + finalClipID + "&sms_type=" + vst.type();

				if (viralInfoMap != null
						&& viralInfoMap.containsKey(KEYPRESSED_ATTR)
						&& url != null) {
					String keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
					if (keypressed != null && keypressed.length() > 0
							&& !keypressed.equalsIgnoreCase("null"))
						url = url + "&keypressed=" + keypressed;
					
					//RBT-12781 check added for supporting "CROSSCOPY_KEY" configuration.
					ArrayList<String> crosscopyKey=tokenizeArrayList(getParamAsString("COMMON",CROSSCOPY_KEY,null), ",");
					logger.debug("configured crosscopyKey: "+crosscopyKey);
					if(crosscopyKey!=null &&  !isKeyConfigured(crosscopyKey, keypressed)) {
						return ResponseEnum.SUCCESS;
					}else if(crosscopyKey == null && keypressed != null	&& keypressed.toLowerCase().indexOf("s") == -1) {	
							logger.info("Inter-operator copy request failing for "
									+ vst.callerID()
									+ " as keypressed "
									+ keypressed + " contains s");
							return ResponseEnum.SUCCESS;
					}
				}

				if (url != null && clipName != null
						&& !clipName.equalsIgnoreCase(""))
					url = url + "&songname=" + clipName;
				if (url != null && promoCode != null)
					url = url + "&tonecode=" + promoCode;
				HttpResponse httpResponse = null;
				try {
					httpResponse = rbtHttpClient.makeRequestByGet(url, null);
				} catch (Exception e) {

				}
				String response = null;
				if (httpResponse != null)
					response = httpResponse.getResponse().trim();

				/*
				 * Tools.callURL(url, statusInt, result, useProxy, proxyHost,
				 * proxyPort); String response = result.toString().trim();
				 */

				if (response != null
						&& (response.indexOf("SUCCESS") != -1 || response
								.indexOf("SUCESS") != -1)) {
					logger.info("Copy successful for the following url " + url);
					return ResponseEnum.SUCCESS;
				}				
				if (response != null && response.length() == 1
						&& successCodes!= null && successCodes.contains(response)) {
					logger.info("Non-onmobile copy successful for the following url "
							+ url);
					return ResponseEnum.SUCCESS;
				}else if(response != null && response.length() == 1
						&& retryCodes!= null && retryCodes.contains(response)){
					logger.info("RETRYING AND THE Response CODE IS CONFIGURED in retry code ");
					return ResponseEnum.RETRY;
				}else {
					logger.info("Copy unsuccessful for the following url "
							+ url);
					return ResponseEnum.FAILURE;
				}
			}

			url = url + "rbt_cross_copy.jsp?subscriber_id=" + vst.subID()
					+ "&caller_id=" + vst.callerID() + "&clip_id="
					+ finalClipID + "&sms_type=" + vst.type() + "&sel_by="
					+ selBy;
			if (url != null && clipName != null
					&& !clipName.equalsIgnoreCase(""))
				url = url + "&songname=" + clipName;
			if (url != null && promoCode != null)
				url = url + "&tonecode=" + promoCode;
		}

		/*
		 * Integer statusInt = new Integer(-1); StringBuffer result = new
		 * StringBuffer();
		 */

		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR)
				&& url != null) {
			String keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
			if (keypressed != null && keypressed.length() > 0
					&& !keypressed.equalsIgnoreCase("null"))
				url = url + "&keypressed=" + keypressed;
		}

		String sourceClipName = "";
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
			sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
		if (url != null && sourceClipName != null
				&& !sourceClipName.equalsIgnoreCase("")
				&& url.indexOf("songname") == -1)
			url = url + "&songname=" + sourceClipName;

		/*
		 * Tools.callURL(url, statusInt, result, useProxy, proxyHost,
		 * proxyPort); String response = result.toString().trim();
		 */

		HttpResponse httpResponse = null;
		try {
			httpResponse = rbtHttpClient.makeRequestByGet(url, null);
		} catch (Exception e) {

		}
		String response = null;
		if (httpResponse != null)
			response = httpResponse.getResponse();

		if (response != null
				&& (response.indexOf("SUCCESS") != -1 || response
						.indexOf("SUCESS") != -1)) {
			logger.info("Copy successful for the following url " + url);
			return ResponseEnum.SUCCESS;
		}
		//Split it 
		if (response != null && response.length() != -1
				&& successCodes!=null && successCodes.contains(response)) {
			logger.info("Non-onmobile copy successful for the following url "
					+ url);
			return ResponseEnum.SUCCESS;
		}else if(response != null && response.length() != -1
				&& retryCodes!=null && retryCodes.contains(response)){
			logger.info("RETRYING AND THE Response CODE IS CONFIGURED in retry code ");
			return ResponseEnum.RETRY;
		}else {
			logger.info("Copy unsuccessful for the following url " + url);
			return ResponseEnum.FAILURE;
		}
	}

	public String getURL(String circleId) {

		return m_operatorPrefixMap.get(circleId);
	}

	public String getCalleeOperator(Subscriber subscriber, String selectedBy) {
		String circleId = null;
		String response = "NA";
		if (m_RdcNovaOperatorMap == null
				|| (subscriber == null && selectedBy == null))
			return response;

		if (subscriber != null) {
			circleId = subscriber.getCircleID();
			if (circleId != null)
				response = m_RdcNovaOperatorMap.get(LOCAL_OPERATOR);
		}

		if (selectedBy != null && selectedBy.contains("XCOPY")) {
			String operator = selectedBy.split("_")[0];
			if (m_RdcNovaOperatorMap.containsKey(operator))
				response = m_RdcNovaOperatorMap.get(operator);
			else
				response = operator;
		}

		return response;

	}

	public void copyTestFailed(ViralSMSTable vst, String subType) {
		String confMode = "-";//RBT-14671 - # like
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());
		logger.info("Entered with subType = " + subType + ", for subscriber = "
				+ vst.callerID());

		if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
			try {
				eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
						subType, "-", "-", vst.sentTime(), OPTINCOPY, "s",
						"COPYTESTFAILED", vst.clipID(), confMode,
						getCalleeOperator(sub, vst.selectedBy()), new Date());
			} catch (ReportingException e) {
				logger.info("Caught an exception while writing event logs");
				logger.error("", e);
			}
			if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
				writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
						Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss"), subType, " - ", "-",
						"COPYTESTFAILED", "s", OPTINCOPY, confMode);
			}
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type());
		} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type());
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
					Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), subType, " - ", "-",
					"COPYTESTFAILED", "s", OPTINCOPY, confMode);
		} else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type(), "COPYTESTFAILED", null);

	}

	private void copyFailed(ViralSMSTable vst, String reason,
			String keyPressed, String confMode) {
		logger.info("Entered with reason = " + reason + ", for subscriber = "
				+ vst.callerID());//RBT-14671 - # like
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());
		String copyType = DEFAULTCOPY;
		if (vst.type().equalsIgnoreCase(COPY)
				|| vst.type().equalsIgnoreCase(COPYCONFIRM))
			copyType = DIRECTCOPY;
		String subType = m_localType;
		if (vst.callerID() == null)
			subType = "UNKNOWN";
		else if (vst.type().equalsIgnoreCase(COPYCONFIRMED))
			copyType = OPTINCOPY;
		if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
			try {
				eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
						subType, "-", "-", vst.sentTime(), copyType,
						keyPressed, reason, vst.clipID(), confMode,
						getCalleeOperator(sub, vst.selectedBy()), new Date());
			} catch (ReportingException e) {
				logger.info("Caught an exception while writing event logs");
				logger.error("", e);
			}
			if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
				writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
						Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss"), subType, " - ", "-",
						reason, keyPressed, copyType, confMode);
			}
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type());
		} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type());
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
					Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), subType, " - ", "-",
					reason, keyPressed, copyType, confMode);
		} else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					vst.type(), reason, null);
	}

	public void copyExpired(ViralSMSTable vst, String subType) {
		logger.info("Entered with subType = " + subType + ", for subscriber = "
				+ vst.callerID());//RBT-14671 - # like
		Subscriber subscriber = m_rbtCopyLikeUtils.getSubscriber(vst.callerID());
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(vst.extraInfo());
		String confMode = "-";
		String keyPressed = "NA";
		if (vst.type().equalsIgnoreCase(COPY)
				|| vst.type().equalsIgnoreCase(COPYCONFIRM)) {
			keyPressed = "s9";
		} else if (vst.type().equalsIgnoreCase(COPYCONFIRMED)) {
			keyPressed = "s";
		}
		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR)) {
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		}
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		String wasActive = "YES";
		if (isSubActive(subscriber))
			wasActive = "NO";
		if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
			try {
				/*
				 * String wavFile = "-"; int clipId = -1; Clip clip = null;
				 * StringTokenizer stk = new StringTokenizer(vst.clipID(), ":");
				 * if (stk.hasMoreTokens()) wavFile = stk.nextToken(); if
				 * (wavFile != null && wavFile.length() > 0 &&
				 * !wavFile.equalsIgnoreCase("-")) clip = getClipRBT(wavFile);
				 * if (clip != null) clipId = clip.getClipId();
				 */
				eventLogger.copyTrans(vst.subID(), vst.callerID(), wasActive,
						subType, "-", "-", vst.sentTime(), OPTINCOPY,
						keyPressed, COPYEXPIRED, vst.clipID(), confMode,
						getCalleeOperator(sub, vst.selectedBy()), new Date());

			} catch (ReportingException e) {
				logger.info("Caught an exception while writing event logs");
				logger.error("", e);
			}
			if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
				writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
						Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss"), subType, wasActive,
						"-", COPYEXPIRED, keyPressed, OPTINCOPY, confMode);
			}
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					COPYCONFPENDING);
		} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					COPYCONFPENDING);
			writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
					Tools.getFormattedDate(vst.sentTime(),
							"yyyy-MM-dd HH:mm:ss"), subType, wasActive, "-",
					COPYEXPIRED, keyPressed, OPTINCOPY, confMode);
		} else
			updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					COPYCONFPENDING, COPYEXPIRED, null);
	}

	// Added for airtel to specify caller type
	public static String getSpecialCopyMode(Subscriber sub, Subscriber called) {
		try {
			logger.info("Getting the caller type for " + sub.getSubscriberID()
					+ " copying from " + called.getSubscriberID());
			if (sub == null || called == null)
				return "";

			if (called.getCircleID() == null)
				return "";

			String virtualNumberConfig = RBTParametersUtils.getParamAsString(
					"VIRTUAL_NUMBERS", called.getSubscriberID(), null);
			if (virtualNumberConfig != null) {
				String circleID = null;
				String[] tokens = virtualNumberConfig.split(",");
				if (tokens.length >= 3)
					circleID = tokens[2];
				if (sub.getCircleID() != null
						&& sub.getCircleID().equalsIgnoreCase(circleID)) {
					// subscriber called a virtual number and
					// belongs to the same circle.
					return "|CALLER_TYPE:VN";
				}
			}
			if (sub.getCircleID().equalsIgnoreCase(called.getCircleID()))
				return "|CALLER_TYPE:P2P";

			if (called.getCircleID().equalsIgnoreCase("NON_ONMOBILE"))
				return "|CALLER_TYPE:COM";
			if (!sub.getCircleID().equalsIgnoreCase(called.getCircleID()))
				return "|CALLER_TYPE:ONM";

			return "";
		} catch (Exception e) {
			// safety check
			logger.error("Error while getting the caller type ", e);
			return "";
		}
	}

	/*
	 * This is used for processing the copy request. Here for activation and
	 * selection it makes separate request to webservice. We didn't made it in
	 * one request because of some airtel features
	 */
	public String processLocalCopyRequest(ViralSMSTable vst, boolean isProcess,
			Subscriber subscriber) {
		logger.info("Processing local copy request. subscriber: " + subscriber
				+ ", isProcess: " + isProcess);
		if (isProcess == false) {
			vst.setStartTime(Calendar.getInstance().getTime());
			writeCopyStats(vst.toString() + "Copy Process StartTime "
					+ statsDateFormat.format(vst.getStartDate()));
		}
		boolean isActivated = false;
		String caller = vst.callerID();
		boolean isOptinCopy = false;
		String called = vst.subID();
		String setForCaller = null;
		//RBT-14671 - # like
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());
		Subscriber callerSub = m_rbtCopyLikeUtils.getSubscriber(vst.callerID());
		String clipID = vst.clipID();
		String selectedBy = vst.selectedBy();
		String selectedForSMSBy = vst.selectedBy();
		Date currentDate = null;
		String response = null;
		String crossOperatorName = null;
		String extraInfoStr = vst.extraInfo();
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);
		String keyPressed = "NA";
		String copyType = DEFAULTCOPY;
		String confMode = "-";
		if (vst.type().equalsIgnoreCase(COPY)
				|| vst.type().equalsIgnoreCase(COPYCONFIRM)) {
			copyType = DIRECTCOPY;
			keyPressed = "s9";
		} else if (vst.type().equalsIgnoreCase(COPYCONFIRMED)) {
			copyType = OPTINCOPY;
			keyPressed = "s";
		}
		String sourceClipName = "";
		String offerSubClass = null;
		boolean isThirdPartyAllowedKeys = false;
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
			sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(IS_THIRD_PARTY_ALLOWEDED_KEY))
			isThirdPartyAllowedKeys = Boolean.parseBoolean(viralInfoMap
					.get(IS_THIRD_PARTY_ALLOWEDED_KEY)); 
		if(!isValidKeyPressed(caller, keyPressed)){
			copyFailed(vst, "INVALID_KEYPRESSED", keyPressed, confMode);
			logger.info("DELETING LOCAL COPY REQUEST BECAUSE OF INVALID KEY" + keyPressed);
			return "INVALID_KEYPRESSED";
		}
		if (clipID != null && clipID.toUpperCase().indexOf("DEFAULT_") != -1
				&& clipID.length() >= 16) {
			try {
				String currentTime = clipID.substring(8, 16);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				try {
					currentDate = sdf.parse(currentTime);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {

			}
		}
		if (getParamAsBoolean("SHOW_OPERATOR_NAME_CROSS_OPERATOR_SMS", "FALSE")
				&& selectedBy != null && selectedBy.indexOf("XCOPY") != -1) {
			crossOperatorName = selectedBy
					.substring(0, selectedBy.indexOf("_"));
		}
		if (selectedBy != null && !selectedBy.equalsIgnoreCase("null"))
			selectedBy = selectedBy.trim().toUpperCase();
		else
			selectedBy = "PRESSSTAR";

		int cat = 26;
		int status = 1;
		String wavFile = null;
		StringBuffer wavFileBuf = new StringBuffer();
		StringBuffer catTokenBuf = new StringBuffer();
		StringBuffer catNameBuf = new StringBuffer();
		StringBuffer classTypeBuffer = new StringBuffer();
		StringBuffer statusBuf = new StringBuffer();
		StringBuffer setForCallerBuf = new StringBuffer();
		StringBuffer vrbtBuf = new StringBuffer();
		String songName = null;
		String classType = m_copyClassType; // TODO change
		boolean isCallerSubscribed = true;
		boolean isCopyDone = false;
		boolean isPollRBTCopy = false;

		String callerCircleID = null;

		if (subscriber == null)//RBT-14671 - # like
			subscriber = m_rbtCopyLikeUtils.getSubscriber(caller);
		callerCircleID = subscriber.getCircleID();

		if (isInvalidCopy(subscriber, vst, isProcess)) {
			copyFailed(vst, "INVALIDCOPY", keyPressed, confMode);
			return "INVALIDCOPY";
		}
		if (clipID != null && clipID.toUpperCase().indexOf("DEFAULT_") != -1
				&& currentDate != null) {
			isPollRBTCopy = true;
			if (isProcess && getParamAsBoolean("ALLOW_POLL_COPY", "FALSE")) {
				logger.debug("Processing poll copy");

				wavFile = clipID;

				if (!isSubActive(subscriber)) {
					String copytype = "";
					if (RBTParametersUtils.getParamAsBoolean("GATHERER",
							"ALLOW_SPECIAL_COPY_MODE", "FALSE")) {//RBT-14671 - # like
						copytype = getSpecialCopyMode(m_rbtCopyLikeUtils.getSubscriber(caller),
								m_rbtCopyLikeUtils.getSubscriber(called));
					}
					String defaultSubType = RBTParametersUtils
							.getParamAsString("GATHERER", "DEFAULT_SUBTYPE",
									"pre");
					String copySubClass = RBTParametersUtils.getParamAsString(
							"GATHERER", "COPY_SUB_CLASS", "COPY");
					String mode = "COPY";
					// RBT Like feature
					if (SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {
						logger.info("Checking configuration"
								+ " for Like feature. keyPressed: "
								+ keyPressed);
						copySubClass = getParamAsString("GATHERER",
								Constants.RBT_LIKE_SUB_CLASS, copySubClass);
						mode = getParamAsString("GATHERER",
								Constants.RBT_LIKE_MODE, "RBT_LIKE");
						logger.info("Configured subClass: " + copySubClass
								+ ", confMode: " + mode);
					}

					boolean prepaid = defaultSubType.equalsIgnoreCase("pre");
					//GetOffer Tef-Spain RBT New Re-pricing
					boolean isAllowBaseSelOffer = getParamAsBoolean("ALLOW_GET_OFFER", "FALSE");
					boolean isAllowOnlyBaseOffer = getParamAsBoolean("ALLOW_ONLY_BASE_OFFER", "FALSE");
					String subOfferId = null;
					if(isAllowBaseSelOffer || isAllowOnlyBaseOffer) {
						Offer offer = getOffer(caller, "sub");
						if(offer != null) {
							copySubClass = offer.getSrvKey();
							offerSubClass = copySubClass;
							subOfferId = offer.getOfferID();
						}
					}
					
					if(sub !=  null & freemiumSubClassList.contains(sub.getSubscriptionClass())) {
						String temp =  getParamAsString(mode.toUpperCase() + "_" + sub.getSubscriptionClass().toUpperCase()+"_ACTIVATE_MODE_COPY_FROM_FREEMIUM_USER");
						mode = temp != null? temp : mode;
					}
					
					mode = RBTParametersUtils.getParamAsString("GATHERER","CG_INACTIVE_MODE_CONFIGURED", mode);
					//RBT-17850 Pilot for Copy Callertunes
					String circleModeMapping = RBTParametersUtils.getParamAsString("GATHERER","COPY_MODE_MAPPING_BY_CIRCLE", null);
					if(circleModeMapping!=null){
						Map<String,String> circleModeMap = MapUtils.convertIntoMap(circleModeMapping, ",", ":", null);
						if(circleModeMap!=null && callerCircleID!=null){
							String mappedCircleMode = circleModeMap.get(callerCircleID);
							if(mappedCircleMode!=null){
								mode = mappedCircleMode;
							}
						}
					}
					StringBuffer strBuff = new StringBuffer();
					subscriber = activateSubscriber(caller, callerCircleID,
							called, mode, prepaid, copySubClass, copytype
									+ "|CP:" + selectedBy + "-" + vst.subID()
									+ ":CP|", vst.selectedBy(), null, null,
							confMode, subOfferId, strBuff);
					updateExtraInfoAndPlayerStatus(caller, true);
					isActivated = true;
						String language = subscriber != null ? subscriber
							.getLanguage() : null;

					handleBaseOfferNotFoundAndDwnldLimitReached(strBuff, callerSub, language);

				} else {
					StringBuffer strBuff = new StringBuffer();
					SelectionRequestBean selBean = new SelectionRequestBean();
					selBean.setSubscriberId(caller);
					selBean.setStatus(1);
					selBean.setFromTimeOfTheDay(0);
					selBean.setToTimeOfTheDay(23);
					rbtConnector.getSubscriberRbtclient().deleteSelections(
							selBean, "GATHERER", "DAEMON", strBuff);
					updateExtraInfoAndPlayerStatus(caller, true);
					isCopyDone = true;
				}
			}
		}

		Clip clip = null;
		Category category = null;

		boolean isVirtualNo = false;
		boolean isVirtualNoTypeTwo = false;
		if (copyVirtualNumbers != null && copyVirtualNumbers.size() > 0) {
			if (copyVirtualNumbers.contains(called)) {
				isVirtualNo = true;
				logger.info("match found");
			}
		}

		if (!isPollRBTCopy) {
			logger.debug("Processing non-poll copy. caller: " + caller
					+ ", clipID: " + clipID);
			if (clipID != null)
				cat = getClipCopyDetails(clipID, wavFileBuf, catTokenBuf,
						catNameBuf, classTypeBuffer, statusBuf,
						setForCallerBuf, isVirtualNo, vrbtBuf);
			logger.info("Got the category, category: " + cat + ", for clipID: "
					+ clipID);
			if (setForCallerBuf.length() > 0)
				setForCaller = setForCallerBuf.toString();
			if (wavFileBuf.toString().trim().length() > 0
					&& wavFileBuf.toString().trim().indexOf(">") != -1) {
				cat = Integer.parseInt(wavFileBuf.toString().substring(
						wavFileBuf.indexOf(">") + 1));
				wavFileBuf.delete(wavFileBuf.indexOf(">"), wavFileBuf.length());
				category = rbtConnector.getMemCache().getCategory(cat);
				if (category != null) {
					catNameBuf = new StringBuffer(category.getCategoryName());
				}
			} else {
				category = rbtConnector.getMemCache().getCategory(cat);
			}
			logger.debug("Got the category: " + category);
			if ((clipID == null || (clipID.toUpperCase().indexOf("DEFAULT") != -1 && clipID
					.toUpperCase().indexOf("MISSING") == -1))
					&& defaultClipWavName != null) {
				wavFileBuf = new StringBuffer(defaultClipWavName);
				logger.info("Settinf wavFileBuffer as default :"
						+ defaultClipWavName);
			}

			if (classTypeBuffer.toString().trim().length() > 0)
				classType = classTypeBuffer.toString().trim();

			try {
				status = Integer.parseInt(statusBuf.toString().trim());
				if (getParamAsBoolean("DAEMON", "CALLER_ID_HIGHER_PRIORITY", "FALSE")) {
					if (status == 91) {
						status = 1;
					} else if (status == 92) {
						status = 80;
					}
				}
			} catch (Exception e)

			{
				// logger.error("", e);
				status = 1;
			}
			wavFile = wavFileBuf.toString().trim();

			if (wavFile != null && wavFile.length() > 0 && status != 90
					&& status != 99)
				clip = getClipRBT(wavFile);
		}

		
		
		if (vst.type().equalsIgnoreCase(PROMOTE) ) {
			String subTypeRegion = m_localType;
			String responseText = "FAILURE";
			
			if (Utility.checkDNDUser(caller)) {
				logger.info("As " + caller + " is DND user, so not processing the request");
				responseText = "DND-ENABLED";
			} else {
				if (viralInfoMap != null
						&& viralInfoMap.containsKey(KEYPRESSED_ATTR)) {
					keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);

					String promoteURLKey = "PROMOTE_MSEARCH_URL_FOR_KEY_" + keyPressed.charAt(0);
					String url = RBTParametersUtils.getParamAsString("COMMON",
							promoteURLKey, null);
					logger.debug("Promotion key:" + promoteURLKey + " URL:" + url );

					if (url != null) {
					url = url.replace("<MSISDN>", caller);
						responseText = promoteThroughMSearch(url);
					} else {
						logger.warn("Promote URL is not configured for " + promoteURLKey);
					}
				}
			}
			
			if ("SUCCESS".equalsIgnoreCase(responseText)) {
				isActivated = true;
			}
			if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
				
				writeTrans(vst.subID(), vst.callerID(), wavFile,
						catNameBuf.toString(), Tools.getFormattedDate(
								vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO", String.valueOf(isActivated),
										responseText, keyPressed, PROMOTE, confMode);
			}
			
			if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
				
				try {
					eventLogger.copyTrans(vst.subID(), vst.callerID(),
							isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), String.valueOf(isActivated), vst.sentTime(),
									responseText, keyPressed, PROMOTE, wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());
					
				} catch (ReportingException e) {
					logger.info("Caught an exception while writing event logs");
					logger.error("", e);
				}
				
			}
			
			removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(),
					PROMOTE);
			logger.info("Response after promote :" + responseText);
			return responseText;
		}
		
		if (clip != null && clip.getContentType() != null) {
			List<String> contentTypes = Arrays.asList(getParamAsString(
					GATHERER, "COPY_NON_SUPPORTED_CONTENT_TYPES", "")
					.toUpperCase().split(","));
			if (contentTypes.contains(clip.getContentType().toUpperCase())) {
				String language = subscriber.getLanguage();//RBT-14671 - # like
				String smsText = RBTCopyLikeUtils.getSMSText(GATHERER,
						"NON_SUPPORTED_CONTENT_TYPE_SMS" + "_"
								+ clip.getContentType().toUpperCase(), null,
						language);
				if (smsText == null) {//RBT-14671 - # like
					smsText = RBTCopyLikeUtils.getSMSText(GATHERER,
							"NON_SUPPORTED_CONTENT_TYPE_SMS",
							m_nonCopyContentSMS, language);
				}

				if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE") && !isProcess)
					sendSMSviaPromoTool(
							callerSub,
							 m_rbtCopyLikeUtils.getSubstituedSMS(
									smsText,
									clip.getClipName() == null ? "" : clip
											.getClipName(), called, null, null,
									null, clip, category, null,null,null));
				else
					sendSMS(callerSub,
							 m_rbtCopyLikeUtils.getSubstituedSMS(
									smsText,
									clip.getClipName() == null ? "" : clip
											.getClipName(), called, null, null,
									null, clip, category, null,null,null));

				copyFailed(vst, "NONCOPY", keyPressed, confMode);
				return "NONCOPY";
			}
		}
		String catInfo = null;
		if (null != category) {
			catInfo = category.getCategoryInfo(CategoryInfoKeys.CONTENT_TYPE);
		}
		boolean isNonCopyContent = isNonCopyContent(clipID,
				catTokenBuf.toString(), clip, status, wavFile, isVirtualNo,
				subscriber, sub, keyPressed, category.getCategoryTpe(),
				isThirdPartyAllowedKeys, catInfo, selectedBy);
		// RBT-12581
		Category category1=category;//RBT-14671 - # like
		if (category1!=null && ! m_rbtCopyLikeUtils.isShuffleCategory(String.valueOf(category1.getCategoryId()))
				&& !getParamAsBoolean("IS_NORMAL_COPY_ALLOWED", "TRUE")
				&& category1.getCategoryEndTime() != null
				&& category1.getCategoryEndTime().getTime() < System
						.currentTimeMillis()) {
			isNonCopyContent=true;
		}
		
		
		//RBT-13822: Solution required for Multiple Charging at CG through P2P COPY || Airtel RBT
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(caller);
		rbtDetailsRequest.setMode(GATHERER);
		rbtDetailsRequest.setConsentInd(true);
		Subscriber subForStatusCheck = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		String subscriberStatus = subForStatusCheck.getStatus();
		// Jira :RBT-15026: Changes done for allowing the multiple
		// Azaan pack.
		String waveFileSubType = null;
		String[] waveFileArray = wavFile.split("_");
		waveFileSubType = confAzaanCopticDoaaSubTpeContentNameMap.get(wavFile);
		if (waveFileSubType == null && waveFileArray.length > 0)
			waveFileSubType = confAzaanCopticDoaaSubTpeContentNameMap
					.get(waveFileArray[0]);
		if (isNonCopyContent) {
			logger.info("The content is non copy content. Sending SMS.");
			String language = subscriber.getLanguage();
			// RBT-13585//RBT-14671 - # like
			if (m_rbtCopyLikeUtils.isTNBuser(callerSub.getSubscriptionClass())) {
				if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE"))
					sendSMSviaPromoTool(
							callerSub,
							RBTCopyLikeUtils.getSMSText(
									"GATHERER",
									"TNB_SONG_SELECTON_NOT_ALLOWED",
									"You cant select the above song as you are not a TNB user.",
									language));
				else
					sendSMS(callerSub,
							RBTCopyLikeUtils.getSMSText(
									"GATHERER",
									"TNB_SONG_SELECTON_NOT_ALLOWED",
									"You cant select the above song as you are not a TNB user.",
									language));

			} else if (getParamAsBoolean("SEND_CROSS_COPY_CONTENT_MISSING_SMS",
					"FALSE")
					&& wavFile != null
					&& wavFile.indexOf("MISSING") != -1) {
				if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE") && !isProcess)
					sendSMSviaPromoTool(
							callerSub,
							prepareCrossOperatorContentMissingSmsText(called,
									crossOperatorName, sourceClipName, language));
				else
					sendSMS(callerSub,
							prepareCrossOperatorContentMissingSmsText(called,
									crossOperatorName, sourceClipName, language));
			} else if (getParamAsBoolean("NON_COPY_SENT_SMS", "FALSE")) {
				if (clip != null
						&& clip.getClipEndTime() != null
						&& clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					deleteDownloadsForExpiredContent(subscriber.getSubscriberID(),clip,null);
					if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE")
							&& !isProcess)
						sendSMSviaPromoTool(
								callerSub,
								 m_rbtCopyLikeUtils.getSubstituedSMS(
										RBTCopyLikeUtils.getSMSText("GATHERER",
												"NON_COPY_EXPIRED_CLIP_SMS",
												m_nonCopyExpiredClipSMS,
												language),
										clip.getClipName() == null ? "" : clip
												.getClipName(), called, null,
										null, null, clip, category, null,null,null));
					else
						sendSMS(callerSub,
								 m_rbtCopyLikeUtils.getSubstituedSMS(
										RBTCopyLikeUtils.getSMSText("GATHERER",
												"NON_COPY_EXPIRED_CLIP_SMS",
												m_nonCopyExpiredClipSMS,
												language),
										clip.getClipName() == null ? "" : clip
												.getClipName(), called, null,
										null, null, clip, category, null,null,null));
				} else if (category1 != null
						&& category1.getCategoryEndTime() != null
						&& category1.getCategoryEndTime().getTime() < System
								.currentTimeMillis()) {
					deleteDownloadsForExpiredContent(subscriber.getSubscriberID(),null,category1);
					if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE")
							&& !isProcess)
						sendSMSviaPromoTool(
								callerSub,
								 m_rbtCopyLikeUtils.getSubstituedSMS(
										RBTCopyLikeUtils.getSMSText("GATHERER",
												"NON_COPY_EXPIRED_CATEGORY_SMS",
												m_nonCopyExpiredCategorySMS,
												language),
										category1.getCategoryName() == null ? "" : category1
												.getCategoryName(), called, null,
										null, null, clip, category, null,null,null));
					else
						sendSMS(callerSub,
								 m_rbtCopyLikeUtils.getSubstituedSMS(
										RBTCopyLikeUtils.getSMSText("GATHERER",
												"NON_COPY_EXPIRED_CATEGORY_SMS",
												m_nonCopyExpiredCategorySMS,
												language),
												category1.getCategoryName() == null ? "" : category1
														.getCategoryName(), called, null,
										null, null, clip, category, null,null,null));
				
				} else {

					if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE")
							&& !isProcess) {
						
						if(getParamAsString("GATHERER",
								"COPY_ALLOWED_NORAML_KEYS", null) != null && !isThirdPartyAllowedKeys) {
							sendSMSviaPromoTool(
									callerSub,
									RBTCopyLikeUtils.getSMSText(
											"GATHERER",
											"BLOCKED_USER_NON_COPY_CONTENT_SMS",
											"Sorry this selection cannot be copied",
											language));
						}
						else {
							sendSMSviaPromoTool(
									callerSub,
									RBTCopyLikeUtils.getSMSText(
											"GATHERER",
											"NON_COPY_CONTENT_SMS",
											"Sorry this selection cannot be copied",
											language));
						}
					}
					else {
						if(getParamAsString("GATHERER",
								"COPY_ALLOWED_NORAML_KEYS", null) != null && !isThirdPartyAllowedKeys){
							sendSMS(callerSub,
									RBTCopyLikeUtils.getSMSText(
											"GATHERER",
											"BLOCKED_USER_NON_COPY_CONTENT_SMS",
											"Sorry this selection cannot be copied",
											language));
						}
						else{
				
							sendSMS(callerSub,
									RBTCopyLikeUtils.getSMSText(
											"GATHERER",
											"NON_COPY_CONTENT_SMS",
											"Sorry this selection cannot be copied",
											language));
						}
					}
				}
			}
			copyFailed(vst, "NONCOPY", keyPressed, confMode);
			logger.info("Sent SMS and returning NONCOPY, since the content is non copy content");
			return "NONCOPY";
		} else if (subscriberStatus != null && subStatusesBlockedForCopy.contains(subscriberStatus)) {
			String language = subscriber.getLanguage();
			if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE")
					&& !isProcess) {//RBT-14671 - # like
				sendSMSviaPromoTool(
						callerSub,
						 m_rbtCopyLikeUtils.getSubstituedSMS(
								RBTCopyLikeUtils.getSMSText("GATHERER",
										"SUB_STATUS_BLOCKED_FOR_COPY_SMS",
										m_subStatusBlockedForCopySMS,
										language),
										category1.getCategoryName() == null ? "" : category1
												.getCategoryName(), called, null,
												null, null, clip, category, null,null,null));
			} else {
				sendSMS(callerSub,
						 m_rbtCopyLikeUtils.getSubstituedSMS(
								RBTCopyLikeUtils.getSMSText("GATHERER",
										"SUB_STATUS_BLOCKED_FOR_COPY_SMS",
										m_subStatusBlockedForCopySMS,
										language),
										category1.getCategoryName() == null ? "" : category1
												.getCategoryName(), called, null,
												null, null, clip, category, null,null,null));
			}

			copyFailed(vst, "NONCOPY", keyPressed, confMode);
			logger.info("Sent SMS and returning NONCOPY, since the content is non copy content");
			return "NONCOPY";
		} else if (category != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
				.isShuffleCategory(category.getCategoryTpe())
				&& category.getCategoryEndTime().after(new Date())
				&& getParamAsBoolean("COPY_SHUFFLE", "FALSE")
				&& !getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY", "FALSE"))
			songName = category.getCategoryName();
		else if (clip != null)
			songName = clip.getClipName();
		else if (status == 90)
			songName = "Cricket Feed";
		else if (azaanWavFileName != null
				&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
				&& rbtDBManager.azaanDefaultCosId != null)
			songName = azaanContentName != null ? azaanContentName : "Azaan";
		// Jira :RBT-15026: Changes done for allowing the multiple
		// Azaan pack.
		else if (waveFileSubType != null
				&& confAzaanCopticDoaaCosIdSubTypeMap.get(waveFileSubType) != null)
			songName = waveFileSubType != null ? waveFileSubType : "Coptic";

		String finalSelectedBy = "PRESSSTAR";
		if (selectedBy != null && selectedBy.indexOf("_XCOPY") == -1)
			finalSelectedBy = selectedBy;
		if (getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
			finalSelectedBy = "COPY";
		String selByOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
		if (selByOptInCopy != null) {
			if (vst.type().equalsIgnoreCase(COPYCONFIRMED))
				finalSelectedBy = selByOptInCopy;
		}
		if (status == 90
				&& getParamAsBoolean("COPY_CRICKET_SEL", "FALSE")
				&& !isSubAlreadyActiveOnStatus(subscriber.getSubscriberID(),
						setForCaller, 90)
				&& schedule != null
				&& schedule.getEndDate() != null
				&& schedule.getEndDate().after(
						new Date(System.currentTimeMillis()))
				&& schedule.getChargeClass() != null
				&& schedule.getChargeClass().trim().length() > 0
				&& !schedule.getChargeClass().trim().equalsIgnoreCase("null")) {
			classType = schedule.getChargeClass().trim().toUpperCase();
		}
		SubscriptionClass subscriberClass = getActivationSubClass(caller, subscriber, finalSelectedBy, null);
		ChargeClass chargeClassObj = getSelectionClass(caller, subscriber.getCircleID(),
				finalSelectedBy, classType, category, clip, false,status,classType);
		
		String renewalPeriod=null,renewalAmount=null,freePeriodText=null;
		
		String specialAmtChar = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON,
						"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
		
		String actAmt = (null != subscriberClass?subscriberClass.getSubscriptionAmount(): null);
		String selAmt = (null != chargeClassObj?chargeClassObj.getAmount(): null);
		
		if(null != selAmt && Double.parseDouble(selAmt.replace(specialAmtChar,"."))==0){
			renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(chargeClassObj.getSelectionPeriod());
			freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
					"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
			freePeriodText = freePeriodText.replace("DD",renewalPeriod);
			renewalAmount = chargeClassObj.getRenewalAmount();
		}
			
		ArrayList<String> starCopyConfigList = tokenizeArrayList(getParamAsString("COMMON","STARCOPY_KEY",""), ",");
		boolean isStarcopy=false;
		String key=null;
		String type=null;
		
		if (keyPressed != null && keyPressed.length() >= 1) {
			keyPressed = keyPressed.toLowerCase();
			if (isStarcopy == false && starCopyConfigList != null) {
				logger.info("RBT: copystar");
				for (int i = 0; i < starCopyConfigList.size(); i++) {
					key = (String) starCopyConfigList.get(i).toString()
							.toLowerCase();
					if (keyPressed.indexOf(key) != -1) {
						// type="COPYSTAR";
						type = COPYSTAR;
						isStarcopy = true;
						break;
					}
				}
			}
		}
		
		if (!isPollRBTCopy && isProcess) {
			boolean prepaid = subscriber.isPrepaid();
			if (getParamAsBoolean("DIFFERNTIAL_COPY", "FALSE")
					&& getParamAsString("DIFFERNTIAL_COPY_TYPE") != null)
				if (getSubscriberPromo(caller) != null
						&& getSubscriberPromo(called) != null)
					classType = "YOUTHCARD";

			if (copyChargeClassMap != null && copyChargeClassMap.size() > 0
					&& copyChargeClassMap.containsKey(selectedBy))
				classType = copyChargeClassMap.get(selectedBy);

			String subClass = getParamAsString("GATHERER", "COPY_SUB_CLASS",
					"COPY");
			

			// If user is not deactive overriding the subscription class
			if (subscriber != null
					&& subscriber.getSubscriptionClass() != null
					&& !subscriber.getStatus().equals(
							WebServiceConstants.DEACTIVE))
				subClass = subscriber.getSubscriptionClass();

			boolean makeSel = true;
			logger.info("Reached here makesel is " + makeSel);
			// Jira :RBT-15026: Changes done for allowing the multiple
			// Azaan pack.
			String contentName = waveFileSubType;
			if (azaanWavFileName != null
					&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
					&& rbtDBManager.azaanDefaultCosId != null) {
				makeSel = false; // since there is no clip, we need to skip
									// addSelection() block and just do
									// songPackUpgrade for both active &
									// inactive user
			} else if (contentName != null
					&& confAzaanCopticDoaaCosIdSubTypeMap.get(contentName) != null) {
				makeSel = false;
			}
			//RBT-14671 - # like
			if (defaultClipWavName != null
					&& defaultClipWavName.equalsIgnoreCase(wavFile)
					&& !m_rbtCopyLikeUtils.allowDefaultCopyUserHasSelection(subscriber, sub)) {
				makeSel = false;
				logger.info("Adding default selection is not allowed, makeSel: "
						+ makeSel);
			}

			if (!getParamAsBoolean("INSERT_DEFAULT_SEL", "FALSE")
					&& defaultClipWavName != null
					&& defaultClipWavName.equalsIgnoreCase(wavFile)) {
				makeSel = false;
				logger.info("Adding default selections is not allowed, makeSel: "
						+ makeSel);
			}

			if (defaultClipWavName != null
					&& defaultClipWavName.equalsIgnoreCase(wavFile)
					&& !isUserHavingAllCallerIDSelection(subscriber, wavFile)) {
				makeSel = false;
				response = "ALREADY_EXISTS";
				logger.info("User is active with no selections so not adding the selection, makeSel: "
						+ makeSel);
			}

			if (status == 90
					&& getParamAsBoolean("COPY_CRICKET_SEL", "FALSE")
					&& !isSubAlreadyActiveOnStatus(
							subscriber.getSubscriberID(), setForCaller, 90)
					&& schedule != null
					&& schedule.getEndDate() != null
					&& schedule.getEndDate().after(
							new Date(System.currentTimeMillis()))
					&& schedule.getChargeClass() != null
					&& schedule.getChargeClass().trim().length() > 0
					&& !schedule.getChargeClass().trim()
							.equalsIgnoreCase("null")) {
				// endDate = schedule.getEndDate();
				classType = schedule.getChargeClass().trim().toUpperCase();
			} else if (status == 90) {
				makeSel = false;
				logger.info("3.makeSel =" + makeSel);
			}

			if (selectedBy != null && selectedBy.indexOf("_XCOPY") == -1)
				finalSelectedBy = selectedBy;
			if (getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
				finalSelectedBy = "COPY";
			selByOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
			if (selByOptInCopy != null) {
				if (vst.type().equalsIgnoreCase(COPYCONFIRMED))
					finalSelectedBy = selByOptInCopy;
			}
			logger.info("the value of finalSelectedBy " + finalSelectedBy);
			String copytype = "";
			if (RBTParametersUtils.getParamAsBoolean("GATHERER",
					"ALLOW_SPECIAL_COPY_MODE", "FALSE")) {//RBT-14671 - # like
				copytype = getSpecialCopyMode(m_rbtCopyLikeUtils.getSubscriber(caller),
						m_rbtCopyLikeUtils.getSubscriber(called));
			}
			//selectedBy is used for selectionInfo
//			selectedBy = getP2PModeForDeactiveUser(subscriber, selectedBy, vst.type());
			
			selectedBy = copytype + "|CP:" + selectedBy + "-" + vst.subID()
					+ ":CP|";

			boolean useUIChargeClass = false;
			if (Utility.isSubActive(subscriber) && activeclassTypeParam != null) {
				useUIChargeClass = getParamAsBoolean(
						"USE_DIFFERENTIAL_UI_CHARGE_CLASS", "false");
				classType = activeclassTypeParam;
				logger.info("The called no is normal no & user is active & use ui is "
						+ useUIChargeClass + " and class type = " + classType);

			} else if (!Utility.isSubActive(subscriber)
					&& inactiveclassTypeParam != null) {
				useUIChargeClass = getParamAsBoolean(
						"USE_DIFFERENTIAL_UI_CHARGE_CLASS", "false");
				classType = inactiveclassTypeParam;
				logger.info("The called no is normal no & user is inactive & use ui is "
						+ useUIChargeClass + " and class type = " + classType);

			}

			//To support BI for VRBT subscriber
			String vrbtSubClass = null;
			String vrbtChargeClass = null;
			if(catTokenBuf != null && vrbtBuf.toString().equalsIgnoreCase("VRBT")) {
				String vrbtCategoryId = catTokenBuf.toString();
				if(m_vrbtCatIdSubSongSrvKeyMap != null && m_vrbtCatIdSubSongSrvKeyMap.containsKey(vrbtCategoryId)) {
					vrbtSubClass = getSubclassFromVrbtCatId(vrbtCategoryId);
					if(vrbtSubClass != null && (vrbtSubClass = vrbtSubClass.trim()).length() != 0) {
						subClass = vrbtSubClass;;
					}
					vrbtChargeClass = getClassTypeFromVrbtCatId(vrbtCategoryId);
					if(vrbtChargeClass != null && (vrbtChargeClass = vrbtChargeClass.trim()).length() != 0) {
						useUIChargeClass = true;
						classType = vrbtChargeClass;
					}
				}
			}
			
			//if key is configured as optin key, user is new user or deactive user and TNB subscription class is configured then override the subscription to TNB subscription class
			String TNBSubClass = getParamAsString("OPTIN_COPY_TNB_SUB_CLASS");
			logger.info("isStarCopy :" + isStarcopy +" and type is :" + type + " is sub active :" + isSubActive(subscriber) + "configured TNBSubClass :"+ TNBSubClass);
			if(isStarcopy && !isSubActive(subscriber) && TNBSubClass != null) {
				subClass = TNBSubClass;
			}
			
			SubscriptionClass subscriberClassBean = getActivationSubClass(caller, subscriber, finalSelectedBy, vrbtSubClass);
			ChargeClass chargeClassBean = getSelectionClass(caller, subscriber.getCircleID(),
					finalSelectedBy, classType, category, clip, useUIChargeClass,status,classType);
			
			
			actAmt = (null != subscriberClassBean?subscriberClassBean.getSubscriptionAmount():null);
			selAmt = (null != chargeClassBean?chargeClassBean.getAmount():null);
			
			if(null != selAmt && Double.parseDouble(selAmt.replace(specialAmtChar,"."))==0){
				renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(chargeClassBean.getSelectionPeriod());
				freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
						"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
				freePeriodText = freePeriodText.replace("DD",renewalPeriod);
				renewalAmount = chargeClassBean.getRenewalAmount();
			}
			
			
			if(getParamAsBoolean("DCT_ALL_CALLER_SEL_FOR_DEFAULT_COPY", "FALSE") && defaultClipWavName != null 
					&& defaultClipWavName.equalsIgnoreCase(wavFile) && (subscriber == null
							|| !isSubActive(subscriber))) {
				logger.info("In-Active user copy default song, Subscriber: " + subscriber );
				makeSel = false;
			}
			
			logger.info("Checking to make selection category: " + category
					+ ", clip: " + clip + ", useUIChargeClass: "
					+ useUIChargeClass);
			if (getParamAsBoolean("BLOCK_COPY_SHUFFLE_SUBSCRIBER", "FALSE")
					&& isShufflePresentSelection(caller, setForCaller)) {
				String language = subscriber.getLanguage();
				logger.info("Blocking selection for shuffle subscriber "
						+ caller);//RBT-14671 - # like
				String smsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"SMS_TEXT_BLOCK_COPY_SHUFFLE_SUBSCRIBER",
						smsTextBlockCopyForShuffleSubscriber, language);
				if (getParamAsBoolean("SEND_SMS_BLOCK_COPY_SHUFFLE_SUBSCRIBER",
						"TRUE") && smsText != null && smsText.length() > 0)
					sendSMS(callerSub, smsText);
				return "SHUFFLE_SELECTION_EXISTS";
			} else if (makeSel) {
				Clip clipTemp = rbtConnector.getMemCache()
						.getClipByRbtWavFileName(wavFile);

				// / adf
				if (azaanWavFileName != null
						&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
						&& null != azaanCategoryId) {
					logger.info("Updating songName: " + songName
							+ ", for azaan category model");
					Clip[] clips = rbtConnector.getMemCache().getAllClips(cat);

					logger.info("Updating songName: "
							+ songName
							+ ", for azaan category model, clips in categoryId: "
							+ cat + ", clips len: " + clips.length);
					if (clips.length > 0) {
						logger.info("Updated songName: " + songName);
						clipTemp = clips[0];
					}
				}
				
				if ("RADIO".equalsIgnoreCase(wavFile)) {
					Clip[] clips = rbtConnector.getMemCache().getAllClips(cat);
					int catLen = (null != clips) ? clips.length : 0; 
					logger.info("Getting clips of RADIO category. categoryId: " + cat
							+ ", no of clips: " + catLen);

					if (catLen > 0) {
						clipTemp = clips[0];
					}
				}
				logger.info("Making selection for clip. clipTemp: " + clipTemp
						+ ", caller: " + caller);

				HashMap<String, String> currentSubscriberExtraInfo = null;
				if (isSubActive(subscriber))
					currentSubscriberExtraInfo = subscriber.getUserInfoMap();
				boolean isCallerBuddyNetUser = false;
				boolean isCalledBuddyNetUser = false;
				int buddySelCount = 0;
				int buddynetFreeSelLimit = getParamAsInt(
						"BUDDYNET_FREE_SEL_COUNT", 0);
				if (currentSubscriberExtraInfo == null)
					currentSubscriberExtraInfo = new HashMap<String, String>();
				if (clipTemp != null || status == 90) {
					if (subscriber != null
							&& subscriber.getStatus().equalsIgnoreCase(
									WebServiceConstants.LOCKED)) {
						logger.info("subscriber is locked");
						response = "COPY_SELECTION_USER_LOCKED";
					} else {
						if (vst.selectedBy() == null
								|| vst.selectedBy().contains("PRESSSTAR")) {
							if (m_redirectBuddyNet) {
								if (subscriber == null)//RBT-14671 - # like
									subscriber = m_rbtCopyLikeUtils.getSubscriber(caller);
								Subscriber calledSubscriber = m_rbtCopyLikeUtils.getSubscriber(called);
								logger.info("caller-->subscriber-->"
										+ subscriber.toString());
								isCallerBuddyNetUser = isBuddyNetUsers(caller,
										subscriber);
								logger.info("called-->subscriber-->"
										+ calledSubscriber.toString());
								isCalledBuddyNetUser = isBuddyNetUsers(called,
										calledSubscriber);
								if (isCallerBuddyNetUser
										&& isCalledBuddyNetUser) {
									logger.info("Both caller and called are buddynet users.");
									logger.info("Subscriber extra Info is "
											+ currentSubscriberExtraInfo);
									if (buddynetFreeSelLimit > 0) {
										if (currentSubscriberExtraInfo != null
												&& currentSubscriberExtraInfo
														.containsKey("BUDDY_SEL"))
											buddySelCount = getIntegerValue(currentSubscriberExtraInfo
													.get("BUDDY_SEL"));
										buddySelCount++;
										currentSubscriberExtraInfo
												.put("BUDDY_SEL", ""
														+ buddySelCount);
										if (buddySelCount <= buddynetFreeSelLimit) {
											String classTypeTemp = getParamAsString("PRESSSTAR_BUDDY_NET_CHARGE_CLASS");
											if (classTypeTemp != null
													&& !classTypeTemp
															.equalsIgnoreCase("null")
													&& !classTypeTemp
															.equalsIgnoreCase("")) {
												classType = classTypeTemp;
											}
										}
									}
								}
							}
						}

						String virtualNumberConfig = getParamAsString(
								"VIRTUAL_NUMBERS", vst.subID(), null);
						if (virtualNumberConfig != null) {
							useUIChargeClass = false;
							String circleID = null;
							String subClassStr = null;
							String chargeclass = null;
							String[] tokens = virtualNumberConfig.split(","); // value
							isVirtualNoTypeTwo = true; // :
							// wavFile,SubscriptionClass,circleId

							if (tokens.length >= 2)
								subClassStr = tokens[1];
							if (tokens.length >= 3)
								circleID = tokens[2];
							if (tokens.length >= 4)
								chargeclass = tokens[3];

							if (subscriber.getCircleID() != null
									&& circleID != null
									&& subscriber.getCircleID()
											.equalsIgnoreCase(circleID)) {
								// subscriber called a virtual number and
								// belongs to the same circle. hence
								// overriding the charge class
								if (!isSubActive(subscriber)) {
									subClass = subClassStr;
								}
								if (chargeclass != null) {
									String[] chargeClassSplit = chargeclass
											.split(":");
									String activeChargeClass = null;
									String inactiveChargeClass = null;

									if (chargeClassSplit.length > 1) {

										activeChargeClass = chargeClassSplit[0];
										inactiveChargeClass = chargeClassSplit[1];

									} else if (chargeClassSplit.length == 1) {

										activeChargeClass = chargeClassSplit[0];

									}

									// RBT-5338 virtual number copy is
									// overriding cosid feature
									boolean isOverrideCosFeature = getParamAsBoolean(
											"GATHERER",
											"OVERRIDE_VIRTUALNUMBER_CHARGE_ON_COS",
											"TRUE");
									if (!isOverrideCosFeature) {
										CosDetails cos = CacheManagerUtil
												.getCosDetailsCacheManager()
												.getCosDetail(
														subscriber.getCosID());
										if (cos != null) {
											boolean isDefault = cos
													.isDefaultCos();
											if (!isDefault) {
												useUIChargeClass = false;
												classType = cos
														.getFreechargeClass();
												selAmt = getSelectionAmount(
														caller,
														subscriber
																.getCircleID(),
														finalSelectedBy,
														classType, category,
														clip, useUIChargeClass,
														status);
												activeChargeClass = null;
												inactiveChargeClass = null;
											}
										}
									}

									if (Utility.isSubActive(subscriber)
											&& activeChargeClass != null
											&& !activeChargeClass
													.equalsIgnoreCase("")) {

										useUIChargeClass = true;
										classType = activeChargeClass;
										selAmt = getSelectionAmount(caller,
												subscriber.getCircleID(),
												finalSelectedBy, classType,
												category, clip,
												useUIChargeClass, status);
									} else if (!Utility.isSubActive(subscriber)
											&& inactiveChargeClass != null
											&& !inactiveChargeClass
													.equalsIgnoreCase("")) {

										useUIChargeClass = true;
										classType = inactiveChargeClass;
										selAmt = getSelectionAmount(caller,
												subscriber.getCircleID(),
												finalSelectedBy, classType,
												category, clip,
												useUIChargeClass, status);
									}

									logger.info("The called no is virtual no & use ui is "
											+ useUIChargeClass
											+ " and class type = " + classType);

								}
							}
						}

						if (!isSubActive(subscriber)) {
							isCallerSubscribed = false;
							isActivated = true;

							if (selectedBy == null
									|| selectedBy.contains("PRESSSTAR")
									|| selectedBy.contains("XCOPY")) {
								currentSubscriberExtraInfo.put(REFUND, "TRUE");
							}
							if (vst.type() != null
									&& vst.type().equalsIgnoreCase(
											COPYCONFIRMED)) {
								currentSubscriberExtraInfo.put(
										EXTRA_INFO_COPY_TYPE,
										EXTRA_INFO_COPY_TYPE_OPTIN);
							}
							if (confMode != null
									&& !confMode.equalsIgnoreCase("-")) {
								currentSubscriberExtraInfo.put(
										EXTRA_INFO_COPY_MODE, confMode);

							}
						}
						writeCopyStats("Calling addSelections for "
								+ caller
								+ ". Before call time "
								+ statsDateFormat.format(Calendar.getInstance()
										.getTime()));

						finalSelectedBy = getP2PModeForUser(subscriber, finalSelectedBy, vst.type(),sub);
						
						// RBT Like feature
						if (SmsKeywordsStore.likeKeywordsSet
								.contains(keyPressed)) {
							logger.info("Checking configuration"
									+ " for Like feature. keyPressed: "
									+ keyPressed);

							subClass = getParamAsString("GATHERER",
									Constants.RBT_LIKE_SUB_CLASS, subClass);
							classType = getParamAsString("GATHERER",
									Constants.RBT_LIKE_CHARGE_CLASS, classType);
							finalSelectedBy = getParamAsString("GATHERER",
									Constants.RBT_LIKE_MODE, "RBT_LIKE");
							logger.info("Configured subClass: " + subClass
									+ ", chargeClass: " + classType
									+ ", mode: " + finalSelectedBy);
						}
						if (isVirtualNo || isVirtualNoTypeTwo) {
							String virtualNumberCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE
									+ "_" + called);
							if (virtualNumberCopyMode == null)
								virtualNumberCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE);
							if (virtualNumberCopyMode != null)
								finalSelectedBy = virtualNumberCopyMode;
							
							if(callerSub.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)){
								String virtualNumberUserCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER
										+ "_" + called);
								if (virtualNumberUserCopyMode == null)
									virtualNumberUserCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER);
								if (virtualNumberUserCopyMode != null && isSubActive(callerSub))
									finalSelectedBy = virtualNumberUserCopyMode;
							}
						}
						
						logger.debug("Checking key pressed is pick of the day. keyPressed: "
								+ keyPressed
								+ ", pickOfTheDayKeysList: "
								+ pickOfTheDayKeysList
								+ "Selected By: "
								+ finalSelectedBy
								+ ", subscriber status: "
								+ subscriber.getStatus());
						
						
						//GetOffer Tef-Spain RBT New Re-pricing RBT-11113
						boolean isAllowBaseSelOffer = getParamAsBoolean("ALLOW_GET_OFFER", "FALSE");
						boolean isAllowOnlyBaseOffer = getParamAsBoolean("ALLOW_ONLY_BASE_OFFER", "FALSE");
						String subOfferId = null;
						String selOfferId = null;
						if(!isSubActive(callerSub) && (isAllowBaseSelOffer || isAllowOnlyBaseOffer)) {
							Offer offer = getOffer(caller, "sub");
							if(offer != null) {
								subClass = offer.getSrvKey();
								offerSubClass = subClass;
								subOfferId = offer.getOfferID();
							}
						}
						
						if(isAllowBaseSelOffer) {
							Offer offer = getOffer(caller, "sel");
							if(offer != null) {
								classType = offer.getSrvKey();
								selOfferId = offer.getOfferID();
								useUIChargeClass = true;
								if (offer.getAmount()==0){
									renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(offer.getOfferRenewalValidity());
									freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
											"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
									freePeriodText = freePeriodText.replace("DD",renewalPeriod);
									renewalAmount = offer.getOfferRenewalAmount();
									selAmt = String.valueOf(offer.getAmount());
								}
							}
						}
						// RBT-13870- POD is not getting activated when user
						// press multiple DTMF keys for POD
						boolean isPickOfTheDay = false;
						if (null != pickOfTheDayKeys
								&& !pickOfTheDayKeysList.isEmpty()) {
							for (String pickOftheDaykey : pickOfTheDayKeysList) {
								if (keyPressed.indexOf(pickOftheDaykey) != -1) {
									isPickOfTheDay = true;
									break;
								}
							}
							logger.info("isPickOfTheDay: " + isPickOfTheDay);
						}

						if(sub !=  null & freemiumSubClassList.contains(sub.getSubscriptionClass())) {
							String temp =  getParamAsString(finalSelectedBy.toUpperCase() + "_" + sub.getSubscriptionClass().toUpperCase()+"_ACTIVATE_MODE_COPY_FROM_FREEMIUM_USER");
							finalSelectedBy = temp != null? temp : finalSelectedBy;
						}
						
						
						boolean isCDTNDTUser =  Utility.isUserCDTNDT(subscriber.getCosID());		
						boolean isActiveUDSUser = false;
						if (isSubActive(callerSub)) {
							finalSelectedBy = RBTParametersUtils
									.getParamAsString("GATHERER",
											"CG_ACTIVE_MODE_CONFIGURED",
											finalSelectedBy);
							
							String isUdsUserOptInTrue = com.onmobile.apps.ringbacktones.webservice.common.Utility.isUDSUser(subscriber.getUserInfoMap(), false);
							if(isUdsUserOptInTrue!=null){
								isActiveUDSUser = true;
							}
							
							if (isCDTNDTUser && !isActiveUDSUser
									&& vst.type() != null && "COPY".equalsIgnoreCase(vst.type())) {
								finalSelectedBy = RBTParametersUtils
										.getParamAsString(
												"GATHERER",
												"CDT_NDT_USERS_NORMALCOPY_MODE",
												finalSelectedBy);
							}

						} else {
							finalSelectedBy = RBTParametersUtils
									.getParamAsString("GATHERER",
											"CG_INACTIVE_MODE_CONFIGURED",
											finalSelectedBy);
						}
												
						if (isPickOfTheDay) {
							cat = 26;
							Clip pickOfTheDayClip = getPickOfTheDayClip();
							if (null != pickOfTheDayClip) {
								clipTemp = pickOfTheDayClip;
								logger.info("Making pick of the selection."
										+ ", clip: " + clipTemp);
								//RBT-17850 Pilot for Copy Callertunes
								String circleModeMapping = RBTParametersUtils.getParamAsString("GATHERER","COPY_MODE_MAPPING_BY_CIRCLE", null);
								if(circleModeMapping!=null){
									Map<String,String> circleModeMap = MapUtils.convertIntoMap(circleModeMapping, ",", ":", null);
									if(circleModeMap!=null && caller!=null){
										Subscriber callerSubObj = Utility.getSubscriber(caller);
										if(callerSubObj!=null){
											String mappedCircleMode = circleModeMap.get(callerSubObj.getCircleID());
											if(mappedCircleMode!=null){
												finalSelectedBy = mappedCircleMode;
											}
										}
									}
								}
								response = addSelections(caller, setForCaller,
										prepaid, cat, clipTemp,
										finalSelectedBy, selectedBy, classType,
										subClass, vst.selectedBy(), vst.type(),
										isCallerSubscribed,
										subscriber.getUserType(),
										subscriber.getCosID(), status,
										currentSubscriberExtraInfo, confMode,
										useUIChargeClass, subOfferId, selOfferId);
								writeCopyStats("Calling addSelections for "
										+ caller
										+ ". After call time "
										+ statsDateFormat.format(Calendar
												.getInstance().getTime()));
							} else {
								logger.error("No pick of the clip is found. Not "
										+ " making selection");
								response = PICK_OF_THE_DAY_NOT_FOUND;
							}
						} else {
							
							//For vodafone turkey
							if(getParamAsBoolean("DCT_ALL_CALLER_SEL_FOR_DEFAULT_COPY", "FALSE") && defaultClipWavName != null 
									&& defaultClipWavName.equalsIgnoreCase(wavFile)) {
								StringBuffer strBuff = new StringBuffer();
								SelectionRequestBean selBean = new SelectionRequestBean();
								selBean.setSubscriberId(caller);
								selBean.setStatus(1);
								selBean.setFromTimeOfTheDay(0);
								selBean.setToTimeOfTheDay(23);
								rbtConnector.getSubscriberRbtclient().deleteSelections(
										selBean, "GATHERER", "DAEMON", strBuff);
								
								String responseTemp = "FAILURE";
								if (strBuff != null && strBuff.length() > 0) {
									responseTemp = strBuff.toString();
									if (responseTemp != null) {
										responseTemp = responseTemp.trim();
										responseTemp = responseTemp.toUpperCase();
									}
								}
								response = responseTemp;								
								logger.info("User copy default song, response of deactivate all caller settings : " + response + ". strSubID: " + caller
										+ ", responseTemp: " + responseTemp);								
							}
							else {
								//RBT-17850 Pilot for Copy Callertunes
								String circleModeMapping = RBTParametersUtils.getParamAsString("GATHERER","COPY_MODE_MAPPING_BY_CIRCLE", null);
								if(circleModeMapping!=null){
									Map<String,String> circleModeMap = MapUtils.convertIntoMap(circleModeMapping, ",", ":", null);
									if(circleModeMap!=null && caller!=null){
										Subscriber callerSubObj = Utility.getSubscriber(caller);
										if(callerSubObj!=null){
											String mappedCircleMode = circleModeMap.get(callerSubObj.getCircleID());
											if(mappedCircleMode!=null){
												finalSelectedBy = mappedCircleMode;
											}
										}
									}
								}
								response = addSelections(caller, setForCaller, prepaid,
									cat, clipTemp, finalSelectedBy, selectedBy,
									classType, subClass, vst.selectedBy(),
									vst.type(), isCallerSubscribed,
									subscriber.getUserType(),
									subscriber.getCosID(), status,
									currentSubscriberExtraInfo, confMode,
									useUIChargeClass, subOfferId, selOfferId);
							}
							writeCopyStats("Calling addSelections for "
									+ caller
									+ ". After call time "
									+ statsDateFormat.format(Calendar.getInstance()
											.getTime()));
						}

					}

					if (response.indexOf("SUCCESS") != -1) {
						isCopyDone = true;
						logger.debug("Successfully added selection for caller: "
								+ caller);
						if (isSubActive(subscriber) && isCallerBuddyNetUser
								&& isCalledBuddyNetUser
								&& buddySelCount <= buddynetFreeSelLimit) {
							updateSubscription(subscriber.getSubscriberID(),
									buddySelCount);
						}
					} else {
						logger.info("Failed to add selections. response from webservice is: "
								+ response + ", caller: " + caller);
						if (!isSubActive(subscriber))
							isActivated = false;

					}
				}// Jira :RBT-15026: Changes done for allowing the multiple
				// Azaan pack.
			} else if (subscriber == null
					|| !isSubActive(subscriber)
					|| (azaanWavFileName != null
							&& (azaanWavFileName.equalsIgnoreCase(wavFile)) && rbtDBManager.azaanDefaultCosId != null)
					|| (azaanWavFileName != null && wavFile
							.startsWith(azaanWavFileName))) {
				logger.info("subscriber is not active");
				isCallerSubscribed = false;
				String actBy = "COPY";
				String cosId = null;
				// Jira :RBT-15026: Changes done for allowing the multiple
				// Azaan pack.
				String azaanCopDuaContentName = waveFileSubType;
				if (azaanWavFileName != null
						&& (azaanWavFileName.equalsIgnoreCase(wavFile) || wavFile.startsWith(azaanWavFileName))
						&& rbtDBManager.azaanDefaultCosId != null) {
					cosId = rbtDBManager.azaanDefaultCosId; // user tried to
															// copy azaan,
															// activating him on
															// azaan pack
				}else if (azaanCopDuaContentName != null 
						&& confAzaanCopticDoaaCosIdSubTypeMap.get(azaanCopDuaContentName) != null) {
					cosId = confAzaanCopticDoaaCosIdSubTypeMap.get(azaanCopDuaContentName);
				}
				// Jira :RBT-15026: Changes done for allowing the multiple
				// Azaan pack.
				String[] azaanWavCosID = wavFile.split("_");
				String azaanPackCosId = null;
				if (azaanWavCosID.length > 1 && wavFile.startsWith(azaanWavFileName)) {
					azaanPackCosId = azaanWavCosID[1];
					CosDetails cos = CacheManagerUtil
							.getCosDetailsCacheManager().getActiveCosDetail(
									azaanPackCosId, subscriber.getCircleID());
					if (null != cos)
						cosId = cos.getCosId();
				}
				if (finalSelectedBy != null
						&& finalSelectedBy.indexOf("_XCOPY") != -1)
					actBy = finalSelectedBy;
				if (getParamAsBoolean("USE_DEFAULT_ACT_SEL_BY", "TRUE"))
					actBy = "COPY";
				String modeOptInCopy = getParamAsString("MODE_FOR_OPTIN_COPY");
				if (modeOptInCopy != null) {
					if (vst.type().equalsIgnoreCase(COPYCONFIRMED))
						actBy = modeOptInCopy;
				}
				
				actBy = getP2PModeForUser(subscriber, actBy, vst.type(),sub);
				
				logger.info("the value of actBy " + actBy);
				boolean prepaid1 = getParamAsString("GATHERER",
						"DEFAULT_SUBTYPE", "pre").equalsIgnoreCase("pre");

				// To support Lite feature in copy. To pass cosId in the
				// activation request.
				Clip clipTemp = rbtConnector.getMemCache()
						.getClipByRbtWavFileName(wavFile);
				if (clipTemp != null
						&& clipTemp.getContentType().equalsIgnoreCase(
								WebServiceConstants.COS_TYPE_LITE)) {
					String circleID = null;

					MNPContext mnpContext = new MNPContext(caller, "COPY");
					mnpContext.setOnlineDip(getParamAsBoolean(
							"COPY_ONLINE_DIP", "FALSE"));
					SubscriberDetail subscriberDetail = RbtServicesMgr
							.getSubscriberDetail(mnpContext);
					if (subscriberDetail != null)
						circleID = subscriberDetail.getCircleID();
					List<CosDetails> cos = CacheManagerUtil
							.getCosDetailsCacheManager()
							.getCosDetailsByCosType(
									WebServiceConstants.COS_TYPE_LITE,
									circleID, prepaid1 ? "y" : "n");
					if (cos != null && cos.size() > 0)
						cosId = cos.get(0).getCosId();
				}
				// RBT Like feature
				if (SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {
					logger.info("Checking configuration"
							+ " for Like feature. keyPressed: " + keyPressed);
					subClass = getParamAsString("GATHERER",
							Constants.RBT_LIKE_SUB_CLASS, subClass);
					classType = getParamAsString("GATHERER",
							Constants.RBT_LIKE_CHARGE_CLASS, classType);
					actBy = getParamAsString("GATHERER",
							Constants.RBT_LIKE_MODE, "RBT_LIKE");
					logger.info("Configured subClass: " + subClass
							+ ", chargeClass: " + classType + ", mode: "
							+ actBy);
				}
				if (isVirtualNo || isVirtualNoTypeTwo) {
					String virtualNumberCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE
							+ "_" + called);
					if (virtualNumberCopyMode == null)
						virtualNumberCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE);
					if (virtualNumberCopyMode != null)
						actBy = virtualNumberCopyMode;
					
					if(isSubActive(callerSub)){
						String virtualNumberUserCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER
								+ "_" + called);
						if (virtualNumberUserCopyMode == null)
							virtualNumberUserCopyMode = getParamAsString(VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER);
						if (virtualNumberUserCopyMode != null)
							actBy = virtualNumberUserCopyMode;
					}
				}

				if (baseModeMap.containsKey(actBy))
					actBy = baseModeMap.get(actBy);

				logger.debug("Activated By: "
						+ actBy
						+ ", subscriber status: "
						+ subscriber.getStatus());

				writeCopyStats("Calling activateSubscriber for "
						+ caller
						+ ". Before call time "
						+ statsDateFormat.format(Calendar.getInstance()
								.getTime()));
				
				//GetOffer Tef-Spain RBT New Re-pricing
				boolean isAllowBaseSelOffer = getParamAsBoolean("ALLOW_GET_OFFER", "FALSE");
				boolean isAllowOnlyBaseOffer = getParamAsBoolean("ALLOW_ONLY_BASE_OFFER", "FALSE");
				String subOfferId = null;
				if(isAllowBaseSelOffer || isAllowOnlyBaseOffer) {
					Offer offer = getOffer(caller, "sub");
					if(offer != null) {
						subClass = offer.getSrvKey();
						offerSubClass = subClass;
						subOfferId = offer.getOfferID();
					}
				}
				
				if(sub !=  null & freemiumSubClassList.contains(sub.getSubscriptionClass())) {
					String temp =  getParamAsString(actBy.toUpperCase()+"_" + sub.getSubscriptionClass().toUpperCase()+"_ACTIVATE_MODE_COPY_FROM_FREEMIUM_USER");
					actBy = temp != null? temp : actBy;
				}
				StringBuffer strBuff = new StringBuffer();
				//RBT-17850 Pilot for Copy Callertunes
				String circleModeMapping = RBTParametersUtils.getParamAsString("GATHERER","COPY_MODE_MAPPING_BY_CIRCLE", null);
				if(circleModeMapping!=null){
					Map<String,String> circleModeMap = MapUtils.convertIntoMap(circleModeMapping, ",", ":", null);
					if(circleModeMap!=null && callerCircleID!=null){
						String mappedCircleMode = circleModeMap.get(callerCircleID);
						if(mappedCircleMode!=null){
							selectedBy = mappedCircleMode;
						}
					}
				}
				subscriber = activateSubscriber(caller, callerCircleID, called,
						actBy, prepaid1, subClass, selectedBy,
						vst.selectedBy(), cosId, vst.type(), confMode, subOfferId, strBuff);
				writeCopyStats("Calling activateSubscriber for "
						+ caller
						+ ". After call time "
						+ statsDateFormat.format(Calendar.getInstance()
								.getTime()));
				String language = subscriber != null ? subscriber.getLanguage()	: null;
				handleBaseOfferNotFoundAndDwnldLimitReached(strBuff, callerSub, language);

				if (subscriber == null) {
					logger.info("subscriber is still null");
					copyFailed(vst, "NA", keyPressed, confMode);
					return "NA";
				}
				isActivated = true;
			} else
				logger.info("addselections failed");
		}

		if (isProcess) {

			String subTypeRegion = m_localType;
			if (selectedBy.contains("VN")) {
				subTypeRegion = m_virtualType;
			}
			if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
				if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
					// If selection is already exist, put the status as
					// DUPLICATE
					if (response != null && response.equals("ALREADY_EXISTS")) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", DUPLICATE,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(), "D",
									keyPressed, DUPLICATE, wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}
					} else if (response != null
							&& (response
									.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED) 
							|| response
							.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", PREMIUM_CONTENT,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed, PREMIUM_CONTENT,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
									.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", "COS_MISMATCH",
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed, "COS_MISMATCH",
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
									.equalsIgnoreCase(WebServiceConstants.OFFER_NOT_FOUND)) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", OFFER_NOT_FOUND,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed, OFFER_NOT_FOUND,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
									.equalsIgnoreCase(WebServiceConstants.PICK_OF_THE_DAY_NOT_FOUND)) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", PICK_OF_THE_DAY_NOT_FOUND,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed, PICK_OF_THE_DAY_NOT_FOUND,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
									.equalsIgnoreCase(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE",
								REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed,
									REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
									.equalsIgnoreCase(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION
											.toUpperCase())) {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE",
								RBT_CORPORATE_NOTALLOW_SELECTION,
								keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyPressed,
									RBT_CORPORATE_NOTALLOW_SELECTION,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					} else if (response != null
							&& response
							.equalsIgnoreCase(WebServiceConstants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE
									.toUpperCase())) {
				writeTrans(vst.subID(), vst.callerID(), wavFile,
						catNameBuf.toString(), Tools.getFormattedDate(
								vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
						subTypeRegion, isActivated ? "YES" : "NO",
						isCopyDone ? "TRUE" : "FALSE",
						Constants.SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE,
						keyPressed, copyType, confMode); /*RBT-18975*/
				try {
					eventLogger.copyTrans(vst.subID(), vst.callerID(),
							isActivated ? "YES" : "NO", subTypeRegion,
							catNameBuf.toString(), isCopyDone ? "TRUE"
									: "FALSE", vst.sentTime(),
							copyType, keyPressed,
							Constants.SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE,
							wavFile, confMode,
							getCalleeOperator(sub, vst.selectedBy()),
							new Date());

				} catch (ReportingException e) {
					logger.info("Caught an exception while writing event logs");
					logger.error("", e);
				}

			} else if (response != null
							&& response.equalsIgnoreCase(DOWNLOAD_MONTHLY_LIMIT_REACHED)) {
						writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate(
								vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", DOWNLOAD_MONTHLY_LIMIT_REACHED, keyPressed, copyType, confMode);
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(), isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE" : "FALSE", vst.sentTime(), copyType, keyPressed,
											DOWNLOAD_MONTHLY_LIMIT_REACHED, wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()), new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}

					}else {
						writeTrans(vst.subID(), vst.callerID(), wavFile,
								catNameBuf.toString(), Tools.getFormattedDate(
										vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
								subTypeRegion, isActivated ? "YES" : "NO",
								isCopyDone ? "TRUE" : "FALSE", COPIED,
								keyPressed, copyType, confMode);
						try {
							StringBuffer keyBuffer = new StringBuffer(keyPressed);
							// Write event logs once the copy process is
							// success.
							if (SmsKeywordsStore.likeKeywordsSet
									.contains(keyPressed)) {
								keyBuffer.insert(0, "l");
							}

							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									isActivated ? "YES" : "NO", subTypeRegion,
									catNameBuf.toString(), isCopyDone ? "TRUE"
											: "FALSE", vst.sentTime(),
									copyType, keyBuffer.toString(), COPIED, wavFile,
									confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());

						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}
					}
				}
				removeCopyViralPromotion(vst.subID(), vst.callerID(),
						vst.sentTime());
			} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
				removeCopyViralPromotion(vst.subID(), vst.callerID(),
						vst.sentTime());
				// If selection is already exist, put the status as DUPLICATE
				if (response != null && response.equals("ALREADY_EXISTS")) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", DUPLICATE,
							keyPressed, copyType, confMode);
				} else if (response != null
						&& (response
								.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)
								|| response
								.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", PREMIUM_CONTENT,
							keyPressed, copyType, confMode);
				} else if (response != null
						&& response
								.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", "COS_MISMATCH",
							keyPressed, copyType, confMode);

				} else if (response != null
						&& response
								.equalsIgnoreCase(WebServiceConstants.OFFER_NOT_FOUND)) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", OFFER_NOT_FOUND,
							keyPressed, copyType, confMode);
				} else if (response != null
						&& response
								.equalsIgnoreCase(WebServiceConstants.PICK_OF_THE_DAY_NOT_FOUND)) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", PICK_OF_THE_DAY_NOT_FOUND,
							keyPressed, copyType, confMode);
				} else if (response != null
						&& response
								.equalsIgnoreCase(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE",
							REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED,
							keyPressed, copyType, confMode);
				} else if (response != null
						&& response
						.equalsIgnoreCase(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION
								.toUpperCase())) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
					catNameBuf.toString(), Tools.getFormattedDate(
							vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
					subTypeRegion, isActivated ? "YES" : "NO",
					isCopyDone ? "TRUE" : "FALSE",
					RBT_CORPORATE_NOTALLOW_SELECTION,
					keyPressed, copyType, confMode);
				} else if (response != null
						&& response
								.equalsIgnoreCase(WebServiceConstants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE
										.toUpperCase())) {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE",
							Constants.SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE, keyPressed,
							copyType, confMode);/*RBT-18975*/
				} else if (response != null
						&& response.equalsIgnoreCase(DOWNLOAD_MONTHLY_LIMIT_REACHED)) {
					writeTrans(vst.subID(), vst.callerID(), wavFile, catNameBuf.toString(), Tools.getFormattedDate(
							vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", DOWNLOAD_MONTHLY_LIMIT_REACHED, keyPressed, copyType, confMode);
				}else {
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							subTypeRegion, isActivated ? "YES" : "NO",
							isCopyDone ? "TRUE" : "FALSE", COPIED, keyPressed,
							copyType, confMode);
				}
			} else {
				// If selection is already exist, put the status as DUPLICATE
				if (response != null && response.equals("ALREADY_EXISTS")) {
					updateCopyViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), DUPLICATE, null);
				} else if (response != null
						&& (response
								.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)
								|| response
								.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))) {
					updateCopyViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), PREMIUM_CONTENT, null);
				} else if (response != null
						&& response
								.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)) {
					updateCopyViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), "CONTENT_MISMATCH", null);
				} else {
					updateCopyViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), COPIED, null);
				}
			}
		}

		if (songName == null && isPollRBTCopy)
			songName = "RBT POLL";
		else if (songName == null)
			songName = "Default Tune";
		boolean sensPressStarSMS = false;
		String sms = null;
		String language = subscriber.getLanguage();
		if (isProcess) {
			logger.info("To send SMS, checking addselection response: "
					+ response + ", isActivated: " + isActivated);
			if (crossOperatorName != null)
				crossOperatorName = crossOperatorName + " No.";
			String strSmsText = null;
			// get the different status

			boolean isAnyKeyCopyOptIn = false; 
			if (isStarcopy && copyType.equals(OPTINCOPY)) {
				isAnyKeyCopyOptIn = true;
			}
			if (isActivated) {	
				if(offerSubClass != null) {
					strSmsText = getSuccessSMSText("GATHERER", "COPY_ACT_SMS_" + offerSubClass.toUpperCase(),
							null, language, isAnyKeyCopyOptIn);
				}
				
				if(strSmsText == null) {
					strSmsText = getSuccessSMSText("GATHERER", "COPY_ACT_SMS",
						m_copyActSMS, language, isAnyKeyCopyOptIn);
				}//RBT-14671 - # like
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called,
						crossOperatorName, actAmt, selAmt, clip, category, null,renewalAmount,freePeriodText);
			} else if (response != null
					&& response.equals("SUCCESS_DOWNLOAD_EXISTS")) {
				strSmsText = getSuccessSMSText("GATHERER",
						"SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT", null,
						language, isAnyKeyCopyOptIn);//RBT-14671 - # like
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (isCopyDone) {
				
				if(classType != null) {
					strSmsText = getSuccessSMSText("GATHERER", "COPY_SEL_SMS_" + classType.toUpperCase(),
							null, language, isAnyKeyCopyOptIn);
					ChargeClass chargeClass = rbtConnector.getRbtGenericCache().getChargeClass(classType);					
					if (chargeClass != null) {
						if (null != chargeClass.getAmount()
								&& Double.parseDouble(selAmt.replace(specialAmtChar,".")) == 0) {
							renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility
									.getSubscriptionPeriodInDays(chargeClass
											.getSelectionPeriod());
							freePeriodText = CacheManagerUtil
									.getParametersCacheManager()
									.getParameterValue(iRBTConstant.COMMON,
											"FREE_SMS_PERIOD_TEXT",
											"(DD dias GRATIS)");
							freePeriodText = freePeriodText.replace("DD",
									renewalPeriod);
							renewalAmount = chargeClass.getRenewalAmount();
						}
					}
				}
				
				if(strSmsText == null) {
					strSmsText = getSuccessSMSText("GATHERER", "COPY_SEL_SMS",
						m_copySelSMS, language, isAnyKeyCopyOptIn);
				}//RBT-14671 - # like
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called,
						crossOperatorName, actAmt, selAmt, clip, category, null,renewalAmount,freePeriodText);
			} else if (response != null
					&& response.equals("SELECTION_SUSPENDED")) {
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"COPY_SELECTION_SUSPENDED_TEXT", null, language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null
					&& response
							.contains(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)) {
				String cosContent = response
						.substring(response
								.indexOf(WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED)
								+ WebServiceConstants.COPY_COS_MISMATCH_CONTENT_BLOCKED
										.length());
				logger.info("Getting sms for COPY_COS_MISMATCH,CONTENT_BLOCKED_"
						+ cosContent.toUpperCase());
				strSmsText =RBTCopyLikeUtils.getSMSText(
						"COPY_COS_MISMATCH",
						"CONTENT_BLOCKED_" + cosContent.toUpperCase(),
						RBTCopyLikeUtils.getSMSText("LITE_USER", "PREMIUM_BLOCKED", null,
								language), language);

				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null
					&& (response
							.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)
							|| response
							.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))) {
				String selectionAmount = null;
				SelectionRequest selectionRequest= new SelectionRequest(subscriber.getSubscriberID());
				if (clip != null) {
					selectionRequest.setClipID("" + clip.getClipId());
				}
				if (category != null) {
					selectionRequest.setCategoryID("" + category.getCategoryId());
				}
				
				com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequest);
				if (chargeClass != null) {
					selectionAmount = chargeClass.getAmount();
				}//RBT-14671 - # like
				if (response
						.equalsIgnoreCase(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
					strSmsText = RBTCopyLikeUtils.getSMSText(GATHERER,
							"LITEUSER_PREMIUM_NOT_PROCESSED",
							"You are not authorised to access this content",
							language);
				} else {
					strSmsText = RBTCopyLikeUtils.getSMSText("LITE_USER", "PREMIUM_BLOCKED", null,
							language);
				}
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, selectionAmount, clip, category, null,renewalAmount,freePeriodText);
			} else if (response != null
					&& response
							.equalsIgnoreCase(WebServiceConstants.OFFER_NOT_FOUND)) {
				strSmsText = RBTCopyLikeUtils.getSMSText("OFFER_NOT_FOUND", null, null, language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null
					&& response
							.equalsIgnoreCase(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED", null,
						language);
				strSmsText = (strSmsText != null) ? strSmsText : RBTCopyLikeUtils.getSMSText(
						"GATHERER", "COPY_TECHNICAL_FAILURE_TEXT", null,
						language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null && response.equals("ALREADY_EXISTS")) {
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER", "COPY_SAME_SEL_SMS", null,
						language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null && response.indexOf("OVERLIMIT") != -1) {
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"COPY_SELECTION_OVERLIMIT_TEXT", null, language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null
					&& response.equalsIgnoreCase("COPY_SELECTION_USER_LOCKED")) {
				strSmsText = RBTCopyLikeUtils.getSMSText("LOCK_COPY", "FAILURE", null, language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			} else if (response != null
					&& response.toUpperCase().startsWith("SELECTIONS_BLOCKED")) {
				strSmsText = RBTCopyLikeUtils.getSMSText("SEL_BLOCKED_FOR_"
						+ subscriber.getStatus().toUpperCase(), null, null,
						language);
				sms = strSmsText;
			}else if (response != null
					&& response.toUpperCase().equalsIgnoreCase(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
				strSmsText = RBTCopyLikeUtils.getSMSText("CORPORATE_SONG_CHANGE_NOT_ALLOWED", null, null,language);
				sms = strSmsText;
			}else if(response != null
					&& response.toUpperCase().equalsIgnoreCase(DOWNLOAD_MONTHLY_LIMIT_REACHED)){
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						DOWNLOAD_MONTHLY_LIMIT_REACHED, "DOWNLOAD_MONTHLY_LIMIT_REACHED", language);				
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
				
			} else if (response != null
					&& response.toUpperCase().equalsIgnoreCase(WebServiceConstants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE)) {
				/*RBT-18975*/
				strSmsText = RBTCopyLikeUtils.getSMSText(Constants.SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE, null, null,language);
				sms = strSmsText;
			} else {
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"COPY_TECHNICAL_FAILURE_TEXT", null, language);
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(strSmsText, songName, called, null,
						null, null, clip, category, null,null,null);
			}
		} else {
			logger.info("Processing copy type: " + vst.type());
			String circleId = (subscriber == null) ? null : subscriber
					.getCircleID();
			if (getParamAsBoolean("IS_STAR_OPT_IN_ALLOWED", "FALSE")
					&& vst.type().equals(COPYSTAR)) {
				
				String value = getParamAsString("GATHERER", "OPTIN_COPYSTAR_CONSIDER_INVALID_KEY_FOR_USERTYPE", null);
				
				if(value != null && ((value.equalsIgnoreCase("ALL")) 
						|| (value.equalsIgnoreCase("NEW") && !isSubActive(subscriber)) 
						|| (value.equalsIgnoreCase("ACTIVE") && isSubActive(subscriber)))) {
					copyFailed(vst, "INVALID_KEY_PRESSED", keyPressed, confMode);
					logger.info("Copy star consider invalid key for " + (subscriber != null ? subscriber.getStatus() : "NEW"));
					return "INVALID_KEY_PRESSED";
				}
				
				String virtualNumberConfig = getParamAsString(
						"VIRTUAL_NUMBERS", vst.subID(), null);
				logger.info("virtualNumberConfig is : " + virtualNumberConfig);
				String circleID = null;
				if (virtualNumberConfig != null) {
					String[] tokens = virtualNumberConfig.split(","); // value :
					// wavFile,SubscriptionClass,circleId

					if (tokens.length >= 3)
						circleID = tokens[2];

				}
				
				boolean isStarOptOutAllowedForNewUser = Boolean.parseBoolean(getParamAsString("GATHERER", "IS_STAR_OPT_OUT_ALLOWED_FOR_INACTIVE_USER", "FALSE"));
				if(isStarOptOutAllowedForNewUser && !isSubActive(subscriber) ) {
					//copy will be processed as dicrect copy, if new user or deactive user press *
					sms = null;
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPYCONFIRMED, null);
					logger.info("updated copy request to copyconfirmed bcoz of virtual number.");
				}
				else if ((virtualNumberConfig != null && circleID == null)
						|| (circleID != null && callerCircleID != null && circleID
								.equalsIgnoreCase(callerCircleID))) {
					// subscriber called a virtual number and belongs to the
					// same circle. hence copy will be processed as direct copy
					sms = null;
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPYCONFIRMED, null);
					logger.info("updated copy request to copyconfirmed bcoz of virtual number.");

				} else {
					sensPressStarSMS = true;
					boolean isActive = isSubActive(subscriber);
					boolean liteCondition = false;
					if (subscriber != null && isActive && clip != null) {
						String cosStr = subscriber.getCosID();
						String cosType = null;
						if (cosStr != null)
							cosType = CacheManagerUtil
									.getCosDetailsCacheManager()
									.getCosDetail(subscriber.getCosID())
									.getCosType();

						boolean isLite = (cosType != null
								&& cosType
										.equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE)
								&& clip.getContentType() != null && !clip
								.getContentType().equalsIgnoreCase(
										WebServiceConstants.COS_TYPE_LITE));

						boolean isUDS = false;
						if (subscriber.getUserInfoMap() != null) {
							//JIRA-ID: RBT-13626
							HashMap<String, String> userInfoMap = subscriber
									.getUserInfoMap();
							String premiumChargeClass = com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isUDSUser(userInfoMap, false);
							isUDS = (premiumChargeClass != null);							
							String blockedContentTypesStr = CacheManagerUtil
									.getParametersCacheManager()
									.getParameterValue(iRBTConstant.COMMON,
											"UDS_BLOCKED_CONTENT_TYPES", "");
							List<String> blockedContentTypesList = Arrays
									.asList(blockedContentTypesStr.split(","));
							isUDS = isUDS
									&& clip.getContentType() != null
									&& blockedContentTypesList.contains(clip
											.getContentType());
						}

						if (isLite || isUDS) {
							String cosidOrUdsType = null;
							if (isLite) {
								// RBT-14835 Blocking PPL content for specific
								// service
								cosidOrUdsType = (subscriber == null) ? null
										: subscriber.getCosID();
							} else if (isUDS) {
								// RBT-14835 Blocking PPL content for specific
								// service
								if (subscriber.getUserInfoMap() != null) {
									HashMap<String, String> userInfoMap = subscriber
											.getUserInfoMap();
									cosidOrUdsType = userInfoMap
											.get("UDS_OPTIN");
								}
							}
							boolean isBlockedPPLContent = validateSendSMSForPPLContent(
									vst, called, callerSub, keyPressed,
									confMode, clip, category, cosidOrUdsType,
									language);
							if (isBlockedPPLContent) {
								return "INVALIDCOPY";
							}
							if (RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
									"FALSE")) {
								DataRequest dataRequest = new DataRequest(
										caller, setForCaller, "SELCONFPENDING");
								HashMap<String, String> infoMap = new HashMap<String, String>();
								infoMap.put("CATEGORY_ID", String.valueOf(cat));
								dataRequest.setInfoMap(infoMap);
								if (!RBTParametersUtils
										.getParamAsBoolean(
												iRBTConstant.COMMON,
												"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
												"FALSE")) {
									RBTClient.getInstance().removeViralData(
											dataRequest);
								}

								String OptInCopyMode = getParamAsString("MODE_FOR_OPTIN_COPY");
								if (OptInCopyMode != null) {
									finalSelectedBy = OptInCopyMode;
								}

								dataRequest.setMode(finalSelectedBy);
								Clip clipTemp = rbtConnector.getMemCache()
										.getClipByRbtWavFileName(wavFile);
								if (clipTemp != null) {
									dataRequest.setClipID(String
											.valueOf(clipTemp.getClipId()));
								}
								RBTClient.getInstance().addViralData(
										dataRequest);
								
								SelectionRequest selectionRequest= new SelectionRequest(subscriber.getSubscriberID());
								if (clip != null) {
									selectionRequest.setClipID("" + clip.getClipId());
								}
								if (category != null) {
									selectionRequest.setCategoryID("" + category.getCategoryId());
								}
								
								com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequest);
								if (chargeClass != null) {
									selAmt = chargeClass.getAmount();
								}
							}

							if (pplContentRejectionLogger != null) {

								try {
									if (category != null
											&&  m_rbtCopyLikeUtils.isShuffleCategory(category
													.getCategoryId() + ""))
										pplContentRejectionLogger
												.PPLContentRejectionTransaction(
														vst.subID(),
														finalSelectedBy,
														"-1",
														category.getCategoryId()
																+ "",
														new Date());
									else if (clip != null)
										pplContentRejectionLogger
												.PPLContentRejectionTransaction(
														vst.subID(),
														finalSelectedBy,
														clip.getClipId() + "",
														"-1", new Date());
								} catch (ReportingException e) {
									logger.error(e.getMessage(), e);
								}

							}
							liteCondition = true;//RBT-14671 - # like
							sms = RBTCopyLikeUtils.getSMSText("LITE_USER", "PREMIUM_BLOCKED",
									null, language);
							sms =  m_rbtCopyLikeUtils.getSubstituedSMS(sms, songName, null, null,
									actAmt, selAmt, clip, category, vst.subID(),renewalAmount,freePeriodText);
							
							writeTrans(vst.subID(), vst.callerID(), wavFile,
									catNameBuf.toString(),
									Tools.getFormattedDate(vst.sentTime(),
											"yyyy-MM-dd HH:mm:ss"),
									m_localType, isActivated ? "YES" : "NO",
									isCopyDone ? "TRUE" : "FALSE",
									PREMIUM_CONTENT, keyPressed, copyType,
									confMode);
							if (getParamAsBoolean("EVENT_MODEL_GATHERER",
									"FALSE")) {
								try {
									eventLogger.copyTrans(
											vst.subID(),
											vst.callerID(),
											"NO",
											m_localType,
											"",
											"FALSE",
											vst.sentTime(),
											"N",
											keyPressed,
											PREMIUM_CONTENT,
											wavFile,
											confMode,
											getCalleeOperator(sub,
													vst.selectedBy()),
											new Date());
								} catch (ReportingException e) {
									logger.info("Caught an exception while writing event logs");
									logger.error("", e);
								}
							}
							removeCopyViralPromotion(vst.subID(),
									vst.callerID(), vst.sentTime());
							logger.info("SMS:" + sms);
						}

					}

					if (!liteCondition) {

						String smsText = null;
						boolean isAllowBaseSelOffer = getParamAsBoolean("ALLOW_GET_OFFER", "FALSE");
						boolean isAllowOnlyBaseOffer = getParamAsBoolean("ALLOW_ONLY_BASE_OFFER", "FALSE");
						String offerSubscriptionClass = null;
						if(!isSubActive(callerSub) && (isAllowBaseSelOffer || isAllowOnlyBaseOffer)) {
							logger.info("Going for the base/sel offer");
							Offer offer = getOffer(caller, "SUB");
							if(offer != null) {
								   offerSubscriptionClass = offer.getSrvKey();
								   smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_ACT_SMS_" + offerSubscriptionClass.toUpperCase(),	null, language, circleId);
							    }
						}
						
						if(smsText == null) {
							smsText = getSMSText("GATHERER",
								"OPT_IN_CONFIRMATION_ACT_SMS",
								m_optInConfirmationActSMS, language, circleId);
						}
						isOptinCopy = true;
						if (isActive) {
							
							smsText = getSMSText("GATHERER",
									"OPT_IN_CONFIRMATION_SEL_SMS",
									m_optInConfirmationSelSMS, language,
									circleId);
							//RBT-9284
							String optInConfirmationSelMPActiveSms = getSMSText(	
									"GATHERER",
									"OPT_IN_CONFIRMATION_SEL_MPACTIVE_SMS",
									null, language, circleId);
							if (optInConfirmationSelMPActiveSms != null && isSubActiveMPUser(subscriber)) {
								smsText = optInConfirmationSelMPActiveSms;
							}
							boolean isFreemiumUserUpgrdReq = isFreemiumUpgradeRequest(callerSub, clip,
									category1);
							String optInFreemiumUpgrdSms =  getSMSText(	
									"GATHERER",
									"OPT_IN_CONFIRMATION_SEL_FREEMIUM_SMS",
									null, language, circleId);
							if(isFreemiumUserUpgrdReq && optInFreemiumUpgrdSms!=null){
								smsText = optInFreemiumUpgrdSms;
								com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chrgClass=
								        getNextChargeClass(callerSub.getSubscriberID(), clip, category1);
								if(chrgClass!=null){
									selAmt = chrgClass.getAmount();
								}
								actAmt = getFreemiumBaseUpgrdAmt(callerSub.getSubscriberID(),"COPY");
							}
						}  

						// Incase of RBT Like feature it overrides the message.
						if (SmsKeywordsStore.likeKeywordsSet
								.contains(keyPressed)) {
                            
							boolean subActive = isSubActive(callerSub);
							logger.info("Sending Like SMS text for caller subscriberId: "
									+ callerSub.getSubscriberID()
									+ ", stats: "
									+ sub.getStatus());
							if (subActive) {
								smsText = getSMSText(
										"GATHERER",
										Constants.RBT_LIKE_ACCEPT_MESSAGE_FOR_ACTIVE_USER,
										likeActiveUserConfirmationSMS,
										language, circleId);
							} else {
								smsText = getSMSText(
										"GATHERER",
										Constants.RBT_LIKE_ACCEPT_MESSAGE_FOR_INACTIVE_USER,
										likeConfirmationSMS, language, circleId);
							}
							
							smsText = smsText.replaceAll("%caller%",
									vst.subID());
							int configuredSongLength = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.SMS,
									"SONG_NAME_LENGTH","0")) ;
							if(configuredSongLength >0 && songName!= null && !songName.isEmpty() && configuredSongLength <= songName.length() ){
								songName = songName.substring(0, configuredSongLength);
								songName = songName.trim();
							}
							smsText = smsText.replaceAll("%song%", songName);
							logger.debug("Sending Like SMS text. SmsText: "
									+ smsText);
						}
						sms = getSubstituedSMS(
									smsText,
									songName,
									getParamAsString("GATHERER",
											"WAIT_TIME_DOUBLE_CONFIRMATION",
											30 + ""),
									null,
									actAmt,
									selAmt,
									getParamAsString("SMS", "COPY_CONFIRM_KEYWORD",
											"COPYYES"),
									getParamAsString("SMS", "COPY_CANCEL_KEYWORD",
											"COPYNO"), clip, category, vst.subID(),renewalAmount,freePeriodText);
						updateViralPromotion(vst.subID(), vst.callerID(),
								vst.sentTime(), vst.type(), COPYCONFPENDING,
								null);
						logger.info("SMS:" + sms);
					}
				}
			} else if (getParamAsBoolean("IS_OPT_IN", "FALSE")) {
				logger.info(" OPT_IN COPY is enabled. subscriberId: "
						+ sub.getSubscriberID());
				sensPressStarSMS = true;
				String smsInCases = null;
				if (getParamAsBoolean("VALIDATE_COPY_CONTENT_TO_SEND_SMS",
						"FALSE")) {
					logger.info("Validating copy content to send SMS.");
					double baseOfferAmt = getOfferAmount(callerSub, "ACT",null);
					double selOfferAmt = getOfferAmount(callerSub, "SEL",clip.getClipId()+"");
					
					if(baseOfferAmt == -1 || selOfferAmt == -1){
						copyFailed(vst, "TECHNICAL_ERROR_FOR_OFFER", keyPressed, confMode);
						logger.info("Prism system is down while getting offer " + (subscriber != null ? subscriber.getStatus() : "NEW"));
						return "TECHNICAL_ERROR_FOR_OFFER";
					}

					actAmt = String.valueOf(baseOfferAmt);
					selAmt = String.valueOf(selOfferAmt);

					smsInCases = checkConditionsToSendSms(callerSub, sub,
							clipID, clip, language);
				}

				if (isSubActive(subscriber)
						&& !getParamAsBoolean("IS_OPT_IN_FOR_ACTIVE_SUB",
								"FALSE")) {
					logger.info("OptIn is TRUE and m_isOptInForActiveSub is false.. update sms_type to COPYCONFIRMED ");
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPYCONFIRMED, null);
				} else {
					String smsText = null;
					if (null != smsInCases) {
						smsText = smsInCases;
					} else {

						String subscriberId = subscriber.getSubscriberID();
						String subSubscriptionClass = subscriber.getSubscriptionClass();
			
						// RBT-10785: If offer is enabled then hit offer.do and
						// get offer.
						boolean isAllowGetOffer = getParamAsBoolean(
								"ALLOW_GET_OFFER", "FALSE");
						if (isSubActive(subscriber)) {
							// active subscriber to allow offer. 
							logger.info("Subscriber is active, subscriberId: "
									+ subscriberId);
							if (isAllowGetOffer) {
								Offer offer = getOffer(subscriberId, "sel");
								if (null != offer) {
									String srvKey = offer.getSrvKey();
									ChargeClass cClass = rbtConnector
											.getRbtGenericCache()
											.getChargeClass(srvKey);
									
									selAmt = cClass.getAmount();
									double selectionAmt = offer.getAmount();

									// replace char . to comma. 
									String replaceString = getParamAsString(
											"GATHERER",
											"SEL_AMT_REPLACEMENT_CHARS", null);
									if (null != replaceString && null != selAmt
											&& selAmt.contains(replaceString)) {
										selAmt = selAmt.replaceAll(".",
												replaceString);
									}
									if (selectionAmt ==0){
										renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(offer.getOfferRenewalValidity());
										freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
												"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
										freePeriodText = freePeriodText.replace("DD",renewalPeriod);
										renewalAmount = offer.getOfferRenewalAmount();
										selAmt = String.valueOf(selectionAmt);
									}
									logger.info("Got selection offer, updated"
											+ " selAmt: " + selAmt
											+ ", offer srvKey: " + srvKey
											+ ", database chargeClass: "
											+ cClass+", subscriberId: "
											+ subscriberId);
								}
							}
						} else {
							// Inactive subscriber.
							boolean isAllowOnlyBaseOffer = getParamAsBoolean(
									"ALLOW_ONLY_BASE_OFFER", "FALSE");
							logger.info("Subscriber is not active. subscriberId: "
									+ subscriberId);
							if (isAllowGetOffer || isAllowOnlyBaseOffer) {
								Offer offer = getOffer(subscriberId, "sub");
								if (null != offer) {
									String srvKey = offer.getSrvKey();
									SubscriptionClass sClass = rbtConnector
											.getRbtGenericCache()
											.getSubscriptionClassByName(srvKey);
									
									if (null == sClass) {
										logger.warn("Offer subscription class is"
												+ " not present in database. srvKey: "
												+ srvKey);
									} else {
										actAmt = sClass.getSubscriptionAmount();
										// replace char . to comma. 
										String replaceString = getParamAsString(
												"GATHERER",
												"ACT_AMT_REPLACEMENT_CHARS", null);
										if (null != replaceString && null != actAmt
												&& actAmt.contains(replaceString)) {
											actAmt = actAmt.replaceAll(".",
													replaceString);
										}
									
										String offerSmsText = getSMSText(
												"GATHERER", "OPT_IN_SMS_TEXT_free", null,
												language, circleId);
										String subTypeParam = "OPT_IN_" + srvKey
												+ "_OFFER_SMS_TEXT";
										if (offerSmsText == null) {
											offerSmsText = getSMSText(
													"GATHERER", subTypeParam, null,
													language, circleId);
										} else {
											logger.debug("free smsText configured. offerSmsText: " + offerSmsText + ", subscriberId: "
													+ subscriberId);
										}

										if(null != offerSmsText) {
											smsText = offerSmsText;
										} else {
											logger.warn("Sms Text parameter: "
													+ subTypeParam
													+ " is not configured");
										}
									}
									
									logger.info("Got base offer."
											+ " Updated actAmt: " + actAmt
											+ ", smsText: " + smsText
											+ ", offer" + " srvKey: " + srvKey
											+ ", database subscriptionClass: "
											+ sClass+", subscriberId: "
											+ subscriberId);
								} else {
									//RBT-13563: viral/copy sms based on user type
									smsText =  getSMSText(
											"GATHERER", "OPT_IN_SMS_TEXT_no_free", null,
											language, circleId);
									logger.debug("no_free smsText: " + smsText
											+ ", subscriberId: "
											+ subscriberId);
								}
							}

							if (isAllowGetOffer) {

								Offer offer = getOffer(subscriberId, "sel");
								if (null != offer) {
									
									String srvKey = offer.getSrvKey();
									ChargeClass cClass = rbtConnector
											.getRbtGenericCache()
											.getChargeClass(srvKey);
									
									if (cClass == null) {
										logger.warn("Offer charge class is not"
												+ " present in database. srvKey: "
												+ srvKey);
									} else {
										
										selAmt = cClass.getAmount();
										double selectionAmt = offer.getAmount();
										// replace char . to comma. 
										String replaceString = getParamAsString(
												"GATHERER",
												"SEL_AMT_REPLACEMENT_CHARS", null);
										if (null != replaceString && null != selAmt
												&& selAmt.contains(replaceString)) {
											selAmt = selAmt.replaceAll(".",
													replaceString);
										}
										if (selectionAmt==0){
											renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(offer.getOfferRenewalValidity());
											freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
													"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
											freePeriodText = freePeriodText.replace("DD",renewalPeriod);
											renewalAmount = offer.getOfferRenewalAmount();
											selAmt = String.valueOf(selectionAmt);
										}
										logger.info("Got selection offer, updated"
												+ " selAmt: " + selAmt
												+ ", offer srvKey: " + srvKey
												+ ", database chargeClass: "
												+ cClass+", subscriberId: "
												+ subscriberId);
									}
								}
							}
						}
						
						// RBT-10785: OI deployments. If migrated user
						// subscription classes are configured and subscriber's 
						// subscription class  is present in the configuration
						// send configured sms.
						boolean isMigratedUser = migratedUserSubClassesList
								.contains(subSubscriptionClass);

						if (isMigratedUser) {
							String migratedUserSmsText = getSMSText("GATHERER",
									"MIGRATED_USER_SMS_TEXT",
									m_optInMigratedUserSMS, language, circleId);
							smsText = migratedUserSmsText;
							logger.info("Successfully sent sms to migrated"
									+ " user. subscriberId: " + subscriberId
									+ ", smsText: " + migratedUserSmsText);
						}
						// RBT-10785 ends.
						
						if(smsText == null) {
							
							smsText = getSMSText("GATHERER",
									"OPT_IN_CONFIRMATION_ACT_SMS",
									m_optInConfirmationActSMS, language, circleId);
							isOptinCopy = true;
							if (isSubActive(subscriber)){
								boolean isFreemiumUserUpgrdReq = isFreemiumUpgradeRequest(callerSub, clip,
										category1);
								String optInFreemiumUpgrdSms =  getSMSText(	
										"GATHERER",
										"OPT_IN_CONFIRMATION_SEL_FREEMIUM_SMS",
										null, language, circleId);
								if(isFreemiumUserUpgrdReq && optInFreemiumUpgrdSms!=null){
									smsText = optInFreemiumUpgrdSms;
									com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chrgClass=
									        getNextChargeClass(callerSub.getSubscriberID(), clip, category1);
									if(chrgClass!=null){
										selAmt = chrgClass.getAmount();
									}
									actAmt = getFreemiumBaseUpgrdAmt(callerSub.getSubscriberID(),"COPY");
								} else {
									smsText = getSMSText("GATHERER", "OPT_IN_CONFIRMATION_SEL_SMS",
											m_optInConfirmationSelSMS, language, circleId);
								}
							}
						}
					}
					sms = getSubstituedSMS(
							smsText,
							songName,
							getParamAsString("GATHERER",
									"WAIT_TIME_DOUBLE_CONFIRMATION", 30 + ""),
							null,
							actAmt,
							selAmt,
							getParamAsString("SMS", "COPY_CONFIRM_KEYWORD",
									"COPYYES"),
							getParamAsString("SMS", "COPY_CANCEL_KEYWORD",
									"COPYNO"), clip, category, vst.subID(),renewalAmount,freePeriodText);
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPYCONFPENDING,
							vst.subID());
					logger.info("SMS:" + sms);
				}
			} else {
				//OPT-OUT model
				boolean isAllowBaseSelOffer = getParamAsBoolean("ALLOW_GET_OFFER", "FALSE");
				boolean isAllowOnlyBaseOffer = getParamAsBoolean("ALLOW_ONLY_BASE_OFFER", "FALSE");
				
				Offer subscriptionOffer = getOfferForSubscription(caller, callerSub, isAllowBaseSelOffer, isAllowOnlyBaseOffer);
				Offer selectionOffer = getOfferForSelection(caller, callerSub, isAllowBaseSelOffer);
	
					String offerSubscriptionClass = null;
					if(subscriptionOffer != null) {
						offerSubscriptionClass = subscriptionOffer.getSrvKey();
						actAmt = String.valueOf(subscriptionOffer.getAmount());
					} else {
						offerSubscriptionClass = getParamAsString("GATHERER", "COPY_SUB_CLASS",
								"COPY");
						if (offerSubscriptionClass != null)
						{
							SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(offerSubscriptionClass);
						if (subscriptionClass != null) {
							actAmt = subscriptionClass.getSubscriptionAmount();
						}
					}
					}
					
					String nextChargeClass = null;
					if(selectionOffer != null) {
						nextChargeClass = selectionOffer.getSrvKey();
						selAmt = String.valueOf(selectionOffer.getAmount());
						if(null != selAmt && Double.parseDouble(selAmt.replace(specialAmtChar,"."))==0){
							renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(selectionOffer.getOfferRenewalValidity());
							freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
									"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
							freePeriodText = freePeriodText.replace("DD",renewalPeriod);
							renewalAmount = selectionOffer.getOfferRenewalAmount();
						}
					} else {
						SelectionRequest selectionRequestObj =  new SelectionRequest(subscriber.getSubscriberID());
						selectionRequestObj.setClipID(""+ clip.getClipId());							
						if(category != null) selectionRequestObj.setCategoryID(""+category.getCategoryId());
						com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequestObj);
						if (chargeClass != null)
						{
							if(null != chargeClass.getAmount() && Double.parseDouble(selAmt.replace(specialAmtChar,"."))==0){
								nextChargeClass = chargeClass.getChargeClass();
								selAmt = chargeClass.getAmount();
								renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(chargeClass.getRenewalPeriod());
								freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
										"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
								freePeriodText = freePeriodText.replace("DD",renewalPeriod);
								renewalAmount = chargeClass.getRenewalAmount();
							}
						}
					}
					//RBT-14671 - # like
				sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
						RBTCopyLikeUtils.getSMSText("GATHERER", "PRESS_STAR_CONFIRMATION_SMS",
								m_pressStarConfirmationSMS, language),
						songName,
						getParamAsString("GATHERER",
								"WAIT_TIME_DOUBLE_CONFIRMATION", 30 + ""),
						null, actAmt, selAmt, clip, category, vst.subID(),renewalAmount,freePeriodText);
				if(isThirdPartyAllowedKeys) {
					sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
							RBTCopyLikeUtils.getSMSText("GATHERER", "PRESS_STAR_CONFIRMATION_SMS_BUNDLE_USER",
									sms, language),
							songName,
							getParamAsString("GATHERER",
									"WAIT_TIME_DOUBLE_CONFIRMATION", 30 + ""),
							null, actAmt, selAmt, clip, category, vst.subID(), renewalAmount,freePeriodText);
				}
				if (isSubActive(subscriber)) {
					if (RBTCopyLikeUtils.getSMSText("GATHERER",
							"PRESS_STAR_CONFIRMATION_SMS_ACTIVE", null,
							language) != null)
						sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
								RBTCopyLikeUtils.getSMSText("GATHERER",
										"PRESS_STAR_CONFIRMATION_SMS_ACTIVE",
										m_pressStarConfirmationSMS, language),
								songName,
								getParamAsString("GATHERER",
										"WAIT_TIME_DOUBLE_CONFIRMATION",
										30 + ""), null, actAmt, selAmt, clip,
								category, vst.subID(),renewalAmount,freePeriodText);
					if (isSubActiveMPUser(subscriber)
							&& RBTCopyLikeUtils.getSMSText(
									"GATHERER",
									"PRESS_STAR_CONFIRMATION_SMS_RBTACTIVE_MPACTIVE",
									null, language) != null) {
						sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
								RBTCopyLikeUtils.getSMSText(
										"GATHERER",
										"PRESS_STAR_CONFIRMATION_SMS_RBTACTIVE_MPACTIVE",
										m_pressStarConfirmationSMS, language),
								songName,
								getParamAsString("GATHERER",
										"WAIT_TIME_DOUBLE_CONFIRMATION",
										30 + ""), null, actAmt, selAmt, clip,
								category, vst.subID(), renewalAmount,freePeriodText);
					}

					boolean isFreemiumUserUpgrdReq = isFreemiumUpgradeRequest(callerSub, clip,
							category1);
					String optInFreemiumUpgrdSms =  getSMSText(	
							"GATHERER",
							"PRESS_STAR_CONFIRMATION_SEL_FREEMIUM_SMS",
							null, language, circleId);
					if(isFreemiumUserUpgrdReq && optInFreemiumUpgrdSms!=null){
						com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chrgClass=
						        getNextChargeClass(callerSub.getSubscriberID(), clip, category1);
						if(chrgClass!=null){
							selAmt = chrgClass.getAmount();
						}
						actAmt = getFreemiumBaseUpgrdAmt(callerSub.getSubscriberID(),"COPY");
						sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
								optInFreemiumUpgrdSms,
								songName,
								getParamAsString("GATHERER",
										"WAIT_TIME_DOUBLE_CONFIRMATION",
										30 + ""), null, actAmt, selAmt, clip,
								category, vst.subID(),renewalAmount,freePeriodText);

					}
					
					if(isThirdPartyAllowedKeys) {
						sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
								RBTCopyLikeUtils.getSMSText("GATHERER", "PRESS_STAR_CONFIRMATION_SMS_ACTIVE_BUNDLE_USER",
										sms, language),
								songName,
								getParamAsString("GATHERER",
										"WAIT_TIME_DOUBLE_CONFIRMATION", 30 + ""),
								null, actAmt, selAmt, clip, category, vst.subID(),renewalAmount,freePeriodText);
					}


				} else if (!isSubActive(subscriber)
						&& RBTCopyLikeUtils.getSMSText("GATHERER",
								"PRESS_STAR_CONFIRMATION_SMS_INACTIVE", null,
								language) != null) {
					String smsText = null;
					if(subscriptionOffer != null) {
						smsText = RBTCopyLikeUtils.getSMSText("GATHERER", "PRESS_STAR_CONFIRMATION_SMS_INACTIVE_"+ subscriptionOffer.getSrvKey(), null, language);
					}
					if(smsText == null) {
						smsText = RBTCopyLikeUtils.getSMSText("GATHERER", "PRESS_STAR_CONFIRMATION_SMS_INACTIVE", m_pressStarConfirmationSMS, language);
					}
					sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
							smsText,
							songName,
							getParamAsString("GATHERER",
									"WAIT_TIME_DOUBLE_CONFIRMATION", 30 + ""),
							null, actAmt, selAmt, clip, category, vst.subID(), renewalAmount,freePeriodText);
				}

				updateViralPromotion(vst.subID(), vst.callerID(),
						vst.sentTime(), vst.type(), COPYCONFIRM, null);
			}
		}
		if (sms != null
				&& sms.trim().length() > 0
				&& (getParamAsBoolean("SEND_COPY_PRE_SMS_ALL_MODES", "TRUE") || selectedForSMSBy == null)) {
			logger.info("SMS response is :" + sms);

			if (isOptinCopy) { // deactive caller case
				logger.info("Inside the check for same song " + songName);
				com.onmobile.apps.ringbacktones.content.Subscriber sub1 = rbtDBManager
						.getSubscriber(caller);
				if (sub1 != null) {
					if (songName != null && !songName.equalsIgnoreCase("")) {
						logger.info("Clip id is" + vst.clipID());
						SubscriberStatus substatus[] = rbtDBManager
								.getAllActiveSubSelectionRecords(caller);
						SubscriberStatus substatus1 = rbtDBManager
								.getAvailableSelection(
										null,
										caller,
										null,
										substatus,
										rbtDBManager.getCategory(
												category.getCategoryId(),
												callerCircleID, 'b'),
										wavFile,
										status,
										0,
										2359,
										clip.getClipStartTime(),
										clip.getClipEndTime(),
										false,
										(rbtDBManager.allowLooping() && rbtDBManager
												.isDefaultLoopOn()), sub1
												.rbtType(), null, vst.selectedBy());
						logger.info("Subscriber status= " + substatus1);
						if (substatus1 != null) {
							logger.debug("Selection : " + songName
									+ " is already active. "
									+ "Hence rejecting the request");//RBT-14671 - # like
							sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
									RBTCopyLikeUtils.getSMSText("GATHERER",
											"OPT_IN_SAME_SONG_FAILURE",
											m_optInSameSongSms, language),
									songName, "", null, null, null, clip,
									category, null,null,null);
							sendPressStarSMS(callerSub, sms);
							writeTrans(vst.subID(), vst.callerID(), wavFile,
									catNameBuf.toString(),
									Tools.getFormattedDate(vst.sentTime(),
											"yyyy-MM-dd HH:mm:ss"),
									m_localType, isActivated ? "YES" : "NO",
									"FALSE", DUPLICATE, keyPressed, copyType,
									confMode);
							if (getParamAsBoolean("EVENT_MODEL_GATHERER",
									"FALSE")) {
								try {
									eventLogger.copyTrans(
											vst.subID(),
											vst.callerID(),
											"NO",
											m_localType,
											"",
											"FALSE",
											vst.sentTime(),
											"N",
											keyPressed,
											DUPLICATE,
											wavFile,
											confMode,
											getCalleeOperator(sub,
													vst.selectedBy()),
											new Date());
								} catch (ReportingException e) {
									logger.info("Caught an exception while writing event logs");
									logger.error("", e);
								}
							}
							removeViralPromotion(vst.subID(), vst.callerID(),
									vst.sentTime(), COPYCONFPENDING);
							logger.info("Selection is alreadey active. Returning Response: "
									+ response);
							return response;
						}

					}
				}

				if (!isUserHavingAllCallerIDSelection(subscriber, wavFile)) {//RBT-14671 - # like
					logger.info("User not having all callerID selection and the current selection is default");
					sms =  m_rbtCopyLikeUtils.getSubstituedSMS(
							RBTCopyLikeUtils.getSMSText("GATHERER", "OPT_IN_SAME_SONG_FAILURE",
									m_optInSameSongSms, language), songName,
							"", null, null, null, clip, category, null,null,null);

/*					sms = getSubstituedSMS(
							getSMSText("GATHERER", "DEFAULT_COPY_CONTENT_EXISTS_SMS",
									m_defaultCopyContentSetSMS, language), songName,
							"", null, null, null, clip, category, null);
*/
					writeTrans(vst.subID(), vst.callerID(), wavFile,
							catNameBuf.toString(), Tools.getFormattedDate(
									vst.sentTime(), "yyyy-MM-dd HH:mm:ss"),
							m_localType, isActivated ? "YES" : "NO", "FALSE",
							DUPLICATE, keyPressed, copyType, confMode);
					if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									"NO", m_localType, "", "FALSE",
									vst.sentTime(), "N", keyPressed, DUPLICATE,
									wavFile, confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());
						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}
					}
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), COPYCONFPENDING);
				}
			}

			logger.info("Is optin Copy is : " + isOptinCopy);
			if (getParamAsBoolean("USE_UMP_URL", "FALSE") && isOptinCopy)// To
																			// send
																			// USSD
																			// optin
																			// confirmation
																			// request
			{
				logger.info("Using UMP URL ");
				String umpUrl = getParamAsString("UMP_GATEWAY_URL");// Gathrer
				if (umpUrl != null && !umpUrl.equalsIgnoreCase("")) {
					try {
						umpUrl = umpUrl.replaceAll("<%msisdn%>", caller);
						umpUrl = umpUrl.replaceAll("<%smstext%>",
								getEncodedUrlString(sms));
						Integer statusInt = new Integer(-1);
						StringBuffer result = new StringBuffer();
						logger.info("RBT:: UmpUrl: " + umpUrl);
						Tools.callURL(umpUrl, statusInt, result, false, null,
								-1);
						return response;

					} catch (Exception e) {
						logger.error("", e);
						e.printStackTrace();
					}
				}

			} else if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE")
					&& isOptinCopy) // To send optin confirmation request
									// through promo tool to check DND users
			{
				logger.info("Using DND Url to send the SMS ");
				sendSMSviaPromoTool(callerSub, sms);
			} else if (sensPressStarSMS) {
				sendPressStarSMS(callerSub, sms);
			} else {
				sendSMS(callerSub, sms);
			}
		}

		if (getParamAsBoolean("COPIEE_SEND_SMS", "FALSE") && isProcess) {//RBT-14671 - # like
			String copieeSMSText = RBTCopyLikeUtils.getSMSText("GATHERER", "COPIEE_SMS_TEXT", null, language);
			String copieeSMS =  m_rbtCopyLikeUtils.getSubstituedSMS(copieeSMSText, clip.getClipName() == null ? "" : clip.getClipName(), caller, null, null, null, clip, category, null,null,null);

			if (copieeSMS != null && copieeSMS.length() > 0 && called != null
					&& called.length() > 0) {
				
				logger.info("sensPressStarSMS :" + sensPressStarSMS);
				if (sensPressStarSMS) {
					sendPressStarSMS(callerSub, copieeSMS);
				} else {
					MNPContext mnpContext = new MNPContext(called, "COPY");
					SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
					boolean isSameOperator = (null != subscriberDetail) ? subscriberDetail.isValidSubscriber() : false;
					logger.info("Checking subscriber belongs to same operator. called: "+called+", isSameOperator: "+isSameOperator);
					if(isSameOperator) {//sms if called is with same operator
						sendSMS(sub, copieeSMS);
					} else {
						logger.warn("Subscriber and caller are not belongs to the same operator. So not sending SMS. caller: "+caller+", called: "+called);
					}
					
				}
			}
		}

		if (!isProcess && getParamAsBoolean("COPIEE_SEND_SMS", "FALSE")
				&& isOptinCopy
				&& SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {//RBT-14671 - # like
			String copieeSMS =  m_rbtCopyLikeUtils.getSubstituedSMS(
					RBTCopyLikeUtils.getSMSText("GATHERER", "COPIEE_OPTIN_SMS_TEXT", null,
							language), songName, caller, null, null, null,
					clip, category, null,null,null);
			if (copieeSMS != null && copieeSMS.length() > 0 && called != null
					&& called.length() > 0) {
				sendSMS(sub, copieeSMS);
			}
		}
		if (isProcess == false)
			writeCopyStats(vst.toString()
					+ "Copy Process EndTime "
					+ statsDateFormat.format(Calendar.getInstance().getTime())
					+ ", Process Time is : "
					+ (Calendar.getInstance().getTimeInMillis() - vst
							.getStartDate().getTime()));
		return response;
	}

	private boolean validateSendSMSForPPLContent(ViralSMSTable vst,
			String called, Subscriber callerSub, String keyPressed,
			String confMode, Clip clip, Category category,
			String cosidOrUdsType, String language) {
		List<String> currentContentType = new ArrayList<String>();
		currentContentType.add(clip.getContentType());
		String smsText = RBTCopyLikeUtils.getSMSText(GATHERER,
				"LITEUSER_PREMIUM_NOT_PROCESSED",
				"You are not authorised to access this content", language);
		if (rbtDBManager.isContentTypeBlockedForCosIdorUdsType(cosidOrUdsType,
				currentContentType)) {
			logger.info("Content type ::" + currentContentType
					+ " is blocked for cosid:: " + cosidOrUdsType);
			logger.info("RBTCopyprocessor :: processlocalcopyrequest() response :"
					+ WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED);
			if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE"))
				sendSMSviaPromoTool(callerSub,
						m_rbtCopyLikeUtils.getSubstituedSMS(
								smsText,
								clip.getClipName() == null ? "" : clip
										.getClipName(), called, null, null,
								null, clip, category, null,null,null));
			else
				sendSMS(callerSub, m_rbtCopyLikeUtils.getSubstituedSMS(smsText,
						clip.getClipName() == null ? "" : clip.getClipName(),
						called, null, null, null, clip, category, null,null,null));
			copyFailed(vst, "INVALIDCOPY", keyPressed, confMode);
			return true;
		} else {
			return false;
		}
	}

	private Offer getOfferForSelection(String caller,
			Subscriber callerSub, boolean isAllowBaseSelOffer) {
		Offer selectionOffer = null;
		if(isAllowBaseSelOffer) {
			selectionOffer = getOffer(caller, "sel");
		}
		return selectionOffer;
	}

	private Offer getOfferForSubscription(String caller, Subscriber callerSub, boolean isAllowBaseSelOffer, boolean isAllowOnlyBaseOffer) {
		Offer offer = null;
		if(!isSubActive(callerSub) && (isAllowBaseSelOffer || isAllowOnlyBaseOffer)) {
			offer = getOffer(caller, "SUB");
		}
		return offer;
	}

	private Clip getPickOfTheDayClip() {
		logger.info("Getting pick of the days");
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		PickOfTheDay[] picks = RBTClient.getInstance().getPickOfTheDays(
				applicationDetailsRequest);
		int size = (null != picks) ? picks.length : 0;
		if (size > 0) {
			int clipId = picks[0].getClipID();
			Clip pickOfTheDayclip = rbtConnector.getMemCache().getClip(clipId);
			logger.debug("Fetched pick of the clip from cache. clipId: "
					+ clipId + ", pickOfTheDayclip: " + pickOfTheDayclip);
			if (null != pickOfTheDayclip) {
				logger.info("Returning pick of the day clip. pickOfTheDayclip: " + pickOfTheDayclip);
				return pickOfTheDayclip;
			} else {
				logger.error("Invalid pickOfTheDayclip. clipId: " + clipId
						+ ", not exits.");
				return null;
			}
		}
		logger.error("Returning null, no pickOfTheDayClips are present.");
		return null;
	}

	public String checkConditionsToSendSms(Subscriber callerSub,
			Subscriber calledSub, String clipID, Clip clip, String language) {
		String strSmsText = null;
		boolean isContentExpired = (clip != null
				&& clip.getClipEndTime() != null && clip.getClipEndTime()
				.getTime() < System.currentTimeMillis());
		boolean isDefaultContent = (clipID == null || clipID.toUpperCase()
				.indexOf("DEFAULT") != -1);
		boolean isCallerActive = isSubActive(callerSub);
		boolean isSubNotActive = !isCallerActive;
		String callerStatus = callerSub.getStatus();
		String callerSubId = callerSub.getSubscriberID();
		String calledSubId = calledSub.getSubscriberID();
		boolean isNewUser = callerStatus
				.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| callerStatus.equalsIgnoreCase(WebServiceConstants.DEACTIVE);

		logger.info("To send SMSes checking the conditions."
				+ " isContentExpired: " + isContentExpired
				+ ", isDefaultContent: " + isDefaultContent + ", callerSubId: "
				+ callerSubId + ", calledSubId: " + calledSubId
				+ ", isCallerActive: " + isCallerActive + ", callerStatus: "
				+ callerStatus + ", isNewUser: " + isNewUser + ", clip: "
				+ clip);

		if (isContentExpired) {
			if (isSubNotActive) {
				// get the offer and send sms to activate base with default
				// song for all callers"GATHERER",
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
						"OPT_IN_CONFIRMATION_ACT_SMS_CONTENT_EXPIRED_IN_ACTIVE",
						m_optInConfirmationActSMS, language);
				
				logger.info("Content is expired and subscriber is not active, sending OPT_IN_CONFIRMATION_ACT_SMS_CONTENT_EXPIRED_IN_ACTIVE");
			} else {
				// send error message
				strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER", "EXPIRED_COPY_CONTENT_SMS",
						m_expiredCopyClipSMS, language);
				
				logger.info("Content is expired and subscriber is active, sending EXPIRED_COPY_CONTENT_SMS");
			}
		} else {
			// content is not expired
			if (isDefaultContent) {
				if (isCallerActive) {
					// check user has default song, if yes send error sms
					boolean hasDefaultSong = false;
					hasDefaultSong = hasDefaultSelection(callerSub);
					if (!hasDefaultSong) {
						hasDefaultSong = hasDefaultDownload(callerSub);
					}

					if (hasDefaultSong) {
						// send error sms//RBT-14671 - # like
						strSmsText = RBTCopyLikeUtils.getSMSText("GATHERER",
								"DEFAULT_COPY_CONTENT_EXISTS_SMS",
								m_defaultCopyContentSetSMS, language);
						logger.info("Content is NOT expired, default content and caller is active and has default song." +
								"So, sending DEFAULT_COPY_CONTENT_EXISTS_SMS");

					} else {
						// send opt in sms to user to make all caller
						// selection
						strSmsText = getSMSText("GATHERER",
								"OPT_IN_CONFIRMATION_SEL_SMS_DEFAULT_CONTENT",
								m_optInConfirmationSelSMS, language,
								callerSub.getCircleID());
						logger.info("Content is NOT expired, default content and caller is active and has NO default song." +
								"So, sending OPT_IN_CONFIRMATION_SEL_SMS_DEFAULT_CONTENT");

					}
				} else if (isNewUser) {
					// send optin sms along with base offer price to make
					// song selection with default song for all callers.

					strSmsText = getSMSText("GATHERER",
							"OPT_IN_CONFIRMATION_DEFAULT_SONG_ACT_SMS",
							m_optInConfirmationDefaultSongActSMS, language,
							callerSub.getCircleID());

					logger.info("Content is NOT expired, default content and caller is new user."
							+ "So, sending OPT_IN_CONFIRMATION_ACT_SMS");
				}
			} else {
				if (isCallerActive) {
					String smsMaxSameSongText = null;
					if (getParamAsBoolean("VALIDATE_COPY_CONTENT_TO_SEND_SMS",
							"FALSE")) {
						smsMaxSameSongText = checkContentToSendSMS(callerSub,clip,language);
					}
					if(smsMaxSameSongText!=null){
						strSmsText = smsMaxSameSongText;
					} else {
						// send error sms to user to say you have song
						// already
						strSmsText = getSMSText("GATHERER",
								"OPT_IN_CONFIRMATION_SEL_SMS",
								m_optInConfirmationSelSMS, language,
								callerSub.getCircleID());
						
						logger.info("Content is NOT expired, NON default content and caller is active"
								+ " and same content is not set."
								+ " So, sending OPT_IN_CONFIRMATION_SEL_SMS");
					}
				} else if (isNewUser) {
					// if content is not default content and subscriber is
					// new subscriber,
					// then to be send option sms to make this song along
					// with base + song price.

					strSmsText = getSMSText("GATHERER",
							"OPT_IN_CONFIRMATION_ACT_SMS",
							m_optInConfirmationActSMS, language,
							callerSub.getCircleID());
					
					logger.info("Content is NOT expired, NON default content and caller is new user"
							+ " So, sending OPT_IN_CONFIRMATION_ACT_SMS");

				}
			}
		}
		logger.info("Returning strSmsText: " + strSmsText);
		return strSmsText;
	}
	
	private boolean hasDownloadLimitReached(Subscriber callerSub, Clip clip) {
		boolean hasDownloadLimitReached = false;
		SubscriberDownloads[] subscriberDownloads = rbtDBManager
				.getActiveSubscriberDownloads(callerSub.getSubscriberID());
		int maxDwnAllowed = getParamAsInt("COMMON", "MAX_DOWNLOADS_ALLOWED", 1000);
		String wavFile = null;
		if (clip != null) {
			wavFile = clip.getClipRbtWavFile();
		}
		if (subscriberDownloads != null && subscriberDownloads.length > 0) {
			if(subscriberDownloads.length >= maxDwnAllowed) {
				hasDownloadLimitReached = true;
			}
			for (SubscriberDownloads subDownload : subscriberDownloads) {
				String promoId = subDownload.promoId();
				if (promoId.equalsIgnoreCase(wavFile)){
					hasDownloadLimitReached = false;
					break;
				}
			}
		}

		return hasDownloadLimitReached;
	}
	
	private boolean isDownloadPresent(Subscriber callerSub, Clip clip) {
		boolean isDownloadPresent = false;
		SubscriberDownloads[] subscriberDownloads = rbtDBManager
				.getActiveSubscriberDownloads(callerSub.getSubscriberID());
		String wavFile = null;
		if (clip != null) {
			wavFile = clip.getClipRbtWavFile();
		}
		if (subscriberDownloads != null && subscriberDownloads.length > 0) {
			for (SubscriberDownloads subDownload : subscriberDownloads) {
				String promoId = subDownload.promoId();
				if (promoId.equalsIgnoreCase(wavFile)){
					isDownloadPresent = true;
				    break;
				}
			}
		}
		
		return isDownloadPresent;
	}
	
	private boolean hasSetSameContent(Subscriber callerSub, Clip clip) {
		boolean hasSetSameContent = false;
		SubscriberStatus selections[] = rbtDBManager
				.getAllActiveSubscriberSettings(callerSub.getSubscriberID());
		String copyClipWav = clip.getClipRbtWavFile();
		logger.info("Got the selections for callerSub: "
				+ callerSub.getSubscriberID() + ", copyClipWav: " + copyClipWav
				+ ", selections: " + selections);

		if (null != selections && selections.length > 0) {

			for (SubscriberStatus sel : selections) {
				String selCallerID = sel.callerID();
				String selWavFile = sel.subscriberFile();

				logger.info("Comparing settingCallerID: " + selCallerID
						+ ", with callerSubscriberId: "
						+ callerSub.getSubscriberID()
						+ ", and comparing copyWavFile: " + copyClipWav
						+ ", with selection wavFile: " + selWavFile);

				if (copyClipWav.equalsIgnoreCase(selWavFile)
						&& (null == selCallerID || "all".equals(selCallerID))) {
					hasSetSameContent = true;
				}
			}
		}
		logger.info("Returning hasSetSameContent: " + hasSetSameContent);
		return hasSetSameContent;
	}

	private boolean hasDefaultDownload(Subscriber callerSub) {
		boolean hasDefaultSong = false;
		SubscriberDownloads downloads[] = rbtDBManager
				.getActiveSubscriberDownloads(callerSub.getSubscriberID());

		logger.info("Got the downloads for callerSub: "
				+ callerSub.getSubscriberID() + ", downloads: " + downloads);

		if (null == downloads || (null != downloads && downloads.length == 0)) {
			hasDefaultSong = true;
		}

		if (null != downloads) {
			for (SubscriberDownloads dwn : downloads) {
				String dwnPromotID = dwn.promoId();
				if (dwnPromotID.equalsIgnoreCase(defaultClipWavName)) {
					hasDefaultSong = false;
				}
			}
		}
		logger.info("Returning hasDefaultSong: " + hasDefaultSong);
		return hasDefaultSong;
	}

	private boolean hasDefaultSelection(Subscriber callerSub) {
		boolean hasDefaultSong = false;
		SubscriberStatus selections[] = rbtDBManager
				.getAllActiveSubscriberSettings(callerSub.getSubscriberID());
		logger.info("Got the selections for callerSub: "
				+ callerSub.getSubscriberID() + ", selections: " + selections);
		if (null == selections
				|| (null != selections && selections.length == 0)) {
			hasDefaultSong = true;
		}

		if (null != selections) {
			for (SubscriberStatus sel : selections) {
				String settingCallerID = sel.callerID();
				String wavFile = sel.subscriberFile();
				if (defaultClipWavName.equals(wavFile)
						&& (null == settingCallerID || settingCallerID
								.equalsIgnoreCase("all"))) {
					hasDefaultSong = true;
				}
			}
		}
		logger.info("Returning hasDefaultSong: " + hasDefaultSong
				+ ", callerSub: " + callerSub);
		return hasDefaultSong;
	}

	
	private double getOfferAmount(Subscriber sub, String offerType,String clipID) {
		String subscriberId = sub.getSubscriberID();
		Offer offer =  null;
		if(getParamAsBoolean("COMMON", "ENABLE_PACKAGE_OFFER", "FALSE")){
			offer = getPackageOffer(subscriberId,clipID);
		}else{
		    offer = getOffer(subscriberId, offerType);
		}
		double offerAmount = 0;
		if (null != offer) {
			if (offer.getOfferID().equalsIgnoreCase("-1")) {
				offerAmount = -1;
            }else {
            	offerAmount = offer.getAmount();
            }
		}
		logger.info("Returning offerAmount: " + offerAmount
				+ ", subscriberId: " + subscriberId + ", offerType: "
				+ offerType);
		return offerAmount;
	}

	private Offer getOffer(String subscriberId, String offerType) {
		Offer offer = null;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberId);
		rbtDetailsRequest.setMode("COPY");
		rbtDetailsRequest.setType(offerType);

		Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
		if (null != offers && offers.length > 0) {
			offer = offers[0];
		}
		logger.info("Returning offer: " + offer + ", subscriberId: "
				+ subscriberId + ", offerType: " + offerType);
		return offer;
	}

	private Offer getPackageOffer(String subscriberId, String clipID) {
		Offer offer = null;
		String offerType = null;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberId);
		rbtDetailsRequest.setMode("COPY");
		if(clipID!=null){
			rbtDetailsRequest.setClipID(clipID);
			rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
			offerType = "sel";
		}else{
			rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);
			offerType = "sub";
		}

		Offer[] offers = RBTClient.getInstance().getPackageOffer(rbtDetailsRequest);
		if (null != offers && offers.length > 0) {
			offer = offers[0];
		}
		logger.info("Returning package offer: " + offer + ", subscriberId: "
				+ subscriberId + ", offerType: " + offerType + "clipId = "+clipID);
		return offer;
	}

	private boolean isUserHavingAllCallerIDSelection(Subscriber subscriber,
			String wavFile) {
		boolean allowMakeSelection = true;
		SubscriberStatus[] subscriberStatus = rbtDBManager
				.getAllActiveSubscriberSettings(subscriber.getSubscriberID());
		boolean isHavingAllCallerIDSelection = false;
		if (subscriberStatus != null && subscriberStatus.length != 0) {
			for (SubscriberStatus subStatus : subscriberStatus) {
				if (subStatus.callerID() == null
						|| subStatus.callerID().equalsIgnoreCase(
								WebServiceConstants.ALL)) {
					isHavingAllCallerIDSelection = true;
					break;
				}
			}
		}

		if (isSubActive(subscriber)
				&& (subscriberStatus == null || subscriberStatus.length == 0 || !isHavingAllCallerIDSelection)
				&& !getParamAsBoolean("INSERT_DEFAULT_SEL", "FALSE")
				&& defaultClipWavName != null
				&& (wavFile.equalsIgnoreCase(defaultClipWavName))) {
			allowMakeSelection = false;
		}
		return allowMakeSelection;
	}

	public void processRRBTCopy(ViralSMSTable vst, Subscriber subscriber) {
		vst.setStartTime(Calendar.getInstance().getTime());
		writeCopyStats(vst.toString() + "Copy Process StartTime "
				+ statsDateFormat.format(vst.getStartDate()));
		RBTNode node = RBTMonitorManager.getInstance().startNode(
				vst.callerID(), RBTNode.NODE_COPY_PROCESSOR);
		String nodeResponse = RBTNode.RESPONSE_FAILURE;
		String extraInfoStr = vst.extraInfo();
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());//RBT-14671 - # like
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);
		String keyPressed = "NA";
		String copyType = DEFAULTCOPY;
		String confMode = "-";
		int trycount = vst.count();

		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		try {
			String caller = vst.callerID();
			logger.info("subscriber_id=" + vst.subID() + "|caller_id=" + caller
					+ "|clipID=" + vst.clipID() + "|sentTime=" + vst.sentTime()
					+ "|selBy=" + vst.selectedBy() + "|tryCount=" + vst.count());

			String subTypeRegion = "UNKNOWN";
			boolean isVirtualNo = false;
			if (virtualNumbers != null && virtualNumbers.size() > 0) {
				if (virtualNumbers.contains(vst.subID())) {
					isVirtualNo = true;
					logger.info("match found");
				}
			} else {
				isVirtualNo = true;
				logger.info("No Virtual numbers found");
			}
			logger.info("is Virtal no " + isVirtualNo + " caller " + caller
					+ " subsc" + vst.subID() + " key " + keyPressed);

			if (!isVirtualNo
					|| caller == null
					|| caller.length() < getParamAsInt(
							"PHONE_NUMBER_LENGTH_MIN", 10)
					|| caller.length() > getParamAsInt(
							"PHONE_NUMBER_LENGTH_MAX", 10)
					|| keyPressed == null || vst.subID() == null) {
				removeViralPromotion(vst.subID(), vst.callerID(),
						vst.sentTime(), vst.type());
				if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
					try {
						eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
								subTypeRegion, "-", "-", vst.sentTime(),
								copyType, keyPressed, RRBTCOPYFAILED,
								vst.clipID(), confMode,
								getCalleeOperator(sub, vst.selectedBy()),
								new Date());
					} catch (ReportingException e) {
						logger.info("Caught an exception while writing event logs");
						logger.error("", e);
					}
					if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
						writeTrans(vst.subID(), vst.callerID(), vst.clipID(),
								"-", Tools.getFormattedDate(vst.sentTime(),
										"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
								" - ", "-", RRBTCOPYFAILED, keyPressed,
								copyType, confMode);
					}
				}
				return;
			}

			String rrbtUrl = getParamAsString("RRBT_SYSTEM_URL");// Gathrer
			if (rrbtUrl != null && !rrbtUrl.equalsIgnoreCase("")) {
				try {
					rrbtUrl = rrbtUrl.replaceAll("<%keypressed%>", keyPressed);
					String details = vst.subID() + ":" + vst.callerID() + ":"
							+ vst.clipID();
					rrbtUrl = rrbtUrl.replaceAll("<%details%>", details);
					Integer statusInt = new Integer(-1);
					StringBuffer result = new StringBuffer();
					logger.info("RBT:: RRBTUrl: " + rrbtUrl);
					boolean success = Tools.callURL(rrbtUrl, statusInt, result,
							false, null, -1);
					if (!success)
						return;
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
					if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
						try {
							eventLogger.copyTrans(vst.subID(), vst.callerID(),
									"-", subTypeRegion, "-", "-",
									vst.sentTime(), copyType, keyPressed,
									RRBTCOPYREQUESTED, vst.clipID(), confMode,
									getCalleeOperator(sub, vst.selectedBy()),
									new Date());
						} catch (ReportingException e) {
							logger.info("Caught an exception while writing event logs");
							logger.error("", e);
						}
					}
					if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
						writeTrans(vst.subID(), vst.callerID(), vst.clipID(),
								"-", Tools.getFormattedDate(vst.sentTime(),
										"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
								" - ", "-", RRBTCOPYREQUESTED, keyPressed,
								copyType, confMode);

					}
					return;

				} catch (Exception e) {
					if (vst.count() == 0)
						logger.error("", e);
					else
						logger.info(e.getMessage());
					if (trycount < 3)
						rbtDBManager.setSearchCountCopy(vst.subID(),
								vst.type(), ++trycount, vst.sentTime(),
								vst.callerID());
					if (getParamAsInt("MAX_COPY_RETRY_COUNT", -1) == -1
							|| vst.count() <= getParamAsInt(
									"MAX_COPY_RETRY_COUNT", -1))
						throw new RetryableException(e.getMessage());
					else
						throw new Exception(e.getMessage());
				}
			}

		} catch (Throwable e) {
			logger.error("", e);
		} finally {
			RBTMonitorManager.getInstance().endNode(vst.callerID(), node,
					nodeResponse);
			writeCopyStats(vst.toString()
					+ "RRBTCopy Process EndTime "
					+ statsDateFormat.format(Calendar.getInstance().getTime())
					+ ", Process Time is : "
					+ (Calendar.getInstance().getTimeInMillis() - vst
							.getStartDate().getTime()));
		}
	}

	// getSubscriber is commented and passing Subscriber obj to method
	private String getActivationAmount(String subscriberID,
			Subscriber subscriber, String mode, String vrbtSubscriptionClass) {
		SubscriptionClass sClass = getActivationSubClass(subscriberID,
				subscriber, mode, vrbtSubscriptionClass);
		return null!=sClass?sClass.getSubscriptionAmount():null;
	}

	private SubscriptionClass getActivationSubClass(String subscriberID,
			Subscriber subscriber, String mode, String vrbtSubscriptionClass) {
		String subscriptionClass = "DEFAULT";
		String prepaidYes = "n";
		if (getParamAsString("GATHERER", "DEFAULT_SUBTYPE", "pre")
				.equalsIgnoreCase("pre"))
			prepaidYes = "y";
		CosDetails cosDetail = rbtDBManager.getCos(subscriberID,
				subscriber.getCircleID(), prepaidYes, mode);
		if (cosDetail == null) {
			subscriptionClass = subscriber.getSubscriptionClass();
		} else {
			subscriptionClass = cosDetail.getSubscriptionClass();
		}
		if (getParamAsString("GATHERER", "COPY_SUB_CLASS", "COPY") != null)
			subscriptionClass = getParamAsString("GATHERER", "COPY_SUB_CLASS",
					"COPY");
		
		if(vrbtSubscriptionClass != null && (vrbtSubscriptionClass = vrbtSubscriptionClass.trim()).length() != 0) {
			subscriptionClass = vrbtSubscriptionClass;
		}
		
		logger.info("RBT:SUBSCRIPTION Class = " + subscriptionClass
				+ " for subscriber id :" + subscriberID);
		SubscriptionClass sClass = rbtConnector.getRbtGenericCache()
				.getSubscriptionClassByName(subscriptionClass);
		return sClass;
	}

	private String getSelectionAmount(String subscriberID, String circleID,
			String selectedBy, String classType, Category category, Clip clip,
			boolean useUIChargeclass, int status) {

		logger.info("Getting the selection amount");

		String classtype = classType;

		try {
			ChargeClass cClass = getSelectionClass(subscriberID, circleID,
					selectedBy, classType, category, clip, useUIChargeclass,
					status, classtype);
			String amount = cClass.getAmount();
			logger.info("The amount is " + amount);
			return amount;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	private ChargeClass getSelectionClass(String subscriberID, String circleID,
			String selectedBy, String classType, Category category, Clip clip,
			boolean useUIChargeclass, int status, String classtype) {
		try{
			if (status != 90 && clip != null) {
				classtype = rbtDBManager.getChargeClass(subscriberID, circleID,
						clip.getClipNameWavFile(), classType, useUIChargeclass,
						category.getCategoryId(), category.getCategoryTpe(),
						category.getClassType(), clip.getClipEndTime(),
						selectedBy, clip.getClassType());
			}
			
			logger.info("Getting the chargeclass for the classtype : "
					+ classtype);
		}catch(Exception e){
			logger.error(e);
		}
		ChargeClass cClass = rbtConnector.getRbtGenericCache()
				.getChargeClass(classtype);
		return cClass;
	}

	private String getSelectionAmount(String subscriberID, String circleID,
			String selectedBy, String classType, Category category, Clip clip,
			int status) {

		return getSelectionAmount(subscriberID, circleID, selectedBy,
				classType, category, clip, false, status);
	}

	/*
	 * private boolean isCorpSub(String strSubID) { return
	 * isSubAlreadyActiveOnStatus(strSubID, null, 0); }
	 */

	private boolean isSubAlreadyActiveOnStatus(String strSubID,
			String callerID, int status) {
		SubscriberStatus subStatus = rbtDBManager.getActiveSubscriberRecord(
				strSubID, callerID, status, 0, 2359);

		if (subStatus != null)
			return true;

		return false;
	}

	private int getClipCopyDetails(String clipID, StringBuffer wavFileBuf,
			StringBuffer catTokenBuf, StringBuffer catNameBuf,
			StringBuffer classTypeBuffer, StringBuffer statusBuf,
			StringBuffer setForCallerbuf, boolean isVirtualNo, StringBuffer vrbtBuf) {
		logger.info("Getting category id. clipIDToken: " + clipID);
		int cat = 26;
		StringTokenizer stk = new StringTokenizer(clipID, ":");
		if (stk.hasMoreTokens()) {
			wavFileBuf.append(stk.nextToken());
			logger.debug("Got wavFileBuf: " +wavFileBuf.toString());
		}

		if (stk.hasMoreTokens()) {
			String catToken = stk.nextToken();
			if (catToken.toUpperCase().startsWith("S")) {
				catToken = catToken.substring(1);
			}
			
			boolean isShuffleCategory =  m_rbtCopyLikeUtils.isShuffleCategory(catToken);//RBT-14671 - # like
			logger.debug("Got catToken: " + catToken + ", wavFileBuf: "
					+ wavFileBuf.toString() + ", isShuffleCatetory: " + isShuffleCategory);
			
			// Return the category if the category type is shuffle. 
			if (isShuffleCategory) {
				
				logger.debug("Category id: " + catToken
						+ " is shuffle.  Checking wav file: "
						+ wavFileBuf.toString());
				// catToken = catToken.substring(1);
				if ("RADIO".equalsIgnoreCase(wavFileBuf.toString())) {
					cat = Integer.parseInt(catToken);
					logger.debug("Returning Category id: " + catToken
							+ " is shuffle and wav file is RADIO. ");
				}
				
				if (isVirtualNo
						|| !getParamAsBoolean("COPY_SHUFFLE_SONG_ONLY", "FALSE"))
					try {
						cat = Integer.parseInt(catToken);
					} catch (Exception e) {
						cat = 0;
					}
			} else {
				logger.warn("Category is not shuffle type. " + catToken
						+ ", so not returing category id.");
			}

			// Return the category if the wavFile is azaan wav file.  
			String azaanWavFile = getParamAsString(AZAAN_WAV_FILE_NAME);
			if (null != azaanWavFile
					&& azaanWavFile.equals(wavFileBuf.toString())) {
				if (null != azaanCategoryId) {
					cat = Integer.parseInt(azaanCategoryId);
					catToken = azaanCategoryId;
					logger.info("WavFile is Azaan WavFile, updating azaan catTokenBuf: "
							+ catToken + ", cat: " + cat);
				} else {
					logger.warn("Azaan Category is not configured. Please configure "
							+ "GATHERER, AZAAN_CATEGORY_ID");
				}
			} else {
				logger.debug("AZAAN_WAV_FILE_NAME is not configured"
						+ ", so not returing azaan category id.");
			}
			
			
			
			catTokenBuf.append(catToken);
			getCategoryCharge(catToken, classTypeBuffer, catNameBuf);
		}

		if (stk.hasMoreTokens()) {
			StringTokenizer stkStatus = new StringTokenizer(stk.nextToken(),
					"|");
			if (stkStatus.hasMoreTokens())
				statusBuf.append(stkStatus.nextToken());
			if (stkStatus.hasMoreTokens())
				setForCallerbuf.append(stkStatus.nextToken());
		}
		
		if(stk.hasMoreTokens()) {
			String vrbt = stk.nextToken();
			if(vrbt.equalsIgnoreCase("VRBT") && m_vrbtCatIdSubSongSrvKeyMap.containsKey(catTokenBuf.toString())) {
				vrbtBuf.append(vrbt);
			}
		}
		logger.info("Returning cat: " + cat);
		return cat;
	}

	private Clip getClipRBT(String strWavFile) {
		// return rbtDBManager.getClipRBT(strWavFile);
		return rbtConnector.getMemCache().getClipByRbtWavFileName(strWavFile);
	}

	public static boolean isSubActive(Subscriber sub) {
		if (sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| sub.getStatus().equalsIgnoreCase(
						WebServiceConstants.SUSPENDED)
				|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.GRACE)
				|| sub.getStatus().equalsIgnoreCase(WebServiceConstants.LOCKED))
			return true;
		else
			return false;
	}
	
	public static boolean isSubDctPending(Subscriber sub) {
		if (sub.getStatus().equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			return true;
		else
			return false;
	}

	// added by Parul for RBT-7683
	public boolean isSubActiveMPUser(Subscriber sub) {
		if (sub == null)
			return false;

		boolean isUserActive = false;
		HashMap<String, String> extraInfoMap = sub.getUserInfoMap();
		if (extraInfoMap != null && extraInfoMap.containsKey(EXTRA_INFO_PACK)) {
			String packStr = extraInfoMap.get(EXTRA_INFO_PACK);
			String[] packs = (packStr != null) ? packStr.trim().split(",")
					: null;
			for (int i = 0; packs != null && i < packs.length; i++) {
				String activePackCosId = packs[i];
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(
								activePackCosId);
				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					isUserActive = true;
					logger.info("subscriber is active music pack user.");
				}
			}
		}

		return isUserActive;
	}

	// Added subCallerID param to API on 21/01/10
	public Subscriber activateSubscriber(String strSubID, String subCircleID,
			String calledTo, String strActBy, boolean isPrepaid,
			String classType, String strActInfo, String selectedBy,
			String cosId, String viralSmsType, String confMode, String subOfferId, StringBuffer strBuff) {
		logger.debug("Activating subscriber. strSubID: " + strSubID
				+ ", subCircleID: " + subCircleID+", classType: "
				+ classType + " cosId: " + cosId+", confMode: "+confMode);

		String virtualNumberConfig = getParamAsString("VIRTUAL_NUMBERS",
				calledTo, null);
		if (virtualNumberConfig != null) {
			String circleID = null;
			String subClass = null;
			String[] tokens = virtualNumberConfig.split(","); // value :
			// wavFile,SubscriptionClass,circleId

			if (tokens.length >= 2)
				subClass = tokens[1];
			if (tokens.length >= 3)
				circleID = tokens[2];

			if (subCircleID == null) {
				MNPContext mnpContext = new MNPContext(strSubID, "COPY");
				mnpContext.setOnlineDip(getParamAsBoolean("COPY_ONLINE_DIP",
						"FALSE"));
				SubscriberDetail subscriberDetail = RbtServicesMgr
						.getSubscriberDetail(mnpContext);
				if (subscriberDetail != null)
					subCircleID = subscriberDetail.getCircleID();
			}
			if (subCircleID != null && subCircleID.equalsIgnoreCase(circleID)) {
				// subscriber called a virtual number and belongs to the same
				// circle. hence overriding the charge class
				classType = subClass;
			}
		}

		HashMap<String, String> extraInfo = new HashMap<String, String>();
		if (selectedBy == null || selectedBy.contains("PRESSSTAR")
				|| selectedBy.contains("XCOPY")) {
			extraInfo.put(REFUND, "TRUE");
		}
		if (viralSmsType != null
				&& viralSmsType.equalsIgnoreCase(COPYCONFIRMED)) {
			extraInfo.put(EXTRA_INFO_COPY_TYPE, EXTRA_INFO_COPY_TYPE_OPTIN);
		}
		if (confMode != null && !confMode.equalsIgnoreCase("-")) {
			extraInfo.put(EXTRA_INFO_COPY_MODE, confMode);

		}
		SubscriptionBean subBean = new SubscriptionBean();
		subBean.setSubId(strSubID);
		subBean.setIsPrepaid(isPrepaid);
		subBean.setExtraInfo(extraInfo);
		subBean.setCircleID(subCircleID);
		if (cosId != null) {
			subBean.setCosId(cosId);
			if (rbtDBManager.azaanDefaultCosId != null
					&& rbtDBManager.azaanDefaultCosId.equals(cosId))
				subBean.setSubcriptionClass(classType);
		} else
			subBean.setSubcriptionClass(classType);
		
		
		if(subOfferId != null) {
			subBean.setSubOfferId(subOfferId);
		}

		Subscriber activateSubscriber = rbtConnector.getSubscriberRbtclient().activateSubscriber(
				subBean, strBuff, strActBy, strActInfo);
		logger.debug("Activated subscriber. strSubID: " + strSubID
				+ ", activateSubscriber: " + activateSubscriber);
		return activateSubscriber;
	}

	public boolean updateExtraInfoAndPlayerStatus(String subscriberId,
			boolean pollOn) {
		String requestMode = "GATHERER";
		String actInfo = "GATHERER";
		StringBuffer responseBuff = new StringBuffer();
		SubscriptionBean subBean = new SubscriptionBean();
		subBean.setSubId(subscriberId);
		subBean.setPollOn(pollOn);
		boolean responseStatus = false;
		rbtConnector.getSubscriberRbtclient().updateSubcriberInfo(subBean,
				responseBuff, requestMode, actInfo);
		if (responseBuff != null && responseBuff.length() > 0) {
			String responseTemp = responseBuff.toString();
			if (responseTemp != null) {
				responseStatus = true;
				responseTemp = responseTemp.trim();
				responseTemp = responseTemp.toUpperCase();
				if (responseTemp.indexOf("SUCCESS") != -1) {
					responseStatus = true;
				}
			}
		}
		logger.debug("Updated SubcriberInfo for subscriberId: " + subscriberId
				+ ", responseStatus: " + responseStatus);
		return responseStatus;
	}

	private SubscriberPromo getSubscriberPromo(String strSubID) {
		return rbtDBManager.getSubscriberPromo(strSubID, "YOUTHCARD");
	}

	public void updateSubscription(String strSubID, int buddySelCount) {
		logger.debug("Updating subscription. strSubID: " + strSubID);
		SubscriptionBean subBean = new SubscriptionBean();
		subBean.setSubId(strSubID);
		HashMap<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("BUDDY_SEL", "" + buddySelCount);
		subBean.setExtraInfo(infoMap);
		rbtConnector.getSubscriberRbtclient().updateSubscription(subBean);
	}

	// Added selBy for TRAI regulation change. If selBy is NULL or PRESSSTAR or
	// XCOPY , update extraInfo with REFUND=TRUE
	public String addSelections(String strSubID, String strCallerID,
			boolean isPrepaid, int categoryID, Clip clip, String strSelectedBy,
			String strSelectionInfo, String classType, String subClass,
			String selBy, String viralSmsType, boolean isCallerSubscribed,
			String rbtTypeStr, String cosIdStr, int status,
			HashMap<String, String> subscriberInfoMap, String confMode,
			boolean useUIChargeClass, String subOfferId, String selOfferId) {

		logger.info("Making selection. strSubID: " + strSubID
				+ ", strCallerID: " + strCallerID + " categoryID: "
				+ categoryID + ", strSelectedBy: " + strSelectedBy
				+ ", strSelectionInfo: " + strSelectionInfo + ", classType: "
				+ classType + ", subClass: " + subClass + ", selBy: " + selBy
				+ ", cosIdStr: " + cosIdStr + ", useUIChargeClass: "
				+ useUIChargeClass);

		HashMap<String, String> extraInfo = new HashMap<String, String>();
		if (selBy == null || selBy.contains("PRESSSTAR")
				|| selBy.contains("XCOPY")) {
			extraInfo.put(REFUND, "TRUE");
		}
		if (viralSmsType != null
				&& viralSmsType.equalsIgnoreCase(COPYCONFIRMED)) {
			extraInfo.put(EXTRA_INFO_COPY_TYPE, EXTRA_INFO_COPY_TYPE_OPTIN);
		}
		if (confMode != null && !confMode.equalsIgnoreCase("-")) {
			extraInfo.put(EXTRA_INFO_COPY_MODE, confMode);

		}

		SelectionRequestBean selBean = new SelectionRequestBean();

		if (useUIChargeClass)
			selBean.setUseUIChargeClass(true);
		selBean.setSubscriberId(strSubID);
		selBean.setCallerId(strCallerID);
		selBean.setPrepaid(isPrepaid);
		selBean.setCatId("" + categoryID);
		if (clip != null)
			selBean.setToneId("" + clip.getClipId());
		if (status == 90)
			selBean.setCricpack("SP");
		selBean.setStatus(status);
		selBean.setChargeClass(classType);
		selBean.setSubscriptionClass(subClass);
		selBean.setExtraInfo(extraInfo);
		selBean.setSubscriberExtraInfo(subscriberInfoMap);
		if (rbtDBManager.allowLooping()
				&& getParamAsBoolean("ADD_COPY_SEL_IN_LOOP", "FALSE")) {
			selBean.setSetInLoop("true");
		}
		if (clip != null
				&& "EMOTION_RBT".equalsIgnoreCase(clip.getContentType())) {
			// status emotion song has content Type as EMOTION_RBT
			selBean.setMmContext(WebServiceConstants.EMOTION_RBT);
		}

		if(subOfferId != null) {
			selBean.setSubOfferId(subOfferId);
		}
		
		if(selOfferId != null) {
			selBean.setSelOfferId(selOfferId);
		}
		
		StringBuffer responseBuff = new StringBuffer();
		String actBy = strSelectedBy;
		if (baseModeMap.containsKey(actBy))
			actBy = baseModeMap.get(actBy);
		if (clip != null) {
			String tranformIndiaContentType = RBTParametersUtils
					.getParamAsString("COMMON",
							"TRANSFORM_INDIA_CLIP_CONTENT_TYPE", null);
			if (tranformIndiaContentType != null 
					&& !tranformIndiaContentType.isEmpty()
					&& tranformIndiaContentType.equalsIgnoreCase(clip
							.getContentType())) {
				StringBuffer selByBuff = new StringBuffer();
				StringBuffer cosIdBuff = new StringBuffer();
				StringBuffer actByBuff = new StringBuffer();
				selBean = prepareSelectionObjforTransformIndiaUser(selBean, clip,
						selByBuff, cosIdBuff, actByBuff);
				strSelectedBy = selByBuff != null ? selByBuff.toString() : "";
				cosIdStr = cosIdBuff != null ? cosIdBuff.toString() : "";
				actBy = actByBuff != null ? actByBuff.toString() : "";
			}
		}
		rbtConnector.getSubscriberRbtclient().makeSelection(selBean,
				strSelectedBy, strSelectionInfo, responseBuff,
				isCallerSubscribed, rbtTypeStr, cosIdStr, actBy);
		String responseTemp = "FAILURE";
		if (responseBuff != null && responseBuff.length() > 0) {
			responseTemp = responseBuff.toString();
			if (responseTemp != null) {
				responseTemp = responseTemp.trim();
				responseTemp = responseTemp.toUpperCase();
			}
		}
		logger.info("Successfully made selection. strSubID: " + strSubID
				+ ", responseTemp: " + responseTemp);
		return responseTemp;
	}

	public void processCopy(ViralSMSTable vst, Subscriber subscriber) {
		vst.setStartTime(Calendar.getInstance().getTime());
		writeCopyStats(vst.toString() + "Copy Process StartTime "
				+ statsDateFormat.format(vst.getStartDate()));
		RBTNode node = RBTMonitorManager.getInstance().startNode(
				vst.callerID(), RBTNode.NODE_COPY_PROCESSOR);
		String nodeResponse = RBTNode.RESPONSE_FAILURE;

		// Get Subscriber information from WebService
		if (subscriber == null)//RBT-14671 - # like
			subscriber = m_rbtCopyLikeUtils.getSubscriber(vst.callerID());
		String language = subscriber.getLanguage();
		String extraInfoStr = vst.extraInfo();
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(vst.subID());
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);
		String keyPressed = "NA";
		String copyType = DEFAULTCOPY;
		String confMode = "-";
		if (vst.type().equalsIgnoreCase(COPY)
				|| vst.type().equalsIgnoreCase(COPYCONFIRM)) {
			copyType = DIRECTCOPY;
			keyPressed = "s9";
		} else if (vst.type().equalsIgnoreCase(COPYCONFIRMED)) {
			copyType = OPTINCOPY;
			keyPressed = "s";
		}
		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR))
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
			confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
		try {
			String caller = vst.callerID();
			int tryCount = vst.count();
			int retryCountLimit =  getParamAsInt("RETRY_MAX_LIMIT", 3) ;
			logger.info("subscriber_id=" + vst.subID() + "|caller_id=" + caller
					+ "|clipID=" + vst.clipID() + "|sentTime=" + vst.sentTime()
					+ "|selBy=" + vst.selectedBy() + "|tryCount=" + vst.count());

			String subTypeRegion = "UNKNOWN";
			boolean copyProcessing = false;
			boolean copyTestFailed = false;
			ResponseEnum responseEnum = ResponseEnum.RETRY;
			if (caller == null
					|| caller.length() < getParamAsInt(
							"PHONE_NUMBER_LENGTH_MIN", 10)
					|| caller.length() > getParamAsInt(
							"PHONE_NUMBER_LENGTH_MAX", 10)) {
				removeViralPromotion(vst.subID(), vst.callerID(),
						vst.sentTime(), vst.type());
				writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
						Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss"), subTypeRegion, " - ",
						"-", COPYFAILED, keyPressed, copyType, confMode);
				if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
					try {
						eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
								subTypeRegion, "-", "-", vst.sentTime(),
								copyType, keyPressed, COPYFAILED, vst.clipID(),
								confMode,
								getCalleeOperator(sub, vst.selectedBy()),
								new Date());
					} catch (ReportingException e) {
						logger.info("Caught an exception while writing event logs");
						logger.error("", e);
					}
				}
				return;
			}
			caller = subscriber.getSubscriberID();
			String circleId = subscriber.getCircleID();
			if (circleId == null
					&& getParamAsBoolean("GATHERER",
							"CONSIDER_CROSS_OPR_AS_NON_ONMOBILE", "FALSE")) {
				circleId = "NON_ONMOBILE";
				subscriber.setCircleID(circleId);
			}

			logger.info("Subscriber is validPrefix: "+subscriber.isValidPrefix()+", selType: "+vst.selectedBy());
			
			// RBT Like feature: In case, if the subscriber non-local and
			// key pressed is like then reject the request.
			if (SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {
				subTypeRegion = m_localType;
				boolean isLocalCaller = subscriber.isValidPrefix();
				boolean isLocalSubscriber = sub.isValidPrefix();
				logger.info("Processing RBT Like for Caller: "
						+ subscriber.getSubscriberID() + ", isLocalCaller: "
						+ isLocalCaller + ", subscriber: "
						+ sub.getSubscriberID() + ", isLocalSubscriber: "
						+ isLocalSubscriber);

				// subscriber is local, if prefix is valid. CHECK FOR CALLER AND
				// CALLEE
				if (isLocalCaller && isLocalSubscriber) {
					nodeResponse = processLocalCopyRequest(vst, true,
							subscriber);
				} else {
					logger.warn("Subscriber or Caller is Non-local. Deleting from ViralSmsTable: "
							+ vst);
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
					// copy txn logs, internal
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
							" - ", "-", COPYFAILED, "l".concat(keyPressed),
							copyType, confMode);
					// copy txn logs for reporting
					eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
							subTypeRegion, "-", "-", vst.sentTime(), copyType,
							"l".concat(keyPressed), COPYFAILED, vst.clipID(),
							confMode, getCalleeOperator(sub, vst.selectedBy()),
							new Date());
				}
			} else if (subscriber.isValidPrefix()) {
				logger.info(caller + " is Local sub.");
				subTypeRegion = m_localType;
				if (getParamAsBoolean("IS_LOCAL_COPY_TEST_ON", "FALSE")
						&& !Arrays.asList(
								getParamAsString("GATHERER",
										"LOCAL_COPY_TEST_NUMBERS", "").split(
										",")).contains(caller))
					copyTestFailed = true;
				else
					nodeResponse = processLocalCopyRequest(vst, true,
							subscriber);
			} else if (getParamAsBoolean("MULTI_OP_COPY_CROSS_OPERATOR",
					"FALSE")
					&& !subscriber.isValidPrefix()
					&& (vst.selectedBy() != null && vst.selectedBy().indexOf(
							"XCOPY") != -1)) {
				logger.info("Caller: "+caller + " is CROSS copied. So, deleting the entry.");
				copyTestFailed = true;
				
			} else if (circleId == null
					&& getParamAsBoolean("COPY_CROSS_OPERATOR", "FALSE")
					&& ((getParamAsBoolean("IS_LOCAL_COPY_TEST_ON", "FALSE") && Arrays
							.asList(getParamAsString("GATHERER",
									"LOCAL_COPY_TEST_NUMBERS", "").split(","))
							.contains(caller)) || (!getParamAsBoolean(
							"IS_LOCAL_COPY_TEST_ON", "FALSE")))
					&& (vst.selectedBy() == null || vst.selectedBy().indexOf(
							"XCOPY") == -1)) {
				logger.info(caller + " is cross_operator sub.");
				subTypeRegion = m_crossOperatorType;
				
				// TODO: Orange spain changes.
				if(getParamAsBoolean("MULTI_OP_COPY_CROSS_OPERATOR", "FALSE")) {
					
					copyProcessing = processNonLocalMultiOperatorCopy(vst, subTypeRegion,
							subscriber);
				} else {
					 responseEnum =  processNonLocalCopy(vst, subTypeRegion,
							subscriber);
					if(responseEnum.equals(ResponseEnum.SUCCESS)){
						copyProcessing= true ;
					}
				}
				if (copyProcessing)
					nodeResponse = RBTNode.RESPONSE_SUCCESS;
			} else if (circleId != null
					&& circleId.equalsIgnoreCase("NON_ONMOBILE")) {
				logger.info(caller + " is non_onmobile sub.");
				subTypeRegion = m_nonOnmobileType;
				if (getParamAsBoolean("IS_NON_ONMOBILE_COPY_TEST_ON", "FALSE")
						&& !Arrays.asList(
								getParamAsString("GATHERER",
										"NON_ONMOBILE_COPY_TEST_NUMBERS", "")
										.split(",")).contains(caller))
					copyTestFailed = true;
				else {
					responseEnum = processNonLocalCopy(vst, subTypeRegion,
							subscriber);
					if (responseEnum.equals(ResponseEnum.SUCCESS)) {
						copyProcessing = true;
					}
				}
				if (copyProcessing)
					nodeResponse = RBTNode.RESPONSE_SUCCESS;
			}
				else if (circleId != null) {
				logger.info(caller + " is operator sub.");
				subTypeRegion = m_nationalType;
				if (getParamAsBoolean("IS_NATIONAL_COPY_TEST_ON", "FALSE")
						&& !Arrays.asList(
								getParamAsString("GATHERER",
										"NATIONAL_COPY_TEST_NUMBERS", "")
										.split(",")).contains(caller))
					copyTestFailed = true;
				else{

					 responseEnum = processNonLocalCopy(vst,
							subTypeRegion, subscriber);
					if (responseEnum.equals(ResponseEnum.SUCCESS)) {
						copyProcessing = true;
					}
				
				}
				if (copyProcessing)
					nodeResponse = RBTNode.RESPONSE_SUCCESS;
			} else {
				logger.info(caller + " is unrecognized sub.");
				if (getParamAsBoolean("SEND_SMS_NO_CROSS_COPY", "FALSE")) {//RBT-14671 - # like
					String sms = RBTCopyLikeUtils.getSMSText("GATHERER",
							"CROSS_COPY_NOT_SUPPORTED_SMS",
							m_crossCopyNotSupportedSMS, language);
					logger.info("SMS to  be sent : " + sms);
					sendSMS(subscriber, sms);
				}
			}
			if (copyTestFailed) {
				copyTestFailed(vst, subTypeRegion);
				return;
			}

			if (copyProcessing && !subTypeRegion.equalsIgnoreCase(m_localType)) {
				if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
					try {
						eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
								subTypeRegion, "-", "-", vst.sentTime(),
								copyType, keyPressed, COPIED, vst.clipID(),
								confMode,
								getCalleeOperator(sub, vst.selectedBy()),
								new Date());
					} catch (ReportingException e) {
						logger.info("Caught an exception while writing event logs");
						logger.error("", e);
					}
					if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
						writeTrans(vst.subID(), vst.callerID(), vst.clipID(),
								"-", Tools.getFormattedDate(vst.sentTime(),
										"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
								" - ", "-", COPIED, keyPressed, copyType,
								confMode);
					}
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
				} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
							" - ", "-", COPIED, keyPressed, copyType, confMode);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPIED, null);
			} else if (!subTypeRegion.equalsIgnoreCase(m_localType)) {
				if (responseEnum.equals(ResponseEnum.RETRY) && tryCount < retryCountLimit && !subTypeRegion.equalsIgnoreCase("UNKNOWN"))
					setSearchCountCopy(vst.subID(), ++tryCount, vst.type(),
							vst.sentTime(), vst.callerID());
				else if (getParamAsBoolean("EVENT_MODEL_GATHERER", "FALSE")) {
					try {
						eventLogger.copyTrans(vst.subID(), vst.callerID(), "-",
								subTypeRegion, "-", "-", vst.sentTime(),
								copyType, keyPressed, COPYFAILED, vst.clipID(),
								confMode,
								getCalleeOperator(sub, vst.selectedBy()),
								new Date());
					} catch (ReportingException e) {
						logger.info("Caught an exception while writing event logs");
						logger.error("", e);
					}
					if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
						writeTrans(vst.subID(), vst.callerID(), vst.clipID(),
								"-", Tools.getFormattedDate(vst.sentTime(),
										"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
								" - ", "-", COPYFAILED, keyPressed, copyType,
								confMode);
					}
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
				} else if (getParamAsBoolean("WRITE_TRANS", "FALSE")) {
					removeViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type());
					writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-",
							Tools.getFormattedDate(vst.sentTime(),
									"yyyy-MM-dd HH:mm:ss"), subTypeRegion,
							" - ", "-", COPYFAILED, keyPressed, copyType,
							confMode);
				} else
					updateViralPromotion(vst.subID(), vst.callerID(),
							vst.sentTime(), vst.type(), COPYFAILED, null);
			}

		} catch (Throwable e) {
			logger.error("", e);
		} finally {
			RBTMonitorManager.getInstance().endNode(vst.callerID(), node,
					nodeResponse);
			writeCopyStats(vst.toString()
					+ "Copy Process EndTime "
					+ statsDateFormat.format(Calendar.getInstance().getTime())
					+ ", Process Time is : "
					+ (Calendar.getInstance().getTimeInMillis() - vst
							.getStartDate().getTime()));
		}
	}

	private boolean processNonLocalMultiOperatorCopy(ViralSMSTable vst,
			String subTypeRegion, Subscriber subscriber) {
		logger.info("Processing non local multi operator copy. " + vst);
		int status = 2;
		String callerId = vst.callerID();
		String subscriberId = vst.subID();
		String clipId = vst.clipID();
		String smsType = vst.type();
		String extraInfoStr = vst.extraInfo();
		String sourceOp = getParamAsString("DAEMON",
				"MULTI_OP_COPY_SOURCE_OPERATOR", null);
		Set<String> operatorIdSet = RBTMultiOpCopyParams
				.getOperatorIdOperatorRBTNameMap().keySet();

		String copierOperatorIds = null;
		if (!operatorIdSet.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String opId : operatorIdSet) {
				if (!opId.equals(sourceOp)) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(opId);
				}
			}
			copierOperatorIds = sb.toString();
		}

		int clipID = -1;
		String clipName = "";
		String promoCode = "";
		String keyPressed = null;

		// To get the value keyPressed. Viral data extra info contains
		// keypressed.
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);
		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR)) {
			keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
		}

		// To get clip details based on wav file.
		if (clipId != null) {

			String wavFile = new StringTokenizer(vst.clipID(), ":").nextToken()
					.trim();
			if (wavFile != null) {
				Clip clip = getClipRBT(wavFile);
				if (clip != null) {
					clipID = clip.getClipId();
					clipName = clip.getClipName();
					promoCode = clip.getClipPromoId();
				}
			}
		}

		RBTMultiOpCopyRequest rbtMultiOpCopyRequest = new RBTMultiOpCopyRequest();

		rbtMultiOpCopyRequest.setStatus(status);
		rbtMultiOpCopyRequest.setCopierMdn(Long.parseLong(callerId));
		rbtMultiOpCopyRequest.setCopierOperatorIds(copierOperatorIds);
		rbtMultiOpCopyRequest.setRequestTime(Calendar.getInstance().getTime());
		rbtMultiOpCopyRequest.setCopieeMdn(Long.parseLong(subscriberId));

		rbtMultiOpCopyRequest.setCopieeOperatorIds(sourceOp);

		if (null != sourceOp) {
			String sourceOperatorName = RBTMultiOpCopyParams
					.getOperatorIdOperatorRBTNameMap().get(sourceOp);
			logger.info("Found sourceOperatorName: " + sourceOperatorName
					+ ", sourceOp: " + sourceOp);
			String selBy = null;
			if (sourceOperatorName != null) {
				selBy = sourceOperatorName + "_XCOPY";
				rbtMultiOpCopyRequest.setSourceMode(selBy);
			}
		}

		rbtMultiOpCopyRequest.setSourceContentId(String.valueOf(clipID));
		rbtMultiOpCopyRequest.setSourceContentDetails(clipId);

		rbtMultiOpCopyRequest.setCopyType(smsType);
		rbtMultiOpCopyRequest.setKeyPressed(keyPressed);
		rbtMultiOpCopyRequest.setTransferRetryCount(0);
		rbtMultiOpCopyRequest.setSourceSongName(clipName);
		rbtMultiOpCopyRequest.setSourcePromoCode(promoCode);

		boolean isInserted = RBTMultiOpCopyHibernateDao.getInstance().save(
				rbtMultiOpCopyRequest);

		logger.info("Successfully inserted multi operator copy request. isInserted: "
				+ isInserted
				+ ", rbtMultiOpCopyRequest: "
				+ rbtMultiOpCopyRequest);
		return isInserted;
	}

	private ViralSMSTable[] processCopyBulk() {
		logger.info("Entering");
		ViralSMSTable[] context = null;

		List<ViralSMSTable> viralSMSRecordList = new ArrayList<ViralSMSTable>();
		writeCopyStats("Entering processCopyBulk. Time is "
				+ statsDateFormat.format(Calendar.getInstance().getTime()));
		if (getParamAsBoolean("PRESS_STAR_DOUBLE_CONFIRMATION", "FALSE")
				|| getParamAsBoolean(
						"PRESS_STAR_DOUBLE_CONFIRMATION_INACTIVE_USER", "FALSE")) {
			// opt out

			ViralSMSTable[] optOutContext = null;
			writeCopyStats("Running COPYCONFIRM qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			optOutContext = populateOptOutPendingCopy();
			writeCopyStats("After COPYCONFIRM qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));

			if (optOutContext != null) {
				writeCopyStats("COPYCONFIRM count in DB is "
						+ optOutContext.length);
				for (int i = 0; i < optOutContext.length; i++) {
					viralSMSRecordList.add(optOutContext[i]);
				}
			} else
				writeCopyStats("COPYCONFIRM count in DB is " + 0);

		}
		if (getParamAsBoolean("IS_RRBT_COPY_ON", "FALSE")) {
			ViralSMSTable[] rrbtContext = null;
			writeCopyStats("Running RRBT_COPY qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			rrbtContext = populateRRBTCopy();
			writeCopyStats("After RRBT_COPY qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));

			if (rrbtContext != null) {
				writeCopyStats("RRBT_COPY count in DB is " + rrbtContext.length);
				for (int i = 0; i < rrbtContext.length; i++) {
					viralSMSRecordList.add(rrbtContext[i]);
				}
			}
		}
		if (getParamAsBoolean("IS_OPT_IN", "FALSE")
				|| getParamAsBoolean("IS_STAR_OPT_IN_ALLOWED", "FALSE")) {
			logger.info("inside optIn");

			// opt in
			ViralSMSTable[] optInContext = null;
			writeCopyStats("Running COPYCONFIRMED qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			optInContext = populateOptInPendingCopy();
			writeCopyStats("After COPYCONFIRM qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));

			if (optInContext != null) {
				writeCopyStats("COPYCONFIRMED count in DB is "
						+ optInContext.length);
				for (int i = 0; i < optInContext.length; i++) {
					viralSMSRecordList.add(optInContext[i]);
				}
			} else
				writeCopyStats("COPYCONFIRMED count in DB is " + 0);

			// copystar requests to be processed
			ViralSMSTable[] starOptInContext = null;
			writeCopyStats("Running COPYSTAR qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			starOptInContext = populateStarOptInCopy();
			writeCopyStats("After COPYSTAR qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			if (starOptInContext != null) {
				writeCopyStats("COPYSTAR count in DB is "
						+ starOptInContext.length);
				for (int i = 0; i < starOptInContext.length; i++) {
					viralSMSRecordList.add(starOptInContext[i]);
				}
			} else
				writeCopyStats("COPYSTAR count in DB is " + 0);

		}

		if (getParamAsBoolean(IS_COPY_PROMOTE, "FALSE")) {
			ViralSMSTable[] promoteCopyContext = null;
			writeCopyStats("Running COPY_PROMOTE qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			promoteCopyContext = getViralSMSByType(PROMOTE);
			writeCopyStats("After COPYCONFIRM qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));

			if (promoteCopyContext != null) {
				writeCopyStats("COPY_PRMOTE count in DB is "
						+ promoteCopyContext.length);
				for (int i = 0; i < promoteCopyContext.length; i++) {
					viralSMSRecordList.add(promoteCopyContext[i]);
				}
			} else {
				writeCopyStats("COPY_PRMOTE count in DB is " + 0);
			}

		}
		writeCopyStats("Running COPY qeury");
		writeCopyStats("Count Time is "
				+ statsDateFormat.format(Calendar.getInstance().getTime()));
		context = getViralSMSByType("COPY");
		writeCopyStats("After COPY qeury");
		writeCopyStats("Count Time is "
				+ statsDateFormat.format(Calendar.getInstance().getTime()));

		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			writeCopyStats("COPY count in DB is " + 0);
		} else {
			logger.info("Count of copyContext is " + context.length);
			writeCopyStats("COPY count in DB is " + context.length);
			for (int i = 0; i < context.length; i++) {
				viralSMSRecordList.add(context[i]);
			}
		}
		
		String crossCopyConfigured = rbtConnector.getRbtGenericCache().getParameter(COMMON, CROSSCOPY_KEY, "null");
		logger.info("to process virulSMSTable  crossCopyConfigured value is :" + crossCopyConfigured);
		if(crossCopyConfigured != null) {
			ViralSMSTable[] crossCopyContext = null;
			crossCopyContext = getViralSMSByType(CROSSCOPY);
			writeCopyStats("After CROSSCOPY qeury");
			writeCopyStats("Count Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			
			if (crossCopyContext == null || crossCopyContext.length <= 0) {
				logger.info("crossCopyContext is null or count <= 0");
				writeCopyStats("CROSSCOPY count in DB is " + 0);
			} else {
				logger.info("Count of crossCopyContext is " + crossCopyContext.length);
				writeCopyStats("CROSSCOPY count in DB is " + crossCopyContext.length);
				for (int i = 0; i < crossCopyContext.length; i++) {
					viralSMSRecordList.add(crossCopyContext[i]);
				}
			}
		}

		if (viralSMSRecordList != null && viralSMSRecordList.size() > 0) {
			logger.info("Count of copy Context is not null");
			writeCopyStats("Total count in DB is " + viralSMSRecordList.size());
			writeCopyStats("Exiting processCopyBulk. Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			return (ViralSMSTable[]) viralSMSRecordList
					.toArray(new ViralSMSTable[0]);
		} else {
			logger.info("Final Count of copy context is null");
			writeCopyStats("Total count in DB is " + 0);
			writeCopyStats("Exiting processCopyBulk. Time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			return null;
		}
	}

	private void populateArrayListBasedOnCircle() {
		logger.info("Entering populateArrayListBasedOnCircle()");

		logger.info("runCount for copyPopulator is " + runCount);
		writeCopyStats("Loop Time is "
				+ statsDateFormat.format(Calendar.getInstance().getTime()));
		writeCopyStats("runCount for copyPopulator is " + runCount);
		writeCopyStats("List of circles is " + m_circleList);
		writeCopyStats("Prefix map is " + m_operatorPrefixMap);
		writeCopyStats("Size of copy queues are : ");
		for (int j = 0; j < m_circleList.size(); j++) {
			String key = m_circleList.get(j);
			Vector<ViralSMSTable> pendingList = m_ViralSMSRecordsListMap
					.get(key);
			logger.info("The size of pending records for " + key + " is "
					+ pendingList.size());
			writeCopyStats("The size of pending records for " + key + " is "
					+ pendingList.size());
			if (pendingList.size() <= 10) {
				for (int i = 0; i < pendingList.size(); i++)
					writeCopyStats("Record " + i + " is "
							+ pendingList.get(i).toString());
			}
		}

		boolean recordAdded = false;
		int copyProcessingCount = getParamAsInt("COPY_PROCESSING_COUNT", 1000);
		runCount++;
		boolean isListEmpty = false;
		boolean isLocalListEmpty = false;

		for (int j = 0; j < m_circleList.size(); j++) {
			String key = m_circleList.get(j);
			Vector<ViralSMSTable> pendingList = m_ViralSMSRecordsListMap
					.get(key);
			if (key.equalsIgnoreCase("LOCAL") && pendingList.size() == 0)
				isLocalListEmpty = true;
			if (pendingList.size() == 0
					|| ((key.equals("CROSS_OPERATOR")
							|| key.equals("NON_ONMOBILE") || key
								.equals("OPERATOR")) && pendingList.size() < 50)) {
				writeCopyStats("Running queries as some queue size is zero or minimal");
				isListEmpty = true;
			} else
				writeCopyStats("No queue with size is zero or minimal found.");
		}
		logger.info("Empty/Relatively empty queues found : " + isListEmpty);

		if (isListEmpty)
			logger.info("Empty/Relatively empty queues found : " + isListEmpty);
		else {
			logger.info("Returning from  populateArrayListBasedOnCircle as no queue is empty.");
			return;
		}
		ViralSMSTable[] context = processCopyBulk();
		if (context == null)
			return;
		Hashtable<String, String> callerClipMap = new Hashtable<String, String>();
		for (int i = 0; i < context.length; i++) {
			writeCopyStats("Resolving request number " + i + ". Record is "
					+ context[i].toString());
			logger.info("Resolving request number " + i + ". Record is "
					+ context[i].toString());
			String extraInfoStr = context[i].extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfoStr);
			String keyPressed = "NA";
			String confMode = "-";
			if (viralInfoMap != null
					&& viralInfoMap.containsKey(COPY_CONFIRM_MODE_KEY))
				confMode = viralInfoMap.get(COPY_CONFIRM_MODE_KEY);
			if (context[i].type().equalsIgnoreCase(COPY)
					|| context[i].type().equalsIgnoreCase(COPYCONFIRM))
				keyPressed = "s";
			else if (context[i].type().equalsIgnoreCase(COPYCONFIRMED))
				keyPressed = "s9";
			if (viralInfoMap != null
					&& viralInfoMap.containsKey(KEYPRESSED_ATTR))
				keyPressed = viralInfoMap.get(KEYPRESSED_ATTR);
			String circleId = null;
			boolean isLocalSubscriber = false;
			if (context[i].callerID() == null) {
				writeCopyStats("Deleting record as caller is null. "
						+ context[i].callerID());
				logger.info("Deleting record as caller is null : "
						+ context[i].callerID());
				copyFailed(context[i], "COPYFAILED", keyPressed, confMode);
				continue;
			}
			if (callerClipMap.containsKey(context[i].callerID())) {
				// if(sameSong(pendingTable.get(context[i].callerID())),
				// context[i])
				// deleteViral();
				if (sameSongCopy(context[i],
						callerClipMap.get(context[i].callerID()))) {
					writeCopyStats("Deleting record as song is duplicate. "
							+ context[i].toString());
					logger.info("Deleting record as song is duplicate. : "
							+ context[i].toString());
					copyFailed(context[i], "DUPLICATE", keyPressed, confMode);
					continue;
				} else {
					writeCopyStats("Multiple selection by same caller.Will be processed in next run "
							+ context[i].toString());
					logger.info("Multiple selection by same caller.Will be processed in next run "
							+ context[i].toString());
					continue;
				}
			}

			if (context[i].type() != null
					&& context[i].type().equals(RRBT_COPY))
				circleId = "RRBT";
			else if (isDbImplForPrefix)
				circleId = getCircleId(context[i].callerID());
			else {//RBT-14671 - # like
				Subscriber subscriber = m_rbtCopyLikeUtils.getSubscriber(context[i].callerID());
				if (subscriber != null) {
					circleId = subscriber.getCircleID();
					isLocalSubscriber = subscriber.isValidPrefix();

					context[i].setSubscriber(subscriber);
				}
			}

			logger.info("circle id for  " + context[i].callerID() + " is : "
					+ circleId);
			Vector<ViralSMSTable> pendingTable = null;
			if (circleId == null || circleId.equalsIgnoreCase("CROSS_OPERATOR"))
				pendingTable = m_ViralSMSRecordsListMap.get("CROSS_OPERATOR");
			else if ((isLocalSubscriber || circleId.equals("LOCAL"))
					&& isLocalListEmpty)
				pendingTable = m_ViralSMSRecordsListMap.get("LOCAL");
			else
				pendingTable = m_ViralSMSRecordsListMap.get(circleId);

			if (pendingTable != null
					&& pendingTable.size() < copyProcessingCount) {
				synchronized (pendingTable) {
					// subs.add(context[i].callerID());
					recordAdded = true;
					logger.info("The record is populated in queue : "
							+ circleId + " and callerId : "
							+ context[i].callerID());
					pendingTable.add(context[i]);
					callerClipMap.put(context[i].callerID(), context[i]
							.clipID() == null ? "" : context[i].clipID());
					writeCopyStats("Populated record in queue " + circleId);
					pendingTable.notify();
				}
			} else if (pendingTable == null && isLocalListEmpty) {
				writeCopyStats("Local queue not empty. hence not populating request "
						+ context[i].callerID());
				logger.info("Local queue not empty. hence not populating request "
						+ context[i].callerID());
			} else if (pendingTable == null) {
				writeCopyStats("No queuRRBTVe found for caller "
						+ context[i].callerID());
				logger.info("No queue found for caller "
						+ context[i].callerID());
			} else {
				writeCopyStats("Queue size limit reached for "
						+ context[i].callerID());
				logger.info("Queue size limit reached for "
						+ context[i].callerID());
			}
		}

		if (recordAdded) {
			Set<String> circleKeySet = m_ViralSMSRecordsListMap.keySet();
			for (String circleKeys : circleKeySet) {
				synchronized (m_ViralSMSRecordsListMap.get(circleKeys)) {
					m_ViralSMSRecordsListMap.get(circleKeys).notifyAll();
				}
			}
		}
	}

	private ViralSMSTable[] populateRRBTCopy() {
		ViralSMSTable[] context = null;
		context = rbtDBManager.getViralSMSByType(RRBT_COPY);
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			return null;
		}
		logger.info("Count of Opt Out Double Confirm copyContext is "
				+ context.length);
		return context;
	}

	private ViralSMSTable[] populateOptOutPendingCopy() {
		ViralSMSTable[] context = null;
		context = rbtDBManager.getViralSMSByTypeAndTime(COPYCONFIRM,
				getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION", 30));
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			return null;
		}
		logger.info("Count of Opt Out Double Confirm copyContext is "
				+ context.length);
		return context;
	}

	private ViralSMSTable[] populateOptInPendingCopy() {
		ViralSMSTable[] context = null;
		context = getViralSMSByType(COPYCONFIRMED);
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			return null;
		}
		logger.info("Count of Opt In Double Confirm copyContext is "
				+ context.length);
		return context;
	}

	private ViralSMSTable[] populateStarOptInCopy() {
		ViralSMSTable[] context = null;
		context = getViralSMSByType(COPYSTAR);
		if (context == null || context.length <= 0) {
			logger.info("Context is null or count <= 0");
			return null;
		}
		logger.info("Count of Star Opt In Double Confirm copyContext is "
				+ context.length);
		return context;
	}
	//RBT-14671 - # like
	private String getSubstituedSMS(String smsText, String str1, String str2,
			String str3, String actAmt, String selAmt, String confirmKey,
			String cancelKey, Clip clip, Category category, String calledID, String renewalAmount, String freePeriodText) {
		if (confirmKey != null) {
			while (smsText.indexOf("%RBT_CONFIRM") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%RBT_CONFIRM"))
						+ confirmKey
						+ smsText
								.substring(smsText.indexOf("%RBT_CONFIRM") + 12);
			}
		}
		if (cancelKey != null) {
			while (smsText.indexOf("%RBT_CANCEL") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%RBT_CANCEL"))
						+ cancelKey
						+ smsText
								.substring(smsText.indexOf("%RBT_CANCEL") + 11);
			}
		}
		return  m_rbtCopyLikeUtils.getSubstituedSMS(smsText, str1, str2, str3, actAmt, selAmt,
				clip, category, calledID, renewalAmount,freePeriodText);
	}
	
	private void checkThreads() {
		String method = "checkThreads";
		logger.info("Entering " + method + " with pool size = "
				+ getParamAsInt("COPY_THREAD_POOL_SIZE", 1));
		Set<String> keySet = m_ViralSMSRecordsListMap.keySet();

		for (String key : keySet) {
			List<RBTCopyThread> tempThreadList = (List<RBTCopyThread>) m_copyThreadPoolMap
					.get(key);
			Vector<ViralSMSTable> pendingList = m_ViralSMSRecordsListMap
					.get(key);
			writeCopyStats("Thread check time is "
					+ statsDateFormat.format(Calendar.getInstance().getTime()));
			for (int i = 0; i < tempThreadList.size(); i++) {
				RBTCopyThread tempThread = (RBTCopyThread) tempThreadList
						.get(i);
				logger.info("Got copy thread " + tempThread + " for circle : "
						+ key);
				if (tempThread == null || !tempThread.isAlive()) {
					writeCopyStats("Found a dead thread in circle "
							+ key
							+ (tempThread == null ? "Null Thread" : tempThread
									.getThreadDetail()));
					tempThread = new RBTCopyThread(m_parentGathererThread,
							pendingList);
					tempThread.setThreadName("COPY=" + key + "-"
							+ System.currentTimeMillis());
					ThreadMonitor.getMonitor().register(tempThread);
					tempThread.start();
					tempThreadList.set(i, tempThread);
					logger.info("Created copy thread " + tempThread
							+ " for circle : " + key);
				} else
					writeCopyStats("Circle " + key + " : "
							+ tempThread.getThreadDetail());
			}

			m_copyThreadPoolMap.put(key, tempThreadList);
		}
	}

	private void makeThreads() {
		String method = "makeThreads";
		int copyThreadPoolSize = getParamAsInt("COPY_THREAD_POOL_SIZE", 1);
		logger.info("Entering " + method + " with copy size = "
				+ copyThreadPoolSize);
		Set<String> keySet = m_ViralSMSRecordsListMap.keySet();
		for (String key : keySet) {
			Vector<ViralSMSTable> pendingList = m_ViralSMSRecordsListMap
					.get(key);
			List<RBTCopyThread> tempThreadList = new ArrayList<RBTCopyThread>();

			for (int i = 0; i < copyThreadPoolSize; i++) {
				RBTCopyThread tempThread = new RBTCopyThread(
						m_parentGathererThread, pendingList);
				tempThread.setThreadName("COPY-" + key + "-" + i);
				ThreadMonitor.getMonitor().register(tempThread);
				tempThread.start();
				tempThreadList.add(tempThread);
				logger.info("Created copy thread " + tempThread
						+ " on circle : " + key);
			}

			m_copyThreadPoolMap.put(key, tempThreadList);
		}
	}

	private void startFailedCopyThread() {
		RBTFailedCopyThread rbtFailedCopyThread = new RBTFailedCopyThread(
				m_parentGathererThread);
		boolean isThreadStart = rbtFailedCopyThread.initialize();
		if (isThreadStart) {
			logger.info("Starting thread for reprocessing of failed copy requests");
			rbtFailedCopyThread.start();
		} else {
			logger.info("Thread is not started for reprocessing of failed copy requests");
		}
	}

	private void startExpiredCopyThread() {
		RBTExpiredCopyThread rbtExpiredCopyThread = new RBTExpiredCopyThread(
				m_parentGathererThread);
		rbtExpiredCopyThread.initialize();
		rbtExpiredCopyThread.start();
	}

	private String getParamAsString(String param) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter("GATHERER",
					param, null);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}

	private String getParamAsString(String type, String param,
			String defaultValue) {
		try {
			return rbtConnector.getRbtGenericCache().getParameter(type, param,
					defaultValue);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type
					+ ". Returning defVal > " + defaultValue);
			return defaultValue;
		}
	}

	private int getParamAsInt(String param, int defaultVal) {
		try {
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(
					"GATHERER", param, defaultVal + "");
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal) {
		try {
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(
					type, param, defaultVal + "");
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type
					+ " returning defaultVal >" + defaultVal);
			return defaultVal;
		}
	}

	private boolean getParamAsBoolean(String param, String defaultVal) {
		try {
			return rbtConnector.getRbtGenericCache()
					.getParameter("GATHERER", param, defaultVal)
					.equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	public static ArrayList<String> tokenizeArrayList(String stringToTokenize,
			String delimiter) {
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());

		return result;
	}

	private boolean getParamAsBoolean(String type, String param,
			String defaultVal) {
		try {
			return rbtConnector.getRbtGenericCache()
					.getParameter(type, param, defaultVal)
					.equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param + "  type ->" + type
					+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	public boolean prepareAndSendXml(ViralSMSTable[] context) {
		if (context == null || context.length == 0)
			return false;

		String followupKeys = rbtConnector.getRbtGenericCache().getParameter(
				"COMMON", "OBD_FOLLOWUP_KEYS", null);
		if (followupKeys != null) {
			starCopyKeys = Arrays.asList(followupKeys.split(","));
		}

		String fileName = System.currentTimeMillis() + "_copypending" + ".xml";
		File file = null;
		FileOutputStream fos = null;

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element element = document.createElement("rbt");
			document.appendChild(element);

			Element timestampElement = document.createElement("timestamp");
			timestampElement.setAttribute("time", new Date().toString());
			element.appendChild(timestampElement);

			String countryPrefix = getParamAsString("COMMON", "COUNTRY_PREFIX",
					"91");

			for (int i = 0; i < context.length; i++) {
				boolean send = false;
				String keypressed = null;
				String extraInfoStr = context[i].extraInfo();
				HashMap<String, String> viralInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfoStr);
				String wavFileName = context[i].clipID();
				String vstWavFileName = wavFileName;
				String serviceId = null;
				String classString = null;
				String chargeClass = m_copyClassType;
				
				StringBuffer wavFileBuf = new StringBuffer();
				StringBuffer catTokenBuf = new StringBuffer();
				StringBuffer catNameBuf = new StringBuffer();
				StringBuffer classTypeBuffer = new StringBuffer();
				StringBuffer statusBuf = new StringBuffer();
				StringBuffer setForCallerbuf = new StringBuffer();
				StringBuffer vrbtBuf = new StringBuffer();
				boolean isVirtualNo= false;
				
				classTypeBuffer.append(chargeClass);
				int categoryId = getClipCopyDetails(
						vstWavFileName, wavFileBuf,
						catTokenBuf, catNameBuf, classTypeBuffer, statusBuf,
						setForCallerbuf, isVirtualNo, vrbtBuf);
				Category category=new Category(); 
				if (viralInfoMap != null
						&& viralInfoMap.containsKey(KEYPRESSED_ATTR)) {
					keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
					for (String key : starCopyKeys) {
						if (keypressed.indexOf(key) != -1)
							send = true;
					}

				}
				if (!send) {
					continue;
				}
				if (wavFileName == null)
					continue;

				StringTokenizer tokenizer = new StringTokenizer(wavFileName,
						":");
				if (tokenizer.hasMoreTokens())
					wavFileName = tokenizer.nextToken();

				Clip clip = getClipRBT(wavFileName);
				if (clip == null || clip.getClipPromoId() == null)
					continue;

				String subStatus = "active";
				Subscriber sub = rbtConnector.getSubscriberRbtclient()
						.getSubscriber(context[i].callerID(), "GATHERER");
				String status = sub.getStatus();
				if (status != null){
					if ( WebServiceConstants.ACTIVE.equalsIgnoreCase(status)) {
						subStatus = WebServiceConstants.ACTIVE;
					}else if(WebServiceConstants.ACT_PENDING.equalsIgnoreCase(status)
							|| WebServiceConstants.ACT_ERROR.equalsIgnoreCase(status)){
						subStatus = WebServiceConstants.ACT_PENDING;
					}else if(WebServiceConstants.DEACT_PENDING.equalsIgnoreCase(status)
							|| WebServiceConstants.DEACT_ERROR.equalsIgnoreCase(status)){
						subStatus = WebServiceConstants.DEACT_PENDING;
					}else if(WebServiceConstants.SUSPENDED.equalsIgnoreCase(status)){
						subStatus = WebServiceConstants.SUSPENDED;
					}else if(WebServiceConstants.GRACE.equalsIgnoreCase(status)){
						subStatus = WebServiceConstants.GRACE;
					}else {
						subStatus = WebServiceConstants.DEACTIVE;
					}
				
				}	

				Element requestElement = document.createElement("request");
				requestElement.setAttribute("msisdn",
						countryPrefix + context[i].callerID());
				category = RBTCacheManager.getInstance().getCategory(
						categoryId);
				// Modified for VD-107130
				if (null != category
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(category.getCategoryTpe())) {
					requestElement.setAttribute("promoid",
							category.getCategoryPromoId());
				} else {
					requestElement.setAttribute("promoid",
							clip.getClipPromoId());
				}
				//Modifications Ended for VD-107130
				if (keypressed != null) {
					requestElement.setAttribute("keypressed", keypressed);
				}
				requestElement.setAttribute("time", context[i].sentTime() + "");
				requestElement.setAttribute("status", subStatus);

				if (classTypeBuffer.toString().trim().length() > 0) {
					chargeClass = classTypeBuffer.toString().trim();
				}
				chargeClass = rbtDBManager.getChargeClass(context[i].callerID(), sub.getCircleID(),
						clip.getClipNameWavFile(), chargeClass, false,
						categoryId, 7,
						null, null,
						"COPY", null);
				SelectionRequest selectionRequest = new SelectionRequest(context[i].callerID());
				selectionRequest.setChargeClass(chargeClass);
				selectionRequest.setCategoryID(String.valueOf(categoryId));
				selectionRequest.setClipID(String.valueOf(clip.getClipId()));				
				com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClassObj = RBTClient.getInstance().getNextChargeClass(selectionRequest);
				if (chargeClassObj != null) {
					chargeClass = chargeClassObj.getChargeClass();
				}	

				boolean isActAndSelRequest = false;
				boolean isActRequest = false;
				boolean isSelRequest = true;
				//RBT-15987	VF IN:: changed for tnb user upgrade
				if (!isSubActive(sub) || (isSubActive(sub) &&  m_rbtCopyLikeUtils.isTNBuser(sub.getSubscriptionClass()))) {
					isActAndSelRequest = true;
				}
				String subClass = getParamAsString("GATHERER", "COPY_SUB_CLASS",
						"COPY");
				
				// If user is not deactive overriding the subscription class
				if (sub != null
						&& sub.getSubscriptionClass() != null
						&& !sub.getStatus().equals(
								WebServiceConstants.DEACTIVE)) {
					subClass = sub.getSubscriptionClass();
				}
				
				//*** Added for tnb upgraded subscription class
				if (subClass != null
						&&  m_rbtCopyLikeUtils.isTNBuser(sub.getSubscriptionClass())) {
					ArrayList<String> tnbUpgradeSubClassLst = DBConfigTools
							.getParameter("COMMON",
									"TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO",
									",");
					ArrayList<String> tnbFreeChargeClass = DBConfigTools
							.getParameter("DAEMON", "TNB_FREE_CHARGE_CLASS",
									ConstantsTools.FREE, ",");

					if (tnbFreeChargeClass.contains(chargeClass)
							|| chargeClass.startsWith(ConstantsTools.FREE)) {
						// Free Selection or Download Callback
						logger.debug("Free selection classType: " + chargeClass
								+ " tnbFreeChargeClass: " + tnbFreeChargeClass);
					} else {
						for (String tnbUpgradeSubClass : tnbUpgradeSubClassLst) {
							String[] split = tnbUpgradeSubClass.split("\\:");
							if (split == null || split.length != 2) {
								continue;
							}
							if (subClass.equalsIgnoreCase(split[0])) {
								subClass = split[1];
								break;
							}
						}
					}
				}
				
				///*****/
				
				serviceId = DoubleConfirmationConsentPushThread
						.getServiceValue("SERVICE_ID",
								subClass, chargeClass,
								sub.getCircleID(), isActRequest, isSelRequest,
								isActAndSelRequest);
				classString = DoubleConfirmationConsentPushThread
						.getServiceValue("SERVICE_CLASS",
								subClass, chargeClass,
								sub.getCircleID(), isActRequest, isSelRequest,
								isActAndSelRequest);

				requestElement.setAttribute("serviceId", serviceId);
				requestElement.setAttribute("class", classString);
				requestElement.setAttribute("circle", sub.getCircleID());
				
				element.appendChild(requestElement);

			}
			// DOMSource domSource = new DOMSource(document);
			// StringWriter writer = new StringWriter();
			// StreamResult result = new StreamResult(writer);
			// TransformerFactory transformerFactory =
			// TransformerFactory.newInstance();
			// Transformer transformer = transformerFactory.newTransformer();
			// transformer.transform(domSource, result);

			String xml = XMLUtils.getStringFromDocument(document);

			logger.info("copyPending file path >"
					+ getParamAsString("COPY_PENDING_FILE_PATH"));
			file = new File(getParamAsString("COPY_PENDING_FILE_PATH")
					+ File.separator + fileName);
			logger.info("file absolute path >" + file.getAbsolutePath());

			byte[] bytes = xml.getBytes();
			int offset = 0;
			int length = 8;
			fos = new FileOutputStream(file);
			while (offset < bytes.length) {
				fos.write(bytes, offset, length);
				offset = offset + 8;
				if ((offset + length) >= bytes.length)
					length = bytes.length - offset;
			}

			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(getParamAsString("COPY_PENDING_UPLOAD_URL"));
			httpParameters.setUseProxy(getParamAsBoolean(
					"COPY_PENDING_USE_PROXY", "FALSE"));
			httpParameters
					.setProxyHost(getParamAsString("COPY_PENDING_PROXY_HOST"));
			httpParameters.setProxyPort(getParamAsInt(
					"COPY_PENDING_PROXY_PORT", 80));
			httpParameters.setSoTimeout(getParamAsInt(
					"COPY_PENDING_CONNECTION_TIMEOUT", 15000));
			httpParameters.setConnectionTimeout(getParamAsInt(
					"COPY_PENDING_CONNECTION_TIMEOUT", 15000));

			HashMap<String, File> fileParams = new HashMap<String, File>();
			fileParams.put("xml", file);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(
					httpParameters, null, fileParams); // Check with Response
			// String

			logger.info("Response code >" + httpResponse.getResponseCode());
			logger.info("Response  >" + httpResponse.getResponse());

			if (httpResponse != null
					&& httpResponse.getResponseCode() == 200
					&& httpResponse.getResponse().trim()
							.equalsIgnoreCase("200"))
				return true;

		} catch (ParserConfigurationException e) {
			logger.error("", e);
		} catch (Exception e) {
			logger.error("", e);
		} catch (Error e) {
			logger.error("", e);
			throw e;
		} finally {
			// if(file != null && file.exists())
			// file.delete();
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e) {
			}
		}
		return false;
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

	public void initTransactionFile() {
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("CALLED");
		headers.add("CALLER");
		headers.add("SONG");
		headers.add("CATEGORY");
		headers.add("COPY_TIME");
		headers.add("CALLER_TYPE");
		headers.add("CALLER_SUBSCRIBED_AT_COPY");
		headers.add("COPY_DONE");
		headers.add("SMS_TYPE");
		headers.add("KEY_PRESSED");
		headers.add("COPY_TYPE");
		headers.add("COPY_MODE");
		copyTransactionWriter = new TransFileWriter(m_transDir, "COPY_TRANS",
				headers);
	}

	public void initCopyStats() {
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("MESSAGE");
		copyStatsWriter = new TransFileWriter(m_copyStats, "COPY_STATS",
				headers);
	}

	static public boolean writeTrans(String subid, String callerID,
			String song, String cat, String req_time, String type,
			String isSubscribed, String success, String smsType,
			String keyPressed, String copyType, String confMode) {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("CALLED", subid);
		h.put("CALLER", callerID);
		h.put("SONG", song);
		h.put("CATEGORY", cat);
		h.put("COPY_TIME", req_time);
		h.put("CALLER_TYPE", type);
		h.put("CALLER_SUBSCRIBED_AT_COPY", isSubscribed);
		h.put("COPY_DONE", success);
		h.put("SMS_TYPE", smsType);
		h.put("KEY_PRESSED", keyPressed);
		h.put("COPY_TYPE", copyType);
		h.put("COPY_MODE", confMode);
		logger.info("h=" + h);
		if (copyTransactionWriter != null) {
			copyTransactionWriter.writeTrans(h);
			return true;
		}

		return false;
	}

	public boolean writeCopyStats(String message) {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("MESSAGE", message);

		if (copyStatsWriter != null) {
			copyStatsWriter.writeTrans(h);
			return true;
		}

		return false;
	}

	public boolean isBuddyNetUsers(String subscriberId, Subscriber sub) {
		boolean returnFlag = false;
		if (subscriberId != null) {
			if (m_redirectBuddyNet) {
				try {
					HttpParameters httpParameters = new HttpParameters();
					// http://:PORT/mca_buddynet_det/GetBuddyDet.do?platform=CRBT&mdn=9241034453&chain=TC06OT
					// m_buddynetUrl
					String url = m_buddynetUrl + "&mdn=" + subscriberId.trim();
					String wds = null;
					String wdsFinal = null;
					if (sub != null) {
						wds = sub.getOperatorUserInfo();
						if (wds == null) {
							HashMap<String, String> extraInfoStr = sub
									.getUserInfoMap();
							if (extraInfoStr != null
									&& extraInfoStr.size() > 0
									&& extraInfoStr
											.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)) {
								wds = extraInfoStr
										.get(EXTRA_INFO_WDS_QUERY_RESULT);
							}
						}
						if (wds != null) {
							wds = wds.trim();
							logger.info("wds==" + wds);
							if (wds.indexOf("|") != -1) {
								StringTokenizer st = new StringTokenizer(wds,
										"#");
								int count = 0;
								while (st.hasMoreTokens()) {
									++count;
									String temp = st.nextToken();
									logger.info("temp==" + temp);
									if (count == 11) {
										wdsFinal = temp;
										logger.info("wdsFinal==" + wdsFinal);
										break;
									}
								}
							}
							if (wdsFinal != null) {
								wdsFinal = wdsFinal.trim();
								url = url + "&chain=" + wdsFinal;
							} else {
								url = url + "&chain=";
							}
						}

					}
					if (wdsFinal == null) {
						url = url + "&chain=";
					}
					logger.info("url  >" + url);
					httpParameters.setUrl(url);
					httpParameters.setUseProxy(m_buddynetUseProxy);
					httpParameters.setProxyHost(m_buddynetProxyHost);
					httpParameters.setProxyPort(m_buddynetProxyPort);
					httpParameters.setSoTimeout(15000);
					httpParameters.setConnectionTimeout(15000);
					HttpResponse httpResponse = RBTHttpClient
							.makeRequestByPost(httpParameters, null, null); // Check
					// with
					// Response
					// String
					if (httpResponse != null) {
						logger.info("Response code >"
								+ httpResponse.getResponseCode());
						logger.info("Response  >" + httpResponse.getResponse());
					} else {
						logger.info("httpResponse==null");
					}
					if (httpResponse != null
							&& httpResponse.getResponseCode() == 200
							&& httpResponse.getResponse() != null) {
						String responseStr = httpResponse.getResponse().trim();
						if (responseStr != null) {
							if (responseStr.indexOf("|") != -1) {
								StringTokenizer st = new StringTokenizer(
										responseStr, "|");
								int count = 0;
								String buddyNetRes = null;
								while (st.hasMoreTokens()) {
									String temp = st.nextToken();
									++count;
									if (count == 3) {
										buddyNetRes = temp;
										break;
									}
								}
								if (buddyNetRes != null
										&& !buddyNetRes.trim()
												.equalsIgnoreCase("")
										&& !buddyNetRes.trim()
												.equalsIgnoreCase("null")
										&& buddyNetRes.trim().indexOf(";") != -1) {
									logger.info("^^^^^returnFlag=="
											+ returnFlag);
									returnFlag = true;
								}
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("returnFlag==" + returnFlag);
		return returnFlag;
	}

	public void initBuddyNetUrlConfig() {
		String urlInfoString = getParamAsString("BUDDY_NET_URL");
		if (urlInfoString != null) {
			StringTokenizer strTokenizer = new StringTokenizer(urlInfoString,
					",");
			if (strTokenizer.hasMoreTokens()) {
				String token = strTokenizer.nextToken();
				m_redirectBuddyNet = (token != null && (token
						.equalsIgnoreCase("true") || token
						.equalsIgnoreCase("on")));
			}

			// http://:PORT/mca_buddynet_det/GetBuddyDet.do?platform=CRBT
			if (strTokenizer.hasMoreTokens())
				m_buddynetUrl = strTokenizer.nextToken();
			if (strTokenizer.hasMoreTokens())
				m_buddynetUseProxy = strTokenizer.nextToken().trim()
						.equalsIgnoreCase("true");
			if (strTokenizer.hasMoreTokens())
				m_buddynetProxyHost = strTokenizer.nextToken().trim();
			try {
				if (strTokenizer.hasMoreTokens())
					m_buddynetProxyPort = Integer.parseInt(strTokenizer
							.nextToken().trim());
			} catch (Exception e) {
				m_buddynetProxyPort = -1;
			}
		}
	}

	public void initNationalUrlConfig() {
		String urlInfoString = getParamAsString("REDIRECT_NATIONAL");
		if (urlInfoString != null) {
			StringTokenizer strTokenizer = new StringTokenizer(urlInfoString,
					",");
			if (strTokenizer.hasMoreTokens()) {
				String token = strTokenizer.nextToken();
				m_redirectNational = (token.equalsIgnoreCase("true") || token
						.equalsIgnoreCase("on"));
			}
			if (strTokenizer.hasMoreTokens())
				m_nationalUrl = strTokenizer.nextToken();
			if (strTokenizer.hasMoreTokens())
				m_nationalUseProxy = strTokenizer.nextToken().trim()
						.equalsIgnoreCase("true");
			if (strTokenizer.hasMoreTokens())
				m_nationalProxyHost = strTokenizer.nextToken().trim();
			try {
				if (strTokenizer.hasMoreTokens())
					m_nationalProxyPort = Integer.parseInt(strTokenizer
							.nextToken().trim());
			} catch (Exception e) {
				m_nationalProxyPort = -1;
			}
		}
	}

	public void initNonOnmobileUrlConfig() {
		String urlInfoString = getParamAsString("NON_ONMOBILE_URL");
		if (urlInfoString != null) {
			StringTokenizer strTokenizer = new StringTokenizer(urlInfoString,
					",");
			if (strTokenizer.hasMoreTokens())
				m_nonOnmobileUrl = strTokenizer.nextToken();
			if (strTokenizer.hasMoreTokens())
				m_nonOnmobileUseProxy = strTokenizer.nextToken().trim()
						.equalsIgnoreCase("true");
			if (strTokenizer.hasMoreTokens())
				m_nonOnmobileProxyHost = strTokenizer.nextToken().trim();
			try {
				if (strTokenizer.hasMoreTokens())
					m_nonOnmobileProxyPort = Integer.parseInt(strTokenizer
							.nextToken().trim());
			} catch (Exception e) {
				m_nonOnmobileProxyPort = -1;
			}
		}
	}

	public void initCrossOperatorUrlConfig() {
		String urlInfoString = getParamAsString("RDC_URL");
		if (urlInfoString != null) {
			StringTokenizer strTokenizer = new StringTokenizer(urlInfoString,
					",");
			if (strTokenizer.hasMoreTokens()) {
				String token = strTokenizer.nextToken();
				m_redirectCrossOperator = (token.equalsIgnoreCase("true") || token
						.equalsIgnoreCase("on"));
			}
			if (strTokenizer.hasMoreTokens())
				m_crossOperatorUrl = strTokenizer.nextToken();
			if (strTokenizer.hasMoreTokens())
				m_crossOperatorUseProxy = strTokenizer.nextToken().trim()
						.equalsIgnoreCase("true");
			if (strTokenizer.hasMoreTokens())
				m_crossOperatorProxyHost = strTokenizer.nextToken().trim();
			try {
				if (strTokenizer.hasMoreTokens())
					m_crossOperatorProxyPort = Integer.parseInt(strTokenizer
							.nextToken().trim());
			} catch (Exception e) {
				m_crossOperatorProxyPort = -1;
			}
		}
	}

	public boolean isValidSub(Subscriber subscriber) {
		return subscriber.isValidPrefix();
	}

	private String getEncodedUrlString(String param) {
		String ret = null;
		try {
			ret = m_urlEncoder.encode(param, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}
	//RBT-14671 - # like
	public String getSMSText(String type, String subType, String defaultValue,
			String language, String circleId) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				type, subType, language, circleId);
		if (smsText != null)
			return smsText;
		else
			return defaultValue;
	}

	public int getIntegerValue(String countStr) {
		int count = 0;
		if (countStr == null)
			return count;
		try {
			count = Integer.parseInt(countStr);
		} catch (Exception e) {
			count = 0;
		}
		return count;
	}

	public boolean amIAlive() {
		return this.isAlive();
	}

	public String getActivity() {
		return null;
	}

	public String getLoad() {
		String loadStr = "Gatherer's run Count is  " + loopCount;
		long curTime = System.currentTimeMillis();
		long diff = curTime - copyThreadStartTime.getTime();
		if (loopCount > 0)
			loadStr += ". Avg loopTime is " + (diff / loopCount);
		return loadStr;
	}

	public String getStatus() {
		String statusStr = null;
		if (isSleeping)
			statusStr = "Is in Sleeping mode. Sleep interval is "
					+ sleepInterval + " ms.";
		else
			statusStr = "Gatherer thread is not sleeping.";
		return statusStr;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String name) {
		threadName = name;
	}

	public String getCircleId(String subscriberid) {
		if (subscriberid == null || subscriberid.length() < 1)
			return null;
		for (int i = 1; i < subscriberid.length(); i++) {
			String prefix = subscriberid.substring(0, i);
			if (m_operatorPrefixArrayListMap.containsKey(prefix))
				return m_operatorPrefixArrayListMap.get(prefix);
		}
		return null;
	}

	public boolean sameSongCopy(ViralSMSTable v1, String clipId2) {
		if (v1.clipID() == null && clipId2.equals(""))
			return true;
		if (v1.clipID() == null || clipId2.equals(""))
			return false;
		if (v1.clipID().indexOf("MISSING") != -1
				|| clipId2.indexOf("MISSING") != -1)
			return false;
		String clipid1 = v1.clipID();
		StringTokenizer stk1 = new StringTokenizer(clipid1, ":");
		StringTokenizer stk2 = new StringTokenizer(clipId2, ":");
		if (stk1.nextToken().equals(stk2.nextToken()))
			return true;
		return false;
	}

	private String getClassTypeFromVrbtCatId(String catId) {
		
		if(m_vrbtCatIdSubSongSrvKeyMap != null) {
			List<String> list = m_vrbtCatIdSubSongSrvKeyMap.get(catId);
			if(list != null) {
				//return classType
				return list.get(1);
			}
		}
		return null;
	}
	
	private String getSubclassFromVrbtCatId(String catId) {
		
		if(m_vrbtCatIdSubSongSrvKeyMap != null) {
			List<String> list = m_vrbtCatIdSubSongSrvKeyMap.get(catId);
			if(list != null) {
				//return subClass
				return list.get(0);
			}
		}
		return null;
	}
	
	private String getP2PModeForUser(Subscriber subscriber, String mode, String type, Subscriber called) {
		
		//Call the API to find the Operator Type and Get KEY
		//4 Operator Types * Inactive (2), Active (1).
		String operatorCircle = getOperaterTypeWithCircleId(called);
		logger.info("Operator circle : " + operatorCircle );
		boolean isVirtualNumber = false ; 
		String virtualNumberConfig = RBTParametersUtils.getParamAsString(
				"VIRTUAL_NUMBERS", called.getSubscriberID(), null);
		if (virtualNumberConfig != null) {
			String circleID = null;
			String[] tokens = virtualNumberConfig.split(",");
			if (tokens.length >= 3)
				circleID = tokens[2];
			if (subscriber.getCircleID() != null
					&& subscriber.getCircleID().equalsIgnoreCase(circleID)) {
				// subscriber called a virtual number and
				// belongs to the same circle.
				isVirtualNumber = true ;
			}
		}
		
		if ((subscriber == null ||(subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER) || subscriber
				.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)))
				&& getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_"+operatorCircle) != null && !isVirtualNumber ) {
			mode = getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_"+operatorCircle);
			logger.info("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_"+operatorCircle +" : " + mode );
			String p2pOptInCopy = getParamAsString("ACTIVATION_FOR_P2P_MODE_FOR_OPTIN_COPY_"+operatorCircle);
			logger.info("ACTIVATION_FOR_P2P_MODE_FOR_OPTIN_COPY_"+operatorCircle +" : " + mode );
			if (p2pOptInCopy != null) {
				if (type.equalsIgnoreCase(COPYCONFIRMED))
					mode = p2pOptInCopy;
			}
			return mode;
        }
		
		if ((subscriber == null ||(subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER) || subscriber
				.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)))
				&& getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY") != null) {
			mode = getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY");
			
			String p2pOptInCopy = getParamAsString("ACTIVATION_FOR_P2P_MODE_FOR_OPTIN_COPY");
			if (p2pOptInCopy != null) {
				if (type.equalsIgnoreCase(COPYCONFIRMED))
					mode = p2pOptInCopy;
			}
        }
		else if(getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_ACT_USER_"+operatorCircle) != null && !isVirtualNumber){
			mode = getParamAsString("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_ACT_USER_"+operatorCircle);
			logger.info("MODE_OF_ACTIVATION_FOR_P2P_IN_COPY_ACT_USER_"+operatorCircle +" : " + mode );
			return mode;
			
		}
		return mode;
	}
	
	private String checkContentToSendSMS(Subscriber callerSub,Clip clip,String language){
		String strSmsText = null;
		if(callerSub == null || clip == null){
			return null;
		}
		boolean hasSetSameContent = hasSetSameContent(callerSub,
				clip);
		boolean hasDownloadLimitReached = false;
		boolean isDownloadPresent = false;
		if(getParamAsBoolean("COMMON","ADD_TO_DOWNLOADS","FALSE")){
			  hasDownloadLimitReached = hasDownloadLimitReached(callerSub,
					clip);
			  isDownloadPresent = isDownloadPresent(callerSub,
					clip);
		}
		
       if (hasSetSameContent) {
			// check same content exist in selection or not
			// send optin sms along with content offer price to
			// user to make this content for everybody.
			strSmsText = getSMSText("GATHERER",
					"OPT_IN_COPY_CONTENT_SET",
					m_copyContentSetClipSMS, language,
					callerSub.getCircleID());
			
			logger.info("Same content is Set, NON default content and caller is active "
					+ "and caller has the same content."
					+ " So, sending OPT_IN_COPY_CONTENT_SET");
			
		} else if(hasDownloadLimitReached){
			strSmsText = getSMSText("GATHERER",
					"OPT_IN_COPY_CONTENT_MAX_DOWNLOAD_LIMIT",
					m_copyContentMaxDwnLimitSMS, language,
					callerSub.getCircleID());
			logger.info("Max Download Limit Reached ...SMS Text = "+strSmsText);
			
		} else if(isDownloadPresent){
			strSmsText = getSMSText("GATHERER",
					"OPT_IN_CONFIRMATION_DWN_PRESENT_SEL_SMS",
					m_optInConfirmationDWNSelSMS, language,
					callerSub.getCircleID());
			logger.info("Download is already present.Sending sms to make selection only .SMS Text = "+strSmsText);
		}
       
       return strSmsText;
	}
	
	public static void main(String args[]) throws Exception{
		RBTCopyProcessor rbtCopyProcessor = new RBTCopyProcessor(null);
		Subscriber callerSub = RBTConnector.getInstance().getSubscriberRbtclient().getSubscriber("3446872720","GATHERER");
		Subscriber calledSub = RBTConnector.getInstance().getSubscriberRbtclient().getSubscriber("5454778173","GATHERER");
		Clip clip = RBTCacheManager.getInstance().getClip(1288123);
		rbtCopyProcessor.checkConditionsToSendSms(callerSub, calledSub, "DEFAULT", clip, "eng");
	}
	
	//RBT-12581
	public static void deleteDownloadsForExpiredContent(String subscriberID,Clip clip,Category category) {
		HashMap<String, String> whereClauseMap = new HashMap<String, String>();
		if(clip!=null || category!=null) {
			if (clip != null) {
				whereClauseMap.put("SUBSCRIBER_WAV_FILE",
						clip.getClipRbtWavFile());
				SubscriberStatus subSatus = RBTDBManager.getInstance()
						.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
								subscriberID, whereClauseMap);

				if (subSatus == null) {
					boolean removeFromDownload =  RBTDBManager.getInstance()
							.removeSubscriberDownloadBySubIdAndWavFileAndCatId(
									subscriberID,
									clip.getClipRbtWavFile(),
									-1);
					if (removeFromDownload) {
						logger.info("Download deleted.");
					}
				}

			} else if (category != null) {
				whereClauseMap.put("CATEGORY_ID",
						String.valueOf(category.getCategoryId()));
				SubscriberStatus subSatus =  RBTDBManager.getInstance()
						.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
								subscriberID, whereClauseMap);
				if (subSatus == null) {
					boolean removeFromDownload =  RBTDBManager.getInstance()
							.removeSubscriberDownloadBySubIdAndWavFileAndCatId(
									subscriberID, null,
									category.getCategoryId());
					if (removeFromDownload) {
						logger.info("Download deleted.");
					}
				}

			}
		}
	}
	//RBT-12993: Multiple SMS in case of any key copy
	//If selection success sms is not to be sent, an sms config with "_ANY_KEY_COPY" suffixed to subType has to be there with value set as "NA".
	private String getSuccessSMSText(String type, String subType, String defaultValue, String language, boolean isAnyKeyCopyOptIn) {
		if (isAnyKeyCopyOptIn) {
			String localSubType = subType + "_ANY_KEY_COPY";
			String smsText = RBTCopyLikeUtils.getSMSText(type, localSubType, null, language);
			if (smsText != null) {
				logger.debug("Any key copy success sms configured. Returning the same. Type: " + type + ", subType: "+ localSubType + ", language: " + language + ", smsText: " + smsText);
				return smsText;
			}
			
		}//RBT-14671 - # like
		return RBTCopyLikeUtils.getSMSText(type, subType, defaultValue, language);	
	}
	
	private static boolean isFreemiumUpgradeRequest(Subscriber subscriber, Clip clip,
			Category category) {
        
		boolean isFreemiumUpgrdReq = false;
		if(subscriber == null){
			return isFreemiumUpgrdReq;
		}
		com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = getNextChargeClass(
				subscriber.getSubscriberID(), clip, category);
		logger.info("RBTCopyProcessor:: Next Charge Class for Freemium = "+chargeClass);
		if (chargeClass != null) {
//			String numMaxSel = m_FreemiumUpgradeChargeClass.get(chargeClass.getChargeClass());
			if (freemiumSubClassList.contains(subscriber.getSubscriptionClass())
					&& m_FreemiumUpgradeChargeClass.contains(chargeClass.getChargeClass())) {
				isFreemiumUpgrdReq = true;
			}
		}
		logger.info("RBTCopyProcessor:: isFreemiumUpgrdReq = "+isFreemiumUpgrdReq);
		return isFreemiumUpgrdReq;

	}
	
	private static com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass 
                 getNextChargeClass(String subscriberId, Clip clip, Category category) {

		String clipId = null;
		String categoryId = null;
		if (clip != null) {
			clipId = clip.getClipId() + "";
		}
		if (category != null) {
			categoryId = category.getCategoryId() + "";
		}
		SelectionRequest selectionRequestObj = new SelectionRequest(subscriberId);
		selectionRequestObj.setClipID(clipId);
		selectionRequestObj.setCategoryID(categoryId);
		com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient
				.getInstance().getNextChargeClass(selectionRequestObj);
		return chargeClass;

	} 
	
	private String getFreemiumBaseUpgrdAmt(String subscriberID, String mode) {
		String baseAmt = "0.0";
		try {
			com.onmobile.apps.ringbacktones.webservice.client.beans.Offer offer = getOffer(
					subscriberID, "SUB");
			String freemiumRentalPack = null;
			String extraInfo1 = null;
			if (offer != null) {
				freemiumRentalPack = offer.getSrvKey();
				baseAmt = offer.getAmount() + "";
			} else {
				freemiumRentalPack = RBTParametersUtils.getParamAsString("COMMON",
						"FREEMIUM_RENTAL_PACK", null);
				SubscriptionClass sClass = rbtConnector.getRbtGenericCache()
						.getSubscriptionClassByName(freemiumRentalPack);
				if (sClass != null) {
					baseAmt = sClass.getSubscriptionAmount();
				}
			}
		} catch (Exception ex) {
			logger.info("Exception while getting freemium upgradation amount ");
		}
		logger.info("Freemium Base Amount = "+baseAmt);
		return baseAmt;
	}
	
	private boolean isKeyConfigured(List<String> keys, String keyPressed) {
		for (String key : keys) {
			if (keyPressed.indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}
	
	private String promoteThroughMSearch(String url) {
		String responseText = "FAILURE";
		HttpParameters httpParameters = new HttpParameters(url);
		try {
			logger.info("RBT:: Promotiong URL " + url);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			int statusCode = httpResponse.getResponseCode();
			logger.info("RBT:: Promotiong URL status " + statusCode + " httpResponse: " + httpResponse);
			String responseStr = httpResponse.getResponse();
			if (statusCode == 200 && responseStr != null
					&& responseStr.trim().equalsIgnoreCase("success")) {
				responseText = "SUCCESS";
			} else {
				logger.error("Could not get proper response");
			}
		} catch (Exception e) {
			logger.error("RBT:: " + e.getMessage(), e);
			//
		}
		return responseText;
	}
	
	private String getOperaterTypeWithCircleId(Subscriber subscriber) {
		String circleId = subscriber.getCircleID();
		if (circleId != null) {
			if (circleId.equalsIgnoreCase("OPERATOR")) {
				return "NON_LOCAL_CIRCLE";
			} else if (circleId.equalsIgnoreCase("NON_ONMOBILE")) {
				return "COMVIVA_CIRCLE";
			} else if (circleId.equalsIgnoreCase("CROSS_OPERATOR ")) {
				return "CROSS_OPERATOR";
			} else {
				return "LOCAL_CIRCLE";
			}
		} else {
			return "CROSS_OPERATOR";

		}

	}
		private void handleBaseOfferNotFoundAndDwnldLimitReached(StringBuffer baseApiRes,
			Subscriber callerSub, String langugae) {
		if (null != baseApiRes
				&& WebServiceConstants.BASE_OFFER_NOT_AVAILABLE
						.equalsIgnoreCase(baseApiRes.toString())) {
			if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE"))
				sendSMSviaPromoTool(callerSub, RBTCopyLikeUtils.getSMSText(
						"GATHERER", "BASE_OFFER_NOT_AVAILABLE",
						m_noBaseOfferCopySMS, langugae));
			else
				sendSMS(callerSub, RBTCopyLikeUtils.getSMSText("GATHERER",
						"BASE_OFFER_NOT_AVAILABLE", m_noBaseOfferCopySMS,
						langugae));

		} else if (null != baseApiRes
				&& WebServiceConstants.PACK_DOWNLOAD_LIMIT_REACHED
						.equalsIgnoreCase(baseApiRes.toString())) {
			if (getParamAsBoolean("USE_DND_SMS_URL", "FALSE"))
				sendSMSviaPromoTool(callerSub, RBTCopyLikeUtils.getSMSText(
						"GATHERER", "PACK_DOWNLOAD_LIMIT_REACHED",
						m_copyContentMaxDwnLimitSMS, langugae));
			else
				sendSMS(callerSub, RBTCopyLikeUtils.getSMSText("GATHERER",
						"PACK_DOWNLOAD_LIMIT_REACHED", m_copyContentMaxDwnLimitSMS,
						langugae));

		}

	}
		
	private boolean isValidKeyPressed(String subscriberID, String keyPressed) {
		String circleID = null;
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(subscriberID, "COPY"));
		if (subscriberDetail != null)
			circleID = subscriberDetail.getCircleID();
		String type = null;
		boolean circleConfigPresent = false;
		boolean foundMatch = false;
		boolean isValidKeyPressed = false;

		if ((directCopyKeysMap != null || optinCopyKeysMap != null)
				&& keyPressed != null) {
			if (subscriberDetail.isValidSubscriber() && circleID != null) {
				if (directCopyKeysMap != null
						&& directCopyKeysMap.containsKey(circleID)) {

					circleConfigPresent = true;
					HashSet<String> hashSet = directCopyKeysMap.get(circleID);
					for (String key : hashSet) {
						if (keyPressed.indexOf(key) != -1) {
							type = "COPY";
							foundMatch = true;
							break;
						}
					}
				}
				if (!foundMatch && optinCopyKeysMap != null
						&& optinCopyKeysMap.containsKey(circleID)) {
					circleConfigPresent = true;
					HashSet<String> hashSet = optinCopyKeysMap.get(circleID);
					for (String key : hashSet) {
						if (keyPressed.indexOf(key) != -1) {
							type = "COPYSTAR";
							foundMatch = true;
							break;
						}
					}
				}
			}
		}
		if (!circleConfigPresent && type == null) {
			if (keyPressed != null && normalCopyKeys != null) {
				// RBT-10651
				boolean condition = false;
				logger.info("Service processor keyPressed is :" + keyPressed);
				String keySuffix = getParamAsString("COMMON",
						"COPY_KEY_SUFFIX", null);
				logger.info("keySuffix :" + keySuffix);

				for (String key : normalCopyKeys) {
					if (keySuffix != null && keySuffix.equalsIgnoreCase("true"))
						condition = keyPressed.startsWith(key);
					else
						condition = keyPressed.indexOf(key) != -1;
					logger.info("condition after updating: " + condition);
					if (condition)
						type = "COPY";
				}
			}
			if (type == null) {
				if (keyPressed != null && starCopyKey != null) {
					for (String key : starCopyKey) {
						if (keyPressed.indexOf(key) != -1)
							type = "COPYSTAR";
					}
				}
			}

			if (type == null && keyPressed != null && crossCopy != null) {
				for (String key : crossCopy) {
					if (keyPressed.indexOf(key) != -1) {
						type = CROSSCOPY;
						break;
					}
				}
			}// RBT-14671 - # like
			if (type == null && keyPressed != null && toLikeKeys != null) {
				for (String keyFromConf : toLikeKeys) {
					if (keyPressed
							.indexOf(keyFromConf.toString().toLowerCase()) != -1) {
						type = LIKE;
						break;
					}
				}
			}

		}
		
		logger.info("type for keypressed : " + keyPressed + " is " + type );
		if (type != null && !type.isEmpty()) {
			isValidKeyPressed = true;
		}

		return isValidKeyPressed;
	}
	
	private void initCopyKeys() {
		String normalCopyKeys = getParamAsString("COMMON",
				"CIRCLEWISE_NORMALCOPY_KEY", null);
		logger.info("parameter normalCopyKeys=" + normalCopyKeys);
		if (normalCopyKeys != null) {
			directCopyKeysMap = new HashMap<String, HashSet<String>>();
			// circle1:1,2,3;circle2:4,5
			String[] circleIdAndKeyPairs = normalCopyKeys.split(";");
			for (int i = 0; i < circleIdAndKeyPairs.length; i++) {
				String circleIdAndKeyPair = circleIdAndKeyPairs[i];
				String[] circleIdAndKeys = circleIdAndKeyPair.split(":");
				String circleId = circleIdAndKeys[0];
				String keys = circleIdAndKeys[1];
				String[] keyArray = keys.split(",");
				List<String> keyList = Arrays.asList(keyArray);
				HashSet<String> keySet = new HashSet<String>();
				keySet.addAll(keyList);
				directCopyKeysMap.put(circleId, keySet);
			}
		}
		logger.info("directCopyKeysMap=" + directCopyKeysMap);
		String optinCopyKeys = getParamAsString("COMMON",
				"CIRCLEWISE_STARCOPY_KEY", null);
		logger.info("parameter optinCopyKeys=" + optinCopyKeys);
		if (optinCopyKeys != null) {
			optinCopyKeysMap = new HashMap<String, HashSet<String>>();
			// circle1:1,2,3;circle2:4,5
			String[] circleIdAndKeyPairs = optinCopyKeys.split(";");
			for (int i = 0; i < circleIdAndKeyPairs.length; i++) {
				String circleIdAndKeyPair = circleIdAndKeyPairs[i];
				String[] circleIdAndKeys = circleIdAndKeyPair.split(":");
				String circleId = circleIdAndKeys[0];
				String keys = circleIdAndKeys[1];
				String[] keyArray = keys.split(",");
				List<String> keyList = Arrays.asList(keyArray);
				HashSet<String> keySet = new HashSet<String>();
				keySet.addAll(keyList);
				optinCopyKeysMap.put(circleId, keySet);
			}
		}
		logger.info("parameter optinCopyKeysMap=" + optinCopyKeysMap);

	}

	public SelectionRequestBean prepareSelectionObjforTransformIndiaUser(
			SelectionRequestBean selBean, Clip clip, StringBuffer selBy,
			StringBuffer cosId, StringBuffer actBy) {
		SelectionRequestBean selObj = selBean;
		String cosType = RBTParametersUtils.getParamAsString("COMMON",
				"TRANSFORM_INDIA_COS_TYPE", null);
		String selectedBy = RBTParametersUtils.getParamAsString("COMMON",
				"TRANSFORM_INDIA_SEL_BY", null);
		String actvivatedBy = RBTParametersUtils.getParamAsString("COMMON",
				"TRANSFORM_INDIA_ACT_BY", null);
		Subscriber sub = m_rbtCopyLikeUtils.getSubscriber(selBean
				.getSubscriberId());
		String circleID = sub.getCircleID();
		boolean prepaid = getParamAsString("GATHERER", "DEFAULT_SUBTYPE", "pre")
				.equalsIgnoreCase("pre");
		List<CosDetails> cos = CacheManagerUtil.getCosDetailsCacheManager()
				.getCosDetailsByCosType(cosType, circleID, prepaid ? "y" : "n");
		if (cos != null && cos.size() > 0) {
			cosId.append(cos.get(0).getCosId());
			selObj.setChargeClass(cos.get(0).getFreechargeClass());
			selObj.setSubscriptionClass(cos.get(0).getSubscriptionClass());
		}
		selBy.append(selectedBy);
		actBy.append(actvivatedBy);
		return selObj;
	}
}