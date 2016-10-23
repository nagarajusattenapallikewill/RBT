package com.onmobile.apps.ringbacktones.common;

public interface iRBTConstant
{	
	public static final String APP_ID = "com.onmobile.apps.ringbacktones.voice.SORBTManager";
	public static final String MAIN_MENU_APP_ID = "com.onmobile.apps.dynamicentrylogic.vui.SOEntryLogic";
	public static final String RBT_RESOURCE_FILE = "resources/RBTResource";
	/*  parameter name in rbt_parameter table corresponding to column "PARAM" to denote max allowed selection per caller number for a subscriber_id*/
	public static final String MAX_ALLOWED_SELECTION="MAX_ALLOWED_SELECTION";
	public static final String SAME_SONG_REACTIVATION_NOT_ALLOWED="SAME_SONG_REACTIVATION_NOT_ALLOWED";
	public static final String IS_IMAGE_URL = "IS_IMAGE_URL";
	public static final int max_selection=3; 
	/* parameter name in rbt_parameter table corresponding to column "TYPE" */
	public static final String COMMON="COMMON";
	public static final String CONSENT="CONSENT";
	public static final String GATHERER="GATHERER";
	public static final String DAEMON="DAEMON";
	public static final String SMS ="SMS";
	public static final String RETAILER ="RETAILER";
	public static final String WEBSERVICE = "WEBSERVICE";
	public static final String USSD ="USSD";
	public static final String SMSREDIRECT ="SMSREDIRECT";
	public static final String PROVISIONING ="PROVISIONING";
	public static final String SUPERSONGS = "SUPERSONGS";
	public static final String RESONGS = "RESONGS";
	public static final String TATADAEMON ="TATADAEMON";
	public static final String MONITOR = "MONITOR";
	public static final String PROMOTION = "PROMOTION";
	public static final String SRBT = "SRBT";
	public static final String BI = "BI";
	public static final String REPORTER = "REPORTER";
	public static final String CONTEST = "CONTEST";
	public static final String DOUBLE_CONFIRMATION = "DOUBLE_CONFIRMATION";
	public static final String IS_SHORTEN_CONSENT_TRANID = "IS_SHORTEN_CONSENT_TRANID";
	public static final String SUSPEND_INTRO_PRE_PROMPT_FLAG_IN_CALLBACK = "SUSPEND_INTRO_PRE_PROMPT_FLAG_IN_CALLBACK";
	
	public static final String ALLOW_SELECTION_FOR_SUSPENDED_USER="ALLOW_SELECTION_FOR_SUSPENDED_USER";
	//CG Integration Flow - Jira -12806
	public static final String CPID_FORMAT_FOR_BSNL = "CPID_FORMAT_FOR_BSNL";
	public static final String LIMIT_TO_FETCH_LOGIN_USER_DATA = "1000";
	public static final String CPID_FORMAT = "CPID_FORMAT";	
	
	public static final String INFO = "INFO";

	
	/* SRBT InfoXML attributes */
	public static final String SRBT_COPY_START_TOKEN = "|CP:";
	public static final String SRBT_COPY_END_TOKEN = ":CP|";
	public static final String SRBT_GIFT_TOKEN = "GIFTER";
	public static final String SRBT_COPY ="COPY";
	public static final String SRBT_GIFT = "GIFT";
	public static final String SRBT_OMMSG = "OM_MSG";
	public static final String SRBT_OPRTMSG = "OPRT_MSG";
	public static final String SRBT_PROMOMSG = "PROMO_MSG";
	public static final String SRBT_USERMSG = "USER_MSG";
	public static final String SRBT_USER_SQN_ID = "USER_SQN_ID";
	public static final String SRBT_INFO_ATTR_CONFIG = "COPY,GIFT,OM_MSG,OPRT_MSG,PROMO_MSG";
	public static final String SRBT_PARAM_NAME_SEPERATOR = "_";
	public static final String YES = "TRUE";
	public static final String NO = "FALSE";
	public static final String SRBT_DOWNLOAD_ACTION = "download";
	public static final String SRBT_SELECTION_ACTION = "selection";
	public static final String SRBT_COPY_ACTION = "copy";
	public static final String SRBT_GIFT_ACTION = "gift";
	public static final String SRBT_SUBSCRIBER_ACTION = "subscriber";
	public static final String SRBT_ACT_SUB_ACTION = "act";
	public static final String SRBT_DEACT_SUB_ACTION = "deact";
	public static final String SRBT_COPY_SOURCE_USER_ID = "SRC_USER_ID";
	public static final String PUBLISH_CALLER_NAME = "PUBLISH_CALLER_NAME";
	public static final String PUBLISH_CALLER_MSISDN = "PUBLISH_CALLER_MSISDN";
	public static final String ALLOW_PUBLISH = "ALLOW_PUBLISH";
	public static final String MESSAGE_ID = "MESSAGE_ID";
	public static final String OLD_SUBSCRIBER_ID = "OLD_SUBSCRIBER_ID";
	
	

	public static final String MYSQL="MYSQL";
	public static final String SAPDB="SAPDB";

	/*CallState variables*/
	public static final String COS_DETAILS="COS_DETAILS";
	public static final String RBT_ENTRYLOGIC = "RBT_ENTRYLOGIC";
	public static final String RBT_PROMOTION = "RBT_PROMOTION";
	public static final String RBT_ACTIVATION = "RBT_ACTIVATION";
	public static final String RBT_PLAY_PROMPT = "RBT_PLAY_PROMPT";
	public static final String SUB_LANG_KEY = "SUB_LANG_KEY";
	public static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	public static final String MM_CONTEXT_DATA = "MM_CONTEXT_DATA";
	public static final String MM_CONTEXT_KEY = "MM_CONTEXT_KEY";
	public static final String NEXT_STATE_KEY = "NEXT_STATE_KEY";
	public static final String ALL_CATEGORIES_KEY = "ALL_CATEGORIES_KEY";
	public static final String ACTIVE_CATEGORIES_KEY = "ACTIVE_CATEGORIES_KEY";
	public static final String NEXT_CATEGORY_KEY = "NEXT_CATEGORY_KEY";
	public static final String SUBSCRIBER_STATUS_KEY = "SUBSCRIBER_STATUS_KEY";
	public static final String CATEGORY_OBJ_KEY	= "CATEGORY_OBJ_KEY";
	public static final String SUB_CATEGORIES_KEY = "SUB_CATEGORIES_KEY";
	public static final String ACTIVE_CLIPS_KEY = "ACTIVE_CLIPS_KEY";
	public static final String ALL_CLIPS_KEY = "ALL_CLIPS_KEY";
	public static final String ACTIVE_BOUQUET_KEY = "ACTIVE_BOUQUET_KEY";
	public static final String CLIP_OBJ_KEY = "CLIP_OBJ_KEY";
	public static final String IS_FREE_CLIP = "IS_FREE_CLIP";
	public static final String NEXT_CLIP_KEY = "NEXT_CLIP_KEY";
	public static final String PLAY_MORE_KEY = "PLAY_MORE_KEY";
	public static final String PREVIOUS_STATE_KEY = "PREVIOUS_STATE_KEY";
	public static final String SUB_MSG_KEY = "SUB_MSG_KEY";
	public static final String RECORDING_FAILED_KEY = "RECORDING_FAILED_KEY";
	public static final String TELEPHONE_NUMBER = "TELEPHONE_NUMBER";
	public static final String SET_FOR_ALL = "SET_FOR_ALL";
	public static final String FINAL_STATE_KEY = "FINAL_STATE_KEY";
	public static final String MULTIPLE_TELEPHONE_NUMBERS = "MULTIPLE_TELEPHONE_NUMBERS";
	public static final String NO_OF_ATTEMPTS = "NO_OF_ATTEMPTS";
	public static final String NEXT_SELECTION_KEY = "NEXT_SELECTION_KEY";
	public static final String ACTIVE_SUBSCRIBER_STATUS_KEY = "ACTIVE_SUBSCRIBER_STATUS_KEY";
	public static final String SUBSCRIBER_TYPE = "SUBSCRIBER_TYPE";
	public static final String HYBRID_SUBSCRIBER_TYPE = "SUBSCRIBER_TYPE=\"H\"";
	public static final String ALLOW_NON_SUBSCRIBER_ACCESS = "ALLOW_NON_SUBSCRIBER_ACCESS";
	public static final String RBT_SELECTION = "RBT_SELECTION";
	public static final String CALLED_NUMBER = "CALLED_NUMBER";
	public static final String ADVANCE_RENTAL_SUB_CLASS = "ADVANCE_RENTAL_SUB_CLASS"; 
    public static final String ADVANCE_RENTAL_VALUES="ADVANCE_RENTAL_VALLUES"; 
	public static final String RBT_FEED = "RBT_FEED";
	public static final String RBT_FEED_UNSUBSCRIBE = "RBT_FEED_UNSUBSCRIBE";
	public static final String PROFILE_LANGUAGE_KEY = "PROFILE_LANGUAGE_KEY";
	public static final String PROFILE_HOUR_KEY = "PROFILE_HOUR_KEY";
	public static final String NO_OF_CHANCES = "NO_OF_CHANCES";
	public static final String SUBSCRIPTION_CLASS = "SUBSCRIPTION_CLASS";
	public static final String NO_OF_TRIALS = "NO_OF_TRIALS";
	public static final String GIFTEE_NUMBER = "GIFTEE_NUMBER";
	public static final String MULTIPLE_GIFTEE_NUMBERS = "MULTIPLE_GIFTEE_NUMBERS";
	public static final String GO_BACK_KEY = "GO_BACK_KEY";
	/*time of the day changes by Gautam*/
	public static final String NO_OF_TIMES = "NO_OF_TIMES";
	public static final String TIME = "TIME";
	public static final String STATUS = "STATUS";
	public static final String CHANGE = "CHANGE";
	/*weekly/monthly package changes by Gautam*/
	public static final String CHARGING_MODEL = "CHARGING_MODEL";
    public static final String PREVIOUS_CLIP_KEY = "PREVIOUS_CLIP_KEY";
    //added by sreekar for Non-subscriber MM number feature
    public static final String NON_SUBSCRIBER_CATEGORY = "NON_SUBSCRIBER_CATEGORY";
    public static final String START_ACCESS_SECONDS = "START_ACCESS_SECONDS";
    public static final String TIMER_THREAD_OBJECT = "TIMER_THREAD_OBJECT";
    //added by sreekar for RecordKaraoke feature
    public static final String KARAOKE_ORG_TRACK_KEY = "KARAOKE_ORG_TRACK_KEY";
    public static final String IS_KARAOKE_KEY = "IS_KARAOKE_KEY";
    
//  added by senthilraja for UGC feature
    public static final String UGC_OPT_MODEL = "UGC_OPT_MODEL";
    public static final String UGC_SUBSCRIBER_CALLBACK = "UGC_SUBSCRIBER_CALLBACK";
    
    public static final String RETAILER_STRING = "RET"; // not used anymore
    public static final String RETAILER_TYPE_1 = "RET";
    public static final String RETAILER_TYPE_2 = "RET2";
	
	/*Added by Abhinav for LOOP feature*/
	public static final String LOOP_STATUS="LOOP_STATUS";
	public static final String RBT_UGC_CATEGORY="RBT_UGC_CATEGORY";
	
	// Added by Sreekar for TATA Subscriber Class feature
	public static final String SUBSCRIBER_CLASS = "SUBSCRIBER_CLASS";
	
	/*State keys for SORBTManager*/
	public static final String ENTRY_KEY = "ENTRY_KEY";
	public static final String GET_LANGUAGE_KEY = "GET_LANGUAGE_KEY";
	public static final String CONFIRM_CONTINUITY_KEY = "CONFIRM_CONTINUITY_KEY";
	public static final String GET_CATEGORY_KEY = "GET_CATEGORY_KEY";
	public static final String GET_SUB_CATEGORY_KEY = "GET_SUB_CATEGORY_KEY";
	public static final String GET_BOUQUET_KEY = "GET_BOUQUET_KEY";
	public static final String BROWSE_CLIPS_KEY = "BROWSE_CLIPS_KEY";
	public static final String LIST_CLIPS_KEY = "LIST_CLIPS_KEY";
	public static final String RECORD_MESSAGE_KEY = "RECORD_MESSAGE_KEY";
	public static final String PREVIEW_MESSAGE_KEY = "PREVIEW_MESSAGE_KEY";
	public static final String SET_SELECTION_KEY = "SET_SELECTION_KEY";
	public static final String GET_MOBILE_NUMBER_KEY = "GET_MOBILE_NUMBER_KEY";
	public static final String CONFIRM_MOBILE_NUMBER_KEY = "CONFIRM_MOBILE_NUMBER_KEY";
	public static final String SET_DATA_KEY = "SET_DATA_KEY";
	public static final String GET_NEXT_MOBILE_NUMBER_KEY = "GET_NEXT_MOBILE_NUMBER_KEY";
	public static final String GET_CALLER_NUMBER_KEY = "GET_CALLER_NUMBER_KEY";
	public static final String PLAY_RBT_KEY = "PLAY_RBT_KEY";
	public static final String SET_RBT_KEY = "SET_RBT_KEY";
	public static final String BROWSE_RBT_KEY = "BROWSE_RBT_KEY";
	public static final String GET_MANAGE_OPTION_KEY = "GET_MANAGE_OPTION_KEY";
	public static final String COPY_SELECTION_KEY = "COPY_SELECTION_KEY";
	public static final String UNSUBSCRIBE_RBT_KEY = "UNSUBSCRIBE_RBT_KEY";
	public static final String PICK_OF_THE_DAY_KEY = "PICK_OF_THE_DAY_KEY";
	public static final String CONFIRM_FEED_KEY = "CONFIRM_FEED_KEY";
	public static final String GET_TIME_KEY = "GET_TIME_KEY";
	public static final String GIFT_SELECTION_KEY = "GIFT_SELECTION_KEY";
	public static final String CONFIRM_GIFTING_KEY = "CONFIRM_GIFTING_KEY";
	public static final String GET_GIFT_NUMBER_KEY = "GET_GIFT_NUMBER_KEY";
	public static final String CONFIRM_GIFT_NUMBER_KEY = "CONFIRM_GIFT_NUMBER_KEY";
	public static final String GIFT_DATA_KEY = "GIFT_DATA_KEY";
	public static final String GET_NEXT_GIFT_NUMBER_KEY = "GET_NEXT_GIFT_NUMBER_KEY";
	/*time of the day changes by Gautam*/
	public static final String GET_TIME_OF_THE_DAY_KEY = "GET_TIME_OF_THE_DAY_KEY";
	public static final String CONFIRM_TIME_OF_THE_DAY_KEY = "CONFIRM_TIME_OF_THE_DAY_KEY";
	public static final String CONFIRM_TIME_OPTION_KEY = "CONFIRM_TIME_OPTION_KEY";
	/*Weekly/Monthly Package changes by Gautam*/
	public static final String GET_CHARGING_MODEL_KEY = "GET_CHARGING_MODEL_KEY";
	public static final String CONFIRM_CHARGING_MODEL_KEY = "CONFIRM_CHARGING_MODEL__KEY";
	//public static final String PO_CONFIRM_CONTINUITY_KEY="PO_CONFIRM_CONTINUITY_KEY";
    public static final String DTMF_BROWSE_CLIPS_KEY = "DTMF_BROWSE_CLIPS_KEY";
	public static final String PLAY_GIFT_KEY = "PLAY_GIFT_KEY";
	/*added by Sreekar for Karaoke feature*/
	public static final String BROWSE_KARAOKE_KEY = "BROWSE_KARAOKE_KEY";
	public static final String KARAOKE_DEMO_KEY = "KARAOKE_DEMO_KEY";
	public static final String RECORD_KARAOKE_KEY = "RECORD_KARAOKE_KEY";
	public static final String PREVIEW_RECORDED_KARAOKE_KEY = "PREVIEW_RECORDED_KARAOKE_KEY";
	/*Added by Abhinav for LOOP feature*/
	public static final String PLAY_PASSWORD="PLAY_PASSWORD";
    public static final String ASK_LOOP_OVERWRITE="ASK_LOOP_OVERWRITE";
    public static final String NEW_BOOKMARKS_KEY = "NEW_BOOKMARKS_KEY";
    public static final String BOOKMARKS_CATEGORY_KEY = "BOOKMARKS_CATEGORY_KEY";
    public static final String BOOKMARKS_KEY = "BOOKMARKS_KEY";
	
	/*Slots and rules*/
	public static final String GET_LANGUAGE_RULENAME = "Language";
	public static final String SLOT_LANGUAGE = "slot_language";
	public static final String CONFIRM_GRAMMAR_FILE = "rbtConfirm.grammar";
	public static final String CONFIRM_RULENAME = "Confirm";
    public static final String LOOP_CONFIRM_RULENAME = "Loop_Confirm";
	public static final String SLOT_CONFIRM = "slotA";
	public static final String SLOT_CLIP = "slot_clip";
	public static final String SLOT_OPTION = "slot_option";
	public static final String SLOT_COMMAND = "slot_command";
    public static final String SLOT_BACK = "slot_back";
	public static final String GET_CATEGORY_RULENAME = "GetCategoryRule";
	public static final String GET_SUB_CATEGORY_RULENAME = "GetSubCategoryRule";
	public static final String SLOT_DTMF = "slot_dtmf";
	public static final String SLOT_CATEGORY = "slot_category";
	public static final String GET_BOUQUET_RULENAME = "GetBouquetRule";
	public static final String BROWSE_CLIPS_RULENAME = "BrowseClipsRule";
	public static final String LIST_CLIPS_RULENAME = "ListClipsRule";
	public static final String PREVIEW_MESSAGE_RULENAME = "PreviewMessageRule";
	public static final String GET_MOBILE_NO_GRAMMAR_FILE= "rbtMobileNo.grammar";
	public static final String GET_MOBILE_NO_RULENAME = "SimpleDigitString";
    public static final String GET_MOBILE_NO_RULENAME1 = "SimpleDigitString1";
    public static final String GET_MOBILE_NO_RULENAME2 = "SimpleDigitString2";
	public static final String SLOT_DIGITSTRING = "slot_digitstring";
	public static final String PLAY_RBT_RULENAME = "PlayRBTRule";
	public static final String BROWSE_RBT_RULENAME = "BrowseRBTRule";
	public static final String GET_MANAGE_OPTION_RULENAME = "GetManageOptionRule";
	public static final String GET_TIME_GRAMMAR_FILE = "rbtTime.grammar";
	public static final String GET_TIME_RULENAME = "GetTime";
	public static final String GIFT_SELECTION_RULENAME = "GiftSelectionRule";
	/*time of the day changes by Gautam*/
	public static final String CONFIRM_TIME_OPTION_GRAMMAR_FILE = "rbtConfirmTimeOption.grammar";
	public static final String CONFIRM_TIME_OPTION_RULENAME = "ConfirmTimeOptionRule";
	public static final String SLOT_CONFIRM_TIME_OPTION = "slot_time";
	public static final String GET_TIME_OF_THE_DAY_GRAMMAR_FILE= "rbtTimeOfTheDay.grammar";
	public static final String GET_TIME_OF_THE_DAY_RULENAME = "TimeOfTheDayRule";
	public static final String SLOT_TIME_OF_THE_DAY = "slot_timeoftheday";
	/*Weekly/Monthly package changes by Gautam*/
	public static final String GET_CHARGING_MODEL_GRAMMAR_FILE= "rbtChargingModel.grammar";
	public static final String GET_CHARGING_MODEL_RULENAME = "ChargingModelRule";
	public static final String SLOT_CHARGING_MODEL = "slot_chargingmodel";
	//karaoke changes by sreekar
	public static final String KARAOKE_DEMO_RULENAME = "Karaoke_demo";
	public static final String KARAOKE_DEMO_GRAMMAR_FILE = "rbtKaraokeDemo.grammar";
	
	 //       Added by Sreekar for TATA blocking wired line SMS 
    public static final String RBT_SUBSCRIBER_TYPE = "RBT_SUBSCRIBER_TYPE"; 
    public static final String RBT_GIFT_SUBSCRIBER_TYPE = "RBT_GIFT_SUBSCRIBER_TYPE"; 
    public static final String param_newUser = "newUser";
    
    
	/*In resource file*/
	public static final String REJ_THRESHOLD  = "rej_threshold";
	public static final String BARGEIN_ON = "bargein_on";
	public static final String ALLOW_MORE_CATEGORIES = "allow_more_categories";
	public static final String NO_OF_CATEGORIES_PER_SET = "no_of_categories_per_set";
	public static final String ALLOW_NEXT_CLIPS = "allow_next_clips";
	public static final String ALLOW_OPEN_ENDED = "allow_open_ended";
	public static final String NO_OF_CLIPS_PER_SET = "no_of_clips_per_set";
	public static final String MAX_RECORDING_DURATION = "max_recording_duration";
	public static final String RECORDING_NSP_TIMEOUT = "recording_nsp_timeout";
	public static final String END_SECONDS = "end_seconds";
    public static final String FILE_FORMAT = "file_format"; 
	public static final String THRESHOLD_SNR = "threshold_snr";
	public static final String NSP_TIMEOUT = "nsp_timeout";
	public static final String DTMF_TERMINATION_LENGTH = "dtmf_termination_length";
	public static final String DTMF_TERMINATION_TIMEOUT = "dtmf_termination_timeout";
	public static final String DO_HANGUP = "do_hangup";
	public static final String GET_NEXT_NUMBER = "get_next_number";
	public static final String NO_OF_TRIES = "no_of_tries";
	public static final String GOTO_CATEGORY = "goto_category";
	public static final String BLACK_LIST_TYPE_KEY = "BLACK_LIST_TYPE_KEY";
	/*GSL*/
	public static final String GSL_SOGETCATEGORY_MANAGE = "gsl.sogetcategory.manage";
	public static final String GSL_SOGETCATEGORY_MORE = "gsl.sogetcategory.more";
	public static final String GSL_GOTOCATEGORY_CATEGORIES = "gsl.gotocategory.categories";
	public static final String GSL_SOLISTCLIPS_MORE = "gsl.solistclips.more";
	public static final String GSL_SOPREVIEWMESSAGE_SET = "gsl.sopreviewmessage.set";	
	public static final String GSL_SOPREVIEWMESSAGE_RECORD = "gsl.sopreviewmessage.record";	
	public static final String GSL_SOPREVIEWMESSAGE_REPLAY = "gsl.sopreviewmessage.replay";	
	public static final String GSL_SETRBT_CHANGE = "gsl.setrbt.change";
	public static final String GSL_SETRBT_REMOVE = "gsl.setrbt.remove";
    public static final String GSL_SETRBTALL_REMOVE = "gsl.setrbtall.remove";
	public static final String GSL_SETRBT_NEXT = "gsl.setrbt.next";
    public static final String GSL_SETRBTALL_NEXT = "gsl.setrbtall.next";
	public static final String GSL_SOGETMANAGEOPTION_SELECTIONS = "gsl.sogetmanageoption.selections";
	public static final String GSL_SOGETMANAGEOPTION_COPY = "gsl.sogetmanageoption.copy";
	public static final String GSL_SOGETMANAGEOPTION_UNSUBSCRIBE = "gsl.sogetmanageoption.unsubscribe";
    public static final String GSL_SOGETMANAGEOPTION_ASKPASSWORD = "gsl.sogetmanageoption.askpassword";
	public static final String GSL_SOGIFTSELECTION_SET = "gsl.sogiftselection.set";
	public static final String GSL_SOGIFTSELECTION_GIFT = "gsl.sogiftselection.gift";
	public static final String GSL_SOGIFTSELECTION_BACK = "gsl.sogiftselection.back";
	public static final String GSL_SOPREVIEWRECORDEDKARAOKE_SET = "gsl.sopreviewrecordedkaraoke.set";	
	public static final String GSL_SOPREVIEWRECORDEDKARAOKE_RECORD = "gsl.sopreviewrecordedkaraoke.record";	
	public static final String GSL_SOPREVIEWRECORDEDKARAOKE_REPLAY = "gsl.sopreviewrecordedkaraoke.replay";	

