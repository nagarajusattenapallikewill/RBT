package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ChargeClassMap;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.RBTLogin;
import com.onmobile.apps.ringbacktones.content.RbtBulkSelectionTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.UserRights;
import com.onmobile.apps.ringbacktones.content.ViralBlackListTable;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.lucene.AbstractLuceneIndexer;
import com.onmobile.apps.ringbacktones.lucene.LuceneIndexerFactory;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.tangentum.phonetix.DoubleMetaphone;
public class RBTSubUnsub implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(RBTSubUnsub.class);
	
    ResourceBundle m_bundle = null;
    private static String _class = "RBTSubUnsub";
    String[] m_validIP = null;
    //String [] m_validPrefix = null;
    String m_daysDefault = "45";
    String m_days = m_daysDefault;

    int m_activationPeriodDefault = 0;
    int m_activationPeriod = m_activationPeriodDefault;
    int m_smsPromotionCategoryIDDefault = 2;
    int m_smsPromotionCategoryID = m_smsPromotionCategoryIDDefault;

    public boolean m_allowDayOfWeekFutureDate = false;
    public boolean m_allowGroupSelection = false;
    public boolean m_allowAddUserGroup = false;
    public boolean m_showClipExpiryDateInListClips = false;
    public boolean m_showClipExpiryDateInViewSubscrberDetails = false;
	public boolean m_showSubCategoriesOutSide = true;
    boolean m_allowReactivationDefault = false;
    boolean m_allowReactivation = m_allowReactivationDefault;
    boolean m_delSelectionsDefault = true;
    boolean m_delSelections = m_delSelectionsDefault;
    boolean m_isPrepaidDefault = false;
    boolean m_isPrepaid = m_isPrepaidDefault;
    boolean m_showchargeduration;
    boolean m_processBlackListTypes = false;
    public boolean m_showDownloadsOnCCGui=false;
    public boolean m_showGiftOptionInAddSelections=false;
    public String subMgrUrlForDeactivationDaysLeft=null;
    public boolean m_useProxyADRBT = false;
    public String m_proxyHostADRBT = null;
    public int m_proxyPortADRBT = 80;
    public String m_ADRBTActUrl = null;
    public String m_ADRBTDeactUrl = null;
    public boolean m_ADRBTServerUrlHit = false;
    public int m_ADRBTConnectionTimeOut = 5000;
    
    boolean m_corpBlockSongChangeSomeSongs = false; 
    ArrayList m_corpBlockSongs = null; 

    ArrayList m_subActiveDeactivatedBy = null;

    String m_messagePathDefault = null;
    String m_messagePath = m_messagePathDefault;
    String m_filePathDefault = null;
    String m_filePath = m_filePathDefault;
    String m_invalidPrefixDefault = "You are not authorized to use this service. We apologize the inconvenience";
    String m_invalidPrefix = m_invalidPrefixDefault;
    String m_countryPrefix = "91";
    ArrayList m_countryPrefixList = new ArrayList();

    int m_deactivationPeriodDefault = 30;
    int m_deactivationPeriod = m_deactivationPeriodDefault;

  //  Hashtable m_clipTable = new Hashtable();
    Hashtable m_userPwd = new Hashtable(); 
    Hashtable m_userRights = new Hashtable(); 

    int m_bulkSelectionSuccessCount = 0;
    int m_bulkSelectionFailureCount = 0;
	private Date m_endDate = null;

    String FAILURE = "FAILURE";
    String SUCCESS = "SUCCESS";
    String CC = "Customer Care";
    String SMS = "SMS Request";
    String VP = "Voice Portal";
    String AU = "Auto";
    String OP = "Operator";
    String NA = "Not Activated";
    String NEF = "Not Enough Fund";
    String FRE = "Free";
    String VPO = "Auto Dialer";

    String m_profileCorporateCategories = "99";

    boolean m_useSubscriptionManager = false;
    boolean m_corpChangeSelectionBlock = false;

    static int STATUS_SUCCESS = 1;
    static int STATUS_ALREADY_ACTIVE = 2;
    static int STATUS_ALREADY_CANCELLED = 3;
    static int STATUS_NOT_AUTHORIZED = 4;
    static int STATUS_TECHNICAL_FAILURE = 5;
	HashMap m_phonemes = null;
    ArrayList m_Monthly = null;
    ArrayList m_Weekly = null;

    Hashtable chargePeriodMonthMap = new Hashtable();

    private static RBTSubUnsub rbtSubUnsub = null;
    private Hashtable m_subUnsubsmsTable;
    //ArrayList m_NAVCategoryIDs = new ArrayList();
    ArrayList m_nonOnmobilePrefixList = null;

    public static final String URL_SEND_ACT = "URL_SEND_ACTIVATION";
    public static final String URL_SEND_ACT_SUCCESS = "URL_SEND_ACTIVATION_SUCCESS";
    public static final String URL_SEND_ACT_FAILURE = "URL_SEND_ACTIVATION_FAILURE";
    public static final String URL_SEND_ACT_ALREADY_ACT = "URL_SEND_ACTIVATION_ALREADY_ACTIVE";
    public static final String URL_SEND_ACT_MISSING_PARAMETER = "URL_SEND_ACTIVATION_MISSING_PARAMETER";
    public static final String URL_SEND_SEL = "URL_SEND_SELECTION";
    public static final String URL_SEND_SEL_INACTIVE = "URL_SEND_SELECTION_INACTIVE";
    public static final String URL_SEND_SEL_FAILURE = "URL_SEND_SELECTION_FAILURE";
    public static final String URL_SEND_SEL_SUCCESS = "URL_SEND_SELECTION_SUCCESS";
    public static final String URL_SEND_SEL_INVALID_TONEID = "URL_SEND_SELECTION_INVALID_TONEID";
    public static final String URL_SEND_SEL_MISSING_PARAMETER = "URL_SEND_SELECTION_MISSING_PARAMETER";
    public static final String URL_SEND_DEACT = "URL_SEND_DEACTIVATION";
    public static final String URL_SEND_DEACT_ALREADY_INACT = "URL_SEND_DEACTIVATION_ALREADY_INACTIVE";
    public static final String URL_SEND_DEACT_SUCCESS = "URL_SEND_DEACTIVATION_SUCCESS";
    public static final String URL_SEND_DEACT_FAILURE = "URL_SEND_DEACTIVATION_FAILURE";
    public static final String URL_SEND_DEACT_MISSING_PARAMETER = "URL_SEND_DEACTIVATION_MISSING_PARAMETER";
    
    //Added by Sreekar for Airtel USSD
    private int m_freeCategoryID = 52;
    private int m_chargedCategoryID = 53;
    private List m_top10Categories = null;
    private List m_freeZoneCategories = null;
    private HashMap m_advanceRentalMap = null;
    private String m_advancePacksList = null;
    private HashMap m_ussdResposeMap = new HashMap();
    private String m_ussdUrlReplacePackage = "/sms";
    //Added by Sreekar for Airtel auto-dialer
    private int m_autoCategoryLower = 54;
    private int m_autoCategoryUpper = 53;
    private String m_autoSubClass = "AUTODIALER";
    private int m_autoSubCategory = 53;
    private HashMap m_autoActivatedByMap = new HashMap();
    private HashMap m_autoFlagSubClassMap = new HashMap();
    private String m_autodialUrlReplacePackage = "/autodial";
    private String m_freeAdvRntlSubClass = "";
//    private Clips m_defaultClip = null;
    //Added by Sreekar for Airtel EnvIO
    private int m_envIOCatgory = 53;
    private String m_envIOSubClass = "ENVIO";
    private String m_envIOSubClass_10DayPack = "ENVIO_10";
    private String m_envIOSubClass_20DayPack = "ENVIO_20";
    private HashMap m_envIOResposeMap = new HashMap();
    private String m_envIOUrlReplacePackage = "/envio";
    //Added by Sreekar for Airtel EC
    private String m_ecUrlReplacePackage = "/easycharge";
//    private HashMap m_ecTariffSubClassMap = new HashMap();
    private HashMap m_ecFlagSubClassMap = new HashMap();
    private int m_ecCategory = 53;
    private HashMap m_ecResponseMap = new HashMap();
    
//    private int m_maxSplSettings = -1;
	/**added by sandeep**/
	private HashMap m_categoryIdMap = null;
	private HashMap m_parentCategories = null;
	private HashMap m_categoryClipsMap =null;
	public boolean m_allowLooping  = false; 
    public boolean m_isDefaultLoopOn = false;
    public boolean m_showUpgrade = false;

	/***/
    
    /**
     * @author Sreekar
     * responses for USSD request
     */
	public static final int USSD_SUCCESS = 200;
	public static final int USSD_UNKNOWN_ERROR = 400;
	public static final int USSD_SUBSCRIBER_INVALID = 410;
	public static final int USSD_COMMAND_INVALID = 420;
	public static final int USSD_VCODE_INVALID = 430;
	public static final int USSD_VCODE_NOT_FOUND = 431;
	public static final int USSD_VCODE_EXPIRED = 432;
	public static final int USSD_CALLER_INVALID = 440;
	public static final int USSD_DST_CALLER_INVALID = 441;
	public static final int USSD_INTERNAL_SERVER_ERROR = 500;
	public static final int USSD_REQUEST_TIME_OUT = 510;
	public static final int USSD_SUBSCRIBER_AUTORBT_SUB = 450;
	public static final int USSD_DST_CALLER_AUTORBT_SUB = 451;
	public static final int USSD_COPY_ALBUM_BLOCKED = 442;
	/**
	 * @author Sreekar
	 * responses for auto-dialer request
	 */
	public static final int AUTO_SUCCESS = 0;
	public static final int AUTO_SUBSCRIBER_EXISTS = 1;
	public static final int AUTO_SUBSCRIBER_NOT_EXISTS = 2;
	public static final int AUTO_ACT_DEACT_PENDING = 3;
	public static final int AUTO_SUBSCRIPTION_FAILED = 4;
	public static final int AUTO_SELECTION_FAILED = 5;
	public static final int AUTO_SUBSCRIBER_INVALID = 6;
	public static final int AUTO_VCODE_INVALID = 7;
	public static final int AUTO_ERROR = -1;
	//added in the 2nd phase
	public static final int AUTO_CALLER_SETTING_FULL = 8;
	public static final int AUTO_STYPE_INVALID = 9;
	public static final int AUTO_UCODE_INVALID = 10;
	public static final int AUTO_CALLER_INVALID = 11;
	public static final int AUTO_FLAG_INVALID = 12;
	public static final int AUTO_SUBSCRIBER_SUSPENDED = 13;
	//added by Sreekar
	public static final int AUTO_ALBUM_CODE_INVALID = 14;

	/**
	 * @author Sreekar
	 * responses for EnvIO request
	 */
	public static final int ENVIO_COPY_ALL_SUCCESS = 0;
	public static final int ENVIO_COPY_CALLER_SUCCESS = 1;
	public static final int ENVIO_COPY_ALL_SUCCESS_NEW_SUB = 2;
	public static final int ENVIO_COPY_CALLER_SUCCESS_NEW_SUB = 3;
	public static final int ENVIO_CALLER_SETTING_FULL = 4;
	public static final int ENVIO_SUBSCRIBER_INVALID = 5;
	public static final int ENVIO_DST_CALLER_INVALID = 6;
	public static final int ENVIO_VCODE_INVALID = 7;
	public static final int ENVIO_SEL_SUCCESS = 8;
	public static final int ENVIO_SUB_SEL_SUCCESS = 9;
	public static final int ENVIO_ERROR = 10;
	public static final int ENVIO_SUBSCRIBER_EXISTS = 11;
	public static final int ENVIO_SUBSCRIBER_NOT_EXISTS = 12;
	public static final int ENVIO_COPY_ALL_FAILURE = 13;
	public static final int ENVIO_COPY_CALLER_FAILURE = 14;
	public static final int ENVIO_COPY_ALL_FAILURE_NEW_SUB = 15;
	public static final int ENVIO_COPY_CALLER_FAILURE_NEW_SUB = 16;
	public static final int ENVIO_SEL_FAILURE = 17;
	public static final int ENVIO_SUB_SEL_FAILURE = 18;
	public static final int ENVIO_FLAG_INVALID = 19;
	public static final int ENVIO_SUB_UNSUB_PENDING = 20;
	public static final int ENVIO_DST_CALLER_NOT_EXITS = 21;
	public static final int ENVIO_CALLER_INVALID = 22;
	//	added in the 2nd phase
	public static final int ENVIO_SUB_WAITING_OR_HLR_REMOVE = 23;
	public static final int ENVIO_SEL_DELETION_SUCCESSFUL = 24;
	public static final int ENVIO_SEL_DELETION_FAILED = 25;
	public static final int ENVIO_SEL_DELETION_INVALID = 26;
	public static final int ENVIO_GIFT_SUCCESSFUL = 27;
	public static final int ENVIO_GIFT_FAILED = 28;
	public static final int ENVIO_GIFTEE_INBOX_FULL = 29;
	public static final int ENVIO_ALBUM_SEL_SUCCESS = 30;
	public static final int ENVIO_SUB_ADVANCE_RENTAL_USER = 31;
	public static final int ENVIO_ALBUM_SEL_FAILED = 32;
	public static final int ENVIO_DSTMSISDN_WAITING = 33;
	public static final int ENVIO_HLR_REMOVE_OR_SUSPENDED_USER = 34;
	public static final int ENVIO_VCODE_EXPIRED = 35;
	public static final int ENVIO_VCODE_INVALID_FOR_COPY = 36;
	public static final int ENVIO_INDEX_INVALID = 37;
	public static final int ENVIO_SUB10_SUCCESS = 42;
	public static final int ENVIO_SUB20_SUCCESS = 43;
	public static final int ENVIO_SUB10_FAILURE = 44;
	public static final int ENVIO_SUB20_FAILURE = 45;
	
	/**
	 * @author Sreekar
	 * responses for Easy_Charge requests (EC)
	 */
	public static int EC_0_SUCCESS = 0;
	public static int EC_0_SUBSCRIBER_EXISTS = 1;
	public static int EC_0_SEL_FAILURE = 2;
	public static int EC_0_SUB_FAILURE = 4;
	public static int EC_0_SUBSCRIBER_EXISTS_PENDING = 5;
	public static int EC_0_SUBSCRIBER_INVALID = 6;
	public static int EC_0_VCODE_INVALID = 7;
	public static int EC_0_ERROR = -1;
	public static int EC_0_SUBSCRIBER_SUSPENDED = 9;
	
	public static int EC_1_SUCCSS = 0;
	public static int EC_1_SEL_FAILURE = 3;
	public static int EC_1_SUBSCRIBER_NOT_EXISTS = 2;
	public static int EC_1_SUCCESS_LIGHT = 4;
	public static int EC_1_SUBSCRIBER_EXISTS_PENDING = 5;
	public static int EC_1_SUBSCRIBER_INVALID = 6;
	public static int EC_1_VCODE_INVALID = 7;
	public static int EC_1_ERROR = -1;
	public static int EC_1_SUBSCRIBER_SUSPENDED = 9;
	public static int EC_1_TONE_ALREADY_EXISTS = 10;

	public static int EC_2_SUCCESS = 0;
	public static int EC_2_SUBSCRIBER_EXISTS = 1;
	public static int EC_2_SEL_FAILURE = 2;
	public static int EC_2_SUB_FAILURE = 4;
	public static int EC_2_SUBSCRIBER_EXISTS_PENDING = 5;
	public static int EC_2_SUBSCRIBER_INVALID = 6;
	public static int EC_2_VCODE_INVALID = 7;
	public static int EC_2_TARIFF_CODE_INVALID = 8;
	public static int EC_2_ERROR = -1;
	public static int EC_2_SUBSCRIBER_SUSPENDED = 9;
	
	/**
	 * @author Sandipt
	 * responses for MOD request
	 */
	public static final int MOD_SEL_SUCCESS = 0;
	public static final int MOD_SUB_SEL_SUCCESS = 1;
	public static final int MOD_SELECTION_FAILED = 2;
	public static final int MOD_SUBSCRIPTION_FAILED = 3;
	public static final int MOD_WAITING_USER = 4;
	public static final int MOD_SUBSCRIBER_INVALID = 5;
	public static final int MOD_VCODE_INVALID = 6;
	public static final int MOD_SUSPENDED_USER = 7;
	public static final int MOD_ERROR = -1;
	private String m_modUrlReplacePackage = "/mod";
	private String m_modSubClass = "MOD";
	private int m_modCategory = 61;
	
    /**
     * @added Sreekar for SMS redirect
     */
    //stores the URL key and URL map
    private HashMap<String, String> _smsRedirectURLMap = new HashMap<String, String>();
    //stores the keyword URL key map
    private HashMap<String, String> _smsRedirectKeywordMap = new HashMap<String, String>();
    //stores URL Key and sender No. map
    private HashMap<String, String> _smsRedirectSenderMap = new HashMap<String, String>();
    //PromoTool URL
    private String _smsRedirectPromotoolURL = null;
    //SDR path
    private String _smsRedirectSDRPath = null;
    //SDR rotation size
    private int _smsRedirectRotationSize = 8000;
    //Date format to write SDR
    private SimpleDateFormat _formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    //country prefix
    private String _countryPrefix = "91";

	Object m_lock = new Object();
    static Object m_initLock = new Object();
