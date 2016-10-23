package com.onmobile.android.utils;


public interface StringConstants {
	
	public static final String MOBILEAPP_CONFIG_FILE = "mobileapp";
	public static final String MANDATORY_PARAM_INVALID = "MANDATORY_PARAM_INVALID";
	
	public static final String PARAM_OPERATOR_NAME = "mobileapp.operator.name";
	
	//--------- Promotional category ID
	public final static String PARAM_PROMOTIONAL_MOBILE_CATEGORY = "mobileapp.promotional.category";
	
	//--------- Promotional category ID
	public final static String PARAM_PROMOTIONAL_MOBILE_CLIPS = "mobileapp.promotional.clips";
	
	public final static String PARAM_PROMOTIONAL_MOBILE_CLIPS_FOR_CATEGORY = "PROMOTIONAL_CLIPS_FOR_CATEGORY_";

	//---------Clip row count, to load max clips on the main page
	public final static String PARAM_CLIP_ROW_COUNT = "mobileapp.clip.rowcount";
	
	//---------Clip row count for BI category
     public final static String PARAM_CLIP_ROW_COUNT_FOR_BI = "mobileapp.clip.rowcount.for.BI";
     
     public final static String PARAM_CLIP_ROW_COUNT_FOR_BLACKBERRY = "mobileapp.clip.rowcount.for.Blackberry";
	
	//-------- PARAM type in the DB in RBT_PARAMETERS table
	public final static String PARAM_TYPE = "MOBILEAPP";
	
	//-------- Default Channel
	public final static String CHANNEL = "MOBILEAPP";
	
	//------- USER TYPE for rbt_logon_user table
	public final static String USER_TYPE = "MOBILECLIENT";
	
	/* MESSAGE KEYS TO BE RETURNED FROM SubscriberResponse */
	
	//---------- Download Success
	public final static String MESSAGE_KEY_DOWNLOAD_SUCCESS ="download.success";

	public final static String 	PARAM_PROFILE_CATEGORY_ID = "mobileapp.profile.parent.cat";
	public final static String 	PARAM_MAIN_CATEGORY_ID = "mobileapp.main.cat";
	public final static String 	PARAM_PARENT_CATEGORY_ID = "mobileapp.parent.cat";
	public final static String 	PARAM_HOME_CATEGORY_IDS = "mobileapp.home.cat";
	public final static String 	PARAM_MUSIC_PACK_AMOUNT = "mobileapp.musicpack.amount";
	public final static String 	PARAM_NEW_RELEASE_CATEGORY_ID = "mobileapp.newRelease.cat";
	public final static String 	PARAM_IMG_PATH = "IMG_PATH";
	public final static String 	PARAM_CATEGORY_IMAGE_WITH_RESOLUTIONS_PATH = "mobileapp.category.image.with.resolutions.path";
	
	public final static String 	PARAM_HIDE_PATH = "mobileapp.hide.path";
	public final static String 	PARAM_IMAGE_PATH = "mobileapp.image.path";
	public final static String 	PARAM_IMAGE_WITH_RESOLUTIONS_PATH = "mobileapp.image.with.resolutions.path";
	public final static String 	PARAM_PREVIEW_PATH = "mobileapp.preview.path";
	public final static String 	PARAM_CIRCLEID_LANGUAGE_MAP = "CIRCLE_ID_LANGUAGE_MAP";	


	public final static String 	PARAM_PROFILE_COS_ID = "mobileapp.profile.cos.id";
	public final static String 	PARAM_MUSIC_PACK_COS_ID = "mobileapp.musicpack.cos.id";
	public final static String 	PARAM_DEFAULT_PRICE_AMOUNT = "mobileapp.default.price.amount";
	public static final String  PARAM_DEFAULT_SELECTION_PERIOD = "mobileapp.default.selection.period";
	public final static String PARAM_NORMAL_PROFILE_COS_ID = "mobileapp.normal.profile.cos.id";
	public final static String PARAM_DEFAULT_CHARGE_CLASS = "mobileapp.default.charge.class";
	public final static String 	PARAM_MUSIC_PACK_CONTENT_TYPES = "mobileapp.musicpack.content.types";

