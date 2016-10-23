/**
 * OnMobile Ring Back Tone 
 * 
 * $Author: manjunatha.c $
 * $Id: Constants.java,v 1.344 2015/04/29 11:08:47 manjunatha.c Exp $
 * $Revision: 1.344 $
 * $Date: 2015/04/29 11:08:47 $
 */
package com.onmobile.apps.ringbacktones.provisioning.common;

/**
 * @author vinayasimha.patil
 */
public interface Constants {

	//Added for OBD
	public static final String param_OBDSUBID = "SUB_ID";
	public static final String param_OBDMODE = "MODE";
	public static final String param_OBDCATID = "CAT_ID";
	public static final String param_OBDSMSTEXT = "SMS_TEXT";
	public static final String api_obd = "OBD";
	public static final String invalid_sms = "INVALID_SMS";
	
	//Added for Referral Program - XML: tags
	public static final String refer_MSISDN = "MSISDN";
	public static final String refer_DateTime = "DateTime";
	public static final String refer_ProductName = "ProductName";
	public static final String refer_ProductID = "ProductID";
	public static final String refer_ProductPack = "ProductPack";
	public static final String refer_PackName = "PackName";
	public static final String refer_Content = "Content";
	public static final String refer_ContentName = "ContentName";
	public static final String refer_price = "price";
	public static final String refer_descriptionmessage = "descriptionmessage";
	public static final String refer_ContentExpiry = "ContentExpiry";
	public static final String refer_interface = "interface";
	public static final String refer_otherinfo = "otherinfo";
	public static final String refer_Check = "Check";
	public static final String refer_SUB  = "SUB";
	public static final String refer_UNSUB = "UNSUB";
	public static final String refer_INVALID_MSISDN = "INVALID_MSISDN";
	public static final String refer_PENDING_MSISDN  = "PENDING_MSISDN ";
	public static final String refer_CONTENT_INVALID = "CONTENT_INVALID";
	public static final String refer_CONTENT_EXPIRE = "CONTENT_EXPIRE";
	public static final String refer_UNSPECIFIED = "UNSPECIFIED";




	public static final String param_parent_category = "parentCategory";
	public static final String param_KEYPRESSED = "KEYPRESSED";
	public static final String param_RETAILER_ID = "RETAILER_ID";
	public static final String param_DESELECTED_BY = "DESELECTED_BY";
	//Added by Sreekar for other processing
	public static final String param_ACTIVATION_INFO = "ACTIVATION_INFO";
	public static final String param_ADVANCE_RENTAL_CLASS = "ADVANCE_RENTAL_CLASS";
	public static final String param_DEACTIVATION_INFO = "DEACTIVATION_INFO";
	public static final String param_SELECTION_INFO = "SELECTION_INFO";
	public static final String param_feedPass = "feedPass";
	public static final String param_SPECIAL_CALLER_ID = "SPECIAL_CALLER_ID";

	// Promotion api responses
	public static final String Resp_Err		           = "ERROR";
	public static final String Resp_Success            = "SUCCESS";
	public static final String Resp_Fail               = "FAILURE";
	public static final String Resp_Failure            = "FAILURE";
	public static final String Resp_AlreadyActive	   = "ALREADY_ACTIVE";
	public static final String Resp_PackAlreadyActive  = "PACK_ALREADY_ACTIVE";
	public static final String Resp_AlreadyInactive	   = "ALREADY_INACTIVE";
	public static final String Resp_ActPending 		   = "ACTIVATION_PENDING";
	public static final String Resp_SuspendedNo 	   = "SUSPENDED_NO";
	public static final String Resp_DeactPending 	   = "DEACTIVATION_PENDING";
	public static final String Resp_giftPending		   = "GIFT_PENDING";
	public static final String Resp_RenewalPending	   = "RENEWAL_PENDING";
	public static final String Resp_BlackListedNo 	   = "BLACKLISTED_NO";
	public static final String Resp_InvalidPrefix 	   = "INVALID_PREFIX";
	public static final String Resp_CopyPending 	   = "COPY_PENDING";
	public static final String Resp_ActivationBlocked  = "ACTIVATION_BLOCKED";
	public static final String Resp_Help 			   = "HELP";
	public static final String Resp_SelSuspended	   = "SELECTION_SUSPENDED";
	public static final String Resp_ClipNotAvailable   = "INVALID TONEID";
	public static final String Resp_PromoCodeNotAvailable = "INVALID_PROMO_CODE";
	public static final String Resp_ClipExpired		   = "CLIP_EXPIRED";
	public static final String Resp_SelExists		   = "SELECTION_ALREADY_EXISTS";
	public static final String Resp_SelNotExists	   = "SELECTION_NOT_EXISTS";
	public static final String Resp_missingParameter   = "MISSING_PARAMETER";
	public static final String Resp_invalidTpCgid	   = "INVALID_TPCG_ID";
	public static final String Resp_offerNotFound 	   = "OFFER_NOT_FOUND";
	public static final String Resp_invalidParam	   = "INVALID_PARAM";
	public static final String Resp_Eligible		   = "ELIGIBLE";
	public static final String Resp_NonEligible		   = "NON_ELIGIBLE";
	public static final String resp_ActivePrepaid	   = "ACTIVE_PREPAID";
	public static final String resp_ActivePostpaid	   = "ACTIVE_POSTPAID";
	public static final String Resp_InvalidNumber	   = "INVALID_NUMBER";
	public static final String Resp_copyInvalidClip	   = "COPY_INVALID_CLIP";
	public static final String Resp_copyNoSelection	   = "COPY_NO_SELECTION";
	public static final String Resp_copyShuffleSelection	= "COPY_SHUFFLE_SELECTION";
	public static final String Resp_copyCantCopy	   = "COPY_CANT_COPY";
	public static final String Resp_copyInvalidNumber  = "COPY_INVALID_NUMBER";
	public static final String Resp_invalidTransID	   = "INVALID_TRANS_ID";
	public static final String Resp_songNotSet		   = "SONG_NOT_SET";
	public static final String Resp_selectionLimit	   = "SONG_SELECTION_OVERLIMIT";
	public static final String Resp_downloadOverlimit	   = "DOWNLOAD_OVERLIMIT";
	public static final String Resp_maxCallerSel	   = "MAX_CALLER_SELECTIONS_REACHED";
	public static final String Resp_giftGifterNotActive= "GIFTER_NOT_ACTIVE";
	public static final String Resp_giftGifteeActPending	= "GIFTEE_ACTIVATION_PENDING";
	public static final String Resp_giftGifteeDeactPending	= "GIFTEE_DEACTIVATION_PENDING";
	public static final String Resp_giftActPending		= "GIFT_ACTIVATION_PENDING";
	public static final String Resp_giftInUse	   		= "GIFT_IN_USE";
	public static final String Resp_giftAlreadyPreset	= "GIFT_ALREADY_PRESENT";
	public static final String Resp_NotAllowedGraceUser = "NOT_ALLOWED_FOR_GRACE";
	public static final String Resp_songSetDownloadAlreadyExists = "SONG_SET_DOWNLOAD_ALREADY_EXISTS";
	public static final String Resp_songSetSelectionAlreadyExists= "SONG_SET_SELECTION_ALREADY_EXISTS";
	public static final String Resp_InactiveUserUpgradeFailure	   = "INACTIVE_USER_NOT_ALLOWED_TO_UPGRADE";
	public static final String Resp_OffersNotAvailable	   = "OFFERS_NOT_AVAILABLE";
	public static final String Resp_InvalidCosId 	   = "INVALID_COS_ID";
	public static final String Resp_MissingCosId 	   = "MISSING_COS_ID";
	
	//RBT-13585
	public static final String  Resp_UpgradeNotAllowed = "UPGRADE_NOT_ALLOWED.";
	public static final String  Resp_TnbSongSelectionNotAllowed = "SONG_SELECTION_NOT_ALLOWED_FOR_TNB_USER";
	//Ad RBT responses
	public static final String Resp_alreadyActiveOnAdRBT= "FAILURE:ALREADY_ACTIVE_ON_ADRBT";
	public static final String Resp_alreadyActiveOnRBT	= "FAILURE:ALREADY_ACTIVE_ON_RBT";
	public static final String Resp_InactiveOnAdRBT		= "FAILURE:NOT_ACTIVE_ON_ADRBT";
	public static final String Resp_InactiveOnRBT		= "FAILURE:NOT_ACTIVE_ON_RBT";
	public static final String Resp_actPendingAdRBT		= "Failure. ACTIVATION_PENDING";
	public static final String Resp_deactPendingAdRBT	= "Failure. DEACTIVATION_PENDING";
	
	// VoluntarySuspension
	public static final String Resp_alreadyVoluntarySuspended	= "FAILURE: ALREADY VOLUNTARILY SUSPENDED";
	public static final String Resp_notVoluntarySuspended	= "FAILURE: NOT SUSPENDED VOLUNTARILY";
	public static final String Resp_suspensionNotAllowed	= "FAILURE: SUSPENSION NOT ALLOWED";
	

	//lock 
	public static final String Resp_userLocked	= "USER_LOCKED";

	public static final String Resp_liteUserPremiumBlocked	= "LITE_USER_PREMIUM_BLOCKED";
	public static final String Resp_liteUserPremiumNotProcessed	= "LITE_USER_PREMIMUM_CONTENT_NOT_PROCESSED";

	// Blacklisting
	public static final String Resp_alreadyBlackListed	= "ALREADY_BLACK_LISTED";
	
	//Task Action Constants
	public static final String action_obd			= "obd";
	public static final String action_remove_profile	= "removeProfile";
	public static final String action_list_profiles		= "listProfile";
	public static final String action_list_next_profiles = "actionListNextProfiles";
	public static final String action_trial				= "trial";
	public static final String action_trialReply		= "trialReply";
	public static final String action_cricket			= "cricket";
	public static final String action_viral				= "viral";
	
	
	public static final String request_mod     		= "MOD";
	public static final String request_envio   		= "ENVIO";
	public static final String request_ussd    		= "USSD";
	public static final String request_ec      		= "EC";
	public static final String request_autodial		= "AUTODIAL";


	//Start copy 
	public static final String api_startcopy	    = "StartCopy";
	public static final String failureResponse	    = "FAILURE";
	
	//CGI Song Selection 
	public static final String api_cgi_song_selection	    = "CgiSongSelection";
	
	public static final String param_Called="CALLED";
	public static final String param_subscriber_id="SUBSCRIBER_ID";
	public static final String param_caller="CALLER";
	public static final String param_caller_id="CALLER_ID";
	public static final String param_clip_id="CLIP_ID";
	public static final String param_vcode="VCODE";
	public static final String param_sel_by="SEL_BY";
	public static final String param_opr_flag="OPR_FLAG";
	public static final String param_sms_type="SMS_TYPE";
	public static final String param_keyPressed="KEYPRESSED";
	public static final String param_songname="SONGNAME";



	//API Names Constants
	//Parameters Constants
	public static final String param_invalidParam	= "invalidParam";
	public static final String param_specialResp	= "specialResp";
	public static final String param_startTime		= "startTime";
	public static final String param_endTime		= "endTime";
	public static final String param_queryString	= "queryString";
	public static final String param_actFailed		= "activationFailed";
	public static final String param_actRequested	= "activationRequested";
	
	public static final String param_library		= "library";
	public static final String param_respMessage	= "respMessage";
	public static final String param_finalResponse	= "finalResponse";

	public static final String param_sms_alias		= "smsAlias";
	
	
	public static final String param_isOBDRequest		= "isOBDRequest";
	public static final String param_obd_response		= "obdResponse";
	
	public static final String param_retPack		= "retPack";
	
	//MOD Parameters
	public static final String param_mod_msisdn  = "msisdn";
	public static final String param_mod_scode  = "scode";
	public static final String param_mod_channel  = "channel";
	
	//ENVIO Parameters
	public static final String param_envio_srcmsisdn = "srcmsisdn";
	public static final String param_envio_Flag = "Flag";
	public static final String param_envio_vcode = "vcode";
	public static final String param_envio_cbsmsisdn = "cbsmsisdn";
	public static final String param_envio_dstmsisdn = "dstmsisdn";
	public static final String param_envio_indx = "indx";
	public static final String param_envio_requester = "requester";
	public static final String param_envio_issubscribed = "issubscribed";
	public static final String param_envio_downChg = "downChg";
	public static final String param_envio_subsChg = "subsChg";
	//added in v11
	public static final String param_envio_cLogs = "cLogs";
	
	//Autodial Parameters
	public static final String param_auto_msisdn = "msisdn";
	public static final String param_auto_vcode = "vcode";
	public static final String param_auto_ucode = "ucode";
	public static final String param_auto_stype = "stype";
	public static final String param_auto_caller = "caller";
	public static final String param_auto_syscode = "syscode";
	public static final String param_auto_flag = "flag";
	public static final String param_auto_AlbumCode = "AlbumCode";
	public static final String param_auto_albumcode = "albumcode";
	//added in v1.4
	public static final String param_auto_cLogs = "cLogs";
	
	//EC Parameters
	public static final String param_ec_customer = "customer";
	public static final String param_ec_vcode = "vcode";
	public static final String param_ec_reseller = "reseller";
	public static final String param_ec_Flag = "Flag";
	public static final String param_ec_ccode = "ccode";
	public static final String param_ec_requester = "requester";
	
	//Airtel USSD Parameters
	public static final String param_ussd_srcMsisdn = "srcMsisdn";
	public static final String param_ussd_cmd = "cmd";
	public static final String param_ussd_vcode = "vcode";
	public static final String param_ussd_cbsMsisdn = "cbsMsisdn";
	public static final String param_ussd_dstMsisdn = "dstMsisdn";
	public static final String param_ussd_chg = "chg";
	
	public static final String param_isRetailerAccept		= "isRetailerAccept";

	public static final String TRIAL_KEYWORDS = "TRIAL_KEYWORDS";
    public static final String TRIAL_REPLY_KEYWORD = "TRIAL_REPLY_KEYWORD";
    public static final String OBD_NUMBER = "OBD_NUMBER";   
    public static final String OBD_ACT_KEYWORD = "OBD_ACT_KEYWORD";
    public static final String OBD_REPORT_PATH = "OBD_REPORT_PATH";
    public static final String OBD_REPORT_ROTATION_SIZE = "OBD_REPORT_ROTATION_SIZE";//int
    public static final String PARENT_CAT_ID_LIST_FOR_CAT_SEARCH = "PARENT_CAT_ID_LIST_FOR_CAT_SEARCH";
    public static final String MAX_SEARCH_RESULTS_TO_SEND_FOR_CAT_SEARCH = "MAX_SEARCH_RESULTS_TO_SEND_FOR_CAT_SEARCH";//int
    public static final String VIRAL_KEYWORDS = "VIRAL_KEYWORDS";
    public static final String LIST_RBT_KEYWORD = "LIST_RBT_KEYWORD";
    
    
    //BULK PROMO SMS TYPES
    public static final String BPST_OBD = "OBD";
    public static final String BPST_TRIAL = "TRIAL";
    public static final String BPST_PROFILE = "PROFILE";
    
    //General
    public static final String NO_SMS	   = "NO_SMS";
    public static final String INVALID_IP_ADDRESS = "INVALID_IP_ADDRESS";
    public static final String INSUFFICIENT_PARAMETERS = "INSUFFICIENT_PARAMETERS";
    
    public static final String SUBSCRIPTION_WITH_PACK_SUCCESS = "SUBSCRIPTION_WITH_PACK_SUCCESS";
    
    
    
    public static final String TECHNICAL_ERROR = "TECHNICAL_ERROR";
    
    public static final String REQUEST_RBT_SMS2_SUCCESS_SUB_COS 		= "REQUEST_RBT_SMS2_SUCCESS_SUB_COS";
    public static final String REQUEST_RBT_SMS2_SUCCESS_NONSUB_COS 		= "REQUEST_RBT_SMS2_SUCCESS_NONSUB_COS";
    public static final String REQUEST_RBT_SMS2_SUCCESS_SUB_FREE_COS 	= "REQUEST_RBT_SMS2_SUCCESS_SUB_FREE_COS";
    public static final String REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS 		= "REQUEST_RBT_SMS1_SUCCESS_NONSUB_COS";
    public static final String REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS 		= "REQUEST_RBT_SMS3_SUCCESS_NONSUB_COS";
    public static final String REQUEST_RBT_SMS1_SUCCESS_SUB_COS 		= "REQUEST_RBT_SMS1_SUCCESS_SUB_COS";
    public static final String REQUEST_RBT_SMS3_SUCCESS_SUB_COS 		= "REQUEST_RBT_SMS3_SUCCESS_SUB_COS";
    public static final String REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS 	= "REQUEST_RBT_SMS1_SUCCESS_SUB_FREE_COS";
    public static final String REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS 	= "REQUEST_RBT_SMS3_SUCCESS_SUB_FREE_COS";
    public static final String CONFIRM_BULK_ACTIVATION_PREP_SMS = "CONFIRM_BULK_ACTIVATION_PREP_SMS";
    public static final String CONFIRM_BULK_ACTIVATION_POST_SMS = "CONFIRM_BULK_ACTIVATION_POST_SMS";
    
    
    
    public static final String INVALID_TONE_CODE = "INVALID_TONE_CODE";
    
    //Trial
    public static final String RBT_INVALID_SMS_COS = "RBT_INVALID_SMS_COS";
    public static final String RBT_SUCCESS_SMS_COS = "RBT_SUCCESS_SMS_COS";
    
    //Category Search
    public static final String CAT_SEARCH_RESULTS_SUCCESS = "CAT_SEARCH_RESULT_SUCCESS";
    public static final String CLIP_SEARCH_RESULTS_SUCCESS = "CLIP_SEARCH_RESULT_SUCCESS";
    
    //Profile
    public static final String LIST_SUCCESS = "LIST_SUCCESS";
    public static final String LIST_FAILURE = "LIST_FAILURE";
    public static final String REMOVE_SUCCESS = "REMOVE_SUCCESS";
    public static final String REMOVE_FAILURE = "REMOVE_FAILURE";
    public static final String REMOVE_ERROR = "REMOVE_ERROR";
    
    public static final int SHUFFLE = 0;
	//Task Action Constants sms
	public static final String action_promoselection	    = "Promoselection";
	public static final String action_listprofile	        = "Listprofile";
	public static final String action_activation		    = "Activation";
	
	public static final String BPST_ACTIVATION_PROMO = "ACTIVATION_PROMO";
    public static final String BPST_TEMP_OVERRIDE = "TEMP_OVERRIDE";
    public static final String BPST_TEMP_OVERRIDE_LIST = "TEMP_OVERRIDE_LIST";
    public static final String BPST_LISTEN = "LISTEN";
    public static final String BPST_GIFTINACT = "GIFTINACT";
    public static final String BPST_GIFTCODE = "GIFT_CODE";
    public static final String BPST_GIFT = "GIFT";
    public static final String BPST_COPYCANCEL = "COPY_CANCEL";
    public static final String BPST_VIRAL = "VIRAL";
    public static final String BPST_MGM_RECIPIENT_ACK = "MGM_RECIPIENT_ACK";
    public static final String BPST_MGM_SENDER = "MGM_SENDER";
    public static final String BPST_RETAILER_RESP_SMS = "RETAILER_RESP_SMS";
    public static final String BPST_MGM_RECIPIENT = "MGM_RECIPIENT";
    public static final String BPST_SEL_KEYWORD = "SEL_KEYWORD1";
    public static final String BPST_OVERRIDE_SHUFFEL = "OVERRIDE_SHUFFLE";
    public static final String BPST_POLLON = "POLL_ON";
    public static final String BPST_MGM_SENDER_MIN = "MGM_SENDER_MIN";
    public static final String BPST_MGM_SENDER_MAX = "MGM_SENDER_MAX";
    public static final String BPST_TNB = "TNB";
    public static final String BPST_SONG_OFMONTH = "SONG_OF_MONTH"; 
    public static final String BPST_CORP_CHANGESEL = "CORP_CHANGE_SELECTION_ALL"; 
    public static final String BPST_REQUEST_RBT_SET = "REQUEST_RBT_SET";
    public static final String BPST_CATEGORY_SEARCH_SET = "CATEGORY_SEARCH_SET";
    public static final String BPST_REQUEST_RBT_SMS2 = "REQUEST_RBT_SMS2"; 
    public static final String BPST_REQUEST_MORE_NO = "REQUEST_MORE_NO"; 
    public static final String BPST_REQUEST_MORE_CAT_NO = "REQUEST_MORE_CAT_NO"; 
    public static final String BPST_REQUEST_MORE_CAT = "REQUEST_MORE_CAT";
    public static final String BPST_REQUEST_MORE = "REQUEST_MORE";
    public static final String BPST_REQUEST_RET_MORE = "REQUEST_RET_MORE";
    public static final String BPST_CORP_CHANGE_SELECTION_ALL = "CORP_CHANGE_SELECTION_ALL";
    public static final String BPST_REQUEST_RBT_SMS1 = "REQUEST_RBT_SMS1";
    public static final String BPST_CATEGORY_SEARCH = "CATEGORY_SEARCH";
    public static final String BPST_MANAGE = "MANAGE";
    public static final String BPST_DOWNLOADS_NOT_PRESENT = "DOWNLOADS_NOT_PRESENT";
    public static final String BPST_DOWNLOADS_LIST = "DOWNLOADS_LIST";
    public static final String BPST_COPY = "COPY";
    public static final String BPST_CALLER_ID = "CALLER_ID";
    public static final String BPST_RENEW_INVALID_REQUSET = "RENEW_INVALID_REQUSET";
    public static final String BPST_RENEW_SONG = "RENEW_SONG";
    public static final String BPST_RENEW = "RENEW";
    public static final String BPST_RMV_CALLERID = "RMV_CALLERID";
    public static final String BPST_PROMO_ID = "PROMO_ID";
    public static final String BPST_POLL_ON_ = "POLL_ON";
    public static final String BPST_CATEGORY = "CATEGORY";
    public static final String BPST_NON_SUBSCRIBER = "NON_SUBSCRIBER";
    public static final String BPST_REQUEST = "REQUEST";
	public static final String BPST_REQUEST_NO_MATCH = "REQUEST_NO_MATCH";
    //General
	public static final String REQMORE = "REQ_MORE";
    public static final String NOTAVAILABLE = "NOT_AVAILABLE";
    public static final String DISP_RET = "DISP_RET";
    public static final String MORECAT = "MORE_CAT";
    public static final String NONSUBSMS = "SMS";
    public static final String POLLSUCCESS = "POLLSUCCESS";
    public static final String POLLTECHDIF = "TECHNICAL_DIFFICULTIES";
    public static final String PROMOIDFAIL = "PROMID_FAILURE";
    public static final String RMVFAILURE = "RMVFAILURE";
    public static final String INVALIDCODE = "INVALID_CODE";
    public static final String SONGEXPIRED = "SONGEXPIRED";
    public static final String RENEWREQ = "RENEW_REQUEST";
    public static final String SEL_BLOCK = "SELECTION_BLOCK";
    public static final String NO_SEL = "NO_SEL";
    public static final String COPYFAIL = "COPYFAIL";
    public static final String TEMPFAIL = "TEMPFAIL";
    public static final String NOT_PRESENT = "NOT_PRESENT";
    public static final String RETEXHAUSTED = "EXHAUSTED";
    public static final String MOREEXHAUSTED = "EXHAUSTED";
    public static final String CATEXHAUSTED = "EXHAUSTED";
    public static final String SEARCH = "SEARCH";
    public static final String CATSEARCH = "CATSEARCH";
    public static final String TECHDIF = "TECHNICAL_DIFFICULTIES";
    public static final String HELPERR = "HELP_ERR";
    public static final String SETFAIL = "SETFAILURE";
    public static final String ACK = "ACK";
    public static final String REQSUCCESS = "REQSUCCESS";
    public static final String REQFAILURE= "REQFAILURE";
    public static final String ACT_FAIL = "ACT_FAILURE";
    public static final String GIFT_FAIL = "GIFT_FAILURE";
    public static final String SEL = "SELECTION";
    public static final String ACK_SEL = "ACK_SEL";
    public static final String ACCEPT_SEL = "ACCEPT_SEL";
    public static final String ACCEPT_NOSEL = "ACCEPT_NOSEL";
    public static final String ACCEPT_NOSEL1 = "ACCEPT_NOSEL1";
    public static final String GIFTER = "GIFTER";
    public static final String ACCEPT1 = "ACCEPT";
    public static final String SENDERFAILURE = "SENDERFAILURE";
    
