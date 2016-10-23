package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet.ComvivaFactoryObject;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.BasicResponseHandler;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.logger.consent.ConsentUrlHitLogger;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class DoubleConfirmationHttpUtils {


	private static HttpClient httpClient = new HttpClient();
	private static MultiThreadedHttpConnectionManager connectionManager = null;;
	private static Logger logger = Logger.getLogger(DoubleConfirmationHttpUtils.class);

	//Http Status Codes
	public static final int httpStatusCode_200 = 200;
	public static final int httpStatusCode_202 = 202;
	public static final int httpStatusCode_404 = 200;
	public static final int httpStatusCode_500 = 500;

	//Http Parameters
	public static final String http_URL = "URL";
	public static final long http_readTimeOut = 5000;
	public static final long http_connectionTimeOut = 5000;
	private static URLCodec m_urlEncoder = new URLCodec();
	private static RBTHttpClient rbtHttpClient = null;
	static
	{
		connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.setMaxConnectionsPerHost(10);
		connectionManager.setMaxTotalConnections(50);
		httpClient = new HttpClient(connectionManager);
		httpClient.setConnectionTimeout(5000);
		httpClient.setTimeout(5000);

		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(200);
		httpParameters.setMaxHostConnections(200);
		httpParameters.setConnectionTimeout(RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, "SMDAEMON_TIMEOUT",6)*1000);
		httpParameters.setSoTimeout(RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON, "SMDAEMON_TIMEOUT",6)*1000);
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	/**
	 * @param url
	 * @param requestParameters
	 * @param protocolParameters
	 * @return
	 */
	public static String getResponse(String url)
	{
		logger.info("url hit = " + url);
		if (url == null)
			return null;

		String cgAuthenticationDetails = RBTParametersUtils.getParamAsString(iRBTConstant.DOUBLE_CONFIRMATION, 
				"USERNAME_PASSWORD_CG_AUTHENTICATION", null);
		HttpMethod getMethod = new GetMethod(url);
		int responseCode = -1;
		String responseString = null;
		Header[] responseHeaders = null;
		try
		{	
			if (cgAuthenticationDetails != null) {
				String[] cgAuthenticationDetailsArray = cgAuthenticationDetails.split(",");
				for (int i = 0; i < cgAuthenticationDetailsArray.length; i++) {
					getMethod.setRequestHeader(
							cgAuthenticationDetailsArray[i].split(":")[0],
							cgAuthenticationDetailsArray[i].split(":")[1]);
				}
			}
			//logger.info(getMethod.getRequestHeaders());
			responseCode = httpClient.executeMethod(getMethod);
			responseString = getMethod.getResponseBodyAsString();
			responseHeaders = getMethod.getResponseHeaders();
			logger.info("url responseCode = " + responseCode + ", responseString = " + responseString);	
		}
		catch (HttpException e)
		{
			logger.error("httpexception occured", e);
		}
		catch (IOException e)
		{
			logger.error("IOException occured", e);
		}
		finally
		{
			getMethod.releaseConnection();
		}
		BasicResponseHandler responseHandler = ComvivaFactoryObject.getResponseInstance();
		
		return responseHandler.processResponse(responseCode,
				(null != responseString ? responseString.trim()
						: responseString));
	}
	
	public static HttpResponse getResponse(String url, HashMap<String, String> requestParams, String mode, String entityType, String requestType)
	{
		HashMap<String, String> logParams = new HashMap<String, String>();
		try
		{
			logParams.put(ConsentUrlHitLogger.URL, url);
			logParams.put(ConsentUrlHitLogger.ENTITY, entityType);
			long startTime = System.currentTimeMillis();
			HttpResponse httpResponse;
			if(requestType != null && requestType.equalsIgnoreCase("GET"))
				httpResponse = rbtHttpClient.makeRequestByGet(url, requestParams);
			else
				httpResponse = rbtHttpClient.makeRequestByPost(url, requestParams, null);
		
			int statusCode = httpResponse.getResponseCode();
			logParams.put(ConsentUrlHitLogger.HTTP_CODE, String.valueOf(statusCode));
			String response = httpResponse.getResponse();
			logParams.put(ConsentUrlHitLogger.HTTP_RESPONSE, response);
			long endTime = System.currentTimeMillis();
			logParams.put(ConsentUrlHitLogger.TIME_TAKEN, String.valueOf(endTime - startTime));
			logger.info("Response:"+response+", statusCode="+statusCode+", timetaken="+(endTime-startTime)+"ms");
			return httpResponse;
		}
		catch (HttpException e)
		{
			logParams.put(ConsentUrlHitLogger.HTTP_RESPONSE, "HTTP_EXCEPTION");
			logger.error("Exception", e);
			return null;
		}
		catch (IOException e)
		{
			logParams.put(ConsentUrlHitLogger.HTTP_RESPONSE, "IO_EXCEPTION");
			logger.error("Exception", e);
			return null;
		}
		finally
		{
			ConsentUrlHitLogger.log(logParams, requestParams);
		}
	}
}
