/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: gaurav.khandelwal $
 * $Id: WebServiceConstants.java,v 1.323 2015/06/18 11:01:26 gaurav.khandelwal Exp $
 * $Revision: 1.323 $
 * $Date: 2015/06/18 11:01:26 $
 */
package com.onmobile.apps.ringbacktones.webservice.common;

/**
 * @author vinayasimha.patil
 *
 */
public interface WebServiceConstants
{
	/* Subscriber Status */
	public static final String NEW_USER				= "new_user";
	public static final String ACT_PENDING			= "act_pending";
	public static final String ACT_ERROR			= "act_error";
	public static final String ACTIVE				= "active";
	public static final String RENEWAL_PENDING		= "renewal_pending";
	public static final String DEACT_PENDING		= "deact_pending";
	public static final String DEACT_ERROR			= "deact_error";
	public static final String DEACTIVE				= "deactive";
	public static final String GRACE				= "grace";
	public static final String RENEWAL_GRACE		= "renewal_grace";
	public static final String SUSPENDED			= "suspended";
	public static final String DCT_NOT_ALLOWED		= "dct_not_allowed";
	public static final String NOT_ALLOWED			= "not_allowed";
	public static final String REFUND_PENDING   	= "refund_pending";
	public static final String REFUNDED				= "refunded";
	public static final String ACT_CONSENT_PENDING	= "act_consent_pending";
	public static final String CONSENT_PENDING      = "consent_pending";
	public static final String UNKNOWN_USER			= "unknown_user";
	//RBT-14671 - # like
	public static final String TOP_LIKE_SONG = "topLikeSong";
	public static final String TOP_LIKE_SUBSCRIBER_SONG = "topLikeSubscriberSong";
	
	public static final String CONSENT_SELECTION_UNPROCESSED_RECORDS = "consent_sel_unprocessed_record";
	
	public static final String BLACK_LISTED		= "black_listed";
	public static final String INVALID_PREFIX	= "invalid_prefix";
	public static final String GIFTING_PENDING	= "gifting_pending";
	public static final String COPY_PENDING		= "copy_pending";
	public static final String LOCKED			= "locked";
	public static final String ACTIVATION_SUSPENDED		= "activation_suspended";
	//GrameenPhone if subscriber is voluntary suspension
	public static final String PAUSED			= "paused";
	//for dealyed deact
	public static final String ALREADY_DELAY_DEACT  = "already_delay_deact";
	/* Pack Status */
	public static final String PACK_NEW_USER			= "pack_new_user";
	public static final String PACK_ACT_PENDING		= "pack_act_pending";
	public static final String PACK_ACT_ERROR		= "pack_act_error";
	public static final String PACK_ACTIVE			= "pack_active";
	public static final String PACK_RENEWAL_PENDING	= "pack_renewal_pending";
	public static final String PACK_DEACT_PENDING	= "pack_deact_pending";
	public static final String PACK_DEACT_ERROR		= "pack_deact_error";
	public static final String PACK_DEACTIVE			= "pack_deactive";
	public static final String PACK_GRACE			= "pack_grace";
	public static final String PACK_RENEWAL_GRACE	= "pack_renewal_grace";
	public static final String PACK_SUSPENDED		= "pack_suspended";
	public static final String PACK_DCT_NOT_ALLOWED	= "pack_dct_not_allowed";
	public static final String PACK_NOT_ALLOWED		= "pack_not_allowed";
	public static final String PACK_BLACK_LISTED		= "pack_black_listed";
	public static final String PACK_INVALID_PREFIX	= "pack_invalid_prefix";
	public static final String PACK_GIFTING_PENDING	= "pack_gifting_pending";
	public static final String PACK_COPY_PENDING		= "pack_copy_pending";
	public static final String PACK_LOCKED			= "pack_locked";
	
	/* User Type */
	public static final String BOTH					= "both";
	public static final String PREPAID				= "prepaid";
	public static final String POSTPAID				= "postpaid";
	public static final String NORMAL				= "normal";
	public static final String AD_RBT				= "ad_rbt";
	public static final String REVERSE_RBT			= "reverse_rbt";
	public static final String NORMAL_REVERSE_RBT	= "normal_reverse_rbt";
	public static final String AD_REVERSE_RBT		= "ad_reverse_rbt";
	public static final String CORPORATE			= "corporate";
	public static final String EMOTION_RBT_USER		= "emotion_rbt_user";
	public static final String RBT_LITE_USER		= "rbt_lite_user";
	public static final String LANDLINE				= "landline";
	public static final String TNB					= "TNB";
	public static final String HYBRID				= "HYBRID";

	/* Content type */
	public static final String CLIP								= "clip";
	public static final String CATEGORY							= "category";
	public static final String CATEGORY_PARENT					= "category_parent";
	public static final String CATEGORY_DTMF_CLIPS				= "category_dtmf_clips";
	public static final String CATEGORY_BOUQUET					= "category_bouquet";
	public static final String CATEGORY_SHUFFLE					= "category_shuffle";
	public static final String CATEGORY_DYNAMIC_SHUFFLE			= "category_dynamic_shuffle";
	public static final String CATEGORY_LIST_CLIPS				= "category_list_clips";
	public static final String CATEGORY_SOUNDS					= "category_sounds";
	public static final String CATEGORY_RECORD					= "category_record";
	public static final String CATEGORY_SONGS					= "category_songs";
	public static final String CATEGORY_KARAOKE					= "category_karaoke";
	public static final String CATEGORY_INFO					= "category_info";
	public static final String CATEGORY_PROFILE_CLIPS			= "category_profile_clips";
	public static final String CATEGORY_FEED					= "category_feed";
	public static final String CATEGORY_PROMO_ID				= "category_promo_id";
	public static final String CATEGORY_ODA_SHUFFLE				= "category_oda_shuffle";
	public static final String UGC_CLIPS						= "ugc_clips";
	public static final String EMOTION_UGC_CLIPS				= "emotion_ugc_clips";
	public static final String GIFT								= "gift";
	public static final String SUPER_SONGS						= "super_songs";
	public static final String RE_SONGS							= "re_songs";
	public static final String CATEGORY_BOX_OFFICE_SHUFFLE		= "category_box_office_shuffle";
	public static final String CATEGORY_FESTIVAL_SHUFFLE		= "category_festival_shuffle";
	public static final String CATEGORY_FEEED_SHUFFLE			= "category_feed_shuffle";
	public static final String CATEGORY_DAILY_SHUFFLE			= "category_daily_shuffle";
	
	/* XML Constants*/

	public static final String RBT	= "rbt";
	public static final String RRBT	= "rrbt";
	public static final String YES	= "y";
	public static final String NO	= "n";

	public static final String PROTOCOLS = "protocols";
	public static final String PROTOCOL = "protocol";
	
	public static final String CLIP_ID					= "clip_id";
	public static final String NAME_FILE				= "name_file";
	public static final String PREVIEW_FILE				= "preview_file";
	public static final String RBT_FILE					= "rbt_file";
	public static final String DEMO_FILE				= "demo_file";
	public static final String CATEGORY_ID				= "category_id";
	public static final String SHUFFLE_ID				= "shuffle_id";
	public static final String CATEGORY_PREVIEW_FILE	= "category_preview_file";
	public static final String CATEGORY_NAME_FILE		= "category_name_file";
	public static final String CATEGORY_GREETING		= "category_greeting";
	public static final String CATEGORY_GRAMMAR			= "category_grammar";
	public static final String UGC_PREVIEW_FILE			= "ugc_preview_file";
	public static final String UGC_RBT_FILE				= "ugc_rbt_file";
	public static final String CRICKET_PREVIEW_FILE		= "cricket_preview_file";
	public static final String CHARGE_CLASS				= "charge_class";
	public static final String CHARGING_MODEL			= "charging_model";
	public static final String OPTINOUT_MODEL			= "optinout_model";
	public static final String ALBUM					= "album";
	public static final String ARTISTS					= "artists";
	public static final String CONTENT_TYPE				= "content_type";
	public static final String CLIP_INFO				= "clip_info";
	public static final String ARTIST			 	    = "artist";
	public static final String IMAGE_URL			 	= "image_url";
	public static final String SELECTION_STATUS_ID 		= "selectionStatusID";
	public static final String VCODE			 	    = "vcode";
	public static final String VALID_NUMBERS			= "valid_numbers";
	public static final String CALLER_FIRST_NAME		= "callerFirstName";
	public static final String CALLER_LAST_NAME			= "callerLastName";

	//Content Constants
	public static final String RECENT_SELECTIONS                    = "recent_selections";
	public static final String CONTENTS			= "contents";
	public static final String NO_OF_CONTENTS	= "no_of_contents";
	public static final String START_INDEX		= "start_index";
	public static final String END_INDEX		= "end_index";
	public static final String CONTENT			= "content";
	public static final String SCRATCH_STATE	= "scratchstate";
	public static final String ID				= "id";
	public static final String NAME				= "name";
	public static final String TYPE				= "type";
	public static final String PASSWORD_DAYS_LEFT		= "password_days_left";
	public static final String PASSWORD_EXPIRED		= "password_expired";
	public static final String REF_AMOUNT		= "refund_amt";
	public static final String VALIDITY		= "validity";
	public static final String PROPERTY			= "property";
	public static final String VALUE			= "value";
	public static final String DATA				= "data";
	public static final String PROMPT			= "prompt";
	public static final String GRAMMAR			= "grammar";
	public static final String AGENT_ID			= "agentId";
	public static final String REASON			= "reason";
	public static final String SONG_NAME	    = "songName";
	