	/*General prompts*/
	//Bengali
	public static final String RBT_BEN = "dialog.rbt_ben";
	//Bhojpuri
	public static final String RBT_BOJ = "dialog.rbt_boj";
	//English
	public static final String RBT_ENG = "dialog.rbt_eng";
	//Gujarati
	public static final String RBT_GUJ = "dialog.rbt_guj";
	//Indi
	public static final String RBT_IND = "dialog.rbt_ind";
	//Hindi
	public static final String RBT_HIN = "dialog.rbt_hin";
	//Kannada
	public static final String RBT_KAN = "dialog.rbt_kan";
	//Malayalam
	public static final String RBT_MAL = "dialog.rbt_mal";
	//Marathi
	public static final String RBT_MAR = "dialog.rbt_mar";
	//Punjabi
	public static final String RBT_PUN = "dialog.rbt_pun";
	//Rajasthani   
    public static final String RBT_RAJ = "dialog.rbt_raj"; 
	//Tamil
	public static final String RBT_TAM = "dialog.rbt_tam";
	//Telugu
	public static final String RBT_TEL = "dialog.rbt_tel";
	//Bahasa
	public static final String RBT_BAH = "dialog.rbt_bah";
	//Bengali
	public static final String RBT_BEN_VOICE = "dialog.rbt_ben_voice";
	//Bhojpuri
	public static final String RBT_BOJ_VOICE = "dialog.rbt_boj_voice";
	//English
	public static final String RBT_ENG_VOICE = "dialog.rbt_eng_voice";
	//Gujarati
	public static final String RBT_GUJ_VOICE = "dialog.rbt_guj_voice";
	//Indi
	public static final String RBT_IND_VOICE = "dialog.rbt_ind_voice";
	//Hindi
	public static final String RBT_HIN_VOICE = "dialog.rbt_hin_voice";
	//Kannada
	public static final String RBT_KAN_VOICE = "dialog.rbt_kan_voice";
	//Malayalam
	public static final String RBT_MAL_VOICE = "dialog.rbt_mal_voice";
	//Marathi
	public static final String RBT_MAR_VOICE = "dialog.rbt_mar_voice";
	//Punjabi
	public static final String RBT_PUN_VOICE = "dialog.rbt_pun_voice";
	//Tamil
	public static final String RBT_TAM_VOICE = "dialog.rbt_tam_voice";
	//Telugu
	public static final String RBT_TEL_VOICE = "dialog.rbt_tel_voice";
	//Bahasa
	public static final String RBT_BAH_VOICE = "dialog.rbt_bah_voice";
	//Bengali
	public static final String RBT_BEN_DTMF = "dialog.rbt_ben_dtmf";
	//Bhojpuri
	public static final String RBT_BOJ_DTMF = "dialog.rbt_boj_dtmf";
	//English
	public static final String RBT_ENG_DTMF = "dialog.rbt_eng_dtmf";
	//Gujarati
	public static final String RBT_GUJ_DTMF = "dialog.rbt_guj_dtmf";
	//Indi
	public static final String RBT_IND_DTMF = "dialog.rbt_ind_dtmf";
	//Hindi
	public static final String RBT_HIN_DTMF = "dialog.rbt_hin_dtmf";
	//Kannada
	public static final String RBT_KAN_DTMF = "dialog.rbt_kan_dtmf";
	//Malayalam
	public static final String RBT_MAL_DTMF = "dialog.rbt_mal_dtmf";
	//Marathi
	public static final String RBT_MAR_DTMF = "dialog.rbt_mar_dtmf";
	//Punjabi
	public static final String RBT_PUN_DTMF = "dialog.rbt_pun_dtmf";
	//Tamil
	public static final String RBT_TAM_DTMF = "dialog.rbt_tam_dtmf";
	//Telugu
	public static final String RBT_TEL_DTMF = "dialog.rbt_tel_dtmf";
	//Bahasa
	public static final String RBT_BAH_DTMF = "dialog.rbt_bah_dtmf";
	//Welcome to caller tunes
	public static final String RBT_WELCOME  = "dialog.rbt_welcome";  
	//Where you decide what someone else gets to hear when they call your phone instead of the regular tring tring. For eg. you can choose Main hoon naa for your friend so that she hears Main hoon naa when she calls you  
	public static final String RBT_INTRO  = "dialog.rbt_intro"; 
	//<second intro after playing the preview>  
	public static final String RBT_INTRO_1  = "dialog.rbt_intro_1"; 
	//We are upgrading the system right now, We regret the inconvenience. Please call back later
	public static final String RBT_FATAL_ERROR = "dialog.rbt_fatal_error";
	//I am sorry, I am having trouble understanding you
	public static final String RBT_GIVEUP = "dialog.rbt_giveup";
	//For now, lets go back to the main menu
	public static final String RBT_MAIN_MENU = "dialog.rbt_main_menu";
	//To go to the main menu, press 0	
	public static final String RBT_MAIN_MENU_DTMF = "dialog.rbt_main_menu_dtmf";
	//For
	public static final String RBT_FOR = "dialog.rbt_for";
	//Press 1
	public static final String RBT_PRESS_1 = "dialog.rbt_press_1";  
	//Press 2
    public static final String RBT_PRESS_2 = "dialog.rbt_press_2";  
	//Press 3
    public static final String RBT_PRESS_3 = "dialog.rbt_press_3";  	
	//Press 4
    public static final String RBT_PRESS_4 = "dialog.rbt_press_4";  	
	//Press 5
    public static final String RBT_PRESS_5 = "dialog.rbt_press_5";  	
	//Press 6
    public static final String RBT_PRESS_6 = "dialog.rbt_press_6";  	
	//Press 7
    public static final String RBT_PRESS_7 = "dialog.rbt_press_7";  	
	//Press 8
    public static final String RBT_PRESS_8 = "dialog.rbt_press_8";  	
	//Press 9
    public static final String RBT_PRESS_9 = "dialog.rbt_press_9";  
    //Press star
    public static final String RBT_PRESS_STAR = "dialog.rbt_press_star";  
	//I didn't hear you
	public static final String RBT_NSP = "dialog.rbt_nsp";  
	//I didn't get you
	public static final String RBT_REJ = "dialog.rbt_rej";  
	//Sorry, this clip is not available 
    public static final String RBT_CLIP_UNAVAILABLE = "dialog.rbt_clip_unavailable";  
	//Sorry, I don't have any selections for you right now 
    public static final String RBT_CLIPS_UNAVAILABLE = "dialog.rbt_clips_unavailable";
	//To change or remove your tune, say change
	public static final String RBT_MANAGE = "dialog.rbt_manage";
	//To change or remove your tune
	public static final String RBT_MANAGE_DTMF = "dialog.rbt_manage_dtmf";
	//I am sorry, but you have not made any selection
	public static final String RBT_MANAGE_INVALID = "dialog.rbt_manage_invalid";
	//You have
	public static final String RBT_MANAGE_INTRO_1 = "dialog.rbt_manage_intro_1";
	//no of selections
	public static final String RBT_MANAGE_INTRO_2 = "dialog.rbt_manage_intro_2";
	//I am sorry, this is an invalid option
	public static final String RBT_INVALID_OPTION = "dialog.rbt_invalid_option";
	//on a weekly subscription
	public static final String RBT_MANAGE_WEEKLY = "dialog.rbt_manage_weekly";
	//on a monthly subscription
	public static final String RBT_MANAGE_MONTHLY = "dialog.rbt_manage_monthly";
	//To go back to categories, say categories
	public static final String RBT_GET_CATEGORY = "dialog.rbt_get_category";
	//To go back to categories, press star
	public static final String RBT_GET_CATEGORY_DTMF = "dialog.rbt_get_category_dtmf";
	//you want to record your own, press star
	public static final String RBT_RECORD_OWN = "dialog.rbt_record_own";
	//beep
	public static final String RBT_BEEP = "dialog.rbt_beep";  
	//For now, lets go back to categories
	public static final String RBT_GOTO_GET_CATEGORY = "dialog.rbt_goto_get_category";
//	For now, lets go back 
    public static final String RBT_GOTO_GET_DTMFBROWSECLIP = "dialog.rbt_goto_get_dtmfbrowseclip"; 
	//Sorry the caller tune you have selected is a user recorded message and cannot be copied.
	public static final String RBT_PERSONAL_MESSAGE_COPY_NOT_POSSIBLE	=	"dialog.rbt_personal_message_copy_not_possible";  
	//Sorry the caller tune you have selected is a user recorded message and cannot be copied.
	public static final String RBT_COPY_NOT_POSSIBLE	=	"dialog.rbt_copy_not_possible";
	//If yes, press 1, else press 2
	public static final String RBT_CONFIRM = "dialog.rbt_confirm";  
	//Zero
	public static final String RBT_ZERO = "dialog.rbt_zero";
	//One
	public static final String RBT_ONE = "dialog.rbt_one";
	//Two
	public static final String RBT_TWO = "dialog.rbt_two";
	//Three
	public static final String RBT_THREE = "dialog.rbt_three";
	//Four
	public static final String RBT_FOUR = "dialog.rbt_four";
	//Five
	public static final String RBT_FIVE = "dialog.rbt_five";
	//Six
	public static final String RBT_SIX = "dialog.rbt_six";
	//Seven
	public static final String RBT_SEVEN = "dialog.rbt_seven";
	//Eight
	public static final String RBT_EIGHT = "dialog.rbt_eight";
	//Nine
	public static final String RBT_NINE = "dialog.rbt_nine";
	//All
	public static final String RBT_ALL = "dialog.rbt_all";
	//OK
	public static final String RBT_OK = "dialog.rbt_ok";  
	//You have exceeded your free selections. You will be charged 10 Rs. for this selection
	public static final String RBT_PAID_SELECTION = "dialog.rbt_paid_selection";
	//You have
	public static final String RBT_FREE_SELECTION_1 = "dialog.rbt_free_selection_1";
	//more free selections
	public static final String RBT_FREE_SELECTION_2 = "dialog.rbt_free_selection_2";
	//Bye and thanks for calling
	public static final String RBT_THANK_YOU = "dialog.rbt_thank_you";  
	//Remember you can always call back anytime to change your caller tune
	public static final String RBT_FINAL = "dialog.rbt_final";  
	//You cannot resubscribe immediately after unsubscribing from this service. Please call back later
	public static final String RBT_ACTIVATION_FAILED = "dialog.rbt_activation_failed"; 
	//Currently you are not active on caller tunes. Please call 123 and say caller tunes to subscribe to this service
	public static final String RBT_SUBSCRIBER_NOT_ACTIVE = "dialog.rbt_subscriber_not_active"; 
	//This is a premium category. Any selection here is a premium selection. The cost per selection is X
	public static final String RBT_CATEGORY_IS_PREMIUM_INTRO_1 = "dialog.rbt_category_is_premium_intro_1";
	//Rs
	public static final String RBT_CATEGORY_IS_PREMIUM_INTRO_2 = "dialog.rbt_category_is_premium_intro_2";
	//This is a NON premium category. The cost per selection is X
	public static final String RBT_CATEGORY_IS_NON_PREMIUM_INTRO_1 = "dialog.rbt_category_is_non_premium_intro_1";
	//Rs
	public static final String RBT_CATEGORY_IS_NON_PREMIUM_INTRO_2 = "dialog.rbt_category_is_non_premium_intro_2";
	//This is available only for subscribers who are not active on caller tunes
	public static final String RBT_SUBSCRIBER_ACTIVE = "dialog.rbt_subscriber_active"; 
	//You are not authorized to use this service
	public static final String RBT_ACCESS_NOT_ALLOWED = "dialog.rbt_access_not_allowed";
	/*time of the day changes by Gautam*/
	//From
	public static final String RBT_FROM = "dialog.rbt_from";
	//To
	public static final String RBT_TO = "dialog.rbt_to";
	//AM
	public static final String RBT_AM = "dialog.rbt_am";
	//PM
	public static final String RBT_PM1 = "dialog.rbt_pm1";
	//PM
	public static final String RBT_PM2 = "dialog.rbt_pm2";
	//PM
	public static final String RBT_PM3 = "dialog.rbt_pm3";
	//Ten
	public static final String RBT_TEN = "dialog.rbt_ten";
	//Eleven
	public static final String RBT_ELEVEN = "dialog.rbt_eleven";
	//Twelve
	public static final String RBT_TWELVE = "dialog.rbt_twelve";
    
    //press one
    public static final String RBT_PRESS1 = "dialog.rbt_press1";
    
    //press two
    public static final String RBT_PRESS2 = "dialog.rbt_press2";

	/*time of the day changes by Gautam*/
	//Monthly
	public static final String RBT_MONTHLY = "dialog.rbt_montly";
	//Weekly
	public static final String RBT_WEEKLY = "dialog.rbt_weekly";
	//This is a 
	public static final String RBT_PACKAGE_INTRO_1 = "dialog.rbt_package_intro_1";
	//Selection
	public static final String RBT_PACKAGE_INTRO_2 = "dialog.rbt_package_intro_2";

	
	/*POEntry prompts*/
	//Looks like you are roaming. You can use this service only when you are in your home network. So don't forget to call back when you are home
	public static final String RBT_POENTRY_INVALID_PREFIX = "dialog.rbt_poentry_invalid_prefix";
	
	//Whats your language preference? Say English or Hindi
	public static final String RBT_SOGETLANGUAGE_INTRO = "dialog.rbt_sogetlanguage_intro";
	
	/*SOConfirmContinuity prompts*/
	//You will be charged a monthly fee of 30 Rs for this service. You can select upto 3 tunes at no additional charge. Any additional tune will be charged at 10 Rs per tune. If you wish to continue, press 1
	public static final String RBT_SOCONFIRMCONTINUITY_INTRO = "dialog.rbt_soconfirmcontinuity_intro";
	public static final String RBT_SOADDEDTOLOOP="dialog.rbt_soaddedtoloop";
	
	/*SOGetCategory prompts*/	
	//Select a category to set a new caller tune by saying
	public static final String RBT_SOGETCATEGORY_INTRO_1 = "dialog.rbt_sogetcategory_intro_1";
	//To go to the next set of categories, say more
	public static final String RBT_SOGETCATEGORY_INTRO_2 = "dialog.rbt_sogetcategory_intro_2";
	//To go to the next set of categories
	public static final String RBT_SOGETCATEGORY_INTRO_3 = "dialog.rbt_sogetcategory_intro_3";
	//To change the language say
	public static final String RBT_SOGETCATEGORY_INTRO_4 = "dialog.rbt_sogetcategory_intro_4";
	//Please select the category following
	public static final String RBT_SOGETCATEGORY_DTMF_INTRO_1 = "dialog.rbt_sogetcategory_dtmf_intro_1";

	/*SOGetSubCategory prompts*/
	//There are more categories to choose from. Select a category by saying
	public static final String RBT_SOGETSUBCATEGORY_INTRO = "dialog.rbt_sogetsubcategory_intro";
	
	/*SOGetBouquet prompts*/
	//Selection a collection by saying
	public static final String RBT_SOGETBOUQUET_INTRO = "dialog.rbt_sogetbouquet_intro";
	public static final String RBT_SOGETBOUQUET_INTRO_2 = "dialog.rbt_sogetbouquet_intro_2";
	public static final String RBT_SOGETBOUQUET_INTRO_3 = "dialog.rbt_sogetbouquet_intro_3";
	
	//SubscriberDownload Statuses
	public static final char STATE_DOWNLOAD_TO_BE_ACTIVATED = 'n';
	public static final char STATE_DOWNLOAD_ACTIVATION_PENDING = 'p';
	public static final char STATE_DOWNLOAD_ACTIVATED = 'y';
	public static final char STATE_DOWNLOAD_TO_BE_DEACTIVATED = 'd';
	public static final char STATE_DOWNLOAD_DEACTIVATION_PENDING = 's';
	public static final char STATE_DOWNLOAD_DEACTIVATED = 'x';
	public static final char STATE_DOWNLOAD_BOOKMARK = 'b';
	public static final char STATE_DOWNLOAD_ACT_ERROR = 'e';
	public static final char STATE_DOWNLOAD_DEACT_ERROR = 'f';
	public static final char STATE_DOWNLOAD_BASE_ACT_PENDING = 'w';
	public static final char STATE_DOWNLOAD_GRACE = 'g';
	public static final char STATE_DOWNLOAD_SUSPENSION = 'z';
	public static final char STATE_DOWNLOAD_CHANGE = 'c';
	public static final char STATE_DOWNLOAD_SEL_TRACK = 't';
	
	/*SOListClips prompts*/
	//Just say a latest song name
	public static final String RBT_SOLISTCLIPS_INTRO_1 = "dialog.rbt_solistclips_intro_1";
	//Ok. Let me help you with the selection
	public static final String RBT_SOLISTCLIPS_INTRO_2 = "dialog.rbt_solistclips_intro_2";
	//To go to the next set of tunes, say next
	public static final String RBT_SOLISTCLIPS_INTRO_3 = "dialog.rbt_solistclips_intro_3";
	//To go to the next set of tunes
	public static final String RBT_SOLISTCLIPS_INTRO_4 = "dialog.rbt_solistclips_intro_4";
	
	/*SOBrowseClips prompts*/
	//Here is the list
	public static final String RBT_SOBROWSECLIPS_INTRO_1 = "dialog.rbt_sobrowseclips_intro_1";
	//Select a caller tune by saying its name at anytime
	public static final String RBT_SOBROWSECLIPS_INTRO_2 = "dialog.rbt_sobrowseclips_intro_2";
	//Sorry we don't seem to have what you asked for
	public static final String RBT_SOBROWSECLIPS_CLIP_UNAVAILABLE = "dialog.rbt_sobrowseclips_clip_unavailable";
    
	public static final String RBT_SODTMFBROWSECLIPS_INTRO_1 = "dialog.rbt_sodtmfbrowseclips_intro_1";
    public static final String RBT_SODTMFBROWSECLIPS_INTRO_2 = "dialog.rbt_sodtmfbrowseclips_intro_2";
    public static final String RBT_SODTMFBROWSECLIPS_INTRO_3 = "dialog.rbt_sodtmfbrowseclips_intro_3";
    public static final String RBT_SODTMFBROWSECLIPS_PLAYINGFIRSTCLIPAGAIN = "dialog.rbt_sodtmfbrowseclips_playingfirstclipagain";

	/*SORecordMessage prompts*/
	//Record the message after the beep and when you are done press 1
	public static final String RBT_SORECORDMESSAGE_INTRO = "dialog.rbt_sorecordmessage_intro";

	/*SOPreviewMessage prompts*/
	//Here is the message we have just recorded
	public static final String RBT_SOPREVIEWMESSAGE_INTRO = "dialog.rbt_sopreviewmessage_intro";
	//Now what do you want to do? Set it, record again or replay
	public static final String RBT_SOPREVIEWMESSAGE_OPTIONS = "dialog.rbt_sopreviewmessage_options";
	//To set it, press 1. To record again, press 2. To replay, press 3
	public static final String RBT_SOPREVIEWMESSAGE_OPTIONS_DTMF = "dialog.rbt_sopreviewmessage_options_dtmf";

	/*SOSetSelection prompts*/
	//Shall I set this as your caller tune?
	public static final String RBT_SOSETSELECTION_INTRO = "dialog.rbt_sosetselection_intro";

	/*SOGetMobileNumber prompts*/
	//To set it for everyone, say all or just key in the phone number including the STD code of the person for whom you wish to set this caller tune and remember, include the STD code for the local landline numbers as well. 
	public static final String RBT_SOGETMOBILENUMBER_INTRO = "dialog.rbt_sogetmobilenumber_intro";
	//Key in the phone number including the STD code of the person for whom you wish to set this caller tune or say all
	public static final String RBT_SOGETMOBILENUMBER_ERROR = "dialog.rbt_sogetmobilenumber_error";
	//You cannot set a caller tune for this number
	public static final String RBT_SOGETMOBILENUMBER_INVALID = "dialog.rbt_sogetmobilenumber_invalid";
	//You have already set a caller tune for this number
	public static final String RBT_SOGETMOBILENUMBER_REPEAT = "dialog.rbt_sogetmobilenumber_repeat";
	//Since you are corporate subscriber, you cannot set Caller Tune for ALL
	public static final String RBT_SOGETMOBILENUMBER_CORPORATE = "dialog.rbt_sogetmobilenumber_corporate";
	
	/*SOConfirmMobileNumber prompts*/
	//I got that as
	public static final String RBT_SOCONFIRMMOBILENUMBER_INTRO_1 = "dialog.rbt_soconfirmmobilenumber_intro_1";
	//Is that correct?
	public static final String RBT_SOCONFIRMMOBILENUMBER_INTRO_2 = "dialog.rbt_soconfirmmobilenumber_intro_2";
	
	/*SOGetNextMobileNumber prompts*/
	//Do you wish to select this caller tune for another caller?
	public static final String RBT_SOGETNEXTMOBILENUMBER_INTRO = "dialog.rbt_sogetnextmobilenumber_intro";
	
	/*POSetData prompts*/
	//Your caller tune has been successfully set
	public static final String RBT_POSETDATA_INTRO_1 = "dialog.rbt_posetdata_intro_1";
	//Your caller tune selection request has been accepted
	public static final String RBT_POSETDATA_INTRO_2 = "dialog.rbt_posetdata_intro_2";
	//Your caller tune will be set within 24 hours. 
    public static final String RBT_POSETDATA_INTRO_SET_PERIOD = "dialog.rbt_posetdata_intro_set_period"; 

	
	/*SOGetCallerNumber prompts*/
	//Just key in the phone number of the person for whom you wish to change the caller tune
	public static final String RBT_SOGETCALLERNUMBER_INTRO_1 = "dialog.rbt_sogetcallernumber_intro_1";
	//To change what the rest of your callers listen to, say all
	public static final String RBT_SOGETCALLERNUMBER_INTRO_2 = "dialog.rbt_sogetcallernumber_intro_2";
	//To list your selections, say list
	public static final String RBT_SOGETCALLERNUMBER_INTRO_3 = "dialog.rbt_sogetcallernumber_intro_3";
	
	/*SOPlayRBT prompts*/
	//Your caller tune is 
	public static final String RBT_SOPLAYRBT_INTRO = "dialog.rbt_soplayrbt_intro";
	//Now what do you want to do? Change it or remove it.To change it say change, to remove it say remove.
	public static final String RBT_SOPLAYRBT_OPTIONS = "dialog.rbt_soplayrbt_options";
    //Now what do you want to do? remove it
    public static final String RBT_SOPLAYRBTALL_OPTIONS = "dialog.rbt_soplayrbtall_options";
	//To change it, press 1. To remove it, press 2
	public static final String RBT_SOPLAYRBT_OPTIONS_DTMF = "dialog.rbt_soplayrbt_options_dtmf";
    //To remove it, press 1
    public static final String RBT_SOPLAYRBTALL_OPTIONS_DTMF = "dialog.rbt_soplayrbtall_options_dtmf";
	//To change the caller tune
	public static final String RBT_SOPLAYRBT_CHANGE = "dialog.rbt_soplayrbt_change";
	//The caller tune has been successfully removed
	public static final String RBT_SOPLAYRBT_REMOVE = "dialog.rbt_soplayrbt_remove";
	
