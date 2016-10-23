package com.onmobile.android.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.android.utils.memcache.MemcacheUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

public class ParamForTypeUtils {
	private static Logger logger = Logger.getLogger(ParamForTypeUtils.class);

	public static Map<String, String> getAllPropertiesForType(String type) {
		@SuppressWarnings("unchecked")
		Map<String, String> paramMap = (Map<String, String>) MemcacheUtils.getFromMemcache(type);
		if (paramMap == null) {
			paramMap = hitWebServiceForParams(type);
			logger.info("Webservice hit for params of type: " + type);
			if (paramMap != null) {
				addParamMapToMemcache(type, paramMap);
			}
		} else {
			logger.debug("Parameters obtained from memcache. Type: " + type);
		}
		return paramMap;
	}

	public static Map<String, String> getPropertyOfType(String type,
			String paramList) {
		Map<String, String> responseParamMap = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Map<String, String> paramMap = (Map<String, String>) MemcacheUtils.getFromMemcache(type);
		if (paramMap == null) {
			paramMap = hitWebServiceForParams(type);
			logger.info("Webservice hit for params of type: " + type);
			if (paramMap != null) {
				addParamMapToMemcache(type, paramMap);
			}
		} else {
			logger.debug("Parameters obtained from memcache. Type: " + type);
		}
		if (paramList != null) {
			String[] paramArray = paramList.split(",");
			for (String param : paramArray) {
				String value = paramMap.get(param);
				responseParamMap.put(param, value);
			}
		} else {
			logger.info("No properties present for this type: " + type);
		}
		return responseParamMap;
	}
	
	public static Map<String, String> hitWebServiceForParams(String type) {
		Map<String, String> paramMap = new HashMap<String, String>();
		RBTClient client = RBTClient.getInstance();
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType(type);
		Parameter[] parameters = client.getParameters(request);
		for(Parameter parameter: parameters){
			paramMap.put(parameter.getName(),parameter.getValue());
		}
		logger.info("Type: " + type + ", paramMap: "+ paramMap);
		return paramMap;
	}

	public static Boolean addParamMapToMemcache(String type,
			Map<String, String> paramMap) {
		boolean isUpdated = MemcacheUtils.addToMemcache(type, paramMap);
		logger.info("type: " + type + ", isMemcacheUpdated: " + isUpdated);
		return isUpdated;
	}
}