	public final static String 	PARAM_BLOCKED_CATEGORY_IDS = "mobileapp.blocked.cat.ids";
	
	public final static String groupPrefix ="G";
	
	public final static String defaultLanguage = "eng";
	
	public final static int defaultRowCount = 8;
	public final static int defaultCategoryId = 5;
	
	public static final String FAILURE = "failure";

	//---------Voice Presence Recorded FilePath
    public final static String PARAM_VP_RECORDING_FOLDER = "mobileapp.vp.recording.folder";
    public final static String defaultVPRecordingFolder = "/tmp";
    // RBT-6497:-Handset Client- First song download via app for free
    public final static String 	PARAM_FREE_CHARGE_CLASS = "mobileapp.free.chargeclass";
    public final static String 	PARAM_FREE_SEL_MODE = "mobileapp.free.sel.mode";
    public final static String 	PARAM_DEFAULT_CIRCLE_ID = "mobileapp.default.circleId";
    public final static String 	PARAM_DAY_FORMAT = "mobileapp.day.format";
    public final static String 	PARAM_MONTH_FORMAT = "mobileapp.month.format";
    public final static String 	PARAM_TOPSONG_MSG = "mobileapp.topsong.msg";
    public final static String 	PARAM_GIFT_AMOUNT = "mobileapp.gift.amount";
    public final static String 	PARAM_CHARGE_CLASS_PROFILE = "mobileapp.profile.chargeClass";
    public final static String 	PARAM_DATE_FORMAT = "mobileapp.date.format";
    public final static String 	PARAM_NAME_TUNE_TXT = "mobileapp.nametune.text";
    public final static String 	PARAM_TUNE_FILE_PATH = "mobileapp.tune.filepath";
    public final static String 	PARAM_CHARGECLASS_CAT_ID = "mobileapp.chargeClass.cat";
    public final static String 	PARAM_VALID_AREA_CODE = "mobileapp.valid.areaCode";
    public final static String 	PARAM_VALID_MOBILE_DIGIT = "mobileapp.valid.mobileDigit";
    public final static String IS_IMAGE_URL = "IS_IMAGE_URL";
    public final static String FILE_EXTENSION_ALLOWED =  "mobileapp.filestream.extension";
    //RBT-13982
    public final static String PARAM_IS_RELATIVE_PATH ="mobileapp.is.relative.path";
    public final static String 	PARAM_MODE_FOR_GET_SELECTIONS = "mobileapp.mode.for.getSelection";
    
    public final static String PARAM_ENABLE_ENCRYPTION = "mobileapp.enable.encryption";
    public final static String PARAM_REQUEST_SUBSCRIBERID_ENCRYPTION_KEY = "mobileapp.request.subscriberId.encryption.key";
    public final static String PARAM_REQUEST_UNIQUEID_ENCRYPTION_KEY = "mobileapp.request.uniqueId.encryption.key";
    public final static String PARAM_RESPONSE_SUBSCRIBERID_ENCRYPTION_KEY = "mobileapp.response.subscriberId.encryption.key";
    public final static String PARAM_MOBILEAPP_CONSENT_CG_URL = "mobileapp.consent.cg.url";
    public final static String PARAM_MOBILEAPP_CONSENT_CONSENT_CG_URL = "mobileapp.consent.comviva.cg.url";
    public final static String PARAM_MOBILEAPP_CONSENT_CG_URL_TIMESTAMP_FORMAT = "mobileapp.consent.cg.url.timestamp.format";
    public final static String PARAM_MOBILEAPP_CONSENT_CG_URL_TIMESTAMP_TIME_ZONE = "mobileapp.consent.cg.url.timestamp.time.zone";
	
	//rurl for comviva 
	public final static String PARAM_MOBILEAPP_R_URL = "mobileapp.r.url";
    