	/*SOSetRBT prompts*/
	//You have not set any caller tune for
	public static final String RBT_SOSETRBT_INTRO_1 = "dialog.rbt_sosetrbt_intro_1";
	//Do you want to set it now?
	public static final String RBT_SOSETRBT_INTRO_2 = "dialog.rbt_sosetrbt_intro_2";
	//To set the caller tune for
	public static final String RBT_SOSETRBT_SET = "dialog.rbt_sosetrbt_set";
	
	/*SOBrowseRBT prompts*/
	//To change it, say change. To remove it, say remove. For the next selection, say next
	public static final String RBT_SOBROWSERBT_OPTIONS = "dialog.rbt_sobrowserbt_options";
	//To remove it, say remove. For the next selection, say next
    public static final String RBT_SOBROWSERBTALL_OPTIONS = "dialog.rbt_sobrowserbtall_options";
	//To change it, press 1. To remove it, press 2. For the next selection, press 3 
	public static final String RBT_SOBROWSERBT_OPTIONS_DTMF = "dialog.rbt_sobrowserbt_options_dtmf";
    //To remove it, press 1. For the next selection, press 2
    public static final String RBT_SOBROWSERBTALL_OPTIONS_DTMF = "dialog.rbt_sobrowserbtall_options_dtmf";
	//To change it, press 1. To remove it, press 2. For the next selection, press 3 RBT_SOBROWSERBT_FIRSTAGAIN
	public static final String RBT_SOBROWSERBT_FIRSTAGAIN = "dialog.rbt_sobrowserbt_firstagain";	
	
	/*SOGetManageOption prompts*/
	//To browse through your selections, say selections
	public static final String RBT_SOGETMANAGEOPTION_INTRO_1 = "dialog.rbt_sogetmanageoption_intro_1";
	//To choose someone else's selection, say copy
	public static final String RBT_SOGETMANAGEOPTION_INTRO_2 = "dialog.rbt_sogetmanageoption_intro_2";
	//To unsubscribe from caller tunes, say unsubscribe 
	public static final String RBT_SOGETMANAGEOPTION_INTRO_3 = "dialog.rbt_sogetmanageoption_intro_3";
    //To manage your password, say password
    public static final String RBT_SOGETMANAGEOPTION_INTRO_4 = "dialog.rbt_sogetmanageoption_intro_4";
	//To browse through your selections, press 1
	public static final String RBT_SOGETMANAGEOPTION_DTMF_1 = "dialog.rbt_sogetmanageoption_dtmf_1";
	//To choose someone else's selection, press 2
	public static final String RBT_SOGETMANAGEOPTION_DTMF_2 = "dialog.rbt_sogetmanageoption_dtmf_2";
	//To unsubscribe from caller tunes, press 3
	public static final String RBT_SOGETMANAGEOPTION_DTMF_3 = "dialog.rbt_sogetmanageoption_dtmf_3";
    //To manage your password, press 4
    public static final String RBT_SOGETMANAGEOPTION_DTMF_4 = "dialog.rbt_sogetmanageoption_dtmf_4";
	//You have been successfully unsubscribed from caller tunes
	public static final String RBT_SOGETMANAGEOPTION_DEACTIVE = "dialog.rbt_sogetmanageoption_deactive";
	//As your activation request is still being processed, you cannot deactivate now. Please try again later.
	public static final String RBT_SOGETMANAGEOPTION_DEACTIVE_FAILED = "dialog.rbt_sogetmanageoption_deactive_failed";
	
	/*SOCopySelection prompts*/
	//Just key in the phone number of the person whose caller tune you wish to copy
	public static final String RBT_SOCOPYSELECTION_INTRO = "dialog.rbt_socopyselection_intro";
	//The person does not have any active selections
	public static final String RBT_SOCOPYSELECTION_INVALID = "dialog.rbt_socopyselection_invalid";
	
	/*SOUnsubscribeRBT prompts*/
	//To unsubscribe from caller tunes, press 1
	public static final String RBT_SOUNSUBSCRIBERBT_INTRO = "dialog.rbt_sounsubscriberbt_intro";

	/*SOPickOfTheDay prompts*/
	//The pick of the day is 
	public static final String RBT_SOPICKOFTHEDAY_INTRO = "dialog.rbt_sopickoftheday_intro";
	
	/*SOConfirmFeed prompts*/
	//If you want to set it as your caller tune, press 1
	public static final String RBT_SOCONFIRMFEED_INTRO_1 = "dialog.rbt_soconfirmfeed_intro_1";
	//To remove this caller tune, press 1
	public static final String RBT_SOCONFIRMFEED_INTRO_2 = "dialog.rbt_soconfirmfeed_intro_2";
	
	/*SOGetTime prompts*/
	//Just key in the number of hours for which you want to set this caller tune. For eg: For 24 hours, press two four
	public static final String RBT_SOGETTIME_INTRO = "dialog.rbt_sogettime_intro";
	//The number of hours you have entered is invalid
	public static final String RBT_SOGETTIME_INVALID = "dialog.rbt_sogettime_invalid";
	
	/*SOGiftSelection prompts*/
	//To set this as your caller tune, say set it
	public static final String RBT_SOGIFTSELECTION_INTRO_1 = "dialog.rbt_sogiftselection_intro_1";
	//To gift this to someone, say gift it
	public static final String RBT_SOGIFTSELECTION_INTRO_2 = "dialog.rbt_sogiftselection_intro_2";
	//To go back, say go back
	public static final String RBT_SOGIFTSELECTION_INTRO_3 = "dialog.rbt_sogiftselection_intro_3";
	//To set this as your caller tune, press 1
	public static final String RBT_SOGIFTSELECTION_DTMF_1 = "dialog.rbt_sogiftselection_dtmf_1";
	//To gift it to someone, press 2
	public static final String RBT_SOGIFTSELECTION_DTMF_2 = "dialog.rbt_sogiftselection_dtmf_2";
	//To go back, press 3
	public static final String RBT_SOGIFTSELECTION_DTMF_3 = "dialog.rbt_sogiftselection_dtmf_3";
	
	/*SOConfirmGifting*/
	//You will be charged a one time fee of 30 Rs for this service. You will also be charged extra for the tune. If you wish to continue, press 1
	public static final String RBT_SOCONFIRMGIFTING_INTRO = "dialog.rbt_soconfirmgifting_intro";
	
	/*SOGiftMobileNumber prompts*/
	//Please enter the 10-digit mobile number of the Hutch subscriber to whom you want to gift this caller tune
	public static final String RBT_SOGIFTMOBILENUMBER_INTRO = "dialog.rbt_sogiftmobilenumber_intro";
	//You cannot gift caller tunes to this number
	public static final String RBT_SOGIFTMOBILENUMBER_INVALID = "dialog.rbt_sogiftmobilenumber_invalid";
	//You have already gifted a caller tune for this number
	public static final String RBT_SOGIFTMOBILENUMBER_REPEAT = "dialog.rbt_sogiftmobilenumber_repeat";
	
	/*POGiftData prompts*/
	//Your gift request has been accepted
	public static final String RBT_POGIFTDATA_INTRO = "dialog.rbt_pogiftdata_intro";  
	
	/*SOGetNextGiftNumber prompts*/
	//Do you want to gift this caller tune to another number?
	public static final String RBT_SOGETNEXTGIFTNUMBER_INTRO = "dialog.rbt_sogetnextgiftnumber_intro";
	
	/*SOPlayGift prompts*/
	//Here is the caller tune that has been gifted to you
	public static final String RBT_SOPLAYGIFT_INTRO_1 = "dialog.rbt_soplaygift_intro_1";
	//You will not be charged for this service for the first month. Second month onwards, you will be charged a monthly rental of Rs 30
	public static final String RBT_SOPLAYGIFT_INTRO_2 = "dialog.rbt_soplaygift_intro_2";
	//The song that has been gifted to you is free of charge. If you want to accept the gift, press 1, else press 2
	public static final String RBT_SOPLAYGIFT_INTRO_3 = "dialog.rbt_soplaygift_intro_3";

	
	/*time of the day changes by Gautam*/
	/*SOConfirmTimeOption prompts*/
	//To set this tune for full day, say Full Day. To set it for specific hours of the day, say Time.
	public static final String RBT_SOCONFIRMTIMEOPTION_INTRO = "dialog.rbt_soconfirmtimeoption_intro";
	//To set this tune for full day, press 1. To set it for specific hours of the day, press2.
	public static final String RBT_SOCONFIRMTIMEOPTION_DTMF = "dialog.rbt_soconfirmtimeoption_dtmf";
	
	
	/*SOGetTimeOfTheDay prompts*/
	//To set this caller tune for full day, press star. Or if you wish to set it for a specific time of the day, just key in the time period. For example, to play this tune between 8am and 3pm, key in 0815
	public static final String RBT_SOGETTIMEOFTHEDAY_INTRO = "dialog.rbt_sogettimeoftheday_intro";
	//To set it for full day, press star. To set it for a particular time period, key in the required hours. For example to play this tune between 6am and 10pm, key in 0622
	public static final String RBT_SOGETTIMEOFTHEDAY_ERROR = "dialog.rbt_sogettimeoftheday_error";
	//You cannot set a caller tune for this time
	public static final String RBT_SOGETTIMEOFTHEDAY_INVALID = "dialog.rbt_sogettimeoftheday_invalid";
	//You have already set a caller tune for this time
	public static final String RBT_SOGETTIMEOFTHEDAY_REPEAT = "dialog.rbt_sogettimeoftheday_repeat";
	
	
	/*weekly/monthly package changes by Gautam*/
	/*SOGetChargingModel prompts*/
	//You will be charged Rp.9000 per month for monthly subscription and Rp.3000 per week for weekly subscription. Select one by saying "weekly" or "monthly"
	public static final String RBT_SOGETCHARGINGMODEL_INTRO = "dialog.rbt_sogetchargingmodel_intro";
	//To select monthly subscription at the rate of Rp.9000 per month press 1. To select weekly subscription at the rate of Rp.3000 per week press 2.
	public static final String RBT_SOGETCHARGINGMODEL_ERROR= "dialog.rbt_sogetchargingmodel_error";
	
	/* SOConfirmChargingModel prompts */
	// If you are a postpaid user you will be charged Rp 3000 every week on your phone bill and the subscription will be renewed automatically every week. If you are a prepaid user, you need to have at least Rp. 3300 as balance at the end of this call for successful subscription of this service. If you wish to continue, press 1
	public static final String RBT_SOCONFIRMCHARGING_MODEL_WEEKLY= "dialog.rbt_soconfirmcontinuity_intro_weekly";
	//If you are a postpaid user you will be charged Rp 9000 every month on your phone bill and the subscription will be renewed automatically every month. If you are a prepaid user, you need to have at least Rp. 9900 as balance at the end of this call for successful subscription of this service. If you wish to continue, press 1
	public static final String RBT_SOCONFIRMCHARGING_MODEL_MONTHLY= "dialog.rbt_soconfirmcontinuity_intro_monthly";
	//
	
	/* SORecordKaraoke prompts */
	// If you are a postpaid user you will be charged Rp 3000 every week on your phone bill and the subscription will be renewed automatically every week. If you are a prepaid user, you need to have at least Rp. 3300 as balance at the end of this call for successful subscription of this service. If you wish to continue, press 1
	public static final String RBT_SORECORDKARAOKE_INTRO= "dialog.rbt_sorecordkaraoke_intro";
	
	/*SOPreviewRecordedKaraoke prompts*/
	//Here is the message we have just recorded
	public static final String RBT_SOPREVIEWRECORDEDKARAOKE_INTRO = "dialog.rbt_sopreviewrecordedkaraoke_intro";
	//Now what do you want to do? Set it, record again or replay
	public static final String RBT_SOPREVIEWRECORDEDKARAOKE_OPTIONS = "dialog.rbt_sopreviewrecordedkaraoke_options";
	//To set it, press 1. To record again, press 2. To replay, press 3
	public static final String RBT_SOPREVIEWRECORDEDKARAOKE_OPTIONS_DTMF = "dialog.rbt_sopreviewrecordedkaraoke_options_dtmf";

	/*SOPlayKaraokeDemo prompts*/
	//Here is the message we have just recorded
	public static final String RBT_SOPLAYKARAOKEDEMO_INTRO = "dialog.rbt_soplaykaraokedemo_intro";
	//Now what do you want to do? Set it, record again or replay
	public static final String RBT_SOPLAYKARAOKEDEMO_CONFIRM = "dialog.rbt_soplaykaraokedemo_confirm";
	
	public static final String RBT_SOBROWSEKARAOKECLIPS_INTRO_1 = "dialog.rbt_sobrowsekaraokeclips_intro_1";
    public static final String RBT_SOBROWSEKARAOKECLIPS_INTRO_2 = "dialog.rbt_sobrowsekaraokeclips_intro_2";
    public static final String RBT_SOBROWSEKARAOKECLIPS_INTRO_3 = "dialog.rbt_sobrowsekaraokeclips_intro_3";
    public static final String RBT_SOBROWSEKARAOKECLIPS_PLAYINGFIRSTCLIPAGAIN = "dialog.rbt_sobrowsekaraokeclips_playingfirstclipagain";
    
    //For OptIn and OptOut model
    public static final String GET_OPTIN_OPTOUT_KEY = "GET_OPRIN_OPTOUT";
    public static final String CONFIRM_OPTIN_OPTOUT_KEY = "CONFIRM_OPTIN_OPTOUT";
    public static final String OPTIN_OPTOUT_MODEL = "OPTIN_OPTOUT_MODEL";
    public static final String ALLOWEDOPTINOPTOUT = "ALLOWEDOPTINOPTOUT";
    public static final String PRMO_TYPE = "PRMO_TYPE";
    
    //For SOAskPassword
    public static final String ASK_PASSWORD_KEY = "ASK_PASSWORD";
    
    //for SOBrowseRBT
    public static final String RBT_MANAGE_OPTOUT = "dialog.optout_model";
    public static final String RBT_MANAGE_OPTIN = "dialog.optin_model";
    
    //For SOConfirmGotoCategories
    public static final String CONFIRM_GOTO_CATEGORIES_KEY="CONFIRM_GOTO_CATEGORIES";
    
    //For SODisablePressStar
    public static final String DISABLE_PRESS_STAR_KEY = "DISABLE_PRESS_STAR";
    	
    //SOGetOptInOptOut prompts
    //We have two modes of operation opt_in and opt_out.
    //What is your model. Please choose one from the following
    //for optin
    //for optout
    public static final String RBT_SOGETOPTINOUT_INTRO = "dialog.rbt_optin_intro";
    public static final String RBT_OPTIN_MAKE_CHOICE = "dialog.rbt_optin_make_choice";
    
    //Slots and Rules for OptInOptOut model
    public static final String GET_OPTINOPTOUT_RULENAME = "GetOptInOptOut";
    
    //ptompt for viral sms total 
    public static final String RBT_VIRAL_NOT_ALLOWED = "dialog.rbt_poentry_viral_sms_notallowed";
    
    //For TATA
    public static final String PREFIX="PREFIX";			/*ADDED FOR TATA*/
	public static final String CIRCLE_ID="CIRCLE_ID";	/*ADDED FOR TATA*/	
	public static final String DATABASE_MODE="DATABASE_MODE";	/*ADDED FOR TATA*/
	public static final String USER_TYPE="USER_TYPE";	/*ADDED FOR TATA*/
	public static final String SUBSCRIBER_STATUS="SUBSCRIBER_STATUS";	/*ADDED FOR TATA*/
	public static final String ALL_MUSICBOXES_KEY="ALL_MUSICBOXES_KEY";	/*ADDED FOR TATA*/
	public static final String ACTIVE_MUSICBOXES_KEY="ACTIVE_MUSICBOXES_KEY";	/*ADDED FOR TATA*/
	public static final String NEXT_MUSICBOX_KEY="NEXT_MUSICBOX_KEY";	/*ADDED FOR TATA*/
	public static final String ALL_SUBSCRIBER_SELECTIONS_KEY="ALL_SUBSCRIBER_SELECTIONS_KEY";	/*ADDED FOR TATA*/
	public static final String NEXT_SUBSCRIBER_SELECTION_KEY="NEXT_SUBSCRIBER_SELECTION_KEY";	/*ADDED FOR TATA*/
	public static final String PREVIOUS_SUBSCRIBER_SELECTION_KEY="PREVIOUS_SUBSCRIBER_SELECTION_KEY";	/*ADDED FOR TATA*/
	public static final String PRESENT_SUBSCRIBER_SELECTION_KEY="PRESENT_SUBSCRIBER_SELECTION_KEY";	/*ADDED FOR TATA*/
	public static final String ALL_SUSCRIBER_SELECTIONS_FROM_DB_KEY="ALL_SUSCRIBER_SELECTIONS_FROM_DB_KEY";	/*ADDED FOR TATA*/
	public static final String ALL_SUSCRIBER_DOWNLOADS_FROM_DB_KEY="ALL_SUSCRIBER_DOWNLOADS_FROM_DB_KEY";	/*ADDED FOR TATA*/
	public static final String CURRENT_WAV_FILE_KEY="CURRENT_WAV_FILE_KEY";	/*ADDED FOR TATA*/
	public static final String SUBSCRIBER_MYFAVORITE_SELECTION_KEY="SUBSCRIBER_MYFAVORITE_SELECTION_KEY";	/*ADDED FOR TATA*/
	public static final String DELETE_CLIP_FOR_NUMBER_KEY="DELETE_CLIP_FOR_NUMBER_KEY";	/*ADDED FOR TATA*/
	public static final String LOOPING_OPTION_KEY="LOOPING_OPTION_KEY";	/*ADDED FOR TATA*/
	public static final String DEFAULT_TUNE_KEY="DEFAULT_TUNE_KEY";	/*ADDED FOR TATA*/
	public static final String NEXT_PICK_KEY = "NEXT_PICK_KEY";
	public static final String PREVIOUS_PICK_KEY = "PREVIOUS_PICK_KEY";
	public static final String QUERY_WDS_TRUE_KEY = "QUERY_WDS_TRUE_KEY";
	public static final String CONFRIM_OVERWRITE_CS_KEY = "CONFRIM_OVERWRITE_CS_KEY";
	public static final String SUBSCRIBER_STATUS_RESPONSE_KEY="SUBSCRIBER_STATUS_RESPONSE_KEY";
//	public static final String PROMO_LIVE_KEY="PROMO_LIVE_KEY";
	public static final String FREE_DOWNLOAD_KEY="FREE_DOWNLOAD_KEY";
	public static final String LANG_MISMATCH_KEY="LANG_MISMATCH_KEY";
	public static final String SUB_WAV_FILE_KEY = "SUB_WAV_FILE_KEY";
	public static final String COS_KEY = "COS_KEY";
	public static final String CLIP_DEFAULT_SETTING_PENDING = "CLIP_DEFAULT_SETTING_PENDING";
	public static final String MB_DEFAULT_SETTING_PENDING = "MB_DEFAULT_SETTING_PENDING";
	public static final String NON_DEFAULT_TUNES = "NON_DEFAULT_TUNES"; 
    

	/*ADDED FOR TATA*/
	//sorry your activation request is not successful yet, plz callback later, for now lets go back to main menu
	public static final String RBT_BEFORE_OPEN_STATE = "dialog.rbt_before_open_state";
	//sorry, we have some technical difficulties, do callback later, for now lets go back to main menu
	public static final String RBT_TECHNICAL_DIFFICULTIES = "dialog.rbt_technical_difficulties";
	//your activation request is in progress, plz callback later.
	public static final String RBT_ACTIVATION_REQUEST_TAKEN = "dialog.rbt_activation_request_taken";
	//sorry your deactivation request is not successful yet, plz callback later, for now lets go back to main menu
	public static final String RBT_BEFORE_CLOSE_STATE = "dialog.rbt_before_close_state";
	//sorry your number is currently black listed, plz call customer care for other details
	public static final String RBT_BLACK_LISTED_USER = "dialog.rbt_black_listed_user";
	
	//sorry your account has been temporarily disabled, plz call customer care for other details
	public static final String RBT_SUSPENDED_USER = "dialog.rbt_suspended_user";
	//
	public static final String RBT_RENEWAL_PENDING_STATE = "dialog.rbt_renewal_pending_state";
	//
	public static final String RBT_SUSPENDED_STATE = "dialog.rbt_suspended_state";
	//jingle prompt
	public static final String RBT_JINGLE = "dialog.rbt_jingle";
	//Your request to set a welcome tune has been accepted, you will be intimated by an SMS abbout the result
	public static final String RBT_SET_SELECTION_SUCCESSFUL = "dialog.rbt_set_selection_successful";
//	Your request for free song has been accepted and you will receive confirmationvia SMS 
    public static final String RBT_SET_SELECTION_SUCCESSFUL_FREE = "dialog.rbt_set_selection_successful_free"; 
    public static final String RBT_FREE_SELECTION_DOWNLOADED = "dialog.rbt_free_selection_downloaded";
	//lets continue with the list
	public static final String RBT_CONTINUE_WITH_LIST = "dialog.rbt_continue_with_list";
	//Dear subscriber, we are glad to provide you with a free song along with the current selection.
	public static final String RBT_FREE_SELECTION = "dialog.rbt_free_selection";
	
	/*ADDED FOR TATA*/	
	
	public static final String DTMF_SET_SELECTION_KEY="DTMF_SET_SELECTION_KEY";
	public static final String DTMF_PRE_SET_SELECTION_KEY="DTMF_PRE_SET_SELECTION_KEY";
	public static final String SET_MUSICBOX_KEY="SET_MUSICBOX_KEY";
//	public static final String POST_SET_MUSICBOX_KEY="POST_SET_MUSICBOX_KEY";
	public static final String MY_FAVORITES_KEY="MY_FAVORITES_KEY";
	public static final String POST_MY_FAVORITES_KEY="POST_MY_FAVORITES_KEY";
	public static final String CONFIRM_DELETE_KEY="CONFIRM_DELETE_KEY";
	public static final String PERSONALIZE_KEY="PERSONALIZE_KEY";
//	public static final String SHUFFLE_KEY="SHUFFLE_KEY";
	public static final String GET_LOOPING_OPTION_KEY="GET_LOOPING_OPTION_KEY";
	public static final String CONFIRM_OVERWRITE_KEY="CONFIRM_OVERWRITE_KEY";
	public static final String CONFIRM_UNSUBSCRIBE_KEY="CONFIRM_UNSUBSCRIBE_KEY";
	public static final String ASK_TRANSITION_KEY="ASK_TRANSITION_KEY";
	public static final String PERSONALIZE_SETTING_KEY="PERSONALIZE_SETTING_KEY";
	public static final String CONFIRM_GIFTEE_NUMBER_KEY="CONFIRM_GIFTEE_NUMBER_KEY";
	public static final String CONFIRM_LOOP_SETTING_KEY="CONFIRM_LOOP_SETTING_KEY";
    public static final String CLIP_INFO_KEY="CLIP_INFO_KEY";
    public static final String OVERWRITE_BOOKMARK="OVERWRITE_BOOKMARK";
    public static final String BOOKMARKS_STATE_KEY="BOOKMARKS_STATE_KEY";
    public static final String REMOVE_BOOKMARK="REMOVE_BOOKMARK";
    public static final String CONFIRM_FREE_CLIP_KEY = "CONFIRM_FREE_CLIP_KEY";

