/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.tatacdma;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
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
 * Created on 28th April 2009
 */
public class TataCDMAProcessor extends SmsProcessor
{
	private String[] trialMsgs = null;
	private List<String> parentCatIdsListForCatSearch = null;
	private int maxSearchResultsToSendForCatSearch = 6;
	public List<String> deleteKeywords = new ArrayList<String>();//List of Delete keywords
	public List<String> moreDeleteKeywords = new ArrayList<String>();//List of More Delete keywords
	
	/**
	 * @throws Exception 
	 * 
	 */
	public TataCDMAProcessor() throws Exception
	{
		String subKeywords = getSMSParameter(ACTIVATION_KEYWORD);
		if (subKeywords == null)
			throw new RBTException("Activation Keywords are null");
		
		String unsubKeywords = getSMSParameter(DEACTIVATION_KEYWORD);
		if (unsubKeywords == null)
			throw new RBTException("Deactivation Keywords are null");

		String keyword = getSMSParameter(RBT_KEYWORDS);
		if (keyword == null)
			throw new RBTException("RBT SMS Keywords are null");
		
		deleteKeywords.add("delete");
		moreDeleteKeywords.add("more");
		
		//getting cat id list for category search
		String catIDs = getSMSParameter(PARENT_CAT_ID_LIST_FOR_CAT_SEARCH);
		if (catIDs != null)
			parentCatIdsListForCatSearch = Arrays.asList(catIDs.split(","));
        
		//getting max search results to send for cat search
		String maxResults = getSMSParameter(MAX_SEARCH_RESULTS_TO_SEND_FOR_CAT_SEARCH);
		if (maxResults != null){
			int junkNumber = -1;
			try {
				junkNumber = Integer.parseInt(maxResults);
			} catch (Exception e) {
				junkNumber = -1;
			}
			if (junkNumber != -1) 
				maxSearchResultsToSendForCatSearch = junkNumber;
		}	
	
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#getTask(java.util.HashMap)
	 */
	@Override
	public Task getTask(HashMap<String, String> requestParams)
	{
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		Task task = new Task(null, taskSession);
		reorderParameters(task);

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		String smsNo = param(iRBTConstant.SMS, SMS_NO, "123456");
		task.setObject(param_Sender, smsNo);

		if (task.containsKey("error"))
		{
			if (task.getString("error").equalsIgnoreCase("true"))
			{
				task.setObject(param_response, NO_SMS);
				return task;
			}
		}

		String smsText = task.getString(param_smsText);
		String[] values = rearrangeSmsText(task, smsText);

		if (values == null || values.length == 0)
		{
			logger.info("No tokens in smsText");

			task.setObject(param_response, HELP);
			task.setObject(param_error, "true");
			return task;
		}

		task.setObject(param_sms, values);
		logger.info("SMS Text after re-ordering :" + values.toString());

		boolean isRetailerRequest = false;
		boolean isOBDRequest = false;

		if (isOBDRequest(task))
		{
			task.setObject(param_isOBDRequest, "true");
			isOBDRequest = true;

			String[] tokens = smsText.split(" ");
			if (tokens.length < 3)
			{
				logger.info("obd request - Invalid data");
				
				task.setObject(param_obd_response, "INVALID_DATA");
				task.setObject(param_error, "true");
				return task;
			}
			String obdKeyword = tokens[0];
			String obdActKeyword = getSMSParameter(OBD_ACT_KEYWORD);
			if (obdActKeyword != null
					&& !obdKeyword.equalsIgnoreCase(obdActKeyword))
			{
				logger.info("obd request - Invalid obd request");

				task.setObject(param_obd_response, "INVALID_OBD_REQUEST");
				task.setObject(param_error, "true");
				return task;
			}
			String subID = tokens[1];
			String toneID = tokens[2];
			task.setObject(param_subscriberID, subID);
			task.setObject(param_promoID, toneID);
		}
		else if (isRetailerRequest(values))
		{
			task.setObject(param_isRetailerRequest, "true");
			String retailerID = task.getString(param_subscriberID);
			task.setObject(param_RetailerMSISDN, retailerID);
			
			ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
			applicationDetailsRequest.setRetailerID(retailerID);
			Retailer retailer = rbtClient.getRetailer(applicationDetailsRequest);
			if (retailer == null)
			{
				logger.info("Non retailer " + retailerID + " sent retailer request.");
				
				task.setObject(param_retailer_response, NON_RETAILER);
				task.setObject(param_send_sms_to_user, false);
				task.setObject(param_send_sms_to_retailer, true);
				task.setObject(param_error, "true");
				return task;
			}

			task.setObject(param_retailer, retailer);
			String subID = values[values.length - 1];
			task.setObject(param_subscriberID, subID);
			isRetailerRequest = true;
		}
		else
		{
			task.setObject(param_isRetailerRequest, "false");
			task.setObject(param_isOBDRequest, "false");
			if (checkTrialReply(values[0]))
			{
				List<String> al = new ArrayList<String>();

				al.add(trialMsgs[0]);
				al.add(values[0].substring(values[0].length() - 1,
						values[0].length()));

				values = al.toArray(new String[0]);
				task.setObject(param_sms, values);
			}
		}

		String subscriberID = task.getString(param_subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		task.setObject(param_subscriber, subscriber);

		if (isRetailerRequest)
		{
			task.setObject(param_send_sms_to_user, false);
			task.setObject(param_send_sms_to_retailer, true);
		}
		else if (isOBDRequest)
		{
			task.setObject(param_send_sms_to_user, false);
		}

		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
		{
			logger.info("User in activation pending");

			if (isRetailerRequest)
				task.setObject(param_retailer_response, ACTIVATION_PENDING);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response,
						"SUBSCRIBER_ACTIVATION_PENDING");
			}
			else
			{
				task.setObject(param_response, ACTIVATION_PENDING);
			}

			task.setObject(param_error, "true");
			return task;
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
		{
			logger.info("User in deactivation pending");

			if (isRetailerRequest)
				task.setObject(param_retailer_response, DEACTIVATION_PENDING);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response,
						"SUBSCRIBER_DEACTIVATION_PENDING");
			}
			else
			{
				task.setObject(param_response, DEACTIVATION_PENDING);
			}
			
			task.setObject(param_error, "true");
			return task;
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
		{
			logger.info("User account suspended");
			
			if (isRetailerRequest)
				task.setObject(param_retailer_response, SUSPENDED);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response, "SUBSCRIBER_SUSPENDED");
			}
			else
			{
				task.setObject(param_response, SUSPENDED);
			}