    public final static String PARAM_IS_QUERY_STRING_ENCRYPTION_ENABLED = "mobileapp.is.query.string.encryption.enabled";
    public final static String PARAM_REQUEST_ENCRYPTION_KEY = "mobileapp.request.encryption.key";
    public final static String PARAM_OSTYPES_FOR_ENCRYPTION = "mobileapp.ostypes.for.encryption";

	public static final String PARAM_OSTYPE_MODE_MAP = "mobileapp.ostype.mode.map";
	public static final String PARAM_LOCALE_CONFIG = "mobileapp.locale.config";
	public static final String PARAM_CURRENT_VERSION = "mobileapp.current.version";
	public static final String PARAM_MANDATORY_TO_UPGRADE = "mobileapp.mandatory.to.upgrade";
	public static final String PARAM_CONSENT_RETURN_YES_URL = "mobileapp.consent.return.yes.url";
	public static final String PARAM_CONSENT_RETURN_NO_URL = "mobileapp.consent.return.no.url";
	public static final String PARAM_PACKAGE_OFFER_SUPPORTED = "mobileapp.package.offer.supported";
	public static final String PARAM_OFFER_DESCRIPTION = "mobileapp.offer.description";
	public static final String PARAM_TO_BE_MERGE_DOWNLOAD_SELECTION = "mobileapp.to.be.merge.download.selection";
	public static final String PARAM_SUBSCRIPTION_AMOUNT = "mobileapp.subscription.amount";
	public static final String PARAM_SUBSCRIPTION_PERIOD = "mobileapp.subscription.period";

	public static final String PARAM_CONSENT_REFID_PREFIX_RBT_ACT = "mobileapp.consent.refid.prefix.rbt.act";
	public static final String PARAM_CONSENT_REFID_PREFIX_RBT_SEL = "mobileapp.consent.refid.prefix.rbt.sel";
	public static final String PARAM_CONSENT_REFID_PREFIX_CP_ID = "mobileapp.consent.refId.prefix.cpid";
	public static final String PARAM_CONSENT_INFO_COMBO = "mobileapp.consent.info.combo";
	public static final String PARAM_CONSENT_INFO_BASE = "mobileapp.consent.info.base";
	public static final String PARAM_CONSENT_INFO_SEL = "mobileapp.consent.info.sel";
	public static final String PARAM_CONSENT_CONSTANT_PRECHARGE = "mobileapp.consent.constant.precharge";
	public static final String PARAM_CONSENT_CONSTANT_ORIGINATOR = "mobileapp.consent.constant.originator";
	public static final String PARAM_CONSENT_IMAGE_PREFIX_URL = "mobileapp.consent.image.prefix.url";
	public static final String PARAM_CONSENT_DEAFULT_IMAGE_PATH = "mobileapp.consent.default.image.path";
	public static final String PARAM_CONSENT_USERID = "mobileapp.consent.userId";
	public static final String PARAM_CONSENT_PASSWORD = "mobileapp.consent.password";
	public static final String PARAM_CONSENT_RETURN_MESSAGE = "mobileapp.consent.return.message";
	public static final String PARAM_CONSENT_RETURN_SUCCESS_MESSAGE = "mobileapp.consent.return.success.message";
	public static final String PARAM_CONSENT_RETURN_LOW_BALANCE_MESSAGE = "mobileapp.consent.return.low.balance.message";
	public static final String PARAM_CONSENT_RETURN_TECHNICAL_DIFFICULTY_MESSAGE = "mobileapp.consent.return.technical.difficulty.message";
	public static final String PARAM_CONSENT_RETURN_FAILURE_MESSAGE = "mobileapp.consent.return.failure.message";
	public static final String PARAM_HT_CONSENT_URL = "mobileapp.ht.consent.url";
	public static final String PARAM_TOP_PLAYLISTS_CATEGORY_ID = "mobileapp.top.playlists.category.id";
	public static final String PARAM_OTHER_PLAYLISTS_CATEGORY_ID = "mobileapp.other.playlists.category.id";
	