//    private Thread m_thread;
    
    private static Hashtable m_subClasses = new Hashtable();
    
    //Added by Sreekar
    private String _currencyStr = "Rs.";
    private String m_defaultCricketPass = "DP";
    private int m_cricketInterval = 0;
    public static AbstractLuceneIndexer luceneIndexer = null;
    public static int _ccSearchCount = 15;
	
    public static RBTSubUnsub init()
    {
        if (rbtSubUnsub != null)
            return rbtSubUnsub;
        synchronized (m_initLock)
        {
            if (rbtSubUnsub != null)
                return rbtSubUnsub;
            try
            {
                rbtSubUnsub = new RBTSubUnsub();
            }
            catch (Exception e)
            {
            	logger.error("", e);
                rbtSubUnsub = null;
            }
        }
        return rbtSubUnsub;

    }

    private RBTSubUnsub() throws Exception
    {
    	Tools.init("RBT_WAR", false);
        String validIP = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_IP", "");

        StringTokenizer tokens = new StringTokenizer(validIP, ",");
        List ipList = new ArrayList();
        while (tokens.hasMoreTokens())
        {
        	ipList.add(tokens.nextToken());
        }
        if (ipList.size() > 0)
        {
        	m_validIP = (String[]) ipList.toArray(new String[0]);
        }
        /*
         * String validPrefix = rbtCommonConfig.validPrefix();
         * if(validPrefix == null) throw new Exception();
         * 
         * StringTokenizer token = new StringTokenizer(validPrefix, ",");
         * List prefixList = new ArrayList(); while(token.hasMoreTokens()) {
         * prefixList.add(token.nextToken()); } if(prefixList.size() > 0) {
         * m_validPrefix = (String[])prefixList.toArray(new String[0]); }
         */
        m_subActiveDeactivatedBy = new ArrayList();

        String deactBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SUB_ACTIVE_DEACTIVATED_BY", null);
        if (deactBy == null)
        {
        	m_subActiveDeactivatedBy.add("AUX");
        	m_subActiveDeactivatedBy.add("NEFX");
        }
        else
        {
        	StringTokenizer stk = new StringTokenizer(deactBy, ",");
        	while (stk.hasMoreTokens())
        	{
        		m_subActiveDeactivatedBy.add(stk.nextToken());
        	}
        }

        m_days = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "AUTO_DEACTIVATION_PERIOD", null);
        if (m_days == null)
        	m_days = m_daysDefault;

        m_activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);

        m_messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);

        m_smsPromotionCategoryID = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "SMS_PROMOTION_CATEGORY_ID", 2);

        m_delSelections = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DEL_SELECTIONS", "TRUE");

        m_useSubscriptionManager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");
        if (m_useSubscriptionManager)
        	m_allowReactivation = false;

        if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("pre"))
        	m_isPrepaid = true;

        m_filePath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);

        m_profileCorporateCategories = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROFILE_CORPORATE_CATEGORIES", "99");

        m_invalidPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "INVALID_PREFIX_TEXT", null);
        if (m_invalidPrefix == null)
        	m_invalidPrefix = m_invalidPrefixDefault;

        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
        StringTokenizer strTokenizer = new StringTokenizer(m_countryPrefix, ",");
        while(strTokenizer.hasMoreTokens())
        	m_countryPrefixList.add(strTokenizer.nextToken());

        m_corpChangeSelectionBlock = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE");

        m_deactivationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "DEACTIVATION_PERIOD", 30);

        m_processBlackListTypes = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_BLACKLIST_TYPE", "FALSE");

        fillChargePeriodMonthMap();
        //     m_thread = new Thread(this);
        //   m_thread.start();
        m_showchargeduration = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGE_DURATION_IN_GUI", "FALSE");
        if (m_showchargeduration)
        {
        	m_Monthly = new ArrayList();
        	String type = "MONTHLY";
        	ChargeClassMap[] chargeclassmapmonthly = RBTDBManager.getInstance()
        			.getChargeClassMapsForType(type, "SEL", null);
        	if (chargeclassmapmonthly != null)
        	{
        		for (int i = 0; i < chargeclassmapmonthly.length; i++)
        		{
        			m_Monthly
        			.add(chargeclassmapmonthly[i].finalClassType());
        		}
        	}
        	m_Weekly = new ArrayList();
        	type = "WEEKLY";
        	ChargeClassMap[] chargeclassmapweekly = RBTDBManager.getInstance()
        			.getChargeClassMapsForType(type, "SEL", null);
        	if (chargeclassmapweekly != null)
        	{
        		for (int i = 0; i < chargeclassmapweekly.length; i++)
        		{
        			m_Weekly.add(chargeclassmapweekly[i].finalClassType());
        		}
        	}
        }

        UserRights[] rbtRights = RBTDBManager.getInstance().getUserRights(); 
        if(rbtRights != null && rbtRights.length > 0) 
        { 
        	for (int i = 0; i < rbtRights.length; i++) 
        	{ 
        		m_userRights.put(rbtRights[i].type(), rbtRights[i].rights()); 
        	} 
        } 

        RBTLogin[] rbtLogin = RBTDBManager.getInstance().getLogins(); 
        if(rbtLogin != null && rbtLogin.length > 0) 
        { 
        	for (int i = 0; i < rbtLogin.length; i++) 
        	{ 
        		m_userPwd.put(rbtLogin[i].user().toLowerCase().trim()+"_"+rbtLogin[i].pwd().trim(), rbtLogin[i].userType()); 
        	} 
        } 

        m_corpBlockSongChangeSomeSongs = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_BLOCK_CHANGE_FOR_SOME_SONGS", "FALSE"); 
        m_corpBlockSongs = new ArrayList(); 

        String str = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "CORP_BLOCK_SONGS", null); 
        if(str != null) 
        { 
        	StringTokenizer stk = new StringTokenizer(str, ","); 
        	while(stk.hasMoreTokens()) 
        		m_corpBlockSongs.add(stk.nextToken().trim()); 
        } 

        m_subUnsubsmsTable = new Hashtable();
        loadSubUnsubSMSTable(RBTSubUnsub.URL_SEND_ACT);
        loadSubUnsubSMSTable(RBTSubUnsub.URL_SEND_SEL);
        loadSubUnsubSMSTable(RBTSubUnsub.URL_SEND_DEACT);

        List<SubscriptionClass> sc = CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses(); 
        if(sc != null && sc.size() > 0) 
        	for(int i = 0; i < sc.size(); i++) 
        		m_subClasses.put(sc.get(i).getSubscriptionClass(),sc.get(i));
        Calendar endCal = Calendar.getInstance(); 
        endCal.set(2037, 0, 1); 
        m_endDate = endCal.getTime(); 

        //Added by Sreekar for Airtel
        initWarParams();
        RBTDBManager dbManager = RBTDBManager.getInstance();
        Parameters tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ADVANCE_PACKS");
        if(tempParam != null)
        	m_advancePacksList = tempParam.getValue();

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ALLOW_DAY_OF_WEEK_FUTURE_DATE");
        if(tempParam != null && tempParam.getValue() != null){
        	m_allowDayOfWeekFutureDate = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ALLOW_GROUP_SELECTION");
        if(tempParam != null && tempParam.getValue() != null){
        	m_allowGroupSelection = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ALLOW_ADD_USER_GROUP");
        if(tempParam != null && tempParam.getValue() != null){
        	m_allowAddUserGroup = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "LIST_CAT_SHOW_SUB_CAT_OUTSIDE");
        if(tempParam != null && tempParam.getValue() != null){
        	m_showSubCategoriesOutSide = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_CLIP_EXPIRY_DATE_IN_LIST_CLIPS");
        if(tempParam != null && tempParam.getValue() != null){
        	m_showClipExpiryDateInListClips = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_CLIP_EXPIRY_DATE_IN_SUBSCRIBER_DELTAILS");
        if(tempParam != null && tempParam.getValue() != null){
        	m_showClipExpiryDateInViewSubscrberDetails = tempParam.getValue().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "EC_CATEGORY");
        if(tempParam != null)
        	m_ecCategory = Integer.parseInt(tempParam.getValue());

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(GATHERER, "NON_ONMOBILE_PREFIX");
        if(tempParam != null && tempParam.getValue() != null && tempParam.getValue().length() > 0)
        	m_nonOnmobilePrefixList = Tools.tokenizeArrayList(tempParam.getValue().trim(), null);
        m_allowLooping = dbManager.allowLooping(); 
        m_isDefaultLoopOn = dbManager.isDefaultLoopOn();
        m_showUpgrade = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_UPGRADE", "FALSE");
        /*tempParam = dbManager.getParameter(COMMON, "DEFAULT_CLIP");
            if(tempParam != null) {
            	m_defaultClip = getClip(Integer.parseInt(tempParam.value()));
            }*/

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_SUBSCRIBER_DOWNLOADS_ON_CC_GUI");
        if(tempParam != null && tempParam.getValue() != null){
        	m_showDownloadsOnCCGui=CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_SUBSCRIBER_DOWNLOADS_ON_CC_GUI").getValue().equalsIgnoreCase("TRUE");

        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_GIFT_OPTION_IN_ADD_SELECTIONS");
        if(tempParam != null && tempParam.getValue() != null){
        	m_showGiftOptionInAddSelections=CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SHOW_GIFT_OPTION_IN_ADD_SELECTIONS").getValue().equalsIgnoreCase("true");

        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SUBMGR_URL_FOR_DEACT_DAYS_LEFT");
        if(tempParam != null && tempParam.getValue() != null){
        	subMgrUrlForDeactivationDaysLeft=CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "SUBMGR_URL_FOR_DEACT_DAYS_LEFT").getValue();
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "USE_PROXY_ADRBT");
        if(tempParam != null && tempParam.getValue() != null){
        	m_useProxyADRBT= tempParam.getValue().trim().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "PROXY_HOST_ADRBT");
        if(tempParam != null && tempParam.getValue() != null){
        	m_proxyHostADRBT= tempParam.getValue().trim();
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "PROXY_PORT_ADRBT");
        if(tempParam != null && tempParam.getValue() != null){
        	try
        	{
        		m_proxyPortADRBT = Integer.parseInt(tempParam.getValue());
        	}
        	catch(Exception e)
        	{
        		m_proxyPortADRBT = 80;
        	}
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "ADRBT_CONNECTION_TIME_OUT");
        if(tempParam != null && tempParam.getValue() != null){
        	try
        	{
        		m_ADRBTConnectionTimeOut = Integer.parseInt(tempParam.getValue());
        	}
        	catch(Exception e)
        	{
        		m_ADRBTConnectionTimeOut = 5000;
        	}
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "ADRBT_ACT_URL");
        if(tempParam != null && tempParam.getValue() != null){
        	m_ADRBTActUrl= tempParam.getValue().trim();
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "ADRBT_DEACT_URL");
        if(tempParam != null && tempParam.getValue() != null){
        	m_ADRBTDeactUrl= tempParam.getValue().trim();
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON, "ADRBT_SERVER_URL_HIT");
        if(tempParam != null && tempParam.getValue() != null){
        	m_ADRBTServerUrlHit= tempParam.getValue().trim().equalsIgnoreCase("TRUE");
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "URL_MAP");

        if(tempParam != null) {
        	String value = tempParam.getValue();
        	StringTokenizer stk = new StringTokenizer(value, ",");
        	while(stk.hasMoreTokens()) {
        		String token = stk.nextToken();
        		int index = token.indexOf("=");
        		_smsRedirectURLMap.put(token.substring(0, index), token.substring(index+1));
        	}
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "KEYWORD_MAP");
        if(tempParam != null) {
        	String value = tempParam.getValue();
        	StringTokenizer stk = new StringTokenizer(value, ",");
        	while(stk.hasMoreTokens()) {
        		String token = stk.nextToken();
        		int index = token.indexOf("=");
        		_smsRedirectKeywordMap.put(token.substring(0, index).toLowerCase(), token
        				.substring(index + 1));
        	}
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "SENDER_NO_MAP");
        if(tempParam != null) {
        	String value = tempParam.getValue();
        	StringTokenizer stk = new StringTokenizer(value, ",");
        	while(stk.hasMoreTokens()) {
        		String token = stk.nextToken();
        		int index = token.indexOf("=");
        		_smsRedirectSenderMap.put(token.substring(0, index), token.substring(index+1));
        	}
        }

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "PROMOTOOL_URL");
        if(tempParam != null)
        	_smsRedirectPromotoolURL = tempParam.getValue();

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "SDR_PATH");
        if(tempParam != null)
        	_smsRedirectSDRPath = tempParam.getValue();
        else
        	_smsRedirectSDRPath = ".";

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMSREDIRECT, "SDR_ROTATION_SIZE");
        if(tempParam != null) {
        	try {
        		_smsRedirectRotationSize = Integer.parseInt(tempParam.getValue());
        	}
        	catch(Exception e) {
        	}
        }
        StringTokenizer tokenizer = new StringTokenizer(dbManager.getCountryPrefix(), ",");
        _countryPrefix = tokenizer.nextToken();

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "CURRENCY_STRING");
        if(tempParam != null && tempParam.getValue() != null)
        	_currencyStr = tempParam.getValue();

        String temp = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_CRICKET_PASS", null);
        if(temp != null)
        	m_defaultCricketPass = temp;

        m_cricketInterval = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "CRICKET_INTERVAL", 2);
        luceneIndexer = LuceneIndexerFactory.getInstance();

        tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(SMS, "WEB_RESULTS");
        if(tempParam != null && tempParam.getValue() != null)
        {
        	try
        	{
        		_ccSearchCount = Integer.parseInt(tempParam.getValue().trim());
        	}
        	catch(Exception e)
        	{
        		_ccSearchCount = 15;	
        	}
        }
    }
    
    private void initWarParams() {
		List<Parameters> warParameters = CacheManagerUtil.getParametersCacheManager().getParameters("WAR");
		HashMap warParamMap = new HashMap();
		for (int i = 0; warParameters != null && i < warParameters.size(); i++) {
			warParamMap.put(warParameters.get(i).getParam(), warParameters.get(i).getValue());
		}
		if (warParamMap.containsKey("USSD_FREE_CATEGORY"))
			m_freeCategoryID = Integer.parseInt((String) warParamMap.get("USSD_FREE_CATEGORY"));
		if (warParamMap.containsKey("USSD_CHARGED_CATEGORY"))
			m_chargedCategoryID = Integer.parseInt((String) warParamMap
					.get("USSD_CHARGED_CATEGORY"));
		if (warParamMap.containsKey("USSD_TOP10_CATEGORIES")) {
			m_top10Categories = new ArrayList();
			String m_top10CategoriesStr = (String) warParamMap.get("USSD_TOP10_CATEGORIES");
			StringTokenizer stk = new StringTokenizer(m_top10CategoriesStr);
			while (stk.hasMoreTokens())
				m_top10Categories.add(stk.nextToken());
		}
		if (warParamMap.containsKey("USSD_FREEZONE_CATEGORIES")) {
			m_freeZoneCategories = new ArrayList();
			String m_freeZoneCategoriesStr = (String) warParamMap.get("USSD_FREEZONE_CATEGORIES");
			StringTokenizer stk = new StringTokenizer(m_freeZoneCategoriesStr);
			while (stk.hasMoreTokens())
				m_freeZoneCategories.add(stk.nextToken());
		}
		if (warParamMap.containsKey("USSD_ADVANCE_RENTAL")) {
			m_advanceRentalMap = new HashMap();
			StringTokenizer stk = new StringTokenizer((String) warParamMap
					.get("USSD_ADVANCE_RENTAL"), ",");
			while (stk.hasMoreTokens()) {
				String token = stk.nextToken();
				m_advanceRentalMap.put(token.substring(0, token.indexOf(";")), token
						.substring(token.indexOf(";") + 1));
			}
		}
		if(warParamMap.containsKey("USSD_URL_REPLACE_PACKAGE"))
			m_ussdUrlReplacePackage = (String) warParamMap.get("USSD_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("AUTODIAL_URL_REPLACE_PACKAGE"))
			m_autodialUrlReplacePackage = (String) warParamMap.get("AUTODIAL_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("AUTODIAL_FREE_ADVANCE_RENTAL_SUB_CLASSES"))
			m_freeAdvRntlSubClass = (String) warParamMap
					.get("AUTODIAL_FREE_ADVANCE_RENTAL_SUB_CLASSES");
		if(warParamMap.containsKey("ENVIO_URL_REPLACE_PACKAGE"))
			m_envIOUrlReplacePackage = (String) warParamMap.get("ENVIO_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("EC_URL_REPLACE_PACKAGE"))
			m_ecUrlReplacePackage = (String) warParamMap.get("EC_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("AUTO_CATEGORY_LOWER"))
			m_autoCategoryLower = Integer.parseInt((String) warParamMap.get("AUTO_CATEGORY_LOWER"));
		if(warParamMap.containsKey("AUTO_CATEGORY_UPPER"))
			m_autoCategoryUpper = Integer.parseInt((String) warParamMap.get("AUTO_CATEGORY_UPPER"));
		if (warParamMap.containsKey("AUTO_SUB_CLASS_CATEGORY")) {
			String subCatStr = (String) warParamMap.get("AUTO_SUB_CLASS_CATEGORY");
			StringTokenizer stk = new StringTokenizer(subCatStr, ",");
			if (stk.hasMoreTokens())
				m_autoSubClass = stk.nextToken();
			if (stk.hasMoreTokens())
				m_autoSubCategory = Integer.parseInt(stk.nextToken());
		}
		if (warParamMap.containsKey("AUTO_ACTIVATED_BY_IP_MAP")) {
			String actByIPMapStr = (String) warParamMap.get("AUTO_ACTIVATED_BY_IP_MAP");
			StringTokenizer mainStk = new StringTokenizer(actByIPMapStr, ";");
			while (mainStk.hasMoreTokens()) {
				String token = mainStk.nextToken();
				String actBy = token.substring(0, token.indexOf("="));
				StringTokenizer stk = new StringTokenizer(token.substring(token.indexOf("=") + 1),
						",");
				while (stk.hasMoreTokens())
					m_autoActivatedByMap.put(stk.nextToken(), actBy);
			}
		}
		if(warParamMap.containsKey("AUTO_FLAG_SUB_CLASS_MAP")) {
			String actByIPMapStr = (String) warParamMap.get("AUTO_FLAG_SUB_CLASS_MAP");
			StringTokenizer mainStk = new StringTokenizer(actByIPMapStr, ";");
			while (mainStk.hasMoreTokens()) {
				String token = mainStk.nextToken();
				int index = token.indexOf(",");
				String flag = token.substring(0, index);
				String subClass = token.substring(index + 1);
				m_autoFlagSubClassMap.put(flag, subClass);
			}
		}
		/*if(warParamMap.containsKey("EC_TARIFF_SUB_CLASS_MAP")) {
			String actByIPMapStr = (String) warParamMap.get("EC_TARIFF_SUB_CLASS_MAP");
			StringTokenizer mainStk = new StringTokenizer(actByIPMapStr, ";");
			while (mainStk.hasMoreTokens()) {
				String token = mainStk.nextToken();
				int index = token.indexOf(",");
				String tariff = token.substring(0, index);
				String subClass = token.substring(index + 1);
				m_ecTariffSubClassMap.put(tariff, subClass);
			}
		}*/
		if(warParamMap.containsKey("EC_FLAG_SUB_CLASS_MAP")) {
			String actByIPMapStr = (String) warParamMap.get("EC_FLAG_SUB_CLASS_MAP");
			StringTokenizer mainStk = new StringTokenizer(actByIPMapStr, ";");
			while (mainStk.hasMoreTokens()) {
				String token = mainStk.nextToken();
				int index = token.indexOf(",");
				String flag = token.substring(0, index);
				String subClass = token.substring(index + 1);
				m_ecFlagSubClassMap.put(flag, subClass);
			}
		}
		if(warParamMap.containsKey("ENVIO_CATEGORY"))
			m_envIOCatgory = Integer.parseInt((String)warParamMap.get("ENVIO_CATEGORY"));
		if(warParamMap.containsKey("ENVIO_SUBSCRIPTION_CLASS"))
			m_envIOSubClass = (String)warParamMap.get("ENVIO_SUBSCRIPTION_CLASS");
		
		// initing EnvIO responses
		for(int eCount = 0;;eCount++) {
			if(warParamMap.containsKey("ENVIO_RESPONSE_" + eCount))
				m_envIOResposeMap.put("ENVIO_RESPONSE_" + eCount, (String) warParamMap
						.get("ENVIO_RESPONSE_" + eCount));
			else
				break;
		}
		
		if(warParamMap.containsKey("MOD_URL_REPLACE_PACKAGE"))
			m_modUrlReplacePackage = (String) warParamMap.get("MOD_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("MOD_SUB_CLASS"))
			m_modSubClass = (String) warParamMap.get("MOD_SUB_CLASS");
		if(warParamMap.containsKey("MOD_CATEGORY"))
			m_modCategory = Integer.parseInt(((String) warParamMap.get("MOD_CATEGORY")).trim());
	}
	
    public String getSMSText(String promoID, String smsDate)
    {
        String smsId = promoID + "_" + smsDate;
        Object returnObject = null;
        if (URL_SEND_ACT_SUCCESS.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        else if (URL_SEND_ACT_FAILURE.equals(smsId)
                || URL_SEND_ACT_ALREADY_ACT.equals(smsId)
                || URL_SEND_ACT_MISSING_PARAMETER.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        else if (URL_SEND_SEL_SUCCESS.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        else if (URL_SEND_SEL_FAILURE.equals(smsId)
                || URL_SEND_SEL_INACTIVE.equals(smsId)
                || URL_SEND_SEL_INVALID_TONEID.equals(smsId)
                || URL_SEND_SEL_MISSING_PARAMETER.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        else if (URL_SEND_DEACT_SUCCESS.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        else if (URL_SEND_DEACT_FAILURE.equals(smsId)
                || URL_SEND_DEACT_ALREADY_INACT.equals(smsId)
                || URL_SEND_DEACT_MISSING_PARAMETER.equals(smsId))
            returnObject = m_subUnsubsmsTable.get(smsId);
        if (returnObject != null)
            return (String) returnObject;
        return null;
    }

    private void loadSubUnsubSMSTable(String promoID)
    {
        BulkPromoSMS[] bulkPromoSMSes = getBulkPromoSmses(promoID);
        if (bulkPromoSMSes != null && bulkPromoSMSes.length > 0)
        {
            for (int i = 0; i < bulkPromoSMSes.length; i++)
            {
                String object = null;
                if (bulkPromoSMSes[i].bulkPromoId() != null)
                {
                    object = bulkPromoSMSes[i].bulkPromoId();
                    if (bulkPromoSMSes[i].smsDate() != null)
                    {
                        object = object + "_" + bulkPromoSMSes[i].smsDate();
                    }
                }
                if (object != null)
                    m_subUnsubsmsTable.put(object, bulkPromoSMSes[i].smsText());
            }
        }
    }

    private BulkPromoSMS[] getBulkPromoSmses(String promoId)
    {
//       String _method = "getBulkPromoSmses()";
        ////logger.info("****** no parameters.");
        return (RBTDBManager.getInstance()
                .getBulkPromoSmses(promoId));
    }

    /*
     * public void initTools() { Tools.init("RBT_WAR", 6, false); }
     */

    public boolean isValidIP(String strIP){
        if(strIP != null) { 
			for (int i = 0; i < m_validIP.length; i++){
				if (strIP.trim().equalsIgnoreCase(m_validIP[i].trim()))
					return true;
			}
		}
        return false;
    }

    public void fillChargePeriodMonthMap()
    {
        chargePeriodMonthMap.put("O", "One Time");
        chargePeriodMonthMap.put("B", "Billing Cycle");
        chargePeriodMonthMap.put("D", "Day(s)");
        chargePeriodMonthMap.put("M", "Month(s)");
    }

    public boolean isSubActive(String strSubID)
    {
        return (isSubActive(getSubscriber(strSubID)));
    }

    public String actSubscriber(String strSubID, String strActby, boolean bPrepaid,
			boolean bSendToHLR, String strActInfo, String circleId) {
		return actSubscriber(strSubID, strActby, bPrepaid, bSendToHLR, strActInfo, "DEFAULT", circleId);
	}

    
    public String actSubscriber(String strSubID, String strActby,
            boolean bPrepaid, boolean bSendToHLR, String strActInfo, String subClass, String circleId)
    {
        Subscriber subscriber;
        String sub;

        int period = 0;

        if (!bSendToHLR && !m_useSubscriptionManager)
        {
            period = 30;
        }

        subscriber = getSubscriber(strSubID);
//        Date currDate = new Date(System.currentTimeMillis());
        //Calendar endCal = Calendar.getInstance();
        //endCal.set(2037, 0, 1);
//        Date endDate = endCal.getTime();
        if (subscriber != null)
        {
            if (!isSubActive(subscriber))
            {
                sub = activate(strSubID, bPrepaid, strActby, subscriber
                        .endDate(), period, strActInfo, subClass, circleId);

                if (sub != null)
                    return sub;
                else
                    return SUCCESS;
            }
            else
            {
				if(m_showUpgrade) {
					if(subscriber.subscriptionClass().equals(subClass))
						return "Subscriber already exists";
					changeSubscriptionInternal(strSubID, subscriber.subscriptionClass(), subClass, subscriber);
				}
				else {
					if(m_allowReactivation) {
						reactivateSubscriber(strSubID, bPrepaid, true, strActby, strActInfo);
						return SUCCESS;
					}
					return "Subscriber already exists";
				}
			}
        }
        if (subscriber == null)
        {
            sub = activate(strSubID, bPrepaid, strActby, null, period,
                           strActInfo, subClass, circleId);
            if (sub != null)
                return "Unable to activate subscriber due to some internal reasons";
        }

        return SUCCESS;
    }
	 private String activate(String strSubID, boolean bPrepaid, String strActby,
            Date endDate, int trialPeriod, String strActInfo, String subClass, String circleId)
    {
        Subscriber subscriber = activateSubscriber(strSubID, strActby, null,
                                                   bPrepaid, trialPeriod,
                                                   strActInfo, subClass, circleId);
        if (subscriber == null)
            return "You cannot activate just after deactivation. Pls try after some time";

        return null;
    } 

    //	Trial Hack

    private String activate(String strSubID, boolean bPrepaid, String strActby,
            Date endDate, int trialPeriod, String strActInfo, String subClass,
            String selClass, String subscriptionType, String circleId)
    {
        Subscriber subscriber = trialActivateSubscriber(strSubID, strActby,
                                                        null, bPrepaid,
                                                        trialPeriod,
                                                        strActInfo, subClass,
                                                        selClass,
                                                        subscriptionType, circleId);
        if (subscriber == null)
            return "You cannot activate just after deactivation. Pls try after some time";

        return null;
    }

    //
/*    public String reActSubscriber(String strSubID, String strActby,
            boolean bPrepaid, String strActInfo)
    {
        Subscriber subscriber = getSubscriber(strSubID);

        if (subscriber == null)
        {
            return "Subscriber does not exist";
        }
        else
        {
            if (isSubActive(subscriber))
                reactivateSubscriber(strSubID, bPrepaid, false, strActby,
                                     strActInfo);
            else
                return "Subscriber is deactive";

            return SUCCESS;

        }
    }
	*/

    public boolean isSubActive(Subscriber subscriber)
    {
		return (RBTDBManager.getInstance().isSubActive(subscriber));
	}
    public boolean isSubActive(Subscriber subscriber, int rbtType)
    {
		return (RBTDBManager.getInstance().isSubActive(subscriber));
	}
    public StringBuffer updateSubscriberId(String newSubscriberId,String subscriberId)
    {
    	if(subscriberId==null || newSubscriberId==null){
    		return new StringBuffer("FAILURE:MISSING PARAMETER");
    	}
    		
    	String success = RBTDBManager.getInstance().updateSubscriberId(newSubscriberId,subscriberId);
   		return new StringBuffer(success);
    }
    
    public String addGroupForSubscriberID(String preGroupID, String groupName,
			String subscriberID, String groupPromoID)
    {
    	if(subscriberID == null)
    	{
    		return "FAILURE";
    	}
    	if(preGroupID != null)
    	{
    		List<PredefinedGroup> predefinedGroups = CacheManagerUtil.getPredefinedGroupCacheManager().getAllPredefinedGroups();
    		if(predefinedGroups != null)
    		{
    			for(int i=0;i<predefinedGroups.size();i++)
    			{
    				if(predefinedGroups.get(i).getPreGroupID().equals(preGroupID))
    				{
    					groupName = predefinedGroups.get(i).getPreGroupName();
    					break;
    				}
    			}
    		}
    	}
    	String success = RBTDBManager.getInstance().addGroupForSubscriberID(preGroupID, groupName, subscriberID, groupPromoID);
    	if(success.equals(MAX_GROUP_PRESENT_FOR_SUBSCRIBER))
    		return "MAX_GROUP_PRESENT";
    	else if(success.equals(USER_NOT_ACTIVE))
    		return "USER_NOT_ACTIVE";
    	else if(success.equals(GROUP_ADDED_SUCCESFULLY))
    		return "SUCCESS";
    	else
    		return "FAILURE";
    }
    
    public Groups[] getActiveGroupsForSubscriberID(String subscriberID)
	{
		Groups [] groups = RBTDBManager.getInstance().getActiveGroupsForSubscriberID(subscriberID);
		return groups;
	}
    
    public String addCallerInGroup(String subscriberID, int groupID, String callerID, String callerName)
    {
    	if(subscriberID == null || groupID==0 || callerID == null || callerID.equals(""))
    	{
    		return "FAILURE";
    	}
    	String response = RBTDBManager.getInstance().addCallerInGroup(subscriberID, groupID, callerID, callerName);
    	if(response.equals(CALLER_ADDED_TO_GROUP))
    	{
    		return "SUCCESS";
    	}
    	else if(response.equals(ALREADY_PERSONALIZED_SELECTION_FOR_CALLER))
    	{
    		return "FAILURE_PERSONALIZED_SELECTION_PRESENT";
    	}
    	else if(response.equals(CALLER_ALREADY_PRESENT_IN_GROUP))
    	{
    		return "FAILURE_ALREADY_IN_GROUP";
    	}
    	return null;
    }
    
    public GroupMembers[] getActiveMembersForGroupID(int groupID)
    {
    	GroupMembers[] groupMembers = RBTDBManager.getInstance().getActiveMembersForGroupID(groupID);
    	return groupMembers;
    }
    
    public Groups getActiveGroupByGroupName(String groupName, String subscriberID)
    {
    	Groups group = RBTDBManager.getInstance().getActiveGroupByGroupName(groupName, subscriberID);
    	return group;
    }
    
    public String removeCallerFromGroup(String subscriberID, int groupID, String callerID)
    {
    	if(groupID == -1 || callerID == null)
    	{
    		return "FAILURE";
    	}
    	boolean res = RBTDBManager.getInstance().removeCallerFromGroup(subscriberID, groupID, callerID);
    	if(res)
    		return "SUCCESS";
    	else
    		return "FAILURE";
    }
    
    public String changeGroupForCaller(String subscriberID, String callerID, int fromGroupID, int toGroupID)
    {
    	if(subscriberID == null || callerID == null || fromGroupID == -1 || toGroupID == -1)
    	{
    		return "FAILURE";
    	}
    	boolean res = RBTDBManager.getInstance().changeGroupForCaller(subscriberID, callerID, fromGroupID, toGroupID);
    	if(res)
    		return "SUCCESS";
    	else
    		return "FAILURE";
    }
    
    public PredefinedGroup[] getPredefinedGroupsNotAddedForSubscriber(String subscriberID)
    {
    	List<PredefinedGroup> groupsNotAddedForSubscriber = new ArrayList<PredefinedGroup>();
    	boolean groupAdded = false;
    	List<PredefinedGroup> predefinedGroups = CacheManagerUtil.getPredefinedGroupCacheManager().getAllPredefinedGroups();
    	Groups[] groups = RBTDBManager.getInstance().getPredefinedGroupsAddedForSubscriber(subscriberID);
    	if(predefinedGroups != null)
    	{
    		for(int i=0;i<predefinedGroups.size();i++)
    		{
    			groupAdded = false;
    			if(groups != null)
    			{
    				for(int j=0;j<groups.length;j++)
    				{
    					if(groups[j].preGroupID() != null)
    					{
    						if(predefinedGroups.get(i).getPreGroupID().equals(groups[j].preGroupID()))
    						{
    							groupAdded = true;
    							break;
    						}
    					}
    				}
    			}
    			if(!groupAdded)
    			{
    				groupsNotAddedForSubscriber.add(predefinedGroups.get(i));
    			}
    		}
    	}
    	return groupsNotAddedForSubscriber.toArray(new PredefinedGroup[0]);
    	
    }
    
    public String deleteGroup(String subscriberID, int groupID, String deactivatedBy)
    {
    	if(groupID == -1)
    	{
    		return "FAILURE";
    	}
    	boolean res = RBTDBManager.getInstance().deleteGroup(subscriberID, groupID, deactivatedBy);
    	if(res)
    		return "SUCCESS";
    	else
    		return "FAILURE";
    }
    
    public String deactSubscriber(String strSubID, String strDeactby,
            boolean bSendToHLR, String actInfo, boolean checkSubClass, Subscriber subscriber)
    {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
        if (m_useSubscriptionManager)
            bSendToHLR = true;
        //Subscriber subscriber = getSubscriber(strSubID);

        if (subscriber == null)
            return "Subscriber does not exist";

        else if (!isSubActive(subscriber))
            return "Subscriber already deactivated by "
                    + subscriber.deactivatedBy() + " at "
                    + df.format(subscriber.endDate());

        else
        {
        	Connection conn = RBTDBManager.getInstance().getConnection();
        	String dct = RBTDBManager.getInstance()
              .deactivateSubscriber(conn, strSubID, strDeactby, null,
                  m_delSelections, bSendToHLR, m_useSubscriptionManager, false, checkSubClass, subscriber.rbtType(), subscriber, actInfo, null);
            
            if(dct != null && dct.equals("ACT_PENDING"))
            {
                return "Subscriber activation being processed. Try after some time";
            }
            else if(dct != null && dct.equals("DCT_NOT_ALLOWED")) 
            { 
                return "Deactivation not allowed for Subscriber"; 
            } 

        }

        return SUCCESS;
    }
    
    public String deactSubscriber(String strSubID, String strDeactby,
            boolean bSendToHLR, String actInfo, boolean checkSubClass)
    {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
        if (m_useSubscriptionManager)
            bSendToHLR = true;
        Subscriber subscriber = getSubscriber(strSubID);

        if (subscriber == null)
            return "Subscriber does not exist";

        else if (!isSubActive(subscriber))
            return "Subscriber already deactivated by "
                    + subscriber.deactivatedBy() + " at "
                    + df.format(subscriber.endDate());

        else
        {
        	Connection conn = RBTDBManager.getInstance().getConnection();
        	String dct = RBTDBManager.getInstance()
              .deactivateSubscriber(conn, strSubID, strDeactby, null,
                  m_delSelections, bSendToHLR, m_useSubscriptionManager, false, checkSubClass, subscriber.rbtType(), subscriber, actInfo, null);
            
            if(dct != null && dct.equals("ACT_PENDING"))
            {
                return "Subscriber activation being processed. Try after some time";
            }
            else if(dct != null && dct.equals("DCT_NOT_ALLOWED")) 
            { 
                return "Deactivation not allowed for Subscriber"; 
            } 

        }

        return SUCCESS;
    }

    public String subscriberStatus(String strSubID, Subscriber subscriber)
    {
//        Subscriber subscriber = getSubscriber(strSubID);

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
//        Date EndDate = new Date();
        String lastChargeDate = null;
        if (subscriber == null)
            return "Subscriber does not exists: ";
        String startDate = df.format(subscriber.startDate());
        String endDate = df.format(subscriber.endDate());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        String actBy = subscriber.activatedBy();
        boolean prepaid = subscriber.prepaidYes();
        String subType = null;
//        Date actDate = subscriber.activationDate();

        Date nxtchargingDate = subscriber.nextChargingDate();
        String subyes = subscriber.subYes();
        if (actBy != null)
        {
            if (actBy.equalsIgnoreCase("CC"))
                actBy = CC;
            if (actBy.equalsIgnoreCase("SMS"))
                actBy = SMS;
            if (actBy.equalsIgnoreCase("VP"))
                actBy = VP;
            if (actBy.equalsIgnoreCase("OP"))
                actBy = OP;
        }

        String deactBy = subscriber.deactivatedBy();
        if (deactBy != null)
        {
            if (deactBy.equalsIgnoreCase("CC"))
                deactBy = CC;
            if (deactBy.equalsIgnoreCase("SMS"))
                deactBy = SMS;
            if (deactBy.equalsIgnoreCase("VP"))
                deactBy = VP;
            if (deactBy.equalsIgnoreCase("AU"))
                deactBy = AU;
            if (deactBy.equalsIgnoreCase("OP"))
                deactBy = OP;
            if (deactBy.equalsIgnoreCase("NA"))
                deactBy = NA;
            if (deactBy.equalsIgnoreCase("NEF"))
                deactBy = NEF;
        }
        boolean submanager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");
        if (submanager)
        {
            if (nxtchargingDate != null
                    && nxtchargingDate.before(Calendar.getInstance().getTime()))
                lastChargeDate = df.format(nxtchargingDate);
            else
                lastChargeDate = " - ";
        }
        else
            lastChargeDate = " - ";
        if (prepaid == true)
            subType = "Prepaid";
        else
            subType = "Postpaid";

        String subInfo = "";
        String activationInfo = subscriber.activationInfo();
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBSCRIPTION_CHARGE", "FALSE")) { 
	        String amtCharged = "-";
	        if( activationInfo != null && activationInfo.indexOf("|AMT:") > -1 && activationInfo.indexOf(":AMT|") > -1)
	        {
	        	int firstIndex = activationInfo.indexOf("|AMT:");
	        	int secondIndex = activationInfo.indexOf(":AMT|");
	        	amtCharged = activationInfo.substring(firstIndex+5,secondIndex);
	        }
	        subInfo += "AMT:"+amtCharged+"<br/>"; 
        } 
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_INTRO_PROMPT_STATUS", "FALSE")) { 
	        String introPromptStatus = "-"; 
	        String ipFlag = subscriber.cosID() ; 
	        if(ipFlag == null || ipFlag.trim().equalsIgnoreCase("0")) 
	                introPromptStatus = "Plays"; 
	        else 
	                introPromptStatus = "Does Not Play."; 
	        subInfo += "INTRO_PROMPT:"+introPromptStatus+"<br/>"; 
	    } 
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_LOYALTY_POINTS", "FALSE")) { 
        	subInfo += "LTP:"+RBTDBManager.getInstance().getLTPPoints(activationInfo)+"<br/>"; 
        } 
        String copyInfo = getCopyInfo(activationInfo); 
        if(copyInfo != null) 
            subInfo += "COPY:"+copyInfo+"<br/>"; 
        if(subInfo == null || subInfo.length() <= 0) 
        	subInfo = "-"; 

        if (isSubActive(subscriber))
        {
            if (submanager == true)
            {
                if (subyes.equalsIgnoreCase("A")
                        || subyes.equalsIgnoreCase("N")
                        || subyes.equalsIgnoreCase("E"))
                {
                    if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE"))
                        subType = "Unknown";
                    return (startDate + "," + actBy + ",Activation Pending,"
                    		+ subType + "," + lastChargeDate+","+subInfo);
                }
				else if (subyes.equalsIgnoreCase("G"))
                {
                    if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE"))
                        subType = "Unknown";
                    return (startDate + "," + actBy + ",Activation Grace,"
                    		+ subType + "," + lastChargeDate+","+subInfo);
                }
                else if (subyes.equalsIgnoreCase("E"))
                {
                    if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE"))
                        subType = "Unknown";
                    return (startDate + "," + actBy + ",Activation Error,"
                    		+ subType + "," + lastChargeDate+","+subInfo);
                }
                else if (subyes.equalsIgnoreCase("Z")) 
                { 
                    if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE")) 
                        subType = "Unknown"; 
                    return (startDate + "," + actBy + ",Suspended," 
                            + subType + "," + lastChargeDate+","+subInfo); 
                } 
            }

            return (startDate + "," + actBy + ",Active," + subType + "," + lastChargeDate+","+subInfo);
        }
        else
        {
            if (subyes.equalsIgnoreCase("D") || subyes.equalsIgnoreCase("P")
                    || subyes.equalsIgnoreCase("F"))
            {
                return (startDate + "," + actBy + ",Deactivation Pending,"
                        + subType + "," + lastChargeDate);
            }
            else if (subyes.equalsIgnoreCase("F"))
            {
                return (startDate + "," + actBy + ",Deactivation Error,"
                        + subType + "," + lastChargeDate);
            }
        }

        if ((deactBy != null) && (deactBy.equalsIgnoreCase("AU")))
            return startDate + "," + actBy + ",Deactivated automatically on "
                    + endDate + ". No access for " + m_days + "," + subType
                    + "," + lastChargeDate;

        return (startDate + "," + actBy + ",Deactivated on  " + endDate + "("
                + deactBy + ")" + "," + subType + "," + lastChargeDate);
    }

    public String[] getSubscriberSelections(String strSubID, Subscriber subscriber)
    {
        Categories category = null;
        ClipMinimal clip = null;
        String callerid, status, selectedby;
        List statusList = new ArrayList();
        String selInterval = "-";
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");

        SubscriberStatus[] subscriberStatus = getAllSubscriberSelectionRecords(strSubID);
        if (subscriberStatus == null)
                /*|| (subscriberStatus.length == 1 && subscriberStatus[0]
                        .status() == 90))*/
            return null;
//        Subscriber subscriber = getSubscriber(strSubID);
        for (int i = 0; i < subscriberStatus.length; i++)
        {
            int st = subscriberStatus[i].status();
            String validity = "-";
            /*if (st == 90)
                continue;*/
            callerid = subscriberStatus[i].callerID();
            String circleID = getCircleID(subscriberStatus[i].subID());
            char prepaidYes = 'n';
            if(subscriberStatus[i].prepaidYes())
            	prepaidYes = 'y';
            category = getCategory(subscriberStatus[i].categoryID(), circleID, prepaidYes);
            clip = getClipRBT(subscriberStatus[i].subscriberFile());
            String clipExpiryDate = "-";
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
            if(clip != null && clip.getEndTime() != null)
            {
            	Date expiryDate = clip.getEndTime();
            	clipExpiryDate = sdf.format(expiryDate);
            }
            Date nextCharge = subscriberStatus[i].nextChargingDate();
//            Date starttime = subscriberStatus[i].startTime();
            selectedby = subscriberStatus[i].selectedBy();
            String fromTime = "" + subscriberStatus[i].fromTime();
            String toTime = "" + (subscriberStatus[i].toTime());
            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyy");
            if(m_allowDayOfWeekFutureDate)
            {
            	selInterval = subscriberStatus[i].selInterval();
//            	System.out.println("the value of sel interval "+selInterval);
            	if(selInterval != null)
            	{
            		if(selInterval.charAt(0) == 'Y')
            		{
//            			System.out.println(" im in future date");
            			selInterval = selInterval.substring(1);
            			Date futureDate = null;
            			try {
							futureDate = s.parse(selInterval);
						} catch (ParseException e) {
							e.printStackTrace();
						}
            			selInterval = sdf.format(futureDate);
            		}
            		else
            		{
//            			System.out.println("i m in week");
            			selInterval = selInterval.substring(1);
            			int ch = Integer.parseInt(selInterval);
            			switch(ch)
            			{
            				case 1: selInterval = "SUNDAY";
            						break;
            				case 2: selInterval = "MONDAY";
            						break;
            				case 3: selInterval = "TUESDAY";
            						break;
            				case 4: selInterval = "WEDNESDAY";
            						break;
            				case 5: selInterval = "THURSDAY";
            						break;
            				case 6: selInterval = "FRIDAY";
            						break;
            				case 7: selInterval = "SATURDAY";
            						break;
            				default : selInterval = "-";
            			}
            		}
            	}
            	else 
            		selInterval = "-";
            }
            String tempclasstype = subscriberStatus[i].classType();
//            Date endtime = subscriberStatus[i].endTime();
            String selStatus = subscriberStatus[i].selStatus();
            String lastChargeDate = null;
            String selInfo = "-"; 
            String copyInfo = getCopyInfo(subscriberStatus[i].selectionInfo()); 
            if(copyInfo != null) 
                selInfo = "COPY:"+copyInfo; 
            if (nextCharge != null)
            {
                long tmp = nextCharge.getTime()
                        - Calendar.getInstance().getTime().getTime();
                int days = new Long(tmp / (3600 * 1000 * 24)).intValue();
                if (days > 0)
                    validity = "" + days + " days";
                else
                    validity = "0 days";
            }
            if (st != 80)
            {
                fromTime = " - ";
                toTime = " - ";
            }
            String Classtype = " - ";
            if (m_showchargeduration)
            {
                if (m_Monthly.contains(tempclasstype))
                    Classtype = "MONTHLY";
                if (m_Weekly.contains(tempclasstype))
                    Classtype = "WEEKLY";
            }
            String temp = null;
            if (category != null)
            {
                if (category.type() == SHUFFLE || category.type() == RECORD || st == 90)
                {
                	temp = category.name() + "," + " " + "," + "-"+",-";
                }
                else
                {
                	temp = category.name() + "," + " " + "," + "-"+",-";
                    if (clip != null)
                    {
                        String promoID = ((clip.getPromoID() == null) ? "-" : clip
                                .getPromoID());
                        temp = category.name() + "," + clip.getClipName() + ","
                        + promoID.replaceAll(",", ";")+","+clip.getWavFile();
                    }
                }
                String selectionStatus = " Active ";
                boolean submanager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");
                if (submanager == true)
                {
                    if (selStatus != null
                            && (selStatus.equalsIgnoreCase("A")
                                    || selStatus.equalsIgnoreCase("N") || selStatus
                                    .equalsIgnoreCase("W") || selStatus.equalsIgnoreCase("E")))
                    {
                        selectionStatus = "Activation Pending";
                    }
                    else if (selStatus != null && selStatus.equalsIgnoreCase("G")) 
                    { 
                        selectionStatus = "Activation Grace"; 
                    } 
                    else if (selStatus != null && selStatus.equalsIgnoreCase("E"))
                    {
                        selectionStatus = "Activation Error";
                    }
                    else if (selStatus != null && selStatus.equalsIgnoreCase("Z"))
                    {
                        selectionStatus = "Suspended";
                    }
                    else
                    {
                        selectionStatus = "Active";
                    }
                }
                if (submanager)
                {
                    if (nextCharge != null
                            && nextCharge.before(Calendar.getInstance()
                                    .getTime()))
                        lastChargeDate = df.format(nextCharge);
                    else
                        lastChargeDate = " - ";
                }
                else
                    lastChargeDate = " - ";

                if (callerid == null && st == 0)
                    status = "CORPORATE" + "," + temp + "," + tempclasstype
                            + "," + df.format(subscriberStatus[i].setTime())
                            + "," + validity + "," + selectedby + ","
                            + fromTime + "," + toTime + "," + selInterval + "," + Classtype + ","
                            + lastChargeDate + "," + selectionStatus + "," + st;
                else if (callerid == null)
                    status = "ALL" + "," + temp + "," + tempclasstype + ","
                            + df.format(subscriberStatus[i].setTime()) + ","
                            + validity + "," + selectedby + "," + fromTime
                            + "," + toTime + "," + selInterval + "," + Classtype + ","
                            + lastChargeDate + "," + selectionStatus + "," + st;
                else
                    status = callerid + "," + temp + "," + tempclasstype + ","
                            + df.format(subscriberStatus[i].setTime()) + ","
                            + validity + "," + selectedby + "," + fromTime
                            + "," + toTime + "," + selInterval + "," + Classtype + ","
                            + lastChargeDate + "," + selectionStatus + "," + st;
                status += ","+selInfo+","+clipExpiryDate;

                statusList.add(status);

            }
        }
        return (String[]) statusList.toArray(new String[0]);
    }
    
    public void deactivateSubscriberRecords(String strSubID, 
            String strCallerID, int status, int fTime, int tTime,String wavFile) 
    { 
        RBTDBManager.getInstance() 
                .deactivateSubscriberRecords(strSubID, strCallerID, status, 
                                             fTime, tTime, 
                                             m_useSubscriptionManager, "CC", wavFile); 
    } 


public String addCorporateSelection(String strSubID, String song,
            boolean bPrepaid, boolean bReact, boolean bRemSel, String strActBy,
            String strActInfo, boolean corpSplFeatureBln, String circleID)
    {
        logger.info("RBT::subscriber "
                + strSubID + " selected song " + song + " whether prepaid "
                + bPrepaid + " activated by " + strActBy + " corpSplFeature "+corpSplFeatureBln + " circleID "+circleID); 
        
        String subClass = "DEFAULT"; 
                if(corpSplFeatureBln) 
                { 
                        if (m_subClasses.containsKey("DEFAULT_CORP")) 
                           subClass = "DEFAULT_CORP"; 
                } 


        Subscriber subscriber = getSubscriber(strSubID);
		if (!isSubActive(subscriber))
        {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            //String sub = activate(strSubID, bPrepaid, strActBy, endDate, 0,
              //                    strActInfo, "DEFAULT");
            
            String sub = activate(strSubID, bPrepaid, strActBy, endDate, 0,
                    strActInfo, subClass, circleID);
            
            if (sub != null)
                return sub;
        }
        else
        {
            if(subscriber.rbtType() == 1)
				return "Corp Sel blocked for ADRBT subscriber";
			if (bReact)
                reactivateSubscriber(strSubID, bPrepaid, false, strActBy,
                                     strActInfo);
        }

        subscriber = getSubscriber(strSubID);
        String subYes = null;
        if (subscriber != null)
            subYes = subscriber.subYes();

        boolean changeSubType = false;
        if (subscriber != null && bPrepaid != subscriber.prepaidYes())
            changeSubType = true;

        if (bRemSel)
            deactivateSubscriberRecords(strSubID, null, 1, 0, 2359);

        if (subscriber != null){ 
            boolean OptIn = false; 
            if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                    OptIn = true; 
            
            int status = 0; 
            if(m_corpBlockSongChangeSomeSongs) 
            { 
                    if(!m_corpBlockSongs.contains(song)) 
                            status = 1; 
            } 

            addCorpSelections(strSubID, null, bPrepaid, changeSubType, 1, song,
                          null, status, 0, strActBy, strActInfo, 0, 2359, null, null,
                          null, subYes, subscriber.maxSelections(), subscriber
                                  .subscriptionClass(), OptIn, subscriber,null);
        }

        return null;
    }

    private void addBulkSelections(String strSubID, String strSong,
            String strPrepaid, String strPeriod, String strActBy,
           String strActInfo, StringBuffer success, StringBuffer failure, String subClass, String chargeClass, String circleId)
    {
        boolean isPrepaid = m_isPrepaid;
		if (strPrepaid != null) {
			if (strPrepaid.toLowerCase().startsWith("pre"))
                isPrepaid = true;
			else if (strPrepaid.toLowerCase().startsWith("post"))
                isPrepaid = false;
        }
        
        int trialPeriod = 0;
        try {
            trialPeriod = Integer.parseInt(strPeriod);
        } catch (NumberFormatException e) {
            trialPeriod = 0;
        }

        logger.info("RBT::subscriber "
                + strSubID + " selected song " + strSong + " " + strPrepaid
                + " activated by " + strActBy);

        
        String wavFile = null;
        ClipMinimal clip = null;
		boolean supportClipId = false;
        Parameters tempParam = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "BULK_TASK_SUPPORT_CLIPID");
        logger.info("RBT:: TempParam   " + tempParam);        
        if(tempParam != null && tempParam.getValue() != null && tempParam.getValue().trim().equalsIgnoreCase("TRUE")){
//        	supportClipId = Boolean.parseBoolean(tempParam.getValue().trim());
        	logger.info("RBT:: SupportClipID  " + tempParam.getValue());
        	supportClipId = true;
        }
        logger.info("RBT:: SupportClipID  " + supportClipId);
		if(supportClipId)
		{
			try
			{
				int clipID = Integer.parseInt(strSong);
				clip = getClip(clipID);
				if (clip == null) {
					failure.append("Invalid clip id " + strSong
							+ " requested by subscriber " + strSubID + ". \n");
					m_bulkSelectionFailureCount++;
					return;
				}
			}
			catch(NumberFormatException nfe){
				clip = null;
			}
			
		}
		else
		{
			clip = getClipPromoID(strSong);
		}
        if (clip == null)
        {
        	clip = getClipFromVCode(strSong);
        	if(clip == null)
			{
        		failure.append("Invalid clip id " + strSong
					+ " requested by subscriber " + strSubID + ". \n");
				m_bulkSelectionFailureCount++;
				return;
			}
		}
        wavFile = clip.getWavFile();

		Subscriber subscriber = getSubscriber(strSubID);
		if (!isSubActive(subscriber)) {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            String sub = activate(strSubID, isPrepaid, strActBy, endDate,
                                  trialPeriod, strActInfo, subClass, circleId);

			if (sub != null) {
                failure.append(strSubID + " activation failed.\n");
                m_bulkSelectionFailureCount++;
                return;
            }
        }

        subscriber = getSubscriber(strSubID);
        String subYes = null;
        if (subscriber != null)
            subYes = subscriber.subYes();

        boolean changeSubType = false;

        if (isPrepaid != subscriber.prepaidYes())
            changeSubType = true;


		if (subscriber != null) { 
            
            boolean OptIn = false; 
                    if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                            OptIn = true; 

            addSelections(strSubID, null, isPrepaid, changeSubType,
                          m_smsPromotionCategoryID, wavFile, null, 1,
                          trialPeriod, strActBy, strActInfo, 0, 2359, chargeClass, null,
                          null, subYes, subscriber.maxSelections(), subscriber
                                  .subscriptionClass(),OptIn, subscriber,null);
        }

        success.append("Activated " + wavFile + " for " + strPrepaid
                + " number " + strSubID + ".\n");
        m_bulkSelectionSuccessCount++;

        return;
    }
    /*
     * private String subID(String subscriberID) { if(subscriberID != null) {
     * if(subscriberID.startsWith("0")) { subscriberID =
     * subscriberID.substring(1); } if(subscriberID.startsWith("+91")) {
     * subscriberID = subscriberID.substring(3); }
     * if(subscriberID.startsWith("91")) { subscriberID =
     * subscriberID.substring(2); } } return subscriberID; }
     * 
     * public int isValidSub(String strSubID, Hashtable reason) { String
     * subscriber = subID(strSubID);
     * 
     * for(int i=0;i <m_validPrefix.length;i++) {
     * if(subscriber.substring(0,4).equalsIgnoreCase(m_validPrefix[i])) return
     * STATUS_SUCCESS; } reason.put("Reason", m_invalidPrefix); return
     * STATUS_NOT_AUTHORIZED; }
     */
    private HashMap getClipsByPhonemes()
    {
        ClipMinimal[] clips = getClips();
        HashMap hMap = null;

        if (clips != null)
        {
            hMap = new HashMap();
            for (int i = 0; i < clips.length; i++)
            {
                String encoded = getEncoding(clips[i].getClipName());
                if (hMap.containsKey(encoded))
                {
                    String name = (String) hMap.get(encoded) + ","
                            + clips[i].getWavFile();
                    hMap.put(encoded, "Duplicate Entry-" + name);
                }
                else
                {
                    hMap.put(encoded, clips[i].getWavFile());
                }
            }
        }
        return hMap;
    }

    private String getEncoding(String name)
    {
        DoubleMetaphone metaphone = new DoubleMetaphone();
        StringTokenizer st = new StringTokenizer(name.trim());
        String encoded = "";
        while (st.hasMoreTokens())
        {
            encoded = encoded + metaphone.generateKey(st.nextToken().trim());
        }
        encoded = encoded.trim();
        return encoded;
    }

    public ClipMinimal[] getClips()
    {
        return (RBTDBManager.getInstance()
                .getClipsByName(null));
    }

    public Clips[] getActiveClips(int categoryID)
    {
        return (RBTDBManager.getInstance().getAllClips(categoryID));
    }
    
    /*private long timeToSleep()
    {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 24);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        long nexttime = now.getTime().getTime();
        return nexttime - Calendar.getInstance().getTime().getTime();
    }*/

    /*public void run()
    {
        while (true)
        {
            Categories[] categories = RBTDBManager.getInstance()
                    .getBouquet();
            String category = m_profileCorporateCategories;
            if (categories != null && categories.length > 0)
            {
                category = category + ",";
                for (int i = 0; i < categories.length; i++)
                {
                    category = category
                            + new Integer(categories[i].id()).toString();
                    if (i < (categories.length - 1))
                    {
                        category = category + ",";
                    }
                }
            }
            Clips[] clips = RBTDBManager.init(m_dbURL, m_usePool,
                                              m_countryPrefix)
                    .getClipsNotInCategories(category);
            if (clips == null)
            {
                return;
            }
            m_clipTable = new Hashtable();
            for (int j = 0; j < clips.length; j++)
            {
                m_clipTable.put(clips[j].name(), clips[j].wavFile());
            }
            try
            {
                Thread.sleep(timeToSleep());
            }
            catch (InterruptedException e)
            {
                //do nothing
            }
        }
    }*/

   // public Hashtable getSMSPromoClips()
   // {
    //    return m_clipTable;
    //}

    //added to return all Clips. (19/12)
    public ClipMinimal[] getAllActiveClips()
    {
    	ClipMinimal[] allClips=RBTDBManager.getInstance()
        .getAllActiveClips();
    	if(allClips!=null){
    		
//    		System.out.println("all clips not null");
    	}
    	
    	else {
    		
    		System.out.println(" all clips is null");
    		
    	}
        return (allClips);
    }
    
    public Clips[] getAllClips(String categoryID)
    {
        return (RBTDBManager.getInstance()
                .getClipsInCategory(categoryID));
    }

    public Clips getClip(String name)
    {
        return (RBTDBManager.getInstance()
                .getClip(name));
    }

    public ClipMinimal getClip(int clipID)
    {
        return (RBTDBManager.getInstance()
                .getClipMinimal(clipID,false));
    }
    
    public Clips getClipFromDB(int clipID)
    {
    	return (RBTDBManager.getInstance().getClip(clipID));
    	
    }

    public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID,
            String date)
    {
        return (RBTDBManager.getInstance()
                .insertPickOfTheDay(4, clipID, date));
    }

    //added to include circle id parameter.
    public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID, String date, String circleId)
    {
        return (RBTDBManager.getInstance()
                .insertPickOfTheDay(4, clipID, date,circleId,'b',null));
    }

    // added to include profile id parameter. 
    public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID, String date, String circleId,String profile)
    {
        return (RBTDBManager.getInstance()
                .insertPickOfTheDay(4, clipID, date,circleId,'b',profile));
    }

    
    
    public PickOfTheDay[] getPickOfTheDays(String range)
    {
        return (RBTDBManager.getInstance()
                .getPickOfTheDays(range));
    }

    
    //added for getting pick of the days for specific circle ids.

    
    public PickOfTheDay[] getPickOfTheDays(String range,String circleId)
    {
        return (RBTDBManager.getInstance()
                .getPickOfTheDays(range,circleId));
    }

    
    public UserRights insertUserRights(String user, String rights)
    {
        return (RBTDBManager.getInstance()
                .insertUserRights(user, rights));
    }

    public String getUserRights(String userType)
    {
        String ret = null;
        if(m_userRights != null && m_userRights.containsKey(userType)) 
        { 
                ret = (String)m_userRights.get(userType); 
        } 

        return ret; 

    }

    public Categories getCategory(int categoryID, String circleID, char prepaidYes)
    {
		return (RBTDBManager.getInstance().getCategory(categoryID, circleID, prepaidYes));
	}

    public void deactivateSubscriberRecords(String strSubID,
            String strCallerID, int status, int fTime, int tTime)
    {
        RBTDBManager.getInstance()
                .deactivateSubscriberRecords(strSubID, strCallerID, status,
                                             fTime, tTime,
                                             m_useSubscriptionManager, "CC");
    }

    public void convertSelectionClassType(String subscriberID, String initType,
            String finalType)
    {
        RBTDBManager.getInstance()
                .convertSelectionClassType(subscriberID, initType, finalType,
                                           "CC");
    }

    public Categories[] getAllCategories()
    {
        return (RBTDBManager.getInstance()
                .getAllCategories());
    }
    
    
    public Subscriber getSubscriber(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getSubscriber(strSubID));
    }

    public Categories[] getActiveBouquet(int categoryID, String circleID, char prepaidYes)
    {
        return (RBTDBManager.getInstance()
                .getActiveBouquet(categoryID, circleID, prepaidYes));
    }

    public void setPrepaidYes(String strSubID, boolean bPrepaid)
    {
        RBTDBManager.getInstance()
                .setPrepaidYes(strSubID, bPrepaid);
    }

    public Categories[] getSubCategories(int categoryID, String circleID, char prepaidYes)
    {
        Categories[] cats =  (RBTDBManager.getInstance()
                .getSubCategories(categoryID, circleID, prepaidYes));
		Arrays.sort(cats, new CategoriesComparator());
		return cats;
    }

    public ClipMinimal getClipRBT(String wavFile)
    {
        return (RBTDBManager.getInstance()
                .getClipRBT(wavFile));
    }

    public String getChargeAmount(String classType)
    {
        return CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType).getAmount();
    }

    public Categories[] getActiveCategories()
    {
    	return (RBTDBManager.getInstance().getActiveCategoriesbyCircleID());
    	/*if(isCircleId()){
    		return (RBTDBManager.getInstance().getActiveCategoriesbyCircleID());
    	}
        return (RBTDBManager.getInstance()
               .getActiveCategories());*/
    }
    
    public boolean isCircleId()
    {
    	return RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "CIRCLE_ID", "FALSE");
    }
    
	 public Categories[] getActiveCategories(String circleID){
    	Categories[] cats = RBTDBManager.getInstance().getAllCategoriesForCircle(circleID, m_showSubCategoriesOutSide);
		Arrays.sort(cats, new CategoriesComparator());
		return cats;
    }

    public Categories[] getActiveCategories(String circleID, char prepaidYes){
    	Categories[] cats = RBTDBManager.getInstance().getAllCategories(circleID, prepaidYes);
		Arrays.sort(cats, new CategoriesComparator());
		return cats;
    }
    
    public SubscriberPromo addSubscriberPromo(String strSubID, int freedays,
            boolean bPrepaid, String strActBy, String type)
    {
        return (RBTDBManager.getInstance()
                .createSubscriberPromo(strSubID, freedays, bPrepaid, strActBy,
                                       type));
    }

    private void reactivateSubscriber(String strSubID, boolean bPrepaid,
            boolean bchargeSelections, String strActBy, String strActInfo)
    {
        //RBTDBManager.init(m_dbURL, m_usePool,
        //m_countryPrefix).reactivateSubscriber(strSubID, bPrepaid,
        //bchargeSelections, "CC");
        //String ret = deactivateSubscriber(strSubID, strActBy, null, m_delSelections, true, true);
        //if(ret != null && ret.equals("SUCCESS"))
        	//activateSubscriber(strSubID, strActBy, null, bPrepaid, 0, strActInfo,null);

    }
    public Subscriber activateSubscriber(String strSubID, String strActBy,
            Date startDate, boolean bPrepaid, int days, String strActInfo,
            String classType, String circleId)
    {
        /*return (RBTDBManager.getInstance()
                .activateSubscriber(strSubID, strActBy, startDate, bPrepaid,
                                    m_activationPeriod, days, strActInfo,
                                    classType, m_useSubscriptionManager));*/
    	return activateSubscriber(strSubID, strActBy, startDate, null, bPrepaid, days, strActInfo,
				classType, circleId);
    }

    //Added by Sreekar for TNB
    public Subscriber activateSubscriber(String strSubID, String strActBy, Date startDate,
			Date endDate, boolean bPrepaid, int days, String strActInfo, String classType, String circleId)
    {
        return (RBTDBManager.getInstance().activateSubscriber(strSubID,
				strActBy, startDate, endDate, bPrepaid, m_activationPeriod, days, strActInfo, classType,
				m_useSubscriptionManager,circleId));
    }

	/** *added by sandeep* */
    public String getChargingDetails(String subId) {
    	String chargDetails ="";
    	String[] selns = getallAirtelSubscriberSelections(subId, null, null, null);
    	SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd  h:mm a");
    	SimpleDateFormat guiformat = new SimpleDateFormat("dd-MMM-yyyy h:mm a");
    	String[] smsTypes = {"GIFTED","ACCEPT_ACK","REJECT_ACK"};
    	ViralSMSTable[] gifts = RBTDBManager.getInstance()
				.getViralSMSByTypesForSubscriber(subId, smsTypes);
    	int giftIndex=0;
    	int selnsIndex=0;
    	System.out.println("gifts="+gifts+"\t selns="+selns+"abcdef");
    	
    	if(gifts==null){
    		if(selns==null){
    			System.out.println("inside null");
    			return "";
    		}else{
    			while(selnsIndex<selns.length){
        			String[] selnDetail = selns[selnsIndex].split("::");
        			Date selDate=null;
        			try{
        			selDate = df.parse(selnDetail[8]);
        			} catch (java.text.ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			chargDetails=chargDetails+guiformat.format(selDate)+"::"+selnDetail[7]+
    				"::"+selnDetail[5]+";;";
        			selnsIndex++;
        		}
    		}
    	}if(selns==null&&gifts!=null){
    		while(giftIndex<gifts.length){
				String catName=gifts[giftIndex].selectedBy();
				String chargedAmount="-";
				if(catName.indexOf(":")>0)
				chargedAmount =catName.split(":")[1]; 
				chargDetails=chargDetails+guiformat.format(gifts[giftIndex].sentTime())+"::"+"Gift Tone &/or Gift Subscription Charges"
								+"::"+chargedAmount+";;";
				giftIndex++;
    		}
    		
    	}
    	if(gifts!=null&&gifts.length>0&&selns!=null&&selns.length>0){
    	while(giftIndex<gifts.length&&selnsIndex<selns.length){
    		String[] selnDetail = selns[selnsIndex].split("::");
    		Date selDate=null;
    		if(selnDetail.length==10){
    			try {
					selDate = df.parse(selnDetail[8]);
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			if(selDate.after(gifts[giftIndex].sentTime())){
    				chargDetails=chargDetails+guiformat.format(selDate)+"::"+selnDetail[7]+
    								"::"+selnDetail[5]+";;";
    				selnsIndex++;
    				
    			}else{
    				String catName=gifts[giftIndex].selectedBy();
    				String chargedAmount="-";
    				if(catName!=null&&catName.indexOf(":")>0)
    				chargedAmount =catName.split(":")[1]; 
    				chargDetails=chargDetails+guiformat.format(gifts[giftIndex].sentTime())+"::"+"Gift Tone &/or Gift Subscription Charges"
    								+"::"+chargedAmount+";;";
    				giftIndex++;
    			}
    		}
    	}
    	if(selnsIndex!=selns.length){
    		
    		while(selnsIndex<selns.length){
    			String[] selnDetail = selns[selnsIndex].split("::");
    			Date selDate=null;
    			try {
					selDate = df.parse(selnDetail[8]);
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			chargDetails=chargDetails+guiformat.format(selDate)+"::"+selnDetail[7]+
				"::"+selnDetail[5]+";;";
    			selnsIndex++;
    		}
    		
    	}if(giftIndex!=gifts.length){
    		while(giftIndex<gifts.length){
				String catName=gifts[giftIndex].selectedBy();
				String chargedAmount="-";
				if(catName!=null&&catName.indexOf(":")>0)
				chargedAmount =catName.split(":")[1]; 
				chargDetails=chargDetails+guiformat.format(gifts[giftIndex].sentTime())+"::"+"Gift Tone &/or Gift Subscription Charges"
								+"::"+chargedAmount+";;";
				giftIndex++;
    		}
    	}
    	}
    	return chargDetails;
    }
    public HashMap getCategoryIdMap(){
    	if(m_categoryIdMap==null){
    		initialiseCategoryMap();
    	}
    	return m_categoryClipsMap;
    }
    public HashMap getParentCategoriesMap(){
    	if(m_parentCategories==null){
    		initialiseClipMap();
    	}
    	return m_parentCategories;
    }
    public HashMap getcategoryClipMap(){
    	if(m_categoryClipsMap==null){
    		initialiseClipMap();
    	}
    	return m_categoryClipsMap;
    }
	private void initialiseCategoryMap(){
		this.m_categoryIdMap=RBTDBManager.getInstance()
		.initialiseCategoriesMap();	
		initialiseClipMap();
	}
	
	private void initialiseClipMap(){
		System.out.println("Initialising Cat Maps+++");
		
		m_parentCategories=new HashMap();
		m_categoryClipsMap=new HashMap();
		Categories[] parents = getActiveCategories();
		if(parents!=null){
			for(int i=0;i<parents.length;i++){
				ArrayList cats = new ArrayList();
				Categories[] subc = getSubCategories(parents[i].id(), parents[i].circleID(),
						parents[i].prepaidYes());
				if(subc!=null){
					for(int j=0;j<subc.length;j++){
						ArrayList catClips = new ArrayList();
						cats.add(new Integer(subc[j].id()));
						Clips[] clips = getAllClips(Integer.toString(subc[j].id()));
						if(clips!=null){
							for(int k=0;k<clips.length;k++){
								catClips.add(new Integer(clips[k].id()));
							}
							m_categoryClipsMap.put(new Integer(subc[j].id()), catClips);
						}
					}
					m_parentCategories.put(new Integer(parents[i].id()), cats);
				}
			}
		}
	}


	public String[] getClipDetails(int from,int to,String parentId,String subId,String searchOption,String searchText){
		getCategoryName(1);
		List clipDetails = new ArrayList();
		RBTMOHelper rbtHelper = RBTMOHelper.init();
		int count=0;
		System.out.println("in clipdets parent="+parentId+"\tsub="+subId+"\tsearch="+searchText+"from="+from+"\tto="+to);
		if(parentId!=null&&parentId.length()>0&&!parentId.equalsIgnoreCase("all")){
			String parent = (String) m_categoryIdMap.get(parentId);
			if(subId!=null&&subId.length()>0&&!subId.equalsIgnoreCase("all")){
				String genre = (String) m_categoryIdMap.get(subId);
				ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(Integer.parseInt(subId)));
				if(clips!=null){
					if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){
						for(int l=0;l<clips.size();l++){
							ClipMinimal cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
							boolean isAdd=false;
							if(searchOption.equalsIgnoreCase("artist")){
								if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}		
							}else if(searchOption.equalsIgnoreCase("album")){
								if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}else if(searchOption.equalsIgnoreCase("song")){
								if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}else if(searchOption.equalsIgnoreCase("vcode")){
								if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}
							if(isAdd){
								count++;
								if(count>=from&&count<=to){
									System.out.println("count="+count);
									String str = parent+";"+genre+";"+subId+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
									clipDetails.add(str);			
								}
							}
							if(clipDetails.size()>=to-from){
								return  (String[])clipDetails.toArray(new String[0]);
							}
						}
					}else{
						if(count+clips.size()<from){
							count=count+clips.size();
						}
						else if(count+clips.size()>=from){
							for(int l=0;l<clips.size();l++){
								count++;
								if(count>=from&&count<=to){
									System.out.println("count="+count);
									ClipMinimal cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									String str = parent+";"+genre+";"+subId+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
									clipDetails.add(str);
								}

								if(clipDetails.size()>=to-from){
									return  (String[])clipDetails.toArray(new String[0]);
								}
							}
						}
					}
				}
				return (String[])clipDetails.toArray(new String[0]);

			}else if(subId!=null&&subId.length()>0&&subId.equalsIgnoreCase("all")){
				ArrayList subCats = (ArrayList) m_parentCategories.get(new Integer(Integer.parseInt(parentId)));
				if(subCats!=null){
					
					for(int k=0;k<subCats.size();k++){
						int	subid = ((Integer)subCats.get(k)).intValue();
						String genre = getCategoryName(subid);
						ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(subid));
						if(clips!=null){
							if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){
								for(int l=0;l<clips.size();l++){
									ClipMinimal cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									boolean isAdd=false;
									if(searchOption.equalsIgnoreCase("artist")){
										if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}		
									}else if(searchOption.equalsIgnoreCase("album")){
										if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("song")){
										if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("vcode")){
										if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}
									if(isAdd){
										count++;
										if(count>=from&&count<=to){
											System.out.println("count="+count+"\tcm="+cm.getClipId());
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);			
										}
									}
									if(clipDetails.size()>=to-from){
										return  (String[])clipDetails.toArray(new String[0]);
									}
								}
							}else{
								if(count+clips.size()<from){
									count=count+clips.size();
								}
								else if(count+clips.size()>=from){
									for(int l=0;l<clips.size();l++){
										count++;
										if(count>=from&&count<=to){
											ClipMinimal cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);
										}

										if(clipDetails.size()>=to-from){
											return  (String[])clipDetails.toArray(new String[0]);
										}
									}
								}
							}
						}
					}
				}
				return  (String[])clipDetails.toArray(new String[0]);
			}
		}else if(parentId!=null&&parentId.length()>0&&parentId.equalsIgnoreCase("all")){
			
			Iterator iter= m_parentCategories.keySet().iterator();
			while(iter.hasNext()){
				int parId =((Integer)iter.next()).intValue();
				String parent = (String) m_categoryIdMap.get(Integer.toString(parId));
				ArrayList subCats = (ArrayList) m_parentCategories.get(new Integer(parId));
				if(subCats!=null){
					for(int k=0;k<subCats.size();k++){
						int	subid = ((Integer)subCats.get(k)).intValue();
						String genre = getCategoryName(subid);
						ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(subid));
						if(clips!=null){
							if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){

								for(int l=0;l<clips.size();l++){
									ClipMinimal cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									boolean isAdd=false;
									if(searchOption.equalsIgnoreCase("artist")){
										if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}		
									}else if(searchOption.equalsIgnoreCase("album")){
										if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("song")){
										if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("vcode")){
										if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}
									if(isAdd){
										count++;
										if(count>=from&&count<=to){
											System.out.println("count="+count+"\tcm="+cm.getClipId());
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);			
										}
									}
									if(clipDetails.size()>=to-from){
										return  (String[])clipDetails.toArray(new String[0]);
									}
								}
							}else{
								if(count+clips.size()<from){
									count=count+clips.size();
								}
								else if(count+clips.size()>=from){
									for(int l=0;l<clips.size();l++){
										count++;
										if(count>=from&&count<=to){
											ClipMinimal cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());
								
											if(cm!=null){
												System.out.println("count="+count+"\tcm="+cm.getClipId());
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);
											}
										}

										if(clipDetails.size()>=to-from){
											return  (String[])clipDetails.toArray(new String[0]);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return (String[])clipDetails.toArray(new String[0]);
	}

	public String getCategoryName(int a){
		if(m_categoryIdMap==null){
			System.out.println("Initialising Categories");
			initialiseCategoryMap();
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "543215_CALLBACK_CATEGORY");
			m_categoryIdMap.put(param.getValue(), "SEARCH");
			param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "GIFT_CATEGORY");
			m_categoryIdMap.put(param.getValue(), "GIFT");
			param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "CRICKET_CATEGORY");
			m_categoryIdMap.put(param.getValue(), "cricket");
		}

		String catId = new Integer(a).toString();
		return (String)m_categoryIdMap.get(catId);
	}

	public SubscriberStatus[] selectValidEntriesNSort(SubscriberStatus[] selections,String startDate,String endDate){
		String subId=null;
		if(selections!=null){
		subId=selections[0].subID();
		}
		ArrayList selectionFinal=new ArrayList();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SubscriberStatus tempSelection=null;
		Date setTime=null;
		String setTimeString=null;
		boolean check=true;
		try {
			Date startTime=null;
			startTime=df.parse(startDate);
			
			
			Date endTime=null;
			endTime=df.parse(endDate);
			endTime.setDate(endTime.getDate()+1);
			
			selections = sortSubscriberStatusBySetTime(selections);
			for(int count=0;count<selections.length;count++){
				tempSelection=selections[count];
				setTime=tempSelection.setTime();
				setTimeString=df.format(setTime);
				setTime=df.parse(setTimeString);
				if(setTime.after(startTime)&& setTime.before(endTime)){
					selectionFinal.add(selections[count]);
				}
				
			}
		} catch (ParseException e) {
			check=false;
			e.printStackTrace();
		}
		
		//finally{
			if (check) {
				if (selectionFinal != null && selectionFinal.size() > 0) {
					logger.info("subscriber_id="+subId+"returning selections with select count=="+selectionFinal.size() );
					return (SubscriberStatus[]) selectionFinal.toArray(new SubscriberStatus[0]);
				} else {
					logger.info("subscriber_id="+subId+"returning selections null with check==true");
					return null;
				}
			}else{
				logger.info("subscriber_id="+subId+"returning selections null with check==false");
				return null;
			}			
	//	}
	}
	
	public static SubscriberStatus[] sortSubscriberStatusBySetTime(SubscriberStatus[] settings)
	{
		if(settings == null)
			return null;

		SubscriberStatus subscriberStatus = null; 
		for (int i = 0; i < settings.length; i++)
		{
			for (int j = i; j < settings.length; j++)
			{
				if(settings[i].setTime().compareTo(settings[j].setTime()) < 0)
				{
					subscriberStatus = settings[i];
					settings[i] = settings[j];
					settings[j] = subscriberStatus;
				}
			}
		}

		return settings;
	}

	public String[] getallAirtelSubscriberSelections(String subId,String startDate,String endDate,String clipName){

		SubscriberStatus[] selections=RBTDBManager.getInstance().getAllAirtelSubscriberSelectionRecords(subId, startDate, endDate);
		selections=selectValidEntriesNSort(selections,startDate,endDate);
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd  h:mm a");
		List statusList=new ArrayList();
		int categoryId =0;
		int categoryType = 0;
		String categoryName = null;
		if(selections!=null && selections.length>0){
			ClipMinimal clip=null;
			String wave=null;
			String selns=null;
			for(int i=0;i<selections.length ;i++){
				if(selections[i]!=null){
				Categories category = (Categories)(RBTDBManager.getInstance().getCategory(selections[i].categoryID(), getCircleID(subId), 'b'));
				String catName =null;
				if(category!=null){
					catName=category.name();
				}
				if(selections[i].deSelectedBy()!=null && 
						(selections[i].deSelectedBy().equalsIgnoreCase("NA")||
						selections[i].deSelectedBy().equalsIgnoreCase("NEF")||
						selections[i].deSelectedBy().equalsIgnoreCase("RF")||
						selections[i].deSelectedBy().equalsIgnoreCase("RA"))){
						catName = catName+"/NEF";
				}
				if(category!=null){
				categoryId = category.id();
				categoryType = category.type();
				categoryName = category.name();
				}
				if(categoryName==null){
					categoryName="--";
				}
				String parentCategoryName=null;
				String rbtType=selections[i].selectionInfo();
				String buyType="buy";
				if ((categoryType == 0 || categoryType == 4))
				{
					Categories parentCategory = (Categories)(RBTDBManager.getInstance().getCategory(category.parentID()));
					if(parentCategory!=null){
					parentCategoryName=parentCategory.name();
					if(parentCategoryName==null){
						parentCategoryName="--";
					}
					selns = categoryId+"::"+categoryName+"(Album)"+"::"+parentCategoryName+"::--::"+catName+"::"+getChargeAmount(selections[i].classType())
					+"::"+buyType+"::"+selections[i].selectedBy()+"::"+df.format(selections[i].setTime())+"::"+rbtType;		 
					//logger.info("selection info="+selns);
					statusList.add(selns);
				}
				}else{
				clip =getClipRBT(selections[i].subscriberFile());
				if(clip!=null){
					wave=clip.getWavFile();
					
					
					
					boolean shudinclude=true;
					if(clipName!=null){
						if(clipName.length()>0){
							shudinclude=false;
							String name=clip.getClipName();
							if(name.toUpperCase().indexOf(clipName.toUpperCase())!=-1){
								shudinclude=true;
							}
						}
					}
					if(wave!=null)
						wave=wave.substring(4, wave.length()-4);
					
					if(shudinclude){
						String artist = clip.getArtist();
						if(artist==null||artist.equalsIgnoreCase("null")){
							artist="na";
						}
						
					
						
						if(catName!=null){
							catName=catName.toLowerCase();
							catName = catName.substring(0, 1).toUpperCase()+catName.substring(1, catName.length());
						}
						
						selns = wave+"::"+clip.getClipName()+"::"+clip.getAlbum()+"::"+artist+"::"+catName+"::"+getChargeAmount(selections[i].classType())
						+"::"+buyType+"::"+selections[i].selectedBy()+"::"+df.format(selections[i].setTime())+"::"+rbtType;		 
						//logger.info("selection info="+selns);
						statusList.add(selns);
					}
				}
			}
			}
		}
		}	
		if(statusList==null || statusList.size()==0){
			return null;
		}
		logger.info("subscriber_id="+subId+"returning selections with size"+statusList.size());
		return ((String[])statusList.toArray(new String[0]));
	}

		
	/****/ 
	
