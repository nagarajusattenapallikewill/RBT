/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.promo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.logger.PromotionLogger;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;

/**
 * @author vinayasimha.patil
 */
public class PromoResponseEncoder extends ResponseEncoder implements Constants {
	protected Logger logger = null;
	public String rbtBrandName = null;
	private static Logger appLogger = Logger.getLogger(PromoResponseEncoder.class);
	private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>()
	{
        @Override
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };
	
	/**
	 * @throws Exception
	 */
	public PromoResponseEncoder() throws Exception {
		logger = Logger.getLogger(PromoResponseEncoder.class);
		rbtBrandName = getRBTBrandName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#encode(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String encode(Task task) {
		String response = task.getString(param_response);
		if (response.equalsIgnoreCase("success")) {
			response = Resp_Success;
		} else {
			sendErrorMsg(task);
		}
		logger.info("RBT:: response: " + response);
		Long startTime = 0l;
		if (task.containsKey(param_startTime))
			startTime = Long.parseLong(task.getString(param_startTime));
		if (task.containsKey(param_XML_REQUIRED)) {
			String xmlRequired = task.getString(param_XML_REQUIRED);
			if (xmlRequired.equalsIgnoreCase("true")) {
				Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
				response = getXMLTag(response, subscriber);
			}
		}
		if (response.equals(Resp_InvalidNumber))
			response = Resp_Failure;
		else if (response.equals(Resp_invalidTransID))
			response = "Invalid Request. Already recieved a request with the same TransID : "
					+ task.getString(param_TRANSID) + " and subscriber ID : "
					+ task.getString(param_subscriberID);
		writeTrans(task.getString(param_queryString), response, String.valueOf(System
				.currentTimeMillis()
				- startTime), task.getString(param_ipAddress));
		//hsb processing
		if(task.containsKey(param_HSB_REQUEST))
			response = getHSBResponse(response, task);
		return response;
	}
	
	protected String getHSBResponse(String response, Task task) {
		if(response.equalsIgnoreCase(Resp_Success))
			response = "RESPONSE_STATUS:800:Request Successfully accepted";
		else if(response.equalsIgnoreCase(Resp_userLocked))
			response = "RESPONSE_STATUS:805:Service locked";
		else if(response.equalsIgnoreCase(Resp_AlreadyActive))
			response = "RESPONSE_STATUS:801:service already activated.";
		else if(response.equalsIgnoreCase(Resp_AlreadyInactive) || response.equals(Resp_Inactive))
			response = "RESPONSE_STATUS:803:Service not active/already deactivate.";
		else if(response.equalsIgnoreCase(Resp_liteUserPremiumBlocked))
			response = "RESPONSE_STATUS:806:LITE_USER_PREMIUM_BLOCKED";
		else {
			response = "RESPONSE_STATUS:805:Error Do Not Retry";
			if(task.containsKey(param_actFailed))
				response = "RESPONSE_STATUS:804:There is some problem please try again.";
			else if(task.getTaskAction().equalsIgnoreCase(request_hsb_act) && response.equals(Resp_Failure))
				response = "RESPONSE_STATUS:804:There is some problem please try again.";
		}
		
		return response;
	}

	/**
	 * (input parameters MSISDN=9980590682&REQUEST=STATUS&XML_REQUIRED=TRUE)
	 * 
	 * @author Sreekar
	 * @param message the response
	 * @param serviceKey the subscription class for which the subscription class is requested
	 * @return returns the XML required
	 */
	
	public String getXMLTag(String message,  Subscriber sub) {
		String serviceKey = sub.getSubscriptionClass();
		String value = null;
		String nextBillDateValue = null;
		String clipIdXMLParamValue=null;
		String promoIdXMLParamValue=null;
		String clipIdXMLSelParamValue=null;
		String promoIdXMLSelParamValue=null;
		
		SimpleDateFormat sdf = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
		// JIRAID-RBT-3786: Info URL required
		// JIRAID-RBT-3793 : Need Subscriber Pack details in PROMOTION.JSP 
		// Both has been fixed together using e rbt parameter which is a configuration format for promotion response XML. 
		Parameters responseXMLParam = CacheManagerUtil.getParametersCacheManager().
				getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESPONSE_XML);
		