    public static final String SETRETFAILURE = "SETRETFAILURE";
    public static final String SMSFAILURE = "SMSFAILURE";
    public static final String TECHFAILURE = "TECHFAILURE";
    public static final String ERROR = "ERROR";
    public static final String RETRIABLE_ERROR = "RETRIABLE_ERROR";
    public static final String ALREADY_ACCEPTED = "ALREADY_ACCEPTED";
    public static final String TEMPOVERRIDEFAIL = "TEMPOVERRIDE";
    public static final String TEMPOVERRIDESUCCESS = "TEMPOVERRIDESUCCESS";
    
	// Task Action Constants
	public static final String action_acceptretailer = "ACCEPT_RETAILER";
	public static final String action_acceptmgm = "ACCEPT_MGM";
	public static final String action_acceptviral = "ACCEPT_VIRAL";
	//JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs.
	public static final String param_password = "passWord";
	public static final String param_userId = "userId";
	public static final String param_Feature = "feature";
	public static final String param_callerid = "callerid";
	public static final String param_enddate = "endate";
	public static final String param_optin = "optin";
	public static final String param_clip = "clip";
	public static final String param_category = "category";
	public static final String param_clipid = "clipid";
	public static final String param_catid = "catid";
	public static final String param_MODE = "MODE";
	public static final String param_actMode = "ACT_MODE";
	public static final String param_isdirectact = "isDirectact";
	public static final String param_isdirectdct = "isDirectdct";
	public static final String param_isDelayDeactForUpgrade = "isDelayDeactForUpgrade";
	public static final String param_subclass = "subclass";
	public static final String param_rbttype = "rbttype";
	public static final String param_rbt_object = "rbtObject";
	public static final String param_freeperiod = "freeperiod";
	public static final String param_actby = "actby";
	public static final String param_profile_hours = "profileHours";
	public static final String param_WAITTIME = "WAITTIME";
	public static final String param_copysuccesskey  = "copysuccesskey ";
	public static final String param_actdatestr  = "actdatestr";
	public static final String param_cricket_pack  = "paramCricketPack";
	public static final String param_chargeclass  = "chargeclass";
	public static final String param_inLoop  = "inLoop";
	public static final String param_fromTime  = "fromTime";
	public static final String param_toTime  = "toTime";
	public static final String param_chargeModel  = "chargeModel";
	public static final String param_interval  = "interval";
	public static final String param_transid  = "param_transid";
	public static final String param_song_chrg_amt  = "song_chrg_amt";
	public static final String param_scratchCardNo = "param_scratchCardNo";
	public static final String param_playerStatus = "playerStatus";
	public static final String param_userInfoMap = "userInfoMap";
	public static final String param_PROFILE_NAME = "PROFILE_NAME";
	public static final String param_PROFILE_HOURS = "PROFILE_HOURS";
	public static final String param_fromTimeMins  = "fromTimeMins";
	public static final String param_toTimeMins  = "toTimeMins";

	public static final String param_Sender = "sender";
	public static final String param_Reciver = "reciver";
	public static final String param_Msg = "message";
	public static final String param_revrbt = "revrbt";
	public static final String param_mmContext = "mmContext";
	public static final String param_contentType = "CONTENTTYPE";

	public static final String param_STATUS = "STATUS";
	public static final String param_GIFTEDTO = "GIFTED_TO";
	public static final String param_GIFTEDBY = "GIFTED_BY";
	public static final String param_REQTIMESATMP = "REQUESTED_TIMESTAMP";
    public static final String param_REQVAL= "REQUEST_VALUE";

	public static final String Resp_Active = "ACTIVE";
	public static final String Resp_Inactive = "INACTIVE";

	public static final String param_PromoKeyword = "param_PromoKeyword";
	public static final String param_SmsList = "param_SmsList";
	public static final String CLIP_OBJ = "CLIP_OBJ";
	public static final String CAT_OBJ = "CAT_OBJ";
	public static final String param_requesttype = "param_requesttype";
	public static final String type_content_validator = "type_content_validator";
	public static final String type_song_set = "type_song_set";
	public static final String param_ocg_charge_id = "param_ocg_charge_id";
	
	public static final String param_action = "action";
	public static final String param_status = "status";
	public static final String param_Originator ="Originator";
	
	//Added for CRM Request Jira Id RBT-11962 
	public static final String param_SR_ID  = "SR_ID";
	public static final String param_ORIGINATOR = "ORIGINATOR";
	public static final String param_LANG_CODE = "LANGUAGE_CODE";
	
	//Promotion api parameters
	public static final String param_MSISDN		= "MSISDN";
	public static final String param_ACTIVATED_BY = "ACTIVATED_BY";
	public static final String param_CALLER_ID = "CALLER_ID";
	public static final String param_SENT_TIME = "SENT_TIME";
	public static final String param_CATEGORY_ID = "CATEGORY_ID";
	public static final String param_CHANGE_TYPE = "CHANGE_TYPE";
	public static final String param_DEACTIVATED_BY = "DEACTIVATED_BY";
	public static final String param_END_DATE = "END_DATE";
	public static final String param_FEED_PASS = "FEED_PASS";
	public static final String param_FEED_STATUS = "FEED_STATUS";
	public static final String param_IN_LOOP = "IN_LOOP";
	public static final String param_ISACTIVATE = "ISACTIVATE";
	public static final String param_IS_OPTOUT = "IS_OPTOUT";
	public static final String param_PROMO_ID = "PROMO_ID";
	public static final String param_REACTIVATE = "REACTIVATE";
	public static final String param_REDIRECT_NATIONAL = "REDIRECT_NATIONAL";
	public static final String param_REQUEST = "REQUEST";
	public static final String param_SELECTED_BY = "SELECTED_BY";
	public static final String param_SEND_SMS = "SEND_SMS";
	public static final String param_SUBSCRIPTION_CLASS = "SUBSCRIPTION_CLASS";
	public static final String param_CHARGE_CLASS = "CHARGE_CLASS";
	public static final String param_USE_UI_CHARGE_CLASS = "USE_UI_CHARGE_CLASS";
	public static final String param_SUB_TYPE = "SUB_TYPE";
	public static final String param_TONE_ID = "TONE_ID";
	public static final String param_TRANSID = "TRANSID";
	public static final String param_WAV_FILE = "WAV_FILE";
	public static final String param_XML_REQUIRED = "XML_REQUIRED";
	public static final String param_REFUND = "REFUND";
	public static final String param_COSID = "COSID";
	public static final String param_PACK_COSID = "PACK_COSID";
	public static final String param_CONSENT_LOG = "CONSENT_LOG";
	public static final String param_SMS_ALIAS = "SMS_ALIAS";
	public static final String param_update_sms_id = "update_sms_id";
	public static final String param_RBTTYPE = "RBTTYPE";
	public static final String param_allowPremiumContent = "allowPremiumContent";
	public static final String param_isMultiChargesRequest = "isMultiChargesRequest";
	// RBT-14301: Uninor MNP changes.
	public static final String param_CIRCLE_ID		= "CIRCLE_ID";
	
	// Added for copy confirm mode
	public static final String param_copy_confirm_mode = "COPY_CONFIRM_MODE";
	public static final String param_copy_mode = "COPY_MODE";
	public static final String param_offerID = "OFFERID";

	//Added for DU SmsProcessor to Activate TNB.
	public static final String param_sel_offerID = "SEL_OFFERID";
	public static final String param_alreadyGetBaseOffer = "ALREADY_GET_BASE_OFFER";
	public static final String param_alreadyGetSelOffer = "ALREADY_GET_SEL_OFFER";

	//for displaying charge amt of song only for active subscriber
	public static final String param_chrgAmtOnlyForActive = "CHRG_AMT_ONLY_FOR_ACTIVE";
	//Added for Meri dhun
	public static final String param_PROMO_TEXT = "PROMO_TEXT";
	
	/**
	 * Added for Ad RBT
	 * @author Sreekar
	 */
	public String param_AD_RBT_REQUEST = "AD_RBT_REQUEST";
	public static final String param_TRANS_TYPE = "TRANS_TYPE";
	public static final String param_NEW_RBT_TYPE = "NEW_RBT_TYPE";
	/**
	 * Added for hsb
	 * @author Sreekar
	 */
	public String param_HSB_REQUEST = "HSB_REQUEST";

	public static final String response_Sms_String = "RESPONSE_STRING";
	public static final String response_Sms_Code = "RESPONSE_CODE";
	

	// promotion api request type
	public static final String request_status = "status";
	public static final String request_check = "check";
	public static final String request_activate = "activate";
	public static final String request_deactivate = "deactivate";
	public static final String request_selection = "selection";
	public static final String request_hsb_act = "act";
	public static final String request_hsb_deact = "deact";
	public static final String request_hsb_dct = "dct";
	public static final String request_hsb_can = "can";
	public static final String request_cricket = "cricket";
	public static final String request_upgrade = "upgrade";
	public static final String request_block = "block";
	public static final String request_unblock = "unblock";
	public static final String request_TNB = "TNB";
	public static final String request_upgrade_base	= "upgrade_base";
	public static final String request_deact_pack	= "deact_pack";
	public static final String request_SHUFFLE = "SHUFFLE";
	
	// ad rbt request type
	public static final String request_ad_rbt_act = "ACT";
	public static final String request_ad_rbt_deact = "DEACT";
	public static final String request_ad_rbt_act_convert = "ACT_CONVERT";
	public static final String request_ad_rbt_deact_convert = "DEACT_CONVERT";
	
	// RBT User Types
	public static final int TYPE_RBT = 0;
	public static final int TYPE_SRBT = 1;
	public static final int TYPE_RRBT = 2;
	public static final int TYPE_RBT_RRBT = 3;
	public static final int TYPE_SRBT_RRBT = 4;
	
	public static final String param_URL		= "URL";
	//Task Action Constants
	public static final String action_activate		= "activate";
	public static final String action_upgrade		= "upgrade";
	public static final String action_deactivate	= "deactivate";
	public static final String action_selection		= "selection";
	public static final String action_delete_selection = "delete_selection";
	public static final String action_help 			= "help";
	public static final String action_cat_search	= "catSearch";
	public static final String action_song_search	= "songSearch";
	public static final String action_default_search	= "defaultSearch";
	public static final String action_retailer_request	= "retailerRequest";
	public static final String action_retailer_search	= "retailerSearch";
	public static final String action_retailer_accept	= "retailerAccept";
	public static final String action_retailer_type_2	= "type2RetailerRequest";
	public static final String action_feed				= "feed";
	public static final String action_clip_promo		= "clipPromo";
	public static final String action_category_promo	= "categoryPromo";
	public static final String action_profile			= "profile";
	public static final String action_clip_alias		= "clipAlias";
	public static final String action_category_alias	= "categoryAlias";
	public static final String action_loop				= "loop";
	public static final String action_delete			= "delete";
	public static final String action_optin_copy_cancel	= "optInCopyCancel";
	public static final String action_copy_cancel		= "copyCancel";
	public static final String action_copy_confirm		= "copyConfirm";
	public static final String action_topup				= "TOPUP";
	public static final String action_meriDhun			= "MERIDHUN";
	public static final String action_suspend			= "suspend";
	public static final String action_resume            = "resume";
	public static final String action_up_validity = "up_validity";
	
	public static final String action_copy		      = "copy";
	public static final String action_cross_copy	  = "crosscopy";
	public static final String action_mnp_cross_copy	  = "mnpcrosscopy";
	public static final String action_cross_copy_rdc  = "crosscopyrdc";
	public static final String action_rbt_play_help   = "rbtplayhelp";
	public static final String action_esia_quiz_forward   = "EsiaQuizForward";
	public static final String action_vodactservice		      = "action_vodactservice";

	
	public static final String action_ugc_add     = "ADD";
	public static final String action_ugc_expire  = "EXPIRE";
	
	public static final String action_rbt_access    = "ACCESS";
	public static final String action_rbt_poll      = "POLL";
	public static final String action_rbt_copy      = "COPY";
	//API Names Constants
	public static final String api_gift			    = "Gift";
	public static final String api_redirect			= "redirect";
	public static final String api_Promotion	    = "Promotion";
	public static final String api_SubscriptionPreConsent = "SubscriptionPreConsent";
	public static final String api_Consent          = "Consent";
	public static final String api_consentCallback  = "ConsentCallback";
	public static final String api_Sms			    = "Sms";
	public static final String api_copy			    = "Copy";
	public static final String api_subprof			= "subscriberprofile";
	public static final String api_subsats			= "subscriberstatus";
	public static final String api_giftack			= "giftack";
	public static final String api_crossgift		= "Crossgift";
	public static final String api_tonecopy			= "ToneCopy";
	public static final String api_service			= "service";
	public static final String api_UGC			    = "Ugc";
	public static final String api_rbtplayhelp	    = "rbtplayerhelper";
	public static final String api_cross_copy	    = "CrossCopy";
	public static final String api_refer_check	    = "CheckSubscriber";
	public static final String api_mnp_cross_copy	= "MnpCrossCopy";
	public static final String api_cross_copy_rdc	= "CrossCopyRdc";
	public static final String api_ESIAQuizForward	= "ESIAQuizForward";
	public static final String api_Daemon			= "Daemon";
	public static final String api_validateAndCopy	= "ValidateAndCopy";
	public static final String api_VodaCTservice	= "VodaCTservice";
	
	//Parameters Constants
	public static final String param_api			= "api";
	public static final String param_ipAddress		= "ipAddress";
	public static final String param_response		= "response";
	public static final String param_another_response	= "another_response";
	public static final String param_Status	    	= "status";
	public static final String param_hostName = "hostName";
	public static final String param_isValid = "param_isValid";
	
	public static final String param_subscriberID	= "subscriberID";
	public static final String param_subscriber		= "subscriber";
	public static final String param_clipName		= "clipName";
	public static final String param_clipAlias		= "clipAlias";
	public static final String param_categoryAlias	= "categoryAlias";
	public static final String param_promoID		= "promoID";
	
	public static final String param_isPrepaid		= "isPrepaid";
	public static final String param_actInfo		= "actInfo";
	public static final String param_error			= "error";
	public static final String HELP					= "helpMsg";
	public static final String param_sms			= "SMS";
	public static final String param_sms_for_user	= "SMSForUser";
	public static final String param_search_results	= "searchResults";
	public static final String param_downloads_list_for_sms	= "downloadsListSMS";
	public static final String param_responseSms		= "responseSms";
	public static final String param_responseUssd		= "responseUssd";
	public static final String param_responseObdMark		= "param_responseObdMark";
	public static final String param_isGifterConfRequired	= "isGifterConfRequired";
	public static final String param_isGifteeConfRequired	= "isGifteeConfRequired";
	public static final String param_isFreemiumDoubleConfirm = "isFreemiumDoubleConfirm";
	public static final String param_isDefaultProfileHrsByIndex = "isDefaultProfileHrsByIndex";
	public static final String param_send_sms_to_user	= "SEND_SMS_TO_USER";
	public static final String param_send_sms_to_retailer	= "SEND_SMS_TO_RETAILER";
	public static final String param_not_allowed			= "not_allowed";
	
	public static final String param_profileClipSMSAlias = "profileClipSMSAlias";
	public static final String param_retailer		= "retailer";
	public static final String param_retailer_response		= "retailerResponse";
	public static final String param_isRetailerRequest		= "isRetailerRequest";
	public static final String param_isRetailerSearch		= "isRetailerSearch";
	public static final String param_RetailerMSISDN			= "retailerMSISDN";
	
	public static final String param_cos			= "cos";
	public static final String param_cosid			= "cosId";
	
	public static final String param_subID		= "SUB_ID";
	public static final String param_msisdn		= "msisdn";
	
	public static final String param_smsText	= "SMS_TEXT";
	public static final String param_smsSent	= "SMS_SENT";
	public static final String param_selectionType				= "selectionType";
	public static final String param_rentalPack					= "rentalPack";
	
	public static final String param_refID					= "refId";


	public static final String param_msg	= "MSG";
	public static final String param_MESSAGE	= "MESSAGE";
	
	public static final String param_smsParam	= "SMS_PARAM";
	public static final String param_subType    = "SUB_TYPE";
	public static final String param_access    	= "ACCESS";
	public static final String param_shortCode  = "SHORTCODE";
	public static final String param_TRX_ID  = "TRX_ID";
	public static final String param_TRANS_ID  = "TRANS_ID";
	public static final String param_src  = "src";
	public static final String param_USER_INFO  = "USER_INFO";
	public static final String param_feature = "FEATURE";
	public static final String param_language = "LANGUAGE";
	public static final String param_mode	 = "MODE";
	public static final String param_info	 = "info";
	public static final String param_consent_status = "CONSENT_STATUS";
	
	//api responses
	public static final String param_SUBID              = "SUBSCRIBER_ID";
	public static final String param_EXTRAINFO              = "extraInfo";
    public static final String param_CALLERID       = "CALLER_ID";
    public static final String param_CLIPID           = "CLIP_ID";
    public static final String param_SMSTYPE        = "SMS_TYPE";
    public static final String param_SELBY            = "SEL_BY";
    public static final String param_wavfile      = "WAV_FILE";
    public static final String param_SEARCHCOUNT    = "searchcount";
    public static final String param_SMSNEWTYPE           = "SMS_NEWTYPE";
    public static final String param_DATE               = "DATE";
    public static final String param_SONGNAME               = "SONGNAME";
    public static final String param_TONECODE               = "TONECODE";
    public static final String param_SOURCEOP        = "SOURCE_OP";
    public static final String param_USSD_ACTION			= "USSD_ACTION";
    public static final String param_BROWSING_LANGUAGE		= "BROWSING_LANGUAGE";
	public static final String param_NEWSUBID              = "NEW_SUBSCRIBER_ID";
    
    //copy api response
    public static final String copy_Resp_Err                ="ERROR";
    public static final String copy_Resp_Success            ="SUCCESS";
    public static final String copy_Resp_Fail               ="FAILURE";
    public static final String cross_copy_Resp_insuf            ="INSUFFICIENT PARAMETERS";
    public static final String cross_copy_Resp_Err            ="-1";
    public static final String cross_copy_Resp_Success          ="0";

    //ugc api response
	public static final String ugc_Resp_Err		           ="ERROR";
	public static final String ugc_Resp_Success            ="SUCCESS";
	public static final String ugc_Resp_Fail               ="FAILURE";
	public static final String ugc_Resp_insuf              ="MISSING_PARAMETERS";
	
	//ugc api paramaeters ACTION,SUB_ID,PROMO_ID,TRANS_ID
	public static final String ugc_param_SUBID		    = "SUB_ID";
	public static final String ugc_param_ACTION		    = "ACTION";
	public static final String ugc_param_PROMOID	    = "PROMO_ID";
	public static final String ugc_param_TRANSID		= "TRANS_ID";
	public static final String ugc_param_WAVFILE		= "UPLOADFILE";
	
	//Mera Hello Tune Parameters
	public static final String meraHT_param_CHANNEL		= "CHANNEL";
	public static final String meraHT_param_CONSENT_LOG	= "CONSENT_LOG";
	public static final String meraHT_param_SINGER		= "SINGER";
	public static final String meraHT_param_CLIPNAME	= "CLIP_NAME";
	public static final String meraHT_param_CATEGORY_ID	= "CATEGORY_ID";
	public static final String meraHT_param_SUBCATEGORY_ID	= "SUBCATEGORY_ID";
	public static final String meraHT_param_EXPIRY_DATE	= "EXPIRE_DATE";
	public static final String meraHT_param_COPYRIGHT_ID = "COPYRIGHT_ID";
	public static final String meraHT_param_ALBUM	= "ALBUM_MOVIE";
	public static final String meraHT_param_LANGUAGE_ID		= "LANG_ID";
	public static final String meraHT_param_PUBLISHER_ID		= "PUBLISHER_ID";
	public static final String meraHT_param_VCODE				= "VCODE";
	public static final String meraHT_param_CCODE				="CCODE";
	
	//parameter
	public static final String default_report_PATHDIR	= "DEFAULT_REPORT_PATH";
	public static final String copy_default_song	    = "COPY_DEFAULT_SONG";
	
	//rbt player helper api parameters
	public static final String rbt_param_DETAILS		= "DETAILS";
	public static final String rbt_param_ACTION		    = "ACTION";
	public static final String rbt_param_EXTRAPARAM	    = "EXTRA_PARAMS";
	
	//BSNL ad rbt params
	public static final String param_BSNL_adRBT_command	= "command";
	public static final String param_BSNL_adRBT_msisdn	= param_msisdn;
	public static final String param_BSNL_adRBT_trx_id	= "trx_id";
	public static final String param_BSNL_adRBT_sender	= param_Sender;
	public static final String param_BSNL_adRBT_transactionKey	= "transaction_key";
	
	//BSNL ad rbt commands
	public static final String param_BSNL_adRBT_command_SUBINFO	= "getSubscriberInfo";
	public static final String param_BSNL_adRBT_command_ACT		= "provisionSub";
	public static final String param_BSNL_adRBT_command_DCT		= "removeSub";
	
	public static final String param_isSuperHitAlbum = "isSuperHitAlbum";
	public static final String param_isPromoIDFailure = "isPromoIDFailure";
	
	//response message for tonecopy api
	
	public static final short NO_ERROR			= 0;	/*Incase of Success*/
	public static final short SUSPEND_PROCESS_STATUS	= 0x9e;	/*Incase of Subscriber De-Provisioning fails or suspended*/
	public static final short SUSPEND_STATUS		= 0x01;	/*Incase of Insufficient Balance*/
	public static final short DATABASE_DOWN			= 0x92;	/*Incase of DataBase is down */
	public static final short POLY_DB_NOT_CONNECTED		= 171;	/*Incase of Polyhedra Database is not connected*/ 
	public static final short RECORD_NOT_FOUND		= 0x91;	/*Incase of subscriber record is not found*/
	public static final short RECORD_EXISTS_ALREADY		= 0x93;	/*Incase of a request made to add a entry , that already exit*/
	public static final short ILLIGAL_DATE_TIME		= 0x70;	/*Incase of Invalid date format*/
	public static final short SUBSCRIBER_DOES_NOT_EXIST	= 0x82;	/*Incase of request for subscriber that doesnot exist*/
	public static final short ILLIGAL_WEEKDAY		= 5;	/*Incase of Invalid WEEDAY*/
	public static final short INVALID_PACKET		= 0x01;	/*Incase of Content of the packet is malformed */
	public static final short PASSWORD_NOT_MATCHED		= 0x89;	/*Incase of Incorrect password*/
	public static final short DB_ERROR			= 0x9F;	/*Incase of any kind of error related to database*/
	public static final short SUBSCRIBER_GIFT_PENDING	= 55;	/*Incase of presenting a gift to a non-HT subscriber already having one gift*/
	public static final short GIFT_INBOX_FULL		= 56;	/*Incase of presenting one more gift to HT subscriber already having 5 gift in the inbox*/
	public static final short SYNTAX_ERROR			= 1001;	/*Incase of malformed request*/

