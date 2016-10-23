package com.onmobile.apps.ringbacktones.daemons;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.onmobile.apps.ringbacktones.Gatherer.RBTRtoExecutor;
import com.onmobile.apps.ringbacktones.Gatherer.RBTSATPushExecutors;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationDaemon;
import com.onmobile.apps.ringbacktones.daemons.genericftp.FTPCampaignManager;
import com.onmobile.apps.ringbacktones.daemons.grbt.CopyDataCollector;
import com.onmobile.apps.ringbacktones.daemons.grbt.GRBTServerUploader;
import com.onmobile.apps.ringbacktones.daemons.interfaces.PlayerThread;
import com.onmobile.apps.ringbacktones.daemons.multioperator.RBTMultiOpCopyContentReslover;
import com.onmobile.apps.ringbacktones.daemons.multioperator.RBTMultiOpCopyPollOperator;
import com.onmobile.apps.ringbacktones.daemons.nametunes.NameTunesNewRequestThread;
import com.onmobile.apps.ringbacktones.daemons.nametunes.NameTunesProcessingThread;
import com.onmobile.apps.ringbacktones.daemons.reliance.RelianceDaemonBootstrap;
import com.onmobile.apps.ringbacktones.daemons.reminder.ReminderDaemon;
import com.onmobile.apps.ringbacktones.daemons.viralwhitelist.ViralWhiteListDaemon;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SubscriptionClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.hunterFramework.DummyManagedObject;
import com.onmobile.apps.ringbacktones.hunterFramework.RBTVersionManagedObject;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.logger.SMHitLogger;
import com.onmobile.apps.ringbacktones.logger.TransLogForSelection;
import com.onmobile.apps.ringbacktones.logging.SMDaemonLogger;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTSMDaemonService;
import com.onmobile.apps.ringbacktones.rbt2.service.impl.RBTSMDaemonManagerImpl;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.snmp.agentx.client.ManagedObjectCallback;

public class RBTDaemonManager extends Thread implements iRBTConstant
{
	//public static Stopwatch smBaseActStopwatch = SimonManager.getStopwatch("smBaseAct");
	//public static Stopwatch smBaseDctStopwatch = SimonManager.getStopwatch("smBaseDct");
	//public static Stopwatch smSelActStopwatch = SimonManager.getStopwatch("smSelAct");
	//public static Stopwatch smSelDctStopwatch = SimonManager.getStopwatch("smSelDct");
	
	private static Logger logger = Logger.getLogger(RBTDaemonManager.class);
	public static RBTDaemonManager m_rbtDaemonManager = null;
	protected static RBTDBManager rbtDBManager = null;
	protected static RBTCacheManager rbtCacheManager = null;
	protected static ParametersCacheManager m_rbtParamCacheManager = null;
	protected static ChargeClassCacheManager m_rbtChargeClassCacheManager = null;
	protected static SubscriptionClassCacheManager m_rbtSubClassCacheManager = null;
	protected static final String SM_URL_FAILURE = "SM_URL_FAILURE";
	protected static final String FAILED = "FAILED";
	protected static final String RESPONSE = "RESPONSE";
	protected static final String REFID = "INT_REF_ID";
	private static final String REQUEST_TYPE = "REQUEST_TYPE";
	private static final String URL = "URL";
	protected static final String REFID_CREATED = "REF_CREATED";
	public static final String DAEMON = "DAEMON";
	protected static final String CONTENT_EXPIRED = "CONTENT_EXPIRED"; //RBT-14497 - Tone Status Check
	HttpClient m_httpClient = null;
	URLCodec m_urlEncoder = new URLCodec();
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	SimpleDateFormat actOrSelFailSdf = new SimpleDateFormat("yyyyMMdd");
	protected SimpleDateFormat m_timeSdf = new SimpleDateFormat("ddMMMyy");
	SimpleDateFormat m_clipExpirySdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
	public static ArrayList<String> m_lowPriorityModes = null;
	static String m_sdrWorkingDir = ".";
	static int m_sdrSize = 1000;
	static long m_sdrInterval = 24;
	static String m_sdrRotation = "size";
	static boolean m_sdrBillingOn = true;
	private String m_smsTextForAll = "All";
	boolean m_combinedCharging = false;
	private static boolean m_Continue = true;
	public static WriteDailyTrans m_writeTrans = null;
	protected static WriteDailyTrans smErrorCasesTrans = null;
	Integer statusCode = new Integer(-1);
	StringBuffer response = new StringBuffer();
	private String m_overrideShuffleSMS = "%S has been activated for %C , the service would be activate from %L to %T"; 
	protected List<String> supportedLangList = null;
	PlayerThread _playerThread = null;
	private String _playerClass = null;
	static ReminderDaemon reminderDaemon = null;
	static VodaPrismDaemon vodaPrismDaemon = null;
    static ClipStatusDaemon clipStatusDaemon = null;
    private RBTHttpClient rbtHttpClient = null;
	public static boolean isFcapsEnabled = false;
	private static Map<String, String> liteUpgradeChargeClassMap = null;
	public static List<ManagedObjectCallback> managedObjectsList = null;
	private Date m_lastRunTime = null;
	protected static List<String> shuffleCategoryTypesForSendingClipInfoList = null;
	// RBT-14301: Uninor MNP changes.
	protected static Map<String, String> circleIdToSiteIdMapping = null;
	//RBT-14497 - Tone Status Check
	private static boolean enable_Tone_Status_Check = false;
	
	public boolean getConfigValues() 
	{
		setName("RBTDaemonManager-Main");
		rbtCacheManager = RBTCacheManager.getInstance();
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();
		m_rbtChargeClassCacheManager = CacheManagerUtil.getChargeClassCacheManager();
		m_rbtSubClassCacheManager = CacheManagerUtil.getSubscriptionClassCacheManager();

		rbtDBManager = RBTDBManager.getInstance();

		_playerClass = getParamAsString("PLAYER_START_CLASS");
		m_smsTextForAll = getParamAsString("SMS", "SMS_TEXT_FOR_ALL", "ALL");
		m_lowPriorityModes = Tools.tokenizeArrayList(getParamAsString("LOW_PRIORITY_MODES"), null);
		m_combinedCharging = getParamAsBoolean("COMBINED_CHARGING", "FALSE");

		m_sdrWorkingDir = getParamAsString("DAEMON", "SUBMGR_SDR_WORKING_DIR", ".");

		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("REQUEST TYPE");
		headers.add("REQUEST URL");
		headers.add("REPONSE CODE");
		headers.add("RESPONSE STRING");
		headers.add("RESPONSE DELAY");
		headers.add("REQUEST TIME");
		
		smErrorCasesTrans = new WriteDailyTrans(m_sdrWorkingDir, "SM_ERROR_CASES", headers);
				
		supportedLangList = Arrays.asList(getParamAsString("COMMON", "SUPPORTED_LANGUAGES", "eng").split(","));
		initSnmpManagedObjects();
		
		SMDaemonPerformanceMonitor.startSMPerformanceMonitorDaemon();

		HttpPerformanceMonitor httpPerformanceMonitor = PerformanceMonitorFactory.newHttpPerformanceMonitor(
				"SMDaemon", "SMDaemonMonitor", PerformanceDataType.LONG, "Milliseconds");
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(200);
		httpParameters.setMaxHostConnections(200);
		httpParameters.setConnectionTimeout(getParamAsInt("SMDAEMON_TIMEOUT",6)*1000);
		httpParameters.setSoTimeout(getParamAsInt("SMDAEMON_TIMEOUT",6)*1000);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);
		rbtHttpClient = new RBTHttpClient(httpParameters);
		
		isFcapsEnabled = RBTParametersUtils.getParamAsBoolean("COMMON", "IS_FCAPS_ENABLED", "FALSE");
		
		String liteUpgradeChargeClassMapping = RBTParametersUtils.getParamAsString("DAEMON", "LITE_UPGRADE_CHARGE_CLASS_MAPPING", null);
		if (liteUpgradeChargeClassMapping != null)
		{
			liteUpgradeChargeClassMap = new HashMap<String, String>();
			List<String> mappingList = Arrays.asList(liteUpgradeChargeClassMapping.split(","));
			for (String map : mappingList)
			{
				String[] keyValue = map.split(":");
				liteUpgradeChargeClassMap.put(keyValue[0], keyValue[1]);
			}
		}

