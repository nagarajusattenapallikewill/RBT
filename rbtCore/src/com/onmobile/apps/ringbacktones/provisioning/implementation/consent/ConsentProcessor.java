package com.onmobile.apps.ringbacktones.provisioning.implementation.consent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.api.Utils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class ConsentProcessor extends Processor {

	private static List<String> modesForPromoIdSupport = null;
    private static List<String> subClassConfForAdRBTSpptList = null;
	private static List<String> configuredVendorIdsList = null; 
	private static Map<String,String> mapForCricketPack = null; 
	
	static {

		String confModesForPromoId = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "MODES_FOR_PROMOID_SUPPORT_THROUGH_CONSENT", null);
		if (confModesForPromoId != null) {
			modesForPromoIdSupport = Arrays.asList(confModesForPromoId
					.split(","));
		}

		String configuredVendorIdsforConsent = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "VENDOR_IDS_FOR_SDPDIRECT_CONSENT", null);
		if (configuredVendorIdsforConsent != null) {
			configuredVendorIdsList = Arrays.asList(configuredVendorIdsforConsent
					.split(","));
		}

		String subClassForAdRBTSppt = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "ADRBT_SUB_CLASS", null);
		if(subClassForAdRBTSppt!=null){
			subClassConfForAdRBTSpptList = Arrays.asList(subClassForAdRBTSppt.split(",")); 
		}
		String cricketPackMapByCatId = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "CATEGORY_ID_MAP_FOR_CRICKET_PACK", null);
		if (null != cricketPackMapByCatId) {
			mapForCricketPack = MapUtils.convertToMap(cricketPackMapByCatId,
					",", "=", null);
		}
		
	}

	public ConsentProcessor() throws RBTException {
		super();
	}

	@Override
	public Task getTask(HashMap<String, String> requestParams) {
		Set<Entry<String, String>> entrySet = requestParams.entrySet();
		List<String> unwantedList = new ArrayList<String>();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || value.equals("")
					|| value.equalsIgnoreCase("null")) {
				unwantedList.add(key);
			}
		}

		if (unwantedList.size() > 0) {
			for (String key : unwantedList)
				requestParams.remove(key);
		}
		requestParams.put(param_subscriberID, requestParams.get(param_msisdn));

		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);
		Task task = new Task(null, taskSession);
		String taskAction = requestParams.get(param_action);
		String info = task.getString(param_info);
		String activationInfo = null;
		String categoryID = null;
		String songSRVKey = null;
		String callerID = null;
		String childRefId = null;
		String childPrice = null;
		if (info != null) {
			String infoArr[] = info.split("\\|");
			for (String arr : infoArr) {
				if (arr == null)
					continue;
				String aa[] = arr.split(":");
				if(aa == null || aa.length<2)
					continue;
				if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_activation_info))
					activationInfo = aa[1];
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_categoryid))
					categoryID = aa[1];
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_SONG_SRVKEY))
					songSRVKey = aa[1];
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_callerid))
					callerID = aa[1];
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_childrefid))
					task.setObject(param_childrefid, aa[1]);
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_childprice))
					task.setObject(param_childprice, aa[1]);
				else if(aa[0] != null
						&& aa[0].equalsIgnoreCase(param_upgrade))
					task.setObject(param_upgrade, aa[1]);
			}
		}
		String srvkey = task.getString(param_srvkey.toLowerCase());
		if(srvkey!=null){
			srvkey = srvkey.replaceAll("RBT_ACT_", "");
			task.setObject(param_srvkey.toLowerCase(), srvkey);
		}
		if (categoryID != null) {
			task.setObject(param_CATEGORY_ID, categoryID);
			Category category = rbtCacheManager.getCategory(Integer
					.parseInt(task.getString(param_CATEGORY_ID)), null);
			if (category != null) {
				boolean isShuffle = Utility.isShuffleCategory(category
						.getCategoryTpe());
				task.setObject(param_isShuffle, isShuffle);
			}
		}
		if (activationInfo != null) {
			task.setObject(param_ACTIVATION_INFO, activationInfo);
		}
		if (songSRVKey != null) {
			songSRVKey = songSRVKey.replaceAll("RBT_SEL_", "");
			task.setObject(param_SONG_SRVKEY, songSRVKey);
		}
		if (callerID != null) {
			task.setObject(param_CALLER_ID, callerID);
		}
		task.setTaskAction(taskAction);
		logger.info("RBT:: task: " + task);
		return task;
	}

	@Override
	public boolean isValidPrefix(String subId) {
		return false;
	}

	@Override
	public void processGiftAckRequest(Task task) {
		// do nothing
	}

	@Override
	public void processSelection(Task task) {
		String clipID = task.getString(param_songid);
		String mode = task.getString(param_mode.toLowerCase());
		String subscriberID = task.getString(param_subscriberID);
		String timestamp = task.getString(param_timestamp);
		String transid = task.getString(param_transID);
		String activationInfo = task.getString(param_ACTIVATION_INFO);
		String categoryID = task.getString(param_CATEGORY_ID);
		String info = task.getString(param_info);
		String promoID = null;
        String requestType = null;
        if(task.containsKey(param_upgrade)){
        	requestType = "UPGRADE";
        }
        
		boolean isRMOClip = false;
		if (categoryID != null) {
			Category category = RBTCacheManager.getInstance().getCategory(
					Integer.parseInt(categoryID));
			if (category != null
					&& category.getCategoryTpe() == iRBTConstant.RECORD) {
				isRMOClip = true;
			}
		}

		if (clipID != null && !isRMOClip) {
			if (modesForPromoIdSupport != null
					&& !modesForPromoIdSupport.contains(mode)) {
				Clip clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
				if(clip == null){
					task.setObject(param_response, ERROR.toLowerCase());
					return;
				}else {
					clipID = clip.getClipId() + "";
				}
			}
		}

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		try {
			SelectionRequest selectionRequest = rbtDBManager
					.getMatchingSelectionRecordForConsent(clipID, categoryID,
							subscriberID, mode, transid,isRMOClip,requestType);
			
			if (selectionRequest == null) {
				task.setObject(param_response, param_requestMismatched);
			} else {
				task.setObject(param_transID, selectionRequest.getTransID());
				selectionRequest.setTransID(null);
				if(selectionRequest.getSubscriptionClass() != null && subClassConfForAdRBTSpptList!=null && subClassConfForAdRBTSpptList.contains(selectionRequest.getSubscriptionClass())){
					selectionRequest.setRbtType(1);
				}
				logger.info("Is RMOClip selection = "+isRMOClip);
				if(isRMOClip){
					selectionRequest.setClipID(selectionRequest.getRbtFile());
				}
//				its been added if the subscriber need to upgrade his subscription
				if(task.getString(param_upgrade) != null)
				{
					logger.warn("param upgraded is present..!! and value is:" + task.getString(param_upgrade));
					if(task.getString(param_upgrade).equalsIgnoreCase("TRUE")){
						logger.warn("SubscriptionClass is set to rentalpack as param upgrade value is true");
						selectionRequest.setRentalPack(selectionRequest.getSubscriptionClass());
					}
				}
				
				if(task.containsKey(param_Refid)) {
					String refId = task.getString(param_Refid);
					if((refId = refId.trim()).length() != 0) {
						selectionRequest.setLinkedRefId(task.getString(param_Refid));
					}
				}
				
				if(task.containsKey(param_childrefid)) {
					selectionRequest.setRefID(task.getString(param_childrefid));
				}
				
				RBTClient.getInstance()
						.addSubscriberSelection(selectionRequest);
				String response = selectionRequest.getResponse();
				task.setObject(param_response, response);
			}

		} catch (Exception ex) {
			task.setObject(param_response, param_error);
			logger.info("Exception while processing Consent Selection");
		}

	}

	@Override
	public String validateParameters(Task task) {

		String response = "VALID";
		String taskAction = task.getTaskAction();
		task.setObject(param_subscriberID, task.getString(param_MSISDN
				.toLowerCase()));

		if (task.containsKey(param_mode.toLowerCase()))
			task.setObject(param_ACTIVATED_BY, task.getString(param_mode
					.toLowerCase()));
		if (task.containsKey(param_mode.toLowerCase()))
			task.setObject(param_SELECTED_BY, task.getString(param_mode
					.toLowerCase()));

		String callerID = task.getString(param_callerid);
		if (callerID != null && callerID.length() != 0
				&& !callerID.equalsIgnoreCase("null")) {
			boolean validCallerID = false;
			if (callerID.length() >= param(COMMON, MINIMUM_CALLER_ID_LENGTH, 7)) {
				try {
					Long.parseLong(callerID);
					validCallerID = true;
				} catch (NumberFormatException e) {
				}
			}

			if (!validCallerID) {
				response = "INVALID";
				task.setObject(param_response, Resp_invalidParam);
				logger.info("RBT:: Invalid CALLER_ID. response: " + response
						+ " & param_response: "
						+ task.getString(param_response));
				return response;
			}
		}

		if (taskAction != null && taskAction.equalsIgnoreCase(request_activate)
				&& task.containsKey(param_cosid)) {
			if (task.getString(param_mode) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_invalidParam);
			}
		} else if (taskAction != null
				&& taskAction.equalsIgnoreCase(request_activate)) {
			if (task.getString(param_ACTIVATED_BY) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		} else if (taskAction != null
				&& taskAction.equalsIgnoreCase(request_selection)) {
			if (task.getString(param_SELECTED_BY) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		}
		logger.info("RBT:: response: " + response + " & param_response: "
				+ task.getString(param_response));
		return response;

	}

	/*
	 * For the Selection request coming from consent with transID as null and
	 * mode which is not configured in the Parameter
	 * CONFIGURED_MODES_FOR_CONSENT_REQUEST.It directly processes the request by
	 * inserting into RBT_SUBSCRIBER_SELECTIONS table.
	 */

	@Override
	public String processAddSelection(Task task) {
		String subscriberID = task.getString(param_subscriberID);
		String clipID = task.getString(param_songid);
		String mode = task.getString(param_mode.toLowerCase());
		String srvkey = task.getString(param_srvkey.toLowerCase());
		String chargeClass = task.getString(param_SONG_SRVKEY);
		String categoryID = task.getString(param_CATEGORY_ID);
		String callerID = task.getString(param_CALLER_ID);
		String actinfo = task.getString(param_ACTIVATION_INFO);
		String useUiChargeClass = task.getString(param_USE_UI_CHARGE_CLASS);
		String allowPremiumContent = task.getString(param_allowPremiumContent);
		String modeInfo = mode;
//		if (chargeClass == null && categoryID != null) {
//			chargeClass = srvkey;
//		}
		SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
		selectionRequest.setMode(mode);
		selectionRequest.setActivationMode(mode);
		
		selectionRequest.setCategoryID(categoryID);
		if(useUiChargeClass!=null && useUiChargeClass.equalsIgnoreCase("y"))
			selectionRequest.setUseUIChargeClass(true);
		if (chargeClass != null)
			selectionRequest.setChargeClass(chargeClass);
		if (actinfo != null)
			selectionRequest.setInfo(actinfo);
		if (srvkey != null)
			selectionRequest.setSubscriptionClass(srvkey);
		if (subClassConfForAdRBTSpptList != null
				&& subClassConfForAdRBTSpptList.contains(srvkey))
			selectionRequest.setRbtType(1);
		
		if (clipID != null) {
			if (modesForPromoIdSupport != null
					&& !modesForPromoIdSupport.contains(mode)) {
				Clip clip = RBTCacheManager.getInstance().getClipByPromoId(
						clipID);
				if (clip == null) {
					//if Clip Object is null, clipID to be treated as Category promo ID
					Category category = RBTCacheManager.getInstance()
							.getCategoryByPromoId(clipID);
					if (category != null){
						selectionRequest.setCategoryID(category.getCategoryId()	+ "");
					}else {
						task.setObject(param_response, ERROR.toLowerCase());
						return ERROR.toLowerCase();
					}
				}else{
				   selectionRequest.setClipID(clip.getClipId() + "");
				}
			} else {
				Clip clip = RBTCacheManager.getInstance().getClip(clipID);
				if (clip != null) {
					selectionRequest.setClipID(clipID);
				} else {
					//if Clip Object is null, clipID to be treated as Category promo ID
					Category category = RBTCacheManager.getInstance()
							.getCategoryByPromoId(clipID);
					if (category != null)
						selectionRequest.setCategoryID(category.getCategoryId()
								+ "");
				}
			}
		}
		if (callerID != null)
			selectionRequest.setCallerID(callerID);
		
		if(task.containsKey(param_Refid)) {
			String refId = task.getString(param_Refid);
			if((refId = refId.trim()).length() != 0) {
				selectionRequest.setLinkedRefId(task.getString(param_Refid));
			}
		}
		
		if(task.containsKey(param_childrefid)) {
			selectionRequest.setRefID(task.getString(param_childrefid));
		}
		
		HashMap<String,String> userInfoMap = new HashMap<String, String>();
		if(task.containsKey(param_baseprice)) {
			String basePrice = task.getString(param_baseprice);
			if((basePrice = basePrice.trim()).length() != 0) {
				userInfoMap.put(param_baseprice, task.getString(param_baseprice));
			}						
		}
		
		HashMap<String,String> selectionInfoMap = new HashMap<String, String>();
		if(task.containsKey(param_childprice)) {
			selectionInfoMap.put(param_childprice, task.getString(param_childprice));
		}
		
//		when the subscription need to be upgraded
		if(task.getString(param_upgrade) != null)
		{
			logger.warn("param upgraded is present..!! and value is:" + task.getString(param_upgrade));
			if(task.getString(param_upgrade).equalsIgnoreCase("TRUE")){
				logger.info("SubscriptionClass is set to rentalpack as param upgrade value is true");
				selectionRequest.setRentalPack(selectionRequest.getSubscriptionClass());
			}
		}
		
		if(allowPremiumContent!=null && allowPremiumContent.equalsIgnoreCase("y")){
			selectionRequest.setAllowPremiumContent(true);
		}
		
		selectionRequest.setUserInfoMap(userInfoMap);
		selectionRequest.setSelectionInfoMap(selectionInfoMap);
		selectionRequest.setModeInfo(modeInfo);
		logger.info(" Direct Selection for Consent :: " + selectionRequest);
		RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();
		task.setObject(param_response, response);
		return response;
	}

	public String processSelectionConsentIntegration(Task task){
		try {
			boolean isActive = task.containsKey(WebServiceConstants.param_subscriberStatus);
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode.toLowerCase());
			String modeInfo = task.getString(WebServiceConstants.param_modeInfo);
			String categoryID = task.getString("categoryID");
			String useUiChargeClass = task.getString("useUIChargeClass");
			String callerID = task.getString("callerID");
			String clipID = task.getString("clipID");
			String calledNo = task.getString("calledNo");
			String language = task.getString("language");
			String subscriptionClass = task.getString("subscriptionClass");
			String chargeClass = task.getString("chargeClass");
			String prepaid = task.getString("isPrepaid");
			String fromTime = task.getString("fromTime");
			String toTime = task.getString("toTime");
			String inLoop = task.getString("inLoop");
			String interval = task.getString("interval");
			String profileHours = task.getString("profileHours");
			String cosID = task.getString("cosID");
			String cricketPack = task.getString("cricketPack");
			String rbtType = task.getString("rbtType");

			SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
			selectionRequest.setActivationMode(mode);
			selectionRequest.setMode(mode);
			selectionRequest.setModeInfo(modeInfo);
			selectionRequest.setCalledNo(calledNo);
			selectionRequest.setCallerID(callerID);
			selectionRequest.setClipID(clipID);
			selectionRequest.setSubscriptionClass(subscriptionClass);
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setLanguage(language);
			selectionRequest.setCategoryID(categoryID);
			if (cricketPack != null) {
				selectionRequest.setCricketPack(cricketPack);
			}

			try {
				if (rbtType != null) {
					selectionRequest.setRbtType(Integer.parseInt(rbtType));
				}
				if (cosID != null) {
					selectionRequest.setCosID(Integer.parseInt(cosID));
				}
				if (fromTime != null) {
					selectionRequest.setFromTime(Integer.parseInt(fromTime));
				}
				if (toTime != null) {
					selectionRequest.setFromTime(Integer.parseInt(toTime));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (inLoop != null) {
				selectionRequest.setInLoop(inLoop.equalsIgnoreCase("y"));
			}
			if (interval != null) {
				selectionRequest.setInterval(interval);
			}

			boolean isRMOClip = false;
			if (categoryID != null) {
				Category category = RBTCacheManager.getInstance().getCategory(
						Integer.parseInt(categoryID));
				if (category != null && category.getCategoryTpe() == iRBTConstant.RECORD) {
					isRMOClip = true;
				}
			}

			// if(isRMOClip){
			// selectionRequest.setClipID(clipID);
			// }

			if (useUiChargeClass != null) {
				boolean isUseUiChargeClass = useUiChargeClass != null ? useUiChargeClass
						.equalsIgnoreCase("y") : false;
				selectionRequest.setUseUIChargeClass(isUseUiChargeClass);
			}

			if (prepaid != null) {
				boolean isPrepaid = prepaid != null ? prepaid.equalsIgnoreCase("y") : false;
				selectionRequest.setIsPrepaid(isPrepaid);
			}
			if (profileHours != null) {
				selectionRequest.setProfileHours(profileHours);
			}
			Rbt rbt = RBTClient.getInstance().addSubscriberConsentSelectionIntegration(
					selectionRequest);
			Consent consent = null;
			if (rbt != null) {
				consent = rbt.getConsent();
			}
			
			logger.info("Added consent selection. consent: " + consent
					+ ", selectionRequest: " + selectionRequest);
			
			String response = selectionRequest.getResponse();
			String consentChargeClass = null;
			String consentSubscriptionClass = null;
			String consentId = null;
			String consentCategoryId = null;
			String consentClipId = null;
			String consentMode = null;
			String promo_id = null;
			if (consent != null) {
				consentChargeClass = consent.getChargeclass();
				consentSubscriptionClass = consent.getSubClass();
				consentId = consent.getTransId();
				consentCategoryId = consent.getCatId();
				consentClipId = consent.getClipId();
				consentMode = consent.getMode();
				promo_id = consent.getPromoId();
			}
			logger.info("ConsentSelectionIntegration :: consentChargeClass = " + consentChargeClass
					+ " consentSubscriptionClass = " + consentSubscriptionClass + " consentId = "
					+ consentId + " consentCategoryId = " + consentCategoryId + " consentClipId = "
					+ consentClipId + " consentMode =" + consentMode + "response = " + response);
			if (task.containsKey(WebServiceConstants.param_useSameResForConsent)
					&& task.getString(WebServiceConstants.param_useSameResForConsent)
							.equalsIgnoreCase(WebServiceConstants.YES)) {
				WebServiceContext webSrvContext = new WebServiceContext();
				webSrvContext.put(WebServiceConstants.param_response, response);
				webSrvContext.put(WebServiceConstants.param_subscriberID, subscriberID);
				webSrvContext.put(WebServiceConstants.param_mode, consentMode);
				webSrvContext.put(WebServiceConstants.param_clipID, consentClipId);
				webSrvContext.put(WebServiceConstants.param_promoID, promo_id);
				webSrvContext.put(WebServiceConstants.param_categoryID, consentCategoryId);
				webSrvContext.put("CONSENTID", consentId);
				webSrvContext.put("CONSENTCLASSTYPE", consentChargeClass);
				webSrvContext.put("CONSENTSUBCLASS", consentSubscriptionClass);
				if (isActive) {					 
					webSrvContext.put("USER_ACTIVE_SEL_CON_INT", "true");
				}
				if(callerID != null) {
					webSrvContext.put(WebServiceConstants.param_callerID, callerID);
				}
				ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(consentChargeClass);
				SubscriptionClass subClassObj = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(consentSubscriptionClass);
				String pricePoint = null;
				String priceValidity = null;
				if (subClassObj != null) {
					pricePoint = subClassObj.getSubscriptionAmount() + "|";
					priceValidity = subClassObj.getSubscriptionPeriod() + "|";
				}
				if (chargeClassObj != null) {
					pricePoint += chargeClassObj.getAmount();
					priceValidity += chargeClassObj.getSelectionPeriod();
				}
				pricePoint = pricePoint != null ? pricePoint : "";
				priceValidity = priceValidity != null ? priceValidity : "";
				webSrvContext.put("price", pricePoint);
				webSrvContext.put("priceValidity", priceValidity);
				Document document = RBTAdminFacade.getRBTInformationObject(webSrvContext).getSelIntegrationPreConsentResponseDocument(webSrvContext);
				return (XMLUtils.getStringFromDocument(document));
			} else {
				return  getSelIntegrationPreConsentResponseDocument(
					subscriberID, consentChargeClass, consentSubscriptionClass, consentId,
					consentCategoryId, consentClipId, consentMode, promo_id, isActive, response,callerID, null);
			}
		}catch(Exception ex){
			logger.error("Exception: " , ex);
		}
		return null;
		
	}
	
	
	/*
	 * For the Activation request coming from consent with transID as null and
	 * mode which is not configured in the Parameter
	 * CONFIGURED_MODES_FOR_CONSENT_REQUEST.It directly processes the request by
	 * inserting into RBT_SUBSCRIBER table.
	 */
	@Override
	public void processActivationRequest(Task task) {
		// http://ip:port/rbt/consent.do?msisdn=9886679873&transid=&consent=yes&songid=
		// &srvkey=DEFAULT&timestamp=20130417120101&info=activationinfo:554561|&mode=SIVR
		String srvkey = task.getString(param_srvkey.toLowerCase());
		String subscriberID = task.getString(param_subscriberID);
		String mode = task.getString(param_mode.toLowerCase());
		String activationInfo = task.getString(param_ACTIVATION_INFO);
		String modeInfo = mode;
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriberID);
		subscriptionRequest.setActivationMode(mode);		
		subscriptionRequest.setInfo(activationInfo);
		subscriptionRequest.setSubscriptionClass(srvkey);
		
		if(task.getString(param_upgrade) != null)
		{
			logger.warn("param upgraded is present..!! and value is:" + task.getString(param_upgrade));
			if(task.getString(param_upgrade).equalsIgnoreCase("TRUE")){
				logger.warn("SubscriptionClass is set to rentalpack as param upgrade value is true");
				subscriptionRequest.setRentalPack(subscriptionRequest.getSubscriptionClass());
			}
		}
		
		if(subClassConfForAdRBTSpptList!=null && subClassConfForAdRBTSpptList.contains(srvkey)){
			subscriptionRequest.setRbtType(1);
		}
		
		if(task.containsKey(param_Refid)) {
			subscriptionRequest.setRefId(task.getString(param_Refid));
		}
		HashMap<String,String> userInfoMap = null;
		if(task.containsKey(param_baseprice)) {
			userInfoMap = new HashMap<String, String>();
			userInfoMap.put(param_baseprice, task.getString(param_baseprice));			
		}
		subscriptionRequest.setUserInfoMap(userInfoMap);
		subscriptionRequest.setModeInfo(modeInfo);
		logger.info("Direct Subscription For Consent::" + subscriptionRequest);
		RBTClient.getInstance().activateSubscriber(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		task.setObject(param_response, response);

	}

	@Override
	public Subscriber processActivation(Task task) {
		String mode = task.getString(param_mode.toLowerCase());
		String subscriberID = task.getString(param_subscriberID);
		String srvkey = task.getString(param_srvkey.toLowerCase());
		String timestamp = task.getString(param_timestamp);
		String transid = task.getString(param_transID);
	    String requestType = null;
	    if(task.containsKey(param_upgrade)){
	    	requestType = "UPGRADE";
	    }
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = null;
		try {
			SubscriptionRequest subscriptionRequest = rbtDBManager
					.getMatchingSubscriptionRecordForConsent(subscriberID,
							timestamp, mode, transid, requestType);
			if (subscriptionRequest == null) {
				task.setObject(param_response, param_requestMismatched);
			} else {
				//task.setObject(param_transID, subscriptionRequest
				//		.getConsentTransId());
				
				if(subscriptionRequest.getSubscriptionClass() != null && subClassConfForAdRBTSpptList!=null && subClassConfForAdRBTSpptList.contains(subscriptionRequest.getSubscriptionClass())){
					subscriptionRequest.setRbtType(1);
				}
				
				if(task.getString(param_upgrade) != null)
				{
					logger.warn("param upgraded is present..!! and value is:" + task.getString(param_upgrade));
					if(task.getString(param_upgrade).equalsIgnoreCase("TRUE")){
//						logger.warn("SubscriptionClass is set to rentalpack as param upgrade value is true");
						subscriptionRequest.setRentalPack(subscriptionRequest.getSubscriptionClass());
					}
				}
				if(task.containsKey(param_Refid)) {
					subscriptionRequest.setRefId(task.getString(param_Refid));
				}
				subscriber = RBTClient.getInstance().activateSubscriber(
						subscriptionRequest);
				String response = subscriptionRequest.getResponse();
				task.setObject(param_response, response);
			}
		} catch (Exception ex) {
			task.setObject(param_response, param_error);
			logger.info("Exception in processConsentActivation( )");
		}

		return subscriber;
	}

	public boolean deleteConsentRecord(Task task) {
		String mode = task.getString(param_mode.toLowerCase());
		String subscriberID = task.getString(param_subscriberID);
		String srvkey = task.getString(param_srvkey.toLowerCase());
		String timestamp = task.getString(param_timestamp);
		String transid = task.getString(param_transID);
		String info = task.getString(param_info);
		String clipID = task.getString(param_songid);
		String chargeClass = task.getString(param_SONG_SRVKEY);
		String categoryID = task.getString(param_CATEGORY_ID);
		Category category = (Category) task.getObject(CATEGORY_OBJ);
		boolean isShuffle = false;
		
		if(null != category){
			int categoryType = category.getCategoryTpe();
			isShuffle = Utility.isShuffleCategory(categoryType);
		}
		
		if (clipID != null) {
			if (modesForPromoIdSupport != null
					&& !modesForPromoIdSupport.contains(mode)) {
				Clip clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
				if(clip != null){
					clipID = clip.getClipId() + "";
				}
			}
		}

		boolean response = false;
		try {
			response = RBTDBManager.getInstance().deleteConsentRecord(transid,
					clipID, categoryID, mode, subscriberID, isShuffle);
			if (response)
				task.setObject(param_response, SUCCESS);
		} catch (Exception e) {
			task.setObject(param_response, param_error);
			logger.error("Exception while deleting record from Consent table ",
					e);
		}

		return response;

	}
	
	public String getSelIntegrationPreConsentResponseDocument(String msisdn, String chargeclass,
			String sub_class, String trans_id, String cat_id, String clip_id, String mode,
			String promo_id, boolean isActive, String response,String callerID, String gifteeID) {
		DocumentBuilder documentBuilder = null;
		try {
				if (isActive) {
					 return response;
/*					documentBuilder = DocumentBuilderFactory.newInstance()
							              .newDocumentBuilder();
					Document document = documentBuilder.newDocument();
					Element element = document.createElement("RBT");
					document.appendChild(element);
					Element consentElem = document.createElement(param_consent);
					consentElem.setAttribute(param_msisdn, msisdn);
					if (mode == null)
						mode = "VP";
					consentElem.setAttribute("mode", mode);
					if (sub_class != null && sub_class.length() > 0) {
						consentElem.setAttribute("sub_class", sub_class);
					}
					consentElem.setAttribute("trans_id", trans_id);
					if (clip_id != null && clip_id.length() > 0)
						consentElem.setAttribute("clip_id", clip_id);
					if (promo_id != null && promo_id.length() > 0)
						consentElem.setAttribute("promoId", promo_id);
					if (chargeclass != null && chargeclass.length() > 0)
						consentElem.setAttribute("chargeclass", chargeclass);
					if (cat_id != null && cat_id.length() > 0)
						consentElem.setAttribute("catId", cat_id);
					element.appendChild(consentElem);
					return document;
*/				}
			if (response != null
					&& (response.indexOf("success") != -1 || response.indexOf("already_exists") != -1)) {
				Clip clip = null;
				if (clip_id != null && clip_id.length() > 0) {
					clip = rbtCacheManager.getClip(clip_id);
					if(clip == null) {
						clip = rbtCacheManager.getClipByRbtWavFileName(clip_id);
					}
				} else if (promo_id != null && promo_id.length() > 0) {
					clip = rbtCacheManager.getClipByPromoId(promo_id);
				}
				ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(chargeclass);
				SubscriptionClass subClassObj = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(sub_class);
				String htSuccessConfXml = CacheManagerUtil.getParametersCacheManager()
						.getParameterValue(iRBTConstant.COMMON,
								"CONFIGURED_HT_SUCCESS_XML_" + mode.toUpperCase(), null);
				String subClassProductId="";
				String chrgClassProductId=""; 
				
				if (htSuccessConfXml == null) {
					htSuccessConfXml = CacheManagerUtil
							.getParametersCacheManager()
							.getParameterValue(
									iRBTConstant.COMMON,
									"CONFIGURED_HT_SUCCESS_XML",
									"<rbt><response><Errorcode value=\"%errorcode%\"/><parameters mth=\"handleHTNewActivation\" m=\"%m%\" /></response></rbt>");
				}
				String confSubClassProductIds = CacheManagerUtil.getParametersCacheManager()
						.getParameterValue(COMMON, "SUBSCRIPTION_CLASS_PRODUCT_IDS_MAPPING", null);
				Map<String, String> subClassProductIdMap = MapUtils.convertToMap(
						confSubClassProductIds, ";", "=", ",");
				String confChrgClassProductIds = CacheManagerUtil.getParametersCacheManager()
						.getParameterValue(COMMON, "CHARGE_CLASS_PRODUCT_IDS_MAPPING", null);
				Map<String, String> chrgClassProductIdMap = MapUtils.convertToMap(
						confChrgClassProductIds, ";", "=", ",");
				if (subClassObj!=null && subClassProductIdMap.containsKey(subClassObj.getSubscriptionClass())) {
					subClassProductId = subClassProductIdMap.get(subClassObj
							.getSubscriptionClass());
				}
				if (chargeClassObj!=null && chrgClassProductIdMap.containsKey(chargeClassObj.getChargeClass())) {
					chrgClassProductId = chrgClassProductIdMap.get(chargeClassObj
							.getChargeClass());
				}
				logger.info("ConfSubClassProductIds = " + confSubClassProductIds
						+ " ConfChrgClassProductIds = " + confChrgClassProductIds);
				htSuccessConfXml = htSuccessConfXml.replaceAll("%scpid%", subClassProductId);
				htSuccessConfXml = htSuccessConfXml.replaceAll("%ccpid%", chrgClassProductId);
				
				htSuccessConfXml = htSuccessConfXml.replaceAll("%errorcode%", "0");
				htSuccessConfXml = htSuccessConfXml.replaceAll("%m%", msisdn);
				String clipName = "";
				String clipPromoId = "";
				String vCode = "";
				String rbtWavFile = null;
				if (clip != null) {
					clipName = clip.getClipName() != null ? clip.getClipName() : "";
					clipPromoId = clip.getClipPromoId() != null ? clip.getClipPromoId() : "";
					rbtWavFile = clip.getClipRbtWavFile();
					vCode = rbtWavFile != null ? rbtWavFile.replaceAll("rbt_", "").replaceAll(
							"_rbt", "") : "";
				}
				//Done for RBT-12656: Shuffle Support for RBT Activation - with CG flow
				String consentCategoryPromoId = null;
				String consentCategoryName = null;
				boolean isShuffleType = false;
				if (cat_id != null) {
					Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(cat_id));
					if (category != null) {
						int categoryType = category.getCategoryTpe();
						if (Utility.isShuffleCategory(categoryType)) {
							isShuffleType = true;
							consentCategoryName = category.getCategoryName();
							consentCategoryPromoId = category.getCategoryPromoId();
							htSuccessConfXml = htSuccessConfXml.replaceAll("%son%", consentCategoryName);
							htSuccessConfXml = htSuccessConfXml.replaceAll("%sovc%", consentCategoryPromoId);
							logger.info("Category is shuffle type. "
									+ "subscriberId: " + msisdn
									+ ", consentCategoryPromoId: "
									+ consentCategoryPromoId
									+ ", consentCategoryName: "
									+ consentCategoryName);
						}
					}
				}
				if (!isShuffleType) {
					htSuccessConfXml = htSuccessConfXml.replaceAll("%son%", clipName);
					htSuccessConfXml = htSuccessConfXml.replaceAll("%sovc%", vCode);
				}

				String pricePoint = null;
				String priceValidity = null;
				if (subClassObj != null) {
					pricePoint = subClassObj.getSubscriptionAmount() + "|";
					priceValidity = subClassObj.getSubscriptionPeriod() + "|";
				}
				if (chargeClassObj != null) {
					pricePoint += chargeClassObj.getAmount();
					priceValidity += chargeClassObj.getSelectionPeriod();
				}
				pricePoint = pricePoint != null ? pricePoint : "";
				htSuccessConfXml = htSuccessConfXml.replaceAll("%pp%", pricePoint);
				priceValidity = priceValidity != null ? priceValidity : "";
				htSuccessConfXml = htSuccessConfXml.replaceAll("%pv%", priceValidity);
				trans_id = trans_id != null ? trans_id : "";
				htSuccessConfXml = htSuccessConfXml.replaceAll("%cpt%", trans_id);
				if (callerID == null || callerID.equalsIgnoreCase("ALL")) {
					callerID = "";
				}
				htSuccessConfXml = htSuccessConfXml.replaceAll("%md%", callerID);
				
				if (gifteeID == null) {
					gifteeID = "";
				}
				htSuccessConfXml = htSuccessConfXml.replaceAll("%gifteeId%", gifteeID);
				
				return htSuccessConfXml;
			} else {
				String htErrorConfXml = CacheManagerUtil
						.getParametersCacheManager()
						.getParameterValue(
								iRBTConstant.COMMON,
								"CONFIGURED_HT_ERROR_XML",
								"<rbt><response><Errorcode value=\"1\"/><parameters mth=\"handleHTNewActivation\" m=\"%m%\" /></response></rbt>");
				htErrorConfXml = htErrorConfXml.replaceAll("%m%", msisdn);
				return htErrorConfXml;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.equals("Exception: " + ex);
		}

		return null;
	}

	//RBT-9213 Added all methods
	@Override
	public Task getSDPDirectConsentTask(HashMap<String, String> requestParams){

		Set<Entry<String, String>> entrySet = requestParams.entrySet();
		List<String> unwantedList = new ArrayList<String>();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || value.equals("")
					|| value.equalsIgnoreCase("null")) {
				unwantedList.add(key);
			}
		}

		if (unwantedList.size() > 0) {
			for (String key : unwantedList)
				requestParams.remove(key);
		}
		Parameters trimCountryPrefixParam = CacheManagerUtil
				.getParametersCacheManager().getParameter(
						iRBTConstant.WEBSERVICE, "TRIM_COUNTRY_PREFIX", "TRUE");
		if (trimCountryPrefixParam.getValue().equalsIgnoreCase("TRUE")) {
			String msisdn = RBTDBManager.getInstance().subID(
					requestParams.get(param_msisdn));
			requestParams.put(param_msisdn, msisdn);
		}
		requestParams.put(param_subscriberID, requestParams.get(param_msisdn));

		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);
		Task task = new Task(null, taskSession);
		String taskAction = requestParams.get(param_action);
		String info = task.getString(param_info);
		String categoryID = null;
		String songSRVKey = null;
		if (info != null) {
			String infoArr[] = info.split("\\|");
			for (String arr : infoArr) {
				if (arr == null)
					continue;
				String aa[] = arr.split(":");
				if(aa == null || aa.length<2)
					continue;
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_categoryid))
					categoryID = aa[1];
				else if (aa[0] != null
						&& aa[0].equalsIgnoreCase(param_SONG_SRVKEY))
					songSRVKey = aa[1];
			}
		}
		String srvkey = task.getString(param_srvkey.toLowerCase());
		//RBT-12893-RRBT centric changes in Url SdpDirect.do
		boolean isRRBTSystem = RBTDeploymentFinder.isRRBTSystem();
		//RBT-14074 - Changes for cricket pack if the srvKey has been passed as with RBT_SEL
		//it will be removed and placed in the song srvkey. 
		if (srvkey != null && !srvkey.startsWith("RBT_SEL_")) {
			task.setObject(param_SdpSrvkey, srvkey);
			srvkey = srvkey.replaceAll("RBT_ACT_", "");
			task.setObject(param_srvkey.toLowerCase(),
					setSrvkey(task, srvkey, isRRBTSystem));
		}
		if (categoryID != null) {
			task.setObject(param_CATEGORY_ID, categoryID);
			Category category = rbtCacheManager.getCategory(Integer
					.parseInt(task.getString(param_CATEGORY_ID)), null);
			if (category != null) {
				boolean isShuffle = Utility.isShuffleCategory(category
						.getCategoryTpe());
				task.setObject(param_isShuffle, isShuffle);
			}
		}
		//Changes for RBT-14074	Sports Pack for RRBT
		if (songSRVKey != null) {
			task.setObject(param_SdpSongSrvkey, songSRVKey);
			songSRVKey = songSRVKey.replaceAll("RBT_SEL_", "");
			task.setObject(param_SONG_SRVKEY,
					setSrvkey(task, songSRVKey, isRRBTSystem));
		} else if (srvkey != null && !srvkey.isEmpty()
				&& srvkey.startsWith("RBT_SEL_")) {
			task.setObject(param_SdpSongSrvkey, srvkey);
			if (task.containsKey(param_srvkey.toLowerCase()))
				task.remove(param_srvkey.toLowerCase());
			songSRVKey = srvkey.replaceAll("RBT_SEL_", "");
			task.setObject(param_SONG_SRVKEY,
					setSrvkey(task, songSRVKey, isRRBTSystem));
		}
        String productId = task.getString(param_productId);
		if(productId != null) {
	        while(productId.startsWith("0")) {
	        	productId = productId.substring(productId.indexOf("0") +1 );
	        }	        
	        logger.debug("ProductID: " + productId);	        
	        if(productId.trim().length() > 0) {
	        	task.setObject(param_productId, productId);
	        }
        }

		task.setTaskAction(taskAction);
		logger.info("RBT:: task: " + task);
		return task;
	
	}
	//Changes for RBT-14074	Sports Pack for RRBT
	private String setSrvkey(Task task, String songSRVKey,
			boolean isRRBTSystem) {
		if (isRRBTSystem) {
			if (!songSRVKey.isEmpty() && songSRVKey.endsWith("_RRBT")) {
				songSRVKey = songSRVKey.substring(0,
						(songSRVKey.length() - 5));
			}
		}
		return songSRVKey;		
	}
	
	@Override
	public void processSDPDirectActivation(Task task){
		/*https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
		 * &channelType=<mode>&srvkey=<parent service
		 * key>&productId=<>&productCategoryId=<>&orderTypeId=<R>&transid=<transid>
		 * & timestamp=<YYYYMMDDhhmmss>&info=<>&sdpomtxid=<>;
		 * */
		String subscriptionClass = task.getString(param_srvkey.toLowerCase());
		String subscriberID = task.getString(param_subscriberID);
		String mode = task.getString(param_channelType);
		String orderTypeId=task.getString(param_orderTypeId);
		String sdpomtxnid = task.getString(param_sdpomtxnid); 
		String originatorId = task.getString(param_Originator);
		//Added extra info column in the update to update the vendor info 
		// as per the jira id RBT-11962
		String vendorId=task.getString(param_vendor);
		HashMap<String,String> userInfoMap = new HashMap<String,String>();

		try {
			SubscriptionClass subClassObj = CacheManagerUtil.getSubscriptionClassCacheManager()
					.getSubscriptionClass(subscriptionClass);
			if (subClassObj == null) {
				task.setObject(param_response, "FAILED");
				return;
			}
			
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
			subscriptionRequest.setMode(mode);
			subscriptionRequest.setActivationMode(mode);
			subscriptionRequest.setSubscriptionClass(subscriptionClass);
			if (orderTypeId.equals("U")) {
				subscriptionRequest.setRentalPack(subscriptionClass);
			}
			if (subClassConfForAdRBTSpptList != null
					&& subClassConfForAdRBTSpptList.contains(subscriptionClass)) {
				subscriptionRequest.setRbtType(1);
			}

			if (sdpomtxnid != null) {
				userInfoMap.put(param_sdpomtxnid, sdpomtxnid);
			}
			String seapitype = getseapiType(subscriberID, orderTypeId, true, false);
			if(seapitype!=null) {
				userInfoMap.put(param_seapitype, seapitype);
			}
			//Added extra info column in the update to update the vendor info 
			// as per the jira id RBT-11962
			if (null != vendorId) {
				userInfoMap.put(param_vendor.toUpperCase(), vendorId);
			}
			if (null != originatorId) {
				userInfoMap.put(param_ORIGINATOR, originatorId);
			}
			if (userInfoMap.size() > 0) {
				subscriptionRequest.setUserInfoMap(userInfoMap);
			}

		subscriptionRequest.setModeInfo(mode);
		logger.info("SDP Direct Subscription For Consent::" + subscriptionRequest);
		RBTClient.getInstance().activateSubscriber(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		task.setObject(param_response, response);
		}catch(Exception e) {
			task.setObject(param_response, param_error);
			logger.info("Exception while processing SDP Direct Consent Activation");
		}

	}
	@Override
	public void processSDPDirectSelection(Task task){
		/* Song
		 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
		 * &channelType=<mode>&srvkey=<child service key>&productId=<clip_ID/promo
		 * code>&productCategoryId=<>&orderTypeId=<R>&transid=<transid> &
		 * timestamp=<YYYYMMDDhhmmss>&info=categoryid:<song
		 * category>|songSrvKey:<childname>&sdpomtxid=<>;*/
		String subscriberID = task.getString(param_subscriberID);
		String clipID = task.getString(param_productId);
		String mode = task.getString(param_channelType);
		String subscriptionClass = task.getString(param_srvkey.toLowerCase()); 
		String chargeClass = task.getString(param_SONG_SRVKEY);
		String categoryID = task.getString(param_CATEGORY_ID);
		String sdpomtxnid = task.getString(param_sdpomtxnid);
		String orderTypeId=task.getString(param_orderTypeId);
		String vendorId=task.getString(param_vendor);
		String originatorId = task.getString(param_Originator);
		HashMap<String,String> userInfoMap = new HashMap<String,String>();
		HashMap<String,String> selectionInfoMap = new HashMap<String,String>();
		String isUpgradeSelRequest=task.getString(param_isUpgradeSongSelection);
		try {
			SubscriptionClass subClassObj = CacheManagerUtil.getSubscriptionClassCacheManager()
					.getSubscriptionClass(subscriptionClass);
			
			//changed for bug
			com.onmobile.apps.ringbacktones.content.Subscriber subscriber = RBTDBManager
					.getInstance().getSubscriber(subscriberID);
			String prepaidYes = (subscriber != null && subscriber.prepaidYes()) ? YES
					: NO;
			
			boolean isSubActive = RBTDBManager.getInstance().isSubActive(subscriber);
			if (!isSubActive) {
				//RBT-10761
				if (orderTypeId.equalsIgnoreCase("R")||orderTypeId.equalsIgnoreCase("A")) {
					task.setObject(param_response, "PARENT_NOT_ACTIVE");
					return;
				}
				if (subClassObj == null || categoryID == null) {
					task.setObject(param_response, "FAILED");
					return;
				}
			} else {
				if (categoryID == null) {
					task.setObject(param_response, "FAILED");
					return;
				}
			}
			SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
			selectionRequest.setMode(mode);
			selectionRequest.setActivationMode(mode);
			selectionRequest.setCategoryID(categoryID);
			
			//RBT-15453 - CDT User Song change Selection class to be made configurable.
			String categoryChargeClass = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.WEBSERVICE, "OVERRIDE_CATEGORY_CHARGE_CLASS_FOR_SDP_REQUEST",
							null);
			List<String> categoryChargeClsMapping = ListUtils.convertToList(
					categoryChargeClass, ",");
			
			if(null != categoryID && categoryChargeClsMapping.contains(categoryID)){
				Category category = rbtCacheManager.getCategory(Integer
						.parseInt(categoryID), null);
				chargeClass = category.getClassType();
				
				selectionRequest.setUseUIChargeClass(true);
			}
			
			
			if (chargeClass != null) {
				// Changes for RBT-14074 Sports Pack for RRBT
				selectionRequest.setChargeClass(chargeClass);
			}
			// make new configuration and read the value and assign it.
			if (mapForCricketPack != null && !mapForCricketPack.isEmpty()
					&& mapForCricketPack.get(categoryID) != null
					&& !mapForCricketPack.get(categoryID).isEmpty()) {
				selectionRequest.setCricketPack(mapForCricketPack.get(categoryID));
				selectionRequest.setCategoryID("10");
				categoryID = "10";
			}
			if (subscriptionClass != null){
				selectionRequest.setSubscriptionClass(subscriptionClass);
			}
			if (subClassConfForAdRBTSpptList != null
					&& subClassConfForAdRBTSpptList.contains(subscriptionClass)){
				selectionRequest.setRbtType(1);
			}
			
            if(sdpomtxnid!=null){
            	userInfoMap.put(param_sdpomtxnid, sdpomtxnid);
				if (isUpgradeSelRequest == null
						|| (isUpgradeSelRequest != null && !isUpgradeSelRequest
								.equalsIgnoreCase("true"))) {
					selectionInfoMap.put(param_sdpomtxnid, sdpomtxnid);
				}
				logger.info("isUpgradeSelRequest:--> " + isUpgradeSelRequest);
            }
            String seapitype = getseapiType(subscriberID, orderTypeId, false, true);
			if(seapitype!=null) {
				userInfoMap.put(param_seapitype, seapitype);
				selectionInfoMap.put(param_seapitype, seapitype);
			}
			//Added extra info column in the update to update the vendor info 
			// as per the jira id RBT-11962
			if (null != vendorId) {
				selectionInfoMap.put(param_vendor.toUpperCase(), vendorId);
			}
			if (null != originatorId) {
				selectionInfoMap.put(param_ORIGINATOR, originatorId);
			}
            if(userInfoMap.size()>0 && selectionInfoMap.size()>0){
            	selectionRequest.setUserInfoMap(userInfoMap);
            	selectionRequest.setSelectionInfoMap(selectionInfoMap);
            }
			Clip clip = null;
			if (clipID != null) {
				if (modesForPromoIdSupport != null && modesForPromoIdSupport.contains(mode)) {
					if (configuredVendorIdsList != null && configuredVendorIdsList.contains(vendorId)) {
						clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
					} else {
						clip = RBTCacheManager.getInstance().getClip(clipID);
					}
				} else if (modesForPromoIdSupport == null || !modesForPromoIdSupport.contains(mode)) {
					clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
				}
			}
			
			if (clip != null) {
				selectionRequest.setClipID(clip.getClipId() + "");
			} else if (!categoryID.equalsIgnoreCase("10") && clip == null) {
				Category category = RBTCacheManager.getInstance()
						.getCategoryByPromoId(clipID);
				if (category != null) {
					selectionRequest.setCategoryID(category.getCategoryId()
							+ "");
				} else {
					task.setObject(param_response, ERROR.toLowerCase());
					return;
				}
			}

			
			String udsAllowedCategories = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_ALLOWED_CATEGORIES", null);
			String udsNotAllowedCosIds = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON, "UDS_NOT_ALLOWED_COS_IDS", null);
			boolean isEasyTone = false;
			if (subscriptionClass !=null && udsNotAllowedCosIds != null && !udsNotAllowedCosIds.isEmpty()) {
				List<String> udsNotAllowedCosId = Arrays.asList(udsNotAllowedCosIds.split(","));
				for(String cosId: udsNotAllowedCosId){
					CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosId);
					if(cosDetails != null && subscriptionClass.equalsIgnoreCase(cosDetails.getSubscriptionClass())){
						isEasyTone = true;
						break;
					}
				}
			}
			if (udsAllowedCategories != null && !udsAllowedCategories.isEmpty() && Arrays.asList(udsAllowedCategories.split(",")).contains(categoryID)) {
				if(isEasyTone){
					task.setObject(param_response, param_not_allowed);
					logger.info("rejecting request for easytoneUser: "+ subscriptionClass );
					return;
				}
				if (isSubActive) {
				SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
						subscriberID);
				HashMap<String,String> subUserInfoMap = new HashMap<String,String>();
				subUserInfoMap.put(UDS_OPTIN, "TRUE");
				subscriptionRequest.setUserInfoMap(subUserInfoMap);
					RBTClient.getInstance().updateSubscription(subscriptionRequest);
				} else{
					userInfoMap.put(UDS_OPTIN, "TRUE");
					selectionRequest.setUserInfoMap(userInfoMap);
				}
				
			}
			
			// RBT-17864
			CosDetails subClassBasedCos = null;
			if (subClassObj != null
					&& subscriber != null
					&& (isUpgradeSelRequest != null && isUpgradeSelRequest
							.equalsIgnoreCase("true"))) {
				List<CosDetails> cosList = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetailsByCosType(
								"SUB_CLASS", subscriber.circleID(), prepaidYes);
				if (cosList != null && cosList.size() > 0) {
					for (CosDetails cosDetails : cosList) {
						if (cosDetails.getSubscriptionClass().equalsIgnoreCase(
								subClassObj.getSubscriptionClass())) {
							subClassBasedCos = cosDetails;
							break;
						}
					}
				}
				if (subClassBasedCos != null) {
					String coschargeClass = getCosChargeClass(subClassBasedCos);
					if (coschargeClass != null && !coschargeClass.isEmpty()) {
						selectionRequest.setChargeClass(coschargeClass);
						selectionRequest.setRentalPack(subClassObj
								.getSubscriptionClass());
						selectionInfoMap.put("IS_SDP_COMBO_SELECTION", "true");
						selectionRequest.setSelectionInfoMap(selectionInfoMap);
						selectionRequest.setUseUIChargeClass(true);
					}
				}
			}
			selectionRequest.setModeInfo(mode);
			logger.info("SDP Direct Selection for Consent :: " + selectionRequest);
			RBTClient.getInstance().addSubscriberSelection(selectionRequest);
			String response = selectionRequest.getResponse();
			task.setObject(param_response, response);
			
		}catch(Exception e){
			task.setObject(param_response, param_error);
			logger.info("Exception while processing SDP Direct Consent Selection");
		}
	}
	
	@Override
	public void processSDPIndirectActivation(Task task){
		/*https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
		 * &channelType=<mode>&srvkey=<parent service
		 * key>&productId=<>&productCategoryId=<>&orderTypeId=<R>&transid=<transid>
		 * & timestamp=<YYYYMMDDhhmmss>&info=<>&sdpomtxid=<>;
		 * */
		String mode = task.getString(param_channelType);
		String subscriberID = task.getString(param_subscriberID);
		String timestamp = task.getString(param_timestamp);
		String transid = task.getString(param_transID);
		String orderTypeId=task.getString(param_orderTypeId);
		String sdpomtxnid=task.getString(param_sdpomtxnid);
		//Added extra info column in the update to update the vendor info 
		// as per the jira id RBT-11962
		String vendorId=task.getString(param_vendor);
		String originatorId = task.getString(param_Originator);
		HashMap<String,String> userInfoMap=new HashMap<String,String>();
	    String requestType = null;
	    if(orderTypeId.equals("U")){
	    	requestType = "UPGRADE";
	    }
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = null;
		try {
			SubscriptionRequest subscriptionRequest = rbtDBManager
					.getMatchingSubscriptionRecordForConsent(subscriberID,
							timestamp, mode, transid, requestType);
			if (subscriptionRequest == null) {
				boolean checkRtoinBase = false;
				if (task.getString(param_retry) != null) {
					checkRtoinBase = rbtDBManager.checkRtoinBase(subscriberID,
							sdpomtxnid);
					if (checkRtoinBase) {
						task.setObject(param_response, SUCCESS);
						return;
					}
				} else
					task.setObject(param_response, param_mismatch);
			} else {
				if(subscriptionRequest.getSubscriptionClass() != null && subClassConfForAdRBTSpptList!=null && subClassConfForAdRBTSpptList.contains(subscriptionRequest.getSubscriptionClass())){
					subscriptionRequest.setRbtType(1);
				}
				if(orderTypeId.equals("U")) {
					subscriptionRequest.setRentalPack(subscriptionRequest.getSubscriptionClass());
				}

				if(sdpomtxnid!=null){
					userInfoMap.put(param_sdpomtxnid, sdpomtxnid);
				}
				String seapitype = getseapiType(subscriberID, orderTypeId, true, false);
				if(seapitype!=null) {
					userInfoMap.put(param_seapitype, seapitype);
				}
				//Added extra info column in the update to update the vendor info 
				// as per the jira id RBT-11962
				if (null != vendorId) {
					userInfoMap.put(param_vendor.toUpperCase(), vendorId);
				}
				if (null != originatorId) {
					userInfoMap.put(param_ORIGINATOR, originatorId);
				}
				if(userInfoMap.size()>0){
					subscriptionRequest.setUserInfoMap(userInfoMap);
				}
				subscriber = RBTClient.getInstance().activateSubscriber(
						subscriptionRequest);
				String response = subscriptionRequest.getResponse();
				task.setObject(param_response, response);
			}
		} catch (Exception ex) {
			task.setObject(param_response, param_error);
			logger.info("Exception in processSDPIndirectActivation( )");
		}

	}
	
	@Override
	public void processSDPIndirectSelection(Task task){
		/* Song
		 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
		 * &channelType=<mode>&srvkey=<child service key>&productId=<clip_ID/promo
		 * code>&productCategoryId=<>&orderTypeId=<R>&transid=<transid> &
		 * timestamp=<YYYYMMDDhhmmss>&info=categoryid:<song
		 * category>|songSrvKey:<childname>&sdpomtxid=<>;*/
		String clipID = task.getString(param_productId);
		String mode = task.getString(param_channelType);
		String subscriberID = task.getString(param_subscriberID);
		String transid = task.getString(param_transID);
		String orderTypeId=task.getString(param_orderTypeId); 
		String sdpomtxnid=task.getString(param_sdpomtxnid); 
		HashMap<String,String> userInfoMap=new HashMap<String,String>();
		HashMap<String,String> selectionInfoMap=new HashMap<String,String>();
		String vendorId=task.getString(param_vendor);
		String originatorId = task.getString(param_Originator);
		String requestType = null;
		
		 if(orderTypeId.equals("U")){
		    	requestType = "UPGRADE";
		  }
		 
		boolean isRMOClip = false;
		String categoryID = task.getString(param_CATEGORY_ID);

		Clip clip = null;
		if (clipID != null && !isRMOClip) {
			if (modesForPromoIdSupport != null && modesForPromoIdSupport.contains(mode)) {

				if (configuredVendorIdsList != null && configuredVendorIdsList.contains(vendorId)) {
					clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
				} else {
					clip = RBTCacheManager.getInstance().getClip(clipID);
				}
			} else if (modesForPromoIdSupport == null || !modesForPromoIdSupport.contains(mode)) { //TODO check

				clip = RBTCacheManager.getInstance().getClipByPromoId(clipID);
			}
			if (clip == null) {
				task.setObject(param_response, ERROR.toLowerCase());
				return;
			}
		}
		
        if(clip!=null){
        	clipID = clip.getClipId()+"";
        }
        
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		try {
			SelectionRequest selectionRequest = rbtDBManager
					.getMatchingSelectionRecordForConsent(clipID, categoryID,
							subscriberID, mode, transid,isRMOClip,requestType);
			
			if (selectionRequest == null) {
				boolean checkRtoinSelection = false;
				if (task.getString(param_retry) != null) {
					if (orderTypeId.equalsIgnoreCase("C"))
						checkRtoinSelection = rbtDBManager.checkRtoforCombo(
								clip.getClipRbtWavFile(), categoryID,
								subscriberID, sdpomtxnid);
					else
						checkRtoinSelection = rbtDBManager.checkRtoinSelection(
								clip.getClipRbtWavFile(), categoryID,
								subscriberID, sdpomtxnid);
					if (checkRtoinSelection) {
						task.setObject(param_response, SUCCESS);
						return;
					}
				} else
					task.setObject(param_response, param_mismatch);
			} else {
				task.setObject(param_transID, selectionRequest.getTransID());
				selectionRequest.setTransID(null);

				if (selectionRequest.getSubscriptionClass() != null
						&& subClassConfForAdRBTSpptList != null
						&& subClassConfForAdRBTSpptList.contains(selectionRequest
								.getSubscriptionClass())) {
					selectionRequest.setRbtType(1);
				}
				logger.info("Is RMOClip selection = "+isRMOClip);
				if(isRMOClip){
					selectionRequest.setClipID(selectionRequest.getRbtFile());
				}
				
				if(sdpomtxnid!=null){
					userInfoMap.put(param_sdpomtxnid, sdpomtxnid);
					selectionInfoMap.put(param_sdpomtxnid, sdpomtxnid); 
				}
				String seapitype = getseapiType(subscriberID, orderTypeId, false, true);
				if(seapitype!=null) {
					userInfoMap.put(param_seapitype, seapitype);
					selectionInfoMap.put(param_seapitype, seapitype);
				}
				//Added extra info column in the update to update the vendor info 
				// as per the jira id RBT-11962
				if (null != vendorId) {
					selectionInfoMap.put(param_vendor.toUpperCase(), vendorId);
				}
				if (null != originatorId) {
					selectionInfoMap.put(param_ORIGINATOR, originatorId);
				}
				if(userInfoMap.size()>0 && selectionInfoMap.size()>0){
					selectionRequest.setUserInfoMap(userInfoMap);
					selectionRequest.setSelectionInfoMap(selectionInfoMap);
				}

				//RBT-15453 - CDT User Song change Selection class to be made configurable.
				String categoryChargeClass = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.WEBSERVICE, "OVERRIDE_CATEGORY_CHARGE_CLASS_FOR_SDP_REQUEST",
								null);
				List<String> categoryChargeClsMapping = ListUtils.convertToList(
						categoryChargeClass, ",");
				
				if(null != categoryID && categoryChargeClsMapping.contains(categoryID)){
					Category category = rbtCacheManager.getCategory(Integer
							.parseInt(categoryID), null);
					String chargeClass = category.getClassType();
					
					selectionRequest.setChargeClass(chargeClass);
					
					selectionRequest.setUseUIChargeClass(true);
				}
				String udsNotAllowedCosIds = CacheManagerUtil.getParametersCacheManager()
						.getParameterValue(iRBTConstant.COMMON, "UDS_NOT_ALLOWED_COS_IDS", null);
				boolean isEasyTone = false;
				com.onmobile.apps.ringbacktones.content.Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
				boolean isSubActive = RBTDBManager.getInstance().isSubActive(subscriber);
				String cosID = selectionRequest.getCosID()!=null?selectionRequest.getCosID().toString():null;
				if(cosID == null && isSubActive){
					cosID = subscriber.cosID();
				}
				
				if ( cosID !=null && udsNotAllowedCosIds != null && !udsNotAllowedCosIds.isEmpty()) {
					List<String> udsNotAllowedCosId = Arrays.asList(udsNotAllowedCosIds.split(","));
					for(String cosId: udsNotAllowedCosId){
						if(cosId.equalsIgnoreCase(cosID)){
							isEasyTone = true;
							break;
						}
					}
				}
				if (selectionRequest.isUdsOn()) {
					if(isEasyTone){
						task.setObject(param_response, param_not_allowed);
						logger.info("rejecting request for easytoneUser: "+ cosID );
						return;
					}
					if (isSubActive) {
						SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
						HashMap<String, String> subUserInfoMap = new HashMap<String, String>();
						subUserInfoMap.put(UDS_OPTIN, "TRUE");
						subscriptionRequest.setUserInfoMap(subUserInfoMap);
						logger.info("Updating to UDS use subscriberId: " + subscriberID );
						RBTClient.getInstance().updateSubscription(subscriptionRequest);
					} else {
						userInfoMap.put(UDS_OPTIN, "TRUE");
						selectionRequest.setUserInfoMap(userInfoMap);
					}
				}
				
				RBTClient.getInstance()
						.addSubscriberSelection(selectionRequest);
				String response = selectionRequest.getResponse();
				task.setObject(param_response, response);
			}

		} catch (Exception ex) {
			task.setObject(param_response, param_error);
			logger.info("Exception while processing SDP Indirect Consent Selection");
		}

	}
	
	public String getseapiType(String subscriberID, String orderType, boolean isActRequest, boolean isSelRequest){
		if (orderType == null)
			return null;
		String seapitype = null;
		com.onmobile.apps.ringbacktones.content.Subscriber subscriber = RBTDBManager
				.getInstance().getSubscriber(subscriberID);
		boolean isSubActive = RBTDBManager.getInstance()
				.isSubActive(subscriber);
		if(isSubActive && orderType.equalsIgnoreCase("A") && isSelRequest){
			seapitype = "R";
		}else if(!isSubActive && orderType.equalsIgnoreCase("A") && isActRequest){
			seapitype = "R";
		}else if (isSubActive && orderType.equalsIgnoreCase("U")) {
			seapitype = "U";
		} else if (!isSubActive && orderType.equalsIgnoreCase("U")) {
			seapitype = "R";
		} else if (orderType.equalsIgnoreCase("R")) {
			seapitype = "R";
		} else if (!isSubActive && orderType.equalsIgnoreCase("C")) {
			seapitype = "C";
		} else if (isSubActive && orderType.equalsIgnoreCase("C")) {
			seapitype = "R";
		}
		return seapitype;
	}
	
	public String processGiftConsentIntegration(Task task){
		try {
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode.toLowerCase());
			String categoryID = task.getString("categoryID");
			String callerID = task.getString("callerID");
			String clipID = task.getString("clipID");
			String language = task.getString("language");
			String subscriptionClass = task.getString("subscriptionClass");
			String chargeClass = task.getString("chargeClass");
			String gifteeID = task.getString("gifteeID");
			String isGifteeActive = task.getString(WebServiceConstants.param_IsGifteeActive);
			String gifteeHub = task.getString(WebServiceConstants.param_GifteeHub);
			
			
			GiftRequest giftRequest = new GiftRequest();
	        giftRequest.setMode(mode);
	        giftRequest.setGifteeID(gifteeID);
	        giftRequest.setGifterID(subscriberID);
	        giftRequest.setToneID(clipID);
	        giftRequest.setCategoryID(categoryID);
	        giftRequest.setSubscriptionClass(subscriptionClass);
	        giftRequest.setChargeClass(chargeClass);
	        
	        boolean isConsentFlow = false;
	        //Validate B partySubscriber
	        SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(gifteeID, "GIFT"));
	        if(!subscriberDetail.isValidSubscriber()) {
	        	if(isGifteeActive == null || (isGifteeActive=isGifteeActive.trim()).length() == 0 
	        		|| gifteeHub == null || (gifteeHub=gifteeHub.trim()).length() == 0){	        	
	        		return "IVALID_REQUEST";
	        	}
	        	else if(isGifteeActive.equalsIgnoreCase(NO) || isGifteeActive.equalsIgnoreCase("n")) {
	        		isConsentFlow = true;
	        	}
	        	HashMap<String,String> infoMap = new HashMap<String, String>();
		        infoMap.put(WebServiceConstants.param_IsGifteeActive, isGifteeActive);
		        infoMap.put(WebServiceConstants.param_GifteeHub, gifteeHub);
		        infoMap.put("isComvivaCircle", "true");
		        giftRequest.setInfoMap(infoMap);
	        }
	        else {
	        	//hit prism the check the status of B user

	        	WebServiceContext webServiceContext = new WebServiceContext();
	        	webServiceContext.put(param_subscriberID, gifteeID);
	        	HashMap<String, String> resultMap = Utility.getNextBillingDateOfServices(webServiceContext);
	        	if(resultMap == null || !resultMap.containsKey(gifteeID+"_substatus") || !resultMap.get(gifteeID+"_substatus").equalsIgnoreCase("ACTIVE")){
	        		isConsentFlow = true;
	        	}
	        }
	        
	        giftRequest.setIsConsentFlow(isConsentFlow);
	        Rbt rbt = RBTClient.getInstance().sendGiftWithoutGifteeValidation(giftRequest);
	        
			//TO-DO callwebservice api to gift consent
			Consent consent = null;
			if (rbt != null) {
				consent = rbt.getConsent();
			}
			
			logger.info("Added consent selection. consent: " + consent
					+ ", selectionRequest: " + giftRequest);
			
			String response = giftRequest.getResponse();
			boolean isActive = !isConsentFlow;
			String consentChargeClass = null;
			String consentSubscriptionClass = null;
			String consentId = null;
			String consentCategoryId = null;
			String consentClipId = null;
			String consentMode = null;
			String promo_id = null;
			if (consent != null) {
				consentChargeClass = consent.getChargeclass();
				consentSubscriptionClass = consent.getSubClass();
				consentId = consent.getTransId();
				consentCategoryId = consent.getCatId();
				consentClipId = consent.getClipId();
				consentMode = consent.getMode();
				promo_id = consent.getPromoId();
			}
			logger.info("ConsentSelectionIntegration :: consentChargeClass = " + consentChargeClass
					+ ", consentSubscriptionClass = " + consentSubscriptionClass + ", consentId = "
					+ consentId + ", consentCategoryId = " + consentCategoryId + ", consentClipId = "
					+ consentClipId + ", consentMode =" + consentMode + ", isActive = " + isActive + ", response = " + response);
			if (task.containsKey(WebServiceConstants.param_useSameResForConsent)
					&& task.getString(WebServiceConstants.param_useSameResForConsent)
							.equalsIgnoreCase(WebServiceConstants.YES)) {
				WebServiceContext webSrvContext = new WebServiceContext();
				webSrvContext.put(WebServiceConstants.param_response, response);
				webSrvContext.put(WebServiceConstants.param_subscriberID, subscriberID);
				webSrvContext.put(WebServiceConstants.param_mode, consentMode);
				webSrvContext.put(WebServiceConstants.param_clipID, consentClipId);
				webSrvContext.put(WebServiceConstants.param_promoID, promo_id);
				webSrvContext.put(WebServiceConstants.param_categoryID, consentCategoryId);
				webSrvContext.put("CONSENTID", consentId);
				webSrvContext.put("CONSENTCLASSTYPE", consentChargeClass);
				webSrvContext.put("CONSENTSUBCLASS", consentSubscriptionClass);
				if (isActive) {					 
					webSrvContext.put("USER_ACTIVE_SEL_CON_INT", "true");
				}
				if(callerID != null) {
					webSrvContext.put(WebServiceConstants.param_callerID, callerID);
				}
				ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(consentChargeClass);
				SubscriptionClass subClassObj = CacheManagerUtil.getSubscriptionClassCacheManager()
						.getSubscriptionClass(consentSubscriptionClass);
				String pricePoint = null;
				String priceValidity = null;
				if (subClassObj != null) {
					pricePoint = subClassObj.getSubscriptionAmount() + "|";
					priceValidity = subClassObj.getSubscriptionPeriod() + "|";
				}
				if (chargeClassObj != null) {
					pricePoint += chargeClassObj.getAmount();
					priceValidity += chargeClassObj.getSelectionPeriod();
				}
				pricePoint = pricePoint != null ? pricePoint : "";
				priceValidity = priceValidity != null ? priceValidity : "";
				webSrvContext.put("price", pricePoint);
				webSrvContext.put("priceValidity", priceValidity);
				Document document = RBTAdminFacade.getRBTInformationObject(webSrvContext).getSelIntegrationPreConsentResponseDocument(webSrvContext);
				return (XMLUtils.getStringFromDocument(document));
			} else {
				return  getSelIntegrationPreConsentResponseDocument(
					subscriberID, consentChargeClass, consentSubscriptionClass, consentId,
					consentCategoryId, consentClipId, consentMode, promo_id, isActive, response,callerID,gifteeID);
			}
		}catch(Exception ex){
			logger.error("Exception: " , ex);
		}
		return null;
		
	}
	
	public String processAcceptGiftConsentIntegration(Task task){
		try {

			String gifteeID = task.getString(param_subscriberID);
			String gifterID = task.getString(WebServiceConstants.param_gifterID);
			String giftSentTime = task.getString(WebServiceConstants.param_giftSentTime);
			String toneId = task.getString("clipID");
			String categoryId = task.getString("categoryID");
			
			if(toneId != null) {
				Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(toneId);
				if(clip == null) {
					clip = RBTCacheManager.getInstance().getClip(toneId);
				}
				
				if(clip != null) {
					toneId = clip.getClipId() + "";
				}
			}

			boolean isGifteeActive = true;
			
			WebServiceContext webServiceContext = new WebServiceContext();
        	webServiceContext.put(param_subscriberID, gifteeID);
        	HashMap<String, String> resultMap = Utility.getNextBillingDateOfServices(webServiceContext);
        	if(resultMap == null || !resultMap.containsKey(gifteeID+"_substatus") || !resultMap.get(gifteeID+"_substatus").equalsIgnoreCase("ACTIVE")){
        		isGifteeActive = false;
        	}

   	
        	SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			Date sentTime = null;
			if(giftSentTime != null) {
				sentTime = dateFormat.parse(giftSentTime);
			}
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			if (!isGifteeActive) {
				
				
				ViralSMSTable gift = rbtDBManager.getViralPromotion(
						gifterID, gifteeID, sentTime, "GIFTED", toneId);
				
				if(gift == null) {
					return "GIFT_INVALID";
				}
				
				String extraInfo = gift.extraInfo();
				Map<String, String> giftExtraInfo = DBUtility.getAttributeMapFromXML(extraInfo);
				
				String subscriptionClass = giftExtraInfo.get(SUBSCRIPTION_CLASS);
				String chargeClass = giftExtraInfo.get(CHARGE_CLASS);				
				String mode = gift.selectedBy();
				String toneID = gift.clipID();
				
				giftExtraInfo.put(WebServiceConstants.param_giftSentTime, giftSentTime);
				extraInfo = DBUtility.getAttributeXMLFromMap(giftExtraInfo);
				
				Clip clip = RBTCacheManager.getInstance().getClip(toneID);
				String promo_id = null;
				if(clip != null) {
					promo_id = clip.getClipPromoId();
				}
				
				
				String consentUniqueId = com.onmobile.apps.ringbacktones.services.common.Utility.generateConsentIdRandomNumber(gifteeID);
				if (consentUniqueId == null) {
					consentUniqueId = UUID.randomUUID().toString();
				}
				boolean isSuccess = rbtDBManager.makeEntryInConsent(consentUniqueId, gifteeID, gifterID, null, subscriptionClass, mode, null, null, 1, chargeClass, null, 
						null, toneID, null, 0, 2359, null, 0, false, null, false, 7, null, true, null,
						null, 1, null, null, new Date(), extraInfo, "GIFT_ACCEPT",	1);
				
				String response = "error";
				if(isSuccess) {
					response = "success";
					rbtDBManager.removeViralPromotion(gifterID, gifteeID, sentTime, "GIFTED");
				}
				
				return  getSelIntegrationPreConsentResponseDocument(
						gifteeID, chargeClass, subscriptionClass, consentUniqueId,
						categoryId, toneID, mode, promo_id, false, response, null, null);
			}
			else {
				
				ViralSMSTable gift = rbtDBManager.getViralPromotion(
						gifterID, gifteeID, sentTime, "GIFTED", toneId);
				
				SelectionRequest selectionRequest = new SelectionRequest(gifteeID);
				selectionRequest.setGifterID(gifterID);;
				selectionRequest.setGiftSentTime(sentTime);
				selectionRequest.setCategoryID(categoryId);
				selectionRequest.setClipID("" + gift.clipID());
				RBTClient.getInstance().acceptGift(selectionRequest);
				return selectionRequest.getResponse();
			}

		}catch(Exception ex){
			logger.error("Exception: " , ex);
		}
		return null;		
	}

	private String getCosChargeClass(CosDetails cosDetails) {

		String[] chargeClassTokens = cosDetails.getFreechargeClass().split(",");
		if (chargeClassTokens != null) {
			for (String chargeClassToken : chargeClassTokens) {
				int startIndex = chargeClassToken.indexOf('*');
				if (startIndex != -1) {
					String chargeClass = chargeClassToken.substring(0,
							startIndex);
					return chargeClass;
				} else {
					return chargeClassToken;
				}
			}

		}
		return null;
	}
	
}
