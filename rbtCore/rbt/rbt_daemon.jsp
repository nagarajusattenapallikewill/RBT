
<%@page import="com.onmobile.apps.ringbacktones.webservice.client.requests.CallbackRequest"%>
<%@page import="com.onmobile.apps.ringbacktones.logger.CallBackLogTool"%>
<%@page import="org.apache.log4j.Level"%><%@page import="com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackConstants"%>
<%@page import="com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackResponse"%>
<%@page import="com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallback"%>
<%@page import="com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackFinder"%>
<%@page import="java.util.Map"%>
<%@page import="com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackContext"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.Parameters"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails"%>
<%@page session="false"%>
<%@page import="com.onmobile.apps.ringbacktones.webservice.common.HttpResponse"%>
<%@page import="com.onmobile.apps.ringbacktones.webservice.common.HttpParameters"%>
<%@page import="com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient"%>
<%@page import="com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr"%>
<%@page import="com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail"%>
<%@page import="com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass"%><%@page import="com.onmobile.apps.ringbacktones.provisioning.AdminFacade"%>
<%@page import="com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder"%><%@page import="com.onmobile.apps.ringbacktones.provisioning.common.Constants"%><%@page import="com.onmobile.apps.ringbacktones.provisioning.common.Utility"%><%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassBean"%><%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.subscriptions.RBTDaemonHelper,java.util.HashMap,java.util.ResourceBundle,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,java.util.Hashtable,com.onmobile.apps.ringbacktones.common.Tools,org.apache.log4j.Logger,java.sql.Connection,java.util.StringTokenizer,com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass,com.onmobile.apps.ringbacktones.genericcache.*,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.content.Subscriber,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants,com.onmobile.apps.ringbacktones.provisioning.AdminFacade"%>
<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%><%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%><%
Logger logger = Logger.getLogger(RBTDaemonHelper.class);
try
{
long start = System.currentTimeMillis();
String strIP  = request.getRemoteAddr();
String actInfo = "SMS";
String strSubID = null;
String strSrvKey = null;
String strStatus = null;
String strRefID = null;
String strAction = null;
String strDate = null;
String strTime = null;
String strType = null;
String strAmount = null;
String strInfo = null;
String strChangedServiceKey = null;
String strAutoUpg = null;
String strMode = null;
String strFailureInfo=null;
String strReason = null;
String strReasonCode = null;
String strCurrentSubStatus = null;
String strTransId = null;
String strEventKey = null;
Subscriber subscriber = null;
String strProbeResult = null;
String requestType = null;
String offerID = null;
String rtKey = null;
String sbnID = null;
String retry = null;
String siteId = null;
boolean isValidPrefix = false ;
String circleID = null;
String circleIDFromPrism = null;
String X_ONMOBILE_REASON = "FAILURE";
String smsPromoPrefix = null;
Connection conn = null;
RBTDaemonHelper rbtDaemon = RBTDaemonHelper.init();
HashMap subscriberDetail = new HashMap();
String cosID = null;
String winresponse=null;
CosDetails cosDetail = null;
String prepaidYes = null;

String insuspension=null;

String strNextBillingDate = null;


ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
String dbURL = resourceBundle.getString("DB_URL");
String poolSizeStr = resourceBundle.getString("DB_POOL_SIZE");
int poolSize = 4;
if (poolSizeStr != null)
{
	try
	{
		poolSize = Integer.parseInt(poolSizeStr);
	}
	catch (Exception e)
	{
		poolSize = 4;
	}
}
RBTDBManager rbtDBManager = RBTDBManager.init(dbURL, poolSize);

Map<String, String[]> parametersMap = request.getParameterMap();
siteId = request.getParameter("site_id");
if(siteId!=null && !siteId.isEmpty()){
	circleIDFromPrism = rbtDaemon.getMappedCircleIdFromSiteId(siteId);
	if(circleIDFromPrism ==null || circleIDFromPrism.trim().isEmpty()){
		X_ONMOBILE_REASON = "INVALID_PARAMETER";	
		response.setStatus(500);
		out.flush();
		out.write(X_ONMOBILE_REASON);
		return;
	}
}
SMCallbackContext smCallbackContext = SMCallbackContext.buildSMCallbackContext(parametersMap);

strRefID = request.getParameter("refid");
strAction = request.getParameter("action");
if (strAction != null && strRefID != null && strAction.equalsIgnoreCase("EVT") && strRefID.startsWith("RBTGIFT"))
{
        smCallbackContext.setAction("GIFT");
}

SMCallback smCallback = SMCallbackFinder.findCallback(smCallbackContext);
String callbackResponse = null;
long timeDiff = -1;
if (smCallback != null)
{
	try
	{
		SMCallbackResponse smCallbackResponse = smCallback.processCallback(smCallbackContext);
		callbackResponse = smCallbackResponse.getResponse();
		timeDiff = System.currentTimeMillis() - start;
		//rbtDaemon.writeTrans("CALLBACK-RESPONSE", request.getQueryString(), callbackResponse, String.valueOf(timeDiff));
		
		if(callbackResponse.equals(SMCallbackConstants.FAILURE))
			response.setStatus(500);
		out.flush();
		out.write(callbackResponse);
	}
	catch(Exception e)
	{
		if(logger.isEnabledFor(Level.ERROR))
			logger.error("Issue in callback", e);	
	}
	CallBackLogTool.writeCallBackTransactionLog(parametersMap, callbackResponse, timeDiff, null, smCallback );
	return;
}

System.out.println("RBT: Action = " + request.getParameter("action") + ", SUB_ID = " + request.getParameter("msisdn") + " type "+ 
request.getParameter("type"));

if(CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SMS_ACT_PROMO_PREFIX", null) != null) 
{ 
	smsPromoPrefix =CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SMS_ACT_PROMO_PREFIX", null).getValue().toLowerCase(); 
} 


if(rbtDaemon != null)
{
	
	strSubID = request.getParameter("msisdn");
	strSrvKey = request.getParameter("srvkey");
	strStatus = request.getParameter("status");
	strRefID = request.getParameter("refid");
	strAction = request.getParameter("action");
	strDate = request.getParameter("date");
	strTime = request.getParameter("time");
	strType = request.getParameter("type");
	strAmount = request.getParameter("amount_charged");
	strInfo = request.getParameter("info");
	strMode = request.getParameter("mode");
	strChangedServiceKey = request.getParameter("charge_keyword");
	strFailureInfo=request.getParameter("CYCLE_RESPONSE");
	strReason = request.getParameter("reason");
	if(strReason != null && (strReason.trim().length() == 0 || strReason.trim().equalsIgnoreCase("null")))
		strReason = null;
	strReasonCode = request.getParameter("reason_code");
	strCurrentSubStatus = request.getParameter("cur_status");
	strEventKey = request.getParameter("eventkey");
	strTransId = request.getParameter("trans_id");
	cosID = request.getParameter("cosid");
	strProbeResult = request.getParameter("probe_result");
	requestType = request.getParameter("reqtype");
	offerID = request.getParameter("offerid");
	rtKey = request.getParameter("rtkey");
	sbnID = request.getParameter("sbn_id");
	retry = request.getParameter("isInRetry");	
	insuspension=request.getParameter("insuspension");
	strNextBillingDate = request.getParameter("nextchargingdate");
	
	
	
	
	//RBT-12906 - Resubscription callback
	if(strSubID != null)
	{
		String countryCodePrefix = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "SM_MSISDN_PREFIX", "").getValue();
		if (!countryCodePrefix.equalsIgnoreCase("") && strSubID.startsWith(countryCodePrefix))
		{
			strSubID = strSubID.substring(countryCodePrefix.length(), strSubID.length());	
			System.out.println("SubscriberID >"+strSubID);
		}
		subscriber = rbtDBManager.getSubscriber(strSubID);	
	}
	if(strMode != null && strAction != null && ((null==subscriber || (null!=subscriber && subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATED)) || RBTDaemonHelper.smInitiatedDeactAllowedModes.contains(strMode)) && strAction.trim().equalsIgnoreCase("DCT"))) 
	{ 
	           strAction = "REN"; 
	           strStatus = "FAILURE";
	} 
	//Added by Sreekar for activation suspension RBT-4183
	if(strAction != null && strStatus !=null && strStatus.trim().equalsIgnoreCase("SUS") && strAction.trim().equalsIgnoreCase("ACT")) 
	{ 
	           strAction = "SUS"; 
	           strStatus = "BAL-LOW"; 
	}
	//Added by Sreekar for activation suspension RBT-4183
	if(strAction != null && strStatus !=null && strStatus.trim().equalsIgnoreCase("GRC") && strAction.trim().equalsIgnoreCase("ACT"))
	{
		strStatus = "GRACE";
	}
	if(strStatus != null && strAction != null && strStatus.trim().equalsIgnoreCase("SUCCESS") && strAction.trim().equalsIgnoreCase("AUPG")) 
	{ 
	           strAction = "REN"; 
	           strStatus = "SUCCESS"; 
			   strSrvKey = request.getParameter("charge_keyword");
	}
	if(strStatus != null && strStatus.trim().equalsIgnoreCase("SUCCESS"))
	{
			if(strAction != null  && (strAction.trim().equalsIgnoreCase("ACT") || strAction.trim().equalsIgnoreCase("REN"))) 
		    { 
					Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","SUSPEND_BASE_FOR_LOW_AMOUNT");
					
					if (param != null) {
						if(strAmount != null && strAmount.indexOf(".") != -1) {
							strAmount = strAmount.substring(0, strAmount.indexOf("."));
						}
						
						if(strAmount != null && strAmount.equals("1") && param.getValue().equalsIgnoreCase("TRUE"))
						{
								strAction = "SUS"; 
								strStatus = "FAILURE";
							
						}
				}
			}
			else if(strAction.trim().equalsIgnoreCase("SUS"))
			{
					strStatus = "FAILURE"; 
			}
	   }
	   
if(null!=subscriber){
			
	if(!rbtDBManager.isSubscriberDeactivated(subscriber))
	{
		isValidPrefix = true;
		circleID = subscriber.circleID();
		if(circleIDFromPrism!=null && !circleIDFromPrism.equalsIgnoreCase(circleID)){
			circleID = circleIDFromPrism;
			SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
			if (sitePrefix != null && sitePrefix.getSiteUrl() == null)
				isValidPrefix = true;
			else
				isValidPrefix = false;
		}
		strSubID = subscriber.subID();
	}
	else
	{
		if(circleIDFromPrism!=null){
			circleID = circleIDFromPrism;
			SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
			if (sitePrefix != null && sitePrefix.getSiteUrl() == null)
				isValidPrefix = true;
			else
				isValidPrefix = false;
		} else {
			SubscriberDetail sub = RbtServicesMgr.getSubscriberDetail(new MNPContext(strSubID,strMode != null ? strMode.trim() : null));
			if(sub != null){
				isValidPrefix = sub.isValidSubscriber();
				circleID = sub.getCircleID();
			}
		}
	}
	
	
	boolean isRRBTSystem = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "RRBT_SYSTEM", "FALSE").getValue().equalsIgnoreCase("TRUE");
	boolean isPRECALLSystem = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "PRECALL_SYSTEM", "FALSE").getValue().equalsIgnoreCase("TRUE");
	if (isRRBTSystem)
	{
		if (strSrvKey != null && strSrvKey.contains("_RRBT"))
			strSrvKey = strSrvKey.replace("_RRBT", "");
		if (strChangedServiceKey != null && strChangedServiceKey.contains("_RRBT"))
			strChangedServiceKey = strChangedServiceKey.replace("_RRBT", "");
	}else if(isPRECALLSystem){
		if (strSrvKey != null && strSrvKey.contains("_PRECALL"))
			strSrvKey = strSrvKey.replace("_PRECALL", "");
		if (strChangedServiceKey != null && strChangedServiceKey.contains("_PRECALL"))
			strChangedServiceKey = strChangedServiceKey.replace("_PRECALL", "");
	}
}	
if(strAction != null && strAction.equalsIgnoreCase("DIRACT"))
{
	if(strType != null)
	{
		if(strType.equalsIgnoreCase("p"))
		{
			prepaidYes = "y";
		}
		else if(strType.equalsIgnoreCase("b"))
		{
			prepaidYes = "n";
		}
		else
		{
			if(CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "DEFAULT_SUB_TYPE", "POSTPAID").getValue().toUpperCase().startsWith("PRE"))
			{
				prepaidYes = "y";
			}
			else
			{
				prepaidYes = "n";
			}
		}
	}
	else
	{
		if(CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "DEFAULT_SUB_TYPE", "POSTPAID").getValue().toUpperCase().startsWith("PRE"))
		{
			prepaidYes = "y";
		}
		else
		{
			prepaidYes = "n";
		}
	}
	
	if(strSubID != null)
	{
		if(isValidPrefix)
		{
			if(rbtDBManager.isSubscriberActivated(subscriber))
			{
				X_ONMOBILE_REASON = "ALREADYACTIVE";
			}
			else 
			{
				if(strInfo != null)
				{
					StringTokenizer stk = new StringTokenizer(strInfo, "|"); 
					while(stk.hasMoreTokens())
					{
						String next = stk.nextToken();
						if(next.startsWith("cosid"))
						{
							cosID = next.substring(6);	
						}
						if(next.startsWith("winresponse"))
						{
							winresponse=next.substring(12);	
						}
						
					}
				}
				
				if(cosID != null)
				{
					cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID, circleID);
					if(cosDetail != null)
					{
						subscriberDetail.put("SUBSCRIBER_ID",strSubID);
						subscriberDetail.put("ACT_BY",strMode);
						subscriberDetail.put("ACT_INFO",strIP);
						subscriberDetail.put("COS_DETAIL",cosDetail);
						subscriberDetail.put("IS_PREPAID",prepaidYes);
						subscriberDetail.put("WINRESPONSE",winresponse);
						subscriberDetail.put("CIRCLE_ID",circleID);
						boolean res = rbtDBManager.activateSubscriber(subscriberDetail);
						if(res)
						{
							boolean realTime = false;
							String realTimeSelection = null;
							Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "REAL_TIME_SELECTIONS");
							if(param != null && param.getValue() != null)
								realTimeSelection = param.getValue();
							if(realTimeSelection != null)
								realTime = realTimeSelection.equalsIgnoreCase("TRUE");
							rbtDBManager.smUpdateSelStatusSubscriptionSuccess(strSubID, realTime, circleID);
							X_ONMOBILE_REASON = "SUCCESS";
						}
						else
						{
							X_ONMOBILE_REASON = "FAILURE";
						}
					}
					else
					{
						X_ONMOBILE_REASON = "INVALID_PARAMETER";
					}
				}
				else
				{
					X_ONMOBILE_REASON = "MISSING_PARAMETER";	
				}
			
			}
		}
		else
		{
			X_ONMOBILE_REASON = "INVALID_PARAMETER";
		}
	}
	else
	{
		X_ONMOBILE_REASON = "MISSING_PARAMETER";
	}

}
else if (strAction != null && strAction.equalsIgnoreCase("confirmCharge"))
{
	X_ONMOBILE_REASON = rbtDaemon.processConfirmCharge(strSubID, strRefID);
}
else if(null==subscriber){
	X_ONMOBILE_REASON = "INVALID|SUBSCRIPTION DOES NOT EXIST";
} 
else if(strAction != null && strSubID != null && strType != null && strSrvKey != null)
{
	String classType = null;
	if(strSrvKey.startsWith("RBT_ACT"))
	{
		classType = strSrvKey.substring(8);
		
		//added for Upgradation to convert SubscriptionClass to new one.
		if((strAction.trim().equalsIgnoreCase("UPG") || strAction.trim().equalsIgnoreCase("AUPG")) && 
					   strChangedServiceKey != null && 
					   strChangedServiceKey.startsWith("RBT_ACT")){
			classType = strChangedServiceKey.substring(8);	
		}//upgradation ends

	}
	else if(strSrvKey.startsWith("RBT_SEL") || strSrvKey.startsWith("RBT_SET"))
	{
		if(strSrvKey.indexOf("_RBT_ACT") != -1)
			classType = strSrvKey.substring(8, strSrvKey.indexOf("_RBT_ACT"));
		else
			classType = strSrvKey.substring(8);

		if((strAction.trim().equalsIgnoreCase("UPG") || strAction.trim().equalsIgnoreCase("AUPG")) && 
				   strChangedServiceKey != null && 
				   strChangedServiceKey.startsWith("RBT_SEL")){
			classType = strChangedServiceKey.substring(8);	
		}
	}
	else if(strSrvKey.startsWith("RBT_PACK"))
	{
		classType = strSrvKey.substring(9);

	}

	if(isValidPrefix)
	{
		X_ONMOBILE_REASON = "ERROR";
		if(strRefID != null && strRefID.startsWith("RBTGIFT"))
		{
			if(classType!= null && classType.indexOf("_GIFT") != -1)
				classType = classType.substring(0, classType.indexOf("_GIFT"));
		
			X_ONMOBILE_REASON =  rbtDaemon.processGift(strSubID, strStatus, strRefID, strAmount, classType, strEventKey, strTransId,circleIDFromPrism);
		}
		else if(strSrvKey.startsWith("RBT_SEL_") || strSrvKey.startsWith("RBT_SET_") || strSrvKey.startsWith("RBT_PACK_"))
		{
			if(strReason != null && strReason.startsWith("REFUND"))
			{
				X_ONMOBILE_REASON = rbtDaemon.refundSelection(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strReason,circleIDFromPrism);
			}
			else 
			{   
				boolean isODAPackRequest = false;
				if(CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "ENABLE_ODA_PACK_PLAYLIST_FEATURE",												     "FALSE").getValue().equalsIgnoreCase("TRUE") && strSrvKey.startsWith("RBT_PACK_")){
					isODAPackRequest =rbtDBManager.isODAPackRequest(strSubID,strRefID);
				}
				
				if(isODAPackRequest){
					X_ONMOBILE_REASON = rbtDaemon.packODASelection(strSubID, strAction, strDate + strTime,strStatus, strRefID, strType, strAmount ,
							classType,strReason, strReasonCode, sbnID);
				}
				else 
				{
					 CallbackRequest callbackReqObj=new CallbackRequest();
						callbackReqObj.setStrSubID(strSubID);
						callbackReqObj.setAction(strAction);
						callbackReqObj.setChargedDate(strDate + strTime);
						callbackReqObj.setStatus(strStatus);
						callbackReqObj.setRefID(strRefID);
						callbackReqObj.setType(strType);
						callbackReqObj.setAmountCharged(strAmount);
						callbackReqObj.setClassType(classType);
						callbackReqObj.setReason(strReason);
						callbackReqObj.setReasonCode(strReasonCode);
						callbackReqObj.setSbnID(sbnID);
						callbackReqObj.setSys_mode(strMode);
						callbackReqObj.setCircleIDFromPrism(circleIDFromPrism);
						callbackReqObj.setStrNextBillingDate(strNextBillingDate);
						
					cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getSmsKeywordCosDetail(classType, circleID, strType); // pass type,circle ID
					if(rbtDBManager.isPackRequest(cosDetail)) {
						X_ONMOBILE_REASON = rbtDaemon.packSelection(strSubID, strAction, strDate + strTime,strStatus, strRefID, strType, strAmount ,
						classType,strReason, strReasonCode, sbnID,circleIDFromPrism);
					}
					else if(CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType) == null){
					 if(rbtDBManager.isPackRequest(cosDetail)) {
						X_ONMOBILE_REASON = rbtDaemon.packSelection(strSubID, strAction, strDate + strTime,strStatus, strRefID, strType, strAmount ,
						classType,strReason, strReasonCode, sbnID,circleIDFromPrism);
					 }
					 else{
						//X_ONMOBILE_REASON = rbtDaemon.selection(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strReason, strReasonCode, sbnID, strMode,circleIDFromPrism);
					 	X_ONMOBILE_REASON = rbtDaemon.selection(callbackReqObj);
					 }
				    }
					else if(rbtDaemon.isChargePerCallCallback(classType, subscriber)) 
					{
						// Incase of charge per call callbacks, refId will not be passed. 
						strRefID = subscriber.refID();
						classType = subscriber.subscriptionClass();
						
						String m_strActionDeactivation = "REN";
                               
						if (m_strActionDeactivation.equalsIgnoreCase(strAction) && "FAILURE".equalsIgnoreCase(strStatus)) {
							Parameters delelectionsOnDeactParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON,
											"DEL_SELECTION_ON_DEACT", "TRUE");
							String deactivatedBy = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
											"CALLBACKS_DEACTIVATE_MODE", "DAEMON");
  							logger.info("******************** m_strActionDeactivation " + m_strActionDeactivation + " strAction " + strAction + " strStatus " + strStatus+", deactivatedBy: "+deactivatedBy);

							boolean delelectionsOnDeact = delelectionsOnDeactParam.getValue().trim().equalsIgnoreCase("TRUE");
							rbtDBManager.deactivateSubscriber(strSubID,
									deactivatedBy, null, delelectionsOnDeact, true,
									true, false,
									true, subscriber.rbtType(),
									subscriber, null, null);
							rbtDBManager.updateCircleIdForSubscriber(true,
									strSubID, circleIDFromPrism, strRefID, null);
                            X_ONMOBILE_REASON = "SUCCESS";
						} else {
						
							X_ONMOBILE_REASON = rbtDaemon.subscription(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strFailureInfo, strReason, strReasonCode, strCurrentSubStatus, requestType, rtKey, retry, strMode, insuspension,circleIDFromPrism, strNextBillingDate);
						}
					}
					else 
					{
						//X_ONMOBILE_REASON = rbtDaemon.selection(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strReason, strReasonCode, sbnID, strMode,circleIDFromPrism);
						X_ONMOBILE_REASON = rbtDaemon.selection(callbackReqObj);
					}
				}
			}
		 }
		else if(strSrvKey.startsWith("RBT_ACT_"))
		{
			X_ONMOBILE_REASON ="FAILURE";
			String smsTextParam =null;
			if(strAction.equals("ACT"))
				smsTextParam="actsmstext_:";
			if(strAction.equals("DCT"))
				smsTextParam = "dctsmstext_:";
			if(strInfo != null && smsTextParam!=null && strInfo.toLowerCase().indexOf(smsTextParam) != -1)
			{	HashMap<String, String> requestParams = Utility.getRequestParamsMap(config, request, response, "Sms");
				requestParams.put("SUBSCRIBER_ID", strSubID.trim());
				requestParams.put("info", strInfo);
				requestParams.put("action", strAction);
				requestParams.put("MODE", strMode);
				requestParams.put("SUBSCRIPTION_CLASS", classType);
				requestParams.put("COSID", cosID);
				requestParams.put("REFID", strRefID);

				X_ONMOBILE_REASON  = AdminFacade.processDirectActivationRequest(requestParams);
			}
	         else {
	        	 // If strReason starts with REFUND call rbtDaemon.RefundSubscription ... else call rbtDaemon.subscription
	        	 if(strReason != null && strReason.startsWith("REFUND"))
	        		 X_ONMOBILE_REASON = rbtDaemon.refundSubscription(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strFailureInfo, strReason,circleIDFromPrism);
	        	 else
	        		 X_ONMOBILE_REASON = rbtDaemon.subscription(strSubID, strAction, strDate + strTime, strStatus, strRefID, strType, strAmount, classType, strFailureInfo, strReason, strReasonCode, strCurrentSubStatus, requestType, rtKey, retry, strMode, insuspension,circleIDFromPrism, strNextBillingDate);
	         }
		}
		else
		{
			X_ONMOBILE_REASON = "FAILURE";
		}
		if(strProbeResult != null && !strProbeResult.equalsIgnoreCase("null"))
			rbtDBManager.concatToActiveMonitor(strSubID, strProbeResult);
	}
	else
	{
		X_ONMOBILE_REASON = "FAILURE";
	}
}
else
{
	X_ONMOBILE_REASON = "FAILURE";
}

//rbtDaemon.writeTrans("CALLBACK-RESPONSE", request.getQueryString(), X_ONMOBILE_REASON, ""+(System.currentTimeMillis() - start));
timeDiff = System.currentTimeMillis() - start;
CallBackLogTool.writeCallBackTransactionLog(parametersMap, X_ONMOBILE_REASON, timeDiff,circleID, null);
}
else
{
	X_ONMOBILE_REASON = "FAILURE";
}

if(X_ONMOBILE_REASON.equalsIgnoreCase("FAILURE"))
{
	response.setStatus(500);
}
out.flush();
out.write(X_ONMOBILE_REASON);
}
catch(Throwable e)
{
	logger.error("", e);
//	out.write(e.getMessage());
//	out.write(e.printStackTrace());
	System.out.println(e);
	e.printStackTrace();
}
%>
