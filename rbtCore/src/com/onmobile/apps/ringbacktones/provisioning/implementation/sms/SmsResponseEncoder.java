/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSText;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
public class SmsResponseEncoder extends ResponseEncoder
{
	protected static RBTClient rbtClient = null;
	protected Logger logger = null; 
	private static WriteDailyTrans smsTrans = null;
	String m_sdrWorkingDir = ".";
	
	protected static SMSText[] smsTexts = null;
	private Hashtable smsTable = null;
	
	//error responses
	public String invalidIPAddress = "Invalid IP Address";
	public String insufficientParameters = "Insufficient Parameters";
	
	//user invalid prefix error message
	public String invalidPrefix = "You are not authorized to use this service. We apologize the inconvenience";
	//user black-listed error message
	public String blackListedSMSText = "Your number is in the black list. Plz call Customer care for further details";
	//user activation-pending error message
	public String activationPending = "Your activation request is in pending state. Plz wait for the SMS confirmation";
	//user deactivation-pending error message
	public String deactivationPending = "Your deactivation request is in pending state. Plz wait for the SMS confirmation";
	//user express-copy-pending error message
	public String expressCopyPending = "Your express copy request is still pending. Plz wait for the SMS confirmation";
	//user gifting-pending error message
	public String giftingPending = "You have been gifted a CRBT by your friend. The request is still pending.";
	//user renewal-pending error message
	public String renewalPending = "Your request for renewal is under process. Please try after sometime";
	//user suspended error message
	public String suspended = "Dear subscriber, your account has been temporarily suspended";
	//technical failure error message
	public String technicalFailure = "I am sorry, we are having some technical difficulties. Please try later";
	//access failure error message
	public String accessFailure = "Dear subscriber, you are not authorised to access the service.";
	//not active user message
	public String notActiveText = "Dear User, You are not subscribed to Welcome Tunes service, to activate send SUB to 12800";
	//already active user message
	public String alreadyActive = "Dear User, You are already subscribed to Welcome Tunes service";
	//user black-listed error message
	public String technicalErrorSMSText = "I am sorry, we are having some technical difficulties. Please try later";
	
	/* Selection related errors */
	//clip expired message
	public String clipExpired = "Sorry! The requested song is no longer available";
	//clip not available message
	public String clipNotAvailable = "Sorry!!! The requested song is not available";
	//song already exist message
	public String selAlreadyExists = "The song you requested is already set at your welcome tune";
	
	/* Retailer related errors */
	//if non retailer sends retailer requests
	public String retailerNonRetailerSMS = "Dear subscriber, you are not authorised to send retailer requests.";
	//No results for retailer search
	public String retailerSearchNoResultsSMS = "No matches found. SMS TT <SONG NAME> to search another song";
	//retailer user access failure error message
	public String retailerUserAccessFailureSMS = "Dear retailer, The number you sent is not authorised to access the service.";
	//retailer user invalid prefix
	public String retailerUserInvalidNumSMS = "Dear retailer, the user number you sent is invalid. Plz check.";
	//retailer sent activation req for already active user
	public String retailerUserAlreadyActiveSMS = "Dear retailer, the user is already active.";
	//retailer user only selection failed sms
	public String retailerSelAloneFailureSMS = "Dear retailer, invalid code %S for subscriber %C"; 
	//retailer request already exists
	public String retailerRequestExistsSMS = "Dear retailer, Same request already exists for subscriber %C";
	//retailer user selection already exists
	public String retailerSelectionExistsSMS = "Dear retailer, song %S already exists for subscriber %C";
	public String retailerSubActPendingSMS = "";
	public String retailerSubDeactPendingSMS = "";
	public String retailerSubSuspendedSMS = "";
	public String retailerSubBlackListedSMS = "";
	public String retailerSubTechnicalErrorSMS = "";
	//retailer sent invalid ret pack code
	private String retailerInvalidRetCodeSMS = "The COS code sent is invalid";

	
	//Activation success message
	public String activationSuccess = "Your request has been received. You will be activated in the next 24 hours";
	//Deactivation success message
	public String deactivationSuccess = "Your request has been received. You will be deactivated in the next 24 hours";
	//Selection success message
	public String selectionSuccess = "The song you requested will be set as your welcome tune";
	//Retailer success messages
	public String retailerSearchSuccess = ".To set one of the above send RET <SONGID> <SUBID> to 12900.";
	//Retailer success messages with more
	public String retailerSearchSuccess2 = ".To set one of the above RET <PROMOID> <SUBID>,SMS MORE to 12900 for more songs.";
	//Invalid sms reply
	public String retailerSmsFailure = "Invalid Number. Pls send TT RET <PROMOID> <SUBID> to set one of your previous request. SMS TT <SONG NAME> to search another song";
	//retailer subscription success
	public String retailerSubSMS = "Dear retailer, we received your request to activate %C.";
	//retailer sub success sel failed
	public String retailerOnlySubSuccessSMS = "Dear retailer, we received your request to activate %C. The song request failed.";
	//retailer sleection success
	public String retailerOnlySelSMS = "Dear retailer, we received your request to download song %S for subscriber %C";
	//retailer request success
	public String retailerRequestSuccessSMS = "Dear retailer, we received your request to activate %C and set the song %S";
	//retailer user accept success
	public String retailerRequestAcceptSMS = "Dear retailer, The request for subscriber %C has been processed";
	//retailer sub request alone with pack success
	private String retailerSubSuccessAloneRetPackSMS = "Dear retailer, we received your request to activate %C. For Pack %S";

	
	//Retailer - To User messages
	//retailer user already active
	public String retailerUserActiveSMSToUser = null;
	//retailer subscription success
	public String retailerOnlySubSMSToUser = "Dear user, we received your request to activate %C";
	//retailer sub success sel failed
	public String retailerOnlySubSuccessSMSToUser = "Dear user, we received your request to activate %C. The song request failed.";
	//retailer sleection success
	public String retailerOnlySelSMSToUser = "Dear user, we received your request to download song %S for subscriber %C";
	//retailer request success
	public String retailerRequestSuccessSMSToUser = "Dear user, we received your request to activate %C and set the song %S....";
	//retailer user accept success
	public String retailerRequestAcceptSMSToUser = "Dear user, your request has been processed";
	//retailer user only selection failed sms
	public String retailerSelAloneFailureSMSToUser = null;
	//retailer user selection already exists
	public String retailerSelectionExistsSMSToUser = null;
	//retailer sub request alone with pack success
	private String retailerSubSuccessAloneRetPackSMSToUser = null;

