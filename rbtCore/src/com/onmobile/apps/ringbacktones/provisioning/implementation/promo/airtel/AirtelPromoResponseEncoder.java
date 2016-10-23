package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.airtel;

import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoResponseEncoder;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class AirtelPromoResponseEncoder extends PromoResponseEncoder implements iRBTConstant {
	//MOD responses
	private final String MOD_ERROR = "-1";
	private final String MOD_SEL_SUCCESS = "0";
	private final String MOD_SUB_SEL_SUCCESS = "1";
	private final String MOD_SELECTION_FAILED = "2";
	private final String MOD_SUBSCRIPTION_FAILED = "3";
	private final String MOD_WAITING_USER = "4";
	private final String MOD_SUBSCRIBER_INVALID = "5";
	private final String MOD_VCODE_INVALID = "6";
	private final String MOD_SUSPENDED_USER = "7";
	private final String MOD_ALBUM_FULL = "8";
//	private final String MOD_COPY_PENDING = "9";
	private final String MOD_TECHNICAL_DIFFICULTIES = "10";
//	private final String MOD_GIFT_ACTIVATION_PENDING = "11";
	private final String MOD_RENEWAL_PENDING = "12";
//	private final String MOD_SONG_SET_DOWNLOAD_ALREADY_EXISTS = "13";
	private final String MOD_SONG_SET_SELECTION_ALREADY_EXISTS = "14";
	private final String MOD_LITE_USER_PREMIUM_BLOCKED = "15";
	private final String MOD_SONG_NOT_SET = "16";
//	private final String MOD_MAX_CALLER_SELECTIONS_REACHED = "17";
	
	//ENVIO responses
	private final String ENVIO_COPY_ALL_SUCCESS = "0";
	private final String ENVIO_COPY_CALLER_SUCCESS = "1";
	private final String ENVIO_COPY_ALL_SUCCESS_NEW_SUB = "2";
	private final String ENVIO_COPY_CALLER_SUCCESS_NEW_SUB = "3";
	private final String ENVIO_CALLER_SETTING_FULL = "4";
	private final String ENVIO_SUBSCRIBER_INVALID = "5";
	private final String ENVIO_DST_CALLER_INVALID = "6";
	private final String ENVIO_VCODE_INVALID = "7";
	private final String ENVIO_SEL_SUCCESS = "8";
	private final String ENVIO_SUB_SEL_SUCCESS = "9";
	private final String ENVIO_ERROR = "10";
	private final String ENVIO_SUBSCRIBER_EXISTS = "11";
	private final String ENVIO_SUBSCRIBER_NOT_EXISTS = "12";
	private final String ENVIO_COPY_ALL_FAILURE = "13";
	private final String ENVIO_COPY_CALLER_FAILURE = "14";
	private final String ENVIO_COPY_ALL_FAILURE_NEW_SUB = "15";
	private final String ENVIO_COPY_CALLER_FAILURE_NEW_SUB = "16";
	private final String ENVIO_SEL_FAILURE = "17";
	private final String ENVIO_SUB_SEL_FAILURE = "18";
	private final String ENVIO_FLAG_INVALID = "19";
	private final String ENVIO_SUB_UNSUB_PENDING = "20";
	private final String ENVIO_DST_CALLER_NOT_EXITS = "21";
	private final String ENVIO_CALLER_INVALID = "22";
	//	added in the 2nd phase
	private final String ENVIO_SUB_WAITING_OR_HLR_REMOVE = "23";
	private final String ENVIO_SEL_DELETION_SUCCESSFUL = "24";
	private final String ENVIO_SEL_DELETION_FAILED = "25";
	private final String ENVIO_SEL_DELETION_INVALID = "26";
	private final String ENVIO_GIFT_SUCCESSFUL = "27";
	private final String ENVIO_GIFT_FAILED = "28";
	private final String ENVIO_GIFTEE_INBOX_FULL = "29";
	private final String ENVIO_ALBUM_SEL_SUCCESS = "30";
	private final String ENVIO_SUB_ADVANCE_RENTAL_USER = "31";
	private final String ENVIO_ALBUM_SEL_FAILED = "32";
	private final String ENVIO_DSTMSISDN_WAITING = "33";
	private final String ENVIO_HLR_REMOVE_OR_SUSPENDED_USER = "34";
	private final String ENVIO_VCODE_EXPIRED = "35";
	private final String ENVIO_VCODE_INVALID_FOR_COPY = "36";
	private final String ENVIO_INDEX_INVALID = "37";
	private final String ENVIO_SUB10_SUCCESS = "42";
	private final String ENVIO_SUB20_SUCCESS = "43";
	private final String ENVIO_SUB10_FAILURE = "44";
	private final String ENVIO_SUB20_FAILURE = "45";
	private final String ENVIO_ALBUM_FULL = "48";
	
	//Autodial Responses
	private final String AUTO_SUCCESS = "0";
	private final String AUTO_SUBSCRIBER_EXISTS = "1";
	private final String AUTO_SUBSCRIBER_NOT_EXISTS = "2";
	private final String AUTO_ACT_DEACT_PENDING = "3";
	private final String AUTO_SUBSCRIPTION_FAILED = "4";
	private final String AUTO_SELECTION_FAILED = "5";
	private final String AUTO_SUBSCRIBER_INVALID = "6";
	private final String AUTO_VCODE_INVALID = "7";
	private final String AUTO_ERROR = "-1";
	//added in the 2nd phase
	private final String AUTO_CALLER_SETTING_FULL = "8";
	private final String AUTO_STYPE_INVALID = "9";
	private final String AUTO_UCODE_INVALID = "10";
	private final String AUTO_CALLER_INVALID = "11";
	private final String AUTO_FLAG_INVALID = "12";
	private final String AUTO_SUBSCRIBER_SUSPENDED = "13";
	//added by Sreekar
	private final String AUTO_ALBUM_CODE_INVALID = "14";
	//added by Sreekar
	private final String AUTO_ALBUM_FULL = "15";
	
	//Easycharge Responses
	private static String EC_0_SUCCESS = "0";
	private static String EC_0_SUBSCRIBER_EXISTS = "1";
	private static String EC_0_SEL_FAILURE = "2";
	private static String EC_0_SUB_FAILURE = "4";
	private static String EC_0_SUBSCRIBER_EXISTS_PENDING = "5";
	private static String EC_0_SUBSCRIBER_INVALID = "6";
	private static String EC_0_VCODE_INVALID = "7";
	private static String EC_0_ERROR = "-1";
	private static String EC_0_SUBSCRIBER_SUSPENDED = "9";
	
	private static String EC_1_SUCCSS = "0";
	private static String EC_1_SEL_FAILURE = "3";
	private static String EC_1_SUBSCRIBER_NOT_EXISTS = "2";
	private static String EC_1_SUCCESS_LIGHT = "4";
	private static String EC_1_SUBSCRIBER_EXISTS_PENDING = "5";
	private static String EC_1_SUBSCRIBER_INVALID = "6";
	private static String EC_1_VCODE_INVALID = "7";
	private static String EC_1_ERROR = "-1";
	private static String EC_1_SUBSCRIBER_SUSPENDED = "9";
	private static String EC_1_TONE_ALREADY_EXISTS = "10";

	private static String EC_2_SUCCESS = "0";
	private static String EC_2_SUBSCRIBER_EXISTS = "1";
	private static String EC_2_SEL_FAILURE = "2";
	private static String EC_2_SUB_FAILURE = "4";
	private static String EC_2_SUBSCRIBER_EXISTS_PENDING = "5";
	private static String EC_2_SUBSCRIBER_INVALID = "6";
	private static String EC_2_VCODE_INVALID = "7";
	private static String EC_2_TARIFF_CODE_INVALID = "8";
	private static String EC_2_ERROR = "-1";
	private static String EC_2_SUBSCRIBER_SUSPENDED = "9";
	
	private static String EC_7_SUCCESS = "0";
	private static String EC_7_SUBSCRIBER_EXISTS = "1";
	private static String EC_7_SEL_FAILURE = "2";
	private static String EC_7_SUB_FAILURE = "4";
	private static String EC_7_CCODE_INVALID = "8";
	private static String EC_7_ERROR = "-1";
	private static String EC_7_TONE_ALREADY_EXISTS = "10";
	private static String EC_7_FAILURE = "11";

	//USSD Responses
	private final int USSD_SUCCESS = 200;
	private final int USSD_UNKNOWN_ERROR = 400;
	private final int USSD_SUBSCRIBER_INVALID = 410;
	private final int USSD_COMMAND_INVALID = 420;
	private final int USSD_VCODE_INVALID = 430;
	private final int USSD_VCODE_NOT_FOUND = 431;
	private final int USSD_VCODE_EXPIRED = 432;
	private final int USSD_CALLER_INVALID = 440;
	private final int USSD_DST_CALLER_INVALID = 441;
	private final int USSD_INTERNAL_SERVER_ERROR = 500;
	private final int USSD_REQUEST_TIME_OUT = 510;
	private final int USSD_SUBSCRIBER_AUTORBT_SUB = 450;
	private final int USSD_DST_CALLER_AUTORBT_SUB = 451;
	private final int USSD_COPY_ALBUM_BLOCKED = 442;

	public AirtelPromoResponseEncoder() throws Exception {
		super();
		logger = Logger.getLogger(AirtelPromoResponseEncoder.class);
	}
	
	public String encode(Task task) {
		if(task.containsKey(param_REQUEST))
			return super.encode(task);
		String response = "";
		String taskAction = task.getTaskAction();
		if(!task.containsKey(param_response)) {
			logger.error("RBT::no response populated taskAction-" + taskAction + "taskSession-" + task.getTaskSession());
			task.setObject(param_response, Resp_Err);
		}
		else
			logger.info("RBT::response-" + task.getString(param_response));
		if(taskAction.equals(request_mod))
			response = getModResponse(task);
		if(taskAction.equals(request_envio)) {
			String code = getEnvioResponse(task);
			String responseString = task.getString(param_respMessage);
			if(responseString == null)
				responseString = getEnvioResponseString(code);
			response = "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">"
					+ code
					+ "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>"
					+ responseString + "</td> <!-- response message (msg) --></tr></table></body></html>";
		}
		if(taskAction.equals(request_autodial))
			response = getAutodialResponse(task);
		else if(taskAction.equals(request_ec)) {
			String code = getEasychargeResponse(task);
			String responseString = getEasychargeResponseString(code, task);
			response = "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">"
					+ code
					+ "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>"
					+ responseString
					+ "</td> <!-- response message (msg) --></tr></table></body></html>";
		}
		else if(taskAction.equals(request_ussd)) {
			int code = getUSSDResponse(task);
			String responseString = getUSSDResponseString(code);
			response = code + ":" + responseString;
		}
		return response;
	}
	
	private int getUSSDResponse(Task task) {
		String response = task.getString(param_response);
		if(response.equals(Resp_InvalidPrefix) || response.equals(Resp_BlackListedNo) || response.equals(Resp_SuspendedNo))
			return USSD_SUBSCRIBER_INVALID;
		else if(response.equals(Resp_ClipNotAvailable))
			return USSD_VCODE_NOT_FOUND;
		else if(response.equals(Resp_ClipExpired))
			return USSD_VCODE_EXPIRED;
		else if(response.equals(Resp_invalidParam)) {
			String invalidParam = task.getString(param_invalidParam);
			if(invalidParam.equals(param_ussd_cmd))
				return USSD_COMMAND_INVALID;
			if(invalidParam.equals(param_ussd_cbsMsisdn))
				return USSD_CALLER_INVALID;
			if(invalidParam.equals(param_ussd_dstMsisdn) || invalidParam.equals(param_CALLER_ID))
				return USSD_DST_CALLER_INVALID;
		}
		else if(response.equals(Resp_Success))
			return USSD_SUCCESS;
		
		return USSD_UNKNOWN_ERROR;
	}
	
	private String getUSSDResponseString(int code) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "USSD_RESPONSE_"+ code);
		if(param != null)
			return param.getValue();
		return null;
	}
	
	private String getEasychargeResponseString(String code, Task task) {
		int flag = Utility.getIntFromStr(task.getString(param_ec_Flag));
		if(flag == -1)
			flag = 0;
		if((flag > 2 && flag < 7) || flag == 11|| flag == 12|| flag == 13|| flag == 14)
			flag = 2;
		return getEasychargeResponseString(code, flag);
	}
	
	private String getEasychargeResponseString(String code, int flag) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR","EC_RESPONSE_" + flag + "_" + code);
		if(param != null)
			return param.getValue();
		return null;
	}
	
	private String getEasychargeResponse(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		int flag = Utility.getIntFromStr(task.getString(param_ec_Flag));
		String response = task.getString(param_response);
		String errorRespErroCode = RBTParametersUtils.getParamAsString("COMMON", "EC_ERROR_RESP_ERROR_CODE_MAP", null);
		Map<String, String> errorRespErroCodeMap = MapUtils.convertIntoMap(errorRespErroCode == null? errorRespErroCode : errorRespErroCode.toUpperCase(), ";",":",null);
		if(response.equals(Resp_InvalidPrefix) || response.equals(Resp_BlackListedNo))
			return EC_0_SUBSCRIBER_INVALID;
		else if(response.equals(Resp_SuspendedNo))
			return EC_0_SUBSCRIBER_SUSPENDED;
		else if(response.equals(Resp_ClipNotAvailable) || response.equals(Resp_ClipExpired))
			return EC_0_VCODE_INVALID;
		else if(response.equals(Resp_ActPending))
			return EC_0_SUBSCRIBER_EXISTS_PENDING;

		if(flag == -1)
			return EC_0_ERROR;
		if((flag > 2 && flag < 7) || flag == 11|| flag == 12|| flag == 13|| flag == 14)
			flag = 2;
		switch(flag) {
			case 0:
				if(response.equals(Resp_AlreadyActive))
					return EC_0_SUBSCRIBER_EXISTS;
				else if(response.equals(Resp_Success))
					return EC_0_SUCCESS;
				else if(response.equals(Resp_Failure)) {
					if(task.containsKey(param_actFailed))
						return EC_0_SUB_FAILURE;
					else
						return EC_0_SEL_FAILURE;
				}
				else {
					return EC_0_SUBSCRIBER_SUSPENDED;
				}
			case 1:
				if(response.equals(Resp_Inactive))
					return EC_1_SUBSCRIBER_NOT_EXISTS;
				else if(response.equals(Resp_SelExists))
					return EC_1_TONE_ALREADY_EXISTS;
				else if(response.equals(Resp_Success))
					return EC_1_SUCCSS;
				else if(response.equals(FAILURE))
					return EC_1_SEL_FAILURE;
				else if(errorRespErroCodeMap.containsKey(response)){
					String retrunErroResp =  errorRespErroCodeMap.get(response);
					return retrunErroResp == null? EC_1_SEL_FAILURE : retrunErroResp;
				}
				break;
			case 2:
				if(response.equals(Resp_invalidParam)) {
					String invalidParam = task.getString(param_invalidParam);
					if(invalidParam.equals("tariffCode"))
						return EC_2_TARIFF_CODE_INVALID;
				}
				else if(response.equals(Resp_AlreadyActive))
					return EC_2_SUBSCRIBER_EXISTS;
				else if(response.equals(Resp_Success))
					return EC_2_SUCCESS;
				else if(response.equals(Resp_Failure)) {
					if(task.containsKey(param_actFailed))
						return EC_2_SUB_FAILURE;
					else
						return EC_2_SEL_FAILURE;
				}
				break;
			case 7:
				if (response.equals(Resp_invalidParam))
					return EC_7_CCODE_INVALID;
				else if(response.equals(Resp_AlreadyActive))
					return EC_7_SUBSCRIBER_EXISTS;
				else if(response.equals(Resp_Success))
					return EC_7_SUCCESS;
				else if(response.equals(Resp_Failure))
				{
					if(task.containsKey(param_actFailed))
						return EC_7_SUB_FAILURE;
					else
						return EC_7_SEL_FAILURE;
				}
				else if(response.equals(Resp_Err))
					return EC_7_ERROR;
				else if(response.equals(Resp_songSetSelectionAlreadyExists))
					return EC_7_TONE_ALREADY_EXISTS;
				else
					return EC_7_FAILURE;
			default:
				return EC_0_ERROR;	
		}
		logger.error("RBT::invalid response-" + response + ",flag-" + flag);
		return response;
	}
	
	private String getAutodialResponse(Task task) {
		String response = task.getString(param_response);
		if(response.equals(Resp_InvalidPrefix) || response.equals(Resp_BlackListedNo))
			return AUTO_SUBSCRIBER_INVALID;
		else if(response.equals(Resp_SuspendedNo))
			return AUTO_SUBSCRIBER_SUSPENDED;
		else if(response.equals(Resp_ActPending) || response.equals(Resp_DeactPending) || response.equals(Resp_giftPending))
			return AUTO_ACT_DEACT_PENDING;
		else if(response.equals(Resp_ClipNotAvailable))
			return AUTO_VCODE_INVALID;
		else if(response.equals(Resp_invalidParam)) {
			String invalidParam = task.getString(param_invalidParam);
			if(invalidParam.equals(param_auto_ucode))
				return AUTO_UCODE_INVALID;
			if(invalidParam.equals(param_auto_stype))
				return AUTO_STYPE_INVALID;
			else if(invalidParam.equals(param_auto_caller))
				return AUTO_CALLER_INVALID;
			else if(invalidParam.equals(param_auto_flag))
				return AUTO_FLAG_INVALID;
			else if(invalidParam.equals("albumCode"))
				return AUTO_ALBUM_CODE_INVALID;
		}
		else if(response.equals(Resp_AlreadyActive))
			return AUTO_SUBSCRIBER_EXISTS;
		else if(response.equals(Resp_maxCallerSel))
			return AUTO_CALLER_SETTING_FULL;
		else if(response.equals(Resp_songNotSet))
			return AUTO_SELECTION_FAILED;
		else if(response.equals(Resp_Failure)) {
			return AUTO_SELECTION_FAILED;
		}
		else if(response.equals(Resp_Inactive))
			return AUTO_SUBSCRIBER_NOT_EXISTS;
		else if(response.equals(Resp_Failure)) {
			if(task.containsKey(param_actFailed))
				return AUTO_SUBSCRIPTION_FAILED;
		}
		else if(response.equals(Resp_InvalidNumber))
			return AUTO_SUBSCRIPTION_FAILED;
		else if(response.equals(Resp_Success))
			return AUTO_SUCCESS;
		else if(response.equals(Resp_selectionLimit))
			return AUTO_ALBUM_FULL;
		else if(response.equals(Resp_liteUserPremiumBlocked))
			return AUTO_SELECTION_FAILED;
		else {
			logger.error("RBT::invalid response-" + response);
			return AUTO_ERROR;
		}
		return response;
	}
	
	private String getEnvioProfileResp(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		int subType = -1;
		String status = subscriber.getStatus();
		if(!status.equals(WebServiceConstants.NEW_USER) && !status.equals(WebServiceConstants.DEACTIVE))
			subType = getAirtelSubscriberType(subscriber.getSubscriptionClass());
		if(status.equals(WebServiceConstants.ACT_PENDING)) {
			task.setObject(param_respMessage, subType + ":Waiting");
			return ENVIO_SUB_WAITING_OR_HLR_REMOVE;
		}
		else if(status.equals(WebServiceConstants.DEACT_PENDING)) {
			task.setObject(param_respMessage, subType + ":HLR Remove");
			return ENVIO_SUB_WAITING_OR_HLR_REMOVE;
		}
		else if (status.equals(WebServiceConstants.ACTIVE)) {
			StringBuffer sb = new StringBuffer(subType + "|");
			Library library = (Library) task.getObject(param_library);
			Settings settings = null;
			if(library != null)
				settings = library.getSettings();
			Setting[] allSettings = null;
			if(settings != null)
				allSettings = settings.getSettings();
			String allVCode = "";
			StringBuffer callerIDSettings = new StringBuffer();
			for (int i = 0; allSettings != null && i < allSettings.length; i++) {
				String callerID = allSettings[i].getCallerID();
				if (callerID != null && !callerID.equalsIgnoreCase("all")) {
					callerIDSettings.append("|");
					callerIDSettings.append(allSettings[i].getCallerID() + ":");
					callerIDSettings.append(Utility.getVCodeFromWavFile(allSettings[i].getRbtFile()) + "|");
				}
				else
					allVCode = Utility.getVCodeFromWavFile(allSettings[i].getRbtFile());
			}
			sb.append(allVCode);
			sb.append(callerIDSettings.toString());
			task.setObject(param_respMessage, sb.toString());
			return ENVIO_SUBSCRIBER_EXISTS;
		}
		return ENVIO_SUBSCRIBER_NOT_EXISTS; 
	}
	
	private String getEnvioResponse(Task task) {
		String flagStr = task.getString(param_envio_Flag);
		if(flagStr != null && flagStr.equals("2"))
			return getEnvioProfileResp(task);
		String response = task.getString(param_response);
		if(response.equals(Resp_InvalidPrefix) || response.equals(Resp_BlackListedNo) || response.equals(Resp_SuspendedNo))
			return ENVIO_SUBSCRIBER_INVALID;
		int flag = Utility.getIntFromStr(flagStr);
		if(response.equals(Resp_invalidParam)) {
			String invalidParam = task.getString(param_invalidParam);
			if(invalidParam.equals(param_envio_indx))
				return ENVIO_INDEX_INVALID;
			else if(invalidParam.equals(param_envio_cbsmsisdn) || invalidParam.equals(param_SPECIAL_CALLER_ID))
				return ENVIO_CALLER_INVALID;
			else if(invalidParam.equals(param_envio_dstmsisdn) || invalidParam.equals(param_CALLER_ID))
				return ENVIO_DST_CALLER_INVALID;
			else if(invalidParam.equals(param_envio_Flag))
				return ENVIO_FLAG_INVALID;
		}
		if(response.equals(Resp_ClipExpired))
			return ENVIO_VCODE_EXPIRED;
		if(response.equals(Resp_ClipNotAvailable))
			return ENVIO_VCODE_INVALID;
		if (response.equals(Resp_copyShuffleSelection) || response.equals(Resp_CopyPending)
				|| response.equals(Resp_copyInvalidClip))
			response = Resp_Failure;
		if(response.equals(Resp_copyInvalidClip) || response.equals(Resp_copyCantCopy))
			return ENVIO_VCODE_INVALID_FOR_COPY;
		if (response.equals(Resp_giftActPending) || response.equals(Resp_giftGifteeActPending)
				|| response.equals(Resp_giftGifteeDeactPending)
				|| response.equals(Resp_giftAlreadyPreset)
				|| response.equals(Resp_giftGifterNotActive) || response.equals(Resp_giftPending)
				|| response.equals(Resp_giftInUse))
			response = Resp_Failure;
		if (response.equals(Resp_maxCallerSel)) {
			StringBuffer sb = new StringBuffer();
			Library library = (Library) task.getObject(param_library);
			Settings settings = library.getSettings();
			Setting[] allSettings = settings.getSettings();
			for (int i = 0; allSettings != null && i < allSettings.length; i++) {
				String callerID = allSettings[i].getCallerID();
				if (callerID != null && !callerID.equalsIgnoreCase("all")) {
					sb.append(allSettings[i].getCallerID() + ":");
					sb.append(Utility.getVCodeFromWavFile(allSettings[i].getRbtFile()) + "|");
				}
			}
			task.setObject(param_respMessage, sb.toString().substring(0, sb.length()));
			return ENVIO_CALLER_SETTING_FULL;
		}
		if (response.equals(Resp_songNotSet) || response.equals(Resp_Failure)
				|| response.equals(Resp_Err) || response.equals(Resp_SelNotExists)) {
			switch (flag) {
				case 0:
					boolean isCaller = task.getString(param_envio_cbsmsisdn) != null;
					Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
					boolean isNewUser = isNewUser(subscriber.getStatus());
					if(isNewUser) {
						if(!isCaller)
							return ENVIO_COPY_ALL_FAILURE_NEW_SUB;
						else
							return ENVIO_COPY_CALLER_FAILURE_NEW_SUB;
					}
					else {
						if(!isCaller)
							return ENVIO_COPY_ALL_FAILURE;
						else
							return ENVIO_COPY_CALLER_FAILURE;
					}
				case 1:
					if (task.containsKey(param_actFailed))
						return ENVIO_SUB_SEL_FAILURE;
					else
						return ENVIO_SEL_FAILURE;
				case 3:
					if(response.equals(Resp_Err))
						return ENVIO_SEL_DELETION_FAILED;
					return ENVIO_SEL_DELETION_INVALID;
				case 4:
					return ENVIO_GIFT_FAILED;
				case 5:
					return ENVIO_ALBUM_SEL_FAILED;
				case 6:
					if(task.containsKey(param_actFailed))
						return ENVIO_SUB10_FAILURE;
					else
						return ENVIO_SEL_FAILURE;
				case 7:
					if(task.containsKey(param_actFailed))
						return ENVIO_SUB20_FAILURE;
					else
						return ENVIO_SEL_FAILURE;
			}
		}
		if(response.equals(Resp_Success)) {
			switch(flag) {
				case 0:
					boolean isCaller = task.getString(param_envio_cbsmsisdn) != null;
					Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
					boolean isNewUser = isNewUser(subscriber.getStatus());
					if(isNewUser) {
						if(!isCaller)
							return ENVIO_COPY_ALL_SUCCESS_NEW_SUB;
						else
							return ENVIO_COPY_CALLER_SUCCESS_NEW_SUB;
					}
					else {
						if(!isCaller)
							return ENVIO_COPY_ALL_SUCCESS;
						else
							return ENVIO_COPY_CALLER_SUCCESS;
					}
				case 1:
					if(task.containsKey(param_actRequested))
						return ENVIO_SUB_SEL_SUCCESS;
					else
						return ENVIO_SEL_SUCCESS;
				case 3:
					return ENVIO_SEL_DELETION_SUCCESSFUL;
				case 4:
					return ENVIO_GIFT_SUCCESSFUL;
				case 5:
					return ENVIO_ALBUM_SEL_SUCCESS;
				case 6:
					return ENVIO_SUB10_SUCCESS;
				case 7:
					return ENVIO_SUB20_SUCCESS;
			}
		}
		if(response.equals(Resp_Inactive))
			return ENVIO_SUBSCRIBER_NOT_EXISTS;
		if(response.equals(Resp_AlreadyActive)) {
			String splResp = task.getString(param_specialResp);
			if(splResp != null && splResp.equals("ENVIO_10"))
				return ENVIO_SUB10_FAILURE;
			if(splResp != null && splResp.equals("ENVIO_20"))
				return ENVIO_SUB20_FAILURE;
			return ENVIO_SUBSCRIBER_EXISTS;
		}
		if(response.equals(Resp_copyInvalidNumber))
			return ENVIO_DST_CALLER_NOT_EXITS;
		if(response.equals(Resp_selectionLimit))
			return ENVIO_ALBUM_FULL;
		if(response.equals(Resp_liteUserPremiumBlocked))
			return ENVIO_SEL_FAILURE;    // Check & change response
		logger.info("RBT::invalid response-" + response);
		return ENVIO_ERROR;
	}
	
	/*updated by Laxman*/
	private String getModResponse(Task task) {
		String response = task.getString(param_response);
		if(response.equals(Resp_BlackListedNo) || response.equals(Resp_InvalidNumber) || response.equals(Resp_InvalidPrefix))
			return MOD_SUBSCRIBER_INVALID;
		if(response.equals(Resp_SuspendedNo))
			return MOD_SUSPENDED_USER;
		if(response.equals(Resp_ActPending) || response.equals(Resp_DeactPending))
			return MOD_WAITING_USER;
		if(response.equals(Resp_ClipNotAvailable) || response.equals(Resp_ClipExpired))
			return MOD_VCODE_INVALID;
		if (response.equals(Resp_Failure) || response.equals(Resp_songNotSet)
				|| response.equals(Resp_maxCallerSel)
				|| response.equals(Resp_liteUserPremiumBlocked)) {
			if("true".equalsIgnoreCase(task.getString(param_actFailed))) {
				return MOD_SUBSCRIPTION_FAILED;
			}
			if (response.equals(Resp_songNotSet)) {
				return MOD_SONG_NOT_SET;
			}
//			if (response.equals(Resp_maxCallerSel)) {
//				return MOD_MAX_CALLER_SELECTIONS_REACHED;
//			}
			if (response.equals(Resp_liteUserPremiumBlocked)) {
				return MOD_LITE_USER_PREMIUM_BLOCKED;
			}
			return MOD_SELECTION_FAILED;
		}
		if(response.equals(Resp_selectionLimit))
			return MOD_ALBUM_FULL;
		if(response.equals(Resp_Success)) {
			if("true".equalsIgnoreCase(task.getString(param_actRequested)))
				return MOD_SUB_SEL_SUCCESS;
			return MOD_SEL_SUCCESS;
		}
		// Added by Laxman
//		if(response.equals(Resp_CopyPending)) {
//			return MOD_COPY_PENDING;
//		}
		if(response.equals(Resp_Err)) {
			return MOD_TECHNICAL_DIFFICULTIES;
		}
//		if(response.equals(Resp_giftActPending)) {
//			return MOD_GIFT_ACTIVATION_PENDING;
//		}
		if(response.equals(Resp_RenewalPending)) {
			return MOD_RENEWAL_PENDING;
		}
//		if(response.equals(Resp_songSetDownloadAlreadyExists)) {
//			return MOD_SONG_SET_DOWNLOAD_ALREADY_EXISTS;
//		}
		if(response.equals(Resp_songSetSelectionAlreadyExists)) {
			return MOD_SONG_SET_SELECTION_ALREADY_EXISTS;
		}
		// till here
		logger.error("RBT::unknown response-" + response);
		return MOD_ERROR;
	}
	
	private int getAirtelSubscriberType(String subClass) {
		if (isAdvanceRentalSubClass(subClass))
			return RBT_USER_TYPE_RENTAL;
		if (isLightPackSubClass(subClass))
			return RBT_USER_TYPE_LIGHT;
		if (isSamplingSubClass(subClass))
			return RBT_USER_TYPE_SAMPLING;
		if (isLifeTimeSubClass(subClass))
			return RBT_USER_TYPE_LIFE_TIME;
		if (isLowRentalSubClass(subClass))
			return RBT_USER_TYPE_LOW_RENTAL;
		if (isAlbumRentalSubClass(subClass))
			return RBT_USER_TYPE_ALBUM;
		return RBT_USER_TYPE_NORMAL;
	}
	
	private boolean isAdvanceRentalSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"ADVANCE_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isLightPackSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"LIGHT_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isSamplingSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"SAMPLING_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isLifeTimeSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"LIFE_TIME_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isLowRentalSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"LOW_RENTAL_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private boolean isAlbumRentalSubClass(String subClass) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"ALBUM_RENTAL_PACKS");
		return (param != null && param.getValue().indexOf(subClass) != -1);
	}
	
	private String getEnvioResponseString(String code) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR","ENVIO_RESPONSE_" + code);
		if(param != null)
			return param.getValue();
		return null;
	}
	
	private boolean isNewUser(String status) {
		if(status == null)
			return false;
		return status.equals(WebServiceConstants.NEW_USER) || status.equals(WebServiceConstants.DEACTIVE);
	}
}