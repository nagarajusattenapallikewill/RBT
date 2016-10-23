/**
 * OnMobile Ring Back Tone 
 * 
 * $Author: manjunatha.c $
 * $Id: AdminFacade.java,v 1.237 2015/04/29 11:08:47 manjunatha.c Exp $
 * $Revision: 1.237 $
 * $Date: 2015/04/29 11:08:47 $
 */

package com.onmobile.apps.ringbacktones.provisioning;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.bean.RBTRto;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class AdminFacade implements Constants {
	private static HashMap<String, Processor> processors = new HashMap<String, Processor>();
	private static HashMap<String, ResponseEncoder> responseEncoders = new HashMap<String, ResponseEncoder>();
	private static Object processorSync = new Object();
	private static Object responseEncoderSync = new Object();
	private static Set<String> runningRequests = Collections
			.synchronizedSet(new HashSet<String>());
	private static SimpleDateFormat sdfStatus = new SimpleDateFormat("yyyyMMdd");
//	private static Set<String> consentConfModes = new HashSet<String>();
	private static Logger logger = Logger.getLogger(AdminFacade.class);
	
	private static List<String> configuredVendorIdsList = null; 

	public static Processor getProcessorObject(String api) {
		Processor processor = null;
		api = api.toUpperCase();
		processor = processors.get(api);

		if (processor == null) {
			try {
				synchronized (processorSync) {
					if (processors.containsKey(api))
						return processors.get(api);
					String processorClassName = "com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor";
					Parameters processorParam = CacheManagerUtil
							.getParametersCacheManager().getParameter(
									iRBTConstant.PROVISIONING,
									"PROCESSOR_CLASS_" + api,
									processorClassName);
					if (processorParam != null)
						processorClassName = processorParam.getValue().trim();

					logger.info("RBT:: processorClassName: "
							+ processorClassName);
					Class<?> processorClass = Class.forName(processorClassName);
					processor = (Processor) processorClass.newInstance();
					processors.put(api, processor);
				}
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
			}
		}
		return processor;
	}

	public static ResponseEncoder getResponseEncoderObject(String api) {
		ResponseEncoder responseEncoder = null;
		api = api.toUpperCase();
		responseEncoder = responseEncoders.get(api);

		if (responseEncoder == null) {
			try {
				synchronized (responseEncoderSync) {
					if (responseEncoders.containsKey(api))
						return responseEncoders.get(api);
					String responseEncoderClassName = "com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceResponseEncoder";
					Parameters responseEncoderParam = CacheManagerUtil
							.getParametersCacheManager().getParameter(
									iRBTConstant.PROVISIONING,
									"RESPONSEENCODER_CLASS_" + api,
									responseEncoderClassName);
					if (responseEncoderParam != null)
						responseEncoderClassName = responseEncoderParam
								.getValue().trim();

					logger.info("RBT:: responseEncoderClassName: "
							+ responseEncoderClassName);
					Class<?> responseEncoderClass = Class
							.forName(responseEncoderClassName);
					responseEncoder = (ResponseEncoder) responseEncoderClass
							.newInstance();
					responseEncoders.put(api, responseEncoder);
				}
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
			}

		}
		return responseEncoder;
	}

	public static String processPromotionRequest(
			HashMap<String, String> requestParams) throws Exception {
		
		if(!Utility.isValidModeIPConfigured(requestParams.get(WebServiceConstants.param_modeConsent),requestParams.get(WebServiceConstants.param_ipAddressConsent))) {
			return INVALID;
		}
		
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		logger.info("Processing promotion request. request params: "+requestParams);

		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		Task task = processor.getTask(requestParams);
		requestParams.put("TASK_ACTION", task.getTaskAction());
		response = processor.validateParameters(task);
		// date is required for Status api required by a certain airtel 3rd
		// party
		if (task.getTaskAction() != null
				&& task.getTaskAction().equalsIgnoreCase("status")) {
			getDate(task, requestParams);
			getPackCharge(task, requestParams);
		}
		if (!response.equalsIgnoreCase("VALID")) {
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		
		if (task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
			String tpCGID = task.getString(iRBTConstant.EXTRA_INFO_TPCGID);
			String minMaxLenght = RBTParametersUtils.getParamAsString("DOUBLE_CONFIRMATION", "MIN_MAX_LENGTH_TPCGID", "1,40");
			if(minMaxLenght != null) {
				String[] strArrays = minMaxLenght.split(",");
				int minLength = Integer.parseInt(strArrays[0]);
				int maxLength = Integer.parseInt(strArrays[1]);
				if(!((tpCGID = tpCGID.trim()).length() >= minLength && tpCGID.length() <= maxLength)) {
					task.setObject(param_response,Resp_invalidTpCgid);
					response = responseEncoder.encode(task);
					return response;
				}
			}
		}
		
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);
				response = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
			}
		} else {
			String taskAction = task.getTaskAction();
			logger.info("Promotion request action is: " + taskAction
					+ ", task: " + task);
			if (!runningRequests.contains(task.getString(param_MSISDN))) {
				try {
					runningRequests.add(task.getString(param_MSISDN));
					if (taskAction.equalsIgnoreCase("status"))
						processor.processSubStatusRequest(task);
					if (taskAction.equalsIgnoreCase("check"))
						processor.processSubProfileRequest(task);
					else if (((taskAction.equalsIgnoreCase(action_activate) && task
							.containsKey(param_COSID)) || taskAction
							.equalsIgnoreCase(action_upgrade))) {
						processor.processSongPackRequest(task);
					} else if (taskAction.equalsIgnoreCase(action_topup)) {
						processor.processTopupRequest(task);
					} else if (taskAction.equalsIgnoreCase(action_up_validity))
						processor.upgradeSubscription(task);
					else if (taskAction.equalsIgnoreCase(action_activate)) {
						if (!task.containsKey(param_ACTIVATED_BY))
							task.setObject(param_response,
									Resp_missingParameter);
						else
							processor.processActivation(task);
					} else if (taskAction.equalsIgnoreCase(action_selection)) {
						if (!(task.containsKey(param_TONE_ID)
								|| task.containsKey(param_PROMO_ID)
								|| task.containsKey(param_WAV_FILE)
								|| task.containsKey(param_SMS_ALIAS) || task
								.containsKey(param_PROFILE_NAME)))
							task.setObject(param_response,
									Resp_missingParameter);
						else
							processor.processSelection(task);
					} else if (taskAction.equalsIgnoreCase(action_deactivate)) {
						if (!task.containsKey(param_DEACTIVATED_BY))
							task.setObject(param_response,
									Resp_missingParameter);
						else
							processor.processDeactivation(task);
					} else if (taskAction.equals(action_cricket))
						processor.processFeed(task);
					else if (taskAction.equals(action_meriDhun))
						processor.processMeriDhunRequest(task);
					else if (taskAction.equals(action_suspend))
						processor.processSuspensionRequest(task);
					else if (taskAction.equals(action_resume))
						processor.processResumptionRequest(task);
					else if (taskAction.equalsIgnoreCase(request_block))
						processor.addToBlackList(task);
					else if (taskAction.equalsIgnoreCase(request_unblock))
						processor.removeFromBlackList(task);
					else if (taskAction.equalsIgnoreCase(request_TNB))
						processor.processTNBActivation(task);
					else if (taskAction
							.equalsIgnoreCase(action_delete_selection))
						processor.processDeleteSelection(task);
					else if (taskAction.equalsIgnoreCase(request_upgrade_base))
						processor.processBaseUpgradationRequest(task);
					else if (taskAction.equalsIgnoreCase(request_SHUFFLE)
							&& task.getString("ACTION") != null
							&& task.getString("ACTION").equalsIgnoreCase("ACT"))
						processor.enableRandomization(task);
					else if (taskAction.equalsIgnoreCase(request_SHUFFLE)
							&& task.getString("ACTION") != null
							&& task.getString("ACTION").equalsIgnoreCase("DCT"))
						processor.disableRandomization(task);
					else if (taskAction.equalsIgnoreCase(request_deact_pack))
						processor.processDeactivationPack(task);
					else if (taskAction.equalsIgnoreCase(action_deactivate_tone))
						processor.processDeleteTone(task);

				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
				} finally {
					if (runningRequests.contains(task.getString(param_MSISDN)))
						runningRequests.remove(task.getString(param_MSISDN));
				}
			} else {
				logger
						.info("Promotion jsp request already pending for this subscriber. So returning failure for this : "
								+ task.getString(param_MSISDN));
				task.setObject(param_response, Resp_Failure);
			}
			response = responseEncoder.encode(task);
		}
		logger.info("RBT:: response: " + response);
		return response;
	}
	
	public static String getConsentSelIntegrationResponseXML(HashMap<String, String> requestParams) throws Exception {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);
		Task task = new Task(null, taskSession);
		String action = task.getString(param_action);
		Processor processor = getProcessorObject(requestParams.get(param_api));
		logger.info("Process selection request for  action: " + action
				+ ", task: " + task+ ", rbtProcessor: " + processor);
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				                               .get(param_api));

		String response = ERROR;
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);
				response = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
			}
			 return response;
		} else {
			Subscriber subscriber = Processor.getSubscriber(task);
			boolean isActive = true;
			try {
				if (subscriber == null
						|| (subscriber != null && (subscriber.getStatus().equalsIgnoreCase(
								WebServiceConstants.NEW_USER)
								|| subscriber.getStatus().equalsIgnoreCase(
										WebServiceConstants.DEACTIVE) || subscriber.getStatus()
								.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)))) {
					isActive = false;
				}
				if (isActive) {
					task.setObject(WebServiceConstants.param_subscriberStatus, "Active");
				}
			} catch (Exception ex) {
				logger.error("Exception: " , ex);
			}
			if (!(action.equalsIgnoreCase(WebServiceConstants.action_gift) || action.equalsIgnoreCase(WebServiceConstants.action_acceptGift)) && isActive && subscriber != null) {
				if (!subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
					response = subscriber.getStatus();
					if(task.containsKey(WebServiceConstants.param_useSameResForConsent) && task.getString(WebServiceConstants.param_useSameResForConsent).equalsIgnoreCase(WebServiceConstants.YES)) {
						response = XMLUtils.getStringFromDocument(Utility.getResponseDocument(response));
					}
					return response;
				} else {
					String mode = task.getString(param_mode.toLowerCase());
					Map<String, String> mappedMode = MapUtils.convertToMap(RBTParametersUtils
							.getParamAsString("COMMON", "MODE_MAPPING_FOR_COMVIVA", null), ";", "=",
							",");
					if (mappedMode != null && mappedMode.containsKey(mode))
						task.setObject(param_mode.toLowerCase(), mappedMode.get(mode));
				}
			}
			if (action.equalsIgnoreCase(WebServiceConstants.action_set)) {
				 response = processor.processSelectionConsentIntegration(task);
			} else if (action.equalsIgnoreCase(WebServiceConstants.action_gift)) {
				//To process the consent gift				
				 response = processor.processGiftConsentIntegration(task);
			} else if (action.equalsIgnoreCase(WebServiceConstants.action_acceptGift)) {
				//To process the consent accept accpet gift
				 response = processor.processAcceptGiftConsentIntegration(task);
			}else {
				response = "NOT_ALLOWED";
				if(task.containsKey(WebServiceConstants.param_useSameResForConsent) && task.getString(WebServiceConstants.param_useSameResForConsent).equalsIgnoreCase(WebServiceConstants.YES)) {
					response = XMLUtils.getStringFromDocument(Utility.getResponseDocument(response));
				}
			}
		}
		task.setObject(param_response, response);
		logger.info("ConsentSelIntegrationResponse == "+response);
		return response;
		
	}

	public static String processConsentRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(requestParams.get(param_api));
		Task task = processor.getTask(requestParams);
		String response = processor.validateParameters(task);
		String transId = task.getString(param_transID);
		String songid = task.getString(param_songid);
		String mode = task.getString(param_mode.toLowerCase());
		boolean isConsent = task.getString(param_consent) != null ? task
				.getString(param_consent).equalsIgnoreCase("yes") : false;
		String categoryID = task.getString(param_CATEGORY_ID);
		
		Category category = null;
		if(categoryID != null) {
			category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryID));
			task.setObject(CATEGORY_OBJ, category);
		}		
		
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
        boolean isDirectProcessRequest = false;
		String redirectionURL = Processor.getRedirectionURL(task);
		if (!response.equalsIgnoreCase("VALID") && redirectionURL == null) {
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
        logger.info("Task Object For Consent::"+task);
        
        
        if(songid != null) {
	        while(songid.startsWith("0")) {
	        	songid = songid.substring(songid.indexOf("0") +1 );
	        }	        
	        logger.debug("SongiD: " + songid);	        
	        if(songid.trim().length() > 0) {
	        	task.setObject(param_songid, songid);
	        }
        }
        
        
        
		if (redirectionURL != null) {
			String str= redirectionURL.substring(redirectionURL.indexOf("/rbt_"), redirectionURL.indexOf("?"));
			redirectionURL = redirectionURL.replaceAll(str, "/Consent.do");
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);

				response = httpResponse.getResponse();
				task.setObject(param_response, response);
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
				requestParams.put("RESPONSE_CODE", String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
			}
		} else {
		 // if (!runningRequests.contains(task.getString(param_MSISDN))) {
				try {
			//		runningRequests.add(task.getString(param_MSISDN));
					if (transId == null && !Utility.isModeConfiguredForIdeaConsent(mode)
							&& isConsent) {
						if (songid == null && categoryID == null) {
							logger
									.info("Going For Direct Consent Activation ....");
							isDirectProcessRequest = true;
							processor.processActivationRequest(task);
						} else {
							logger
									.info("Going For Direct Consent Selection ....");
							isDirectProcessRequest = true;
							processor.processAddSelection(task);
						}
					} else if (isConsent) {
						if (songid == null && categoryID == null) {
							logger
									.info("Going For InDirect Consent Activation ....");
							processor.processActivation(task);
						} else {
							logger
									.info("Going For InDirect Consent Selection ....");
							processor.processSelection(task);
						}
					}
				} catch (Exception ex) {
                        logger.info("RBT::Exception " + ex.getMessage());
				} 
          /*    finally {
					if (runningRequests.contains(task.getString(param_MSISDN)))
						runningRequests.remove(task.getString(param_MSISDN));
				}
			} */
		}
		
		if(!isConsent){
			task.setObject(param_response,  NO_CONSENT);
		}

		response = task.getString(param_response);
		logger.info("Consent Response :: " + response);
		if (!isDirectProcessRequest && (response.equalsIgnoreCase(NO_CONSENT) || (response.indexOf(SUCCESS) != -1) || !isConsent ||
				response.equalsIgnoreCase(SUCCESS)||(response.indexOf("success")!=-1)||response.indexOf("already_exists") != -1)) {
			  boolean success = processor.deleteConsentRecord(task);
			  if(success){
				response = SUCCESS; 
			  }
		}
		return response;
	}