	//Response codes
	public static final String RESPONSE							= "response";
	public static final String VALID							= "valid";
	public static final String INVALID							= "invalid";
	public static final String INVALID_GIFTEE_ID                = "invalid_giftee_id";
	public static final String SUCCESS							= "success";
	public static final String FAILED							= "failed";
	public static final String ERROR							= "error";
	public static final String FAILURE							= "failure";
	public static final String INVALID_IP						= "invalid_ip";
	public static final String TECHNICAL_DIFFICULTIES			= "technical_difficulties";
	public static final String OVERLIMIT						= "overlimit";
	public static final String LIMIT_EXCEEDED					= "limit_exceeded";
	public static final String SELECTIONS_BLOCKED               = "selections_blocked";
	public static final String USER_NOT_ACTIVE					= "user_not_active";
	public static final String USER_NOT_EXISTS					= "user_not_exists";
	public static final String SUBSCRIPTION					    = "Subscription";
	public static final String UNSUBSCRIPTION					= "Unubscription";
	public static final String CUSTOMIZATION					= "Customization";
	public static final String PURCHASE							= "Purchase";
	public static final String NOT_ALLOWED_FOR_GRACE_USER		= "not_allowed_for_grace_user";
	public static final String INVALID_RRBT_CONSENT_DEACT_REQ	= "invalid_rrbt_consent_deact_req";
	public static final String OPERATOR_NOT_CONFIGURED			= "operator_not_configured";
	
	public static final String ALREADY_ACTIVE					= "already_active";
	public static final String ALREADY_MEMBER_OF_GROUP			= "already_member_of_group";
	public static final String SETTING_EXISTS_FOR_MEMBER		= "setting_exists_for_member";
	public static final String OFFER_NOT_FOUND					= "offer_not_found";
	public static final String PICK_OF_THE_DAY_NOT_FOUND		= "pick_of_the_day_not_found";
	public static final String SELECTION_SUSPENDED				= "selection_suspended";
	public static final String INVALID_ACTION					= "invalid_action";
	public static final String INVALID_PARAMETER				= "invalid_parameter";
	public static final String CATEGORY_NOT_EXISTS				= "category_not_exists";
	public static final String CRICKET_PACK_NOT_EXISTS			= "cricket_pack_not_exists";
	public static final String INVALID_CATEGORY					= "invalid_category";
	public static final String CLIP_NOT_EXISTS					= "clip_not_exists";
	public static final String CLIP_EXPIRED						= "clip_expired";
	public static final String ALREADY_EXISTS					= "already_exists";
	public static final String OWN_NUMBER						= "own_number";
	public static final String ALREADY_USED						= "already_used";
	public static final String ERROR_STATE						= "error_state";
	public static final String NOT_EXISTS						= "not_exists";
	public static final String NO_SELECTIONS					= "no_selections";
	public static final String NO_DOWNLOADS						= "no_downloads";
	public static final String NO_BOOKMARKS						= "no_bookmarks";
	public static final String INVALID_CODE						= "invalid_code";
	public static final String ALREADY_VOLUNTARILY_SUSPENDED	= "already_voluntarily_suspended";
	public static final String ALREADY_SUSPENDED                = "already_suspended";
	public static final String DOWNLOADS_PENDING				= "download_pending";
	public static final String CATEGORY_EXPIRED					= "category_expired";
	public static final String DOWNLOAD_SUSPENDED				= "download_suspended";
	public static final String DOWNLOAD_GRACE					= "download_grace";
	public static final String COS_NOT_EXISTS					= "cos_not_exists";
	public static final String COS_NOT_UPGRADE_USER_NO_SELECTION = "cos_not_upgrade_user_no_selection";
	public static final String BIRESPONSE						= "BiResponse";
	public static final String NO_PENDING_REQUESTS				= "no_pending_requests";
	public static final String REQUEST_PENDING					= "request_pending";
	public static final String ALREADY_REGISTERED				= "already_registered";
	public static final String NO_PINS_AVAILABLE				= "no_pins_available";
	public static final String PIN_EXPIRED						= "pin_expired";
	public static final String MAX_PINS_LIMIT_REACHED			= "max_pins_limit_reached";
	public static final String ALREADY_DOWNLOADED				= "already_downloaded";
	public static final String ACTIVATION_BLOCKED				= "activation_blocked";
	public static final String NO_SELECTIONS_TO_UPGRADE			= "no_selections_to_upgrade";
	public static final String NO_DOWNLOADS_TO_UPGRADE			= "no_downloads_to_upgrade";
	public static final String PREVIOUS_DOWNLOAD_PENDING		= "previous_download_pending";
	public static final String PREVIOUS_SELECTION_PENDING		= "previous_selection_pending";
	public static final String NOT_VOLUNTARILY_SUSPENDED_USER	= "not_voluntarily_suspended_user";
	public static final String PACK_DOWNLOAD_LIMIT_REACHED		= "pack_download_limit_reached";
	
	public static final String CORPORATE_SUSPENSION_NOT_ALLOWED = "corporate_suspension_not_allowed";
	public static final String RBT_CORPORATE_NOTALLOW_SELECTION = "corporate_selection_not_allowed";
	public static final String SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE = "selection_not_allowed_for_user_on_blocked_service";
	public static final String NOT_VOLUNTARILY_SUSPENDED		= "not_voluntarily_suspended";
	public static final String CALLER_BLOCKED					= "caller_blocked";
	public static final String ALREADY_BLOCKED					= "already_blocked";
	public static final String SUCCESS_DOWNLOAD_EXISTS			= "success_download_exists";
	public static final String SELECTION_OVERLIMIT				= "selection_overlimit";
	public static final String PERSONAL_SELECTION_OVERLIMIT		= "personal_selection_overlimit";
	public static final String LOOP_SELECTION_OVERLIMIT			= "loop_selection_overlimit";
	public static final String SUSPENSION_NOT_ALLOWED			= "suspension_not_allowed";
	public static final String NO_DATA							= "no_data";
	public static final String ANOTHER_TASK_IN_PROGRESS			= "another_task_in_progress";
	public static final String TASK_DOES_NOT_EXIST				= "task_does_not_exists";
	public static final String TASK_ALREADY_PROCESSED			= "task_already_processed";
	public static final String TASK_BEING_PROCESSED				= "task_being_processed";
	public static final String TASK_EDIT_NOT_ALLOWED			= "task_edit_not_allowed";
	public static final String LITE_USER_PREMIUM_BLOCKED		= "lite_user_premium_blocked";
	public static final String COSID_BLOCKED_CIRCKET_PROFILE	= "cosid_blocked_circket_profile";
	public static final String COSID_BLOCKED_FOR_NEW_USER		= "cosid_blocked_for_new_user";
	public static final String COSID_BLOCKED_FOR_USER		    = "cosid_blocked_for_user";
	public static final String COS_MISMATCH_CONTENT_BLOCKED		= "content_purchase_blocked_";
	public static final String COPY_COS_MISMATCH_CONTENT_BLOCKED= "CONTENT_PURCHASE_BLOCKED_";
	public static final String TASK_DELETION_NOT_ALLOWED		= "task_deletion_not_allowed";
	public static final String TASK_ALREADY_REMOVED				= "task_already_removed";
	public static final String PIN_LIMIT_EXCEEDED				= "pin_limit_exceeded";
	public static final String INVALID_SUBSCRIPTION_CLASS		= "invalid_subscription_class";
	public static final String REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED= "reactivation_with_same_song_not_allowed";

	public static final String EMOTION_RBT_NOT_ALLOWED_FOR_SPECIFIC_CALLER	= "emotion_rbt_not_allowed_for_specific_caller";
	public static final String ADRBT_USER						= "ADRBT_USER";
	public static final String NOT_AN_ADRBT_USER				= "NOT_AN_ADRBT_USER";

	//Subscriber cos type
	public static final String LITE								= "LITE";
	public static final String PROFILE1								= "PROFILE1";

	public static final String PACK_ALREADY_ACTIVE = "pack_already_active";
	public static final String PACK_ALREADY_DEACTIVE = "pack_already_deactive";
	public static final String INVALID_PACK_COS_ID = "invalid_pack_cos_id";
	public static final String SUCCESS_PACK_NOT_AVAILABLE = "success_pack_not_available";

	//Subscriber Details Constants
	public static final String SUBSCRIBER_DETAILS			= "subscriber_details";
	public static final String SUBSCRIBER					= "subscriber";
	public static final String SUBSCRIBER_ID				= "subscriber_id";
	public static final String IS_VALID_PREFIX				= "is_valid_prefix";
	public static final String CAN_ALLOW					= "can_allow";
	public static final String IS_PREPAID					= "is_prepaid";
	public static final String ACCESS_COUNT					= "access_count";
	public static final String STATUS						= "status";
	public static final String CIRCLE_ID					= "circle_id";
	public static final String LANGUAGE						= "language";
	public static final String USER_TYPE					= "user_type";
	public static final String IS_ALBUM_USER				= "is_album_user";
	public static final String SUBSCRIPTION_CLASS			= "subscription_class";
	public static final String COS_ID						= "cos_id";
	public static final String ACTIVATED_BY					= "activated_by";
	public static final String ACTIVATION_INFO				= "activation_info";
	public static final String DEACTIVATED_BY				= "deactivated_by";
	public static final String LAST_DEACTIVATION_INFO		= "last_deactivation_info";
	public static final String START_DATE					= "start_date";
	public static final String END_DATE						= "end_date";
	public static final String NEXT_CHARGING_DATE			= "next_charging_date";
	public static final String LAST_DEACTIVATION_DATE		= "last_deactivation_date";
	public static final String ACTIVATION_DATE				= "activation_date";
	public static final String IS_NEWS_LETTER_ON			= "is_news_letter_on";
	public static final String DAYS_AFTER_DEACTIVATION		= "days_after_deactivation";
	public static final String USER_INFO					= "user_info";
	public static final String OPERATOR_USER_INFO			= "operator_user_info";
	public static final String NEXT_BILLING_DATE			= "next_billing_date";
	public static final String CHARGE_DETAILS				= "charge_details";
	public static final String SUBSCRIPTION_STATE			= "subscription_state";
	public static final String IS_REFUNDABLE				= "is_refundable";
	public static final String IS_UDS_ON					= "is_uds_on";
	public static final String IMEI_NO						= "imei_no";
	public static final String NO_OF_FREE_SONGS_LEFT		= "no_of_free_songs_left";
	public static final String PACK							= "pack";
	public static final String SEGMENT						= "segment";
	public static final String AGE							= "age";
	public static final String PCA							= "pca";
	public static final String BI_DOWNLOAD_HISTORY_INFO		= "bi_download_history";
	public static final String SUBSCRIBER_PACKS				= "subscriber_packs";
	public static final String LAST_CHARGE_AMOUNT			= "last_charge_amount";
	public static final String LAST_TRANSACTION_TYPE			= "last_transaction_type";
	public static final String ALREADY_UNSUB_DELAY          ="already_unsub_delay";
	public static final String CONSENT					    = "consent";
	public static final String CONSENTS					    = "consents";
	public static final String CONSENT_EXPIRED			    ="consent_expired";
    public static final String IS_FREEMIUM_SUBSCRIBER       ="is_freemium_subscriber";
    public static final String IS_FREEMIUM        			="is_freemium";
    public static final String NUM_OF_FREE_SONGS_LEFT       ="num_free_songs_left";
    public static final String NUM_MAX_SELECTIONS			= "num_max_selections";
    public static final String XML_OPERATOR_NAME 				= "operator_name";
    
