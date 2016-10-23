package com.onmobile.apps.ringbacktones.rbt2.common;


public interface BeanConstant {

	//Bean Name for Spring
	public static final String UDP_RBT_SERVICE_IMPL = "udpRBTServiceImpl";
	public static final String SUBSCRIBER_SELECTION_IMPL = "subscriberSelectionImpl";
	public static final String UDP_DAO_IMPL = "udpDaoImpl";
	public static final String SELECTION_WEB_SERVICE = "selectionWebService";
	public static final String SELECTION_SERVICE = "selectionService";
	public static final String UDP_RBT_CONTENT_SERVICE_IMPL = "udpRBTContentServiceImpl";
	public static final String CONVERTER_HELPER_UTIL = "helperUtil";
	public static final String RBT_SELECTION_SERVICE_IMPL = "rbtSelectionServiceImpl";
	public static final String GROUP_MEMBER_PROCESSOR = "groupMemberProcessor";
	public static final String BASIC_RBT_SUBSCRIPTION_SERVICE_IMPL = "basicRBTSubscriptionServiceImpl";
	public static final String BASIC_VOLTRON_SUBSCRIPTION_SERVICE_IMPL = "basicVoltronSubscriptionServiceImpl";
	public static final String BASIC_SUBSCRIPTION_PROCESSOR = "basicSubscriptionProcessor";
	public static final String SUBSCRIBER_IMPL ="subscriberImpl";
	public static final String FEATURE_LIST_RESTRICTION_COMMAND_LIST = "featureListRestrictionCommandList";
	public static final String SERVICE_MAPPING_BEAN = "serviceMappingBean";
	public static final String RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN = "rbtOperatorUserDetailsMappingBean";
	public static final String B2B_MIGRATION_LIST = "b2bMigrationList";
	public static final String B2B_MIGRATION_IMPL = "b2bMigrationImpl";
	
	public static final String CLIP_UTIL_SERVICE_MAPPING_BEAN = "clipUtilServiceMappingBean";

	public static final String SELECTION_PROCESSOR_BEAN = "selectionProcessorImpl";
	public static final String THREAD_EXECUTOR = "threadExecutor";
	public static final String ADD_SELECTION_TP = "addSelToTonePlayer";
	public static final String PROCESSING_CLIP_TRANSFER = "processingClipTransfer";
	public static final String WAV_FILE_MAPPING_DAO ="wavFileMappingDAOImpl";
	public static final String CLIP_STATUS_MAPPING_DAO = "clipStatusMappingDAOImpl";
	public static final String OPERATOR_CIRCLE_MAPPING_DAO ="operatorCircleMappingDAOImpl";
	public static final String PROPERTY_CONFIG = "propertyConfig";
	public static final String LOAD_WAVE_FILE_MAPPING_FOR_2_0 = "loadWavFileMappingToMapping";
	public static final String ASSET_TYPE_ADAPTER = "assetTypeAdapter";
	
	//Message for Invalid Bean Name
	public static final String ILLEGAL_BEAN_NAME = "illegal_bean_name";
	public static final String BEAN_NOT_CONFIGURED = "bean_not_configured";
	
	
	//Integration Bean
	public static final String INTEGRATION_HELPER_BEAN = "integrationHelperBean";
	public static final String RESPONSE_HANDLER_BEAN = "responseHandlerBean";
	public static final String GET_SM_URL_IMPL = "smURLServiceBean";
	public static final String GET_NEXT_CHARGE_CLASS_IMPL = "getNextChargeClassImpl";
	public static final String CHARGE_CLASS_PROCESSOR = "chargeClassProcessor";
	
	//Bean for UGC DAO
	public static final String UGC_WAV_FILE_DAO = "rbtUgcWavfileDao";
	

	//Bean for UGC service
	public static final String FILE_DOWNLOAD_SERVICE = "fileDownloadService_";
	public static final String UGC_THREAD_EXECUTOR = "ugcThreadExecutor";

