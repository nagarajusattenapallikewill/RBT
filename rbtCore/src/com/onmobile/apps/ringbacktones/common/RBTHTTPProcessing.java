package com.onmobile.apps.ringbacktones.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;

import com.onmobile.apps.ringbacktones.common.Tools;

public class RBTHTTPProcessing implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(RBTHTTPProcessing.class);
	
//	private static final String HTTP_ERROR = "HTTP_ERROR";
//	private static final String MALFORMED_URL_ERROR = "MALFORMED_URL_ERROR";
//	private static final String NO_ROUTE_TO_HOST_ERROR = "NO_ROUTE_TO_HOST_ERROR";
//	private static final String PROTOCOL_ERROR = "PROTOCOL_ERROR";
//	private static final String SOCKET_ERROR = "SOCKET_ERROR";
//	private static final String UNKNOWN_HOST_ERROR = "UNKNOWN_HOST_ERROR";
//	private static final String UNKNOWN_SERVICE_ERROR = "UNKNOWN_SERVICE_ERROR";
//	private static final String IO_ERROR = "IO_ERROR";
	
	private static int timeout = (1000)*3;//3 seconds..
	private static int dataTimeout = (1000)*10;//10 seconds..
	
	HostConfiguration hcfg = null;
	HttpClient client = null;
	HttpConnection connection = null;
	HttpConnectionManager connectionManager = null;

	private RBTHTTPProcessing()
	{

	}
	
	public static synchronized RBTHTTPProcessing getInstance()
	{
		RBTHTTPProcessing obj = new RBTHTTPProcessing();
		
		timeout = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "HTTP_CONNECTION_TIME_OUT", 3);
		timeout = timeout * 1000;
		
		dataTimeout = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "HTTP_TIME_OUT", 10);
		dataTimeout = dataTimeout*1000;
		return obj;
	}

	/**
	 * Used by RBTPlayer update daemon
	 * @param connTimeOut
	 * @param dataTimeOut
	 * @return
	 */
	public static synchronized RBTHTTPProcessing getInstance(int connTimeOut, int dataTimeOut)
	{
		RBTHTTPProcessing obj = new RBTHTTPProcessing();
		timeout = connTimeOut*1000;
		dataTimeout = dataTimeOut*1000;
		return obj;
	}
    
	public String makeRequest1(String urlStr, String subId, String app)
	{
		return makePostRequest(urlStr, null);
	}
	
	public String makePostRequest(String urlStr, HashMap<String, String> requestParams)
	{
		String response = "Notok";
		PostMethod post = null;
		try
		{
			if(!init(urlStr)) {
				logger.info("RBT:: init if http params failed returning null");
				return null;
			}
			post = new PostMethod(urlStr);
			//adding parameters from hashmap
			if(requestParams != null && requestParams.size() > 0)
			{
				Set<String> allReqParams = requestParams.keySet();
				Iterator<String> it = allReqParams.iterator();
				while(it.hasNext()) {
					String thisParam = it.next();
					post.addParameter(thisParam, requestParams.get(thisParam));
				}
			}

			int statusCode = client.executeMethod(hcfg, post);
			logger.info("RBT::Response Code : " + statusCode);
			response = post.getResponseBodyAsString();
			if(response != null)
				response = response.trim();
			logger.info("RBT::Response : " + response);
		}
        catch (MalformedURLException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (NoRouteToHostException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (ProtocolException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (SocketException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (UnknownHostException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (UnknownServiceException e)
        {
        	logger.error("", e);
			return null;
        }
        catch (HttpException e)
        {
        	logger.error("", e);
			return null;
		}
        catch (IOException e)
		{
        	logger.error("", e);
			return null;
		}
		finally
		{
			if(post!=null)
				post.releaseConnection();
			//releasing connection using SimpleHttpConnectionManager
			connectionManager.releaseConnection(connection);
			//checking for connection and closing it, if it is still live
			if(connection.isOpen())
				connection.close();
		}
		return response;
	}
	
	private boolean init(String urlStr) throws MalformedURLException {
		URL url = new URL(urlStr);
		hcfg = new HostConfiguration();
		hcfg.setHost(url.getHost(), url.getPort());
		
		connectionManager = new SimpleHttpConnectionManager();
		connection = connectionManager.getConnection(hcfg);
		
		HttpConnectionParams connParams = connection.getParams();
		connParams.setConnectionTimeout(timeout);
		connParams.setSoTimeout(dataTimeout);

		client = new HttpClient(connectionManager);
        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

		return true;
	}
	
	public static String postFile(HttpParameters httpParameters, HashMap<String, String> params, File[] files)
			throws HttpException, IOException, RBTException, Exception {
		return postFile(httpParameters, params, files, false);
	}
	

	
    /**
     * API for Uploading files over HTTP.
     * HTTPS support not there now. If needed add boolean value to HttpParameters 
     * and do code change here. The server page accepting this request should be of type ENCTYPE="multipart/form-data"
     * @param httpParameters The srever details to connect to.
     * @param params Key Value pair of String params. Other objects not supported.
     * @param files The files to upload to server.
     * @author geo
     * @throws HttpException,IOException,Exception
     */
	public static String postFile(HttpParameters httpParameters, HashMap<String, String> params, File[] files,
			boolean appendStatus)
    	throws HttpException,IOException, Exception {
		PostMethod mPost = null;
		byte[] response = null;
		int statusCode = -1;
		String paramStr = null;
		try{
			logger.info("In post file for URL -> " + httpParameters.getUrl());
			validateParams(params, files);
			Part[] parts = getParts(httpParameters, params, files);
			if(parts == null)
				paramStr = addParams(params);
			
			String urlStr = httpParameters.getUrl();
			if(paramStr != null)
				urlStr = urlStr + paramStr;
			
			urlStr = urlStr.replaceAll(" ", "%20");
			urlStr = Tools.decodeHTML(urlStr);
			URL url = new URL(urlStr);
			mPost = new PostMethod(urlStr);
			
			if(parts != null)
				mPost.setRequestEntity(new MultipartRequestEntity(parts, mPost.getParams()));
			
			HostConfiguration hcfg = new HostConfiguration();
			hcfg.setHost(url.getHost(), url.getPort());
			
			HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
			HttpConnection connection = connectionManager.getConnection(hcfg);
			
			HttpConnectionParams connParams = connection.getParams();
			connParams.setConnectionTimeout(httpParameters.getConnectionTimeout());
			connParams.setSoTimeout(httpParameters.getDataTimeout());
			
			if(httpParameters.getHasProxy())
				hcfg.setProxy(httpParameters.getProxyHost(),httpParameters.getProxyPort());
			
			HttpClient client = new HttpClient(connectionManager);
			DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
	        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
			
	        statusCode = client.executeMethod(hcfg,mPost);
			
	        logger.info("RBT::status - " + statusCode);
			response = mPost.getResponseBody();
			logger.info("Uploaded status="+statusCode);
			
		}catch (HttpException e){
			logger.error("", e);
			throw e;
		}catch (IOException e){
			logger.error("", e);
			throw  e;
		}catch (Throwable e){
			logger.error("", e);
		}
		finally{
			releaseConnection(mPost);
		}
		StringBuffer retString = new StringBuffer(new String(response));
		if(appendStatus && statusCode != -1)
			retString.insert(0, statusCode + "|");// = statusCode + "|" + retString;
		if (response!=null)
			return retString.toString().trim();
		else
			return null;
	}
	
	private static String addParams(HashMap<String, String> params) {
		if(params != null) {
			logger.info("param!=null && parts==null");
			Set<String> keySet = params.keySet();
			Iterator<String> itr = keySet.iterator();
			StringBuffer sb = new StringBuffer();
			while(itr.hasNext()) {
				String key = itr.next();
				String value = params.get(key);
				logger.info("param key=="+key);
				logger.info("param value=="+value);
				sb.append(key + "=" + value + "&");
//				mPost.addParameter(key,(String)params.get(key));
			}
			return sb.toString();
		}
		return null;
//		return mPost;
	}
	
	private static void validateParams(HashMap<String, String> map, File[] files) throws RBTException {
		if(map == null && files == null){
			logger.info("map == null && files == null");
			throw new RBTException("Post something dude....");
		}
	}

	private static void releaseConnection( PostMethod mPost){
		if(mPost != null){
			try{
				mPost.releaseConnection();
			}catch(Throwable thr){
			}
		}
	}

    private  static Part[] getParts(HttpParameters httpParams, HashMap<String, String> params,
    		File [] files) throws FileNotFoundException {
    	Part[] parts = null;
    	boolean check=true;
    	if(files != null || httpParams.getParamsAsParts()) {
    		logger.info("files != null || httpParams.getParamsAsParts()");
    		check=false;
	        int iCounter = 0;
	        int iPartLength = 0;
	        
	        if(files != null)
	        	iPartLength += files.length;
	        
	        if(params != null)
	        	iPartLength += params.size();
	        
	        if(iPartLength == 0)
	        	return null;
	        
	        parts = new Part[iPartLength];
	        
			if(params != null) {
				Set<String> keySet = params.keySet();
				Iterator<String> itr = keySet.iterator();
				while(itr.hasNext()) {
					String key = itr.next();
					parts [iCounter++] = new StringPart(key, params.get(key));
					System.out.println("added(as part) param - " + key + " value is " + params.get(key));
				}
			}
	        
	        for(int i=0;files != null && i < files.length;i++){
	        	logger.info("RBT::attaching file -> " + files[i].getAbsolutePath());
	            parts [iCounter++] = new FilePart(files[i].getName(), files[i]);
	            System.out.println("added file " + files[i].getName());
	        }
    	}
		if (check) {
			logger.info("files == null && !httpParams.getParamsAsParts()");
		}    	
		return parts;
    }

		public static String doGetRequest(HttpParameters httpParameters, HashMap<String, String> params) {
		int statusCode = -1;
		String response = null;
		GetMethod mGet = null;
		try {
			String urlStr = httpParameters.getUrl();
			if(params != null && params.size() > 0) {
				StringBuffer sb = new StringBuffer();
				Iterator<String> itr = params.keySet().iterator();
				while(itr.hasNext()) {
					String param = itr.next();
					sb.append(param + "=" +  params.get(param) + "&");
				}
				urlStr = urlStr + sb.toString();
			}
			urlStr = urlStr.replaceAll(" ", "%20");
			urlStr = Tools.decodeHTML(urlStr);
			logger.info("RBT::doing GET for " + urlStr);
			HostConfiguration hcfg = new HostConfiguration();
			mGet = new GetMethod(urlStr);
			URL url = new URL(urlStr);
			hcfg.setHost(url.getHost(), url.getPort());
			
			HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
			HttpConnection connection = connectionManager.getConnection(hcfg);
			
			HttpConnectionParams connParams = connection.getParams();
			connParams.setConnectionTimeout(httpParameters.getConnectionTimeout());
			connParams.setSoTimeout(httpParameters.getDataTimeout());
			
			if(httpParameters.getHasProxy())
				hcfg.setProxy(httpParameters.getProxyHost(),httpParameters.getProxyPort());
			
			HttpClient client = new HttpClient(connectionManager);
	        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
	        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
	        
	        statusCode = client.executeMethod(hcfg,mGet);
			
	        logger.info("RBT::status - " + statusCode);
			response = mGet.getResponseBodyAsString();
//			System.out.println("Uploaded status="+statusCode + ", response=" + response);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConnection(mGet);
		}
		return response;
	}

	private static void releaseConnection(GetMethod mGet) {
		if (mGet != null) {
			try {
				mGet.releaseConnection();
			}
			catch (Throwable thr) {
			}
		}
	}

    public static void main  (String args[]) throws Exception {
        HttpParameters http = new HttpParameters();
        HashMap<String, String>  map = new HashMap<String, String>();
        map.put("hello","test");
        map.put("test","hello");
        File [] f = new File [2];
        f[0] = new File ("E:\\mcom\\test.txt");
        f[1] = new File ("E:\\RBT\\master\\apps\\rbt2\\lib\\wrapper.jar");
        postFile (http, map, f);
    }
}