	//RBT OTP Login User constants
	public static final String RBT_OTP_LOGIN                = "otp_login";
	public static final String param_regeneratePassword     = "regeneratePassword";
	//Subscriber Gift Inbox and Outbox Constants
	public static final String GIFT_INBOX	= "gift_inbox";
	public static final String GIFT_OUTBOX	= "gift_outbox";
	public static final String NO_OF_GIFTS	= "no_of_gifts";
	public static final String SENDER		= "sender";
	public static final String RECEIVER		= "receiver";
	public static final String SENT_TIME	= "sent_time";
	public static final String SERVICE		= "service";
	public static final String EXTRA_INFO	= "extra_info";
	public static final String SERVICEKEY		= "servicekey";
	//Subscriber Library Details Constants
	public static final String LIBRARY					= "library";
	public static final String LIBRARY_HISTORY			= "library_history";
	public static final String REFUNDABLE_SELECTIONS	= "refundable_selections";
	public static final String SETTINGS					= "settings";
	public static final String NO_OF_SETTINGS			= "no_of_settings";
	public static final String NO_OF_DEFAULT_SETTINGS	= "no_of_default_settings";
	public static final String IS_RECENT_SEL_CONSENT	= "is_recent_sel_consent";
	public static final String NO_OF_SPECIAL_SETTINGS	= "no_of_special_settings";
	public static final String SELECTION_STATUS			= "selection_status";
	public static final String CALLER_ID				= "caller_id";
	public static final String FROM_TIME				= "from_time";
	public static final String FROM_TIME_MINUTES		= "from_time_minutes";
	public static final String TO_TIME					= "to_time";
	public static final String TO_TIME_MINUTES			= "to_time_minutes";
	public static final String DOWNLOADS				= "downloads";
	public static final String NO_OF_DOWNLOADS			= "no_of_downloads";
	public static final String NO_OF_ACTIVE_DOWNLOADS	= "no_of_active_downloads";
	public static final String DOWNLOAD_STATUS			= "download_status";
	public static final String DOWNLOAD_TYPE			= "download_type";
	public static final String NEXT_SELECTION_AMOUNT	= "next_selection_amount";
	public static final String NEXT_CHARGE_CLASS		= "next_charge_class";
	public static final String LOOP_INDEX				= "loop_index";
	public static final String IS_SET_FOR_ALL			= "is_set_for_all";
	public static final String INTERVAL					= "interval";
	public static final String SELECTED_BY				= "selected_by";
	public static final String SELECTION_INFO			= "selection_info";
	public static final String DESELECTED_BY			= "deselected_by";
	public static final String SET_TIME					= "set_time";
	public static final String END_TIME					= "end_time";
	public static final String SELECTION_EXTRA_INFO		= "selection_extra_info";
	public static final String DOWNLOAD_INFO			= "download_info";
	public static final String REF_ID					= "ref_id";
	public static final String CONTENT_END_TIME			= "content_end_time";
	public static final String START_TIME				= "start_time";
	public static final String SUBSCRIBERPACKS			= "subscriberpacks";
	public static final String SUBSCRIBERPACK			= "subscriberpack";
	public static final String LAST_CHARGED_DATE		= "last_charged_date";
	public static final String CALLER_NAME				= "callerName";
	public static final String CUT_RBT_START_TIME		= "cutrbt_start_time";
	
	// JIRA-RBT-6194:Search based on songs in Query Gallery API 
	public static final String SEARCH_GALLERY			= "search_gallery";
	//Subscriber BookMark Details Constants
	public static final String BOOKMARKS		= "bookmarks";
	public static final String NO_OF_BOOKMARKS	= "no_of_bookmarks";

	//Subscriber offer constants
	public static final String OFFERS				= "offers";
	public static final String OFFER				= "offer";
	public static final String OFFER_ID				= "offer_id";
	public static final String TOBE_ACT_OFFER_ID	= "tobe_act_offer_id";
	public static final String OFFER_DESC			= "offer_desc";
	public static final String OFFER_AMOUNT			= "offer_amount";
	public static final String OFFER_VALID_DAYS		= "offer_validity_days";
	public static final String OFFER_TYPE			= "offer_type";
	public static final String OFFER_TYPE_VALUE		= "offer_type_value";
	public static final String OFFER_SRVKEY			= "offer_srvKey";
	public static final String OFFER_METAINFO		= "offer_metainfo";
	public static final String OFFER_STATUS			= "offer_status";
	public static final String OFFER_SM_OFFER_TYPE	= "sm_offer_type";
	public static final String OFFER_SM_RATE		= "sm_offer_rate";
	public static final String OFFER_CREDITS_AVAILABLE= "credits_available";
	//RBT-14540: Added to support offer validity, offer renewal amout and offer renewal validity.
	public static final String OFFER_VALIDITY    	= "offer_validity";
	public static final String OFFER_RENEWAL_AMOUNT	= "offer_renewal_amount";
	public static final String OFFER_RENEWAL_VALIDITY = "offer_renewal_validity";

	//Call Details Constants
	public static final String CALL_DETAILS			= "call_details";
	public static final String LANGUAGES			= "languages";
	public static final String ASK_LANGUAGE			= "ask_language";
	public static final String PICK_OF_THE_DAY		= "pick_of_the_day";
	public static final String HOT_SONG				= "hot_song";
	public static final String MM_CONTENT			= "mm_content";
	public static final String IS_TOLL_FREE_NO		= "is_toll_free_no";
	public static final String ADVANCE_RENTAL_PACKS	= "advance_rental_packs";
	public static final String PACKS				= "packs";
	public static final String TNB_DETAILS			= "tnb_details";
	public static final String MAX_ACCESS			= "max_access";
	public static final String EASY_CHARGE			= "easy_charge";
	public static final String CRICKET_DETAILS		= "cricket_details";
	public static final String IS_CRICKET_USER		= "is_cricket_user";
	public static final String ALBUM_DETAILS		= "album_details";
	public static final String SCRATCHCARD_DETAILS	= "scratchcard_details";
	public static final String ASK_NUMBER			= "ask_number";
	public static final String NUMBER				= "number";
	public static final String DOWNLOAD_PROMPT		= "download_prompt";
	public static final String PROFILES				= "profiles";
	public static final String CHARGING_MODELS		= "charging_models";

	public static final String COS_DETAILS				= "cos_details";
	public static final String COS						= "cos";
	public static final String COS_TYPE_LITE			= "LITE";
	public static final String COS_CONTENT_TYPE_PROFILE	= "PROFILE";
	public static final String IS_DEFAULT				= "is_default";
	public static final String VALID_DAYS				= "valid_days";
	public static final String FREE_SONGS				= "free_songs";
	public static final String FREE_MUSICBOXES			= "free_musicboxes";
	public static final String ACTIVATION_INTRO_PROMPT	= "activation_intro_prompt";
	public static final String ACTIVATION_PROMPT		= "activation_prompt";
	public static final String CLIP_DOWNLOAD_PROMPT		= "clip_download_prompt";
	public static final String MUSICBOX_DOWNLOAD_PROMPT	= "musicbox_download_prompt";
	public static final String TOTAL_DOWNLOADS			= "total_downloads";
	public static final String PROMO_CLIPS				= "promo_clips";
	public static final String OPERATOR_CODE			= "operator_code";
	public static final String ACCESS_MODE				= "access_mode";
	public static final String CONTENT_TYPE_FEED		= "FEED";

	public static final String PROVIDE_DEFAULT_LOOP_OPTION			= "provide_default_loop_option";
	public static final String IS_MUSICBOX_DEFAULT_SETTING_PENDING	= "is_musicbox_default_setting_pending";
	public static final String IS_CLIP_DEFAULT_SETTING_PENDING		= "is_clip_default_setting_pending";

	//Copy Details Constants
	public static final String COPY_DETAILS					= "copy_details";
	public static final String RESULT						= "result";
	public static final String FROM_SUBSCRIBER				= "from_subscriber";
	public static final String USER_HAS_MULTIPLE_SELECTIONS	= "user_has_multiple_selections";
	public static final String NOT_RBT_USER					= "not_rbt_user";
	public static final String ALBUM_RBT					= "album_rbt";
	public static final String DEFAULT_RBT					= "default_rbt";
	public static final String PERSONAL_MESSAGE				= "personal_message";

	public static final String GIFTEE_ACTIVE			= "giftee_active";
	public static final String GIFTEE_NEW_USER			= "giftee_new_user";
	public static final String GIFTER_NOT_ACT			= "gifter_not_act";
	public static final String GIFTEE_ACT_PENDING		= "giftee_act_pending";
	public static final String GIFTEE_DEACT_PENDING		= "giftee_deact_pending";
	public static final String GIFTEE_GIFT_ACT_PENDING	= "giftee_gift_act_pending";
	public static final String GIFTEE_GIFT_IN_USE		= "giftee_gift_in_use";
	public static final String EXISTS_IN_GIFTEE_LIBRAY	= "exists_in_giftee_libray";

	public static final String ALL		= "all";
	public static final String PROFILE	= "profile";
	public static final String CRICKET	= "cricket";
	
