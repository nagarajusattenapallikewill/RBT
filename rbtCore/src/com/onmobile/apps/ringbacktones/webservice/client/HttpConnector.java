/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.features.RN.RNBean;
import com.onmobile.apps.ringbacktones.webservice.features.RN.RNPropertyUtils;
import com.onmobile.apps.ringbacktones.webservice.features.RN.RNResponseUtils;

/**
 * @author vinayasimha.patil
 * @author abhinav.anand
 */
public class HttpConnector implements Connector
{
	private RBTHttpClient rbtHttpClient = null;
	private RBTHttpClient bulkRBTHttpClient = null;
	private Configurations configurations = null;

	private DocumentBuilder documentBuilder = null;

	/**
	 * @param configurations
	 */
	public HttpConnector(Configurations configurations) throws Exception
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

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#redirectWebServiceRequest(java.utils.HashMap, java.lang.String)
	 */
	public String redirectWebServiceRequest(HashMap<String,String> requestParams, String api)
	{
		String returnStr = null;
		try
		{
			configurations.getLogger().info("RBT:: requestParams: " + requestParams);
			String url = getUrl(requestParams, api);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: url: ").append(url);
			configurations.getLogger().info(stringBuilder.toString());

			if (url == null)
			{
				String response = "<" + RBT + "><" + RESPONSE + ">" + INVALID_PREFIX + "</" + RESPONSE + "></" + RBT + ">";
				configurations.getLogger().info("RBT:: url is null, returning: " + response);
				return response;
			}

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			HttpResponse httpResponse = null;
			if (fileParams == null || fileParams.size() == 0) {
				httpResponse = rbtHttpClient.makeRequestByGet(url, requestParams);
				if (configurations.getLogger().isDebugEnabled()) {
					configurations.getLogger().debug(
							"RBT:: " + rbtHttpClient.getConnectionPoolStatus(url));
				}
			} else {
				httpResponse = bulkRBTHttpClient.makeRequestByPost(url, requestParams, fileParams);
				if (configurations.getLogger().isDebugEnabled()) {
					configurations.getLogger().debug(
							"RBT:: " + bulkRBTHttpClient.getConnectionPoolStatus(url));
				}
			}

			configurations.getLogger().info("RBT:: httpResponse: " + httpResponse);

			if (httpResponse != null && httpResponse.getResponse() != null)
			{
				returnStr=httpResponse.getResponse().trim();
			}
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return returnStr;
	}

		
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#makeWebServiceRequest(com.onmobile.apps.ringbacktones.webservice.client.requests.Request, java.lang.String, java.lang.String)
	 */
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler, Request request, String api, String action)
	{
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		Document document = null;
		try
		{
			
			HashMap<String, String> requestParams = request.getRequestParamsMap();
			if (action != null) requestParams.put(param_action, action);
			if (connectorHandler != null) {
				Map<String, String> b2bSubInfo = connectorHandler
						.getB2bUserInfo();
				if (null != b2bSubInfo && !b2bSubInfo.isEmpty()
						&& null != request.getRequestParamsMap()) {
					requestParams.putAll(b2bSubInfo);
				}
			}
			configurations.getLogger().info("RBT:: requestParams: " + requestParams);
			String url = getUrl(requestParams, api);

			//RBT-15014
			//Added to exclude circle id if request is from Mobile App
			if ("MOBILEAPP".equalsIgnoreCase(requestParams.get(param_mode)) && api.equalsIgnoreCase(api_ApplicationDetails)
					&& requestParams.containsKey(param_info)
					&& requestParams.get(param_info).equalsIgnoreCase(RBT_LOGIN_USER) && requestParams.containsKey(param_circleID)) {
				
				requestParams.remove(param_circleID);
				
			}
			
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: url: ").append(url);
			configurations.getLogger().info(stringBuilder.toString());

			if (url == null)
			{
				String response = "<" + RBT + "><" + RESPONSE + ">" + INVALID_PREFIX + "</" + RESPONSE + "></" + RBT + ">";
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes());

				synchronized (documentBuilder)
				{
					document = documentBuilder.parse(byteArrayInputStream);
				}

				configurations.getLogger().info("RBT:: url is null, returning: " + response);
				Parser parser = new Parser();
				parser.setDocument(document);
				parser.setRequest(request);
				parser.setParser(new RBTParser());
				return parser;
			}

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			HttpResponse httpResponse = null;
			String isPostMethod = requestParams.get(param_isPostMethod);
			if (fileParams == null || fileParams.size() == 0) {
				if(action != null && (action.equalsIgnoreCase(action_addMultipleMember) 
						|| action.equalsIgnoreCase(action_removeMultipleMember))) {
					httpResponse = rbtHttpClient.makeRequestByPost(url, requestParams, null);
				} else if (isPostMethod != null && isPostMethod.equalsIgnoreCase(YES)){
					httpResponse = rbtHttpClient.makeRequestByPost(url, requestParams, null);
				} else {
					httpResponse = rbtHttpClient.makeRequestByGet(url, requestParams);
				}
				if (configurations.getLogger().isDebugEnabled()) {
					configurations.getLogger().debug(
							"RBT:: " + rbtHttpClient.getConnectionPoolStatus(url));
				}
			} else {
				httpResponse = bulkRBTHttpClient.makeRequestByPost(url, requestParams, fileParams);
				if (configurations.getLogger().isDebugEnabled()) {
					configurations.getLogger().debug(
							"RBT:: " + bulkRBTHttpClient.getConnectionPoolStatus(url));
				}
			}

			configurations.getLogger().info("RBT:: httpResponse: " + httpResponse);
			
			String response = httpResponse.getResponse();
			
			if (requestParams.containsKey("IS_RN_URL")) {
				String info = requestParams.get(param_info);
				String subscriberId = getSubscriberId(requestParams, api, action);
				subscriberId = RBTDBManager.getInstance().subID(subscriberId);
				String clipId = requestParams.get(param_clipID);
				String mode = requestParams.get(param_mode);
				RNBean rnBean = new RNBean(httpResponse.getResponse(), api, action, info, subscriberId, clipId, mode);
				document = documentBuilder.newDocument();
				document = RNResponseUtils.getResponseDocument(rnBean, document);
			} else if(null != response && (response.trim().equalsIgnoreCase("success") 
					|| response.trim().equalsIgnoreCase("failure") 
					|| response.trim().equalsIgnoreCase("fail")
					|| response.trim().equalsIgnoreCase("failed"))) {
				document = documentBuilder.newDocument();
				
				Element rbtEle = document.createElement(RBT);
				
				Element rbtResponseEle = document.createElement(RESPONSE);
				rbtResponseEle.setTextContent(response);

				rbtEle.appendChild(rbtResponseEle);
				
				document.appendChild(rbtEle);
				
				configurations.getLogger().info("Plain httpResponse: " + httpResponse);
				
			} else if (httpResponse != null && httpResponse.getResponse() != null) {
				
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
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}
		Parser parser = new Parser();
		parser.setDocument(document);
		parser.setRequest(request);
		parser.setParser(new RBTParser());
		return parser;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#redirectWebServiceBulkRequest(java.utils.HashMap, java.lang.String)
	 */
	/**
	public File redirectWebServiceBulkRequest(HashMap<String,String> requestParams, String api)
	{
		File file = null;
		try
		{
			String url = getUrl(requestParams, api);
			if (url == null)
			{
				configurations.getLogger().info("RBT:: url is null, returning: null");
				return null;
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: url: ").append(url);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}
			
			HttpResponse httpResponse = bulkRBTHttpClient.makeRequestByPost(url, requestParams, fileParams);
			configurations.getLogger().info("RBT:: httpResponse: " + httpResponse);

			if (configurations.getLogger().isDebugEnabled())
				configurations.getLogger().debug("RBT:: " + bulkRBTHttpClient.getConnectionPoolStatus(url));

			if (httpResponse != null && httpResponse.getResponse() != null)
				file = new File(httpResponse.getResponse().trim());
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return file;
	}

	**/
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#makeWebServiceBulkRequest(com.onmobile.apps.ringbacktones.webservice.client.requests.Request, java.lang.String, java.lang.String)
	 */
	public File makeWebServiceBulkRequest(Request request, String api, String action)
	{
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		File file = null;
		try
		{
			HashMap<String, String> requestParams = request.getRequestParamsMap();
			if (action != null) requestParams.put(param_action, action);

			String url = getUrl(requestParams, api);
			if (url == null)
			{
				configurations.getLogger().info("RBT:: url is null, returning: null");
				return null;
			}
			configurations.getLogger().info("REQ_PARAM2: "+requestParams);
			//RBT-15014
			//Added to exclude circle id if request is from Mobile App
			if ("MOBILEAPP".equalsIgnoreCase(requestParams.get(param_mode)) && api.equalsIgnoreCase(api_ApplicationDetails)
					&& requestParams.containsKey(param_info)
					&& requestParams.get(param_info).equalsIgnoreCase(RBT_LOGIN_USER) && requestParams.containsKey(param_circleID)) {
				
				requestParams.remove(param_circleID);
				
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: url: ").append(url);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}
			
			HttpResponse httpResponse = bulkRBTHttpClient.makeRequestByPost(url, requestParams, fileParams);
			configurations.getLogger().info("RBT:: httpResponse: " + httpResponse);

			if (configurations.getLogger().isDebugEnabled())
				configurations.getLogger().debug("RBT:: " + bulkRBTHttpClient.getConnectionPoolStatus(url));

			if (httpResponse != null && httpResponse.getResponse() != null)
				file = new File(httpResponse.getResponse().trim());
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return file;
	}

	private String getUrl(HashMap<String, String> requestParams, String api)
	{
		String url = null;
		/* operatorID has to be passed by SRBT central box only. No operator specific box will pass this parameter*/
		String operatorID = null;
		operatorID = requestParams.get(param_operatorID);

		String circleID = null;
		circleID = requestParams.get(param_circleID);
		
		if (circleID == null && operatorID == null)
		{
			
			String action = requestParams.get(param_action);
			String subscriberID = getSubscriberId(requestParams, api, action);

			if (action != null &&
					(action.equals(action_getClipRating)
					|| action.equals(action_rateClip)
					|| action.equals(action_likeClip)
					|| action.equals(action_dislikeClip)))
			{
				circleID = configurations.getCentralCircle();
			}
			else if (api.equalsIgnoreCase(api_ApplicationDetails)
					&& requestParams.containsKey(param_info)
					&& requestParams.get(param_info).equalsIgnoreCase(RBT_LOGIN_USER) && requestParams.get(param_subscriberID) == null)
			{
				circleID = configurations.getCentralCircle();
			}
			else if (configurations.isUseMNPService() && subscriberID != null)
			{
				SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID, "RBTCLIENT"));
				if (subscriberDetail != null)
					circleID = subscriberDetail.getCircleID();
			}
			else
				circleID = configurations.getCircle(subscriberID);
		}
		
		String rnUrl = RNPropertyUtils.getUrl(circleID, api, requestParams); 
		if (rnUrl != null) {
			requestParams.put("IS_RN_URL", "TRUE");
			return rnUrl;
		}

		String ipAddressNPort = null;
	    String B2BOperatorName = requestParams.get("B2B_OPERATORNAME");
	    String B2BCircleId  = requestParams.get("B2B_CIRCLEID");
		if (B2BOperatorName != null && B2BCircleId != null
				&& !B2BOperatorName.isEmpty() && !B2BCircleId.isEmpty()) {
			ipAddressNPort = configurations.getValueFromResourceBundle(B2BOperatorName + "_" + B2BCircleId);
		}
		if (operatorID != null && (ipAddressNPort==null || ipAddressNPort.isEmpty()) )
			ipAddressNPort = configurations.getIPAddressNPort(operatorID);
		else if(ipAddressNPort==null || ipAddressNPort.isEmpty())
			ipAddressNPort = configurations.getIPAddressNPort(circleID);
		if(requestParams.containsKey(param_restRequest) && Boolean.parseBoolean(requestParams.get(param_restRequest))){
			url = configurations.getProtocol() + "://" + ipAddressNPort + "/rbt/v2/" + api + "/rbt" ;
			requestParams.remove(param_restRequest);
		}else if (ipAddressNPort != null){
			url = configurations.getProtocol() + "://" + ipAddressNPort + "/rbt/" + api + ".do";
		}
		return url;
	}

	public static String getSubscriberId(Map<String, String> requestParams,
			String api, String action) {
		String subscriberID = requestParams.get(param_subscriberID);
		if (api.equalsIgnoreCase(api_Gift))
		{
			if (action.equalsIgnoreCase(action_sendGift))
				subscriberID = requestParams.get(param_gifterID);
			else if (action.equalsIgnoreCase(action_rejectGift))
				subscriberID = requestParams.get(param_gifteeID);
		}
		else if (api.equalsIgnoreCase(api_Copy) && requestParams.containsKey(param_fromSubscriber))
			subscriberID = requestParams.get(param_fromSubscriber);
		else if (api.equalsIgnoreCase(api_Utils) && requestParams.containsKey(param_receiverID))
			subscriberID = requestParams.get(param_receiverID);
		else if (api.equalsIgnoreCase(api_Data) && requestParams.containsKey(param_callerID))
			subscriberID = requestParams.get(param_callerID);
		return subscriberID;
	}
	
	public String makeRestRequest(Request request, String api,
			String action) {
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		String response = null;
		try {
			HashMap<String, String> requestParams = request
					.getRequestParamsMap();
			if (action != null)
				requestParams.put(param_action, action);

			configurations.getLogger().info(
					"RBT:: requestParams: " + requestParams);
			String url = getUrl(requestParams, api);
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api)
					.append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams)
					.append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: url: ").append(url);
			configurations.getLogger().info(stringBuilder.toString());

			if (url == null) {
				return "{\"response\":" + INVALID_PREFIX + "}";
			}

			HttpResponse httpResponse = null;
			httpResponse = rbtHttpClient.makeRequestByGet(url, requestParams);

			if (configurations.getLogger().isDebugEnabled()) {
				configurations.getLogger().debug(
						"RBT:: " + rbtHttpClient.getConnectionPoolStatus(url));
			}

			configurations.getLogger().info(
					"RBT:: httpResponse: " + httpResponse);

			response = httpResponse.getResponse();
		} catch (Exception e) {
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return response;
	}

	@Override
	public Parser makeWebServiceRequest(Request request, String api,
			String action) {
				ConnectorHandler connectionHanlder = null;
				return makeWebServiceRequest(connectionHanlder, request, api, action);
	}
	
}