	public static final String MUSICBOX_OBJ_KEY="MUSICBOX_OBJ_KEY";
	public static final String ALL_MUSICBOX_CLIPS_KEY="ALL_MUSICBOX_CLIPS";
	public static final String ACTIVE_MUSICBOX_CLIPS_KEY="ACTIVE_MUSICBOX_CLIPS";
	public static final String CLIPS_LIMIT_KEY="CLIPS_LIMIT_KEY";
	public static final String MUSICBOXES_LIMIT_KEY="MUSICBOXES_LIMIT_KEY";
	public static final String CLIPS_RESULT_FROM_BAK_END_KEY="CLIPS_RESULT_FROM_BAK_END_KEY";
	public static final String MUSICBOXES_RESULT_FROM_BAK_END_KEY="MUSICBOXES_RESULT_FROM_BAK_END_KEY";
	public static final String NO_OF_CLIPS_FROM_BAK_END_KEY="NO_OF_CLIPS_FROM_BAK_END_KEY";
	public static final String NO_OF_MUSICBOXES_FROM_BAK_END_KEY="NO_OF_MUSICBOXES_FROM_BAK_END_KEY";
	
	public static final String ALLOW_MORE_MUSICBOXES = "allow_more_musicboxes";/*ADDED FOR TATA*/
	public static final String NO_OF_MUSICBOXES_PER_SET = "no_of_musicboxes_per_set";/*ADDED FOR TATA*/
	public static final String ALLOW_MORE_CATEGORIES_ONLY_IN_DTMF = "allow_more_categories_only_in_dtmf";/*ADDED FOR TATA*/
	public static final String ALLOW_MORE_MUSICBOXES_ONLY_IN_DTMF = "allow_more_musicboxes_only_in_dtmf";/*ADDED FOR TATA*/
	public static final String ALLOW_HELP = "allow_help";/*ADDED FOR TATA*/
	
	/*SOBrowseMusicbox prompts*/
	/*ADDED FOR TATA*/
	//Select a musicbox to set a new caller tune by saying
	public static final String RBT_SOBROWSEMUSICBOX_INTRO_1 = "dialog.rbt_sobrowsemusicbox_intro_1";
	//To go to the next set of musicboxes, say more
	public static final String RBT_SOBROWSEMUSICBOX_INTRO_2 = "dialog.rbt_sobrowsemusicbox_intro_2";
	//To go to the next set of musicboxes
	public static final String RBT_SOBROWSEMUSICBOX_INTRO_3 = "dialog.rbt_sobrowsemusicbox_intro_3";
	//this musicbox is already in your personal library
	public static final String RBT_SOBROWSEMUSICBOX_PENDINGINPERSONALLIBRARY = "dialog.rbt_sobrowsemusicbox_pendinginpersonallibrary";
	//this musicbox is already in your personal library
	public static final String RBT_SOBROWSEMUSICBOX_ALREADYINPERSONALLIBRARY = "dialog.rbt_sobrowsemusicbox_alreadyinpersonallibrary";
	
	//Added by vinayasimha.patil
	public static final String RBT_VOICE_NSP = "dialog.rbt_voice_nsp";
	public static final String RBT_VOICE_REJ = "dialog.rbt_voice_rej";
	public static final String RBT_DTMF_NSP = "dialog.rbt_dtmf_nsp";
	public static final String RBT_DTMF_REJ = "dialog.rbt_dtmf_rej";
	//public static final String RBT_SOGETNEXTMOBILENUMBER_DTMF = "dialog.rbt_songnextmobilenumberdtmf";
	
	public static final String SLOT_MUSICBOX = "slot_musicbox";/*ADDED FOR TATA*/
	public static final String GET_MUSICBOX_RULENAME = "GetMusicboxRule";/*ADDED FOR TATA*/
	public static final String GET_PERSONALIZE_OPTION_RULENAME = "GetPersonalizeOptionRule";/*ADDED FOR TATA*/
	public static final String GET_POSTMYFAVORITES_OPTION_RULENAME = "GetPostMyFavoritesOptionRule";/*ADDED FOR TATA*/
	
	/*CallState variables*/	
	public static final String SUB_SUB_CATEGORIES_KEY = "SUB_SUB_CATEGORIES_KEY";
	public static final String PARENT_SUB_CATEGORY_KEY = "PARENT_SUB_CATEGORY_KEY";
	public static final String PARENT_SUB_SUB_CATEGORY_KEY = "PARENT_SUB_SUB_CATEGORY_KEY";
	//public static final String BROWSE_CLIPS_INTRO_FREQ = "BROWSE_CLIPS_INTRO_FREQ";
	
	/*SOConfirmContinuity prompts*/	
	//This wecome tune costs you Rs 10/-. The subscription to TATA welcome tune will be charged at Rs 30/- per month. This welcome tune will be set for the next 365 days to continue press1, to go back press*
	public static final String RBT_SOCONFIRMCONTINUITY_INTRO_CLIP = "dialog.rbt_soconfirmcontinuity_intro_clip";
	//This musicbox costs you Rs 30/-. The subscription to TATA welcome tune will be charged at Rs 30/- per month. This musicbox will be set for the next 365 days to continue press1, to go back press*
	public static final String RBT_SOCONFIRMCONTINUITY_INTRO_MUSICBOX = "dialog.rbt_soconfirmcontinuity_intro_musicbox";
	//You will be charged a fee of 15 per 15 days Rs for this service. Any additional tune will be charged at 10 Rs per tune. If you wish to continue, press 1
	public static final String RBT_SOCONFIRMCONTINUITY_PREPAID_INTRO = "dialog.rbt_soconfirmcontinuity_prepaid_intro";
	//This wecome tune costs you Rs 10/-. The subscription to TATA welcome tune will be charged at Rs 15/- per 15 days. This welcome tune will be set for the next 365 days to continue press1, to go back press*
	public static final String RBT_SOCONFIRMCONTINUITY_PREPAID_INTRO_CLIP = "dialog.rbt_soconfirmcontinuity_prepaid_intro_clip";
	//This musicbox costs you Rs 30/-. The subscription to TATA welcome tune will be charged at Rs 15/- per 15 days. This musicbox will be set for the next 365 days to continue press1, to go back press*
	public static final String RBT_SOCONFIRMCONTINUITY_PREPAID_INTRO_MUSICBOX = "dialog.rbt_soconfirmcontinuity_prepaid_intro_musicbox";
	
	/*ADDED FOR TATA*/
	/*SODTMFSetSelection prompts*/
	//This song costs you
	public static final String RBT_SODTMFPRESETSELECTION_INTRO1 = "dialog.rbt_sodtmfpresetselection_intro1";
	//This musicbox costs you
	public static final String RBT_SODTMFPRESETSELECTION_INTRO2 = "dialog.rbt_sodtmfpresetselection_intro2";
	//and will be set for next (in hindi --> set kardiya jaayega)
	public static final String RBT_SODTMFPRESETSELECTION_INTRO3 = "dialog.rbt_sodtmfpresetselection_intro3";
	//ise agley (in telugu --> deenini raaboye)(not needed in english needed only in regional languages)
	public static final String RBT_SODTMFPRESETSELECTION_INTRO4 = "dialog.rbt_sodtmfpresetselection_intro4";
	/*<promo download prompt>*/
	public static final String RBT_SODTMFPRESETSELECTION_INTRO1_PROMO = "dialog.rbt_sodtmfpresetselection_intro1_promo";
	//By downloading this song you are confirming to continue the service after the offer period. Your subscription charges will be Rs. 30/- per month
	public static final String RBT_SODTMFPRESETSELECTION_INTRO1_CONFIRMBULKACTIVATION_POST = "dialog.rbt_sodtmfpresetselection_intro1_confirmbulkactivation_post";
	//By downloading this song you are confirming to continue the service after the offer period. Your subscription charges will be Rs. 15/- for 15 days
	public static final String RBT_SODTMFPRESETSELECTION_INTRO1_CONFIRMBULKACTIVATION_PRE = "dialog.rbt_sodtmfpresetselection_intro1_confirmbulkactivation_pre";
	//By downloading this musicbox you are confirming to continue the service after the offer period. Your subscription charges will be Rs. 30/- per month
	public static final String RBT_SODTMFPRESETSELECTION_INTRO2_CONFIRMBULKACTIVATION_POST = "dialog.rbt_sodtmfpresetselection_intro2_confirmbulkactivation_post";
	//By downloading this musicbox you are confirming to continue the service after the offer period. Your subscription charges will be Rs. 15/- for 15 days
	public static final String RBT_SODTMFPRESETSELECTION_INTRO2_CONFIRMBULKACTIVATION_PRE = "dialog.rbt_sodtmfpresetselection_intro2_confirmbulkactivation_pre";
	//If you are the lucky subscriber it will be absolutely free . 
	public static final String RBT_SODTMFPRESETSELECTION_INTRO_BULKACTIVATION_FREEDOWNLOAD = "dialog.rbt_sodtmfpresetselection_intro_bulkactivation_freedownload";

	//If yes, press 1, else press 2
	public static final String RBT_CONFIRM_DTMF = "dialog.rbt_confirm_dtmf";
	/*Added for TATA*/ 
    /*SOConfirmFreeClip*/ 
    //You are now eligible for a free song. Here is your free song 
    public static final String RBT_SOCONFIRMFREECLIP_INTRO_1 = "dialog.rbt_soconfirmfreeclip_intro_1"; 
    //This free song is valid for 90 days. Post which Rs 10/- will be charged for 90 days and will be auto-renewed. Press 1 to confirm. 
    public static final String RBT_SOCONFIRMFREECLIP_INTRO_2 = "dialog.rbt_soconfirmfreeclip_intro_2"; 

	
	/*ADDED FOR TATA*/
	/*SOConfirmDelete prompts*/
	//you are going to delete this tune, all the settings attached to this tune will be automatically deleted,
	//to continue press1, to go back press*
	public static final String RBT_SOCONFIRMDELETE_INTRO = "dialog.rbt_soconfirmdelete_intro";
	//your request to delete a song has been accepted
	public static final String RBT_SOCONFIRMDELETE_CONFIRMATION = "dialog.rbt_soconfirmdelete_confirmation";

	/*ADDED FOR TATA*/
	/*SOConfirmUnsubscribe prompts*/
	//to unsubscribe form the TATA Indicom welcome tunes press1, to cancel and go back press*
	public static final String RBT_SOCONFIRMUNSUBSCRIBE_INTRO = "dialog.rbt_soconfirmunsubscribe_intro";

	/*ADDED FOR TATA*/
	/*SOPreSubscriberAskTransition prompts*/
	//you being already a subscriber we would like to present you with phata-phat offer...........
	public static final String RBT_SOPRESUBSCRIBERASKTRANSITION_INTRO = "dialog.rbt_sopresubscriberasktransition_intro";
	//press1 if u want to accept the offer, or if you want to continue with the old offer press2
	public static final String RBT_SOPRESUBSCRIBERASKTRANSITION_DTMF = "dialog.rbt_sopresubscriberasktransition_dtmf";
	
	/*ADDED FOR TATA*/
	/*SOMyFavorites prompts*/
	//my favourites jingle
	public static final String RBT_SOMYFAVORITES_JINGLE = "dialog.rbt_somyfavourites_jingle";
	//To see the option for the tune press2, anytime to goto next tune press3, to go to previous tune press1,
	//here is your first tune
	public static final String RBT_SOMYFAVORITES_INTRO = "dialog.rbt_somyfavourites_intro";
	//To see the option for this tune press2, to goto next tune press3, to go to previous tune press1,
	//to go back press*
	public static final String RBT_SOMYFAVORITES_BROWSE = "dialog.rbt_somyfavourites_browse";
	//you have no active selections, lets goback to manage now
	public static final String NO_ACTIVE_SELECTIONS_PROMPT = "dialog.rbt_somyfavourites_noactiveselections";
	//
	public static final String RBT_SOGETMANAGEOPTION_NO_BOOKMARKS = "dialog.rbt_sogetmanageoption_no_bookmarks";
	//this tune is your default welcome tune
	public static final String RBT_SOMYFAVORITES_DEFAULTTUNE = "dialog.rbt_somyfavourites_defaulttune";
	//this tune is set for following numbers
	public static final String RBT_SOMYFAVORITES_PERSONALTUNE = "dialog.rbt_somyfavourites_personaltune";
	//the next number set for the tune is 
	public static final String RBT_SOMYFAVORITES_NEXT_NUMBER_FOR_TUNE = "dialog.rbt_somyfavourites_next_number_for_tune";
	//this tune is not set for any of the numbers
	public static final String RBT_SOMYFAVORITES_ONLY_IN_LIBRARY = "dialog.rbt_somyfavourites_only_in_library";
	//this is a song
	public static final String RBT_CLIP = "dialog.rbt_clip";
	//this is a musicbox
	public static final String RBT_MUSICBOX = "dialog.rbt_musicbox";
	
	/*ADDED FOR TATA*/
	/*SOConfirmOverwrite prompts*/
	//you have reached the limit of clips in youy my favourites. You can manually delete the tunes from my favourite by going to manage option
	// form the welcome tune menu. Or if you want to set this tune by over writing your oldest welcome tune press2. to go back without setting this tune press* 
	public static final String RBT_SOCONFIRMOVERWRITE_INTRO_TONE = "dialog.rbt_soconfirmoverwrite_intro_tone";
	//you have reached the limit of musicboxes in youy my favourites. You can manually delete the tunes from my favourite by going to manage option
	// form the welcome tune menu. Or if you want to set this tune by over writing your oldest welcome tune press2. to go back without setting this tune press* 
	public static final String RBT_SOCONFIRMOVERWRITE_INTRO_MUSICBOX = "dialog.rbt_soconfirmoverwrite_intro_musicbox";
	//you have reached the limit of welcome tunes in youy my favourites. You can manually delete the welcome tunes from my favourite by going to manage option
	// form the welcome tune menu. Or if you want to set this tune by over writing your oldest welcome tune press2. to go back without setting this tune press* 
	public static final String RBT_SOCONFIRMOVERWRITE_INTRO = "dialog.rbt_soconfirmoverwrite_intro";

	/*ADDED FOR TATA*/
	/*SOShuffle prompts*/
	//we will set all the tunes and musicboxes in your my favorites as a shuffle welcome tune, to continue press1, to go back press*
	public static final String RBT_SOSHUFFLE_INTRO = "dialog.rbt_soshuffle_intro";
	//shuffle is your default welcometune now, lets go back to previous menu now
	public static final String RBT_SOSHUFFLE_CONFIRMATION = "dialog.rbt_soshuffle_confirmation";

	/*ADDED FOR TATA*/
	/*SOPersonalize prompts*/
	//to add a personal number for this tune press1, to delete any of the number for this tune press2, to listen to 
	//all the phone numbers for this tune press3, to go back press*
	public static final String RBT_SOPERSONALIZE_INTRO = "dialog.rbt_sopersonalize_intro";
	
	/*ADDED FOR TATA*/
	/*SODTMFBrowseClips prompts*/
	
	/*//To select any of the clip press2.....
	public static final String RBT_SODTMFBROWSECLIPS_INTRO_1 = "dialog.rbt_sodtmfbrowseclips_intro_1";
	//To select this clip press2, press1 to listen previous clip, press3 to listen next clip or press* to go to the previous menu
	public static final String RBT_SODTMFBROWSECLIPS_INTRO_2 = "dialog.rbt_sodtmfbrowseclips_intro_2";
	//To select this clip press2, press3 to listen next clip or press* to go to the previous menu
	public static final String RBT_SODTMFBROWSECLIPS_INTRO_3 = "dialog.rbt_sodtmfbrowseclips_intro_3";
	//finished all clips in the category again playing the first clip
	public static final String RBT_SODTMFBROWSECLIPS_PLAYINGFIRSTCLIPAGAIN = "dialog.rbt_sodtmfbrowseclips_playingfirstclipagain";*/
	
	//Sorry your currently do not have the capability to select more than one song per fortnight, lets move to the next song
	public static final String RBT_SODTMFBROWSECLIPS_CANNOTSELECT = "dialog.rbt_sodtmfbrowseclips_cannotselect";
	//this song is already in your personal library
	public static final String RBT_SODTMFBROWSECLIPS_ALREADYINPERSONALLIBRARY = "dialog.rbt_sodtmfbrowseclips_alreadyinpersonallibrary";
	//we will set this welcome tune after the expiry of your current welcome tune, which is 15 days from when u've set it
	public static final String RBT_SODTMFBROWSECLIPS_TOBESETAFTER = "dialog.rbt_sodtmfbrowseclips_tobesetafter";
	//this welcome tune is already in your personal library and the download is in progress
	public static final String RBT_SODTMFBROWSECLIPS_PENDINGINPERSONALLIBRARY = "dialog.rbt_sodtmfbrowseclips_pendinginpersonallibrary";
	//This welcome tune is already in your personal library and the tone deletion is pending.
	public static final String RBT_SODTMFBROWSECLIPS_DELETIONPENDING = "dialog.rbt_sodtmfbrowseclips_deletionpending";

	/*ADDED FOR TATA*/
	//You already have maximum selections in your library. You cannot add more welcome tunes at present
	public static final String RBT_SOPICKOFTHEDAY_MAXSELECTIONSFINISHED = "dialog.rbt_sopickoftheday_maxselectionsfinished";	
	//to set this as your welcome tune press1, to goto categories list press2
	public static final String RBT_SOPICKOFTHEDAY_SET = "dialog.rbt_sopickoftheday_set";
	

	public static final String STATE_TO_BE_ACTIVATED = "A";
	public static final String STATE_ACTIVATION_PENDING = "N";
	public static final String STATE_ACTIVATION_GRACE = "G";
	public static final String STATE_ACTIVATED = "B";
	public static final String STATE_EVENT = "O";//used in COS which resemble BULK ACTIVATION
	public static final String STATE_TO_BE_DEACTIVATED = "D";
	public static final String STATE_DEACTIVATION_PENDING = "P";
	public static final String STATE_DEACTIVATED = "X";
	public static final String STATE_BASE_ACTIVATION_PENDING = "W";//valid only for selections
	public static final String STATE_CHANGE = "C";
	public static final String STATE_REQUEST_RENEWAL = "R";
	public static final String STATE_ACTIVATION_ERROR = "E";
	public static final String STATE_DEACTIVATION_ERROR = "F";
	public static final String STATE_UN = "S";
	public static final String STATE_Y = "Y";
	public static final String STATE_SUSPENDED_INIT = "z";
	public static final String STATE_SUSPENDED = "Z";
	//added for separate player DB feature
	public static final String STATE_DEACTIVATED_INIT = "x";// user will have this state if he is deactive in SM and still active at player
	public static final String STATE_GRACE = "G";
	
	//Loop status'
	public static final char LOOP_STATUS_OVERRIDE_INIT = 'o';
	public static final char LOOP_STATUS_OVERRIDE = 'O';
	public static final char LOOP_STATUS_OVERRIDE_FINAL = 'B';
	public static final char LOOP_STATUS_LOOP_INIT = 'l';
	public static final char LOOP_STATUS_LOOP = 'L';
	public static final char LOOP_STATUS_LOOP_FINAL = 'A';
	public static final char LOOP_STATUS_EXPIRED_INIT = 'x';
	public static final char LOOP_STATUS_EXPIRED = 'X';
	public static final char LOOP_STATUS_ERROR = 'E';
	
	//Voice Presence Pack states

	public static final int PACK_BASE_ACTIVATION_PENDING = 30; //W
	public static final int PACK_SUSPENDED = 35; //Z

		
	//rbt user types
	public static final String USER_TYPE_RBT = "RBT";
	//public static final String USER_TYPE_ADD_RBT = "ADDRBT";
	public static final String USER_TYPE_SRBT = "SRBT";
	public static final String USER_TYPE_RRBT = "RRBT";
	public static final String USER_TYPE_PRE_CALL = "PRECALL";
	public static final String USER_TYPE_RBT_RRBT = "RBT+RRBT"; 
    public static final String USER_TYPE_SRBT_RRBT = "SRBT+RRBT"; 
	public static final String USER_TYPE_UNKNOWN = "UNKNOWN";
	public static final String USER_TYPE_VRBT = "VRBT";
	
	//rbt types
	public static final int TYPE_RBT = 0;
	public static final int TYPE_SRBT = 1;
	public static final int TYPE_RRBT = 2;
	public static final int TYPE_CORPORATE = 2;
	public static final int TYPE_RBT_RRBT = 3;
	public static final int TYPE_SRBT_RRBT = 4;
	
	//COPYSTAR - TRAI REFUND String Types
	public static final String REFUND 		= "REFUND";
	public static final String REFUNDED 	= "REFUNDED";
	public static final String REACTIVE 	= "REACTIVE";
	public static final String LOOP		 	= "LOOP";
	public static final String DEACT_REFID 	= "DEACT_REFID";
	public static final String EXTRA_INFO 	= "EXTRA_INFO";
	
	// VOLUTARY SUSPENSION
	public static final String VOLUNTARY 	= "VOLUNTARY";
	
	// COS Types
	public static final String SONG_PACK			= "SONG_PACK";
	public static final String UNLIMITED_DOWNLOADS	= "UNLIMITED_DOWNLOADS";
	public static final String PACK	= "PACK";
	public static final String LIMITED_DOWNLOADS	= "LIMITED_DOWNLOADS";
	public static final String LITE	= "LITE";
    public static final String MUSIC_POUCH = "MUSIC_POUCH";
    public static final String SUB_CLASS = "SUB_CLASS";
    public static final String UNLIMITED_DOWNLOADS_OVERWRITE	= "UNLIMITED_DOWNLOADS_OVERWRITE";
    public static final String LIMITED_SONG_PACK_OVERLIMIT	= "LIMITED_SONG_PACK_OVERLIMIT";
	public static final String AZAAN	= "AZAAN";
	

	/*ADDED FOR TATA*/
	/*SODTMFSetSelection prompts*/
	//To set this tune for default number press 1 to set for any personal number press2
	public static final String RBT_SODTMFSETSELECTION_INTRO = "dialog.rbt_sodtmfsetselection_intro";

	/*ADDED FOR TATA*/
	/*SOPersonalizeSetting prompts*/
	//To overwrite the default settings for all callers press 1, to set for any personal number press2
	public static final String RBT_SOPERSONALIZESETTING_INTRO = "dialog.rbt_sopersonalizesetting_intro";
	public static final String RBT_SOPERSONALIZESETTING_INTRO_1 = "dialog.rbt_sopersonalizesetting_intro_1";
	
	
	//sorry there are no categories to browse from, for now lets go to the main menu
	public static final String RBT_NO_ACTIVE_CATEGORIES = "dialog.rbt_no_active_categories";
	//<help text>
	public static final String RBT_SOGETCATEGORY_HELP = "dialog.rbt_sogetcategory_help";
	
//	for help say help
	public static final String RBT_HELP = "dialog.rbt_help";/*ADDED FOR TATA*/
	//for help
	public static final String RBT_HELP_DTMF = "dialog.rbt_help_dtmf";/*ADDED FOR TATA*/
	
//	for bookmarks say bookmark
	public static final String RBT_BOOKMARKS = "dialog.rbt_bookmarks";/*ADDED FOR TATA*/
	//for bookMark
	public static final String RBT_BOOKMARKS_DTMF = "dialog.rbt_bookmarks_dtmf";/*ADDED FOR TATA*/
	
