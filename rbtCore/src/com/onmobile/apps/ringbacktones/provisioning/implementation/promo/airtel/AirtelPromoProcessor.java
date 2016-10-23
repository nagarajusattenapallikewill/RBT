package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.airtel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * This class process' the promo requests for Airtel. Only change is the process third party requests 
 * 
 * @author Sreekar
 * 
 */
public class AirtelPromoProcessor extends PromoProcessor {
	public AirtelPromoProcessor() throws RBTException {
		super();
		logger = Logger.getLogger(AirtelPromoProcessor.class);
	}
	
	public Task getTask(HashMap<String, String> requestParams) {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		if (requestParams.containsKey(param_REQUEST)) {
			return super.getTask(requestParams);
		}

		Task task = new Task(requestParams.get("taskAction"), taskSession);
		logger.info("RBT:: task: " + task);
		return task;
	}
	
	@Override
	/**
	 * This method doesn't process anything as parameters can be different for different requests 
	 */
	public String validateParameters(Task task) {
		if(task.containsKey(param_REQUEST)){
				
			if(task.getString(param_REQUEST).equalsIgnoreCase(STATUS))
			{	
				task.setObject(param_mode, "CCC");
				task.setObject(param_consent_status, "TRUE");
				logger.info("added mode ccc " + task);
			}
			return super.validateParameters(task);
		}
		
		String taskAction = task.getTaskAction();
		String redirectURL = null;
		if(taskAction.equals(request_mod))
			redirectURL = "mod.jsp";
		else if(taskAction.equals(request_envio))
			redirectURL = "envio.jsp";
		else if(taskAction.equals(request_autodial))
			redirectURL = "autodial.jsp";
		else if(taskAction.equals(request_ec))
			redirectURL = "easycharge.jsp";
		else if(taskAction.equals(request_ussd))
			redirectURL = "ussd.jsp";
		task.setObject(param_URL, redirectURL);
		return "VALID";
	}
	
	/**
	 * @author Sreekar
	 * 
	 * Processess all the third party requests like Airtel's mod,ussd,ec,envio,autodial etc
	 * @param task The task having all the request params sent in the HTTP request
	 */
	public void processThirdPartyRequest(Task task) {
		String taskAction = task.getTaskAction();
		logger.info("RBT::action-" + taskAction + "::params-" + task.getTaskSession());
		String response = null;
		if(taskAction.equals(request_mod))
			response = processMODRequest(task);
		else if(taskAction.equals(request_envio))
			response = processEnvioRequest(task);
		else if(taskAction.equals(request_autodial))
			response = processAutodialRequest(task);
		else if(taskAction.equals(request_ec))
			response = processEasyChargeRequest(task);
		else if(taskAction.equals(request_ussd))
			response = processUSSDRequest(task);
		
		if(response != null)
			task.setObject(param_response, response);
	}
	
	/**
	 * Process' the http USSD requests for Airtel
	 * 
	 * @author Sreekar
	 * @param task contains all the http parameters sent by the requester
	 * @return response as String
	 */
	private String processUSSDRequest(Task task) {
		String msisdn = task.getString(param_ussd_srcMsisdn);
		if (msisdn == null || msisdn.equals("") || msisdn.equalsIgnoreCase("null")
				|| msisdn.length() != 12)
			return Resp_InvalidPrefix;
		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_subscriber, subscriber);
		
		String blockedSubStatus = getSubscriberBlockedStatus(subscriber);
		if(blockedSubStatus != null)
			return blockedSubStatus;
		String strVCode = task.getString(param_ussd_vcode);
		String cmd = task.getString(param_ussd_cmd);
		if(cmd == null) {
			task.setObject(param_invalidParam, param_ussd_cmd);
			return Resp_invalidParam;
		}
		
		Clip clip = getClipByWavFile(Utility.getWavFileFromVCode(strVCode));
		if (strVCode != null) {
			if (clip == null && !cmd.equalsIgnoreCase("gift"))
				return Resp_ClipNotAvailable;
			else if (clip != null && clip.getClipEndTime().before(new Date()) && !cmd.equalsIgnoreCase("gift"))
				return Resp_ClipExpired;
			task.setObject(param_clip, clip);
		}
		
		String chg = task.getString(param_ussd_chg);
		String categoryID = getUSSDChargedCategory();
		if(chg != null && chg.equals("0"))
			categoryID = getUSSDFreeCategory();
		
