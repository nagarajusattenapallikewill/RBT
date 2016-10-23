package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationContentProcessUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class DoubleConfirmationCallbackServlet extends HttpServlet {


	private static final long serialVersionUID = -3658436991765738658L;
	static Logger logger = Logger.getLogger(DoubleConfirmationCallbackServlet.class);
	private static String statusAfterConsentSuccess = "2";
	private static String statusAfterConsentFailure = "3";
	private static String loggerName = "CALLBACK.RECEIVED";
	private static Logger consentCallbackLogger = RbtLogger
			.createRollingFileLogger(RbtLogger.consentTransactionPrefix
				+ loggerName, ROLLING_FREQUENCY.HOURLY);
//	private static Logger consentCallbackLogger = logger;
    private static Map<String,String> htIntegrationBaseConfMap = null;
    private static Map<String,String> htIntegrationSelConfMap = null;
	private static Map<String, String> externalToInternalModeMapping = null;
	private static String deactivationMode = null;
	private static boolean isUseProxy = false;
	private static String proxyHostname = null;
	private static int proxyPort = 80;
	private static int connectionTimeout = 6000;
	//private static int connectionSoTimeout = 6000;
	private static String strCircleIdMap =null;
	private static Map<String,String> circleIdMap = null;
	private static RBTDBManager rbtDbMgr = null;
	
	static{
		
		
		try {
			//Initializing PromoProcessor so that It internally initializes Processor class
			//to use redirection logic
			new PromoProcessor();
			isUseProxy = RBTParametersUtils.getParamAsBoolean("DAEMON",
					"CONSENT_EXPIRED_URL_IS_USE_PROXY", "false");
			proxyHostname = RBTParametersUtils.getParamAsString("DAEMON",
					"CONSENT_EXPIRED_URL_PROXY_HOST", null);
			proxyPort = RBTParametersUtils.getParamAsInt("DAEMON", "CONSENT_URL_PROXY_PORT",
					80);
			connectionTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
					"CONSENT_EXPIRED_URL_CONNECTION_TIMEOUT", 6000);
			/*connectionSoTimeout = RBTParametersUtils.getParamAsInt("DAEMON",
					"CONSENT_EXPIRED_URL_SO_CONNECTION_TIMEOUT", 6000);*/
			String internalToExternalModeMappingStr = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "INTERNAL_EXTERNAL_MODES_MAP", null);
			logger.info("internalToExternalModeMappingStr="+internalToExternalModeMappingStr);
			externalToInternalModeMapping = MapUtils.convertToMap(internalToExternalModeMappingStr, ";", "=", ",");
			logger.info("externalToInternalModeMapping="+externalToInternalModeMapping);
			deactivationMode = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "CONSENT_FAILURE_DEACTIVATION_MODE", null);
			logger.info("CONSENT_FAILURE_DEACTIVATION_MODE: "
					+ deactivationMode);
			strCircleIdMap = RBTParametersUtils.getParamAsString("COMMON",
					"CIRCLEID_MAPPING_FOR_THIRD_PARTY", null);
			circleIdMap = MapUtils.convertToMap(strCircleIdMap, ";", ":", null);
			logger.info("circleIdMap: " + circleIdMap);
			getHtConfigMap();
			rbtDbMgr = RBTDBManager.getInstance();
		} catch (RBTException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException, IOException {
		
		logger.info("Inside Get of DoubleServlet****"+httpRequest.getRequestURL()+"*******"+httpRequest.getQueryString()+"***");
		String response = "FAILURE";
		// For the redirection Concept IN Callback
		HashMap<String, String> requestParamsMap = Utility.getRequestParamsMap(
				getServletConfig(), httpRequest, httpResponse, Constants.api_consentCallback);
        
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParamsMap);
		Task task = new Task(null, taskSession);
		task.setObject(Constants.param_URL, "consentCallback.do");
		//CG Integration Flow - Jira -12806
		boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.DOUBLE_CONFIRMATION,
				"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
		String redirectionURL = Processor.getRedirectionURL(task);
		
		
		
		//CG Integration Flow - Jira -12806
		String transId = null;
		int responseCode = HttpServletResponse.SC_OK;
		String failureResponseValues = RBTParametersUtils.getParamAsString(
				iRBTConstant.DOUBLE_CONFIRMATION,iRBTConstant.AIRTEL_CG_FAILED_RESPONSES, "FAILURE_NO_CONSENT");
		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse1 = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParamsMap);
				logger.info("RBT:: httpResponse: " + httpResponse1);

				response = httpResponse1.getResponse();
			} catch (Exception e) {
				response = WebServiceConstants.TECHNICAL_DIFFICULTIES;
				responseCode = HttpServletResponse.SC_BAD_REQUEST;
				logger.error("RBT:: " + e.getMessage(), e);
			}
		} else {//CG Integration Flow - Jira -12806
			String result = null, msisdn = null, tpcgID = null, clipID = null, consentmode = null, circleId = null, vas_id = null, error_code = null, error_desc = null, 
					consent_status = null, consent_time = null, opt1 = null, opt2 = null, opt3 = null;
			
			//here
			BasicRequestHandler requestHandler = ComvivaFactoryObject.getRequestInstance();
			Map<String,String>requestParams = requestHandler.populateRequestParameters( httpRequest, checkCGFlowForBSNL );
			
			result = requestParams.get( "RESULT" );
			transId = requestParams.get( "TRANSACTION_ID" );
			tpcgID = requestParams.get( "CG_ID" );
			clipID = requestParams.get( "SONG_ID" );
			consentmode = requestParams.get( "CONSENT_MODE" );
			msisdn = requestParams.get( "MSISDN" );
			circleId = requestParams.get( "CIRCLE_ID" );
			
			vas_id = requestParams.get( "VAS_ID" );
			error_code = requestParams.get( "ERROR_CODE" );
			error_desc = requestParams.get( "ERROR_DESC" );
			consent_status = requestParams.get( "CONSENT_STATUS" );
			consent_time = requestParams.get( "CONSENT_TIME" );
			opt1 = requestParams.get( "OPTION_1" );
			opt2 = requestParams.get( "OPTION_2" );
			opt3 = requestParams.get( "OPTION_3" );
			
			if (result != null)
				result = result.toLowerCase();
			String minMaxLenght = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "MIN_MAX_LENGTH_TPCGID", "1,40");
			if (minMaxLenght != null && result != null
					&& result.equalsIgnoreCase("success")) {
				String  []strArrays = minMaxLenght.split(",");
				int minLength = Integer.parseInt(strArrays[0]);
				int maxLength = Integer.parseInt(strArrays[1]);
				if (tpcgID == null
						|| !((tpcgID = tpcgID.trim()).length() >= minLength && tpcgID
								.length() <= maxLength)) {
					response = CacheManagerUtil.getParametersCacheManager()
							.getParameterValue("DOUBLE_CONFIRMATION",
									"TPCGID_INVALID", "TPCGID_INVALID");
					if (checkCGFlowForBSNL) {//CG Integration Flow - Jira -12806
						response = "533|" + response + "|" + transId;
					}
					httpResponse.getWriter().write(response);
					
					return;
				}
			}
			// //CG Integration Flow - Jira -12806
			DoubleConfirmationContentProcessUtils processRequest = null;
			Map<String, String> xtraInfoMap = null;
			List<DoubleConfirmationRequestBean> reqBeanList = null;
			
			//Get IVR modes from configuration and validate. If true then execute the below if block
			String modesForBSNLConsent = RBTParametersUtils.getParamAsString(
					iRBTConstant.DOUBLE_CONFIRMATION,
					"CONSENT_MODES_WITHOUT_TRANS_ID_FOR_BSNL", null);
			String modeMapForBSNLConsent = RBTParametersUtils.getParamAsString(
					iRBTConstant.DOUBLE_CONFIRMATION,
					"MODE_MAP_FOR_BSNL", null);
			List<String> modeList = null;
			List<String> modeMappingList = null;
			if (null != modesForBSNLConsent) {
				modeList = Arrays.asList(modesForBSNLConsent.split(","));
			}
			if (null != modeMapForBSNLConsent) {
				modeMappingList = Arrays.asList(modeMapForBSNLConsent
						.split(";"));
			}
			
			String reqTransId = null;
			
			if (null != modeList && !modeList.isEmpty()
					&& modeList.contains(consentmode)) {
				try {
					if (checkCGFlowForBSNL) {
						reqTransId = transId;
						ClipMinimal clip = null;
						if (null != modeMappingList) {
							for (String mode : modeMappingList) {
								String[] mappedMode = mode.split("=");
								if (consentmode.equalsIgnoreCase(mappedMode[0])) {
									consentmode = mappedMode[1];
									break;
								}
							}

						}
						consentmode = splitAndAppendSingleQuotes(consentmode);
						if (null != clipID && !clipID.isEmpty()) {
							clip = RBTDBManager.getInstance().getClipById(
									Integer.parseInt(clipID));
							if (clip != null) {
								reqBeanList = RBTDBManager.getInstance()
										.getConsentRecordListBySongID(msisdn,
												"SEL", clip.getWavFile(),
												consentmode);
							}
						}

						if (clipID == null || clip == null) {
							reqBeanList = RBTDBManager.getInstance()
									.getConsentRecordListBySongID(msisdn,
											"SEL", null, consentmode);
						}

						if (reqBeanList != null && reqBeanList.size() > 0) {
							DoubleConfirmationRequestBean reqBean = reqBeanList
									.get(0);
							String xtraInfo = reqBean.getExtraInfo();
							if (xtraInfo != null
									&& !xtraInfo.equalsIgnoreCase("null"))
								xtraInfoMap = DBUtility
										.getAttributeMapFromXML(xtraInfo);
							if (xtraInfoMap != null
									&& xtraInfoMap.containsKey("TRANS_ID")) {
								transId = xtraInfoMap.get("TRANS_ID");
								xtraInfoMap = null;
							} else {
								transId = reqBean.getTransId();
							}
						}
					}
				} catch (OnMobileException e) {
					e.printStackTrace();
					logger.error("Exception :", e);
				}

			}
			
			String makeConsentForConfigChargeClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CHARGE_CLASS_FOR_CONSENT", null);
			List<String> consentChargeClass = null;
			if(makeConsentForConfigChargeClass != null) {
				consentChargeClass = Arrays.asList(makeConsentForConfigChargeClass.split(","));
			}
			

			String comboTransID = null;
			try {
				
				//CG Integration Flow - Jira -12806
				reqBeanList = RBTDBManager
						.getInstance().getConsentRequestForCallBack(transId,
								msisdn);
				String extraInfo = null;
//				Map<String, String> xtraInfoMap = null;
				consentCallbackLogger.info("CONSENT CALLBACK RECEIVED: "
						+ httpRequest.toString());
				if (reqBeanList != null && reqBeanList.size() > 0) {
					DoubleConfirmationRequestBean reqBean = reqBeanList.get(0);

					if (reqBean.getConsentStatus() == 1
							|| reqBean.getConsentStatus() == 0) {
						
						
						if(reqBean.getRequestType().equalsIgnoreCase("GIFT") || reqBean.getRequestType().equalsIgnoreCase("GIFT_ACCEPT")) {
							comboTransID = transId;
							response = processGiftNGiftAcceptCallback(reqBean, httpRequest, tpcgID);
						}
						else {
							String xtraInfo = reqBean.getExtraInfo();
							// RBT-14301: Uninor MNP changes.
							String subsciberCirleId = null;
							boolean normalFlow = true;
							if (circleId != null && !circleId.isEmpty()) {
								circleId = (!circleIdMap.isEmpty() && circleIdMap
										.get(circleId) != null) ? circleIdMap
										.get(circleId) : circleId;
								Subscriber sub=rbtDbMgr.getSubscriber(reqBean.getSubscriberID());
								if(!rbtDbMgr.isSubscriberDeactivated(sub)) {
									subsciberCirleId = sub.circleID();
									if (!subsciberCirleId
											.equalsIgnoreCase(circleId)) {
										RBTDBManager
												.getInstance()
												.updateConsentStatusOfConsentRecord(
														reqBean.getSubscriberID(),
														reqBean.getTransId(),
														statusAfterConsentFailure, null, null);
										response = "CONSENT_FAILED";
										logger.info("Consent failedbecause the circleId mismatch. transId: "
												+ transId + ", Result: " + result);
										normalFlow = false;
									}
								} else {
									SitePrefix sitePrefix = CacheManagerUtil
											.getSitePrefixCacheManager()
											.getSitePrefixes(circleId);
									if (sitePrefix == null
											|| sitePrefix.getSiteUrl() != null) {
										RBTDBManager
												.getInstance()
												.updateConsentStatusOfConsentRecord(
														reqBean.getSubscriberID(),
														reqBean.getTransId(),
														statusAfterConsentFailure, null, null);
										response = "CONSENT_FAILED";
										logger.info("Consent failedbecause the circleId mismatch. transId: "
												+ transId
												+ ", Result: "
												+ result);
										normalFlow = false;
									}
								}
							}
							if (normalFlow) {
								if (xtraInfo != null
										&& !xtraInfo.equalsIgnoreCase("null"))
									xtraInfoMap = DBUtility
									.getAttributeMapFromXML(xtraInfo);
								if (xtraInfoMap == null)
									xtraInfoMap = new HashMap<String, String>();
								if (tpcgID != null)
									xtraInfoMap.put("TPCGID", tpcgID);
								if (circleId != null
										&& !circleId.trim().isEmpty()) {
									xtraInfoMap.put("MNPCHECKENABLED", "false");
								}
								Map<String,String> xtraIntMap = null;
								if(reqBean.getRequestType().equalsIgnoreCase("ACT")){
									xtraIntMap = htIntegrationBaseConfMap;
								}else{
									xtraIntMap = htIntegrationSelConfMap;
								}
								if(xtraIntMap!=null && xtraIntMap.size()>0){
									for(Entry<String, String> entry : xtraIntMap.entrySet()){
										String key = entry.getKey();
										String value = entry.getValue();
										String paramVal = httpRequest.getParameter(key);
										if(paramVal!=null){
											xtraInfoMap.put(value, paramVal);
										}
									}
								}
								extraInfo = DBUtility
										.getAttributeXMLFromMap(xtraInfoMap);
								
								if (extraInfo != null) {
									comboTransID = xtraInfoMap.get("TRANS_ID");
								}
								if (result != null && result.indexOf("success") != -1) {
									
									String mode = null;
									if (externalToInternalModeMapping != null && reqBean.getMode() != null && externalToInternalModeMapping.containsKey(reqBean.getMode())) {
										mode = externalToInternalModeMapping.get(reqBean.getMode());
										logger.info("Mode changed. From:" + reqBean.getMode() + ". To: " + mode + ".");
									}
									
									if (xtraInfoMap != null && xtraInfoMap.containsKey("rrbt_activated")) {
										boolean responseStr = processExpiredRecordForRRBT(reqBean);
										logger.info("RRBT Consent Deactivation response = "+responseStr);
									}
									//CG Integration Flow - Jira -12806
									boolean update = false;
									String inlineFlow = null;
									
									if(!checkCGFlowForBSNL) {
										Integer inlineFlag = null;
										
										inlineFlow = com.onmobile.apps.ringbacktones.services.common.Utility.getInlineFlow(reqBean.getClassType(), 1);
										if(inlineFlow != null) {
											Subscriber sub=rbtDbMgr.getSubscriber(reqBean.getSubscriberID());
											if(sub.subYes().equals(iRBTConstant.STATE_ACTIVATED) && reqBean.getRequestType().equalsIgnoreCase("SEL"))
												inlineFlag = 1;
											else
												inlineFlow = null;
										}
										
										update = RBTDBManager.getInstance()
												.updateConsentStatusAndModeOfConsentRecord(null,
														reqBean.getSubscriberID(), transId,
														statusAfterConsentSuccess, null, mode, extraInfo,circleId, inlineFlag);
									}
									
									if(consentChargeClass != null && reqBean.getClassType() != null && consentChargeClass.contains(reqBean.getClassType())) {
										List<DoubleConfirmationRequestBean> doubleConfirmActReqBean = RBTDBManager.getInstance().getDoubleConfirmationRequestBeanForStatus("1", null, reqBean.getSubscriberID(), "ACT", false);
										if(doubleConfirmActReqBean != null && doubleConfirmActReqBean.size() > 0) {
											RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(doubleConfirmActReqBean.get(0).getSubscriberID(),doubleConfirmActReqBean.get(0).getTransId(), statusAfterConsentFailure, null, null);
										}
									}
									
									//CG Integration Flow - Jira -12806
									if (!checkCGFlowForBSNL && comboTransID != null) {
										RBTDBManager.getInstance().updateModeOfConsentRecord(null, reqBean.getSubscriberID(), comboTransID, mode, circleId);
										logger.info("Mode updated for selection record. Mode: " + mode + "TransId: " + comboTransID);
									}
									if (comboTransID != null && htIntegrationSelConfMap!=null
											&& htIntegrationSelConfMap.size() > 0) {
										Map<String, String> map = new HashMap<String, String>();
										for (Entry<String, String> entry : htIntegrationSelConfMap.entrySet()) {
											String key = entry.getKey();
											String value = entry.getValue();
											String paramValue = httpRequest.getParameter(key);
											if(paramValue!=null){
												map.put(value, paramValue);
											}
										}
										if (map.size() > 0) {
											List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeanForStatus = RBTDBManager
													.getInstance().getDoubleConfirmationRequestBeanForStatus(null, comboTransID, msisdn,
															null, false);
											if (doubleConfirmationRequestBeanForStatus != null
													&& doubleConfirmationRequestBeanForStatus.size() > 0) {
												DoubleConfirmationRequestBean requestBean = doubleConfirmationRequestBeanForStatus.get(0);
												Map<String, String> htExtraInfoMap = DBUtility.getAttributeMapFromXML(requestBean
														.getExtraInfo());
												if (htExtraInfoMap == null)
													htExtraInfoMap = new HashMap<String, String>();
												htExtraInfoMap.putAll(map); 
												String htExtraInfo = DBUtility.getAttributeXMLFromMap(htExtraInfoMap);
												logger.info("Capturing Parameters in the Extra Info for Selection = "+htExtraInfo);
												RBTDBManager.getInstance().updateConsentExtrInfo(requestBean.getSubscriberID(),comboTransID, htExtraInfo, circleId); 
												
											}
										}
									}
									//CG Integration Flow - Jira -12806
									/*if (!checkCGFlowForBSNL && extraInfo != null) {
									RBTDBManager.getInstance()
											.updateConsentExtrInfo(
													reqBean.getSubscriberID(),
													transId, extraInfo);
								}
								else*/ 
									if(checkCGFlowForBSNL) {
										// Direct Process
										reqBean.setExtraInfo(extraInfo);
										if(mode != null) {
											reqBean.setMode(mode);
										}
										processRequest = new DoubleConfirmationContentProcessUtils();
										response = processRequest
												.processRecord(reqBean, reqTransId);
										
									}
									
									if (!checkCGFlowForBSNL && update) {
										response = "SUCCESS";
										if(inlineFlow != null)
											com.onmobile.apps.ringbacktones.services.common.Utility.sendInlineMessage(reqBean.getSubscriberID(), reqBean.getTransId(), null, null, WebServiceConstants.PROVISIONING_CONSENT_TO_SELECTION);
									}
									
								} else {
									RBTDBManager.getInstance()
									.updateConsentStatusOfConsentRecord(
											reqBean.getSubscriberID(), transId,
											statusAfterConsentFailure, null, null);
									if (comboTransID != null)
										RBTDBManager.getInstance()
										.updateConsentStatusOfConsentRecord(
												reqBean.getSubscriberID(),
												comboTransID,
												statusAfterConsentFailure, null, null);
									response = "FAILURE_NO_CONSENT";
								}
							}
						}
					} else if (reqBean.getConsentStatus() == 4) {
						response = "USER_DEACTIVE";
					} else {
						response = "CALLBACK_ALREADY_PRCESSED";
					}
				} else {
					boolean isNoConsentFlow = false;
					Subscriber subscriber = RBTDBManager.getInstance()
							.getSubscriber(msisdn);
					SubscriberStatus[] subscriberStatuses = RBTDBManager
							.getInstance()
							.getAllSubscriberSelectionRecords(msisdn, null);
					if (subscriber != null) {
						extraInfo = subscriber.extraInfo();
						xtraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						
						if(xtraInfoMap !=null && !xtraInfoMap
								.containsKey(iRBTConstant.EXTRA_INFO_USER_CONSENT)){
						if (xtraInfoMap
								.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
							if (xtraInfoMap.get(
									iRBTConstant.EXTRA_INFO_TRANS_ID).equals(
									transId)) {
								isNoConsentFlow = true;
								String consentStr = iRBTConstant.NO;
								String[] failureResponses=failureResponseValues.split(",");
								boolean isFailureResponse=false;
								for(String failureResponse:failureResponses){
									if(result.equalsIgnoreCase(failureResponse)){
										isFailureResponse=true;
										break;
									}
								}
								if(isFailureResponse){
									deactivateSubsriberAndSelections(
											transId, result, msisdn,
											xtraInfoMap, subscriberStatuses);
									response = "CONSENT_FAILED";
									boolean smsSend = sendPromotionalSmsForNOConsent(msisdn,subscriber.activatedBy(),subscriberStatuses); 
									if (smsSend) {
										logger.info("SMS successfully sent to " + " MSISDN = " + msisdn);
									} else {
										logger.info("SMS could not be sent to " + " MSISDN = " + msisdn);
									}
								}
								else{
									consentStr = iRBTConstant.YES;
									response = "SUCCESS";
									logger.info("Subscription consent successful. Returning without doing any operation. Result: "
											+ result);
								}
								boolean success = RBTDBManager.getInstance().updateExtraInfo(msisdn,  iRBTConstant.EXTRA_INFO_USER_CONSENT, consentStr);
								if(!success)
									logger.info("Could not update the extra info col, " + consentStr + " callback was received. success: "
											+ success);
							}
						} else {
							// Checking for selection consent case
							if (subscriberStatuses != null) {
								int numberOfSelectionsDeleted = 0;
								for (SubscriberStatus selection : subscriberStatuses) {
									Map<String, String> selExtraInfoMap = DBUtility
											.getAttributeMapFromXML(selection
													.extraInfo());
									if (selExtraInfoMap != null) {
										if (xtraInfoMap
												.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)
												&& xtraInfoMap
														.get(iRBTConstant.EXTRA_INFO_TRANS_ID)
														.equals(transId)) {
											isNoConsentFlow = true;
											if (result
													.equalsIgnoreCase("SUCCESS")) {
												response = "SUCCESS";
												logger.info("Subscribription consent successful. Returning without doing any operation. Result: "
														+ result);
											} else {
												boolean isDeleted = deactivateSelection(
														msisdn,
														xtraInfoMap,
														numberOfSelectionsDeleted,
														selection.refID());
												if (isDeleted) {
													numberOfSelectionsDeleted++;
												}
												response = "CONSENT_FAILED";
												logger.info("Consent failed. transId: "
														+ transId
														+ ", Result: "
														+ result
														+ ". Selection(s) deactivated. "
														+ "Number of selections deleted: "
														+ numberOfSelectionsDeleted);
											}
										}
									}
								}
							}
						}
						} else {
							isNoConsentFlow = true;
							response = "CONSENT_CALLBACK_ALREADY_PROCESSED";
							logger.info("Callback has already been processed, Current requested request:"
									+ result);
						}
						if (!isNoConsentFlow) {
							response = "INVALID_TRANS_ID";
						}
					}
				}
			} catch (OnMobileException e) {
				e.printStackTrace();
				logger.error("Exception :", e);
			} finally {
			consentCallbackLogger.info("CONSENT CALLBACK RECEIVED: "
						+ httpRequest.toString() + " , COMBO_TRANS_ID = "
						+ comboTransID + " , RESPONSE = " + response);
			}
			logger.info("Double Confirmation Callback Received for TRANS_ID = "
					+ transId + " MSISDN = " + msisdn + " Result = " + result);
		}	
		if (checkCGFlowForBSNL) {//CG Integration Flow - Jira -12806
			if (response.equalsIgnoreCase("success")) {
				response = "200|Accepted|" + transId;
			} else if (response.equalsIgnoreCase("FAILURE")
					|| response
							.equalsIgnoreCase(WebServiceConstants.TECHNICAL_DIFFICULTIES)) {
				response = "500|Internal System Error|" + transId;
			} else {
				response = "533|" + response + "|" + transId;
			}

		} else {
			response = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue("DOUBLE_CONFIRMATION", response,
							response);
		}
		httpResponse.setStatus(responseCode);
		httpResponse.getWriter().write(response);
	}

	private boolean sendPromotionalSmsForNOConsent(String msisdn,String mode,SubscriberStatus[] subscriberStatus) {
		String smsText = null;
		String songName = null;
		String promoId = null;
		
		if(mode != null){
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					mode.toUpperCase(),iRBTConstant.FAILED_CONSENT_PROMOTION_TEXT ,null);
		}
		
		//SMS_<MODE>, FAILED_CONSENT_PROMOTION_TEXT
		if(smsText == null){
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					"DEFAULT",iRBTConstant.FAILED_CONSENT_PROMOTION_TEXT, null);
		}
		
		if(smsText == null){
			logger.info(iRBTConstant.FAILED_CONSENT_PROMOTION_TEXT +"Sms Text is not configured for this operator. "
					+" MSISDN = " + msisdn);
			return false;
		}	
		
		if (Utility.isBlackOutPeriodNow()) {
			logger.info("Blackout period, cannot send sms now."
					+ Calendar.getInstance().getTime());
			return false;
		}
		if (Utility.isDNDEnabled(msisdn)) {
			logger.info("User with subscriber Id " + msisdn
					+ " has DND enabled");
			return false;
		}
		
		for (SubscriberStatus selection : subscriberStatus) {
			if (Utility.isShuffleCategory(selection.categoryType())) {
				Category category = RBTCacheManager.getInstance().getCategory(
						selection.categoryID());
				if (category != null) {
					songName = String.valueOf(category.getCategoryName());
					promoId = category.getCategoryPromoId();
				} else {
					logger.error("Category Object is null," +" msisdn: " +msisdn);
					return false;
				}
			} else {
				Clip clip = RBTCacheManager.getInstance()
						.getClipByRbtWavFileName(selection.subscriberFile());
				if (clip != null) {
					songName = clip.getClipName();
					promoId = clip.getClipPromoId();
				} else {
					logger.error("Clip Object is null," +" msisdn: " +msisdn);
					return false;				
				}
			}
		}
		smsText = smsText.replace("$SONG_NAME$", songName != null ? songName : "");
		smsText = smsText.replace("$PROMO_ID$", promoId != null ? promoId : ""); 		
		
		String senderNo = RBTParametersUtils.getParamAsString(
				iRBTConstant.SMS, iRBTConstant.SENDER_NUMBER,  "543211");
		
		if(senderNo == null){
			logger.info("Sender No is not configured"
					+" MSISDN = " + msisdn);
			return false;
		}
				
		try {
			return Tools.sendSMS(senderNo, msisdn, smsText, false);	
		} catch (Exception e) {
			logger.error("Exception occurred while sending promotional sms: smsText,"
					+ smsText +" MSISDN= "+msisdn);
			return false;
		}
		
	}
	
	private void deactivateSubsriberAndSelections(String transId,
			String result, String msisdn, Map<String, String> xtraInfoMap,
			SubscriberStatus[] subscriberStatuses) {
		int numberOfSelectionsDeleted = 0;
		if (subscriberStatuses != null) {
			for (SubscriberStatus selection : subscriberStatuses) {
				Map<String, String> selExtraInfoMap = DBUtility
						.getAttributeMapFromXML(selection
								.extraInfo());
				if (selExtraInfoMap != null) {
					if (xtraInfoMap
							.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)
							&& xtraInfoMap
									.get(iRBTConstant.EXTRA_INFO_TRANS_ID)
									.equals(transId)) {
						boolean isDeleted = deactivateSelection(
								msisdn,
								xtraInfoMap,
								numberOfSelectionsDeleted,
								selection.refID());
						if (isDeleted) {
							numberOfSelectionsDeleted++;
						}
					}
				}
			}
		}
		boolean subDeactStatus = false;
		subDeactStatus = deactivateSubscriber(msisdn);
		logger.info("Consent failed. transId: "
				+ transId
				+ ", Result: "
				+ result
				+ ". Subscriber and selections (if exists) deactivated. Subscriber deact status: "
				+ subDeactStatus
				+ ", number of selections deleted: "
				+ numberOfSelectionsDeleted);
	}

	private boolean deactivateSubscriber(String msisdn) {
		boolean subDeactStatus;
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				msisdn);
		subscriptionRequest
				.setMode(deactivationMode);
		RBTClient.getInstance()
				.deactivateSubscriber(
						subscriptionRequest);
		subDeactStatus = subscriptionRequest
				.getResponse() != null
				&& subscriptionRequest
						.getResponse()
						.equalsIgnoreCase("SUCCESS");
		return subDeactStatus;
	}

	private boolean deactivateSelection(String msisdn,
			Map<String, String> xtraInfoMap, int numberOfSelectionsDeleted,
			String refId) {
		boolean isDeleted = false;
		SelectionRequest selectionRequest = new SelectionRequest(msisdn);
		selectionRequest.setMode(deactivationMode);
		selectionRequest.setRefID(refId);
		RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);
		String selDeletionResponse = selectionRequest.getResponse();
		if (selDeletionResponse != null
				&& selDeletionResponse.equalsIgnoreCase("SUCCESS")) {
			isDeleted = true;
		}
		return isDeleted;
	}

	private String processGiftNGiftAcceptCallback(
			DoubleConfirmationRequestBean reqBean, HttpServletRequest httpRequest, String tpcgId) throws OnMobileException {
		String gifterID = null;
		String gifteeID = null;
		
		if(reqBean.getRequestType().equalsIgnoreCase("GIFT")) {
			gifterID = reqBean.getSubscriberID();
			gifteeID = reqBean.getCallerID();
		}
		else {
			gifteeID = reqBean.getSubscriberID();
			gifterID = reqBean.getCallerID();
		}
		
		String subscriptionClass = reqBean.getSubscriptionClass();
		String chargeClass = reqBean.getClassType();
		String toneID = reqBean.getClipID() +"";
		
		Map<String,String> reqBeanExtraInfoMap = DBUtility.getAttributeMapFromXML(reqBean.getExtraInfo());
		
		if(reqBean.getRequestType().equalsIgnoreCase("GIFT")) {
			GiftRequest giftRequest = new GiftRequest();
			giftRequest.setGifterID(gifterID);;
			giftRequest.setGifteeID(gifteeID);
			giftRequest.setMode(reqBean.getMode());
			giftRequest.setToneID(toneID);			
			giftRequest.setGiftSentTime(reqBean.getRequestTime());
			
			HashMap<String, String> giftInfoMap = new HashMap<String, String>();
			giftInfoMap.put(iRBTConstant.SUBSCRIPTION_CLASS, subscriptionClass);
			giftInfoMap.put(iRBTConstant.CHARGE_CLASS, chargeClass);
			giftInfoMap.put("TPCGID", tpcgId);
			
			
			Map<String,String> xtraIntMap = htIntegrationSelConfMap;
			if(xtraIntMap!=null && xtraIntMap.size()>0){
				for(Entry<String, String> entry : xtraIntMap.entrySet()){
					String key = entry.getKey();
					String value = entry.getValue();
					String paramVal = httpRequest.getParameter(key);
					if(paramVal!=null){
						giftInfoMap.put(value, paramVal);
					}
				}
			}
			
			giftInfoMap.put("preCharge", "true");
			if(reqBeanExtraInfoMap != null ) { 
				if(reqBeanExtraInfoMap.containsKey("isComvivaCircle")) {
					giftInfoMap.put("isComvivaCircle", reqBeanExtraInfoMap.get("isComvivaCircle"));
				}
				if(reqBeanExtraInfoMap.containsKey(WebServiceConstants.param_IsGifteeActive)){
					giftInfoMap.put(WebServiceConstants.param_IsGifteeActive, reqBeanExtraInfoMap.get(WebServiceConstants.param_IsGifteeActive));
				}
				if(reqBeanExtraInfoMap.containsKey(WebServiceConstants.param_GifteeHub)){
					giftInfoMap.put(WebServiceConstants.param_GifteeHub, reqBeanExtraInfoMap.get(WebServiceConstants.param_GifteeHub));
				}
			}

			giftRequest.setInfoMap(giftInfoMap);
			
			
			RBTClient.getInstance().sendGift(giftRequest);
			RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(reqBean.getSubscriberID(), reqBean.getTransId(), "3");
			return giftRequest.getResponse();
		}
		else {
			
			HashMap<String,String> infoMap = new HashMap<String,String>();
			infoMap.put(iRBTConstant.SUBSCRIPTION_CLASS, subscriptionClass);
			infoMap.put(iRBTConstant.CHARGE_CLASS, chargeClass);
			infoMap.put("giftee_tpcgid", tpcgId);
			
			ViralSMSTable gift = RBTDBManager.getInstance().insertViralSMSTableMap(
					gifterID, new Date(), "GIFTED", gifteeID, toneID, 0,
					reqBean.getMode(), null, infoMap);

			SelectionRequest selectionRequest = new SelectionRequest(gifteeID);
			selectionRequest.setGifterID(gifterID);
			selectionRequest.setClipID(toneID);
			selectionRequest.setCategoryID("23");
			selectionRequest.setGiftSentTime(gift.sentTime());
			selectionRequest.setMode(gift.selectedBy());
			selectionRequest.setUserInfoMap(infoMap);
			
			infoMap.clear();
			
			Map<String,String> xtraIntMap = htIntegrationSelConfMap;
			if(xtraIntMap!=null && xtraIntMap.size()>0){
				for(Entry<String, String> entry : xtraIntMap.entrySet()){
					String key = entry.getKey();
					String value = entry.getValue();
					String paramVal = httpRequest.getParameter(key);
					if(paramVal!=null){
						infoMap.put(value, paramVal);
					}
				}
			}
			
			infoMap.put("TPCGID", tpcgId);
			selectionRequest.setTpcgID(tpcgId);
			
			RBTClient.getInstance().acceptGift(selectionRequest);
			RBTDBManager.getInstance().updateConsentStatusOfConsentRecord(reqBean.getSubscriberID(), reqBean.getTransId(), "3");
			return selectionRequest.getResponse();
			
		}
		
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private static void getHtConfigMap() {
		String baseMappedStr = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON,
						"BASE_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
		String selMappedStr = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.COMMON,
						"SEL_PARAMETERS_MAPPING_FOR_INTEGRATION", null);
		if (baseMappedStr != null) {
			htIntegrationBaseConfMap = new HashMap<String,String>();
			String str[] = baseMappedStr.split(";");
			for (int i = 0; i < str.length; i++) {
				String s[] = str[i].split(",");
				if (s != null && s.length == 2){
					htIntegrationBaseConfMap.put(s[0], s[1]);
				}
			}
		}
		
		if(selMappedStr!=null){
			htIntegrationSelConfMap = new HashMap<String,String>();
			String str[] = selMappedStr.split(";");
			for (int i = 0; i < str.length; i++) {
				String s[] = str[i].split(",");
				if (s != null && s.length == 2){
					htIntegrationSelConfMap.put(s[0], s[1]);
				}
		    }
	   }

	}
	
	private boolean processExpiredRecordForRRBT(DoubleConfirmationRequestBean bean){
		boolean isProcessed = false;
		String rrbtSupportUrl = RBTParametersUtils.getParamAsString("DAEMON",
			        	"RRBT_URL_FOR_CONSENT_EXPIRED", null);
		if(rrbtSupportUrl == null){
			logger.info("The Parameter RRBT_URL_FOR_CONSENT_EXPIRED is not configured");
			return false; 
		}
		HttpParameters httpParameters =new HttpParameters();
		rrbtSupportUrl = rrbtSupportUrl.replaceAll("%MSISDN%", bean.getSubscriberID());
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return isProcessed;
	}
	private String splitAndAppendSingleQuotes(String str) {
		if(null != str) {
			String[] arr = str.split(",");
			StringBuilder sb = new StringBuilder("'");
			for(int i = 0; i< arr.length;i++) {
				sb.append(arr[i].trim());
				sb.append("'");
				if(arr.length - 1 > i) {
					sb.append(",'");	
				}
			}
			return sb.toString();
		}
		logger.info("Returning mode: " + str);
		return str;
	}	
}