	/*ADDED FOR TATA*/
	public static final String GSL_SOGETMANAGEOPTIONTATA_MYFAVOURITES = "gsl.sogetmanageoptionTATA.myfavourites";
	//public static final String GSL_SOGETMANAGEOPTIONTATA_SHUFFLE = "gsl.sogetmanageoptionTATA.shuffle";
	public static final String GSL_SOGETMANAGEOPTIONTATA_LOOP = "gsl.sogetmanageoptionTATA.loop";
	public static final String GSL_SOGETMANAGEOPTIONTATA_UNSUBSCRIBE = "gsl.sogetmanageoptionTATA.unsubscribe";
	public static final String GSL_SOGETMANAGEOPTIONVIRGIN_UNSUBSCRIBE = "gsl.sogetmanageoptionVIRGIN.unsubscribe";
	public static final String GSL_SOGETMANAGEOPTIONTATA_BOOKMARK = "gsl.sogetmanageoptionTATA.bookmark";
	public static final String GSL_SOGETMANAGEOPTIONVIRGIN_BOOKMARK = "gsl.sogetmanageoptionVIRGIN.bookmark";
	public static final String GSL_SOGETCATEGORY_HELP = "gsl.sogetcategory.help";
	public static final String GSL_SOGETCATEGORY_BOOKMARKS = "gsl.sogetcategory.bookmarks";
	
	/*SOGetLanguagePrompts*/
	
	//Whats your language preference? Say English or Hindi
	public static final String RBT_SOGETLANGUAGE_DTMF = "dialog.rbt_sogetlanguage_dtmf";
	
	//to subscribe to TATA welcome tunes you will be charged Rs.15/- per 15 days  
	public static final String RBT_POST_INTRO_PREPAID  = "dialog.rbt_post_intro_prepaid";
	//to subscribe to TATA welcome tunes you will be charged Rs.30/- per month
	public static final String RBT_POST_INTRO_POSTPAID  = "dialog.rbt_post_intro_postpaid";
	
	/*ADDED FOR TATA*/
	/*SOPostMyFavorites prompts*/
	//if you want to overwrite the previous setting press1 or if you want to add this tune in a loop press2, to go back press*
	public static final String RBT_SOGETLOOPINGOPTION_INTRO = "dialog.rbt_sogetloopingoption_intro";

	/*SOGetMobileNumber prompts*/	
	//please input the personal number you want to remove the setting for, please input the phone number and end with #, to go back press* 
	public static final String RBT_SOGETMOBILENUMBER_DELETE_INTRO = "dialog.rbt_sogetmobilenumber_delete_intro";
	
	public static final String NO_OF_CATEGORIES_PER_SET_TATA = "no_of_categories_per_set_tata";
	public static final String NO_OF_SUB_CATEGORIES_PER_SET = "no_of_sub_categories_per_set";
	public static final String NO_OF_SUB_SUB_CATEGORIES_PER_SET = "no_of_sub_sub_categories_per_set";
	
	//and
	public static final String RBT_AND = "dialog.rbt_and";	
	
	/*ADDED FOR TATA*/
	/*SOPostMyFavorites prompts*/
	//to set this tune as your default tune press1, to manage personal numbers for this tune press2, to delete this
	//tune from your my favorites press5, to go back press*
	public static final String RBT_SOPOSTMYFAVORITES_INTRO = "dialog.rbt_sopostmyfavourites_intro";
	//to manage personal numbers for this tune press2, to delete this tune from your my favorites press5, to go back press*
	public static final String RBT_SOPOSTMYFAVORITES_INTRO_1 = "dialog.rbt_sopostmyfavourites_intro_1";
	//to manage personal numbers for this tune press2, tomake this your default song and remove all the other songs from your default loop press 3
	//to delete this tune from your my favorites press5, to go back press*
	public static final String RBT_SOPOSTMYFAVORITES_INTRO_2 = "dialog.rbt_sopostmyfavourites_intro_2";
	//to add this tune in your default loop press1, to manage personal numbers for this tune press2, to delete this
	//tune from your my favorites press5, to go back press*
	public static final String RBT_SOPOSTMYFAVORITES_INTRO_3 = "dialog.rbt_sopostmyfavourites_intro_3";
	//To set this tune to as your default tune press 1.
	public static final String RBT_SOPOSTMYFAVORITES_INTRO_4 = "dialog.rbt_sopostmyfavourites_intro_4";
	//Your previous request is in pending.
	public static final String RBT_SOPOSTMYFAVORITES_INTRO_5 = "dialog.rbt_sopostmyfavourites_intro_5";
	//your request for deletion of the same welcome tunes is in progress, lets continue with the list
	public static final String RBT_SOPOSTMYFAVORITES_ALREADY_DELETED = "dialog.rbt_sopostmyfavourites_already_deleted";
	//
	public static final String RBT_SOPOSTMYFAVORITES_DELETED_DEFAULT_SETTINGS = "dialog.rbt_sopostmyfavorites_deleted_default_settings";
	
	public static final String GET_SUB_SUB_CATEGORY_KEY = "GET_SUB_SUB_CATEGORY_KEY";//ADDED FOR TATA
	
	//invalid prefix
	public static final String RBT_INVALID_PREFIX = "dialog.rbt_poentry_invalid_prefix";
	
	/*SOSetMusicbox prompts*/
	/*ADDED FOR TATA*/
	//here are the songs in the musicbox
	public static final String RBT_SOSETMUSICBOX_INTRO_1 = "dialog.rbt_sosetmusicbox_intro_1";
	//to set this musicbox press2 and to go back press*
	public static final String RBT_SOSETMUSICBOX_INTRO_2 = "dialog.rbt_sosetmusicbox_intro_2";

	public static final String OPERATOR_ACCOUNT = "OPERATOR_ACCOUNT";
	public static final String OPERATOR_PASSWORD = "OPERATOR_PASSWORD";
	public static final String PHONE_NUMBER = "PHONE_NUMBER";
	public static final String OPERATOR = "OPERATOR";
	public static final String NUMBER_OF_SUBSCRIBER_SELECTIONS="NUMBER_OF_SUBSCRIBER_SELECTIONS";

	public static final String OPERATOR_ACCOUNT_KEY = "OPERATOR_ACCOUNT_KEY";
	public static final String OPERATOR_PASSWORD_KEY = "OPERATOR_PASSWORD_KEY";
	public static final String PHONE_NUMBER_KEY = "PHONE_NUMBER_KEY";
	public static final String OPERATOR_KEY = "OPERATOR_KEY";
	public static final String SPECIAL_PHONE_KEY = "SPECIAL_PHONE_KEY";
	public static final String SET_TYPE_KEY = "SET_TYPE_KEY";
	public static final String START_TIME_KEY = "START_TIME_KEY";
	public static final String END_TIME_KEY = "END_TIME_KEY";
	public static final String TIME_TYPE_KEY = "TIME_TYPE_KEY";
	public static final String TONE_CODE_KEY = "TONE_CODE_KEY";
	public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
	public static final String FLAG_KEY = "FLAG_KEY";
	public static final String TONE_FLAG_KEY = "TONE_FLAG_KEY";

	//Prepaid/Postpaid welcome prompt	
	public static final String RBT_POSTPAID_WELCOME = "rbt_postpaid_welcome";
	public static final String RBT_PREPAID_WELCOME = "rbt_prepaid_welcome";
	
	/*ADDED FOR TATA*/
	//The subscription to TATA Welcome tune will cost you 30 rupees and charge for downloading this musicbox is 
	//10 rupees. This welcome tune will be set for the next 365 to continue press1 to go back press*
	public static final String RBT_SOPOSTSETMUSICBOX_INTRO_1 = "dialog.rbt_sopostsetmusicbox_intro_1";
	//This musicbox costs 10 rupees, and will be set for the next 365 days. To continue press1 and to go back press*
	public static final String RBT_SOPOSTSETMUSICBOX_INTRO_2 = "dialog.rbt_sopostsetmusicbox_intro_2";

	//YOUR SHUFFLE REQUEST IS IN PROCESS
	public static final String RBT_SOGETMANAGEOPTION_SUCCESS_SHUFFLE = "dialog.rbt_sogetmanageoption_success_shuffle";
	
	/*SOConfirmLoopSetting prompts */ 
    // 
    public static final String RBT_SOCONFIRMLOOPSETTINNG_INTRO = "dialog.rbt_soconfirmloopsettinng_intro"; 
    // 
    public static final String RBT_SOCONFIRMLOOPSETTINNG_SUCCESSFUL = "dialog.rbt_soconfirmloopsettinng_successful"; 

	
	public static final int RBT_THREAD_PRIMARY = 0;
	public static final int RBT_THREAD_SECONDARY = 1;

	//FOR TATA-RBT-DAEMON
	public static final int ACTIVATION_TASK = 1;
	public static final int DEACTIVATION_TASK = 2;
	public static final int ACTIVATION_POLLING_TASK_POSTPAID = 3;
	public static final int ACTIVATION_POLLING_TASK_PREPAID = 4;
	public static final int DEACTIVATION_POLLING_TASK_POSTPAID = 5;
	public static final int DEACTIVATION_POLLING_TASK_PREPAID = 6;
	public static final int ADD_SETTING_TASK_POSTPAID = 7;
	public static final int ADD_SETTING_TASK_PREPAID = 8;
	public static final int DELETE_SELECTION_TASK = 9;
	public static final int DELETE_SETTING_TASK = 10;
	public static final int UPDATE_TO_DEACTIVATE = 11;
	public static final int UPDATE_TO_TO_BE_DELETED = 12;
	public static final int ACTIVATION_GRACE_TASK = 13;
	public static final int SELECTION_GRACE_TASK = 14;

	public static String PRE_PAID = "PRE_PAID";
	public static String POST_PAID="POST_PAID";
	public static String H = "H";
	
	public static final int SHUFFLE = 0;
	public static final int LIST = 1;
	public static final int SOUNDS = 2;
	public static final int BOUQUET = 3;
	public static final int RECORD = 4;
	public static final int SONGS = 5;
	public static final int PARENT = 6;
	public static final int DTMF_CATEGORY = 7;
	public static final int KARAOKE = 8;
	public static final int INFO_CATEGORY = 9;
	public static final int WEEKLY_SHUFFLE = 9;
	public static final int DAILY_SHUFFLE = 10;
	public static final int DYNAMIC_SHUFFLE = 11;
	public static final int MONTHLY_SHUFFLE = 12;
	public static final int TIME_OF_DAY_SHUFFLE = 13;
	public static final int FEED_CATEGORY = 15;
	public static final int ODA_SHUFFLE = 16;
	public static final int BOX_OFFICE_SHUFFLE = 17;
	public static final int FESTIVAL_SHUFFLE = 18;
	public static final int FEED_SHUFFLE = 19;
	public static final int MONTHLY_ODA_SHUFFLE = 20;
	/**
	 * Not actually a shuffle. So not including this category_type in
	 * Utility.isShuffleCategory() method.
	 */
	public static final int AUTO_DOWNLOAD_SHUFFLE = 21;
	public static final int OVERRIDE_MONTHLY_SHUFFLE = 22;
	
	public static final int PHONE_RADIO_SHUFFLE = 23;
	public static final int FESTIVAL_NAMETUNES_SHUFFLE = 27;
	public static final int PLAYLIST_ODA_SHUFFLE = 33;
	
	public static final String CONFIRM_STAR_RULENAME = "Confirm_STAR";
	public static final String GET_MOBILE_NO_DTMF_RULENAME = "SimpleDigitString_DTMF";
	
	public static final String UGC_CATEGORY_EXIST = "UGC_CATEGORY_EXIST";
	public static final String UGC_REVERSE_ORDER = "UGC_REVERSE_ORDER"; 
    
    /*SoGetCategory and SoGetSubCategory prompts*/ 
    public static final String RBT_NOCATEGORY = "dialog.rbt_nocategory"; 
    
    public static final String CORP_ID = "CORP_ID";
    public static final String GIFT_KEY = "GIFT_KEY";
    public static final String BROWSE_CLIP_GRAMMAR_RULENAME = "BrowseClipGrammarRule";
    public static final String RBT_POENTRY_GIFTING_PENDING = "dialog.rbt_poentry_gifting_pending";
    //Dear subscriber, your express copy request is in pending, pls call after some time 
    public static final String RBT_POENTRY_EXPRESS_COPY_PENDING = "dialog.rbt_poentry_express_copy_pending";
    public static final String CLIP_INFO_GRAMMAR_RULENAME = "ClipInfoGrammarRule";
  
    public static final String RBT_SODTMFBROWSECLIPS_INTRO_4 = "dialog.rbt_sodtmfbrowseclips_intro_4";
    
    public static final String RBT_SODTMFBROWSECLIPS_INTRO_5 = "dialog.rbt_sodtmfbrowseclips_intro_5";
    
    public static final String RBT_SODTMFBROWSECLIPS_INTRO_6 = "dialog.rbt_sodtmfbrowseclips_intro_6";
    
    public static final String RBT_GOBACK_PRESS_STAR = "dialog.rbt_goback_press_star"; 
  
    public static final String RBT_SOGETMOBILENUMBER_INVALID_GIFT_SELF = "dialog.rbt_sogetmobilenumber_invalid_gift_self"; 
 
    public static final String RBT_SOGETMOBILENUMBER_REPEAT_GIFT = "dialog.rbt_sogetmobilenumber_repeat_gift"; 
  
    public static final String RBT_SOGETMOBILENUMBER_GIFT = "dialog.rbt_sogetmobilenumber_gift"; 
 
    public static final String RBT_SOGETMOBILENUMBER_GIFT_ERROR = "dialog.rbt_sogetmobilenumber_gift_error"; 

    public static final String RBT_SOCONFIRMMOBILENUMBER_SONG_ALREADY_EXIST = "dialog.rbt_soconfirmmobilenumber_song_already_exist"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTEE_PESONAL_LIB_FULL = "dialog.rbt_soconfirmmobilenumber_giftee_pesonal_lib_full"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTEE_BLACKLISTED = "dialog.rbt_soconfirmmobilenumber_giftee_blacklisted"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTEE_SUSPENDED = "dialog.rbt_soconfirmmobilenumber_giftee_suspended"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_INVALID_GIFTEE_NUMBER = "dialog.rbt_soconfirmmobilenumber_invalid_giftee_number"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTEE_ACT_PENDING = "dialog.rbt_soconfirmmobilenumber_giftee_act_pending"; 
 
    public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTEE_DCT_PENDING = "dialog.rbt_soconfirmmobilenumber_giftee_dct_pending"; 
    //public static final String GIFTEE_ACTIVATED = "GIFTEE_ACTIVATED"; 
    public static final String GIFTEE_TYPE = "GIFTEE_TYPE"; 

    public static final String RBT_SOGETNEXTMOBILENUMBER_DTMF = "dialog.rbt_sogetnextmobilenumber_dtmf"; 
 
    //public static final String RBT_SOGETNEXTMOBILENUMBER_GIFT_INTRO = "dialog.rbt_sogetnextmobilenumber_gift_intro"; 
 
    public static final String RBT_SOGETNEXTMOBILENUMBER_GIFT_DTMF = "dialog.rbt_sogetnextmobilenumber_gift_dtmf";
    
    public static final String RBT_GIFT_SELECTION_SUCCESSFUL = "dialog.rbt_gift_selection_successful";
    
    /*SOGift prompts*/ 
    // 
    public static final String RBT_SOGIFT_SERVICE_PREPAID = "dialog.rbt_sogift_service_prepaid"; 
    // 
    public static final String RBT_SOGIFT_SERVICE_POSTPAID = "dialog.rbt_sogift_service_postpaid"; 
    // 
    public static final String RBT_SOGIFT_SONG = "dialog.rbt_sogift_song"; 
    // 
    public static final String RBT_SOGIFT_CONFIRM = "dialog.rbt_sogift_confirm"; 
    // 
    public static final String RBT_SOGIFT_GIFTING_FAILED = "dialog.rbt_sogift_gifting_failed"; 

    public static final String RBT_GIFT_KEY="RBT_GIFT_KEY"; 

//  FOR AD RBT 
    public static final String SELECTION_ADRBT_NOTALLOWED_ = "SELECTION_ADRBT_NOTALLOWED";
    public static final String SELECTION_OVERLIMIT = "SELECTION_OVERLIMIT";
    public static final String PERSONAL_SELECTION_OVERLIMIT = "PERSONAL_SELECTION_OVERLIMIT";
    public static final String LOOP_SELECTION_OVERLIMIT = "LOOP_SELECTION_OVERLIMIT";
    public static final String REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED = "REACTIVATION_WITH_SAME_SONG_NOT_ALLOWED";

    /* Sorry you cannot add selection for all users since you are an ad rbt subscriber. Please choose a different caller ID*/ 
    public static final String RBT_ADRBT_ALL_REJECT = "dialog.rbt_adrbt_all_reject"; 

    /*This selection is not allowed as you are an ADRBT subscriber*/ 
    public static final String RBT_ADRBT_REJECT = "dialog.rbt_adrbt_reject"; 

    /*Please enter a caller number for whom you want to add this selection*/ 
    public static final String RBT_ENTER_CALLERID = "dialog.rbt_enter_callerid"; 

//  Since you are corporate subscriber, you cannot set Caller Tune for ALL
    public static final String RBT_CORPORATE_NOTALLOWSELECTION = "dialog.rbt_corporate_notallowselection";
    
    /*SOClipInfo prompts */
	//
	public static final String RBT_SOCLIPINFO_INTRO = "dialog.rbt_soclipinfo_intro";
    
    /*SOAskLoopOverwrite prompt */
    //For your selected caller number, we are going to overwrite your old selection. Do you want to continue
    public static final String RBT_SOASKLOOPOVERWRITE_INTRO = "dialog.rbt_soaskloopoverwrite_intro";
    //For your selected caller number ,do you want to set this tune for all? If you don't put the song in loop the existing song will be lost
    public static final String RBT_SOASKLOOPOVERWRITE_INTRO1 = "dialog.rbt_soaskloopoverwrite_intro1";
   // for your selected caller number,We are going to overwrite your earlier shuffle selection , Do you want to continue
    public static final String RBT_SOASKLOOPOVERWRITE_INTRO2 = "dialog.rbt_soaskloopoverwrite_intro2";
    //by adding this tune you will cross max selection limit.first goto selection and remove one or more tunes for this caller number.
    public static final String RBT_SOASKLOOPOVERWRITE_GOTO_MANAGE_SELECTION= "dialog.rbt_soaskloopoverwrite_intro3";
//    That was an invalid input .for your selected caller number,We are going to overwrite your old selections,
//    to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_REJ="dialog.rbt_soaskloopoverwrite_rej";
//    I didn't hear you.for your selected caller number,We are going to overwrite your old selections,
//    to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_NSP="dialog.rbt_soaskloopoverwrite_nsp";
// That was an invalid input.for your selected caller number ,Do you want to add this tune in loop? If you don't put the song in loop the existing song will be lost
//  to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_REJ_1="dialog.rbt_soaskloopoverwrite_rej_1";
//    I didn't hear you.for your selected caller number ,Do you want to add this tune in loop? If you don't put the song in loop the existing song will be lost
//    to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_NSP_1="dialog.rbt_soaskloopoverwrite_nsp_1";
    // That was an invalid input.for your selected caller number,We are going to overwrite your earlier shuffle selection , 
//  to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_REJ_2="dialog.rbt_soaskloopoverwrite_rej_2";
    // I didn't hear you.for your selected caller number,We are going to overwrite your earlier shuffle selection , 
//    to continue press 1 else press 2
    public static final String RBT_SOASKLOOPOVERWRITE_NSP_2="dialog.rbt_soaskloopoverwrite_nsp_2";
   /*VoiceUtil*/
    
//  by adding this tune you will cross max selection limit.first goto selection and remove one or more tunes for this caller number.
    public static final String RBT_SOASKLOOPOVERWRITE_GOTO_MANAGE="rbt_soaskloopoverwrite_intro3";
    
    /*POPlayPassword*/
//    Your password is
    public static final String RBT_PLAYPASSWORD_INIT1="dialog.rbt_poplaypassword_intro1";
//  Now lets go back to categories
    public static final String RBT_PLAYPASSWORD_INIT2="dialog.rbt_poplaypassword_intro2";
//  You dont have a password now.Log on to www.xxx.com and set a password
    public static final String RBT_PLAYPASSWORD_INIT3="dialog.rbt_poplaypassword_intro3";
//    We are not able to retrieve your password now. Please try later. 
    public static final String RBT_PLAYPASSWORD_UNABLE_TO_PROCESS="dialog.rbt_poplaypassword_unable_to_process";
//  a
    public static final String RBT_PLAYPASSWORD_A="dialog.rbt_poplaypassword_a";
//  b
    public static final String RBT_PLAYPASSWORD_B="dialog.rbt_poplaypassword_b";
//  c
    public static final String RBT_PLAYPASSWORD_C="dialog.rbt_poplaypassword_c";
//  d
    public static final String RBT_PLAYPASSWORD_D="dialog.rbt_poplaypassword_d";
//  e
    public static final String RBT_PLAYPASSWORD_E="dialog.rbt_poplaypassword_e";
//  f
    public static final String RBT_PLAYPASSWORD_F="dialog.rbt_poplaypassword_f";
//  g
    public static final String RBT_PLAYPASSWORD_G="dialog.rbt_poplaypassword_g";
//  h
    public static final String RBT_PLAYPASSWORD_H="dialog.rbt_poplaypassword_h";
//  i
    public static final String RBT_PLAYPASSWORD_I="dialog.rbt_poplaypassword_i";
//  j
    public static final String RBT_PLAYPASSWORD_J="dialog.rbt_poplaypassword_j";
//  k
    public static final String RBT_PLAYPASSWORD_K="dialog.rbt_poplaypassword_k";
//  l
    public static final String RBT_PLAYPASSWORD_L="dialog.rbt_poplaypassword_l";
//  m
    public static final String RBT_PLAYPASSWORD_M="dialog.rbt_poplaypassword_m";
//  n
    public static final String RBT_PLAYPASSWORD_N="dialog.rbt_poplaypassword_n";
//  o
    public static final String RBT_PLAYPASSWORD_O="dialog.rbt_poplaypassword_o";
//  p
    public static final String RBT_PLAYPASSWORD_P="dialog.rbt_poplaypassword_p";
//  q
    public static final String RBT_PLAYPASSWORD_Q="dialog.rbt_poplaypassword_q";
//  r
    public static final String RBT_PLAYPASSWORD_R="dialog.rbt_poplaypassword_r";
//  s
    public static final String RBT_PLAYPASSWORD_S="dialog.rbt_poplaypassword_s";
//  t
    public static final String RBT_PLAYPASSWORD_T="dialog.rbt_poplaypassword_t";
//  u
    public static final String RBT_PLAYPASSWORD_U="dialog.rbt_poplaypassword_u";
//  v
    public static final String RBT_PLAYPASSWORD_V="dialog.rbt_poplaypassword_v";
//  w
    public static final String RBT_PLAYPASSWORD_W="dialog.rbt_poplaypassword_w";
//  x
    public static final String RBT_PLAYPASSWORD_X="dialog.rbt_poplaypassword_x";
//  y
    public static final String RBT_PLAYPASSWORD_Y="dialog.rbt_poplaypassword_y";
//  z
    public static final String RBT_PLAYPASSWORD_Z="dialog.rbt_poplaypassword_z";
//  0
//    public static final String RBT_PLAYPASSWORD_0="dialog.zero";
////  1
//    public static final String RBT_PLAYPASSWORD_1="dialog.1";
////  2
//    public static final String RBT_PLAYPASSWORD_2="dialog.2";
////  3
//    public static final String RBT_PLAYPASSWORD_3="dialog.3";
////  4
//    public static final String RBT_PLAYPASSWORD_4="dialog.4";
////  5
//    public static final String RBT_PLAYPASSWORD_5="dialog.5";
////  6
//    public static final String RBT_PLAYPASSWORD_6="dialog.6";
////  7
//    public static final String RBT_PLAYPASSWORD_7="dialog.7";
////  8
//    public static final String RBT_PLAYPASSWORD_8="dialog.8";
////  9
//    public static final String RBT_PLAYPASSWORD_9="dialog.9";
    