		Parameters nextBillDateXMLParam  = CacheManagerUtil.getParametersCacheManager().
				getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_NBD_APPEND);
		Parameters clipIdXMLParam  = CacheManagerUtil.getParametersCacheManager().
 		        getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_CLIPID_APPEND);
		Parameters promoIdXMLParam  = CacheManagerUtil.getParametersCacheManager().
	        getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_PROMOID_APPEND);
        
		Parameters clipIdXMLSelParam  = CacheManagerUtil.getParametersCacheManager().
	        getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_SEL_CLIPID_APPEND);
        Parameters promoIdXMLSelParam  = CacheManagerUtil.getParametersCacheManager().
            getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_SEL_PROMOID_APPEND);

		Parameters clipIdPromoIdConfStatus  = CacheManagerUtil.getParametersCacheManager().
                         getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESOPNSEXML_CLIPID_APPEND_STATUS);
        String str[]=null;
        List<String> xmlClipIdAppendStatusList = null;
		if(clipIdPromoIdConfStatus!=null&&clipIdPromoIdConfStatus.getValue()!=null){
		       str = clipIdPromoIdConfStatus.getValue().split(",");
		}
		if(str!=null){ 
		     xmlClipIdAppendStatusList = Arrays.asList(str);
		}
		logger.info("RBT:: response: message " +message);
		
		if(null != responseXMLParam) {
		    value = responseXMLParam.getValue().trim();
		}
		
		if(null!=clipIdXMLParam && xmlClipIdAppendStatusList!=null && xmlClipIdAppendStatusList.contains(sub.getStatus())){
			  clipIdXMLParamValue=clipIdXMLParam.getValue().trim();
		}
		if(null!=clipIdXMLParamValue){
			 int position = value.indexOf(iRBTConstant.kPromotionXMLEndTag);
			 StringBuilder tempBuilder = new StringBuilder(value);
			 tempBuilder.insert(position, clipIdXMLParamValue);
		     value = tempBuilder.toString() ;
		}
		
		if(null!=promoIdXMLParam && xmlClipIdAppendStatusList!=null && xmlClipIdAppendStatusList.contains(sub.getStatus())){
			promoIdXMLParamValue=promoIdXMLParam.getValue().trim();
		}
		if(null!=promoIdXMLParamValue){
			 int position = value.indexOf(iRBTConstant.kPromotionXMLEndTag);
			 StringBuilder tempBuilder = new StringBuilder(value);
			 tempBuilder.insert(position, promoIdXMLParamValue);
		     value = tempBuilder.toString() ;
		}
		if(null!=clipIdXMLSelParam && xmlClipIdAppendStatusList!=null && xmlClipIdAppendStatusList.contains(sub.getStatus())){
			  clipIdXMLSelParamValue=clipIdXMLSelParam.getValue().trim();
		}
		if(null!=clipIdXMLSelParamValue){
			 int position = value.indexOf(iRBTConstant.kPromotionXMLEndTag);
			 StringBuilder tempBuilder = new StringBuilder(value);
			 tempBuilder.insert(position, clipIdXMLSelParamValue);
		     value = tempBuilder.toString() ;
		}
		
		if(null!=promoIdXMLSelParam && xmlClipIdAppendStatusList!=null && xmlClipIdAppendStatusList.contains(sub.getStatus())){
			promoIdXMLSelParamValue=promoIdXMLSelParam.getValue().trim();
		}
		if(null!=promoIdXMLSelParamValue){
			 int position = value.indexOf(iRBTConstant.kPromotionXMLEndTag);
			 StringBuilder tempBuilder = new StringBuilder(value);
			 tempBuilder.insert(position, promoIdXMLSelParamValue);
		     value = tempBuilder.toString() ;
		}
		
		 if(null != nextBillDateXMLParam) {
		    nextBillDateValue = nextBillDateXMLParam.getValue().trim();
		 }
		 if(null != nextBillDateValue ) {
			 int position = value.indexOf(iRBTConstant.kPromotionXMLEndTag);
			 StringBuilder tempBuilder = new StringBuilder(value);
			 tempBuilder.insert(position, nextBillDateValue);
		    value = tempBuilder.toString() ;
		 }
		if (message != null && message.indexOf(iRBTConstant.kRootXMLStartTag) > -1) {
			return message;
		 }
		
		// Added by Sreekar for JIRA RBT-4589. XML response would be returned for configured status. If no status is
		// configured then XML would be returned for all status'
		List<String> requiredStatusList = null;
		Parameters requiredStatusParam = CacheManagerUtil.getParametersCacheManager().
				getParameter(iRBTConstant.WEBSERVICE, iRBTConstant.PROMOTION_STATUS_RESPONSE_XML_REQUIRED_STATUS);
		if(requiredStatusParam != null)
			requiredStatusList = Tools.tokenizeArrayList(requiredStatusParam.getValue(), ",");
		
		if (message != null && (requiredStatusList == null || requiredStatusList.contains(sub.getStatus()))) {
			value = value.replace(iRBTConstant.kRBT_BRAND_NAME, rbtBrandName);
			value = value.replace(iRBTConstant.kSTATUS, message);
			if(value.contains(iRBTConstant.kCOSID)) {
				if(null == sub.getCosID()) {
					value = value.replace(iRBTConstant.kCOSID, iRBTConstant.kEmptyString);
				} else {
					value = value.replace(iRBTConstant.kCOSID, sub.getCosID());
				}
			}
			
			//Added by Sreekar as part of RBT-4589
			if (value.contains(iRBTConstant.kUDS)) {//JIRA-ID: RBT-13626
				String udsOnStr = "FALSE";
				if (sub.getUserInfoMap() != null) {
					String premiumChargeClass = null;
					premiumChargeClass = com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isUDSUser(sub.getUserInfoMap(),true);
					udsOnStr = premiumChargeClass != null ? iRBTConstant.UDS_OPTIN_ON
							: iRBTConstant.UDS_OPTIN_OFF;
				}
				//RBT-14022: to send uds type in xml rsponse
				String  udsInfo = "true";
					if(sub.getUserInfoMap().containsKey(iRBTConstant.UDS_OPTIN))
						udsInfo = sub.getUserInfoMap().get(iRBTConstant.UDS_OPTIN);
				value = value
						.replace(
								iRBTConstant.kUDS,
						(udsOnStr != null && !udsOnStr
								.equals(iRBTConstant.UDS_OPTIN_OFF)) ? udsInfo.toUpperCase()
								: iRBTConstant.NO);
			}
			
			if(RBTDeploymentFinder.isRRBTSystem()) { 
				value = value.replace(iRBTConstant.kSERVICE_KEY, serviceKey + 
						iRBTConstant.kUnderScoreString + iRBTConstant.USER_TYPE_RRBT);
			    
			} else {
				value = value.replace(iRBTConstant.kSERVICE_KEY, serviceKey);
			}
			
			if(value.contains(iRBTConstant.kNEXTBILLDATE)) {
			    if(sub != null && sub.getNextBillingDate() != null) {
				    Date nextDate =  sub.getNextBillingDate();
				    logger.info("Next billing date is " +nextDate );
				    String nextBillingDate = null;
				      try {
					      nextBillingDate = sdf.format(nextDate);
				      } catch(Exception e) {
					      logger.error(iRBTConstant.kEmptyString,e);
					      nextBillingDate = null;
				      }
				      
				      if(null != nextBillingDate) {
					     value = value.replace(iRBTConstant.kNEXTBILLDATE, nextBillingDate);
				      } else {
				    	  value = value.replace(nextBillDateValue, iRBTConstant.kEmptyString);
				      }
			    } else {
			    	value = value.replace(nextBillDateValue, iRBTConstant.kEmptyString);
			    }
		    }
			if(value.contains(iRBTConstant.kCLIPID)) {
				if(sub!=null){
					String clipIds = activeDownloadsClipIdsOfSubscriber(sub.getSubscriberID());
					if(null!=clipIds){
						value = value.replace(iRBTConstant.kCLIPID, clipIds);
					}else{
						value = value.replace(iRBTConstant.kCLIPID, iRBTConstant.kEmptyString);
					}
				}
			}else{
				if(clipIdXMLParamValue != null)
					value = value.replace(clipIdXMLParamValue, iRBTConstant.kEmptyString);
			}
			if(value.contains(iRBTConstant.kPROMOID)) {
				if(sub!=null){
					String promoIds = activeDownloadsPromoIdsOfSubscriber(sub.getSubscriberID());
					if(null!=promoIds){
						value = value.replace(iRBTConstant.kPROMOID, promoIds);
					}else{
						value = value.replace(iRBTConstant.kPROMOID, iRBTConstant.kEmptyString);
					}
				}
			}else{
				if(promoIdXMLParamValue != null)
					value = value.replace(promoIdXMLParamValue, iRBTConstant.kEmptyString);
			}
			if(value.contains(iRBTConstant.kSELCLIPID)) {
				if(sub!=null){
					String clipIds = activeSelectionsClipIdsOfSubscriber(sub.getSubscriberID());
					if(null!=clipIds){
						value = value.replace(iRBTConstant.kSELCLIPID, clipIds);
					}else{
						value = value.replace(iRBTConstant.kSELCLIPID, iRBTConstant.kEmptyString);
					}
				}
			}else{
				if(clipIdXMLSelParamValue != null)
					value = value.replace(clipIdXMLSelParamValue, iRBTConstant.kEmptyString);
			}
			if(value.contains(iRBTConstant.kSELPROMOID)) {
				if(sub!=null){
					String promoIds = activeSelectionsPromoIdsOfSubscriber(sub.getSubscriberID());
					if(null!=promoIds){
						value = value.replace(iRBTConstant.kSELPROMOID, promoIds);
					}else{
						value = value.replace(iRBTConstant.kSELPROMOID, iRBTConstant.kEmptyString);
					}
				}
			}else{
				if(promoIdXMLSelParamValue != null)
					value = value.replace(promoIdXMLSelParamValue, iRBTConstant.kEmptyString);
			}
			
			if(value.contains(iRBTConstant.kPROMPT)){
				if(sub!=null && sub.getUserInfoMap()!=null){
					String prompt = sub.getUserInfoMap().get("PROMPT");
					if(prompt!=null)
						value = value.replace(iRBTConstant.kPROMPT, prompt);
					else
						value = value.replace(iRBTConstant.kPROMPT, "NA");
				}else{
						value = value.replace(iRBTConstant.kPROMPT, "NA");
				}
			}
			
	    } else {
			value = iRBTConstant.kRootXMLTag ;
		}	
	  return value;
	}
   
	private String activeDownloadsClipIdsOfSubscriber(String subscriberId){
    	RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
    	Library library = RBTClient.getInstance().getLibrary(rbtDetailsRequest);
    	if(library!=null){
    		Downloads download = library.getDownloads();
    		if(download!=null){
    			Download []downloads = download.getDownloads();
    			if(downloads!=null){
    				StringBuilder strBuilder = new StringBuilder();
    				for(int i=0;i<downloads.length;i++){
    		          	strBuilder.append(downloads[i].getToneID());
    		          	if(i != (downloads.length - 1))
    		          		strBuilder.append(",");
    				}
    				return strBuilder.toString();
    			}
    		}
    	}
    	return null;
    }
   
	private String activeDownloadsPromoIdsOfSubscriber(String subscriberId) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Library library = RBTClient.getInstance().getLibrary(rbtDetailsRequest);
		if (library != null) {
			Downloads download = library.getDownloads();
			if (download != null) {
				Download[] downloads = download.getDownloads();
				if (downloads != null) {
					StringBuilder strBuilder = new StringBuilder();
					for (int i = 0; i < downloads.length; i++) {
						int clipId = downloads[i].getToneID();
						try {
							strBuilder.append(RBTCacheManager.getInstance().getClip(clipId).getClipPromoId());
							if (i != (downloads.length - 1))
								strBuilder.append(",");
						} catch (Exception e) {
							logger.error("Exception while getting promo id for clip id:" + clipId);
						}
					}
					return strBuilder.toString();
				}
			}
		}
		return null;
	}
	private String activeSelectionsClipIdsOfSubscriber(String subscriberId){
    	RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
    	Library library = RBTClient.getInstance().getLibrary(rbtDetailsRequest);
    	if(library!=null){
    		Settings settings = library.getSettings();
    		if(settings!=null){
    			Setting []setting = settings.getSettings();
    			if(setting!=null){
    				Set<String> clipIdsSet = new HashSet<String>();
    				StringBuilder strBuilder = new StringBuilder();
    				for(int i=0;i<setting.length;i++){
    					clipIdsSet.add(setting[i].getToneID()+"");
    		        }
    				String clipIds[] = clipIdsSet.toArray(new String[0]);
    				for(int j=0;clipIds!=null && j<clipIds.length;j++){
    					strBuilder.append(clipIds[j]);
    					if(j!=clipIds.length-1)
    					   strBuilder.append(",");
    				}
    				return strBuilder.toString();
    			}
    		}
    	}
    	return null;
    }
   
	private String activeSelectionsPromoIdsOfSubscriber(String subscriberId) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Library library = RBTClient.getInstance().getLibrary(rbtDetailsRequest);
		if (library != null) {
			Settings settings = library.getSettings();
    		if(settings!=null){
    			Setting []setting = settings.getSettings();
    			if(setting!=null){
    				Set<String> set = new HashSet<String>();
    				StringBuilder strBuilder = new StringBuilder();
					for (int i = 0; i < setting.length; i++) {
						int clipId = setting[i].getToneID();
						try {
							set.add(RBTCacheManager.getInstance().getClip(clipId).getClipPromoId());
							
						} catch (Exception e) {
							logger.error("Exception while getting promo id for clip id:" + clipId);
						}
					}
					String clipPromoIds[] = set.toArray(new String[0]);
					for(int j=0;clipPromoIds!=null && j<clipPromoIds.length;j++){
    					strBuilder.append(clipPromoIds[j]);
    					if(j!=clipPromoIds.length-1)
    					    strBuilder.append(",");
    				}
					return strBuilder.toString();
				}
			}
		}
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#getContentType(java.util.HashMap)
	 */
	public String getContentType(HashMap<String, String> requestParams) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#getGenericErrorResponse(java.util.HashMap)
	 */
	public String getGenericErrorResponse(HashMap<String, String> requestParams) {
		return Resp_Failure;
	}

	protected String getSMSParameter(String paramName) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.SMS, paramName);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return null;
	}

	protected String getCOMMONParameter(String paramName) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, paramName);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return null;
	}

	public String param(String type, String paramName, String defaultVal) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(type, paramName,defaultVal);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return defaultVal;
	}

	public synchronized void writeTrans(String params, String resp, String diff, String ip)
	{
		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(params).append("|").append(resp).append("|").append(diff).append("|").append(ip).append("|");
			stringBuilder.append(formatter.get().format(Calendar.getInstance().getTime()));
			PromotionLogger.getLogger().info(stringBuilder.toString());
		}
		catch(Exception e)
		{
			appLogger.error("Exception", e);
		}
	}
	
	/**
	 * send msg to subscriber in case of any error while activation/selection
	 * @param task
	 * @author laxmankumar
	 */
	protected void sendErrorMsg(Task task) {
		
		String response = task.getString(param_response);  
		String reqType = task.getTaskAction();
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String lang = null;
		if (null != subscriber) {
			lang = subscriber.getLanguage();
		}
		if (null == response) {
			response = FAILURE;
		}
		String mode = null;
		// handles only activation/selection requests
		if (request_activate.equalsIgnoreCase(reqType)) {
			mode = task.getString(param_ACTIVATED_BY);
		} else if (request_selection.equalsIgnoreCase(reqType)) {
			mode = task.getString(param_SELECTED_BY);
		} else {
			logger.warn("Couldn't find task mode..");
			return;
		}
		String type = (mode + "_" + reqType).toUpperCase();
		
		String status = subscriber.getStatus();
		String smsText = null;
		if ("suspended".equalsIgnoreCase(status)) {
			// suppressing the response type 'suspended' so checking it from status
			logger.info("Looking for sms text for mode:" + mode + " reqType:" + reqType + " type:" + type
					+ " status:" + status.toUpperCase() + " lang:" + lang);
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, status.toUpperCase(), lang);
		} else if (request_activate.equalsIgnoreCase(reqType)
				&& "act_pending".equalsIgnoreCase(status)
				&& !Resp_BlackListedNo.equalsIgnoreCase(response)) {
			// suppressing the response type 'act_pending' so checking it from
			// subscriber status for activation requests
			// and black listed number case should not come here, should be handled as default
			logger.info("Looking for sms text for mode:" + mode + " reqType:" + reqType + " type:" + type
					+ " status:" + status.toUpperCase() + " lang:" + lang);
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, status.toUpperCase(), lang);
		} else {
			logger.info("Get the sms text for mode:" + mode + " reqType:" + reqType + " type:" + type
					+ " resp:" + response.toUpperCase() + " lang:" + lang);
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, response.toUpperCase(), lang);
		}
		
		if (null == smsText || "".equals(smsText.trim())) {
			logger.info("Looking for sms text for failure case");
			smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(type, "FAILURE", lang);
			if (null == smsText || "".equals(smsText.trim())) {
				return;
			}
		}
		
		
