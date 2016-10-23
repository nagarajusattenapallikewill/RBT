/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupDetails;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 */
public abstract class RBTAdminFacade implements WebServiceConstants {
	private static HashMap<String, Object> implementationMap = null;
	
	private static HashMap<String, String> siteUrls = null;

	private static Logger logger = Logger.getLogger(RBTAdminFacade.class);

	synchronized public static void initialize() {
		if (implementationMap == null) {
			implementationMap = new HashMap<String, Object>();

			try {
				Class
						.forName("com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil");
			} catch (ClassNotFoundException e) {
				logger.error("", e);
			}

			RBTDBManager.getInstance();

			String informationClass = "com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation";
			Parameters informationParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(
							iRBTConstant.WEBSERVICE, "INFORMATION_CLASS");
			if (informationParam != null)
				informationClass = informationParam.getValue().trim();

			String processorClass = "com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor";
			Parameters processorParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(
							iRBTConstant.WEBSERVICE, "PROCESSOR_CLASS");
			if (processorParam != null)
				processorClass = processorParam.getValue().trim();

			try {
				Class<?> rbtInformationClass = Class.forName(informationClass);
				RBTInformation rbtInformation = (RBTInformation) rbtInformationClass
						.newInstance();
				implementationMap.put("DEFAULT_INFORMATION_CLASS",
						rbtInformation);

				Class<?> rbtProcessorClass = Class.forName(processorClass);
				RBTProcessor rbtProcessor = (RBTProcessor) rbtProcessorClass
						.newInstance();
				implementationMap.put("DEFAULT_PROCESSOR_CLASS", rbtProcessor);
			} catch (ClassNotFoundException e) {
				logger.error("", e);
			} catch (InstantiationException e) {
				logger.error("", e);
			} catch (IllegalAccessException e) {
				logger.error("", e);
			}

			String[] modes = { "CCC", "CC", "USSD", "SRBT", "DTOC" };
			Parameters implementationsParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(
							iRBTConstant.WEBSERVICE, "IMPLEMENTATIONS");
			if (implementationsParam != null)
				modes = implementationsParam.getValue().trim().split(",");

			for (String mode : modes) {
				mode = mode.toUpperCase();
				informationParam = CacheManagerUtil.getParametersCacheManager()
						.getParameter(iRBTConstant.WEBSERVICE,
								mode + "_INFORMATION_CLASS");
				if (informationParam != null) {
					informationClass = informationParam.getValue().trim();

					try {
						Class<?> rbtInformationClass = Class
								.forName(informationClass);
						RBTInformation rbtInformation = (RBTInformation) rbtInformationClass
								.newInstance();
						implementationMap.put(mode + "_INFORMATION_CLASS",
								rbtInformation);
					} catch (ClassNotFoundException e) {
						logger.error("", e);
					} catch (InstantiationException e) {
						logger.error("", e);
					} catch (IllegalAccessException e) {
						logger.error("", e);
					}
				}

				processorParam = CacheManagerUtil.getParametersCacheManager()
						.getParameter(iRBTConstant.WEBSERVICE,
								mode + "_PROCESSOR_CLASS");
				if (processorParam != null) {
					processorClass = processorParam.getValue().trim();

					try {
						Class<?> rbtProcessorClass = Class
								.forName(processorClass);
						RBTProcessor rbtProcessor = (RBTProcessor) rbtProcessorClass
								.newInstance();
						implementationMap.put(mode + "_PROCESSOR_CLASS",
								rbtProcessor);
					} catch (ClassNotFoundException e) {
						logger.error("", e);
					} catch (InstantiationException e) {
						logger.error("", e);
					} catch (IllegalAccessException e) {
						logger.error("", e);
					}
				}
			}
		}

		logger.info("RBT:: implementationMap: " + implementationMap);
		logger.info("RBT:: Web Service Initialized");
	}