    /*SOASKPassword*/
    //CallState key
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String NEW_PASSWORD = "NEW_PASSWORD";
    public static final String COUNT_ASKPASSWORD = "COUNT_ASKPASSWORD";
    //Please enter your 8 digit password, or to goback category categories
    public static final String RBT_ASKPASSWORD_INTRO_1 = "dialog.rbt_askpassword_intro_1";
    //Please enter your 8 digit password by using key  pad or go back gategory press *
    public static final String RBT_ASKPASSWORD_ERROR_1 = "dialog.rbt_askpassword_error_1";
    //Do you want to change your password, say change or go back gategory say category
    public static final String RBT_ASKPASSWORD_CHANGE = "dialog.rbt_askpassword_change";
    //Do you want to change you password, press 1 or go back category press *
    public static final String RBT_ASKPASSWORD_CHANGE_DTMF = "dialog.rbt_askpassword_change_dtmf";
    //Please Enter your 8 digit old password, gotoCategories say category
    public static final String RBT_ASKPASSWORD_INTRO_OLD = "dialog.rbt_askpassword_intro_old";
    //Please Enter your 8 digit old password by useing your phone key pad, gotocategory press *
    public static final String RBT_ASKPASSWORD_DTMF_OLD = "dialog.rbt_askpassword_dtmf_old";
    //Please Enter your 8 digit new password, gotoCategories say category
    public static final String RBT_ASKPASSWORD_INTRO_NEW = "dialog.rbt_askpassword_intro_new";
    //Please Enter your 8 digit new password by useing your phone key pad, gotocategory press *
    public static final String RBT_ASKPASSWORD_DTMF_NEW = "dialog.rbt_askpassword_dtmf_new";
    //Password is wrong
    public static final String RBT_ASKPASSWORD_NOCORRECT_PASSWORD = "dialog.rbt_askpassword_nocorrect_password";
    //
    public static final String RBT_SOCONFIRMOVERWRITEBOOKMARK_INTRO = "dialog.rbt_soconfirmoverwritebookmark_intro";
    
    public static final String BROWSE_BOOKMARK_GRAMMAR_RULENAME = "BrowseBookMarkGrammarRule";
    //
    public static final String RBT_SOBOOKMARKS_JINGLE = "dialog.rbt_sobookmarks_jingle";
    //
    public static final String RBT_SOBOOKMARKS_INTRO_1 = "dialog.rbt_sobookmarks_intro_1";
    //
    public static final String RBT_SOBOOKMARKS_INTRO_2 = "dialog.rbt_sobookmarks_intro_2";
    //
    public static final String RBT_SOBOOKMARKS_PLAYINGFIRSTBOOKMARKAGAIN = "dialog.rbt_sobookmarks_playingfirstbookmarkagain";
    
    public static final String SUBSCRIBER_BOOKMARKS_KEY="SUBSCRIBER_BOOKMARKS_KEY";
    
    //
    public static final String RBT_SOREMOVEBOOKMARK_INTRO = "dialog.rbt_soremovebookmark_intro";
    //
    public static final String RBT_SOREMOVEBOOKMARK_SUCCESSFUL = "dialog.rbt_soremovebookmark_successful";
    
    public static final String SUBSCRIBER_KEY = "SUBSCRIBER_KEY";
    
    public static final String RBT_SODTMFBROWSECLIPS_BOOKMARK_SET = "dialog.rbt_sodtmfbrowseclips_bookmark_set";
    
    public static final String RBT_WELCOME_LONG  = "dialog.rbt_welcome_long";  
    
    public static final String RBT_WELCOME_SHORT  = "dialog.rbt_welcome_short";
    
    public static final String BOOKMARK_SET_KEY = "BOOKMARK_SET_KEY";
    
    public static final String GOTO_MAINMENU = "GOTO_MAINMENU";
    
    public static final String PROMOTION_TYPE = "PROMOTION_CATGORY_TYPE";
    
    public static final String ASK_DEFAULT_OR_WTPacks = "askDefaultorWTPacks";
    
    public static final String ASK_DEFAULT_OR_WTPacks_KEY="ASK_DEFAULT_OR_WTPacks_KEY";
    public static final String WTPacksConfirmContinuity_key="WTPacksConfirmContinuity_key";
    public static final String FEED="FEED";
    public static final String CRICKET="CRICKET";
    public static final String UGCFILE="UGCFILE";
    

	/*ADDED FOR TATA*/
	/*SOAskDefaultORWTPacks*/
//	for longer validity packs press 2.
	public static final String RBT_SOASKDEFAULTORWTPACKS_INTRO = "dialog.rbt_soaskdefaultorwtpacks_intro";
	
	/*SOWTPacksConfirmContinuity*/

//	please choose among the following options
	public static final String SOWTPACKSCONFIRM_INTRO="dialog.rbt_sowtpacksconfirm_intro";
//	to select this press 1
	public static final String RBT_SELECT_1="dialog.rbt_select_1";
//	to select this press 2
	public static final String RBT_SELECT_2="dialog.rbt_select_2";
//	to select this press 3
	public static final String RBT_SELECT_3="dialog.rbt_select_3";
//	to select this press 4
	public static final String RBT_SELECT_4="dialog.rbt_select_4";
//	to select this press 5
	public static final String RBT_SELECT_5="dialog.rbt_select_5";
//	to select this press 6
	public static final String RBT_SELECT_6="dialog.rbt_select_6";
//	to select this press 7
	public static final String RBT_SELECT_7="dialog.rbt_select_7";
//	to select this press 8
	public static final String RBT_SELECT_8="dialog.rbt_select_8";
//	to select this press 9
	public static final String RBT_SELECT_9="dialog.rbt_select_9";
	
	public static final String CONFIRM_UNSUBSCRIPTION_KEY="CONFIRM_UNSUBSCRIPTION_KEY";
	public static final String CONFIRM_ADVANCE_RENTAL_KEY="CONFIRM_ADVANCE_RENTAL_KEY";
	public static final String ASK_FOR_GIFT_INBOX_KEY="ASK_FOR_GIFT_INBOX_KEY";
	public static final String GIFT_INBOX_KEY = "GIFT_INBOX_KEY";
	public static final String DELETE_GIFT_KEY = "DELETE_GIFT_KEY";
	public static final String MAINMENU_KEY = "MAINMENU_KEY";
	public static final String CRICKET_KEY = "CRICKET_KEY";
	public static final String SET_CRICKET_KEY = "SET_CRICKET_KEY";
	public static final String REMOVE_CRICKET_KEY = "REMOVE_CRICKET_KEY";
	public static final String PLAY_COPY_TUNE_KEY = "PLAY_COPY_TUNE_KEY";
	public static final String PROFILE_KEY = "PROFILE_KEY";
	public static final String UNSUBSCRIBE_KEY = "UNSUBSCRIBE_KEY";
	public static final String EVERYONE_PROFILE_KEY = "EVERYONE_PROFILE_KEY";
	public static final String CONFIRM_DELETE_SETTING_KEY = "CONFIRM_DELETE_SETTING_KEY";
	public static final String POST_DELETE_EVERYONE_SETTING_KEY = "POST_DELETE_EVERYONE_SETTING_KEY";
	public static final String SPECIAL_PROFILE_KEY = "SPECIAL_PROFILE_KEY";
	public static final String DELETE_SPECIAL_SETTING_KEY = "DELETE_SPECIAL_SETTING_KEY";
	public static final String POST_DELETE_SPECIAL_SETTING_KEY = "POST_DELETE_SPECIAL_SETTING_KEY";
	public static final String BROWSE_CATEGORIES_KEY = "BROWSE_CATEGORIES_KEY";
	public static final String CONFIRM_ALBUM_OVERWRITE_KEY = "CONFIRM_ALBUM_OVERWRITE_KEY";
	public static final String SPECIAL_SETTINGS_FULL_KEY = "SPECIAL_SETTINGS_FULL_KEY";
	public static final String SPECIAL_SETTING_EXIST_KEY = "SPECIAL_SETTING_EXIST_KEY";
	public static final String CONFIRM_GIFT_KEY = "CONFIRM_GIFT_KEY";
	public static final String POST_SETTING_KEY = "POST_SETTING_KEY";
	public static final String OTHER_OPTIONS_KEY = "OTHER_OPTIONS_KEY";
	public static final String HELP_KEY = "HELP_KEY";
	public static final String RETAILER_KEY = "RETAILER_KEY";
	public static final String ADVANCE_RENTAL_KEY = "ADVANCE_RENTAL_KEY";
	public static final String SPECIAL_PACKS_KEY="SPECIAL_PACKS_KEY";
	public static final String INTERNATIONAL_ROAMING_KEY = "INTERNATIONAL_ROAMING_KEY";
	public static final String PERSONALIZED_HELLOTUNES_KEY = "PERSONALIZED_HELLOTUNES_KEY";
	public static final String TNB_MAINMENU_KEY = "TNB_MAINMENU_KEY";
	public static final String GET_GENDER_KEY = "GET_GENDER_KEY";
	public static final String GET_AGE_KEY = "GET_AGE_KEY";
	public static final String ALBUM_INTRO_KEY = "ALBUM_INTRO_KEY";
	public static final String BROWSE_ALBUM_KEY = "BROWSE_ALBUM_KEY";
	public static final String ALBUMS_KEY = "ALBUMS_KEY";
	public static final String ALBUM_PREVIEW_KEY = "ALBUM_PREVIEW_KEY";
	public static final String SET_ALBUM_KEY = "SET_ALBUM_KEY";
	public static final String ALBUM_PROFILE_KEY = "ALBUM_PROFILE_KEY";
	public static final String UNSUBSCRIBE_ALBUM_KEY = "UNSUBSCRIBE_ALBUM_KEY";
	public static final String POST_UNSUBSCRIBE_ALBUM_KEY = "POST_UNSUBSCRIBE_ALBUM_KEY";
	public static final String GET_NUMBER_KEY = "GET_NUMBER_KEY";
	public static final String CONFIRM_NUMBER_KEY = "CONFIRM_NUMBER_KEY";
	public static final String SELECTION_MODE = "SELECTION_MODE";
	public static final String AUTO_UPGRADE_MODE = "AUTO_UPGRADE_MODE";
	
	public static final String SELECTION_STATUS = "SELECTION_STATUS";
	public static final String GIFT_OBJ = "GIFT_OBJ";
	public static final String SELECTED_BY = "SELECTED_BY";
	public static final String SELECTION_INFO = "SELECTION_INFO";
	public static final String CATEGORY_BROWSE_STACKTRACE = "CATEGORY_BROWSE_STACKTRACE";
	public static final String SUBSCRIBER_SELECTIONS = "SUBSCRIBER_SELECTIONS";
	public static final String COPY_KEY = "COPY_KEY";
	public static final String PICK_OF_THE_DAY_MODE = "PICK_OF_THE_DAY_MODE";
	public static final String NEXT_SETTING_KEY = "NEXT_SETTING_KEY";
	public static final String COPY_TUNE = "COPY_TUNE";
	public static final String ALL_GIFTS_KEY = "ALL_GIFTS_KEY";
	public static final String NEXT_GIFT_KEY = "NEXT_GIFT_KEY";
	public static final String ALLOW_GIFTING = "ALLOW_GIFTING";
	public static final String PLAY_ACTIVATION_PROMPT = "PLAY_ACTIVATION_PROMPT";
	public static final String HOT_SONG_TYPE = "HOT_SONG_TYPE";
	public static final String MORE_OPTIONS = "MORE_OPTIONS";
	public static final String HOTSONG_OF_THE_DAY = "HOTSONG_OF_THE_DAY";
	public static final String PICK_OF_THE_DAY= "PICK_OF_THE_DAY";
	public static final String CATEGORY_CALLBACK = "CATEGORY_CALLBACK";
	public static final String DTMF_CATEGORY_CALLBACK = "DTMF_CATEGORY_CALLBACK";
	public static final String HANGUP_POST_SETTING = "HANGUP_POST_SETTING";
	public static final String VP = "IVR";
	public static final String CRICKET_FEED = "CRICKET_FEED";
	public static final String CRICKET_SCHEDULE = "CRICKET_SCHEDULE";
	public static final String COPY_SETTING = "COPY_SETTING";
	public static final String COPY_NUMBER = "COPY_NUMBER";
	public static final String TNB_CATEGORY ="TNB_CATEGORY" ;
	public static final String TNB_ACTIVATION ="TNB_ACTIVATION";
	public static final String ALBUM_USER ="ALBUM_USER";
	public static final String DONT_PLAY_ALBUM_INTRO ="DONT_PLAY_ALBUM_INTRO";
	public static final String ALBUM_CATEGORIES ="ALBUM_CATEGORIES";
	public static final String ALBUMS ="ALBUMS";
	public static final String NEXT_ALBUM_KEY ="NEXT_ALBUM_KEY";
	public static final String SCRATCHCARD_ACTIVATION ="SCRATCHCARD_ACTIVATION";
	public static final String GET_NUMBER_CONTEXT ="GET_NUMBER_CONTEXT";
	public static final String NUMBER ="NUMBER";
	public static final String MAX_DTMF_CLIPS ="MAX_DTMF_CLIPS";
	public static final String MAX_SCRATCHCARD_ACCESS ="MAX_SCRATCHCARD_ACCESS";
	public static final String DONT_ALLOW_MAINMENU ="DONT_ALLOW_MAINMENU";
	public static final String PARENT_CATEGORY = "PARENT_CATEGORY";
	public static final String CHARGE_CLASS = "CHARGE_CLASS";
	public static final String RBT_ADVANCE_RENTAL_INTRO = "dialog.rbt_advance_rental_intro";
	public static final String RBT_DEFAULT_SONG_SET="dialog.rbt_default_song_set";
	public static final String RBT_CONFIRM_ADVANCE_RENTAL_INTRO="dialog.rbt_confirm_advance_rental_intro";
	public static final String RBT_SOUNSUBSCRIBE_YOU_HAVE="dialog.rbt_sounsubscribe_you_have";
	public static final String RBT_SOUNSUBSCRIBE_DAYS_LEFT="dialog.rbt_sounsubscribe_days_left";
	public static final String RBT_CONFIRM_UNSUBSCRIPTION_INTRO="dialog.rbt_confirm_unsubscription_intro";
	public static final String RBT_SOASKFORGIFTINBOX_INTRO1="dialog.rbt_soaskforgiftinbox_intro1";
	public static final String RBT_SOCONFIRMDELETESETTING_INTRO = "dialog.rbt_soconfirmdeletesetting_intro";
	public static final String RBT_SOCONFIRMDELETESETTING_SUCCESS = "dialog.rbt_soconfirmdeletesetting_success";
	public static final String RBT_SOCONFIRMGIFT_INTRO = "dialog.rbt_soconfirmgift_intro";
	public static final String RBT_SOCONFIRMGIFT_INTRO_SONGCATCHER = "dialog.rbt_soconfirmgift_intro_songcatcher";
	public static final String RBT_SOCONFIRMGIFT_SUCCESS = "dialog.rbt_soconfirmgift_success";
	public static final String RBT_SOCOPY_INTRO = "dialog.rbt_socopy_intro";
	public static final String RBT_SOCRICKET_INTRO = "dialog.rbt_socricket_intro";
	public static final String RBT_SOCRICKET_INTRO_2 = "dialog.rbt_socricket_intro_2";
	public static final String RBT_SODELETEGIFT_INTRO = "dialog.rbt_sodeletegift_intro";
	public static final String RBT_NOGIFT = "dialog.rbt_nogift";
	public static final String RBT_SODELETESPECIALSETTING_INTRO = "dialog.rbt_sodeletespecialsetting_intro";
	public static final String RBT_SODELETESPECIALSETTING_LASTNUMBER = "dialog.rbt_sodeletespecialsetting_lastnumber";
	public static final String RBT_SODELETESPECIALSETTING_FIRSTNUMBER = "dialog.rbt_sodeletespecialsetting_firstnumber";
	public static final String RBT_SODELETESPECIALSETTING_SUCCESS = "dialog.rbt_sodeletespecialsetting_success";
	public static final String RBT_SODTMFBROWSECLIPS_INTRO = "dialog.rbt_sodtmfbrowseclips_intro";
	public static final String RBT_SODTMFBROWSECLIPS_LASTCLIP = "dialog.rbt_sodtmfbrowseclips_lastclip";
	public static final String RBT_SODTMFBROWSECLIPS_FIRSTCLIP = "dialog.rbt_sodtmfbrowseclips_firstclip";
	public static final String RBT_SOEVERYONEPROFILE_INTRO = "dialog.rbt_soeveryoneprofile_intro";
	public static final String RBT_NO_SETTINGS = "dialog.rbt_no_settings";
	public static final String RBT_SOGETLANGUAGE_SET_SUCCESS = "dialog.rbt_sogetlanguage_set_success";
	public static final String RBT_SOGETMOBILENUMBER_COPY = "dialog.rbt_sogetmobilenumber_copy";
	public static final String RBT_SOGETMOBILENUMBER_COPY_ERROR = "dialog.rbt_sogetmobilenumber_copy_error";
	public static final String RBT_SOGETMOBILENUMBER_RBT_ALBUM_USER = "dialog.rbt_sogetmobilenumber_rbt_album_user";
	public static final String RBT_SOGETMOBILENUMBER_NOT_RBT_USER = "dialog.rbt_sogetmobilenumber_not_rbt_user";
	public static final String RBT_SOCONFIRMMOBILENUMBER_GIFTINBOX_FULL = "dialog.rbt_soconfirmmobilenumber_giftinbox_full";
	public static final String RBT_SOGIFTINBOX_INTRO_1 = "dialog.rbt_sogiftinbox_intro_1";
	public static final String RBT_SOGIFTINBOX_INTRO_2 = "dialog.rbt_sogiftinbox_intro_2";
	public static final String RBT_SOGIFTINBOX_INTRO_3 = "dialog.rbt_sogiftinbox_intro_3";
	public static final String RBT_SOGIFTINBOX_INTRO_4 = "dialog.rbt_sogiftinbox_intro_4";
	public static final String RBT_SOGIFTINBOX_INTRO_5 = "dialog.rbt_sogiftinbox_intro_5";
	public static final String RBT_SOGIFTINBOX_LASTGIFT = "dialog.rbt_sogiftinbox_lastgift";
	public static final String RBT_SOGIFTINBOX_FIRSTGIFT = "dialog.rbt_sogiftinbox_firstgift";
	public static final String RBT_SOHELP_INTRO = "dialog.rbt_sohelp_intro";
	public static final String RBT_SOHELP_ABOUT_IVR = "dialog.rbt_sohelp_about_ivr";
	public static final String RBT_SOHELP_HOW_IVR = "dialog.rbt_sohelp_how_ivr";
	public static final String RBT_SOHELP_CC = "dialog.rbt_sohelp_cc";
	public static final String RBT_SOINTERNATIONALROAMING_INTRO = "dialog.rbt_sointernationalroaming_intro";
	public static final String RBT_SOINTERNATIONALROAMING_INTRO_SHORT = "dialog.rbt_sointernationalroaming_intro_short";
	public static final String RBT_SOINTERNATIONALROAMING_TERMS_CONDITIONS = "dialog.rbt_sointernationalroaming_terms_conditions";
	public static final String RBT_SOINTERNATIONALROAMING_CHANGED = "dialog.rbt_sointernationalroaming_changed";
	public static final String RBT_SOMAINMENU_INTRO_CRICKET = "dialog.rbt_somainmenu_intro_cricket";
	public static final String RBT_SOMAINMENU_INTRO_HOT = "dialog.rbt_somainmenu_intro_hot";
	public static final String RBT_SOMAINMENU_INTRO_SONG = "dialog.rbt_somainmenu_intro_song";
	public static final String RBT_SOMAINMENU_INTRO_CATEGORY = "dialog.rbt_somainmenu_intro_category";
	public static final String RBT_SOMAINMENU_INTRO_COPY = "dialog.rbt_somainmenu_intro_copy";
	public static final String RBT_SOMAINMENU_INTRO_PROFILE = "dialog.rbt_somainmenu_intro_profile";
	public static final String RBT_SOMAINMENU_PROFILE_NOT_ALLOWED = "dialog.rbt_somainmenu_profile_not_allowed";
	public static final String RBT_SOOTHEROPTIONS_INTRO_MORE = "dialog.rbt_sootheroptions_intro_more";
	public static final String RBT_SOOTHEROPTIONS_INTRO = "dialog.rbt_sootheroptions_intro";
	public static final String RBT_SOPERSONALIZEDHELLOTUNES_INTRO = "dialog.rbt_sopersonalizedhellotunes_intro";
	public static final String RBT_SOPERSONALIZEDHELLOTUNES_INTRO_SHORT = "dialog.rbt_sopersonalizedhellotunes_intro_short";
	public static final String RBT_SOPERSONALIZEDHELLOTUNES_TERMS_CONDITIONS = "dialog.rbt_sopersonalizedhellotunes_terms_conditions";
	public static final String RBT_SOPERSONALIZEDHELLOTUNES_CHANGED = "dialog.rbt_sopersonalizedhellotunes_changed";
	public static final String RBT_SOPLAYCOPYTUNE_INTRO = "dialog.rbt_soplaycopytune_intro";
	public static final String RBT_SOPOSTDELETEEVERYONESETTING_INTRO = "dialog.rbt_sopostdeleteeveryonesetting_intro";
	public static final String RBT_SOPOSTDELETESPECIALSETTING_INTRO = "dialog.rbt_sopostdeletespecialsetting_intro";
	public static final String RBT_SOPOSTSETTING_INTRO = "dialog.rbt_sopostsetting_intro";
	public static final String RBT_SOPROFILE_INTRO = "dialog.rbt_soprofile_intro";
	public static final String RBT_SOREMOVECRICKET_INTRO = "dialog.rbt_soremovecricket_intro";
	public static final String RBT_SOREMOVECRICKET_SUCCESS = "dialog.rbt_soremovecricket_success";
	public static final String RBT_SOSETCRICKET_INTRO = "dialog.rbt_sosetcricket_intro";
	public static final String RBT_SOSETSELECTION_INTRO_1 = "dialog.rbt_sosetselection_intro_1";
	public static final String RBT_SOSETSELECTION_INTRO_2 = "dialog.rbt_sosetselection_intro_2";
	public static final String RBT_SOSETSELECTION_INTRO_3 = "dialog.rbt_sosetselection_intro_3";
	public static final String RBT_SOSPECIALPROFILE_INTRO = "dialog.rbt_sospecialprofile_intro";
	public static final String RBT_SOSPECIALSETTINGEXIST_INTRO_1 = "dialog.rbt_sospecialsettingexist_intro_1";
	public static final String RBT_SOSPECIALSETTINGEXIST_INTRO_2 = "dialog.rbt_sospecialsettingexist_intro_2";
	public static final String RBT_SOSPECIALSETTINGSFULL_INTRO = "dialog.rbt_sospecialsettingsfull_intro";
	public static final String RBT_SOUNSUBSCRIBE_NOT_SUBSCRIBER = "dialog.rbt_sounsubscribe_not_subscriber";
	public static final String RBT_SOUNSUBSCRIBE_INTRO = "dialog.rbt_sounsubscribe_intro";
	public static final String RBT_SOUNSUBSCRIBE_CANCEL_CONFIRM = "dialog.rbt_sounsubscribe_cancel_confirm";
	public static final String RBT_SOUNSUBSCRIBE_DEACTIVATE = "dialog.rbt_sounsubscribe_deactivate";
	public static final String RBT_SOUNSUBSCRIBE_DEACTIVE_FAILED = "dialog.rbt_sounsubscribe_deactive_failed";
	public static final String RBT_PRESS_1_BEG = "dialog.rbt_press_1_beg";
	public static final String RBT_PRESS_2_BEG = "dialog.rbt_press_2_beg";
	public static final String RBT_PRESS_3_BEG = "dialog.rbt_press_3_beg";
	public static final String RBT_PRESS_4_BEG = "dialog.rbt_press_4_beg";
	public static final String RBT_PRESS_5_BEG = "dialog.rbt_press_5_beg";
	public static final String RBT_PRESS_6_BEG = "dialog.rbt_press_6_beg";
	public static final String RBT_PRESS_7_BEG = "dialog.rbt_press_7_beg";
	public static final String RBT_PRESS_8_BEG = "dialog.rbt_press_8_beg";
	public static final String RBT_PRESS_9_BEG = "dialog.rbt_press_9_beg";
	public static final String RBT_CLIP_CALLBACK = "dialog.rbt_clip_callback";
	public static final String RBT_SODELETEGIFT_SUCCESS = "dialog.rbt_sodeletegift_success";
	public static final String RBT_SOADVANCERENTAL_AVAILED = "dialog.rbt_soadvancerental_availed";
	public static final String RBT_SOCRICKET_WELCOME = "dialog.rbt_socricket_welcome";
	public static final String RBT_SOSETCRICKET_INTRO_1 = "dialog.rbt_sosetcricket_intro_1";
	public static final String RBT_SERVICE_UNAVAILABLE = "dialog.rbt_service_unavailable";
	public static final String RBT_SOSETSELECTION_NEWUSER = "dialog.rbt_sosetselection_newuser";
	public static final String RBT_SOSETSELECTION_USER = "dialog.rbt_sosetselection_user";
	public static final String RBT_SOSETSELECTION_INTRO_4 = "dialog.rbt_sosetselection_intro_4";
	public static final String RBT_SOGETMOBILENUMBER_NOTVALID = "dialog.rbt_sogetmobilenumber_notvalid";
	public static final String RBT_TNB_LASTCHANCE = "dialog.rbt_tnb_lastchance";
	public static final String RBT_TNB_CATEGORY_UNAVAILABLE = "dialog.rbt_tnb_category_unavailable";
	public static final String RBT_TNB_UNAVAILABLE = "dialog.rbt_tnb_unavailable";
	public static final String RBT_SOTNBMAINMENU_INTRO = "dialog.rbt_sotnbmainmenu_intro";
	public static final String RBT_SOGETGENDER_INTRO = "dialog.rbt_sogetgender_intro";
	public static final String RBT_SOGETGENDER_ERROR = "dialog.rbt_sogetgender_error";
	public static final String RBT_SOGETAGE_INTRO = "dialog.rbt_sogetage_intro";
	public static final String RBT_SOBROWSECATEGORIES_MORE = "dialog.rbt_sobrowsecategories_more";
	public static final String RBT_SOALBUMINTRO_INTRO = "dialog.rbt_soalbumintro_intro";
	public static final String RBT_SOALBUMINTRO_NEWUSER = "dialog.rbt_soalbumintro_newuser";
	public static final String RBT_SOALBUMINTRO_USER = "dialog.rbt_soalbumintro_user";
	public static final String RBT_SOALBUMINTRO_TERMS = "dialog.rbt_soalbumintro_terms";
	public static final String RBT_SOBROWSEALBUMS_UNSUBSCRIBE = "dialog.rbt_sobrowsealbums_unsubscribe";
	public static final String RBT_SOALBUMS_INTRO = "dialog.rbt_soalbums_intro";
	public static final String RBT_SOALBUMPREVIEW_INTRO = "dialog.rbt_soalbumpreview_intro";
	public static final String RBT_SOSETALBUM_NEWUSER = "dialog.rbt_sosetalbum_newuser";
	public static final String RBT_SOSETALBUM_USER = "dialog.rbt_sosetalbum_user";
	public static final String RBT_SOSETALBUM_SUCCESS = "dialog.rbt_sosetalbum_success";
	public static final String RBT_SOALBUMPROFILE_INTRO = "dialog.rbt_soalbumprofile_intro";
	public static final String RBT_SOUNSUBSCRIBEALBUM_INTRO = "dialog.rbt_sounsubscribealbum_intro";
	public static final String RBT_SOPOSTUNSUBSCRIBEALBUM_INTRO = "dialog.rbt_sopostunsubscribealbum_intro";
	public static final String RBT_SOCONFIRMALBUMOVERWRITE_INTRO = "dialog.rbt_soconfirmalbumoverwrite_intro";
	public static final String RBT_SOPROFILE_ALBUM_EVERYONESETTING = "dialog.rbt_soprofile_album_everyonesetting";
	public static final String RBT_SOCONFIRMNUMBER_INTRO = "dialog.rbt_soconfirmnumber_intro";
	public static final String RBT_MM_CONTENT_DOES_NOT_EXIST = "dialog.rbt_mm_content_does_not_exist";
	public static final String RBT_SOALBUMS_LASTCLIP = "dialog.rbt_soalbums_lastclip";
	public static final String RBT_SOALBUMS_FIRSTCLIP = "dialog.rbt_soalbums_firstclip";
	public static final String RBT_SCRATCHCARD_WELCOME = "dialog.rbt_scratchcard_welcome";
	public static final String RBT_SOCONFIRMNUMBER_SUBSCRIPTION = "dialog.rbt_soconfirmnumber_subscription";
	public static final String RBT_SOCONFIRMNUMBER_FREE_DOWNLOAD = "dialog.rbt_soconfirmnumber_free_download";
	public static final String RBT_SOCONFIRMNUMBER_DOWNLOAD = "dialog.rbt_soconfirmnumber_download";
		
