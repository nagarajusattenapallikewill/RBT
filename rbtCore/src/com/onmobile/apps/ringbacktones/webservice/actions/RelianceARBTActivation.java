/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SubscriptionClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author Sreekar
 */
public class RelianceARBTActivation implements WebServiceAction, WebServiceConstants, iRBTConstant {

	private static final Logger _logger = Logger.getLogger(RelianceARBTActivation.class);

	private static final String DEFAULT_SUB_CLASS = "DEFAULT";

	public static final String RESP_SUBSCRIPTION_SUCCESS = "1000";
	public static final String RESP_SUBSCRIPTION_ERROR = "1100";
	public static final String RESP_SUBSCRIPTION_ALREADY_ACTIVE = "1101";
	public static final String RESP_SUBSCRIPTION_LOW_BALANCE = "1102";
	public static final String RESP_SUBSCRIPTION_RETRY = "1200";

	public static final Map<String, String> _responseMap = new HashMap<String, String>();

	private static final ParametersCacheManager _parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
	private static final SubscriptionClassCacheManager _subClassCacheManager = CacheManagerUtil
			.getSubscriptionClassCacheManager();

	private static RBTHttpClient _rbtHttpClient;
	
	private static final DateFormat _formatter = new SimpleDateFormat("yyyyMMddHHHmmss");

	static {
		initHttpClient();
		initResponseMap();
	}
	
	private static void initHttpClient() {
		HttpParameters httpParameters = new HttpParameters();

		String param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_CONNECTION_TIMEOUT_MS", "1000");
		httpParameters.setConnectionTimeout(Integer.parseInt(param));

		param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_SOCKET_TIMEOUT_MS", "0");
		httpParameters.setSoTimeout(Integer.parseInt(param));

		param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_PROXY", null);
		if (param != null) {
			int index = param.indexOf(":");
			httpParameters.setProxyHost(param.substring(0, index));
			httpParameters.setProxyPort(Integer.parseInt(param.substring(index + 1)));
		}
		_rbtHttpClient = new RBTHttpClient(httpParameters);
	}
	
	private static void initResponseMap() {
		_responseMap.put(RESP_SUBSCRIPTION_SUCCESS, "SUCCESS");
		_responseMap.put(RESP_SUBSCRIPTION_ERROR, "ERROR");
		_responseMap.put(RESP_SUBSCRIPTION_ALREADY_ACTIVE, "ALREADY_ACTIVE");
		_responseMap.put(RESP_SUBSCRIPTION_LOW_BALANCE, "LOW_BALANCE");
		_responseMap.put(RESP_SUBSCRIPTION_RETRY, "RETRY");
	}
	
	private WebServiceResponse getWebServiceResponse(String response) {
		WebServiceResponse webserviceResponse = new WebServiceResponse(_responseMap.get(response));
		webserviceResponse.addResponseHeader("_status", response);
		webserviceResponse.setResponseWriter(WebServiceResponseFactory.getResponseWriter(StringResponseWriter.class));
		return webserviceResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones
	 * .webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		_logger.info("starting with context " + webServiceContext);
		String subscriberId = webServiceContext.getString(param_msisdn);
		if (subscriberId == null) {
			_logger.info("no subscriber id passed");
			return getWebServiceResponse(RESP_SUBSCRIPTION_ERROR);
		}
		
		String subClassStr = webServiceContext.getString(param_subClass);
		/*if(subClassStr == null) {
			_logger.info("no subclass passed");
			return getWebServiceResponse(RESP_SUBSCRIPTION_ERROR);
		}*/
		if (subClassStr != null) {
			SubscriptionClass subClass = _subClassCacheManager.getSubscriptionClass(subClassStr);
			if (subClass == null) {
				_logger.info("invalid subclass " + subClassStr);
				return getWebServiceResponse(RESP_SUBSCRIPTION_ERROR);
			}
		}

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		if (subscriber != null) {
			String status = subscriber.subYes();
			if (status.equals(STATE_ACTIVATED) || status.equals(STATE_TO_BE_ACTIVATED)
					|| status.equals(STATE_ACTIVATION_PENDING) || status.equals(STATE_DEACTIVATION_PENDING)
					|| status.equals(STATE_TO_BE_DEACTIVATED) || status.equals(STATE_SUSPENDED)
					|| status.equals(STATE_SUSPENDED_INIT) || status.equals(STATE_GRACE) || status.equals(STATE_CHANGE))
				return getWebServiceResponse(RESP_SUBSCRIPTION_ALREADY_ACTIVE);
			else {
				_logger.info("Activating " + subscriberId + " with status " + status);
			}
		}

		// insert into DB
		insertIntoDB(webServiceContext);

		String smResponse = hitSM(webServiceContext);
		
		// if failure from SM delete from DB
		if(!smResponse.equals(RESP_SUBSCRIPTION_SUCCESS)) {
			RBTDBManager.getInstance().deleteSubscriber(subscriberId);
		}
		
		return getWebServiceResponse(smResponse);
	}
	