	public static RBTInformation getRBTInformationObject(WebServiceContext task) {
		RBTInformation rbtInformation = null;
		String mode = task.getString(param_mode);
		if (mode == null
				|| !implementationMap.containsKey(mode.trim().toUpperCase()
						+ "_INFORMATION_CLASS"))
			rbtInformation = (RBTInformation) implementationMap
					.get("DEFAULT_INFORMATION_CLASS");
		else
			rbtInformation = (RBTInformation) implementationMap.get(mode.trim()
					.toUpperCase()
					+ "_INFORMATION_CLASS");

		return rbtInformation;
	}

	public static String getScratchCardResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();
		logger.info("RBT:: task: " + task);

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		String response = ERROR;
		response = rbtProcessor.processNormalScratchCard(task);
		task.put(param_response, response);
		Document document = getRBTInformationObject(task)
				.getScratchCardResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static RBTProcessor getRBTProcessorObject(WebServiceContext task) {
		RBTProcessor rbtProcessor = null;
		String mode = task.getString(param_mode);
		
		//Changed for selection model support
		try{
		 RBTProcessor dtocProcessor = (RBTProcessor)ConfigUtil.getBean(BeanConstant.DTOC_PROCESSOR_CLASS);
		 if(dtocProcessor != null){
			 return dtocProcessor;
		 }
		}catch(Exception e){
			logger.warn(BeanConstant.DTOC_PROCESSOR_CLASS+ " is not configured .So fallback to existing processor.");
		}
		
		/*boolean isDTOCRequest = YES.equalsIgnoreCase((String)task.remove(param_dtocRequest));
		
		if(isDTOCRequest) {
			rbtProcessor = (RBTProcessor) implementationMap.get("DTOC_PROCESSOR_CLASS");
			if(rbtProcessor != null) {
				return rbtProcessor;
			}
		}*/
		
		if (mode == null
				|| !implementationMap.containsKey(mode.trim().toUpperCase()
						+ "_PROCESSOR_CLASS"))
			rbtProcessor = (RBTProcessor) implementationMap
					.get("DEFAULT_PROCESSOR_CLASS");
		else
			rbtProcessor = (RBTProcessor) implementationMap.get(mode.trim()
					.toUpperCase()
					+ "_PROCESSOR_CLASS");

		return rbtProcessor;
	}