    //Enabling-Disabling Shuffle through Promo
	public static final String ALREADY_SHUFFLE_ACTIVATED = "ALREADY_SHUFFLE_ACTIVATED";
	public static final String ALREADY_SHUFFLE_DEACTIVATED = "ALREADY_SHUFFLE_DEACTIVATED";
	//sms api
	public static final String SMS_SUBSCRIBER_ID = "SMS_SUBSCRIBER_ID";
    public static final String SUBSCRIBER_OBJ = "SUBSCRIBER_OBJ";
    public static final String IS_RETAILER = "IS_RETAILER";
    public static final String CALLER_ID = "CALLER_ID";
    public static final String FEATURE = "FEATURE";
    public static final String SMS_CHARGING_MODEL = "SMS_CHARGING_MODEL";
    public static final String REGEX_TYPE = "REGEX_TYPE";
    public static final String SUBSCRIPTION_TYPE = "SUBSCRIPTION_TYPE";
    public static final String CLASS_TYPE = "CLASS_TYPE";
    public static final String SUB_CLASS_TYPE = "SUB_CLASS_TYPE";
    public static final String CUSTOMIZE_KEYWORD = "CUST_KEYWORD";
    public static final String ACT_BY = "ACT_BY";
    public static final String ACT_INFO = "ACT_INFO";
    public static final String SMS_SELECTED_BY = "SMS_SELECTED_BY";
    public static final String SMS_SELECTION_INFO = "SMS_SELECTION_INFO";
    public static final String CLIP_OBJECT = "CLIP_OBJECT";
    public static final String CATEGORY_OBJECT = "CATEGORY_OBJECT";
    public static final String SEARCH_TYPE = "SEARCH_TYPE";
    public static final String LANGUAGE = "LANGUAGE";
    public static final String NEWS_BEAUTY_FEED = "NEWS_BEAUTY_FEED";
    public static final String RETURN_STRING = "RETURN_STRING";
    public static final String RETURN_CODE = "RETURN_CODE";
    public static final String IS_PREPAID = "IS_PREPAID";
    public static final String SEARCH_STRING = "SEARCH_STRING";
    public static final String IS_ACTIVATION_REQUEST = "IS_ACTIVATION_REQUEST";
    public static final String FAILURE_TEXT = "FAILURE_TEXT";
    public static final String SUCCESS_TEXT = "SUCCESS_TEXT";
    public static final String DAYS = "DAYS";
    public static final String IS_CTONE = "IS_CTONE";
    public static final String PROMO_TYPE = "PROMO_TYPE";
    public static final String OCG_CHARGE_ID = "OCG_CHARGE_ID";
    public static final String REQUEST_TYPE = "REQUEST_TYPE";
    public static final String TRANS_ID = "TRANS_ID";
    public static final String SONG_SET_RESPONSE = "SONG_SET_RESPONSE";
    public static final String SG_CAT_ID = "SG_CAT_ID";
    public static final String SG_MODE = "SG_MODE";
    public static final String WDS_ALLOW= "WDS_ALLOW";
    public static final String EXTRA_INFO_WDS= "EXTRA_INFO_WDS";
    
 // Added by Ankur for TATA CDMA.. SMS Type - Parameters Table
    public static final String ADD_PRICE_AND_VALIDITY = "ADD_PRICE_AND_VALIDITY";
    public static final String ADD_MOVIE_NAME = "ADD_MOVIE_NAME";
    public static final String VALID_IP = "VALID_IP";
    public static final String VALID_SERVER_IP = "VALID_SERVER_IP";
    public static final String VALID_PREPAID_IP = "VALID_PREPAID_IP";
    public static final String SMS_NO = "SMS_NO";
    public static final String SMS_DB_URL = "SMS_DB_URL";
    public static final String RBT_KEYWORDS = "RBT_KEYWORDS";
    public static final String MORE_RBT_KEYWORDS = "MORE_RBT_KEYWORDS";
    public static final String HELP_KEYWORDS = "HELP_KEYWORDS";
    public static final String RETAILER_REQUEST_ACCEPT_KEYWORD = "RETAILER_REQUEST_ACCEPT_KEYWORD";
    public static final String PARENT_CAT_LIST_FOR_CAT_SEARCH = "PARENT_CAT_LIST_FOR_CAT_SEARCH";
    public static final String MAX_CLIP_SEARCH_RESULTS_COUNT = "MAX_CLIP_SEARCH_RESULTS_COUNT";
    public static final String SMS_ACT_PROMO_PREFIX = "SMS_ACT_PROMO_PREFIX";
    public static final String DEFAULT_SUB_TYPE = "DEFAULT_SUB_TYPE";
    public static final String ACTIVATION_KEYWORD = "ACTIVATION_KEYWORD";
    public static final String DEACTIVATION_KEYWORD = "DEACTIVATION_KEYWORD";
    public static final String UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD = "UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD";
    public static final String SUPPORTED_LANGUAGES = "SUPPORTED_LANGUAGES";
    public static final String RBT_KEYWORD_OPTIONAL = "RBT_KEYWORD_OPTIONAL";
    public static final String SMS_START_STRING = "SMS_START_STRING";
    public static final String RETAILER_SUBSCRIPTION_CLASS = "RETAILER_SUBSCRIPTION_CLASS";
    public static final String RETAILER_SELECTION_CATEGORY = "RETAILER_SELECTION_CATEGORY";
    public static final String HELP_TEXT = "HELP_TEXT";
    public static final String SMS_CATEGORY_ID = "SMS_CATEGORY_ID";
    public static final String LUCENE_MAX_RESULTS_TO_SEND = "LUCENE_MAX_RESULTS_TO_SEND";
    public static final String SMS_HELP_MESSAGE = "SMS_HELP_MESSAGE";
    public static final String ADD_MOVIE_NAME_IN_SMS = "ADD_MOVIE_NAME_IN_SMS";
    public static final String DEFAULT_SEARCH_ON = "DEFAULT_SEARCH_ON";
    public static final String ACTIVATE_AFTER_SEARCH = "ACTIVATE_AFTER_SEARCH";
    public static final String IS_ACT_OPTIONAL = "IS_ACT_OPTIONAL";
    public static final String REQUEST_SEARCH_ON_MAP = "REQUEST_SEARCH_ON_MAP";
    public static final String CRICKET_INTERVAL = "CRICKET_INTERVAL";
    public static final String LOOP_KEYWORDS = "LOOP_KEYWORDS";
    public static final String DELETE_KEYWORDS = "DELETE_KEYWORDS";
    public static final String DELETE_MORE_KEYWORDS = "DELETE_MORE_KEYWORDS";
    public static final String MAX_DELETE_RESULTS = "MAX_DELETE_RESULTS";
    public static final String RBT_KEYWORD = "RBT_KEYWORD";
    public static final String HELP_KEYWORD = "HELP_KEYWORD";
    public static final String CATEGORY_SEARCH_KEYWORD = "CATEGORY_SEARCH_KEYWORD";
    public static final String SPECIFIC_CATEGORIES_SEARCH_KEYWORD = "SPECIFIC_CATEGORIES_SEARCH_KEYWORD";
    public static final String REQUEST_RBT_KEYWORD = "REQUEST_RBT_KEYWORD";
    public static final String REQUEST_OPTIN_RBT_KEYWORD = "REQUEST_OPTIN_RBT_KEYWORD";
    public static final String NEWS_AND_BEAUTY_FEED_KEYWORD = "NEWS_AND_BEAUTY_FEED_KEYWORD";
    public static final String VIEW_SUBSCRIPTION_STATISTICS_KEYWORD = "VIEW_SUBSCRIPTION_STATISTICS_KEYWORD";
    public static final String RMVCALLERID_KEYWORD = "RMVCALLERID_KEYWORD";
    public static final String TEMPORARY_OVERRIDE_CANCEL_MESSAGE = "TEMPORARY_OVERRIDE_CANCEL_MESSAGE";
    public static final String WEEKLY_TO_MONTHLY_CONVERSION = "WEEKLY_TO_MONTHLY_CONVERSION";
    public static final String GIFT_KEYWORD = "GIFT_KEYWORD";
    public static final String POLLON_KEYWORD = "POLLON_KEYWORD";
    public static final String POLLOFF_KEYWORD = "POLLOFF_KEYWORD";
    public static final String CANCELCOPY_KEYWORD = "CANCELCOPY_KEYWORD";
    public static final String COPY_CONFIRM_KEYWORD = "COPY_CONFIRM_KEYWORD";
    public static final String COPY_CANCEL_KEYWORD = "COPY_CANCEL_KEYWORD";
    public static final String COPY_KEYWORDS = "COPY_KEYWORDS";
    public static final String PROMOTION1 = "PROMOTION1";
    public static final String PROMOTION2 = "PROMOTION2";
    public static final String SONG_PROMOTION1 = "SONG_PROMOTION1";
    public static final String SONG_PROMOTION2 = "SONG_PROMOTION2";
    public static final String VIRAL_KEYWORD = "VIRAL_KEYWORD";
    public static final String WEB_REQUEST_KEYWORD = "WEB_REQUEST_KEYWORD";
    public static final String MGM_ACCEPT_KEY = "MGM_ACCEPT_KEY";
    public static final String RENEW_KEYWORD = "RENEW_KEYWORD";
    public static final String TNB_KEYWORDS = "TNB_KEYWORDS";
    public static final String REFER_KEYWORDS = "REFER_KEYWORDS";
    public static final String LIST_PROFILE_KEYWORD = "LIST_PROFILE_KEYWORD";
    public static final String NEXT_PROFILE_KEYWORD = "NEXT_PROFILE_KEYWORD";
    public static final String NAV_DEACT_KEYWORD = "NAV_DEACT_KEYWORD";
    public static final String MANAGE_DEACT_KEYWORD = "MANAGE_DEACT_KEYWORD";
    public static final String SEL_KEYWORD1 = "SEL_KEYWORD1";
    public static final String SEL_KEYWORD2 = "SEL_KEYWORD2";
    public static final String LISTEN_KEYWORD = "LISTEN_KEYWORD";
    public static final String SONG_CATCHER_ACCEPT_KEYWORD = "SONG_CATCHER_ACCEPT_KEYWORD";
    public static final String SET_NEWSLETTER_ON_KEYWORDS = "SET_NEWSLETTER_ON_KEYWORDS";
    public static final String SET_NEWSLETTER_OFF = "SET_NEWSLETTER_OFF";
    public static final String TOP_CLIPS_KEYWORD = "TOP_CLIPS_KEYWORD";
    public static final String TOP_CATEGORIES_KEYWORD = "TOP_CATEGORIES_KEYWORD";
    public static final String MANAGE_KEYWORD = "MANAGE_KEYWORD";
    public static final String DISABLE_INTRO = "DISABLE_INTRO";
    public static final String DISABLE_OVERLAY_KEYWORD = "DISABLE_OVERLAY_KEYWORD";
    public static final String ENABLE_OVERLAY_KEYWORD = "ENABLE_OVERLAY_KEYWORD";
    public static final String DOWNLOADS_LIST_KEYWORD = "DOWNLOADS_LIST_KEYWORD";
    public static final String REQUEST_RBT_KEYWORD_OPTIONAL = "REQUEST_RBT_KEYWORD_OPTIONAL";
    public static final String CHURN_OFFER = "CHURN_OFFER";
    public static final String RDC_SEL_KEYWORD = "RDC_SEL_KEYWORD";
    public static final String INIT_GIFT_KEYWORD = "INIT_GIFT_KEYWORD";
    public static final String INIT_GIFT_CONFIRM_KEYWORD = "INIT_GIFT_CONFIRM_KEYWORD";
    public static final String DISCOUNTED_SEL_KEYWORD = "DISCOUNTED_SEL_KEYWORD";
    public static final String CONTEST_INFLUENCER_KEYWORD = "CONTEST_INFLUENCER_KEYWORD";
    public static final String CONSENT_YES_KEYWORD = "CONSENT_YES_KEYWORD";
    public static final String CONSENT_NO_KEYWORD = "CONSENT_NO_KEYWORD";
    public static final String CP_SEL_CONFIRM_KEYWORD = "CP_SEL_CONFIRM_KEYWORD";
    public static final String COMBO_PACK_KEYWORD = "COMBO_PACK_KEYWORD";
    public static final String VOUCHER_KEYWORD = "VOUCHER_KEYWORD";
    public static final String UPGRADE_SEL_KEYWORD = "UPGRADE_SEL_KEYWORD";
    public static final String MUSIC_PACK_KEYWORD = "MUSIC_PACK_KEYWORD";
    public static final String PREMIUM_SELECTION_CONFIRMATION_KEYWORD = "PREMIUM_SELECTION_CONFIRMATION_KEYWORD";
    public static final String DOUBLE_OPT_IN_CONFIRMATION_KEYWORD = "DOUBLE_OPT_IN_CONFIRMATION_KEYWORD";
    //RBT-15149
    public static final String SINGLE_KEYWORD_FOR_DOUBLE_CONFIRMATION = "SINGLE_KEYWORD_FOR_DOUBLE_CONFIRMATION";
    public static final String param_viral_data = "viral_data";
    //SMS OPTOUT KEYWORD
    public static final String param_srvkey = "SRVKEY";
    public static final String RECHARGE_SMS_OPTOUT_KEYWORD ="RECHARGE_SMS_OPTOUT_KEYWORD";
    public static final String URL_FOR_UPDATING_DND_OF_SUBSCRIBER_WITH_SM ="URL_FOR_UPDATING_DND_OF_SUBSCRIBER_WITH_SM";
    //Gift Accept, Reject, Download keyword added
    public static final String GIFT_ACCEPT_KEYWORD = "GIFT_ACCEPT_KEYWORD";
    public static final String GIFT_REJECT_KEYWORD = "GIFT_REJECT_KEYWORD";
    public static final String GIFT_DOWNLOAD_KEYWORD = "GIFT_DOWNLOAD_KEYWORD";
    //For Viral SMS STOP/START feature
    public static final String VIRAL_START_KEYWORD = "VIRAL_START_KEYWORD";
    public static final String VIRAL_STOP_KEYWORD = "VIRAL_STOP_KEYWORD";
    //For randomization and unrandomization of Shuffles
    public static final String RANDOMIZE_KEYWORD = "RANDOMIZE_KEYWORD";
    public static final String UNRANDOMIZE_KEYWORD = "UNRANDOMIZE_KEYWORD";
    //For Search by SongCode
    public static final String SONG_CODE_REQUEST_KEYWORD = "SONG_CODE_REQUEST_KEYWORD";
    //BASE UPGRADTION
    public static final String BASE_UPGRADATION_KEYWORD = "BASE_UPGRADATION_KEYWORD";
    public static final String VIRAL_OPTOUT_KEYWORD = "VIRAL_OPTOUT_KEYWORD";
    public static final String VIRAL_OPTIN_KEYWORD = "VIRAL_OPTIN_KEYWORD";
    public static final String DOWNLOAD_SET_KEYWORD = "DOWNLOAD_SET_KEYWORD";

    public static final String INIT_RANDOMIZE_KEYWORD = "INIT_RANDOMIZE_KEYWORD";
    
    public static final String RESUBSCRIPTION_FEATURE_KEYWORD = "RESUBSCRIPTION_FEATURE_KEYWORD";
    public static final String PRE_GIFT_KEYWORD = "PRE_GIFT_KEYWORD";
    public static final String PRE_GIFT_CONFIRM_KEYWORD = "PRE_GIFT_CONFIRM_KEYWORD";
    public static final String SUPRESS_PRERENEWAL_SMS_KEYWORD = "SUPRESS_PRERENEWAL_SMS_KEYWORD";
    public static final String OUI_SMS_KEYWORD = "OUI_SMS_KEYWORD";
    public static final String SMS_CANCEL_DEACTIVATION_KEYWORD = "SMS_CANCEL_DEACTIVATION_KEYWORD";
    public static final String SMS_BASE_SONG_UPGRADE_KEYWORD = "SMS_BASE_SONG_UPGRADE_KEYWORD";
    public static final String TIME_OF_DAY_SETTING_KEYWORD = "TIME_OF_DAY_SETTING_KEYWORD";
    public static final String SMS_CHURN_OFFER_KEYWORD = "SMS_CHURN_OFFER_KEYWORD";
    //RBT-15026 - Azaan Search.// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    public static final String AZAAN_REQUEST_RBT_KEYWORD = "AZAAN_REQUEST_RBT_KEYWORD";
    public static final String AZAAN_SEARCH_FAILURE  = "AZAAN_SEARCH_FAILURE";
    public static final String AZAAN_SEARCH_FAILURE1 = "AZAAN_SEARCH_FAILURE1";
    
	// RBT like feature keywords
	public static final String RBT_LIKE_KEYS = "RBT_LIKE_KEYS";
	public static final String RBT_LIKE_CONFIRM_KEYWORD = "RBT_LIKE_CONFIRM_KEYWORD";
	public String RBT_LIKE_CONFIRM_SUCCESS = "RBT_LIKE_CONFIRM_SUCCESS";
	public String RBT_LIKE_CONFIRM_FAILURE = "RBT_LIKE_CONFIRM_FAILURE";
	public String RBT_LIKE_SUCCESS_MESSAGE = "RBT_LIKE_SUCCESS_MESSAGE";
	public String RBT_LIKE_ACCEPT_MESSAGE = "RBT_LIKE_ACCEPT_MESSAGE";
	public String RBT_LIKE_ACCEPT_MESSAGE_FOR_ACTIVE_USER = "RBT_LIKE_ACCEPT_MESSAGE_FOR_ACTIVE_USER";
	public String RBT_LIKE_ACCEPT_MESSAGE_FOR_INACTIVE_USER = "RBT_LIKE_ACCEPT_MESSAGE_FOR_INACTIVE_USER";
	public String RBT_LIKE_MODE = "RBT_LIKE_MODE";
	public String RBT_LIKE_SUB_CLASS = "RBT_LIKE_SUB_CLASS";
	public String RBT_LIKE_CHARGE_CLASS = "RBT_LIKE_CHARGE_CLASS";
	
    //Recommended songs //added by ganesh
    public static final String SMS_RECOMMEND_SONGS_KEYWORD = "SMS_RECOMMEND_SONGS_KEYWORD";
    public static final String RECOMMEND_SONGS = "RECOMMEND_SONGS";

	
	//COPY Contest Influencer Feature
	public static final String COPY_CONTEST_THRESHOLD_BELOW_SMS = "COPY_CONTEST_THRESHOLD_BELOW_SMS";
	public static final String COPY_CONTEST_THRESHOLD_ABOVE_SMS = "COPY_CONTEST_THRESHOLD_ABOVE_SMS";
	public static final String COPY_CONTEST_FINAL_SMS = "COPY_CONTEST_FINAL_SMS";
	
	public static final String POINTS_CONTEST_THRESHOLD_BELOW_SMS = "POINTS_CONTEST_THRESHOLD_BELOW_SMS";
	public static final String POINTS_CONTEST_THRESHOLD_ABOVE_SMS = "POINTS_CONTEST_THRESHOLD_ABOVE_SMS";
	public static final String POINTS_CONTEST_FINAL_SMS = "POINTS_CONTEST_FINAL_SMS";
	
	public static final String LOTTERY_LIST_KEYWORD = "LOTTERY_LIST_KEYWORD";

    //TNB keyword added 
    public static final String TNB_KEYWORD = "TNB_KEYWORD";
    public static final String SONGOFMONTH = "SONGOFMONTH";
    public static final String IS_ACT_REQUEST = "IS_ACT_REQUEST";
    public static final String CHECK_CLIP_SMS_ALIAS = "CHECK_CLIP_SMS_ALIAS";
    public static final String CHECK_CATEGORY_SMS_ALIAS = "CHECK_CATEGORY_SMS_ALIAS";
    public static final String CLIP_SMS_ALIASES = "CLIP_SMS_ALIASES";
    public static final String CATEGORY_SMS_ALIASES = "CATEGORY_SMS_ALIASES";
    public static final String REMOVE_LEADING_ZERO = "REMOVE_LEADING_ZERO";
    public static final String MERIDHUN_KEYWORD = "MERIDHUN_KEYWORD";
    public static final String SEND_SEARCH_SMS_ALIAS = "SEND_SEARCH_SMS_ALIAS";
    public static final String CHECK_CLIP_PROMO_ID = "CHECK_CLIP_PROMO_ID";

    //Added by Sreekar for airtel comes with music opt in
    public static final String CONFIRM_CHARGE_KEYWORD = "CONFIRM_CHARGE_KEYWORD";

    //Added for lock feature in vodafone
    public static final String LOCK_KEYWORD = "LOCK_KEYWORD";
    public static final String UNLOCK_KEYWORD = "UNLOCK_KEYWORD";

    public static final String ODA_KEYWORD = "ODA_KEYWORD";
    public static final String LIST_CATEGORIES_KEYWORD = "LIST_CATEGORIES_KEYWORD";
    
    public static final String EMOTION_KEYWORD = "EMOTION_KEYWORD";
    public static final String EMOTION_EXTEND_KEYWORD = "EMOTION_EXTEND_KEYWORD";
    public static final String EMOTION_DCT_KEYWORD="EMOTION_DCT_KEYWORD";
    public static final String DOWNLOAD_OPTIN_RENEWAL_KEYWORD="DOWNLOAD_OPTIN_RENEWAL_KEYWORD";
    
    //Added for the UDS enable/disable feature
    
    public static final String UDS_ENABLE = "UDS_ENABLE_KEYWORD";
    public static final String UDS_DISABLE = "UDS_DISABLE_KEYWORD";
    public static final String DOWNLOAD_OPTIN_RENEWAL = "DOWNLOAD_OPTIN_RENEWAL";
    
    //BULK PROMO SMS TYPES
    public static final String BPST_PREFIX = "PREFIX";
    public static final String BPST_ACTIVATION = "ACTIVATION";
    public static final String BPST_DEACTIVATION = "DEACTIVATION";
    public static final String BPST_SELECTION = "SELECTION";
    public static final String BPST_CLIP = "CLIP";
    public static final String BPST_SUBSCRIBER_STATUS = "SUBSCRIBER_STATUS";
    public static final String BPST_RETAILER_SEARCH = "RETAILER_SEARCH";
    public static final String BPST_RETAILER = "RETAILER";
    public static final String BPST_RETAILER_REQUEST = "RETAILER_REQUEST";
    public static final String BPST_RETAILER_USER = "RETAILER_USER";
    public static final String BPST_RETAILER_SUBSCRIBER = "RETAILER_SUBSCRIBER";
    public static final String BPST_ERROR = "ERROR";
    public static final String BPST_HELP = "HELP";
    public static final String BPST_SEARCH = "SEARCH";
    public static final String BPST_LOOP = "LOOP";
    public static final String BPST_DELETE = "DELETE";
    public static final String BPST_COPY_CONFIRM = "COPY_CONFIRM";
    public static final String BPST_COPY_CANCEL = "COPY_CANCEL";
    