	private void insertIntoDB(WebServiceContext webServiceContext) {
		String subscriberId = webServiceContext.getString(param_msisdn);
		String mode = webServiceContext.getString(param_mode);
		if(mode == null)
			mode = "ARBT";
		String modeInfo = mode;
		if(webServiceContext.containsKey(param_ipAddress))
			modeInfo = mode + ":" + webServiceContext.getString(param_ipAddress);
		String subscriptionClass = webServiceContext.getString(param_subClass);
		if (subscriptionClass == null)
			subscriptionClass = DEFAULT_SUB_CLASS;
		boolean isPrepaid = true;
		String subType = webServiceContext.getString(param_subtype);
		if(subType != null && subType.toLowerCase().startsWith("post"))
			isPrepaid = false;
		
		webServiceContext.put(param_subscriberID, subscriberId);
		String circleId = DataUtils.getUserCircle(webServiceContext);
		
		//TODO circle ID
		Subscriber subscriber = RBTDBManager.getInstance().activateSubscriber(subscriberId, mode, null, null, isPrepaid,
				0, 0, modeInfo, subscriptionClass, true, null, true, 1, circleId);
		webServiceContext.put(param_refID, subscriber.refID());
		webServiceContext.put(param_subscriber, subscriber);
	}
	
	public static final String getSMSubType(String subType) {
		if(subType == null || subType.toLowerCase().startsWith("pre"))
			return "P";
		return "B";
	}

	/**
	 * Invokes SM URL via HTTP
	 * 
	 * @param webServiceContext
	 * @return
	 */
	private String hitSM(WebServiceContext webServiceContext) {
		String response = RESP_SUBSCRIPTION_RETRY;
		String url = _parametersCacheManager.getParameterValue(COMMON, "ARBT_ACT_URL", null);
		if (url == null) {
			_logger.fatal("ARBT activation url not configured");
			return response;
		}

		Subscriber subscriber = (Subscriber)webServiceContext.get(param_subscriber);
		
		String subscriberId = webServiceContext.getString(param_msisdn);
		String mode = webServiceContext.getString(param_mode);
		if(mode == null)
			mode = "ARBT";
		String subscriptionClass = webServiceContext.getString(param_subClass);
		if (subscriptionClass == null)
			subscriptionClass = DEFAULT_SUB_CLASS;
		String refId = webServiceContext.getString(param_refID);
		String userType = getSMSubType(webServiceContext.getString(param_subtype));
		String modeInfo = subscriber.activationInfo();
		String cosId = subscriber.cosID();
		String language = subscriber.language();
		if(language == null)
			language= "";

		url = url.replace("%MSISDN%", subscriberId);
		url = url.replace("%MODE%", mode);
		url = url.replace("%USER_TYPE%", userType);
		url = url.replace("%SUBSCRIPTION_CLASS%", subscriptionClass);
		url = url.replace("%MODE_INFO%", modeInfo);
		url = url.replace("%LANGUAGE%", language);
		if(refId != null)
			url = url.replace("%REF_ID%", refId);
		if(cosId != null)
			url = url.replace("%COS_ID%", cosId);
		
		_logger.info("arbt activation url " + url);
		
		HttpResponse httpResponse = null;

		Date startTime = new Date();
		try {
			httpResponse = _rbtHttpClient.makeRequestByGet(url, null);
		}
		catch (Exception e) {
			_logger.error("Exception while calling ARBT activation url " + e.getMessage(), e);
			return response;
		}
		finally {
			if (httpResponse != null) {
				String smResponse = httpResponse.getResponse();
				response = processSMResponse(smResponse);
			}

			Date endTime = new Date();
			StringBuilder sb = new StringBuilder();
			sb.append(subscriberId);
			sb.append(",");
			sb.append("ARBT_RT_ACTIVATION");
			sb.append(",");
			sb.append(response);
			sb.append(",");
			sb.append(url);
			sb.append(",");
			if (httpResponse != null) {
				String smResponse = httpResponse.getResponse();
				smResponse = smResponse.replace("\n", "");
				smResponse = smResponse.replace("\r", "");
				smResponse = smResponse.replace("\t", "");
				sb.append(smResponse);
				sb.append("|");
				sb.append(httpResponse.getResponseCode());
			}
			else
				sb.append("EXCEPTION");
			sb.append(",");
			sb.append(_formatter.format(startTime));
			sb.append(",");
			sb.append(endTime.getTime() - startTime.getTime());

			RBTEventLogger.logEvent(RBTEventLogger.Event.ARBT, sb.toString());
		}
		
		return response;
	}
	
	/** 
	 * sets WebServiceResponse based on SM response
	 * 
	 * @param smResponse XML response received from SM for the realtime subscrition request
	 * @return
	 */
	private String processSMResponse(String smResponse) {
		_logger.info("SM response is " + smResponse);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(smResponse.getBytes()));
			
			Node statusNode = document.getElementsByTagName("status").item(0);
			String status = statusNode.getTextContent();
			
			Node reasonNode = document.getElementsByTagName("reason").item(0);
			String reason = reasonNode.getTextContent();
			
			if(status != null && status.equalsIgnoreCase("SUCCESS"))
				return RESP_SUBSCRIPTION_SUCCESS;
			else if(reason != null && reason.equalsIgnoreCase("LOW_BALANCE"))
				return RESP_SUBSCRIPTION_LOW_BALANCE;
			else
				return RESP_SUBSCRIPTION_ERROR;
		}
		catch (ParserConfigurationException e) {
			_logger.error(e.getMessage(), e);
		}
		catch (SAXException e) {
			_logger.error(e.getMessage(), e);
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
		
		return RESP_SUBSCRIPTION_ERROR;
	}
}