		String shuffleCategoryTypesForSendingClipInfoString = getParamAsString("SHUFFLE_CATEGORY_TYPES_FOR_SENDING_CLIP_INFO");
		logger.info("shuffleCategoryTypesForSendingClipInfoString: " + shuffleCategoryTypesForSendingClipInfoString);
		if (shuffleCategoryTypesForSendingClipInfoString != null) {
			shuffleCategoryTypesForSendingClipInfoList = Arrays.asList(shuffleCategoryTypesForSendingClipInfoString.split(","));
			logger.info("shuffleCategoryTypesForSendingClipInfoList: " + shuffleCategoryTypesForSendingClipInfoList);
		}
		// RBT-14301: Uninor MNP changes.
		circleIdToSiteIdMapping = MapUtils.convertToMap(
				CacheManagerUtil.getParametersCacheManager().getParameterValue(
						"COMMON", "CIRCLEID_TO_SITEID_MAPPING", null),
				";", ":", null);
		logger.info("circleIdToSiteIdMapping= " + circleIdToSiteIdMapping);
		//RBT-14497 - Tone Status Check
		enable_Tone_Status_Check = RBTParametersUtils.getParamAsBoolean(
				"DAEMON", "ENABLE_TONE_STATUS_CHECK", "false");
		logger.info("enable_Tone_Status_Check= " + enable_Tone_Status_Check);
		logger.info("Successfully loaded all the configuration");
		return true;
	}

	private void initSnmpManagedObjects()
	{
		try
		{
			managedObjectsList = new ArrayList<ManagedObjectCallback>();
			
			ResourceBundle resourceBundle = ResourceBundle.getBundle("snmp");
			
			String entityOIDStr = resourceBundle.getString("rbt_version_get_oid");
			logger.info("rbt_version_get_oid = "+entityOIDStr);
			if(entityOIDStr != null)
				managedObjectsList.add(new RBTVersionManagedObject(entityOIDStr));
			
			entityOIDStr = resourceBundle.getString("dummy_get_oid");
			logger.info("dummy_get_oid = "+entityOIDStr);
			if(entityOIDStr != null)
				managedObjectsList.add(new DummyManagedObject(entityOIDStr));
			
		}
		catch (Exception e)
		{
			logger.error("Exception initializing snmp get objects", e);
		}
	}

	/*
	 * Starts the player updater thread (daemon parameter : START_PLAYER_DAEMON),  gift thread (daemon parameter :  PROCESS_GIFT), gets the activation and selection requests to be processed. If downloads need to be 
	 * processed (PROCESS_DOWNLOADS) , processes opt in subscribers (PROCESS_OPT_IN), trial selections (PROCESS_TRIAL_SELECTIONS) , ugc procecssing (PROCESS_UGC_CREDIT)
	 * sets the next running time. 
	 * */
	public void run()
	{
		//new StopwatchThread().start();
		//Added for loading spring xml
		ApplicationContext context = new ClassPathXmlApplicationContext("bean_spring.xml");
		
		logger.info("Started run() method");
		if(getParamAsBoolean("START_SM_DAEMON", "TRUE"))
			createSMThreadPools();
		
		
		if(getParamAsBoolean("START_PLAYER_DAEMON","FALSE"))
			startPlayerUpdateThread();

		if(getParamAsBoolean("START_AD2C_DAEMON","FALSE"))
			new Ad2cDaemon().start();
		
		if(getParamAsBoolean("START_CONTENT_EXPIRY_DAEMON","FALSE"))
			new ExpiredContentDeactivationDaemon().start();
		
		if(getParamAsBoolean("START_SEND_EXPIRY_SMS_DAEMON","FALSE"))
			new SendExpirySMSDaemon().start();
		
		if(getParamAsBoolean("PROCESS_GIFT","FALSE"))
			new RBTGift(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("START_REMINDER_DAEMON","FALSE"))
			ReminderDaemon.getInstance().start();
		
		if(getParamAsBoolean("START_SHUFFLE_PROMO_DAEMON","FALSE"))
			new ShufflePromoDaemon(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("START_VODACT_PRISM_DAEMON","FALSE"))
			new VodaPrismDaemon().start();
		
		if(getParamAsBoolean("START_CLIP_STATUS_DAEMON", "FALSE"))
			new ClipStatusDaemon(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean(COMMON, "RRBT_SYSTEM", "FALSE"))
		{
			if (getParamAsBoolean(COMMON, "PROCESS_ANNOUNCEMENTS", "false"))
				new RBTAnnouncementDaemon(m_rbtDaemonManager).start();
			
			if (getParamAsBoolean(COMMON, "PROCESS_CONFIRM_ACT_REQUESTS", "FALSE"))
				new RRBTConfirmActThread(m_rbtDaemonManager).start();
		}

		if(getParamAsBoolean("PROCESS_TRIAL_SELECTIONS","FALSE"))
			new RBTProcessTrialSelections(m_rbtDaemonManager).start();

		if(getParamAsBoolean("PROCESS_UGC_CREDIT","FALSE"))
			new RBTProcessUGCCharging(m_rbtDaemonManager).start();

		if(getParamAsBoolean("START_GRBT_COPYDATA_COLLECTOR", "FALSE"))
			new CopyDataCollector().start();

		if(getParamAsBoolean("START_GRBT_SERVER_UPLOADER", "FALSE"))
			new GRBTServerUploader().start();

		if(getParamAsBoolean("SEND_ACTIVITY_REPORTS","FALSE"))
			new DailyActivityReport(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("PROCESS_OVERRIDE_SHUFFLE_DEACTIVATION","FALSE"))
			new RBTProcessOverrideShuffleSelections(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("PROCESS_CORPORATE_TASKS","FALSE"))
			new RBTCorporateProcessor(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("START_EMOTIONS_RBT_SMS_NOTIFIER","FALSE"))
			new EmotionRbtUserSmsNotifier(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("PROCESS_RETAILER","FALSE"))
			new RBTRetailer(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("PROCESS_EXPIRED_CONFIRMATION_SMS","FALSE"))
			new RBTExpiredViralSmsProcessor(m_rbtDaemonManager).processExpiredViralSmsRecords();
		
		if (getParamAsBoolean("PROCESS_IDEA_RETAILER", "FALSE"))
			new RBTIdeaRetailProcessor(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_UNINOR_RETAILER", "FALSE"))
			new RBTUninorRetailProcessor(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_EXPIRED_CONSENT_RECORDS", "FALSE"))
			new RBTConsentExpiredProcessor(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_PRESS_DOWNLOAD_THREAD", "FALSE"))
			new RbtSupportDaemon(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_FTP_CAMPAIGNS", "FALSE"))
			new FTPCampaignManager(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_PENDING_CONFIRMATIONS_REMAINDER", "FALSE"))
			new PendingConfirmationsReminderDaemon(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_LOTTERY_ENTRIES", "FALSE"))
			new RBTLotteryEntriesDaemon(m_rbtDaemonManager).start();
		
		if (getParamAsBoolean("PROCESS_VIRAL_WHITE_LIST", "FALSE"))
			new ViralWhiteListDaemon().start();

		if (getParamAsBoolean("PROCESS_JMS_UNINOR_RETAILER", "FALSE"))
			startUninorJmsRetailProcessor();
		
		if(getParamAsBoolean("START_DOUBLE_CONFIRMATION_DAEMON","FALSE"))
			new DoubleConfirmationDaemon(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean("START_RBT_TEMP_GROUP_MEMBER_THREAD", "FALSE"))
			startRbtTempGroupMemberProcessor();

		if(getParamAsBoolean("START_RBT_MULTI_OP_COPY_DAEMONS", "FALSE")) {
			startRbtMultiOpCopyDaemons();
		}

		
		if(getParamAsBoolean("START_IBM_STATUS_UPDATE_DAEMON", "FALSE")) {
			new IBMStatusUpdateDaemon().start();
		}
		
		//RBT-11752
		if(getParamAsBoolean("START_ODA_UPDATE_DAEMON", "FALSE"))
			new RbtODAUpdateDaemon(m_rbtDaemonManager).start();
		
		if(getParamAsBoolean(WebServiceConstants.START_GET_CURRENT_SONG_DAEMON, "FALSE"))
			CurrentSongDaemon.start();

		//TATA TELECALLIN CREATE NAME TUNE REUESTS
		if (getParamAsBoolean("PROCESS_NAME_TUNE_REUESTS", "FALSE")) {
			new NameTunesNewRequestThread().start();
			new NameTunesProcessingThread().start();
		}
		
		// RBT-14652
		if (getParamAsBoolean("START_SAT_PUSH_DAEMON", "FALSE")) {
			new RBTSATPushExecutors(m_rbtDaemonManager).start();
		}

		if (getParamAsBoolean("START_AD_PARTNER_CALL_BACK_DAEMON", "FALSE")) {
			new AdPartnerRequestProducer(m_rbtDaemonManager).start();
		}
		if(getParamAsBoolean("START_SUBSCRIBER_PACK_VALIDITY_UPDATION_THREAD", "FALSE")){
			new SubscriberPackValidityUpdationThread(m_rbtDaemonManager).start();
		}

		//Added for RBT-18249
		if (getParamAsBoolean("START_RBT_RTO_DAEMON", "FALSE")) {
			new RBTRtoExecutor(m_rbtDaemonManager).start();
		}
				
		if(getParamAsBoolean("START_SELECTION_DEACT_CONTENT_EXPIRY_DAEMON","FALSE"))
			new SelectionDeactivationContentExpiredDaemon().start();
		
		while (m_Continue) 
		{
			if(getParamAsBoolean("PROCESS_OPT_IN","FALSE"))
				processSubscriptionsOptin();
			
			if (m_lastRunTime == null || (m_lastRunTime.getTime() < System.currentTimeMillis()))
			{
				if (m_lastRunTime != null)
				{
					processExpiredSelections();
			
					if(getParamAsBoolean("PROCESS_DOWNLOADS", "FALSE"))
						processExpiredDownloads();
				}
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);
				cal.set(Calendar.HOUR_OF_DAY, 2);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				m_lastRunTime = cal.getTime();
				logger.info("RBT::NonDeactivatedSelections will be next removed on "+ m_lastRunTime);
			} 
			try
			{
				long sleepTime = getParamAsInt("SMDAEMON_SLEEP_INTERVAL_SECONDS", 300);
				Thread.sleep(sleepTime * 1000);
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		
	}
		
	private void processExpiredDownloads()
	{
		SubscriberDownloads[] exDownloads = rbtDBManager.getAllSubscriberDownloadRecordsNotDeactivated();
		String smDaemonMode = RBTParametersUtils.getParamAsString("DAEMON", "MODE_FOR_EXPIRE_SELECTION", "SMDaemon");
		if (exDownloads == null || exDownloads.length == 0)
			return;
	
		for (int i = 0; i < exDownloads.length; i++)
		{
				rbtDBManager.expireSubscriberDownload(exDownloads[i].subscriberId(),exDownloads[i].refID(),smDaemonMode);
		}
	}

	private void processExpiredSelections()
	{
		SubscriberStatus[] ss = rbtDBManager.getAllSubscriberSelectionRecordsNotDeactivated();
		String smDaemonMode = RBTParametersUtils.getParamAsString("DAEMON", "MODE_FOR_EXPIRE_SELECTION", "SMDaemon");
		if(ss == null || ss.length == 0 )
			return;
		
		for (int i = 0; i < ss.length; i++)
		{
			rbtDBManager.deactivateSubscriberRecords(ss[i].subID(),ss[i].callerID(),ss[i].status(),
					ss[i].fromTime(),ss[i].toTime(), true,smDaemonMode);
		}
	}

	private void createSMThreadPools()
	{
		logger.info("creating sm thread pools");
		SMRequestProducer baseActRequestProducer = new SMRequestProducer(RequestType.SM_BASE_ACT, getParamAsInt("BASE_THREAD_POOL_SIZE",1));
		baseActRequestProducer.start();
		
		if(getParamAsBoolean("PRORITIZE_THREAD_MODE", "FALSE"))
		{
			SMRequestProducer baseActLowRequestProducer = new SMRequestProducer(RequestType.SM_BASE_ACT_LOW, getParamAsInt("BASE_THREAD_LOW_POOL_SIZE",1));
			baseActLowRequestProducer.start();
		}
		
		SMRequestProducer baseDeactRequestProducer = new SMRequestProducer(RequestType.SM_BASE_DCT, getParamAsInt("BASE_THREAD_POOL_SIZE",1));
		baseDeactRequestProducer.start();
		
		SMRequestProducer selActRequestProducer = new SMRequestProducer(RequestType.SM_SEL_ACT, getParamAsInt("SEL_THREAD_POOL_SIZE",1));
		selActRequestProducer.start();
		
		if(getParamAsBoolean("PRORITIZE_THREAD_MODE", "FALSE"))
		{
			SMRequestProducer selActLowRequestProducer = new SMRequestProducer(RequestType.SM_SEL_ACT_LOW, getParamAsInt("SEL_THREAD_LOW_POOL_SIZE",1));
			selActLowRequestProducer.start();
		}
		
		if(getParamAsBoolean("DIRECT_ACTIVATIONS", "FALSE"))
		{
			SMRequestProducer selActDirectRequestProducer = new SMRequestProducer(RequestType.SM_SEL_ACT_DIR, getParamAsInt("SEL_THREAD_POOL_SIZE",1));
			selActDirectRequestProducer.start();
		}
		
		if(getParamAsBoolean("PROCESS_RENEWAL_SELECTIONS", "FALSE")) 
		{
			SMRequestProducer selActRenRequestProducer = new SMRequestProducer(RequestType.SM_SEL_REN, getParamAsInt("SEL_THREAD_POOL_SIZE",1));
			selActRenRequestProducer.start();
		}
		
		SMRequestProducer selDeactRequestProducer = new SMRequestProducer(RequestType.SM_SEL_DCT, getParamAsInt("SEL_THREAD_POOL_SIZE",1));
		selDeactRequestProducer.start();
	
		if(getParamAsBoolean("PROCESS_DOWNLOADS", "FALSE"))
		{
			SMRequestProducer downloadActRequestProducer = new SMRequestProducer(RequestType.SM_DWN_ACT, getParamAsInt("DOWNLOAD_THREAD_POOL_SIZE",1));
			downloadActRequestProducer.start();
		
			SMRequestProducer downloadDeactRequestProducer = new SMRequestProducer(RequestType.SM_DWN_DCT, getParamAsInt("DOWNLOAD_THREAD_POOL_SIZE",1));
			downloadDeactRequestProducer.start();
		}
		
		SMRequestProducer packActRequestProducer = new SMRequestProducer(RequestType.SM_PACK_ACT, getParamAsInt("PACK_THREAD_POOL_SIZE",1));
		packActRequestProducer.start();

		SMRequestProducer packDeactRequestProducer = new SMRequestProducer(RequestType.SM_PACK_DCT, getParamAsInt("PACK_THREAD_POOL_SIZE",1));
		packDeactRequestProducer.start();
	}

	private void startPlayerUpdateThread()
	{
		try
		{
			String playerClass = getParamAsString("PLAYER_START_CLASS");
			if(playerClass == null || playerClass.trim().length() ==0)
				new RBTPlayerUpdateDaemon(m_rbtDaemonManager).start();              //start default Player
			else 																	//start the configured player
			{
				logger.info("RBT:: starting custom player update thread");
				Class<?> classIns = Class.forName(_playerClass);
				Constructor<?> constructor = classIns.getConstructor(new Class[] {this.getClass()});
				_playerThread = (PlayerThread)constructor.newInstance(new Object[] {m_rbtDaemonManager});
				_playerThread.start();
			}
		}
		catch (Exception e)
		{
			logger.error("Issue in creating player thread", e);
		}
	}
	
	private void startUninorJmsRetailProcessor()
	{
		try {
			new RBTUninorJMSRetailProcessor(m_rbtDaemonManager).start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void startRbtTempGroupMemberProcessor()
	{
		try {
			new RbtTempGroupMemberThread(m_rbtDaemonManager).start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void startRbtMultiOpCopyDaemons()
	{
		try {
			logger.info("Starting RBTMultiOpCopyContentReslover daemon.");
			new RBTMultiOpCopyContentReslover(m_rbtDaemonManager).start();
			logger.info("Starting RBTMultiOpCopyPollOperator daemon.");
			new RBTMultiOpCopyPollOperator(m_rbtDaemonManager).start();
			logger.info("Stared RBTMultiOpCopyContentReslover and RBTMultiOpCopyPollOperator daemon.");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected String getSMResponseString(HashMap<String, String> map, String key)
	{
		if(map == null) {
			return null;
		}
		return map.get(key);
	}
	
	protected boolean isNavCat(int catID) {
		Category cat = rbtCacheManager.getCategory(catID);
		if (cat != null && cat.getCategoryTpe() == 10)
			return true;
		return false;
	} 
	
	public boolean processDirectRecordsSub(Subscriber subObj) {
		boolean bSendSubTypeUnknown = false;
		SubscriptionClass subscriptionClass = m_rbtSubClassCacheManager.getSubscriptionClass(subObj.subscriptionClass());

		if(subscriptionClass.getSubscriptionAmount() != null && subscriptionClass.getSubscriptionAmount().equalsIgnoreCase("0")
				&& getParamAsBoolean("SEND_UNKNOWN_ZERO_ACT","FALSE"))
			bSendSubTypeUnknown = true;

		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = subObj.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& (subObj.subYes().equalsIgnoreCase("A") && retryCount.startsWith("A")))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = subObj.nextRetryTime();
			if (noOfRetries >= maxRetries || nextRetryTime.after(new Date()))
				return false;
		}

		String success = getSMResponseString(makeSubMgrRequest(subObj, null, null, null, "ACT", bSendSubTypeUnknown), RESPONSE);
		logger.info("Direct activation request of subscriber " + subObj.subID() + " - Response: " + success);
		
		if(success != null && success.equals(SM_URL_FAILURE)) {
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
			return false;
		}
		else if(success != null && success.startsWith("SUCCESS")) {
			smSubscriptionSuccess(subObj.subID(), null, null);
			smUpdateSelStatusSubscriptionSuccess(subObj.subID());
		}
		else if(success != null && success.trim().length() > 0 && !success.equalsIgnoreCase(FAILED)) {
			smURLSubscription(subObj.subID(), false, true, null);
			return true;
		}
		else {
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
			return false;
		}

		return true;
	}

	public boolean processDirectRecordsSel(SubscriberStatus ss) 
	{
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(ss.classType());
		try
		{
			if(!getParamAsBoolean("SEND_ZERO_SEL_SM", "TRUE")
					&& (chargeClass.getAmount() != null 
							&& Double.parseDouble(chargeClass.getAmount().replace(",", "."))==0
							&& chargeClass.getRenewalAmount() != null 
							&& Double.parseDouble(chargeClass.getRenewalAmount().replace(",", "."))==0)) {
				Date startTime = null;
				if(isNavCat(ss.categoryID()))
				{
					startTime = getStartTime(ss.categoryID());
				}
				else if(ss.status() == 95 && ss.selInterval() == null)
				{
					startTime = ss.startTime();
				}
				/*
				 * songWavFilesList will contains, all de-activation selection song rbt wav file name. 
				 */
				List<String> wavFileNameList = new ArrayList<String>();
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss
						.fromTime(), ss.toTime(), false, false, 
						sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), startTime, ss.selType(), ss.subscriberFile(), false, ss.refID(), ss.selInterval(), wavFileNameList);
				if(getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE") && chargeClass.getSmschargeSuccess() != null
						&& !chargeClass.getSmschargeSuccess().equalsIgnoreCase("null")) {
					try {
						
						String smsText = getSelectionSMS(chargeClass.getSmschargeSuccess(),ss);
						Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
					}
					catch (Exception e) {
						logger.error("", e);
					}
				}
				
				/*
	    		 * Sms will not send to subscriber, if sms text is not configured or there is not song for deactivation
	    		 * Sending sms to user for his previous song is also available in his inbox until the expiry period of that song 
	    		 */
				try {
				Subscriber subscriber = rbtDBManager.getSubscriber(ss.subID());
				String smsText = Utility.getSmsTextForDeactivationSelection(subscriber.language(), wavFileNameList);
				if(smsText != null)
					Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch(Exception e) {
					logger.error(e);
				}
				return true;
			}
			boolean updateRefID = false;

			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = ss.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& (ss.selStatus().equals("A") && retryCount.startsWith("A")))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = ss.nextRetryTime();
				if (noOfRetries >= maxRetries || nextRetryTime.after(new Date()))
					return false;
			}

			HashMap<String, String> resp = makeSubMgrRequest(null, ss, null, null, "REALTIME", false);
			String success = getSMResponseString(resp, RESPONSE);
			String refID = getSMResponseString(resp, REFID);
			if(getSMResponseString(resp, REFID_CREATED) != null)
				updateRefID = true;
			logger.info("Direct selection real time request of subscriber " + ss.subID() + " - Response: " + success);
			if(refID == null)
			{
				logger.info("RBT::Could not get RefID for sub " + ss.subID());
				return false;
			}

			if(success != null && success.equals(SM_URL_FAILURE))
			{
				if(updateRefID)
					rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss
							.fromTime(), ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
				else {
					long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
					noOfRetries++;
					retryCount = "A" + noOfRetries;

					Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
					rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
				}

				return false;
			}
			else if(success != null && success.startsWith("SUCCESS")) {
				/*
				 * songWavFilesList will contains, all de-activation selection song rbt wav file name. 
				 */
				List<String> wavFileNameList = new ArrayList<String>();
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss
						.fromTime(), ss.toTime(), false, false,  
						sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), updateRefID, refID,ss.selInterval(), wavFileNameList);

				if(getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE") && chargeClass.getSmschargeSuccess() != null
						&& !chargeClass.getSmschargeSuccess().equalsIgnoreCase("null")) {
					try {
						String smsText = getSelectionSMS(chargeClass.getSmschargeSuccess(),ss);
						if (!sendSelectionSMSCrossPromo(smsText, ss))
							Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
					}
					catch (Exception e) {
						logger.error("", e);
					}
				}
				
				/*
	    		 * Sms will not send to subscriber, if sms text is not configured or there is not song for deactivation
	    		 * Sending sms to user for his previous song is also available in his inbox until the expiry period of that song 
	    		 */
				try {
				Subscriber subscriber = rbtDBManager.getSubscriber(ss.subID());
				String smsText = Utility.getSmsTextForDeactivationSelection(subscriber.language(), wavFileNameList);
				if(smsText != null)
					Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch(Exception e) {
					logger.error(e);
				}
				return true;
			}
			else if(success != null && success.trim().length() > 0 && !success.equalsIgnoreCase(FAILED)){
				rbtDBManager.deactivateRealTimeSubscriberRecords(ss.subID(), ss.callerID(),
						ss.status(), ss.fromTime(), ss.toTime(), sdf.format(ss.setTime()), refID);
				String prepaid = "n"; 
				if(ss.prepaidYes()) 
					prepaid = "y"; 
				if(ss.categoryType() == 11) 
					rbtDBManager.smDeactivateOtherUGSSelections(ss.subID(), ss.callerID(), prepaid, ss.selType());
				return true; 
			} 
			else if(updateRefID)
				rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss
						.fromTime(), ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "A" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return false;
		}

		return false;
	}

	public static String getSenderNumber(String circleID, String senderNumber) {
		if(circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = RBTParametersUtils.getParamAsString("DAEMON", operatorName +"_SENDER_NO", senderNumber);
			}
		}
		logger.info("senderNumber :" + senderNumber);
		return senderNumber;
	}
	
	public boolean processDirectRecordsDct(Subscriber subObj) {
		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = subObj.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& (subObj.subYes().equals("D") && retryCount.startsWith("D")))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = subObj.nextRetryTime();
			if (noOfRetries >= maxRetries || nextRetryTime.after(new Date()))
				return false;
		}

		SubscriptionClass subscriptionClass = m_rbtSubClassCacheManager.getSubscriptionClass(subObj.subscriptionClass());
		if(subscriptionClass.getSubscriptionPeriod() != null
				&& !subscriptionClass.getSubscriptionPeriod().equalsIgnoreCase("O")) {
			String success = getSMResponseString(makeSubMgrRequest(subObj, null, null, null, "DIRECTDCT", false), RESPONSE);
			logger.info("Direct deactivation request of subscriber " + subObj.subID() + " - Response: " + success);
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
				return false;
			}
			else if(success != null && success.trim().length() > 0 && success.startsWith("SUCCESS"))
				smURLUnSubscription(subObj.subID(), false, false);
			else if(success != null && !success.equalsIgnoreCase("ALREADY_DEACTIVE"))
				rbtDBManager.smDeactivationSuccess(subObj.subID(), subObj.subYes(), null); // Added extraInfo
			else if(success != null && !success.equalsIgnoreCase(FAILED))
				smURLUnSubscription(subObj.subID(), false, true);
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
				return false;
			}
		}
		else
			smURLUnSubscription(subObj.subID(), false, false);
		return true;
	}

	public boolean processSubscriptionsAct(Subscriber subObj) {
		
		boolean bSendSubTypeUnknown = false;
		SubscriptionClass subscriptionClass = m_rbtSubClassCacheManager.getSubscriptionClass(subObj.subscriptionClass());

		//m_sendUnknownZeroAct is SEND_UNKNOWN_ZERO_ACT in RBT_PARAMETERS
		if(subscriptionClass.getSubscriptionAmount() != null
				&& subscriptionClass.getSubscriptionAmount().equalsIgnoreCase("0")
				&& getParamAsBoolean("SEND_UNKNOWN_ZERO_ACT", "FALSE"))
			bSendSubTypeUnknown = true;
		addDefaultSelectionForSubscriber(subObj.subID(), subObj, subObj.activatedBy());
		boolean isDelayedDeact =false;
		if (subObj.subYes().equalsIgnoreCase("C"))
		{  
			HashMap<String,String> subscriptionExtraInfoMap =null;
	  	    subscriptionExtraInfoMap = DBUtility.getAttributeMapFromXML(subObj.extraInfo());
			// If subscriber status is C and ExtraInfo contains DELAY_DEACT="TRUE" then it is delayed deactivation request
			isDelayedDeact = (subscriptionExtraInfoMap != null && "TRUE".equalsIgnoreCase(subscriptionExtraInfoMap.get("DELAY_DEACT")));
			if (isDelayedDeact)
				logger.info("sel status is C and ExtraInfo contains DELAY_DEACT=TRUE");
		}
		
		String requestType = isDelayedDeact ? "DCT" : "ACT";

		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = subObj.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& ((subObj.subYes().equalsIgnoreCase("C") && retryCount.startsWith("U"))
						|| (subObj.subYes().equalsIgnoreCase("A") && retryCount.startsWith("A"))))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = subObj.nextRetryTime();
			if(noOfRetries >= maxRetries){
				rbtDBManager.setSubscriptionYes(subObj.subID(), "E");
				return false;
			} else if (nextRetryTime.after(new Date()))
				return false;
		}

		String success = getSMResponseString(makeSubMgrRequest(subObj, null, null, null, requestType, bSendSubTypeUnknown), RESPONSE);
		logger.info("Activation request of subscriber: " + subObj.subID() + "  - Response: " + success);
		
		//RBT-14497 - Tone Status Check
		if (success != null && success.equalsIgnoreCase(CONTENT_EXPIRED)) {
			return true;
		}
		//RBT-12195 - User block - unblock feature.
		if(success != null && success.equals(Constants.BLOCK_SUB_KEYWORD))
		{
			return false;
		}
		int rbtType = 0; 
		if(subObj.rbtType() == TYPE_SRBT_RRBT || subObj.rbtType() == TYPE_RRBT) 
			rbtType = 1; 
		
		String extraInfo = subObj.extraInfo();
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		if (extraInfo != null)
			extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if(success != null && success.equals(SM_URL_FAILURE)) {
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			if (subObj.subYes().equalsIgnoreCase("C"))
				retryCount = "U" + noOfRetries;
			else
				retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
			return false;
		}
		else if(success != null && success.startsWith("SUCCESS")) {
			boolean extraInfoChanged= false;
			if(extraInfoMap!=null) {
				if (extraInfoMap.containsKey("sdpomtxnid")) {
					extraInfoMap.remove("sdpomtxnid");
					logger.info("Removed sdpomtxnid from extrainfo after getting sm response as success.Now extraInfoMap: "
							+ extraInfoMap);
					extraInfoChanged = true;
				}
				// RBT-12842-Unwanted Parameter In Upgrade Request From RBT
				String baseMappedStr = getParamAsString(COMMON,
						"BASE_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (baseMappedStr != null) {
					String str[] = baseMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2 && extraInfoMap.containsKey(s[1])) {
							extraInfoMap.remove(s[1]);
							extraInfoChanged = true;
						}
					}
				}
				logger.info("Removed BASE_PARAMETERS_MAPPING_FOR_INTEGRATION parameters from extrainfo after getting sm response as success.Now extraInfoMap: "
						+ extraInfoMap);
				if (extraInfoChanged) {
					rbtDBManager.updateExtraInfo(subObj.subID(),
							DBUtility.getAttributeXMLFromMap(extraInfoMap));
				}
			}
			if (isDelayedDeact)
			{   String prevDealyDeactSubYes = extraInfoMap.get("SUB_YES");
				boolean update = smURLSubscription(subObj.subID(), false, false, prevDealyDeactSubYes);
				if(update && extraInfoMap.containsKey("SUB_YES")){
					extraInfoMap.remove("SUB_YES");
					rbtDBManager.updateExtraInfo(subObj.subID(), DBUtility.getAttributeXMLFromMap(extraInfoMap));
				}
					
				return true;
			}
			boolean result = smURLSubscription(subObj.subID(), true, false, null); // change subscription_yes to N, next_charging_date and activation_date to sysdate
			logger.debug("subscriberId: " + subObj.subID() +  ". DB updation result: " + result);
			if (result && extraInfoMap!=null && extraInfoMap.containsKey("SELECTION_MODE")) {
				logger.info("subscriberId: " + subObj.subID() + ". Removing SELCTION_MODE from extraInfo.");
				String selectionMode = extraInfoMap.get("SELECTION_MODE");
				extraInfoMap.remove("SELECTION_MODE");
				extraInfoMap.put("AUTO_UPGRADE_MODE", selectionMode);
				rbtDBManager.updateExtraInfo(subObj.subID(), DBUtility.getAttributeXMLFromMap(extraInfoMap));
			}