    //General
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String NO_CONSENT = "NO_CONSENT";
    
    //Prefix
    public static final String INVALID = "INVALID";
    public static final String BLACK_LISTED = "BLACK_LISTED";
    
    //Subscriber status
    public static final String ACTIVATION_PENDING = "ACTIVATION_PENDING";
    public static final String DEACTIVATION_PENDING = "DEACTIVATION_PENDING";
    public static final String EXPRESS_COPY_PENDING = "EXPRESS_COPY_PENDING";
    public static final String GIFTING_PENDING = "GIFTING_PENDING";
    public static final String RENEWAL_PENDING = "RENEWAL_PENDING";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String NOT_ACTIVE = "NOT_ACTIVE";
    public static final String LOCKED = "LOCKED";
    
    //Clip
    public static final String EXPIRED = "EXPIRED";
    public static final String NOT_AVAILABLE = "NOT_AVAILABLE";
    
    //Activation
    public static final String ALREADY_ACTIVE = "ALREADY_ACTIVE";
    
    //Selection
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
    
    //Retailer Search
    public static final String NO_RESULTS = "NO_RESULTS";
    public static final String SMS_FAILURE = "SMS_FAILURE";
    
    //Retailer
    public static final String NON_RETAILER = "NON_RETAILER";
    public static final String SELECTION_FAILURE = "SELECTION_FAILURE";
    public static final String SELECTION_EXISTS = "SELECTION_EXISTS";
    public static final String SUBSCRIPTION = "SUBSCRIPTION";
    public static final String SELECTION = "SELECTION";
    public static final String ONLY_SUBSCRIPTION_SUCCESS = "ONLY_SUBSCRIPTION_SUCCESS";
    public static final String INVALID_RET_PACK_CODE = "INVALID_RET_PACK_CODE";
    
    //Retailer Request
    public static final String EXISTS = "EXISTS";
    public static final String ACCEPT = "ACCEPT";
    
    
    //Error
    public static final String TECHNICAL_FAILURE = "TECHNICAL_FAILURE";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    public static final String AZAAN_TECHNICAL_FAILURE = "AZAAN_TECHNICAL_FAILURE";
    public static final String ACCESS_FAILURE = "ACCESS_FAILURE";
    public static final String RDC_TECHNICAL_FAILURE = "RDC_TECHNICAL_FAILURE";
    
    //Search
    public static final String SETTING_EXISTS = "SETTING_EXISTS";
    public static final String REQUEST_RBT_SMS2_SUCCESS = "REQUEST_RBT_SMS2_SUCCESS";
    public static final String REQUEST_RBT_SMS3_SUCCESS = "REQUEST_RBT_SMS3_SUCCESS";
    public static final String REQUEST_RBT_SMS1_SUCCESS = "REQUEST_RBT_SMS1_SUCCESS";
    public static final String REQUEST_RBT_SMS1_FAILURE = "REQUEST_RBT_SMS1_FAILURE";
    public static final String REQUEST_RBT_SMS2_FAILURE = "REQUEST_RBT_SMS2_FAILURE";
    public static final String REQUEST_RBT_SMS3_FAILURE = "REQUEST_RBT_SMS3_FAILURE";
    public static final String ACTIVATION_FAILURE = "ACTIVATION_FAILURE";
    public static final String DEACTIVATION_FAILURE = "DEACTIVATION_FAILURE";
    public static final String DEACTIVATION_FAILURE_NON_COMBO_USER = "DEACTIVATION_FAILURE_NON_COMBO_USER";
    public static final String DEACTIVATION_FAILURE_COMBO_USER = "DEACTIVATION_FAILURE_COMBO_USER";
    public static final String MORE_RBT_SMS1_FAILURE = "MORE_RBT_SMS1_FAILURE";
    public static final String TEMPORARY_OVERRIDE_FAILURE = "TEMPORARY_OVERRIDE_FAILURE";
    public static final String TEMPORARY_OVERRIDE_TIME_FAILURE = "TEMPORARY_OVERRIDE_TIME_FAILURE";
    public static final String TEMPORARY_OVERRIDE_SUCCESS = "TEMPORARY_OVERRIDE_SUCCESS";
    public static final String TEMPORARY_OVERRIDE_ACTIVE_SUCCESS = "TEMPORARY_OVERRIDE_ACTIVE_SUCCESS";
    public static final String SMS_ALIAS_CLIP_INVALID = "SMS_ALIAS_CLIP_INVALID";
    public static final String SMS_ALIAS_CLIP_EXPIRED = "SMS_ALIAS_CLIP_EXPIRED";
    public static final String SMS_ALIAS_ONLY_CLIP_SUCCESS = "SMS_ALIAS_ONLY_CLIP_SUCCESS";
    public static final String SMS_ALIAS_CLIP_SUCCESS = "SMS_ALIAS_CLIP_SUCCESS";
    public static final String SMS_ALIAS_CATEGORY_NO_CLIP = "SMS_ALIAS_CATEGORY_NO_CLIP";
    public static final String CATEGORY_EXPIRED = "CATEGORY_EXPIRED";
    public static final String CLIP_NOT_AVAILABLE = "CLIP_NOT_AVAILABLE";
    public static final String CLIP_EXPIRED = "CLIP_EXPIRED";
    public static final String FEED_FAILURE = "FEED_FAILURE";
    public static final String FEED_SUCCESS = "FEED_SUCCESS";
    public static final String DEACTIVATION_SUCCESS = "DEACTIVATION_SUCCESS";
    public static final String DEACTIVATE_SONG_SUCCESS = "DEACTIVATE_SONG_SUCCESS";
    public static final String UPGRADE_ON_DELAY_DCT = "UPGRADE_ON_DELAY_DCT";
    public static final String DISCOUNTED_SEL_FAILURE = "DISCOUNTED_SEL_FAILURE";
    public static final String TIME_OF_DAY_FAILURE = "TIME_OF_DAY_FAILURE";
    public static final String CONTEST_INFLUENCER_NOT_FOUND = "CONTEST_INFLUENCER_NOT_FOUND";
    public static final String CONTEST_INFLUENCER_ALREADY_CONFIRMED = "CONTEST_INFLUENCER_ALREADY_CONFIRMED";
    public static final String CONTEST_INFLUENCER_SUCCESS = "CONTEST_INFLUENCER_SUCCESS";
    public static final String CONSENT_PROFILE_SELECTION_SUCCESS = "CONSENT_PROFILE_SELECTION_SUCCESS";
    public static final String DOUBLE_OPT_IN_COMBO_SUCCESS = "DOUBLE_OPT_IN_COMBO_SUCCESS";
    public static final String DOUBLE_OPT_IN_SEL_SUCCESS = "DOUBLE_OPT_IN_SELECTION_SUCCESS";
    public static final String DOUBLE_OPT_IN_SEL_FAILURE = "DOUBLE_OPT_IN_SELECTION_FAILURE";
    public static final String DOUBLE_OPT_IN_ACT_SUCCESS = "DOUBLE_OPT_IN_ACT_SUCCESS";
    public static final String DOUBLE_OPT_IN_ACT_FAILURE = "DOUBLE_OPT_IN_ACT_FAILURE";
    public static final String DOUBLE_OPT_IN_NO_ENTRIES_FOUND = "DOUBLE_OPT_IN_NO_ENTRIES_FOUND";
    
    //Loop
    public static final String NO_DOWNLOADS = "NO_DOWNLOADS";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String NO_SETTINGS = "NO_SETTINGS";
      
    //Delete
    public static final String INVALID_SUB = "INVALID_SUB";
    public static final String MORE_INVALID = "MORE_INVALID";
    public static final String NUMBER_INVALID = "NUMBER_INVALID";
    public static final String CODE_INVALID = "CODE_INVALID";
    public static final String ALREADY_DELETED = "ALREADY_DELETED";
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";
    
    String m_giftAlreadyExistsDefault ="Your Welcome Tune Gift has failed as the receiver already has the song %songName% in his gallery. You are not charged for Gifting. Only SMS Charge is applicable. Thank You.";
    String m_maxDownloadLimitExceeded ="Your Welcome Tune Gift has failed as the receiver's gallery is full. You are not charged for Song gifting. Only SMS Charge is applicable. Thank You.";
    String m_maxToneGiftLimitExceeded ="Your Welcome Tune Gift has failed as you have exceeded the maximum gift limit.";
    String m_serviceNotAllowed = "This service is not available for you.";
    String m_technicalFailuresDefault = "I am sorry, we are having some technical difficulties. Please try later";
    String m_DctDownloadDoubleConfirmationDefault = "Dear user, we received your request to continue to deactivate the service.send the same message.";
    String m_giftOwnNumberFailure = "Your Welcome Tune Gift %SONG_NAME has failed as you tried to gift yourself";
    String m_giftNotAllowed = "Your Welcome Tune Gift has failed as gifting is not allowed";
    String m_giftClipExpired = "Your Welcome Tune Gift has failed as the song %SONG_NAME you tried to gift is expired";
    String m_giftClipNotExists = "Your Welcome Tune Gift has failed as the song %SONG_NAME you tried to gift does not exists";
    public String m_invalidPrefixDefault = "You are not authorized to use this service. We apologize the inconvenience";
	public String m_deactivationPendingDefault = "Your deactivation request is pending.";
    String m_errorDefault = "You are not subscribed to this service";
    public String m_helpDefault = "Usage Keyword <Duration in hours>. Keyword(s) are %K";
    public String m_referDefaultSuccess = "Refer successful";
    public String m_referDefaultFailure = "Refer failed";
    public String m_RetailerSuccess = "You have been successfully activated";
    public String m_RetailerFailure = "User could not be activated";
    String m_clipNotAvailableDefault = "Sorry!!! The requested song is not available";
    String m_clipExpiredDefault = "Sorry!!! The requested song is no longer available";
    String m_corpChangeSelectionFailureDefault = "Selection change for all users not allowed for CORPORATE subscribers";
    String m_suspendedTextDefault = "The number is suspended from RBT. Plz try later.";
    String m_lockedTextDefault = "The subscriber is locked. Please unlock to access this service.";
    String m_retAccSelSMSDefault = "Your selections has been changed to %L";
    String m_retAccActSMSDefault = "You will be activated %L on RingbackTone in the next 24 hrs";
    String m_retRespSMSAcceptDefault = " %L has accepted your RBT.";
    String m_retFailureTextDeactPending = "The subscriber deactivation is pending";
    String m_retFailureTextAlreadyActive = "The subscriber is already active";
    String m_retFailureTextSuspended = "The subscriber is suspended";
    String m_retFailureTextBlackListed = "The subscriber is blacklisted";
    String m_retFailureTextDefault = "You have sent a wrong SMS. Please try again";
    String m_retFailureTextInactiveUser = "The subscriber is inactive please activate him first";
    String m_retFailureTextTechnicalDifficulty = "Sorry we are facing some technical problems please try later";
    String m_retChargingSuccess = "You have been successfully charged";
    String m_retFailureTextLowBalance = "You do not have sufficient funds";
    String m_retFailureTextClipDoesNotExist = "The Clip you have requested for does not exist";
    String m_retFailureTextInvalidUser = "The user is invalid";
    String m_retSuccessTextDefault = "You have activated %L. The activation will be done in the next 24 hours.";
    String m_retSongSuccessTextDefault = "As per ur request the song for %S has been changed to %C";
    String m_retReqResActDefault = "An activation request has been received for you. Send RBT ACCEPT to accept the selection.";
    String m_retReqResSelSuccessful = "The song %SONG_NAME has been successfully set as your caller tune";
    String m_retReqResActSuccessful = "You have been successfully activated";
    String m_retReqResActnSelSuccessful = "You have been successfully activated and song %SONG_NAME has been successfully set as your caller tune ";
    String m_retReqResSelDefault = "Request to change your selection to %L received.";
    String m_mgmRecAccFailureDefault = "No activation request received for you";
    String m_mgmRecAccSuccessDefault = "You will be activated %L on RingbackTone in the next 24 hrs";
    String m_mgmSenderSuccessTextDefault = "You have succesfully made an activation request for %L.";
    String m_mgmSenderMinActFailureTextDefault = "You cannot gift as You are not active on RingBackTones for %L days.";
    String m_mgmSenderMaxGiftFailureTextDefault = "You have already made %L gifts in the last 30 days. Pls try later";
    String m_mgmSenderFailureTextDefault = "You have sent a invalid request.";
    String m_mgmRecSMSDefault = "An activation request %S is made by %C.The expiry for the same is %X. To accept the same SMS RBT ACK";
    String m_viralFailureTextDefault = "The request you have sent is invalid";
    String m_viralOptOutTextDefault = "You will be charged %PRICE, to cancel the request send OPTOUT to 54321";
    String m_viralDefaultSongTextDefault = "Default song not allowed";
    String m_viralOptInTextDefault = "You will be charged %PRICE, to confirm the request send OPTIN to 54321";
    String m_viralSuccessTextDefault = "RingbackTones has been activated as per your request";
    String m_viralSuccessTextDefaultActivation = "You have been successfully activated as per your request";
    String m_viralSuccessTextDefaultSongText = "Default RingbackTones has been activated as per your request";
    String m_viralEntryExpiredTextDefault = "The request you have sent is expired";
    String m_promotion1FailureTextDefault = "This product is already activated for you";
    String m_promotion1SuccessTextDefault = "RingbackTones has been activated as per your request";
    String m_promotion2SuccessTextDefault = "RingbackTones has been activated as per your request";
    String m_promotion2FailureTextDefault = "The request you have sent is invalid";
    String m_reqMoreSMSNoSearchDefault = "Sorry!!! You have not made any searches. SMS RBT FIND &lt;SONG NAME&gt; to search";
    // Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	String m_reqAzaanMoreSMSNoSearchDefault = "Sorry!!! You have not made any searches. SMS AZAANRBT to search";
    String m_reqMoreSMSExhaustedDefault = "You have exceeded the maximum matches for this search. Please make a new search by sending SMS RBT FIND &lt;SONG NAME&gt;";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    String m_reqAzaanMoreSMSExhaustedDefault = "You have exceeded the maximum matches for this search. Please make a new search by sending SMS AZAANRBT";
    String m_reqMoreSMSDefault = "To look for more matches for the same search SMS RBT MORE";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    String m_reqMoreAzaanSMSDefault = "To look for more matches for the same search SMS AZAANMORE";
    String m_profileSelNotAllowedForNewUser = "Profile Selection is not allowed for new user";
    String m_reqMoreSMSNoSearchCatDefault = "Sorry!!! You have not made any searches. SMS RBT CAT &lt;CATEGORY NAME&gt; to search";    
    String m_reqMoreSMSExhaustedCatDefault = "You have exceeded the maximum matches for this search. Please make a new search by sending SMS RBT CAT &lt;CATEGORY NAME&gt;";
    String m_reqMoreSMSCatDefault = "To look for more matches for the same search SMS MORE RBT";
    String m_reqMoreRetSMSExhaustedDefault = "You have exceeded the maximum matches for this search.";
    String m_requestRbtSuccess1Default = "To set any of the above send RBT REQ &lt;SONG NO&gt;";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    String m_requestAzaanSuccessDefault = "To activate any of the above send &lt;COS ID&gt; number";
    String m_reqNoMatchSMSDefault = "No matches. %L are the top selections. To set one of the above SMS RBT REQ &lt;SONG NO&gt;";
    String m_requestRbtFailure1Default = "No matches found. SMS RBT REQUEST &lt;SONG NAME&gt; to search another song";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    String m_requestAzaanSearchFailureDefault = "No Azaan matches found.";
    String m_requestAzaanSearchFailure1Default = "Invalid Number. SMS AZAANRBT to search another azaan pack";
    String m_requestRbtSuccess2Default = "As per your request %L has been set as your RingbackTone";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
    String m_requestAzaanRbtSuccessDefault = "As per your request %COS_ID pack has been activated";
    String m_requestRbtFailure2Default = "Invalid Number. SMS RBT REQUEST &lt;SONG NAME&gt; to search another song";
    String m_reqSetRetFailureDefault = "Invalid RBT GET request.";
    String m_reqNoMatchRetSMSDefault = "No matches.";
    String m_catRbtSuccess1Default = "To set any of the above send RBT CAT &lt;SONG NO&gt;";
    String m_catRbtFailure1Default = "No matches found. SMS RBT CAT &lt;CAT NAME&gt; to search another song";
    String m_catRbtSuccess2Default = "As per your request %S has been set as your RingbackTone";
    String m_catRbtFailure2Default = "Invalid Number. SMS RBT REQUEST &lt;SONG NAME&gt; to search another song";
    String m_catSearchNoCatDefault = "Sorry! No selections available for this category.";
    String m_nopromoIdDefault = "Sorry! No selections available for this promoId.";
    String m_feed1OnSuccessTextDefault = "As per your request CRICKET has been set as your RingBackTone";
    String m_feed1OnFailureTextDefault = "You are already active on CRICKET RingBackTone. Thank you";
    String m_feed1OffSuccessTextDefault = "As per your request CRICKET RingBackTone has been removed";
    String m_feed1OffFailureTextDefault = "You do not have active CRICKET RingBackTone";
    String m_feed1NonActiveTextDefault = "You are not active on RingBackTones. Please subscribe and then activate CRICKET RingBackTones";
    String m_temporaryOverrideSuccessDefault = "Your profile has been set. It will be reset to your original tune in 1 hr by default, or after the time specified by you";
    String m_consentSelectionSuccessDefault = "Your Profile Selection request has been received and It will be processed after taking consent.";
    String m_temporaryOverrideFailureDefault = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
    String m_temporaryOverrideTimeDefault = "The format of time is wrong. Please try again.";
    String m_renewSuccessDefault = "Your renew request has been recieved. You will be intimated for the same.";
    String m_renewInvalidRequestDefault = "You dont have any selection set for you.";
    String m_renewFailureDefault = "You have no selections to renew.";
    String m_renewSOngExpiredDefault = "This song is no longer available. Renew failed.";    
    String m_temporaryOverrideListSuccessDefault = "The available profiles are %L";
    String m_ShortCodeTemporaryOverrideListSuccessDefault = "The available profiles are %SONG_NAME";
    String m_temporaryOverrideListFailureDefault = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
    String m_tnbSuccessSMSDefault = "Thank you for continuing with RBT.";
    String m_tnbFailureSMSDefault = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
    String m_giftSuccessDefault = "You have successfully gifted %S to %C. You will be notified if the gift is accepted or rejected";
    String m_giftMobileFailureDefault = "You have sent an Invalid Mobile Number. Send RBT GIFT &lt;SONG CODE&gt &lt;MOBILE NO&gt;";
    String m_giftCodeFailureDefault = "You have sent an Invalid Promo ID. Send RBT GIFT &lt;SONG CODE&gt &lt;MOBILE NO&gt;";
    String m_giftGifteeInvalidFailureDefault = "You have sent an Invalid giftee ID.";
    String m_gifteeIdBlacklistedDefault = "The gifteeId u have sent is blacklisted";
    String m_giftFailureDefault = "You have sent an Invalid Gift SMS. Send RBT GIFT &lt;SONG CODE&gt &lt;MOBILE NO&gt; ";
    String m_giftHelpDefault = "To Gift a RingBackTone send RBT GIFT &lt;SONG CODE&gt; &lt;MOBILE NO&gt;";
    String m_giftInactiveGifterDefault = "Only RBT subscribers can gift RBT. Plz send START to activate and then gift.";
    String m_giftCallerStatusPendingDefault = "Try again after sometime.";
    String m_giftTechnicalDifficultiesFailureDefault = "The gift cannot be sent due to some error";
    String m_giftAlreadyPresentDefault = "The %S is already there with the %C";
    String m_giftAlreadyPendingDefault = "The same %S gift is already pending for the %C";
	String m_actGiftAlreadyPendingDefault = "A gift subscription is already pending for %C";
    String m_giftInvalidGifteeDefault = "%C is not a valid RBT user. Gifting failed";
    String m_pollONTechnicalDifficultiesDefault = "The poll cannot be activated due to some error";
    String m_pollONSuccessDefault = "The poll has been activated successfully";
    String m_pollOFFTechnicalDifficultiesDefault = "The poll cannot be deactivated due to some internal error";
    String m_pollOFFSuccessDefault = "The poll has been deactivated successfully";
    String m_pollSuscriberNotActiveDefault = "You are not active on the RBTPoll Service";
    String m_pollONRepeatDefault = "Polling service is already active."; 
    String m_copyCancelSuccessDefault = "Your request to cancel the Copy has been processed successfully";
    String m_copyCancelFailureDefault = "No press Star copy request are present for you to cancel";
    String m_copySuccessSMSDefault = "Your request copy selection from %L has been accepted";
	String m_copySuccessSMSDefault2 = "Your request copy selection from %L has been accepted. The expiry date for the same is %X";
    String m_copyFailureUGSSMSDefault = "%L has a more than one song as his RBT. copy failed. Plz try voice or web mode to copy.";
    String m_copyFailureSMSDefault = "Your request copy selection from %L has failed";
    String m_copyFailureNonOprDefault = "%L is not an Airtel subscriber. Copy failed.";
    String m_copyFailureSMSLoop = "Your request copy cannot be processed as the subscriber has loop selections";
    String m_copyFailureNonRBTDefault = "%L is not an HT subscriber. Copy failed.";
    String m_copyFailureAlbumDefault = "%L has an album as HT, so copy not allowed. Plz call 543211444 to copy.";
    String m_copyConfirmSuccessDefault = "The copy reqest will be processed."; 
    String m_copyConfirmFailureDefault = "The copy reqest will not be processed.";
    String m_likeConfirmSuccessDefault = "The like request will be processed."; 
    String m_likeConfirmFailureDefault = "The like request will not be processed.";
    String m_weeklyToMonthlyConversionSuccessDefault = "Your Weekly selections have been modified to Monthly";
    String m_weeklyToMonthlyConversionFailureDefault = "You do not have any Weekly selections";
    String m_rmvCallerIDSuccessDefault = "The RingBackTone selection for Caller ID %L has been removed";
    String m_rmvCallerIDDelayedDeactSuccessDefault = "Your deactivation request has been received and it will be processed within next 5 hours. Meanwhile call at 1234 short code to get some exciting offers.";
    String m_rmvCallerIDFailureDefault = "You have sent an invalid Caller ID";
    String m_deactivationSuccessDefault = "Your request has been received. You will be deactivated in the next 24 hours";
    String m_upgradeSuccessOnDelayDct = "Your request has been received. Your service will be upgraded soon";
    String m_deactivationFailureDefault = "You are not a subscriber.sms LIST or TOP10 to get song codes or SEARCH &lt;song name&gt; to search for your favorite song or HELP to get more information.";
    String m_deactivationFailureNonComboUserDefault = "Its invalid keyword send CT DCT to deactivate Caller Tunes.";
    String m_deactivationFailureComboUserDefault = "Its invalid keyword send CT DCT COMBO to deactivate Combo pack.";
    String m_deactivationFailureActDefault = "Your activation request is pending. Please try later.";
    String m_deactivationNotAllowedDefault = "Deactivation not allowed for You.";
    String m_deactivationConfirmTextDefault = "You will not be charged for subscription till %S. To deactivate send CT CAN with the next %C day(s).";
    String m_deactivationChurnOfferTextDefault = "You are a previliged subscriber, you have an offer of extending your validity for a few more days. To avail the offer, send CT OFFER. To deactivate, send CT DCT";
    String m_deactivationFailureDeactDefault = "You have already sent a deactivation request to this service";
    String m_actPromoSuccessTextDefault = "Your RingbackTones will be activated in the next 24 hours. Also your requested song will be set. The expiry for the same is %X";
    String m_activatedPromoSuccessTextDefault = "Your RingbackTones will be activated in the next 24 hours. Also your requested song will be set. The expiry for the same is %X And You will be charged %SEL_AMT";
    String m_consentActivatedPromoSuccessTextDefault = "Your RingbackTones Activation Request has been received. Also your requested song will be set after you give the consent to sent sms.";
    String m_consentPromoIdSuccessTextDefault = "Your Ringback tone Consent Selection Request has been received and It will be processed soon";
    String m_actMusicPackSuccessTextDefault = "Your RingbackTones for music pack will be activated in the next 24 hours. Also your requested song will be set. The expiry for the same is %X";
    String m_promoSuccessTextDefault = "Your requested song will be set. The expiry for the same is %X. To change the song selection send RBT &lt;code&gt;";
    String m_timeOfDaySettingTextDefault = "Your Requested time of the day setting has been set Succeesfully";
    String m_activationFailureActDefault = "Re-activation not possible immediately. Try after some time";
    String m_activationFailureDefault = "You have already sent a activation request to this service";
    String m_activationSuccessDefault = "Your request has been received. You will be activated in the next 24 hours";
    String m_consentActivationSuccessDefault = "Your consent activation request has been received. Your request will be processed soon";
    String m_activationPromoSuccessDefault = "Your request for  activation has been received. Your request will be processed soon";
    String m_selectionPromoSuccessDefault = "Your selection request has been received. Your request will be processed soon";
    String m_comboPromoSuccessDefault = "Your combo request for activation has been received. Your request will be processed soon";
    
