package com.onmobile.apps.ringbacktones.rbt2.service.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

/**
 * 
 * @author md.alam
 *
 */
 
public class ConsentPropertyConfigurator {
	
	private static Logger logger = Logger.getLogger(ConsentPropertyConfigurator.class);
	private static Map<String, Parameter> paramMap = new HashMap<String, Parameter>();
	private static boolean isDownloadsModel = false;
	private static final String OPERATOR_NAME = "OPERATOR_NAME";
	private static final String RBT_OPERATOR_USER_DETAILS_URL = "RBT_OPERATOR_USER_DETAILS_URL";
	private static final String RBT_D2C_MIGRATION_URL = "RBT_D2C_MIGRATION_URL";
	
	static {

		RBTClient client = RBTClient.getInstance();
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		//TODO type??
		request.setType("WEB");
		Parameter[] parameters = client.getParameters(request);
		for(Parameter parameter: parameters){
			paramMap.put(parameter.getName(),parameter);
		}
		logger.info("parameter======================: "+parameters);
		request = new ApplicationDetailsRequest();
		request.setType("COMMON");
		request.setName("ADD_TO_DOWNLOADS");
		Parameter downloadsParam = client.getParameter(request);
		logger.info("downloadsParam======================: "+downloadsParam);
		if (downloadsParam != null && downloadsParam.getValue() != null)
			isDownloadsModel = downloadsParam.getValue().equalsIgnoreCase("TRUE");


		try {
			//TODO config??
			ResourceBundle resourceBundle = ResourceBundle.getBundle("config"); 
			if (resourceBundle != null) {
				logger.info("Loading properties from config.properties file.");
				Enumeration<String> keys = resourceBundle.getKeys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (paramMap.containsKey(key)) {
						logger.warn("Duplicate property in DB and in properties file. Preference given to the property in the later. Key: " + key);
					}
					//TODO type?
					Parameter param = new Parameter("MOBILEAPP", key, resourceBundle.getString(key));
					paramMap.put(key, param);
				}
			}
		} catch(MissingResourceException e) {
			logger.warn("config.properties file not found in classpath.", e);
		}
	
	}

	
	public static boolean isDownloadsModel() {
		return isDownloadsModel;
	}
	
	public static String getConsentRefIdPrefixRbtAct() {
		String consentRefIfPrefixRbtAct = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_REFID_PREFIX_RBT_ACT*/"webservice.consent.refid.prefix.rbt.act");
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIfPrefixRbtAct = param.getValue();
		}
		return consentRefIfPrefixRbtAct;
	}
	
	public static String getConsentRefIdPrefixRbtSel() {
		String consentRefIfPrefixRbtSel = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_REFID_PREFIX_RBT_SEL*/"webservice.consent.refid.prefix.rbt.sel");
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIfPrefixRbtSel = param.getValue();
		}
		return consentRefIfPrefixRbtSel;
	}
	
	public static String getConsentRefIdPrefixCPId() {
		String consentRefIdPrefixCPId = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_REFID_PREFIX_CP_ID*/"webservice.consent.refId.prefix.cpid");
		logger.info("param :==>" + param);
		if(param != null) {
			consentRefIdPrefixCPId = param.getValue();
		}
		return consentRefIdPrefixCPId;
	}
	
	
	public static String getConsentInfoCombo() {
		String consentInfoCombo = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_INFO_COMBO*/"webservice.consent.info.combo");
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoCombo = param.getValue();
		}
		return consentInfoCombo;
	}

	public static String getConsentInfoBase() {
		String consentInfoBase = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_INFO_BASE*/"webservice.consent.info.base");
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoBase = param.getValue();
		}
		return consentInfoBase;
	}
	
	public static String getConsentInfoSel() {
		String consentInfoSel = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_INFO_SEL*/"webservice.consent.info.sel");
		logger.info("param :==>" + param);
		if(param != null) {
			consentInfoSel = param.getValue();
		}
		return consentInfoSel;
	}
	
	public static String getCgCpId(String atlantisCpId) {
		String cgCpId = null;
		String paramName = "cpid." + atlantisCpId + ".id";
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			cgCpId = param.getValue();
		}
		return cgCpId;
	}
	
	public static String getCgCpName(String atlantisCpId) {
		String cgCpName = null;
		String paramName = "cpid." + atlantisCpId + ".name";
		logger.debug("paramName: " + paramName);
		Parameter param = paramMap.get(paramName);
		logger.info("param :==>" + param);
		if(param != null) {
			cgCpName = param.getValue();
		}
		return cgCpName;
	}
	
	public static String getDefaultCgCpId() {
		String defaultCgCpId = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CPID_DEFAULT_ID*/"webservice.cpid.default.id");
		logger.info("param :==>" + param);
		if(param != null) {
			defaultCgCpId = param.getValue();
		}
		return defaultCgCpId;
	}
	
	public static String getDefaultCgCpName() {
		String defaultCgCpName = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CPID_DEFAULT_NAME*/"webservice.cpid.default.name");
		logger.info("param :==>" + param);
		if(param != null) {
			defaultCgCpName = param.getValue();
		}
		return defaultCgCpName;
	}
	
	public static String getConsentPrecharge() {
		String consentPrecharge = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_CONSTANT_PRECHARGE*/"webservice.consent.constant.precharge");
		logger.info("param :==>" + param);
		if(param != null) {
			consentPrecharge = param.getValue();
		}
		return consentPrecharge;
	}

	public static String getConsentOriginator() {
		String consentOriginator = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_CONSTANT_ORIGINATOR*/"webservice.consent.constant.originator");
		logger.info("param :==>" + param);
		if(param != null) {
			consentOriginator = param.getValue();
		}
		return consentOriginator;
	}

	public static String getConsentImagePrefixUrl() {
		String consentImagePrefixUrl = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_IMAGE_PREFIX_URL*/"webservice.consent.image.prefix.url");
		logger.info("param :==>" + param);
		if(param != null) {
			consentImagePrefixUrl = param.getValue();
		}
		return consentImagePrefixUrl;
	}

	public static String getConsentDefaultImagePath() {
		String consentDefaultImagePath = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_DEAFULT_IMAGE_PATH*/"webservice.consent.default.image.path");
		logger.info("param :==>" + param);
		if(param != null) {
			consentDefaultImagePath = param.getValue();
		}
		return consentDefaultImagePath;
	}
	
	public static String getConsentUserId() {
		String consentUserId = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_USERID*/"webservice.consent.userId");
		logger.info("param :==>" + param);
		if(param != null) {
			consentUserId = param.getValue();
		}
		return consentUserId;
	}
	
	public static String getConsentPassword() {
		String consentPassword = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_PASSWORD*/"webservice.consent.password");
		logger.info("param :==>" + param);
		if(param != null) {
			consentPassword = param.getValue();
		}
		return consentPassword;
	}
	
	public static String getConsentMsisdnPrefix() {
		String consentMsisdnPrefix = null;
		Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_MSISDN_PREFIX*/"webservice.consent.msisdn.prefix");
		logger.info("param :==>" + param);
		if(param != null) {
			consentMsisdnPrefix = param.getValue();
		}
		return consentMsisdnPrefix;
	}
	
	public static String getThirdPartyCircleIdMapping() {
		String thirdPartyCircleIdMapping = null;
	//	Parameter param = paramMap.get(/*StringConstants.PARAM_CONSENT_THIRD_PARTY_CIRCLE_ID_MAPPING*/"webservice.consent.third.party.circle.id.mapping");
		Parameter param = paramMap.get("webservice.consent.third.party.circle.id.mapping");
		logger.info("param :==>" + param);
		if(param != null) {
			thirdPartyCircleIdMapping = param.getValue();
		}
		return thirdPartyCircleIdMapping;
	}
	
	public static String getParameterValue(String param)
	{
		Parameter parameter = paramMap.get(param);
		logger.info("param:"+param);
		if (param != null && parameter != null) {
			return parameter.getValue();
		}
		return null;
	}
	
	public static boolean isConsentFlowIsGenerateRefId() {
		boolean ConsentFlowIsGenerateRefId = false;
		Parameter param = paramMap.get("webservice.consent.flow.is.generate.refId");
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			ConsentFlowIsGenerateRefId = true;
		}
		logger.debug("ConsentFlowIsGenerateRefId: " + ConsentFlowIsGenerateRefId);
		return ConsentFlowIsGenerateRefId;	
	}
	
	public static boolean isConsentFlowMakeEntryInDB() {
		boolean consentFlowMakeEntryInDB = false;
		Parameter param = paramMap.get("webservice.consent.flow.make.entry.in.db");
		logger.info("param :==>" + param);
		if (param!= null && param.getValue().toUpperCase().equals("TRUE")) {
			consentFlowMakeEntryInDB = true;
		}
		logger.debug("consentFlowMakeEntryInDB: " + consentFlowMakeEntryInDB);
		return consentFlowMakeEntryInDB;
	}

	public static Map<String, Parameter> getParamMap() {
		return paramMap;
	}
	
	

	public static String getOperatorFormConfig() {
		Parameter parameter = paramMap.get(OPERATOR_NAME);
		logger.info("param:" + parameter);
		if (parameter != null) {
			return parameter.getValue();
		}
		return null;
	}
	

	public static String getFreeClipCategoryID() {
		Parameter parameter = paramMap.get("specific.catergory.id");
		logger.info("param:" + parameter);
		if (parameter != null) {
			return parameter.getValue();
		}
		return "103";
	}
	
	public static String getRBTOperatorUserInfoURLFormConfig() {
		Parameter parameter = paramMap.get(RBT_OPERATOR_USER_DETAILS_URL);
		logger.info("param:" + parameter);
		if (parameter != null) {
			return parameter.getValue();
		}
		return null;
	}
	
	
	public static String getRBTD2CMigrationURLFormConfig() {
		Parameter parameter = paramMap.get(RBT_D2C_MIGRATION_URL);
		logger.info("param:" + parameter);
		if (parameter != null) {
			return parameter.getValue();
		}
		return null;
	}
	
	
}
