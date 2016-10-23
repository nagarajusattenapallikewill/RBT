package com.onmobile.android.configuration;

import org.apache.log4j.Logger;

import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;

public class HttpConfigurations {

	private static int httpConnectionTimeout = 6000;
	private static int httpSoTimeout = 6000;
	private static boolean useProxy = false;
	private static String proxyHost = null;
	private static int proxyPort;
	private static int maxTotalHttpConnections = 20;
	private static int maxHostHttpConnections = 2;
	private static String protocol = "http";

	private static Logger logger = Logger.getLogger(HttpConfigurations.class);

	static {
		initConfigurations();
	}

	private static void initConfigurations() {
		logger.info("Initializing http configurations");

		String httpConnectionTimeoutStr = getValueFromParameterMap("HTTP_CONNECTION_TIMEOUT");
		if (httpConnectionTimeoutStr != null)
			httpConnectionTimeout = Integer.parseInt(httpConnectionTimeoutStr);

		String httpSoTimeoutStr = getValueFromParameterMap("HTTP_SOCKET_TIMEOUT");
		if (httpSoTimeoutStr != null)
			httpSoTimeout = Integer.parseInt(httpSoTimeoutStr);

		String useProxyStr = getValueFromParameterMap("USE_PROXY");
		if (useProxyStr != null)
			useProxy = useProxyStr.equalsIgnoreCase("TRUE");
		if (useProxy)
		{
			proxyHost = getValueFromParameterMap("PROXY_HOST");
			proxyPort = Integer.parseInt(getValueFromParameterMap("PROXY_PORT"));
		}

		String maxTotalHttpConnectionsStr = getValueFromParameterMap("MAX_TOTAL_HTTP_CONNECTIONS");
		if (maxTotalHttpConnectionsStr != null)
			maxTotalHttpConnections = Integer.parseInt(maxTotalHttpConnectionsStr);

		String maxHostHttpConnectionsStr = getValueFromParameterMap("MAX_HOST_HTTP_CONNCETIONS");
		if (maxHostHttpConnectionsStr != null)
			maxHostHttpConnections = Integer.parseInt(maxHostHttpConnectionsStr);


		String rbtClientProtocol = getValueFromParameterMap("PROTOCOL");
		if (rbtClientProtocol != null) {
			protocol = rbtClientProtocol.toLowerCase();
		}
		logger.info("httpConnectionTimeout: " + httpConnectionTimeout
				+ ", httpSoTimeout: " + httpSoTimeout + ", useProxy: "
				+ useProxy + ", proxyHost: " + proxyHost + ", proxyPort: "
				+ proxyPort + ", maxTotalHttpConnections: "
				+ maxTotalHttpConnections + ", maxHostHttpConnections: "
				+ maxHostHttpConnections + ", protocol: " + protocol);
	}

	private static String getValueFromParameterMap(String propertyName) {
		Parameter param = PropertyConfigurator.paramMap.get(propertyName);
		if (param != null && Utility.isStringValid(param.getValue())) {
			return param.getValue();
		}
		return null;
	}

	/**
	 * @return the httpConnectionTimeout
	 */
	public static int getHttpConnectionTimeout()
	{
		return httpConnectionTimeout;
	}

	/**
	 * @return the httpSoTimeout
	 */
	public static int getHttpSoTimeout()
	{
		return httpSoTimeout;
	}

	/**
	 * @return the useProxy
	 */
	public static boolean isUseProxy()
	{
		return useProxy;
	}

	/**
	 * @return the proxyHost
	 */
	public static String getProxyHost()
	{
		return proxyHost;
	}

	/**
	 * @return the proxyPort
	 */
	public static int getProxyPort()
	{
		return proxyPort;
	}

	/**
	 * @return the maxTotalHttpConnections
	 */
	public static int getMaxTotalHttpConnections()
	{
		return maxTotalHttpConnections;
	}

	/**
	 * @return the maxHostHttpConnections
	 */
	public static int getMaxHostHttpConnections()
	{
		return maxHostHttpConnections;
	}

	/**
	 * @return the protocol
	 */
	public static String getProtocol()
	{
		return protocol;
	}
}