		task.setObject(param_ACTIVATED_BY, "USSD");
		task.setObject(param_CATEGORY_ID, categoryID);
		String advRentalPack = null;
		if(cmd.equalsIgnoreCase("subchk"))
			return Resp_Success;
		else if(cmd.equalsIgnoreCase("set") || cmd.equalsIgnoreCase("set_caller"))
			return processUSSDSelection(task);
		else if(cmd.equalsIgnoreCase("unsub")) {
			task.setObject(param_DEACTIVATED_BY, "USSD");
			processDeactivation(task);
		}
		else if (cmd.equalsIgnoreCase("top10"))
			return Resp_Success;
		else if (cmd.equalsIgnoreCase("freezone"))
			return Resp_Success;
		else if((advRentalPack = getUSSDAdvRentalPack(cmd)) != null) {
			task.setObject(param_ADVANCE_RENTAL_CLASS, advRentalPack);
			task.setObject(param_ISACTIVATE, "true");
			processActivation(task);
		}
		else if(cmd.equalsIgnoreCase("gift"))
		{
			clip = (Clip)task.getObject(param_clip);
			getCategoryAndClipForPromoID(task, strVCode);
			Category cat = null;
			if(task.getObject(CAT_OBJ) != null )
			{	
				cat = (Category)task.getObject(CAT_OBJ);
				task.setObject(param_catid, cat.getCategoryId()+"");
				task.remove(param_clip);
			}
			else
			{
				//Nothing to be done here as clip is already populated
			}
			if (strVCode != null) {
				if (clip == null && cat == null)
					return Resp_ClipNotAvailable;
				else if ((clip != null && clip.getClipEndTime().before(new Date())) || (cat != null && cat.getCategoryEndTime().before(new Date())))
					return Resp_ClipExpired;
				if(cat != null)
					task.setObject(param_category, cat);
				else if(clip != null)
					task.setObject(param_clip, clip);
			}
			return processUSSDGift(task);
		}
		else if(cmd.equalsIgnoreCase("copy"))
			return processUSSDCopy(task);
		else {
			task.setObject(param_invalidParam, param_ussd_cmd);
			return Resp_invalidParam;
		}
		return null;
	}
	
	private String processUSSDCopy(Task task) {
		task.setObject(param_MSISDN, task.getString(param_ussd_srcMsisdn));
		task.setObject(param_CALLER_ID, task.getString(param_ussd_dstMsisdn));
		task.setObject(param_SELECTED_BY, "USSD");
		if(task.containsKey(param_ussd_cbsMsisdn))
			task.setObject(param_SPECIAL_CALLER_ID, task.getString(param_ussd_cbsMsisdn));
		processCopyRequest(task);
		return task.getString(param_response);
	}
	
/*	private String processUSSDGift(Task task) {
		String gifterID = task.getString(param_ussd_dstMsisdn);
		String subscriberID = task.getString(param_ussd_srcMsisdn);
		if(gifterID == null || gifterID.equals(subscriberID)) {
			task.setObject(param_invalidParam, param_ussd_dstMsisdn);
			return Resp_invalidParam;
		}
		Subscriber gifter = rbtClient.getSubscriber(new RbtDetailsRequest(gifterID));
		String gifterStatus = gifter.getStatus();
		if(gifterStatus != null && !gifterStatus.equals(WebServiceConstants.ACTIVE)) {
			task.setObject(param_invalidParam, param_ussd_dstMsisdn);
			return Resp_invalidParam;
		}
		if(!task.containsKey(param_clip))
			return Resp_ClipNotAvailable;
		
		task.setObject(param_callerid, gifterID);
		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_SMSTYPE, "GIFT");
		task.setObject(param_CLIPID, ((Clip)task.getObject(param_clip)).getClipId());
		task.setObject(param_SELECTED_BY, "USSD");
		addViraldata(task);
		return null;
	}
*/	
	
	private String processUSSDGift(Task task) {
		String gifterID = task.getString(param_ussd_dstMsisdn);
		String subscriberID = task.getString(param_ussd_srcMsisdn);
		if(!task.containsKey(param_clip)&& !task.containsKey(param_category))
			return Resp_ClipNotAvailable;
		
		task.setObject(param_MSISDN, subscriberID);
		task.setObject(param_CALLER_ID, gifterID);
		task.setObject(param_SELECTED_BY, "USSD");
		String response = processGift(task);
		if(response.equals(Resp_giftGifterNotActive)) {
			task.setObject(param_invalidParam, param_ussd_dstMsisdn);
			return Resp_invalidParam;
		}
		if(!response.equals(Resp_Success) && !response.equals(Resp_Failure) && !response.equals(Resp_Err))
			return Resp_Failure;
		return response;
	}
	
	private String getUSSDAdvRentalPack(String cmd) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "USSD_ADVANCE_RENTAL");
		if(param != null) {
			StringTokenizer stk = new StringTokenizer(param.getValue(), ",");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				int index = token.indexOf(";");
				String thisCmd = token.substring(0, index);
				if(cmd.equalsIgnoreCase(thisCmd))
					return token.substring(index + 1);
			}
		}
		return null;
	}
	
	private String processUSSDSelection(Task task) {
		Clip clip = (Clip)task.getObject(param_clip);
		if(clip == null)
			return Resp_ClipNotAvailable;
		task.setObject(param_clip, clip);
		String cmd = task.getString(param_ussd_cmd);
		if(cmd.equalsIgnoreCase("set_caller")) {
			if(!task.containsKey(param_ussd_cbsMsisdn)) {
				task.setObject(param_invalidParam, param_ussd_cbsMsisdn);
				return Resp_invalidParam;
			}
		}
		else if(cmd.equals("set"))
			task.remove(param_ussd_cbsMsisdn);
		if(task.containsKey(param_ussd_cbsMsisdn))
			task.setObject(param_CALLER_ID, task.getString(param_ussd_cbsMsisdn));
		task.setObject(param_ISACTIVATE, "true");
		processSelection(task);
		return null;
	}
	
	/**
	 * Process' the easy charge requests of Airtel
	 * 
	 * @author Sreekar
	 * @param task contains all the http parameters sent by the requester
	 * @return response for the request as String
	 */
	private String processEasyChargeRequest(Task task) {
		String msisdn = task.getString(param_ec_customer);
		if (msisdn == null || msisdn.equals("") || msisdn.equalsIgnoreCase("null")
				|| msisdn.length() > 15 || msisdn.length() < 7)
			return Resp_InvalidPrefix;
		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_subscriber, subscriber);
		
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
			return subStatus;
		
