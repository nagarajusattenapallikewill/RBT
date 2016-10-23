package com.onmobile.apps.ringbacktones.daemons.interoperator.tools;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorHttpResponse;

public class InterOperatorHttpUtils
{

	static HttpClient httpClient = new HttpClient();
	static MultiThreadedHttpConnectionManager connectionManager = null;;
	static private Logger logger = Logger.getLogger(InterOperatorHttpUtils.class);

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
	
	static
	{
		connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.setMaxConnectionsPerHost(10);
		connectionManager.setMaxTotalConnections(50);
		httpClient = new HttpClient(connectionManager);
		httpClient.setConnectionTimeout(5000);
		httpClient.setTimeout(5000);
		
	}
	
	public static void main(String[] args)
	{
		String url1 = "http://cvs/cgi/viewcvs.cgi/master/rbt4/readme-build.txt?rev=1.2.2.1&cvsroot=SubMgr&content-type=text/vnd.viewcvs-markup";
		InterOperatorHttpResponse httpResponse = getResponse(url1, null, null);
		System.out.println(httpResponse);
		
	}
	
	public static InterOperatorHttpResponse getResponse(String url, HashMap<String, String> requestParameters, HashMap<String, String> protocolParameters)
	{
		url = getFinalUrl(url, requestParameters);
		logger.info("url hit="+url);
		if(url == null)
			return null;
		HttpMethod getMethod = new GetMethod(url);
		InterOperatorHttpResponse iohttpResponse = null;
		int responseCode = -1;
		String responseString = null;
		Header[] responseHeaders = null;
		try
		{
			iohttpResponse = new InterOperatorHttpResponse();
			iohttpResponse.setFinalUrl(url);
			responseCode = httpClient.executeMethod(getMethod);
			responseString = getMethod.getResponseBodyAsString();
			responseHeaders = getMethod.getResponseHeaders();
			iohttpResponse.setHttpHeaders(responseHeaders);
			iohttpResponse.setHttpResponseCode(responseCode);
			iohttpResponse.setHttpResponseString(responseString);
			logger.info("url responseCode="+responseCode);	
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
		iohttpResponse.setHttpResponseCode(responseCode);
		return iohttpResponse;
	}
	
	public static String getFinalUrl(String url, HashMap<String, String> requestParameters)
	{
		if(url == null || url.trim().length() == 0)
			return null;
		url  =url.trim();
		if(requestParameters == null || requestParameters.size() == 0)
			return url;
		if(url.indexOf("?") == -1 && requestParameters.size() > 0)
			url += "?";
		
		for (String paramName : requestParameters.keySet())
		{
			String paramValue = requestParameters.get(paramName);
			if(url.endsWith("?"))
					url += paramName+"="+getEncodedValue(paramValue);
				else
					url += "&"+paramName+"="+getEncodedValue(paramValue);
		}
		return url;
	}
	
	public static String getEncodedValue(String paramValue)
	{
		if(paramValue == null)
			return null;
		String ret = null;
		try
		{
			ret = m_urlEncoder.encode(paramValue, "UTF-8");
		}
		catch(Throwable t)
		{
			ret = null;
		}
		return ret;}
	
	
}