	//Subscriber pack constants
	public static final String PACK_COS_ID					= "cos_id";
	public static final String PACK_REF_ID					= "ref_id";
	public static final String PACK_CHARGE_CLASS			= "charge_class";
	public static final String PACK_START_TIME				= "star_time";
	public static final String PACK_MODE_INFO				= "mode_info";
	public static final String PACK_MODE					= "mode";
	public static final String PACK_COS_TYPE				= "cos_type";
	public static final String PACK_DEACTIVATE_MODE			= "deactivate_mode";
	public static final String PACK_DEACTIVATE_TIME			= "deactivate_time";
	public static final String PACK_DEACTIVATE_MODE_INFO	= "deactivate_mode_info";
	public static final String PACK_LAST_CHARGING_TIME		= "last_charging_date";
	public static final String PACK_STATUS					= "status";
	public static final String PACK_AMOUNT					= "amount";
	public static final String PACK_NUM_MAX_SELECTIONS		= "num_max_selections";

	//SMS
	public static final String SMS			= "sms";
	public static final String SEND_SMS		= "send_sms";
	public static final String SMS_TEXT		= "sms_text";
	public static final String SMS_HISTORY	= "sms_history";
	public static final String SMS_HISTORY_FROM_UMP	= "sms_history_from_ump";

	//WC ON OFF HISTORY(VOLUNTARY SUSPENSION)
	public static final String WC_HISTORY	    = "wc_history";
	public static final String WC_REQUEST_DATE	= "request_date";
	public static final String WC_ACTION	    = "action";
	public static final String WC_RETAILER_ID	= "retailer_id";
	public static final String WC_MODE_INFO	    = "mode_info";
	public static final String WC_MODE	        = "mode";
	
	
	//Group Details constants
	public static final String GROUP_DETAILS		= "group_details";
	public static final String GROUPS				= "groups";
	public static final String GROUP				= "group";
	public static final String NO_OF_GROUPS			= "no_of_groups";
	public static final String NO_OF_ACTIVE_GROUPS	= "no_of_active_groups";
	public static final String GROUP_PROMO_ID		= "group_promo_id";
	public static final String PREDEFINED_GROUP_ID	= "predefined_group_id";
	public static final String GROUP_STATUS			= "group_status";
	public static final String GROUP_NAME_PROMPT	= "group_name_prompt";
	public static final String GROUP_MEMBERS		= "group_members";
	public static final String ALL_MEMBERS			= "all_members";
	public static final String NO_OF_MEMBERS		= "no_of_members";
	public static final String NO_OF_ACTIVE_MEMBERS	= "no_of_active_members";
	public static final String MEMBER_STATUS		= "member_status";
	public static final String PREDEFINED_GROUPS	= "predefined_groups";

	//Application Details constants
	public static final String APPLICATION_DETAILS			= "application_details";
	public static final String PARAMETERS					= "parameters";
	public static final String SUBSCRIPTION_CLASSES			= "subscription_classes";
	public static final String CHARGE_CLASSES				= "charge_classes";
	public static final String AMOUNT						= "amount";
	public static final String PRICE						= "price";
	public static final String PERIOD						= "period";
	public static final String ISSHUFFLEORLOOP				= "is_shuffle_or_loop";
	public static final String RENEWAL_AMOUNT				= "renewal_amount";
	public static final String RENEWAL_PERIOD				= "renewal_period";
	public static final String SHOW_ON_GUI					= "show_on_gui";
	public static final String OPERATOR_CODE_1				= "operator_code_1";
	public static final String SMS_TEXTS					= "sms_texts";
	public static final String PICK_OF_THE_DAYS				= "pick_of_the_days";
	public static final String PLAY_DATE					= "play_date";
	public static final String CHARGE_SMS					= "charge_sms";
	public static final String PREPAID_SUCCESS_SMS			= "prepaid_success_sms";
	public static final String PREPAID_FAILURE_SMS			= "prepaid_failure_sms";
	public static final String POSTPAID_SUCCESS_SMS			= "postpaid_success_sms";
	public static final String POSTPAID_FAILURE_SMS			= "postpaid_failure_sms";
	public static final String PREPAID_NEF_SUCCESS_SMS		= "prepaid_nef_success_sms";
	public static final String PREPAID_RENEWAL_SUCCESS_SMS	= "prepaid_renewal_success_sms";
	public static final String PREPAID_RENEWAL_FAILURE_SMS	= "prepaid_renewal_failure_sms";
	public static final String POSTPAID_RENEWAL_SUCCESS_SMS	= "postpaid_renewal_success_sms";
	public static final String POSTPAID_RENEWAL_FAILURE_SMS	= "postpaid_renewal_failure_sms";
	public static final String FEED_DETAILS					= "feed_details";
	public static final String SUBSCRIPTION_PERIOD			= "subscription_period";
	public static final String SUBSCRIPTION_AMOUNT			= "subscription_amount";

	public static final String BLACKLIST					= "blacklist";
	public static final String SUBSCRIBER_STATUS			= "subscriber_status";
	public static final String LOGIN_USER					= "login_user";
	

	//RBT Login User constants
	public static final String RBT_LOGIN_USER	= "rbt_login_user";
	public static final String USER_ID			= "user_id";
	public static final String PASSWORD			= "password";
	public static final String RBT_LOGIN_USERS	= "rbt_login_users";
	public static final String CREATION_TIME	= "creation_time";
		
	//Sites Constant
	public static final String SITES				= "sites";
	public static final String SITE_PREFIX			= "site_prefix";
	public static final String SITE_URL				= "site_url";
	public static final String ACCESS_ALLOWED		= "access_allowed";
	public static final String SUPPORTED_LANGUAGES	= "supported_languages";
	public static final String PLAYER_URL			= "player_url";
	public static final String PLAY_UNCHARGED_FOR	= "play_uncharged_for";

	//Viral Data Constants
	public static final String VIRAL_DATA	= "viral_data";
	public static final String COUNT		= "count";
	public static final String INFO			= "info";
	public static final String SMS_ID		= "sms_id";
	public static final String VIRAL_SMS_TABLE_ARRAY	= "viral_data_array";

	//Transaction History Constants
	public static final String TRANSACTION_HISTORY	= "transaction_history";
	public static final String MODE					= "mode";
	public static final String DATE					= "date";

	//Retailer Constants
	public static final String RETAILER	= "retailer";

	//Feed Status Constants
	public static final String FEED_STATUS				= "feed_status";
	public static final String FEED_FILE				= "feed_file";
	public static final String SMS_KEYWORDS				= "sms_keywords";
	public static final String SUB_KEYWORDS				= "sub_keywords";
	public static final String FEED_ON_SUCCESS_SMS		= "feed_on_success_sms";
	public static final String FEED_ON_FAILURE_SMS		= "feed_on_failure_sms";
	public static final String FEED_OFF_SUCCESS_SMS		= "feed_off_success_sms";
	public static final String FEED_OFF_FAILURE_SMS		= "feed_off_failure_sms";
	public static final String FEED_FAILURE_SMS			= "feed_failure_sms";
	public static final String FEED_NON_ACTIVE_USER_SMS	= "feed_non_active_user_sms";

	//Trans Data Constants
	public static final String TRANS_DATA	= "trans_data";
	
	//RBtSupport Data Constants
	public static final String RBTSUPPORT_DATA	= "rbtsupport_data";

	//Subscriber Promo Constants
	public static final String SUBSCRIBER_PROMO		= "subscriber_promo";
	public static final String FREE_DAYS			= "free_days";
	public static final String SUBSCRIPTION_TYPE	= "subscription_type";

	//Info Constants
	public static final String TNB_TO_NORMAL		= "tnb_to_normal";
	public static final String UPGRADE_VALIDITY		= "upgrade_validity";
	public static final String RENEW				= "renew";
	public static final String MODIFY				= "modify";
	public static final String DEACT_DELAY			= "deact_delay";
	public static final String WEEKLY_TO_MONTHLY	= "weekly_to_monthly";
	public static final String UPGRADE_SEL_PACK		= "upgrade_sel_pack";

	//Added by Sreekar for Airtel Comes With Music opt in 
	public static final String CONFIRM_CHARGE		= "confirm_charge";

	// BulkUpload Constants
	public static final int BULKTASK_STATUS_INIT			= -1;
	public static final int BULKTASK_STATUS_NEW				= 0;
	public static final int BULKTASK_STATUS_SUCCESS			= 1;
	public static final int BULKTASK_STATUS_FAILURE			= 2;
	public static final int BULKTASK_STATUS_PROCESSING		= 3;
	public static final int BULKTASK_STATUS_EDITED			= 4;
	public static final int BULKTASK_STATUS_CAMPAIGN_ENDED	= 5;
	public static final int BULKTASK_STATUS_REMOVED			= 6;

	public static final int BULKTASK_SUBSCRIBER_DELETE		= 3;  // BulkTaskSubscribers table status
	public static final int BULKTASK_SUBSCRIBER_DELETED		= 5;

	public static final String BULK_TASKS				= "bulk_tasks";
	public static final String BULK_TASK_ID				= "task_id";
	public static final String BULK_TASK_STATUS			= "task_status";
	public static final String BULK_TASK_NAME			= "task_name";
	public static final String BULK_TASK_TYPE			= "task_type";
	public static final String SELECTION_TYPE			= "selection_type";
	public static final String BULK_UPLOAD_TIME			= "upload_time";
	public static final String BULK_PROCESS_TIME		= "process_time";
	public static final String BULK_END_TIME			= "end_time";
	public static final String BULK_TASK_MODE			= "task_mode";
	public static final String BULK_TASK_INFO			= "task_info";
	public static final String EDIT_TASK				= "edit_task";
	public static final String CAMPAIGN_ID				= "CAMPAIGN_ID";


	public static final String BULKACTION_ACTIVATION	= "ACTIVATION";
	public static final String BULKACTION_DEACTIVATION	= "DEACTIVATION";
	public static final String BULKACTION_SELECTION		= "SELECTION";
	public static final String BULKACTION_DELETION		= "DELETION";
	public static final String BULKACTION_CORPORATE		= "CORPORATE";

	public static final String EMOTION_RBT  = "EMOTION_RBT";


	//Web Service API names
	public static final String api_Caller_Based_Multi_Selection			= "MultiSelection";
	public static final String api_Rbt					= "Rbt";
	public static final String api_Subscription			= "Subscription";
	public static final String api_Selection			= "Selection";
	public static final String api_SelectionPreConsent	= "SelectionPreConsent";

