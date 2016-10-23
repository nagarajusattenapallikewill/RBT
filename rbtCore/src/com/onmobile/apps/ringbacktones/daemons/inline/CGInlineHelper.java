package com.onmobile.apps.ringbacktones.daemons.inline;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet.ComvivaFactoryObject;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.BasicResponseHandler;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

@Component("cgHelper")
public class CGInlineHelper extends DoubleConfirmationConsentPushThread {
	private static Logger logger = Logger.getLogger(CGInlineHelper.class);
	private HttpClient httpClient = new HttpClient();
	private MultiThreadedHttpConnectionManager connectionManager = null;;

	public CGInlineHelper() {
		super(null);
		connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams connectionParam = new HttpConnectionManagerParams();
		connectionParam.setMaxTotalConnections(200);
		connectionParam.setDefaultMaxConnectionsPerHost(200);
		connectionParam.setConnectionTimeout(RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, "SMDAEMON_TIMEOUT",6)*1000);
		connectionParam.setSoTimeout(RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, "SMDAEMON_TIMEOUT",6)*1000);
		connectionManager.setParams(connectionParam);
		httpClient = new HttpClient(connectionManager);
	}

	public void processConsent(DoubleConfirmationRequestBean requestBean) throws Exception {
		long timeTakenToQueryWDS = 0;
		long timeTakenToHITConsent = 0;
		String consentUrl = null;
		try {
			if (requestBean == null) {
				throw new OnMobileException(WebServiceConstants.REQ_BEAN_NULL);
			}

			consentUrl = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION",
					"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL", null);
			String requestDateFormat = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION", "DATE_FORMAT_OF_REQUEST_TIME",
					"yyMMddHHmmss");
			RequestTimeDateFormat = new SimpleDateFormat(requestDateFormat);
			if (modesForNotToGetConsentList != null
					&& modesForNotToGetConsentList.contains(requestBean
							.getMode()))
				throw new OnMobileException(WebServiceConstants.NO_CONSENT_MODE);
			
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
					String operatorName = circleID.substring(0,circleID.indexOf("_"));
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
					throw new OnMobileException(WebServiceConstants.MAX_RETRIES_REACHED);
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
				throw new OnMobileException(WebServiceConstants.WDS_INVALID);
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
					.getInstance().getDoubleConfirmationRequestBeanForStatusWithInlineFlag(
							null, requestBean.getTransId(), requestBean.getSubscriberID(), null, true, false, 0);
			if (dbBeanList.get(0).getConsentStatus() != 0)
				throw new OnMobileException(WebServiceConstants.CONSENT_STATUS_INVALID);
			
			
			
			logger.info("Consent url: " + consentUrl);
			long initConsentPushMillis = System.currentTimeMillis();
			String response = getResponse(consentUrl);
			long endConsentPushMillis = System.currentTimeMillis();
			timeTakenToHITConsent = endConsentPushMillis
					- initConsentPushMillis;

			logger
					.info("Consent url: " + consentUrl + " response: "
							+ response);
			if(response == null)
				throw new OnMobileException("CG Response is null while processing consent");
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
				//TODO:for updating status to 2 for CGR in daemon code, apply same here & accordingly change statusToBeUpdated either 1 or 2
				String statusToBeUpdated = "1";
				boolean success = RBTDBManager.getInstance()
						.updateConsentStatusOfConsentRecord(requestBean.getSubscriberID(),
								transId, statusToBeUpdated, "0", 1);
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
				RBTDBManager.getInstance().updateConsentExtrInfo(
						requestBean.getSubscriberID(), requestBean.getTransId(), xtraInfo, null);
				throw new OnMobileException(WebServiceConstants.RETRIAL);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Exception while hitting Consent url...");
			throw ex;
		} finally {
			consentPushLogger.info("QUERY_WDS_TIME = " + timeTakenToQueryWDS
					+ " ,CONSENT_HIT_RESPONSE_TIME = " + timeTakenToHITConsent
					+ " ,CONSENT_URL = " + consentUrl);
		}
	}
	
	public String getResponse(String url)
	{
		logger.info("url hit = " + url);
		if (url == null)
			return null;

		String cgAuthenticationDetails = RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, 
				"USERNAME_PASSWORD_CG_AUTHENTICATION", null);
		HttpMethod getMethod = new GetMethod(url);
		int responseCode = -1;
		String responseString = null;
		Header[] responseHeaders = null;
		try
		{	
			if (cgAuthenticationDetails != null) {
				String[] cgAuthenticationDetailsArray = cgAuthenticationDetails.split(",");
				for (int i = 0; i < cgAuthenticationDetailsArray.length; i++) {
					getMethod.setRequestHeader(
							cgAuthenticationDetailsArray[i].split(":")[0],
							cgAuthenticationDetailsArray[i].split(":")[1]);
				}
			}
			//logger.info(getMethod.getRequestHeaders());
			responseCode = httpClient.executeMethod(getMethod);
			responseString = getMethod.getResponseBodyAsString();
			responseHeaders = getMethod.getResponseHeaders();
			logger.info("url responseCode = " + responseCode + ", responseString = " + responseString);	
		}
		catch (HttpException e)
		{
			logger.error("httpexception occured", e);
		}
		catch (IOException e)
		{
			logger.error("IOException occured", e);
		}
		finally
		{
			getMethod.releaseConnection();
		}
		BasicResponseHandler responseHandler = ComvivaFactoryObject.getResponseInstance();
		
		return responseHandler.processResponse(responseCode,
				(null != responseString ? responseString.trim()
						: responseString));
	}
}