	public static final String PARAM_CPID_DEFAULT_NAME = "mobileapp.cpid.default.name";
	public static final String PARAM_CPID_DEFAULT_ID = "mobileapp.cpid.default.id";
	public static final String PARAM_CONSENT_THIRD_PARTY_CIRCLE_ID_MAPPING = "mobileapp.consent.third.party.circle.id.mapping";
	public static final String PARAM_CONSENT_MSISDN_PREFIX = "mobileapp.consent.msisdn.prefix";
	
	public final static String PARAM_MSISDN_HEADER_NAMES = "mobileapp.msisdn.header.names";
	public static final String PARAM_RESPONSE_FOR_GET_SUBSCRIBER_ID = "mobileapp.response.for.getSubsciberId";
	public static final String PARAM_RESPONSE_FOR_REGISTRATION = "mobileapp.response.for.registration";
	public static final String PARAM_SELECTION_PERIOD_DESCRIPTION = "mobileapp.selection.period.description";
	public static final String PARAM_SUGGESTION_SEARCH_URL = "mobileapp.suggestion.search.url";
	
	public static final String PARAM_CATEGORY_ROW_COUNT = "mobileapp.category.row.count";
	public static final String PARAM_FREEMIUM_CATEGORY_ID = "mobileapp.freemium.category.id";
	public static final Object PARAM_FREEMIUM_CATEGORY_ID_FOR_CLIPS = "mobileapp.freemium.category.id.for.clips";
	public static final String PARAM_CONSENT_FLOW_MAKE_ENTRY_IN_DB = "mobileapp.consent.flow.make.entry.in.db";
	public static final String PARAM_IS_SOLR_NAMETUNE_SEARCH = "mobileapp.is.solr.nametune.search";
	
	public static final String PARAM_CONSENT_FLOW_IS_GENERATE_REFID = "mobileapp.consent.flow.is.generate.refId";
	public static final String PARAM_CATEGORY_TYPE_MAPPING = "mobileapp.category.type.mapping";
	public static final String PARAM_ARTIST_ROW_COUNT = "mobileapp.artist.row.count";
	public static final String IS_CIRCLE_ID_REQUIRED_IN_GET_SUBSCRIBER_ID_RESP = "mobileapp.is.circle.id.required.in.get.subscriber.id.resp";
	public static final String IS_CIRCLE_ID_REQUIRED_IN_LOGIN_USER_RESP = "mobileapp.is.circle.id.required.in.login.user.resp";

	public static final String REALTIME_SONG_NOTIFICATION_CONTENT_TYPES = "REALTIME_SONG_NOTIFICATION_CONTENT_TYPES" ;
	
	//RBT-14540
	public static final String PARAM_DELTA_CONTENT_HEADER_DATE_FORMAT = "mobileapp.delta.content.header.date.format";
	public static final Object PARAM_SUPPORTED_PARAM_TYPES = "mobileapp.supported.param.types";
	public final static String PARAM_MOBILEAPP_NAME_TUNE_CREATION_URL = "mobileapp.nametune.creation.url";
	public final static String PARAM_MOBILEAPP_LANGUAGE = "mobileapp.nametune.online.api.language";
	public final static String PARAM_MOBILEAPP_NAME_TUNE_ONLINE_API_REQUIRED = "mobileapp.nametune.online.api.required";
	
	//RBT-16263	Unable to remove selection for local/site RBT user
	public final static String PARAM_MOBILEAPP_RDC = "mobileapp.rdc";
	public static final String PARAM_ERROR_CODE_MESSAGE = "mobileapp.error.message.response";
	public static final String PARAM_UBONA_LANGUAGE_MAPPING = "mobileapp.ubona.language.mapping";
	public static final String PARAM_UBONA_CIRCLEID_MAPPING = "mobileapp.ubona.circleid.mapping";
	public static final String PARAM_UBONA_FEEDBACK_URL = "mobileapp.ubona.feedback.url";

}