	public static final String api_SubscriptionPreConsent	= "SubscriptionPreConsent";
	public static final String api_SelectionConsentIntegration = "SelectionConsentIntegration";
	public static final String api_SelectionPreConsentInt = "SelectionPreConsentInt";
	public static final String api_BookMark				= "BookMark";
	public static final String api_Copy					= "Copy";
	public static final String api_Gift					= "Gift";
	public static final String api_ValidateNumber		= "ValidateNumber";
	public static final String api_SetSubscriberDetails	= "SetSubscriberDetails";
	public static final String api_Group				= "Group";
	public static final String api_Utils				= "Utils";
	public static final String api_ApplicationDetails	= "ApplicationDetails";
	public static final String api_BulkTask				= "BulkTask";
	public static final String api_BulkUploadTask		= "BulkUploadTask";
	public static final String api_Content				= "Content";
	public static final String api_Data					= "Data";
	public static final String api_Offer				= "Offer";
	public static final String api_Sng					= "Sng";
	public static final String api_WebService			= "WebService";
    public static final String api_Search               = "Search"; 
    public static final String api_RBTDownloadFile		= "RBTDownloadFile";
    public static final String api_CallLog				= "callLog";
    //RBT-14652
    public static final String api_SAT					= "Sat";
	//Web Service actions
    public static final String action_rrbt_consent_deactivate	= "rrbt_consent_deactivate";
    public static final String action_reset                 ="reset";
	public static final String action_activate				= "activate";
	public static final String action_refer					= "refer";
	public static final String deactivate_all				= "deactivate_all";
	public static final String action_deactivate			= "deactivate";
	public static final String action_gift					= "gift";
	public static final String action_sendGift				= "send_gift";
	public static final String action_acceptGift			= "accept_gift";
	public static final String action_rejectGift			= "reject_gift";
	public static final String action_overwriteGift			= "overwrite_gift";
	public static final String action_add					= "add";
	public static final String action_remove				= "remove";
	public static final String action_overwrite				= "overwrite";
	public static final String action_get					= "get";
	public static final String action_getdownloadOfDays		= "get_download_of_days";
	public static final String action_downloadOfDay			= "download_of_day";
	public static final String action_set					= "set";
	public static final String action_process				= "process";
	public static final String action_update				= "update";
	public static final String action_upgrade				= "upgrade";
	public static final String action_upgradeSelection		= "upgrade_selection";
	public static final String action_deleteSetting			= "delete_setting";
	public static final String action_downloadTone			= "download_tone";
	public static final String action_deleteTone			= "delete_tone";
	public static final String action_overwriteDownload		= "overwrite_download";
	public static final String action_downloadGift			= "download_gift";
	public static final String action_overwriteDownloadGift	= "overwrite_download_gift";
	public static final String action_shuffle				= "shuffle";
	public static final String action_unRandomize			= "unRandomize";
	public static final String action_default				= "default";
	public static final String action_addMember				= "add_member";
	public static final String action_updateMember			= "update_member";
	public static final String action_moveMember			= "move_member";
	public static final String action_removeMember			= "remove_member";
	public static final String action_addMultipleMember				= "add_multiple_member";
	public static final String action_updateMultipleMember			= "update_multiple_member";
	public static final String action_moveMultipleMember			= "move_multiple_member";
	public static final String action_removeMultipleMember			= "remove_multiple_member";
	public static final String action_personalize			= "personalize";
	public static final String action_scratchCard			= "scratch_card";
	public static final String action_sendSMS				= "send_sms";
	public static final String action_tickHLR				= "tick_hlr";
	public static final String action_untickHLR				= "untick_hlr";
	public static final String action_suspension			= "suspension";
	public static final String action_thirdPartyRequest		= "thirdPartyRequest";
	public static final String action_addSubscriberPromo	= "add_subscriber_promo";
	public static final String action_removeSubscriberPromo	= "remove_subscriber_promo";
	public static final String action_upload				= "upload";
	public static final String action_uploadNprocess		= "uploadNprocess";
	public static final String action_editTask				= "editTask";
	public static final String action_deleteTask			= "deleteTask";
	public static final String action_checkBulkSubscribersStatus    = "checkBulkSubscribersStatus";
	public static final String action_getCorporateDetails       = "get_corporate_details";
	public static final String action_getBulkTaskSubscriberDetails       = "get_bulk_task_subscriber_details";
	public static final String action_activateAnnouncement      = "activate_announcement";
	public static final String action_deactivateAnnouncement    = "deactivate_announcement";
	public static final String action_updateValidity	  	 	= "update_validity";
	public static final String action_deactivateOffer			= "deactivate_offer";
	public static final String action_changeMsisdn				= "change_msisdn";
	public static final String action_sendChangeMsisdnRequest	= "send_change_msisdn_request";
	public static final String action_deleteConsentRecord	= "send_delete_consent_record";							
	public static final String action_removeTask				= "removeTask";
	public static final String action_recommendation_music		= "recommendation_mucisc";
	public static final String action_re_recommendation_music	= "re_recommendation_mucisc";
	public static final String action_memcache					= "memcache";
	public static final String circle_top_ten					= "circle_top_ten";
	public static final String action_getUserPIN				= "getUserPIN";
	public static final String action_setUserPIN				= "setUserPIN";
	public static final String action_expireUserPIN				= "expireUserPIN";
	public static final String action_upgradeAllSelections		= "upgrade_all_selections";
	public static final String action_upgradeAllDownloads       = "action_upgradeAllDownloads";
	public static final String action_upgradeDownload      		= "action_upgradeDownload";
	public static final String action_deactivatePack			= "deactivate_pack";
	public static final String action_copyContest			    = "copy_contest";
	public static final String action_subscribeUser				= "subscribe_user";
	public static final String action_directCopy				= "direct_copy";
	public static final String action_addGCMRegistration		= "addGCMRegistration";
	public static final String action_removeGCMRegistration		= "removeGCMRegistration";
	public static final String action_getOrSetNotificationStatus= "getOrSetNotificationStatus";

	public static final String action_addMultipleSettings		= "add_multiple_settings";
	public static final String action_addMultipleDownloads		= "add_multiple_downloads";
	public static final String action_deleteMultipleSettings	= "delete_multiple_settings";
	public static final String action_deleteMultipleTones		= "delete_multiple_tones";
    public static final String action_offerFromBI               = "bIOffer";
    public static final String action_getPackageOffer           = "packageOffer";
    //RBT-13415 - Nicaragua Churn Management.
    public static final String action_rejectDelayDct            = "rejectDelayDct";
    public static final String action_getCurrentPlayingSong		= "getCurrentPlayingSong";
    
	//Web Service parameters
	public static final String param_api		= "api";
	public static final String param_ipAddress	= "ipAddress";
	public static final String param_ipAddressConsent	= "ipAddressConsent";
	public static final String param_modeConsent = "modeConsent";
	public static final String param_response	= "response";
    
	public static final String param_isSelConsentIntRequest    = "isSelConsentIntRequest";
	public static final String param_isPreConsentBaseRequest    = "isPreConsentBaseRequest";
	public static final String param_isPreConsentBaseSelRequest = "isPreConsentBaseSelRequest";
	public static final String param_consent                    = "consent";
	public static final String param_errorCode                  = "ErrorCode";
	public static final String param_parameters                 = "parameters";
	public static final String param_action						= "action";
	public static final String param_calledNo					= "calledNo";
	public static final String param_subscriberID				= "subscriberID";
	public static final String param_isPrepaid					= "isPrepaid";
	public static final String param_offSet						= "offSet";
	public static final String param_rowCount					= "rowCount";
	public static final String param_userType					= "userType";
	public static final String param_language					= "language";
	public static final String param_gifterID					= "gifterID";
	public static final String param_subscriptionClass			= "subscriptionClass";
	public static final String param_chargeClass				= "chargeClass";
	public static final String param_useUIChargeClass			= "useUIChargeClass";
	public static final String param_rentalPack					= "rentalPack";
	public static final String param_freePeriod					= "freePeriod";
	public static final String param_rbtType					= "rbtType";
	public static final String param_selectionType				= "selectionType";
	public static final String param_agentId					= "agentId";
	public static final String param_playerStatus				= "playerStatus";
	public static final String param_lastAccessDate				= "lastAccessDate";
	public static final String param_callerID					= "callerID";
	public static final String param_categoryID					= "categoryID";
	public static final String param_clipID						= "clipID";
	