			task.setObject(param_error, "true");
			return task;
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED))
		{
			logger.info("RBT::getTask:User account blacklisted");

			if (isRetailerRequest)
				task.setObject(param_retailer_response, BLACK_LISTED);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response, "SUBSCRIBER_BLACK_LISTED");
			}
			else
			{
				task.setObject(param_response, BLACK_LISTED);
			}

			task.setObject(param_error, "true");
			return task;
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.ERROR))
		{
			logger.info("RBT::getTask:User account blacklisted");

			if (isRetailerRequest)
				task.setObject(param_retailer_response, TECHNICAL_ERROR);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response, TECHNICAL_ERROR);
			}
			else
			{
				task.setObject(param_response, TECHNICAL_ERROR);
			}

			task.setObject(param_error, "true");
			return task;
		}

		if (!subscriber.isCanAllow() || !subscriber.isValidPrefix())
		{
			if (!subscriber.isCanAllow())
				if (isRetailerRequest)
					task.setObject(param_retailer_response, ACCESS_FAILURE);
				else if (isOBDRequest)
				{
					task.setObject(param_obd_response,
							"SUBSCRIBER_NOT_AUTHORIZED");
				}
				else
				{
					task.setObject(param_response, ACCESS_FAILURE);
				}
			else if (isRetailerRequest)
				task.setObject(param_retailer_response, INVALID);
			else if (isOBDRequest)
			{
				task.setObject(param_obd_response, "INVALID_SUBSCRIBER");
			}
			else
			{
				task.setObject(param_response, INVALID);
			}
			task.setObject(param_error, "true");
			return task;
		}

		// identifies the task action to be performed
		String taskAction = getTaskAction(values, task); 
		task.setTaskAction(taskAction);

		logger.info("RBT:: task: " + task.toString());
		return task;
	}
	
	private String getTaskAction(String[] values, Task task)
	{
		String action = null;
		if (task.containsKey(param_isRetailerRequest)
				&& task.getString(param_isRetailerRequest).equalsIgnoreCase("true"))
			action = action_retailer_request;
		else if (task.containsKey(param_isOBDRequest) &&
				task.getString(param_isOBDRequest).equalsIgnoreCase("true"))
			action = action_obd;
		else if (checkAct(values[0]))
			action = action_activate;
		else if (checkDeAct(values[0]))
			action = action_deactivate;
		else if (checkOptInCopyCancel(values[0]))
			action = action_optin_copy_cancel;
		else if (checkCopyConfirm(values[0]))
			action = action_copy_confirm;
		else if (checkClipAlias(task, getFiveDigitClipID(task, values)))
			action = action_clip_alias;
		else if (values.length > 1 && checkTrial(values[0]))
			action = action_trialReply;
		else if (checkTrial(values[0]))
			action = action_trial;
		else if (checkLoop(values[0]))
			action = action_loop;
		else if (checkDelete(values[0]))
			action = action_delete;
		else if (checkHelpRequest(values[0])
				|| checkHelpRequest((String) task.getObject(param_smsText)))
			action = action_help;
		else if (checkTempCan(values[0]))
			action = action_remove_profile;
		else if (checkFeed(values[0]))
			action = action_feed;
		else if (checkCategorySearch(task))
			action = action_cat_search;
		else if (checkSearchResponse(values[0]))
			action = action_default_search;
		else if (checkCategoryPromo(task))
			action = action_category_promo;
		else if (checkCategoryAlias(task))
			action = action_category_alias;
		else
			action = action_default_search;
		
		logger.info("Setting task action as :" + action);
		return action;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#reorderParameters(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	protected void reorderParameters(Task task)
	{
		String smsText = task.getString(param_smsText);
		String subscriberID = task.getString(param_subID);
		String ipAddress = task.getString(param_ipAddress);
		String smsParam = task.getString(param_smsParam);
		String access = task.getString(param_access);
		String actInfo = ipAddress + ":SMS";

		if (logger.isInfoEnabled())
			logger.info("RBT: smsText = " + smsText + ", subID = " + subscriberID
					+ " ipAddress = " + ipAddress);

		if (ipAddress == null || !isValidIP(ipAddress))
		{
			logger.info("Invalid IP Adresss");

			task.setObject(param_response, INVALID_IP_ADDRESS);
			task.setObject(param_error, "true");
			return;
		}

		if (subscriberID != null)
			subscriberID = subscriberID.trim();
		if (subscriberID != null && subscriberID.equals(""))
			subscriberID = null;

		if (access != null)
			access = access.toLowerCase();

		if (smsText != null)
		{
			String[] smsTokens = smsText.split(" ");
			smsText = "";
			for (String smsToken : smsTokens)
			{
				String smsActPromoPrefix = getSMSParameter(SMS_ACT_PROMO_PREFIX);
				if (smsActPromoPrefix != null
							&& smsToken.toLowerCase().startsWith(
									smsActPromoPrefix))
					actInfo = ipAddress + ":" + smsToken;
				else
					smsText = smsText.trim() + " " + smsToken;
			}
			smsText = smsText.trim();
			
			if (smsParam != null)
				smsText += " " + smsParam.trim();
		}

		if (smsText == null || subscriberID == null)
		{
			logger.info("Insufficient Parameters, subscriberID or smsText is null");

			task.setObject(param_response, INSUFFICIENT_PARAMETERS);
			task.setObject(param_error, "true");
			return;
		}

		task.setObject(param_isPrepaid, String.valueOf(isPrepaid));
		task.setObject(param_actInfo, actInfo);
		task.setObject(param_smsText, smsText);
		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_access, access);

		task.setObject(param_language, defaultLang);

		if (smsText.length() == 0)
		{
			logger.info("smsText can not be empty string");

			task.setObject(param_response, HELP);
			task.setObject(param_error, "true");
			return;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processActivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public Subscriber processActivation(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		logger.info("Processing Activation for subID:"
				+ subscriber.getSubscriberID() + ", isPrepaid:"
				+ task.getString(param_isPrepaid));
		
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
		{
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriber.getSubscriberID());

			if (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false))
			{
				Offer offer = getBaseOffer(task);
				if (offer != null)
				{
					subscriptionRequest.setOfferID(offer.getOfferID());
					subscriptionRequest.setSubscriptionClass(offer.getSrvKey());

					task.setObject(param_subclass, offer.getSrvKey());
				}
			}

			subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
			if (task.getString(param_mod_channel) != null)
				subscriptionRequest.setMode(task.getString(param_mod_channel));
			else
				subscriptionRequest.setMode("SMS");
			subscriptionRequest.setInfo(task.getString(param_actInfo));
			subscriptionRequest.setCircleID(subscriber.getCircleID());
			if (task.containsKey(param_cosid))
			{
				subscriptionRequest.setCosID((Integer) task
						.getObject(param_cosid));
			}
			subscriber = rbtClient.activateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			{
				task.setObject(param_subscriber, subscriber);
				response = SUCCESS;
			}
			else
			{
				response = TECHNICAL_FAILURE;
			}
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			response = ALREADY_ACTIVE;
		else if (status.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			response = SUSPENDED;
		else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
			response = ACTIVATION_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			response = DEACTIVATION_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING))
			response = GIFTING_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING))
			response = RENEWAL_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED))
			response = BLACK_LISTED;
		else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX))
			response = INVALID;
		else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING))
			response = EXPRESS_COPY_PENDING;
		else
			response = HELP;

		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		return subscriber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processDeactivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public String processDeactivation(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		
		logger.info("Processing Deactivation for subID:"
				+ subscriber.getSubscriberID());
		
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
			response = NOT_ACTIVE;
		else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
			response = ACTIVATION_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			response = DEACTIVATION_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.GIFTING_PENDING))
			response = GIFTING_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING))
			response = RENEWAL_PENDING;
		else if (status.equalsIgnoreCase(WebServiceConstants.BLACK_LISTED))
			response = BLACK_LISTED;
		else if (status.equalsIgnoreCase(WebServiceConstants.INVALID_PREFIX))
			response = INVALID;
		else if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING))
			response = EXPRESS_COPY_PENDING;
		else
		{
			// active, suspended, grace
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriber.getSubscriberID());
			subscriptionRequest.setMode("SMS");
			subscriptionRequest.setInfo(task.getString(param_actInfo));
			subscriber = rbtClient.deactivateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();
			if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			{
				task.setObject(param_subscriber, subscriber);
				response = SUCCESS;
			}
			else if (response.equalsIgnoreCase(WebServiceConstants.DCT_NOT_ALLOWED))
				response = TECHNICAL_FAILURE;
			else
				response = TECHNICAL_FAILURE;
		}

		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		return null;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processSelection(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processSelection(Task task)
	{
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String clipID = task.getString(param_clipid);
		
		logger.info("Processing Selection for subID:"
				+ subscriber.getSubscriberID() + " ,isPrepaid:"
				+ subscriber.isPrepaid() + " ,clipID:" + clipID);
		
		SelectionRequest selectionRequest = new SelectionRequest(
				subscriber.getSubscriberID(), subscriber.isPrepaid(),
						String.valueOf(smsCategoryID), clipID, null, null);
		String status1 = subscriber.getStatus();

		if (status1.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status1.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
		{
			if (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false))
			{
				Offer offer = getBaseOffer(task);
				if (offer != null)
				{
					selectionRequest.setSubscriptionOfferID(offer.getOfferID());
					selectionRequest.setSubscriptionClass(offer.getSrvKey());

					task.setObject(param_subclass, offer.getSrvKey());
				}
			}
		}

		if (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false))
		{
			Offer offer = getSelOffer(task);
			if (offer != null)
			{
				selectionRequest.setOfferID(offer.getOfferID());
				selectionRequest.setChargeClass(offer.getSrvKey());

				task.setObject(param_chargeclass, offer.getSrvKey());
			}
		}

		String callerID = null;
		if (task.containsKey(param_callerid))
		{
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
		if (ph != null)
			selectionRequest.setProfileHours(ph);

		Object statusObj = task.getObject(param_status);
		if (statusObj != null)
		{
			Integer status = (Integer) statusObj;
			selectionRequest.setStatus(status);
		}

		if (task.getString(param_catid) != null)
			selectionRequest.setCategoryID(task.getString(param_catid));

		if (task.getString(param_cricket_pack) != null)
			selectionRequest.setCricketPack(task.getString(param_cricket_pack));

		selectionRequest.setInLoop(true);
		Rbt rbt = rbtClient.addSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
		{
			Library lib = rbt.getLibrary();
			task.setObject(param_library, lib);
			response = SUCCESS;
		}
		else if (response.equalsIgnoreCase(WebServiceConstants.SUSPENDED)
				|| response.equalsIgnoreCase(WebServiceConstants.SELECTION_SUSPENDED))
			response = SUSPENDED;
		else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_NOT_EXISTS))
			response = NOT_AVAILABLE;
		else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED))
			response = EXPIRED;
		else if (response.equalsIgnoreCase(WebServiceConstants.FAILED))
			response = TECHNICAL_FAILURE;
		else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_EXISTS))
			response = ALREADY_EXISTS;
		else if (response.equalsIgnoreCase(WebServiceConstants.TECHNICAL_DIFFICULTIES))
			response = TECHNICAL_FAILURE;
		else if (response.equalsIgnoreCase(WebServiceConstants.ALREADY_MEMBER_OF_GROUP))
			response = TECHNICAL_FAILURE;
		else if (response.equalsIgnoreCase(WebServiceConstants.NOT_ALLOWED))
			response = TECHNICAL_FAILURE;
		else
			response = HELP;

		task.setObject(param_response, response);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processFeed(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processFeed(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		String subStatus = subscriber.getStatus();
		
		task.setObject(param_status, 90);
		boolean activated = false;
		if (subStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			activated = true;
		
		String pass = null;
		String status = null;
		String[] values = (String[]) task.getObject(param_sms);
		if (values.length > 1)
		{
			if (!values[1].equalsIgnoreCase("ON")
					&& !values[1].equalsIgnoreCase("OFF"))
				pass = values[1].toUpperCase();
			else
				status = values[1];
		}

		if (pass == null && values.length > 2)
			pass = values[2].toUpperCase();

		if (!activated)
		{
			processActivation(task);
			subscriber = (Subscriber) task.getObject(param_subscriber);
			subStatus = subscriber.getStatus();
			if (subStatus.equals(WebServiceConstants.ACTIVE)
					|| subStatus.equals(WebServiceConstants.ACT_PENDING))
				activated = true;
		}
		if (activated)
		{
			String feed1 = null;
			if (feedStatus != null)
				feed1 = feedStatus.getSmsKeywords();

			FeedStatus feed = null;
			if (feed1 != null)
			{
				feed1 = feed1.toUpperCase();
				String smsKey = feed1.substring(0, feed1.indexOf(","));
				ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
				applicationDetailsRequest.setType(smsKey);
				feed = rbtClient.getFeedStatus(applicationDetailsRequest);
			}

			String feedFile = null;
			// cricket pass = true for virgin
			if (status == null || status.equalsIgnoreCase("ON"))
			{
				if (pass == null || !cricketSubKey.contains(pass))
				{
					pass = "SP";
				}
				if (feed != null)
				{
					feedFile = feed.getFeedFile();
					if (feedFile != null && feedFile.indexOf(",") != -1)
					{
						feedFile = feedFile.substring(feedFile
								.lastIndexOf(",") + 1);
					}
					if (feed.getStatus().equalsIgnoreCase("OFF"))
						feedFile = null;
				}
				Feed schedule = getCricketClass(pass);
				if (schedule == null)
				{
					String sms = feedStatus.getFeedFailureSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_FAILURE);
				}
				else
				{
					Setting[] settings = getActiveSubSettings(subID, 90);
					Setting cricSel = null;
					if (settings == null || settings.length == 0)
					{
						logger.info("No active settings for subscriber = "
								+ subID + " for status = 90");
						String sms = feedStatus.getFeedFailureSms();
						task.setObject(param_sms_for_user, sms);
						task.setObject(param_response, FEED_FAILURE);
					}
					else
					{
						for (int i = 0; i < settings.length; i++)
						{
							Setting setting = settings[i];
							if (setting.getCallerID().equalsIgnoreCase(
									WebServiceConstants.ALL))
							{
								cricSel = setting;
								break;
							}
						}
					}
					if (cricSel != null
							&& (cricSel.getEndTime().after(
									schedule.getEndDate()) ||
								cricSel.getEndTime().equals(
										schedule.getEndDate())))
					{
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
			}
			else if (status.equalsIgnoreCase("OFF"))
			{
				// checking is subscriber already active on status 90
				Setting[] settings = getActiveSubSettings(subID, 90);
				Setting cricSel = null;
				for (int i = 0; i < settings.length; i++)
				{
					Setting setting = settings[i];
					if (setting.getCallerID().equalsIgnoreCase(
							WebServiceConstants.ALL))
					{
						cricSel = setting;
						break;
					}
				}
				if (cricSel != null)
				{
					processDeactivateSubscriberRecords(task);
					String sms = feedStatus.getFeedOffSuccessSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_SUCCESS);
				}
				else
				{
					String sms = feedStatus.getFeedOffFailureSms();
					task.setObject(param_sms_for_user, sms);
					task.setObject(param_response, FEED_FAILURE);
				}

			}
		}
		else
		{
			task.setObject(param_response, DEACTIVATION_FAILURE);
		}
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDefaultSearch(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processDefaultSearch(Task task)
	{
		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null)
			moreRBTKeywords = Arrays.asList(moreKeywords.toLowerCase().split(","));

		String[] values = (String[]) task.getObject(param_sms);
		int songNo = -1;
		try
		{
			songNo = Integer.parseInt(values[0]);
		}
		catch (Exception e)
		{
			songNo = -1;
		}
		int maxSearchResultsToSend = 4;
		String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
		if (maxResults != null)
		{
			try
			{
				maxSearchResultsToSend = Integer.parseInt(maxResults);
			}
			catch (Exception e)
			{
				maxSearchResultsToSend = 4;
			}
		}
		if (songNo > 0 && songNo <= maxSearchResultsToSend)
		{
			setRequest(task, songNo);// check activation info
		}
		else if (songNo > maxSearchResultsToSend)
		{
			// already covered > should not come in this if condition
		}
		else if (moreRBTKeywords.contains(values[0].toLowerCase()))
		{
			moreRequest(task);
		}
		else
		{
			String searchString = " ";
			String searchOn = "song";
			HashMap<String, String> searchOnMap = getSearchOnMap();
			if (values.length > 1 && searchOnMap.containsKey(values[0]))
			{
				searchOn = searchOnMap.get(values[0]);
				for (int k = 1; k < values.length; k++)
				{
					searchString = searchString.trim() + " "
							+ values[k];
				}
			}
			else
			{
				for (int k = 0; k < values.length; k++)
				{
					searchString = searchString.trim() + " " + values[k];
				}
			}

			searchRequest(task, searchString.trim().toLowerCase(), searchOn);
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	private void setRequest(Task task, int songNo)
	{
		Subscriber subscriber = (Subscriber) (task.getObject(param_subscriber));
		String status = subscriber.getStatus();
		boolean invalid = false;
		String activationResult = "";
		
		logger.info("Setting request for selection: after getting response to Default search");
		
		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null
				&& activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| activateAfterSearch)
		{
			ViralData[] viralDataArr = getViraldata(
					subscriber.getSubscriberID(), null, "SEARCH");
			if (viralDataArr == null || viralDataArr.length == 0)
			{
				task.setObject(param_response, TEMPORARY_OVERRIDE_FAILURE);
			}
			else
			{
				if (!status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
				{
					// Can use processActivation here
					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
							subscriber.getSubscriberID());
					subscriptionRequest.setMode("SMS");
					subscriptionRequest.setCircleID(subscriber.getCircleID());
					subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
					subscriber = rbtClient
							.activateSubscriber(subscriptionRequest);
					activationResult = "activated";
				}
				ViralData data = viralDataArr[0];
				String clipIDs = data.getClipID();
				StringTokenizer stk = new StringTokenizer(clipIDs, ",");
				for (int i = 1; i < songNo; i++)
				{
					if (stk.hasMoreTokens())
						stk.nextToken();
					else
					{
						invalid = true;
						break;
					}
				}
				if (!invalid && stk.hasMoreTokens())
				{
					try
					{
						int clipId = Integer.parseInt(stk.nextToken());
						Clip reqClip = getClipById(String.valueOf(clipId),
								subscriber.getLanguage());
						if (reqClip != null)
						{
							String callerId = null;
							if (!isExistingSelection(
									subscriber.getSubscriberID(),
									reqClip.getClipId()))
							{
								// Can use processSelection here
								SelectionRequest selectionRequest = new SelectionRequest(
										subscriber.getSubscriberID(),
										subscriber.isPrepaid(), ""
												+ smsCategoryID,
										reqClip.getClipId() + "", null, null);
								selectionRequest.setMode("SMS");
								selectionRequest.setCircleID(subscriber
										.getCircleID());
								selectionRequest.setInLoop(true);
								Library lib = rbtClient.addSubscriberSelection(
										selectionRequest).getLibrary();
								Cos cos = getCos(subscriber);
								task.setObject(param_cosid, cos.getCosID());
								task.setObject(param_response,
										REQUEST_RBT_SMS2_SUCCESS_SUB_COS);
								if (activationResult.equals("activated"))
									task.setObject(param_response,
											REQUEST_RBT_SMS2_SUCCESS_NONSUB_COS);
								else if (!cos.isDefault()
										&& lib.getTotalDownloads() <= cos
												.getNoOfFreeSongs())
									task.setObject(param_response,
											REQUEST_RBT_SMS2_SUCCESS_SUB_FREE_COS);
							}
							else
							{
								task.setObject(param_clipName,
										reqClip.getClipName());
								task.setObject(param_callerid, callerId);
								task.setObject(param_response, SETTING_EXISTS);
							}
							removeViraldata(subscriber.getSubscriberID(), null,
									"SEARCH");
						}
						else
						{
							task.setObject(param_response, TECHNICAL_FAILURE);
						}
					}
					catch (Exception e)
					{
						task.setObject(param_response, TECHNICAL_FAILURE);
						logger.error(e);
					}
				}
			}
		}
		else
		{
			task.setObject(param_response, TEMPORARY_OVERRIDE_FAILURE);
		}
	}
	
	private void moreRequest(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		logger.info("more request : after getting response to Default search");
		boolean isSubscribed = false;
		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			isSubscribed = true;
		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null
				&& activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (isSubscribed || activateAfterSearch)
		{
			ViralData[] viralDataArr = getViraldata(subID, null, "SEARCH");
			if (viralDataArr == null || viralDataArr.length == 0)
			{
				task.setObject(param_response, MORE_RBT_SMS1_FAILURE);
			}
			else
			{
				ViralData data = viralDataArr[0];
				String clipIDs = data.getClipID();
				StringTokenizer stk = new StringTokenizer(clipIDs, ",");
				int maxSearchResultsToSend = 4;
				String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
				if (maxResults != null)
				{
					try
					{
						maxSearchResultsToSend = Integer.parseInt(maxResults);
					}
					catch (Exception e)
					{
						maxSearchResultsToSend = 4;
					}
				}
				if (stk.countTokens() <= maxSearchResultsToSend)
				{
					task.setObject(param_response, REQUEST_RBT_SMS2_FAILURE);
				}
				else
				{
					String clipId = null;
					for (int clipCounter = 0; clipCounter < maxSearchResultsToSend; clipCounter++)
					{
						clipId = stk.nextToken();
					}
					int clipIDLenght = clipId != null ? clipId.length() : 0;
					String finalClipIds = clipIDs.substring(clipIDs
							.indexOf(clipId)
							+ clipIDLenght + 1, clipIDs.length());

					StringTokenizer newStk = new StringTokenizer(finalClipIds,
							",");
					int finalCounter = 1;
					String finalMatches = "";
					boolean moreFlag = false;
					while (newStk.hasMoreTokens()
							&& finalCounter <= maxSearchResultsToSend)
					{
						String token = newStk.nextToken();
						Clip clip = getClipById(token.trim(),
								subscriber.getLanguage());
						String clipName = clip.getClipName();
						finalMatches += "" + finalCounter + ". " + clipName;
						boolean addMovieName = false;
						String addMovieNameInSms = getSMSParameter(ADD_MOVIE_NAME_IN_SMS);
						if (addMovieNameInSms != null
								&& addMovieNameInSms.equalsIgnoreCase("true"))
							addMovieName = true;
						if (addMovieName)
						{
							String movieName = clip.getAlbum();
							if (movieName != null
									&& !movieName.trim().equalsIgnoreCase(""))
								finalMatches += "/" + movieName.trim();
						}
						boolean addPriceAndValidity = false;
						String addPriceAndValidityStr = getSMSParameter(ADD_PRICE_AND_VALIDITY);
						if (addPriceAndValidityStr != null
								&& addPriceAndValidityStr
										.equalsIgnoreCase("true"))
							addPriceAndValidity = true;
						if (addPriceAndValidity)
						{
							String[] priceNvalidity = getPriceAndValidity(clip
									.getClassType());
							if (priceNvalidity != null
									&& priceNvalidity[0] != null
									&& priceNvalidity[1] != null)
								finalMatches += "(" + priceNvalidity[0] + "/"
										+ priceNvalidity[1] + ")";
						}
						if (newStk.hasMoreTokens())
							finalMatches += " ";
						finalCounter++;
					}
					if (newStk.hasMoreTokens())
					{
						moreFlag = true;
					}
					removeViraldata(subID, null, "SEARCH");
					addViraldata(subID, null, "SEARCH", finalClipIds, "SMS", 0,
							null);
					task.setObject(param_search_results, finalMatches);
					if (!isSubscribed)
					{
						task.setObject(param_response,
								REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS);
						if (moreFlag)
							task.setObject(param_response,
									REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS);
					}
					else
					{
						task.setObject(param_response,
								REQUEST_RBT_SMS1_SUCCESS_SUB_COS);
						Cos cos = getCos(subscriber);
						boolean isSubNull = false;
						if (status.equals(WebServiceConstants.NEW_USER)
								|| status.equals(WebServiceConstants.DEACTIVE))
							isSubNull = true;
						if (!isSubNull
								&& !cos.isDefault()
								&& subscriber.getTotalDownloads() < cos
										.getNoOfFreeSongs())
							task.setObject(param_response,
									REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS);

						if (moreFlag)
						{
							task.setObject(param_response,
									REQUEST_RBT_SMS3_SUCCESS_SUB_COS);
							if (!isSubNull
									&& !cos.isDefault()
									&& subscriber.getTotalDownloads() < cos
											.getNoOfFreeSongs())
								task.setObject(param_response,
										REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS);
						}
						boolean sendConfSMS = subscriber.getSubscriptionState()
								.equals(iRBTConstant.STATE_EVENT)
								&& subscriber.getTotalDownloads() == cos
										.getNoOfFreeSongs();
						if (sendConfSMS)
						{
							if (subscriber.isPrepaid())
								task.setObject(param_another_response,
										CONFIRM_BULK_ACTIVATION_PREP_SMS);
							else
								task.setObject(param_another_response,
										CONFIRM_BULK_ACTIVATION_POST_SMS);
						}
					}
				}
			}
		}
		else
		{
			task.setObject(param_response, HELP);
			if (!isSubscribed && !activateAfterSearch)
				task.setObject(param_response, ACTIVATION_FAILURE);
		}
	}

	private void searchRequest(Task task, String searchString, String searchOn)
	{
		searchString = replaceSpecialChars(searchString);

		logger.info("RBT:: search string = " + searchString
				+ " and search on = " + searchOn);
		boolean performSearch = true;

		if (searchString == null || searchString.equals(""))
			performSearch = false;

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		boolean activated = false;
		boolean isSubscribed = status
				.equalsIgnoreCase(WebServiceConstants.ACTIVE);

		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null
				&& activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (!isSubscribed && !activateAfterSearch)
		{
			processActivation(task);
			subscriber = (Subscriber) task.getObject(param_subscriber);
			status = subscriber.getStatus();
			if (!(status.equalsIgnoreCase(WebServiceConstants.DEACTIVE) || status
					.equalsIgnoreCase(WebServiceConstants.NEW_USER)))
				activated = true;
		}
		String[] results = null;
		if (performSearch)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(searchOn, searchString);
			map.put("SUBSCRIBER_ID", subscriber.getSubscriberID());
			ArrayList<LuceneClip> list = luceneIndexer.searchQuery(map, 0,
					maxSearchResultsToFetch);
			if (list == null || list.size() == 0)
			{
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);
				return;
			}
			ArrayList<String> al = new ArrayList<String>();
			for (int i = 0; i < list.size(); i++)
			{
				LuceneClip clip = list.get(i);
				if (clip.getClipEndTime().after(new Date()))
					al.add(String.valueOf(list.get(i).getClipId()));
			}
			if (al.size() > 0)
				results = al.toArray(new String[0]);
		}
		processSearchClips(task, results, activated, null);
	}

	private void processSearchClips(Task task, String[] results,
			boolean activated, String categoryAlias)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		boolean moreFlag = false;
		boolean isSubscribed = status
				.equalsIgnoreCase(WebServiceConstants.ACTIVE);

		if (results == null || results.length == 0)
		{
			if (categoryAlias != null)
			{
				task.setObject(param_response, SMS_ALIAS_CATEGORY_NO_CLIP);
				task.setObject(param_categoryAlias, categoryAlias);// replace %S
			}
			else
			{
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);
			}

			removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");
		}
		else
		{
			ArrayList<String> list = new ArrayList<String>();
			Cos cos = getCos(subscriber);
			if (cos != null)
				task.setObject(param_cosid, cos.getCosID());
			
			for (int c = 0; c < results.length; c++)
			{
				if (!list.contains(results[c]))
				{
					Clip clip = getClipById(results[c],
							subscriber.getLanguage());
					if (cos != null)
					{
						if (!cos.isDefault()
								&& cos.getChargeClass() != null
								&& cos.getChargeClass().indexOf(
										clip.getClassType()) > -1)
							list.add(results[c]);
						else if (cos.isDefault()
								|| (!cos.isDefault() && cos.getChargeClass() == null))
							list.add(results[c]);
					}
					else
						list.add(results[c]);
				}
			}
			results = list.toArray(new String[0]);
			String match = "";
			String clipIDs = "";
			for (int hit = 0; results != null && hit < results.length; hit++)
			{
				try
				{
					String id = results[hit].trim();
					Clip clip = getClipById(id.trim(), subscriber.getLanguage());
					if (clipIDs.equalsIgnoreCase(""))
						clipIDs = "" + id.trim();
					else
						clipIDs = clipIDs + "," + id.trim();
					int maxSearchResultsToSend = 4;
					String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
					if (maxResults != null)
					{
						try
						{
							maxSearchResultsToSend = Integer
									.parseInt(maxResults);
						}
						catch (Exception e)
						{
							maxSearchResultsToSend = 4;
						}
					}
					if (hit + 1 <= maxSearchResultsToSend)
					{
						match = match + (hit + 1) + "." + clip.getClipName();
						boolean addMovieName = false;
						String addMovieNameInSms = getSMSParameter(ADD_MOVIE_NAME_IN_SMS);
						if (addMovieNameInSms != null
								&& addMovieNameInSms.equalsIgnoreCase("true"))
							addMovieName = true;
						if (addMovieName)
						{
							String movieName = clip.getAlbum();
							if (movieName != null
									&& !movieName.trim().equalsIgnoreCase(""))
								match += "/" + movieName.trim();
						}
						boolean addPriceAndValidity = false;
						String addPriceAndValidityStr = getSMSParameter(ADD_PRICE_AND_VALIDITY);
						if (addPriceAndValidityStr != null
								&& addPriceAndValidityStr
										.equalsIgnoreCase("true"))
							addPriceAndValidity = true;
						if (addPriceAndValidity)
						{
							String[] priceNvalidity = getPriceAndValidity(clip
									.getClassType());
							if (priceNvalidity != null
									&& priceNvalidity[0] != null
									&& priceNvalidity[1] != null)
								match += "(" + priceNvalidity[0] + "/"
										+ priceNvalidity[1] + ")";
						}
						match += " ";
					}
					else
					{
						moreFlag = true;
					}
				}
				catch (Exception e)
				{
					logger.info("RBT::ERROR in " + results[hit]
							+ " and exception is " + e);
				}
			}
			// checking if we got any clips with charge classes
			if (clipIDs.equalsIgnoreCase(""))
			{
				if (activated)
					task.setObject(param_response, REQUEST_RBT_SMS3_FAILURE);
				else
					task.setObject(param_response, REQUEST_RBT_SMS1_FAILURE);

				removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");
			}
			else
			{
				removeViraldata(subscriber.getSubscriberID(), null, "SEARCH");// check
																				// this
				addViraldata(subscriber.getSubscriberID(), null, "SEARCH",
						clipIDs, "SMS", 0, null);
				if (!isSubscribed && !activated)
				{
					task.setObject(param_response,
							REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS);
					if (moreFlag)
						task.setObject(param_response,
								REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS);
				}
				else
				{
					task.setObject(param_response,
							REQUEST_RBT_SMS1_SUCCESS_SUB_COS);
					boolean isSubNull = false;
					if (status.equals(WebServiceConstants.NEW_USER)
							|| status.equals(WebServiceConstants.DEACTIVE))
						isSubNull = true;
					if (!isSubNull && cos != null
							&& !cos.isDefault()
							&& subscriber.getTotalDownloads() < cos
									.getNoOfFreeSongs())
					{
						task.setObject(param_response,
								REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS);
					}

					if (moreFlag)
					{
						task.setObject(param_response,
								REQUEST_RBT_SMS3_SUCCESS_SUB_COS);
						if (!isSubNull && cos != null
								&& !cos.isDefault()
								&& subscriber.getTotalDownloads() < cos
										.getNoOfFreeSongs())
						{
							task.setObject(param_response,
									REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS);
						}
					}

					boolean sendConfSMS = subscriber.getSubscriptionState().equals(iRBTConstant.STATE_EVENT)
							&& cos != null && subscriber.getTotalDownloads() == cos.getNoOfFreeSongs();
					if (sendConfSMS)
					{
						if (subscriber.isPrepaid())
						{
							task.setObject(param_another_response,
									CONFIRM_BULK_ACTIVATION_PREP_SMS);
						}
						else
						{
							task.setObject(param_another_response,
									CONFIRM_BULK_ACTIVATION_POST_SMS);
						}
					}
				}
				task.setObject(param_search_results, match.trim());
			}
		}
	}

	@Override
	public void processCategoryByPromoID(Task task)
	{
		Clip clip = (Clip) task.getObject(param_clip);
		task.setObject(param_clipid, String.valueOf(clip.getClipId()));
		String[] values = (String[]) task.getObject(param_sms);
		String callerID = null;
		if (values.length > 1)
		{
			if (checkAct(values[1]))
			{
				if (values.length > 2)
					callerID = values[2];
				task.setObject(param_callerid, callerID);
			}
			else
			{
				callerID = values[1];
				task.setObject(param_callerid, callerID);
				processActivation(task);
			}
		}
		processSelection(task);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processClipByAlias(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processClipByAlias(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String[] values = (String[]) task.getObject(param_sms);
		
		Clip clip = (Clip)task.getObject(param_clip);
		if (clip == null)
		{
			// Expecting clip object will be stored in context by checkClipAlias() method, otherwise getting the clip object.  
			String smsAlias = getFiveDigitClipID(task, values);
			clip = rbtCacheManager.getClipBySMSAlias(smsAlias);
		}

		if (clip == null
				|| clip.getSmsStartTime() == null
				|| clip.getClipEndTime() == null
				|| (clip.getSmsStartTime() != null && clip.getSmsStartTime()
						.after(new Date())))
		{
			task.setObject(param_response, SMS_ALIAS_CLIP_INVALID);
			task.setObject(param_clipAlias, values[0]);// replace %S with clipAlias
		}
		else if (clip.getClipEndTime().before(new Date()))
		{
			task.setObject(param_response, SMS_ALIAS_CLIP_EXPIRED);
			task.setObject(param_clipAlias, values[0]);// replace %S with clipAlias
		}
		else
		{
			// clip is valid
			String callerID = null;
			if (values.length > 1)
			{
				try
				{
					String temp = values[1];
					Long.parseLong(temp);
					if (temp.length() >= 7 && temp.length() <= 15)
						callerID = temp;
				}
				catch (Exception e)
				{

				}
			}

			boolean activated = false;
			boolean isSubscribed = status.equalsIgnoreCase(WebServiceConstants.ACTIVE);
			if (!isSubscribed)
			{
				processActivation(task);
				subscriber = (Subscriber) task.getObject(param_subscriber);
				status = subscriber.getStatus();
				if (!(status.equalsIgnoreCase(WebServiceConstants.DEACTIVE) || status
						.equalsIgnoreCase(WebServiceConstants.NEW_USER)))
					activated = true;
			}
			else
				activated = true;
			
			if (activated)
			{
				task.setObject(param_clipid, String.valueOf(clip.getClipId()));
				processSelection(task);

				if (isSubscribed)
					task.setObject(param_response, SMS_ALIAS_ONLY_CLIP_SUCCESS);
				else
					task.setObject(param_response, SMS_ALIAS_CLIP_SUCCESS);

				task.setObject(param_clipName, clip.getClipName()); // %S
				task.setObject(param_callerid, callerID); // %C
			}
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	@Override
	public void processCategoryByAlias(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String[] values = (String[]) task.getObject(param_sms);
		Category category = (Category) task.getObject(param_category);
		boolean activated = false;
		boolean isSubscribed = status
				.equalsIgnoreCase(WebServiceConstants.ACTIVE);

		boolean activateAfterSearch = true;
		String activateAfterSearchStr = getSMSParameter(ACTIVATE_AFTER_SEARCH);
		if (activateAfterSearchStr != null
				&& activateAfterSearchStr.equalsIgnoreCase("false"))
			activateAfterSearch = false;
		if (!isSubscribed && !activateAfterSearch)
		{
			processActivation(task);
			subscriber = (Subscriber) task.getObject(param_subscriber);
			status = subscriber.getStatus();
			activated = status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
					|| status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING);
		}
		Clip[] clips = getClipsByCatId(category.getCategoryId(),
				subscriber.getLanguage());

		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; clips != null && i < clips.length; i++)
		{
			al.add(String.valueOf(clips[i].getClipId()));
		}
		String[] results = null;
		if (al.size() > 0)
			results = al.toArray(new String[0]);

		processSearchClips(task, results, activated, values[0]);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processConfirmCopyRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processConfirmCopyRequest(Task task)
	{
		String subscriberID = task.getString(param_subscriberID);
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_CHANGE_TYPE, "COPYCONFIRMED");
		task.setObject(param_WAITTIME,
				param(GATHERER, WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		
		String response = updateViraldata(task);
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			task.setObject(param_response, SUCCESS);
		else
			task.setObject(param_response, FAILURE);

		task.setObject(param_subscriberID, subscriberID);
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processCancelOptInCopy(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processCancelOptInCopy(Task task)
	{
		String subscriberID = task.getString(param_subscriberID);
		task.setObject(param_CALLER_ID, subscriberID);
		task.setObject(param_subscriberID, null);
		task.setObject(param_SMSTYPE, "COPYCONFPENDING");
		task.setObject(param_WAITTIME,
				param(GATHERER, WAIT_TIME_DOUBLE_CONFIRMATION, 30));
		
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
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processRemoveProfile(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processRemoveProfile(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subID = task.getString(param_subscriberID);
	
		SelectionRequest selectionRequest = new SelectionRequest(subID);
		selectionRequest.setStatus(99);
		
		rbtClient.deleteSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();
		if (response != null
				&& response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
		{
			task.setObject(param_response, SUCCESS);
			// TEMPORARY_OVERRIDE_REMOVAL_SUCCESS_TEXT
		}
		else
		{
			String status = subscriber.getStatus();
			if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
			{
				task.setObject(param_response, FAILURE);
				// TEMPORARY_OVERRIDE_REMOVAL_FAILURE_TEXT
			}
			else
			{
				task.setObject(param_response, ERROR);
				// user is not subscribed
			}
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
			
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processHelpRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processHelpRequest(Task task)
	{
		task.setObject(param_response, HELP);

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processCategorySearch(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processCategorySearch(Task task)
	{
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subId = subscriber.getSubscriberID();
		
		Category category = (Category) task.getObject(param_category);
		if (category == null)
		{
			task.setObject(param_response, INVALID);
			return;
		}

		Date setTime = Calendar.getInstance().getTime();
		String[] values = (String[]) task.getObject(param_sms);
		String smsText = values[0];
		if (smsText.length() > 4 && smsText.contains("MORE"))
		{
			smsText = smsText.substring(0, smsText.indexOf("MORE"));

			if (isParentCatForCatSearch(task))
			{
				if (isNewSession(subId))
				{
					String strCatId = String.valueOf(category.getCategoryId());
					String newClipId = strCatId + "," + maxSearchResultsToSendForCatSearch + ",-1,-1";
					sendCatList(task, category, 0, setTime, newClipId, true);
					return;
				}
				else
				{
					if (isCatCountNotInitialized(subId))
					{
						String strCatId = String.valueOf(category.getCategoryId());
						if (isValidParentCat(category, subscriber, false))
						{
							ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch + ","
									+ sessionvariables.get(2) + ","
									+ sessionvariables.get(3);
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}
						else
						{
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch
									+ ",-1,-1";
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}

					}
					else if (isValidParentCat(category, subscriber, true))
					{
						ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
						try
						{
							String strCatId = String.valueOf(category
									.getCategoryId());
							int catCount = new Integer(sessionvariables.get(1))
									.intValue();
							catCount = catCount
									+ maxSearchResultsToSendForCatSearch;
							String newClipId = strCatId + "," + catCount + ","
									+ sessionvariables.get(2) + ","
									+ sessionvariables.get(3);

							sendCatList(task, category, catCount
									- maxSearchResultsToSendForCatSearch, null,
									newClipId, false);
							return;
						}
						catch (NumberFormatException e)
						{
							logger.error(e.getMessage(), e);
							task.setObject(param_response, INVALID);
							return;
						}
					}
					else
					{
						String strCatId = String.valueOf(category
								.getCategoryId());
						String newClipId = strCatId + ","
								+ maxSearchResultsToSendForCatSearch + ",-1,-1";
						sendCatList(task, category, 0, null, newClipId, true);
						return;
					}
				}
			}
			else
			{
				// its a child category
				Category parentCategory = (Category) task
						.getObject(param_parent_category);
				if (parentCategory == null)
				{
					task.setObject(param_response, INVALID);
					return;
				}
				if (isNewSession(subId))
				{
					String strCatId = String.valueOf(category.getCategoryId());
					String parentId = String.valueOf(parentCategory
							.getCategoryId());
					String newClipId = parentId + ",-1," + strCatId + ","
							+ maxSearchResultsToSendForCatSearch;
					sendClipsList(task, category, 0, setTime, newClipId);
					return;
				}
				else
				{

					if (isExistingParentCat(parentCategory, subId))
					{
						if (isExistingChildCat(category, subId))
						{
							try
							{
								ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
								int clipCount = new Integer(
										sessionvariables.get(3)).intValue();
								clipCount = clipCount
										+ maxSearchResultsToSendForCatSearch;
								String newClipId = sessionvariables.get(0)
										+ ","
										+ sessionvariables.get(1) + ","
										+ sessionvariables.get(2)
										+ "," + clipCount;

								sendClipsList(task, category, clipCount
										- maxSearchResultsToSendForCatSearch,
										null, newClipId);
								return;
							}
							catch (NumberFormatException e)
							{
								e.printStackTrace();
								task.setObject(param_response, INVALID);
								return;
							}
						}
						else
						{
							ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
							String newClipId = sessionvariables.get(0)
									+ "," + sessionvariables.get(1)
									+ "," + category.getCategoryId() + ","
									+ maxSearchResultsToSendForCatSearch;

							// return first 3 clips of this new subCat
							sendClipsList(task, category, 0, null, newClipId);
							return;
						}
					}
					else
					{
						task.setObject(param_response, INVALID);
						return;
					}

				}

			}
		}
		else if (smsText.length() > 0 && smsText.indexOf("MORE") == -1)
		{

			if (isParentCatForCatSearch(task))
			{
				if (isNewSession(subId))
				{
					String strCatId = String.valueOf(category.getCategoryId());
					String newClipId = strCatId + ","
							+ maxSearchResultsToSendForCatSearch + ",-1,-1";
					sendCatList(task, category, 0, setTime, newClipId, true);
					return;
				}
				else
				{
					if (isCatCountNotInitialized(subId))
					{
						String strCatId = String.valueOf(category
								.getCategoryId());
						if (isValidParentCat(category, subscriber, false))
						{
							ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch + ","
									+ sessionvariables.get(2) + ","
									+ sessionvariables.get(3);
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}
						else
						{
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch
									+ ",-1,-1";
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}
					}
					else
					{
						if (isValidParentCat(category, subscriber, false))
						{
							ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
							String strCatId = String.valueOf(category
									.getCategoryId());
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch + ","
									+ sessionvariables.get(2) + ","
									+ sessionvariables.get(3);
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}
						else
						{
							String strCatId = String.valueOf(category
									.getCategoryId());
							String newClipId = strCatId + ","
									+ maxSearchResultsToSendForCatSearch
									+ ",-1,-1";
							sendCatList(task, category, 0, null, newClipId,
									true);
							return;
						}
					}
				}
			}
			else
			{
				// its a child category case
				Category parentCategory = (Category) task
						.getObject(param_parent_category);
				if (parentCategory == null)
				{
					task.setObject(param_response, INVALID);
					return;
				}
				if (isNewSession(subId))
				{
					String strCatId = String.valueOf(category.getCategoryId());
					String parentId = String.valueOf(parentCategory
							.getCategoryId());
					String newClipId = parentId + ",-1," + strCatId + ","
							+ maxSearchResultsToSendForCatSearch;
					sendClipsList(task, category, 0, setTime, newClipId);
					return;
				}
				else
				{
					if (isExistingParentCat(parentCategory, subId))
					{
						ArrayList<String> sessionvariables = tokenizeSessionVariables(subId);
						String newClipId = sessionvariables.get(0) + ","
								+ sessionvariables.get(1)
								+ "," + category.getCategoryId() + ","
								+ maxSearchResultsToSendForCatSearch;
						sendClipsList(task, category, 0, null, newClipId);
						return;
					}
					else
					{
						task.setObject(param_response, INVALID);
						return;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processRetailerRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processRetailerRequest(Task task)
	{
		String response = "FAILURE";
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, true);

		String[] values = (String[]) task.getObject(param_sms);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		Retailer retailer = (Retailer) task.getObject(param_retailer);

		boolean activated = true;
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
		{
			activated = false;
		}

		// the following code will check if we got act request from RET2 again
		boolean isActivationRequested = false;
		if (getSevenDigitSmsPrefix(values[0]) != null)
			isActivationRequested = true;

		if (activated
				&& (retailer.getType().equalsIgnoreCase(
						iRBTConstant.RETAILER_TYPE_1) || isActivationRequested))
		{
			// Retailer 1 sent request for already active user || we got act
			// request from RET2 again
			task.setObject(param_response, ALREADY_ACTIVE);
			task.setObject(param_retailer_response, ALREADY_ACTIVE);
		}
		else
		{
			if (values.length == 1)
			{
				// only activation request
				processActivation(task);
				response = task.getString(param_response);
				if (response.equalsIgnoreCase(SUCCESS))
				{
					// already active case is handled earlier
					task.setObject(param_response, SUBSCRIPTION);
					task.setObject(param_retailer_response, SUBSCRIPTION);
				}
			}
			else if (values.length == 2 && getSevenDigitSmsPrefix(values[0]) != null)
			{
				// retailer sent song and subID
				uniqueCodeCheck(task);
			}
			else
			{
				ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
				applicationDetailsRequest.setPack(values[0]);
				applicationDetailsRequest.setCircleID(subscriber.getCircleID());
				applicationDetailsRequest.setIsPrepaid(subscriber.isPrepaid());
				Cos cos = rbtClient.getCos(applicationDetailsRequest);
				if (cos == null)
				{
					task.setObject(param_retailer_response,
							INVALID_RET_PACK_CODE);
					task.setObject(param_send_sms_to_user, false);
					return;
				}

				task.setObject(param_cosid, cos.getCosID());

				if (getSevenDigitSmsPrefix(values[1]) != null)
				{
					// length is 3 -> retailer sent pack, song and subID
					task.setObject(param_sms, new String[] { values[1] });
					uniqueCodeCheck(task);
				}
				else
				{
					// retailer sent pack and subID
					task.setObject(param_retPack, String.valueOf(values[0]));
					processActivation(task);
					response = task.getString(param_response);
					if (response.equals(SUCCESS))
					{
						task.setObject(param_response,
								SUBSCRIPTION_WITH_PACK_SUCCESS);
						task.setObject(param_retailer_response,
								SUBSCRIPTION_WITH_PACK_SUCCESS);
					}
				}
			}

		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processRetailerSearch(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processRetailerSearch(Task task)
	{
		String[] values = (String[]) task.getObject(param_sms);
		String searchString = "";
		String match = "";
		boolean gotResults = false;
		String searchType = "song";
		HashMap<String, String> searchOnMap = getSearchOnMap();
		if (searchOnMap.containsKey(values[0]))
		{
			searchType = searchOnMap.get(values[0]);
			for (int i = 1; i < values.length; i++)
				searchString = searchString.trim() + " " + values[i];
		}
		else
			for (int i = 0; i < values.length; i++)
				searchString = searchString.trim() + " " + values[i];

		ArrayList<LuceneClip> results = null;
		if (searchString.trim().length() > 0)
		{
		
			String subscriberID = task.getString(param_subscriberID);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(searchType, searchString);
			map.put("SUBSCRIBER_ID", subscriberID);
			results = luceneIndexer
					.searchQuery(map, 0, maxSearchResultsToFetch);
		}

		if (results != null && results.size() > 0)
		{
			gotResults = true;
			// no of results will always be less than or equals to
			// maxSearchResultsToSend
			for (int i = 0; i < results.size(); i++)
			{
				LuceneClip clip = results.get(i);
				if (clip != null)
				{
					String songName = clip.getClipName();
					if (clip.getClipPromoId() != null)
					{
						match = match + songName + "-" + clip.getClipPromoId()
								+ " ";
					}
					else
					{
						match = match + songName + " ";
					}
				}
			}
		}
		if (!gotResults)
		{
			task.setObject(param_retailer_response, NO_RESULTS);
		}
		else
		{
			task.setObject(param_retailer_response, SUCCESS);
			task.setObject(param_search_results, match);
		}
		task.setObject(param_send_sms_to_user, false);
		task.setObject(param_send_sms_to_retailer, true);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processTrial(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processTrial(Task task)
	{
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		Cos cos = getCos(subscriber);
		String promoClips = cos.getPromoClips();
		task.setObject(param_cosid, Integer.parseInt(subscriber.getCosID()));
		if (cos.isDefault()
				|| promoClips == null
				|| (subscriber.getTotalDownloads() >= cos.getNoOfFreeSongs()))
		{
			task.setObject(param_response, RBT_INVALID_SMS_COS);
			return;
		}

		String trialReply = parameterCacheManager.getParameter(SMS, TRIAL_REPLY_KEYWORD, "T1,T2,T3,T4").getValue();
		StringBuilder smsTextInitial = new StringBuilder();
	
		String[] clipTokens = promoClips.split(",");
		String[] trialReplyTokens = trialReply.split(",");
		for (int i = 0; i < clipTokens.length; i++)
		{
			String thisClip = clipTokens[i];
			Clip clip = rbtCacheManager.getClip(thisClip);
			String keyword = trialReplyTokens[i];
			smsTextInitial.append(keyword.toUpperCase()).append(".");
			smsTextInitial.append(clip.getClipName().trim());
		}
		
		task.setObject(param_search_results, smsTextInitial.toString());
		task.setObject(param_response, RBT_SUCCESS_SMS_COS);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processTrialReply(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processTrialReply(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String[] values = (String[]) task.getObject(param_sms);
		
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
		
		Cos cos = getCos(subscriber);
		task.setObject(param_cosid, Integer.parseInt(subscriber.getCosID()));

		String songNoStr = values[1];
		int songNo = -1;
		try
		{
			songNo = Integer.parseInt(songNoStr);
		}
		catch (Exception e)
		{
			task.setObject(param_response, RBT_INVALID_SMS_COS);
			return;
		}

		String trialClipsSMS = cos.getPromoClips();

		boolean isSubNull = false;
		if (status.equals(WebServiceConstants.NEW_USER)
				|| status.equals(WebServiceConstants.DEACTIVE))
			isSubNull = true;

		if (!cos.isDefault()
				&& trialClipsSMS != null
				&& (isSubNull || (!isSubNull && subscriber.getTotalDownloads() < cos
						.getNoOfFreeSongs())))
		{
			boolean activated = false;

			StringTokenizer st = new StringTokenizer(trialClipsSMS, ",");
			boolean invalid = false;
			for (int count = 1; count < songNo; count++)
			{
				if (st.hasMoreTokens())
					st.nextToken();
				else
				{
					invalid = true;
					break;
				}
			}
			if (!invalid && st.hasMoreTokens())
			{
				try
				{
					int clipId = Integer.parseInt(st.nextToken());
					Clip clip = rbtCacheManager.getClip(clipId);
					task.setObject(param_clipid,
							String.valueOf(clip.getClipId()));
					task.setObject(param_clipName, clip.getClipName());
					if (isSubNull)
					{
						processActivation(task);
						String response = task.getString(param_response);
						if (response.equals(SUCCESS)
								|| response.equals(ALREADY_ACTIVE))
							activated = true;
						else
							task.setObject(param_response, ACTIVATION_FAILURE);
					}
					else
						activated = true;

					if (activated)
					{
						processSelection(task);
						String response = task.getString(param_response);
						if (response.equals(ALREADY_EXISTS))
							return;
						task.setObject(param_response,
								REQUEST_RBT_SMS2_SUCCESS_SUB_FREE_COS);

					}
				}
				catch (Exception e)
				{
					task.setObject(param_response, TECHNICAL_FAILURE);
				}
			}
		}
		else
		{
			task.setObject(param_response, RBT_INVALID_SMS_COS);
		}
	}
		
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processLoop(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processLoop(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);

		if (status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
		{
			task.setObject(param_response, INVALID);
			return;
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriber.getSubscriberID());
		Downloads downloads = rbtClient.getDownloads(rbtDetailsRequest);
		Download[] downloadsArr = downloads.getDownloads();
		if (downloadsArr == null || downloadsArr.length == 0)
		{
			task.setObject(param_response, NO_DOWNLOADS);
			return;
		}

		SelectionRequest selectionRequest = new SelectionRequest(
				subscriber.getSubscriberID());
		rbtClient.shuffleDownloads(selectionRequest);
		String response = selectionRequest.getResponse();
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			task.setObject(param_response, SUCCESS);
		else
			task.setObject(param_response, FAILURE);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDelete(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processDelete(Task task)
	{
		String[] values = (String[]) task.getObject(param_sms);
		if (values.length == 1)
			getDeleteDownloads(task);
		else if (checkMoreDelete(values[1]))
			getMoreDeleteDownloads(task);
		else
			deleteDownload(task);
		
		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processOBDRequest(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public void processOBDRequest(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.COPY_PENDING))
		{
			task.setObject(param_obd_response,
					"SUBSCRIBER_EXPRESS_COPY_REQUEST_PENDING");
			return;
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING))
		{
			task.setObject(param_obd_response, "SUBSCRIBER_RENEWAL_PENDING");
			return;
		}

		task.setObject(param_send_sms_to_user, true);
		task.setObject(param_send_sms_to_retailer, false);

		setRequestForOBD(task);
	}
	
	private void setRequestForOBD(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		String promoId = task.getString(param_promoID);

		Clip clip = rbtCacheManager.getClipByPromoId(promoId);
		if (clip == null)
		{
			task.setObject(param_obd_response, INVALID_TONE_CODE);
			return;
		}

		task.setObject(param_actInfo, "SMS-OBD");
		task.setObject(param_actby, "SMS-OBD");
		task.setObject(param_mod_channel, "SMS-OBD");
		if (!status.equals(WebServiceConstants.ACTIVE))
		{
			processActivation(task);
			String response = task.getString(param_response);
			if (!response.equals(SUCCESS))
			{
				logger.info("failed to activate subscriber " + subID);
				task.setObject(param_obd_response, ACTIVATION_FAILURE);
				task.setObject(param_send_sms_to_user, false);
				return;
			}
		}

		subscriber = (Subscriber) task.getObject(param_subscriber);
		task.setObject(param_clipid, String.valueOf(clip.getClipId()));
		processSelection(task);
		task.setObject(param_send_sms_to_user, false);
		String response = task.getString(param_response);
		if (response.equals(ALREADY_EXISTS))
		{
			logger.info("song " + promoId
					+ " already set for default for subscriber " + subID);
		}
		task.setObject(param_obd_response, SUCCESS);
		return;
	}
	
	private void uniqueCodeCheck(Task task)
	{
		String response = "FAILURE";

		String[] values = (String[]) task.getObject(param_sms);
		String isRetRequest = task.getString(param_isRetailerRequest);
		boolean retailerRequest = (isRetRequest != null && isRetRequest
				.equalsIgnoreCase("true"));

		Clip clip = null;
		String strFiveDigitUniqueCode = getFiveDigitClipID(task, values);

		if (strFiveDigitUniqueCode != null)
			clip = rbtCacheManager.getClipBySMSAlias(strFiveDigitUniqueCode);

		if (clip != null)
		{
			task.setObject(param_clipid, String.valueOf(clip.getClipId()));
			task.setObject(param_clipName, clip.getClipName());
			processActivation(task);
			response = task.getString(param_response);
			if (response.equals(SUCCESS) || response.equals(ALREADY_ACTIVE))
			{
				String actResponse = response;
				processSelection(task);
				response = task.getString(param_response);
				if (response.equals(SUCCESS))
				{
					if (actResponse.equals(ALREADY_ACTIVE))
					{
						task.setObject(param_response, SELECTION);
						if (retailerRequest)
							task.setObject(param_retailer_response, SELECTION);
					}
					else
					{
						task.setObject(param_response, SUCCESS);
						if (retailerRequest)
							task.setObject(param_retailer_response, SUCCESS);
					}
				}
				else if (response.equals(ALREADY_EXISTS))
				{
					task.setObject(param_response, SELECTION_EXISTS);
					if (retailerRequest)
						task.setObject(param_retailer_response,
								SELECTION_EXISTS);
				}
				else
				{
					task.setObject(param_response, ONLY_SUBSCRIPTION_SUCCESS);
					if (retailerRequest)
						task.setObject(param_retailer_response,
								ONLY_SUBSCRIPTION_SUCCESS);
				}
			}

		}
		else if (retailerRequest)
		{
			processActivation(task);
			response = task.getString(param_response);
			if (!response.equals(ALREADY_ACTIVE) && response.equals(SUCCESS))
			{
				task.setObject(param_response, ONLY_SUBSCRIPTION_SUCCESS);
				task.setObject(param_retailer_response,
						ONLY_SUBSCRIPTION_SUCCESS);
			}
			else if (response.equals(SUCCESS))
			{
				task.setObject(param_response, SELECTION_FAILURE);
				task.setObject(param_retailer_response, SELECTION_FAILURE);
			}
		}
	}
	
	private void getDeleteDownloads(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber.getStatus().equalsIgnoreCase(
				WebServiceConstants.DEACTIVE))
		{
			task.setObject(param_response, INVALID_SUB);
			return;
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriber.getSubscriberID());
		rbtDetailsRequest.setMmContext("SYNC_LIBRARY");
		Downloads downloads = rbtClient.getLibrary(rbtDetailsRequest)
				.getDownloads();
		Download[] downloadsArr = downloads.getDownloads();

		ArrayList<Download> songList = new ArrayList<Download>();
		if (downloadsArr != null && downloadsArr.length > 0)
		{
			for (Download download : downloadsArr)
			{
				if (download.getToneType().equalsIgnoreCase(WebServiceConstants.CLIP)
						&& !download.getDownloadStatus().equalsIgnoreCase(
								WebServiceConstants.DEACT_PENDING))
				{
					songList.add(download);
				}
			}
		}
		if (songList.size() == 0)
		{
			task.setObject(param_response, NO_DOWNLOADS);
			return;
		}

		downloadsArr = songList.toArray(new Download[0]);
		StringBuilder downloadsList = new StringBuilder();
		StringBuilder downloadsListForSMS = new StringBuilder();
		for (int i = 0; i < downloadsArr.length; i++)
		{
			int clipId = downloadsArr[i].getToneID();
			Clip clip = rbtCacheManager.getClip(clipId);
			if (i < maxDeleteResults)
			{
				downloadsListForSMS.append((i + 1) + "." + clip.getClipName() + " ");
			}
			downloadsList.append(clip.getClipPromoId() + ",");
		}

		addViraldata(subscriber.getSubscriberID(), null, "DELETE",
				downloadsList.toString(), "SMS", 0, null);
		
		task.setObject(param_response, SUCCESS1);
		task.setObject(param_downloads_list_for_sms, downloadsListForSMS
				.toString().trim());
		
		if (downloadsArr.length <= maxDeleteResults)
			task.setObject(param_another_response, SUCCESS3);
		else
			task.setObject(param_another_response, SUCCESS2);
	}
	
	private void getMoreDeleteDownloads(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		ViralData[] dataArr = getViraldata(subscriber.getSubscriberID(), null, "DELETE");
		if (dataArr == null || dataArr.length == 0)
		{
			task.setObject(param_response, MORE_INVALID);
			return;
		}

		String downloads = dataArr[dataArr.length - 1].getClipID();
		StringTokenizer stk = new StringTokenizer(downloads, ",");
		if (stk.countTokens() <= maxDeleteResults)
		{
			task.setObject(param_response, MORE_INVALID);
			return;
		}
		StringBuffer newDownloadList = new StringBuffer();
		StringBuffer downloadsListForSMS = new StringBuffer();
		for (int i = 0, smsCount = 0; stk.hasMoreTokens(); i++)
		{
			if (i < maxDeleteResults)
			{
				stk.nextToken();
				continue;
			}
			
			String promoID = stk.nextToken();
			Clip clip = rbtCacheManager.getClipByPromoId(promoID);

			if (smsCount < maxDeleteResults)
			{
				downloadsListForSMS.append((smsCount++ + 1) + "."
						+ clip.getClipName() + " ");
			}

			newDownloadList.append(promoID + ",");
		}
		addViraldata(subscriber.getSubscriberID(), null, "DELETE",
				newDownloadList.toString(), "SMS", 0, null);
		
		task.setObject(param_response, SUCCESS1);
		task.setObject(param_downloads_list_for_sms, downloadsListForSMS
				.toString().trim());
		
		StringTokenizer newStk = new StringTokenizer(newDownloadList.toString(), ",");
		if (newStk.countTokens() <= maxDeleteResults)
			task.setObject(param_another_response, SUCCESS3);
		else
			task.setObject(param_another_response, SUCCESS2);
	}

	private void deleteDownload(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String[] values = (String[]) task.getObject(param_sms);
		String deleteStr = values[1];
		try
		{
			int delNum = Integer.parseInt(deleteStr);
			if (delNum > maxDeleteResults)
			{
				task.setObject(param_response, NUMBER_INVALID);
				return;
			}

			ViralData[] dataArr = getViraldata(subscriber.getSubscriberID(), null, "DELETE");
			if (dataArr == null || dataArr.length == 0)
			{
				task.setObject(param_response, NUMBER_INVALID);
				return;
			}

			String downloads = dataArr[dataArr.length - 1].getClipID();
			StringTokenizer stk = new StringTokenizer(downloads, ",");
			String delPromoID = null;
			for (int i = 1; stk.hasMoreTokens(); i++)
			{
				String temp = stk.nextToken();
				if (i == delNum)
				{
					delPromoID = temp;
					break;
				}
			}
			if (delPromoID != null)
				deleteSong(task, delPromoID, true);
			else
			{
				task.setObject(param_response, NUMBER_INVALID);
				return;
			}
		}
		catch (Exception e)
		{
			// WT codes will be processed here
			String sevenDigitSmsPrefix = getSevenDigitSmsPrefix(values[1]);
			if (sevenDigitSmsPrefix != null)
			{
				String wtCode = null;
				if (values.length == 2)
					wtCode = values[1].substring(sevenDigitSmsPrefix.length());
				else if (values.length == 3)
					wtCode = values[2];

				if (wtCode != null)
					deleteSong(task, wtCode, false);
			}
			else
			{
				String delPromoID = (values.length > 2) ? values[2] : values[1];
				task.setObject(param_promoID, delPromoID);
				task.setObject(param_response, CODE_INVALID);
				return;
			}
		}
	}

	private void deleteSong(Task task, String delPromoID, boolean actualPromoID)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		try
		{
			if (!actualPromoID)
			{
				Clip clip = rbtCacheManager.getClipByPromoId(delPromoID);
				if (clip != null)
					delPromoID = clip.getClipPromoId();
				else
				{
					task.setObject(param_promoID, delPromoID);
					task.setObject(param_response, CODE_INVALID);
					return;
				}
			}

			Clip clip = rbtCacheManager.getClipByPromoId(delPromoID);
			int clipId = clip.getClipId();
			task.setObject(param_clipName, clip.getClipName());

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriber.getSubscriberID());
			Downloads downloads = rbtClient.getDownloads(rbtDetailsRequest);
			Download[] downloadsArr = downloads.getDownloads();

			boolean downloadExists = false;
			Download download = null;
			for (int i = 0; i < downloadsArr.length; i++)
			{
				if (downloadsArr[i].getToneID() == clipId)
				{
					downloadExists = true;
					download = downloadsArr[i];
					break;
				}
			}

			if (!downloadExists)
			{
				task.setObject(param_response, NO_DOWNLOADS);
			}
			else if (download != null
					&& download.getDownloadStatus().equalsIgnoreCase(
							WebServiceConstants.DEACT_PENDING))
			{
				task.setObject(param_promoID, delPromoID);
				task.setObject(param_response, ALREADY_DELETED);
			}
			else if (download != null)
			{
				SelectionRequest selectionRequest = new SelectionRequest(
						subscriber.getSubscriberID());
				selectionRequest.setCategoryID(String.valueOf(download
						.getCategoryID()));
				selectionRequest.setClipID(String.valueOf(clipId));
				
				rbtClient.deleteSubscriberDownload(selectionRequest);
				String response = selectionRequest.getResponse();
				if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
				{
					task.setObject(param_response, SUCCESS);
				}
				else
				{
					task.setObject(param_promoID, delPromoID);
					task.setObject(param_response, FAILURE);
				}
			}
		}
		finally
		{
			if (actualPromoID)
				removeViraldata(subscriber.getSubscriberID(), null, "DELETE");
		}
	}
	
	private String[] rearrangeSmsText(Task task, String smsText)
	{
		logger.info("smsText:" + smsText);
		String[] smsTokens = smsText.split(" ");

		boolean rbtKey = false;
		List<String> numbers = new ArrayList<String>();
		List<String> smsList = new ArrayList<String>();
		List<String> wtList = new ArrayList<String>();
		
		for (int j = 0; j < smsTokens.length; j++)
		{
			try
			{
				Long.parseLong(smsTokens[j]);
				numbers.add(smsTokens[j]);
				continue;
			}
			catch (Exception e)
			{

			}

			int i = 0;
			for (; i < supportedLangs.length; i++)
			{
				if (supportedLangs[i].equalsIgnoreCase(smsTokens[j]))
				{
					task.setObject(param_language, supportedLangs[i]);
					break;
				}
			}
			if (i == supportedLangs.length)
			{
				if (checkKey(smsTokens[j]))
				{
					rbtKey = true;
				}
				else
				{
					if (getSevenDigitSmsPrefix(smsTokens[j]) != null)
					{
						wtList.add(smsTokens[j]);
						if (j != (smsTokens.length - 1))
						{
							String nextElement = smsTokens[j + 1];
							try
							{
								if (nextElement.length() >= 7)
									continue;
								
								Long.parseLong(nextElement);
								String wtCode = wtList.get(0) + nextElement;
								
								wtList.clear();
								wtList.add(wtCode);
								j++;
							}
							catch (Exception e)
							{
								continue;
							}
						}
					}
					else
						smsList.add(smsTokens[j]);
				}
			}
		}
		
		String rbtKeyOptional = getSMSParameter(RBT_KEYWORD_OPTIONAL);
		boolean isRBTKeywordOptional = (rbtKeyOptional != null && rbtKeyOptional.equalsIgnoreCase("true"));
		if (rbtKey || isRBTKeywordOptional)
		{
			// adding wt list
			for (String wtCode : wtList)
			{
				smsList.add(wtCode);
			}

			String temp = null;
			for (String number : numbers)
			{
				if (temp == null && number.length() >= 10)
					temp = number;
				else
					smsList.add(number);
			}

			if (temp != null)
				smsList.add(temp);
			
			return (smsList.toArray(new String[0]));
		}
		else
			return null;
	}
	
	private boolean checkKey(String strKey)
	{
		String keyword = getSMSParameter(RBT_KEYWORDS);
		if (keyword != null)
		{
			String[] rbtKeywords = keyword.toLowerCase().split(",");
			for (String rbtKeyword : rbtKeywords)
			{
				if (strKey.equalsIgnoreCase(rbtKeyword))
					return true;
			}
		}

		return false;
	}

	private String getFiveDigitClipID(Task task, String[] values)
	{
		String strFiveDigitUniqueCode = null;
		String sevenDigitSmsPrefix = getSevenDigitSmsPrefix(values[0]);
		if (sevenDigitSmsPrefix == null)
		{
			// Checking smsText is only clipAlias
			if (values.length == 1 && checkClipAlias(task, values[0]))
				strFiveDigitUniqueCode = values[0];
		}
		else
		{
			if (values[0].trim().equalsIgnoreCase(sevenDigitSmsPrefix)
					&& values.length >= 2)
				strFiveDigitUniqueCode = values[1].trim();
			else
				strFiveDigitUniqueCode = values[0]
						.substring(sevenDigitSmsPrefix.length());
		}

		return strFiveDigitUniqueCode;
	}
	
	private String getSevenDigitSmsPrefix(String smsText)
	{
		logger.debug("smsText is :" + smsText);

		String sevenDigitSmsPrefix = null;
		String[] sevenDigitSmsPrefixArr = null;
		String smsStartString = getSMSParameter(SMS_START_STRING);
		if (smsStartString != null)
		{
			sevenDigitSmsPrefixArr = smsStartString.toLowerCase().split(",");
			smsText = smsText.toLowerCase();

			for (String sevenDigitPrefix : sevenDigitSmsPrefixArr)
			{
				if (smsText.startsWith(sevenDigitPrefix))
				{
					sevenDigitSmsPrefix = sevenDigitPrefix;
					break;
				}
			}
		}

		return sevenDigitSmsPrefix;
	}

	private boolean checkClipAlias(Task task, String smsAlias)
	{
		if (smsAlias == null)
			return false;

		// If this method called multiple times, then the clip object stored in
		// the context will be returned back.
		Clip clip = (Clip)task.getObject(param_clip);
		if (clip != null)
			return true;

		clip = rbtCacheManager.getClipBySMSAlias(smsAlias);
		if (clip == null
				|| clip.getSmsStartTime() == null
				|| clip.getClipEndTime() == null
				|| (clip.getSmsStartTime() != null && clip.getSmsStartTime()
						.after(new Date())))
		{
			return false;
		}

		task.setObject(param_clip, clip);
		return true;
	}
	
	private boolean checkAct(String strMsg)
	{
		String subKeywords = getSMSParameter(ACTIVATION_KEYWORD);
		String[] subMsgs = subKeywords.split(",");
		for (String subMsg : subMsgs)
		{
			if (strMsg.equalsIgnoreCase(subMsg))
				return true;
		}

		return false;
	}
	
	private boolean checkDeAct(String strMsg)
	{
		String unsubKeywords = getSMSParameter(DEACTIVATION_KEYWORD);
		String[] unsubMsgs = unsubKeywords.split(",");
		for (String unsubMsg : unsubMsgs)
		{
			if (strMsg.equalsIgnoreCase(unsubMsg))
				return true;
		}

		return false;
	}
	
	private String[] getPriceAndValidity(String chargeClassStr)
	{
		String[] priceNvalidity = new String[2];

		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setName(chargeClassStr);
		ChargeClass chargeClass = rbtClient
				.getChargeClass(applicationDetailsRequest);

		if (chargeClass != null)
		{
			priceNvalidity[0] = "Rs." + chargeClass.getAmount();

			String validity = chargeClass.getPeriod();
			String validFor = null;
			String validForNo = null;
			validForNo = validity.substring(1);
			if (validity.startsWith("D"))
			{
				validFor = "days";
			}
			else if (validity.startsWith("M"))
			{
				validFor = "months";
			}
			else if (validity.startsWith("Y"))
			{
				validFor = "years";
			}
			priceNvalidity[1] = validForNo + validFor;
		}

		return priceNvalidity;
	}

	private boolean checkOptInCopyCancel(String strMsg)
	{
		String[] optInCopyCancelKeywords = null;
		String optInCopyCancelKeys = getSMSParameter(COPY_CANCEL_KEYWORD);
		if (optInCopyCancelKeys != null)
		{
			optInCopyCancelKeywords = optInCopyCancelKeys.toLowerCase().split(
					",");
			for (String optInCopyCancelKeyword : optInCopyCancelKeywords)
			{
				if (strMsg.equalsIgnoreCase(optInCopyCancelKeyword))
					return true;
			}
		}

		return false;
	}

	private boolean checkCopyConfirm(String strMsg)
	{
		String[] copyConfirmKeywords = null;
		String copyConfirmKeys = getSMSParameter(COPY_CONFIRM_KEYWORD);
		if (copyConfirmKeys != null)
		{
			copyConfirmKeywords = copyConfirmKeys.toLowerCase().split(",");
			for (String copyConfirmKeyword : copyConfirmKeywords)
			{
				if (strMsg.equalsIgnoreCase(copyConfirmKeyword))
					return true;
			}
		}

		return false;
	}
	
	public boolean checkFeed(String strMsg)
	{
		if (feedMsgs != null)
		{
			for (String feedMsg : feedMsgs)
			{
				if (strMsg.equalsIgnoreCase(feedMsg))
					return true;
			}
		}

		return false;
	}
	
	private boolean checkLoop(String keyword)
	{
		if (keyword == null)
			return false;
		String loopKeywordsStr = getSMSParameter(LOOP_KEYWORDS);
		if (loopKeywordsStr != null)
			loopKeywords = Arrays.asList(loopKeywordsStr.toLowerCase().split(","));
		
		if (loopKeywords.contains(keyword.toLowerCase()))
			return true;
		
		return false;
	}

	private boolean checkDelete(String keyword)
	{
		if (keyword == null)
			return false;
		String deleteKeywordsStr = getSMSParameter(DELETE_KEYWORDS);
		if (deleteKeywordsStr != null)
			deleteKeywords = Arrays.asList(deleteKeywordsStr.toLowerCase().split(","));

		if (deleteKeywords.contains(keyword.toLowerCase()))
			return true;
		return false;
	}
	
	private boolean checkMoreDelete(String keyword)
	{
		if (keyword == null)
			return false;
		String moreDeleteKeywordsStr = getSMSParameter(DELETE_MORE_KEYWORDS);
		if (moreDeleteKeywordsStr != null)
		{
			moreDeleteKeywords = getTokenizedList(moreDeleteKeywordsStr, ",",
					true);
		}
		if (moreDeleteKeywords.contains(keyword.toLowerCase()))
			return true;
		return false;
	}
	
	public boolean checkSearchResponse(String strMsg)
	{
		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null)
			moreRBTKeywords = Arrays.asList(moreKeywords.toLowerCase().split(","));

		if (strMsg == null)
			return false;
		int songNo = -1;
		try
		{
			songNo = Integer.parseInt(strMsg);
		}
		catch (Exception e)
		{
			songNo = -1;
		}

		int maxSearchResultsToSend = 4;
		String maxResults = getSMSParameter(LUCENE_MAX_RESULTS_TO_SEND);
		if (maxResults != null)
		{
			try
			{
				maxSearchResultsToSend = Integer.parseInt(maxResults);
			}
			catch (Exception e)
			{
				maxSearchResultsToSend = 4;
			}
		}

		if (songNo > 0 && songNo <= maxSearchResultsToSend)
			return true;
		else if (songNo > maxSearchResultsToSend)
			return false;
		else if (moreRBTKeywords.contains(strMsg.toLowerCase()))
			return true;

		return false;
	}
	
	private boolean checkCategoryPromo(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String[] values = (String[]) task.getObject(param_sms);
		Category category = getCategoryByPromoId(values[0], subscriber.getLanguage());
		if (category != null)
		{
			Clip[] clips = getClipsByCatId(category.getCategoryId(),
					subscriber.getLanguage());
			if (clips != null)
			{
				Clip clip = clips[0];
				task.setObject(param_clip, clip);
				task.setObject(param_catid,
						String.valueOf(category.getCategoryId()));
				return true;
			}
		}

		return false;
	}

	private boolean checkCategoryAlias(Task task)
	{
		String[] values = (String[]) task.getObject(param_sms);
		Category category = rbtCacheManager.getCategoryBySMSAlias(values[0]);
		if (category != null)
		{
			task.setObject(param_category, category);
			task.setObject(param_catid,
					String.valueOf(category.getCategoryId()));
			return true;
		}

		return false;
	}
	
	private boolean checkTempCan(String strMsg)
	{
		String canMsgs = getSMSParameter(TEMPORARY_OVERRIDE_CANCEL_MESSAGE);
		if (canMsgs != null)
		{
			String[] tempCanMsgs = canMsgs.toLowerCase().split(",");
			for (String canMsg : tempCanMsgs)
			{
				if (strMsg.equalsIgnoreCase(canMsg))
					return true;
			}
		}

		return false;
	}
	
	/**
	 * The following if block will change the trail reply keyword T1 to TRAIL 1
	 * It is isolated from other keyword verifications as it doesn't process the
	 * message but simply modifies the message format
	 * 
	 * @param strMsg
	 * @return
	 */
	private boolean checkTrialReply(String strMsg)
	{
		String[] trialReplyMsgs = null;
		String trialReply = getSMSParameter(TRIAL_REPLY_KEYWORD);
		if (trialReply != null)
		{
			trialReplyMsgs = trialReply.toLowerCase().split(",");
			for (String trialReplyMsg : trialReplyMsgs)
			{
				if (strMsg.equalsIgnoreCase(trialReplyMsg))
					return true;
			}
		}

		return false;
	}

	private boolean checkTrial(String strMsg)
	{
		String trials = getSMSParameter(TRIAL_KEYWORDS);
		if (trials != null)
		{
			trialMsgs = trials.toLowerCase().split(",");
			for (String trialMsg : trialMsgs)
			{
				if (strMsg.equalsIgnoreCase(trialMsg))
					return true;
			}
		}

		return false;
	}

	private boolean checkCategorySearch(Task task)
	{
		if (parentCatIdsListForCatSearch == null)
			return false;

		String[] smsTokens = (String[]) task.getObject(param_sms);
		List<String> smsStartTokens = new ArrayList<String>();
		String smsStartString = getSMSParameter(SMS_START_STRING);
		if (smsStartString != null)
			smsStartTokens = Arrays.asList(smsStartString.toUpperCase().split(","));

		String moreKeywords = getSMSParameter(MORE_RBT_KEYWORDS);
		if (moreKeywords != null)
			moreRBTKeywords = Arrays.asList(moreKeywords.toUpperCase().split(
					","));

		boolean isMoreRequest = false;
		String smsText = null;
		if (smsTokens.length == 1)
		{
			for (String smsStartToken : smsStartTokens)
			{
				smsText = smsTokens[0].toUpperCase();
				if (smsText.startsWith(smsStartToken))
				{
					// Trimming WT Keyword
					smsText = smsText.substring(smsStartToken.length());
					break;
				}
			}
		}
		else if (smsTokens.length > 1)
		{
			for (String smsToken : smsTokens)
			{
				if (smsStartTokens.contains(smsToken))
				{
					// Removing WT Keyword
					continue;
				}

				String tmpSMSText = smsToken.toUpperCase();
				if (moreRBTKeywords.contains(tmpSMSText))
				{
					isMoreRequest = true;
					continue;
				}

				smsText = tmpSMSText;
			}
		}

		if (smsText != null)
		{
			if (moreRBTKeywords.contains(smsText))
			{
				// It is MORE request, so resetting the SMS in session,
				// request will be transfered to default search and there MORE
				// request will be handled.
				task.setObject(param_sms, new String[] { smsText });
				return false;
			}
			
			Category category = rbtCacheManager.getCategoryBySMSAlias(smsText);
			if (category == null)
				return false;

			String categoryID = String.valueOf(category.getCategoryId());
			if (parentCatIdsListForCatSearch.contains(categoryID))
			{
				if (isMoreRequest)
					smsText = moreRBTKeywords.get(0) + smsText;

				task.setObject(param_sms, new String[] { smsText });
				task.setObject(param_category, category);
				return true;
			}
			else
			{
				// If its not parent category search, so resetting the SMS in
				// session, request will be transfered to category alias process
				// and request will be handled there.
				if (isMoreRequest)
					smsText = moreRBTKeywords.get(0);
				task.setObject(param_sms, new String[] { smsText });
			}
		}

		return false;
	}

	private boolean isOBDRequest(Task task)
	{
		String subscriberID = task.getString(param_subscriberID);
		String obdNo = getSMSParameter(OBD_NUMBER);
		if (obdNo != null && obdNo.equals(subscriberID))
			return true;
		return false;
	}

	private boolean isRetailerRequest(String values[])
	{
		if (values.length > 3)
			return false;

		String subID = values[values.length - 1];

		try
		{
			subID = subID(subID);
			if (subID.length() != 10)
				return false;
			Long.parseLong(subID);
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}

	private boolean isParentCatForCatSearch(Task task)
	{
		Category category = (Category) task.getObject(param_category);
		if (category == null)
			return false;
		
		String catId = String.valueOf(category.getCategoryId());
		if (parentCatIdsListForCatSearch.contains(catId))
			return true;

		return false;
	}
	
	private boolean isNewSession(String subId)
	{
		ViralData[] vdataArr = getViraldata(subId, null, "CAT_SEARCH");
		if (vdataArr != null && vdataArr.length >= 1)
		{
			ViralData viralData = vdataArr[0];
			Date setTime = viralData.getSentTime();
			Date sysDate = new Date();
			if (sysDate.after(setTime))
			{
				Date setTimeAfterOneDay = increaseDateByOneDay(setTime);
				if (setTimeAfterOneDay.before(sysDate))
				{
					removeViraldata(subId, null, "CAT_SEARCH");
				}
				else
				{
					SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
					try
					{
						sysDate = sdf.parse(sdf.format(sysDate));
						setTime = sdf.parse(sdf.format(setTime));
					}
					catch (ParseException e)
					{
						logger.error(e.getMessage(), e);
					}

					if (sysDate.compareTo(setTime) == 0)
					{
						// day should be same
						return false;
					}
					else
					{
						removeViraldata(subId, null, "CAT_SEARCH");
					}
				}
			}
			else
				removeViraldata(subId, null, "CAT_SEARCH");
		}

		return true;
	}

	private Date increaseDateByOneDay(Date setTime)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(setTime);
		cal.add(Calendar.DATE, 1);
		return cal.getTime();
	}
	
	private boolean isCatCountNotInitialized(String subID)
	{
		ViralData[] viralDataArr = getViraldata(subID, null, "CAT_SEARCH");
		ViralData viralData = viralDataArr[0];

		String searchSessionTracker = viralData.getClipID();
		StringTokenizer st = new StringTokenizer(searchSessionTracker, ",");
		int count = 0;
		boolean flag = false;
		while (st.hasMoreTokens())
		{
			String temp = st.nextToken();
			String catCount = null;

			if (count == 1)
			{
				catCount = temp;
				if (catCount != null && catCount.length() > 0)
				{
					catCount = catCount.trim();
					String junk = "-1";
					junk = junk.trim();
					if (catCount.equalsIgnoreCase(junk))
					{
						flag = true;
					}
				}
				break;
			}
			count++;
		}
		return flag;
	}
	
	private boolean isValidParentCat(Category category, Subscriber subscriber,
			boolean ifMORESeach)
	{
		ArrayList<String> sessionvariables = tokenizeSessionVariables(subscriber
				.getSubscriberID());
		String existingParCat = sessionvariables.get(0);
		String parentCat = String.valueOf(category.getCategoryId());
		parentCat = parentCat.trim();
		String childCatId = sessionvariables.get(2);
		childCatId = childCatId.trim();
		if (ifMORESeach && childCatId.equalsIgnoreCase("-1"))
		{
			if (parentCat.equalsIgnoreCase(existingParCat))
			{
				return true;
			}
		}
		else
		{
			int clipId = -1;
			try
			{
				clipId = new Integer(childCatId).intValue();
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				return false;
			}

			char prepaidYes = 'n';
			if (subscriber.isPrepaid())
				prepaidYes = 'y';
			for (int i = 0; i < parentCatIdsListForCatSearch.size(); i++)
			{
				Category[] categories = rbtCacheManager
						.getActiveCategoriesInCircle(subscriber.getCircleID(),
								Integer.parseInt(parentCatIdsListForCatSearch
										.get(i)), prepaidYes);
				for (int j = 0; j < categories.length; j++)
				{
					if (categories[j].getCategoryId() == clipId)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private ArrayList<String> tokenizeSessionVariables(String subID)
	{
		ViralData[] viralDataArr = getViraldata(subID, null, "CAT_SEARCH");
		ArrayList<String> sessionVariables = new ArrayList<String>();
		if (viralDataArr != null && viralDataArr.length >= 1)
		{
			ViralData viralData = viralDataArr[0];
			String searchSessionTracker = viralData.getClipID();
			StringTokenizer st = new StringTokenizer(searchSessionTracker, ",");
			while (st.hasMoreTokens())
			{
				sessionVariables.add(st.nextToken());
			}
		}

		for (int i = sessionVariables.size(); i < 4; i++)
		{
			sessionVariables.add("-1");
		}
		return sessionVariables;
	}
	
	private boolean isExistingParentCat(Category parentCategory,
			String subscriberID)
	{
		ArrayList<String> sessionvariables = tokenizeSessionVariables(subscriberID);
		String parentId = String.valueOf(parentCategory.getCategoryId());
		if (sessionvariables != null && parentId != null
				&& parentId.length() > 0)
		{
			if (parentId.equalsIgnoreCase(sessionvariables.get(0)))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}

	private boolean isExistingChildCat(Category category, String strSubID)
	{
		ArrayList<String> sessionvariables = tokenizeSessionVariables(strSubID);
		String catId = String.valueOf(category.getCategoryId());
		if (sessionvariables != null && catId != null && catId.length() > 0)
		{
			if (catId.equalsIgnoreCase(sessionvariables.get(2)))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	private void sendCatList(Task task, Category category, int count,
			Date setTime, String newClipId, boolean updateSetTime)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subId = subscriber.getSubscriberID();
		char prepaidYes = subscriber.isPrepaid() ? 'y' : 'n';

		String response = null;
		if (category != null && count < 50)
		{
			String smsAlias = category.getCategorySmsAlias();
			Category[] catList = rbtCacheManager.getActiveCategoriesInCircle(
					subscriber.getCircleID(),
					category.getCategoryId(), prepaidYes);

			int j = 0;
			for (int i = count; catList != null && i < catList.length
					&& i < (count + maxSearchResultsToSendForCatSearch); i++, j++)
			{
				Category cat = catList[i];
				if (cat.getCategorySmsAlias() != null)
				{
					if (j == 0)
					{
						response = cat.getCategoryName() + "<WT"
								+ cat.getCategorySmsAlias().toUpperCase() + ">";
					}
					else
					{
						response = response + "," + cat.getCategoryName()
								+ "<WT"
								+ cat.getCategorySmsAlias().toUpperCase() + ">";
					}
				}
			}

			if (response != null && smsAlias != null)
			{
				smsAlias = smsAlias.toUpperCase();
				task.setObject(param_response, CAT_SEARCH_RESULTS_SUCCESS);
				task.setObject(param_search_results, response);
				task.setObject(param_sms_alias, smsAlias);

				if (setTime != null)
				{
					addViraldata(subId, setTime, "CAT_SEARCH", null, newClipId,
							-1, "SMS", setTime);
				}
				else
				{
					if (updateSetTime)
					{
						updateViraldata(subId, "CAT_SEARCH", newClipId);
						updateViraldata(subId, "CAT_SEARCH", setTime);
					}
					else
					{
						updateViraldata(subId, "CAT_SEARCH", newClipId);
					}
				}

			}
			else
			{
				task.setObject(param_response, INVALID);
			}
		}
		else
		{
			task.setObject(param_response, INVALID);
		}
		return;
	}
	
	/**
	 * @param task
	 * @param category
	 *            ChildCategory
	 * @param count
	 * @param setTime
	 * @param newClipId
	 * @return
	 */
	private boolean sendClipsList(Task task, Category category, int count,
			Date setTime, String newClipId)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subId = subscriber.getSubscriberID();
		String response = null;
		if (category != null)
		{
			String catname = category.getCategoryName();
			String smsAlias = category.getCategorySmsAlias();

			Clip[] clipList = rbtCacheManager.getClipsInCategory(category
					.getCategoryId());
			int j = 0;
			for (int i = count; clipList != null && i < clipList.length
					&& i < (count + maxSearchResultsToSendForCatSearch); i++, j++)
			{
				Clip clip = clipList[i];

				if (clip != null && clip.getClipSmsAlias() != null)
				{
					if (j == 0)
					{
						response = catname + ":To set " + clip.getClipName()
								+ "<WT" + clip.getClipSmsAlias().toUpperCase()
								+ ">";
					}
					else
					{
						response = response + "," + clip.getClipName() + "<WT"
								+ clip.getClipSmsAlias().toUpperCase() + ">";
					}
				}
			}
			if (response != null && smsAlias != null)
			{
				smsAlias = smsAlias.toUpperCase();
				task.setObject(param_response, CLIP_SEARCH_RESULTS_SUCCESS);
				task.setObject(param_search_results, response);
				task.setObject(param_sms_alias, smsAlias);

				if (setTime != null)
				{
					addViraldata(subId, setTime, "CAT_SEARCH", null, newClipId,
							-1, "SMS", setTime);
				}
				else
				{
					updateViraldata(subId, "CAT_SEARCH", newClipId);
				}

			}
			else
			{
				task.setObject(param_response, INVALID);
			}
		}
		else
		{
			task.setObject(param_response, INVALID);
		}
		return true;
	}
	
	private void updateViraldata(String subId, String smsType, String clipId)
	{
	    DataRequest viraldataRequest=new DataRequest(null,smsType);
		viraldataRequest.setSubscriberID(subId);
		viraldataRequest.setClipID(clipId);
		rbtClient.updateViralData(viraldataRequest);
	}

	private void updateViraldata(String subId, String smsType, Date sentTime)
	{
	    DataRequest viraldataRequest=new DataRequest(null,smsType);
		viraldataRequest.setSubscriberID(subId);
		viraldataRequest.setSentTime(sentTime);
		rbtClient.updateViralData(viraldataRequest);
	}
	
	private void addViraldata(String subId, Date sentTime, String smsType, String callerId, 
			String clipId, int count, String selectedBy, Date setTime)
	{
	    DataRequest viraldataRequest=new DataRequest(callerId,smsType);
		viraldataRequest.setSubscriberID(subId);
		viraldataRequest.setSentTime(sentTime);
		viraldataRequest.setClipID(clipId);
		viraldataRequest.setCount(count);
		rbtClient.addViralData(viraldataRequest);
	}	

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#processDeactivateSubscriberRecords(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	// using for feed
	@Override
	public void processDeactivateSubscriberRecords(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		SelectionRequest selectionRequest = new SelectionRequest(
				subscriber.getSubscriberID());
		selectionRequest.setMode(task.getString(param_actby));
		selectionRequest.setIsPrepaid(((Subscriber) task
				.getObject(param_subscriber)).isPrepaid());
		selectionRequest.setModeInfo(task.getString(param_actInfo));
		selectionRequest.setCallerID(task.getString(param_callerid));
		selectionRequest.setSubscriptionClass(task.getString(param_subclass));
		Object statusObj = task.getObject(param_status);
		if (statusObj != null)
		{
			Integer status = (Integer) statusObj;
			selectionRequest.setStatus(status);
		}
		rbtClient.deleteSubscriberSelection(selectionRequest);
	}
}