	public static String getRBTInformationXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTInformation rbtInformation = getRBTInformationObject(task);
		Document document = null;
		if (task.containsKey(param_info))
			document = rbtInformation.getSpecificRBTInformationDocument(task);
		else
			document = rbtInformation.getRBTInformationDocument(task);

		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getPreConsentSubscriptionResponseXML(WebServiceContext task) {
		
		String response = NOT_ALLOWED;
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		
		String action = task.getString(param_action);
		String subscriberID = task.getString(param_subscriberID);
		if(subscriberID == null) {
			subscriberID = task.getString(param_msisdn);
		}
        task.put(param_isPreConsentBaseRequest, true);
		logger.info("Process SubscriptionPreConsent request for  action: " + action
				+ ", task: " + task + ", Processor: " + rbtProcessor);
		
		if (action.equalsIgnoreCase(action_activate)){
			response = rbtProcessor.processActivation(task);
		}
		else{
			response =  NOT_ALLOWED;
			logger.info("Only Subscription Activation Supported.So response = " + response);
		}

		logger.info("Subscription response for response: " + response);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getSubscriptionPreConsentResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getSubscriptionResponse(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);

		logger.info("Process subscription request for  action: " + action
				+ ", task: " + task + ", rbtProcessor: " + rbtProcessor);
		String response = ERROR;
		if (action.equalsIgnoreCase(action_activate)
				|| action.equalsIgnoreCase(action_acceptGift))
			response = rbtProcessor.processActivation(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.updateSubscription(task);
		else if (action.equalsIgnoreCase(action_deactivate))
			response = rbtProcessor.processDeactivation(task);
		else if (action.equalsIgnoreCase(action_addSubscriberPromo)
				|| action.equalsIgnoreCase(action_removeSubscriberPromo))
			response = rbtProcessor.processSubscriberPromoRequest(task);
		else if (action.equalsIgnoreCase(action_deactivatePack))
			response = rbtProcessor.deactivatePack(task);
		else if (action.equalsIgnoreCase(action_copyContest))
			response = rbtProcessor.updateCopyContestInfo(task);
		else if(action.equalsIgnoreCase(action_rrbt_consent_deactivate) || action.equalsIgnoreCase(action_rrbt_consent_suspension_deactivate))
			response = rbtProcessor.processRRBTConsentDeactivation(task);
		else if (action.equalsIgnoreCase(action_rejectDelayDct)) //RBT-13415 - Nicaragua Churn Management.
			response = rbtProcessor.processRejectDelayDeactivation(task);
		
		task.put(param_response, response);

		return response;
	}

	public static String getSubscriptionResponseXML(WebServiceContext task) {
		
		//Verify mode and IP address configured
		if(!Utility.isValidModeIPConfigured(task.getString(param_mode),task.getString(param_ipAddressConsent))) {
			return Utility.getInvalidIPXML();
		}
		
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);

		logger.info("Process subscription request for  action: " + action
				+ ", task: " + task + ", rbtProcessor: " + rbtProcessor);

		String response = ERROR;
		if (action.equalsIgnoreCase(action_activate)
				|| action.equalsIgnoreCase(action_acceptGift))
			response = rbtProcessor.processActivation(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.updateSubscription(task);
		else if (action.equalsIgnoreCase(action_deactivate))
			response = rbtProcessor.processDeactivation(task);
		else if (action.equalsIgnoreCase(action_addSubscriberPromo)
				|| action.equalsIgnoreCase(action_removeSubscriberPromo))
			response = rbtProcessor.processSubscriberPromoRequest(task);
		else if (action.equalsIgnoreCase(action_deactivatePack))
			response = rbtProcessor.deactivatePack(task);
		else if (action.equalsIgnoreCase(action_subscribeUser))
			response = rbtProcessor.subscribeUser(task);
		else if (action.equalsIgnoreCase(action_copyContest))
			response = rbtProcessor.updateCopyContestInfo(task);
		else if (action.equalsIgnoreCase(action_upgrade))
			response = rbtProcessor.upgradeSelectionPack(task);
		else if(action.equalsIgnoreCase(action_rrbt_consent_deactivate) || action.equalsIgnoreCase(action_rrbt_consent_suspension_deactivate))
			response = rbtProcessor.processRRBTConsentDeactivation(task);
		else if (action.equalsIgnoreCase(action_rejectDelayDct))//RBT-13415 - Nicaragua Churn Management.
			response = rbtProcessor.processRejectDelayDeactivation(task);			
		
		logger.info("Subscription response for response: " + response);
		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getSubscriptionResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getSelectionResponseXML(WebServiceContext task) {
		
		if(!Utility.isValidModeIPConfigured(task.getString(param_mode),task.getString(param_ipAddressConsent))) {
			return Utility.getInvalidIPXML();
		}
		
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);

		logger.info("Process selection request for  action: " + action
				+ ", task: " + task + ", rbtProcessor: " + rbtProcessor);

		String response = ERROR;
		if (action.equalsIgnoreCase(action_get))
			response = SUCCESS;
		if (action.equalsIgnoreCase(action_set)
				|| action.equalsIgnoreCase(action_acceptGift)
				|| action.equalsIgnoreCase(action_default)
				|| action.equalsIgnoreCase(action_overwrite)
				|| action.equalsIgnoreCase(action_overwriteGift))
			response = rbtProcessor.processSelection(task);
		else if (action.equalsIgnoreCase(action_deleteSetting))
			response = rbtProcessor.deleteSetting(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.updateSelection(task);
		else if (action.equalsIgnoreCase(action_downloadTone)
				|| action.equalsIgnoreCase(action_overwriteDownload)
				|| action.equalsIgnoreCase(action_downloadGift)
				|| action.equalsIgnoreCase(action_overwriteDownloadGift))
			response = rbtProcessor.downloadTone(task);
		else if (action.equalsIgnoreCase(action_deleteTone))
			response = rbtProcessor.deleteTone(task);
		else if (action.equalsIgnoreCase(action_shuffle))
			response = rbtProcessor.shuffleDownloads(task);
		else if (action.equalsIgnoreCase(action_upgrade))
			response = rbtProcessor.upgradeSelectionPack(task);
		else if (action.equalsIgnoreCase(action_upgradeSelection))
			response = rbtProcessor.upgradeSpecialSelectionPack(task);
		else if (action.equalsIgnoreCase(action_deactivateOffer))
			response = rbtProcessor.deactivateOffer(task);
		else if (action.equalsIgnoreCase(action_activateAnnouncement))
			response = rbtProcessor.processAnnouncementActivation(task);
		else if ((action.equalsIgnoreCase(action_deactivateAnnouncement)))
			response = rbtProcessor.processAnnouncementDeactivation(task);
		else if ((action.equalsIgnoreCase(action_upgradeAllSelections)))
			response = rbtProcessor.processUpgradeAllSelections(task);
		else if ((action.equalsIgnoreCase(action_addMultipleSettings)))
			response = rbtProcessor.processAddMultipleSelections(task);
		else if ((action.equalsIgnoreCase(action_addMultipleDownloads)))
			response = rbtProcessor.processAddMultipleDownloads(task);
		else if ((action.equalsIgnoreCase(action_deleteMultipleSettings)))
			response = rbtProcessor.processDeleteMultipleSelections(task);
		else if ((action.equalsIgnoreCase(action_deleteMultipleTones)))
			response = rbtProcessor.processDeleteMultipleDownloads(task);
		else if ((action.equalsIgnoreCase(action_unRandomize)))
			response = rbtProcessor.disableRandomization(task);
		else if ((action.equalsIgnoreCase(action_reset)))
			response = rbtProcessor.reset(task);
		else if(action.equalsIgnoreCase(action_upgradeAllDownloads))
			response = rbtProcessor.processUpgradeAllDownloads(task);
		else if(action.equalsIgnoreCase(ACTION_SET_UDP))
			response = rbtProcessor.processUDPSelections(task);
		else if(action.equalsIgnoreCase(ACTION_DEACT_UDP))
			response = rbtProcessor.processUDPDeactivation(task);
		else if (action.equalsIgnoreCase(action_upgradeDownload))
			response = rbtProcessor.processUpgradeDownload(task);
		task.put(param_response, response);

		logger.info("Selection response: " + response);

		Document document = getRBTInformationObject(task)
				.getSelectionResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}
	
	public static String getConsentSelIntegrationResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();
		String action = task.getString(param_action);
		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		logger.info("Process selection request for  action: " + action
				+ ", task: " + task+ ", rbtProcessor: " + rbtProcessor);

		String response = ERROR;
//		try {
//			Subscriber subscriber = DataUtils.getSubscriber(task);
//			boolean isActive =  RBTDBManager.getInstance().isSubActive(subscriber);
//			if(isActive){
//				task.put(param_isSelConsentIntRequest, "true");
//			}
//		} catch (RBTException e) {
//			e.printStackTrace();
//		}
		if (action.equalsIgnoreCase(action_set)) {
		//	task.put(WebServiceConstants.param_isPreConsentBaseSelRequest, "true");
		    response = rbtProcessor.processSelection(task);
		} else {
			response =  NOT_ALLOWED;
		}
		task.put(param_response, response);
		logger.info("ConsentSelIntegrationResponse == "+response);
		Document document = getRBTInformationObject(task)
				.getSelIntegrationPreConsentResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
		
	}
	
	//RBT-7621:-Idea RBT Consent Logic Implementation for IVR
 	public static String getConsentPreSelectionResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);