	public static final String param_clipName					= "clipName";
	public static final String param_clipInfo					= "clipInfo";
	public static final String param_rbtFile					= "rbtFile";
	public static final String param_refID						= "refID";
	public static final String param_status						= "status";
	public static final String param_selstatus					= "selstatus";
	public static final String param_fromTime					= "fromTime";
	public static final String param_fromTimeMinutes			= "fromTimeMinutes";
	public static final String param_toTime						= "toTime";
	public static final String param_toTimeMinutes				= "toTimeMinutes";
	public static final String param_profileHours				= "profileHours";
	public static final String param_cricketPack				= "cricketPack";
	public static final String param_giftSentTime				= "giftSentTime";
	public static final String param_referredTime				= "referredTime";
	public static final String param_toneID						= "toneID";
	public static final String param_fromSubscriber				= "fromSubscriber";
	public static final String param_gifteeID					= "gifteeID";
	public static final String param_contentID					= "contentID";
	public static final String param_contentType				= "contentType";
	public static final String param_circleID					= "circleID";
	public static final String param_operatorID					= "operatorID";
	public static final String param_pageNo						= "pageNo";
	public static final String param_oldPassword				= "oldPassword";
	public static final String param_number						= "number";
	public static final String param_age						= "age";
	public static final String param_gender						= "gender";
	public static final String param_scratchCardNo				= "scratchCardNo";
	public static final String param_mmContext					= "mmContext";
	public static final String param_inLoop						= "inLoop";
	public static final String param_mode						= "mode";
	public static final String param_sngId						= "sngId";
	public static final String param_modeInfo					= "modeInfo";
	public static final String param_actMode					= "ACT_MODE";
	public static final String param_chargingModel				= "chargingModel";
	public static final String param_optInOutModel				= "optInOutModel";
	public static final String param_info						= "info";
	public static final String param_interval					= "interval";
	public static final String param_message					= "message";
	public static final String param_startIndex					= "startIndex";
	public static final String param_endIndex					= "endIndex";
	public static final String param_isNewsLetterOn				= "isNewsLetterOn";
	public static final String param_selectionStartTime			= "selectionStartTime";
	public static final String param_selectionEndTime			= "selectionEndTime";
	public static final String param_setTime					= "setTime";
	public static final String param_sentTime					= "sentTime";
	public static final String param_processAllCircles			= "processAllCircles";
	public static final String param_toBeprocessCircles			= "toBeProcessCircles";
	public static final String param_groupID					= "groupID";
	public static final String param_msisdn_operator			= "msisdn_operator";
	public static final String param_groupName					= "groupName";
	public static final String param_predefinedGroupID			= "predefinedGroupID";
	public static final String param_memberID					= "memberID";
	public static final String param_memberName					= "memberName";
	public static final String param_dstGroupID					= "dstGroupID";
	public static final String param_senderID					= "senderID";
	public static final String param_receiverID					= "receiverID";
	public static final String param_smsText					= "smsText";
	public static final String param_smsSent	                = "SMS_SENT";
	public static final String param_checkSubscriptionClass		= "checkSubscriptionClass";
	public static final String param_removeExistingSetting		= "removeExistingSetting";
	public static final String param_ignoreActiveUser			= "ignoreActiveUser";
	public static final String param_isDirectActivation			= "isDirectActivation";
	public static final String param_isDirectDeactivation		= "isDirectDeactivation";
	public static final String param_isCorporateDeactivation	= "isCorporateDeactivation";
	public static final String param_isDeactivateCorporateUser	= "isDeactivateCorporateUser";
	public static final String param_subscriptionPeriod			= "subscriptionPeriod";
	public static final String param_dontSMSInBlackOut			= "dontSMSInBlackOut";
	public static final String param_isPressStarIntroEnabled	= "isPressStarIntroEnabled";
	public static final String param_isPollOn					= "isPollOn";
	public static final String param_isOverlayOn				= "isOverlayOn";
	public static final String param_isUdsOn					= "isUdsOn";
	public static final String param_udsType					= "udsType";
	public static final String param_isBlacklisted				= "isBlacklisted";
	public static final String param_blacklistType				= "blacklistType";
	public static final String param_range						= "range";
	public static final String param_type						= "type";
	public static final String param_clipIds					= "clipIds";
	public static final String param_catId						= "catId";
	public static final String param_title						= "title";
	public static final String param_newCallerID				= "newCallerID";
	public static final String param_newType					= "newType";
	public static final String param_name						= "name";
	public static final String param_value						= "value";
	public static final String param_playDate					= "playDate";
	public static final String param_profile					= "profile";
	public static final String param_startDate					= "startDate";
	public static final String param_endDate					= "endDate";
	public static final String param_userID						= "userID";
	public static final String param_userId						= "userId";
	public static final String param_newUserID					= "newUserID";
	public static final String param_password					= "password";
	public static final String param_encryptPassword			= "encryptPassword";
	public static final String param_userInfo					= "userInfo";
	public static final String param_selectionInfo				= "selectionInfo";
	public static final String param_operatorUserInfo			= "operatorUserInfo";
	public static final String param_siteName					= "siteName";
	public static final String param_sitePrefix					= "sitePrefix";
	public static final String param_siteURL					= "siteURL";
	public static final String param_accessAllowed				= "accessAllowed";
	public static final String param_supportedLanguage			= "supportedLanguage";
	public static final String param_playerURL					= "playerURL";
	public static final String param_playUnchargedFor			= "playUnchargedFor";
	public static final String param_bulkTaskFile				= "bulkTaskFile";
	public static final String param_bulkTaskResultFile			= "bulkTaskResultFile";
	public static final String param_isShuffleSelection			= "isShuffleSelection";
	public static final String param_period						= "period";
	public static final String param_context					= "context";
	public static final String param_prepaidSuccessSms			= "prepaidSuccessSms";
	public static final String param_prepaidFailureSms			= "prepaidFailureSms";
	public static final String param_postpaidSuccessSms			= "postpaidSuccessSms";
	public static final String param_postpaidFailureSms			= "postpaidFailureSms";
	public static final String param_prepaidNEFSuccessSms		= "prepaidNEFSuccessSms";
	public static final String param_prepaidRenewalSuccessSms	= "prepaidRenewalSuccessSms";
	public static final String param_prepaidRenewalFailureSms	= "prepaidRenewalFailureSms";
	public static final String param_postpaidRenewalSuccessSms	= "postpaidRenewalSuccessSms";
	public static final String param_postpaidRenewalFailureSms	= "postpaidRenewalFailureSms";
	public static final String param_count						= "count";
	public static final String param_cosID						= "cosID";
	public static final String param_pack						= "pack";
	public static final String param_retailerID					= "retailerID";
	public static final String param_transID					= "transID";
	public static final String param_duration					= "duration";
	public static final String param_suspend					= "suspend";
	public static final String param_preCharged					= "preCharged";
	public static final String param_freeDays					= "freeDays";
	public static final String param_activatedNow				= "activatedNow";
	public static final String param_activatedPackNow			= "param_activatedPackNow";
	public static final String param_smsID						= "smsID";
	public static final String param_updateSmsID				= "updateSmsID";
	public static final String param_newSubscriberID			= "newSubscriberID";
	public static final String param_isPromoID					= "isPromoID";
	public static final String param_clipPromoID				= "clipPromoID";
	public static final String param_categoryPromoID			= "categoryPromoID";
	public static final String param_clipVcode					= "clipVcode";
	public static final String param_frequency					= "frequency";
	public static final String param_doSubscriberValidation		= "doSubscriberValidation";
	public static final String param_pinID						= "pinID";
	public static final String param_allowPremiumContent		= "allowPremiumContent";
	public static final String param_isSmsAlias					= "isSmsAlias";
	public static final String param_clipSmsAlias				= "clipSmsAlias";
	public static final String param_categorySmsAlias			= "categorySmsAlias";
	public static final String param_isGifterConfRequired		= "isGifterConfRequired";
	public static final String param_isGifteeConfRequired		= "isGifteeConfRequired";
	public static final String param_songAlreadyAdded			= "songAlreadyAdded";
	public static final String param_initClassType				= "initClassType";
	public static final String param_isAllowSmOffer				= "isAllowSmOffer";
	public static final String param_upgradeGraceAndSuspended   = "upgradeGraceAndSuspended";
	public static final String param_suspendedUsersAllowed		= "suspendedUsersAllowed";
	public static final String param_clipIndex					= "clipIndex";
	public static final String param_fileName					= "fileName";
	public static final String param_srvId					    = "srvId";
	public static final String param_SrvClass					= "srvClass";
	public static final String param_makeEntryInDB				= "makeEntryInDB";
	public static final String param_dtmfInputKeys				= "dtmfInputKeys";
	public static final String param_isResetPassword			= "isResetPassword";
	public static final String param_deactivateAllClips			= "deactivateAllClips";
	public static final String param_shuffleChargeClass			= "shuffleChargeClass";
	public static final String param_operatorUserType			= "operatorUserType";
	//RBT-13415 - Nicaragua Churn Management.
	public static final String param_delayDct					= "delayDct";
	//Added for VF Greece, delayed deactivation support based on the UI
	public static final String param_delayDct_UI				= "delayDctUI";
	
	public static final String param_consent_inserted 			= "isConsentInserted";

	public static final String param_redirectionRequired		= "redirectionRequired";
	public static final String param_byPassConsent		        = "byPassConsent";
	
	public static final String param_protocolNo		 			= "protocolNo";
	public static final String param_protocolStaticText		 	= "protocolStaticText";
	public static final String PROTOCOL_NO		 			= "protocolNo";
	
	/* pending confirmations reminder constants */
	public static final String param_pendingConfirmationsReminderData = "pendingConfirmationsReminder";
	public static final String param_remindersLeft = "remindersLeft";
	public static final String param_lastReminderSent = "lastReminderSent";
	public static final String param_smsReceivedTime = "smsReceivedTime";
	public static final String param_reminderText = "reminderText";
	public static final String param_sender = "sender";
	public static final String param_recordsFrom = "records_from";
	public static final String param_numOfRecords = "num_of_records";
	public static final String param_delayInSentTime = "delay_in_senttime";
	public static final String param_deleteLimit = "delete_limit";
	public static final String REMINDERS_LEFT = "reminders_left";
	public static final String LAST_REMINDER_SENT = "last_reminder_sent";
	public static final String SMS_RECEIVED_TIME = "sms_received_time";
	public static final String REMINDER_TEXT = "reminder_text";
	public static final String PENDING_CONFIRMATIONS_REMINDER_DATA = "pending_confirmations_reminder";

	public static final String param_subscriberStatus				= "subscriberStatus";
	public static final String param_subscriberDownloads			= "subscriberDownloads";
	public static final String param_corpID							= "corpID";
	public static final String param_clipDownloadsFromBackEnd		= "clipDownloadsFromBackEnd";
	public static final String param_musicBoxDownloadsFromBackEnd	= "musicBoxDownloadsFromBackEnd";

	public static final String param_userHasMultipleSelections	= "userHasMultipleSelections";
	public static final String param_totalNoOfContents			= "totalNoOfContents";
	public static final String param_subscriber					= "subscriber";
	public static final String param_subscriber_consent			= "consentSubscriber";
	public static final String param_upgrade_consent_flow		= "upgradeConsentFlow";
	public static final String param_consent_subscriptionClass	= "consent_subscriptionClass";
	
	public static final String param_settings					= "settings";
	public static final String param_downloads					= "downloads";
	
	public static final String param_viralData					= "viralData";
	public static final String param_transData					= "transData";
	public static final String param_rbtSupportData				= "rbtSupportData";
	public static final String param_serviceNextBillingDateMap	= "serviceNextBillingDateMap";
	public static final String param_superSongsMap				= "superSongsMap";
	public static final String param_subscriberPromo			= "subscriberPromo";
	public static final String param_pickOfTheDays				= "pickOfTheDays";
	public static final String param_subscriberDetail			= "subscriberDetail";