//		//ACWM code by Sreekar
		try {
			
			boolean allowOffer = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", iRBTConstant.ALLOW_GET_OFFER, "FALSE").getValue().equalsIgnoreCase("TRUE");
			if(!allowOffer) {
				allowOffer = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", iRBTConstant.ALLOW_ONLY_BASE_OFFER, "FALSE").getValue().equalsIgnoreCase("TRUE");
			}
			if(allowOffer) {
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.getSubscriberID());
				rbtDetailsRequest.setMode("EC");
				rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION+"");
				Boolean isPrepaid = subscriber.isPrepaid() ? true : false;
				rbtDetailsRequest.setIsPrepaid(isPrepaid);
				com.onmobile.apps.ringbacktones.webservice.client.beans.Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
				String offerID = CacheManagerUtil.getParametersCacheManager().getParameter(
						iRBTConstant.COMMON, "ACWM_OFFER_ID", "-100").getValue();
				if(offers != null && offers.length > 0 && offers[0].getOfferID().equals(offerID)) {
					logger.warn("RBT::got EC request for ACWM user. rejecting the same");
					return Resp_Err;
				}
			}
			
//			Offer[] smOffers = RBTSMClientHandler.getInstance().getOffer(msisdn, "EC",
//					Offer.OFFER_TYPE_SUBSCRIPTION, subscriber.isPrepaid() ? "p" : "b");
//			String offerID = CacheManagerUtil.getParametersCacheManager().getParameter(
//					iRBTConstant.COMMON, "ACWM_OFFER_ID", "-100").getValue();
//			if(smOffers != null && smOffers.length > 0 && smOffers[0].getOfferID().equals(offerID)) {
//				logger.warn("RBT::got EC request for ACWM user. rejecting the same");
//				return Resp_Err;
//			}
		}
		catch (Exception e) {
			logger.error("RBT::Exception while getting offer", e);
			return Resp_Err;
		}