    String m_baseOfferNotFound = "offer not avilable for the request";    
    String m_activationConsentSuccessDefault = "Your request has been received. Your activation request will be processed soon";
    String m_scratchCardActFailureDefault = "The activation request for this scratch card pin is failed.";
    String m_scratchCardActInvalidPinDefault = "The pin number you sent is invalid.";
    String m_actTrialFailureDefault = "This song is not available for selection. Pls try a differnet song";
    String m_promoIDFailureDefault = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
    String m_smsRequestFailureDefault = "The request that you have sent is not valid. Please check the format/keyword and resend your request";
    String m_smsSessionExpireDefault = "The session is been expired try new request";
    String m_promoSuccessWithUGSSongListTextDefault = "Your requested song will be set and to change the song selection send RBT &lt;code&gt;. The expiry for the same is %X. Other songs in the shuffle are";
    String m_callerIdSelectionBlockDefault = "You already have personalized selections set for %L. To set another personalized selections remove one of these by sending DEL <mobile no.>";
    String m_totalBlackListTextDefault = "You are not authorised to use this feature.";
    String m_ADRBTSelectionFailureDefault = "This selection is not allowed as you are a ADRBT subscriber."; 
    String m_categoryExpiredDefault = "Sorry!!! The requested category is no longer available";
    String m_SuspendedSelDefault = "Selection not alllowed. Your current is suspended. Plz call customer care"; 
    String m_OfferAlreadyUsed = "Selection not alllowed. You have already used the Offer "; 
    String m_selBlockedForStatus = "Selections not allowed as your status is blocked";
    String m_selectionLimitReachedDefault = "Dear subscriber, you have reached the song limit for this user. If you want to set delete any of the song and resend the SMS";
    String m_downloadLimitReachedDefault = "Dear subscriber, you have reached the download limit.";
    String m_selKeyword1SuccessTextDefault = "Subs not active. Send OK."; 
    String m_selKeyword2FailureTextDefault = "What are you trying"; 
    String m_overrideShuffleSelTextDefault = "Override Shuffle?"; 
    String m_CopyOverrideShuffleSelTextDefault = "Override Shuffle?"; 
    String m_CopyActivateSubTextDefault = "Activate ?"; 
    String m_CopyAcceptTextDefault = "Activated and set %C"; 
    String m_listenSuccessTextDefault = "To listen dial %L"; 
    String m_listenFailureTextDefault = "No such resource"; 
    String m_feed1FailureTextDefault = "Feed Failed";
    String m_renewInvalidCodeDefault = "Invalid Song Code.";
    String m_repeatTrialFailureDefault = "This trial is not available.";
    String m_WDSSuspendedText = "Sorry, you are not authorized to use this service";
	String m_songOfMonthFailureDefault = "This feature is not supported currently.";
	String m_giftCopyInvalidCallerIdDefault = "The number you have sent is invalid";
	String m_giftCopyReceivedCallerIdDefault = "We have received the Giftee number. Your gift request is in process";
	String m_giftCopyNoRequestsDefault = "Dear user, there is no pending gift request by you";
	String m_giftCanNotBeGiftedDefault = "Dear user, Can not gift the content to the number you sent";
	String m_confirmChargeSuccessDefault = "Dear user, we received your request to continue to the service.";
	String m_tnbExistingUser="Dear user, You are already a subscriber to this service.";
	String m_tnbOfferUsed="Dear user, You are have already used the offer.";
	String m_tnbSucceessMessage="The request for FREE TRIAL successful Default Jingle set for 7 days.";
	String m_tnbFailureMessage="The request for FREE TRIAL failed";
	String m_confirmChargeFailureDefault = "Dear user, the sms you sent is invalid.";
	String m_confirmChargeConfirmFailureDefault = "Dear user, pls confirm again on the confirmation.";
	String m_deactivationNotAllowedForGraceDefault = "You cannot deavtiate since your service is on grace";
	String m_deactivationNotAllowedForDelayedDeactDefault = "You cannot deavtiate, your delayed deactivation is already pending";
	String liteUserPremiumBlocked = "You are not authorised to access this content";
	String liteUserPremiumNotProcessed = "You are not authorised to access this content";
	String cosMismatchContentBlocked = "You can not access this content";
	String noActiveEmotionExists = "Currently you dont have any active emotion selection";
	String emotionRbtDeactivationSuccess = "You are successfully deactivated from Emotion Rbt service";
	String emotionRbtDeactivationFailure = "Your deactivation request from Emotion Rbt service is failed";
	String emotionRbtNotAllowedForSpecificCaller = "Emotion RBT can not be set for special callers. You can set for all callers only.";
	String invalidEmotionContent = "Invalid emotion song.";
	String m_rdcSelFailureTextDefault = "The request you have sent is invalid";
    String m_rdcSelSuccessTextDefault = "RingbackTones has been activated as per your request";
    String m_initGiftSuccessGifteeNewUser = "You selected CallerTunes- %SONG_NAME as a Gift for %CALLER_ID. You'll be charged 30Tk for his/her subscription & 10Tk for song. To confirm, reply 'GIFT YES %SMS_ID' to 3123";
    String m_initGiftSuccessGifteeActive = "You selected Caller Tunes- %SONG_NAME as a Gift for %CALLER_ID. You'll be charged 10Tk for song. To confirm, reply 'GIFT YES %SMS_ID' to 3123";
    String m_initGiftFailureGiftExistsInGifteeLibrary = "You selected CallerTunes- %SONG_NAME as a Gift. This is already available with %CALLER_ID ";
    String m_initGiftFailureNoPendingGifts = "No pending gifts available with the matching code sent";
    String m_discountedSelFailureDefault = "The discounted selection is failed as the promo code doesn't support discount";
    String m_timeOfTheDayFailureDefault = "The format for the time of the day selection is invalid. Please try again";
    String m_cpSelectionInternalErrorDefault = "Facing some internal error, so not processing the request";
    String m_voucherInternalErrorDefault = "Facing some internal error in voucher, so not processing the request.";
    String m_voucherFailureDefault = "The VoucherID sent is invalid, so not processing the request.";
    String m_voucherUpgradeSuccessDefault = "Your voucher request has been accepted and ur validity will be extended.";
    String m_voucherUpgradeFailureDefault = "Your upgrade request has been failed to process.";
    String m_voucherFailureAlreadyActiveDefault = "User is already active, so not processing the request.";
    String m_userNotActiveDefault = "User is not active, so not upgrading the request";
    String m_helpUpgradeSelDefault = "Please check the keyword format of ur sms. Send UPG <promocode>";
    String m_selUpgradeSuccessDefault = "Successfully upgraded all the selections corresponding to the promo code.";
    String m_selUpgradeFailureDefault = "Failed to upgrade the selections.";
    String m_preGiftSuccessGifteeNewUser = "You selected CallerTunes- %SONG_NAME as a Gift and base service for %CALLER_ID. You will  be informed once giftee accepts your gift.";
    String m_preGiftSuccessGifteeActive = "You selected Caller Tunes- %SONG_NAME as a Gift for %CALLER_ID. You will  be informed once giftee accepts your gift.";
    String m_preGiftFailureGiftExistsInGifteeLibrary = "You selected CallerTunes- %SONG_NAME as a Gift. This is already available with %CALLER_ID";
    String m_preGiftFailureNoPendingGifts = "No pending gifts available with the matching code sent";
    String m_preGiftGifteeSmsNewUser = "%CALLER_ID has gifted %SONG_NAME as a Gift and base service to you. To accept, send sms <PREGIFTCONF> <%PROMOID>.";
    String m_preGiftGifteeSmsActive = "%CALLER_ID has gifted %SONG_NAME as a Gift to you. To accept, send sms <PREGIFTCONF> <%PROMOID>.";
    String m_lotteryListNoEntriesDefault = "There are no lottery entries for your number";
    String m_lotteryListSuccessDefault = "Below are the details of your lottery entries. %LOTTERY_LIST";
    //RBT-13585
    String m_UpgradeNotAllowed = "Upgrade not allowed.";
    String m_TnbSongSelectionNotAllowed = "Song selection not allowed for tnb user";
    String m_baseUpgradeSameSubClass = "you are already subscribed to the same pack";
    String m_baseUpgradeSubClassKeywordMapNotFound = "Subscription class and Keyword Mapping not configured";
    String m_baseUpgradeSuccessDefault = "Successfully upgraded the base pack of the Subscriber";
    String m_baseUpgradeFailureDefault = "Failed to upgrade the base pack";
    String m_giftNoInboxDefault = "You have no gift in your inbox";
    String m_giftAcceptSuccessDefult = "Gift has been successfly setted in you selection library";
    String m_giftAcceptFailureDefult = "Facing problem while accept gift";
    String m_giftRejectSuccessDefult = "Gift has been successfuly rejected";
    String m_giftRejectFailureDefult = "Facing problem while reject gift";
    String m_giftDownloadSuccessDefult = "Gift has been successfully downloaded in your download library";
    String m_giftDownloadFailureDefult = "Facing problem while download gift";
    String m_giftInvalidCodeDefault = "Sorry, your promocode failure, please type %KEYWORD <promocode> or type onlye %KEYWORK";
    String m_packDownloadLimitReached = "Sorry, you reached to maximum download limit ";
    String m_contestInfluencerNotFoundDefault = "Sorry. You are not eligible for this contest.";
    String m_contestInfluencerAlreadyConfirmedDefault = "Thank you. Your friends will be sent the contest message on your behalf.";
    String m_contestInfluencerSuccessDefault = "Thank you. Your friends will be sent the contest message on your behalf.";
    
    //OI operator
    String m_dctManageBaseText = "%SONG_NAME% %ARTIST_NAME%";
    String m_dctManageDeactText = "%ALPHABET%. To Deactivate";
    String m_dctManageMoreText = "%ALPHABET%. For More ";
    
    String m_dctManageSessionExpired = "Sorry, The session got expired";
    String m_smsDctBaseSongText = "To deactivate base send SEGUIR and song send TROCAR.";
    
    
    //vivo
    String m_smsBaseSongDeactDeafault = "To replace your current song(s) send TROCAR or send SEGUIR to cancel.";
    String m_smsSongDeactSuccessDefault = "Your request has been received. Your song will be deactivated in the next 24 hours";
    String m_smsSongDeactFailureDefault = "Sorry request couldn't be handled.";
    String m_smsSongDeactConfirmTextDefault = "You still have %DAYS_LEFT to enjoy this song without costs. To remove it and lose these days send APAGAR.";
    
    
    String m_topCategoriesListingSMSTextDefault = "No such resource"; 
    String m_subCategoriesListingSMSTextDefault = "%L are the subcategories. SMS <cat name> to get clips."; 
    String m_manageNoSelDefault = "You have no selections."; 
    String m_manageSuccessDefault = "Your selections are for %L."; 
    String m_manageDefaultSettingSuccess = "Your Default Settings are :%SELECTIONS";
    String m_disableIntroSuccessDefault = "The intro prompt has been disabled.";
    String m_enableIntroSuccessDefault = "The intro prompt has been enabled."; 
    String m_giftAlreadyInUseDefault = "%C is already using a gift so he cannot get any other gift";
    String m_smsFailureTextDefault = "The request you have sent is invalid";
    public String invalidIPAddress = "Invalid IP Address";
	public String insufficientParameters = "Insufficient Parameters";
	public String m_newsLetterOnSuccessDefault  = "The newsletter feature has been turned on.";
	public String m_newsLetterOffSuccessDefault = "the newsletter feature has been turned off.";
	public String m_meriDhunSuccessDefault = "The song request is is under processing.";
	public String m_meriDhunFailureDefault = "Request failed. Plz check the promo code.";

	//delay deactivation
	String m_default_delay_dct_already_deactivated = "Your subscription is already deactivated.";
	   
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
	public String retailerSearchNoResultsSMS = "No results found.";
	//retailer user access failure error message
	public String retailerUserAccessFailureSMS = "Dear retailer, The number you sent is not authorised to access the service.";
	//retailer user invalid prefix
	public String retailerUserInvalidNumSMS = "Dear retailer, the user number you sent is invalid. Plz check.";
	//retailer user already active sms
	public String retailerUserAlreadyActiveSMS = "Dear retailer, the user is already active.";
	//retailer user already active sms
	public String retailerSelAloneFailureSMS = "Dear retailer, invalid code %S for subscriber %C"; 
	//retailer request already exists
	public String retailerRequestExistsSMS = "Dear retailer, Same request already exists for subscriber %C";
	//retailer user selection already exists
	public String retailerSelectionExistsSMS = "Dear retailer, song %S already exists for subscriber %C";
	public String retailerSubActPendingSMS = "";
	public String retailerSubDeactPendingSMS = "";
	public String retailerSubSuspendedSMS = "";
	
	
	//Activation success message
	public String activationSuccess = "Your request has been received. You will be activated in the next 24 hours";
	//Deactivation success message
	public String deactivationSuccess = "Your request has been received. You will be deactivated in the next 24 hours";
	//Selection success message
	public String selectionSuccess = "The song you requested will be set as your welcome tune";
	//Retailer success messages
	public String retailerSearchSuccess = ".To set one of the above send RET <SONGID> <SUBID> to 12900.";
	public String retailerSubSMS = "Dear retailer, we received your request to activate %C.";
	public String retailerOnlySubSuccessSMS = "Dear retailer, we received your request to activate %C. The song request failed.";
	public String retailerOnlySelSMS = "Dear retailer, we received your request to download song %S for subscriber %C";
	public String retailerRequestSuccessSMS = "Dear retailer, we received your request to activate %C and set the song %S";
	public String retailerRequestAcceptSMS = "Dear retailer, The request for subscriber %C has been processed";
	
	public String retailerOnlySubSMSToUser = "Dear user, we received your request to activate %C ....";
	public String retailerOnlySubSuccessSMSToUser = "Dear user, we received your request to activate %C. The song request failed....";
	public String retailerOnlySelSMSToUser = "Dear user, we received your request to download song %S for subscriber %C....";
	public String retailerRequestSuccessSMSToUser = "Dear user, we received your request to activate %C and set the song %S....";
	public String retailerRequestAcceptSMSToUser = "Dear user, your request has been processed";
	public String dynamicShuffleRemoveSuccessDefault = "Your selection has been removed successfully";
	public String removeProfileSuccess = "Your profile '%SONG_NAME' has been removed successfully.";
	public String removeProfileFailed = "Your profile '%SONG_NAME' has not been removed successfully, due to some technical defficulty.";
	public String removeProfileFailure = "You don't have a profile setting.";
	public String m_downloadsNoSelDefault = "You have no active downloads.";
	public String m_downloadsListSuccessDefault = "Your active downloads are ";
	public String m_downloadDeactInvalidPromoId = "Plz check the promo code and try again.";
	public String m_manageDeactInvalidId = "Plz check the option sent and try again.";
	public String m_manageDeactFailure = "Plz send manage keyword to get the list of options.";
	public String m_manageAlreadyDeact = "Your selection has already been deactivated.";
	public String m_manageDeactSuccess = "Your request has been successfully processed.";
	public String m_downloadDeactFailureDefault = "The download could not be deactivated. Plz try later.";
	public String m_downloadDeactSuccessDefault = "The download has been deactivated.";
	public String m_selDeactInvalidpromoIdDefault = "Plz check the promo code and try again.";
	
	// voluntary suspension default messages
	public String suspensionSuccessDefault = "Your request to suspension of service processed successfully."; 
	public String suspensionFailureDefault = "Your request to suspension of service will not be processed.";
	public String suspensionAlreadySusDefault = "You are already voluntarily suspended";
	public String resumptionSuccessDefault = "Your request to resumption of service processed successfully."; 
	public String resumptionFailureDefault = "Your request to resumption of service will not be processed.";
	public String resumptionNotVolSuspendedDefault = "You are not voluntarily suspended";
	
	//lock/unlock default messages
	public String lockSuccessDefault = "Your request to lock the service processed successfully";
	public String lockFailureDefault = "Your request to lock the service will not be processed";
	public String unlockSuccessDefault = "Your request to unlock the service processed successfully";
	public String unlockFailureDefault = "Your request to unlock the service will not be processed";
	public String alreadyLockDefault = "You are already locked, the service will not be processed";
	
	//Block messages
	public String blockSuccessDefault 		= "Your block request processed successfully."; 
	public String blockFailureDefault 		= "Your block request will not be processed.";
	public String blockLimitExceededDefault = "You have reached maximum blocked numbers limit.";
	public String unblockSuccessDefault 	= "Requested numbers unblocked successfully."; 
	public String unblockFailureDefault 	= "Your request to unblock will not be processed.";
	
	public String songPackSuccessDefault 	= "Your song pack request processed successfully.";
	public String songPackFailureDefault 	= "Your song pack request processed will not be processed.";
	public String songPackActPendingDefault = "Your Activation is pending.";
	public String songPackAlreadyActiveDefault = "You are already active on song pack.";
		
	public String specialSongPackSuccessDefault 	= "Your song pack request processed successfully.";
	public String specialSongPackFailureDefault 	= "Your song pack request processed will not be processed.";
	public String specialSongPackActPendingDefault = "Your Activation is pending.";
	public String specialSongPackAlreadyActiveDefault = "You are already active on song pack.";
	public String specialSongPackNotAllowedDefault = "You are already active on a pack and are not allowed to change the pack.";

	
	String CategoryListSuccess1Default 		= "To set any of the above super hit album send SUPERHIT ALBUM SET &lt;CODE/ALBUM NAME&gt;";

	//Enable or disable UDS for user
	public String udsOptInSuccess = "The User is enabled for Uds successfully";
	public String udsOptInFailed = "The User is disable for Uds successfully";
	public String udsDctOptInSuccess = "The user is disabled from the uds successfully";
	public String udsDctOptInFailed = "The user has not got his uds disabled"; 
	
	//Viral Start and Stop Default Messages
	public String m_viralStopSuccessDefault = "Your viral stop request has been processed successfully";
	public String m_viralStopFailureDefault = "Your viral stop request has failed";
	public String m_viralStartSuccessDefault = "Your viral start request has been processed successfully";
	public String m_viralStartFailureDefault = "Your viral start request has failed";
	public String m_viralStartAlreadyEnabled = "You are already removed from Viral blacklist";
	public String m_viralStopAlreadyEnabled = "You are already added to Viral blacklist";
	
	String m_freemiumSelUpgrade = "You will be upgraded to freemium charge classes after selection";
	String    m_optInFailureActSelSMS = "The Keyword Sent is wrong or PromoId sent does not exist";
	String m_optInConfirmationActSelSMS = "If you want to copy send Y. The Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationActBaseSelSMS = "If you want to copy send Y. The Subscription charge is %ACT_AMT Rs. And Song Selection Charge is %SEL_AMT Rs";
	String m_optInConfirmationActSMS = "If you want to activate send Y. The Subscription charge is %ACT_AMT Rs.";
	
	String m_optInUpgradeSMS ="Your upgrade request processed sucessfully";
	
	//multiple selection through sms
	String m_noDownloadsFoundDefault = "No clips present in the downloads";
	String m_noSettingsFoundDefault = "No setings found for the subscriber";
	String m_selectionSuccessDefault = "Selection successfull";
	String m_selectionFailureDefault = "Tone selection Failed";
	String m_invalidRequestDefault = "Invalid request";
	public String MULTI_SELECTION_SUCCESS = "MULTI_SELECTION_SUCCESS";
	public String MULTI_SELECTION_FAILURE = "MULTI_SELECTION_FAILURE";
	
	public String m_premiumSelectinConfirmationSuccessDefault = "Your song selection has been processed successfully";
	public String m_premiumSelectinConfirmationFailureDefault = "Your song selection has failed.";
	public String m_premiumSelectinConfirmationEntryMissing	  = "Premium song selection entry missing";
	
	public String m_doubleOptInComboSuccess = "Your combo request has been processed successfully";
	public String m_doubleOptInSelSuccess = "Your song selection has been processed successfully";
	public String m_doubleOptInSelFailed = "Your song selection failed";
	public String m_doubleOptInActSuccess = "Your base activation has been processed successfully";
	public String m_doubleOptInActFailed = "Your base activaion failed";
	public String m_doubleOptInNoEntriesFound = "No entries found";
	
	//Buy and Gift feature messages(TODO)
	