		logger.info("Process selection request for  action: " + action
				+ ", task: " + task + ", rbtProcessor: " + rbtProcessor);

		String response = ERROR;
		if (action.equalsIgnoreCase(action_set)) {
			task.put(WebServiceConstants.param_isPreConsentBaseSelRequest, "true");
		   response = rbtProcessor.processSelection(task);
		} else {
			response =  NOT_ALLOWED;
		}
		
		task.put(param_response, response);

		logger.info("Selection response: " + response);

		Document document = getRBTInformationObject(task)
				.getSelectionPreConsentResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getBookMarkResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);

		logger.info("Process bookMark response for action: " + action + ", task: "
				+ task + ", rbtProcessor: " + rbtProcessor);

		String response = ERROR;
		if (action.equalsIgnoreCase(action_add)
				|| action.equalsIgnoreCase(action_overwrite))
			response = rbtProcessor.addBookMark(task);
		else if (action.equalsIgnoreCase(action_remove))
			response = rbtProcessor.removeBookMark(task);

		logger.info("BookMark response: " + response);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getBookMarkResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getGroupXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		String response = ERROR;
		String processAllCircles = task.getString(param_processAllCircles);
		String toBeProcessFailedCircles = task.getString(param_toBeprocessCircles);
		boolean isProcessAllCircles = false;
		if (null != processAllCircles) {
			isProcessAllCircles = Boolean
					.parseBoolean(processAllCircles);

			if (isProcessAllCircles) {
				redirectToAllSites(task);
			}
		}

		if(toBeProcessFailedCircles == null || toBeProcessFailedCircles.indexOf("local") != -1 ) {
			logger.info("Received Group request. task: " + task);
	
			String action = task.getString(param_action);
	
			if (action.equalsIgnoreCase(action_get))
				response = SUCCESS;
			else if (action.equalsIgnoreCase(action_add)
					|| action.equalsIgnoreCase(action_update)
					|| action.equalsIgnoreCase(action_remove))
				response = rbtProcessor.processGroupRequest(task);
			else if (action.equalsIgnoreCase(action_addMember)
					|| action.equalsIgnoreCase(action_updateMember)
					|| action.equalsIgnoreCase(action_moveMember)
					|| action.equalsIgnoreCase(action_removeMember))
				response = rbtProcessor.processGroupMemberRequest(task);
			else if (action.equalsIgnoreCase(action_addMultipleMember)
					|| action.equalsIgnoreCase(action_updateMultipleMember)
					|| action.equalsIgnoreCase(action_moveMultipleMember)
					|| action.equalsIgnoreCase(action_removeMultipleMember))
				response = rbtProcessor.processGroupMultipleMemberRequest(task);
	
			task.put(param_response, response);
			String failedCircle = task.getString("failedCircle");
			if(!response.equalsIgnoreCase("SUCCESS") ) {
				if(failedCircle != null) {
					failedCircle = failedCircle + ","; 
				}
				failedCircle += "local";
			}
			
			if(isProcessAllCircles && failedCircle != null) {
				task.put(param_response, failedCircle);
			}
		}
		else {
			String failedCircle = task.getString("failedCircle");
			task.put(param_response, SUCCESS);
			if(failedCircle != null) {
				task.put(param_response, failedCircle);
			}
		}
		Document document = getRBTInformationObject(task)
				.getGroupResponseDocument(task);
		String stringFromDocument = XMLUtils.getStringFromDocument(document);
		logger.info("Response for Group request. stringFromDocument: "
				+ stringFromDocument);		
		return stringFromDocument;

	}
	
	public static String getAffiliateGroupXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		String response = ERROR;

		logger.info("Received Group request. task: " + task);

		String action = task.getString(param_action);

		if (action.equalsIgnoreCase(action_get))
			response = SUCCESS;
		else if (action.equalsIgnoreCase(action_add)
				|| action.equalsIgnoreCase(action_update)
				|| action.equalsIgnoreCase(action_remove))
			response = rbtProcessor.processAffiliateGroupRequest(task);
		else if (action.equalsIgnoreCase(action_addMember)
				|| action.equalsIgnoreCase(action_updateMember)
				|| action.equalsIgnoreCase(action_moveMember)
				|| action.equalsIgnoreCase(action_removeMember))
			response = rbtProcessor.processAffiliateGroupMemberRequest(task);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getAffiliateGroupResponseDocument(task);
		String stringFromDocument = XMLUtils.getStringFromDocument(document);
		logger.info("Response for Group request. stringFromDocument: "
				+ stringFromDocument);
		return stringFromDocument;
	}

	private static void redirectToAllSites(WebServiceContext task) {
		
		
		HashMap<String, String> siteUrlsMap = getSiteUrls();
		logger.info("Redirecting to all sites. siteUrlsMap " + siteUrlsMap);

		// Remove parameter processsAllCircles from task,
		// otherwise it will get added to the redirection URL.
		task.remove(param_processAllCircles);
		
		String toBeProcessedCircles = null;
		if(task.containsKey(param_toBeprocessCircles)) {
			toBeProcessedCircles = task.remove(param_toBeprocessCircles).toString();
		}

		Set<String> circleList = siteUrlsMap.keySet();
		if(toBeProcessedCircles != null) {
			circleList = new HashSet<String>(Arrays.asList(toBeProcessedCircles.split(",")));
		}
		
		for (String circle : circleList) {

//			String circle = siteUrlsMap.getKey();
			String siteUrl = siteUrlsMap.get(circle);
			if (siteUrl == null) {
				logger.warn("SiteUrl is not found for circle: "
						+ circle);
			} else {

				siteUrl = Processor.reconstructRedirectURL(siteUrl,
						"Group.do");

				String finalRedirectionUrl = getFinalUrl(siteUrl, task);
				HttpParameters httpParameters = new HttpParameters(
						finalRedirectionUrl);
				String response = null;
				try {
					logger.debug("Redirecting to Group URL: "
							+ finalRedirectionUrl);
					HttpResponse httpResponse = RBTHttpClient
							.makeRequestByGet(httpParameters, null);
					logger.info("Successfully redirected to Group URL: "
							+ finalRedirectionUrl
							+ ", response: "
							+ httpResponse.getResponse());
					
					if (httpResponse != null && httpResponse.getResponse() != null) {
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(httpResponse.getResponse().trim().getBytes("UTF-8"));
						
						try {
							DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							Document document = documentBuilder.parse(byteArrayInputStream);
							Element responseElem = (Element) document.getElementsByTagName(RESPONSE).item(0);
							Text responseText = (Text) responseElem.getFirstChild();
							response = responseText.getNodeValue();							
						}
						catch (Exception e) {
							logger.debug(
									"Failed to parse response: "
											+ byteArrayInputStream.toString());
						}
						
					}
					
				} catch (Exception e) {
					logger.error("Failed redirect for Group. "
							+ "Url: " + finalRedirectionUrl
							+ ", exception: " + e.getMessage(), e);
				}
				if(!"SUCCESS".equalsIgnoreCase(response)) {
					String failedCircle = "";
					if(task.containsKey("failedCircle")) {
						failedCircle = task.getString("failedCircle") + "," + circle;
					}
					else {
						failedCircle = circle; 
					}
					task.put("failedCircle", failedCircle);
				}
			}
		}
		
		logger.info("Successfully redirected to all sites. task: "+task);
	}

	private static HashMap<String, String> getSiteUrls() {

		if (null == siteUrls) {
			siteUrls = new HashMap<String, String>();
			List<SitePrefix> sitePrefixes = CacheManagerUtil
					.getSitePrefixCacheManager().getAllSitePrefix();
			if (sitePrefixes != null && sitePrefixes.size() > 0) {
				for (int i = 0; i < sitePrefixes.size(); i++)
					siteUrls.put(sitePrefixes.get(i).getCircleID(),
							sitePrefixes.get(i).getSiteUrl());
			}
			logger.info("Loading siteUrls: " + siteUrls);
		}

		logger.info("Returning siteUrls: " + siteUrls);
		return siteUrls;
	}
	
	public static String getFinalUrl(String url, WebServiceContext task) {

		url = url.trim();

		Set<String> keys = task.keySet();

		if (keys == null || keys.size() == 0)
			return url;

		if (url.indexOf("?") == -1 && keys.size() > 0)
			url += "?";

		for (String paramName : keys) {
			String paramValue = task.getString(paramName);
			if (url.endsWith("?"))
				url += paramName + "=" + getEncodedValue(paramValue);
			else
				url += "&" + paramName + "=" + getEncodedValue(paramValue);
		}
		return url;
	}
	
	public static String getEncodedValue(String paramValue) {
		if (paramValue == null)
			return null;

		String ret = null;
		try {
			URLCodec m_urlEncoder = new URLCodec();
			ret = m_urlEncoder.encode(paramValue, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}

	public static String getCopyResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_set)) {
			response = getRBTProcessorObject(task).processCopyRequest(task);
			task.put(param_response, response);
		} else if (action.equalsIgnoreCase(action_directCopy)) {
			response = getRBTProcessorObject(task).processDirectCopyRequest(
					task);
			task.put(param_response, response);
		}

		Document document = getRBTInformationObject(task)
				.getCopyResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getGiftResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;
		if (action.equalsIgnoreCase(action_sendGift))
			response = rbtProcessor.processGiftRequest(task);
		else if (action.equalsIgnoreCase(action_rejectGift))
			response = rbtProcessor.processGiftRejectRequest(task);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getGiftResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getValidateNumberResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		Document document = getRBTInformationObject(task)
				.getValidateNumberResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getSetSubscriberDetailsResponseXML(
			WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;
		if (action != null && action.equalsIgnoreCase(action_changeMsisdn))
			response = rbtProcessor.processChangeMsisdn(task);
		else if (action != null
				&& action.equalsIgnoreCase(action_sendChangeMsisdnRequest))
			response = rbtProcessor.processSendChangeMsisdnRequestToSM(task);
		else if(action != null
				&& action.equalsIgnoreCase(action_deleteConsentRecord)){
			response = rbtProcessor.processDeleteConsentRecords(task);
		}
		else
			response = getRBTProcessorObject(task).setSubscriberDetails(task);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getSetSubscriberDetailsResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getApplicationDetailsResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_get) && !(task.containsKey(param_info) && task.getString(param_info).equalsIgnoreCase(RBT_OTP_LOGIN)))
			response = SUCCESS;
		else if (action.equalsIgnoreCase(action_set))
			response = rbtProcessor.setApplicationDetails(task);
		else if (action.equalsIgnoreCase(action_remove))
			response = rbtProcessor.removeApplicationDetails(task);
		else if (action.equalsIgnoreCase(action_get) && (task.containsKey(param_info) && task.getString(param_info).equalsIgnoreCase(RBT_OTP_LOGIN)))
			response = rbtProcessor.getApplicationDetails(task);
		
		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getApplicationDetailsDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	/**
	 * action - Upload : Uploads the bulkFile, creates the task and returns the
	 * response file action - process : Processes the selected task, returns the
	 * response file action - uploadNprocess: both the upload & process actions
	 */
	public static String getBulkTaskResponse(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_upload))
			response = rbtProcessor.processBulkUpload(task);
		else if (action.equalsIgnoreCase(action_process))
			response = rbtProcessor.processBulkTask(task);
		else if (action.equalsIgnoreCase(action_uploadNprocess))
			response = rbtProcessor.uploadNprocessBulkTask(task);
		else if (action.equalsIgnoreCase(action_activate))
			response = rbtProcessor.processBulkActivation(task);
		else if (action.equalsIgnoreCase(action_deactivate))
			response = rbtProcessor.processBulkDeactivation(task);
		else if (action.equalsIgnoreCase(action_set))
			response = rbtProcessor.processBulkSelection(task);
		else if (action.equalsIgnoreCase(action_deleteSetting))
			response = rbtProcessor.processBulkDeleteSelection(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.processBulkSetSubscriberDetails(task);
		else if (action.equalsIgnoreCase(action_get))
			response = rbtProcessor.processBulkGetSubscriberDetails(task);
		else if (action.equalsIgnoreCase(action_checkBulkSubscribersStatus))
			response = rbtProcessor.checkBulkSubscribersStatus(task);
		else if (action.equalsIgnoreCase(action_getCorporateDetails))
			response = rbtProcessor.processBulkGetCorporateDetails(task);
		else if (action.equalsIgnoreCase(action_getBulkTaskSubscriberDetails))
			response = rbtProcessor.processBulkGetTaskDetails(task);
		else if (action.equalsIgnoreCase(action_activateAnnouncement))
			response = rbtProcessor.processBulkAnnouncementActivation(task);
		else if (action.equalsIgnoreCase(action_deactivateAnnouncement))
			response = rbtProcessor.processBulkAnnouncementDeactivation(task);
		else if (action.equalsIgnoreCase(action_updateValidity))
			response = rbtProcessor.processBulkUpdateSubscription(task);
		else if (action.equalsIgnoreCase(action_downloadOfDay))
			response = rbtProcessor.processDownloadOfDayInsertion(task);
		else if (action.equalsIgnoreCase(action_getdownloadOfDays))
			response = rbtProcessor.getDownloadOfTheDayEntries(task);
		// JiraID-RBT-4187:Song upgradation through bulk process
		else if (action.equalsIgnoreCase(action_upgradeAllSelections))
			response = rbtProcessor.processBulkSelectionUpgradation(task);
		else if (action.equalsIgnoreCase(action_editTask))
			response = rbtProcessor.editBulkTaskForCorporate(task);
		return response;
	}

	/**
	 * action - getTask: gets the pending tasks for the given circleID
	 */
	public static String getBulkProcessTaskResponse(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		String response = ERROR;

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		String action = task.getString(param_action);

		if (action.equalsIgnoreCase(action_get))
			response = SUCCESS;
		else if (action.equalsIgnoreCase(action_editTask)
				|| action.equalsIgnoreCase(action_deleteTask)) {
			response = rbtProcessor.editBulkTask(task);
		} else if (action.equalsIgnoreCase(action_removeTask)) {
			response = rbtProcessor.removeBulkTask(task);
		}
		task.put(param_response, response);

		Document document = getRBTInformationObject(task).getBulkUploadTasks(
				task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getUtilsXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_sendSMS))
			response = rbtProcessor.sendSMS(task);
		else if (action.equalsIgnoreCase(action_tickHLR)
				|| action.equalsIgnoreCase(action_untickHLR))
			response = rbtProcessor.processHLRRequest(task);
		else if (action.equalsIgnoreCase(action_suspension))
			response = rbtProcessor.processSuspension(task);
		else if (action.equalsIgnoreCase(action_thirdPartyRequest))
			response = rbtProcessor.processThirdPartyRequest(task);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getUtilsResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getDataResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);

		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_get))
			response = SUCCESS;
		else if (action.equalsIgnoreCase(action_add))
			response = rbtProcessor.addData(task);
		else if (action.equalsIgnoreCase(action_process))
			response = rbtProcessor.processData(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.updateData(task);
		else if (action.equalsIgnoreCase(action_remove))
			response = rbtProcessor.removeData(task);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task).getDataDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getOfferResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		Document document = getRBTInformationObject(task)
				.getOfferDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static String getUssdSubscriptionResponseXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress))) {
			logger.info("Invalid IP");
			return Utility.getInvalidIPXML();
		}
		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		String response = ERROR;
		logger.info("Call Process ussd");
		response = rbtProcessor.processUSSD(task);
		return response;
	}

	public static String getSngXML(WebServiceContext task) {
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		RBTProcessor rbtProcessor = getRBTProcessorObject(task);
		logger.info("entering");
		String action = task.getString(param_action);
		String response = ERROR;

		if (action.equalsIgnoreCase(action_activate))
			response = rbtProcessor.processSngActivation(task);
		else if (action.equalsIgnoreCase(action_deactivate))
			response = rbtProcessor.processSngDeactivation(task);
		else if (action.equalsIgnoreCase(action_update))
			response = rbtProcessor.processSngUserUpdate(task);
		if (action.equalsIgnoreCase(deactivate_all))
			response = rbtProcessor.processSngAllDeativation(task);

		logger.info("response==" + response);

		task.put(param_response, response);

		Document document = getRBTInformationObject(task)
				.getSngResponseDocument(task);
		return (XMLUtils.getStringFromDocument(document));
	}

	public static BufferedImage generateQRCode(WebServiceContext task) {
		RBTInformation rbtInformation = getRBTInformationObject(task);
		BufferedImage bufferedImage = rbtInformation.getQRCodeImage(task);

		return bufferedImage;
	}
}
