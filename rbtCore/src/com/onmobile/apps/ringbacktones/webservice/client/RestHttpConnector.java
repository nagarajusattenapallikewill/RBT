/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author vinayasimha.patil
 * @author abhinav.anand
 */
public class RestHttpConnector implements Connector, Constants
{	
	private HttpConnector httpConnector = null;
	private Configurations configurations= null;
	private static ResourceBundle resourceBundle = null;
	private RBTHttpClient rbtHttpClient = null;
	
	static {
		resourceBundle = ResourceBundle.getBundle("restUrl");
	}

	public static String getValue(String key, String defaultValue) {
		String value = resourceBundle.getString(key);
		if(value == null || value.trim().isEmpty() || value.equalsIgnoreCase(key))
			value = defaultValue;		
		return value;
	}
	
	public RestHttpConnector() throws Exception {
		configurations = new Configurations();
		httpConnector = new HttpConnector(configurations);
		
		HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
				configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
				configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
		configurations.getLogger().info("RBT:: httpParameters: " + httpParameters);

		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	@Override
	public Parser makeWebServiceRequest(Request request, String api,
			String action) {
		return httpConnector.makeWebServiceRequest(request, api, action);
	}

	@Override
	public File makeWebServiceBulkRequest(Request request, String api,
			String action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String redirectWebServiceRequest(
			HashMap<String, String> requestParams, String api) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String makeRestRequest(Request restrequest, String api, String action)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public <T> T makeWebServiceRequest(ConnectorHandler connectorHandler, Request request, String api, String action,Class<T> classObj){
		Map<String, String> requestParams = request.getRequestParamsMap();
		String url = getUrl(requestParams, api);
		HttpResponse httpResponse = null;
		if (api.equalsIgnoreCase("create")) {
			//httpResponse = rbtHttpClient.makeRequestByPost(url, requestParams, null);
		}
		return null;
	}
	
	
	private String getUrl(Map<String, String> requestParams, String api) {
		String url = null;
		String operatorID = requestParams.get(param_operatorID);
		String circleID = requestParams.get(param_circleID);
		
		String ipAddressNPort = null;
		if (operatorID != null)
			ipAddressNPort = configurations.getIPAddressNPort(operatorID);
		else
			ipAddressNPort = configurations.getIPAddressNPort(circleID);
		
		if(requestParams.containsKey(param_restRequest) && Boolean.parseBoolean(requestParams.get(param_restRequest))){
			url = configurations.getProtocol() + "://" + ipAddressNPort + "/rbt/v2/";
			url = url + constructUDPUrl(requestParams, api);			
			requestParams.remove(param_restRequest);
		}
		
		return url;
	}
	
	private String constructUDPUrl(Map<String, String> requestParams, String api) {
		String url = getValue(UDP_URL+"_"+api.toUpperCase(), null);
		
		if (url != null) {
			if (requestParams.get(param_udpId) != null) 
				url = replace("%UDPID%", requestParams.get(param_udpId), url);
			
			if (requestParams.get(param_subscriberID) != null)
				url = replace("%SUBSCRIBERID%", requestParams.get(param_subscriberID), url);
			
			if (requestParams.get(param_mode) != null)
				url = replace("%MODE%", requestParams.get(param_mode), url);
			else {
				if (url.contains("&mode=%MODE%"))
					url = replace("&mode=%MODE%", "", url);
				else if (url.contains("mode=%MODE%&"))
					url = replace("mode=%MODE%&","",url);
			}
			
			if (requestParams.get(param_name) != null)
				url = replace("%UDPNAME%", requestParams.get(param_name), url);
			else {
				if (url.contains("&name=%UDPNAME%"))
					url = replace("&name=%UDPNAME%", "", url);
				else if (url.contains("name=%UDPNAME%&"))
					url = replace("name=%UDPNAME%&", "", url);
			}
			
			if (requestParams.get(param_extraInfo) != null)
				url = replace("%EXTRAINFO%", requestParams.get(param_extraInfo), url);
			else {
				if (url.contains("&extrainfo=%EXTRAINFO%"))
					url = replace("&extrainfo=%EXTRAINFO%","",url);
				else if (url.contains("extrainfo=%EXTRAINFO%&"))
					url = replace("extrainfo=%EXTRAINFO%&", "", url);
			}
			
			if (requestParams.get(param_offSet) != null)
				url = replace("%OFFSET%", requestParams.get(param_offSet), url);
			if (requestParams.get(param_pageSize) != null)
				url = replace("%PAGAESIZE%", requestParams.get(param_pageSize), url);
		}
		
		return url;
	}
	
	private String replace(String replaceable, String replacedWith, String url) {
		url = url.replace(replaceable, replacedWith);
		return url;
	}

	@Override
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler,
			Request request, String api, String action) {
		ConnectorHandler connectionHanlder = null;
		return makeWebServiceRequest(connectionHanlder, request, api, action);
	}
}
