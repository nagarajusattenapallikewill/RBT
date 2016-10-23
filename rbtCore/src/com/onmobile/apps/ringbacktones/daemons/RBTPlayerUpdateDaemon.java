package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.net.URL;
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
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberStatusImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.helper.AbstractIntegrationHelper;
import com.onmobile.apps.ringbacktones.rbt2.helper.impl.TPIntegrationHelperImpl;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.reporting.framework.capture.api.Configuration;

/**
 * 
 * @author vsreekar 03/12/07 This class updates the player DB with the new
 *         selections and subscriptions & deactivations of selections and
 *         subscriptions
 */
public class RBTPlayerUpdateDaemon extends Thread implements iRBTConstant {

	public static Stopwatch playerSelActStopwatch = SimonManager
			.getStopwatch("playerSelAct");

	private static final String CHILD_MDN = "CHILD_MDN";
	private static String _class = "RBTPlayerUpdateDaemon";
	private static Logger logger = Logger
			.getLogger(RBTPlayerUpdateDaemon.class);

	protected static final int ACTION_TYPE_ADD_SEL = 1;
	protected static final int ACTION_TYPE_REMOVE_SEL = 2;
	private static final int ACTION_TYPE_DEACT_USER = 3;
	private static final int ACTION_TYPE_UPDATE_SUB = 4;

	private static final String ACTION_TYPE_ADD_SEL_STR = "ACTION_ADD_SEL";
	private static final String ACTION_TYPE_REMOVE_SEL_STR = "ACTION_DEL_SEL";
	private static final String ACTION_TYPE_DEACT_USER_STR = "ACTION_DEACT_USER";
	private static final String ACTION_TYPE_UPDATE_SUB_STR = "ACTION_UPDATE_SUB";
	private static final String ACTION_TYPE_UNKNOWN = "ACTION_UNKNOWN";

	private static final int UPDATE_TYPE_NONE = 0;
	@SuppressWarnings("unused")
	private static final int UPDATE_TYPE_OVERRIDE = 1;
	@SuppressWarnings("unused")
	private static final int UPDATE_TYPE_LOOP = 2;
	protected static final int UPDATE_TYPE_BOTH = 3;

	private static final String PARAM_SUB_ID = "SUB_ID";
	private static final String PARAM_ACTION = "ACTION";
	private static final String PARAM_TYPE = "TYPE";
	private static final String PARAM_XML = "XML";
	private static final String PARAM_ACTION_UPDATE = "UPDATE";
	private static final String PARAM_ACTION_DEACTIVATE = "DEL";

	private RBTDaemonManager m_mainDaemonThread;
	public static RBTDBManager dbManager = null;
	static RBTCacheManager rbtCacheManager = null;
	static ParametersCacheManager m_rbtParamCacheManager = null;
	AltruistUgcClipDownloader altruistUgcClipDownloader = null;

	private static String playerUpdaterPage = "rbtplayer/rbt_memcache_invalidation.jsp?";

	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static final String RESPONSE_SUCCESS = "SUCCESS";

	private static final String logDateFormatterString = "yyyyMMddHHmmss";
	private static final SimpleDateFormat logDateFormatter = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static final String DOW_KEY_1 = "W1";
	private static final String DOW_KEY_2 = "W2";
	private static final String DOW_KEY_3 = "W3";
	private static final String DOW_KEY_4 = "W4";
	private static final String DOW_KEY_5 = "W5";
	private static final String DOW_KEY_6 = "W6";
	private static final String DOW_KEY_7 = "W7";

	private static final String DOW_VALUE_1 = "Sun";
	private static final String DOW_VALUE_2 = "Mon";
	private static final String DOW_VALUE_3 = "Tue";
	private static final String DOW_VALUE_4 = "Wed";
	private static final String DOW_VALUE_5 = "Thu";
	private static final String DOW_VALUE_6 = "Fri";
	private static final String DOW_VALUE_7 = "Sat";

	private static HashMap<String, String> dowMap = null;
	private static ArrayList<String> dowList = null;

	static List<String> m_modesAllowedToUploadFiles = null;
	static RBTHttpClient rbtHttpClient = null;
	static public WriteDailyTrans m_writeTrans = null;
	public static ADRBTEventLogger adrbtEventLogger = null;
	public static Clip defaultClip = null;

	protected static Hashtable<String, String> m_processingHash = new Hashtable<String, String>();
	private static Map<String, String> oldNnewPrefixMap = new HashMap<String, String>();

	private static List<String> vrbtSubscriptionClassList = null;
	private static List<String> azaanCosIdList = new ArrayList<String>();
	private static List<String> confAzaanCategoryIdList = new ArrayList<String>();
	private static Map<String,String> confAzaanCopticDoaaCosIdSubTpeMap = null;
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public static List<String> cosTypesForMultiPack = null;
	public static boolean sendAppName = false;
	
	protected RBTPlayerUpdateDaemon(RBTDaemonManager mainDaemonThread) {
		if (initParams())
			m_mainDaemonThread = mainDaemonThread;
		setName("RBTPlayerUpdateDaemon");
	}

	private boolean initParams() {
		try {
			m_rbtParamCacheManager = CacheManagerUtil
					.getParametersCacheManager();
			dbManager = RBTDBManager.getInstance();

			rbtCacheManager = RBTCacheManager.getInstance();
			SMDaemonPerformanceMonitor.startSMPerformanceMonitorDaemon();

			if (!initDaemonParams()) {
				logger.info("RBT::problem initialising DAEMON params");
				return false;
			}
			this.setName(_class);
		} catch (Exception e) {
			logger.error("Issue in creating RBTPlayerUpdateDaemon", e);
			return false;
		}
		return true;
	}

	private boolean initDaemonParams() {
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("EVENT_TYPE");
		headers.add("SUBSCRIBERID");
		headers.add("REQUESTED_TIMESTAMP");
		headers.add("RESPONSE_DELAYINMS");
		headers.add("REQUEST_DETAIL");
		headers.add("RESPONSE_DETAIL");
		headers.add("IP_ADDRESS");
		m_writeTrans = new WriteDailyTrans(getParamAsString("DAEMON",
				"PLAYER_SDR_WORKING_DIR", null), "PLAYER_REQUEST", headers);

		// Changed to RBTHttpClient API
		HttpPerformanceMonitor httpPerformanceMonitor = PerformanceMonitorFactory
				.newHttpPerformanceMonitor("PlayerDaemon",
						"PlayerDaemonMonitor", PerformanceDataType.LONG,
						"Milliseconds");
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(40);
		httpParameters.setMaxHostConnections(40);
		httpParameters
				.setConnectionTimeout(getParamAsInt("SMDAEMON_TIMEOUT", 6) * 1000);
		httpParameters
				.setSoTimeout(getParamAsInt("SMDAEMON_TIMEOUT", 6) * 1000);
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);
		rbtHttpClient = new RBTHttpClient(httpParameters);

		dowMap = new HashMap<String, String>();
		dowMap.put(DOW_KEY_1, DOW_VALUE_1);
		dowMap.put(DOW_KEY_2, DOW_VALUE_2);
		dowMap.put(DOW_KEY_3, DOW_VALUE_3);
		dowMap.put(DOW_KEY_4, DOW_VALUE_4);
		dowMap.put(DOW_KEY_5, DOW_VALUE_5);
		dowMap.put(DOW_KEY_6, DOW_VALUE_6);
		dowMap.put(DOW_KEY_7, DOW_VALUE_7);

		if (dowList == null) {
			dowList = new ArrayList<String>();
		}
		dowList.add(DOW_VALUE_1);
		dowList.add(DOW_VALUE_2);
		dowList.add(DOW_VALUE_3);
		dowList.add(DOW_VALUE_4);
		dowList.add(DOW_VALUE_5);
		dowList.add(DOW_VALUE_6);
		dowList.add(DOW_VALUE_7);

		initializeADRBTEventLogger();
		initPlayerDefaultClip();

		String param = getParamAsString(DAEMON, "SUB_ID_OLD_N_NEW_PREFIXES",
				null);
		if (param != null && param.length() > 0) {
			String[] stringParts = param.split(",");
			for (String s : stringParts) {
				int hyphenPos = s.indexOf("-");
				String oldPrefix = s.substring(0, hyphenPos);
				String newPrefix = s.substring(hyphenPos + 1);
				oldNnewPrefixMap.put(oldPrefix, newPrefix);
			}
		}
		String modesAllowed = getParamAsString("COMMON",
				"MODES_ALLOWED_TO_UPLOAD_FILES", null);
		if (modesAllowed != null)
			m_modesAllowedToUploadFiles = Arrays
					.asList(modesAllowed.split(","));

		vrbtSubscriptionClassList = new ArrayList<String>();
		String vrbtSubscriptionClasses = getParamAsString("COMMON",
				"VRBT_SUBSCRIPTION_CLASSES", null);
		if (vrbtSubscriptionClasses != null
				&& (vrbtSubscriptionClasses = vrbtSubscriptionClasses.trim())
						.length() != 0) {
			String[] vrbtSubscriptionClass = vrbtSubscriptionClasses.split(",");
			for (String temp : vrbtSubscriptionClass) {
				vrbtSubscriptionClassList.add(temp);
			}
		}

		// List of Cos Ids which are used for Azaan feature.
		// Azaan is the call for prayer in Islam which happens 5 times a day.
		String cosIds = getParamAsString(DAEMON, AZAAN_COS_ID_LIST, null);
		azaanCosIdList = ListUtils.convertToList(cosIds, ",");
		
		// List of Category Ids which are used for Azaan feature.
		// Azaan is the call for prayer in Islam which happens 5 times a day.
		String catIds = getParamAsString(DAEMON, AZAAN_CATEGORY_ID_LIST, "");
		confAzaanCategoryIdList = ListUtils.convertToList(catIds, ",");

