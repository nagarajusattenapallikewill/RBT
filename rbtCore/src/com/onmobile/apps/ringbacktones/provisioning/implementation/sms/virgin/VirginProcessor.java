package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.virgin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Retailer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author ankur.gupta
 * Created on 1st Jun 2009
 */
public class VirginProcessor extends SmsProcessor implements Constants{
	
	public VirginProcessor() throws Exception {
		String subKeywords = getSMSParameter(ACTIVATION_KEYWORD);
		if (subKeywords == null)
			throw new RBTException("Activation Keywords are null");
		
		String unsubKeywords = getSMSParameter(DEACTIVATION_KEYWORD);
		if (unsubKeywords == null)
			throw new RBTException("Deactivation Keywords are null");

		String keyword = getSMSParameter(RBT_KEYWORDS);
		if (keyword == null)
			throw new RBTException("RBT SMS Keywords are null");
		
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#getTask(java.util.HashMap)
	 */
	public Task getTask(HashMap<String, String> requestParams)
	{
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		Task task = new Task(null, taskSession);
		reorderParameters(task);
		
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		String smsNo = param(iRBTConstant.SMS,SMS_NO,"123456");
		task.setObject(param_Sender, smsNo);
		
		if (task.containsKey("error")){
			if (task.getString("error").equalsIgnoreCase("true")){
				task.setObject(param_response, NO_SMS);
				return task;
			}
		}
		
		String smsText = task.getString(param_smsText);
		String[] values = rearrangeSmsText(task, smsText);
		
		if (values == null || values.length < 1){
			task.setObject(param_response, HELP);
			logger.info("RBT:: getTask: No tokens in smsText" );
			task.setObject(param_error, "true");
			return task;
		}
		
		task.setObject(param_sms, values);
		String smsTextNew = "";
		for (String str : values){
			smsTextNew += str+" ";
		}
		logger.info("SMS Text after re-ordering :"+smsTextNew);
		
		
		boolean isRetailerSearch = false;
		boolean isRetailerRequest = false;
		
		String access = task.getString(param_access);
		if ("RETAILER".equalsIgnoreCase(access)){
			if (checkRetailerAcceptRequest(values[0], task.getString(param_subscriberID))){
				task.setObject(param_isRetailerAccept, "true");
			}else{
				String retailerID = task.getString(param_subscriberID);
				task.setObject(param_RetailerMSISDN, retailerID);
				ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
				applicationDetailsRequest.setRetailerID(retailerID);
				Retailer retailer = rbtClient.getRetailer(applicationDetailsRequest);
				task.setObject(param_isRetailerRequest, "true");
				if (retailer == null){
					task.setObject(param_retailer_response, NON_RETAILER);
					task.setObject(param_send_sms_to_user, false);
					task.setObject(param_send_sms_to_retailer, true);
					logger.info("RBT:: getTask: non retailer "+ retailerID + " sent retailer request.");
					task.setObject(param_error, "true");
					return task;
				}
				if (isRetailerRequest(values)){
					task.setObject(param_isRetailerSearch, "false");
					String subID = values[values.length-1];
					task.setObject(param_subscriberID, subID);	
					isRetailerRequest = true;
				}else{
					task.setObject(param_isRetailerSearch, "true");
					isRetailerSearch = true;
				}
			}
		}else{
			task.setObject(param_isRetailerRequest, "false");
			task.setObject(param_RetailerMSISDN, "");
		}
		
		String subID = task.getString(param_subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subID); 
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		task.setObject(param_subscriber, subscriber);
		
		if (isRetailerRequest){
			task.setObject(param_send_sms_to_user, false);
			task.setObject(param_send_sms_to_retailer, true);
		}else{
			task.setObject(param_send_sms_to_user, true);
			task.setObject(param_send_sms_to_retailer, false);
		}
		
		if (!isRetailerSearch && (!subscriber.isCanAllow() || !subscriber.isValidPrefix())){
			if (!subscriber.isCanAllow())
				if (isRetailerRequest){
					task.setObject(param_retailer_response, ACCESS_FAILURE);
				}else	
					task.setObject(param_response, ACCESS_FAILURE);
			else
				if (isRetailerRequest)
					task.setObject(param_retailer_response, INVALID);
				else	
					task.setObject(param_response, INVALID);
			task.setObject(param_error, "true");
			return task;
		}
		
		if (!isRetailerSearch){
			String status = subscriber.getStatus();
			if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)){
				if (isRetailerRequest)
					task.setObject(param_retailer_response, ACTIVATION_PENDING);
				else
					task.setObject(param_response, ACTIVATION_PENDING);
				task.setObject(param_error, "true");
				logger.info("RBT::getTask:User activation pending");
				return task;
			}else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)){
				if (isRetailerRequest)
					task.setObject(param_retailer_response, DEACTIVATION_PENDING);
				else
					task.setObject(param_response, DEACTIVATION_PENDING);
				task.setObject(param_error, "true");
				logger.info("RBT::getTask:User deactivation pending");
				return task;
			}else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)){
				if (isRetailerRequest)
					task.setObject(param_retailer_response, BLACK_LISTED);
				else
					task.setObject(param_response, BLACK_LISTED);
				task.setObject(param_error, "true");
				logger.info("RBT::getTask:User account blacklisted");
				return task;
			}else if (status.equalsIgnoreCase(WebServiceConstants.ERROR)){
				if (isRetailerRequest)
					task.setObject(param_retailer_response, TECHNICAL_ERROR);
				else{
					task.setObject(param_response, TECHNICAL_ERROR);
				}
				task.setObject(param_error, "true");
				logger.info("RBT::getTask:User account blacklisted");
				return task;
			}
		}
		
		String taskAction = getTaskAction(values, task); // identifies the task action to be performed
		task.setTaskAction(taskAction);

		logger.info("RBT:: task: " + task.toString());
		return task;
	}
	
	private String getTaskAction(String[] values, Task task) {
		if (task.containsKey(param_isRetailerAccept) && 
				task.getString(param_isRetailerAccept).equalsIgnoreCase("true")){
			logger.info("Setting task action as :"+action_retailer_accept);
			return action_retailer_accept;
		}else if (task.containsKey(param_isRetailerRequest) && 
				task.getString(param_isRetailerRequest).equalsIgnoreCase("true")){
			if (task.containsKey(param_isRetailerSearch) && 
					task.getString(param_isRetailerSearch).equalsIgnoreCase("true")){
				logger.info("Setting task action as :"+action_retailer_search);
				return action_retailer_search;
			}else{
				logger.info("Setting task action as :"+action_retailer_request);
				return action_retailer_request;
			}
		}else if (checkAct(values[0])){
			logger.info("Setting task action as :"+action_activate);
			return action_activate;
		}else if (checkDeAct(values[0])){
			logger.info("Setting task action as :"+action_deactivate);
			return action_deactivate;
		}else if (checkOptInCopyCancel(values[0])){
			logger.info("Setting task action as :"+action_optin_copy_cancel);
			return action_optin_copy_cancel;
		}else if (checkCopyConfirm(values[0])){
			logger.info("Setting task action as :"+action_copy_confirm);
			return action_copy_confirm;
		}else if (checkViral(values[0])){
			logger.info("Setting task action as :"+action_viral);
			return action_viral;
		}else if (checkHelpRequest(values[0]) || checkHelpRequest((String)task.getObject(param_smsText))){
			logger.info("Setting task action as :"+action_help);
			return action_help;
		}else if (checkTempCan(values[0])){
			logger.info("Setting task action as :"+action_remove_profile);
			return action_remove_profile;
		}else if (checkList(values[0])){
			logger.info("Setting task action as :"+action_list_profiles);
			return action_list_profiles;
		}else if (checkListNext(values[0])){
			logger.info("Setting task action as :"+action_list_next_profiles);
			return action_list_next_profiles;
		}else if (checkFeed(values[0])){
			logger.info("Setting task action as :"+action_feed);
			return action_feed;
		}else if (checkSearchResponse(values[0])){
			logger.info("Setting task action as :"+action_default_search);
			return action_default_search;
		}else if (checkClipPromo(task)){
			logger.info("Setting task action as :"+action_clip_promo);
			return action_clip_promo;
		}else if (checkCategoryPromo(task)){
			logger.info("Setting task action as :"+action_category_promo);
			return action_category_promo;
		}else if (checkProfile(task)){
			logger.info("Setting task action as :"+action_profile);
			return action_profile;
		}else if (checkCategoryAlias(task)){
			logger.info("Setting task action as :"+action_category_alias);
			return action_category_alias;
		}else if (checkClipAlias(task)){
			logger.info("Setting task action as :"+action_clip_alias);
			return action_clip_alias;
		}else if (isTNBRequest(values[0])){
			logger.info("Setting task action as :"+TNB_KEYWORDS);
			return TNB_KEYWORDS;
		}
		else{
			logger.info("Setting task action as :"+action_default_search);
			return action_default_search;
		}
		/*
		 * 0. Check for song/artist/movie search
		 * 		>ProcessSearch
		 * 1. first check if songNo > maxResultsToSend
		 * 2. check if clip exists for promoID
		 * 		>  processClipPromo
		 * 3. check if category exists for promoID
		 * 		> processCategoryPromo
		 * 4. check profile
		 * 		>processProfile
		 * 5. process default search
		 */
	}

	protected void reorderParameters(Task task)
	{
		String smsMsg = task.getString(param_smsText);
		String subID = task.getString(param_subID);
		String ipAddress = task.getString(param_ipAddress);
		String smsParam = task.getString(param_smsParam);
		String access = task.getString(param_access);
//		String shortCode = task.getString(param_shortCode);
		String actInfo = ipAddress + ":SMS";
		
		logger.info("RBT: SMS Text = " + smsMsg + ", SUB_ID = " + subID + " from " + ipAddress);
		
		if (ipAddress == null || (!isValidIP(ipAddress))){
			task.setObject(param_response, INVALID_IP_ADDRESS);
			logger.info("RBT::reorderParameters : invalid IP Adresss");
			task.setObject(param_error, "true");
			return;
		}
		
		if (subID != null)
			subID = subID.trim();
		if (subID != null && subID.equals(""))
				subID = null;
		
		if (access != null) access = access.toLowerCase();
		
		if(smsMsg != null)
		{
			StringTokenizer message = new StringTokenizer(smsMsg, " ");
			smsMsg = " ";
			while(message.hasMoreTokens())
			{
				String token = message.nextToken();
				String smsActPromoPrefix = getSMSParameter(SMS_ACT_PROMO_PREFIX);
				if(smsActPromoPrefix != null && token.toLowerCase().startsWith(smsActPromoPrefix))
					actInfo =  ipAddress + ":" + token;
				else
					smsMsg = smsMsg.trim() + " " + token;
			}
			smsMsg = smsMsg.trim();
			if (smsParam != null)
				smsMsg = smsMsg + " " + smsParam.trim();
			
		}
		
		if (smsMsg == null || subID == null){
			task.setObject(param_response, INSUFFICIENT_PARAMETERS);
			logger.info("RBT::reorderParameters : insufficient Parameters, sub id or smsMsg is null");
			task.setObject(param_error, "true");
			return;
		}

		task.setObject(param_isPrepaid, ""+isPrepaid);
		task.setObject(param_actInfo, actInfo);
		task.setObject(param_smsText, smsMsg);
		task.setObject(param_subscriberID, subID);
		task.setObject(param_access, access);
		
		task.setObject(param_language, defaultLang);
		
		if (smsMsg.equals("")){
			task.setObject(param_response, HELP);
			logger.info("RBT::reorderParameters :smsMsg can not be empty string");
			task.setObject(param_error, "true");
			return;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processActivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public Subscriber processActivation(Task task)
	{
		logger.info("Processing Activation for subID :");
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		logger.info("Processing Activation for subID:"+subscriber.getSubscriberID()+ " ,isPrepaid:"+ task.getString(param_isPrepaid));
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)){
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
			subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
			subscriptionRequest.setMode("SMS");
			subscriptionRequest.setInfo(task.getString(param_actInfo));
			subscriptionRequest.setCircleID(subscriber.getCircleID());
			subscriber = rbtClient.activateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
				task.setObject(param_subscriber, subscriber);
				response = SUCCESS;
			}else{
				response = TECHNICAL_FAILURE;
			}
		}else if(status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
			response = ALREADY_ACTIVE;
		}else if(status.equalsIgnoreCase(WebServiceConstants.SUSPENDED)){
			response = SUSPENDED;
		}else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING) || status.equalsIgnoreCase(WebServiceConstants.GRACE)){
			response = ACTIVATION_PENDING;	
		}else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)){
			response = DEACTIVATION_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING)){
			response = GIFTING_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)){
			response = RENEWAL_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)){
			response = BLACK_LISTED;
		}else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX)){
			response = INVALID;
		}else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING)){
			response = EXPRESS_COPY_PENDING;
		}else{
			response = HELP;
		}
		
		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		return subscriber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDeactivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String processDeactivation(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		logger.info("Processing Deactivation for subID:"+subscriber.getSubscriberID());
		if(status.equalsIgnoreCase(WebServiceConstants.NEW_USER)||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)){
			response = NOT_ACTIVE;
		}else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)){
			response = ACTIVATION_PENDING;	
		}else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING)){
			response = DEACTIVATION_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING)){
			response = GIFTING_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)){
			response = RENEWAL_PENDING;
		}else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED)){
			response = BLACK_LISTED;
		}else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX)){
			response = INVALID;
		}else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING)){
			response = EXPRESS_COPY_PENDING;
		}else{//active, suspended, grace
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
			subscriptionRequest.setMode("SMS");
			subscriptionRequest.setInfo(task.getString(param_actInfo));
			subscriber = rbtClient.deactivateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
				task.setObject(param_subscriber, subscriber);
				response = SUCCESS;
			}else if (response.equalsIgnoreCase(WebServiceConstants.DCT_NOT_ALLOWED)){
				response = TECHNICAL_FAILURE;
			}else{
				response = TECHNICAL_FAILURE;
			}
		}
		
		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		return null;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processSelection(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processSelection(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String clipID = task.getString(param_clipid);
		logger.info("Processing Selection for subID:"+subscriber.getSubscriberID()+ " ,isPrepaid:"+ subscriber.isPrepaid()+" ,clipID:"+clipID);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID(),subscriber.isPrepaid(),""+smsCategoryID,clipID,null,null);
		String callerID = null; 
		if (task.containsKey(param_callerid)){
			callerID = task.getString(param_callerid);
			selectionRequest.setCallerID(callerID);
		}
		if (task.getString(param_mod_channel) != null)
			selectionRequest.setMode(task.getString(param_mod_channel));
		else
			selectionRequest.setMode("SMS");
		selectionRequest.setInfo(task.getString(param_actInfo));
		selectionRequest.setCircleID(subscriber.getCircleID());
		String ph = task.getString(param_profile_hours);
		if (ph != null){
			selectionRequest.setProfileHours(ph);
		}
		Object statusObj = task.getObject(param_status);
		if (statusObj != null){
			Integer status = (Integer)statusObj;
			selectionRequest.setStatus(status);
		}
		if (task.getString(param_catid) != null)
			selectionRequest.setCategoryID(task.getString(param_catid));
		
		if (task.getString(param_cricket_pack) != null)
			selectionRequest.setCricketPack(task.getString(param_cricket_pack));
		
//		selectionRequest.setInLoop(true);
		rbtClient.addSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
			response = SUCCESS;
		}else if (response.equalsIgnoreCase(WebServiceConstants.SUSPENDED) || 
				response.equalsIgnoreCase(WebServiceConstants.SELECTION_SUSPENDED)){
			response = SUSPENDED;
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_NOT_EXISTS)){
			response = NOT_AVAILABLE;
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED)){
			response = EXPIRED;
		}else if (response.equalsIgnoreCase(WebServiceConstants.FAILED)){
			response = TECHNICAL_FAILURE;
		}else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_EXISTS)){
			response = ALREADY_EXISTS;
		}else if (response.equalsIgnoreCase(WebServiceConstants.TECHNICAL_DIFFICULTIES)){
			response = TECHNICAL_FAILURE;
		}else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_MEMBER_OF_GROUP)){
			response = TECHNICAL_FAILURE;
		}else if (response.equalsIgnoreCase(WebServiceConstants.NOT_ALLOWED)){
			response = TECHNICAL_FAILURE;
		}else{
			response = HELP;
		}
		
		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processFeed(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processFeed(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		String subStatus = subscriber.getStatus();
		String status = null, pass = null;
		String[] values = (String[])task.getObject(param_sms);
		FeedStatus feed = null;
		task.setObject(param_status, 90);
		boolean activated = false;
		if (subStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			activated = true;
		if (values.length > 1) {
			if (!values[1].equalsIgnoreCase("ON")
					&& !values[1].equalsIgnoreCase("OFF"))
				pass = values[1].toUpperCase();
			else
				status = values[1];
		}

		if (pass == null && values.length > 2)
			pass = values[2].toUpperCase();

		if (!activated){
			processActivation(task);
			subscriber = (Subscriber)task.getObject(param_subscriber);
			subStatus = subscriber.getStatus();
			if (subStatus.equals(WebServiceConstants.ACTIVE)||subStatus.equals(WebServiceConstants.ACT_PENDING))
				activated = true;
		}
		if (activated) {
			String feed1 = null;
			if (feedStatus != null)
				feed1 = feedStatus.getSmsKeywords();

			if (feed1 != null) {
				feed1 = feed1.toUpperCase();
				String smsKey = feed1.substring(0, feed1.indexOf(","));
				ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
				applicationDetailsRequest.setType(smsKey);
				feed = rbtClient.getFeedStatus(applicationDetailsRequest);
			}
			
			String feedFile = null;
			//cricket pass = true for virgin
			if (status == null || status.equalsIgnoreCase("ON")) {
				if (pass == null || !cricketSubKey.contains(pass)) {
					pass = "SP";
				}
				if (feed != null) {
					feedFile = feed.getFeedFile();
					if (feedFile != null && feedFile.indexOf(",") != -1) {
						feedFile = feedFile.substring(feedFile
								.lastIndexOf(",") + 1);
					}
					if (feed.getStatus().equalsIgnoreCase("OFF"))
						feedFile = null;
				}
				Feed schedule = getCricketClass(pass);
				if (schedule == null) {
					String sms = feedStatus.getFeedFailureSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_FAILURE);
				}else {
					Setting[] settings = getActiveSubSettings(subID,90);
					Setting cricSel = null;
					for (int i=0; settings != null && settings.length > 0 &&  i<settings.length ; i++){
						Setting setting = settings[i];
						if (setting.getCallerID().equalsIgnoreCase(WebServiceConstants.ALL)){
							cricSel = setting;
							break;
						}
					}
					logger.info("cricSel="+cricSel);
					if (cricSel != null	&& (cricSel.getEndTime().after(schedule.getEndDate()) || 
								cricSel.getEndTime().equals(schedule.getEndDate()))) {
						String sms = feedStatus.getFeedFailureSms();
						task.setObject(param_sms_for_user, sms);
						task.setObject(param_response, FEED_FAILURE);
						return;
					}
					task.setObject(param_catid, "10");
					task.setObject(param_cricket_pack, pass);
					
					processSelection(task);
					
					String sms = feedStatus.getFeedOnSuccessSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_SUCCESS);
				}
			}else if (status != null && status.equalsIgnoreCase("OFF")) {
				//				SubscriberStatus[] subscriberStatus = getSubscriberRecords(strSubID);
				boolean cricket = false;
				//checking is subscriber already active on status 90
				Setting[] settings = getActiveSubSettings(subID,90);
				Setting cricSel = null;
				for (int i=0; i<settings.length ; i++){
					Setting setting = settings[i];
					if (setting.getCallerID().equalsIgnoreCase(WebServiceConstants.ALL)){
						cricSel = setting;
						break;
					}
				}
				if (cricSel != null){
					processDeactivateSubscriberRecords(task);
					String sms = feedStatus.getFeedOffSuccessSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_SUCCESS);
				}else{
					String sms = feedStatus.getFeedOffFailureSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_FAILURE);
				}
				
			}
		} else {
			task.setObject(param_response, DEACTIVATION_FAILURE);
		}
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDefaultSearch(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processDefaultSearch(Task task) {
		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null){
			moreRBTKeywords = getTokenizedList(moreKeywords, ",", true);
		}
		String[] values = (String[])task.getObject(param_sms);
		int songNo = -1;
		try {
			songNo = Integer.parseInt(values[0]);
		} catch (Exception e) {
			songNo = -1;
		}
		int maxSearchResultsToSend = 4;
		String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
		if (maxResults != null){
			try {
				maxSearchResultsToSend = Integer.parseInt(maxResults);
			} catch (Exception e) {
				maxSearchResultsToSend = 4;
			}
		}
		if (songNo > 0 && songNo <= maxSearchResultsToSend){
			setRequest(task, songNo);//check activation info
		}else if (songNo > maxSearchResultsToSend){
			// already covered > should not come in this if condition
		}else if (moreRBTKeywords.contains(values[0].toLowerCase())){
			moreRequest(task);
		}else{
			String searchString = " ";
			String searchOn = "song";
			HashMap<String, String> searchOnMap = getSearchOnMap();
			if (values.length > 1 && searchOnMap.containsKey(values[0])) {
				searchOn = searchOnMap.get(values[0]);
				for (int k = 1; k < values.length; k++) {
					searchString = searchString.trim() + " "
							+ values[k];
				}
			}
			else {
				for (int k = 0; k < values.length; k++) {
					searchString = searchString.trim() + " " + values[k];
				}
			}

			searchRequest(task, searchString.trim().toLowerCase(), searchOn);
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}	
	
	private void setRequest(Task task, int songNo) {
		Subscriber subscriber = (Subscriber)(task.getObject(param_subscriber));
		String status = subscriber.getStatus();
		boolean invalid = false;
		logger.info("Setting request for selection: after getting response to Default search");
		
		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null && activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE) || activateAfterSearch){
			ViralData[] viralDataArr = getViraldata(subscriber.getSubscriberID(),null, "SEARCH");
			if (viralDataArr == null || viralDataArr.length == 0) {
				task.setObject(param_response, TEMPORARY_OVERRIDE_FAILURE);
			} else {
				if (!status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
					//Can use processActivation here
					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
					subscriptionRequest.setMode("SMS");
					subscriptionRequest.setCircleID(subscriber.getCircleID());
					subscriber = rbtClient.activateSubscriber(subscriptionRequest);
				}
				ViralData data = viralDataArr[0];
				String clipIDs = data.getClipID();
				StringTokenizer stk = new StringTokenizer(clipIDs, ",");
				for (int i = 1; i < songNo; i++) {
					if (stk.hasMoreTokens())
						stk.nextToken();
					else {
						invalid = true;
						break;
					}
				}
				if (!invalid && stk.hasMoreTokens()) {
					try {
						int clipId = Integer.parseInt(stk.nextToken());
						Clip reqClip = getClipById(String.valueOf(clipId),subscriber.getLanguage());
						if (reqClip != null) {
							String callerId = null;
							if (!isExistingSelection(subscriber.getSubscriberID(),reqClip.getClipId())) {
								//Can use processSelection here
								SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID(),subscriber.isPrepaid(),""+smsCategoryID,reqClip.getClipId()+"",null,null);
								selectionRequest.setMode("SMS");
								selectionRequest.setCircleID(subscriber.getCircleID());
//								selectionRequest.setInLoop(true);
								rbtClient.addSubscriberSelection(selectionRequest);
								String response = selectionRequest.getResponse();
								if (response.equals(WebServiceConstants.CLIP_EXPIRED)){
									task.setObject(param_response,CLIP_EXPIRED);
									return;
								}
								task.setObject(param_response, REQUEST_RBT_SMS2_SUCCESS);
							} else {
								task.setObject(param_clipName, reqClip.getClipName());
								task.setObject(param_callerid, callerId );//check whether its null or not in response encoder
								task.setObject(param_response, SETTING_EXISTS);
							}		
							removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");
						} else {
							task.setObject(param_response, TECHNICAL_FAILURE);
						}
					} catch (Exception e) {
						task.setObject(param_response, TECHNICAL_FAILURE);
						logger.error(e);
					}
				}
			}
		} else {
			task.setObject(param_response, TEMPORARY_OVERRIDE_FAILURE);
		}
			
	}
	
	private void moreRequest(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		logger.info("more request : after getting response to Default search");
		boolean isSubscribed = false;
		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			isSubscribed = true;
		
		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null && activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (isSubscribed || activateAfterSearch) {
			ViralData[] viralDataArr = getViraldata(subID, null, "SEARCH");
			if (viralDataArr == null || viralDataArr.length == 0) {
				task.setObject(param_response, MORE_RBT_SMS1_FAILURE);
			} else {
				ViralData data = viralDataArr[0];
				String clipIDs = data.getClipID();
				StringTokenizer stk = new StringTokenizer(clipIDs, ",");
				int maxSearchResultsToSend = 4;
				String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
				if (maxResults != null){
					try {
						maxSearchResultsToSend = Integer.parseInt(maxResults);
					} catch (Exception e) {
						maxSearchResultsToSend = 4;
					}
				}
				if (stk.countTokens() <= maxSearchResultsToSend) {
					task.setObject(param_response, REQUEST_RBT_SMS2_FAILURE);
				} else {
					String clipId = null;
					for (int clipCounter = 0; clipCounter < maxSearchResultsToSend; clipCounter++) {
						clipId = stk.nextToken();
					}
					String finalClipIds = clipIDs.substring(clipIDs
							.indexOf(clipId)
							+ clipId.length() + 1, clipIDs.length());

					StringTokenizer newStk = new StringTokenizer(finalClipIds,",");
					int finalCounter = 1;
					String finalMatches = "";
					boolean moreFlag = false;
					while (newStk.hasMoreTokens() && finalCounter <= maxSearchResultsToSend) {
						String token = newStk.nextToken();
						Clip clip = getClipById(token.trim(),subscriber.getLanguage());
						String clipName = clip.getClipName();
						finalMatches += "" + finalCounter + ". " + clipName;
						boolean addMovieName = false;
						String addMovieNameInSms = getSMSParameter(ADD_MOVIE_NAME_IN_SMS);
						if (addMovieNameInSms != null && addMovieNameInSms.equalsIgnoreCase("true"))
							addMovieName = true;
						if (addMovieName) {
							String movieName = clip.getAlbum();
							if (movieName != null && !movieName.trim().equalsIgnoreCase(""))
								finalMatches += "/" + movieName.trim();
						}
						boolean addPriceAndValidity = false;
						String addPriceAndValidityStr = getSMSParameter(ADD_PRICE_AND_VALIDITY);
						if (addPriceAndValidityStr != null && addPriceAndValidityStr.equalsIgnoreCase("true"))
							addPriceAndValidity = true;
						if (addPriceAndValidity) {
							String[] priceNvalidity = getPriceAndValidity(clip.getClassType());
							if (priceNvalidity != null && priceNvalidity[0] != null && priceNvalidity[1] != null)
								finalMatches += "(" + priceNvalidity[0] + "/"+ priceNvalidity[1] + ")";
						}
						if (newStk.hasMoreTokens())
							finalMatches += " ";
						finalCounter++;
					}
					if (newStk.hasMoreTokens()) {
						moreFlag = true;
					}
					removeViraldata(subID, null, "SEARCH");
					addViraldata(subID, null, "SEARCH", finalClipIds, "SMS", 0, null);

					task.setObject(param_search_results, finalMatches);
					
					if (moreFlag)
						task.setObject(param_response, REQUEST_RBT_SMS3_SUCCESS);
					else
						task.setObject(param_response, REQUEST_RBT_SMS1_SUCCESS);
				}
			}
		} else {
			task.setObject(param_response, HELP);
			if (!isSubscribed && activateAfterSearch)
				task.setObject(param_response, ACTIVATION_FAILURE);
		}
	}
	
	private void searchRequest(Task task, String searchString, String searchOn) {
		searchString = replaceSpecialChars(searchString);

		logger.info("RBT:: search string = " + searchString + " and search on = "+searchOn);
		boolean performSearch = true;

		if (searchString == null || searchString.equals(""))
			performSearch = false;

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		boolean activated = false;
		boolean isSubscribed = status.equalsIgnoreCase(WebServiceConstants.ACTIVE);

		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null && activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (!isSubscribed && !activateAfterSearch) {
			processActivation(task);
			subscriber = (Subscriber)task.getObject(param_subscriber);
			status = subscriber.getStatus();
			if (!(status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)||status.equalsIgnoreCase(WebServiceConstants.NEW_USER)))
				activated = true;
		}
		String[] results = null;
		if (performSearch){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(searchOn,searchString);
			map.put("SUBSCRIBER_ID", subscriber.getSubscriberID());
			ArrayList<LuceneClip> list = (ArrayList<LuceneClip>)luceneIndexer.searchQuery(map, 0, maxSearchResultsToFetch);
			if (list == null || list.size() == 0){
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);
				return;
			}
			ArrayList al = new ArrayList();
			for (int i = 0; list != null && i < list.size(); i++) {
				LuceneClip clip = list.get(i);
				if (clip.getClipEndTime().after(new Date()))
					al.add(String.valueOf(list.get(i).getClipId()));
			}
			if (al.size() > 0)
				results = (String[]) al.toArray(new String[0]);
		}
		processSearchClips(task, results, activated, null);
	}
	
	private void processSearchClips(Task task, String[] results, boolean activated, String categoryAlias) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		
		boolean moreFlag = false;

		if (results == null || results.length == 0) {
			if (categoryAlias != null) {
				task.setObject(param_response, SMS_ALIAS_CATEGORY_NO_CLIP);
				task.setObject(param_categoryAlias, categoryAlias);// replace %S
			} else {
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);
			}

			removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");
		} else {
			ArrayList list = new ArrayList();
			for (int c = 0; c < results.length; c++) {
				if (list != null && !list.contains(results[c])) {
					list.add(results[c]);
				}
			}
			results = (String[]) list.toArray(new String[0]);
			String match = "";
			String clipIDs = "";
			for (int hit = 0; results != null && hit < results.length; hit++) {
				try {
					String id = results[hit].trim();
					Clip clip = getClipById(id.trim(),subscriber.getLanguage());
					if (clipIDs.equalsIgnoreCase(""))
						clipIDs = "" + id.trim();
					else
						clipIDs = clipIDs + "," + id.trim();
					int maxSearchResultsToSend = 4;
					String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
					if (maxResults != null){
						try {
							maxSearchResultsToSend = Integer.parseInt(maxResults);
						} catch (Exception e) {
							maxSearchResultsToSend = 4;
						}
					}
					if (hit + 1 <=  maxSearchResultsToSend) {
						match = match + (hit + 1) + "." + clip.getClipName();
						boolean addMovieName = false;
						String addMovieNameInSms = getSMSParameter(ADD_MOVIE_NAME_IN_SMS);
						if (addMovieNameInSms != null && addMovieNameInSms.equalsIgnoreCase("true"))
							addMovieName = true;
						if (addMovieName) {
							String movieName = clip.getAlbum();
							if (movieName != null
									&& !movieName.trim().equalsIgnoreCase(""))
								match += "/" + movieName.trim();
						}
						boolean addPriceAndValidity = false;
						String addPriceAndValidityStr = getSMSParameter(ADD_PRICE_AND_VALIDITY);
						if (addPriceAndValidityStr != null && addPriceAndValidityStr.equalsIgnoreCase("true"))
							addPriceAndValidity = true;
						if (addPriceAndValidity) {
							String[] priceNvalidity = getPriceAndValidity(clip.getClassType());
							if (priceNvalidity != null
									&& priceNvalidity[0] != null
									&& priceNvalidity[1] != null)
								match += "(" + priceNvalidity[0] + "/"
										+ priceNvalidity[1] + ")";
						}
						match += " ";
					} else {
						moreFlag = true;
					}
				} catch (Exception e) {
					logger.info("RBT::ERROR in "+ results[hit] + " and exception is " + e);
				}
			}
			// checking if we got any clips with charge classes
			if (clipIDs.equalsIgnoreCase("")) {
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);
				
				removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");
			} else {
				removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");//check this
				addViraldata(subscriber.getSubscriberID(), null, "SEARCH", clipIDs, "SMS", 0, null);
				if (moreFlag)
					task.setObject(param_response, REQUEST_RBT_SMS3_SUCCESS);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_SUCCESS);

				task.setObject(param_search_results, match.trim());
			}
		}
	}

	@Override
	public void processCategoryByPromoID(Task task) {
		Clip clip = (Clip)task.getObject(param_clip);
		task.setObject(param_clipName, clip.getClipName());
		task.setObject(param_clipid, String.valueOf(clip.getClipId()));
		String[] values = (String[])task.getObject(param_sms);
		String callerID = null;
		if (values.length > 1) {
			if (checkAct(values[1])) {
				if (values.length > 2)
					callerID = values[2];
				task.setObject(param_callerid, callerID);
			}else {
				callerID = values[1];
				task.setObject(param_callerid, callerID);
				processActivation(task);
			}
		}
		processSelection(task);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	@Override
	public void processClipByAlias(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String[] values = (String[])task.getObject(param_sms);
		Clip clip = rbtCacheManager.getClipBySMSAlias(values[0]);
		if (clip == null || clip.getSmsStartTime() == null || clip.getClipEndTime() == null
				|| (clip.getSmsStartTime() != null && clip.getSmsStartTime().after(new Date()))) {
			task.setObject(param_response, SMS_ALIAS_CLIP_INVALID);
			task.setObject(param_clipAlias, values[0]);//replace %S with clipAlias
		} else if (clip.getClipEndTime().before(new Date())){
			task.setObject(param_clipAlias, values[0]);//replace %S with clipAlias
			task.setObject(param_response, SMS_ALIAS_CLIP_EXPIRED);
		}
		else { // clip is valid
			String callerID = null;
			if (values.length > 1) {
				try {
					Long.parseLong(values[values.length-1]);
					String temp = values[1];
					if (temp.length() >= 7 && temp.length() <= 15)
						callerID = temp;
				} catch (Exception e) {

				}
			}

			boolean activated = false;
			boolean isSubscribed = status.equalsIgnoreCase(WebServiceConstants.ACTIVE);
			if (!isSubscribed) {
				processActivation(task);
				subscriber = (Subscriber)task.getObject(param_subscriber);
			}
			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			if (callerID != null)
				task.setObject(param_callerid, callerID);//%C
			processSelection(task);
			
			if (isSubscribed)
				task.setObject(param_response, SMS_ALIAS_ONLY_CLIP_SUCCESS);
			else
				task.setObject(param_response, SMS_ALIAS_CLIP_SUCCESS);

			task.setObject(param_clipName, clip.getClipName());//%S
		}
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	@Override
	public void processClipByPromoID(Task task) {
		Clip clip = (Clip)task.getObject(param_clip);
		task.setObject(param_clipName, clip.getClipName());
		task.setObject(param_clipid, String.valueOf(clip.getClipId()));
		String[] values = (String[])task.getObject(param_sms);
		String callerID = null;
		if (values.length > 1) {
			if (checkAct(values[1])) {
				if (values.length > 2)
					callerID = values[2];
				task.setObject(param_callerid, callerID);
			}else {
				callerID = values[1];
				task.setObject(param_callerid, callerID);
			}
		}
		processSelection(task);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	@Override
	public void processCategoryByAlias(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String[] values = (String[])task.getObject(param_sms);
		Category category = (Category)task.getObject(param_category);
		boolean activated = false;
		boolean isSubscribed = status.equalsIgnoreCase(WebServiceConstants.ACTIVE);
		
		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null && activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (!isSubscribed && !activateAfterSearch) {
			processActivation(task);
			subscriber = (Subscriber)task.getObject(param_subscriber);
			status = subscriber.getStatus();
			activated = status.equalsIgnoreCase(WebServiceConstants.ACTIVE) || status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING);
		}
		
		Clip[] clips = getClipsByCatId(category.getCategoryId(),subscriber.getLanguage());
							
		ArrayList al = new ArrayList();
		for (int i = 0; clips != null && i < clips.length; i++) {
			al.add(String.valueOf(clips[i].getClipId()));
		}
		String[] results = null;
		if (al.size() > 0)
			results = (String[]) al.toArray(new String[0]);

		processSearchClips(task, results, activated, values[0]);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	@Override
	public void processProfile(Task task) {
		Clip clip = (Clip)task.getObject(param_clip);
		String[] values = (String[])task.getObject(param_sms);
		String subID = task.getString(param_subscriberID);
		String value = null;
		int duration = 1;
		if (values.length > 1) {
			duration = checkDuration(values[1].toLowerCase());
		}
		if (clip != null) {
			value = clip.getClipRbtWavFile();
			SelectionRequest selectionRequest = new SelectionRequest(subID);
			selectionRequest.setStatus(99);
			rbtClient.deleteSubscriberSelection(selectionRequest);
			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			task.setObject(param_profile_hours, String.valueOf(duration));
			task.setObject(param_status, 99);
			task.setObject(param_catid, "99");
			processSelection(task);
			//assuming success
			task.setObject(param_response, SUCCESS);
		}else{
			task.setObject(param_response, FAILURE);
		}
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	@Override
	public void processListProfiles(Task task) {

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		Clip[] profileClips = rbtCacheManager.getClipsInCategory(profileCategoryID); 
		if (profileClips == null || profileClips.length < 1)
		{
			task.setObject(param_response, FAILURE);
			return;
		}
		ArrayList<Clip> engClips = new ArrayList<Clip>();
		for (Clip profileClip : profileClips)
		{
			if (profileClip.getClipRbtWavFile() != null && profileClip.getClipRbtWavFile().indexOf("_eng_") != -1)
			{
				if(profileClip.getClipSmsAlias() != null )
					engClips.add(profileClip);
			}	
		}
			
			
		if (engClips.size() < 1)
		{
			task.setObject(param_response, FAILURE);
			return;
		}
			
		String sms = "";
		int profileMaxLimit =  param(SMS,PROFILE_LIST_COUNT, 1000);
		for (int profileCount = 0; profileCount < engClips.size() && profileCount < profileMaxLimit; profileCount++)
		{
				Clip profileClip = engClips.get(profileCount);
				StringTokenizer stk = new StringTokenizer(profileClip.getClipSmsAlias(),",");
				if(profileMaxLimit == 1000)
					sms = sms + ", " + stk.nextToken();
				else
					sms = sms + ((profileCount+1) +")" + profileClip.getClipName() + "-" + stk.nextToken() + " ");
		}
		if(profileMaxLimit != 1000 )
		{
			task.setObject(param_SMSTYPE, "PROFILE");
			removeViraldata(task);
			addViraldata(subscriberID,null,"PROFILE",null,"SMS",1, null);
		}
		if(sms.startsWith(", "))
			sms = sms.substring(2);
		task.setObject(param_sms_for_user, sms);
		task.setObject(param_response, SUCCESS);
	}
	
	public void getNextProfile(Task task) {
		logger.info("inside getNextProfile");
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		task.setObject(param_SMSTYPE, "PROFILE");
		logger.info("inside getNextProfile. task is "+task);
		ViralData context[] = getViraldata(task);
		logger.info("Profile exhausted is"+getSMSTextForID(task,PROFILE_NEXT_FAILURE,null,subscriber.getLanguage()));
		if(context == null || context.length <= 0)
		{
			logger.info("getNextProfile context is "+context == null ? "zero null":context.length);
			task.setObject(param_response, getSMSTextForID(task,PROFILE_NEXT_FAILURE,null,subscriber.getLanguage()));
			return;
		}
		int profileMaxLimit =  param(SMS,PROFILE_LIST_COUNT, 1000);
		int nextCount = context[0].getCount();
		logger.info("getNextProfile. count is "+nextCount + " and limit is "+ profileMaxLimit );
		Clip[] profileClips =getClipsByCatId(99,subscriber.getLanguage());
		ArrayList<Clip> engClips = new ArrayList<Clip>();
		for (Clip profileClip : profileClips)
		{
			if (profileClip.getClipRbtWavFile() != null && profileClip.getClipRbtWavFile().indexOf("_eng_") != -1)
			{
				if(profileClip.getClipSmsAlias() != null )
					engClips.add(profileClip);
			}	
		}
		String profileNames = "";
		if(engClips.size() <= nextCount*profileMaxLimit)	
		{
			logger.info("Clips exhausted");
			logger.info("Profile exhausted is"+getSMSTextForID(task,PROFILE_NEXT_EXHAUSTED,null,subscriber.getLanguage()));
			task.setObject(param_response, getSMSTextForID(task,PROFILE_NEXT_EXHAUSTED,null,subscriber.getLanguage()));	
			return;
		}
		else
		{
			int count = nextCount*profileMaxLimit;
			int maxCount = (nextCount+1)*profileMaxLimit; 
			for(int i = count ; i < engClips.size() && i < maxCount; i++)
			{
				Clip profileClip = engClips.get(i);
				StringTokenizer stk = new StringTokenizer(profileClip.getClipSmsAlias(),",");
				profileNames = profileNames + ((i+1) +")" + profileClip.getClipName() + "-" + stk.nextToken() + " ");
			}
			task.setObject(param_response, SUCCESS);
			task.setObject(param_sms_for_user, profileNames);
		
			if(engClips.size() > maxCount)
				task.setObject(PROFILE_NEXT_FOOTER, getSMSTextForID(task,PROFILE_NEXT_FOOTER, null,subscriber.getLanguage()));
			
			task.setObject(param_SEARCHCOUNT , (nextCount+1)+"");
			updateViraldata(task);
		}	
	}
	
	@Override
	public void processRemoveProfile(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subID = task.getString(param_subscriberID);
		SelectionRequest selectionRequest = new SelectionRequest(subID);
		selectionRequest.setStatus(99);
		selectionRequest.setMode("SMS");
		rbtClient.deleteSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();
		
		if (response != null && response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
			task.setObject(param_response, SUCCESS);
			//TEMPORARY_OVERRIDE_REMOVAL_SUCCESS_TEXT
		}else{
			String status = subscriber.getStatus();
			if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
				task.setObject(param_response, FAILURE);
				//TEMPORARY_OVERRIDE_REMOVAL_FAILURE_TEXT
			}else{
				task.setObject(param_response, ERROR);
				//user is not subscribed
			}
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
		
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processHelpRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processHelpRequest(Task task) {
		
		task.setObject(param_response, HELP);

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processRetailerRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processRetailerRequest(Task task) {
		task.setObject(param_send_sms_to_user, false);
		task.setObject(param_send_sms_to_retailer, true);
		String[] values = (String[])task.getObject(param_sms);
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String promoID = null;
		Clip clip = null;
		String response = "FAILURE";
		if(values.length > 2)
			promoID = values[values.length-2];
		
		if (promoID != null)
			clip = getClipByPromoId(promoID,subscriber.getLanguage());
		else if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
			task.setObject(param_retailer_response, ALREADY_ACTIVE);
			return;
		}		
		logger.info("promoID="+promoID+", clip="+clip+", subscriber status="+status);
		if (clip == null && status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
			task.setObject(param_promoID, promoID);
			task.setObject(param_retailer_response, SELECTION_FAILURE);
			return;
		}
		if (clip != null && status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
			if (isExistingSelection(subscriber.getSubscriberID(),clip.getClipId())){
				task.setObject(param_clipName,clip.getClipName());
				task.setObject(param_retailer_response, SELECTION_EXISTS);
				logger.info("seeting response = SELECTION_EXISTS");
				return;
			}
		}
		
		String clipID = (clip != null) ? String.valueOf(clip.getClipId()) : null;
		
		
		ViralData[] viralDataArr = getViraldata(null, subscriber.getSubscriberID(), "RETAILER");
		viralDataArr = reorderViralData(viralDataArr);
		if (viralDataArr != null && viralDataArr.length > 0){
			String lastReqclipID = viralDataArr[viralDataArr.length - 1].getClipID();
			if(clip == null || (lastReqclipID != null && clipID.equals(lastReqclipID)))
			{
				task.setObject(param_retailer_response, EXISTS);
				return;
			}
		}
		
		String retailerID = task.getString(param_RetailerMSISDN);
		DataRequest dataRequest = new DataRequest(retailerID, subscriber.getSubscriberID(),"RETAILER", clipID, new Date(), "SMS");
		ViralData viralData = rbtClient.addViralData(dataRequest);
		if (viralData != null){
			String subResponse = "FAILURE";
			if(promoID == null){
				response = SUBSCRIPTION;
				subResponse = SUBSCRIPTION;
			}else if(clip == null){
				response = ONLY_SUBSCRIPTION_SUCCESS;
				subResponse = ONLY_SUBSCRIPTION_SUCCESS;
			}else if(status.equalsIgnoreCase(WebServiceConstants.ACTIVE)){
				response = SELECTION;
				subResponse = SELECTION;
			}else{
				response = SUCCESS;
				subResponse = SUCCESS;
			}
			if (clip != null){
				task.setObject(param_clipName,clip.getClipName());
			}
			task.setObject(param_retailer_response, response);
			task.setObject(param_response, subResponse);
		}
		task.setObject(param_send_sms_to_user, true);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processRetailerSearch(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processRetailerSearch(Task task){
		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null){
			moreRBTKeywords = getTokenizedList(moreKeywords, ",", true);
		}
		String[] values = (String[])task.getObject(param_sms);
		if (moreRBTKeywords.contains(values[0].toLowerCase())){
			moreRetailerSearch(task);
		}else{	
			removeViraldata(task.getString(param_subscriberID), null, "RETAILER_SEARCH");
			String searchString = "";
			String match = "";
			boolean gotResults = false;
			String searchType = "song";
			HashMap<String, String> searchOnMap = getSearchOnMap();
			if(searchOnMap.containsKey(values[0])) {
				searchType = searchOnMap.get(values[0]);
				for(int i=1;i<values.length;i++)
					searchString = searchString.trim() + " " + values[i];
			}
			else
				for (int i = 0; i < values.length; i++)
					searchString = searchString.trim() + " " + values[i];
			
			ArrayList<LuceneClip>  results = null;
			if ( searchString != null && searchString.trim().length() > 0){
				HashMap<String, String> map = new HashMap<String, String>();;
				map.put(searchType, searchString);
				map.put("SUBSCRIBER_ID", task.getString(param_subscriberID));
				results = (ArrayList<LuceneClip>)luceneIndexer.searchQuery(map, 0, maxSearchResultsToFetch);
			}
			String clipIDs = "";
			boolean moreFlag = false;
			if (results != null && results.size() > 0) {
				gotResults = true;
				//no of results will always be less than or equals to maxSearchResultsToSend
				for(int i=0;i<results.size();i++)
				{
					LuceneClip clip = results.get(i);
					if (clip != null){
						if (clipIDs.equalsIgnoreCase(""))
							clipIDs = "" + clip.getClipId();
						else
							clipIDs = clipIDs + "," + clip.getClipId();
						String songName = clip.getClipName();
						int maxSearchResultsToSend = 4;
						String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
						if (maxResults != null){
							try {
								maxSearchResultsToSend = Integer.parseInt(maxResults);
							} catch (Exception e) {
								maxSearchResultsToSend = 4;
							}
						}
						if (i < maxSearchResultsToSend){
							if (clip.getClipPromoId() != null && clip.getClipEndTime().after(new Date())){
								match = match + songName + "-" + clip.getClipPromoId()+ " ";
							}else{
								match = match + songName + " ";
							}
						}else{
							moreFlag = true;
						}
					}
				}
				addViraldata(task.getString(param_subscriberID), null, "RETAILER_SEARCH", clipIDs, "SMS", 0, null);
			}
			if (!gotResults){
				task.setObject(param_retailer_response, NO_RESULTS);
			}else{
				task.setObject(param_search_results, match);
				if (moreFlag)
					task.setObject(param_retailer_response, SUCCESS2);
				else
					task.setObject(param_retailer_response, SUCCESS);
			}
		}
		task.setObject(param_send_sms_to_user, false);
		task.setObject(param_send_sms_to_retailer, true);
	}
	
	private void moreRetailerSearch(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		logger.info("in more retailer request");
		
		ViralData[] viralDataArr = getViraldata(subID, null, "RETAILER_SEARCH");
		if (viralDataArr == null || viralDataArr.length == 0) {
			task.setObject(param_retailer_response, MORE_RBT_SMS1_FAILURE);
		} else {
			ViralData data = viralDataArr[0];
			String clipIDs = data.getClipID();
			if (clipIDs == null){
				task.setObject(param_retailer_response, MORE_RBT_SMS1_FAILURE);
				return;
			}
			StringTokenizer stk = new StringTokenizer(clipIDs, ",");
			int maxSearchResultsToSend = 4;
			String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
			if (maxResults != null){
				try {
					maxSearchResultsToSend = Integer.parseInt(maxResults);
				} catch (Exception e) {
					maxSearchResultsToSend = 4;
				}
			}
			if (stk.countTokens() <= maxSearchResultsToSend) {
				task.setObject(param_retailer_response, SMS_FAILURE);
			} else {
				String clipId = null;
				for (int clipCounter = 0; clipCounter < maxSearchResultsToSend; clipCounter++) {
					clipId = stk.nextToken();
				}
				String finalClipIds = clipIDs.substring(clipIDs.indexOf(clipId)
						+ clipId.length() + 1, clipIDs.length());

				StringTokenizer newStk = new StringTokenizer(finalClipIds,",");
				int finalCounter = 1;
				String finalMatches = "";
				boolean moreFlag = false;
				while (newStk.hasMoreTokens() && finalCounter <= maxSearchResultsToSend) {
					String token = newStk.nextToken();
					Clip clip = getClipById(token.trim(),subscriber.getLanguage());
					String songName = clip.getClipName();
					if (clip != null){
						if (clip.getClipPromoId() != null && clip.getClipEndTime().after(new Date())){
							finalMatches = finalMatches + songName + "-" + clip.getClipPromoId()+ " ";
						}else{
							finalMatches = finalMatches + songName + " ";
						}
					}
					finalCounter++;
				}
				if (newStk.hasMoreTokens()) {
					moreFlag = true;
				}
				removeViraldata(subID, null, "RETAILER_SEARCH");
				addViraldata(subID, null, "RETAILER_SEARCH", finalClipIds, "SMS", 0, null);

				task.setObject(param_search_results, finalMatches);
				
				if (moreFlag)
					task.setObject(param_retailer_response, SUCCESS2);
				else
					task.setObject(param_retailer_response, SUCCESS);
			}
		}
	}
	
	public void processConfirmCopyRequest(Task task){
		String subscriberID = task.getString(param_subscriberID);
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_CHANGE_TYPE, "COPYCONFIRMED");
		task.setObject(param_WAITTIME, param(GATHERER, WAIT_TIME_DOUBLE_CONFIRMATION,30));
		String response = updateViraldata(task);
		
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			task.setObject(param_response, SUCCESS);
		else
			task.setObject(param_response, FAILURE);
		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	public void processCancelOptInCopy(Task task){
		String subscriberID = task.getString(param_subscriberID);
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_WAITTIME, param(GATHERER, WAIT_TIME_DOUBLE_CONFIRMATION,30));
		boolean response = cancelcopyViraldata(task);
		if (response)
			task.setObject(param_response, SUCCESS);
		else
			task.setObject(param_response, FAILURE);
		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processViralRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processViralRequest(Task task){
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subId = subscriber.getSubscriberID();
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		
		ViralData[] viralDataArr = getViraldata(subId, null, "BASIC");
		if (viralDataArr == null || viralDataArr.length < 1){
			task.setObject(param_response, FAILURE);
			return;
		}
		
		ViralData viralData = viralDataArr[0];
		try {
			if (viralData.getClipID() != null) {
				int clipID = Integer.parseInt(viralData.getClipID());
				Clip clip = rbtCacheManager.getClip(clipID);
				if (clip != null) {
					task.setObject(param_clipid, String.valueOf(clip.getClipId()));
					task.setObject(param_mod_channel, "VIRAL");
					processSelection(task);
					String response = task.getString(param_response);
					if (response.equals(SUCCESS)|| response.equals(ALREADY_EXISTS)){
						task.setObject(param_response, SUCCESS);
						task.setObject(param_clipName, clip.getClipName());//%S
					} else
						task.setObject(param_response, FAILURE);
				}
			}
		} catch (Exception e) {
			task.setObject(param_response, FAILURE);
		} finally {
			logger.info("Going to delete VIRAL request in viral sms table for subscriberID="+subId +" and type=BASIC");
			removeViraldata(subId, null, "BASIC");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processRetailerAccept(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processRetailerAccept(Task task){
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		ViralData[] viralDataArr = getViraldata(null, subscriber.getSubscriberID(),"RETAILER");
		viralDataArr = reorderViralData(viralDataArr);
		ViralData lastViralData = viralDataArr[viralDataArr.length - 1];
		String lastRequestClipID = lastViralData.getClipID();
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date date = new Date();
		
		String activationInfo = "RET:"+lastViralData.getSubscriberID()+ "REQTIME:"
			+ formatter.format(lastViralData.getSentTime())  + "SETTIME:" + formatter.format(date);
		boolean activated = false;
		if (status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)||status.equalsIgnoreCase(WebServiceConstants.NEW_USER)){
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
			subscriptionRequest.setMode("RET");
			subscriptionRequest.setModeInfo(activationInfo);
			subscriptionRequest.setIsPrepaid(Boolean.valueOf(task.getString(param_isPrepaid)));
			String retailerSubscriptionClass = getSMSParameter(RETAILER_SUBSCRIPTION_CLASS);
			subscriptionRequest.setSubscriptionClass(retailerSubscriptionClass);
			subscriber = rbtClient.activateSubscriber(subscriptionRequest);
			status = subscriber.getStatus();
			
			if (!(status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)||status.equalsIgnoreCase(WebServiceConstants.NEW_USER)))
				activated = true;
		}else
			activated = true;
		
		if (activated){
			if (lastRequestClipID != null){
				Clip clip = getClipById(lastRequestClipID,subscriber.getLanguage());
				SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
				selectionRequest.setMode("RET");
				int retailerSelectionCategory = 13;
				String retailerSelCat = getSMSParameter(RETAILER_SELECTION_CATEGORY);
				if (retailerSelCat != null){
					try{
						retailerSelectionCategory = Integer.parseInt(retailerSelCat);
					}catch(NumberFormatException e){
						retailerSelectionCategory = 13;
					}
				}
				selectionRequest.setCategoryID(retailerSelectionCategory+"");
				selectionRequest.setClipID(lastRequestClipID);
				selectionRequest.setModeInfo(activationInfo);
				selectionRequest.setIsPrepaid(Boolean.valueOf(task.getString(param_isPrepaid)));
				selectionRequest.setCircleID(subscriber.getCircleID());
				rbtClient.addSubscriberSelection(selectionRequest);
				
				if (clip != null){
					task.setObject(param_clipName, clip.getClipName());
				}
			}
			
			task.setObject(param_response, ACCEPT);
			task.setObject(param_RetailerMSISDN, lastViralData.getSubscriberID());
			task.setObject(param_retailer_response, ACCEPT);
			
			task.setObject(param_send_sms_to_user, true);
			task.setObject(param_send_sms_to_retailer, true);
			task.setObject(param_isRetailerAccept, "true");
		}
		removeViraldata(null, subscriber.getSubscriberID(), "RETAILER");
	}
	
	private String[] rearrangeSmsText(Task task, String smsText) {
		logger.info("smsText:"+smsText);
		String[] sms = null;
		StringTokenizer msgTokens = new StringTokenizer(smsText, " ");
		List<String> list = new ArrayList<String>();
		while (msgTokens.hasMoreTokens()) {
			list.add(msgTokens.nextToken());
		}
		sms = (String[]) list.toArray(new String[0]);
		
		boolean rbtKey = false;
		List<String> numbers = new ArrayList<String>();
		List<String> smsList = new ArrayList<String>();
		for (int j = 0; j < sms.length; j++) {
			try {
				Long.parseLong(sms[j]);
				numbers.add(sms[j]);
				continue;
			} catch (Exception e) {

			}

			int i = 0;
			for (; i < supportedLangs.length; i++) {
				if (supportedLangs[i].equalsIgnoreCase(sms[j])) {
					task.setObject(param_language, supportedLangs[i]);
					break;
				}
			}
			if (i == supportedLangs.length) {
				if (checkKey(sms[j])) {
					rbtKey = true;
				} else {
					smsList.add(sms[j]);
				}
			}
		}
		boolean isRBTKeywordOptional = false;
		String rbtKeyOptional = getSMSParameter(RBT_KEYWORD_OPTIONAL);
		if (rbtKeyOptional != null && rbtKeyOptional.equalsIgnoreCase("true"))
			isRBTKeywordOptional = true;
		if (rbtKey || isRBTKeywordOptional) {

			String temp = null;
			for (int i = 0; i < numbers.size(); i++) {
				if (temp == null && ((String) numbers.get(i)).length() >= 10) { //7) {
					temp = (String) numbers.get(i);
				} else {
					smsList.add(numbers.get(i));
				}
			}
			if (temp != null)
				smsList.add(temp);
			return ((String[]) smsList.toArray(new String[0]));
		} else
			return null;
	}
	
	private boolean checkKey(String strKey) {
		String keyword = getSMSParameter(RBT_KEYWORDS);
		String[] rbtKeywords = (String[]) getTokenizedList(keyword, ",", true).toArray(new String[0]);
		if (rbtKeywords != null) {
			for (int i = 0; i < rbtKeywords.length; i++) {
				if (strKey.equalsIgnoreCase(rbtKeywords[i]))
					return true;
			}
		}
		return false;
	}
	
	private boolean isRetailerRequest(String values[])
	{
		for (int i = 0; i < values.length; i++)
        {
            if(values[i].equalsIgnoreCase("RET"))
            	return true;
        }
		return false;
	}
	
	private boolean checkAct(String strMsg) {
		String subKeywords = getSMSParameter(ACTIVATION_KEYWORD);
		String[] subMsgs = (String[]) getTokenizedList(subKeywords, ",", false).toArray(new String[0]);
		for (int i = 0; i < subMsgs.length; i++) {
			if (strMsg.equalsIgnoreCase(subMsgs[i]))
				return true;
		}
		return false;
	}
	
	private boolean checkDeAct(String strMsg) {
		String unsubKeywords = getSMSParameter(DEACTIVATION_KEYWORD);
		String[] unsubMsgs = (String[]) getTokenizedList(unsubKeywords, ",", false).toArray(new String[0]);
		for (int i = 0; i < unsubMsgs.length; i++) {
			if (strMsg.equalsIgnoreCase(unsubMsgs[i]))
				return true;
		}
		return false;
	}

	private boolean checkOptInCopyCancel(String strMsg) {
		String[] optInCopyCancelKeywords = null;
		String optInCopyCancelKeys = getSMSParameter(COPY_CANCEL_KEYWORD);
		if (optInCopyCancelKeys != null)
			optInCopyCancelKeywords = (String[]) getTokenizedList(optInCopyCancelKeys, ",", true).toArray(new String[0]);
		for (int i = 0; i < optInCopyCancelKeywords.length; i++) {
			if (strMsg.equalsIgnoreCase(optInCopyCancelKeywords[i]))
				return true;
		}
		return false;
	}

	private boolean checkCopyConfirm(String strMsg) {
		String[] copyConfirmKeywords = null;
		String copyConfirmKeys = getSMSParameter(COPY_CONFIRM_KEYWORD);
		if (copyConfirmKeys != null)
			copyConfirmKeywords = (String[]) getTokenizedList(copyConfirmKeys, ",", true).toArray(new String[0]);
		for (int i = 0; i < copyConfirmKeywords.length; i++) {
			if (strMsg.equalsIgnoreCase(copyConfirmKeywords[i]))
				return true;
		}
		return false;
	}
	
	private boolean checkViral(String strMsg) {
		List<String> viralRBTKeywords = new ArrayList<String>();
		String viralKeywordsStr = getSMSParameter(VIRAL_KEYWORDS);		
		if (viralKeywordsStr != null)
			viralRBTKeywords = getTokenizedList(viralKeywordsStr, ",", true);
		if (viralRBTKeywords.contains(strMsg.toLowerCase()))
			return true;
		return false;
	}
	
	private boolean checkRetailerAcceptRequest(String keyword, String subID) {
		if (keyword == null) return false;
		List<String> retailerAcceptKeywords = new ArrayList<String>();
		String retailerAccKeys = getSMSParameter(RETAILER_REQUEST_ACCEPT_KEYWORD);
		if (retailerAccKeys != null){
			retailerAcceptKeywords = getTokenizedList(retailerAccKeys, ",", true);
		}
		if (retailerAcceptKeywords.contains(keyword.toLowerCase())){
			if (getViraldata(null, subID,"RETAILER") != null)
				return true;
		}
			
		return false;
	}
	
	private String[] getPriceAndValidity(String chargeClassStr) {
		String[] priceNvalidity = new String[2];

		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setName(chargeClassStr);
		ChargeClass chargeClass = rbtClient.getChargeClass(applicationDetailsRequest);

		if (chargeClass != null) {
			priceNvalidity[0] = "Rs." + chargeClass.getAmount();

			String validity = chargeClass.getPeriod();
			String validFor = null;
			String validForNo = null;
			validForNo = validity.substring(1);
			if (validity.startsWith("D")) {
				validFor = "days";
			} else if (validity.startsWith("M")) {
				validFor = "months";
			} else if (validity.startsWith("Y")) {
				validFor = "years";
			}
			priceNvalidity[1] = validForNo + validFor;
		}

		return priceNvalidity;
	}
	
	public boolean checkFeed(String strMsg) {
		if (feedMsgs != null) {
			for (int i = 0; i < feedMsgs.length; i++) {
				if (strMsg.equalsIgnoreCase(feedMsgs[i]))
					return true;
			}
		}
		return false;
	}
	
	private boolean checkTempCan(String strMsg) {
		String[] tempCanMsgs = null;
		String canMsgs = getSMSParameter(TEMPORARY_OVERRIDE_CANCEL_MESSAGE);
		if (canMsgs != null)
			tempCanMsgs = (String[]) getTokenizedList(canMsgs, ",", true).toArray(new String[0]);
		if (tempCanMsgs != null) {
			for (int i = 0; i < tempCanMsgs.length; i++) {
				if (strMsg.equalsIgnoreCase(tempCanMsgs[i]))
					return true;
			}
		}
		return false;
	}
	
	private boolean checkList(String strMsg) {
		String[] listRBTKeywords = null;
		String listKeys = getSMSParameter(LIST_RBT_KEYWORD);
		if (listKeys != null)
			listRBTKeywords = (String[]) getTokenizedList(listKeys, ",", true).toArray(new String[0]);
		
		if (listRBTKeywords != null) {
			for (int i = 0; i < listRBTKeywords.length; i++) {
				if (strMsg.equalsIgnoreCase(listRBTKeywords[i]))
					return true;
			}
		}
		return false;
	}
	
	private boolean checkListNext(String strMsg) {
		String[] listNextProfileKeywords = null;
		String listNextKeys = getSMSParameter(NEXT_PROFILE_KEYWORD);
		if (listNextKeys != null)
			listNextProfileKeywords = (String[]) getTokenizedList(listNextKeys, ",", true).toArray(new String[0]);
		
		if (listNextProfileKeywords != null) {
			for (int i = 0; i < listNextProfileKeywords.length; i++) {
				if (strMsg.equalsIgnoreCase(listNextProfileKeywords[i]))
					return true;
			}
		}
		return false;
	}
	public boolean checkSearchResponse(String strMsg){
		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null){
			moreRBTKeywords = getTokenizedList(moreKeywords, ",", true);
		}
		if (strMsg == null) return false;
		int songNo = -1;
		try {
			songNo = Integer.parseInt(strMsg);
		} catch (Exception e) {
			songNo = -1;
		}
		int maxSearchResultsToSend = 4;
		String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
		if (maxResults != null){
			try {
				maxSearchResultsToSend = Integer.parseInt(maxResults);
			} catch (Exception e) {
				maxSearchResultsToSend = 4;
			}
		}
		if (songNo > 0 && songNo <= maxSearchResultsToSend){
			return true;
		}else if (songNo > maxSearchResultsToSend){
			return false;
		}else if (moreRBTKeywords.contains(strMsg.toLowerCase())){
			return true;
		}
		return false;
	}

	private boolean checkClipPromo(Task task) {
		Subscriber sub=(Subscriber)task.getObject(param_subscriber);
		String[] values = (String[])task.getObject(param_sms);
		Clip clip = getClipByPromoId(values[0],sub.getLanguage());
		if (clip != null){
			task.setObject(param_clip, clip);
			return true;
		}
		return false;
	}
	
	private boolean checkCategoryPromo(Task task) {
		Subscriber sub=(Subscriber)task.getObject(param_subscriber);
		String[] values = (String[])task.getObject(param_sms);
		Category category = getCategoryByPromoId(values[0],sub.getLanguage());
		if (category != null) {
			Clip[] clips = getClipsByCatId(category.getCategoryId(),sub.getLanguage());
			if (clips != null) {
				Clip clip = clips[0];
				task.setObject(param_clip, clip);
				task.setObject(param_catid, String.valueOf(category.getCategoryId()));
				return true;
			}
		}
		return false;
	}

	private boolean checkCategoryAlias(Task task) {
		String[] values = (String[])task.getObject(param_sms);
		Category category = rbtCacheManager.getCategoryBySMSAlias(values[0]);
		if (category != null){
			task.setObject(param_category, category);
			task.setObject(param_catid, String.valueOf(category.getCategoryId()));
			return true;
		}
		return false;
	}

	private boolean checkClipAlias(Task task) {
		String[] values = (String[])task.getObject(param_sms);
		Clip clip = rbtCacheManager.getClipBySMSAlias(values[0]);
		if (clip == null || clip.getSmsStartTime() == null || clip.getClipEndTime() == null
				|| (clip.getSmsStartTime() != null && clip.getSmsStartTime().after(new Date()))) {
			return false;
		}
		return true;
	}
	
	private boolean isTNBRequest(String strMsg) 
	{
		List<String> tnbKeywordsList = new ArrayList<String>();
		String tnbKeyWordStr = getSMSParameter(TNB_KEYWORDS);
		
		if (tnbKeyWordStr != null)
			tnbKeywordsList = Arrays.asList(tnbKeyWordStr.split(","));
		
		logger.info("RBT:isTNBRequest tnbKeywordsList >"+tnbKeywordsList +" and Msg >"+strMsg);
		if (tnbKeywordsList.contains(strMsg))
			return true;
		else return false;
	}

	private boolean checkProfile(Task task) 
	{
		String[] values = (String[])task.getObject(param_sms);
		Clip profileClip = getProfileClip(task, values[0]);
		if (profileClip == null)
			return false;
		else
		{
			task.setObject(param_clip, profileClip);
			return true;
		}
	}

	private Clip getProfileClip(Task task, String strValue) 
	{
		Clip[] clips = getActiveClips(profileCategoryID);
		if (clips == null || clips.length == 0)
			return null;

		String defaultLanguage = "eng";
		String preferedLanguage = (String)task.getString(param_language);
		Clip profileClip = null;

		for (Clip clip : clips) 
		{
			if(clip.getClipSmsAlias() != null)
			{
				if(clip.getClipRbtWavFile() != null && clip.getClipRbtWavFile().indexOf("_"+preferedLanguage+"_") != -1)
				{
					if(tokenizeArrayList(clip.getClipSmsAlias().toLowerCase(), ",").contains(strValue.toLowerCase()))
					{
						profileClip = clip;
						break;
					}
				}
				else if(clip.getClipRbtWavFile() != null && clip.getClipRbtWavFile().indexOf("_"+defaultLanguage+"_") != -1)
				{
					if(tokenizeArrayList(clip.getClipSmsAlias().toLowerCase(), ",").contains(strValue.toLowerCase()))
					{
						profileClip = clip;
					}
				}
			}
		}

		return profileClip;
	}
		
	private boolean checkAlias(Task task, String strValue, int id, String strName,
			String strAlias, String lang1) {
		if (strAlias == null)
			return false;
		String lang = task.getString(param_language);
		StringTokenizer tokens = new StringTokenizer(strAlias, ":");
		List aliasList = new ArrayList();
		while (tokens.hasMoreTokens()) {
			aliasList.add(tokens.nextToken().trim().toLowerCase());//busy
		}
		if (aliasList.size() > 0) {
			String[] aliases = (String[]) aliasList.toArray(new String[0]);
			int index = 0;
			while (index < aliases.length) {
				if (aliases[index].equalsIgnoreCase(strValue) //== true
						&& ((strName.indexOf(" " + lang) != -1) || lang.equalsIgnoreCase(lang1))) {
					return true;
				}
				index++;
			}
		
			if ((strName.indexOf(" " + defaultLang) != -1) || defaultLang.equalsIgnoreCase(lang1)){
				index = 0;
				while (index < aliases.length) {
					if (aliases[index].equalsIgnoreCase(strValue)){
						task.setObject(param_language, defaultLang);
						return true;
					}
					index++;
				}
			}
		}
		return false;
	}
	
	//using for feed
	public void processDeactivateSubscriberRecords(Task task){
		String response = null;
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		selectionRequest.setMode(task.getString(param_actby));
		selectionRequest.setIsPrepaid(((Subscriber)task.getObject(param_subscriber)).isPrepaid());
		selectionRequest.setModeInfo(task.getString(param_actInfo));
	    selectionRequest.setCallerID(task.getString(param_callerid));
		selectionRequest.setSubscriptionClass(task.getString(param_subclass));
		Object statusObj = task.getObject(param_status);
		if (statusObj != null){
			Integer status = (Integer)statusObj;
			selectionRequest.setStatus(status);
		}
		rbtClient.deleteSubscriberSelection(selectionRequest);
	}
}