	//Loop 
	public String loopInvalidSub = "Dear subscriber, you have to be subscribed to WT service to send this Keyword";
	public String loopNoDownloads = "Dear subscriber, you have no active downloads to set in loop";
	public String loopSuccess = "Your request to set all songs in your library in loop has been accepted";
	public String loopFailure = "Your request to set all songs in your library in loop failed";
	
	//Delete
	public String delInvalidUnsub = "Dear Subscriber, you are not yet subscribed to the Service.";
	public String delNoDownload = "Dear Subscriber, you have no active downloads.";
	public String delSuccess1 = "Dear subscriber, following is the list of downloads";
	public String delSuccess2 = ". To delete a song SMS DELETE <song number> to 12800. SMS DELETE MORE to get more songs from you library.";
	public String delSuccess3 = ". To delete a song SMS DELETE <song number> to 12800.";
	public String delMoreInvalid = "Dear subscriber, there are no more delete records.";
	public String delNumInvalid = "Dear subscriber, the song number you have sent is invalid";
	public String delCodeInvalid = "Dear subscriber, the song code you have sent is invalid";
	public String delSongDeleted = "Dear subscriber, request to delete the song %S already received";
	public String delSuccess = "Dear subscriber, your request to delete %S has been accepted";
	public String delFailure = "Dear subscriber, your request to delete %S failed";

	//OBD
	public String obdInvalidToneCode = "Invalid song code";
	
	//Search related messages
	//setting already exists
	public String settingExists = "The requested setting already exists";
	//more rbt
	public String moreRbtFailure1 = "Sorry, there are no search results. SMS <SONG NAME> to search a song";
	public String confirmBulKActivationPrepSMS = "Downloading song will be considered as your confirmation to continue the service after the offer period you will be charged Rs. 15/- for 15 days.";
	public String confirmBulKActivationPostSMS = "Downloading song will be considered as your confirmation to continue the service after the offer period you will be charged Rs. 30/- per month.";

	//error
	public String deactivationFailure = "You have already sent a deactivation request to this service";
	public String activationFailure = "You have already sent a activation request to this service";
	
	//profile
	public String temporaryOverrideSuccess = "Your profile has been set. It will be reset to your original tune in 1 hr by default, or after the time specified by you";
	public String temporaryOverrideFailure = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
	public String temporaryOverrideListSuccess = "Your profile has been set. It will be reset to your original tune in 1 hr by default, or after the time specified by you";
	public String temporaryOverrideListFailure = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
	public String temporaryOverrideRemovalSuccess = "Your profile has removed successfully";
	public String temporaryOverrideRemovalFailure = "There is no profile to be removed";
	public String temporaryOverrideRemovalError = "You are not subscribed to this service";
	
	//category alias
	private String categoryNoClips = "Dear subscriber, there are no songs for the choosen category %S";
	public String requestRbtFailure1 = "No matches found. SMS CT FIND <SONG NAME> to search another song";
	public String requestRbtFailure2 = "Invalid Number. Pls send CT FIND 1-3 to set one of your previous request. SMS CT FIND <SONG NAME> to search another song";
	public String requestRbtFailure3 = "Your activation request has been accepted. No matches found for the search word.";
	public String requestRbtSuccess1 = ".To set one of the above send CT FIND <SONG NO> to 12900.";
	public String requestRbtSuccess2 = "As per your request %S has been set as your Caller Tune";
	public String requestRbtSuccess3 = ".To set one of the above SMS SONG NO,SMS MORE to 12900 for more songs.";
	//clip alias
	public String smsAliasClipInvalid = "Dear subscriber, the requested song %S is not a valid song";
	public String smsAliasClipExpired = "Dear subscriber, the requested song %S is expired";
	public String smsAliasOnlySelSuccess = "Dear subscriber, you request to set %S for %C has been accepted";
	public String smsAliasSuccess = "Dear subscriber, your request to subscribe to Tring Tunes and set %S for %C has been accepted";
	//category search
	public String catSearchResultSMS = "SMS the WT code to 12800 to select songs of your choice.SMS WT%smsAlias%MORE to 12800 for more";
	public String clipSearchResultSMS= "for your callers, Just SMS the WT code to 12800. SMS WT%smsAlias%MORE to 12800 for more.SMS:Rs3 Song:Rs10.";
	public String invalidrequestSMS = "Invalid Input";
	//viral
	public String viralFailure = "Your viral request is invalid";
	public String viralSuccess = "Your viral request has been accepted";
	//copy cancel
	public String copyCancelSuccessDefault = "Your request to cancel the Copy has been processed successfully";
	public String copyCancelFailureDefault = "No press Star copy request are present for you to cancel";
	//copy confirm
	public String copyConfirmSuccessDefault = "The copy reqest will be processed."; 
    public String copyConfirmFailureDefault = "The copy reqest will not be processed."; 
	
	
	// The help message
	public String helpMessage = "Usage Keyword <Duration in hours>. Keyword(s) are SUB, UNSUB, WT";
	// Subscribed and unsubscribed help message
	
	public String subhelpMessage = "Usage Keyword <Duration in hours>. Keyword(s) are SUB, UNSUB, WT";
	public String unsubhelpMessage = "Usage Keyword <Duration in hours>. Keyword(s) are SUB, WT";

	
	
