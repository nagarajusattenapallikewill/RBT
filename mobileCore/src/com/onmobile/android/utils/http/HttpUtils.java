package com.onmobile.android.utils.http;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class HttpUtils {
	
	private static Logger logger = Logger.getLogger(HttpUtils.class);
	/**
	 * Method to do URL hit
	 * @param request
	 * @param response
	 * @param requestParams
	 * @param httpParameters
	 * @return responseText
	 */
	public static String makeRequest(HashMap<String, String> requestParams,
			HttpParameters httpParameters, HashMap<String, File> fileParamMap,Boolean isResponseCodeRequired) {
		String responseText = "FAILURE"  ;
		logger.info("Making http request. requestParams: " + requestParams
				+ ", httpParameters: " + httpParameters + ", fileParamMap: "
				+ fileParamMap);
		if (null != fileParamMap) {
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(
						httpParameters, requestParams, fileParamMap);
				logger.info("Multi-part request, using post"
						+ " method. " + "httpResponse: " + httpResponse
						+ ", httpParameters: " + httpParameters
						+ ", fileParamMap: " + fileParamMap);
				if(isResponseCodeRequired){
					if(httpResponse.getResponseCode()== 200){
						responseText = "SUCCESS";
					}
				}else{
				responseText = httpResponse.getResponse();
				}
				} catch (Exception e) {
				logger.error("Unable to redirect using post. Exception: " + e.getMessage(), e);
				responseText = null;
			}

		} else {
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("Redirected using get method. httpResponse: "
						+ httpResponse + ", httpParameters: " + httpParameters);
				if(isResponseCodeRequired){
					if(httpResponse.getResponseCode()== 200){
						responseText = "SUCCESS";
					}
				}else{
				responseText = httpResponse.getResponse();
				}
			} catch (Exception e) {
				logger.error("Unable to redirect using get. Exception: " + e.getMessage(), e);
				responseText = null;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("responseText: " + responseText);
		}
		return responseText;
	}
}