//		//end of ACWM code
		
		int flag = Utility.getIntFromStr(task.getString(param_ec_Flag));
		if(flag == -1) {
			task.setObject(param_invalidParam, param_ec_Flag);
			return Resp_invalidParam;
		}
		
		String strVCode = task.getString(param_ec_vcode);
		String strCCode = task.getString(param_ec_ccode);
		Clip clip = null;
		if (strVCode!= null && (strVCode.equals("") || strVCode.equalsIgnoreCase("null") || strVCode.equalsIgnoreCase("invalid") 
				|| strVCode.equalsIgnoreCase("unknown") || strVCode.equalsIgnoreCase("blank")))
			strVCode = null;
		if (strCCode!= null && (strCCode.equals("") || strCCode.equalsIgnoreCase("null") || strCCode.equalsIgnoreCase("invalid") 
				|| strCCode.equalsIgnoreCase("unknown") || strCCode.equalsIgnoreCase("blank")))
			strCCode = null;
		
		if(strVCode != null)
			clip = getClipByWavFile(Utility.getWavFileFromVCode(strVCode));
		if(strCCode != null) {
			if(strCCode.startsWith("HT"))
				strCCode = strCCode.substring(2);
			clip = getClipByPromoId(strCCode);
		}

		Date nowTime = new Date();
		if(flag == 1) { // all flags >=2 are advance packs
			if(clip == null)
				return Resp_ClipNotAvailable;
			else if(clip.getClipEndTime().before(nowTime))
				return Resp_ClipExpired;
		}
		else if(clip != null && clip.getClipEndTime().before(nowTime))
			clip = null;
		
		String status = subscriber.getStatus();
		if(clip != null)
			task.setObject(param_clip, clip);
		switch(flag) {
			case 0://activation + selection. Selection will be processed in the case 1. thats y there is no break in case 0
				if(status.equals(WebServiceConstants.ACTIVE))
					return Resp_AlreadyActive;
				String pendingStatus = getSubscriberPendingStatus(subscriber);
				if(pendingStatus != null)
					return pendingStatus;

				if(status.equalsIgnoreCase(WebServiceConstants.GRACE)) {
					return status;
				}
				
				ViralData[] viral = rbtClient.getViralData(new DataRequest(msisdn, "EC"));
				if (viral != null) {
					for (int i = 0; i < viral.length; i++) {
						if (viral[i].getClipID() == null)
							return Resp_AlreadyActive;
					}
				}
				task.setObject(param_callerid, msisdn);
				task.setObject(param_subscriberID, task.getString(param_ec_reseller));
				task.setObject(param_SMSTYPE, "EC");
				addViraldata(task);
				if(clip == null)// no clip requested, so giving success for activation
					return Resp_Success;
			case 1://selection
				if(status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)) {
						return Resp_DeactPending;					
				}
				
				String baseStatusForSelBlock = RBTParametersUtils.getParamAsString("COMMON", "BASE_STATUS_FOR_BLOCKING_EC_SELECTION", null);
				String baseStatusErrorResponse = RBTParametersUtils.getParamAsString("COMMON", "BASE_STATUS_EC_ERROR_RESP_MAP", null);
				List<String> baseStatusForSelBlockList = ListUtils.convertToList(baseStatusForSelBlock == null? baseStatusForSelBlock : baseStatusForSelBlock.toLowerCase(), ",");
				Map<String, String> baseStatusErrorResponseMap = MapUtils.convertIntoMap(baseStatusErrorResponse == null? baseStatusErrorResponse : baseStatusErrorResponse.toUpperCase(), ";",":",null);
				if(baseStatusForSelBlockList != null && baseStatusForSelBlockList.contains(status.toLowerCase())){
					String returnString = baseStatusErrorResponseMap.get(status.toUpperCase());
					return returnString == null? Resp_Failure : returnString;
				}
				
				return processECSelection(task);
			case 2:// advance rental case
			case 3:
			case 4:
			case 5:
			case 6:
			case 12:
			case 13:
			case 14:
			case 11:
				return processECAdvanceRental(task);
			case 7:
				return processOptinShufflePack(task);
			default:
				task.setObject(param_invalidParam, param_ec_Flag);
				return Resp_invalidParam;
		}
	}
	
	private String processECAdvanceRental(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subClass = getECAdvRentalPack(task.getString(param_ec_Flag));
		if (subClass == null) {
			task.setObject(param_invalidParam, "tariffCode");
			return Resp_invalidParam;
		}

		if (!Utility.isDeactiveSubscriber(subscriber.getStatus())) {
			if (subscriber.getUserType().equals("1"))
				return Resp_AlreadyActive;
			if (isAdvanceRentalSubClass(subscriber.getSubscriptionClass()))
				return Resp_AlreadyActive;
		}

		task.setObject(param_ACTIVATED_BY, "EC");
		task.setObject(param_ACTIVATION_INFO, "EC:" + task.getString(param_ec_reseller));
		task.setObject(param_ADVANCE_RENTAL_CLASS, subClass);
		task.setObject(param_ISACTIVATE, "true");
		if (task.containsKey(param_clip)) {
			task.setObject(param_clip, (Clip) task.getObject(param_clip));
			task.setObject(param_CATEGORY_ID, getECCategory());
			processSelection(task);
		}
		else
			processActivation(task);
		return task.getString(param_response);
	}
	
	private String processOptinShufflePack(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String shuffleID = task.getString(param_ec_ccode);
		if (shuffleID == null || shuffleID.length() == 0)
		{
			logger.info("ShuffleID is null, so returning response : " + Resp_invalidParam);
			return Resp_invalidParam;
		}

		String subClass = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "EASY_CHARGE_SHUFFLE_OPTIN_SUB_CLASS", null).getValue();
		if (subClass == null)
		{
			logger.info("Shuffle Optin subscriptionClass is not configured, so returning response : " + Resp_Err);
			return Resp_Err;
		}

		String freeChargeClass = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "FREE_CHARGE_CLASS_FOR_EC_SHUFFLE_OPTIN", "FREE").getValue();

		task.setObject(param_SUBSCRIPTION_CLASS, subClass);
		task.setObject(param_CHARGE_CLASS, freeChargeClass);
		task.setObject(param_USE_UI_CHARGE_CLASS, "TRUE");
		task.setObject(param_CATEGORY_ID, shuffleID);
		task.setObject(param_ISACTIVATE, "TRUE");
		task.setObject(param_ACTIVATED_BY, "EC");
		if (!Utility.isDeactiveSubscriber(subscriber.getStatus()))
		{
			task.setObject(param_ADVANCE_RENTAL_CLASS, subClass);
		}

		processSelection(task);

		logger.info("Response while activating the user on shuffle optin pack : " + task.getString(param_response));
		return task.getString(param_response);
	}
	
	private String processECSelection(Task task) {
		Clip clip = (Clip)task.getObject(param_clip);
		if(clip == null)
			return Resp_ClipNotAvailable;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String msisdn = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		boolean subRequested = !canActivateUserWithStatus(status);
		boolean clipRequested = false;
		ViralData[] viral = rbtClient.getViralData(new DataRequest(msisdn, "EC"));
		for(int i = 0; viral != null && i < viral.length; i++) {
			if(viral[i].getClipID() == null)
				subRequested = true;
			else if(viral[i].getClipID().equals(clip.getClipId()+""))
				clipRequested = true;
		}
		Library library = rbtClient.getLibrary(new RbtDetailsRequest(msisdn));
		Settings settings = library.getSettings();
		Setting[] allSettings = settings.getSettings();
		for(int i = 0; allSettings != null && i < allSettings.length; i++) {
			String wavFile = allSettings[i].getRbtFile();
			int index = wavFile.length();
			if(wavFile != null && (index = wavFile.indexOf(".wav")) != -1){
				wavFile = wavFile.substring(0, index);
			}
			if((allSettings[i].getCallerID() == null || allSettings[i].getCallerID().equalsIgnoreCase("ALL")) && allSettings[i].getStatus() == 1 && wavFile.equals(clip.getClipRbtWavFile()))
				clipRequested = true;
		}
		if(!subRequested)
			return Resp_Inactive;
		if(clipRequested)
			return Resp_SelExists;
		task.setObject(param_callerid, msisdn);
		task.setObject(param_subscriberID, task.getString(param_ec_reseller));
		task.setObject(param_SMSTYPE, "EC");
		task.setObject(param_CLIPID, clip.getClipId()+"");
		addViraldata(task);
		return Resp_Success;
	}
	
	private String processAutodiaInboundRequest(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		String vCode = task.getString(param_auto_vcode);
		Clip clip = getClipByWavFile(vCode);
		if(clip == null)
			return Resp_ClipNotAvailable;
		task.setObject(param_clip, clip);
		task.setObject(param_CATEGORY_ID, getAutodialCategoryLower());
		task.setObject(param_ISACTIVATE, "true");
		task.setObject(param_SUBSCRIPTION_CLASS, getAutodialSubClass());
		//activateBy and activationInfo is populated in processAutodialRequest itself
		processSelection(task);
		return task.getString(param_response);
	}
	
	/**
	 * Process' the autodial requests of Airtel 
	 * 
	 * @author Sreekar
	 * @param task contains all the http request params
	 * @return localized response as string
	 */
	private String processAutodialRequest(Task task) {
		String msisdn = task.getString(param_auto_msisdn);
		if(msisdn == null || msisdn.equals("") || msisdn.equalsIgnoreCase("null"))
			return Resp_InvalidPrefix;
		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_subscriber, subscriber);
		logger.info("RBT::taskSession-" + task.getTaskSession());
		task.setObject(param_REFUND, "true");
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
			return subStatus;
		String sTypeStr = task.getString(param_auto_stype);
		int sType = Utility.getIntFromStr(sTypeStr);
		if(sTypeStr == null || sTypeStr.equals(""))
			sType = -2;
		String actByStr = getAutodialActivatedBy(task.getString(param_auto_syscode));
		String actInfoStr = task.getString(param_auto_syscode); 
		task.setObject(param_ACTIVATED_BY, actByStr);
		task.setObject(param_ACTIVATION_INFO, actInfoStr);
		logger.info("RBT::sType-" + sType);
		String strVCode = task.getString(param_auto_vcode);
		if (strVCode!= null && (strVCode.equals("") || strVCode.equalsIgnoreCase("null") || strVCode.equalsIgnoreCase("invalid") 
				|| strVCode.equalsIgnoreCase("unknown") || strVCode.equalsIgnoreCase("blank")))
			strVCode = null;
		
		if(task.containsKey(param_auto_cLogs))
			task.setObject(param_CONSENT_LOG, task.getString(param_auto_cLogs));
		
		if(sType == -2)
			return processAutodiaInboundRequest(task);
		
		int uCode = Utility.getIntFromStr(task.getString(param_auto_ucode));
		String callerID = task.getString(task.getString(param_auto_caller));
		if(uCode == 0)
			callerID = null;
		if(callerID != null && uCode == 1) {
			int callerIDInt = Utility.getIntFromStr(callerID);
			if(callerID.length() != 10 || callerIDInt == -1) {
				task.setObject(param_invalidParam, param_auto_caller);
				return Resp_invalidParam;
			}
		}
		Clip clip = null;
		if (sType != 6 && sType != 7) {
			if (sType != 4) {
				if (strVCode == null)
					return Resp_ClipNotAvailable;
				clip = getClipByWavFile(Utility.getWavFileFromVCode(strVCode));
				if (clip == null || clip.getClipEndTime().before(new Date()))
					return Resp_ClipNotAvailable;
			}
			else {
				if (strVCode != null) {
					clip = getClipByWavFile(Utility.getWavFileFromVCode(strVCode));
					if (clip == null || clip.getClipEndTime().before(new Date()))
						return Resp_ClipNotAvailable;
				}
			}
		}
		if(sType != 4 && uCode != 0 && uCode != 1) {
			task.setObject(param_invalidParam, param_auto_ucode);
			return Resp_invalidParam;
		}
		String suspendedStatus = getSubscriberBlockedStatus(subscriber);
		if(suspendedStatus != null)
			return suspendedStatus;
		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if(pendingStatus != null)
			return pendingStatus;
		String status = subscriber.getStatus();
		populateActivatedBySelectedBy(task);
		String albumCode = task.getString(param_auto_AlbumCode);
		if(task.containsKey(param_auto_albumcode))
			albumCode = task.getString(param_auto_albumcode);
		switch(sType) {
			case 1://subscription + selection
				if(!canActivateUserWithStatus(status))
					return Resp_AlreadyActive;
				task.setObject(param_clip, clip);
				task.setObject(param_CATEGORY_ID, getAutodialCategory());
				task.setObject(param_ISACTIVATE, "true");
				task.setObject(param_SUBSCRIPTION_CLASS, getAutodialSubClass());
				if(callerID != null)
					task.setObject(param_CALLER_ID, callerID);
				processSelection(task);
				break;
			case 2:
			case 3:
				if(canActivateUserWithStatus(status))
					return Resp_Inactive;
				task.setObject(param_clip, clip);
				if(sType == 2)
					task.setObject(param_CATEGORY_ID, getAutodialCategoryLower());
				else
					task.setObject(param_CATEGORY_ID, getAutodialCategoryUpper());
				processSelection(task);
				break;
			case 4:
				String flag = task.getString(param_auto_flag);
				String advSubClassCategory = getAutodialAdvSubClass(flag);
				if(flag == null || advSubClassCategory == null) {
					task.setObject(param_invalidParam, param_auto_flag);
					return Resp_invalidParam;
				}
				int index = advSubClassCategory.indexOf(",");
				String subClass = advSubClassCategory.substring(0, index);
				String categoryID = advSubClassCategory.substring(index + 1);
				if(canActivateUserWithStatus(status))
					task.setObject(param_SUBSCRIPTION_CLASS, subClass);
				else {
					String subscriberClass = subscriber.getSubscriptionClass();
					if(isAdvanceRentalSubClass(subscriberClass))
						return Resp_AlreadyActive;
					if(isAlbumRentalSubClass(subscriberClass))
						return Resp_InvalidNumber;
					task.setObject(param_ADVANCE_RENTAL_CLASS, subClass);
					task.setObject(param_ISACTIVATE, "true");
				}
				if(clip == null)
					processActivation(task);
				else {
					task.setObject(param_clip, clip);
					task.setObject(param_CATEGORY_ID, categoryID);
					task.setObject(param_ISACTIVATE, "true");
					if(callerID != null)
						task.setObject(param_CALLER_ID, callerID);
					processSelection(task);
				}
				break;
			case 5:
				if(!canActivateUserWithStatus(status))
					return Resp_AlreadyActive;
				task.setObject(param_clip, clip);
				task.setObject(param_CATEGORY_ID, getECCategory());
				task.setObject(param_ISACTIVATE, "true");
				if(callerID != null)
					task.setObject(param_CALLER_ID, callerID);
				processSelection(task);
				break;
			case 6:
				if(!canActivateUserWithStatus(status))
					return Resp_AlreadyActive;
				Category albumCategory = getCategoryByPromoId(albumCode);
				if(albumCategory == null) {
					task.setObject(param_invalidParam, "albumCode");
					return Resp_invalidParam;
				}
				task.setObject(param_CATEGORY_ID, albumCategory.getCategoryId()+"");
				task.setObject(param_ISACTIVATE, "true");
				if(callerID != null)
					task.setObject(param_CALLER_ID, callerID);
				processSelection(task);
				break;
			case 7:
				if(canActivateUserWithStatus(status))
					return Resp_Inactive;
				if(isAdvanceRentalSubClass(subscriber.getSubscriptionClass()))
					return Resp_Err;
				Category albCategory = getCategoryByPromoId(albumCode);
				if(albCategory == null) {
					task.setObject(param_invalidParam, "albumCode");
					return Resp_invalidParam;
				}
				task.setObject(param_CATEGORY_ID, albCategory.getCategoryId()+"");
				task.setObject(param_ISACTIVATE, "true");
				if(callerID != null)
					task.setObject(param_CALLER_ID, callerID);
				processSelection(task);
				break;
			default:
				task.setObject(param_invalidParam, param_auto_stype);
				return Resp_invalidParam;
		}
		return null;
	}
	
	/**
	 * @author Sreekar
	 * 
	 * Processess the Airtel MOD request
	 * @param task The task having all the request params sent in the HTTP request
	 */
	private String processMODRequest(Task task) {
		String msisdn = task.getString(param_mod_msisdn);
		if(msisdn == null || msisdn.equals("") || msisdn.equalsIgnoreCase("null"))
			return Resp_InvalidPrefix;
		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_subscriber, subscriber);
		
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
			return subStatus;
		
		subStatus = getSubscriberPendingStatus(subscriber);
		if(subStatus != null)
			return subStatus;
		
		String sCode = task.getString(param_mod_scode);
		if(sCode == null || (sCode.length() != 15 && sCode.length() != 6))
			return Resp_ClipNotAvailable;
		String channel = task.getString(param_mod_channel);
		if(channel == null || channel.equals(""))
			channel = "MOD";
		String wavFile = Utility.getWavFileFromVCode(sCode);
		Clip clip = getClipByWavFile(wavFile);
		if(clip == null)
			return Resp_ClipNotAvailable;
		if(clip.getClipEndTime().before(new Date()))
			return Resp_ClipExpired;
		
		task.setObject(param_WAV_FILE, wavFile);
		task.setObject(param_CATEGORY_ID, getMODCategory());
		task.setObject(param_ACTIVATED_BY, channel);
		task.setObject(param_ISACTIVATE, "true");
		processSelection(task);
		return null;
	}
	
	public void processSelection(Task task) {
		String callerID = task.getString(param_CALLER_ID);
		if(callerID != null && !checkMaxCallerSettings(task))
			task.setObject(param_response, Resp_maxCallerSel);
		else
			super.processSelection(task);
	}
	
	/**
	 * checks user's library for caller selections 
	 * 
	 * @author Sreekar
	 * @param task contains all the parameters required
	 * @return true if still 3 caller ID selections are not made
	 */
	protected boolean checkMaxCallerSettings(Task task) {
		Library library = getSubscrbierLibrary(task);
		Settings allSettings = null;
		String caller = task.getString(param_CALLER_ID);
		int counter = 0;
		if(library != null) {
			allSettings = library.getSettings();
			Setting[] allActiveSettings = allSettings.getSettings();
			for(int i = 0; allActiveSettings != null && i < allActiveSettings.length; i++) {
				if(!allActiveSettings[i].getCallerID().equalsIgnoreCase("all") && (caller != null && allActiveSettings[i].getCallerID().equalsIgnoreCase(caller)))
					counter++;
			}
		}
		return counter < Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "MAX_SPECIAL_SETTINGS", "3").getValue());
	}
	
	private String getMODCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "MOD_CATEGORY");
		if(param != null)
			return param.getValue();
		return "61";
	}
	
	private String getEnvioCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "ENVIO_CATEGORY");
		if(param != null)
			return param.getValue();
		return "61";
	}
	
	private String getEnvioSubClass() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "ENVIO_SUBSCRIPTION_CLASS");
		if(param != null)
			return param.getValue();
		return "61";
	}
	
	private String getAutodialCategoryUpper() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_CATEGORY_UPPER");
		if(param != null)
			return param.getValue();
		return "53";
	}
	
	private String getAutodialCategoryLower() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_CATEGORY_LOWER");
		if(param != null)
			return param.getValue();
		return "54";
	}
	
	private String getAutodialCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_SUB_CLASS_CATEGORY");
		if(param != null) {
			String value = param.getValue();
			return value.substring(value.indexOf(",") + 1);
		}
		return "53";
	}
	
	private String getAutodialSubClass() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_SUB_CLASS_CATEGORY");
		if(param != null) {
			String value = param.getValue();
			return value.substring(0, value.indexOf(","));
		}
		return "AUTODIALER";
	}
	
	private String getAutodialActivatedBy(String syscode) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_ACTIVATED_BY_IP_MAP");
		if(param != null) {
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				int index = token.indexOf("=");
				String actBy = token.substring(0, index);
				StringTokenizer stkInner = new StringTokenizer(token.substring(index + 1), ",");
				while(stkInner.hasMoreTokens()) {
					String ip = stkInner.nextToken();
					if(ip.equals(syscode))
						return actBy;
				}
			}
		}
		return "OBD";
	}
	
	private String getAutodialAdvSubClass(String flag) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "AUTO_FLAG_SUB_CLASS_MAP");
		if(param != null) {
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				int index = token.indexOf(",");
				String flagFromToken = token.substring(0, index);
				if(flagFromToken.equals(flag))
					return token.substring(index + 1);
			}
		}
		return null;
	}
	
	private String getECCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "EC_CATEGORY");
		if(param != null)
			return param.getValue();
		return "53";
	}
	
	private String getECAdvRentalPack(String flag) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "EC_FLAG_SUB_CLASS_MAP");
		if(param != null) {
			StringTokenizer stk = new StringTokenizer(param.getValue(), ";");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				int index = token.indexOf(",");
				String thisFlag = token.substring(0, index);
				if(thisFlag.equals(flag))
					return token.substring(index + 1);
			}
		}
		return null;
	}
	
	private String getUSSDChargedCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "USSD_CHARGED_CATEGORY");
		if(param != null)
			return param.getValue();
		return "53";
	}
	
	private String getUSSDFreeCategory() {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "USSD_FREE_CATEGORY");
		if(param != null)
			return param.getValue();
		return "53";
	}
	
	private String processEnvioGift(Task task) {
		task.setObject(param_MSISDN, task.getString(param_envio_srcmsisdn));
		task.setObject(param_CALLER_ID, task.getString(param_envio_dstmsisdn));
		String response = processGift(task);
		if(response.equals(Resp_giftGifterNotActive)) {
			task.setObject(param_invalidParam, param_ussd_dstMsisdn);
			return Resp_invalidParam;
		}
		if(!response.equals(Resp_Success) && !response.equals(Resp_Failure) && !response.equals(Resp_Err))
			return Resp_Failure;
		return response;
	}
	
	private String processEnvioCopy(Task task) {
		String dstCallerID = task.getString(param_envio_dstmsisdn);
		String msisdn = task.getString(param_envio_srcmsisdn);
		if(dstCallerID == null || dstCallerID.length() == 0 || dstCallerID.equals(msisdn)) {
			task.setObject(param_invalidParam, param_envio_dstmsisdn);
			return Resp_invalidParam;
		}
		task.setObject(param_MSISDN, task.getString(param_envio_srcmsisdn));
		task.setObject(param_CALLER_ID, task.getString(param_envio_dstmsisdn));
		processCopyRequest(task);
		return task.getString(param_response);
	}
	
	private String processEnvioRequest(Task task) {
		String msisdn = task.getString(param_envio_srcmsisdn);
		if(msisdn == null || msisdn.equals("") || msisdn.equalsIgnoreCase("null") || msisdn.length() != 10)
			return Resp_InvalidPrefix;
		task.setObject(param_subscriberID, msisdn);
		Subscriber subscriber = getSubscriber(task);
		task.setObject(param_subscriber, subscriber);
		
		String subStatus = getSubscriberBlockedStatus(subscriber);
		if(subStatus != null)
			return subStatus;
		
		int flag = Utility.getIntFromStr(task.getString(param_envio_Flag));
		if(flag == -1) {
			task.setObject(param_invalidParam, param_envio_Flag);
			return Resp_invalidParam;
		}
		
		if(flag != 0 && flag != 4)
			task.setObject(param_envio_dstmsisdn, null);
		
		String indexStr = task.getString(param_envio_indx);
		int index = Utility.getIntFromStr(indexStr);
		if((flag == 0 || flag == 1 || flag == 3) && (index < 0 || index > 3)) {
			task.setObject(param_invalidParam, param_envio_indx);
			return Resp_invalidParam;
		}
		if(index == 0)
			task.setObject(param_envio_cbsmsisdn, null);
		
		String callerID = task.getString(param_envio_cbsmsisdn);
		if(callerID != null && (callerID.length() < 7 || callerID.length() > 15)) {
			task.setObject(param_invalidParam, param_envio_cbsmsisdn);
			return Resp_invalidParam;
		}
		String destCallerID = task.getString(param_envio_dstmsisdn);
		if(destCallerID != null && (destCallerID.length() < 7 || destCallerID.length() > 15)) {
			task.setObject(param_invalidParam, param_envio_dstmsisdn);
			return Resp_invalidParam;
		}
		
		String vCode = task.getString(param_envio_vcode);
		Clip clip = null;
		if(flag == 1 || flag == 4 || flag == 6 || flag==7) {
			if(vCode == null || vCode.length() != 15)
				return Resp_ClipNotAvailable;
			String wavFile = Utility.getWavFileFromVCode(vCode);
			clip = getClipByWavFile(wavFile);
			if(clip == null)
				return Resp_ClipNotAvailable;
			else if(clip.getClipEndTime().before(new Date()))
				return Resp_ClipExpired;
		}

		String activatedByStr = "ENVIO";
		String selectedByStr = activatedByStr;
		String subCharge = task.getString(param_envio_subsChg);
		if(subCharge != null && subCharge.equals("0"))
			activatedByStr = "ENVIO_FREE";
		String downCharge = task.getString(param_envio_downChg);
		if(downCharge != null && downCharge.equals("0"))
			selectedByStr = "ENVIO_FREE";
		String status = subscriber.getStatus();
		task.setObject(param_ACTIVATED_BY, activatedByStr);
		task.setObject(param_SELECTED_BY, selectedByStr);
		
		if(task.containsKey(param_envio_cLogs))
			task.setObject(param_CONSENT_LOG, task.getString(param_envio_cLogs));
		
		logger.info("RBT::starting procesing with flag-" + flag);
		switch(flag) {
			case 0: //copy request
				if(callerID != null)
					task.setObject(param_SPECIAL_CALLER_ID, callerID);
				return processEnvioCopy(task);
			case 1: //selection request
				task.setObject(param_ISACTIVATE, "true");
				task.setObject(param_SUBSCRIPTION_CLASS, getEnvioSubClass());
				task.setObject(param_CATEGORY_ID, getEnvioCategory());
				task.setObject(param_clip, clip);
				if(task.containsKey(param_envio_cbsmsisdn) && task.getString(param_envio_cbsmsisdn) != null)
					task.setObject(param_CALLER_ID, task.getString(param_envio_cbsmsisdn));
				processSelection(task);
				break;
			case 2: //profile query
				processEnvioProfileRequest(task);
				break;
			case 3: //deleting a tone
				if(!status.equals(WebServiceConstants.ACTIVE) && !status.equals(WebServiceConstants.ACT_PENDING))
					return Resp_Inactive;
				if(index <= 0)
					task.setObject(param_CALLER_ID, null);
				else
					task.setObject(param_CALLER_ID, task.getString(param_envio_cbsmsisdn));
				processDeleteSelection(task);
				break;
			case 4: //gift
				return processEnvioGift(task);
			case 5: //album selection
				if(vCode == null)
					return Resp_ClipNotAvailable;
				Category category = getCategoryByPromoId(vCode);
				if(category == null || category.getCategoryEndTime().before(new Date()))
					return Resp_ClipNotAvailable;
				if(status.equals(WebServiceConstants.ACTIVE) && isAdvanceRentalSubClass(subscriber.getSubscriptionClass())) {
					task.setObject(param_respMessage, "31");
					task.setObject(param_response, Resp_Failure);
				}
				task.setObject(param_ISACTIVATE, "true");
				task.setObject(param_SUBSCRIPTION_CLASS, getEnvioSubClass());
				task.setObject(param_category, category);
				if(task.containsKey(param_envio_cbsmsisdn) && task.getString(param_envio_cbsmsisdn) != null)
					task.setObject(param_CALLER_ID, task.getString(param_envio_cbsmsisdn));
				processSelection(task);
				break;
			case 6:
				task.setObject(param_specialResp, "ENVIO_10");
				if(status.equals(WebServiceConstants.ACTIVE))
					return Resp_AlreadyActive;
				
				task.setObject(param_ISACTIVATE, "true");
				task.setObject(param_CATEGORY_ID, getEnvioCategory());
				task.setObject(param_clip, clip);
				task.setObject(param_SUBSCRIPTION_CLASS, "ENVIO_10");
				if(task.containsKey(param_envio_cbsmsisdn) && task.getString(param_envio_cbsmsisdn) != null)
					task.setObject(param_CALLER_ID, task.getString(param_envio_cbsmsisdn));
				processSelection(task);
				break;
			case 7:
				task.setObject(param_specialResp, "ENVIO_20");
				if(status.equals(WebServiceConstants.ACTIVE))
					return Resp_AlreadyActive;

				task.setObject(param_ISACTIVATE, "true");
				task.setObject(param_CATEGORY_ID, getEnvioCategory());
				task.setObject(param_clip, clip);
				task.setObject(param_SUBSCRIPTION_CLASS, "ENVIO_20");
				if(task.containsKey(param_envio_cbsmsisdn) && task.getString(param_envio_cbsmsisdn) != null)
					task.setObject(param_CALLER_ID, task.getString(param_envio_cbsmsisdn));
				processSelection(task);
				break;
			default:
				task.setObject(param_invalidParam, param_envio_Flag);
				return Resp_invalidParam;
		}
		return task.getString(param_response);
	}
	
	/**
	 * Returns the subscriber profile in the format subType|ALL:VCODE|caller1:VCODE1|caller2:VCODE2|caller3:VCODE3
	 * 
	 * @author Sreekar
	 * @param msisdn mobile number of the user
	 */
	private String processEnvioProfileRequest(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if (canActivateUserWithStatus(subscriber.getStatus()))
			return Resp_Inactive;
		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if(pendingStatus != null)
			return pendingStatus;
		RbtDetailsRequest request = new RbtDetailsRequest(subscriber.getSubscriberID());
		Library library = rbtClient.getLibrary(request);
		task.setObject(param_library, library);
		return null;
	}
	
	private boolean isAdvanceRentalSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ADVANCE_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isAlbumRentalSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "ALBUM_RENTAL_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
}