	/**
	 * 
	 * VirginSmsProcessor and TATCDMAProcessor uses this response encoder
	 * All others use RWSmsResponseEncoder 
	 * @throws Exception 
	 * 
	 */
	public SmsResponseEncoder() throws Exception
	{
		logger = Logger.getLogger(SmsResponseEncoder.class);
		rbtClient = RBTClient.getInstance();
		
		String dir = getSMSParameter("SDR_WORKING_DIR");
		if (dir != null)
			m_sdrWorkingDir = dir;
		ArrayList<String> headers = new ArrayList<String> ();
		headers.add("REQUEST PARAMS");
		headers.add("RESPONSE");
		headers.add("TIME DELAY");
		headers.add("REQ IP");
		smsTrans = new WriteDailyTrans(m_sdrWorkingDir, "SMS_REQUEST", headers);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#encode(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String encode(Task task)
	{
		String taskAction = task.getTaskAction();
		String response = task.getString(param_response);
		String retResponse = task.getString(param_retailer_response);
		String obdResponse = task.getString(param_obd_response);
		String subID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subType = "UNKNOWN";
		String clipName = task.getString(param_clipName);
		String promoID = task.getString(param_promoID);
		String callerID = task.getString(param_callerid);
		String queryString = task.getString(param_queryString);
		Long startTime = 0l;
		if (task.containsKey(param_startTime))
			startTime = Long.parseLong(task.getString(param_startTime));
		if (callerID == null)
			callerID = "ALL";
		
		//below parameters are used for OBD 
		String obdReportPath = null;
		int logRotationSize = 0;
		String requestDetail = "NA";
		String requestedTimeString = "";
		
		boolean isRetailerRequest = false;
		boolean isRetailerAccept = false;
		boolean isOBDRequest = false;
		String obdRequestResponse = "OBD Request failed ";
		if (task.containsKey(param_isOBDRequest) && task.getString(param_isOBDRequest).equalsIgnoreCase("true")){
			isOBDRequest = true;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			requestedTimeString = formatter.format(new Date());
			obdReportPath = getCOMMONParameter(OBD_REPORT_PATH)+ "\\RBT_OBD_REQUEST_FAILURE";
			try {
				logRotationSize = Integer.parseInt(getCOMMONParameter(OBD_REPORT_ROTATION_SIZE));
			} catch (NumberFormatException e) {
				logRotationSize = 0;
			}
			if (subscriber.isPrepaid())
				subType = "PREPAID";
			else 
				subType = "POSTPAID";
		}
		
		if (task.containsKey(param_isRetailerRequest) && task.getString(param_isRetailerRequest).equalsIgnoreCase("true")){
			isRetailerRequest = true;
		}
		if (task.containsKey(param_isRetailerAccept) && task.getString(param_isRetailerAccept).equalsIgnoreCase("true")){
			isRetailerAccept = true;
		}
		
		
		helpMessage = getSms(BPST_HELP, SUCCESS, helpMessage);
		
		String sms = helpMessage;
		String smsForRetailer = helpMessage;
		
		Boolean sendSMS = (Boolean)task.getObject(param_send_sms_to_user);
		boolean sendSMSToUser = true;
		boolean sendSMSToRetailer = false;
		if (sendSMS != null)
			sendSMSToUser = sendSMS;
		
		sendSMS = (Boolean)task.getObject(param_send_sms_to_retailer);
		if (sendSMS != null)
			sendSMSToRetailer = sendSMS;
		
		if (taskAction == null){
			//error conditions
			if (response != null){
				if (response.equalsIgnoreCase(ACCESS_FAILURE)){
					sms = getSms(BPST_ERROR, ACCESS_FAILURE, accessFailure);
				}else if (response.equalsIgnoreCase(INVALID)){
					sms = getSms(BPST_PREFIX, INVALID, invalidPrefix);
				}else if (response.equalsIgnoreCase(ACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, ACTIVATION_PENDING, activationPending);
				}else if (response.equalsIgnoreCase(DEACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, DEACTIVATION_PENDING, deactivationPending);
				}else if (response.equalsIgnoreCase(SUSPENDED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, SUSPENDED, suspended);
				}else if (response.equalsIgnoreCase(BLACK_LISTED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, BLACK_LISTED, blackListedSMSText);
				}else if (response.equalsIgnoreCase(TECHNICAL_ERROR)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, TECHNICAL_ERROR, technicalErrorSMSText);
				}else if (response.equalsIgnoreCase(NO_SMS)){
					;
				}else if (response.equalsIgnoreCase(INVALID_IP_ADDRESS)){
					sms = getSms(BPST_ERROR, INVALID_IP_ADDRESS, invalidIPAddress);
				}else if (response.equalsIgnoreCase(INSUFFICIENT_PARAMETERS)){
					sms = getSms(BPST_ERROR, INSUFFICIENT_PARAMETERS, insufficientParameters);
				}else if (response.equalsIgnoreCase(HELP)){
					// send help message - already done
				}
			}
			if (retResponse != null){
				if (retResponse.equalsIgnoreCase(NON_RETAILER)){
					smsForRetailer = getSms(BPST_RETAILER_USER, NON_RETAILER, retailerNonRetailerSMS);
				}else if (retResponse.equalsIgnoreCase(ACCESS_FAILURE)){
					smsForRetailer = getSms(BPST_RETAILER_USER, ACCESS_FAILURE, retailerUserAccessFailureSMS);
				}else if (retResponse.equalsIgnoreCase(INVALID)){
					smsForRetailer = getSms(BPST_RETAILER_USER, INVALID, retailerUserInvalidNumSMS);
				}else if (retResponse.equalsIgnoreCase(ACTIVATION_PENDING)){
					smsForRetailer = getSms(BPST_RETAILER_USER, ACTIVATION_PENDING, retailerSubActPendingSMS);
				}else if (retResponse.equalsIgnoreCase(DEACTIVATION_PENDING)){
					smsForRetailer = getSms(BPST_RETAILER_USER, DEACTIVATION_PENDING, retailerSubDeactPendingSMS);
				}else if (retResponse.equalsIgnoreCase(SUSPENDED)){
					smsForRetailer = getSms(BPST_RETAILER_USER, SUSPENDED, retailerSubSuspendedSMS);
				}else if (retResponse.equalsIgnoreCase(BLACK_LISTED)){
					smsForRetailer = getSms(BPST_RETAILER_USER, BLACK_LISTED, retailerSubBlackListedSMS);
				}else if (retResponse.equalsIgnoreCase(TECHNICAL_ERROR)){
					smsForRetailer = getSms(BPST_RETAILER_USER, TECHNICAL_ERROR, retailerSubTechnicalErrorSMS);
				}else if (retResponse.equalsIgnoreCase(HELP)){
					// send help message - already done
				} 
				smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%C",subID);
			}
			
			if (obdResponse != null){
				if (obdResponse.equalsIgnoreCase("INVALID_DATA") 
						|| obdResponse.equalsIgnoreCase("INVALID_OBD_REQUEST") || obdResponse.equalsIgnoreCase(TECHNICAL_ERROR)){
					logger.info("obdResponse : Invalid data");
					subID = "UNKNOWN";
				}else if (!(obdResponse.equalsIgnoreCase("SUBSCRIBER_NOT_AUTHORIZED")||(obdResponse.equalsIgnoreCase("INVALID_SUBSCRIBER"))) 
						&& subscriber!= null){
					if (subscriber.isPrepaid())
						subType = "PREPAID";
					else 
						subType = "POSTPAID";
				}
				WriteSDR.addToAccounting(obdReportPath, logRotationSize,"RBT_OBD_REQUEST", subID, subType,
						"obd_request", obdResponse, requestedTimeString,"NA", "RBT_SMS", requestDetail, "NA");
				
				return obdRequestResponse + obdResponse;
			}
		}else if (taskAction.equalsIgnoreCase(action_activate)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_ACTIVATION, SUCCESS, activationSuccess);
				}else if (response.equalsIgnoreCase(SUSPENDED)){
					sms = getSms(BPST_ACTIVATION, SUSPENDED, suspended);
				}else if (response.equalsIgnoreCase(ALREADY_ACTIVE)){
					sms = getSms(BPST_ACTIVATION, ALREADY_ACTIVE, alreadyActive);
				}else if (response.equalsIgnoreCase(ACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, ACTIVATION_PENDING, activationPending);
				}else if (response.equalsIgnoreCase(DEACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, DEACTIVATION_PENDING, deactivationPending);
				}else if (response.equalsIgnoreCase(GIFTING_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, GIFTING_PENDING, giftingPending);
				}else if (response.equalsIgnoreCase(RENEWAL_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, RENEWAL_PENDING, renewalPending);
				}else if (response.equalsIgnoreCase(BLACK_LISTED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, BLACK_LISTED, blackListedSMSText);
				}else if (response.equalsIgnoreCase(INVALID)){
					sms = getSms(BPST_PREFIX, INVALID, invalidPrefix);
				}else if (response.equalsIgnoreCase(EXPRESS_COPY_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, EXPRESS_COPY_PENDING, expressCopyPending);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_deactivate)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_DEACTIVATION, SUCCESS, deactivationSuccess);
				}else if (response.equalsIgnoreCase(NOT_ACTIVE)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, NOT_ACTIVE, notActiveText);
				}else if (response.equalsIgnoreCase(ACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, ACTIVATION_PENDING, activationPending);
				}else if (response.equalsIgnoreCase(DEACTIVATION_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, DEACTIVATION_PENDING, deactivationPending);
				}else if (response.equalsIgnoreCase(GIFTING_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, GIFTING_PENDING, giftingPending);
				}else if (response.equalsIgnoreCase(RENEWAL_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, RENEWAL_PENDING, renewalPending);
				}else if (response.equalsIgnoreCase(BLACK_LISTED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, BLACK_LISTED, blackListedSMSText);
				}else if (response.equalsIgnoreCase(INVALID)){
					sms = getSms(BPST_PREFIX, INVALID, invalidPrefix);
				}else if (response.equalsIgnoreCase(EXPRESS_COPY_PENDING)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, EXPRESS_COPY_PENDING, expressCopyPending);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_selection)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_SELECTION, SUCCESS, selectionSuccess);
				}else if (response.equalsIgnoreCase(SUSPENDED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, SUSPENDED, suspended);
				}else if (response.equalsIgnoreCase(NOT_AVAILABLE)){
					sms = getSms(BPST_CLIP, NOT_AVAILABLE, clipNotAvailable);
				}else if (response.equalsIgnoreCase(ALREADY_EXISTS)){
					sms = getSms(BPST_SELECTION, ALREADY_EXISTS, selAlreadyExists);
				}else if (response.equalsIgnoreCase(EXPIRED)){
					sms = getSms(BPST_CLIP, EXPIRED, clipExpired);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}
				if (clipName != null)
					sms = Utility.findNReplaceAll(sms,"%S",clipName);
				else
					sms = Utility.findNReplaceAll(sms,"%S","");
			}
		}else if (taskAction.equalsIgnoreCase(action_viral)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_VIRAL, SUCCESS, viralSuccess);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_VIRAL, FAILURE, viralFailure);
				}
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_retailer_search)){
			if (retResponse != null){
				if (retResponse.equalsIgnoreCase(SUCCESS)){
					String searchResults = task.getString(param_search_results);
					smsForRetailer = searchResults + getSms(BPST_RETAILER_SEARCH, SUCCESS, retailerSearchSuccess);
				}else if (retResponse.equalsIgnoreCase(SUCCESS2)){
					String searchResults = task.getString(param_search_results);
					smsForRetailer = searchResults + getSms(BPST_RETAILER_SEARCH, SUCCESS2, retailerSearchSuccess2);
				}else if (retResponse.equalsIgnoreCase(NO_RESULTS)){
					smsForRetailer = getSms(BPST_RETAILER_SEARCH, NO_RESULTS, retailerSearchNoResultsSMS);
				}else if (retResponse.equalsIgnoreCase(MORE_RBT_SMS1_FAILURE)){
					smsForRetailer =  getSms(BPST_SEARCH, MORE_RBT_SMS1_FAILURE, moreRbtFailure1);
				}else if (retResponse.equalsIgnoreCase(SMS_FAILURE)){
					smsForRetailer =  getSms(BPST_RETAILER_SEARCH, SMS_FAILURE, retailerSmsFailure);
				}//
			}
		}else if (taskAction.equalsIgnoreCase(action_retailer_accept)){
			if (response != null && response.equalsIgnoreCase(ACCEPT)){
				sms = getSms(BPST_RETAILER_SUBSCRIBER, ACCEPT, retailerRequestAcceptSMSToUser); 
				sms = Utility.findNReplaceAll(sms,"%C",task.getString(param_RetailerMSISDN));
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
			if (retResponse != null && retResponse.equalsIgnoreCase(ACCEPT)){
				smsForRetailer = getSms(BPST_RETAILER_REQUEST, ACCEPT, retailerRequestAcceptSMS);
				smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%C",subID);
				smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_retailer_request)){
			if (response != null){
				if (response.equalsIgnoreCase(ALREADY_ACTIVE)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, ALREADY_ACTIVE, retailerUserActiveSMSToUser);
				}else if (response.equalsIgnoreCase(SUBSCRIPTION)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, SUBSCRIPTION, retailerOnlySubSMSToUser);
				}else if (response.equalsIgnoreCase(ONLY_SUBSCRIPTION_SUCCESS)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, ONLY_SUBSCRIPTION_SUCCESS, retailerOnlySubSuccessSMSToUser);
				}else if (response.equalsIgnoreCase(SELECTION)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, SELECTION, retailerOnlySelSMSToUser);
