package com.onmobile.apps.ringbacktones.rbtcontents.v2.utils.implementation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbt2.response.IResponseHandler;
import com.onmobile.apps.ringbacktones.rbt2.response.ResponseHandlerFactory;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.v2.utils.ITPHitUtils;

public class MultipleTPHitUtilsImpl implements ITPHitUtils {
	private static Logger logger = Logger.getLogger(MultipleTPHitUtilsImpl.class);
	private static String xml_ = "xml";
	private static String circle_ = "circle";
	private static String operator_ = "operator";

	@Override
	public String hitUrl(String xml, String operator) {
		HttpClient client = null;
		GetMethod getMethod = null;
		ByteArrayOutputStream bos = null;
		// HostConfiguration config = new HostConfiguration();
		int statuscode;
		InputStream in = null;

		String urls = RBTContentJarParameters.getInstance().getParameter("HTTP_URL_"+operator.toUpperCase());
		String connectionTimeOut = RBTContentJarParameters.getInstance().getParameter("CONNECTION_TIMEOUT");
		String soTimeOut = RBTContentJarParameters.getInstance().getParameter("SO_TIMEOUT");
		String totalConnections = RBTContentJarParameters.getInstance().getParameter("TOTAL_CONNECTION");
		String maxHostConnection = RBTContentJarParameters.getInstance().getParameter("MAX_HOST_CONNECTION");
		String proxyHost = RBTContentJarParameters.getInstance().getParameter("PROXY_HOST");
		String proxyPort = RBTContentJarParameters.getInstance().getParameter("PROXY_PORT");

		SimpleHttpConnectionManager multiThreadedHttpConnectionManager = new SimpleHttpConnectionManager();

		HttpConnectionManagerParams httpConnectionManagerParams = multiThreadedHttpConnectionManager.getParams();
		httpConnectionManagerParams.setConnectionTimeout(getParameterASInt(connectionTimeOut));
		httpConnectionManagerParams.setSoTimeout(getParameterASInt(soTimeOut));
		httpConnectionManagerParams.setMaxTotalConnections(getParameterASInt(totalConnections));
		httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(getParameterASInt(maxHostConnection));

		client = new HttpClient(multiThreadedHttpConnectionManager);
		DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(0, false);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, defaultHttpMethodRetryHandler);

		String[] arrUrl = urls.split("\\,");

		for (int j = 0; j < arrUrl.length; j++) {
			String url = arrUrl[j].trim();
			getMethod = new GetMethod(url);

			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new NameValuePair(xml_, xml));
			list.add(new NameValuePair(circle_, "ALL"));
			list.add(new NameValuePair(operator_,operator ));
			getMethod.setQueryString(list.toArray(new NameValuePair[0]));

			URL httpurl = null;
			try {
				httpurl = new URL(url);
				HostConfiguration hostConfiguration = client.getHostConfiguration();
				hostConfiguration.setHost(httpurl.getHost(), httpurl.getPort());
				if (null != proxyHost && !(proxyHost = proxyHost.trim()).equals("") && null != proxyPort
						&& !(proxyPort = proxyPort.trim()).equals("")) {
					hostConfiguration.setProxy(proxyHost, Integer.parseInt(proxyPort));
				}
				statuscode = client.executeMethod(getMethod);
				if (statuscode == 200) {
					bos = new ByteArrayOutputStream();
					in = getMethod.getResponseBodyAsStream();
					byte[] bytes = new byte[2024];
					int size = 2024;
					int i = 0;
					while ((i = in.read(bytes, 0, size)) != -1) {
						bos.write(bytes, 0, i);
					}
					byte[] response = bos.toByteArray();
					if (response != null) {
						String responseString = new String(response).trim();
						ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();
						IResponseHandler responseHandler = null; 
						String resHandler = RBTContentJarParameters.getInstance().getParameter("tp_response_handler");
						if (responseHandlerFactory != null && resHandler != null) {
							responseHandler = responseHandlerFactory.getResponseHandler(resHandler);
						}
						if (responseHandler != null) {
							responseString = responseHandler.processResponse(responseString);
						}
						if (null != responseString && responseString.equalsIgnoreCase("SUCCESS")) {
							logger.info("We are getting successfull response from Tone Player for Operator:" + operator
									+ ", [Response : " + responseString + " ] Url : " + url);
						} else {
							logger.info("Not getting successfull response from Tone Player for Operator:" + operator
									+ ", [Response : " + responseString + " ] Url : " + url);
						}
					} else {
						logger.info("We are getting no response from Tone Player for Operator:"+ operator+", Url : " + url);
					}
				} else {
					logger.info("Failure for Operator:"+ operator+", Url : " + url + " Response code " + statuscode);
					return null;
				}
			} catch (HttpException e) {
				logger.error("Http Failure : URL " + url, e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("IO Failue : URL " + url, e);
				e.printStackTrace();
			} finally {
				if (null != getMethod) {
					getMethod.releaseConnection();
				}
			}

		}
		return null;
	}

	private int getParameterASInt(String parameter) {
		try {
			return Integer.parseInt(parameter);
		} catch (Exception e) {
			return 0;
		}
	}

}
