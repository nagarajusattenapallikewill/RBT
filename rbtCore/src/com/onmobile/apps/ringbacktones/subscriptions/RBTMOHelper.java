package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTLuceneSearch;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ChargeClassMap;
import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.PromoMaster;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.smsgateway.accounting.Accounting;

public class RBTMOHelper extends Thread implements iRBTConstant,
		iRBTMOConstants {
	private static Logger logger = Logger.getLogger(RBTMOHelper.class);

	boolean m_langFilter = false;
	boolean m_checkClipSMSAlias = false;
	boolean m_mgmOn = false;
	boolean m_useSubscriptionManager = false;
	boolean m_allowReactivation = ALLOW_REACTIVATION;
	boolean m_delSelections = m_delSelectionsDefault;
	boolean m_isActOptional = false;
	boolean m_isActOptionalRequestRBT = false;
	boolean m_isActOptionalCopy = false;
	boolean m_cricketPass = false;
	String m_defaultCricketPass = "DP";
	boolean m_corpChangeSelectionBlock = false;
	boolean m_sdrBillingOn = true;
	boolean m_removeLeadingZero = false;
	boolean m_retReqResponse = false;
	boolean m_actAllowed = true;
	boolean m_reqNoMatchDispTop = false;
	boolean m_ctOneClassTypeValid = false;
	boolean m_processBlackListTypes = false;
	private boolean m_addSMSSELToLoop = false;
	private boolean m_addMovieRequest = false;
	private boolean m_doTodCheck = true;
	boolean m_sendSMS = m_sendSMSDefault;
	boolean m_putPageNoStart = m_putPageNoStartDefault;
	
	public boolean m_giftNational = m_giftNationalDefault;
	private boolean m_isViralClipAllowed = m_isViralClipAllowedDefault;
	private boolean m_insertSearchNumberAtEnd = true;;
	private boolean bInitialized = false;
	private boolean bUpdateOperatorPrefix = false;
	private boolean bUpdateOperatorPrefixSite = false;
	public boolean isMemCacheModel = false;
	private boolean giveUGSsongList = false;
	private boolean allowRemovalOfNullCallerIDSelection = false;
	int maxCallerIDSelectionsAllowed = 0;
	String m_defaultClip = null;
	public String[] m_songCatcherNumberList = null;
	boolean m_contentRefreshed = false;
	private boolean bConfirmDeactivation = false;
	boolean m_corpBlockSongChangeSomeSongs = false;
	private Date m_endDate = null;
	public String m_SongCatcherClassType = "DEFAULT";
	private boolean m_checkForSuspendedSelection = false;
	String  m_onlySearchShortCode = null;
	String m_smsTextForAll = "All";
	int m_minValuePromoId  = 0;

	Date m_last_lucene_update_date = null;
	Date m_next_update_operator_prefix_date = null;

	private static WriteDailyTrans m_smsTrans = null;
	
	int m_categoryID = 99;
	int m_mgmMinNoDaysActive = 7;
	int m_mgmMaxGiftsMonth = 2;
	boolean bDontUpdateLucene = false;
	int m_catMaxSMS = 5;
	int m_activationPeriod = m_activationPeriodDefault;
	int m_cricketInterval = 0;
	int m_sdrSize = 1000;
	int m_reqNoMatchDispMax = m_reqNoMatchDispMaxDefault;
	int m_reqMaxSMS = m_reqMaxSMSDefault;
	int m_reqMaxSMSCat = m_reqMaxSMSCatDefault;
	int m_maxClipSearched = m_maxClipSearchedDefault;
	int m_phoneNumberLength = m_phoneNumberLengthDefault;
	int m_operator_prefix_update_interval_hrs = 24;
	int m_minDaysDeactivationConfirm = 5;
	int m_deactivationConfirmClearDays = 1;
    int m_waitTimeDoubleConfirmation = 30;

	long m_sdrInterval = 24;

	// Common Texts

	private static String _class = "RBTMOHelper";
	String m_strHELP = "help";
	public int m_nConn = 4;
	public String m_smsNo = null;
	String m_countryPrefix = "91";
	String m_reqDefLang = "hindi";
	String m_mgmActBy = "MGM";
	String m_remoteURL = null;
	String m_messagePathDefault = null;
	String m_messagePath = m_messagePathDefault;
	String m_promoActBy;
	String m_promotion1Default = null;
	String m_promotion1 = m_promotion1Default;
	String m_songPromotion2Default = null;
	String m_songPromotion2 = m_songPromotion2Default;
	String m_profileCorporateCategories = "99";
	String m_retActByDefault = "RET";
	String m_retActBy = m_retActByDefault;
	String m_sdrWorkingDir = ".";
	String m_sdrRotation = "size";
	String m_songPromotion1Default = null;
	String m_songPromotion1 = m_songPromotion1Default;
	String m_promotion1ValidFormat = null;
	String m_promotion1InvalidFormat = null;
	String m_viralKey = null;
	String m_promotion2 = null;
//	String m_defaultLanguage = "eng";
	String m_globalDefaultLanguage = "eng";
	String m_chargingCycle = null;
	String m_subscriptionType = null;
	String m_UGCsubscriptionType = "OPTIN";
	String m_model = null;

	ArrayList m_validIP = null;
	ArrayList m_validServerIP = null;
	ArrayList m_validPrepaidIP = null;
	ArrayList m_rbtKeyword = null;
	ArrayList m_rrbtKeyword = null;
	ArrayList m_subMsg = null;
	ArrayList m_unsubMsg = null;
	ArrayList m_newsAndBeautyFeedKeyword = null;
//	ArrayList m_supportedLang = null;
	ArrayList m_remotePrefix = null;
	ArrayList m_requestRBTkeyword = null;
	ArrayList m_requestMoreKeyword = null;
	ArrayList m_DayProfileList = null;
	ArrayList m_catRBTkeyword = null;
	ArrayList m_cricketSubKey = null;
	ArrayList m_yearlySubscription = null;
	ArrayList m_profileList = new ArrayList();
	ArrayList m_ctOneKeyword = null;
	ArrayList m_topClipsListingKeywords = null;
	ArrayList m_topCategoriesListingKeywords = null;
	ArrayList m_deactivateDownloadKeyword = null;
	ArrayList m_TrialWithActivations = new ArrayList();
	Hashtable m_trialClassDaysMap = new Hashtable();
	Hashtable m_params = null;
	HashMap m_chargemap = null;
	Map m_rbtSubKeyClassMap = null;
	HashMap m_requestRBTsearchOn = new HashMap();
	
	SubscriptionClass[] m_subClasses;

	String COPYCONFIRM = "COPYCONFIRM";
	String COPYCONFIRMED = "COPYCONFIRMED";
    String COPYCONFPENDING = "COPYCONFPENDING";
    
	private static RBTMOHelper m_rbtMOHelper = null;
	public static RBTLuceneSearch rbtClipsLucene = null;
	public static SMSProcessInterface m_preProcessImpl = null;
	public static Accounting m_rbtAccounting = null;
	public static Accounting m_rbtHsbAccounting = null;
	private Hashtable m_profileClips = null;
	private Hashtable m_smsTable = null;
	private Hashtable m_featureNameKeywordTable = new Hashtable();
	private ArrayList m_featureNameArrayList = new ArrayList();
	private FeedStatus m_feedStatus = null;
	private static Object m_init = new Object();
	private static Object m_initMO = new Object();
	private static Object m_reInit = new Object();
	private static Object m_initObject = new Object();
	private Hashtable m_ChargingModelKeywordMap = new Hashtable();
	private Hashtable m_SubscriptionModelKeywordMap = new Hashtable();
	private Hashtable m_promoCodeKeywordMap = new Hashtable();
	private boolean m_allowInactUserGift = false;
	public RBTDBManager m_rbtDBManager = null;
	private Thread m_thread_update_cache;
	private HashMap m_operatorPrefix = new HashMap();
	public SitePrefix localSitePrefix = null;
	private Hashtable conversionTableChargeClass = new Hashtable();
	private HashMap m_circle_id = new HashMap();
	private String m_profileSubClass = null;
	private String m_invalidProfileRreqestSMS = "This is invalid keyword request";
	private static boolean m_isTataGSMImpl=false;
	private static boolean m_makeWDSRequest = false;
	private static boolean m_useWDSMap=false;
	private static SimpleDateFormat expiryDateFormat = null;
	//private static SimpleDateFormat dummyDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss zz yyyy");
	private static SimpleDateFormat dummyDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	public static RBTMOHelper init() {
		if (m_rbtMOHelper == null) {
			synchronized (m_initMO) {
				if (m_rbtMOHelper == null) {
					try {
						String rbtMOImpl = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MO_IMPL_NAME", null);
						if (rbtMOImpl == null) {
							m_rbtMOHelper = new RBTMOHelper();
						} else {
							Class implClass = Class.forName(rbtMOImpl);
							m_rbtMOHelper = (RBTMOHelper) implClass
									.newInstance();

						}
					} catch (Exception e) {
						logger.error("", e);
						m_rbtMOHelper = null;
					}
				}
			}
		}
		return m_rbtMOHelper;
	}

	RBTMOHelper() throws Exception {
		Tools.init("RBT_WAR", false);

		m_nConn = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "NUM_CONN", 4);

		m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");

		m_rbtDBManager = RBTDBManager.getInstance();

		if (m_rbtDBManager == null)
			throw new Exception();

		bUpdateOperatorPrefixSite = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "UPDATE_OPERATOR_PREFIX", "FALSE");
		if (bUpdateOperatorPrefixSite) {
			updateOperatorPrefixes();
			updateCircleID();
		}

		m_rbtKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_KEYWORD", null), null);
		m_rrbtKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REV_RBT_KEYWORD", null), null);
		m_remoteURL = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REMOTE_URL", null);
		m_profileSubClass = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROFILE_SUB_CLASS", null);

		String tempSMS = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "INVALID_PROFILE_REQUEST_SMS", null);
		if (tempSMS != null && tempSMS.length() > 0) {
			m_invalidProfileRreqestSMS = tempSMS;
		}
		m_promoActBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROMO_ID_ACTIVATED_BY", null);
		m_validIP = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_IP", null), null);
		m_validServerIP = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_SERVER_IP", null), null);
		m_validPrepaidIP = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_PREPAID_IP", null), null);
		m_remotePrefix = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REMOTE_PREFIX", null), null);

		m_subMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "ACTIVATION_KEYWORD", null), null);

		ArrayList rbtPromoActKey = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_ACT_KEY", null), ";");

		m_subClasses = getSubscriptionClasses();
		if (m_subClasses == null)
			throw new Exception("RBT::Exception in SubClasses not found");
		if (rbtPromoActKey != null) {
			String rbtPromoActSubClass = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RBT_ACT_SUB_CLASS", null);
			if (rbtPromoActSubClass != null) {
				List rbtPromoActSubClassList = new ArrayList();
				if (rbtPromoActSubClass != null) {
					StringTokenizer token = new StringTokenizer(
							rbtPromoActSubClass, ",");
					while (token.hasMoreTokens()) {
						String tmpToken = token.nextToken();
						String tokenToAdd = null;
						for (int i = 0; i < m_subClasses.length; i++) {
							if (tmpToken.equals(m_subClasses[i]
									.getSubscriptionClass())) {
								tokenToAdd = tmpToken.toUpperCase();
								break;
							}
						}
						rbtPromoActSubClassList.add(tokenToAdd);
					}
					m_rbtSubKeyClassMap = new HashMap();
					Iterator promoActKeyIter = rbtPromoActKey.iterator();
					Iterator promoActSubClassIter = rbtPromoActSubClassList
							.iterator();
					while (promoActKeyIter.hasNext()) {
						String[] promoActKeys = ((String) promoActKeyIter
								.next()).split(",");
						if (promoActSubClassIter.hasNext()) {
							String subClassId = (String) promoActSubClassIter
									.next();
							if (subClassId != null) {
								subClassId = subClassId.toUpperCase();
								for (int index = 0; index < promoActKeys.length; index++)
									m_rbtSubKeyClassMap.put(promoActKeys[index]
											.trim(), subClassId.trim());
							}
						}
					}
				}
			}
		}
		m_DayProfileList = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROFILE_DAYS_ALIAS", null), null);

		m_model = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_IMPLEMENTATION", "NormalSMSImpl");
		Class impl = null;
		try {
			impl = Class
					.forName("com.onmobile.apps.ringbacktones.subscriptions."
							+ m_model);
		} catch (Exception e) {
			impl = Class
					.forName("com.onmobile.apps.ringbacktones.subscriptions.NormalSMSImpl");
		}

		m_preProcessImpl = (SMSProcessInterface) impl.newInstance();
		m_preProcessImpl = (SMSProcessInterface) m_preProcessImpl.getInstance();
		if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_SEARCH_ON_MAP", null) == null
				|| RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_SEARCH_ON_MAP", null).length() == 0) {
			m_requestRBTsearchOn.put("song","song");
			m_requestRBTsearchOn.put("movie","movie");
			m_requestRBTsearchOn.put("singer","singer");
		}
		else
		{
			StringTokenizer stkParent = new StringTokenizer(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_SEARCH_ON_MAP", null), ";");
			while (stkParent.hasMoreTokens())
			{
				StringTokenizer stkSingleElement = new StringTokenizer(stkParent.nextToken(),":");
				String searchOn = stkSingleElement.nextToken();
				if(stkSingleElement.hasMoreTokens())
				{
					StringTokenizer searchOnKeywords = new StringTokenizer(stkSingleElement.nextToken(),",");
					while(searchOnKeywords.hasMoreTokens())
						m_requestRBTsearchOn.put(searchOnKeywords.nextToken(), searchOn);
				}	
			}	
		}	
		
		
		m_processBlackListTypes = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_BLACKLIST_TYPE", "FALSE");

		// FEATURE Hashtable entries start
		ArrayList helpKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "HELP_KEYWORD", null), null);
		addToFeatureTable("HELP", helpKeywords);

		String reqRes = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RETAILER_REQ_RESPONSE", null);
		String retReqAcceptKeyword = null;
		if (reqRes != null) {
			StringTokenizer stk = new StringTokenizer(reqRes, ",");
			if (stk.hasMoreTokens())
				m_retReqResponse = stk.nextToken().equalsIgnoreCase("TRUE");
			if (stk.hasMoreTokens())
				retReqAcceptKeyword = stk.nextToken();
			addToFeatureTable("ACCEPT_RETAILER", retReqAcceptKeyword);
		}

		// The keywords for REQUEST_MORE has to come in the feature hashtable
		// before the keywords for CATEGORY_SEARCH.
		if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_MORE_KEYWORD", null) == null
				|| RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_MORE_KEYWORD", null).length() == 0) {
			m_requestMoreKeyword = new ArrayList();
			m_requestMoreKeyword.add("more");
		} else
			m_requestMoreKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_MORE_KEYWORD", null), null);
		
		addToFeatureTable("REQUEST_MORE", m_requestMoreKeyword);

		if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CATEGORY_SEARCH_KEYWORD", null) == null
				|| RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CATEGORY_SEARCH_KEYWORD", null).length() == 0) {
			m_catRBTkeyword = new ArrayList();
			m_catRBTkeyword.add("cat");
		} else
			m_catRBTkeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CATEGORY_SEARCH_KEYWORD", null), null);
		addToFeatureTable("CATEGORY_SEARCH", m_catRBTkeyword);

		
		if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_RBT_KEYWORD", null) == null
				|| RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_RBT_KEYWORD", null).length() == 0) {
			m_requestRBTkeyword = new ArrayList();
			m_requestRBTkeyword.add("req");
		} else
			m_requestRBTkeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_RBT_KEYWORD", null), null);
		addToFeatureTable("REQUEST", m_requestRBTkeyword);

		m_unsubMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEACTIVATION_KEYWORD", null), null);
		addToFeatureTable("DEACTIVATION", m_unsubMsg);
		
		m_newsAndBeautyFeedKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "NEWS_AND_BEAUTY_FEED_KEYWORD", null), null);
		addToFeatureTable("NEWS_BEAUTY_FEED", m_newsAndBeautyFeedKeyword);

		ArrayList tnbMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TNB_KEYWORDS", null), null);
		addToFeatureTable("TNB", tnbMsg);
		
		ArrayList rmvCallerIDMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RMVCALLERID_KEYWORD", null), null);
		addToFeatureTable("REMOVE_SELECTION", rmvCallerIDMsg);
		
		ArrayList deactDownloadKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEACT_DOWNLOAD_KEYWORD", null), null);
		addToFeatureTable("DEACT_DOWNLOAD", deactDownloadKeyword);

		ArrayList tempCanMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TEMPORARY_OVERRIDE_CANCEL_MESSAGE", null), null);
		addToFeatureTable("REMOVE_PROFILE", tempCanMsg);

		ArrayList weeklyToMonthlyConversion = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEEKLY_TO_MONTHLY_CONVERSION", null), null);
		addToFeatureTable("WEEKLY_TO_MONTHLY_CONVERSION",
				weeklyToMonthlyConversion);

		ArrayList giftKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "GIFT_KEYWORD", null), null);
		addToFeatureTable("GIFT", giftKeyword);

		ArrayList pollON = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "POLLON_KEYWORD", null), null);
		addToFeatureTable("POLLON", pollON);

		ArrayList pollOFF = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "POLLOFF_KEYWORD", null), null);
		addToFeatureTable("POLLOFF", pollOFF);
		
		ArrayList cancelCopy = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CANCELCOPY_KEYWORD", null), null);
		addToFeatureTable("CANCEL_COPY",cancelCopy);

		ArrayList confirmCopy = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "COPY_CONFIRM_KEYWORD", null), null);
		addToFeatureTable("CONFIRM_COPY",confirmCopy);
		
		ArrayList cancelOptInCopy = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "COPY_CANCEL_KEYWORD", null), null);
		addToFeatureTable("CANCEL_OPT_IN_COPY",cancelOptInCopy);
		
		ArrayList copyMsg = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "COPY_KEYWORDS", null), null);
		addToFeatureTable("COPY", copyMsg);

		m_promotion1 = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROMOTION1", null);
		addToFeatureTable("PROMOTION1", firstToken(m_promotion1));

		m_promotion2 = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROMOTION2", null);
		addToFeatureTable("PROMOTION2", firstToken(m_promotion2));

		m_songPromotion1 = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SONG_PROMOTION1", null);
		addToFeatureTable("SONG_PROMOTION1", firstToken(m_songPromotion1));

		m_songPromotion2 = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SONG_PROMOTION2", null);
		addToFeatureTable("SONG_PROMOTION2", firstToken(m_songPromotion2));

        m_viralKey = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VIRAL_KEYWORD", null);
        addToFeatureTable("ACCEPT_VIRAL", firstToken(m_viralKey));

		String mgmAcceptKeyword = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "MGM_ACCEPT_KEY", null);
		addToFeatureTable("ACCEPT_MGM", mgmAcceptKeyword);

		ArrayList ringRenewKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RENEW_KEYWORD", null), null);
		addToFeatureTable("RENEW", ringRenewKeyword);
		ArrayList listProfileKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "LIST_PROFILE_KEYWORD", null), null);
		addToFeatureTable("LISTPROFILE", listProfileKeywords);

		String retailerActPromo = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACT_PROMO_PREFIX", null);
		addToFeatureTable("RETAILER2", retailerActPromo);

		ArrayList dNavArray = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "NAV_DEACT_KEYWORD", "DNAV").toLowerCase(), null);
		addToFeatureTable("DNAV", dNavArray);
		ArrayList selectionKeywords1 = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SEL_KEYWORD1", null), null);
		addToFeatureTable("SET_SELECTION1", selectionKeywords1);
		ArrayList selectionKeywords2 = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SEL_KEYWORD2", null), null);
		addToFeatureTable("SET_SELECTION2", selectionKeywords2);
		ArrayList ListenKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "LISTEN_KEYWORD", null), null);
		addToFeatureTable("LISTEN", ListenKeywords);
		ArrayList songCatcherAcceptKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SONG_CATCHER_ACCEPT_KEYWORD", null), null);
		addToFeatureTable("SONG_CATCHER_ACCEPT", songCatcherAcceptKeywords);

		ArrayList setNewsLetterOnKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SET_NEWSLETTER_ON_KEYWORDS", null), null);
		addToFeatureTable("SET_NEWSLETTER_ON", setNewsLetterOnKeywords);
	
		ArrayList setNewsLetterOffKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SET_NEWSLETTER_OFF_KEYWORDS", null), null);
		addToFeatureTable("SET_NEWSLETTER_OFF", setNewsLetterOffKeywords);



		m_topClipsListingKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TOP_CLIPS_KEYWORD", "TOP,TOP10"), null);
		m_topCategoriesListingKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TOP_CATEGORIES_KEYWORD", "TOP,TOP10"), null);
		ArrayList manageKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "MANAGE_KEYWORD", null), null);
		addToFeatureTable("MANAGE", manageKeywords);
		initTrialWithActivations();
		ArrayList disableIntroPromptList = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DISABLE_INTRO_PROMPT_KEYWORD", null), null);
		addToFeatureTable("DISABLE_INTRO", disableIntroPromptList);

		ArrayList disableOverlayList = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DISABLE_OVERLAY_KEYWORD", null), null);
		addToFeatureTable("DISABLE_OVERLAY", disableOverlayList);

		ArrayList enableOverlayList = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "ENABLE_OVERLAY_KEYWORD", null), null);
		addToFeatureTable("ENABLE_OVERLAY", enableOverlayList);
		
		ArrayList m_downloadsListKeywords = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DOWNLOADS_LIST_KEYWORD", null), null);
		addToFeatureTable("DOWNLOADSLIST", m_downloadsListKeywords);

		addToFeatureTable("SONGOFMONTH", "ssrm");

		m_smsNo = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_NO", null);
		m_insertSearchNumberAtEnd = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "INSERT_SEARCH_NUMBER_AT_END", "TRUE");
		m_isViralClipAllowed = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "IS_VIRAL_CLIP_ALLOWED", "TRUE");
		m_messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);
		m_activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);
		m_putPageNoStart = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "PUT_PAGE_NO_AT_START", "FALSE");
		m_sendSMS = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SEND_SMS", "FALSE");
		m_isActOptional = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "IS_ACT_OPTIONAL", "FALSE");
		m_isActOptionalRequestRBT = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "IS_ACT_OPTIONAL_REQUEST_RBT", "FALSE");
		m_isActOptionalCopy = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "IS_ACT_OPTIONAL_COPY_RBT", "FALSE");
		m_corpChangeSelectionBlock = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE");
		m_cricketPass = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CRICKET_PASS", "FALSE");
		String temp = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_CRICKET_PASS", null);
		if(temp != null)
			m_defaultCricketPass = temp;
		m_cricketInterval = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "CRICKET_INTERVAL", 2);
		m_sdrWorkingDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SDR_WORKING_DIR", ".");

		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("REQUEST PARAMS");
		headers.add("RESPONSE");
		headers.add("TIME DELAY");
		headers.add("REQ IP");
		m_smsTrans = new WriteDailyTrans(m_sdrWorkingDir, "SMS_REQUEST", headers);

		m_sdrSize = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "SDR_SIZE_IN_KB", 1000);
		m_sdrInterval = RBTParametersUtils.getParamAsLong(iRBTConstant.SMS, "SDR_INTERVAL_IN_HRS", 24);
		m_sdrRotation = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SDR_ROTATION_TYPE", null);
		m_sdrBillingOn = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SDR_BILLING_ON", "TRUE");
		m_removeLeadingZero = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "REMOVE_LEADING_ZERO", "FALSE");
		m_promotion1ValidFormat = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROMOTION1_VALID_FORMAT", null);
		m_promotion1InvalidFormat = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROMOTION1_INVALID_FORMAT", null);
		m_giftNational = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "GIFT_NATIONAL", "FALSE");
		m_checkClipSMSAlias = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CHECK_CLIP_SMS_ALIAS", "FALSE");
		m_actAllowed = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "IS_ACT_ALLOWED", "TRUE");
		m_allowReactivation = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_REACTIVATIONS", "FALSE");
		m_delSelections = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DEL_SELECTIONS", "TRUE");
		m_useSubscriptionManager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");
		if (m_useSubscriptionManager) {
			m_allowReactivation = false;
		}

		m_catMaxSMS = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "CATEGORY_SEARCH_RESULTS", 5);
		m_profileCorporateCategories = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROFILE_CORPORATE_CATEGORIES", "99");

		m_retActBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "RETAILER_ACT", null);
		if (m_retActBy == null)
			m_retActBy = m_retActByDefault;

		m_reqNoMatchDispTop = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "REQUEST_NO_MATCH_DISP_TOP", "FALSE");
		m_reqNoMatchDispMax = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "REQUEST_NO_MATCH_NO_RES", 0);
		if (m_reqNoMatchDispMax == 0)
			m_reqNoMatchDispMax = m_reqNoMatchDispMaxDefault;

		m_reqMaxSMS = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "REQUEST_MAX_SMS", 0);
		if (m_reqMaxSMS == 0)
			m_reqMaxSMS = m_reqMaxSMSDefault;

		m_reqMaxSMSCat = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "REQUEST_MAX_CAT_SMS", 0);
		if (m_reqMaxSMSCat == 0)
			m_reqMaxSMSCat = m_reqMaxSMSCatDefault;

		m_maxClipSearched = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "REQUEST_MAX_CLIP_SEARCHED", 0);
		if (m_maxClipSearched == 0)
			m_maxClipSearched = m_maxClipSearchedDefault;

		m_addMovieRequest = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ADD_MOVIE_REQUEST", "FALSE");
		m_doTodCheck = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DO_TOD_CHECK", "TRUE");

		String filter = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REQUEST_LANG_FILTER", null);
		if (filter != null) {
			StringTokenizer token = new StringTokenizer(filter, ",");
			if (token.hasMoreTokens())
				m_langFilter = token.nextToken().equalsIgnoreCase("TRUE");
			if (token.hasMoreTokens())
				m_reqDefLang = token.nextToken();
		}

		m_phoneNumberLength = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "PHONE_NUMBER_LENGTH", 9);
		if (m_phoneNumberLength == 0)
			m_phoneNumberLength = m_phoneNumberLengthDefault;

		m_ctOneKeyword = Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CT_ONE_KEYWORD", null), null);
		m_ctOneClassTypeValid = isSubClassTypeValid("CTONE");

		initializeFeedStatusVariables();
		String mgmKeyword = null;
		String mgm = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "MGM_PARAMS", null);
		if (mgm != null) {
			StringTokenizer token = new StringTokenizer(mgm, ",");
			if (token.hasMoreTokens())
				m_mgmOn = token.nextToken().equalsIgnoreCase("TRUE");
			if (token.hasMoreTokens())
				mgmKeyword = token.nextToken().toLowerCase();
			if (token.hasMoreTokens())
				m_mgmActBy = token.nextToken().toLowerCase();
			if (token.hasMoreTokens()) {
				try {
					m_mgmMinNoDaysActive = Integer.parseInt(token.nextToken());
				} catch (Exception e) {
					m_mgmMinNoDaysActive = 7;
				}
			}
			if (token.hasMoreTokens()) {
				try {
					m_mgmMaxGiftsMonth = Integer.parseInt(token.nextToken());
				} catch (Exception e) {
					m_mgmMaxGiftsMonth = 2;
				}
			}
			addToFeatureTable("MGM", mgmKeyword);
		}

		// m_clips = new Hashtable();
		// m_clipIDPromoID = new Hashtable();
		// m_Categories = new Hashtable();
		m_profileClips = new Hashtable();

		if (rbtClipsLucene == null) {
			rbtClipsLucene = new RBTLuceneSearch();
			rbtClipsLucene.init();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 3);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			m_last_lucene_update_date = cal.getTime();
			m_thread_update_cache = new Thread(this);
			m_thread_update_cache.start();
		}
		if (m_rbtAccounting == null) {
			createAccounting();
		}

		String yrsub = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "YEARLY_SUBSCRPTION_CLASS", null);
		m_yearlySubscription = new ArrayList();
		if (yrsub != null) {
			StringTokenizer token = new StringTokenizer(yrsub, ",");
			while (token.hasMoreTokens()) {
				String tmpToken = token.nextToken();
				for (int i = 0; i < m_subClasses.length; i++) {
					if (tmpToken.equals(m_subClasses[i].getSubscriptionClass())) {
						m_yearlySubscription.add(tmpToken.toUpperCase());
						break;
					}
				}
			}
		}

		List<ChargeClass> charge_class = CacheManagerUtil.getChargeClassCacheManager().getAllChargeClass();
		m_chargemap = new HashMap();
		if (charge_class != null) {
			for (int y = 0; y < charge_class.size(); y++) {
				m_chargemap.put(charge_class.get(y).getChargeClass(), charge_class.get(y));
			}
		}

		m_chargingCycle = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_CHARGING_CYCLE", "MONTHLY");
		Parameters memCacheOn = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "START_PLAYER_DAEMON");
		if (memCacheOn != null && memCacheOn.getValue() != null)
			isMemCacheModel = (memCacheOn.getValue().equalsIgnoreCase("TRUE") || memCacheOn
					.getValue().equalsIgnoreCase("ON"));

		Parameters ugcDefaultType = CacheManagerUtil.getParametersCacheManager().getParameter("GATHERER",
				"UGC_SUBSCRIPTION_TYPE");
		if (ugcDefaultType != null && ugcDefaultType.getValue() != null)
			m_UGCsubscriptionType = ugcDefaultType.getValue();
		m_subscriptionType = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUBSCRIPTION_TYPE", "OPTOUT");
		ChargePromoTypeMap[] ccMap = m_rbtDBManager.getChargePromoTypeMaps();
		if (ccMap != null && ccMap.length > 0) {
			for (int i = 0; i < ccMap.length; i++)
				if (ccMap[i].level() == 1)
					addToHashtable(m_promoCodeKeywordMap, ccMap[i].promoType(),
							Tools
									.tokenizeArrayList(ccMap[i].smsKeyword(),
											null));
				else if (ccMap[i].level() == 2)
					addToHashtable(m_ChargingModelKeywordMap, ccMap[i]
							.promoType(), Tools.tokenizeArrayList(ccMap[i]
							.smsKeyword(), null));
				else if (ccMap[i].level() == 3)
					addToHashtable(m_SubscriptionModelKeywordMap, ccMap[i]
							.promoType(), Tools.tokenizeArrayList(ccMap[i]
							.smsKeyword(), null));
		}
		m_operator_prefix_update_interval_hrs = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "OPERATOR_PREFIX_UPDATE_INTERVAL_HRS", 24);

		bConfirmDeactivation = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CONFIRM_DEACTIVATION", "FALSE");
		m_corpBlockSongChangeSomeSongs = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_BLOCK_CHANGE_FOR_SOME_SONGS", "FALSE");
		m_minDaysDeactivationConfirm = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "MIN_DAYS_DEACTIVATION_CONFIRM", 5);
		m_deactivationConfirmClearDays = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "DEACTIVATION_CONFIRM_CLEAR_DAYS", 1);
		m_allowInactUserGift = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_INACT_USER_GIFT", "FALSE");

		Calendar endCal = Calendar.getInstance();
		endCal.set(2037, 0, 1);
		m_endDate = endCal.getTime();
		giveUGSsongList = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "GIVE_UGS_SONG_LIST", "FALSE");
		allowRemovalOfNullCallerIDSelection = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_REMOVAL_OF_NULL_CALLERID_SEL", "FALSE");
		maxCallerIDSelectionsAllowed = m_rbtDBManager.maxCallerIDSelectionsAllowed;

		getConversionTableChargeClass();
		// copyDefaultAllowed = copyDefaultAllowed();
		m_defaultClip = getDefaultClip();
		getSongCatcherDetails();
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("GATHERER",
				"CHECK_FOR_SUSPENDED_SEL");
		if (param != null && param.getValue() != null)
			m_checkForSuspendedSelection = (param.getValue().equalsIgnoreCase(
					"TRUE") || param.getValue().equalsIgnoreCase("ON"));
		param = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "ADD_SEL_TO_LOOP");
		if (param != null && param.getValue() != null)
			m_addSMSSELToLoop = (param.getValue().equalsIgnoreCase("TRUE") || param
					.getValue().equalsIgnoreCase("ON"));
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "ONLY_SEARCH_SHORT_CODE");
		if (param != null && param.getValue() != null)
			m_onlySearchShortCode = param.getValue().trim();
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SMS_TEXT_FOR_ALL");
		if (param != null && param.getValue() != null)
			m_smsTextForAll = param.getValue().trim();
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("GATHERER", "WAIT_TIME_DOUBLE_CONFIRMATION");
		if (param != null && param.getValue() != null)
		try
		{
			m_waitTimeDoubleConfirmation = Integer.parseInt(param.getValue().trim());
		}
		catch(Exception e)
		{
			m_waitTimeDoubleConfirmation = 30;
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "MIN_VALUE_PROMO_ID");
		if (param != null && param.getValue() != null)
		try
		{
			m_minValuePromoId = Integer.parseInt(param.getValue().trim());
		}
		catch(Exception e)
		{
			m_minValuePromoId = 0;
		}

		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "IS_TATA_GSM_IMPL");
		if (param != null && param.getValue() != null)
			m_isTataGSMImpl = param.getValue().trim().equalsIgnoreCase("TRUE");
		
		m_useWDSMap= RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_WDS_CIRCLE_MAP", "FALSE");
		m_makeWDSRequest= RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "MAKE_WDS_REQUEST", "FALSE");
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "EXPIRY_FORMAT_STRING");
		String expiryFormatString = "yyyy MM dd HH:mm :ss"; 
		if(param != null && param.getValue() != null)
			expiryFormatString = param.getValue().trim();
		try
		{
			if(expiryFormatString != null)
				expiryDateFormat = new SimpleDateFormat(expiryFormatString);
		}catch(Exception e)
		{
			expiryDateFormat = new SimpleDateFormat("yyyy MM dd HH:mm :ss");
		}
		
		loadSMSTexts();
	}

	private void createAccounting() {
		// String _method = "createAccounting()";
		// logger.info("****** blank" );
		m_rbtAccounting = Accounting.getInstance(m_sdrWorkingDir, m_sdrSize,
				m_sdrInterval, m_sdrRotation, m_sdrBillingOn);
		if (m_rbtAccounting == null)
			logger.info("RBT::Accounting class can not be created");
		m_rbtHsbAccounting = Accounting.getInstance(m_sdrWorkingDir
				+ File.separator + "hsb", m_sdrSize, m_sdrInterval,
				m_sdrRotation, m_sdrBillingOn);
	}

	public void addToHsbAccounting(String type, String subscriberID,
			String request, String response, String ip) {
		// String _method = "addToAccounting()";
		// logger.info("****** parameters are -- "+type + "
		// & "+subscriberID + " & "+request+ " & "+response+" & "+ip );
		try {
			if (m_rbtHsbAccounting != null) {
				HashMap acMap = new HashMap();
				acMap.put("APP_ID", "RBT");
				if (type.equalsIgnoreCase("request")) {
					acMap.put("TYPE", "SMS-REQUEST");
					acMap.put("SENDER", subID(subscriberID));
					acMap.put("RECIPIENT", m_smsNo);
					acMap.put("REQUEST_TS", request);
					acMap.put("RESPONSE_TIME_IN_MS", "NA");
					acMap.put("CALLING_MODE", "NA");
					acMap.put("CALLBACK_MODE", "NA");
					acMap.put("DATA_VOLUME", "NA");
				} else {
					acMap.put("TYPE", "SMS-RESPONSE");
					acMap.put("SENDER", m_smsNo);
					acMap.put("RECIPIENT", subscriberID);
					acMap.put("REQUEST_TS", request);
					acMap.put("RESPONSE_TIME_IN_MS", response);
					acMap.put("CALLING_MODE", "NA");
					acMap.put("CALLBACK_MODE", "NA");
					acMap.put("DATA_VOLUME", "NA");
				}
				acMap.put("SMSC_MESSAGE_ID", ip);
				acMap.put("STATUS", (new SimpleDateFormat("yyyyMMddHHmmssms"))
						.format((new Date(System.currentTimeMillis()))));
				if (m_rbtHsbAccounting != null) {
					m_rbtHsbAccounting.generateSDR("sms", acMap);
					// logger.info("RBT::Writing
					// to the accounting file");
				}
				acMap = null;
			}
		} catch (Exception e) {
			logger.info("RBT::Exception caught " + e.getMessage());
		}
	}

	private void instantiateClipsCategories() {
		// String _method = "instantiateclipscategories()";
		// logger.info("****** blank");
		/*
		 * m_clips = new Hashtable(); if (clips != null && clips.length > 0) {
		 * for (int i = 0; i < clips.length; i++) { if (clips[i].getPromoID() !=
		 * null) { // m_clipIDPromoID.put(""+clips[i].id(), clips[i].promoID()); //
		 * m_clips.put(clips[i].promoID().toLowerCase(), clipMinimal);
		 * m_clipIDPromoID.put(clips[i].getPromoID().toLowerCase(),
		 * ""+clips[i].getClipId()); } // else // m_clips.put(clips[i].id() +
		 * ":NULL_PROMO", clipMinimal);
		 * 
		 * m_clips.put(""+clips[i].getClipId(), clips[i]); }
		 * //logger.info("***** //
		 * "+m_clips); }
		 */

		m_profileClips = new Hashtable();
		m_profileList = new ArrayList();
		Clips[] catClips = getActiveClips(m_categoryID);
		String value = null;
		String clipAlias = null;
		StringTokenizer stkn = null;
		if (catClips != null && catClips.length > 0) {
			for (int i = 0; i < catClips.length; i++) {
				clipAlias = catClips[i].alias();
				if (clipAlias != null
						&& catClips[i].wavFile() != null
						&& (catClips[i].wavFile()).indexOf("_"
								+ m_globalDefaultLanguage + "_") != -1) {
					stkn = new StringTokenizer(clipAlias, ",");
					while (stkn.hasMoreTokens()) {
						value = stkn.nextToken().toLowerCase();
						m_profileClips.put(value, catClips[i]);
						m_profileList.add(value);
					}
				}
			}
		}

		addToFeatureTable("SET_PROFILE", m_profileList);

		// m_rbtDBManager.getAllCategoriesWithAlias(m_Categories);
		/*
		 * Categories[] category = m_rbtDBManager.getAllCategories(); String
		 * valueCat = null; m_Categories = new Hashtable(); if (category != null &&
		 * category.length > 0) { for (int i = 0; i < category.length; i++) {
		 * String categoryAlias = category[i].alias(); if (categoryAlias !=
		 * null) { StringTokenizer stkn = new StringTokenizer(categoryAlias,
		 * ","); while (stkn.hasMoreTokens()) { valueCat =
		 * stkn.nextToken().toLowerCase(); m_Categories.put(valueCat,
		 * category[i]); } } } }
		 */
	}

	public void addToAccounting(String type, String subscriberID,
			String request, String response, String ip) {
		// String _method = "addToAccounting()";
		// logger.info("****** parameters are -- "+type + "
		// & "+subscriberID + " & "+request+ " & "+response+" & "+ip );
		try {
			if (m_rbtAccounting != null) {
				HashMap acMap = new HashMap();
				acMap.put("APP_ID", "RBT");
				if (type.equalsIgnoreCase("request")) {
					acMap.put("TYPE", "SMS-REQUEST");
					acMap.put("SENDER", subID(subscriberID));
					acMap.put("RECIPIENT", m_smsNo);
					acMap.put("REQUEST_TS", request);
					acMap.put("RESPONSE_TIME_IN_MS", "NA");
					acMap.put("CALLING_MODE", "NA");
					acMap.put("CALLBACK_MODE", "NA");
					acMap.put("DATA_VOLUME", "NA");
				} else {
					acMap.put("TYPE", "SMS-RESPONSE");
					acMap.put("SENDER", m_smsNo);
					acMap.put("RECIPIENT", subscriberID);
					acMap.put("REQUEST_TS", request);
					acMap.put("RESPONSE_TIME_IN_MS", response);
					acMap.put("CALLING_MODE", "NA");
					acMap.put("CALLBACK_MODE", "NA");
					acMap.put("DATA_VOLUME", "NA");
				}
				acMap.put("SMSC_MESSAGE_ID", ip);
				acMap.put("STATUS", (new SimpleDateFormat("yyyyMMddHHmmssms"))
						.format((new Date(System.currentTimeMillis()))));
				if (m_rbtAccounting != null) {
					m_rbtAccounting.generateSDR("sms", acMap);
					// logger.info("RBT::Writing
					// to the accounting file");
				}
				acMap = null;
			}
		} catch (Exception e) {
			logger.info("RBT::Exception caught " + e.getMessage());
		}
	}

	private void initializeLuceneClips() {
		String _method = "initializeLuceneClips()";
		rbtClipsLucene.createWriter(getRequestRBTClips());
	}

	public boolean isSubActive(Subscriber sub) {
		return (m_rbtDBManager.isSubActive(sub));
	}

	public boolean isSubActive(Subscriber sub, HashMap z) {
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		
		return (m_rbtDBManager.isSubActive(sub));
	}
	
	public boolean isSubscriberActivated(Subscriber sub, HashMap z) {
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		return (m_rbtDBManager.isSubscriberActivated(sub, rbtType));
	}


	public String refreshContent() {
		// String _method = "refreshContent()";
		if (!m_contentRefreshed) {
			synchronized (m_reInit) {
				if (!m_contentRefreshed && !m_thread_update_cache.isAlive()) {
					m_thread_update_cache = new Thread(this);
					m_contentRefreshed = true;
					System.out.println("Starting Content Refresh Thread ");
					m_thread_update_cache.start();
				} else
					return "later";
			}

		} else
			return "false";
		return "true";
	}

	private boolean isCorpSub(String strSubID, HashMap z) {
		// String _method = "isCorpSub()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		return (isSubAlreadyActiveOnStatus(strSubID, null, 0, rbtType));
	}

	private boolean isDurationDays(String alias) {
		// String _method = "isDurationDays()";
		// //logger.info("****** parameters are --
		// "+alias);
		if (alias != null && m_DayProfileList != null) {
			StringTokenizer stk = new StringTokenizer(alias, ",");
			while (stk.hasMoreTokens()) {
				if (m_DayProfileList.contains(stk.nextToken().toLowerCase()))
					return true;
			}

		}

		return false;
	}

	private FeedSchedule getCricketClass(String pass) {
		// String _method = "getcricketClass()";
		// //logger.info("****** parameters are -- "+pass);
		return (m_rbtDBManager.getFeedSchedule("CRICKET", pass));
	}

	private FeedSchedule getNextCricketSchedule(String pass, int interval) {
		// String _method = "getNextCricketSchedule()";
		// //logger.info("****** parameters are -- "+pass +
		// " & "+interval);
		FeedSchedule[] schedule = m_rbtDBManager.getFeedSchedules("CRICKET",
				pass, interval);
		if (schedule == null || schedule.length == 0)
			return null;
		else
			return (schedule[0]);
	}

	private Date getNextChargingDate(String subscriptionPeriod,
			String gracePeriod) {
		// String _method = "getNextChargingDate()";
		// //logger.info("****** parameters are --
		// "+subscriptionPeriod + " & "+gracePeriod);
		if (subscriptionPeriod == null)
			subscriptionPeriod = "M1";
		int type = 0;
		int number = 0;

		Calendar calendar = Calendar.getInstance();
		if (subscriptionPeriod.startsWith("D"))
			type = 0;
		else if (subscriptionPeriod.startsWith("W"))
			type = 1;
		else if (subscriptionPeriod.startsWith("M"))
			type = 2;
		else if (subscriptionPeriod.startsWith("Y"))
			type = 3;
		else if (subscriptionPeriod.startsWith("B"))
			type = 4;
		else if (subscriptionPeriod.startsWith("O"))
			type = 5;

		if (type != 4 && type != 5) {
			try {
				number = Integer.parseInt(subscriptionPeriod.substring(1));
			} catch (Exception e) {
				type = 2;
				number = 1;
			}
		}

		try {
			calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(gracePeriod));
		} catch (Exception e) {

		}

		switch (type) {
		case 0:
			calendar.add(Calendar.DAY_OF_YEAR, number);
			break;
		case 1:
			calendar.add(Calendar.WEEK_OF_YEAR, number);
			break;
		case 2:
			calendar.add(Calendar.MONTH, number);
			break;
		case 3:
			calendar.add(Calendar.YEAR, number);
			break;
		case 4:
			calendar.add(Calendar.YEAR, 50);
			break;
		case 5:
			calendar.add(Calendar.YEAR, 50);
			break;
		default:
			calendar.add(Calendar.MONTH, 1);
			break;
		}
		return calendar.getTime();
	}

	public boolean isRetailer(String strSubID) {
		// String _method = "isRetailer()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		Retailer ret = getRetailer(strSubID, "RETAILER");
		if (ret != null)
			return true;
		else
			return false;
	}

	private Retailer getRetailer(String strSubID, String type) {
		// String _method = "getRetailer()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+type);
		return (m_rbtDBManager.getRetailer(strSubID, type));
	}

	private FeedStatus getCricketFeed() {
		// String _method = "getCricketFeed()";
		// //logger.info("****** no parameters.");
		return (m_rbtDBManager.getFeedStatus("CRICKET"));
	}

	private Categories getCategoryAlias(String alias, String circleId,
			char prepaidYes) {
		// String _method = "getCategoryAlias()";
		// //logger.info("****** parameters are --
		// "+alias);
		return (m_rbtDBManager.getCategoryAlias(alias, circleId, prepaidYes));
	}

	public String connectToRemote(String strSubID, String strMsg, boolean isCopy) {
		// String _method = "connectToRemote()";
		// logger.info("****** parameters are -- " +
		// strSubID
		// + " & " + strMsg + " & " + isCopy);
		if (bUpdateOperatorPrefixSite
				&& (m_next_update_operator_prefix_date == null || m_next_update_operator_prefix_date
						.getTime() < System.currentTimeMillis()
						&& !m_thread_update_cache.isAlive())) {
			synchronized (m_initObject) {
				if (m_next_update_operator_prefix_date == null
						|| m_next_update_operator_prefix_date.getTime() < System
								.currentTimeMillis()) {
					// bUpdateOperatorPrefix = true;
					// m_thread_update_prefix = new Thread(this);
					// m_thread_update_prefix.start();
					updateOperatorPrefixes();
					updateCircleID();
					m_next_update_operator_prefix_date = new Date(System
							.currentTimeMillis()
							+ m_operator_prefix_update_interval_hrs
							* 60
							* 60
							* 1000);
				}
			}
		}
		strSubID = subID(strSubID);

		try {
			String strURL = null;
			if (isRemoteSub(strSubID) && !isCopy) {
				if (m_remoteURL == null)
					return null;
				strURL = m_remoteURL + "SUB_ID="
						+ URLEncoder.encode(strSubID, "UTF-8") + "&SMS_TEXT="
						+ URLEncoder.encode(strMsg, "UTF-8");
			} else {
				String sitePrefix = getURL(strSubID);
				if (sitePrefix != null && sitePrefix.length() > 0) {
					if (isCopy) {
						strURL = Tools.findNReplaceAll(sitePrefix,
								"rbt_sms.jsp", "");
						strURL = Tools.findNReplaceAll(strURL, "?", "");
						strURL = strURL + strMsg;
					} else
						strURL = sitePrefix + "SUB_ID="
								+ URLEncoder.encode(strSubID, "UTF-8")
								+ "&SMS_TEXT="
								+ URLEncoder.encode(strMsg, "UTF-8");
				} else
					return null;
			}

			/*URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
			BufferedReader buffer = new BufferedReader(
					new InputStreamReader(is));

			String line = null;
			String response = "";
			while ((line = buffer.readLine()) != null)
				response += line;
			boolean success = false;
			// String response = "";
			*/
			StringBuffer result = new StringBuffer();
			Integer statusInt = new Integer(-1);
			boolean success = Tools.callURL(strURL, statusInt, result, false, "", -1);
			String response = result.toString().trim();

			// logger.info("RBT::invoked URL "
			// + strURL + " and got response " + response);
			if (success && response != null && !response.trim().equals("")) {
				return response;
			}
			return null;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	 public String connectToRemote(HashMap hm, boolean isCopy)
    {
//        String _method = "connectToRemote()";
//        logger.info("****** parameters are -- " + strSubID
//                + " & " + strMsg + " & " + isCopy);
		
		String strSubID = (String)hm.get("SUB_ID");
		String strMsg = (String)hm.get("SMS_TEXT");
		String strShortCode = (String)hm.get("SHORTCODE");
		String strTrxID = (String)hm.get("TRX_ID");	
		
		if (bUpdateOperatorPrefixSite
                && (m_next_update_operator_prefix_date == null || m_next_update_operator_prefix_date
                        .getTime() < System.currentTimeMillis()
                        && !m_thread_update_cache.isAlive()))
        {
            synchronized (m_initObject)
            {
                if (m_next_update_operator_prefix_date == null
                        || m_next_update_operator_prefix_date.getTime() < System
                                .currentTimeMillis())
                {
                    //bUpdateOperatorPrefix = true;
                    //m_thread_update_prefix = new Thread(this);
                    //m_thread_update_prefix.start();
                    updateOperatorPrefixes();
					updateCircleID();
                    m_next_update_operator_prefix_date = new Date(System
                            .currentTimeMillis()
                            + m_operator_prefix_update_interval_hrs
                            * 60
                            * 60
                            * 1000);
                }
            }
        }
        strSubID = subID(strSubID);

        try
        {
            String strURL = null;
            if (isRemoteSub(strSubID) && !isCopy)
            {
                if (m_remoteURL == null)
                	return null;
                strURL = m_remoteURL + "SUB_ID="
                        + URLEncoder.encode(strSubID, "UTF-8") + "&SMS_TEXT="
                        + URLEncoder.encode(strMsg, "UTF-8");
            }
            else
            {
                String sitePrefix = getURL(strSubID);
                if (sitePrefix != null && sitePrefix.length() > 0)
                {
                    if (isCopy)
                    {
                        strURL = Tools.findNReplaceAll(sitePrefix,
                                                       "rbt_sms.jsp", "");
                        strURL = Tools.findNReplaceAll(strURL, "?", "");
                        strURL = strURL + strMsg;
                    }
                    else
                    {
						strURL = sitePrefix + "SUB_ID="
                                + URLEncoder.encode(strSubID, "UTF-8")
                                + "&SMS_TEXT="
                                + URLEncoder.encode(strMsg, "UTF-8");
						 if(strShortCode != null)
							 strURL = strURL + "&SHORTCODE="
                                + URLEncoder.encode(strShortCode, "UTF-8");
						if(strTrxID != null)
							strURL = strURL + "&TRX_ID="
                                + URLEncoder.encode(strTrxID, "UTF-8");
					}
                }
                else
                    return null;
            }

            /*URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(is));

            String line = null;
            String response = "";
            while ((line = buffer.readLine()) != null)
                response += line;
            */
            Integer statusInt = new Integer(-1); 
            StringBuffer result = new StringBuffer(); 
            boolean success = false; 
            String response = ""; 
            success = Tools.callURL(strURL, statusInt, result,false,"",-1); 
            response = result.toString().trim(); 

//            logger.info("RBT::invoked URL "
//                    + strURL + " and got response " + response);
            if (success && response != null && !response.trim().equals(""))
            {
                return response;
            }
            return null;
        }
        catch (Exception e)
        {
            logger.error("", e);
            return null;
        }
    }

	private boolean removeViralSMSTable(String strSubID, String type) {
		// String _method = "removeViralSMSTable";
		// //logger.info("****** parameters are -- "+type +
		// " & "+strSubID + " & "+type);
		return (m_rbtDBManager.removeViralSMS(strSubID, type));
	}

	private void setSearchCount(String strSubID, int count, String type) {
		// String _method = "setSearchCount()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+type);
		m_rbtDBManager.setSearchCount(strSubID, type, count);
	}

	public ViralSMSTable insertViralSMSTable(String strSubID, String callerID,
			String clipID, String type, int count) {
		// String _method = "insertViralSMSTable()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+callerID + " & "+clipID+ " & "+type+" & "+count );
		return (m_rbtDBManager.insertViralSMSTableMap(strSubID, Calendar
				.getInstance().getTime(), type, callerID, clipID, count, "SMS",
				null, null));
	}

	private ViralSMSTable getViralSMSTable(String strSubID, String type) {
		// String _method = "getViralSMSTable()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+type);
		return (m_rbtDBManager.getViralSMSByType(strSubID, type));
	}

	private ViralSMSTable[] getViralSMSesByType(String strSubID, String type) {
		// String _method = "getViralSMSesByType()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+type);
		return (m_rbtDBManager.getViralSMSesByType(strSubID, type));
	}

	public boolean addSelections(String strSubID, String strCallerID,
			boolean isPrepaid, boolean changeSubType, int categoryID,
			String songName, Date startDate, Date endDate, int status,
			int trialPeriod, String strSelectedBy, String strSelectionInfo,
			String classType, String regexType, String subYes,
			int maxSelections, String subClass, String promoType, String req,
			HashMap z, boolean OptIn) {
		// String _method = "addSelections()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strCallerID + " & "+isPrepaid+ " & "+changeSubType+" &
		// "+categoryID+" & "+songName+" & "+startDate+" & "+endDate+" &
		// "+status+" & "+trialPeriod+" & "+strSelectedBy+" &
		// "+strSelectionInfo+" & "+classType+" & "+chargingModel+" & "+subYes
		// );
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!m_rbtDBManager
				.isSelectionAllowed(subscriber, strCallerID, rbtType)) {
			setReturnValues(z, getSMSTextForID(z, "ADRBT_SELECTION_FAILURE",
					m_ADRBTSelectionFailureDefault), STATUS_SUCCESS);
			return false;
		}
		boolean isSelSuspended = false;
		if (m_checkForSuspendedSelection) {
			isSelSuspended = m_rbtDBManager.isSelSuspended(strSubID,
					strCallerID, rbtType);
		}
		if (isSelSuspended) {
			setReturnValues(z, getSMSTextForID(z, "SELECTION_SUSPENDED",
					m_SuspendedSelDefault), STATUS_SUCCESS);
			return false;
		}
		/*
		 * ADDED FOLLOWING TO ADD ALL SELECTIONS IN LOOP/NOT(based on
		 * configuration)
		 */
		boolean inLoop = m_rbtDBManager.allowLooping() && m_addSMSSELToLoop; // &&
																				// m_rbtDBManager.isDefaultLoopOn();
		if (inLoop
				&& !m_rbtDBManager.moreSelectionsAllowed(strSubID, strCallerID,
						rbtType)) {
			setReturnValues(z, getSMSTextForID(z, "SELECTION_LIMIT_REACHED",
					m_selectionLimitReachedDefault), STATUS_SUCCESS);
			return false;
		}
		SubscriptionClass sub = getSubscriptionClass(subClass);
		if (sub != null && sub.getFreeSelections() > 0
				&& maxSelections < sub.getFreeSelections())
			classType = "FREE";
		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		String circleID = getCircleID(strSubID);
		if (!isActivationRequest
				&& req.equalsIgnoreCase("VALIDATE")
				&& (m_rbtDBManager.getAvailableSelection(null,
						strSubID,
						strCallerID,
						null,
						// getCategory(categoryID,circleID), songName, status,
						// 0, 23, m_doTodCheck, (m_rbtDBManager.allowLooping()
						// && m_rbtDBManager.isDefaultLoopOn())) == null))
						getCategory(categoryID, circleID), songName, status, 0,
						2359, null, null, m_doTodCheck,
						(m_rbtDBManager.allowLooping() && m_rbtDBManager
								.isDefaultLoopOn()), rbtType, null, strSelectedBy) == null)) {
			classType = getSelectionChargeClass(categoryID, songName,
					classType, z);
			ChargeClass charge = null;
			if (classType != null && m_chargemap.containsKey(classType))
				charge = (ChargeClass) m_chargemap.get(classType);

			addToHashMap(z, OCG_CHARGE_ID, charge.getOperatorCode1() + ":"
					+ charge.getAmount());

			return true;

		}

		String transID = (String) getFromZTable(z, TRANS_ID);

		if (transID != null && transID.trim().equalsIgnoreCase("-1"))
			transID = null;

		if (getFromZTable(z, SG_MODE) != null)
			strSelectedBy = (String) getFromZTable(z, SG_MODE);

		if (getFromZTable(z, SG_CAT_ID) != null) {
			String tmp = (String) getFromZTable(z, SG_CAT_ID);
			int cat = -1;
			try {
				cat = Integer.parseInt(tmp.trim());
			} catch (Exception e) {
				cat = -1;
			}

			if (cat != -1) {
				categoryID = cat;
			}
		}
		
		HashMap clipMap = new HashMap();
		ClipMinimal clips = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		// if(m_NAVCategoryIDs != null &&
		// m_NAVCategoryIDs.contains(""+categoryID))
		// status = 85;
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		}

		if(((String) z.get(NEWS_BEAUTY_FEED)) != null && z.get(NEWS_BEAUTY_FEED).equals("TRUE"))
		{
			if(clips != null)
				categoryID = m_rbtDBManager.getCatIDsForClipId(clips.getClipId());
		}
		logger.info("The value of categoryID "+categoryID);
		HashMap extraInfo= new HashMap();
		
		String wdsResult=null;
		if (getFromZTable(z, EXTRA_INFO_WDS)!=null){
			wdsResult=(String) getFromZTable(z, EXTRA_INFO_WDS);
			extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
		}
		String success = m_rbtDBManager.addSubscriberSelections(strSubID,
				strCallerID, getCategory(categoryID, circleID), clipMap, null,
				startDate, endDate, status, strSelectedBy, strSelectionInfo,
				trialPeriod, isPrepaid, changeSubType, m_messagePath, 0, 2359,
				classType, m_useSubscriptionManager, m_doTodCheck, "SMS",
				regexType, subYes, promoType, null, true, false, transID,
				OptIn, false, inLoop, sub.getSubscriptionClass(), subscriber,
				rbtType, null,extraInfo, false, null, false);

		if(success != null && success.equalsIgnoreCase("FAILURE:DOWNLOAD_OVERLIMIT"))
		{
			setReturnValues(z, getSMSTextForID(z, "DOWNLOAD_LIMIT_REACHED",
					m_downloadLimitReachedDefault), STATUS_SUCCESS);
			return false;
		}
		if(success != null && success.startsWith("SELECTION_FAILED_ADRBT_"))
		{
			setReturnValues(z, getSMSTextForID(z, "ADRBT_SELECTION_FAILURE",
					m_ADRBTSelectionFailureDefault), STATUS_SUCCESS);
			return false;
		}
		if(success != null && success.equalsIgnoreCase(SELECTION_FAILED_SELECTION_OVERLAP))
		{
			String returnSms = getSMSTextForID(z, "SELECTION_FAILED_OVERLAP",
					null);
			if(returnSms != null)
			{
				setReturnValues(z, returnSms , STATUS_SUCCESS);
				return false;
			}
		}
		String response = "SUCCESS";
		if (success != null && !success.startsWith("SELECTION_SUCCESS"))
			response = "FAILURE";
		else
			addToHashMap(z, SONG_SET_RESPONSE, response);
		// }
		return true;
	}

	public boolean addPromoSelections(String strSubID, String strCallerID,
			boolean bPrepaid, boolean changeSubType, int categoryID,
			String songName, Date endDate, int status, int trialPeriod,
			String strSelectedBy, String strSelectionInfo, int FromTime,
			int ToTime, String classType, String mode, String type,
			String subYes, int maxSelections, String subClass, boolean OptIn,
			Subscriber subscriber, String promoID, HashMap z,
			boolean inLoopParam) {
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		boolean inLoop = m_rbtDBManager.allowLooping() && inLoopParam;
		if (inLoop
				&& !m_rbtDBManager.moreSelectionsAllowed(strSubID, strCallerID,
						rbtType)) {
			setReturnValues(z, getSMSTextForID(z, "SELECTION_LIMIT_REACHED",
					m_selectionLimitReachedDefault), STATUS_SUCCESS);
			return false;
		}

		String transID = (String) getFromZTable(z, TRANS_ID);

		if (transID != null && transID.trim().equalsIgnoreCase("-1"))
			transID = null;

		if (endDate == null) {
			/*
			 * Calendar endCal = Calendar.getInstance(); endCal.set(2037, 0, 1);
			 * endDate = endCal.getTime();
			 */
			endDate = m_endDate;
		}

		if (strCallerID == null && m_corpChangeSelectionBlock
				&& isCorpSub(strSubID, z)) {
			return false;
		} else if (!RBTDBManager.getInstance().isSelectionAllowed(
				subscriber, strCallerID, rbtType))
			return false;
		else {
			if (FromTime <= ToTime) {
				SubscriptionClass sClass = getSubscriptionClass(subClass);
				if (sClass != null && sClass.getFreeSelections() > 0
						&& maxSelections < sClass.getFreeSelections())
					classType = "FREE";

				HashMap clipMap = new HashMap();
				ClipMinimal clips = null;

				if (promoID != null)
					clips = getClipPromoID(promoID);
				else {
					clips = m_rbtDBManager.getClipRBT(songName);
				}
				
				HashMap extraInfo=new HashMap();
				String wdsResult=null;
				if (getFromZTable(z, EXTRA_INFO_WDS)!=null){
					wdsResult=(String) getFromZTable(z, EXTRA_INFO_WDS);
					extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
				}
				
				
				if (clips != null) {
					clipMap.put("CLIP_CLASS", clips.getClassType());
					clipMap.put("CLIP_END", clips.getEndTime());
					clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
					clipMap.put("CLIP_WAV", clips.getWavFile());
					clipMap.put("CLIP_ID", "" + clips.getClipId());
					clipMap.put("CLIP_NAME", clips.getClipName());

					String circleID = getCircleID(strSubID);
					String ret = RBTDBManager.getInstance()
							.addSubscriberSelections(strSubID, strCallerID,
									getCategory(categoryID, circleID), clipMap,
									null, null, endDate, status, strSelectedBy,
									strSelectionInfo, trialPeriod, bPrepaid,
									changeSubType, m_messagePath, FromTime,
									ToTime, classType,
									m_useSubscriptionManager, true, mode, type,
									subYes, "ESIA", null, true, false, transID,
									OptIn, false, inLoop,
									sClass.getSubscriptionClass(), subscriber,
									rbtType, null,extraInfo, false, null, false);
					if(ret != null && ret.startsWith("SELECTION_SUCCESS"))
						return true;
					else
						return false;
				}
			}
		}

		return false;
	}

	/*
	 * private SubscriberStatus getSubscriberFile(String strSubID, String
	 * callerID, String type) { // String _method = "getRBTwavFile()"; //
	 * //logger.info("****** parameters are -- //
	 * "+strSubID + " & "+callerID + " & "+type+ " & "+shuffleTable); return
	 * (m_rbtDBManager.getSubscriberFile(strSubID, callerID, type,
	 * RBTTonePlayerHelper .init().getShuffleTable(), null)); }
	 */

	public SubscriberStatus getSubscriberFile(String strSubID, String callerID,
			String type, boolean isMemCaheModel, int rbtType) {
		// String _method = "getRBTwavFile()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+callerID + " & "+type+ " & "+shuffleTable);
		return (m_rbtDBManager.getSubscriberFile(strSubID, callerID, type,
				isMemCaheModel, RBTTonePlayerHelper.init().getShuffleTable(),
				null));
	}

	public Subscriber getSubscriber(String strSubID) {
		// String _method = "getSubscriber()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		return (m_rbtDBManager.getSubscriber(strSubID));
	}

	public Subscriber activateSubscriber(String strSubID, String strActBy,
			Date endDate, boolean isPrepaid, int days, String strActInfo,
			String subClass, boolean isDirectActivation, int rbtType) {
		// String _method = "activateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strActBy + " & "+endDate+ " & "+isPrepaid+" & "+days+" &
		// "+strActInfo+" & "+subClass);
		return (m_rbtDBManager.activateSubscriber(strSubID, strActBy, endDate,
				isPrepaid, m_activationPeriod, days, strActInfo, subClass,
				m_useSubscriptionManager, isDirectActivation, rbtType));
	}


	
	public Subscriber activateSubscriber(Connection conn, String strSubID,
			String strActBy, Date endDate, boolean isPrepaid, int days,
			String strActInfo, String subClass, boolean isDirectActivation,
			int rbtType) {
		// String _method = "activateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strActBy + " & "+endDate+ " & "+isPrepaid+" & "+days+" &
		// "+strActInfo+" & "+subClass);
		return (m_rbtDBManager
				.activateSubscriber(conn, strSubID, strActBy, endDate,
						isPrepaid, m_activationPeriod, days, strActInfo,
						subClass, m_useSubscriptionManager, isDirectActivation,
						rbtType));
	}

	private void reactivateSubscriber(String strSubID, String strActBy,
			boolean isPrepaid, boolean chargeSelections, String strActInfo,
			boolean isDirectDeact, int rbtType) {
		// String _method = "reactivateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strActBy + " & "+isPrepaid+ " & "+chargeSelections+" &
		// "+strActInfo);
		String ret = deactivateSubscriber(strSubID, "SMS", null,
				m_delSelections, isDirectDeact);
		if (ret == null && ret.equals("SUCCESS"))
			activateSubscriber(strSubID, "SMS", null, isPrepaid, 0, strActInfo,
					null, false, rbtType);
	}

	public ClipMinimal getClip(int clipID) {
		// String _method = "getClip()";
		// //logger.info("****** parameters are --
		// "+clipID);
		// return (m_rbtDBManager.getClip(clipID));

		return RBTDBManager.getInstance().getClipMinimal(clipID, true);
	}

	public String getWavFile(int clipID) {
		// String _method = "getClip()";
		// //logger.info("****** parameters are --
		// "+clipID);
		// return (m_rbtDBManager.getClip(clipID));

		ClipMinimal clip = m_rbtDBManager.getClipMinimal(clipID, true);
		if (clip != null)
			return clip.getWavFile();

		return null;
	}

	public SortedMap getSMSPromoClips() {
		/*
		 * Set promoIdSet = m_clips.keySet(); Iterator iter =
		 * promoIdSet.iterator(); SortedMap clipsMap = new TreeMap(); while
		 * (iter.hasNext()) { ClipMinimal clipMinimal = (ClipMinimal)
		 * m_clips.get((String) iter .next());
		 * clipsMap.put(clipMinimal.getClipName(), clipMinimal .getWavFile()); }
		 * 
		 * return clipsMap;
		 */
		return RBTDBManager.getInstance().getSMSPromoClips();
	}

	public String getURL(String strSub) {
		// String _method = "getURL()";
		// //logger.info("****** parameters are --
		// "+strPrefix);
		if (strSub == null || strSub.length() <= 0)
			return null;
		for (int i = 1; i <= strSub.length(); i++) {
			if (m_operatorPrefix.containsKey(strSub.substring(0, i)))
				return (String) m_operatorPrefix.get(strSub.substring(0, i));
		}
		return null;
	}

	public Categories getCategory(int categoryID, String circleID) {
		return (m_rbtDBManager.getCategory(categoryID, circleID, 'b'));
	}

	private SubscriberPromo getSubscriberPromo(String strSubID, String strActBy) {
		return (m_rbtDBManager.getSubscriberPromo(strSubID, strActBy));
	}

	private SubscriberPromo createSubscriberPromo(String strSubID,
			int freedays, boolean bPrepaid, String strActBy, String type) {
		return m_rbtDBManager.createSubscriberPromo(strSubID, freedays,
				bPrepaid, strActBy, type);
	}

	private Clips[] getInListClips(int categoryID) {
		return (m_rbtDBManager.getInListCategoryClips(categoryID));
	}

	private Clips[] getActiveClips(int categoryID) {
		// String _method = "getActiveClips()";
		// //logger.info("****** parameters are --
		// "+categoryID);
		return (m_rbtDBManager.getAllClips(categoryID));
	}

	private boolean removeCallerIDSelection(String subscriberID,
			String callerID, String deSelBy, String wavFile, int rbtType) {
		// String _method = "removecallerIDSelection()";
		// logger.info("****** parameters are --
		// "+subscriberID + " & "+callerID);
		return (m_rbtDBManager.deactivateSubscriberRecords(subscriberID,
				callerID, 1, 0, 2359, m_useSubscriptionManager, deSelBy, wavFile,
				rbtType));
	}

	public String deactivateSubscriber(String strSubID, String deactivate,
			boolean isDirectDeact) {
		// String _method = "deactivateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+deactivate + " & "+date+ " & "+delSelections);
		return (m_rbtDBManager.deactivateSubscriber(strSubID, deactivate, null,
				m_delSelections, true, m_useSubscriptionManager, isDirectDeact,
				true));
	}

	public String deactivateSubscriber(Connection conn, String strSubID,
			String deactivate, boolean isDirectDeact, Subscriber sub) {
		// String _method = "deactivateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+deactivate + " & "+date+ " & "+delSelections);
		return (m_rbtDBManager.deactivateSubscriber(conn, sub.subID(),
				deactivate, null, m_delSelections, true,
				m_useSubscriptionManager, isDirectDeact, true));
	}

	private String deactivateSubscriber(String strSubID, String deactivate,
			Date date, boolean delSelections, boolean isDirectDeact) {
		// String _method = "deactivateSubscriber()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+deactivate + " & "+date+ " & "+delSelections);
		return (m_rbtDBManager.deactivateSubscriber(strSubID, deactivate, date,
				delSelections, true, m_useSubscriptionManager, isDirectDeact,
				true));
	}

	private void deactivateSubscriberRecords(String strSubID,
			String strCallerID, int status, int rbtType) {
		// String _method = "deactivateSubscriberRecords()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strCallerID+" & "+status);
		m_rbtDBManager.deactivateSubscriberRecords(strSubID, strCallerID,
				status, 0, 2359, m_useSubscriptionManager, "SMS", rbtType);
	}

	private ClipMinimal getClipRBT(HashMap z, String strWavFile) {
		// String _method = "getClipRBT()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+strWavFile);
		ClipMinimal clipMinimal = m_rbtDBManager.getClipRBT(strWavFile);
		addToHashMap(z, CLIP_OBJECT, clipMinimal);
		return clipMinimal;
	}

	public ClipMinimal getClipRBT(String strWavFile) {
		// String _method = "getClipRBT()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+strWavFile);
		ClipMinimal clipMinimal = m_rbtDBManager.getClipRBT(strWavFile);
		return clipMinimal;
	}

	private Clips getProfileClip(HashMap z, String strWavFile) {
		// String _method = "getClipRBT()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+strWavFile);
		Clips clip = (Clips) m_profileClips.get(strWavFile);
		ClipMinimal clipMinimal = null;
		if (clip != null)
			clipMinimal = new ClipMinimal(clip);
		addToHashMap(z, CLIP_OBJECT, clipMinimal);
		return clip;
	}

	private Access getAccess(int clipID, String name, String year, String month) {
		// String _method = "getAccess()";
		// //logger.info("****** parameters are -- "+clipID
		// + " & "+name + " & "+year+ " & "+month);
		return (m_rbtDBManager.getAccess(clipID, name, year, month, null));
	}

	private boolean isSubAlreadyActiveOnStatus(String strSubID,
			String callerID, int status, int rbtType) {
		// String _method = "isSubAlreadyActiveOnStatus()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+callerID + " & "+status);
		SubscriberStatus subStatus = m_rbtDBManager.getActiveSubscriberRecord(
				strSubID, callerID, status, 0, 2359, rbtType);

		if (subStatus != null)
			return true;

		return false;
	}

	/*
	 * public boolean checkSubscriberPromo(String strSubID, String strActBy) {
	 * //String _method = "checkSubscriberPromo()"; SubscriberPromo subPromo =
	 * getSubscriberPromo(strSubID, strActBy);
	 * 
	 * if (subPromo != null) { return true; } return false; }
	 * 
	 * public boolean changeSubscriberPromoActivatedBy(String strSubID, String
	 * strActBy) { //String _method = "changeSubscriberPromoActivatedBy()";
	 * return (RBTDBManager.getInstance()
	 * .changeSubscriberPromoActivatedBy(strSubID, strActBy)); }
	 */

	public Clips[] getAllClips(int categoryID) {
		// String _method = "getAllClips()";
		// //logger.info("****** parameters are --
		// "+categoryID );
		return (m_rbtDBManager.getAllClips(categoryID));
	}

	public boolean isRemoteSub(String strSubID) {
		// String _method = "isRemoteSub()";
		// //logger.info("****** parameters are --
		// "+strSubID );
		int prefixIndex = RBTDBManager.getInstance().getPrefixIndex();
		return isThisFeature(getArrayList(subID(strSubID).substring(0,
				prefixIndex)), m_remotePrefix);
	}

	public boolean isValidIP(String strIP) {
		// String _method = "isValidIP()";
		// //logger.info("****** parameters are --
		// "+strIP);
		if (strIP == null)
			return false;
		return isThisFeature(getArrayList(strIP), m_validIP);
	}

	public boolean isValidServerIP(String strIP) {
		// String _method = "isValidServerIP()";
		// //logger.info("****** parameters are -- "+strIP
		// );
		return isThisFeature(getArrayList(strIP), m_validServerIP);
	}

	public boolean isValidPrepaidIP(String strIP, String strActBy) {
		// String _method = "isValidPrepaidIP()";
		// //logger.info("****** parameters are -- "+strIP
		// + " & "+strActBy );
		return isThisFeature(getArrayList(strIP), m_validPrepaidIP);
	}

	/*
	 * private void getClips() { //String _method = "getClips()";
	 * ////logger.info("****** no parameters."); //return
	 * (m_rbtDBManager.getClipsByName(null));
	 * m_rbtDBManager.getAllClipsForCaching(m_clips, m_clipIDPromoID); }
	 */

	public String subID(String strSubID) {
		// String _method = "subID()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		return (m_rbtDBManager.subID(strSubID));
	}

	public boolean isValidSub(String strSubID) {
		// String _method = "isValidSub()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		return (m_rbtDBManager.isValidPrefix(strSubID));
	}

	/*
	 * private SubscriberStatus[] getTypeSelections(String strSubID, String
	 * type) { //String _method = "getTypeSelections()";
	 * ////logger.info("****** parameters are -- //
	 * "+strSubID + " & "+type); return
	 * (m_rbtDBManager.getTypeSelections(strSubID, type, "SEL", null)); }
	 * 
	 * private boolean convertSelectionClassType(String strSubID, String
	 * intType, String finalType) { //String _method = "convertSelections()";
	 * //logger.info("****** parameters are -- "+strSubID // + " &
	 * "+intType + " & "+finalType); return
	 * (m_rbtDBManager.convertSelectionClassType(strSubID, intType, finalType,
	 * "SMS")); }
	 */

	public SubscriberStatus[] getSubscriberRecords(String strSubID, int rbtType) {
		// String _method = "getSubscriberRecords()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		return (m_rbtDBManager.getSubscriberRecords(strSubID, "GUI",
				m_useSubscriptionManager, rbtType));
	}

	public SubscriberStatus[] getSubscriberRecords(String strSubID, int rbtType,String callerID) {
		// String _method = "getSubscriberRecords()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		return (m_rbtDBManager.getSubscriberRecords(strSubID, "GUI",callerID,
				m_useSubscriptionManager, rbtType));
	}


	private SubscriptionClass[] getSubscriptionClasses() {
		// String _method = "getSubscriptionClasses()";
		// //logger.info("****** no parameters.");
		return (CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses().toArray(new SubscriptionClass[0]));
	}

	private SubscriptionClass getSubscriptionClass(String subClassType) {
		// String _method = "getSubscriptionClass";
		// //logger.info("****** no parameters.");
		for (int i = 0; i < m_subClasses.length; i++) {
			if (subClassType.equals(m_subClasses[i].getSubscriptionClass())) {
				return m_subClasses[i];
			}
		}

		return null;
	}

	private boolean isSubClassTypeValid(String subClassType) {
		// String _method = "issubClassTypeValid()";
		// //logger.info("****** parameters are --
		// "+subClassType);
		for (int i = 0; i < m_subClasses.length; i++) {
			if (subClassType.equals(m_subClasses[i].getSubscriptionClass())) {
				return true;
			}
		}
		return false;
	}

	private BulkPromoSMS[] getBulkPromoSmses() {
		// String _method = "getBulkPromoSmses()";
		// //logger.info("****** no parameters.");
		return (m_rbtDBManager.getBulkPromoSmses());
	}

	private boolean updateTNBSubscribertoNormal(String subscriber) {
		// String _method = "update()";
		// logger.info("****** parameters are --
		// "+subscriber );
		return (m_rbtDBManager.updateTNBSubscribertoNormal(subscriber,
				m_useSubscriptionManager, 40));
	}

	public boolean isInitializationDone() {
		// String _method = "isInitializationDone()";
		// logger.info("****** no parameters." );
		boolean success = false;
		synchronized (m_init) {
			success = bInitialized;
		}
		return success;
	}

	private void initializeFeedStatusVariables() throws Exception {
		// String _method = "initializeFeedStatusVariables()";
		// logger.info("****** no parameters.");
		m_feedStatus = getCricketFeed();
		if (m_feedStatus != null) {
			ArrayList feed1Msg = Tools.tokenizeArrayList(m_feedStatus
					.smsKeyword(), null);
			addToFeatureTable("FEED1", feed1Msg);
			m_cricketSubKey = Tools.tokenizeArrayList(
					m_feedStatus.subKeyword(), null);
		}
	}

	public Subscriber getSubscriberForSMCallbacks(Connection conn,
			String subscriberID) {
		return (RBTDBManager.getInstance()
				.getSubscriberForSMCallbacks(conn, subscriberID));
	}

	public Connection getConnection() {
		return (RBTDBManager.getInstance().getConnection());
	}

	public void initializeProcessing(HashMap z, ArrayList smsList)
			throws Exception {
		// String _method = "initializeProcessing";
		// logger.info("****** parameters are -- "+z + " &
		// "+smsList );
		// logger.info(""+m_featureNameKeywordTable);

		setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
				m_technicalFailureDefault), 0);
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);

		// Subscriber subscriber = getSubscriber(subscriberID);
		/*
		 * Connection conn = RBTDBManager.init(m_dbURL, m_usePool,
		 * m_countryPrefix) .getConnection(m_dbURL); if(conn == null)
		 * addToHashMap(z, "CONNECTION_ERROR", "TRUE"); Subscriber subscriber =
		 * RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
		 * .getSubscriberForSMCallbacks(conn, subscriberID); addToHashMap(z,
		 * SUBSCRIBER_OBJ, subscriber);
		 */

		String mappedCircleID=null;
		String wdsResult=null;
		HashMap<String,String> subscriberInfo=null;
		Subscriber subscriber=(Subscriber)getFromZTable(z, SUBSCRIBER_OBJ);
		
		if ( m_isTataGSMImpl && m_makeWDSRequest) {

			if (subscriberID != null)
				subscriberInfo = m_rbtDBManager.getSubscriberInfo(subscriberID);

			if (subscriberInfo != null) {
				if (subscriberInfo.containsKey("USER_TYPE")
						&& subscriberInfo.get("USER_TYPE") != null)
					addToHashMap(z, IS_PREPAID, new Boolean(subscriberInfo.get(
							"USER_TYPE").equalsIgnoreCase("PREPAID")));
				if (subscriberInfo.containsKey("CIRCLE_ID")
						&& subscriberInfo.get("CIRCLE_ID") != null)
					addToHashMap(z, CIRCLE_ID, subscriberInfo.get("CIRCLE_ID"));
				if (subscriberInfo.containsKey("STATUS")
						&& subscriberInfo.get("STATUS") != null)
					addToHashMap(z, WDS_ALLOW, subscriberInfo.get("STATUS"));

				if (subscriberInfo.containsKey("WDS_RESPONSE")
						&& subscriberInfo.get("WDS_RESPONSE") != null)
					addToHashMap(z, EXTRA_INFO_WDS, subscriberInfo
							.get("WDS_RESPONSE"));
			}
		}

		SitePrefix prefix = Utility.getPrefix(subscriberID);
		
		String callerID = getCallerID(smsList);
		addToHashMap(z, CALLER_ID, callerID);
		smsList = requestSearchOn(z, smsList);
		if(smsList != null && smsList.size() > 0)
		{
			String value = (String) smsList.get(0);
			int num = m_minValuePromoId;
			try
			{
				num = Integer.parseInt(value);
			}
			catch(Exception e)
			{
				num = m_minValuePromoId;
			}
			if(num < m_minValuePromoId)
			{
				smsList.add(m_requestRBTkeyword.get(0));
			}
		}
		String feature = getFeature(smsList, m_featureNameKeywordTable);
		addToHashMap(z, FEATURE, feature);
		
		if (((String) z.get(FEATURE)).equals("NEWS_BEAUTY_FEED"))
		{
			for(int i=0;i<smsList.size();i++)
			{
				if(smsList.get(i).equals(m_newsAndBeautyFeedKeyword))
					smsList.remove(i);
			}
			addToHashMap(z, NEWS_BEAUTY_FEED, "TRUE");
		}
		
		String language = getLang(smsList,prefix);
		addToHashMap(z, LANGUAGE, language);

		String chargingModel = getChargingModel(smsList);
		addToHashMap(z, SMS_CHARGING_MODEL, chargingModel);

		String subscriptionType = getSubscriptionType(smsList);
		addToHashMap(z, SUBSCRIPTION_TYPE, subscriptionType);

		boolean isRetailer = isRetailer(subscriberID);
		addToHashMap(z, IS_RETAILER, new Boolean(isRetailer));

		addToHashMap(z, ACT_BY, "SMS");
		addToHashMap(z, SUB_CLASS_TYPE, "DEFAULT");

		addToHashMap(z, SMS_SELECTED_BY, "SMS");
		addToHashMap(z, CLASS_TYPE, "DEFAULT");

		String rbtPromoActSubClass = getRbtPromoActSubClass(z, smsList,
				m_rbtSubKeyClassMap);
		if (rbtPromoActSubClass == null) {
			boolean isActivationRequest = isThisFeature(smsList, m_subMsg);
			addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(
					isActivationRequest));
			String yearlySubscriptionClass = getYearlySubscriptionClass(smsList);
			if (yearlySubscriptionClass != null) {
				addToHashMap(z, SUB_CLASS_TYPE, yearlySubscriptionClass
						.toUpperCase());
				addToHashMap(z, ACT_BY, "SMS-"
						+ yearlySubscriptionClass.toUpperCase());
			}
		} else {
			isThisFeature(smsList, m_subMsg);
			addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(true));
			addToHashMap(z, SUB_CLASS_TYPE, rbtPromoActSubClass.toUpperCase());
			String rbtPromoActKeyword = (String) getFromZTable(z,
					CUSTOMIZE_KEYWORD);
			addToHashMap(z, ACT_BY, "SMS-" + rbtPromoActKeyword.toUpperCase());
		}

		addToHashMap(z, "DAYS", "0");
		addToHashMap(z, IS_CTONE, new Boolean(false));

	}

	private String getRbtPromoActSubClass(HashMap z, ArrayList smsList,
			Map rbtSubKeyClassMap) {
		// String _method = "isThisRbtPromoActFeature()";
		// logger.info("******rbtSubKeyClassMap*********"
		// + rbtSubKeyClassMap);
		if (!isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return null;
		}
		if (smsList == null || rbtSubKeyClassMap == null)
			return null;
		Set actSubKeySet = rbtSubKeyClassMap.keySet();
		Iterator smsListIter = smsList.iterator();
		while (smsListIter.hasNext()) {
			String token = (String) smsListIter.next();
			if (actSubKeySet.contains(token)) {
				if (!m_profileList.contains(token))
					smsListIter.remove();
				addToHashMap(z, CUSTOMIZE_KEYWORD, token);
				return (String) rbtSubKeyClassMap.get(token);
			}
		}
		return null;
	}

	public int parseSMSText(String strSubID, String strSmsText,
			Hashtable reason, boolean isPrepaid, String strActInfo,
			String reqType) throws Exception {
		String _method = "parseSMSText()";
		logger.info("****** parameters are -- " + strSubID
				+ " & " + strSmsText + " & " + reason + " & " + isPrepaid
				+ " & " + strActInfo);

		// try
		// {
		Date today = Calendar.getInstance().getTime();
		if (m_last_lucene_update_date != null
				&& today.after(m_last_lucene_update_date)) {
			synchronized (m_reInit) {
				if (today.after(m_last_lucene_update_date)) {
					if (!m_thread_update_cache.isAlive()) {
						m_thread_update_cache = new Thread(this);
						m_contentRefreshed = false;
						System.out
								.println("Starting Thread m_last_lucene_update_date "
										+ m_last_lucene_update_date
										+ " today "
										+ today);
						m_thread_update_cache.start();
					}
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, 1);
					cal.set(Calendar.HOUR_OF_DAY, 3);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					m_last_lucene_update_date = cal.getTime();
				}
			}
		}

		HashMap z = new HashMap();
		setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
				STATUS_TECHNICAL_FAILURE);
		// strSubID = subID(strSubID).substring(0, 10);
		strSubID = subID(strSubID);
		addToHashMap(z, SMS_SUBSCRIBER_ID, strSubID);
		Connection conn = RBTDBManager.getInstance().getConnection();
		if (conn == null)
			addToHashMap(z, "CONNECTION_ERROR", "TRUE");

		Subscriber subscriber = RBTDBManager.getInstance()
				.getSubscriberForSMCallbacks(conn, strSubID);
		addToHashMap(z, SUBSCRIBER_OBJ, subscriber);

		addToHashMap(z, IS_PREPAID, new Boolean(isPrepaid));
		addToHashMap(z, ACT_INFO, strActInfo);
		addToHashMap(z, SMS_SELECTION_INFO, strActInfo);
		if (reason.containsKey("MODE")) {
			String thirdParty = "SMS:"
					+ ((String) reason.get("MODE")).trim().toUpperCase();
			addToHashMap(z, "THIRD_MODE", thirdParty);
		}
		
		if(reason.containsKey("TRX_ID")) 
            addToHashMap(z,"TRX_ID", (String)reason.get("TRX_ID")); 
		
		ArrayList smsList = Tools.tokenizeArrayList(strSmsText, " ");
		// logger.info("****** smsList1 is " + smsList);
		smsList = m_preProcessImpl.preProcess(z, smsList);
		// logger.info("****** smsList2 is " + smsList);
		if (reqType.equals("VALIDATE") || reqType.equals("SONGSET")) {
			String mode = null;
			int catID = -1;
			if (reason.containsKey("MODE")) {
				mode = (String) reason.get("MODE");
			}
			if (reason.containsKey("CAT_ID")) {
				String tmp = (String) reason.get("CAT_ID");
				try {
					catID = Integer.parseInt(tmp);
				} catch (Exception e) {
					catID = -1;
				}
			}

			if (mode != null)
				addToHashMap(z, SG_MODE, mode);
			if (catID != -1)
				addToHashMap(z, SG_CAT_ID, "" + catID);
		}
		// if (!reqType.equals("SMS"))
		if (!reqType.equals("SMS") && !(reqType.equals("SMS_PROFILE"))) {
			m_isActOptional = false;
			m_isActOptionalRequestRBT = false;

			if (reqType.equals("SONGSET")) {
				if (reason.containsKey("TRXID"))
					addToHashMap(z, TRANS_ID, (String) reason.get("TRXID"));

			}
		}
		// added by mohsin
		addToHashMap(z, REQUEST_TYPE, reqType);

		boolean ctOne = false;
		if (m_ctOneClassTypeValid)
			ctOne = isThisFeature(smsList, m_ctOneKeyword);
		if (smsList == null
				|| smsList.size() == 0
				|| (!ctOne && !checkRBTKeyword(smsList) && !checkRevRBTKeyword(
						z, smsList)) || strSubID == null) {
			// logger.info("RBT::returning
			// technical failure since length is less than 2 or RBT keyword
			// not present or subscriber id is null.");
			if (m_sendSMS)
				sendSMS(strSubID, getSMSTextForID(z, "HELP", m_helpDefault));
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			reason.put("Reason", (String) getFromZTable(z, RETURN_STRING));
			return Integer.parseInt((String) getFromZTable(z, RETURN_CODE));
		}

		initializeProcessing(z, smsList);
		if (m_isTataGSMImpl){
			if (getFromZTable(z,WDS_ALLOW)==null || !((String)getFromZTable(z,WDS_ALLOW)).equalsIgnoreCase("VALID")){
				reason.put("Reason", getSMSTextForID(z, "WDS_SUSPENDED", m_WDSSuspendedText));
				return STATUS_SUCCESS;
			}
		}
		// logger.info("****** smsList3 is " + smsList);
		String featureStr = (String) z.get(FEATURE);
		logger.info("****** feature  is "+ featureStr);
		String shortCode = (String)reason.get("SHORTCODE");
		if(shortCode != null && m_onlySearchShortCode != null && shortCode.trim().equalsIgnoreCase(m_onlySearchShortCode.trim()))
		{
			if (featureStr.equalsIgnoreCase("REQUEST") || featureStr.equalsIgnoreCase("REQUEST_MORE") || featureStr.equalsIgnoreCase("CATEGORY_SEARCH"))
			{}
			else
			{
				reason.put("Reason", getSMSTextForID(z, "HELP", m_helpDefault));
				return STATUS_SUCCESS;
			}
				
		}	
		addToHashMap(z,"SHORTCODE", shortCode); 
		// logger.info("****** searchOn is " +
		// (String)z.get(SEARCH_TYPE));
		if (subscriber != null
				&& subscriber.subYes() != null
				&& (subscriber.subYes().equals(STATE_SUSPENDED) || subscriber
						.subYes().equals(STATE_SUSPENDED_INIT))
				&& !((String) z.get(FEATURE)).equals("DEACTIVATION")) {
			reason.put("Reason", getSMSTextForID(z, "SUSPENDED",
					m_suspendedTextDefault));
			return STATUS_SUCCESS;
		}

		if (m_processBlackListTypes
				&& m_rbtDBManager.isTotalBlackListSub(strSubID)) {
			reason.put("Reason", getSMSTextForID(z, "TOTAL_BLACKLIST_MSG",
					m_totalBlackListTextDefault));
			return STATUS_NOT_AUTHORIZED;
		}

		if (ctOne)
			addToHashMap(z, IS_CTONE, new Boolean(true));
		boolean continueWithNormalFlow = true;
		if (((String) z.get(REQUEST_TYPE)).equals("SMS_PROFILE")) {
			// logger.info("inside request
			// type==sms_profile");
			boolean isValidProfileRequest = false;
			// logger.info("going to check if a valid
			// profile request");
			isValidProfileRequest = isValidProfileRequest(z);
			// Tools.logDetail(_class, _method,
			// "isValidProfileRequest"+isValidProfileRequest);
			if (!isValidProfileRequest) {
				// logger.info("not a Valid Profile
				// Request");
				// logger.info("returning with
				// sms=="+m_invalidProfileRreqestSMS);
				setReturnValues(z, m_invalidProfileRreqestSMS, STATUS_SUCCESS);
				if (m_sendSMS)
					sendSMS(strSubID, (String) z.get(RETURN_STRING));
				continueWithNormalFlow = false;
			}
		}
		if (continueWithNormalFlow) {

			if (((String) z.get(FEATURE)).equals("ACCEPT_RETAILER"))
				processACCEPT_RETAILER(z, smsList);
			else if (((String) z.get(FEATURE)).equals("ACCEPT_MGM"))
				processACCEPT_MGM(z, smsList);
			else if (((String) z.get(FEATURE)).equals("ACCEPT_VIRAL"))
				processACCEPT_VIRAL(z, smsList);
			else if (((String) z.get(FEATURE)).equals("PROMOTION1")) {
				addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(true));
				addToHashMap(z, SUCCESS_TEXT, getSMSTextForID(z,
						"PROMOTION1_SUCCESS", m_promotion1SuccessTextDefault));
				addToHashMap(z, FAILURE_TEXT, getSMSTextForID(z,
						"PROMOTION1_FAILURE", m_promotion1FailureTextDefault));
				handlePromotion(m_promotion1, z, smsList);
			} else if (((String) z.get(FEATURE)).equals("PROMOTION2")) {
				addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(true));
				addToHashMap(z, SUCCESS_TEXT, getSMSTextForID(z,
						"PROMOTION2_SUCCESS", m_promotion2SuccessTextDefault));
				addToHashMap(z, FAILURE_TEXT, getSMSTextForID(z,
						"PROMOTION2_FAILURE", m_promotion2FailureTextDefault));
				handlePromotion(m_promotion2, z, smsList);
			} else if (((String) z.get(FEATURE)).equals("SONG_PROMOTION1"))
				handlePromotion(m_songPromotion1, z, smsList);
			else if (((String) z.get(FEATURE)).equals("SONG_PROMOTION2"))
				handlePromotion(m_songPromotion2, z, smsList);
			else if (((String) z.get(FEATURE)).equals("REQUEST_MORE"))
				processREQUEST_MORE(z, smsList);
			else if (((String) z.get(FEATURE)).equals("REQUEST"))
				processREQUEST(z, smsList);
			else if (((String) z.get(FEATURE)).equals("CATEGORY_SEARCH"))
				processCATEGORY_SEARCH(z, smsList);
			else if (((String) z.get(FEATURE)).equals("COPY"))
				processCOPY(z, smsList);
			else if (((String) z.get(FEATURE)).equals("POLLON"))
				processPollON(z);
			else if (((String) z.get(FEATURE)).equals("SONGOFMONTH"))
				processSongOfMonth(z);
			else if (((String) z.get(FEATURE)).equals("POLLOFF"))
				processPollOFF(z);
			else if (((String) z.get(FEATURE)).equals("CANCEL_COPY"))
				processCancelCopyRequest(z);
			else if (((String) z.get(FEATURE)).equals("CONFIRM_COPY"))
				processConfirmCopyRequest(z);
			else if (((String) z.get(FEATURE)).equals("CANCEL_OPT_IN_COPY"))
				processCancelOptInCopyRequest(z);
			else if (((String) z.get(FEATURE)).equals("GIFT"))
				processGIFT(z, smsList);
			else if (((String) z.get(FEATURE)).equals("DEACT_DOWNLOAD"))
				processDeactivateDownload(z, smsList);
			else if (((String) z.get(FEATURE)).equals("FEED1"))
				processFEED1(z, smsList);
			else if (((String) z.get(FEATURE)).equals("TNB"))
				processTNB(z, smsList);
			else if (((String) z.get(FEATURE)).equals("REMOVE_SELECTION")) {

				if (!reqType.equals("VALIDATE"))
					removeCallerIDSel(z, smsList);
			} else if (((String) z.get(FEATURE)).equals("REMOVE_PROFILE"))
				removeTempOverride(z);
			else if (((String) z.get(FEATURE)).equals("SET_PROFILE"))
				setTempOverride(z, smsList);
			else if (((String) z.get(FEATURE)).equals("LISTPROFILE"))
				listTempOverride(z);
			else if (((String) z.get(FEATURE))
					.equals("WEEKLY_TO_MONTHLY_CONVERSION"))
				processWEEKLY_TO_MONTHLY_CONVERSION(z, smsList);
			else if (((Boolean) getFromZTable(z, IS_RETAILER)).booleanValue()
					&& m_retReqResponse && getFromZTable(z, CALLER_ID) != null)
				processRETAILER1(z, smsList);
			else if (((Boolean) getFromZTable(z, IS_RETAILER)).booleanValue()
					&& !m_retReqResponse && getFromZTable(z, CALLER_ID) != null)
				processRETAILER2(z, smsList);
			else if (((String) z.get(FEATURE)).equals("MGM") && m_mgmOn)
				processMGM(z, smsList);
			else if (((String) z.get(FEATURE)).equals("RENEW")
					&& m_model.equals("EsiaSMSImpl"))
				processRENEW(z, smsList);
			else if (((String) z.get(FEATURE)).equals("DEACTIVATION"))
				deactivate(z);
			else if (((String) z.get(FEATURE)).equals("DNAV"))
				removeNavraatri(z);
			else if (((String) z.get(FEATURE)).equals("HELP"))
				processHELP(z, smsList);
			else if (((String) z.get(FEATURE)).equals("LISTEN"))
				processLISTEN(z, smsList);
			else if (((String) z.get(FEATURE)).equals("MANAGE"))
				processMANAGE(z);
			else if (((String) z.get(FEATURE)).equals("DISABLE_INTRO"))
				processDisableIntro(z);
			else if (((String) z.get(FEATURE)).equals("DISABLE_OVERLAY"))
				processDisableOverlay(z);
			else if (((String) z.get(FEATURE)).equals("ENABLE_OVERLAY"))
				processEnableOverlay(z);
			else if (((String) z.get(FEATURE)).equals("SET_SELECTION1"))
				processSET_SELECTION1(z, smsList);
			else if (((String) z.get(FEATURE)).equals("SET_SELECTION2"))
				processSET_SELECTION2(z, smsList);
			else if (((String) z.get(FEATURE)).equals("SONG_CATCHER_ACCEPT"))
				processSONG_CATCHER_ACCEPT(z, smsList);
			else if (((String) z.get(FEATURE)).equals("DOWNLOADSLIST"))
				processDOWNLOADSLIST(z);
				else if (((String) z.get(FEATURE)).equals("SET_NEWSLETTER_ON"))
				processNewsletter(z, smsList,"ON");
			else if (((String) z.get(FEATURE)).equals("SET_NEWSLETTER_OFF"))
				processNewsletter(z, smsList,"OFF");
			else if (m_model.equalsIgnoreCase("AirtelSMSImpl")
					&& !((Boolean) getFromZTable(z, IS_ACTIVATION_REQUEST))
							.booleanValue())
				processCategoryListing(z, smsList);
			else
				processDEFAULT_CASE(z, smsList);
		}
		reason.put("Reason", (String) getFromZTable(z, RETURN_STRING));

		if (getFromZTable(z, SONG_SET_RESPONSE) != null)
			reason.put("SONG_SET_RESPONSE", (String) getFromZTable(z,
					SONG_SET_RESPONSE));
		if (getFromZTable(z, OCG_CHARGE_ID) != null)
			reason.put("OCG_CHARGE_ID",
					(String) getFromZTable(z, OCG_CHARGE_ID));
		String feature = (String) getFromZTable(z, FEATURE);
		reason.put("FEATURE", feature);
		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		if (!feature.equals("PROMOTION1") && !feature.equals("PROMOTION2")
				&& isActivationRequest) {
			if (getFromZTable(z, "CONNECTION_ERROR") != null)
				reason.put("ACTIVATION", "CONN_ERROR");
			else {
				reason.put("ACTIVATION", "TRUE");
				if (((String) getFromZTable(z, RETURN_STRING))
						.equals(getSMSTextForID(z, "ACTIVATION_FAILURE",
								m_activationFailureDefault)))
					reason.put("ACTIVATION_FAILED", "ALREADYACTIVE");
				else if (((String) getFromZTable(z, RETURN_STRING))
						.equals(getSMSTextForID(z,
								"ACTIVATION_ACT_PERIOD_FAILURE",
								m_activationFailureActDefault)))
					reason.put("ACTIVATION_FAILED", "DEACTIVATIONPENDING");
			}
		}
		if (feature.equalsIgnoreCase("DEACTIVATION")) {
			if (getFromZTable(z, "CONNECTION_ERROR") != null)
				reason.put("DEACTIVATION", "CONN_ERROR");
			else {
				reason.put("DEACTIVATION", "TRUE");
				if (((String) getFromZTable(z, RETURN_STRING))
						.equals(getSMSTextForID(z, "DEACTIVATION_FAILURE",
								m_deactivationFailureDefault)))
					reason.put("DEACTIVATION_FAILED", "NOTACTIVE");
				else if (((String) getFromZTable(z, RETURN_STRING))
						.equals(getSMSTextForID(z, "DEACTIVATION_FAILURE_ACT",
								m_deactivationFailureActDefault)))
					reason.put("DEACTIVATION_FAILED", "ACTIVATIONPENDING");
			}

		}
		return Integer.parseInt((String) getFromZTable(z, RETURN_CODE));
		// }
		// catch (Exception e)
		// {
		// logger.info("********** "
		// + getStackTrace(e));
		// throw new Exception();
		// }
	}

	private void processRENEW(HashMap z, ArrayList smsList) {
		// String _method = "processRENEW";
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		if (!m_useSubscriptionManager) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_SUCCESS);
			return;
		}
		if (smsList.size() != 1) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}

		// SubscriberStatus[] subscriberStatus =
		// getSubscriberRecords(subscriberID);
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		SubscriberStatus[] subscriberStatus = getSubscriberRecords(
				subscriberID, rbtType);
		if (subscriberStatus == null) {
			setReturnValues(z, getSMSTextForID(z, "RENEW_INVALID_REQUEST",
					m_renewInvalidRequestDefault), STATUS_SUCCESS);
			return;
		}
		String promoCode = (String) smsList.get(0);
		PromoMaster[] promoMasters = getPromoForCode(promoCode);
		if (promoMasters != null) {
			if (promoMasters.length == 1) {
				promoCode = promoMasters[0].clipID();
			} else {
				setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
						m_technicalFailureDefault), STATUS_SUCCESS);
				return;
			}
		}
		ClipMinimal clipMinimal = getClipPromoID(promoCode);
		boolean foundSelection = false;
		for (int subIndex = 0; subIndex < subscriberStatus.length; subIndex++) {
			// if (clipMinimal.getWavFile().equals(
			if (clipMinimal != null
					&& clipMinimal.getWavFile().equals(
							subscriberStatus[subIndex].subscriberFile())
					&& subscriberStatus[subIndex].selStatus().equals("B")
					&& (subscriberStatus[subIndex].classType()
							.equalsIgnoreCase("DEFAULT_WEEKLY_OPTIN") || subscriberStatus[subIndex]
							.classType().equalsIgnoreCase("DEFAULT_OPTIN"))) {
				foundSelection = true;
				if (clipMinimal.getEndTime().getTime() < System
						.currentTimeMillis()) {
					setReturnValues(z, getSMSTextForID(z, "RENEW_SONG_EXPIRED",
							m_renewSOngExpiredDefault), STATUS_SUCCESS);
					return;
				}
				smUpdateSelStatus(subscriberID, subscriberStatus[subIndex]
						.callerID(), subscriberStatus[subIndex]
						.subscriberFile(),
						subscriberStatus[subIndex].setTime(),
						subscriberStatus[subIndex].selStatus(), "R", rbtType);
				break;
			}
		}
		if (!foundSelection) {
			setReturnValues(z, getSMSTextForID(z, "RENEW_INVALID_CODE",
					m_renewInvalidCodeDefault), STATUS_SUCCESS);
			return;
		}
		setReturnValues(z, getSMSTextForID(z, "RENEW_SUCCESS",
				m_renewSuccessDefault), STATUS_SUCCESS);
	}

	private void processACCEPT_RETAILER(HashMap z, ArrayList smsList)
			throws Exception {
		// String _method = "processACCEPT_RETAILER";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		ViralSMSTable context = getViralSMSTable(subscriberID, "RETAILER");
		if (context == null || context.clipID() == null) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}

		String actInfo = (String) getFromZTable(z, ACT_INFO);
		// addToHashMap(z, CATEGORY_OBJECT, getCategory(24));
		Categories category = getCategory(24, getCircleID(subscriberID));
		addToHashMap(z, CATEGORY_OBJECT, category);

		StringTokenizer stk = new StringTokenizer(m_retActBy, ",");
		if (stk.hasMoreTokens())
			addToHashMap(z, ACT_BY, stk.nextToken());
		if (stk.hasMoreTokens())
			addToHashMap(z, SMS_SELECTED_BY, stk.nextToken());
		if (stk.hasMoreTokens())
			addToHashMap(z, SUB_CLASS_TYPE, stk.nextToken());
		if (!isSubClassTypeValid((String) z.get(SUB_CLASS_TYPE)))
			addToHashMap(z, SUB_CLASS_TYPE, "DEFAULT");

		actInfo = actInfo.substring(0, actInfo.indexOf(":") + 1)
				+ context.callerID();
		addToHashMap(z, ACT_INFO, actInfo);
		addToHashMap(z, SMS_SELECTION_INFO, actInfo);

		String token = null;
		StringTokenizer ret = new StringTokenizer(context.clipID(), " ");
		token = ret.nextToken();
		removeViralSMSTable(subscriberID, "RETAILER");
		if (token.equalsIgnoreCase("act")) {

			addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(true));
			if (!ret.hasMoreTokens() && !m_actAllowed) {
				setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
						m_retFailureTextDefault), STATUS_TECHNICAL_FAILURE);
				return;
			}
			// if (!handleActivation(z))
			if (!handleActivation(z, false)) {
				setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
						m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
				return;
			} else if (!ret.hasMoreTokens()) {
				Tools
						.sendSMS(m_smsNo, context.callerID(), getSubstituedSMS(
								getSMSTextForID(z, "RETAILER_RESP_SMS_ACCEPT",
										m_retRespSMSAcceptDefault),
								subscriberID, null), false);
				setReturnValues(z, getSubstituedSMS(m_retAccActSMSDefault, "",
						null), STATUS_SUCCESS);
				return;
			}
		}
		if (ret.hasMoreTokens())
			token = ret.nextToken();
		getCategoryAndClipForID(token, z);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		// Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (z.containsKey(CLASS_TYPE))
			z.remove(CLASS_TYPE);
		if (!handleSelection(z))
			return;

		String name = null;
		String endDate = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			if (clipMinimal.getEndTime() != null)
				endDate = clipMinimal.getEndTime().toString();

		}
		if (category != null && category.id() != 24)
			name = category.name();

		if (((Boolean) getFromZTable(z, IS_ACTIVATION_REQUEST)).booleanValue()) {
			if (name == null)
				setReturnValues(z, getSubstituedSMS(m_retAccActSMSDefault, "",
						null, endDate), STATUS_SUCCESS);
			else
				setReturnValues(z, getSubstituedSMS(m_retAccActSMSDefault,
						"with selection " + name, null, endDate),
						STATUS_SUCCESS); // made changes here
		} else {
			if (name == null) {
				setReturnValues(z, (String) getFromZTable(z, RETURN_STRING),
						STATUS_TECHNICAL_FAILURE);
				return;
			} else
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"RETAILER_ACCEPT_SEL", m_retAccSelSMSDefault), name,
						null, endDate), STATUS_SUCCESS);
		}

		Tools.sendSMS(m_smsNo, context.callerID(), getSubstituedSMS(
				getSMSTextForID(z, "RETAILER_RESP_SMS_ACCEPT",
						m_retRespSMSAcceptDefault), subscriberID, null), false);
	}

	private void processACCEPT_MGM(HashMap z, ArrayList smsList) {
		// String _method = "processACCEPT_MGM";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		ViralSMSTable context = getViralSMSTable(subscriberID, "MGM");
		if (context == null || context.clipID() == null) {
			setReturnValues(z, getSMSTextForID(z, "MGM_RECIPIENT_ACK_FAILURE",
					m_mgmRecAccFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}

		Categories category = getCategory(25, getCircleID(subscriberID));
		addToHashMap(z, CATEGORY_OBJECT, category);

		String actInfo = (String) getFromZTable(z, ACT_INFO);
		actInfo = actInfo.substring(0, actInfo.indexOf(":") + 1)
				+ context.callerID();
		addToHashMap(z, ACT_INFO, actInfo);
		addToHashMap(z, SMS_SELECTION_INFO, actInfo);
		addToHashMap(z, ACT_BY, m_mgmActBy.toUpperCase());

		String token = null;
		StringTokenizer ret = new StringTokenizer(context.clipID(), " ");
		token = ret.nextToken();
		if (token.equalsIgnoreCase("act")) {

			if (!ret.hasMoreTokens() && !m_actAllowed) {
				setReturnValues(z, getSMSTextForID(z, "MGM_SENDER_FAILURE",
						m_mgmSenderFailureTextDefault),
						STATUS_TECHNICAL_FAILURE);
				return;
			}
			// if (!handleActivation(z))
			if (!handleActivation(z, false)) {
				setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
						m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
				return;
			} else if (!ret.hasMoreTokens()) {
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"MGM_RECIPIENT_ACK", m_mgmRecAccSuccessDefault), "",
						null), STATUS_SUCCESS);
				return;
			}
		}

		if (ret.hasMoreTokens())
			token = ret.nextToken();
		getCategoryAndClipForID(token, z);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		// Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (z.containsKey(CLASS_TYPE))
			z.remove(CLASS_TYPE);
		if (!handleSelection(z))
			return;

		String endDate = null;
		String name = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			if (clipMinimal.getEndTime() != null)
				endDate = clipMinimal.getEndTime().toString();
		}
		if (category != null && category.id() != 25)
			name = category.name();

		if (name == null)
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_RECIPIENT_ACK", m_mgmRecAccSuccessDefault), "", null,
					endDate), STATUS_SUCCESS);
		else
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_RECIPIENT_ACK", m_mgmRecAccSuccessDefault),
					"with selection " + name, null, endDate), STATUS_SUCCESS);
	}

	private void processACCEPT_VIRAL(HashMap z, ArrayList smsList) {
		// String _method = "processACCEPT_VIRAL";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Categories category = getCategory(3, getCircleID(subscriberID));
		addToHashMap(z, CATEGORY_OBJECT, category);

		StringTokenizer st = new StringTokenizer(m_viralKey, ",");
		if (st.hasMoreTokens())
			st.nextToken();
		if (st.hasMoreTokens())
			addToHashMap(z, ACT_BY, st.nextToken());
		if (st.hasMoreTokens())
			addToHashMap(z, SMS_SELECTED_BY, st.nextToken());
		if (st.hasMoreTokens())
			addToHashMap(z, CLASS_TYPE, st.nextToken());
		if (st.hasMoreTokens()) {
			try {
				int categoryID = Integer.parseInt(st.nextToken());
				category = getCategory(categoryID, getCircleID(subscriberID));
				addToHashMap(z, CATEGORY_OBJECT, category);

			} catch (Exception e) {
				category = getCategory(3, getCircleID(subscriberID));
				addToHashMap(z, CATEGORY_OBJECT, category);
			}
		}

		ViralSMSTable context = getViralSMSTable(subscriberID, "BASIC");
		if ((context == null)
				|| (context.clipID() == null && m_isViralClipAllowed)) {
			setReturnValues(z, getSMSTextForID(z, "VIRAL_FAILURE",
					m_viralFailureTextDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		// if (!handleActivation(z))
		if (!handleActivation(z, false)) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (m_isViralClipAllowed && context.clipID() != null) {
			// Clips clip = getClip(Integer.parseInt(context.clipID()));
			// ClipMinimal clipMinimal = null;
			// if (clip != null)
			// clipMinimal = new ClipMinimal(clip);
			ClipMinimal clipMinimal = getClip(Integer
					.parseInt(context.clipID()));
			addToHashMap(z, CLIP_OBJECT, clipMinimal);
			handleSelection(z);
		}
		setReturnValues(z, getSMSTextForID(z, "VIRAL_SUCCESS",
				m_viralSuccessTextDefault), STATUS_SUCCESS);
	}

	private void processREQUEST(HashMap z, ArrayList smsList) {
		// String _method = "processREQUEST";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		if (isCorpSub(subscriberID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}

		if (smsList == null || smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "REQUEST_RBT_SMS1_FAILURE",
					m_requestRbtFailure1Default), STATUS_TECHNICAL_FAILURE);
			return;
		}
		int songNo = -1;
		try {
			songNo = Integer.parseInt((String) smsList.get(0));
			if (((Boolean) z.get(IS_RETAILER)).booleanValue())
				setReturnValues(z, getSMSTextForID(z,
						"REQUEST_RBT_SET_FAILURE", m_reqSetRetFailureDefault),
						STATUS_TECHNICAL_FAILURE);
			else
				setRequest(z, songNo, "REQUEST");
		} catch (Exception e) {
			songNo = -1;
		}
		if (songNo == -1) {
			String searchString = " ";
			String tmp = null;
			for (int k = 0; k < smsList.size(); k++) {
				tmp = (String) smsList.get(k);
				tmp = replaceSpecialChars(tmp);
				if (tmp.trim().length() > 0)
					searchString = searchString.trim() + " " + tmp.trim();
			}
			addToHashMap(z, SEARCH_STRING, searchString);
			searchRequest(z, smsList);
		}
	}

	private void processCATEGORY_SEARCH(HashMap z, ArrayList smsList) {
		// String _method = "processCATEGORY_SEARCH";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		if (isCorpSub(subscriberID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}

		if (smsList == null || smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
			return;
		}

		int songNo = -1;
		try {
			songNo = Integer.parseInt((String) smsList.get(0));
			if (((Boolean) z.get(IS_RETAILER)).booleanValue())
				setReturnValues(z, getSMSTextForID(z,
						"REQUEST_RBT_SET_FAILURE", m_reqSetRetFailureDefault),
						STATUS_TECHNICAL_FAILURE);
			else
				setRequest(z, songNo, "CATEGORY");
		} catch (Exception e) {
			songNo = -1;
		}
		if (songNo == -1)
			searchCategory(z, (String) smsList.get(0));
	}

	private void processREQUEST_MORE(HashMap z, ArrayList smsList) {
		// String _method = "processREQUEST_MORE";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);
		if (smsList.size() > 0
				&& isThisFeature(getArrayList((String) smsList.get(0)),
						m_catRBTkeyword))
			getMoreClips(z, "CATEGORY");
		else
			getMoreClips(z, "REQUEST");
	}

	private void processTNB(HashMap z, ArrayList smsList) {
		// String _method = "processTNB";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);
		boolean update = updateTNBSubscribertoNormal((String) z
				.get(SMS_SUBSCRIBER_ID));
		if (update)
			setReturnValues(z, getSMSTextForID(z, "TNB_SUCCESS",
					m_tnbSuccessSMSDefault), STATUS_SUCCESS);
		else
			setReturnValues(z, getSMSTextForID(z, "TNB_FAILURE",
					m_tnbFailureSMSDefault), STATUS_TECHNICAL_FAILURE);
	}

	public void processGIFT(HashMap z, ArrayList smsList) {
		// String _method = "processGIFT()";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		if (smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "GIFT_HELP",
					m_giftHelpDefault), STATUS_SUCCESS);
			return;
		}
		if (!isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		
		String canBeGifted = m_rbtDBManager.canBeGifted(subscriberID, callerID, null);
		if(canBeGifted != null && canBeGifted.equalsIgnoreCase(GIFT_FAILURE_GIFTEE_INVALID))
		{
			setReturnValues(z, getSMSTextForID(z, "GIFT_MOBILE_FAILURE",
					m_giftMobileFailureDefault), STATUS_SUCCESS);
			return;
		}
		
		if (!m_allowInactUserGift && (subscriber == null || !isSubActive(subscriber, z))) {
			setReturnValues(z, getSMSTextForID(z, "GIFT_INACTIVE_GIFTER",
					m_giftInactiveGifterDefault), STATUS_SUCCESS);
			return;
		}

		String token = (String) smsList.get(0);
		getCategoryAndClipForPromoID(token, z, false);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (clipMinimal == null) {
			setReturnValues(z, getSMSTextForID(z, "GIFT_CODE_FAILURE",
					m_giftCodeFailureDefault), STATUS_SUCCESS);
			return;
		}

		if (m_model.equals("AirtelSMSImpl")) {
			boolean invalid = false;
			if (!isValidSub(callerID) && !m_rbtDBManager.isValidOperatorPrefix(callerID)) {
				Parameters nonOMPrefixParam = CacheManagerUtil.getParametersCacheManager().getParameter(
						"GATHERER", "NON_ONMOBILE_PREFIX");
				// String telPrefix =
				// m_rbtDBManager.subID(callerID).substring(0, 4);
				int prefixIndex = RBTDBManager.getInstance()
						.getPrefixIndex();
				String telPrefix = m_rbtDBManager.subID(callerID).substring(0,
						prefixIndex);
				if (nonOMPrefixParam == null
						|| nonOMPrefixParam.getValue().indexOf(telPrefix) == -1)
					invalid = true;
			}
			if (callerID == null || callerID.equalsIgnoreCase(subscriberID)
					|| invalid) {
				setReturnValues(z, getSMSTextForID(z, "GIFT_MOBILE_FAILURE",
						m_giftMobileFailureDefault), STATUS_SUCCESS);
				return;
			}
		} else if (callerID == null
				|| callerID.equalsIgnoreCase(subscriberID)
				|| (!m_giftNational && !(isValidSub(callerID)))
				|| (m_giftNational && (!m_rbtDBManager
						.isValidOperatorPrefix(callerID)))) {
			setReturnValues(z, getSMSTextForID(z, "GIFT_MOBILE_FAILURE",
					m_giftMobileFailureDefault), STATUS_SUCCESS);
			return;
		}
		String clipID = null;
		String gift = null;
		String endDate = null;
		if (category != null) {
			gift = category.name();
			clipID = "C" + category.id();
		} else if (clipMinimal != null) {
			gift = clipMinimal.getClipName();
			clipID = "" + clipMinimal.getClipId();
			if (clipMinimal.getEndTime() != null) {
				endDate = clipMinimal.getEndTime().toString();
			}
		}

		insertViralSMSTable(subscriberID, callerID, clipID, "GIFT", 0);
		setReturnValues(z, getSubstituedSMS(getSMSTextForID(z, "GIFT_SUCCESS",
				m_giftSuccessDefault), gift, callerID, endDate), STATUS_SUCCESS);
	}

	private void processWEEKLY_TO_MONTHLY_CONVERSION(HashMap z,
			ArrayList smsList) {
		// String _method = "processWEEKLY_TO_MONTHLY_CONVERSION()";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!m_useSubscriptionManager) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_SUCCESS);
			return;
		}

		/*
		 * SubscriberStatus[] sel = getTypeSelections(subscriberID, "WEEKLY");
		 * if (sel != null) { convertSelectionClassType(subscriberID, "WEEKLY",
		 * "MONTHLY"); setReturnValues(z, getSMSTextForID(z,
		 * "WEEKLY_TO_MONTHLY_CONVERSION_SUCCESS",m_weeklyToMonthlyConversionSuccessDefault),
		 * STATUS_SUCCESS); } else setReturnValues(z,
		 * getSMSTextForID(z,"WEEKLY_TO_MONTHLY_CONVERSION_FAILURE",m_weeklyToMonthlyConversionFailureDefault),
		 * STATUS_NOT_AUTHORIZED);
		 */
		boolean success = false;
		Iterator itChargeMap = null;
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		if (conversionTableChargeClass != null
				&& conversionTableChargeClass.size() > 0) {
			itChargeMap = conversionTableChargeClass.keySet().iterator();
			String initClass = null;
			String finalClass = null;
			while (itChargeMap.hasNext()) {
				initClass = (String) itChargeMap.next();
				finalClass = (String) conversionTableChargeClass.get(initClass);
				if (convertWeeklySelectionsClassTypeToMonthly(subscriberID,
						initClass, finalClass, rbtType))
					success = true;
			}
		}
		if (success)
			setReturnValues(z, getSMSTextForID(z,
					"WEEKLY_TO_MONTHLY_CONVERSION_SUCCESS",
					m_weeklyToMonthlyConversionSuccessDefault), STATUS_SUCCESS);
		else
			setReturnValues(z, getSMSTextForID(z,
					"WEEKLY_TO_MONTHLY_CONVERSION_FAILURE",
					m_weeklyToMonthlyConversionFailureDefault),
					STATUS_NOT_AUTHORIZED);
	}

	private void processRETAILER2(HashMap z, ArrayList smsList) {
		// String _method = "processRETAILER2";
		// //logger.info("******** parameters are "+z +" &
		// "+smsList );

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		z.remove(CALLER_ID);
		Subscriber caller = getSubscriber(callerID);
		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		if (isCorpSub(callerID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (!isValidSub(callerID)) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_NOT_AUTHORIZED);
			return;
		}
		if (isActivationRequest && isSubActive(caller, z)) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_SUCCESS);
			return;
		}
		if (!isActivationRequest && !isSubActive(caller, z) && !m_isActOptional) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_SUCCESS);
			return;
		}
		addToHashMap(z, SMS_SUBSCRIBER_ID, callerID);
		z.remove(SUBSCRIBER_OBJ);
		addToHashMap(z, SUBSCRIBER_OBJ, caller);
		String actInfo = (String) getFromZTable(z, ACT_INFO);
		actInfo = actInfo.substring(0, actInfo.indexOf(":") + 1) + ":RET:" + subscriberID;
		addToHashMap(z, ACT_INFO, actInfo);
		addToHashMap(z, SMS_SELECTION_INFO, actInfo);
		addToHashMap(z, SMS_SELECTED_BY, m_retActBy);
		StringTokenizer stk = new StringTokenizer(m_retActBy, ",");
		if (stk.hasMoreTokens())
			addToHashMap(z, ACT_BY, stk.nextToken());
		if (stk.hasMoreTokens())
			addToHashMap(z, SMS_SELECTED_BY, stk.nextToken());

		if (isActivationRequest || (!isSubActive(caller, z) && m_isActOptional))
			// if (!handleActivation(z))
			if (!handleActivation(z, false))
				return;

		String token = null;
		if (smsList.size() > 0)
			token = (String) smsList.get(0);
		getCategoryAndClipForPromoID(token, z, false);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (!isActivationRequest && clipMinimal == null) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}
		if (!handleSelection(z) && !isActivationRequest)
			return;

		String name = "";
		String endDate = "";
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			if (clipMinimal.getEndTime() != null) {
				endDate = clipMinimal.getEndTime().toString();
			}
		}
		if (category != null && category.id() != 3)
			name = category.name();

		if (isActivationRequest)
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"RETAILER_SUCCESS", m_retSuccessTextDefault), callerID,
					null, endDate), STATUS_SUCCESS);
		else
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"RETAILER_SONG_SUCCESS", m_retSongSuccessTextDefault),
					callerID, name, endDate), STATUS_SUCCESS);

	}

	private void processDEFAULT_CASE(HashMap z, ArrayList smsList) {
		// String _method = "processDEFAULT_CASE()";
		// //logger.info("******** parameters are "+z +" &
		// "+smsList );

		// logger.info("!!!!!!! class_type is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		if (isCorpSub((String) z.get(SMS_SUBSCRIBER_ID), z)
				&& m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}

		String reqType = (String) getFromZTable(z, REQUEST_TYPE);
		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		if (isActivationRequest && isSubActive(subscriber, z)
				&& !m_allowReactivation) {
			if (!reqType.equals("VALIDATE")
					|| (reqType.equals("VALIDATE") && (subscriber.subYes()
							.equals("B") || subscriber.subYes().equals("O")))) {
				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_FAILURE",
						m_activationFailureDefault), STATUS_SUCCESS);
				return;
			}
		}
		
		if (!isActivationRequest && !isSubActive(subscriber, z)
				&& !m_isActOptional) {
			logger.info("I m in help Keyword");
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_SUCCESS);
			return;
		}
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (!isActivationRequest && m_model.equals("AirtelSMSImpl")
				&& callerID != null && maxCallerIDSelectionsAllowed > 0) {
			SubscriberStatus[] ssCallerId = getOtherCallerIDSelections(
					subscriberID, callerID, rbtType);
			String callerIDNumbers = "";
			int countCaller = 0;
			for (int i = 0; ssCallerId != null && i < ssCallerId.length; i++) {
				String callerI = ssCallerId[i].callerID();
				if (callerI != null && callerIDNumbers.indexOf(callerI) == -1) {
					callerIDNumbers += ", " + ssCallerId[i].callerID();
					countCaller++;
				}
			}
			if (callerIDNumbers.length() > 2)
				callerIDNumbers = callerIDNumbers.substring(2);
			if (countCaller >= maxCallerIDSelectionsAllowed) {
				setReturnValues(z,
						getSubstituedSMS(getSMSTextForID(z,
								"CALLER_ID_SELECTION_BLOCK",
								m_callerIdSelectionBlockDefault),
								callerIDNumbers, null), STATUS_SUCCESS);
				return;
			}
		}

		String promoType = getPromoType(smsList);
		addToHashMap(z, PROMO_TYPE, promoType);
		String token = null;

		if (smsList.size() > 0)
			token = (String) smsList.get(0);

		if (m_model.equals("EsiaSMSImpl") && promoType == null) {
			promoType = "ESIA";
			addToHashMap(z, PROMO_TYPE, promoType);
		}

		// logger.info("!!!!!!!2 class_type is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		ClipMinimal clipMinimal = null;
		if (m_model.equals("EsiaSMSImpl") && token != null) {
			String selectionInfo = (String) getFromZTable(z, SMS_SELECTION_INFO);
			if (selectionInfo != null)
				selectionInfo += ":" + promoType;
			addToHashMap(z, SMS_SELECTION_INFO, selectionInfo);
			if (!promoType.equals("ESIA"))
				token = getBasicPromoId(token, promoType);
			if (!getCategoryAndClipForPromoID(token, z, false))
				return;
			clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
			if (!z.containsKey(SMS_CHARGING_MODEL))
				addToHashMap(z, SMS_CHARGING_MODEL, m_chargingCycle);
			if (!z.containsKey(SUBSCRIPTION_TYPE))
				addToHashMap(z, SUBSCRIPTION_TYPE, m_subscriptionType);
			String chargingModel = (String) getFromZTable(z, SMS_CHARGING_MODEL);
			String subscriptionType = (String) getFromZTable(z,
					SUBSCRIPTION_TYPE);
			// if (clipMinimal != null && clipMinimal.getGrammar() != null
			// && clipMinimal.getGrammar().equalsIgnoreCase("UGC"))
			// subscriptionType = m_UGCsubscriptionType;
			String type = promoType + " " + chargingModel + " "
					+ subscriptionType;
			addToHashMap(z, REGEX_TYPE, type);
		} else if (m_model.equals("EsiaSMSImpl") && token == null) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_SUCCESS);
			return;
		} else {
			if (!getCategoryAndClipForPromoID(token, z, false))
				return;
			clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		}

		// Added to make sure ESIA non-active subscribers cannot activate if a
		// trial song is sent
		if (m_model.equals("EsiaSMSImpl")
				&& clipMinimal != null
				&& clipMinimal.getClassType() != null
				&& clipMinimal.getClassType().startsWith("TRIAL")
				&& (m_TrialWithActivations == null || !m_TrialWithActivations
						.contains(clipMinimal.getClassType()))) {
			if (!isSubActive(subscriber, z)) {
				setReturnValues(z, getSMSTextForID(z, "ACT_TRIAL_FAILURE",
						m_actTrialFailureDefault), STATUS_NOT_AUTHORIZED);
				return;
			}
		}
		if (m_model.equals("EsiaSMSImpl") && clipMinimal != null
				&& clipMinimal.getClassType() != null
				&& clipMinimal.getClassType().startsWith("TRIAL")
				&& m_TrialWithActivations != null
				&& m_TrialWithActivations.contains(clipMinimal.getClassType())) {
			SubscriberPromo sPromo = getSubscriberPromo(subscriberID, "TRIAL");
			if (sPromo != null) {
				String clipClassType = clipMinimal.getClassType();
				Integer trialIntObj = (Integer) m_trialClassDaysMap
						.get(clipClassType);
				int trialInt = trialIntObj.intValue();
				Date trialDate = sPromo.startDate();
				Calendar calTrial = Calendar.getInstance();
				calTrial.setTime(trialDate);
				calTrial.add(Calendar.DATE, trialInt);
				Date finalDate = calTrial.getTime();
				SubscriberStatus ss = m_rbtDBManager.smSubscriberSelections(
						subscriberID, callerID, 1, rbtType);
				if (ss != null && ss.classType() != null
						&& m_TrialWithActivations.contains(ss.classType())) {
					;// do nothing
				} else if (Calendar.getInstance().getTime().before(finalDate)/*
																				 * &&
																				 * !isSubActive(subscriber)
																				 */) {
					setReturnValues(z,
							getSMSTextForID(z, "TRIAL_REPEAT_FAILURE",
									m_repeatTrialFailureDefault),
							STATUS_NOT_AUTHORIZED);
					return;
				}
			} else
				createSubscriberPromo(subscriberID, 0, false, "SMS", "TRIAL");
		}
		logger.info("The value of clipMinimal - "+clipMinimal);
		if (clipMinimal == null && (!isActivationRequest || !m_actAllowed)) {
			setReturnValues(z, getSMSTextForID(z, "PROMO_ID_FAILURE",
					m_promoIDFailureDefault), STATUS_NOT_AUTHORIZED);
			return;
		}

		String act_by = (String) getFromZTable(z, ACT_BY);
		String featureStr = (String) z.get(FEATURE);
		if (isActivationRequest && clipMinimal != null) {

			if (m_promoActBy != null) {
				addToHashMap(z, ACT_BY, m_promoActBy);
				addToHashMap(z, SMS_SELECTED_BY, m_promoActBy);
			}
			if (m_promoActBy == null && act_by.equals("SMS")) {
				addToHashMap(z, ACT_BY, "PROMO");
				addToHashMap(z, SMS_SELECTED_BY, "PROMO");
			} else if (!act_by.equals("SMS") && !act_by.startsWith("SMS:")) {
				String newActAndSelBy = "PROMO-";
				if (getFromZTable(z, CUSTOMIZE_KEYWORD) != null)
					newActAndSelBy += ((String) getFromZTable(z,
							CUSTOMIZE_KEYWORD)).toUpperCase();
				addToHashMap(z, ACT_BY, newActAndSelBy);
				addToHashMap(z, SMS_SELECTED_BY, newActAndSelBy);
			}
		}
		if (featureStr != null
				&& featureStr.equalsIgnoreCase("SONG_CATCHER_ACCEPT")) {
			addToHashMap(z, ACT_BY, "SMS-SC");
			addToHashMap(z, SMS_SELECTED_BY, "SMS-SC");
		}

		String thirdParty = (String) getFromZTable(z, "THIRD_MODE");
		if (thirdParty != null && thirdParty.length() > 0) {
			addToHashMap(z, ACT_BY, thirdParty);
			addToHashMap(z, SMS_SELECTED_BY, thirdParty);
		}
		// logger.info("!!!!!!! class_type3 is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		if (isActivationRequest || !isSubActive(subscriber, z))
			// if (!handleActivation(z))
			if (!handleActivation(z, true))
				return;

		// logger.info("!!!!!!! class_type4 is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		if (z.containsKey(CLASS_TYPE))
			z.remove(CLASS_TYPE);
		if (featureStr != null
				&& featureStr.equalsIgnoreCase("SONG_CATCHER_ACCEPT"))
			addToHashMap(z, CLASS_TYPE, m_SongCatcherClassType);
		// logger.info("!!!!!!! class_type5 is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		if (!handleSelection(z))
			if (!isActivationRequest)
				return;

		// String callerID = (String) getFromZTable(z, CALLER_ID);

		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (callerID == null)
			callerID = m_smsTextForAll;
		String name = null;
		String endDate = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			if (clipMinimal.getEndTime() != null) {
				endDate = clipMinimal.getEndTime().toString();
			}
		}
		if (category != null && category.id() != 3)
			name = category.name();

		if (isActivationRequest && clipMinimal == null) {
			setReturnValues(z, getSMSTextForID(z, "ACTIVATION_SUCCESS",
					m_activationSuccessDefault), STATUS_SUCCESS);
			return;
		} else if (isActivationRequest) {
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"ACTIVATION_PROMO_SUCCESS", m_actPromoSuccessTextDefault),
					clipMinimal.getClipName(), callerID, endDate),
					STATUS_SUCCESS);
			return;
		}
		if (giveUGSsongList && m_rbtDBManager.allowLooping()
				&& m_rbtDBManager.isDefaultLoopOn()) {
			SubscriberStatus[] ssUGS = getSubscriberCallerSelectionsInLoop(
					subscriberID, (String) getFromZTable(z, CALLER_ID), rbtType);
			if (ssUGS != null && ssUGS.length > 1) {
				String songList = "";
				for (int i = 0; i < ssUGS.length - 1; i++) {
					ClipMinimal clip = null;
					String clipName = null;
					endDate = null;
					if (ssUGS[i].subscriberFile() != null)
						clip = m_rbtDBManager.getClipRBT(ssUGS[i]
								.subscriberFile().trim());
					if (clip != null) {
						clipName = clip.getClipName().trim();
						if (clipMinimal.getEndTime() != null) {
							endDate = clipMinimal.getEndTime().toString();
						}
					}
					if (clipName != null && clipName.length() > 0
							&& songList.indexOf(clipName) == -1)
						songList += ", " + clipName;
				}
				z.put(RETURN_STRING, getSubstituedSMS(getSMSTextForID(z,
						"PROMO_ID_SUCESS_UGS_LIST",
						m_promoSuccessWithUGSSongListTextDefault), name,
						callerID, endDate)
						+ " " + songList.substring(2) + ".");
				return;

			}
		}

		z.put(RETURN_STRING, getSubstituedSMS(getSMSTextForID(z,
				"PROMO_ID_SUCCESS", m_promoSuccessTextDefault), name, callerID,
				endDate));
	}

	private void processMGM(HashMap z, ArrayList smsList) throws Exception {
		// String _method = "processMGM";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		Subscriber caller = getSubscriber(callerID);

		if (isCorpSub(callerID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (callerID == null || !isValidSub(callerID) || isSubActive(caller, z)) {
			setReturnValues(z, getSMSTextForID(z, "MGM_SENDER_FAILURE",
					m_mgmSenderFailureTextDefault), STATUS_NOT_AUTHORIZED);
			return;
		}

		if (!isSubActiveForDays(subscriberID, m_mgmMinNoDaysActive)) {
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_SENDER_MIN_ACT_FAILURE",
					m_mgmSenderMinActFailureTextDefault), ""
					+ m_mgmMinNoDaysActive, null), STATUS_NOT_AUTHORIZED);
			return;
		}

		if (isSubMoreGifts(subscriberID, m_mgmMaxGiftsMonth)) {
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_SENDER_MAX_GIFT_FAILURE",
					m_mgmSenderMaxGiftFailureTextDefault), ""
					+ m_mgmMaxGiftsMonth, null), STATUS_NOT_AUTHORIZED);
			return;
		}

		String token = null;
		if (smsList.size() > 0)
			token = (String) smsList.get(0);
		getCategoryAndClipForPromoID(token, z, false);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		String contextClipID = "act ";

		String name = null;
		String id = null;
		String endDate = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			id = "" + clipMinimal.getClipId();
			if (clipMinimal.getEndTime() != null) {
				endDate = clipMinimal.getEndTime().toString();
			}

		}
		if (category != null) {
			name = category.name();
			id = "C" + category.id();
		}

		if (id == null) {
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_SENDER_SUCCESS", m_mgmSenderSuccessTextDefault),
					callerID, null, endDate), STATUS_SUCCESS);
			Tools.sendSMS(m_smsNo, callerID, getSubstituedSMS(getSMSTextForID(
					z, "MGM_RECIPIENT", m_mgmRecSMSDefault), "", subscriberID),
					false);
		} else {
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"MGM_SENDER_SUCCESS", m_mgmSenderSuccessTextDefault),
					callerID + " with selection " + name, null, endDate),
					STATUS_SUCCESS);
			Tools.sendSMS(m_smsNo, callerID, getSubstituedSMS(getSMSTextForID(
					z, "MGM_RECIPIENT", m_mgmRecSMSDefault), " with selection "
					+ name, subscriberID, endDate), false);
			contextClipID += id;
		}
		insertViralSMSTable(callerID, subscriberID, contextClipID, "MGM", 1);
	}

	private void processRETAILER1(HashMap z, ArrayList smsList)
			throws Exception {
		// String _method = "processRETAILER";
		// //logger.info("******** parameters are "+z + " &
		// "+ smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		Subscriber caller = getSubscriber(callerID);

		if (isCorpSub(callerID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (!isValidSub(callerID)) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_NOT_AUTHORIZED);
			return;
		}
		if (isSubActive(caller, z)
				&& ((Boolean) getFromZTable(z, IS_ACTIVATION_REQUEST))
						.booleanValue()) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_NOT_AUTHORIZED);
			return;
		}
		if (!isSubActive(caller, z)
				&& !((Boolean) getFromZTable(z, IS_ACTIVATION_REQUEST))
						.booleanValue() && !m_isActOptional) {
			setReturnValues(z, getSMSTextForID(z, "RETAILER_FAILURE",
					m_retFailureTextDefault), STATUS_NOT_AUTHORIZED);
			return;
		}

		String token = null;
		if (smsList.size() > 0)
			token = (String) smsList.get(0);
		getCategoryAndClipForPromoID(token, z, false);
		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		boolean isActRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		if (!isActRequest && clipMinimal == null) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}

		String name = null;
		String id = null;
		String endDate = null;
		name = null;
		id = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			id = "" + clipMinimal.getClipId();
			if (clipMinimal.getEndTime() != null)
				endDate = clipMinimal.getEndTime().toString();

		}
		if (category != null) {
			name = category.name();
			id = "C" + category.id();
		}

		removeViralSMSTable(callerID, "RETAILER");

		String contextClipID = "";
		if (isActRequest) {
			contextClipID = "act ";
			if (id != null)
				contextClipID += id;
			Tools.sendSMS(m_smsNo, callerID, getSMSTextForID(z,
					"RETAILER_RESP_SMS_ACT", m_retReqResActDefault), false);
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"RETAILER_SUCCESS", m_retSuccessTextDefault), callerID,
					null, endDate), STATUS_SUCCESS);
		} else if (id != null) {
			contextClipID = id;
			Tools.sendSMS(m_smsNo, callerID, getSubstituedSMS(getSMSTextForID(
					z, "RETAILER_RESP_SMS_SEL", m_retReqResSelDefault), name,
					null), false);
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"RETAILER_SONG_SUCCESS", m_retSongSuccessTextDefault),
					callerID, name, endDate), STATUS_SUCCESS);
		}
		insertViralSMSTable(callerID, subscriberID, contextClipID, "RETAILER",
				1);
	}

	// private boolean handleActivation(HashMap z)
	private boolean handleActivation(HashMap z, boolean isDirectActivation) {
		// String _method = "handleActivation()";
		// //logger.info("****** parameters are -- "+z);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String req = (String) getFromZTable(z, REQUEST_TYPE);
		 String trxID = (String) getFromZTable(z, "TRX_ID"); 
		String actInfo = (String) getFromZTable(z, ACT_INFO);
		if(trxID != null) 
			actInfo = actInfo + ":trxid:"+trxID+":"; 

		String subClassType = (String) getFromZTable(z, SUB_CLASS_TYPE);
		if (m_yearlySubscription.contains(subClassType.toUpperCase()))
			addToHashMap(z, ACT_BY, "SMS-" + subClassType.toUpperCase());
		String actBy = (String) getFromZTable(z, ACT_BY);
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID))
				.booleanValue();

		if (!req.equalsIgnoreCase("VALIDATE") || !isDirectActivation)
			isDirectActivation = false;

		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		int days = Integer.parseInt((String) getFromZTable(z, DAYS));

		boolean isDirectDeactivations = false;
		if (req != null && req.equals("VALIDATE"))
			isDirectDeactivations = true;
		
		String extraInfo=null;
		if (getFromZTable(z,"WDS_RESULT")!=null){
			extraInfo=(String)getFromZTable(z,"WDS_RESULT");
		}

		boolean success = false;
		if (((Boolean) getFromZTable(z, IS_CTONE)).booleanValue()
				&& subClassType.equalsIgnoreCase("DEFAULT"))
			subClassType = "CTONE";
		if (subscriber == null || !isSubActive(subscriber, z)) {
			if (subscriber != null
					&& minActivationPeriodDisqualification(m_activationPeriod,
							subscriber)) {
				setReturnValues(z, getSMSTextForID(z,
						"ACTIVATION_ACT_PERIOD_FAILURE",
						m_activationFailureActDefault),
						STATUS_TECHNICAL_FAILURE);
				return false;
			}

			if (getFromZTable(z, SG_MODE) != null && isDirectActivation)
				actBy = (String) getFromZTable(z, SG_MODE);

			subscriber = activateSubscriber(subscriberID, actBy, null,
					isPrepaid, days, actInfo, subClassType, isDirectActivation,
					rbtType);
			if (m_isTataGSMImpl){
				CosDetails cos=null;
				isPrepaid=((Boolean) getFromZTable(z, IS_PREPAID))
				.booleanValue();
				String prepaidYes="y";
				if (!isPrepaid)
					prepaidYes="n";
				cos=m_rbtDBManager.getCos(subscriberID, getFromZTable(z,CIRCLE_ID)!=null?(String)getFromZTable(z,CIRCLE_ID):null,prepaidYes, actBy);
				HashMap extraInfoMap=new HashMap();
				extraInfoMap.put(EXTRA_INFO_WDS_QUERY_RESULT,(String) getFromZTable(z,EXTRA_INFO_WDS));
				//RBT-9873 Added null for xtraParametersMap for CG flow
				subscriber=m_rbtDBManager.activateSubscriber(
						subscriberID,actBy, null, null, isPrepaid, m_activationPeriod, days,
						actInfo, subClassType, true, cos, false, rbtType,extraInfoMap, null, null, false, null);
			}
			if (subscriber == null) {
				// //logger.info("RBT::unable to
				// activate the subscriber " +subscriberID);
				setReturnValues(z, getSMSTextForID(z,
						"ACTIVATION_ACT_PERIOD_FAILURE",
						m_activationFailureActDefault),
						STATUS_TECHNICAL_FAILURE);
				return false;
			} else {
				success = true;
				if (m_sendSMS)
					sendSMS(subscriberID, getSMSTextForID(z,
							"ACTIVATION_SUCCESS", m_activationSuccessDefault));
				// //logger.info("RBT::activated the
				// subscriber " +subscriberID);
				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_SUCCESS",
						m_activationSuccessDefault), STATUS_SUCCESS);
			}
		} else {
			if (m_allowReactivation/*
									 * && subscriber.subscriptionClass() != null &&
									 * subscriber.subscriptionClass()
									 * .equalsIgnoreCase("DEFAULT")
									 */) {
				reactivateSubscriber(subscriberID, actBy, isPrepaid, true,
						actInfo, isDirectDeactivations, rbtType);
				success = true;
				// //logger.info("RBT::reactivated the
				// subscriber " +subscriberID);
				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_SUCCESS",
						m_activationSuccessDefault), STATUS_SUCCESS);
			} else {
				// //logger.info("RBT::failed to
				// activate the subscriber " +subscriberID);

				if (!subscriber.subYes().equals("B")
						&& !subscriber.subYes().equals("O") && req != null
						&& req.equals("VALIDATE")) {
					String ret = smSubscriptionSuccess(subscriberID, "DEFAULT");
					if (ret != null && ret.equalsIgnoreCase("SUCCESS")) {
						smDeactivateAllSelections(subscriberID);
						setReturnValues(z, getSMSTextForID(z,
								"ACTIVATION_SUCCESS",
								m_activationSuccessDefault), STATUS_SUCCESS);
						return true;
					}
				}

				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_FAILURE",
						m_activationFailureDefault), STATUS_ALREADY_ACTIVE);
				return false;
			}
		}
		addToHashMap(z, SUBSCRIBER_OBJ, subscriber);
		return success;
	}

	private void smDeactivateAllSelections(String strSubID) {
		RBTDBManager.getInstance().smDeactivateAllSelections(strSubID);
	}

	/*
	 * private void smUpdateSelStatusSubscriptionSuccess(String strSubID) {
	 * RBTDBManager.getInstance()
	 * .smUpdateSelStatusSubscriptionSuccess(strSubID, true); }
	 */
	// Added ExtraInfo - TRAI changes
	private String smSubscriptionSuccess(String strSubID, String subClass) {
		return (RBTDBManager.getInstance().smSubscriptionSuccess(
				strSubID, null, null, null, subClass, true, "", false, null));
	}
	
	private void processDeactivateDownload(HashMap z, ArrayList smsList)
	{
		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String token = null;
		
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		
		token = (String) smsList.get(0);
		String wavFile = null;
		String deactivateBy = "SMS";
		Categories category = null;
		int categoryId = 9;
		int categoryType = 7;
		if(token != null)
		{
			token = token.trim();
			ClipMinimal clipMinimal = m_rbtDBManager.getClipMinimalPromoID(token,
					false);
			
			if(clipMinimal != null)
			{
				wavFile = clipMinimal.getWavFile();
			}
			else
			{
				char prepaidYes = 'n';
				boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID)).booleanValue();
				
				if(isPrepaid)
					prepaidYes = 'y';
				
				String circleId = getCircleID(subscriberID);
				
				category = getCategoryPromoID(token, circleId, prepaidYes);
				
				if(category != null)
				{
					categoryId = category.id();
					categoryType = category.type();
				}
			}
		}
		
		if(wavFile == null && category == null)
		{
			setReturnValues(z, getSMSTextForID(z, "PROMO_ID_FAILURE",
					m_promoIDFailureDefault), STATUS_NOT_AUTHORIZED);
			return;
		}
		
		boolean success = m_rbtDBManager.expireSubscriberDownload(subscriberID, wavFile, categoryId, categoryType, deactivateBy, null, false);
		
		if(!success)
		{
			setReturnValues(z, getSMSTextForID(z, "DOWNLOAD_DEACT_FAILURE",
					m_downloadDeactFailureDefault), STATUS_NOT_AUTHORIZED);
			return;
		}
		
		setReturnValues(z, getSMSTextForID(z, "DOWNLOAD_DEACT_SUCCESS",
				m_downloadDeactSuccessDefault), STATUS_NOT_AUTHORIZED);
		return;
	}

	private void removeCallerIDSel(HashMap z, ArrayList smsList) {
		// String _method = "removeCallerIDSel()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (callerID == null && !allowRemovalOfNullCallerIDSelection) {
			setReturnValues(z, getSMSTextForID(z, "RMV_CALLERID_FAILURE",
					m_rmvCallerIDFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		String token = null;
		String wavFile = null;
		if (smsList.size() > 0)
			token = (String) smsList.get(0);
		if (token != null) {
			if (!isInitializationDone()) {
				setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
						m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
				return;
			} else {
				if (m_model.equalsIgnoreCase("EsiaSMSImpl")) {
					PromoMaster[] promoMasters = getPromoForCode(token);
					if (promoMasters != null) {
						if (promoMasters.length == 1) {
							token = promoMasters[0].clipID();
						}
					}
				}
				ClipMinimal cm = m_rbtDBManager.getClipMinimalPromoID(token,
						false);
				if (cm != null)
					wavFile = cm.getWavFile();
			}
			if (wavFile == null) {
				setReturnValues(z, getSMSTextForID(z, "PROMO_ID_FAILURE",
						m_promoIDFailureDefault), STATUS_NOT_AUTHORIZED);
				return;
			}
		}
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		String deSelBy = "SMS";
		String thirdParty = (String) getFromZTable(z, "THIRD_MODE");
		if (thirdParty != null && thirdParty.length() > 0)
			deSelBy = thirdParty;
		if (!removeCallerIDSelection(subscriberID, callerID, deSelBy, wavFile,
				rbtType)) {
			// //logger.info("RBT::failed to
			// remove the selection for callerID-"+ callerID +" of the
			// subscriber " +subscriberID);
			setReturnValues(z, getSMSTextForID(z, "RMV_CALLERID_FAILURE",
					m_rmvCallerIDFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (callerID == null)
			callerID = m_smsTextForAll;
		String sms = getSubstituedSMS(getSMSTextForID(z,
				"RMV_CALLERID_SUCCESS", m_rmvCallerIDSuccessDefault), callerID,
				null);
		if (m_sendSMS)
			sendSMS(subscriberID, sms);
		// //logger.info("RBT::removed the
		// selection for callerID-"+ callerID +" of the subscriber "
		// +subscriberID);
		setReturnValues(z, sms, STATUS_SUCCESS);
	}

	private void deactivate(HashMap z) {
		// String _method = "deactivate()";
		// //logger.info("****** parameters are -- "+z);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String req = (String) getFromZTable(z, REQUEST_TYPE);

		if (subscriber == null) {
			// //logger.info("RBT::subscriber does not
			// exist");
			setReturnValues(z, getSMSTextForID(z, "DEACTIVATION_FAILURE",
					m_deactivationFailureDefault), STATUS_NOT_AUTHORIZED);
			return;
		}

		if (!isSubActive(subscriber, z)) {
			// //logger.info("RBT::subscriber " +
			// subscriberID + " already deactivated");
			if (req != null && req.equals("VALIDATE")
					&& !subscriber.subYes().equals("X")) {
				String ret = smDeactivationSuccess(subscriberID, subscriber
						.subYes());

				if (ret != null && ret.equalsIgnoreCase("SUCCESS")) {
					setReturnValues(z, getSMSTextForID(z,
							"DEACTIVATION_SUCCESS",
							m_deactivationSuccessDefault), STATUS_SUCCESS);
					return;
				}
			}

			if (subscriber.subYes().equalsIgnoreCase("D")
					|| subscriber.subYes().equalsIgnoreCase("P")
					|| subscriber.subYes().equalsIgnoreCase("F"))
				setReturnValues(z, getSMSTextForID(z,
						"DEACTIVATION_FAILURE_DEACT",
						m_deactivationFailureDeactDefault),
						STATUS_ALREADY_CANCELLED);
			else
				setReturnValues(z, getSMSTextForID(z, "DEACTIVATION_FAILURE",
						m_deactivationFailureDefault), STATUS_ALREADY_CANCELLED);
			return;
		}

		if (bConfirmDeactivation) {
			// ViralSMSTable vst =
			// m_rbtDBManager.getViralSMSByType(subscriberID, "CAN");
			ViralSMSTable vst = m_rbtDBManager
					.getViralSMSByTypeOrderedByTimeDesc(subscriberID, "CAN");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -m_deactivationConfirmClearDays);
			if (vst == null
					|| (vst.sentTime() != null && vst.sentTime().before(
							cal.getTime()))) {
				long daysLeftBeforeNextCharging = getDaysBeforeNextCharging(subscriber);
				if (daysLeftBeforeNextCharging >= m_minDaysDeactivationConfirm) {
					m_rbtDBManager.insertViralSMSTableMap(subscriberID, null,
							"CAN", null, null, 0, null, null, null);
					if (m_sendSMS)
						sendSMS(subscriberID, getSubstituedSMS(getSMSTextForID(
								z, "DEACTIVATION_CONFIRM",
								m_deactivationConfirmTextDefault),
								daysLeftBeforeNextCharging + "",
								m_deactivationConfirmClearDays + ""));
					setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
							"DEACTIVATION_CONFIRM",
							m_deactivationConfirmTextDefault),
							daysLeftBeforeNextCharging + "",
							m_deactivationConfirmClearDays + ""),
							STATUS_SUCCESS);
					return;
				}
			}

		}

		boolean isDirectDeactivations = false;
		// String req = (String) getFromZTable(z, REQUEST_TYPE);
		if (req != null && req.equals("VALIDATE"))
			isDirectDeactivations = true;

		String deactBy = "SMS";
		String thirdParty = (String) getFromZTable(z, "THIRD_MODE");
		if (thirdParty != null && thirdParty.length() > 0)
			deactBy = thirdParty;
		if (getFromZTable(z, SG_MODE) != null)
			deactBy = (String) getFromZTable(z, SG_MODE);

		String success = deactivateSubscriber(subscriberID, deactBy, null,
				m_delSelections, isDirectDeactivations);

		if (bConfirmDeactivation)
			removeViralSMSTable(subscriberID, "CAN");
		if (success != null && success.equals("ACT_PENDING")) {
			// //logger.info("RBT::failed to
			// deactivate the subscriber " +subscriberID);
			setReturnValues(z, getSMSTextForID(z, "DEACTIVATION_FAILURE_ACT",
					m_deactivationFailureActDefault), STATUS_TECHNICAL_FAILURE);
		} else if (success != null && success.equals("SUCCESS")) {
			if (m_sendSMS)
				sendSMS(subscriberID, getSMSTextForID(z,
						"DEACTIVATION_SUCCESS", m_deactivationSuccessDefault));
			// //logger.info("RBT::deactivated the
			// subscriber " +subscriberID);
			setReturnValues(z, getSMSTextForID(z, "DEACTIVATION_SUCCESS",
					m_deactivationSuccessDefault), STATUS_SUCCESS);
		} else if (success != null && success.equals("DCT_NOT_ALLOWED")) {
			if (m_sendSMS)
				sendSMS(subscriberID, getSMSTextForID(z,
						"DEACTIVATION_NOTALLOWED",
						m_deactivationNotAllowedDefault));
			// //logger.info("RBT::deactivated the
			// subscriber " +subscriberID);
			setReturnValues(z, getSMSTextForID(z, "DEACTIVATION_NOTALLOWED",
					m_deactivationNotAllowedDefault), STATUS_SUCCESS);
		}

	}
	
	// Added extraInfo in smDeactivationSuccess method
	private String smDeactivationSuccess(String strSubID, String subYes) {
		return (RBTDBManager.getInstance().smDeactivationSuccess(strSubID, subYes, null));
	}

	private void removeTempOverride(HashMap z) {
		// String _method = "removeTempOverride()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String reqType = (String) getFromZTable(z, REQUEST_TYPE);
		if (reqType != null && reqType.equals("VALIDATE"))
			return;
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (isSubAlreadyActiveOnStatus(subscriberID, null, 99, rbtType)) {
			deactivateSubscriberRecords(subscriberID, null, 99, rbtType);
			// //logger.info("RBT::deactivated
			// sms profile of the subscriber " +subscriberID);
			setReturnValues(z, "Your profile has been removed successfully",
					STATUS_SUCCESS);
		} else {
			if (isSubActive(subscriber, z)) {
				setReturnValues(z, "You do not have any active profiles",
						STATUS_NOT_AUTHORIZED);
				// //logger.info("RBT::no
				// active sms profiles for the subscriber " +subscriberID);
			} else {
				// //Tools.logDetail(_class, "removeTempOverride",
				// "RBT::subscriber " + subscriberID + " is deactivated");
				setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
						STATUS_NOT_AUTHORIZED);
			}
		}
		if (m_sendSMS)
			sendSMS(subscriberID, (String) z.get(RETURN_STRING));
	}

	private void listTempOverride(HashMap z) throws Exception {
		// String _method = "listTempOverride()";
		// //logger.info("****** parameters are -- "+z );

		if (!isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		SitePrefix prefix = Utility.getPrefix(subscriberID);
		
		Clips[] profileClips = (Clips[]) (m_profileClips.values())
				.toArray(new Clips[0]);

		if (profileClips == null || profileClips.length < 1) {
			setReturnValues(z, getSMSTextForID(z,
					"TEMPORARY_OVERRIDE_LIST_FAILURE",
					m_temporaryOverrideListFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
			return;
		}
		String sms = "";
		ArrayList al = null;
		String name = null;
		StringTokenizer stk = null;
		String tmp = null;
		
		ArrayList<String> m_supportedLang = null;
		String m_defaultLanguage = m_globalDefaultLanguage;
//		if (prefix != null) {
//			String languagesSupported = prefix.supportedLang();
//			m_supportedLang = Tools.tokenizeArrayList(languagesSupported, ",");
//			if (m_supportedLang != null && m_supportedLang.size() == 0)
//				m_defaultLanguage = m_supportedLang.get(0);
//		}

		for (int i = 0; i < profileClips.length; i++) {
			al = Tools.tokenizeArrayList(profileClips[i].wavFile(), "_");
			if (al != null && al.contains(m_defaultLanguage)) {
				name = "";
				stk = new StringTokenizer(profileClips[i].name(), " ");
				while (stk.hasMoreTokens()) {
					tmp = stk.nextToken().toLowerCase();
					if (!tmp.equals(m_defaultLanguage))
						name = name + " " + tmp;
				}
				name = name.trim().toLowerCase();
				if (sms.equalsIgnoreCase(""))
					sms = name;
				else if (sms.indexOf(name) == -1)
					sms = sms.trim() + ", " + name;

			}
		}

		sms = getSubstituedSMS(getSMSTextForID(z,
				"TEMPORARY_OVERRIDE_LIST_SUCCESS",
				m_temporaryOverrideListSuccessDefault), sms, null);
		String[] tempSMS = parseText(sms);

		if (tempSMS != null) {
			sms = "";
			for (int i = 0; i < tempSMS.length; i++) {
				if (sms.equalsIgnoreCase(""))
					sms = tempSMS[i];
				else
					sms = sms + "   " + tempSMS[i];
			}
		}

		setReturnValues(z, sms, STATUS_SUCCESS);
		if (m_sendSMS)
			sendSMS(subscriberID, (String) z.get(RETURN_STRING));
	}

	public HashMap getExtraInfoMap(Subscriber subscriber) {

		logger.info("inside getextrainfomap");
		return RBTDBManager.getInstance().getExtraInfoMap(subscriber);
	}

	public boolean updateExtraInfoAndPlayerStatus(String subscriberId,
			Subscriber subscriber, String name, String value, String playerStatus) {

		return RBTDBManager.getInstance()
				.updateExtraInfoAndPlayerStatus(subscriber, name, value,
						playerStatus);
	}

	public void processPollON(HashMap z) {
		logger.info("inside processPollON");
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String subscriberId = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String actInfo = (String) getFromZTable(z, ACT_INFO);
		String req = (String) getFromZTable(z, REQUEST_TYPE);
		String actBy = (String) getFromZTable(z, ACT_BY);
		int rbtType = TYPE_RBT;
		boolean isDirectActivation = true;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;
		boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID))
				.booleanValue();

		if (!req.equalsIgnoreCase("VALIDATE") || !isDirectActivation)
			isDirectActivation = false;

		int days = Integer.parseInt((String) getFromZTable(z, DAYS));

		String subClassType = (String) getFromZTable(z, SUB_CLASS_TYPE);
		String name = PLAY_POLL_STATUS;
		String value = PLAY_POLL_STATUS_ON;
		String playerStatus = "A";
		HashMap extraInfoMap = null;
		String pollExtraInfo = null;
		boolean result = false;
		extraInfoMap = getExtraInfoMap(subscriber);
		if (extraInfoMap != null && extraInfoMap.get(PLAY_POLL_STATUS) != null)
			pollExtraInfo = (String) extraInfoMap.get(PLAY_POLL_STATUS);

		if (subscriber == null || !isSubActive(subscriber)) {

			Subscriber sub = activateSubscriber(subscriberId, actBy, null,
					isPrepaid, days, actInfo, subClassType, isDirectActivation,
					rbtType);
			if (sub != null) {
				result = updateExtraInfoAndPlayerStatus(subscriberId, subscriber,
						name, value, playerStatus);
				if (result)
					setReturnValues(z, getSMSTextForID(z, "POLL_ON_SUCCESS",
							m_pollONSuccessDefault), STATUS_SUCCESS);
				else
					setReturnValues(z, getSMSTextForID(z,
							"POLL_ON_TECHNICAL_DIFFICULTIES",
							m_pollONTechnicalDifficultiesDefault),
							STATUS_SUCCESS);
			} else {
				setReturnValues(z, getSMSTextForID(z,
						"POLL_ON_TECHNICAL_DIFFICULTIES",
						m_pollONTechnicalDifficultiesDefault), STATUS_SUCCESS);
				return;
			}

		} else {
			boolean res = true;
			if (pollExtraInfo == null
					|| pollExtraInfo.equals(PLAY_POLL_STATUS_OFF))
				res = updateExtraInfoAndPlayerStatus(subscriberId, subscriber,
						name, value, playerStatus);
			if (res) {
				System.out.println("i m in deactive");
				RBTDBManager.getInstance()
						.deactivateSubscriberRecords(subscriberId, null, 1, 0,
								2359, true, "SMS");
				setReturnValues(z, getSMSTextForID(z, "POLL_ON_SUCCESS",
						m_pollONSuccessDefault), STATUS_SUCCESS);

			} else {
				System.out.println("i m not in deactive");
				setReturnValues(z, getSMSTextForID(z,
						"POLL_ON_TECHNICAL_DIFFICULTIES",
						m_pollONTechnicalDifficultiesDefault), STATUS_SUCCESS);
			}
		}
	}

	public void processPollOFF(HashMap z) {
		logger.info("inside processPollOFF");
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (subscriber == null || !isSubActive(subscriber)) {

			setReturnValues(z, getSMSTextForID(z,
					"POLL_OFF_SUBSCRIBER_NOT_ACTIVE",
					m_pollSuscriberNotActiveDefault), STATUS_SUCCESS);
			return;
		}
		String name = PLAY_POLL_STATUS;
		String value = PLAY_POLL_STATUS_OFF;
		String playerStatus = "A";
		boolean result = updateExtraInfoAndPlayerStatus(subscriber.subID(),
				subscriber, name, value, playerStatus);
		if (result)
			setReturnValues(z, getSMSTextForID(z, "POLL_OFF_SUCCESS",
					m_pollOFFSuccessDefault), STATUS_SUCCESS);
		else
			setReturnValues(z, getSMSTextForID(z,
					"POLL_OFF_TECHNICAL_DIFFICULTIES",
					m_pollOFFTechnicalDifficultiesDefault), STATUS_SUCCESS);
	}
	
	public void processCancelCopyRequest(HashMap z)
	{
		logger.info("inside processCancelCopy");
		
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		boolean success = m_rbtDBManager.removeCopyPendingViralSMSOfCaller(subscriberID, COPYCONFIRM, m_waitTimeDoubleConfirmation);
		logger.info("The value of success "+success);
		if(success)
		{
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CANCEL_SUCCESS",
					m_copyCancelSuccessDefault), STATUS_SUCCESS);
		}
		else
		{
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CANCEL_FAILURE",
					m_copyCancelFailureDefault), STATUS_SUCCESS);
		}
	}
	
	public void processConfirmCopyRequest(HashMap z)
	{
		logger.info("inside processConfirmCopy");
		
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		boolean success = m_rbtDBManager.updateViralSMSTypeOfCaller(subscriberID, COPYCONFPENDING, COPYCONFIRMED, m_waitTimeDoubleConfirmation);
		logger.info("The value of success "+success);
		if(success)
		{
			success = m_rbtDBManager.removeCopyPendingViralSMSOfCaller(subscriberID, COPYCONFPENDING, m_waitTimeDoubleConfirmation);
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CONFIRM_SUCCESS",
					m_copyConfirmSuccessDefault), STATUS_SUCCESS);
		}
		else
		{
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CONFIRM_FAILURE",
					m_copyConfirmFailureDefault), STATUS_SUCCESS);
		}
	}
	
	public void processCancelOptInCopyRequest(HashMap z)
	{
		logger.info("inside processCancelCopy");
		
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		boolean success = m_rbtDBManager.removeCopyPendingViralSMSOfCaller(subscriberID, COPYCONFPENDING, m_waitTimeDoubleConfirmation);
		logger.info("The value of success "+success);
		if(success)
		{
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CANCEL_SUCCESS",
					m_copyCancelSuccessDefault), STATUS_SUCCESS);
		}
		else
		{
			setReturnValues(z, getSMSTextForID(z,
					"COPY_CANCEL_FAILURE",
					m_copyCancelFailureDefault), STATUS_SUCCESS);
		}
	}


	public void processCOPY(HashMap z, ArrayList smsList) {
		// String _method = "processCopy()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+smsList );

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		String setForCaller = null;
		if (smsList.size() > 0)
			setForCaller = (String) smsList.get(0);

		if (callerID == null
				|| (callerID).equalsIgnoreCase(subscriberID)
				|| (!m_model.equals("AirtelSMSImpl") && !isSubActive(
						getSubscriber(callerID), z))) {
			if (callerID == null)
				setReturnValues(z, getSMSTextForID(z,
						"TEMPORARY_OVERRIDE_FAILURE",
						m_temporaryOverrideFailureDefault),
						STATUS_TECHNICAL_FAILURE);
			else
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_FAILURE", m_copyFailureSMSDefault), callerID,
						null), STATUS_TECHNICAL_FAILURE);
			return;
		}

		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!(isSubActive(subscriber, z) || m_isActOptional
				|| m_isActOptionalCopy || m_model
				.equalsIgnoreCase("AirtelSMSImpl"))) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (isSubActive(subscriber, z) && m_model.equals("AirtelSMSImpl")
				&& setForCaller != null && maxCallerIDSelectionsAllowed > 0) {
			SubscriberStatus[] ssCallerId = getOtherCallerIDSelections(
					subscriberID, setForCaller, rbtType);
			String callerIDNumbers = "";
			int countCaller = 0;
			for (int i = 0; ssCallerId != null && i < ssCallerId.length; i++) {
				String callerI = ssCallerId[i].callerID();
				if (callerI != null && callerIDNumbers.indexOf(callerI) == -1) {
					callerIDNumbers += ", " + ssCallerId[i].callerID();
					countCaller++;
				}
			}
			if (callerIDNumbers.length() > 2)
				callerIDNumbers = callerIDNumbers.substring(2);
			if (countCaller >= maxCallerIDSelectionsAllowed) {
				setReturnValues(z,
						getSubstituedSMS(getSMSTextForID(z,
								"CALLER_ID_SELECTION_BLOCK",
								m_callerIdSelectionBlockDefault),
								callerIDNumbers, null), STATUS_SUCCESS);
				return;
			}
		}
		/*
		 * NOT_VALID : If number is not airtel number NOT_FOUND : Not RBT User
		 * ALBUM : user has album selection (not allowed to copy) DEFAULT : User
		 * does not have any selection (just activate the user) ERROR : If any
		 * error
		 */
		String subWavFileCatId = null;
		if (m_model.equals("AirtelSMSImpl")) {
			// subWavFileCatId = RBTDBManager.init(m_dbURL,
			// m_nConn).getSubscriberVcode(callerID, subscriberID,
			// RBTCommonConfig.getInstance().useProxyNonCircle(),
			// RBTCommonConfig.getInstance().proxyServerPort());
			subWavFileCatId = RBTDBManager.getInstance()
					.getSubscriberVcode(callerID, subscriberID,
							RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_PROXY", "FALSE"),
							RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROXY_SERVER_PORT", "FALSE"),
							rbtType);
			if (subWavFileCatId.equalsIgnoreCase("ERROR")) {
				setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
						m_technicalFailureDefault), STATUS_SUCCESS);
				return;
			} else if (subWavFileCatId.equalsIgnoreCase("NOT_VALID")) {
				setReturnValues(z, getSMSTextForID(z, "COPY_FAILURE_NON_OPR",
						m_copyFailureNonOprDefault), STATUS_SUCCESS);
				return;
			} else if (subWavFileCatId.equalsIgnoreCase("NOT_FOUND")) {
				setReturnValues(z, getSMSTextForID(z,
						"GIFT_FAILURE_GIFTEE_INVALID",
						m_giftInvalidGifteeDefault), STATUS_SUCCESS); 
				return;
			} else if (subWavFileCatId.equalsIgnoreCase("ALBUM")) {
				setReturnValues(z, getSMSTextForID(z, "COPY_FAILURE_ALBUM",
						m_copyFailureAlbumDefault), STATUS_SUCCESS);
				return;
			} else if (subWavFileCatId.equalsIgnoreCase("DEFAULT")) {
				subWavFileCatId = "default:26";
			}
		}

		String name = null;
		String status = null;
		boolean isPrepaid = true;
		if (subscriber != null)
			isPrepaid = subscriber.prepaidYes();
		String playUncharged = "ALL";
		if (localSitePrefix != null
				&& !localSitePrefix.playUncharged(isPrepaid))
			playUncharged = "NONE";
		SubscriberStatus subscriberStatus = null;
		if (!m_model.equals("AirtelSMSImpl"))
			subscriberStatus = getSubscriberFile(callerID, subscriberID,
					playUncharged, isMemCacheModel, rbtType);
		if ((subscriberStatus == null || subscriberStatus.categoryID() == -1)
				&& setForCaller == null && !m_model.equals("AirtelSMSImpl"))
			RBTDBManager.getInstance().insertViralSMSTableMap(callerID,
					null, "COPY", subscriberID, null, 0, "SMS", null, null);
		else if ((subscriberStatus == null || subscriberStatus.categoryID() == -1)
				&& setForCaller != null && !m_model.equals("AirtelSMSImpl")) {
			RBTDBManager.getInstance().insertViralSMSTableMap(callerID,
					null, "COPY", subscriberID,
					"default:-1:-1|" + setForCaller, 0, "SMS", null, null);
		} else {
			int catID = 26;
			String subWavFile = null;

			if (m_model.equals("AirtelSMSImpl") && subWavFileCatId != null
					&& subWavFileCatId.indexOf(":") != -1) {
				subWavFile = subWavFileCatId.substring(0, subWavFileCatId
						.indexOf(":"));
				try {
					catID = Integer.parseInt(subWavFileCatId
							.substring(subWavFileCatId.indexOf(":") + 1));
				} catch (Exception e) {
					catID = 26;
				}
			}
			if (subscriberStatus != null)
				catID = subscriberStatus.categoryID();
			Categories cat = getCategory(catID, getCircleID(subscriberID));
			String clip;
			String endDate = null;
			if (subscriberStatus != null)
				subWavFile = subscriberStatus.subscriberFile();
			ClipMinimal clipObject = null;
			if (subWavFile != null)
				clipObject = m_rbtDBManager.getClipRBT(subWavFile);
			if (clipObject != null) {
				name = clipObject.getClipName();
				if (clipObject.getEndTime() != null) {
					endDate = clipObject.getEndTime().toString();
				}
			}
			if (subscriberStatus != null)
				status = "" + subscriberStatus.status();
			else
				status = "1";
			if (status.equals("90")) {
				name = "Cricket Feed";
				subWavFile = "CRICKET";
			}
			if (setForCaller != null)
				status = status + "|" + setForCaller;
			if (cat != null && cat.type() == SHUFFLE)
				clip = subWavFile + ":" + "S" + catID + ":" + status;
			else
				clip = subWavFile + ":" + catID + ":" + status;
			if (!isSubActive(subscriber, z)
					&& m_model.equalsIgnoreCase("AirtelSMSImpl")) {
				removeViralSMSTable(subscriberID, "PRECOPY");
				removeViralSMSTable(subscriberID, "SELECTION");
				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "PRECOPY", subscriberID, clip, 0,
						"SMS", null,null);
				setReturnValues(z, getSMSTextForID(z, "COPY_ACTIVATE_SUB",
						m_CopyActivateSubTextDefault), STATUS_SUCCESS);
				return;
			} else if (m_model.equalsIgnoreCase("AirtelSMSImpl")
					&& isShufflePresentSelection(subscriberID, setForCaller,
							rbtType)) {
				removeViralSMSTable(subscriberID, "COPY");
				removeViralSMSTable(subscriberID, "PRECOPY");
				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "PRECOPY", subscriberID, clip, 0,
						"SMS", null,null);
				setReturnValues(z, getSMSTextForID(z,
						"COPY_OVERRIDE_SHUFFLE_SELECTION",
						m_CopyOverrideShuffleSelTextDefault), STATUS_SUCCESS);
				return;

			} else
				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "COPY", subscriberID, clip, 0, "SMS",
						null,null);
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"COPY_SUCCESS", m_copySuccessSMSDefault), callerID, name,
					endDate), STATUS_SUCCESS);
			return;
		}
		if (status != null && status.equals("90"))
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
					"Cricket Feed"), STATUS_SUCCESS);
		else
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
					m_defaultClip), STATUS_SUCCESS);

	}

	private void handlePromotion(String promoKeyword, HashMap z,
			ArrayList smsList) {
		// String _method = "handlePromotion()";
		// //logger.info("****** parameters are -- "+
		// promoKeyword + " & "+z + " & "+smsList);

		if (smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_NOT_AUTHORIZED);
			return;
		}

		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		String successText = (String) getFromZTable(z, SUCCESS_TEXT);
		String failureText = (String) getFromZTable(z, FAILURE_TEXT);

		if (isActivationRequest && isSubActive(subscriber, z)) {
			setReturnValues(z, failureText, STATUS_NOT_AUTHORIZED);
			return;
		}
		if (!isActivationRequest && !isSubActive(subscriber, z)
				&& !m_isActOptional) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_SUCCESS);
			return;
		}

		addToHashMap(z, ACT_BY, "PROMO");
		addToHashMap(z, SMS_SELECTED_BY, "PROMO");
		int gracePeriod = 0;
		String subscriptionPeriod = null;
		Date nextChargingDate = null;
		long days = 0;
		String token = null;
		if (promoKeyword != null) {
			StringTokenizer st = new StringTokenizer(promoKeyword, ",");
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens()) {
				if (promoKeyword.equalsIgnoreCase(m_promotion1)
						|| promoKeyword.equalsIgnoreCase(m_promotion2)) {
					token = st.nextToken();
					addToHashMap(z, ACT_BY, token);
					addToHashMap(z, SMS_SELECTED_BY, token);
				} else if (promoKeyword.equalsIgnoreCase(m_songPromotion1)
						|| promoKeyword.equalsIgnoreCase(m_songPromotion2))
					addToHashMap(z, SMS_SELECTED_BY, st.nextToken());
			}
			if (st.hasMoreTokens()) {
				if (promoKeyword.equalsIgnoreCase(m_promotion1)
						|| promoKeyword.equalsIgnoreCase(m_promotion2)) {
					token = st.nextToken();
					addToHashMap(z, SUB_CLASS_TYPE, token);
					addToHashMap(z, CLASS_TYPE, token);
				} else if (promoKeyword.equalsIgnoreCase(m_songPromotion1)
						|| promoKeyword.equalsIgnoreCase(m_songPromotion2))
					addToHashMap(z, CLASS_TYPE, st.nextToken());
			}
			if (st.hasMoreTokens()) {
				try {
					gracePeriod = Integer.parseInt(st.nextToken());
				} catch (Exception e) {
					gracePeriod = 0;
				}
			}
		}

		SubscriptionClass subClass = getSubscriptionClass((String) z
				.get(SUB_CLASS_TYPE));
		if (subClass == null)
			addToHashMap(z, SUB_CLASS_TYPE, "DEFAULT");
		if (subClass != null && gracePeriod > 0) {
			subscriptionPeriod = subClass.getSubscriptionPeriod();
			nextChargingDate = getNextChargingDate(subscriptionPeriod,
					new Integer(gracePeriod).toString());
			days = (nextChargingDate.getTime() - System.currentTimeMillis())
					/ (1000 * 60 * 60 * 24);
			if (days < 0)
				days = 0;
		}
		addToHashMap(z, DAYS, "" + days);
		token = (String) smsList.get(0);
		getCategoryAndClipForPromoID(token, z, true);
		// ClipMinimal clipMinimal = (ClipMinimal) z.get(CLIP_OBJECT);
		if (!isValidClip(z)) {
			setReturnValues(z, "Request completed successfully", STATUS_SUCCESS);
			return;
		}

		if (isActivationRequest
				|| (!isSubActive(subscriber, z) && m_isActOptional))
			// if (!handleActivation(z))
			if (!handleActivation(z, false))
				return;

		if (!handleSelection(z))
			return;

		String callerID = (String) getFromZTable(z, CALLER_ID);
		// Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (callerID == null)
			callerID = m_smsTextForAll;
		/*
		 * String name = null; if (clipMinimal != null) name =
		 * clipMinimal.getClipName(); if (category != null && category.id() !=
		 * 3) name = category.name();
		 */
		setReturnValues(z, getSMSTextForID(z, "ACTIVATION_PROMO_SUCCESS",
				m_actPromoSuccessTextDefault), STATUS_SUCCESS);

		if (isActivationRequest) {
			setReturnValues(z, successText, STATUS_SUCCESS);
			return;
		}
	}

	private void setRequest(HashMap z, int songNo, String type) {
		// String _method = "setRequest()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+songNo + " & "+type);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		String success = "";
		String failure = "";
		if (type.equalsIgnoreCase("REQUEST")) {
			success = getSMSTextForID(z, "REQUEST_RBT_SMS2_SUCCESS",
					m_requestRbtSuccess2Default);
			failure = getSMSTextForID(z, "REQUEST_RBT_SMS2_FAILURE",
					m_requestRbtFailure2Default);
		} else if (type.equalsIgnoreCase("CATEGORY")) {
			success = getSMSTextForID(z, "CATEGORY_SEARCH_SET_SUCCESS",
					m_catRbtSuccess2Default);
			failure = getSMSTextForID(z, "CATEGORY_SEARCH_SET_FAILURE",
					m_catRbtFailure2Default);
		}

		if (!(isSubActive(subscriber, z) || m_isActOptional || m_isActOptionalRequestRBT)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_NOT_AUTHORIZED);
			return;
		}
		//Aircel feature. Don't allow activation if coming from a particular shortcode
		if(!isSubActive(subscriber, z))
		{	
			String shortCode = (String)getFromZTable(z, "SHORTCODE");
			if(shortCode != null && m_onlySearchShortCode != null && shortCode.trim().equalsIgnoreCase(m_onlySearchShortCode.trim()))
			{
				setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
						STATUS_NOT_AUTHORIZED);
				return ;
			}	
		}
		
		ViralSMSTable context = getViralSMSTable(subscriberID, type);
		if (context == null || context.clipID() == null) {
			setReturnValues(z, getSMSTextForID(z, "REQUEST_MORE_NO_SEARCH",
					m_reqMoreSMSNoSearchDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}

		boolean invalid = false;
		String clipIDs = context.clipID();
		StringTokenizer stk = new StringTokenizer(clipIDs, ",");
		if (type.equalsIgnoreCase("CATEGORY") && stk.hasMoreTokens())
			stk.nextToken();
		for (int i = 1; i < (songNo); i++) {
			if (stk.hasMoreTokens())
				stk.nextToken();
			else {
				invalid = true;
				break;
			}
		}

		if (songNo < 1 || invalid || !stk.hasMoreTokens()) {
			setReturnValues(z, failure, STATUS_TECHNICAL_FAILURE);
			return;
		}

		String token = null;
		ClipMinimal reqClip = null;
		if (stk.hasMoreTokens())
			token = stk.nextToken();
		if (token != null)
			reqClip = getClip(Integer.parseInt(token));
		if (reqClip == null) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		} else {
			// ClipMinimal clipMinimal = new ClipMinimal(reqClip);
			// addToHashMap(z, CLIP_OBJECT, clipMinimal);
			addToHashMap(z, CLIP_OBJECT, reqClip);
		}
		String thirdParty = (String) getFromZTable(z, "THIRD_MODE");
		if (thirdParty != null && thirdParty.length() > 0) {
			addToHashMap(z, ACT_BY, thirdParty);
			addToHashMap(z, SMS_SELECTED_BY, thirdParty);
		}
		if (!isSubActive(subscriber, z)) {
			// z.put(ACT_INFO, "SMS");
			// if (!handleActivation(z))
			if (!handleActivation(z, false))
				return;
			subscriber = (Subscriber) z.get(SUBSCRIBER_OBJ);
		}
		// addToHashMap(z, CATEGORY_OBJECT, getCategory(11));
		Categories category = getCategory(11, getCircleID(subscriberID));
		addToHashMap(z, CATEGORY_OBJECT, category);

		// addToHashMap(z, SMS_SELECTION_INFO, "SMS");
		if (z.containsKey(CLASS_TYPE))
			z.remove(CLASS_TYPE);
		if (!handleSelection(z))
			return;
		String sms = reqClip.getClipName();
		String endDate = null;
		if (reqClip != null && reqClip.getEndTime() != null)
			endDate = reqClip.getEndTime().toString();
		if (m_addMovieRequest && reqClip.getAlbum() != null
				&& reqClip.getAlbum().length() > 0
				&& type.equalsIgnoreCase("REQUEST"))
			sms = sms + "," + reqClip.getAlbum();
		//sending second string in getSubstitutesSMS as non null so that the end date will also get replaced in the SMS - Sreekar
		setReturnValues(z, getSubstituedSMS(success, sms, "", endDate),
				STATUS_SUCCESS);
		String req = (String) getFromZTable(z, REQUEST_TYPE);
		if (!req.equals("VALIDATE"))
			removeViralSMSTable(subscriberID, type);
	}

	private void searchRequest(HashMap z, ArrayList smsList) {

		// String _method = "searchRequest()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+smsList);

		if (!isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		if (!(isSubActive(subscriber, z) || m_isActOptional || m_isActOptionalRequestRBT)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_NOT_AUTHORIZED);
			return;
		}

		String searchType = (String) getFromZTable(z, SEARCH_TYPE);
		String searchString = (String) getFromZTable(z, SEARCH_STRING);
		boolean isRetailer = ((Boolean) getFromZTable(z, IS_RETAILER))
				.booleanValue();
		String[] results = null;
		if (searchString != null && searchString.trim().length() > 0)
			results = rbtClipsLucene.search(searchString.trim(), true,
					searchType);
		if (results == null || results.length == 0) {
			if (m_reqNoMatchDispTop) {
				Clips[] topClips = getAllClips(5);
				String match = "";
				String clipIDs = "";
				String song = null;
				for (int i = 0; i < m_reqNoMatchDispMax; i++) {
					try {
						if (clipIDs.equalsIgnoreCase(""))
							clipIDs = "" + topClips[i].id();
						else
							clipIDs = clipIDs + "," + topClips[i].id();
						song = topClips[i].name();
						if (m_addMovieRequest && topClips[i].album() != null
								&& topClips[i].album().length() > 0)
							song = song + "," + topClips[i].album();
						if (isRetailer || m_model.equals("AirtelSMSImpl")) {
							if (topClips[i].promoID() != null)
								match = match + song + "-"
										+ topClips[i].promoID() + " ";
							else
								match = match + song + " ";
						} else {
							if (m_insertSearchNumberAtEnd)
								match = match + song + "-" + (i + 1) + " ";
							else
								match = match + (i + 1) + "-" + song + " ";
						}
					} catch (Exception e) {
						System.out.println("exception " + e);
					}
				}
				removeViralSMSTable(subscriberID, "REQUEST");
				if (m_model.equalsIgnoreCase("AirtelSMSImpl"))
					removeViralSMSTable(subscriberID, "CATEGORY");
				insertViralSMSTable(subscriberID, null, clipIDs, "REQUEST", 1);
				String sms = getSubstituedSMS(getSMSTextForID(z,
						"REQUEST_NO_MATCH_DISP", m_reqNoMatchSMSDefault), match
						.trim(), null);

				if (!isSubActive(subscriber, z))
					sms += getSMSTextForID(z, "NON_SUBSCRIBER_SMS", "");
				if (isRetailer)
					sms = getSubstituedSMS(getSMSTextForID(z,
							"REQUEST_NO_MATCH_DISP_RET",
							m_reqNoMatchRetSMSDefault), match.trim(), null);
				String[] tempSMS = parseText(sms);

				if (tempSMS != null) {
					match = "";
					for (int i = 0; i < tempSMS.length; i++) {
						if (match.equalsIgnoreCase(""))
							match = tempSMS[i];
						else
							match = match + "  " + tempSMS[i];
					}
				}
				setReturnValues(z, match, STATUS_SUCCESS);
			} else
				setReturnValues(z,
						getSMSTextForID(z, "REQUEST_RBT_SMS1_FAILURE",
								m_requestRbtFailure1Default),
						STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
		} else {
			String clipIDs = "";
			String langClips = "";
			String id = null;
			int clipID = -1;
			ClipMinimal clip = null;

			for (int hit = 0; hit < results.length; hit++) {
				try {
					id = results[hit].trim();
					clipID = Integer.parseInt(id.trim());
					clip = getClip(clipID);
					if (m_langFilter
							&& clip.getLanguage() != null
							&& clip.getLanguage()
									.equalsIgnoreCase(m_reqDefLang)) {
						if (langClips.equalsIgnoreCase(""))
							langClips = "" + clipID;
						else
							langClips = langClips + "," + clipID;
					} else {
						if (clipIDs.equalsIgnoreCase(""))
							clipIDs = "" + clipID;
						else
							clipIDs = clipIDs + "," + clipID;
					}
				} catch (Exception e) {
					System.out.println("ERROR in " + results[hit]
							+ " and exception is" + e);
				}
			}

			if (!langClips.equalsIgnoreCase(""))
				clipIDs = langClips + "," + clipIDs;

			String match = "";
			StringTokenizer clipTokens = new StringTokenizer(clipIDs, ",");
			int iSong = 0;
			while (clipTokens.hasMoreTokens()) {
				int id1 = 0;
				try {
					id1 = Integer.parseInt(clipTokens.nextToken());
				} catch (Exception e) {
					continue;
				}
				clip = getClip(id1);
				if (iSong < m_reqMaxSMS) {
					String song = clip.getClipName();
					if (m_addMovieRequest && clip.getAlbum() != null
							&& clip.getAlbum().length() > 0)
						song = song + "," + clip.getAlbum();

					if (isRetailer || m_model.equals("AirtelSMSImpl")) {
						if (clip.getPromoID() != null)
							match = match + song + "-" + clip.getPromoID()
									+ " ";
						else
							match = match + song + " ";
					} else {
						if (m_insertSearchNumberAtEnd)
							match = match + song + "-" + (iSong + 1) + " ";
						else
							match = match + (iSong + 1) + "-" + song + " ";
					}
				} else
					break;

				iSong++;
			}
			removeViralSMSTable(subscriberID, "REQUEST");
			if (m_model.equalsIgnoreCase("AirtelSMSImpl"))
				removeViralSMSTable(subscriberID, "CATEGORY");
			insertViralSMSTable(subscriberID, null, clipIDs, "REQUEST", 1);
			String sms = match.trim()
					+ getSMSTextForID(z, "REQUEST_RBT_SMS1_SUCCESS",
							m_requestRbtSuccess1Default);
			if (!isSubActive(subscriber, z))
				sms += getSMSTextForID(z, "NON_SUBSCRIBER_SMS", "");
			if (isRetailer)
				sms = match.trim();
			if (results.length > m_reqMaxSMS)
				sms = sms
						+ getSMSTextForID(z, "REQUEST_MORE",
								m_reqMoreSMSDefault);

			String[] tempSMS = parseText(sms);

			if (tempSMS != null) {
				match = "";
				for (int i = 0; i < tempSMS.length; i++) {
					if (match.equalsIgnoreCase(""))
						match = tempSMS[i];
					else
						match = match + "  " + tempSMS[i];
				}
			}
			setReturnValues(z, match, STATUS_SUCCESS);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
		}
	}

	private void searchCategory(HashMap z, String alias) {
		// String _method = "searchCategory()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+alias);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID))
				.booleanValue();
		boolean isTopFeature = (z.get("IS_TOP_LISTING") != null);
		if (!(isSubActive(subscriber, z) || m_isActOptional || m_isActOptionalRequestRBT)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_NOT_AUTHORIZED);
			return;
		}

		if (!isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}

		// Categories category = (Categories) m_Categories.get(alias);
		Categories category = RBTDBManager.getInstance()
				.getCategoryAlias(alias, getCircleID(subscriberID),
						isPrepaid ? 'y' : 'n');
		if (category == null) {
			setReturnValues(z, getSMSTextForID(z, "CATEGORY_SEARCH_FAILURE",
					m_catRbtFailure1Default), STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
			return;
		}
		/*
		 * StringTokenizer catStk = new StringTokenizer(categoryDetails, ",");
		 * String categoryIDStr = null; String categoryName = null;
		 * if(catStk.hasMoreTokens()) categoryIDStr = catStk.nextToken();
		 * if(catStk.hasMoreTokens()) categoryName = catStk.nextToken();
		 */

		int categoryid = category.id();
		String categoryName = category.name();

		String clipIDs = "" + categoryid;
		Clips[] clips = getInListClips(categoryid);
		if (clips == null || clips.length == 0) {
			setReturnValues(z, getSMSTextForID(z, "CATEGORY_SEARCH_FAILURE",
					m_catRbtFailure1Default), STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
			return;
		}

		for (int i = 0; i < clips.length; i++) {
			try {
				if (i < m_maxClipSearched)
					clipIDs = clipIDs + "," + clips[i].id();
				else
					break;
			} catch (Exception e) {
				System.out.println("exception is" + e);
			}
		}
		int noOfClips = clips.length;
		if (noOfClips > m_reqMaxSMSCat)
			noOfClips = m_reqMaxSMSCat;
		String match = "";
		StringTokenizer clipTokens = new StringTokenizer(clipIDs, ",");
		clipTokens.nextToken();
		int iSong = 0;
		String song = null;
		int id1 = -1;
		ClipMinimal clip = null;
		while (clipTokens.hasMoreTokens()) {
			// int id = 0;
			try {
				id1 = Integer.parseInt(clipTokens.nextToken());
			} catch (Exception e) {
				continue;
			}
			clip = getClip(id1);
			if (iSong < m_reqMaxSMSCat) {
				song = clip.getClipName();
				if (m_model.equalsIgnoreCase("AirtelSMSImpl")) {
					if (isTopFeature) {
						String tpKeyword = "0" + (iSong + 1);
						tpKeyword = tpKeyword.substring(tpKeyword.length() - 2,
								tpKeyword.length());
						match = match + song + "-TP" + tpKeyword + " ";
					} else if (clip.getPromoID() != null)
						match = match + song + "-" + clip.getPromoID() + " ";
				} else {
					if (m_insertSearchNumberAtEnd)
						match = match + song + "-" + (iSong + 1) + " ";
					else
						match = match + (iSong + 1) + "-" + song + " ";
				}
			} else
				break;

			iSong++;
		}
		removeViralSMSTable(subscriberID, "CATEGORY");
		if (m_model.equalsIgnoreCase("AirtelSMSImpl"))
			removeViralSMSTable(subscriberID, "REQUEST");
		if (isTopFeature)
			clipIDs = "TOP:" + clipIDs;
		insertViralSMSTable(subscriberID, null, clipIDs, "CATEGORY", 1);
		String sms = getSubstituedSMS(
				match.trim()
						+ getSMSTextForID(z, "CATEGORY_SEARCH",
								m_catRbtSuccess1Default), "" + m_reqMaxSMSCat,
				categoryName);
		if (!isSubActive(subscriber, z))
			sms += getSMSTextForID(z, "NON_SUBSCRIBER_SMS", "");
		if (clips.length > m_reqMaxSMSCat)
			sms = sms
					+ getSMSTextForID(z, "REQUEST_MORE_CAT",
							m_reqMoreSMSCatDefault);

		String[] tempSMS = parseText(sms);
		if (tempSMS != null) {
			match = "";
			for (int i = 0; i < tempSMS.length; i++) {
				if (match.equalsIgnoreCase(""))
					match = tempSMS[i];
				else
					match = match + "  " + tempSMS[i];
			}
		}
		setReturnValues(z, match, STATUS_SUCCESS);
		if (m_sendSMS)
			sendSMS(subscriberID, (String) z.get(RETURN_STRING));
	}

	private void setTempOverride(HashMap z, ArrayList smsList) {
		// String _method = "setTempOverride()";
		// logger.info("****** parameters are -- "+z + "&
		// "+smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String reqType = (String) getFromZTable(z, REQUEST_TYPE);

		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		if (isActivationRequest && isSubActive(subscriber, z)
				&& !m_allowReactivation) {
			if (!reqType.equals("VALIDATE")
					|| (reqType.equals("VALIDATE") && (subscriber.subYes()
							.equals("B") || subscriber.subYes().equals("O")))) {
				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_FAILURE",
						m_activationFailureDefault), STATUS_SUCCESS);
				if (m_sendSMS)
					sendSMS(subscriberID, (String) z.get(RETURN_STRING));
				return;
			}
		}
		if (!isActivationRequest && !isSubActive(subscriber, z)
				&& !m_isActOptional) {
			// //logger.info("RBT::subscriber " +
			// subscriberID + " is deactivated");
			String helpText = getSMSTextForID(z, "ERROR", m_errorDefault)
					+ getSMSTextForID(z, "HELP", m_helpDefault);
			setReturnValues(z, helpText, STATUS_NOT_AUTHORIZED);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) z.get(RETURN_STRING));
			return;
		}

		double duration = 1.0;
		if (smsList.size() > 1)
			duration = checkDuration(((String) smsList.get(1)).toLowerCase());
		// //logger.info("RBT::sms profile duration
		// "+duration);
		// //logger.info("RBT::sms profile requested
		// "+(String)smsList.get(0));

		String value = checkDB((String) smsList.get(0));
		if (value.equalsIgnoreCase(m_strHELP)) {
			// logger.info("****** 6");
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault), STATUS_NOT_AUTHORIZED);
			if (m_sendSMS)
				sendSMS((String) z.get(SMS_SUBSCRIBER_ID), (String) z
						.get(RETURN_STRING));
			return;
		}
		String clipName = null;
		Clips clipDefault = getProfileClip(z, (String) smsList.get(0));
		clipName = clipDefault.name();
		boolean isDurationDays = false;
		if (clipDefault != null && clipDefault.alias() != null
				&& isDurationDays(clipDefault.alias()))
			isDurationDays = true;
		// //logger.info("RBT::sms profile
		// language "+(String)getFromZTable(z, LANGUAGE));
		String wavFile = renameWavFile(value, (String) getFromZTable(z,
				LANGUAGE));
		ClipMinimal langClip = null;
		if (!value.equalsIgnoreCase(wavFile)) {
			langClip = getClipRBT(z, wavFile);
		}

		
		String clipWavFile = null;
		if (clipDefault == null && langClip == null) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return;
		} else {
			if (langClip != null) {
				clipName = langClip.getClipName();
				clipWavFile = langClip.getWavFile();
			}
		}

		if (isActivationRequest) {

			if (m_promoActBy == null) {
				addToHashMap(z, ACT_BY, "PROMO");
				addToHashMap(z, SMS_SELECTED_BY, "PROMO");
			} else {
				addToHashMap(z, ACT_BY, m_promoActBy);
				addToHashMap(z, SMS_SELECTED_BY, m_promoActBy);
			}
		}
		if (isActivationRequest || !isSubActive(subscriber, z))
			// if (!handleActivation(z))
			if (!handleActivation(z, true))
				return;
		subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String dur = "hour";
		Calendar cal = Calendar.getInstance();
		if (isDurationDays) {
			cal.add(Calendar.DATE, new Double(duration).intValue());
			dur = "day";
		} else
			cal.add(Calendar.HOUR_OF_DAY, new Double(duration).intValue());
		Date endDate = cal.getTime();
		if (subscriber != null) {
			boolean OptIn = false;
			if (subscriber.activationInfo() != null
					&& subscriber.activationInfo().indexOf(":optin:") != -1)
				OptIn = true;

			if (!addSelections(subscriberID, null, subscriber.prepaidYes(),
					false, m_categoryID, clipWavFile, null, endDate, 99, 0,
					(String) z.get(SMS_SELECTED_BY), (String) z
							.get(SMS_SELECTION_INFO), null, null, subscriber
							.subYes(), subscriber.maxSelections(), subscriber
							.subscriptionClass(), null, (String) getFromZTable(
							z, REQUEST_TYPE), z, OptIn))
				return;

		}
		setReturnValues(z, getSubstituedSMS(
				getSMSTextForID(z, "TEMPORARY_OVERRIDE_SUCCESS",
						m_temporaryOverrideSuccessDefault), clipName, dur, ""
						+ new Double(duration).intValue()), STATUS_SUCCESS);
		if (m_sendSMS)
			sendSMS((String) z.get(SMS_SUBSCRIBER_ID), (String) z
					.get(RETURN_STRING));

	}

	private void processFEED1(HashMap z, ArrayList smsList) throws Exception {
		// String _method = "updateSubscriberFeed()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+smsList);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		if (isCorpSub(subscriberID, z) && m_corpChangeSelectionBlock) {
			setReturnValues(z, getSMSTextForID(z,
					"CORP_CHANGE_SELECTION_ALL_FAILURE",
					m_corpChangeSelectionFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}

		boolean isActivationRequest = ((Boolean) getFromZTable(z,
				IS_ACTIVATION_REQUEST)).booleanValue();
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		String reqType = (String) getFromZTable(z, REQUEST_TYPE);

		if (isActivationRequest && isSubActive(subscriber, z)
				&& !m_allowReactivation) {
			if (!reqType.equals("VALIDATE")
					|| (reqType.equals("VALIDATE") && (subscriber.subYes()
							.equals("B") || subscriber.subYes().equals("O")))) {
				setReturnValues(z, getSMSTextForID(z, "ACTIVATION_FAILURE",
						m_activationFailureDefault), STATUS_SUCCESS);
				return;
			}
		}
		if (!isActivationRequest && !isSubActive(subscriber, z)
				&& !m_isActOptional) {
			setReturnValues(z, getSMSTextForID(z, "FEED1_NON_ACTIVE",
					m_feed1NonActiveTextDefault), STATUS_SUCCESS);
			return;
		}

		subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String token = null;
		String pass = null;
		String status = null;
		if (smsList.size() > 0) {
			token = (String) smsList.get(0);
			if (!token.equalsIgnoreCase("ON") && !token.equalsIgnoreCase("OFF"))
				pass = token;
			else
				status = token;
		}
		if (pass == null && smsList.size() > 1)
			pass = ((String) smsList.get(1));

		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (status != null && status.equalsIgnoreCase("OFF")) {
			SubscriberStatus[] subscriberStatus = getSubscriberRecords(
					subscriberID, rbtType);
			boolean cricket = false;

			if (subscriberStatus != null) {
				int index = 0;
				while (index < subscriberStatus.length) {
					if (subscriberStatus[index].status() == 90) {
						cricket = true;
						deactivateSubscriberRecords(subscriberID, null, 90,
								rbtType);
						setReturnValues(z, getSMSTextForID(z,
								"FEED1_OFF_SUCCESS",
								m_feed1OffSuccessTextDefault), STATUS_SUCCESS);
						break;
					}
					index++;
				}
			}

			if (!cricket)
				setReturnValues(z, getSMSTextForID(z, "FEED1_OFF_FAILURE",
						m_feed1OffFailureTextDefault), STATUS_TECHNICAL_FAILURE);

			if (m_sendSMS)
				sendSMS(subscriberID, (String) getFromZTable(z, RETURN_STRING));
			return;
		}

		// If control comes here, implies the wants to add a feed selection.
		if (isActivationRequest) {
			if (m_promoActBy == null) {
				addToHashMap(z, ACT_BY, "PROMO");
				addToHashMap(z, SMS_SELECTED_BY, "PROMO");
			} else {
				addToHashMap(z, ACT_BY, m_promoActBy);
				addToHashMap(z, SMS_SELECTED_BY, m_promoActBy);
			}
		}

		if (isActivationRequest || !isSubActive(subscriber, z))
			// if (!handleActivation(z))
			if (!handleActivation(z, true))
				return;
		subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		//Changed by Sreekar to support pack upgradation
		if (!m_rbtDBManager.allowFeedUpgrade() && isSubAlreadyActiveOnStatus(subscriberID, null, 90, rbtType)) {
			setReturnValues(z, getSMSTextForID(z, "FEED1_ON_FAILURE",
					m_feed1OnFailureTextDefault), STATUS_TECHNICAL_FAILURE);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) getFromZTable(z, RETURN_STRING));
			return;
		}

		if (!m_cricketPass)
			pass = null;
		else if (pass == null || !m_cricketSubKey.contains(pass))
			pass = m_defaultCricketPass;
		if (pass != null)
			pass = pass.toUpperCase();
		String feedFile = null;
		if (m_feedStatus != null) {
			feedFile = m_feedStatus.file();
			if (feedFile != null && feedFile.indexOf(",") != -1) {
				feedFile = feedFile.substring(feedFile.lastIndexOf(",") + 1);
			}
			if (m_feedStatus.status().equalsIgnoreCase("OFF"))
				feedFile = null;
		}

		if (m_cricketPass) {
			// there is a match of requested pass
			FeedSchedule schedule = getCricketClass(pass);

			// there is no current match
			if (schedule == null)
				schedule = getNextCricketSchedule(pass, m_cricketInterval);

			if (schedule == null) {
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"FEED1_FAILURE", m_feed1FailureTextDefault),
						new Integer(m_cricketInterval).toString(), null),
						STATUS_TECHNICAL_FAILURE);
				if (m_sendSMS)
					sendSMS(subscriberID, (String) getFromZTable(z,
							RETURN_STRING));
			} else {
				//Added by Sreekar to support pack upgradation
				if (m_rbtDBManager.allowFeedUpgrade()) {
					SubscriberStatus cricSel = m_rbtDBManager.getActiveSubscriberRecord(
							subscriberID, null, 90, 0, 2359);
					if (cricSel != null
							&& (cricSel.endTime().after(schedule.endTime()) || cricSel.endTime()
									.equals(schedule.endTime()))) {
						setReturnValues(z, getSMSTextForID(z, "FEED1_ON_FAILURE",
								m_feed1OnFailureTextDefault), STATUS_TECHNICAL_FAILURE);
						if (m_sendSMS)
							sendSMS(subscriberID, (String) getFromZTable(z, RETURN_STRING));
						return;
					}
				}
				String classType = schedule.classType();
				if (subscriber != null) {
					boolean OptIn = false;
					if (subscriber.activationInfo() != null
							&& subscriber.activationInfo().indexOf(":optin:") != -1)
						OptIn = true;

					if (!addSelections(subscriberID, null, subscriber
							.prepaidYes(), false, 10, feedFile, schedule
							.startTime(), schedule.endTime(), 90, 0, "SMS",
							(String) getFromZTable(z, ACT_INFO), classType,
							null, subscriber.subYes(), subscriber
									.maxSelections(), subscriber
									.subscriptionClass(), null,
							(String) getFromZTable(z, REQUEST_TYPE), z, OptIn))
						return;
				}

				setReturnValues(z, schedule.smsFeedOnSuccess(), STATUS_SUCCESS);
				if (m_sendSMS)
					sendSMS(subscriberID, (String) getFromZTable(z,
							RETURN_STRING));
			}
		} else {
			// Calendar endCal = Calendar.getInstance();
			// endCal.set(2037, 0, 1);
			// Date endDate = endCal.getTime();
			Date endDate = m_endDate;

			if (subscriber != null) {
				boolean OptIn = false;
				if (subscriber.activationInfo() != null
						&& subscriber.activationInfo().indexOf(":optin:") != -1)
					OptIn = true;

				addSelections(subscriberID, null, subscriber.prepaidYes(),
						false, 10, feedFile, null, endDate, 90, 0, "SMS",
						(String) getFromZTable(z, ACT_INFO), null, null,
						subscriber.subYes(), subscriber.maxSelections(),
						subscriber.subscriptionClass(), null,
						(String) getFromZTable(z, REQUEST_TYPE), z, OptIn);
			}
			setReturnValues(z, getSMSTextForID(z, "FEED1_ON_SUCCESS",
					m_feed1OnSuccessTextDefault), STATUS_SUCCESS);
			if (m_sendSMS)
				sendSMS(subscriberID, (String) getFromZTable(z, RETURN_STRING));
		}
	}

	private void getMoreClips(HashMap z, String type) {
		// String _method = "getMoreClips()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+type);

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		boolean isTopList = false;
		ViralSMSTable context = null;
		if (m_model.equalsIgnoreCase("AirtelSMSImpl")) {
			context = getViralSMSTable(subscriberID, "CATEGORY");
			if (context != null)
				type = "CATEGORY";
			else
				context = getViralSMSTable(subscriberID, "REQUEST");
		} else
			context = getViralSMSTable(subscriberID, type);

		if (context == null || context.clipID() == null) {
			if (type.equalsIgnoreCase("REQUEST"))
				setReturnValues(z, getSMSTextForID(z, "REQUEST_MORE_NO_SEARCH",
						m_reqMoreSMSNoSearchDefault), STATUS_TECHNICAL_FAILURE);
			else
				setReturnValues(z, getSMSTextForID(z,
						"REQUEST_MORE_CAT_NO_SEARCH",
						m_reqMoreSMSNoSearchCatDefault),
						STATUS_TECHNICAL_FAILURE);
			return;
		}

		if (!(isSubActive(subscriber, z) || m_isActOptional || m_isActOptionalRequestRBT)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_NOT_AUTHORIZED);
			return;
		}

		boolean valid = false;
		boolean isRetailer = ((Boolean) getFromZTable(z, IS_RETAILER))
				.booleanValue();
		String clipIDs = context.clipID();
		int searchCount = context.count();
		String categoryId = null;
		int categoryID = -1;
		String catName = null;
		Categories cat = null;
		StringTokenizer stk = new StringTokenizer(clipIDs, ",");

		if (type.equalsIgnoreCase("CATEGORY") && stk.hasMoreTokens()) {
			categoryId = stk.nextToken();
			if (categoryId.startsWith("TOP:")) {
				isTopList = true;
				categoryId = categoryId.substring(4);
			}
			categoryID = Integer.parseInt(categoryId);
			cat = getCategory(categoryID, getCircleID(subscriberID));
			catName = cat.name();
		}

		int perSMSCount = 0;
		if (type.equalsIgnoreCase("REQUEST"))
			perSMSCount = m_reqMaxSMS;
		else
			perSMSCount = m_reqMaxSMSCat;

		if (stk.countTokens() > perSMSCount * searchCount)
			valid = true;
		if (!valid) {
			if (isRetailer && type.equalsIgnoreCase("REQUEST"))
				z.put(RETURN_STRING, getSMSTextForID(z,
						"REQUEST_RET_MORE_EXHAUSTED",
						m_reqMoreRetSMSExhaustedDefault));
			else {
				if (type.equalsIgnoreCase("REQUEST"))
					setReturnValues(z, getSMSTextForID(z,
							"REQUEST_MORE_EXHAUSTED",
							m_reqMoreSMSExhaustedDefault),
							STATUS_NOT_AUTHORIZED);
				else
					setReturnValues(z, getSMSTextForID(z,
							"REQUEST_MORE_CAT_EXHAUSTED",
							m_reqMoreSMSExhaustedCatDefault),
							STATUS_NOT_AUTHORIZED);
			}
			return;
		}

		for (int a = 0; a < perSMSCount * searchCount; a++)
			stk.nextToken();

		String match = "";
		int iSong = 0;
		for (int i = 1; i <= perSMSCount; i++) {
			if (stk.hasMoreTokens()) {
				String song = "";
				String id = (String) stk.nextToken();
				int clipID = Integer.parseInt(id.trim());
				ClipMinimal clip = getClip(clipID);
				song = clip.getClipName();
				if (type.equalsIgnoreCase("REQUEST")) {
					if (m_addMovieRequest && clip.getAlbum() != null
							&& clip.getAlbum().length() > 0)
						song = song + "," + clip.getAlbum(); 
				}

				if ((isRetailer && type.equalsIgnoreCase("REQUEST"))
						|| (m_model.equalsIgnoreCase("AirtelSMSImpl"))) {
					if (isTopList) {
						String tpKeyword = "0"
								+ (i + perSMSCount * searchCount);
						tpKeyword = tpKeyword.substring(tpKeyword.length() - 2,
								tpKeyword.length());
						match = match + song + "-TP" + tpKeyword + " ";
					} else if (clip.getPromoID() != null)
						match = match + song + "-" + clip.getPromoID() + " ";
					else
						match = match + song + " ";
				} else {
					if (m_insertSearchNumberAtEnd)
						match = match + song + "-"
								+ (perSMSCount * searchCount + i) + " ";
					else
						match = match + (perSMSCount * searchCount + i) + "-"
								+ song + " ";
				}
			} else
				break;
			iSong++;
		}

		String sms = null;
		if (type.equalsIgnoreCase("REQUEST"))
			sms = match.trim()
					+ getSMSTextForID(z, "REQUEST_RBT_SMS1_SUCCESS",
							m_requestRbtSuccess1Default);
		else
			sms = getSubstituedSMS(match.trim()
					+ getSMSTextForID(z, "CATEGORY_SEARCH",
							m_catRbtSuccess1Default), "" + perSMSCount, catName);
		if (isRetailer)
			sms = match.trim();
		else if (!isSubActive(subscriber, z))
			sms = sms + getSMSTextForID(z, "NON_SUBSCRIBER_SMS", "");
		if (stk.hasMoreTokens()) {
			if (type.equalsIgnoreCase("REQUEST"))
				sms = sms
						+ getSMSTextForID(z, "REQUEST_MORE",
								m_reqMoreSMSDefault);
			else
				sms = sms
						+ getSMSTextForID(z, "REQUEST_MORE_CAT",
								m_reqMoreSMSCatDefault);
		}
		String[] tempSMS = parseText(sms);
		if (tempSMS != null) {
			match = "";
			for (int i = 0; i < tempSMS.length; i++) {
				if (match.equalsIgnoreCase(""))
					match = tempSMS[i];
				else
					match = match + "  " + tempSMS[i];
			}
		}
		searchCount++;
		setSearchCount(subscriberID, searchCount, type);
		setReturnValues(z, match, STATUS_SUCCESS);
		if (m_sendSMS)
			sendSMS(subscriberID, (String) z.get(RETURN_STRING));
	}

	/*
	 * private String prepareSMSText(String str) { //String _method =
	 * "prepareSMSText()"; ////logger.info("******
	 * parameters are -- "+str); String[] tempSMS = parseText(str); if (tempSMS !=
	 * null) { str = ""; for (int i = 0; i < tempSMS.length; i++) { if
	 * (str.equalsIgnoreCase("")) str = tempSMS[i]; else str = str + " " +
	 * tempSMS[i]; } } return str; }
	 */

	public ArrayList requestSearchOn(HashMap z, ArrayList smsList) {
		// String _method = "requestSearchOn()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+smsList);
		String searchType = "song";
		int i = 0;
		for (i = 0; i < smsList.size(); i++) {
			if (m_requestRBTsearchOn.containsKey(smsList.get(i)))
			{
				searchType = (String) m_requestRBTsearchOn.get(smsList.get(i));
				smsList.remove(i);
				break;
			}
		}
		addToHashMap(z, SEARCH_TYPE, searchType);
		return smsList;
	}

	public ClipMinimal getClipPromoID(String promoID) {
		String _method = "getClipPromoID()";
		logger.info("****** parameters are --"+promoID);
		String promotionID = promoID;
		if (promotionID != null)
			promotionID = promotionID.toLowerCase();
		if (m_removeLeadingZero && promotionID != null
				&& promotionID.startsWith("0"))
			promotionID = promotionID.substring(1);

		ClipMinimal clipMinimal = RBTDBManager.getInstance()
				.getClipMinimalPromoID(promoID, true);
		// ClipMinimal clipMinimal = (ClipMinimal) m_clips.get(promotionID);

		/*
		 * ClipMinimal clipMinimal = null;
		 * if(m_clipIDPromoID.containsKey(promotionID)) clipMinimal =
		 * (ClipMinimal) m_clips.get(m_clipIDPromoID.get(promotionID));
		 */

		if (clipMinimal == null && m_checkClipSMSAlias) {
			clipMinimal = m_rbtDBManager.getClipSMSAlias(promotionID);
		}
		logger.info("The clipMinimal - "+clipMinimal);
		return clipMinimal;
	}

	
	public ClipMinimal getClipPromoID(String promoID,boolean checkMap) {
		// String _method = "getClipPromoID()";
		// //logger.info("****** parameters are --
		// "+promoID);
		String promotionID = promoID;
		if (promotionID != null)
			promotionID = promotionID.toLowerCase();
		if (m_removeLeadingZero && promotionID != null
				&& promotionID.startsWith("0"))
			promotionID = promotionID.substring(1);
		logger.info("promoid " + promoID);
		ClipMinimal clipMinimal = RBTDBManager.getInstance()
				.getClipMinimalPromoID(promoID, checkMap);
		// ClipMinimal clipMinimal = (ClipMinimal) m_clips.get(promotionID);

		/*
		 * ClipMinimal clipMinimal = null;
		 * if(m_clipIDPromoID.containsKey(promotionID)) clipMinimal =
		 * (ClipMinimal) m_clips.get(m_clipIDPromoID.get(promotionID));
		 */

		if (clipMinimal == null && m_checkClipSMSAlias) {
			clipMinimal = m_rbtDBManager.getClipSMSAlias(promotionID);
		}
		return clipMinimal;
	}

	
	public Categories getCategoryPromoID(String promoID, String circleId,
			char prepaidYes) {
		// String _method = "getCategoryPromoID()";
		// //logger.info("****** parameters are --
		// "+promoID);
		String promotionID = promoID;
		if (promotionID != null)
			promotionID = promotionID.toLowerCase();
		if (m_removeLeadingZero && promotionID != null
				&& promotionID.startsWith("0"))
			promotionID = promotionID.substring(1);
		return (m_rbtDBManager.getCategoryPromoID(promotionID, circleId,
				prepaidYes));
	}

	private String checkDB(String strValue) {
		// String _method = "checkDB()";
		// //logger.info("****** parameters are --
		// "+strValue );
		Clips clip = (Clips) m_profileClips.get(strValue);
		if (clip == null)
			return m_strHELP;
		if (clip.addAccess()) {
			Date date = new Date(System.currentTimeMillis());
			DateFormat timeFormat = new SimpleDateFormat("yyyy");
			String year = timeFormat.format(date);
			timeFormat = new SimpleDateFormat("MM");
			String month = timeFormat.format(date);
			Access access = getAccess(clip.id(), clip.name(), year, month);
			access.incrementNoOfPreviews();
			access.update(null, m_nConn);
		}
		return clip.wavFile();
	}

	public String getSubstituedSMS(String smsText, String str1, String str2) {
		// String _method = "getSubstitutedSMS()";
		// //logger.info("****** parameters are --
		// "+smsText + " & "+str1 + " & "+str2);
		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
		}

		return smsText;
	}

	public String getSubstituedSMS(String smsText, String str1, String str2,
			String str3) {
		// String _method = "getSubstitutedSMS()";
		// //logger.info("****** parameters are --
		// "+smsText + " & "+str1 + " & "+str2);
		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			
			if(str3 != null)
			{
				try
				{
					String str3Clone = str3;
					Date d = dummyDateFormat.parse(str3);
					str3Clone = expiryDateFormat.format(d);
					str3 = str3Clone;
				}
				catch(Exception e)
				{
				}
			}
			
			while (smsText.indexOf("%K") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%K")) + str3
						+ smsText.substring(smsText.indexOf("%K") + 2);
			}

			while (smsText.indexOf("%X") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%X")) + str3
						+ smsText.substring(smsText.indexOf("%X") + 2);
			}

		}

		return smsText;
	}

	public long sendSMS(String strSubID, String strSms) {
		// String _method = "sendSMS()";
		// logger.info("****** parameters are -- "+strSubID
		// + " & "+strSms );
		long smsSuccess = -1;

		String[] smsTexts = parseText(strSms);

		if (smsTexts != null) {
			try {
				String[] subscribers = new String[1];
				subscribers[0] = strSubID;
				String smsText = null;
				for (int i = 0; i < smsTexts.length; i++) {
					smsText = smsTexts[i];
					if ((smsTexts.length != 1) && (m_putPageNoStart)) {
						smsText = (i + 1) + "/" + smsTexts.length + " "
								+ smsTexts[i];
					} else if ((smsTexts.length != 1) && (!m_putPageNoStart)) {
						smsText = smsTexts[i] + " " + (i + 1) + "/"
								+ smsTexts.length;
					}

					// //logger.info("RBT::SMS text is "
					// +smsText);
					// //logger.info("RBT::SMS no is "
					// +m_smsNo);
					try {
						Tools.sendSMS(m_smsNo, strSubID, smsText, false);
					} catch (Exception e) {
						return smsSuccess;
					}
				}
				// //logger.info("RBT::SMS sent
				// successfully to " +strSubID);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return smsSuccess;
	}

	private double checkDuration(String strValue) {
		// String _method = "checkDuration()";
		// //logger.info("****** parameters are --
		// "+strValue);
		try {
			return DecimalFormat.getInstance().parse(strValue).doubleValue();
		} catch (ParseException e) {
			// //logger.info("RBT::ParseException
			// caught " +e.getMessage());
			return 1.0;
		}
	}

	private String[] getRequestRBTClips() {
		//String _method = "getRequestRBTClips()";
		//logger.info("****** no parameters.");
		/*
		 * String category = m_profileCorporateCategories; Categories[]
		 * categories = m_rbtDBManager.getBouquetForSMS(); if (categories !=
		 * null && categories.length > 0) { category = category + ","; for (int
		 * i = 0; i < categories.length; i++) { category = category + new
		 * Integer(categories[i].id()).toString(); if (i < (categories.length -
		 * 1)) { category = category + ","; } } }
		 */
		StringBuffer category = new StringBuffer(m_profileCorporateCategories);
		ArrayList activeShuffles = m_rbtDBManager.getActiveShuffleCategoryIDs();
		for (int i = 0; activeShuffles != null && (i<998 && i < activeShuffles.size()); i++) {
			category.append(",");
			category.append(((Integer) activeShuffles.get(i)).intValue());
		}
		return (m_rbtDBManager.getClipsNotInCategories(category.toString()));
	}

	public String[] parseText(String s) {
		// String _method = "parseText()";
		// //logger.info("****** parameters are -- "+s);
		int index = 154;
		ArrayList list = new ArrayList();
		String t = null;
		while (s.length() != 0) {
			index = 154;
			if (s.length() <= 154) {
				t = s;
				s = "";
			} else {
				while (index >= 0 && s.charAt(index) != ' ')
					index--;
				t = s.substring(0, index);
				s = s.substring(index + 1);
			}
			list.add(t);
		}

		if (list.size() > 0) {
			String[] smsTexts = (String[]) list.toArray(new String[0]);
			return smsTexts;
		} else {
			return null;
		}

	}

	private boolean isSubActiveForDays(String strSubID, int days) {
		// String _method = "isSubActiveForDays()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+days);
		Subscriber sub = getSubscriber(strSubID);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);

		if (sub == null || sub.activationDate() == null
				|| sub.activationDate().after(cal.getTime())) {
			return false;
		}

		return true;
	}

	private boolean isSubMoreGifts(String strSubID, int days) {
		// String _method = "isSubMoreGifts()";
		// //logger.info("****** parameters are --
		// "+strSubID + " & "+days );
		ViralSMSTable[] viral = getViralSMSesByType(strSubID, "MGM");
		if (viral == null || viral.length < days) {
			return false;
		}

		return true;
	}

	public String formatResult(String smsText, String response, int returnCode,
			String transactionId) {
		 String _method = "formatResult()";
        logger.info("****** parameters are -- "+smsText + " & "+response + " & "+returnCode+ " & ");
		StringTokenizer st = new StringTokenizer(smsText, " ");
		boolean shouldFormat = false;
		String formattedString = null;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if ((m_promotion1 != null && m_promotion1.toUpperCase().startsWith(
					token.toUpperCase()))
					|| (m_songPromotion1 != null && m_songPromotion1
							.toUpperCase().startsWith(token.toUpperCase()))) {
				shouldFormat = true;
			}
		}

        logger.info("****** parameters are -- "+smsText + " & "+response + " & "+returnCode+ " & "+shouldFormat);

		if (shouldFormat) {
			if (transactionId == null)
				transactionId = "";
			if (returnCode == STATUS_SUCCESS) {
				// //logger.info("return Code: SUCCESS");
				if (m_promotion1ValidFormat == null
						|| m_promotion1ValidFormat.trim().equalsIgnoreCase("")
						|| m_promotion1ValidFormat.trim().equalsIgnoreCase(
								"null"))
					return response;
				if (response != null
						&& response.trim().equalsIgnoreCase(
								"Request completed successfully")) {
					formattedString = Tools.findNReplaceAll(
							m_promotion1InvalidFormat, "%R",
							"The song code you have sent is invalid");
				} else {
					formattedString = Tools.findNReplaceAll(
							m_promotion1ValidFormat, "%R", response);
				}
				formattedString = Tools.findNReplaceAll(formattedString, "%T",
						transactionId);
				// //logger.info("Returning :" +
				// formattedString);
				return formattedString;
			} else {
				// //logger.info("return Code: FAILURE");
				if (m_promotion1InvalidFormat == null
						|| m_promotion1InvalidFormat.trim()
								.equalsIgnoreCase("")
						|| m_promotion1InvalidFormat.trim().equalsIgnoreCase(
								"null"))
					return response;
				formattedString = Tools.findNReplaceAll(
						m_promotion1InvalidFormat, "%R", response);
				formattedString = Tools.findNReplaceAll(formattedString, "%T",
						transactionId);
				// //logger.info("Returning :" +
				// formattedString);
				return formattedString;
			}
		} else {
			// //logger.info("No formatting required.
			// Returning the sms string...");
			return response;
		}
	}

	public void loadSMSTexts() {
		// String _method = "loadSMSTexts()";
		// //logger.info("****** no parameters.");
		Hashtable smsTable = new Hashtable();
		BulkPromoSMS[] bulkPromoSMSes = getBulkPromoSmses();
		String object = null;
		if (bulkPromoSMSes != null && bulkPromoSMSes.length > 0) {
			for (int i = 0; i < bulkPromoSMSes.length; i++) {
				// String object = null;
				if (bulkPromoSMSes[i].bulkPromoId() != null) {
					object = bulkPromoSMSes[i].bulkPromoId();
					if (bulkPromoSMSes[i].smsDate() != null) {
						object = object + "_" + bulkPromoSMSes[i].smsDate();
					}
				}

				if (object != null)
					smsTable.put(object, bulkPromoSMSes[i].smsText());
			}
			m_smsTable = smsTable;

		}
	}

	public String getSMSTextForID(HashMap z, String SMSID, String defaultText) {
		// String _method = "getSMSTextForID()";
		// //logger.info("****** parameters are -- "+SMSID
		// + " & "+defaultText );
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			SMSID = "REV_" + SMSID;
		String smsText = null;
		if (!m_smsTable.containsKey(SMSID))
			return defaultText;

		smsText = (String) m_smsTable.get(SMSID);

		if (smsText != null && smsText.length() > 0)
			return smsText;
		else
			return defaultText;
	}

	public boolean checkRevRBTKeyword(HashMap z, ArrayList smsList) {
		// String _method = "checkRBTKeyword()";
		// //logger.info("****** parameters are --
		// "+smsList);

		if (isThisFeature(smsList, m_rrbtKeyword)) {
			addToHashMap(z, "REV_RBT", "TRUE");
			return true;
		}
		return false;
	}

	public boolean checkRBTKeyword(ArrayList smsList) {
		// String _method = "checkRBTKeyword()";
		// //logger.info("****** parameters are --
		// "+smsList);
		isThisFeature(smsList, m_rbtKeyword);
		return true;
	}

	private String getFeature(ArrayList strList,
			Hashtable featureNameKeywordTable) {
		// String _method = "getFeature()";
		// //logger.info("****** parameters are --
		// "+strList + " & "+featureNameKeywordTable );
		Object objKey = null;
		Object objValue = null;
		Iterator featureListKeys = m_featureNameArrayList.iterator();
		while (featureListKeys.hasNext()) {
			objKey = featureListKeys.next();
			if (m_featureNameKeywordTable.containsKey(objKey)) {
				objValue = featureNameKeywordTable.get(objKey);

				if (objValue instanceof String) {
					if (isThisFeature(strList, (String) objValue))
						return (String) objKey;
				} else if (objValue instanceof ArrayList) {
					if (isThisFeature(strList, (ArrayList) objValue))
						return (String) objKey;
				}
			}
		}
		return "SET_SELECTION";

	}

	private String firstToken(String stringToTokenize) {
		// String _method = "firstToken()";
		// //logger.info("****** parameters are --
		// "+stringToTokenize);
		if (stringToTokenize == null)
			return null;

		StringTokenizer st = new StringTokenizer(stringToTokenize, ",");
		if (st.hasMoreTokens())
			return st.nextToken();

		return null;
	}

	private void getCategoryAndClipForID(String token, HashMap z) {
		// String _method = "getCategoryAndClipforID()";
		// //logger.info("****** parameters are -- "+token
		// + " & "+z);
		Clips clip = null;
		Category category = null;
		ClipMinimal clipMinimal = null;
		if (token == null)
			return;
		if (token.startsWith("C")) {
			try {
				category = m_rbtDBManager.getCategory(Integer.parseInt(token
						.substring(1)));
				Clips[] categoryClips = getAllClips(category.getID());
				clip = categoryClips[0];
			} catch (Exception e) {
			}
		} else
			clipMinimal = getClip(Integer.parseInt(token));
		if (clipMinimal == null)
			clipMinimal = new ClipMinimal(clip);
		addToHashMap(z, CLIP_OBJECT, clipMinimal);
		addToHashMap(z, CATEGORY_OBJECT, category);
	}

	private boolean minActivationPeriodDisqualification(int minActPeriod,
			Subscriber subscriber) {
		// String _method = "minActivationPeriodDisqualification()";
		if (m_useSubscriptionManager)
			return false;
		// //logger.info("****** parameters are --
		// "+minActPeriod + " & "+subscriber );
		if (minActPeriod == 0 || subscriber == null)
			return false;

		Date activationPeriod = subscriber.endDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(activationPeriod);
		long difference = (System.currentTimeMillis() - calendar.getTime()
				.getTime())
				/ (1000 * 60 * 60);
		long timePeriod = (new Integer(m_activationPeriod)).longValue();
		// //logger.info("RBT::difference "+difference);
		// //logger.info("RBT::timePeriod "+timePeriod);
		if (difference < timePeriod)
			return true;
		return false;
	}

	private boolean isValidClip(HashMap z) {
		// String _method = "isValidClip()";
		// //logger.info("****** parameters are -- "+z );
		ClipMinimal clipMinimal = (ClipMinimal) z.get(CLIP_OBJECT);
		if (clipMinimal == null) {
			setReturnValues(z, getSMSTextForID(z, "TEMPORARY_OVERRIDE_FAILURE",
					m_temporaryOverrideFailureDefault),
					STATUS_TECHNICAL_FAILURE);
			return false;
		} else if (clipMinimal != null
				&& clipMinimal.getSmsTime() != null
				&& clipMinimal.getSmsTime().getTime() > System
						.currentTimeMillis()) {
			setReturnValues(z, getSMSTextForID(z, "CLIP_NOT_AVAILABLE",
					m_clipNotAvailableDefault), STATUS_TECHNICAL_FAILURE);
			return false;
		} else if (clipMinimal != null
				&& clipMinimal.getEndTime() != null
				&& clipMinimal.getEndTime().getTime() < System
						.currentTimeMillis()) {
			setReturnValues(z, getSMSTextForID(z, "CLIP_EXPIRED",
					m_clipExpiredDefault), STATUS_TECHNICAL_FAILURE);
			return false;
		}
		return true;
	}

	public ClipMinimal[] getAllClipsByStartLetter(String letter) {
		// String _method = "getAllClipsByStartLetter()";
		return m_rbtDBManager.getClipsByName(letter);
		/*
		 * Set promoIdSet = m_clips.keySet(); Iterator iter =
		 * promoIdSet.iterator(); SortedMap clipsMap = new TreeMap(); boolean
		 * isStartNumber = false; if (letter.equalsIgnoreCase("[1-9]"))
		 * isStartNumber = true; while (iter.hasNext()) { ClipMinimal
		 * clipMinimal = (ClipMinimal) m_clips.get((String) iter .next()); if
		 * (!isStartNumber) { if
		 * (letter.equalsIgnoreCase(clipMinimal.getStartingLetter())) {
		 * clipsMap.put(clipMinimal.getClipName(), clipMinimal .getPromoID()); } }
		 * else { try { Integer.parseInt(clipMinimal.getStartingLetter());
		 * clipsMap.put(clipMinimal.getClipName(), clipMinimal .getPromoID()); }
		 * catch (Exception e) {
		 *  } } } if (clipsMap.size() == 0) return null; return clipsMap;
		 */
	}

	public boolean getCategoryAndClipForPromoID(String token, HashMap z,
			boolean modifyClassType) {
		String _method = "getCategoryAndClipForPromoID()";
		logger.info("****** parameters are -- "+token + " & "+z + " & "+modifyClassType);

		if (token != null && !isInitializationDone()) {
			setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE",
					m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
			return false;
		}

		String classType = (String) getFromZTable(z, CLASS_TYPE);
		ClipMinimal clipMinimal = null;
		if (token != null)
			clipMinimal = getClipPromoID(token);
		logger.info("the value of clipMinimal is "+clipMinimal);
		Categories category = null;
		if (clipMinimal == null) {
			// Subscriber subscriber = (Subscriber) getFromZTable(z,
			// SUBSCRIBER_OBJ);
			String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
			String circleId = m_rbtDBManager.getCircleId(subscriberID);
			char prepaidYes = 'n';
			boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID))
					.booleanValue();
			if (isPrepaid)
				prepaidYes = 'y';
			category = getCategoryPromoID(token, circleId, prepaidYes);
			if (category != null
					&& category.endTime() != null
					&& category.endTime().getTime() > System
							.currentTimeMillis()) {
				Clips[] clips = getAllClips(category.id());
				if (clips != null) {
					if (modifyClassType && classType != null
							&& !classType.equalsIgnoreCase("DEFAULT"))
						classType = classType + "_SHUFFLE";
					clipMinimal = new ClipMinimal(clips[0]);
					addToHashMap(z, SG_CAT_ID, "" + category.id());
				}
			}
		}
		logger.info("the value of clipObject is "+clipMinimal);
		addToHashMap(z, CLIP_OBJECT, clipMinimal);
		addToHashMap(z, CATEGORY_OBJECT, category);
		addToHashMap(z, CLASS_TYPE, classType);
		// addToZTable(z, SUB_CLASS_TYPE, classType);

		return true;
	}

	private ArrayList getArrayList(String str) {
		// String _method = "getArrayList()";
		// //logger.info("****** parameters are -- "+str );
		ArrayList a = new ArrayList();
		a.add(str.toLowerCase());
		return a;
	}

	private String renameWavFile(String strValue, String lang) {
		// String _method = "renameWavFile()";
		// //logger.info("****** parameters are --
		// "+strValue + " & "+lang);
		if (strValue != null && lang != null) {
			StringTokenizer tkn = new StringTokenizer(strValue, "_");
			String wavFile = "";
			String val1 = null;
			String val2 = null;
			while (tkn.hasMoreTokens()) {
				val1 = tkn.nextToken();
				if (tkn.hasMoreTokens())
					val2 = tkn.nextToken();
			}

			String val = val2;
			if (val2.equalsIgnoreCase("rbt"))
				val = val1;

			wavFile = Tools.findNReplace(strValue, val, lang);

			return wavFile;
		} else
			return strValue;
	}

	public boolean isVodafoneOCGInvalidSMS(String sms) {
		if (m_subMsg != null && m_unsubMsg != null) {
			StringTokenizer stk = new StringTokenizer(sms.toLowerCase(), " ");
			while (stk.hasMoreTokens()) {
				String str = stk.nextToken();
				if (m_subMsg.contains(str) || m_unsubMsg.contains(str)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isThisFeature(ArrayList strList, ArrayList featureKeywords) {
		// String _method = "isThisFeature()";
		// //logger.info("****** parameters are --
		// "+strList + " & "+featureKeywords );
		if (strList == null || featureKeywords == null)
			return false;
		String token = null;
		for (int i = 0; i < strList.size(); i++) {
			token = (String) strList.get(i);
			if (featureKeywords.contains(token)) {
				if (!m_profileList.contains(token))
					strList.remove(token);
				return true;
			}
		}

		return false;
	}

	public boolean isThisFeature(ArrayList strList, String featureKeyword) {
		// String _method = "isThisFeature()";
		// //logger.info("****** parameters are --
		// "+strList + " & "+featureKeyword );
		if (strList == null || featureKeyword == null)
			return false;
		featureKeyword = featureKeyword.toLowerCase();
		if (strList.contains(featureKeyword)) {
			strList.remove(featureKeyword);
			return true;
		}

		return false;
	}

	private String getLang(ArrayList smsList, SitePrefix prefix) {
		// String _method = "getLang()";
		// //logger.info("****** parameters are --
		// "+smsList );
		if (prefix == null) return m_globalDefaultLanguage;
		String languagesSupported = prefix.getSupportedLanguage();
		ArrayList<String> m_supportedLang = Tools.tokenizeArrayList(languagesSupported, ",");
		if (m_supportedLang == null || m_supportedLang.size() == 0) return m_globalDefaultLanguage;
		
		String m_defaultLanguage = m_supportedLang.get(0);
		String lang = null;
		for (int j = 0; j < smsList.size(); j++) {
			lang = (String) smsList.get(j);
			if (m_supportedLang.contains(lang)) {
				smsList.remove(lang);
				return lang;
			}
		}
		return m_defaultLanguage;
	}

	private String getChargingModel(ArrayList smsList) {
		// String _method = "getChargingModel()";
		// //logger.info("****** parameters are --
		// "+smsList );
		if (m_ChargingModelKeywordMap == null
				|| m_ChargingModelKeywordMap.size() == 0)
			return null;
		String token = null;
		Iterator it = (Iterator) m_ChargingModelKeywordMap.keys();
		while (it.hasNext()) {
			String keyName = (String) it.next();
			ArrayList aList = (ArrayList) m_ChargingModelKeywordMap
					.get(keyName);
			if (aList == null || aList.size() <= 0)
				continue;
			for (int i = 0; i < smsList.size(); i++) {
				token = (String) smsList.get(i);
				if (aList.contains(token)) {
					smsList.remove(i);
					return keyName;
				}
			}
		}
		return null;
	}

	private String getSubscriptionType(ArrayList smsList) {
		// String _method = "getSubscriptionType()";
		// //logger.info("****** parameters are --
		// "+smsList );
		if (m_SubscriptionModelKeywordMap == null
				|| m_SubscriptionModelKeywordMap.size() == 0)
			return null;
		String token = null;
		Iterator it = (Iterator) m_SubscriptionModelKeywordMap.keys();
		while (it.hasNext()) {
			String keyName = (String) it.next();
			ArrayList aList = (ArrayList) m_SubscriptionModelKeywordMap
					.get(keyName);
			if (aList == null || aList.size() <= 0)
				continue;
			for (int i = 0; i < smsList.size(); i++) {
				token = (String) smsList.get(i);
				if (aList.contains(token)) {
					smsList.remove(i);
					return keyName;
				}
			}
		}
		return null;
	}

	private boolean handleSelection(HashMap z) {
		// String _method = "handleSelection()";
		// //logger.info("****** parameters are -- "+z );

		// logger.info("!!!!!!! class_type6 is "+(String)
		// getFromZTable(z, CLASS_TYPE));
		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String callerID = (String) getFromZTable(z, CALLER_ID);

		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		if (category == null) {
			// addToHashMap(z, CATEGORY_OBJECT, getCategory(3));
			Categories category1 = getCategory(3, getCircleID(subscriberID));
			if (category1 == null)
				logger.info("Sree:: category 3 is not present in DB");
			addToHashMap(z, CATEGORY_OBJECT, category1);

		}
		category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
		String regexType = (String) getFromZTable(z, REGEX_TYPE);
		String trxID = (String) getFromZTable(z, "TRX_ID");
		String selInfo = (String) getFromZTable(z, SMS_SELECTION_INFO);
		if(trxID != null)
			selInfo = selInfo + ":trxid:"+trxID+":";
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return false;
		}

		if (!isValidClip(z))
			return false;

		int status = 1;
		// if (m_NAVCategoryIDs != null
		// && m_NAVCategoryIDs.contains("" + category.id()))
		// status = 85;
		// Calendar endCal = Calendar.getInstance();
		// endCal.set(2037, 0, 1);
		// Date endDate = endCal.getTime();
		Date endDate = m_endDate;
		if (subscriber != null) {
			boolean OptIn = false;
			if (subscriber.activationInfo() != null
					&& subscriber.activationInfo().indexOf(":optin:") != -1)
				OptIn = true;

			// logger.info("!!!!!!! class_type7 is
			// "+(String) getFromZTable(z, CLASS_TYPE));
			if (category == null)
				logger.info("Sree::category is null, sub is " + subscriberID);
			if (clipMinimal == null)
				logger.info("Sree::clipMinimal is null, sub is " + subscriberID);

			return addSelections(subscriberID, callerID, subscriber
					.prepaidYes(), true, category.id(), clipMinimal
					.getWavFile(), null, endDate, status, 0,
					(String) getFromZTable(z, SMS_SELECTED_BY),
					selInfo,
					(String) getFromZTable(z, CLASS_TYPE), regexType,
					subscriber.subYes(), subscriber.maxSelections(), subscriber
							.subscriptionClass(), (String) getFromZTable(z,
							PROMO_TYPE),
					(String) getFromZTable(z, REQUEST_TYPE), z, OptIn);
		}
		return true;

	}

	private void addToFeatureTable(String key, ArrayList value) {
		if (value != null && value.size() > 0)
			m_featureNameKeywordTable.put(key, value);
		if (!m_featureNameArrayList.contains(key))
			m_featureNameArrayList.add(key);
	}

	private void addToFeatureTable(String key, String value) {
		if (value != null && value.length() > 0)
			m_featureNameKeywordTable.put(key, value);
		if (!m_featureNameArrayList.contains(key))
			m_featureNameArrayList.add(key);
	}

	private void addToHashMap(HashMap z, String key, Object obj) {
		// String _method = "addToZTable()";
		// //logger.info("****** parameters are -- "+z + "
		// and "+ key + " and "+obj );

		if (z != null && key != null & obj != null)
			z.put(key, obj);
	}

	private void addToHashtable(Hashtable z, String key, Object obj) {
		// String _method = "addToZTable()";
		// //logger.info("****** parameters are -- "+z + "
		// and "+ key + " and "+obj );

		if (z != null && key != null & obj != null)
			z.put(key, obj);
	}

	public Object getFromZTable(HashMap z, String key) {
		if (z == null || key == null)
			return null;
		if (z.containsKey(key))
			return z.get(key);
		return null;
	}

	private String getCallerID(ArrayList smsList) {
		// String _method = "getcallerID()";
		// //logger.info("****** parameters are --
		// "+smsList );
		if (smsList.size() == 0)
			return null;
		String token = null;
		for (int i = 0; i < smsList.size(); i++) {
			try {
				token = (String) smsList.get(i);
				// token = subID(token);
				if (token.length() >= m_phoneNumberLength) {
					if (!isInitializationDone())
						return null;
					/*
					 * if(m_clipIDPromoID.containsKey(token)) continue;
					 */
					ClipMinimal clip = m_rbtDBManager.getClipMinimalPromoID(
							token, true);
					if (clip != null)
						continue;
					String callerID = subID(token);
					Long.parseLong(callerID);
					smsList.remove(i);
					return callerID;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	private String replaceSpecialChars(String str) {
		// String _method = "replaceSpecialChars()";
		// //logger.info("****** parameters are -- "+str);
		int n = str.length();
		for (int i = 0; i < n; i++) {
			if (!(str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
					&& !(str.charAt(i) >= 'a' && str.charAt(i) <= 'z')) {
				str = Tools.findNReplace(str, "" + str.charAt(i), "");
				n = str.length();
				i = 0;
			}
		}
		return (str);

	}

	public void setReturnValues(HashMap z, String returnString, int returnCode) {
		// String _method = "setReturnValues()";
		// //logger.info("****** parameters are -- "+z + "
		// & "+returnString + " & "+returnCode);
		if (returnString != null)
			z.put(RETURN_STRING, returnString);
		z.put(RETURN_CODE, "" + returnCode);
	}

	/*
	 * private long timeToSleep() { Calendar now = Calendar.getInstance();
	 * now.set(Calendar.HOUR_OF_DAY, 24); now.set(Calendar.MINUTE, 0);
	 * now.set(Calendar.SECOND, 0);
	 * 
	 * long nexttime = now.getTime().getTime(); return nexttime -
	 * Calendar.getInstance().getTime().getTime(); }
	 */

	public void run() {
		String _method = "run()";
		// logger.info("^^^^^^^^^^^^^^^^^^^^^");
		// while (true)
		if (bUpdateOperatorPrefix) {
			updateOperatorPrefixes();
			updateCircleID();
			bUpdateOperatorPrefix = false;
		} else {
			synchronized (m_init) {
				bInitialized = false;
				System.out.println("Initialization started");
				logger.info("Initialization started");
			}
			try {
				// instantiateClipsCategories(getClips());
				// bInitialized = true;
				doInitializeCache();
				synchronized (m_init) {

					bInitialized = true;
					System.out.println("Content Initialization done");
					logger.info("Content Initialization done");
				}
				if (!bDontUpdateLucene) {
					initializeLuceneClips();
					System.out.println("Lucene Initialization done");
					// if(m_corpBlockSongChangeSomeSongs)
					// bDontUpdateLucene = true;
				}

			} catch (Exception e) {
				System.out.println("Thread Run Exception " + getStackTrace(e));
				System.out.println("Thread Run Exception " + e.getMessage());
				if (e != null) {
					e.printStackTrace();
					System.out.println("Thread Run Exception " + e.toString());
				}
				logger.error("", e);
			} catch (Throwable e) {
				System.out.println("Thread Run Exception " + getStackTrace(e));
				if (e != null) {
					System.out
							.println("Thread Run Exception " + e.getMessage());
					e.printStackTrace();
					System.out.println("Thread Run Exception " + e.toString());
				}
				logger.error("", e);
			}
			/*
			 * synchronized (m_init) { bInitialized = true;
			 * System.out.println("Initialization done");
			 * Tools.logFatalError(_class, _method, "Initialization done"); }
			 */
		}
	}

	private void doInitializeCache() {
		// rbtClipsLucene.createWriter(getRequestRBTClips());
		/*
		 * m_clips = new Hashtable(); m_clipIDPromoID = new Hashtable();
		 * getClips();
		 */
		instantiateClipsCategories();
	}

//	private String getDefaultlanguage() {
//		String result = "eng";
//		if (m_supportedLang != null && m_supportedLang.size() > 0)
//			result = (String) m_supportedLang.get(0);
//		return result;
//	}

	private String getYearlySubscriptionClass(ArrayList smsList) {
		// String _method = "getYearlySubscriptionClass";
		// //logger.info("****** parameters are --
		// "+smsList );

		if (smsList.size() < 1 || m_yearlySubscription.size() < 1)
			return null;
		String subClass = null;
		for (int i = 0; i < m_yearlySubscription.size(); i++) {
			subClass = (String) m_yearlySubscription.get(i);
			if (smsList.contains(subClass.toLowerCase())) {
				smsList.remove(subClass.toLowerCase());
				return subClass.toUpperCase();
			}
		}
		return null;
	}

	public static String getStackTrace(Throwable ex) {
		// String _method = "getStackTrace()";
		// logger.info("****** parameters are -- " + ex);
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if (ex instanceof Exception) {
			Exception exception = (Exception) ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}

	private String getPromoType(ArrayList smsList) {
		String promoType = null;
		ArrayList promoKeyword = null;
		if (m_promoCodeKeywordMap == null || m_promoCodeKeywordMap.size() <= 0)
			return null;
		Iterator i = (Iterator) m_promoCodeKeywordMap.keys();
		while (i.hasNext()) {
			promoType = (String) i.next();
			promoKeyword = (ArrayList) m_promoCodeKeywordMap.get(promoType);
			for (int j = 0; j < smsList.size(); j++)
				if (promoKeyword.contains((String) smsList.get(j))) {
					smsList.remove(j);
					return promoType;
				}
		}
		return null;
	}

	private String getBasicPromoId(String token, String promoType) {
		String promoID = "-1";
		PromoMaster promoMaster = m_rbtDBManager.getPromoForTypeAndCode(
				promoType, token);
		if (promoMaster != null)
			promoID = promoMaster.clipID();
		return promoID;
	}

	private PromoMaster[] getPromoForCode(String promoCode) {
		return m_rbtDBManager.getPromoForCode(promoCode);
	}

	private void smUpdateSelStatus(String strSubID, String callerID,
			String subFile, Date setTime, String initStatus,
			String finalStatus, int rbtType) {
		m_rbtDBManager.smUpdateSelStatus(strSubID, callerID, subFile, setTime,
				initStatus, finalStatus, rbtType);
	}

	private void updateOperatorPrefixes() {
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;
		HashMap temp = new HashMap();
		ArrayList numbers = null;
		for (int i = 0; i < prefixes.size(); i++) {
			numbers = Tools.tokenizeArrayList(prefixes.get(i).getSitePrefix(), null);
			if (prefixes.get(i).getSiteUrl() == null || prefixes.get(i).getSiteUrl().length() <= 0) {
				localSitePrefix = prefixes.get(i);
				continue;
			}
			if (numbers == null || numbers.size() <= 0)
				continue;
			for (int j = 0; j < numbers.size(); j++)
				temp.put((String) numbers.get(j), prefixes.get(i).getSiteUrl());
		}
		m_operatorPrefix = temp;
	}

	// /////////////
	private Date getNextDate(Date nextChargingDate, String chargeperiod) {
		// String _method = "getNextDate";
		// logger.info("****** parameters are --
		// nextChargingDate -> " + nextChargingDate + " || and chargeperiod ->
		// "+chargeperiod);
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

		// logger.info("****** getNextDate::type " + type +
		// " for " + chargeperiod);

		if (type != 4 && type != 5) {
			try {
				number = Integer.parseInt(chargeperiod.substring(1));
			} catch (Exception e) {
				type = 2;
				number = 1;
			}
		}

		calendar1.setTime(nextChargingDate);
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
		// logger.info("****** getNextDate::type " +
		// calendar1.getTime());
		return calendar1.getTime();
	}

	private long getDaysBeforeNextCharging(Subscriber subscriber) {
		if (subscriber == null || subscriber.activationDate() == null)
			return 0;
		String subClassStr = subscriber.subscriptionClass();
		SubscriptionClass sClassObj = getSubscriptionClass(subClassStr);
		if (sClassObj == null || sClassObj.getSubscriptionPeriod() == null
				|| sClassObj.getRenewalPeriod() == null)
			return 0;
		int dayConvertFactor = 1000 * 60 * 60 * 24;
		Date nextChargeDate = subscriber.activationDate();
		Calendar currCal = Calendar.getInstance();
		if (nextChargeDate.after(currCal.getTime()))
			return (nextChargeDate.getTime() - currCal.getTime().getTime())
					/ dayConvertFactor;
		nextChargeDate = getNextDate(nextChargeDate, sClassObj
				.getSubscriptionPeriod());
		if (nextChargeDate.after(currCal.getTime()))
			return (nextChargeDate.getTime() - currCal.getTime().getTime())
					/ dayConvertFactor;
		while (nextChargeDate.before(currCal.getTime()))
			nextChargeDate = getNextDate(nextChargeDate, sClassObj
					.getRenewalPeriod());

		return (nextChargeDate.getTime() - currCal.getTime().getTime())
				/ dayConvertFactor;
	}

	/* Hutch Sub Mngr Change */

	public String connectToRemoteForSM(String strSubID, String strMsg,
			String trxID) {
		String _method = "connectToRemoteForSM()";
		logger.info("****** parameters are -- " + strSubID
				+ " & " + strMsg + " & " + trxID);
		if (!bUpdateOperatorPrefixSite)
			return "INVALID PREFIX";

		strSubID = subID(strSubID);

		try {
			String strURL = null;
			String sitePrefix = getURL(strSubID);
			if (sitePrefix != null && sitePrefix.length() > 0) {
				if (trxID == null)
					strURL = Tools.findNReplaceAll(sitePrefix, "rbt_sms.jsp",
							"rbt_content_validator.jsp");
				else
					strURL = Tools.findNReplaceAll(sitePrefix, "rbt_sms.jsp",
							"rbt_song_set.jsp");

				strURL = sitePrefix + "SUB_ID="
						+ URLEncoder.encode(strSubID, "UTF-8") + "&SMS_TEXT="
						+ URLEncoder.encode(strMsg, "UTF-8") + "&TRANS_ID="
						+ URLEncoder.encode(trxID, "UTF-8");
			} else
				return getSMSTextForID(null, "TECHNICAL_FAILURE",
						m_technicalFailureDefault);

			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
			BufferedReader buffer = new BufferedReader(
					new InputStreamReader(is));
			String line = null;
			String response = "";
			while ((line = buffer.readLine()) != null)
				response += line;
			logger.info("RBT::invoked URL "
					+ strURL + " and got response " + response);
			if (response != null && !response.trim().equals("")) {
				return response;
			}
			return getSMSTextForID(null, "TECHNICAL_FAILURE",
					m_technicalFailureDefault);
		} catch (Exception e) {
			logger.error("", e);
			return getSMSTextForID(null, "TECHNICAL_FAILURE",
					m_technicalFailureDefault);
		}
	}

	// Hutch

	public String getSelectionChargeClass(int categoryID,
			String subscriberWavFile, String chargeClassType, HashMap z) {

		String classType = null;

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Categories categories = getCategory(categoryID,
				getCircleID(subscriberID));

		if (categories != null) {
			classType = categories.classType();
		}

		ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
		String clipClassType = null;
		if (clipMinimal == null
				|| !clipMinimal.getWavFile().equals(subscriberWavFile)) {
			ClipMinimal clips = getClipRBT(z, subscriberWavFile);
			if (clips != null)
				clipClassType = clips.getClassType();
		} else {
			clipClassType = clipMinimal.getClassType();
		}
		if (clipClassType != null) {
			if (clipClassType != null
					&& !clipClassType.equalsIgnoreCase("DEFAULT")
					&& classType != null
					&& !clipClassType.equalsIgnoreCase(classType)) {

				ChargeClass catCharge = (ChargeClass) m_chargemap
						.get(classType);
				ChargeClass clipCharge = (ChargeClass) m_chargemap
						.get(clipClassType);

				if (catCharge != null && clipCharge != null
						&& catCharge.getAmount() != null
						&& clipCharge.getAmount() != null) {
					try {
						int firstAmount = Integer.parseInt(catCharge.getAmount());
						int secondAmount = Integer
								.parseInt(clipCharge.getAmount());

						if (firstAmount < secondAmount)
							classType = clipClassType;
					} catch (Exception e) {
					}
				}
				classType = clipClassType;
			}
		}

		if (chargeClassType != null) {
			ChargeClass first = (ChargeClass) m_chargemap.get(classType);
			ChargeClass second = (ChargeClass) m_chargemap.get(chargeClassType);

			if (first != null && second != null && first.getAmount() != null
					&& second.getAmount() != null) {
				try {
					int firstAmount = Integer.parseInt(first.getAmount());
					int secondAmount = Integer.parseInt(second.getAmount());

					if (firstAmount < secondAmount || secondAmount == 0
							|| chargeClassType.equalsIgnoreCase("DEFAULT"))
						classType = chargeClassType;
				} catch (Exception e) {
					classType = chargeClassType;
				}
			} else {
				classType = chargeClassType;
			}
		}

		return (classType);
	}

	private void removeNavraatri(HashMap z) {
		// String _method = "removeNavraatri()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		String callerID = (String) getFromZTable(z, CALLER_ID);

		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (isSubAlreadyActiveOnStatus(subscriberID, callerID, 80, rbtType)) {
			deactivateSubscriberRecords(subscriberID, callerID, 80, rbtType);
			// //logger.info("RBT::deactivated
			// sms profile of the subscriber " +subscriberID);
			setReturnValues(z, "Your selection has been removed successfully",
					STATUS_SUCCESS);
		} else {
			if (isSubActive(subscriber, z)) {
				setReturnValues(z,
						"You do not have active navaraatri selection.",
						STATUS_NOT_AUTHORIZED);
				// //logger.info("RBT::no
				// active sms profiles for the subscriber " +subscriberID);
			} else {
				// //Tools.logDetail(_class, "removeTempOverride",
				// "RBT::subscriber " + subscriberID + " is deactivated");
				setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
						STATUS_NOT_AUTHORIZED);
			}
		}
		if (m_sendSMS)
			sendSMS(subscriberID, (String) z.get(RETURN_STRING));
	}

	public void processHELP(HashMap z, ArrayList smsList) {
		String helpOn = null;
		if (smsList == null || smsList.size() <= 0)
			helpOn = "HELP";
		else
			helpOn = ((String) smsList.get(0)).trim().toUpperCase();

		String smsParamName = "HELP_" + helpOn;
		String smsText = (String) m_smsTable.get(smsParamName);
		if (smsText != null && smsText.length() >= 0)
			setReturnValues(z, smsText, STATUS_SUCCESS);
		else
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);

	}

	public void processLISTEN(HashMap z, ArrayList smsList) {
		if (smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		String cCode = (String) smsList.get(0);
		if (cCode.startsWith("tp"))
			cCode = getPromoCodeForTopListCode(z, cCode.substring(2));

		if (cCode == null) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		/*
		 * String clipID = (String)m_clipIDPromoID.get(cCode); ClipMinimal clip =
		 * null; if(clipID != null) clip = (ClipMinimal)m_clips.get(clipID);
		 */
		ClipMinimal clip = m_rbtDBManager.getClipMinimalPromoID(cCode, true);
		if (clip != null)
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"LISTEN_SUCCESS", m_listenSuccessTextDefault), m_smsNo
					+ cCode, clip.getClipName() + " - " + cCode),
					STATUS_SUCCESS);
		else
			setReturnValues(z, getSMSTextForID(z, "LISTEN_FAILURE",
					m_listenFailureTextDefault), STATUS_SUCCESS);

	}

	private void processDOWNLOADSLIST(HashMap z) {
		// String _method = "processDOWNLOADSLIST()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "MANAGE_FAILURE",
					getSMSTextForID(z, "ERROR", m_errorDefault)),
					STATUS_SUCCESS);
			return;
		}
		
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		SubscriberDownloads[] sd = m_rbtDBManager.getActiveSubscriberDownloads(
				subscriberID);

		if (sd == null || sd.length <= 0) {
			setReturnValues(z, getSMSTextForID(z, "DOWNLOADS_NOT_PRESENT",
							m_downloadsNoSelDefault),
					STATUS_SUCCESS);
			return;
		}
		
		String sms = "";
		String endDate = null;
		for (int i = 0; i < sd.length; i++) {
			ClipMinimal clip = null;
			int songCount = 1;
			String clipName = null;

			if (sd[i].promoId() != null)
				clip = m_rbtDBManager.getClipRBT(sd[i].promoId().trim());
			if (clip != null) {
				clipName = clip.getClipName().trim();
				if (clip.getEndTime() != null)
					endDate = clip.getEndTime().toString();
			}
			if (clipName != null) {
					sms = sms
							+ ", "
							+ (songCount++ + ". " + clipName + "-" + (clip
									.getPromoID() == null ? "-" : clip
									.getPromoID()));

				}
			}
		if (sms.length() > 2)
			sms = sms.substring(2);

		String smsFinal = getSubstituedSMS(getSMSTextForID(z, "DOWNLOADS_LIST_SUCCESS",
				m_downloadsListSuccessDefault), sms, null);
		if (m_sendSMS)
			sendSMS(subscriberID, smsFinal);
		// //logger.info("RBT::removed the
		// selection for callerID-"+ callerID +" of the subscriber "
		// +subscriberID);
		setReturnValues(z, smsFinal, STATUS_SUCCESS);
	}


private void processMANAGE(HashMap z) {
		// String _method = "processMANAGE()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "MANAGE_FAILURE",
					getSMSTextForID(z, "ERROR", m_errorDefault)),
					STATUS_SUCCESS);
			return;
		}
		Date actDate = subscriber.activationDate();
		String actDateStr = "Yet to be activated";
		if (actDate != null)
			actDateStr = new SimpleDateFormat("dd/MM/yyyy").format(actDate);
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		SubscriberStatus[] ss = m_rbtDBManager.smSubscriberRecords(
				subscriberID, "0,80,90,99", true, rbtType);

		if (ss == null || ss.length <= 0) {
			setReturnValues(z,
					getSubstituedSMS(getSMSTextForID(z, "MANAGE_NO_SEL",
							m_manageNoSelDefault), "None", actDateStr),
					STATUS_SUCCESS);
			return;
		}
		ArrayList ssFinal = new ArrayList();
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].selStatus() != null && ss[i].selStatus().equals("B"))
				ssFinal.add(ss[i]);
		}
		if (ssFinal.size() <= 0) {
			setReturnValues(z,
					getSubstituedSMS(getSMSTextForID(z, "MANAGE_NO_SEL",
							m_manageNoSelDefault), "None", actDateStr),
					STATUS_SUCCESS);
			return;
		}
		String sms = "";
		String endDate = null;
		for (int i = 0; i < ssFinal.size(); i++) {
			ClipMinimal clip = null;
			int songCount = 1;
			String clipName = null;

			SubscriberStatus s = (SubscriberStatus) ssFinal.get(i);
			if (s.subscriberFile() != null)
				clip = m_rbtDBManager.getClipRBT(s.subscriberFile().trim());
			if (clip != null) {
				clipName = clip.getClipName().trim();
				if (clip.getEndTime() != null)
					endDate = clip.getEndTime().toString();
			}
			if (clipName != null) {
				if (!m_model.equalsIgnoreCase("EsiaSMSImpl")) {
					if (s.callerID() == null)
						sms = sms + ", " + m_smsTextForAll+"-" + clipName;
					else
						sms = sms + ", " + s.callerID() + "-" + clipName;
				} else {
					sms = sms
							+ ", "
							+ (songCount++ + ". " + clipName + "-" + (clip
									.getPromoID() == null ? "-" : clip
									.getPromoID()));

				}
			}
		}
		if (sms.length() > 2)
			sms = sms.substring(2);

		String smsFinal = getSubstituedSMS(getSMSTextForID(z, "MANAGE_SUCCESS",
				m_manageSuccessDefault), sms, actDateStr, endDate);
		if (m_sendSMS)
			sendSMS(subscriberID, smsFinal);
		// //logger.info("RBT::removed the
		// selection for callerID-"+ callerID +" of the subscriber "
		// +subscriberID);
		setReturnValues(z, smsFinal, STATUS_SUCCESS);
	}

	public void processSONG_CATCHER_ACCEPT(HashMap z, ArrayList smsList) {
		Subscriber subscriber = (Subscriber) z.get(SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z))
			addToHashMap(z, IS_ACTIVATION_REQUEST, new Boolean(true));

		addToHashMap(z, CLASS_TYPE, m_SongCatcherClassType);
		processDEFAULT_CASE(z, smsList);
	}

	public void processNewsletter(HashMap z,ArrayList smsList,String requirement){
		return;
	}

	public String getSubClassSMSText(String subClass, boolean isActivation) {
		String ret = null;
		String tmp = null;
		if (m_subClasses != null && m_subClasses.length > 0) {
			for (int i = 0; i < m_subClasses.length; i++) {
				if (m_subClasses[i].getSubscriptionClass().equalsIgnoreCase(
						subClass)) {
					if (isActivation) {
						tmp = m_subClasses[i].getSmsOnSubscription();
						if (tmp != null && tmp.length() > 10)
							ret = tmp;
						break;
					} else {
						tmp = m_subClasses[i].getSmsRenewalSuccess();
						if (tmp != null && tmp.length() > 10)
							ret = tmp;
						break;
					}
				}
			}
		}
		return ret;
	}

	public String isDctSMS(String sms) {
		StringTokenizer stk = new StringTokenizer(sms);
		while (stk.hasMoreTokens()) {
			String str = stk.nextToken();
			if (m_unsubMsg.contains(str)) {
				return (sms.replaceAll(str, ""));
			}
		}
		return null;
	}

	public String isActSMS(String sms) {
		StringTokenizer stk = new StringTokenizer(sms);
		while (stk.hasMoreTokens()) {
			String str = stk.nextToken();
			if (m_subMsg.contains(str)) {
				return (sms.replaceAll(str, ""));
			}
		}
		return null;
	}

	/*
	 * public Categories getFromUsedCategories(String catID) { Categories
	 * category = null; try { if(m_usedCategories.containsKey(catID)) category =
	 * (Categories) m_usedCategories.get(catID); else { category =
	 * getCategory(Integer.parseInt(catID)); m_usedCategories.put(catID,
	 * category); } } catch(Exception e) { category = null; }
	 * 
	 * return category; }
	 */

	private boolean isShufflePresentSelection(String subID, String callerID,
			int rbtType) {
		return m_rbtDBManager.isShufflePresentSelection(subID,
				callerID, rbtType);
	}

	private void processSET_SELECTION1(HashMap z, ArrayList smsList) {
		if (smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) z.get(SUBSCRIBER_OBJ);
		String callerID = (String) z.get(CALLER_ID);
		String cCode = (String) smsList.get(0);
		if (cCode.startsWith("tp"))
			cCode = getPromoCodeForTopListCode(z, cCode.substring(2));

		if (cCode == null) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		smsList.clear();
		smsList.add(cCode);
		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		if (!isSubActive(subscriber, z)
				|| isShufflePresentSelection(subscriberID, callerID, rbtType)) {
			removeViralSMSTable(subscriberID, "PRECOPY");
			removeViralSMSTable(subscriberID, "SELECTION");
			insertViralSMSTable(subscriberID, callerID, cCode, "SELECTION", 0);
			if (!isSubActive(subscriber, z))
				setReturnValues(z, getSMSTextForID(z, "SEL_KEYWORD1_SUCCESS",
						m_selKeyword1SuccessTextDefault), STATUS_SUCCESS);
			else
				setReturnValues(z, getSMSTextForID(z,
						"OVERRIDE_SHUFFLE_SELECTION",
						m_overrideShuffleSelTextDefault), STATUS_SUCCESS);
			return;
		}

		processDEFAULT_CASE(z, smsList);
	}

	private void processSET_SELECTION2(HashMap z, ArrayList smsList) {
		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) z.get(SUBSCRIBER_OBJ);

		ViralSMSTable context1 = getViralSMSTable(subscriberID, "SELECTION");
		ViralSMSTable[] context2List = getViralSMSesByType(subscriberID,
				"PRECOPY");
		ViralSMSTable context2 = null;
		if (context2List != null && context2List.length > 0)
			context2 = context2List[0];
		if ((context1 == null || context1.clipID() == null)
				&& (context2 == null || context2.clipID() == null)
				&& smsList.size() < 1) {
			setReturnValues(z, getSMSTextForID(z, "SEL_KEYWORD2_FAILURE",
					m_selKeyword2FailureTextDefault), STATUS_TECHNICAL_FAILURE);
			return;
		}
		if (context2 != null) {
			m_rbtDBManager.updateViralPromotion(context2.subID(), context2
					.callerID(), context2.sentTime(), "PRECOPY", "COPY",
					new Date(System.currentTimeMillis()), "SMS", null);
			String name = getClipNameFromViralContext(context2.clipID());
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"COPY_ACCEPT_SUCCESS", m_CopyAcceptTextDefault), context2
					.callerID(), name), STATUS_SUCCESS);
			return;
		}
		if (smsList.size() > 0) {
			addToHashMap(z, FEATURE, "SONG_CATCHER_ACCEPT");
			processSONG_CATCHER_ACCEPT(z, smsList);
			return;
		}
		removeViralSMSTable(subscriberID, "SELECTION");
		if (!isSubActive(subscriber, z))
			z.put(IS_ACTIVATION_REQUEST, new Boolean(true));
		addToHashMap(z, CALLER_ID, context1.callerID());
		smsList.add(context1.clipID());
		processDEFAULT_CASE(z, smsList);

	}

	private void processCategoryListing(HashMap z, ArrayList smsList) {
		String categoryAlias = ((String) smsList.get(0)).trim();
		if (m_topClipsListingKeywords.contains(categoryAlias)) {
			z.put("IS_TOP_LISTING", "TRUE");
			String lang = ((String) z.get(LANGUAGE)).trim();
			categoryAlias = ((String) m_topClipsListingKeywords.get(0)).trim()
					+ lang;
			smsList.clear();
			smsList.add(categoryAlias);
			processCATEGORY_SEARCH(z, smsList);
		} else if (m_topCategoriesListingKeywords != null
				&& m_topCategoriesListingKeywords.contains(categoryAlias)) {
			setReturnValues(z, getSMSTextForID(z,
					"TOP_CATEGORIES_LISTING_SUCCESS",
					m_topCategoriesListingSMSTextDefault), STATUS_SUCCESS);
			return;
		} else {
			Subscriber subscriber = (Subscriber) z.get(SUBSCRIBER_OBJ);
			boolean isPrepaid = ((Boolean) getFromZTable(z, IS_PREPAID))
					.booleanValue();
			String circleId = m_rbtDBManager.getCircleId(subscriber.subID());
			Categories cat = getCategoryAlias(categoryAlias, circleId,
					isPrepaid ? 'y' : 'n');
			Categories[] subCats = null;
			if (cat != null)
				subCats = m_rbtDBManager.getSubCategories(cat.id(), circleId,
						'b');
			if (subCats == null || subCats.length < 1) {
				processCATEGORY_SEARCH(z, smsList);
				return;
			}
			String sms = "";
			for (int i = 0; i < subCats.length; i++) {
				if (subCats[i].alias() != null
						&& subCats[i].alias().length() > 0)
					sms += "," + subCats[i].alias() + "-" + subCats[i].name()
							+ " ";
			}
			sms = sms.substring(1);
			setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
					"SUB_CATEGORIES_LISTING_SUCCESS",
					m_subCategoriesListingSMSTextDefault), sms, null),
					STATUS_SUCCESS);

		}

	}

	private String getPromoCodeForTopListCode(HashMap z, String tpCode) {
		try {
			String subscriberId = (String) z.get(SMS_SUBSCRIBER_ID);
			ViralSMSTable context = getViralSMSTable(subscriberId, "CATEGORY");
			if (context == null || context.clipID() == null
					|| !context.clipID().startsWith("TOP:"))
				return null;
			int tokenNumber = -1;
			tokenNumber = Integer.parseInt(tpCode) - 1;
			StringTokenizer stk = new StringTokenizer(context.clipID(), ",");
			for (int i = 0; i <= tokenNumber; i++)
				stk.nextToken();
			String clipId = stk.nextToken();
			// ClipMinimal clip = (ClipMinimal)m_clips.get(clipId);
			ClipMinimal clip = m_rbtDBManager.getClipMinimal(Integer
					.parseInt(clipId), true);
			return clip.getPromoID();
		} catch (Exception e) {
			return null;
		}
	}

	private String getClipNameFromViralContext(String clipID) {
		if (clipID == null)
			return null;
		String clipWavName = firstToken(clipID, ":");
		ClipMinimal clip = null;
		if (clipWavName != null)
			clip = m_rbtDBManager.getClipRBT(clipWavName);
		if (clip != null)
			return clip.getClipName();
		return null;
	}

	private String firstToken(String str, String delimiter) {
		if (str == null)
			return null;
		String delimiterUsed = ",";
		if (delimiter != null)
			delimiterUsed = delimiter;
		StringTokenizer tokenizeStk = new StringTokenizer(str, delimiterUsed);
		if (tokenizeStk.hasMoreTokens())
			return tokenizeStk.nextToken().trim();
		return null;
	}

	private SubscriberStatus[] getSubscriberCallerSelectionsInLoop(
			String subID, String callerID, int rbtType) {
		return m_rbtDBManager.getSubscriberCallerSelectionsInLoop(subID,
				callerID, rbtType);
	}

	public int getMaxCallerIdSelections() {
		int retVal = 0;
		Parameters cp = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON",
				"MAX_CALLERID_SEL_ALLOWED");
		if (cp != null && cp.getValue() != null) {
			try {
				retVal = Integer.parseInt(cp.getValue().trim());

			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return retVal;
	}

	public boolean copyDefaultAllowed() {
		boolean result = false;
		Parameters cp = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "COPY_DEFAULT");
		if (cp != null && cp.getValue() != null)
			result = cp.getValue().equalsIgnoreCase("TRUE");
		return result;
	}

	public String getDefaultClip() {
		String clipName = "Default Tune";
		int defaultClipId = -1;
		ClipMinimal clip = null;
		Parameters cp = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_CLIP");
		if (cp != null && cp.getValue() != null) {
			try {
				defaultClipId = Integer.parseInt(cp.getValue().trim());

			} catch (Exception e) {
				defaultClipId = -1;
				logger.error("", e);
			}
		}
		if (defaultClipId > -1)
			clip = getClip(defaultClipId);
		if (clip != null)
			clipName = clip.getClipName();
		return clipName;
	}

	public void getSongCatcherDetails() {
		int categoryID = 56;
		Parameters callBackBaseNoParameter = CacheManagerUtil.getParametersCacheManager().getParameter(
				COMMON, "CALLBACK_BASENUMBERS");
		
		
		if (callBackBaseNoParameter != null
				&& callBackBaseNoParameter.getValue() != null
				&& callBackBaseNoParameter.getValue().trim().length() > 0)
			m_songCatcherNumberList = callBackBaseNoParameter.getValue().trim()
					.split(",");
		if (m_songCatcherNumberList != null
				&& m_songCatcherNumberList.length > 0
				&& m_songCatcherNumberList[0] != null) {
			String firstSongCatcherNumber = m_songCatcherNumberList[0];
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,
					firstSongCatcherNumber + "_CALLBACK_CATEGORY");
			if (parameter != null && parameter.getValue() != null
					&& parameter.getValue().trim().length() > 0) {
				StringTokenizer stkCat = new StringTokenizer(parameter.getValue()
						.trim(), ",");
				if (stkCat != null && stkCat.hasMoreTokens()) {
					String scCat = stkCat.nextToken().trim();
					if (scCat != null && scCat.length() > 0) {
						try {
							categoryID = Integer.parseInt(scCat);
						} catch (Exception e) {
							categoryID = 56;
						}
					}
				}
			}
		}
		Category category = m_rbtDBManager.getCategory(categoryID);
		if (category != null)
			m_SongCatcherClassType = category.getClassType();
	}

	public SubscriberStatus[] getOtherCallerIDSelections(String subscriberID,
			String callerID, int rbtType) {
		return m_rbtDBManager.getPersonalCallerIDSelections(subscriberID,
				callerID, rbtType);
	}

	private void getConversionTableChargeClass() {
		ChargeClassMap[] cCM = m_rbtDBManager.getChargeClassMapsForModeType(
				"SMS", "CONVERT");
		if (cCM == null || cCM.length <= 0)
			return;
		for (int i = 0; i < cCM.length; i++) {
			if (cCM[i].mode() != null && cCM[i].mode().equalsIgnoreCase("ALL"))
				conversionTableChargeClass.put(cCM[i].classType(), cCM[i]
						.finalClassType());
		}
		for (int i = 0; i < cCM.length; i++) {
			if (cCM[i].mode() != null && cCM[i].mode().equalsIgnoreCase("SMS"))
				conversionTableChargeClass.put(cCM[i].classType(), cCM[i]
						.finalClassType());
		}
		logger.info("conversionTableChargeClass is " + conversionTableChargeClass);
	}

	private boolean convertWeeklySelectionsClassTypeToMonthly(
			String subscriberID, String initClass, String finalClass,
			int rbtType) {
		return m_rbtDBManager.convertWeeklySelectionsClassTypeToMonthly(
				subscriberID, initClass, finalClass, rbtType);
	}

	private void updateCircleID() {
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;
		HashMap temp = new HashMap();
		ArrayList numbers = null;
		for (int i = 0; i < prefixes.size(); i++) {
			numbers = Tools.tokenizeArrayList(prefixes.get(i).getSitePrefix(), null);
			/*
			 * if (prefixes[i].url() == null || prefixes[i].url().length() <= 0 ) {
			 * localSitePrefix = prefixes[i]; continue; } if( numbers == null ||
			 * numbers.size() <= 0) continue;
			 */
			for (int j = 0; j < numbers.size(); j++)
				temp.put((String) numbers.get(j), prefixes.get(i).getCircleID());
		}
		m_circle_id = temp;
		logger.info("Sree::m_circle_id is "
				+ m_circle_id);
	}

	public String getCircleID(String strSub) {
		// logger.info("RBT::entering
		// getCircleID ");
		/*
		 * if(strSub == null || strSub.length() <= 0) return null; for(int i =
		 * 1; i <=strSub.length(); i++) {
		 * if(m_circle_id.containsKey(strSub.substring(0,i))) return
		 * (String)m_circle_id.get(strSub.substring(0,i)); } return null;
		 */
		// logger.info("RBT::exiting
		// getCircleID with value == "+temp);
		return m_rbtDBManager.getCircleId(strSub);
	}

	private void initTrialWithActivations() {
		String trialStr = null;
		Parameters p = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "TRIAL_WITH_ACT");
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

	public boolean isNavCat(int catID, String subID) {
		Categories cat = getCategory(catID, getCircleID(subID));
		if (cat != null && cat.type() == 10)
			return true;
		return false;
	}

	public Date getCatStartTime(int catID, String subscriberID) {
		Categories cat = null;
		if (subscriberID != null)
			cat = getCategory(catID, getCircleID(subscriberID));
		if (cat != null)
			return cat.startTime();
		return null;
	}

	private void processDisableIntro(HashMap z) {
		// String _method = "processMANAGE()";
		// //logger.info("****** parameters are -- "+z );

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		m_rbtDBManager.disablePressStarIntro(subscriber);
		setReturnValues(z, getSMSTextForID(z, "INTRO_PROMPT_DISABLE_SUCCESS",
				m_disableIntroSuccessDefault), STATUS_SUCCESS);
	}

	public Date getCatEndTime(int catID, String subscriberID) {
		Categories cat = getCategory(catID, getCircleID(subscriberID));
		if (cat != null)
			return cat.endTime();
		return null;
	}

	public String getCatName(int catID, String subscriberID) {
		Categories cat = getCategory(catID, getCircleID(subscriberID));
		if (cat != null)
			return cat.name();
		return null;
	}

	public boolean isvalidProfileUser(String strSubID) {
		String _method = "isvalidProfileUser";
		// logger.info("inside isvalidProfileUser");
		boolean returnFlag = false;
		Subscriber sub = m_rbtDBManager.getSubscriber(strSubID);

		if (sub != null && m_rbtDBManager.isSubActive(sub)) {
			// logger.info("subscriber is not null");

			String subClass = sub.subscriptionClass();
			// logger.info("subscription
			// class=="+subClass);
			if (m_profileSubClass != null && subClass != null
					&& subClass.equalsIgnoreCase(m_profileSubClass)) {
				returnFlag = true;
				// logger.info("strSubID is a valid profile
				// user");
			}
		}
		logger.info("exiting isvalidProfileUser with value==" + returnFlag);
		return returnFlag;
	}

	private boolean isValidProfileRequest(HashMap z) {
		// String _method="isValidProfileRequest";
		// logger.info("inside isValidProfileRequest");
		boolean returnFlag = false;
		if (((String) z.get(FEATURE)).equals("REMOVE_PROFILE")
				|| ((String) z.get(FEATURE)).equals("SET_PROFILE")
				|| ((String) z.get(FEATURE)).equals("LISTPROFILE")) {
			returnFlag = true;
		}
		// logger.info("exiting isValidProfileRequest with
		// value=="+returnFlag);
		return returnFlag;
	}

	private void processDisableOverlay(HashMap z) {
		String _method = "processEnableOverlay";
		logger.info("");

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		m_rbtDBManager.disableOverlay(subscriber);
		setReturnValues(z, getSMSTextForID(z, "INTRO_PROMPT_DISABLE_SUCCESS",
				m_disableIntroSuccessDefault), STATUS_SUCCESS);
	}

	private void processEnableOverlay(HashMap z) {
		String _method = "processEnableOverlay";
		logger.info("");

		String subscriberID = (String) z.get(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);
		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "ERROR", m_errorDefault),
					STATUS_SUCCESS);
			return;
		}
		m_rbtDBManager.enableOverlay(subscriber);
		setReturnValues(z, getSMSTextForID(z, "INTRO_PROMPT_ENABLE_SUCCESS",
				m_enableIntroSuccessDefault), STATUS_SUCCESS); 
	}

	
	private void processSongOfMonth(HashMap z) {
		String _method = "processSongOfMonth";
		logger.info("z is" + z);

		setReturnValues(z, getSMSTextForID(z, "SONG_OF_MONTH_FAILURE",
				m_songOfMonthFailureDefault), STATUS_SUCCESS); 
	}

	public boolean writeTrans(String params, String resp, String diff, String ip)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("REQUEST PARAMS", params);
		h.put("RESPONSE", resp);
		h.put("TIME DELAY", diff);
		h.put("REQ IP", ip);

		if(m_smsTrans != null)
		{
			m_smsTrans.writeTrans(h);
			return true;
		}

		return false;
	}

}