/*    public Subscriber activateADRBTSubscriber(String strSubID, String strActBy, 
            Date endDate, boolean bPrepaid, int days, String strActInfo, 
            String classType) 
    { 
        return (RBTDBManager.getInstance() 
                .activateSubscriber(strSubID, strActBy, endDate, bPrepaid, 
                                    m_activationPeriod, days, strActInfo, 
                                    classType, m_useSubscriptionManager, false, 1)); 
    } */ 
    
    public Subscriber trialActivateSubscriber(String strSubID,
            String strActBy, Date endDate, boolean bPrepaid, int days,
            String strActInfo, String classType, String selClassType,
            String subscriptionType, String circleId)
    {
        return (RBTDBManager.getInstance()
                .trialActivateSubscriber(strSubID, strActBy, endDate, bPrepaid,
                                         m_activationPeriod, days, strActInfo,
                                         classType, m_useSubscriptionManager,
                                         selClassType, subscriptionType, circleId));
    }

    private String deactivateSubscriber(String strSubID, String deactivate,
            Date date, boolean delSelections, boolean bSendHLR, boolean checkSubClass)
    {
        return (RBTDBManager.getInstance()
                .deactivateSubscriber(strSubID, deactivate, date,
                                      delSelections, bSendHLR,
                                      m_useSubscriptionManager, checkSubClass));
    }