	//FOR SOConfirmGotoCategories
	//Your BSNL tune has been successfully set. Thanks for calling BSNL. 
	//If you wish to go to another category, press 1 else press 2 to end the call
	public static final String RBT_SOCONFIRM_GOTO_CATEGORIES = "dialog.rbt_soconfirm_goto_categories";
	
	//If you want to disable the press star to copy Intro message header playing for your callers, press 1
	public static final String RBT_SODISABLEPRESSSTAR_INTRO = "dialog.rbt_sodisablepressstar_intro";
	//Your Intro message have been successfully removed.
	public static final String RBT_SODISABLEPRESSSTAR_CONFIRM = "dialog.rbt_sodisablepressstar_confirm";
	//Thank you for using BSNL Tunes, we hope you enjoy the service, Dial 56700 for more songs
	public static final String RBT_SODISABLEPRESSSTAR_THANKS = "dialog.rbt_sodisablepressstar_thanks";
	
	//Airtel Subscriber types
	public static int RBT_USER_TYPE_NORMAL = 0;
	public static int RBT_USER_TYPE_LIGHT = 5;
	public static int RBT_USER_TYPE_LIFE_TIME = 9;
	public static int RBT_USER_TYPE_RENTAL = 14;
	public static int RBT_USER_TYPE_ALBUM = 20;
	public static int RBT_USER_TYPE_SAMPLING = 25;
	public static int RBT_USER_TYPE_LOW_RENTAL = 5;

	//RT SMS 
	public static final String RT_INIT = "RT_INIT";
	
	//RBT Enabling and Disabling press star to intro, overlay. 
	public static final String EXTRA_INFO_INTRO_PROMPT_FLAG="PROMPT";
	public static final String DISABLE_PRESS_STAR_INTRO="1";
	public static final String ENABLE_PRESS_STAR_INTRO="0";
	public static final String EXTRA_INFO_PRE_RBT_WAV = "PRE_RBT_WAV";
	public static final String EXTRA_INFO_SYSTEM_INIT_PROMPT = "SYS_PROMPT";
	public static final String EXTRA_INFO_DTMF_KEYS = "DTMF_KEYS";
	public static final String EXTRA_INFO_FREE_SONG = "FREE_SONG";
	
	//RBT jingle(a set of songs) to be played or not
	public static final String EXTRA_INFO_JINGLE_FLAG = "JINGLE";
	public static final String PLAYER_XML_JINGLE_FLAG = "JINGLE";
	public static final String EXTRA_INFO_PCA_FLAG 	  = "PCA";
	public static final String PLAYER_XML_PCA_FLAG 	  = "PCA";
	
	public static final String EXTRA_INFO_RRBT_TYPE_FLAG 	  = "rrbt_type";

	public static final String EXTRA_INFO_INTRO_OVERLAY_FLAG="OVER";
	public static final String DISABLE_INTRO_OVERLAY="1";
	public static final String ENABLE_INTRO_OVERLAY="0";
	public static final String EXTRA_INFO_TPCGID = "TPCGID";
	//CG Integration Flow - Jira -12806
	public static final String EXTRA_INFO_TRANS_ID = "TRANS_ID";
	
	public static final String CAMPAIGN_CODE = "CAMPAIGN_CODE";
	public static final String TREATMENT_CODE = "TREATMENT_CODE";
	public static final String OFFER_CODE = "OFFER_CODE";
	public static final String RETRY_COUNT = "RETRY_COUNT";
	
	public static final String SONG_PROD_ID = "SONG_PROD_ID";
	public static final String SUB_PROD_ID = "SUB_PROD_ID";
	public static final String AMOUNT = "AMOUNT";
	public static final String EXTRA_INFO_OLD_ACT_BY = "OLD_ACT_BY"; 
	public static final String SOURCE_WAV_FILE_ATTR="SOURCE";
	public static final String KEYPRESSED_ATTR="KEY";
	public static final String IS_THIRD_PARTY_ALLOWEDED_KEY="IS_THIRD_PARTY_ALLOWEDED_KEY";
	public static final String COPY_CONFIRM_MODE_KEY="COPY_MODE";
	public static final String GIFTTYPE_ATTR="GIFTTYPE";
	public static final String PROMOID_ATTR="PROMOID";
	
	
	public static final String EXTRA_INFO_WDS_QUERY_RESULT="winresponse";
	
	//failure message column in extra info
	public static final String EXTRA_INFO_FAILURE_MESSAGE="FAILURE_MESSAGE";

	//Offer constants
	public static String ACWM_OFFER_ID = "ACWM_OFFER_ID";	
	public static final String EXTRA_INFO_OFFER_ID="OFFER_ID";
	public static final String EXTRA_INFO_UPGRADE_CONSENT = "UPGRADE_CONSENT";
	public static final String EXTRA_INFO_TOBE_ACT_OFFER_ID = "TOBE_ACT_OFFER_ID";
	public static final String EXTRA_INFO_IMEI_NO="IMEI_NO";
	public static final String EXTRA_INFO_REACT_REFID = "REACT_REFID";
	public static final String EXTRA_INFO_OFFER_TYPE="OFFER_TYPE";
	
	//CONSENT
	public static final String EXTRA_INFO_USER_CONSENT="USER_CONSENT";
	public static final String FAILED_CONSENT_PROMOTION_TEXT="FAILED_CONSENT_PROMOTION_TEXT";
	public static final String SENDER_NUMBER = "SENDER_NUMBER";
	
	//ALTACC
	public static final String EXTRA_INFO_ACT_TYPE="ACT_TYPE";
	public static final String EXTRA_INFO_REQUEST_TYPE="REQUEST_TYPE";
	
	//BSNL Ad-Rbt constants
	public static final String EXTRA_INFO_ADRBT_ACTIVATION = "ADRBT_ACTIVATION";
	public static final String EXTRA_INFO_ADRBT_DEACTIVATION = "ADRBT_DEACTIVATION";
	public static final String EXTRA_INFO_ADRBT_TRANS_ID = "ADRBT_TRANS_ID";
	public static final String EXTRA_INFO_ADRBT_MODE = "ADRBT_MODE";
	//Voice presence 
	
	public static final String EXTRA_INFO_PACK = "PACK";
	
	// UNSUB DELAY
	public static final String UNSUB_DELAY = "UNSUB_DELAY";
	
	//DELAY DEACT
	public static final String DELAY_DEACT = "DELAY_DEACT";
	//COPY_TYPE for reporting
	public static final String EXTRA_INFO_COPY_TYPE = "COPY_TYPE";
	//COPY CONF MODE 
	public static final String EXTRA_INFO_COPY_MODE = "COPY_MODE";
	public static final String EXTRA_INFO_COPY_TYPE_OPTIN = "OPTIN";

	//COS Upgradation
	public static final String EXTRA_INFO_COS_ID = "COS_ID";
	public static final String EXTRA_INFO_RETRY_COS_ID = "RETRY_COS_ID";
	
	public static final String EXTRA_INFO_IS_DAEMON = "ISDAEMON";
	
	//Cos types
	public static final String COS_TYPE_PPU = "PPU";	
	
	//RomaniaGiftImplementation
	public static final String GIFT_FAILURE_GIFTER_NOT_ACT = "GIFT_FAILURE_GIFTER_NOT_ACT";
	public static final String GIFT_FAILURE_ACT_GIFT_PENDING = "GIFT_FAILURE_ACT_GIFT_PENDING";
	public static final String GIFT_FAILURE_GIFT_IN_USE = "GIFT_FAILURE_GIFT_IN_USE";
	public static final String GIFT_SUCCESS = "GIFT_SUCCESS";
	public static final String GIFT_FAILURE_ACT_PENDING = "GIFT_FAILURE_ACT_PENDING";
	public static final String GIFT_FAILURE_DEACT_PENDING = "GIFT_FAILURE_DEACT_PENDING"; 
	public static final String GIFT_FAILURE_TECHNICAL_DIFFICULTIES = "GIFT_FAILURE_TECHNICAL_DIFFICULTIES";
	public static final String GIFT_FAILURE_GIFTEE_NOT_ACTIVE = "GIFT_FAILURE_GIFTEE_NOT_ACTIVE";
	public static final String GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS = "GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS";
	public static final String GIFT_FAILURE_SONG_GIFT_PENDING = "GIFT_FAILURE_SONG_GIFT_PENDING";
	public static final String GIFT_FAILURE_SONG_IN_USE = "GIFT_FAILURE_SONG_IN_USE";
	public static final String GIFT_FAILURE_GIFTEE_INVALID = "GIFT_FAILURE_GIFTEE_INVALID";
	public static final String GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE = "GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE";
	public static final String GIFT_SUCCESS_GIFTEE_NEW_USER = "GIFT_SUCCESS_GIFTEE_NEW_USER";

	// WARID GIFT dTANTS
	public static final String INIT_GIFT_SUCCESS_GIFTEE_NEW_USER 				= "INIT_GIFT_SUCCESS_GIFTEE_NEW_USER";
	public static final String INIT_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE		 	= "INIT_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE";
	public static final String INIT_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY 	= "INIT_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY";
	public static final String INIT_GIFT_FAILURE_NO_PENDING_GIFTS 				= "INIT_GIFT_FAILURE_NO_PENDING_GIFTS";
	public static final String INIT_GIFT_CONFIRM_SUCCESS		 				= "INIT_GIFT_CONFIRM_SUCCESS";

	public static final String PRE_GIFT_SUCCESS_GIFTEE_NEW_USER 				= "PRE_GIFT_SUCCESS_GIFTEE_NEW_USER";
	public static final String PRE_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE		 	= "PRE_GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE";
	public static final String PRE_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY 	= "PRE_GIFT_FAILURE_GIFT_EXISTS_IN_GIFTEE_LIBRARY";
	public static final String PRE_GIFT_FAILURE_NO_PENDING_GIFTS 				= "PRE_GIFT_FAILURE_NO_PENDING_GIFTS";
	public static final String PRE_GIFT_FAILURE_INVALID_PROMO_CODE 				= "PRE_GIFT_FAILURE_INVALID_PROMO_CODE";
	public static final String PRE_GIFT_CONFIRM_SUCCESS		 					= "PRE_GIFT_CONFIRM_SUCCESS";
	public static final String PRE_GIFT_GIFTEE_SMS_NEW_USER 					= "PRE_GIFT_GIFTEE_SMS_NEW_USER";
	public static final String PRE_GIFT_GIFTEE_SMS_ALREADY_ACTIVE		 		= "PRE_GIFT_GIFTEE_SMS_ALREADY_ACTIVE";

	public static final String LOTTERY_LIST_SUCCESS = "LOTTERY_LIST_SUCCESS";
	public static final String LOTTERY_LIST_INACTIVE_USER = "LOTTERY_LIST_INACTIVE_USER";
	public static final String LOTTERY_LIST_NO_ENTRIES = "LOTTERY_LIST_NO_ENTRIES";

	//GIFT STATUS
	public static final String GIFT = "GIFT";
	public static final String GIFTCHRGPENDING = "GIFTCHRGPENDING";
	public static final String GIFT_CHARGED = "GIFT_CHARGED";
	public static final String GIFTED = "GIFTED";
	public static final String ACCEPT_ACK = "ACCEPT_ACK";
	public static final String ACCEPTED = "ACCEPTED";
	public static final String GIFTFAILED = "GIFTFAILED";
	public static final String REJECT_ACK = "REJECT_ACK";
	public static final String REJECTED = "REJECTED";
	public static final String ACCEPT_PRE = "ACCEPT_PRE";
	public static final String GIFT_TRANSACTION_ID = "GIFT_TRANSACTION_ID";
	
	//------ User Lock/Unlock constants for Voda India
	
	public static final String EXTRA_INFO_LOCK_USER="USER_LOCK";
	public static final String EXTRA_INFO_LOCK_USER_TRUE="TRUE";
	public static final String EXTRA_INFO_LOCK_USER_FALSE="FALSE";
	
	//Romania max download impl
	public static int MAX_DOWNLOAD_ALLOWED = 16;

	public static String IS_NEWSLETTER_ON = "NEWS";
	public static String NEWSLETTER_ON = "TRUE";
	public static String NEWSLETTER_OFF = "FALSE";
	
	public static String PLAY_POLL_STATUS = "POLL";
	
	public static String PLAY_POLL_STATUS_ON = "0";
	public static String PLAY_POLL_STATUS_OFF = "1";
	
	//addsubscriberselections response strings
	public static String SELECTION_FAILED_SUBSCRIBER_SUSPENDED = "SELECTION_FAILED_SUBSCRIBER_SUSPENDED";
	public static String SELECTION_FAILED_OWN_NUMBER = "SELECTION_FAILED_OWN_NUMBER";
	public static String SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED = "SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED";
	public static String SELECTION_FAILED_NULL_WAV_FILE = "SELECTION_FAILED_NULL_WAV_FILE";
	public static String SELECTION_FAILED_CLIP_EXPIRED = "SELECTION_FAILED_CLIP_EXPIRED";
	public static String SELECTION_FAILED_CATEGORY_EXPIRED = "SELECTION_FAILED_CATEGORY_EXPIRED";
	public static String SELECTION_FAILED_TNB_TO_DEFAULT_FAILED = "SELECTION_FAILED_TNB_TO_DEFAULT_FAILED";
	public static String SELECTION_FAILED_SELECTION_OVERLAP = "SELECTION_FAILED_SELECTION_OVERLAP";
	public static String RBT_CORPORATE_NOTALLOW_SELECTION = "RBT_CORPORATE_NOTALLOW_SELECTION";
	
	public static String SELECTION_FAILED_INTERNAL_ERROR = "SELECTION_FAILLED_INTERNAL_ERROR";
	public static String SELECTION_SUCCESS = "SELECTION_SUCCESS";
	public static String SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS = "SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS";
	public static String SELECTION_FAILED_CALLER_ALREADY_IN_GROUP = "SELECTION_FAILED_CALLER_ALREADY_IN_GROUP";
	public static String SELECTION_FAILED_DOWNLOAD_FAILURE = "SELECTION_FAILED_DOWNLOAD_FAILURE";
	public static String SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE = "SELECTION_FAILED_ADRBT_FOR_PROFILES_OR_CORPORATE";
	public static String SELECTION_FAILED_ADRBT_FOR_SHUFFLES = "SELECTION_FAILED_ADRBT_FOR_SHUFFLES";
	public static String SELECTION_FAILED_ADRBT_FOR_SPECIFIC_CALLER = "SELECTION_FAILED_ADRBT_FOR_SPECIFIC_CALLER";
	public static String SELECTION_FAILED_WDS_FAILED = "SELECTION_FAILED_WDS_FAILED";
	public static String SELECTION_FAILED_CALLER_BLOCKED = "SELECTION_FAILED_CALLER_BLOCKED";
	public static String DOWNLOAD_FAILED_CLIP_EXPIRED = "DOWNLOAD_FAILED_CLIP_EXPIRED";
	public static String DOWNLOAD_FAILED_CATEGORY_EXPIRED = "DOWNLOAD_FAILED_CATEGORY_EXPIRED";
	public static String SELECTION_FAILED_SELECTION_LIMIT_REACHED = "SELECTION_FAILED_SELECTION_LIMIT_REACHED";
	public static String SELECTION_FAILED_CALLERID_LIMIT_REACHED = "SELECTION_FAILED_CALLERID_LIMIT_REACHED";
	public static String SELECTION_FAILED_LOOP_SELECTION_LIMIT_REACHED = "SELECTION_FAILED_CALLERID_SELECTION_LIMIT_REACHED";
	public static String SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN = "SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN";
	public static String PACK_DOWNLOAD_LIMIT_REACHED = "PACK_DOWNLOAD_LIMIT_REACHED";
	public static String BASE_OFFER_NOT_AVAILABLE = "BASE_OFFER_NOT_AVAILABLE";
	