//	public static boolean isModeConfiguredForConsent(String mode) {
//		if (consentConfModes.size() == 0) {
//			Parameters responseEncoderParam = CacheManagerUtil
//					.getParametersCacheManager().getParameter(
//							iRBTConstant.PROVISIONING,
//							"CONFIGURED_MODES_FOR_CONSENT_REQUEST", null);
//			if (responseEncoderParam != null) {
//				String confMode = responseEncoderParam.getValue();
//				logger.info("CONFIGURED_MODES_FOR_CONSENT_REQUEST == "
//						+ confMode);
//				String str[] = confMode.split(",");
//				for (String modeStr : str)
//					consentConfModes.add(modeStr);
//			}
//		}
//		boolean response = consentConfModes.contains(mode);
//		return response;
//	}

	public static String processPromotionPreConsentRequest(
			HashMap<String, String> requestParams) throws Exception {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));

		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		Task task = processor.getTask(requestParams);
		requestParams.put("TASK_ACTION", task.getTaskAction());
		response = processor.validateParameters(task);
		
		if (!response.equalsIgnoreCase("VALID")) {
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);
				response = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
			}
		} else {
			String taskAction = task.getTaskAction();
			if (!runningRequests.contains(task.getString(param_MSISDN))) {
				try {
					runningRequests.add(task.getString(param_MSISDN));
					 if (taskAction.equalsIgnoreCase(action_activate)) {
						if (!task.containsKey(param_ACTIVATED_BY)) {
							task.setObject(param_response, Resp_missingParameter);
					    } else {
					    	task.setObject(WebServiceConstants.param_isPreConsentBaseRequest, true);
							processor.processActivation(task);
					    }		
					 } else if (taskAction.equalsIgnoreCase(action_selection)) {
						if (!(task.containsKey(param_TONE_ID)
								|| task.containsKey(param_PROMO_ID)
								|| task.containsKey(param_WAV_FILE)
								|| task.containsKey(param_SMS_ALIAS) || task
								.containsKey(param_PROFILE_NAME))) {
							task.setObject(param_response,
									Resp_missingParameter);
						} else {
							task.setObject(WebServiceConstants.param_isPreConsentBaseSelRequest, true);
							processor.processSelection(task);
						}	
					}else if(taskAction.equalsIgnoreCase(request_upgrade_base)){
						task.setObject(WebServiceConstants.param_isPreConsentBaseRequest, true);
						processor.processBaseUpgradationRequest(task);
					} else {
						task.setObject(param_response, "NOT_ALLOWED");
					}
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
				} finally {
					if (runningRequests.contains(task.getString(param_MSISDN)))
						runningRequests.remove(task.getString(param_MSISDN));
				}
			} else {
				logger
						.info("Promotion jsp request already pending for this subscriber. So returning failure for this : "
								+ task.getString(param_MSISDN));
				task.setObject(param_response, Resp_Failure);
			}
			response = responseEncoder.encode(task);
		}
		logger.info("RBT:: response: " + response);
		Document document = getPreConsentResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}
	
	
	public static Document getPreConsentResponseDocument(Task task){
		 DocumentBuilder documentBuilder = null;
		 Document document = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		
			 document = documentBuilder.newDocument();
			Element element = document.createElement(WebServiceConstants.RBT);
			document.appendChild(element);
	
			String response = task.getString(param_response);
			Element responseElem = Utility.getResponseElement(document, response);
			element.appendChild(responseElem);
			if (response.equalsIgnoreCase(SUCCESS) && !task.containsKey(WebServiceConstants.param_byPassConsent)) {
				Consent consentObj = (Consent) task.getObject("consentObj");
				String msisdn = null;
				String mode = null;
				String sub_class = null;
				String clip_id = null;
				String promo_id = null;
				String chargeclass = null;
				String cat_id = null;
				String trans_id = null;
				
				if(consentObj != null) {
				 msisdn = consentObj.getMsisdn();
			     mode = consentObj.getMode();
			     sub_class = consentObj.getSubClass();
			     trans_id = consentObj.getTransId();
			     clip_id = consentObj.getClipId();
			     promo_id = consentObj.getPromoId();
			     chargeclass = consentObj.getChargeclass();
			     cat_id = consentObj.getCatId();
				}
			    Element consentElem = document.createElement(param_consent);
			    consentElem.setAttribute(param_msisdn, msisdn);
			    if(mode == null) 
			    	mode = "VP";
			    consentElem.setAttribute("mode", mode);
			    if(sub_class != null && sub_class.length() > 0) {
			      consentElem.setAttribute("sub_class", sub_class);
			    }
			    consentElem.setAttribute("trans_id", trans_id);
			    if(clip_id != null && clip_id.length()>0 )
			    consentElem.setAttribute("clip_id", clip_id);
			    if(promo_id != null && promo_id.length()>0 )
			       consentElem.setAttribute("promoId", promo_id);
			    if(chargeclass != null && chargeclass.length()>0) 
			      consentElem.setAttribute("chargeclass", chargeclass);
			    if(cat_id != null && cat_id.length()>0)
			    consentElem.setAttribute("catId", cat_id);
			    element.appendChild(consentElem);
			}
		} catch (ParserConfigurationException e) {
			
		}
	 return document;
	}
	
	private static void getPackCharge(Task task,
			HashMap<String, String> requestParams) {
		String modifiedResponseRequired = requestParams.get("ALTER_STATUS_RES");
		if (modifiedResponseRequired == null)
			return;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber == null)
			return;
		String subClass = subscriber.getSubscriptionClass();
		if (subClass == null)
			return;
		requestParams.put("PACK_STATUS", "paid");
		SubscriptionClass subClassObj = CacheManagerUtil
				.getSubscriptionClassCacheManager().getSubscriptionClass(
						subClass);
		if (subClassObj == null || subClassObj.getSubscriptionAmount() == null)
			return;
		if (subClassObj.getSubscriptionAmount().trim().equalsIgnoreCase("0"))
			requestParams.put("PACK_STATUS", "free");
		else
			requestParams.put("PACK_STATUS", "paid");

	}

	private static void getDate(Task task, HashMap<String, String> requestParams) {
		// This date will be used to return the act or inact date to 3rd party
		// in the http response

		String modifiedResponseRequired = requestParams.get("ALTER_STATUS_RES");
		if (modifiedResponseRequired == null)
			return;
		task.setObject(param_mode, "CCC");
		Subscriber subscriber = Processor.getSubscriber(task);

		requestParams.put("STATUS_DATE", "NULL");
		if (subscriber.getStatus().equals(WebServiceConstants.ACTIVE)
				|| subscriber.getStatus().equals(
						WebServiceConstants.ACT_PENDING)) {
			Date actDate = subscriber.getStartDate();
			if (actDate != null)
				requestParams.put("STATUS_DATE", sdfStatus.format(actDate));
		} else if (subscriber.getStatus().equalsIgnoreCase(
				WebServiceConstants.NEW_USER)) {
			// in case of new user, date will be sent as NULL
		} else {
			Date statusDate = subscriber.getEndDate();
			if (statusDate == null)
				statusDate = subscriber.getStartDate();
			if (statusDate != null) {
				requestParams.put("STATUS_DATE", sdfStatus.format(statusDate));
			}
		}

	}

	public static String processSmsRequest(HashMap<String, String> requestParams)
			throws Exception {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));

		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		String redirectionURL = Processor.getRedirectionURL(task);
		if (!response.equalsIgnoreCase("VALID") && redirectionURL == null) {
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);

				response = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
			}
		} else {
			String taskAction = task.getTaskAction();
			logger.info("RBT:: taskAction: " + taskAction);
			if (taskAction.equalsIgnoreCase(REATILER_ACT_N_SEL_FEATURE))
				processor.processRetailerActnSel(task);
			if (taskAction
					.equalsIgnoreCase(CONFIRM_SUBSCRIPTION_N_COPY_FEATURE))
				processor.processConfirmSubscriptionNCopy(task);
			if (taskAction.equalsIgnoreCase(ACTIVATE_N_SELECTION))
				processor.processActNSel(task);
			else if (taskAction.equalsIgnoreCase(ACTIVATION_KEYWORD))
				processor.processActivationRequest(task);
			else if (taskAction.equalsIgnoreCase(CRICKET_KEYWORD))
				processor.processCricket(task);
			else if (taskAction.equalsIgnoreCase(SONG_CODE_REQUEST_KEYWORD))
				processor.processSongCodeRequest(task);
			else if (taskAction.equalsIgnoreCase(REFERRAL_KEYWORD))
				processor.processReferral(task);
			else if (taskAction.equalsIgnoreCase(NEWS_AND_BEAUTY_FEED_KEYWORD))
				processor.processNewFeed(task);
			else if (taskAction
					.equalsIgnoreCase(VIEW_SUBSCRIPTION_STATISTICS_KEYWORD))
				processor.viewSubscriptionStatistics(task);
			else if (taskAction.equalsIgnoreCase(PROMOTION1))
				processor.processPromotion1(task);
			else if (taskAction.equalsIgnoreCase(PROMOTION2))
				processor.processPromotion2(task);
			else if (taskAction.equalsIgnoreCase(SONG_PROMOTION1))
				processor.processSongPromotion1(task);
			else if (taskAction.equalsIgnoreCase(SONG_PROMOTION2))
				processor.processSongPromotion2(task);
			else if (taskAction.equalsIgnoreCase(SEL_KEYWORD1))
				processor.processSel1(task);
			else if (taskAction.equalsIgnoreCase(SEL_KEYWORD2))
				processor.processSel2(task);
			else if (taskAction.equalsIgnoreCase(DEACTIVATION_KEYWORD))
				processor.processDeactivation(task);
			else if (taskAction.equalsIgnoreCase(UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD))
				processor.processDelayDeactivation(task);
			else if (taskAction.equalsIgnoreCase(RMVCALLERID_KEYWORD))
				processor.processremoveCallerIDSel(task);
			else if (taskAction
					.equalsIgnoreCase(TEMPORARY_OVERRIDE_CANCEL_MESSAGE))
				processor.processRemoveTempOverride(task);
			else if (taskAction.equalsIgnoreCase(NAV_DEACT_KEYWORD))
				processor.processRemoveNavraatri(task);
			else if (taskAction.equalsIgnoreCase(MANAGE_DEACT_KEYWORD))
				processor.processManageRemoveSelection(task);
			else if (taskAction.equalsIgnoreCase(SMS_DOUBLE_CONFIRMATION))
				processor.confirmActNSel(task);
			else if (taskAction.equalsIgnoreCase(PROCESS_DOUBLE_CONFIRMATION))
				processor.processDoubleConfirmation(task);
			else if (taskAction.equalsIgnoreCase(DEACT_DOWNLOAD_KEYWORD))
				processor.processDeactivateDownload(task);
			else if (taskAction.equalsIgnoreCase(CATEGORY_SEARCH_KEYWORD))
				processor.processCategorySearch(task);
			else if (taskAction.equalsIgnoreCase(REQUEST_RBT_KEYWORD))
				processor.processREQUEST(task);
			else if (taskAction.equalsIgnoreCase(AZAAN_REQUEST_RBT_KEYWORD))
				processor.processAzaanSearchRequest(task);
			else if (taskAction.equalsIgnoreCase(REQUEST_OPTIN_RBT_KEYWORD))
				processor.confirmRequestActNSel(task);
			else if (taskAction.equalsIgnoreCase(REQUEST_MORE_KEYWORD))
				processor.getMoreClips(task);
			else if (taskAction.equalsIgnoreCase(AZAAN_REQUEST_MORE))
				processor.getMoreAzaan(task);
			else if (taskAction.equalsIgnoreCase(CONTEST_INFLUENCER_KEYWORD))
				processor.processInfluencerOptin(task);
			else if (taskAction.equalsIgnoreCase(CANCELCOPY_KEYWORD))
				processor.processCancelCopyRequest(task);
			else if (taskAction.equalsIgnoreCase(COPY_CONFIRM_KEYWORD))
				processor.processConfirmCopyRequest(task);
			else if (taskAction.equalsIgnoreCase(COPY_CANCEL_KEYWORD))
				processor.processCancelOptInCopy(task);
			else if (taskAction.equalsIgnoreCase(COPY_KEYWORDS))
				processor.processCOPY(task);
			else if (taskAction.equalsIgnoreCase(GIFT_KEYWORD))
				processor.getGift(task);
			else if (taskAction.equalsIgnoreCase(POLLON_KEYWORD))
				processor.proceesPOLLON(task);
			else if (taskAction.equalsIgnoreCase(POLLOFF_KEYWORD))
				processor.processPollOFF(task);
			else if (taskAction.equalsIgnoreCase(SET_NEWSLETTER_ON_KEYWORDS))
				processor.setNewsletterOn(task);
			else if (taskAction.equalsIgnoreCase(SET_NEWSLETTER_OFF))
				processor.setNewsLetterOff(task);
			else if (taskAction.equalsIgnoreCase(DISABLE_INTRO))
				processor.processDisableIntro(task);
			else if (taskAction.equalsIgnoreCase(DISABLE_OVERLAY_KEYWORD))
				processor.processDisableOverlay(task);
			else if (taskAction.equalsIgnoreCase(ENABLE_OVERLAY_KEYWORD))
				processor.processEnableOverlay(task);
			else if (taskAction.equalsIgnoreCase(WEEKLY_TO_MONTHLY_CONVERSION))
				processor.processWeekToMonthConversion(task);
			else if (taskAction.equalsIgnoreCase(RENEW_KEYWORD))
				processor.processRenew(task);
			else if (taskAction.equalsIgnoreCase(TNB_KEYWORDS))
				processor.processTNB(task);
			else if (taskAction.equalsIgnoreCase(VIRAL_KEYWORD))
				processor.processViralAccept(task);
			else if (taskAction.equalsIgnoreCase(WEB_REQUEST_KEYWORD))
				processor.processWebRequest(task);
			else if (taskAction.equalsIgnoreCase(MGM_ACCEPT_KEY))
				processor.processMgmAccept(task);
			else if (taskAction.equalsIgnoreCase(RETAILER_REQ_RESPONSE))
				processor.processRetailerAccept(task);
			else if (taskAction.equalsIgnoreCase(RETAILER_FEATURE))
				processor.processRetailer(task);
			else if (taskAction.equalsIgnoreCase(MGM_FEATURE))
				processor.processMGM(task);
			else if (taskAction.equalsIgnoreCase(LISTEN_KEYWORD))
				processor.processListen(task);
			else if (taskAction.equalsIgnoreCase(HELP_KEYWORD))
				processor.processHelp(task);
			else if (taskAction.equalsIgnoreCase(SONGOFMONTH))
				processor.processSongOfMonth(task);
			else if (taskAction.equalsIgnoreCase(DOWNLOADS_LIST_KEYWORD))
				processor.processDownloadsList(task);
			else if (taskAction.equalsIgnoreCase(TNB_KEYWORD))
				processor.processTNBActivation(task);
			else if (taskAction.equalsIgnoreCase(MANAGE_KEYWORD))
				processor.processManage(task);
			else if (taskAction.equalsIgnoreCase(LIST_PROFILE_KEYWORD))
				processor.processListProfile(task);
			else if (taskAction.equalsIgnoreCase(NEXT_PROFILE_KEYWORD))
				processor.getNextProfile(task);
			else if (taskAction.equalsIgnoreCase(SCRATCH_CARD_FEATURE))
				processor.processScratchCard(task);
			else if (taskAction.equalsIgnoreCase(GIFTCOPY_FEATURE))
				processor.processGiftCopy(task);
			else if (taskAction.equalsIgnoreCase(MERIDHUN_KEYWORD))
				processor.processMeriDhun(task);
			else if (taskAction.equalsIgnoreCase(DOWNLOAD_OPTIN_RENEWAL))
				processor.processDownloadOptinRenewal(task);
			else if (taskAction.equalsIgnoreCase(INIT_GIFT_KEYWORD))
				processor.processInitGift(task);
			else if (taskAction.equalsIgnoreCase(INIT_GIFT_CONFIRM_KEYWORD))
				processor.processInitGiftConfirm(task);
			else if (taskAction.equalsIgnoreCase(action_retailer_request))
				processor.processRetailerRequest(task);
			else if (taskAction.equalsIgnoreCase(action_retailer_search))
				processor.processRetailerSearch(task);
			else if (taskAction.equalsIgnoreCase(action_retailer_accept))
				processor.processRetailerAccept(task);
			else if (taskAction.equalsIgnoreCase(action_activate))
				processor.processActivation(task);
			else if (taskAction.equalsIgnoreCase(action_deactivate))
				processor.processDeactivation(task);
			else if (taskAction.equalsIgnoreCase(action_selection))
				processor.processSelection(task);
			else if (taskAction.equalsIgnoreCase(action_help))
				processor.processHelpRequest(task);
			else if (taskAction.equalsIgnoreCase(action_cat_search))
				processor.processCategorySearch(task);
			else if (taskAction.equalsIgnoreCase(action_viral))
				processor.processViralRequest(task);
			else if (taskAction.equalsIgnoreCase(action_loop))
				processor.processLoop(task);
			else if (taskAction.equalsIgnoreCase(action_delete))
				processor.processDelete(task);
			else if (taskAction.equalsIgnoreCase(action_obd))
				processor.processOBDRequest(task);
			else if (taskAction.equalsIgnoreCase(action_feed))
				processor.processFeed(task);
			else if (taskAction.equalsIgnoreCase(action_profile))
				processor.processProfile(task);
			else if (taskAction.equalsIgnoreCase(action_list_profiles))
				processor.processListProfiles(task);
			else if (taskAction.equalsIgnoreCase(action_list_next_profiles))
				processor.getNextProfile(task);
			else if (taskAction.equalsIgnoreCase(action_remove_profile))
				processor.processRemoveProfile(task);
			else if (taskAction.equalsIgnoreCase(action_clip_promo))
				processor.processClipByPromoID(task);
			else if (taskAction.equalsIgnoreCase(action_category_promo))
				processor.processCategoryByPromoID(task);
			else if (taskAction.equalsIgnoreCase(action_clip_alias))
				processor.processClipByAlias(task);
			else if (taskAction.equalsIgnoreCase(action_category_alias))
				processor.processCategoryByAlias(task);
			else if (taskAction.equalsIgnoreCase(action_default_search))
				processor.processDefaultSearch(task);
			else if (taskAction.equalsIgnoreCase(action_copy_cancel))
				processor.processCancelCopyRequest(task);
			else if (taskAction.equalsIgnoreCase(action_copy_confirm))
				processor.processConfirmCopyRequest(task);
			else if (taskAction.equalsIgnoreCase(action_optin_copy_cancel))
				processor.processCancelOptInCopy(task);
			//wrong key press get the recommended songs
			else if (taskAction.equalsIgnoreCase(SMS_RECOMMEND_SONGS_KEYWORD))
				processor.processSMSRecommendSongs(task);
			// RBT Like Feature
			else if (taskAction.equalsIgnoreCase(RBT_LIKE_CONFIRM_KEYWORD))
				processor.processConfirmLikeRequest(task);
			else if (taskAction.equalsIgnoreCase(SUSPENSION_KEYWORD))
				// Added for IDEA volunary suspension
				processor.processSuspensionRequest(task);
			else if (taskAction.equalsIgnoreCase(RESUMPTION_KEYWORD))
				processor.processResumptionRequest(task);
			else if (taskAction.equalsIgnoreCase(BLOCK_KEYWORD))
				// Added for Vodafone Block feature
				processor.processBlockRequest(task);
			else if (taskAction.equalsIgnoreCase(UNBLOCK_KEYWORD))
				processor.processUnblockRequest(task);
			else if (taskAction.equalsIgnoreCase(PACK_KEYWORD))
				processor.processSongPackRequest(task);
			else if (taskAction.equalsIgnoreCase(SONG_PACK_KEYWORD))
				// Added for BSNL song packs
				processor.processSpecialSongPackRequest(task);
			else if (taskAction.equalsIgnoreCase(CONFIRM_CHARGE_KEYWORD))
				// Added by Sreekar for ACWM opt-in
				processor.processConfirmCharge(task);
			else if (taskAction.equalsIgnoreCase(LOCK_KEYWORD))
				processor.processLockRequest(task);
			else if (taskAction.equalsIgnoreCase(UNLOCK_KEYWORD))
				processor.processUnlockRequest(task);
			else if (taskAction.equalsIgnoreCase(LIST_CATEGORIES_KEYWORD))
				// Added for SuperHit Album List
				processor.processListCategories(task);
			else if (taskAction.equalsIgnoreCase(EMOTION_KEYWORD))
				processor.processEmotionSongRequest(task);
			else if (taskAction.equalsIgnoreCase(EMOTION_DCT_KEYWORD))
				processor.processDeactEmotionRbtService(task);
			else if (taskAction.equalsIgnoreCase(EMOTION_EXTEND_KEYWORD))
				processor.processExtendEmotionRequest(task);
			// added by sreekar for tata cdma
			else if (taskAction.equalsIgnoreCase(action_trial))
				processor.processTrial(task);
			else if (taskAction.equalsIgnoreCase(action_trialReply))
				processor.processTrialReply(task);
			else if (taskAction.equalsIgnoreCase(UDS_ENABLE))
				processor.processEnableUdsRequest(task);
			else if (taskAction.equalsIgnoreCase(UDS_DISABLE))
				processor.processDisableUdsRequest(task);
			else if (taskAction.equalsIgnoreCase(CHURN_OFFER))
				processor.processChurnOffer(task);
			else if (taskAction.equalsIgnoreCase(RDC_SEL_KEYWORD))
				processor.processRDCViralSelection(task);
			else if (taskAction.equalsIgnoreCase(DISCOUNTED_SEL_KEYWORD))
				processor.processDiscountedSelection(task);
			else if (taskAction.equals(CONSENT_YES_KEYWORD)
					|| taskAction.equals(CONSENT_NO_KEYWORD))
				processor.processChargingConsentRequest(task);
			else if (taskAction.equalsIgnoreCase(CP_SEL_CONFIRM_KEYWORD))
				processor.processCPSelectionConfirm(task);
			else if (taskAction.equalsIgnoreCase(VOUCHER_KEYWORD))
				processor.processVoucherRequest(task);
			else if (taskAction.equalsIgnoreCase(UPGRADE_SEL_KEYWORD))
				processor.processUpgradeSelRequest(task);
			else if (taskAction.equalsIgnoreCase(GIFT_ACCEPT_KEYWORD))
				processor.processGiftAccept(task);
			if (taskAction.equalsIgnoreCase(MOBILE_REGISTRATION))
				processor.processRegistraionSMS(task);
			// else if (taskAction.equalsIgnoreCase(GIFT_DOWNLOAD_KEYWORD))
			// processor.processGiftDownload(task);
			else if (taskAction.equalsIgnoreCase(GIFT_REJECT_KEYWORD)) {
				processor.processGiftReject(task);
			} else if (taskAction.equalsIgnoreCase(MUSIC_PACK_KEYWORD)) {
				// RBT-4549: SMS Activation of music pack implemented for the
				// Tf-Spain
				processor.processMusicPack(task);
			} else if (taskAction.equalsIgnoreCase(RECHARGE_SMS_OPTOUT_KEYWORD)) {
				processor.processOptOutRequest(task);
			} else if (taskAction.equalsIgnoreCase(BASE_UPGRADATION_KEYWORD)) {
				processor.processBaseUpgradationRequest(task);
			} else if (taskAction.equalsIgnoreCase(PRE_GIFT_KEYWORD)) {
				processor.processPreGift(task);
			} else if (taskAction.equalsIgnoreCase(PRE_GIFT_CONFIRM_KEYWORD)) {
				processor.processPreGiftConfirm(task);
			} else if (taskAction.equalsIgnoreCase(VIRAL_START_KEYWORD)) {
				processor.processViralStart(task);
			} else if (taskAction.equalsIgnoreCase(VIRAL_STOP_KEYWORD)) {
				processor.processViralStop(task);
			} else if (taskAction.equalsIgnoreCase(LOTTERY_LIST_KEYWORD)) {
				processor.processLotteryListRequest(task);
			} else if (taskAction.equalsIgnoreCase(RANDOMIZE_KEYWORD)) {
				processor.enableRandomization(task);
			} else if (taskAction.equalsIgnoreCase(UNRANDOMIZE_KEYWORD)) {
				processor.disableRandomization(task);
			} else if (taskAction.equalsIgnoreCase(VIRAL_OPTOUT_KEYWORD)) {
				processor.processViralOptOutRequest(task);
			} else if (taskAction.equalsIgnoreCase(DOWNLOAD_SET_KEYWORD)) {
				processor.processDownloadSetRequest(task);
			} else if (taskAction.equalsIgnoreCase(VIRAL_OPTIN_KEYWORD)) {
				processor.processViralOptInRequest(task);
			} else if (taskAction.equalsIgnoreCase(INIT_RANDOMIZE_KEYWORD)) {
				processor.processInitRandomizeRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(RESUBSCRIPTION_FEATURE_KEYWORD)) {
				processor.processResubscriptionRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(SUPRESS_PRERENEWAL_SMS_KEYWORD)) {
				processor.processSupressPreRenewalSmsRequest(task);
			} else if (taskAction.equalsIgnoreCase(OUI_SMS_KEYWORD)) {
				processor.processOUISmsRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(SMS_CANCEL_DEACTIVATION_KEYWORD)) {
				processor.processCancelDeactvation(task);
			} else if (taskAction
					.equalsIgnoreCase(SMS_BASE_SONG_UPGRADE_KEYWORD)) {
				processor.processBaseSongUpgradationRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(TIME_OF_DAY_SETTING_KEYWORD)) {
				processor.processTimeBasedSettingRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(SMS_CHURN_OFFER_KEYWORD)) {
				processor.processSMSChurnOfferOrDeact(task);
			} else if (taskAction.equalsIgnoreCase(CALLER_BASED_MULTIPLE_SELECTION_KEYWORD)){
				processor.processMultipleSelection(task);
			} else if (taskAction.equalsIgnoreCase(DEACT_BASE_SONG_CHURN_KEYWORD)){
				processor.processCancellar(task);
			} else if (taskAction.equalsIgnoreCase(SMS_CANCELLAR_KEYWORD)){
				processor.processSongManageDeact(task);
			} else if (taskAction.equalsIgnoreCase(DIRECT_SONG_DEACT_KEYWORD)){
				processor.processSongDeactivationConfirm(task);
			}else if(taskAction.equalsIgnoreCase(MANAGE_DEFAULT_SETTINGS_KEYWORD)){
				processor.getOnlyAllCallerSettings(task);
			} else if (taskAction.equalsIgnoreCase(BLOCK_SUB_KEYWORD)) {//RBT-12195 - User block - unblock feature.
				processor.processBlockSubRequest(task);
			} else if (taskAction
					.equalsIgnoreCase(UNBLOCK_SUB_KEYWORD)) {
				processor.processUnBlockSubRequest(task);
			} else if (taskAction.equalsIgnoreCase(PREMIUM_SELECTION_CONFIRMATION_KEYWORD)) {
				processor.processPremiumSelectionConfirmation(task);
			} else if (taskAction.equalsIgnoreCase(DOUBLE_OPT_IN_CONFIRMATION_KEYWORD)) {
				processor.processDoubleOptInConfirmation(task);
			}else if(taskAction.equalsIgnoreCase(DOUBLE_CONFIRMATION_FOR_XBI_PACK)){
				processor.processXbiPack(task);
			} else if (taskAction.equalsIgnoreCase(BASE_AND_COS_UPGRADATION_KEYWORD)) {
				processor.processBaseAndCosUpgradationRequest(task);
			}
			//Added for VB-380
			else if(taskAction.equalsIgnoreCase(AZAAN_REQUEST_DCT_KEYWORD)){
					processor.processDeactivateAzaan(task);
			}

			response = responseEncoder.encode(task);
		}

		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processToneCopyRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		processor.processToneCopyReq(task);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processSubProfileRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		processor.processSubProfileRequest(task);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processComboSubStatusRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);
		task.setObject(param_URL, "rbt_status.jsp");
		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		Subscriber subscriber = Processor.getSubscriber(task);
		if (subscriber.isValidPrefix()) {
			processor.processComboSubStatusRequest(task);
			response = responseEncoder.encode(task);
			if (response.indexOf("ActiveSelections") == -1) {
				response = response
						.replaceFirst("</Service></Response>",
								"<ActiveSelections/><DeActivatedSelections/></Service></Response>");
			}
		} else if (subscriber.getCircleID() != null) {
			response = Resp_Err;
			String redirectionURL = Processor.getRedirectionURL(task);
			logger.info("RBT:: redirect url : " + redirectionURL);
			if (redirectionURL != null) {
				HttpParameters httpParameters = new HttpParameters(
						redirectionURL);
				try {
					requestParams = convertRequestParamToLowerCase("status",
							requestParams);
					logger.debug("request param:" + requestParams);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					logger.info("RBT:: httpResponse: " + httpResponse);

					response = httpResponse.getResponse();
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
					response = Resp_Err;
				}
			}
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
		} else {
			task.setObject(param_response, Resp_InvalidNumber);
			response = responseEncoder.encode(task);
		}

		logger.info("RBT:: substatus response: " + response);
		return response;
	}

	public static String processSubStatusRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);
		task.setObject(param_URL, "rbt_status.jsp");
		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		Subscriber subscriber = Processor.getSubscriber(task);
		if (subscriber.isValidPrefix()) {
			processor.processSubStatusRequest(task);
			response = responseEncoder.encode(task);
		} else if (subscriber.getCircleID() != null) {
			response = Resp_Err;
			String redirectionURL = Processor.getRedirectionURL(task);
			logger.info("RBT:: redirect url : " + redirectionURL);
			if (redirectionURL != null) {
				HttpParameters httpParameters = new HttpParameters(
						redirectionURL);
				try {
					requestParams = convertRequestParamToLowerCase("status",
							requestParams);
					logger.debug("request param:" + requestParams);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					logger.info("RBT:: httpResponse: " + httpResponse);

					response = httpResponse.getResponse();
					if (response != null && response.indexOf("SUCCESS") != -1) {
						response = Resp_Success;
					} else if (response != null) {
						response = response.trim();
					} else
						response = Resp_Err;
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
					response = Resp_Err;
				}
			}
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
		} else {
			task.setObject(param_response, Resp_InvalidNumber);
			response = responseEncoder.encode(task);
		}

		logger.info("RBT:: substatus response: " + response);
		return response;
	}

	public static String processCopyConfirmRequest(
			HashMap<String, String> requestParams) throws Exception {
		Processor processor = getProcessorObject(api_Sms);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_Sms);

		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		task.setObject(param_URL, "rbt_confirm.jsp");
		Parameters modeParam = CacheManagerUtil.getParametersCacheManager()
				.getParameter(iRBTConstant.USSD, COPY_CONF_MODE_USSD, "USSD");
		task.setObject(param_mode, modeParam.getValue());
		logger.info("RBT:: response3: " + modeParam.getValue());

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		Parameters ussdkey = CacheManagerUtil.getParametersCacheManager()
				.getParameter(iRBTConstant.COMMON, USSD_COPY_KEY);
		if (ussdkey != null)
			if (ussdkey.getValue() != null && !(ussdkey.getValue().equals(""))) {
				if (task.getString("USER_RESPONSE").equalsIgnoreCase(
						ussdkey.getValue())) {
					processor.processConfirmCopyRequest(task);
					response = task.getString(param_responseUssd);
					logger.info("RBT:: response4: " + response);
					task.setObject(param_response, response);
				}
			}

		RBTText rbtText = null;
		if (response.equalsIgnoreCase("SUCCESS")) {
			rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(
					iRBTConstant.USSD, USSD_COPY_CONFIRM_SUCCESS);
			response = rbtText.getText();
		} else

		{
			rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(
					iRBTConstant.USSD, USSD_COPY_CONFIRM_FAILURE);
			response = rbtText.getText();

		}

		logger.info("RBT:: response6: " + response);
		return response;
	}

	public static String processCopyRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);
		task.setObject(param_URL, "rbt_copy.jsp");

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		String taskAction = task.getTaskAction();

		if (taskAction.equalsIgnoreCase(action_copy)) {
			processor.processCopyRequest(task);
		} else if (taskAction.equalsIgnoreCase(action_mnp_cross_copy)) {
			processor.processMnpCrossCopyRequest(task);
		} else if (taskAction.equalsIgnoreCase(action_cross_copy)) {
			processor.processCrossCopyRequest(task);
		} else if (taskAction.equalsIgnoreCase(action_cross_copy_rdc)) {
			processor.processCrossCopyRdcRequest(task);
		}

		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String validateAndProcessCopyRequest(
			HashMap<String, String> requestParams) throws Exception {
		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		task.setObject(param_URL, "rbt_validateandcopy.jsp");

		processor.validateAndProcessCopyRequest(task);

		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processMeraHTRequest(
			HashMap<String, String> requestParams) throws Exception {
		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		response = processor.processMeraHelloTuneRequest(task);
		task.setObject(param_response, response);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;

	}

	public static String processUGCRequestOthers(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		response = processor.processUGCRequestOthers(task);
		task.setObject(param_response, response);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processUGCRequest(HashMap<String, String> requestParams)
			throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		String taskAction = task.getTaskAction();
		logger.info("RBT:: Taskaction: " + taskAction);
		response = processor.processUGCRequest(task);
		task.setObject(param_response, response);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processGiftackRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);
		task.setObject(param_URL, "rbt_gift_acknowledge.jsp");
		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		Subscriber gifter = Processor.getSubscriber(task);
		if (gifter.isValidPrefix())
			processor.processGiftAckRequest(task);
		else if (gifter.getCircleID() != null) {
			response = copy_Resp_Err;
			String redirectionURL = Processor.getRedirectionURL(task);

			if (redirectionURL != null) {
				HttpParameters httpParameters = new HttpParameters(
						redirectionURL);
				try {
					requestParams = convertRequestParamToLowerCase("giftack",
							requestParams);
					logger.debug("request param:" + requestParams);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					logger.info("RBT:: httpResponse: " + httpResponse);

					response = httpResponse.getResponse();
					if (response != null && response.indexOf("SUCCESS") != -1) {
						response = Resp_Success;
					} else if (response != null) {
						response = response.trim();
					} else
						response = Resp_Err;
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
					response = responseEncoder
							.getGenericErrorResponse(requestParams);
				}
			}
			task.setObject(param_response, response);

		}

		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processCrossGiftRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		processor.processCrossGiftReq(task);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processRbtHelperRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		response = processor.processRbtPlayerHelperReq(task);
		task.setObject(param_response, response);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processESIAQuizForwarderRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(api_service);
		String response = Resp_Failure;// daemon who calls this jsp/servlet
		// needs FAILURE response in all cases.

		Task task = processor.getTask(requestParams);
		processor.processESIAQuizForwarderReq(task);
		return response;
	}

	public static String processGiftRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);
		task.setObject(param_URL, "rbt_gift.jsp");
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}

		Subscriber giftee = Processor.getSubscriber(task);
		if (giftee.isValidPrefix())
			processor.processGiftRequest(task);
		else if (giftee.getCircleID() != null) {
			response = copy_Resp_Err;
			String redirectionURL = Processor.getRedirectionURL(task);

			if (redirectionURL != null) {
				HttpParameters httpParameters = new HttpParameters(
						redirectionURL);
				try {
					requestParams = convertRequestParamToLowerCase("gift",
							requestParams);
					logger.debug("request param:" + requestParams);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					logger.info("RBT:: httpResponse: " + httpResponse);

					response = httpResponse.getResponse();
					if (response != null && response.indexOf("SUCCESS") != -1) {
						response = Resp_Success;
					} else if (response != null) {
						response = response.trim();
					} else
						response = Resp_Err;
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
					response = responseEncoder
							.getGenericErrorResponse(requestParams);
				}
			}
			task.setObject(param_response, response);

		}

		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processRedirectRequest(
			HashMap<String, String> requestParams) throws Exception {

		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		response = Resp_Fail;
		Task task = processor.getTask(requestParams);
		task.setObject(param_URL, "rbt_redirect.jsp");
		if (task.getString(param_REQVAL) != null
				&& task.getString(param_REQVAL).equalsIgnoreCase("vcode")) {
			if (task.getString(param_subID) == null) {
				response = Resp_Err;
				return response;
			} else if (processor.isValidPrefix(task.getString(param_subID))) {
				response = processor.getSubscriberDefaultVcode(task);
				return response;
			} else {
				response = Resp_Err;
				String redirectionURL = Processor.getRedirectionURL(task);
				logger.info("RBT:: redirect url : " + redirectionURL);
				if (redirectionURL != null) {
					HttpParameters httpParameters = new HttpParameters(
							redirectionURL);
					try {
						requestParams = convertRequestParamToLowerCase(
								"redirect", requestParams);
						logger.debug("request param:" + requestParams);
						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, requestParams);
						logger.info("RBT:: httpResponse: " + httpResponse);

						response = httpResponse.getResponse();
						if (response != null
								&& response.indexOf("SUCCESS") != -1) {
							response = Resp_Success;
						} else if (response != null) {
							response = response.trim();
						} else
							response = Resp_Err;
					} catch (Exception e) {
						logger.error("RBT:: " + e.getMessage(), e);
						response = Resp_Err;
					}
				}
				task.setObject(param_response, response);
			}
		}
		task.setObject(param_response, response);
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processThirdPartyRequest(
			HashMap<String, String> requestParams, String action) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));

		Task task = processor.getTask(requestParams);
		if (action != null)
			task.setTaskAction(action);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		try {
			processor.processThirdPartyRequest(task);
		} catch (Exception e) {
			logger.error("RBT::Exception", e);
		}
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processHSBRequest(HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));

		Task task = processor.getTask(requestParams);
		task.setObject(param_HSB_REQUEST, "true");
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		String valid = processor.validateParameters(task);
		boolean isUserLocked = false;
		if (task.getString(param_response) != null) {
			isUserLocked = task.getString(param_response).equalsIgnoreCase(
					Resp_userLocked);
		}
		if (!valid.equalsIgnoreCase("valid") && !isUserLocked) {
			return response;
		}
		if (!isUserLocked) {
			task.setObject(param_URL, "rbt_hsb.jsp");
			String redirectionURL = Processor.getRedirectionURL(task);
			if (redirectionURL != null) {
				HttpParameters httpParameters = new HttpParameters(
						redirectionURL);
				try {
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
							httpParameters, requestParams);
					logger.info("RBT:: httpResponse: " + httpResponse);
					response = httpResponse.getResponse();
				} catch (Exception e) {
					logger.error("RBT:: " + e.getMessage(), e);
					response = responseEncoder
							.getGenericErrorResponse(requestParams);
				}
				return response;
			}
			try {
				processor.processHSBRequest(task);
			} catch (Exception e) {
				logger.info("RBT::Exception", e);
			}
		}
		response = responseEncoder.encode(task);
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processAdRBTRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		Task task = processor.getTask(requestParams);
		task.setObject(param_AD_RBT_REQUEST, "true");
		task.setObject(param_URL, "rbt_ad_rbt2.jsp");
		task.setObject(param_REDIRECT_NATIONAL, "true");
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;
		String redirectionURL = Processor.getRedirectionURL(task);
		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);
				response = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = responseEncoder
						.getGenericErrorResponse(requestParams);
			}
			return response;
		}
		processor.processAdRBTRequest(task);
		response = responseEncoder.encode(task);
		if (response == null)
			response = responseEncoder.getGenericErrorResponse(requestParams);
		logger.info("RBT:: response: " + response);
		return response;
	}

	/**
	 * @param HashMap
	 *            <String, String> requestParams
	 * @return String Process Copy Request from BTSL or vice versa.
	 */
	public static String processStartCopyRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		Task task = processor.getTask(requestParams);
		logger.info("RBT::  processStartCopyRequest task : " + task);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;

		try {
			processor.processStartCopyRequest(task);
			response = responseEncoder.encode(task);
			logger.info("RBT::  processStartCopyRequest response: " + response);
		} catch (Exception e) {
			logger.error("RBT::  processStartCopyRequest " + e.getMessage(), e);
			response = responseEncoder.getGenericErrorResponse(requestParams);
		}
		return response;
	}

	/**
	 * @param HashMap
	 *            <String, String> requestParams
	 * @return String Process Song Selection Request from BTSL or vice versa.
	 */
	public static String processRDCToCgiSongSelectionRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		Task task = processor.getTask(requestParams);
		logger.info("RBT::  processStartCopyRequest task : " + task);
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;

		try {
			processor.processRdcToCgiSongSelectionRequest(task);
			response = responseEncoder.encode(task);
			logger.info("RBT::  processStartCopyRequest response: " + response);
		} catch (Exception e) {
			logger.error("RBT::  processStartCopyRequest " + e.getMessage(), e);
			response = responseEncoder.getGenericErrorResponse(requestParams);
		}
		return response;
	}

	public static String processDirectActivationRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		Task task = new Task(null, taskSession);
		logger.info("TASK >" + task);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;

		try {
			processor.processDirectActivationRequest(task);
			HashMap<String, Object> requestParamValues = task.getTaskSession();

			if (requestParamValues.containsKey(param_response))
				response = (String) requestParamValues.get(param_response);
		} catch (Exception e) {
			logger.info("RBT::Exception", e);
		}
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processOBDRequest(HashMap<String, String> requestParams)
			throws Exception {
		Processor processor = getProcessorObject(api_service);
		ResponseEncoder responseEncoder = getResponseEncoderObject(api_service);
		String response = FAILURE;

		Task task = processor.getTask(requestParams);
		response = processor.validateParameters(task);

		if (!response.equalsIgnoreCase("VALID")) {
			task.setObject(param_response, response);
			response = responseEncoder.encode(task);
			logger.info("RBT:: response: " + response);
			return response;
		}
		response = FAILURE;
		if (task.getString(param_OBDSMSTEXT) != null
				&& task.getString(param_OBDSUBID) != null) {
			if (processor.isValidPrefix(task.getString(param_OBDSUBID))) {
				if (!processor.isVodafoneOCGInvalidSMS(task
						.getString(param_OBDSMSTEXT)))
					response = processor.processOBDReq(task);
				else
					response = invalid_sms;
			} else
				logger.info("RBT:: Invalid SubID: "
						+ task.getString(param_OBDSUBID));
		}
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static HashMap<String, String> convertRequestParamToLowerCase(
			String type, HashMap<String, String> requestParams) {
		HashMap<String, String> newRequestParams = new HashMap<String, String>();

		Set<String> keySet = requestParams.keySet();
		for (String key : keySet) {
			if (type.equalsIgnoreCase("redirect")) {
				if (key.equalsIgnoreCase("request_value")) {
					newRequestParams.put(key.toLowerCase(), "vCode");
				} else {
					newRequestParams.put(key, requestParams.get(key));
				}

			} else
				newRequestParams.put(key.toLowerCase(), requestParams.get(key));
		}

		return newRequestParams;
	}

	public static String processUSSDSubscriptionRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);

		logger.info("TASK >" + task);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;

		try {
			processor.processUSSDSubscriptionRequest(task);
			HashMap<String, Object> requestParamValues = task.getTaskSession();

			if (requestParamValues.containsKey(param_response))
				response = (String) requestParamValues.get(param_response);
		} catch (Exception e) {
			logger.error("RBT::Exception", e);
		}
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processChangeMsisdnRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
		String response = responseEncoder
				.getGenericErrorResponse(requestParams);

		Task task = processor.getTask(requestParams);

		logger.info("TASK >" + task);
		String valid = processor.validateParameters(task);
		if (!valid.equalsIgnoreCase("valid"))
			return response;

		try {
			processor.processChangeMsisdnRequest(task);
			HashMap<String, Object> requestParamValues = task.getTaskSession();

			if (requestParamValues.containsKey(param_response))
				response = (String) requestParamValues.get(param_response);
		} catch (Exception e) {
			logger.error("RBT::Exception", e);
		}
		logger.info("RBT:: response: " + response);
		return response;
	}

	public static String processVodaCTserviceRequest(
			HashMap<String, String> requestParams) {
		Processor processor = getProcessorObject(requestParams.get(param_api));

		String response = response_VODACT_INTERNAL_ERROR;

		Task task = processor.getTask(requestParams);
		task.setTaskAction(action_vodactservice);
		response = processor.validateParameters(task);
		task.setObject(param_URL, "vodaCTService.do");
		String redirectionURL = Processor.getRedirectionURL(task);

		if (!response.equalsIgnoreCase("VALID") && redirectionURL == null) {
			response = response_VODACT_INVALID_MSISDN;
			logger.info("RBT:: response: " + response);
			return response;
		}

		if (redirectionURL != null) {
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT:: httpResponse: " + httpResponse);

				response = httpResponse.getResponse();
				if (response != null && response.startsWith("RESPONSE_STATUS:"))
					response = response.substring(16);
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				response = response_VODACT_INTERNAL_ERROR;
			}
		} else {
			String mode = requestParams.get(param_VODACT_MODE);
			List<String> upgradeModesLst = com.onmobile.apps.ringbacktones.services.common.Utility.vfUpgradeNonCheckModesList;
			if (mode != null
					&& (com.onmobile.apps.ringbacktones.services.common.Utility.tpcgModesList
							.contains(mode) || (!upgradeModesLst.isEmpty() && !upgradeModesLst
							.contains(mode))))
			{
				response = processor.processConsentCallback(task);
				requestParams.put(param_VODACT_TRANSID, task.getString(param_VODACT_TRANSID));
			}
			else
				response = processor.processVodaCTservice(task);
		}
		
		logger.info("RBT:: vodact_response: " + response);
		return response;
	}
	
	
	//RBT-9213 
	public static String processSDPDirectConsentRequest(
			HashMap<String, String> requestParams) throws Exception {

//		 https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
//			 * &channelType=<mode>&srvkey=<parent service key>&productId=<clip_ID/promo
//			 * code>&productCategoryId=<>&orderTypeId=<C>&transid=<transid> &
//			 * timestamp=<YYYYMMDDhhmmss>&info=categoryid:<song
//			 * category>|songSrvKey:<childname>&sdpomtxid=<>;
		Processor processor = getProcessorObject(requestParams.get(param_api));
		Task task = processor.getSDPDirectConsentTask(requestParams);
		String response = "FAILED";
		//		processor.validateParameters(task);
		String mode = task.getString(param_channelType);
		if (mode != null && !Arrays.asList(
				RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
				"MODES_CONFIGURED_FOR_TRANS_ID_FOR_SDP_CONSENT", "").split(","))
						.contains(mode)) {
			task.setObject(param_transID, null);
		}
		boolean isDirectProcessRequest = false;
		String transId = task.getString(param_transID);
		String songProductId = task.getString(param_productId);
		String orderTypeId = task.getString(param_orderTypeId);
		String sdpSrvkey=task.getString(param_SdpSrvkey);
		String sdpSongSrvkey=task.getString(param_SdpSongSrvkey);
		boolean isConsent = task.getString(param_consent) != null ? (task
				.getString(param_consent).equalsIgnoreCase("yes")||task
				.getString(param_consent).equalsIgnoreCase("SDP")) : false;
		
		String vendorId = task.getString(param_vendor);
		String originatorId = task.getString(param_Originator);
		String agrId = task.getString(param_AGR);
		//Added for RBT-18249
		boolean rtoEnabled = RBTParametersUtils.getParamAsBoolean(iRBTConstant.DAEMON, "ENABLE_RTO_HANDLING", "FALSE");
		if (null !=originatorId) {
			task.setObject(param_Originator, originatorId);
		}
		
        if ((null != agrId)&&(!agrId.equals("0"))) {
			vendorId = agrId;
		}else if (vendorId == null) {
			vendorId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
					"DEFAULT_VENDOR_FOR_SDPDIRECT_CONSENT", "OM");
		}
		task.setObject(param_vendor, vendorId);
		if (orderTypeId == null	|| (!orderTypeId.equals("A") 
				&& !orderTypeId.equals("R") && !orderTypeId.equals("C") && !orderTypeId.equals("U"))) {
			return ORDERTYPE_INVALID;
		}
		//changed for bug
		if (configuredVendorIdsList == null) {
			String configuredVendorIdsforConsent = RBTParametersUtils
					.getParamAsString(iRBTConstant.COMMON,
							"VENDOR_IDS_FOR_SDPDIRECT_CONSENT", null);
			if (configuredVendorIdsforConsent != null) {
				configuredVendorIdsList = Arrays
						.asList(configuredVendorIdsforConsent.split(","));
			}
		}
		ResponseEncoder responseEncoder = getResponseEncoderObject(requestParams
				.get(param_api));
        
		String redirectionURL = Processor.getRedirectionURL(task);
		
        logger.info("Task Object For SDPDirectConsent::"+task);

		if (songProductId!=null && songProductId.trim().length() > 0) {
			task.setObject(param_songid, songProductId);
		}

		boolean isCommboUpgradeRequest = isComboUpgradeRequest(orderTypeId, songProductId, sdpSrvkey);
		if (isCommboUpgradeRequest) {
			task.setObject(param_isUpgradeSongSelection, "true");
		}
		logger.info("isCommboUpgradeRequest :: "+isCommboUpgradeRequest);
		if (redirectionURL != null) {
			String str= redirectionURL.substring(redirectionURL.indexOf("/rbt_"), redirectionURL.indexOf("?"));
			redirectionURL = redirectionURL.replaceAll(str, "/SdpDirect.do");
			HttpParameters httpParameters = new HttpParameters(redirectionURL);
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("RBT::SDPDirect Consent HTTTPResponse: " + httpResponse);

				response = httpResponse.getResponse();
				task.setObject(param_response, response);
			} catch (Exception e) {
				logger.error("RBT:: " + e.getMessage(), e);
				//RBT-12168
				response =  e.getMessage();
				//Added for RBT-18249
				if (rtoEnabled) {
					if (response.contains("Read timed out")
							|| response
									.contains("java.net.SocketTimeoutException")) {
						task.setObject(param_response, "SUCCESS");
						saveSDPObject(task, requestParams);
					} else {
						RBTRto rtoObj = getRtoObj(task, requestParams);
						if (rtoObj != null) {
							task.setObject(param_response, "SUCCESS");
						} else
							task.setObject(param_response, response);
					}
				} else
					task.setObject(param_response, response);
				//Ended for RBT-18249
			}
		} else {
				try {
				if (transId == null && isConsent
						&& (!Utility.isModeConfiguredForIdeaConsent(mode) 
								|| (Utility.isModeConfiguredForIdeaConsent(mode) && configuredVendorIdsList.contains(vendorId)))) {
					    if (sdpSrvkey!=null && sdpSrvkey.contains("RBT_ACT") && orderTypeId != null
							&& !orderTypeId.equals("C") && !isCommboUpgradeRequest) {
							logger
									.info("Going For Direct SDP Consent Activation ....");
							isDirectProcessRequest = true;
							processor.processSDPDirectActivation(task);
						} else {
							logger
									.info("Going For Direct SDP Consent Selection ....");
							isDirectProcessRequest = true;
							processor.processSDPDirectSelection(task);
						}
					} else if (isConsent) {
						if (sdpSrvkey!=null && sdpSrvkey.contains("RBT_ACT") && orderTypeId != null
								&& !orderTypeId.equals("C")) {
							logger
									.info("Going For InDirect SDP Consent Activation ....");
							processor.processSDPIndirectActivation(task);
						} else {
							logger
									.info("Going For InDirect SDP Consent Selection ....");
							processor.processSDPIndirectSelection(task);
						}
					}
				} catch (Exception ex) {
                        logger.info("RBT::Exception " + ex.getMessage(), ex);
				} 
		}
		
		if(!isConsent){
			task.setObject(param_response,  "3011|NO_CONSENT");
			}
		response = task.getString(param_response);
		logger.info("Consent Response :: " + response);
		if (response != null) {
			if (!isDirectProcessRequest
					&& (response.contains(NO_CONSENT)
							|| (response.indexOf(SUCCESS) != -1) || !isConsent
							|| response.equalsIgnoreCase(SUCCESS)
							|| (response.indexOf("success") != -1) || response
							.indexOf("already_exists") != -1)) {
				boolean success = processor.deleteConsentRecord(task);
				// changed for showing no consent in browser
				if (success) {					
					if (response.contains(NO_CONSENT)) {
						response = task.getString(param_response);
					}
					else
						response = "SUCCESS";
				}

			}

			Map<String, String> returnCodeToStringMap = Utility
					.getCodeToStringResponseMap();
			if (returnCodeToStringMap != null
					&& returnCodeToStringMap
							.containsKey(response.toUpperCase())) {
				response = returnCodeToStringMap.get(response.toUpperCase());
			}
		}
		return response;
	}
	
	
	public static boolean isComboUpgradeRequest(String orderTypeId, String productId,
			String sdpSrvkey) {
		if (sdpSrvkey != null && sdpSrvkey.contains("RBT_ACT")
				&& orderTypeId != null && orderTypeId.equalsIgnoreCase("U")
				&& productId != null) {
			return true;
		}
		return false;

	}
	//Added for RBT-18249
		public static void saveSDPObject(Task task,
				HashMap<String, String> reqParams) {
			Session session = null;
			Transaction tx = null;
			RBTRto rbtRtoObj = new RBTRto();
			String redirectionURL = Processor.getRedirectionURL(task);
			int retry = 0;
			String subId = task.getString(param_msisdn);
			String sdpomtxnid = reqParams.get("sdpomtxnid");
			if (redirectionURL != null) {
				String str = redirectionURL.substring(
						redirectionURL.indexOf("/rbt_"),
						redirectionURL.indexOf("?"));
				redirectionURL = redirectionURL.replaceAll(str, "/SdpDirect.do");
				if (reqParams != null && reqParams.size() > 0) {
					Set<Entry<String, String>> entry = reqParams.entrySet();
					for (Entry<String, String> e : entry) {
						redirectionURL = redirectionURL.concat(e.getKey() + "="
								+ e.getValue() + "&");
					}
				}
				redirectionURL = redirectionURL.substring(0,
						redirectionURL.lastIndexOf("&"));
				redirectionURL = redirectionURL.concat("&retry="
						+ String.valueOf(retry));
			}
			try {
				session = HibernateUtil.getSession();
				rbtRtoObj.setSubscriberId(subId);
				rbtRtoObj.setUrl(redirectionURL);
				rbtRtoObj.setSystem("SDPOM");
				rbtRtoObj.setRetry(retry);
				rbtRtoObj.setRetryTime(new Date());
				rbtRtoObj.setSdpomtxnid(sdpomtxnid);
				tx = session.beginTransaction();
				session.saveOrUpdate(rbtRtoObj);
				tx.commit();
				session.flush();
			} catch (HibernateException he) {
				if (tx != null) {
					tx.rollback();
				}
				logger.error(
						"Unable to save: " + rbtRtoObj + ", Exception: "
								+ he.getMessage(), he);
			} finally {
				if (session != null) {
					session.clear();
					session.close();
				}
			}
		}

		public static RBTRto getRtoObj(Task task, HashMap<String, String> reqParams) {
			Session session = null;
			RBTRto rtoObj = null;
			Query query = null;
			String sdpomtxnid = null;
			if (reqParams != null && reqParams.size() > 0) {
				sdpomtxnid = reqParams.get("sdpomtxnid");
			}
			try {
				session = HibernateUtil.getSession();
				query = session
						.createQuery("FROM RBTRto WHERE subscriberId  = :id and sdpomtxnid = :sdpomtxnid");
				query.setString("id", task.getString(param_msisdn));
				query.setString("sdpomtxnid", sdpomtxnid);
				rtoObj = (RBTRto) query.uniqueResult();
			} catch (HibernateException he) {
				he.printStackTrace();
			} finally {
				if (session != null) {
					session.clear();
					session.close();
				}
			}
			return rtoObj;
		}
		//Ended for RBT-18249
}