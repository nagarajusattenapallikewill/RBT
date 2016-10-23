/**
 * OnMobile Ring Back Tone
 * 
 * $Author: balachandar.p $
 * $Id: SmsProcessor.java,v 1.600 2015/06/18 11:52:44 balachandar.p Exp $
 * $Revision: 1.600 $
 * $Date: 2015/06/18 11:52:44 $
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTLotteryEntries;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralBlackListTable;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph.PromotionStatus;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraphDao;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.DTWebsiteEventLogger;
import com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction;
import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceActionFactory;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSText;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ValidateNumberRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.implementation.RBTProtocolGenerator;
import com.onmobile.reporting.framework.capture.api.Configuration;
import com.onmobile.reporting.framework.capture.api.ReportingException;

/**
 * 
 * @author vinayasimha.patil
 */
public class SmsProcessor extends Processor {
	int STATUS_SUCCESS = 1;
	int STATUS_ALREADY_ACTIVE = 2;
	int STATUS_ALREADY_CANCELLED = 3;
	int STATUS_NOT_AUTHORIZED = 4;
	int STATUS_TECHNICAL_FAILURE = 5;

	public static final int TYPE_RBT = 0;
	public static final int TYPE_SRBT = 1;
	public static final int TYPE_RRBT = 2;
	public static final int TYPE_RBT_RRBT = 3;
	public static final int TYPE_SRBT_RRBT = 4;

	public static String PLAY_POLL_STATUS = "POLL";
	public static String PLAY_POLL_STATUS_ON = "0";
	public static String PLAY_POLL_STATUS_OFF = "1";

	public static HashMap<String, String> smsTextMap = null;

	public List<String> moreRBTKeywords = new ArrayList<String>();
	// The help message
	public String helpMessage = null;

	public int smsCategoryID = 3;
	public int profileCategoryID = 99;

	public boolean isPrepaid = false;

	public int rbtType = 0;

	public FeedStatus feedStatus = null;
	public String[] feedMsgs = null;
	public List<String> cricketSubKey = null;

	// List of Loop keywords
	public List<String> loopKeywords = new ArrayList<String>();

	public int maxDeleteResults = 3;

	public int maxSearchResultsToFetch = 10;

	public String[] supportedLangs = null; // All supported languages
	public String defaultLang = "eng";

	private Logger blockUnblockLogger = Logger.getLogger("BLOCK_UNBLOCK_LOGGER");
	
	private static DTWebsiteEventLogger webLogger = null;
	private String dtWebsiteEventLoggingDir = RBTParametersUtils
			.getParamAsString("COMMON", "DT_WEBSITE_EVENT_LOGGING_DIR", null);

	public final String NOT_PROFILE = "NOT_PROFILE";

	private static Logger rdcSelectionsLogger = Logger
			.getLogger("rdcSelectionsLogger");

	private List<String> discountedClipIDsList = null;
	private boolean isCheckCategorySmsAlias = false;
	
	protected boolean isConfirmationOn = RBTParametersUtils.getParamAsBoolean("SMS", "SMS_CONFIRMATION_ON", "FALSE");
	protected boolean isConfirmationOnWithoutOffer = RBTParametersUtils.getParamAsBoolean("SMS", "SMS_CONFIRMATION_ON_WITHOUT_OFFER", "FALSE");
	protected boolean isSupportBasePackageOffer = RBTParametersUtils.getParamAsBoolean("COMMON", "ALLOW_BASE_PACKAGE_OFFER", "FALSE");
	protected boolean isSupportSelPackageOffer = RBTParametersUtils.getParamAsBoolean("COMMON", "ALLOW_SEL_PACKAGE_OFFER", "FALSE");
    private  Map<String, String> cosIdCirleMap = new HashMap<String, String>(); 
    
    private List<String> migratedUserSubClassesList = new ArrayList<String>();
	String multiChargingKeyword = param(SMS, "MULTI_CHARGES_KEYWORD", "daily");
	private List<String> multiChargesList = null;
    private Map<String,String> multiChargeClassMap  = null;
    protected static List<String> m_FreemiumUpgradeChargeClass = null;
    
    protected List<String> freemiumSubClassList = null;
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	protected List<String> cosTypesForMultiPack = null;
	/**
	 * @throws Exception
	 * 
	 */
	public SmsProcessor() throws RBTException {
		moreRBTKeywords.add("more");
		loopKeywords.add("loop");

		if (dtWebsiteEventLoggingDir != null) {

			new File(dtWebsiteEventLoggingDir).mkdirs();
			Configuration cfg = new Configuration(dtWebsiteEventLoggingDir);
			try {
				webLogger = new DTWebsiteEventLogger(cfg);
				logger.info("Loaded DT Website logger");
			} catch (IOException e) {
				logger.error("Unable to load dt website logger ", e);
			}

		}

		String maxDeleteResultsStr = getSMSParameter(MAX_DELETE_RESULTS);
		if (maxDeleteResultsStr != null) {
			try {
				maxDeleteResults = Integer.parseInt(maxDeleteResultsStr);
			} catch (Exception e) {
				maxDeleteResults = 3;
			}
		}

		String sLang = getSMSParameter(SUPPORTED_LANGUAGES);
		if (sLang != null) {
			supportedLangs = sLang.toLowerCase().split(",");
			defaultLang = supportedLangs[0];
		}

		feedStatus = getCricketFeed();
		if (feedStatus != null) {
			String feed1Msg = feedStatus.getSmsKeywords();
			feedMsgs = feed1Msg.split(",");

			cricketSubKey = new ArrayList<String>();
			String temp = feedStatus.getSubKeywords();
			if (temp != null)
				cricketSubKey = Arrays.asList(temp.split(","));
		}

		String smsCatID = getSMSParameter(SMS_CATEGORY_ID);
		if (smsCatID != null) {
			int id;
			try {
				id = Integer.parseInt(smsCatID);
			} catch (Exception e) {
				id = 3;
			}
			smsCategoryID = id;
		}
		
		isCheckCategorySmsAlias = param(SMS, CHECK_CATEGORY_SMS_ALIAS, false);
 	 // RBT-7912 : Default Amount change for new Profile user
	  String configCosIDWithCircleList = param(SMS, PROFILE_ACTIVATION_ON_CIRCLE_COS_ID, null);
	    if(configCosIDWithCircleList != null && configCosIDWithCircleList.length()>0) {
	    	if(configCosIDWithCircleList.contains(";")) {
	    		String [] cosIdCircleArr = configCosIDWithCircleList.split(";");
	    		for(String cosIdCircle : cosIdCircleArr) {
	    			cosIdCirleMap.put(cosIdCircle.split(":")[0], cosIdCircle.split(":")[1]);
	    		}
	    	} else {
	    		cosIdCirleMap.put(configCosIDWithCircleList.split(":")[0], configCosIDWithCircleList.split(":")[1]);
	    	}
	     }
	    logger.info("cosIdCirleMap"+cosIdCirleMap);
	    
	    String migratedUserSubClasses = getParamAsString("WEBSERVICE",
				"MIGRATED_USER_SUBSCRIPTION_CLASSES", null);
		if (null != migratedUserSubClasses) {
			migratedUserSubClassesList = ListUtils.convertToList(
					migratedUserSubClasses, ",");
		}
		
		String multiChargingKeyword = param(SMS, "MULTI_CHARGES_KEYWORD", "daily");
		multiChargesList = Arrays.asList(multiChargingKeyword.split(","));
		
		String multiChargingKeywordMapStr = param(SMS, "MULTI_CHARGES_KEYWORD_MAPPING", null);
		multiChargeClassMap = MapUtils.convertIntoMap(multiChargingKeywordMapStr, ";", ":", ",");

		String chrgClassNumMaxMappingStr = RBTParametersUtils.getParamAsString("COMMON", "FREEMIUM_CHARGE_CLASSES_NUM_MAX_MAPPING", null);
		m_FreemiumUpgradeChargeClass = ListUtils.convertToList(chrgClassNumMaxMappingStr, ","); //MapUtils.convertIntoMap(chrgClassNumMaxMappingStr, ";", ":", ","); 
		
		
		freemiumSubClassList = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
				"FREEMIUM_SUB_CLASSES", "").split(","));
		logger.info("freemiumSubClassList = "+freemiumSubClassList);
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		String cosTypesForEnableMultiPack = RBTParametersUtils
				.getParamAsString("COMMON",
						"COS_TYPE_FOR_ALLOWING_MULTIPLE_PACK", null);
		cosTypesForMultiPack = ListUtils.convertToList(
				cosTypesForEnableMultiPack, ",");
	} 

	public void parseSms() {

	}
	
	
	@Override
	public String validateParameters(Task task) {
		String response = "VALID";

		if (task.getString(param_ocg_charge_id) != null)
			return "NOTVALID";
		if (task.containsKey("error")
				&& task.getString("error").equalsIgnoreCase("true"))
			return "INVALID";
		if (task.containsKey(param_isValid)
				&& task.getString(param_isValid).equalsIgnoreCase("INVALID"))
			return "INVALID";

		return response;
	}

	protected String getCOMMONParameter(String paramName) {
		ApplicationDetailsRequest smsRequest = new ApplicationDetailsRequest(
				iRBTConstant.COMMON, paramName, (String) null);
		Parameter param = rbtClient.getParameter(smsRequest);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return null;
	}

	protected String getDaemonParameter(String paramName) {
		ApplicationDetailsRequest smsRequest = new ApplicationDetailsRequest(
				iRBTConstant.COMMON, paramName, (String) null);
		Parameter param = rbtClient.getParameter(smsRequest);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return null;
	}

	public List<String> getTokenizedList(String string, String delimeter,
			boolean makeLowerCase) {
		if (string == null)
			return null;
		StringTokenizer st = new StringTokenizer(string, delimeter);
		List<String> list = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			if (makeLowerCase)
				list.add(st.nextToken().trim().toLowerCase());
			else
				list.add(st.nextToken().trim());
		}

		return list;
	}

	public boolean checkHelpRequest(String string) {
		if (string == null)
			return false;
		List<String> helpKeywords = new ArrayList<String>();
		String helpKeys = getSMSParameter(HELP_KEYWORDS);
		if (helpKeys != null)
			helpKeywords = getTokenizedList(helpKeys, ",", true);
		return helpKeywords.contains(string.toLowerCase());
	}

	public ViralData[] getViraldata(String subscriberID, String callerID,
			String type) {
		DataRequest dataRequest = new DataRequest(callerID, type);
		dataRequest.setSubscriberID(subscriberID);
		return rbtClient.getViralData(dataRequest);
	}

	public boolean isPhoneNumber(String phoneNo) {
		try {
			if (phoneNo.startsWith("+"))
				phoneNo = phoneNo.substring(1);
			if (phoneNo.length() >= param(GATHERER, PHONE_NUMBER_LENGTH_MIN, 10)
					&& phoneNo.length() <= param(GATHERER,
							PHONE_NUMBER_LENGTH_MAX, 10)) {
				Long.parseLong(phoneNo);
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	public void processSMSText(Task task) {
		task.setObject(param_subscriberID, task.getString("SUB_ID"));
		getSubscriber(task);
	}

	public Clip[] getActiveClips(int catId) {
		return rbtCacheManager.getActiveClipsInCategory(catId);
	}

	public FeedStatus getCricketFeed() {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType("CRICKET");
		return rbtClient.getFeedStatus(applicationDetailsRequest);
	}

	public Feed getCricketClass(String subKeyword) {
		logger.info("came in  getCricketClass");
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType("CRICKET");
		applicationDetailsRequest.setName(subKeyword);
		return rbtClient.getFeed(applicationDetailsRequest);
	}

	public boolean isCorpSub(Subscriber subscriber) {
		String userType = subscriber.getUserType();
		if (userType != null
				&& userType.equalsIgnoreCase(WebServiceConstants.CORPORATE))
			return true;
		return false;
	}

	public Setting[] getActiveSubSettings(String subID, int status) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subID);
		rbtDetailsRequest.setStatus("" + status);
		Settings settings = rbtClient.getSettings(rbtDetailsRequest);
		return settings.getSettings();
	}

	public Setting[] getActiveSubSettings(String subID) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subID);
		Settings settings = rbtClient.getSettings(rbtDetailsRequest);
		return settings.getSettings();
	}

	public Clip getClipByUniqueKey(String uniqueCode) {
		return rbtCacheManager.getClipFromPromoMaster(uniqueCode);
	}

	public Cos getCos(Subscriber subscriber) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest(
				subscriber.getCircleID(), Integer.parseInt(subscriber
						.getCosID()));
		applicationDetailsRequest.setIsPrepaid(subscriber.isPrepaid());
		return rbtClient.getCos(applicationDetailsRequest);
	}

	@Override
	public boolean isValidPrefix(String subId) {
		return false;
	}

	@Override
	public void processGiftAckRequest(Task task) {
	}

	@Override
	public void processSelection(Task task) {
	}

	public String replaceSpecialChars(String str) {
		int n = str.length();
		for (int i = 0; i < n; i++) {
			if (!(str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
					&& !(str.charAt(i) >= 'a' && str.charAt(i) <= 'z')
					&& !(str.charAt(i) >= '0' && str.charAt(i) <= '9')
					&& !(str.charAt(i) > 255)
					&& !(str.charAt(i) >= 192 && str.charAt(i) <= 255 && str.charAt(i) != 215 && str.charAt(i) != 247))	// added this because spanish characters fall between 192 to 255 except 215 and 247
			{
				str = Tools.findNReplace(str, "" + str.charAt(i), "");
				n = str.length();
				i = 0;
			}
		}
		return (str);

	}

	public int checkDuration(String strValue) {

		try {
			return Integer.parseInt(strValue);
		} catch (NumberFormatException e) {
			return 1;
		}
	}
	private String checkDurationFormat(List<String> smsList,String defaultProfileStr){
		
			if(smsList.get(1).contains(":")||smsList.get(1).contains("\\.")){
				return smsList.get(1);
			}else{
			   try{
			 	   Integer.parseInt(smsList.get(1));
			    }catch(Exception e){
			    	if(smsList.size()>2)
				       return smsList.get(2);
			    	else 
			    		return defaultProfileStr;
			   }
			}
			return smsList.get(1);
	}

	public long checkDuration(Task task, boolean isDays) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		int maxHourDuration = param(SMS, "MAX_HOURS_FOR_PROFILES", 9999999);
		String defaultProfileStr =  RBTParametersUtils.getParamAsString(SMS,"DEFAULT_PROFILE_HOURS", "1");
		String defaultDays =  RBTParametersUtils.getParamAsString(SMS,"DEFAULT_PROFILE_DAYS", "1");
		double defaultProfileHours = 1;
		try{
			defaultProfileHours = Double.parseDouble(defaultProfileStr);
		}catch(Exception ex){
			defaultProfileHours = 1;
            logger.info("Exception while parsing default profile hours");
		}

		if (smsList == null || smsList.size() < 2) {
			if (isDays)
				return Integer.parseInt(defaultDays) * 24 * 60;
			else
				return (long)(defaultProfileHours*60);
		}
		String strValue = checkDurationFormat(smsList,defaultProfileStr);
		long total = 0;
		try {
			String[] tokens;
			if (strValue.indexOf(":") != -1)
				tokens = strValue.split(":");
			else
				tokens = strValue.split("\\.");
			if (tokens.length > 2 && !isDays)
				return -1;
			if (tokens.length > 3 && isDays)
				return -1;

			String firstToken = tokens[0];
			if (firstToken.trim().length() == 0)
				firstToken = "0";
			long digit = Long.parseLong(firstToken);
			if (digit < 0)
				return -1;
			if (isDays)
				total += digit * 24 * 60;
			else {
				if (digit > maxHourDuration)
					return -1;
				total += digit * 60;
			}

			if (tokens.length < 2)
				return total;

			String secondToken = tokens[1];
			if (secondToken.trim().length() == 0)
				secondToken = "0";
			long digit2 = Long.parseLong(secondToken);
			if (digit2 < 0)
				return -1;
			if (isDays) {
				if (digit > maxHourDuration)
					return -1;
				total += digit2 * 60;
			} else {
				if (digit2 > 59)
					return -1;
				total += digit2;
			}

			if (tokens.length < 3)
				return total;
			String thirdToken = tokens[2];
			if (thirdToken.trim().length() == 0)
				thirdToken = "0";
			long digit3 = Long.parseLong(thirdToken);
			if (digit3 < 0)
				return -1;
			if (isDays) {
				if (digit3 > 59)
					return -1;
				total += digit3;
			}
		} catch (NumberFormatException e) {
			logger.error("Issue in getting profile duration", e);
			if (isDays)
				return 24 * 60;
			else
				return (long)(defaultProfileHours * 60);
		}
		return total;
	}

	public String renameWavFile(Task task, String strValue) {
		String lang = task.getString(param_language);
		if (lang != null) {
			StringTokenizer tkn = new StringTokenizer(strValue, "_", true);
			String wavFile = "";
			String val = null;
			while (tkn.hasMoreTokens()) {
				val = tkn.nextToken();
				if (langCheck(val))
					wavFile = wavFile.trim() + lang;
				else
					wavFile = wavFile.trim() + val;
			}
			return wavFile;
		} else
			return strValue;
	}

	private boolean langCheck(String strLang) {
		if (supportedLangs == null || supportedLangs.length <= 0)
			return false;
		for (int i = 0; i < supportedLangs.length; i++) {
			if (strLang.equalsIgnoreCase(supportedLangs[i]))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#getTask(java.util
	 * .HashMap)
	 */
	@Override
	public Task getTask(HashMap<String, String> requestParams) {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		Task task = new Task(null, taskSession);

		String subscriberID = task.getString(param_subID);
		if (subscriberID == null)
			subscriberID = task.getString(param_msisdn);
		if (subscriberID == null)
			subscriberID = task.getString(param_MSISDN);
		task.setObject(param_subscriberID, subscriberID);

		if (!task.containsKey(param_isdirectact)
				&& !task.containsKey(param_isdirectdct)) {
			String mode = task.getString(param_MODE);
			if (mode == null || mode.length() == 0) {
				mode = param(SMS, PROMO_ID_ACTIVATED_BY, "SMS");
				task.setObject(param_MODE, mode);
			}

			task.setObject(param_actInfo, task.getString(param_ipAddress) + ":"
					+ mode);
			task.setObject(param_actby, mode);

			if (task.containsKey(param_shortCode)) {
				task.setObject(param_actInfo, task.getString(param_actInfo)
						+ ":" + task.getString(param_shortCode));
			}
		}

		getSubscriber(task);

		String smsText = task.getString(param_smsText);
		if (smsText == null)
			smsText = task.getString(param_msg);
		if (smsText == null)
			smsText = task.getString(param_MESSAGE);
		task.setObject(param_smsSent, smsText);
		ArrayList<String> smsWords = tokenizeArrayList(smsText, " ");
		task.setObject(param_smsText, smsWords);

		preProcess(task);

		logger.info("RBT:: task: " + task);
		return task;
	}

	public void preProcess(Task task) {
		if (!isValidIP(task.getString(param_ipAddress))
				&& !isValidIP(task.getString(param_hostName))) {
			task.setObject(param_responseSms, "Invalid IP Address");
			task.setObject(param_isValid, "INVALID");
		}
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!subscriber.isValidPrefix()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					INVALID_PREFIX, m_invalidPrefixDefault, subscriber
							.getLanguage()));
			task.setObject(param_isValid, "INVALID");
		}
		// If its direct activation request allow
		if (subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING)
				&& !task.containsKey(param_isdirectact)
				&& !task.containsKey(param_isdirectdct)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DEACTIVATION_PENDING_TEXT, m_deactivationPendingDefault,
					subscriber.getLanguage()));
			task.setObject(param_isValid, "INVALID");
		}

		String access = (String) task.getObject(param_access);
		if (access != null) {
			if (access.startsWith("profile")) {
				if (!isUserActive(subscriber.getStatus())
						|| !param(SMS, PROFILE_SUB_CLASS, "").equalsIgnoreCase(
								subscriber.getSubscriptionClass())) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							SMS_ACCESS_FAILURE_TEXT, m_serviceNotAllowed,
							subscriber.getLanguage()));
					return;
				}
			}
			if (access.startsWith("ret") && retailer(task) == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						SMS_ACCESS_FAILURE_TEXT, m_serviceNotAllowed,
						subscriber.getLanguage()));
				return;
			}
		}

		if (task.getTaskAction() == null)
			getFeature(task);
		String feature = task.getTaskAction();
		//Start :RBT-12195 - User block - unblock feature.
		String subscriberID = subscriber.getSubscriberID();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		boolean isBlockedSub = rbtDBManager.isBlackListSub(subscriberID);
		if (!(feature.equalsIgnoreCase(BLOCK_SUB_KEYWORD) || feature
				.equalsIgnoreCase(UNBLOCK_SUB_KEYWORD)) && isBlockedSub) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, SMS_NON_ACT_BLCK_SUB_OTHER_TEXT,
							unblck_blck_serviceNotAllowed,
							subscriber.getLanguage()));
			task.setObject(param_isValid, "INVALID");
			return;
		}
		//End:RBT-12195 - User block - unblock feature.
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (feature.equalsIgnoreCase(ACTIVATE_N_SELECTION) && smsList != null
				&& smsList.size() == 1) {
			String promocode = smsList.get(0);
			try {
				int promoId = Integer.parseInt(promocode);
				if (promoId < param(COMMON, MIN_VALUE_PROMO_ID, 0)) {
					logger
							.info("promo id is less than min value of promo id, hence set song via find feature");
					task.setTaskAction(REQUEST_RBT_KEYWORD);
				}
			} catch (Exception e) {

			}
		}
		
		if((feature.equalsIgnoreCase(VIRAL_START_KEYWORD)||feature.equalsIgnoreCase(VIRAL_STOP_KEYWORD))
				&& subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING)){
			 task.setObject(param_isValid, "VALID");
		}
		
		if (!task.getTaskAction().equalsIgnoreCase(MOBILE_REGISTRATION) && !task.getTaskAction().equalsIgnoreCase(REATILER_ACT_N_SEL_FEATURE)) {
			getCallerID(task);
		}

		String callerId = task.getString(param_callerid);

		if (!subscriber.isCanAllow() && !(param(COMMON, "ALLOW_DEACTIVATION_FOR_BLACKLISTED_SUBSCRIBER", false) 
					&& feature.equalsIgnoreCase(DEACTIVATION_KEYWORD))) {
		
				task.setObject(param_responseSms, getSMSTextForID(task,
					TOTAL_BLACKLIST_MSG, m_totalBlackListTextDefault,
					subscriber.getLanguage()));
			    task.setObject(param_isValid, "INVALID");
		  }
		
		getLang(task);
		
		if (subscriber.getUserType().equalsIgnoreCase("corporate")
				&& callerId == null
				&& param(SMS, CORP_CHANGE_SELECTION_ALL_BLOCK, true)) {
			if(feature.equalsIgnoreCase(DEACTIVATION_KEYWORD)){
				task.setObject(param_responseSms, getSMSTextForID(task,
						CORP_DEACT_SELECTION_ALL_FAILURE,
						m_totalBlackListTextDefault, subscriber.getLanguage()));
			    task.setObject(param_isValid, "INVALID");
			} else if (!RBTParametersUtils.getParamAsBoolean("SMS", "CORP_ALLOWED_PROFILE_SELECTION", "FALSE")
					|| (!(feature.equalsIgnoreCase(TEMPORARY_OVERRIDE_CANCEL_MESSAGE)
							|| feature.equalsIgnoreCase(LIST_PROFILE_KEYWORD) || feature
								.equalsIgnoreCase(NEXT_PROFILE_KEYWORD)) && getProfileClip(task) == null)) {
			    task.setObject(param_responseSms, getSMSTextForID(task,
					CORP_CHANGE_SELECTION_ALL_FAILURE,
					m_totalBlackListTextDefault, subscriber.getLanguage()));
			    task.setObject(param_isValid, "INVALID");
			}
		}
		if (!param(COMMON, ALLOW_SUSPENDED_USER_ACCESS, false)
				&& !feature.equalsIgnoreCase("DEACTIVATION_KEYWORD")
				&& !feature.equalsIgnoreCase(RESUMPTION_KEYWORD)
				&& !feature.equalsIgnoreCase(SUSPENSION_KEYWORD)
				&& !feature.equalsIgnoreCase(SCRATCH_CARD_FEATURE)
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.SUSPENDED)) {
			if (task.containsKey(param_isdirectact)
					|| task.containsKey(param_isdirectdct)) {
				// Its direct activation request , Allow user to activate
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						SUSPENDED, m_suspendedTextDefault, subscriber
								.getLanguage()));
				task.setObject(param_isValid, "INVALID");
			}
		}
		if (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.LOCKED)
				&& !feature.equalsIgnoreCase(DEACTIVATION_KEYWORD)
				&& !feature.equalsIgnoreCase(UNLOCK_KEYWORD)
				&& !feature.equalsIgnoreCase(LOCK_KEYWORD)) {
			task.setObject(param_responseSms, getSMSTextForID(task, LOCKED,
					m_lockedTextDefault, subscriber.getLanguage()));
			task.setObject(param_isValid, "INVALID");
		}
		if (task.getString(param_access) != null
				&& task.getString(param_access).equalsIgnoreCase("ret")
				&& retailer(task) == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					SERVICE_NOT_ALLOWED, m_serviceNotAllowed, subscriber
							.getLanguage()));
			task.setObject(param_isValid, "INVALID");
		}


		if (!task.containsKey(param_catid))
			task.setObject(param_catid, "3");

		ArrayList<String> smsTextList = (ArrayList<String>)task.getObject(param_smsText);
		if(smsTextList != null && smsTextList.size() > 0)
        {
			for (String charge : multiChargesList) {
				if (smsTextList.contains(charge.toLowerCase())) {
					smsTextList.remove(charge.toLowerCase());
					String chrgClass = multiChargeClassMap.get(charge);
					if(chrgClass == null)
						continue;
					task.setObject(param_chargeclass, chrgClass); 
					task.setObject(param_USE_UI_CHARGE_CLASS, true);
					task.setObject(param_isMultiChargesRequest,true);
					break;
				}
			}
		}

		// getChargingModel();

		// getSubscriptionType();
		// getRbtPromoActSubClass();
	}

	@Override
	public void getFeature(Task task) {
		task.setTaskAction(ACTIVATE_N_SELECTION);
		// populates CosDetails object if any smsKeyword corresponding to a COS
		// is sent in SMS_TEXT
		isCosSmsKeywordPresent(task);
		isThisFeature(task, SmsKeywordsStore.rbtKeywordsSet, null);
		if (isThisFeature(task, SmsKeywordsStore.retailerActNSelKeywordsSet,
				REATILER_ACT_N_SEL_FEATURE))
			return;
		boolean isActivationRequest = isThisFeature(task,
				SmsKeywordsStore.activationKeywordsSet, ACTIVATION_KEYWORD);
		task.setObject(IS_ACTIVATION_REQUEST, isActivationRequest ? "TRUE"
				: "FALSE");

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (isActivationRequest && !isConfirmationOnWithoutOffer){
			if (smsList != null && smsList.size() > 0)
				task.setTaskAction(ACTIVATE_N_SELECTION);
		}
		if (smsList == null || smsList.size() == 0)
			return;

		if (SmsKeywordsStore.specificCategorySearchKeywordsSet != null && SmsKeywordsStore.specificCategorySearchKeywordsSet.size() > 0)
		{
			for (int i = 0; i < smsList.size(); i++)
			{
				String smsToken = smsList.get(i).trim();
				if(smsToken.length() >= 3 && smsToken.toLowerCase().startsWith("wt"))
					smsToken = smsToken.substring(2);
				if (SmsKeywordsStore.specificCategorySearchKeywordsSet.contains(smsToken))
				{
					task.setTaskAction(CATEGORY_SEARCH_KEYWORD);
					smsList.remove(i);
					smsList.add(smsToken);
				}
			}
		}
		if (isThisFeature(task, SmsKeywordsStore.recommendedSongsKeywordsSet,
				SMS_RECOMMEND_SONGS_KEYWORD))
			return;
		
		if(isThisFeature(task, SmsKeywordsStore.ouiSmsRequestKeywordSet, OUI_SMS_KEYWORD)) {			
			return;			
		}			
		if (isThisFeature(task,
				SmsKeywordsStore.discountedSelectionKeywordsSet,
				DISCOUNTED_SEL_KEYWORD))
			return;
		if (isThisFeature(task, SmsKeywordsStore.deactivateBaseKeywordsSet,
				DEACTIVATION_KEYWORD))
			return;
		if (isThisFeature(task, SmsKeywordsStore.referKeywordsSet,
				REFERRAL_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.selectionTypeOneKeywordsSet, SEL_KEYWORD1))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.selectionTypeTwoKeywordsSet, SEL_KEYWORD2))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.viralKeywordsSet,
				VIRAL_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.doubleConfirmationKeywordSet,
				SMS_DOUBLE_CONFIRMATION))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.singledoubleConfirmationKeywordSet,
				PROCESS_DOUBLE_CONFIRMATION))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.viewSubscriptionStatisticsKeywordSet,
				VIEW_SUBSCRIPTION_STATISTICS_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.deactivateSelectionKeywordsSet,
				RMVCALLERID_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.deactivateProfileKeywordsSet,
				TEMPORARY_OVERRIDE_CANCEL_MESSAGE))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.deactivateOverrideShuffleKeywordsSet,
				NAV_DEACT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.deactivateManageSelectionsKeywordsSet,
				MANAGE_DEACT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.deactivateDownloadKeywordsSet,
				DEACT_DOWNLOAD_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.searchMoreContentKeywordsSet,
				REQUEST_MORE_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.azaanSearchMoreContentKeywordsSet,
				AZAAN_REQUEST_MORE))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.searchCategoriesKeywordsSet,
				CATEGORY_SEARCH_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.searchClipsKeywordsSet,
				REQUEST_RBT_KEYWORD))
			return;
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		else if (isThisFeature(task, SmsKeywordsStore.azaanSearchKeywordsSet,
				AZAAN_REQUEST_RBT_KEYWORD))
			return;		
		else if (isThisFeature(task, SmsKeywordsStore.searchOptinKeywordsSet,
				REQUEST_OPTIN_RBT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.contestInfluencerKeywordsSet,
				CONTEST_INFLUENCER_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.topClipsKeywordsSet,
				TOP_CLIPS_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.topCategoriesKeywordsSet,
				TOP_CATEGORIES_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.listCategoriesKeywordsSet,
				LIST_CATEGORIES_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.copyConfirmKeywordsSet,
				COPY_CONFIRM_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.likeConfirmKeywordsSet,
				RBT_LIKE_CONFIRM_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.copyCancelTypeOneKeywordsSet,
				COPY_CANCEL_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.copyKeywordsSet,
				COPY_KEYWORDS))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.giftKeywordsSet,
				GIFT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.copyCancelTypeTwoKeywordsSet,
				CANCELCOPY_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.tnbKeywordsSet,
				TNB_KEYWORDS))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.cricketKeywordsSet,
				CRICKET_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.helpKeywordsSet,
				HELP_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.tryAndBuyKeywordsSet,
				TNB_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.enableUDSKeywordsSet,
				UDS_ENABLE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.disableUDSKeywordsSet,
				UDS_DISABLE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.churnKeywordsSet,
				CHURN_OFFER))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.webRequestKeywordsSet,
				WEB_REQUEST_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.confirmSubscriptionAndCopyKeywordsSet,
				CONFIRM_SUBSCRIPTION_N_COPY_FEATURE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.scratchCardKeywordsSet,
				SCRATCH_CARD_FEATURE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.giftCopyKeywordsSet,
				GIFTCOPY_FEATURE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.newsAndBeautyKeywordsSet,
				NEWS_AND_BEAUTY_FEED_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.promotionTypeOneKeywordsSet, PROMOTION1))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.promotionTypeTwoKeywordsSet, PROMOTION2))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.songCodeRequestKeywordSet,
				SONG_CODE_REQUEST_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.songPromotionTypeOneKeywordsSet,
				SONG_PROMOTION1))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.songPromotionTypeTwoKeywordsSet,
				SONG_PROMOTION2))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.initGiftKeywordsSet,
				INIT_GIFT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.initGiftConfirmKeywordsSet,
				INIT_GIFT_CONFIRM_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.pollOnKeywordsSet,
				POLLON_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.pollOffKeywordsSet,
				POLLOFF_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.newsletterOnKeywordsSet,
				SET_NEWSLETTER_ON_KEYWORDS))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.newsletterOffKeywordsSet,
				SET_NEWSLETTER_OFF))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.disableIntroKeywordsSet,
				DISABLE_INTRO))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.disableOverlayKeywordsSet,
				DISABLE_OVERLAY_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.enableOverlayKeywordsSet,
				ENABLE_OVERLAY_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.weeklyToMonthlyConversionKeywordSet,
				WEEKLY_TO_MONTHLY_CONVERSION))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.renewSelectionKeywordsSet, RENEW_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.mgmAcceptKeywordsSet,
				MGM_ACCEPT_KEY))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.retailerRequestKeywordsSet,
				RETAILER_REQ_RESPONSE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.listenSongKeywordsSet,
				LISTEN_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.songOfTheMonthKeywordsSet, SONGOFMONTH))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.listDownloadsKeywordsSet,
				DOWNLOADS_LIST_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.manageKeywordsSet,
				MANAGE_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.listProfilesKeywordsSet,
				LIST_PROFILE_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.nextProfilesKeywordsSet,
				NEXT_PROFILE_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.mgmRequestKeywordsSet,
				MGM_FEATURE))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.suspensionKeywordsSet,
				SUSPENSION_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.resumptionKeywordsSet,
				RESUMPTION_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.blockKeywordsSet,
				BLOCK_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.unblockKeywordsSet,
				UNBLOCK_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.packKeywordsSet,
				PACK_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.meridhunKeywordsSet,
				MERIDHUN_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.confirmChargeKeywordsSet,
				CONFIRM_CHARGE_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.lockKeywordsSet,
				LOCK_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.unlockKeywordsSet,
				UNLOCK_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.emotionKeywordsSet,
				EMOTION_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.emotionExtendKeywordsSet,
				EMOTION_EXTEND_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.emotionDeactivateKeywordsSet,
				EMOTION_DCT_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.downloadOptinKeywordsSet,
				DOWNLOAD_OPTIN_RENEWAL))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.rdcSelectionKeywordsSet,
				RDC_SEL_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.consentYesKeywordsSet,
				CONSENT_YES_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.consentNoKeywordsSet,
				CONSENT_NO_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.giftAcceptKeywordSet,
				GIFT_ACCEPT_KEYWORD))
			return;
		// else if(isThisFeature(task, SmsKeywordsStore.giftDownloadKeywordSet,
		// GIFT_DOWNLOAD_KEYWORD))
		// return;
		else if (isThisFeature(task, SmsKeywordsStore.giftRejectKeywordSet,
				GIFT_REJECT_KEYWORD))
			return;
		else if (param(COMMON, REQUEST_RBT_KEYWORD_OPTIONAL, "FALSE")
				.equalsIgnoreCase("TRUE")
				&& !getCategoryAndClipForPromoID(task, smsList.get(0))) {
			task.setTaskAction(REQUEST_RBT_KEYWORD);
			return;
		} else if (retailer(task) != null) {
			task.setTaskAction(RETAILER_FEATURE);
			return;
		} else if (isThisFeature(task,
				SmsKeywordsStore.cpSelectionConfirmKeywordsSet,
				CP_SEL_CONFIRM_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.voucherKeywordsSet,
				VOUCHER_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.upgradeSelKeywordsSet,
				UPGRADE_SEL_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.musicPackKeywordSet,
				MUSIC_PACK_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.rechargeSmsOptOutKeywordSet,
				RECHARGE_SMS_OPTOUT_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.baseUpgradationKeywordSet,
				BASE_UPGRADATION_KEYWORD))
			return;
		else if (isThisFeature(task, SmsKeywordsStore.preGiftKeywordsSet,
				PRE_GIFT_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.preGiftConfirmKeywordsSet,
				PRE_GIFT_CONFIRM_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.viralStartKeywordSet,
				VIRAL_START_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.viralStopKeywordSet,
				VIRAL_STOP_KEYWORD))
			return;
		else if (isThisFeature(task,
				SmsKeywordsStore.lotteryListKeywordsSet,
				LOTTERY_LIST_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.randomizeKeywordSet,
				RANDOMIZE_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.unrandomizeKeywordSet,
				UNRANDOMIZE_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.viralOptOutRequestKeywordSet,
				VIRAL_OPTOUT_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.downloadSetRequestKeywordSet,
				DOWNLOAD_SET_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.viralOptInRequestKeywordSet,
				VIRAL_OPTIN_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.initRandomizeRequestKeywordSet,
				INIT_RANDOMIZE_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.resubscriptionRequestKeywordSet,
				RESUBSCRIPTION_FEATURE_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.supressPreRenewalSmsRequestKeywordSet,
				SUPRESS_PRERENEWAL_SMS_KEYWORD))
			return;
		else if(isThisFeature(task, SmsKeywordsStore.cancelDeactivationKeywordSet, SMS_CANCEL_DEACTIVATION_KEYWORD)) {			
			return;			
		}
		else if(isThisFeature(task, SmsKeywordsStore.upgradeBaseKeywordSet, SMS_BASE_SONG_UPGRADE_KEYWORD)) {			
			return;			
		}
		else if(isThisFeature(task, SmsKeywordsStore.mobileUidRegistrationSet, MOBILE_REGISTRATION)) {			
			return;			
		}
		else if(isThisFeature(task, SmsKeywordsStore.timeOfDaySettingSet, TIME_OF_DAY_SETTING_KEYWORD)) {			
			return;			
		}
		else if(isThisFeature(task, SmsKeywordsStore.smsChurnOfferSet, SMS_CHURN_OFFER_KEYWORD)){
			return;
		}
		else if(isThisFeature(task, SmsKeywordsStore.upgradeOnDeactDelaySet, UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD)){
			return;
		} else if (isThisFeature(task, SmsKeywordsStore.multipleSelectionKeywordSet,
				CALLER_BASED_MULTIPLE_SELECTION_KEYWORD)){
			return;
		} else if (isThisFeature(task, SmsKeywordsStore.deactBaseSongChurnKeywordSet, SMS_CANCELLAR_KEYWORD)){
			return;
		} else if (isThisFeature(task, SmsKeywordsStore.baseSongDeactKeywordSet, DEACT_BASE_SONG_CHURN_KEYWORD)){
			return;
		} else if (isThisFeature(task, SmsKeywordsStore.directSongDeactKeywordSet, DIRECT_SONG_DEACT_KEYWORD)){
			return;
		}else if(isThisFeature(task, SmsKeywordsStore.manageDefaultSettingsKeywordSet, MANAGE_DEFAULT_SETTINGS_KEYWORD)){
			return;
		//Start:RBT-12195 - User block - unblock feature.
		} else if (isThisFeature(task, SmsKeywordsStore.blockUsrKeywordsSet,
				BLOCK_SUB_KEYWORD)) {
			return;
		} else if (isThisFeature(task, SmsKeywordsStore.unblockUsrKeywordsSet,
				UNBLOCK_SUB_KEYWORD)) {
			return; //End:RBT-12195 - User block - unblock feature.
		} else if ((isThisFeature(task, SmsKeywordsStore.premiumSelectionConfirmationKeywordsSet,
				PREMIUM_SELECTION_CONFIRMATION_KEYWORD))) {
			return;
		} else if ((isThisFeature(task, SmsKeywordsStore.doubleOptinConfirmationKeywordsSet,
				DOUBLE_OPT_IN_CONFIRMATION_KEYWORD))) {
			return;
		}else if ((isThisFeature(task, SmsKeywordsStore.doubleConfirmationForXbiPack, DOUBLE_CONFIRMATION_FOR_XBI_PACK))) {
			return;
		}else if(isThisFeature(task, SmsKeywordsStore.baseAndCosUpgradationKeywordSet,
				BASE_AND_COS_UPGRADATION_KEYWORD)){
			return;
		}
		//Added for VB-380
		else if (isThisFeature(task, SmsKeywordsStore.azaanDeactKeywordSet,
				AZAAN_REQUEST_DCT_KEYWORD)){
			return;
		}
			
	}

	public boolean isThisFeature(Task task, HashSet<String> featureKeywordsSet,
			String feature) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() == 0)
			return false;
		if (featureKeywordsSet == null || featureKeywordsSet.size() == 0)
			return false;
		if (smsList.removeAll(featureKeywordsSet)) {
			if (feature != null)
				task.setTaskAction(feature);
			return true;
		}
		return false;
	}

	public boolean isThisFeature(Task task, ArrayList<String> featureKeywords,
			String feature) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (featureKeywords == null || featureKeywords.size() <= 0
				|| smsList == null || smsList.size() <= 0)
			return false;
		for (int i = 0; i < smsList.size(); i++) {
			if (featureKeywords.contains(smsList.get(i))) {
				smsList.remove(i);
				task.setTaskAction(feature);
				return true;
			}
		}
		return false;
	}

	public boolean isThisFeature(Task task, String featureKeywords,
			String feature) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (featureKeywords == null || featureKeywords.length() <= 0
				|| smsList == null || smsList.size() <= 0)
			return false;
		for (int i = 0; i < smsList.size(); i++) {
			if (featureKeywords.equalsIgnoreCase(smsList.get(i))) {
				smsList.remove(i);
				task.setTaskAction(feature);
				return true;
			}
		}
		return false;
	}

	protected void reorderParameters(Task task) {
		if (task.containsKey(param_MSISDN))
			task.setObject(param_subscriberID, task.getString(param_MSISDN));
	}

	private boolean processupdateExtraInfoStatus(Task task, String attribute,
			String value) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(
				subscriber.getSubscriberID());
		if (value.equalsIgnoreCase(PLAY_POLL_STATUS_ON)
				&& attribute.equalsIgnoreCase(PLAY_POLL_STATUS))
			updateRequest.setIsPollOn(true);
		else if (value.equalsIgnoreCase(PLAY_POLL_STATUS_OFF)
				&& attribute.equalsIgnoreCase(PLAY_POLL_STATUS))
			updateRequest.setIsPollOn(false);
		else if (value.equalsIgnoreCase("DISABLE_INTRO"))
			updateRequest.setIsPressStarIntroEnabled(false);
		else if (value.equalsIgnoreCase("DISABLE_OVERLAY"))
			updateRequest.setIsOverlayOn(false);
		else if (value.equalsIgnoreCase("ENABLE_OVERLAY"))
			updateRequest.setIsOverlayOn(true);
		else if (value.equalsIgnoreCase("SET_NEWSLETTER_ON"))
			updateRequest.setIsNewsLetterOn(true);
		else if (value.equalsIgnoreCase("SET_NEWSLETTER_OFF"))
			updateRequest.setIsNewsLetterOn(false);

		rbtClient.setSubscriberDetails(updateRequest);
		return true;
	}

	private boolean isProfileSubscriber(String subscriberId, List<Setting> settingList) {
		Setting[] settings = getActiveSubSettings(subscriberId, 99);
		if (settings == null || settings.length <= 0)
			return false;
		
		settingList.addAll(Arrays.asList(settings));
		return true;
	}

	private boolean isCricketFeedSubscriber(Setting[] settings) {
		if (settings == null || settings.length <= 0)
			return false;
		return true;
	}

	private boolean isDynamicShuffleSubscriber(Task task) {

		Settings subscriberSettings = getSettings(task);
		Setting settings[] = subscriberSettings.getSettings();
		if (settings == null || settings.length <= 0)
			return false;
		for (Setting setting : settings) {
			if (setting.getStatus() == 92 || setting.getStatus() == 93
					|| setting.getStatus() == 79) {
				logger.info("User setting status is " + setting.getStatus()
						+ " category id: " + setting.getCategoryID());
				task.setObject(param_status, "" + setting.getStatus());
				task.setObject(param_catid, "" + setting.getCategoryID());
				return true;
			}
		}
		return false;
	}

	private boolean isSubActiveForDays(Subscriber mgmAgent, int days) {
		if (days == -1)
			return true;

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);

		if (mgmAgent.getActivationDate() == null
				|| mgmAgent.getActivationDate().after(cal.getTime())) {
			return false;
		}
		return true;
	}

	public int checkDuration(Task task) {
		int time = 1;

		try {
			@SuppressWarnings("unchecked")
			ArrayList<String> smsList = (ArrayList<String>) task
					.getObject(param_smsText);
			if (smsList == null || smsList.size() <= 1)
				return time;
			String timeStr = smsList.get(1);
			return Integer.parseInt(timeStr);
		} catch (Exception e) {

			return 1;
		}
	}

	protected Clip getProfileClip(Task task)
	{
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		String contentId = null;
		int smsListSize = (null != smsList) ? smsList.size() : 0;
		logger.info("Getting profile clip. smsListSize: " + smsListSize
				+ ", smsList: " + smsList);
		Clip profileClip = null;
		Clip[] clips = rbtCacheManager.getClipsInCategory(99);
		
		if(clips == null || clips.length <= 0) {
			logger.warn("No clips found under category 99");
			return profileClip;
		}

		String language = "eng";
        String origLanguage = task.getString(param_language);
        
        if(smsList != null && smsList.size() > 0){
        	contentId = smsList.get(0);
        } else {
			logger.warn("Returning null, Requested smsText list is null.");
        	return profileClip;
        }
		boolean isProfileMenuListingAllowed = param(SMS,
				PROFILE_SET_ALLOWED_BY_INDEX, false);
		if (isProfileMenuListingAllowed) {
			int songNo = -1;
			double profilehrs = -1;
			try {
				logger.debug("Index based profile clip is fetching. "
						+ "smsListSize: " + smsListSize + ", smsList: " + smsList);
				if (smsList != null && smsList.size() >= 1) {
					songNo = Integer.parseInt(smsList.get(0));
					if(smsList.size() >= 2){
					   profilehrs = Double.parseDouble(smsList.get(1));
					}
					Subscriber subscriber = (Subscriber) task
							.getObject(param_subscriber);
					task.setObject(param_SMSTYPE, "PROFILE");
					ViralData context[] = getViraldata(task);
					logger.info("Request contains profileHrs: " + profilehrs
							+ ", songNo: " + songNo + ", Viraldata: " + context);
					if (context != null && songNo != -1) {
						int profileMaxLimit = param(SMS, PROFILE_LIST_COUNT,
								1000);
						int nextCount = context[0].getCount();
						String clipIds = context[0].getClipID();
						logger.info("Configued profileListCount: "
								+ profileMaxLimit + ", nextCount: " + nextCount
								+ ", viraldata clipIds: " + clipIds);
						String str[] = null;
						int length = 0;
						if (clipIds != null)
							length = clipIds.split(",").length;
						int count = (nextCount - 1)*profileMaxLimit+1;
						StringTokenizer stk = new StringTokenizer(clipIds,",");
						for (int i = count; i <= (count+length); i++) {
							String clipSmsAlias = stk.nextToken();
							if (i == songNo) {
								contentId = clipSmsAlias.trim();
								task.setObject(param_profileClipSMSAlias, contentId);
								break;
							}
						}
						logger.info("Got the contentId: " + contentId
								+ ", checked viraldata"
								+ " clipIds with requested songNo. "
								+ ", viraldata clipIds: " + clipIds
								+ ", songNo: " + songNo+", ");
					}
					logger.info("ContentId for profile song: " + contentId);
				}
				logger.info("Index based profile clip is fetched. "
						+ "smsListSize: " + smsListSize + ", smsList: "
						+ smsList + ", contentId: " + contentId);
			} catch (Exception ex) {
				logger.error("Exception while getting the profile clip by Index", ex);
			}
		} logger.info("Checking the content id: "+contentId+", clips: "+clips);
		for(int i=0; i < clips.length; i++)
		{  
			if(clips[i]!=null && clips[i].getClipRbtWavFile() != null && (clips[i].getClipRbtWavFile().indexOf("_"+origLanguage+"_") != -1
					||(clips[i].getShortLanguage()!=null&&clips[i].getShortLanguage().equals(origLanguage)) || (clips[i].getLanguage()!=null&&clips[i].getLanguage().equals(origLanguage))))
			{
				if(clips[i].getClipSmsAlias() != null)
				{
					if(tokenizeArrayList(clips[i].getClipSmsAlias().toLowerCase(), ",").contains(contentId.toLowerCase()))
						profileClip = clips[i];
				}
			}
		
		}
		if (profileClip == null) {
			logger.info("Since profile clip is not found, searching based on "
					+ "clipSmsAlias. clips length: " + clips.length);
			for (int i = 0; i < clips.length; i++) {
				if (clips[i]!=null && ((clips[i].getShortLanguage() != null && clips[i]
						.getShortLanguage().equals(defaultLang))
						|| (clips[i].getClipRbtWavFile() != null && (clips[i]
								.getClipRbtWavFile().indexOf(
										"_" + defaultLang + "_") != -1)))) {
					if (clips[i].getClipSmsAlias() != null
							&& tokenizeArrayList(
									clips[i].getClipSmsAlias().toLowerCase(),
									",").contains(contentId.toLowerCase())) {
						profileClip = clips[i];
						break;
					}
				}
			}
		}
		
		if (profileClip == null) {
			logger.warn("Returning null, profile clip is not found.");
			return null;
		}
		logger.info("Found profile clip: " + profileClip.getClipNameWavFile());
		String profileWavFile = Utility.findNReplaceAll(profileClip.getClipRbtWavFile(), "_eng_", "_"+origLanguage+"_");
		Clip requestedProfileClip = getClipByWavFile(profileWavFile,origLanguage);
		logger.info("Fetched profile clip by wavFile. requestedProfileClip: "
				+ requestedProfileClip + ", profileClip: " + profileClip);
		if(requestedProfileClip == null) 
			requestedProfileClip = profileClip;
		return requestedProfileClip;
	}

	private boolean isDurationDays(Task task) {
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
        String alias = smsList.get(0);
        if(task.containsKey(param_profileClipSMSAlias)){
        	alias = task.getString(param_profileClipSMSAlias);
        }
		if (alias == null)
			return false;
		if (tokenizeArrayList(param(SMS, PROFILE_DAYS_ALIAS, "").toLowerCase(),
				",").contains(alias.toLowerCase()))
			return true;
		return false;
	}

	private boolean isMgmMoreGiftAllowed(String mgmAgentId, int countMgmRequest) {
		Task task = new Task();
		String callerid = task.getString(param_callerid);
		task.setObject(param_callerid, null);
		task.setObject(param_subscriberID, mgmAgentId);
		task.setObject(param_SMSTYPE, "MGM");
		ViralData[] viral = getViraldata(task);
		task.setObject(param_callerid, callerid);
		if (viral == null || viral.length < countMgmRequest) {
			return true;
		}

		return false;
	}


	/*@Deepak Kumar
	 * For Enabling Randomization RBT-5349
	 */
	public void enableRandomization(Task task){
		
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(subscriber!=null && subscriber.isUdsOn()){
			task.setObject(param_responseSms, getSMSTextForID(task,
					RANDOMIZATION_ALREADY_ENABLED, m_randomizationAlreadyEnabled, subscriber.getLanguage()));
			return;
		}
		SelectionRequest selectionRequest = new SelectionRequest(null);
		selectionRequest.setSubscriberID(subscriberID);
	    rbtClient.shuffleDownloads(selectionRequest);	
	    String response = selectionRequest.getResponse();
	    if(getParamAsString(SMS, "ENABLE_UDS_OPTIN_THROUGH_RANDOMIZATION", "FALSE").equalsIgnoreCase("TRUE")){
		    UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(null);
		    updateRequest.setSubscriberID(subscriberID);
		    updateRequest.setIsUdsOn(true);
		    rbtClient.setSubscriberDetails(updateRequest);
		    response = updateRequest.getResponse();
	    }
	    if(response.equalsIgnoreCase(SUCCESS)||response.equalsIgnoreCase(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)){
			task.setObject(param_responseSms, getSMSTextForID(task,
					RANDOMIZATION_ENABLING_SUCCESS, m_randomizationEnablingSuccess, subscriber.getLanguage()));
			task.setObject(param_respMessage, "SUCCESS");
			return;
	    }
	    
		task.setObject(param_responseSms, getSMSTextForID(task,
				RANDOMIZATION_ENABLING_FAILURE, m_randomizationEnablingFailure, subscriber.getLanguage()));
		return;

	    
	}
	
	/*@Deepak Kumar
	 * For Disabling Randomization RBT-5349
	 */
	public void disableRandomization(Task task){
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(!subscriber.isUdsOn()){
			task.setObject(param_responseSms, getSMSTextForID(task,
					RANDOMIZATION_ALREADY_DISABLED, m_randomizationAlreadyDisabled, subscriber.getLanguage()));
			return;
		}
		SelectionRequest selectionRequest = new SelectionRequest(null);
		selectionRequest.setSubscriberID(subscriberID);
        rbtClient.disableRandomization(selectionRequest);
        if(selectionRequest.getResponse().equalsIgnoreCase(SUCCESS)){
		     task.setObject(param_responseSms, getSMSTextForID(task,
					RANDOMIZATION_DISABLING_SUCCESS, m_randomizationDisablingSuccess, subscriber.getLanguage()));
			 return;
        }

		task.setObject(param_responseSms, getSMSTextForID(task,
				RANDOMIZATION_DISABLING_FAILURE, m_randomizationDisablingFailure, subscriber.getLanguage()));
		return;

	}
	
	public void processSongCodeRequest(Task task){
		ArrayList<String> smsWords =(ArrayList<String>)task.getObject(param_smsText);
		if(smsWords == null || smsWords.size() != 1){
			task.setObject(param_responseSms, getSMSTextForID(task,
					SONG_CODE_REQUEST_FORMAT_FAILURE, m_songCodeRequestFormatFailure));
		 return;
		}
		String promoId = smsWords.get(0);
		Clip clip = RBTCacheManager.getInstance().getClipByPromoId(promoId);
		if(clip!=null){
            promoId = clip.getClipPromoId();
            String songName = clip.getClipName();
            String artist = clip.getArtist();
            String album = clip.getAlbum();

            String smsToBeSent = getSMSTextForID(task,
					SONG_CODE_REQUEST_SUCCESS, m_songCodeRequestDefaultText);
            smsToBeSent = smsToBeSent.replaceAll("%PROMO_ID%", promoId);
            smsToBeSent = smsToBeSent.replaceAll("%SONG_NAME%", songName);
            smsToBeSent = smsToBeSent.replaceAll("%ARTIST%", artist);
            smsToBeSent = smsToBeSent.replaceAll("%ALBUM%", album);
			task.setObject(param_responseSms,smsToBeSent);
            return; 
		}
		task.setObject(param_responseSms, getSMSTextForID(task,
				SONG_CODE_REQUEST_FAILURE, m_songCodeRequestResultFailure));
        	
	}
	
	/*@Deepak Kumar
	 * For Viral SMS Start RBT-5060
	 */
	public void processViralStop(Task task){
		try
		{
			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			ViralBlackListTable viralBlackList = RBTDBManager.getInstance().getViralBlackList(subscriber.getSubscriberID(), "VIRAL");
			String language = subscriber.getLanguage();
			if (viralBlackList!=null)
			{
				// Viral Black listed, that is why returning 
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_STOP_ALREADY_ENABLED, m_viralStopAlreadyEnabled, language));
				return;
			}
			
			UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
					subscriber.getSubscriberID(), true, "VIRAL");
			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			if(updateDetailsRequest.getResponse().equalsIgnoreCase(WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_STOP_SUCCESS, m_viralStopSuccessDefault, language));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_STOP_FAILURE, m_viralStopFailureDefault, language));
			
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			task.setObject(param_responseSms, Resp_Err);
		}

	}
	
   /*
    * @Deepak Kumar
    * For Viewing the Subscriber Subscription Statistics Details
    */	
	public void viewSubscriptionStatistics(Task task){
		String subscriberID = task.getString(param_subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		rbtDetailsRequest.setMode("CCC");
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		if(subscriber==null || !subscriber.isValidPrefix()){
			task.setObject(param_responseSms, Resp_InvalidPrefix);
			return;
		}
		Date nextBillDate = subscriber.getNextBillingDate();
		Date lastBillDate = subscriber.getNextChargingDate();
		String subClass = subscriber.getSubscriptionClass();
		String noOfSongsInGallery = "0";
		RbtDetailsRequest rbtDetailRequest = new RbtDetailsRequest(subscriberID);
		Downloads downloads = rbtClient.getDownloads(rbtDetailRequest);
		if(downloads!=null){
			Download []subDownloads = downloads.getDownloads();
			 if(subDownloads!=null){
		         noOfSongsInGallery = subDownloads.length+"";
			 }
		}
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setInfo(SUBSCRIPTION_CLASS);
		applicationDetailsRequest.setName(subClass);
		String subscriptionStatus = null;
		com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriptionClass subscriptionClass = rbtClient.getSubscriptionClass(applicationDetailsRequest);
		if(subscriptionClass!=null && (subscriptionClass.getPeriod().equalsIgnoreCase("w1")||
				              subscriptionClass.getPeriod().equalsIgnoreCase("D7"))){
			 subscriptionStatus = "Weekly";
		}else if(subscriptionClass!=null && (subscriptionClass.getPeriod().equalsIgnoreCase("M1")||
				              subscriptionClass.getPeriod().equalsIgnoreCase("D30"))){
			 subscriptionStatus = "Monthly";
		}
		String statisticsResponse = getSMSTextForID(task, "SUBSCRIPTION_STATISTIC_RESPONSE", m_viewSubscriptionStatisticsDefault,subscriber.getLanguage());
		statisticsResponse = statisticsResponse.replace("%SUBSCRIPTION_STATUS%", getSMSTextForID(task, "SUBCLASS_"+subscriptionStatus.toUpperCase(), subscriptionStatus, subscriber.getLanguage()));
		statisticsResponse = statisticsResponse.replace("%LAST_BILLING_DATE%", lastBillDate!=null?lastBillDate.toString():"NA");
		statisticsResponse = statisticsResponse.replace("%NEXT_BILLING_DATE%", nextBillDate!=null?nextBillDate.toString():"NA");
		statisticsResponse = statisticsResponse.replace("%NO_OF_SONGS_IN_GALLARY%", noOfSongsInGallery);
	    task.setObject(param_responseSms, statisticsResponse);
	}
	
	/*@Deepak Kumar
	 * For Viral SMS Stop RBT-5060
	 */
	public void processViralStart(Task task){
		try
		{
			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			String language = subscriber.getLanguage();
			ViralBlackListTable viralBlackList = RBTDBManager.getInstance().getViralBlackList(subscriber.getSubscriberID(), "VIRAL");

			if (viralBlackList==null)
			{
				// Not Viral BlackListed, that is why returning
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_START_ALREADY_ENABLED, m_viralStartAlreadyEnabled, language));
				return;
			}
			
			UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
					subscriber.getSubscriberID(), false, "VIRAL");
			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			
			if(updateDetailsRequest.getResponse().equalsIgnoreCase(WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_START_SUCCESS, m_viralStartSuccessDefault, language));
			else if(updateDetailsRequest.getResponse().equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX))
				task.setObject(param_responseSms,getSMSTextForID(task,
						INVALID_PREFIX, m_invalidPrefixDefault, language) );
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_START_FAILURE, m_viralStartFailureDefault, language));

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			task.setObject(param_responseSms, Resp_Err);
		}

	}

	private String getPromoCodeForTopListCode(Task task, String tpCode) {
		try {
			task.setObject(param_SMSTYPE, "CATEGORY");
			ViralData[] context = getViraldata(task);
			if (context == null)
				return null;
			if (context[0] != null && context[0].getClipID() != null
					&& !context[0].getClipID().startsWith("TOP:"))
				return null;
			int tokenNumber = -1;
			tokenNumber = Integer.parseInt(tpCode) - 1;
			StringTokenizer stk = new StringTokenizer(context[0].getClipID(),
					",");
			for (int i = 0; i <= tokenNumber; i++)
				stk.nextToken();
			String clipId = stk.nextToken();
			Clip clip = getClipById(clipId, null);
			return clip.getClipPromoId();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isExistingSelection(String subscriberID, int clipId) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		Library lib = rbtClient.getLibrary(rbtDetailsRequest);
		Settings settings = lib.getSettings();
		Setting[] settingsArr = settings.getSettings();
		for (Setting setting : settingsArr) {
			if (!(setting.getCallerID() == null || setting.getCallerID()
					.equalsIgnoreCase("all")))
				continue;
			if (setting.getToneID() == clipId) {
				logger.info("is existing selection = true");
				return true;
			}
		}
		return false;
	}

	public ViralData[] reorderViralData(ViralData[] viralDataArr) {
		if (viralDataArr == null)
			return null;
		ViralData data = null;
		for (int i = 0; i < viralDataArr.length; i++) {
			for (int j = i; j < viralDataArr.length; j++) {
				if (viralDataArr[i].getSentTime().compareTo(
						viralDataArr[j].getSentTime()) > 0) {
					data = viralDataArr[i];
					viralDataArr[i] = viralDataArr[j];
					viralDataArr[j] = data;
				}
			}
		}
		return viralDataArr;
	}

	public boolean isValidIP(String strIP) {
		String validIPStr = param(COMMON, VALID_IP, "");
		ArrayList<String> ipList = tokenizeArrayList(validIPStr, null);
		if (ipList.contains(strIP.toLowerCase()))
			return true;
		return false;
	}

	public boolean isValidPrepaidIP(String strIP) {
		String validIPStr = param(SMS, VALID_PREPAID_IP, "");
		ArrayList<String> ipList = tokenizeArrayList(validIPStr, null);
		if (ipList.contains(strIP.toLowerCase()))
			return true;
		return false;
	}

	public String getSMSTextForID(Task task, String SMSID, String defaultText,
			String language) {

		if (task != null) {
			if (SMSID.toLowerCase().contains("success"))
				task.setObject(param_finalResponse, "SUCCESS");
			else
				task.setObject(param_finalResponse, "FAILURE");
		}
		logger.info(" getSMSTextForID smsID : " + SMSID + " defaultText : "
				+ defaultText + " language : " + language);
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				SMSID, language);
		logger.info(" getSMSTextForID smsText : " + smsText);
		if (smsText != null && !smsText.isEmpty())
			return smsText;
		else
			return defaultText;

	}
	
	public String getSMSText(String type, String subType, String defaultValue,
			String language, String circleId) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				type, subType, language, circleId);
		if (smsText != null)
			return smsText;
		else
			return defaultValue;
	}

	@Override
	public void loadSMSTexts() {

		if (smsTextMap != null)
			return;
		synchronized (syncObject) {
			if (smsTextMap != null)
				return;

			HashMap<String, String> smsTable = new HashMap<String, String>();
			SMSText[] smsTexts = rbtClient
					.getSMSTexts(new ApplicationDetailsRequest());
			if (smsTexts != null && smsTexts.length > 0) {
				for (int i = 0; i < smsTexts.length; i++) {
					String bulkPromoId = smsTexts[i].getType();
					HashMap<String, String> conditionMap = smsTexts[i]
							.getSmsConditionMap();
					if (conditionMap != null && conditionMap.size() > 0) {
						Iterator<String> it = conditionMap.keySet().iterator();
						while (it.hasNext()) {
							String thisName = bulkPromoId;
							String key = it.next();
							String value = conditionMap.get(key);
							if (key != null && !key.equalsIgnoreCase(""))
								thisName += "_" + key;
							smsTable.put(thisName, value);
						}
					}
				}
			}
			smsTextMap = smsTable;
		}
	}

	private void getCallerID(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() <= 0)
			return;
		String token = null;
		for (int i = 0; i < smsList.size(); i++) {
			try {
				token = smsList.get(i);
				if (token.startsWith("+"))
					token = token.substring(1);
				token = subID(token);
				if (token.length() >= param(GATHERER, PHONE_NUMBER_LENGTH_MIN,
						10)
						&& token.length() <= param(GATHERER,
								PHONE_NUMBER_LENGTH_MAX, 10)) {
					Long.parseLong(token);
					Clip clip = getClipByPromoId(token, null);
					if (clip == null && param(SMS, CHECK_CLIP_SMS_ALIAS, false)) {
						clip = getClipByAlias(token, null);
					}
					if (clip != null)
						continue;
					else {
						Category category = getCategoryByPromoId(token, null);
						if (category == null
								&& isCheckCategorySmsAlias) {
							category = getCategoryBySMSAlias(token, null);
						}
						if (category != null) {
							continue;
						}
					}
					smsList.remove(i);
					task.setObject(param_callerid, token);
				}
			} catch (Exception e) {
			}
		}
		return;
	}

	private void getLang(Task task) {
		String lang = "eng";
		Site site = getSite(task);
		if (site != null) {
			String[] languagesSupported = site.getSupportedLanguages();
			if (languagesSupported != null && languagesSupported.length > 0) {
				lang = languagesSupported[0];
				@SuppressWarnings("unchecked")
				ArrayList<String> smsList = (ArrayList<String>) task
						.getObject(param_smsText);
				if (smsList != null && smsList.size() > 0) {
					for (int i = 0; i < languagesSupported.length; i++) {
						if (smsList.contains(languagesSupported[i])) {
							lang = languagesSupported[i];
							smsList.remove(lang);
							break;
						}
					}
				}
			}
			task.setObject(param_language, lang);
		}
	}

	private void getSearchOn(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		Map<String, String> searchOnMap = new HashMap<String, String>();
		String mappings = param(SMS, REQUEST_SEARCH_ON_MAP,
				"song:song;album:movie;artist:artist");
		StringTokenizer stkParent = new StringTokenizer(mappings.toLowerCase(),
				";");
		while (stkParent.hasMoreTokens()) {
			StringTokenizer stkSingleElement = new StringTokenizer(stkParent
					.nextToken(), ":");
			String searchOn = stkSingleElement.nextToken();
			if (stkSingleElement.hasMoreTokens()) {
				StringTokenizer searchOnKeywords = new StringTokenizer(
						stkSingleElement.nextToken(), ",");
				while (searchOnKeywords.hasMoreTokens())
					searchOnMap.put(searchOnKeywords.nextToken(), searchOn);
			}
		}
		String searchType = RBTParametersUtils.getParamAsString(SMS, "DEFAULT_SEARCH_TYPE", "song");
		for (int i = 0; i < smsList.size(); i++) {
			if (searchOnMap.containsKey(smsList.get(i))) {
				searchType = searchOnMap.get(smsList.get(i));
				smsList.remove(i);
				break;
			}
		}
		task.setObject(SEARCH_TYPE, searchType);
	}

	public static String finalizeSmsText(HashMap<String, String> hashMap) {
		String smsText = hashMap.get("SMS_TEXT");
		if (smsText == null || smsText.length() <= 0)
			return smsText;
		
		smsText = substitutePackNameValidDays(smsText,hashMap.get("COS_KEYWORD"));
		
		if (hashMap.containsKey("CALLER_ID"))
			smsText = Utility.findNReplaceAll(smsText, "%CALLER_ID", hashMap
					.get("CALLER_ID"));
		else
			smsText = Utility.findNReplaceAll(smsText, "%CALLER_ID", param(SMS,
					SMS_TEXT_FOR_ALL, "all"));
		smsText = Utility.findNReplaceAll(smsText, "%SONG_NAME", hashMap
				.get("SONG_NAME"));
		smsText = Utility.findNReplaceAll(smsText, "%ARTIST", hashMap
				.get("ARTIST"));
		smsText = Utility.findNReplaceAll(smsText, "%END_TIME", hashMap
				.get("END_TIME"));
		smsText = Utility.findNReplaceAll(smsText, "%CALLER_ID", hashMap
				.get("CALLER_ID"));
		
		String specialAmtChar = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
				"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
		if (smsText.contains("%FREE_CHARGE_TEXT")
				&& null != hashMap.get("SEL_AMT")
				&& !hashMap.get("SEL_AMT").isEmpty()
				&& Double.parseDouble(hashMap.get("SEL_AMT").replace(
						specialAmtChar, ".")) == 0) {
			smsText = Utility.findNReplaceAll(smsText, "%FREE_CHARGE_TEXT", hashMap
					.get("FREE_PERIOD_TEXT"));
			smsText = Utility.findNReplaceAll(smsText, "%SEL_AMT", hashMap
					.get("RENEWAL_AMOUNT"));
		}else{
			smsText = Utility.findNReplaceAll(smsText, "%FREE_CHARGE_TEXT", "");
			smsText = Utility.findNReplaceAll(smsText, "%SEL_AMT", hashMap
					.get("SEL_AMT"));
		}
		smsText = Utility.findNReplaceAll(smsText, "%ACT_AMT", hashMap
				.get("ACT_AMT"));
		smsText = Utility.findNReplaceAll(smsText, "%AMT", hashMap.get("AMT"));
		smsText = Utility.findNReplaceAll(smsText, "%DURATION", hashMap
				.get("DURATION"));
		smsText = Utility.findNReplaceAll(smsText, "%CAT_NAME", hashMap
				.get("CAT_NAME"));
		smsText = Utility.findNReplaceAll(smsText, "%OPERATOR_NAME", param(SMS,
				OPERATOR_NAME, null));
		
		String circleID = hashMap.get("CIRCLE_ID");
		String brandName = Utility.getBrandName(circleID);
		smsText = Utility.findNReplaceAll(smsText, "%BRAND_NAME", brandName);
		
		smsText = Utility.findNReplaceAll(smsText, "%OPERATOR_SHORTCODE",
				param(SMS, OPERATOR_SHORTCODE, null));
		smsText = Utility.findNReplaceAll(smsText, "%REPLACE_TEXT", hashMap
				.get("REPLACE_TEXT"));
		smsText = Utility.findNReplaceAll(smsText, "%START_TIME", hashMap
				.get("START_TIME"));
		smsText = Utility.findNReplaceAll(smsText, "%COUNT", hashMap
				.get("COUNT"));
		smsText = Utility.findNReplaceAll(smsText, "%ACT_DATE", hashMap
				.get("ACT_DATE"));
		smsText = Utility.findNReplaceAll(smsText, "%SELECTIONS", hashMap
				.get("SELECTIONS"));
		smsText = Utility.findNReplaceAll(smsText, "%DOWNLOADS", hashMap
				.get("DOWNLOADS"));
		smsText = Utility.findNReplaceAll(smsText, "%SONG_LIST", hashMap
				.get("SONG_LIST"));
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		smsText = Utility.findNReplaceAll(smsText, "%COS_LIST", hashMap
				.get("COS_LIST"));
		smsText = Utility.findNReplaceAll(smsText, "%AZAAN_PACK", hashMap
				.get("AZAAN_PACK"));
		smsText = Utility
				.findNReplaceAll(smsText, "%CODE", hashMap.get("CODE"));
		smsText = Utility.findNReplaceAll(smsText, "%DAYS_LEFT", hashMap
				.get("DAYS_LEFT"));
		smsText = Utility.findNReplaceAll(smsText, "%DEACT_CONFIRM_DAYS",
				hashMap.get("DEACT_CONFIRM_DAYS"));
		smsText = Utility.findNReplaceAll(smsText, "%MORE_TEXT", hashMap
				.get("MORE_TEXT"));
		smsText = Utility.findNReplaceAll(smsText, "%SMS_ID", hashMap
				.get("SMS_ID"));
		smsText = Utility.findNReplaceAll(smsText, "%KEYWORD", hashMap
				.get("KEYWORD"));
		smsText = Utility.findNReplaceAll(smsText, "%PROMO_ID", hashMap
				.get("PROMO_ID"));
		smsText = Utility.findNReplaceAll(smsText, "%LOTTERY_LIST", hashMap
				.get("LOTTERY_LIST"));
		smsText = Utility.findNReplaceAll(smsText, "%DOWNLOAD_COUNT", hashMap
				.get("DOWNLOAD_COUNT"));
		smsText = Utility.findNReplaceAll(smsText, "%FROM_TIME", hashMap
				.get("FROM_TIME"));
		smsText = Utility.findNReplaceAll(smsText, "%TO_TIME", hashMap
				.get("TO_TIME"));
		smsText = Utility.findNReplaceAll(smsText, "%PROFILE_HOURS", hashMap
				.get("PROFILE_HOURS"));
		smsText = Utility.findNReplaceAll(smsText, "%ALBUM", hashMap
				.get("ALBUM"));
		
		
		String senderNumber = Utility.getSenderNumberbyType("SMS", circleID, "SENDER_NO");
		smsText = Utility.findNReplaceAll(smsText, "%SENDER_NO", senderNumber);

		return smsText;
	}
	
	@Override
	public void proceesPOLLON(Task task) {
		logger.info("inside processPollON");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		HashMap<String, String> extraInfoMap = subscriber.getUserInfoMap();
		String pollExtraInfo = null;
		if (extraInfoMap != null && extraInfoMap.get(PLAY_POLL_STATUS) != null)
			pollExtraInfo = extraInfoMap.get(PLAY_POLL_STATUS);

		if (!isUserActive(subscriber.getStatus())) {
			subscriber = processActivation(task);
			if (isUserActive(subscriber.getStatus())) {
				processupdateExtraInfoStatus(task, PLAY_POLL_STATUS,
						PLAY_POLL_STATUS_ON);
				task.setObject(param_responseSms, getSMSTextForID(task,
						POLL_ON_SUCCESS, m_pollONSuccessDefault, subscriber
								.getLanguage()));
				return;
			} else
				task.setObject(param_responseSms, getSMSTextForID(task,
						POLL_ON_TECHNICAL_DIFFICULTIES,
						m_pollONTechnicalDifficultiesDefault, subscriber
								.getLanguage()));
		} else {
			boolean res = true;
			if (pollExtraInfo == null
					|| pollExtraInfo.equals(PLAY_POLL_STATUS_OFF)) {
				res = processupdateExtraInfoStatus(task, PLAY_POLL_STATUS,
						PLAY_POLL_STATUS_OFF);
				if (res) {
					task.setObject(param_actby, "SMS");
					processDeactivateSelection(task);
					task.setObject(param_responseSms, getSMSTextForID(task,
							POLL_ON_SUCCESS, m_pollONSuccessDefault, subscriber
									.getLanguage()));
					return;
				} else
					task.setObject(param_responseSms, getSMSTextForID(task,
							POLL_ON_TECHNICAL_DIFFICULTIES,
							m_pollONTechnicalDifficultiesDefault, subscriber
									.getLanguage()));
			} else
				task.setObject(param_responseSms, getSMSTextForID(task,
						POLL_ON_REPEAT, m_pollONRepeatDefault, subscriber
								.getLanguage()));
		}
	}

	@Override
	public void processPollOFF(Task task) {
		logger.info("inside processPollOFF");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					POLL_OFF_SUBSCRIBER_NOT_ACTIVE,
					m_pollSuscriberNotActiveDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, PLAY_POLL_STATUS,
				PLAY_POLL_STATUS_OFF);
		task.setObject(param_responseSms, getSMSTextForID(task,
				POLL_OFF_SUCCESS, m_pollOFFSuccessDefault, subscriber
						.getLanguage()));
	}

	@Override
	public void processDisableIntro(Task task) {
		logger.info("inside processDisableIntro");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, null, DISABLE_INTRO);
		task.setObject(param_responseSms, getSMSTextForID(task,
				INTRO_PROMPT_DISABLE_SUCCESS, m_disableIntroSuccessDefault,
				subscriber.getLanguage()));
	}

	@Override
	public void processDisableOverlay(Task task) {
		logger.info("inside processDisableOverlay");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, null, "DISABLE_OVERLAY");
		task.setObject(param_responseSms, getSMSTextForID(task,
				INTRO_PROMPT_DISABLE_SUCCESS, m_disableIntroSuccessDefault,
				subscriber.getLanguage()));
	}

	@Override
	public void processEnableOverlay(Task task) {
		logger.info("inside processEnableOverlay");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, null, "ENABLE_OVERLAY");
		task.setObject(param_responseSms, getSMSTextForID(task,
				INTRO_PROMPT_ENABLE_SUCCESS, m_enableIntroSuccessDefault,
				subscriber.getLanguage()));
	}

	@Override
	public void setNewsletterOn(Task task) {
		logger.info("inside setNewsletterOn");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, null, "SET_NEWSLETTER_ON");
		task.setObject(param_responseSms, getSMSTextForID(task,
				NEWSLETTER_ON_SUCCESS, m_newsLetterOnSuccessDefault, subscriber
						.getLanguage()));
	}

	@Override
	public void setNewsLetterOff(Task task) {
		logger.info("inside setNewsLetterOff");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		processupdateExtraInfoStatus(task, null, "SET_NEWSLETTER_OFF");
		task.setObject(param_responseSms, getSMSTextForID(task,
				NEWSLETTER_OFF_SUCCESS, m_newsLetterOffSuccessDefault,
				subscriber.getLanguage()));
	}

	@Override
	public void processMGM(Task task) {

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		task.setObject(param_mode, "CCC");
		Subscriber mgmAgent = getSubscriber(task);

		String mgmConfig = param(SMS, MGM_PARAMS, "OFF,MGM,MGM,7,2");
		StringTokenizer token = new StringTokenizer(mgmConfig, ",");
		@SuppressWarnings("unused")
		boolean mgmOn = token.nextToken().equalsIgnoreCase("TRUE");
		@SuppressWarnings("unused")
		String mgmKeyword = token.nextToken().toLowerCase();
		@SuppressWarnings("unused")
		String mgmActBy = token.nextToken().toLowerCase();
		int mgmMinNoDaysActive = Integer.parseInt(token.nextToken());
		int mgmMaxGiftsMonth = Integer.parseInt(token.nextToken());

		if (!isSubActiveForDays(mgmAgent, mgmMinNoDaysActive)) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, MGM_SENDER_MIN_ACT_FAILURE,
							m_mgmSenderMinActFailureTextDefault, mgmAgent
									.getLanguage()));
			return;
		}

		if (!isMgmMoreGiftAllowed(mgmAgent.getSubscriberID(), mgmMaxGiftsMonth)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MGM_SENDER_MAX_GIFT_FAILURE,
					m_mgmSenderMaxGiftFailureTextDefault, mgmAgent
							.getLanguage()));
			return;
		}

		String callerID = task.getString(param_callerid);
		task.setObject(param_subscriberID, callerID);
		Subscriber caller = getSubscriber(task);
		task.setObject(param_subscriber, mgmAgent);
		task.setObject(param_subscriberID, mgmAgent.getSubscriberID());
		if (isUserActive(caller.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MGM_SENDER_FAILURE, m_mgmSenderFailureTextDefault, mgmAgent
							.getLanguage()));
			return;
		}

		if (smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));
		Clip clipMinimal = (Clip) task.getObject(CLIP_OBJ);
		Category category = (Category) task.getObject(CAT_OBJ);
		String contextClipID = "act";

		String name = null;
		String id = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			id = "" + clipMinimal.getClipId();
		} else if (category != null) {
			name = category.getCategoryName();
			id = "C" + category.getCategoryId();
		}
		if (id != null && id.trim().length() > 0
				&& !id.trim().equalsIgnoreCase("null"))
			contextClipID = contextClipID + " " + id;

		removeViraldata(null, callerID, "MGM");
		addViraldata(mgmAgent.getSubscriberID(), callerID, "MGM",
				contextClipID, "SMS", 1, null);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, MGM_SENDER_SUCCESS,
				m_mgmSenderSuccessTextDefault, getSubscriber(callerID)
						.getLanguage()));
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SONG_NAME", name == null ? "" : name);
		hashMap.put("CIRCLE_ID", mgmAgent.getCircleID());
		String smsTextMgmSender = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsTextMgmSender);

		hashMap.put("SMS_TEXT", getSMSTextForID(task, MGM_RECIPIENT,
				m_mgmRecSMSDefault, getSubscriber(callerID).getLanguage()));
		hashMap.put("CALLER_ID", mgmAgent.getSubscriberID());
		hashMap.put("SONG_NAME", name == null ? "" : name);
		hashMap.put("CIRCLE_ID", mgmAgent.getCircleID());
		String smsTextMgmRecepient = finalizeSmsText(hashMap);
		task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
		task.setObject(param_Reciver, callerID);
		task.setObject(param_Msg, smsTextMgmRecepient);
		sendSMS(task);

	}

	@Override
	public void processRetailer(Task task) {
		Subscriber retailer = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String callerID = task.getString(param_callerid);
		if (callerID == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_FAILURE, m_retFailureTextDefault, retailer
							.getLanguage()));
			return;
		}

		String retailerId = retailer.getSubscriberID();
		task.setObject(param_subscriberID, callerID);
		Subscriber caller = getSubscriber(task);
		task.setObject(param_subscriber, retailer);
		boolean isActRequest = task.getString(IS_ACTIVATION_REQUEST)
				.equalsIgnoreCase("true");
		String subscriberID = caller.getSubscriberID();

		if (!caller.isValidPrefix()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_FAILURE, m_retFailureTextDefault, retailer
							.getLanguage()));
			return;
		}
		if (task.getString(IS_ACTIVATION_REQUEST).equalsIgnoreCase("true")) {
			if (!caller.getStatus().equalsIgnoreCase(
					WebServiceConstants.NEW_USER)
					&& !caller.getStatus().equalsIgnoreCase(
							WebServiceConstants.DEACTIVE)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE, m_retFailureTextDefault, retailer
								.getLanguage()));
				return;
			}
		} else {
			if (!caller.getStatus().equalsIgnoreCase(
					WebServiceConstants.ACT_PENDING)
					&& !caller.getStatus().equalsIgnoreCase(
							WebServiceConstants.ACTIVE)
					&& !caller.getStatus().equalsIgnoreCase(
							WebServiceConstants.RENEWAL_PENDING)
					&& !caller.getStatus().equalsIgnoreCase(
							WebServiceConstants.RENEWAL_PENDING)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE, m_retFailureTextDefault, retailer
								.getLanguage()));
				return;
			}
		}

		if (smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		Clip clipMinimal = (Clip) task.getObject(CLIP_OBJ);
		Category category = (Category) task.getObject(CAT_OBJ);
		String contextClipID = "act ";

		String name = null;
		String id = null;
		if (clipMinimal != null) {
			name = clipMinimal.getClipName();
			id = "" + clipMinimal.getClipId();
		} else if (category != null) {
			name = category.getCategoryName();
			id = "C" + category.getCategoryId();
		}
		if (id != null)
			contextClipID += id;

		if (clipMinimal == null
				&& !task.getString(IS_ACTIVATION_REQUEST).equalsIgnoreCase(
						"true")) {
			task.setObject(param_responseSms, isValidPromoId(task, smsList.get(0), caller.getLanguage()));
			return;
		}

		removeViraldata(subscriberID, null, "RETAILER");

		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (isActRequest)
			hashMap.put("SMS_TEXT", getSMSTextForID(task, RETAILER_SUCCESS,
					m_retSuccessTextDefault, caller.getLanguage()));
		else
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					RETAILER_SONG_SUCCESS, m_retSongSuccessTextDefault, caller
							.getLanguage()));
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SONG_NAME", name == null ? "" : name);
		hashMap.put("CIRCLE_ID", retailer.getCircleID());
		String smsTextRetailer = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsTextRetailer);
		hashMap.put("CALLER_ID", retailerId);
		if (isActRequest)
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					RETAILER_RESP_SMS_ACT, m_retReqResActDefault, caller
							.getLanguage()));
		else
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					RETAILER_RESP_SMS_SEL, m_retReqResSelDefault, caller
							.getLanguage()));

		hashMap.put("CIRCLE_ID", retailer.getCircleID());
		String smsTextRetailee = finalizeSmsText(hashMap);
		task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
		task.setObject(param_Reciver, callerID);
		task.setObject(param_Msg, smsTextRetailee);
		sendSMS(task);

		addViraldata(callerID, retailerId, "RETAILER", contextClipID, "SMS", 1,
				null);
	}

	@Override
	public void processTNB(Task task) {
		Subscriber sub = (Subscriber) task.getObject(param_subscriber);
		boolean update = updateSubscriber(task);
		if (update)
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_SUCCESS, m_tnbSuccessSMSDefault, sub.getLanguage()));
		else if(task.getString(param_response).equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, sub.getLanguage()));
		}else if(task.getString(param_response).equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, sub.getLanguage()));
		}
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_FAILURE, m_tnbFailureSMSDefault, sub.getLanguage()));
	}
	
	@Override
	public void processSMSRecommendSongs(Task task) {
		super.processSMSRecommendSongs(task);
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		String errorResponseSMS = CacheManagerUtil.getSmsTextCacheManager()
				.getSmsText(
						Constants.RECOMMEND_SONGS +"_ERROR",
						subscriber.getLanguage());
		
		task.setObject(param_responseSms, errorResponseSMS);
//		String responseSMS = null;
		try
		{
			Parameters biURLParam = null;
			ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
			
			biURLParam = parametersCacheManager.getParameter(iRBTConstant.BI, "BI_RECOMMENDATION_SONGS_URL", null);
			if(null == biURLParam){
				logger.warn("BI_RECOMMENDATION_SONGS_URL parameter is not configured");
				task.setObject(param_responseSms, errorResponseSMS);
				return;
			}
			String url = biURLParam.getValue().trim();
			String status = "INACTIVE";
			if(!Utility.isDeactive(subscriber.getStatus())) {
				status = "ACTIVE";
			}

			ViralData[] copyConfVst = getViraldata(null, subscriber.getSubscriberID(), COPYCONFPENDING);
			ViralData latestViralData = null;

			List<ViralData> completeViralDataList = new ArrayList<ViralData>();
			completeViralDataList.addAll(Arrays.asList(copyConfVst));

			Collections.sort(completeViralDataList);

			if (completeViralDataList.size() > 0)
				latestViralData = completeViralDataList.get(completeViralDataList.size() - 1);

			if (latestViralData == null) {
				logger.error("no entry found in the viral table for recommendation of songs");
				task.setObject(param_responseSms, errorResponseSMS);
				return;
			}
			logger.info("Latest Viral Data got : " + latestViralData.toString());
			String waveFileNameCombo = latestViralData.getClipID();
			String waveFileName = null;
			if(waveFileNameCombo != null) {
				waveFileName = waveFileNameCombo.split(":")[0];	
			}
			Clip clip = getClipByWavFile(waveFileName, subscriber.getLanguage());
			
			if (latestViralData.getType().equalsIgnoreCase(COPYCONFPENDING) && clip != null) {
				logger.info("configured BI url : " +url + " clip id :" + clip.getClipId() +" clip promo id :" +clip.getClipPromoId());
				try {
					url = url.replace("%SUBSCRIBER_ID%", subscriberID);
					url = url.replace("%STATUS%", 	status);
					url = url.replace("%PROMO_ID%", clip.getClipPromoId());

					logger.info("RBT:: processSMSRecommendSongs URL: " + url);
					HttpParameters httpParameters = new HttpParameters(url);
					logger.info("RBT:: httpParameters: " + httpParameters);

					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
					logger.info("RBT:: httpResponse: " + httpResponse);
					String BIresponse = httpResponse.getResponse();
					if(BIresponse != null) {
						if (copyConfVst != null && copyConfVst.length > 0)
							removeViraldata(null, subscriber.getSubscriberID(), "X"
									+ COPYCONFPENDING);
						task.setObject(param_responseSms, BIresponse);
						return;
					} else{
						logger.error("there was some error while getting response from the BI url : " + url);
						task.setObject(param_responseSms, errorResponseSMS);
					}

				} catch (Exception e) {
					logger.error("some error while replacing the attributes in the BI url for subscriberID :" + subscriberID +" promoID :" + clip.getClipPromoId() + " status :" +status);
					logger.error(e);
				}
			}
			
		}
		catch (Exception e)
		{
			logger.error("", e);
			task.setObject(param_responseSms, errorResponseSMS);
		}
	}

	@Override
	public void processRemoveNavraatri(Task task) {
		String subscriberID = task.getString(SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = task.getString(CALLER_ID);

		if (isDynamicShuffleSubscriber(task)) {
			logger
					.info("User has a dynamic shuffle and going to deactivate it");
			task.setObject(param_subscriberID, subscriberID);
			task.setObject(param_callerid, callerID);
			processDeactivateSelection(task);
			task.setObject(param_responseSms, getSMSTextForID(task,
					REMOVE_SEL_DYNAMIC_SUCCESS,
					dynamicShuffleRemoveSuccessDefault, subscriber
							.getLanguage()));
		} else {
			if (isUserActive(subscriber.getStatus())) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						REMOVE_SEL_DYNAMIC_FAILURE, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage()));
			} else
				task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
						m_errorDefault, subscriber.getLanguage()));
		}
	}

	@Override
	public void processManageRemoveSelection(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		if (smsList == null || smsList.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MANAGE_SELECTION_REMOVE_INVALID, m_manageDeactInvalidId,
					subscriber.getLanguage()));
			return;
		}

		String token = smsList.get(0).trim();

		task.setObject(param_SMSTYPE, "MANAGE");
		logger.info("gettig the viral data " + task);
		ViralData context[] = getViraldata(task);
		if (context == null || context.length <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MANAGE_SELECTION_REMOVE_FAILURE, m_manageDeactFailure,
					subscriber.getLanguage()));
			return;
		}
		String selectionRefIdMap = context[0].getClipID();
		if (!selectionRefIdMap.contains(token + ":")) {

			task.setObject(param_responseSms, getSMSTextForID(task,
					MANAGE_SELECTION_REMOVE_INVALID, m_manageDeactInvalidId,
					subscriber.getLanguage()));
			return;
		}
		logger.info("The selection ref id map for the subscriber is "
				+ selectionRefIdMap);
		String[] countRefMap = selectionRefIdMap.split(",");
		if (countRefMap == null || countRefMap.length <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MANAGE_SELECTION_REMOVE_FAILURE, m_manageDeactFailure,
					subscriber.getLanguage()));
			return;
		}

		Setting[] setting = getActiveSubSettings(subscriber.getSubscriberID(),
				1);
		boolean active = false;
		for (int i = 0; i < countRefMap.length; i++) {
			if (countRefMap[i].startsWith(token + ":")) {
				// deactivate where ref id contains
				String[] refId = countRefMap[i].split(":");
				for (int k = 0; k < setting.length; k++) {
					if (setting[k].getRefID().endsWith(refId[1])) {
						active = true;
						RBTDBManager.getInstance().deactivateSubscriberRecordsByRefId(
								subscriber.getSubscriberID(), "SMS", setting[k].getRefID());
						task.setObject(param_responseSms, getSMSTextForID(task,
								MANAGE_SELECTION_REMOVE_SUCCESS, m_manageDeactSuccess,
								subscriber.getLanguage()));
						return;
//						break;
					}
				}
				if (!active) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							MANAGE_SELECTION_REMOVE_ALREADY_DEACTIVE,
							m_manageAlreadyDeact, subscriber.getLanguage()));
					return;
				}
//				RBTDBManager.getInstance().deactivateSubscriberRecordsByRefId(
//						subscriber.getSubscriberID(), "SMS", refId[1]);
//				task.setObject(param_responseSms, getSMSTextForID(task,
//						MANAGE_SELECTION_REMOVE_SUCCESS, m_manageDeactSuccess,
//						subscriber.getLanguage()));
//				return;
			}
		}

	}

	@Override
	public void processRemoveTempOverride(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		
		boolean isOuiSmsRequest = isThisFeature(task,SmsKeywordsStore.ouiSmsRequestKeywordSet, OUI_SMS_KEYWORD);
		if(isOuiSmsRequest) {
			ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);
			if(smsList.size() == 0) {
				task.setObject(param_responseSms, getSMSTextForID(task,	OUI_SMS_KEYWORD_FAILURE, "Please check your sms text>", subscriber.getLanguage()));
				return;
			}
			task.setObject(param_ouiRegCode, smsList.remove(0));
		}
		
		List<Setting> settingList = new ArrayList<Setting>(); 

		if (isProfileSubscriber(subscriberID, settingList)) {
			task.setObject(param_subscriberID, subscriberID);
			task.setObject(param_status, "99");
			task.remove(param_catid);
			
			Clip clip = null;
			if(task.containsKey(param_ouiRegCode)) {
				clip = getProfileClip(task);
				if(clip == null) {
					//Error
					task.setObject(param_responseSms, getSMSTextForID(task,	OUI_SMS_KEYWORD_PROFILE_SEL_NOT_EXIST, "Profile selection not found", subscriber.getLanguage()));
					return;
				}
				task.setObject(param_clipid, clip.getClipId() + "");
				task.setObject(param_actInfo, "DCT_REG:" + task.getString(param_ouiRegCode));
				
			}
			
			
			String songName = settingList.get(0).getToneName();
			if(clip != null) {
				songName = clip.getClipName();
			}
			
			String response = processDeactivateSelection(task);			
			String smsText=getSMSTextForID(task,
					REMOVE_SEL_PROFILE_FAILED,removeProfileFailed,
					subscriber.getLanguage());
			if(response.equalsIgnoreCase(SUCCESS)) {
				smsText=getSMSTextForID(task,
						REMOVE_SEL_PROFILE_SUCCESS,removeProfileSuccess,
						subscriber.getLanguage());
			}
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SONG_NAME", songName);
			hashMap.put("SMS_TEXT", smsText);
			task.setObject(param_responseSms,finalizeSmsText(hashMap));
		} else {
			if (isUserActive(subscriber.getStatus())) {
				String smsText=getSMSTextForID(task,
						REMOVE_SEL_PROFILE_FAILURE, removeProfileFailure,
						subscriber.getLanguage());
				task.setObject(param_responseSms,smsText);

				} else {
				task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
						m_errorDefault, subscriber.getLanguage()));
			}
		}
	}

	@Override
	public void processCategorySearch(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (!(isUserActive(subscriber.getStatus())
				|| param(SMS, IS_ACT_OPTIONAL, false) || param(SMS,
				IS_ACT_OPTIONAL_REQUEST_RBT, false))) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}
		if (smsList == null || smsList.size() <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		int songNo = -1;
		try {
			if (smsList.size() == 1) {
				songNo = Integer.parseInt(smsList.get(0));
				setRequest(task, songNo, "CATEGORY");
			}
		} catch (Exception e) {
			songNo = -1;
		}
		if (songNo == -1)
			searchCategory(task, smsList.get(0));
	}

	private void searchCategory(Task task, String alias) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		if (task.getString(param_requesttype) != null
				&& task.getString(param_requesttype).equalsIgnoreCase(
						type_content_validator)) {
			task.setObject(param_ocg_charge_id, "NOTVALID");
			return;
		}

		Category category = getCategoryBySMSAlias(alias, subscriber
				.getLanguage());
		if (category == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					CATEGORY_SEARCH_FAILURE, m_catRbtFailure1Default,
					subscriber.getLanguage()));
			return;
		}

		int categoryid = category.getCategoryId();
		String categoryName = category.getCategoryName();

		String clipIDs = "" + categoryid;
		Clip[] clips = getClipsByCatId(categoryid, subscriber.getLanguage());
		if (clips == null || clips.length == 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					CATEGORY_SEARCH_FAILURE, m_catRbtFailure1Default,
					subscriber.getLanguage()));
			return;
		}

		for (int i = 0; i < clips.length; i++) {
			try {
				if (i < param(SMS, REQUEST_MAX_CLIP_SEARCHED, 15))
					clipIDs = clipIDs + "," + clips[i].getClipId();
				else
					break;
			} catch (Exception e) {
				System.out.println("exception is" + e);
			}
		}

		String match = "";
		StringTokenizer clipTokens = new StringTokenizer(clipIDs, ",");
		clipTokens.nextToken();
		int iSong = 0;
		String song = null;
		Clip clip = null;
		while (clipTokens.hasMoreTokens()) {
			clip = getClipById(clipTokens.nextToken(), subscriber.getLanguage());
			if (iSong < param(SMS, REQUEST_MAX_CAT_SMS, 5)) {
				song = clip.getClipName();

				/*
				 * Append the artist name with song name.
				 */
				if (param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)) {
					if (clip.getArtist() != null)
						song = song + " (" + clip.getArtist().trim() + ")";
				}

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if(param(SMS, INSERT_SEARCH_NUMBER_AT_BEGINNING, false)){
						match = match + (iSong + 1) + "-";
					}
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
				} else if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
					match = match + song + "-" + (iSong + 1) + " ";
				else
					match = match + (iSong + 1) + "-" + song + " ";

			} else
				break;

			iSong++;
		}
		task.setObject(param_SMSTYPE, "CATEGORY");
		removeViraldata(task);

		addViraldata(subscriberID, null, "CATEGORY", clipIDs, "SMS", 1, null);

		// String smsText = "To set the song ";
		// categorySearch - replace smsText(%SONG_LIST% XX %MORE%) with song
		// list and more,
		// clipSearch - same as above
		// getmoreclips
		HashMap<String, String> hashMap = new HashMap<String, String>();
		// hashMap.put("SMS_TEXT", match + getSMSTextForID(task,CATEGORY_SEARCH,
		// m_catRbtSuccess1Default));
		hashMap.put("SMS_TEXT", getSMSTextForID(task, CATEGORY_SEARCH_SUCCESS,
				m_catRbtSuccess1Default, subscriber.getLanguage()));
		hashMap.put("CAT_NAME", categoryName);
		hashMap.put("COUNT", param(SMS, REQUEST_MAX_CAT_SMS, "10"));
		hashMap.put("SONG_LIST", match);
		// String finalText = finalizeSmsText(hashMap, subscriber.getCircleID());

		// if (clips.length > param(SMS,REQUEST_MAX_CAT_SMS,5))
		// finalText = finalText +
		// getSMSTextForID(task,REQUEST_MORE_CAT,m_reqMoreSMSCatDefault);

		if (clips.length > param(SMS, REQUEST_MAX_CAT_SMS, 5))
			hashMap.put("MORE_TEXT", getSMSTextForID(task,
					REQUEST_MORE_CAT_SUCCESS, m_reqMoreSMSCatDefault,
					subscriber.getLanguage()));
		else
			hashMap.put("MORE_TEXT", "");

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String finalText = finalizeSmsText(hashMap);

		task.setObject(param_responseSms, finalText);
	}

	@Override
	public void processREQUEST(Task task) {
		ArrayList<?> smsList = (ArrayList<?>) task.getObject(param_smsText);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (!(isUserActive(subscriber.getStatus())
				|| param(SMS, IS_ACT_OPTIONAL, false) || param(SMS,
				IS_ACT_OPTIONAL_REQUEST_RBT, false))) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		if (smsList == null || smsList.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		int songNo = -1;
		try {
			if (smsList.size() == 1) {
				songNo = Integer.parseInt((String) smsList.get(0));
				// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
				String type = "REQUEST";
				if (null != SmsKeywordsStore.azaanSearchKeywordsSet) {
					type = type + ",AZAAN_REQUEST";
				}
				setRequest(task, songNo, type);
			}
		} catch (Exception e) {
			songNo = -1;
		}
		if (songNo == -1) {
			getSearchOn(task);
			String searchString = " ";
			String tmp = null;
			for (int k = 0; k < smsList.size(); k++) {
				tmp = (String) smsList.get(k);
				tmp = replaceSpecialChars(tmp);
				if (tmp.trim().length() > 0)
					searchString = searchString.trim() + " " + tmp.trim();
			}
			task.setObject(SEARCH_STRING, searchString.trim());
			searchRequest(task);
		}
	}
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	@Override
	public void processAzaanSearchRequest(Task task) {
		ArrayList<?> smsList = (ArrayList<?>) task.getObject(param_smsText);
		int cosId = -1;
		try {
			if (smsList.size() == 1) {
				cosId = Integer.parseInt((String) smsList.get(0));
				setRequest(task, cosId, "AZAAN_REQUEST");
			}
		} catch (Exception e) {
			cosId = -1;
		}
		if (cosId == -1) {
			searchAzaanPack(task, cosId);
		}
	}

	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	private void searchAzaanPack(Task task, int cosId) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		String cosIdOrderFilter = param(SMS, REQUEST_COSID_ORDER_FILTER, null);
		List<CosDetails> cosList = new ArrayList<CosDetails>();
		if (cosIdOrderFilter == null || cosIdOrderFilter.isEmpty()) {
			logger.warn("Not processing activation, cos order configuration is not exists. subscriberID: "
					+ subscriberID
					+ " REQUEST_COSID_ORDER__FILTER value is : "
					+ cosIdOrderFilter);
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, AZAAN_SEARCH_FAILURE,
							m_requestAzaanSearchFailureDefault,
							subscriber.getLanguage()));
			return;
		}
		int i = 0;
		List<String> cosIdsLst = Arrays.asList(cosIdOrderFilter.split(","));
		for (String cosID : cosIdsLst) {
			CosDetails cosDetails = (CosDetails) CacheManagerUtil
					.getCosDetailsCacheManager().getActiveCosDetail(cosID,
							subscriber.getCircleID());
			if (null != cosDetails && null != cosDetails.getCosType()
					&& cosTypesForMultiPack != null
					&& cosTypesForMultiPack.contains(cosDetails.getCosType())) {
				cosList.add(cosDetails);
			}
		}
		String azaanCosIdsList = "";
		for (CosDetails cos : cosList) {
			try {
				if (i < param(SMS, REQUEST_MAX_AZAAN_SEARCHED, 15)) {
					azaanCosIdsList = azaanCosIdsList + cos.getCosId() + ",";
				} else
					break;
			} catch (Exception e) {
				System.out.println("exception is" + e);
			}
		}

		String match = "";
		String[] azaanCosIds = azaanCosIdsList.split(",");
		int iCos = 0;
		String activationPrompt = "";
		for (String cosID : azaanCosIds) {
			if (iCos < param(SMS, REQUEST_MAX_AZAAN_SMS, 5)) {
				CosDetails cosDetails = (CosDetails) CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);
				activationPrompt = cosDetails.getActivationPrompt();
				if (param(SMS, INSERT_SEARCH_NUMBER_AT_BEGINNING, false)) {
					match = match + (iCos + 1) + "-" + activationPrompt + " ";
				} else if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
					match = match + activationPrompt + "-" + (iCos + 1) + " ";
				else
					match = match + (iCos + 1) + "-" + activationPrompt + " ";

			} else
				break;
			iCos++;
		}
		task.setObject(param_SMSTYPE, "AZAAN_REQUEST");
		removeViraldata(task);
		addViraldata(subscriberID, null, "AZAAN_REQUEST", azaanCosIdsList,
				"SMS", 1, null);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		/*
		 * Get the configured text for the AZAAN_SEARCH_SUCCESS_<responses>, if
		 * that message is not configured then it will take the above default
		 * message.
		 */
		String defaultConfTextForSuccessRequest = getSMSTextForID(task,
				AZAAN_SEARCH_SUCCESS, m_requestAzaanSuccessDefault,
				subscriber.getLanguage());
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS_<responses>,
		 * if that message is not configured then it will take the above default
		 * message.
		 */
		StringBuffer confForSuccessRequest = new StringBuffer(
				AZAAN_SEARCH_SUCCESS);
		confForSuccessRequest.append("_").append(
				subscriber.getStatus().toUpperCase());
		String textForSuccessRequest = getSMSTextForID(task,
				confForSuccessRequest.toString(),
				defaultConfTextForSuccessRequest, subscriber.getLanguage());
		hashMap.put("SMS_TEXT", textForSuccessRequest);

		hashMap.put("COS_LIST", match);
		if (azaanCosIds.length > param(SMS, REQUEST_MAX_AZAAN_SMS, 5))
			hashMap.put(
					"MORE_TEXT",
					getSMSTextForID(task, AZAAN_REQUEST_MORE,
							m_reqMoreAzaanSMSDefault, subscriber.getLanguage()));
		else
			hashMap.put("MORE_TEXT", "");

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
	}
	
	public void searchRequest(Task task) {

		if (task.getString(param_requesttype) != null
				&& task.getString(param_requesttype).equalsIgnoreCase(
						type_content_validator)) {
			task.setObject(param_ocg_charge_id, "NOTVALID");
			return;
		}

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		String searchType = task.getString(SEARCH_TYPE);
		String searchString = task.getString(SEARCH_STRING);

		HashMap<String, String> searchMap = new HashMap<String, String>();
		searchMap.put(searchType, searchString);
		searchMap.put("SUBSCRIBER_ID",subscriberID);
		ArrayList<LuceneClip> clipsList = luceneIndexer.searchQuery(searchMap,
				0, param(SMS, LUCENE_MAX_RESULTS, 15));

		if (clipsList == null || clipsList.size() <= 0) {
			if (!param(SMS, REQUEST_NO_MATCH_DISP_TOP, false)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						REQUEST_RBT_SMS1_FAILURE, m_requestRbtFailure1Default,
						subscriber.getLanguage()));
				return;
			}

			Clip[] topClips = getClipsByCatId(5, subscriber.getLanguage());
			if (topClips != null && topClips.length > 0) {
				clipsList = new ArrayList<LuceneClip>();
				for (Clip clip : topClips) {
					LuceneClip luceneClip = new LuceneClip(clip, 0, 0, "", "");
					clipsList.add(luceneClip);
				}
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						REQUEST_RBT_SMS1_FAILURE, m_requestRbtFailure1Default,
						subscriber.getLanguage()));
				return;
			}
		}

		String nonLangClips = "";
		String langClips = "";
		String langFilterConfig = param(SMS, REQUEST_LANG_FILTER, "FALSE,hindi");
		StringTokenizer stk = new StringTokenizer(langFilterConfig, ",");
		boolean lanFilterOn = stk.nextToken().equalsIgnoreCase("true");
		String language = stk.nextToken();
		for (int i = 0; i < clipsList.size(); i++) {
			LuceneClip luceneClip = clipsList.get(i);
			String id = luceneClip.getClipId() + "";

			if (lanFilterOn && luceneClip.getLanguage() != null
					&& luceneClip.getLanguage().equalsIgnoreCase(language))
				langClips = langClips + id + ",";
			else
				nonLangClips = nonLangClips + id + ",";
		}
		if (!langClips.equalsIgnoreCase(""))
			langClips = langClips.substring(0, langClips.length() - 1);
		if (!nonLangClips.equalsIgnoreCase(""))
			nonLangClips = nonLangClips.substring(0, nonLangClips.length() - 1);
		if (langClips.length() > 0 && nonLangClips.length() > 0)
			langClips = langClips + "," + nonLangClips;
		else if (nonLangClips.length() > 0)
			langClips = nonLangClips;

		String match = "";
		StringTokenizer clipTokens = new StringTokenizer(langClips, ",");
		int iSong = 0;

		while (clipTokens.hasMoreTokens()) {
			if (iSong < param(SMS, REQUEST_MAX_SMS, 5)) {
				Clip clip = getClipById(clipTokens.nextToken(), subscriber
						.getLanguage());
				String song = clip.getClipName();
				if (param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)) {
					if (clip.getArtist() != null)
						song = song + " (" + clip.getArtist().trim() + ")";
				}

				if (param(SMS, ADD_MOVIE_REQUEST, false)
						&& clip.getAlbum() != null
						&& clip.getAlbum().length() > 0)
					song = song + "," + clip.getAlbum();

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if(param(SMS, INSERT_SEARCH_NUMBER_AT_BEGINNING, false)){
						match = match + (iSong + 1) + "-";
					}
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
						+ " ";
					else
						match = match + song + " ";

				} else {
					if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
						match = match + song + "-" + (iSong + 1) + " ";
					else
						match = match + (iSong + 1) + "-" + song + " ";
				}
			} else
				break;
			iSong++;
		}

		task.setObject(param_SMSTYPE, "REQUEST");
		removeViraldata(task);

		addViraldata(subscriberID, null, "REQUEST", langClips, "SMS", 1, null);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		// hashMap.put("SMS_TEXT",match +
		// getSMSTextForID(task,REQUEST_RBT_SMS1_SUCCESS,
		// m_requestRbtSuccess1Default));
		// hashMap.put("SONG_LIST","");
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS, if no
		 * configuration found then it will take the system default message.
		 */
		String defaultConfTextForSuccessRequest = getSMSTextForID(task,
				REQUEST_RBT_SMS1_SUCCESS, m_requestRbtSuccess1Default, language);
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS_<responses>,
		 * if that message is not configured then it will take the above default
		 * message.
		 */
		StringBuffer confForSuccessRequest = new StringBuffer(
				REQUEST_RBT_SMS1_SUCCESS);
		confForSuccessRequest.append("_").append(
				subscriber.getStatus().toUpperCase());
		String textForSuccessRequest = getSMSTextForID(task,
				confForSuccessRequest.toString(),
				defaultConfTextForSuccessRequest, language);
		hashMap.put("SMS_TEXT", textForSuccessRequest);

		hashMap.put("SONG_LIST", match);
		// String smsText = finalizeSmsText(hashMap, subscriber.getCircleID());

		// if (clipsList.size() > param(SMS,REQUEST_MAX_SMS,5))
		// smsText = smsText +
		// getSMSTextForID(task,REQUEST_MORE,m_reqMoreSMSDefault);

		if (clipsList.size() > param(SMS, REQUEST_MAX_SMS, 5))
			hashMap.put("MORE_TEXT", getSMSTextForID(task, REQUEST_MORE,
					m_reqMoreSMSDefault, subscriber.getLanguage()));
		else
			hashMap.put("MORE_TEXT", "");

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
	}

	@Override
	public void processDownloadsList(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		Downloads downloads = getDownloads(task);
		Download sd[] = downloads.getDownloads();

		if (sd == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOADS_NOT_PRESENT, m_downloadsNoSelDefault, subscriber
							.getLanguage()));
			return;
		}

		String sms = "";
		int songCount = 1;
		for (int i = 0; sd.length > 0 && i < sd.length; i++) {
			Clip clip = getClipById(sd[i].getToneID() + "", subscriber
					.getLanguage());
			if (clip != null
					&& sd[i].getEndTime().getTime() > System
							.currentTimeMillis())
				sms = sms
						+ ", "
						+ ((songCount++) + ". " + clip.getClipName() + "-" + (clip
								.getClipPromoId() == null ? "-" : clip
								.getClipPromoId()));
		}
		if (sms.length() > 2) {
			String smsString = getSMSTextForID(task, DOWNLOADS_LIST_SUCCESS, m_downloadsListSuccessDefault, subscriber
					.getLanguage());
			smsString = smsString.replaceAll("%RES%", sms.substring(2));
			task.setObject(param_responseSms, smsString);
		}
		else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOADS_NOT_PRESENT, m_downloadsNoSelDefault, subscriber
							.getLanguage()));
		}
	}

	@Override
	public void processManage(Task task) {
		task.setObject(param_mode, "CCC");
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MANAGE_INACTIVE_USER, getSMSTextForID(task, HELP_SMS_TEXT,
							m_helpDefault, subscriber.getLanguage()),
					subscriber.getLanguage()));
			return;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();

		if (param(SMS, SMS_MANAGE_ACT_DATE, true)) {
			Date actDate = subscriber.getStartDate();
			String actDateStr = new SimpleDateFormat("dd/MM/yyyy")
					.format(actDate);
			hashMap.put("ACT_DATE", actDateStr);
		}

		if (param(SMS, SMS_MANAGE_SEL_DISPLAY, true)) {
			StringBuilder smsBuilder = new StringBuilder();
			int songCount = 1;

			Setting[] settings = getSettings(task).getSettings();
			if (settings != null) {
				for (Setting setting : settings) {
					if (!setting.getSelectionStatus().equals(
							WebServiceConstants.ACTIVE))
						continue;

					String contentName = "-";
					String promoCode = "-";
					if (setting.getToneType().equals(
							WebServiceConstants.CATEGORY_SHUFFLE)) {
						Category category = rbtCacheManager.getCategory(setting
								.getCategoryID(), subscriber.getLanguage());
						if (category == null)
							continue;

						contentName = (category.getCategoryName() == null || category
								.getCategoryName().equalsIgnoreCase("null")) ? "NA"
								: category.getCategoryName();
						promoCode = (category.getCategoryPromoId() == null || category
								.getCategoryPromoId().equalsIgnoreCase("null")) ? "NA"
								: category.getCategoryPromoId();
					} else {
						Clip clip = rbtCacheManager.getClip(
								setting.getToneID(), subscriber.getLanguage());
						if (clip == null)
							continue;

						contentName = (clip.getClipName() == null || clip
								.getClipName().equalsIgnoreCase("null")) ? "NA"
								: clip.getClipName();
						promoCode = (clip.getClipPromoId() == null || clip
								.getClipPromoId().equalsIgnoreCase("null")) ? "NA"
								: clip.getClipPromoId();

					}

					String callerId = setting.getCallerID().equals(
							WebServiceConstants.ALL) ? param(SMS,
							SMS_TEXT_FOR_ALL, "all") : setting.getCallerID();

					if (param(SMS, SMS_MANAGE_PROMO_ID_DISPLAY, false)) {
						smsBuilder.append(", ").append(songCount++)
								.append(". ").append(contentName).append("-")
								.append(promoCode);
					} else {
						smsBuilder.append(", ").append(callerId).append("-")
								.append(contentName);
					}
				}
			}
			if (smsBuilder.length() > 2)
				hashMap.put("SELECTIONS", smsBuilder.substring(2));
			else {
				if (getSMSTextForID(task, MANAGE_NO_SELECTION, null, subscriber
						.getLanguage()) != null) {
					task.setObject(param_responseSms,
							getSMSTextForID(task, MANAGE_NO_SELECTION, null,
									subscriber.getLanguage()));
					return;
				}
				hashMap.put("SELECTIONS", "0");

			}
		}

		if (param(SMS, SMS_MANAGE_DOWNLOADS_DISPLAY, true)) {
			StringBuilder smsBuilder = new StringBuilder();
			int songCount = 1;

			Download[] downloads = null;
			Downloads downloadsObj = getDownloads(task);
			if (downloadsObj != null)
				downloads = downloadsObj.getDownloads();
			if (downloads != null) {
				for (Download download : downloads) {
					if (download.getEndTime().getTime() < System
							.currentTimeMillis())
						continue;

					String contentName = "-";
					String promoCode = "-";
					if (download.getToneType().equals(
							WebServiceConstants.CATEGORY_SHUFFLE)) {
						Category category = rbtCacheManager.getCategory(
								download.getCategoryID(), subscriber
										.getLanguage());
						if (category == null)
							continue;

						contentName = (category.getCategoryName() == null || category
								.getCategoryName().equalsIgnoreCase("null")) ? "NA"
								: category.getCategoryName();
						promoCode = (category.getCategoryPromoId() == null || category
								.getCategoryPromoId().equalsIgnoreCase("null")) ? "NA"
								: category.getCategoryPromoId();
					} else {
						Clip clip = rbtCacheManager.getClip(download
								.getToneID(), subscriber.getLanguage());
						if (clip == null)
							continue;

						contentName = (clip.getClipName() == null || clip
								.getClipName().equalsIgnoreCase("null")) ? "NA"
								: clip.getClipName();
						promoCode = (clip.getClipPromoId() == null || clip
								.getClipPromoId().equalsIgnoreCase("null")) ? "NA"
								: clip.getClipPromoId();

					}

					smsBuilder.append(", ").append(songCount++).append(". ")
							.append(contentName).append("-").append(promoCode);
				}
			}

			if (smsBuilder.length() > 2)
				hashMap.put("DOWNLOADS", smsBuilder.substring(2));
			else {
				if (getSMSTextForID(task, MANAGE_NO_SELECTION, null, subscriber
						.getLanguage()) != null) {
					task.setObject(param_responseSms,
							getSMSTextForID(task, MANAGE_NO_SELECTION, null,
									subscriber.getLanguage()));
					return;
				}
				hashMap.put("DOWNLOADS", "0");
			}

		}

		hashMap.put("SMS_TEXT", getSMSTextForID(task, MANAGE_SUCCESS,
				m_manageSuccessDefault, subscriber.getLanguage()));

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger
				.info("SmsProcessor-removeCallerIDSel RBT::managed selection successfull  "
						+ " of the subscriber " + subscriberID);
	}
// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	private void processAzaanPackRequest(Task task, ViralData viralData,
			int cosIdNo, String language) {
		logger.info("Processing processAzaanPackRequest for subID :");
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		logger.info("Processing processAzaanPackRequest for subID:"
				+ subscriber.getSubscriberID() + " ,isPrepaid:"
				+ task.getString(param_isPrepaid));
		boolean invalid = false;
		String clipIDs = viralData.getClipID();
		StringTokenizer stk = new StringTokenizer(clipIDs, ",");
		for (int i = 1; i < cosIdNo; i++) {
			if (stk.hasMoreTokens())
				stk.nextToken();
			else {
				invalid = true;
				break;
			}
		}

		if (cosIdNo < 1 || invalid || !stk.hasMoreTokens()) {
			logger.info("Azaan search invalid clause");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, AZAAN_SEARCH_FAILURE1,
							m_requestAzaanSearchFailure1Default,
							subscriber.getLanguage()));
			return;
		}
		String token = stk.nextToken();
		CosDetails cosDetails = (CosDetails) CacheManagerUtil
				.getCosDetailsCacheManager().getActiveCosDetail(token,
						subscriber.getCircleID());
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("AZAAN_PACK",
				(null != cosDetails ? cosDetails.getActivationPrompt() : token));
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		int packCosId = Integer.parseInt(cosDetails.getCosId());
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
		subscriptionRequest.setMode("SMS");
		subscriptionRequest.setInfo(task.getString(param_actInfo));
		subscriptionRequest.setCircleID(subscriber.getCircleID());
		subscriptionRequest.setPackCosId(packCosId);
		subscriber = rbtClient.activateSubscriber(subscriptionRequest);
		response = subscriptionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
			String defaultConfTextForSetRequestSucess = getSMSTextForID(task,
					AZAAN_ACTIVATION_SUCCESS, m_requestAzaanRbtSuccessDefault,
					language);
			StringBuffer confForSetRequestSuccess = new StringBuffer(
					AZAAN_ACTIVATION_SUCCESS);
			String textForSetRequestSuccess = getSMSTextForID(task,
					confForSetRequestSuccess.toString(),
					defaultConfTextForSetRequestSucess,
					language);
			hashMap.put("SMS_TEXT", textForSetRequestSuccess);
		} else {
			String smsText = getSMSTextForID(task, AZAAN_TECHNICAL_FAILURE,
					m_technicalFailuresDefault, language);
			smsText = smsText + ".Reason is: " + response;
			hashMap.put("SMS_TEXT", smsText);
		}
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		removeViraldata(task);
	}
	
	private void setRequest(Task task, int songNo, String type) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		task.setObject(param_SMSTYPE, type);
		ViralData context[] = getViraldata(task);
		logger
				.info("setRequest context length is " + context == null ? "null zero"
						: context.length);
		String language = subscriber.getLanguage();
		if (context == null || context.length <= 0 || context[0] == null
				|| context[0].getClipID() == null) {
			if(param(SMS,PROFILE_SET_ALLOWED_BY_INDEX, false)){
	                task.setObject(param_isDefaultProfileHrsByIndex, true);
					processActNSel(task);
					return;
			}
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_MORE_NO_SEARCH, m_reqMoreSMSNoSearchDefault,
					language));
			return;
		}// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		if (context != null && context.length > 0 && context[0] != null
				&& context[0].getType().equalsIgnoreCase("AZAAN_REQUEST")) {
			processAzaanPackRequest(task, context[0], songNo, language);
			return;
		}

		Category category = getCategory(11, language);
		if (category == null) {
			logger.info("category 11 not found");
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}

		boolean invalid = false;
		String clipIDs = context[0].getClipID();
		StringTokenizer stk = new StringTokenizer(clipIDs, ",");
		if (type.equalsIgnoreCase("CATEGORY") && stk.hasMoreTokens())
			stk.nextToken();
		for (int i = 1; i < songNo; i++) {
			if (stk.hasMoreTokens())
				stk.nextToken();
			else {
				invalid = true;
				break;
			}
		}

		if (songNo < 1 || invalid || !stk.hasMoreTokens()) {
			logger.info("category search invalid clause");
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_RBT_SMS2_FAILURE, m_requestRbtFailure2Default,
					language));
			return;
		}

		String token = stk.nextToken();
		Clip reqClip = getClipById(token, language);
		if (reqClip == null) {
			logger.info("category search clip found to be null error");
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}

		task.setObject(CLIP_OBJ, reqClip);

		task.setObject(CAT_OBJ, category);
		task.setObject(param_catid, category.getCategoryId() + "");
		task.setObject(param_clipid, reqClip.getClipId() + "");
		if (param(COMMON, ALLOW_LOOPING, false)
				&& param(COMMON, ADD_SEL_TO_LOOP, false))
			task.setObject(param_inLoop, "YES");

		logger.info("SMS_REQUEST_CONFIRMATION_ON is"
				+ getParameter(SMS, SMS_REQUEST_CONFIRMATION_ON));

		if (getParameter(SMS, SMS_REQUEST_CONFIRMATION_ON) != null
				&& getParameter(SMS, SMS_REQUEST_CONFIRMATION_ON)
						.equalsIgnoreCase("true")) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("CALLER_ID",
					task.getString(param_callerid) == null ? param(SMS,
							SMS_TEXT_FOR_ALL, "all") : task
							.getString(param_callerid));
			String smsText = getSMSTextForID(task,
					REQUEST_OPT_IN_CONFIRMATION_ACT_SMS, null, language);
			if (isUserActive(subscriber.getStatus()))
				smsText = getSMSTextForID(task,
						REQUEST_OPT_IN_CONFIRMATION_SEL_SMS, null, language);
			else {
				Setting[] setting = getActiveSubSettings(subscriberID, 1);
				if (isOverlap(setting, null, reqClip.getClipId() + "", language)) {
					super.removeViraldata(subscriber.getSubscriberID(), null,
							"REQUEST");
					task.setObject(param_responseSms, getSMSTextForID(task,
							REQUEST_OVERLAP_FAILURE, m_copyFailureSMSDefault,
							language));
					return;
				}

			}
			logger.info(reqClip.getClipName());
			hashMap.put("SONG_NAME", reqClip.getClipName());
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			String sms = finalizeSmsText(hashMap);
			RBTDBManager.getInstance().updateViralPromotion1(
					subscriber.getSubscriberID(), context[0].getCallerID(),
					context[0].getSentTime(), "REQUEST", "REQUESTCONFPENDING",
					context[0].getSetTime(), context[0].getSelectedBy(), null,
					reqClip.getClipId() + "");

			task.setObject(param_responseSms, sms);
			return;
		}
		String response = processSetSelection(task);

		if (response.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_SUSPENDED_TEXT,
			// m_SuspendedSelDefault,subscriber.getLanguage()));
			String smsText = getSMSTextForID(task, SELECTION_SUSPENDED_TEXT,
					m_SuspendedSelDefault, language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response.equals(WebServiceConstants.OFFER_NOT_FOUND)) {
			String smsText = getSMSTextForID(task, OFFER_NOT_FOUND_TEXT,
					m_OfferAlreadyUsed, language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_ALREADY_EXISTS_TEXT,
			// getSMSTextForID(task,PROMO_ID_SUCCESS,
			// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			String smsText = getSMSTextForID(task,
					SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							language), language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response.equals(WebServiceConstants.ALREADY_ACTIVE)) {
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
			// getSMSTextForID(task,PROMO_ID_SUCCESS,
			// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			String smsText = getSMSTextForID(task,
					SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT, getSMSTextForID(
							task, PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							language), language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_ADRBT_NOTALLOWED_,
			// m_ADRBTSelectionFailureDefault,subscriber.getLanguage()));
			String smsText = getSMSTextForID(task, SELECTION_ADRBT_NOTALLOWED_,
					m_ADRBTSelectionFailureDefault, language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response.equals(WebServiceConstants.SELECTION_OVERLIMIT)) {
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_OVERLIMIT,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			String smsText = getSMSTextForID(task, SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, language), language);
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		} else if (response
				.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					PERSONAL_SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, language), language));
		} else if (response
				.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					LOOP_SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, language), language));
		} else if (response
				.equals(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
			String smsText = getSMSTextForID(task,
					REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, language), language);
			 smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							LITEUSER_PREMIUM_BLOCKED, liteUserPremiumBlocked,
							language));
		}else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							LITEUSER_PREMIUM_NOT_PROCESSED, liteUserPremiumNotProcessed,
							language));
		}else if (response
				.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							Corporate_Selection_Not_Allowed, m_corpChangeSelectionFailureDefault,
							language));
		} else if (!response.equals("success")) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}

		String sms = reqClip.getClipName();
		if (reqClip.getClipEndTime() != null)
			reqClip.getClipEndTime().toString();

		if (type.equalsIgnoreCase("REQUEST")
				&& param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)
				&& reqClip.getArtist() != null
				&& reqClip.getArtist().length() > 0)
			sms = sms + " (" + reqClip.getArtist().trim() + ")";

		if (param(SMS, ADD_MOVIE_REQUEST, false) && reqClip.getAlbum() != null
				&& reqClip.getAlbum().length() > 0
				&& type.equalsIgnoreCase("REQUEST"))
			sms = sms + "," + reqClip.getAlbum();

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SONG_NAME", sms);

		if (type.equalsIgnoreCase("REQUEST")) {
			/*
			 * Get the configured text for the REQUEST_RBT_SMS2_SUCCESS, if no
			 * configuration found then it will take the system default message.
			 */
			String defaultConfTextForSetRequestSucess = getSMSTextForID(task,
					REQUEST_RBT_SMS2_SUCCESS, m_requestRbtSuccess2Default,
					language);
			/*
			 * Get the configured text for the
			 * REQUEST_RBT_SMS2_SUCCESS_<responses>, if that message is not
			 * configured then it will take the above default message.
			 */
			StringBuffer confForSetRequestSuccess = new StringBuffer(
					REQUEST_RBT_SMS2_SUCCESS);
			confForSetRequestSuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			String textForSetRequestSuccess = getSMSTextForID(task,
					confForSetRequestSuccess.toString(),
					defaultConfTextForSetRequestSucess, language);

			hashMap.put("SMS_TEXT", textForSetRequestSuccess);
		} else {
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					CATEGORY_SEARCH_SET_SUCCESS, m_catRbtSuccess2Default,
					language));
		}

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		removeViraldata(task);
	}

	@Override
	public void processremoveCallerIDSel(Task task) {
		logger
				.info(" SmsProcessor : processremoveCallerIDSel : parameters are -- "
						+ task);

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		String callerID = task.getString(param_callerid);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		Clip clip = null;
		Category category = null;

		if (smsList != null && smsList.size() > 0) {
			String token = smsList.get(0);
			clip = getClipByPromoId(token, subscriber.getLanguage());
			if (clip == null && param(SMS, CHECK_CLIP_SMS_ALIAS, false))
				clip = getClipByAlias(token, subscriber.getLanguage());
			if (clip == null) {
				clip = rbtCacheManager.getClipFromPromoMaster(token);
				if (clip == null) {
					category = getCategoryByPromoId(token, subscriber
							.getLanguage());
					if (category == null
							&& isCheckCategorySmsAlias)
						category = getCategoryBySMSAlias(token, subscriber
								.getLanguage());

					if (category != null
							&& !com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isShuffleCategory(category
											.getCategoryTpe())) {
						// If category is not shuffle, then not allowing
						// deleting selection by category.
						category = null;
					}
				}
			}
		}

		if (clip == null && category == null && callerID == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					SEL_DEACT_INVALID_PROMO_ID,
					m_selDeactInvalidpromoIdDefault, subscriber.getLanguage()));
			return;
		}

		if (callerID == null
				&& !param(SMS, ALLOW_REMOVAL_OF_NULL_CALLERID_SEL, false)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RMV_CALLERID_FAILURE, m_rmvCallerIDFailureDefault,
					subscriber.getLanguage()));
			return;
		}
		if (callerID == null)
			callerID = "ALL";
		task.setObject(param_callerid, callerID);

		String contentName = "";
		task.remove(param_catid);
		if (clip != null) {
			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			contentName = clip.getClipName();
		} else if (category != null) {
			task.setObject(param_catid, String
					.valueOf(category.getCategoryId()));
			contentName = category.getCategoryName();
		}

		String response = processDeactivateSelection(task);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (callerID.equalsIgnoreCase("ALL"))
			callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SONG_NAME", contentName);

		if (response
				.equalsIgnoreCase(WebServiceConstants.DELAYED_DEACT_SUCCESS)) {
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					RMV_CALLERID_DELAYED_DEACT_SUCCESS,
					m_rmvCallerIDDelayedDeactSuccessDefault, subscriber
							.getLanguage()));
		} else if (!response.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
			hashMap.put("SMS_TEXT", getSMSTextForID(task, RMV_CALLERID_FAILURE,
					m_rmvCallerIDFailureDefault, subscriber.getLanguage()));
		} else {
			hashMap.put("SMS_TEXT", getSMSTextForID(task, RMV_CALLERID_SUCCESS,
					m_rmvCallerIDSuccessDefault, subscriber.getLanguage()));
		}

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger.info("removeCallerIDSel RBT::removed theselection for callerID-"
				+ callerID + " of the subscriber " + subscriberID);
	}

	@Override
	public void processCOPY(Task task) {
		logger.info("SmsProcessor : processCOPY : parameters are : " + task);

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		String callerId = task.getString(param_callerid);
		String language = subscriber.getLanguage();
		String copyFailureSmsText = getSMSTextForID(task,
				COPY_FAILURE, m_copyFailureSMSDefault, language);
		HashMap<String, String> hashmap = new HashMap<String, String>();
		if (callerId == null) {
			logger.warn("Unable to process SMS copy, callerId is null");
			task.setObject(param_subscriber, subscriber);
			hashmap.put("SMS_TEXT", copyFailureSmsText);
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
			return;
		}
		hashmap.put("CALLER_ID", callerId);

		if (subscriber.getSubscriberID().equals(callerId)) {
			logger
					.warn("Unable to process SMS copy song, caller and subscriber are same");
			hashmap.put("SMS_TEXT", copyFailureSmsText);
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
			return;
		}

		CopyRequest copyRequest = new CopyRequest(subscriberID, callerId);
		CopyDetails copyDetails = RBTClient.getInstance().getCopyData(
				copyRequest);
		CopyData copydata[] = copyDetails.getCopyData();

		String wavFile = null;
		String songName = null;
		Clip clip = null;
		if (copyRequest.getResponse().equalsIgnoreCase(
				WebServiceConstants.SUCCESS)
				&& copydata != null) {
			// If User has multiple selections, not allowing the copy
			if (!copyDetails.isUserHasMultipleSelections()) {
				int clipID = copydata[0].getToneID();
				clip = rbtCacheManager.getClip(clipID);
				if (clip != null) {
					wavFile = clip.getClipRbtWavFile();
					songName = clip.getClipName();
				}
			}
		} else if (copyRequest.getResponse().equalsIgnoreCase(
				WebServiceConstants.NOT_RBT_USER)) {
			logger.warn("SmsProcessor : processCOPY : subscriber: "
					+ subscriberID + " is not rbt user ");
			String copyFailureInactiveSmsText = getSMSTextForID(task,
					COPY_FAILURE_INACTIVE, copyFailureSmsText, language);
			hashmap.put("SMS_TEXT", copyFailureInactiveSmsText);
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
			return;
		}else if(copyRequest.getResponse().equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			String smsText = getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, subscriber.getLanguage());
			task.setObject(param_responseSms, smsText);
			return;
		}else if(copyRequest.getResponse().equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			String smsText = getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, subscriber.getLanguage());
			task.setObject(param_responseSms, smsText);
			return;
		}

		logger.info("SmsProcessor : processCOPY : wavFile >" + wavFile);
		if (wavFile != null) {
			Setting[] setting = getActiveSubSettings(subscriberID, 1);
			if (clip != null
					&& isOverlap(setting, null, clip.getClipId() + "", language)) {
				String copyFailureOverlapSmsText = getSMSTextForID(task, COPY_OVERLAP_FAILURE,
								m_copyFailureSMSDefault, language);
				hashmap.put("SMS_TEXT", copyFailureOverlapSmsText);
				task.setObject(param_responseSms, finalizeSmsText(hashmap));
				return;
			}
			String mode = "SMS";
			if (param(SMS, PROCESS_SMSUI_COPY_AS_OPTIN, true))
				mode = null;
			/*addViraldata(callerId, subscriber.getSubscriberID(), "COPY",
					wavFile, mode, 0, null);*/
			task.setObject(param_clipid, clip.getClipId() + "");
			task.setObject(param_catid, "3");
			task.setObject(param_actInfo, "CP:" + callerId);
			task.remove(param_callerid);
			
			
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false)) {
				task.setObject(param_inLoop, "YES");
			}
			
			processSetSelection(task);

			hashmap.put("SONG_NAME", songName);
			/*
			 * Get the configured text for the COPY_SUCCESS, if no configuration
			 * found then it will take the system default message.
			 */
			String defaultConfTextForCopySuccess = getSMSTextForID(task,
					COPY_SUCCESS, m_copySuccessSMSDefault, language);
			/*
			 * Get the configured text for the COPY_SUCCESS_<responses>, if that
			 * message is not configured then it will take the above default
			 * message.
			 */
			StringBuffer confForCopySuccess = new StringBuffer(COPY_SUCCESS);
			confForCopySuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			String textForCopyResponses = getSMSTextForID(task,
					confForCopySuccess.toString(),
					defaultConfTextForCopySuccess, language);
			hashmap.put("SMS_TEXT", textForCopyResponses);
			hashmap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
		} else {
			hashmap.put("SMS_TEXT", copyFailureSmsText);
			task.setObject(param_responseSms, finalizeSmsText(hashmap));
			return;
		}
	}

	@Override
	public void getGift(Task task) {
		String songName = null;
		String giftDefClipID = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		String language = subscriber.getLanguage();
		if (task.getObject(CAT_OBJ) != null) {
			task.setObject(param_catid, ((Category) task.getObject(CAT_OBJ))
					.getCategoryId()
					+ "");
			songName = ((Category) task.getObject(CAT_OBJ)).getCategoryName();
		} else if (task.getObject(CLIP_OBJ) != null) {
			task.setObject(param_clipid, ((Clip) task.getObject(CLIP_OBJ))
					.getClipId()
					+ "");
			songName = ((Clip) task.getObject(CLIP_OBJ)).getClipName();
		} else if ((giftDefClipID = RBTParametersUtils.getParamAsString(
				"COMMON", "DEFAULT_CLIP_ID_FOR_GIFT", null)) != null) {
			Clip giftClip = rbtCacheManager.getClip(giftDefClipID);
			if (giftClip != null) {
				task.setObject(param_clipid, String.valueOf(giftClip
						.getClipId()));
				songName = giftClip.getClipName();
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						GIFT_CODE_FAILURE, m_giftCodeFailureDefault, language));
				return;
			}
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, language));
			return;
		}
		String response = processGift(task);
		logger.info("SMSProcessor Gift Response === "+response);
		if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SUCCESS)) {
			/*
			 * Get the configured text for the GIFT_SUCCESS, if no configuration
			 * found then it will take the system default message.
			 */
			String defaultConfTextForGiftSuccess = getSMSTextForID(task,
					GIFT_SUCCESS, m_giftSuccessDefault, language);
			/*
			 * Get the configured text for the GIFT_SUCCESS_<responses>, if that
			 * message is not configured then it will take the above default
			 * message.
			 */
			StringBuffer confForGiftSuccess = new StringBuffer(GIFT_SUCCESS);
			confForGiftSuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			String textForGiftSuccess = getSMSTextForID(task,
					confForGiftSuccess.toString(),
					defaultConfTextForGiftSuccess, language);

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", textForGiftSuccess);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID",
					task.getString(param_callerid) == null ? "" : task
							.getString(param_callerid));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.INVALID)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.OWN_NUMBER)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NOT_ALLOWED)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.CLIP_EXPIRED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, language));
			return;
		}else if(response.equalsIgnoreCase(WebServiceConstants.EXISTS_IN_GIFTEE_LIBRAY)){
			String confResponse = getSMSTextForID(task,GIFT_ALREADY_EXISTS, m_giftAlreadyExistsDefault, language);
			confResponse = confResponse.replaceAll("%songName%", songName);
			task.setObject(param_responseSms,confResponse);

		}else if(response.equalsIgnoreCase(WebServiceConstants.LIMIT_EXCEEDED)){
			task.setObject(param_responseSms, getSMSTextForID(task,
					MAX_DOWNLOAD_LIMIT_EXCEEDED, m_maxDownloadLimitExceeded, language));

		}else
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, language));
	}
// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public void getMoreAzaan(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		logger.info("getMoreAzaan task is " + task);
		String type = "AZAAN_REQUEST";
		task.setObject(param_SMSTYPE, type);
		ViralData context[] = getViraldata(task);
		logger.info("getMoreAzaan context is " + context == null ? "zero null"
				: context.length);
		if (context == null || context.length <= 0 || context[0] == null
				|| context[0].getClipID() == null) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, AZAAN_REQUEST_MORE_NO_SEARCH,
							m_reqAzaanMoreSMSNoSearchDefault,
							subscriber.getLanguage()));
			return;
		}
		String cosIds = context[0].getClipID();
		int searchCount = context[0].getCount();
		int perSMSCount = 0;
		StringTokenizer stk = new StringTokenizer(cosIds, ",");
		perSMSCount = param(SMS, REQUEST_MAX_AZAAN_SMS, 5);
		if (stk.countTokens() <= perSMSCount * searchCount) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, AZAAN_REQUEST_MORE_EXHAUSTED,
							m_reqAzaanMoreSMSExhaustedDefault,
							subscriber.getLanguage()));
			return;
		}
		for (int a = 0; a < perSMSCount * searchCount; a++)
			stk.nextToken();
		String match = "";
		int iCos = perSMSCount * searchCount;
		for (int i = 1; i <= perSMSCount && stk.hasMoreTokens(); i++) {
			String activationPrompt = "";
			String cosID = stk.nextToken();
			CosDetails cosDetails = (CosDetails) CacheManagerUtil
					.getCosDetailsCacheManager().getCosDetail(cosID);
			activationPrompt = cosDetails.getActivationPrompt();
			if (param(SMS, INSERT_SEARCH_NUMBER_AT_BEGINNING, false)) {
				match = match + (iCos + 1) + "-" + activationPrompt + " ";
			} else if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
				match = match + activationPrompt + "-" + (iCos + 1) + " ";
			else
				match = match + (iCos + 1) + "-" + activationPrompt + " ";
			iCos++;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		/*
		 * Get the configured text for the AZAAN_SEARCH_SUCCESS_<responses>, if
		 * that message is not configured then it will take the above default
		 * message.
		 */
		String defaultConfTextForSuccessRequest = getSMSTextForID(task,
				AZAAN_SEARCH_SUCCESS, m_requestAzaanSuccessDefault,
				subscriber.getLanguage());
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS_<responses>,
		 * if that message is not configured then it will take the above default
		 * message.
		 */
		StringBuffer confForSuccessRequest = new StringBuffer(
				AZAAN_SEARCH_SUCCESS);
		String textForSuccessRequest = getSMSTextForID(task,
				confForSuccessRequest.toString(),
				defaultConfTextForSuccessRequest, subscriber.getLanguage());
		hashMap.put("SMS_TEXT", textForSuccessRequest);

		hashMap.put("COS_LIST", match);
		if (stk.hasMoreTokens())
			hashMap.put(
					"MORE_TEXT",
					getSMSTextForID(task, AZAAN_REQUEST_MORE,
							m_reqMoreAzaanSMSDefault, subscriber.getLanguage()));
		else
			hashMap.put("MORE_TEXT", "");

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
		task.setObject(param_SEARCHCOUNT, ++searchCount + "");
		updateViraldata(task);
	}
	
	@Override
	public void getMoreClips(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		logger.info("getMoreClips task is " + task);
		String type = "REQUEST";
		boolean isCatSearch = isThisFeature(task, tokenizeArrayList(param(SMS,
				CATEGORY_SEARCH_KEYWORD, null), null), CATEGORY_SEARCH_KEYWORD);
		if (isCatSearch)
			type = "CATEGORY";
		task.setObject(param_SMSTYPE, type);

		ViralData context[] = getViraldata(task);
		logger.info("getMoreClips context is " + context == null ? "zero null"
				: context.length);
		if (context == null || context.length <= 0 || context[0] == null
				|| context[0].getClipID() == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_MORE_NO_SEARCH, m_reqMoreSMSNoSearchDefault,
					subscriber.getLanguage()));
			return;
		}

		String clipIDs = context[0].getClipID();
		int searchCount = context[0].getCount();
		String catName = null;
		Category cat = null;
		int perSMSCount = 0;

		StringTokenizer stk = new StringTokenizer(clipIDs, ",");

		if (type.equalsIgnoreCase("CATEGORY")) {
			cat = getCategory(stk.nextToken(), subscriber.getLanguage());
			catName = cat.getCategoryName();
			perSMSCount = param(SMS, REQUEST_MAX_CAT_SMS, 10);
		} else {
			perSMSCount = param(SMS, REQUEST_MAX_SMS, 10);
		}

		if (stk.countTokens() <= perSMSCount * searchCount) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_MORE_EXHAUSTED, m_reqMoreSMSExhaustedDefault,
					subscriber.getLanguage()));
			return;
		}

		for (int a = 0; a < perSMSCount * searchCount; a++)
			stk.nextToken();

		String match = "";
		int iSong = perSMSCount * searchCount;
		for (int i = 1; i <= perSMSCount && stk.hasMoreTokens(); i++) {
			String song = "";
			String id = stk.nextToken();
			Clip clip = getClipById(id, subscriber.getLanguage());
			song = clip.getClipName();
			if (type.equalsIgnoreCase("CATEGORY")) {

				/*
				 * Append the artist name with song name.
				 */
				if (param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)) {
					if (clip.getArtist() != null)
						song = song + " (" + clip.getArtist().trim() + ")";
				}

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
				} else if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
					match = match + song + "-" + (iSong + 1) + " ";
				else
					match = match + (iSong + 1) + "-" + song + " ";
			} else {
				if (param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)) {
					if (clip.getArtist() != null)
						song = song + " (" + clip.getArtist().trim() + ")";
				}

				if (param(SMS, ADD_MOVIE_REQUEST, false)
						&& clip.getAlbum() != null
						&& clip.getAlbum().length() > 0)
					song = song + "," + clip.getAlbum();

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
					else
						match = match + song + " ";
				} else {
					if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
						match = match + song + "-" + (iSong + 1) + " ";
					else
						match = match + (iSong + 1) + "-" + song + " ";
				}
			}
			iSong++;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, REQUEST_RBT_SMS1_SUCCESS,
				m_requestRbtSuccess1Default, subscriber.getLanguage()));
		if (isCatSearch) {
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					CATEGORY_SEARCH_SUCCESS, m_catRbtSuccess1Default,
					subscriber.getLanguage()));
			hashMap.put("CAT_NAME", catName);
			hashMap.put("COUNT", perSMSCount + "");
		}
		hashMap.put("SONG_LIST", match);
		// String smsText = finalizeSmsText(hashMap);
		// if (stk.hasMoreTokens())
		// {
		// if(isCatSearch)
		// smsText +=
		// getSMSTextForID(task,REQUEST_MORE_CAT,m_reqMoreSMSCatDefault);
		// else
		// smsText += getSMSTextForID(task,REQUEST_MORE, m_reqMoreSMSDefault);
		// }
		if (stk.hasMoreTokens()) {
			if (isCatSearch)
				hashMap.put("MORE_TEXT", getSMSTextForID(task,
						REQUEST_MORE_CAT_SUCCESS, m_reqMoreSMSCatDefault,
						subscriber.getLanguage()));
			else
				hashMap.put("MORE_TEXT", getSMSTextForID(task, REQUEST_MORE,
						m_reqMoreSMSDefault, subscriber.getLanguage()));
		} else {
			hashMap.put("MORE_TEXT", "");
		}
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
		task.setObject(param_SEARCHCOUNT, ++searchCount + "");
		updateViraldata(task);

	}

	protected void processSetTempOverride(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		logger.info("Processing request,  smsList: " + smsList);
		
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		boolean isActivationRequest = ((String) task
				.getObject(IS_ACTIVATION_REQUEST)).equalsIgnoreCase("true");

		if (!isActivationRequest && !isUserActive(subscriber.getStatus())
				&& !param(SMS, IS_ACT_OPTIONAL, false)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}
		// RBT-7912 : Default Amount change for new Profile user
          if(cosIdCirleMap.size()>0 && (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER) || 
    	     subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE))) {
        	  String cosId = cosIdCirleMap.get(subscriber.getCircleID());
        	  logger.info("cosId:="+cosId);
        	  if(cosId != null) {
        	    task.setObject(param_COSID, cosId);
        	  }
        	}			
          
		// Getting the profile clip
		Clip profileClip = getProfileClip(task);
		
		if (profileClip == null) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, TEMPORARY_OVERRIDE_FAILURE,
							m_temporaryOverrideFailureDefault, subscriber
									.getLanguage()));
			return;
		} else{
			if (param(SMS, "BLOCK_PROFILE_SELECTION_FOR_INACTIVE_USER", false)
					&& (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER)
					|| subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE))) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						PROFILE_SEL_NOT_ALLOWED_FOR_NEW_USER, m_profileSelNotAllowedForNewUser,
						subscriber.getLanguage()));
				return;
			}
			task.setObject(param_clipid, profileClip.getClipId() + "");
		}
		boolean isDurationDays = isDurationDays(task);
		long duration = checkDuration(task, isDurationDays);
		if (duration <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TEMPORARY_OVERRIDE_TIME_FAILURE,
					m_temporaryOverrideTimeDefault, subscriber.getLanguage()));
			return;
		}
		String timeStr = getTimeAsString(duration);
		task.setObject(param_profile_hours, "M" + duration);

		String clipName = profileClip.getClipName();

		boolean OptIn = false;
		if (subscriber.getActivationInfo() != null
				&& subscriber.getActivationInfo().indexOf(":optin:") != -1)
			OptIn = true;
		task.setObject(param_optin, OptIn ? "YES" : "NO");
		task.setObject(param_catid, "99");
		
		if(task.containsKey(param_ouiRegCode)) {
			task.setObject(param_actInfo, "ACT_REG:" + task.getString(param_ouiRegCode));
		}
				
		// task.setObject(param_enddate,endDate);
		String response = processSetSelection(task);
		
		logger.info("Set the selection, response: " + response);
		
		if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					SELECTION_ADRBT_NOTALLOWED_,
					m_ADRBTSelectionFailureDefault, subscriber.getLanguage()));
			return;
		} else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					LITEUSER_PREMIUM_BLOCKED, liteUserPremiumBlocked,
					subscriber.getLanguage()));
		}else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					LITEUSER_PREMIUM_NOT_PROCESSED, liteUserPremiumNotProcessed,
					subscriber.getLanguage()));
		} else if (response
				.contains(WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED)) {
			String cosContent = response.substring(response.indexOf(WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED)+ WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED.length());
			String smsText = getSMSTextForID(task,"COS_MISMATCH_CONTENT_BLOCKED_" + cosContent.toUpperCase(),getSMSTextForID(task, LITEUSER_PREMIUM_BLOCKED,
							liteUserPremiumBlocked, subscriber.getLanguage()),subscriber.getLanguage());
			task.setObject(param_responseSms, smsText);
		}else if (response
				.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					Corporate_Selection_Not_Allowed, m_corpChangeSelectionFailureDefault,
					subscriber.getLanguage()));
			return;
		}else if (!response.equals("success")) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber
							.getLanguage()));
			return;
		}
		// To send profile hours.
		 
		String profileHrs = (null != smsList && smsList.size() >= 2) ? smsList
				.get(1) : null;
		
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SONG_NAME", clipName);
		hashMap.put("DURATION", timeStr);
		hashMap.put("PROFILE_HOURS", profileHrs);		
		String smsText = null;
		
		if(task.containsKey(param_isSelConsentInserted)){
			smsText = getSMSTextForID(task, CONSENT_PROFILE_SELECTION_SUCCESS, m_consentSelectionSuccessDefault, subscriber.getLanguage());
		}
		if(subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE) && smsText==null) {
			//SmsText for active user
			smsText = getSMSTextForID(task, TEMPORARY_OVERRIDE_ACTIVE_SUCCESS, null, subscriber.getLanguage());
		}
		if(smsText == null) {
			smsText = getSMSTextForID(task, TEMPORARY_OVERRIDE_SUCCESS, m_temporaryOverrideSuccessDefault, subscriber.getLanguage());
		}
		hashMap.put("SMS_TEXT", smsText);
		
		logger.debug(" profile hrs: "+hashMap.get("PROFILE_HOURS"));
		
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		 
		if (param(SMS, PROFILE_SET_ALLOWED_BY_INDEX, false)) {
			task.setObject(param_SMSTYPE, "PROFILE");
			removeViraldata(task);
		}
	}

	private String getTimeAsString(long duration) {
		long days = duration / 60 / 24;
		long totalHours = duration / 60;
		long residualHours = totalHours - days * 24;
		long residualMinutes = duration - totalHours * 60;

		StringBuilder strBuilder = new StringBuilder(" ");
		if (days > 1)
			strBuilder.append(days + " days ");
		else if (days > 0)
			strBuilder.append(days + " day ");

		if (residualHours > 1)
			strBuilder.append(residualHours + " hours ");
		else if (residualHours > 0)
			strBuilder.append(residualHours + " hour ");

		if (residualMinutes > 1)
			strBuilder.append(residualMinutes + " mins ");
		else if (residualMinutes > 0)
			strBuilder.append(residualMinutes + " mins ");

		return strBuilder.toString();
	}

	@Override
	public void processListProfile(Task task) {
		logger.info("inside processlistProfile");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		if (param(SMS, TEMPORARY_OVERRIDE_LIST_STATIC, null) != null) {
			task.setObject(param_responseSms, param(SMS,
					TEMPORARY_OVERRIDE_LIST_STATIC, null));
			return;
		}

		Clip[] profileClips = getActiveClipsByCatId(99, subscriber
				.getLanguage());
		if (profileClips == null || profileClips.length < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TEMPORARY_OVERRIDE_LIST_FAILURE,
					m_temporaryOverrideListFailureDefault, subscriber
							.getLanguage()));
			return;
		}
		ArrayList<Clip> engClips = new ArrayList<Clip>();
		for (Clip profileClip : profileClips) {
			if (profileClip.getClipRbtWavFile() != null
					&& profileClip.getClipRbtWavFile().indexOf("_eng_") != -1) {
				if (profileClip.getClipSmsAlias() != null)
					engClips.add(profileClip);
			}
		}

		if (engClips.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TEMPORARY_OVERRIDE_LIST_FAILURE,
					m_temporaryOverrideListFailureDefault, subscriber
							.getLanguage()));
			return;
		}
        
		String sms = "";
		String smsAliases = ""; 
		int profileMaxLimit = param(SMS, PROFILE_LIST_COUNT, 1000);
		String profileEndSepartor = param(SMS, PROFILE_END_SEPARATOR, null);
		if(profileEndSepartor == null) {
			profileEndSepartor = " ";
		}
		for (int profileCount = 0; profileCount < engClips.size()
				&& profileCount < profileMaxLimit; profileCount++) {
			Clip profileClip = engClips.get(profileCount);
			StringTokenizer stk = new StringTokenizer(profileClip
					.getClipSmsAlias(), ",");
			if (profileMaxLimit == 1000){
				sms = sms + ", " + stk.nextToken();
			}else{
			    String clipSmsAlias = stk.nextToken();
				sms = sms + (profileCount + 1);
				boolean showDotInName = param(SMS,"SHOW_DOT_IN_PROFILE_LIST",false);
				if(showDotInName)
					sms += ".";
				else
					sms += ")";
				boolean showClipName = param(SMS,"SHOW_CLIP_NAME_IN_PROFILE_LIST",true);
				if(showClipName)
					sms += profileClip.getClipName();
				boolean showSeparator = param(SMS,"SHOW_SEPARATOR_BW_NAME_SMSALIAS_IN_PROFILE_LIST",true);
				if(showSeparator)
					sms += " -";
				boolean showClipSmsAlias = param(SMS,"SHOW_SMS_ALIAS_IN_PROFILE_LIST",true);
				if(showClipSmsAlias)
					sms += " " + clipSmsAlias + profileEndSepartor;

				smsAliases = smsAliases +","+ clipSmsAlias; 
			}
		}
		if (sms.startsWith(", "))
			sms = sms.substring(2);
		
        if(smsAliases!=null && smsAliases.startsWith(","))
        	smsAliases = smsAliases.substring(1); 
		
		boolean isProfileMenuListingAllowed = param(SMS, PROFILE_SET_ALLOWED_BY_INDEX, false);		
		if (profileMaxLimit != 1000) {
			task.setObject(param_SMSTYPE, "PROFILE");
			removeViraldata(task);
			if(isProfileMenuListingAllowed){
				//Storing SMS Aliases for ClipIds for sms menu profile listing
			    addViraldata(subscriberID, null, "PROFILE", smsAliases, "SMS", 1, null);
			}else{
				addViraldata(subscriberID, null, "PROFILE", null, "SMS", 1, null);
			}
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		String shortCode = task.getString(param_shortCode);	
		String smsText = null;
		if (engClips.size() > profileMaxLimit) {
			smsText = getSMSTextForID(task,
					shortCode+"_"+TEMPORARY_MORE_OVERRIDE_LIST_SUCCESS, null,
					subscriber.getLanguage());
			if (smsText == null)
				smsText = getSMSTextForID(task,
						TEMPORARY_MORE_OVERRIDE_LIST_SUCCESS, null,
						subscriber.getLanguage());

		}
		if(smsText == null ){
			smsText =getSMSTextForID(task, TEMPORARY_OVERRIDE_LIST_SUCCESS,
					m_temporaryOverrideListSuccessDefault, subscriber
					.getLanguage());
			if(shortCode != null) {
				smsText =getSMSTextForID(task, shortCode+"_"+TEMPORARY_OVERRIDE_LIST_SUCCESS,
					smsText, subscriber
					.getLanguage());
			}
		}
		hashMap.put("SMS_TEXT",smsText);
		hashMap.put("SONG_NAME", sms);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	@Override
	public void getNextProfile(Task task) {
		logger.info("inside getNextProfile");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		task.setObject(param_SMSTYPE, "PROFILE");
		logger.info("inside getNextProfile. task is " + task);
		ViralData context[] = getViraldata(task);
		if (context == null || context.length <= 0) {
			logger.info("getNextProfile context is "
					+ (context == null ? "zero null" : context.length));
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROFILE_NEXT_FAILURE, null, subscriber.getLanguage()));
			return;
		}
		int profileMaxLimit = param(SMS, PROFILE_LIST_COUNT, 1000);
		int nextCount = context[0].getCount();
		logger.info("getNextProfile. count is " + nextCount + " and limit is "
				+ profileMaxLimit);
		Clip[] profileClips = getClipsByCatId(99, subscriber.getLanguage());
		ArrayList<Clip> engClips = new ArrayList<Clip>();
		for (Clip profileClip : profileClips) {
			if (profileClip.getClipRbtWavFile() != null
					&& profileClip.getClipRbtWavFile().indexOf("_eng_") != -1) {
				if (profileClip.getClipSmsAlias() != null)
					engClips.add(profileClip);
			}
		}
		String profileNames = "";
		if (engClips.size() <= nextCount * profileMaxLimit)
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROFILE_NEXT_EXHAUSTED, null, subscriber.getLanguage()));
		else {
			int count = nextCount * profileMaxLimit;
			int maxCount = (nextCount + 1) * profileMaxLimit;
			String smsAliases = "";
			String profileEndSepartor = param(SMS, PROFILE_END_SEPARATOR, null);
			if(profileEndSepartor == null) {
				profileEndSepartor = " ";
			}
			for (int i = count; i < engClips.size() && i < maxCount; i++) {
				Clip profileClip = engClips.get(i);
				StringTokenizer stk = new StringTokenizer(profileClip
						.getClipSmsAlias(), ",");
				String clipSmsAlias = stk.nextToken();
				profileNames = profileNames	+ (i + 1) ;
				boolean showDotInName = param(SMS,"SHOW_DOT_IN_PROFILE_LIST",false);
				if(showDotInName)
					profileNames += ".";
				else
					profileNames += ")";
				boolean showClipName = param(SMS,"SHOW_CLIP_NAME_IN_PROFILE_LIST",true);
				if(showClipName)
					profileNames += profileClip.getClipName();
				boolean showSeparator = param(SMS,"SHOW_SEPARATOR_BW_NAME_SMSALIAS_IN_PROFILE_LIST",true);
				if(showSeparator)
					profileNames += " -";
				boolean showClipSmsAlias = param(SMS,"SHOW_SMS_ALIAS_IN_PROFILE_LIST",true);
				if(showClipSmsAlias)
					profileNames += " " + clipSmsAlias + profileEndSepartor;
				//For SMS PROFILE MENU LISTING
				smsAliases = smsAliases + "," + clipSmsAlias;
			}
			if(smsAliases!=null && smsAliases.startsWith(",")){
				smsAliases = smsAliases.substring(1); 
			}
			HashMap<String, String> hashMap = new HashMap<String, String>();
			String smsText = null;
			String shortCode = task.getString(param_shortCode);	
			if (engClips.size() > maxCount) {
				smsText = getSMSTextForID(task, shortCode+"_"+PROFILE_NEXT_FOOTER,
								null, subscriber.getLanguage());
				if (smsText == null)
					smsText = getSMSTextForID(task, PROFILE_NEXT_FOOTER, null,
									subscriber.getLanguage());
			} else {
				smsText = getSMSTextForID(task, shortCode+"_"+PROFILE_NEXT_SUCCESS,
						null, subscriber.getLanguage());
				if (smsText == null)
					smsText = getSMSTextForID(task, PROFILE_NEXT_SUCCESS, null,
							subscriber.getLanguage());
			}
			boolean isProfileMenuListingAllowed = param(SMS, PROFILE_SET_ALLOWED_BY_INDEX, false);
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("SONG_NAME", profileNames);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			task.setObject(param_SEARCHCOUNT, (nextCount + 1) + "");
			if (isProfileMenuListingAllowed) {
				task.setObject(param_clipid, smsAliases);
			}
			updateViraldata(task);
		}
	}

	@Override
	public void processListCategories(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		Category[] categories = null;
		if (task.containsKey(param_isSuperHitAlbum))
			categories = getCategoriesByType("16", language); // Lists Automatic
																// Shuffles

		if (categories == null || categories.length < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TEMPORARY_OVERRIDE_LIST_FAILURE,
					m_temporaryOverrideListFailureDefault, language));
			return;
		}

		String sms = "";
		for (Category category : categories) {
			if (category.getCategoryEndTime().getTime() > System
					.currentTimeMillis()) {
				if (category.getCategoryPromoId() != null)
					sms = sms + category.getCategoryName() + "-"
							+ category.getCategoryPromoId() + " ";
				else if (category.getCategorySmsAlias() != null)
					sms = sms + category.getCategoryName() + "-"
							+ category.getCategorySmsAlias() + " ";
			}
		}

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, CATEGORY_LIST_SUCCESS,
				CategoryListSuccess1Default, language));
		hashMap.put("SONG_LIST", sms);

		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String finalText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, finalText);

	}

	@Override
	public void processListen(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, language));
			return;
		}
		String songCode = smsList.get(0);
		if (songCode.startsWith("tp"))
			songCode = getPromoCodeForTopListCode(task, songCode.substring(2));

		if (songCode == null) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, language));
			return;
		}

		Clip clip = getClipByPromoId(songCode, language);
		if (clip == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					LISTEN_FAILURE, m_listenFailureTextDefault, language));
			return;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, LISTEN_SUCCESS,
				m_listenSuccessTextDefault, language));
		hashMap.put("SONG_NAME", clip.getClipName());
		hashMap.put("CODE", param(SMS, OPERATOR_SHORTCODE, "")
				+ clip.getClipPromoId());
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));

	}

	@Override
	public void processSongOfMonth(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		task.setObject(param_responseSms, getSMSTextForID(task,
				SONG_OF_MONTH_FAILURE, m_songOfMonthFailureDefault, language));
	}

	@Override
	public void processCancelCopyRequest(Task task) {

		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = getSubscriber(subscriberID);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		task.setObject(param_CALLER_ID, subscriberID);
		// task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_SMSTYPE, "COPYCONFIRM");
		task.setObject(param_WAITTIME, param(GATHERER,
				WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		String success = removeViraldata(task);
		if (success.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					COPY_CANCEL_SUCCESS, m_copyCancelSuccessDefault, language));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					COPY_CANCEL_FAILURE, m_copyCancelFailureDefault, language));

	}

	@Override
	public void processConfirmCopyRequest(Task task) {
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = getSubscriber(subscriberID);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_CHANGE_TYPE, "COPYCONFIRMED");
		task.setObject(param_update_sms_id, "TRUE");
		task.setObject(param_WAITTIME, param(GATHERER,
				WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		if (!task.containsKey(param_mode))
			task.setObject(param_mode, param(SMS, COPY_CONF_MODE_SMS, "SMS"));
		else if (task.getString(param_mode) == null
				|| task.getString(param_mode).equalsIgnoreCase(""))
			task.setObject(param_mode, param(SMS, COPY_CONF_MODE_SMS, "SMS"));

		logger.info("RBT:: TASKTASK : " + task);
		String response = updateViraldata(task);

		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, COPY_CONFIRM_SUCCESS,
							m_copyConfirmSuccessDefault, language));
			task.setObject(param_responseUssd, "SUCCESS");
		} else {
			task.setObject(param_responseSms,
					getSMSTextForID(task, COPY_CONFIRM_FAILURE,
							m_copyConfirmFailureDefault, language));
			task.setObject(param_responseUssd, "FAILURE");
		}
	}
	
	/**
	 * To confirm like request, updates the viral data in database and based on
	 * the update status, place the message text in the task object. The message
	 * text can be either from configuration or default value. It is a
	 * replication of copy confirm feature.
	 */
	@Override 
	public void processConfirmLikeRequest(Task task) {
		logger.info("Processing like confirm request. task: " + task);
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = getSubscriber(subscriberID);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		task.setObject(param_callerid, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_CHANGE_TYPE, "COPYCONFIRMED");
		task.setObject(param_update_sms_id, "TRUE");
		task.setObject(param_WAITTIME, param(GATHERER,
				WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		String copyConfMode = param(SMS, COPY_CONF_MODE_SMS, "SMS");
		if (!task.containsKey(param_mode)) {
			task.setObject(param_mode, copyConfMode);
		} else if (task.getString(param_mode) == null
				|| task.getString(param_mode).equalsIgnoreCase("")) {
			task.setObject(param_mode, copyConfMode);
		}
	
		logger.debug("Updating viral data to confirm like request. task: "
				+ task);

		// Get the viral data of type COPYCONFPENDING. 
		ViralData[] viralData = getViraldata(task);
		if (null == viralData) {
			logger.debug("No ViralData found for subscriberId: " + subscriberID);
		} else {
			logger.debug("Found ViralData size: " + viralData.length
					+ ", for subscriberId: " + subscriberID);
			// If there are 5 viral requests it will update all of them.
			for (ViralData vdata : viralData) {
				Map<String, String> vInfo = vdata.getInfoMap();
				if(vInfo.containsKey(KEYPRESSED_ATTR)) {
					String keyPressed = vInfo.get(KEYPRESSED_ATTR);
					if(SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {
						task.setObject(param_CALLER_ID, subscriberID);
						String response = updateViraldata(task);
						logger.debug("vdata: " + vdata + ", response: " + response);
						String smsText = "";
						String responseUssd = "";
						if (response
								.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
							smsText = getSMSTextForID(task,
									RBT_LIKE_CONFIRM_SUCCESS,
									m_likeConfirmSuccessDefault, language);
							responseUssd = "SUCCESS";
						} else {
							smsText = getSMSTextForID(task, RBT_LIKE_CONFIRM_FAILURE,
									m_likeConfirmFailureDefault, language);
							responseUssd = "FAILURE";
						}
						
						if (smsText.contains("%song%")) {
							logger.debug("Replacing the song in smsText: "
									+ smsText);
							String song = "";
							String clipId = vdata.getClipID();
							int index = clipId.indexOf(":");
							if (index != -1) {
								try {
									String wavFile = clipId.substring(0, index);
									Clip clip = RBTCacheManager.getInstance()
											.getClipByRbtWavFileName(wavFile);
									if (null != clip) {
										song = clip.getClipName();
									} else {
										logger.warn("Clip not found. clipWavFile:  "
												+ wavFile);
									}
								} catch (Exception e) {
									logger.error(
											"Clip not found. error: "
													+ e.getMessage(), e);
								}
							}
							smsText = smsText.replaceAll("%song%", song);
						}

						if (smsText.contains("%caller%")) {
							logger.debug("Replacing the caller in smsText: "
									+ smsText);
							String callerId = subID(vdata.getSubscriberID());
							smsText = smsText.replaceAll("%caller%", callerId);
						}
						

						task.setObject(param_responseSms, smsText);
						task.setObject(param_responseUssd, responseUssd);
						logger.info("Successfully confirmed like request. smsText: "
								+ smsText + ", responseUssd: " + responseUssd);
					} else {
						logger.warn("Unable to update viral data, KeyPressed: " + keyPressed
								+ " is not present in RBTLikeKeys: "
								+ SmsKeywordsStore.likeKeywordsSet);
					}
				} else {
					logger.warn("Attribute KEY is not present in extra info of ViralData. extra info: "
							+ vInfo);
				}
			}
		}
	}
	

	// Added to confirm subscription and copy using same keyword for VIVO
	@Override
	public void processConfirmSubscriptionNCopy(Task task) {

		logger.debug("Processing Subscription and copy feature");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ViralData[] smsConfVst = getViraldata(subscriber.getSubscriberID(),
				null, SMSCONFPENDING);
		ViralData[] copyConfVst = getViraldata(null, subscriber
				.getSubscriberID(), COPYCONFPENDING);
		ViralData[] WebRequestVst = getViraldata(subscriber.getSubscriberID(),
				null, "WEB_REQUEST");
		ViralData[] randomizeRequestVst = getViraldata(subscriber.getSubscriberID(),
				null, "RANDOMIZE");

		String deactSmsType = "CAN";
		Parameters subClassParams = CacheManagerUtil
				.getParametersCacheManager().getParameter(COMMON,
						"SUBSCRIPTION_CLASS_FOR_CHURN_OFFER");
		if (subClassParams != null) {
			ViralBlackListTable viralBlackList = RBTDBManager.getInstance()
					.getViralBlackList(subscriber.getSubscriberID(),
							"CHURN_OFFER");
			if (viralBlackList == null)
				deactSmsType = "CANCEL_OFFER";
		}

		ViralData[] deactRequestVst = getViraldata(
				subscriber.getSubscriberID(), null, deactSmsType);
		ViralData latestViralData = null;

		List<ViralData> completeViralDataList = new ArrayList<ViralData>();
		completeViralDataList.addAll(Arrays.asList(smsConfVst));
		completeViralDataList.addAll(Arrays.asList(copyConfVst));
		completeViralDataList.addAll(Arrays.asList(WebRequestVst));
		completeViralDataList.addAll(Arrays.asList(deactRequestVst));
		completeViralDataList.addAll(Arrays.asList(randomizeRequestVst));

		Collections.sort(completeViralDataList);

		if (completeViralDataList.size() > 0)
			latestViralData = completeViralDataList.get(completeViralDataList
					.size() - 1);

		logger.info("Latest Viral Data got : " + latestViralData);
		if (latestViralData == null) {
			String smsText = getSMSTextForID(task, HELP_SMS_TEXT,
					m_helpDefault, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		}

		if (latestViralData.getType().equalsIgnoreCase("WEB_REQUEST")) {
			Clip clip = getClipById(latestViralData.getClipID(), subscriber
					.getLanguage());
			processWebRequest(task, null, latestViralData, clip, true);

			if (smsConfVst != null && smsConfVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						SMSCONFPENDING);
			if (copyConfVst != null && copyConfVst.length > 0)
				removeViraldata(null, subscriber.getSubscriberID(), "X"
						+ COPYCONFPENDING);
			if (deactRequestVst != null && deactRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(), deactSmsType);
			if (randomizeRequestVst != null && randomizeRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"RANDOMIZE");
		} else if (latestViralData.getType().equalsIgnoreCase(COPYCONFPENDING)) {
			logger.info("Processing copy feature");
			task.setObject(param_SENT_TIME, latestViralData.getSentTime());
			processConfirmCopyRequest(task);

			if (smsConfVst != null && smsConfVst.length > 0)
				removeViraldata(subscriber.getSubscriberID(), null,
						SMSCONFPENDING);
			if (WebRequestVst != null && WebRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"WEB_REQUEST");
			if (deactRequestVst != null && deactRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(), deactSmsType);
			if (randomizeRequestVst != null && randomizeRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"RANDOMIZE");
		} else if (latestViralData.getType().equalsIgnoreCase(deactSmsType)) {
			logger.info("Processing deactivation feature");
			processDeactivationConfirmed(task, deactSmsType);

			if (smsConfVst != null && smsConfVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						SMSCONFPENDING);
			if (copyConfVst != null && copyConfVst.length > 0)
				removeViraldata(null, subscriber.getSubscriberID(), "X"
						+ COPYCONFPENDING);
			if (WebRequestVst != null && WebRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"WEB_REQUEST");
			if (randomizeRequestVst != null && randomizeRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"RANDOMIZE");
		} else if (latestViralData.getType().equalsIgnoreCase("RANDOMIZE")) {
			logger.info("Processing randomization feature");
			enableRandomization(task);

			if (task.containsKey(param_respMessage))
			{
				removeViraldata(latestViralData.getSubscriberID(),
						"RANDOMIZE");
			}

			if (smsConfVst != null && smsConfVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						SMSCONFPENDING);
			if (copyConfVst != null && copyConfVst.length > 0)
				removeViraldata(null, subscriber.getSubscriberID(), "X"
						+ COPYCONFPENDING);
			if (WebRequestVst != null && WebRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"WEB_REQUEST");
			if (deactRequestVst != null && deactRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(), deactSmsType);
			
		} else {
			logger.info("Processing Subscription feature");
			confirmActNSel(task, latestViralData);

			if (copyConfVst != null && copyConfVst.length > 0)
				removeViraldata(null, subscriber.getSubscriberID(), "X"
						+ COPYCONFPENDING);
			if (WebRequestVst != null && WebRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"WEB_REQUEST");
			if (deactRequestVst != null && deactRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(), deactSmsType);
			if (randomizeRequestVst != null && randomizeRequestVst.length > 0)
				removeViraldata(latestViralData.getSubscriberID(),
						"RANDOMIZE");
		}

	}

	@Override
	public void processCancelOptInCopy(Task task) {
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = getSubscriber(subscriberID);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_WAITTIME, param(GATHERER,
				WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		String response = removeViraldata(task);
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					COPY_CANCEL_SUCCESS, m_copyCancelSuccessDefault, language));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					COPY_CANCEL_FAILURE, m_copyCancelFailureDefault, language));
	}

	@Override
	public void processDeactivateDownload(Task task) {
		
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = task.getString(param_callerid);
		ArrayList<String> smsList = (ArrayList<String>) task
		.getObject(param_smsText);
		String subscriberId = subscriber.getSubscriberID();
		
		logger.debug("Processing deactive download request for subscriber: "
				+ subscriberId + ", keywords: " + smsList);

		if (!isUserActive(subscriber.getStatus())) {
			String deactDownloadSmsText = getSMSTextForID(task, "DEACT_DOWNLOAD_NOT_ALLOWED_FOR_"
					+ subscriber.getStatus().toUpperCase(), null, subscriber.getLanguage());
			logger.info("Unable to process deactive download request, subscriber: "
					+ subscriberId + " is not active . Deact Download Sms Text ="+deactDownloadSmsText);
			if(deactDownloadSmsText == null){
				deactDownloadSmsText = getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage());
			}
			task.setObject(param_responseSms,deactDownloadSmsText);
			return;
		}

		if ((smsList == null || smsList.size() < 1) && !task.containsKey(param_promoID)) {
			logger.info("Unable to process deactive download request for subscriber: "
					+ subscriberId + ", sms keywords does not exists");
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_DEACT_INVALID_PROMO_ID,
					m_downloadDeactInvalidPromoId, subscriber.getLanguage()));
			return;
		}

		String keyword = smsList.get(0).trim();
		if(keyword == null) keyword = task.getString(param_promoID);
		logger.debug("Checking clip by promo id: " + keyword
				+ " for subscriber: " + subscriberId);
		Clip clip = getClipByPromoId(keyword, subscriber.getLanguage());
		String categoryId = null;
		String wavFile = null;
		Category category = null;
		if (clip != null) {
			wavFile = clip.getClipRbtWavFile();
		} else {
			logger.debug("Since clip doesnt exists, checking for category for"
					+ " subscriber: " + subscriberId);
			category = getCategoryByPromoId(keyword, subscriber.getLanguage());
			if (null == category && isCheckCategorySmsAlias) {
				logger.debug("Checking category based on sms alias for"
						+ " subscriber: " + subscriberId);
				category = getCategoryBySMSAlias(keyword, subscriber
						.getLanguage());
			}
			if (category != null) {
				categoryId = String.valueOf(category.getCategoryId());
			}
		}

		if (wavFile == null && categoryId == null) {
			logger.info("Unable to process deactive download request for subscriber: "
					+ subscriberId + ", wave file and category id are not found");
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_DEACT_INVALID_PROMO_ID,
					m_downloadDeactInvalidPromoId, subscriber.getLanguage()));
			return;
		}
		if (clip != null)
			task.setObject(param_clipid, "" + clip.getClipId());
		if (category != null)
			task.setObject(param_catid, "" + category.getCategoryId());
		if (callerID == null)
			callerID = "ALL";
		
		// deactivate selection 
		String deactivateSelResponse = processDeactivateSelection(task);
		String deleteSubscriberDownload = deleteSubscriberDownload(task);
		logger.info("deactivate selection Response: " + deactivateSelResponse
				+ ", delete subscriber download response: " + deleteSubscriberDownload);
		
		if (callerID.equalsIgnoreCase("ALL")) {
			callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
		}
		String contentName = "";
		if (clip != null)
			contentName = clip.getClipName();
		else if (category != null)
			contentName = category.getCategoryName();
		String smsText = null;
		if (deleteSubscriberDownload.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			smsText = getSMSTextForID(task, DOWNLOAD_DEACT_SUCCESS,
					m_downloadDeactSuccessDefault, subscriber.getLanguage());
		else
			smsText = getSMSTextForID(task, DOWNLOAD_DEACT_FAILURE,
					m_downloadDeactFailureDefault, subscriber.getLanguage());
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	@Override
	public void processHelp(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String helpOn = "HELP";
		if (smsList != null && smsList.size() > 0)
			helpOn = smsList.get(0).trim().toUpperCase();

		task.setObject(param_responseSms, getSMSTextForID(task, "HELP_"
				+ helpOn, m_helpDefault, language));
	}

	@Override
	public void processViralAccept(Task task) {
		String categoryId = "3";
		Category category = null;

		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		String subscriptionClass = subscriber.getSubscriptionClass();

		logger.info("Subscriber details. subscriberID: " + subscriberID
				+ ", subscriptionClass: " + subscriptionClass);
		
		String smsType = "BASIC";
		if (RBTParametersUtils.getParamAsBoolean(COMMON, "VIRAL_OPTIN_MODEL_SUPPORTED", "FALSE"))
		{
			smsType = "VIRAL_OPTIN";
		}
		
		// RBT-10785: OI deployements
		// subscription clas is migrated subscripton class update smsType t VIRAL_OPTIN
		if(migratedUserSubClassesList.contains(subscriptionClass)) {
			smsType = "VIRAL_OPTIN";
		}
		
		task.setObject(param_SMSTYPE, smsType);
		ViralData context[] = (ViralData[]) task.getObject(param_viral_data);
		if(context == null || context.length < 1 || context[0] == null){
			context = getViraldata(task);
		}
		if (context == null || context.length < 1 || context[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		ViralData viralData = context[context.length - 1];
		logger.info("RBT:: Context : " + viralData + ", smsType: " + smsType);

		String profileHours = null;
		String chargeClass = "DEFAULT";
		String subClass = "DEFAULT";
		HashMap<String, String> contextInfoMap = viralData.getInfoMap();
		boolean isInfoMapIsNotNull = (contextInfoMap != null);
		if (isInfoMapIsNotNull) {
			String inLoop = contextInfoMap.get("inLoop");
			if (inLoop != null && inLoop.equalsIgnoreCase("true")) {
				task.setObject(param_inLoop, "yes");
			}
		}
		if (isInfoMapIsNotNull
				&& contextInfoMap.containsKey("SMS_TYPE")
				&& contextInfoMap.get("SMS_TYPE").equalsIgnoreCase(
						"WEBSELECTION")) {
			String[] tokens = param(SMS, "WEBSELECTION_CONFIG", "").split(","); // activatedBy,
																				// selectedBy,
																				// classType,
																				// categoryID
			if (tokens.length >= 1) {
				task.setObject(param_actby, tokens[0]);
			}
			if (tokens.length >= 2)
				task.setObject(param_actby, tokens[1]);
			if (tokens.length >= 3)
				task.setObject(param_subclass, tokens[2]);

			if (contextInfoMap.containsKey("CATEGORY_ID")) {
				// check if the category id is given by the 3rd party URL.
				categoryId = contextInfoMap.get("CATEGORY_ID");
			} else if (tokens.length >= 4) {
				// if not, get the category id from the config
				// WEBSELECTION_CONFIG
				categoryId = tokens[3];
			}
			category = getCategory(categoryId, subscriber.getLanguage());

			if (contextInfoMap.containsKey("PROFILE_HOURS"))
				profileHours = contextInfoMap.get("PROFILE_HOURS");

			String callerID = viralData.getCallerID();
			if (callerID != null && !callerID.trim().equals(""))
				task.setObject(param_callerid, callerID);

		} else {
			if (param(SMS, IS_VIRAL_CLIP_ALLOWED, true)
					&& viralData.getClipID() == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
								.getLanguage()));
				return;
			}

			int expiryHrs = RBTParametersUtils.getParamAsInt("SMS", "VIRAL_SMS_EXPIRY_PERIOD_IN_HRS", 0);	 
            if (expiryHrs > 0)	 
            {	 
                    Date sentTime = viralData.getSentTime();	 
                    if ((System.currentTimeMillis() - sentTime.getTime()) > (expiryHrs * 60 * 60 * 1000L))	 
                    {	 
                            task.setObject(param_responseSms, getSMSTextForID(task,	 
                                            VIRAL_ENTRY_EXPIRED, m_viralEntryExpiredTextDefault, subscriber	 
                                                            .getLanguage()));	 
                            return;	 
                    }	 
            }

			StringTokenizer st = new StringTokenizer(param(SMS, VIRAL_KEYWORD,
					""), ",");
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens())
				task.setObject(param_actMode, st.nextToken());
			if (st.hasMoreTokens())
				task.setObject(param_actby, st.nextToken());
			if (st.hasMoreTokens())
			{
				subClass = st.nextToken();
				task.setObject(param_subclass, subClass);
			}
			if (st.hasMoreTokens()) {
				categoryId = st.nextToken();
				category = getCategory(categoryId, subscriber.getLanguage());
			}
			if (st.hasMoreTokens())
			{
				chargeClass = st.nextToken();
				task.setObject(param_chargeclass, chargeClass);
				task.setObject(param_USE_UI_CHARGE_CLASS, true);
			}
		}

		if (category == null) {
			logger
					.info("RBT:: CategoryID is null. Getting default category ID 3 ");
			categoryId = "3";
			category = getCategory(3, subscriber.getLanguage());
		}
		task.setObject(param_catid, categoryId);
		task.setObject(CAT_OBJ, category);
		if (profileHours != null)
			task.setObject(param_profile_hours, profileHours);

		if (RBTParametersUtils.getParamAsBoolean(COMMON, "VIRAL_OPTOUT_MODEL_SUPPORTED", "FALSE"))
		{
			updateViraldataType("VIRAL_OPTOUT", subscriberID, "BASIC");
			String smsText = getSMSTextForID(task,
					VIRAL_OPTOUT, m_viralOptOutTextDefault, subscriber
					.getLanguage());
			smsText = smsText.replace("%PRICE", CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass).getAmount());
			smsText = smsText.replace("%SUB_PRICE", CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subClass).getSubscriptionAmount());
			task.setObject(param_responseSms, smsText);
			return;
		}
		//RBT 12928 -Changes done for allow viral default song selection
		//Default Clip, not make song sel, new user / deactivated user -- activate the base
		//Default Clip, not make song sel, active user (A,N,B,G,Z,D,P) -- Send new sms to be configuration
		//Default Clip, make song sel, new user / deactivated user -- activate base and song
		//Default Clip, make song sel, active user, song already present -- Send new sms to be configuration
		//Default Clip, make song sel, active user, song not present -- activate song

		String defaultClipId = RBTParametersUtils.getParamAsString(
				"COMMON", "DEFAULT_CLIP", null);
		boolean makeDefaultSong = RBTParametersUtils.getParamAsBoolean(
				"GATHERER", "INSERT_DEFAULT_SEL", "false");
		boolean activateBaseOnly = false;
		boolean isDefaultSong = false;
		
		if ((null != defaultClipId && !defaultClipId.isEmpty() && viralData
				.getClipID().equalsIgnoreCase(defaultClipId))) {
			isDefaultSong = true;

		}
		// Default Clip, not make song sel, active user (A,N,B,G,Z,D,P) -- Send
		// new sms to be configuration
		if (isDefaultSong && isUserActive(subscriber.getStatus())
				&& !makeDefaultSong) { // Msg need to be asked.
			String smsText = getSMSTextForID(task,
					DEFAULT_SELECTION_NOT_ALLOWED_TEXT,
					m_viralDefaultSongTextDefault, subscriber.getLanguage());
			task.setObject(param_responseSms, smsText);
			return;
		}// Default Clip, not make song sel, new user / deactivated user --
			// activate the base
		else if (isDefaultSong && !isUserActive(subscriber.getStatus())
				&& !makeDefaultSong) {
			activateBaseOnly = true;
		}
		//RBT 12928 -Changes done for allow viral default song selection
		if (viralData.getClipID() != null && !activateBaseOnly) {
			Clip clipMinimal = getClipById(viralData.getClipID(), subscriber
					.getLanguage());

			task.setObject(CLIP_OBJ, clipMinimal);
			if (clipMinimal != null)
				task.setObject(param_clipid, clipMinimal.getClipId() + "");
			String subStatus = subscriber.getStatus();
			boolean combo = isUserActive(subStatus) ? false : true;
			String response = processSetSelection(task);

			if (isActivationFailureResponse(response)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
			}
			if (response.equals(WebServiceConstants.SUCCESS)) {
				String sms = null;
				HashMap<String, String> hashMap = new HashMap<String, String>();
				String callerId = task.getString(param_callerid) == null ? param(SMS,
						SMS_TEXT_FOR_ALL, "all") : task
						.getString(param_callerid);
				hashMap.put("CALLER_ID", callerId);
				if (clipMinimal != null) {
					if (null != category
							&& com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isShuffleCategory(category.getCategoryTpe())) {
						hashMap.put("SONG_NAME", category.getCategoryName());
						hashMap.put("PROMO_ID",category.getCategoryPromoId());
					} else {
						hashMap.put("SONG_NAME", clipMinimal.getClipName());
						hashMap.put("PROMO_ID", clipMinimal.getClipPromoId());
					}
					hashMap.put("ALBUM", clipMinimal.getAlbum());
					hashMap.put("ARTIST", clipMinimal.getArtist());
				}
				//String chargeClassAmount = getChargeClassFromSelections(task, null, clipMinimal);
				
				hashMap.put("CIRCLE_ID", subscriber.getCircleID());
				if (combo) {
					sms = getSMSTextForID(task,
							"VIRAL_DEFAULT_COMBO_SUCCESS", m_viralSuccessTextDefaultSongText, subscriber
							.getLanguage());
					String subAmount = null;
					if (task.getTaskSession() != null) {
						Rbt rbt = (Rbt) task.getTaskSession().get(param_rbt_object);
						if (rbt != null) {
							Subscriber sub = rbt.getSubscriber();
							if (sub != null) {
								subscriptionClass = sub.getSubscriptionClass();
								if (subscriptionClass != null) {
									SubscriptionClass subClassLocal = CacheManagerUtil
											.getSubscriptionClassCacheManager()
											.getSubscriptionClass(subscriptionClass);
									if (subClassLocal != null) {
										subAmount = subClassLocal.getSubscriptionAmount();
									}
								}
							}
						}
					}
					hashMap.put("ACT_AMT", subAmount == null ? "" : subAmount);
				}
				if (sms == null) {
					sms =  getSMSTextForID(task,
							VIRAL_DEFAULT_SONG_SUCCESS, m_viralSuccessTextDefaultSongText, subscriber
							.getLanguage());
				}
				hashMap.put("SMS_TEXT", sms);
				//RBT-16981- Issue with sending confirmation request for Viral || VDS STG
				com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass chargeClassDB = getChargeClassFromSettings(task, null, clipMinimal);
				String chargeClassAmount = null;
				if (null != chargeClassDB) {
					chargeClassAmount = chargeClassDB.getAmount();
				}
				hashMap.put("SEL_AMT", chargeClassAmount);
				
				if (null != chargeClassDB) {
					String selAmount = chargeClassDB.getAmount();
					String renewalAmount = null,renewalPeriod = null,freePeriodText=null,specialAmtChar=null;
					if (null != selAmount) {
						specialAmtChar = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
								"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
						if(Double.parseDouble(selAmount.replace(specialAmtChar,".")) == 0){
							renewalAmount = chargeClassDB.getRenewalAmount();
							renewalPeriod = com.onmobile.apps.ringbacktones.webservice.common.Utility.getSubscriptionPeriodInDays(chargeClassDB.getSelectionPeriod());
							freePeriodText = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
									"FREE_SMS_PERIOD_TEXT", "(DD dias GRATIS)");
							freePeriodText = freePeriodText.replace("DD",renewalPeriod);
							
							hashMap.put("RENEWAL_AMOUNT", renewalAmount);
							hashMap.put("FREE_PERIOD_TEXT",freePeriodText);
						}
					}
				}
				
				String smsText = finalizeSmsText(hashMap);
				task.setObject(param_responseSms, smsText);
			} else if (response.equals(WebServiceConstants.ALREADY_EXISTS) && isDefaultSong) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						DEFAULT_SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
								PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage()));
			} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
								PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage()));
			}else {
				//RBT-12599
				String smsTextForID = getSMSTextForID(task,
						response.toUpperCase(), null,
						subscriber.getLanguage());
				if(smsTextForID==null) {
					smsTextForID = getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage());
				}
				task.setObject(param_responseSms, smsTextForID);
			}
		} else {
			subscriber = processActivation(task);
			if (!isUserActive(subscriber.getStatus())) {//RBT 12928 -Changes done for allow viral default song selection
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, TECHNICAL_FAILURE,
								m_technicalFailuresDefault,
								subscriber.getLanguage()));
				return;
			} else if ("success".equalsIgnoreCase(task
					.getString(param_response)) && isDefaultSong) {
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, VIRAL_DEFAULT_SONG_ACTIVATION_SUCCESS,
								m_viralSuccessTextDefaultActivation,
								subscriber.getLanguage()));
			}
		}

		updateViraldataType("VIRAL_EXPIRED", subscriberID, smsType);
		if (RBTParametersUtils.getParamAsBoolean(SMS, "DELETE_VIRAL_ENTRY_AFTER_USER_ACCEPTANCE", "TRUE"))
			removeViraldata(viralData.getSmsID(), smsType);
	}

	public boolean isActivationFailureResponse(String response) {
		if (response.equalsIgnoreCase(WebServiceConstants.FAILED)
				|| response.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| response.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)
				|| response.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| response.equalsIgnoreCase(WebServiceConstants.LOCKED)
				|| response
						.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| response.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| response.equalsIgnoreCase(WebServiceConstants.SUSPENDED)
				|| response.equalsIgnoreCase(WebServiceConstants.ERROR)) {
			return true;
		}

		return false;
	}

	@Override
	public void processMgmAccept(Task task) {
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		String mgmConfig = param(SMS, MGM_PARAMS, "OFF,MGM,MGM,7,2");
		StringTokenizer stk = new StringTokenizer(mgmConfig, ",");
		stk.nextToken();
		stk.nextToken();
		String mgmActBy = stk.nextToken().toLowerCase();

		task.setObject(param_SMSTYPE, "MGM");
		task.setObject(param_callerid, subscriberID);
		task.setObject(param_subscriberID, null);
		ViralData context[] = getViraldata(task);
		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_callerid, null);
		logger.info("RBT:: Context : " + context);
		if (context == null || context.length < 1 || context[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MGM_RECIPIENT_ACK_FAILURE, m_mgmRecAccFailureDefault,
					subscriber.getLanguage()));
			return;
		}
		removeViraldata(null, subscriberID, "MGM");

		task.setObject(param_actby, mgmActBy);
		task.setObject(param_actInfo, task.getString(ACT_INFO) + ":"
				+ context[0].getSubscriberID());
		stk = new StringTokenizer(context[0].getClipID(), " ");
		String token = stk.nextToken();
		if (!isUserActive(subscriber.getStatus()) && !stk.hasMoreTokens()
				&& !param(SMS, IS_ACT_ALLOWED, true)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MGM_SENDER_FAILURE, m_mgmSenderFailureTextDefault,
					subscriber.getLanguage()));
			return;
		}
		if (isUserActive(subscriber.getStatus()) && !stk.hasMoreTokens()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MGM_SENDER_FAILURE, m_mgmSenderFailureTextDefault,
					subscriber.getLanguage()));
			return;
		}

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, MGM_RECIPIENT_ACK,
				m_mgmRecAccSuccessDefault, subscriber.getLanguage()));
		if (stk.hasMoreTokens()) {
			token = stk.nextToken();
			String categoryID = "25";
			Category category = null;
			String contentName = "";
			if (token.toUpperCase().startsWith("C")) {
				categoryID = token.substring(1);
				category = getCategory(categoryID, subscriber.getLanguage());
				Clip[] clip = getClipsByCatId(Integer.parseInt(categoryID),
						subscriber.getLanguage());
				if (category != null && clip != null && clip.length > 0
						&& clip[0] != null) {
					task.setObject(CAT_OBJ, category);
					task.setObject(CLIP_OBJ, clip[0]);
					contentName = category.getCategoryName();
				} else {
					task.setObject(param_responseSms, getSMSTextForID(task,
							TECHNICAL_FAILURE, m_technicalFailuresDefault,
							subscriber.getLanguage()));
					return;
				}
			} else {
				category = getCategory(categoryID, subscriber.getLanguage());
				Clip clip = getClipById(token, subscriber.getLanguage());
				if (clip != null && category != null) {
					task.setObject(param_catid, category.getCategoryId() + "");
					task.setObject(param_clipid, clip.getClipId() + "");
					contentName = clip.getClipName();
				}
			}
			String response = processSetSelection(task);
			if (!response.equals("success")) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
				return;
			}
			hashMap.put("SONG_NAME", contentName);
		} else {
			subscriber = processActivation(task);
			if (!isUserActive(subscriber.getStatus())) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
				return;
			}
		}
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	@Override
	public void processRetailerAccept(Task task) {
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_SMSTYPE, "RETAILER");
		ViralData context[] = getViraldata(task);

		if (context == null || context.length < 1 || context[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_FAILURE, m_retFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}
		StringTokenizer stk = new StringTokenizer(param(SMS, RETAILER_ACT,
				"RET,RET,DEFAULT"), ",");
		task.setObject(ACT_BY, stk.nextToken());
		stk.nextToken();
		task.setObject(SUB_CLASS_TYPE, stk.nextToken());
		task.setObject(ACT_INFO, task.getString(ACT_INFO) + ":"
				+ context[0].getCallerID());
		if (task.getObject(param_actInfo) != null) {
			task.setObject(param_actInfo, task.getString(param_actInfo) + ":"
					+ context[0].getCallerID());
		} else {
			task.setObject(param_actInfo, context[0].getCallerID());
		}

		stk = new StringTokenizer(context[0].getClipID(), " ");
		String token = stk.nextToken();
		String retaileeText = getSMSTextForID(task,
				RETAILER_ACCEPT_RESPONSE_SENDER_SEL, m_retAccSelSMSDefault,
				subscriber.getLanguage());
		Subscriber sub = getSubscriber(context[0].getCallerID());

		String retailerText = getSMSTextForID(task,
				RETAILER_RESP_SMS_ACCEPT_SEL, m_retRespSMSAcceptDefault, sub
						.getLanguage());
		if (!isUserActive(subscriber.getStatus()) && !stk.hasMoreTokens()
				&& !param(SMS, IS_ACT_ALLOWED, true)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_FAILURE, m_retFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		retaileeText = getSMSTextForID(task,
				RETAILER_ACCEPT_RESPONSE_SENDER_SEL, m_retAccSelSMSDefault,
				subscriber.getLanguage());
		retailerText = getSMSTextForID(task, RETAILER_RESP_SMS_ACCEPT,
				m_retRespSMSAcceptDefault, sub.getLanguage());

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", retaileeText);
		String contentName = "";
		if (stk.hasMoreTokens()) {
			token = stk.nextToken();
			String categoryID = "24";
			Category category = null;
			if (token.toUpperCase().startsWith("C")) {
				categoryID = token.substring(1);
				category = getCategory(categoryID, subscriber.getLanguage());
				Clip[] clip = getClipsByCatId(Integer.parseInt(categoryID),
						subscriber.getLanguage());
				if (category != null && clip != null && clip.length > 0
						&& clip[0] != null) {
					task.setObject(CAT_OBJ, category);
					task.setObject(CLIP_OBJ, clip[0]);
					task.setObject(param_clipid, clip[0].getClipId() + "");
					task.setObject(param_catid, category.getCategoryId() + "");
					contentName = category.getCategoryName();
				} else {
					task.setObject(param_responseSms, getSMSTextForID(task,
							TECHNICAL_FAILURE, m_technicalFailuresDefault,
							subscriber.getLanguage()));
				}
			} else {
				category = getCategory(categoryID, subscriber.getLanguage());
				Clip clip = getClipById(token, subscriber.getLanguage());
				if (clip != null && category != null) {
					task.setObject(CAT_OBJ, category);
					task.setObject(CLIP_OBJ, clip);
					task.setObject(param_catid, category.getCategoryId() + "");
					task.setObject(param_clipid, clip.getClipId() + "");
					contentName = clip.getClipName();
				}
			}
			String response = processSetSelection(task);
			if (!response.equals("success")) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
				return;
			}
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					RETAILER_ACCEPT_RESPONSE_SENDER_SEL,
					m_retRespSMSAcceptDefault, subscriber.getLanguage()));
		} else {
			subscriber = processActivation(task);
			if (!isUserActive(subscriber.getStatus())) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
				return;
			}
		}
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CALLER_ID", context[0].getCallerID());
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));

		HashMap<String, String> hashmap = new HashMap<String, String>();
		hashmap.put("SMS_TEXT", retailerText);
		hashmap.put("CALLER_ID", context[0].getSubscriberID());
		hashmap.put("SONG_NAME", contentName);
		task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
		task.setObject(param_Reciver, context[0].getCallerID());
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_Msg, finalizeSmsText(hashMap));
		sendSMS(task);
		removeViraldata(task);
	}

	@Override
	public void processRetailerRequest(Task task) {
		String[] values = (String[]) task.getObject(param_sms);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String promoID = null;
		Clip clip = null;
		String response = "FAILURE";
		if (values.length >= 2)
			promoID = values[values.length - 2];

		if (promoID != null)
			clip = getClipByPromoId(promoID, subscriber.getLanguage());
		else if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
			task.setObject(param_responseSms, retailerUserAlreadyActiveSMS);
			return;
		}

		if (clip == null && status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
			response = Utility.findNReplaceAll(retailerSelAloneFailureSMS,
					"%S", promoID);
			response = Utility.findNReplaceAll(response, "%C", subscriber
					.getSubscriberID());
			task.setObject(param_responseSms, response);
			return;
		}
		if (clip != null && status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
			if (isExistingSelection(subscriber.getSubscriberID(), clip
					.getClipId())) {
				response = Utility.findNReplaceAll(retailerSelectionExistsSMS,
						"%S", clip.getClipName());
				response = Utility.findNReplaceAll(response, "%C", subscriber
						.getSubscriberID());
				task.setObject(param_responseSms, response);
				return;
			}
		}

		String clipID = (clip != null) ? String.valueOf(clip.getClipId())
				: null;

		ViralData[] viralDataArr = getViraldata(task);
		viralDataArr = reorderViralData(viralDataArr);
		if (viralDataArr != null) {
			String lastReqclipID = viralDataArr[viralDataArr.length - 1]
					.getClipID();
			if (clip == null
					|| (lastReqclipID != null && clipID != null && clipID
							.equals(lastReqclipID))) {
				response = Utility.findNReplaceAll(retailerRequestExistsSMS,
						"%C", subscriber.getSubscriberID());
				task.setObject(param_responseSms, response);
				return;
			}
		}

		String retailerID = task.getString(param_RetailerMSISDN);
		DataRequest dataRequest = new DataRequest(retailerID, subscriber
				.getSubscriberID(), "RETAILER", clipID, new Date(), "SMS");
		ViralData viralData = rbtClient.addViralData(dataRequest);
		if (viralData != null) {
			String subResponse = "FAILURE";
			if (promoID == null) {
				response = retailerSubSMS;
				subResponse = retailerOnlySubSMSToUser;
			} else if (clip == null) {
				response = retailerOnlySubSuccessSMS;
				subResponse = retailerOnlySubSuccessSMSToUser;
			} else if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
				response = retailerOnlySelSMS;
				subResponse = retailerOnlySelSMSToUser;
			} else {
				response = retailerRequestSuccessSMS;
				subResponse = retailerRequestSuccessSMSToUser;
			}
			response = Utility.findNReplaceAll(response, "%C", subscriber
					.getSubscriberID());
			subResponse = Utility.findNReplaceAll(subResponse, "%C", subscriber
					.getSubscriberID());
			if (clip != null) {
				response = Utility.findNReplaceAll(response, "%S", clip
						.getClipName());
				subResponse = Utility.findNReplaceAll(subResponse, "%S", clip
						.getClipName());
			}
			task.setObject(param_responseSms, response);

			logger.info("Sending sms :\"" + subResponse + "\" to subscriber :"
					+ subscriber.getSubscriberID() + " for retailer request.");
			Utility.sendSMS(subscriber.getSubscriberID(), subResponse);
		}
	}

	@Override
	public void processRetailerSearch(Task task) {
		String[] values = (String[]) task.getObject(param_sms);
		String searchString = null;
		String match = "";

		for (int i = 0; i < values.length; i++) {
			searchString = values[i] + " ";
		}

		ArrayList<LuceneClip> results = null;
		if (searchString != null && searchString.trim().length() > 0) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("SUBSCRIBER_ID", task.getString(param_subscriberID));
			// map.put(AbstractLuceneIndexer.SEARCH_ON_SONG_NAME, searchString);
			results = luceneIndexer.searchQuery(map, 0, param(SMS,
					LUCENE_MAX_RESULTS, 15));
		}

		if (results == null || results.size() == 0) {
			match = "no match";
		} else {
			// no of results will always be less than or equals to
			// maxSearchResultsToSend
			for (int i = 0; i < results.size(); i++) {
				LuceneClip clip = results.get(i);
				if (clip != null) {
					String songName = clip.getClipName();
					if (clip.getClipPromoId() != null) {
						match = match + songName + "-" + clip.getClipPromoId()
								+ " ";
					} else {
						match = match + songName + " ";
					}
				}
			}
		}

		if (match.equalsIgnoreCase("no match")) {
			task.setObject(param_responseSms, retailerSearchNoResultsSMS);
		} else {
			task.setObject(param_responseSms, match + retailerSearchSuccess);
		}
	}

	@Override
	public void processRenew(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}
		String token = null;
		Clip cm = null;
		if (smsList != null && smsList.size() > 0) {
			token = smsList.get(0);
			cm = getClipByPromoId(token, subscriber.getLanguage());
			if (cm == null && param(SMS, CHECK_PROMO_MASTER, false))
				cm = rbtCacheManager.getClipFromPromoMaster(token);

			if (cm == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RENEW_INVALID_REQUEST, m_renewInvalidRequestDefault,
						subscriber.getLanguage()));
				return;
			}
		}

		task.setObject(param_SUBID, subscriber.getSubscriberID());

		if (cm != null)
			task.setObject(param_CLIPID, cm.getClipId() + "");

		String response = updateSubscriberSelection(task);

		if (response.equalsIgnoreCase("SUCCESS"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					RENEW_SUCCESS, m_renewSuccessDefault, subscriber
							.getLanguage()));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					RENEW_FAILURE, m_renewFailureDefault, subscriber
							.getLanguage()));
	}

	@Override
	public void processActivationRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		boolean isDirectActRequest = false;
		if (task.containsKey(param_isdirectact))
			isDirectActRequest = task.getString(param_isdirectact)
					.equalsIgnoreCase("true");
		Clip clip = null;
		String clipID = null;
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		if (smsList != null && smsList.size()>0) {
			for (String promocode : smsList) {
				try {
					int promoId = Integer.parseInt(promocode);
					if (promoId >= param(COMMON, MIN_VALUE_PROMO_ID, 0)) {
                          clip = getClipByPromoId(promoId+"");
                          if(clip!=null){
                        	  clipID = clip.getClipId()+"";
                        	  task.setObject(CLIP_OBJ, clip); 
                          }
					}
				} catch (Exception e) {
                      e.printStackTrace();
				}
			}
		}
		if (isDirectActRequest
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
			logger
					.info("processActivationRequest:: DirectActivationRequest & user is already ACTIVE");
			task.setObject(param_response, "ALREADYACTIVE");
			return;
		}

		if (isUserActive(subscriber.getStatus()) && !isDirectActRequest && clip == null) {
			String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(ACTIVATION_FAILURE + "_"+subscriber.getStatus().toUpperCase(),subscriber.getLanguage()); 
			if(smsText == null){
				task.setObject(param_responseSms, getSMSTextForID(task,
						ACTIVATION_FAILURE, m_activationFailureDefault, subscriber
								.getLanguage()));
			}else{
				task.setObject(param_responseSms,smsText);
			}
			return;
		}

		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		if (cosDetail != null) {
			task.setObject(param_COSID, cosDetail.getCosId());
		}
        String subClass = getParamAsString(SMS, "SUB_CLASS_FOR_DT_SERVICE", null);
        if(subClass!=null){
        	task.setObject(param_subclass, subClass);
        }
        
		if (isConfirmationOnWithoutOffer) {
			processForUserConfirmationWithoutOffer(task);
			return;
		}

		boolean isConsentSubscriptionRequest = false;
		
		if ((!isUserActive(subscriber.getStatus()) || isDirectActRequest)) {
			
			if(isConfirmationOn) {
				sendBaseAmoutForUserConfirmation(task);
				return;
			}
			subscriber = processActivation(task);

			if (isDirectActRequest)
				task.setObject(param_response, "SUCCESS");
			
			if (getParamAsBoolean(SMS, "SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED", "FALSE")
					&& subscriber != null && subscriber.isSubConsentInserted()) {
				logger.info("Consent Subscription Request through SMS : "+isConsentSubscriptionRequest);
				isConsentSubscriptionRequest = true;
			}
			
			if (isConsentSubscriptionRequest) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						CONSENT_ACTIVATION_SUCCESS, m_consentActivationSuccessDefault,
						subscriber.getLanguage()));
				
			}else if (!isDirectActRequest
					&& (subscriber != null && isUserActive(subscriber
							.getStatus())))
				task.setObject(param_responseSms, getSMSTextForID(task,
						ACTIVATION_SUCCESS, m_activationSuccessDefault,
						subscriber.getLanguage()));
		}

		if ((subscriber == null || !isUserActive(subscriber.getStatus()))&& !isConsentSubscriptionRequest) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, null));
			return;
		}

		logger
				.info("processActivationRequest::  smsList is zero, isDirectActRequest >"
						+ isDirectActRequest);
		if (isDirectActRequest) {
			task.setObject(param_response, "SUCCESS");

			if (task.getString(param_response).equals(SUCCESS)) {
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver, task
							.getString(param_subscriberID));
					task.setObject(param_Msg, subscriptionClass
							.getSmsOnSubscription());
					sendSMS(task);
				}
			}
		}

	}

	@Override
	public void confirmActNSel(Task task) {
		confirmActNSel(task, null);
	}

	public void confirmActNSel(Task task, ViralData viral) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ViralData latestViralData = null;
		String status = subscriber.getStatus();

		if (viral == null) {
			ViralData[] vst = getViraldata(subscriber.getSubscriberID(), null,
					SMSCONFPENDING);
			if (vst != null && vst.length > 0)
				latestViralData = vst[0];
		} else {
			latestViralData = viral;
		}
		boolean active = isUserActive(status);
		logger.info("user is active " + active);
		String response = null;
		if (latestViralData != null) {
			/*
			 * RBT-4396: When the mode is received as any of the configured modes, the
			 * initial confirmation message has to be different informing user
			 * about this being an affiliate content and the same will be
			 * charged even if this is a music content. Also during the final
			 * confirmation SMS received from user useUIChargeClass is to be
			 * passed as true. 
			 */
			
			Map<String, String> extraInfoMap = latestViralData.getInfoMap();
			logger.info("latestViralData InfoMap contains: " + extraInfoMap);
			if (extraInfoMap != null) {
				if(extraInfoMap.containsKey("CHARGE_CLASS")) {
					task.setObject(param_chargeclass, extraInfoMap
							.get("CHARGE_CLASS"));
					task.setObject(param_USE_UI_CHARGE_CLASS, true);
					logger.info("Updated CHARGE_CLASS and USE_UI_CHARGE_"
							+ "CLASS in the task object");
				}
				
				/*
				 * RBT-4549: SMS Activation of music pack. Checking for the new user and 
				 * de-active user. If the selection offer id and cos id are present then 
				 * updating the same in task.
				 */
				if ((status.equalsIgnoreCase(WebServiceConstants.NEW_USER) || status
						.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
						&& extraInfoMap.containsKey("BASE_OFFERID")) {
					task.setObject(param_offerID, extraInfoMap
							.get("BASE_OFFERID"));
				}
				
				if(extraInfoMap.containsKey(param_alreadyGetSelOffer)){
					task.setObject(param_alreadyGetSelOffer, extraInfoMap.get(param_alreadyGetSelOffer));
				}
				// Requirement for Vodafone Turkey
				if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
						&& extraInfoMap
								.containsKey("UPGRADE_SUBSCRIPTION_CLASS")) {
					task.setObject(param_rentalPack,
							extraInfoMap.get("UPGRADE_SUBSCRIPTION_CLASS"));
					task.setObject(param_upgrade, "true");
				}
				if ((status.equalsIgnoreCase(WebServiceConstants.NEW_USER) || status
						.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
						&& extraInfoMap.containsKey("SUBSCRIPTION_CLASS")) {
					task.setObject(param_subclass,
							extraInfoMap.get("SUBSCRIPTION_CLASS"));
				}
				
				if((status.equalsIgnoreCase(WebServiceConstants.NEW_USER) || status
						.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
						&& extraInfoMap.containsKey("USER_INFO_AMOUNT")) {
					task.setObject(param_actInfo, "AMOUNT:" + extraInfoMap
							.get("USER_INFO_AMOUNT"));
				}
				
				if(extraInfoMap.containsKey(WebServiceConstants.param_packOfferID)) {
					task.setObject(WebServiceConstants.param_packOfferID, extraInfoMap
							.get(WebServiceConstants.param_packOfferID));
				}
				if(extraInfoMap.containsKey(param_COSID)) {
					task.setObject(param_COSID, extraInfoMap
							.get(param_COSID));
				}
			}
			
			String selectedBy = latestViralData.getSelectedBy();
			logger.info("Subscriber selection mode: " + selectedBy);
			if (selectedBy != null)
				task.setObject(param_actby, selectedBy);

			if (latestViralData.getClipID() != null) {
				Clip clip = getClipById(latestViralData.getClipID());
				task.setObject(CLIP_OBJ, clip);
				task.setObject(param_clipid, latestViralData.getClipID());
				String viralDataCallerID = latestViralData.getCallerID();
				task.setObject(param_callerid, viralDataCallerID);

				logger.info("clipid=" + latestViralData.getClipID()
						+ " caller = " + viralDataCallerID
						+ " selected_by = " + selectedBy);
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("CALLER_ID",
						task.getString(param_callerid) == null ? param(SMS,
								SMS_TEXT_FOR_ALL, "all") : task
								.getString(param_callerid));
				
				
				String cosId = task.getString(param_COSID);
				CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
				
				response = processSetSelection(task);
				logger.info("response" + response);
				if (response == null) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							HELP_SMS_TEXT, m_helpDefault, subscriber
									.getLanguage()));
				}else if (response.equalsIgnoreCase("success")) {

					Clip clipLocal = getClipById(latestViralData.getClipID());

					if (clipLocal != null)
						hashMap.put("SONG_NAME", clipLocal.getClipName());

					if (cosDetails != null && (LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType())
							|| SONG_PACK.equalsIgnoreCase(cosDetails.getCosType())
							|| AZAAN.equalsIgnoreCase(cosDetails.getCosType())
							|| UNLIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType())
							|| UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails.getCosType())
							|| LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails.getCosType())))
					{
						if (!active)
							hashMap.put("SMS_TEXT", getSMSTextForID(task,
									MUSIC_PACK_N_BASE_ACTIVATION_SUCCESS,
									m_actMusicPackSuccessTextDefault, subscriber
											.getLanguage()));
						else
							hashMap.put("SMS_TEXT", getSMSTextForID(task,
									MUSIC_PACK_ACTIVATION_SUCCESS, m_actMusicPackSuccessTextDefault,
									subscriber.getLanguage()));
					}
					else
					{
						if (!active)
							hashMap.put("SMS_TEXT", getSMSTextForID(task,
									ACTIVATION_PROMO_SUCCESS,
									m_actPromoSuccessTextDefault, subscriber
									.getLanguage()));
						else
							hashMap.put("SMS_TEXT", getSMSTextForID(task,
									PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
									subscriber.getLanguage()));
					}

					/*
					 * RBT-4539: Get the downloads and selections from the rbt
					 * object . For each download promo id matches with clip
					 * wave file get amount for that particular charge class and
					 * put it into the hash map with key: SEL_AMT.
					 */
					String chargeClassAmount = getChargeClassFromSelections(task, viralDataCallerID, clip);
					hashMap.put("SEL_AMT", chargeClassAmount);
					
					hashMap.put("CIRCLE_ID", subscriber.getCircleID());
					
					if(cosDetails!=null){
						hashMap.put("COS_KEYWORD", cosDetails.getSmsKeyword());
					}
					
					task.setObject(param_responseSms, finalizeSmsText(hashMap));

				}

				else if (isActivationFailureResponse(response)) {
					String smsText = getSMSTextForID(task, HELP_SMS_TEXT,
							m_helpDefault, subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,HELP_SMS_TEXT,
					// m_helpDefault,subscriber.getLanguage()));
				} else if (response.equals(WebServiceConstants.OFFER_NOT_FOUND)) {
					String smsText = getSMSTextForID(task,
							OFFER_NOT_FOUND_TEXT, m_OfferAlreadyUsed,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response
						.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
					String smsText = getSMSTextForID(task,
							SELECTION_SUSPENDED_TEXT, m_SuspendedSelDefault,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,SELECTION_SUSPENDED_TEXT,
					// m_SuspendedSelDefault,subscriber.getLanguage()));
				} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
					String smsText = getSMSTextForID(task,
							SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(
									task, PROMO_ID_SUCCESS,
									m_promoSuccessTextDefault, subscriber
											.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,SELECTION_ALREADY_EXISTS_TEXT,
					// getSMSTextForID(task,PROMO_ID_SUCCESS,
					// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response
						.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
					String smsText = getSMSTextForID(task,
							SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
							getSMSTextForID(task, PROMO_ID_SUCCESS,
									m_promoSuccessTextDefault, subscriber
											.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
					// getSMSTextForID(task,PROMO_ID_SUCCESS,
					// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
					String smsText = getSMSTextForID(task,
							SELECTION_ADRBT_NOTALLOWED_,
							m_ADRBTSelectionFailureDefault, subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,SELECTION_ADRBT_NOTALLOWED_,
					// m_ADRBTSelectionFailureDefault,subscriber.getLanguage()));
				} else if (response
						.equals(WebServiceConstants.SELECTION_OVERLIMIT)) {
					String smsText = getSMSTextForID(task, SELECTION_OVERLIMIT,
							getSMSTextForID(task, PROMO_ID_FAILURE,
									m_promoIDFailureDefault, subscriber
											.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,SELECTION_OVERLIMIT,
					// getSMSTextForID(task,PROMO_ID_FAILURE,
					// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response
						.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT)) {
					String smsText = getSMSTextForID(task,
							PERSONAL_SELECTION_OVERLIMIT, getSMSTextForID(task,
									PROMO_ID_FAILURE, m_promoIDFailureDefault,
									subscriber.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,PERSONAL_SELECTION_OVERLIMIT,
					// getSMSTextForID(task,PROMO_ID_FAILURE,
					// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response
						.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT)) {
					String smsText = getSMSTextForID(task,
							LOOP_SELECTION_OVERLIMIT, getSMSTextForID(task,
									PROMO_ID_FAILURE, m_promoIDFailureDefault,
									subscriber.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,LOOP_SELECTION_OVERLIMIT,
					// getSMSTextForID(task,PROMO_ID_FAILURE,
					// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response
						.equals(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
					String smsText = getSMSTextForID(task,
							REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED, getSMSTextForID(task,
									PROMO_ID_FAILURE, m_promoIDFailureDefault,
									subscriber.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,LOOP_SELECTION_OVERLIMIT,
					// getSMSTextForID(task,PROMO_ID_FAILURE,
					// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
				} else if (response.equals(WebServiceConstants.OVERLIMIT)) {
					String smsText = getSMSTextForID(task, DOWNLOAD_OVERLIMIT,
							getSMSTextForID(task, PROMO_ID_FAILURE,
									m_promoIDFailureDefault, subscriber
											.getLanguage()), subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response
						.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
					String smsText = getSMSTextForID(task,
							LITEUSER_PREMIUM_BLOCKED, liteUserPremiumBlocked,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
					// task.setObject(param_responseSms,
					// getSMSTextForID(task,LITEUSER_PREMIUM_BLOCKED,
					// liteUserPremiumBlocked,subscriber.getLanguage()));
				}else if (response
						.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
					String smsText = getSMSTextForID(task,
							LITEUSER_PREMIUM_NOT_PROCESSED, liteUserPremiumNotProcessed,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response
						.startsWith(WebServiceConstants.SELECTIONS_BLOCKED)) {
                    //response comes when the subscriber is in the configured blocked status
					String smsText = getSMSTextForID(task,
							"SEL_BLOCKED_FOR_"+subscriber.getStatus().toUpperCase(), m_selBlockedForStatus,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response.equals(WebServiceConstants.PACK_DOWNLOAD_LIMIT_REACHED)) {
					String smsText = getSMSTextForID(task,
							PACK_DOWNLOAD_LIMIT_REACHED,
							m_downloadLimitReachedDefault, subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response.equalsIgnoreCase(WebServiceConstants.BASE_OFFER_NOT_AVAILABLE)) {
					String smsText = getSMSTextForID(task,
							BASE_OFFER_NOT_AVAILABLE,
							m_baseOfferNotFound, subscriber
									.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				} else if (response.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
					String smsText = getSMSTextForID(task,
							Corporate_Selection_Not_Allowed,
							m_corpChangeSelectionFailureDefault,
							subscriber.getLanguage());
					smsText = finalSmsText(smsText, task,
							subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);
				}else {
					if (task.containsKey(param_isSuperHitAlbum))
						task.setObject(param_isPromoIDFailure, "TRUE");
					String smsText = isValidPromoId(task, latestViralData.getClipID(), subscriber.getLanguage());
					smsText = finalSmsText(smsText, task, subscriber.getCircleID());
					task.setObject(param_responseSms, smsText);

					// task.setObject(param_responseSms,
					// getSMSTextForID(task,PROMO_ID_FAILURE,
					// m_promoIDFailureDefault,subscriber.getLanguage()));
				}

			} else {
				subscriber = processActivation(task);
				if (subscriber != null && isUserActive(subscriber.getStatus())){
					task.setObject(param_responseSms, getSMSTextForID(task,
							ACTIVATION_SUCCESS, m_activationSuccessDefault,
							subscriber.getLanguage()));
					//To set PACK_NAME and PACK VALID DAYS
					String smsKeyWord = null;
					if(subscriber!=null){
						String cosId = subscriber.getCosID();
					    cosId =	(subscriber.getUserInfoMap()!=null && subscriber.getUserInfoMap().containsKey("COS_ID"))?subscriber.getUserInfoMap().get("COS_ID"):cosId;
						CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
						if(cosDetails!=null){
							smsKeyWord = cosDetails.getSmsKeyword();
							String smsText = task.getString(param_responseSms);
							if(smsText!=null && smsKeyWord!=null){
								task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
							}
						}
					}
				}
				else if (subscriber == null
						|| !isUserActive(subscriber.getStatus())) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							HELP_SMS_TEXT, m_helpDefault, null));
					return;
				}
			}
			sendSMS(task);
			super.removeViraldata(subscriber.getSubscriberID(), null,
					SMSCONFPENDING);
		}else{
			 task.setObject(param_responseSms, getSMSTextForID(task,	 
					 DOUBLE_CONFIRMATION_ENTRY_EXPIRED, m_doubleConfirmationEntryExpiredTextDefault, subscriber	 
                                      .getLanguage()));
		}
	}

	@Override
	public void confirmRequestActNSel(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ViralData[] vst = getViraldata(subscriber.getSubscriberID(), null,
				"REQUESTCONFPENDING");
		boolean active = isUserActive(subscriber.getStatus());
		String response = null;
		if (vst != null && vst.length > 0) {
			task.setObject(param_clipid, vst[0].getClipID());
			task.setObject(param_callerid, vst[0].getCallerID());
			logger.info("clipid=" + vst[0].getClipID() + " caller = "
					+ vst[0].getCallerID());
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("CALLER_ID",
					task.getString(param_callerid) == null ? param(SMS,
							SMS_TEXT_FOR_ALL, "all") : task
							.getString(param_callerid));
			response = processSetSelection(task);
			logger.info("response" + response);
			if (response == null) {
				task
						.setObject(param_responseSms, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()));
			} else if (response.equalsIgnoreCase("success")) {

				Clip clip = getClipById(vst[0].getClipID());

				if (clip != null)
					hashMap.put("SONG_NAME", clip.getClipName());
				if (!active)
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							ACTIVATION_PROMO_SUCCESS,
							m_actPromoSuccessTextDefault, subscriber
									.getLanguage()));
				else
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage()));
				hashMap.put("CIRCLE_ID", subscriber.getCircleID());
				task.setObject(param_responseSms, finalizeSmsText(hashMap));

			}

			else if (isActivationFailureResponse(response)) {
				String smsText = getSMSTextForID(task, HELP_SMS_TEXT,
						m_helpDefault, subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,HELP_SMS_TEXT,
				// m_helpDefault,subscriber.getLanguage()));
			} else if (response.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
				String smsText = getSMSTextForID(task,
						SELECTION_SUSPENDED_TEXT, m_SuspendedSelDefault,
						subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,SELECTION_SUSPENDED_TEXT,
				// m_SuspendedSelDefault,subscriber.getLanguage()));
			} else if (response.equals(WebServiceConstants.OFFER_NOT_FOUND)) {
				String smsText = getSMSTextForID(task, OFFER_NOT_FOUND_TEXT,
						m_OfferAlreadyUsed, subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
			} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
				String smsText = getSMSTextForID(task,
						SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
								PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,SELECTION_ALREADY_EXISTS_TEXT,
				// getSMSTextForID(task,PROMO_ID_SUCCESS,
				// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
				String smsText = getSMSTextForID(task,
						SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
						getSMSTextForID(task, PROMO_ID_SUCCESS,
								m_promoSuccessTextDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
				// getSMSTextForID(task,PROMO_ID_SUCCESS,
				// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
				String smsText = getSMSTextForID(task,
						SELECTION_ADRBT_NOTALLOWED_,
						m_ADRBTSelectionFailureDefault, subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,SELECTION_ADRBT_NOTALLOWED_,
				// m_ADRBTSelectionFailureDefault,subscriber.getLanguage()));
			} else if (response.equals(WebServiceConstants.SELECTION_OVERLIMIT)) {
				String smsText = getSMSTextForID(task, SELECTION_OVERLIMIT,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,SELECTION_OVERLIMIT,
				// getSMSTextForID(task,PROMO_ID_FAILURE,
				// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT)) {
				String smsText = getSMSTextForID(task,
						PERSONAL_SELECTION_OVERLIMIT, getSMSTextForID(task,
								PROMO_ID_FAILURE, m_promoIDFailureDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,PERSONAL_SELECTION_OVERLIMIT,
				// getSMSTextForID(task,PROMO_ID_FAILURE,
				// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT)) {
				String smsText = getSMSTextForID(task,
						LOOP_SELECTION_OVERLIMIT, getSMSTextForID(task,
								PROMO_ID_FAILURE, m_promoIDFailureDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,LOOP_SELECTION_OVERLIMIT,
				// getSMSTextForID(task,PROMO_ID_FAILURE,
				// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
				String smsText = getSMSTextForID(task,
						REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED, getSMSTextForID(task,
								PROMO_ID_FAILURE, m_promoIDFailureDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
			} else if (response.equals(WebServiceConstants.OVERLIMIT)) {
				String smsText = getSMSTextForID(task, DOWNLOAD_OVERLIMIT,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,DOWNLOAD_OVERLIMIT,
				// getSMSTextForID(task,PROMO_ID_FAILURE,
				// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
				String smsText = getSMSTextForID(task,
						LITEUSER_PREMIUM_BLOCKED, liteUserPremiumBlocked,
						subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
				// task.setObject(param_responseSms,
				// getSMSTextForID(task,LITEUSER_PREMIUM_BLOCKED,
				// liteUserPremiumBlocked,subscriber.getLanguage()));
			} else if (response
					.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
				String smsText = getSMSTextForID(task,
						LITEUSER_PREMIUM_NOT_PROCESSED, liteUserPremiumNotProcessed,
						subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
			} else if (response.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
				String smsText = getSMSTextForID(task,
						Corporate_Selection_Not_Allowed,
						m_corpChangeSelectionFailureDefault,
						subscriber.getLanguage());
				smsText = finalSmsText(smsText, task,
						subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);
			} else {
				if (task.containsKey(param_isSuperHitAlbum))
					task.setObject(param_isPromoIDFailure, "TRUE");
				String smsText = isValidPromoId(task, null, subscriber.getLanguage());
				smsText = finalSmsText(smsText, task, subscriber.getCircleID());
				task.setObject(param_responseSms, smsText);

				// task.setObject(param_responseSms,
				// getSMSTextForID(task,PROMO_ID_FAILURE,
				// m_promoIDFailureDefault,subscriber.getLanguage()));
			}
			super.removeViraldata(subscriber.getSubscriberID(), null,
					"REQUESTCONFPENDING");
		} else {
			String smsText = getSMSTextForID(task, PROMO_ID_FAILURE,
					m_promoIDFailureDefault, subscriber.getLanguage());
			task.setObject(param_responseSms, smsText);
		}
	}

	/**
	 * SM URL format:
	 * http://localhost:8080/subscription/RealTimeCharge?msisdn=9845098001
	 * &siteid=1&type=P&srvkey=EVENT&eventkey=GLB_EVENT
	 * &user=mmp&pass=mmp&mode=voice&refid=2&info=songname:tum
	 * mile|code:10&remarks=
	 * 
	 * @param retailer
	 * @param operation
	 * @param subId
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	private String chargeRetailer(Subscriber retailer, String operation,
			String subId) throws HttpException, IOException {
		String SMCheckBalURL = getParameter("RETAILER", "CHARGE_REALTIME_URL");
		SMCheckBalURL = SMCheckBalURL.replace("%msisdn%", retailer
				.getSubscriberID());
		SMCheckBalURL = SMCheckBalURL.replace("%type%",
				retailer.isPrepaid() ? "P" : "B");
		SMCheckBalURL = SMCheckBalURL.replace("%eventkey%", operation);
		SMCheckBalURL = SMCheckBalURL.replace("%info%", subId);
		SMCheckBalURL = SMCheckBalURL.replace("%remarks%", operation);

		// Setting HttpParameters
		HttpParameters httpParam = new HttpParameters();
		httpParam.setUrl(SMCheckBalURL);
		httpParam.setConnectionTimeout(6000);

		// Setting request Params
		HashMap<String, String> params = new HashMap<String, String>();

		logger.info("SMCheckBalURL: " + SMCheckBalURL + ". Parameters: "
				+ params.toString());
		HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParam,
				params);
		String response = httpResponse.getResponse();
		logger.info("Response is " + response);
		if (response.toUpperCase().contains("SUCCESS")) {
			return "SUCCESS";
		}
		if (response.toUpperCase().contains("BAL_LOW")) {
			return "LOWBAL";
		}
		return "ERROR";
	}

	/*
	 * Input 1: RET ACT 9897969812 <clip promo code> Input 2: RET ACT 9897969812
	 * Actual Input 1: ACT 9897969812 <clip promo code> Actual Input 2: ACT
	 * 9897969812 (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processRetailerActnSel
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void processRetailerActnSel(Task task) {

		Subscriber retailer = (Subscriber) task.getObject(param_subscriber);
		Retailer ret = RBTDBManager.getInstance().getRetailer(
				retailer.getSubscriberID(), null);
		if (ret == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_INVALID, m_retFailureTextDefault, retailer
							.getLanguage()));
			return;
		}
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String reatailerActivationKey = getParameter(SMS, "RETAILER_ACT_KEY");
		logger.info("Retailer act keyword: " + reatailerActivationKey
				+ ". SMS list: " + smsList);

		if (smsList.size() < 2) {
			logger.info("Retailer is invalid");
			task.setObject(param_responseSms, getSMSTextForID(task,
					RETAILER_FAILURE_DEFAULT, m_retFailureTextDefault, retailer
							.getLanguage()));
			return;
		}

		logger.info("Retailer is valid");
		if (smsList.get(0).equalsIgnoreCase(reatailerActivationKey)) {
			String retaileeId = smsList.get(1);
			Subscriber retailee = getSubscriber(retaileeId);
			logger.info("Retailer = " + retailer + " retailee = " + retailee);
			if (retailee == null || !retailee.isValidPrefix()) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_INVALID_USER,
						m_retFailureTextInvalidUser, retailer.getLanguage()));
				return;
			}
			String status = retailee.getStatus();
			if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
					|| status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_ALREADY_ACTIVE,
						m_retFailureTextAlreadyActive, retailer.getLanguage()));
				return;
			} else if (status
					.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_DAECT_PENDING,
						m_retFailureTextDeactPending, retailer.getLanguage()));
				return;
			} else if (status
					.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_BLACK_LISTED,
						m_retFailureTextBlackListed, retailer.getLanguage()));
				return;
			} else if (status.equalsIgnoreCase(WebServiceConstants.SUSPENDED)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_SUSPENDED, m_retFailureTextSuspended,
						retailer.getLanguage()));
				return;
			}

			if (!status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
					&& !status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {
				logger.info("Retailee is invalid: " + retaileeId);
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_INVALID_USER,
						m_retFailureTextInvalidUser, retailer.getLanguage()));
				return;
			}
			String feature = getParameter(RETAILER,
					"RETAILER_ACTIVATION_FEATURE");
			Clip clip = null;
			if (smsList.size() > 2 && smsList.get(2) != null) {
				feature = getParameter(RETAILER, "RETAILER_ACT_N_SEL_FEATURE");
				clip = getClipByPromoId(smsList.get(2));
				if (clip == null) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_FAILURE_CLIP_DOES_NOT_EXIST,
							m_retFailureTextClipDoesNotExist, retailer
									.getLanguage()));
					return;
				}
			}
			// Charge the retailer
			try {
				String chargeResponse = chargeRetailer(retailer, feature,
						retaileeId);
				logger.info("Charging response is " + chargeResponse);
				if (chargeResponse.equalsIgnoreCase("LOWBAL")) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_LOW_BALANCE, m_retFailureTextLowBalance,
							retailer.getLanguage()));
					return;
				} else if (chargeResponse.equalsIgnoreCase("ERROR")) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_TECHNICAL_DIFFICULTY,
							m_retFailureTextTechnicalDifficulty, retailer
									.getLanguage()));
					return;
				}
			} catch (Exception e) {
				logger.error("", e);
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_TECHNICAL_DIFFICULTY,
						m_retFailureTextTechnicalDifficulty, retailer
								.getLanguage()));
				return;
			}

			task.setObject(param_actby, "RETAILER");
			task.setObject(param_subclass, getParameter(RETAILER,
					"RETAILER_SUBSCRIPTION_CLASS"));
			task.setObject(param_subscriber, retailee);

			if (clip != null) {
				logger
						.info("Case Subscriber activation and selection is called");
				task.setObject(CLIP_OBJ, clip);
				task.setObject(param_clipid, String.valueOf(clip.getClipId()));
				task.setObject(param_chargeclass, getParameter(RETAILER,
						"RETAILER_CHARGE_CLASS"));
				String allowSelectionInLoop = getParameter(SMS,
						"ALLOW_SELECTION_IN_LOOP");
				if (allowSelectionInLoop != null
						&& allowSelectionInLoop.equalsIgnoreCase("TRUE"))
					task.setObject(param_inLoop, "yes");

				String response = processSetSelection(task);
				logger.info("response " + response);
				if (response.equalsIgnoreCase("success")) {
					// send sms
					HashMap<String, String> hashMap = new HashMap<String, String>();
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							"SUBSCRIBER_ACT_N_SEL_SUCCESSFULLY",
							m_retReqResActnSelSuccessful, retailee
									.getLanguage()));
					hashMap.put("CIRCLE_ID", retailee.getCircleID());
					String smsTextRetailee = finalizeSmsText(hashMap);
					task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
					task.setObject(param_Reciver, retaileeId);
					task.setObject(param_Msg, smsTextRetailee);
					sendSMS(task);
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_ACT_N_SEL_SUCCESS, m_RetailerSuccess,
							retailer.getLanguage()));
					return;
				}
				logger.info("Set Selection failed");
				return;
			}

			logger.info("Case Subscriber activation is called");
			Subscriber sub = processActivation(task);
			logger.info("Response is " + task.getString(param_response));
			if (isUserActive(sub.getStatus())) {
				// send sms
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("SMS_TEXT", getSMSTextForID(task,
						"SUBSCRIBER_ACTIVATED_SUCCESSFULLY",
						m_retReqResActSuccessful, retailee.getLanguage()));
				hashMap.put("CIRCLE_ID", retailee.getCircleID());
				String smsTextRetailee = finalizeSmsText(hashMap);
				task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
				task.setObject(param_Reciver, retaileeId);
				task.setObject(param_Msg, smsTextRetailee);
				sendSMS(task);
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_ACT_SUCCESS, m_RetailerSuccess, retailer
								.getLanguage()));
				return;

			}
			logger.info("User was not successfully activated ");

		} else {
			logger.info("Case Subscriber selection is called");
			// 9897969812 <clip promo code>
			String retaileeId = smsList.get(0);
			Subscriber retailee = getSubscriber(retaileeId);
			if (retailee == null || !retailee.isValidPrefix()) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_INVALID_USER,
						m_retFailureTextInvalidUser, retailer.getLanguage()));
				return;
			}

			if (retailee.getStatus().equalsIgnoreCase(
					WebServiceConstants.DEACT_PENDING)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_DAECT_PENDING,
						m_retFailureTextDeactPending, retailer.getLanguage()));
				return;
			} else if (retailee.getStatus().equalsIgnoreCase(
					WebServiceConstants.BLACK_LISTED)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_BLACK_LISTED,
						m_retFailureTextBlackListed, retailer.getLanguage()));
				return;
			} else if (retailee.getStatus().equalsIgnoreCase(
					WebServiceConstants.SUSPENDED)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_SUSPENDED, m_retFailureTextSuspended,
						retailer.getLanguage()));
				return;
			} else if (!retailee.getStatus().equalsIgnoreCase(
					WebServiceConstants.ACTIVE)) {
				logger.info("Subscriber is not active");
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_INACTIVE_USER,
						m_retFailureTextInactiveUser, retailer.getLanguage()));
				return;
			}

			Clip clip = null;
			if (smsList.get(1) != null) {
				clip = getClipByPromoId(smsList.get(1));
			}

			if (clip == null) {
				logger.info("Clip cannot be found");
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_FAILURE_CLIP_DOES_NOT_EXIST,
						m_retFailureTextClipDoesNotExist, retailer
								.getLanguage()));
				return;
			}

			String feature = getParameter(RETAILER,
					"RETAILER_SELECTION_FEATURE");

			// Charge the retailer
			try {
				String chargeResponse = chargeRetailer(retailer, feature,
						retaileeId);
				if (chargeResponse.equalsIgnoreCase("LOWBAL")) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_LOW_BALANCE, m_retFailureTextLowBalance,
							retailer.getLanguage()));
					return;
				} else if (chargeResponse.equalsIgnoreCase("ERROR")) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							RETAILER_TECHNICAL_DIFFICULTY,
							m_retFailureTextTechnicalDifficulty, retailer
									.getLanguage()));
					return;
				}
			} catch (Exception e) {
				logger.error(e.getStackTrace());
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_TECHNICAL_DIFFICULTY,
						m_retFailureTextTechnicalDifficulty, retailer
								.getLanguage()));
				return;
			}

			task.setObject(CLIP_OBJ, clip);
			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			task.setObject(param_actby, "RETAILER");
			task.setObject(param_chargeclass, getParameter(RETAILER,
					"RETAILER_CHARGE_CLASS"));
			String allowSelectionInLoop = getParameter(SMS,
					"ALLOW_SELECTION_IN_LOOP");
			if (allowSelectionInLoop != null
					&& allowSelectionInLoop.equalsIgnoreCase("TRUE"))
				task.setObject(param_inLoop, "yes");
			task.setObject(param_subscriber, retailee);
			String response = processSetSelection(task);
			if (response.equalsIgnoreCase("success")) {
				// Send SMS
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("SONG_NAME", clip.getClipName());
				hashMap.put("SMS_TEXT", getSMSTextForID(task,
						"SUBSCRIBER_SELECTION_MADE_SUCCESSFULLY",
						m_retReqResSelSuccessful, retailee.getLanguage()));
				hashMap.put("CIRCLE_ID", retailee.getCircleID());
				task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
				hashMap.put("SENDER_NO", param(SMS, SMS_NO, "SMS_NO"));
				String smsTextRetailee = finalizeSmsText(hashMap);				
				task.setObject(param_Reciver, retaileeId);
				task.setObject(param_Msg, smsTextRetailee);
				sendSMS(task);
				logger.info("Response is " + response);
				task.setObject(param_responseSms, getSMSTextForID(task,
						RETAILER_SEL_SUCCESS, m_RetailerSuccess, retailer
								.getLanguage()));
				return;
			}else if(response.startsWith(WebServiceConstants.SELECTIONS_BLOCKED)){
				//Send SMS
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("SMS_TEXT", getSMSTextForID(task,
						"SEL_BLOCKED_FOR_"+retailee.getStatus().toUpperCase(),
						m_selBlockedForStatus, retailee.getLanguage()));
				hashMap.put("CIRCLE_ID", retailee.getCircleID());
				task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
				hashMap.put("SENDER_NO", param(SMS, SMS_NO, "SMS_NO"));
				String smsTextRetailee = finalizeSmsText(hashMap);
				task.setObject(param_Reciver, retaileeId);
				task.setObject(param_Msg, smsTextRetailee);
				sendSMS(task);
				logger.info("Response is " + response);
				task.setObject(param_responseSms, getSMSTextForID(task,
						"SEL_BLOCKED_FOR_"+retailee.getStatus().toUpperCase(),
						m_selBlockedForStatus, retailee.getLanguage()));
				return; 

			}
		}
	}
	
	public void processCancellar(Task task) {
		logger.info("RBT:: manageSongBaseDeact : " + task);
		String response = null;
		try {
			boolean isDirectDctRequest = false;
			if (task.containsKey(param_isdirectdct))
				isDirectDctRequest = task.getString(param_isdirectdct)
						.equalsIgnoreCase("true");

			Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

			if (isDirectDctRequest) {
				if (subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.DEACTIVE)) {
					task.setObject(param_response, "NOTACTIVE");
					return;
				}
			} else if (!isUserActive(subscriber.getStatus())) {
				String deactSmsText = getSMSTextForID(task, "DEACT_NOT_ALLOWED_FOR_"
						+ subscriber.getStatus().toUpperCase(), null, subscriber.getLanguage());
				if (deactSmsText == null) {
					deactSmsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
									   subscriber.getLanguage());
				}
				task.setObject(param_responseSms,deactSmsText);
				return;
			} else if (!param(COMMON, "ALLOW_DEACTIVATION_FOR_GRACE_USERS", true)
					&& subscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.GRACE)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						DEACTIVATION_NOT_ALLOWED_FOR_GRACE_SMS,
						m_deactivationNotAllowedForGraceDefault, subscriber
								.getLanguage()));
				return;
			}
			
			logger.debug("sending the static message for DEACT_BASE_SONG");
			task.setObject(param_responseSms, getSMSTextForID(task,
					SMS_DCT_MANAGE_DEACT_BASE_SONG_SMS, m_smsDctBaseSongText,
					subscriber.getLanguage()));
			response = "success";
			task.setObject(param_response, response);//remove this if not working
			return;
		} catch (Exception e) {
			logger.info("RBT:: manageSongBaseDeact : " + e.getMessage());
		}
		task.setObject(param_response, response);
		return;
	}
	
	@Override
	public void processSongManageDeact(Task task) {
		try {
			Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
			String language = subscriber.getLanguage();
			@SuppressWarnings("unchecked")
			ArrayList<String> smsList = (ArrayList<String>) task
			.getObject(param_smsText);
			if(smsList == null || smsList.size() <= 0){
				logger.debug("in the request specific selection is not there so sending the fresh dowload list");
				listAndSendActiveDownloads(1, task);
				return;
			}
			String selectionString = smsList.get(0).toUpperCase();
			logger.debug("the requested tokens are :" + selectionString);
			
			ViralData[] viralDataArray = getViraldata(subscriber.getSubscriberID(), null, SMS_DCT_SONG_MANAGE);
			if ((viralDataArray == null || viralDataArray.length <= 0) && selectionString.length() > 1) {
				logger.debug("the request session expired as there are no entry in the viral sms table so send the fresh downloads");
				task.setObject(param_responseSms, getSMSTextForID(task,
						DCT_MANAGE_SESSION_EXPIRED, m_smsSessionExpireDefault,
						language));
				return;
			}
			
			String optionPrefix = getParamAsString(param_sms, DCT_SONG_OPTION_PREFIX, null);
			if (viralDataArray != null && viralDataArray.length > 0) {
				if(optionPrefix == null) {
					optionPrefix = "";
				}
				ViralData viralData = viralDataArray[0];
				HashMap<String, String> extraInfoMap = viralData.getInfoMap();
				logger.info("extraInfo of viralData :" + viralData.toString());
				boolean keyFound = extraInfoMap.containsKey(selectionString);
				
				if(keyFound && optionPrefix != null && selectionString.equalsIgnoreCase(optionPrefix+"0")){
					int alphaStartIndex = Integer.valueOf(extraInfoMap.get(selectionString));
					logger.info("the next alphabet for more is :" +extraInfoMap.get(selectionString) +" and index is " + alphaStartIndex);
					task.setObject(param_EXTRAINFO, viralData.getInfoMap());
					listAndSendActiveDownloads(alphaStartIndex, task);
				} else if(keyFound){
					String clipID = extraInfoMap.get(selectionString);
					Clip selectionClip = rbtCacheManager.getClip(clipID,language);
					logger.debug("the selection and is processed selection rbtwavfile :" + extraInfoMap.get(selectionString) + "for selection String :" + selectionString + " selectionClip :" +selectionClip);
					task.setObject(CLIP_OBJ, selectionClip);
					task.setObject(param_promoID, selectionClip.getClipPromoId());
					
					if (param(SMS, CONFIRM_SONG_DEACTIVATION, false)) {
						task.setObject(param_promoID, selectionClip.getClipPromoId());
						processSongDeactivationConfirm(task);
						return;
					}
//					TODO double confirmation
					
					SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
					selectionRequest.setClipID(String.valueOf(selectionClip.getClipId()));
					rbtClient.deleteSubscriberDownload(selectionRequest);
					String response = selectionRequest.getResponse();
					if(response.equalsIgnoreCase("SUCCESS")) {
						removeViraldata(task);
					}
				} else {
					logger.debug("user sent the invalid reuest string:" + selectionString +" for :" + viralData.toString());
					task.setObject(param_responseSms, getSMSTextForID(task,
							INVALID_USER_REQUEST, m_smsRequestFailureDefault,
							language));
					return;
				}
			}
		} catch (Exception e) {
			logger.info("RBT:: processSongManageDeact : " + e.getMessage());
		}
	}
	
	private void listAndSendActiveDownloads(int accessCount, Task task) {
//		TODO handle if the viral table contains the entries.
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String language = subscriber.getLanguage();
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.getSubscriberID());
		rbtDetailsRequest.setStatus("" + status);
		Downloads downloads = RBTClient.getInstance().getDownloads(rbtDetailsRequest);
		if(downloads == null || downloads.getDownloads() == null || downloads.getNoOfActiveDownloads() == 0){
			task.setObject(param_responseSms, getSMSTextForID(task,
					NO_DOWNLOADS, m_downloadsNoSelDefault,
					language));
			return;
		}
		
		Download[] downloadsArray = downloads.getDownloads();
		Map<String, String> indexMap =  (task.getObject(param_EXTRAINFO) != null ? (Map<String, String>) task.getObject(param_EXTRAINFO) : new HashMap<String, String>());
		String smsHeaderText = getSMSTextForID(task, DCT_MANAGE_MSSG_HEADER, null, subscriber.getLanguage());
		String smsFooterText = getSMSTextForID(task, DCT_MANAGE_MSSG_FOOTER, null, subscriber.getLanguage());
		String clipTextFormat = getSMSTextForID(task, DCT_MANAGE_BASE_TEXT, m_dctManageBaseText, subscriber.getLanguage());
		String clipMoreText = getSMSTextForID(task, DCT_MANAGE_MORE_TEXT, m_dctManageMoreText, subscriber.getLanguage());
		int maxClipsAllowed = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_REQUEST_MAX_CLIPS_IN_LIST, "3"));
		int songMaxChar = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_SONG_MAX_CHAR_ALLOWED, "15"));
		int artistMaxChar = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_ARTIST_MAX_CHAR_ALLOWED, "10"));
		String optionPrefix = getParamAsString(param_sms, DCT_SONG_OPTION_PREFIX, null);
		if(optionPrefix == null){
			logger.info("DCT_SONG_OPTION_PREFIX is not configured");
			optionPrefix = "";
		}
		optionPrefix = optionPrefix.toUpperCase();
		String alphabet;
		StringBuilder smsBuilder = new StringBuilder();
		int activeDownloadsCount = 0;
		for (Download download : downloadsArray) {
			if(download != null && download.getDownloadStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE))
				activeDownloadsCount++;
		}
		
		for (int i = accessCount; i <= downloadsArray.length && maxClipsAllowed > 0; i++) {
				Download download = downloadsArray[i-1];//minimum access count value should be 1
				logger.info("download is "+download);
				String downloadStatus = download.getDownloadStatus();
				if(!downloadStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE))
					continue;
				
				String downloadWavFile = download.getRbtFile();
				if(downloadWavFile == null)
					continue;
				if(downloadWavFile.endsWith(".wav"))
					downloadWavFile = downloadWavFile.substring(0,downloadWavFile.length()-4);
				Clip downloadClip = rbtCacheManager.getClipByRbtWavFileName(downloadWavFile,language);
				if(downloadClip == null)
					continue;
				String clipName = downloadClip.getClipName();
				if(clipName.length() > songMaxChar) clipName = clipName.substring(0, songMaxChar);
//				alphabet = alphabets[accessCount++];
				alphabet = optionPrefix + accessCount;
				String clipText = clipTextFormat;
				clipText = clipText.replace("%ALPHABET%", alphabet);
				clipText = clipText.replace("%SONG_NAME%", clipName);
				
				String artistName = downloadClip.getArtist() != null? downloadClip.getArtist() : "";
				if(artistName.length() > artistMaxChar) artistName = artistName.substring(0, artistMaxChar);
				clipText = clipText.replace("%ARTIST_NAME%", artistName);
				String promoID = downloadClip.getClipPromoId() != null? downloadClip.getClipPromoId() : "";
				clipText = clipText.replace("%PROMO_ID%", promoID);
				
				smsBuilder.append(clipText);
				indexMap.put(alphabet, String.valueOf(downloadClip.getClipId()));
				accessCount++;
				maxClipsAllowed--;
			}
		String finalSMSText = "";
		if(smsHeaderText != null) {
			finalSMSText = smsHeaderText;
		}
		
		finalSMSText = finalSMSText + smsBuilder.toString();
		
		if(activeDownloadsCount >= accessCount){
			clipMoreText = clipMoreText.replace("%ALPHABET%", optionPrefix+"0");
			finalSMSText = finalSMSText + clipMoreText;
			indexMap.put(optionPrefix+"0", String.valueOf(accessCount));
		}
		
		if(smsFooterText != null) {
			finalSMSText += smsFooterText;
		}
		
		task.setObject(param_sms_type, SMS_DCT_SONG_MANAGE);
		task.setObject(param_responseSms, finalSMSText);
		task.setObject(param_info, WebServiceConstants.VIRAL_DATA);//TODO it may be needed or not check it
		task.setObject(param_SEARCHCOUNT, String.valueOf(accessCount));
		removeViraldata(subscriber.getSubscriberID(), SMS_DCT_SONG_MANAGE);
		removeViraldata(subscriber.getSubscriberID(), null, SMS_DCT_SONG_CONFIRM);
		logger.info("indexMap size :" + indexMap.size());
		logger.info("extra info after DBUtility.getAttributeXMLFromMap(indexMap) : " + DBUtility.getAttributeXMLFromMap(indexMap));
		task.setObject(param_EXTRAINFO, DBUtility.getAttributeXMLFromMap(indexMap));
		logger.info("the updated viraldata is inserted into viraltable");
		addViraldata(task);
		return;
//		return "success";
	}

	
	public void processMultipleSelection(Task task){
		String callerId = task.getString(param_callerid);
		ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);
		String subscriberID = task.getString(param_subID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		
		if(callerId == null || smsList == null || smsList.size() <= 0){
			task.setObject(param_responseSms, getSMSTextForID(task,
					INVALID_REQUEST, m_invalidRequestDefault, subscriber.getLanguage()));
			return;
		}
		
		Downloads downloads = RBTClient.getInstance().getDownloads(rbtDetailsRequest);
		if(downloads == null){
			task.setObject(param_responseSms, getSMSTextForID(task,
					NO_DOWNLOADS, m_noDownloadsFoundDefault, subscriber.getLanguage()));
			return;
		}
		Download[] downloadsArray = downloads.getDownloads();
		if(downloadsArray == null || downloadsArray.length <= 0){
			task.setObject(param_responseSms, getSMSTextForID(task,
					NO_DOWNLOADS, m_noDownloadsFoundDefault, subscriber.getLanguage()));
			return;
		}

		List<String> downloadsWavFileList = new ArrayList<String>();
        List<String> selectionsWavFileCallerIdsList = new ArrayList<String>();
		for (int j = 0; j < downloadsArray.length; j++) {
			downloadsWavFileList.add(String.valueOf(downloadsArray[j].getToneID()));
		}
		
		Settings settings = RBTClient.getInstance().getSettings(rbtDetailsRequest);
		if (settings != null) {
			Setting[] settingArray = settings.getSettings();
			for (int i = 0; i < settingArray.length; i++) {
				selectionsWavFileCallerIdsList.add(String
						.valueOf(settingArray[i].getCallerID() + ":"
								+ settingArray[i].getToneID()));
			}
		}
		
		String promoIDListString = smsList.get(0);
		
		String[] promoIDArray = promoIDListString.split(",");
		logger.info("promoID count :" + promoIDArray.length);
		
		String finalResponse = FAILURE;
		for (String promoID : promoIDArray) {
			try {
				Clip clip = RBTCacheManager.getInstance().getClipByPromoId(promoID);
				int clipID = clip.getClipId();
				if(!downloadsWavFileList.contains(String.valueOf(clipID))){
					continue;
				}
				String success=null;
				if(selectionsWavFileCallerIdsList.contains(callerId+":"+clipID)){
					SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
					selectionRequest.setPromoID(promoID);
					selectionRequest.setCategoryID("3");
					selectionRequest.setCallerID(callerId);
					selectionRequest.setClipID(String.valueOf(clipID));
					RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);
					success = selectionRequest.getResponse();
					logger.info("delete selection response for promoId :" + promoID +"is " +success);
					selectionRequest.setInLoop(true);
					if(success.equalsIgnoreCase("SUCCESS")) {
						RBTClient.getInstance().addSubscriberSelection(selectionRequest);
						success = selectionRequest.getResponse();
						logger.info("selection response for reactivated selection with promoId :" + promoID +" is " +success);
						if(success.equalsIgnoreCase("SUCCESS")||success.indexOf("success")!=-1){
							finalResponse = SUCCESS;
						}
					}
 
				} else {
					SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
					selectionRequest.setPromoID(promoID);
					selectionRequest.setCategoryID("3");
					selectionRequest.setCallerID(callerId);
					selectionRequest.setClipID(String.valueOf(clipID));
					selectionRequest.setInLoop(true);
					RBTClient.getInstance().addSubscriberSelection(selectionRequest);
					success = selectionRequest.getResponse();
					logger.info("selection response for promoId :" + promoID +"is " +success);
					if(success.equalsIgnoreCase("SUCCESS")||success.indexOf("success")!=-1){
						finalResponse = SUCCESS;
					}
				}
				
			} catch (Exception e) {
				logger.info("Exception while processing Multiple selection based on callerId");
				e.printStackTrace();
			}
			
		}
				
		String smsText = null;
		if (finalResponse.equalsIgnoreCase(SUCCESS)) {
			
			smsText = getSMSTextForID(task,
					MULTI_SELECTION_SUCCESS, m_selectionSuccessDefault, subscriber.getLanguage());
		} else {
			smsText = getSMSTextForID(task,
					MULTI_SELECTION_FAILURE, m_selectionFailureDefault, subscriber.getLanguage());
		}
		
		smsText = finalSmsText(smsText, task, subscriber.getCircleID());
		task.setObject(param_responseSms, smsText);
			
	}

	@Override
	public void processActNSel(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean isActOptional = param(SMS, IS_ACT_OPTIONAL, false);
		boolean isActRequest = task.getString(IS_ACTIVATION_REQUEST)
				.equalsIgnoreCase("true");

		boolean isDirectActRequest = false;
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		
		if (task.containsKey(param_isdirectact))
			isDirectActRequest = task.getString(param_isdirectact)
					.equalsIgnoreCase("true");

		if (isDirectActRequest
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
			logger
					.info("processActNSel:: DirectActivationRequest & user is already ACTIVE");
			task.setObject(param_response, "ALREADYACTIVE");
			return;
		}

		if (isUserActive(subscriber.getStatus()) && isActRequest
				&& !isDirectActRequest) {
			String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(ACTIVATION_FAILURE + "_"+ 
		 			subscriber.getStatus().toUpperCase(),subscriber.getLanguage()); 
			if(smsText == null){
			    task.setObject(param_responseSms, getSMSTextForID(task,
				    	ACTIVATION_FAILURE, m_activationFailureDefault, subscriber
					 		  .getLanguage()));
			}else{
				task.setObject(param_responseSms,smsText);
			}
			return;
		}
		
		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		logger.info("Sms list : " + smsList + " and teh cos detail is : "
				+ cosDetail);
		if (cosDetail != null) {
			if (isUserActive(subscriber.getStatus())) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						ACTIVATION_FAILURE, m_activationFailureDefault,
						subscriber.getLanguage()));
				return;
			}

			task.setObject(param_COSID, cosDetail.getCosId());
		}

		if (!populateFromTimeAndToTime(task)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TIME_OF_DAY_FAILURE, m_timeOfTheDayFailureDefault,
					subscriber.getLanguage()));
			return;
		}
        String subClass = getParamAsString(SMS, "SUB_CLASS_FOR_DT_SERVICE", null);
        if(subClass!=null){
        	task.setObject(param_subclass,subClass);
        }
		String response = null;
		Clip clip = null;

		if (smsList == null || smsList.size() < 1) {
			/*
			 * Added by Sandeep for Buy and Gift Feature( when latest download
			 * is within maximumWaitingTime(configurable) then gift that song to
			 * the msisdn sent as sms_text
			 */
			if (param(SMS, SMS_IS_BUY_AND_GIFT_ALLOWED, false)) {
				logger.info("inside Buy and Gift Feature");
				boolean isBuyNGiftWaitingTimeOver = isBuyNGiftWaitingTimeOver(task);
				if (!isBuyNGiftWaitingTimeOver) {
					buyAndGift(task);
					return;
				}
			}
			task.setObject(param_responseSms, getSMSTextForID(task,
					INVALID_USER_REQUEST, m_smsRequestFailureDefault,
					subscriber.getLanguage()));
			return;
		}

		if (smsList != null && smsList.size() > 0) {
			if (smsList.get(0) != null
					&& !smsList.get(0).equalsIgnoreCase("null")) {
				clip = getProfileClip(task);
				if(clip == null && task.containsKey(param_isDefaultProfileHrsByIndex)){
					task.setObject(param_responseSms, getSMSTextForID(task,
							REQUEST_MORE_NO_SEARCH, m_reqMoreSMSNoSearchDefault,
							subscriber.getLanguage()));
					return;
				}else if (clip != null) {
					processSetTempOverride(task);
					return;
				} 

				if (task.getObject(CLIP_OBJ) == null)
					getCategoryAndClipForPromoID(task, smsList.get(0));

				clip = (Clip) task.getObject(CLIP_OBJ);
				
				if (clip == null
						|| clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					// if CosDetails object present in task object and
					// clipPromoID sent is invalid, then activate the user on
					// the COS
					if (cosDetail != null) {
						if(isConfirmationOn) {							
							sendBaseAmoutForUserConfirmation(task);
							return;
						}
						subscriber = processActivation(task);
						boolean isConsentSubscriptionRequest = false;
						if (getParamAsBoolean(SMS, "SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED", "FALSE")
								&& subscriber != null && subscriber.isSubConsentInserted()) {
							logger.info("Consent Subscription Request through SMS : "+isConsentSubscriptionRequest);
							isConsentSubscriptionRequest = true;
						}
						
						if (isConsentSubscriptionRequest) {
							task.setObject(param_responseSms, getSMSTextForID(task,
									CONSENT_ACTIVATION_SUCCESS, m_consentActivationSuccessDefault,
									subscriber.getLanguage()));
							
						}else if (subscriber != null
								&& isUserActive(subscriber.getStatus())) {
							task.setObject(param_responseSms, getSMSTextForID(
									task, ACTIVATION_SUCCESS,
									m_activationSuccessDefault, subscriber
											.getLanguage()));
						}

						if ((subscriber == null || !isUserActive(subscriber.getStatus()))
								&& !isConsentSubscriptionRequest) {
							String language = null;
							if (subscriber != null)
								language = subscriber.getLanguage();

							if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis())
								task.setObject(
										param_responseSms,
										getSMSTextForID(task, CLIP_EXPIRED_SMS_TEXT,
												getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault, language), language));
							else
								task.setObject(
										param_responseSms,
										getSMSTextForID(task, CLIP_DOES_NOT_EXIST_SMS_TEXT,
												getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault, language), language));

						}
						return;
					}

					if (!isUserActive(subscriber.getStatus())) {
						if (isDirectActRequest) {
							smsList.remove(0);
						}
						else if (isActRequest || isActOptional) {
							if (task.containsKey(param_isSuperHitAlbum))
								task.setObject(param_isPromoIDFailure, "TRUE");

							if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis())
								task.setObject(
										param_responseSms,
										getSMSTextForID(task, CLIP_EXPIRED_SMS_TEXT,
												getSMSTextForID(task, PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber.getLanguage()),
												subscriber.getLanguage()));
							else
								task.setObject(
										param_responseSms,
										getSMSTextForID(task, CLIP_DOES_NOT_EXIST_SMS_TEXT,
												getSMSTextForID(task, PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber.getLanguage()),
												subscriber.getLanguage()));

							return;
						}
					}
				}
			} else {
				smsList.remove(0);
			}
		} else if (cosDetail != null) {
			if(isConfirmationOn) {
				sendBaseAmoutForUserConfirmation(task);
				return;
			} 
			subscriber = processActivation(task);
			boolean isConsentSubscriptionRequest = false;
			if (getParamAsBoolean(SMS, "SENDING_CONSENT_SUBSCRIPTION_MESSAGE_ENABLED", "FALSE")
					&& subscriber != null && subscriber.isSubConsentInserted()) {
				logger.info("Consent Subscription Request through SMS : "+isConsentSubscriptionRequest);
				isConsentSubscriptionRequest = true;
			}
			
			if (isConsentSubscriptionRequest) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						CONSENT_ACTIVATION_SUCCESS, m_consentActivationSuccessDefault,
						subscriber.getLanguage()));
				
			}else if (subscriber != null && isUserActive(subscriber.getStatus())) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						ACTIVATION_SUCCESS, m_activationSuccessDefault,
						subscriber.getLanguage()));
			}

			if ((subscriber == null || !isUserActive(subscriber.getStatus()))
					&& !isConsentSubscriptionRequest) {
				String language = null;
				if (subscriber != null)
					language = subscriber.getLanguage();

				task.setObject(param_responseSms, getSMSTextForID(task,
						HELP_SMS_TEXT, m_helpDefault, language));
			}
			return;
		} else {
			if (task.getString(param_callerid) != null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber
								.getLanguage()));
				return;
			}
		}
		
		clip = (Clip) task.getObject(CLIP_OBJ);
		Clip[] clips = rbtCacheManager.getClipsInCategory(99);
		if(clip!=null){
			boolean clipFound = false;
            for(Clip clp : clips ){
          	   if(clp!=null && clip.getClipId() == clp.getClipId()){
				  task.setObject(param_responseSms,getSMSTextForID(task,CLIP_DOES_NOT_EXIST_SMS_TEXT,
						m_clipNotAvailableDefault,subscriber.getLanguage()));
				  clipFound = true;
				  break;
        	   }
            }
            if(clipFound){
               return;
            }
		}
		/*
		 * if((!isUserActive(subscriber.getStatus()) || isDirectActRequest) &&
		 * (isActRequest || (isActOptional && smsList.size() > 0 ))) {
		 * subscriber = processActivation(task);
		 * 
		 * if(isDirectActRequest) task.setObject(param_response, "SUCCESS");
		 * if(!isDirectActRequest && isUserActive(subscriber.getStatus()))
		 * task.setObject(param_responseSms,
		 * getSMSTextForID(task,ACTIVATION_SUCCESS,
		 * m_activationSuccessDefault)); }
		 * if(!isUserActive(subscriber.getStatus())) {
		 * task.setObject(param_responseSms, getSMSTextForID(task,HELP_SMS_TEXT,
		 * m_helpDefault)); return; }
		 */
		if (smsList != null && smsList.size() > 0) {
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");
 
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("CALLER_ID",
					task.getString(param_callerid) == null ? param(SMS,
							SMS_TEXT_FOR_ALL, "all") : task
							.getString(param_callerid));

			if (isActRequest || isActOptional
					|| isUserActive(subscriber.getStatus())) {
				if(isConfirmationOn) {
					task.setObject("SEL_SMS", "TRUE");
					sendBaseAmoutForUserConfirmation(task);
					return;
				}
				else {
					response = processSetSelection(task);
				}
			}

			setResponseSmsFromSelectionResponse(task, response,
					isDirectActRequest, isActRequest);
		}

	}

	/*
	 * gift song in buy and gift feature
	 */
	protected void buyAndGift(Task task) {

		String songName = null;
		String giftDefClipID = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (task.getObject(CAT_OBJ) != null) {
			task.setObject(param_catid, ((Category) task.getObject(CAT_OBJ))
					.getCategoryId()
					+ "");
			songName = ((Category) task.getObject(CAT_OBJ)).getCategoryName();
		} else if (task.getObject(CLIP_OBJ) != null) {
			task.setObject(param_clipid, ((Clip) task.getObject(CLIP_OBJ))
					.getClipId()
					+ "");
			songName = ((Clip) task.getObject(CLIP_OBJ)).getClipName();
		} else if ((giftDefClipID = RBTParametersUtils.getParamAsString(
				"COMMON", "DEFAULT_CLIP_ID_FOR_GIFT", null)) != null) {
			Clip giftClip = rbtCacheManager.getClip(giftDefClipID);
			if (giftClip != null) {
				task.setObject(param_clipid, String.valueOf(giftClip
						.getClipId()));
				songName = giftClip.getClipName();
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
								.getLanguage()));
				return;
			}
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		}
		String mode = param(SMS, SMS_BUY_AND_GIFT_MODE, null);
		task.setObject(param_mode, mode);
		String response = processGift(task);
		if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SUCCESS)) {
			String smstext = getSMSTextForID(task, GIFT_SUCCESS,
					m_giftSuccessDefault, subscriber.getLanguage());
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smstext);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID",
					task.getString(param_callerid) == null ? "" : task
							.getString(param_callerid));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.INVALID)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.OWN_NUMBER)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NOT_ALLOWED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		} else
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));

	}

	/*
	 * checks whether latest active download set time is older than the
	 * configured waitTime
	 */
	protected boolean isBuyNGiftWaitingTimeOver(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber
				.getSubscriberID());
		Downloads downloads = rbtClient.getDownloads(rbtDetailsRequest);
		logger.debug("downloads :" + downloads);
		Download[] downloadArray = (downloads != null) ? downloads
				.getDownloads() : null;
		logger.debug("downloadArray :" + downloadArray);
		if (downloadArray != null && downloadArray.length > 0) {
			logger.debug("download size is:" + downloadArray.length);
			Download latestDownload = null;
			for (Download download : downloadArray) {
				if (download.getDownloadStatus().equals(
						WebServiceConstants.ACTIVE)
						&& (latestDownload == null || download.getSetTime()
								.after(latestDownload.getSetTime()))) {
					latestDownload = download;
				}
			}
			logger.info("latestDownload :" + latestDownload);

			if (latestDownload == null)
				return false;

			Calendar latestDownloadSetTime = Calendar.getInstance();
			latestDownloadSetTime.setTime(latestDownload.getSetTime());
			long waitingTime = param(SMS, SMS_BUY_AND_GIFT_WAITING_TIME, 0) * 60000;
			long currentTime = Calendar.getInstance().getTimeInMillis();
			long latestDownloadTime = latestDownloadSetTime.getTimeInMillis();
			logger.info("latestDownloadSetTime :" + latestDownloadTime
					+ ", currentTime:" + currentTime + ", waitingTIme:"
					+ waitingTime);
			if (currentTime - latestDownloadTime < waitingTime) {
				int clipId = latestDownload.getToneID();
				Clip clip = rbtCacheManager.getClip(clipId);
				task.setObject(CLIP_OBJ, clip);
				return false;
			}
		}
		return true;
	}

	private String finalSmsText(String sms, Task task, String circleID) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		if (clip != null){
			hashMap.put("SONG_NAME", clip.getClipName());
			hashMap.put("PROMO_ID", clip.getClipPromoId());
		}else
			hashMap.put("SONG_NAME", "");
		hashMap.put("SMS_TEXT", sms);

		if (task.containsKey(param_fromTime)
 			    && task.containsKey(param_fromTimeMins)) {
	         hashMap.put("FROM_TIME", task.getString(param_fromTime) + ":"
			               + task.getString(param_fromTimeMins));
        }

	    if (task.containsKey(param_toTime)
		         && task.containsKey(param_toTimeMins)) {
	         hashMap.put("TO_TIME", task.getString(param_toTime) + ":"
			               + task.getString(param_toTimeMins));
        }

	    if(task.containsKey(param_song_chrg_amt)){
	    	hashMap.put("SEL_AMT", task.getString(param_song_chrg_amt));
	    }

		hashMap.put("CIRCLE_ID", circleID);
		sms = finalizeSmsText(hashMap);
		return sms;

	}

	protected void setResponseSmsFromSelectionResponse(Task task,
			String response, boolean isDirectActRequest, boolean isActRequest) {
		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("CALLER_ID",
				task.getString(param_callerid) == null ? param(SMS,
						SMS_TEXT_FOR_ALL, "all") : task
						.getString(param_callerid));

		if (task.containsKey(param_fromTime)
 			    && task.containsKey(param_fromTimeMins)) {
	         hashMap.put("FROM_TIME", task.getString(param_fromTime) + ":"
			               + task.getString(param_fromTimeMins));
        }

	    if (task.containsKey(param_toTime)
		         && task.containsKey(param_toTimeMins)) {
	         hashMap.put("TO_TIME", task.getString(param_toTime) + ":"
			               + task.getString(param_toTimeMins));
        }

	    if(task.containsKey(param_song_chrg_amt)){
	    	hashMap.put("SEL_AMT", task.getString(param_song_chrg_amt));
	    }


		logger.info("Webservice response is : " + response);
		if (response == null) {
			if (clip != null
					&& clip.getClipEndTime().getTime() < System
							.currentTimeMillis())
				task.setObject(param_responseSms, getSMSTextForID(task,
						CLIP_EXPIRED_SMS_TEXT, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						CLIP_DOES_NOT_EXIST_SMS_TEXT, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage()));

		} else if (response.equalsIgnoreCase("success")) {
			if (!isDirectActRequest) {
				/*
				 * If the response is success, the response message will be
				 * constructed based on the selection i.e clip or shuffle. If
				 * the selection is of clip type, clip name will be set in the
				 * response. If the selection is shuffle type, category name
				 * will be set. The category object exists in the task only if
				 * the selection is shuffle.
				 */
				Category category = (Category) task.getObject(CAT_OBJ);
				if (null != category
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(category.getCategoryTpe())) {
					hashMap.put("SONG_NAME", category.getCategoryName());
				} else {
					clip = (Clip) task.getObject(CLIP_OBJ);
					if (clip != null) {
						hashMap.put("SONG_NAME", clip.getClipName());
						hashMap.put("PROMO_ID", clip.getClipPromoId());
					}
				}
				
				if (task.containsKey(param_isSelConsentInserted)) {
					if (task.containsKey(param_isSubConsentInserted)) { 
						hashMap.put("SMS_TEXT",
								getSMSTextForID(task, CONSENT_ACTIVATED_PROMO_SUCCESS,
										m_consentActivatedPromoSuccessTextDefault,
										subscriber.getLanguage()));
					} else {
						hashMap.put("SMS_TEXT",
								getSMSTextForID(task, CONSENT_PROMO_ID_SUCCESS,
										m_consentPromoIdSuccessTextDefault,
										subscriber.getLanguage()));
					}

				}else if(task.containsKey(param_song_chrg_amt)){
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							ACTIVATED_PROMO_SUCCESS,
							m_activatedPromoSuccessTextDefault, subscriber
									.getLanguage()));
					
				}else if(task.containsKey("isTODSettingRequest")){
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							TOD_SETTING_SUCCESS, m_timeOfDaySettingTextDefault,
							subscriber.getLanguage()));
					
				}else if (isActRequest) {
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							ACTIVATION_PROMO_SUCCESS,
							m_actPromoSuccessTextDefault, subscriber
									.getLanguage()));
				}else {
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage()));
				}

				hashMap.put("CIRCLE_ID", subscriber.getCircleID());
				task.setObject(param_responseSms, finalizeSmsText(hashMap));
			} else {
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver, task
							.getString(param_subscriberID));
					task.setObject(param_Msg, subscriptionClass
							.getSmsOnSubscription());
					sendSMS(task);
				}
			}
		} else if (isActivationFailureResponse(response)) {
			String smsText = getSMSTextForID(task, HELP_SMS_TEXT,
					m_helpDefault, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,HELP_SMS_TEXT,
			// m_helpDefault,subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.OFFER_NOT_FOUND)) {
			String smsText = getSMSTextForID(task, OFFER_NOT_FOUND_TEXT,
					m_OfferAlreadyUsed, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
			String smsText = getSMSTextForID(task, SELECTION_SUSPENDED_TEXT,
					m_SuspendedSelDefault, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_SUSPENDED_TEXT,
			// m_SuspendedSelDefault,subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
			String smsText = getSMSTextForID(task,
					SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage()), subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_ALREADY_EXISTS_TEXT,
			// getSMSTextForID(task,PROMO_ID_SUCCESS,
			// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
			String smsText = null;
			if(task.containsKey("isTODSettingRequest"))
				smsText = getSMSTextForID(task,
					    	TOD_SETTING_SUCCESS, m_timeOfDaySettingTextDefault,
						       subscriber.getLanguage());
			else
			    smsText = getSMSTextForID(task,
					      SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT, getSMSTextForID(
							task, PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage()), subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
			// getSMSTextForID(task,PROMO_ID_SUCCESS,
			// m_promoSuccessTextDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
			String smsText = getSMSTextForID(task, SELECTION_ADRBT_NOTALLOWED_,
					m_ADRBTSelectionFailureDefault, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_ADRBT_NOTALLOWED_,
			// m_ADRBTSelectionFailureDefault,subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task, SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber.getLanguage()),
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,SELECTION_OVERLIMIT,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task,
					PERSONAL_SELECTION_OVERLIMIT, getSMSTextForID(task,
							PROMO_ID_FAILURE, m_promoIDFailureDefault,
							subscriber.getLanguage()), subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,PERSONAL_SELECTION_OVERLIMIT,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task, LOOP_SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber.getLanguage()),
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,LOOP_SELECTION_OVERLIMIT,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED)) {
			String smsText = getSMSTextForID(task, REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber.getLanguage()),
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.OVERLIMIT)) {
			String smsText = getSMSTextForID(task, DOWNLOAD_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber.getLanguage()),
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,DOWNLOAD_OVERLIMIT,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()),subscriber.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
			SelectionRequest selectionRequest= new SelectionRequest(subscriber.getSubscriberID());
			if (clip != null) {
				selectionRequest.setClipID("" + clip.getClipId());
			}
			Category category = (Category) task.getObject(CAT_OBJ);
			if (category != null) {
				selectionRequest.setCategoryID("" + category.getCategoryId());
			}
			ChargeClass chargeClass = rbtClient.getNextChargeClass(selectionRequest);
			if (chargeClass != null) {
				task.setObject(param_song_chrg_amt, chargeClass.getAmount());
			}
			String smsText = getSMSTextForID(task, LITEUSER_PREMIUM_BLOCKED,
					liteUserPremiumBlocked, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,LITEUSER_PREMIUM_BLOCKED,
			// liteUserPremiumBlocked,subscriber.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED)) {
			String smsText = getSMSTextForID(task, LITEUSER_PREMIUM_NOT_PROCESSED,
					liteUserPremiumNotProcessed, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if (response
				.contains(WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED)) {
			String cosContent = response
					.substring(response
							.indexOf(WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED)
							+ WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED
									.length());
			String smsText = getSMSTextForID(task,
					"COS_MISMATCH_CONTENT_BLOCKED_" + cosContent.toUpperCase(),
					getSMSTextForID(task, LITEUSER_PREMIUM_BLOCKED,
							liteUserPremiumBlocked, subscriber.getLanguage()),
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			// task.setObject(param_responseSms,
			// getSMSTextForID(task,LITEUSER_PREMIUM_BLOCKED,
			// liteUserPremiumBlocked,subscriber.getLanguage()));
		}else if(response.startsWith(WebServiceConstants.SELECTIONS_BLOCKED)){
			//response of Selections blocked for the status configured
			String smsText = getSMSTextForID(task, "SEL_BLOCKED_FOR_"+subscriber.getStatus().toUpperCase(),
					m_selBlockedForStatus, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if(response.equals(WebServiceConstants.COSID_BLOCKED_CIRCKET_PROFILE)){
			//response of Selections blocked for the status configured
			String smsText = getSMSTextForID(task, "COSID_BLOCKED_CRICKE_PROFILE",
					m_selBlockedForStatus, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if(response.equals(WebServiceConstants.COSID_BLOCKED_FOR_NEW_USER)){
			//response of Selections blocked for the status configured
			String smsText = getSMSTextForID(task, "COSID_BLOCKED_FOR_NEW_USER",
					m_selBlockedForStatus, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if(response.equals(WebServiceConstants.COSID_BLOCKED_FOR_USER)){
			//response of Selections blocked for the status configured
			String smsText = getSMSTextForID(task, "COSID_BLOCKED_FOR_USER",
					m_selBlockedForStatus, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if(response.equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			String smsText = getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if(response.equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			String smsText = getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if(response.equals(WebServiceConstants.PACK_DOWNLOAD_LIMIT_REACHED)){
			String smsText = getSMSTextForID(task, "PACK_DOWNLOAD_LIMIT_REACHED",
					m_packDownloadLimitReached, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else if (response
				.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)) {
			String smsText = getSMSTextForID(task,
					RBT_CORPORATE_NOTALLOW_SELECTION,
					Corporate_Selection_Not_Allowed, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE)) {
			String smsText = getSMSTextForID(task,
					SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE,
					SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
		}else {
			if (task.containsKey(param_isSuperHitAlbum))
				task.setObject(param_isPromoIDFailure, "TRUE");
			String smsText = getSMSTextForID(task, PROMO_ID_FAILURE,
					m_promoIDFailureDefault, subscriber.getLanguage());

			if (clip != null
					&& clip.getClipEndTime().getTime() < System
							.currentTimeMillis())
				smsText = getSMSTextForID(task, CLIP_EXPIRED_SMS_TEXT,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage());
			else if (clip == null)
				smsText = getSMSTextForID(task, CLIP_DOES_NOT_EXIST_SMS_TEXT,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber
										.getLanguage()), subscriber
								.getLanguage());

			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);

			// task.setObject(param_responseSms,
			// getSMSTextForID(task,PROMO_ID_FAILURE,
			// m_promoIDFailureDefault,subscriber.getLanguage()));
		}
	}

	@Override
	public void processPromotion1(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String smsText = null;
		String transID = task.getString(param_TRANS_ID);
		if (isUserActive(subscriber.getStatus())) {
			smsText = getSMSTextForID(task, PROMOTION1_FAILURE,
					m_promotion1FailureTextDefault, subscriber.getLanguage());
			if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
						.replaceAll("%R", smsText);

			if (transID != null)
				smsText = smsText.replaceAll("%T", transID);
			task.setObject(param_responseSms, smsText);
			return;
		}
		StringTokenizer promoKeywords = new StringTokenizer(param(SMS,
				PROMOTION1, null), ",");
		promoKeywords.nextToken();
		if (promoKeywords.hasMoreTokens())
			task.setObject(param_actby, promoKeywords.nextToken());
		if (promoKeywords.hasMoreTokens()) {
			String classType = promoKeywords.nextToken();
			task.setObject(param_subclass, classType);
			task.setObject(param_chargeclass, classType);
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String response = null;
		if (smsList.size() > 0) {
			getCategoryAndClipForPromoID(task, smsList.get(0));
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");
			response = processSetSelection(task);
			if (isActivationFailureResponse(response)) {
				smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
						subscriber.getLanguage());
				if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
					smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
							.replaceAll("%R", smsText);

				if (transID != null)
					smsText = smsText.replaceAll("%T", transID);
				task.setObject(param_responseSms, smsText);
				return;
			}
		} else {
			subscriber = processActivation(task);
			if (!isUserActive(subscriber.getStatus())) {
				smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
						subscriber.getLanguage());
				if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
					smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
							.replaceAll("%R", smsText);

				if (transID != null)
					smsText = smsText.replaceAll("%T", transID);
				task.setObject(param_responseSms, smsText);
				return;
			}
		}

		smsText = getSMSTextForID(task, PROMOTION1_SUCCESS,
				m_promotion1SuccessTextDefault, subscriber.getLanguage());
		if (response != null && response.equalsIgnoreCase("success")) {
			if (param(SMS, PROMOTION1_VALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_VALID_FORMAT, null).replaceAll(
						"%R", smsText);

			if (transID != null)
				smsText = smsText.replaceAll("%T", transID);
		} else {
			smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
					subscriber.getLanguage());
			if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
						.replaceAll("%R", smsText);

			if (transID != null)
				smsText = smsText.replaceAll("%T", transID);
		}

		task.setObject(param_responseSms, smsText);
	}

	@Override
	public void processPromotion2(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROMOTION2_FAILURE, m_promotion2FailureTextDefault,
					subscriber.getLanguage()));
			return;
		}
		StringTokenizer promoKeywords = new StringTokenizer(param(SMS,
				PROMOTION2, null), ",");
		promoKeywords.nextToken();
		if (promoKeywords.hasMoreTokens())
			task.setObject(param_actby, promoKeywords.nextToken());
		if (promoKeywords.hasMoreTokens()) {
			String classType = promoKeywords.nextToken();
			task.setObject(param_subclass, classType);
			task.setObject(param_chargeclass, classType);
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList.size() > 0) {
			getCategoryAndClipForPromoID(task, smsList.get(0));
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");
			processSetSelection(task);
		} else {
			subscriber = processActivation(task);
			if (!isUserActive(subscriber.getStatus())) {
				task
						.setObject(param_responseSms, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()));
				return;
			}
		}

		String smsText = getSMSTextForID(task, PROMOTION2_SUCCESS,
				m_promotion2SuccessTextDefault, subscriber.getLanguage());
		task.setObject(param_responseSms, smsText);
	}

	@Override
	public void processSongPromotion1(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String smsText = null;
		String transID = task.getString(param_TRANS_ID);
		if (!isUserActive(subscriber.getStatus())) {
			smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
					subscriber.getLanguage());
			if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
						.replaceAll("%R", smsText);
			if (transID != null)
				smsText = smsText.replaceAll("%T", transID);
			task.setObject(param_responseSms, smsText);
			return;
		}
		StringTokenizer promoKeywords = new StringTokenizer(param(SMS,
				SONG_PROMOTION1, null), ",");
		promoKeywords.nextToken();
		if (promoKeywords.hasMoreTokens())
			task.setObject(param_actby, promoKeywords.nextToken());
		if (promoKeywords.hasMoreTokens()) {
			String classType = promoKeywords.nextToken();
			task.setObject(param_subclass, classType);
			task.setObject(param_chargeclass, classType);
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String response = null;
		if (smsList.size() > 0) {
			getCategoryAndClipForPromoID(task, smsList.get(0));
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");
			response = processSetSelection(task);
		}

		smsText = getSMSTextForID(task, ACTIVATION_PROMO_SUCCESS,
				m_actPromoSuccessTextDefault, subscriber.getLanguage());
		if (response != null && response.equalsIgnoreCase("success")) {
			if (param(SMS, PROMOTION1_VALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_VALID_FORMAT, null).replaceAll(
						"%R", smsText);
		} else {
			smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
					subscriber.getLanguage());
			if (param(SMS, PROMOTION1_INVALID_FORMAT, null) != null)
				smsText = param(SMS, PROMOTION1_INVALID_FORMAT, null)
						.replaceAll("%R", smsText);
		}
		if (transID != null)
			smsText = smsText.replaceAll("%T", transID);
		task.setObject(param_responseSms, smsText);

	}

	@Override
	public void processSongPromotion2(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}
		StringTokenizer promoKeywords = new StringTokenizer(param(SMS,
				SONG_PROMOTION2, null), ",");
		promoKeywords.nextToken();
		if (promoKeywords.hasMoreTokens())
			task.setObject(param_actby, promoKeywords.nextToken());
		if (promoKeywords.hasMoreTokens()) {
			String classType = promoKeywords.nextToken();
			task.setObject(param_subclass, classType);
			task.setObject(param_chargeclass, classType);
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList.size() > 0) {
			getCategoryAndClipForPromoID(task, smsList.get(0));
			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");
			processSetSelection(task);
			task.setObject(param_responseSms, getSMSTextForID(task,
					ACTIVATION_PROMO_SUCCESS, m_actPromoSuccessTextDefault,
					subscriber.getLanguage()));
		} else
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));

	}

	@Override
	public void processCricket(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		task.setObject(param_status, "90");
		Setting[] settings = getActiveSubSettings(subscriber.getSubscriberID(),
				90);
		if (smsList.size() > 0 && smsList.contains("off")) {
			if (!isCricketFeedSubscriber(settings)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						FEED1_OFF_FAILURE, m_feed1OffFailureTextDefault,
						subscriber.getLanguage()));
				return;
			}
			processDeactivateSelection(task);
			task.setObject(param_responseSms, getSMSTextForID(task,
					FEED1_OFF_SUCCESS, m_feed1OffSuccessTextDefault, subscriber
							.getLanguage()));
			task.setObject(param_responseObdMark, "SUCCESS");
			return;
		}

		if (isCricketFeedSubscriber(settings)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					FEED1_ON_FAILURE, m_feed1OnFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		smsList.remove("on");
		if (param(SMS, CRICKET_PASS, false)) {
			if (smsList.size() > 0)
				task.setObject(param_cricket_pack, smsList.get(0));
			else
				task.setObject(param_cricket_pack, "DP");
		}
		boolean isActOptional = param(SMS, IS_ACT_OPTIONAL, false);
		boolean isActRequest = false;
		if (task.getString(IS_ACTIVATION_REQUEST) != null)
			isActRequest = task.getString(IS_ACTIVATION_REQUEST)
					.equalsIgnoreCase("true");
		if (!isUserActive(subscriber.getStatus()) && !isActRequest
				&& !isActOptional) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		String response = processSetSelection(task);
		if (isActivationFailureResponse(response))
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
		else if (response == null || !response.equalsIgnoreCase("success"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					FEED1_ON_FAILURE, m_feed1OnFailureTextDefault, subscriber
							.getLanguage()));
		else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					FEED1_ON_SUCCESS, m_feed1OnSuccessTextDefault, subscriber
							.getLanguage()));
			task.setObject(param_responseObdMark, "SUCCESS");
		}
	}

	@Override
	public void processReferral(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		boolean isClipReferral = false;
		boolean isPackReferral = false;
		Clip clip = null;
		SubscriptionClass subClass = null;
		// Need to get pack name or promoid
		if (smsList != null && smsList.size() > 0) {
			if (smsList.get(0) != null
					&& !smsList.get(0).equalsIgnoreCase("null")) {
				getCategoryAndClipForPromoID(task, smsList.get(0));

				clip = (Clip) task.getObject(CLIP_OBJ);
				logger.info("The clip is : " + clip);
				// Does any subscriber check need to be made ?
				if (clip != null
						&& clip.getClipEndTime().getTime() > System
								.currentTimeMillis())
					isClipReferral = true;

				if (!isClipReferral) {
					List<SubscriptionClass> subclassList = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getAllSubscriptionClasses();
					if (subclassList != null && subclassList.size() > 0) {
						logger.info("Sub Class list = " + subclassList);
						for (int i = 0; i < subclassList.size(); i++) {

							if (smsList.get(0).equalsIgnoreCase(
									subclassList.get(i).getOperatorCode4())) {
								isPackReferral = true;
								subClass = subclassList.get(i);
							}
						}
					}
				}

				if (!isClipReferral && !isPackReferral) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							"REFER_INVALID_INPUT", m_referDefaultFailure,
							subscriber.getLanguage()));
					return;
				}
			}
		}
		logger.info("Subclass is " + subClass + " clip is " + clip);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber
				.getSubscriberID());
		selectionRequest.setMode("SMS");
		if (subClass != null)
			selectionRequest.setSubscriptionClass(subClass.getOperatorCode4());
		if (clip != null)
			selectionRequest.setPromoID(clip.getClipPromoId());
		logger.info("Calling webservice refer ");
		rbtClient.referUser(selectionRequest);
		String response = selectionRequest.getResponse();
		logger.info("Webservice refer response is " + response);
		if (response != null && response.equalsIgnoreCase(SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					REFER_SUCCESS, m_referDefaultSuccess, subscriber
							.getLanguage()));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					REFER_FAILURE, m_referDefaultFailure, subscriber
							.getLanguage()));
	}

	@Override
	public void processNewFeed(Task task) {

	}

	@Override
	public void processSel1(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		String promoCode = smsList.get(0);
		if (promoCode.toLowerCase().startsWith("tp"))
			promoCode = getPromoCodeForTopListCode(task, promoCode.substring(2));
		if (promoCode == null) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
        boolean allowDirectSel = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
						"ALLOW_DIRECT_SEL_FOR_NEW_AND_DEACT_USER","FALSE");
        
		if (!isUserActive(subscriber.getStatus()) && !allowDirectSel) {
			task.setObject(param_SMSTYPE, "SELECTION");
			removeViraldata(task);

			HashMap<String, String> infoMap = new HashMap<String, String>();
			if (task.containsKey(param_isSuperHitAlbum))
				infoMap.put(param_isSuperHitAlbum, "TRUE");

			addViraldata(subscriberID, task.getString(param_callerid),
					"SELECTION", promoCode, "SMS", 0, infoMap);
			task.setObject(param_responseSms, getSMSTextForID(task,
					SEL_KEYWORD1_SUCCESS, m_selKeyword1SuccessTextDefault,
					subscriber.getLanguage()));
			return;
		}
		Setting[] settings = getActiveSubSettings(subscriberID, 1);
		for (int i = 0; settings != null && settings.length > 0
				&& i < settings.length; i++) {
			if (settings[i]
					.getToneType()
					.equalsIgnoreCase(
							com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.CATEGORY_SHUFFLE)) {
				removeViraldata(task);
				addViraldata(subscriberID, task.getString(param_callerid),
						"SELECTION", promoCode, "SMS", 0, null);
				task.setObject(param_responseSms, getSMSTextForID(task,
						OVERRIDE_SHUFFLE_SELECTION,
						m_overrideShuffleSelTextDefault, subscriber
								.getLanguage()));
				return;
			}
		}
		processActNSel(task);
	}

	@Override
	public void processSel2(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		subscriber.getSubscriberID();
		task.setObject(param_SMSTYPE, "SELECTION");
		ViralData[] viralData = getViraldata(task);
		if (viralData == null || viralData.length <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					SEL_KEYWORD2_FAILURE, m_selKeyword2FailureTextDefault,
					subscriber.getLanguage()));
			return;
		}
		task.setObject(IS_ACTIVATION_REQUEST, "TRUE");
		String callerid = viralData[0].getCallerID();
		if (callerid != null && callerid.trim().length() == 0)
			callerid = null;
		task.setObject(param_callerid, callerid);
		HashMap<String, String> infoMap = viralData[0].getInfoMap();
		if (infoMap != null && infoMap.containsKey(param_isSuperHitAlbum))
			task.setObject(param_isSuperHitAlbum, "TRUE");
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		smsList.add(viralData[0].getClipID());
		processActNSel(task);

	}

	@Override
	public void processCopy(Task task) {

	}

	@Override
	public void processWeekToMonthConversion(Task task) {

	}
	
    @Override
	public void processDelayDeactivation(Task task){
		//It upgrades the subscriber on the service key and extends the end date as 2037.
		logger.info("RBT:: processDeactivation : " + task);
        task.setObject(param_isDelayDeactForUpgrade, true);    	
        //processDeactivation(task);
        String subscriberID = task.getString(param_subscriberID);
        Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
        String serviceKey = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"SERVICE_KEY_FOR_UPGRADE_ON_DELAY_DEACT",null);
        String response = ERROR;
        HashMap<String, String> subExtraInfoMap = null;
        if(subscriber!=null){
              subExtraInfoMap = subscriber.getUserInfoMap();
        }
        
        String smsText = null;
		if (serviceKey != null && subExtraInfoMap != null
				&& subExtraInfoMap.containsKey("UNSUB_DELAY")) {
			String oldSrvKey = subscriber.getSubscriptionClass();
			String mode = task.getString(param_mode);
			if(mode == null){
				mode = "SMS";
			}
			String strActBy = mode;
            
			String oldActBy = subscriber.getActivatedBy();
		    subExtraInfoMap.remove("UNSUB_DELAY");
			subExtraInfoMap.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, oldActBy);
			String xtraInfoXml = DBUtility.getAttributeXMLFromMap(subExtraInfoMap);

			boolean upgrade = RBTDBManager.getInstance().convertSubscriptionTypeAndEndDate(
					subscriberID, oldSrvKey, serviceKey, null,
					xtraInfoXml,null);
			if (upgrade) {
				response = "UPGRADE_SUCCESS_ON_DELAY_DCT";
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(
								Constants.UPGRADE_ON_DELAY_DCT + "_"
										+ subscriber.getCircleID(),
								subscriber.getLanguage());
				if (smsText == null) {
					smsText = CacheManagerUtil.getSmsTextCacheManager()
							.getSmsText(Constants.UPGRADE_ON_DELAY_DCT,
									subscriber.getLanguage());
					
				}
			}
			else {
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(
								Constants.UPGRADE_ON_DELAY_DCT + "_"
										+ subscriber.getCircleID() + "ERROR",
								subscriber.getLanguage());
				if (smsText == null) {
					smsText = CacheManagerUtil.getSmsTextCacheManager()
							.getSmsText(Constants.UPGRADE_ON_DELAY_DCT + "ERROR",
									subscriber.getLanguage());
				}
				
			}
		}
		else{
			// TO be send error sms
			smsText = CacheManagerUtil.getSmsTextCacheManager()
					.getSmsText(
							Constants.UPGRADE_ON_DELAY_DCT + "_"
									+ subscriber.getCircleID() + "_FAILURE",
							subscriber.getLanguage());
			if (smsText == null) {
				smsText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(Constants.UPGRADE_ON_DELAY_DCT + "_FAILURE",
								subscriber.getLanguage());
				
			}
		}
		task.setObject(param_responseSms,smsText);
		

	}
    
	@Override
	public String processDeactivation(Task task) {
		logger.info("RBT:: processDeactivation : " + task);
		if (isThisFeature(task,
				SmsKeywordsStore.deactivateProfileKeywordsSet,
				TEMPORARY_OVERRIDE_CANCEL_MESSAGE))
		{
			logger.info("Redirecting the request to profile deactivation block");
			processRemoveTempOverride(task);
			return null;
		}
        
		if (isThisFeature(task,
				SmsKeywordsStore.unrandomizeKeywordSet,
				UNRANDOMIZE_KEYWORD))
		{
			logger.info("Redirecting the request to profile deactivation block");
			disableRandomization(task);
			return null;
		}

		boolean isDirectDctRequest = false;
		if (task.containsKey(param_isdirectdct))
			isDirectDctRequest = task.getString(param_isdirectdct)
					.equalsIgnoreCase("true");

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (isDirectDctRequest) {
			if (subscriber.getStatus().equalsIgnoreCase(
					WebServiceConstants.DEACTIVE)) {
				task.setObject(param_response, "NOTACTIVE");
				return null;
			}
		} else if (!isUserActive(subscriber.getStatus())) {
			String deactSmsText = getSMSTextForID(task, "DEACT_NOT_ALLOWED_FOR_"
					+ subscriber.getStatus().toUpperCase(), null, subscriber.getLanguage());
			if (deactSmsText == null) {
				deactSmsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault,
								   subscriber.getLanguage());
			}
			task.setObject(param_responseSms,deactSmsText);
			return null;
		} else if (!param(COMMON, "ALLOW_DEACTIVATION_FOR_GRACE_USERS", true)
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.GRACE)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DEACTIVATION_NOT_ALLOWED_FOR_GRACE_SMS,
					m_deactivationNotAllowedForGraceDefault, subscriber
							.getLanguage()));
			return null;
		}
		
		if (subscriber.getUserInfoMap() != null && subscriber.getUserInfoMap().get("DELAY_DEACT") != null
				&& subscriber.getUserInfoMap().get("DELAY_DEACT").equalsIgnoreCase("true")) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DEACTIVATION_NOT_ALLOWED_FOR_DELAYED_DEACT_SMS,
					m_deactivationNotAllowedForDelayedDeactDefault, subscriber
							.getLanguage()));
			return null;
		}
		
		if (param(SMS, CONFIRM_DEACTIVATION, false)) {
			processDeactivationConfirm(task);
			return null;
		}
		
		String response = super.processDeactivation(task);
		logger.info("Response fr deactivation :" + response);
		String smsKeyWord = null;
		if(subscriber!=null){
			CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID());
			if(cosDetails!=null)
				smsKeyWord = cosDetails.getSmsKeyword();
		}
		
		if (response.equals(WebServiceConstants.SUCCESS)) {
			if (isDirectDctRequest) {
				task.setObject(param_response, "SUCCESS");
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver, task
							.getString(param_subscriberID));
					task.setObject(param_Msg, subscriptionClass
							.getSmsAlertBeforeRenewal());
					sendSMS(task);
				}

			} else{
				String smsText =  getSMSTextForID(task,DEACTIVATION_SUCCESS, m_deactivationSuccessDefault,subscriber.getLanguage());
				task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
			}
		}else if(response.equalsIgnoreCase("UPGRADE_SUCCESS")){
			String smsText = getSMSTextForID(task,UPGRADE_ON_DELAY_DCT, m_upgradeSuccessOnDelayDct,subscriber.getLanguage());
			task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
		}
		else {
			if (isDirectDctRequest){
				 task.setObject(param_response, "DEACTIVATION_FAILURE");
			}else{
				 String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(DEACTIVATION_FAILURE + "_"+ 
			 			subscriber.getStatus().toUpperCase(),subscriber.getLanguage()); 
                if(smsText == null){
				    task.setObject(param_responseSms, getSMSTextForID(task,
					    	DEACTIVATION_FAILURE, m_deactivationFailureDefault,
						    subscriber.getLanguage()));
                }else{
                	task.setObject(param_responseSms,smsText);
                }
			}
		}
		logger.info("modified task objectc :" + task);
		return null;
	}

	@Override
	public void processScratchCard(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		task.setObject(param_scratchCardNo, smsList.get(0));

		subscriber = processActivation(task);
		String response = task.getString(param_response);
		if (response.equalsIgnoreCase("success"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					SCRATCH_ACT_SUCCESS, m_activationSuccessDefault, subscriber
							.getLanguage()));
		else if (response.equalsIgnoreCase("invalid"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					SCRATCH_ACT_INVALID_PIN, m_scratchCardActFailureDefault,
					subscriber.getLanguage()));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					SCRATCH_ACT_FAILURE, m_scratchCardActInvalidPinDefault,
					subscriber.getLanguage()));
	}

	@Override
	public void processGiftCopy(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerId = task.getString(param_callerid);
		ValidateNumberRequest validateNumberRequest = new ValidateNumberRequest(
				subscriber.getSubscriberID(), callerId);
		rbtClient.validateGifteeNumber(validateNumberRequest);
		String response = validateNumberRequest.getResponse();

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerId);
		Subscriber giftee = rbtClient.getSubscriber(rbtDetailsRequest);
		if (!giftee.isCanAllow()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFTCOPY_CAN_NOT_GIFT, m_giftCanNotBeGiftedDefault,
					subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.VALID)
				|| response.equals(WebServiceConstants.GIFTER_NOT_ACT)) {
			DataRequest dataRequest = new DataRequest(subscriber
					.getSubscriberID(), "GIFTCOPY_PENDING", 0);
			ViralData[] viralDataArr = rbtClient.getViralData(dataRequest);
			if (viralDataArr == null || viralDataArr.length <= 0) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						GIFTCOPY_NO_REQUESTS, m_giftCopyNoRequestsDefault,
						subscriber.getLanguage()));
			} else {
				Setting[] setting = getActiveSubSettings(callerId, 1);
				logger.info("setting " + setting);
				viralDataArr = reorderViralData(viralDataArr);
				ViralData lastRequest = viralDataArr[viralDataArr.length - 1];
				if (isOverlap(setting, null, lastRequest.getClipID(),
						subscriber.getLanguage()))
					task.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_OVERLAP, null, subscriber.getLanguage()));
				else if (isSelectionSuspended(setting))
					task.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_SUSPENDED_GIFTEE, null, subscriber
									.getLanguage()));
				else if (selectionLimitExceeded(setting))
					task.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_SELECTION_LIMIT_EXCEEDED, null, subscriber
									.getLanguage()));
				else if (giftee.getStatus().equalsIgnoreCase(
						WebServiceConstants.DEACT_PENDING))
					task.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_GIFTEE_DEACT_PENDING, null, subscriber
									.getLanguage()));
				else {
					dataRequest = new DataRequest(subscriber.getSubscriberID(),
							null, "GIFTCOPY_PENDING",
							lastRequest.getSentTime(), "GIFT", "GIFT");
					dataRequest.setNewCallerID(callerId);
					dataRequest.setInfoMap(lastRequest.getInfoMap());
					rbtClient.updateViralData(dataRequest);
					task.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_RECEIVED_CALLERID,
							m_giftCopyReceivedCallerIdDefault, subscriber
									.getLanguage()));
					if (viralDataArr.length > 1) {
						for (int i = 0; i < viralDataArr.length - 1; i++) {
							dataRequest = new DataRequest(subscriber
									.getSubscriberID(), null,
									"GIFTCOPY_PENDING", viralDataArr[i]
											.getSentTime(), "GIFT",
									"GIFTCOPY_FAILED");
							rbtClient.removeViralData(dataRequest);
						}
					}
				}
			}
		} else if (response.equals(WebServiceConstants.INVALID)) {
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							GIFTCOPY_INVALID_CALLERID,
							m_giftCopyInvalidCallerIdDefault, subscriber
									.getLanguage()));
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFTCOPY_CAN_NOT_GIFT, m_giftCanNotBeGiftedDefault,
					subscriber.getLanguage()));
		}

	}

	// Added for Lock Feature
	@Override
	public void processLockRequest(Task task) {
		logger.info("inside processLockRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
				subscriber.getSubscriberID());

		if (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.LOCKED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					ALREADY_LOCKED, alreadyLockDefault, subscriber
							.getLanguage()));
			return;
		}

		try {
			updateDetailsRequest.setIsUserLocked(true);

			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						LOCK_SUCCESS, lockSuccessDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						LOCK_FAILURE, lockFailureDefault, subscriber
								.getLanguage()));
		} catch (Exception e) {
			logger.error("exception in processing the lock request >"
					+ e.getMessage());
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							LOCK_FAILURE, lockFailureDefault, subscriber
									.getLanguage()));
		}
	}

	@Override
	public void processUnlockRequest(Task task) {
		logger.info("inside processUnlockRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
				subscriber.getSubscriberID());

		if (!subscriber.getStatus()
				.equalsIgnoreCase(WebServiceConstants.LOCKED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					UNLOCK_FAILURE, unlockFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		try {
			updateDetailsRequest.setIsUserLocked(false);

			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						UNLOCK_SUCCESS, unlockSuccessDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						UNLOCK_FAILURE, unlockFailureDefault, subscriber
								.getLanguage()));
		} catch (Exception e) {
			logger.error("exception in processing the unlock request >"
					+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					UNLOCK_FAILURE, unlockFailureDefault, subscriber
							.getLanguage()));
		}
	}

	// Added for voluntary suspension
	@Override
	public void processSuspensionRequest(Task task) {
		logger.info("inside processSuspensionRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UtilsRequest utilsRequest = new UtilsRequest(subscriber
				.getSubscriberID(), true, SMS);
		try {
			RBTClient.getInstance().suspension(utilsRequest);

			if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SUSPENSION_SUCCESS, suspensionSuccessDefault,
						subscriber.getLanguage()));
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.ALREADY_VOLUNTARILY_SUSPENDED))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SUSPENSION_ALREADY_VOL_SUS,
						suspensionAlreadySusDefault, subscriber.getLanguage()));
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUSPENSION_NOT_ALLOWED))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SUSPENSION_FAILURE, suspensionFailureDefault,
						subscriber.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						SUSPENSION_FAILURE, suspensionFailureDefault,
						subscriber.getLanguage()));

		} catch (Exception e) {
			logger.error("exception in processing the suspension request >"
					+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					SUSPENSION_FAILURE, suspensionFailureDefault, subscriber
							.getLanguage()));
		}
		
		//To set PACK_NAME and PACK VALID DAYS
		String smsKeyWord = null;
		if(subscriber!=null){
			CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID());
			if(cosDetails!=null){
				smsKeyWord = cosDetails.getSmsKeyword();
				String smsText = task.getString(param_responseSms);
				if(smsText!=null && smsKeyWord!=null){
					task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
				}
			}
		}
		

	}

	@Override
	public void processResumptionRequest(Task task) {
		logger.info("inside processResumptionRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UtilsRequest utilsRequest = new UtilsRequest(subscriber
				.getSubscriberID(), false, SMS);
		try {
			RBTClient.getInstance().suspension(utilsRequest);

			if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						RESUMPTION_SUCCESS, resumptionSuccessDefault,
						subscriber.getLanguage()));
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.NOT_VOLUNTARILY_SUSPENDED))
				task.setObject(param_responseSms, getSMSTextForID(task,
						RESUMPTION_NOT_VOL_SUS,
						resumptionNotVolSuspendedDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						RESUMPTION_FAILURE, resumptionFailureDefault,
						subscriber.getLanguage()));

		} catch (Exception e) {
			logger.error("exception in processing the resumption request >"
					+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					RESUMPTION_FAILURE, resumptionFailureDefault, subscriber
							.getLanguage()));
		}
		
		//To set PACK_NAME and PACK VALID DAYS
		String smsKeyWord = null;
		if(subscriber!=null){
			CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID());
			if(cosDetails!=null){
				smsKeyWord = cosDetails.getSmsKeyword();
				String smsText = task.getString(param_responseSms);
				if(smsText!=null && smsKeyWord!=null){
					task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
				}
			}
		}
	}

	// Added for Block feature
	@Override
	public void processBlockRequest(Task task) {
		logger.info("inside processResumptionRequest");
		String successMsg = null;
		String failureMsg = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		try {
			String[] callerIDs = null;
			@SuppressWarnings("unchecked")
			ArrayList<String> smsKeyWords = (ArrayList<String>) task
					.getObject(param_smsText);
			if (smsKeyWords.size() == 0) {
				callerIDs = new String[1];
				callerIDs[0] = task.getString(param_callerid);
			} else
				callerIDs = smsKeyWords.get(0).split(",");

			if (callerIDs != null) {
				for (String callerID : callerIDs) {
					if (!isPhoneNumber(callerID)) {
						successMsg = BLOCK_FAILURE;
						continue;
					}
					if (subscriber.getSubscriberID().equalsIgnoreCase(callerID)) {
						failureMsg = BLOCK_FAILURE;
						continue;
					}

					GroupRequest groupRequest = new GroupRequest(subscriber
							.getSubscriberID(), "Blocked Callers", "99",
							callerID, null);
					RBTClient.getInstance().addGroupMember(groupRequest);

					if (groupRequest.getResponse().equals(
							WebServiceConstants.SUCCESS))
						successMsg = BLOCK_SUCCESS;
					else if (groupRequest.getResponse().equals(
							WebServiceConstants.OVERLIMIT)) {
						failureMsg = BLOCK_MAX_LIMIT;
						break;
					} else if (groupRequest.getResponse().equals(
							WebServiceConstants.ALREADY_BLOCKED))
						failureMsg = BLOCK_ALREADY_MEMBER;
					else if (groupRequest.getResponse().equals(
							WebServiceConstants.ALREADY_MEMBER_OF_GROUP))
						failureMsg = BLOCK_FAILURE;
					else
						failureMsg = BLOCK_FAILURE;
				}
			}

			if (callerIDs == null || callerIDs.length == 0)
				task.setObject(param_responseSms, getSMSTextForID(task,
						BLOCK_FAILURE, blockFailureDefault, subscriber
								.getLanguage()));
			else if (successMsg != null
					&& successMsg.equalsIgnoreCase(BLOCK_SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						BLOCK_SUCCESS, blockSuccessDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						failureMsg, blockFailureDefault, subscriber
								.getLanguage()));

		} catch (Exception e) {
			logger
					.info("processBlockRequest :: Exception in processing block request >"
							+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					BLOCK_FAILURE, blockFailureDefault, subscriber
							.getLanguage()));
			return;
		}
	}

	@Override
	public void processUnblockRequest(Task task) {
		logger.info("inside processUnBlockRequest");
		String successMsg = null;
		String failureMsg = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		try {
			GroupRequest groupRequest = new GroupRequest(subscriber
					.getSubscriberID(), null, "99");
			Group group = RBTClient.getInstance().getGroup(groupRequest);
			if (group == null) {
				logger
						.info("processUnBlockRequest :: Predefined group for UNBLOCK is not defined");
				task.setObject(param_responseSms, getSMSTextForID(task,
						UNBLOCK_FAILURE, unblockFailureDefault, subscriber
								.getLanguage()));
				return;
			}
			String[] callerIDs = null;
			@SuppressWarnings("unchecked")
			ArrayList<String> smsKeyWords = (ArrayList<String>) task
					.getObject(param_smsText);
			if (smsKeyWords.size() == 0) {
				callerIDs = new String[1];
				callerIDs[0] = task.getString(param_callerid);
			} else
				callerIDs = smsKeyWords.get(0).split(",");

			if (callerIDs != null) {
				for (String callerID : callerIDs) {
					if (!isPhoneNumber(callerID)) {
						successMsg = UNBLOCK_FAILURE;
						continue;
					}

					groupRequest = new GroupRequest(subscriber
							.getSubscriberID(), group.getGroupID(), callerID,
							null);
					RBTClient.getInstance().removeGroupMember(groupRequest);

					if (groupRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.SUCCESS))
						successMsg = UNBLOCK_SUCCESS;
					else if (groupRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.FAILED)
							|| groupRequest.getResponse().equalsIgnoreCase(
									WebServiceConstants.ERROR))
						failureMsg = UNBLOCK_FAILURE;
					else
						failureMsg = UNBLOCK_FAILURE;
				}
			}

			if (callerIDs == null || callerIDs.length == 0)
				task.setObject(param_responseSms, getSMSTextForID(task,
						UNBLOCK_FAILURE, unblockFailureDefault, subscriber
								.getLanguage()));
			else if (successMsg != null
					&& successMsg.equalsIgnoreCase(UNBLOCK_SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						UNBLOCK_SUCCESS, unblockSuccessDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						failureMsg, unblockFailureDefault, subscriber
								.getLanguage()));

		} catch (Exception e) {
			logger
					.info("processUnBlockRequest :: Exception in processing unblock request >"
							+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					UNBLOCK_FAILURE, unblockFailureDefault, subscriber
							.getLanguage()));
			return;
		}
	}

	@Override
	public void processSpecialSongPackRequest(Task task) {

		logger.info("inside processSongPackRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsKeyWords = (ArrayList<String>) task
				.getObject(param_smsText);

		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		if (cosDetail == null) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		logger.info("The sms keywords are " + smsKeyWords
				+ " and the list size is " + smsKeyWords.size());
		try {
			SelectionRequest selectionRequest = new SelectionRequest(subscriber
					.getSubscriberID());
			selectionRequest.setIsPrepaid(subscriber.isPrepaid());
			selectionRequest.setCosID(Integer.valueOf(cosDetail.getCosId()));
			selectionRequest.setMode(SMS);
			if (smsKeyWords.size() > 1) {
				smsKeyWords.remove(0);
				if (subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
					String allowUpgrade = getParameter(SMS,
							"ALLOW_SONG_PACK_UPGRADE");
					if (allowUpgrade != null
							&& allowUpgrade.equalsIgnoreCase("TRUE")) {
						logger.info("upgrade subscriber is allowed");
						task.setObject(param_rentalPack, cosDetail
								.getSubscriptionClass());

					} else {
						task.setObject(param_responseSms, getSMSTextForID(task,
								SPECIAL_SONGPACK_NOT_ALLOWED,
								specialSongPackNotAllowedDefault, subscriber
										.getLanguage()));
						return;
					}
				}
				String isCorporate = getParameter(SMS,
						"ALLOW_SONG_PACK_FOR_CORPORATE");
				if (isCorporate != null && isCorporate.equalsIgnoreCase("TRUE")) {
					task.setObject(param_selectionType, "2");
				}

				task.setObject(param_COSID, cosDetail.getCosId());
				task
						.setObject(param_subclass, cosDetail
								.getSubscriptionClass());
				task.setObject(param_smsText, smsKeyWords);

				logger.info("Calling act n sel");
				processActNSel(task);
				logger.info("Done");
				return;

			}
			RBTClient.getInstance().upgradeSpecialSelectionPack(
					selectionRequest);

			if (selectionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms,
						getSMSTextForID(task, SPECIAL_SONGPACK_SUCCESS,
								specialSongPackSuccessDefault, subscriber
										.getLanguage()));
			else if (selectionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.ACT_PENDING))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SPECIAL_SONGPACK_ACT_PENDING,
						specialSongPackActPendingDefault, subscriber
								.getLanguage()));
			else if (selectionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.ALREADY_ACTIVE))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SPECIAL_SONGPACK_ALREADY_ACTIVE,
						specialSongPackAlreadyActiveDefault, subscriber
								.getLanguage()));
			else if (selectionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.COS_NOT_EXISTS))
				task
						.setObject(param_responseSms, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()));
			else if (selectionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.NOT_ALLOWED))
				task.setObject(param_responseSms, getSMSTextForID(task,
						SPECIAL_SONGPACK_NOT_ALLOWED,
						specialSongPackNotAllowedDefault, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms,
						getSMSTextForID(task, SPECIAL_SONGPACK_FAILURE,
								specialSongPackFailureDefault, subscriber
										.getLanguage()));

		} catch (Exception e) {
			logger
					.error("Exception in processing specialsongpack request >",
							e);
			task.setObject(param_responseSms, getSMSTextForID(task,
					SPECIAL_SONGPACK_FAILURE, specialSongPackFailureDefault,
					subscriber.getLanguage()));
		}

	}

	@Override
	public void processSongPackRequest(Task task) {
		logger.info("inside processSongPackRequest");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		CosDetails cosDetail = (CosDetails) task.getObject(param_cos);
		if (cosDetail == null) {
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		try {
			SelectionRequest selectionRequest = new SelectionRequest(subscriber
					.getSubscriberID());
			selectionRequest.setIsPrepaid(subscriber.isPrepaid());
			selectionRequest.setCosID(Integer.valueOf(cosDetail.getCosId()));
			selectionRequest.setMode(SMS);

			RBTClient.getInstance().upgradeSelectionPack(selectionRequest);

			if (SONG_PACK.equalsIgnoreCase(cosDetail.getCosType())) {
				if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.SUCCESS))
					task.setObject(param_responseSms, getSMSTextForID(task,
							SONGPACK_SUCCESS, songPackSuccessDefault,
							subscriber.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.ACT_PENDING))
					task.setObject(param_responseSms, getSMSTextForID(task,
							SONGPACK_ACT_PENDING, songPackActPendingDefault,
							subscriber.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.ALREADY_ACTIVE))
					task.setObject(param_responseSms, getSMSTextForID(task,
							SONGPACK_ALREADY_ACTIVE,
							songPackAlreadyActiveDefault, subscriber
									.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.COS_NOT_EXISTS))
					task.setObject(param_responseSms, getSMSTextForID(task,
							HELP_SMS_TEXT, m_helpDefault, subscriber
									.getLanguage()));
				else
					task.setObject(param_responseSms, getSMSTextForID(task,
							SONGPACK_FAILURE, songPackFailureDefault,
							subscriber.getLanguage()));
			} else {
				if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.SUCCESS))
					task.setObject(param_responseSms, getSMSTextForID(task,
							UNLIMITED_DOWNLOAD_SUCCESS, songPackSuccessDefault,
							subscriber.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.ACT_PENDING))
					task.setObject(param_responseSms,
							getSMSTextForID(task,
									UNLIMITED_DOWNLOAD_ACT_PENDING,
									songPackActPendingDefault, subscriber
											.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.ALREADY_ACTIVE))
					task.setObject(param_responseSms, getSMSTextForID(task,
							UNLIMITED_DOWNLOAD_ALREADY_ACTIVE,
							songPackAlreadyActiveDefault, subscriber
									.getLanguage()));
				else if (selectionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.COS_NOT_EXISTS))
					task.setObject(param_responseSms, getSMSTextForID(task,
							HELP_SMS_TEXT, m_helpDefault, subscriber
									.getLanguage()));
				else
					task.setObject(param_responseSms, getSMSTextForID(task,
							UNLIMITED_DOWNLOAD_FAILURE, songPackFailureDefault,
							subscriber.getLanguage()));
			}
		} catch (Exception e) {
			logger.error("Exception in processing songpack request >"
					+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					SONGPACK_FAILURE, songPackFailureDefault, subscriber
							.getLanguage()));
		}
	}

	@Override
	public void processMeriDhun(Task task) {
		logger.info("inside processMeriDhun");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsKeyWords = (ArrayList<String>) task
				.getObject(param_smsText);

		if (smsKeyWords == null || smsKeyWords.size() != 2) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MERI_DHUN_FAILURE_TEXT, m_meriDhunFailureDefault,
					subscriber.getLanguage()));
			return;
		}

		Clip clip = null;
		if (smsKeyWords.size() > 0) {
			getCategoryAndClipForPromoID(task, smsKeyWords.get(0));
			clip = (Clip) task.getObject(CLIP_OBJ);
		}

		if (clip == null
				|| clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					MERI_DHUN_FAILURE_TEXT, m_meriDhunFailureDefault,
					subscriber.getLanguage()));
			return;
		}

		task.setObject(param_smsText, smsKeyWords.get(1));
		processMeriDhunRequest(task);
		String response = task.getString(param_response);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("CALLER_ID",
				task.getString(param_callerid) == null ? param(SMS,
						SMS_TEXT_FOR_ALL, "all") : task
						.getString(param_callerid));
		hashMap.put("SONG_NAME", clip.getClipName());
		hashMap.put("REPLACE_TEXT", smsKeyWords.get(1));
		if (response == null || response.equals(CLIP_NOT_AVAILABLE)
				|| response.equals(FAILURE))
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					MERI_DHUN_FAILURE_TEXT, m_meriDhunFailureDefault,
					subscriber.getLanguage()));
		else if (response.equals(SUCCESS))
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					MERI_DHUN_SUCCESS_TEXT, m_meriDhunSuccessDefault,
					subscriber.getLanguage()));
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	public HashMap<String, String> getSearchOnMap() {
		HashMap<String, String> searchOnMap = new HashMap<String, String>();
		String searchOnMapStr = getSMSParameter(REQUEST_SEARCH_ON_MAP);
		if (searchOnMapStr == null || searchOnMapStr.length() == 0) {
			searchOnMap.put("song", "song");
			searchOnMap.put("movie", "movie");
			searchOnMap.put("singer", "singer");
		} else {
			StringTokenizer stkParent = new StringTokenizer(searchOnMapStr, ";");
			while (stkParent.hasMoreTokens()) {
				StringTokenizer stkSingleElement = new StringTokenizer(
						stkParent.nextToken(), ":");
				String searchOn = stkSingleElement.nextToken();
				if (stkSingleElement.hasMoreTokens()) {
					StringTokenizer searchOnKeywords = new StringTokenizer(
							stkSingleElement.nextToken(), ",");
					while (searchOnKeywords.hasMoreTokens())
						searchOnMap.put(searchOnKeywords.nextToken(), searchOn);
				}
			}
		}
		return searchOnMap;
	}
	
	public void processSongDeactivationConfirm(Task task) {
		logger.debug("RBT:: processSongDeactivationConfirm : " + task);
		task.setObject(param_mode, "CCC");
		Subscriber subscriber = getSubscriber(task);
		String language = subscriber.getLanguage();
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.getSubscriberID());
		rbtDetailsRequest.setMode("CCC");
		Downloads downloads = RBTClient.getInstance().getDownloads(rbtDetailsRequest);
		if(downloads == null || downloads.getDownloads() == null || downloads.getNoOfActiveDownloads() == 0){
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}
		
		Download[] downloadArray = downloads.getDownloads();
		List<Download> downloadList = Arrays.asList(downloadArray);
		
		Clip clip = null;
		task.setObject(param_SMSTYPE, SMS_DCT_SONG_CONFIRM);
		ViralData[] pendingDeactRequests = getViraldata(task);
		if(pendingDeactRequests != null && pendingDeactRequests.length > 0){
			ViralData data = pendingDeactRequests[0];
			String clipID = data.getClipID();
			clip = rbtCacheManager.getClip(clipID,language);
		}
		
		clip = task.containsKey(CLIP_OBJ) ? (Clip) task.getObject(CLIP_OBJ) : clip;
		Download selectedDownload = null;
		for (Download download : downloadList) {
			if(download.getRbtFile().contains(clip.getClipRbtWavFile())){
				selectedDownload = download;
				break;
			}
		}
		
		if(selectedDownload == null){
			logger.info("selected download is not found in the downloads list");
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}
		
		Date nextBillingDate = selectedDownload.getNextBillingDate();
		if (nextBillingDate == null) {
			logger.info("nextChargingDate is null for the selected clip with clipid:" + selectedDownload.getToneID());
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}

		long daysLeft = 0;
		if (nextBillingDate != null) {
			daysLeft = (nextBillingDate.getTime() - System.currentTimeMillis())
			/ (1000 * 24 * 60 * 60);
		}

		long daysAllowed = param(COMMON, "ALLOW_DEACTIVATION_BEFORE_N_DAYS", 0);
		if ((nextBillingDate != null && nextBillingDate.before(new Date()))
				|| (daysLeft < daysAllowed) || (pendingDeactRequests != null
						&& pendingDeactRequests.length > 0)) {
			task.setObject(CLIP_OBJ, clip);
			processSongDeactivationConfirmed(task);
			removeViraldata(subscriber.getSubscriberID(), null, SMS_DCT_SONG_CONFIRM);
		} else {
				HashMap<String, String> hashmap = new HashMap<String, String>();
				String daysKeyword = getSMSTextForID(task, "DAYS", "days", language);
				String hoursKeyword = getSMSTextForID(task, "HOURS", "hours",
						language);
				if (daysLeft <= 0) {
					long hoursLeft = (nextBillingDate.getTime() - System
							.currentTimeMillis())
							/ (1000 * 60 * 60);
					hashmap.put("DAYS_LEFT", hoursLeft + " " + hoursKeyword);
				} else
					hashmap.put("DAYS_LEFT", daysLeft + " " + daysKeyword);

				hashmap.put("DEACT_CONFIRM_DAYS", ""
						+ param(SMS, SONG_DEACTIVATION_CONFIRM_CLEAR_DAYS, 5));

				String smsText = getSMSTextForID(task, SMS_DCT_MANAGE_SONG_DEACTIVATION_CONFIRM_SMS,
						m_smsSongDeactConfirmTextDefault, language);

				hashmap.put("SMS_TEXT", smsText);
				hashmap.put("CIRCLE_ID", subscriber.getCircleID());
				task.setObject(param_responseSms, finalizeSmsText(hashmap));
				if (pendingDeactRequests != null
						&& pendingDeactRequests.length > 0) {
					removeViraldata(subscriber.getSubscriberID(), null, SMS_DCT_SONG_CONFIRM);
				}
				
				task.setObject(param_sms_type, SMS_DCT_SONG_CONFIRM);
				task.setObject(param_info, WebServiceConstants.VIRAL_DATA);//TODO it may be needed or not check it
				task.setObject(param_CLIPID, String.valueOf(clip.getClipId()));
				task.setObject(param_subscriberID, subscriber.getSubscriberID());
				logger.info("extraInfo of viralData clipId:" +clip.getClipId());
				removeViraldata(subscriber.getSubscriberID(), SMS_DCT_SONG_CONFIRM);
				addViraldata(task);
		}
	}

	public void processDeactivationConfirm(Task task) {
		logger.debug("RBT:: processDeactivationConfirm : " + task);
		task.setObject(param_mode, "CCC");
		Subscriber subscriber = getSubscriber(task);
		String language = subscriber.getLanguage();
		String subscriberID = subscriber.getSubscriberID();

		String graceSmsText = getSMSTextForID(task, "GRACE_DEACT_CONFIRM_SMS", null, language);
		boolean isActPendingDeactSupported = false;
		if (graceSmsText != null
				&& (subscriber.getStatus().equals(WebServiceConstants.ACT_PENDING)
						|| subscriber.getStatus().equals(WebServiceConstants.GRACE)))
		{
			isActPendingDeactSupported = true;
		}

		Date nextBillingDate = subscriber.getNextBillingDate();
		if (nextBillingDate == null && !isActPendingDeactSupported) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, language));
			return;
		}

		String smsType = "CAN";
		Parameters subClassParams = CacheManagerUtil
				.getParametersCacheManager().getParameter(COMMON,
						"SUBSCRIPTION_CLASS_FOR_CHURN_OFFER");
		if (subClassParams != null) {
			ViralBlackListTable viralBlackList = RBTDBManager.getInstance()
					.getViralBlackList(subscriberID, "CHURN_OFFER");
			if (viralBlackList == null)
				smsType = "CANCEL_OFFER";
		}

		boolean isDeactSupported = RBTParametersUtils.getParamAsBoolean(SMS,
				"IS_DEACTIVATION_CONFIRMATION_ALLOWED_WITH_DEACT_KEYWORD",
				"TRUE");

		task.setObject(param_SMSTYPE, smsType);
		ViralData[] pendingDeactRequests = getViraldata(task);

		int duration = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DCT_WAIT_TIME_FOR_BASE_DCT_CONFIRM", -1);
		
		if (duration >= 0 && pendingDeactRequests != null
				&& pendingDeactRequests.length > 0) {
			ViralData data = pendingDeactRequests[0];
			Date sysDate = new Date(System.currentTimeMillis());
			if (data != null && data.getSentTime() != null) {
				long sentTimeAfterDuration = data.getSentTime().getTime() + (duration * 60 * 1000L);
				if (sysDate.after(new Date(sentTimeAfterDuration))){
					logger.info("sentTimeAfterDuration :"+ new Date(sentTimeAfterDuration) + " sysDate :"+ sysDate);
					removeViraldata(subscriberID, null, smsType);
					processDeactivation(task);
					return;
				}
			}
		}
		
		long daysLeft = 0;
		if (nextBillingDate != null) {
			daysLeft = (nextBillingDate.getTime() - System.currentTimeMillis())
			/ (1000 * 24 * 60 * 60);
		}

		boolean isTraiDeactConfig = getParamAsBoolean(
				iRBTConstant.COMMON, "TRAI_UNSUB_DELAY_DEACT", "FALSE");
		
		long daysAllowed = param(COMMON, "ALLOW_DEACTIVATION_BEFORE_N_DAYS", 0);
		if ((nextBillingDate != null && nextBillingDate.before(new Date()) && !isActPendingDeactSupported)
				|| (daysLeft < daysAllowed && !isActPendingDeactSupported)
				|| (pendingDeactRequests != null
						&& pendingDeactRequests.length > 0 && isDeactSupported)) {
			processDeactivationConfirmed(task, smsType);
		} else {
			
			if (nextBillingDate == null || nextBillingDate.before(new Date()))
			{
				if (!isDeactSupported && pendingDeactRequests != null
						&& pendingDeactRequests.length > 0) {
					removeViraldata(subscriberID, null, smsType);
				}
				
				if (!isTraiDeactConfig) {
					addViraldata(subscriberID, null, smsType, null, "SMS", 0, null);
				}
				task.setObject(param_responseSms, graceSmsText);
			}
			else
			{
				HashMap<String, String> hashmap = new HashMap<String, String>();
				String daysKeyword = getSMSTextForID(task, "DAYS", "days", language);
				String hoursKeyword = getSMSTextForID(task, "HOURS", "hours",
						language);
				if (daysLeft <= 0) {
					long hoursLeft = (nextBillingDate.getTime() - System
							.currentTimeMillis())
							/ (1000 * 60 * 60);
					hashmap.put("DAYS_LEFT", hoursLeft + " " + hoursKeyword);
				} else
					hashmap.put("DAYS_LEFT", daysLeft + " " + daysKeyword);

				hashmap.put("DEACT_CONFIRM_DAYS", ""
						+ param(SMS, DEACTIVATION_CONFIRM_CLEAR_DAYS, 5));
				
				if(duration >= 0) hashmap.put("DURATION", ""+ duration);

				String smsText = getSMSTextForID(task, DEACTIVATION_CONFIRM,
						m_deactivationConfirmTextDefault, language);
				if (smsType.equals("CANCEL_OFFER"))
					smsText = getSMSTextForID(task,
							DEACTIVATION_CONFIRM_CHURN_OFFER,
							m_deactivationChurnOfferTextDefault, language);

				hashmap.put("SMS_TEXT", smsText);
				if (!isDeactSupported && pendingDeactRequests != null
						&& pendingDeactRequests.length > 0) {
					removeViraldata(subscriberID, null, smsType);
				}

				String confDelayTime = getParamAsString(iRBTConstant.SMS,"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",null);
				if(confDelayTime != null) {
					long  endDate = System.currentTimeMillis();
					try{
		            	endDate += Double.parseDouble(confDelayTime)*60*1000L;
		            }catch(NumberFormatException ex){
		            	logger.info("Error in Parsing the Configured time for UNSUB_DELAY ON DEACTIVATION");
		            }
					
					HashMap<String, String> userInfoMap = subscriber.getUserInfoMap();
					if(userInfoMap == null) {
						userInfoMap = new HashMap<String, String>();
					}
					userInfoMap.put("UNSUB_DELAY","SMS");
					
		            Date date = new Date(endDate);
		            SubscriptionRequest request = new SubscriptionRequest(subscriberID);
		            request.setSubscriberEndDate(date);
		            request.setUserInfoMap(userInfoMap);

		            rbtClient.updateSubscription(request);
				}
				else {
					if(!isTraiDeactConfig){
					    addViraldata(subscriberID, null, smsType, null, "SMS", 0, null);
					}
				}
				hashmap.put("CIRCLE_ID", subscriber.getCircleID());
				task.setObject(param_responseSms, finalizeSmsText(hashmap));
			}
		}
	}
	
	public void processSongDeactivationConfirmed(Task task) {
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		String language = subscriber.getLanguage();

		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
		selectionRequest.setClipID(String.valueOf(clip.getClipId()));
		
		task.setObject(param_clipid, String.valueOf(clip.getClipId()));
		String response = super.deleteSubscriberDownload(task);
		if (response.equals(WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					SMS_DCT_MANAGE_SONG_DEACT_SUCCESS, m_smsSongDeactSuccessDefault,
					language));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					SMS_DCT_MANAGE_SONG_DEACT_FAILURE, m_smsSongDeactFailureDefault,
					language));

	}

	public void processDeactivationConfirmed(Task task, String smsType) {
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		String language = subscriber.getLanguage();

		String response = super.processDeactivation(task);
		if (response.equals(WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					DEACTIVATION_SUCCESS, m_deactivationSuccessDefault,
					language));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					DEACTIVATION_FAILURE, m_deactivationFailureDefault,
					language));

		removeViraldata(subscriberID, null, smsType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processDirectActivationRequest(Task task) {
		logger.info("RBT:: processDirectActivationRequest  >" + task);
		String subscriberID = task.getString(param_SUBID);
		String info = task.getString("INFO");
		String action = task.getString("ACTION");
		String mode = task.getString("MODE");
		String cosId = task.getString("COSID");
		String classType = task.getString(param_SUBSCRIPTION_CLASS);
		String refId = task.getString("REFID");

		String response = "INVALID_SMS";

		String smsTextParam = null;
		if (action.equals("ACT"))
			smsTextParam = "actsmstext_:";
		if (action.equals("DCT"))
			smsTextParam = "dctsmstext_:";

		try {
			com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(classType);
			if (subscriptionClass == null) {
				task.setObject(param_response, "INVALID_SRVKEY");
				return;
			}

			if (info != null && smsTextParam != null
					&& info.toLowerCase().indexOf(smsTextParam) != -1) {
				String categoryID = null;
				info = info.toLowerCase();
				String temp = info;
				String tmp = info.substring(info.indexOf(smsTextParam) + 12);

				int pIndex = tmp.indexOf("|");
				if (pIndex == -1)
					info = tmp;
				else
					info = tmp.substring(0, pIndex);
				if (temp.indexOf("category_id_:") != -1) {
					tmp = temp.substring(temp.indexOf("category_id_:") + 13);
					pIndex = tmp.indexOf("|");
					if (pIndex == -1)
						categoryID = tmp;
					else
						categoryID = tmp.substring(0, pIndex);
				}

				String actBy = "SMS";
				if (mode != null)
					actBy = mode;

				logger.info("RBT:: processDirectActivationRequest  >" + info);

				task.setObject(param_info, info);
				task.setObject(param_actby, actBy);
				task.setObject(param_subID, subscriberID);
				if (categoryID != null)
					task.setObject(param_catid, categoryID);

				if (isActSMS(task)) {
					String actInfo = task.getString(param_ipAddress) + ":"
							+ SMS;
					response = "ACTIVATION_FAILED";
					if (info != null) {
						StringTokenizer message = new StringTokenizer(info, " ");
						info = " ";
						while (message.hasMoreTokens()) {
							String token = message.nextToken();
							String smsPromoPrefix = getParameter("SMS",
									"SMS_ACT_PROMO_PREFIX");
							if (smsPromoPrefix != null
									&& token.toLowerCase().startsWith(
											smsPromoPrefix)) {
								smsPromoPrefix = smsPromoPrefix.toLowerCase();
								actInfo = "RET:" + token.trim();
							} else
								info = info.trim() + " " + token.trim();
						}
					}
					logger.info("RBT:: processDirectActivationRequest  >>"
							+ info);

					ArrayList<String> smsList = tokenizeArrayList(task
							.getString(param_info), " ");

					task.setObject(param_smsText, smsList);
					task.setObject(param_isdirectact, "true");
					task.setObject(param_cosid, cosId);
					task.setObject(param_actInfo, actInfo);
					task.setObject(param_subclass, classType);
					task.setObject(param_subscriberID, subscriberID);
					task.setObject(param_refID, refId);
					Subscriber subscriber = getSubscriber(task);
					if (subscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.ACTIVE)) {
						logger
								.info("processDirectActivationRequest:: user is already ACTIVE");
						task.setObject(param_response,
								"INVALID|CALLBACK ALREADY RECEIVED|"
										+ subscriber.getRefID());
						return;
					}
					// JIRAID-515:Segmented price issue fix. If cosID is in the
					// configured cosID list, deactivating act_pending
					// selections before activating
					if (subscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.ACT_PENDING)) {
						String cosIdsForSongDeact = getParameter("COMMON",
								"DCT_PENDING_SONGS_COS");
						if (cosIdsForSongDeact != null && cosId != null) {
							List<String> cosIDs = Arrays
									.asList(cosIdsForSongDeact.split(","));
							if (cosIDs.contains(cosId)) {
								SelectionRequest selectionRequest = new SelectionRequest(
										subscriberID);
								rbtClient
										.deleteSubscriberSelection(selectionRequest);
								logger
										.info("processDirectActivationRequest:: user is in act pending, "
												+ "cos is in configured cos list.Deactivating users act pending selection : "
												+ selectionRequest
														.getResponse());
							}
						}
					}
					// End of JIRAID-515

					// Added to support pre-rbt for users activated through
					// direct activation and only through configured modes.
					if (mode != null) {
						String preRbtSupportedModes = RBTParametersUtils
								.getParamAsString(
										COMMON,
										"PRE_RBT_SUPPORTED_MODES_FOR_DIRECT_ACT",
										null);
						if (preRbtSupportedModes != null) {
							String[] supportedModes = preRbtSupportedModes
									.split(",");
							Set<String> supportedModesSet = new HashSet<String>(
									Arrays.asList(supportedModes));
							if (supportedModesSet.contains(mode))
								task.setObject(EXTRA_INFO_INTRO_PROMPT_FLAG,
										ENABLE_PRESS_STAR_INTRO);
						}
					}

					subscriber = processActivation(task);
					task.setObject(param_subscriber, subscriber);
					if (subscriber != null
							&& isUserActive(subscriber.getStatus())) {
						task.setObject(param_response, SUCCESS + "|"
								+ subscriber.getRefID());

						if (!task.containsKey(param_catid))
							task.setObject(param_catid, "3");

						isThisFeature(task, tokenizeArrayList(param(SMS,
								RBT_KEYWORD, null), null), null);
						isThisFeature(task, tokenizeArrayList(param(SMS,
								ACTIVATION_KEYWORD, null), null), null);
						boolean isCricket = isThisFeature(task,
								tokenizeArrayList(param(SMS, CRICKET_KEYWORD,
										null), null), null);

						getCallerID(task);

						smsList = (ArrayList<String>) task
								.getObject(param_smsText);
						logger
								.info("processDirectActivationRequest:: sms keywords > "
										+ smsList);

						Clip clip = null;
						boolean isProfileSelection = false;
						if (smsList != null && smsList.size() > 0
								&& smsList.get(0) != null
								&& !smsList.get(0).equalsIgnoreCase("null")) {
							clip = getProfileClip(task);
							if (clip != null) {
								isProfileSelection = true;
							} else {
								logger
										.info("processDirectActivationRequest:: Clip ID >"
												+ smsList.get(0));
								getCategoryAndClipForPromoID(task, smsList
										.get(0));
								clip = (Clip) task.getObject(CLIP_OBJ);
							}
						}
						if (isProfileSelection) {
							task.setObject(IS_ACTIVATION_REQUEST, "false");
							getLang(task);
							processSetTempOverride(task);
						} else if (isCricket) {
							processCricket(task);
							logger
									.info("processDirectActivationRequest:: CricketResponse > "
											+ task
													.getString(param_responseObdMark));
						} else if (clip == null
								|| clip.getClipEndTime().getTime() < System
										.currentTimeMillis()) {
							logger
									.info("processDirectActivationRequest:: Clip is Null, Not making any Selection");
						} else {
							response = processSetSelection(task);
							logger
									.info("processDirectActivationRequest:: SelectionResponse > "
											+ response);
						}

						task.setObject(param_Sender, "56789");
						task.setObject(param_Reciver, task
								.getString(param_subscriberID));
						task.setObject(param_Msg, subscriptionClass
								.getSmsOnSubscription());
						sendSMS(task);
					} else
						task.setObject(param_response, FAILURE);
				} else if (isDctSMS(task)) {
					task.setObject(param_subscriberID, subscriberID);
					Subscriber subscriber = getSubscriber(task);
					if (subscriber == null
							|| subscriber.getStatus().equalsIgnoreCase(
									WebServiceConstants.NEW_USER)
							|| subscriber.getStatus().equalsIgnoreCase(
									WebServiceConstants.DEACTIVE)) {
						task.setObject(param_response, "NOTACTIVE");
					} else {
						// task.setObject(param_smsText, info);
						task.setObject(param_isdirectdct, "true");
						task.setObject(param_subclass, classType);
						String deactResponse = super.processDeactivation(task);
						if (deactResponse.equals(WebServiceConstants.SUCCESS)) {
							task.setObject(param_response, SUCCESS);
							task.setObject(param_Sender, "56789");
							task.setObject(param_Reciver, task
									.getString(param_subscriberID));
							task.setObject(param_Msg, subscriptionClass
									.getSmsAlertBeforeRenewal());
							sendSMS(task);
						} else
							task.setObject(param_response,
									"DEACTIVATION_FAILED");
					}
				}
			}
		} catch (Exception e) {
			logger.error("exception ", e);
		}
		return;

	}

	private boolean isActSMS(Task task) {
		boolean isActSms = false;
		logger.info("RBT:: isActSMS  " + task.getString(param_info));

		ArrayList<String> smsKeyWords = tokenizeArrayList((String) task
				.getObject(param_info), " ");
		ArrayList<String> actKeyWords = tokenizeArrayList(getParameter(SMS,
				"ACTIVATION_KEYWORD"), ",");

		if (smsKeyWords == null || actKeyWords == null)
			return false;

		for (String keyWord : actKeyWords) {
			if (smsKeyWords.contains(keyWord)) {
				isActSms = true;
				smsKeyWords.remove(keyWord);
			}
		}
		task.setObject(param_smsText, smsKeyWords);
		return isActSms;
	}

	private boolean isDctSMS(Task task) {
		boolean isDctSms = false;
		logger.info("RBT:: isDctSMS  " + task.getString(param_info));
		ArrayList<String> smsKeyWords = tokenizeArrayList((String) task
				.getObject(param_info), " ");
		ArrayList<String> dctKeyWords = tokenizeArrayList(getParameter(SMS,
				"DEACTIVATION_KEYWORD"), ",");

		if (smsKeyWords == null || dctKeyWords == null)
			return false;

		for (String keyWord : dctKeyWords) {
			if (smsKeyWords.contains(keyWord)) {
				isDctSms = true;
				smsKeyWords.remove(keyWord);
			}
		}
		task.setObject(param_smsText, smsKeyWords);
		return isDctSms;

	}

	@Override
	public void processEmotionSongRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		task.setObject(param_mmContext, WebServiceConstants.EMOTION_RBT);

		if (smsList != null && smsList.size() > 0) {
			String promoID = smsList.get(0);
			logger
					.info("RBT:: processEmotionSongRequest: promoID ->"
							+ promoID);
			getCategoryAndClipForPromoID(task, smsList.get(0));
			Clip clip = (Clip) task.getObject(CLIP_OBJ);
			if (clip != null
					&& clip.getClipEndTime().getTime() > System
							.currentTimeMillis()) {
				// Checking for Non Emotion Content
				String contentType = clip.getContentType();
				if (contentType == null
						|| (!contentType.equalsIgnoreCase("EMOTION_RBT") && !contentType
								.equalsIgnoreCase("EMOTION_UGC"))) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							EMOTION_INVALID_CONTENT, invalidEmotionContent,
							subscriber.getLanguage()));
					return;
				}

				task.setObject(param_chargeclass, clip.getClassType());
				task.setObject(param_inLoop, WebServiceConstants.NO); // Emotion
																		// Rbt
																		// selections
																		// will
																		// be
																		// added
																		// in
																		// override
																		// mode
			} else {
				if (task.containsKey(param_clipid))
					task.remove(param_clipid);
			}
		}
		String response = processSetSelection(task);
		logger.info("RBT:: processEmotionSongRequest: selection response ->"
				+ response);
		setEmotionRequestResponse(response, task, subscriber);
	}

	@Override
	public void processDeactEmotionRbtService(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			logger
					.info("RBT:: processDeactEmotionRbtService: User is not active ->"
							+ subscriber.getStatus());
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		if (!subscriber.getUserType().equals(
				WebServiceConstants.EMOTION_RBT_USER)) {
			logger
					.info("RBT:: processDeactEmotionRbtService: User is not Emotion RBT subscriber ->"
							+ subscriber.getUserType());
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		SelectionRequest selectionRequest = new SelectionRequest(subscriber
				.getSubscriberID());
		selectionRequest.setMmContext(WebServiceConstants.EMOTION_RBT);
		RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);
		logger
				.info("RBT:: processDeactEmotionRbtService: deactivation response ->"
						+ selectionRequest.getResponse());
		if (selectionRequest.getResponse().equals(WebServiceConstants.SUCCESS)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					EMOTION_DCT_SUCCESS, emotionRbtDeactivationSuccess,
					subscriber.getLanguage()));
			return;
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					EMOTION_DCT_FAILURE, emotionRbtDeactivationFailure,
					subscriber.getLanguage()));
			return;
		}
	}

	@Override
	public void processExtendEmotionRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (!isUserActive(subscriber.getStatus())) {
			logger
					.info("RBT:: processExtendEmotionRequest: User is not active ->"
							+ subscriber.getStatus());
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber
				.getSubscriberID());
		rbtDetailsRequest.setStatus(String.valueOf(94));
		Settings settings = RBTClient.getInstance().getSettings(
				rbtDetailsRequest);

		if (settings == null || settings.getSettings() == null
				|| settings.getSettings().length == 0) {
			logger
					.info("RBT:: processExtendEmotionRequest: No Active emotion selections exists");
			task.setObject(param_responseSms, getSMSTextForID(task,
					EMOTION_NO_ACTIVE_EMOTION, noActiveEmotionExists,
					subscriber.getLanguage()));
			return;
		}

		Setting[] setting = settings.getSettings();

		long timeDiff = setting[0].getSetTime().getTime()
				+ (12 * 60 * 60 * 1000) - System.currentTimeMillis();
		float diffMinutes = (timeDiff / (1000 * 60));

		// Deactivating the current active emotion selection
		SelectionRequest selectionRequest = new SelectionRequest(subscriber
				.getSubscriberID());
		selectionRequest.setRbtFile(setting[0].getRbtFile());
		selectionRequest.setStatus(setting[0].getStatus());
		RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);

		if (selectionRequest.getResponse().equalsIgnoreCase(
				WebServiceConstants.SUCCESS)) {
			logger
					.info("RBT:: processExtendEmotionRequest: Deactivation is Sucess, making new Selection");
			// Deactivation is Success
			// Making new Selection with the same clipID and adding remaining
			// profile Hrs of deactive selection to the 12Hrs
			int diffHrs = Math.round(diffMinutes / 60);

			Clip clip = rbtCacheManager.getClipByRbtWavFileName(setting[0]
					.getRbtFile());

			task.setObject(param_mmContext, WebServiceConstants.EMOTION_RBT);

			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			task.setObject(param_profile_hours, String.valueOf(12 + diffHrs));
			task.setObject(param_status, String.valueOf(94)); // status 94 means
																// Emotion RBT
																// song
			task.setObject(param_chargeclass, setting[0].getChargeClass());
			task.setObject(param_actby, setting[0].getSelectedBy());
			task.setObject(param_actInfo, setting[0].getSelectionInfo());
			task.setObject(param_inLoop, "NO");

			String response = processSetSelection(task);
			logger
					.info("RBT:: processExtendEmotionRequest: selection response "
							+ response);
			setEmotionRequestResponse(response, task, subscriber);
		}
	}

	private void setEmotionRequestResponse(String response, Task task,
			Subscriber subscriber) {
		if (response.equals(WebServiceConstants.SUCCESS)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROMO_ID_SUCCESS, m_promoSuccessTextDefault, subscriber
							.getLanguage()));
		} else if (response.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					SELECTION_SUSPENDED_TEXT, m_SuspendedSelDefault, subscriber
							.getLanguage()));
		} else if (isActivationFailureResponse(response)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
		} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, SELECTION_ALREADY_EXISTS_TEXT,
							getSMSTextForID(task, PROMO_ID_SUCCESS,
									m_promoSuccessTextDefault, subscriber
											.getLanguage()), subscriber
									.getLanguage()));
		} else if (response
				.equals(WebServiceConstants.EMOTION_RBT_NOT_ALLOWED_FOR_SPECIFIC_CALLER)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					EMOTION_NOT_ALLOWED_FOR_SPECIFIC_CALLER,
					emotionRbtNotAllowedForSpecificCaller, subscriber
							.getLanguage()));
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber
							.getLanguage()));
		}
	}

	/**
	 * @author Sreekar
	 */
	@Override
	public void processConfirmCharge(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberCosID =  subscriber.getCosID();
		String songBasedCosId = getParameter(COMMON,
				"SONG_BASED_COS_ID");
		List<String> songBaseCosIdsList = new ArrayList<String>();
		if (songBasedCosId != null && !songBasedCosId.isEmpty()) {
			songBaseCosIdsList = Arrays.asList(songBasedCosId.split(","));
			if(songBaseCosIdsList.contains(subscriberCosID)){
				processBaseAndCosUpgradationRequest(task);
				return;
			}
		}
		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			confirmSubscription(task);
		if (!task.containsKey(param_responseSms))
			task.setObject(param_responseSms, getSMSTextForID(task,
					CONFIRM_CHARGE_FAILURE, m_confirmChargeFailureDefault,
					subscriber.getLanguage()));
	}

	/**
	 * This method calls the updateSubscription method of rbtClient with info as
	 * confirmCharge so that the updateSubscription API will invoke the SM
	 * client confirm charge API
	 * 
	 * @param task
	 * @return
	 */
	protected void confirmSubscription(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		SubscriptionRequest updateRequest = new SubscriptionRequest(subscriber
				.getSubscriberID());
		subscriber = rbtClient.confirmSubscription(updateRequest);
		if (updateRequest.getResponse().equalsIgnoreCase(
				WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					CONFIRM_CHARGE_SUCCESS, m_confirmChargeSuccessDefault,
					subscriber.getLanguage()));
	}

	@Override
	public void processDownloadOptinRenewal(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (!isUserActive(subscriber.getStatus())) {
			logger.info("Inactive user");
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_OPTIN_RENEWAL_INACTIVE_USER, "Inactive User.",
					subscriber.getLanguage()));
			return;
		}


		SubscriberDownloads[] downloadsList = RBTDBManager.getInstance()
				.getActiveSubscriberDownloads(subscriber.getSubscriberID());
		if (downloadsList == null || downloadsList.length == 0) {
			logger.info("No active downloads");
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID,
					"Invalid Content Id.", subscriber.getLanguage()));
			return;
		}
		String allDownloadsRenAllowed = getParameter("COMMON", "OPTIN_RENEWAL_FOR_ALL_DOWNLOADS_ALLOWED");
		boolean isAllowed = false;
		if(allDownloadsRenAllowed!=null && allDownloadsRenAllowed.equalsIgnoreCase("true")){
			isAllowed = true;
		}
		String contentName =null;
		String response = ERROR;
		if ((smsList == null ||smsList.size()== 0)&& isAllowed){
          String renewalChargeClass = getParameter("COMMON", "DOWNLOAD_OPTIN_RENEWAL_CHARGE_CLASS");
          if(renewalChargeClass!=null){
        	logger.info("Processing download renewal for all matching downloads charge type.......");
            List<String> renewalChargeClassList = Arrays.asList(renewalChargeClass.split(","));
            for(SubscriberDownloads download : downloadsList){
          	   if(renewalChargeClassList.contains(download.classType())){
          		  String resp = hitSMForDownloadRenewal(download);
          		  if(resp.equalsIgnoreCase("SUCCESS")){
          			String wavFile = download.promoId(); //It gives wavfile from download
          			Clip clip = getClipByWavFile(wavFile);
          			if(clip!=null)
          				contentName += clip.getClipName()+",";
          		  }
          	   }
            }
            if(contentName!=null){
              contentName = contentName.substring(0,contentName.lastIndexOf(","));
              response = SUCCESS;
            }
          }
		}else{
			if (smsList == null || smsList.size() < 1) {
				logger.info("sms text invalid");
				task.setObject(param_responseSms, getSMSTextForID(task,
						DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID,
						"Invalid Content Id.", subscriber.getLanguage()));
				return;
			}
	
			String token = smsList.get(0).trim();
			Clip clip = getClipByPromoId(token, subscriber.getLanguage());
			String categoryId = null;
			String wavFile = null;
			Category category = null;
			if (clip != null) {
				wavFile = clip.getClipRbtWavFile();
			} else {
				category = getCategoryByPromoId(token, subscriber.getLanguage());
				if (category != null)
					categoryId = category.getCategoryId() + "";
			}
			logger.info("wavFile=" + wavFile + ", category=" + category);
			if (wavFile == null && categoryId == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID,
						"Invalid Content Id.", subscriber.getLanguage()));
				return;
			}
	
			SubscriberDownloads download = null;
			for (int i = 0; i < downloadsList.length; i++) {
				if ((category != null
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(category.getCategoryTpe())
						&& categoryId != null && categoryId
						.equalsIgnoreCase(downloadsList[i].categoryID() + ""))
						||
	
						(wavFile != null && wavFile
								.equalsIgnoreCase(downloadsList[i].promoId()))) {
					download = downloadsList[i];
					break;
				}
			}
			if (download == null) {
				logger.info("No appropriate downloads");
				task.setObject(param_responseSms, getSMSTextForID(task,
						DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID,
						"Invalid Content Id.", subscriber.getLanguage()));
				return;
			}
	
			response = hitSMForDownloadRenewal(download);
			
			if (clip != null)
				contentName = clip.getClipName();
			else if (category != null)
				contentName = category.getCategoryName();
		}
		String smsText = null;
		if (response.equalsIgnoreCase("SUCCESS"))
			smsText = getSMSTextForID(task, DOWNLOAD_OPTIN_RENEWAL_SUCCESS,
					"Download renewal successful.", subscriber.getLanguage());
		else if (response.equalsIgnoreCase("ERROR"))
			smsText = getSMSTextForID(task, DOWNLOAD_OPTIN_RENEWAL_FAILURE,
					"Downlaod renewal failed.", subscriber.getLanguage());
    
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	private String hitSMForDownloadRenewal(SubscriberDownloads download) {
		try {
			String confirmOptinUrl = getParameter("COMMON",
					"DOWNLOAD_OPTIN_RENEWAL_URL");
			confirmOptinUrl = confirmOptinUrl.replace("%msisdn%", download
					.subscriberId());
			confirmOptinUrl = confirmOptinUrl.replace("%srvkey%", "RBT_SEL_"
					+ download.classType());
			confirmOptinUrl = confirmOptinUrl.replace("%refid%", download
					.refID());

			// Setting HttpParameters
			HttpParameters httpParam = new HttpParameters();
			httpParam.setUrl(confirmOptinUrl);
			httpParam.setConnectionTimeout(6000);

			// Setting request Params
			HashMap<String, String> params = new HashMap<String, String>();

			logger.info("confirmOptinUrl: " + confirmOptinUrl
					+ ". Parameters: " + params.toString());
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParam, params);
			String response = httpResponse.getResponse();
			logger.info("Response is " + response);
			if (response.toUpperCase().contains("SUCCESS")) {
				return "SUCCESS";
			}
			return "ERROR";
		} catch (Exception e) {
			logger.error("Exception caught", e);
			return "ERROR";
		}
	}

	@Override
	public void processTNBActivation(Task task) {
		logger.debug("Processing TNB Activation");
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)) {
			// Send sms informing existing subscription
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_ACTIVATION_EXISTINGUSER, m_tnbExistingUser, subscriber
							.getLanguage()));
			return;
		}

		if (!task.containsKey(param_subclass))
			task.setObject(param_subclass, "ZERO"); // pass subscriptionClass
		task.setObject(param_actby, "TNB");

		String promoId = task.getString(param_PROMO_ID);
		String defaultJinglePromoID = param("SMS", "DEFAULT_PROMO_ID", null);
		if (promoId != null) {
			defaultJinglePromoID = promoId;
		}
		if (defaultJinglePromoID != null) {
			getCategoryAndClipForPromoID(task, defaultJinglePromoID); // pass
																		// default
																		// song
																		// clipID,
																		// if
																		// promoId
																		// is
																		// null
			if (!task.containsKey(param_chargeclass))
				task.setObject(param_chargeclass, "FREE");
			response = processSetSelection(task);
		} else {
			processActivation(task);
			response = task.getString(param_response);
		}

		if (response.equals(WebServiceConstants.SUCCESS))
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_ACTIVATION_SUCCESS, m_tnbSucceessMessage, subscriber
							.getLanguage()));
		else if(response.equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, subscriber.getLanguage()));
		}else if(response.equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, subscriber.getLanguage()));
		}else if(response.equals(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)){
			task.setObject(param_responseSms, getSMSTextForID(task, "RBT_CORPORATE_NOTALLOW_SELECTION",
					m_corpChangeSelectionFailureDefault, subscriber.getLanguage()));
		}
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_ACTIVATION_FAILURE, m_tnbFailureMessage, subscriber
							.getLanguage()));

	}

	@Override
	public void processEnableUdsRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (subscriber.getStatus().equals(WebServiceConstants.ACTIVE)
				|| subscriber.getStatus().equals(
						WebServiceConstants.ACT_PENDING)) {
			HashMap<String, String> userInfoMap = subscriber.getUserInfoMap();
			//JIRA-ID: RBT-13626
			String udsOptinKeyMapStr = RBTParametersUtils.getParamAsString(
					COMMON, "UDS_OPTIN_KEY_TO_TYPE_MAP", null);
			Map<String, String> udsOptinKeyMap = MapUtils.convertToMap(
					udsOptinKeyMapStr, ",", "=", null);
			String udsOptInType = null;
			if (udsOptinKeyMap != null && !udsOptinKeyMap.isEmpty()) {
				@SuppressWarnings("unchecked")
				ArrayList<String> smsList = (ArrayList<String>) task
						.getObject(param_smsText);
				if (null != smsList && !smsList.isEmpty()) {
					for (String key : smsList) {
						key = key.toUpperCase();
						udsOptInType = udsOptinKeyMap.get(key);
						if (null != udsOptInType) {
							break;
						}
					}
				}
				udsOptInType = (udsOptInType == null? "TRUE" : udsOptInType);
				userInfoMap.put(UDS_OPTIN, udsOptInType);
			} else if (userInfoMap != null) {
				userInfoMap.put(UDS_OPTIN, "TRUE");
			} else {
				userInfoMap = new HashMap<String, String>();
				userInfoMap.put(UDS_OPTIN, "TRUE");
			}

			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriber.getSubscriberID());
			subscriptionRequest.setUserInfoMap(userInfoMap);

			rbtClient.updateSubscription(subscriptionRequest);

			if (subscriptionRequest.getResponse().equals(
					WebServiceConstants.SUCCESS))
				task.setObject(param_responseSms, getSMSTextForID(task,
						UDSOPTIN_SUCCESS, udsOptInSuccess, subscriber
								.getLanguage()));
			else
				task.setObject(param_responseSms, getSMSTextForID(task,
						UDSOPTIN_FAILURE, udsOptInFailed, subscriber
								.getLanguage()));
		} else
			task
					.setObject(param_responseSms, getSMSTextForID(task,
							UDSOPTIN_FAILURE, udsOptInFailed, subscriber
									.getLanguage()));
	}

	@Override
	public void processDisableUdsRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (subscriber != null
				&& (!subscriber.equals(WebServiceConstants.DEACTIVE) || !subscriber
						.equals(WebServiceConstants.DEACT_PENDING))) {
			HashMap<String, String> userInfoMap = subscriber.getUserInfoMap();
			userInfoMap.put(UDS_OPTIN, "FALSE");
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriber.getSubscriberID());
			subscriptionRequest.setUserInfoMap(userInfoMap);

			rbtClient.updateSubscription(subscriptionRequest);

			if (subscriptionRequest.getResponse().equals(
					WebServiceConstants.SUCCESS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						UDSDCTOPIN_SUCCESS, udsDctOptInSuccess, subscriber
								.getLanguage()));
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						UDSDCTOPIN_FAILURE, udsDctOptInFailed, subscriber
								.getLanguage()));
			}
		} else
			task.setObject(param_responseSms, getSMSTextForID(task,
					UDSOPTIN_FAILURE, udsOptInFailed, null));
	}

	@Override
	public void processChurnOffer(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		Parameters subClassParams = CacheManagerUtil
				.getParametersCacheManager().getParameter(COMMON,
						"SUBSCRIPTION_CLASS_FOR_CHURN_OFFER");
		task.setObject(param_SMSTYPE, "CANCEL_OFFER");
		ViralData[] cancelOfferRequests = getViraldata(task);
		if (subClassParams == null || cancelOfferRequests == null
				|| cancelOfferRequests.length <= 0) {
			logger
					.info("Churn offer feature is not enabled or the subscriber is not offered the feature");
			task.setObject(param_responseSms, getSMSTextForID(task,
					"CHURN_OFFER_NOT_ALLOWED", churnOfferNotAllowed, subscriber
							.getLanguage()));
			return;
		}

		String subClass = subClassParams.getValue();

		String circleId = subscriber.getCircleID();
		String oldSubClass = subscriber.getSubscriptionClass();

		subClassParams = CacheManagerUtil.getParametersCacheManager()
				.getParameter(
						COMMON,
						"SUBSCRIPTION_CLASS_FOR_CHURN_OFFER_"
								+ circleId.toUpperCase() + "_"
								+ oldSubClass.toUpperCase());
		if (subClassParams == null) {
			subClassParams = CacheManagerUtil.getParametersCacheManager()
					.getParameter(
							COMMON,
							"SUBSCRIPTION_CLASS_FOR_CHURN_OFFER_ALL_"
									+ oldSubClass.toUpperCase());
		}

		if (subClassParams != null) {
			subClass = subClassParams.getValue();
		}

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
		subscriptionRequest.setMode(SMS);
		subscriptionRequest.setRentalPack(subClass);
		try {
			RBTClient.getInstance().activateSubscriber(subscriptionRequest);
			if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS)) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(2037, 0, 1);
				Date endDate = calendar.getTime();
				RBTDBManager.getInstance().insertViralBlackList(
						subscriber.getSubscriberID(), null, endDate,
						"CHURN_OFFER");
				removeViraldata(subscriber.getSubscriberID(), null,
						"CANCEL_OFFER");
				logger
						.info("Upgraded the subscriber successfully to CHURN offer pack");
				task.setObject(param_responseSms, getSMSTextForID(task,
						"CHURN_OFFER_SUCCESS", churnOfferSuccess, subscriber
								.getLanguage()));
			} else {
				logger
						.info("Failed to upgrade the subscriber to CHURN offer pack, response from webservice : "
								+ subscriptionRequest.getResponse());
				task.setObject(param_responseSms, getSMSTextForID(task,
						"CHURN_OFFER_FAILURE", churnOfferFailure, subscriber
								.getLanguage()));
			}

		} catch (Exception e) {
			logger.error("Exception in processing churn offer request >"
					+ e.getMessage());
			task.setObject(param_responseSms, getSMSTextForID(task,
					"CHURN_OFFER_FAILURE", churnOfferFailure, subscriber
							.getLanguage()));
		}
	}

	@Override
	public void processWebRequest(Task task) {
		Subscriber subscriber = getSubscriber(task);

		task.setObject(param_SMSTYPE, "WEB_REQUEST");
		ViralData context[] = getViraldata(task);
		if (context == null || context.length <= 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		ViralData viralData = null;

		Clip clip = null;
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String promoId = (smsList.size() >= 1 ? smsList.get(0) : null);

		if (promoId != null) {
			clip = getClipByPromoId(promoId, subscriber.getLanguage());
			if (clip == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
								.getLanguage()));
				return;
			}
		}

		boolean isSelectionActionType = false;
		if (RBTParametersUtils.getParamAsBoolean(SMS, "IS_PROMO_CODE_OPTIONAL",
				"FALSE")) {
			viralData = context[context.length - 1];
			clip = getClipById(viralData.getClipID(), subscriber.getLanguage());
		} else {
			for (int loop = 0; loop < context.length; loop++) {
				HashMap<String, String> contextInfoMap = context[loop]
						.getInfoMap();
				if (clip == null
						&& !isWebSelectionSongActRequest(contextInfoMap)) {
					// consider the latest act or dct request
					viralData = context[loop];
					continue;
				}
				if (isWebSelectionSongActRequest(contextInfoMap)) {
					if (clip != null
							&& context[loop].getClipID().equalsIgnoreCase(
									clip.getClipId() + "")) {
						viralData = context[loop];
						isSelectionActionType = true;
						break;
					}
				}
			}
		}

		logger.info("RBT:: Context : " + viralData);
		processWebRequest(task, context, viralData, clip, isSelectionActionType);
	}

	private void processWebRequest(Task task, ViralData[] context,
			ViralData viralData, Clip clip, boolean isSelectionActionType) {
		String categoryId = "3";
		Category category = null;
		Subscriber subscriber = getSubscriber(task);

		if (viralData == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		try {
			if (webLogger != null && isSelectionActionType)
				webLogger.DTWebsiteLogger(new Timestamp(System
						.currentTimeMillis()), subscriber.getSubscriberID(),
						clip != null ? (clip.getClipName() != null ? clip
								.getClipName() : "NA") : "NA",
						clip != null ? (clip.getClipName() != null ? clip
								.getClipPromoId() : "NA") : "NA", task
								.getString(param_smsSent) != null ? task
								.getString(param_smsSent) : "NA", "SMS",
						"sms received");
		} catch (ReportingException e) {
			logger.error("Error while writing DT Event Logs ", e);
		}

		String profileHours = null;
		HashMap<String, String> contextInfoMap = viralData.getInfoMap();
		if (contextInfoMap != null
				&& contextInfoMap.containsKey("SMS_TYPE")
				&& contextInfoMap.get("SMS_TYPE").equalsIgnoreCase(
						"WEBSELECTION")) {
			String[] tokens = param(SMS, "WEBSELECTION_CONFIG", "").split(","); // activatedBy,
																				// selectedBy,
																				// classType,
																				// categoryID
			if (tokens.length >= 1)
				task.setObject(param_actby, tokens[0]);
			if (tokens.length >= 2)
				task.setObject(param_actby, tokens[1]);
			if (tokens.length >= 3)
				task.setObject(param_subclass, tokens[2]);

			if(contextInfoMap.containsKey("MODE")){
				task.setObject(param_actby, contextInfoMap.get("MODE"));
			}
			
			if (contextInfoMap.containsKey("CATEGORY_ID")) {
				// check if the category id is given by the 3rd party URL.
				categoryId = contextInfoMap.get("CATEGORY_ID");
			} else if (tokens.length >= 4) {
				// if not, get the category id from the config
				// WEBSELECTION_CONFIG
				categoryId = tokens[3];
			}
			category = getCategory(categoryId, subscriber.getLanguage());

			if (contextInfoMap.containsKey("PROFILE_HOURS")) {
				profileHours = contextInfoMap.get("PROFILE_HOURS");
				if (profileHours != null)
					task.setObject(param_profile_hours, profileHours);
			}

			if (contextInfoMap.containsKey("CHARGE_CLASS")) {
				task.setObject(param_chargeclass, contextInfoMap
						.get("CHARGE_CLASS"));
			}

			if (contextInfoMap.containsKey("USE_UI_CHARGE_CLASS")) {
				task.setObject(param_USE_UI_CHARGE_CLASS, contextInfoMap.get(
						"USE_UI_CHARGE_CLASS").equalsIgnoreCase("y"));
			}
			
			if (contextInfoMap.containsKey("SUBSCRIPTION_CLASS")) {
				task.setObject(param_subclass, contextInfoMap.get(
						"SUBSCRIPTION_CLASS"));
			}


			String callerID = viralData.getCallerID();
			if (callerID != null && !callerID.trim().equals(""))
				task.setObject(param_callerid, callerID);

		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_FAILURE, m_viralFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		if (category == null) {
			logger
					.info("RBT:: CategoryID is null. Getting default category ID 3 ");
			categoryId = "3";
			category = getCategory(3, subscriber.getLanguage());
		}
		task.setObject(param_catid, categoryId);
		task.setObject(CAT_OBJ, category);

		if (task.containsKey(param_actInfo)) {
			String actInfo = task.getString(param_actInfo) + ":WEBSELECTION";
			task.setObject(param_actInfo, actInfo);
		} else
			task.setObject(param_actInfo, "WEBSELECTION");

		if (viralData.getClipID() != null) {
			// Clip clip =
			// getClipById(viralData.getClipID(),subscriber.getLanguage());
			task.setObject(CLIP_OBJ, clip);
			if (clip != null)
				task.setObject(param_clipid, clip.getClipId() + "");

			String response = processSetSelection(task);

			if (isActivationFailureResponse(response)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
			}
			if (response.equals(WebServiceConstants.SUCCESS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						VIRAL_SUCCESS, m_viralSuccessTextDefault, subscriber
								.getLanguage()));
			} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
								PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage()));
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
			}
		} else {
			if ("DCT".equalsIgnoreCase(contextInfoMap.get("ACTION_TYPE"))) {
				processDeactivation(task);
			} else {
				subscriber = processActivation(task);
				if (!isUserActive(subscriber.getStatus())) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							TECHNICAL_FAILURE, m_technicalFailuresDefault,
							subscriber.getLanguage()));
					// return;
				} else {
					task.setObject(param_responseSms, getSMSTextForID(task,
							WEB_ACTIVATION_SUCCESS,
							m_webActivationSuccessDefault, subscriber
									.getLanguage()));
				}
			}
		}

		if (RBTParametersUtils.getParamAsBoolean(SMS, "IS_PROMO_CODE_OPTIONAL",
				"FALSE")) {
			removeViraldata(viralData.getSubscriberID(), viralData.getType());
		}

		if (isSelectionActionType) {
			removeViraldata(viralData.getSmsID());
			return;
		}

		if (context != null) {
			// this is act or dct request hence delete all the act and dct
			// requests
			for (int loop = 0; loop < context.length; loop++) {
				HashMap<String, String> infoMap = context[loop].getInfoMap();
				if (!isWebSelectionSongActRequest(infoMap)) {
					removeViraldata(context[loop].getSmsID());
				}
			}
		}
		
		//To set PACK_NAME and PACK VALID DAYS
		String smsKeyWord = null;
		if(subscriber!=null){
			CosDetails cosDetails = (CosDetails) CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID());
			if(cosDetails!=null){
				smsKeyWord = cosDetails.getSmsKeyword();
				String smsText = task.getString(param_responseSms);
				if(smsText!=null && smsKeyWord!=null){
					task.setObject(param_responseSms, substitutePackNameValidDays(smsText, smsKeyWord));
				}
			}
		}
	}

	private boolean isWebSelectionSongActRequest(
			HashMap<String, String> contextInfoMap) {
		if (contextInfoMap != null
				&& "WEBSELECTION".equalsIgnoreCase(contextInfoMap
						.get("SMS_TYPE"))) {
			return "SEL".equalsIgnoreCase(contextInfoMap.get("ACTION_TYPE"));
		}
		return false;
	}

	@Override
	public void processRDCViralSelection(Task task) {
		String categoryId = "3";
		Category category = null;

		Subscriber subscriber = getSubscriber(task);

		task.setObject(param_SMSTYPE, "RDC_SEL_PENDING");
		ViralData contexts[] = getViraldata(task);
		if (contexts == null || contexts.length < 1 || contexts[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RDC_SEL_FAILURE, m_rdcSelFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		ViralData viralData = contexts[contexts.length - 1];
		logger.info("RBT:: Context : " + viralData);

		HashMap<String, String> contextInfoMap = viralData.getInfoMap();
		if (contextInfoMap != null) {
			if (contextInfoMap.containsKey("CATEGORY_ID")) {
				categoryId = contextInfoMap.get("CATEGORY_ID");
			}
			if (contextInfoMap.containsKey("MODE")) {
				task.setObject(param_actby, contextInfoMap.get("MODE"));
			}
			if (contextInfoMap.containsKey("SUB_CLASS")) {
				task.setObject(param_subclass, contextInfoMap.get("SUB_CLASS"));
			}
			if (contextInfoMap.containsKey("ADD_IN_LOOP")) {
				task.setObject(param_inLoop, contextInfoMap.get("ADD_IN_LOOP"));
			}
			if(contextInfoMap.containsKey("MODE_INFO")){
            	task.setObject(param_actInfo, contextInfoMap.get("MODE_INFO"));
            }
			
			if (contextInfoMap.containsKey("CHARGE_CLASS")) {	
				task.setObject(param_chargeclass, contextInfoMap
						.get("CHARGE_CLASS"));
			}

			if (contextInfoMap.containsKey("USE_UI_CHARGE_CLASS")) {
				task.setObject(param_USE_UI_CHARGE_CLASS, contextInfoMap.get(
						"USE_UI_CHARGE_CLASS").equalsIgnoreCase("y"));
			}

			category = getCategory(categoryId, subscriber.getLanguage());
		}

		if (category == null) {
			logger
					.info("RBT:: CategoryID is null. Getting default category ID 3 ");
			categoryId = "3";
			category = getCategory(3, subscriber.getLanguage());
		}
		task.setObject(param_catid, categoryId);
		task.setObject(CAT_OBJ, category);

		if (viralData.getClipID() != null) {
			Clip clipMinimal = getClipById(viralData.getClipID(), subscriber
					.getLanguage());

			task.setObject(CLIP_OBJ, clipMinimal);
			if (clipMinimal != null)
				task.setObject(param_clipid, clipMinimal.getClipId() + "");
			String response = processSetSelection(task);

			if (isActivationFailureResponse(response)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RDC_TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
			}
			if (response.equals(WebServiceConstants.SUCCESS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RDC_SEL_SUCCESS, m_rdcSelSuccessTextDefault, subscriber
								.getLanguage()));
			} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
								PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
								subscriber.getLanguage()), subscriber
								.getLanguage()));
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						RDC_TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
			}
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					RDC_TECHNICAL_FAILURE, m_technicalFailuresDefault,
					subscriber.getLanguage()));
		}

		String oldViralTransText = "OLD";
		String newViralTransText = "ACT_N_SEL";
		if (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACT_PENDING)
				|| subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.GRACE)
				|| subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.RENEWAL_PENDING))
			newViralTransText = "SEL";

		// writing all viralData Information for the subscriberID except the
		// latest one
		for (int i = 0; i < contexts.length; i++) {
			ViralData data = contexts[i];
			if (i == (contexts.length - 1))
				rdcSelectionsLogger.info(data.getSubscriberID() + "!"
						+ data.getClipID() + "!" + data.getType() + "!"
						+ data.getSentTime() + "!" + data.getSmsID() + "!"
						+ newViralTransText);
			else
				rdcSelectionsLogger.info(data.getSubscriberID() + "!"
						+ data.getClipID() + "!" + data.getType() + "!"
						+ data.getSentTime() + "!" + data.getSmsID() + "!"
						+ oldViralTransText);
		}
		removeViraldata(viralData.getSubscriberID(), "RDC_SEL_PENDING");
	}

	@Override
	public void processInitGift(Task task) {
		String songName = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		Clip clipObj = (Clip) task.getObject(CLIP_OBJ);
		if (task.getObject(CAT_OBJ) != null) {
			task.setObject(param_catid, ((Category) task.getObject(CAT_OBJ))
					.getCategoryId()
					+ "");
			songName = ((Category) task.getObject(CAT_OBJ)).getCategoryName();
		} else if (clipObj != null
				&& !clipObj.getClipEndTime().before(new Date())) {
			task.setObject(param_clipid, clipObj.getClipId() + "");
			songName = clipObj.getClipName();
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		task.setObject(param_isGifterConfRequired, "y");
		String response = processGift(task);

		String callerID = task.getString(param_callerid);
		if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SUCCESS)) {
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					callerID);
			Subscriber gifteeSubscriber = RBTClient.getInstance()
					.getSubscriber(rbtDetailsRequest);
			String smstext = null;
			if (gifteeSubscriber.getStatus().equalsIgnoreCase(
					WebServiceConstants.NEW_USER)
					|| gifteeSubscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.DEACTIVE)) {
				smstext = getSMSTextForID(task,
						INIT_GIFT_SUCCESS_GIFTEE_NEW_USER,
						m_initGiftSuccessGifteeNewUser, subscriber
								.getLanguage());
			} else {
				smstext = getSMSTextForID(task,
						INIT_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE,
						m_initGiftSuccessGifteeActive, subscriber.getLanguage());
			}

			DataRequest dataRequest = new DataRequest(subscriber
					.getSubscriberID(), callerID, "INIT_GIFT");
			dataRequest.setClipID(task.getString(param_clipid));
			ViralData[] viralData = RBTClient.getInstance().getViralData(
					dataRequest);

			String smsIDStr = String.valueOf(viralData[0].getSmsID());
			if (smsIDStr.length() > 4) {
				smsIDStr = smsIDStr.substring(smsIDStr.length() - 4);
			}
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smstext);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID", callerID == null ? "" : callerID);
			hashMap.put("SMS_ID", smsIDStr);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.EXISTS_IN_GIFTEE_LIBRAY)) {
			String smsText = getSMSTextForID(task,
					INIT_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY,
					m_initGiftFailureGiftExistsInGifteeLibrary, subscriber
							.getLanguage());

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID", callerID == null ? "" : callerID);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.INVALID)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.OWN_NUMBER)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NOT_ALLOWED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		} else
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
	}

	@Override
	public void processInitGiftConfirm(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0) {
			String smsID = smsList.get(0);

			DataRequest dataRequest = new DataRequest(subscriberID, null,
					"INIT_GIFT");
			ViralData[] viralData = RBTClient.getInstance().getViralData(
					dataRequest);
			if (viralData == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						INIT_GIFT_FAILURE_NO_PENDING_GIFTS,
						m_initGiftFailureNoPendingGifts, subscriber
								.getLanguage()));
				return;
			}

			boolean isPendingRequestFound = false;
			for (ViralData data : viralData) {
				String smsIDStr = String.valueOf(data.getSmsID());
				if (smsIDStr.length() > 4)
					smsIDStr = smsIDStr.substring(smsIDStr.length() - 4);

				if (smsID.equals(smsIDStr)) {
					String callerID = data.getCallerID();
					isPendingRequestFound = true;
					DataRequest viralDataRequest = new DataRequest(subscriber
							.getSubscriberID(), null, "INIT_GIFT");
					viralDataRequest.setSentTime(data.getSentTime());
					viralDataRequest.setNewType("GIFT");
					viralDataRequest.setCallerID(callerID);

					HashMap<String, String> infoMap = data.getInfoMap();
					if (infoMap == null)
						infoMap = new HashMap<String, String>();

					infoMap.put(GIFTTYPE_ATTR, "direct");
					viralDataRequest.setInfoMap(infoMap);
					RBTClient.getInstance().updateViralData(viralDataRequest);

					String smsText = getSMSTextForID(task,
							INIT_GIFT_CONFIRM_SUCCESS, null, subscriber
									.getLanguage());

					if (smsText != null) {
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put("SMS_TEXT", smsText);
						hashMap.put("CALLER_ID", callerID == null ? ""
								: callerID);
						hashMap.put("CIRCLE_ID", subscriber.getCircleID());
						task.setObject(param_responseSms,
								finalizeSmsText(hashMap));
					} else
						task.setObject(param_responseSms, "");

					break;
				}
			}

			if (!isPendingRequestFound) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						INIT_GIFT_FAILURE_NO_PENDING_GIFTS,
						m_initGiftFailureNoPendingGifts, subscriber
								.getLanguage()));
				return;
			}
		}
	}

	@Override
	public void processPreGift(Task task) {
		String songName = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		String promoID = null;
		Clip clipObj = (Clip) task.getObject(CLIP_OBJ);
		if (task.getObject(CAT_OBJ) != null) {
			task.setObject(param_catid, ((Category) task.getObject(CAT_OBJ))
					.getCategoryId()
					+ "");
			songName = ((Category) task.getObject(CAT_OBJ)).getCategoryName();
			promoID = ((Category) task.getObject(CAT_OBJ)).getCategoryPromoId();
		} else if (clipObj != null
				&& !clipObj.getClipEndTime().before(new Date())) {
			task.setObject(param_clipid, clipObj.getClipId() + "");
			songName = clipObj.getClipName();
			promoID = clipObj.getClipPromoId();
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		task.setObject(param_isGifteeConfRequired, "y");
		String response = processGift(task);

		String callerID = task.getString(param_callerid);
		if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SUCCESS)) {
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					callerID);
			Subscriber gifteeSubscriber = RBTClient.getInstance()
					.getSubscriber(rbtDetailsRequest);
			String smstext = null;
			String gifteeSmsText = null;
			if (gifteeSubscriber.getStatus().equalsIgnoreCase(
					WebServiceConstants.NEW_USER)
					|| gifteeSubscriber.getStatus().equalsIgnoreCase(
							WebServiceConstants.DEACTIVE)) {
				smstext = getSMSTextForID(task,
						PRE_GIFT_SUCCESS_GIFTEE_NEW_USER,
						m_preGiftGifteeSmsNewUser, subscriber
								.getLanguage());

				gifteeSmsText = getSMSTextForID(task,
							PRE_GIFT_GIFTEE_SMS_NEW_USER,
							m_preGiftSuccessGifteeActive, subscriber.getLanguage());
			} else {
				smstext = getSMSTextForID(task,
						PRE_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE,
						m_preGiftSuccessGifteeActive, subscriber.getLanguage());

				gifteeSmsText = getSMSTextForID(task,
						PRE_GIFT_GIFTEE_SMS_ALREADY_ACTIVE,
						m_preGiftGifteeSmsActive, subscriber.getLanguage());
			}

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smstext);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID", callerID == null ? "" : callerID);
			hashMap.put("PROMO_ID", promoID);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));

			hashMap.put("CALLER_ID", subscriber.getSubscriberID());
			hashMap.put("SMS_TEXT", gifteeSmsText);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			gifteeSmsText = finalizeSmsText(hashMap);
			task.setObject(param_Sender, param(SMS, SMS_NO, "SMS_NO"));
			task.setObject(param_Reciver, callerID);
			task.setObject(param_Msg, gifteeSmsText);
			sendSMS(task);
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.EXISTS_IN_GIFTEE_LIBRAY)) {
			String smsText = getSMSTextForID(task,
					PRE_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY,
					m_preGiftFailureGiftExistsInGifteeLibrary, subscriber
							.getLanguage());

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("SONG_NAME", songName == null ? "" : songName);
			hashMap.put("CALLER_ID", callerID == null ? "" : callerID);
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.INVALID)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.OWN_NUMBER)
				|| response
						.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NOT_ALLOWED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, subscriber
							.getLanguage()));
			return;
		} else if (response
				.equalsIgnoreCase(com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.BLACK_LISTED)) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFTEE_BLACKLISTED, m_gifteeIdBlacklistedDefault, subscriber
							.getLanguage()));
			return;
		}else
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
	}

	@Override
	public void processPreGiftConfirm(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0) {
			String promoID = smsList.get(0);

			DataRequest dataRequest = new DataRequest(null, subscriberID,
					"PRE_GIFT");
			ViralData[] viralData = RBTClient.getInstance().getViralData(
					dataRequest);
			if (viralData == null) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						PRE_GIFT_FAILURE_NO_PENDING_GIFTS,
						m_preGiftFailureNoPendingGifts, subscriber
								.getLanguage()));
				return;
			}

			Clip clip = rbtCacheManager.getClipByPromoId(promoID);
			Category category = null;
			if (clip == null)
			{
				category = rbtCacheManager.getCategoryByPromoId(promoID);
				if (category == null)
				{
					task.setObject(param_responseSms, getSMSTextForID(task,
							PRE_GIFT_FAILURE_INVALID_PROMO_CODE,
							m_preGiftFailureNoPendingGifts, subscriber
								.getLanguage()));
					return;
				}
			}

			String contentID = null;
			String songName = null;
			if (clip != null)
			{
				contentID = String.valueOf(clip.getClipId());
				songName = clip.getClipName();
			}
			else
			{
				contentID = "C" + String.valueOf(category.getCategoryId());
				songName = category.getCategoryName();
			}

			boolean isPendingRequestFound = false;
			for (ViralData data : viralData) {
				if (contentID.equals(data.getClipID())) {
					String gifterID = data.getSubscriberID();
					isPendingRequestFound = true;
					DataRequest viralDataRequest = new DataRequest(gifterID, subscriberID, "PRE_GIFT");
					viralDataRequest.setSentTime(data.getSentTime());
					viralDataRequest.setNewType("GIFT");

					HashMap<String, String> infoMap = data.getInfoMap();
					if (infoMap == null)
						infoMap = new HashMap<String, String>();

					infoMap.put(GIFTTYPE_ATTR, "direct");
					viralDataRequest.setInfoMap(infoMap);
					RBTClient.getInstance().updateViralData(viralDataRequest);

					String smsText = getSMSTextForID(task,
							PRE_GIFT_CONFIRM_SUCCESS, null, subscriber
									.getLanguage());

					if (smsText != null) {
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put("SMS_TEXT", smsText);
						hashMap.put("CALLER_ID", gifterID);
						hashMap.put("SONG_NAME", songName);
						hashMap.put("CIRCLE_ID", subscriber.getCircleID());
						task.setObject(param_responseSms,
								finalizeSmsText(hashMap));
					} else
						task.setObject(param_responseSms, "");

					break;
				}
			}

			if (!isPendingRequestFound) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						PRE_GIFT_FAILURE_NO_PENDING_GIFTS,
						m_preGiftFailureNoPendingGifts, subscriber
								.getLanguage()));
				return;
			}
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					PRE_GIFT_FAILURE_INVALID_PROMO_CODE,
					m_preGiftFailureNoPendingGifts, subscriber
							.getLanguage()));
			return;
		}
	}

	private boolean isCosSmsKeywordPresent(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0) {
			for (String smsWord : smsList) {
				String prepaidYes = subscriber.isPrepaid() ? "y" : "n";
				CosDetails cosDetail = CacheManagerUtil
						.getCosDetailsCacheManager().getSmsKeywordCosDetail(
								smsWord, subscriber.getCircleID(), prepaidYes);
				if (cosDetail != null) {
					task.setObject(param_cos, cosDetail);
					smsList.remove(smsWord);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void processDiscountedSelection(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (discountedClipIDsList == null) {
			String discountedClipIDsStr = RBTParametersUtils.getParamAsString(
					SMS, "DISCOUNTED_SEL_CLIP_IDS", "");
			String[] clipIDs = discountedClipIDsStr.split(",");
			discountedClipIDsList = Arrays.asList(clipIDs);
		}

		Clip clip = null;
		if (task.getObject(CLIP_OBJ) == null && smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		clip = (Clip) task.getObject(CLIP_OBJ);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (clip != null
				&& discountedClipIDsList.contains(String.valueOf(clip
						.getClipId()))) {
			String chargeClass = RBTParametersUtils.getParamAsString(SMS,
					"DISCOUNTED_SEL_CHARGE_CLASS", null);
			if (chargeClass == null) {
				task
						.setObject(param_responseSms, getSMSTextForID(task,
								HELP_SMS_TEXT, m_helpDefault, subscriber
										.getLanguage()));
				return;
			}
			task.setObject(param_chargeclass, chargeClass);
			task.setObject(param_USE_UI_CHARGE_CLASS, true);

			processActNSel(task);
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DISCOUNTED_SEL_FAILURE, m_discountedSelFailureDefault,
					subscriber.getLanguage()));
		}
	}

	protected boolean populateFromTimeAndToTime(Task task) {
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		int fromTimeHrs = -1;
		int toTimeHrs = -1;
		int fromTimeMins = -1;
		int toTimeMins = -1;

		try {
			boolean isTODTokenPresent = false;
			Iterator<String> iter = smsList.iterator();
			while (iter.hasNext()) {
				String smsToken = iter.next();
				if (smsToken.contains(":")) {
					isTODTokenPresent = true;
					if (fromTimeHrs == -1) {
						String[] fromTimeTokens = smsToken.split(":");
						fromTimeHrs = Integer.parseInt(fromTimeTokens[0]);
						fromTimeMins = Integer.parseInt(fromTimeTokens[1]);
					} else {
						String[] toTimeTokens = smsToken.split(":");
						toTimeHrs = Integer.parseInt(toTimeTokens[0]);
						toTimeMins = Integer.parseInt(toTimeTokens[1]);
					}
					// Removes the fromTime and toTime tokens from the smsList
					iter.remove();
				}

			}

			if (fromTimeHrs != -1 && toTimeHrs != -1 && fromTimeMins != -1
					&& toTimeMins != -1 && fromTimeHrs >= 0
					&& fromTimeHrs <= 23 && toTimeHrs >= 0 && toTimeHrs <= 24
					&& fromTimeMins >= 0 && fromTimeMins <= 59
					&& toTimeMins >= 0 && toTimeMins <= 59
					&& (toTimeHrs != 24 || toTimeMins == 0)) {
				if (toTimeHrs == 24 && toTimeMins == 0) {
					toTimeHrs = 23;
					toTimeMins = 59;
				}
				
				if (task.containsKey("isTODSettingRequest")) {
					if (fromTimeHrs > toTimeHrs
							|| (fromTimeHrs >= toTimeHrs && fromTimeMins > toTimeMins))
						return false;
				}

				task.setObject(param_fromTime, String.valueOf(fromTimeHrs));
				if(fromTimeMins == 0){
					task.setObject(param_fromTimeMins, "00");
				}else{
					task.setObject(param_fromTimeMins, String.valueOf(fromTimeMins));
				}
				
				task.setObject(param_toTime, String.valueOf(toTimeHrs));
				
				if(toTimeMins == 0){
				    task.setObject(param_toTimeMins, "00");
				}else{
					task.setObject(param_toTimeMins, String.valueOf(toTimeMins));
				}
				
				task.setObject(param_status, String.valueOf(80));

				return true;
			} else if (isTODTokenPresent)
				return false;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return true;
	}

	@Override
	public void processInfluencerOptin(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberId = subscriber.getSubscriberID();
		CallGraph callGraph = CallGraphDao.getBySubscriberID(subscriberId);
		if (callGraph == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					CONTEST_INFLUENCER_NOT_FOUND,
					m_contestInfluencerNotFoundDefault, subscriber
							.getLanguage()));
			return;
		} else if (callGraph.isConfirmedForPromotion()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					CONTEST_INFLUENCER_ALREADY_CONFIRMED,
					m_contestInfluencerAlreadyConfirmedDefault, subscriber
							.getLanguage()));

		}
		callGraph.setPromotionStatus(PromotionStatus.CONFIRMED);
		callGraph.setPromotionConfirmedTime(new Date());
		CallGraphDao.update(callGraph);
		task.setObject(param_responseSms, getSMSTextForID(task,
				CONTEST_INFLUENCER_SUCCESS, m_contestInfluencerSuccessDefault,
				subscriber.getLanguage()));
	}

	@Override
	public void processChargingConsentRequest(Task task) {
		boolean consent = (task.getTaskAction().equals(CONSENT_YES_KEYWORD));
		String consentString = consent ? "YES" : "NO";

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			logger.info("Inactive user");
			task.setObject(param_responseSms, getSMSTextForID(task, "CONSENT_"
					+ consentString + "_REQUEST_INACTIVE_USER", "CONSENT_"
					+ consentString + "_REQUEST_INACTIVE_USER", subscriber
					.getLanguage()));
			return;
		}

		String subscriberID = subscriber.getSubscriberID();

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() == 0) {
			// Base Consent Request
			String srvKey = "RBT_ACT_" + subscriber.getSubscriptionClass();
			String refID = subscriber.getRefID();

			String response = Utility.hitConsentRequestToSM(subscriberID,
					srvKey, refID, consent);

			task.setObject(param_responseSms, getSMSTextForID(task, "CONSENT_"
					+ consentString + "_REQUEST_BASE_" + response, "CONSENT_"
					+ consentString + "_REQUEST_BASE_" + response, subscriber
					.getLanguage()));

			return;
		}

		Category category = null;
		String promoID = smsList.get(0).trim();
		Clip clip = getClipByPromoId(promoID, subscriber.getLanguage());
		if (clip == null) {
			category = getCategoryByPromoId(promoID, subscriber.getLanguage());
		}

		if (clip == null && category == null) {
			task.setObject(param_responseSms, getSMSTextForID(task, "CONSENT_"
					+ consentString + "_REQUEST_INVALID_CONTENT", "CONSENT_"
					+ consentString + "_REQUEST_INVALID_CONTENT", subscriber
					.getLanguage()));
			return;
		}

		String smsText = null;
		boolean isDownloadsModel = RBTParametersUtils.getParamAsBoolean(
				"COMMON", "ADD_TO_DOWNLOADS", "FALSE");
		if (isDownloadsModel) {
			// Download Consent Request
			Downloads downloadsObj = rbtClient
					.getDownloads(new RbtDetailsRequest(subscriberID));
			Download[] downloads = null;
			if (downloadsObj != null)
				downloads = downloadsObj.getDownloads();

			Download consentForDownload = null;
			if (downloads != null) {
				for (Download download : downloads) {
					if ((category != null
							&& com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isShuffleCategory(category
											.getCategoryTpe()) && category
							.getCategoryId() == download.getCategoryID())
							|| (clip != null && clip.getClipId() == download
									.getToneID())) {
						consentForDownload = download;
						break;
					}
				}
			}

			if (consentForDownload == null) {
				logger.info("Subscriber does not have downloads");
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", subscriber.getLanguage());
			} else {
				String srvKey = "RBT_SEL_"
						+ consentForDownload.getChargeClass();
				String refID = consentForDownload.getRefID();

				String response = Utility.hitConsentRequestToSM(subscriberID,
						srvKey, refID, consent);
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_DOWNLOAD_" + response, "CONSENT_"
						+ consentString + "_REQUEST_DOWNLOAD_" + response,
						subscriber.getLanguage());
			}
		} else {
			// Selection Consent Request
			Library library = rbtClient
					.getLibraryHistory(new RbtDetailsRequest(subscriberID));
			Settings settingsObj = null;
			if (library != null)
				settingsObj = library.getSettings();
			Setting[] settings = null;
			if (settingsObj != null)
				settings = settingsObj.getSettings();

			String response = null;
			if (settings != null) {
				for (Setting setting : settings) {
					String selectionStatus = setting.getSelectionStatus();
					if ((!selectionStatus
							.equals(WebServiceConstants.DEACT_PENDING) && !selectionStatus
							.equals(WebServiceConstants.DEACTIVE))
							&& ((category != null
									&& com.onmobile.apps.ringbacktones.webservice.common.Utility
											.isShuffleCategory(category
													.getCategoryTpe()) && category
									.getCategoryId() == setting.getCategoryID()) || (clip != null && clip
									.getClipId() == setting.getToneID()))) {
						String srvKey = "RBT_SEL_" + setting.getChargeClass();
						String refID = setting.getRefID();

						String tempResponse = Utility.hitConsentRequestToSM(
								subscriberID, srvKey, refID, consent);
						if (response == null || tempResponse.equals(SUCCESS)
								|| response.equals(ERROR))
							response = tempResponse;
					}
				}
			}

			if (response == null) {
				logger.info("Subscriber does not have selections");
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", subscriber.getLanguage());
			} else {
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_DOWNLOAD_" + response, "CONSENT_"
						+ consentString + "_REQUEST_DOWNLOAD_" + response,
						subscriber.getLanguage());
			}
		}

		String contentName = "";
		if (clip != null)
			contentName = clip.getClipName();
		else if (category != null)
			contentName = category.getCategoryName();

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	@Override
	public void processCPSelectionConfirm(Task task) {
		String categoryId = "3";
		Subscriber subscriber = getSubscriber(task);
		Category category = getCategory(3, subscriber.getLanguage());

		task.setObject(param_catid, categoryId);
		task.setObject(CAT_OBJ, category);

		String cpMode = task.getString(param_MODE);
		if (cpMode == null) {
			logger.info("CP mode is not passed, so not processing the request");
			task.setObject(param_responseSms, getSMSTextForID(task,
					CP_SEL_INTERNAL_ERROR, m_cpSelectionInternalErrorDefault,
					subscriber.getLanguage()));
			return;
		}

		String clipID = null;
		String cpModeClipIDMappingStr = RBTParametersUtils.getParamAsString(
				"SMS", "CP_MODE_CLIPID_MAPPING", null);
		if (cpModeClipIDMappingStr == null) {
			logger
					.info("CP short code clipID mapping not present, so not processing the request");
			task.setObject(param_responseSms, getSMSTextForID(task,
					CP_SEL_INTERNAL_ERROR, m_cpSelectionInternalErrorDefault,
					subscriber.getLanguage()));
			return;
		}

		String[] cpModeClipIDs = cpModeClipIDMappingStr.split(",");
		if (cpModeClipIDs != null) {
			for (String eachMapping : cpModeClipIDs) {
				String[] tokens = eachMapping.split(":");
				if (cpMode.equalsIgnoreCase(tokens[0].trim())) {
					clipID = tokens[1].trim();
					break;
				}
			}
		}

		if (clipID != null) {
			Clip clip = getClipById(clipID, subscriber.getLanguage());

			task.setObject(CLIP_OBJ, clip);
			if (clip != null)
				task.setObject(param_clipid, String.valueOf(clip.getClipId()));

			if (param(COMMON, ALLOW_LOOPING, false)
					&& param(COMMON, ADD_SEL_TO_LOOP, false))
				task.setObject(param_inLoop, "YES");

			String response = processSetSelection(task);

			setResponseSmsFromSelectionResponse(task, response, false, false);
		} else {
			logger
					.info("CP short code clipID mapping not present for the short code : "
							+ cpMode);
			task.setObject(param_responseSms, getSMSTextForID(task,
					CP_SEL_INTERNAL_ERROR, m_cpSelectionInternalErrorDefault,
					subscriber.getLanguage()));
			return;
		}
	}

	@Override
	public void processVoucherRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String voucherConfig = RBTParametersUtils.getParamAsString(SMS,
				"VOUCHER_CONFIG", null);
		if (voucherConfig == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VOUCHER_INTERNAL_ERROR, m_voucherInternalErrorDefault,
					subscriber.getLanguage()));
			return;
		}

		String[] config = voucherConfig.split(",");
		String subscriptionClass = config[0];
		String clipID = null;
		String chargeClass = null;
		if (config.length > 1)
			clipID = config[1];

		if (config.length > 2)
			chargeClass = config[2];

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() < 1) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VOUCHER_FAILURE, m_voucherFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		String voucherID = smsList.get(0).trim();
		TransData transData = RBTDBManager.getInstance()
				.getTransDataAndUpdateAccessCount(voucherID, "VOUCHER");
		if (transData == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VOUCHER_FAILURE, m_voucherFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		boolean isVoucherUsed = false;
		if (!isUserActive(subscriber.getStatus())) {
			logger.info("Inactive user");
			task.setObject(param_subclass, subscriptionClass);
			if (clipID != null) {
				Clip clip = getClipById(clipID, subscriber.getLanguage());
				if (clip == null
						|| clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					logger
							.warn("ClipID configured in not present in DB or expired.");
					task.setObject(param_responseSms, getSMSTextForID(task,
							VOUCHER_INTERNAL_ERROR,
							m_voucherInternalErrorDefault, subscriber
									.getLanguage()));
					return;
				}

				task.setObject(CLIP_OBJ, clip);
				task.setObject(param_clipid, String.valueOf(clip.getClipId()));

				if (chargeClass != null) {
					task.setObject(param_chargeclass, chargeClass);
					task.setObject(param_USE_UI_CHARGE_CLASS, true);
				}

				String response = processSetSelection(task);

				setResponseSmsFromSelectionResponse(task, response, false,
						false);
				if (response.equalsIgnoreCase("success")) {
					RBTDBManager.getInstance().removeTransData(voucherID,
							"VOUCHER");
					isVoucherUsed = true;
				}
			} else {
				subscriber = processActivation(task);

				if (subscriber != null && isUserActive(subscriber.getStatus())) {
					RBTDBManager.getInstance().removeTransData(voucherID,
							"VOUCHER");
					isVoucherUsed = true;

					task.setObject(param_responseSms, getSMSTextForID(task,
							ACTIVATION_SUCCESS, m_activationSuccessDefault,
							subscriber.getLanguage()));
				} else {
					task.setObject(param_responseSms, getSMSTextForID(task,
							HELP_SMS_TEXT, m_helpDefault, null));
				}
			}
		} else {
			task.setObject(param_subclass, subscriptionClass);

			String response = processUpgradeValidity(task);
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						VOUCHER_UPGRADE_SUCCESS,
						m_voucherUpgradeSuccessDefault, subscriber
								.getLanguage()));

				RBTDBManager.getInstance()
						.removeTransData(voucherID, "VOUCHER");
				isVoucherUsed = true;
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						VOUCHER_UPGRADE_FAILURE,
						m_voucherUpgradeFailureDefault, subscriber
								.getLanguage()));
			}
		}

		if (!isVoucherUsed) {
			RBTDBManager.getInstance().updateTransData(voucherID, "VOUCHER",
					transData.subscriberID(), transData.transDate(), "0");
		} else if (subscriber != null) {
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(subscriber.getSubscriberID()).append(", ")
					.append(voucherID);
			RBTEventLogger.logEvent(RBTEventLogger.Event.VOUCHER, logBuilder
					.toString());
		}
	}
    /**
     * @author deepak.kumar
     * JIRA-4680 = FOR UPDATING_DND_OF_SUBSCRIBER_WITH_SM
     */
	@Override
	public void processOptOutRequest(Task task){
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(!isUserActive(subscriber.getStatus())){
			task.setObject(param_responseSms,TECHNICAL_FAILURE);
			return;
		}
		
		String srv_key = (String)task.getTaskSession().get(param_srvkey);
		String action = (String)task.getTaskSession().get(param_action.toUpperCase());
		String dND_Url = getParameter(SMS,URL_FOR_UPDATING_DND_OF_SUBSCRIBER_WITH_SM);
		
		if(dND_Url!=null){
			dND_Url =dND_Url.replaceAll("%msisdn%", subscriber.getSubscriberID());
			dND_Url =dND_Url.replaceAll("%srvkey%", srv_key);
			dND_Url =dND_Url.replaceAll("%action%", action);
						
		}
		logger.info("dND_Url: " + dND_Url +"srv_key="+srv_key + "action ="+action);
		String response = null;
		try {
			// Setting HttpParameters
			HttpParameters httpParam = new HttpParameters();
			httpParam.setUrl(dND_Url);
			httpParam.setConnectionTimeout(6000);
	
			// Setting request Params
			HashMap<String, String> params = new HashMap<String, String>();
	
			logger.info("dND_Url: " + dND_Url
					+ ". Parameters: " + params.toString());
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParam, params);
			response = httpResponse.getResponse();
			logger.info("Response is " + response);
	
			task.setObject(param_responseSms,response);
		}catch(Exception e){
			logger.error("Exception Caught:"+e);
			task.setObject(param_responseSms,TECHNICAL_FAILURE);
		}
	}
	/**
	 * @author deepak.kumar
	 * JIRAID-4843 = SMS BASE UPGRADATION
	 */
	@Override
	public void processBaseUpgradationRequest(Task task){
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(!isUserActive(subscriber.getStatus())){
			task.setObject(param_responseSms, getSMSTextForID(task,
					USER_NOT_ACTIVE, m_userNotActiveDefault, subscriber
							.getLanguage()));
			return;
		}
		String tobeUpdateSubClass = null;
        String confSubClass = getParameter("SMS", "BASE_UPGRADTION_KEYWORD_SUBCLASS_MAPPING");
        if(confSubClass == null){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND, m_baseUpgradeSubClassKeywordMapNotFound, subscriber
							.getLanguage()));

			return;

        }
        StringTokenizer st = new StringTokenizer(confSubClass,";");
		while (st.hasMoreTokens()) {
			StringTokenizer stk = new StringTokenizer(st.nextToken(), "=");
			String smsWordsStr = task.getString(param_smsSent);
			String[] smsWords = smsWordsStr != null ? smsWordsStr.split(" ")
					: null;
			String keyWord = stk.nextToken();

			if (smsWords != null && smsWords.length > 0) {
				for (String tempWord : smsWords) {
					if (tempWord.contains(keyWord)) {
						tobeUpdateSubClass = stk.nextToken();
						break;
					}
				}
			}
		}
        
        if(tobeUpdateSubClass == null){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND, m_baseUpgradeSubClassKeywordMapNotFound, subscriber
							.getLanguage()));

			return;

        }
		String subClass = subscriber.getSubscriptionClass();
		if(subClass.equalsIgnoreCase(tobeUpdateSubClass)){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SAME_SUB_CLASS, m_baseUpgradeSameSubClass, subscriber
							.getLanguage()));

			return;
		}
		SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager()
		                                     .getSubscriptionClass(tobeUpdateSubClass.toUpperCase());
		if(subscriptionClass==null){
			task.setObject(param_responseSms,TECHNICAL_FAILURE);
			return;
		}
		task.setObject(param_subclass.toUpperCase(),tobeUpdateSubClass);
		String response = upgradeBasePackOfSubscriber(task);
		if (response.equalsIgnoreCase("SUCCESS"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUCCESS, m_baseUpgradeSuccessDefault, subscriber
							.getLanguage()));
		else if(response.equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, subscriber.getLanguage()));
		}else if(response.equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, subscriber.getLanguage()));
		}else
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_FAILURE, m_baseUpgradeFailureDefault, subscriber
							.getLanguage()));
	}

	public void processTimeBasedSettingRequest(Task task){
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String smsSent = task.getString(param_smsSent);
		String smsTokens[] = smsSent.split(" ");
		if(smsTokens.length>=4){
			if(!smsTokens[2].contains(":") || !smsTokens[3].contains(":")){
				task.setObject(param_responseSms, getSMSTextForID(task,
						TIME_OF_DAY_FAILURE, m_timeOfTheDayFailureDefault,
						subscriber.getLanguage()));
				return;
			}
			try{
				task.setObject(param_callerid,smsTokens[4]);
			}catch(Exception ex){ 
				
			}

			task.setObject("isTODSettingRequest", "true");
			processActNSel(task);
		}else
			task.setObject(param_responseSms, getSMSTextForID(task,
					TIME_OF_DAY_FAILURE, m_timeOfTheDayFailureDefault,
					subscriber.getLanguage()));

		
	}

	/*
	 * It is for the churn offer through SMS.If any SMS_CHURN_OFFER smstype
	 * entry in Viral sms table, then It would Upgrade pack of the subscriber if
	 * Subscriber responds with SMS_TEXT =
	 * <keyword><space><msisdn><space><serial no of offer> otherwise if the
	 * sms_text is <keyword><space><msisdn> , then it wd deactivate the
	 * subscriber.
	 */
	
	public void processSMSChurnOfferOrDeact(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String smsSent = task.getString(param_smsSent);
		String smsTokens[] = smsSent.split(" ");
		String subscriberID = task.getString(param_subscriberID);
		String mode = task.getString(param_mode);
		int serialNo = -1;
		try {
			serialNo = Integer.parseInt(smsTokens[1]);
		} catch (Exception ex) {
			serialNo = -1;
			logger.info("Exception while parsing offer no...Going for deactivation");
		}
		ViralData viralData[] = null;
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriberID);
		subscriptionRequest.setMode(mode);
		if (serialNo != -1) {
			task.setObject(param_sms_type, "SMS_CHURN_OFFER");
			viralData = getViraldata(task);
		}

		String response = ERROR;
		if (viralData != null && viralData.length > 0) {
			String clipID = viralData[0].getClipID();
			String offers[] = clipID != null ? clipID.split(",") : null;
			if (offers != null && serialNo <= offers.length) {
				String rentalPack = offers[serialNo - 1];
				subscriptionRequest.setRentalPack(rentalPack);
				subscriptionRequest.setBIOffer(true);
				RBTClient.getInstance().activateSubscriber(subscriptionRequest);
				response = subscriptionRequest.getResponse();
			}
			task.setObject(param_response, response);
		} else {
			RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			task.setObject(param_response, response);
		}
		
		response = task.getString(param_response);
		if(response!=null && response.indexOf("success")!=-1){
			removeViraldata(task);
			task.setObject(param_responseSms, getSMSTextForID(task,
					CHURN_OFFER_SUCCESS, m_smsChurnOfferSuccess, subscriber.getLanguage()));
		}else{
			task.setObject(param_responseSms, getSMSTextForID(task,
					CHURN_OFFER_FAILURE, m_smsChurnOfferFailure, subscriber.getLanguage()));
		}
		logger.info("Response from processSMSChurnOfferOrDeact = "+response);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processBaseSongUpgradationRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 * @support Base and Song upgradation. If base is upgrade successfully, then in base upgradation success callback, song will get upgrade
	 */
	public void processBaseSongUpgradationRequest(Task task){

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if(!isUserActive(subscriber.getStatus())){
			task.setObject(param_responseSms, getSMSTextForID(task,
					USER_NOT_ACTIVE, m_userNotActiveDefault, subscriber
							.getLanguage()));
			return;
		}
		String tobeUpdateSubClass = null;
        String confSubChargeClass = getParameter("SMS", "BASE_SONG_UPGRADTION_KEYWORD_SUBCLASS_CHARGECLASS_MAPPING");
        if(confSubChargeClass == null){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND, m_baseUpgradeSubClassKeywordMapNotFound, subscriber
							.getLanguage()));

			return;

        }
        StringTokenizer st = new StringTokenizer(confSubChargeClass,";");
        String upgradeKeyword = null;
    	while(st.hasMoreTokens()){
        	StringTokenizer stk = new StringTokenizer(st.nextToken(),":");
        	ArrayList<String> smsWords = (ArrayList<String>)task.getObject(param_smsText);
        	upgradeKeyword = stk.nextToken();
        	if(smsWords == null || smsWords.size() == 0) {
        		smsWords = new ArrayList<String>();
        		smsWords.add("DEFAULT");
        	}
        	boolean isUpdateSubClass = false;
        	if(upgradeKeyword!=null) {
        		isUpdateSubClass = smsWords.contains(upgradeKeyword)||smsWords.contains(upgradeKeyword.toLowerCase());
        	}
        	if(smsWords!=null && isUpdateSubClass){
        		tobeUpdateSubClass = stk.nextToken();
        	}
        }
        
        if(tobeUpdateSubClass == null){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND, m_baseUpgradeSubClassKeywordMapNotFound, subscriber
							.getLanguage()));

			return;

        }
		String subClass = subscriber.getSubscriptionClass();
		if(subClass.equalsIgnoreCase(tobeUpdateSubClass)){
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SAME_SUB_CLASS, m_baseUpgradeSameSubClass, subscriber
							.getLanguage()));

			return;
		}
		SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager()
		                                     .getSubscriptionClass(tobeUpdateSubClass.toUpperCase());
		if(subscriptionClass==null){
			task.setObject(param_responseSms,TECHNICAL_FAILURE);
			return;
		}
		task.setObject(param_subclass.toUpperCase(),tobeUpdateSubClass);
		task.setObject("upgrade_keyword", upgradeKeyword);
		String response = upgradeBasePackOfSubscriber(task);
		if (response.equalsIgnoreCase("SUCCESS"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_SUCCESS, m_baseUpgradeSuccessDefault, subscriber
							.getLanguage()));
		else if(response.equals(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "TNB_SONG_SELECTON_NOT_ALLOWED",
					m_TnbSongSelectionNotAllowed, subscriber.getLanguage()));
		}else if(response.equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			task.setObject(param_responseSms, getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
					m_UpgradeNotAllowed, subscriber.getLanguage()));
		}else
			task.setObject(param_responseSms, getSMSTextForID(task,
					BASE_UPGRADE_FAILURE, m_baseUpgradeFailureDefault, subscriber
							.getLanguage()));
	}
	
	protected static String upgradeBasePackOfSubscriber(Task task)
	{
    	SubscriptionRequest subscriptionRequest = new SubscriptionRequest(task.getString(param_subscriberID));
        subscriptionRequest.setRentalPack(task.getString(param_subclass.toUpperCase()));
        subscriptionRequest.setMode(task.getString(param_MODE));
        if(task.getObject(param_cosid) != null){
        subscriptionRequest.setCosID(Integer.parseInt(task.getString(param_cosid)));
        }
        rbtClient.activateSubscriber(subscriptionRequest);
	    return subscriptionRequest.getResponse();

	}

	@Override
	public void processUpgradeSelRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					USER_NOT_ACTIVE, m_userNotActiveDefault, subscriber
							.getLanguage()));
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() == 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_UPGRADE_SEL, m_helpUpgradeSelDefault, subscriber
							.getLanguage()));
			return;
		}

		String promoID = smsList.get(0);
		Clip clip = rbtCacheManager.getClipByPromoId(promoID, subscriber
				.getLanguage());
		if (clip == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		task.setObject(param_SUBID, subscriber.getSubscriberID());
		task.setObject(param_CLIPID, String.valueOf(clip.getClipId()));

		String response = upgradeSelectionPack(task);

		if (response.equalsIgnoreCase("SUCCESS"))
			task.setObject(param_responseSms, getSMSTextForID(task,
					SEL_UPGRADE_SUCCESS, m_selUpgradeSuccessDefault, subscriber
							.getLanguage()));
		else
			task.setObject(param_responseSms, getSMSTextForID(task,
					SEL_UPGRADE_FAILURE, m_selUpgradeFailureDefault, subscriber
							.getLanguage()));
	}

	/*
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processGiftAccept
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task) Process accept
	 * gift request
	 */
	@Override
	public void processGiftAccept(Task task) {

		String response = "Error";
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String promoId = null;
		if (smsList != null && smsList.size() > 0) {
			promoId = smsList.get(0);
		}

		if (promoId != null) {
			getCategoryAndClipForPromoID(task, promoId);
		}

		Gift gift = getLatestGiftFromGiftInbox(task, promoId);

		String smsText = null;
		String language = subscriber.getLanguage();
		if (promoId != null && gift == null) {
			logger.info("There is no gift for this promoId " + promoId
					+ "subscriberId: " + subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, GIFT_INVALID_CODE_FAILURE,
					m_giftInvalidCodeDefault, language);
		} else if (gift == null) {
			logger.info("There is no gift in gift inbox subscriberId: "
					+ subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, NO_GIFT_INBOX,
					m_giftNoInboxDefault, language);
		}

		if (gift == null) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("KEYWORD", getParameter(SMS, GIFT_ACCEPT_KEYWORD));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		}

		SelectionRequest selectionRequest = new SelectionRequest(subscriber
				.getSubscriberID());
		selectionRequest.setGifterID(gift.getSender());
		selectionRequest.setGiftSentTime(gift.getSentTime());
		selectionRequest.setCategoryID("" + gift.getCategoryID());
		selectionRequest.setClipID("" + gift.getToneID());
		selectionRequest.setMode("SMS");
		rbtClient.acceptGift(selectionRequest);
		response = selectionRequest.getResponse();

		logger.info("Gfit Accept Response: " + response);
		if (response.equalsIgnoreCase(SUCCESS)) {
			/*
			 * Get the configured text for the GIFT_ACCEPT_SUCCESS, if no
			 * configuration found then it will take the system default message.
			 */
			String defaultConfTextForGiftAcceptSuccess = getSMSTextForID(task,
					GIFT_ACCEPT_SUCCESS, m_giftAcceptSuccessDefult, language);
			/*
			 * Get the configured text for the GIFT_ACCEPT_SUCCESS_<responses>,
			 * if that message is not configured then it will take the above
			 * default message.
			 */
			StringBuffer confForGiftAcceptSuccess = new StringBuffer(
					GIFT_ACCEPT_SUCCESS);
			confForGiftAcceptSuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			smsText = getSMSTextForID(task,
					confForGiftAcceptSuccess.toString(),
					defaultConfTextForGiftAcceptSuccess, language);
		} else {
			smsText = getSMSTextForID(task, GIFT_ACCEPT_FAILURE,
					m_giftAcceptFailureDefult, language);
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		Category category = rbtCacheManager.getCategory(gift.getCategoryID());
		if (category != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getCategoryTpe())) {
			hashMap.put("CODE", category.getCategoryPromoId());
			hashMap.put("SONG_NAME", category.getCategoryName());
		} else {
			Clip clip = rbtCacheManager.getClip(gift.getToneID());
			hashMap.put("CODE", clip.getClipPromoId());
			hashMap.put("SONG_NAME", clip.getClipName());
		}
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	/*
	 * process sms request to reject gift
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processGiftReject
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processGiftReject(Task task) {
		String response = "Error";
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String promoId = null;
		if (smsList != null && smsList.size() > 0) {
			promoId = smsList.get(0);
		}

		if (promoId != null) {
			getCategoryAndClipForPromoID(task, promoId);
		}

		Gift gift = getLatestGiftFromGiftInbox(task, promoId);

		String smsText = null;
		if (promoId != null && gift == null) {
			logger.info("There is no gift for this promoId " + promoId
					+ "subscriberId: " + subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, GIFT_INVALID_CODE_FAILURE,
					m_giftInvalidCodeDefault, subscriber.getLanguage());
		} else if (gift == null) {
			logger.info("There is no gift in gift inbox subscriberId: "
					+ subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, NO_GIFT_INBOX,
					m_giftNoInboxDefault, subscriber.getLanguage());
		}

		if (gift == null) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("KEYWORD", getParameter(SMS, GIFT_REJECT_KEYWORD));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		}

		GiftRequest giftRequest = new GiftRequest();
		giftRequest.setGifterID(gift.getSender());
		giftRequest.setGifteeID(gift.getReceiver());
		giftRequest.setGiftSentTime(gift.getSentTime());
		giftRequest.setMode("SMS");
		rbtClient.rejectGift(giftRequest);
		response = giftRequest.getResponse();

		logger.info("Gfit Accept Response: " + response);
		if (response.equalsIgnoreCase(SUCCESS)) {
			smsText = getSMSTextForID(task, GIFT_REJECT_SUCCESS,
					m_giftRejectSuccessDefult, subscriber.getLanguage());
		} else {
			smsText = getSMSTextForID(task, GIFT_REJECT_FAILURE,
					m_giftRejectFailureDefult, subscriber.getLanguage());
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		Category category = rbtCacheManager.getCategory(gift.getCategoryID());
		if (category != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getCategoryTpe())) {
			hashMap.put("CODE", category.getCategoryPromoId());
			hashMap.put("SONG_NAME", category.getCategoryName());
		} else {
			Clip clip = rbtCacheManager.getClip(gift.getToneID());
			hashMap.put("CODE", clip.getClipPromoId());
			hashMap.put("SONG_NAME", clip.getClipName());
		}
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	/*
	 * Download gift feature is not available in SMS process sms request to
	 * download gift
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processGiftDownload
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processGiftDownload(Task task) {
		String response = "Error";
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String promoId = null;
		if (smsList != null && smsList.size() > 0) {
			promoId = smsList.get(0);
		}

		if (promoId != null) {
			getCategoryAndClipForPromoID(task, promoId);
		}

		Gift gift = getLatestGiftFromGiftInbox(task, promoId);

		String smsText = null;
		String language = subscriber.getLanguage();
		if (promoId != null && gift == null) {
			logger.info("There is no gift for this promoId " + promoId
					+ "subscriberId: " + subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, GIFT_INVALID_CODE_FAILURE,
					m_giftInvalidCodeDefault, language);
		} else if (gift == null) {
			logger.info("There is no gift in gift inbox subscriberId: "
					+ subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, NO_GIFT_INBOX,
					m_giftNoInboxDefault, language);
		}

		if (gift == null) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("KEYWORD", getParameter(SMS, GIFT_DOWNLOAD_KEYWORD));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
			return;
		}

		SelectionRequest selectionRequest = new SelectionRequest(subscriber
				.getSubscriberID());
		selectionRequest.setGifterID(gift.getSender());
		selectionRequest.setGiftSentTime(gift.getSentTime());
		selectionRequest.setCategoryID("" + gift.getCategoryID());
		selectionRequest.setClipID("" + gift.getToneID());
		rbtClient.downloadGift(selectionRequest);
		response = selectionRequest.getResponse();

		logger.info("Gift Download Response: " + response);
		if (response.equalsIgnoreCase(SUCCESS)) {

			/*
			 * Get the configured text for the GIFT_DOWNLOAD_SUCCESS, if no
			 * configuration found then it will take the system default message.
			 */
			String defaultConfTextForGiftDownloadSuccess = getSMSTextForID(
					task, GIFT_DOWNLOAD_SUCCESS, m_giftDownloadSuccessDefult,
					language);
			/*
			 * Get the configured text for the
			 * GIFT_DOWNLOAD_SUCCESS_<responses>, if that message is not
			 * configured then it will take the above default message.
			 */
			StringBuffer confForGiftDownloadSuccess = new StringBuffer(
					GIFT_DOWNLOAD_SUCCESS);
			confForGiftDownloadSuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			smsText = getSMSTextForID(task, confForGiftDownloadSuccess
					.toString(), defaultConfTextForGiftDownloadSuccess,
					language);
		} else {
			smsText = getSMSTextForID(task, GIFT_DOWNLOAD_FAILURE,
					m_giftDownloadFailureDefult, language);
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		Category category = rbtCacheManager.getCategory(gift.getCategoryID());
		if (category != null
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getCategoryTpe())) {
			hashMap.put("CODE", category.getCategoryPromoId());
			hashMap.put("SONG_NAME", category.getCategoryName());
		} else {
			Clip clip = rbtCacheManager.getClip(gift.getToneID());
			hashMap.put("CODE", clip.getClipPromoId());
			hashMap.put("SONG_NAME", clip.getClipName());
		}
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	public void processLotteryListRequest(Task task)
	{
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					LOTTERY_LIST_INACTIVE_USER, getSMSTextForID(task, HELP_SMS_TEXT,
							m_helpDefault, subscriber.getLanguage()),
							subscriber.getLanguage()));
			return;
		}

		String lotteryIdsStr = RBTParametersUtils.getParamAsString("DAEMON", "LOTTERY_IDS_FOR_EACH_DOWNLOAD", null);
		String[] lotteryIds = lotteryIdsStr.split(",");
		List<String> lotteryIDsList = Arrays.asList(lotteryIds);
		String nonMasterLotteryID = null;
		if (lotteryIdsStr.contains("!"))
		{
			int index = lotteryIdsStr.indexOf(",", lotteryIdsStr.indexOf("!"));
			if (index == -1)
				nonMasterLotteryID = lotteryIdsStr.substring(lotteryIdsStr.indexOf("!"));
			else
				nonMasterLotteryID = lotteryIdsStr.substring(lotteryIdsStr.indexOf("!"), index);
		}

		Map<Integer, String> lotteriesMap = new HashMap<Integer, String>();
		RBTLotteryEntries[] rbtLotteryEntries = RBTDBManager.getInstance().getLotteryEntriesBySubscriberID(subscriberID);
		if (rbtLotteryEntries != null) {
			String lotteryText = null;
			for (RBTLotteryEntries rbtLotteryEntry : rbtLotteryEntries) {
				if (rbtLotteryEntry.lotteryID() == -1 || rbtLotteryEntry.lotteryNumber() == null)
					continue;

				Clip clip = rbtCacheManager.getClip(rbtLotteryEntry.clipID());
				lotteryText = lotteriesMap.get(rbtLotteryEntry.clipID());
				if (lotteryText == null)
				{
					lotteryText = getSMSTextForID(task, LOTTERY_LIST_SUCCESS,
						m_lotteryListSuccessDefault, subscriber.getLanguage());
				}

				if (lotteryIDsList.contains(String.valueOf(rbtLotteryEntry.lotteryID())))
					lotteryText = lotteryText.replaceAll("%LOTTERYNUMBER_" + String.valueOf(rbtLotteryEntry.lotteryID()) , rbtLotteryEntry.lotteryNumber());
				else
					lotteryText = lotteryText.replaceAll("%LOTTERYNUMBER_" + nonMasterLotteryID , rbtLotteryEntry.lotteryNumber());

				lotteryText = lotteryText.replaceAll("%SONGNAME", (clip != null) ? clip.getClipName() : "");
				lotteryText = lotteryText.replaceAll("%ALBUM", (clip != null && clip.getAlbum() != null) ? clip.getAlbum() : "");
				lotteryText = lotteryText.replaceAll("%ARTIST", (clip != null && clip.getArtist() != null) ? clip.getArtist() : "");

				lotteriesMap.put(rbtLotteryEntry.clipID(), lotteryText);
			}
		}
		else
		{
			task.setObject(param_responseSms,
					getSMSTextForID(task, LOTTERY_LIST_NO_ENTRIES, m_lotteryListNoEntriesDefault,
							subscriber.getLanguage()));
			return;
		}

		Set<Entry<Integer, String>> entriesSet = lotteriesMap.entrySet();
		int count = 0;
		String smsText = null;
		for (Entry<Integer, String> entry : entriesSet)
		{
			count++;
			if (count == 1)
			{
				smsText = entry.getValue();
				continue;
			}

			task.setObject(param_Sender, RBTParametersUtils
					.getParamAsString("DAEMON",
							"LOTTERY_SMS_SENDER_NO", "12345"));
			task.setObject(param_Reciver, subscriberID);
			task.setObject(param_Msg, entry.getValue());
			sendSMS(task);
		}
		task.setObject(param_responseSms, smsText);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processViralOptOutRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processViralOptOutRequest(Task task)
	{
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();

		task.setObject(param_SMSTYPE, "VIRAL_OPTOUT");
		ViralData context[] = getViraldata(task);
		if (context == null || context.length < 1 || context[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_OPTOUT_FAILURE, m_viralOptOutFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}

		String response = updateViraldataType("VIRAL_EXPIRED", subscriberID, "VIRAL_OPTOUT");
		if (response.equalsIgnoreCase("SUCCESS"))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
				VIRAL_OPTOUT_SUCCESS, m_viralOptOutSuccessTextDefault, subscriber
						.getLanguage()));
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_OPTOUT_FAILURE, m_viralOptOutFailureTextDefault, subscriber
							.getLanguage()));
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDownloadSetRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processDownloadSetRequest(Task task)
	{
		Subscriber subscriber = getSubscriber(task);
		if (!isUserActive(subscriber.getStatus()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INACTIVE_USER, m_downloadSetInactiveUserTextDefault, subscriber
							.getLanguage()));
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() < 1) 
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INVALID_PROMOID, m_downloadSetInvalidPromoIDTextDefault, subscriber
							.getLanguage()));
			return;
		}

		if (task.getObject(CLIP_OBJ) == null)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		Clip clip = (Clip) task.getObject(CLIP_OBJ);;
		if (clip == null
				|| clip.getClipEndTime().getTime() < System
						.currentTimeMillis())
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INVALID_PROMOID, m_downloadSetInvalidPromoIDTextDefault, subscriber
							.getLanguage()));
			return;
		}

		SubscriberDownloads[] downloadsList = RBTDBManager.getInstance()
				.getActiveSubscriberDownloads(subscriber.getSubscriberID());
		if (!isDownloadExistsWithWavFile(downloadsList, clip.getClipRbtWavFile()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_NO_DOWNLOAD, m_downloadSetNoDownloadTextDefault, subscriber
							.getLanguage()));
			return;
		}

		String response = processSetSelection(task);
		if (response.equalsIgnoreCase("SUCCESS") || response.toUpperCase().startsWith("SUCCESS"))
		{
			String smsText = getSMSTextForID(task,
					DOWNLOAD_SET_SUCCESS, m_downloadSetSuccessTextDefault, subscriber
						.getLanguage());
			String callerID = task.getString(param_callerid);
			if (callerID != null) {
				smsText = getSMSTextForID(task,
						DOWNLOAD_SET_CALLERID_SUCCESS, m_downloadSetSuccessTextDefault, subscriber
							.getLanguage());
			} else {
				callerID = param(SMS, SMS_TEXT_FOR_ALL, "all");
			}

			smsText = smsText.replace("%SONG_NAME", (clip.getClipName() == null) ? "":clip.getClipName());
			smsText = smsText.replace("%ARTIST", (clip.getArtist() == null) ? "":clip.getArtist());
			smsText = smsText.replace("%CALLER_ID", callerID);
			task.setObject(param_responseSms, smsText);
		}
		else if (response.equals(WebServiceConstants.ALREADY_EXISTS))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_ALREADY_EXISTS, m_downloadSetAlreadyExistsTextDefault, subscriber
							.getLanguage()));
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_FAILURE, m_downloadSetFailureTextDefault, subscriber
							.getLanguage()));
		}
	}

	protected boolean isDownloadExistsWithWavFile(SubscriberDownloads[] downloads, String wavFile)
	{
		if (downloads == null)
			return false;

		for (SubscriberDownloads download : downloads)
		{
			if (download.promoId().equals(wavFile))
				return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processViralOptOutRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processViralOptInRequest(Task task)
	{
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		String subscriptionClass = subscriber.getSubscriptionClass();
		logger.info("Processing viral opt in request. subscriberID: "+subscriberID+", subscriptionClass: "+subscriptionClass);

		task.setObject(param_SMSTYPE, "BASIC");
		ViralData context[] = getViraldata(task);
		if (context == null || context.length < 1 || context[0] == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_OPTIN_FAILURE, m_viralOptInFailureTextDefault, subscriber
							.getLanguage()));
			return;
		}
		
		ViralData viralData = context[context.length - 1];
		String response = updateViraldataType("VIRAL_OPTIN", subscriberID, "BASIC");
		if (response.equalsIgnoreCase("SUCCESS"))
		{
			String chargeClass = "DEFAULT";
			String subClass = "DEFAULT";
			Clip clip = null;
			StringTokenizer st = new StringTokenizer(param(SMS, VIRAL_KEYWORD,
					""), ",");
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens())
			{
				subClass = st.nextToken();
				task.setObject(param_subclass, subClass);
			}
			if (st.hasMoreTokens())
				st.nextToken();
			if (st.hasMoreTokens())
			{
				chargeClass = st.nextToken();
				task.setObject(param_chargeclass, chargeClass);
				task.setObject(param_USE_UI_CHARGE_CLASS, true);
			}
			else {
				String clipID = viralData.getClipID();
				clip = clipID != null? RBTCacheManager.getInstance().getClip(clipID) : null;
				SelectionRequest selectionRequest = new SelectionRequest(
						subscriberID);
				selectionRequest.setClipID(clipID);
				com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass nextChargeClass = RBTClient
						.getInstance().getNextChargeClass(selectionRequest);
				if (null != nextChargeClass) {
					chargeClass = nextChargeClass.getChargeClass();
				}
			}

			String smsText = getSMSTextForID(task,
					VIRAL_OPTIN_SUCCESS, m_viralOptInSuccessTextDefault, subscriber
					.getLanguage());

			String defaultClipId = RBTParametersUtils.getParamAsString(
					"COMMON", "DEFAULT_CLIP", null);
			boolean makeDefaultSong = RBTParametersUtils.getParamAsBoolean(
					"GATHERER", "INSERT_DEFAULT_SEL", "false");
			boolean isDefaultSong = false;
			// Default Clip, not make song sel, active user (A,N,B,G,Z,D,P) --
			// Send
			// new sms to be configuration
			if ((null != defaultClipId && !defaultClipId.isEmpty() && viralData
					.getClipID().equalsIgnoreCase(defaultClipId))
					) {
				isDefaultSong = true;

			}
			if (isDefaultSong && isUserActive(subscriber.getStatus())
					&& !makeDefaultSong) { // Msg need to be asked.
				smsText = getSMSTextForID(task,
						OPTIN_DEFAULT_SELECTION_NOT_ALLOWED_TEXT,
						m_viralDefaultSongTextDefault, subscriber.getLanguage());
			} else if (isUserActive(subscriber.getStatus())) {
				smsText = getSMSTextForID(task,
						VIRAL_OPTIN_ACTIVE_USER_SUCCESS,
						m_viralOptInSuccessTextDefault,
						subscriber.getLanguage());
			}

			smsText = smsText.replace("%PRICE", CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass).getAmount());
			smsText = smsText.replace("%SUB_PRICE", CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subClass).getSubscriptionAmount());
			smsText = smsText.replace("%SONG%", clip == null ? "" : clip.getClipName());
			
			task.setObject(param_responseSms, smsText);
			
			// RBT-10785: migrated user subscription class to be  checked with subscriber.subscriptionclass
			// send migrated user sms text.
			if(migratedUserSubClassesList.contains(subscriptionClass)) {
				
				String migratedUserSmsText = getSMSText("MIGRATED_USER_SMS_TEXT",
						null,
						null, subscriber
						.getLanguage(),
						subscriber.getCircleID());
				try {
					if (migratedUserSmsText != null) {
						String sender = Utility.getSenderNumberbyType("GATHERER", subscriber.getCircleID(), "SENDER_NO");
						Tools.sendSMS(sender, subscriber.getSubscriberID(), migratedUserSmsText,
								false);
					} else {
						logger.warn("MIGRATED_USER_SMS_TEXT is not configured.");
					}
				} catch (Exception e) {
					logger.error(
							"Unable to send sms to migrated user. subscriberId: "
									+ subscriberID + ", subscriptionClass: "
									+ subscriptionClass, e);
				}
			} else {
				logger.debug("Not sending migrated SMS to subscriberID: "
						+ subscriberID + ". subscriptionClass"
						+ " is not migrated subscription class. ");
			}
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					VIRAL_OPTIN_FAILURE, m_viralOptInFailureTextDefault, subscriber
							.getLanguage()));
		}
	}
	
	@Override
	public void processResubscriptionRequest(Task task){
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		HashMap<String,String> userInfoMap = subscriber.getUserInfoMap();
		if(!isUserActive(subscriber.getStatus())){
			task.setObject(param_responseSms, getSMSTextForID(task, ERROR,
					m_errorDefault, subscriber.getLanguage()));
			return;
		}
		if(userInfoMap == null || !userInfoMap.containsKey("UNSUB_DELAY")){
 			   task.setObject(param_responseSms, getSMSTextForID(task,USER_NOT_UNSUB_DELAYED,
				    "You are not Unsub Dealyed.So, You Can't use this feature", subscriber
							.getLanguage()));
			return;
		}
		userInfoMap.remove("UNSUB_DELAY");
		String extraInfo = DBUtility.getAttributeXMLFromMap(userInfoMap);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date defaultEndDate = null;
		try{
		     defaultEndDate = sdf.parse("20371231 00:00:00");
		}catch(Exception ex){
			logger.info("exception in processResubscriptionRequest() while parsing");
		}
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String response = rbtDBManager.updateEndDateAndExtraInfo(subscriberID, defaultEndDate, extraInfo);
		if(response.equalsIgnoreCase("success")){
		    task.setObject(param_responseSms, getSMSTextForID(task,UNSUB_DELAYED_SUCCESS,
				    "UnSub Dealyed Success. You are Resubscribed.", subscriber
							.getLanguage()));
  		    return;
		}

		task.setObject(param_responseSms, getSMSTextForID(task,UNSUB_DELAYED_FAILURE,
				    "Unsub Dealyed Failed", subscriber
						.getLanguage()));
	}

	
	protected void processForUserConfirmationWithoutOffer(Task task){	
        String clipName = null;
        String artist = null;
        String base_amount = "0.0";
        String sel_amount = "0.0";
        String clipID = null;
        String type = "SMSCONFPENDING";
        Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
        String subscriberId = task.getString(param_subscriberID);
		Date sentTime = new Date();
		Clip clip = (Clip)task.getObject(CLIP_OBJ);
		if(clip!=null){
			clipID = clip.getClipId()+"";
			clipName = clip.getClipName();
			artist = clip.getArtist();
			SelectionRequest selectionRequest= new SelectionRequest(subscriber.getSubscriberID());
			if (clip != null) {
				selectionRequest.setClipID("" + clip.getClipId());
			}
			Category category = (Category) task.getObject(CAT_OBJ);
			if (category != null) {
				selectionRequest.setCategoryID("" + category.getCategoryId());
			}else{
				selectionRequest.setCategoryID("3");
			}
			ChargeClass chargeClass = rbtClient.getNextChargeClass(selectionRequest);
			if (chargeClass != null) {
				sel_amount = chargeClass.getAmount();
			}

		}
		String selectedBy = task.getString(param_mode);
		HashMap<String, String> map = new HashMap<String, String>();
		if (task.getString(param_COSID) != null) {
			map.put(param_COSID, task.getString(param_COSID));
		}
		if (task.getString(param_subclass) != null) {
			map.put("SUBSCRIPTION_CLASS", task.getString(param_subclass));
			SubscriptionClass subscriptionClass = getSubscriptionClass(task
					.getString(param_subclass)); 
			base_amount = subscriptionClass.getSubscriptionAmount();
		}
		if (task.getString(param_categoryid) != null) {
			map.put("CATEGORY_ID", task.getString(param_categoryid));
		}
		String extraInfo = DBUtility.getAttributeXMLFromMap(map);
		RBTDBManager.getInstance().insertViralSMSTable(subscriberId, sentTime, type,
				task.getString(param_callerid), clipID, 0, selectedBy, null, extraInfo);
		String smsText  = null;
		if (!isUserActive(subscriber.getStatus()) && clip!=null){
			smsText = getSMSTextForID(task, COMBO_PROMO_CONFIRM_SUCCESS,
					m_comboPromoSuccessDefault, subscriber.getLanguage());
		}else if (clip != null) {
			smsText = getSMSTextForID(task, SELECTION_PROMO_CONFIRM_SUCCESS,
					m_selectionPromoSuccessDefault, subscriber.getLanguage());
		} else {
			smsText = getSMSTextForID(task, ACTIVATION_PROMO_CONFIRM_SUCCESS,
					m_activationPromoSuccessDefault, subscriber.getLanguage());
		}
		smsText = getSubstituedSMS(smsText, clipName, artist, null, base_amount, sel_amount);
		task.setObject(param_responseSms, smsText); 
		sendSMS(task);			

		return;
	}

	private void sendBaseAmoutForUserConfirmation(Task task){		
		
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = subscriber.getLanguage();
		Date sentTime = new Date();
		String smsText = null;
        String clipName = null;
        String artist = null;
        double base_amount = 0.0;
        double sel_amount = 0.0;
        String clipId = null;
		Clip clip =  (Clip)task.getObject(CLIP_OBJ);
		if(clip == null){
			if(task.getString(param_clipid) == null){
			  task.setObject(param_clipid, task.getString("CLIPID"));
			}
			if(task.getString(param_clipid)!=null){
			   clip = RBTCacheManager.getInstance().getClip(task.getString(param_clipid));
			}
		}
		if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
			clipName = clip.getClipName();
			artist = clip.getArtist();
			smsText = getSMSTextForID(task, CLIP_EXPIRED_SMS_TEXT,
							getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault, language), language);
		} else {
			boolean isBlockViralEntry = false;
			if (task.containsKey("SEL_SMS") && isUserActive(subscriber.getStatus())
					&& task.getObject(CLIP_OBJ) == null) {
				isBlockViralEntry = true;
				smsText = getSMSTextForID(task, "OPT_IN_FAILURE_ACT_SEL_SMS",
						m_optInFailureActSelSMS, language);
			} else if (isUserActive(subscriber.getStatus()) && task.containsKey("SEL_SMS")) {
				// active user send request for song selection
				smsText = getSMSTextForID(task, "OPT_IN_CONFIRMATION_ACT_SEL_SMS",
						m_optInConfirmationActSelSMS, language);
			} else if (task.containsKey("SEL_SMS")) {
				// new user or in-active user send request for song selection
				smsText = getSMSTextForID(task, "OPT_IN_CONFIRMATION_ACT_BASE_SEL_SMS",
						m_optInConfirmationActBaseSelSMS, language);
			} else {
				// new user or in-active user send activation request
				smsText = getSMSTextForID(task, "OPT_IN_CONFIRMATION_ACT_SMS",
						m_optInConfirmationActSMS, language);
			}

			String selectedBy = task.getString(param_actby);
			if (selectedBy == null) {
				selectedBy = task.getString(param_actMode);
			}
			String subscriberId = subscriber.getSubscriberID();
			String type = "SMSCONFPENDING";

			Map<String, String> extraInfoMap = new HashMap<String, String>();
			String subscriptionClass = null;
			String chargeClass = null;
            String smsTextBasedOnOffer = null;
			if (isSupportBasePackageOffer
					&& subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER)
					|| subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {

				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
						subscriber.getSubscriberID());
				rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);
				rbtDetailsRequest.setMode(SMS);
				Offer[] offer = RBTClient.getInstance().getPackageOffer(rbtDetailsRequest);
				if (offer != null && offer.length > 0 && (offer[0].getOfferID() == null 
						|| !offer[0].getOfferID().equalsIgnoreCase("-1"))) {
					subscriptionClass = offer[0].getSrvKey();
					base_amount = offer[0].getAmount();
				}
				if (offer != null && offer.length>0 && offer[0].getOfferID() != null
						&& offer[0].getOfferID().equalsIgnoreCase("-1")) {
					isBlockViralEntry =true;
					smsTextBasedOnOffer = getSMSTextForID(task, "TECHNICAL_ERROR_FOR_BASE_OFFER",null);
                }else if (subscriptionClass != null) {
                	if (task.containsKey("SEL_SMS")) {
        				// new user or in-active user send request for song selection
        				smsText = getSMSTextForID(task, "OPT_IN_CONFIRMATION_ACT_BASE_SEL_SMS_"+subscriptionClass,
        						m_optInConfirmationActBaseSelSMS, language);
        			} else {
        				smsTextBasedOnOffer = getSMSTextForID(task, "OPT_IN_BASE_CONFIRMATION_SMS_"+subscriptionClass,
			 					null, language);
        			}
					
					extraInfoMap.put("SUBSCRIPTION_CLASS", subscriptionClass);
				}
				
				if(smsTextBasedOnOffer!=null){
					smsText = smsTextBasedOnOffer;
				}
			}

			if (isSupportSelPackageOffer && task.getString("SEL_SMS") != null) {

				if (clip != null) {
					clipId = "" + clip.getClipId();
					clipName = clip.getClipName();
					artist = clip.getArtist();
				}
				
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
						subscriber.getSubscriberID());
				Library library = RBTClient.getInstance().getLibraryHistory(rbtDetailsRequest);
				logger.info("Library Obtained === "+library);
				if (library != null) {
					Downloads downloads = library.getDownloads();
					if (downloads != null) {
						int noOfActiveDownloads = downloads.getNoOfActiveDownloads();
						int maxAllowedDownloads = RBTParametersUtils.getParamAsInt(COMMON,
								"MAX_DOWNLOADS_ALLOWED", -1);
						if (maxAllowedDownloads > 0 && noOfActiveDownloads >= maxAllowedDownloads) {
							logger.info("Maximum downloads limit has already reached");
							String maxLimitSms = getSMSTextForID(task,
									"MAX_DOWNLOAD_LIMIT_REACHED", null);
							if (maxLimitSms != null) {
								isBlockViralEntry = true;
								smsText = maxLimitSms;
							}
						} else {
							Download[] downloads2 = downloads.getDownloads();
							for (Download download : downloads2) {
								if (download.getEndTime().after(new Date())
										&& !download.getDownloadStatus().equalsIgnoreCase("deactive")
										&& !download.getDownloadStatus().equalsIgnoreCase("deact_pending")
										&& clip != null
										&& download.getRbtFile().replaceAll(".wav", "")
												.equalsIgnoreCase(clip.getClipRbtWavFile())) {
									String downloadAlreadyExistsSmsText = getSMSTextForID(task,
											"DOWNLOAD_ALREADY_EXISTS", null);
                                    logger.info("Download already Exists.downloadAlreadyExistsSmsText="+downloadAlreadyExistsSmsText);
									if (downloadAlreadyExistsSmsText != null) {
										isBlockViralEntry = true;
										smsText = downloadAlreadyExistsSmsText;
									}
									break;
								}
							}
						}
					}
				}
				rbtDetailsRequest.setClipID(clipId);
				rbtDetailsRequest.setMode(SMS);
				rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
				Offer[] offer = null;
				if (!isBlockViralEntry) {
					offer = RBTClient.getInstance().getPackageOffer(rbtDetailsRequest);
				}
				if (offer != null && offer.length > 0
						&& (offer[0].getOfferID() == null || !offer[0].getOfferID()
								.equalsIgnoreCase("-1"))) {
					chargeClass = offer[0].getSrvKey();
					sel_amount = offer[0].getAmount();
				}
				if (offer != null && offer.length > 0 && offer[0].getOfferID() != null
						&& offer[0].getOfferID().equalsIgnoreCase("-1")) {
					isBlockViralEntry = true;
					smsText = getSMSTextForID(task, "TECHNICAL_ERROR_FOR_SEL_OFFER", null);
				}
				if (chargeClass != null) {
					extraInfoMap.put("CHARGE_CLASS", chargeClass);
				}
			}
            logger.info("SMS Confirmation smsText = "+smsText); 
			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

			if (!isSupportSelPackageOffer && clip != null) {
				clipId = "" + clip.getClipId();
				clipName = clip.getClipName();
				artist = clip.getArtist();
			}
			
			if (!isBlockViralEntry) {
				RBTDBManager.getInstance().insertViralSMSTable(subscriberId, sentTime, type,
						task.getString(param_callerid), clipId, 0, selectedBy, null, extraInfo);
			}
		}
		String sms = getSubstituedSMS(smsText, clipName, artist, null, base_amount + "", sel_amount +"");
		task.setObject(param_responseSms, sms);
		sendSMS(task);			
		return;
	}
	
	
	@Override
	public void processSupressPreRenewalSmsRequest(Task task) {
		
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		HashMap<String,String> userInfoMap = subscriber.getUserInfoMap();
		if(!isUserActive(subscriber.getStatus())){
			task.setObject(param_responseSms, getSMSTextForID(task, SUPRESS_PRE_RENEWAL_SMS_ERROR,
					"Will not accept the request, becuse user is not active user", subscriber.getLanguage()));
			return;
		}
		if(userInfoMap == null) {
			userInfoMap = new HashMap<String,String>();
		}
		if(userInfoMap.containsKey("SUPRESS_RENEWAL_SMS")) {
			task.setObject(param_responseSms, getSMSTextForID(task, SUPRESS_PRE_RENEWAL_SMS_ALREADY_ENABLED,
					"Request has been processed before. Duplicate Request", subscriber.getLanguage()));
			return;
		}
		userInfoMap.put("SUPRESS_RENEWAL_SMS", "true");
		String extraInfo = DBUtility.getAttributeXMLFromMap(userInfoMap);		
		String strTimeOut = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON,"SMDAEMON_TIMEOUT","6").getValue();
		String url = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON,"SUPRESS_PRE_RENEWAL_SMS_PRISM_URL",null);
		if(parameter != null) {
			url = parameter.getValue();
			if(url != null) {
				url = url.replaceAll("<msisdn>", subscriberID);
			}
			logger.debug("Supress Pre-renewal prism url: " + url);
		}
		int iTimeOut = Integer.parseInt(strTimeOut);
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setConnectionTimeout(iTimeOut*1000);
		httpParameters.setSoTimeout(iTimeOut*1000);
		httpParameters.setUrl(url);
		HttpResponse httpResponse = null;
		try {
			httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
		} catch (HttpException e) {
			logger.error("Unable to hit prism to enable pre renewal no sms ", e);
		} catch (IOException e) {
			logger.error("Unable to hit prism to enable pre renewal no sms ", e);
		}
		if(httpResponse != null && httpResponse.getResponse().toLowerCase().indexOf("success") != -1){
			HashMap<String,String> attributeMap = new HashMap<String, String>();
			attributeMap.put("EXTRA_INFO", extraInfo);
			rbtDBManager.updateSubscriber(subscriberID, attributeMap);
		    task.setObject(param_responseSms, getSMSTextForID(task,SUPRESS_PRE_RENEWAL_SMS_SUCCESS,
				    "Request processed successfully. you will not get sms before charging", subscriber
							.getLanguage()));
  		    return;
		}

		task.setObject(param_responseSms, getSMSTextForID(task,SUPRESS_PRE_RENEWAL_SMS_FAULURE,
				    "Request got failed", subscriber
						.getLanguage()));

	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processInitRandomizeRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processInitRandomizeRequest(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, INIT_RANDOMIZE_INACTIVE_USER,
							"User is inactive. So please download few songs and then randomize your downloads.",
							subscriber.getLanguage()));
			return;
		}

		SubscriberDownloads[] downloadsList = RBTDBManager.getInstance()
				.getActiveSubscriberDownloads(subscriber.getSubscriberID());
		int count = 0;
		if (downloadsList != null)
		{
			for (SubscriberDownloads download : downloadsList)
			{
				if (download.downloadStatus() == 'y')
					count++;
			}
		}

		if (count < 2) {
			task.setObject(param_responseSms,
					getSMSTextForID(task, INIT_RANDOMIZE_NOT_ENOUGH_DOWNLOADS,
							"You don't have enough downloads to randomize.",
							subscriber.getLanguage()));
			return;
		}

		String response = addViraldata(subscriberID, null, "RANDOMIZE", null, "SMS", 0, null);

		if (response.equalsIgnoreCase("SUCCESS")) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT",
					getSMSTextForID(task, INIT_RANDOMIZE_SUCCESS,
							"You have %DOWNLOAD_COUNT songs in your library. To confirm please send OK to 54321.",
							subscriber.getLanguage()));

			hashMap.put("DOWNLOAD_COUNT", String.valueOf(downloadsList.length));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
		} else {
			task.setObject(param_responseSms,
					getSMSTextForID(task, INIT_RANDOMIZE_FAILURE,
							"Your randomization request has been failed due to some technical difficulties.",
							subscriber.getLanguage()));
		}
	}
	
	/*
	 * It shows only the all caller selections.
	 */
    @Override
	public void getOnlyAllCallerSettings(Task task) {
		StringBuilder smsBuilder = new StringBuilder();
		int songCount = 1;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();

		Setting[] settings = getSettings(task).getSettings();
		if (settings != null) {
			for (Setting setting : settings) {
				if (setting.getEndTime().before(new Date()) || (setting.getCallerID() != null
						&& !setting.getCallerID().equalsIgnoreCase("all")))
					continue;

				String contentName = "-";
				String promoCode = "-";
				if (setting.getToneType().equals(WebServiceConstants.CATEGORY_SHUFFLE)) {
					Category category = rbtCacheManager.getCategory(setting.getCategoryID(),
							subscriber.getLanguage());
					if (category == null)
						continue;

					contentName = (category.getCategoryName() == null || category.getCategoryName()
							.equalsIgnoreCase("null")) ? "NA" : category.getCategoryName();
					promoCode = (category.getCategoryPromoId() == null || category
							.getCategoryPromoId().equalsIgnoreCase("null")) ? "NA" : category
							.getCategoryPromoId();
				} else {
					Clip clip = rbtCacheManager.getClip(setting.getToneID(),
							subscriber.getLanguage());
					if (clip == null)
						continue;

					contentName = (clip.getClipName() == null || clip.getClipName()
							.equalsIgnoreCase("null")) ? "NA" : clip.getClipName();
					promoCode = (clip.getClipPromoId() == null || clip.getClipPromoId()
							.equalsIgnoreCase("null")) ? "NA" : clip.getClipPromoId();
				}
				smsBuilder.append(",").append(contentName);
			}
		}
                      
		if (smsBuilder.length() > 1) {
			hashMap.put("SELECTIONS", smsBuilder.substring(1));
		} else {
			if (getSMSTextForID(task, NO_DEFAULT_SETTING_SELECTION, null, subscriber.getLanguage()) != null) {
				task.setObject(param_responseSms,
						getSMSTextForID(task, NO_DEFAULT_SETTING_SELECTION, null,
								subscriber.getLanguage()));
				return;
			}
			hashMap.put("SELECTIONS", "0");
		}

		hashMap.put(
				"SMS_TEXT",
				getSMSTextForID(task, MANAGE_DEFAULT_SETTING_SUCCESS,
						m_manageDefaultSettingSuccess, subscriber.getLanguage()));
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		String finalSmsText = finalizeSmsText(hashMap);
		logger.info("Final Sms Text for Default Setting : " + finalSmsText);
		task.setObject(param_responseSms, finalSmsText);

	}

	/**
	 * This method will validate the minimum and maximum lengths of a caller id.
	 * <p>
	 * Default text returned by this method is PROMO_ID_FAILURE or m_promoIDFailureDefault
	 * 
	 * @param task
	 * @param token
	 */
	protected String isValidPromoId(Task task, String token, String language) {
		String returnSMS = null;
		// Nothing to do
		if (token == null || token == null) {
			returnSMS = getSMSTextForID(task, PROMO_ID_FAILURE, m_promoIDFailureDefault, language);
			return returnSMS;
		}

		int minPromoIdLength = RBTParametersUtils.getParamAsInt(SMS, PROMO_ID_MIN_LENGTH, -1);
		int maxPromoIdLength = RBTParametersUtils.getParamAsInt(SMS, PROMO_ID_MAX_LENGTH, -1);
		if (minPromoIdLength > 0 && token.length() < minPromoIdLength) {
			returnSMS = getSMSTextForID(task, "PROMO_ID_LENGTH_LESS_THAN_MIN_LENGTH", null, language);
		}

		if (returnSMS == null && maxPromoIdLength > 0 && token.length() > maxPromoIdLength) {
			returnSMS = getSMSTextForID(task, "PROMO_ID_LENGTH_MORE_THAN_MAX_LENGTH", null, language);
		}

		// Checking if the promo id passed is valid integer
		if (returnSMS == null) {
			try {
				Integer.parseInt(token);
				returnSMS = getSMSTextForID(task, PROMO_ID_FAILURE, m_promoIDFailureDefault, language);
			}
			catch (Exception e) {
				returnSMS = getSMSTextForID(task, "PROMO_ID_NOT_INTEGER", null, language);
			}
		}
		
		//Last check to be sure that the return SMS is not null
		if(returnSMS == null) {
			returnSMS = getSMSTextForID(task, PROMO_ID_FAILURE, m_promoIDFailureDefault, language);
		}
		return returnSMS;
	}
	
	@Override
	public void processOUISmsRequest(Task task){
		logger.info("Process OUI SMS keyword");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean isProfileDeactivation = isThisFeature(task,SmsKeywordsStore.deactivateProfileKeywordsSet, TEMPORARY_OVERRIDE_CANCEL_MESSAGE);
		ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);
		if(smsList.size() == 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,	OUI_SMS_KEYWORD_FAILURE, "Please check your sms text>", subscriber.getLanguage()));
			return;
		}
		task.setObject(param_ouiRegCode, smsList.remove(0));
		if(isProfileDeactivation) {			
			processRemoveTempOverride(task);
		}
		else {
			processActNSel(task);
		}
	}
	
	@Override
	public void processCancelDeactvation(Task task){
		logger.info("Process cancel deactivation");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.getSubscriberID());
		rbtDetailsRequest.setMode("ccc");
		subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		
		
		
		
		if(!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,	CANCEL_DEACTIVATION_SMS_USER_NOT_ACTIVE, "User is not active", subscriber.getLanguage()));
			return;
		}
		
		HashMap<String, String> userInfoMap = subscriber.getUserInfoMap();
		String confDelayTime = getParamAsString(iRBTConstant.SMS,"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",null);
		if(confDelayTime != null) {
			
			
			boolean isSupportDelayDct = false;			
			if(userInfoMap != null && userInfoMap.containsKey("UNSUB_DELAY")) {
				isSupportDelayDct = true;
				userInfoMap.remove("UNSUB_DELAY");
			}
			
			if(!isSupportDelayDct) {
				task.setObject(param_responseSms, getSMSTextForID(task,	CANCEL_DEACTIVATION_SMS_ERROR, "CANCEL_DEACTIVATION_SMS_ERROR", subscriber.getLanguage()));
				return;
			}
			
			Date subEndDate = subscriber.getEndDate();
			long  endDate = System.currentTimeMillis();
			double longConfDelayTime = 0;
			try{
	        	longConfDelayTime = Double.parseDouble(confDelayTime);        	
	        }catch(NumberFormatException ex){
	        	logger.info("Error in Parsing the Configured time for UNSUB_DELAY ON DEACTIVATION");
	        }
		
			logger.info("SubEnddate: " + subEndDate.getTime() + " system current time: " + endDate + " sub value: " + longConfDelayTime);
			if(subEndDate.getTime() - endDate  < 0 ) {
				task.setObject(param_responseSms, getSMSTextForID(task,	CANCEL_DEACTIVATION_SMS_TIME_EXCEED, "Sorry, your request has been ingored, becuase time limit exceeed", subscriber.getLanguage()));
				return;
			}			        
		}
		Date date = null;
		try {
			date = new SimpleDateFormat("dd/MM/yyyy").parse("31/12/2037");
		} catch (ParseException e) {
			// TODO Auto-generated catch block			
		}
		
		String extraInfo = DBUtility.getAttributeXMLFromMap(userInfoMap);
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String response = rbtDBManager.updateEndDateAndExtraInfo(subscriber.getSubscriberID(), date, extraInfo);
        
        if(response.equalsIgnoreCase("success")) {
        	task.setObject(param_responseSms, getSMSTextForID(task,	CANCEL_DEACTIVATION_SMS_SUCCESS, "Request successfully proceed", subscriber.getLanguage()));
        }
        else {
        	task.setObject(param_responseSms, getSMSTextForID(task,	CANCEL_DEACTIVATION_SMS_FAILURE, "Request got failed", subscriber.getLanguage()));
        }
	}
	
	
	public void processRegistraionSMS(Task task) {
		
		String response  = Resp_Failure;
		WebServiceAction webServiceAction = WebServiceActionFactory.getWebServiceActionProcessor("registerUID");
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		if(smsList == null || smsList.size() == 0) {
			task.setObject(param_responseSms, response);
			return;
		}
		String uid = smsList.get(0);
		if(webServiceAction != null) {
			WebServiceContext webServiceContext =  new WebServiceContext();
			webServiceContext.put(WebServiceConstants.param_uid, uid);
			webServiceContext.put(WebServiceConstants.param_subscriberID, task.getString(param_subscriberID));
			webServiceContext.put(WebServiceConstants.param_type, "MOBILECLIENT");
			//webServiceContext.putAll(task.getTaskSession());
			WebServiceResponse webServiceResponse = webServiceAction.processAction(webServiceContext);
			response = webServiceResponse.getResponse();
		}
		logger.info("webServiceResponse : " + response);
		task.setObject(param_responseSms, response);
	}
	
	//Start:RBT-12195 - User block - unblock feature.
	public void processBlockSubRequest(Task task) {
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();
		boolean isBlockedSub = checkBlockedUser(subscriberID);
		boolean userActive = isUserActive(subscriber.getStatus());
		logger.info("processBlockSubRequest : subscriberID: " + subscriberID
				+ " isBlockedSub : " + isBlockedSub + " userActive"
				+ userActive);
		if (isBlockedSub) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, SMS_NON_ACT_BLCK_SUB_BLOCK_TEXT,
							blocked_serviceNotAllowed, subscriber.getLanguage()));
			return;
		}
		
		String dctProtocolNum = "";
		if (userActive) {
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriberID);
			logger.info("processBlockSubRequest : " + subscriberID);
			String SMS_MODE = RBTParametersUtils.getParamAsString(COMMON,
					"DEACTIVATION_SMS_BLOCK_SUB_MODE", "SMS");
			subscriptionRequest.setMode(SMS_MODE);
			Subscriber dctSubscriber = rbtClient.deactivateSubscriber(subscriptionRequest);
			String dctInfo = dctSubscriber.getActivationInfo();
			//Activation info column will be appended with 'DCT:|protocolnumber:2015123456789012|' 
			String strArr[] = dctInfo.split("DCT:\\|protocolnumber:");
			dctProtocolNum = strArr[1];
			dctProtocolNum = dctProtocolNum.substring(0, dctProtocolNum.length() - 1);

		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(2037, 0, 1);
		Date viralEndDate = calendar.getTime();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		ViralBlackListTable viralBlackListTable = rbtDBManager
				.insertViralBlackList(subscriberID, null, viralEndDate, "BLOCK");

		RBTProtocolGenerator proGen = RBTProtocolGenerator.getInstance();
		String protocolNum = proGen.generateUniqueProtocolNum();
		StringBuffer sb = new StringBuffer();
		sb.append(subscriberID);
		sb.append(",BLOCK");
		sb.append("," + protocolNum);
		blockUnblockLogger.info(sb);
		
		String smsText = getSMSTextForID(task, SMS_NON_ACT_UNBLCK_SUB_BLOCK_TEXT,
				unblck_blck_serviceNotAllowed, subscriber.getLanguage());
		smsText = smsText.replace("<DCT_PROTOCOL_NUMBER>", dctProtocolNum);
		smsText = smsText.replace("<BLOCK_PROTOCOL_NUMBER>", protocolNum);
		task.setObject(param_responseSms, smsText);

	}

	public void processUnBlockSubRequest(Task task) {
		Subscriber subscriber = getSubscriber(task);
		boolean isBlockedSub = checkBlockedUser(subscriber.getSubscriberID());
		boolean userActive = isUserActive(subscriber.getStatus());
		boolean result = false;
		logger.info("processUnBlockSubRequest : subscriberID: "
				+ subscriber.getSubscriberID() + " isBlockedSub : "
				+ isBlockedSub + " userActive" + userActive);
		if (isBlockedSub) {
			String subscriberID = subscriber.getSubscriberID();
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			result = rbtDBManager.removeViralBlackList(subscriberID, "BLOCK");
			
			RBTProtocolGenerator proGen = RBTProtocolGenerator.getInstance();
			String protocolNum = proGen.generateUniqueProtocolNum();
			StringBuffer sb = new StringBuffer();
			sb.append(subscriberID);
			sb.append(",UNBLOCK");
			sb.append("," + protocolNum);
			blockUnblockLogger.info(sb);
			
			if (result == true) { // NON-ACTIVE USER BLOCKED --> UnBlock request
				String smsText = getSMSTextForID(task,
						SMS_NON_ACT_BLCK_SUB_UNBLOCK_TEXT,
						unblocked_serviceAllowed, subscriber.getLanguage());
				smsText = smsText.replace("<UNBLOCK_PROTOCOL_NUMBER>", protocolNum);
				task.setObject(param_responseSms, smsText);
			}
		} else {
			if (userActive) {// ACTIVE USER UNBLOCKED --> UnBlock request
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, SMS_ACT_UNBLCK_SUB_UNBLOCK_TEXT,
								blocked_serviceAllowed,
								subscriber.getLanguage()));
			} else { // NON-ACTIVE USER UNBLOCKED --> UnBlock request
				task.setObject(
						param_responseSms,
						getSMSTextForID(task,
								SMS_NON_ACT_UNBLCK_SUB_UNBLOCK_TEXT,
								other_serviceAllowed, subscriber.getLanguage()));
			}
		}
	}

	private boolean checkBlockedUser(String subscriberID) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		boolean isBlockedSub = rbtDBManager.isBlackListSub(subscriberID);
		logger.info("checkBlockedUser : subscriberID: " + subscriberID);
		return isBlockedSub;
	}//End :RBT-12195 - User block - unblock feature.

	@Override
	public void processPremiumSelectionConfirmation(Task task) {
		Subscriber subscriber = getSubscriber(task);
		boolean toBeConsiderUds4NewUser = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_CONSIDER_UDS_FOR_NEW_USER", "FALSE");
		if (!toBeConsiderUds4NewUser && !isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					USER_NOT_ACTIVE, m_userNotActiveDefault, subscriber
							.getLanguage()));
			return;
		}
		String smsText = null;
		int duration = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "SEL_WAIT_TIME_DOUBLE_CONFIRMATION", 30);
		ViralSMSTable viralSMS = null;
		ViralSMSTable[] viralSMSes = RBTDBManager.getInstance().getLatestViralSMSesByTypeSubscriberAndTime(subscriber.getSubscriberID(), "SELCONFPENDING", duration);
		if (viralSMSes != null && viralSMSes.length > 0) {
			viralSMS = viralSMSes[0];
		}
		if (viralSMS == null) {
			logger.debug("No valid viral sms table entries found. Returning. subscriberId: " + subscriber.getSubscriberID());
			smsText = getSMSTextForID(task, PREMIUM_SELECTION_CONFIRMATION_ENTRY_MISSING,
					m_premiumSelectinConfirmationEntryMissing, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task, subscriber.getCircleID());
			task.setObject(param_responseSms, smsText);
			return;
		}
		Clip reqClip = getClipById(viralSMS.clipID());
		task.setObject(CLIP_OBJ, reqClip);
		String callerID = viralSMS.callerID();
		if (callerID != null && !callerID.trim().equals("")) {
			task.setObject(param_callerid, callerID);
		}
		
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		if (viralSMS.callerID() != null) {
			selectionRequest.setCallerID(viralSMS.callerID());		
		}
		selectionRequest.setClipID(viralSMS.clipID());
		selectionRequest.setMode(SMS);
		selectionRequest.setModeInfo(viralSMS.selectedBy());
		selectionRequest.setAllowPremiumContent(true);

		String extraInfo = viralSMS.extraInfo();
		Map<String, String> infoMap = null;
		if (extraInfo != null) {
			infoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		}

		if (infoMap != null) {
			if (infoMap.containsKey("CATEGORY_ID"))
				selectionRequest.setCategoryID(infoMap.get("CATEGORY_ID"));
			if (infoMap.containsKey("SEL_INFO"))
				selectionRequest.setModeInfo(infoMap.get("SEL_INFO"));
			if (infoMap.containsKey("COS_ID"))
				selectionRequest.setCosID(Integer.parseInt(infoMap
						.get("COS_ID")));
			if (infoMap.containsKey("SUBSCRIPTION_CLASS"))
				selectionRequest.setSubscriptionClass(infoMap
						.get("SUBSCRIPTION_CLASS"));
			if(infoMap.containsKey("UDS_OPTIN")){
				HashMap<String,	String> xtrMap = selectionRequest.getSelectionInfoMap();
				if(xtrMap == null){
					xtrMap = new HashMap<String, String>();
				}
				xtrMap.put("UDS_OPTIN", infoMap.get("UDS_OPTIN"));
				selectionRequest.setSelectionInfoMap(xtrMap);
			}			
		}
		rbtClient.addSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase("SUCCESS")) {
			smsText = getSMSTextForID(task,
					PREMIUM_SELECTION_CONFIRMATION_SUCCESS, m_premiumSelectinConfirmationSuccessDefault, subscriber
					.getLanguage());
			boolean isRemoved = RBTDBManager.getInstance().deleteViralPromotionBySMSID(viralSMS.getSmsId());
			logger.debug("viralSMS entry: " + viralSMS + ", isRemoved: " + isRemoved);
		} else {
			smsText = getSMSTextForID(task, "PREMIUM_SELECTION_CONFIRMATION_" + response,
					null, subscriber.getLanguage());
			if (smsText == null) {
				smsText = getSMSTextForID(task, PREMIUM_SELECTION_CONFIRMATION_FAILURE,
						m_premiumSelectinConfirmationFailureDefault, subscriber.getLanguage());
			}
		}
		smsText = finalSmsText(smsText, task, subscriber.getCircleID());
		task.setObject(param_responseSms, smsText);
	}
	
	protected String getFreemiumBaseUpgrdAmt(Task task) {
		String baseAmt = "0.0";
		try {
			com.onmobile.apps.ringbacktones.webservice.client.beans.Offer offer = getBaseOffer(task);
			String freemiumRentalPack = null;
			String extraInfo1 = null;
			if (offer != null) {
				freemiumRentalPack = offer.getSrvKey();
				baseAmt = offer.getAmount() + "";
			} else {
				freemiumRentalPack = RBTParametersUtils.getParamAsString("COMMON",
						"FREEMIUM_RENTAL_PACK", null);
				SubscriptionClass sClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(freemiumRentalPack);
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
	
	@Override
	public void processBaseAndCosUpgradationRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String mode = getParameter(COMMON,
				"SONG_BASED_UPGRADATION_MODE");
		if(mode != null && !mode.isEmpty()){
		  task.setObject(param_MODE, mode);	
		}
		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, USER_NOT_ACTIVE,
							m_userNotActiveDefault, subscriber.getLanguage()));
			return;
		}
		String tobeUpdateSubClass = null;
		String confSubClass = getParameter("SMS",
				"BASE_AND_COS_UPGRADATION_CIRCLEID_SUBCLASS_MAPPING");
		if (confSubClass == null || confSubClass.isEmpty()) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task,
							BASE_UPGRADE_SUBCLASS_AND_COSID_MAP_NOT_FOUND,
							m_baseUpgradeSubClassAndCosIDMapNotFound,
							subscriber.getLanguage()));

			return;

		}
		String cosId = "";
		String subClass = subscriber.getSubscriptionClass();
		String circleId = subscriber.getCircleID();
		List<String> subClassAndCosId = getSubClassAndCosID(confSubClass.toUpperCase(),
				subClass, circleId);
		if (subClassAndCosId != null && !subClassAndCosId.isEmpty() && subClassAndCosId.size() == 2) {
			tobeUpdateSubClass = subClassAndCosId.get(0);
			cosId = subClassAndCosId.get(1);
		}
		if (tobeUpdateSubClass == null || confSubClass.isEmpty()) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task,
							BASE_UPGRADE_SUBCLASS_AND_COSID_MAP_NOT_FOUND,
							m_baseUpgradeSubClassAndCosIDMapNotFound,
							subscriber.getLanguage()));

			return;

		}
		task.setObject(param_cosid, cosId);
		if (subClass.equalsIgnoreCase(tobeUpdateSubClass)) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, BASE_UPGRADE_SAME_SUB_CLASS,
							m_baseUpgradeSameSubClass, subscriber.getLanguage()));

			return;
		}
		SubscriptionClass subscriptionClass = CacheManagerUtil
				.getSubscriptionClassCacheManager().getSubscriptionClass(
						tobeUpdateSubClass.toUpperCase());
		if (subscriptionClass == null) {
			task.setObject(param_responseSms, TECHNICAL_FAILURE);
			return;
		}
		task.setObject(param_subclass.toUpperCase(), tobeUpdateSubClass);
		String response = upgradeBasePackOfSubscriber(task);
		if (response.equalsIgnoreCase("SUCCESS"))
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, BASE_AND_COSID_UPGRADE_SUCCESS,
							m_baseAndCosIdUpgradeSuccessDefault,
							subscriber.getLanguage()));
		else if (response.equals(WebServiceConstants.UPGRADE_NOT_ALLOWED)) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, "UPGRADE_NOT_ALLOWED",
							m_UpgradeNotAllowed, subscriber.getLanguage()));
		} else
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, BASE_AND_COS_UPGRADE_FAILURE,
							m_baseAndCosIDUpgradeFailureDefault,
							subscriber.getLanguage()));
	}
	
	private List<String> getSubClassAndCosID(String Configuration,
			String subClass, String circleId) {

		HashMap<String, String> oldAndNewSubClassMapping = (HashMap<String, String>) MapUtils
				.convertIntoMap(Configuration, ";", "=", null);
		String subClassAndCosId = "";
		List<String> subAndCosId = new ArrayList<String>();
		if(subClass != null && !subClass.isEmpty()){
			subClass = subClass.toUpperCase();
		}
		
		if(circleId != null && !circleId.isEmpty()){
			circleId = circleId.toUpperCase();
		}
		
		String subClassCircleId = subClass + "_" + circleId;
		if (oldAndNewSubClassMapping != null) {
			if (oldAndNewSubClassMapping.containsKey(subClassCircleId)) {
				subClassAndCosId = oldAndNewSubClassMapping
						.get(subClassCircleId);
			} else if (oldAndNewSubClassMapping.containsKey(subClass)) {
				subClassAndCosId = oldAndNewSubClassMapping.get(subClass);
			}
		}
		if (subClassAndCosId != null && !subClassAndCosId.isEmpty()) {
			subAndCosId = Arrays.asList(subClassAndCosId.split(","));
		}
		return subAndCosId;
	}	
	
	//Based on SMS_KEYWORD get Validity days and PACK_NAME
	public static String substitutePackNameValidDays(String smsText, String smsKeyWord) {
		String validity = "";
		String packName = "";
		
		if (smsText != null) {
			if (smsKeyWord != null) {
				String cosKeyPckNameDurMapStr = RBTParametersUtils.getParamAsString(WEBSERVICE,"COS_KEYWORD_TO_PACK_NAME_DURATION_MAP", null);
				Map<String, List<String>> cosKeyPckNameDurMap = MapUtils.convertMapList(cosKeyPckNameDurMapStr, ";", "=", "|");
				List<String> durationAndPackName = cosKeyPckNameDurMap.get(smsKeyWord);
				if (durationAndPackName != null && durationAndPackName.size() > 0) {
					validity = durationAndPackName.get(0) != null ? durationAndPackName.get(0) : "";
					//IF NO CONFIGURATION AVAILABLE FOR PACKNAME
					if(durationAndPackName.size()>1)
						packName = durationAndPackName.get(1) != null ? durationAndPackName.get(1) : "";
				}
			}
			while (smsText.indexOf("%VALID_DAYS") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%VALID_DAYS"))
						+ validity
						+ smsText
								.substring(smsText.indexOf("%VALID_DAYS") + 11);
			}
			while (smsText.indexOf("%PACK_NAME") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%PACK_NAME"))
						+ packName
						+ smsText.substring(smsText.indexOf("%PACK_NAME") + 10);
			}
		}
		
		return smsText;
	}
}