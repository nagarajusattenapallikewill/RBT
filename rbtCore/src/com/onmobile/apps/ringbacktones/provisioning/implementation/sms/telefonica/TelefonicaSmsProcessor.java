/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: rony.gregory $
 * $Id: TelefonicaSmsProcessor.java,v 1.70 2015/02/25 08:40:52 rony.gregory Exp $
 * $Revision: 1.70 $
 * $Date: 2015/02/25 08:40:52 $
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.telefonica;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PendingConfirmationsRemainder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class TelefonicaSmsProcessor extends SmsProcessor {
	private static final Logger logger = Logger.getLogger(TelefonicaSmsProcessor.class);
//	private RBTConnector rbtConnector = null;
	protected RBTDBManager rbtDBManager = null;
	String m_optInConfirmationActSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Subscription charge is %ACT_AMT Rs. And Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationSelSMS = "You have pressed star to copy the selection %S. If you want to copy send %RBT_CONFIRM within %C min. The Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationMusicPackSMS = "You have selection %S on music pack. If you want to confirm send %RBT_CONFIRM within %C min.";
	String affiliatedContentConfirmationSMS = "You have purchased affiliated content %S. If you want to copy send %RBT_CONFIRM within %C min.";
	private String reminderSmsTextForAct = "Your subscription is in pending. To confirm send %RBT_CONFIRM by %C";
	private String reminderSmsTextForSel = "You have selected %S. To confirm send %RBT_CONFIRM by %C";
	private String smsConfirmationOn = null;
	private String smsConfirmationOnForActiveUsers = null;
	private String affiliatedModes = null;
	// Check for one more confirmation is required for activation or not.
	private boolean isSmsConfirmationOn = false;
	private boolean isSmsConfirmationOnForActiveUsers = false;
	private boolean isActOptional = false;
	private boolean isSmsSendSearchAlias = false;
	private boolean allowLooping = false;
	private boolean isAddToLoop = false;
	private boolean isBuyAndGiftAllowed = false;
	private String numberOfReminders = null;
	private String remindersSender = null;
	private String waitTimeForConfirmation = null;
	private String reminderTextDateFormat = null;

	// Track TRANSACTION_LOGGER updates made to pending confirmation records.
	private static final Logger TRANSACTION_LOG = Logger.getLogger("TRANSACTION_LOGGER");

	public TelefonicaSmsProcessor() throws RBTException {
		RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();
		smsConfirmationOn = getParameter(SMS, SMS_CONFIRMATION_ON);
		isSmsConfirmationOn = Boolean.valueOf(smsConfirmationOn);
		
		smsConfirmationOnForActiveUsers = getParameter(SMS, SMS_CONFIRMATION_ON_FOR_ACTIVE_USERS);
		if(smsConfirmationOnForActiveUsers != null) {
			isSmsConfirmationOnForActiveUsers = Boolean.valueOf(smsConfirmationOnForActiveUsers);
		}
		else {
			isSmsConfirmationOnForActiveUsers = isSmsConfirmationOn;
		}
		
		affiliatedModes = getParameter(SMS, SMS_AFFILIATED_CONTENT_MODES);
		isActOptional = param(SMS, IS_ACT_OPTIONAL, false);
		isSmsSendSearchAlias = param(SMS, SEND_SEARCH_SMS_ALIAS, false);
		allowLooping = param(COMMON, ALLOW_LOOPING, false);
		isAddToLoop = param(COMMON, ADD_SEL_TO_LOOP, false);
		isBuyAndGiftAllowed = param(SMS, SMS_IS_BUY_AND_GIFT_ALLOWED, false);
		waitTimeForConfirmation = getParamAsString(SMS,
				"WAIT_TIME_DOUBLE_CONFIRMATION", String.valueOf(30));
		// reminders to be sent to the subscriber, default is 3.
		numberOfReminders = getParamAsString("DAEMON","NUM_OF_REMINDERS_TO_SEND", "3");
		// sender configuration for sending reminders to the subscriber
		remindersSender = getParamAsString("DAEMON","REMINDERS_SENDER", null);
		reminderTextDateFormat = getParamAsString("DAEMON","REMINDER_TEXT_DATE_FORMAT", "dd MMM hh:mm"); 
	}

	@Override
	public void preProcess(Task task) {
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (isUserActive(subscriber.getStatus())
				&& (smsList != null && smsList.size() > 2))
			isThisFeature(task, tokenizeArrayList(param(SMS,
					ACTIVATION_KEYWORD, null), null), null);
		super.preProcess(task);
	}

	@Override
	public void searchRequest(Task task) {

		if (task.getString(param_requesttype) != null
				&& task.getString(param_requesttype).equalsIgnoreCase(
						type_content_validator)) {
			task.setObject(param_ocg_charge_id, "NOTVALID");
			return;
		}

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();

		String searchType = task.getString(SEARCH_TYPE);
		String searchString = task.getString(SEARCH_STRING);

		HashMap<String, String> searchMap = new HashMap<String, String>();
		searchMap.put(searchType, searchString);
		String alias = task.getString(ALIAS_SEARCH);
		if (alias == null)
			alias = "";
		searchMap.put("SUBSCRIBER_ID", subscriberID);
		ArrayList clipsList = luceneIndexer.searchQuery(searchMap, 0, param(
				SMS, LUCENE_MAX_RESULTS, 15));

		if (clipsList == null || clipsList.size() <= 0) {
			if (alias.equalsIgnoreCase("true")) {
				task.setObject(SEARCH_FOR_ALIAS_FAIL, SEARCH_FOR_ALIAS_FAIL);
				return;
			}
			String smsTextForRbtFailure = getSMSTextForID(task,
					REQUEST_RBT_SMS1_FAILURE, m_requestRbtFailure1Default,
					subscriber.getLanguage());
			if (!param(SMS, REQUEST_NO_MATCH_DISP_TOP, false)) {
				task.setObject(param_responseSms, smsTextForRbtFailure);
				return;
			}

			Clip[] topClips = getClipsByCatId(5, subscriber.getLanguage());
			if (topClips != null && topClips.length > 0) {
				clipsList = new ArrayList();
				for (Clip clip : topClips) {
					LuceneClip luceneClip = new LuceneClip(clip, 0, 0, "", "");
					clipsList.add(luceneClip);
				}
			} else {
				task.setObject(param_responseSms, smsTextForRbtFailure);
				return;
			}
		}

		String nonLangClips = "";
		String langClips = "";
		String langFilterConfig = param(SMS, REQUEST_LANG_FILTER, "FALSE,hindi");
		StringTokenizer stk = new StringTokenizer(langFilterConfig, ",");
		boolean lanFilterOn = stk.nextToken().equalsIgnoreCase("true");
		String language = stk.nextToken();
		for (int i = 0; i < clipsList.size(); i++) {
			LuceneClip luceneClip = (LuceneClip) clipsList.get(i);
			String id = luceneClip.getClipId() + "";

			if (lanFilterOn && luceneClip.getLanguage() != null
					&& luceneClip.getLanguage().equalsIgnoreCase(language))
				langClips = langClips + id + ",";
			else
				nonLangClips = nonLangClips + id + ",";
		}
		if (!langClips.equalsIgnoreCase(""))
			langClips = langClips.substring(0, langClips.length() - 1);
		if (!nonLangClips.equalsIgnoreCase(""))
			nonLangClips = nonLangClips.substring(0, nonLangClips.length() - 1);
		if (langClips.length() > 0 && nonLangClips.length() > 0)
			langClips = langClips + "," + nonLangClips;
		else if (nonLangClips.length() > 0)
			langClips = nonLangClips;

		String match = "";
		StringTokenizer clipTokens = new StringTokenizer(langClips, ",");
		int iSong = 0;
		while (clipTokens.hasMoreTokens()) {
			if (iSong < param(SMS, REQUEST_MAX_SMS, 5)) {
				Clip clip = getClipById(clipTokens.nextToken(), subscriber
						.getLanguage());
				String song = clip.getClipName();
				if (param(SMS, ADD_MOVIE_REQUEST, false)
						&& clip.getArtist() != null
						&& clip.getArtist().length() > 0)
					song = clip.getArtist() + "/" + song;
				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
					else
						match = match + song + " ";
				} else {
					if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
						match = match + song + "-" + (iSong + 1) + " ";
					else
						match = match + (iSong + 1) + "-" + song + " ";
				}
			} else
				break;
			iSong++;
		}

		task.setObject(param_SMSTYPE, "REQUEST");
		removeViraldata(task);

		addViraldata(subscriberID, null, "REQUEST", langClips, "SMS", 1, null);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		// hashMap.put("SMS_TEXT",match +
		// getSMSTextForID(task,REQUEST_RBT_SMS1_SUCCESS,
		// m_requestRbtSuccess1Default));
		// hashMap.put("SONG_LIST","");

		
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS, if no
		 * configuration found then it will take the system default message.
		 */
		String defaultConfTextForSuccessRequest = getSMSTextForID(task,
				REQUEST_RBT_SMS1_SUCCESS, m_requestRbtSuccess1Default, language);
		/*
		 * Get the configured text for the REQUEST_RBT_SMS1_SUCCESS_<responses>,
		 * if that message is not configured then it will take the above default
		 * message.
		 */
		StringBuffer confForSuccessRequest = new StringBuffer(
				REQUEST_RBT_SMS1_SUCCESS);
		confForSuccessRequest.append("_").append(
				subscriber.getStatus().toUpperCase());
		String textForSuccessRequest = getSMSTextForID(task,
				confForSuccessRequest.toString(),
				defaultConfTextForSuccessRequest, language);
		hashMap.put("SMS_TEXT", textForSuccessRequest);
		hashMap.put("SONG_LIST", match);
		// String smsText = finalizeSmsText(hashMap);

		// if (clipsList.size() > param(SMS,REQUEST_MAX_SMS,5))
		// smsText = smsText +
		// getSMSTextForID(task,REQUEST_MORE,m_reqMoreSMSDefault);

		if (clipsList.size() > param(SMS, REQUEST_MAX_SMS, 5))
			hashMap.put("MORE_TEXT", getSMSTextForID(task, REQUEST_MORE,
					m_reqMoreSMSDefault, subscriber.getLanguage()));
		else
			hashMap.put("MORE_TEXT", "");
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
	}

	@Override
	public void getMoreClips(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subscriberID = subscriber.getSubscriberID();
		logger.info("getMoreClips task is " + task);
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String type = "REQUEST";
		boolean isCatSearch = isThisFeature(task, tokenizeArrayList(param(SMS,
				CATEGORY_SEARCH_KEYWORD, null), null), CATEGORY_SEARCH_KEYWORD);
		if (isCatSearch)
			type = "CATEGORY";
		task.setObject(param_SMSTYPE, type);

		ViralData context[] = getViraldata(task);
		logger.info("getMoreClips context is " + context == null ? "zero null"
				: context.length);
		if (context == null || context.length <= 0 || context[0] == null
				|| context[0].getClipID() == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_MORE_NO_SEARCH, m_reqMoreSMSNoSearchDefault,
					subscriber.getLanguage()));
			return;
		}

		String clipIDs = context[0].getClipID();
		int searchCount = context[0].getCount();
		int categoryID = -1;
		String catName = null;
		Category cat = null;
		int perSMSCount = 0;

		StringTokenizer stk = new StringTokenizer(clipIDs, ",");

		if (type.equalsIgnoreCase("CATEGORY")) {
			cat = getCategory(stk.nextToken(), subscriber.getLanguage());
			catName = cat.getCategoryName();
			perSMSCount = param(SMS, REQUEST_MAX_CAT_SMS, 10);
		} else {
			perSMSCount = param(SMS, REQUEST_MAX_SMS, 10);
		}

		if (stk.countTokens() <= perSMSCount * searchCount) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					REQUEST_MORE_EXHAUSTED, m_reqMoreSMSExhaustedDefault,
					subscriber.getLanguage()));
			return;
		}

		for (int a = 0; a < perSMSCount * searchCount; a++)
			stk.nextToken();

		String match = "";
		int iSong = perSMSCount * searchCount;
		for (int i = 1; i <= perSMSCount && stk.hasMoreTokens(); i++) {
			String song = "";
			String id = stk.nextToken();
			Clip clip = getClipById(id, subscriber.getLanguage());
			song = clip.getClipName();
			if (type.equalsIgnoreCase("CATEGORY")) {

				/*
				 * Append the artist name with song name.
				 */
				if (param(SMS, SONG_SEARCH_GIVE_ARTIST_NAME, false)) {
					if (clip.getArtist() != null)
						song = song + " (" + clip.getArtist().trim() + ")";
				}

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
				} else if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
					match = match + song + "-" + (iSong + 1) + " ";
				else
					match = match + (iSong + 1) + "-" + song + " ";
			} else {
				if (param(SMS, ADD_MOVIE_REQUEST, false)
						&& clip.getArtist() != null
						&& clip.getArtist().length() > 0)
					song = song + "/" + clip.getArtist();

				if (param(SMS, SONG_SEARCH_GIVE_PROMO_ID, false)) {
					if (clip.getClipPromoId() != null)
						match = match + song + "-" + clip.getClipPromoId()
								+ " ";
					else
						match = match + song + " ";
				} else {
					if (param(SMS, INSERT_SEARCH_NUMBER_AT_END, true))
						match = match + song + "-" + (iSong + 1) + " ";
					else
						match = match + (iSong + 1) + "-" + song + " ";
				}
			}
			iSong++;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", getSMSTextForID(task, REQUEST_RBT_SMS1_SUCCESS,
				m_requestRbtSuccess1Default, subscriber.getLanguage()));
		if (isCatSearch) {
			hashMap.put("SMS_TEXT", getSMSTextForID(task,
					CATEGORY_SEARCH_SUCCESS, m_catRbtSuccess1Default,
					subscriber.getLanguage()));
			hashMap.put("CAT_NAME", catName);
			hashMap.put("COUNT", perSMSCount + "");
		}
		hashMap.put("SONG_LIST", match);
		// String smsText = finalizeSmsText(hashMap);
		// if (stk.hasMoreTokens())
		// {
		// if(isCatSearch)
		// smsText +=
		// getSMSTextForID(task,REQUEST_MORE_CAT,m_reqMoreSMSCatDefault);
		// else
		// smsText += getSMSTextForID(task,REQUEST_MORE, m_reqMoreSMSDefault);
		// }
		if (stk.hasMoreTokens()) {
			if (isCatSearch)
				hashMap.put("MORE_TEXT", getSMSTextForID(task,
						REQUEST_MORE_CAT_SUCCESS, m_reqMoreSMSCatDefault,
						subscriber.getLanguage()));
			else
				hashMap.put("MORE_TEXT", getSMSTextForID(task, REQUEST_MORE,
						m_reqMoreSMSDefault, subscriber.getLanguage()));
		} else {
			hashMap.put("MORE_TEXT", "");
		}
		String smsText = finalizeSmsText(hashMap);
		task.setObject(param_responseSms, smsText);
		task.setObject(param_SEARCHCOUNT, ++searchCount + "");
		updateViraldata(task);

	}

	@Override
	public void processActivationRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean confirmationSmsEnabled = RBTParametersUtils.getParamAsBoolean("SMS","OPT_IN_RESUBSCRIPTION_SMS_ENABLED", "false");
		String displayDateFormat = RBTParametersUtils.getParamAsString("SMS","OPT_IN_NEXT_BILLING_DATE_FORMAT","dd-MM-yyyy");
		//to get next billing date
		if (confirmationSmsEnabled) {
			task.setObject(param_mode, "CCC");
			subscriber = getSubscriber(task);
		}
		boolean isDirectActRequest = false;
		if (task.containsKey(param_isdirectact))
			isDirectActRequest = task.getString(param_isdirectact)
					.equalsIgnoreCase("true");

		if (isDirectActRequest
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE)) {
			logger
					.info("processActivationRequest:: DirectActivationRequest & user is already ACTIVE");
			task.setObject(param_response, "ALREADYACTIVE");
			return;
		}

		if (isUserActive(subscriber.getStatus()) && !isDirectActRequest) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					ACTIVATION_FAILURE, m_activationFailureDefault, subscriber
							.getLanguage()));
			return;
		}

		if ((!isUserActive(subscriber.getStatus()) || isDirectActRequest)) {
			if (isSmsConfirmationOn) {
				// subscriber = processActivation(task);
				String language = subscriber.getLanguage();
				Date sentTime = new Date();
				String smsText = getSMSTextForID(task,
						"OPT_IN_CONFIRMATION_ACT_SMS",
						m_optInConfirmationActSMS, language);

				Date nextbillingDate = subscriber.getNextBillingDate();
				SimpleDateFormat sdf = new SimpleDateFormat(displayDateFormat);
				Date sysdate = new Date();
				//Deactivated user within subscription period
				if(null != nextbillingDate && nextbillingDate.after(sysdate)){
					smsText = getSMSTextForID(task,
							OPT_IN_CONFIRMATION_ACT_SMS_WITHIN_SUBSCRIPTION_PERIOD,
							m_optInConfirmationActSMS, language);
					smsText = smsText.replace("%NEXT_BILLING_DATE", sdf.format(nextbillingDate));
				}
				//Deactivated user after subscription period
				if(null != nextbillingDate && nextbillingDate.before(sysdate)){
					smsText = getSMSTextForID(task,
							OPT_IN_CONFIRMATION_ACT_SMS_AFTER_SUBSCRIPTION_PERIOD,
							m_optInConfirmationActSMS, language);
					smsText = smsText.replace("%NEXT_BILLING_DATE", sdf.format(nextbillingDate));
				}
			
				//
				String sms = getSubstituedSMS(smsText, null, waitTimeForConfirmation, null,
						null, null);

				String selectedBy = task.getString(param_actby);
				String subscriberId = subscriber.getSubscriberID();
				String type = "SMSCONFPENDING";
				
				//RBT-12673 getting offer for sms
				String extraInfo=null;
				boolean paramAsBoolean = RBTParametersUtils.getParamAsBoolean("SMS", "ALLOW_ONLY_BASE_OFFER", "FALSE");
				if (paramAsBoolean) {
					String subscriptionClass = null;
					String offerId = null;
					Map<String, String> extraInfoMap = new HashMap<String, String>();
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
							subscriber.getSubscriberID());
					rbtDetailsRequest
							.setOfferType(com.onmobile.apps.ringbacktones.webservice.client.beans.Offer.OFFER_TYPE_SUBSCRIPTION_STR);
					rbtDetailsRequest.setMode(SMS);
					com.onmobile.apps.ringbacktones.webservice.client.beans.Offer[] offer = RBTClient.getInstance().getOffers(
							rbtDetailsRequest);
					if (offer != null
							&& offer.length > 0
							&& (offer[0].getOfferID() == null || !offer[0]
									.getOfferID().equalsIgnoreCase("-1"))) {
						subscriptionClass = offer[0].getSrvKey();
						offerId = offer[0].getOfferID();
					}
					if (subscriptionClass != null && offerId!=null) {
						extraInfoMap.put("SUBSCRIPTION_CLASS",
								subscriptionClass);
						extraInfoMap.put("BASE_OFFERID",
								offerId);
					}
					extraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);
				}
				rbtDBManager.insertViralSMSTable(subscriberId,
						sentTime, type, null, null, 0,
						selectedBy, null, extraInfo);
				task.setObject(param_responseSms, sms);
				sendSMS(task);
				
				// Adding request to the pending confirmation remainder table
				// to send reminders to the subscriber.
				String reminderConfText = getSMSTextForID(task,
						"REMINDER_SMS_TEXT_FOR_ACT",
						reminderSmsTextForAct, language);
				String remindedByDate = convertDateToString(sentTime);
				String reminderText = getSubstituedSMS(reminderConfText, null, remindedByDate, null,
						null, null);
				addToPendingConfirmationReminder(subscriberId, type, sentTime, reminderText);
				return;
			}
			subscriber = processActivation(task);
			if (isDirectActRequest)
				task.setObject(param_response, "SUCCESS");
			if (!isDirectActRequest
					&& (subscriber != null && isUserActive(subscriber
							.getStatus()))) {
				String smsText = getSMSTextForID(task,
						ACTIVATION_SUCCESS, m_activationSuccessDefault,
						subscriber.getLanguage());
				
				HashMap<String, String> hashMap = new HashMap<String,String>();
				String subscriptionClass = subscriber.getSubscriptionClass();
				String subAmount = null;
				if (subscriptionClass != null) {
					SubscriptionClass subClass = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(subscriptionClass);
					if (subClass != null) {
						subAmount = subClass.getSubscriptionAmount();
					}
				}
				hashMap.put("SMS_TEXT", smsText);
				hashMap.put("ACT_AMT", subAmount == null ? "" : subAmount);
				hashMap.put("CIRCLE_ID", subscriber.getCircleID());
				String sms = finalizeSmsText(hashMap);
				task.setObject(param_responseSms, sms);
			}

		}

		if (subscriber == null || !isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		logger
				.info("processActivationRequest::  smsList is zero, isDirectActRequest >"
						+ isDirectActRequest);
		if (isDirectActRequest) {
			task.setObject(param_response, "SUCCESS");

			if (task.getString(param_response).equals(SUCCESS)) {
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver, task
							.getString(param_subscriberID));
					task.setObject(param_Msg, subscriptionClass
							.getSmsOnSubscription());
					sendSMS(task);
				}
			}
		}

	}

	@Override
	public void processActNSel(Task task) {
		String response = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean confirmationSmsEnabled = RBTParametersUtils.getParamAsBoolean("SMS","OPT_IN_RESUBSCRIPTION_SMS_ENABLED", "false");
		String displayDateFormat = RBTParametersUtils.getParamAsString("SMS","OPT_IN_NEXT_BILLING_DATE_FORMAT","dd-MM-yyyy");
		//to get next billing date
		if (confirmationSmsEnabled) {
			task.setObject(param_mode, "CCC");
			subscriber = getSubscriber(task);
		}

		String subStatus = subscriber.getStatus();
		String subscriberID = subscriber.getSubscriberID();
		String subLanguage = subscriber.getLanguage();
		logger.info("Processing activation and selection for subscriber: "
				+ subscriberID);
		boolean isActRequest = Boolean.valueOf(task
				.getString(IS_ACTIVATION_REQUEST));
		boolean isDirectActRequest = false;
		
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String optionPrefix = getParamAsString(param_sms, DCT_SONG_OPTION_PREFIX, null);//added for vivo song churn
		if(optionPrefix!= null && smsList != null && smsList.size() > 0 && smsList.get(0).length() <= 3 && smsList.get(0).toUpperCase().startsWith(optionPrefix.toUpperCase())){
			task.setObject(param_SMSTYPE, SMS_DCT_SONG_MANAGE);
			ViralData[] viralDataArray = getViraldata(task);
			if ((viralDataArray != null && viralDataArray.length > 0)) {
				logger.debug("got an entry in the viral sms table for smsType");
				processSongManageDeact(task);
				return;
			} else {
				logger.debug("the request session expired as there are no entry in the viral sms table so send the fresh downloads");
				task.setObject(param_responseSms, getSMSTextForID(task,
						DCT_MANAGE_SESSION_EXPIRED, m_smsSessionExpireDefault,
						subscriber.getLanguage()));
				removeViraldata(subscriber.getSubscriberID(), null, SMS_DCT_SONG_CONFIRM);
				return;
			}
		}

		if (task.containsKey(param_isdirectact)) {
			isDirectActRequest = Boolean.valueOf(task
					.getString(param_isdirectact));
		}

		if (isDirectActRequest
				&& subStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
			logger.info("Could not process activation, User is already Active");
			task.setObject(param_response, "ALREADYACTIVE");
			return;
		}

		boolean isUserActive = isUserActive(subStatus);
		if (isUserActive && isActRequest && !isDirectActRequest) {
			logger.info("Could not process activation, subscriber is already active");
			task.setObject(param_responseSms,
					getSMSTextForID(task, ACTIVATION_FAILURE,
							m_activationFailureDefault, subLanguage));
			return;
		}

		logger.info("SmsList: " + smsList);
		if (smsList != null && smsList.size() > 0) {
			String firstItem = smsList.get(0);
			if (firstItem != null && !firstItem.equalsIgnoreCase("null")) {
				Clip clip = getProfileClip(task);
				if (clip != null) {
					logger.info("Could not process. Selected clip: " + clip
							+ " is profile clip");
					processSetTempOverride(task);
					return;
				}

				getCategoryAndClipForPromoID(task, firstItem);
				clip = (Clip) task.getObject(CLIP_OBJ);
				logger.debug("Clip Object from Task: " + clip);

				if (clip == null && isSmsSendSearchAlias) {
					try {
						Integer.parseInt(firstItem);
					} catch (NumberFormatException nfe) {
						logger.info("Since first element in the SmsList"
								+ " is not a number, it is processing as "
								+ " search request");
						task.setObject(ALIAS_SEARCH, "true");
						processREQUEST(task);
						if (task.getString(SEARCH_FOR_ALIAS_FAIL) == null) {
							return;
						}
					}
				}
				
				Category category = (Category) task.getObject(CAT_OBJ);
				
				logger.info("category object : "+category+" and clip object: "+clip);
				
				// Config will have values like CH15:PACK30;CH25:PACK35;
				String xbiChargeSubClassMap = RBTParametersUtils
						.getParamAsString(SMS, XBI_CHARGE_SUB_CLASS_MAPPING,
								null);
				logger.info("xbiChargeSubClassMap :" + xbiChargeSubClassMap);
				String expiredResponse = null;
				if (category != null && xbiChargeSubClassMap != null && xbiChargeSubClassMap.trim().length() != 0) {
					HashMap<String, String> catIdRentalPack = (HashMap<String, String>) MapUtils
							.convertIntoMap(xbiChargeSubClassMap, ";", ":", null);
					String catClassType = category.getClassType();
					Collection<String> subClassList = catIdRentalPack.values();
					if (catIdRentalPack != null
							&& catIdRentalPack.containsKey(catClassType)) {
						task.setObject(param_isXBIUser, "true");
						if (category.getCategoryEndTime().getTime() < System
								.currentTimeMillis()) {
							expiredResponse = WebServiceConstants.CATEGORY_EXPIRED;
							logger.warn("Content expired...expiredResponse: "
									+ expiredResponse);
							if (expiredResponse != null) {
								task.setObject(
										param_responseSms,
										getSMSTextForID(task,
												expiredResponse.toUpperCase(),
												m_clipOrCategoryExpiredDefault));
								return;
							}
						}
						logger.info("Direct processing of XBI request. UserActive:"
								+ isUserActive
								+ " or active XBI user subClass:"
								+ subscriber.getSubscriptionClass());
						if (!isUserActive
								|| subClassList.contains(subscriber
										.getSubscriptionClass())) {
							String subClass = catIdRentalPack.get(catClassType);
							task.setObject(param_USE_UI_CHARGE_CLASS, true);
							if(!isUserActive){
							task.setObject(param_subclass, subClass);
							}else{
							task.setObject(param_rentalPack, subClass);	
							logger.info("Updating xbi users subClass with :" + subClass);
							}
							task.setObject(param_catid,
									category.getCategoryId() + "");
							task.setObject(param_chargeclass, catClassType);
							String respnse = processSetSelection(task);
							logger.info("Response after making xbi selection :"
									+ respnse);
							boolean combo = isUserActive ? false : true;
							verfifyResponse(task, subscriber, isActRequest,
									isDirectActRequest, respnse, combo);
							return;

						} else {
							task.setObject(param_SMSTYPE, SONG_PACK_REQUEST);
							// we need to have category since it is shuffle
							task.setObject(param_CLIPID,
									category.getCategoryId() + "");
							ViralData data = addViraldata(task);
							HashMap<String, String> hashMap = new HashMap<String, String>();
							// we need to have category name since it is shuffle
							hashMap.put("SONG_NAME", category.getCategoryName());
							if (data != null) {
								hashMap.put(
										"SMS_TEXT",
										getSMSTextForID(
												task,
												XBI_PACK_ACT_CONFIRM_REQ_SUCCESS,
												null));

							} else {
								hashMap.put(
										"SMS_TEXT",
										getSMSTextForID(
												task,
												XBI_PACK_ACT_CONFIRM_REQ_FAILURE,
												null));

							}
							task.setObject(param_responseSms,
									finalizeSmsText(hashMap));
							return;
						}

					}

				} else {
					logger.info("Not a valid XBI request");
				}
				
				// RBT-12582
				if (RBTParametersUtils.getParamAsBoolean("COMMON",
						"SELECTION_MODEL_PARAMETER", "FALSE")) {
				
				if (category!=null && Utility.isShuffleCategory(category.getCategoryTpe())) {
					if (category.getCategoryEndTime().getTime() < System
							.currentTimeMillis())
						expiredResponse = WebServiceConstants.CATEGORY_EXPIRED;
				}
				else if (clip != null
						&& clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					expiredResponse = WebServiceConstants.CLIP_EXPIRED;
				}
				HashMap<String, String> whereClauseMap = new HashMap<String, String>();
				if(clip!=null || category!=null) {
					if (expiredResponse != null
							&& expiredResponse.equalsIgnoreCase(CLIP_EXPIRED)
							&& clip != null) {
						whereClauseMap.put("SUBSCRIBER_WAV_FILE",
								clip.getClipRbtWavFile());
						SubscriberStatus subSatus = rbtDBManager
								.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
										subscriberID, whereClauseMap);

						if (subSatus == null) {
							boolean removeFromDownload = rbtDBManager
									.removeSubscriberDownloadBySubIdAndWavFileAndCatId(
											subscriberID,
											clip.getClipRbtWavFile(),
											-1);
							if (removeFromDownload) {
								expiredResponse = WebServiceConstants.CLIP_EXPIRED_DOWNLOAD_DELETED;
							}
						}

					} else if (expiredResponse != null
							&& expiredResponse.equalsIgnoreCase(CATEGORY_EXPIRED)
							&& category != null) {
						whereClauseMap.put("CATEGORY_ID",
								String.valueOf(category.getCategoryId()));
						SubscriberStatus subSatus = rbtDBManager
								.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(
										subscriberID, whereClauseMap);
						if (subSatus == null) {
							boolean removeFromDownload = rbtDBManager
									.removeSubscriberDownloadBySubIdAndWavFileAndCatId(
											subscriberID, null,
											category.getCategoryId());
							if (removeFromDownload) {
								expiredResponse = WebServiceConstants.CATEGORY_EXPIRED_DOWNLOAD_DELETED;
							}
						}

					}
				}
					logger.warn("Content expired...expiredResponse: "+expiredResponse);
					if (expiredResponse != null) {
						task.setObject(param_responseSms,
								getSMSTextForID(task, expiredResponse.toUpperCase(), m_clipOrCategoryExpiredDefault));
						return;
					}
				}

				if (clip == null
						|| clip.getClipEndTime().getTime() < System
								.currentTimeMillis()) {
					//Following if is commented by Sreekar for Voda-Spain on 2013/01/25
//					if (!isUserActive(subStatus)) {
						if (isDirectActRequest) {
							smsList.remove(0);
						} else if (isActRequest || isActOptional) {
							if (task.containsKey(param_isSuperHitAlbum)) {
								task.setObject(param_isPromoIDFailure, "TRUE");
							}
							logger.warn("Failed to process. Clip is not found");
							task.setObject(param_responseSms, isValidPromoId(task, firstItem, subscriber.getLanguage()));
							return;
						}
//					}
				}

				logger.info("smsConfirmationOn: " + smsConfirmationOn);
				boolean isAycePackUser = isAycePackUser(subscriber);
				logger.info("isAycePackUser: " + isAycePackUser);
				boolean confirmationNeeded = isSmsConfirmationOn;
				boolean toBeHitSMTogetUpgradeOffer = false;
				String baseUpgradeAmt = null;
				if(confirmationNeeded && isUserActive && !isSmsConfirmationOnForActiveUsers) {
					confirmationNeeded = false;
				}else{
					SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
					selectionRequest.setClipID(clip.getClipId()+"");
					selectionRequest.setCategoryID(task.getString(param_catid));
					com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient
							.getInstance().getNextChargeClass(selectionRequest);
					
					String subscriptionClass = subscriber.getSubscriptionClass() != null ? subscriber.getSubscriptionClass() : "";
					
					if (chargeClass != null) {
						if (m_FreemiumUpgradeChargeClass != null
								&& m_FreemiumUpgradeChargeClass.contains(chargeClass
										.getChargeClass()) && freemiumSubClassList.contains(subscriptionClass)) {
							confirmationNeeded = true;
							baseUpgradeAmt = getFreemiumBaseUpgrdAmt(task);
							toBeHitSMTogetUpgradeOffer = true;
						}
					}

				}
				if (category != null
						&& Utility.isShuffleCategory(category.getCategoryTpe())) {
					isAycePackUser = false;
				}
				if (isAycePackUser) {
					confirmationNeeded = true;
					boolean isSelectionAlreadyExists = isSelectionAlreadyPresent(clip, subscriberID, category, task);
						
					if (isSelectionAlreadyExists) {
						task.setObject(
								param_responseSms,
								getSMSTextForID(
										task,
										SELECTION_ALREADY_ACTIVE,
										m_doubleConfirmationEntryExpiredTextDefault,
										subscriber.getLanguage()));
					 	return;
					}

				}
				if (confirmationNeeded) {
					if (clip != null) {
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put("CALLER_ID",
								task.getString(param_callerid) == null ? param(
										SMS, SMS_TEXT_FOR_ALL, "all") : task
										.getString(param_callerid));
						if (isActRequest || isActOptional
								|| isUserActive) {       
							String language = subLanguage;
							// SMS_OPT_IN
							//For new user
							String smsText = getSMSTextForID(task,
									OPT_IN_CONFIRMATION_ACT_SMS,
									m_optInConfirmationActSMS, language);
							
							if (isUserActive) {
								smsText = getSMSTextForID(task,
										OPT_IN_CONFIRMATION_SEL_SMS,
										m_optInConfirmationSelSMS, language);
							}
							
							String chargeAmt = "0.0";
							/**
							 * Vf-Spain SM offer type different SMS change
							 */
							if(getParamAsString("SMS", "GET_OFFER_BEFORE_SMS_CONFIRMATION", "FALSE").equalsIgnoreCase("true")) {
								String doubleConfirmationSMSText = null;
								com.onmobile.apps.ringbacktones.webservice.client.beans.Offer offer = getSelOffer(task);
								if (offer!=null && offer.getSmOfferType() != null) {
									doubleConfirmationSMSText = getSMSTextForID(task,
											OPT_IN_CONFIRMATION_ACT_SMS + "_" + offer.getOfferID() + "_" + offer.getSmOfferType(), null,
											language);
									
									if (isUserActive) {
										doubleConfirmationSMSText = getSMSTextForID(task,
												OPT_IN_CONFIRMATION_SEL_SMS + "_" + offer.getOfferID() + "_" + offer.getSmOfferType(),
												null, language);
									}
								}
								
								if(offer!=null && offer.getSrvKey()!=null){
									String chargeClass = offer.getSrvKey();
									logger.info("Offer Obtained :: "+chargeClass);
									ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClass);
									if(chargeClassObj != null){
									     chargeAmt = chargeClassObj.getAmount();
									}
								}
								if(doubleConfirmationSMSText != null)
									smsText = doubleConfirmationSMSText;
							}//End of Vf-Spain offer sms changes
							else{
								SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
								selectionRequest.setClipID(clip.getClipId()+"");
								selectionRequest.setCategoryID(task.getString(param_catid));
								com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass chargeClass = RBTClient
										.getInstance().getNextChargeClass(selectionRequest);
								logger.info(" Next Charge Class in TelefonicaSmsProcessor = " + chargeClass);
								if(chargeClass!=null){
									chargeAmt = chargeClass.getAmount();
								}
							}
							if(!isUserActive){
								Date nextbillingDate = subscriber.getNextBillingDate();
								SimpleDateFormat sdf = new SimpleDateFormat(displayDateFormat);
								Date sysdate = new Date();
								//Deactivated user within subscription period
								if(null != nextbillingDate && nextbillingDate.after(sysdate)){
									smsText = getSMSTextForID(task,
											OPT_IN_CONFIRMATION_ACT_SMS_WITHIN_SUBSCRIPTION_PERIOD,
											m_optInConfirmationActSMS, language);
									smsText = smsText.replace("%NEXT_BILLING_DATE", sdf.format(nextbillingDate));
								}
								//Deactivated user after subscription period
								if(null != nextbillingDate && nextbillingDate.before(sysdate)){
									smsText = getSMSTextForID(task,
											OPT_IN_CONFIRMATION_ACT_SMS_AFTER_SUBSCRIPTION_PERIOD,
											m_optInConfirmationActSMS, language);
									smsText = smsText.replace("%NEXT_BILLING_DATE", sdf.format(nextbillingDate));
								}
							}

							String selectedBy = task.getString(param_actby);
							logger.info(" activation mode: " + selectedBy);
							/*
							 * RBT-4396: When the mode is received as any of the
							 * configured modes, the initial confirmation
							 * message has to be different and informing user
							 * about this being an affiliate content and the
							 * same will be charged even if this is a music
							 * content.
							 */
							String extraInfo = null;
							Map<String, String> extraInfoMap = new HashMap<String, String>();
							if (null != selectedBy) {
								List<String> modesList = tokenizeArrayList(
										affiliatedModes, null);
								logger
										.info("Selected mode is affiliated."
												+ " Configured affiliated modes string is: "
												+ affiliatedModes
												+ ", modesList: " + modesList);
								if (modesList != null
										&& modesList.contains(selectedBy
												.toLowerCase()) && isPackActiveForSubscriber(subscriber)) {
									smsText = getSMSTextForID(task,
											"SMS_AFFILIATED_CONTENT_TEXT",
											affiliatedContentConfirmationSMS,
											language);
									
									extraInfoMap.put("CHARGE_CLASS", clip
											.getClassType());
									
								}
							}

							if(toBeHitSMTogetUpgradeOffer) {
								smsText = getSMSTextForID(task,
										"UPGRADE_CONFIRMATION_SEL_FREEMIUM_SMS",
										smsText, language);
							}
							
							String getUpgradeSubClassForAycePack = getUpgradeSubClassForAyce(isAycePackUser, subscriber);
							if(getUpgradeSubClassForAycePack != null && !getUpgradeSubClassForAycePack.isEmpty()){
								extraInfoMap.put("UPGRADE_SUBSCRIPTION_CLASS", getUpgradeSubClassForAycePack);
							}
							extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);
							logger.info("Substitue SMS text: " + smsText
									+ ", clip name: " + clip.getClipName()
									+ ", extra info: " + extraInfo+",chargeAmt: "+chargeAmt + ", baseChargeAmt: " + baseUpgradeAmt);

							String sms = getSubstituedSMS(smsText, clip
									.getClipName(), waitTimeForConfirmation, null, baseUpgradeAmt, chargeAmt);
							Date sentTime = new Date();
							String type = "SMSCONFPENDING";
							rbtDBManager.insertViralSMSTable(subscriber
									.getSubscriberID(), sentTime,
									type, task
											.getString(param_callerid), String
											.valueOf(clip.getClipId()), 0,
									selectedBy, null, extraInfo);
							logger.info("Successfully inserted subscriber: "
									+ subscriberID + " into viral sms table");
							task.setObject(param_responseSms, sms);
							
							sendSMS(task);
							logger.info("Successfully sent sms for subcriber: "+subscriberID);
							
							// Adding request to the pending confirmation remainder table
							// to send reminders to the subscriber.
							boolean isconfrRemainderAllowed = RBTParametersUtils.getParamAsBoolean(
									COMMON, "CONFIRMATION_PENDING_ALLOWED", "TRUE");
							if (isconfrRemainderAllowed) {
								String reminderConfText = null;
								if (isUserActive) {
									reminderConfText = getSMSTextForID(task,
											"REMINDER_SMS_TEXT_FOR_SEL", reminderSmsTextForSel,
											language);
								} else {
									reminderConfText = getSMSTextForID(task,
											"REMINDER_SMS_TEXT_FOR_ACT", reminderSmsTextForAct,
											language);
								}

								String remindedByDate = convertDateToString(sentTime);
								String reminderText = getSubstituedSMS(reminderConfText,
										clip.getClipName(), remindedByDate, null, null, chargeAmt);
								// Adding request to the pending confirmation
								// remainder table
								// to send reminders to the subscriber.
								addToPendingConfirmationReminder(subscriber.getSubscriberID(),
										type, sentTime, reminderText);

								return;
							}
						}
					}
				}
			} else {
				logger.warn("First element in the SmsList is null");
				smsList.remove(0);
			}
		}

		if (smsList.size() > 0) {
			if (allowLooping && isAddToLoop)
				task.setObject(param_inLoop, "YES");
			boolean combo = isUserActive ? false : true;
			if (isActRequest || isActOptional || isUserActive) {
				logger.info("Processing selection");
				response = processSetSelection(task);
			}

			verfifyResponse(task, subscriber, isActRequest, isDirectActRequest,
					response, combo);
		}

		if (smsList == null || smsList.size() < 1) {
			/*
			 * Added by Sandeep for Buy and Gift Feature( when latest download
			 * is within maximumWaitingTime(configurable) then gift that song to
			 * the msisdn sent as sms_text
			 */
			if (isBuyAndGiftAllowed) {
				logger.info("Processing Buy and Gift Feature");
				boolean isBuyNGiftWaitingTimeOver = isBuyNGiftWaitingTimeOver(task);
				if (!isBuyNGiftWaitingTimeOver) {
					buyAndGift(task);
					return;
				}
			}
		}
	}
	
	@Override
	public void confirmActNSel(Task task) {
		logger.debug("Confirm activation and selection of the subscriber");
		super.confirmActNSel(task);
		
		// when the subscriber send confirmation for the pending requests,
		// update reminders left to -1
		updatePendingConfirmationReminder(task.getString(param_subscriberID),
				-1);
	}
	
	@Override
	public void confirmActNSel(Task task, ViralData viralData) {
		logger.debug("Confirm activation and selection of the subscriber");
		super.confirmActNSel(task, viralData);
		
		// when the subscriber send confirmation for the pending requests,
		// update reminders left to -1
		updatePendingConfirmationReminder(task.getString(param_subscriberID),
				-1);
	}

	private void verfifyResponse(Task task, Subscriber subscriber,
			boolean isActRequest, boolean isDirectActRequest, String response,boolean combo) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		String callerId = task.getString(param_callerid) == null ? param(SMS,
				SMS_TEXT_FOR_ALL, "all") : task
				.getString(param_callerid);
		hashMap.put("CALLER_ID", callerId);
		boolean active = isUserActive(subscriber.getStatus());

		Clip clip = null;
		String taskAction = task.getTaskAction();
		Boolean isXBIUser=  Boolean.parseBoolean(task.getString(param_isXBIUser));
		
		if (response == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
		} else if (taskAction.equalsIgnoreCase(MUSIC_PACK_KEYWORD)
				&& response.equalsIgnoreCase("success")) {

			Category category = (Category) task.getObject(CAT_OBJ);
			if (null != category
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(category.getCategoryTpe())) {
				clip = (Clip) task.getObject(CLIP_OBJ);
				hashMap.put("SONG_NAME", category.getCategoryName());
			} else {
				clip = (Clip) task.getObject(CLIP_OBJ);
				if (clip != null) {
					hashMap.put("SONG_NAME", clip.getClipName());
				}
			}
			
			
			String smsTextForID = getSMSTextForID(task,
					MUSIC_PACK_ACTIVATION_SUCCESS,
					m_actMusicPackSuccessTextDefault, subscriber.getLanguage());
			
		
			if (!active)
				smsTextForID = getSMSTextForID(task,
						MUSIC_PACK_N_BASE_ACTIVATION_SUCCESS,
						m_actMusicPackSuccessTextDefault, subscriber
								.getLanguage());
			

			hashMap.put("SMS_TEXT", smsTextForID);
			task.setObject(param_responseSms, finalizeSmsText(hashMap));
		} else if (response.equalsIgnoreCase("success")) {
			if (!isDirectActRequest) {
				Category category = (Category) task.getObject(CAT_OBJ);
				clip = (Clip) task.getObject(CLIP_OBJ);
				if (null != category
						&& com.onmobile.apps.ringbacktones.webservice.common.Utility
								.isShuffleCategory(category.getCategoryTpe())) {
					hashMap.put("SONG_NAME", category.getCategoryName());
					hashMap.put("PROMO_ID",category.getCategoryPromoId());
				} else if (clip != null) {
					hashMap.put("SONG_NAME", clip.getClipName());
					hashMap.put("PROMO_ID", clip.getClipPromoId());
				}
				if (clip != null) {
					hashMap.put("ALBUM", clip.getAlbum());
					hashMap.put("ARTIST", clip.getArtist());
				}

				if(task.containsKey(param_song_chrg_amt)){
					hashMap.put("SMS_TEXT", getSMSTextForID(task,
							ACTIVATED_PROMO_SUCCESS,
							m_activatedPromoSuccessTextDefault, subscriber
									.getLanguage()));
					
				} else if (isActRequest) {
					hashMap.put(
							"SMS_TEXT",
							getSMSTextForID(task, ACTIVATION_PROMO_SUCCESS,
									m_actPromoSuccessTextDefault,
									subscriber.getLanguage()));
				} else {//RBT-13980 - different sms for new and active user.
					String promoIdSmsText = getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage());
					if (combo) {
						promoIdSmsText = getSMSTextForID(task,
								PROMO_ID_COMBO_SUCCESS, promoIdSmsText,
								subscriber.getLanguage());
						String subAmount = null;
						if (task.getTaskSession() != null) {
							Rbt rbt = (Rbt) task.getTaskSession().get(param_rbt_object);
							if (rbt != null) {
								Subscriber sub = rbt.getSubscriber();
								if (sub != null) {
									String subscriptionClass = sub.getSubscriptionClass();
									if (subscriptionClass != null) {
										SubscriptionClass subClass = CacheManagerUtil
												.getSubscriptionClassCacheManager()
												.getSubscriptionClass(subscriptionClass);
										if (subClass != null) {
											subAmount = subClass.getSubscriptionAmount();
										}
									}
								}
							}
						}
						hashMap.put("ACT_AMT", subAmount == null ? "" : subAmount);
						promoIdSmsText = getSMSTextForID(task,
								NEW_USR_PROMO_ID_SUCCESS, promoIdSmsText,
								subscriber.getLanguage());
					}
					if(isXBIUser){
						promoIdSmsText = getSMSTextForID(task,
								XBI_PACK_ACT_REQ_SUCCESS, null,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", promoIdSmsText);
						task.setObject(param_responseSms, finalizeSmsText(hashMap));
						logger.info("Xbi user so returning after getting smstext fro success case");
						return;
					}
					
					hashMap.put("SMS_TEXT", promoIdSmsText);
				}
					
				/*
				 * RBT-4539: Based on the rbt object get the downloads and
				 * selections. For each download promo id matches with clip
				 * wave file get amount for that particular charge class and
				 * put it into the hash map with key: SEL_AMT.
				 */
				if (task.containsKey(param_song_chrg_amt)) {
					hashMap.put("SEL_AMT", task.getString(param_song_chrg_amt));
				} else {
					String chargeClassAmount = getChargeClassFromSelections(
							task, callerId, clip);
					hashMap.put("SEL_AMT", chargeClassAmount == null ? ""
							: chargeClassAmount);
				}

				task.setObject(param_responseSms, finalizeSmsText(hashMap));
			} else {
				com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass subscriptionClass = getSubscriptionClass(task
						.getString(param_subclass));
				if (subscriptionClass != null) {
					task.setObject(param_Sender, "56789");
					task.setObject(param_Reciver, task
							.getString(param_subscriberID));
					task.setObject(param_Msg, subscriptionClass
							.getSmsOnSubscription());
					sendSMS(task);
				}
			}
		} else if (isActivationFailureResponse(response)) {
			String smsText = getSMSTextForID(task, HELP_SMS_TEXT,
					m_helpDefault, subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.SELECTION_SUSPENDED)) {
			String smsText = getSMSTextForID(task,
					SELECTION_SUSPENDED_TEXT, m_SuspendedSelDefault,
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.ALREADY_EXISTS)) {
			String smsText = getSMSTextForID(task,
					SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,
							PROMO_ID_SUCCESS, m_promoSuccessTextDefault,
							subscriber.getLanguage()), subscriber
							.getLanguage());
			if(isXBIUser){
				smsText = getSMSTextForID(task,
						XBI_PACK_ALREADY_EXISTS,
						m_actMusicPackSuccessTextDefault, subscriber.getLanguage());
				logger.info("Xbi user getting smstext for already exists case");
				
			}
			
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
			String smsText = getSMSTextForID(task,
					SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT,
					getSMSTextForID(task, PROMO_ID_SUCCESS,
							m_promoSuccessTextDefault, subscriber
									.getLanguage()), subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.NOT_ALLOWED)) {
			String smsText = getSMSTextForID(task,
					SELECTION_ADRBT_NOTALLOWED_,
					m_ADRBTSelectionFailureDefault, subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task, SELECTION_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber
									.getLanguage()), subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task,
					PERSONAL_SELECTION_OVERLIMIT, getSMSTextForID(task,
							PROMO_ID_FAILURE, m_promoIDFailureDefault,
							subscriber.getLanguage()), subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT)) {
			String smsText = getSMSTextForID(task,
					LOOP_SELECTION_OVERLIMIT, getSMSTextForID(task,
							PROMO_ID_FAILURE, m_promoIDFailureDefault,
							subscriber.getLanguage()), subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equals(WebServiceConstants.OVERLIMIT)) {
			String smsText = getSMSTextForID(task, DOWNLOAD_OVERLIMIT,
					getSMSTextForID(task, PROMO_ID_FAILURE,
							m_promoIDFailureDefault, subscriber
									.getLanguage()), subscriber
							.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED)) {
			String smsText = getSMSTextForID(task,
					LITEUSER_PREMIUM_BLOCKED, liteUserPremiumBlocked,
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response.equalsIgnoreCase("DOWNLOAD_MONTHLY_LIMIT_REACHED")) {
			String smsText = getSMSTextForID(task,
					"DOWNLOAD_MONTHLY_LIMIT_REACHED", DOWNLOAD_OVERLIMIT,
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else if (response
				.equals(WebServiceConstants.OFFER_NOT_FOUND)) {
			String smsText = getSMSTextForID(task,
					OFFER_NOT_FOUND_TEXT, m_OfferAlreadyUsed,
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		}else if(response.startsWith(WebServiceConstants.SELECTIONS_BLOCKED)){
			String smsText = getSMSTextForID(task,
					"SEL_BLOCKED_FOR_"+subscriber.getStatus().toUpperCase(), m_selBlockedForStatus,
					subscriber.getLanguage());
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		} else {
			if (task.containsKey(param_isSuperHitAlbum))
				task.setObject(param_isPromoIDFailure, "TRUE");
			String smsText = getSMSTextForID(task, PROMO_ID_FAILURE,
					m_promoIDFailureDefault, subscriber.getLanguage());
			if(isXBIUser){
				smsText = getSMSTextForID(task,
						XBI_PACK_ACT_REQ_FAILURE,
						null, subscriber.getLanguage());
				logger.info("Xbi user getting smstext for failure case");
			}
			smsText = finalSmsText(smsText, task);
			task.setObject(param_responseSms, smsText);
		}
	}

	private String finalSmsText(String sms, Task task) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		Clip clip = null;
		Category category = (Category) task.getObject(CAT_OBJ);
		if (null != category
				&& com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(category.getCategoryTpe())) {
			hashMap.put("SONG_NAME", category.getCategoryName());
		} else {
			clip = (Clip) task.getObject(CLIP_OBJ);
			if (clip != null) {
				hashMap.put("SONG_NAME", clip.getClipName());
			} else
				hashMap.put("SONG_NAME", "");
		}
		// RBT-14299:Code changes done for special caller so that the message
		// will get replace with CALLER_ID
		String callerId = task.getString(param_callerid) == null ? param(SMS,
				SMS_TEXT_FOR_ALL, "all") : task.getString(param_callerid);
		hashMap.put("CALLER_ID", callerId);
		hashMap.put("SMS_TEXT", sms);
		sms = finalizeSmsText(hashMap);
		return sms;

	}

//	protected String getSubstituedSMS(String smsText, String str1, String str2,
//			String str3, String actAmt, String selAmt) {
//		if (smsText == null)
//			return null;
//		if (actAmt != null) {
//			while (smsText.indexOf("%ACT_AMT") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%ACT_AMT"))
//						+ actAmt
//						+ smsText.substring(smsText.indexOf("%ACT_AMT") + 8);
//			}
//		}
//		if (selAmt != null) {
//			while (smsText.indexOf("%SEL_AMT") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%SEL_AMT"))
//						+ selAmt
//						+ smsText.substring(smsText.indexOf("%SEL_AMT") + 8);
//			}
//		}
//
//		if (str2 == null) {
//			if (smsText.indexOf("%L") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
//						+ smsText.substring(smsText.indexOf("%L") + 2);
//			}
//		} else if (str3 == null) {
//			while (smsText.indexOf("%S") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
//						+ smsText.substring(smsText.indexOf("%S") + 2);
//			}
//			while (smsText.indexOf("%C") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
//						+ smsText.substring(smsText.indexOf("%C") + 2);
//			}
//			while (smsText.indexOf("%L") != -1) {
//				smsText = smsText.replace(" %L", "");
//			}
//		} else {
//			while (smsText.indexOf("%S") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
//						+ smsText.substring(smsText.indexOf("%S") + 2);
//			}
//			while (smsText.indexOf("%C") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
//						+ smsText.substring(smsText.indexOf("%C") + 2);
//			}
//			while (smsText.indexOf("%L") != -1) {
//				smsText = smsText.substring(0, smsText.indexOf("%L")) + str3
//						+ smsText.substring(smsText.indexOf("%L") + 2);
//			}
//		}
//
//		return smsText;
//	}

	public void processHelp(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String language = null;
		if (subscriber != null)
			language = subscriber.getLanguage();
		String helpOn = "HELP";
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0)
			helpOn = smsList.get(0).trim().toUpperCase();
		if (subscriber.getStatus().equalsIgnoreCase(
				WebServiceConstants.DEACTIVE)
				|| subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.NEW_USER)) {
			String defaultUnsubscribed = getSMSTextForID(task,
					"HELP_UNSUBSCRIBED_DEFAULT", m_helpDefault, language);
			task.setObject(param_responseSms, getSMSTextForID(task,
					"HELP_UNSUBSCRIBED_" + helpOn, defaultUnsubscribed,
					language));
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task, "HELP_"
					+ helpOn, getSMSTextForID(task, "HELP_HELP", m_helpDefault,
					language), language));
		}

	}

	@Override
	public void processTNBActivation(Task task) {
		logger.debug("Processing TNB Activation");
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		if (!status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				&& !status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {
			// Send sms informing existing subscription
			task.setObject(param_responseSms, getSMSTextForID(task,
					TNB_ACTIVATION_EXISTINGUSER, m_tnbExistingUser, subscriber
							.getLanguage()));
			return;
		}

		String promoId = param("SMS", "DEFAULT_PROMO_ID", null);

		if (promoId == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber
							.getLanguage()));
			return;
		}

		task.setObject(param_PROMO_ID, promoId);

		String subscriptionClass = null;
		Parameters param = parameterCacheManager.getParameter("SMS",
				"TNB_OFFER_SUBSCRIPTION_CLASS", null);
		if (param != null) {
			subscriptionClass = param.getValue();
		}

		String userType = subscriber.isPrepaid() ? "p" : "b";
		try {
			Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
					subscriber.getSubscriberID(), "TNB",
					Offer.OFFER_TYPE_SUBSCRIPTION, userType, subscriptionClass,
					null);
			if (offers == null || offers.length == 0) {
				task.setObject(param_responseSms, getSMSTextForID(task,
						TNB_ACTIVATION_OFFERUSED, m_tnbOfferUsed, subscriber
								.getLanguage()));
				return;
			}
			task.setObject(param_offerID, offers[0].getOfferID());
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
			task.setObject(param_responseSms, getSMSTextForID(task,
					TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber
							.getLanguage()));
			return;
		}

		task.setObject(param_subclass, subscriptionClass);
		task.setObject(param_alreadyGetBaseOffer, true);

		// calling selection offer, if smsText has song promo code
		if (promoId != null) {

			try {
				Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
						subscriber.getSubscriberID(), "TNB",
						Offer.OFFER_TYPE_SELECTION, userType, null, null);
				if (offers == null || offers.length == 0) {
					task.setObject(param_responseSms, getSMSTextForID(task,
							TECHNICAL_FAILURE, m_technicalFailuresDefault,
							subscriber.getLanguage()));
					return;
				}
				String chargeClass = offers[0].getSrvKey();
				String selOfferId = offers[0].getOfferID();
				task.setObject(param_sel_offerID, selOfferId);
				task.setObject(param_chargeclass, chargeClass);
				task.setObject(param_USE_UI_CHARGE_CLASS, true);
				task.setObject(param_alreadyGetSelOffer, true);
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
				task.setObject(param_responseSms, getSMSTextForID(task,
						TECHNICAL_FAILURE, m_technicalFailuresDefault,
						subscriber.getLanguage()));
				return;
			}
		}
		logger.debug("Task ::::: " + task);
		super.processTNBActivation(task);
	}

	@Override
	public void processVoucherRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					VOUCHER_FAILURE_ALREADY_ACTIVE,
					m_voucherFailureAlreadyActiveDefault, subscriber
							.getLanguage()));
			return;
		}

		super.processVoucherRequest(task);
	}

	
	/*
	 * RBT-4549: SMS Activation of music pack
	 * 
	 */
	public void processMusicPack(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		// Checking clip expired or not.
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		if (smsList == null || smsList.size() == 0) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					invalid_sms, getSMSTextForID(task, HELP_SMS_TEXT,
							m_helpDefault, subscriber.getLanguage()),
					subscriber.getLanguage()));
			return;
		}
		
		com.onmobile.apps.ringbacktones.webservice.client.beans.Offer packOffer = getPackOffer(task);
		getCategoryAndClipForPromoID(task, smsList.get(0));
		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		if (clip == null
				|| clip.getClipEndTime().getTime() < System.currentTimeMillis()) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					CLIP_EXPIRED_SMS_TEXT, getSMSTextForID(task, HELP_SMS_TEXT,
							m_helpDefault, subscriber.getLanguage()),
					subscriber.getLanguage()));
			return;
		}

		/*
		 * assign base offer, if subscriber is new user or de-active
		 */
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER) || status
				.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {
			com.onmobile.apps.ringbacktones.webservice.client.beans.Offer baseOffer = getBaseOffer(task);
			if (baseOffer != null) {
				task.setObject(param_offerID, baseOffer.getOfferID());
				extraInfoMap.put("BASE_OFFERID", baseOffer.getOfferID());
			}
		}

		// If offer is present in the configured music pack.
		if (null != packOffer) {
			String offerId = packOffer.getOfferID();
			extraInfoMap.put(WebServiceConstants.param_packOfferID, offerId);
			task.setObject(WebServiceConstants.param_packOfferID, offerId);
			// Get the music pack cos id from the configuration and store it.
			String cosId = getParamAsString(SMS, "MUSIC_PACK_OFFER_COS_ID",
					null);
			extraInfoMap.put(param_COSID, cosId);
			task.setObject(param_COSID, cosId);
		} else {
			// Get the non-music pack cos id from the configuration and store
			// it.
			String cosId = getParamAsString(SMS, "MUSIC_PACK_COS_ID", null);
			extraInfoMap.put(param_COSID, cosId);
			task.setObject(param_COSID, cosId);
		}

		CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_COSID));
		if (cos != null
				&& cos.getContentTypes() != null && !Arrays.asList(
						cos.getContentTypes().split(",")).contains(
								clip.getContentType()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
							"OPT_IN_CONFIRMATION_NON_MUSIC_PACK_CONTENT_SMS",
							m_optInConfirmationMusicPackSMS,
							subscriber.getLanguage()));
			return;
		}

		logger.debug("Configuration for SMS_CONFIRMATION_ON: "
				+ isSmsConfirmationOn);

		if (isSmsConfirmationOn) {
			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			String selectedBy = task.getString(param_actby);
			logger.debug("Since confirmation is required"
					+ ", inserting data into viral sms table");
			rbtDBManager.insertViralSMSTable(subscriber.getSubscriberID(),
					new Date(), "SMSCONFPENDING", task
							.getString(param_callerid), String.valueOf(clip
							.getClipId()), 0, selectedBy, null, extraInfo);

			String language = subscriber.getLanguage();
			String smsText = getSMSTextForID(task,
					"OPT_IN_CONFIRMATION_MUSIC_PACK_SMS", m_optInConfirmationMusicPackSMS,
					language);
			
			String sms = getSubstituedSMS(smsText, null,
					waitTimeForConfirmation, null, null, null);
			logger.debug("After inserting data into viral sms table. Sending SMS text: "
							+ sms);

			task.setObject(param_responseSms, sms);
			sendSMS(task);
			return;
		}

		logger.debug("Since double confirmation is not required,"
				+ " processing selection directly");
		
		String response = processSetSelection(task);
		logger.info("processSetSelection response: " + response);
		
		verfifyResponse(task, subscriber, false, false, response, false);

		sendSMS(task);
	}
	
	/**
	 * Insert subscriber details into pending confirmation reminder
	 * table. PendingConfirmationsRemainder daemon will pick these
	 * records and send reminders to the subscriber.
	 *  
	 * @param subscriberId
	 * @param type
	 * @param sentTime
	 * @param reminderText
	 * @param sender
	 */
	protected void addToPendingConfirmationReminder(String subscriberId,
			String type, Date sentTime,
			String reminderText) {
		logger.debug("Getting viral data for subcriber: "+subscriberId);
		
		// Get the recent viral data for the given subscriber.
		ViralSMSTable[] viralData = rbtDBManager.getViralSMSes(subscriberId,
				null, type, null, sentTime);

		/*
		 * Update reminders left to -2 for previous requests to the old
		 * requests, so the daemon will send reminders to only recent
		 * requests.
		 */
		updatePendingConfirmationReminder(subscriberId, -2);
		
		if (null != viralData && viralData.length > 0) {
			DataRequest dataRequest = new DataRequest(null);
			dataRequest.setSubscriberID(subscriberId);
			// Initially last reminder sent and SMS received time will be same
			dataRequest.setLastReminderSent(sentTime);
			dataRequest.setRemindersLeft(numberOfReminders);
			dataRequest.setSmsReceivedTime(sentTime);
			dataRequest.setReminderText(reminderText);
			dataRequest.setSmsID(viralData[0].getSmsId());
			dataRequest.setSender(remindersSender);
			PendingConfirmationsRemainder response = rbtClient
					.addPendingConfirmationsRemainder(dataRequest);
			logger.debug("Successfully inserted. dataRequest: " + dataRequest
					+ ", response: " + response);
		} else {
			logger.debug("No viral data found for : "+subscriberId+", type: "+type);
		}
	}

	/**
	 * 
	 * @param reminderText
	 * @param sentTime
	 * @return
	 */
	private String convertDateToString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(reminderTextDateFormat);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, Integer.parseInt(waitTimeForConfirmation));
		return dateFormat.format(c.getTime());
	}

	/**
	 * To stop sending reminders, update reminders left either -1 or -2, Pending
	 * confirmations reminder daemon will pick only for the reminders left is
	 * greater than 0. The remindersLeft will be set to -1 when subscriber sends
	 * back confirmation and it will be set to -2 when the subscriber is making
	 * more than one selection.
	 * 
	 * @param subscriberId
	 * @param remindersLeft
	 */
	protected void updatePendingConfirmationReminder(String subscriberId,
			int remindersLeft) {

		int count = rbtDBManager.updatePendingConfirmationReminder(
				subscriberId, remindersLeft, new Date());

		TRANSACTION_LOG.info("SUBSCRIBER_ID:" + subscriberId
				+ "|REMINDERS_LEFT:" + remindersLeft + "|RECORDS_UPDATED:"
				+ count);

		logger.debug("Updated remindersLeft: " + remindersLeft
				+ " for subscriberId: " + subscriberId + ", records pdated: "
				+ count);
	}
	
	public void processXbiPack(Task task) {

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		boolean isUserActive = isUserActive(subscriber.getStatus());
		boolean isActRequest = Boolean.valueOf(task
				.getString(IS_ACTIVATION_REQUEST));
		boolean isDirectActRequest=  false ;
		task.setObject(param_SMSTYPE, SONG_PACK_REQUEST);
		ViralData[] viralDataArray = getViraldata(task);
		String xbiChargeSubClassMap = RBTParametersUtils.getParamAsString(SMS,
				XBI_CHARGE_SUB_CLASS_MAPPING, null);
		logger.info("xbiChargeSubClassMap :" + xbiChargeSubClassMap);
		HashMap<String, String> catIdRentalPack = (HashMap<String, String>) MapUtils.convertIntoMap(xbiChargeSubClassMap, ";", ":", null);
		if (task.containsKey(param_isdirectact)) {
			isDirectActRequest = Boolean.valueOf(task
					.getString(param_isdirectact));
		}
		String expiredResponse = null;
		if ((viralDataArray != null && viralDataArray.length > 0)) {
			logger.debug("got an entry in the viral sms table for smsType");
			ViralData viralData = viralDataArray[0];
			int catId = Integer.parseInt(viralData.getClipID());
			Category category = rbtCacheManager
					.getCategory(catId);
			String catClassType = category.getClassType();
			if (category != null
					&& catIdRentalPack.containsKey(catClassType)) {
				task.setObject(CAT_OBJ, category);
				task.setObject(param_isXBIUser, "true");
				if (category.getCategoryEndTime().getTime() < System
						.currentTimeMillis()) {
					expiredResponse = WebServiceConstants.CATEGORY_EXPIRED;
					logger.warn("Content expired...expiredResponse: "
							+ expiredResponse);
					if (expiredResponse != null) {
						task.setObject(
								param_responseSms,
								getSMSTextForID(task,
										expiredResponse.toUpperCase(),
										m_clipOrCategoryExpiredDefault));
						return;
					}
				}
				String subClass = catIdRentalPack.get(catClassType);
				task.setObject(param_USE_UI_CHARGE_CLASS, true);
				task.setObject(param_rentalPack, subClass);
				task.setObject(param_catid, category.getCategoryId()+"");
				task.setObject(param_chargeclass, catClassType);
				String respnse = processSetSelection(task);
				logger.info("Response after making xbi selection :" + respnse) ;
				boolean combo = isUserActive ? false : true;
				verfifyResponse(task, subscriber, isActRequest, isDirectActRequest, respnse, combo);
				removeViraldata(task);
				return;

			}

		} else {
			logger.error("the request session expired as there are no entry in the viral sms table");
			return;
		}

	}

	//TIM-88
	public boolean isAycePackUser(Subscriber subscriber) {
		String timUserSubClass = RBTParametersUtils.getParamAsString("SMS",
				"AYCE_USER_SUBCLASS", null);
		List<String> timUserSubClassList = new ArrayList<String>();
		if (timUserSubClass != null && !timUserSubClass.isEmpty()) {
			timUserSubClassList = Arrays.asList(timUserSubClass.split(","));
		}
		if (timUserSubClassList != null
				&& subscriber != null
				&& timUserSubClassList.contains(subscriber
						.getSubscriptionClass())) {
			return true;
		}

		return false;

	}
	//TIM-88
	public String getUpgradeSubClassForAyce(boolean isAycePack,
			Subscriber subscriber) {
		String UpgradeSubClass = null;
		if (isAycePack) {
			String timUserSubClassMapping = RBTParametersUtils
					.getParamAsString("SMS",
							"SUBSCRIPTION_CLASS_MAPPING_FOR_AYCE_USERS", null);
			HashMap<String, String> timUserSubClassMappingMap = new HashMap<String, String>();

			if (timUserSubClassMapping != null
					&& !timUserSubClassMapping.isEmpty()) {
				timUserSubClassMappingMap = (HashMap<String, String>) MapUtils
						.convertIntoMap(timUserSubClassMapping, ",", "=", null);
			}

			if (timUserSubClassMappingMap != null
					&& !timUserSubClassMappingMap.isEmpty()
					&& subscriber != null
					&& timUserSubClassMappingMap.containsKey(subscriber
							.getSubscriptionClass())) {

				UpgradeSubClass = timUserSubClassMappingMap.get(subscriber
						.getSubscriptionClass());
			}

		}
		return UpgradeSubClass;
	}
	
	
	public boolean isSelectionAlreadyPresent(Clip clip, String subscriberID , Category category , Task  task) {
		Map<String, String> whereClauseMap = new HashMap<String, String>();
		if (category != null && Utility.isShuffleCategory(category.getCategoryTpe())) {
			whereClauseMap.put("CATEGORY_ID", category.getCategoryId() + "");
		} else if(clip != null && clip.getClipRbtWavFile()!= null){
			whereClauseMap.put("SUBSCRIBER_WAV_FILE", clip.getClipRbtWavFile());
		}
		
		
		int fromHrs = 0;
		int toHrs = 23;
		int fromMinutes = 0;
		int toMinutes = 59;
		
		whereClauseMap.put("FROM_TIME", (ServiceUtil.getTime(fromHrs, fromMinutes)));
		whereClauseMap.put("TO_TIME", ServiceUtil.getTime(toHrs, toMinutes));
		whereClauseMap.put("CALLER_ID", null);
		whereClauseMap.put("SEL_INTERVAL" , null);
		
		SubscriberStatus subscriberStatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subscriberID, whereClauseMap); 
		if(subscriberStatus != null){
			return true ;
		}
		
		if (task.containsKey(param_fromTime))
			fromHrs = Integer.parseInt(task.getString(param_fromTime));
		
		if (task.containsKey(param_toTime))
			toHrs = Integer.parseInt(task.getString(param_toTime));
		
		if (task.containsKey(WebServiceConstants.param_toTimeMinutes))
			toMinutes = Integer.parseInt(task.getString(WebServiceConstants.param_toTimeMinutes));
		
		if (task.containsKey(WebServiceConstants.param_fromTimeMinutes))
			fromMinutes = Integer.parseInt(task.getString(WebServiceConstants.param_fromTimeMinutes));
		
		String fromTime = ServiceUtil.getTime(fromHrs, fromMinutes);
		String toTime = ServiceUtil.getTime(toHrs, toMinutes);
		
		whereClauseMap.put("FROM_TIME", fromTime);
		whereClauseMap.put("TO_TIME", toTime);
		
		if(task.containsKey(param_status)) {
			whereClauseMap.put("STATUS", task.getString(param_status));
		}
		
		String callerId = (!task.containsKey(WebServiceConstants.param_callerid) || task
				.getString(WebServiceConstants.param_callerid).equalsIgnoreCase(WebServiceConstants.ALL)) ? null
				: task.getString(WebServiceConstants.param_callerid);
		whereClauseMap.put("CALLER_ID", callerId);
		
		whereClauseMap.put("SEL_INTERVAL" , task.getString(WebServiceConstants.param_interval));
		
		subscriberStatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subscriberID, whereClauseMap); 
		if(subscriberStatus != null){
			return true ;
		}

		return false;

	}
	
	
}
 