	//SMS Text Configs
	public String TOTAL_BLACKLIST_MSG = "TOTAL_BLACKLIST_MSG";
	public String CORP_CHANGE_SELECTION_ALL_FAILURE = "CORP_CHANGE_SELECTION_ALL_FAILURE";
	public String CORP_DEACT_SELECTION_ALL_FAILURE = "CORP_DEACT_SELECTION_ALL_FAILURE";
	public String SERVICE_NOT_ALLOWED	 = "SERVICE_NOT_ALLOWED";
	public String POLL_ON_SUCCESS = "POLL_ON_SUCCESS";
	public String POLL_ON_TECHNICAL_DIFFICULTIES = "POLL_ON_TECHNICAL_DIFFICULTIES";
	public String POLL_ON_REPEAT = "POLL_ON_REPEAT";
	public String MGM_SENDER_SUCCESS = "MGM_SENDER_SUCCESS";
	public String MGM_RECIPIENT = "MGM_RECIPIENT";
	public String MGM_SENDER_MIN_ACT_FAILURE = "MGM_SENDER_MIN_ACT_FAILURE";
	public String MGM_SENDER_MAX_GIFT_FAILURE = "MGM_SENDER_MAX_GIFT_FAILURE";
	public String SMS_ACCESS_FAILURE_TEXT = "SMS_ACCESS_FAILURE_TEXT";
	public String REMOVE_SEL_DYNAMIC_SUCCESS = "REMOVE_SEL_DYNAMIC_SUCCESS";
	public String REMOVE_SEL_DYNAMIC_FAILURE = "REMOVE_SEL_DYNAMIC_FAILURE";
	public String REMOVE_SEL_PROFILE_SUCCESS = "REMOVE_SEL_PROFILE_SUCCESS";
	public String REMOVE_SEL_PROFILE_FAILED = "REMOVE_SEL_PROFILE_ERROR";
	public String REMOVE_SEL_PROFILE_FAILURE = "REMOVE_SEL_PROFILE_FAILURE";
	public String CATEGORY_SEARCH_FAILURE = "CATEGORY_SEARCH_FAILURE";	
	public String REQUEST_MORE_CAT = "REQUEST_MORE_CAT";
	public String DOWNLOADS_NOT_PRESENT = "DOWNLOADS_NOT_PRESENT";
	public String DOWNLOADS_LIST_SUCCESS = "DOWNLOADS_LIST_SUCCESS";
	public String MANAGE_SUCCESS = "MANAGE_SUCCESS";
	public String REQUEST_MORE_NO_SEARCH = "REQUEST_MORE_NO_SEARCH";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String AZAAN_REQUEST_MORE_NO_SEARCH = "AZAAN_REQUEST_MORE_NO_SEARCH";
	public String PROFILE_SEL_NOT_ALLOWED_FOR_NEW_USER = "PROFILE_SEL_NOT_ALLOWED_FOR_NEW_USER";
	public String CATEGORY_SEARCH_SET_SUCCESS = "CATEGORY_SEARCH_SET_SUCCESS";	
	public String REQUEST_MORE_EXHAUSTED = "REQUEST_MORE_EXHAUSTED";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String AZAAN_REQUEST_MORE_EXHAUSTED = "AZAAN_REQUEST_MORE_EXHAUSTED";
	public String TEMPORARY_OVERRIDE_LIST_FAILURE = "TEMPORARY_OVERRIDE_LIST_FAILURE";
	public String TEMPORARY_OVERRIDE_LIST_STATIC = "TEMPORARY_OVERRIDE_LIST_STATIC";
	public String TEMPORARY_OVERRIDE_LIST_SUCCESS = "TEMPORARY_OVERRIDE_LIST_SUCCESS";
	public String TEMPORARY_MORE_OVERRIDE_LIST_SUCCESS = "TEMPORARY_MORE_OVERRIDE_LIST_SUCCESS";
	public String PROFILE_NEXT_FAILURE = "PROFILE_NEXT_FAILURE";
	public String PROFILE_NEXT_EXHAUSTED = "PROFILE_NEXT_EXHAUSTED";
	public String PROFILE_NEXT_SUCCESS = "PROFILE_NEXT_SUCCESS";
	public String PROFILE_NEXT_FOOTER = "PROFILE_NEXT_FOOTER";
	public String LISTEN_FAILURE = "LISTEN_FAILURE";
	public String LISTEN_SUCCESS = "LISTEN_SUCCESS";
	public String SONG_OF_MONTH_FAILURE = "SONG_OF_MONTH_FAILURE";
	public String COPY_CANCEL_SUCCESS = "COPY_CANCEL_SUCCESS";
	public String COPY_CANCEL_FAILURE = "COPY_CANCEL_FAILURE";
	public String VIRAL_FAILURE = "VIRAL_FAILURE";
	public String VIRAL_OPTOUT = "VIRAL_OPTOUT";
	public String VIRAL_OPTIN = "VIRAL_OPTIN";
	public String VIRAL_SUCCESS = "VIRAL_SUCCESS";
	public String DOWNLOAD_DEACT_VIRAL_SUCCESS = "DOWNLOAD_DEACT_VIRAL_SUCCESS";
	public String DOWNLOAD_DEACT_VIRAL_FAILURE = "DOWNLOAD_DEACT_VIRAL_FAILURE";
	public String SELECTED_SONG_NOT_IN_DOWNLOAD = "SELECTED_SONG_NOT_IN_DOWNLOAD";
	public String VIRAL_DEFAULT_SONG_SUCCESS = "VIRAL_DEFAULT_SONG_SUCCESS";
	public String VIRAL_DEFAULT_SONG_ACTIVATION_SUCCESS = "VIRAL_DEFAULT_SONG_ACTIVATION_SUCCESS";
	public String VIRAL_ENTRY_EXPIRED = "VIRAL_ENTRY_EXPIRED";
	public String MGM_RECIPIENT_ACK_FAILURE = "MGM_RECIPIENT_ACK_FAILURE";	
	public String MGM_RECIPIENT_ACK = "MGM_RECIPIENT_ACK";
	public String RETAILER_FAILURE_INVALID_PREFIX = "RETAILER_FAILURE_INVALID_PREFIX";
	public String RETAILER_FAILURE_ALREADY_ACTIVE = "RETAILER_FAILURE_ALREADY_ACTIVE";
	public String RETAILER_FAILURE_DAECT_PENDING = "RETAILER_FAILURE_DAECT_PENDING";
	public String RETAILER_FAILURE_SUSPENDED = "RETAILER_FAILURE_SUSPENDED";
	public String RETAILER_FAILURE_BLACK_LISTED = "RETAILER_FAILURE_BLACK_LISTED";
	public String RETAILER_FAILURE_INVALID_USER = "RETAILER_FAILURE_INVALID_USER";
	public String RETAILER_FAILURE_CLIP_DOES_NOT_EXIST = "RETAILER_FAILURE_CLIP_DOES_NOT_EXIST";
	public String RETAILER_TECHNICAL_DIFFICULTY = "RETAILER_TECHNICAL_DIFFICULTY";
	public String RETAILER_CHARGED_SUCCESSFULLY = "RETAILER_CHARGED_SUCCESSFULLY";
	public String RETAILER_LOW_BALANCE = "RETAILER_LOW_BALANCE";
	public String RETAILER_ACT_SUCCESS = "RETAILER_ACT_SUCCESS";
	public String RETAILER_ACT_N_SEL_SUCCESS = "RETAILER_ACT_N_SEL_SUCCESS";
	public String RETAILER_SEL_SUCCESS = "RETAILER_SEL_SUCCESS";
	public String RETAILER_ACT_FAILURE = "RETAILER_ACT_FAILURE";
	public String RETAILER_FAILURE = "RETAILER_FAILURE";
	public String RETAILER_FAILURE_INACTIVE_USER = "RETAILER_FAILURE_INACTIVE_USER";
	public String RETAILER_FAILURE_DEFAULT = "RETAILER_FAILURE_DEFAULT";
	public String RETAILER_INVALID = "RETAILER_INVALID";
	public String RETAILER_ACCEPT_RESPONSE_SENDER = "RETAILER_ACCEPT_RESPONSE_SENDER";
	public String RETAILER_ACCEPT_RESPONSE_SENDER_SEL = "RETAILER_ACCEPT_RESPONSE_SENDER_SEL";
	public String RETAILER_RESP_SMS_ACCEPT = "RETAILER_RESP_SMS_ACCEPT";
	public String RETAILER_RESP_SMS_ACCEPT_SEL = "RETAILER_RESP_SMS_ACCEPT_SEL";
	public String INTRO_PROMPT_DISABLE_SUCCESS = "INTRO_PROMPT_DISABLE_SUCCESS";
	public String RETAILER_ACT = "RETAILER_ACT";
	public String POLL_OFF_SUBSCRIBER_NOT_ACTIVE = "POLL_OFF_SUBSCRIBER_NOT_ACTIVE";
	public String POLL_OFF_SUCCESS = "POLL_OFF_SUCCESS";
	public String DOWNLOAD_DEACT_INVALID_PROMO_ID = "DOWNLOAD_DEACT_INVALID_PROMO_ID";
	public String DOWNLOAD_DEACT_FAILURE = "DOWNLOAD_DEACT_FAILURE";
	public String DOWNLOAD_DEACT_SUCCESS = "DOWNLOAD_DEACT_SUCCESS";
	public String DOWNLOAD_DEACT_CONFIRM_PENDING_SUCCESS = "DOWNLOAD_DEACT_CONFIRM_PENDING_SUCCESS";
	public String NEWSLETTER_ON_SUCCESS = "NEWSLETTER_ON_SUCCESS";
	public String NEWSLETTER_OFF_SUCCESS = "NEWSLETTER_OFF_SUCCESS";
	public String INTRO_PROMPT_ENABLE_SUCCESS = "INTRO_PROMPT_ENABLE_SUCCESS";
	public String COPY_CONFIRM_SUCCESS = "COPY_CONFIRM_SUCCESS";
	public String SEL_DEACT_INVALID_PROMO_ID = "SEL_DEACT_INVALID_PROMO_ID";
	public String RMV_CALLERID_FAILURE = "RMV_CALLERID_FAILURE";
	public String RMV_CALLERID_SUCCESS = "RMV_CALLERID_SUCCESS";
	public String RMV_CALLERID_DELAYED_DEACT_SUCCESS = "RMV_CALLERID_DELAYED_DEACT_SUCCESS";
	public String RENEW_INVALID_REQUEST = "RENEW_INVALID_REQUEST";
	public String RENEW_SUCCESS = "RENEW_SUCCESS";
	public String RENEW_FAILURE = "RENEW_FAILURE";
	public String FEED1_OFF_FAILURE = "FEED1_OFF_FAILURE";	
	public String FEED1_OFF_SUCCESS = "FEED1_OFF_SUCCESS";
	public String FEED1_ON_FAILURE = "FEED1_ON_FAILURE";
	public String FEED1_ON_SUCCESS = "FEED1_ON_SUCCESS";
	public String REFER_SUCCESS = "REFER_SUCCESS";
	public String REFER_FAILURE = "REFER_FAILURE";
	public String PROMOTION1_FAILURE = "PROMOTION1_FAILURE";
	public String PROMOTION1_SUCCESS = "PROMOTION1_SUCCESS";
	public String PROMOTION2_FAILURE = "PROMOTION2_FAILURE";
	public String PROMOTION2_SUCCESS = "PROMOTION2_SUCCESS";
	public String ACTIVATION_PROMO_SUCCESS = "ACTIVATION_PROMO_SUCCESS";
	public String MUSIC_PACK_ACTIVATION_SUCCESS = "MUSIC_PACK_ACTIVATION_SUCCESS";
	public String MUSIC_PACK_N_BASE_ACTIVATION_SUCCESS = "MUSIC_PACK_N_BASE_ACTIVATION_SUCCESS";
	public String COPY_FAILURE = "COPY_FAILURE";	
	public String COPY_FAILURE_LOOP = "COPY_FAILURE_LOOP";	
	public String COPY_OVERLAP_FAILURE = "COPY_OVERLAP_FAILURE";
	public String REQUEST_OVERLAP_FAILURE = "REQUEST_OVERLAP_FAILURE";
	public String COPY_FAILURE_INACTIVE = "COPY_FAILURE_INACTIVE";
	public String COPY_SUCCESS = "COPY_SUCCESS";
	public String GIFT_CODE_FAILURE = "GIFT_CODE_FAILURE";
	public String GIFT_GIFTEE_INVALID_FAILURE = "GIFT_GIFTEE_INVALID_FAILURE";
	public String GIFTEE_BLACKLISTED = "GIFTEE_BLACKLISTED";
	public String GIFT_ALREADY_EXISTS ="GIFT_ALREADY_EXISTS";
	public String GIFT_OWN_NUMBER_FAILURE = "GIFT_OWN_NUMBER_FAILURE";
	public String GIFT_NOT_ALLOWED = "GIFT_NOT_ALLOWED";
	public String GIFT_CLIP_EXPIRED = "GIFT_CLIP_EXPIRED";
	public String GIFT_CLIP_NOT_EXISTS = "GIFT_CLIP_NOT_EXISTS";
	public String MAX_DOWNLOAD_LIMIT_EXCEEDED = "MAX_DOWNLOAD_LIMIT_EXCEEDED";
	public String MAX_TONE_GIFT_LIMIT_EXCEEDED = "MAX_TONE_GIFT_LIMIT_EXCEEDED";
	public String SEL_KEYWORD1_SUCCESS = "SEL_KEYWORD1_SUCCESS";
	public String OVERRIDE_SHUFFLE_SELECTION = "OVERRIDE_SHUFFLE_SELECTION";
	public String SEL_KEYWORD2_FAILURE = "SEL_KEYWORD2_FAILURE";
	public String ACTIVATION_SUCCESS = "ACTIVATION_SUCCESS";
	public String CONSENT_ACTIVATION_SUCCESS = "CONSENT_ACTIVATION_SUCCESS";
	public String ACTIVATION_PROMO_CONFIRM_SUCCESS = "ACTIVATION_PROMO_CONFIRM_SUCCESS";
	public String SELECTION_PROMO_CONFIRM_SUCCESS = "SELECTION_PROMO_CONFIRM_SUCCESS";
	public String COMBO_PROMO_CONFIRM_SUCCESS = "COMBO_PROMO_CONFIRM_SUCCESS";
	public String PROMO_ID_SUCCESS = "PROMO_ID_SUCCESS";
	public String PROMO_ID_COMBO_SUCCESS = "PROMO_ID_COMBO_SUCCESS";
	public String NEW_USR_PROMO_ID_SUCCESS = "NEW_USR_PROMO_ID_SUCCESS";
	public String PROMO_ID_FAILURE = "PROMO_ID_FAILURE";
	public String TOD_SETTING_SUCCESS = "TOD_SETTING_SUCCESS";
	public String ACTIVATED_PROMO_SUCCESS = "ACTIVATED_PROMO_SUCCESS";
	public String CONSENT_ACTIVATED_PROMO_SUCCESS = "CONSENT_ACTIVATED_PROMO_SUCCESS";
	public String CONSENT_PROMO_ID_SUCCESS = "CONSENT_PROMO_ID_SUCCESS";
	public String INVALID_USER_REQUEST = "INVALID_USER_REQUEST";
	public String INVALID_PREFIX = "INVALID_PREFIX";
	public String COPY_CONFIRM_FAILURE = "COPY_CONFIRM_FAILURE";
	public String HELP_SMS_TEXT = "HELP";
	public String CLIP_EXPIRED_SMS_TEXT = "CLIP_EXPIRED";
	public String CLIP_DOES_NOT_EXIST_SMS_TEXT = "CLIP_DOES_NOT_EXIST";
	public String MANAGE_SELECTION_REMOVE_INVALID = "MANAGE_SELECTION_REMOVE_INVALID";
	public String MANAGE_SELECTION_REMOVE_FAILURE = "MANAGE_SELECTION_REMOVE_FAILURE";
	public String MANAGE_SELECTION_REMOVE_ALREADY_DEACTIVE = "MANAGE_SELECTION_REMOVE_ALREADY_DEACTIVE";
	public String MANAGE_SELECTION_REMOVE_SUCCESS = "MANAGE_SELECTION_REMOVE_SUCCESS";
	public String MANAGE_INACTIVE_USER = "MANAGE_INACTIVE_USER";
	public String MANAGE_NO_SELECTION = "MANAGE_NO_SELECTION";
	public String DEACTIVATION_NOT_ALLOWED_FOR_GRACE_SMS = "DEACTIVATION_NOT_ALLOWED_FOR_GRACE_SMS";
	public String DEACTIVATION_NOT_ALLOWED_FOR_DELAYED_DEACT_SMS = "DEACTIVATION_NOT_ALLOWED_FOR_DELAYED_DEACT_SMS";
	public String RETAILER_SUCCESS = "RETAILER_SUCCESS";
	public String RETAILER_SONG_SUCCESS = "RETAILER_SONG_SUCCESS";
	public String RETAILER_RESP_SMS_ACT = "RETAILER_RESP_SMS_ACT";
	public String RETAILER_RESP_SMS_SEL = "RETAILER_RESP_SMS_SEL";
	public String DEACTIVATION_PENDING_TEXT = "DEACTIVATION_PENDING";
	public String SELECTION_SUSPENDED_TEXT = "SELECTION_SUSPENDED";
	public String OFFER_NOT_FOUND_TEXT = "OFFER_NOT_FOUND";
	public String MERI_DHUN_SUCCESS_TEXT = "MERI_DHUN_SUCCESS";
	public String MERI_DHUN_FAILURE_TEXT = "MERI_DHUN_FAILURE";
	public String SELECTION_ALREADY_EXISTS_TEXT = "SELECTION_ALREADY_EXISTS_TEXT";
	public String DEFAULT_SELECTION_ALREADY_EXISTS_TEXT = "DEFAULT_SELECTION_ALREADY_EXISTS_TEXT";
	public String DEFAULT_SELECTION_NOT_ALLOWED_TEXT = "DEFAULT_SELECTION_NOT_ALLOWED_TEXT";
	public String OPTIN_DEFAULT_SELECTION_NOT_ALLOWED_TEXT = "OPTIN_DEFAULT_SELECTION_NOT_ALLOWED_TEXT";
	
	public String SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT = "SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT";
	public String PROFILE_LIST_COUNT =  "PROFILE_LIST_COUNT";
	public String PROFILE_SET_ALLOWED_BY_INDEX = "PROFILE_SET_ALLOWED_BY_INDEX";
	public String PROFILE_END_SEPARATOR = "PROFILE_END_SEPARATOR";
	public String CONFIRM_CHARGE_CONFIRM = "CONFIRM_CHARGE_CONFIRM";
	public String CONFIRM_CHARGE_FAILURE = "CONFIRM_CHARGE_FAILURE";
	public String CONFIRM_CHARGE_SUCCESS = "CONFIRM_CHARGE_SUCCESS";
	public String TNB_ACTIVATION_EXISTINGUSER="TNB_EXISTING_USER";
	public String TNB_ACTIVATION_OFFERUSED="TNB_OFFER_USED";
	public String TNB_ACTIVATION_SUCCESS="TNB_ACTIVATION_SUCCESS";
	public String TNB_ACTIVATION_FAILURE="TNB_ACTIVATION_FAILURE";
	public String EMOTION_NO_ACTIVE_EMOTION = "EMOTION_NO_ACTIVE_EMOTION";
	public String EMOTION_DCT_SUCCESS = "EMOTION_DCT_SUCCESS";
	public String EMOTION_DCT_FAILURE = "EMOTION_DCT_FAILURE";
	public String EMOTION_NOT_ALLOWED_FOR_SPECIFIC_CALLER = "EMOTION_NOT_ALLOWED_FOR_SPECIFIC_CALLER";
	public String EMOTION_INVALID_CONTENT = "EMOTION_INVALID_CONTENT";
	public String DOWNLOAD_OPTIN_RENEWAL_INACTIVE_USER = "DOWNLOAD_OPTIN_RENEWAL_INACTIVE_USER";
	public String DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID = "DOWNLOAD_OPTIN_RENEWAL_INVALID_CONTENT_ID";
	public String DOWNLOAD_OPTIN_RENEWAL_FAILURE = "DOWNLOAD_OPTIN_RENEWAL_FAILURE";
	public String DOWNLOAD_OPTIN_RENEWAL_SUCCESS = "DOWNLOAD_OPTIN_RENEWAL_SUCCESS";
	public String DOWNLOAD_OPTIN_RENEWAL_ERROR = "DOWNLOAD_OPTIN_RENEWAL_ERROR";
	public String RDC_SEL_FAILURE = "RDC_SEL_FAILURE";
	public String RDC_SEL_SUCCESS = "RDC_SEL_SUCCESS";
	public String CP_SEL_INTERNAL_ERROR = "CP_SEL_INTERNAL_ERROR";
	public String VOUCHER_INTERNAL_ERROR = "VOUCHER_INTERNAL_ERROR";
	public String VOUCHER_FAILURE = "VOUCHER_FAILURE";
	public String VOUCHER_UPGRADE_SUCCESS = "VOUCHER_UPGRADE_SUCCESS";
	public String VOUCHER_UPGRADE_FAILURE = "VOUCHER_UPGRADE_FAILURE";
	public String VOUCHER_FAILURE_ALREADY_ACTIVE = "VOUCHER_FAILURE_ALREADY_ACTIVE";
	public String HELP_UPGRADE_SEL = "HELP_UPGRADE_SEL";
	public String SEL_UPGRADE_SUCCESS = "SEL_UPGRADE_SUCCESS";
	public String SEL_UPGRADE_FAILURE = "SEL_UPGRADE_FAILURE";
	public String GIFT_ACCEPT_SUCCESS = "GIFT_ACCEPT_SUCCESS";
	public String GIFT_ACCEPT_FAILURE = "GIFT_ACCEPT_FAILURE";
	public String GIFT_REJECT_SUCCESS = "GIFT_REJECT_SUCCESS";
	public String GIFT_REJECT_FAILURE = "GIFT_REJECT_FAILURE";
	public String GIFT_DOWNLOAD_SUCCESS = "GIFT_DOWNLOAD_SUCCESS";
	public String GIFT_DOWNLOAD_FAILURE = "GIFT_DOWNLOAD_FAILURE";
	public String NO_GIFT_INBOX = "NO_GIFT_INBOX";
	public String GIFT_INVALID_CODE_FAILURE = "GIFT_INVALID_CODE_FAILURE";
	public String SEND_OLD_SELECTION_IN_LIBRARY = "SEND_OLD_SELECTION_IN_LIBRARY";
	//VIRAL START AND STOP
	public String VIRAL_START_SUCCESS = "VIRAL_START_SUCCESS";
	public String VIRAL_START_FAILURE = "VIRAL_START_FAILURE";
	public String VIRAL_STOP_SUCCESS = "VIRAL_STOP_SUCCESS";
	public String VIRAL_STOP_FAILURE = "VIRAL_STOP_FAILURE";
	public String VIRAL_START_ALREADY_ENABLED = "VIRAL_START_ALREADY_ENABLED";
	public String VIRAL_STOP_ALREADY_ENABLED = "VIRAL_STOP_ALREADY_ENABLED";
	
	public String OUI_SMS_KEYWORD_FAILURE = "OUI_SMS_KEYWORD_FAILURE";
	public String OUI_SMS_KEYWORD_PROFILE_SEL_NOT_EXIST = "OUI_SMS_KEYWORD_PROFILE_SEL_NOT_EXIST";
	
	public String CANCEL_DEACTIVATION_SMS_USER_NOT_ACTIVE = "CANCEL_DEACTIVATION_SMS_USER_NOT_ACTIVE";
	public String CANCEL_DEACTIVATION_SMS_FAILURE = "CANCEL_DEACTIVATION_SMS_FAILURE";
	public String CANCEL_DEACTIVATION_SMS_SUCCESS = "CANCEL_DEACTIVATION_SMS_SUCCESS";
	public String CANCEL_DEACTIVATION_SMS_TIME_EXCEED = "CANCEL_DEACTIVATION_SMS_TIME_EXCEED";
	public String CANCEL_DEACTIVATION_SMS_ERROR = "CANCEL_DEACTIVATION_SMS_ERROR";
	
