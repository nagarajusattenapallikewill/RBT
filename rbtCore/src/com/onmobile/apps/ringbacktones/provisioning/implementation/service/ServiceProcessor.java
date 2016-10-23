/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.service;

import static com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore.activationKeywordsSet;
import static com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore.deactivateBaseKeywordsSet;
import static com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore.rbtKeywordsSet;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.ACTIVE;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.ACT_ERROR;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.ACT_PENDING;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.CATEGORY_SHUFFLE;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.DEACTIVE;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.DEACT_ERROR;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.DEACT_PENDING;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.GRACE;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.LITE_USER_PREMIUM_BLOCKED;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NEW_USER;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.NOT_ALLOWED;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.OVERLIMIT;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SELECTION_SUSPENDED;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS;
import static com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants.param_isPressStarIntroEnabled;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.eventlogging.UGCEventLogger;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.SmsTextCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.logger.consent.ConsentCallbackHitLogger;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.CopyProcessorUtils;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.UgcClip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoryClipMapDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.UgcClipDAO;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.actions.WriteCDRLog;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.common.exception.OnMobileException;
import com.onmobile.reporting.framework.capture.api.Configuration;

/**
 * @author bikash.panda
 */
public class ServiceProcessor extends Processor {

	protected static RBTClient rbtClient = null;
	protected static Logger logger = Logger.getLogger(ServiceProcessor.class);
	protected static UGCEventLogger ugcEventLogger = null;
	private static Logger cdr_logger = Logger.getLogger("CDR_LOGGER");

	boolean usePool = false;
	private ArrayList<String> m_subMsg = null;
	private ArrayList<String> m_unsubMsg = null;
	private HashMap<String, String> operatoraccounts = new HashMap<String, String>();
	private ArrayList<String> normalCopyKeys = null;
	private ArrayList<String> starCopyKeys = null;
	private static ArrayList<String> crossCopy=null;
	private int vrbtRECategoryId = 26;
	private static HashMap<String, HashSet<String>> directCopyKeysMap = null;
	private static HashMap<String, HashSet<String>> optinCopyKeysMap = null;
	private static ArrayList<String> allowedThirdPartyKeysList = null;
	private static List<String> consentModesNoTransId = null;
	//TPCG_MODES_NO_TRANSID 
	private SmsTextCacheManager smsTextCacheManager = CacheManagerUtil
			.getSmsTextCacheManager();
	
	private HashSet<String> copyVirtualNumbers = null;
	public static Map<String,String> upgradeSubscriptionClassMap = new HashMap<String, String>();
	
	private Map<String,List<String>> m_vrbtCatIdSubSongSrvKeyMap = null;
	private static Map<String, String> externalToInternalModeMapping = null;
	//RBT-14671 - # like
	private static ArrayList<String> toLikeKeys=null;
	
