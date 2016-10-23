package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.logger.consent.ConsentDaemonTransLogger;
import com.onmobile.apps.ringbacktones.logger.consent.ConsentUrlHitLogger;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.common.exception.OnMobileException;

public class DoubleConfirmationConsentPushThread extends Thread {

	DoubleConfirmationDBFetcher dbFetcher = null;
	static Logger logger = Logger.getLogger(DoubleConfirmationConsentPushThread.class);
	protected static Map<String, String> amtSubClassMapping = null;
	protected static Map<String, String> amtChargeClassMapping = null;
	protected static Map<String, String> modeMapping = null;
	private static Map<String, String> externalToInternalModeMapping = null;
	protected static List<String> modesForNotToGetConsentList = null;
	protected static int maxNoOfRetrials = -1;
	private static boolean isHttpVodafoneRequired = false; // used in vodafone
	private static String countryPrefix = null;
	private SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat ussdDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	protected SimpleDateFormat RequestTimeDateFormat = null;	
	private static Map<String, String> modeAndFromParamMap = null;
	private static Map<String, String> usernameModeMap = null;
	private static Map<String, String> passwordModeMap = null;
	private static Map<String, String> urlModeMap = null;
	public static List<String> modesForGetHttp = null;
	private static String loggerName = "PUSH";
	protected static Logger consentPushLogger = RbtLogger
			.createRollingFileLogger(RbtLogger.consentTransactionPrefix
					+ loggerName, ROLLING_FREQUENCY.HOURLY);
	private static List<String> allowededRetryReponseCodes = new ArrayList<String>(); 
	
