package com.onmobile.apps.ringbacktones.v2.common;

public abstract interface Constants {

	//RBT-2.0 response code
	public static String INVALID_PREFIX = "invalid_prefix";
	public static String UNKNOWN_ERROR = "UNKNOWNERROR";
	
	public static final String CLIP_NOT_IN_LIBRARY = "song_doesnt_exist_in_library";
	public static final String SUB_DONT_EXIST = "sub_doesnt_exist";
	public static final String SELECTION_ALREADY_ACTIVE = "selection_already_active";
	public static final String SUCCESS = "success";
	public static final String SUCCESS_DOWNLOAD_EXISTS			= "success_download_exists";
	public static final String CATEGORY_NOT_EXIST = "category_not_exists";
	
	//RBT 2.0 for ctype enum added
	public static final String RING_BACK = "ringback";
	public static final String RING_BACK_STATION = "ringback_station";
	public static final String RING_BACK_PLAYLIST = "ringback_playlist";
	public static final String INVALID_PARAMETER = "invalid_parameter";
	public static final String INVALID_CONTENT_TYPE = "invalid_content_type";
	//Added for subtype
	public static final String INVALID_CONTENT_SUB_TYPE = "invalid_content_sub_type";
	
	public static String FAILURE = "failure";
	
	//UDP 
	public static String UDP_ALREADY_ACTIVE = "udp_already_active"; 
	public static final String CLIP_NOT_EXIST	= "clip_not_exists";
	public static final String CLIP_EXPIRED	= "clip_expired";
	public static final String INVALID_UDP_ID = "invalid_udpId";
	public static final String CONTENT_NOT_FOUND = "Content_Not_Found";

	public static final String UDP_URL = "UDP_URL";
	public static final String UDP_ID_COL = "UDP_ID";
	
	//Group
	public static final String INVALID_GROUP_ID = "invalid_group_id";
	public static final String INVALID_GROUP_MEMBER_ID = "invalid_group_member_id";
	public static final String STATE_ACTIVATED = "B";
	public static final String DUPLICATE_CALLER_ENTRY ="duplicate_caller_entry";
	
	public static final String MEMBER_ID_NULL ="member_id_null";
	public static final String MEMBER_ALREADY_PRESENT ="member_already_present";
	public static final String MEMBER_NOT_PRESENT ="member_not_present";
	
	//PlayRule
	public static final String PLAY_RULE_DONT_EXIST = "play_rule_doesnt_exist";
	
	//ImplPath Prefixes
	public static final String SUBSCRIPTION_PREFIX = "subscription";
	
	//Subscription
	public static final String INVALID_SUBSCRIBER = "invalid_subscriber";	
	public static final String SUB_DEACT_PENDING = "subscriber_deact_pending";
	public static final String SUB_ACT_PENDING = "subscriber_act_pending";
	public static final String SUB_ACT_GRACE = "subscriber_grace";
	public static final String SUB_ACT_ERROR = "subscriber_act_error";
	public static final String SUB_SUSPENDED = "subscriber_suspended";
	public static final String SUB_ALREADY_EXISTS = "subscriber_already_exists";
	public static final String SUB_STATUS_CHANGE_NOT_ALLOWED = "status_change_not_allowed";
	//public static final String OFFER_NOT_FOUND = "offer_not_found";
	public static final String OFFER_NOT_INITILIAZED = "offer_not_initialized";
	public static final String CHARGE_CLASS = "chargeClass" ;
	public static final String USER_DETAILS = "userDetails" ;
	public static final String CALL_LOG_HISTORY  = "call_Log_History";
	public static final String LOG_NOT_FOUND = "log_not_found";
	public static final String SERVICE_KEY_REQ = "service_key_required";
	public static final String CATALOG_SUBSCRIPTION_ID_REQ = "catalog_subscription_id_required";
	