	/**
	 * @throws Exception
	 */
	public ServiceProcessor() throws Exception {

		try {
			String ugcEventLoggingDir = RBTParametersUtils.getParamAsString(
					"COMMON", "UGC_EVENT_LOG_PATH", null);
			if (ugcEventLoggingDir != null) {
				ugcEventLoggingDir += File.separator + "UGCEventLogs";
				File file = new File(ugcEventLoggingDir);
				if (!file.isDirectory()) {
					new File(ugcEventLoggingDir).mkdirs();
				}
			} else {
				logger.error("UGC_EVENT_LOG_PATH is not configured in rbt_parameters table");
			}
			Configuration cfg = new Configuration(ugcEventLoggingDir);
			try {
				ugcEventLogger = new UGCEventLogger(cfg);
			} catch (IOException e) {
				logger.error("Exception creating the UGC event logger object ",
						e);
			}
			rbtClient = RBTClient.getInstance();
			m_subMsg = tokenizeArrayList(
					getParameter("SMS", "ACTIVATION_KEYWORD"), null);
			m_unsubMsg = tokenizeArrayList(
					getParameter("SMS", "DEACTIVATION_KEYWORD"), null);
			String operatoraccountsStr = getParameter("COMMON",
					"COPY_OPR_ACCOUNTS");
			if (operatoraccountsStr != null
					&& operatoraccountsStr.trim().length() > 0) {
				StringTokenizer parentTokenizer = new StringTokenizer(
						operatoraccountsStr.trim(), ";");
				while (parentTokenizer.hasMoreTokens()) {
					StringTokenizer childTokenizer = new StringTokenizer(
							parentTokenizer.nextToken().trim(), ",");
					if (childTokenizer.countTokens() == 2)
						operatoraccounts.put(childTokenizer.nextToken().trim(),
								childTokenizer.nextToken().trim());
				}
			}
			logger.info("Configured operator accounts map: " + operatoraccounts);
			normalCopyKeys = tokenizeArrayList(
					getParameter("COMMON", "NORMALCOPY_KEY"), ",");
			starCopyKeys = tokenizeArrayList(
					getParameter("COMMON", "STARCOPY_KEY"), ",");
			crossCopy = tokenizeArrayList(getParameter("COMMON", CROSSCOPY_KEY), ",");
			String allowedThirdPartyKeys = getParamAsString("GATHERER",
					"COPY_ALLOWED_NORAML_KEYS", null);
			logger.info("parameter allowedThirdPartyKeysList ="
					+ allowedThirdPartyKeys);
			if (null != allowedThirdPartyKeys && !allowedThirdPartyKeys.isEmpty()) {
				allowedThirdPartyKeysList = tokenizeArrayList(allowedThirdPartyKeys, ",");
			}			
			try {
				vrbtRECategoryId = Integer.parseInt(getParameter("COMMON",
						"VRBT_RE_CATEGORY_ID"));
			} catch (NumberFormatException nfe) {
				vrbtRECategoryId = 26;
			}
			initCopyKeys();
			
			String paramValue = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue("COMMON",
							"VODACT_SERVICE_UPGRADE_SUB_CLASS", null);
			upgradeSubscriptionClassMap = new HashMap<String, String>();
			if (paramValue != null) {

				StringTokenizer tokenizer = new StringTokenizer(paramValue, ";");
				while (tokenizer.hasMoreTokens()) {
					StringTokenizer childTokenizer = new StringTokenizer(
							tokenizer.nextToken(), ",");
					if (childTokenizer.countTokens() == 2) {
						String token1 = childTokenizer.nextToken().trim();
						String token2 = childTokenizer.nextToken().trim();
						upgradeSubscriptionClassMap.put(token1, token2);
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Exception in Serviceprovidr Initialising rbtclient",
					e);
		}
		
		m_vrbtCatIdSubSongSrvKeyMap = Utility.getVrbtCatSubSongSrvMap();
		initVirtualNumbers();//RBT-14671 - # like
		toLikeKeys = tokenizeArrayList(getParameter("COMMON",TOLIKE_KEY), ",");
	}

	public Task getTask(HashMap<String, String> requestParams) {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);
		String taskAction = null;
		Task task = new Task(null, taskSession);
		reorderParameters(task);
		// TODO: Identify the task action
		if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_copy))
			taskAction = action_copy;
		else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_cross_copy))
			taskAction = action_cross_copy;
		else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_cross_copy_rdc))
			taskAction = action_cross_copy_rdc;
		else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_rbtplayhelp))
			taskAction = action_rbt_play_help;
		else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_ESIAQuizForward))
			taskAction = action_esia_quiz_forward;
		if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_mnp_cross_copy))
			taskAction = action_mnp_cross_copy;
		task.setTaskAction(taskAction);
		logger.info("RBT:: task: " + task);
		return task;
	}

	protected void reorderParameters(Task task) {
		if (task.containsKey(param_MSISDN))
			task.setObject(param_subscriberID, task.getString(param_MSISDN));
		else if (task.containsKey(param_SUBID))
			task.setObject(param_subscriberID, task.getString(param_SUBID));
	}

	public String validateParameters(Task task) {
		String response = "VALID";
		if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_copy)) {
			if (task.getString(param_SMSTYPE) == null
					|| task.getString(param_SMSTYPE).equalsIgnoreCase("null"))
				task.setObject(param_SMSTYPE, "COPY");
			if (task.getString(param_SELBY) == null
					|| task.getString(param_SELBY).length() <= 0
					|| task.getString(param_SELBY).equalsIgnoreCase("null"))
				task.setObject(param_SELBY, null);
			if (task.getString(param_CLIPID) != null
					&& task.getString(param_CLIPID).equalsIgnoreCase("null"))
				task.setObject(param_CLIPID, null);

			if (task.getString(param_SUBID) == null
					|| task.getString(param_CALLERID) == null)
				return copy_Resp_Err;
		} else if (task.getString(param_api).equals(api_cross_copy)
				|| task.getString(param_api).equals(api_cross_copy_rdc)) {
			if (task.getString(param_SMSTYPE) == null
					|| task.getString(param_SMSTYPE).equalsIgnoreCase("null"))
				task.setObject(param_SMSTYPE, "COPY");
			if (task.getString(param_SELBY) == null
					|| task.getString(param_SELBY).length() <= 0
					|| task.getString(param_SELBY).equalsIgnoreCase("null"))
				task.setObject(param_SELBY, null);
			if (task.getString(param_SUBID) != null
					&& task.getString(param_SUBID).equalsIgnoreCase("null"))
				task.setObject(param_SUBID, null);
			if (task.getString(param_CLIPID) != null
					&& task.getString(param_CLIPID).equalsIgnoreCase("null"))
				task.setObject(param_CLIPID, null);
			if (task.getString(param_SUBID) == null
					|| task.getString(param_CALLERID) == null)
				return cross_copy_Resp_insuf;
		} else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_giftack)) {

			if (task.getString(param_SUBID) == null
					|| task.getString(param_CLIPID) == null
					|| task.getString(param_GIFTEDTO) == null
					|| task.getString(param_REQTIMESATMP) == null)
				return Resp_Err;
		} else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_gift)) {

			if (task.getString(param_SUBID) == null
					|| task.getString(param_GIFTEDBY) == null
					|| task.getString(param_REQTIMESATMP) == null)
				return Resp_Err;
		} else if (task.getString(param_api) != null
				&& task.getString(param_api).equals(api_subsats)) {

			if ((task.getString(param_SUBID) == null || task.getString(
					param_SUBID).equalsIgnoreCase("null"))
					&& (task.getString(param_MSISDN) == null || task.getString(
							param_MSISDN).equalsIgnoreCase("null")))
				return Resp_invalidParam;
		} else if (task.getTaskAction() != null
				&& task.getTaskAction().equals(action_vodactservice)) {

			if (task.getString(param_MSISDN) == null
					|| task.getString(param_MSISDN).equalsIgnoreCase("null"))
				return Resp_invalidParam;
		}

		if (task.containsKey(param_OBDSUBID)) {
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					task.getString(param_OBDSUBID));
			Subscriber sub = rbtClient.getSubscriber(rbtDetailsRequest);
			task.setObject(param_subscriber, sub);
		}

		logger.info("Returning: " + response);
		return response;
	}

	public boolean isValidPrefix(String subId) {
		boolean isValid = false;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subId);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		if (subscriber != null && subscriber.isValidPrefix())
			isValid = true;
		logger.info("Is valid prefix: " + isValid);
		return isValid;
	}

	public String getSubscriberDefaultVcode(Task task) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				task.getString(param_subID));
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		if (!isUserActive(subscriber.getStatus()))
			return "NOT_FOUND";
		Setting allSetting = null;
		RbtDetailsRequest request = new RbtDetailsRequest(
				task.getString(param_subID));
		Library library = rbtClient.getLibrary(request);
		if (library != null) {
			Settings settings = library.getSettings();
			if (settings != null) {
				Setting[] setting = settings.getSettings();
				if (setting != null) {
					for (int i = 0; i < setting.length; i++) {
						if (setting[i].getCallerID().equalsIgnoreCase("ALL"))
							allSetting = setting[i];
					}
				}
			}
		}
		if (allSetting == null)
			return "DEFAULT";
		else {
			if (allSetting.getToneType().equals(CATEGORY_SHUFFLE))
				return "ALBUM";
			else
				return allSetting.getRbtFile().replaceAll(".wav", "") + ":"
						+ allSetting.getCategoryID();
		}
	}

	public static String getRedirectionURL(Task task) {
		String redirectURL = null;
		logger.info("Task: " + task);
		String subID = null;
		if (task.getString(param_api) != null
				&& task.getString(param_api).trim().equalsIgnoreCase(api_copy)) {
			subID = task.getString(param_CALLERID);
			logger.info("RBT:: redirectURL 0: Subid" + subID);
		} else {
			subID = task.getString(param_SUBID);
			if (subID == null)
				subID = task.getString(param_subID);
			logger.info("RBT:: redirectURL 1: Subid" + subID);
		}
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subID);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		String circleID = subscriber.getCircleID();
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setCircleID(circleID);
		Site site = rbtClient.getSite(applicationDetailsRequest);
		if (site != null) {
			redirectURL = site.getSiteURL();
			if (task.containsKey(param_URL)) {
				logger.info("RBT:: redirectURL: contain param_url "
						+ task.getString(param_URL));
				if (redirectURL.endsWith("?"))
					redirectURL = redirectURL.substring(0,
							redirectURL.length() - 1);
				if (redirectURL.indexOf("/rbt_copy.jsp") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_copy.jsp", "");
				if (redirectURL.indexOf("/rbt_gift_acknowledge") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_gift_acknowledge.jsp", "");
				if (redirectURL.indexOf("/rbt_gift.jsp") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_gift.jsp", "");
				if (redirectURL.indexOf("/rbt_redirect.jsp") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_redirect.jsp", "");
				if (redirectURL.indexOf("/rbt_status.jsp") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_status.jsp", "");
				if (redirectURL.indexOf("/rbt_sms.jsp") != -1)
					redirectURL = Utility.findNReplaceAll(redirectURL,
							"/rbt_sms.jsp", "");
			}
			if (!redirectURL.endsWith("/"))
				redirectURL = redirectURL + "/" + task.getString(param_URL)
						+ "?";
			else
				redirectURL = redirectURL + task.getString(param_URL) + "?";
		}

		logger.info("RBT:: redirectURL: " + redirectURL);
		return redirectURL;
	}

	public void processToneCopyReq(Task task) {
		logger.info("Processtone copy request : " + task);
		String response = copy_Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String callerID = task.getString(param_CALLERID);
		Clip clip = null;
		int clipid = -1;
		try {

			if (subscriberID == null) {
				response = copy_Resp_Err + ":" + SUBSCRIBER_DOES_NOT_EXIST;
				task.setObject(param_response, response);
				return;
			}

			CopyRequest copyRequest = new CopyRequest(callerID, subscriberID);
			CopyDetails copyDetails = rbtClient.getCopyData(copyRequest);
			CopyData copydata[] = copyDetails.getCopyData();
			if (logger.isDebugEnabled()) {
				logger.debug("copy data response : "
						+ copyRequest.getResponse());
			}
			if (copyRequest.getResponse().equalsIgnoreCase("not_rbt_user")) {
				response = copy_Resp_Err + ":" + SUBSCRIBER_DOES_NOT_EXIST;
			} else if (copyRequest.getResponse().equalsIgnoreCase("success")) {
				String subWavFile = null;
				if (copydata != null)
					clipid = copydata[0].getToneID();

				clip = getClipById(clipid + "");
				if (logger.isDebugEnabled()) {
					logger.debug("In tonecopy clp wav1" + clip);
				}
				if (clip != null)
					subWavFile = clip.getClipRbtWavFile();
				logger.info("In tonecopy clp wav2" + subWavFile);
				if (subWavFile != null) {
					subWavFile = subWavFile.trim();
					if (subWavFile.indexOf("rbt_") != -1)
						subWavFile = subWavFile.substring(4);
					if (subWavFile.indexOf("_rbt") != -1)
						subWavFile = subWavFile.substring(0,
								subWavFile.length() - 4);
					response = copy_Resp_Success + ":" + subWavFile;
				} else
					response = copy_Resp_Err + ":" + RECORD_NOT_FOUND;
			} else if (copyRequest.getResponse()
					.equalsIgnoreCase("default_rbt")) {
				response = copy_Resp_Err + ":" + RECORD_NOT_FOUND;
			}
		} catch (Exception e) {
			logger.error("", e);
			response = copy_Resp_Err + ":" + SYNTAX_ERROR;
		}
		task.setObject(param_response, response);
	}

	public String processRbtPlayerHelperReq(Task task) {

		String response = Resp_Err;
		String action = task.getString(rbt_param_ACTION);
		String details = task.getString(rbt_param_DETAILS);
		String extraparam = task.getString(rbt_param_EXTRAPARAM);
		if (logger.isDebugEnabled()) {
			logger.debug("RBT:: processRbtplayerHelperReq" + " Action : "
					+ action + " Details :" + details + " extraparam : "
					+ extraparam);
		}
		if (action != null && action.equals(action_rbt_access)) {
			StringTokenizer tokens = new StringTokenizer(details.trim(), ":");
			String strSubscriberID = tokens.nextToken();
			SubscriptionRequest subreq = new SubscriptionRequest(
					strSubscriberID);
			subreq.setLastAccessDate(new Date());
			rbtClient.updateSubscription(subreq);

			if (subreq.getResponse() != null
					&& subreq.getResponse().equalsIgnoreCase("success"))
				response = Resp_Success;
			else
				response = Resp_Err;
		} else if (action != null && action.equals(action_rbt_poll)) {
			try {
				StringTokenizer tokens = new StringTokenizer(details.trim(),
						":");
				String strSubscriberID = tokens.nextToken();
				String strCallerID = tokens.nextToken();
				String strSubscriberWavFile = tokens.nextToken().trim();
				if (strSubscriberWavFile.endsWith(".wav"))
					strSubscriberWavFile = strSubscriberWavFile.substring(0,
							strSubscriberWavFile.length() - 4);
				String pollInput = tokens.nextToken().trim();
				DataRequest viraldataRequest = new DataRequest(strCallerID,
						"POLL");
				viraldataRequest.setSubscriberID(strSubscriberID);
				viraldataRequest.setClipID(strSubscriberWavFile + ":"
						+ pollInput);
				viraldataRequest.setCount(0);
				rbtClient.addViralData(viraldataRequest);
				if (viraldataRequest.getResponse() != null
						&& viraldataRequest.getResponse().equalsIgnoreCase(
								"success"))
					response = Resp_Success;
				else
					response = Resp_Err;
				logger.info("RBT:: processRbtPlayerHelperReq poll done : "
						+ response + " : " + details);
			} catch (Exception exe) {
				logger.error("", exe);
			}
		} else if (action != null && action.equals(action_rbt_copy)) {
			response = processPlayerHelper(task);
			logger.info("RBT:: processRbtPlayerHelperReq copy done : "
					+ response);
		}
		return response;
	}

	public String processESIAQuizForwarderReq(Task task) {
		String response = Resp_Failure;
		String msisdn = task.getString(param_MSISDN);
		String rbtpromoid = task.getString("RBTPROMOID");
		String chargeClass = task.getString("CHARGE_CLASS");
		logger.info("MSISDN:" + msisdn + ", rbtid(clip_promo_id):" + rbtpromoid);

		String clipIds = "0810752,0711702,2060369,0260138,0311227,1110379,0113799";
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter("DEAMON", "PROMO_ID_CROSS_PROMO");
		ArrayList<String> promoIdList = null;
		if (param != null && param.getValue() != null) {
			StringTokenizer st = new StringTokenizer(param.getValue(), ",");
			while (st.hasMoreElements()) {
				String tempval = st.nextToken();
				if (tempval != null && tempval.length() > 0) {
					if (promoIdList == null) {
						promoIdList = new ArrayList<String>();
					}
					promoIdList.add(tempval);
				}
			}
		}
		if (promoIdList != null && promoIdList.size() > 0
				&& promoIdList.contains(rbtpromoid)) {
			String url = "http://172.16.109.135:7001/WSPromo_Wrapper/service/CRBTAddPoint?dn="
					+ msisdn
					+ "&servicetype="
					+ chargeClass
					+ "&songcode="
					+ rbtpromoid + "&usr=onmobile&pwd=onmobile123";
			logger.info("URL:" + url);
			StringBuffer sbResponse = new StringBuffer();
			Integer status = new Integer(200);
			boolean useProxy = true;
			String proxyHost = getParameter(iRBTConstant.COMMON,
					"ESIA_QUIZ_PROXY_HOST");// ESIA_QUIZ_PROXY_HOST
			String port = getParameter(iRBTConstant.COMMON,
					"ESIA_QUIZ_PROXY_PORT");
			int proxyPort = 80;// ESIA_QUIZ_PROXY_PORT
			if (port != null) {
				try {
					proxyPort = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					useProxy = false;
				}
			} else {
				useProxy = false;
			}
			if (proxyHost == null || proxyHost.equalsIgnoreCase("null")
					|| proxyHost.equals(""))
				useProxy = false;
			boolean resp = Tools.callURL(url, status, sbResponse, useProxy,
					proxyHost, proxyPort);
			logger.info("URL Response:" + sbResponse.toString());
		} else if (clipIds.contains(rbtpromoid)) {
			String url = "http://202.93.240.210/esia/vopuasa.aspx?msisdn="
					+ msisdn + "&rbtid=" + rbtpromoid;
			logger.info("URL:" + url);
			StringBuffer sbResponse = new StringBuffer();
			Integer status = new Integer(200);
			boolean useProxy = true;
			String proxyHost = getParameter(iRBTConstant.COMMON,
					"ESIA_QUIZ_PROXY_HOST");// ESIA_QUIZ_PROXY_HOST
			String port = getParameter(iRBTConstant.COMMON,
					"ESIA_QUIZ_PROXY_PORT");
			int proxyPort = 80;// ESIA_QUIZ_PROXY_PORT
			if (port != null) {
				try {
					proxyPort = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					useProxy = false;
				}
			} else {
				useProxy = false;
			}
			if (proxyHost == null || proxyHost.equalsIgnoreCase("null")
					|| proxyHost.equals(""))
				useProxy = false;
			boolean resp = Tools.callURL(url, status, sbResponse, useProxy,
					proxyHost, proxyPort);
			logger.info("URL Response:" + sbResponse.toString());
		}
		return response;
	}

	private boolean isDefaultCopyNeeded() {
		String isCopy = getParameter("COMMON", copy_default_song);
		// returns false if isCopy is null
		return Boolean.getBoolean((isCopy));
	}

	public void processCrossGiftReq(Task task) {
		String response = copy_Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String callerID = task.getString(param_CALLERID);
		String smsType = task.getString(param_SMSTYPE);
		String wavFile = task.getString(param_wavfile);
		String clipId = null;
		Clip clip = null;
		try {
			if (wavFile != null)
				clip = getClipByWavFile("rbt_" + wavFile.trim() + "_rbt");
			logger.info("clip: " + clip);
			if (clip != null)
				clipId = clip.getClipId() + "";
			if (smsType == null || smsType.equalsIgnoreCase("null"))
				smsType = "GIFT_CHARGED";
			if (subscriberID == null || callerID == null)
				response += SYNTAX_ERROR;
			else {
				if (clipId != null && !clipId.equalsIgnoreCase("null")) {
					DataRequest viraldataRequest = new DataRequest(callerID,
							smsType);
					viraldataRequest.setSubscriberID(subscriberID);
					viraldataRequest.setClipID(clipId);
					viraldataRequest.setCount(0);
					rbtClient.addViralData(viraldataRequest);
					if (viraldataRequest.getResponse() != null
							&& viraldataRequest.getResponse().equalsIgnoreCase(
									"success"))
						response = Resp_Success;
					else
						response += SYNTAX_ERROR;
				} else
					response += SYNTAX_ERROR;

			}

		} catch (Exception e) {
			logger.error("In processcrossgiftRequest" + e);
			response = copy_Resp_Err + SYNTAX_ERROR;
		}
		task.setObject(param_response, response);
	}

	public void processSubProfileRequest(Task task) {
		String response = Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriberID);
			rbtDetailsRequest.setMode("CCC");
			Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
			logger.info("Subscriber " + subscriber);
			if (subscriber == null) {
				response = Resp_Err + ":" + SUBSCRIBER_DOES_NOT_EXIST;
			} else if (subscriber.getStatus() == null
					|| subscriber.getStartDate() == null) {
				response = Resp_Err + ":" + DB_ERROR;
			} else {
				String strDate = sdf.format(subscriber.getStartDate());
				String status = subscriber.getStatus();
				logger.info("Subscriber status strdate : " + status + ":"
						+ strDate);

				if (status.equals("act_pending")
						|| status.equalsIgnoreCase("grace"))
					response = "SUCCESS:" + strDate + ":" + 1;

				else if (status.equals("active"))
					response = "SUCCESS:" + strDate + ":" + 0;
				else if (status.equals("deact_pending"))
					response = "SUCCESS:" + strDate + ":" + 2;
				else if (status.equalsIgnoreCase("deactive"))
					response = "SUCCESS:" + strDate + ":" + 4;
				else if (status.equalsIgnoreCase("suspended"))
					response = "SUCCESS:" + strDate + ":" + 12;
			}
		} catch (Exception e) {
			logger.error("In processsubprofileRequest" + e);
			response = Resp_Err + DB_ERROR;
		}
		task.setObject(param_response, response);
	}

	public void processComboSubStatusRequest(Task task) {
	}

	public void processSubStatusRequest(Task task) {
		String response = Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		if (subscriberID == null) {
			task.setObject(param_response, response);
			return;
		}
		task.setObject(param_subscriberID, subscriberID);
		getSubscriber(task);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		try {

			if (isUserActive(subscriber.getStatus())) {
				response = Resp_Active;
			} else {
				response = Resp_Inactive;
			}
		} catch (Exception e) {
			logger.error("In processubstatusRequest" + e);
			response = Resp_Err;
		}
		task.setObject(param_response, response);
	}

	public void processGiftRequest(Task task) {
		logger.info("In processGiftRequest" + task);
		String response = Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String extraInfo = task.getString(param_EXTRAINFO);
		String clipId = task.getString(param_CLIPID);
		String giftedBy = task.getString(param_GIFTEDBY);
		String mode = task.getString(param_MODE);
		String requestedTimestamp = task.getString(param_REQTIMESATMP);
		String senderNum = getParameter("DAEMON", "GIFT_SENDER_NUMBER");
		String senderNumber = giftedBy;
		String smsText = "I have gifted a RBT to you.";
		
		String validateGifter = getParameter("COMMON", "DONT_VALID_GIFTER_SENDING_GIFT");
		boolean toValidateGifter = validateGifter == null ? true : validateGifter.equalsIgnoreCase("TRUE");
		
		boolean bNational = true;
		if (senderNum != null && !senderNum.trim().equalsIgnoreCase(""))
			senderNumber = senderNum;
		Subscriber giftee = (Subscriber) task.getObject(param_subscriber);
		if (giftee.isValidPrefix() && (isValidPrefix(giftedBy) || !toValidateGifter)) {
			bNational = false;
		}
		
		if(mode == null) {
			mode = "SMS";
		}
		
		try {
			if (clipId == null || clipId.trim().equals("null")) {
				String serviceSmstext = smsTextCacheManager
						.getSmsText("DAEMON", "GIFT_SERVICE_SMS_TEXT",
								giftee.getLanguage());
				if (serviceSmstext != null
						&& !serviceSmstext.trim().equalsIgnoreCase(""))
					smsText = serviceSmstext;
				smsText = Utility.findNReplaceAll(smsText, "%S", giftedBy);
				logger.info("In processGiftRequest clipid is null");
				try {
					sendSMS(senderNumber, giftee, smsText, false);
				} catch (Exception e) {
					response = Resp_Err;
				}
				response = Resp_Success;
			} else {

				String clipName = null;
				if (clipId.startsWith("C")) {
					com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = getCategory(clipId
							.substring(1));
					clipName = category.getCategoryName();

				} else {
					Clip clips = getClipById(clipId);
					clipName = clips.getClipName();
				}

				if (clipName == null || clipName.trim().equalsIgnoreCase("")) {
					response = Resp_Err;
				} else {
					smsText = "I have gifted a RBT to you. To accept this gift call XXXXXX";
					
					String giftSmstext = smsTextCacheManager.getSmsText(
							"DAEMON", "GIFT_SMS_TEXT_" + mode.toUpperCase(), giftee.getLanguage());
					
					if(giftSmstext == null) {
						giftSmstext = smsTextCacheManager.getSmsText(
							"DAEMON", "GIFT_SMS_TEXT", giftee.getLanguage());
					}
					
					if (giftSmstext != null
							&& !giftSmstext.trim().equalsIgnoreCase(""))
						smsText = giftSmstext;
					smsText = Utility.findNReplaceAll(smsText, "%S", giftedBy);
					smsText = Utility.findNReplaceAll(smsText, "%C", clipName);
					sendSMS(senderNumber, giftee, smsText, false);
					response = Resp_Success;
					if (bNational) {
						logger.error("RBT:: In processGiftRequest comin to addviraldata "
								+ task);
						long lTime = new Long(requestedTimestamp).longValue();
						logger.error("RBT:: In processGiftRequest ITIME "
								+ lTime);
						task.setObject(param_subscriberID, giftedBy);
						task.setObject(param_DATE, new Date(lTime));
						task.setObject(param_SMSTYPE, "GIFTED");
						task.setObject(param_callerid, subscriberID);
						task.setObject(param_CLIPID, clipId);
						task.setObject(param_SEARCHCOUNT, "0");
						task.setObject(param_SELECTED_BY, mode);
						task.setObject(param_EXTRAINFO, extraInfo);
						logger.error("RBT:: In processGiftRequest addviraldata "
								+ task);
						ViralData viraldata = addViraldata(task);
						if (viraldata != null)
							response = Resp_Success;
					}

					logger.info("RBT:: In processGiftRequest resp " + response);
				}
			}
		} catch (Exception e) {
			logger.error("RBT:: In processGiftRequest" + e);
			response = Resp_Err;
		}
		task.setObject(param_response, response);
	}

	public void processGiftAckRequest(Task task) {
		logger.info("In processGiftAckRequest : " + task);
		String response = Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String clipId = task.getString(param_CLIPID);
		String giftedTo = task.getString(param_GIFTEDTO);
		String status = task.getString(param_STATUS);
		String requestedTimestamp = task.getString(param_REQTIMESATMP);
		task.setObject(param_response, response);
		try {
			Subscriber gifter = (Subscriber) task.getObject(param_subscriber);

			String acceptSmsText = "Your gift of %C has been accepted by %S. You have been charged Rs.%A for the gifting";
			String acceptSms = smsTextCacheManager.getSmsText("DAEMON",
					"GIFT_ACCEPT_SMS_TEXT", gifter.getLanguage());
			if (acceptSms != null && !acceptSms.trim().equalsIgnoreCase(""))
				acceptSmsText = acceptSms;

			String rejectSmsText = "Your gift of %C has been rejected by %S. You have been charged Rs.%A for the gifting";
			String rejectSms = smsTextCacheManager.getSmsText("DAEMON",
					"GIFT_REJECT_SMS_TEXT", gifter.getLanguage());
			if (rejectSms != null && !rejectSms.trim().equalsIgnoreCase(""))
				rejectSmsText = rejectSms;

			String serviceAcceptSmsText = "Your service gift has been accepted by %S. You have been charged Rs.%A for the gifting";
			String serviceAccept = smsTextCacheManager.getSmsText("DAEMON",
					"GIFT_SERVICE_ACCEPT_SMS_TEXT", gifter.getLanguage());
			if (serviceAccept != null
					&& !serviceAccept.trim().equalsIgnoreCase(""))
				serviceAcceptSmsText = serviceAccept;

			String serviceRejectSmsText = "Your serivce gift has been rejected by %S. You have been charged Rs.%A for the gifting";
			String serviceReject = smsTextCacheManager.getSmsText("DAEMON",
					"GIFT_SERVICE_REJECT_SMS_TEXT", gifter.getLanguage());
			if (serviceReject != null
					&& !serviceReject.trim().equalsIgnoreCase(""))
				serviceRejectSmsText = serviceReject;

			String use_pool = getParameter("DAEMON", "USE_POOL");
			if (use_pool != null && use_pool.equalsIgnoreCase("false"))
				usePool = false;
			else
				usePool = true;
			String clipName = null;
			if (clipId != null && clipId.startsWith("C")) {

				com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = getCategory(clipId
						.substring(1));
				clipName = category.getCategoryName();

			} else if (clipId != null && !clipId.equalsIgnoreCase("null")) {
				Clip clip = getClipById(clipId);
				clipName = clip.getClipName();
			}

			if (!clipId.equalsIgnoreCase("null")
					&& (clipName == null || clipName.trim()
							.equalsIgnoreCase(""))) {
				response = Resp_Err;
			} else {
				if (clipId.equalsIgnoreCase("null")) {
					acceptSmsText = serviceAcceptSmsText;
					rejectSmsText = serviceRejectSmsText;
				}
				acceptSmsText = Utility.findNReplaceAll(acceptSmsText, "%S",
						giftedTo);
				acceptSmsText = Utility.findNReplaceAll(acceptSmsText, "%C",
						clipName);

				rejectSmsText = Utility.findNReplaceAll(rejectSmsText, "%S",
						giftedTo);
				rejectSmsText = Utility.findNReplaceAll(rejectSmsText, "%C",
						clipName);

				String senderNumber = getParameter("DAEMON",
						"GIFT_ACKNOWLEDGE_SENDER_NUMBER");

				if (senderNumber == null
						|| senderNumber.trim().equalsIgnoreCase(""))
					senderNumber = giftedTo;
				logger.info("RBT:: In processGiftAckRequest" + "sub id : "
						+ subscriberID + "gifted to " + ": " + giftedTo
						+ "sender no : " + senderNumber + "clipid : " + clipId
						+ "clip name : " + clipName);
				boolean update = false;
				if (status.equalsIgnoreCase("ACCEPT_ACK")) {
					logger.info("RBT: processGiftAckRequest: ACCEPT_ACK ");
					update = updateViralpromotion(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"GIFTED", "ACCEPTED",
							new Date(System.currentTimeMillis()), null);
					update = updateViralpromotion(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"ACCEPT_ACK", "ACCEPTED",
							new Date(System.currentTimeMillis()), null);
					ViralData viraldata = getViralData(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"ACCEPTED");
					String amount = "10";
					if (viraldata != null && viraldata.getSelectedBy() != null){
						int index = viraldata.getSelectedBy().indexOf(":");
						if(index != -1) {
							amount = viraldata.getSelectedBy().substring(
								index + 1);
						}else {
							if(viraldata.getInfoMap() != null) {
								String amountCharged = viraldata.getInfoMap().get("aountCharged");
								if(amountCharged != null) {
									amount = amountCharged;
								}
							}
						}
					}
					acceptSmsText = Utility.findNReplaceAll(acceptSmsText,
							"%A", amount);
					sendSMS(senderNumber, gifter, acceptSmsText, false);
					logger.info("RBT: processGiftAckRequest: ACCEPT_ACK over "
							+ acceptSmsText + subscriberID);
				} else if (status.equalsIgnoreCase("REJECT_ACK")) {
					logger.info("RBT: processGiftAckRequest: REJECT_ACK ");
					update = updateViralpromotion(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"GIFTED", "REJECTED",
							new Date(System.currentTimeMillis()), null);
					logger.info("RBT: processGiftAckRequest:update viral promo 1 ");
					update = updateViralpromotion(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"REJECT_ACK", "REJECTED",
							new Date(System.currentTimeMillis()), null);
					logger.info("RBT: processGiftAckRequest:update viral promo 2 ");
					ViralData viraldata = getViralData(subscriberID, giftedTo,
							new Date(new Long(requestedTimestamp).longValue()),
							"ACCEPTED");
					logger.info("RBT: processGiftAckRequest : viraldata.getselby "
							+ viraldata);
					String amount = "10";
					if (viraldata != null && viraldata.getSelectedBy() != null) {
						int index = viraldata.getSelectedBy().indexOf(":");
						if(index != -1) {
							amount = viraldata.getSelectedBy().substring(
								index + 1);
						}else {
							if(viraldata.getInfoMap() != null) {
								String amountCharged = viraldata.getInfoMap().get("aountCharged");
								if(amountCharged != null) {
									amount = amountCharged;
								}
							}
						}
					}

					rejectSmsText = Utility.findNReplaceAll(rejectSmsText,
							"%A", amount);
					logger.info("RBT: processGiftAckRequest: REJECT_ACK rejsms"
							+ rejectSms);
					sendSMS(senderNumber, gifter, rejectSmsText, false);
					logger.info("RBT: processGiftAckRequest: REJECT_ACK over"
							+ rejectSmsText + subscriberID);
				}

				response = "SUCCESS";
			}

		} catch (Exception e) {
			logger.error("RBT:: In processGiftAckRequest" + e);
			response = Resp_Err;
		}
		task.setObject(param_response, response);
	}

	public void processCopyRequest(Task task) {
		String response = copy_Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String callerID = task.getString(param_CALLERID);
		String clipID = task.getString(param_CLIPID);
		String smsType = null;
		String selBy = task.getString(param_SELBY);
		String songName = task.getString(param_SONGNAME);
		String keyPressed = task.getString(param_KEYPRESSED);
		try {

			smsType = getSmsType(callerID, keyPressed, task, "COPY");
			DataRequest viraldataRequest = new DataRequest(callerID, smsType);
			viraldataRequest.setSubscriberID(subscriberID);
			viraldataRequest.setClipID(clipID);
			viraldataRequest.setMode(selBy);
			viraldataRequest.setCount(0);
			HashMap<String, String> hashMap = new HashMap<String, String>();
			if (songName != null && songName.length() > 0
					&& !songName.equalsIgnoreCase("null"))
				hashMap.put(SOURCE_WAV_FILE_ATTR, songName);
			if (keyPressed != null && keyPressed.length() > 0
					&& !keyPressed.equalsIgnoreCase("null"))
				hashMap.put(KEYPRESSED_ATTR, keyPressed);
			viraldataRequest.setInfoMap(hashMap);
			rbtClient.addViralData(viraldataRequest);
			if (viraldataRequest.getResponse() != null
					&& viraldataRequest.getResponse().equalsIgnoreCase(
							"success"))
				response = copy_Resp_Success;
			else
				response = copy_Resp_Fail;
		} catch (Exception e) {
			logger.error("In processcopyRequest" + e);
			response = copy_Resp_Err;
		}
		task.setObject(param_response, response);
	}

	private String getSmsType(String subscriberID, String keyPressed,
			Task task, String defaultValue) {
		String circleID = null;
		SubscriberDetail subscriberDetail = RbtServicesMgr
				.getSubscriberDetail(new MNPContext(subscriberID, "COPY"));
		if (subscriberDetail != null)
			circleID = subscriberDetail.getCircleID();
		String type = null;
		boolean circleConfigPresent = false;
		boolean foundMatch = false;

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
				 //RBT-10651
	             boolean condition=false;
	             logger.info("Service processor keyPressed is :"+keyPressed);
	             String keySuffix=getParameter("COMMON","COPY_KEY_SUFFIX");
	             logger.info("keySuffix :"+keySuffix);
	            
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
				if (keyPressed != null && starCopyKeys != null) {
					for (String key : starCopyKeys) {
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
			}//RBT-14671 - # like
			if (type == null && keyPressed != null && toLikeKeys != null) {
				for (String keyFromConf : toLikeKeys) {
					if (keyPressed
							.indexOf(keyFromConf.toString().toLowerCase()) != -1) {
						type = LIKE;
						break;
					}
				}
			}

			if (type == null)
				type = task.getString(param_SMSTYPE);
			if (type == null)
				type = defaultValue;
		}
		return type;
	}

	public void validateAndProcessCopyRequest(Task task) {
		/*
		 * Url :
		 * http://ip:port/interfaces/ttmlexpresscopy.do?operatoraccount=aaaaaa
		 * &operatorpwd=aaaaaa
		 * &srcphonenumber=9240000000&phonenumber=9240000000&
		 * tonecode=1111000013&
		 * operator=19&submittime=20091023101010&keypressed=s Responses 99
		 * Request Accepted Successfully 1. Invalid Operator Parameter(s). 2.
		 * Input Parameter(s) Format error 3 Invalid user phone number (user is
		 * in blacklist) 4 Request already under process 8 Portal error 16 The
		 * configuration TTML express copy is not supported.
		 */
		String response = "2";
		String operatoraccount = task.getString("operatoraccount");
		String operatorpwd = task.getString("operatorpwd");
		String srcphonenumber = task.getString("srcphonenumber");
		String phonenumber = task.getString("phonenumber");
		String tonecode = task.getString("tonecode");
		String operator = task.getString("operator");
		String submittime = task.getString("submittime");
		String keypressed = task.getString("keypressed");

		try {
			if (operatoraccount == null
					|| operatorpwd == null
					|| !operatoraccounts.containsKey(operatoraccount)
					|| !operatoraccounts.get(operatoraccount).equals(
							operatorpwd))
				response = "1";
			else if (srcphonenumber == null || phonenumber == null
					|| tonecode == null || operator == null)
				response = "2";
			else {
				Clip clip = rbtCacheManager.getClipByPromoId(tonecode);
				if (clip == null
						|| clip.getClipEndTime().getTime() < System
								.currentTimeMillis())
					response = "2";
				else {
					String smsType = null;
					if (keypressed != null && normalCopyKeys != null) {
						for (String key : normalCopyKeys) {
							if (keypressed.indexOf(key) != -1)
								smsType = "COPY";
						}
					}
					if (smsType == null) {
						if (keypressed != null && starCopyKeys != null) {
							for (String key : starCopyKeys) {
								if (keypressed.indexOf(key) != -1)
									smsType = "COPYSTAR";
							}
						}
					}
					if (smsType == null)
						smsType = "COPY";
					DataRequest viraldataRequest = new DataRequest(phonenumber,
							smsType);
					viraldataRequest.setSubscriberID(srcphonenumber);
					viraldataRequest.setClipID(clip.getClipRbtWavFile()
							+ ":26:1");
					viraldataRequest.setCount(0);
					HashMap<String, String> hashMap = new HashMap<String, String>();
					String songName = clip.getClipName();
					if (songName != null && songName.length() > 0
							&& !songName.equalsIgnoreCase("null"))
						hashMap.put(SOURCE_WAV_FILE_ATTR, songName);
					if (keypressed != null && keypressed.length() > 0
							&& !keypressed.equalsIgnoreCase("null"))
						hashMap.put(KEYPRESSED_ATTR, keypressed);
					viraldataRequest.setInfoMap(hashMap);
					rbtClient.addViralData(viraldataRequest);
					if (viraldataRequest.getResponse() != null
							&& viraldataRequest.getResponse().equalsIgnoreCase(
									"success"))
						response = "99";
					else
						response = "8";
				}
			}
		} catch (Exception e) {
			logger.error("In validateAndProcessCopyRequest" + e);
			response = "8";
		}
		task.setObject(param_response, response);
	}

	public void processCrossCopyRequest(Task task) {
		String response = cross_copy_Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String callerID = task.getString(param_CALLERID);
		String clipID = task.getString(param_CLIPID);
		String smsType = null;
		String selBy = task.getString(param_SELBY);
		String songName = task.getString(param_SONGNAME);
		String keyPressed = task.getString(param_KEYPRESSED);
		String promoid = task.getString(param_TONECODE);

		try {
			
			smsType = getSmsType(callerID, keyPressed, task, null);
			if (keyPressed != null && smsType == null) {
				logger.info("Copy failed for " + callerID
						+ " as keypressed is not a copy key.");
				task.setObject(param_response, cross_copy_Resp_Success);
				return;
			}
			DataRequest viraldataRequest = new DataRequest(callerID, smsType);
			viraldataRequest.setSubscriberID(subscriberID);
			viraldataRequest.setClipID(clipID);
			viraldataRequest.setMode(selBy);
			viraldataRequest.setCount(0);
			HashMap<String, String> hashMap = new HashMap<String, String>();
			if (songName != null && songName.length() > 0
					&& !songName.equalsIgnoreCase("null"))
				hashMap.put(SOURCE_WAV_FILE_ATTR, songName);
			if (keyPressed != null && keyPressed.length() > 0
					&& !keyPressed.equalsIgnoreCase("null"))
				hashMap.put(KEYPRESSED_ATTR, keyPressed);
			if (promoid != null && promoid.length() > 0
					&& !promoid.equalsIgnoreCase("null"))
				hashMap.put(PROMOID_ATTR, promoid);
			viraldataRequest.setInfoMap(hashMap);
			rbtClient.addViralData(viraldataRequest);
			if (!smsType.equalsIgnoreCase("LIKE"))// RBT-14671 - # like
				addLikeViralData(viraldataRequest, keyPressed);
			if (viraldataRequest.getResponse() != null
					&& viraldataRequest.getResponse().equalsIgnoreCase(
							"success"))
				response = cross_copy_Resp_Success;
			else
				response = cross_copy_Resp_Err;
		} catch (Exception e) {
			System.out.println("excep cross : " + e);
			logger.error("In processcrosscopyRequest" + e);
			response = cross_copy_Resp_Err;
		}
		task.setObject(param_response, response);
	}

	public void processCrossCopyRdcRequest(Task task) {

		String response = cross_copy_Resp_Err;
		String subscriberID = task.getString(param_SUBID);
		String callerID = task.getString(param_CALLERID);
		String clipID = task.getString(param_CLIPID);
		String smsType = null;
		String selBy = task.getString(param_SELBY);
		String finalWavFile = clipID;
		String songName = task.getString(param_SONGNAME);
		String keyPressed = task.getString(param_KEYPRESSED);
		if (keyPressed == null || keyPressed.trim().length() == 0)
			keyPressed = "s9";
		smsType = getSmsType(callerID, keyPressed, task, null);

		if (keyPressed != null && smsType == null) {
			logger.info("Copy failed for " + callerID
					+ " as keypressed is not a copy key.");
			response = cross_copy_Resp_Success;
			task.setObject(param_response, response);
			return;
		}
		if (clipID != null && clipID.toUpperCase().indexOf("MISSING") == -1)

		{
			finalWavFile = "ERROR";
			String clipIDFromRdc = new StringTokenizer(clipID, ":").nextToken()
					.trim();
			try {
				Clip clip = getClipById(clipIDFromRdc);
				logger.info("RBT:: Cross copy Clp: " + clip);
				if (clip != null)
					finalWavFile = clip.getClipRbtWavFile();
				logger.info("RBT:: finaleavfile: " + finalWavFile);
			} catch (Exception e) {
			}
		}
		try {
			DataRequest viraldataRequest = new DataRequest(callerID, smsType);
			viraldataRequest.setSubscriberID(subscriberID);
			viraldataRequest.setClipID(finalWavFile);
			viraldataRequest.setCount(0);
			viraldataRequest.setMode(selBy);
			HashMap<String, String> hashMap = new HashMap<String, String>();
			if (songName != null && songName.length() > 0
					&& !songName.equalsIgnoreCase("null"))
				hashMap.put(SOURCE_WAV_FILE_ATTR, songName);
			if (keyPressed != null && keyPressed.length() > 0
					&& !keyPressed.equalsIgnoreCase("null"))
				hashMap.put(KEYPRESSED_ATTR, keyPressed);
			viraldataRequest.setInfoMap(hashMap);
			rbtClient.addViralData(viraldataRequest);
			if (viraldataRequest.getResponse() != null
					&& viraldataRequest.getResponse().equalsIgnoreCase(
							"success"))
				response = cross_copy_Resp_Success;
			else
				response = cross_copy_Resp_Err;
		} catch (Exception e) {
			logger.error("In processcrosscopyRdcRequest" + e);
			response = cross_copy_Resp_Err;
		}
		task.setObject(param_response, response);
	}

	public String processMeraHelloTuneRequest(Task task) {
		logger.info("In ProcessHelloTuneRequest : " + task);
		String response = ugc_Resp_Fail;
		String subscriberId = task.getString(meraHT_param_SINGER);
		String clipName = task.getString(meraHT_param_CLIPNAME);
		String channel = task.getString(meraHT_param_CHANNEL);
		String consentLog = task.getString(meraHT_param_CONSENT_LOG);
		String categId = task.getString(meraHT_param_CATEGORY_ID);
		String subcategory_id = task.getString(meraHT_param_SUBCATEGORY_ID);
		String clipEndTime = task.getString(meraHT_param_EXPIRY_DATE);
		String copyRightId = task.getString(meraHT_param_COPYRIGHT_ID);
		String publisherId = task.getString(meraHT_param_PUBLISHER_ID);
		String albummovie = task.getString(meraHT_param_ALBUM);
		String langId = task.getString(meraHT_param_LANGUAGE_ID);
		String vcode = task.getString(meraHT_param_VCODE);
		String ccode = task.getString(meraHT_param_CCODE);
		// add the entry into the RBT_UGC_CLIPS
		String languageMap = getParameter("COMMON", "UGC_LANGUAGE_MAP");
		String publisherMap = getParameter("COMMON", "UGC_PUBLISHER_MAP");
		String[] languages = languageMap.split(",");
		String[] publishers = publisherMap.split(",");
		HashMap<String, String> languagesMap = new HashMap<String, String>();
		HashMap<String, String> publishersMap = new HashMap<String, String>();
		for (int i = 0; i < languages.length; i++) {
			String[] tokens = languages[i].split("=");
			languagesMap.put(tokens[0], tokens[1]);
		}
		for (int j = 0; j < publishers.length; j++) {
			String[] tokens = publishers[j].split("=");
			publishersMap.put(tokens[0], tokens[1]);
		}
		if (categId != null && !categId.equalsIgnoreCase("75")) {
			return 1 + "";
		}
		if (subcategory_id != null && !subcategory_id.equalsIgnoreCase("1")) {
			return 2 + "";
		}
		if (copyRightId != null && !copyRightId.equalsIgnoreCase("1395")) {
			return 4 + "";
		}

		String language = languagesMap.get(langId);
		if (language == null) {
			return 5 + "";
		}

		if (publisherId != null) {
			boolean value = publishersMap.containsKey(publisherId);
			if (!value) {
				logger.info("invalid publisher id");
				return 6 + "";
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		sdf.setLenient(false);
		try {
			date = sdf.parse(clipEndTime);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Exception while parsing the date" + e.getMessage());
			return 3 + "";
		}

		// add the entry into RBT_UGC_CLIPS
		UgcClip ugcClip = new UgcClip();
		ugcClip.setAlbum(albummovie);
		ugcClip.setSubscriberId(subscriberId);
		ugcClip.setArtist("");
		ugcClip.setCategoryId(Integer.parseInt(categId));
		ugcClip.setClipEndTime(date);
		ugcClip.setClipExtraInfo("");
		ugcClip.setClipName(clipName);
		ugcClip.setClipStartTime(new Date());
		ugcClip.setRightsBody(Integer.parseInt(copyRightId));
		ugcClip.setLanguage(languagesMap.get(langId));
		ugcClip.setPublisher(publishersMap.get(publisherId));
		ugcClip.setClipStatus('n');

		ugcClip.setClipRbtWavFile(vcode);
		ugcClip.setClipPromoId(ccode);
		String temp = null;
		if (consentLog != null) {
			temp = meraHT_param_CONSENT_LOG + "=" + consentLog;
		}
		if (channel != null) {
			temp = temp + "," + meraHT_param_CHANNEL + "=" + channel;
		}
		logger.debug("ClipExtraInfo==" + temp);
		ugcClip.setClipExtraInfo(temp);
		UgcClip ugcClipExists = null;
		UgcClip ugcCLIP = null;
		try {
			ugcClipExists = UgcClipDAO.getUgcClipByPromoId(ccode);
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (ugcClipExists != null) {
			return -1 + "";
		} else {
			try {
				ugcCLIP = UgcClipDAO.saveClip(ugcClip);
			} catch (DataAccessException e) {
				e.printStackTrace();
				return -1 + "";
			}
		}
		// if successful
		if (ugcCLIP != null) {
			return vcode + ":" + ccode;
		} else {
			return -1 + "";
		}
	}

	public String processUGCRequestOthers(Task task) {
		logger.info("In ProcessHelloTuneRequest : " + task);
		String response = ugc_Resp_Fail;
		String subscriberId = task.getString(meraHT_param_SINGER);
		String clipName = task.getString(meraHT_param_CLIPNAME);
		String categId = task.getString(meraHT_param_CATEGORY_ID);
		String subcategory_id = task.getString(meraHT_param_SUBCATEGORY_ID);
		String clipEndTime = task.getString(meraHT_param_EXPIRY_DATE);
		String copyRightId = task.getString(meraHT_param_COPYRIGHT_ID);
		String publisherId = task.getString(meraHT_param_PUBLISHER_ID);
		String albummovie = task.getString(meraHT_param_ALBUM);
		String langId = task.getString(meraHT_param_LANGUAGE_ID);
		String vcode = task.getString(meraHT_param_VCODE);
		String ugcStartTag = "<UGC>";
		String responseStartTag = "<RESPONSE>";
		String responseEndTag = "</RESPONSE>";
		String messageStartTag = "<MESSAGE>";
		String messageEndTag = "</MESSAGE>";
		String ugcEndTag = "</UGC>";

		// add the entry into the RBT_UGC_CLIPS
		String languageMap = getParameter("COMMON", "UGC_LANGUAGE_MAP");
		String publisherMap = getParameter("COMMON", "UGC_PUBLISHER_MAP");
		String[] languages = languageMap.split(",");
		String[] publishers = publisherMap.split(",");
		HashMap<String, String> languagesMap = new HashMap<String, String>();
		HashMap<String, String> publishersMap = new HashMap<String, String>();
		for (int i = 0; i < languages.length; i++) {
			String[] tokens = languages[i].split("=");
			languagesMap.put(tokens[0], tokens[1]);
		}
		for (int j = 0; j < publishers.length; j++) {
			String[] tokens = publishers[j].split("=");
			publishersMap.put(tokens[0], tokens[1]);
		}

		int languageId = Integer.parseInt(langId);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		sdf.setLenient(false);
		try {
			date = sdf.parse(clipEndTime);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Exception while parsing the date" + e.getMessage());
			StringBuffer sb = new StringBuffer();
			sb.append(ugcStartTag);
			sb.append(responseStartTag);
			sb.append("FAILURE");
			sb.append(responseEndTag);
			sb.append(messageStartTag);
			sb.append("INVALIDEXPIRYDATE");
			sb.append(messageEndTag);
			sb.append(ugcEndTag);
			return sb.toString();
		}

		// add the entry into RBT_UGC_CLIPS
		UgcClip ugcClip = new UgcClip();
		ugcClip.setAlbum(albummovie);
		ugcClip.setSubscriberId(subscriberId);
		ugcClip.setArtist("");
		ugcClip.setCategoryId(Integer.parseInt(categId));
		ugcClip.setClipEndTime(date);
		ugcClip.setClipExtraInfo("");
		ugcClip.setClipName(clipName);
		ugcClip.setClipStartTime(new Date());
		ugcClip.setRightsBody(Integer.parseInt(copyRightId));
		ugcClip.setLanguage(languagesMap.get(langId));
		ugcClip.setPublisher(publishersMap.get(publisherId));
		ugcClip.setClipStatus('n');

		ugcClip.setClipRbtWavFile(vcode);
		// ugcClip.setClipPromoId(ccode);
		ugcClip.setClipExtraInfo(null);
		UgcClip ugcClipExists = null;
		UgcClip ugcCLIP = null;

		StringBuffer strBuff = new StringBuffer();
		try {
			ugcCLIP = UgcClipDAO.saveClip(ugcClip);
		} catch (DataAccessException e) {
			e.printStackTrace();
			strBuff.append(ugcStartTag);
			strBuff.append(responseStartTag);
			strBuff.append("FAILURE");
			strBuff.append(responseEndTag);
			strBuff.append(messageStartTag);
			strBuff.append("TECHNICAL_DIFFICULTIES");
			strBuff.append(messageEndTag);
			strBuff.append(ugcEndTag);
			return strBuff.toString();
		}

		// if successful
		if (ugcCLIP != null) {
			strBuff = new StringBuffer("");
			strBuff.append(ugcStartTag);
			strBuff.append(responseStartTag);
			strBuff.append("SUCCESS");
			strBuff.append(responseEndTag);
			strBuff.append(messageStartTag);
			strBuff.append("" + ugcCLIP.getClipRbtWavFile() + ":"
					+ ugcCLIP.getClipPromoId());
			strBuff.append(messageEndTag);
			strBuff.append(ugcEndTag);
			return strBuff.toString();
		} else {

			strBuff = new StringBuffer("");
			strBuff.append(ugcStartTag);
			strBuff.append(responseStartTag);
			strBuff.append("FAILURE");
			strBuff.append(responseEndTag);
			strBuff.append(messageStartTag);
			strBuff.append("TECHNICAL_DIFFICULTIES");
			strBuff.append(messageEndTag);
			strBuff.append(ugcEndTag);
			return strBuff.toString();
		}
	}

	public String processUGCRequest(Task task) {
		logger.info("In processUGCRequest : " + task);
		String response = ugc_Resp_Fail;
		String subscriberID = task.getString(ugc_param_SUBID);
		String promoID = task.getString(ugc_param_PROMOID);
		String strTransID = task.getString(ugc_param_TRANSID);
		String action = task.getString(ugc_param_ACTION);
		String wavfile = task.getString(ugc_param_WAVFILE);
		String contentType = task.getString(param_contentType);
		String pathDir = getParameter("SMS", default_report_PATHDIR);
		logger.info("In processUGCRequest : pathdir" + pathDir);
		java.io.File oldfile = null;

		try {
			if (contentType != null
					&& contentType.equalsIgnoreCase("emotion_ugc_clips")
					&& action.equalsIgnoreCase(action_ugc_add)) {
				if (wavfile != null && wavfile.endsWith(".wav")) {
					oldfile = new java.io.File(wavfile);
					String strFile = oldfile.getName();

					// removes .wav extension from wav file name
					String clipName = strFile
							.substring(0, strFile.length() - 4);

					Clip clip = rbtCacheManager
							.getClipByRbtWavFileName(clipName);

					// If clip is not yet gone live.. cache
					// returns null
					if (clip == null) {
						ClipsDAO.getClipByRbtWavFileName(clipName);
					}

					logger.info("clipName: " + clipName + " Clip Object: "
							+ clip);
					if (clip != null) {
						// Clip already exists in DB and user recorded new clip
						// again with the same name
						// Setting clip endTime to 2004/01/01 , so as to make
						// UGCProcessor copy the clip to all webserver/telephony
						// servers
						java.io.File newfile = new java.io.File(pathDir
								+ File.separator + strFile);
						Utility.copyFile(oldfile, newfile);
						oldfile.delete();

						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy/MM/dd");
						clip.setClipEndTime(sdf.parse("2004/01/01"));
						ClipsDAO.updateClip(clip);
						return clip.getClipPromoId();
					} else {
						int clipID = -1;
						try {
							if (subscriberID == null
									|| subscriberID.equalsIgnoreCase("null")
									|| subscriberID.length() < RBTParametersUtils
											.getParamAsInt("GATHERER",
													"PHONE_NUMBER_LENGTH_MIN",
													10))
								subscriberID = oldfile
										.getName()
										.trim()
										.substring(
												0,
												RBTParametersUtils
														.getParamAsInt(
																"GATHERER",
																"PHONE_NUMBER_LENGTH_MIN",
																10));

							if (subscriberID == null)
								return ugc_Resp_insuf;

							java.io.File newfile = new java.io.File(pathDir
									+ File.separator + strFile);
							Utility.copyFile(oldfile, newfile);
							oldfile.delete();

							logger.info("RBT:: UGC NewFile : "
									+ newfile.getAbsolutePath()
									+ " & UGC NewFile exists :"
									+ newfile.exists());
							if (!newfile.exists() || newfile.length() <= 0)
								return ugc_Resp_Err;

							String clipId = getParameter("COMMON", "UGC_CLIPID");
							if (clipId == null)
								return ugc_Resp_Err;

							logger.info("RBT: Clipid : " + clipId);
							clipID = Integer.parseInt(clipId);
							clipID = clipID + 1;

							String ugcchargeclass = getParameter("SMS",
									"EMOTION_UGC_CLASSTYPE");
							if (ugcchargeclass == null)
								ugcchargeclass = "DEFAULT";

							Clip newClip = new Clip();
							newClip.setClipId(clipID);
							promoID = "7" + clipID;
							newClip.setClipPromoId(promoID);
							newClip.setAlbum(subscriberID);
							newClip.setClipName(clipName);
							newClip.setClassType(ugcchargeclass);
							newClip.setAddToAccessTable('y');
							newClip.setClipGrammar("UGC");
							newClip.setContentType("EMOTION_UGC");

							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy/MM/dd");
							newClip.setClipEndTime(sdf.parse("2004/01/01"));
							newClip.setSmsStartTime(new Date());

							newClip.setClipNameWavFile(clipName + "_name");
							newClip.setClipPreviewWavFile(clipName + "_preview");
							newClip.setClipRbtWavFile(clipName);
							newClip.setClipStartTime(new Date());

							newClip = ClipsDAO.saveClip(newClip);

							if (newClip == null) {
								return Resp_Err;
							}
							return newClip.getClipPromoId();
						} catch (Exception e) {
							logger.error("RBT:: Exception ", e);
						} finally {
							if (rbtCacheManager.getClip(clipID) != null)
								setParameter("COMMON", "UGC_CLIPID",
										String.valueOf(clipID));
						}
					}
				}
			} else if (action != null
					&& action.equalsIgnoreCase(action_ugc_add)) {
				logger.info("RBT: wavfile : " + wavfile);
				if (wavfile != null && wavfile.endsWith(".wav")) {
					boolean isTransExist = false;
					oldfile = new java.io.File(wavfile);
					String strFile = oldfile.getName();
					if (subscriberID == null
							|| subscriberID.equalsIgnoreCase("null")
							|| subscriberID.length() < RBTParametersUtils
									.getParamAsInt("GATHERER",
											"PHONE_NUMBER_LENGTH_MIN", 10))
						subscriberID = oldfile
								.getName()
								.trim()
								.substring(
										0,
										RBTParametersUtils.getParamAsInt(
												"GATHERER",
												"PHONE_NUMBER_LENGTH_MIN", 10));
					logger.info("RBT: file name : " + strFile);
					if (strTransID != null) {
						isTransExist = checkTransDataExits(strTransID,
								"UGC_ADD");
						if (!isTransExist) {
							addTransData(strTransID, subscriberID, "UGC_ADD");
						}
					}
					if (isTransExist) {
						response = "Invalid Request. Already recieved a request with the same TransID : "
								+ strTransID
								+ " and subscriber ID : "
								+ subscriberID + " and wavFile " + strFile;
					} else {
						int clipID = -1;
						try {
							if (subscriberID == null)
								return ugc_Resp_insuf;

							String categoryID = getCategoryId(subscriberID);
							logger.info("RBT: Catid : " + categoryID);
							if (categoryID == null)
								return ugc_Resp_Err;

							String clipId = getParameter("COMMON", "UGC_CLIPID");
							logger.info("RBT: Clipid : " + clipId);
							if (clipId == null)
								return ugc_Resp_Err;
							clipID = Integer.parseInt(clipId);
							clipID = clipID + 1;

							String newWavFileName = "rbt_ugc_" + "7" + clipID
									+ "_rbt" + ".wav";
							java.io.File newfile = new java.io.File(pathDir
									+ File.separator + newWavFileName);
							Utility.copyFile(oldfile, newfile);
							oldfile.delete();

							logger.info("RBT:: UGC NewFile : "
									+ newfile.getAbsolutePath()
									+ " & UGC NewFile exists :"
									+ newfile.exists());
							if (!newfile.exists() || newfile.length() <= 0)
								return ugc_Resp_Err;

							String ugcchargeclass = getParameter("SMS",
									"UGC_CLASSTYPE");
							if (ugcchargeclass == null)
								ugcchargeclass = "DEFAULT";

							int length = strFile.length();
							strFile = strFile.substring(0, length - 4);
							logger.info("RBT: strfile : " + strFile);

							Clip newClip = new Clip();
							newClip.setClipId(clipID);
							promoID = "7" + clipID;
							newClip.setClipPromoId(promoID);
							newClip.setAlbum(subscriberID);
							newClip.setClipName(strFile);
							newClip.setClassType(ugcchargeclass);
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy/MM/dd");
							newClip.setAddToAccessTable('y');
							newClip.setClipGrammar("UGC");
							newClip.setClipEndTime(sdf.parse("2004/01/01"));
							newClip.setSmsStartTime(new Date());
							newClip.setClipNameWavFile("rbt_ugc_" + promoID
									+ "_name");
							newClip.setClipPreviewWavFile("rbt_ugc_" + promoID
									+ "_preview");
							newClip.setClipRbtWavFile("rbt_ugc_" + promoID
									+ "_rbt");
							newClip.setClipStartTime(new Date());
							newClip.setLanguage(categoryID);
							newClip = ClipsDAO.saveClip(newClip);

							if (newClip == null) {
								return Resp_Err;
							}
							try {
								logger.info("RBT: new clip afer ading: "
										+ newClip);
								CategoryClipMap clipMap = new CategoryClipMap();
								logger.info("RBT: new clip map: " + clipMap);
								int catid = Integer.parseInt(categoryID);
								logger.info("RBT:catid: " + catid);
								clipMap.setCategoryId(catid);
								clipMap.setClipId(newClip.getClipId());
								clipMap.setClipInList('y');
								clipMap.setClipIndex(0);
								CategoryClipMapDAO.saveCategoryClipMap(clipMap);
								logger.info("RBT: new clipmmap afer ading clipmap: "
										+ clipMap);
								ugcEventLogger.songCreation(
										newClip.getClipId(),
										newClip.getClipName(),
										newClip.getClipNameWavFile(),
										newClip.getClipPromoId(),
										newClip.getClipStartTime(),
										newClip.getClipEndTime(), new Date(),
										newClip.getAlbum(),
										newClip.getLanguage(),
										newClip.getArtist());
							} catch (Exception e) {
								logger.error(
										"RBT:Exception in saving CategoryClipMap",
										e);
							}
							return newClip.getClipPromoId();
						} catch (Exception e) {
							logger.error("RBT:Exception", e);
						} finally {
							if (rbtCacheManager.getClip(clipID) != null)
								setParameter("COMMON", "UGC_CLIPID",
										String.valueOf(clipID));
						}
					}
				} else
					response = ugc_Resp_Fail;
			} else if (action != null
					&& action.equalsIgnoreCase(action_ugc_expire)) {
				boolean isTransExist = false;
				if (strTransID != null && subscriberID != null) {

					isTransExist = checkTransDataExits(strTransID, "UGC_EXPIRE");
					logger.error("In processugcRequest istrans exist"
							+ isTransExist);
					if (!isTransExist) {
						addTransData(strTransID, subscriberID, "UGC_EXPIRE");
					}
				}
				if (isTransExist)
					response = "Invalid Request. Already recieved a request with the same TransID : "
							+ strTransID
							+ " and subscriber ID : "
							+ subscriberID + " and promoIDList " + promoID;
				else {

					if (promoID != null) {
						response = ugc_Resp_Err;
						ArrayList<String> clipPromoIDList = tokenizeArrayList(
								promoID, null);
						logger.error("In processugcRequest clippromoidlist"
								+ clipPromoIDList.size());
						for (int i = 0; i < clipPromoIDList.size(); i++) {
							String ID = ((String) clipPromoIDList.get(i))
									.trim();
							Clip clip = getClipByPromoId(ID);
							logger.error("In processugcRequest clip" + clip);
							if (clip != null) {
								clip.setClipEndTime(new Date());
								ClipsDAO.updateClip(clip);
								ugcEventLogger.songCreation(clip.getClipId(),
										clip.getClipName(),
										clip.getClipNameWavFile(),
										clip.getClipPromoId(),
										clip.getClipStartTime(),
										clip.getClipEndTime(), new Date(),
										clip.getAlbum(), clip.getLanguage(),
										clip.getArtist());
								logger.error("In processugcRequest clip updated");
								response = ugc_Resp_Success;
							}

						}
					} else if (subscriberID != null) {
						response = ugc_Resp_Err;
						Clip[] clip = getClipByAlbum(subscriberID);
						if (clip != null) {
							for (int a = 0; a < clip.length; a++) {
								clip[a].setClipEndTime(new Date());
								ClipsDAO.updateClip(clip[a]);
								ugcEventLogger.songCreation(
										clip[a].getClipId(),
										clip[a].getClipName(),
										clip[a].getClipNameWavFile(),
										clip[a].getClipPromoId(),
										clip[a].getClipStartTime(),
										clip[a].getClipEndTime(), new Date(),
										clip[a].getAlbum(),
										clip[a].getLanguage(),
										clip[a].getArtist());
							}
							response = ugc_Resp_Success;
						}
					}

				}
			}
		} catch (Exception e) {
			logger.error("In processugcRequest" + e, e);
			response = ugc_Resp_Fail;
		}
		return response;
	}

	private String getCategoryId(String subscriberID) {
		String ugcCatMap = getParameter("GATHERER", "UGC_REGION_CATEGORY_MAP");
		logger.error("In getCategoryId ugc cat map : " + ugcCatMap);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		String circleID = subscriber.getCircleID();

		logger.error("In getCategoryId circle id : " + circleID);

		StringTokenizer stk = new StringTokenizer(ugcCatMap, ";");
		while (stk.hasMoreTokens()) {
			String temp = stk.nextToken();
			logger.error("In getCategoryId temp: "
					+ temp.substring(0, temp.indexOf(",")));
			if (temp.substring(0, temp.indexOf(",")).equalsIgnoreCase(circleID))
				return temp.substring(temp.indexOf(",") + 1);
		}
		return null;
	}

	public boolean updateViralpromotion(String subscriberID, String callerID,
			Date sentTime, String oldType, String newType, Date setTime,
			String selectedBy) {

		DataRequest viraldataRequest = new DataRequest(callerID, oldType);
		viraldataRequest.setSubscriberID(subscriberID);
		viraldataRequest.setSentTime(sentTime);
		viraldataRequest.setNewType(newType);
		rbtClient.updateViralData(viraldataRequest);
		if (viraldataRequest.getResponse().equalsIgnoreCase("success"))
			return true;
		else
			return false;

	}

	public ViralData getViralData(String subscriberID, String callerID,
			Date sentTime, String type) {
		DataRequest viraldataRequest = new DataRequest(callerID, type);
		viraldataRequest.setSubscriberID(subscriberID);
		viraldataRequest.setSentTime(sentTime);
		ViralData[] viraldata = rbtClient.getViralData(viraldataRequest);
		if (viraldata != null && viraldata.length > 0)
			return viraldata[0];
		else
			return null;
	}

	public boolean isRemoteSub(String strSubID) {
		if (strSubID == null)
			return false;
		if (isValidPrefix(strSubID))
			return false;
		String url = getURL(strSubID);
		if (url != null && url.length() > 1)
			return true;
		return false;
	}

	public String getURL(String strSub) {

		if (strSub == null || strSub.length() <= 0)
			return null;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(strSub);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		if (subscriber != null) {
			String circleID = subscriber.getCircleID();
			ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
			applicationDetailsRequest.setCircleID(circleID);
			Site site = rbtClient.getSite(applicationDetailsRequest);
			if (site != null) {

				return site.getSiteURL();
			}
		}
		return null;
	}

	public boolean checkTransDataExits(String transID, String type) {
		logger.info("RBT: CheckTransexist transid: " + transID);
		boolean ret = false;
		DataRequest dataRequest = new DataRequest(null, type);
		dataRequest.setTransID(transID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.TransData[] td = rbtClient
				.getTransDatas(dataRequest);
		logger.info("RBT: CheckTransexist transdata: " + td + " : "
				+ dataRequest.getResponse());
		if (dataRequest.getResponse().equalsIgnoreCase("success") && td != null
				&& td.length > 0) {
			if (td[0] != null)
				ret = true;
		}

		return ret;
	}

	public boolean addTransData(String transID, String subid, String type) {
		boolean ret = false;
		DataRequest dataRequest = new DataRequest(subid, type);
		dataRequest.setTransID(transID);
		com.onmobile.apps.ringbacktones.webservice.client.beans.TransData td = rbtClient
				.addTransData(dataRequest);

		if (dataRequest.getResponse().equalsIgnoreCase("success") && td != null) {
			ret = true;
		}

		return ret;
	}

	public boolean sendSMS(String sender, Subscriber receiver, String msg,
			boolean isMassPush) {
		String subscriptionClassOperatorName = getParamAsString(COMMON, "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
		if(subscriptionClassOperatorName != null){
			String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumberbyType("SMS", receiver.getCircleID(), "SENDER_NO");
			String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility.getBrandName(receiver.getCircleID());
			msg = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(msg, "%NO_SENDER", senderNumber);
			msg = com.onmobile.apps.ringbacktones.provisioning.common.Utility.findNReplaceAll(msg, "%BRAND_NAME", brandName);
		}
		
		UtilsRequest utilsRequest = new UtilsRequest(sender, receiver.getSubscriberID(), msg);
		try {
			rbtClient.sendSMS(utilsRequest);
		} catch (Exception e) {
			logger.error("SendSMS Exception : ", e);
		}
		logger.info("SendSMS UtilReq: " + utilsRequest + ", Response: "
				+ utilsRequest.getResponse());
		if (utilsRequest.getResponse().equalsIgnoreCase("success"))
			return true;
		else
			return false;
	}

	public String processPlayerHelper(Task task) {
		String keyWord = task.getString(param_KEYPRESSED);
		String details = task.getString(rbt_param_DETAILS);
		String response = null;
		String type = CopyProcessorUtils.findSMSTypeFromDTMF(keyWord, details);
		response = processPlayerHelperCopyReq(task, type);
		logger.info("Task: " + task + "Response: " + response);
		return response;
	}

	// public String processPlayerHelper(Task task){
	// logger.info("RBT:: processplayerhelper "+task);
	// String keyWord=task.getString(param_KEYPRESSED);
	// // ArrayList
	// normalCopy=tokenizeArrayList(getParameter("COMMON","NORMALCOPY_KEY"),
	// ",");
	// // ArrayList
	// starCopy=tokenizeArrayList(getParameter("COMMON","STARCOPY_KEY"), ",");
	// // ArrayList
	// rtCopy=tokenizeArrayList(getParameter("COMMON","RTCOPY_KEY"), ",");
	// // ArrayList
	// selfGiftCopy=tokenizeArrayList(getParameter("COMMON","SELFGIFTCOPY_KEY"),
	// ",");
	// // ArrayList
	// giftCopy=tokenizeArrayList(getParameter("COMMON","GIFTCOPY_KEY"), ",");
	// logger.info("RBT:: In ServiceProcessor processPlayerHelper() Normalcopy:"+normalCopy+" starcopy: "+starCopy+" rtcopy : "+rtCopy+" keypressed : "+keyWord);
	// String response=null;
	// String key=null;
	// boolean isStarcopy=false;
	// boolean isCopy=false;
	// if(keyWord!=null&&keyWord.length()>=1){
	// keyWord=keyWord.toLowerCase();
	// if(normalCopy!=null){
	// for(int i=0;i<normalCopy.size();i++){
	// key=(String)normalCopy.get(i).toString().toLowerCase();
	// if(keyWord.indexOf(key)!=-1){
	// response=processPlayerHelperCopyReq(task,"COPY");
	// isStarcopy=true;
	// isCopy=true;
	// break;
	//
	// }else
	// response=Resp_Err;
	// }
	// }
	//
	// if(rtCopy!=null){
	// for(int i=0;i<rtCopy.size();i++){
	// key=(String)rtCopy.get(i).toString().toLowerCase();
	// if(keyWord.indexOf(key)!=-1){
	// response=processPlayerHelperCopyReq(task,"RTCOPY");
	// isStarcopy=true;
	// break;
	//
	// }else{
	// if(isCopy==true&&response.equals(Resp_Success)){
	//
	// }else
	// response=Resp_Err;
	//
	// }
	//
	// }
	// }
	//
	// if(selfGiftCopy!=null){
	// for(int i=0;i<selfGiftCopy.size();i++){
	// key=(String)selfGiftCopy.get(i).toString().toLowerCase();
	// if(keyWord.indexOf(key)!=-1){
	// response=processPlayerHelperCopyReq(task,"SELF_GIFT");
	// isStarcopy=true;
	// break;
	//
	// }
	// }
	// }
	//
	// if(giftCopy!=null){
	// for(int i=0;i<giftCopy.size();i++){
	// key=(String)giftCopy.get(i).toString().toLowerCase();
	// if(keyWord.indexOf(key)!=-1){
	// response=processPlayerHelperCopyReq(task,"GIFTCOPY");
	// isStarcopy=true;
	// break;
	//
	// }
	// }
	// }
	//
	//
	// if(isStarcopy==false&&starCopy!=null){
	// logger.info("RBT: copystar");
	// for(int i=0;i<starCopy.size();i++){
	// key=(String)starCopy.get(i).toString().toLowerCase();
	// if(keyWord.indexOf(key)!=-1){
	// response=processPlayerHelperCopyReq(task,"COPYSTAR");
	// break;
	// }else
	// response=Resp_Err;
	// }
	// }
	// }else{
	// logger.info("RBT:: processplayerhelper else part : ");
	// if
	// (task.getString(rbt_param_DETAILS)!=null&&task.getString(rbt_param_DETAILS).indexOf(":RT")!=(-1)){
	// response=processPlayerHelperCopyReq(task,"RTCOPY");
	// }else
	// response=processPlayerHelperCopyReq(task,"COPY");
	// }
	//
	// logger.info("RBT:: processplayerhelper resp : "+response);
	// return response;
	// }
	public String processPlayerHelperCopyReq(Task task, String copyType) {
		logger.info("RBT:: in processPlayerHelperCopyReq " + task + " : "
				+ copyType);
		String details = task.getString(rbt_param_DETAILS);
		String copy = copyType;
		boolean isShuffle = false;
		String response = null;
		String strClip = null;
		try {
			String[] detailsTokens = details.trim().split(":");
			String strSubscriberID = detailsTokens[0];
			String strCallerID = detailsTokens[1];
			if (copy != null
					&& ((copy.equalsIgnoreCase("COPY")) || (copy
							.equalsIgnoreCase("COPYSTAR"))))
				copy = getSmsType(strCallerID,
						task.getString(param_KEYPRESSED), task, null);
			if (copy == null)
				return Resp_Err;
			
			String strSubscriberWavFile = detailsTokens[2];
			int iCategoryID = 26;
			String strVrbt = null;
			if (detailsTokens.length >= 4)
				iCategoryID = Integer.parseInt(detailsTokens[3].trim());
			if (iCategoryID == -1)
				iCategoryID = 26;
			String strSelectionStatus = "1";
			if (detailsTokens.length >= 5)
				strSelectionStatus = detailsTokens[4].trim();
			if (strSelectionStatus.equals("-1"))
				strSelectionStatus = "1";
			boolean containsS = false;
			if (detailsTokens.length >= 6
					&& detailsTokens[5].equalsIgnoreCase("S"))
				containsS = true;
			if (detailsTokens.length >= 7
					&& detailsTokens[6].equalsIgnoreCase("VRBT")) {
				if(m_vrbtCatIdSubSongSrvKeyMap != null && m_vrbtCatIdSubSongSrvKeyMap.containsKey(iCategoryID+"")) {
					strVrbt = "VRBT";					
				}else {
					iCategoryID = vrbtRECategoryId;
				}
			}
			HashMap<String, String> hashMap = null;
			boolean isThirdPartyAllowedKeys = false;
			if (null != allowedThirdPartyKeysList
					&& !allowedThirdPartyKeysList.isEmpty()) {
				String keyPressed = task.getString(param_KEYPRESSED);
				if (keyPressed != null) {
					for (String key : allowedThirdPartyKeysList) {
						if (keyPressed.indexOf(key) != -1) {
							isThirdPartyAllowedKeys = true;
							hashMap = new HashMap<String, String>();
							hashMap.put(IS_THIRD_PARTY_ALLOWEDED_KEY,
									String.valueOf(isThirdPartyAllowedKeys));
							break;
						}
					}
				}
			}
			
			
			//check virtual number airtel consent senthil
			boolean copyConsent = getParamAsString("COMMON", "COPY_CONSENT_FOR_VIRTUAL_NUMBERS", "FALSE").equalsIgnoreCase("TRUE");
			if(copyConsent && copyVirtualNumbers != null) {
				if(copyVirtualNumbers.contains(strSubscriberID) && (copy.equalsIgnoreCase(COPY) || copy.equalsIgnoreCase(COPYSTAR) ||copy.equalsIgnoreCase(RRBT_COPY))) {
					Subscriber subscriber = getSubscriber(strCallerID);
					if (subscriber == null
							|| subscriber.getStatus() == null
							|| !subscriber.getStatus().equalsIgnoreCase(
									"ACTIVE")) {
						copy = copy + "_CONSENT";
				}
			  }
			}
			DataRequest viraldataRequest = new DataRequest(strCallerID, copy);
			logger.info("RBT:: In processPlayerHelperCopyReq ");

			if (copy.equalsIgnoreCase("CONFIRM_ACT")) {
				strClip = strSubscriberWavFile + ":" + iCategoryID + ":"
						+ strSelectionStatus;

				String keyPressed = task.getString(param_KEYPRESSED);
				if (null == hashMap) {
					hashMap = new HashMap<String, String>();
				}
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(KEYPRESSED_ATTR, keyPressed);
				viraldataRequest.setCallerID(strCallerID);
				viraldataRequest.setClipID(strClip);
				viraldataRequest.setSubscriberID(strSubscriberID);
				viraldataRequest.setCount(0);
				viraldataRequest.setInfoMap(hashMap);
				rbtClient.addViralData(viraldataRequest);
			} else if (copy.equalsIgnoreCase("GIFTCOPY")) {
				if (containsS)
					strClip = strSubscriberWavFile + ":" + iCategoryID + ":"
							+ strSelectionStatus + ":S";
				else
					strClip = strSubscriberWavFile + ":" + iCategoryID + ":"
							+ strSelectionStatus;
				String keyPressed = task.getString(param_KEYPRESSED);
				if (null == hashMap) {
					hashMap = new HashMap<String, String>();
				}
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(KEYPRESSED_ATTR, keyPressed);
				hashMap.put(GIFTTYPE_ATTR, "direct");
				viraldataRequest.setCallerID(null);
				viraldataRequest.setClipID(strClip);
				viraldataRequest.setSubscriberID(strCallerID);
				viraldataRequest.setCount(0);
				viraldataRequest.setInfoMap(hashMap);
				rbtClient.addViralData(viraldataRequest);
			} else if (copy.equalsIgnoreCase("SELF_GIFT")) {

				String keyPressed = task.getString(param_KEYPRESSED);
				if (null == hashMap) {
					hashMap = new HashMap<String, String>();
				}
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(KEYPRESSED_ATTR, keyPressed);
				hashMap.put(GIFTTYPE_ATTR, "direct");
				viraldataRequest.setCallerID(strSubscriberID);
				viraldataRequest.setSubscriberID(strCallerID);
				viraldataRequest.setCount(0);
				viraldataRequest.setInfoMap(hashMap);
				rbtClient.addViralData(viraldataRequest);
			} else if (copy.equalsIgnoreCase(RRBT_COPY) || copy.equalsIgnoreCase(RRBT_COPY + "_CONSENT")) {
				if (containsS)
					strClip = strSubscriberWavFile + ":" + iCategoryID + ":"
							+ strSelectionStatus + ":S";
				else
					strClip = strSubscriberWavFile + ":" + iCategoryID + ":"
							+ strSelectionStatus;
				if(strVrbt != null) {
					strClip += ":" + strVrbt;
				}
				String keyPressed = task.getString(param_KEYPRESSED);
				if (null == hashMap) {
					hashMap = new HashMap<String, String>();
				}
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(KEYPRESSED_ATTR, keyPressed);
				viraldataRequest.setCallerID(strCallerID);
				viraldataRequest.setClipID(strClip);
				viraldataRequest.setSubscriberID(strSubscriberID);
				viraldataRequest.setCount(0);
				viraldataRequest.setInfoMap(hashMap);
				rbtClient.addViralData(viraldataRequest);
			} else if (copy.equalsIgnoreCase(HASH_DOWNLOAD)) {
				String keyPressed = task.getString(param_KEYPRESSED);
				String info = strSubscriberWavFile + ":" + iCategoryID + ":"
						+ strSelectionStatus;
				HashMap<String, String> infoMap = new HashMap<String, String>();
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					infoMap.put(KEYPRESSED_ATTR, keyPressed.toLowerCase());
				if (info != null && info.length() > 0
						&& !info.equalsIgnoreCase("null")) {
					infoMap.put("INFO", info);
				}
				if (isThirdPartyAllowedKeys)
					hashMap.put(IS_THIRD_PARTY_ALLOWEDED_KEY,
							String.valueOf(isThirdPartyAllowedKeys));
				Clip clip = getClipByWavFile(strSubscriberWavFile);
				if (clip != null) {
					viraldataRequest.setCallerID(strCallerID);
					viraldataRequest
							.setClipID(String.valueOf(clip.getClipId()));
					viraldataRequest.setSubscriberID(strSubscriberID);
					viraldataRequest.setCount(0);
					viraldataRequest.setInfoMap(infoMap);
					rbtClient.addRbtSupport(viraldataRequest);
				} else {
					logger.info("Clip doesn't exist subscriberWavFile: "
							+ strSubscriberWavFile);
				}
			} else if (copy.equalsIgnoreCase(PROMOTE)) {

				String keyPressed = task.getString(param_KEYPRESSED);
				String info = strSubscriberWavFile + ":" + iCategoryID + ":"
						+ strSelectionStatus;
				HashMap<String, String> infoMap = new HashMap<String, String>();
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					infoMap.put(KEYPRESSED_ATTR, keyPressed.toLowerCase());
				if (info != null && info.length() > 0
						&& !info.equalsIgnoreCase("null")) {
					infoMap.put("INFO", info);
				}

				Clip clip = getClipByWavFile(strSubscriberWavFile);
				if (clip != null) {
					viraldataRequest.setCallerID(strCallerID);
					viraldataRequest
							.setClipID(String.valueOf(clip.getClipId()));
					viraldataRequest.setSubscriberID(strSubscriberID);
					viraldataRequest.setCount(0);
					viraldataRequest.setInfoMap(infoMap);
					addPromoteViralData(viraldataRequest);
				} else {
					logger.info("Clip doesn't exist subscriberWavFile: "
							+ strSubscriberWavFile);
				}

			} else {
				// Like request reaches here
				String keyPressed = task.getString(param_KEYPRESSED);
				if (null == hashMap) {
					hashMap = new HashMap<String, String>();
				}
				if (keyPressed != null && keyPressed.length() > 0
						&& !keyPressed.equalsIgnoreCase("null"))
					hashMap.put(KEYPRESSED_ATTR, keyPressed);
				viraldataRequest.setInfoMap(hashMap);
				if ((copy.equalsIgnoreCase("COPY") || copy
						.equalsIgnoreCase("COPYSTAR") || copy
						.equalsIgnoreCase("COPY_CONSENT") || copy
						.equalsIgnoreCase("COPYSTAR_CONSENT")) && containsS) {
					isShuffle = true;
				}
				if (iCategoryID == -1) {
					logger.info("iCategoryID is -1 and isDefaultCopyNeeded: "
							+ isDefaultCopyNeeded());
					if (isDefaultCopyNeeded()) {
						viraldataRequest.setSubscriberID(strSubscriberID);
						viraldataRequest.setCount(0);
						rbtClient.addViralData(viraldataRequest);
						if (!copy.equalsIgnoreCase("LIKE"))//RBT-14671 - # like
							addLikeViralData(viraldataRequest, keyPressed);
					}
				} else {
					if (isShuffle) {
						strClip = strSubscriberWavFile + ":" + "S"
								+ iCategoryID + ":" + strSelectionStatus;
					} else
						strClip = strSubscriberWavFile + ":" + iCategoryID
								+ ":" + strSelectionStatus;

					if(strVrbt != null) {
						strClip += ":" + strVrbt;
					}
					viraldataRequest.setSubscriberID(strSubscriberID);
					viraldataRequest.setCount(0);
					viraldataRequest.setClipID(strClip);
					rbtClient.addViralData(viraldataRequest);
					if (!copy.equalsIgnoreCase("LIKE"))
						addLikeViralData(viraldataRequest, keyPressed);
					logger.info("Successfully added viraldata. "
							+ ", viraldataRequest: " + viraldataRequest);
				}
				
			}

			if (viraldataRequest.getResponse() != null
					&& viraldataRequest.getResponse().equalsIgnoreCase(
							"success"))
				response = Resp_Success;
			else
				response = Resp_Err;
			logger.error("RBT:: processRbtPlayerHelperReq copy " + response
					+ " : " + details);

		} catch (Exception e) {
			logger.error("RBT:: processRbtPlayerHelperReq copy" + e);
			response = Resp_Err;
		}
		return response;
	}
	//RBT-14671 - # like
	/**
	 * @param viraldataRequest
	 * @param keyPressed
	 */
	protected void addLikeViralData(DataRequest viraldataRequest,
			String keyPressed) {
		logger.info("Inside addLikeViralData. " + ", viraldataRequest: "
				+ viraldataRequest);
		String type = CopyProcessorUtils.findLikeTypeFromDTMF(keyPressed);
		logger.info("Inside addLikeViralData. " + ", type: " + type
				+ ", keyPressed: " + keyPressed);
		if (type != null && type.equalsIgnoreCase(LIKE)) {
			viraldataRequest.setType(type);
			rbtClient.addViralData(viraldataRequest);
			logger.info("Successfully added viraldata. "
					+ ", viraldataRequest: " + viraldataRequest);
		}
	}
	
	protected void addPromoteViralData(DataRequest viraldataRequest) {
		logger.info("Inside addPromoteViralData. " + ", viraldataRequest: "
				+ viraldataRequest);

		logger.info("Inside addLikeViralData. " + ", type: PRMOTE");
		viraldataRequest.setType(PROMOTE);
		rbtClient.addViralData(viraldataRequest);
		logger.info("Successfully added viraldata. " + ", viraldataRequest: "
				+ viraldataRequest);

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

	public static boolean isUserActive(String status) {
		if (status.equalsIgnoreCase(ACT_PENDING)
				|| status.equalsIgnoreCase(ACTIVE)
				|| status.equalsIgnoreCase(LOCKED)
				|| status.equalsIgnoreCase(RENEWAL_PENDING)
				|| status.equalsIgnoreCase(GRACE)
				|| status.equalsIgnoreCase(SUSPENDED)) {
			return true;
		}
		return false;
	}

	public static boolean isUserDeactive(String status) {
		if (status.equalsIgnoreCase(ACT_ERROR)
				|| status.equalsIgnoreCase(DEACTIVE)
				|| status.equalsIgnoreCase(DEACT_PENDING)
				|| status.equalsIgnoreCase(DEACT_ERROR)) {
			return true;
		}
		return false;
	}

	public String processOBDReq(Task task) {
		String response = FAILURE;
		Subscriber sub = (Subscriber) task.getObject(param_subscriber);
		String promocode = null;
		String clipId = null;
		String callerID = null;
		String catID = task.getString(param_OBDCATID);
		String mode = task.getString(param_OBDMODE);
		String reqType = "SELECTION";
		promocode = getPromoCode(task);
		logger.error("Task: " + task + " promocode: " + promocode);
		if (promocode == null) {
			response = Resp_PromoCodeNotAvailable;
			logger.error("RBT:: Response is Invalid promocode ");
			sendErrorMsg(sub, response, mode, reqType, null, task);
			return response;
		}
		callerID = task.getString(param_callerid);
		getCategoryAndClipForPromoID(task, promocode);
		String responseWS = "FAILURE";
		String songName = "";
		if (promocode.equalsIgnoreCase("CRICKET")) {
			Processor smsProcessor = AdminFacade.getProcessorObject(api_Sms);
			if (mode != null)
				task.setObject(param_actby, mode);
			ArrayList<String> list = tokenizeArrayList(
					task.getString(param_OBDSMSTEXT), " ");
			task.setObject(param_smsText, list);
			smsProcessor.getFeature(task);
			task.setObject(param_catid, "3");
			Parameters obdallow = CacheManagerUtil.getParametersCacheManager()
					.getParameter("OBD", "ALLOW_OBD_ACT", "false");
			if (obdallow == null
					|| obdallow.getValue().equalsIgnoreCase("false")) {
				String status = sub.getStatus();
				if (!isUserActive(status)) {
					sendErrorMsg(sub, status, mode, reqType, null, task);
					return response;
				}
			}
			if (!isUserActive(sub.getStatus())) {
				Parameters modeParams = CacheManagerUtil
						.getParametersCacheManager().getParameter("OBD",
								"DEFAULT_MODE");
				if (modeParams != null && modeParams.getValue() != null) {
					task.setObject(param_actby, task.getString(param_OBDMODE)
							+ modeParams.getValue());
				}
			}
			if (!isUserActive(sub.getStatus())) {
				Parameters modeParams = CacheManagerUtil
						.getParametersCacheManager().getParameter("OBD",
								"DEFAULT_MODE");
				if (modeParams != null && modeParams.getValue() != null) {
					task.setObject(param_actby, task.getString(param_OBDMODE)
							+ modeParams.getValue());
				}
			}
			smsProcessor.processCricket(task);
			reqType = "CRICKET";
			songName = "Cricket";
			if (task.getString(param_responseObdMark) != null
					&& task.getString(param_responseObdMark).equalsIgnoreCase(
							"SUCCESS")) {
				response = "SUCCESS";
				responseWS = "SUCCESS";
			}

		} else {
			if (task.getObject(CAT_OBJ) != null) {
				catID = task.getString(param_catid);
				clipId = task.getString(param_clipid);
			} else {
				clipId = task.getString(param_clipid);
			}
			logger.error("RBT:: processOBDReq cat_ID= " + catID + " clipId== "
					+ clipId);

			if (catID == null)
				catID = "3";

			if (clipId == null || catID.equalsIgnoreCase("null")
					|| catID.length() == 0) {
				response = Resp_PromoCodeNotAvailable;
				logger.error("RBT:: Response is Invalid promocode ");
				sendErrorMsg(sub, response, mode, reqType, null, task);
				return response;
			}

			// only selection can happen through OBD
			Parameters obdallow = CacheManagerUtil.getParametersCacheManager()
					.getParameter("OBD", "ALLOW_OBD_ACT", "false");
			if (obdallow == null
					|| obdallow.getValue().equalsIgnoreCase("false")) {
				logger.info("checking for inactive users");
				String status = sub.getStatus();
				if (!isUserActive(status)) {
					sendErrorMsg(sub, status, mode, reqType, null, task);
					return response;
				}
			}
			songName = "";
			Clip clip = null;
			SelectionRequest selectionRequest = new SelectionRequest(
					task.getString(param_OBDSUBID));
			selectionRequest.setMode(task.getString(param_OBDMODE));
			selectionRequest.setCategoryID(catID);
			selectionRequest.setClipID(clipId);
			selectionRequest.setCallerID(callerID);

			if (!isUserActive(sub.getStatus())) {
				Parameters modeParams = CacheManagerUtil
						.getParametersCacheManager().getParameter("OBD",
								"DEFAULT_MODE");
				if (modeParams != null && modeParams.getValue() != null) {
					selectionRequest.setMode(task.getString(param_OBDMODE)
							+ modeParams.getValue());
				}

				Parameters subClassParams = CacheManagerUtil
						.getParametersCacheManager().getParameter("OBD",
								"DEFAULT_SUBSCRIPTION_CLASS");
				if (subClassParams != null && subClassParams.getValue() != null)
					selectionRequest.setSubscriptionClass(subClassParams
							.getValue());
			}
			rbtClient.addSubscriberSelection(selectionRequest);
			if (selectionRequest.getResponse().equalsIgnoreCase("SUCCESS"))
				response = SUCCESS;
			else if (selectionRequest.getResponse().equalsIgnoreCase(
					ALREADY_EXISTS)) {
				response = Resp_songSetSelectionAlreadyExists;
				if (task.getObject("CLIP_OBJ") != null)
					clip = (Clip) task.getObject("CLIP_OBJ");
				if (clip != null)
					songName = clip.getClipName();

			} else {
				response = FAILURE;
				if (task.getObject("CLIP_OBJ") != null)
					clip = (Clip) task.getObject("CLIP_OBJ");
				if (clip != null)
					songName = clip.getClipName();
			}

			logger.error("RBT:: processOBDReq response " + response);
			responseWS = selectionRequest.getResponse();
		}
		if (!SUCCESS.equalsIgnoreCase(responseWS)) {
			sendErrorMsg(sub, responseWS, mode, reqType, songName, task);
		}

		return response;
	}

	/**
	 * send msg to subscriber in case of any error while activation/selection
	 * 
	 * @param subscriber
	 * @param response
	 * @param mode
	 * @param reqType
	 * @author laxmankumar
	 */
	private void sendErrorMsg(Subscriber subscriber, String response,
			String mode, String reqType, String songName, Task task) {

		String lang = null;
		if (null != subscriber) {
			lang = subscriber.getLanguage();
		}
		if (null == response) {
			response = FAILURE;
		}
		if (null == mode || "".equals(mode.trim())) {
			logger.warn("Couldn't find mode...");
			return;
		}
		String type = (mode + "_" + reqType).toUpperCase();

		logger.info("Get the sms text for mode:" + mode + " reqType:" + reqType
				+ " type:" + type + " resp:" + response.toUpperCase()
				+ " lang:" + lang);

		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
				type, response.toUpperCase(), lang);
		if (null == smsText || "".equals(smsText.trim())) {
			logger.info("Checking for failure case");
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					type, "FAILURE", lang);
			if (null == smsText || "".equals(smsText.trim())) {
				return;
			}

		}

		if (songName != null) {
			smsText = smsText.replaceAll("%SONG_NAME", songName);
		}

		Parameters sender = null;
		String senderId = null;
		if (task.containsKey(SENDER_ID)) {
			senderId = task.getString(SENDER_ID).trim();
			logger.info("Sender ID is configured from third party end SENDER_ID : "
					+ senderId);
		} else {
			sender = CacheManagerUtil.getParametersCacheManager().getParameter(
					"SMS", "SENDER_NUMBER");
			senderId = sender.getValue();
		}

		logger.info("Got sms text as '" + smsText + "'" + " Sender:" + senderId
				+ " Receiver:" + subscriber.getSubscriberID());

		RBTClient rbtClient = RBTClient.getInstance();
		UtilsRequest utilsRequest = new UtilsRequest(senderId,
				subscriber.getSubscriberID(), smsText);
		rbtClient.sendSMS(utilsRequest);
		if (utilsRequest.getResponse().equalsIgnoreCase("success")) {
			logger.info("Successfully sent SMS");
		} else {
			logger.info("Failed to send SMS");
		}
	}

	private boolean getCallerID(String token, Task task) {
		try {
			if (token.startsWith("+"))
				token = token.substring(1);
			token = subID(token);
			if (token.length() >= param(GATHERER, PHONE_NUMBER_LENGTH_MIN, 10)
					&& token.length() <= param(GATHERER,
							PHONE_NUMBER_LENGTH_MAX, 10)) {
				Clip clip = getClipByPromoId(token);
				if (clip == null) {
					Long.parseLong(token);
					task.setObject(param_callerid, token);
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public String getPromoCode(Task task) {
		logger.error("RBT:: getPromoCode smsText :  "
				+ task.getString(param_OBDSMSTEXT));
		ArrayList<String> list = tokenizeArrayList(
				task.getString(param_OBDSMSTEXT), " ");
		boolean isCricket = false;
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (getCallerID(list.get(i), task)) {
					list.remove(i);
					i--;
				} else {
					String feature = isFeature(list.get(i));
					if (feature == null)
						continue;
					if (feature.equalsIgnoreCase("CRICKET"))
						isCricket = true;
					list.remove(i);
					i--;
				}
			}
			if (isCricket)
				return "CRICKET";
			if (list.size() == 1)
				return list.get(0);
		}
		return null;
	}

	public String isFeature(String key) {
		ArrayList<String> listfeature = tokenizeArrayList(
				getParameter("SMS", RBT_KEYWORD), null);
		if (listfeature != null) {
			for (int j = 0; j < listfeature.size(); j++) {
				if (key.equalsIgnoreCase(listfeature.get(j)))
					return "RBT";
			}
		}
		listfeature = tokenizeArrayList(getParameter("SMS", CRICKET_KEYWORD),
				null);
		if (listfeature != null) {
			for (int j = 0; j < listfeature.size(); j++) {
				if (key.equalsIgnoreCase(listfeature.get(j)))
					return "CRICKET";
			}
		}
		return null;
	}

	public void processUSSDSubscriptionRequest(Task task) {
		Processor processor = AdminFacade.getProcessorObject(api_Promotion);

		String subID = task.getString(param_SUBID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subID);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		task.setObject(param_subscriber, subscriber);

		boolean checkSubStatus = true;
		if (task.containsKey("CHECK_STATUS")
				&& task.getString("CHECK_STATUS").equalsIgnoreCase("FALSE"))
			checkSubStatus = false;

		if (checkSubStatus
				&& (subscriber.getStatus().equals(NEW_USER) || subscriber
						.getStatus().equals(DEACTIVE))) {
			logger.info("Subscriber: " + subID + " is NEW_USER");
			subscriber = processor.processActivation(task);
			String rbtTextResponse = null;
			if (task.getString(param_response) != null) {
				if (task.getString(param_response).equalsIgnoreCase("success")) {
					Parameters ussdParams = CacheManagerUtil
							.getParametersCacheManager().getParameter("USSD",
									"USSD_MENU_LIST_FOR_NEW_USER", null);
					rbtTextResponse = ussdParams.getValue();

					rbtTextResponse = getUssdMenuWithClipInfo(task,
							rbtTextResponse);

					logger.info("Subscriber: " + subID
							+ " USSD menu list for new user: "
							+ rbtTextResponse);
				} else
					rbtTextResponse = CacheManagerUtil.getRbtTextCacheManager()
							.getRBTText("USSD", "USSD_ACT_FAILURE").getText();

				task.setObject(param_response, rbtTextResponse);
			}
			logger.info("Response is : " + task.getString(param_response));
		} else if (subscriber.getStatus().equals(DEACT_PENDING)) {
			logger.info("Response is : " + DEACT_PENDING);
			task.setObject(param_response, DEACT_PENDING);
		} else if (subscriber.getStatus().equals(SUSPENDED)) {
			logger.info("Response is : " + SUSPENDED);
			task.setObject(param_response, SUSPENDED);
		} else if (task.containsKey(param_USSD_ACTION)
				&& task.getString(param_USSD_ACTION) != null
				&& task.getString(param_USSD_ACTION).equalsIgnoreCase(
						action_selection)) {
			processor.processSelection(task);
			String rbtTextResponse = null;
			if (task.getString(param_response) != null) {
				rbtTextResponse = CacheManagerUtil
						.getRbtTextCacheManager()
						.getRBTText(
								"USSD",
								"USSD_SEL_"
										+ task.getString(param_response)
												.toUpperCase()).getText();
				task.setObject(param_response, rbtTextResponse);
			}
			logger.info("Response is : " + task.getString(param_response));
		} else {
			Parameters ussdParams = CacheManagerUtil
					.getParametersCacheManager().getParameter("USSD",
							"USSD_MENU_LIST", null);
			String responseText = ussdParams.getValue();

			responseText = getUssdMenuWithClipInfo(task, responseText);

			logger.info("Response : " + responseText);
			task.setObject(param_response, responseText);
		}
	}

	private String getUssdMenuWithClipInfo(Task task, String ussdMenu) {
		String subID = task.getString(param_SUBID);

		String activeClipIds = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "ACTIVATED_CLIP_IDS", null).getValue();
		String[] clipIds = activeClipIds.split(",");

		int i = 1;
		String browsingLanguage = task.getString(param_BROWSING_LANGUAGE);
		for (String clipId : clipIds) {
			Clip clip = rbtCacheManager.getClip(clipId, browsingLanguage);
			String songName;
			if (clip != null) {
				songName = clip.getClipName();
				ussdMenu = ussdMenu.replaceAll("%SONG" + i, songName);
				ussdMenu = ussdMenu.replaceAll("<%CLIPID" + i + ">", clipId);
				ussdMenu = ussdMenu.replaceAll("<msisdn>", subID);
				i++;
			}
		}
		return ussdMenu;
	}

	public void processChangeMsisdnRequest(Task task) {
		String subscriberID = task.getString(param_SUBID);
		String newSubscriberID = task.getString(param_NEWSUBID);
		UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
				subscriberID);
		updateDetailsRequest.setNewSubscriberID(newSubscriberID);

		Subscriber sub = rbtClient.changeMsisdn(updateDetailsRequest);
		task.setObject(param_subscriber, sub);
		task.setObject(param_response, updateDetailsRequest.getResponse());

		logger.info("Old subscriber id: " + subscriberID + "New subscriber id:"
				+ newSubscriberID + " Response : "
				+ updateDetailsRequest.getResponse());
	}

	public String processConsentCallback(Task task)
	{
		String response = response_VODACT_INTERNAL_ERROR;
		long timeTaken = -1;
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		try
		{
			logger.info("Processing consent callback for "+task);
			String msisdn = task.getString(param_VODACT_MSISDN);
			String mode = task.getString(param_VODACT_MODE);
			String srvClass = task.getString(param_VODACT_SRVCLASS);
			String srvId = task.getString(param_VODACT_SRVID);
			String transID = task.getString(param_VODACT_TRANSID);
			
			if(msisdn == null || srvClass == null || srvId == null)
			{
				logger.info("Parameters missing, not processing further...");
				response = response_VODACT_PARAMETER_MISSING;
				return response;
			}
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
			Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
			msisdn = subscriber.getSubscriberID();
			DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
			try
			{
				List<String> upgradeModesLst = com.onmobile.apps.ringbacktones.services.common.Utility.vfUpgradeNonCheckModesList;
				if(consentModesNoTransId.contains(mode))
				{
					//Added for TTG-14813
					List<String> modeMappingList = new ArrayList<String>();
					modeMappingList.add(mode);
					mode = com.onmobile.apps.ringbacktones.webservice.common.Utility
							.listToStringWithQuots(modeMappingList);
					//End of TTG-14814
					
					List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = RBTDBManager.getInstance().getConsentRecordListForStatusNMsisdnMode("1", msisdn,mode,false);
					filterConsentBeansBasedOnServiceClassAndServiceId(doubleConfirmationRequestBeans, subscriber, srvId, srvClass);
					if(doubleConfirmationRequestBeans.size() == 0)
					{
						response =  response_VODACT_NO_SUBSCRIPTIONS_FOUND;
						return response;
					}
					doubleConfirmationRequestBean = doubleConfirmationRequestBeans.get(doubleConfirmationRequestBeans.size() - 1);
					deleteOlderDuplicateConsentRecords(doubleConfirmationRequestBeans, subscriber);					
				}
				else if ((com.onmobile.apps.ringbacktones.services.common.Utility.tpcgModesList.contains(mode) || (!upgradeModesLst.isEmpty() && !upgradeModesLst
						.contains(mode))) && transID != null)
				{
					logger.info("consent processing based on trans id case");
					doubleConfirmationRequestBean = RBTDBManager.getInstance().getConsentRecordForStatusNMsisdnNTransId("1", msisdn, transID);
					if(doubleConfirmationRequestBean == null)
					{
						response = response_VODACT_NO_SUBSCRIPTIONS_FOUND;
						return response;
					}
					
					if(doubleConfirmationRequestBean != null && (doubleConfirmationRequestBean.getRequestType().equalsIgnoreCase("ACT") || doubleConfirmationRequestBean.getRequestType().equalsIgnoreCase("UPGRADE"))) {
						deleteOldConsent(subscriber.getSubscriberID(), doubleConfirmationRequestBean.getTransId());			
					}
				}
				if(doubleConfirmationRequestBean == null || doubleConfirmationRequestBean.getRequestType() == null)
				{
					logger.info("No pending record found...possible config issue");
					response = response_VODACT_NO_SUBSCRIPTIONS_FOUND;
					return response;
				}
				logger.info("been identified for callback "+doubleConfirmationRequestBean);
				
				
				response = processConsentCallbackForBean(doubleConfirmationRequestBean, transID, srvId, srvClass);
				task.setObject(param_VODACT_TRANSID, doubleConfirmationRequestBean.getTransId());
				endTime = System.currentTimeMillis();
				timeTaken = endTime = startTime;
			}
			catch(Exception e)
			{
				logger.error("Exception", e);
			}
		}
		catch(Exception e)
		{
			logger.error("Exception", e);
		}
		finally
		{
			ConsentCallbackHitLogger.log(response, timeTaken, task);
		}
		return response;
	}
	
	private String processConsentCallbackForBean(DoubleConfirmationRequestBean reqBean,String transID, String srvId, String srvClass)
	{
		logger.info("processing consent callback for bean "+reqBean);
		String xtraInfo = reqBean.getExtraInfo();
		String extraInfo = null;
		Map<String, String> xtraInfoMap = null;
		String reqTransID = transID;

		String comboTransID = null;
		if (xtraInfo != null && !xtraInfo.equalsIgnoreCase("null"))
			xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
		if (xtraInfoMap == null)
			xtraInfoMap = new HashMap<String, String>();
		xtraInfoMap.put("TPCGID", reqBean.getTransId());
		if(xtraInfoMap.containsKey("TRANS_ID")){
			comboTransID = xtraInfoMap.get("TRANS_ID");
		}
		extraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
		String mode = reqBean.getMode();
		if (externalToInternalModeMapping == null ||externalToInternalModeMapping.size() == 0 ) {
			String internalToExternalModeMappingStr = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "INTERNAL_EXTERNAL_MODES_MAP", null);
			externalToInternalModeMapping = MapUtils.convertToMap(internalToExternalModeMappingStr,
					";", "=", ",");
			logger.info("externalToInternalModeMapping=" + externalToInternalModeMapping);
		}
		String toBeUpdatedWithMode = externalToInternalModeMapping!=null ? externalToInternalModeMapping.get(mode) : null;
		
		try
		{
			String selectionInfo = reqBean.getSelectionInfo();
			if(selectionInfo != null) {
				selectionInfo += ":trxid:" + reqTransID + ":";
			} else {
				selectionInfo = "trxid:" + reqTransID;
			}
			logger.info("selectionInfo after reqtransID:" + selectionInfo);
			reqBean.setSelectionInfo(selectionInfo);
			boolean update = false;
            update = RBTDBManager.getInstance().updateConsentStatusAndModeOfConsentRecord(selectionInfo, reqBean.getSubscriberID(), reqBean.getTransId(), "2", null, toBeUpdatedWithMode, extraInfo, null, null);
			//			boolean update = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(reqBean.getSubscriberID(), reqBean.getTransId(), "2");
			if (comboTransID != null && toBeUpdatedWithMode != null) {
				String trxid = (reqTransID != null) ? ":trxid:" + reqTransID + ":" : null;
				RBTDBManager.getInstance().updateModeOfConsentRecord(trxid, reqBean.getSubscriberID(), comboTransID, toBeUpdatedWithMode,null);
            }
			//RBT-14181 - Vodafone In:-Feature mentioned in RBT-13537 not working as per requirement.
			String wavFileName = reqBean.getWavFileName();
			Clip clip = null;
			if (comboTransID != null) {
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
						.getInstance()
						.getDoubleConfirmationRequestBeanForStatus(null,
								comboTransID, reqBean.getSubscriberID(), null,
								true);
				DoubleConfirmationRequestBean dbConfirmReqBean = null;
				String classType = null;		
				if (doubleConfirmReqBeans != null
						&& doubleConfirmReqBeans.size() > 0) {
					dbConfirmReqBean = doubleConfirmReqBeans.get(0);
				}
				if (dbConfirmReqBean != null) {
					classType = dbConfirmReqBean.getClassType();
					reqBean.setClassType(classType);
					wavFileName = dbConfirmReqBean.getWavFileName();					
				}
			}			
			if(wavFileName != null){
				clip =rbtCacheManager.getClipByRbtWavFileName(wavFileName);
				if(clip!=null){
					reqBean.setClipID(clip.getClipId());
				}
			}
			//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
			writeCDRLog(reqBean,comboTransID,srvClass,srvId);
			
			if (update)
			{
				/*if(extraInfo != null)
					RBTDBManager.getInstance().updateConsentExtrInfo(reqBean.getSubscriberID(), reqBean.getTransId(), extraInfo);*/
				return response_VODACT_SUCCESS_WITH_ID;
			}
		}
		catch(Exception e)
		{
			//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
			writeCDRLog(reqBean,comboTransID,srvClass,srvId);
			logger.error("Exception", e);
		}
		return response_VODACT_INTERNAL_ERROR;
	}
	
	
	public static int[] getTime(int itime) {

		String strTime = itime + "";
		int[] times = new int[2];
		if (strTime.length() <= 2) {
			times[0] = 0;
			times[1] = Integer.parseInt(strTime);
			return times;
		}

		String time = strTime.substring(0, strTime.length() - 2);
		String mins = strTime.substring(strTime.length() - 2);

		if (time != null && time.trim().length() > 0)
			times[0] = Integer.parseInt(time);
		if (mins != null && mins.trim().length() > 0)
			times[1] = Integer.parseInt(mins);

		return times;
	}

	private void deleteOlderDuplicateConsentRecords(List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans, Subscriber subscriber) throws OnMobileException
	{
		if(doubleConfirmationRequestBeans.size() <= 1)
		{
			logger.info("No older duplicate consent records found !");			
		}
		else {
			logger.info("Deleting older beans");		
			for(int i = 0; i < doubleConfirmationRequestBeans.size() - 1 ; i++)
			{
				DoubleConfirmationRequestBean bean = doubleConfirmationRequestBeans.get(i);
				RBTDBManager.getInstance().deleteConsentRequestByTransIdAndMSISDN(bean.getTransId(), bean.getSubscriberID());
				if(bean.getRequestType().equals("ACT")) {
					String selTransId = getTransIdOfCombinedSel(bean);
					if(selTransId != null) {
						RBTDBManager.getInstance().deleteConsentRequestByTransIdAndMSISDN(selTransId, bean.getSubscriberID());
					}
				}
			}
		}

		boolean isUserActive = com.onmobile.apps.ringbacktones.webservice.common.Utility.isUserActive(subscriber.getStatus());
		DoubleConfirmationRequestBean bean = doubleConfirmationRequestBeans.get(doubleConfirmationRequestBeans.size() - 1);
		if(bean != null && bean.getRequestType().equalsIgnoreCase("ACT") && !isUserActive) {
			deleteOldConsent(subscriber.getSubscriberID(), bean.getTransId());			
		}		
	}
	
	private void deleteOldConsent(String subscriberId, String toBeprocessedTransId) throws OnMobileException {
		List<DoubleConfirmationRequestBean> tobeDeleteDoubleConfirmationRequestBeans = RBTDBManager.getInstance().getConsentRecordListForStatusNMsisdnNType("1", subscriberId, "ACT");
		if(tobeDeleteDoubleConfirmationRequestBeans == null) {
			return;
		}
		for(DoubleConfirmationRequestBean bean : tobeDeleteDoubleConfirmationRequestBeans) {
			if(bean.getTransId().equals(toBeprocessedTransId)) {
				continue;
			}
			RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(bean.getSubscriberID(), bean.getTransId(), "6", "1");
			String selTransId = getTransIdOfCombinedSel(bean);
			if(selTransId != null) {
				RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(bean.getSubscriberID(), selTransId, "6", "1");
			}
		}			
	}

	public void filterConsentBeansBasedOnServiceClassAndServiceId(
			List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans, Subscriber subscriber, String srvId, String srvClass)
	{
		List<DoubleConfirmationRequestBean> upgradeBeans = new ArrayList<DoubleConfirmationRequestBean>();

		if (doubleConfirmationRequestBeans.size() == 0) {
			return;
		} else if (doubleConfirmationRequestBeans.size() > 1) {
			for (DoubleConfirmationRequestBean findBean : doubleConfirmationRequestBeans) {
				if (findBean.getRequestType() != null
						&& findBean.getRequestType()
								.equalsIgnoreCase("UPGRADE")) {
					boolean remove = doubleConfirmationRequestBeans
							.remove(findBean);
					if (remove)
						upgradeBeans.add(findBean);
					break;
				}
			}
			upgradeBeans.addAll(doubleConfirmationRequestBeans);
			logger.debug("upgradeBeans:" + upgradeBeans);
			doubleConfirmationRequestBeans.clear();
			doubleConfirmationRequestBeans.addAll(upgradeBeans);
			logger.debug("doubleConfirmationRequestBeans:-->"
					+ doubleConfirmationRequestBeans);
		}

		logger.info("Filtering pending beans");
		String baseSubClass = null;
		
		boolean isUserActive = com.onmobile.apps.ringbacktones.webservice.common.Utility.isUserActive(subscriber.getStatus());
		
		if(com.onmobile.apps.ringbacktones.webservice.common.Utility.isUserActive(subscriber.getStatus()))
			baseSubClass = subscriber.getSubscriptionClass();
		
		
		List<DoubleConfirmationRequestBean> selectedBeans = new ArrayList<DoubleConfirmationRequestBean>();
		for(DoubleConfirmationRequestBean bean : doubleConfirmationRequestBeans)
		{
			if((isUserActive && bean.getRequestType().equals("ACT")) || (!isUserActive && bean.getRequestType().equals("SEL"))) {
				continue;
			}
			
			String subClass = null;
			String chargeClass = null;
			
			if (baseSubClass != null) {
				if (bean.getRequestType().equals("UPGRADE")) {
					subClass = bean.getSubscriptionClass();
				} else {
					subClass = baseSubClass;
				}
			} else {
				subClass = bean.getSubscriptionClass();
			}

			if(bean.getRequestType().equals("ACT") || bean.getRequestType().equals("UPGRADE"))
				chargeClass = getChargeClassOfCombinedSel(bean);
			else
				chargeClass = bean.getClassType();
			
			boolean isActRequest = false;
			boolean isSelRequest = false;
			boolean isActAndSelRequest = false;
			
			if(chargeClass == null)
				isActRequest = true;
			else if (chargeClass != null && ( bean.getRequestType().equals("ACT") || bean.getRequestType().equals("UPGRADE")))
				isActAndSelRequest = true;
			else
				isSelRequest = true;
			String serviceId = DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_ID", subClass, chargeClass, 
					subscriber.getCircleID(), isActRequest, isSelRequest, isActAndSelRequest);
			String serviceClass = DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_CLASS", subClass, chargeClass, 
					subscriber.getCircleID(), isActRequest, isSelRequest, isActAndSelRequest);
			logger.info("srvId="+srvId+", serviceId="+serviceId+", srvClass="+srvClass+", serviceClass="+serviceClass);
			if(srvId.equalsIgnoreCase(serviceId) && srvClass.equalsIgnoreCase(serviceClass))
				selectedBeans.add(bean);
		}
		logger.info("total beans are "+doubleConfirmationRequestBeans);
		logger.info("selectedBeans beans are "+selectedBeans);
		doubleConfirmationRequestBeans.retainAll(selectedBeans);
		logger.info("filtered beans are "+doubleConfirmationRequestBeans);
	}

	private String getChargeClassOfCombinedSel(DoubleConfirmationRequestBean requestBean)
	{
		String chargeClass = null;
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(requestBean.getExtraInfo());
		if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID"))
		{
			String selTrasId = extraInfoMap.get("TRANS_ID");
			List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager.getInstance().getDoubleConfirmationRequestBeanForStatus(null, selTrasId, requestBean.getSubscriberID(), null, true);
			if(doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0)
			{
				chargeClass = doubleConfirmReqBeans.get(0).getClassType();
				logger.debug("Combo Charge class = "+chargeClass);
			}
		}
		return chargeClass;
	}
	
	private String getTransIdOfCombinedSel(DoubleConfirmationRequestBean requestBean)
	{
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(requestBean.getExtraInfo());
		String selTrasId = null;
		if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID"))
		{
			selTrasId = extraInfoMap.get("TRANS_ID");
			logger.debug("Combo Selection TransID = "+selTrasId);

		}
		return selTrasId;
	}

	public String processVodaCTservice(Task task) {
		logger.info("processing vodactservice request for "+task);
		String response = response_VODACT_INTERNAL_ERROR;

		String msisdn = task.getString(param_VODACT_MSISDN);
		// request parameter contains promo id + keyword (ACT/DCT OR CAN)
		String request = task.getString(param_VODACT_REQUEST);
		String mode = task.getString(param_VODACT_MODE);
		String srvClass = task.getString(param_VODACT_SRVCLASS);
		String prompt = task.getString(param_VODACT_PROMPT);
		String categoryIdStr = task.getString(param_VODACT_CATEGORYID);
		String chargeClass = task.getString(param_VODACT_CHARGECLASS);
		String upgradeRequest = task.getString(param_VODACT_UPGRADE);
		boolean isUpgradeRequest = ("TRUE".equalsIgnoreCase(upgradeRequest)) ? true
				: false; 

		ChargeClass chargeClassObj = getChargeClass(chargeClass);
		if (chargeClass != null && chargeClassObj != null
				&& chargeClassObj.getChargeClass() != null) {
			task.setObject(param_chargeclass, chargeClassObj.getChargeClass());
			task.setObject(param_USE_UI_CHARGE_CLASS, true);
		} else if (chargeClass != null
				&& (chargeClassObj == null || chargeClassObj.getChargeClass() == null)) {
			return reponse_INVALID_CHARGE_CLASS;
		}

		// Either PROMPT or REQUEST are Optional
		if (Utility.isEmpty(prompt) && Utility.isEmpty(request)) {
			// Both REQUEST and PROMPT are empty treated as invalid
			// request.
			logger.warn("Both PROMPT : " + prompt + " and REQUEST: " + request
					+ " are missing, any one is mandatory ");
			return response_VODACT_PARAMETER_MISSING;
		}

		// If prompt is present in the request, check for the validate prompt.
		if (!Utility.isEmpty(prompt) && !Utility.isValidPrompt(prompt)) {
			logger.warn("Invalid PROMPT: " + prompt
					+ ", it is not updating prompt");
			return response_VODACT_PARAMETER_VALIDATION_ERROR;
		}

		if (categoryIdStr == null || categoryIdStr.trim().equals("")) {
			categoryIdStr = "3";
		}

		if (!Utility.isEmpty(msisdn)) {

			Subscriber subscriber = getSubObject(task);
			String subscriberId = subscriber.getSubscriberID();
			String status = subscriber.getStatus();
			String subSubClass = subscriber.getSubscriptionClass();
			boolean isSubActive = isUserActive(status);
			HashMap<String, String> userInfoMap = subscriber.getUserInfoMap();
			String promptInDB = (null != userInfoMap) ? userInfoMap
					.get(param_VODACT_PROMPT) : null;
			logger.debug("Subscriber existing prompt: " + promptInDB
					+ " and new prompt: " + prompt);

			// In case if the request parameter is CT, string contains method
			// returns true when compare it with ACT and process it for
			// activation which is not correct. So, request
			// parameter converting into array list to compare.
			List<String> requestActionList = (null != request) ? Arrays
					.asList(request.toLowerCase().split("\\ "))
					: new ArrayList<String>();

			if (status.equals(SUSPENDED) && !request.equalsIgnoreCase("DCT")) {
				logger.info("Subscriber is suspended");
				return response_VODACT_USER_SUSPENDED;
			}
			if (status.equals(LOCKED)) {
				logger.info("Subscriber is locked");
				return response_VODACT_USER_LOCKED;
			}
			if (!subscriber.isCanAllow()) {
				logger.info("Subscriber is blacklisted");
				return response_VODACT_USER_BLACKLISTED;
			}

			if (!Utility.isEmpty(request)) {

				// Mode and srvclass are mandatory if request is present in
				// the http request.
				if (Utility.isEmpty(mode) || Utility.isEmpty(srvClass)) {
					logger.info("Mode or SrvClass are missing");
					return response_VODACT_PARAMETER_MISSING;
				}

				/*
				 * Added to store the transactionID in activation_info and
				 * selection_info of subscriber and selection tables.
				 */
				String transID = task.getString(param_VODACT_TRANSID);
				String actInfo = (subscriber != null && subscriber
						.getActivationInfo() != null) ? subscriber
						.getActivationInfo() : "";
				actInfo += ":trxid:" + transID + ":";
				task.setObject(param_actInfo, actInfo);
				task.remove(param_transid);

				String circleID = subscriber.getCircleID();

				boolean isActivation = (null != activationKeywordsSet) ? Utility
						.contains(requestActionList, activationKeywordsSet)
						: requestActionList.contains("ACT");
				boolean isCanOrDct = requestActionList.contains("DCT")
						|| requestActionList.contains("CAN");
				boolean isDeactivation = (null != deactivateBaseKeywordsSet) ? Utility
						.contains(requestActionList, deactivateBaseKeywordsSet)
						: isCanOrDct;
				boolean isSelection = (null != rbtKeywordsSet) ? Utility
						.contains(requestActionList, rbtKeywordsSet)
						: requestActionList.contains("CT");

				if (isActivation) {
					// treated as activation request
					StringBuilder activationFailedMsg = new StringBuilder(
							"Unable to process activation request, ");
					activationFailedMsg.append("Subsciber: ").append(
							subscriberId);
					activationFailedMsg.append(", status: ").append(status);

					String promoId = getPromoId(requestActionList);
					Clip clip = getClipByPromoId(promoId);
					Category category = null;
					if (clip == null) {
						category = getCategoryByPromoId(promoId);
					}

					// It should not allow the activation request, when
					// subscriber is in activation pending/grace/deactivation
					// pending.
					if (promoId == null) {
						if (status.equals(ACT_PENDING)) {
							logger.error(activationFailedMsg.toString());
							return response_VODACT_ACTIVATION_PENDING;
						} else if (status.equals(GRACE)) {
							logger.error(activationFailedMsg.toString());
							return response_VODACT_SUBSCRIPTION_ALREADY_EXISTS;
						} else if (status.equals(DEACT_PENDING)) {
							logger.error(activationFailedMsg.toString());
							return response_VODACT_DEACTIVATION_PENDING;
						}
					}

					if (isSubActive
							&& promoId != null
							&& !param(
									"SMS",
									dbparam_VODACTURL_PROCESS_SEL_FOR_ALREADY_ACTIVE,
									true)) {
						activationFailedMsg
								.append(", Subscription Already Exists");
						logger.error(activationFailedMsg.toString());
						return response_VODACT_SUBSCRIPTION_ALREADY_EXISTS;
					} else {
						// At first, Subscription class will be fetched from the
						// configuration based on Mode, SrvClass and Circle if
						// it is not found, again it will fetch based on Mode
						// and SrvClass.
						String subClass = Utility.getVodaCTSubClass(mode,
								srvClass, circleID);
						if (null == subClass || INVALID.equals(subClass)) {
							logger.error(activationFailedMsg
									+ " Missed Suscription class configuration");
							return response_VODACT_SERVICE_NOT_CONFIGURED;
						}
						
						if (isUpgradeRequest && isSubActive) {
							String upgradedSubClass = upgradeSubscriptionClassMap
									.get(subSubClass);
							if (!subClass.equals(upgradedSubClass)) {
								logger.error("VodaCT Service "
										+ "subscriptionClass is not"
										+ " configured. subscriptionClass: "
										+ upgradedSubClass + ", from existing "
										+ "subscriptionClass: " + subSubClass
										+ ", for subscriberId: " + subscriberId);
								return response_VODACT_SERVICE_NOT_CONFIGURED;
							}
							task.setObject(param_rentalPack, upgradedSubClass);
							logger.debug("Upgrading to subscriptionClass: "
									+ upgradedSubClass + ", from existing "
									+ "subscriptionClass: " + subSubClass
									+ ", for subscriberId: " + subscriberId);
						}
						
						logger.info("Processing with SuscriptionClass: "
								+ subClass);

						// At first, CosId will be fetched from the
						// configuration
						// based on Mode, SrvClass and Circle if it is not found
						// again it will fetch based on Mode and SrvClass.
						String cosId = Utility.getVodaCTCosId(mode, srvClass,
								circleID);

						if (INVALID.equals(cosId)) {
							cosId = null;
						}
						logger.info("Processing with CosId : " + cosId);

						if (mode.equalsIgnoreCase("TNBOBD")) {
							String userType = subscriber.isPrepaid() ? "p"
									: "b";
							try {
								Offer[] offers = RBTSMClientHandler
										.getInstance().getOffer(subscriberId,
												"TNBOBD",
												Offer.OFFER_TYPE_SUBSCRIPTION,
												userType, subClass, null);
								if (offers == null || offers.length == 0) {
									logger.error(activationFailedMsg
											+ "Tnb Already availed");
									return response_VODACT_TNBOBD_ALREADY_AVAILED;
								} else {
									task.setObject(param_offerID,
											offers[0].getOfferID());
								}
							} catch (RBTException rbte) {
								logger.error(
										"Failed to get offers. RBTException: "
												+ rbte.getMessage(), rbte);

							}
						}

						boolean isValidContent = isValidContent(clip, category);

						task.setObject(param_actby, mode);
						if (task.getString(param_RBTTYPE) != null) {
							task.setObject(param_rbttype, Integer.parseInt(task.getString(param_RBTTYPE)));
						} else if (mode.equalsIgnoreCase("ADRBT")) {
							task.setObject(param_rbttype, 1);
						}
						task.setObject(param_subclass, subClass);
						task.setObject(param_COSID, cosId);

						if (!isValidContent) {
							// Since there is no valid content, makes only
							// subscriber activation.
							subscriber = processActivation(task);
							response = task.getString(param_response);
							response = convertToVodaCTServiceResponse(response);
						} else {
							// Make subscriber base activation and selection.
							logger.debug("Processing both activation and selection");

							// It should not allow activation and selection
							// request when the subscriber is active on
							// corporate feature.
							if (status.equalsIgnoreCase(ACTIVE)
									&& isCorporateUser(subscriber)) {
								logger.error("Subscriber is corporate user. Subscriber Id: "
										+ subscriberId);
								return response_VODACT_SELECTION_NOT_ALLOWED;
							}

							task.setObject(
									param_inLoop,
									param("COMMON",
											"VODACT_SERVICE_ALLOW_INLOOP",
											false) ? "yes" : "no");

							if (clip != null) {
								task.setObject(param_clipid,
										String.valueOf(clip.getClipId()));
								task.setObject(param_catid, categoryIdStr);
							} else {
								task.setObject(param_catid, String
										.valueOf(category.getCategoryId()));
							}

							response = processSetSelection(task);
							response = convertToVodaCTServiceResponse(response);
						}
					}
					logger.info("Process activation response: " + response);
				} else if (isDeactivation) {
					// deactivation request
					try {
						// It should not allow de-activation request when the
						// subscriber is
						// active on corporate feature.
						if ((status.equalsIgnoreCase(ACTIVE) || status.equalsIgnoreCase(GRACE) || status.equalsIgnoreCase(SUSPENDED))
								&& isCorporateUser(subscriber)) {
							logger.error("Subscriber is corporate user. Subscriber Id: "
									+ subscriberId);
							return response_VODACT_COPRORATE_DEACTIVATION_NOT_ALLOWED;
						}
						if (status.equalsIgnoreCase(ACTIVE)
								|| status.equalsIgnoreCase(LOCKED)
								|| status.equalsIgnoreCase(SUSPENDED)
								|| status.equalsIgnoreCase(GRACE)) {
							SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
									subscriberId);
							subscriptionRequest.setMode(mode);
							subscriptionRequest.setModeInfo(mode + actInfo);

							logger.info("Processing deactivation"
									+ " Request: " + subscriptionRequest);

							rbtClient.deactivateSubscriber(subscriptionRequest);
							response = subscriptionRequest.getResponse();

							logger.info("Deactivation request response is: "
									+ response);
							if (response.equalsIgnoreCase(SUCCESS)) {
								logger.info("Deactivation is Success");
								return response_VODACT_SUCCESS_WITH_ID;
							} else {
								logger.info("Deactivation is Failed");
								return response_VODACT_FAILED;
							}
						} else if (status.equalsIgnoreCase(NEW_USER)
								|| status.equalsIgnoreCase(DEACTIVE)) {
							logger.info("No Subscriptions Found for Deactivation");
							return response_VODACT_NO_SUBSCRIPTIONS_FOUND;
						} else if (status.equalsIgnoreCase(ACT_PENDING)) {
							logger.info("Failed to Deactivate, Subscriber status is Act Pending");
							return response_VODACT_ACTIVATION_PENDING;
						} else if (status.equalsIgnoreCase(DEACT_PENDING)) {
							response = response_VODACT_PREVIOUS_DEACTIVATION_PENDING;
							logger.info("Failed to deactivate, Previous Deactivation Pending");
							return response;
						} else if (status.equalsIgnoreCase(BLACK_LISTED)) {
							logger.info("Failed to deactivate, User Blacklisted");
							return response_VODACT_USER_BLACKLISTED;
						}
					} catch (Exception e) {
						logger.error(
								"Unable to process subscription deactivation : "
										+ e.getMessage(), e);
					}
					logger.info("Failed to deactivate subscription, generic error occured");
					return response_VODACT_ERROR;
				} else if (isSelection) {
					// Treated as selection request

					// It should not allow selection request when the subscriber
					// is active on corporate feature.
					if (status.equalsIgnoreCase(ACTIVE)
							&& isCorporateUser(subscriber)) {
						logger.error("Subscriber is corporate user. Subscriber Id: "
								+ subscriberId);
						logger.error("Subscriber is corporate user. Subscriber Id: "
								+ subscriberId);
						return response_VODACT_SELECTION_NOT_ALLOWED;
					}

					String promoId = getPromoId(requestActionList);
					Clip clip = getClipByPromoId(promoId);
					Category category = null;
					if (clip == null) {
						category = getCategoryByPromoId(promoId);
					}

					boolean isValidContent = isValidContent(clip, category);
					if (!isSubActive
							&& !param("COMMON",
									dbparam_VODACTURL_ACTIVATION_OPTIONAL, true)) {
						logger.info("Unable to process selection,"
								+ " No Subscriptions Found");
						return response_VODACT_NO_SUBSCRIPTIONS_FOUND;
					} else if (isSubActive && !isValidContent) {
						logger.info("Unable to process selection, Content Not Found");
						return response_VODACT_CONTENT_NOT_FOUND;
					} else {
						// At first, Subscription class will be fetched from the
						// configuration based on Mode, SrvClass and Circle if
						// it is not found, again it will fetch based on Mode
						// and SrvClass.
						String subClass = Utility.getVodaCTSubClass(mode,
								srvClass, circleID);
						if (null == subClass || INVALID.equals(subClass)) {
							logger.error(" Missed Suscription class configuration");
							return response_VODACT_SERVICE_NOT_CONFIGURED;
						}
						
						if (isUpgradeRequest && isSubActive) {
							String upgradedSubClass = upgradeSubscriptionClassMap
									.get(subSubClass);
							if (!subClass.equals(upgradedSubClass)) {
								logger.error("VodaCT Service "
										+ "subscriptionClass is not"
										+ " configured. subscriptionClass: "
										+ upgradedSubClass + ", from existing "
										+ "subscriptionClass: " + subSubClass
										+ ", for subscriberId: " + subscriberId);
								return response_VODACT_SERVICE_NOT_CONFIGURED;
							}
							task.setObject(param_rentalPack, upgradedSubClass);
							logger.debug("Upgrading to subscriptionClass: "
									+ upgradedSubClass + ", from existing "
									+ "subscriptionClass: " + subSubClass
									+ ", for subscriberId: " + subscriberId);
						}
						
						logger.info("Processing with SuscriptionClass: "
								+ subClass);

						// At first, CosId will be fetched from the
						// configuration
						// based on Mode, SrvClass and Circle if it is not found
						// again it will fetch based on Mode and SrvClass.
						String cosId = Utility.getVodaCTCosId(mode, srvClass,
								circleID);

						logger.info("Processing with Suscription class: "
								+ subClass);

						if (!isSubActive
								&& (null == subClass || INVALID
										.equals(subClass))) {
							String activationFailedMsg = "Unable to process activation request, ";
							logger.info(activationFailedMsg
									+ "Suscription class is not Configured");
							return response_VODACT_SERVICE_NOT_CONFIGURED;
						}

						if (INVALID.equals(cosId)) {
							cosId = null;
						}
						logger.info("Processing with CosId: " + cosId);

						task.setObject(param_actby, mode);
						if (task.getString(param_RBTTYPE) != null) {
							task.setObject(param_rbttype, Integer.parseInt(task.getString(param_RBTTYPE)));
						} else if (mode.equalsIgnoreCase("ADRBT")) {
							task.setObject(param_rbttype, 1);
						}
						task.setObject(param_subclass, subClass);
						task.setObject(param_COSID, cosId);

						if (!isValidContent) {
							logger.warn("Content is not valid. Process activation: "
									+ task);
							subscriber = processActivation(task);
							response = task.getString(param_response);
							response = convertToVodaCTServiceResponse(response);

						} else {
							task.setObject(
									param_inLoop,
									param("COMMON",
											"VODACT_SERVICE_ALLOW_INLOOP",
											false) ? "yes" : "no");

							if (clip != null) {
								task.setObject(param_clipid,
										String.valueOf(clip.getClipId()));
								task.setObject(param_catid, categoryIdStr);
							} else {
								task.setObject(param_catid, String
										.valueOf(category.getCategoryId()));
							}

							response = processSetSelection(task);
							response = convertToVodaCTServiceResponse(response);
						}
					}
					logger.info("Process Selection response is: " + response);
				} else {
					logger.warn("Could not process, no valid keyword to process. response string: "
							+ response);
					return response_VODACT_NO_SUBSCRIPTIONS_FOUND;
				}
			} else {
				// Subscriber is new_user and the request(ACT|SEL|DCT) is empty.
				// not need to update player
				if (status.equalsIgnoreCase(NEW_USER)) {
					logger.warn("Subscriber is new_user and the request(ACT|SEL|DCT) is empty");
					return response_VODACT_NO_SUBSCRIPTIONS_FOUND;
				}

			}

			boolean isErrorOrSuccess = (response
					.equalsIgnoreCase(response_VODACT_INTERNAL_ERROR) || response
					.equalsIgnoreCase(response_VODACT_SUCCESS_WITH_ID));

			if (!Utility.isEmpty(prompt) && prompt.equals(promptInDB)
					&& isErrorOrSuccess) {
				logger.warn("Subscriber already set with the same prompt");
				return response_VODACT_SUCCESS_WITH_ID;
			}

			logger.debug("Checking for user deactive status: " + status);

			if (!Utility.isEmpty(prompt) && !prompt.equals(promptInDB)
					&& !isUserDeactive(status)) {
				logger.debug("Updating subscriber prompt, response: "
						+ response);
				updatePromtAndPlayerStatus(task, prompt, subscriberId);
				// Should not overwrite the subscription or selection response.
				if (response.equals(response_VODACT_INTERNAL_ERROR)) {
					return response_VODACT_SUCCESS_WITH_ID;
				}
			}

		} else {
			logger.warn("Mandatory parameter MSISDN is missing ");
			return response_VODACT_PARAMETER_MISSING;
		}
		logger.debug("response: " + response);
		return response;
	}

	/**
	 * Update the prompt in Task object and makes a rbt client hit to update the
	 * subscriber's prompt in extra info column of rbt_subscriber table.
	 * 
	 * @param task
	 * @param prompt
	 * @param subscriberId
	 */
	protected void updatePromtAndPlayerStatus(Task task, String prompt,
			String subscriberId) {
		task.setObject(param_isPressStarIntroEnabled, true);
		UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(
				subscriberId);
		if ("0".equals(prompt)) {
			updateRequest.setIsPressStarIntroEnabled(true);
		} else {
			updateRequest.setIsPressStarIntroEnabled(false);
		}
		logger.debug("Updating subscriber Prompt. UpdateRequest: "
				+ updateRequest);
		rbtClient.setSubscriberDetails(updateRequest);
		logger.debug("Prompt and PlayerStatus has been updated. SubscriberId "
				+ subscriberId);
	}
	
	private String getPromoId(List<String> requestActionList) {
		String str = null;
		for(String action: requestActionList) {
			if(null != activationKeywordsSet) {
				if(activationKeywordsSet.contains(action)) {
					continue;
				}
			}
			if(null != rbtKeywordsSet) {
				if(rbtKeywordsSet.contains(action)) {
					continue;
				}
			}
			if(null != deactivateBaseKeywordsSet) {
				if(rbtKeywordsSet.contains(action)) {
					continue;
				}
			}
			str = action;
		}
		return str;
	}

	private String convertToVodaCTServiceResponse(String response) {
		if (response.equalsIgnoreCase("success"))
			return response_VODACT_SUCCESS_WITH_ID;
		else if (response.equalsIgnoreCase(activationPending))
			return response_VODACT_ERROR;
		else if (response.equalsIgnoreCase(deactivationPending))
			return response_VODACT_ERROR;
		else if (response.equalsIgnoreCase(alreadyActive))
			return response_VODACT_SUBSCRIPTION_ALREADY_EXISTS;
		else if (response.equalsIgnoreCase(suspended))
			return response_VODACT_USER_SUSPENDED;
		else if (response.equalsIgnoreCase(blackListedSMSText))
			return response_VODACT_USER_BLACKLISTED;
		else if (response.equalsIgnoreCase("invalid")
				|| response.equalsIgnoreCase(invalidPrefix))
			return response_VODACT_INVALID_MSISDN;
		else if (response.equals(SELECTION_SUSPENDED))
			return response_VODACT_SELECTION_SUSPENDED;
		else if (response.equalsIgnoreCase(ALREADY_EXISTS))
			return response_VODACT_SELECTION_ALREADY_EXIXTS;
		else if (response.equals(SUCCESS_DOWNLOAD_EXISTS))
			return response_VODACT_SUCCESS_WITH_ID;
		else if (response.equals(NOT_ALLOWED))
			return response_VODACT_SELECTION_NOT_ALLOWED;
		else if (response.equalsIgnoreCase(SELECTION_OVERLIMIT))
			return response_VODACT_SELECTION_OVERLIMIT;
		else if (response.equalsIgnoreCase(PERSONAL_SELECTION_OVERLIMIT))
			return response_VODACT_PERSONAL_SELECTION_OVERLIMIT;
		else if (response.equalsIgnoreCase(LOOP_SELECTION_OVERLIMIT))
			return response_VODACT_LOOP_SELECTION_OVERLIMIT;
		else if (response.equals(OVERLIMIT))
			return response_VODACT_SELECTION_OVERLIMIT;
		else if (response.equals(DEACT_PENDING))
			return response_VODACT_DEACTIVATION_PENDING;
		else if (response.equals(LITE_USER_PREMIUM_BLOCKED))
			return response_VODACT_PREMIUM_CONTENT_BLOCKED;
		else if(response.equals(LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))
			return response_VODACT_CONTENT_BLOCKED_FOR_COSID;
		else if (response.equals(WebServiceConstants.COS_NOT_UPGRADE_USER_NO_SELECTION))
			return response_VODACT_ERROR;
		else if (response.equals(WebServiceConstants.COSID_BLOCKED_FOR_NEW_USER))
			return response_VODACT_ERROR;
		else if (response.equals(WebServiceConstants.COSID_BLOCKED_FOR_USER))
			return response_VODACT_ERROR;
		else if (response.equals(WebServiceConstants.COSID_BLOCKED_CIRCKET_PROFILE))
			return response_VODACT_ERROR;
		else if(response.equalsIgnoreCase(
				WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)){
			return response_VODACT_SELECTION_NOT_ALLOWED;
		}
		else if(response.equalsIgnoreCase(
				WebServiceConstants.UPGRADE_NOT_ALLOWED)){
			return response_VODACT_UPGRADE_NOT_ALLOWED;
		}else if(response.equalsIgnoreCase(
				WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION)){
			return response_VODACT_COPRORATE_SELECTION_NOT_ALLOWED;
		}
		else
			return response_VODACT_ERROR;
	}

	@Override
	public void processCategoryByAlias(Task task) {

	}

	@Override
	public void processCategoryByPromoID(Task task) {

	}

	@Override
	public void processCategorySearch(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processClipByAlias(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processClipByPromoID(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processDefaultSearch(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processFeed(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processHelpRequest(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processProfile(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRetailerAccept(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRetailerRequest(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRetailerSearch(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processSelection(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processDelete(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processLoop(Task task) {
		// TODO Auto-generated method stub

	}

	private boolean isValidContent(Clip clip, Category category) {
		if (clip != null && clip.getClipEndTime() != null
				&& clip.getClipEndTime().getTime() > System.currentTimeMillis())
			return true;
		if (category != null
				&& category.getCategoryEndTime() != null
				&& category.getCategoryEndTime().getTime() > System
						.currentTimeMillis())
			return true;
		return false;
	}

	private void initCopyKeys() {
		String normalCopyKeys = getParameter("COMMON",
				"CIRCLEWISE_NORMALCOPY_KEY");
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
		String optinCopyKeys = getParameter("COMMON", "CIRCLEWISE_STARCOPY_KEY");
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
		
		consentModesNoTransId = ListUtils.convertToList(getParameter(iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_MODES_NO_TRANSID"), ",");
	}

	public boolean isCorporateUser(Subscriber subscriber) {
		String userType = subscriber.getUserType();
		if (userType != null
				&& userType.equalsIgnoreCase(WebServiceConstants.CORPORATE)) {
			logger.debug("Subscriber is corporate user");
			return true;
		}
		return false;
	}
	
	private void initVirtualNumbers() {
		copyVirtualNumbers = new HashSet<String>();
		List<Parameters> virtualNoParameters = CacheManagerUtil.getParametersCacheManager().getParameters("VIRTUAL_NUMBERS");
		if (virtualNoParameters != null) {
			for (Parameters virtualNoParameter : virtualNoParameters) {
				copyVirtualNumbers.add(virtualNoParameter.getParam());
			}
			logger.info("The set of copy virtual numbers are : "
					+ copyVirtualNumbers);
		}
	}
	
	//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
	private void writeCDRLog(DoubleConfirmationRequestBean reqBean,String isCombo, String srvclass, String srvid) {
			//TIMESTAMP, TRANSACTION_TYPE, CIRCLE, MISIDN, REQEUST_TYPE, REQUEST_MODE, REQUEST_ID, SERVICE_ID, SERVICE_CLASS, SUBSCRIPTION_CLASS, CHARGE_CLASS, PROMO_ID, MODE_INFO 
			SimpleDateFormat dateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
			String currentTime = dateFormat.format(new Date());
			String transactionType = "callback";
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(reqBean.getSubscriberID());
			Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
			String circleId = subscriber!=null?subscriber.getCircleID():"";
			String requestType = reqBean.getRequestType();
			if(isCombo != null)
			  requestType = "COMBO";
			
			String promoId="";
		    Category category = rbtCacheManager.getCategory(reqBean.getCategoryID());
			if(category != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(category.getCategoryTpe())) {
				promoId = category.getCategoryPromoId();
			  
			}else {
				 Clip clip = rbtCacheManager.getClip(reqBean.getClipID());
				 if(clip != null)
				  promoId =  clip.getClipPromoId();
			}
			String modeInfoParam = reqBean.getSelectionInfo();
			String modeInfo = "NA";
			String twShortCodeValue = WriteCDRLog.getTwShortCodeFromMode(modeInfoParam);
			if(twShortCodeValue != null) {
				modeInfo = twShortCodeValue;	
			}
			cdr_logger.info(currentTime+ ","+transactionType+ "," +circleId+","+reqBean.getSubscriberID()+","+requestType+","+reqBean.getMode()+","+reqBean.getTransId()+","
			+srvid+","+srvclass+","+reqBean.getSubscriptionClass()+","+reqBean.getClassType()+","+promoId+","+modeInfo);	
		}
}
