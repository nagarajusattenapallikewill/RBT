package com.onmobile.apps.ringbacktones.webservice.features.RN;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.client.HttpConnector;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RNPropertyUtils {
	private static Logger logger = Logger.getLogger(RNPropertyUtils.class);

	private static ResourceBundle resourceBundle;
	static {
		try {
			resourceBundle = ResourceBundle.getBundle("RNUrl");
		} catch (MissingResourceException t) {
			logger.info("RNUrl not configured.");
		} catch (Throwable t) {
			logger.info("Throwable caught", t);
		}
	}

	public static String getProperty(String key) {
		String value = null;
		try {
			if (resourceBundle != null) {
				value = resourceBundle.getString(key);
			}
		} catch (MissingResourceException e) {

		}
		return value;
	}

	public static String getUrl(String circleId, String api, Map<String, String> requestParams) {
		String action = requestParams.get(WebServiceConstants.param_action);
		String info = requestParams.get(WebServiceConstants.param_info);
		String url = getUrl(circleId, api, action, info);
		if (url != null) {
			String subscriberID = HttpConnector.getSubscriberId(requestParams, api, action);
			if (subscriberID != null) {
				url = url.replaceAll("%MSISDN%", RBTDBManager.getInstance().subID(subscriberID));
			}
			String clipId = requestParams.get(WebServiceConstants.param_clipID);
			if (clipId != null) {
				url = url.replaceAll("%CLIP_ID%", clipId);
			}
			String mode = requestParams.get(WebServiceConstants.param_mode);
			if (mode != null) {
				url = url.replaceAll("%MODE%", mode);
			}
			String callerId = "";
			if(requestParams.get(WebServiceConstants.param_callerID) != null){
				callerId = requestParams.get(WebServiceConstants.param_callerID);
			}
			url = url.replaceAll("%CALLER_ID%", callerId);
		}
		return url;
	}

	public static String getUrl(String circleId, String api, String action, String info) {
		String value = null;
		String propertyName = null; 
		if (resourceBundle != null) {
			if (info != null) {
				propertyName = getPropertyName(circleId, api, info);
				value = getProperty(propertyName);
			}
			if (value == null) {
				propertyName = getPropertyName(circleId, api, action);
				value = getProperty(propertyName);	
				if (value == null) {
					propertyName = getPropertyName(circleId, api);
					value = getProperty(propertyName);
				}
			}
		}
		return value;
	}
	

	public static String getPropertyName(String circleId, String api) {
		String propertyName = "rn_" + circleId + "_" + api;
		return propertyName.toLowerCase();


	}

	public static String getPropertyName(String circleId, String api,
			String action) {
		String propertyName = "rn_" + circleId + "_" + api + "_" + action; 
		return propertyName.toLowerCase();
	}

	public static String getErrorResponse(String errorMsg) {
		if (errorMsg == null) {
			return null;
		}
		errorMsg = errorMsg.replaceAll(" ", "_").toLowerCase();
		String key = "error." + errorMsg;
		String value = getProperty(key);
		return value;
	}

	public static void main(String []args) {
		System.out.println(getErrorResponse("dshsd ojf dsljf sdfjdsf ."));
	}
}