	//NextChargeClass
	public static final String INVALID_SUBSCRIBER_STATE = "invalid_subscriber_state";
	public static final String SUBSCRIPTION_CLASS_NOT_FOUND = "subscription_class_not_found";
	public static final String CHARGE_CLASS_NOT_FOUND = "charge_class_not_found";
	
	
	//Ugs Wavfile Constants
	public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
	public static final String THIRD_PARTY_SERVER_ERROR = "third_party_server_error";
	
	public static final String NO_FILE_OBJECT_IN_REQUEST = "no_file_object_in_request";
	public static final String DB_OPERATION_FAILED = "db_operation_failed";
	public static final String PACK_ALREADY_DEACTIVE = "pack_already_deactive";
	public static final String PACK_ALREADY_DELAY_DEACTIVE="pack_already_delay_deactive";
	public static final String USER_NOT_EXISTS="user_not_exists";

	
	//For Selection Mode
	public static final String FEATURE_NOT_SUPPORTED = "feature_not_supported";
	
	//For Combo
	public static final String OPERATOR_NOT_SUPPORTED = "operator_not_supported";
	public static final String ASSET_IS_REQUIRED = "asset_is_required";
	public static final String PURCHASE_IS_REQUIRED = "purchase_is_required";
	public static final String SUBSCRIPTION_IS_REQUIRED = "subscription_is_required";
	
	//Bean_Scope
	public static final String SCOPE_PROTOTYPE = "prototype";
	
	//Added for consent 
	public static final String PARAM_CONSENT_RETURN_SUCCESS_MESSAGE = "consent.return.message.success";
	public static final String PARAM_CONSENT_RETURN_SUCCESS_CODE = "consent.return.code.success";
	public static final String PARAM_CONSENT_RETURN_FAILURE_MESSAGE = "consent.return.message.failure";
	public static final String PARAM_CONSENT_RETURN_FAILURE_CODE = "consent.return.code.failure";
	public static final String  PARAM_CONSENT_RETURN_MESSAGE = "consent.return.message.";
	public static final String  PARAM_CONSENT_RETURN_CODE = "consent.return.code.";
	public static final String  PARAM_BSNL_SOUTH_KEYPASSWORD = "consent.bsnl.south.keypassword";
	
	
	public static final String PARAM_HT_CONSENT_URL = "ht.consent.url";
	public static final String PARAM_CONSENT_RETURN_LOW_BALANCE_MESSAGE = "consent.return.low.balance.message";
	public static final String PARAM_ERROR_CODE_MESSAGE = "error.message.response";
	public static final String PARAM_CONSENT_RETURN_TECHNICAL_DIFFICULTY_MESSAGE = "consent.return.technical.difficulty.message";
	
	public static final String THRIDPARTY_SERVER_DOWN = "thrid_party_server_down";
	
	//Added for ephemeral
	public static final String EPHEMERAL_NOT_SUPPORTED = "ephemeral_not_supported";
	
	//added for restriction 
	public static final String DEFAULT_SELECTION_NOT_SUPPORTED = "default_selection_not_supported";
	public static final String PROFILE_SELECTION_NOT_SUPPORTED = "profile_selection_not_supported";
	public static final String SPECIAL_SELECTION_NOT_SUPPORTED = "special_selection_not_supported";
	public static final String CONTENT_SELECTION_NOT_SUPPORTED = "content_selection_not_supported";
	public static final String CUT_RBT_NOT_SUPPORTED = "cut_rbt_not_supported";


	public static final String ACTIVATION_SUPPORTED_SELECTION_NOT_SUPPORTED = "activation_supported_selection_not_supported";
	
	
	public static final String NON_LEGACY_OPT = "non_legacy_opt";
	public static final String LEGACY_OPT = "legacy_opt";
	public static final String LEGACY_OPT_FREETRIAL = "legacy_opt_freetrial";
	public static final String LEGACY_OPT_FREETRIAL_EXPIRED = "legacy_opt_freetrial_expired";
	public static final String PAID_UNDER_LOWBAL= "PAID_UNDER_LOWBAL";	

	//RBTDeamonHelper
	public static final String  SERVEY_STATUS = "SERVEY_STATUS";
	public static final int  DEF_WAIT_TIME_FOR_SURVEY = 15;
}