//		logger.info("Got sms text as '" + smsText + "'" + " Sender:"
//				+ sender.getValue() + " Receiver:" + subscriber.getSubscriberID());
		
		RBTClient rbtClient = RBTClient.getInstance();
		
		
		if(task.getObject(CLIP_OBJ)!=null)
		{
		Clip clip=(Clip)task.getObject(CLIP_OBJ);
		if(clip==null)
			smsText = Utility.findNReplaceAll(smsText, "%SONG_NAME", "");	
		else 
		smsText = Utility.findNReplaceAll(smsText, "%SONG_NAME",clip.getClipName());
		}

//		Parameters sender = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SENDER_NUMBER");
		Parameters sender = null;
		String senderId = null;
		if (task.containsKey(SENDER_ID)) 
		{			
			senderId = task.getString(SENDER_ID).trim();
			logger.info("Sender ID is configured from third party end SENDER_ID : " +  senderId);
		}
		else
		{
			sender = CacheManagerUtil.getParametersCacheManager().getParameter("SMS", "SENDER_NUMBER");
			senderId = sender.getValue();
		}
		
		logger.info("Got sms text as '" + smsText + "'" + " Sender:"
				+ senderId + " Receiver:" + subscriber.getSubscriberID());

		
//		UtilsRequest utilsRequest = new UtilsRequest(sender.getValue(), subscriber.getSubscriberID(), smsText);
		UtilsRequest utilsRequest = new UtilsRequest(senderId, subscriber.getSubscriberID(), smsText);
		rbtClient.sendSMS(utilsRequest);
		if(utilsRequest.getResponse().equalsIgnoreCase("success")) {
			logger.info("Successfully sent SMS");
		} else {
			logger.info("Failed to send SMS");
		}
	}
	
	private String getRBTBrandName()
	{
		String brandName = "RBT";
		if(RBTDeploymentFinder.isRRBTSystem()) {
			brandName = "RRBT";
		} else if(RBTDeploymentFinder.isPRECALLSystem()) {
			brandName = "PRECALL";
		}
			
		Parameters systemTypeNameMapParam = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SYSTEM_TYPE_NAME_MAP");
		HashMap<String, String> systemNameMap = new HashMap<String, String>();
		systemNameMap.put("RBT", "RBT");
		systemNameMap.put("RRBT", "RBT");
		systemNameMap.put("PRECALL", "RBT");
		if(systemTypeNameMapParam != null)
		{
			String value = systemTypeNameMapParam.getValue();
			if(value != null)
			{
				StringTokenizer stkParent = new StringTokenizer(value,";");
				{
					while(stkParent.hasMoreTokens())
					{
						StringTokenizer stkChild = new StringTokenizer(stkParent.nextToken(),",");
						if(stkChild.countTokens() == 2)
						{
							String systemType = stkChild.nextToken().trim();
							String systemName = stkChild.nextToken().trim();
							systemNameMap.put(systemType, systemName);
						}
					}
				}
			}	
		}
		if(systemNameMap.containsKey(brandName) && systemNameMap.get(brandName) != null)
			return systemNameMap.get(brandName);
		return "RBT";
	}
	
}