	public static final String param_offerType				= "offerType";
	public static final String param_subscriberType			= "subscriberType";
	public static final String param_offerID				= "offerID";
	public static final String param_packOfferID			= "packOfferID";
	public static final String param_subscriptionOfferID	= "subscriptionOfferID";
	public static final String param_extraInfo				= "extraInfo";
	public static final String param_subscriberEndDate		= "subscriberEndDate";
	public static final String param_packCosId				= "packCosId";
	public static final String param_internalRefId				= "internalRefId";
	public static final String param_upgradeFailuer_OfferId		= "upgradeFailure_OfferId";
	// Lock/Unlock feature Vodafone India
	public static final String param_userLocked				= "userLocked";
	
	public static final String param_churnOfferFromBI = "churnOfferFromBI";
	public static final String param_churnOffer = "churnOffer";
	public static final String offerType      = "OFFER_TYPE";

	// BulkProcess Task Parameters
	public static final String param_task						= "task";
	public static final String param_taskID						= "taskID";
	public static final String param_taskName					= "taskName";
	public static final String param_taskType					= "taskType";
	public static final String param_activatedBy				= "activatedBy";
	public static final String param_actInfo					= "actInfo";
	public static final String param_uploadTime					= "uploadTime";
	public static final String param_taskStatus					= "taskStatus";
	public static final String param_taskInfo					= "taskInfo";
	public static final String param_taskMode					= "taskMode";
	public static final String param_reason						= "reason";
	public static final String param_editNumbersInTask			= "editNumbersInTask";
	public static final String param_onlyResponse				= "onlyResponse";
	public static final String param_useSameResForConsent		= "useSameResForConsent";
	public static final String param_isPostMethod				= "isPostMethod";

	// Content Parameters
	public static final String param_nameWavFile	= "nameWavFile";
	public static final String param_previewWavFile	= "previewWavFile";
	public static final String param_rbtWavFile		= "rbtWavFile";
	public static final String param_grammar		= "grammar";
	public static final String param_smsAlias		= "smsAlias";
	public static final String param_promoID		= "promoID";
	public static final String param_classType		= "classType";
	public static final String param_startTime		= "startTime";
	public static final String param_endTime		= "endTime";
	public static final String param_album			= "album";
	public static final String param_demoWavFile	= "demoWavFile";
	public static final String param_artist			= "artist";
	//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
	public static final String param_default_music	= "defaultMusic";
	public static final String param_genre			= "genre";
	public static final String param_price			= "price";
	public static final String param_showNextBillDate	= "nextBillDate";
	
	public static final String LOOP_STATUS = "loop_status";
	public static final String IS_CONSENT_INSERTED = "consentInserted";
	public static final String IS_CURRENT_SETTING = "isCurrentSetting"; //RBT-12247
	
	//Search parameters
	public static final String param_song			= "song";
	public static final String param_searchText		= "searchText";
	public static final String param_promoCode		= "promoCode";
	public static final String param_alias			= "alias";
	public static final String param_maxResults		= "maxResult";
	public static final String PROMO_CODE		    = "promo_code";
	public static final String class_type		    = "class_type";
	public static final String CONTENT_VALIDITY     = "content_validity";
	public static final String param_searchType     = "searchtype";
	public static final String param_sendsms = "sendsms";
	
	public static final String param_clearTNBFlag 	= "clearTNBFlag";
	public static final String param_modifiedSubscriber = "modifiedSubscriber";
	
	public static final int CAMPAIGN_DELETE 			= 1;
	public static final int CAMPAIGN_UPDATED 			= 2;
	public static final int CAMPAIGN_ADD_SUBSCRIBERS	= 4;
	public static final int CAMPAIGN_DELETE_SUBSCRIBERS	= 8;

	public static final String param_requestFromSelection = "fromSelection";
	public static final String param_old_offerid = "old_offerid";
	public static final String param_fromBulkTask = "fromBulkTask";
	public static final String param_newUser = "newUser";
	public static final String param_browsingLanguage = "browsingLanguage";
	public static final String param_limitedPackRequest = "limitedPackRequest";

	public static final String ANNOUNCEMENTS_NOT_SUPPORTED			= "announcements_not_supported";
	public static final String param_TOTIMEMINUTES				= "TOTIMEMINUTES";
	public static final String param_FROMTIMEMINUTES			= "FROMTIMEMINUTES";

	public static final String param_ScratchCard				= "ScratchCard";
	public static final String SCRATCHCARD						= "scratchcard";
	public static final String api_Scratchcard					= "ScratchCard";
	public static final String param_scratchNo					= "scratchNo";
	public static final String param_bIOffer                = "bIOffer";
	public static final String param_preConsent                = "preConsent";
	
	public static final String param_isUnsubDelayDctReq = "isUnsubDelayDctReq";
	public static final String param_isDelayDeactForUpgardation = "isDelayDeactForUpgardation";
	// QRCode Parameters
	public static final String param_data			= "data";
	public static final String param_QRCodeType		= "type";
	public static final String param_QRCodeNumber	= "number";
	public static final String param_sms			= "sms";
	public static final String param_msisdn			= "msisdn";
	public static final String param_text			= "text";
	public static final String param_url			= "url";
	public static final String param_phoneNumber	= "phone_number";
	
	// Session object parameters
	public static final String session_clip			= "session_clip";
	
	//Added for Reliance ARBT
	public static final String param_subClass = "subclass";
	public static final String param_subtype = "subtype";
	public static final String param_contentid = "contentid";
	public static final String param_categoryid = "categoryid";
	public static final String param_inloop = "inloop";
	public static final String param_callerid = "callerid";
	public static final String param_isactivate = "isactivate";
	public static final String param_chargeclass = "chargeclass";
	public static final String param_clip = "clip";
	public static final String param_category = "category";
	public static final String param_downloadRefID = "downloadRefID";
	public static final String param_selectionRefID = "selectionRefID";

	//BI Download History Constants
	public static final String BI_DOWNLOAD_HISTORY	= "bi_download_history";

	//super songs feature
	public static final String param_recommendationType = "recType";
	
	// Vodafone Retailer selections request parameters
	public static final String param_RET_MSISDN 	= "RET_MSISDN";
	public static final String param_CUS_MSISDN 	= "CUS_MSISDN";
	public static final String param_TXNID 			= "TXNID";
	public static final String param_VAS_CONTENT_CD = "VAS_CONTENT_CD";
	public static final String param_MODE 			= "MODE";
	public static final String param_REQ_TS 		= "REQ_TS";
	public static final String param_VAS_SRV_CD 	= "VAS_SRV_CD";
	public static final String param_TXN_TYPE 		= "TXN_TYPE";

	// Vodafone Retailer
	public static final String param_contentId	= "contentId";	
	
	// CopyConsent constant
	public static final String param_submitPacknotChosen ="submitPacknotChosen";

	// Clip Rating
	public static final String action_getClipRating	= "getClipRating";
	public static final String action_rateClip		= "rateClip";
	public static final String action_likeClip		= "likeClip";
	public static final String action_dislikeClip	= "dislikeClip";
	public static final String param_rating			= "rating";
	public static final String action_getMobileAppRegistration	= "getMobileAppRegistration";

	public static final String CLIP_RATINGS		= "clip_ratings";
	public static final String CLIP_RATING		= "clip_rating";
	public static final String NO_OF_VOTES		= "no_of_votes";
	public static final String SUM_OF_RATINGS	= "sum_of_ratings";
	public static final String LIKE_VOTES		= "like_votes";
	public static final String DISLIKE_VOTES	= "dislike_votes";
	
	// Registering UID for Mobile Web
	public static final String param_uid	= "uid";

	// NextChargeClass constants
	public static final String action_getNextChargeClass = "getNextChargeClass";
	public static final String action_addTransData		 = "addTransData";
	
	// Esia Delayed Deactivation Feature
	public static final String param_forceDeactivation 	= "forceDeactivation";
	public static final String NOT_DELAYED_DEACTIVATION	= "not_delayed_deactivation";
	public static final String DELAYED_DEACT_SUCCESS	= "delayed_deact_success";
	public static final String DELAYED_DEACT			= "delayed_deact";
	
	//Subscriber packs constants
	public static final String NO_OF_PACKS			= "no_of_packs";
	public static final String NO_OF_ACTIVE_PACKS	= "no_of_active_packs";
	
	//RbtSupport constants
	public static final String KEY_PRESSED = "key_pressed";
	
	public static final String srvkey = "srvkey";
	
	public static final String REGISTERID	= "REGISTERID";
	public static final String REGISTERIDS	= "REGISTERIDS";
	public static final String NOTIFICATION_SMS = "NOTIFICATION_SMS";
	public static final String param_os_Type = "os_type";
	public static final String IS_COMBO_REQUEST = "is_combo_request";
	//CG Integration Flow - Jira -12806
	public static final String REQUEST_TIME_STAMP = "requesttimestamp";	 
	public static final String DESCRIPTION = "description";	
	public static final String ACT_SEL_CONSENT_PENDING = "act_sel_consent_pending";
	/*Content info to get clip or category from webservice */
	public static final String BYCLIPID	 = "BYCLIPID";
	public static final String BYCLIPPROMOID = "BYCLIPPROMOID";
	public static final String BYCLIPALBUM = "BYCLIPALBUM";
	public static final String BYCLIPALIAS = "BYCLIPALIAS";
	public static final String BYCLIPWAVFILE = "BYCLIPWAVFILE";
	public static final String BYCLIPS = "BYCLIPS";
	public static final String BYCLIPSINCATEGORY = "BYCLIPSINCATEGORY";
	public static final String BYACTIVECLIPSINCATEGORY = "BYACTIVECLIPSINCATEGORY";
	public static final String BYCATID	 = "BYCATID";
	public static final String BYCATMMNUM = "BYCATMMNUM";
	public static final String BYCATNAME = "BYCATNAME";
	public static final String BYCATPROMOID = "BYCATPROMOID";
	public static final String BYCATALIAS = "BYCATALIAS";
	public static final String BYCATPROMOIDWITHCIRCLE = "BYCATPROMOIDWITHCIRCLE";
	public static final String BYCATIDS = "BYCATIDS";
	public static final String BYCATINCIRCLE = "BYCATINCIRCLE";
	public static final String BYACTCATINCIRCLELANG = "BYACTCATINCIRCLELANG";
	public static final String BYACTCATINCIRCLE = "BYACTCATINCIRCLE";
	public static final String BYCATTYPE = "BYCATTYPE";
	public static final String BYCATARRPROMOID = "BYCATARRPROMOID";
	//JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs.
	public static final String param_REQUEST = "REQUEST";
	/*Idea 121 Consent*/
	public static final String param_linkedRefId = "linkedRefId";
	public static final String param_generateRefId = "generateLinkedRefId";