//				}else if (response.equalsIgnoreCase(SELECTION_FAILURE)){
//					 sms = getSms(BPST_RETAILER_SUBSCRIBER, SELECTION_FAILURE, retailerSelAloneFailureSMSToUser);
//					 sms = Utility.findNReplaceAll(sms,"%S",promoID);
				}else if (response.equalsIgnoreCase(SELECTION_EXISTS)){
					 sms = getSms(BPST_RETAILER_SUBSCRIBER, SELECTION_EXISTS, retailerSelectionExistsSMSToUser);
					 sms = Utility.findNReplaceAll(sms,"%S",clipName);
				}else if (response.equalsIgnoreCase(SUBSCRIPTION_WITH_PACK_SUCCESS)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, SUBSCRIPTION_WITH_PACK_SUCCESS, retailerSubSuccessAloneRetPackSMSToUser);
					sms = Utility.findNReplaceAll(sms,"%S", task.getString(param_retPack));
				}else if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_RETAILER_SUBSCRIBER, SUCCESS, retailerRequestSuccessSMSToUser);
				}	
				sms = Utility.findNReplaceAll(sms,"%C",task.getString(param_RetailerMSISDN));
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
			if (retResponse != null){
				if (retResponse.equalsIgnoreCase(ALREADY_ACTIVE)){
					smsForRetailer = getSms(BPST_RETAILER_USER, ALREADY_ACTIVE, retailerUserAlreadyActiveSMS);
				}else if (retResponse.equalsIgnoreCase(SELECTION_FAILURE)){
					smsForRetailer = getSms(BPST_RETAILER, SELECTION_FAILURE, retailerSelAloneFailureSMS);
					smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S",promoID);
				}else if (retResponse.equalsIgnoreCase(SELECTION_EXISTS)){
					 smsForRetailer = getSms(BPST_RETAILER, SELECTION_EXISTS, retailerSelectionExistsSMS);
					 smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S",clipName);
				}else if (retResponse.equalsIgnoreCase(EXISTS)){
					smsForRetailer = getSms(BPST_RETAILER_REQUEST, EXISTS, retailerRequestExistsSMS);
				}else if (retResponse.equalsIgnoreCase(SUBSCRIPTION)){
					smsForRetailer = getSms(BPST_RETAILER, SUBSCRIPTION, retailerSubSMS);
				}else if (retResponse.equalsIgnoreCase(ONLY_SUBSCRIPTION_SUCCESS)){
					smsForRetailer = getSms(BPST_RETAILER, ONLY_SUBSCRIPTION_SUCCESS, retailerOnlySubSuccessSMS);
				}else if (retResponse.equalsIgnoreCase(SELECTION)){
					smsForRetailer = getSms(BPST_RETAILER, SELECTION, retailerOnlySelSMS);
				}else if (retResponse.equalsIgnoreCase(SUBSCRIPTION_WITH_PACK_SUCCESS)){
					smsForRetailer = getSms(BPST_RETAILER_REQUEST, SUBSCRIPTION_WITH_PACK_SUCCESS, retailerSubSuccessAloneRetPackSMS);
					smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S", task.getString(param_retPack));
				}else if (retResponse.equalsIgnoreCase(INVALID_RET_PACK_CODE)){
					smsForRetailer = getSms(BPST_RETAILER_REQUEST, INVALID_RET_PACK_CODE, retailerInvalidRetCodeSMS);
					smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S", task.getString(param_retPack));
				}else if (retResponse.equalsIgnoreCase(SUCCESS)){
					smsForRetailer = getSms(BPST_RETAILER_REQUEST, SUCCESS, retailerRequestSuccessSMS);
				}	
				smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%C",subID);
				smsForRetailer = Utility.findNReplaceAll(smsForRetailer,"%S",clipName);
			}
			
		}else if (taskAction.equalsIgnoreCase(action_obd)){
			if (obdResponse != null){				
				if (obdResponse.equalsIgnoreCase("SUBSCRIBER_EXPRESS_COPY_REQUEST_PENDING")||obdResponse.equalsIgnoreCase("SUBSCRIBER_RENEWAL_PENDING")){
					obdRequestResponse += obdResponse;//write sdr
					WriteSDR.addToAccounting(obdReportPath, logRotationSize,"RBT_OBD_REQUEST", subID, subType,
							"obd_request", obdResponse, requestedTimeString,"NA", "RBT_SMS", requestDetail, "NA");
				}else if (obdResponse.equalsIgnoreCase(INVALID_TONE_CODE)){
					requestDetail = "TONE CODE : "+promoID;
					sms = getSms(BPST_OBD, INVALID_TONE_CODE, obdInvalidToneCode);
					obdRequestResponse += INVALID_TONE_CODE; 
					WriteSDR.addToAccounting(obdReportPath, logRotationSize,"RBT_OBD_REQUEST", subID, subType,
							"obd_request", obdResponse, requestedTimeString,"NA", "RBT_SMS", requestDetail, "NA");
				}else if (obdResponse.equalsIgnoreCase(ACTIVATION_FAILURE)){
					//do nothing
				}else if (obdResponse.equalsIgnoreCase(SUCCESS)){
					obdRequestResponse = "OBD Request Successful";
					//do nothing
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_loop)){
			if (response != null){
				if (response.equalsIgnoreCase(INVALID)){
					sms = getSms(BPST_LOOP, INVALID, loopInvalidSub);
				}else if (response.equalsIgnoreCase(NO_DOWNLOADS)){
					sms = getSms(BPST_LOOP, NO_DOWNLOADS, loopNoDownloads);
				}else if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_LOOP, SUCCESS, loopSuccess);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_LOOP, FAILURE, loopFailure);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_delete)){
			String downloadsList = null;
			String anotherResponse = null;
			if (task.containsKey(param_downloads_list_for_sms))
				downloadsList = task.getString(param_downloads_list_for_sms);
			if (task.containsKey(param_another_response))
				anotherResponse = task.getString(param_another_response);
			
			if (anotherResponse != null){
				if (anotherResponse.equals(SUCCESS2))
					anotherResponse = getSms(BPST_DELETE, SUCCESS2, delSuccess2);
				if (anotherResponse.equals(SUCCESS3))
					anotherResponse = getSms(BPST_DELETE, SUCCESS3, delSuccess3);
			}
			
			if (response != null){
				if (response.equalsIgnoreCase(INVALID_SUB)){
					sms = getSms(BPST_DELETE, INVALID_SUB, delInvalidUnsub);
				}else if (response.equalsIgnoreCase(NO_DOWNLOADS)){
					sms = getSms(BPST_DELETE, NO_DOWNLOADS, delNoDownload);
				}else if (response.equalsIgnoreCase(SUCCESS1)){
					sms = getSms(BPST_DELETE, SUCCESS1, delSuccess1) + downloadsList + anotherResponse;
				}else if (response.equalsIgnoreCase(MORE_INVALID)){
					sms = getSms(BPST_DELETE, MORE_INVALID, delMoreInvalid);
				}else if (response.equalsIgnoreCase(NUMBER_INVALID)){
					sms = getSms(BPST_DELETE, NUMBER_INVALID, delNumInvalid);
				}else if (response.equalsIgnoreCase(CODE_INVALID)){
					sms = getSms(BPST_DELETE, CODE_INVALID, delCodeInvalid);
				}else if (response.equalsIgnoreCase(ALREADY_DELETED)){
					sms = getSms(BPST_DELETE, ALREADY_DELETED, delSongDeleted);
				}else if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_DELETE, SUCCESS, delSuccess);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_DELETE, FAILURE, delFailure);
				}
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_default_search)){
			if (response != null){
				if (response.equalsIgnoreCase(TEMPORARY_OVERRIDE_FAILURE)){
					sms = getSms(BPST_SEARCH, TEMPORARY_OVERRIDE_FAILURE, temporaryOverrideFailure);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS2_SUCCESS)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS2_SUCCESS, requestRbtSuccess2);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_FAILURE)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS1_FAILURE, requestRbtFailure1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS2_FAILURE)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS2_FAILURE, requestRbtFailure2);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_FAILURE)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS3_FAILURE, requestRbtFailure3);
				}else if (response.equalsIgnoreCase(CLIP_EXPIRED)){
					sms = getSms(BPST_CLIP, EXPIRED, clipExpired);
				}else if (response.equalsIgnoreCase(SETTING_EXISTS)){
					sms = getSms(BPST_SEARCH, SETTING_EXISTS, settingExists);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_SUCCESS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS1_SUCCESS, requestRbtSuccess1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_SUCCESS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS3_SUCCESS, requestRbtSuccess3);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS, requestRbtSuccess1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS, requestRbtSuccess3);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_SUCCESS_SUB_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS1_SUCCESS_SUB_COS, requestRbtSuccess1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS, requestRbtSuccess1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_SUCCESS_SUB_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS3_SUCCESS_SUB_COS, requestRbtSuccess3);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS, requestRbtSuccess3);
				}else if (response.equalsIgnoreCase(MORE_RBT_SMS1_FAILURE)){
					sms = getSms(BPST_SEARCH, MORE_RBT_SMS1_FAILURE, moreRbtFailure1);
				}else if (response.equalsIgnoreCase(ACTIVATION_FAILURE)){
					sms = getSms(BPST_ERROR, ACTIVATION_FAILURE, activationFailure);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}else if (task.containsKey(param_cosid)){
					String cosId = String.valueOf((Integer)task.getObject(param_cosid));
					sms = "";
					if (task.containsKey(param_search_results))
						sms = task.getString(param_search_results);
					sms = sms + getSms(BPST_SEARCH, response+cosId, "");
					sms = Utility.findNReplaceAll(sms,"%L",clipName);
				}
				sms = Utility.findNReplaceAll(sms,"%C",callerID);
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
			String anotherResponse = null;
			if (task.containsKey(param_another_response))
				anotherResponse = task.getString(param_another_response);
			
			if (anotherResponse != null){
				if (anotherResponse.equals(CONFIRM_BULK_ACTIVATION_PREP_SMS))
					sms += getSms(BPST_SEARCH, CONFIRM_BULK_ACTIVATION_PREP_SMS, confirmBulKActivationPrepSMS);
				if (anotherResponse.equals(CONFIRM_BULK_ACTIVATION_POST_SMS))
					sms += getSms(BPST_SEARCH, CONFIRM_BULK_ACTIVATION_POST_SMS, confirmBulKActivationPostSMS);
			}
			
			
		}else if (taskAction.equalsIgnoreCase(action_feed)){
			if (response != null){
				if (response.equalsIgnoreCase(FEED_FAILURE)){
					sms = task.getString(param_sms_for_user);
				}else if (response.equalsIgnoreCase(FEED_SUCCESS)){
					sms = task.getString(param_sms_for_user);
				}else if (response.equalsIgnoreCase(DEACTIVATION_FAILURE)){
					sms = getSms(BPST_ERROR, DEACTIVATION_FAILURE, deactivationFailure);
				} 
			}
		}else if (taskAction.equalsIgnoreCase(action_optin_copy_cancel)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_COPY_CANCEL, SUCCESS, copyCancelSuccessDefault);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_COPY_CANCEL, FAILURE, copyCancelFailureDefault);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_copy_confirm)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_COPY_CONFIRM, SUCCESS, copyConfirmSuccessDefault);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_COPY_CONFIRM, FAILURE, copyConfirmFailureDefault);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_clip_promo)){
			//Repeat processSelection responses
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_SELECTION, SUCCESS, selectionSuccess);
				}else if (response.equalsIgnoreCase(SUSPENDED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, SUSPENDED, suspended);
				}else if (response.equalsIgnoreCase(NOT_AVAILABLE)){
					sms = getSms(BPST_CLIP, NOT_AVAILABLE, clipNotAvailable);
				}else if (response.equalsIgnoreCase(ALREADY_EXISTS)){
					sms = getSms(BPST_SELECTION, ALREADY_EXISTS, selAlreadyExists);
				}else if (response.equalsIgnoreCase(EXPIRED)){
					sms = getSms(BPST_CLIP, EXPIRED, clipExpired);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
				logger.info("the response is "+sms);
			}
		}else if (taskAction.equalsIgnoreCase(action_category_promo)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_SELECTION, SUCCESS, selectionSuccess);
				}else if (response.equalsIgnoreCase(SUSPENDED)){
					sms = getSms(BPST_SUBSCRIBER_STATUS, SUSPENDED, suspended);
				}else if (response.equalsIgnoreCase(NOT_AVAILABLE)){
					sms = getSms(BPST_CLIP, NOT_AVAILABLE, clipNotAvailable);
				}else if (response.equalsIgnoreCase(ALREADY_EXISTS)){
					sms = getSms(BPST_SELECTION, ALREADY_EXISTS, selAlreadyExists);
				}else if (response.equalsIgnoreCase(EXPIRED)){
					sms = getSms(BPST_CLIP, EXPIRED, clipExpired);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (response.equalsIgnoreCase(HELP)){
					sms = helpMessage;
				}
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_profile)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_PROFILE, SUCCESS, temporaryOverrideSuccess);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_PROFILE, FAILURE, temporaryOverrideFailure);;
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_list_profiles)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_PROFILE, LIST_SUCCESS, temporaryOverrideListSuccess);
					sms = Utility.findNReplaceAll(sms, "%L", task.getString(param_sms_for_user));
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_PROFILE, LIST_FAILURE, temporaryOverrideListFailure);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_list_next_profiles)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms("PROFILE", "NEXT_SUCCESS", temporaryOverrideListSuccess);
					sms = Utility.findNReplaceAll(sms, "%SONG_NAME", task.getString(param_sms_for_user));
					if( task.getString(PROFILE_NEXT_FOOTER) != null)
						sms = sms + task.getString(PROFILE_NEXT_FOOTER);
				}else
					sms = response;
			}
		}else if (taskAction.equalsIgnoreCase(action_remove_profile)){
			if (response != null){
				if (response.equalsIgnoreCase(SUCCESS)){
					sms = getSms(BPST_PROFILE, REMOVE_SUCCESS, temporaryOverrideRemovalSuccess);
				}else if (response.equalsIgnoreCase(FAILURE)){
					sms = getSms(BPST_PROFILE, REMOVE_FAILURE, temporaryOverrideRemovalFailure);
				}else if (response.equalsIgnoreCase(ERROR)){
					sms = getSms(BPST_PROFILE, REMOVE_ERROR, temporaryOverrideRemovalError) +" "+helpMessage;
				}
				
			}
		}else if (taskAction.equalsIgnoreCase(action_category_alias)){
			if (response != null){
				if (response.equalsIgnoreCase(SMS_ALIAS_CATEGORY_NO_CLIP)){
					sms = getSms(BPST_SEARCH, SMS_ALIAS_CATEGORY_NO_CLIP, categoryNoClips);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_FAILURE)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS1_FAILURE, requestRbtFailure1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_FAILURE)){
					sms = getSms(BPST_SEARCH, REQUEST_RBT_SMS3_FAILURE, requestRbtFailure3);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS1_SUCCESS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS1_SUCCESS, requestRbtSuccess1);
				}else if (response.equalsIgnoreCase(REQUEST_RBT_SMS3_SUCCESS)){
					sms = task.getString(param_search_results) + getSms(BPST_SEARCH, REQUEST_RBT_SMS3_SUCCESS, requestRbtSuccess3);
				}else if (task.containsKey(param_cosid)){
					String cosId = String.valueOf((Integer)task.getObject(param_cosid));
					sms = "";
					if (task.containsKey(param_search_results))
						sms = task.getString(param_search_results);
					sms = sms + getSms(BPST_SEARCH, response+cosId, "");
					sms = Utility.findNReplaceAll(sms,"%L",clipName);
				}
				String categoryAlias = task.getString(param_categoryAlias);
				if (categoryAlias != null)
					sms = Utility.findNReplaceAll(sms,"%S",categoryAlias);
				String anotherResponse = null;
				if (task.containsKey(param_another_response))
					anotherResponse = task.getString(param_another_response);
				
				if (anotherResponse != null){
					if (anotherResponse.equals(CONFIRM_BULK_ACTIVATION_PREP_SMS))
						sms += getSms(BPST_SEARCH, CONFIRM_BULK_ACTIVATION_PREP_SMS, confirmBulKActivationPrepSMS);
					if (anotherResponse.equals(CONFIRM_BULK_ACTIVATION_POST_SMS))
						sms += getSms(BPST_SEARCH, CONFIRM_BULK_ACTIVATION_POST_SMS, confirmBulKActivationPostSMS);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_clip_alias)){
			if (response != null){
				String clipAlias = task.getString(param_clipAlias);
				if (response.equalsIgnoreCase(SMS_ALIAS_CLIP_INVALID)){
					sms = getSms(BPST_SEARCH, SMS_ALIAS_CLIP_INVALID, smsAliasClipInvalid);
					sms = Utility.findNReplaceAll(sms,"%S",clipAlias);
				}else if (response.equalsIgnoreCase(SMS_ALIAS_CLIP_EXPIRED)){
					sms = getSms(BPST_SEARCH, SMS_ALIAS_CLIP_EXPIRED, smsAliasClipExpired);
					sms = Utility.findNReplaceAll(sms,"%S",clipAlias);
				}else if (response.equalsIgnoreCase(SMS_ALIAS_ONLY_CLIP_SUCCESS)){
					sms = getSms(BPST_SEARCH, SMS_ALIAS_ONLY_CLIP_SUCCESS, smsAliasOnlySelSuccess);
				}else if (response.equalsIgnoreCase(SMS_ALIAS_CLIP_SUCCESS)){
					sms = getSms(BPST_SEARCH, SMS_ALIAS_CLIP_SUCCESS, smsAliasSuccess);
				}
				sms = Utility.findNReplaceAll(sms,"%C",callerID);
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_cat_search)){
			if (response != null){
				if (response.equalsIgnoreCase(INVALID)){
					sms = getSms(BPST_CATEGORY_SEARCH, INVALID, invalidrequestSMS);
				}else if (response.equalsIgnoreCase(CAT_SEARCH_RESULTS_SUCCESS)){
					String searchResults = task.getString(param_search_results);
					String smsAlias = task.getString(param_sms_alias);
					sms = searchResults + "." + getSms(BPST_CATEGORY_SEARCH, CAT_SEARCH_RESULTS_SUCCESS, catSearchResultSMS);
					sms = Utility.findNReplaceAll(sms,"%smsAlias%",smsAlias);
				}else if (response.equalsIgnoreCase(CLIP_SEARCH_RESULTS_SUCCESS)){
					String searchResults = task.getString(param_search_results);
					String smsAlias = task.getString(param_sms_alias);
					sms = searchResults + "." + getSms(BPST_CATEGORY_SEARCH, CLIP_SEARCH_RESULTS_SUCCESS, clipSearchResultSMS);
					sms = Utility.findNReplaceAll(sms,"%smsAlias%",smsAlias);
				}
			}
		}else if (taskAction.equalsIgnoreCase(action_trialReply) || taskAction.equalsIgnoreCase(action_trial)){
			if (response != null){
				if (response.equalsIgnoreCase(ACTIVATION_FAILURE)){
					sms = getSms(BPST_ERROR, ACTIVATION_FAILURE, activationFailure);
				}else if (response.equalsIgnoreCase(ALREADY_EXISTS)){
					sms = getSms(BPST_SELECTION, ALREADY_EXISTS, selAlreadyExists);
				}else if (response.equalsIgnoreCase(TECHNICAL_FAILURE)){
					sms = getSms(BPST_ERROR, TECHNICAL_FAILURE, technicalFailure);
				}else if (task.containsKey(param_cosid)){
					String cosId = String.valueOf((Integer)task.getObject(param_cosid));
					sms = getSms(BPST_TRIAL, response+cosId, "");
					sms = Utility.findNReplaceAll(sms,"%L",clipName);
				}
				
				if(task.containsKey(param_search_results))
					sms = task.getString(param_search_results) + sms;
				sms = Utility.findNReplaceAll(sms,"%C",callerID);
				sms = Utility.findNReplaceAll(sms,"%S",clipName);
			}
		}else if (taskAction.equalsIgnoreCase(action_help)){
			//do nothing, send help message, that is configured already. 
		}
		//Added to send diff help sms for sub and unsub users
		
		if(sms.equalsIgnoreCase(helpMessage))
		{
			Subscriber sub=(Subscriber)task.getObject(param_subscriber);
			if(sub!=null)
			{
				if(sub.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE)||sub.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER))
					sms=getSms(BPST_HELP, "UNSUBSCRIBED", unsubhelpMessage);
				else
					sms=getSms(BPST_HELP, "SUBSCRIBED", subhelpMessage);

			}
		}
		if (isRetailerRequest){
			logger.info("RBT:: sendSmsToUser is: " + sendSMSToUser);
			if (sendSMSToUser && !sms.equalsIgnoreCase(helpMessage)){
				task.setObject(param_Reciver,subID);
				task.setObject(param_Msg,sms);
				sendSMS(task);
				logger.info("RBT:: response sms for user: " + sms);
			}			
			writeTrans(queryString, smsForRetailer, String.valueOf(System.currentTimeMillis()-startTime), task.getString(param_ipAddress));
			return smsForRetailer;
		}
		
		if (isRetailerAccept){
			logger.info("RBT:: sendSmsToRetailer is: " + sendSMSToRetailer);
			if (sendSMSToRetailer && !smsForRetailer.equalsIgnoreCase(helpMessage)){
				task.setObject(param_Reciver,task.getString(param_RetailerMSISDN));
				task.setObject(param_Msg,smsForRetailer);
				sendSMS(task);
				logger.info("RBT:: response sms for retailer: " + smsForRetailer);
			}
			writeTrans(queryString, sms, String.valueOf(System.currentTimeMillis()-startTime), task.getString(param_ipAddress));
			return sms;
		}
		if (isOBDRequest){
			logger.info("RBT:: sendSmsToUser is: " + sendSMSToUser);
			if (sendSMSToUser && !sms.equalsIgnoreCase(helpMessage)){
				task.setObject(param_Reciver,subID);
				task.setObject(param_Msg,sms);
				sendSMS(task);
				logger.info("RBT:: response sms for user: " + sms);
			}			
			writeTrans(queryString, obdRequestResponse, String.valueOf(System.currentTimeMillis()-startTime), task.getString(param_ipAddress));
			return obdRequestResponse;
		}
		
		writeTrans(queryString, sms, String.valueOf(System.currentTimeMillis()-startTime), task.getString(param_ipAddress));
		logger.info("Returning response : "+sms);
		return sms;
	}

	private void sendSMS(Task task) {
		UtilsRequest utilsRequest=new UtilsRequest(task.getString(param_Sender),task.getString(param_Reciver),task.getString(param_Msg));
		rbtClient.sendSMS(utilsRequest);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#getContentType(java.util.HashMap)
	 */
	@Override
	public String getContentType(HashMap<String, String> requestParams)
	{
		String contentType = "text/plain; charset=utf-8";
		logger.info("RBT:: contentType: " + contentType);
		return contentType;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#getGenericErrorResponse(java.util.HashMap)
	 */
	public String getGenericErrorResponse(HashMap<String, String> requestParams)
	{
		String genericErrorResponse = helpMessage;
		logger.info("RBT:: genericErrorResponse: " + genericErrorResponse);
		return genericErrorResponse;
	}
	
	protected String getSMSParameter(String paramName) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.SMS,paramName);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return null;
	}
	
	protected String getCOMMONParameter(String paramName) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON,paramName);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return null;
	}
	
	protected  static String getSms(String type, String paramName, String defaultVal) {
		BulkPromoSMS bps = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMS(type, paramName);
		if (bps != null){
			return bps.getSmsText();
		}
		return defaultVal;
	}

	public boolean writeTrans(String params, String resp, String diff, String ip)
	{
		HashMap<String,String> h = new HashMap<String,String> ();
		h.put("REQUEST PARAMS", params);
		h.put("RESPONSE", resp);
		h.put("TIME DELAY", diff);
		h.put("REQ IP", ip);

		if(smsTrans != null)
		{
			smsTrans.writeTrans(h);
			return true;
		}

		return false;
	}
}