//			if (m_combinedCharging && extraInfoMap != null && extraInfoMap.containsKey(PACK))
//			smUpdatePackStatusOnBaseAct(subObj.subID()); // change status from 7 ie. PACK_BASE_ACTIVATION_PENDING to 1 ie. PACK_TO_BE_ACTIVATED in provisioning_requests table
			return true;
		}
		else if(success != null && success.startsWith("ALREADY_ACTIVE")) {
			// change subscription_yes to B, player_status to A, and end_time to 2037 for all
			smSubscriptionSuccess(subObj.subID(), success.substring(15), null); 
			// process all the selections which are in callback waiting state i.e. W
			smUpdateSelStatusSubscriptionSuccess(subObj.subID());
			smUpdatePackStatusOnBaseAct(subObj.subID()); // change status from 7 ie. PACK_BASE_ACTIVATION_PENDING to 1 ie. PACK_TO_BE_ACTIVATED in provisioning_requests table
			return true;
		}
		else if(success != null && success.startsWith("SUSPENDED"))
			smSubscriptionSuspend(subObj.subID(), success.substring(10), null); //next_charging_date and activation_date to systdae, subscription_yes to Z
		else if(success != null && success.startsWith("BLACKLISTED"))
			rbtDBManager.deactivateSubscriber(subObj.subID(), "BLACKLIST", null, true, true, true, true, false);
		else if (success != null && success.startsWith("UPGRADE_FAILURE"))
		{
			String oldActBy  = null;
			if(extraInfoMap.containsKey(EXTRA_INFO_OLD_ACT_BY)){
				oldActBy = extraInfoMap.get(EXTRA_INFO_OLD_ACT_BY);
				extraInfoMap.remove(EXTRA_INFO_OLD_ACT_BY);
			}
			// if upgrade fails set the subscription_yes to B and subscription_class to old_class
			String response = rbtDBManager.updateUpgradeFailure(subObj.subID(),subObj.activatedBy(), subObj.oldClassType(), rbtType, 
					getParamAsBoolean("COMMON","IS_RRBT_ON","FALSE"), "B", null, oldActBy,null);

			rbtDBManager.smUpdateSelStatusSubscriptionSuccess(subObj.subID(),getParamAsBoolean("REAL_TIME_SELECTIONS","FALSE"),false,true,null);// RBT-14301: Uninor MNP changes.

			if (response.equalsIgnoreCase("SUCCESS"))
			{
				processUpgradeTransaction(subObj);
				smUpdatePackStatusOnBaseAct(subObj.subID()); // change status from 7 ie. PACK_BASE_ACTIVATION_PENDING to 1 ie. PACK_TO_BE_ACTIVATED in provisioning_requests table
			}
			else
				removeAllUpgradeTransactions(subObj, "UPGRADE FAILURE");
		}
		else if(success != null && success.trim().length() > 0 && !success.equalsIgnoreCase(FAILED)) 
		{
			// In case the user has availed song_pack or limited_downloads feature and failed at SM,
			// then deactivate downloads and selections with download_status 'w' and sel_status 'W' respectively

			if (m_combinedCharging && extraInfoMap != null && extraInfoMap.containsKey(PACK))
			{
				String cosID = null;
				cosID = extraInfoMap.get(PACK).split(",")[0];
				if(smURLSubscription(subObj.subID(), false, true, null))
				{
					if(cosID != null)
						rbtDBManager.smActFailureSuccess(subObj.subID(), cosID, PACK_ACTIVATION_ERROR+"", true);
					else
						logger.info("Not putting pack in error state as cos Id is not available");
				}
			}
			else
				smURLSubscription(subObj.subID(), false, true, null);

			return true;
		}
		else
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			if (subObj.subYes().equalsIgnoreCase("C"))
				retryCount = "U" + noOfRetries;
			else
				retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSubscriber(subObj.subID(), retryCount, retryTime);
		}

		return false;
	}

    private void addDefaultSelectionForSubscriber(String subscriberID,Subscriber subscriber,String mode){
	    if(!getParamAsBoolean("COMMON","ADD_TO_DOWNLOADS","TRUE")){
	       SubscriberStatus subscriberStatus[]= rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
	       if(subscriberStatus!=null)
	    	   return;
	       List<String> m_DEFAULT_DOWNLOAD = null;
		   Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON","DEFAULT_DOWNLOAD");
		   if (parameter != null && parameter.getValue() != null)
			  m_DEFAULT_DOWNLOAD = Arrays.asList(parameter.getValue().split(","));
		   else
			  return;
		   if(m_DEFAULT_DOWNLOAD!=null && m_DEFAULT_DOWNLOAD.get(0)!=null){
			  Clip clip = rbtCacheManager.getClip(m_DEFAULT_DOWNLOAD.get(0));
			  Category category = rbtCacheManager.getCategory(2);
			  Parameters chargeClassParameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON","DEFAULT_SELECTION_CHARGE_CLASS");
			  String chargeClassType = "DEFAULT";
			  if(chargeClassParameter!=null)
			          chargeClassType=chargeClassParameter.getValue();
			  if(category!=null && clip!=null){
				     HashMap hashMap = new HashMap();
				     hashMap.put("IBM_SE", "TRUE");
			         rbtDBManager.addSubscriberSelections(subscriberID, null, category.getCategoryId(),
						clip.getClipRbtWavFile(), null, null, null, 1, mode, null, 0, subscriber.prepaidYes(), false, null, 0, 2359,
						chargeClassType, true, true, null, null, subscriber.subYes(), null, true, false, false,
						subscriber.subscriptionClass(), subscriber, null, hashMap);
		      }
		  }
      }
	   
   }

	public boolean processSubscriptionsDct(Subscriber ds) {
		SubscriptionClass subscriptionClass = m_rbtSubClassCacheManager.getSubscriptionClass(ds.subscriptionClass());
		// if the subscription Period is 'O' then the subscriber is charged once 
		// and there would not be any renewal for the subscriber and will be deactivated at the SM 
		// Hence no request is send to the SM for deactivation and is deactivated at the RBT side.
		if(subscriptionClass.getSubscriptionPeriod() != null
				&& !subscriptionClass.getSubscriptionPeriod().equalsIgnoreCase("O")) {
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = ds.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& (ds.subYes().equalsIgnoreCase("D") && retryCount.startsWith("D")))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = ds.nextRetryTime();
				if(noOfRetries >= maxRetries){
					rbtDBManager.setSubscriptionYes(ds.subID(), "F");
					return false;
				}else if (nextRetryTime.after(new Date())){
					return false;
				}
			}

			String success = getSMResponseString(makeSubMgrRequest(ds, null, null, null, "DCT", false), RESPONSE);
			logger.info("Deactivation request of subscriber: " + ds.subID() + "  - Response: " + success);
			
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSubscriber(ds.subID(), retryCount, retryTime);
				return false;
			}
			else if(success != null && success.trim().length() > 0 && success.startsWith("SUCCESS")){
				smURLUnSubscription(ds.subID(), true, false);
				if(getParamAsBoolean("DAEMON", "UPDATE_PACK_STATUS_ON_BASE_DEACT", "TRUE")){
				    smUpdatePackStatusOnBaseDeact(ds.subID());
				}
			}
			else if(success != null && success.equalsIgnoreCase("ALREADY_DEACTIVE")) {
				/*
				 * deactivate the subscriber, subscription_yes to x in case of memcache present, if not 
				 * update the subscription_yes directly to X. Also deactivate the selections , 
				 * loop status to X and sel_status to X 
				 */
				rbtDBManager.smDeactivationSuccess(ds.subID(), ds.subYes(), null); // added extraInfo
			}
			else if(success != null && !success.equalsIgnoreCase(FAILED)) // The subscription_yes becomes F, i.e. deactivation falied
				smURLUnSubscription(ds.subID(), false, true);
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSubscriber(ds.subID(), retryCount, retryTime);
			}
		}
		else
			smURLUnSubscription(ds.subID(), false, false); // this is direct deactivation from RBT
		return true;
	}

	/*
	 * Gets all the records which are active (subscription yes as B) but whose end date is less than system date. 
	 *  
	 * If the cos has a provision for continued activation for n days then the cos details are updated for the subscriber .
	 * */
	private void processSubscriptionsOptin() {
		Subscriber[] getSubsTobeDeactivated = getSubsTobeDeactivated();
		if(getSubsTobeDeactivated == null || getSubsTobeDeactivated.length <= 0) {
			logger.info("No subscribers to be deactivated..");
			return;
		}

		for(int i = 0; i < getSubsTobeDeactivated.length; i++) {
			String deactBy = "SMDaemon"; 
			
			if(getSubsTobeDeactivated[i].subscriptionClass().equals("LIFEFREE")) { 
				deactBy = "LIFEFREE";
			}
			
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(getSubsTobeDeactivated[i].cosID());
			if(cos == null || cos.isDefaultCos() || !cos.renewalAllowed()){
				logger.debug("Deactivating subscriber. subscriberId: "
						+ getSubsTobeDeactivated[i].subID());
				rbtDBManager.deactivateSubscriber(getSubsTobeDeactivated[i].subID(), deactBy, null, true, true, true);
			}else{
				logger.debug("Updating Subscriber. subscriberId: "
						+ getSubsTobeDeactivated[i].subID());
				CosDetails renewalCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cos.getRenewalCosid());
				if(renewalCos == null){
					return;
				}
				Calendar cal = Calendar.getInstance();
				if(renewalCos.isDefaultCos()) {
					cal.set(2037, 0, 1, 0, 0, 0);
				} else {
					cal.setTime(getSubsTobeDeactivated[i].endDate());
					cal.add(Calendar.DATE, renewalCos.getValidDays() - 1);
				}
				Date endDate = cal.getTime();

				rbtDBManager.updateSubscriberCosId(getSubsTobeDeactivated[i].subID(),cos.getRenewalCosid(),endDate);
				logger.debug("Updated Subscriber. subscriberId: "
						+ getSubsTobeDeactivated[i].subID());
			}
		}
	}


	public boolean processSelectionsAct(SubscriberStatus ss) {
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(ss.classType());
		try
		{
			
			Subscriber subscriber2 = getSubscriber(ss.subID());
			
			boolean isSMHitNotToBeMade = isSMHitNotToBeMade(ss, "ACT");
			/*
			 * If its a zero sel req and charge class amount is zero do not send a request to SM.
			 * Deactivate the old selections and activate the new selections
			 * NOTE: Specifically used in Idea for zero selection charge
			 */
			if(isSMHitNotToBeMade|| (!getParamAsBoolean("SEND_ZERO_SEL_SM", "TRUE")
					&& (chargeClass.getAmount() != null 
							&& Double.parseDouble(chargeClass.getAmount().replace(",", "."))==0
							&& chargeClass.getRenewalAmount() != null 
							&& Double.parseDouble(chargeClass.getRenewalAmount().replace(",", "."))==0))) {
				
				/*
				 * Get old selection information, if loop status is override
				 * Send sms to user, if list is not null and size > 0 
				 */
				List<String> wavFileNameList = new ArrayList<String>();
				
				//Fixed by Sreekar for RBT-7338
				Date startTime = null;
				if (isNavCat(ss.categoryID())) {
					startTime = getStartTime(ss.categoryID());
				}
				else if (ss.startTime().before(new Date())) {
					// don't do anything so that start time will be updated to sysdate
				}
				else {
					startTime = ss.startTime();
				}
//                Subscriber subscriber2 = getSubscriber(ss.subID());
				//RBT-14044	VF ES - MI Playlist functionality for RBT core
				rbtDBManager.addTrackingOfPendingSelections(ss);
				
				boolean success = smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), false, false,  sdf.format(ss.setTime()), ss.loopStatus(), ss.prepaidYes(), 
						startTime, ss.selType(), ss.subscriberFile(), false, ss.refID(), ss.selInterval(), wavFileNameList);
				if (success) {
					if( RBTParametersUtils.getParamAsBoolean("COMMON",
							"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")) {
						rbtDBManager.deactivateOldODAPackOnSuccessCallback(ss.subID(), ss.refID(), ss.callerID(),
								ss.categoryType(), ss, false, null);
					}
					
					//RBT-14044	VF ES - MI Playlist functionality for RBT core
					rbtDBManager.addOldMiplayListSelections(ss);
					if((ss.callerID() == null || ss.callerID().equalsIgnoreCase("all")) && ss.status()==1 && !Utility.isShuffleCategory(ss.categoryType())) {
					  String resp = rbtDBManager.addDownloadForTrackingMiPlaylist(ss.subID(), ss.subscriberFile(),ss.categoryID(),ss.categoryType(), null, ss.classType(), ss.selectedBy(),ss.status(),ss.selType());
					  logger.info("Response of addDownloadForTrackingMiPlaylist in Daemon manager processSelectionsAct: "+resp);
					}
					
					TransLogForSelection.writeTransLogForSelection(
							ss.circleId(), ss.subID(), ss.callerID(),
							ss.selType(), ss.fromTime(), ss.toTime(),
							ss.selInterval(), ss.categoryType(), ss.status(),
							ss.subscriberFile(), ss.categoryID(),
							1,subscriber2.subscriptionClass(),
							new Date(),
							null, ss.loopStatus()
									+ "");
					if (getParamAsBoolean("SUPPORT_IBM_INTEGRATION", "FALSE")) {
						// IBM-Integration
						RBTCallBackEvent.update(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								ss.subID(), ss.refID(),
								RBTCallBackEvent.SM_SUCCESS_CALLBACK_RECEIVED, ss.classType());
					}
				}

				if(getParamAsBoolean("SEND_SMS_ON_CHARGE", "FALSE") && chargeClass.getSmschargeSuccess() != null
						&& !chargeClass.getSmschargeSuccess().equalsIgnoreCase("null")) {
					try {
						String smsText = getSelectionSMS(chargeClass.getSmschargeSuccess(), ss);
						Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
					}
					catch (Exception e) {
						logger.error("", e);
					}
				}
				
				/*
	    		 * Sms will not send to subscriber, if sms text is not configured or there is not song for deactivation
	    		 * Sending sms to user for his previous song is also available in his inbox until the expiry period of that song 
	    		 */
				try {
				Subscriber subscriber = rbtDBManager.getSubscriber(ss.subID());
				String smsText = Utility.getSmsTextForDeactivationSelection(subscriber.language(), wavFileNameList);
				if(smsText != null)
					Tools.sendSMS(getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")), ss.subID(), smsText, getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch(Exception e) {
					logger.error(e);
				}
				
				return true;
			}
			
			HashMap<String, String> selectionExtraInfoMap = null;
			boolean isDelayedDeact = false;
			if (ss.selStatus().equalsIgnoreCase("C"))
			{
				selectionExtraInfoMap = DBUtility.getAttributeMapFromXML(ss.extraInfo());
				// If sel status is C and ExtraInfo contains DELAY_DEACT="TRUE" then it is delayed deactivation request
				isDelayedDeact = (selectionExtraInfoMap != null && "TRUE".equalsIgnoreCase(selectionExtraInfoMap.get("DELAY_DEACT")));
				if (isDelayedDeact)
					logger.info("sel status is C and ExtraInfo contains DELAY_DEACT=TRUE");
			}

			String requestType = isDelayedDeact ? "DCT" : "ACT";

			boolean updateRefID = false;
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = ss.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& ((ss.selStatus().equalsIgnoreCase("C") && retryCount.startsWith("U"))
							|| (ss.selStatus().equalsIgnoreCase("A") && retryCount.startsWith("A"))))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = ss.nextRetryTime();
				if(noOfRetries >= maxRetries){
					boolean success  = rbtDBManager.updateSelStatusBasedOnRefID(ss.subID(), ss.refID(), "E");
					char loopStatus = ss.loopStatus();
					if(loopStatus == 'A'){
						loopStatus = 'l';
					}else if(loopStatus == 'B'){
						loopStatus = 'o';
					}
					if (success) {
						if (rbtDBManager.updateLoopStatus(ss, loopStatus, null)) {
							rbtDBManager.updatePlayerStatus(ss.subID(), "A");
						}
					}

					return false;
				}else if (nextRetryTime.after(new Date()))
					return false;
			}
			HashMap<String, String> resp = null;
			String success = null;
			String refID = null;

			resp = makeSubMgrRequest(null, ss, null, null, requestType , false);
			success = getSMResponseString(resp, RESPONSE);
			
			//RBT-14497 - Tone Status Check
			if (success != null && success.equalsIgnoreCase(CONTENT_EXPIRED)) {
				return true;
			}
			refID = getSMResponseString(resp, REFID);

			if(getSMResponseString(resp, REFID_CREATED) != null)
				updateRefID = true;

			logger.info("Selection activation request of subscriber: " + ss.subID() + "  - Response: " + success);
			//RBT-12195 - User block - unblock feature.
			if(success != null && success.equals(Constants.BLOCK_SUB_KEYWORD))
			{
				return false;
			}			
			if(refID == null)
			{
				logger.warn("RBT::Could not get RefID for sub " + ss.subID());
				return false;
			}
			
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				if(updateRefID)
					rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
							ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
				else
				{
					long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
					noOfRetries++;
					if (ss.selStatus().equalsIgnoreCase("C"))
						retryCount = "U" + noOfRetries;
					else
						retryCount = "A" + noOfRetries;

					Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
					rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
				}
				return false;
			}
			else if(success != null && success.startsWith("SUCCESS")) {
				String extraInfo = null;
				boolean updateExtraInfo = false;
				boolean successFlag = true;
				// RBT-12842-Unwanted Parameter In Upgrade Request From RBT
				String selMappedStr = getParamAsString(COMMON,
						"SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
				if (selMappedStr != null) {
					selectionExtraInfoMap = DBUtility.getAttributeMapFromXML(ss
							.extraInfo());
				}
				logger.info("Removed SEL_PARAMETERS_MAPPING_FOR_INTEGRATION parameters from extrainfo after getting sm response as success.Now extraInfoMap: "
						+ selectionExtraInfoMap);
				if (selMappedStr != null && selectionExtraInfoMap != null) {
					String str[] = selMappedStr.split(";");
					for (int i = 0; i < str.length; i++) {
						String s[] = str[i].split(",");
						if (s.length == 2
								&& selectionExtraInfoMap.containsKey(s[1])) {
							selectionExtraInfoMap.remove(s[1]);
							updateExtraInfo = true;
						}
						
					}
				}
				logger.info("Removed SEL_PARAMETERS_MAPPING_FOR_INTEGRATION parameters from extrainfo after getting sm response as success.Now extraInfoMap: "
						+ selectionExtraInfoMap);
				if (isDelayedDeact)
				{
					if (selectionExtraInfoMap != null)
					{
						selectionExtraInfoMap.remove("DELAY_DEACT");
						updateExtraInfo = true;
						successFlag = false;
					}
				}
				if (selectionExtraInfoMap != null && updateExtraInfo) {
					extraInfo = DBUtility
							.getAttributeXMLFromMap(selectionExtraInfoMap);
					if (extraInfo == null)
						extraInfo = "NULL";

				}
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), successFlag, false,sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), 
						getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), updateRefID, refID, ss.selInterval(),extraInfo, null);
				return true;
			}
			else if (success != null && success.startsWith("BLACKLISTED"))
			{
				rbtDBManager.deactivateSubscriber(ss.subID(), "BLACKLISTED", null, true, true, true, true, false);
			}
			else if(success != null && success.startsWith("BASE_DEACTIVE")) {
				String deactivateBy = subscriber2.deactivatedBy();
				if(!rbtDBManager.isSubscriberDeactivated(subscriber2) && !rbtDBManager.isSubscriberDeactivationPending(subscriber2)) {
					rbtDBManager.deactivateSubscriber(ss.subID(), "RECON", null, true, true, true, true, false);
					deactivateBy = "RECON";
				}
				rbtDBManager.smSelectionActivationRenewalFailure(ss.subID(),
						ss.refID(), deactivateBy, ss.prepaidYes()?"p":"n", ss.classType(), LOOP_STATUS_EXPIRED, ss.selType(),
						ss.extraInfo(), null);
				return false;
			}
			else if(success != null && success.trim().length() > 0
					&& !success.equalsIgnoreCase(FAILED)) {
				smURLSelectionActivation(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(), 
						ss.toTime(), false, true,sdf.format(ss.setTime()),ss.loopStatus(),ss.prepaidYes(), 
						getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), updateRefID, refID, ss.selInterval(), null);
				return true;
			}
			else if(updateRefID)
				rbtDBManager.smURLSelectionActivationRetry(ss.subID(), ss.callerID(), ss.status(), ss.setTime(), ss.fromTime(),
						ss.toTime(), getStartTime(ss.categoryID()), ss.selType(), ss.subscriberFile(), refID);
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				if (ss.selStatus().equalsIgnoreCase("C"))
					retryCount = "U" + noOfRetries;
				else
					retryCount = "A" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(ss.subID(), ss.refID(), retryCount, retryTime);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return false;
		}
		return false;
	}

	protected boolean isSMHitNotToBeMade(SubscriberStatus ss, String requestType) {
		int categoryId = ss.categoryID();
		Category category = RBTCacheManager.getInstance().getCategory(
				categoryId);
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(ss
				.extraInfo());
		if (category != null) {
			int categoryType = category.getCategoryTpe();
			if (RBTDBManager.catTypesForAutoRenewal != null
					&& RBTDBManager.catTypesForAutoRenewal.contains(String
							.valueOf(categoryType))) {
				String subscriberId = ss.subID();
				SubscriberDownloads download = RBTDBManager.getInstance()
						.getActiveSubscriberDownloadByStatus(subscriberId,
								null, "t", categoryId, categoryType);
				if (requestType.equalsIgnoreCase("DCT")) {
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("DCT_INSERT")) {
						return false;
					} else {
						if (download != null) {
							if (ss.refID().equals(download.refID())) {
								rbtDBManager.updateSelectionExtraInfoAndRefId(
										subscriberId, null, ss.refID(), UUID
												.randomUUID().toString());
							}
						}
						return true;
					}
				}
				if (download != null) {
					logger.info("subscriberId: " + subscriberId
							+ ", categoryId: " + categoryId
							+ ", isSMHitNotToBeMade: " + true);
					return true;
				}
			}

		}
		return false;
	}

	public boolean processActDownload(SubscriberDownloads ss) {
		/* This method is added for sending Subscriber Downloads for 
		 */
		
		HashMap<String, String> downloadExtraInfoMap = null;
		boolean isDelayedDeact = false;
		if (ss.downloadStatus() == STATE_DOWNLOAD_CHANGE)
		{
			downloadExtraInfoMap = DBUtility.getAttributeMapFromXML(ss.extraInfo());
			// If download status is C and ExtraInfo contains DELAY_DEACT="TRUE" then it is delayed deactivation request
			isDelayedDeact = (downloadExtraInfoMap != null && "TRUE".equalsIgnoreCase(downloadExtraInfoMap.get("DELAY_DEACT")));
		}

		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = ss.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& ((ss.downloadStatus() == STATE_DOWNLOAD_CHANGE && retryCount.startsWith("U"))
						|| (ss.downloadStatus() == STATE_DOWNLOAD_TO_BE_ACTIVATED && retryCount.startsWith("A"))))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = ss.nextRetryTime();
			if (noOfRetries >= maxRetries) {
				logger.info(" noOfRetries >= maxRetries so updating the status to 'e'");
				boolean success = rbtDBManager.updateDownloadStatus(ss.subscriberId(),
						ss.promoId(), 'e');
				if (success) {
					SubscriberStatus[] subscriberSelectionsNotDeactivated = rbtDBManager
							.getSubscriberSelectionsNotDeactivated(ss.subscriberId(), ss.promoId());
					if (subscriberSelectionsNotDeactivated != null) {
						for (SubscriberStatus subStatus : subscriberSelectionsNotDeactivated) {
							char loopStatus = subStatus.loopStatus();
							if (loopStatus == 'A')
								loopStatus = 'l';
							else if (loopStatus == 'B')
								loopStatus = 'o';
							rbtDBManager.updateLoopStatus(subStatus, loopStatus, null);
						}
						rbtDBManager.updatePlayerStatus(ss.subscriberId(), "A");
					}
				}
				return false;
			} else if (nextRetryTime.after(new Date()))
				return false;
		}

		String requestType = isDelayedDeact ? "DCT" : "ACT";
		String success = getSMResponseString(makeSubMgrRequest(null, null, ss, null, requestType, false), RESPONSE);
		logger.info("Download activation request of subscriber: " + ss.subscriberId() + "  - Response: " + success);
		
		//RBT-14497 - Tone Status Check
		if (success != null && success.equalsIgnoreCase(CONTENT_EXPIRED)) {
			return true;
		}
		if(success != null && success.equals(SM_URL_FAILURE))
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			if (ss.downloadStatus() == STATE_DOWNLOAD_CHANGE)
				retryCount = "U" + noOfRetries;
			else
				retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForDownload(ss.subscriberId(), ss.refID(), retryCount, retryTime);
			return false;
		}
		
		if(success != null && success.startsWith("SUCCESS"))
		{
			boolean isSuccess = true;
			boolean isError = false;
			String extraInfo = null;
			if (isDelayedDeact)
			{
				if (downloadExtraInfoMap != null)
				{
					downloadExtraInfoMap.remove("DELAY_DEACT");
					extraInfo = DBUtility.getAttributeXMLFromMap(downloadExtraInfoMap);
					if (extraInfo == null)
						extraInfo = "NULL"; 
				}
				isSuccess = false;
			}

			rbtDBManager.smURLDownloadActivation(ss.subscriberId(), isSuccess, isError, ss.refID(), null, extraInfo);
		}
		else if(success != null && success.startsWith("BASE_DEACTIVE")) {
			Subscriber subscriber = getSubscriber(ss.subscriberId());
			String deactivateBy = subscriber.deactivatedBy();
			if(!rbtDBManager.isSubscriberDeactivated(subscriber) && !rbtDBManager.isSubscriberDeactivationPending(subscriber)) {
				rbtDBManager.deactivateSubscriber(ss.subscriberId(), "RECON", null, true, true, true, true, false);
				deactivateBy = "RECON";
			}
			rbtDBManager.smUpdateDownloadRenewalCallback(ss.subscriberId(), ss.promoId(), ss.refID(), "FAILURE", false, "p", ss.classType(), deactivateBy);
		}
		else if (success != null && success.startsWith("BLACKLISTED"))
			rbtDBManager.deactivateSubscriber(ss.subscriberId(), "BLACKLISTED", null, true, true, true, true, false);
		else if(success != null && success.startsWith("ALREADY_DEACTIVE")) 
			rbtDBManager.smURLDownloadDeActivation(ss.subscriberId(), false, false, ss.refID(), ss.promoId());
		else if(success != null && success.trim().length() > 0
				&& !success.equalsIgnoreCase(FAILED))
		{
			if (isDelayedDeact)
			{
				rbtDBManager.smURLDownloadDeActivation(ss.subscriberId(), false, true, ss.refID(), ss.promoId());
				return true;
			}

			String classType = null;
			String extraInfo = null;
			boolean isError = true;
			if (ss.downloadStatus() == STATE_DOWNLOAD_CHANGE)
			{
				boolean isUpgradableLiteContent = false;
				if (liteUpgradeChargeClassMap != null)
				{
					Clip clip = getClipRBT(ss.promoId());
					isUpgradableLiteContent = clip != null && liteUpgradeChargeClassMap.containsKey(clip.getContentType());
				}

				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(ss.extraInfo());
				if (extraInfoMap != null && !isUpgradableLiteContent)
				{
					classType = extraInfoMap.remove("OLD_CLASS_TYPE");
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					if (extraInfo == null)
						extraInfo = "NULL";

					isError = false;
				}
			}

			rbtDBManager.smURLDownloadActivation(ss.subscriberId(), false, isError, ss.refID(), classType, extraInfo);
		}
		else
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			if (ss.downloadStatus() == STATE_DOWNLOAD_CHANGE)
				retryCount = "U" + noOfRetries;
			else
				retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForDownload(ss.subscriberId(), ss.refID(), retryCount, retryTime);
		}

		return true;
	}

	public boolean processDeactDownload(SubscriberDownloads ss) {
		Subscriber subscriber2 = getSubscriber(ss.subscriberId()); 
		List<String> actPendingStatusList = Arrays.asList("A,E,N,G".split(","));
		if (subscriber2 != null && actPendingStatusList.contains(subscriber2.subYes())
				&& getParamAsBoolean("COMBINED_CHARGING", "FALSE")) {
        	 logger.info("Subscriber is in act pending state, so not processing Download deactivation record = "+ss);
        	 return false;
        }
		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = ss.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& (ss.downloadStatus() == STATE_DOWNLOAD_TO_BE_DEACTIVATED && retryCount.startsWith("D")))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = ss.nextRetryTime();
			if (noOfRetries >= maxRetries){
				rbtDBManager.updateDownloadStatus(ss.subscriberId(), ss.promoId(), 'f');
				return false;
			}else if (nextRetryTime.after(new Date()))
				return false;
		}

		/* This method is added for sending Subscriber Downloads for 
		 */
		String success = getSMResponseString(makeSubMgrRequest(null, null, ss, null, "DCT", false), RESPONSE);
		logger.info("Download deactivation request of subscriber: " + ss.subscriberId() + "  - Response: " + success);
		
		if(success != null && success.equals(SM_URL_FAILURE))
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "D" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForDownload(ss.subscriberId(), ss.refID(), retryCount, retryTime);
			return false;
		}
		else if(success != null && success.startsWith("SUCCESS"))
			rbtDBManager.smURLDownloadDeActivation(ss.subscriberId(), true, false, ss.refID(), ss.promoId());
		else if(success != null && success.startsWith("ALREADY_DEACTIVE")) 
			rbtDBManager.smURLDownloadDeActivation(ss.subscriberId(), false, false, ss.refID(), ss.promoId());
		else if(success != null && success.startsWith("BASE_DEACTIVE")) {
			Subscriber subscriber = getSubscriber(ss.subscriberId());
			String deactivateBy = subscriber.deactivatedBy();
			if(!rbtDBManager.isSubscriberDeactivated(subscriber) && !rbtDBManager.isSubscriberDeactivationPending(subscriber)) {
				rbtDBManager.deactivateSubscriber(ss.subscriberId(), "RECON", null, true, true, true, true, false);
				deactivateBy = "RECON";
			}
			rbtDBManager.smUpdateDownloadRenewalCallback(ss.subscriberId(), ss.promoId(), ss.refID(), "FAILURE", false, "p", ss.classType(), deactivateBy);
		}
		else if (success != null && success.startsWith("BLACKLISTED"))
			rbtDBManager.deactivateSubscriber(ss.subscriberId(), "BLACKLISTED", null, true, true, true, true, false);
		else if(success != null && success.trim().length() > 0
				&& !success.equalsIgnoreCase(FAILED))
			rbtDBManager.smURLDownloadDeActivation(ss.subscriberId(), false, true, ss.refID(), ss.promoId());
		else
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "D" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForDownload(ss.subscriberId(), ss.refID(), retryCount, retryTime);
		}
		return true;
	}

	public boolean processSelectionsRen(SubscriberStatus selectionObj) {
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(selectionObj.classType());
		if(chargeClass.getSelectionPeriod() != null
				&& !chargeClass.getSelectionPeriod().equalsIgnoreCase("O"))
			return true;

		/*
		 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
		 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
		 */
		String retryCount = selectionObj.retryCount();
		int noOfRetries = 0;
		if (retryCount != null
				&& retryCount.startsWith("A"))
		{
			int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
			noOfRetries = Integer.parseInt(retryCount.substring(1));
			Date nextRetryTime = selectionObj.nextRetryTime();
			if (noOfRetries >= maxRetries || nextRetryTime.after(new Date()))
				return false;
		}

		HashMap<String, String> resp = makeSubMgrRequest(null, selectionObj, null, null, "REN", false);
		String success = getSMResponseString(resp, RESPONSE);
		logger.info("Selection renewal request of subscriber: " + selectionObj.subID() + "  - Response: " + success);
		
		if(success != null && success.equals(SM_URL_FAILURE))
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
			return false;
		}
		else if(success != null
				&& (success.equals(FAILED) || success.startsWith("ERROR") 
						|| success.startsWith("SUCCESS") || success.startsWith("SUCESS"))) {
			smUpdateSelStatus(selectionObj.subID(), selectionObj.selStatus(), "B");
			if(success.equals(FAILED) || success.startsWith("ERROR")) {
				try {
					Tools.sendSMS(getSenderNumber(selectionObj.circleId(), getParamAsString("SENDER_NO")), selectionObj.subID(), getParamAsString("RENEW_ERROR_MSG"), 
							getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch (Exception e) {
					logger.error("", e);
				}
			}
			else if(success.startsWith("SUCCESS") || success.startsWith("SUCESS")) {
				try {
					Tools.sendSMS(getSenderNumber(selectionObj.circleId(), getParamAsString("SENDER_NO")), selectionObj.subID(), getParamAsString("RENEW_SUCCESS_MSG"),
							getParamAsBoolean("SEND_SMS_MASS_PUSH", "FALSE"));
				}
				catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		else
		{
			long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
			noOfRetries++;
			retryCount = "A" + noOfRetries;

			Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
			rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
		}
		return true;
	}

	public boolean processSelectionsDct(SubscriberStatus selectionObj) {
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(selectionObj.classType());
		logger.info("RBT::Processing Selection "
				+ chargeClass.getSelectionPeriod() + " " + chargeClass.getRenewalAmount());
		Subscriber subscriber2 = getSubscriber(selectionObj.subID());
		List<String> actPendingStatusList = Arrays.asList("A,E,N,G".split(","));
		if (subscriber2 != null && actPendingStatusList.contains(subscriber2.subYes())
				&& getParamAsBoolean("COMBINED_CHARGING", "FALSE")) {
        	 logger.info("Subscriber is in act pending state, so not processing the record = "+selectionObj);
        	 return false;
         }
		boolean isSMHitNotToBeMade = isSMHitNotToBeMade(selectionObj, "DCT");
		
		if(isSMHitNotToBeMade || (chargeClass.getChargeClass() != null && chargeClass.getChargeClass().equalsIgnoreCase("TRIAL")
				|| (!getParamAsBoolean("SEND_ZERO_SEL_SM", "TRUE") 
						&& (chargeClass.getAmount() != null && Double.parseDouble(chargeClass.getAmount().replace(",", "."))==0
								&& chargeClass.getRenewalAmount() != null && Double.parseDouble(chargeClass.getRenewalAmount().replace(",", "."))==0)))) {
//				|| (chargeClass.getSelectionPeriod() != null && chargeClass.getSelectionPeriod().equalsIgnoreCase("O"))) {
			boolean success = rbtDBManager.smURLSelectionNotSendSMDeactivation(selectionObj.subID(), selectionObj.callerID(), selectionObj.status(), selectionObj.setTime(),false, false, selectionObj.subscriberFile(), selectionObj.selType(), selectionObj.loopStatus());
			if (success) {
				
				//RBT-14044	VF ES - MI Playlist functionality for RBT core
				 if(!selectionObj.deSelectedBy().equals("SM") && (selectionObj.callerID() == null || selectionObj.callerID().equalsIgnoreCase("all")) && selectionObj.status()==1 && !Utility.isShuffleCategory(selectionObj.categoryType())) {
				  String resp = rbtDBManager.removeMiPlaylistDownloadTrack(selectionObj.subID(),selectionObj.subscriberFile(),
						  selectionObj.categoryID(), selectionObj.categoryType(),selectionObj.callerID(),selectionObj.status());
				  logger.info("Response of removeMiPlaylistDownloadTrack in Daemon manager processSelectionsDct : "+resp);
				 }
				  
				
				TransLogForSelection.writeTransLogForSelection(
						selectionObj.circleId(), selectionObj.subID(),
						selectionObj.callerID(), selectionObj.selType(),
						selectionObj.fromTime(), selectionObj.toTime(),
						selectionObj.selInterval(),
						selectionObj.categoryType(), selectionObj.status(),
						selectionObj.subscriberFile(),
						selectionObj.categoryID(), 2,
						subscriber2.subscriptionClass(), selectionObj.startTime(),
						new Date(), selectionObj.loopStatus() + "");
			}
		}
		else if(chargeClass.getSelectionPeriod() != null) {
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = selectionObj.retryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& (selectionObj.selStatus().equalsIgnoreCase("D") && retryCount.startsWith("D")))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = selectionObj.nextRetryTime();
				if (noOfRetries >= maxRetries){
					rbtDBManager.updateSelStatusBasedOnRefID(selectionObj.subID(), selectionObj.refID(), "E");
					return false;
				}else if (nextRetryTime.after(new Date())){
					return false;
				}
			}
			HashMap<String, String> resp = null;
			String success = null;
			String refID = null;

			resp = makeSubMgrRequest(null, selectionObj, null, null, "DCT", false);
			success = getSMResponseString(resp, RESPONSE);
			refID = getSMResponseString(resp, REFID);

			logger.info("Selection deactivation request of subscriber: " + selectionObj.subID() + "  - Response: " + success);
			
			if(success != null && success.equals(SM_URL_FAILURE))
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
				return false;
			}
			else if(success != null && success.trim().length() > 0 && success.startsWith("SUCCESS")){
				// the subscriber sel_statius is set to P, end date to deactivation date and next charging date to null
			boolean check = smURLSelectionDeactivation(selectionObj.subID(), refID, true, false, selectionObj.selType());
				if (check) {
					TransLogForSelection.writeTransLogForSelection(
							selectionObj.circleId(), selectionObj.subID(),
							selectionObj.callerID(), selectionObj.selType(),
							selectionObj.fromTime(), selectionObj.toTime(),
							selectionObj.selInterval(),
							selectionObj.categoryType(), selectionObj.status(),
							selectionObj.subscriberFile(),
							selectionObj.categoryID(),
							2, subscriber2.subscriptionClass(),
							selectionObj.startTime(), new Date(),
							selectionObj.loopStatus() + "");
				}
				return true;
			}
			else if(success != null && success.trim().length() > 0
					&& success.equalsIgnoreCase("ALREADY_DEACTIVE")){

				if(selectionObj.refID() == null) 
				{
					Connection conn = rbtDBManager.getConnection(); 
					
					try
					{
						rbtDBManager.updateRefIDSelectionOldLogic(conn, selectionObj.subID(), selectionObj.callerID(), selectionObj.status(), sdf.format(selectionObj.setTime()),
							selectionObj.fromTime(), selectionObj.toTime(), selectionObj.subscriberFile(), refID);
					}
					catch(Throwable e)
					{
						logger.error("Exception before release connection", e);
					}
					finally
					{
						rbtDBManager.releaseConnection(conn);
					}
				}
				// set sel_status to X
				rbtDBManager.smSelectionDeactivationSuccess(selectionObj.subID(), refID, LOOP_STATUS_EXPIRED_INIT, selectionObj.selType(),null);
				return true;
			}
			else if(success != null && success.startsWith("BASE_DEACTIVE")) {
				String deactivateBy = subscriber2.deactivatedBy();
				if(!rbtDBManager.isSubscriberDeactivated(subscriber2) && !rbtDBManager.isSubscriberDeactivationPending(subscriber2)) {
					rbtDBManager.deactivateSubscriber(selectionObj.subID(), "RECON", null, true, true, true, true, false);
					deactivateBy = "RECON";
				}
				rbtDBManager.smSelectionActivationRenewalFailure(selectionObj.subID(),
						selectionObj.refID(), deactivateBy, selectionObj.prepaidYes()?"p":"n", selectionObj.classType(), LOOP_STATUS_EXPIRED, selectionObj.selType(),
								selectionObj.extraInfo(), null);
			}
			else if(success != null && success.trim().length() > 0
					&& !success.equalsIgnoreCase(FAILED)){
				// set sel_status to F
				smURLSelectionDeactivation(selectionObj.subID(), refID, false, true, selectionObj.selType());
				return true;
			}
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForSelection(selectionObj.subID(), selectionObj.refID(), retryCount, retryTime);
			}
		}
		return false;
	}
	
	public boolean processPacksAct(ProvisioningRequests provisioningReqsObj){
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(provisioningReqsObj.getChargingClass());
		try
		{
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = provisioningReqsObj.getRetryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& retryCount.startsWith("A"))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = provisioningReqsObj.getNextRetryTime();
				if(noOfRetries >= maxRetries){
					rbtDBManager.smActFailureSuccess(provisioningReqsObj.getSubscriberId(), provisioningReqsObj.getType()+"", PACK_ACTIVATION_ERROR+"", false);
					return false;
				}else if (nextRetryTime.after(new Date()))
					return false;
			}

			String success = getSMResponseString(makeSubMgrRequest(null, null, null, provisioningReqsObj, "ACT", false), RESPONSE);
			if(success != null && success.startsWith("SUCCESS")) {
				smURLPackActivation(provisioningReqsObj.getSubscriberId());// change status from 1 ie. PACK_TO_BE_ACTIVATED to 2 ie. PACK_ACTIVATION_PENDING  in provisioning_requests table 
			}
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "A" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForPack(provisioningReqsObj.getSubscriberId(), provisioningReqsObj.getTransId(), retryCount, retryTime);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return false;
		}
		return false;
	}
	
	public boolean processPacksDct(ProvisioningRequests provisioningReqsObj){
		Subscriber subscriber2 = getSubscriber(provisioningReqsObj.getSubscriberId());  
		List<String> actPendingStatusList = Arrays.asList("A,E,N,G".split(","));
		if (subscriber2 != null && actPendingStatusList.contains(subscriber2.subYes())
				&& getParamAsBoolean("COMBINED_CHARGING", "FALSE")) {
        	 logger.info("Subscriber is in act pending state, so not processing pack deactivation = "+provisioningReqsObj);
        	 return false;
        }
		ChargeClass chargeClass = m_rbtChargeClassCacheManager.getChargeClass(provisioningReqsObj.getChargingClass());
		try
		{
			logger.info("Processing packs Deact");
			/*
			 * Retry count in DB starts with either 'A', 'D' or 'U'. A for Activation, D for Deactivation and U for Upgradation.
			 * Added to check if the retry count has reached the max limit or not. If reached, then don't hit SM.
			 */
			String retryCount = provisioningReqsObj.getRetryCount();
			int noOfRetries = 0;
			if (retryCount != null
					&& retryCount.startsWith("D"))
			{
				int maxRetries = RBTParametersUtils.getParamAsInt(DAEMON, "SM_MAX_RETRIES_ALLOWED", 3);
				noOfRetries = Integer.parseInt(retryCount.substring(1));
				Date nextRetryTime = provisioningReqsObj.getNextRetryTime();
				if(noOfRetries >= maxRetries){
				   rbtDBManager.smActFailureSuccess(provisioningReqsObj.getSubscriberId(), provisioningReqsObj.getType()+"", PACK_DEACTIVATION_ERROR+"", false);
				   return false;
				}else if (nextRetryTime.after(new Date())){
					return false;
				}
			}

			String success = getSMResponseString(makeSubMgrRequest(null, null, null, provisioningReqsObj, "DCT", false), RESPONSE);
			if(success != null && success.startsWith("SUCCESS")) {
				smURLPackDeactivation(provisioningReqsObj.getSubscriberId());// change status from 1 ie. PACK_TO_BE_ACTIVATED to 2 ie. PACK_ACTIVATION_PENDING  in provisioning_requests table 
			}
			else
			{
				long retryTimeMins = RBTParametersUtils.getParamAsLong(DAEMON, "SM_DAEMON_RETRY_TIME_IN_MINS", 30);
				noOfRetries++;
				retryCount = "D" + noOfRetries;

				Date retryTime = new Date(System.currentTimeMillis() + noOfRetries * retryTimeMins * 60 * 1000);
				rbtDBManager.updateRetryCountAndTimeForPack(provisioningReqsObj.getSubscriberId(), provisioningReqsObj.getTransId(), retryCount, retryTime);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return false;
		}
		return false;
	}
	
		//RBT-14497 - Tone Status Check
		private int getNumOfDaysLeftForExpiration(Date expirydate) {
			logger.info("NUM_OF_DAYS_LEFT");
			int diffInDays = 0;
			Date currDate = new Date();
			
			if (expirydate != null) {
				logger.info("EXPIRY_DATE_IS: "+expirydate);
				try {
					long difference = expirydate.getTime() - currDate.getTime();
					if (difference >= (24 * 60 * 60 * 1000)) {
						double diffInDecimal = (double) difference / (24 * 60 * 60 * 1000);
						if (diffInDecimal % 1 == 0) {
							diffInDays = (int) diffInDecimal;
						} else {
							diffInDays = (int) (diffInDecimal + 1);
						}
					} else if (difference > 0 && difference < (24 * 60 * 60 * 1000)) {
						diffInDays = 1;
					} else {
						diffInDays = 0;
					}
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			
			logger.info("NUMBER_OF_DAYS_LEFT "+diffInDays);
			return diffInDays;
		}

		//RBT-14497 - Tone Status Check
		private boolean isContentEligibleForRenewal(Date expiryDate) {
			boolean isContentExpired = false;	
			
			logger.info("IS_CONTENT_ELIGIBLE_FOR_RENEWAL");
			logger.info("Expiry_Date "+expiryDate);
			if (new Date().compareTo(expiryDate) == -1 || new Date().compareTo(expiryDate) == 0) {
				
				int minDayBeforeRenewal = getParamAsInt("MIN_DAY_BEFORE_RENEWAL", -1);
				int daysLeft = getNumOfDaysLeftForExpiration(expiryDate);
				
				logger.info("MIN_DAY: "+minDayBeforeRenewal+" DAYS_LEFT: "+daysLeft);
				
				if (minDayBeforeRenewal != -1 && daysLeft <= minDayBeforeRenewal) {
					isContentExpired = true;
				} else {
					isContentExpired = false;
				}
			} else  {
				isContentExpired = true;
			}
			
			logger.info("IS_EXPIRED: "+isContentExpired);
			return isContentExpired;
		}
		
	
	//RBT-14497 - Tone Status Check
		private boolean statusCheckBeforeRenewal (SubscriberStatus subscriberStatus, SubscriberDownloads downloads) {
			logger.info("statusCheckBeforeRenewal METHOD");
			
			boolean isContentExpired = false;
			
			int categoryId = -1;
			int categoryType = -1;
			String wavFile = null;
			String subscriebrId = null;
			
			if (subscriberStatus != null) { // If Selection Model
				logger.info("SUBSCRIBER_ID:"+subscriberStatus.subID());
				if (subscriberStatus.selStatus().equalsIgnoreCase("A") || subscriberStatus.selStatus().equalsIgnoreCase("W")) {
					subscriebrId = subscriberStatus.subID();
					categoryId = subscriberStatus.categoryID();
					categoryType = subscriberStatus.categoryType();
					wavFile = subscriberStatus.subscriberFile();
				}
			} else if (downloads != null) { // If Download Model
				if (downloads.downloadStatus() == 'n' || downloads.downloadStatus() == 'w') {
					logger.info("SUBSCRIBER_ID:"+downloads.subscriberId());
					subscriebrId = downloads.subscriberId();
					categoryId = downloads.categoryID();
					categoryType = downloads.categoryType();
					wavFile = downloads.promoId();
				}
			}
			
			if(subscriebrId != null) { // Check For Activation Request
				if (Utility.isShuffleCategory(categoryType)) { // If Shuffle type
					logger.info("SUBSCRIBER_ID: "+subscriebrId+" download is of SHUFFLE_TYPE");
					isContentExpired = isCategoryContentExpired(categoryId);
				
				} else { // If not Shuffle Type
					logger.info("SUBSCRIBER_ID: "+subscriebrId+" download is not of SHUFFLE_TYPE");
					isContentExpired = isClipContentExpired(wavFile);
				}
				logger.info("SUBSCRIBER_ID: " + subscriebrId + ",CategoryId: "
						+ categoryId + ",categoryType: " + categoryType
						+ ",SONG: " + wavFile + " is expired " +isContentExpired);
			}
			return isContentExpired;
		}
		
		//RBT-14497 - Tone Status Check
		private boolean isCategoryContentExpired(int categoryId) {
			
			boolean isCategoryExpired = false;
			
			logger.info("isCategoryContentExpired METHOD");
			
			rbtCacheManager = RBTCacheManager.getInstance();
			Category category = rbtCacheManager.getCategory(categoryId);
			if (category == null) {
				logger.info("CATEGORY IS NULL for Category ID:  "+categoryId);
				isCategoryExpired = true;
				return isCategoryExpired;
			}
			Date catEndTime = category.getCategoryEndTime();
			isCategoryExpired = isContentEligibleForRenewal(catEndTime);
			
			return isCategoryExpired;
		}
		
		//RBT-14497 - Tone Status Check
		private boolean isClipContentExpired(String wavFileName) {
			logger.info("isClipContentExpired METHOD");
			boolean isClipExpired = false;
			rbtCacheManager = RBTCacheManager.getInstance();
			Clip clip = rbtCacheManager.getClipByRbtWavFileName(wavFileName);
			
			if (clip == null) {
				logger.info("CLIP IS NULL For WAV FILE: "+wavFileName);
				isClipExpired = true;
				return isClipExpired;
			} 
			
			Date clipEndTime = clip.getClipEndTime();
			isClipExpired = isContentEligibleForRenewal(clipEndTime);
		
			return isClipExpired;
		}
		
		//RBT-14497 - Tone Status Check
		private HashMap<String,String> processDeactContentExpired (Subscriber subscriber,SubscriberStatus subscriberStatus,SubscriberDownloads downLoads,boolean isCombo) {
			logger.info("processDeactContentExpired METHOD");
			HashMap<String,String> requestMap = new HashMap<String,String>();
			String deactivatedby = getParamAsString("TONE_STATUS_RENEWAL_DCT_MODE");
			String type = "n";
			String deactStatus = null;

			if (subscriber.prepaidYes()) {			
				type = "p";
			}			

			if (downLoads != null){				

				boolean noDownloadDeactSub = Utility.isNoDownloadDeactSub(subscriber);
				logger.info("NO_DOWNLOAD_DEACT is "+noDownloadDeactSub);
				
				 deactStatus = rbtDBManager.smUpdateDownloadRenewalCallback(downLoads.subscriberId(), downLoads.promoId(),
						downLoads.refID(),"FAILURE",noDownloadDeactSub, type, downLoads.classType(),
						deactivatedby);

				logger.info("DOWNLOAD DEACTIVATION STATUS "+deactStatus);
				if(deactStatus.equals("SUCCESS")) {
					requestMap.put(RESPONSE, CONTENT_EXPIRED);
				}
			}

			else if (subscriberStatus != null) {
				char newLoopStatus = 'X';

				deactStatus = rbtDBManager.smSelectionActivationRenewalFailure(subscriberStatus.subID(),
						subscriberStatus.refID(),deactivatedby, type, subscriberStatus.classType(), newLoopStatus,
						subscriberStatus.selType(), subscriberStatus.extraInfo(),null);

				logger.info("SELECTION DEACTIVATION STATUS "+deactStatus);				
				
				if(deactStatus.equals("SUCCESS")) {
					requestMap.put(RESPONSE, CONTENT_EXPIRED);
				}

			}			
			if (isCombo && deactStatus.equals("SUCCESS")) {

				String subDCTStatus  = rbtDBManager.smSubscriptionRenewalFailure(subscriber.subID(),
						deactivatedby, type, subscriber.subscriptionClass(), false, subscriber.extraInfo(),
						false, false, null);

				logger.info("SUBSCRIBER DEACT STATUS IN DOWNLOAD MODEL"+subDCTStatus);
			} else if(deactStatus.equals("SUCCESS")){//RBT-14497 - Tone Status Check
				if(subscriber!=null){
					int noOfSel = subscriber.maxSelections();
					int noOfCurrentSel = noOfSel - 1;
					if(noOfCurrentSel >= 0){
						rbtDBManager.updateNumMaxSelections(subscriber.subID(), noOfCurrentSel);
					}
				}
			}
			
			return requestMap;
		}



	/*
	 * It is used to make the hit at submanager and depending on response the return string is defined. 
	 * */
	protected HashMap<String, String> makeSubMgrRequest(Subscriber subscriber, SubscriberStatus subscriberStatus, SubscriberDownloads downLoads, 
			ProvisioningRequests provisioningRequests, String requestType, boolean sendSubTypeUnknown) 
	{
		String url = null;
		String response = null;
		String subID = null;
		HashMap<String, String> requestMap = null;
		//TIMESTAMP,MSISDN,CIRCLEID,TIMETAKEN,SM_RESPONSE,ENTITY,TYPE,MODE,SRVKEY,SUB_TYPE,REFID,CLIP_ID,CLIP_TYPE,CAT_ID,CAT_TYPE,INFO
		long startTime = -1;
		long endTime = -1;
		int statusCode = 0;
		
		HashMap<String, String> loggingInfoMap = new HashMap<String, String>();
		//Moved for TTG-14814
		HashMap<String, Object> resp = new HashMap<String, Object>();
		try
		{
			if(subscriber != null)
				subID = subscriber.subID();
			else if(subscriberStatus != null)
				subID = subscriberStatus.subID();
			else if(downLoads != null)
				subID = downLoads.subscriberId();
			else if(provisioningRequests != null)
				subID = provisioningRequests.getSubscriberId();
			//Start:RBT-12195 - User block - unblock feature.
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			boolean isBlockedSub = rbtDBManager.isBlackListSub(subID);
			if ((requestType.equalsIgnoreCase("ACT") && (subscriber != null || subscriberStatus != null || downLoads != null)) && isBlockedSub) {
				SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
						subID);
				logger.info("processBlockSubRequest : " + subID);
				String SMS_MODE = RBTParametersUtils.getParamAsString(COMMON,
						"DEACTIVATION_SMS_BLOCK_SUB_MODE", "SMS");
				subscriptionRequest.setMode(SMS_MODE);
				subscriptionRequest.setIsDirectDeactivation(true);
				RBTClient.getInstance().deactivateSubscriber(
						subscriptionRequest);
				String blockedUserSmsText = CacheManagerUtil
						.getSmsTextCacheManager().getSmsText(
								"BLOCKED_USER_TEXT", subscriber.language());
				if (blockedUserSmsText != null)
					Tools.sendSMS(getParamAsString("SENDER_NO"), subID,
							blockedUserSmsText, false);
				requestMap = new HashMap<String, String>();
				requestMap.put(RESPONSE, Constants.BLOCK_SUB_KEYWORD);
				return requestMap;
			}// End:RBT-12195 - User block - unblock feature.
			boolean isBasePreChrg = false;
			boolean packCombinedCharging = true;
			boolean ispackCombinedCharging = false;

			CosDetails packCosDetail = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			
			
			if (subscriber != null && getParamAsBoolean("SEND_ACTIVE_EASY","FALSE") && subscriber.subYes().equalsIgnoreCase("A")) 
			{
				String selected = subscriber.activatedBy();
				if (selected != null && Arrays.asList(getParamAsString("DAEMON", "ACTIVATED_PRE_CHRG", " ").toLowerCase().split(",")).contains(selected.toLowerCase()))
					isBasePreChrg = true;
			}
			Map<String, String> extraInfoMap = null;
			if(subscriber != null)
				extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());

			ProvisioningRequests provReq = null;
			if(extraInfoMap != null && extraInfoMap.containsKey(PACK)){

				String[] cosID = extraInfoMap.get(PACK).split(",");
				if(cosID != null && cosID.length>0)
					packCosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID[0]);
				if(packCosDetail != null)
				{

					List<ProvisioningRequests> pr = ProvisioningRequestsDao.getPacksToBeActivatedBySubscriberIDAndType(subID, Integer.parseInt(packCosDetail.getCosId()));
					if(pr == null || pr.size()<1)
						packCombinedCharging = false;
					else
						provReq = pr.get(0);
					logger.info("Pack cos detail is not null and set packCombinedCharging as " + packCombinedCharging);
				}

			}
			boolean isCombinedChrg = getParamAsBoolean("COMBINED_CHARGING", "FALSE");

			SubscriberStatus linkedSubStatus = null;
			SubscriberDownloads linkedDownload = null;

			String smRequestType = null;
			
			/*
			 * Combined charging : Used when user activates service along with selection. Here the charging request for both is passed together.
			 * isBasePreChrg is used if the charging has already been done through some other means. 
			 * */



			String comUrl = null;
			if(isCombinedChrg && !isBasePreChrg && requestType.equals("ACT") && subscriber != null && subscriber.subYes().equalsIgnoreCase("A") && packCombinedCharging)
			{
				CosDetails cosDetail = null;
				if(getParamAsBoolean("IS_SONGPACK_ENABLED", "FALSE") || getParamAsBoolean("LIMITED_DOWNLOAD_ENABLED", "FALSE"))
				{
					cosDetail = packCosDetail;
				}

				if (extraInfoMap != null
						&& extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_COS_ID)
						&& extraInfoMap.containsKey(EXTRA_INFO_TOBE_ACT_OFFER_ID))
				{
					String cosID = extraInfoMap.get(iRBTConstant.EXTRA_INFO_COS_ID);
					cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
				}
				if (cosDetail != null
						&& (SONG_PACK.equalsIgnoreCase(cosDetail.getCosType()) 
								|| UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
								|| LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetail.getCosType())
								|| MUSIC_POUCH.equalsIgnoreCase(cosDetail.getCosType())
								|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetail.getCosType())
								|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetail.getCosType())))
				{
					if(packCombinedCharging)
						ispackCombinedCharging = true;
					requestMap = getComboAddParamsForSongPack(subID, subscriber, cosDetail);
					// RBT-14301: Uninor MNP changes.
					if(requestMap!=null && requestMap.containsKey(URL))
						url = requestMap.get(URL);

					Map<String, String> packExtraInfoMap = DBUtility.getAttributeMapFromXML((provReq != null) ? provReq.getExtraInfo() : null);
					if (requestMap!=null && packExtraInfoMap != null && packExtraInfoMap.containsKey(EXTRA_INFO_OFFER_ID)) {
						url += "&offerid=" + packExtraInfoMap.get(EXTRA_INFO_OFFER_ID);
					}
					if(requestMap!=null && requestMap.containsKey(REQUEST_TYPE))
						smRequestType = requestMap.get(REQUEST_TYPE);
				}
				else
				{
					resp = getComboAddParams(subID, subscriber);
					if(resp.containsKey(URL))
						comUrl = (String) resp.get(URL);

					boolean isContentExpired = false;
					// TODO
					if (resp.containsKey("LINKED_SELECTION_EXISTS")) {//RBT-14497 - Tone Status Check
						if (enable_Tone_Status_Check) {
							if (resp.containsKey("SUBSCRIBER_SELECTION")) {
								linkedSubStatus = (SubscriberStatus) resp
										.get("SUBSCRIBER_SELECTION");
								
								logger.info("COMBO_REQUEST SUBSCRIBER_SELECTION_MODEL");
								
								isContentExpired = statusCheckBeforeRenewal(linkedSubStatus, null);
								
								if (isContentExpired) {
									
									logger.info("SUBSCRIBER_ID: "
											+ subscriberStatus.subID() + " SONG: "
											+ subscriberStatus.subscriberFile()
											+ " is expired");
									
									requestMap = processDeactContentExpired(subscriber,linkedSubStatus,null,true);
									
									if(requestMap.size() != 0) {
										return requestMap;
									}
								}
							}
							else if (resp.containsKey("SUBSCRIBER_DOWNLOAD")) {
								linkedDownload = (SubscriberDownloads) resp
										.get("SUBSCRIBER_DOWNLOAD");
								logger.info("SUBSCRIBER_DOWNLOAD_MODEL");
								isContentExpired = statusCheckBeforeRenewal(null, linkedDownload);
								
								if (isContentExpired) {
									
									logger.info("SUBSCRIBER_ID: "
											+ linkedDownload.subscriberId() + " SONG: "
											+ linkedDownload.promoId()
											+ " is expired");
									
									logger.info("Combo Case Subscriber DOWNLOAD "+subscriber);
									requestMap = processDeactContentExpired(subscriber,null, linkedDownload,true);
									
									if(requestMap.size() != 0) {
										return requestMap;
									}
								}
							}
						} else {
							if (resp.containsKey("SUBSCRIBER_SELECTION"))
								linkedSubStatus = (SubscriberStatus) resp
										.get("SUBSCRIBER_SELECTION");
							else if (resp.containsKey("SUBSCRIBER_DOWNLOAD"))
								linkedDownload = (SubscriberDownloads) resp
										.get("SUBSCRIBER_DOWNLOAD");
						}
						
						requestMap = getSMURL(null, linkedSubStatus, linkedDownload, provisioningRequests, requestType,
								sendSubTypeUnknown, loggingInfoMap);

						if(requestMap != null)
						{
							if(requestMap.containsKey(URL))
								url = requestMap.get(URL);

							if(requestMap.containsKey(REQUEST_TYPE))
								smRequestType = requestMap.get(REQUEST_TYPE);

						}
					}
					else
					{
						requestMap = getSMURL(subscriber, subscriberStatus, downLoads, provisioningRequests, 
								requestType, sendSubTypeUnknown, loggingInfoMap);

						if(requestMap != null)
						{
							if(requestMap.containsKey(URL))
								url = requestMap.get(URL);

							if(requestMap.containsKey(REQUEST_TYPE))
								smRequestType = requestMap.get(REQUEST_TYPE);
						}

					}
					if(comUrl != null)
					{
						url += comUrl;
						smRequestType += "_COMBINED";
					}
				}
			} // Combined charging code block ends here
			else
			{
				// Normal activation cases
				if (enable_Tone_Status_Check) {//RBT-14497 - Tone Status Check
					boolean isContentExpired = false;				
					isContentExpired = statusCheckBeforeRenewal(subscriberStatus, downLoads);
					
					if (isContentExpired) {
						
						
						logger.info("Normal Case Subscriber SELECTION "+subscriber);
						requestMap = processDeactContentExpired(getSubscriber(subID), subscriberStatus,downLoads,false);
						
						if(requestMap.size() != 0) {
							return requestMap;
						}
					}
				}
				
				requestMap = getSMURL(subscriber, subscriberStatus, downLoads, provisioningRequests,
						requestType, sendSubTypeUnknown, loggingInfoMap);
				
				if(requestMap != null)
				{
					if(requestMap.containsKey(URL))
						url = requestMap.get(URL);

					if(requestMap.containsKey(REQUEST_TYPE))
						smRequestType = requestMap.get(REQUEST_TYPE);

				}
			}
			if(isCombinedChrg && !isBasePreChrg && requestType.equals("ACT") && subscriber != null && subscriber.subYes().equalsIgnoreCase("A"))
			{
				if(resp.containsKey("LINKED_SELECTION_EXISTS"))
					loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_COMBO_CONTENT);
				else
					loggingInfoMap.put(SMHitLogger.ENTITY, SMHitLogger.ENTITY_COMBO_BASE);
			}
			if(url == null)
				return null;
			else if(url.equalsIgnoreCase("DCT_PENDING"))
			{
				requestMap.put(RESPONSE, "DCT_PENDING");
				return requestMap;
			}

			//Added by sreekar for BAC, changing mode to probe
			String numbersStr = CacheManagerUtil.getParametersCacheManager().getParameter(MONITOR,"MONITER_NUMBERS", "").getValue();
			List<String> numberList = Arrays.asList(numbersStr.split(","));

			boolean numberPresent = numberList.contains(subID)? true: false;
			int index = url.indexOf("&mode=");
			if (numberPresent && index != -1) 
			{
				String tempURL = url.substring(0, index + 6) + "PROBE";
				int nextParamIndex = url.indexOf("&", index + 6);
				if(nextParamIndex != -1)
					tempURL +=  url.substring(nextParamIndex);
				url = tempURL;
			}
			//end of BAC changes

			logger.info("RBT::SM URL hit " + url);

			statusCode = 0;
			try 
			{
				logger.debug("SMHitLogger URL==="+url);
				loggingInfoMap.put(SMHitLogger.URL, url);
				SMDaemonLogger.writeSMDaemonTransactionLog(subscriber, subscriberStatus, downLoads, provisioningRequests, url);
				startTime = System.currentTimeMillis();
				HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(url, null);
				endTime = System.currentTimeMillis();

				response = httpResponse.getResponse();
				statusCode = httpResponse.getResponseCode();

				logger.info("SM statusCode: " + statusCode + " Response: " + response);

				if(requestType.trim().equalsIgnoreCase("DIRECTDCT") && response != null) 
				{
					String strResp = parseResponse(response);
					if(resp != null) 
					{
						try 
						{
							statusCode = Integer.parseInt(strResp.substring(0, strResp.indexOf("|")));
							response = strResp.substring(strResp.indexOf("|") + 1);
						}
						catch (Exception e)
						{
						}
					}
				}

				//writeTrans(smRequestType, url, statusCode, response, (endTime - startTime));
				requestMap = processSubMgrResponse(statusCode, response, requestType, smRequestType, requestMap);

			}
			catch (Throwable e)
			{
				logger.error("Error while making SM request", e);
				//if(e != null)
				//writeTrans(smRequestType, url, statusCode, e.getMessage(), 0);
				requestMap.put(RESPONSE, SM_URL_FAILURE);
				response = getHttpExceptionString(e);
				return requestMap;
			}

			// Below code block execute only when status code is 200 
			String refID = null;
			if(requestMap.containsKey(REFID))
				refID = requestMap.get(REFID);
			if(isCombinedChrg && statusCode == 200 && response != null && response.indexOf("SUCCESS") != -1)
			{
				if (linkedSubStatus != null)
				{	
					try
					{
						smURLSelectionActivation(
								linkedSubStatus.subID(),
								linkedSubStatus.callerID(),
								linkedSubStatus.status(),
								linkedSubStatus.setTime(),
								linkedSubStatus.fromTime(),
								linkedSubStatus.toTime(),
								true,
								false, 
								sdf.format(linkedSubStatus.setTime()),
								linkedSubStatus.loopStatus(),
								linkedSubStatus.prepaidYes(),
								getStartTime(linkedSubStatus.categoryID()),
								linkedSubStatus.selType(),
								linkedSubStatus.subscriberFile(),
								(requestMap != null && requestMap.containsKey("NEWREF")),
								refID, linkedSubStatus.selInterval(), null);
						rbtDBManager.deactivateNewSelections(linkedSubStatus.subID(), "Daemon", linkedSubStatus.callerID(), linkedSubStatus.setTime(), true,linkedSubStatus.status()+""); 
					}
					catch(Exception e)
					{
						logger.error("", e);
					}
				}
				else if(linkedDownload != null)
				{
					// Updating the download to TO_BE_ACTIVATED state after successful combined charging request
					rbtDBManager.smURLDownloadActivation(linkedDownload.subscriberId(), true, false, linkedDownload.refID(), null, null);
				}
				else if(ispackCombinedCharging)
				{  
					//Updating pack to to be activated state after successful combined charging
					rbtDBManager.smUpdateSpecificPackStatusOnBaseAct(subscriber.subID(),packCosDetail.getCosId());

				}
			}

		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in hitting SM", e);
			response = "SOME_EX";
		}
		finally
		{
			loggingInfoMap.put(SMHitLogger.SM_RESPONSE_STRING, response);
			loggingInfoMap.put(SMHitLogger.SM_RESPONSE_CODE, String.valueOf(statusCode));
			loggingInfoMap.put(SMHitLogger.TIME_TAKEN, String.valueOf(endTime - startTime));
			//Added for TTG-14814
			if (resp!=null && resp.size()>0){
				if(resp.containsKey("LINKED_REF_ID") && resp.get("LINKED_REF_ID") != null)
				loggingInfoMap.put("LINKED_REF_ID", resp.remove("LINKED_REF_ID")
						.toString());
			} else if (requestMap!=null && requestMap.size()>0)
			{
				if(requestMap.containsKey("LINKED_REF_ID") && requestMap.get("LINKED_REF_ID") != null)
				loggingInfoMap.put("LINKED_REF_ID",
						requestMap.remove("LINKED_REF_ID"));
			}
			//End of TTG-14814
			SMHitLogger.writeCallBackTransactionLog(loggingInfoMap);
		}
		return requestMap;
	}
	
	
	private String getHttpExceptionString(Throwable e)
	{
		if(e instanceof ConnectException || e instanceof ConnectTimeoutException)
			return "HTTP_CONN_EX";
		else if (e instanceof NoHttpResponseException)
			return "NO_HTTP_RES_EX";
		else if (e instanceof SocketTimeoutException)
			return "READ_TIMEOUT_EX";
		return "SOME_EXCEP";
	}
	private HashMap<String, String> processSubMgrResponse(int statusCode, String response, String requestType, String smRequestType, HashMap<String, String> requestMap)
	{
		/**
		 * requestType -> ACT/DCT/REN/REALTIME/DIRECTDCT
		 * smRequestType -> BASE/SEL/DOWNLOAD-ACT/DCT/REN/UPG/EVT/DIRECTDCT
		 */
		if (response != null)
			response = response.trim();
		logger.info("RBT:: SmrequestType: " + smRequestType + " & requestType :"+requestType);
		if(statusCode == 500 || (statusCode >= 600 && statusCode < 700))
		{
			if (statusCode == iRBTConstant.SM_SBN_MODE_UNKNOWN) 
			{
				requestMap.put(RESPONSE, "ERROR");
				writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
			}
			else if (statusCode == iRBTConstant.SM_PARENT_UNDER_BILLING)
			{
				if (smRequestType.startsWith("SEL") || smRequestType.startsWith("DOWNLOAD"))
				{
					// Retry case
				}
				else
				{
					requestMap.put(RESPONSE, "ERROR");
					writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
				}
			}
			else if (statusCode == iRBTConstant.SM_SBN_DEACT_PENDING)  
			{
				// Retry case. 
			}
			if (!requestMap.containsKey(RESPONSE)) // RETRY CASES
				requestMap.put(RESPONSE, FAILED);
			
		}
		else if(statusCode >= 700 && statusCode < 800)
		{
			if (statusCode == iRBTConstant.SM_SUB_NOT_FOUND
					|| statusCode == iRBTConstant.SM_SUBN_NOT_FOUND
					|| statusCode == iRBTConstant.SM_SBN_NOT_ACTIVE) 
			{
				if (requestType.equalsIgnoreCase("DCT"))
				{
					requestMap.put(RESPONSE, "ALREADY_DEACTIVE"); 
				}
				else
				{
					writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
				}
			}
			else if(statusCode == iRBTConstant.SM_SUBSCRIPTION_ALREADY_EXISTS) 
			{
				if (requestType.equalsIgnoreCase("ACT") && ((smRequestType.indexOf("BASE") != -1) || smRequestType.indexOf("COMBINED") != -1) )
				{
					// Response string : ERROR|SUBSCRIPTION ALREADY EXISTS|SRV_KEY|SUB_STATUS|REFID
					if (response.indexOf("ERROR|SUBSCRIPTION ALREADY EXISTS|") != -1)
					{
						response = response.trim();
						String[] tokens = response.split("\\|");

						String rbtRefID = requestMap.get(REFID);
						String srvKey = null;
						String smRefID = null;
						String subscriberStatus = null;

						if(tokens.length >= 3)
							srvKey = tokens[2];
						if(tokens.length >= 4)
							subscriberStatus = tokens[3].trim();
						if(tokens.length >= 5)
							smRefID = tokens[4].trim();
						if (smRefID != null && rbtRefID != null && rbtRefID.equalsIgnoreCase(smRefID))
						{
							writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response+":"+rbtRefID);
						}
						else
						{	
							if (subscriberStatus != null
									&& (subscriberStatus.equalsIgnoreCase("A") || subscriberStatus.equalsIgnoreCase("J"))
									&& srvKey != null && srvKey.length() > 8
									&& m_rbtSubClassCacheManager.getSubscriptionClass(srvKey.substring(8))!= null) // Base Activation request case only
							{
								if(RBTDeploymentFinder.isRRBTSystem())
									requestMap.put(RESPONSE, "ALREADY_ACTIVE|" + srvKey.substring(9));
								else
									requestMap.put(RESPONSE, "ALREADY_ACTIVE|" + srvKey.substring(8));
							}
							else if (subscriberStatus != null && subscriberStatus.trim().equalsIgnoreCase("H")	&& srvKey != null && srvKey.length() > 8)
							{
								requestMap.put(RESPONSE, "SUSPENDED|" + srvKey.substring(8));
							}
						}
					}
				}
				else if (requestType.equalsIgnoreCase("ACT")
						&& (smRequestType.indexOf("SEL") != -1 || smRequestType.indexOf("DOWNLOAD") != -1))
				{
					requestMap.put(RESPONSE, "ALREADY_ACTIVE");
				}
				else if (requestType.equalsIgnoreCase("DCT"))
				{
					writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
				}
			}
			else if (statusCode == iRBTConstant.SM_DUPLICAT_REFID) 
			{
				requestMap.put(RESPONSE, "SUCCESS");
			}
			else if(statusCode == iRBTConstant.SM_PARENT_NOT_ACTIVE) 
			{
				if (smRequestType.startsWith("SEL") || smRequestType.startsWith("DOWNLOAD"))
				{
					requestMap.put(RESPONSE, "BASE_DEACTIVE");
				}
				else
				{
					writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
				}
			}
			else if(statusCode == iRBTConstant.SM_SUB_BLACKLISTED)  
			{
				if (requestType.equalsIgnoreCase("DCT"))
				{
					// As confirmed by SM, For blacklisting subscriber deactivation is allowed.
					requestMap.put(RESPONSE, "ALREADY_DEACTIVE");
				}
				else
				{
					requestMap.put(RESPONSE, "BLACKLISTED");
				}
			}
			else if (statusCode == iRBTConstant.SM_UPGRADATION_NOT_FOUND)
			{
				requestMap.put(RESPONSE, "UPGRADE_FAILURE");
			}
			else if (statusCode == iRBTConstant.SM_SUBSCRIPTION_IN_HOLD) 
			{
				 
			}
			else if(statusCode == iRBTConstant.SM_PARENT_UNDER_SUSPENSION)  
			{
				if (smRequestType.startsWith("SEL") || smRequestType.startsWith("DOWNLOAD"))
				{
					
				}
			}
			else if (statusCode == iRBTConstant.SM_SBN_UNDER_DEACTIVATION)
			{
				if (requestType.equalsIgnoreCase("DCT"))
				{
					requestMap.put(RESPONSE, "SUCCESS"); 
				}
				else
				{
					writeErrorCasesTrans(smRequestType, requestMap.get(URL), statusCode, response);
				}
			}
			if (!requestMap.containsKey(RESPONSE)) 
				requestMap.put(RESPONSE, "ERROR");
		}
		else if(statusCode != 200)
		{
			requestMap.put(RESPONSE, SM_URL_FAILURE);
		}
		else
		{
			requestMap.put(RESPONSE, response);
		}
		return requestMap;
	}

	private HashMap<String, String> getSMURL(Subscriber subscriber, SubscriberStatus subscriberStatus,
			SubscriberDownloads subDownload, ProvisioningRequests provisioningRequests, String requestType,
			boolean sendSubTypeUnknown, HashMap<String, String> loggingInfoMap) {
		HashMap<String, String> requestMap = null;
		try {
			RBTSMDaemonService rbtDaemonService = (RBTSMDaemonService) ConfigUtil.getBean(BeanConstant.GET_SM_URL_IMPL);
			if (rbtDaemonService != null) {
				requestMap = rbtDaemonService.getSMURL(subscriber, subscriberStatus, subDownload, provisioningRequests,
						requestType, sendSubTypeUnknown, loggingInfoMap);
			}
		} catch (Exception e) {
			logger.error("Bean is not configured: " + e, e);
			requestMap = new RBTSMDaemonManagerImpl().getSMURL(subscriber, subscriberStatus, subDownload,
					provisioningRequests, requestType, sendSubTypeUnknown, loggingInfoMap);
		}
		
		return requestMap;
	}
	// RBT-14301: Uninor MNP changes.
	public String appendSiteId(String circleId, String url)
			throws MappedSiteIdNotFoundException {
		String siteId = getParamAsString(COMMON, "CONFIGURED_DEFAULT_SITE_ID",
				"0");
		String tempSiteId = null;
		if (!url.contains("%siteid%")) {
			return url;
		}
		logger.info("appendSiteId:circleId " + circleId);
		logger.info("appendSiteId:circleIdToSiteIdMapping " + circleIdToSiteIdMapping);
		if (circleIdToSiteIdMapping != null
				&& !circleIdToSiteIdMapping.isEmpty()) {
			if (circleId != null) {
				tempSiteId = circleIdToSiteIdMapping.get(circleId);
			}
			logger.info("appendSiteId: tempSiteId " + tempSiteId);
			if (tempSiteId != null && !tempSiteId.equalsIgnoreCase(siteId)) {
				siteId = tempSiteId;
			} else if ((tempSiteId == null || tempSiteId.trim().isEmpty())) {
				throw new MappedSiteIdNotFoundException(
						"For "
								+ circleId
								+ " siteId info not found in CIRCLEID_TO_SITEID_MAPPING for subscriber.");
			}
			url = url.replaceAll("%siteid%", siteId);
		} else {
			url = url.replaceAll("%siteid%", siteId);
		}
		logger.info("appendSiteId: siteId " + siteId);
		return url;
	}

	private String getUserInfo(Clip clipObj, Category categoryObj, Subscriber subscribers, SubscriberStatus selection,
			SubscriberDownloads download) throws WDSInfoNotFoundException {
		String user_info = "";
		boolean isShuffleCategory = Utility.isShuffleCategory(categoryObj.getCategoryTpe()) && (categoryObj.getCategoryTpe() != iRBTConstant.AUTO_DOWNLOAD_SHUFFLE);
		if(isShuffleCategory && getParamAsBoolean(COMMON, "SENDING_CATEGORY_INFO_FOR_SHUFFLE", "FALSE")){

			String grammar = categoryObj.getCategoryGrammar(); 
			if (grammar != null && grammar.equalsIgnoreCase("UGC")) {
				user_info += ("|songtype:UGC");
			}
			String promoId = categoryObj.getCategoryPromoId();
			String name = convertWindow1252(getCategoryName(categoryObj));
			String movieName = convertWindow1252(categoryObj.getCategoryName());
			
			String categoryInfo = convertWindow1252(getCategoryInfo(categoryObj));
            if(promoId==null){
            	promoId = getParamAsString(COMMON, "CONFIGURED_CATEGORY_PROMOID_FOR_SHUFFLE", null);
            }
            promoId = convertWindow1252(promoId);
			user_info += name + "songcode:" + promoId ;
			if(categoryInfo != null && !categoryInfo.equalsIgnoreCase("null"))
				user_info = user_info + "|" + categoryInfo;
			user_info = user_info + "|moviename:" + movieName;

    	}else if (clipObj != null){
			String grammar = clipObj.getClipGrammar(); 
			if (grammar != null && grammar.equalsIgnoreCase("UGC")) {
				user_info += ("|songtype:UGC");
			}
			String promoId = clipObj.getClipPromoId();
			String artist = convertWindow1252(getArtistName(clipObj));
			String name = convertWindow1252(getSongName(clipObj));
			String movieName = convertWindow1252(getMovieName(clipObj));
			
			//Added for Tef-Spain
			// Auto_download check done so as to consider that shuffl's song as normal song.
			boolean isShuffle = Utility.isShuffleCategory(categoryObj.getCategoryTpe()) && (categoryObj.getCategoryTpe() != iRBTConstant.AUTO_DOWNLOAD_SHUFFLE);
			String clipInfo = null;
			
			boolean isShuffleCategoryTypeConfiguredForSendingClipInfo = false;			//Config added for RBT-12877
			if (shuffleCategoryTypesForSendingClipInfoList == null
					|| shuffleCategoryTypesForSendingClipInfoList.isEmpty() 
					|| shuffleCategoryTypesForSendingClipInfoList.contains(String.valueOf(categoryObj.getCategoryTpe()))) {
				isShuffleCategoryTypeConfiguredForSendingClipInfo = true;
			}
			logger.debug("isShuffleCategoryTypesConfiguredForSendingClipInfo: " + isShuffleCategoryTypeConfiguredForSendingClipInfo);
			if(!isShuffle || (isShuffle && getParamAsBoolean("SEND_CLIP_INFO_FOR_SHUFFLE", "TRUE") && isShuffleCategoryTypeConfiguredForSendingClipInfo)) {
				clipInfo = convertWindow1252(getClipInfo(clipObj));
			}
			
			if (categoryObj != null && isShuffle)
			{
				promoId = categoryObj.getCategoryPromoId();
			}
			promoId = convertWindow1252(promoId);
			user_info += name + "songcode:" + promoId ;
			
			String selOrDownloadExtraInfo = null;
			String chargeClass = null;
			if(selection != null) {
				selOrDownloadExtraInfo = selection.extraInfo();
				chargeClass = selection.classType();
			}
			else if(download != null) {
				selOrDownloadExtraInfo = download.extraInfo();
				chargeClass = download.classType();
			}
			
			//param=:CP:TF-Spain-Onmobile|ISRC:|UPC:|AUTHOR:|CPC:32349|CPCF:32347|CC:35446|CCF:35444|SPI:4200805|RBY:TME
			//This parameter will replace the values configured in the parameters table while sending it to SM.
			String packSelectionReplacableInfo = getParamAsString("PACK_SELECTION_CLIP_INFO");
			if(chargeClass != null && getParamAsString("PACK_SELECTION_CLIP_INFO_" + chargeClass) != null)
				packSelectionReplacableInfo = getParamAsString("PACK_SELECTION_CLIP_INFO_" + chargeClass);
			
			if (clipInfo != null && !clipInfo.equalsIgnoreCase("null") && packSelectionReplacableInfo != null
					&& selOrDownloadExtraInfo != null && selOrDownloadExtraInfo.contains("PACK")) {
				Map<String, String> clipInfoMap = parseClipInfo(clipInfo);
				Map<String, String> toBeReplacedClipInfoMap = parseClipInfo(packSelectionReplacableInfo);

				for (Entry<String, String> entry : toBeReplacedClipInfoMap.entrySet()) {
					clipInfoMap.put(entry.getKey(), entry.getValue());
				}
				clipInfo = parseClipInfoMap(clipInfoMap);
			}
			
			if(clipInfo != null && !clipInfo.equalsIgnoreCase("null"))
				user_info = user_info + "|" + clipInfo;
			user_info = user_info + "|moviename:" + movieName + "|artist:"
					+ artist;
			if (clipObj.getContentType() != null)
				user_info += "|contentType:"
						+ convertWindow1252(clipObj.getContentType());
		} else if (categoryObj.getCategoryTpe() == iRBTConstant.RECORD) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "RMO_CLIP_INFO", null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "RMO_CLIP_PROMOID", null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "RMO_CLIP_MOVIENAME", null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "RMO_CLIP_ARTISTNAME", null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam,
					promoIdParam, movieNameParam, artistNameParam);
		} else if (categoryObj.getCategoryTpe() == iRBTConstant.KARAOKE) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_INFO", null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_PROMOID", null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_MOVIENAME", null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "MERIDHUN_OR_KARAOKE_CLIP_ARTISTNAME", null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam,
					promoIdParam, movieNameParam, artistNameParam);
		} else if (categoryObj.getCategoryId() == 99) {
			Parameters clipInfoParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "PROFILE_RMO_CLIP_INFO", null);
			Parameters promoIdParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "PROFILE_RMO_CLIP_PROMOID", null);
			Parameters movieNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "PROFILE_RMO_CLIP_MOVIENAME", null);
			Parameters artistNameParam = m_rbtParamCacheManager.getParameter(
					"DAEMON", "PROFILE_RMO_CLIP_ARTISTNAME", null);
			user_info += getUserInfoForKarokeAndRMO(clipInfoParam,
					promoIdParam, movieNameParam, artistNameParam);
		} else {
			user_info += ("songname:|songcode:|moviename:|artist:");
		}

		if (getParamAsBoolean("IS_TATA_GSM_IMPL", "FALSE")){
			String cosID = subscribers != null ? subscribers.cosID() : "";
			String wdsInfo = "";
			HashMap<String,String> extraInfoMap=rbtDBManager.getExtraInfoMap(subscribers);
			if (extraInfoMap!=null&&extraInfoMap.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)){
				wdsInfo=extraInfoMap.get(EXTRA_INFO_WDS_QUERY_RESULT);
			} else {
				throw new WDSInfoNotFoundException("WDS info not found for subscriber " + subscribers.subID());
			}
	
			user_info += ("|cosid:"+ cosID + "|" + EXTRA_INFO_WDS_QUERY_RESULT + ":"+wdsInfo);
		}
		
		if(download != null) {
			if(download.selectionInfo() != null && download.selectionInfo().toLowerCase().indexOf("refund:true") != -1) {
				user_info += "|REFUND:true"; 
			}
		}
		
		return user_info;
	}
	
	private String parseClipInfoMap(Map<String, String> clipInfoMap) {
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> entry : clipInfoMap.entrySet()) {
			sb.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		return sb.substring(0, sb.length() - 1);
	}

	private Map<String, String> parseClipInfo(String clipInfo) {
		Map<String, String> clipInfoMap = new HashMap<String, String>();
		StringTokenizer stk = new StringTokenizer(clipInfo, "|");
		while(stk.hasMoreTokens()) {
			String token = stk.nextToken();
			String[] split = token.split(":", -1);
			clipInfoMap.put(split[0], split[1]);
		}
		return clipInfoMap;
	}

	private String getUserInfoForKarokeAndRMO(Parameters clipInfoParam,
			Parameters promoIdParam, Parameters movieNameParam,
			Parameters artistNameParam) {
		String clipInfo = "";
		if(clipInfoParam != null)
		{
			clipInfo = clipInfoParam.getValue();
			clipInfo = (clipInfo != null) ? clipInfo.replaceAll("=", ":") : "";
			if(clipInfo.length() > 0) {
				clipInfo += "|";
			}
		}
		String clipPromoId = "";
		if(promoIdParam != null)
		{
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
		return ("songname:|songcode:" + convertWindow1252(clipPromoId) + "|"
				+ convertWindow1252(clipInfo) + "moviename:" +convertWindow1252(clipMovieId)+ "|artist:"+ convertWindow1252(clipArtistId));
	}
	/*
	 * Used to get the url for combined charging. 
	 * */
	private HashMap<String, Object> getComboAddParams(String subscriberID, Subscriber subscriber)
	{
		logger.info("Getting combo request parameters. subscriberId: "
				+ subscriberID);
		HashMap<String, Object> response = new HashMap<String, Object>();
		String url = null;
		HashMap<String, String> subscriberExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		boolean canSendComboRequest = false;
		if (getParamAsBoolean("COMMON","ADD_TO_DOWNLOADS","FALSE"))
		{
			SubscriberDownloads[] downloads = rbtDBManager.smGetBaseActivationPendingDownloads(subscriberID);
			if (downloads != null && downloads.length > 0)
			{
				canSendComboRequest = true;
				response.put("SUBSCRIBER_DOWNLOAD", downloads[0]);
			}
		}
//		else if(subscriberExtraInfoMap != null && subscriberExtraInfoMap.containsKey("PACK"))
//		{
//			String cosIds = subscriberExtraInfoMap.get("PACK");
//			String[] cosId = cosIds.split(",");
//			if(cosId != null && cosId.length>0){
//				CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId[0]);
//				if (cos != null && (SONG_PACK.equals(cos.getCosType()) || LIMITED_DOWNLOADS.equals(cos.getCosType())|| UNLIMITED_DOWNLOADS.equals(cos.getCosType())))
//				{
//					List<ProvisioningRequests> pr = ProvisioningRequestsDao.getBySubscriberIDAndTypeOrderByCreationTime(subscriberID, Integer.parseInt(cos.getCosId()));
//					if(pr !=null && pr.size()>0)
//					{
//						canSendComboRequest = true;
//						response.put("SUBSCRIBER_PACK", pr.get(0));
//					}
//					logger.info("canSendComboRequest set as "+ canSendComboRequest + " and subscriber pack is set ");
//				}
//			}
//		}
		else
		{
			boolean doQueryDesc = getParamAsBoolean(COMMON, "QUERY_SELECTION_IN_DESC_FOR_COMBO_ACT", "TRUE");
			SubscriberStatus[] selections = rbtDBManager.getUnProcessedNormalSelections(subscriberID, "1,79,90,99", doQueryDesc);
			if (selections != null && selections.length > 0)
			{
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(selections[0].extraInfo());
				if (extraInfoMap == null || !extraInfoMap.containsKey("REACT_REFID"))
                {
					String subscriberExtraInfo = subscriber.extraInfo();
					HashMap<String, String> subExtraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriberExtraInfo);
					String subSdpomtxnid = subExtraInfoMap != null ? subExtraInfoMap
							.get("sdpomtxnid") : null;
					String selSdpomtxnid = extraInfoMap != null ? extraInfoMap.get("sdpomtxnid")
							: null;
					if ((subSdpomtxnid == null && selSdpomtxnid == null)
							|| (subSdpomtxnid != null && selSdpomtxnid != null && selSdpomtxnid
									.equalsIgnoreCase(subSdpomtxnid))) {
						canSendComboRequest = true;
						response.put("SUBSCRIBER_SELECTION", selections[0]);
					}
				}
			}
		}
		if(canSendComboRequest)
		{
			response.put("LINKED_SELECTION_EXISTS", "TRUE");
			String actInfo = "";
			if(subscriber.activationInfo() != null)
			{
				actInfo = subscriber.activationInfo().replaceAll("\\|", "/");
				actInfo = actInfo.replaceAll(":", ";");
			}
			String linkedRefID = subscriber.refID();
			//Added for TTG-14814
			response.put("LINKED_REF_ID", linkedRefID);
			//End of TTG-14814
			String linkedMode = subscriber.activatedBy();
			if(linkedMode != null) linkedMode = linkedMode.toUpperCase(); 
			String linkedSrvKey = "RBT_ACT_"+subscriber.subscriptionClass();
			if (RBTDeploymentFinder.isRRBTSystem()){
				linkedSrvKey = linkedSrvKey+"_RRBT";
			}else if(RBTDeploymentFinder.isPRECALLSystem()){
				linkedSrvKey = linkedSrvKey+"_PRECALL";
			}else if(RBTDeploymentFinder.isBGMSystem()){
				linkedSrvKey = linkedSrvKey+"_BGM";
			}
			
			String content_id = "actinfo=" + actInfo + ",cosid:"+subscriber.cosID() + "|cosid:"+subscriber.cosID();
			url = "&linkedsrvkey="+linkedSrvKey+"&linkedrefid="+linkedRefID+"&linkedmode="+linkedMode+"&linkedinfo="+("|CONTENT_ID:"+content_id);
			HashMap<String, String> subInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			
			String linkedtrxid = getTransID(subscriber.activationInfo());
			
			//Added the following into the info column to update the sr_id and originator info 
			// vendor as per the jira id RBT-11962
			if (subInfoMap != null) {
				// CG Integration Flow - Jira -12806
				boolean checkCGFlowForBSNL = RBTParametersUtils
						.getParamAsBoolean(iRBTConstant.DOUBLE_CONFIRMATION,
								"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
				boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
						.getParamAsBoolean(COMMON,
								"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
				if (checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast) {
					if (subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						url += "|cgId:"
								+ subInfoMap
										.get(iRBTConstant.EXTRA_INFO_TPCGID);
					}
//					if (subInfoMap != null
//							&& subInfoMap
//									.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
//						url += "|transId:"
//								+ subInfoMap
//										.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
//					}
					if(subInfoMap != null
							&& subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						linkedtrxid = subInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
					}

				} else {
					if (subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
						url += "|CGID:"
								+ subInfoMap
										.get(iRBTConstant.EXTRA_INFO_TPCGID);
					}
					if (subInfoMap != null
							&& subInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						url += "|TRANSID:"
								+ subInfoMap
										.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
					}
				}
				if (subInfoMap.containsKey(Constants.param_SR_ID)) {
					url += "|sr_id:" + subInfoMap.get(Constants.param_SR_ID);
				}
				if (subInfoMap.containsKey(Constants.param_ORIGINATOR)) {
					url += "|originator:"
							+ subInfoMap.get(Constants.param_ORIGINATOR);
				}
				if (subInfoMap.containsKey(Constants.param_vendor.toUpperCase())) {
					url += "|vendor:" + subInfoMap.get(Constants.param_vendor.toUpperCase());
				}
			}
			if(subInfoMap != null && subInfoMap.containsKey(EXTRA_INFO_OFFER_ID))
				url += "&linkedofferid=" + subInfoMap.get(EXTRA_INFO_OFFER_ID);
			
			String giftTransID = null;
			if(subscriber.activatedBy() != null && subscriber.activatedBy().equalsIgnoreCase("GIFT"))
			{
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
				if(extraInfoMap != null && extraInfoMap.containsKey(GIFT_TRANSACTION_ID))
				{	
					giftTransID = extraInfoMap.get(GIFT_TRANSACTION_ID);
					url = url+"&linked_gift_trans_id="+giftTransID;
				}
			}

			
			if (linkedtrxid != null)
				url = url + "&linkedtrxid=" + linkedtrxid;
			
			if(subInfoMap != null && subInfoMap.containsKey(Constants.param_baseprice)) {
				url += "&baseprice=" + subInfoMap.get(Constants.param_baseprice);
			}
			
			String baseMappedStr = getParamAsString(COMMON, "BASE_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
			if (baseMappedStr != null && subInfoMap!=null) {
				String str[] = baseMappedStr.split(";");
				for (int i = 0; i < str.length; i++) {
					String s[] = str[i].split(",");
					if (s.length == 2 && subInfoMap.containsKey(s[1]))
						url += "&" + s[1] + "=" + subInfoMap.get(s[1]);
				}
			}

			
			response.put(URL, url);
		}
		logger.info("Returning combo request parameters. response: " + response
				+ ", subscriberId: " + subscriberID);
		return response;
	}

	private HashMap<String, String> getComboAddParamsForSongPack(String subscriberID, Subscriber subscriber, CosDetails cosDetail)
	{
		logger.info("Getting combo request parameters. subscriberId: "
				+ subscriberID);
		HashMap<String, String> responseParam = new HashMap<String, String>();
		String url= null;
		String smRequestType = "SEL_ACT_COMBINED";
		String srvKey = "RBT_PACK_"+cosDetail.getSmsKeyword().toUpperCase();
		String actInfo = "";
		String type = "P";
		if(subscriber.activationInfo() != null)
		{
			actInfo = subscriber.activationInfo().replaceAll("\\|", "/");
			actInfo = actInfo.replaceAll(":", ";");
		}
		if (subscriber.prepaidYes()) {
			type = "P";
		}
		else {
			type = "B";
		}
	
		if(subscriber.extraInfo() !=null && subscriber.extraInfo().contains(HYBRID_SUBSCRIBER_TYPE)){
			type = "H";
		}
		
		/**
		 * For RRBT flow, add suffix _RRBT to srvkey and linked srvKey
		 */
		String linkedRefID = subscriber.refID();
		//Added for TTG-14814
		responseParam.put("LINKED_REF_ID", linkedRefID);
		//End of TTG-14814
		String linkedMode = subscriber.activatedBy();
		if(linkedMode != null) linkedMode = linkedMode.toUpperCase();
		String linkedSrvKey = "RBT_ACT_"+subscriber.subscriptionClass();
		if (RBTDeploymentFinder.isRRBTSystem())
		{
			srvKey = srvKey +"_RRBT";
			linkedSrvKey = linkedSrvKey+"_RRBT";
		}else if(RBTDeploymentFinder.isPRECALLSystem()){
			srvKey = srvKey +"_PRECALL";
			linkedSrvKey = linkedSrvKey+"_PRECALL";
		}else if(RBTDeploymentFinder.isBGMSystem()){
			srvKey = srvKey +"_BGM";
			linkedSrvKey = linkedSrvKey+"_BGM";
		}
	
		HashMap<String, String> subInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
		String packCosId = null; 
		if(subInfoMap != null && subInfoMap.containsKey(PACK))
			packCosId = subInfoMap.get(PACK).split(",")[0];
		String content_id = "actinfo=" + actInfo + ",cosid:"+subscriber.cosID()+",packid:"+packCosId + "|cosid:"+subscriber.cosID();
		url = "&linkedsrvkey="+linkedSrvKey +"&linkedrefid="+linkedRefID+"&linkedmode="+linkedMode+"&linkedinfo="+("|CONTENT_ID:"+content_id);

		String linkedtrxid = getTransID(subscriber.activationInfo());

		// CG Integration Flow - Jira -12806
		boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
						iRBTConstant.DOUBLE_CONFIRMATION,
						"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
		boolean isCGIntegrationFlowForBsnlEast = RBTParametersUtils
				.getParamAsBoolean(COMMON,
						"CG_INTEGRATION_FLOW_FOR_BSNL_EAST", "FALSE");
		if ((checkCGFlowForBSNL || isCGIntegrationFlowForBsnlEast )&& subInfoMap != null
				&& subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
			linkedtrxid = subInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
		}
		
		if (linkedtrxid != null)
			url = url + "&linkedtrxid=" + linkedtrxid;

		
		if(subInfoMap != null && subInfoMap.containsKey(EXTRA_INFO_TOBE_ACT_OFFER_ID))
			url += "&linkedofferid=" + subInfoMap.get(EXTRA_INFO_TOBE_ACT_OFFER_ID);
		List<ProvisioningRequests> pr = ProvisioningRequestsDao.getBySubscriberIDAndTypeOrderByCreationTime(subscriber.subID(), Integer.parseInt(cosDetail.getCosId()));
		String refID = pr.get(0).getTransId();
		String provReqMode = pr.get(0).getMode();
		String comUrl = getParamAsString("ACTIVATION_URL");
		
		String info = "songname:|songcode:|null|moviename:|artist:|cli:all|CONTENT_ID:contentid=MISSING,catname=MISSING,actinfo=null,callerid=ALL,catid=MISSING|catname:MISSING";
		// CG Integration Flow - Jira -12806
		if (checkCGFlowForBSNL) {
			if (subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				url += "|cgId:"
						+ subInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
			}// CG Integration Flow - Jira -12806
//			if (subInfoMap != null
//					&& subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
//				url += "|transId:"
//						+ subInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
//			}

		} else {
			if (subInfoMap != null
					&& subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				info += "|CGID:"
						+ subInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID);
			}
			if (subInfoMap != null
					&& subInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
				info += "|TRANSID:"
						+ subInfoMap.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
			}
		}
		// Changes for PACK- Jira 12810
		String cosID = pr.get(0).getType()+"";
		List<String> azaanCosIdList = getAzaanCosIdList();
		if (azaanCosIdList.contains(cosID)) {
			String paramName = PACK_REQ_INFO_COSID + cosID;
			String packCosID = getParamAsString(DAEMON, paramName, null);
			if (null != packCosID) {
				info = packCosID;
			}
		}
		
		String baseMappedStr = getParamAsString(COMMON, "BASE_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
		if (baseMappedStr != null && subInfoMap!=null) {
			String str[] = baseMappedStr.split(";");
			for (int i = 0; i < str.length; i++) {
				String s[] = str[i].split(",");
				if (s.length == 2 && subInfoMap.containsKey(s[1]))
					url += "&" + s[1] + "=" + subInfoMap.get(s[1]);
			}
		}
		//SDP Direct : rbt-9213
		String sdpInfoParams = getSdpInfoParam(subscriber,null);
		if(sdpInfoParams!=null) {
			info += "|"+sdpInfoParams;
		}
		
		comUrl += "msisdn="+subscriberID+"&srvkey="+srvKey+"&refid="+refID+"&info="+info+"&mode="+provReqMode+"&type="+type;

		comUrl +=url;
		// RBT-14301: Uninor MNP changes.
		try {
			url = appendSiteId(subscriber.circleID(), comUrl);
		} catch (MappedSiteIdNotFoundException e) {
			logger.warn("", e);
			return null;
		}

		responseParam.put(URL, comUrl);
		responseParam.put(REQUEST_TYPE, smRequestType);

		logger.info("Returning combo request parameters. responseParam: " + responseParam
				+ ", subscriberId: " + subscriberID);
		return responseParam;
	}

	private String parseResponse(String response) {
		String resp = null;
		logger.info(response);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(response));
			Document document = builder.parse(is);
			NodeList nodeList = document.getElementsByTagName("TD");
			Node node = nodeList.item(1);
			String temp = node.getChildNodes().item(0).getNodeValue();
			int stat = 500;
			try {
				stat = Integer.parseInt(temp.trim());
				if(stat == 500)
					stat = 200;
				else if(stat == -1)
					stat = 700;
				else
					stat = 601;
			}
			catch (Exception e) {

			}
			node = nodeList.item(3);
			temp = node.getChildNodes().item(0).getNodeValue();
			if(temp.equalsIgnoreCase("record updated"))
				temp = "SUCCESS";
			resp = stat + "|" + temp;

		}
		catch (Exception e) {
			System.out.println(e);
		}

		return resp;
	}

	public static void main(String[] args)
	{
		String method = "main()";
		System.out.println("Entering " + method);
		try
		{
			new ServerSocket(15000);
			m_rbtDaemonManager = new RBTDaemonManager();
			logger.info("m_rbtDaemonManager 1 is " + m_rbtDaemonManager);
			if (m_rbtDaemonManager.getConfigValues())
			{
				System.out.println("RBT daemon started...");
				logger.info("m_rbtDaemonManager 2 is " + m_rbtDaemonManager);
				m_rbtDaemonManager.start();
				System.out.println("RBT daemon stopped...");

				if (RelianceDaemonBootstrap.canStartDeamon())
					RelianceDaemonBootstrap.start();
			}
			else
			{
				System.out.println("Error in config parameters. Exiting...");
			}
		}
		catch (Throwable t)
		{
			System.out.println("Exception in main(): " + t.getMessage());
			t.printStackTrace();
		}
	}


	private String getCatNameID(Category category) {
		String catName = "";
		if(category != null) {
			catName = category.getCategoryName();
		}
		if(catName.length() > 20) 
			catName = catName.substring(0,20); 
		catName = convertWindow1252(catName);
		return catName;
	}

	private String getClipNameRBTWav(String strWavFile) 
	{
		String songName = "";
		Clip clip = getClipRBT(strWavFile);
		if(clip != null) {
			songName = clip.getClipName();
		}
		int clipLengthLimit = RBTParametersUtils.getParamAsInt("DAEMON", "SM_URL_CLIP_NAME_LENGTH_LIMIT", 20); 
		if (songName.length() > clipLengthLimit) { 
			songName = songName.substring(0, clipLengthLimit);
		}
		return songName;
	}

	private String getClipIDRBTWav(String strWavFile, Category categoryObj) 
	{
		String clipID = "MISSING";
		Clip clip = getClipRBT(strWavFile);
		if(clip != null) 
		{
			clipID = "" + clip.getClipId();
		}
		
		if(categoryObj != null) {
			String categoryType =  Utility.getCategoryType(categoryObj.getCategoryTpe());
			Parameters clipIdParam = null;
			if(categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_KARAOKE)) {
				clipIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "KARAOKE_CLIP_ID", null);			
			}
			else if(categoryType.equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
				clipIdParam = m_rbtParamCacheManager.getParameter("DAEMON", "RMO_CLIP_ID", null);
			}
			if(clipIdParam != null) {
				clipID = clipIdParam.getValue();
			}
		}
		
		return clipID;
	}
	
	protected String getSelectionSMS(String sms, SubscriberStatus sel) {
		String songName = null;
		String start = null;
		String end = null;
		String caller = m_smsTextForAll;
		if (sel.callerID() != null)
			caller = sel.callerID();
		if (sel.categoryID() == 104)
			songName = "Record My Own";
		else if (sel.categoryType() == SHUFFLE || sel.categoryType() == 9
				|| Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES", "10").split(","))
				     .contains(sel.categoryType() + "")) {
			try {
				Category category = getCategory(sel.categoryID());
				songName = getCatNameID(category);
				if ( Arrays.asList(getParamAsString("COMMON", "OVERRIDE_SHUFFLE_CATEGORY_TYPES", "10").split(","))
						.contains(sel.categoryType() + "")) {
					start = m_timeSdf.format(getStartTime(sel.categoryID()));
					end = m_timeSdf.format(getEndTime(sel.categoryID()));
				}
			} catch (Throwable e) {

			}
		} else if (sel.subscriberFile() != null)
			songName = getClipNameRBTWav(sel.subscriberFile());

		if (sel.status()==99){
			start=null;
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
			Date endTime=sel.endTime();
			end = formatter.format(endTime);
		}

		if (songName != null && caller != null) {
			sms = sms.replaceAll("%S", songName);
			sms = sms.replaceAll("%C", caller);
		}
		if (start != null && end != null) {
			sms = getParamAsString("DAEMON", "OVERRIDE_SHUFFLE_SMS", m_overrideShuffleSMS);
			sms = sms.replaceAll("%S", songName);
			sms = sms.replaceAll("%C", caller);
			sms = sms.replaceAll("%L", start);
			sms = sms.replaceAll("%T", end);
		}
		if (start==null && end!=null){
			sms = sms.replaceAll("%S", songName);
			sms = sms.replaceAll("%C", caller);
			sms = sms.replaceAll("%T", end);
		}
		return sms;
	}

	protected boolean smURLSelectionActivation(String subscriberID, String callerID, int status,
			Date setDate, int fromTime, int toTime, boolean success, boolean error, String setTime, char oldLoopStatus, boolean isPrepaid, Date startTime, int rbtType, String wavFile, boolean updateRefID, String refID, String interval, List<String> songWavFilesList) throws Exception{
		char newLoopStatus = rbtDBManager.getLoopStatusToUpateSelection(oldLoopStatus,
				subscriberID, isPrepaid);
		//logger.info("startTime "+ startTime);
		 return smURLSelectionActivation(subscriberID, callerID, status, setDate, fromTime, toTime, success, error, setTime, oldLoopStatus, isPrepaid, startTime, rbtType,  wavFile, updateRefID, refID, interval , null, songWavFilesList);
	}
	
	protected boolean smURLSelectionActivation(String subscriberID, String callerID, int status,
			Date setDate, int fromTime, int toTime, boolean success, boolean error, String setTime, char oldLoopStatus, boolean isPrepaid, Date startTime, int rbtType, String wavFile, boolean updateRefID, String refID, String interval , String extraInfo, List<String> songWavFilesList) throws Exception{
		char newLoopStatus = rbtDBManager.getLoopStatusToUpateSelection(oldLoopStatus,
				subscriberID, isPrepaid);
		//logger.info("startTime "+ startTime);
		return rbtDBManager.smURLSelectionActivation(subscriberID, callerID, status, setDate,
				fromTime, toTime, success,  error, setTime, newLoopStatus, startTime, rbtType, wavFile, updateRefID, refID, interval,extraInfo, songWavFilesList);
	}

	protected boolean smURLSelectionDeactivation(String subscriberID,String refID, boolean success,boolean error, int rbtType){
		return rbtDBManager.smURLSelectionDeactivation(subscriberID, refID, success, error, rbtType);
	}

	// Added extraInfo - TRAI changes
	private String smSubscriptionSuccess(String strSubID, String subClass, String actInfo) {
		return rbtDBManager.smSubscriptionSuccess(strSubID, null, null, null, subClass, true, "", getParamAsBoolean("NOT_PLAY_SONG_INACT_USER", "FALSE"), null);
	}

	private String smSubscriptionSuspend(String strSubID, String subClass, String actInfo) {
		return rbtDBManager.smSubscriptionSuspend(strSubID, subClass);
	}

	protected Subscriber getSubscriber(String subscriberID) {
		return rbtDBManager.getSubscriber(subscriberID);
	}

	private Clip getClipRBT(String rbt_wav) 
	{
		if(rbt_wav!=null && rbt_wav.indexOf("rbt_slice_")!=-1){
			String str[] = rbt_wav.split("rbt_slice_");
			String clipId = str[1].substring(0, str[1].indexOf("_"));
			return rbtCacheManager.getClip(clipId);
		}
		return rbtCacheManager.getClipByRbtWavFileName(rbt_wav, "ALL");
	}

	private Category getCategory(int catID) 
	{
		return rbtCacheManager.getCategory(catID);
	}

	public static Subscriber[] smGetActivatedSubscribers(ArrayList<String> modes, boolean getLow) 
	{
		return rbtDBManager.smGetActivatedSubscribers(getParamAsInt("FETCH_SIZE", 5000), modes, getLow);
	}

	public static Subscriber[] smGetDeactivatedSubscribers() {
		return rbtDBManager.smGetDeactivatedSubscribers(getParamAsInt("FETCH_SIZE", 5000));
	}

	public static SubscriberStatus[] smGetActivatedSelections(ArrayList<String> lowModes, boolean bGetLowPriority) {
		return rbtDBManager.smGetActivatedSelections(getParamAsInt("FETCH_SIZE", 5000), lowModes, bGetLowPriority);
	}

	public static SubscriberStatus[] smGetDirectActivatedSelections() {
		return rbtDBManager.smGetDirectActivatedSelections(getParamAsInt("FETCH_SIZE", 5000));
	}

	public static SubscriberStatus[] smGetRenewalSelections() {
		return rbtDBManager.smGetRenewalSelections(getParamAsInt("FETCH_SIZE", 5000));
	}

	public static SubscriberDownloads[] smGetDownloadsToBeActivated() {
		return rbtDBManager.smGetDownloadsToBeActivated(getParamAsInt("FETCH_SIZE", 5000));
	}

	public static SubscriberDownloads[] smGetDeactivatedDownloads() {
		return rbtDBManager.smGetDownloadsToBeDeactivated(getParamAsInt("FETCH_SIZE", 5000));
	}

	private void smUpdateSelStatus(String strSubID, String initStatus, String finalStatus) {
		rbtDBManager.smUpdateSelStatus(strSubID, initStatus, finalStatus);
	}

	public static SubscriberStatus[] smGetDeactivatedSelections() {
		return rbtDBManager.smGetDeactivatedSelections(getParamAsInt("FETCH_SIZE", 5000));
	}
	
	public static ProvisioningRequests[] smGetActivatedPacks() {
		return rbtDBManager.smGetActivatedPacks(getParamAsInt("FETCH_SIZE", 5000));
	}
	
	public static ProvisioningRequests[] smGetDeactivatedPacks() {
		return rbtDBManager.smGetDeactivatedPacks(getParamAsInt("FETCH_SIZE", 5000));
	}

	private boolean smURLSubscription(String subscriberID, boolean isSuccess, boolean isError, String prevDelayDeactSubYes) {
		return rbtDBManager.smURLSubscription(subscriberID, isSuccess, isError, prevDelayDeactSubYes);
	}
	
	private boolean smUpdatePackStatusOnBaseAct(String subscriberID) {
		return rbtDBManager.smUpdatePackStatusOnBaseAct(subscriberID);
	}
	
	private boolean smURLPackActivation(String subscriberID) {
		return rbtDBManager.smURLPackActivation(subscriberID);
	}

	private boolean smURLUnSubscription(String subscriberID, boolean success, boolean isError) {
		return rbtDBManager.smURLUnSubscription(subscriberID, success, isError);
	}
	
	private boolean smUpdatePackStatusOnBaseDeact(String subscriberID) {
		return rbtDBManager.smUpdatePackStatusOnBaseDeact(subscriberID);
	}
	
	private boolean smURLPackDeactivation(String subscriberID) {
		return rbtDBManager.smURLPackDeactivation(subscriberID);
	}

	private void smUpdateSelStatusSubscriptionSuccess(String strSubID) {
		rbtDBManager.smUpdateSelStatusSubscriptionSuccess(strSubID, getParamAsBoolean("REAL_TIME_SELECTIONS","FALSE"));
	}

	private Subscriber[] getSubsTobeDeactivated() {
		return rbtDBManager.getSubsTobeDeactivated(getParamAsInt("FETCH_SIZE", 5000));
	}

	protected static String getStackTrace(Throwable ex) {
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if(ex instanceof Exception) {
			Exception exception = (Exception)ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}

	protected Date getStartTime(int catID) 
	{ 
		Category category = rbtCacheManager.getCategory(catID);
		if(category != null) 
			return category.getCategoryStartTime(); 
		return null; 
	}  

	private Date getEndTime(int catID) 
	{ 
		Category category = rbtCacheManager.getCategory(catID);
		if(category != null) 
			return category.getCategoryEndTime();

		return null; 
	} 

	/*
	 * Gets transaction id from the activation info.(passed as parameter) as the string following trxid
	 * */

	private String getTransID(String info)
	{
		if(info == null || info.trim().length() == 0 )
			return null;
		if(info.lastIndexOf(":trxid:") > -1)
		{
			int index1 = info.lastIndexOf(":trxid:");
			int index2 = info.indexOf(":",index1+7);
			if(index2 > index1+7)
			{
				String returnVal =  info.substring(index1+7,index2);
				if(returnVal != null && returnVal.trim().length() > 0 && !returnVal.trim().equalsIgnoreCase("null"))
					return returnVal.trim();
			}	
		}
		return null;
	}

	private boolean sendSelectionSMSCrossPromo(String sms, SubscriberStatus ss) {


		sms = getSelectionSMS(sms, ss);

		if (ss != null) {

			String subscriberID = ss.subID();

			int songStatus = ss.status();

			int categoryType = ss.categoryType();

			String clipID = getClipIDRBTWav(ss.subscriberFile(),null);

			String result = "SUCCESS";

			String selectedBy = ss.selectedBy();

			if (getParamAsBoolean("MAKE_HTTP_HIT_BEFORE_SEND_SMS", "FALSE")) {

				logger.info("song status "
						+ songStatus + " category type " + categoryType);

				if (!(songStatus == 90 || songStatus == 99 || songStatus == 0
						|| categoryType == 0 || categoryType == 4
						|| categoryType == 10 || categoryType == 12
						|| categoryType == 20)) {
					Date requestTimeStamp = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyyMMddHHmmss");
					String requestTimeString = formatter
					.format(requestTimeStamp);

					String strURL = getParamAsString("GATHERER","CROSS_PROMO_SMS_URL", null);
					logger.info("strURL :  "
							+ strURL);
					if (strURL != null) {
						strURL = strURL.replaceAll("<msisdn>", subscriberID);
						strURL = strURL.replaceAll("<rbtid>", clipID);
						strURL = strURL.replaceAll("<status>", result);
						strURL = strURL.replaceAll("<channel>", selectedBy);
						strURL = strURL.replaceAll("<msg>", sms);
						strURL = strURL.replaceAll("<sendernumber>", getSenderNumber(ss.circleId(), getParamAsString("SENDER_NO")));
						strURL = strURL.replaceAll(" ", "%20");
					}
					logger.info("strURL after replacing:  " + strURL);
					boolean responseStatus =Tools.callURL(strURL, statusCode,
							response, getParamAsBoolean("GATHERER", "CROSS_PROMO_SMS_USEPROXY", "FALSE"), getParamAsString("GATHERER","CROSS_PROMO_SMS_PROXYHOST",null),
							getParamAsInt("GATHERER", "CROSS_PROMO_SMS_PROXYPORT", 0),false, getParamAsInt("GATHERER", "CROSS_PROMO_SMS_TIMEOUT", 5000));

					Date responseTimeStamp = new Date();
					long responseTimeInMillis = responseTimeStamp.getTime()
					- requestTimeStamp.getTime();

					if (responseStatus) {

						WriteSDR.addToAccounting(getParamAsString("GATHERER","CROSS_PROMO_SMS_LOGPATH",null),
								getParamAsInt("GATHERER", "CROSS_PROMO_SMS_ROTATIONSIZE", 24),
								"RBT_CROSS_PROMO_SMS_SENDER", subscriberID,
								null, "CROSS PROMO RT sms send", "SUCCESS",
								requestTimeString, "" + responseTimeInMillis,
								null, strURL, response.toString());

						return true;

					}
				}
			}
		}

		return false;
	}

	public static boolean writeTrans(String type, String url, int code, String resp, long delay)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("REQUEST TYPE", type);
		h.put("REQUEST URL", url);
		h.put("REPONSE CODE", ""+code);
		h.put("RESPONSE STRING", resp);
		h.put("RESPONSE DELAY", ""+delay);

		if(m_writeTrans != null)
		{
			m_writeTrans.writeTrans(h);
			return true;
		}

		return false;
	}
	
	public static boolean writeErrorCasesTrans(String type, String url, int code, String resp)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("REQUEST TYPE", type);
		h.put("REQUEST URL", url);
		h.put("REPONSE CODE", ""+code);
		h.put("RESPONSE STRING", resp);

		if(smErrorCasesTrans != null)
		{
			smErrorCasesTrans.writeTrans(h);
			return true;
		}
		return false;
	}

	protected String getParamAsString(String param)
	{
		try{
			return m_rbtParamCacheManager.getParameter("DAEMON", param, null).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return null;
		}
	}

	protected String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}

	public static int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}

	protected static boolean getParamAsBoolean(String param, String defaultVal) {
		try {
			boolean value = m_rbtParamCacheManager.getParameter("DAEMON", param,
					defaultVal).getValue().equalsIgnoreCase("TRUE");
			logger.debug("Configured param: " + param + ", value: " + value);
			return value;
		} catch (Exception e) {
			logger.info("Unable to get param: " + param
					+", returning default value: "+defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private boolean getParamAsBoolean(String type, String param, String defaultVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	
	
	private String getSongName(Clip clip)
	{
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
				songNameStr += "songname_"+language+":"+songName+"|";
			}
		}
		songNameStr = songNameStr.replaceAll("'", "");
		songNameStr = songNameStr.replaceAll("&", "%26");
		return songNameStr;		
	}
	
	private String getCategoryName(Category category)
	{
		String catNameStr = "songname:"+category.getCategoryName()+"|";
		
		for (String language : supportedLangList)
		{
			if (category.getCategoryName(language) != null)
				catNameStr += "songname_"+language+":"+category.getCategoryName(language)+"|";
		}
		catNameStr = catNameStr.replaceAll("'", "");
		catNameStr = catNameStr.replaceAll("&", "%26");
		return catNameStr;		
	}


	private String getMovieName(Clip clip) {
		String movieName = clip.getAlbum();
		if(movieName != null && movieName.length() > 20)
			movieName = movieName.substring(0, 20);
		movieName = (movieName != null ? movieName.replaceAll("&", "%26") : movieName);
		return movieName;
	}

	private String getClipInfo(Clip clip) {
		String clipInfo = clip.getClipInfo();
		if (clipInfo != null) {
			clipInfo = clipInfo.replaceAll("=", ":");
			clipInfo = clipInfo.replaceAll("&", " ");
		}
		return clipInfo;
	}

	private String getCategoryInfo(Category category) {
		String categoryInfo = category.getCategoryInfo();
		if (categoryInfo != null) {
			categoryInfo = categoryInfo.replaceAll("=", ":");
			categoryInfo = categoryInfo.replaceAll("&", " ");
		}
		return categoryInfo;
	}

	private String getArtistName(Clip clip) {
		String artist = clip.getArtist();
		artist = (artist != null ? artist.replaceAll("&", "%26") : artist);
		return artist;
	}
	
	public static class WDSInfoNotFoundException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public WDSInfoNotFoundException() {
			
		}
		
		public WDSInfoNotFoundException(String s) {
			super(s);
		}

		public WDSInfoNotFoundException(String s, Throwable t) {
			super(s, t);
		}
	}
	
	// RBT-14301: Uninor MNP changes.
	public static class MappedSiteIdNotFoundException extends Exception {
		private static final long serialVersionUID = 2L;

		public MappedSiteIdNotFoundException() {

		}

		public MappedSiteIdNotFoundException(String s) {
			super(s);
		}

		public MappedSiteIdNotFoundException(String s, Throwable t) {
			super(s, t);
		}
	}
	
	private void processUpgradeTransaction(Subscriber subscriber)
    {
		boolean isActPendingUsersAllowed = CacheManagerUtil
				.getParametersCacheManager()
				.getParameterValue(
						iRBTConstant.COMMON,
						"ALLOW_ACT_PENDING_USERS_FOR_UPGRADATION",
						"FALSE").equalsIgnoreCase("TRUE");
    	if (!isActPendingUsersAllowed)
    		return;

    	String subscriberID = subscriber.subID();
    	List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao.getBySubscriberIDAndType(subscriberID, Type.BASE_UPGRADATION.getTypeCode());
    	ProvisioningRequests provisioningRequest = null;
    	if (provisioningRequestsList != null && provisioningRequestsList.size() > 0)
    		provisioningRequest = provisioningRequestsList.get(0);

		if (provisioningRequest != null)
		{
			HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(provisioningRequest.getExtraInfo());
			if (extraInfoMap == null)
				extraInfoMap = new HashMap<String, String>();

			int oldRbtType = subscriber.rbtType();
			int newRbtType = subscriber.rbtType();
			if (extraInfoMap.containsKey(ExtraInfoKey.RBT_TYPE.toString()))
			{
				newRbtType = Integer.parseInt(extraInfoMap.get(ExtraInfoKey.RBT_TYPE.toString()));
				extraInfoMap.remove(ExtraInfoKey.RBT_TYPE.toString());
			}

			if (oldRbtType != newRbtType) // If AdRbt upgradation
			{
				extraInfoMap.put((newRbtType == 1 ? iRBTConstant.EXTRA_INFO_ADRBT_ACTIVATION
								: iRBTConstant.EXTRA_INFO_ADRBT_DEACTIVATION),
								"TRUE");
			}
			
			String extraInfo = subscriber.extraInfo();
			HashMap<String, String> subExtraInfo = DBUtility.getAttributeMapFromXML(extraInfo);
			if (subExtraInfo == null)
				subExtraInfo = new HashMap<String, String>();

			if (!extraInfoMap.containsKey("SCRN"))
			{
				subExtraInfo.remove("SCRS");
				subExtraInfo.remove("SCRN");
			}
			
			subExtraInfo.putAll(extraInfoMap);
			extraInfo = DBUtility.getAttributeXMLFromMap(subExtraInfo);
			
			String activationInfo = provisioningRequest.getModeInfo();
			String newActivationInfo = activationInfo;
			boolean concatActivationInfo = true;
			String subscriberActInfo = subscriber.activationInfo();
			if (subscriberActInfo.contains("scratchcard"))
			{
				newActivationInfo = subscriberActInfo;
				int noOfScratchCardUsed = 0;
				int index = 0;
				while (true)
				{
					index = subscriberActInfo.indexOf("scratchcard", index);
					if (index < 0)
						break;

					index++;
					noOfScratchCardUsed++;
					if (noOfScratchCardUsed > 2)
						newActivationInfo = newActivationInfo.replaceFirst("scratchcard:[0-9]*\\|refid:[0-9]*\\|", "");
				}

				newActivationInfo += "|" + activationInfo;

				concatActivationInfo = false;
			}

			boolean isConversionSuccess = rbtDBManager.convertSubscriptionType(subscriberID, subscriber.subscriptionClass(), 
					provisioningRequest.getChargingClass(),
					provisioningRequest.getMode(), newActivationInfo,
					concatActivationInfo, newRbtType, true, extraInfo, subscriber);

			String status = isConversionSuccess ? "SUCCESS" : "FAILURE";
			ProvisioningRequestsDao.removeByRequestId(subscriberID, provisioningRequest.getRequestId());
			
			StringBuilder logBuilder = new StringBuilder();
    		logBuilder.append(provisioningRequest).append(", ").append(status);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.UPGRADETRANSACTION, logBuilder.toString());
			if (!isConversionSuccess)
				processUpgradeTransaction(subscriber);
		}
    }
    
    private void removeAllUpgradeTransactions(Subscriber subscriber, String response)
    {
    	String subscriberID = subscriber.subID();
    	List<ProvisioningRequests> provisioningRequestsList = ProvisioningRequestsDao.getBySubscriberIDAndType(subscriberID, Type.BASE_UPGRADATION.getTypeCode());
    	for (ProvisioningRequests provisioningRequest : provisioningRequestsList)
    	{
    		StringBuilder logBuilder = new StringBuilder();
    		logBuilder.append(provisioningRequest).append(", ").append(response);
    		RBTEventLogger.logEvent(RBTEventLogger.Event.UPGRADETRANSACTION, logBuilder.toString());
    	}
    	ProvisioningRequestsDao.removeBySubscriberIDAndType(subscriberID, Type.BASE_UPGRADATION.getTypeCode());
    }

	private boolean isSameCaller(String firstCallerId, String secondCallerId)
	{
		if (firstCallerId == null && secondCallerId == null)
			return true;
		else if (firstCallerId != null && firstCallerId.equals(secondCallerId))
			return true;
		return false;
	}
	
	    //RBT-14070
		private boolean checkForPattern(String value) {
			String regex = RBTParametersUtils.getParamAsString("COMMON",
					"SPECIAL_CHAR_PATTERN", null);
			if (regex == null || regex.trim().isEmpty()) {
				return false;
			}
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(value);
			return matcher.find();
		}
	
		private String convertWindow1252(String value) {
			if (value == null || value.trim().isEmpty()) {
				return null;
			}
			String retValue = value;
			try {
			if (checkForPattern(value)) {
				logger.debug("Value before replacing Special Characters "+value);
				value = Normalizer.normalize(value, Normalizer.Form.NFD);
				Pattern pattern = Pattern
						.compile("\\p{InCombiningDiacriticalMarks}+");
				value = pattern.matcher(value).replaceAll("");
				logger.debug("Value after replacing Special Characters "+value);
			}
			if (getParamAsBoolean("COMMON",
					"SUPPORT_ENCODE_WIN1252_CONTENTFIELD", "FALSE")) {
				retValue = new String(value.getBytes(),
						Charset.forName("Windows-1252"));
			}
			return value;
			} catch (Exception e) {
				logger.error("Exception while encoding Windows-1252", e);
			}
			return retValue;
		}
	
	//RBT-9213
	private String getSdpInfoParam(Subscriber subscriber,SubscriberStatus subscriberStatus)
	{
		//changed for bug
		if(subscriber==null && subscriberStatus == null)
			return null;
		boolean isSdpParamsToBeSent = getParamAsBoolean("SEND_SDP_PARAMS", "FALSE");
		if(!isSdpParamsToBeSent) {
			return null;
		}
		
		HashMap<String,String> selExtraInfoMap= null;
		if(subscriberStatus!=null){
			selExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());
		}
		
		HashMap<String,String> extraInfoMap = null;
	    if(subscriber!=null){
		    extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
	    }
	    
		if(extraInfoMap==null && selExtraInfoMap == null)
			return null;
		String sdpInfo = null;
		String info="";
		
		if (selExtraInfoMap != null && selExtraInfoMap.containsKey("seapitype")) {
			info += "seapitype:" + selExtraInfoMap.get("seapitype") + "|";
		} else if (selExtraInfoMap == null && extraInfoMap != null
				&& extraInfoMap.containsKey("seapitype")) {
			info += "seapitype:" + extraInfoMap.get("seapitype") + "|";
		}

		if (selExtraInfoMap != null && selExtraInfoMap.containsKey("sdpomtxnid")) {
			info += "sdpomtxnid:" + selExtraInfoMap.get("sdpomtxnid") + "|";
		} else if (selExtraInfoMap == null && extraInfoMap != null
				&& extraInfoMap.containsKey("sdpomtxnid")) {
			info += "sdpomtxnid:" + extraInfoMap.get("sdpomtxnid") + "|";
		}
		
		if(!info.equalsIgnoreCase("")){
		    info += "statuscode:200"; 
		    sdpInfo = info;
		}
        logger.info("SDP Info = "+sdpInfo);
		return sdpInfo;
	}

	// Changes for PACK Jira - 12810
	private List<String> getAzaanCosIdList() {
		List<String> azaanCosIdList = new ArrayList<String>();
		String cosIds = getParamAsString(DAEMON, AZAAN_COS_ID_LIST, null);
		azaanCosIdList = ListUtils.convertToList(cosIds, ",");
		return azaanCosIdList;
	}

	// RBT-14177 - Profile deactivation msg to be sent on expiry
	public static String[] getValidityUnitForProfileDCT(
			SubscriberStatus subscriberStatus) {
		// HH converts hour in 24 hours format (0-23), day calculation
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss.SSS");
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
			logger.info(period.getYears() + " years," + period.getMonths()
					+ " months," + period.getWeeks() + " weeks,"
					+ period.getDays() + " days," + period.getHours()
					+ " hours," + period.getMinutes() + " minutes,"
					+ period.getSeconds() + " seconds," + period.getMillis()
					+ "milliseconds");
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
}