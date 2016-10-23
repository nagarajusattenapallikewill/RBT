/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceContext;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.Severity;

/**
 * @author vinayasimha.patil
 */
public class RBTHttpClient
{
	private HttpClient httpClient = null;
	private HttpParameters httpParameters = null;
	
	private Set<String> circleIDSet = new HashSet<String>();
	private static Object object = new Object();
	
//	private static RBTHttpClient rbtHttpClientObj = null;
	
	/*static {
		if(rbtHttpClientObj == null) {
			synchronized (object) {
				if(rbtHttpClientObj == null) {
					rbtHttpClientObj = new RBTHttpClient(null);
				}
			}
		}
	}*/

	/**
	 * @param httpParameters
	 */
	public RBTHttpClient(HttpParameters httpParameters)
	{
		this.httpParameters = httpParameters;

		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();

		HttpConnectionManagerParams httpConnectionManagerParams = multiThreadedHttpConnectionManager
				.getParams();
		httpConnectionManagerParams.setConnectionTimeout(httpParameters
				.getConnectionTimeout());
		httpConnectionManagerParams.setSoTimeout(httpParameters.getSoTimeout());
		httpConnectionManagerParams.setMaxTotalConnections(httpParameters
				.getMaxTotalConnections());
		httpConnectionManagerParams
				.setDefaultMaxConnectionsPerHost(httpParameters
						.getMaxHostConnections());

		httpClient = new HttpClient(multiThreadedHttpConnectionManager);

		HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
		if (httpParameters.isUseProxy())
			hostConfiguration.setProxy(httpParameters.getProxyHost(),
					httpParameters.getProxyPort());

		DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(
				0, false);
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				defaultHttpMethodRetryHandler);
	}

	/**
	 * Returns the httpParameters.
	 * 
	 * @return the httpParameters
	 */
	public HttpParameters getHttpParameters()
	{
		return httpParameters;
	}

	public String getConnectionPoolStatus(String url)
	{
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = (MultiThreadedHttpConnectionManager) httpClient
				.getHttpConnectionManager();
		String conncetionPoolStatus = "HttpConnectionPoolStatus[Total Connections: "
				+ multiThreadedHttpConnectionManager.getConnectionsInPool();

		try
		{
			URL urlObj = new URL(url);
			HostConfiguration hostConfiguration = new HostConfiguration();
			hostConfiguration.setHost(urlObj.getHost(), urlObj.getPort());
			conncetionPoolStatus += ", Connections for Host "
					+ urlObj.getHost()
					+ ": "
					+ multiThreadedHttpConnectionManager
							.getConnectionsInPool(hostConfiguration);
		}
		catch (MalformedURLException e)
		{
		}

		conncetionPoolStatus += "]";
		return conncetionPoolStatus;
	}

	public HttpResponse makeRequestByGet(String url,
			Map<String, String> requestParams) throws IOException,
			HttpException
	{
		GetMethod getMethod = new GetMethod();

		if (requestParams == null)
			requestParams = new HashMap<String, String>();

		String circleId = requestParams.remove("CIRCLE_ID");
		addRequestParams(url, requestParams, getMethod);

		HttpResponse httpResponse = makeRequest(getMethod, circleId);
		return httpResponse;
	}

	public HttpResponse makeRequestByPost(String url,
			Map<String, String> requestParams, Map<String, File> fileParams)
			throws IOException, HttpException
	{
		PostMethod postMethod = new PostMethod();

		if (requestParams == null)
			requestParams = new HashMap<String, String>();
		
		String circleId = requestParams.remove("CIRCLE_ID");

		addRequestParams(url, requestParams, fileParams, postMethod);

		HttpResponse httpResponse = makeRequest(postMethod, circleId);
		return httpResponse;
	}

	private HttpResponse makeRequest(HttpMethod httpMethod, String circleId) throws IOException,
			HttpException
	{
		HttpResponse httpResponse = executeMethod(httpClient, httpMethod,
				httpParameters.getHttpPerformanceMonitor(), circleId);
		return httpResponse;
	}

	public static HttpResponse makeRequestByGet(HttpParameters httpParameters,
			Map<String, String> requestParams) throws IOException, HttpException
	{
		GetMethod getMethod = new GetMethod(); 

		if (requestParams == null)			
			requestParams = new HashMap<String, String>();

		String circleId = requestParams.remove("CIRCLE_ID");
		addRequestParams(httpParameters.getUrl(), requestParams, getMethod);

		HttpResponse httpResponse = new RBTHttpClient(httpParameters).makeRequest(httpParameters, getMethod, circleId);
		return httpResponse;
	}
	
	public static HttpResponse makeRequestByPost(HttpParameters httpParameters,
			Map<String, String> requestParams, Map<String, File> fileParams)
			throws IOException, HttpException
	{
		PostMethod postMethod = new PostMethod();

		if (requestParams == null)
			requestParams = new HashMap<String, String>();
		
		String requestBody = requestParams.remove("BI_POST");
		if(requestBody != null) {
			postMethod.setRequestBody(requestBody);
		}

		String circleId = requestParams.remove("CIRCLE_ID");
		addRequestParams(httpParameters.getUrl(), requestParams, fileParams,
				postMethod);

		HttpResponse httpResponse = new RBTHttpClient(httpParameters).makeRequest(httpParameters, postMethod, circleId);
		return httpResponse;
	}

	private HttpResponse makeRequest(HttpParameters httpParameters,
			HttpMethod httpMethod, String circleId) throws IOException, HttpException
	{
		SimpleHttpConnectionManager simpleHttpConnectionManager = new SimpleHttpConnectionManager();

		HttpConnectionManagerParams httpConnectionManagerParams = simpleHttpConnectionManager
				.getParams();
		httpConnectionManagerParams.setConnectionTimeout(httpParameters
				.getConnectionTimeout());
		httpConnectionManagerParams.setSoTimeout(httpParameters.getSoTimeout());

		HttpClient httpClient = new HttpClient(simpleHttpConnectionManager);
		
		UsernamePasswordCredentials usernamePasswordCredentials = httpParameters.getUsernamePasswordCredentials();
		if(usernamePasswordCredentials != null) {
			httpClient.getState().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
		}

		URL url = new URL(httpParameters.getUrl());
		HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
		hostConfiguration.setHost(url.getHost(), url.getPort());
		if (httpParameters.isUseProxy())
			hostConfiguration.setProxy(httpParameters.getProxyHost(),
					httpParameters.getProxyPort());

		DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(
				0, false);
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				defaultHttpMethodRetryHandler);

		HttpResponse httpResponse = executeMethod(httpClient, httpMethod,
				httpParameters.getHttpPerformanceMonitor(), circleId);
		return httpResponse;
	}

	private HttpResponse executeMethod(HttpClient httpClient,
			HttpMethod httpMethod, HttpPerformanceMonitor httpPerformanceMonitor, String circleId)
			throws IOException, HttpException
	{
		int responseCode = -1;
		String response = null;
		Header[] responseHeaders = null;

		long responseTime = 0;
		long startTime = System.currentTimeMillis();
		try
		{
			responseCode = httpClient.executeMethod(httpMethod);

			String fileName = getDownloadableFileName(httpMethod);
			if (fileName != null)
			{
				String tmpDir = System.getProperty("java.io.tmpdir");
				File file = new File(tmpDir, fileName);
				response = file.getAbsolutePath();

				FileOutputStream fileOutputStream = null;
				InputStream inputStream = null;
				try
				{
					fileOutputStream = new FileOutputStream(file);
					inputStream = httpMethod.getResponseBodyAsStream();

					byte[] buffer = new byte[1024];

					int readCount;
					while ((readCount = inputStream.read(buffer)) > 0)
					{
						fileOutputStream.write(buffer, 0, readCount);
					}
				}
				catch (IOException e)
				{
					throw e;
				}
				finally
				{
					if (fileOutputStream != null)
						fileOutputStream.close();
					if (inputStream != null)
						inputStream.close();
				}
			}
			else
			{
				ByteArrayOutputStream byteArrayOutputStream = null;
				InputStream inputStream = null;
				try
				{
					byteArrayOutputStream = new ByteArrayOutputStream();
					inputStream = httpMethod.getResponseBodyAsStream();

					byte[] buffer = new byte[1024];

					int readCount;
					while (inputStream != null && (readCount = inputStream.read(buffer)) > 0)
					{
						byteArrayOutputStream.write(buffer, 0, readCount);
					}
				}
				catch (IOException e)
				{
					throw e;
				}
				finally
				{
					if (byteArrayOutputStream != null)
						byteArrayOutputStream.close();
					if (inputStream != null)
						inputStream.close();
				}
				byte[] responseBody = null;
				if (byteArrayOutputStream != null)
					responseBody = byteArrayOutputStream.toByteArray();
				if (responseBody != null && responseBody.length > 0)
					response = new String(responseBody, "UTF-8");
			}

			responseTime = System.currentTimeMillis() - startTime;
			responseHeaders = httpMethod.getResponseHeaders();

			try
			{
				if (httpPerformanceMonitor != null)
				{
					URI uri = httpMethod.getURI();
					String httpUrl = uri.getURI();

					PerformanceContext performanceContext = new PerformanceContext(
							responseTime, httpUrl);
					httpPerformanceMonitor.logPerformance(performanceContext);
				}
			}
			catch (Exception e)
			{
			}

			if ((responseCode >= 300 && responseCode <= 399) // Redirection
					|| (responseCode >= 400 && responseCode <= 499) // Client Error
					|| (responseCode >= 500 && responseCode <= 599)) // Server Error
			{
				sendNotification(httpMethod, httpPerformanceMonitor,
						Severity.CRITICAL,
						"Error response from server. Ststus Code: "
								+ responseCode, circleId);
			}
			else
			{
				sendNotification(httpMethod, httpPerformanceMonitor,
						Severity.CLEAR, "Normally Restored", circleId);
			}
		}
		catch (NoRouteToHostException e) {
			String alarmMessage = e.getMessage();
			if (alarmMessage == null)
				alarmMessage = " No route to host";

			sendNotification(httpMethod, httpPerformanceMonitor,
					Severity.CRITICAL, alarmMessage, circleId);
			throw e;
		}
		catch (ConnectException e)
		{
			String alarmMessage = e.getMessage();
			if (alarmMessage == null)
				alarmMessage = "Not able to connect";

			sendNotification(httpMethod, httpPerformanceMonitor,
					Severity.CRITICAL, alarmMessage, circleId);
			throw e;
		}
		catch (ConnectTimeoutException e)
		{
			String alarmMessage = e.getMessage();
			if (alarmMessage == null)
				alarmMessage = "Not able to connect";

			sendNotification(httpMethod, httpPerformanceMonitor,
					Severity.CRITICAL, alarmMessage, circleId);
			throw e;
		}
		catch (NoHttpResponseException e)
		{
			String alarmMessage = e.getMessage();
			if (alarmMessage == null)
				alarmMessage = "No response from sever";

			sendNotification(httpMethod, httpPerformanceMonitor,
					Severity.WARNING, alarmMessage, circleId);
			throw e;
		}
		catch (SocketTimeoutException e)
		{
			String alarmMessage = e.getMessage();
			if (alarmMessage == null)
				alarmMessage = "Not able to read the response";

			sendNotification(httpMethod, httpPerformanceMonitor,
					Severity.WARNING, alarmMessage, circleId);
			throw e;
		}
		finally
		{
			try
			{
				httpMethod.releaseConnection();
			}
			catch (Exception e)
			{
			}
		}

		HttpResponse httpResponse = new HttpResponse(responseCode, response,
				responseHeaders, responseTime);
		return httpResponse;
	}

	private static String getDownloadableFileName(HttpMethod httpMethod)
	{
		String fileName = null;
		try
		{
			Header contentDispositionHeader = httpMethod
					.getResponseHeader("Content-Disposition");
			if (contentDispositionHeader != null)
			{
				String headerValue = contentDispositionHeader.getValue().trim();
				fileName = headerValue.substring(headerValue.indexOf("=") + 1,
						headerValue.lastIndexOf(';'));
			}
			else
			{
				URI uri = httpMethod.getURI();
				String uriName = uri.getURI();
				int startIndex = uriName.lastIndexOf('/') + 1;
				int endIndex = uriName.indexOf('?');
				if (endIndex == -1)
					endIndex = uriName.length();

				fileName = uriName.substring(startIndex, endIndex);
				if (!fileName.toLowerCase().endsWith(".wav"))
					fileName = null;
			}

		}
		catch (Exception e)
		{
		}
		return fileName;
	}

	private static void addRequestParams(String url,
			Map<String, String> requestParams, GetMethod getMethod)
			throws URIException
	{
		String uri = url;
		if (url.contains("?"))
		{
			uri = url.substring(0, url.indexOf('?'));
			String queryString = url.substring(url.indexOf('?') + 1);
			addQueryStringToRequestParams(queryString, requestParams);
		}
		getMethod.setURI(new URI(uri, true));

		if (requestParams != null)
		{
			List<NameValuePair> list = new ArrayList<NameValuePair>();

			Set<Entry<String, String>> entrySet = requestParams.entrySet();
			for (Entry<String, String> entry : entrySet)
			{
				list.add(new NameValuePair(entry.getKey(), entry.getValue()));
			}

			getMethod.setQueryString(list.toArray(new NameValuePair[0]));
		}
	}

	private static void addRequestParams(String url,
			Map<String, String> requestParams, Map<String, File> fileParams,
			PostMethod postMethod) throws FileNotFoundException, URIException
	{
		String uri = url;
		if (url.contains("?"))
		{
			uri = url.substring(0, url.indexOf('?'));
			String queryString = url.substring(url.indexOf('?') + 1);
			addQueryStringToRequestParams(queryString, requestParams);
		}
		//RBT-15204 Added for url encoding
		postMethod.getParams().setContentCharset("UTF-8");
		postMethod.setURI(new URI(uri, true));

		Part[] parts = getParts(requestParams, fileParams);
		if (parts != null)
			postMethod.setRequestEntity(new MultipartRequestEntity(parts,
					postMethod.getParams()));
		else if (requestParams != null)
		{
			Set<Entry<String, String>> entrySet = requestParams.entrySet();
			for (Entry<String, String> entry : entrySet)
			{
				if(entry.getKey() != null && entry.getValue() != null)
					postMethod.addParameter(entry.getKey(), entry.getValue());
			}
		}
	}

	private static Part[] getParts(Map<String, String> requestParams,
			Map<String, File> fileParams) throws FileNotFoundException
	{
		Part[] parts = null;

		if (fileParams != null && fileParams.size() > 0)
		{
			int partLength = fileParams.size();
			if (requestParams != null)
				partLength += requestParams.size();

			if (partLength == 0)
				return null;

			parts = new Part[partLength];

			int partsIndex = 0;
			if (requestParams != null)
			{
				Set<Entry<String, String>> entrySet = requestParams.entrySet();
				for (Entry<String, String> entry : entrySet)
				{
					parts[partsIndex++] = new StringPart(entry.getKey(), entry
							.getValue());
				}
			}

			Set<Entry<String, File>> entrySet = fileParams.entrySet();
			for (Entry<String, File> entry : entrySet)
			{
				parts[partsIndex++] = new FilePart(entry.getKey(), entry
						.getValue());
			}
		}

		return parts;
	}

	private static void addQueryStringToRequestParams(String queryString,
			Map<String, String> requestParams)
	{
		if (queryString != null && queryString.length() > 0)
		{
			String[] queryStringTokens = queryString.split("&");
			for (String queryStringToken : queryStringTokens)
			{
				String param = null;
				String value = null;

				int index = queryStringToken.indexOf('=');
				if (index != -1)
				{
					param = queryStringToken.substring(0, index);

					index++;
					if (index < queryStringToken.length())
						value = queryStringToken.substring(index);
				}
				else
					param = queryStringToken;
				
				value = (value != null ? (value.replaceAll("%26", "&")) : value);

				if (!requestParams.containsKey(param))
					requestParams.put(param, value);
			}
		}
	}

	private void sendNotification(HttpMethod httpMethod,
			HttpPerformanceMonitor httpPerformanceMonitor, Severity severity,
			String alarmMessage, String circleId) throws URIException
	{
		if (httpPerformanceMonitor != null)
		{
			URI uri = httpMethod.getURI();
			String httpUrl = uri.getURI();

			if(circleId != null) {
				synchronized (object) {
					if(severity == Severity.CLEAR) {
						circleIDSet.remove(circleId);
						if(circleIDSet.size() != 0) {
							severity = Severity.INFO;
						}
					}
					else {
						circleIDSet.add(circleId);
					}
					String message = "";
					for(String strCircleId : circleIDSet) {
						message = message + strCircleId + ", ";
					}
					if(message.length() > 0) {
						message = message.substring(0, message.length() -1) + " circleids links are not up. ";
					}
					alarmMessage =  message + alarmMessage; 
				}
			}
			
			PerformanceContext performanceContext = new PerformanceContext(
					severity, alarmMessage, httpUrl);

			if (severity == Severity.CLEAR)
				httpPerformanceMonitor.clearAlarm(performanceContext);
			else
				httpPerformanceMonitor.raiseAlarm(performanceContext);
		}
	}
	
	
}