	public static final String BASE_OFFER_NOT_AVAILABLE = "BASE_OFFER_NOT_AVAILABLE";
	
	/*Ad2C Constants*/
	public static final String param_ad2cUrl = "ad2cUrl";
	public static final String PACK_OFFER_NOT_AVAILABLE = "PACK_OFFER_NOT_AVAILABLE";
	public static final String param_reqType = "reqType";
	public static final String param_songname = "songname";
	public static final String param_slice_duration = "slice_duration";
	
	/*JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs.*/
	public static final String user_access_information = "NO ACCESS RIGHTS";
	
	/*Airtel Gift Flow*/
	public static final String param_IsGifteeActive = "IsGifteeActive";
	public static final String param_GifteeHub = "GifteeHub";
	public static final String param_isConsentFlow	= "isConsentFlow";
	public static final String param_isGifterCharged = "isGifterCharged";
	
	//RBT-12419
	public static final String CLIP_EXPIRED_DOWNLOAD_DELETED = "clip_expired_download_deleted";
	public static final String CATEGORY_EXPIRED_DOWNLOAD_DELETED = "category_expired_download_deleted";
	
	public static final String PREMIUM_SELECTION_CHARGE_CLASS = "PREMIUM_SELECTION_CHARGE_CLASS";
	//JIRA-ID: RBT-13626:
	public static final String PREMIUM_SELECTION_CHARGE_CLASS_MAP = "PREMIUM_SELECTION_CHARGE_CLASS_MAP";
	
	public static final String PREMIUM_SELECTION_IS_CHARGE_CLASS_FROM_CLIP = "PREMIUM_SELECTION_IS_CHARGE_CLASS_FROM_CLIP";
	
	public static final String action_rrbt_consent_suspension_deactivate	= "rrbt_consent_suspension_deactivate";
	public static final String param_allowDirectPremiumSelection = "allowDirectPremiumSelection";
	
	public static final String param_chrgDetailsReq = "chrgDetailsReq";
	public static final String RBT_INFO_DEFAULT_SUBSCRIPTION_CLASS = "RBT_INFO_DEFAULT_SUBSCRIPTION_CLASS";
	public static final String RBT_INFO_DEFAULT_CHARGE_CLASS = "RBT_INFO_DEFAULT_CHARGE_CLASS";
	
	public static final String param_appName = "appName";
	public static final String param_format ="format";
	public static final String TNB_SONG_SELECTON_NOT_ALLOWED= "TNB_SONG_SELECTON_NOT_ALLOWED";
	
	public static final String UPGRADE_NOT_ALLOWED ="UPGRADE_NOT_ALLOWED";
	
	public static final String action_Logging_CDR	= "loggingCDR";
	
	public static final String action_Protocol_Numb = "getProtocolNumber";
	
	public static final String CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS = "CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS";
	public static final String CURRENT_PLAYING_SONG_PORT_TO_LISTEN_TO = "CURRENT_PLAYING_SONG_PORT_TO_LISTEN_TO";
	public static final String START_GET_CURRENT_SONG_DAEMON = "START_GET_CURRENT_SONG_DAEMON";
	public static final String APP_NAMES_FOR_REAL_TP_SUPPORT = "APP_NAMES_FOR_REAL_TP_SUPPORT";
	
	//RBT-14052: RBT Double Opt-In
	public static final int consent_pending_status = 5;
	public static final int sel_combo_consent_pending_status = 6;
	public static final String DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS = "DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS";
	public static final String m_DoubleOptInActivationNotficationSms = "Reply with %s to confirm activation of your subscription";
	public static final String DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS = "DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS";
	public static final String m_DoubleOptInSelectionNotficationSms = "Reply with %s to confirm activation of your song selection";
	public static final String DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS = "DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS";
	public static final String m_DoubleOptInComboNotficationSms = "Reply with %s to confirm activation of your base and song selection";
	public static final String DOUBLE_OPT_IN_MODES_FOR_NO_NOTIFICATION_SMS = "DOUBLE_OPT_IN_MODES_FOR_NO_NOTIFICATION_SMS"; 
	public static final String MI_PLAYLIST = "MiPlaylist";
	
	//RBT-14835 Blocking PPL content for specific service
	public static final String LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED  = "LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED";	//response from processor when accessing blocked content type
	public static final String PREFIX_PRIMIUM_CONTENT_NOTALLOWED_CONFIG  = "LITE_USER_PREMIUM_CONTENT_NOT_ALLOWED_FOR_";	
	public static final String CORPORATE_TUNES = "Corporate Tune";
	public static final String NAME_TUNES = "name_tunes";
	public static final String WAV_FILE = "wav_file";
	public static final String param_callType = "callType";
	public static final String param_pageSize = "pageSize";
	public static final String param_restRequest = "restRequest";
	
	//RBT-14491
	public static final String event_type = "event_type";
	public static final String plan_id = "plan_id";
	public static final String consent_class_type = "consent_class_type";
	public static final String consent_id = "consent_id";

	//RBT-14652: RBT Double Opt-In SAT request
	public static final int sat_consent_pending_status = 0;
	public static final int sat_sel_combo_consent_pending_status = 1;
	public static final String SUBSCRIBER_TYPE = "subscriber_type";
	public static final String param_dtocRequest = "dtocRequest";
	public static final String USE_OPT_IN_SMS_NOTIFICATION = "USE_OPT_IN_SMS_NOTIFICATION";


	//RBT2.0 UDP
	public static final String param_udpId = "udpId";
	public static final String ACTION_SET_UDP = "setUdp";
	public static final String ACTION_DEACT_UDP = "deactUdp";
	public static final String SKIP_CONTENT_CHECK = "skipContentCheck";
	public static final String param_selDirectActivation = "selDirectActivation";
	public static final String param_deselectedBy = "deselected_by";
	
	//Subscription service
	public static final String PRISM_SERVER_ERROR 		= "prism_server_error";
	public static final String ALREADY_PROCESSED 		= "already_processed";

	public static final String param_comviva_profile_check_url = "comviva.profile.check.url";
	public static final String param_comviva_consent_url = "airtel.comviva.consent.url";
	public static final String param_comviva_delete_setting_url = "comviva.delete.setting.url";
	public static final String param_comviva_special_delete_setting_url = "comviva.delete.special.setting.url";
	public static final String param_comviva_get_selections_url = "comviva.get.selections.url";
	public static final String action_getNextServiceCharge = "getNextServiceCharge";
	public static final String NEXTSERVICE_CHARGE = "nextservice_class";
	public static final String NEWCHARGE_CLASS = "newcharge_class";
	public static final String NEWSUBSCRIPTION_CLASS= "newsubscription_class";
	public static final String SERVICE_KEY = "service_key";
	public static final String IS_RENEWAL = "is_renewal";
	public static final String RENEWAL_VALIDITY = "renewal_validity";
	
	public static final String OPERATOR_USER_TYPE = "operator_user_type";
	public static final String IS_SERVICE_KEY_BLOCKED_FOR_SELECTION = "is_service_key_blocked_for_selection";
	
	//Added for VDE-2730
	public static final String param_isPressStarIntroSuspendEnabled	= "isPressStarIntroSuspendEnabled";
	

	//Added for inline provisioning
	public static final String param_provisioning = "provisioning";
	public static final String param_provisioning_type = "provisioningType";
//	public static final String QUEUE_PROVISIONING = "Q";
//	public static final String RT_PROVISIONING = "RT";
//	public static final String RTNP_PROVISIONING = "RTNP";
	public static final String MAX_RETRIES_REACHED = "Max. retries reached";
	public static final String RETRIAL = "Going for retrial";
	public static final String CONTENT_EXPIRED = "Content expired";
	public static final String SUB_BLOCKED = "Subscriber is blocked";
	public static final String NULL_REFID = "Ref id is null";
	public static final String INLINE_PARAMETERS_INVALID = "Inline Parameters are null";
	public static final String SM_FAILURE = "SM URL failure";
	public static final String BASE_DEACTIVATED = "Base is deactivated";
	public static final String SUB_ACT_PENDING = "Subscriber is in act pending state";
	public static final String REQ_BEAN_NULL = "Request Bean is null";
	public static final String NO_CONSENT_MODE = "Get Consent is not set for this mode";
	public static final String WDS_INVALID = "Wds Response is null";
	public static final String CONSENT_STATUS_INVALID = "Consent status is not correct for processing consent";
	public static final String SPRING_MESSAGE_SENDING_FAILURE = "Spring message sending failed...";
	public static final String CONSENT_PROCESSING_RECORD_FAILURE = "Failed while processing CG record into selection";
	public static final String INLINE_PARAMETERS = "INLINE_PROVISIONING";
	public static final String TYPE_CALLBACK = "Callback";
	public static final String TYPE_REALTIME = "RealTime";
	public static final String PROVISIONING_CGR = "CGR";
	public static final String PROVISIONING_CGC = "CGC";
	public static final String PROVISIONING_SMR = "SMR";
	public static final String PROVISIONING_SMC = "SMC";
	public static final String PROVISIONING_CONSENT_TO_SELECTION = "CONSENTTOSEL";
	public static final String PROVISIONING_TP = "TP";
	public static final String PROVISIONING_TPACT = "TPACT";
	public static final String PROVISIONING_TPDCT = "TPDCT";
	public static final String DOWNLOAD_MONTHLY_LIMIT_REACHED = "DOWNLOAD_MONTHLY_LIMIT_REACHED" ;

	
	
}