		String azaanCopticDoaaCosIds = getParamAsString(COMMON, COSID_SUBTYPE_MAPPING_FOR_AZAAN, "");
		confAzaanCopticDoaaCosIdSubTpeMap = MapUtils.convertIntoMap(azaanCopticDoaaCosIds, ";",":",","); 
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		String cosTypesForEnableMultiPack = RBTParametersUtils
				.getParamAsString("COMMON",
						"COS_TYPE_FOR_ALLOWING_MULTIPLE_PACK", null);
		cosTypesForMultiPack = ListUtils.convertToList(
				cosTypesForEnableMultiPack, ",");
		sendAppName = getParamAsBoolean(DAEMON, SEND_APP_NAME_TO_TP, "FALSE");
		return true;
	}

	private void initPlayerDefaultClip() {
		int defClipId = getParamAsInt("PLAYER_DEFAULT_CLIP_ID", -1);
		if (defClipId != -1)
			defaultClip = rbtCacheManager.getClip(defClipId);
	}

	private static String getShuffleClips(int categoryID,int categoryType) {
		Clip[] clips = rbtCacheManager.getActiveClipsInCategory(categoryID);
		if (clips == null || clips.length == 0) {
			clips = rbtCacheManager.getClipsInCategory(categoryID);
		}
		if (clips == null || clips.length == 0) {
			return null;
		}
		boolean putSgsInUgs = getParamAsBoolean("COMMON", "PUT_SGS_IN_UGS",
				"FALSE");
		String defaultNameTune = null;
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < clips.length; j++) {
			if (clips[j].getClipInfo() != null
                    && clips[j].getClipInfoMap() != null
					&& clips[j].getClipInfoMap().containsKey("default_festival_nametune")
					&& categoryType == FESTIVAL_NAMETUNES_SHUFFLE) {
				defaultNameTune = clips[j].getClipRbtWavFile();
				break;
			}
			sb.append(clips[j].getClipRbtWavFile());
			if (j != (clips.length - 1)) {
				if (putSgsInUgs) {
					sb.append("|" + categoryID);
				}
				sb.append(",");
			}
		}		
		if(defaultNameTune!=null){
			sb = new StringBuilder();
			sb.append(defaultNameTune);
		}
		
		sb.append("|" + categoryID);
		
		if(categoryType == FESTIVAL_NAMETUNES_SHUFFLE && defaultNameTune == null) {
			return null;
		}
		return sb.toString();
	}

	@Override
	public void run() {
		logger.info("started player thread");
		try {
			createPlayerThreadPools();

			if (getParamAsBoolean("START_UGC_CLIP_DOWNLOAD_THREAD", "FALSE"))
				new AltruistUgcClipDownloader(m_mainDaemonThread).start();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void createPlayerThreadPools() {
		logger.info("creating player thread pools");

		List<SitePrefix> prefixes = CacheManagerUtil
				.getSitePrefixCacheManager().getLocalSitePrefixes();

		if (prefixes == null || prefixes.size() <= 0) {
			logger.info(" No Prefixes got for Site so exiting .....");
			return;
		}
		logger.info(prefixes.size() + " circles found for player pools");

		for (int i = 0; i < prefixes.size(); i++) {
			PlayerRequestProducer baseActRequestProducer = new PlayerRequestProducer(
					RequestType.PLAYER_BASE_ACT, getParamAsInt(
							"PLAYER_THREAD_POOL_SIZE_BASE_ADD", 1), prefixes
							.get(i).getCircleID());
			baseActRequestProducer.start();

			PlayerRequestProducer baseDeactRequestProducer = new PlayerRequestProducer(
					RequestType.PLAYER_BASE_DCT, getParamAsInt(
							"PLAYER_THREAD_POOL_SIZE_BASE_REMOVE", 1), prefixes
							.get(i).getCircleID());
			baseDeactRequestProducer.start();

			PlayerRequestProducer baseSusRequestProducer = new PlayerRequestProducer(
					RequestType.PLAYER_BASE_SUS, getParamAsInt(
							"PLAYER_THREAD_POOL_SIZE_BASE_SUSPEND", 1),
					prefixes.get(i).getCircleID());
			baseSusRequestProducer.start();

			PlayerRequestProducer selActRequestProducer = new PlayerRequestProducer(
					RequestType.PLAYER_SEL_ACT, getParamAsInt(
							"PLAYER_THREAD_POOL_SIZE_SEL_ADD", 1), prefixes
							.get(i).getCircleID());
			selActRequestProducer.start();

			PlayerRequestProducer selDeactRequestProducer = new PlayerRequestProducer(
					RequestType.PLAYER_SEL_DCT, getParamAsInt(
							"PLAYER_THREAD_POOL_SIZE_SEL_REMOVE", 1), prefixes
							.get(i).getCircleID());
			selDeactRequestProducer.start();

			if (getParamAsBoolean("DEACTIVATE_CHANGE_MSISDN_SUBS", "FALSE")) {
				PlayerRequestProducer changeMsisdnRequestProducer = new PlayerRequestProducer(
						RequestType.PLAYER_CHANGE_MSISDN, 1, prefixes.get(i)
								.getCircleID());
				changeMsisdnRequestProducer.start();
			}
		}
		logger.info("Done creating playerthread pools");
	}

	public static boolean addSelectionsToplayer(Subscriber subscriber) {
		try {
			return sendUserXMLToPlayer(subscriber, ACTION_TYPE_ADD_SEL,
					UPDATE_TYPE_BOTH);
		} catch (Exception e) {
			logger.error("", e);
		}
		return false;
	}

	public static boolean removeSelectionsFromplayer(Subscriber subscriber) {
		try {
			return sendUserXMLToPlayer(subscriber, ACTION_TYPE_REMOVE_SEL,
					UPDATE_TYPE_BOTH);
		} catch (Exception e) {
			logger.error("", e);
		}
		return false;
	}

	public static boolean deactivateUsersInPlayerDB(Subscriber subscriber) {
		try {
			return sendUserXMLToPlayer(subscriber, ACTION_TYPE_DEACT_USER,
					UPDATE_TYPE_BOTH);
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	public static boolean updateSubscribersInPlayer(
			Subscriber subscribersToUpdate, boolean suspend) {
		try {
			int update = (suspend ? UPDATE_TYPE_BOTH : UPDATE_TYPE_NONE);
			return sendUserXMLToPlayer(subscribersToUpdate,
					ACTION_TYPE_UPDATE_SUB, update);
		} catch (Throwable e) {
			logger.error(e);
		}
		return false;
	}

	/*
	 * Used to send the necessary details to the player. The parameters passed
	 * are : SUB_ID , XML and ACTION Once response comes as true, then the
	 * required updates are run on the rbt db.
	 * 
	 * The player is informed when : Adding selections, deactivating selections,
	 * deactivating subscriber, suspending subscriber.
	 */

	protected static boolean sendUserXMLToPlayer(Subscriber subscriber,
			int action, int updateType) {
		boolean response = true;
		if (subscriber == null) {
			logger.info("Got Null subscriber object. Returning false");
			return false;
		}
		
		String subID = subscriber.subID();

		logger.info(" inside with " + subscriber + " action " + action);
		try {
			
			//RBT-15826 changes
			String circleID = subscriber.circleID();
			AbstractIntegrationHelper integrationHelper = null;
			try{
				 integrationHelper = (AbstractIntegrationHelper) ConfigUtil.getBean(BeanConstant.INTEGRATION_HELPER_BEAN);
			}catch(NoSuchBeanDefinitionException e){
				logger.info("Exception occured while initializing bean.");
				if(integrationHelper == null){
					integrationHelper = new TPIntegrationHelperImpl();
				}
			}
			
			logger.info("Integration helper classname: "+integrationHelper);
			
			circleID = integrationHelper.getCircleId(subscriber);// changed with operator name
			
			SitePrefix userPrefix = CacheManagerUtil
					.getSitePrefixCacheManager().getSitePrefixes(
							circleID);
			if (userPrefix == null) {
				logger.info("RBT::user prefix null for user " + subID
						+ ". Deactivating the user and his selections");
				dbManager.deactivateSubscriber(subID, "SMPLYER", null, true,
						true, true);
				return false;
			}

			HashMap<Character, ArrayList<String>> statusMap = new HashMap<Character, ArrayList<String>>();
			String playerURLs = userPrefix.getPlayerUrl();
			StringTokenizer stk = new StringTokenizer(playerURLs, ",");

			// this while loop will update all players web-service

			SubscriberStatus latestSubStatus = null;
			if (m_modesAllowedToUploadFiles != null) {
				latestSubStatus = RBTDBManager.getInstance()
						.getSubscriberLatestActiveSelection(subID);
			}
			while (stk.hasMoreTokens()) {

				// HttpParameters httpParams =
				// Tools.getHttpParamsForURL(stk.nextToken(),
				// getParamAsString("COMMON", "PLAYER_UPDATE_PAGE",
				// playerUpdaterPage));

				List<String> httpParamList = Arrays.asList(stk.nextToken()
						.split("\\|"));

				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(httpParamList.get(0)
						+ ""
						+ getParamAsString("COMMON", "PLAYER_UPDATE_PAGE",
								playerUpdaterPage));

				try {
					String xml = null;
					if (action == ACTION_TYPE_ADD_SEL
							|| action == ACTION_TYPE_REMOVE_SEL
							|| action == ACTION_TYPE_UPDATE_SUB) {
						xml = createSubscriberXML(subscriber, action,
								statusMap, userPrefix);
						if (xml == null)
							return false;
					}					
					String type = "";
					if (RBTDeploymentFinder.isRRBTSystem()) {
						type = USER_TYPE_RRBT ;
					} else if (RBTDeploymentFinder.isPRECALLSystem()) {
						type = USER_TYPE_PRE_CALL;
					}
					
					HashMap<String, String>  requestParams = integrationHelper.getRequestParam(replacePrefixInSubId(subID),action,xml,type,subscriber.circleID());
					
					String responseStr = null;
					long requestedTimeStamp = System.currentTimeMillis();
					try {
						logger.debug(" Making http call for subscriber: "
								+ subID + ", requestParams: " + requestParams
								+ ", httpParameters: " + httpParameters);
						responseStr = makeHttpRequest(httpParameters,
								requestParams);
						logger.debug("Response of Http" + " request: "
								+ responseStr);
						
						//Added for RBT 2.0
						responseStr = integrationHelper.handleResponse(responseStr);
						
						
						/*
						 * RBT-4586: To support MultiSIM feature it needs to
						 * update the tone player for the subscriber and its
						 * child MSISDN's. The child MSISDN's of subscriber will
						 * be present in CHILD_MDN attribute of extra info.
						 */
						String extraInfo = subscriber.extraInfo();

						logger.debug("Extra information of Subscriber: "
								+ subID + ", extra info: " + extraInfo);

						Map<String, String> map = DBUtility
								.getAttributeMapFromXML(extraInfo);
						if (null != map) {
							String child = map.get(CHILD_MDN);
							if (null != child && !"".equals(child)) {
								String[] childMsisdns = child.split(",");
								for (String msisdn : childMsisdns) {
									requestParams.put(PARAM_SUB_ID,
											replacePrefixInSubId(msisdn));
									logger.debug("Making Http call for child"
											+ " subscriber: " + msisdn
											+ ", requestParams: "
											+ requestParams);
									String childResponse = makeHttpRequest(
											httpParameters, requestParams);
									logger.debug("Http response for Subscriber: "
											+ msisdn
											+ ", response: "
											+ childResponse);
								}
							} else {
								logger.warn("No Child MSISDN found for the"
										+ " subscriber: " + subID);
							}
						} else {
							logger.warn("No extra info is found for"
									+ " subscriber: " + subID);
						}

						if (responseStr != null)
							responseStr = responseStr.trim();
						else
							response = false;

					} catch (Exception e) {
						logger.error("Faild to make HTTP call to TonePalyer, "
								+ "Exception: " + e.getMessage(), e);
						responseStr = e.getMessage();
						if (responseStr == null) {
							responseStr = "Exception";
						}
					}
					long differenceTime = (System.currentTimeMillis() - requestedTimeStamp);
					URL url = new URL(httpParamList.get(0));
					String ipAddress = url.getHost();
					synchronized (logDateFormatter) {
						// FORMAT:- TYPE:ACTION:XML
						writeTrans(getActionString(action), subID,
								logDateFormatter.format(requestedTimeStamp),
								String.valueOf(differenceTime),
								requestParams.get(PARAM_TYPE) + ":"
										+ requestParams.get(PARAM_ACTION) + ":"
										+ requestParams.get(PARAM_XML),
								responseStr, ipAddress);
					}
					logger.info("TonePlayer Update response: " + responseStr);

					if (responseStr != null)
						response = response
								&& responseStr
										.equalsIgnoreCase(RESPONSE_SUCCESS);
				} catch (Exception e) {
					logger.error(
							"Unable to update TonePalyer, Exception: "
									+ e.getMessage(), e);
				}

				if (m_modesAllowedToUploadFiles != null
						&& latestSubStatus != null
						&& m_modesAllowedToUploadFiles.contains(latestSubStatus
								.selectedBy())) {
					break;
				}

			}// end of while all tokens

			// updating the database
			if (response) {
				if (action == ACTION_TYPE_ADD_SEL) {
					if (statusMap.containsKey(LOOP_STATUS_LOOP)
							&& statusMap.get(LOOP_STATUS_LOOP).size() != 0)
						dbManager.updateAddedSelectionsInPlayer(subID,
								LOOP_STATUS_LOOP,
								statusMap.get(LOOP_STATUS_LOOP));
					if (statusMap.containsKey(LOOP_STATUS_OVERRIDE)
							&& statusMap.get(LOOP_STATUS_OVERRIDE).size() != 0)
						dbManager.updateAddedSelectionsInPlayer(subID,
								LOOP_STATUS_OVERRIDE,
								statusMap.get(LOOP_STATUS_OVERRIDE));
				} else if (action == ACTION_TYPE_REMOVE_SEL) {
					dbManager.updateRemovedSelectionsFromPlayer(subID,
							statusMap.get(LOOP_STATUS_EXPIRED_INIT));
				} else if (action == ACTION_TYPE_DEACT_USER) {
					dbManager.updateDeactivatedAtPlayer(subID,
							STATE_DEACTIVATED);
					deactivate3rdPartyAdRbt(subID);
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriber.extraInfo());
					if(extraInfoMap!=null && extraInfoMap.size()>0){
						String hlr_prov=extraInfoMap.get(iRBTConstant.HLR_PROV);
						if(hlr_prov!=null && hlr_prov.equalsIgnoreCase(iRBTConstant.NO)){
							dbManager.deleteSubscriberRecords(subscriber.subID());
						}
					}
				} else if (action == ACTION_TYPE_UPDATE_SUB) {
					if (updateType == UPDATE_TYPE_BOTH)// no need to Update ad
														// rbt here as it is
														// only suspension and
														// not dectivation
						dbManager.updateDeactivatedAtPlayer(subID,
								STATE_SUSPENDED);
					else {
						dbManager.updateSubUpdatedAtPlayer(subID);
						updateAdRbt(subID);
					}
				}
			}// end of if response is success
		} catch (Throwable t) {
			logger.error("", t);
			logger.info(" " + t.getMessage());
		}

		return response;
	}

	public static boolean sendDeactivationRequestToPlayer(
			String oldsubcriberID, String newSubscriberID) {
		Subscriber subscriber = dbManager.getSubscriber(newSubscriberID);
		if (subscriber == null)
			return false;

		SitePrefix userPrefix = CacheManagerUtil.getSitePrefixCacheManager()
				.getSitePrefixes(subscriber.circleID());
		if (userPrefix == null) {
			logger.info("RBT::user prefix null for user " + oldsubcriberID
					+ ". Deactivating the user and his selections");
			return false;
		}

		String playerURLs = userPrefix.getPlayerUrl();
		StringTokenizer stk = new StringTokenizer(playerURLs, ",");

		boolean response = true;
		while (stk.hasMoreTokens() && response) {
			List<String> httpParamList = Arrays.asList(stk.nextToken().split(
					"\\|"));

			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(httpParamList.get(0)
					+ ""
					+ getParamAsString("COMMON", "PLAYER_UPDATE_PAGE",
							playerUpdaterPage));

			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(PARAM_SUB_ID,
					replacePrefixInSubId(oldsubcriberID));
			requestParams.put(PARAM_ACTION, PARAM_ACTION_DEACTIVATE);
			if (RBTDeploymentFinder.isRRBTSystem()) {
				requestParams.put(PARAM_TYPE, USER_TYPE_RRBT);
			} else if (RBTDeploymentFinder.isPRECALLSystem()) {
				requestParams.put(PARAM_TYPE, USER_TYPE_PRE_CALL);
			}
			String responseStr = null;
			long requestedTimeStamp = System.currentTimeMillis();
			String ipAddress = null;
			try {
				logger.info(" calling makeHttp with SubID: " + oldsubcriberID
						+ " requestParams :" + requestParams
						+ " & HttpParameters :" + httpParameters);
				URL url = new URL(httpParamList.get(0));
				ipAddress = url.getHost();
				responseStr = makeHttpRequest(httpParameters, requestParams);
				if (responseStr != null)
					responseStr = responseStr.trim();
				else
					response = false;

			} catch (Exception e) {
				responseStr = e.getMessage();
				if (responseStr == null)
					responseStr = "Exception";
			}
			long differenceTime = (System.currentTimeMillis() - requestedTimeStamp);
			
			synchronized (logDateFormatter) {
				writeTrans(
						getActionString(ACTION_TYPE_DEACT_USER),
						oldsubcriberID,
						logDateFormatter.format(requestedTimeStamp),
						String.valueOf(differenceTime),
						requestParams.get(PARAM_TYPE) + ":"
								+ requestParams.get(PARAM_ACTION) + ":"
								+ requestParams.get(PARAM_XML), responseStr, ipAddress);
			}
			logger.info("RBT:: player url respose -> " + responseStr);
			if (responseStr != null)
				response = response
						&& responseStr.equalsIgnoreCase(RESPONSE_SUCCESS);
		}
		return response;

	}

	private static void updateAdRbt(String subID) {
		if (!getParamAsBoolean(ADRBT_SERVER_URL_HIT, "false"))
			return;
		Subscriber subscriber = dbManager.getSubscriber(subID);
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		logger.info("RBT:: extraInfoMap is - " + extraInfoMap);
		if (extraInfoMap == null)
			return;
		boolean isActivation = subscriber.rbtType() == 1;
		if (isActivation
				&& extraInfoMap.containsKey(EXTRA_INFO_ADRBT_ACTIVATION)) {
			activate3rdPartyAdRbt(subID, subscriber.activatedBy(), extraInfoMap);
			updateSubUpdatedAtADRbt(subscriber, isActivation);
			if (subscriber.subscriptionClass() != null) {
				if (subscriber.subscriptionClass().equalsIgnoreCase(
						getParamAsString(COMMON, "ADRBT_SUB_CLASS", "ADRBT")))
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(), ADRBT_TRANS_TYPE_ADRBT_ACT_NEW_USER);
				else
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(), ADRBT_TRANS_TYPE_ADRBT_ACT_RBT_USER);
			}
		} else if (!isActivation
				&& extraInfoMap.containsKey(EXTRA_INFO_ADRBT_DEACTIVATION)) {
			deactivate3rdPartyAdRbt(subID,
					extraInfoMap.get(EXTRA_INFO_ADRBT_MODE));
			updateSubUpdatedAtADRbt(subscriber, isActivation);
			if (subscriber.subscriptionClass() != null) {
				if (subscriber.subscriptionClass().equalsIgnoreCase(
						getParamAsString(COMMON, "ADRBT_SUB_CLASS", "ADRBT")))
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(), ADRBT_TRANS_TYPE_ADRBT_DEACT_ADRBT_USER);
				else
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(),
							ADRBT_TRANS_TYPE_ADRBT_DEACT_RBTnADRBT_USER);
			}
		}
	}

	private static void deactivate3rdPartyAdRbt(String subID) {
		Subscriber subscriber = dbManager.getSubscriber(subID);
		if (subscriber.rbtType() == 1) {
			deactivate3rdPartyAdRbt(subID, subscriber.deactivatedBy());
			updateSubUpdatedAtADRbt(subscriber, false);
			if (subscriber.subscriptionClass() != null) {
				if (subscriber.subscriptionClass().equalsIgnoreCase(
						getParamAsString(COMMON, "ADRBT_SUB_CLASS", "ADRBT")))
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(), ADRBT_TRANS_TYPE_ADRBT_DEACT_ADRBT_USER);
				else
					adrbtLog(subscriber.subID(), Calendar.getInstance()
							.getTime(),
							ADRBT_TRANS_TYPE_RBT_DEACT_RBTnADRBT_USER);
			}

		}
	}

	private static void updateSubUpdatedAtADRbt(Subscriber subscriber,
			boolean isActivation) {
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(subscriber.extraInfo());
		if (!isActivation)
			extraInfoMap.remove(EXTRA_INFO_ADRBT_DEACTIVATION);
		if (isActivation) {
			extraInfoMap.remove(EXTRA_INFO_ADRBT_ACTIVATION);
			extraInfoMap.remove(EXTRA_INFO_ADRBT_TRANS_ID);
		}
		extraInfoMap.remove(EXTRA_INFO_ADRBT_MODE);
		String finalXML = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		logger.info("RBT::finalXML-" + finalXML);
		dbManager.updateExtraInfo(subscriber.subID(), finalXML);
	}

	private static String getActionString(int action) {
		switch (action) {
		case ACTION_TYPE_ADD_SEL:
			return ACTION_TYPE_ADD_SEL_STR;
		case ACTION_TYPE_REMOVE_SEL:
			return ACTION_TYPE_REMOVE_SEL_STR;
		case ACTION_TYPE_DEACT_USER:
			return ACTION_TYPE_DEACT_USER_STR;
		case ACTION_TYPE_UPDATE_SUB:
			return ACTION_TYPE_UPDATE_SUB_STR;
		default:
			return ACTION_TYPE_UNKNOWN;
		}
	}

	/*
	 * 
	 * For a given subscriber id and action , this creates the necessary xml.
	 * The format of the xml is such that there is a selection tag for for
	 * selection.
	 * 
	 * The checks which are made include :
	 * 
	 * The start time and end time should be less than and greater than system
	 * time repsectively.
	 * 
	 * If the model is not play uncharged and the user is neither in suspended
	 * state nor active state then the selection is not processed.
	 * 
	 * If two selections are made for the same caller id, for the same from to
	 * to time and with the same status then depending on the loop status,
	 * either it is added into the loop and the wave file is just appended to
	 * the earlier wave files. Or if this selection is set as override, then a
	 * new selection tag is created for it.
	 * 
	 * Whether the user is in grace state or not.
	 * 
	 * For the selection there is a sub_selections tag which contains the common
	 * details as to whether the intro propmt is to be played or not, poll is
	 * active or not, rbt type and if the user is suspended then that is also
	 * noted.
	 */

	private static String createSubscriberXML(Subscriber sub, int action,
			HashMap<Character, ArrayList<String>> statusMap,
			SitePrefix userPrefix) {
		StringBuffer sb = null;
		try {
			// appending subscriber attributes
			String playPreRBTClip = null;
			String playPoll = "-1";
			String jingleUser = "false";
			String pcaUser = "false";
			boolean isActiveAzaanUser = false;
			boolean isAzzanCopticDuaaCos = false;

			Boolean isSuspendedVRBT = isSuspendedVRBT(sub.subYes(), sub.subID());
			
			HashMap<String, String> extraInfo = dbManager.getExtraInfoMap(sub); // changed
																				// to
																				// use
																				// extrainfo
			if (extraInfo != null
					&& extraInfo.containsKey(EXTRA_INFO_INTRO_PROMPT_FLAG)
					&& extraInfo.get(EXTRA_INFO_INTRO_PROMPT_FLAG) != null)
				playPreRBTClip = extraInfo.get(EXTRA_INFO_INTRO_PROMPT_FLAG)
						.toString();
			if (extraInfo != null && extraInfo.containsKey(PLAY_POLL_STATUS)
					&& extraInfo.get(PLAY_POLL_STATUS) != null) {
				playPoll = extraInfo.get(PLAY_POLL_STATUS).toString();
			}
			if (extraInfo != null
					&& extraInfo.containsKey(EXTRA_INFO_JINGLE_FLAG)
					&& extraInfo.get(EXTRA_INFO_JINGLE_FLAG) != null) {
				jingleUser = extraInfo.get(EXTRA_INFO_JINGLE_FLAG).toString()
						.trim();
			}
			if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PCA_FLAG)
					&& extraInfo.get(EXTRA_INFO_PCA_FLAG) != null) {
				pcaUser = extraInfo.get(EXTRA_INFO_PCA_FLAG).toString().trim();
			}

			final String subSelTag = "<sub_selections><selections ";
			sb = new StringBuffer(subSelTag);
			/* if BGM system */
			if (RBTDeploymentFinder.isBGMSystem()) {
				sb.append("bgm_type=\"BGM\"");
			} else {
				sb.append("rbt_type=\"");
				if (RBTDeploymentFinder.isRRBTSystem()) {
					sb.append(USER_TYPE_RRBT + "\"");
				} else if (RBTDeploymentFinder.isPRECALLSystem()) {
					sb.append(USER_TYPE_PRE_CALL
							+ "\" max_play=\"25\" counter_value=\"3\"");
				} else if (vrbtSubscriptionClassList.contains(sub
						.subscriptionClass())) {
					// Type will be VRBT for Idea
					sb.append(USER_TYPE_VRBT + "\"");
					playPreRBTClip = "2";
				} else if (isSuspendedVRBT) {
					sb.append(USER_TYPE_VRBT + "\"");
					sb.append(" sub_type=\"OM2\"");	
				} else {
					sb.append(dbManager.getRBTUserType(sub) + "\"");
				}
			}
         
			boolean isRrbtConsentUser = (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_RRBT_TYPE_FLAG)
					&& extraInfo.get(EXTRA_INFO_RRBT_TYPE_FLAG) != null);
			
			if (isRrbtConsentUser) {
				String rrbtTpe = extraInfo.get(EXTRA_INFO_RRBT_TYPE_FLAG).toString().trim();
				sb.append(" rrbt_type=\"CONSENT\" ");
			}

			//RBT-12494
			boolean isRrbtSuspension=(extraInfo != null && extraInfo.containsKey(EXTRA_INFO_RRBT_TYPE_SUSPENSION_FLAG));
			String rrbtTpesuspension =null;
			if (isRrbtSuspension) {
				rrbtTpesuspension = extraInfo.get(EXTRA_INFO_RRBT_TYPE_SUSPENSION_FLAG).toString().trim();
				sb.append(" "+EXTRA_INFO_RRBT_TYPE_FLAG +"=\""+rrbtTpesuspension+"\"");
			}
			
					
			// ppu changes
			String cosId = sub.cosID();
			if (null != cosId) {
				CosDetails cosDetail = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosId);
				if (null != cosDetail
						&& iRBTConstant.COS_TYPE_PPU.equals(cosDetail
								.getCosType())) {
					sb.append(" ppu_enabled=\"True\" ");
				}
			}
			// Azaan feature: To indicate the subscriber as an Azaan subscriber,
			// set sub_type="AZAAN" to user xml.
			// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
			boolean isMulipleAzaanAllowed = false;
			if (null != cosTypesForMultiPack
					&& !cosTypesForMultiPack.isEmpty()) {
				isMulipleAzaanAllowed = true;
			}
			if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PACK)) {
				String subscriberCosIds = extraInfo.get(EXTRA_INFO_PACK);
				
				// Get the entries from provisioning requests table by
				// subscriber id and type i.e. cosid  if status is 33
				// then update sub_type to azaan.
				List<String> subscriberCosIdList = ListUtils.convertToList(
						subscriberCosIds, ",");
				Set<String> commonCosIds = ListUtils.intersection(
						azaanCosIdList, subscriberCosIdList);
				logger.debug("Azaan cos Ids: " + commonCosIds
						+ " for subscriber: " + sub.subID());
				// One of the subscriber cos id is Azaan cos. So, update the
				// player status.
				Iterator<String> iterator = commonCosIds.iterator();
				
				for(String subCosId : subscriberCosIdList){
					if(confAzaanCopticDoaaCosIdSubTpeMap.containsKey(subCosId)){
						isAzzanCopticDuaaCos = true;
						break;
					}
				}
				if (isAzzanCopticDuaaCos) {
					String str = "";
					for (String acdCosId : subscriberCosIdList) {
						// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
						if (confAzaanCopticDoaaCosIdSubTpeMap
								.containsKey(acdCosId)) {
							boolean isPackActive = isPackActive(sub.subID(),
									Integer.parseInt(acdCosId));
							if (isPackActive) {
								String subType = confAzaanCopticDoaaCosIdSubTpeMap
										.get(acdCosId);
								// Jira :RBT-15026: Changes done for allowing
								// the multiple Azaan pack. Check the allow
								// multiple azaan pack is Configured or not.
								if (isMulipleAzaanAllowed) {
									str += subType + "_" + acdCosId + ",";
								} else {
									str += subType + ",";
								}
							}

						}
					}
					if (str.length() > 0) {
						if (str.lastIndexOf(",") != -1)
							str = str.substring(0, str.lastIndexOf(","));
						sb.append(" sub_type=\"" + str + "\" ");
					}
				} else if (iterator.hasNext()) {
					String subscriberId = sub.subID();
					int cos = Integer.parseInt(iterator.next());
					boolean isPackActive = isPackActive(subscriberId, cos);
					if (isPackActive) {
						isActiveAzaanUser = true;
						if (isMulipleAzaanAllowed) {
							// Jira :RBT-15026: Changes done for allowing
							// the multiple Azaan pack. Check the allow
							// multiple azaan pack is Configured or not.
							sb.append(" sub_type=\"");
							sb.append(AZAAN + "_" + cos + "\" ");
						} else {
							sb.append(" sub_type=\"AZAAN\" ");
						}
					}
				}else {
					logger.debug("Not adding sub_type. subscriberCosIds: "
							+ subscriberCosIds + ", are not configured: "
							+ azaanCosIdList);
				}
				
			}

			if (pcaUser != null && pcaUser.trim().equalsIgnoreCase("TRUE")) {
				String pcaFile = null;
				String frequency = null;
				SubscriberAnnouncements[] subscriberAnnouncements = RBTDBManager
						.getInstance().getActiveSubscriberAnnouncemets(
								sub.subID());
				if (subscriberAnnouncements != null
						&& subscriberAnnouncements.length != 0) {
					SubscriberAnnouncements announcement = subscriberAnnouncements[subscriberAnnouncements.length - 1];
					frequency = announcement.frequency();
					int clipID = announcement.clipId();
					Clip clip = rbtCacheManager.getClip(clipID, null);
					if (clip != null)
						pcaFile = clip.getClipRbtWavFile();
				}

				sb.append(" pca_file=\"");
				sb.append(pcaFile + "\"");

				sb.append(" pca_frequency=\"");
				sb.append(frequency + "\"");
			}
			if (playPreRBTClip != null) {
				sb.append(" pre_rbt_clip=\"");
				sb.append(playPreRBTClip + "\"");
			}

			// If extraInfo contains pre_rbt_wav_file put in the player xml, and
			// put pre_rbt_clip='1' if playPreRBTClip is null
			if (extraInfo != null
					&& extraInfo.containsKey(EXTRA_INFO_PRE_RBT_WAV)) {
				if (playPreRBTClip == null) {
					sb.append(" pre_rbt_clip=\"");
					sb.append("1\"");
				}
				String preRbtWav = extraInfo.get(EXTRA_INFO_PRE_RBT_WAV);
				sb.append(" pre_rbt_wav=\"");
				sb.append(preRbtWav + "\"");			
			}
			if (extraInfo != null
					&& extraInfo.containsKey(EXTRA_INFO_DTMF_KEYS)) {
				String dtmfKeys = extraInfo.get(EXTRA_INFO_DTMF_KEYS);
				if(null != dtmfKeys && !"".equals(dtmfKeys)) {
					sb.append(" dtmf_list=\"");
					sb.append(dtmfKeys + "\"");
				}
			}
			if (!playPoll.equalsIgnoreCase("-1")) {
				sb.append(" play_poll=\"");
				sb.append(playPoll + "\"");
			}
					
			if (!isSuspendedVRBT && (sub.subYes().equals(STATE_SUSPENDED_INIT)
					|| sub.subYes().equals(STATE_SUSPENDED))) {
				sb.append(" status=\"suspend\"");
			}

			if (!RBTDBManager.getInstance().isSubscriberDeactivated(sub)
					&& sub.cosID() != null) {
				CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(sub.cosID());
				if (cos != null
						&& WebServiceConstants.COS_CONTENT_TYPE_PROFILE
								.equalsIgnoreCase(cos.getContentTypes())) {
					// If User availed PROFILE COS (Cos with only PROFILE as
					// content type), then Default Jingle will not be played
					sb.append(" play_default=\"false\"");
				}
			}
			
			//Added for ephemeral rbt
			if(isEphemeralRBT(sub.subID(),0, false)){
			  sb.append(" info=\"X\"");
			}
					
			sb.append(" >");

			if (jingleUser != null
					&& jingleUser.trim().equalsIgnoreCase("true")) {
				String durationStr = RBTParametersUtils.getParamAsString(
						DAEMON, "LAST_N_DAYS_SUPPORTED_FOR_JINGLE", null);
				if (durationStr != null) {
					int duration = Integer.parseInt(durationStr);
					int subscriptionPeriod = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(sub.subscriptionClass())
							.getSubscriptionPeriodInDays();

					Calendar cal = Calendar.getInstance();
					cal.setTime(sub.startDate());
					cal.add(Calendar.DAY_OF_YEAR,
							(subscriptionPeriod - duration));

					int frequency = RBTParametersUtils.getParamAsInt(DAEMON,
							"JINGLE_USER_FREQUENCY", 3);
					sb.append("<tags><tag name=\"")
							.append(PLAYER_XML_JINGLE_FLAG)
							.append("\" start_date=\"")
							.append(formatter.format(cal.getTime()))
							.append("\" end_date=\"")
							.append(formatter.format(sub.endDate()))
							.append("\" frequency=\"").append(frequency)
							.append("\"/></tags>");
				}
			}

			/*
			 * getting all subscriber selections If selection Loop status is
			 * expired(X) or delay_deact and the parameter for this is enabled
			 * do not process If selection Loop status is in expired_init(x) add
			 * refid of this selection to expiredList and continue
			 */
			SubscriberStatus[] allSelections = null;
			if (!(extraInfo != null && extraInfo.containsKey("VOLUNTARY")
					&& extraInfo.get("VOLUNTARY").equalsIgnoreCase("TRUE") && getParamAsBoolean(
						COMMON, "PLAY_DEFAULT_SONG_FOR_VOLUNTARY_SUSPENSION",
						"FALSE")) && !isRrbtConsentUser) {
				allSelections = dbManager
						.getAllSubSelectionRecordsForTonePlayer(sub.subID());
			}
			SubscriberStatus oldSelection = null;
			boolean isGrace = false;
			StringBuffer subWavFile = new StringBuffer();
			Date setTime = null;
			boolean isCallerIDSelSuspend = false;

			ArrayList<String> overrideList = new ArrayList<String>();
			ArrayList<String> loopList = new ArrayList<String>();
			ArrayList<String> expiredList = new ArrayList<String>();

			int profileStatus = 99;
			boolean updateAzzanType = false;
			
			//Commented for RBT-14624	Signal app - RBT tone play notification feature 
			RBTLoginUser user = Utility.getRBTLoginUserBasedOnAppName(
					sub.subID(), null);
			if (user != null && sendAppName) {
				sb = appendAppName(user, sb);
			} else {
				String sendAppName = null;	
				try {
					ApplicationContext context = ConfigUtil.getApplicationContext();
					if(context != null) {
						sendAppName = context.getMessage("sendAPPName", null, LocaleContextHolder.getLocale());
						List<String> paramValue= Arrays.asList(RBTParametersUtils.getParamAsString("COMMON","DTOC_APP_SERVICE_CLASS","").split("\\,"));
						
						if((sendAppName != null && sendAppName.equalsIgnoreCase("YES")) || 
								paramValue.contains(sub.subscriptionClass()) ? true : isPackActiveForUser(sub)) {							
							sb = appendAppName(user, sb);
						}
						
					}
					
				} catch (Exception e) {
					logger.info("Exception came while appending app name from config.properties : "+e,e);
				}
			}
			
			for (int i = 0; allSelections != null && i < allSelections.length; i++) {
				SubscriberStatus thisSelection = allSelections[i];
				// //RBT-18483 & RBT-18121 Skipping Paid selection for free user
				logger.info(":---> IN LOOP");
				logger.info(" :---> thisSelection.subscriberFile() : "+thisSelection.subscriberFile());

				//thisSelection.subscriberFile();
				try {
					IUserDetailsService operatorUserDetailsService = null;
					operatorUserDetailsService = (IUserDetailsService) ConfigUtil
							.getBean(BeanConstant.USER_DETAIL_BEAN);
					OperatorUserDetails operatorDetails = (OperatorUserDetails) operatorUserDetailsService
							.getUserDetails(sub.subID());
					if (operatorDetails != null) {
						logger.info(":---> operatorDetails.serviceKey()  " + operatorDetails.serviceKey());
					}
					if ((operatorUserDetailsService != null && operatorDetails != null)
							&& ((operatorDetails.serviceKey()
									.equalsIgnoreCase(OperatorUserTypes.PAID_APP_USER_LOW_BALANCE.getDefaultValue())
									|| operatorDetails.serviceKey()
											.equalsIgnoreCase(OperatorUserTypes.FREE_APP_USER.getDefaultValue())))) {

						String CategoryId1 = ConsentPropertyConfigurator.getFreeClipCategoryID();
						logger.info("FreeclipCategoryId" + CategoryId1);

						if (thisSelection.categoryID() != Integer.parseInt(CategoryId1)
								&& thisSelection.status() != 200) {
							logger.info(
									":---> Skipping the selection as Free user cant have song other than configured category and ephemeral");
							continue;
						}
						
						/*else if(thisSelection.status() != 200 && thisSelection.subscriberFile().contains("_cut_"))
						{
							logger.info(
									":---> Skipping the selection as Free user cant have cut rbt song");
							continue;
						}*/

					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				int selectionCatType = thisSelection.categoryType();
				int selectionCatId = thisSelection.categoryID();
				boolean activeOrSuspendedSel = thisSelection.selStatus().equals(STATE_ACTIVATED)
						|| thisSelection.selStatus().equals(STATE_SUSPENDED);
				logger.info("Checking selection for AZZAN. category type: "
						+ selectionCatType + ", category id: " + selectionCatId
						+ ", refId: " + thisSelection.refID()
						+ ", activeOrSuspendedSel: " + activeOrSuspendedSel
						+ ", selStatus: "+thisSelection.selStatus());
				if (!updateAzzanType
						&& selectionCatType == iRBTConstant.SHUFFLE
						&& confAzaanCategoryIdList.contains(String
								.valueOf(selectionCatId))
						&& activeOrSuspendedSel) {
					int st = sb.indexOf(subSelTag);
					sb.insert(st + subSelTag.length(), " sub_type=\"AZAAN\" ");
					updateAzzanType = true;
				}
				
				// RBT-3954 Sulekha hack
				if (thisSelection.extraInfo() != null
						&& thisSelection.extraInfo().contains(
								"DTMF_TO_THIRD_PARTY")
						&& sb.indexOf("third_party_url=") == -1) {
					String tillNowString = sb.toString();
					tillNowString = tillNowString
							.replace("<selections",
									"<selections pre_rbt_wav=\"rbt_dummy_sulekha_rbt\" third_party_url=\"true\"");
					sb = new StringBuffer(tillNowString);
				}

				// Ignore all the suspended overridable selections
				if (thisSelection.status() != 1
						&& (thisSelection.selStatus().equals(STATE_SUSPENDED) || thisSelection
								.selStatus().equals(STATE_SUSPENDED_INIT))) {
					logger.info("Ignoring the selection: " + thisSelection);
					// Added by Sreekar regarding JIRA RBT-4206
					if (thisSelection.loopStatus() == LOOP_STATUS_LOOP
							|| thisSelection.loopStatus() == LOOP_STATUS_OVERRIDE) {
						dbManager
								.updateLoopStatus(
										thisSelection,
										thisSelection.loopStatus() == LOOP_STATUS_LOOP ? LOOP_STATUS_LOOP_FINAL
												: LOOP_STATUS_OVERRIDE_FINAL,
										null);
					}
					continue;
				}

				Clip clip = rbtCacheManager
						.getClipByRbtWavFileName(thisSelection.subscriberFile());
				String blockedAlbumNames = RBTParametersUtils.getParamAsString(
						COMMON, "BLOCKED_ALBUMS_FOR_PROMPT", null);
				boolean isBlockedContent = false;
				if (clip != null) {
					if (blockedAlbumNames != null) {
						Set<String> blockedAlbumSet = new HashSet<String>();
						for (String album : blockedAlbumNames.split(",")) {
							blockedAlbumSet.add(album.trim());
						}
						isBlockedContent = blockedAlbumSet.contains(clip
								.getAlbum());
					}
				}

				String selCatID = null;
				if (isBlockedContent) {
					selCatID = RBTParametersUtils.getParamAsString(COMMON,
							"CATEGORYID_FOR_BLOCKED_ALBUMS_FOR_PROMPT",
							String.valueOf(thisSelection.categoryID()));
				}

				String thisSubWavFileToAppend = getSubWavFileToAppened(
						thisSelection, selCatID);
				if (thisSubWavFileToAppend == null
						&& thisSelection.categoryType() == FESTIVAL_NAMETUNES_SHUFFLE) {
//					sb.append(createSelectionTag(thisSelection, thisSubWavFileToAppend,
//							thisSelection.categoryType(), profileStatus, true));
					oldSelection = thisSelection;
					continue;
				}
				logger.info("Sree::0.sub-" + thisSelection.subID() + ",caller-"
						+ thisSelection.callerID() + ",thisSubWavFileToAppend-"
						+ thisSubWavFileToAppend);
                
				if (thisSelection.loopStatus() == LOOP_STATUS_EXPIRED)
					continue;

				if (thisSelection.loopStatus() == LOOP_STATUS_OVERRIDE)
					overrideList.add(thisSelection.refID());
				if (thisSelection.loopStatus() == LOOP_STATUS_LOOP)
					loopList.add(thisSelection.refID());
				if (thisSelection.loopStatus() == LOOP_STATUS_EXPIRED_INIT) {
					expiredList.add(thisSelection.refID());
					continue;
				}

				if (thisSelection.selStatus().equals(STATE_GRACE))
					isGrace = true;
				if (!userPrefix.playUncharged(thisSelection.prepaidYes())
						&& !thisSelection.selStatus().equals(STATE_ACTIVATED)
						&& !thisSelection.selStatus().equals(STATE_SUSPENDED))
					continue;
				logger.info("Sree::1.sub-" + thisSelection.subID() + ",caller-"
						+ thisSelection.callerID() + ",thisSubWavFileToAppend-"
						+ thisSubWavFileToAppend);
				
			
				
				if (oldSelection == null) {
					oldSelection = thisSelection;
					subWavFile.append(thisSubWavFileToAppend);
					if (thisSelection.selStatus().equals(STATE_SUSPENDED))
						isCallerIDSelSuspend = true;
					setTime = thisSelection.setTime();
					
					if(thisSelection.categoryType() == FESTIVAL_NAMETUNES_SHUFFLE){
						sb.append(createSelectionTag(thisSelection,
								subWavFile.toString(), thisSelection.categoryType(),
								profileStatus, true));
					}
					
					continue;
				}
				String oldCallerID = oldSelection.callerID();
				String thisCallerID = thisSelection.callerID();
				boolean isSameCallerID = (thisCallerID == null && oldCallerID == null)
						|| (thisCallerID != null && oldCallerID != null && thisCallerID
								.equals(oldCallerID));

				boolean isSameStatus = thisSelection.status() == oldSelection
						.status();
				if (isSameStatus && thisSelection.status() == 99)
					isSameStatus = false;

				boolean isSameTimeInterval = thisSelection.fromTime() == oldSelection
						.fromTime()
						&& thisSelection.toTime() == oldSelection.toTime();

				String oldSelInterval = oldSelection.selInterval();
				String thisSelInterval = thisSelection.selInterval();
				boolean isSameSelInterval = (thisSelInterval == null && oldSelInterval == null)
						|| (thisSelInterval != null && thisSelInterval
								.equals(oldSelInterval));

				if (isSameCallerID && isSameStatus && isSameTimeInterval
						&& isSameSelInterval) {
					// The following if takes care of the condition that user
					// selected 2
					// songs in the same call and the last one in override, so
					// that we
					// will play only one song to the user

					if ((thisSelection.loopStatus() == LOOP_STATUS_OVERRIDE || thisSelection
							.loopStatus() == LOOP_STATUS_OVERRIDE_FINAL)
							&& (setTime != null && setTime.before(thisSelection
									.setTime()))) {
						oldSelection = thisSelection;
						setTime = thisSelection.setTime();
						logger.info("Sree::2.replacing wavfile-"
								+ subWavFile.toString() + " to-"
								+ thisSubWavFileToAppend);
						subWavFile = new StringBuffer(thisSubWavFileToAppend);
					} else {
						if (thisSelection.loopStatus() == LOOP_STATUS_LOOP
								|| thisSelection.loopStatus() == LOOP_STATUS_LOOP_FINAL) {
							if (!thisSelection.selStatus().equals(
									STATE_SUSPENDED)) {
								oldSelection = thisSelection;
								if (isCallerIDSelSuspend) {
									if (setTime != null
											&& setTime.before(thisSelection
													.setTime())) {
										logger.info("Sree::3.replacing wavfile-"
												+ subWavFile.toString()
												+ " to-"
												+ thisSubWavFileToAppend);
										subWavFile = new StringBuffer(
												thisSubWavFileToAppend);
										isCallerIDSelSuspend = false;
									}
								} else {
									logger.info("Sree::4.appending wavfile-"
											+ subWavFile.toString() + " to-"
											+ thisSubWavFileToAppend);
									subWavFile.append(","
											+ thisSubWavFileToAppend);
								}
							}
						}
					}
				}// end of if callerID and status same
				else {
					sb.append(createSelectionTag(oldSelection,
							subWavFile.toString(), oldSelection.categoryType(),
							profileStatus, false));
					
					oldSelection = thisSelection;
					setTime = thisSelection.setTime();
					if (thisSelection.selStatus().equals(STATE_SUSPENDED))
						isCallerIDSelSuspend = true;
					logger.info("Sree::6.starting new wav file"
							+ thisSubWavFileToAppend);
					subWavFile = new StringBuffer(thisSubWavFileToAppend);
				}
				// Incrementing the profile status here. This is for Voice
				// Presence feature.
				if (thisSelection.status() == 99) {
					profileStatus++;
				}			
				if(thisSelection.categoryType() == FESTIVAL_NAMETUNES_SHUFFLE){
					sb.append(createSelectionTag(thisSelection,
							subWavFile.toString(), thisSelection.categoryType(),
							profileStatus, true));
				}

			}
			statusMap.put(LOOP_STATUS_OVERRIDE, overrideList);
			statusMap.put(LOOP_STATUS_LOOP, loopList);
			statusMap.put(LOOP_STATUS_EXPIRED_INIT, expiredList);
			

			if (oldSelection != null) {
				if(oldSelection.categoryType() != FESTIVAL_NAMETUNES_SHUFFLE || subWavFile.toString().trim().length() != 0) {
					sb.append(createSelectionTag(oldSelection,
							subWavFile.toString(), oldSelection.categoryType(),
							profileStatus, false));
				}
				
//				int selectionCatType = oldSelection.categoryType();
//				int selectionCatId = oldSelection.categoryID();
//				logger.info("Checking selection for AZZAN. category type: "
//						+ selectionCatType + ", category id: " + selectionCatId
//						+ ", refId: " + oldSelection.refID());
//				if (selectionCatType == iRBTConstant.SHUFFLE
//						&& confAzaanCategoryIdList.contains(String
//								.valueOf(selectionCatId))) {
//					int st = sb.indexOf(subSelTag);
//					sb.insert(st + subSelTag.length(), " sub_type=\"AZAAN\" ");
//				}
			}
			
			boolean noActiveDefaultSelectionExists = noActiveDefaultSelectionExists(allSelections);
			
			if (defaultClip != null
					&& noActiveDefaultSelectionExists) {
				SubscriberStatusImpl defaultSel = new SubscriberStatusImpl(
						sub.subID(), null, 26, defaultClip.getClipRbtWavFile(),
						sub.startDate(), sub.startDate(), sub.endDate(), 1,
						"DEFAULT", "CC", "CC", sub.startDate(), "y", 0, 2359,
						"B", null, null, 5, 'B', sub.rbtType(), null,
						"player_default", null, sub.circleID(), null);

				sb.append(createSelectionTag(
						defaultSel,
						defaultSel.subscriberFile() + "|"
								+ defaultSel.categoryID(),
						defaultSel.categoryType(), profileStatus, false));
			}
			
			boolean isNoActiveDefaultSelectionExists = isNoActiveDefaultSelectionExists(allSelections);
			
			if((isActiveAzaanUser || isAzzanCopticDuaaCos) && isNoActiveDefaultSelectionExists) {
				String conf = getParamAsString("DAEMON",
						"AZAAN_NOSEL_DEFAULT_TONE_CONF", null);
				String[] confArray = conf.split("\\|");
				Date date = getFormattedDate("2037-01-01 14:35:06");
				SubscriberStatusImpl defaultSel = new SubscriberStatusImpl(sub.subID(), "ALL", Integer.parseInt(confArray[1]), confArray[0], new Date(),
						new Date(), date, 1, "DEFAULT", "CC", "CC",
						sub.startDate(), "y", 0, 2359, "Z", null,
						null, 5, 'B', sub.rbtType(), null, "player_default", null, sub.circleID(),null);

				sb.append(createSelectionTag(
						defaultSel,
						defaultSel.subscriberFile() + "|"
						        + defaultSel.categoryID(),
						        defaultSel.categoryType(), profileStatus, false));
			}

			//RBT-9568
			String defaultsong2 = getParamAsString("COMMON",
					"DEFAULT_SONG_2_CONFIGURATION", null);
			if (defaultsong2 != null && noActiveDefaultSelection2Exists(allSelections)) {
				String defaultsongConfig[] = null;
				defaultsongConfig = defaultsong2.split(",");

					try {

						if (defaultsongConfig != null
								&& defaultsongConfig.length == 4) {

							SubscriberStatusImpl defaultSel = new SubscriberStatusImpl(
									sub.subID(), null,
									Integer.parseInt(defaultsongConfig[1]),
									defaultsongConfig[0], sub.startDate(),
									sub.startDate(), sub.endDate(),
									Integer.parseInt(defaultsongConfig[3]),
									"DEFAULT", "CC", "CC", sub.startDate(),
									"y", 0, 2359, "B", null, null,
									Integer.parseInt(defaultsongConfig[2]),
									'B', sub.rbtType(), null, "player_default",
									null, sub.circleID(),null);

							sb.append(createSelectionTag(defaultSel,
									defaultSel.subscriberFile() + "|"
											+ defaultsongConfig[1],
									Integer.parseInt(defaultsongConfig[2]),
									new Integer(defaultsongConfig[3]), false));
							logger.info("Default song 2 selection tag added successfully for the configuration : "
									+ defaultsong2);

						} else {
							logger.info("DEFAULT_SONG_2_CONFIGURATION is : "
									+ defaultsong2);
						}
					} catch (Exception e) {
						logger.info("Exception occurr for wrong DEFAULT_SONG_2_CONFIGURATION: "
								+ defaultsong2);
					}
			}
			// end
			if(isRrbtConsentUser && noActiveDefaultSelectionExists) {
				
				String rrbtTypeConsentDefaultWavFileName = RBTParametersUtils.getParamAsString(COMMON,
						"RRBT_TYPE_CONSENT_DEFAULT_WAV_FILE_NAME",	"rbt_consent_prompt_rbt");
				
				SubscriberStatusImpl defaultSel = new SubscriberStatusImpl(
						sub.subID(), null, 3, rrbtTypeConsentDefaultWavFileName ,
						sub.startDate(), sub.startDate(), sub.endDate(), 1,
						"DEFAULT", "CC", "CC", sub.startDate(), "y", 0, 2359,
						"B", null, null, 5, 'B', sub.rbtType(), null,
						"player_default", null, sub.circleID(),null);

				sb.append(createSelectionTag(
						defaultSel,
						defaultSel.subscriberFile() + "|"
								+ defaultSel.categoryID(),
						defaultSel.categoryType(), profileStatus, false));
			}
			//RBT-12494
			if(isRrbtSuspension && noActiveDefaultSelectionExists) {
				
				String rrbtTypeSuspensionWavFileName=null;
				  rrbtTypeSuspensionWavFileName = RBTParametersUtils.getParamAsString(COMMON,
						"RRBT_TYPE_SUSPENSION_WAV_FILE_FOR_"+rrbtTpesuspension,	null);
				
				SubscriberStatusImpl defaultSel = new SubscriberStatusImpl(
						sub.subID(), null, 3, rrbtTypeSuspensionWavFileName ,
						sub.startDate(), sub.startDate(), sub.endDate(), 1,
						"DEFAULT", "CC", "CC", sub.startDate(), "y", 0, 2359,
						"B", null, null, 5, 'B', sub.rbtType(), null,
						"player_default", null, sub.circleID(),null);

				sb.append(createSelectionTag(
						defaultSel,
						defaultSel.subscriberFile() + "|"
								+ defaultSel.categoryID(),
						defaultSel.categoryType(), profileStatus, false));
			}
			
			logger.info(":--->  sb()"+sb.toString()+"\ns");
			logger.info("(sb.toString().indexOf(wave) "+(sb.toString().indexOf("wav_file")));
			logger.info("isGrace"+isGrace);
			logger.info("old"+oldSelection);
			
			if (action == ACTION_TYPE_ADD_SEL
					&& (oldSelection == null || (sb.toString()
							.indexOf("wav_file")) == -1) && !isGrace) {
				logger.info("RBT::allSelections " + allSelections);
				for (int i = 0; allSelections != null
						&& i < allSelections.length; i++) {
					logger.info("RBT::allSelections SelStatus "
							+ allSelections[i].selStatus() + " I is " + i
							+ " loopStatus() " + allSelections[i].loopStatus());
				}
				return null;
			}
		}

		catch (Throwable t) {
			logger.error("", t);
			logger.info(" " + t.getMessage());
		}
		if (sb != null) {
			// sb.append("</selections></sub_selections>");
			sb.append("</selections>");
			if (getParamAsBoolean("COMMON", "IS_BLOCK_ENABLED", "FALSE")) {
				String strCallerID = "";
				Groups group = dbManager
						.getGroupByPreGroupID("99", sub.subID()); // PreDefinedGroupID
																	// 99
				if (group != null) {
					GroupMembers[] groupMembers = dbManager
							.getMembersForGroupID(group.groupID());
					for (int i = 0; groupMembers != null
							&& i < groupMembers.length; i++) {
						if (i < groupMembers.length - 1)
							strCallerID += groupMembers[i].callerID() + ",";
						else
							strCallerID += groupMembers[i].callerID();
					}
				}
				if (!strCallerID.equals(""))
					sb.append("<blocked caller_id=\"" + strCallerID + "\"/>");

			}
			sb.append("</sub_selections>");
			logger.info("SUBSCRIBER_XML: "+sb.toString());
			return sb.toString();
		}

		return null;
	}
	private static Boolean isPackActiveForUser(Subscriber subscriber) {
		if (subscriber.extraInfo() != null) {
			HashMap<String, String> extraInfoMap =DBUtility.getAttributeMapFromXML(subscriber.extraInfo());	
			if(extraInfoMap.containsKey("PACK")){
				String packIdString = extraInfoMap.get("PACK");
				String packIds[] = packIdString.split(",");
				String freeTrailCosId = Integer.toString(RBTParametersUtils.getParamAsInt("COMMON","DTOC_FREE_TRIAL_COS_ID", 0));
				if(Arrays.asList(packIds).contains(freeTrailCosId)) {
					if(RBTDBManager.getInstance().getAciveProvisioningRequests(subscriber.subID(), Integer.valueOf(freeTrailCosId)).size() > 0);
						return true; 
				}
			}			
		}	
		return false;
	}

	private static Date getFormattedDate(String str){
		Date date = null;
		try{
			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
		} catch(ParseException e){

		}
		return date;
	}
	private static Boolean isSuspendedVRBT(String subYes, String subId) {
		String vrbtCatIdSubSongSrvKeyConfig = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VRBT_CAT_ID_SUB_SONG_SRV_KEY_MAPPING", null);
		if (vrbtCatIdSubSongSrvKeyConfig != null && subYes != null && (subYes.equals(STATE_SUSPENDED_INIT) || subYes.equals(STATE_SUSPENDED)) && !checkCallerBlackListed(subId)){
			return true;
		}
		return false;
	}

	private static boolean checkCallerBlackListed(String callerId) {
		MemCachedClient mc = RBTCache.getMemCachedClient();
		String key = callerId.concat("_BLACKLISTED");
		Object value = mc.get(key);
		// Blacklisted when the subscriber is present in memcache.
		boolean isBlackListed = (null != value) ? true : false;
		if (logger.isDebugEnabled()) {
			logger.debug("Checking subscriber is blacklisted in memcache."
					+ " key: " + key + ", value: " + value
					+ ", isBlackListed: " + isBlackListed);
		}
		return isBlackListed;
	}
	
	private static boolean isPackActive(String subscriberId, int cos) {
		logger.debug("Checking pack status. subscriberId: " + subscriberId
				+ ", cos: " + cos);
		List<ProvisioningRequests> provRequests = dbManager
				.getAciveProvisioningRequests(subscriberId, cos);
		if (provRequests.size() > 0) {
			ProvisioningRequests provisioningRequests = provRequests.get(0);
			int status = provisioningRequests.getStatus();
			if (status == PACK_ACTIVATED) {
				logger.info("Returning true, pack is status active. provisioning"
						+ " requestId: " + provisioningRequests.getRequestId()
						+ ", transId: " + provisioningRequests.getTransId());
				return true;
			}
		}
		logger.info("Returning false, pack is status NOT active. "
				+ "provRequests: " + provRequests);
		return false;
	}

	private static boolean isNoActiveDefaultSelectionExists(SubscriberStatus[] allSelections) {
		if (allSelections == null || allSelections.length == 0)
			return true;
		for (SubscriberStatus subscriberStatus : allSelections) {
			if (subscriberStatus.callerID() == null
					&& subscriberStatus.endTime().after(Calendar.getInstance().getTime())
					&& subscriberStatus.selStatus().equalsIgnoreCase("B"))
				return false;
		}
		return true;
	}
	
	private static boolean noActiveDefaultSelectionExists(
			SubscriberStatus[] allSelections) {
		if (allSelections == null || allSelections.length == 0)
			return true;
		for (SubscriberStatus subscriberStatus : allSelections) {
			if (subscriberStatus.callerID() == null
					&& subscriberStatus.endTime().after(
							Calendar.getInstance().getTime()))
				return false;
		}
		return true;
	}

	//RBT-9568
	private static boolean noActiveDefaultSelection2Exists(
			SubscriberStatus[] allSelections) {
		
		if (allSelections == null || allSelections.length == 0)
			return false;
		boolean response=false;
		for (SubscriberStatus subscriberStatus : allSelections) {

			String callerID = subscriberStatus.callerID();
			String selStatus = subscriberStatus.selStatus();
			
			if ((subscriberStatus.callerID() == null || subscriberStatus
					.callerID().equalsIgnoreCase("ALL"))
					&& subscriberStatus.endTime().after(
							Calendar.getInstance().getTime()) && (selStatus.equals("B") || selStatus.equals("D") || selStatus.equals("P")))
				return false;
			
			if (callerID != null
					&& !callerID.equalsIgnoreCase("ALL")
					&& subscriberStatus.endTime().after(
							Calendar.getInstance().getTime())
					&& (selStatus.equals("B") || selStatus.equals("D") || selStatus.equals("P"))) {
				response=true;
			}

		}
		return response;
	}
	/*
	 * Creates the selection tag for the XML to be sent to the player jsp.
	 * Includes : callerId, from time, to time, start time, end time, status,
	 * type (and if cricket, feed type) loop status and selection type
	 * (RRBT/RBT).
	 */

	private static String createSelectionTag(SubscriberStatus selection,
			String subWavFile, int catType, Integer profileStatus, boolean isFestivalNametune) {

		String response = "";
		if (catType == 13) {
			String[] songs = subWavFile.split(",");
			String configuredTimeSlots = getParamAsString("COMMON",
					"SHUFFLE_TIMESLOT_" + songs.length, null);
			int toTime = -1, fromTime = 0;
			String[] timeSlots = null;
			int time = 0;
			int nextTime = 0;

			if (configuredTimeSlots != null
					&& !configuredTimeSlots.equalsIgnoreCase("null")) {
				timeSlots = configuredTimeSlots.split(",");
				if (timeSlots.length < songs.length) {
					configuredTimeSlots = null;
				} else {
					for (int i = 0; i < timeSlots.length - 1; i++) {
						try {
							time = Integer.parseInt(timeSlots[i]);
							nextTime = Integer.parseInt(timeSlots[i + 1]);
							if (time >= nextTime) {
								configuredTimeSlots = null;
								break;
							}
						} catch (Exception e) {
							configuredTimeSlots = null;
							break;
						}
					}
				}
			}

			if (configuredTimeSlots == null
					|| configuredTimeSlots.equalsIgnoreCase("")) {
				int nInterval = 24 / songs.length;
				for (int i = 0; i < songs.length - 1; i++) {
					toTime += nInterval;
					SubscriberStatus sel = new SubscriberStatusImpl(
							selection.subID(), selection.callerID(),
							selection.categoryID(), selection.subscriberFile()
									+ "|" + selection.categoryID(),
							selection.setTime(), selection.startTime(),
							selection.endTime(), selection.status(),
							selection.classType(), selection.selectedBy(),
							selection.selectionInfo(),
							selection.nextChargingDate(),
							selection.prepaidYes() ? "y" : "n", fromTime,
							toTime, selection.selStatus(),
							selection.deSelectedBy(), selection.oldClassType(),
							selection.categoryType(), selection.loopStatus(),
							selection.selType(), selection.selInterval(),
							selection.refID(), selection.extraInfo(),
							selection.circleId(),null);
					fromTime += nInterval;

					response += createSelectionTag(sel, songs[i] + "|"
							+ selection.categoryID(), profileStatus, isFestivalNametune);
				}
				toTime = 2359;
				SubscriberStatus sel = new SubscriberStatusImpl(
						selection.subID(), selection.callerID(),
						selection.categoryID(), selection.subscriberFile()
								+ "|" + selection.categoryID(),
						selection.setTime(), selection.startTime(),
						selection.endTime(), selection.status(),
						selection.classType(), selection.selectedBy(),
						selection.selectionInfo(),
						selection.nextChargingDate(),
						selection.prepaidYes() ? "y" : "n", fromTime, toTime,
						selection.selStatus(), selection.deSelectedBy(),
						selection.oldClassType(), selection.categoryType(),
						selection.loopStatus(), selection.selType(),
						selection.selInterval(), selection.refID(),
						selection.extraInfo(), selection.circleId(),null);

				response += createSelectionTag(sel, songs[songs.length - 1],
						profileStatus, isFestivalNametune);
			} else {
				timeSlots = configuredTimeSlots.split(",");
				int nextConfiguredTime = 0;
				for (int i = 0; i < songs.length - 1; i++) {
					int configuredTime = Integer.parseInt(timeSlots[i]);
					nextConfiguredTime = Integer.parseInt(timeSlots[i + 1]);
					if (i == 0 && configuredTime > 0) {
						fromTime = 0;
						toTime = configuredTime - 1;
						SubscriberStatus sel = new SubscriberStatusImpl(
								selection.subID(), selection.callerID(),
								selection.categoryID(),
								selection.subscriberFile() + "|"
										+ selection.categoryID(),
								selection.setTime(), selection.startTime(),
								selection.endTime(), selection.status(),
								selection.classType(), selection.selectedBy(),
								selection.selectionInfo(),
								selection.nextChargingDate(),
								selection.prepaidYes() ? "y" : "n", fromTime,
								toTime, selection.selStatus(),
								selection.deSelectedBy(),
								selection.oldClassType(),
								selection.categoryType(),
								selection.loopStatus(), selection.selType(),
								selection.selInterval(), selection.refID(),
								selection.extraInfo(), selection.circleId(),null);

						response += createSelectionTag(sel,
								songs[songs.length - 1], profileStatus, isFestivalNametune);

						fromTime = configuredTime;
						toTime = nextConfiguredTime - 1;
						sel = new SubscriberStatusImpl(selection.subID(),
								selection.callerID(), selection.categoryID(),
								selection.subscriberFile() + "|"
										+ selection.categoryID(),
								selection.setTime(), selection.startTime(),
								selection.endTime(), selection.status(),
								selection.classType(), selection.selectedBy(),
								selection.selectionInfo(),
								selection.nextChargingDate(),
								selection.prepaidYes() ? "y" : "n", fromTime,
								toTime, selection.selStatus(),
								selection.deSelectedBy(),
								selection.oldClassType(),
								selection.categoryType(),
								selection.loopStatus(), selection.selType(),
								selection.selInterval(), selection.refID(),
								selection.extraInfo(), selection.circleId(),null);

						response += createSelectionTag(sel, songs[i] + "|"
								+ selection.categoryID(), profileStatus, isFestivalNametune);
					} else {
						fromTime = configuredTime;
						toTime = nextConfiguredTime - 1;
						SubscriberStatus sel = new SubscriberStatusImpl(
								selection.subID(), selection.callerID(),
								selection.categoryID(),
								selection.subscriberFile() + "|"
										+ selection.categoryID(),
								selection.setTime(), selection.startTime(),
								selection.endTime(), selection.status(),
								selection.classType(), selection.selectedBy(),
								selection.selectionInfo(),
								selection.nextChargingDate(),
								selection.prepaidYes() ? "y" : "n", fromTime,
								toTime, selection.selStatus(),
								selection.deSelectedBy(),
								selection.oldClassType(),
								selection.categoryType(),
								selection.loopStatus(), selection.selType(),
								selection.selInterval(), selection.refID(),
								selection.extraInfo(), selection.circleId(),null);

						response += createSelectionTag(sel, songs[i] + "|"
								+ selection.categoryID(), profileStatus, isFestivalNametune);

					}
				}
				fromTime = nextConfiguredTime;
				toTime = 2359;

				SubscriberStatus sel = new SubscriberStatusImpl(
						selection.subID(), selection.callerID(),
						selection.categoryID(), selection.subscriberFile()
								+ "|" + selection.categoryID(),
						selection.setTime(), selection.startTime(),
						selection.endTime(), selection.status(),
						selection.classType(), selection.selectedBy(),
						selection.selectionInfo(),
						selection.nextChargingDate(),
						selection.prepaidYes() ? "y" : "n", fromTime, toTime,
						selection.selStatus(), selection.deSelectedBy(),
						selection.oldClassType(), selection.categoryType(),
						selection.loopStatus(), selection.selType(),
						selection.selInterval(), selection.refID(),
						selection.extraInfo(), selection.circleId(),null);

				response += createSelectionTag(sel, songs[songs.length - 1],
						profileStatus, isFestivalNametune);
			}
		} else
			return (createSelectionTag(selection, subWavFile, profileStatus, isFestivalNametune));
		logger.info("The response xml is : " + response);
		return response;
	}

	private static String createSelectionTag(SubscriberStatus selection,
			String subWavFile, Integer profileStatus, boolean isFestivalNametune) {
		String callerID = selection.callerID();

		if (callerID == null) {
			callerID = "ALL";
			return createSelectionString(selection, subWavFile, callerID,
					profileStatus, isFestivalNametune);
		} else if (callerID.startsWith("G")) {
			String groupID = callerID.substring(1);
			StringBuilder selectionStr = new StringBuilder();
			if (groupID.length() > 0) {
				GroupMembers[] groupMembers = RBTDBManager.getInstance()
						.getActiveMembersForGroupID(Integer.valueOf(groupID));
				if (groupMembers != null) {
					for (GroupMembers groupMember : groupMembers) {
						selectionStr.append(createSelectionString(selection,
								subWavFile, groupMember.callerID(),
								profileStatus, isFestivalNametune));
					}
				}
			}
			return selectionStr.toString();
		} else {
			//RBT-14624	Signal app - RBT tone play notification feature
			if(callerID.equalsIgnoreCase("PRIVATE")){
				callerID = getParamAsString(DAEMON, "CALLER_ID_FOR_PRIVATE_NUMBER", callerID);
			}
			return createSelectionString(selection, subWavFile, callerID,
					profileStatus, isFestivalNametune);
		}
	}

	private static String getDOWDisplayStr(String selInterval,
			ArrayList<String> tempArrList) {
		String returnStr = null;
		ArrayList<String> tempArr = null;

		if (selInterval != null && (selInterval.indexOf("W") != -1)) {
			selInterval = selInterval.toUpperCase();
			StringTokenizer st = new StringTokenizer(selInterval, ",");
			while (st.hasMoreElements()) {
				String tempStr = st.nextToken();
				if (tempStr != null) {
					tempStr = tempStr.trim();
					if (!tempStr.equalsIgnoreCase("null")
							&& !tempStr.equalsIgnoreCase("")
							&& tempStr.indexOf("W") == 0) {
						if (tempArr == null) {
							tempArr = new ArrayList<String>();
						}
						tempArr.add(tempStr);
					}
				}
			}
		} else {
			Set<String> keySet = dowMap.keySet();
			if (keySet != null) {
				Iterator<String> iter = keySet.iterator();
				while (iter.hasNext()) {
					String tempValue = iter.next();
					if (tempValue != null) {
						if (tempArr == null) {
							tempArr = new ArrayList<String>();
						}
						tempArr.add(tempValue);
					}
				}
			}
		}
		if (tempArr != null && tempArr.size() > 0) {
			Collections.sort(tempArr);
			for (int count = 0; count < tempArr.size(); count++) {
				String tempStr = tempArr.get(count);
				if (dowMap.containsKey(tempStr)) {
					String tempValue = dowMap.get(tempStr);
					if (tempValue != null) {
						if (tempArrList != null) {
							tempArrList.add(tempValue);
						}
						if (returnStr == null) {
							returnStr = tempValue;
						} else {
							returnStr = returnStr + "," + tempValue;
						}
					}
				}
			}
		}
		return returnStr;
	}

	private static String createSelectionString(SubscriberStatus selection,
			String subWavFile, String callerID, Integer profileStatus, boolean isFestivalNametune) {
		StringBuilder selectionTag = new StringBuilder();
		StringBuilder selectionTagBase = new StringBuilder();
		StringBuilder defaultNameTuneSelTagBase = new StringBuilder();

		selectionTagBase.append(" callerID=\"" + callerID + "\"");
		int selStat = selection.status();
		if (selStat == 99) {
			selStat = profileStatus;
		}
		if (selection.categoryType() == 10)
			selStat = 81;
		if (getParamAsBoolean("CALLER_ID_HIGHER_PRIORITY", "FALSE")
				&& !callerID.equals("ALL")) {
			if (selStat == 1)
				selStat = 91;
			if (selStat == 80)
				selStat = 92;
		}
		
		if (isFestivalNametune) {
			    selStat = 85;
		}
		
		selectionTagBase.append(" status=\"" + selStat + "\"");

		//Added for VDE-2730
		boolean isPreRBTForSuspendedSelection=false;
		if (selection.selStatus().equals(STATE_SUSPENDED)) {
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(
					selection.subID());
			HashMap<String, String> extraInfo = dbManager
					.getExtraInfoMap(subscriber);
			String rbtWavFile = getParamAsString(iRBTConstant.COMMON,
					"SUSPEND_PRE_RBT_WAV_FILE", "");
			if (extraInfo != null
					&& extraInfo
							.containsKey(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG)
					&& extraInfo.get(EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG).equalsIgnoreCase("0")) {
				isPreRBTForSuspendedSelection=true;
				if (extraInfo.get(EXTRA_INFO_PRE_RBT_WAV) != null) {
					rbtWavFile = extraInfo.get(EXTRA_INFO_PRE_RBT_WAV)
							.toString();
				} 
				selectionTagBase.append(" pre_rbt_wav=\"").append(
						rbtWavFile + "\"");
			}
		}
		//Ended for VDE-2730

		//Added for ephemeral rbt
		if(isEphemeralRBT(selection.subID(), selection.status(), true)){
			String preRBTwavForEphemeralRBT = getParamAsString(iRBTConstant.DAEMON, "DUMMY_WAV_FILE_FOR_EPHEMERAL_RBT", "abc");
			selectionTagBase.append(" pre_rbt_wav=\"").append(preRBTwavForEphemeralRBT + "\"");
		}else if (RBTParametersUtils.getParamAsBoolean(COMMON,
				"CHARGE_CLASS_BASED_PRE_RBT_ENABLED", "FALSE")
				&& !Utility.isShuffleCategory(selection.categoryType())
				&& selection.loopStatus() != iRBTConstant.LOOP_STATUS_LOOP_INIT
				&& selection.loopStatus() != iRBTConstant.LOOP_STATUS_LOOP
				&& selection.loopStatus() != iRBTConstant.LOOP_STATUS_LOOP_FINAL) {
			Clip clip = rbtCacheManager.getClipByRbtWavFileName(selection
					.subscriberFile());

			String preRBTClip = null;
			if (clip != null) {
				preRBTClip = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(clip.getClassType()).getOperatorCode2();
			}

			if (preRBTClip != null) {
				selectionTagBase.append(" pre_rbt_wav=\"");
				selectionTagBase.append(preRBTClip + "\"");
			}
		}

		if (RBTDeploymentFinder.isBGMSystem()) {
			/* get BGM_PLAY_EMOTICONS=true */
			boolean bgmPlayEmoticons = getParamAsBoolean("BGM_PLAY_EMOTICONS",
					"FALSE");
			if (bgmPlayEmoticons) {
				selectionTagBase.append(" play_emoticons=\"true\"");
			} else {
				selectionTagBase.append(" play_emoticons=\"false\"");
			}
		}
		String selCatID = String.valueOf(selection.categoryID());
		
		//RBT-16197	Tone Player xml changes for passing 1.0 clip name
		logger.info("Going to get rbt 1.0 clip ");
		try {
			IWavFileMappingDAO wavFileMappingDAO = (IWavFileMappingDAO) ConfigUtil
					.getBean(BeanConstant.WAV_FILE_MAPPING_DAO);
			if (selection.circleId() != null && wavFileMappingDAO != null) {
				String operatorName = selection.circleId().split("_")[0];
				String[] wavFilesAndCatId = subWavFile.split(",");
				StringBuilder tempSubWavFile = new StringBuilder(subWavFile.length());
				int count = 0;
				for(String wavFileAndCatID : wavFilesAndCatId) {
					if(count > 0) {
						tempSubWavFile.append(",");
					}
					String[] wavFileWithCatId = wavFileAndCatID.split("\\|");
					String wavFile = wavFileWithCatId[0];
					String catId = wavFileWithCatId[1];
					WavFileMapping wavFileVerOne = wavFileMappingDAO.getWavFileVerOne(wavFile, operatorName);
					if(wavFileVerOne != null) {
						tempSubWavFile.append(wavFileVerOne.getWavFileVerOne()+"|");
					} else {
						tempSubWavFile.append(wavFile+"|");
					}
					tempSubWavFile.append(catId);
					count++;
				}
		
				subWavFile = tempSubWavFile.toString();
			}
		}catch(Exception e){
			logger.info("Exception occured while fetching 1.0 clip wav file: "+e);
		}
		
		logger.info("subWavFile : "+subWavFile);

		if (selection.categoryType() == 15){
			selectionTagBase.append(" wav_file=\"" + subWavFile + "|"
					+ selCatID + "\"");
		} else if (selection.categoryType() == PHONE_RADIO_SHUFFLE) {
			selectionTagBase.append(" wav_file=\"RADIO|" + selCatID + "\"");
		} else if (isFestivalNametune) {
			selectionTagBase.append(" wav_file=\"FESTIVAL_NAMETUNES|" + selCatID + "\"");
		}//Added for VDE-2730
		else if(isPreRBTForSuspendedSelection){
			selectionTagBase.append(" wav_file=\""+getParamAsString(iRBTConstant.COMMON,"SUSPEND_RBT_WAV_FILE", "")+ "\"");
		}//Ended for VDE-2730
		else {
			selectionTagBase.append(" wav_file=\"" + subWavFile + "\"");
		}

		SimpleDateFormat selIntervalFormat = null;
		Date startTime = null;
		String selInterval = selection.selInterval();
		
	
		try {
			if (selInterval != null && selInterval.startsWith("Y")) {
				if (selInterval.length() == 5)
					selIntervalFormat = new SimpleDateFormat("ddMM");
				else
					selIntervalFormat = new SimpleDateFormat("ddMMyyyy");
			}
			if (selIntervalFormat != null && selInterval != null) {
				startTime = selIntervalFormat.parse(selInterval.substring(1));
				if (selInterval.length() == 5){ // future reoccuring selection
					selectionTagBase.append(" sel_type=\"Y\"");
				}
			}
		} catch (ParseException e) {
		}

		synchronized (formatter) {
			// Added by SenthilRaja
			// Override shuffle will not expect selection interval
			if (Utility.isNavCat(selection.categoryType())) {
				Category category = rbtCacheManager.getCategory(selection
						.categoryID());
				if (category != null) {
					startTime = category.getCategoryStartTime();
				} else {
					logger.info("Override shuffle selection category could not find in memcache, will update selection start time for Subsriber: "
							+ selection.subID()
							+ " categoryId: "
							+ selection.categoryID());
				}
			}
			if (startTime == null) {
				startTime = selection.startTime();
			}
								
			//RBT-9999 Added for month based selection
			Date endTime=selection.endTime();
			if (Utility.isMonthBasedInterval(selInterval)) {
				startTime = getMonthBasedTime(startTime,
						selInterval.substring(0, selInterval.indexOf(",")));
				endTime = getMonthBasedTime(endTime,
						selInterval.substring(selInterval.indexOf(",") + 1));
			}
			
			
			selectionTagBase.append(" start_date=\""
					+ formatter.format(startTime) + "\"");
			selectionTagBase.append(" end_date=\""
					+ formatter.format(endTime) + "\"");
			
		}
		
		
			//Added for ephemeral rbt
			if(isEphemeralRBT(selection.subID(), selection.status(), true)){
				selectionTagBase.append(" type=\"X\"");
				Map<String, String> map = DBUtility.getAttributeMapFromXML(selection.extraInfo());
				if(map !=null && map.containsKey("PLAYCOUNT")){
				  String playCount = map.get("PLAYCOUNT");
				  selectionTagBase.append(" counter=\"").append(playCount).append("\"");
				}
		    }else if (isFestivalNametune) {
				selectionTagBase.append(" type=\"FN\"");
			} else if (selection.loopStatus() == LOOP_STATUS_LOOP
					|| selection.loopStatus() == LOOP_STATUS_LOOP_INIT
					|| selection.loopStatus() == LOOP_STATUS_LOOP_FINAL) {
				selectionTagBase.append(" type=\"C\"");
			} else if (selection.categoryType() == 0) {
				if (getParamAsBoolean("COMMON", "PUT_SGS_IN_UGS", "FALSE"))
					selectionTagBase.append(" type=\"C\"");
				else
					selectionTagBase.append(" type=\"S\"");
			} else if (selection.categoryType() == 9)
				selectionTagBase.append(" type=\"W\"");
			else if (selection.categoryType() == 10)
				selectionTagBase.append(" type=\"D\"");
			else if (selection.categoryType() == 12
					|| selection.categoryType() == 20
					|| selection.categoryType() == 22)
				selectionTagBase.append(" type=\"M\"");
			else if (!getParamAsBoolean("DAEMON",
					"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")
					&& selection.categoryType() == ODA_SHUFFLE) {
				if (getParamAsBoolean("COMMON", "PUT_SGS_IN_UGS", "FALSE"))
					selectionTagBase.append(" type=\"C\"");
				else
					selectionTagBase.append(" type=\"S\"");
			} else if (selection.categoryType() == BOX_OFFICE_SHUFFLE
					|| selection.categoryType() == FESTIVAL_SHUFFLE) {
				selectionTagBase.append(" type=\"S\"");
			} else if (selection.categoryType() == PHONE_RADIO_SHUFFLE) {
				selectionTagBase.append(" type=\"R\"");
			} else {
				selectionTagBase.append(" type=\"C\"");
			}

		if (selection.status() == 90)
			selectionTagBase.append(" feedType=\"CRICKET\"");
		if (selection.loopStatus() == LOOP_STATUS_OVERRIDE
				|| selection.loopStatus() == LOOP_STATUS_OVERRIDE_FINAL
				|| selection.loopStatus() == LOOP_STATUS_OVERRIDE_INIT) {
			if (selection.selStatus().equals(STATE_SUSPENDED))
				selectionTagBase.append(" sel_status=\"SUSPEND\"");
		}

		// C implies CORPORATE selections
		if (selection.selType() == TYPE_CORPORATE)
			selectionTagBase.append(" sel_type=\"C\"");
		
		if(Utility.isMonthBasedInterval(selection.selInterval())) {	
			//RBT-9999 Added for month based selection
			if(selectionTagBase.indexOf("sel_type")==-1 ) {
				selectionTagBase.append(" sel_type=\"R\"");
			}
		}
		makeSelectionTagfromTimeToTimeAndDays(selection,selectionTagBase,selectionTag);
		return selectionTag.toString();
	}

	private static void makeSelectionTagfromTimeToTimeAndDays(SubscriberStatus selection,StringBuilder selectionTagBase,StringBuilder selectionTag){
		if (!(selection.fromTime() > selection.toTime())) {
			selectionTagBase.append(" days=\""
					+ getDOWDisplayStr(selection.selInterval(), null) + "\"");
			selectionTagBase.append(" fromTime=\"" + selection.fromTime()
					+ "\"");
			selectionTagBase.append(" toTime=\"" + selection.toTime() + "\"");

			selectionTag.append("<selection");
			selectionTag.append(selectionTagBase.toString());
			selectionTag.append("/>");
		} else {
			ArrayList<String> tempArr = new ArrayList<String>();
			selectionTag.append("<selection");
			selectionTag.append(selectionTagBase.toString());
			selectionTag
					.append(" days=\""
							+ getDOWDisplayStr(selection.selInterval(), tempArr)
							+ "\"");
			selectionTag.append(" fromTime=\"" + selection.fromTime() + "\"");
			selectionTag.append(" toTime=\"" + 23 + "\"");
			selectionTag.append("/>");

			if (tempArr.size() > 1) {
				for (int counter = 1; counter < (tempArr.size() + 1); counter++) {
					if (counter != (tempArr.size())) {
						int currIndex = dowList.indexOf(tempArr.get(counter));
						int lastIndex = dowList.indexOf(tempArr
								.get(counter - 1));
						if (currIndex != (lastIndex + 1)
								&& (currIndex - lastIndex) > 1) {
							selectionTag.append("<selection");
							selectionTag.append(selectionTagBase.toString());
							selectionTag.append(" days=\""
									+ dowList.get(lastIndex + 1) + "\"");
							selectionTag.append(" fromTime=\"" + 0 + "\"");
							selectionTag.append(" toTime=\""
									+ selection.toTime() + "\"");
							selectionTag.append("/>");
						} else if (currIndex == (lastIndex + 1)) {
							selectionTag.append("<selection");
							selectionTag.append(selectionTagBase.toString());
							selectionTag.append(" days=\""
									+ dowList.get(currIndex) + "\"");
							selectionTag.append(" fromTime=\"" + 0 + "\"");
							selectionTag.append(" toTime=\""
									+ selection.toTime() + "\"");
							selectionTag.append("/>");
						}
					} else {
						int currIndex = dowList.indexOf(tempArr
								.get(counter - 1));
						int firstIndex = dowList.indexOf(tempArr.get(0));
						if (currIndex - firstIndex != 6) {
							selectionTag.append("<selection");
							selectionTag.append(selectionTagBase.toString());
							if (currIndex != 6) {
								selectionTag.append(" days=\""
										+ dowList.get(currIndex + 1) + "\"");
							} else {
								selectionTag.append(" days=\"" + dowList.get(0)
										+ "\"");
							}
							selectionTag.append(" fromTime=\"" + 0 + "\"");
							selectionTag.append(" toTime=\""
									+ selection.toTime() + "\"");
							selectionTag.append("/>");
						} else {
							selectionTag.append("<selection");
							selectionTag.append(selectionTagBase.toString());
							selectionTag.append(" days=\"" + dowList.get(0)
									+ "\"");
							selectionTag.append(" fromTime=\"" + 0 + "\"");
							selectionTag.append(" toTime=\""
									+ selection.toTime() + "\"");
							selectionTag.append("/>");
						}
					}
				}
			}

		}

	}
	private static String getSubWavFileToAppened(SubscriberStatus selection,
			String categoryID) {
		if (selection.categoryType() == 0
				|| selection.categoryType() == 9
				|| selection.categoryType() == 10
				|| selection.categoryType() == 12
				|| selection.categoryType() == 13
				|| selection.categoryType() == iRBTConstant.BOX_OFFICE_SHUFFLE
				|| selection.categoryType() == iRBTConstant.OVERRIDE_MONTHLY_SHUFFLE
				|| selection.categoryType() == iRBTConstant.FESTIVAL_SHUFFLE
				|| selection.categoryType() == 11
				|| selection.categoryType() == FESTIVAL_NAMETUNES_SHUFFLE) // shuffle
			return getShuffleClips(selection.categoryID(),selection.categoryType());
		else if (!getParamAsBoolean("DAEMON", "ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")
				&& selection.categoryType() == iRBTConstant.ODA_SHUFFLE) // ODA SHUFFLES
			return getODAShuffleWavFile(selection.categoryID());
		else if (selection.categoryType() == 15) // FEED CATEGORY TYPE
			return selection.subscriberFile() + ">" + selection.categoryID();
		else if (selection.categoryType() == iRBTConstant.FEED_SHUFFLE) // FEED
																		// SHUFFLE
																		// CATEGORY
																		// TYPE-19
			return getFeedShuffleWavFile(selection.categoryID());
		else if (selection.categoryType() == 20) // MONTHLY ODA SHUFFLES
			return getMonthlyODAShuffleWavFile(selection.categoryID());
		else {
			if (categoryID == null)
				return selection.subscriberFile() + "|"
						+ selection.categoryID();
			else
				return selection.subscriberFile() + "|" + categoryID;
		}
	}

	private static String getFeedShuffleWavFile(int catID) {
		Clip[] clips = rbtCacheManager.getActiveClipsInCategory(catID);
		if (clips == null || clips.length == 0)
			return null;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < clips.length; i++) {
			sb.append(clips[i].getClipRbtWavFile()).append(">").append(catID)
					.append("|").append(catID).append(",");
		}
		String clipsStr = sb.toString();
		if (clipsStr.endsWith(",")) {
			clipsStr = clipsStr.substring(0, clipsStr.length() - 1);
		}
		return clipsStr;
	}

	/**
	 * 
	 * @param catID
	 * @return
	 * 
	 *         format : catID>catID|catID
	 */
	private static String getODAShuffleWavFile(int catID) {
		StringBuilder sb = new StringBuilder();
		sb.append(catID).append(">").append(catID).append("|").append(catID);
		return sb.toString();
	}

	/**
	 * @param catID
	 * @return
	 */
	private static String getMonthlyODAShuffleWavFile(int categoryID) {
		Clip[] clips = rbtCacheManager.getActiveClipsInCategory(categoryID);
		if (clips == null || clips.length == 0) {
			clips = rbtCacheManager.getClipsInCategory(categoryID);
		}

		if (clips == null || clips.length == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < clips.length; j++) {
			sb.append(clips[j].getClipId()).append(">").append(categoryID);
			if (j != (clips.length - 1)) {
				sb.append(",");
			}
		}
		sb.append("|").append(categoryID);
		return sb.toString();
	}

	private static String makeHttpRequest(HttpParameters httpParameters,
			HashMap<String, String> requestParams) {
		try {
			HttpResponse httpResponse = rbtHttpClient.makeRequestByPost(
					httpParameters.getUrl(), requestParams, null);
			int statusCode = httpResponse.getResponseCode();
			String response = httpResponse.getResponse();
			logger.info("HTTP response status code: " + statusCode
					+ " Response: " + response);
			return response;
		} catch (Exception t) {
			logger.error(t);
			return null;
		}
	}

	public static boolean writeTrans(String type, String subId,
			String requestTs, String responseTs, String requestDetail,
			String respDetail, String ipAddress) {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("EVENT_TYPE", type);
		h.put("SUBSCRIBERID", subId);
		h.put("REQUESTED_TIMESTAMP", requestTs);
		h.put("RESPONSE_DELAYINMS", responseTs);
		h.put("REQUEST_DETAIL", requestDetail);
		h.put("RESPONSE_DETAIL", respDetail);
		h.put("IP_ADDRESS", ipAddress);

		if (m_writeTrans != null) {
			m_writeTrans.writeTrans(h);
			return true;
		}
		return false;
	}

	private static String getParamAsString(String type, String param,
			String defaultVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal)
					.getValue();
		} catch (Exception e) {
			logger.error("Unable to get param: " + param + " type: " + type);
			return defaultVal;
		}
	}

	public static int getParamAsInt(String param, int defaultVal) {
		try {
			String paramVal = m_rbtParamCacheManager.getParameter("DAEMON",
					param, defaultVal + "").getValue();
			return Integer.valueOf(paramVal);
		} catch (Exception e) {
			logger.error("Unable to get param: " + param + " type: DAEMON");
			return defaultVal;
		}
	}

	private static boolean getParamAsBoolean(String param, String defaultVal) {
		return getParamAsBoolean("DAEMON", param, defaultVal);
	}

	private static boolean getParamAsBoolean(String type, String param,
			String defaultVal) {
		try {
			return m_rbtParamCacheManager.getParameter(type, param, defaultVal)
					.getValue().equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.error("Unable to get param: " + param + " type: " + type);
			return "TRUE".equalsIgnoreCase(defaultVal);
		}
	}

	/******************
	 * @author Sreekar BSNL AD RBT changes
	 */

	/**
	 * Makes Http request to the 3rd party activation URL specified by BSNL
	 * 
	 * @param task
	 */
	private static void activate3rdPartyAdRbt(String subID, String actBy,
			HashMap<String, String> extraInfoMap) {
		// String activationUrl = m_rbtParamCacheManager.getParameter(DAEMON,
		// ADRBT_ACT_URL, null).getValue();
		String activationUrl = getParamAsString(DAEMON, ADRBT_ACT_URL, null);
		if (activationUrl == null)
			return;
		int connTimeout = 500;
		int socketTimeout = 6000;
		StringTokenizer stk = new StringTokenizer(activationUrl, ":");
		if (stk.hasMoreTokens()) {
			try {
				connTimeout = Integer.parseInt(stk.nextToken());
			} catch (Exception e) {
			}
		}
		if (stk.hasMoreTokens()) {
			try {
				socketTimeout = Integer.parseInt(stk.nextToken());
			} catch (Exception e) {
			}
		}
		if (actBy != null && actBy.indexOf(":") != -1)
			actBy = actBy.substring(0, actBy.indexOf(":"));
		String adRbtTransKey = (extraInfoMap != null
				&& extraInfoMap.containsKey(EXTRA_INFO_ADRBT_TRANS_ID) ? extraInfoMap
				.get(EXTRA_INFO_ADRBT_TRANS_ID) : "");
		activationUrl = activationUrl.replaceAll("%MSISDN", subID);
		activationUrl = activationUrl.replaceAll("%MODE", actBy);
		activationUrl = activationUrl.replaceAll("%TRANSKEY", adRbtTransKey);

		HttpParameters httpParameters = new HttpParameters(activationUrl,
				false, null, -1, connTimeout, socketTimeout);
		make3rdPartyAdRbtRequest(httpParameters, "ADRBT_ACT", subID);
	}

	/**
	 * Makes Http request to the 3rd party deactivation URL specified by BSNL
	 * 
	 * @param task
	 */
	private static void deactivate3rdPartyAdRbt(String subID, String deactBy) {
		String deactivationUrl = getParamAsString(DAEMON, ADRBT_DEACT_URL, null);
		if (deactivationUrl == null)
			return;

		if (deactBy == null)
			deactBy = "ADRBT";

		int connTimeout = 500;
		int socketTimeout = 6000;
		StringTokenizer stk = new StringTokenizer(deactivationUrl, ":");
		if (stk.hasMoreTokens()) {
			try {
				connTimeout = Integer.parseInt(stk.nextToken());
			} catch (Exception e) {
			}
		}
		if (stk.hasMoreTokens()) {
			try {
				socketTimeout = Integer.parseInt(stk.nextToken());
			} catch (Exception e) {
			}
		}
		if (deactBy.indexOf(":") != -1)
			deactBy = deactBy.substring(0, deactBy.indexOf(":"));
		deactivationUrl = deactivationUrl.replaceAll("%MSISDN", subID);
		deactivationUrl = deactivationUrl.replaceAll("%MODE", deactBy);

		HttpParameters httpParameters = new HttpParameters(deactivationUrl,
				false, null, -1, connTimeout, socketTimeout);
		make3rdPartyAdRbtRequest(httpParameters, "ADRBT_DEACT", subID);
	}

	private static void make3rdPartyAdRbtRequest(HttpParameters httpParameters,
			String requestType, String subID) {
		HttpResponse httpResponse = null;
		long startTime = System.currentTimeMillis();
		long endTime = startTime;
		String ipAddress = null;
		try {
			httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("HTTP url: " + httpParameters.getUrl() + " Response: "
					+ httpResponse.getResponse());
			endTime = System.currentTimeMillis();
			URL url = new URL(httpParameters.getUrl());
			ipAddress = url.getHost();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			SimpleDateFormat sdf = new SimpleDateFormat(logDateFormatterString);
			writeTrans(requestType, subID, sdf.format(new Date(startTime)), ""
					+ (endTime - startTime), httpParameters.getUrl(),
					(httpResponse != null ? httpResponse.getResponse() : ""), ipAddress);
		}
	}

	public static boolean checkProcessingHash(Object obj) {
		if (obj == null) {
			return false;
		}
		Subscriber sub = (Subscriber) obj;
		String subID = sub.subID();
		if (m_processingHash.containsKey(subID)) {
			return true;
		}
		m_processingHash.put(subID, subID);
		return false;
	}

	public static void removeFromProcessingHash(Object obj) {
		if (obj == null) {
			return;
		}
		Subscriber sub = (Subscriber) obj;
		m_processingHash.remove(sub.subID());
	}

	private void initializeADRBTEventLogger() {
		try {
			String adrbtEventLoggingDir = getParamAsString(COMMON,
					"ADRBT_EVENT_LOGGING_DIR", null);
			if (adrbtEventLoggingDir != null) {
				new File(adrbtEventLoggingDir).mkdirs();
				Configuration cfg = new Configuration(adrbtEventLoggingDir);
				adrbtEventLogger = new ADRBTEventLogger(cfg);
				logger.info("Parameter name: ADRBT_EVENT_LOGGING_DIR value: "
						+ adrbtEventLoggingDir);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public static void adrbtLog(String msisdn, Date timestamp, int transType) {
		try {
			switch (transType) {
			case ADRBT_TRANS_TYPE_ADRBT_ACT_NEW_USER:
				adrbtEventLogger.NewSubAdRbtAct(msisdn, timestamp);
				break;
			case ADRBT_TRANS_TYPE_ADRBT_ACT_RBT_USER:
				adrbtEventLogger.RbtSubAdRbtAct(msisdn, timestamp);
				break;
			case ADRBT_TRANS_TYPE_RBT_ACT_ADRBT_USER:
				adrbtEventLogger.AdRbtSubRbtAct(msisdn, timestamp);
				break;
			case ADRBT_TRANS_TYPE_ADRBT_DEACT_ADRBT_USER:
				adrbtEventLogger.AdRbtDct(msisdn, timestamp);
				break;
			case ADRBT_TRANS_TYPE_ADRBT_DEACT_RBTnADRBT_USER:
				adrbtEventLogger.AdRbtDctRbtSub(msisdn, timestamp);
				break;
			case ADRBT_TRANS_TYPE_RBT_DEACT_RBTnADRBT_USER:
				adrbtEventLogger.RbtDctAdRbtSub(msisdn, timestamp);
				break;
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private static String replacePrefixInSubId(String subId) {
		if (oldNnewPrefixMap.size() <= 0) {
			return subId;
		}
		String result = subId;
		Set<Entry<String, String>> entrySet = oldNnewPrefixMap.entrySet();
		Iterator<Entry<String, String>> entrySetIterator = entrySet.iterator();
		while (entrySetIterator.hasNext()) {
			Entry<String, String> entry = entrySetIterator.next();
			if (subId.startsWith(entry.getKey())) {
				String newPrefix = entry.getValue();
				String subIdWithOutPrefix = subId.substring(entry.getKey()
						.length());
				result = newPrefix + subIdWithOutPrefix;
			}
		}
		return result;
	}

	// This for test Player Daemon
	@Deprecated
	public static void testPlayerDaemon(RBTDaemonManager rbtDaemonManager) {
		new RBTPlayerUpdateDaemon(rbtDaemonManager).start();
	}
	
	public static void main(String[] args) {
		RBTDaemonManager rbtDaemonManager = new RBTDaemonManager();
		new RBTPlayerUpdateDaemon(rbtDaemonManager).start();

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber("9742200153");
		SitePrefix sp = new SitePrefix("bangalore","bangalore","9900","http://localhost:9896","y","eng","http://localhost","b");
//		System.out.println(" subscriber: "+subscriber.subYes());
		HashMap<Character, ArrayList<String>> statusMap = new HashMap<Character, ArrayList<String>>();
		//System.exit(0);
		System.out.println(" XML : "+createSubscriberXML(subscriber, 0, statusMap, sp));
		System.exit(0);
		
		
	}


	
	/**
	 * 	Added for RBT-9999
	 * @param date
	 * @param interval
	 * Creating month based selection 
	 * 
	 * * @return  new formatted Date 
	 */
	public static Date getMonthBasedTime(Date date, String interval) {

		logger.debug("Creating month based date and interval:" + date + " "
				+ interval);
		Date returnDate = date;
		try {
			Calendar cDate = Calendar.getInstance();
			cDate.setTime(date);

			int day = Integer
					.parseInt(interval.substring(interval.length() - 2));
			int month = 0;

			if (interval.length() == 5) {
				month = Integer.parseInt(interval.substring(1, 3));
			} else if (interval.length() == 4) {
				month = Integer.parseInt(interval.substring(1, 2));
			}
			cDate.set(Calendar.MONTH, month - 1);
			cDate.set(Calendar.DATE, day);
			returnDate = cDate.getTime();

		} catch (Exception e) {
			logger.info("Exception occured in getMonthBasedTime :" + e);
		}
		logger.info("Returning date for date and interval:" + date + " and "
				+ interval + " is returnDate: " + returnDate);
		return returnDate;
	}
	
	private static StringBuffer appendAppName(RBTLoginUser user, StringBuffer sb) {
		String tillNowString = sb.toString();
		String type = "SIGNAL";
		if(user != null ) {			
			type = user.type();
			type = Utility.getAppNameFromType(type);
			if(type == null) {
				return sb;
			}
		}
		tillNowString = tillNowString.replace(
			"<selections",
			"<selections appName=\""
					+ type
					+ "\"");
		return new StringBuffer(tillNowString);
	}
	
	//Added for ephemeralRBT
	private static boolean isEphemeralRBT(String msisdn, int status, boolean isSelection) {
		boolean isEphemeralRBT = false;
		if(isSelection && status == 200){
			isEphemeralRBT = true;
		}
		if (!isSelection) {
			try {
				SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil
						.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
				ExtendedSubStatus extendedSubStatus = new ExtendedSubStatus();
				extendedSubStatus.setSubId(msisdn);
				extendedSubStatus.setStatus(200);
				extendedSubStatus.setEndTime(new Date());
				List<ExtendedSubStatus> activeEphemeralSelections = subscriberSelection
						.getAllSelectionsByRestrictions(extendedSubStatus);
				if (activeEphemeralSelections != null
						&& activeEphemeralSelections.size() > 0) {
					isEphemeralRBT = true;
				}
			} catch (Exception e) {
				logger.info("Exception occured while getting ephemeral selection : "
						+ e);
			}
		}
		return isEphemeralRBT;
	}
	private static Map<String, String> getParamAsMap(String type, String param,
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
	
	
}