	//AssetBuilder bean
	public static final String SONG_ASSET_UTIL_BUILDER = "song";
	public static final String UGC_ASSET_UTIL_BUILDER = "rbtugc";
	public static final String UDP_SHUFFLELIST_ASSET_UTIL_BUILDER = "shufflelist";
	public static final String DEFAULT_ASSET_UTIL_BUILDER = "defaultassetbuilder";
	public static final String SHUFFLE_CATEGORY_ASSET_UTIL_BUILDER = "shufflecategory";
	public static final String RBT_PROFILE_TONE_ASSET_UTIL_BUILDER = "rbtprofiletone";
	
	
	//logger Beans
	public static final String DOWNLOAD_ACT_CDR_LOGGER_BEAN = "downloadACTcdrLogger";
	public static final String DOWNLOAD_DCT_CDR_LOGGER_BEAN = "downloadDCTcdrLogger";
	public static final String SELECTION_ACT_CDR_LOGGER_BEAN = "selectionACTcdrLogger";
	public static final String SELECTION_DCT_CDR_LOGGER_BEAN = "selectionDCTcdrLogger";
	public static final String CDR_LOGGER_DTO_BEAN = "cdrLoggerDTO";
	public static final String CUTRBT_LOGGER_DTO_BEAN = "cutRbtLoggerDTO";
	public static final String SELECTION_ACT_CUTRBT_LOGGER_BEAN = "selectionACTcutRbtLogger";


	
	public static final String DOWNLOAD_SERVICE_IMPL = "downloadService";
	
	//Combo Request Service
	public static final String COMBO_REQUEST_SERVICE = "comboReqService";
	
	//ComboReqHandlerBeans
	public static final String SUB_PURCHASE_SET_COMBO_HANDLER = "subPurchaseSetComboHandler";
	public static final String SUB_PURCHASE_COMBO_HANDLER = "subPurchaseComboHandler";
	public static final String PURCHASE_SET_COMBO_HANDLER = "purchaseSetComboHandler";
	
	//OperatorConsentUtility
	public static final String OPERATOR_AIRTEL = "airtel";
	public static final String OPERATOR_IDEA = "idea";
	public static final String OPERATOR_VODAFONE = "vodafone";
	public static final String OPERATOR_AIRTEL_COMVIVA = "airtel_comviva";
	public static final String OPERATOR_TATA = "tata";
	public static final String OPERATOR_BSNL = "bsnl";
	public static final String OPERATOR_BSNL_EAST = "bsnl_east";
	public static final String OPERATOR_BSNL_SOUTH = "bsnl_south";
	
	
	//Consent Return Service
	public static final String CONSENT_RETURN_SERVICE = "consentReturnServiceImpl";
	
	// request resolver bean name
	public static final String SELECTION_REQUEST_RESOLVER = "selectionRequestResolver";
	public static final String NEXTCHARGECLASS_RESOLVER = "nextChargeClassResolver";
	public static final String DTOC_PROCESSOR_CLASS =  "dtocProcessor";
	public static final String DOWNLOAD_REQUEST_RESOLVER = "downloadRequestResolver";
	
	//ComboReqHandlerBeans
	public static final String SUB_PURCHASE_SET_COMBO_REQUEST_HANDLER = "subPurchaseSetComboRequestHandler";
	public static final String SUB_PURCHASE_COMBO_REQUEST_HANDLER = "subPurchaseComboRequestHandler";
	public static final String PURCHASE_SET_COMBO_REQUEST_HANDLER = "purchaseSetComboRequestHandler";
	
	public static final String SUBSCRIPTION_PROCESSOR_BEAN = "subscriptionProcessorImpl";

	public static final String USER_DETAIL_BEAN = "operatorUserDetailsImpl";

	public static final String INLINE_MESSAGE_SENDER_BEAN = "messageSender";
	
	public static final String RBT_DEAMEON_HELPER_UTIL = "rbtDeamonHelperUtil";
	
	public static final String TATA_GSM_DATE_UTIL = "tataGsmDateUtil";
	
	//RBT-18975
	public static final String AIRTEL_USER_SELECTION_RESTRICT_BASED_ON_SUBCLASS = "userSelectionRestrictionBasedOnSubClass";

}