	static
	{
		try
		{
			String amtSubClassStr = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "TPCG_SUB_CLASS_MAP", null);
			logger.info("amtSubClassStr="+amtSubClassStr);
			String amtChargeClassStr = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "TPCG_CHARGE_CLASS_MAP", null);
			logger.info("amtChargeClassStr="+amtChargeClassStr);
			String modeMappingStr = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "TPCG_MODES_MAP", null);
			logger.info("modeMappingStr="+modeMappingStr);
			String internalToExternalModeMappingStr = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "INTERNAL_EXTERNAL_MODES_MAP", null);
			logger.info("internalToExternalModeMappingStr="+internalToExternalModeMappingStr);
			String modesForNotToGetConsent = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "MODES_FOR_NOT_CONSENT_HIT", null);
			logger.info("modesForNotToGetConsent="+modesForNotToGetConsent);
			maxNoOfRetrials = RBTParametersUtils.getParamAsInt(
					"DOUBLE_CONFIRMATION", "MAX_NO_OF_RETRIALS_ALLOWED", 6);
			logger.info("maxNoOfRetrials="+maxNoOfRetrials);
			String retryReponseCodes = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "CG_RETRY_REPONSE_CODES", null);
			logger.info("retryReponseCodes="+retryReponseCodes);
			amtChargeClassMapping = MapUtils.convertToMap(amtChargeClassStr, ";",
					"=", ",");
			logger.info("amtChargeClassMapping="+amtChargeClassMapping);
			amtSubClassMapping = MapUtils.convertToMap(amtSubClassStr, ";", "=",
					",");
			logger.info("amtSubClassMapping="+amtSubClassMapping);
			modeMapping = MapUtils.convertToMap(modeMappingStr, ";", "=", ",");
			logger.info("modeMapping="+modeMapping);
			externalToInternalModeMapping = MapUtils.convertToMap(internalToExternalModeMappingStr, ";", "=", ",");
			logger.info("externalToInternalModeMapping="+externalToInternalModeMapping);
			if (modesForNotToGetConsent != null)
				modesForNotToGetConsentList = Arrays.asList(modesForNotToGetConsent
						.split(","));
			logger.info("modesForNotToGetConsentList="+modesForNotToGetConsentList);
			isHttpVodafoneRequired = RBTParametersUtils.getParamAsBoolean("DOUBLE_CONFIRMATION", "IS_DOUBLE_CONSENT_VODAFONE_URL", "false");
			logger.info("isHttpVodafoneRequired="+isHttpVodafoneRequired);
			
			String prefixParam = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
			StringTokenizer prefixParamTokens = new StringTokenizer(prefixParam, ",");
			countryPrefix = prefixParamTokens.nextToken().trim();
			logger.info("countryPrefix="+countryPrefix);
			
			modeAndFromParamMap = MapUtils.convertToMap(
					RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "MODE_AND_FROM_PARAM_MAP", null), ";", ":", null);
			logger.info("modeAndFromParamMap="+modeAndFromParamMap);
				
			usernameModeMap = MapUtils.convertToMap(
					RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "USERNAME_MODE_MAP", null), ";", ",", null);
			logger.info("usernameModeMap="+usernameModeMap);
			
			passwordModeMap = MapUtils.convertToMap(
					RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "PASSWORD_MODE_MAP", null), ";", ",", null);
			logger.info("passwordModeMap="+passwordModeMap);
			
			urlModeMap = MapUtils.convertToMap(
					RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "CG_URL_MODE_MAP", null), ";", ",", null);
			logger.info("urlModeMap="+urlModeMap);
			modesForGetHttp = ListUtils.convertToList(
					RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "CG_HTTP_GET_MODES", null), ",");
			logger.info("modesForGetHttp="+modesForGetHttp);
			if (retryReponseCodes != null)
				allowededRetryReponseCodes = Arrays.asList(retryReponseCodes
						.split(","));
			logger.info("allowededRetryReponseCodes="+allowededRetryReponseCodes);
		}
		catch(Exception e)
		{
			logger.error("Exception in static block", e);
		}
	}

	public DoubleConfirmationConsentPushThread(
			DoubleConfirmationDBFetcher dbFetcher) {
		this.dbFetcher = dbFetcher;
	}

	public void run() {
		while (true) {
			logger.info("Entering while loop");
			DoubleConfirmationRequestBean copyRequest = null;
			synchronized (dbFetcher.contentQueue) {
				if (dbFetcher.contentQueue.size() > 0) {
					logger.info("Consent Push thread found contentrequest, "
							+ dbFetcher.contentQueue.get(0));
					copyRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(copyRequest);
				} else {
					try {
						logger
								.info("Consent Push thread waiting as queue size="
										+ dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					} catch (InterruptedException e) {
						logger.error("Exception", e);
					}
					continue;
				}				
			}

			if (copyRequest != null	&& Arrays.asList(
					RBTParametersUtils.getParamAsString("DOUBLE_CONFIRMATION","MODES_FOR_AOC_CONSENT_PUSH", "").split(","))
					.contains(copyRequest.getMode())) { 
				processWithoutHittingConsent(copyRequest);
			}else if(isHttpVodafoneRequired)
				hitConsentURLVodafone(copyRequest);
			else	
				hitConsentURL(copyRequest);
			synchronized (dbFetcher.pendingQueue) {
			    dbFetcher.pendingQueue.remove(copyRequest);
			}
		}
	}

	public void hitConsentURL(DoubleConfirmationRequestBean requestBean) {
		// CP to TPCG
		// http://TPCGIP:TPCGPORT/API/TPCG? MSISDN=<10 digit
		// number>&productID=<pid>&pName=<name of
		// product>&pPrice=1000&pVal=30&CpId=45&CpPwd=245&CpName=Hungama&reqMode=IVR&Ivr_approach=VXML&NetworkId=CDMA
		// &circleId=MH&Lang=Hindi&Lang_A=F&reqType=Event&ismID=10&transID=24525
		try {
			if (requestBean == null) {
				return;
			}

			long timeTakenToQueryWDS = 0;
			long timeTakenToHITConsent = 0;
			String consentUrl = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION",
					"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL", null);
			String requestDateFormat = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "DATE_FORMAT_OF_REQUEST_TIME",
					"yyMMddHHmmss");
			RequestTimeDateFormat = new SimpleDateFormat(requestDateFormat);
			//Removed for bug RBT-18793
		/*	if (modesForNotToGetConsentList != null
					&& modesForNotToGetConsentList.contains(requestBean
							.getMode()))
				return;
				*/
			String pPrice = "-1";
			String pVal = "-1";
			String subscriberID = requestBean.getSubscriberID();
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriberID);
			Subscriber subscriber = RBTClient.getInstance().getSubscriber(
					rbtDetailsRequest);

			logger.debug("Subscriber: " + subscriber);
			//CG Integration Flow - Jira -12806
			boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.DOUBLE_CONFIRMATION,
					"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
			//RBT-12024
			if(RBTParametersUtils.getParamAsString("COMMON", "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null)!=null)
            {
				String circleID = subscriber.getCircleID();
				if (circleID != null && !circleID.equals("") && !circleID.equalsIgnoreCase("null")) {
					// RBT-18793
					//String operatorName = circleID.substring(0,circleID.indexOf("_"));
					String operatorName = subscriber.getOperatorName();
					logger.info(":--->operatorName"+operatorName);
					if (operatorName != null
							&& !operatorName.equalsIgnoreCase("null")
							&& !operatorName.equals("")) {
						String consentUrlStr = "DOUBLE_CONFIRMATION_CONSENT_PUSH_URL_FOR_"
								+ operatorName.toUpperCase();
						consentUrl = RBTParametersUtils.getParamAsString(
								"DOUBLE_CONFIRMATION", consentUrlStr, consentUrl);
					}
				}
			}
			
			String language = null;
			String eventType = "2";
			String opt1 = null;
			
			String comboTransID = null;
			String clipName = null;
			String clipId = null;
			String param1="0";
			DoubleConfirmationRequestBean dbConfirmReqBean = null;
			String classType = requestBean.getClassType();
			String subClass = requestBean.getSubscriptionClass();
			int categoryId = -1;
			String vcode = null;
			boolean isActAndSelRequest = false;
			boolean isSelRequest = false;
			boolean isActRequest = false;
			if ((subscriber == null || !com.onmobile.apps.ringbacktones.webservice.common.Utility
					.isUserActive(subscriber.getStatus()))
					&& requestBean.getRequestType().equalsIgnoreCase("ACT")) {
				
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(requestBean.getExtraInfo());
				if (extraInfoMap != null
						&& extraInfoMap.containsKey("TRANS_ID")) {
					String selTrasId = extraInfoMap.get("TRANS_ID");
					List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
							.getInstance()
							.getDoubleConfirmationRequestBeanForStatus(null,
									selTrasId, subscriberID, null, true);
					if (doubleConfirmReqBeans != null
							&& doubleConfirmReqBeans.size() > 0) {
						dbConfirmReqBean = doubleConfirmReqBeans.get(0);
					}
				}
				isActRequest = true;
				if (dbConfirmReqBean != null) {
					comboTransID = dbConfirmReqBean.getTransId();
					classType = dbConfirmReqBean.getClassType();
					if (classType == null) {
						Category category = RBTCacheManager.getInstance()
								.getCategory(requestBean.getCategoryID());
						if (category != null) {
							classType = category.getClassType();
							logger
									.debug("Category Charge class = "
											+ classType);
						}
					}
					logger.debug("Combo Charge class = " + classType);
					clipName = getClipName(dbConfirmReqBean);
					vcode = getVcode(dbConfirmReqBean);
					categoryId = dbConfirmReqBean.getCategoryID();
					isActAndSelRequest= true;
					eventType = "1";
				}
				if (checkCGFlowForBSNL || consentUrl.contains("%contentId%")) {//CG Integration Flow - Jira -12806
					clipId = getContentId(dbConfirmReqBean);
					if (null == clipId) {
						consentUrl = consentUrl.replaceAll("%contentId%","NULL");
					} else {
						consentUrl = consentUrl.replaceAll("%contentId%",
								clipId);

					}
				}
				subClass = requestBean.getSubscriptionClass();
			} else if (requestBean.getRequestType().equalsIgnoreCase("SEL")
					|| requestBean.getRequestType().equalsIgnoreCase("DWN")) {
				categoryId = requestBean.getCategoryID();
				clipName = getClipName(requestBean);
				// CG Integration Flow - Jira -12806
				if (checkCGFlowForBSNL || consentUrl.contains("%contentId%")) {
					clipId = getContentId(requestBean);
					if (null == clipId) {
						consentUrl = consentUrl.replaceAll("%contentId%", "NULL");
					} else {
						consentUrl = consentUrl.replaceAll("%contentId%",
								clipId);
					}
				}
				vcode = getVcode(requestBean);
				isSelRequest =true;
				
			}

			if ((classType == null || classType.equalsIgnoreCase("null"))
					&& categoryId != -1) {

				Category category = RBTCacheManager.getInstance().getCategory(
						requestBean.getCategoryID());
				if (category != null) {
					classType = category.getClassType();
				}
				logger.debug("CLASS TYPE = " + classType);
				if ((subClass == null || subClass.equalsIgnoreCase("null"))
						&& (subscriber == null || !com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isUserActive(subscriber.getStatus())))
					subClass = "DEFAULT";
			}

			Boolean isSeparatePriceAndValidityForComboEnabled = Boolean.parseBoolean(CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.COMMON,
							"SEPARATE_PRICE AND VALIDITY_FO"
							+ "R_COMBO_ENABLED",
							"FALSE"));
			if (subscriber != null
					&& !requestBean.getRequestType().equalsIgnoreCase("ACT")) {
				pPrice = String.valueOf(getPrice(amtChargeClassMapping, classType));
				pVal = String.valueOf(getValidityPeriod(amtChargeClassMapping, classType));
			} else {
				int pPrice1 = getPrice(amtSubClassMapping, subClass);						
				int pPrice2 = getPrice(amtChargeClassMapping, classType);
				if (isSeparatePriceAndValidityForComboEnabled && classType != null) {
					pPrice = String.valueOf(pPrice1 + "|" + pPrice2);
					pVal = getValidityPeriod(amtSubClassMapping, subClass) + "|" + getValidityPeriod(amtChargeClassMapping, classType) ;
				} else {
					pPrice = String.valueOf(pPrice1 + pPrice2);
					pVal = String.valueOf(getValidityPeriod(amtSubClassMapping, subClass));
				}
			}

			logger.debug("pPrice == " + pPrice + " pVal === " + pVal);
			String extraInfo = requestBean.getExtraInfo();
			HashMap<String, String> xtraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			String noOfTrialsStr = null;
			if (xtraInfoMap != null && xtraInfoMap.containsKey("NO_OF_TRIALS")) {
				noOfTrialsStr = xtraInfoMap.get("NO_OF_TRIALS");
				if (Integer.parseInt(noOfTrialsStr) >= maxNoOfRetrials){
					try {
						logger.debug("Retry exceed noOfTrialsStr: "
								+ noOfTrialsStr + " maxNoOfRetrials: "
								+ maxNoOfRetrials + " subscriberId: "
								+ subscriberID);
						RBTDBManager.getInstance()
								.updateConsentStatusOfConsentRecord(
										requestBean.getSubscriberID(), requestBean.getTransId(), "3");
						//Combo consent request
						if (xtraInfoMap != null
								&& xtraInfoMap.containsKey("TRANS_ID")) { 
							String selectionTransId = xtraInfoMap
									.get("TRANS_ID");
							logger.info("Processing combo request. selectionTransId: "
									+ selectionTransId);
							RBTDBManager.getInstance()
									.updateConsentStatusOfConsentRecord(
											requestBean.getSubscriberID(),
											selectionTransId, "3");
						}
						return;
					} catch (OnMobileException e) {
						logger.error("Exception", e);
					}
				}
			}

			String reqMode = modeMapping != null
					&& modeMapping.containsKey(requestBean.getMode()) ? modeMapping
					.get(requestBean.getMode())
					: requestBean.getMode();
			String reqType = requestBean.getRequestType();
			if ((reqType != null && reqType.startsWith("ACT"))
					|| comboTransID != null) {
				if (comboTransID != null && isSeparatePriceAndValidityForComboEnabled) {
					reqType = "Subscription|SongDownload";
				} else {
					reqType = "Subscription";
				}
			} else {
				reqType = "SongDownload";
			}
			long initQueryWDSMillis = System.currentTimeMillis();
			String wdsResponse = queryWDS(subscriberID);
			long endQueryWDSMillis = System.currentTimeMillis();
			timeTakenToQueryWDS = endQueryWDSMillis - initQueryWDSMillis;

			String wdsHttpLink = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "WDS_HTTP_LINK", null);
			if (wdsResponse == null && wdsHttpLink != null)
				return;
			String networkID = null;
			String circleId = null;
			String tokens[] = null; 
			if(wdsResponse != null) {
				tokens = wdsResponse.split("\\|");
			}

			if (tokens != null && tokens.length > 9) {
				networkID = tokens[7];
				circleId = Utility.getMappedCircleID(tokens[8]);
			}
			
			if(wdsHttpLink == null) {
				circleId = subscriber.getCircleID();
			}
			logger.debug("networkID === " + networkID + " circleId == "
					+ circleId);

			
			consentUrl = consentUrl.replaceAll("%MSISDN%", requestBean
					.getSubscriberID());
			consentUrl = consentUrl.replaceAll("%MODE%", reqMode);
			consentUrl = consentUrl.replaceAll("%TRANS_ID%", requestBean
					.getTransId());
			consentUrl = consentUrl.replaceAll("%CIRCLE_ID%", circleId);
			consentUrl = consentUrl.replaceAll("%NETWORK_ID%", networkID);
			consentUrl = consentUrl.replaceAll("%REQ_TYPE%", reqType);
			consentUrl = consentUrl.replaceAll("%PRICE%", pPrice + "");
			consentUrl = consentUrl.replaceAll("%VALIDITY%", pVal + "");
			String serviceId = getServiceValue("SERVICE_ID", subClass,
					classType, subscriber.getCircleID(), isActRequest,
					isSelRequest, isActAndSelRequest);
			if(serviceId  != null){
			consentUrl = consentUrl.replaceAll("%serviceid%", serviceId);
			}
			//CG Integration Flow - Jira -12806
			if (checkCGFlowForBSNL) {
				if (!isActAndSelRequest) {
					if (requestBean.getInLoop() != null) {
						if (requestBean.getInLoop().equalsIgnoreCase("l")) {
							param1 = "2";
						} else if (requestBean.getInLoop().equalsIgnoreCase("o")) {
							param1 = "1";
						}
					}
				}
				
				logger.debug("param1 == " + param1 + " contented == " + clipId
						+ "serviceid == " + serviceId + " Requesttimestamp == "
						+ requestBean.getRequestTime()+" clipName == "+clipName);
				if (null == serviceId) {
					serviceId = "";
					logger.debug("serviceid is null replacing empty string"
							+ serviceId);
				}
				String RequestTime = RequestTimeDateFormat.format(requestBean
						.getRequestTime());
				consentUrl = consentUrl.replaceAll("%Requesttimestamp%",
						RequestTime);
				consentUrl = consentUrl.replaceAll("%param1%", param1);
				if (isActRequest && !isActAndSelRequest) {
					consentUrl = consentUrl.replaceAll("%songname%", "NA");
					consentUrl = consentUrl.replaceAll("%contented%", "NA");
				} else {
					if (null == clipName) {
						consentUrl = consentUrl.replaceAll("%songname%", "");
					} else {
						consentUrl = consentUrl.replaceAll("%songname%",
								clipName.replace(" ", "+"));

					}
					if (null == clipId) {
						consentUrl = consentUrl.replaceAll("%contented%", "");
					} else {
						consentUrl = consentUrl.replaceAll("%contented%",
								clipId);

					}
				}
			}

			if(getBParty(requestBean)!=null){
			    consentUrl = consentUrl.replaceAll("%BPARTY%", getBParty(requestBean));
			}
			
			if(vcode!=null){
			    consentUrl = consentUrl.replaceAll("%VCODE%", vcode);
			}
			// CG Integration Flow - Jira -12806
			if (!checkCGFlowForBSNL) {
				if (clipName != null) {
					if (consentUrl.contains("%SONGNAME%")) {
						consentUrl = consentUrl.replaceAll("%SONGNAME%",
								clipName.replace(" ", "+"));
					} else {
						consentUrl += "&Songname=" + clipName.replace(" ", "+");
					}
				} else {
					if (consentUrl.contains("%SONGNAME%")) {
						consentUrl = consentUrl.replaceAll("%SONGNAME%",
								"NULL");
					}
				}
			}
			
			language = com.onmobile.apps.ringbacktones.webservice.common.Utility.getLanguageCode( requestBean.getLanguage() );
			
			consentUrl = consentUrl.replaceAll( "%LANGUAGE_ID%", language );
			
			clipId = getContentId(requestBean);
			if(dbConfirmReqBean != null){
				clipId = getContentId(dbConfirmReqBean);
			}
			
			if (null == clipId) {
				consentUrl = consentUrl.replaceAll("%contented%", "");
			} else {
				consentUrl = consentUrl.replaceAll("%contented%",
						clipId);

			}
			
			
			opt1 = com.onmobile.apps.ringbacktones.webservice.common.Utility.getPlanId( subClass );
			
			consentUrl = consentUrl.replaceAll( "%EVENT_TYPE%", eventType );
			consentUrl = consentUrl.replaceAll( "%PLAN_ID%", opt1 );
			
			List<DoubleConfirmationRequestBean> dbBeanList = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							null, requestBean.getTransId(), requestBean.getSubscriberID(), null, true);
			if (dbBeanList.get(0).getConsentStatus() != 0)
				return;
			
			
			
			logger.info("Consent url: " + consentUrl);
			long initConsentPushMillis = System.currentTimeMillis();
			String response = DoubleConfirmationHttpUtils
					.getResponse(consentUrl);
			long endConsentPushMillis = System.currentTimeMillis();
			timeTakenToHITConsent = endConsentPushMillis
					- initConsentPushMillis;

			logger
					.info("Consent url: " + consentUrl + " response: "
							+ response);
			String[] responses = response.split("\\-");
			String urlResponse = null;
			int responseCode = 0;
			
			try{
				responseCode = Integer.parseInt(responses[0]);
			}
			catch(Exception e) {
				urlResponse = responses[0];
			}
			
		
			
			//CG Integration Flow - Jira -12806
			if (response != null && (responseCode == 200 || responseCode == 534 ||(urlResponse != null && (urlResponse.equalsIgnoreCase("SUCCESS") || urlResponse.contains("ACCEPTED"))))) {
				String transId = requestBean.getTransId();
				try {
						
					
					String statusToBeUpdated = "1";
					boolean success = RBTDBManager.getInstance()
							.updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),
									transId, statusToBeUpdated, "0");
					
				} catch (OnMobileException e) {
					logger.error("Exception", e);
				}
			}else {
				if (xtraInfoMap == null)
					xtraInfoMap = new HashMap<String, String>();
				int noOfTrials = 1;
				if (noOfTrialsStr != null) {
					noOfTrials = Integer.parseInt(xtraInfoMap
							.get("NO_OF_TRIALS")) + 1;
				}
				xtraInfoMap.put("NO_OF_TRIALS", noOfTrials + "");
				String xtraInfo = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
				try {
					RBTDBManager.getInstance().updateConsentExtrInfo(
							requestBean.getSubscriberID(), requestBean.getTransId(), xtraInfo, null);
				} catch (OnMobileException e) {
					logger.error("Exception", e);
				}
			}
			consentPushLogger.info("QUERY_WDS_TIME = " + timeTakenToQueryWDS
					+ " ,CONSENT_HIT_RESPONSE_TIME = " + timeTakenToHITConsent
					+ " ,CONSENT_URL = " + consentUrl);

		} catch (Exception ex) {
			ex.printStackTrace();
			// RBT-18793
			logger.info("Exception while hitting Consent url..."+ex,ex);
		}
	}

	// Post http request is used in vodafone
	public void hitConsentURLVodafone(DoubleConfirmationRequestBean requestBean)
	{
		try
		{
			if(requestBean == null) {
				return;
			}
			
			logger.info("processing pending record="+requestBean);
			if(hasNumberOfTrialsExceededThreshhold(requestBean))
			{
				logger.info("Deleting this pending consent record as trial count exceeded..temporarily disabled, updating consent status to 5");
				ConsentDaemonTransLogger.log("MAX_RETRY", requestBean);
				//RBTDBManager.getInstance().deleteConsentRequestByTransId(requestBean.getTransId());
				//deleteCombinedSelRequest(requestBean);
				boolean success = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),requestBean.getTransId(), "5");
				updateCombinedSelRequest(requestBean, "5");
				return;
			}
			
			logger.info("Mode: " + requestBean.getMode());
			String mode = requestBean.getMode();
			if (externalToInternalModeMapping != null && externalToInternalModeMapping.containsKey(requestBean.getMode())) { 
				mode = externalToInternalModeMapping.get(requestBean.getMode());
				logger.info("Mode: " + mode);
			}
			
			if (modesForNotToGetConsentList != null && modesForNotToGetConsentList.contains(mode))
			{
				logger.info("mode "+mode + " in modesForNotToGetConsentList, skipping record and updating conset status to 5");
				boolean success = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),requestBean.getTransId(), "5");
				updateCombinedSelRequest(requestBean, "5");
				return;
			}
			
			String subscriberID = requestBean.getSubscriberID();
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
			Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
	
			boolean isActAndSelRequest = false;
			boolean isSelRequest = false;
			boolean isActRequest = false;
			String entityType = null;
			DoubleConfirmationRequestBean dbConfirmReqBean = null;
			String chargeClass = requestBean.getClassType();
			String subClass = requestBean.getSubscriptionClass();
			
			if(requestBean.getRequestType().equalsIgnoreCase("ACT") && 
					subscriber != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isUserActive(subscriber.getStatus()))
			{
				logger.info("Not processong base consent request for "+subscriberID + " as subscriber is already active");
				ConsentDaemonTransLogger.log("ALREADY_ACTIVE", requestBean);
				RBTDBManager.getInstance().deleteConsentRequestByTransIdAndMSISDN(requestBean.getTransId(), requestBean.getSubscriberID());
				return;
			}	
			
			if(requestBean.getRequestType().equalsIgnoreCase("SEL") && 
					(subscriber == null || !com.onmobile.apps.ringbacktones.webservice.common.Utility.isUserActive(subscriber.getStatus())))
			{
				logger.info("Not processong sel consent request for "+subscriberID + " as subscriber is not active");
				ConsentDaemonTransLogger.log("INACTIVE", requestBean);
				RBTDBManager.getInstance().deleteConsentRequestByTransIdAndMSISDN(requestBean.getTransId(), requestBean.getSubscriberID());
				return;
			}
			if(requestBean.getRequestType().equalsIgnoreCase("ACT") || requestBean.getRequestType().equalsIgnoreCase("UPGRADE"))
			{
				isActRequest = true;
				entityType = ConsentUrlHitLogger.ENTITY_BASE;
			}
			if(requestBean.getRequestType().equalsIgnoreCase("SEL"))
			{
				isSelRequest = true;
				entityType = ConsentUrlHitLogger.ENTITY_CONTENT;
				subClass = subscriber.getSubscriptionClass();
			}
			
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(requestBean.getExtraInfo());
			if (requestBean.getRequestType().equalsIgnoreCase("ACT") || requestBean.getRequestType().equalsIgnoreCase("UPGRADE"))
			{
				if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID"))
				{
					String selTrasId = extraInfoMap.get("TRANS_ID");
					List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager.getInstance().getDoubleConfirmationRequestBeanForStatus(null, selTrasId, subscriberID, null, true);
					if(doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0)
					{
						isActAndSelRequest = true;
						entityType = ConsentUrlHitLogger.ENTITY_COMBO_BASE;
						dbConfirmReqBean = doubleConfirmReqBeans.get(0);
						logger.info("combo sel bean found = "+dbConfirmReqBean);
						chargeClass = dbConfirmReqBean.getClassType();
						logger.info("Combo Charge class = "+chargeClass);
					}
				}
			}
			//Restructure the logic for the below CG URL Construction.
			
			/*
			 * http://TPCGIP:TPCGPORT/API/TPCG?
			 * MSISDN=<91XXXXXXXXXX>&Service=<string>&Class=<string>&mode=<string>&requestid=<string>&reqesttime<YYYY-MM-DD HH24:MI:SS.0>
			 * &CircleId=<XX>&Loginid=<string>&password=<string> &from=<string>&param1=<string>&param2=<string>&param3=<string>
			 * 
			 * 
			 */
			String circleID = requestBean.getCircleId();
			String consentUrl = null;
			String paramName = mode;
			String requestType = "GET";
			paramName = paramName.toUpperCase();
			Parameters param = CacheManagerUtil.getParametersCacheManager()
					.getParameter(iRBTConstant.DOUBLE_CONFIRMATION, paramName);
			if (param != null) {
				consentUrl = param.getValue();
				logger.info("CG Url for Mode: " + consentUrl + ", paramName: "
						+ paramName);
				requestType = (consentUrl.split(","))[1];
				consentUrl = (consentUrl.split(","))[0];
			}
			if (circleID == null) {
				circleID = subscriber.getCircleID();
			}
			if (circleID != null && !circleID.isEmpty()) {
				paramName = mode + "_" + circleID;
				paramName = paramName.toUpperCase();
			}
			param = CacheManagerUtil.getParametersCacheManager().getParameter(
					iRBTConstant.DOUBLE_CONFIRMATION, paramName);
			if (param != null) {
				consentUrl = param.getValue();
				logger.info("CG Url for Mode+Circle: " + consentUrl
						+ ", paramName: " + paramName);
				requestType = (consentUrl.split(","))[1];
				consentUrl = (consentUrl.split(","))[0];
			}
			if (consentUrl == null) {
				logger.info("Consent url not configure, skipping record..and updating consent status to 5");
				boolean success = RBTDBManager.getInstance()
						.updateConsentStatusOfConsentRecord(
								requestBean.getSubscriberID(),
								requestBean.getTransId(), "5");
				updateCombinedSelRequest(requestBean, "5");
				return;
			} else
				logger.info("Consent url=" + consentUrl);
			HashMap<String, String> requestParams = new HashMap<String, String>();

			consentUrl = consentUrl.replace("<MSISDN>", countryPrefix + subscriberID);
			consentUrl = consentUrl.replace("<CIRCLE_ID>",
					Utility.getMappedCircleIDCached(requestBean.getCircleId()));
			consentUrl = consentUrl.replace(
					"<SERVICE>",
					getServiceValue("SERVICE_ID", subClass, chargeClass,
							subscriber.getCircleID(), isActRequest,
							isSelRequest, isActAndSelRequest));
			consentUrl = consentUrl.replace(
					"<CLASS>",
					getServiceValue("SERVICE_CLASS", subClass, chargeClass,
							subscriber.getCircleID(), isActRequest,
							isSelRequest, isActAndSelRequest));
			consentUrl = consentUrl.replace("<MODE>", mode);
			consentUrl = consentUrl.replace("<REQUEST_ID>", requestBean.getTransId());
			consentUrl = consentUrl.replace("<PARTNER_ID>", "PIBM1160");
			consentUrl = consentUrl.replace("<ACTION>", "ACT");
			consentUrl = consentUrl.replace("<USSD_STRING>", "*567#");
			consentUrl = consentUrl.replace("<SHORT_CODE>", "55677");
			consentUrl = consentUrl.replace("<KEY_WORD>", "CT");
			consentUrl = consentUrl.replace("<FROM>", modeAndFromParamMap.get(mode));
			if (requestType != null && requestType.trim().equalsIgnoreCase("GET")) {
				/*
				 * "https://cgussd.vodafone.in/VodafoneConfirmation/RequestConfirmation?
				 * Circle-id=10 MSISDN=919886642901 Service=CRBT Class=CRBT
				 * Mode=USSD Requestid=$REQID Loginid=ONMOBILEIBM7474
				 * password=6131IBM5143 partner_id=PIBM1160 Action=ACT
				 * Timestamp=2013-07-08 11:39:32 ussdString=*567*856#
				 * From=VENDOR
				 */
				consentUrl = consentUrl
						.replace("<TIME_STAMP>", ussdDateFormat.format(Calendar
								.getInstance().getTime()));
				logger.info("Final Consent url= " + consentUrl);
			} else {
				/*
				 * "https://cgussd.vodafone.in/VodafoneConfirmation/RequestConfirmation?
				 * CircleId=10 MSISDN=919886642901 Service=CRBT Class=CRBT
				 * mode=USSD requestid=$REQID Loginid=ONMOBILEIBM7474
				 * password=6131IBM5143 Action=ACT requesttime=2013-07-08
				 * 11:39:32 ShortCode=55677 keyword = CT from=VENDOR
				 */
				consentUrl = consentUrl.replace("<TIME_STAMP>", localDateFormat
						.format(Calendar.getInstance().getTime()));
				logger.info("After replaced the values in the Consent url= "
						+ consentUrl);
				String requestParmatersArray[] = consentUrl.split("&");
				if (requestParmatersArray != null) {
					int i = 0;
					while (i <= (requestParmatersArray.length-1)) {
						if (i == 0) {
							consentUrl = (requestParmatersArray[i].split("\\?"))[0];
							requestParmatersArray[i] = (requestParmatersArray[i]
									.split("\\?"))[1];
						}
						String keyValues[] = requestParmatersArray[i]
								.split("=");
						if (keyValues != null && keyValues.length > 0) {
							requestParams.put(keyValues[0], keyValues[1]);
						}
						i++;
					}
					logger.info("Final Consent url: " + consentUrl
							+ ", params=" + requestParams);
				}
			}
			logger.info("Consent url: " + consentUrl+", params="+requestParams);
			HttpResponse httpResponse = DoubleConfirmationHttpUtils.getResponse(consentUrl, requestParams, mode, entityType, requestType);
			
			if(httpResponse == null)
			{
				logger.info("httpResponse object is null...updating the retry count");
				/*boolean success = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),requestBean.getTransId(), "5");
				updateCombinedSelRequest(requestBean, "5");*/
				updateNumberOfTrials(requestBean);
				return;
			}	
			logger.info("Consent url response="+httpResponse.getResponse() + (httpResponse.getResponse() != null ? httpResponse.getResponse().length()+"" : "no response string") + ", code="+httpResponse.getResponseCode());
				
			boolean isHitSuccessful = false;
			if(httpResponse != null)
			{
				logger.info("mode="+mode+", length="+mode.length());
				logger.info("modesForGetHttp="+modesForGetHttp);
				logger.info("transId="+requestBean.getTransId()+", length="+requestBean.getTransId().length());
				logger.info("response="+httpResponse.getResponse()+", length="+httpResponse.getResponse().length());
				logger.info("condition="+httpResponse.getResponse().indexOf(requestBean.getTransId()+",0,0"));
				if(requestType != null && requestType.equalsIgnoreCase("GET"))
				{
					logger.info("Checking get modes");
					if(httpResponse.getResponse() != null && httpResponse.getResponse().indexOf(requestBean.getTransId()+",0,0") != -1)
						isHitSuccessful = true;
				}
				else if (httpResponse.getResponseCode() == 200 && httpResponse.getResponse() != null && httpResponse.getResponse().indexOf("CGW200") != -1){
					isHitSuccessful = true;
				}
				else { //This is for retry logic for configured modes 
					String reponse = httpResponse.getResponse();
					boolean enableRetry = false;
					if (null != allowededRetryReponseCodes
							&& !allowededRetryReponseCodes.isEmpty()) {
						for (String responseCode : allowededRetryReponseCodes) {
							if (null != reponse
									&& reponse.indexOf(responseCode) != -1) {
								enableRetry = true;
								break;
							}
						}
						if (!enableRetry) {
							logger.info("httpResponse object has given the response code which is not allowed for retry...so updating the status code to 5");
							boolean success = RBTDBManager.getInstance()
									.updateConsentStatusOfConsentRecord(
											requestBean.getSubscriberID(),
											requestBean.getTransId(), "5");
							updateCombinedSelRequest(requestBean, "5");
							return;
						}
					}
				}
			}
			if(isHitSuccessful == true)
			{
				String transId = requestBean.getTransId();
				try
				{
					String statusToBeUpdated = "1";
					boolean success = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),transId, statusToBeUpdated);
					logger.info("updated bean with success state="+success);
					if(dbConfirmReqBean != null)
					{
						RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(dbConfirmReqBean.getSubscriberID(),dbConfirmReqBean.getTransId(), "1");
						logger.info("updated combined sel bean");
					}
				}
				catch (OnMobileException e)
				{
					logger.error("Exception", e);
				}
			}
			else {
				logger.info("httpResponse object has given the response code which is allowed for retry/Conf is not there...so updating the retry count");
				updateNumberOfTrials(requestBean);
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception", e);
		}
	}
	
	
	private void updateCombinedSelRequest(DoubleConfirmationRequestBean requestBean, String consentStatus)
	{
		if(requestBean == null)
			return;
		if(!requestBean.getRequestType().equalsIgnoreCase("ACT"))
			return;
		
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(requestBean.getExtraInfo());
		if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID"))
		{
			String selTrasId = extraInfoMap.get("TRANS_ID");
			List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							null, selTrasId, requestBean.getSubscriberID(),
							null, true);
			if(doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0)
			{
				DoubleConfirmationRequestBean dbConfirmReqBean = doubleConfirmReqBeans.get(0);
				logger.info("deleting combo sel bean found = "+dbConfirmReqBean);
				try
				{
					RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(
									dbConfirmReqBean.getSubscriberID(),dbConfirmReqBean.getTransId(), "3");
				}
				catch (OnMobileException e)
				{
					logger.error("Exception", e);
				}
			}
		}
	}

	private void deleteCombinedSelRequest(DoubleConfirmationRequestBean requestBean)
	{
		if(requestBean == null)
			return;
		if(!requestBean.getRequestType().equalsIgnoreCase("ACT"))
			return;
		
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(requestBean.getExtraInfo());
		if(extraInfoMap != null && extraInfoMap.containsKey("TRANS_ID"))
		{
			String selTrasId = extraInfoMap.get("TRANS_ID");
			List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = RBTDBManager
					.getInstance().getDoubleConfirmationRequestBeanForStatus(
							null, selTrasId, requestBean.getSubscriberID(),
							null, true);
			if(doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0)
			{
				DoubleConfirmationRequestBean dbConfirmReqBean = doubleConfirmReqBeans.get(0);
				logger.info("deleting combo sel bean found = "+dbConfirmReqBean);
				RBTDBManager.getInstance().deleteConsentRequestByTransIdAndMSISDN(
								dbConfirmReqBean.getTransId(),dbConfirmReqBean.getSubscriberID());
			}
		}
	}

	public static String getServiceValue(String serviceType, String subClass, String chargeClass, String circleId, boolean isActRequest, 
			boolean isSelRequest, boolean isActAndSelRequest)
	{
		logger.info("Entering with serviceType="+serviceType+", subClass="+subClass+", chargeClass="+chargeClass+", circleId="+circleId
				+", isActRequest="+isActRequest+", isSelRequest="+isSelRequest+", isActAndSelRequest="+isActAndSelRequest);
		RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "MODE_AND_FROM_PARAM_MAP", null);
		String serviceValues = getServiceValues(serviceType, subClass, chargeClass, circleId, isActRequest, isSelRequest, isActAndSelRequest);
		if(serviceValues == null && !circleId.equals("ALL"))
			serviceValues = getServiceValues(serviceType, subClass, chargeClass, "ALL", isActRequest, isSelRequest, isActAndSelRequest);
		
		if(serviceValues != null)
		{
			String[] tokens = serviceValues.split(":");
			if(tokens.length == 2)
			{
				if(serviceType.equals("SERVICE_ID"))
				{
					logger.info("service id found="+ tokens[0]);
					return tokens[0];
				}
				else
				{
					logger.info("service class found="+ tokens[1]);
					return tokens[1]; // returning service class
				}
			}
		}
		return null;
	}
	
	public static String getServiceValues(String serviceType, String subClass, String chargeClass, String circleId, boolean isActRequest, 
			boolean isSelRequest, boolean isActAndSelRequest)
	{
		logger.info("Entering with serviceType="+serviceType+", subClass="+subClass+", chargeClass="+chargeClass+", circleId="+circleId
				+", isSelRequest="+isSelRequest+", isActAndSelRequest="+isActAndSelRequest);
		RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "MODE_AND_FROM_PARAM_MAP", null);
		String serviceValues = null;
		if(isActAndSelRequest)
			serviceValues =  RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "ACTSEL_"+subClass+"_"+chargeClass+"_"+circleId, null);
		else if (isActRequest)
			serviceValues = RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "ACT_"+subClass+"_"+circleId, null);
		else if(subClass!=null) // Start: for selection request chargeClass is coming as null some vodafone .Jira id : -RBT-12312
			serviceValues = RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "SEL_"+subClass+"_"+chargeClass+"_"+circleId, null);
		else
			serviceValues = RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, "SEL_"+chargeClass+"_"+circleId, null);
		// End: for selection request chargeClass is coming as null some vodafone .Jira id : -RBT-12312
		return serviceValues;
	}
	
	private void updateNumberOfTrials(DoubleConfirmationRequestBean requestBean)
	{
		try
		{
			logger.info("updating num of trials");
			String extraInfo = requestBean.getExtraInfo();
			HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			String noOfTrialsStr = null;
			
			if (xtraInfoMap == null)
				xtraInfoMap = new HashMap<String, String>();
			if (!xtraInfoMap.containsKey("NO_OF_TRIALS"))
				xtraInfoMap.put("NO_OF_TRIALS", "1");
			else
			{
				noOfTrialsStr = xtraInfoMap.get("NO_OF_TRIALS");
				int currNumOfTrials = Integer.parseInt(noOfTrialsStr);
				xtraInfoMap.put("NO_OF_TRIALS", String.valueOf(++currNumOfTrials));
			}
			RBTDBManager.getInstance().updateConsentExtrInfo(
					requestBean.getSubscriberID(), requestBean.getTransId(),
					DBUtility.getAttributeXMLFromMap(xtraInfoMap), null);
		}
		catch (OnMobileException e)
		{
			logger.error("Exception", e);
		}
	}

	private boolean hasNumberOfTrialsExceededThreshhold(DoubleConfirmationRequestBean requestBean)
	{
		String extraInfo = requestBean.getExtraInfo();
		HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		String noOfTrialsStr = null;
		if (xtraInfoMap != null && xtraInfoMap.containsKey("NO_OF_TRIALS"))
		{
			noOfTrialsStr = xtraInfoMap.get("NO_OF_TRIALS");
			try
			{
				if (Integer.parseInt(noOfTrialsStr) >= maxNoOfRetrials)
				{
					logger.debug("Retry exceed noOfTrialsStr: " + noOfTrialsStr + " maxNoOfRetrials: " + maxNoOfRetrials+ "" +
							" subscriberId: " + requestBean.getSubscriberID());
					//RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(requestBean.getTransId(), "3");
					return true;
				}
			}
			catch (Exception e)
			{
				logger.error("Exception", e);
			}
		}
		return false;
	}

	public static void initializeMap(Map<String, String> map, String paramValue) {
		if (paramValue == null)
			return;
		StringTokenizer stk = new StringTokenizer(paramValue, ",");
		while (stk.hasMoreTokens()) {
			String ss[] = stk.nextToken().split(":");
			if (ss.length == 2)
				map.put(ss[0], ss[1]);
		}
	}

	protected String queryWDS(String subscriberID) {
		String wdsResult = null;
		try {
			String wdsHttpQuery = null;
			Parameters param = CacheManagerUtil.getParametersCacheManager()
					.getParameter(iRBTConstant.COMMON, "WDS_HTTP_LINK");
			if(param == null)
				return null;
			wdsHttpQuery = param.getValue().trim();
			logger.info("query is " + wdsHttpQuery);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("MDN", subscriberID);
			HttpParameters httpParameters = new HttpParameters(wdsHttpQuery);
			wdsResult = RBTHTTPProcessing
					.postFile(httpParameters, params, null);
			logger.info("result for " + subscriberID + "is " + wdsResult);
		} catch (Exception e) {
			logger.error("", e);
			wdsResult = null;
		}
		// result = "9030055076|2|PREPAID|2|1|013DE58A|-|GCMO|AP|SLEE02||";
		return wdsResult;
	}

	public static int getPrice(Map<String, String> map, String str) {
		if (map == null)
			return 0;
		String priceAndValidity = map.get(str);
		String price = null;
		if (priceAndValidity != null) {
			String ss[] = priceAndValidity.split(",");
			price = ss[0];
		}
		if (price != null) {
			try {
				return Integer.parseInt(price);
			} catch (Exception ex) {
				logger.error("Exception", ex);
				return -1;
			}
		} else
			return 0;
	}

	public static int getValidityPeriod(Map<String, String> map, String str) {
		if (map == null)
			return -1;
		String priceAndValidity = map.get(str);
		String validity = null;
		if (priceAndValidity != null) {
			String ss[] = priceAndValidity.split(",");
			validity = ss[1];
		}

		if (validity != null) {
			try {
				return Integer.parseInt(validity);
			} catch (Exception ex) {
				logger.error("Exception while getting Validity Period in Double consent Push", ex);
				return -1;
			}
		} else
			return -1;

	}
	
	protected String getClipName(DoubleConfirmationRequestBean dbReqBean) {
		if (dbReqBean == null)
			return null;
		String clipName = null;
		Clip clip = null;
		if (dbReqBean.getClipID() != null && dbReqBean.getClipID() != -1) {
			clip = RBTCacheManager.getInstance().getClip(dbReqBean.getClipID());
		}
		if (clip == null && dbReqBean.getWavFileName() != null) {
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					dbReqBean.getWavFileName());
		}
		if (clip != null)
			clipName = clip.getClipName();
		logger.debug("clipNameByWavFile= " + clipName);
		return clipName;
	}
	
	//CG Integration Flow - Jira -12806
	protected String getContentId(DoubleConfirmationRequestBean dbReqBean) {
		if (dbReqBean == null)
			return null;
		String clipId = null;
		Clip clip = null;
		if (dbReqBean.getClipID() != null && dbReqBean.getClipID() != -1) {
			clip = RBTCacheManager.getInstance().getClip(dbReqBean.getClipID());
		}
		if (clip == null && dbReqBean.getWavFileName() != null) {
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					dbReqBean.getWavFileName());
		}
		if (clip != null)
			clipId = String.valueOf(clip.getClipId());

		return clipId;
	}
	
	
	protected String getVcode(DoubleConfirmationRequestBean dbReqBean){
		if(dbReqBean == null)
			return null;
		String vcode = null;
		String wavFile = null;
		if(dbReqBean.getClipID()!=null && dbReqBean.getClipID()!=-1){
			Clip clip = RBTCacheManager.getInstance().getClip(dbReqBean.getClipID());
			if(clip!=null){
				wavFile = clip.getClipRbtWavFile();
			}
		}
		if(wavFile == null){
			wavFile = dbReqBean.getWavFileName();
		}
		if(wavFile!=null){
			vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
		}
		return vcode;
	}
	
	protected String getBParty(DoubleConfirmationRequestBean dbReqBean) {
		String bparty = null;
		String selInfo = dbReqBean.getSelectionInfo();
		if (selInfo != null && selInfo.indexOf("CP")!=-1) {
			String str[] = selInfo.split("\\|");
			for (int i = 0; i < str.length; i++) {
				String ss = str[i];
				if (ss.indexOf("CP") != -1) {
					ss = ss.replaceAll("CP:", "").replaceAll(":CP", "");
					String str1[] = ss != null ? ss.split("-") : null;
					if (str1 != null && str1.length == 2) {
						bparty = str1[1];
					}
				}
			}
		}
		return bparty;
	}
	
	private void processWithoutHittingConsent(DoubleConfirmationRequestBean dbReqBean) {
		String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("TEXT_FOR_CONSENT",
				null);
		try {
			String senderNo = RBTParametersUtils.getParamAsString("DOUBLE_CONFIRMATION",
					"AOC_SENDER_NO", null);
			Tools.sendSMS(senderNo, dbReqBean.getSubscriberID(), smsText, false);
			boolean success = RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(
					dbReqBean.getSubscriberID(), dbReqBean.getTransId(), "1");
			logger.info("processWithoutHittingConsent result = "+success);
		} catch (OnMobileException e) {
			e.printStackTrace();
		}
	}

}
