package com.onmobile.apps.ringbacktones.rbt2.http;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class HttpHitUtil {

	private static Logger logger = Logger.getLogger(HttpHitUtil.class);
	public static <T>T makePostHit(String URL, Class<T> T) {
		RestTemplate r = (RestTemplate) ConfigUtil.getBean("restTemplate");
		
		String preUrl = getPreUrl(URL);
		String queryString = getQueryString(URL);
		
		MultiValueMap<String, Object> form = getMultiValueMap(queryString, null);
		
		return r.postForObject(preUrl, form, T);
	}
	
	private static String getPreUrl(String URL) {
		return URL.substring(0, URL.indexOf('?'));
	}
	
	private static String getQueryString(String URL) {
		return URL.substring(URL.indexOf('?') + 1);
	}
	
	private static MultiValueMap<String, Object> getMultiValueMap(String queryString, File object) {
		MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
		
		String[] params = queryString.split("&");
		
		for(String param : params) {
			String[] keyValue = param.split("=");
			if(keyValue[1].equals("fileObject") && object != null) {
				form.add(keyValue[0], new FileSystemResource(object));
			}
			else {
				form.add(keyValue[0], keyValue[1]);
			}
		}
		
		return form;
	}
	
	public static <T>T makePostMultiPartHit(String URL, Class<T> T, File file) {
		RestTemplate r = (RestTemplate) ConfigUtil.getBean("restTemplate");
		
		String preUrl = getPreUrl(URL);
		String queryString = getQueryString(URL);
		
		MultiValueMap<String, Object> form = getMultiValueMap(queryString, file);
	
		return r.postForObject(preUrl, form, T);
	}
	
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