	public String PREMIUM_SELECTION_CONFIRMATION_SUCCESS = "PREMIUM_SELECTION_CONFIRMATION_SUCCESS";
	public String PREMIUM_SELECTION_CONFIRMATION_ENTRY_MISSING = "PREMIUM_SELECTION_CONFIRMATION_ENTRY_MISSING"; 
	public String PREMIUM_SELECTION_CONFIRMATION_FAILURE = "PREMIUM_SELECTION_CONFIRMATION_FAILURE";
	
	//SMS DEACTIVATION
	public String DCT_MANAGE_BASE_TEXT = "DCT_MANAGE_BASE_TEXT";
	public String DCT_MANAGE_MSSG_HEADER = "DCT_MANAGE_MSSG_HEADER";
	public String DCT_MANAGE_SESSION_EXPIRED = "DCT_MANAGE_SESSION_EXPIRED";
	public String DCT_MANAGE_REQUEST_MAX_CLIPS_IN_LIST = "DCT_MANAGE_REQUEST_MAX_CLIPS_IN_LIST";
	public String DCT_MANAGE_DEACT_TEXT = "DCT_MANAGE_DEACT_TEXT";
	public String DCT_MANAGE_MORE_TEXT = "DCT_MANAGE_MORE_TEXT";
	public String DCT_MANAGE_SONG_MAX_CHAR_ALLOWED = "DCT_MANAGE_SONG_MAX_CHAR_ALLOWED";
	public String DCT_MANAGE_ARTIST_MAX_CHAR_ALLOWED = "DCT_MANAGE_ARTIST_MAX_CHAR_ALLOWED";
	
	public String DCT_MANAGE_MSSG_FOOTER = "DCT_MANAGE_MSSG_FOOTER";
//	public String DCT_SONG_MSSG_HEADER = "DCT_SONG_MSSG_HEADER";
//	public String DCT_SONG_BASE_TEXT = "DCT_SONG_BASE_TEXT";
	public String DCT_SONG_OPTION_PREFIX = "DCT_SONG_OPTION_PREFIX";
	
	//VIEW SUBSCRIPTION STATISTICS
	public String m_viewSubscriptionStatisticsDefault = "Subscription Status:%SUBSCRIPTION_STATUS%. Last Billing Date: %LAST_BILLING_DATE%." +
		                           "Next Billing Date: %NEXT_BILLING_DATE%. Number of Songs in Gallery: %NO_OF_SONGS_IN_GALLARY%. Thank You.";
	
	//FOR RANDOMIZATION
	public String RANDOMIZATION_ENABLING_SUCCESS    = "RANDOMIZATION_ENABLING_SUCCESS";
	public String RANDOMIZATION_ENABLING_FAILURE    = "RANDOMIZATION_ENABLING_FAILURE";
	public String CHURN_OFFER_SUCCESS               = "CHURN_OFFER_SUCCESS";
	public String CHURN_OFFER_FAILURE               = "CHURN_OFFER_FAILURE";
	public String RANDOMIZATION_ALREADY_ENABLED     = "RANDOMIZATION_ALREADY_ENABLED";
	public String SONG_CODE_REQUEST_FORMAT_FAILURE  = "SONG_CODE_REQUEST_FORMAT_FAILURE";
	public String SONG_CODE_REQUEST_SUCCESS             = "SONG_CODE_REQUEST_SUCCESS";
	public String SONG_CODE_REQUEST_FAILURE             = "SONG_CODE_REQUEST_FAILURE";
	public String m_randomizationEnablingSuccess    = "Your Randomization Enabling request has been processed Successfully.";
	public String m_randomizationEnablingFailure    = "Your Randomization Enabling request has failed.";
	public String m_smsChurnOfferSuccess           = "Your Sms Churn Offer Request has been processed.";
	public String m_smsChurnOfferFailure           = "Your Sms Churn Offer Request has  failed.";
	public String m_randomizationAlreadyEnabled    = "You are already active on Randomization Feature.";
	public String RANDOMIZATION_ALREADY_DISABLED   = "RANDOMIZATION_ALREADY_DISABLED";
	public String RANDOMIZATION_DISABLING_FAILURE  = "RANDOMIZATION_DISABLED_FAILURE";
	public String RANDOMIZATION_DISABLING_SUCCESS  = "RANDOMIZATION_DISABLING_SUCCESS";
	public String m_randomizationDisablingSuccess    = "Your Randomization Disabling request has been processed Successfully.";
	public String m_randomizationDisablingFailure    = "Your Randomization Disabling request has failed.";
	public String m_randomizationAlreadyDisabled    = "You are already disabled from Randomization Feature.";
	public String m_songCodeRequestFormatFailure    = "Your SongCode Request Format is not Correct.";
	public String m_songCodeRequestDefaultText     = "Welcome Tune is found! Song Code-%PROMO_ID%; Song Name: %SONG_NAME%; Artist-%ARTIST%. To dwonload Welcome Tune, type WT<Space>Code and send to 4000. Thank You.";
	public String m_songCodeRequestResultFailure    = "Your SongCode Request did not find any matching result";

	public String VIRAL_OPTOUT_FAILURE = "VIRAL_OPTOUT_FAILURE";
	public String VIRAL_OPTOUT_SUCCESS = "VIRAL_OPTOUT_SUCCESS";
	String m_viralOptOutFailureTextDefault = "No viral entries are present for your number or optout request processing has been failed.";
	String m_viralOptOutSuccessTextDefault = "Your optout request has been processed successfully.";

	public String VIRAL_OPTIN_FAILURE = "VIRAL_OPTIN_FAILURE";
	public String VIRAL_OPTIN_SUCCESS = "VIRAL_OPTIN_SUCCESS";
	public String VIRAL_OPTIN_ACTIVE_USER_SUCCESS = "VIRAL_OPTIN_ACTIVE_USER_SUCCESS";
	String m_viralOptInFailureTextDefault = "No viral entries are present for your number or viral request processing has been failed.";
	String m_viralOptInSuccessTextDefault = "Your confirmation request has been received. You will be charged %PRICE for song selection. Please send ACCEPT keyword for confirmation";

	public String INIT_RANDOMIZE_INACTIVE_USER = "INIT_RANDOMIZE_INACTIVE_USER";
	public String INIT_RANDOMIZE_NOT_ENOUGH_DOWNLOADS = "INIT_RANDOMIZE_NOT_ENOUGH_DOWNLOADS";
	public String INIT_RANDOMIZE_SUCCESS = "INIT_RANDOMIZE_SUCCESS";
	public String INIT_RANDOMIZE_FAILURE = "INIT_RANDOMIZE_FAILURE";

	public String USER_NOT_UNSUB_DELAYED = "USER_NOT_UNSUB_DELAYED";
	public String UNSUB_DELAYED_SUCCESS  = "UNSUB_DELAYED_SUCCESS";
	public String UNSUB_DELAYED_FAILURE  = "UNSUB_DELAYED_FAILURE";
	
	public String SUPRESS_PRE_RENEWAL_SMS_SUCCESS  		 	= "SUPRESS_PRE_RENEWAL_SMS_SUCCESS";
	public String SUPRESS_PRE_RENEWAL_SMS_FAULURE  		 	= "SUPRESS_PRE_RENEWAL_SMS_FAULURE";
	public String SUPRESS_PRE_RENEWAL_SMS_ALREADY_ENABLED 	= "SUPRESS_PRE_RENEWAL_SMS_ALREADY_ENABLED";
	public String SUPRESS_PRE_RENEWAL_SMS_ERROR 			= "SUPRESS_PRE_RENEWAL_SMS_ERROR";
	
	public String DOWNLOAD_SET_INACTIVE_USER = "DOWNLOAD_SET_INACTIVE_USER";
	public String DOWNLOAD_SET_INVALID_PROMOID = "DOWNLOAD_SET_INVALID_PROMOID";
	public String DOWNLOAD_SET_NO_DOWNLOAD = "DOWNLOAD_SET_NO_DOWNLOAD";
	public String DOWNLOAD_SET_SUCCESS = "DOWNLOAD_SET_SUCCESS";
	public String DOWNLOAD_SET_CALLERID_SUCCESS = "DOWNLOAD_SET_CALLERID_SUCCESS";
	public String DOWNLOAD_SET_ALREADY_EXISTS = "DOWNLOAD_SET_ALREADY_EXISTS";
	public String DOWNLOAD_SET_FAILURE = "DOWNLOAD_SET_FAILURE";
	String m_downloadSetInactiveUserTextDefault = "Your service is not active.";
	String m_downloadSetInvalidPromoIDTextDefault = "The promocode passed is invalid.";
	String m_downloadSetNoDownloadTextDefault = "You have not downloaded this song in your library. Please download and set it.";
	String m_downloadSetSuccessTextDefault = "You have successfully set %SONG_NAME - %ARTIST as your Welcome Tune";
	String m_downloadSetAlreadyExistsTextDefault = "The song is already set as your Welcome Tune";
	String m_downloadSetFailureTextDefault = "Failed to set the song";

	//For BASE UPGRADTION
	public String BASE_UPGRADE_SUCCESS = "BASE_UPGRADE_SUCCESS";
	public String BASE_UPGRADE_FAILURE = "BASE_UPGRADE_FAILURE";
	public String BASE_UPGRADE_SAME_SUB_CLASS = "BASE_UPGRADE_SAME_SUB_CLASS";
	public String BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND = "BASE_UPGRADE_SUBCLASS_KEY_MAP_NOT_FOUND";
	public String LOCK_SUCCESS                  = "LOCK_SUCCESS";
	public String LOCK_FAILURE                  = "LOCK_FAILURE";
	public String UNLOCK_SUCCESS                = "UNLOCK_SUCCESS";
	public String UNLOCK_FAILURE                = "UNLOCK_FAILURE";
	public String SUSPENSION_SUCCESS 			= "SUSPENSION_SUCCESS";
	public String SUSPENSION_FAILURE 			= "SUSPENSION_FAILURE";
	public String SUSPENSION_ALREADY_VOL_SUS	= "SUSPENSION_ALREADY_VOL_SUS";
	public String RESUMPTION_SUCCESS 			= "RESUMPTION_SUCCESS";
	public String RESUMPTION_FAILURE 			= "RESUMPTION_FAILURE";
	public String RESUMPTION_NOT_VOL_SUS		= "RESUMPTION_NOT_VOL_SUS";
	public String BLOCK_SUCCESS 	 			= "BLOCK_SUCCESS";
	public String BLOCK_FAILURE 	 			= "BLOCK_FAILURE";
	public String BLOCK_MAX_LIMIT 	 			= "BLOCK_MAX_LIMIT";
	public String BLOCK_ALREADY_MEMBER 			= "BLOCK_ALREADY_MEMBER";
	public String UNBLOCK_SUCCESS	 			= "UNBLOCK_SUCCESS";
	public String UNBLOCK_FAILURE	 			= "UNBLOCK_FAILURE";
	public String SONGPACK_SUCCESS	 			= "SONGPACK_SUCCESS";
	public String SONGPACK_FAILURE	 			= "SONGPACK_FAILURE";
	public String SONGPACK_ACT_PENDING			= "SONGPACK_ACT_PENDING";
	public String SONGPACK_ALREADY_ACTIVE			= "SONGPACK_ALREADY_ACTIVE";
	public String SPECIAL_SONGPACK_SUCCESS	 			= "SPECIAL_SONGPACK_SUCCESS";
	public String SPECIAL_SONGPACK_FAILURE	 			= "SPECIAL_SONGPACK_FAILURE";
	public String SPECIAL_SONGPACK_ACT_PENDING			= "SPECIAL_SONGPACK_ACT_PENDING";
	public String SPECIAL_SONGPACK_ALREADY_ACTIVE			= "SPECIAL_SONGPACK_ALREADY_ACTIVE";
	public String UNLIMITED_DOWNLOAD_SUCCESS		= "UNLIMITED_DOWNLOAD_SUCCESS";
	public String UNLIMITED_DOWNLOAD_FAILURE	 	= "UNLIMITED_DOWNLOAD_FAILURE";
	public String UNLIMITED_DOWNLOAD_ACT_PENDING	= "UNLIMITED_DOWNLOAD_ACT_PENDING";
	public String UNLIMITED_DOWNLOAD_ALREADY_ACTIVE	= "UNLIMITED_DOWNLOAD_ALREADY_ACTIVE";
	public String SCRATCH_ACT_SUCCESS			= "SCRATCH_ACT_SUCCESS";
	public String SCRATCH_ACT_FAILURE			= "SCRATCH_ACT_FAILURE";
	public String SCRATCH_ACT_INVALID_PIN		= "SCRATCH_ACT_INVALID_PIN";
	public String PROMO_ID_SUCESS_UGS_LIST 		= "PROMO_ID_SUCESS_UGS_LIST";
	public String ACT_TRIAL_FAILURE 			= "ACT_TRIAL_FAILURE";
	public String GIFTCOPY_INVALID_CALLERID 	= "GIFTCOPY_INVALID_CALLERID";
	public String GIFTCOPY_RECEIVED_CALLERID 	= "GIFTCOPY_RECEIVED_CALLERID";
	public String GIFTCOPY_NO_REQUESTS 			= "GIFTCOPY_NO_REQUESTS";
	public String GIFTCOPY_OVERLAP 				= "GIFTCOPY_OVERLAP";
	public String GIFTCOPY_SUSPENDED_GIFTEE 	= "GIFTCOPY_SUSPENDED_GIFTEE";
	public String GIFTCOPY_SELECTION_LIMIT_EXCEEDED = "GIFTCOPY_SELECTION_LIMIT_EXCEEDED";
	public String GIFTCOPY_GIFTEE_DEACT_PENDING = "GIFTCOPY_GIFTEE_DEACT_PENDING";
	public String GIFTCOPY_CAN_NOT_GIFT 			= "GIFTCOPY_CAN_NOT_GIFT";
	public String TRIAL_REPEAT_FAILURE = "TRIAL_REPEAT_FAILURE";
	public String TNB_SUCCESS = "TNB_SUCCESS";
	public String TNB_FAILURE = "TNB_FAILURE";

	public String MANAGE_DEFAULT_SETTING_SUCCESS = "MANAGE_DEFAULT_SETTING_SUCCESS";
	public String NO_DEFAULT_SETTING_SELECTION = "NO_DEFAULT_SETTING_SELECTION";
	
	// Idea voluntary Suspension
	public String SUSPENSION_KEYWORD = "SUSPENSION_KEYWORD";
	public String RESUMPTION_KEYWORD = "RESUMPTION_KEYWORD";
	// Vodafone Block feature
	public String BLOCK_KEYWORD 	 = "BLOCK_KEYWORD";
	public String UNBLOCK_KEYWORD 	 = "UNBLOCK_KEYWORD";
	// Idea songPack feature
	public String PACK_KEYWORD 		 = "PACK_KEYWORD";
	// BSNL song pack feature
	public String SONG_PACK_KEYWORD 		 = "SONG_PACK_KEYWORD";
	public String IS_SPECIAL_SONG_PACK  =  "IS_SPECIAL_SONG_PACK";
	public String SPECIAL_SONGPACK_NOT_ALLOWED			= "SPECIAL_SONGPACK_NOT_ALLOWED";
	//Keywords and parameters
	public String CORP_CHANGE_SELECTION_ALL_BLOCK = "CORP_CHANGE_SELECTION_ALL_BLOCK";
	public String ALLOW_SUSPENDED_USER_ACCESS = "ALLOW_SUSPENDED_USER_ACCESS";
	public String MGM_PARAMS = "MGM_PARAMS";
	public String MGM_SENDER_FAILURE = "MGM_SENDER_FAILURE";
	public String PROFILE_SUB_CLASS = "PROFILE_SUB_CLASS";
	public String YEARLY_SUBSCRPTION_CLASS = "YEARLY_SUBSCRPTION_CLASS";
	public String SMS_TEXT_FOR_ALL = "SMS_TEXT_FOR_ALL";
	public String OPERATOR_NAME = "OPERATOR_NAME";
	public String BRAND_NAME = "BRAND_NAME";
	public String OPERATOR_SHORTCODE = "OPERATOR_SHORTCODE";
	public String PROMO_ID_ACTIVATED_BY = "PROMO_ID_ACTIVATED_BY";
	public String IS_ACT_OPTIONAL_REQUEST_RBT = "IS_ACT_OPTIONAL_REQUEST_RBT";
	public String REQUEST_MAX_CLIP_SEARCHED = "REQUEST_MAX_CLIP_SEARCHED";
	public String REQUEST_MAX_CAT_SMS = "REQUEST_MAX_CAT_SMS";	
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String REQUEST_MAX_AZAAN_SMS = "REQUEST_MAX_AZAAN_SMS";	 
	public String REQUEST_MAX_AZAAN_SEARCHED = "REQUEST_MAX_AZAAN_SEARCHED";	
	public String CAT_SEARCH_GIVE_PROMO_ID = "CAT_SEARCH_GIVE_PROMO_ID";
	public String INSERT_SEARCH_NUMBER_AT_END = "INSERT_SEARCH_NUMBER_AT_END";
	public String CATEGORY_SEARCH = "CATEGORY_SEARCH";
	public String LUCENE_MAX_RESULTS = "LUCENE_MAX_RESULTS";
	public String REQUEST_NO_MATCH_DISP_TOP = "REQUEST_NO_MATCH_DISP_TOP";
	public String REQUEST_LANG_FILTER = "REQUEST_LANG_FILTER";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String REQUEST_COSID_ORDER_FILTER = "REQUEST_COSID_ORDER_FILTER";
	public String REQUEST_MAX_SMS = "REQUEST_MAX_SMS";
	public String ADD_MOVIE_REQUEST = "ADD_MOVIE_REQUEST";
	public String SONG_SEARCH_GIVE_PROMO_ID = "CAT_SEARCH_GIVE_PROMO_ID";
	public String INSERT_SEARCH_NUMBER_AT_BEGINNING = "INSERT_SEARCH_NUMBER_AT_BEGINNING";
	public String REQUEST_MORE = "REQUEST_MORE";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String AZAAN_REQUEST_MORE = "AZAAN_REQUEST_MORE";
	public String CALLER_TUNE_FREE_KEYWORD="WTFREE";
	public String SMS_MANAGE_SEL_DISPLAY = "SMS_MANAGE_SEL_DISPLAY";
	public String SMS_MANAGE_ACT_DATE = "SMS_MANAGE_ACT_DATE";
	public String SMS_MANAGE_PROMO_ID_DISPLAY = "SMS_MANAGE_PROMO_ID_DISPLAY";
	public String SMS_MANAGE_DOWNLOADS_DISPLAY = "SMS_MANAGE_DOWNLOADS_DISPLAY";
	public String PROFILE_DAYS_ALIAS = "PROFILE_DAYS_ALIAS";
	public String WAIT_TIME_DOUBLE_CONFIRMATION = "WAIT_TIME_DOUBLE_CONFIRMATION";
	public String COPY_CONF_MODE_SMS = "COPY_CONF_MODE_SMS";
	public String COPY_CONF_MODE_USSD = "COPY_CONF_MODE_USSD";
	public String IS_VIRAL_CLIP_ALLOWED = "IS_VIRAL_CLIP_ALLOWED";
	public String IS_ACT_ALLOWED = "IS_ACT_ALLOWED";
	public String PHONE_NUMBER_LENGTH_MIN = "PHONE_NUMBER_LENGTH_MIN";
	public String PHONE_NUMBER_LENGTH_MAX = "PHONE_NUMBER_LENGTH_MAX";
	public String MINIMUM_CALLER_ID_LENGTH = "MINIMUM_CALLER_ID_LENGTH";
	public String CHECK_PROMO_MASTER = "CHECK_PROMO_MASTER";
	public String ALLOW_REMOVAL_OF_NULL_CALLERID_SEL = "ALLOW_REMOVAL_OF_NULL_CALLERID_SEL";
	public String ACTIVATE_N_SELECTION = "ACTIVATE_N_SELECTION";
	public String MOBILE_REGISTRATION = "MOBILE_REGISTRATION";
	public String RETAILER_ACTIVATE_N_SELECTION = "ACTIVATE_N_SELECTION";
	public String ONLY_SEARCH_SHORT_CODE = "ONLY_SEARCH_SHORT_CODE";
	public String REQUEST_MORE_KEYWORD = "REQUEST_MORE_KEYWORD";
	public String CRICKET_KEYWORD = "CRICKET_KEYWORD";
	public String CRICKET_PASS = "CRICKET_PASS";
	public String ALLOW_FEED_UPGRADE = "ALLOW_FEED_UPGRADE";
	public String ALLOW_LOOPING = "ALLOW_LOOPING";
	public String ADD_SEL_TO_LOOP = "ADD_SEL_TO_LOOP";
	public String DEACT_DOWNLOAD_KEYWORD = "DEACT_DOWNLOAD_KEYWORD";
	public String SDR_WORKING_DIR = "SDR_WORKING_DIR";
	public String RETAILER_FEATURE = "RETAILER_FEATURE";
	public String RETAILER_REQ_RESPONSE = "RETAILER_REQ_RESPONSE";
	public String MGM_FEATURE = "MGM_FEATURE";
	public String SCRATCH_CARD_FEATURE = "SCRATCH_CARD_FEATURE";
	public String GIFTCOPY_FEATURE = "GIFTCOPY_FEATURE";
	public String REATILER_ACT_N_SEL_FEATURE = "REATILER_ACT_N_SEL_FEATURE";
	public String REFERRAL_KEYWORD = "REFERRAL_KEYWORD";
	public String CONFIRM_SUBSCRIPTION_N_COPY_FEATURE = "CONFIRM_SUBSCRIPTION_N_COPY_FEATURE";
	public String MIN_VALUE_PROMO_ID = "MIN_VALUE_PROMO_ID";
	public String PROMOTION1_INVALID_FORMAT = "PROMOTION1_INVALID_FORMAT";
	public String PROMOTION1_VALID_FORMAT = "PROMOTION1_VALID_FORMAT";
	public String DEFAULT_SUBSCRIPTION_TYPE = "DEFAULT_SUBSCRIPTION_TYPE";
	public String DEFAULT_CHARGING_CYCLE = "DEFAULT_CHARGING_CYCLE";
	public String TRIAL_WITH_ACT = "TRIAL_WITH_ACT";
	public String GIVE_UGS_SONG_LIST = "GIVE_UGS_SONG_LIST";
	public String PROMOTION_SDR_DIR = "PROMOTION_SDR_DIR";
	public String CONFIRM_DEACTIVATION = "CONFIRM_DEACTIVATION";
	public String DEACTIVATION_CONFIRM_CLEAR_DAYS = "DEACTIVATION_CONFIRM_CLEAR_DAYS";
	public String DEACTIVATION_CONFIRM = "DEACTIVATION_CONFIRM";
	public String WEBSERVICE_DEACTIVATION = "WEBSERVICE_DEACTIVATION";
	public String DEACTIVATION_CONFIRM_CHURN_OFFER = "DEACTIVATION_CONFIRM_CHURN_OFFER";
	public String DOWNLOAD_OVERLIMIT = "DOWNLOAD_OVERLIMIT";
	public String NEW_STARTER_PACK = "NEW_STARTER_PACK";
	public String CATEGORY_LIST_SUCCESS = "CATEGORY_LIST_SUCCESS";
	public String STARTER_PACK_CATEGORIES = "STARTER_PACK_CATEGORIES";
	public String SHUFFLE_PROMO_SLEEP_TIME = "SHUFFLE_PROMO_SLEEP_TIME";
	public String PROCESS_SMSUI_COPY_AS_OPTIN = "PROCESS_SMSUI_COPY_AS_OPTIN";
	public String ESIA_SHUFFLE_PROMO_DUMMY_CATEGORYID ="ESIA_SHUFFLE_PROMO_DUMMY_CATEGORYID";
	public String SMS_BUY_AND_GIFT_WAITING_TIME = "SMS_BUY_AND_GIFT_WAITING_TIME";
	public String SMS_IS_BUY_AND_GIFT_ALLOWED = "SMS_IS_BUY_AND_GIFT_ALLOWED";
	public String SMS_BUY_AND_GIFT_MODE = "SMS_BUY_AND_GIFT_MODE";
	public String DONT_ACCEPT_MULTIPLE_DELAYED_DEACT_REQUEST = "DONT_ACCEPT_MULTIPLE_DELAYED_DEACT_REQUEST";
	