	//groups
	public static String CALLER_ALREADY_PRESENT_IN_GROUP = "CALLER_ALREADY_PRESENT_IN_GROUP";
	public static String ALREADY_BLOCKED = "ALREADY_BLOCKED";
	public static String ALREADY_PERSONALIZED_SELECTION_FOR_CALLER = "ALREADY_PERSONALIZED_SELECTION_FOR_CALLER";
	public static String CALLER_ADDED_TO_GROUP = "CALLER_ADDED_TO_GROUP";
	public static String SAME_PREGROUP_EXISTS_FOR_CALLER = "SAME_PREGROUP_EXISTS_FOR_CALLER";
	public static String MAX_GROUP_PRESENT_FOR_SUBSCRIBER = "MAX_GROUP_PRESENT_FOR_SUBSCRIBER";
	public static String GROUP_ADDED_SUCCESFULLY = "GROUP_ADDED_SUCCESFULLY";
	public static String GROUP_ADD_FAILED_INTERNAL_ERROR = "GROUP_ADD_FAILED_INTERNAL_ERROR";
	public static String CALLER_NOT_ADDED_INTERNAL_ERROR = "CALLER_NOT_ADDED_INTERNAL_ERROR";
	public static String MAX_CALLER_PRESENT_IN_GROUP = "MAX_CALLER_PRESENT_IN_GROUP";
	public static String GROUP_ADD_FAILED_GROUPNAME_NULL = "GROUP_ADD_FAILED_GROUPNAME_NULL";
	public static String SAME_GROUP_NAME_EXISTS_FOR_CALLER = "SAME_GROUP_NAME_EXISTS_FOR_CALLER";
	public static String SELECTION_FAILED_INVALID_PARAMETER = "SELECTION_FAILED_INVALID_PARAMETER";
	public static String USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
	
	public static String EXTRA_INFO_CG_SUBSCRIPTION_ID = "CG_SUBSCRIPTION_ID";
	
	//UDS
	public static String UDS_OPTIN = "UDS_OPTIN";
	public static String UDSOPTIN_SUCCESS = "UDSOPTIN_SUCCESS";
	public static String UDSOPTIN_FAILURE = "UDSOPTIN_FAILURE";
	public static String UDSDCTOPIN_SUCCESS = "UDSDCTOPIN_SUCCESS";
	public static String UDSDCTOPIN_FAILURE = "UDSDCTOPIN_FAILURE";
	public static String UDS_OPTIN_ON = "TRUE";
	public static String UDS_OPTIN_OFF = "FALSE";
	
	public static String UGC_MODE = "UGC";
	
	//BSNL AD RBT
	public static String ADRBT_SERVER_URL_HIT = "ADRBT_SERVER_URL_HIT";
	public static String ADRBT_ACT_URL = "ADRBT_ACT_URL";
	public static String ADRBT_DEACT_URL = "ADRBT_DEACT_URL";
	// CORPORATE
	public static String CAMPAIGN_ID = "CAMPAIGN_ID";

	//Monitoring constants
	public static char MONITOR_STATE_STARTED = 'A';
	public static char MONITOR_STATE_FINISHED = 'B';
	
	//Offer Constants
	public static String ALLOW_GET_OFFER = "ALLOW_GET_OFFER";
	public static String ALLOW_ONLY_BASE_OFFER = "ALLOW_ONLY_BASE_OFFER";
	public static String SUPPORT_SMCLIENT_API = "SUPPORT_SMCLIENT_API";
	public static String SUPPORT_SMCLIENT_FOR_OFFERID = "SUPPORT_SMCLIENT_FOR_OFFERID";
	public static String IS_SEL_OFFER_MANDATORY = "IS_SEL_OFFER_MANDATORY";
	
	//Du Offer Constants
	public static String ALLOW_SEL_OFFER  = "ALLOW_SEL_OFFER";
	public static String ALLOW_BASE_OFFER = "ALLOW_BASE_OFFER";
	public static String ALLOW_BASE_OFFER_DU = "ALLOW_BASE_OFFER_DU";
	public static String OFFER_DAYS_LIMIT = "OFFER_DAYS_LIMIT";
	
	//Announcement Constants
	public static final String PROCESS_ANNOUNCEMENTS = "PROCESS_ANNOUNCEMENTS";
	
	//Anouncement Status
	public static final int ANNOUNCEMENT_ACTIVE = 1;
	public static final int ANNOUNCEMENT_DEACTIVE = 2;
	public static final int ANNOUNCEMENT_TO_BE_ACTIVED = 31;
	public static final int ANNOUNCEMENT_TO_BE_DEACTIVED = 32;
	public static final int ANNOUNCEMENT_BASE_DEACTIVATION_PENDING = 33;
	public static final int ANNOUNCEMENT_ACTIVATION_PENDING = 34;
	public static final int ANNOUNCEMENT_DEACTIVATION_PENDING = 35;
	public static final int ANNOUNCEMENT_TO_BE_ACTIVED_PLAYER = 41;
	public static final int ANNOUNCEMENT_TO_BE_DEACTIVED_PLAYER = 42;
	
	
	//Subscription pack status
	public static final int BASE_ACTIVATION_PENDING = 30;
	public static final int PACK_TO_BE_ACTIVATED = 31;
	public static final int PACK_ACTIVATION_PENDING = 32;
	public static final int PACK_ACTIVATED = 33;
	public static final int PACK_ACTIVATION_ERROR = 34;
	public static final int PACK_SUSPEND = 35;
	public static final int PACK_GRACE = 36;
	public static final int PACK_TO_BE_DEACTIVATED = 41;
	public static final int PACK_DEACTIVATION_PENDING = 42;
	public static final int PACK_DEACTIVATED = 43;
	public static final int PACK_DEACTIVATION_ERROR = 44;
	public static final int PACK_ODA_REFRESH = 50;

	//Copy Processor
	public static final String COPY = "COPY";
	public static final String COPYSTAR = "COPYSTAR";
	public static final String RRBT_COPY = "RRBT_COPY";
	public static final String RTCOPY = "RTCOPY";
	public static final String SELF_GIFT = "SELF_GIFT";
	public static final String GIFTCOPY = "GIFTCOPY";
	public static final String CROSSCOPY = "CROSSCOPY";
	public static final String HASH_DOWNLOAD = "HASH_DOWNLOAD";
	
	public static final String CROSSCOPY_KEY = "CROSSCOPY_KEY";
	//RBT-14671 - # like
	public static final String TOLIKE_KEY = "TOLIKE_KEY";
		//COPY_TYPE and CONF_MODE parameters
	
	public static final String LIKE = "LIKE";
	public static final String DIRECTCOPY="D";
	public static final String OPTINCOPY="N";
	public static final String ETOPUP_SUB_CLASS_MAP_FOR_INACTIVE_USER="ETOPUP_SUB_CLASS_MAP_FOR_INACTIVE_USER";
	public static final String SUBCLASS_COS_MAPPING = "SUBCLASS_COS_MAPPING";
	//SM Servlet Response Codes
	public static int SM_AUTH_FAILED =600;
	public static int SM_DB_ERROR =601;
	public static int SM_RETRY_REQUIRED =602;
	public static int SM_BILLING_IN_PROGRESS=603;
	public static int SM_MISSING_ARGUMENTS =604;
	public static int SM_SBN_MODE_UNKNOWN =606;
	public static int SM_PARENT_UNDER_BILLING =607;
	public static int SM_SBN_DEACT_PENDING =608;
	
	public static int  SM_SUCCESS =200;
	public static int  SM_UNKNOWN_ERROR =700;
	public static int  SM_SUB_NOT_FOUND =701;
	public static int  SM_SUBN_NOT_FOUND =702;
	public static int  SM_SERVICE_NOT_FOUND =703;
	public static int  SM_REQUEST_INVALID =704;
	public static int  SM_SUBSCRIPTION_ALREADY_EXISTS =705;
	public static int  SM_MISSING_PARAMETERS =707;
	public static int  SM_DUPLICAT_REFID =708;
	public static int  SM_EVT_KEY_NOT_FOUND =709;
	public static int  SM_EVT_SBN_REQUIRED =710;
	public static int  SM_PARENT_NOT_ACTIVE =711;
	public static int  SM_TRIGGER_NOT_FOUND =712;
	public static int  SM_TRIGGER_OVERLAPPED =713;
	public static int  SM_LOW_BAL =714;
	public static int  SM_SUB_BLACKLISTED =715;
	public static int  SM_SERVICE_INFO_INVALID =715;
	public static int  SM_UPGRADATION_NOT_FOUND =716;
	public static int  SM_INCORRECT_PARAMETER_LENGTH =717;
	public static int  SM_REQUEST_LIMIT_EXCEEDED =718;
	public static int  SM_SUBSCRIPTION_IN_HOLD =720;
	public static int  SM_PARENT_UNDER_SUSPENSION =721;
	public static int  SM_SBN_UNDER_DEACTIVATION =722;
	public static int  SM_SBN_NOT_ACTIVE =723;
	public static int  SM_SBN_ALREADY_IN_SUSPENSION =724;
	public static int  SM_SBN_NOT_IN_SUSPENSION =725;
	public static int  SM_MSISDN_OUTDATED =726;
	

	public static long eventTypeForSocialUserBaseActivation=1;
	public static long eventTypeForSocialUserBaseDeactivation=2;
	public static long eventTypeForSocialUserSelActivation=3;
	public static long eventTypeForSocialUserSelDeactivation=4;
	public static long eventTypeForSocialUserDownloadActivation=5;
	public static long eventTypeForSocialUserDownloadDeactivation=6;
	public static long eventTypeForSocialUserGift=7;
	public static long eventTypeForSocialUserGiftSelection=8;
	public static long eventTypeForSocialUserCopySelection=9;
	public static short evtTypeForAccountExpiry=21;
	public static short evtTypeForChangeMsidn=22;
	

	public static int toBePublishedStatus=1;
	public static int successfullyPublishedStatus=2;
	public static int publishingErrorStatus=3;
	public static int notValidUpdateStatus=4;

	//Added for Telefonica SMS Double confirmation
	
	public static final String SMS_CONFIRMATION_ON="SMS_CONFIRMATION_ON";
	public static final String SMS_CONFIRMATION_ON_FOR_ACTIVE_USERS="SMS_CONFIRMATION_ON_FOR_ACTIVE_USERS";
	public static final String SMS_REQUEST_CONFIRMATION_ON="SMS_REQUEST_CONFIRMATION_ON";
	public static final String OPT_IN_CONFIRMATION_ACT_SMS="OPT_IN_CONFIRMATION_ACT_SMS";
	public static final String OPT_IN_CONFIRMATION_SEL_SMS="OPT_IN_CONFIRMATION_SEL_SMS";
	public static final String REQUEST_OPT_IN_CONFIRMATION_ACT_SMS="REQUEST_OPT_IN_CONFIRMATION_ACT_SMS";
	public static final String REQUEST_OPT_IN_CONFIRMATION_SEL_SMS="REQUEST_OPT_IN_CONFIRMATION_SEL_SMS";

	public static final String OPT_IN_CONFIRMATION_ACT_SMS_WITHIN_SUBSCRIPTION_PERIOD ="OPT_IN_CONFIRMATION_ACT_SMS_WITHIN_SUBSCRIPTION_PERIOD";
	public static final String OPT_IN_CONFIRMATION_ACT_SMS_AFTER_SUBSCRIPTION_PERIOD="OPT_IN_CONFIRMATION_ACT_SMS_AFTER_SUBSCRIPTION_PERIOD";
	
	public static final int ADRBT_TRANS_TYPE_ADRBT_ACT_NEW_USER = 1; 
	public static final int ADRBT_TRANS_TYPE_ADRBT_ACT_RBT_USER = 2;
	public static final int ADRBT_TRANS_TYPE_RBT_ACT_ADRBT_USER = 3;
	public static final int ADRBT_TRANS_TYPE_ADRBT_DEACT_ADRBT_USER = 4;
	public static final int ADRBT_TRANS_TYPE_ADRBT_DEACT_RBTnADRBT_USER = 5;
	public static final int ADRBT_TRANS_TYPE_RBT_DEACT_RBTnADRBT_USER = 6;
	
	// TransactionTypes for Upgrade requests
	public static final int TRANSACTION_TYPE_BASE_PACK_UPGRADATION = 1;

	//TNB_USER
	public static final String TNB_USER  = "TNB_USER";
	
	//hlr prov
	public static final String HLR_PROV = "HLR_PROV";

	public static final String EXTRA_INFO_ZOOMIN 	= "ZOOMIN";
	public static final String TYPE_ZOOMIN 			= "ZOOMIN";
	
	//Upgrade Retry SM callback
	public static final String EXTRA_INFO_UPGRADE_FAILURE_RBTTYPE = "UPGRADE_FAILURE_RBTTYPE";
	public static final String EXTRA_INFO_UPGRADE_FAILURE_SUB_CLASS = "UPGRADE_FAILURE_SUB_CLASS";
	public static final String EXTRA_INFO_UPGRADE_FAILURE_OLD_SUB_CLASS = "OLD_SUB_CLASS";
//	public static final String EXTRA_INFO_IS_SUSPENDED_SM_RETRY = "IS_SUSPENDED_SM_RETRY";
	
	// JIRAID-RBT-3786: Info URL required
	// JIRAID-RBT-3793 : Need Subscriber Pack details in PROMOTION.JSP 
   //param name for promotional response xml format	
	public static final String PROMOTION_STATUS_RESPONSE_XML ="PROMOTION_STATUS_RESPONSE_XML";
	public static final String PROMOTION_STATUS_RESOPNSEXML_NBD_APPEND ="PROMOTION_STATUS_RESPONSE_XML_NBD_APPEND";
	public static final String PROMOTION_STATUS_RESOPNSEXML_PROMOID_APPEND="PROMOTION_STATUS_RESPONSEXML_PROMOID_APPEND";
	public static final String PROMOTION_STATUS_RESOPNSEXML_CLIPID_APPEND="PROMOTION_STATUS_RESPONSEXML_CLIPID_APPEND";
	public static final String PROMOTION_STATUS_RESOPNSEXML_SEL_PROMOID_APPEND="PROMOTION_STATUS_RESPONSEXML_SEL_PROMOID_APPEND";
	public static final String PROMOTION_STATUS_RESOPNSEXML_SEL_CLIPID_APPEND="PROMOTION_STATUS_RESPONSEXML_SEL_CLIPID_APPEND";
	public static final String PROMOTION_STATUS_RESOPNSEXML_CLIPID_APPEND_STATUS="PROMOTION_STATUS_RESPONSEXML_CLIPID_APPEND_STATUS";
	public static final String kCOSID ="%COS_ID%";
	public static final String kSTATUS ="%STATUS%";
	public static final String kCLIPID="%CLIP_ID%";
	public static final String kPROMOID="%PROMO_ID%";
	public static final String kSELCLIPID="%SEL_CLIP_ID%";
	public static final String kSELPROMOID="%SEL_PROMO_ID%";
	public static final String kRBT_BRAND_NAME ="%RBT_BRAND_NAME%";
	public static final String kNEXTBILLDATE = "%NEXTBILLDATE%";
	public static final String kSERVICE_KEY = "%SERVICE_KEY%";
	public static final String kPROMPT = "%PROMPT%";
	public static final String kNextBillDateXMLTag = "<NEXTBILLDATE>%NEXTBILLDATE%</NEXTBILLDATE>";
	public static final String kRootXMLStartTag = "<ROOT>";
	public static final String kPromotionXMLEndTag = "</SERVICE></ROOT>";
	public static final String kSuccess = "SUCCESS";
	public static final String kActive = "ACTIVE";
	public static final String kRootXMLTag = "<ROOT/>";
	public static final String kEmptyString ="";
	public static final String kUnderScoreString ="_";
	public static final String kDateFormatwithTime ="yyyy-MM-dd HH:mm:ss";
	//Added by Sreekar for RBT-4589
	public static final String PROMOTION_STATUS_RESPONSE_XML_REQUIRED_STATUS ="PROMOTION_STATUS_RESPONSE_XML_REQUIRED_STATUS";
	public static final String kUDS ="%UDS%";
	
	public static final String AZAAN_COS_ID_LIST       = "AZAAN_COS_ID_LIST";
	public static final String AZAAN_DEFAULT_COS_ID      = "AZAAN_DEFAULT_COS_ID";
	public static final String AZAAN_CATEGORY_ID_LIST  = "AZAAN_CATEGORY_ID_LIST";
	public static final String AZAAN_WAV_FILE_NAME     = "AZAAN_WAV_FILE_NAME";
	public static final String AZAAN_CATEGORY_ID     = "AZAAN_CATEGORY_ID";
	public static final String AZAAN_CONTENT_NAME     = "AZAAN_CONTENT_NAME";
	public static final String VIRTUAL_NUMBER_COPY_MODE     = "VIRTUAL_NUMBER_COPY_MODE";
	public static final String BASE_MODE_MAP_FOR_COPY     = "BASE_MODE_MAP_FOR_COPY";
	public static final String UNBLOCK_PROVISIONING_REQUEST_MODES      = "UNBLOCK_PROVISIONING_REQUEST_MODES";
	public static final String VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER = "VIRTUAL_NUMBER_COPY_MODE_ACTIVE_USER";
 
	public static final String AZAAN_COPTIC_DOAA_COS_IDS = "AZAAN_COPTIC_DOAA_COS_IDS";
	public static final String COSID_SUBTYPE_MAPPING_FOR_AZAAN = "COSID_SUBTYPE_MAPPING_FOR_AZAAN";
	public static final String SUBTYPE_CONTENT_NAME_MAPPING_FOR_AZAAN = "SUBTYPE_CONTENT_NAME_MAPPING_FOR_AZAAN";
	public static final String AZAAN_COPTIC_DOAA_CATEGORIES = "AZAAN_COPTIC_DOAA_CATEGORIES";
	public static final String CATID_NAME_MAPPING_FOR_AZAAN_COPTIC_DOAA = "CATID_NAME_MAPPING_FOR_AZAAN_COPTIC_DOAA";
	public static final String SEND_APP_NAME_TO_TP = "SEND_APP_NAME_TO_TP";
	
	public static final String PACK_REQ_INFO_COSID       = "PACK_REQ_INFO_";
	
	//PACK SELECTION TYPES
	public static final int PROFILE_SEL_TYPE = 99;
	//PACK COSTYPES
	public static final String PROFILE_COS_TYPE = "PROFILE";
	public static final String COS_TYPE_AUTO_DOWNLOAD	= "AUTO_DOWNLOAD";

	//EXTRA_INFOs upon deactivating pack
	public static final String EXTRA_INFO_PACK_DEACTIVATION_MODE = "deactivate_mode";
	public static final String EXTRA_INFO_PACK_DEACTIVATION_MODE_INFO = "deactivate_mode_info";
	public static final String EXTRA_INFO_PACK_DEACTIVATION_TIME = "deactivate_time";
	public static final String EXTRA_INFO_PACK_MAX_ALLOWED = "MAX_ALLOWED";
	public static final String EXTRA_INFO_PACK_INDEX = "INDEX";
	public static final String EXTRA_INFO_PACK_CATID = "CAT_ID";

	
	//parameter names
	public static final String ADD_ARTIST_NAME_IN_CONTENT_RESPONSE ="ADD_ARTIST_NAME_IN_CONTENT_RESPONSE";
	public static final String ADD_IMAGE_URL_IN_CONTENT_RESPONSE ="ADD_IMAGE_URL_IN_CONTENT_RESPONSE";
	public static final String ADD_CLIP_INFO_IN_CONTENT_RESPONSE ="ADD_CLIP_INFO_IN_CONTENT_RESPONSE";

	public static final String ADD_VCODE_IN_XML_RESPONSE ="ADD_VCODE_IN_XML_RESPONSE";
	
	public static final String SMS_AFFILIATED_CONTENT_MODES = "SMS_AFFILIATED_CONTENT_MODES";
	public static final String SMS_AFFILIATED_CONTENT_TEXT = "SMS_AFFILIATED_CONTENT_TEXT";
	
	public static final String AFFILIATED_CONTENT_MODES = "AFFILIATED_CONTENT_MODES";
	
	//Wind Italy * copy features constants
	public static final String FREE_COPY_AVAILED = "FREE_COPY_AVAILED";
	public static final String COPY_CONTENT_VALIDITY_AVAILED = "COPY_CONTENT_VALIDITY_AVAILED";
	public static final String MOBILE_APP_FREE = "MOBILE_APP_FREE";
	public static final String mdc_msisdn = "m";

	public static final String sms_trans_logger = "SMS.TRANS";
	public static final String promotion_trans_logger = "PROMOTION.TRANS";
	public static final String copy_trans_logger = "COPY.TRANS";
    public static final String param_isSelConsentInserted = "IS_SEL_CONSENT_INSERTED";
    public static final String param_isSubConsentInserted = "CONSENT_SUBSCRIPTION_INSERT";
    
    public static final String param_protocolNo = "protocolNo";
    public static final String param_protocolStaticText = "protocolStaticText";
    
    /*Airtel Gift Flow*/
    public static final String GIFTEE_SUBSCRIPTION_CLASS = "GIFTEE_SUBSCRIPTION_CLASS";
    public static final String GIFTEE_CHARGE_CLASS = "GIFTEE_CHARGE_CLASS";
    
    public static final String SUB_CLASSES_FOR_BASE_DEACT_ON_MP_FAILURE = "SUB_CLASSES_FOR_BASE_DEACT_ON_MP_FAILURE";
    public static final String MUSIC_PACK_COS_TYPES_FOR_SELECTIONS_LOOP	= "MUSIC_PACK_COS_TYPES_FOR_SELECTIONS_LOOP";
    
    //RBT-12942:: Blocking Song Change for Grace Users
    public static final String IS_RENEWAL_GRACE_ENABLED = "IS_RENEWAL_GRACE_ENABLED";
    
    public static final String EXTRA_INFO_RRBT_TYPE_SUSPENSION_FLAG="rrbt_type_suspension";
    
    public static final String MP_NON_MP_FEATURE_CLASS = "MP_NON_MP_FEATURE_CLASS";
    public static final String MP_NON_MP_CHARGE_CLASSES = "MP_NON_MP_CHARGE_CLASSES";
    public static final String MP_NON_MP_MP_COSID = "MP_NON_MP_MP_COSID";
    public static final String MP_NON_MP_MP_DEACTIVATION_MODE = "MP_NON_MP_MP_DEACTIVATION_MODE"; 

    public static final String UDS_ON = "UDS_ON";
    public static final String ALLOW_PREMIUM_CONTENT = "ALLOW_PREMIUM_CONTENT";
    public static final String MODES_TO_VALIDATE_LDAP_LOPD = "MODES_TO_VALIDATE_LDAP_LOPD";
    
    public static final String SHUFFLE_ALWAYS_ENABLED_DISABLED_FLAG = "SHUFFLE_ALWAYS_ENABLED_DISABLED_FLAG";
    
    //RBT-14540:
    public static final String CONTENT_TYPE_OLA = "OLA";
    public static final String XBI_CHARGE_SUB_CLASS_MAPPING = "XBI_CHARGE_SUB_CLASS_MAPPING"; 
    public static final String SONG_PACK_REQUEST = "SONG_PACK_REQUEST"; 
    public static final String PROMOTE_KEY = "PROMOTE_KEY";
    public static final String PROMOTE = "PROMOTE"; 
    public static final String IS_COPY_PROMOTE = "IS_COPY_PROMOTE";
    public static final String AIRTEL_CG_FAILED_RESPONSES="AIRTEL_CG_FAILED_RESPONSES";

	//Added for VDE-2730
    
    public static final String DISABLE_PRESS_STAR_INTRO_SUSPEND="1";
	public static final String ENABLE_PRESS_STAR_INTRO_SUSPEND="0";
	
	public static final String EXTRA_INFO_INTRO_SUSPEND_PRE_PROMPT_FLAG="PRE_PROMPT_SUSPEND";

	//Added for RBT-17883
	public static final String EXTRA_INFO_CHARGE_MDN="CHARGE_MDN";
	public static final String DOWNLOAD_MONTHLY_LIMIT_REACHED = "DOWNLOAD_MONTHLY_LIMIT_REACHED" ;

}
