/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jspsmart.upload.SRequest;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.RBTPrimitive;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 */
public class Utility implements Constants, iRBTConstant {
	private static Logger logger = Logger.getLogger(Utility.class);

	public static void sqlInjectionInRequestParam(
			HashMap<String, String> requestParams) {
		Set<Entry<String, String>> entrySet = requestParams.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String value = entry.getValue();

			if (value != null) {
				StringBuilder stringBuilder = new StringBuilder();
				int from = 0;
				int next;
				while ((next = value.indexOf('\'', from)) != -1) {
					stringBuilder.append(value.substring(from, next + 1));
					stringBuilder.append('\'');
					from = next + 1;
				}

				if (from < value.length()) {
					stringBuilder.append(value.substring(from));
				}
				entry.setValue(stringBuilder.toString());
			}
		}

	}

	public static String getConfiguredResponse(String response, String action) {
		Parameters parameters = CacheManagerUtil.getParametersCacheManager()
				.getParameter("PROMOTION", "ALTERED_RESPONSE_FOR_PROMOTION");
		String confResp = null;
		if (parameters != null) {
			confResp = parameters.getValue();

			if (confResp == null) {
				return response;
			}
			StringTokenizer st = new StringTokenizer(confResp, ";");
			while (st.hasMoreTokens()) {
				String str = st.nextToken();
				StringTokenizer strtk = new StringTokenizer(str, "=");
				String key = response;
				if (response.equalsIgnoreCase("SUCCESS")) {
					key = action + "_" + key;
				}
				if (strtk.hasMoreTokens()
						&& strtk.nextToken().equalsIgnoreCase(key)) {
					response = strtk.nextToken();
					break;
				}
			}
		}
		return response;
	}

	public static HashMap<String, String> getRequestParamsMap(
			ServletConfig servletConfig, HttpServletRequest request,
			HttpServletResponse response, String api) {
		return getRequestParamsMap(servletConfig, request, response, api, true);
	}

	public static HashMap<String, String> getRequestParamsMap(
			ServletConfig servletConfig, HttpServletRequest request,
			HttpServletResponse response, String api, boolean converToUpper) {
		HashMap<String, String> requestParams = new HashMap<String, String>();

		if (isMultipartContent(request)) {
			try {
				SmartUpload smartUpload = new SmartUpload();
				smartUpload.initialize(servletConfig, request, response);
				smartUpload.setTotalMaxFileSize(20000000);
				smartUpload.upload();

				for (int i = 0; i < smartUpload.getFiles().getCount(); i++) {
					com.jspsmart.upload.File file = smartUpload.getFiles()
							.getFile(i);
					if (file.getSize() > 0) {
						String fileName = file.getFileName();
						File savedFile = new File(fileName);
						file.saveAs(savedFile.getAbsolutePath());

						requestParams.put(ugc_param_WAVFILE,
								savedFile.getAbsolutePath());
					}
				}

				SRequest smartUploadRequest = smartUpload.getRequest();
				boolean isSAPDB = RBTPrimitive.getDBSelectionString().equals(
						RBTPrimitive.DB_SAPDB);
				@SuppressWarnings("unchecked")
				Enumeration<String> params = smartUploadRequest
						.getParameterNames();
				while (params.hasMoreElements()) {
					String key = params.nextElement();
					String value = smartUploadRequest.getParameter(key).trim();

					if (!value.equals("") && !value.equalsIgnoreCase("null")) {
						if (converToUpper)
							key = key.toUpperCase();

					}
					requestParams.put(key, value);
				}

			} catch (SmartUploadException e) {
				Logger.getLogger(Utility.class).error(
						"RBT:: " + e.getMessage(), e);
			} catch (IOException e) {
				Logger.getLogger(Utility.class).error(
						"RBT:: " + e.getMessage(), e);
			} catch (ServletException e) {
				Logger.getLogger(Utility.class).error(
						"RBT:: " + e.getMessage(), e);
			}
		} else {
			Enumeration<String> params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String key = params.nextElement();
				String value = request.getParameter(key).trim();

				if (!value.equals("") && !value.equalsIgnoreCase("null")) {
					if (converToUpper)
						key = key.toUpperCase();

				}

				requestParams.put(key, value);
			}
		}

		requestParams.put(param_api, api);

		if (api != null && !api.equals(api_rbtplayhelp)) {
			String ipAddress = request.getRemoteAddr();
			requestParams.put(param_ipAddress, ipAddress);
			requestParams.put(param_hostName, request.getRemoteHost());
		}

		requestParams.put(param_startTime,
				String.valueOf(System.currentTimeMillis()));
		requestParams.put(param_queryString, request.getQueryString());
		Logger.getLogger(Utility.class).info(
				"RBT:: requestParams: " + requestParams);
		return requestParams;
	}

	public static String findNReplaceAll(String input, String findWhatString,
			String replaceWithString) {
		Logger.getLogger(Utility.class).info("Entering findNReplaceAll");
		if (input == null || replaceWithString == null
				|| input.indexOf(findWhatString) == -1)
			return input;
		Logger.getLogger(Utility.class).info(
				"RBT:: findNReplaceAll input=" + input + ",findWhatString="
						+ findWhatString + ",replaceWithString="
						+ replaceWithString);
		StringBuffer ret = new StringBuffer();
		boolean keepGoing = true;
		while (keepGoing) {
			int index = input.indexOf(findWhatString);
			if (index == -1) {
				ret.append(input);
				keepGoing = false;
			} else {
				ret.append(input.substring(0, index));
				ret.append(replaceWithString);
				input = input.substring(index + findWhatString.length());
			}
		}
		Logger.getLogger(Utility.class).info(
				"RBT:: findNReplaceAll Exit with return ret.toString()="
						+ ret.toString());
		return ret.toString();
	}

	public static boolean isMultipartContent(HttpServletRequest request) {
		if (!request.getMethod().toLowerCase().equals("post"))
			return false;

		String contentType = request.getContentType();
		if (contentType == null)
			return false;

		return (contentType.toLowerCase().startsWith("multipart/"));
	}

	/**
	 * @author Sreekar 2009-06-23
	 * @param V
	 *            -CODE
	 * @return C-CDOE If VCode is of the format AABBCDDXXXXYYYY then CCode will
	 *         be DDYYYY
	 */
	/*
	 * public static String getCCodeFromVCode(String strVCode) { if (strVCode ==
	 * null) return null; try { Long.parseLong(strVCode); StringBuffer sb = new
	 * StringBuffer(); sb.append(strVCode.charAt(5));
	 * sb.append(strVCode.charAt(6)); sb.append(strVCode.charAt(11));
	 * sb.append(strVCode.charAt(12)); sb.append(strVCode.charAt(13));
	 * sb.append(strVCode.charAt(14)); return sb.toString(); } catch (Exception
	 * e) { Logger.getLogger(Utility.class).error("RBT::", e); return null; } }
	 */

	public static String getWavFileFromVCode(String strVCode) {
		if (strVCode != null)
			return "rbt_" + strVCode + "_rbt";
		return null;
	}

	public static String getVCodeFromWavFile(String wavFile) {
		if (wavFile != null) {
			wavFile = wavFile.replaceAll("rbt_", "");
			wavFile = wavFile.replaceAll("_rbt", "");
			wavFile = wavFile.replaceAll(".wav", "");
			return wavFile;
		}
		return null;
	}

	public static int getIntFromStr(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return -1;
		}
	}

	public static boolean isDeactiveSubscriber(String status) {
		if (status != null)
			return status.equals(WebServiceConstants.NEW_USER)
					|| status.equals(WebServiceConstants.DEACTIVE);
		return false;
	}

	public static boolean isSubActive(String subscriberID,
			Categories categories, String subscriberWavFile, int status) {
		boolean isSubActive = false;
		RBTDBManager m_rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = m_rbtDBManager.getSubscriber(subscriberID);
		String subYes = subscriber.subYes();
		if (subYes != null
				&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
						.equalsIgnoreCase(STATE_EVENT))) {
			if (!m_rbtDBManager.isPackActivationPendingForContent(subscriber,
					categories, subscriberWavFile, status, null))
				isSubActive = true;
		}

		if (subscriber.subYes().equalsIgnoreCase("Z")) {
			String subscriberExtraInfo = subscriber.extraInfo();
			HashMap<String, String> subExtraInfoMap = DBUtility
					.getAttributeMapFromXML(subscriberExtraInfo);
			if (subExtraInfoMap.get("VOLUNTARY") != null
					&& subExtraInfoMap.get("VOLUNTARY").equals("TRUE")) {
				isSubActive = false;
			}
		}

		return isSubActive;

	}

	public static ArrayList<String> tokenizeArrayList(String stringToTokenize,
			String delimiter) {
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());
		return result;
	}

	public static boolean sendSMS(String subID, String smsText) {
		// TODO
		// Use sender number from Response Encoder class.
		return false;
	}

	public static boolean isTrialWithActivations(String classType,
			String paramText) {
		if (classType == null || paramText == null)
			return false;
		String trialStr = paramText;
		StringTokenizer stkParent = new StringTokenizer(trialStr, ";");
		while (stkParent.hasMoreTokens()) {

			try {
				StringTokenizer stkChild = new StringTokenizer(stkParent
						.nextToken().trim(), ",");
				if (stkChild == null)
					continue;
				String trialClass = null;
				Integer trialInt = null;
				if (stkChild.hasMoreTokens())
					trialClass = stkChild.nextToken().trim().toUpperCase();
				if (trialClass.equalsIgnoreCase(classType))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static boolean isTrialBlackoutPeriod(Task task, Date startDate,
			Setting[] settings) {
		Clip clip = (Clip) task.getObject(CLIP_OBJ);
		String classType = clip.getClassType();
		int trialBlackoutDays = getTrialBlackOutDays(classType);
		Calendar trialStart = Calendar.getInstance();
		trialStart.setTime(startDate);
		trialStart.add(Calendar.DATE, trialBlackoutDays);
		if (Calendar.getInstance().getTime().after(trialStart.getTime()))
			return false;
		else {
			for (int i = 0; i < settings.length; i++) {
				if (settings[i].getChargeClass().equalsIgnoreCase(classType))
					return false;
			}
		}
		return true;
	}

	public static int getTrialBlackOutDays(String chargeClass) {
		int trialInt = 0;
		StringTokenizer stkParent = new StringTokenizer(CacheManagerUtil
				.getParametersCacheManager().getParameterValue(COMMON,
						TRIAL_WITH_ACT, ""), ";");
		while (stkParent.hasMoreTokens()) {
			try {
				StringTokenizer stkChild = new StringTokenizer(stkParent
						.nextToken().trim(), ",");
				if (stkChild == null)
					continue;
				String trialClass = null;
				if (stkChild.hasMoreTokens())
					trialClass = stkChild.nextToken().trim().toUpperCase();
				if (trialClass.equalsIgnoreCase(chargeClass)) {
					try {
						trialInt = Integer
								.parseInt(stkChild.nextToken().trim());
					} catch (Exception e) {
						return 0;
					}
				}
			} catch (Exception e) {
			}
		}
		return trialInt;
	}

	public static boolean isPromotionRequest(Task task) {
		if (task != null && task.containsKey(param_REQUEST))
			return true;
		return false;
	}

	public static void copyFile(File source, File destination)
			throws IOException {
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try {
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination))
					.getChannel();
			sourceFileChannel.transferTo(0, source.length(),
					destinationFileChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			} catch (IOException e) {
			}
		}
	}

	public static String getVodaCTSubClass(String mode, String srvClass,
			String circle) {
		String key = mode + "_" + srvClass + "_" + circle;
		logger.info("Getting Subscription Class for key: " + key);
		ParametersCacheManager cacheManager = CacheManagerUtil
				.getParametersCacheManager();
		String subClassMapStr = cacheManager.getParameterValue(
				"VODA_CT_SUBSCRIPTION_CLASS", key, null);
		if (null == subClassMapStr) {
			key = mode + "_" + srvClass;
			logger.info("Circle based SubscriptionClass is not found. Fetching SubscriptionClass for key: "
					+ key);
			subClassMapStr = cacheManager.getParameterValue(
					"VODA_CT_SUBSCRIPTION_CLASS", key, null);
		}
		return subClassMapStr;
	}

	public static String getVodaCTCosId(String mode, String srvClass,
			String circle) {
		String key = mode + "_" + srvClass + "_" + circle;
		logger.info("Fetching CosId for key: " + key);
		ParametersCacheManager cacheManager = CacheManagerUtil
				.getParametersCacheManager();
		String subClassMapStr = cacheManager.getParameterValue("VODA_CT_COS",
				key, null);
		if (null == subClassMapStr) {
			key = mode + "_" + srvClass;
			logger.info("Circle based CosId is not found. Fetching CosId for key: "
					+ key);
			subClassMapStr = cacheManager.getParameterValue("VODA_CT_COS", key,
					null);
		}
		return subClassMapStr;
	}

	public static boolean isEmpty(String str) {
		if (str == null || str.trim().length() == 0
				|| str.trim().equalsIgnoreCase("null"))
			return true;
		return false;
	}

	public static boolean isDeactive(String status) {
		if (!isEmpty(status)) {
			if (status.equals(WebServiceConstants.ACT_ERROR)
					|| status.equals(WebServiceConstants.DEACTIVE)
					|| status.equals(WebServiceConstants.DEACT_PENDING)
					|| status.equals(WebServiceConstants.DEACT_ERROR))
				return true;
		}
		return false;
	}

	/**
	 * The value 0 or 1 are the valid prompts.
	 * 
	 * @return returns false if the prompt is null and other than 0 or 1.
	 */
	public static boolean isValidPrompt(String prompt) {
		if ("0".equals(prompt) || "1".equals(prompt)) {
			return true;
		}
		return false;
	}

	public static boolean isComboModeCrmRequest(
			HashMap<String, String> requestParams) {
		if (requestParams == null || requestParams.size() == 0
				|| !requestParams.containsKey(param_crmUrlMode))
			return false;
		String allowedMode = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue("COMMON", dbparam_VODACRMURL_COMBO_MODE,
						null);
		if (allowedMode == null)
			return false;
		String mode = requestParams.get(param_crmUrlMode);
		if (allowedMode.trim().equalsIgnoreCase(mode))
			return true;
		return false;
	}

	public static String hitConsentRequestToSM(String subscriberID,
			String srvKey, String refID, boolean consent) {
		String response = ERROR;
		try {
			String url = RBTParametersUtils
					.getParamAsString(iRBTConstant.COMMON,
							"SUBMGR_URL_FOR_CONSENT_REQUEST", null).trim();
			url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
			url = url.replaceAll("%SRV_KEY%", srvKey);
			url = url.replaceAll("%REF_ID%", refID);
			url = url.replaceAll("%CONSENT%", (consent ? "yes" : "no"));

			HttpParameters httpParameters = new HttpParameters(url);
			com.onmobile.apps.ringbacktones.webservice.common.Utility
					.setSubMgrProxy(httpParameters);
			if (logger.isInfoEnabled())
				logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);

			if (logger.isInfoEnabled())
				logger.info("RBT:: httpResponse: " + httpResponse);

			if (httpResponse == null)
				response = ERROR;
			else if (httpResponse.getResponseCode() == 200)
				response = SUCCESS;
			else if (httpResponse.getResponseCode() >= 600
					&& httpResponse.getResponseCode() < 700)
				response = RETRIABLE_ERROR;
			else if (httpResponse.getResponseCode() == 705)
				response = ALREADY_ACCEPTED;
			else
				response = ERROR;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		return response;
	}

	/**
	 * Compares the list with set for the common values and returns true if it
	 * contains the same value. It verifies only for the first common element.
	 * 
	 * @param list
	 * @param set
	 * @return true/false
	 */
	public static boolean contains(List<String> list, HashSet<String> set) {
		if (logger.isDebugEnabled()) {
			logger.debug("Comparing list: " + list + ", set: " + set);
		}

		boolean isContains = false;
		if (null != set && null != list) {
			for (String key : list) {
				if (set.contains(key)) {
					isContains = true;
					break;
				}
			}
		} 
		logger.info("Compared list: " + list + " with set: " + set
				+ ", returns: " + isContains);
		return isContains;

	}
	
	public static Map<String,List<String>> getVrbtCatSubSongSrvMap() {
		Map<String,List<String>> vrbtCatIdSubSongSrvKeyMap = null;
		String vrbtCatIdSubSongSrvKeyConfig = CacheManagerUtil
				.getParametersCacheManager().getParameterValue("COMMON", "VRBT_CAT_ID_SUB_SONG_SRV_KEY_MAPPING", null);
		if(vrbtCatIdSubSongSrvKeyConfig != null) {
			vrbtCatIdSubSongSrvKeyMap = new HashMap<String, List<String>>();
			String[] vrbtCatIdSubSongSrvKeyArr = vrbtCatIdSubSongSrvKeyConfig.split("\\|");
			if(vrbtCatIdSubSongSrvKeyArr != null && vrbtCatIdSubSongSrvKeyArr.length > 0) {
				for(String vrbtCatIdSubSongSrvKey : vrbtCatIdSubSongSrvKeyArr) {
					String[] strVrbtCatIdSubSongSrvMap = vrbtCatIdSubSongSrvKey.split("\\:");
					if(strVrbtCatIdSubSongSrvMap == null || strVrbtCatIdSubSongSrvMap.length != 2) {
						continue;
					}
					String catId = strVrbtCatIdSubSongSrvMap[0];
					String[] subSongSrvKey = strVrbtCatIdSubSongSrvMap[1].split(",");
					String subClass = null;
					String chargeClass = null;
					if(subSongSrvKey != null && subSongSrvKey.length >= 1) {
						subClass = subSongSrvKey[0];
					}
					if(subSongSrvKey != null && subSongSrvKey.length >= 2) {
						chargeClass = subSongSrvKey[1];
					}
					List<String> list = new ArrayList<String>();
					list.add(subClass);
					list.add(chargeClass);
					vrbtCatIdSubSongSrvKeyMap.put(catId, list);
				}
			}
			
		}
		return vrbtCatIdSubSongSrvKeyMap;
	}
	
	
	public static String getBrandName(String circleID) {
		String brandName = getParamAsString("COMMON", "BRAND_NAME", null);
		String subClassOpNameMap = getParamAsString(COMMON, 
				"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
		if(subClassOpNameMap != null && circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			String circleWiseBrandName = getParamAsString("COMMON", circleID +"_BRAND_NAME", null);
			if(circleWiseBrandName != null){
				brandName = circleWiseBrandName;
			} else if(operatorName != null) {
				brandName = getParamAsString("COMMON", operatorName.toUpperCase() +"_BRAND_NAME", brandName);
			}
		}
		logger.info("brandName :" + brandName);
		return brandName;
	}
	
	public static String getParamAsString(String type, String param, String defaultVal)
    {
    	try{
    		return CacheManagerUtil.getParametersCacheManager().getParameter(type, param, defaultVal).getValue();
    	}catch(Exception e){
    		logger.warn("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal;
    	}
    }
	
	public static String getSenderNumberbyType(String type, String circleID, String senderNumberKeyword) {
		String senderNumber = getParamAsString(type, senderNumberKeyword, null);
		String subClassOpNameMap = getParamAsString(COMMON, 
				"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
		if(subClassOpNameMap != null && circleID != null && circleID.length() > 0) {
			String operatorName = circleID.indexOf("_") != -1 ? circleID.substring(0, circleID.indexOf("_")) : null;
			if(operatorName != null && operatorName.trim().length() > 0) {
				senderNumber = getParamAsString(type, operatorName.toUpperCase() +"_"+senderNumberKeyword, senderNumber);
			}
		}
		logger.info("senderNumber :" + senderNumber);
		return senderNumber;
	}
	
	public static String getSenderNumber(String type, String subscriberID, String senderNumberKeyword) {
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);
		String senderNumber = getParamAsString(type, senderNumberKeyword, null);
		if(subscriber == null) return senderNumber;
		String circleID = subscriber.circleID();
		return getSenderNumberbyType(type, circleID, senderNumberKeyword);
	}

}