	//Promo Id length parameter
	public String PROMO_ID_MIN_LENGTH = "PROMO_ID_MIN_LENGTH";
	public String PROMO_ID_MAX_LENGTH = "PROMO_ID_MAX_LENGTH";

	
	//parameters names in RBT_PARAMETERS table
	public String ACTIVATED_BY = "ACTIVATED_BY";
	public String SHOW_UPGRADE = "SHOW_UPGRADE";
	public String CURRENCY_STRING = "CURRENCY_STRING";
	public String USE_SUBSCRIPTION_MANAGER = "USE_SUBSCRIPTION_MANAGER";
	public String BLACKOUT_SMS = "BLACKOUT_SMS";
	public String CORP_SPL_FEATURE = "CORP_SPL_FEATURE";
	
	public String LITEUSER_PREMIUM_BLOCKED = "LITEUSER_PREMIUM_BLOCKED";
	public String LITEUSER_PREMIUM_NOT_PROCESSED = "LITEUSER_PREMIUM_NOT_PROCESSED";
	public String COS_MISMATCH_CONTENT_BLOCKED = "CONTENT_PURCHASE_BLOCKED_";
	
	public String CHECK_MAPPING_MANDATORY_CHARGE_CLASS = "CHECK_MAPPING_MANDATORY_CHARGE_CLASS";
	public String CHECK_CATEGORY_CLIP_MAPPING_MANDATORY_MODES = "CHECK_CATEGORY_CLIP_MAPPING_MANDATORY_MODES";
	public String DEFAULT_PROMOTION_CATEGORY_ID = "DEFAULT_PROMOTION_CATEGORY_ID";
	public String SONG_SEARCH_GIVE_ARTIST_NAME = "SONG_SEARCH_GIVE_ARTIST_NAME";
	
	public String CATEGORY_SEARCH_SUCCESS = "CATEGORY_SEARCH_SUCCESS";
	public String REQUEST_MORE_CAT_SUCCESS = "REQUEST_MORE_CAT_SUCCESS";
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public String AZAAN_SEARCH_SUCCESS = "AZAAN_SEARCH_SUCCESS";
	public String AZAAN_ACTIVATION_SUCCESS = "AZAAN_ACTIVATION_SUCCESS";
	
	public String SMS_ALIAS_STRINGS = "SMS_ALIAS_STRINGS";
	public String ALREADY_LOCKED = "ALREADY_LOCKED";
	
	public String SENDER_ID = "SENDER_ID";

	public String param_FROMTIME = "FROMTIME";
	public String param_TOTIME = "TOTIME";
	public String COPYCONFPENDING="COPYCONFPENDING"; 
	public String param_INTERVAL = "INTERVAL";

	//USSD Copy Key
	public String USSD_COPY_KEY="USSDCOPY_KEY"; 
	public String USSD_COPY_CONFIRM_SUCCESS="COPY_CONFIRM_SUCCESS";
	public String USSD_COPY_CONFIRM_FAILURE="COPY_CONFIRM_FAILURE";
	//SMS Double Confirmation
	public String SMSCONFPENDING="SMSCONFPENDING"; 
    public static final String SEARCH_FOR_ALIAS_FAIL = "SEARCH_FOR_ALIAS_FAIL";
    public static final String ALIAS_SEARCH = "ALIAS_SEARCH";
    public static final String SMS_DOUBLE_CONFIRMATION = "SMS_DOUBLE_CONFIRMATION";
    public static final String SMS_DOUBLE_CONFIRMATION_CONFIRM_KEYWORD = "SMS_DOUBLE_CONFIRMATION_CONFIRM_KEYWORD";
    
    //RBT-15149
    public static final String PROCESS_DOUBLE_CONFIRMATION = "PROCESS_DOUBLE_CONFIRMATION";

    //upgrade subscription SMS
    public String churnOfferSuccess    = "The subscription has been upgraded successfully";
    public String churnOfferFailure    = "The subscription has been failed to upgrade";
    public String churnOfferNotAllowed = "The churn offer is not allowed for the subscriber";
    
    public String param_VODACT_MSISDN = "MSISDN";
    public String param_VODACT_REQUEST = "REQUEST";
    public String param_VODACT_MODE = "MODE";
    public String param_VODACT_SRVCLASS = "SRVCLASS";
    public String param_VODACT_PROMPT = "PROMPT";
    public String param_VODACT_CATEGORYID = "CATEGORYID";
    public String param_VODACT_TRANSID = "TRANSID";
    public String param_VODACT_CHARGECLASS = "CHARGECLASS";
    public String param_crmUrlMode = "MODE";
    public String param_VODACT_SRVID = "SRVID";
    public String param_VODACT_UPGRADE = "UPGRADE";
    public String param_VODACT_SERVICE_CODE = "SERVICE_CODE";
    public String param_VODACT_CLASS = "CLASS";
    
    public static final String response_VODACT_SUCCESS_WITH_ID							= "1500:transID:Success";
    public static final String response_VODACT_AUTHENTICATION_FAILED					= "1501:transID:Authentication Failed";
    public static final String response_VODACT_PARAMETER_MISSING						= "1502:transID:Mandatory Parameter Missing";
    public static final String response_VODACT_SERVICE_NOT_CONFIGURED					= "1503:transID:Service Not Configured";
    public static final String response_VODACT_INVALID_MSISDN							= "1504:transID:Invalid MSISDN";
    public static final String response_VODACT_SUBSCRIPTION_ALREADY_EXISTS				= "1505:transID:Subscription Already Exists";
    public static final String response_VODACT_CONTENT_NOT_FOUND						= "1506:transID:Content Not Found";
    public static final String response_VODACT_CONTENT_EXPIRED							= "1507:transID:Content Expired";
    public static final String response_VODACT_INTERNAL_ERROR							= "1508:transID:Internal Error"; // Any retry able error
    public static final String response_VODACT_PARAMETER_VALIDATION_ERROR				= "1509:transID:Parameter Validation Error";
    public static final String response_VODACT_NO_SUBSCRIPTIONS_FOUND					= "1510:transID:No Subscriptions Found"; // In case of deactivation
    public static final String response_VODACT_DEACTIVATION_PENDING						= "1511:transID:Previous Deactivation Pending"; // In case of activation
    public static final String response_VODACT_ALREADY_UPDATED							= "1512:transID:Response Already Updated"; // Balance check, provisioning, de-provisioning
    public static final String response_VODACT_PREVIOUS_DEACTIVATION_PENDING			= "1513:transID:Previous Deactivation Pending"; // In case of deactivation
    public static final String response_VODACT_ACTIVATION_PENDING						= "1514:transID:Activation Pending";
    public static final String response_VODACT_USER_BLACKLISTED							= "1515:transID:User Blacklisted";
    public static final String response_VODACT_USER_SUSPENDED							= "1516:transID:User Suspended";
    public static final String response_VODACT_USER_LOCKED								= "1517:transID:User Locked";
    public static final String response_VODACT_SELECTION_SUSPENDED						= "1518:transID:Selection Suspended";
    public static final String response_VODACT_SELECTION_ALREADY_EXIXTS					= "1519:transID:Selection Already Exists";
    public static final String response_VODACT_ERROR									= "1520:transID:Error"; // Non retry able
    public static final String response_VODACT_FAILED									= "1521:transID:Failed"; // Non retry able
    public static final String response_VODACT_SELECTION_NOT_ALLOWED					= "1522:transID:Selection Not Allowed";
    public static final String response_VODACT_DEACTIVATION_REQUEST_ALREADY_ACCEPTED	= "1523:transID:Deactivation Request Already Accepted";
    public static final String response_VODACT_SELECTION_OVERLIMIT						= "1524:transID:Selection Overlimit";
    public static final String response_VODACT_PERSONAL_SELECTION_OVERLIMIT				= "1525:transID:Personal Selection Overlimit";
    public static final String response_VODACT_LOOP_SELECTION_OVERLIMIT					= "1526:transID:Loop Selection Overlimit";
    public static final String response_VODACT_PREMIUM_CONTENT_BLOCKED					= "1527:transID:Premium Content Blocked";
    public static final String response_VODACT_CONTENT_BLOCKED_FOR_COSID				= "1528:transID:Premium Content Not Allowed";
    public static final String response_VODACT_TNBOBD_ALREADY_AVAILED					= "1528:transID:Tnb Already availed";
    public static final String response_VODACT_COPRORATE_DEACTIVATION_NOT_ALLOWED		= "1529:transID:Corporate selection, Deactivation Not Allowed";
    public static final String response_VODACT_UPGRADE_NOT_ALLOWED						= "1530:transID:Upgrade not allowed";
	public static final String response_VODACT_COPRORATE_SELECTION_NOT_ALLOWED			= "1531:transID:Corporate selection is Not Allowed for All Subscribers";
    final String ERR_RESPONSE_SONG_CODE="SONG_CODE=-1"; 
    public String response_VODACT_ALREADY_ACTIVE = "ALREADY_ACTIVE";
    public String response_VODACT_SUCCESS = "SUCCESS";
    public String response_VODACT_TECHNICAL_DIFFICULTY = "TECHNICAL_DIFFICULTY";
    public String response_VODACT_INVALID = "INVALID";
    public String response_VODACT_INVALID_PREFIX = "INVALID_PREFIX";
 
    public String dbparam_VODACTURL_ACTIVATION_OPTIONAL = "VODACTURL_ACTIVATION_OPTIONAL";
    public String dbparam_VODACTURL_PROCESS_SEL_FOR_ALREADY_ACTIVE = "VODACTURL_PROCESS_SEL_FOR_ALREADY_ACTIVE";
    public String dbparam_VODACRMURL_COMBO_MODE = "VODACRMURL_COMBO_MODE";
    public String dbparam_VODACRM_INTEGRATION_SM_DETAILS_URL = "VODACRM_INTEGRATION_SM_DETAILS_URL";
    
    public static final String reponse_INVALID_CHARGE_CLASS = "CHARGE_CLASS_INVALID";
    
    public static final String WEB_ACTIVATION_SUCCESS = "WEB_ACTIVATION_SUCCESS";
    String m_webActivationSuccessDefault = "You have been successfully activated";

	//SATRbt Constants
	String propertyFileName = "/resources/SATRbtArgConfig.properties";
	
	String KEY_CATEGORY = "category";
	String KEY_ERROR_INVALID_REQUEST = "error.invalid.request";
	String KEY_ERROR_INVALID_MSISDN = "error.invalid.msisdn";
	String KEY_ERROR_INVALID_CATEGORY = "error.invalid.category";
	String KEY_ERROR_INVALID_CLIP = "error.invalid.clip";
	String KEY_ERROR_NO_CATEGORY_SELECTED = "error.noCategorySelected";
	String KEY_SEL_RESP_SUCCESS = "selection.success.msg";
	String KEY_SEL_RESP_ERROR = "selection.error.msg";
	String KEY_SEL_PRICE = "selection.price";
	
	String NODE_SATML = "satml";
	String NODE_CARD = "card";
	String NODE_P = "p";
	String NODE_SELECT = "select";
	String NODE_OPTION = "option";
	String ONPICK = "onpick";
	String ANCHOR = "a";
	String TITLE = "title";
	String HREF = "href";

	public static final String param_ProductCode 	= "ProductCode";
	public static final String param_Action 		= "Action";
	public static final String param_AgentID 		= "AgentID";
	public static final String param_VendorName 	= "VendorName";

	public static final String param_service 		= "service";
	public static final String param_tid		 	= "tid";
	
	public static final String param_ouiRegCode		= "OUI_REG_CODE";
	
	//CONSENT FEATURE
	public static final String param_songid         = "songid";
	public static final String param_transID        = "transid";
	public static final String param_SONG_SRVKEY    = "songSrvKey";
	public static final String param_requestMismatched = "Request can not be processed due to mismatch";
	public static final String param_timestamp      = "timestamp";
	public static final String param_consent        = "consent";
	public static final String param_isShuffle      = "isShuffle";
	public static final String param_categoryid     = "categoryid";
	public static final String param_activation_info  = "activationinfo";
	public static final String param_agentId  = "agentId";
	public static final String PROFILE_ACTIVATION_ON_CIRCLE_COS_ID = "PROFILE_ACTIVATION_ON_CIRCLE_COS_ID";
	
	//Consent.do
	public static final String param_upgrade = "upgrd";
	public static final String param_Refid = "Refid";
	public static final String param_childrefid = "childrefid";
	public static final String param_childprice = "childprice";
	public static final String param_baseprice = "baseprice";
	
	//RBT-9213 Added for SDPDirectConsent.do
	public static final String param_channelType="channelType";
	public static final String param_productCategoryId="productCategoryId";
	public static final String param_orderTypeId="orderTypeId";
	public static final String param_sdpomtxnid="sdpomtxnid";
	public static final String param_productId="productId";
	public static final String param_vendor = "vendor";
	public static final String param_seapitype = "seapitype";
	public static final String param_mismatch="MISMATCH_REQUEST_PARAMETER";
	public static final String param_SdpSrvkey="SdpSrvkey";
	public static final String param_SdpSongSrvkey="SdpSongSrvkey";
	public static final String UPGRADE_ON_DELAY_DCT_ALREADY_DEACTIVATED = "UPGRADE_ON_DELAY_DCT_ALREADY_DEACTIVATED";
	public String ACTIVATION_CONSENT_SUCCESS = "ACTIVATION_CONSENT_SUCCESS";
	public static final String CALLER_BASED_MULTIPLE_SELECTION_KEYWORD = "CALLER_BASED_MULTIPLE_SELECTION_KEYWORD"; 
	public static final String ORDERTYPE_INVALID = "ORDERTYPE_INVALID";
	public static final String SMS_DCT_SONG_MANAGE = "SMS_DCT_SONG_MANAGE";
	public static final String SMS_DCT_SONG_CONFIRM = "SMS_DCT_SONG_CONFIRM";
	public static final String SMS_CANCELLAR_KEYWORD = "SMS_CANCELLAR_KEYWORD";
//	RBT-13106 Originator value and AGR value in SdpDirect URL
	public static final String param_AGR = "AGR";
	public static final String NO_ORIGINATOR = "NO_ORIGINATOR";
//	public static final String SMS_SONG_DEACTIVATION_KEYWORD = "SMS_SONG_DEACTIVATION_KEYWORD";
	public static final String CONFIRM_SONG_DEACTIVATION = "CONFIRM_SONG_DEACTIVATION";
	public static final String SMS_DCT_MANAGE_SONG_DEACT_FAILURE = "SONG_DEACT_FAILURE";
	public static final String SMS_DCT_MANAGE_SONG_DEACT_SUCCESS = "SMS_DCT_MANAGE_SONG_DEACT_SUCCESS";
	public static final String SMS_DCT_MANAGE_DEACT_BASE_SONG_SMS = "SMS_DCT_MANAGE_DEACT_BASE_SONG_SMS";
	public static final String SMS_DCT_MANAGE_SONG_DEACTIVATION_CONFIRM_SMS = "SMS_DCT_MANAGE_SONG_DEACTIVATION_CONFIRM_SMS";
	public static final String SONG_DEACTIVATION_CONFIRM_CLEAR_DAYS = "SONG_DEACTIVATION_CONFIRM_CLEAR_DAYS";
	public static final String DEACT_BASE_SONG_CHURN_KEYWORD = "DEACT_BASE_SONG_CHURN_KEYWORD";
	public static final String DIRECT_SONG_DEACT_KEYWORD = "DIRECT_SONG_DEACT_KEYWORD";
	public static final String MANAGE_DEFAULT_SETTINGS_KEYWORD = "MANAGE_DEFAULT_SETTINGS_KEYWORD";	
	//RBT-12195 - User block - unblock feature.
	public static final String UNBLOCK_SUB_KEYWORD = "UNBLOCK_SUB_KEYWORD";
	public static final String BLOCK_SUB_KEYWORD = "BLOCK_SUB_KEYWORD";
	public String SMS_BLOCKED_SUB_TEXT = "SMS_BLOCKED_SUB_TEXT";	
	public String SMS_UNBLOCKED_SUB_TEXT = "SMS_UNBLOCKED_SUB_TEXT";
	public String SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE = "selection_not_allowed_for_user_on_blocked_service";
	public String SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE = "SELECTION_NOT_ALLOWED_ON_BLOCKED_SERVICE";
	public String SMS_NON_ACT_BLCK_SUB_OTHER_TEXT = "NON_ACT_BLCK_SUB_OTHER_TEXT";
	public String blocked_serviceNotAllowed = "The service Som de Chamada is already blocked for your number.";
	public String unblocked_serviceNotAllowed = "This service is not available for you.";
	public String unblocked_serviceAllowed ="Your block request will be completed until 24hs.";
	public String blocked_serviceAllowed ="The service Som de Chamada is already active in your number.";
	public String other_serviceAllowed ="Send ASSINAR (subscribe) to activate the service Vivo Som de Chamada.";
	public String unblck_blck_serviceNotAllowed = "Your request will be completed until 24hs. To block, answer this SMS with the keyword DESBLOQUEAR (Unblock) or call *5050.";
	public static final String SMS_NON_ACT_BLCK_SUB_BLOCK_TEXT = "NON_ACT_BLCK_SUB_BLOCK_TEXT";
	public static final String SMS_NON_ACT_UNBLCK_SUB_BLOCK_TEXT = "NON_ACT_UNBLCK_SUB_BLOCK_TEXT";
	public static final String SMS_ACT_UNBLCK_SUB_BLOCK_TEXT = "ACT_UNBLCK_SUB_BLOCK_TEXT";	
	public static final String SMS_NON_ACT_UNBLCK_SUB_UNBLOCK_TEXT = "NON_ACT_UNBLCK_SUB_UNBLOCK_TEXT";
	public static final String SMS_ACT_UNBLCK_SUB_UNBLOCK_TEXT= "ACT_UNBLCK_SUB_UNBLOCK_TEXT";
	public static final String SMS_NON_ACT_BLCK_SUB_UNBLOCK_TEXT= "NON_ACT_BLCK_SUB_UNBLOCK_TEXT";
	
	//RBT-12419
	public static final String Resp_ClipExpiredDwnDeleted = "clip_expired_download_deleted";
	public static final String Resp_CatExpiredDwnDeleted = "category_expired_download_deleted";
	public static final String Resp_CatExpired = "category_expired";
	String m_clipOrCategoryExpiredDefault = "Requested Clip or Category is expired."; 
	
	//RBT-14278
	public static final String CATEGORY_OBJ = "category_object";
	public static final String DOUBLE_CONFIRMATION_FOR_XBI_PACK = "DOUBLE_CONFIRMATION_FOR_XBI_PACK";
	public static final String param_isXBIUser = "ISXBI_USER";
	public static final String XBI_PACK_ACT_REQ_SUCCESS = "XBI_PACK_ACT_REQ_SUCCESS";
	public static final String XBI_PACK_ALREADY_EXISTS = "XBI_PACK_ALREADY_EXISTS";
	public static final String XBI_PACK_ACT_REQ_FAILURE = "XBI_PACK_ACT_REQ_FAILURE";
	public static final String XBI_PACK_ACT_CONFIRM_REQ_SUCCESS = "XBI_PACK_ACT_CONFIRM_REQ_SUCCESS";
	public static final String XBI_PACK_ACT_CONFIRM_REQ_FAILURE = "XBI_PACK_ACT_CONFIRM_REQ_FAILURE";
	public static final String BASE_AND_COS_UPGRADATION_KEYWORD = "BASE_AND_COS_UPGRADATION_KEYWORD";
	public static final String BASE_UPGRADE_SUBCLASS_AND_COSID_MAP_NOT_FOUND = "BASE_UPGRADE_SUBCLASS_AND_COSID_MAP_NOT_FOUND";
	public String BASE_AND_COSID_UPGRADE_SUCCESS = "BASE_AND_COSID_UPGRADE_SUCCESS";
	public String BASE_AND_COS_UPGRADE_FAILURE = "BASE_AND_COS_UPGRADE_FAILURE";
	String m_baseUpgradeSubClassAndCosIDMapNotFound = "Subscription class and Cos Id Mapping not configured";
	String m_baseAndCosIdUpgradeSuccessDefault = "Successfully upgraded the base pack and cosId of the Subscriber";
	String m_baseAndCosIDUpgradeFailureDefault = "Failed to upgrade the base pack and cosId";
	public static final String action_deactivate_tone = "DEACTIVATE_TONE";
	public static final String Resp_InternalErr	   = "INTERNAL ERROR";
	public static final String Resp_Already_Deactive	   = "ALREADY DEACTIVE";
	public static final String Corporate_Selection_Not_Allowed="corporate_selection_not_allowed";

	//Added for RBT-17883
	public String param_ChargeMDN = "CHARGEMDN";
	public String param_SelType = "SELECTIONTYPE";
	
	public static final String NEW_USER = "new_user";
	String m_doubleConfirmationEntryExpiredTextDefault = "The request you have sent is expired";
	public String DOUBLE_CONFIRMATION_ENTRY_EXPIRED = "DOUBLE_CONFIRMATION_ENTRY_EXPIRED";
	String m_selectionAleardyActive = "The request you have sent is already active";
	public String SELECTION_ALREADY_ACTIVE = "SELECTION_ALREADY_ACTIVE";
	
	//Added for RBT-18249
	public static final String param_retry = "retry";
	
	//Added for VB-380
	public String SUBSCRIBER_DEACT_VIRAL_SUCCESS = "SUBSCRIBER_DEACT_VIRAL_SUCCESS";
	public String SUBSCRIBER_DEACT_VIRAL_FAILURE = "SUBSCRIBER_DEACT_VIRAL_FAILURE";	
	public static final String AZAAN_REQUEST_DCT_KEYWORD = "AZAAN_REQUEST_DCT_KEYWORD";
	public String AZAAN_DEACT_FAILURE = "AZAAN_DEACT_FAILURE";
	public String AZAAN_DEACT_SUCCESS = "AZAAN_DEACT_SUCCESS";
	public String AZAAN_DEACT_VIRAL_FAILURE = "AZAAN_DEACT_VIRAL_FAILURE";
	public String AZAAN_DEACT_VIRAL_SUCCESS = "AZAAN_DEACT_VIRAL_SUCCESS";

	public static final String param_isUpgradeSongSelection = "isUpgradeSongSelection";
	public static final String Download_monthly_limit_reached  ="download_monthly_limit_reached";
}