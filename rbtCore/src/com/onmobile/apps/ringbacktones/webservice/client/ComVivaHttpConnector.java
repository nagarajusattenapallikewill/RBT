package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.ComVivaConfigurations;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.URLBuilder;

public class ComVivaHttpConnector implements Connector {
	private RBTHttpClient rbtHttpClient = null;
	private RBTHttpClient bulkRBTHttpClient = null;
	private Configurations configurations = null;

	private DocumentBuilder documentBuilder = null;



	public ComVivaHttpConnector(Configurations configurations) throws Exception
	{
		this.configurations = configurations;

		HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
				configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
				configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
		configurations.getLogger().info("RBT:: httpParameters: " + httpParameters);

		rbtHttpClient = new RBTHttpClient(httpParameters);

		HttpParameters bulkHTTPParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
				configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeoutForBulk(),
				configurations.getMaxTotalHttpConnectionsForBulk(), configurations.getMaxHostHttpConnectionsForBulk());
		configurations.getLogger().info("RBT:: httpBulkParameters: " + httpParameters);

		bulkRBTHttpClient = new RBTHttpClient(bulkHTTPParameters);

		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	@Override
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler, Request request, String api,
			String action) {
		
		configurations.getLogger().info("makeWebServiceRequest method invoked");
		
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		Parser parser = null;
		try {
			Map<String, String> requestParams = request
					.getRequestParamsMap();
			String circleId = request.getCircleID();
			if (action != null)
				requestParams.put(param_action, action);

			configurations.getLogger().info(
					"RBT:: requestParams: " + requestParams);

			if (api.equalsIgnoreCase(api_SelectionPreConsentInt)
					&& action.equalsIgnoreCase(action_set)) {

				parser = makeWSPreConsent(request, requestParams, circleId);

			} else if(api.equalsIgnoreCase(api_Selection)
					&& requestParams.containsKey(param_action)
					&& action.equalsIgnoreCase(action_deleteSetting)) {
				if(requestParams.containsKey(param_callerID) && !requestParams.get(param_callerID).equalsIgnoreCase("ALL"))
					parser = makeWSRequestSpecialToneDeletion(requestParams, param_comviva_special_delete_setting_url, circleId);
				else
					parser = makeWSRequestNormalToneDeletion(requestParams, param_comviva_delete_setting_url, circleId);
			} else if(api.equalsIgnoreCase(api_Rbt)) {
				parser = makeWSGetSelections(requestParams, param_comviva_get_selections_url, circleId);				
			}

		} catch (Exception e) {
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}
		
		configurations.getLogger().info("Returning Parser");
		return parser;
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

	private String getUCode(Map<String, String> requestParams) {
		if(requestParams != null && requestParams.get(param_callerID) != null 
				&& !requestParams.get(param_callerID).isEmpty()
				&& !requestParams.get(param_callerID).equalsIgnoreCase("ALL"))
			return "1";
		else
			return "0";
	}

	

	private String getVcode(Map<String, String> requestParams) {
		Clip clip = null;
		if (requestParams.get(param_clipID) != null) {
			clip = RBTCacheManager.getInstance().getClip(
					requestParams.get(param_clipID));
		}
		if (clip == null)
			return null;
		String vcode = null;
		String wavFile = null;
		if (clip.getClipId() != -1) {
			//if (clip != null) {
			wavFile = clip.getClipRbtWavFile();
			//}
		}

		if (wavFile != null) {
			vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
		}
		configurations.getLogger().info("Returning VCODE: "+vcode);
		return vcode;
	}

	private void validateSubId(Map<String,String> requestParams) {
		configurations.getLogger().info("Validating SubscriberId: "+requestParams.get(param_subscriberID));
		if (requestParams != null
				&& requestParams.containsKey(param_subscriberID)) {
			requestParams.put(param_subscriberID, RBTDBManager.getInstance().subID(requestParams.get(param_subscriberID)));
		}
	}

	private void validateCallerId(Map<String,String> requestParams) {
		configurations.getLogger().info("Validating CallerId: "+requestParams.get(param_callerID));
		if (requestParams != null
				&& requestParams.containsKey(param_callerID)) {
			requestParams.put(param_callerID, RBTDBManager.getInstance().subID(requestParams.get(param_callerID)));
		}
	}


	private Parser makeWSRequestNormalToneDeletion(Map<String, String> requestParams, String urlName,
			String circleId) throws HttpException, IOException {
		
		configurations.getLogger().info("makeWSRequestNormalToneDeletion method invoked");
		
		String url = ComVivaConfigurations.getInstance().getUrl(urlName,
				circleId);
		configurations.getLogger().info("Url received: "+url);
		validateSubId(requestParams);
		String uCode = getUCode(requestParams);

		String vCode = getVcode(requestParams);

		URLBuilder urlBuilder = new URLBuilder(url);
		urlBuilder = urlBuilder.replaceMsisdn(requestParams.get(param_subscriberID)).replaceVCode(
				vCode).replaceUCode(uCode);

		HttpResponse httpResponse =  makeGetRequest(urlBuilder.buildUrl(), requestParams);
		Document document = createXML(httpResponse);
		Parser parser = new Parser();
		parser.setDocument(document);
		parser.setParser(new RBTParser());
		return parser;
	}

	private Parser makeWSRequestSpecialToneDeletion(Map<String, String> requestParams, String urlName,
			String circleId) throws HttpException, IOException {

		configurations.getLogger().info("makeWSRequestSpecialToneDeletion method invoked");
		String url = ComVivaConfigurations.getInstance().getUrl(urlName,
				circleId);

		validateSubId(requestParams);
		validateCallerId(requestParams);

		String uCode = getUCode(requestParams);

		String vCode = getVcode(requestParams);

		URLBuilder urlBuilder = new URLBuilder(url);

		urlBuilder = urlBuilder.replaceMsisdn(requestParams.get(param_subscriberID))
				.replaceVCode(vCode).replaceUCode(uCode)
				.replaceCallerId(requestParams.get(param_callerID));

		HttpResponse httpResponse =  makeGetRequest(urlBuilder.buildUrl(), requestParams);
		Document document = createXML(httpResponse);
		Parser parser = new Parser();
		parser.setDocument(document);
		parser.setParser(new RBTParser());
		return parser;
	}

	private Parser makeWSGetSelections(Map<String, String> requestParams, String urlName,
			String circleId) throws HttpException, IOException {

		configurations.getLogger().info("makeWSGetSelections method invoked");
		String url = ComVivaConfigurations.getInstance().getUrl(urlName,
				circleId);

		validateSubId(requestParams);

		URLBuilder urlBuilder = new URLBuilder(url);

		urlBuilder = urlBuilder.replaceMsisdn(requestParams.get(param_subscriberID));

		HttpResponse httpResponse =  makeGetRequest(urlBuilder.buildUrl(), requestParams);
		Parser parser = new Parser();
		parser.setResponse(httpResponse.getResponse());
		parser.setParser(new ComVivaSAXParser());
		parser.setSubscriberId(requestParams.get(param_subscriberID));
		return parser;
	}

	private Parser makeWSPreConsent(Request request, Map<String,String> requestParams, String circleId) throws HttpException, IOException {

		configurations.getLogger().info("makeWSPreConsent method invoked");
		HttpResponse httpResponse = makeWSProfileCheck(requestParams, param_comviva_profile_check_url, circleId);

		boolean isActiveUser = false;

		if (httpResponse != null && httpResponse.getResponse() != null && !httpResponse.getResponse().isEmpty()
				&& httpResponse.getResponse().equalsIgnoreCase("0"))			
			isActiveUser = true;

		request.setIsActiveUser(isActiveUser);


		String url = ComVivaConfigurations.getInstance().getUrl(param_comviva_consent_url,
				circleId);

		validateSubId(requestParams);
		validateCallerId(requestParams);

		String uCode = getUCode(requestParams);

		String vCode = getVcode(requestParams);

		URLBuilder urlBuilder = new URLBuilder(url);
		String callerId = requestParams.get(param_callerID);
		if(callerId != null) {
			callerId = callerId.trim();
			callerId = RBTDBManager.getInstance().subID(callerId);
		}

		urlBuilder = urlBuilder.replaceMsisdn(requestParams.get(param_subscriberID))
				.replaceVCode(vCode).replaceUCode(uCode)
				.replaceCallerId(callerId).replaceConsentParam(isActiveUser);

		httpResponse =  makeGetRequest(urlBuilder.buildUrl(), null);
		Document document = convertToDocument(httpResponse);
		Parser parser = new Parser();
		parser.setDocument(document);
		parser.setParser(new ComVivaSAXParser());
		parser.setResponse(httpResponse.getResponse());
		return parser;
	}

	private HttpResponse makeWSProfileCheck(Map<String,String> requestParams, String urlName, String circleId) throws HttpException, IOException {

		configurations.getLogger().info("makeWSProfileCheck method invoked");
		validateSubId(requestParams);
		String url = ComVivaConfigurations.getInstance().getUrl(urlName,
				circleId);
		URLBuilder urlBuilder = new URLBuilder(url);		
		urlBuilder = urlBuilder.replaceMsisdn(requestParams.get(param_subscriberID));

		return makeGetRequest(urlBuilder.buildUrl(), null);
	}


	private Document createXML(HttpResponse httpResponse) {
		Document document = null;
		if(httpResponse != null && httpResponse.getResponse() != null && !httpResponse.getResponse().isEmpty()) {			
			document = documentBuilder.newDocument();
			Element rbtElement = document.createElement(RBT);
			Element rbtResponseElement = document.createElement(RESPONSE);

			if(ComVivaConfigurations.getInstance().getResponseMapping().containsKey(httpResponse.getResponse())) {
				rbtResponseElement.setTextContent(ComVivaConfigurations.getInstance().getResponseMapping().get(httpResponse.getResponse()));
			}else {
				rbtResponseElement.setTextContent("FAILURE");
			}
			rbtElement.appendChild(rbtResponseElement);
			document.appendChild(rbtElement);
		}

		return document;
	}

	private Document convertToDocument(HttpResponse httpResponse) throws UnsupportedEncodingException {
		Document document = null;
		if(httpResponse != null && httpResponse.getResponse() != null && !httpResponse.getResponse().isEmpty()) {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(httpResponse.getResponse().trim().getBytes("UTF-8"));
			try {
				synchronized (documentBuilder) {
					document = documentBuilder.parse(byteArrayInputStream);
				}
			} catch (Exception e) {
				if (configurations.getLogger().isDebugEnabled()) {
					configurations.getLogger().debug(
							"Failed to parse response: "
									+ byteArrayInputStream.toString());
				}
			}
		}

		return document;

	}

	private HttpResponse makeGetRequest(String url, Map<String,String> requestParams) throws HttpException, IOException {
		configurations.getLogger().info("Making Get Request for url: "+url);
		return rbtHttpClient.makeRequestByGet(url, requestParams);

	}

	@Override
	public Parser makeWebServiceRequest(Request request, String api,
			String action) {
		ConnectorHandler connectionHanlder = null;
		return makeWebServiceRequest(connectionHanlder, request, api, action);
	}
}