/*    private SubscriberStatus[] getSubscriberRecords(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getSubscriberRecords(strSubID, "GUI", m_useSubscriptionManager));
    }*/

    private SubscriberStatus[] getAllSubscriberSelectionRecords(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getAllSubscriberSelectionRecords(strSubID, "GUI"));
    }

    private SubscriberStatus[] getSubscriberDeactiveRecords(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getSubscriberDeactiveRecords(strSubID, "GUI"));
    }

    public SubscriberStatus smSubscriberSelections(String subID, String callID, int st)
    {
        return (RBTDBManager.getInstance()
                .smSubscriberSelections(subID, callID, st));
    }

	public String addSMSSelection(String strSubID, String strSong,
            boolean bPrepaid, String strActBy, boolean bIgnoreAct,
            boolean bFreeAct, boolean bFreeSel, boolean bReact,
            int trialPeriod, String strActInfo, String classType,
            String subClass, String subscriptionType, String circleID)
    {
        logger.info("RBT::subscriber "
                + strSubID + " selected song " + strSong + " whether prepaid "
                + bPrepaid + " activated by " + strActBy + " circleID " + circleID);
        strSubID = subID(strSubID);
        Subscriber subscriber = getSubscriber(strSubID);
        ClipMinimal clip = null;
       if (strSong != null && !strSong.equals("null"))
        {
            clip = getClipRBT(strSong);
            if(clip == null)
            	return "failure::Clip is null";
            else
            if (clip != null && clip.getClassType().startsWith("TRIAL"))
            {
                classType = clip.getClassType();
            }
        }

        if (!isSubActive(subscriber))
        {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            String sub = null;

            if (bFreeAct)
                sub = activate(strSubID, bPrepaid, strActBy, endDate,
                               trialPeriod, strActInfo, subClass, classType,
                               subscriptionType, circleID);
            else
                sub = activate(strSubID, bPrepaid, strActBy, endDate, 0,
                               strActInfo, subClass, classType,
                               subscriptionType, circleID);

            if (sub != null)
                return sub;
        }
        else if (bIgnoreAct)
        {
            logger.info("RBT::subscriber "
                    + strSubID + " already active");
            return "Subscriber already exists-" + strSubID;
        }
        else
        {
            if (bReact && !bFreeAct)
            {
                reactivateSubscriber(strSubID, bPrepaid, false, strActBy,
                                     strActInfo);
            }
        }

        subscriber = getSubscriber(strSubID);
        String subYes = null;
        if (subscriber != null)
            subYes = subscriber.subYes();

        boolean changeSubType = false;
        if (bPrepaid != subscriber.prepaidYes())
            changeSubType = true;

        if (subscriber == null)
            return null;
        boolean OptIn = false; 
        if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                OptIn = true; 

        if (bFreeSel){
            String ret =  addSelections(strSubID, null, bPrepaid, changeSubType,
                                  m_smsPromotionCategoryID, strSong, null, 1,
                                  trialPeriod, strActBy, strActInfo, 0, 2359,
                                  classType, null, null, subYes, subscriber
                                          .maxSelections(), subscriber
                                          .subscriptionClass(), OptIn, subscriber,null);
            return ret;
        }
        else{
            String ret =  addSelections(strSubID, null, bPrepaid, changeSubType,
                                  m_smsPromotionCategoryID, strSong, null, 1,
                                  0, strActBy, strActInfo, 0, 2359, classType,
                                  null, null, subYes, subscriber
                                          .maxSelections(), subscriber
                                          .subscriptionClass(), OptIn, subscriber,null);
            return ret;
        }

    }

    private boolean isCorpSub(String strSubID)
    {
        return (isSubAlreadyActiveOnStatus(strSubID, null, 0));
    }

    private boolean isSubAlreadyActiveOnStatus(String strSubID,
            String callerID, int status)
    {
        SubscriberStatus subStatus = RBTDBManager.getInstance()
                .getActiveSubscriberRecord(strSubID, callerID, status, 0, 2359);

        if (subStatus != null)
            return true;

        return false;
    }
    public String addCorpSelections(String strSubID, String strCallerID,
            boolean bPrepaid, boolean changeSubType, int categoryID,
            String songName, Date endDate, int status, int trialPeriod,
            String strSelectedBy, String strSelectionInfo, int FromTime,
            int ToTime, String classType, String mode, String type,
            String subYes, int maxSelections, String subClass, boolean OptIn, Subscriber subscriber,String selInterval)
    {
		 if(songName.equalsIgnoreCase("null"))
  	                         return null;
        if (endDate == null)
        {
            //Calendar endCal = Calendar.getInstance();
            //endCal.set(2037, 0, 1);
            //endDate = endCal.getTime();
			endDate = m_endDate;
        }
        
        if(FromTime <= 23)
        	FromTime = FromTime * 100;
        if(ToTime <= 23)
        	ToTime = ToTime * 100 + 59;
        
        //if(m_NAVCategoryIDs != null && m_NAVCategoryIDs.contains(""+categoryID)) 
          //  status = 85; 
        
       if (strCallerID == null && m_corpChangeSelectionBlock
                && isCorpSub(strSubID) &&status==1)
        {
        	
            return ("corp");
        }

         if (categoryID !=1 && !RBTDBManager.getInstance().isSelectionAllowed(subscriber,strCallerID)) 
            return "ADRBT-Block"; 
        else
        {
            if (FromTime <= ToTime)
            {
            	System.out.println(" in sub unsub from < to ");
                SubscriptionClass sClass = getSubscriptionClass(subClass);
                if (sClass != null && sClass.getFreeSelections() > 0
                        && maxSelections < sClass.getFreeSelections())
                    classType = "FREE";
                RBTDBManager dbManager = RBTDBManager.getInstance();
                boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();
                if(inLoop && !dbManager.moreSelectionsAllowed(strSubID, strCallerID))
                	return null;
                
                dbManager.addSubscriberSelections(strSubID, strCallerID, categoryID, songName,
						null, null, endDate, status, strSelectedBy, strSelectionInfo, trialPeriod,
						bPrepaid, changeSubType, m_messagePath, FromTime, ToTime, classType,
						m_useSubscriptionManager, true, mode, type, subYes, "ESIA", true, OptIn,
						inLoop, sClass.getSubscriptionClass(), subscriber,selInterval);
            }
        }

        return null;
    }
    public String addSelections(String strSubID, String strCallerID,
            boolean bPrepaid, boolean changeSubType, int categoryID,
            String songName, Date endDate, int status, int trialPeriod,
            String strSelectedBy, String strSelectionInfo, int FromTime,
            int ToTime, String classType, String mode, String type,
            String subYes, int maxSelections, String subClass, boolean OptIn, Subscriber subscriber,String selInterval)
    {
		 if(songName.equalsIgnoreCase("null"))
  	                         return null;
        if (endDate == null)
        {
            //Calendar endCal = Calendar.getInstance();
            //endCal.set(2037, 0, 1);
            //endDate = endCal.getTime();
			endDate = m_endDate;
        }
        
        if(FromTime <= 23)
        	FromTime = FromTime * 100;
        if(ToTime <= 23)
        	ToTime = ToTime * 100 + 59;
        
        //if(m_NAVCategoryIDs != null && m_NAVCategoryIDs.contains(""+categoryID)) 
          //  status = 85; 
        
        if (strCallerID == null && m_corpChangeSelectionBlock
                && isCorpSub(strSubID))
        {
            return ("corp");
        }
        else if (categoryID !=1 && !RBTDBManager.getInstance().isSelectionAllowed(subscriber,strCallerID)) 
            return "ADRBT-Block"; 
        else
        {
            if (FromTime <= ToTime)
            {
            	System.out.println(" in sub unsub from < to ");
                SubscriptionClass sClass = null;
				if(subClass != null)
						sClass = getSubscriptionClass(subClass);
                if (sClass != null && sClass.getFreeSelections() > 0
                        && maxSelections < sClass.getFreeSelections())
                    classType = "FREE";
                RBTDBManager dbManager = RBTDBManager.getInstance();
                boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();
                if(inLoop && !dbManager.moreSelectionsAllowed(strSubID, strCallerID))
                	return null;
                
                dbManager.addSubscriberSelections(strSubID, strCallerID, categoryID, songName,
						null, null, endDate, status, strSelectedBy, strSelectionInfo, trialPeriod,
						bPrepaid, changeSubType, m_messagePath, FromTime, ToTime, classType,
						m_useSubscriptionManager, true, mode, type, subYes, "ESIA", true, OptIn,
						inLoop, subscriber.subscriptionClass(), subscriber,selInterval);
            }
        }

        return null;
    }

    public String[] getSubscriberDeactivatedRecords(String strSubID)
    {
        SubscriberStatus[] subscriberStatus = null;
        Categories category = null;
        ClipMinimal clip = null;
        String callerid, status;
        List statusList = new ArrayList();
// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
        subscriberStatus = getSubscriberDeactiveRecords(strSubID);
        if (subscriberStatus == null
                || (subscriberStatus.length == 1 && subscriberStatus[0]
                        .status() == 90))
            return null;
//        Subscriber subscriber = getSubscriber(strSubID);
        for (int i = 0; i < subscriberStatus.length; i++)
        {
            int st = subscriberStatus[i].status();
            if (st == 90)
                continue;
            callerid = subscriberStatus[i].callerID();

            String circleID = getCircleID(subscriberStatus[i].subID());
            char prepaidYes = 'n';
            if(subscriberStatus[i].prepaidYes())
            	prepaidYes = 'y';
            category = getCategory(subscriberStatus[i].categoryID(), circleID, prepaidYes);
            
            clip = getClipRBT(subscriberStatus[i].subscriberFile());
//            Date nextCharge = subscriberStatus[i].nextChargingDate();
//            Date starttime = subscriberStatus[i].startTime();
//            Date endtime = subscriberStatus[i].endTime();
            String selStatus = subscriberStatus[i].selStatus();
            String selClassType = subscriberStatus[i].classType();
            String selInfo = "-"; 
            String copyInfo = getCopyInfo(subscriberStatus[i].selectionInfo()); 
            if(copyInfo != null) 
                selInfo = "COPY:"+copyInfo; 
            String temp = null;
            if (category != null)
            {
                if (category.type() == SHUFFLE || category.type() == RECORD)
                {
                    temp = category.name() + "," + " " + "," + "-";
                }
                else
                {
                    temp = category.name() + "," + " " + "," + "-";
                    if (clip != null)
                    {
                        String promoID = ((clip.getPromoID() == null) ? "-" : clip
                                .getPromoID());
                        temp = category.name() + "," + clip.getClipName() + ","
                                + promoID.replaceAll(",", ";");
                    }
                }
                String selectionStatus = " Deactive ";
                boolean submanager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");
                if (submanager == true)
                {
                    if (selStatus != null
                            && (selStatus.equalsIgnoreCase("D") || selStatus
                                    .equalsIgnoreCase("P") || selStatus.equalsIgnoreCase("F")))
                    {
                        selectionStatus = "Deactivation Pending";
                    }
                    else if (selStatus != null
                            && selStatus
                                    .equalsIgnoreCase("F"))
                    {
                        selectionStatus = "Deactivation Error";
                    }
                    else
                    {
                        selectionStatus = "Deactive";
                    }
                }
                if (callerid == null && st == 0)
                    status = "CORPORATE" + "," + temp + "," + selClassType
                            + "," + df.format(subscriberStatus[i].endTime())
                            + "," + selectionStatus;
                else if (callerid == null)
                    status = "ALL" + "," + temp + "," + selClassType + ","
                            + df.format(subscriberStatus[i].endTime()) + ","
                            + selectionStatus;
                else
                    status = callerid + "," + temp + "," + selClassType + ","
                            + df.format(subscriberStatus[i].endTime()) + ","
                            + selectionStatus;
                status += ","+selInfo;
                statusList.add(status);
            }
        }
        return (String[]) statusList.toArray(new String[0]);
    }

    public ViralBlackListTable insertViralBlackList(String subscriberID,
            String subType)
    {
        //Calendar endCal = Calendar.getInstance();
        //endCal.set(2037, 0, 1);
        //Date endDate = endCal.getTime();
		Date endDate = m_endDate;

        return (RBTDBManager.getInstance()
                .insertViralBlackList(subscriberID, null, endDate, subType));
    }

    public boolean removeViralBlackList(String subscriberID, String subType)
    {
        return (RBTDBManager.getInstance()
                .removeViralBlackList(subscriberID, subType));
    }

    public ViralBlackListTable getViralBlackList(String subscriberID,
            String subType)
    {
        return (RBTDBManager.getInstance()
                .getViralBlackList(subscriberID, subType));
    }

    public boolean addBlackListFile(String strFile, String subType)
    {
        FileReader fr = null;
        BufferedReader br = null;

        try
        {
            fr = new FileReader(m_filePath + File.separator + strFile);
            br = new BufferedReader(fr);

            String strSubID;
            String line = br.readLine();
            while (line != null)
            {
                line = line.trim();
                strSubID = line;
                if (strSubID != null)
                {
                    if (isValidSub(strSubID).equals("success"))
                        insertViralBlackList(strSubID, subType);
                }
                line = br.readLine();
            }
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
            }
            catch (Exception e)
            {
            }

        }
        return true;
    }

    public boolean removeBlackListFile(String strFile, String subType)
    {
        FileReader fr = null;
        BufferedReader br = null;

        try
        {
            fr = new FileReader(m_filePath + File.separator + strFile);
            br = new BufferedReader(fr);

            String strSubID;
            String line = br.readLine();
            while (line != null)
            {
                line = line.trim();
                strSubID = line;
                if (strSubID != null)
                {
                    //if (isValidSub(strSubID).equals("success"))
                        removeViralBlackList(strSubID, subType);
                }
                line = br.readLine();
            }
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
            }
            catch (Exception e)
            {

            }

        }
        return true;
    }

    public File ViewBlackListFile(String strFile, String subType)
    {
        FileReader fr = null;
        FileWriter fw = null;
        BufferedReader br = null;
        File statusFile = null;

        StringBuffer success = null;
        StringBuffer failure = null;

        try
        {
            fr = new FileReader(m_filePath + File.separator + strFile);
            br = new BufferedReader(fr);

            String strSubID;
            success = new StringBuffer();
            failure = new StringBuffer();

            String line = br.readLine();
            success.append("Started... \n\n");

            while (line != null)
            {
                line = line.trim();
                strSubID = line;
                if (strSubID != null)
                {
                    //if (isValidSub(strSubID).equals("success"))
                    //{
                        ViralBlackListTable viralBlackList = getViralBlackList(
                                                                               strSubID,
                                                                               subType);
                        if (viralBlackList == null)
                            failure.append(strSubID + " is not a " + subType
                                    + " blacklist subscriber.\n");
                        else
                        {
                            success.append(strSubID);
                            success.append(",");
                            success.append(new SimpleDateFormat(
                                    "yyyy/MM/dd HH:mm:ss")
                                    .format(viralBlackList.startTime()));
                            success.append("\n");
                        }
                    //}
                    /*else
                    {
                        failure.append(strSubID + " is not a valid " + subType
                                + " subscriber.\n");

                    }*/
                }
                line = br.readLine();
            }
            statusFile = new File(m_filePath
                    + File.separator
                    + "BlackList-"
                    + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
                            .getInstance().getTime()) + ".txt");
            fw = new FileWriter(statusFile);
            fw.write(success.toString());
            fw.write(failure.toString());

        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
                fw.close();
            }
            catch (Exception e)
            {

            }
        }
        return statusFile;
    }

    public boolean isSubEligible(Subscriber subscriber)
    {
        if (subscriber != null)
        {
            if (RBTDBManager.getInstance().isSubDeactive(subscriber))
            {
                long difference = (System.currentTimeMillis() - subscriber
                        .endDate().getTime())
                        / (1000 * 60 * 60 * 24);
                long timePeriod = (new Integer(m_deactivationPeriod))
                        .longValue();
                if (difference <= timePeriod)
                    return false;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    public String subID(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .subID(strSubID));
    }

    public String isValidSub(String strSubID)
    {
    	strSubID = subID(strSubID);
        if (m_processBlackListTypes
                && RBTDBManager.getInstance()
                        .isTotalBlackListSub(strSubID))
        {
            return "blacklisted";
        }
    /*  Subscriber sub = getSubscriber(strSubID);
        if(sub!= null && sub.subYes() != null && sub.subYes().equals("Z"))
        	return "suspended";*/
        return (RBTDBManager.getInstance()
                .isValidPrefix(strSubID)) ? "success" : "failure";
    }
    
    public String isValidSub(String strSubID, Subscriber sub)
    {
    	if (m_processBlackListTypes
    			&& RBTDBManager.getInstance()
    			.isTotalBlackListSub(strSubID))
    	{
    		return "blacklisted";
    	}
    	if(sub!= null && sub.subYes() != null && sub.subYes().equals("Z"))
    		return "suspended";
    	return (RBTDBManager.getInstance()
    			.isValidPrefix(strSubID)) ? "success" : "failure";
    }
	 
    public boolean isValidPrefix(String strSubID)
    {
    	return (RBTDBManager.getInstance()
    			.isValidPrefix(strSubID));
    }
	 

    public boolean changeSmsText(String promoId, String smsDate,
            String smsNewText, String smsSent)
    {
        return (RBTDBManager.getInstance()
                .changeSmsText(promoId, smsDate, smsNewText, smsSent));
    }

    public BulkPromoSMS[] getDistinctPromoIds()
    {
        return (RBTDBManager.getInstance()
                .getDistinctPromoIds());
    }

    public BulkPromoSMS[] getConditionsForDistinctPromoId(String promoId)
    {
        return (RBTDBManager.getInstance()
                .getAllPromoIDSMSes(promoId));
    }

    public SubscriptionClass[] getSubscriptionClassesGUI()
    {
        return CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClassesGUI(true).toArray(new SubscriptionClass[0]);
    }

    public ChargeClass[] getChargeClassesGUI()
    {
        return CacheManagerUtil.getChargeClassCacheManager().getChargeClassesGUI(true).toArray(new ChargeClass[0]);
    }

    public String getLogin(String user, String passwd)
    {
    	String ret = null;
        if(m_userPwd != null && m_userPwd.containsKey(user.toLowerCase().trim()+"_"+passwd.trim()))
        { 
            ret = (String) m_userPwd.get(user.toLowerCase().trim()+"_"+passwd.trim()); 
        }
        return ret;

    }

    public String freeActivationClass()
    {
        String subscriptionClass = null;
        String freeActSelClasses = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "FREE_ACT_SUB_SEL_CLASS", null);
		if (freeActSelClasses == null)
			return "DEFAULT";
        StringTokenizer st = new StringTokenizer(freeActSelClasses, ",");
        if (st != null && st.hasMoreTokens())
        {
            subscriptionClass = st.nextToken();
        }

        return subscriptionClass;
    }

    private SubscriptionClass getSubscriptionClass(String sub)
    {
        return CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(sub);
    }

    public String getActivationClassdetails(String subscriptionClass)
    {
        String activationClassDetails = null;
        if (subscriptionClass == null)
            return null;

        SubscriptionClass sClass = getSubscriptionClass(subscriptionClass);

        if (sClass == null)
            return null;

        activationClassDetails = subscriptionClass+": " + _currencyStr + sClass.getSubscriptionAmount();
        if (chargePeriodMonthMap != null
                && chargePeriodMonthMap.containsKey(sClass.getSubscriptionPeriod()
                        .substring(0, 1)))
        {
            activationClassDetails = activationClassDetails + "(";
            if (sClass.getSubscriptionPeriod().substring(0, 1).equals("D")
                    || sClass.getSubscriptionPeriod().substring(0, 1).equals("M"))
                activationClassDetails = activationClassDetails + "First "
                        + sClass.getSubscriptionPeriod().substring(1) + " ";
            activationClassDetails = activationClassDetails
                    + (String) chargePeriodMonthMap.get(sClass
                            .getSubscriptionPeriod().substring(0, 1)) + ")";
        }
        if (sClass.subscriptionRenewal())
        {
            activationClassDetails = activationClassDetails + "/ " + _currencyStr
					+ sClass.getRenewalAmount();
            if (chargePeriodMonthMap != null
                    && chargePeriodMonthMap.containsKey(sClass.getRenewalPeriod()
                            .substring(0, 1)))
            {
                activationClassDetails = activationClassDetails + "(";
                if (sClass.getRenewalPeriod().substring(0, 1).equals("D")
                        || sClass.getRenewalPeriod().substring(0, 1).equals("M"))
                    activationClassDetails = activationClassDetails + "Every "
                            + sClass.getRenewalPeriod().substring(1) + " ";
                activationClassDetails = activationClassDetails
                        + (String) chargePeriodMonthMap.get(sClass
                                .getRenewalPeriod().substring(0, 1)) + ")";
            }
        }
        else
        {
            activationClassDetails = activationClassDetails + " / No Renewal";
        }

        return activationClassDetails;
    }

    public String freeSelectionClass()
    {
        String chargeClass = null;
        String freeActSelClasses = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "FREE_ACT_SUB_SEL_CLASS", null);
        StringTokenizer st = new StringTokenizer(freeActSelClasses, ",");
        if (st != null && st.hasMoreTokens())
        {
            st.nextToken();
            if (st.hasMoreTokens())
            {
                chargeClass = st.nextToken();
            }
        }

        return chargeClass;
    }

    public String getChargeClassDetails(String chargeClass)
    {
        String chargsClassDetails = null;
        if (chargeClass == null)
            return null;

        ChargeClass cClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
        if (cClass == null)
            return null;

        chargsClassDetails = chargeClass+": " + _currencyStr + cClass.getAmount();
        if (chargePeriodMonthMap != null
                && chargePeriodMonthMap.containsKey(cClass.getSelectionPeriod()
                        .substring(0, 1)))
        {
            chargsClassDetails = chargsClassDetails + "(";
            if (cClass.getSelectionPeriod().substring(0, 1).equals("D")
                    || cClass.getSelectionPeriod().substring(0, 1).equals("M"))
                chargsClassDetails = chargsClassDetails + "First "
                        + cClass.getSelectionPeriod().substring(1) + " ";
            chargsClassDetails = chargsClassDetails
                    + (String) chargePeriodMonthMap.get(cClass
                            .getSelectionPeriod().substring(0, 1)) + ")";
        }
        chargsClassDetails = chargsClassDetails + "/ " + _currencyStr + cClass.getRenewalAmount();
        if (chargePeriodMonthMap != null
                && chargePeriodMonthMap.containsKey(cClass.getRenewalPeriod()
                        .substring(0, 1)))
        {
            chargsClassDetails = chargsClassDetails + "(";
            if (cClass.getRenewalPeriod().substring(0, 1).equals("D")
                    || cClass.getRenewalPeriod().substring(0, 1).equals("M"))
                chargsClassDetails = chargsClassDetails + "Every "
                        + cClass.getRenewalPeriod().substring(1) + " ";
            chargsClassDetails = chargsClassDetails
                    + (String) chargePeriodMonthMap.get(cClass.getRenewalPeriod()
                            .substring(0, 1)) + ")";
        }

        return chargsClassDetails;
    }

    private void setActivationInfo(String strSubID, String actInfo)
    {
    	RBTDBManager.getInstance()
                .setActivationInfo(strSubID, actInfo);
    }

    public ClipMinimal getClipPromoID(String promotionID)
    {
        return (RBTDBManager.getInstance().getClipMinimalPromoID(promotionID,false));
    }

    public String[] getSmsHistory(String subscriberID)
    {
		String _method = "getSmsHistory";
		String status = null;
		// String type = null;
		String request = null;
		String response = null;
		String time = null;
		List statusList = new ArrayList();

		String sdrFilePath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SDR_WORKING_DIR", ".");
		String sdrDirName = "smssdr";

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		Date currDate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String strDate = sdf.format(currDate);
		String strYear = strDate.substring(0, 4);
		String strMonth = strDate.substring(4, 6);
		String dirName = sdrFilePath + File.separator + sdrDirName + File.separator + strYear
				+ File.separator + strMonth;
		// logger.info("****** dirName. " + dirName);
		File sdrDirectoryLastMonth = new File(dirName);
		File[] listLastMonth = null;
		if(sdrDirectoryLastMonth.exists())
			listLastMonth = sdrDirectoryLastMonth.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name) {
					Calendar cal = Calendar.getInstance();
					Date endDate = cal.getTime();
					cal.add(Calendar.DATE, -30);
					Date startDate = cal.getTime();
					File sdrFile = new File(dir.getAbsolutePath() + File.separator + name);
					if(sdrFile.isDirectory())
						return false;
					Date fileDate = new Date(sdrFile.lastModified());
					if(fileDate.equals(startDate) || fileDate.equals(endDate)
							|| (fileDate.after(startDate) && fileDate.before(endDate)))
						return true;
					else
						return false;
				}
			});

		File sdrdirectory = new File(sdrFilePath + File.separator + sdrDirName);

		logger.info("****** sdrdirectory. " + sdrdirectory);
		File[] listCurrMonth = sdrdirectory.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name) {
				Calendar cal = Calendar.getInstance();
				Date endDate = cal.getTime();
				cal.add(Calendar.DATE, -30);
				Date startDate = cal.getTime();
				File sdrFile = new File(dir.getAbsolutePath() + File.separator + name);
				if(sdrFile.isDirectory())
					return false;
				Date fileDate = new Date(sdrFile.lastModified());
				if(fileDate.equals(startDate) || fileDate.equals(endDate)
						|| (fileDate.after(startDate) && fileDate.before(endDate)))
					return true;
				else
					return false;
			}
		});

		File[] totalFiles = null;
		try {
			if(listLastMonth == null)
				totalFiles = listCurrMonth;
			else if(listCurrMonth == null)
				totalFiles = listLastMonth;
			else {
				int totLength = listCurrMonth.length + listLastMonth.length;
				totalFiles = new File[totLength];
				for(int count = 0; count < listLastMonth.length; count++)
					totalFiles[count] = listLastMonth[count];
				for(int count = 0; count < listLastMonth.length; count++)
					totalFiles[listLastMonth.length + count] = listCurrMonth[count];
			}
			logger.info("RBT::listCurrMonth. "
				+ (listCurrMonth == null ? ("listCurrMonth is null ->"
				+ (listCurrMonth == null)) : "listCurrMonth.length ->" + listCurrMonth.length));
			logger.info("RBT::listLastMonth. "
				+ (listLastMonth == null ? ("listLastMonth is null ->"
				+ (listLastMonth == null)) : "listLastMonth.length ->" + listLastMonth.length));
			logger.info("****** totalFiles. "
					+ (totalFiles == null ? ("totalFiles is null ->" + (totalFiles == null))
							: "totalFiles.length ->" + totalFiles.length));
		/*	if(totalFiles == null || totalFiles.length == 0) {
				return null;
			}*/
		}
		catch (Exception e) {
			logger.error("", e);
		}
		for(int i = 0; i < totalFiles.length; i++) {
			try {

				// String filename = totalFiles[i].getName();
				logger.info("****** name " + totalFiles[i].getCanonicalPath());
				LineNumberReader fin = new LineNumberReader(new FileReader(totalFiles[i]));
				String str;
				while ((str = fin.readLine()) != null) {
					if(str.indexOf(subscriberID) == -1)
						continue;
					else {
						if(str.startsWith("SMS-REQUEST")) {
							if(str.indexOf("RBT") != -1 && str.indexOf("NA") != -1
									&& str.indexOf("RBT") < str.indexOf("NA"))
								request = str.substring(str.indexOf("RBT") + 4,
										str.indexOf("NA") - 1);
							else
								continue;
							if((str.substring(str.lastIndexOf(",") + 1)).length() > 14)
								time = str.substring(str.lastIndexOf(",") + 1,
										str.lastIndexOf(",") + 15);
							else
								continue;
							status = "SMS-REQUEST," + request + "," + time;
						}
						else if(str.startsWith("SMS-RESPONSE")) {
							java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
							String IPAddress = ip.getHostAddress();
							if(str.indexOf(IPAddress) != -1
									&& str.indexOf(IPAddress) < str.lastIndexOf(","))
								response = str.substring(str.indexOf(IPAddress) + 12, str
										.lastIndexOf(","));
							else
								continue;
							if((str.substring(str.lastIndexOf(",") + 1)).length() > 14)
								time = str.substring(str.lastIndexOf(",") + 1,
										str.lastIndexOf(",") + 15);
							else
								continue;
							status = "SMS-RESPONSE," + response + "," + time;
						}
						else
							continue;
						statusList.add(status);
					}
				}
			}
			catch (FileNotFoundException fne) {
				System.out.println("Caught File not found exception" + fne);
				fne.printStackTrace();
			}
			catch (Exception e) {
				System.out.println("Exception occured while processing " + e);
				e.printStackTrace();
			}

		}
		//statusList = getSmsHistoryNewTrans(subscriberID, statusList);
		
		return (String[]) statusList.toArray(new String[0]);
	}

    public String[] getSmsHistoryNewTrans(String subscriberID)
	{
    	List statusList = new ArrayList();
    	String _method = "getSmsHistoryNewTrans";
		logger.info("RBT :: inside getSmsHistoryNewTrans()");
		String requestStatus = null;
		String responseStatus = null;
		// String type = null;
		String request = null;
		String response = null;
		String time = null;
		SortedMap sortedMap = new TreeMap();
		String sdrFilePath = null;
		Parameters sdrPathParam = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SDR_WORKING_DIR");
		if(sdrPathParam != null && sdrPathParam.getValue() != null)
			sdrFilePath = sdrPathParam.getValue().trim();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		Date currDate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String strDate = sdf.format(currDate);
		String strYear = strDate.substring(0, 4);
		String strMonth = strDate.substring(4, 6);
		String dirName = sdrFilePath + File.separator + strYear
		+ File.separator + strMonth;
		// logger.info("****** dirName. " + dirName);
		File sdrDirectoryLastMonth = new File(dirName);
		File[] listLastMonth = null;
		if(sdrDirectoryLastMonth.exists())
			listLastMonth = sdrDirectoryLastMonth.listFiles(new FilenameFilter()
					{
				public boolean accept(File dir, String name) {
					Calendar cal = Calendar.getInstance();
					Date endDate = cal.getTime();
					cal.add(Calendar.DATE, -30);
					Date startDate = cal.getTime();
					File sdrFile = new File(dir.getAbsolutePath() + File.separator + name);
					if(sdrFile.isDirectory())
						return false;
					Date fileDate = new Date(sdrFile.lastModified());
					if(fileDate.equals(startDate) || fileDate.equals(endDate)
							|| (fileDate.after(startDate) && fileDate.before(endDate)))
						return true;
					else
						return false;
				}
					});
		
		File sdrdirectory = new File(sdrFilePath);
		
		logger.info("****** sdrdirectory. " + sdrdirectory);
		File[] listCurrMonth = sdrdirectory.listFiles(new FilenameFilter()
				{
			public boolean accept(File dir, String name) {
				Calendar cal = Calendar.getInstance();
				Date endDate = cal.getTime();
				cal.add(Calendar.DATE, -30);
				Date startDate = cal.getTime();
				File sdrFile = new File(dir.getAbsolutePath() + File.separator + name);
				if(sdrFile.isDirectory())
					return false;
				Date fileDate = new Date(sdrFile.lastModified());
				if(fileDate.equals(startDate) || fileDate.equals(endDate)
						|| (fileDate.after(startDate) && fileDate.before(endDate)))
					return true;
				else
					return false;
			}
				});
		
		File[] totalFiles = null;
		try {
			if(listLastMonth == null)
				totalFiles = listCurrMonth;
			else if(listCurrMonth == null)
				totalFiles = listLastMonth;
			else {
				int totLength = listCurrMonth.length + listLastMonth.length;
				totalFiles = new File[totLength];
				for(int count = 0; count < listLastMonth.length; count++)
					totalFiles[count] = listLastMonth[count];
				for(int count = 0; count < listLastMonth.length; count++)
					totalFiles[listLastMonth.length + count] = listCurrMonth[count];
			}
			logger.info("RBT::listCurrMonth. "
					+ (listCurrMonth == null ? ("listCurrMonth is null ->"
							+ (listCurrMonth == null)) : "listCurrMonth.length ->" + listCurrMonth.length));
			logger.info("RBT::listLastMonth. "
					+ (listLastMonth == null ? ("listLastMonth is null ->"
							+ (listLastMonth == null)) : "listLastMonth.length ->" + listLastMonth.length));
			logger.info("****** totalFiles. "
					+ (totalFiles == null ? ("totalFiles is null ->" + (totalFiles == null))
							: "totalFiles.length ->" + totalFiles.length));
			if((totalFiles == null || totalFiles.length == 0) && (statusList == null || statusList.size() == 0)) {
				return null;
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		
		for(int i = 0; i < totalFiles.length; i++) {
			try {
				
				// String filename = totalFiles[i].getName();
				logger.info("****** name " + totalFiles[i].getCanonicalPath());
				LineNumberReader fin = new LineNumberReader(new FileReader(totalFiles[i]));
				String str;
				while ((str = fin.readLine()) != null) {
					
					String token = null;
					String token1 = null;
					SimpleDateFormat sdfStarttime = new SimpleDateFormat("yyyyMMddHHmmss");
					String requestTime = null;
					String responseTime = null;
					boolean subscriberFound = true;
					if(str.indexOf("MSISDN=") != -1 || str.indexOf("msisdn=") != -1 || str.indexOf("SUB_ID") != -1)
					{
						StringTokenizer stk = new StringTokenizer(str,"|");
						if(stk.hasMoreTokens())
						{
							token = stk.nextToken();
							StringTokenizer stk1 = new StringTokenizer(token,"&");
							while(stk1.hasMoreTokens())
							{
								token1 = stk1.nextToken();
								
								if(token1.startsWith("MSISDN=") || token1.startsWith("msisdn=") || token1.startsWith("SUB_ID="))
								{
									if(token1.indexOf(subscriberID) == -1)
									{
										subscriberFound = false;
										break;
									}
								}
								
								if(token1.startsWith("MESSAGE=") || token1.startsWith("SMS_TEXT=") || token1.startsWith("msg="))
								{
									request = token1.substring(token1.indexOf("=") + 1);
									URLCodec decoder = new URLCodec();
									request = decoder.decode(request, "UTF-8");
								}
								
								if(token1.startsWith("startTime"))
								{
									try
									{
										long startTime = Long.parseLong(token1.substring(token1.indexOf("=") + 1));
										Date date = new Date(startTime);
										requestTime = sdfStarttime.format(date);
									}
									catch(Exception e)
									{
										requestTime = null;
									}
								}
							}
						}
						if(!subscriberFound)
						{
							continue;
						}
						if(stk.hasMoreTokens())
						{
							response = stk.nextToken();
							response = response.replaceAll(">","&gt;");
							response = response.replaceAll("<","&lt;");
						}
						
						while(stk.hasMoreTokens())
						{
							responseTime = stk.nextToken();
						}
						if(requestTime == null)
						{
							requestTime = responseTime;
						}
						
						requestStatus = "SMS-REQUEST," + request + "," + requestTime;
						responseStatus = "SMS-RESPONSE," + response + "," + responseTime;
						
						List newStatusList = new ArrayList();
						newStatusList.add(requestStatus);
						newStatusList.add(responseStatus);
						
						sortedMap.put(requestTime,newStatusList);
						
					}
				}
			}
			catch (FileNotFoundException fne) {
				System.out.println("Caught File not found exception" + fne);
				fne.printStackTrace();
			}
			catch (Exception e) {
				System.out.println("Exception occured while processing " + e);
				e.printStackTrace();
			}
			
		}
		 
		 if(sortedMap != null && sortedMap.size() > 0)
		 {
			 Set<String> key = sortedMap.keySet();
			 for(String keys : key)
			 {
				 List tmpList = (List) sortedMap.get(keys);
				 for(int i=0;i<tmpList.size();i++)
				 {
					 statusList.add(tmpList.get(i));
				 }
			 }
		}
		return (String[]) statusList.toArray(new String[0]);
	}

    public SitePrefix[] getLocalSitePrefixes()
    {
        return CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes().toArray(new SitePrefix[0]);
    }

    public SitePrefix[] getAllSitePrefixes()
    {
        return CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix().toArray(new SitePrefix[0]);
    }

    public void updateParameter(String type, String param, String value)
    {
    	CacheManagerUtil.getParametersCacheManager().updateParameter(type,param, value);
    }
    
    public boolean updateRBTTypeAndPlayerStatus(String subscriberId, int rbtType, String playerStatus)
    {
    	boolean ret = RBTDBManager.getInstance().updateRBTTypeAndPlayerStatus(subscriberId, rbtType, playerStatus);
    	return ret;
    }
    
    public boolean checkTransIDExist(String strTransID, String type)
    {
    	boolean isExist = false;
    	if (strTransID != null)
    	{
    		TransData transData = RBTDBManager.getInstance()
	                                                     .getTransData(strTransID, type);
    		return (transData != null);
    	}
    	return isExist;
    }
    
    private boolean changeSubscriptionInternal(String subID, String initClass, String finalClass, Subscriber subscriber)
	{
		return (RBTDBManager.getInstance()
                .convertSubscriptionType(subID, initClass, finalClass, subscriber));
	}

    public boolean changeSubscription(String subID, String initClass, String finalClass)
	{
		return (changeSubscriptionInternal(subID, initClass, finalClass, null));
	}
	 
    public void addTransData(String strTransID, String strSubID, String type)
    {
    	if (strTransID != null)
    		RBTDBManager.getInstance()
    			.addTransData(strTransID, strSubID, type);
    }
    
    public boolean addPromoSelections(String strSubID, String strCallerID,
	             boolean bPrepaid, boolean changeSubType, int categoryID,
	             String songName, Date endDate, int status, int trialPeriod,
	             String strSelectedBy, String strSelectionInfo, int FromTime,
	             int ToTime, String classType, String mode, String type,
	             String subYes, int maxSelections, String subClass, boolean OptIn, Subscriber subscriber)
    {
    	
    	if (endDate == null)
    	{
    		// Calendar endCal = Calendar.getInstance();
    		// endCal.set(2037, 0, 1);
    		// endDate = endCal.getTime();
			endDate = m_endDate;
    	}
    	
    	if(FromTime <= 23)
    		FromTime = FromTime * 100;
    	if(ToTime <= 23)
    		ToTime = ToTime * 100 + 59;
	 
    	if (strCallerID == null && m_corpChangeSelectionBlock
    			&& isCorpSub(strSubID))
    	{
    		return false;
    	}
    	else if (!RBTDBManager.getInstance().isSelectionAllowed(subscriber,strCallerID)) 
            return false; 
    	else
    	{
    		if (FromTime <= ToTime)
    		{
    			SubscriptionClass sClass = getSubscriptionClass(subClass);
    			if (sClass != null && sClass.getFreeSelections() > 0
    					&& maxSelections < sClass.getFreeSelections())
    				classType = "FREE";
    			RBTDBManager dbManager = RBTDBManager.getInstance();
    			boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();
    			if(inLoop && !dbManager.moreSelectionsAllowed(strSubID, strCallerID))
    				return false;
    			return dbManager.addSubscriberSelections(strSubID, strCallerID, categoryID,
						songName, null, m_endDate, endDate, status, strSelectedBy, strSelectionInfo,
						trialPeriod, bPrepaid, changeSubType, m_messagePath, FromTime, ToTime,
						classType, m_useSubscriptionManager, true, mode, type, subYes, "ESIA",
						true, OptIn, inLoop, sClass.getSubscriptionClass(), subscriber, null);
    		}
    	}
	 
    	return false;
    }
    
  
    /**
	 * @author Sreekar 2008-02-07 Added for Airtel
	 */
    /*public HashMap processUSSD(String strSubID, String strCmd, String strVCode, String strCallerID,
			String strdestCallerID, String strChg, String strIP, String validSubResult) {
		String method = "processUSSD";
		logger.info("RBT:: strSubID: " + strSubID + ", strCmd: "
				+ strCmd + ", strVCode: " + strVCode + ", strCallerID: " + strCallerID + ", strdestCallerID: "
				+ strdestCallerID + ", strChg: " + strChg + ", validSubResult: " + validSubResult);
		if(strCmd == null)
			return getUSSDHashMap(USSD_COMMAND_INVALID);
		if (validSubResult.equalsIgnoreCase("failure") && (!strCmd.equalsIgnoreCase("copy") && !strCmd.equalsIgnoreCase("gift"))) {
			logger.info("RBT::redirecting subscriber " + strSubID);
			String url = getUSSDUrlForSub(strSubID);
			if(url != null) {
				HashMap httpParamMap = new HashMap();
				if(strSubID != null)
					httpParamMap.put("srcMsisdn", strSubID);
				if(strCmd != null)
					httpParamMap.put("cmd", strCmd);
				if(strVCode != null)
					httpParamMap.put("vcode", strVCode);
				if(strCallerID != null)
					httpParamMap.put("cbsMsisdn", strCallerID);
				if(strdestCallerID != null)
					httpParamMap.put("dstMsisdn", strdestCallerID);
				if(strChg != null)
					httpParamMap.put("chg", strChg);
				
				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(url);
				try {
					String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap, null,
							true);
					int status = Integer.parseInt(postResult.substring(0, postResult.indexOf("|")));
					return getUSSDHashMap(status);
				}
				catch (HttpException e) {
					logger.error("", e);
				}
				catch (IOException e) {
					logger.error("", e);
				}
				catch (RBTException e) {
					logger.error("", e);
				}
				catch (Exception e) {
					logger.error("", e);
				}
			} // url null if
			else {
				logger.info("RBT::url not found for " + strSubID);
				return getUSSDHashMap(USSD_SUBSCRIBER_INVALID);
			}
			return getUSSDHashMap(USSD_UNKNOWN_ERROR);
		}// end of non valid sub if
		int retStatus = USSD_UNKNOWN_ERROR;
		ClipMinimal clipMinimal = null;

		/* if (strSubID == null || strSubID.length() != 12 || !countryPrefixCheck(strSubID))
			return getUSSDHashMap(USSD_SUBSCRIBER_INVALID);
		if (strCallerID != null && (strCallerID.length() != 12 || !countryPrefixCheck(strCallerID)))
			return getUSSDHashMap(USSD_CALLER_INVALID);
		if (strdestCallerID != null
				&& (strdestCallerID.length() != 12 || !countryPrefixCheck(strdestCallerID)))
			return getUSSDHashMap(USSD_DST_CALLER_INVALID); 

		if (strSubID == null)
			return getUSSDHashMap(USSD_SUBSCRIBER_INVALID);

		if (strVCode != null) {
			if (strVCode.length() != 15)
				return getUSSDHashMap(USSD_VCODE_INVALID);

			/*String clipPromoID = RBTDBManager.getCCodeFromVCode(strVCode);
			logger.info("Sree:: vcode->" + strVCode + " ccode->" + clipPromoID);
			clipMinimal = getClipFromVCode(strVCode);
			// to know if clip is expired or not we donot need to add the selection
			if (clipMinimal == null) {
				return getUSSDHashMap(USSD_VCODE_NOT_FOUND);
			}
			else if (clipMinimal.getEndTime().before(new Date()))
				return getUSSDHashMap(USSD_VCODE_EXPIRED);
		}

		//if(!strCmd.equalsIgnoreCase("copy")) {
			if (strSubID != null)
				strSubID = subID(strSubID);
			if (strdestCallerID != null)
				strdestCallerID = subID(strdestCallerID);
			if (strCallerID != null)
				strCallerID = subID(strCallerID);
		//}

		int catID = m_chargedCategoryID;
		if (strChg != null && strChg.equals("0"))
			catID = m_freeCategoryID;

		if(strCmd.equalsIgnoreCase("subchk")) {
			retStatus = USSD_SUCCESS;
		}
		else if (strCmd.equalsIgnoreCase("set") || strCmd.equalsIgnoreCase("set_caller"))
			retStatus = processUSSDSetRequest(strSubID, strCallerID, catID, clipMinimal, "USSD",
					"USSD:" + strIP, "DEFAULT");
		else if (strCmd.equalsIgnoreCase("unsub")) {
			String deactResult = deactivateSubscriber(strSubID, "USSD", null, true, true, false);
			if (deactResult.equalsIgnoreCase("success"))
				retStatus = USSD_SUCCESS;
		}
		else if (strCmd.equalsIgnoreCase("top10")) {
			retStatus = getUSSDCategoryClipMap(m_top10Categories);
		}
		else if (strCmd.equalsIgnoreCase("freezone")) {
			retStatus = getUSSDCategoryClipMap(m_top10Categories);
		}
		else if (m_advanceRentalMap.containsKey(strCmd)) {
				//(strCmd.equalsIgnoreCase("threemonth") || strCmd.equalsIgnoreCase("sixmonth")
				//|| strCmd.equalsIgnoreCase("oneyear")) {
			retStatus = processUSSDAdvanceRental(strSubID, strCmd, strIP);
		}
		else if (strCmd.equalsIgnoreCase("gift")) {
			if (RBTDBManager.getInstance().isSubscriberActivated(
					strSubID, m_useSubscriptionManager))
				retStatus = processUSSDGift(strSubID, strdestCallerID, strVCode, strChg);
			else
				retStatus = USSD_SUBSCRIBER_INVALID;
		}
		else if (strCmd.equalsIgnoreCase("copy")) {
			retStatus = processUSSDCopy(strSubID, strdestCallerID, strCallerID, strChg);
		}
		else {
			logger.info("RBT::invalid USSD request " + strCmd);
			retStatus = USSD_COMMAND_INVALID;
		}

		return getUSSDHashMap(retStatus);
	}
    */
    /*private boolean countryPrefixCheck(String subID) {
    	boolean ret = false;
    	if(m_countryPrefix == null)
    		ret = true;
    	else {
    		for(int i = 0; i < m_countryPrefixList.size(); i++)
    			if(subID.startsWith((String)m_countryPrefixList.get(i))) {
    				ret = true;
    				break;
    			}
    	}
    	return ret;
    }*/
    
    
    
    /*private int processUSSDGift(String strSubID, String strDestCallerID, String strVCode, String strChg) {
//    	String method = "processUSSDGift";
		int retVal = USSD_SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		int prefixIndex = RBTDBManager.getInstance().getPrefixIndex();
		if (strDestCallerID == null || strDestCallerID.equalsIgnoreCase(strSubID)
				|| !(dbManager.isValidOperatorPrefix(strDestCallerID) || m_nonOnmobilePrefixList == null || m_nonOnmobilePrefixList.contains(strDestCallerID.substring(0,prefixIndex))))
			return USSD_DST_CALLER_INVALID;
		if(strVCode == null)
			return USSD_VCODE_INVALID;
		
//		String clipPromoID = RBTDBManager.getCCodeFromVCode(strVCode);
		ClipMinimal clipMinimal = getClipFromVCode(strVCode);
		if(clipMinimal == null)
			return USSD_VCODE_NOT_FOUND;
			
		insertGiftRecord(strSubID, strDestCallerID, clipMinimal.getClipId(), "USSD");

		return retVal;
	}
  */  
    
    public ViralSMSTable insertGiftRecord(String subID, String callerID, int clipID, String selectedBy) {
		return RBTDBManager.getInstance().insertViralSMSTableMap(subID, null, "GIFT", callerID,
				String.valueOf(clipID), 0, selectedBy, null, null);
    }
    
    private int getUSSDCategoryClipMap(List categoryList) {
    	return USSD_SUCCESS;
    }
    
/*    private int processUSSDAdvanceRental(String strSubID, String rentalType, String strIP) {
		int retVal = USSD_SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Subscriber subscriber = dbManager.getSubscriber(strSubID);
		if (!m_advanceRentalMap.containsKey(rentalType))
			retVal = USSD_UNKNOWN_ERROR;
		else if (!dbManager.isSubscriberDeactivated(subscriber)
				&& (m_advancePacksList.indexOf(subscriber.subscriptionClass()) != -1)) {
			retVal = USSD_UNKNOWN_ERROR;
		}
		else if (dbManager.isSubscriberActivationPending(subscriber)) {
			retVal = USSD_SUBSCRIBER_INVALID;
		}
		else {
			String rentalClass = (String) m_advanceRentalMap.get(rentalType);
			if (dbManager.isSubscriberDeactivated(subscriber)) {
				subscriber = activateSubscriber(strSubID, "USSD", null, true, 0, "USSD:" + strIP,
						rentalClass);
				if (subscriber == null)
					retVal = USSD_UNKNOWN_ERROR;
			}
			else if (!changeSubscription(strSubID, subscriber.subscriptionClass(),
					rentalClass))
				retVal = USSD_UNKNOWN_ERROR;
		}
		return retVal;
	}
  */  
/*    private String getUSSDUrlForSub(String subID) {
		if(subID == null)
			return null;
		ClipCacher.init(0);
    	String url = ClipCacher.getURL(subID.substring(2, 6));
    	RBTDBManager dbManager = RBTDBManager.getInstance();
    	Prefix userPrefix = dbManager.getPrefix(dbManager.subID(subID));
		if(url != null) {
			url = Tools.findNReplaceAll(url, "/rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			if(!userPrefix.accessAllowed()) {
				url = Tools.findNReplaceAll(url, "/rbt", m_ussdUrlReplacePackage);
				url = url + "/USSD.jsp?";
			}
			else
				url = url + "/ussd.jsp?";
			
			return url;
		}
		else
			return null;
    }
	    
    private String getAutodialUrlForSub(String subID) {
		if(subID == null)
			return null;
		 ClipCacher.init(0);
    	String url = ClipCacher.getURL(subID);
    	Prefix userPrefix = RBTDBManager.getInstance().getPrefix(subID);
		if(url != null) {
			url = Tools.findNReplaceAll(url, "/rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			if(!userPrefix.accessAllowed())
				url = Tools.findNReplaceAll(url, "/rbt", m_autodialUrlReplacePackage);
			url = url + "/autodial.jsp?";
			
			return url;
		}
		else
			return null;
    }
	    
    private String getEnvioUrlForSub(String subID) {
		if(subID == null)
			return null;
		ClipCacher.init(0);
    	String url = ClipCacher.getURL(subID);
    	Prefix userPrefix = RBTDBManager.getInstance().getPrefix(subID);
		if(url != null) {
			url = Tools.findNReplaceAll(url, "/rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			if(!userPrefix.accessAllowed())
				url = Tools.findNReplaceAll(url, "/rbt", m_envIOUrlReplacePackage);
			url = url + "/envio.jsp?";
			
			return url;
		}
		else
			return null;
    }
    
    private String getMODUrlForSub(String subID) {
		if(subID == null)
			return null;
		ClipCacher.init(0);
    	String url = ClipCacher.getURL(subID);
    	Prefix userPrefix = RBTDBManager.getInstance().getPrefix(subID);
		if(url != null) {
			url = Tools.findNReplaceAll(url, "/rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			if(!userPrefix.accessAllowed())
				url = Tools.findNReplaceAll(url, "/rbt", m_modUrlReplacePackage);
			url = url + "/mod.jsp?";
			
			return url;
		}
		else
			return null;
    }
	    
    private String getEasyChargeUrlForSub(String subID) {
		if(subID == null)
			return null;
		ClipCacher.init(0);
    	String url = ClipCacher.getURL(subID);
    	Prefix userPrefix = RBTDBManager.getInstance().getPrefix(subID);
		if(url != null) {
			url = Tools.findNReplaceAll(url, "/rbt_sms.jsp", "");
			url = Tools.findNReplaceAll(url, "?", "");
			if(!userPrefix.accessAllowed())
				url = Tools.findNReplaceAll(url, "/rbt", m_ecUrlReplacePackage);
			url = url + "/easycharge.jsp?";
			
			return url;
		}
		else
			return null;
    }*/
    
    /*private int getUSSDResultForDestCaller(String strSubID, String strDestCallerID, String strCallerID,
			String strVCode, String strCmd, String strChg) {
    	String method = "getUSSDResultForDestCaller";
		String url = getUSSDUrlForSub(strDestCallerID);
		if(url != null) {
			HashMap httpParamMap = new HashMap();
			if(strSubID != null)
				httpParamMap.put("srcMsisdn", strSubID);
			if(strCallerID != null)
				httpParamMap.put("cbsMsisdn", strCallerID);
			if(strDestCallerID != null)
				httpParamMap.put("dstMsisdn", strDestCallerID);
			if(strVCode != null)
				httpParamMap.put("vcode", strVCode);
			if(strChg != null)
				httpParamMap.put("chg", strChg);
			httpParamMap.put("cmd", strCmd);
			
			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(url);
			try {
				String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap, null,
						true);
				int status = Integer.parseInt(postResult.substring(0, postResult.indexOf("|")));
				return status;
			}
			catch (HttpException e) {
				logger.error("", e);
			}
			catch (IOException e) {
				logger.error("", e);
			}
			catch (RBTException e) {
				logger.error("", e);
			}
			catch (Exception e) {
				logger.error("", e);
			}
		} // url null if
		else {
			logger.info("RBT::url not found for " + strDestCallerID);
			return USSD_DST_CALLER_INVALID;
		}
		return USSD_UNKNOWN_ERROR;
	}*/
    
  /*  private int processUSSDCopy(String strSubID, String strDestCallerID, String strCallerID,
			String strChg) {
//    	String method = "processUSSDCopy";
    	if(strDestCallerID == null || strDestCallerID.equalsIgnoreCase(strSubID))
    		return USSD_DST_CALLER_INVALID;
    	else if (strSubID == null)
			return USSD_SUBSCRIBER_INVALID;
//		int retVal = USSD_UNKNOWN_ERROR;

		RBTDBManager dbManager = RBTDBManager.getInstance();
		RBTCommonConfig rbtCommonConfig = RBTCommonConfig.getInstance();
		String strWavCatId = dbManager.getSubscriberVcode(strDestCallerID, strSubID, rbtCommonConfig.useProxyNonCircle(), rbtCommonConfig.proxyServerPort(), 0);
		if(strWavCatId == null || strWavCatId.equalsIgnoreCase("null") || strWavCatId.length() <= 0 || strWavCatId.equalsIgnoreCase("ERROR"))
			return USSD_UNKNOWN_ERROR;
		else if ( strWavCatId.equalsIgnoreCase("DEFAULT"))
		{
			RBTDBManager.getInstance().insertViralSMSTableMap(strDestCallerID, null, "COPY",
						strSubID, null, 0, "USSD", null,null);
			return USSD_SUCCESS;
		}
		else if(strWavCatId.equalsIgnoreCase("NOT_VALID") || strWavCatId.equalsIgnoreCase("NOT_FOUND"))
			return USSD_DST_CALLER_INVALID;
		else if (strWavCatId.equalsIgnoreCase("ALBUM"))
			return USSD_COPY_ALBUM_BLOCKED;
		else
		{
				String subWavFile = null;
				int catID = 26;
				StringTokenizer stk = new StringTokenizer(strWavCatId,":");
				if(stk.hasMoreTokens())
					subWavFile = stk.nextToken().trim();
				if(stk.hasMoreTokens())
				{
					try
					{
						catID = Integer.parseInt(stk.nextToken().trim());
					}
					catch(Exception e)
					{
						catID = 26;
					}
					
				}
				Category cat = RBTDBManager.getInstance().getCategory(catID);
				String clip;
				String status = "1";
				if (strCallerID != null)
					status = status + "|" + strCallerID;
				if (cat != null && cat.getType() == SHUFFLE)
					clip = subWavFile + ":" + "S" + catID + ":" + status;
				else
					clip = subWavFile + ":" + catID + ":" + status;
				RBTDBManager.getInstance().insertViralSMSTableMap(strDestCallerID, null, "COPY",
						strSubID, clip, 0, "USSD", null, null);
				return USSD_SUCCESS;
		}
//		return retVal;
	}
	*/
    
/*    private int processUSSDSetRequest(String subID, String callerID, int categoryID,
			ClipMinimal clipMinimal, String actBy, String actInfo, String subClass) {
    	if(clipMinimal == null)
    		return USSD_VCODE_NOT_FOUND;
		int selResult = activateAndAddSelection(subID(subID), subID(callerID), categoryID,
				clipMinimal.getWavFile(), 1, actBy, actInfo, 0, 23, subClass, null,null);
		int retVal = USSD_UNKNOWN_ERROR;
		if (selResult == AUTO_SUCCESS)
			retVal = USSD_SUCCESS;
		return retVal;
	}*/
    
    /*private int processMODRequest(String subID, String callerID, int categoryID,
			ClipMinimal clipMinimal, String actBy, String actInfo, String subClass) {
		if(clipMinimal == null)
			return MOD_VCODE_INVALID;

		RBTDBManager dbManager = RBTDBManager.getInstance();
		Subscriber subscriber = dbManager.getSubscriber(subID);
		if(dbManager.isSubscriberActivationPending(subscriber)
				|| dbManager.isSubscriberDeactivationPending(subscriber))
			return MOD_WAITING_USER;
		if(dbManager.isSubscriberSuspended(subscriber))
			return MOD_SUSPENDED_USER;

		int selResult = activateAndAddSelection(subID(subID), subID(callerID), categoryID,
				clipMinimal.getWavFile(), 1, actBy, actInfo, 0, 23, subClass, subscriber,null);

		if(selResult == AUTO_SUCCESS) {
			if(dbManager.isSubscriberActivated(subscriber))
				return MOD_SEL_SUCCESS;
			else
				return MOD_SUB_SEL_SUCCESS;
		}
		else if(selResult == AUTO_SUBSCRIPTION_FAILED)
			return MOD_SUBSCRIPTION_FAILED;
		else if(selResult == AUTO_SELECTION_FAILED || selResult == AUTO_CALLER_SETTING_FULL)
			return MOD_SELECTION_FAILED;

		return MOD_ERROR;
	}
	*/
    
   private ClipMinimal getClipFromVCode(String vCode) {
    	String wavFileName = vCode;
    	if(!wavFileName.startsWith("rbt_"))
    	{
    		wavFileName = "rbt_" + wavFileName;
    	}
    	if(!wavFileName.endsWith("_rbt"))
    	{
    		wavFileName = wavFileName + "_rbt";
    	}
//    	return RBTDBManager.getInstance().getClipRBT("rbt_" + vCode + "_rbt");
    	return RBTDBManager.getInstance().getClipRBT(wavFileName);
    }
    
    private ClipMinimal getClipMinimal(String clipPromoID) {
    	String method = "getClipMinimal";
    	RBTMOHelper moHelper = RBTMOHelper.init();
    	ClipMinimal clipMinimal = null;
    	try {
    		clipMinimal = moHelper.getClipPromoID(clipPromoID);
    	}
    	catch(Exception e) {
    	/*	logger.info("RBT:: exception while getting clipMinimal. "
					+ e.getMessage());
    		Clips clip = getClipPromoID(clipPromoID);
    		if(clip != null)
    			clipMinimal = new ClipMinimal(clip);
    	*/}
    	return clipMinimal;
    }
    
    private ClipMinimal getClipMinimal(String clipPromoID, boolean checkMap) {
    	String method = "getClipMinimal";
    	RBTMOHelper moHelper = RBTMOHelper.init();
    	ClipMinimal clipMinimal = null;
    	try {
    		
    		clipMinimal = moHelper.getClipPromoID(clipPromoID,checkMap);
    	}
    	catch(Exception e) {
    	/*	logger.info("RBT:: exception while getting clipMinimal. "
					+ e.getMessage());
    		Clips clip = getClipPromoID(clipPromoID);
    		if(clip != null)
    			clipMinimal = new ClipMinimal(clip);
    	*/}
    	return clipMinimal;
    }
    /*public int activateAndAddSelection(String subID, String callerID, int categoryID,
			String songName, int status, String activatedBy, String activationInfo, int fromTime,
			int toTime, String subClass, Subscriber subscriber,String selInterval) {
    	return activateAndAddSelection(subID, callerID, categoryID, songName, status, activatedBy,
				activationInfo, fromTime, toTime, subClass, subscriber, activatedBy,selInterval);
    }
	*/
    
    //returns auto dialer codes
/*    public int activateAndAddSelection(String subID, String callerID, int categoryID,
			String songName, int status, String activatedBy, String activationInfo, int fromTime,
			int toTime, String subClass, Subscriber subscriber, String selectedBy, String selInterval) {
    	String method = "activateAndAddSelecion";
		RBTDBManager dbManager = RBTDBManager.getInstance();
		if(subscriber == null)
			subscriber = dbManager.getSubscriber(subID);
		int retInt = AUTO_ERROR;
		if(subID.equals(callerID))
			return retInt;
		if (subscriber == null || dbManager.checkCanAddSelection(subscriber)) {
			if (dbManager.isSubscriberDeactivated(subscriber))
				subscriber = activateSubscriber(subID, activatedBy, null, true, 0, activationInfo,
						subClass);
			if (dbManager.isSubscriberDeactivated(subscriber)) {
				logger.info("RBT:: not able to activate subscriber");
				retInt = AUTO_SUBSCRIPTION_FAILED;
			}
			else {
				//Calendar endCal = Calendar.getInstance();
				//endCal.set(2037, 0, 1);
				boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();

				boolean addSelection = dbManager.checkMaxCallerIDSelections(subscriber, callerID);

				if(addSelection
						&& dbManager.addSubscriberSelections(subID, callerID, categoryID, songName,
								null, null, m_endDate, status, selectedBy, activationInfo,
								0, true, false, m_messagePath, fromTime, toTime, null,
								m_useSubscriptionManager, true, null, null, subscriber.subYes(),
								null, false, false, inLoop, subClass, subscriber,selInterval))
					retInt = AUTO_SUCCESS;
				else if (!addSelection)
					retInt = AUTO_CALLER_SETTING_FULL;
				else
					retInt = AUTO_SELECTION_FAILED;
			}
		}
		logger.info("RBT:: returning " + retInt);
		return retInt;
	}
  */  
    //returns auto-dial response codes
/*    public int upgradeAndAddSelection(String subID, String callerID, int categoryID,
			String songName, int status, String activatedBy, String activationInfo, int fromTime,
			int toTime, String subClass, Subscriber subscriber) {
		String method = "upgradeAndAddSelection";
		int retInt = AUTO_ERROR;
		String actByForUpgrade = null;
		if(activatedBy.equals("EC"))
			actByForUpgrade = activatedBy;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		if(subscriber == null)
			subscriber = dbManager.getSubscriber(subID);
		if(subscriber == null || dbManager.checkCanAddSelection(subscriber)) {
			if(dbManager.isSubscriberDeactivated(subscriber))
				subscriber = activateSubscriber(subID, activatedBy, null, true, 0, activationInfo,
						subClass);
			else {
				if(dbManager.convertSubscriptionType(subID, subscriber.subscriptionClass(),
						subClass, actByForUpgrade, 0, false))
					subscriber = getSubscriber(subID);
			}
			if(dbManager.isSubscriberDeactivated(subscriber)) {
				logger.info("RBT:: not able to activate subscriber");
				retInt = AUTO_SUBSCRIPTION_FAILED;
			}
			else {
				boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();

				boolean addSelection = dbManager.checkMaxCallerIDSelections(subscriber, callerID);

				if(addSelection
						&& dbManager.addSubscriberSelections(subID, callerID, categoryID, songName,
								null, null, m_endDate, status, activatedBy, activationInfo,
								0, true, false, m_messagePath, fromTime, toTime, null,
								m_useSubscriptionManager, true, null, null, subscriber.subYes(),
								null, false, false, inLoop, subClass, subscriber,null))
					retInt = AUTO_SUCCESS;
				else if(!addSelection)
					retInt = AUTO_CALLER_SETTING_FULL;
				else
					retInt = AUTO_SELECTION_FAILED;
			}
		}
		return retInt;
	}
  */  
    public HashMap getUSSDHashMap(int status) {
    	if(!m_ussdResposeMap.containsKey("USSD_RESPONSE_" + status)) {
    		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(
					"WAR", "USSD_RESPONSE_" + status);
    		if(param != null)
    			m_ussdResposeMap.put(param.getParam(), param.getValue());
    	}
		HashMap retMap = new HashMap();
		retMap.put("status", new Integer(status));
		String retMessage = (String)m_ussdResposeMap.get("USSD_RESPONSE_" + status);
		retMap.put("message", retMessage);
		return retMap;
	}
    
    /**
	 * @author Sreekar 2008-02-11 Added for Airtel
	 */
/*    public int processAutoDialer(String strSubID, String strCallerID, String strVCode, int sType,
			int uCode, String sysCode, String flag, String albumCode, String validSubResult) {
		String method = "processAutoDialer";
		logger.info("RBT:: strSubID: " + strSubID + ", strCallerID: "
				+ strCallerID + ", strVCode: " + strVCode + ", sType: " + sType + ", uCode: "
				+ uCode + ", sysCode: " + sysCode + ", flag: " + flag + ", album code: "+albumCode);
		strSubID = subID(strSubID);
		if(strSubID.length() != 10)
			return AUTO_SUBSCRIBER_INVALID;
		strCallerID = subID(strCallerID);
		if(validSubResult.equalsIgnoreCase("failure")) {
			logger.info("RBT::redirecting subscriber " + strSubID);
			String url = getAutodialUrlForSub(strSubID);
			if(url != null) {
				HashMap httpParamMap = new HashMap();
				if(strSubID != null)
					httpParamMap.put("msisdn", strSubID);
				if(strVCode != null)
					httpParamMap.put("vcode", strVCode);
				if(strCallerID != null)
					httpParamMap.put("caller", strCallerID);
				if(sType != -1)
					httpParamMap.put("stype", String.valueOf(sType));
				if(uCode != -1)
					httpParamMap.put("ucode", String.valueOf(uCode));
				if(flag != null)
					httpParamMap.put("flag", flag);
				if(albumCode != null)
					httpParamMap.put("albumcode", albumCode);
				if(sysCode != null)
					httpParamMap.put("syscode", sysCode);
				
				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(url);
				try {
					String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap, null);
					return Integer.parseInt(postResult.trim());
				}
				catch (HttpException e) {
					logger.error("", e);
				}
				catch (IOException e) {
					logger.error("", e);
				} 
				catch (RBTException e) {
					logger.error("", e);
				}
				catch (Exception e) {
					logger.error("", e);
				}
			} // url null if
			else {
				logger.info("RBT::url not found for " + strSubID);
				return AUTO_SUBSCRIBER_INVALID;
			}
			return AUTO_ERROR;
		}// end of non valid sub if
		
		// in bound request
		if(sType == -2) {
			if (strVCode == null)
				return AUTO_VCODE_INVALID;
//			String cCode = RBTDBManager.getCCodeFromVCode(strVCode);
			ClipMinimal clipMinimal = getClipFromVCode(strVCode);
//			logger.info("Sree1:: vcode->" + strVCode + " ccode->" + cCode
//					+ " clipMinimal->" + clipMinimal);
			if(clipMinimal == null || clipMinimal.getEndTime().before(new Date()))
				return AUTO_VCODE_INVALID;
			return processInBoundRequest(strSubID, strVCode, sysCode);
		}
		
		int retVal = AUTO_SUCCESS;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		if (!validSubResult.equalsIgnoreCase("success"))
			return AUTO_SUBSCRIBER_INVALID;
		if (strVCode == null && (sType != 4 && sType != 6 && sType != 7))
			return AUTO_VCODE_INVALID;
		if(sType != 4 && uCode != 0 && uCode != 1)
			return AUTO_UCODE_INVALID;
		if((sType == 6 || sType == 7) && (albumCode == null || albumCode.equals("")))
			return AUTO_ALBUM_CODE_INVALID;
		if(strCallerID != null && uCode == 1) {
			strCallerID = subID(strCallerID);
			if(strCallerID.length() != 10)
				return AUTO_CALLER_INVALID;
			try {
				Long.parseLong(strCallerID);
			}
			catch(Exception e) {
				return AUTO_CALLER_INVALID;
			}
		}

		/*String cCode = RBTDBManager.getCCodeFromVCode(strVCode);
		ClipMinimal clipMinimal = getClipMinimal(cCode);
		logger.info("Sree:: vcode->" + strVCode + " ccode->" + cCode
				+ " clipMinimal->" + clipMinimal);
		if(sType != 4 && (clipMinimal == null || clipMinimal.getEndTime().before(new Date())))
			return AUTO_VCODE_INVALID;
		
		Subscriber subscriber = dbManager.getSubscriber(strSubID);
		if (subscriber != null
				&& (dbManager.isSubscriberActivationPending(subscriber) || dbManager
						.isSubscriberDeactivationPending(subscriber))) {
			logger.info("RBT:: user activation/deactivation pending.....");
			return AUTO_ACT_DEACT_PENDING;
		}
		
		if (subscriber != null && (dbManager.isSubscriberSuspended(subscriber))) { 
			logger.info("RBT:: user suspended....."); 
			return AUTO_SUBSCRIBER_SUSPENDED; 
		} 
//		String cCode = RBTDBManager.getCCodeFromVCode(strVCode); 
		ClipMinimal clipMinimal = getClipFromVCode(strVCode); 
//		logger.info("Sree:: vcode->" + strVCode + " ccode->" + cCode 
//		                + " clipMinimal->" + clipMinimal); 
		if((sType != 4 && sType != 6 && sType != 7) && (clipMinimal == null || clipMinimal.getEndTime().before(new Date()))) 
		        return AUTO_VCODE_INVALID; 

		try {
			int catID = 54;
			if(uCode == 0)
				strCallerID = null;
			switch (sType) {
			case 1: // subscription and selection
				catID = m_autoSubCategory;
				if (!dbManager.isSubscriberDeactivated(subscriber)) {
					logger.info("RBT::activation request received for subscribed user " + strSubID);
					return AUTO_SUBSCRIBER_EXISTS;
				}
				retVal = activateAndAddSelection(strSubID, strCallerID, catID, clipMinimal.getWavFile(), 1,
						getAutoActBy(sysCode), sysCode, 0, 23, m_autoSubClass, subscriber,null);
				break;
			case 2: // selection with category 1
			case 3: // selection with category 2
				catID = m_autoCategoryLower;
				if (sType == 3)
					catID = m_autoCategoryUpper;
				if (dbManager.isSubscriberDeactivated(subscriber)) {
					logger.info("RBT::selection request received for non-subscriber user " + strSubID);
					return AUTO_SUBSCRIBER_NOT_EXISTS;
				}
				retVal = activateAndAddSelection(strSubID, strCallerID, catID, clipMinimal.getWavFile(), 1,
						getAutoActBy(sysCode), sysCode, 0, 23, subscriber.subscriptionClass(), subscriber,null);
				break;
			case 4:
				String value = getAutoAdvRntlSubClass(flag); 
                String subClass = value.substring(0, value.indexOf(",")); 
                int categoryID = Integer.parseInt(value.substring(value.indexOf(",")+1)); 
				if(flag == null || subClass == null)
					return AUTO_FLAG_INVALID;
//				int categoryID = m_autoCategoryLower;
//				if(isFreeAdvRntlSubClass(subClass))
//					categoryID = m_ecCategory;
				if(subscriber == null || dbManager.isSubDeactive(subscriber)) {
					if(clipMinimal != null)
						retVal = activateAndAddSelection(strSubID, strCallerID, categoryID,
								clipMinimal.getWavFile(), 1, getAutoActBy(sysCode), sysCode, 0, 23,
								subClass, null,null);
					else {
						subscriber = activateSubscriber(strSubID, getAutoActBy(sysCode), null, true,
								0, sysCode, subClass);
						if(subscriber == null)
							retVal = AUTO_SUBSCRIPTION_FAILED;
					}
				}
				else {
					if(subscriber.rbtType() == 1)
						retVal = AUTO_SUBSCRIPTION_FAILED;
					else if(m_advancePacksList.indexOf(subscriber.subscriptionClass()) != -1)
						retVal = AUTO_SUBSCRIBER_EXISTS;
					else if(dbManager.isAlbumRentalSubClass(subscriber.subscriptionClass()))
						retVal = AUTO_SUBSCRIPTION_FAILED;
					else if(clipMinimal != null)
						retVal = upgradeAndAddSelection(strSubID, strCallerID, categoryID,
								clipMinimal.getWavFile(), 1, getAutoActBy(sysCode), sysCode, 0, 23,
								subClass, subscriber);
					else {
						if(!dbManager.convertSubscriptionType(strSubID, subscriber
								.subscriptionClass(), subClass))
							retVal = AUTO_SUBSCRIPTION_FAILED;
					}
				}
				break;
				
			case 5: 
                catID = m_ecCategory; 
                if (!dbManager.isSubscriberDeactivated(subscriber)) { 
                        logger.info("RBT::activation request received for subscribed user " + strSubID); 
                        return AUTO_SUBSCRIBER_EXISTS; 
                } 
                retVal = activateAndAddSelection(strSubID, strCallerID, catID, clipMinimal.getWavFile(), 1, 
                                getAutoActBy(sysCode), sysCode, 0, 23, m_autoSubClass, subscriber,null); 
                break;
			case 6: // subscription and selection for album
				if (!dbManager.isSubscriberDeactivated(subscriber)) {
					logger.info("RBT::activation request received for subscribed user " + strSubID);
					return AUTO_SUBSCRIBER_EXISTS;
				}
				Category category = dbManager.getCategoryPromoID(albumCode);
				if(category == null)
					return AUTO_ALBUM_CODE_INVALID;
				catID = category.getID();
				Clips[] allClips = dbManager.getAllClips(catID);
		    	String songName = null;
		    	if(allClips != null)
		    		songName = allClips[0].wavFile();
				retVal = activateAndAddSelection(strSubID, strCallerID, catID, songName, 1,
						getAutoActBy(sysCode), sysCode, 0, 23, m_autoSubClass, subscriber,null);
				break;
			case 7://album selection
				if (dbManager.isSubscriberDeactivated(subscriber)) {
					logger.info("RBT::selection request received for non-subscriber user " + strSubID);
					return AUTO_SUBSCRIBER_NOT_EXISTS;
				}
				Category albCat = dbManager.getCategoryPromoID(albumCode);
				if(albCat == null)
					return AUTO_ALBUM_CODE_INVALID;
				if(dbManager.isAdvanceRentalSubClass(subscriber.subscriptionClass()))
					return AUTO_ERROR;
				catID = albCat.getID();
				Clips[] allAlbumClips = dbManager.getAllClips(catID);
		    	String song = null;
		    	if(allAlbumClips != null)
		    		song = allAlbumClips[0].wavFile();
				retVal = activateAndAddSelection(strSubID, strCallerID, catID, song, 1,
						getAutoActBy(sysCode), sysCode, 0, 23, subscriber.subscriptionClass(), subscriber,null);
				break;
			default:
				retVal = AUTO_STYPE_INVALID;
				logger.info("RBT::invalid sType: " + sType);
				break;
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		return retVal;
	}
    */
    /*private int processInBoundRequest(String strSubID, String wavFile, String sysCode) {
    	String method = "processInBoundRequest";
    	logger.info("RBT::in bound requets for sub: " + strSubID);
    	return activateAndAddSelection(strSubID, null, m_autoCategoryLower, wavFile, 1,
					getAutoActBy(sysCode), sysCode, 0, 23, m_autoSubClass, getSubscriber(strSubID),null);
    }
    */
    /*private boolean isFreeAdvRntlSubClass(String subClass) {
    	if(m_freeAdvRntlSubClass.indexOf(subClass) != -1)
    		return true;
    	return false;
    }*/
    
/*    private String getAutoActBy(String ip) {
    	if(m_autoActivatedByMap.containsKey(ip))
    		return (String)m_autoActivatedByMap.get(ip);
    	else
    		return "OBD";
    }
    
    private String getAutoAdvRntlSubClass(String flag) {
    	if(flag != null && m_autoFlagSubClassMap.containsKey(flag))
    		return (String)m_autoFlagSubClassMap.get(flag);
    	return null;
    }
	*/
    
    /**
	 * @author Sreekar 2008-02-11 Added for Airtel
	 */
    /**
     * @param strSubID
     * @param strCallerID
     * @param strDestCallerID
     * @param strVCode
     * @param flag
     * @param index
     * @param downloadCharge
     * @param strIP
     * @param strRequester
     * @param validSubResult
     * @param isSubscribed
     * @return
     */
  /*  public HashMap processEnvIO(String strSubID, String strCallerID, String strDestCallerID,
			String strVCode, int flag, int index, int downloadCharge, String strIP,
			String strRequester, String validSubResult, String isSubscribed, int subscriptionCharge) {
		String method = "processEnvIO";
		
		logger.info("RBT:: strSubID: " + strSubID + ", flag: " + flag
				+ ", strVCode: " + strVCode + ", strCallerID: " + strCallerID
				+ ", strDestCallerID: " + strDestCallerID + ", index: " + index + ", downChg: "
				+ downloadCharge + ", strRequester: " + strRequester + ", isSubscribed: "
				+ isSubscribed);
		strSubID = subID(strSubID);
		strCallerID = subID(strCallerID);
		if(strSubID.length() != 10)
			return getEnvIOHashMap(ENVIO_SUBSCRIBER_INVALID);
		if (validSubResult.equalsIgnoreCase("failure") && flag != 0) {
			logger.info("RBT::redirecting subscriber " + strSubID);
			String url = getEnvioUrlForSub(strSubID);
			if (url != null) {
				logger.info("RBT::redirecting url " + url);
				HashMap httpParamMap = new HashMap();
				if (strSubID != null)
					httpParamMap.put("srcmsisdn", strSubID);
				if (strVCode != null)
					httpParamMap.put("vcode", strVCode);
				if (strCallerID != null)
					httpParamMap.put("cbsmsisdn", strCallerID);
				if (strDestCallerID != null)
					httpParamMap.put("dstmsisdn", strDestCallerID);
				if (flag != -1)
					httpParamMap.put("Flag", String.valueOf(flag));
				if (index != -1)
					httpParamMap.put("indx", String.valueOf(index));
				
				httpParamMap.put("downChg", String.valueOf(downloadCharge));
				httpParamMap.put("subsChg", String.valueOf(subscriptionCharge));
				httpParamMap.put("requesterip", strIP);
				httpParamMap.put("requester", "onmobile");

				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(url);
				try {
					String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap,
							null);
					logger.info("RBT:: postResult " + postResult);
					String res = postResult.substring(0, postResult.indexOf("|"));
					String msg = postResult.substring(postResult.indexOf("|") + 1);
					HashMap returnMap = new HashMap();
					returnMap.put("result", new Integer(res.trim()));
					returnMap.put("message", msg.trim());
					return returnMap;
				}
				catch (HttpException e) {
					logger.error("", e);
				}
				catch (IOException e) {
					logger.error("", e);
				}
				catch (RBTException e) {
					logger.error("", e);
				}
				catch (Exception e) {
					logger.error("", e);
				}
			} // url null if
			else {
				logger.info("RBT::url not found for " + strSubID);
				return getEnvIOHashMap(ENVIO_SUBSCRIBER_INVALID);
			}
			return getEnvIOHashMap(ENVIO_ERROR);
		}
		String subProfile = null;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		ClipMinimal clipMinimal = null;
		String activatedBy=null;

		if(flag != 0 && flag != 4)
			strDestCallerID = null;
		if(index == 0)
			strCallerID = null;
		if((flag == 0 || flag == 1 || flag == 3) && (index < 0 || index > 3))
			return getEnvIOHashMap(ENVIO_INDEX_INVALID);
		
		if (strSubID == null || strSubID.length() < 7 || strSubID.length() > 15)
			return getEnvIOHashMap(ENVIO_SUBSCRIBER_INVALID);
		if (strCallerID != null && (strCallerID.length() < 7 || strCallerID.length() > 15))
			return getEnvIOHashMap(ENVIO_CALLER_INVALID);
		if (strDestCallerID != null) {
			if (strDestCallerID.length() < 7 || strDestCallerID.length() > 15)
				return getEnvIOHashMap(ENVIO_DST_CALLER_INVALID);
//			Subscriber dstSubscriber = dbManager.getSubscriber(strDestCallerID);
//			if (dstSubscriber == null)
//				return getEnvIOHashMap(ENVIO_DST_CALLER_NOT_EXITS);
		}
		if(strVCode == null && (flag == 1 || flag == 4 || flag == 5 || flag == 6 || flag==7))
			return getEnvIOHashMap(ENVIO_VCODE_INVALID);
		if(strVCode != null && (flag == 1 || flag == 4 || flag==6 || flag==7)) {
			if(strVCode.length() != 15)
				return getEnvIOHashMap(ENVIO_VCODE_INVALID);
			clipMinimal = getClipFromVCode(strVCode);//getClipMinimal(clipPromoID,false);
			if(clipMinimal == null)
				return getEnvIOHashMap(ENVIO_VCODE_INVALID);
			else if(clipMinimal.getEndTime().before(new Date()))
				return getEnvIOHashMap(ENVIO_VCODE_EXPIRED);
		}
		Subscriber subscriber = dbManager.getSubscriber(strSubID);
		int retVal = ENVIO_ERROR;
		switch (flag) {
		case 0: // copy
			if(strDestCallerID == null || strDestCallerID.equalsIgnoreCase(strSubID))
				return getEnvIOHashMap(ENVIO_DST_CALLER_INVALID);
			int copyRetVal = processEnvIOCopy(strSubID, strDestCallerID, strCallerID,
					downloadCharge, isSubscribed, strIP);
			if (copyRetVal == ENVIO_COPY_ALL_SUCCESS_NEW_SUB) {
				if (dbManager.isSubscriberDeactivated(subscriber)) {
					if (strCallerID == null)
						retVal = ENVIO_COPY_ALL_SUCCESS_NEW_SUB;
					else
						retVal = ENVIO_COPY_CALLER_SUCCESS_NEW_SUB;
				}
				else {
					if (strCallerID == null)
						retVal = ENVIO_COPY_ALL_SUCCESS;
					else
						retVal = ENVIO_COPY_CALLER_SUCCESS;
				}
			}
			else if(copyRetVal == ENVIO_DST_CALLER_NOT_EXITS)
				retVal = ENVIO_DST_CALLER_NOT_EXITS;
			else if(copyRetVal == ENVIO_DST_CALLER_INVALID)
				retVal = ENVIO_DST_CALLER_INVALID;
			else {
				if (dbManager.isSubscriberDeactivated(subscriber)) {
					if (strCallerID == null)
						retVal = ENVIO_COPY_ALL_FAILURE_NEW_SUB;
					else
						retVal = ENVIO_COPY_CALLER_FAILURE_NEW_SUB;
				}
				else {
					if (strCallerID == null)
						retVal = ENVIO_COPY_ALL_FAILURE;
					else
						retVal = ENVIO_COPY_CALLER_FAILURE;
				}
			}
			break;
		case 1: // selection request
			if(strVCode == null)
				return getEnvIOHashMap(ENVIO_VCODE_INVALID);
			String subClass = m_envIOSubClass;
			String selectedBy = "ENVIO";
			if(downloadCharge == 0)
				selectedBy = "ENVIO_FREE";
			int selRetVal = activateAndAddSelection(strSubID, strCallerID, m_envIOCatgory,
					clipMinimal.getWavFile(), 1, "ENVIO", "ENVIO:" + strIP, 0, 23, subClass,
					subscriber, selectedBy, null);
			if (selRetVal == AUTO_SUBSCRIPTION_FAILED)
				retVal = ENVIO_SUB_SEL_FAILURE;
			else if (selRetVal == AUTO_SELECTION_FAILED)
				retVal = ENVIO_SEL_FAILURE;
			else if (selRetVal == AUTO_CALLER_SETTING_FULL)
				retVal = ENVIO_CALLER_SETTING_FULL;
			else if (selRetVal == AUTO_SUCCESS) {
				if (subscriber == null || dbManager.isSubDeactive(subscriber))
					retVal = ENVIO_SUB_SEL_SUCCESS;
				else
					retVal = ENVIO_SEL_SUCCESS;
			}
			break;
		case 2: // profile query
			int subType = getAirtelSubscriberType(subscriber);
			logger.info("RBT::subType = " + subType);
			if(dbManager.isSubscriberActivationPending(subscriber)) {
				retVal = ENVIO_SUB_WAITING_OR_HLR_REMOVE;
				subProfile = subType + ":Waiting";
			}
			else if(dbManager.isSubscriberDeactivationPending(subscriber)) {
				retVal = ENVIO_SUB_WAITING_OR_HLR_REMOVE;
				subProfile = subType + ":HLR Remove";
			}
			else if (subscriber == null || subscriber.subYes().equals(STATE_DEACTIVATED))
				retVal = ENVIO_SUBSCRIBER_NOT_EXISTS;
			else if(subscriber.subYes().equals(STATE_ACTIVATED)) {
				retVal = ENVIO_SUBSCRIBER_EXISTS;
				subProfile = getActiveSubscriberProfileString(strSubID, subType);
			}
			break;
		case 3: // deleting a tone
			if(!dbManager.checkCanAddSelection(subscriber))
				retVal = ENVIO_SUBSCRIBER_NOT_EXISTS;
			else {
				if(index <= 0)
					strCallerID = null;
				SubscriberStatus subSel = dbManager.getActiveSubscriberRecord(strSubID,
						strCallerID, 1, 0, 23);
				if(subSel == null)
					retVal = ENVIO_SEL_DELETION_INVALID;
				else {
					if(dbManager.deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 23,
							m_useSubscriptionManager, "ENVIO"))
						retVal = ENVIO_SEL_DELETION_SUCCESSFUL;
					else
						retVal = ENVIO_SEL_DELETION_FAILED;
				}
			}
			break;
		case 4: // gifting
			//only subscribed users can gift
			if(subscriber == null)
				retVal = ENVIO_SUBSCRIBER_INVALID;
			else
				retVal = processEnvioGift(strSubID, strDestCallerID, clipMinimal.getClipId(),
						downloadCharge);
			break;
		case 5: //album selection
			retVal = processEnvioAlbumSelection(strSubID, strCallerID, strVCode, strIP, subscriber,
					downloadCharge);
			break;
		case 6:  
			activatedBy="ENVIO";
			if (subscriptionCharge==0)
				activatedBy="ENVIO_FREE";
		
			if (dbManager.isSubActive(subscriber))
				return getEnvIOHashMap(ENVIO_SUB10_FAILURE);
			if(strVCode == null)
				return getEnvIOHashMap(ENVIO_VCODE_INVALID);
			subClass = m_envIOSubClass_10DayPack;
			selectedBy = "ENVIO";
			if(downloadCharge == 0)
				selectedBy = "ENVIO_FREE";
			selRetVal = activateAndAddSelection(strSubID, strCallerID, m_envIOCatgory,
					clipMinimal.getWavFile(), 1, activatedBy, "ENVIO:" + strIP, 0, 23, subClass,
					subscriber, selectedBy, null);
			if (selRetVal == AUTO_SUBSCRIPTION_FAILED)
				retVal = ENVIO_SUB10_FAILURE;
			else if (selRetVal == AUTO_SELECTION_FAILED)
				retVal = ENVIO_SEL_FAILURE;
			else if (selRetVal == AUTO_CALLER_SETTING_FULL)
				retVal = ENVIO_CALLER_SETTING_FULL;
			else if (selRetVal == AUTO_SUCCESS) {
					retVal = ENVIO_SUB10_SUCCESS;

			}
			break;
		case 7:  
			activatedBy="ENVIO";
			if (subscriptionCharge==0)
				activatedBy="ENVIO_FREE";
		
			if(strVCode == null)
				return getEnvIOHashMap(ENVIO_VCODE_INVALID);
			
			if (dbManager.isSubActive(subscriber))
				return getEnvIOHashMap(ENVIO_SUB20_FAILURE);
			
			subClass = m_envIOSubClass_20DayPack;
			selectedBy = "ENVIO";
			if(downloadCharge == 0)
				selectedBy = "ENVIO_FREE";
			selRetVal = activateAndAddSelection(strSubID, strCallerID, m_envIOCatgory,
					clipMinimal.getWavFile(), 1, activatedBy, "ENVIO:" + strIP, 0, 23, subClass,
					subscriber, selectedBy, null);
			if (selRetVal == AUTO_SUBSCRIPTION_FAILED)
				retVal = ENVIO_SUB10_FAILURE;
			else if (selRetVal == AUTO_SELECTION_FAILED)
				retVal = ENVIO_SEL_FAILURE;
			else if (selRetVal == AUTO_CALLER_SETTING_FULL)
				retVal = ENVIO_CALLER_SETTING_FULL;
			else if (selRetVal == AUTO_SUCCESS) {
				retVal = ENVIO_SUB20_SUCCESS;

		}
			break;

		
		default:
			logger.info("RBT::invalid flag " + flag);
			retVal = ENVIO_FLAG_INVALID;
			break;
		}
		return getEnvIOHashMap(strSubID, strCallerID, retVal, subProfile);
	}
    
    public int processMOD(String strSubID, String strVCode, String strIP, String validSubResult, String actBy) {
		String method = "processMOD";

		logger.info("RBT:: strSubID: " + strSubID + ", strVCode: " + strVCode);
		strSubID = subID(strSubID);

		if(validSubResult.equalsIgnoreCase("failure")) {
			logger.info("RBT::redirecting subscriber " + strSubID);
			String url = getMODUrlForSub(strSubID);
			if(url != null) {
				logger.info("RBT::redirecting url " + url);
				HashMap httpParamMap = new HashMap();
				if(strSubID != null)
					httpParamMap.put("msisdn", strSubID);
				if(strVCode != null)
					httpParamMap.put("scode", strVCode);

				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(url);
				try {
					String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap,
							null);
					logger.info("RBT:: postResult " + postResult);
					if(postResult != null)
						return (Integer.parseInt(postResult.trim()));
				}
				catch (HttpException e) {
					logger.error("", e);
				}
				catch (IOException e) {
					logger.error("", e);
				}
				catch (RBTException e) {
					logger.error("", e);
				}
				catch (Exception e) {
					logger.error("", e);
				}
			} // url null if
			else {
				logger.info("RBT::url not found for " + strSubID);
				return MOD_SUBSCRIBER_INVALID;
			}
			return MOD_ERROR;
		}

		if(strVCode == null || (strVCode.length() != 15 && strVCode.length() != 6))
			return MOD_VCODE_INVALID;

		ClipMinimal clipMinimal = null;
		String clipPromoID = strVCode;
		if(strVCode.length() == 15)
			clipMinimal = getClipFromVCode(strVCode);
		else
			clipMinimal = getClipMinimal(clipPromoID);
		if(clipMinimal == null || clipMinimal.getEndTime().before(new Date()))
			return MOD_VCODE_INVALID;

		return (processMODRequest(strSubID, null, m_modCategory, clipMinimal, actBy,
				actBy + ":" + strIP, m_modSubClass));
	}
    
    private int processEnvioAlbumSelection(String strSubID, String strCallerID, String strVCode,
			String strIP, Subscriber subscriber, int downloadCharge) {
    	RBTDBManager dbManager = RBTDBManager.getInstance();
    	char prepaidYes = 'n';
		if(subscriber!=null && subscriber.prepaidYes())
			prepaidYes = 'y';
		String circleId=dbManager.getCircleId(strSubID);
    	Categories category = dbManager.getCategoryPromoID(strVCode.toLowerCase(), circleId, prepaidYes);
    	if(category == null)
    		return ENVIO_VCODE_INVALID;
    	if(category.endTime().before(new Date()))
    		return ENVIO_VCODE_EXPIRED;
    	
    	Clips[] allClips = dbManager.getAllClips(category.id());
    	String songName = null;
    	if(allClips != null)
    		songName = allClips[0].wavFile();
    	
    	if(subscriber != null && dbManager.isAdvanceRentalSubClass(subscriber.subscriptionClass()))
    			return ENVIO_SUB_ADVANCE_RENTAL_USER;
    	
    	String selectedBy = "ENVIO";
    	if(downloadCharge == 0)
    		selectedBy = "ENVIO_FREE";
    	int tempInt = activateAndAddSelection(strSubID, strCallerID, category.id(), songName, 1,
				"ENVIO", "ENVIO:" + strIP, 0, 23, dbManager.getAlbumSubClass(), subscriber, selectedBy);
    	
    	int retVal = ENVIO_ALBUM_SEL_SUCCESS;
    	if(tempInt == AUTO_ERROR || tempInt == AUTO_SELECTION_FAILED
				|| tempInt == AUTO_SUBSCRIPTION_FAILED)
    		retVal = ENVIO_ALBUM_SEL_FAILED;
    	if(tempInt == AUTO_CALLER_SETTING_FULL)
    		retVal = ENVIO_CALLER_SETTING_FULL;
    	return retVal;
    }
    
    private int processEnvioGift(String strSubID, String strDestCallerID, int clipID,
			int downloadCharge) {
		int retVal = ENVIO_GIFT_SUCCESSFUL;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		if (strDestCallerID == null || strDestCallerID.equalsIgnoreCase(strSubID)
				|| !dbManager.isValidOperatorPrefix(strDestCallerID))
			return ENVIO_DST_CALLER_INVALID;
		String selectedBy = "ENVIO";
		if(downloadCharge == 0)
			selectedBy = "ENVIO_FREE";
		ViralSMSTable viral = insertGiftRecord(strSubID, strDestCallerID, clipID, selectedBy);
		if(viral == null)
			retVal = ENVIO_GIFT_FAILED;
		
		return retVal;
    }
    */
    private String getActiveSubscriberProfileString(String strSubID, int subType) {
    	HashMap callerWavFileMap = new HashMap();
		HashMap callerSetTimeMap = new HashMap();
		RBTDBManager dbManager = RBTDBManager.getInstance();
		SubscriberStatus[] allSel = dbManager.getAllActiveSubSelectionRecords(strSubID);
		for(int i = 0; allSel != null && i < allSel.length; i++) {
			Date thisSetTime = allSel[i].setTime();
			String thisCaller = allSel[i].callerID();
			if(thisCaller == null)
				thisCaller = "ALL";
			if(callerWavFileMap.containsKey(thisCaller)) {
				Date otherSetTime = (Date)callerSetTimeMap.get(thisCaller);
				if(otherSetTime.after(thisSetTime)) {
					String wavFile = RBTDBManager.getVCodeFromWavFile(allSel[i].subscriberFile());
					if(subType == RBT_USER_TYPE_ALBUM && allSel[i].categoryType() == SHUFFLE) {
						Category category = dbManager.getCategory(allSel[i].categoryID());
						if(category != null && category.getPromoID() != null)
							wavFile = category.getPromoID();
					}
					callerWavFileMap.put(thisCaller, wavFile);
					callerSetTimeMap.put(thisCaller, allSel[i].setTime());
				}
			}
			else {
				String wavFile = RBTDBManager.getVCodeFromWavFile(allSel[i].subscriberFile());
				if(subType == RBT_USER_TYPE_ALBUM && allSel[i].categoryType() == SHUFFLE) {
					Category category = dbManager.getCategory(allSel[i].categoryID());
					if(category != null && category.getPromoID() != null)
						wavFile = category.getPromoID();
				}
				callerWavFileMap.put(thisCaller, wavFile);
				callerSetTimeMap.put(thisCaller, allSel[i].setTime());
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append(subType);
		sb.append("|");
		String allWavFile = (callerWavFileMap.containsKey("ALL")) ? ((String) callerWavFileMap
				.get("ALL"))
				: "";
			
		sb.append(allWavFile);
		Set keySet = callerWavFileMap.keySet();
		Iterator itr = keySet.iterator();
		while(itr.hasNext()) {
			String key = (String)itr.next();
			if(key.equals("ALL"))
				continue;
			sb.append("|");
			sb.append(key + ":" + (String)callerWavFileMap.get(key));
		}
		return sb.toString();
    }
    
    private int getAirtelSubscriberType(Subscriber subscriber) {
    	return RBTDBManager.getInstance().getAirtelSubscriberType(
				subscriber);
    }
    
    /*private int processEnvIOCopy(String strSubID, String strDestCallerID, String strCallerID,
			int downloadCharge, String isSubscribed, String strIP) {
		RBTDBManager dbManager = RBTDBManager.getInstance();
		int retVal = ENVIO_DST_CALLER_INVALID;
		String copiedBy = "ENVIO";
		if(downloadCharge == 0)
			copiedBy = "ENVIO_FREE";
		RBTCommonConfig rbtCommonConfig = RBTCommonConfig.getInstance();
		String strWavCatId = dbManager.getSubscriberVcode(strDestCallerID, strSubID,
				rbtCommonConfig.useProxyNonCircle(), rbtCommonConfig.proxyServerPort(), 0);
		if(strWavCatId == null || strWavCatId.equalsIgnoreCase("null") || strWavCatId.length() <= 0
				|| strWavCatId.equalsIgnoreCase("ERROR") || strWavCatId.equalsIgnoreCase("ALBUM"))
			retVal = ENVIO_COPY_ALL_FAILURE;
		else if ( strWavCatId.equalsIgnoreCase("DEFAULT")) {
			RBTDBManager.getInstance().insertViralSMSTableMap(strDestCallerID, null, "COPY",
						strSubID, null, 0, copiedBy, null, null);
			return ENVIO_COPY_ALL_SUCCESS_NEW_SUB;
		}
		else if(strWavCatId.equalsIgnoreCase("NOT_FOUND"))
			return ENVIO_DST_CALLER_NOT_EXITS;
		else if(strWavCatId.equalsIgnoreCase("NOT_VALID"))
			return ENVIO_DST_CALLER_INVALID;
		else {
    		if(isSubscribed == null && isValidSub(strSubID).equalsIgnoreCase("success")) {
    			Subscriber subscriber = dbManager.getSubscriber(strSubID);
    			isSubscribed = "true";
    			if(subscriber == null || dbManager.isSubDeactive(subscriber))
    				isSubscribed = "false";
    		}
			String subWavFile = null;
			int catID = 26;
			StringTokenizer stk = new StringTokenizer(strWavCatId,":");
			if(stk.hasMoreTokens())
				subWavFile = stk.nextToken().trim();
			if(stk.hasMoreTokens()) {
				try {
					catID = Integer.parseInt(stk.nextToken().trim());
				}
				catch(Exception e) {
					catID = 26;
				}
			}
			Category cat = RBTDBManager.getInstance().getCategory(catID);
			String clip;
			String status = "1";
			if (strCallerID != null)
				status = status + "|" + strCallerID;
			if (cat != null && cat.getType() == SHUFFLE)
				clip = subWavFile + ":" + "S" + catID + ":" + status;
			else
				clip = subWavFile + ":" + catID + ":" + status;
			RBTDBManager.getInstance().insertViralSMSTableMap(strDestCallerID, null, "COPY",
					strSubID, clip, 0, copiedBy, null, null);

    		return ENVIO_COPY_ALL_SUCCESS_NEW_SUB;
		}
		return retVal;
	}
    
    public HashMap getEnvIOHashMap(int code) {
    	return getEnvIOHashMap(null, null, code);
    }
    
    public HashMap getEnvIOHashMap(String strSubID, String strCallerID, int code) {
    	return getEnvIOHashMap(strSubID, strCallerID, code, null);
    }
    
    public HashMap getEnvIOHashMap(String strSubID, String strCallerID, int code, String message) {
		HashMap retMap = new HashMap();
		String retMessage = message;
		if(retMessage == null) {
			retMessage = (String) m_envIOResposeMap.get("ENVIO_RESPONSE_" + code);
			RBTDBManager dbManager = RBTDBManager.getInstance();
			if(code == ENVIO_CALLER_SETTING_FULL) {
				HashMap callerWavFileMap = new HashMap();
				HashMap callerSetTimeMap = new HashMap();
				SubscriberStatus[] allSel = dbManager.getAllActiveSubSelectionRecords(strSubID);
				for(int i = 0; allSel != null && i < allSel.length; i++) {
					Date thisSetTime = allSel[i].setTime();
					String thisCaller = allSel[i].callerID();
					if(thisCaller == null)
						thisCaller = "ALL";
					if(callerWavFileMap.containsKey(thisCaller)) {
						Date otherSetTime = (Date) callerSetTimeMap.get(thisCaller);
						if(otherSetTime.after(thisSetTime)) {
							callerWavFileMap.put(thisCaller, allSel[i].subscriberFile());
							callerSetTimeMap.put(thisCaller, allSel[i].setTime());
						}
					}
					else {
						callerWavFileMap.put(thisCaller, RBTDBManager.getVCodeFromWavFile(allSel[i]
								.subscriberFile()));
						callerSetTimeMap.put(thisCaller, allSel[i].setTime());
					}
				}
				StringBuffer sb = new StringBuffer();
				Set keySet = callerWavFileMap.keySet();
				Iterator itr = keySet.iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					if(key.equals("ALL"))
						continue;
					sb.append(key + ":" + (String) callerWavFileMap.get(key));
				}
				retMessage = sb.toString();
			}
		}

		retMap.put("result", new Integer(code));
		retMap.put("message", retMessage);
		return retMap;
	}
	*/
    
/*    public int processEasyCharge(String strSubID, String strReseller, String strVCode, int flag,
			String strCCode, String validSubResult) {
    	String method = "processEasyCharge";
    	logger.info("RBT:: strSubID: " + strSubID + ", flag: " + flag
				+ ", strVCode: " + strVCode + ", strReseller: " + strReseller + ", strCCode: "
				+ strCCode);
		strSubID = subID(strSubID);
		if(strSubID.length() != 10)
			return EC_0_SUBSCRIBER_INVALID;
		if (validSubResult.equalsIgnoreCase("failure")) {
			logger.info("RBT::redirecting subscriber " + strSubID);
			String url = getEasyChargeUrlForSub(strSubID);
			if (url != null) {
				HashMap httpParamMap = new HashMap();
				if (strSubID != null)
					httpParamMap.put("customer", strSubID);
				if (strVCode != null)
					httpParamMap.put("vcode", strVCode);
				if (strReseller != null)
					httpParamMap.put("reseller", strReseller);
				if (flag != -1)
					httpParamMap.put("Flag", String.valueOf(flag));
				if(strCCode != null)
					httpParamMap.put("ccode", strCCode);
				httpParamMap.put("requester", "onmobile");
				HttpParameters httpParameters = new HttpParameters();
				httpParameters.setUrl(url);
				try {
					String postResult = RBTHTTPProcessing.postFile(httpParameters, httpParamMap,
							null);
					return Integer.parseInt(postResult.trim());
				}
				catch (HttpException e) {
					logger.error("", e);
				}
				catch (IOException e) {
					logger.error("", e);
				}
				catch (RBTException e) {
					logger.error("", e);
				}
				catch (Exception e) {
					logger.error("", e);
				}
			} // url null if
			else {
				logger.info("RBT::url not found for " + strSubID);
				return EC_0_SUBSCRIBER_INVALID;
			}
			return EC_0_ERROR;
		}
		if(flag == -1)
			return EC_0_ERROR;
		ClipMinimal clipMinimal = null;
		if (strSubID == null || strSubID.length() > 15 || strSubID.length() < 7)
			return EC_0_SUBSCRIBER_INVALID;
		
		if (strVCode!= null && (strVCode.equals("") || strVCode.equalsIgnoreCase("null") || strVCode.equalsIgnoreCase("invalid") 
				|| strVCode.equalsIgnoreCase("unknown") || strVCode.equalsIgnoreCase("blank"))){
			strVCode = null;
		}
		
		if (strCCode!= null && (strCCode.equals("") || strCCode.equalsIgnoreCase("null") || strCCode.equalsIgnoreCase("invalid") 
				|| strCCode.equalsIgnoreCase("unknown") || strCCode.equalsIgnoreCase("blank"))){
			strCCode = null;
		}
		
		if (strVCode != null || strCCode != null) {
			if(flag !=2 && ((strVCode != null && strVCode.length() != 15)
					|| (strCCode != null && strCCode.length() != 8)))
				return EC_0_VCODE_INVALID;

			String clipPromoID = null;
			if(strVCode != null)
				clipMinimal = getClipFromVCode(strVCode);
			if(strCCode != null) {
				clipPromoID = strCCode.substring(2);
				clipMinimal = getClipMinimal(clipPromoID);
			}
			if(flag < 2) { // all flags >=2 are advance packs
				if(clipMinimal == null)
					return EC_0_VCODE_INVALID;
				else if(clipMinimal.getEndTime().before(new Date()))
					return EC_0_VCODE_INVALID;
			}
			else {
				if(clipMinimal != null && clipMinimal.getEndTime().before(new Date()))
					clipMinimal = null;
			}
		}
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Subscriber subscriber = dbManager.getSubscriber(strSubID);
		switch(flag) {
			case 0: // sub + selection
				if(dbManager.isSubscriberActivated(subscriber))
					return EC_0_SUBSCRIBER_EXISTS;
				if(!dbManager.isSubscriberDeactivated(subscriber))
					return EC_0_SUBSCRIBER_EXISTS_PENDING;
				ViralSMSTable[] viral = dbManager.getViralSMSByTypeForCaller(strSubID, "EC");
				for(int i = 0; viral != null && i < viral.length; i++) {
					if(viral[i].clipID() == null)
						return EC_0_SUBSCRIBER_EXISTS;
				}
				insertEasyCharge(strSubID, strReseller, null);
				int retVal = EC_0_SUCCESS;
				if(clipMinimal != null)
					retVal = processEasyChargeSelection(subscriber, strSubID, strReseller, clipMinimal);
				return retVal;
			case 1: // selection
				return processEasyChargeSelection(subscriber, strSubID, strReseller, clipMinimal);
			case 2: //advance rental case
			case 3:
			case 4:
				String wavFile = null;
				if(clipMinimal != null)
					wavFile = clipMinimal.getWavFile();
				return processECAdvanceRental(strSubID, subscriber, strReseller, wavFile, flag);
			default:
				return EC_0_ERROR;
		}
    }
  */  
    /*private int processEasyChargeSelection(Subscriber subscriber, String strSubID,
			String strReseller, ClipMinimal clipMinimal) {
		if(clipMinimal == null)
			return EC_1_VCODE_INVALID;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		ViralSMSTable[] viral1 = dbManager.getViralSMSByTypeForCaller(strSubID, "EC");
		boolean subEntry = !dbManager.isSubscriberDeactivated(subscriber);
		boolean clipExsts = false;
		for(int i = 0; viral1 != null && i < viral1.length; i++) {
			if(viral1[i].clipID() == null)
				subEntry = true;
			else if(viral1[i].clipID().equalsIgnoreCase(clipMinimal.getClipId()+""))
				clipExsts = true;
		}
		SubscriberStatus subscriberStatus = dbManager.getSelection(strSubID, clipMinimal.getWavFile());
		if(subscriberStatus != null && subscriberStatus.callerID() == null && subscriberStatus.status() == 1
				&& !subscriberStatus.selStatus().equalsIgnoreCase(STATE_DEACTIVATED))
			clipExsts = true;
		if(!subEntry)
			return EC_1_SUBSCRIBER_NOT_EXISTS;
		if(clipExsts)
			return EC_1_TONE_ALREADY_EXISTS;
		insertEasyCharge(strSubID, strReseller, clipMinimal.getClipId()+"");
		return EC_1_SUCCSS;
    }
	*/
    
/*    public String getECMessage(int code, String flag) {
    	String param = "EC_RESPONSE_" + flag + "_" + code;
    	String response = "ERROR";
    	if(m_ecResponseMap.containsKey(param))
    		response = (String)m_ecResponseMap.get(param);
    	else {
    		Parameters tempParam = RBTDBManager.getInstance()
					.getParameter("WAR", param);
    		if(tempParam != null)
    			response = tempParam.value();
    		m_ecResponseMap.put(param, response);
    	}
    	return response;
    }*/
    
/*    private int processECAdvanceRental(String strSubID, Subscriber subscriber, String strReseller,
			String wavFile, int flag) {
		int retVal = EC_2_ERROR;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		String newSubClass = getECAdvRntlSubClass(flag);
		if(newSubClass == null)
			return EC_2_TARIFF_CODE_INVALID;

		if(dbManager.isSubActive(subscriber)) {
			if(subscriber.rbtType() == 1)
				retVal = EC_2_SUBSCRIBER_EXISTS;
			else if(dbManager.isAdvanceRentalSubClass(subscriber.subscriptionClass()))
				retVal = EC_2_SUBSCRIBER_EXISTS;
			else if(wavFile != null) {
				int tempInt = upgradeAndAddSelection(strSubID, null, m_ecCategory, wavFile, 1,
						"EC", "EC:" + strReseller, 0, 23, newSubClass, subscriber);
				if(tempInt == AUTO_SUCCESS)
					retVal = EC_2_SUCCESS;
				else if(tempInt == AUTO_SUBSCRIPTION_FAILED)
					retVal = EC_2_SUB_FAILURE;
			}
			else {
				if(dbManager.convertSubscriptionType(strSubID, subscriber.subscriptionClass(),
						getECAdvRntlSubClass(flag), "EC", 0, false))
					retVal = EC_2_SUCCESS;
			}
		}
		else {
			if(wavFile != null) {
				int tempInt = upgradeAndAddSelection(strSubID, null, m_ecCategory, wavFile, 1,
						"EC", "EC:" + strReseller, 0, 23, newSubClass, subscriber);
				if(tempInt == AUTO_SUCCESS)
					retVal = EC_2_SUCCESS;
				else if(tempInt == AUTO_SUBSCRIPTION_FAILED)
					retVal = EC_2_SUB_FAILURE;
			}
			else {
				subscriber = activateSubscriber(strSubID, "EC", null, true, 0, "EC:" + strReseller,
						newSubClass);
				if(subscriber != null)
					retVal = EC_2_SUCCESS;
				else
					retVal = EC_2_SUB_FAILURE;
			}
		}
		return retVal;
	}
  */  
    /*private String getECAdvRntlSubClass(int flag) {
    	String method = "getECAdvRntlSubClass";
    	String strFlag = String.valueOf(flag);
    	if(m_ecFlagSubClassMap.containsKey(strFlag))
    		return (String)m_ecFlagSubClassMap.get(strFlag);
    	logger.info("RBT:: no subclass for tariff -> " + flag
				+ " and configured are " + m_ecFlagSubClassMap);
    	return null;
    }
    
    private void insertEasyCharge(String strSubID, String strReseller, String clipID) {
    	RBTDBManager.getInstance().insertViralSMSTableMap(strReseller,
				null, "EC", strSubID, clipID, 0, "EC", null,null);
    }
  */  
    /**
     * @author Sreekar 2008-03-27 Added for Airtel
     * @return HashMap containing the status and body of the http response
     * @purpose This method routes all the 3rd party API requests
     * 			to specific circle configured in the site prefix table
     */
/*    public HashMap processThirdPartyRequest(Map requestParams, String strIP, String requestType) {
    	String method = "processThirdPartyRequest";
    	String url = null;
    	HashMap httpParamMap = new HashMap();
    	String response = "INVALID_REQUEST";
    	int status = 200;
    	String user = null;
    	
    	if(requestType.equalsIgnoreCase("USSD")) { // USSD request
    		String strSubID = getHttpRequestParam("srcMsisdn", requestParams);
    		if(strSubID != null)
    			user = strSubID;
    		else
    			user = "";
    		String strCmd = getHttpRequestParam("cmd", requestParams);
    		String strVCode = getHttpRequestParam("vcode", requestParams);
    		String strCallerID = getHttpRequestParam("cbsMsisdn", requestParams);
    		String strdestCallerID = getHttpRequestParam("dstMsisdn", requestParams);
    		String strChg = getHttpRequestParam("chg", requestParams);
    		
			if(strSubID != null)
				httpParamMap.put("srcMsisdn", strSubID);
			if(strCmd != null)
				httpParamMap.put("cmd", strCmd);
			if(strVCode != null)
				httpParamMap.put("vcode", strVCode);
			if(strCallerID != null)
				httpParamMap.put("cbsMsisdn", strCallerID);
			if(strdestCallerID != null)
				httpParamMap.put("dstMsisdn", strdestCallerID);
			if(strChg != null)
				httpParamMap.put("chg", strChg);
			
    		logger.info("RBT::USSD request from " + strIP + " for user " + strSubID);
    		url = getUSSDUrlForSub(strSubID);
    	}
    	else if (requestType.equalsIgnoreCase("envio")) { // envio request
    		String strSubID = getHttpRequestParam("srcmsisdn", requestParams);
    		if(strSubID != null)
    			user = strSubID;
    		else
    			user = "";
    		String strVCode = getHttpRequestParam("vcode", requestParams);
    		String strCallerID = getHttpRequestParam("cbsmsisdn", requestParams);
    		String strDestCallerID = getHttpRequestParam("dstmsisdn", requestParams);
    		String flag = getHttpRequestParam("Flag", requestParams);
    		String index = getHttpRequestParam("indx", requestParams);
    		String strDownChg = getHttpRequestParam("downChg", requestParams);
    		
			if (strSubID != null)
				httpParamMap.put("srcmsisdn", strSubID);
			if (strVCode != null)
				httpParamMap.put("vcode", strVCode);
			if (strCallerID != null)
				httpParamMap.put("cbsmsisdn", strCallerID);
			if (strDestCallerID != null)
				httpParamMap.put("dstmsisdn", strDestCallerID);
			if (flag != null)
				httpParamMap.put("Flag", flag);
			if (index != null)
				httpParamMap.put("indx", index);
			if (strDownChg != null)
				httpParamMap.put("downChg", strDownChg);
			httpParamMap.put("requesterip", strIP);
			//System.out.println("url**********"+ getEnvioUrlForSub(strSubID));
    		logger.info("RBT::ENVIO request from " + strIP + " for user " + strSubID);
    		url = getEnvioUrlForSub(strSubID);
    	}
    	else if (requestType.equalsIgnoreCase("easycharge")) { // easyCharge request
    		String strSubID = getHttpRequestParam("customer", requestParams);
    		if(strSubID != null)
    			user = strSubID;
    		else
    			user = "";
    		String strVCode = getHttpRequestParam("vcode", requestParams);
    		String strReseller = getHttpRequestParam("reseller", requestParams);
    		String flag = getHttpRequestParam("Flag", requestParams);
    		String strCCode = getHttpRequestParam("ccode", requestParams);
    		String strTariff = getHttpRequestParam("tariff", requestParams);
    		
    		if (strSubID != null)
				httpParamMap.put("customer", strSubID);
			if (strVCode != null)
				httpParamMap.put("vcode", strVCode);
			if (strReseller != null)
				httpParamMap.put("reseller", strReseller);
			if (flag != null)
				httpParamMap.put("Flag", flag);
			if(strCCode != null)
				httpParamMap.put("ccode", strCCode);
			if(strTariff != null)
				httpParamMap.put("tariff", strTariff);
    		logger.info("RBT::EC request from " + strIP + " for user " + strSubID);
    		url = getEasyChargeUrlForSub(strSubID);
    	}
    	else if (requestType.equalsIgnoreCase("autodial")) { // autodial request
    		String strSubID = getHttpRequestParam("msisdn", requestParams);
    		if(strSubID != null)
    			user = strSubID;
    		else
    			user = "";
    		String strVCode = getHttpRequestParam("vcode", requestParams);
    		String strCallerID = getHttpRequestParam("caller", requestParams);
    		String sType = getHttpRequestParam("stype", requestParams);
    		String uCode = getHttpRequestParam("ucode", requestParams);
    		String sysCode = getHttpRequestParam("syscode", requestParams);
    		String flag = getHttpRequestParam("flag", requestParams);
    		String AlbumCode = getHttpRequestParam("AlbumCode", requestParams);
    		String albumcode = getHttpRequestParam("albumcode", requestParams);
    		
    		if(strSubID != null)
				httpParamMap.put("msisdn", strSubID);
			if(strVCode != null)
				httpParamMap.put("vcode", strVCode);
			if(strCallerID != null)
				httpParamMap.put("caller", strCallerID);
			if(sType != null)
				httpParamMap.put("stype", String.valueOf(sType));
			if(uCode != null)
				httpParamMap.put("ucode", String.valueOf(uCode));
			if(sysCode != null)
				httpParamMap.put("syscode", sysCode);
			if(flag != null)
				httpParamMap.put("flag", flag);
			if(AlbumCode != null)
				httpParamMap.put("AlbumCode", AlbumCode);
			if(albumcode != null)
				httpParamMap.put("albumcode", albumcode);
			
    		logger.info("RBT::AUTODIAL request from " + strIP + " for sub " + strSubID);
    		url = getAutodialUrlForSub(strSubID);
    	}
    	else if (requestType.equalsIgnoreCase("mod")) { // MOD request
    		String strSubID = getHttpRequestParam("msisdn", requestParams);
    		if(strSubID != null)
    			user = strSubID;
    		else
    			user = "";
    		String strSCode = getHttpRequestParam("scode", requestParams);
    		
    		if(strSubID != null)
				httpParamMap.put("msisdn", strSubID);
			if(strSCode != null)
				httpParamMap.put("scode", strSCode);
			
    		logger.info("RBT::MOD request from " + strIP + " for sub " + strSubID);
    		url = getMODUrlForSub(strSubID);
    	}
    	else
    		logger.info("RBT:: invalid request from " + strIP + " for user " + user);
    	
    	if (url != null) {
			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(url);

			try {
				String httpResponse = RBTHTTPProcessing.postFile(httpParameters, httpParamMap,
						null, true);
				//System.out.println("response**********"+ httpResponse);
				if(httpResponse != null) {
					int index = httpResponse.indexOf("|");
					if(index != -1) {
						response = httpResponse.substring(index+1);
						status = Integer.parseInt(httpResponse.substring(0, index));
					}
					else
						response = httpResponse;
				}
			}
			catch (Exception e) {
				if(requestType.equalsIgnoreCase("ussd")) {
                    HashMap map = getUSSDHashMap(USSD_UNKNOWN_ERROR);
                    response = (String)map.get(new Integer(USSD_UNKNOWN_ERROR));
                    status = USSD_UNKNOWN_ERROR; 
                } 
                else if(requestType.equalsIgnoreCase("envio")) { 
                        HashMap map = getEnvIOHashMap(ENVIO_ERROR); 
                        int result = ((Integer)map.get("result")).intValue(); 
                        String message = (String)map.get("message"); 
                        response = getEnvioResponseAsString(result, message); 
                } 
                else if (requestType.equalsIgnoreCase("autodial")) { 
                        response = String.valueOf(AUTO_ERROR); 
                } 
                else if (requestType.equalsIgnoreCase("easycharge")) { 
                        String message = getECMessage(EC_0_ERROR, String.valueOf(2)); 
                        response = getEasyChargeResponseAsString(EC_0_ERROR, message); 

				}
                else if (requestType.equalsIgnoreCase("mod")) {
                	response = String.valueOf(MOD_ERROR);
                }
				logger.error("", e);
			}
		}
    	else {
    		logger.info("request type is " + requestType); 
            if(requestType.equalsIgnoreCase("ussd")) { 
            	HashMap map = getUSSDHashMap(USSD_SUBSCRIBER_INVALID);
                response = (String)map.get(new Integer(USSD_SUBSCRIBER_INVALID));
                status = USSD_SUBSCRIBER_INVALID; 
            } 
            else if(requestType.equalsIgnoreCase("envio")) { 
                    HashMap map = getEnvIOHashMap(ENVIO_SUBSCRIBER_INVALID); 
                    int result = ((Integer)map.get("result")).intValue(); 
                    String message = (String)map.get("message"); 
                    response = getEnvioResponseAsString(result, message); 
            } 
            else if (requestType.equalsIgnoreCase("autodial")) { 
                    response = String.valueOf(AUTO_SUBSCRIBER_INVALID); 
            } 
            else if (requestType.equalsIgnoreCase("easycharge")) { 
                    String message = getECMessage(EC_0_SUBSCRIBER_INVALID, String.valueOf(2)); 
                    response = getEasyChargeResponseAsString(EC_0_SUBSCRIBER_INVALID, message); 
            } 
            else if (requestType.equalsIgnoreCase("mod")) { 
                    response = String.valueOf(MOD_SUBSCRIBER_INVALID); 
            } 
            /*if(requestType.equalsIgnoreCase("USSD")) 
                    status = USSD_SUBSCRIBER_INVALID;
            Tools.logFatalError(_class, method, "RBT:: url null for user " + user);
    	}
		
    	HashMap responseMap = new HashMap();
    	responseMap.put("response", response);
    	responseMap.put("status", String.valueOf(status));
    	return responseMap;
    }
	*/
    
    private String getHttpRequestParam(String key, Map map) {
    	String retVal = null;
    	if(map.containsKey(key)) {
    		String[] values = (String[]) map.get(key);
    		if(values != null)
    			retVal = values[0];
    	}
    	return retVal;
    }
    
    private String getEnvioResponseAsString(int result, String message) { 
    	return "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">" 
        + result 
        + "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>" 
        + message + "</td> <!-- response message (msg) --></tr></table></body></html>"; 
 
    } 
 
    private String getEasyChargeResponseAsString(int result, String message) { 
    	return "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">" 
        + result 
        + "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>" 
        + message + "</td> <!-- response message (msg) --></tr></table></body></html>"; 
    } 
    
    public String disablePressStarIntro(String subscriberID) {
    	Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
        boolean success = RBTDBManager.getInstance().disablePressStarIntro(subscriber); 
        if(success) 
                return SUCCESS; 
        else 
                return FAILURE; 
    } 
    public String enablePressStarIntro(String subscriberID) {
    	Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
        boolean success = RBTDBManager.getInstance().enablePressStarIntro(subscriber); 
        if(success) 
        	return SUCCESS; 
        else 
        	return FAILURE; 
    } 

	public Categories[] getGUIActiveCategories(String circleID, char prepaidYes) {
		return (RBTDBManager.getInstance().getGUIActiveCategories(circleID, prepaidYes));
	}
	
	public String processTNBRequest(String strSubID, boolean bPrepaid, String subClass,
			String subWavFile, boolean blkSMS, String actInfo, boolean ignoreActiveSub) {
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(strSubID);
		strSubID = subscriberP.getSubscriberID();
		String circleID = 	subscriberP.getCircleID();
		if(!(subscriberP.isValidPrefix() && subscriberP.isCanAllow() && !subscriberP.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED)))
			return "Invalid subscriber number " + strSubID;
		String retVal = null;
		RBTDBManager dbManager = RBTDBManager.getInstance();
		Subscriber subscriber = dbManager.getSubscriber(strSubID);
		if(dbManager.isSubActive(subscriber) && ignoreActiveSub)
			 retVal = "Subscriber " + strSubID + " already active. Ignore active is true. So ignoring.";
		else {
			subClass = subClass.substring(0, subClass.indexOf("[")-1);
			SubscriptionClass subClassIns = getSubscriptionClass(subClass);
			
			int subPeriod = -1;
			try {
				subPeriod = RBTDBManager.getSubscriptionPeriod(subClassIns.getSubscriptionPeriod(),
						subClass);
			}
			catch(Exception e) {
				subPeriod = 30;
			}

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, subPeriod-1);
			
			String activtedBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TNB_ACTIVATED_BY", null);
			int catID;
			String catIDStr = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_PROMOTION_CATEGORY_ID", null);
			try {
				catID = Integer.parseInt(catIDStr);
			}
			catch(Exception e) {
				catID = 7;
			}
			if(!dbManager.isSubActive(subscriber))
				subscriber = activateSubscriber(strSubID, activtedBy, null, cal.getTime(), bPrepaid, 0,
					actInfo, subClass, circleID);
				
				/*dbManager.activateSubscriber(strSubID, activtedBy, null, cal.getTime(),
					bPrepaid, actInfo, subClass, m_useSubscriptionManager, false, 0);*/
			
			if(subscriber == null || !dbManager.isSubActive(subscriber))
				retVal = "Internal Error";
			else {
				String selRes = null;
				if(subWavFile != null && !subWavFile.equalsIgnoreCase("null")) {
					selRes = addSelections(strSubID, null, bPrepaid, false, catID, subWavFile,
							null, 1, 0, activtedBy, actInfo, 0, 2359, null, null, null, subscriber
									.subYes(), 0, subClass, false, subscriber,null);
				}
				if(selRes == null)
					retVal = "TNB Request for subscriber " + strSubID + " accepted";
				else
					retVal = "TNB Request for subscriber " + strSubID
							+ " accepted (selection additon failed)";
			}
		}
		return retVal;
	}
	
	public List getTNBPackList() {
		List tnbPackList = new ArrayList();
		String tnbPacks = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "TNB_SUB_CLASSES", null);
		if(tnbPacks != null) {
			StringTokenizer stk = new StringTokenizer(tnbPacks, ",");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				String subClassDet = getActivationClassdetails(token);
				if(subClassDet != null) {
					tnbPackList.add(token + " [" + subClassDet + "]");
				}
			}
		}
		return tnbPackList;
	}
	
	public String getCircleID(String subscriberID) {
		return RBTDBManager.getInstance().getCircleId(subscriberID);
	}
	
	String getCopyInfo(String activationInfo) { 
		if(activationInfo == null || activationInfo.length() <= 0) 
			return null; 
		if( activationInfo.indexOf("|CP:") > -1 && activationInfo.indexOf(":CP|") > -1) { 
			int firstIndex = activationInfo.indexOf("|CP:"); 
			int secondIndex = activationInfo.indexOf(":CP|"); 
			String copyInfo = null; 
			if(firstIndex > -1 && secondIndex > firstIndex) 
				copyInfo = activationInfo.substring(firstIndex+4,secondIndex); 
			return copyInfo; 
		} 
		return null; 
	} 

    public String getSubscriberPrefix(String subscriberID) { 
        int prefixIndex = RBTDBManager.getInstance().getPrefixIndex(); 
        if(subscriberID == null || subscriberID.length() <= prefixIndex) 
        	return subscriberID; 
        return (subscriberID.substring(0, prefixIndex)); 
    } 
    
    public String addSelections(String strSubID, String strCallerID, 
            boolean bPrepaid, boolean changeSubType, int categoryID, 
            String songName, Date endDate, int status, int trialPeriod, 
            String strSelectedBy, String strSelectionInfo, int FromTime, 
            int ToTime, String classType, String mode, String type, 
            String subYes, int maxSelections, String subClass, boolean OptIn, Subscriber subscriber, boolean putInLoop,String selInterval) 
    { 
                 if(songName.equalsIgnoreCase("null")) 
                                 return null; 
        if (endDate == null) 
        { 
            //Calendar endCal = Calendar.getInstance(); 
            //endCal.set(2037, 0, 1); 
            //endDate = endCal.getTime(); 
                        endDate = m_endDate; 
        } 
 
//        if(m_NAVCategoryIDs != null && m_NAVCategoryIDs.contains(""+categoryID)) 
//            status = 85;
        
        if(FromTime <= 23)
        	FromTime = FromTime * 100;
        if(ToTime <= 23)
        	ToTime = ToTime * 100 + 59;
        
        if (strCallerID == null && m_corpChangeSelectionBlock 
                && isCorpSub(strSubID)) 
        { 
            return ("corp"); 
        } 
        else if (categoryID !=1 && !RBTDBManager.getInstance().isSelectionAllowed(subscriber,strCallerID)) {
            	System.out.println(" sub un sub 4");
        	return "ADRBT-Block";
            } 
        else 
        { 
            if (FromTime <= ToTime) 
            { 
                System.out.println(" sub class " + subClass);
            	SubscriptionClass sClass = getSubscriptionClass(subClass); 
                if (sClass != null && sClass.getFreeSelections() > 0 
                        && maxSelections < sClass.getFreeSelections()) 
                    classType = "FREE"; 
                RBTDBManager dbManager = RBTDBManager.getInstance(); 
                boolean inLoop = dbManager.allowLooping() && putInLoop; 
                if(inLoop && !dbManager.moreSelectionsAllowed(strSubID, strCallerID)){ 
                	return null; 
                        }
                System.out.println(" s class " + sClass);
                dbManager.addSubscriberSelections(strSubID, strCallerID, categoryID, songName, 
                                                null, null, endDate, status, strSelectedBy, strSelectionInfo, trialPeriod, 
                                                bPrepaid, changeSubType, m_messagePath, FromTime, ToTime, classType, 
                                                m_useSubscriptionManager, true, mode, type, subYes, "ESIA", true, OptIn, 
                                                inLoop, sClass.getSubscriptionClass(), subscriber,selInterval); 
            } 
        } 
 
        return null; 
    } 

	//added new function by eswar
  	         public String insertBulkSelectionTask(String filename,String actBy,
  	                 String subStrClass,String selStrClass,String actInfo)
  	         {
  	                 System.out.println("Entering into the Rbt Login::RbtSubUnSub");
  	                 return (RBTDBManager.getInstance()
  	                                 .insertBulkSelectionTask(filename,actBy,subStrClass,selStrClass,actInfo));
  	         }
  	 
  	         public RbtBulkSelectionTask[] getBulkSelectionTasks(){
  	                 return (RBTDBManager.getInstance()
  	                                 .getBulkSelectionTasks());
  	         }
  	 
  	         public String getBulkSelectionTaskStatus(String filename){
  	                 return (RBTDBManager.getInstance()
  	                                 .getBulkSelectionTaskStatus(filename));
  	         }
  	 
  	         public String getActivationInfoTask(String filename){
  	                 return (RBTDBManager.getInstance()
  	                                 .getActivationInfoTask(filename));
  	         }
  	         public String deleteBulkSelectionTask(int fileID) {
  	                 return (RBTDBManager.getInstance()
  	                                 .deleteBulkSelectionTask(fileID));
  	         }
  	         public String updateBulkSelectionTaskStatus(String filename,String status) {
  	                 return (RBTDBManager.getInstance()
  	                                 .updateBulkSelectionTaskStatus(filename,status));
  	         }
  	        
  	         
  	         public String updateProcessedTimeForTask(String filename){
  	                 return (RBTDBManager.getInstance()
  	                                 .updateProcessedTimeForTask(filename));
  	         }
  	 
  	         public String updateActivationInfoTask(String filename,String actInfo){
  	                 return (RBTDBManager.getInstance()
  	                                 .updateActivationInfoTask(filename,actInfo));
  	         }
  	 
  	         public String getFileFromFileID(int fileID) {
  	                 return (RBTDBManager.getInstance()
  	                                 .getFileFromFileID(fileID));
  	         }
  	         //end

  	       public File processSelections(String strFile, ArrayList preSubs,
   	             String strActBy, String strActInfo, String strSubClass, String chargeClass)
   	     {
				 FileReader fr = null;
	 	         FileWriter fw = null;
	 	         BufferedReader br = null;
	 	         File statusFile = null;
	 	 
	 	         m_bulkSelectionSuccessCount = m_bulkSelectionFailureCount = 0;
	 	 
	 	         StringBuffer success = null;
	 	         StringBuffer failure = null;
	 	 
	 	         try
	 	         {
	 	             fr = new FileReader(m_filePath + File.separator + strFile);
	 	             br = new BufferedReader(fr);
	 	 
	 	             String strSubID = null;
	 	             String song;
	 	             String subType;
	 	             String period;
	 	             success = new StringBuffer();
	 	             failure = new StringBuffer();
	 	 
	 	             String line = br.readLine();
	 	 
	 	             success.append("Started... \n\n");
	 	 
	 	             while (line != null)
	 	             {
	 	                 
	 	            	 try
	 	            	 {
	 	                 strSubID = song = subType = period = null;
	 	                 line = line.trim();
	 	                 subType = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID");
	 	                 StringTokenizer tokens = new StringTokenizer(line, ",");
	 	                 if (tokens.hasMoreTokens())
	 	                     strSubID = tokens.nextToken().trim();
	 	                 if (tokens.hasMoreTokens())
	 	                     song = tokens.nextToken().trim();
	 	                 if (tokens.hasMoreTokens())
	 	                     subType = tokens.nextToken().trim();
	 	                 if (tokens.hasMoreTokens())
	 	                     period = tokens.nextToken().trim();
	 	 
	 	                 if (preSubs != null)
	 	                 {
	 	                     if (preSubs.contains(strSubID))
	 	                         subType = "prepaid";
	 	                     else
	 	                         subType = "postpaid";
	 	                 }
	 	 
	 	                 //prepaid-postpaid change by gautam
	 	                 if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
	 	                 {
	 	                     if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
	 	                         subType = "prepaid";
	 	                     if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
	 	                         subType = "postpaid";
	 	                 }
	 	 
	 	                 if (strSubID != null && song != null && subType != null)
	 	                 {
	                        com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = Processor.getSubscriber(strSubID);
     						strSubID = subscriber.getSubscriberID();
							String circleID = 	subscriber.getCircleID();
							
							if (subscriber.isValidPrefix() && subscriber.isCanAllow() && !subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
	 	                         addBulkSelections(strSubID, song, subType, period,
	 	                                           strActBy, strActInfo, success,
	 	                                           failure, strSubClass, chargeClass, circleID);
	 	                     else
	 	                     {
	 	                         failure.append(strSubID
	 	                                 + " is not a valid subscriber.\n");
	 	                         m_bulkSelectionFailureCount++;
	 	                     }
	 	                 }
	 	                 else
	 	                 {
	 	                     if (strSubID == null)
	 	                     {
	 	 
	 	                     }
	 	                     else if (song == null || subType == null)
	 	                         failure.append("Song or subcriber type missing for "
	 	                                 + strSubID + ".\n");
	 	 
	 	                     m_bulkSelectionFailureCount++;
							 }
					 }
					 catch(Exception e)
					 {
						 failure.append(strSubID + " failed bcoz of exception.\n");
                         m_bulkSelectionFailureCount++;
						 logger.error("", e);
						 logger.info("Exception while processing subscriber "+ strSubID + " : " + e);
						  logger.info("Exception while processing subscriber "+ strSubID + " : " + e.getMessage());
	 	                 }
	 	                 line = br.readLine();
	 	             }
	 	             success.append("\n\n");
	 	 
	 	             failure.append("\nEnded... \n\n");
	 	 
	 	             failure.append("Processing Statistics - Success : "
	 	                     + m_bulkSelectionSuccessCount + "   Failure : "
	 	                     + m_bulkSelectionFailureCount);
	 	 
	 	             statusFile = new File(m_filePath
	 	                     + File.separator
	 	                     + "BulkSelection-"
	 	                     + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
	 	                             .getInstance().getTime()) + ".txt");
	 	 
	 	             fw = new FileWriter(statusFile);
	 	             fw.write(success.toString());
	 	             fw.write(failure.toString());
	 	         }
	 	         catch (Exception e)
	 	         {
	 	             e.printStackTrace();
	 	         }
	 	         finally
	 	         {
	 	             try
	 	             {
	 	                 br.close();
	 	                 fr.close();
	 	                 fw.close();
	 	             }
	 	             catch (Exception e)
	 	             {
	 	 
	 	             }
	 	         }
			 return statusFile;
     }	     

  	 public File processSelectionsTask(String strFile, ArrayList preSubs,
            String strActBy, String strActInfo, String strSubClass, String chargeClass)
	{
		FileReader fr = null;
        FileWriter fw = null;
        BufferedReader br = null;
        File statusFile = null;

        m_bulkSelectionSuccessCount = m_bulkSelectionFailureCount = 0;

        StringBuffer success = null;
        StringBuffer failure = null;

        if(!getBulkSelectionTaskStatus(strFile).equals("B"))
         {
                 FileWriter fw1 = null;
                 File statusFile1 = null;

                 try{
                         StringBuffer status = new StringBuffer("File Cannot be processed Please refresh the page");

                         statusFile1 = new File(m_filePath
             + File.separator
             + "BulkSelection-"
             + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
                     .getInstance().getTime()) + ".txt");
                         fw1 = new FileWriter(statusFile1);
                         fw1.write(status.toString());
                 } catch(IOException e1) {
                         e1.printStackTrace();
                 }
                 finally {
                         try
                         {
                                 fw1.close();
                         }
                         catch (Exception e2)
                         {

                         }
                 }
                 return statusFile1;
         }
         updateBulkSelectionTaskStatus(strFile,"p");
        try
        {
            fr = new FileReader(m_filePath + File.separator + strFile);
            br = new BufferedReader(fr);

            String strSubID;
            String song;
            String subType;
            String period;
            success = new StringBuffer();
            failure = new StringBuffer();

            String line = br.readLine();

            success.append("Started... \n\n");

            while (line != null)
            {
                strSubID = song = subType = period = null;
                line = line.trim();
   				subType = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID");
				StringTokenizer tokens = new StringTokenizer(line, ",");
                if (tokens.hasMoreTokens())
                    strSubID = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    song = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    subType = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    period = tokens.nextToken().trim();

                if (preSubs != null)
                {
                    if (preSubs.contains(strSubID))
                        subType = "prepaid";
                    else
                        subType = "postpaid";
                }

                //prepaid-postpaid change by gautam
                if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
                {
                    if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
                        subType = "prepaid";
                    if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
                        subType = "postpaid";
                }

                if (strSubID != null && song != null && subType != null)
                {
                    com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = Processor.getSubscriber(strSubID);
					strSubID = subscriber.getSubscriberID();
					String circleID = 	subscriber.getCircleID();
					if (subscriber.isValidPrefix() && subscriber.isCanAllow() && !subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
                        addBulkSelections(strSubID, song, subType, period,
                                          strActBy, strActInfo, success,
                                          failure, strSubClass, chargeClass, circleID);
                    else
                    {
                        failure.append(strSubID
                                + " is not a valid subscriber.\n");
                        m_bulkSelectionFailureCount++;
                    }
                }
                else
                {
                    if (strSubID == null)
                    {

                    }
                    else if (song == null || subType == null)
                        failure.append("Song or subcriber type missing for "
                                + strSubID + ".\n");

                    m_bulkSelectionFailureCount++;
                }
                line = br.readLine();
            }
            success.append("\n\n");

            failure.append("\nEnded... \n\n");

            failure.append("Processing Statistics - Success : "
                    + m_bulkSelectionSuccessCount + "   Failure : "
                    + m_bulkSelectionFailureCount);

            statusFile = new File(m_filePath
                    + File.separator
                    + "BulkSelection-"
                    + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
                            .getInstance().getTime()) + ".txt");

            fw = new FileWriter(statusFile);
            fw.write(success.toString());
            fw.write(failure.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
                fw.close();
            }
            catch (Exception e)
            {

            }
        }
		updateBulkSelectionTaskStatus(strFile,"P");
		updateProcessedTimeForTask(strFile);
        return statusFile;
	}

		public String getXMLTag(String message,String serviceKey)
	{
		String value = null;
		if(message != null && message.indexOf("<ROOT>") > -1)
			return message;
		if(message != null && ((message.indexOf("SUCCESS") >-1) || (message.indexOf("ACTIVE_POSTPAID")  > -1) || (message.indexOf("ACTIVE_PREPAID") > -1)))
		{
			value =  "<ROOT><SERVICE><SVCID> RBT_ACT_"+serviceKey+"</SVCID><SVCDESC>RBT</SVCDESC><STATUS>ACTIVE</STATUS></SERVICE></ROOT>";
		}	
		else
			value = "<ROOT><SERVICE><SVCID> RBT_ACT_"+serviceKey+"</SVCID><SVCDESC>RBT</SVCDESC><STATUS>ERROR</STATUS></SERVICE></ROOT>";
		return value;
}

		
		// added to accomodate extra info
		public HashMap getExtraInfoMap(String subscriberId, Subscriber subscriber){
			
//			Subscriber subscriber=RBTDBManager.getInstance().getSubscriber(subscriberId);
			if (subscriber!=null)
				return RBTDBManager.getInstance().getExtraInfoMap(subscriber);
			else 
				return null;
			
		}
		
		 public HashMap getActiveSubscriberDownloads(String subscriberId){
			 
				subscriberId = subID(subscriberId);
				SubscriberDownloads[] subscriberDownloads=RBTDBManager.getInstance().getActiveSubscriberDownloads(subscriberId);
				HashMap downloadMap=new HashMap();
				ClipMinimal clip = null;
				String clipExpiryDate = "-";
				ArrayList promoIdList = new ArrayList();
				ArrayList categoryIdList = new ArrayList();
				ArrayList ClipPromoIdList = new ArrayList();
				ArrayList categoryTypeList = new ArrayList();
				ArrayList classTypeList = new ArrayList();
				ArrayList endTimeList = new ArrayList();
				//ArrayList startTimeList = new ArrayList();
				ArrayList setTimeList = new ArrayList();
				ArrayList selectedByList = new ArrayList();
				ArrayList downloadStatusList = new ArrayList();
				ArrayList clipExpiryDateList = new ArrayList();
				char downloadStatus = '-';
				String status=null;
				try{
				if(subscriberDownloads!=null && subscriberDownloads.length>0){
				
					clip = getClipRBT(subscriberDownloads[0].promoId());
					clipExpiryDate = "-";
					if(clip != null)
					{
						if(clip.getEndTime() != null)
						{
							Date expiryDate = clip.getEndTime();
							SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
							clipExpiryDate = sdf.format(expiryDate);
						}
					
						ClipMinimal c = getClipRBT(subscriberDownloads[0].promoId());
						if(c != null)
							promoIdList.add(c.getClipName());
						downloadMap.put("Clip Name", promoIdList);
						
						if(c != null)
							ClipPromoIdList.add(c.getPromoID());
						downloadMap.put("Promo ID", ClipPromoIdList);
						
						Categories category = (Categories)(RBTDBManager.getInstance().getCategory(subscriberDownloads[0].categoryID(), getCircleID(subscriberId), 'b'));
						if(category != null)
							categoryIdList.add(category.name());
						downloadMap.put("Category Name", categoryIdList);
					
						categoryTypeList.add(subscriberDownloads[0].categoryType());
						downloadMap.put("Category Type", categoryTypeList);
					
						classTypeList.add(subscriberDownloads[0].classType());
						downloadMap.put("Class Type", classTypeList);
						
						endTimeList.add(subscriberDownloads[0].endTime());
						downloadMap.put("End Time", endTimeList);
						
					//	startTimeList.add(subscriberDownloads[0].startTime());
					//	downloadMap.put("Start Time", startTimeList);
					
						setTimeList.add(subscriberDownloads[0].setTime());
						downloadMap.put("Set Time", setTimeList);
					
						selectedByList.add(subscriberDownloads[0].selectedBy());
						downloadMap.put("Selected By", selectedByList);
					
						downloadStatus=subscriberDownloads[0].downloadStatus();
						
						if (downloadStatus=='p'){
							status="Download Activation Pending";
						}
					
						if (downloadStatus=='n'){
							status="Download to be Activated";
						}
						if (downloadStatus=='y'){
							status="Download Activated";
						}
						if (downloadStatus=='d'){
							status="Download to be DeActivated";
						}
						if (downloadStatus=='s'){
							status="Download Deactivation Pending";
						}
						if (downloadStatus=='x'){
							status="Download Deactivated";
						}
					
						if (downloadStatus=='b'){
							status="Download Bookmark";
						}
						if (downloadStatus=='e'){
							status="Download Activation Error";
						}
						if (downloadStatus=='f'){
							status="Download Deactivation Error";
						}
						if (downloadStatus=='w'){
							status="Download Base Activation Pending";
						}
						downloadStatusList.add(status);
						downloadMap.put("Download Status", downloadStatusList);
					
						if(m_showClipExpiryDateInViewSubscrberDetails)
						{
							clipExpiryDateList.add(clipExpiryDate);
							downloadMap.put("CLIP EXPIRY DATE", clipExpiryDateList);
						}
					}
					
					for (int i=1;i<subscriberDownloads.length;i++){
						
						clip = getClipRBT(subscriberDownloads[i].promoId());
						clipExpiryDate = "-";
						if(clip != null)
						{
							if(clip.getEndTime() != null)
							{
								Date expiryDate = clip.getEndTime();
								SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
								clipExpiryDate = sdf.format(expiryDate);
							}
						
							promoIdList = (ArrayList) downloadMap.get("Clip Name");
							ClipMinimal c = getClipRBT(subscriberDownloads[i].promoId());
							if(c != null)
								promoIdList.add(c.getClipName());
							downloadMap.put("Clip Name", promoIdList);
							
							promoIdList = (ArrayList) downloadMap.get("Promo ID");
							if(c != null)
								promoIdList.add(c.getPromoID());
							downloadMap.put("Promo ID", promoIdList);
							
							categoryIdList = (ArrayList) downloadMap.get("Category Name");
							Categories category = (Categories)(RBTDBManager.getInstance().getCategory(subscriberDownloads[i].categoryID(), getCircleID(subscriberId), 'b'));
							if(category != null)
								categoryIdList.add(category.name());
							downloadMap.put("Category Name", categoryIdList);
							
							categoryTypeList =  (ArrayList) downloadMap.get("Category Type");
							categoryTypeList.add(subscriberDownloads[i].categoryType());
							downloadMap.put("Category Type", categoryTypeList);
						
							classTypeList = (ArrayList) downloadMap.get("Class Type");
							classTypeList.add(subscriberDownloads[i].classType());
							downloadMap.put("Class Type", classTypeList);
						
						
							endTimeList = (ArrayList) downloadMap.get("End Time");
							endTimeList.add(subscriberDownloads[i].endTime());
							downloadMap.put("End Time", endTimeList);
							
						//	startTimeList = (ArrayList) downloadMap.get("Start Time");
						//	startTimeList.add(subscriberDownloads[i].startTime());
						//	downloadMap.put("Start Time", startTimeList);
						
							setTimeList = (ArrayList) downloadMap.get("Set Time");
							setTimeList.add(subscriberDownloads[i].setTime());
							downloadMap.put("Set Time", setTimeList);
						
							selectedByList = (ArrayList) downloadMap.get("Selected By");
							selectedByList.add(subscriberDownloads[i].selectedBy());
							downloadMap.put("Selected By", selectedByList);
						
							downloadStatusList = (ArrayList) downloadMap.get("Download Status");
							downloadStatus=subscriberDownloads[i].downloadStatus();
							if (downloadStatus=='p'){
								status="Download Activation Pending";
							}	
						
							if (downloadStatus=='n'){
								status="Download to be Activated";
							}
							if (downloadStatus=='y'){
									status="Download Activated";
							}
							if (downloadStatus=='d'){
								status="Download to be DeActivated";
							}		
							if (downloadStatus=='s'){
								status="Download Deactivation Pending";
							}
							if (downloadStatus=='x'){
								status="Download Deactivated";
							}	
						
							if (downloadStatus=='b'){
								status="Download Bookmark";
							}
							if (downloadStatus=='e'){
								status="Download Activation Error";
							}	
							if (downloadStatus=='f'){
								status="Download Deactivation Error";
							}
							if (downloadStatus=='w'){
								status="Download Base Activation Pending";
							}
							downloadStatusList.add(status);
							downloadMap.put("Download Status", downloadStatusList);
						
							if(m_showClipExpiryDateInViewSubscrberDetails)
							{
								clipExpiryDateList = (ArrayList) downloadMap.get("CLIP EXPIRY DATE");
								clipExpiryDateList.add(clipExpiryDate);
								downloadMap.put("CLIP EXPIRY DATE", clipExpiryDateList);
							}
						}
					}
				
				}
				}
				catch(Exception e){
					
					System.out.println(" exception in get active downloads message " + e.getMessage());
					e.printStackTrace();
				}
				
				return downloadMap;
				
			}
		
		 
		 public HashMap getDeactiveSubscriberDownloads(String subscriberId){
				
			 
			 	System.out.println(" in get deac dl.. ");
			 	subscriberId = subID(subscriberId);
				SubscriberDownloads[] subscriberDownloads=RBTDBManager.getInstance().getDeactiveSubscriberDownloads(subscriberId);
				HashMap downloadMap=new HashMap();
				System.out.println("sub downloads " + subscriberDownloads);
				try{
				if(subscriberDownloads!=null && subscriberDownloads.length>0){
				
					
					ArrayList promoIdList = new ArrayList();
					ClipMinimal c = getClipRBT(subscriberDownloads[0].promoId());
					if(c != null)
						promoIdList.add(c.getClipName());
					downloadMap.put("Clip Name", promoIdList);
					
					promoIdList = new ArrayList();
					if(c != null)
						promoIdList.add(c.getPromoID());
					downloadMap.put("Promo ID", promoIdList);
					
					ArrayList categoryIdList = new ArrayList();
					Categories category = (Categories)(RBTDBManager.getInstance().getCategory(subscriberDownloads[0].categoryID(), getCircleID(subscriberId), 'b'));
					if(category != null)
						categoryIdList.add(category.name());
					downloadMap.put("Category Name", categoryIdList);					
					
					ArrayList categoryTypeList = new ArrayList();
					categoryTypeList.add(subscriberDownloads[0].categoryType());
					downloadMap.put("Category Type", categoryTypeList);
					
					ArrayList classTypeList = new ArrayList();
					classTypeList.add(subscriberDownloads[0].classType());
					downloadMap.put("Class Type", classTypeList);
					
					ArrayList endTimeList = new ArrayList();
					endTimeList.add(subscriberDownloads[0].endTime());
					downloadMap.put("End Time", endTimeList);
					
				//	ArrayList startTimeList = new ArrayList();
				//	startTimeList.add(subscriberDownloads[0].startTime());
				//	downloadMap.put("Start Time", startTimeList);
					
					ArrayList setTimeList = new ArrayList();
					setTimeList.add(subscriberDownloads[0].setTime());
					downloadMap.put("Set Time", setTimeList);
					
					ArrayList selectedByList = new ArrayList();
					selectedByList.add(subscriberDownloads[0].selectedBy());
					downloadMap.put("Selected By", selectedByList);
					
					ArrayList downloadStatusList = new ArrayList();
					char downloadStatus=subscriberDownloads[0].downloadStatus();
					String status=null;
					if (downloadStatus=='p'){
						status="Download Activation Pending";
					}
					
					if (downloadStatus=='n'){
						status="Download to be Activated";
					}
					if (downloadStatus=='y'){
						status="Download Activated";
					}
					if (downloadStatus=='d'){
						status="Download to be Deactivated";
					}
					if (downloadStatus=='s'){
						status="Download Deactivation Pending";
					}
					if (downloadStatus=='x'){
						status="Download Deactivated";
					}
					
					if (downloadStatus=='b'){
						status="Download Bookmark";
					}
					if (downloadStatus=='e'){
						status="Download Activation Error";
					}
					if (downloadStatus=='f'){
						status="Download Deactivation Error";
					}
					if (downloadStatus=='w'){
						status="Download Base Activation Pending";
					}
					downloadStatusList.add(status);
					downloadMap.put("Download Status", downloadStatusList);
					
					
					for (int i=1;i<subscriberDownloads.length;i++){
						
						
						promoIdList = (ArrayList) downloadMap.get("Clip Name");
						c = getClipRBT(subscriberDownloads[i].promoId());
						if(c != null)
							promoIdList.add(c.getClipName());
						downloadMap.put("Clip Name", promoIdList);
						
						promoIdList = (ArrayList) downloadMap.get("Promo ID");
						if(c != null)
							promoIdList.add(c.getPromoID());
						downloadMap.put("Promo ID", promoIdList);
						
						categoryIdList = (ArrayList) downloadMap.get("Category Name");
						category = (Categories)(RBTDBManager.getInstance().getCategory(subscriberDownloads[i].categoryID(), getCircleID(subscriberId), 'b'));
						if(category != null)
							categoryIdList.add(category.name());
						downloadMap.put("Category Name", categoryIdList);

						
						categoryTypeList =  (ArrayList) downloadMap.get("Category Type");
						categoryTypeList.add(subscriberDownloads[i].categoryType());
						downloadMap.put("Category Type", categoryTypeList);
						
						classTypeList = (ArrayList) downloadMap.get("Class Type");
						classTypeList.add(subscriberDownloads[i].classType());
						downloadMap.put("Class Type", classTypeList);
						
						
						endTimeList = (ArrayList) downloadMap.get("End Time");
						endTimeList.add(subscriberDownloads[i].endTime());
						downloadMap.put("End Time", endTimeList);
						
					//	startTimeList = (ArrayList) downloadMap.get("Start Time");
					//	startTimeList.add(subscriberDownloads[i].startTime());
					//	downloadMap.put("Start Time", startTimeList);
						
						setTimeList = (ArrayList) downloadMap.get("Set Time");
						setTimeList.add(subscriberDownloads[i].setTime());
						downloadMap.put("Set Time", setTimeList);
						
						selectedByList = (ArrayList) downloadMap.get("Selected By");
						selectedByList.add(subscriberDownloads[i].selectedBy());
						downloadMap.put("Selected By", selectedByList);
						
						downloadStatusList = (ArrayList) downloadMap.get("Download Status");
						downloadStatus=subscriberDownloads[i].downloadStatus();
						if (downloadStatus=='p'){
							status="Download Activation Pending";
						}
						
						if (downloadStatus=='n'){
							status="Download to be Activated";
						}
						if (downloadStatus=='y'){
							status="Download Activated";
						}
						if (downloadStatus=='d'){
							status="Download to be DeActivated";
						}
						if (downloadStatus=='s'){
							status="Download Deactivation Pending";
						}
						if (downloadStatus=='x'){
							status="Download Deactivated";
						}
						
						if (downloadStatus=='b'){
							status="Download Bookmark";
						}
						if (downloadStatus=='e'){
							status="Download Activation Error";
						}
						if (downloadStatus=='f'){
							status="Download Deactivation Error";
						}
						if (downloadStatus=='w'){
							status="Download Base Activation Pending";
						}
						downloadStatusList.add(status);
						downloadMap.put("Download Status", downloadStatusList);
						
					}
				
				}
				}
				catch(Exception e){
					
					System.out.println(" exception in get deactive downloads message " + e.getMessage());
					e.printStackTrace();
				}
				System.out.println(" hash map is " + downloadMap);
				return downloadMap;
				
			}
		 
		 
			 public String canBeGifted(String subscriberId,String callerId,String contentID) {
	                 return (RBTDBManager.getInstance()
	                                 .canBeGifted(subscriberId, callerId, contentID));
	         }
			
			
			 public StringBuffer addGift(String giftee,String gifter,String mode,String contentId){
				 RBTChannelHelper rch=null;
				try {
					rch = new RBTChannelHelper();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (rch!=null){
				 return (rch.addGift(giftee, gifter, mode, contentId));
				 }
				 return null;
			 }
			 
			 
			 public long getNoOfDaysLeftPrompt(String subscriberID) {
		logger.info("entering....");
		long noOfDaysleftForDeactivation = -1;
		if (subMgrUrlForDeactivationDaysLeft == null) {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON",
					"SUBMGR_URL_FOR_DEACT_DAYS_LEFT");
			if (param != null && param.getValue() != null && subscriberID != null) {
				subMgrUrlForDeactivationDaysLeft = param.getValue();
			} else {
				logger.info("subMgrUrlForDeactivationDaysLeft is not configured in parameters table");
			}
		}
		if (subMgrUrlForDeactivationDaysLeft != null) {
			String strURL = subMgrUrlForDeactivationDaysLeft + "&msisdn="
					+ subscriberID + "&output=xml";
			StringBuffer responseString = new StringBuffer();
			Integer statusCode = new Integer("0");
			boolean reply = false;
			// get a XML reply through a http hit to subMgr URL
			reply = Tools.callURL(strURL, statusCode, responseString, false,
					null, 80);
			if (reply && responseString != null && responseString.length() > 0
					&& (responseString.toString().indexOf("error") == -1)
					&& (responseString.toString().indexOf("ERROR") == -1)
					&& (responseString.toString().indexOf("Error") == -1)) {

				String nextChargingDate = null;
				nextChargingDate = parseXmlString(responseString.toString());
				if (nextChargingDate != null) {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date deactivationDate = null;
					try {
						deactivationDate = sdf.parse(nextChargingDate);
						Date currDate = Calendar.getInstance().getTime();
						if (deactivationDate.after(currDate)) {
							long timeDiff = deactivationDate.getTime()
									- currDate.getTime();
							if (timeDiff != 0) {
								if (timeDiff > 0) {
									noOfDaysleftForDeactivation = (timeDiff / (1000 * 60 * 60 * 24));
								}
							} else {
								noOfDaysleftForDeactivation = 0;
							}
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
		logger.info("returning with noOfDaysleftForDeactivation=="
						+ noOfDaysleftForDeactivation);
		// ListSubscriptions?user=<USER>&pass=<PASS>
		// ---subMgrUrlForDeactivationDaysLeft must contains user and pass
		// parameter
		return noOfDaysleftForDeactivation;
	}

	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	private static String parseXmlString(String tempXML) {
		String returnString = null;
		Document dom = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			// String
			// tempXML="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Personnel><ROOT><SERVICE><SVCID>KEYWORD</SVCID><SVCDESC>DESC</SVCDESC><STATUS>STATUS</STATUS><NEXTCHARGEDATE>yyyy-MM-dd HH:mm:ss</NEXTCHARGEDATE></SERVICE></ROOT></Personnel>"
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					tempXML.getBytes());
			dom = db.parse(byteArrayInputStream);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		String tempReturnStr = parseDocument(dom);
		if (tempReturnStr != null) {
			returnString = tempReturnStr;
		} else {
			logger.info("tempReturnStr==null");
		}
		return returnString;
	}

	private static String parseDocument(Document dom) {
		Element docEle = dom.getDocumentElement();
		String nextchargingdate = null;
		// "nextChargingDate" should be of format "yyyy-MM-dd HH:mm:ss"
		NodeList nl = docEle.getElementsByTagName("SERVICE");
		if (nl != null && nl.getLength() > 0) {
			logger.info("nl != null && nl.getLength() > 0");
			Element el = (Element) nl.item(0);
			nextchargingdate = getTextValue(el, "NEXTCHARGEDATE");
			logger.info("nextchargingdate==" + nextchargingdate);
		}
		logger.info("returning nextchargingdate==" + nextchargingdate);
		return nextchargingdate;
	}

			
	public void processSMSRedirect(HttpServletRequest request) {
		String method = "processSMSRedirect";
		Date requestTime = new Date();
		String strIP = request.getRemoteAddr();
		String subID = request.getParameter("SUB_ID");
		String smsText = request.getParameter("SMS_TEXT");
		String redirectURL = null;
		String urlKey = null;
		String status = null;
		String promoURL = null;
		String promoToolResp = null;
		try {
			if(smsText == null) {
				status = "SMS text null";
				return;
			}
			if(subID == null) {
				status = "sub id null";
				return;
			}
			if (!isValidIP(strIP)) {
				logger.info("RBT::Request from invalid IP - " + strIP);
				status = "invalid ip";
				return;
			}
			smsText = smsText.trim().toLowerCase();
			if (_smsRedirectKeywordMap.size() > 1) {
				Iterator<String> itr = _smsRedirectKeywordMap.keySet().iterator();
				while (itr.hasNext()) {
					String keyword = itr.next();
					int index = smsText.indexOf(keyword);
					if (index == -1)
						continue;
					if (((index + keyword.length()) < smsText.length())
							&& (smsText.charAt(index + keyword.length()) != ' '))
						continue;
					if (index > 0 && ((index + keyword.length()) == smsText.length())
							&& smsText.charAt(index - 1) != ' ')
						continue;
					logger.info("SMS::match - <" + smsText + "> & <" + keyword
							+ ">");
					urlKey = _smsRedirectKeywordMap.get(keyword);
					redirectURL = _smsRedirectURLMap.get(urlKey);
					break;
				}
			}
			if(redirectURL == null)
				urlKey = "DEFAULT";
			
			String response = null;
			try {
				redirectURL = _smsRedirectURLMap.get(urlKey);
				redirectURL = redirectURL.replaceAll("%SUB_ID%", subID);
				redirectURL = redirectURL.replaceAll("%SMS_TEXT%", smsText);
				HttpParameters httpParams = new HttpParameters();
				httpParams.setUrl(redirectURL);
				httpParams.setConnectionTimeout(500);
				response = RBTHTTPProcessing.postFile(httpParams, null, null);
				if(response != null)
					response = response.trim();
				logger.info("RBT::url -> " + redirectURL + ", response -> "
						+ response);
				
				if (response != null && !response.equals("")
						&& _smsRedirectSenderMap.containsKey(urlKey)) {
					if(!subID.startsWith(_countryPrefix))
						subID = _countryPrefix + subID;
					promoURL = _smsRedirectPromotoolURL.replaceAll("%SUB_ID%", subID);
					promoURL = promoURL.replaceAll("%SENDER_ID%",
							_smsRedirectSenderMap.get(urlKey));
					response = Tools.decodeHTML(response);
					promoURL = promoURL.replaceAll("%RESPONSE%", response);
					HttpParameters promoToolHTTPParams = new HttpParameters();
					promoToolHTTPParams.setUrl(promoURL);
					promoToolHTTPParams.setConnectionTimeout(500);
					promoToolResp = RBTHTTPProcessing.doGetRequest(promoToolHTTPParams, null);
					if(promoToolResp != null)
						promoToolResp = promoToolResp.trim();
					logger.info("RBT::promo url -> " + promoURL
							+ ", response -> " + promoToolResp);
				}
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		finally {
			Date responseTime = new Date();
			WriteSDR.addToAccounting(_smsRedirectSDRPath, _smsRedirectRotationSize, "SMS_REDIRECT",
					subID, smsText, redirectURL, status, _formatter.format(requestTime),
					(responseTime.getTime() - requestTime.getTime()) + "", strIP, promoURL,
					promoToolResp);
		}
	}
	
	public String getHuaweiStatusForSub(String subscriberID) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		if(subscriber == null)
			return "Deactive";
		return rbtDBManager.getBackEndSubscriberStatus(subscriberID, subscriber.prepaidYes());
	}
	
	public String processFeed(String subscriberID, String status, String pass, String actBy,
			String actInfo, boolean isPrepaid, boolean cricketPass) {
		String method = "processFeed";
		logger.info("RBT::subscriberID-" + subscriberID + ",status-" + status
				+ ",pass-" + pass + ",actBy-" + actBy + ",actInfo-" + actInfo + ",isPrepaid-"
				+ isPrepaid + ",cricketPass-" + cricketPass);
		if(status == null || (!status.equalsIgnoreCase("ON") && !status.equalsIgnoreCase("OFF")))
			return "INVALID STATUS " + status;
		
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (isCorpSub(subscriberID) && m_corpChangeSelectionBlock)
			return "CORPORATE SUBSCRIBER CANNOT CHANGE SELECTION";
		subscriberID = subID(subscriberID);
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		int rbtType = 0;
		if(subscriber != null)
			rbtType = subscriber.rbtType();

		if (status != null && status.equalsIgnoreCase("OFF")) {
			SubscriberStatus[] subscriberStatus = rbtDBManager.getSubscriberRecords(subscriberID,
					"GUI", m_useSubscriptionManager, rbtType);
			boolean cricket = false;

			if (subscriberStatus != null) {
				int index = 0;
				while (index < subscriberStatus.length) {
					if (subscriberStatus[index].status() == 90) {
						cricket = true;
						rbtDBManager.deactivateSubscriberRecords(subscriberID, null,
								90, 0, 2359, m_useSubscriptionManager, actBy, rbtType);
						break;
					}
					index++;
				}
			}

			if (!cricket)
				return "NO CIRCKET SELECTION TO DELETE";
			return "CRICKET SELECTION REMOVED";
		}

		// If control comes here, implies the wants to add a feed selection.
		
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(subscriberID);
		subscriberID = subscriberP.getSubscriberID();
		String circleID = 	subscriberP.getCircleID();
		
		if (subscriber == null || rbtDBManager.isSubDeactive(subscriber)) {
			subscriber = activateSubscriber(subscriberID, actBy, null, isPrepaid, 0,
					actInfo, "DEFAULT",circleID);
		}

		if(subscriber == null || !rbtDBManager.isSubActive(subscriber))
			return "SUBSCRIBER NOT ACTIVE";

		//Changed by Sreekar to support pack upgradation
		if (!rbtDBManager.allowFeedUpgrade()
				&& isSubAlreadyActiveOnStatus(subscriberID, null, 90, rbtType)) {
			return "ALREADY HAS FEED";
		}
		
		FeedStatus m_feedStatus = rbtDBManager.getFeedStatus("CRICKET");
		ArrayList<String> m_cricketSubKey = null;
		if (m_feedStatus != null)
			m_cricketSubKey = Tools.tokenizeArrayList(m_feedStatus.subKeyword(), ",");
		
		logger.info("RBT::m_cricketSubKey-" + m_cricketSubKey);

		if (!cricketPass)
			pass = null;
		else if (pass == null || !m_cricketSubKey.contains(pass.toLowerCase()))
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

		if (cricketPass) {
			// there is a match of requested pass
			FeedSchedule schedule = getCricketClass(pass);

			// there is no current match
			if (schedule == null)
				schedule = getNextCricketSchedule(pass, m_cricketInterval);

			if (schedule == null) {
				return "NO FEED";
			} else {
				//Added by Sreekar to support pack upgradation
				if (rbtDBManager.allowFeedUpgrade()) {
					SubscriberStatus cricSel = rbtDBManager.getActiveSubscriberRecord(subscriberID,
							null, 90, 0, 2359);
					if (cricSel != null
							&& (cricSel.endTime().after(schedule.endTime()) || cricSel.endTime()
									.equals(schedule.endTime())))
						return "ALREADY HAS FEED";
				}

				String classType = schedule.classType();
				if (subscriber != null) {
					boolean OptIn = false;
					if (subscriber.activationInfo() != null
							&& subscriber.activationInfo().indexOf(":optin:") != -1)
						OptIn = true;

					if (!rbtDBManager.addSubscriberSelections(subscriberID, null, 10, feedFile,
							null, schedule.startTime(), schedule.endTime(), 90, actBy, actInfo, 0,
							isPrepaid, false, m_messagePath, 0, 2359, classType, m_useSubscriptionManager,
							true, null, null, subscriber.subYes(), null, true, OptIn, false,
							subscriber.subscriptionClass(), subscriber, null))
						return "NOT SET";
				}
				return "SUCCESS";
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

				if(!rbtDBManager.addSubscriberSelections(subscriberID, null, 10, feedFile,
						null, null, endDate, 90, actBy, actInfo, 0,
						isPrepaid, false, m_messagePath, 0, 2359, null,
						m_useSubscriptionManager, true, null, null, subscriber.subYes(), null, true, OptIn,
						false, subscriber.subscriptionClass(), subscriber,null))
						return "NOT SET";
			}
			return "SUCCESS";
		}
	}

	private FeedSchedule getCricketClass(String pass) {
		return (RBTDBManager.getInstance().getFeedSchedule("CRICKET", pass));
	}
	
	private FeedSchedule getNextCricketSchedule(String pass, int interval) {
		FeedSchedule[] schedule = RBTDBManager.getInstance().getFeedSchedules("CRICKET",
				pass, interval);
		if (schedule == null || schedule.length == 0)
			return null;
		else
			return (schedule[0]);
	}
	
	public FeedSchedule[] getActiveFeedSchedule() {
		return RBTDBManager.getInstance().getActiveFeedSchedule();
	}
	
	public FeedSchedule[] getActiveFeedSchedule(int interval) {
		return RBTDBManager.getInstance().getActiveFeedSchedule(interval);
	}
	
	public FeedSchedule[] getAvailableFeedSchedule() {
		return RBTDBManager.getInstance().getAvailableFeedSchedule(m_cricketInterval);
	}
	
	public String[] getAvailablePacks() {
		FeedSchedule[] activeSchedules = getAvailableFeedSchedule();
		if(activeSchedules == null)
			return null;
		ArrayList<String> cricPacks = new ArrayList<String>();
		for(int i = 0; i < activeSchedules.length; i++) {
			cricPacks.add(activeSchedules[i].subKeyword() + ", "
					+ getCricketPackString(activeSchedules[i].subKeyword()) + "["
					+ activeSchedules[i].name() + "](" + _currencyStr
					+ getChargeAmount(activeSchedules[i].classType()) + ")");
		}
		return cricPacks.toArray(new String[0]);
	}
	
	private String getCricketPackString(String subKeyWord) {
		if(subKeyWord.equals("SP"))
			return "Series Pass";
		else if(subKeyWord.equals("DP"))
			return "Day Pass";
		else if(subKeyWord.equals("MP"))
			return "Match Pass";
		return subKeyWord;
	}

	private boolean isSubAlreadyActiveOnStatus(String strSubID, String callerID, int status,
			int rbtType) {
		SubscriberStatus subStatus = RBTDBManager.getInstance().getActiveSubscriberRecord(
				strSubID, callerID, status, 0, 2359, rbtType);
		if (subStatus != null)
			return true;
		return false;
	}
}