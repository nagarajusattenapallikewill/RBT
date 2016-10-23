package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;

/**
 * @author vasipalli.sreenadh
 * @author laxmankumar - separate configurations for bulk upload
 *
 */
public class Configurations 
{
	private ResourceBundle resourceBundle;

	private boolean useMNPService = false;
	private Map<String, List<String>> circlePrefixMap = null;
	private Map<String, String> circleIpMap = null;
	
	//RBT_AT-103588
	private Set<String> cvCircleId = null;
	private String centralCircle = null;

	private String connectorClass = null;
	private String countryCodePrefix = "91";
	private int minPhoneNumberLength = 10;

	private int httpConnectionTimeout = 6000;
	private int httpSoTimeout = 6000;
	private boolean useProxy = false;
	private String proxyHost = null;
	private int proxyPort;
	private int maxTotalHttpConnections = 20;
	private int maxHostHttpConnections = 2;

	private int httpSoTimeoutForBulk = 6000;
	private int maxTotalHttpConnectionsForBulk = 20;
	private int maxHostHttpConnectionsForBulk = 2;

	private String protocol = "http";
	private boolean overwriteDownload = false;
	private Map<String,List<String>> comvivaApiMap = null;
	private boolean useDirectConnectorCV = false;
	private Logger logger = null;
	

	/**
	 * 
	 */
	public Configurations()
	{
		this(null);
	}

	

	/**
	 * @param logger
	 */
	public Configurations(Logger logger)
	{
		this.logger = logger;
		if (this.logger == null)
			this.logger = Logger.getLogger(RBTClient.class);

		resourceBundle = ResourceBundle.getBundle("rbtclient");

		circlePrefixMap = new HashMap<String, List<String>>();
		circleIpMap = new HashMap<String, String>();

		initilizeCirclePrfixMap();
		initilizeIPPortMap();
		//RBT_AT-103588
		initilizeCVCircle();
		initConfigParameters();
		
	}
	
	
	//RBT_AT-103588
	private void initilizeCVCircle()
	{
		String circleIDs = getValueFromResourceBundle("CV_CIRCLE_ID");
		if (circleIDs != null && !circleIDs.isEmpty())
		{
			cvCircleId = new HashSet<String>();
			String[] circleIDArr = circleIDs.split(",");
			for (String circleId : circleIDArr)
			{
				cvCircleId.add(circleId);
			}
		}
	}
	

	private void initilizeCirclePrfixMap()
	{
		centralCircle = getValueFromResourceBundle("CENTRAL_CIRCLE");

		String circleList = getValueFromResourceBundle("CIRCLES");
		if (circleList != null)
		{
			String[] circles = circleList.split(",");
			for (String circleID : circles)
			{
				String prefixes = getValueFromResourceBundle(circleID);
				if (prefixes != null)
				{
					List<String> prefixList = Arrays.asList(prefixes.split(","));
					circlePrefixMap.put(circleID, prefixList);
				}
			}
		}
	}

	private void initilizeIPPortMap()
	{
		String circleIP = getValueFromResourceBundle("CIRCLE_IP_MAP");
		if (circleIP != null)
		{
			String[] circleIPs = circleIP.split(",");
			for (String circleIPStr : circleIPs)
			{
				String circleID = circleIPStr.substring(0,circleIPStr.indexOf(':'));
				String ipPort = circleIPStr.substring(circleIPStr.indexOf(':') + 1);
				circleIpMap.put(circleID, ipPort);
			}
		}
	}

	private void initConfigParameters()
	{
		try
		{
			String useMNPServiceStr = getValueFromResourceBundle("USE_MNP_SERVICE");
			if (useMNPServiceStr != null)
				useMNPService = useMNPServiceStr.equalsIgnoreCase("TRUE");

			connectorClass = getValueFromResourceBundle("CONNECTOR_CLASS");
			
			String countryCodePrefixTemp = getValueFromResourceBundle("COUNTRY_CODE_PREFIX");
			if (countryCodePrefixTemp != null)
				countryCodePrefix = countryCodePrefixTemp;
			String minPhoneNumberLengthStr = getValueFromResourceBundle("MIN_PHONE_NUMBER_LENGTH");
			if (minPhoneNumberLengthStr != null)
				minPhoneNumberLength = Integer.parseInt(minPhoneNumberLengthStr);

			String httpConnectionTimeoutStr = getValueFromResourceBundle("HTTP_CONNECTION_TIMEOUT");
			if (httpConnectionTimeoutStr != null)
				httpConnectionTimeout = Integer.parseInt(httpConnectionTimeoutStr);

			String httpSoTimeoutStr = getValueFromResourceBundle("HTTP_SOCKET_TIMEOUT");
			if (httpSoTimeoutStr != null)
				httpSoTimeout = Integer.parseInt(httpSoTimeoutStr);

			String useProxyStr = getValueFromResourceBundle("USE_PROXY");
			if (useProxyStr != null)
				useProxy = useProxyStr.equalsIgnoreCase("TRUE");
			if (useProxy)
			{
				proxyHost = getValueFromResourceBundle("PROXY_HOST");
				proxyPort = Integer.parseInt(getValueFromResourceBundle("PROXY_PORT"));
			}

			String maxTotalHttpConnectionsStr = getValueFromResourceBundle("MAX_TOTAL_HTTP_CONNECTIONS");
			if (maxTotalHttpConnectionsStr != null)
				maxTotalHttpConnections = Integer.parseInt(maxTotalHttpConnectionsStr);

			String maxHostHttpConnectionsStr = getValueFromResourceBundle("MAX_HOST_HTTP_CONNCETIONS");
			if (maxHostHttpConnectionsStr != null)
				maxHostHttpConnections = Integer.parseInt(maxHostHttpConnectionsStr);

			String httpSoTimeoutForBulkStr = getValueFromResourceBundle("HTTP_SOCKET_TIMEOUT_FOR_BULK");
			if (httpSoTimeoutForBulkStr != null) {
				httpSoTimeoutForBulk = Integer.parseInt(httpSoTimeoutForBulkStr);
			}
			
			String maxTotalConnsForBulkStr = getValueFromResourceBundle("MAX_TOTAL_HTTP_CONNECTIONS_FOR_BULK");
			if (maxTotalConnsForBulkStr != null) {
				maxTotalHttpConnectionsForBulk = Integer.parseInt(maxTotalConnsForBulkStr);
			}

			String maxHostConnsForBulkStr = getValueFromResourceBundle("MAX_HOST_HTTP_CONNCETIONS_FOR_BULK");
			if (maxHostConnsForBulkStr != null) {
				maxHostHttpConnectionsForBulk = Integer.parseInt(maxHostConnsForBulkStr);
			}

			String rbtClientProtocol = getValueFromResourceBundle("PROTOCOL");
			if (rbtClientProtocol != null) {
				protocol = rbtClientProtocol.toLowerCase();
			}
			
			
			String overwriteStr = getValueFromResourceBundle("OVERWRITE_DOWNLOAD");
			overwriteDownload = (overwriteStr != null && overwriteStr.equalsIgnoreCase("TRUE"));
			
			String rbtClientCVApi = getValueFromResourceBundle("COMVIVA_API_MAP_DETAILS");
			
			if (rbtClientCVApi != null) {
				comvivaApiMap = MapUtils.convertMapList(rbtClientCVApi,";", ":", ",");
			}
			
			String rbtClientUseDirectConnectorCV = getValueFromResourceBundle("USE_DIRECT_CONNECTOR_CV");
			useDirectConnectorCV = (rbtClientUseDirectConnectorCV != null && rbtClientUseDirectConnectorCV.equalsIgnoreCase("TRUE"));
			
		}
		catch (Exception e)
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}
	}

	public String getValueFromResourceBundle(String key)
	{
		String value = null;

		try
		{
			value = resourceBundle.getString(key).trim();
		}
		catch(MissingResourceException e)
		{
			logger.info("RBT:: " + e.getMessage());
		}

		return value;
	}

	public String getIPAddressNPort(String circleID)
	{
		if (circleID == null)
			circleID = centralCircle;

		String ipAddressNPort = null;
		if (circleIpMap.containsKey(circleID))
			ipAddressNPort = circleIpMap.get(circleID);
		else
			ipAddressNPort = circleIpMap.get("ALL");

		return ipAddressNPort;
	}

	public String getCircle(String subscriberID)
	{
		String subID = trimCountryPrefix(subscriberID);

		Set<String> set = circlePrefixMap.keySet();
		for (String key : set)
		{
			List<String> prefixList = circlePrefixMap.get(key);
			for (String prefix : prefixList)
			{
				if (prefix.equalsIgnoreCase("ALL") || (subID != null && subID.startsWith(prefix)))
					return key;
			}
		}

		return null;
	}
	
	/**
	 * @return the centralCircle
	 */
	public String getCentralCircle()
	{
		return centralCircle;
	}

	/**
	 * @return the useMNPService
	 */
	public boolean isUseMNPService()
	{
		return useMNPService;
	}

	/**
	 * @return the connectorClass
	 */
	public String getConnectorClass()
	{
		return connectorClass;
	}

	/**
	 * @return the minPhoneNumberLength
	 */
	public int getMinPhoneNumberLength()
	{
		return minPhoneNumberLength;
	}

	/**
	 * @return the countryCodePrefix
	 */
	public String getCountryCodePrefix()
	{
		return countryCodePrefix;
	}

	/**
	 * @return the httpConnectionTimeout
	 */
	public int getHttpConnectionTimeout()
	{
		return httpConnectionTimeout;
	}

	/**
	 * @return the httpSoTimeout
	 */
	public int getHttpSoTimeout()
	{
		return httpSoTimeout;
	}

	/**
	 * @return the useProxy
	 */
	public boolean isUseProxy()
	{
		return useProxy;
	}

	/**
	 * @return the proxyHost
	 */
	public String getProxyHost()
	{
		return proxyHost;
	}

	/**
	 * @return the proxyPort
	 */
	public int getProxyPort()
	{
		return proxyPort;
	}

	/**
	 * @return the maxTotalHttpConnections
	 */
	public int getMaxTotalHttpConnections()
	{
		return maxTotalHttpConnections;
	}

	/**
	 * @return the maxHostHttpConnections
	 */
	public int getMaxHostHttpConnections()
	{
		return maxHostHttpConnections;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * @return the isOverwriteDownload
	 */
	public boolean isOverwriteDownload()
	{
		return overwriteDownload;
	}

	/**
	 * @param overwriteDownload the overwriteDownload to set 
	 */
	public void setOverwriteDownload(boolean overwriteDownload) {
		this.overwriteDownload = overwriteDownload;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger()
	{
		return logger;
	}

	/**
	 * @param countryCodePrefix the countryCodePrefix to set
	 */
	public void setCountryCodePrefix(String countryCodePrefix)
	{
		this.countryCodePrefix = countryCodePrefix;
	}

	/**
	 * @param minPhoneNumberLength the minPhoneNumberLength to set
	 */
	public void setMinPhoneNumberLength(int minPhoneNumberLength)
	{
		this.minPhoneNumberLength = minPhoneNumberLength;
	}

	/**
	 * @param httpConnectionTimeout the httpConnectionTimeout to set
	 */
	public void setHttpConnectionTimeout(int httpConnectionTimeout)
	{
		this.httpConnectionTimeout = httpConnectionTimeout;
	}

	/**
	 * @param httpSoTimeout the httpSoTimeout to set
	 */
	public void setHttpSoTimeout(int httpSoTimeout)
	{
		this.httpSoTimeout = httpSoTimeout;
	}

	/**
	 * @param useProxy the useProxy to set
	 */
	public void setUseProxy(boolean useProxy)
	{
		this.useProxy = useProxy;
	}

	/**
	 * @param proxyHost the proxyHost to set
	 */
	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(int proxyPort)
	{
		this.proxyPort = proxyPort;
	}

	/**
	 * @param maxTotalHttpConnections the maxTotalHttpConnections to set
	 */
	public void setMaxTotalHttpConnections(int maxTotalHttpConnections)
	{
		this.maxTotalHttpConnections = maxTotalHttpConnections;
	}

	/**
	 * @param maxHostHttpConnections the maxHostHttpConnections to set
	 */
	public void setMaxHostHttpConnections(int maxHostHttpConnections)
	{
		this.maxHostHttpConnections = maxHostHttpConnections;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}
	
	
	


	public String trimCountryPrefix(String subscriberID)
	{
		if (subscriberID != null)
		{
			try
			{
				if (countryCodePrefix != null)
				{
					String[] countryCodePrefixes = countryCodePrefix.split(",");
					for (String prefix : countryCodePrefixes)
					{
						if (subscriberID.startsWith("00"))
							subscriberID = subscriberID.substring(2);
						if (subscriberID.startsWith("+")
								|| subscriberID.startsWith("-")
								|| subscriberID.startsWith("0"))
							subscriberID = subscriberID.substring(1);

						if (subscriberID.startsWith(prefix) && (subscriberID.length() >= (minPhoneNumberLength + prefix.length())))
						{
							subscriberID = subscriberID.substring(prefix.length());
							break;
						}
					}
				}
			}
			finally
			{
				if (subscriberID.startsWith("00"))
					subscriberID = subscriberID.substring(2);
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("-")
						|| subscriberID.startsWith("0"))
					subscriberID = subscriberID.substring(1);
			}
		}

		return subscriberID;
	}

	/**
	 * @return the httpSoTimeoutForBulk
	 */
	public int getHttpSoTimeoutForBulk() {
		return httpSoTimeoutForBulk;
	}

	/**
	 * @param httpSoTimeoutForBulk the httpSoTimeoutForBulk to set
	 */
	public void setHttpSoTimeoutForBulk(int httpSoTimeoutForBulk) {
		this.httpSoTimeoutForBulk = httpSoTimeoutForBulk;
	}

	/**
	 * @return the maxTotalHttpConnectionsForBulk
	 */
	public int getMaxTotalHttpConnectionsForBulk() {
		return maxTotalHttpConnectionsForBulk;
	}

	/**
	 * @param maxTotalHttpConnectionsForBulk the maxTotalHttpConnectionsForBulk to set
	 */
	public void setMaxTotalHttpConnectionsForBulk(int maxTotalHttpConnectionsForBulk) {
		this.maxTotalHttpConnectionsForBulk = maxTotalHttpConnectionsForBulk;
	}

	/**
	 * @return the maxHostHttpConnectionsForBulk
	 */
	public int getMaxHostHttpConnectionsForBulk() {
		return maxHostHttpConnectionsForBulk;
	}

	/**
	 * @param maxHostHttpConnectionsForBulk the maxHostHttpConnectionsForBulk to set
	 */
	public void setMaxHostHttpConnectionsForBulk(int maxHostHttpConnectionsForBulk) {
		this.maxHostHttpConnectionsForBulk = maxHostHttpConnectionsForBulk;
	}
	
	public boolean isUse_direct_connector_cv() {
		return useDirectConnectorCV;
	}
	
	public Map<String, List<String>> getComvivaApiMap() {
		return comvivaApiMap;
	}
	
	public Set<String> getCvCircleId() {
		return cvCircleId;
	}
	

 

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Configurations[centralCircle = ");
		builder.append(centralCircle);
		builder.append(", circleIpMap = ");
		builder.append(circleIpMap);
		builder.append(", circlePrefixMap = ");
		builder.append(circlePrefixMap);
		builder.append(", connectorClass = ");
		builder.append(connectorClass);
		builder.append(", restConnectorClass = ");
		builder.append(", countryCodePrefix = ");
		builder.append(countryCodePrefix);
		builder.append(", httpConnectionTimeout = ");
		builder.append(httpConnectionTimeout);
		builder.append(", httpSoTimeout = ");
		builder.append(httpSoTimeout);
		builder.append(", maxHostHttpConnections = ");
		builder.append(maxHostHttpConnections);
		builder.append(", maxTotalHttpConnections = ");
		builder.append(maxTotalHttpConnections);
		builder.append(", httpSoTimeoutForBulk = ");
		builder.append(httpSoTimeoutForBulk);
		builder.append(", maxHostHttpConnectionsForBulk = ");
		builder.append(maxHostHttpConnectionsForBulk);
		builder.append(", maxTotalHttpConnectionsForBulk = ");
		builder.append(maxTotalHttpConnectionsForBulk);
		builder.append(", minPhoneNumberLength = ");
		builder.append(minPhoneNumberLength);
		builder.append(", proxyHost = ");
		builder.append(proxyHost);
		builder.append(", proxyPort = ");
		builder.append(proxyPort);
		builder.append(", useMNPService = ");
		builder.append(useMNPService);
		builder.append(", useProxy = ");
		builder.append(useProxy);
		builder.append(", protocol = ");
		builder.append(protocol);
		builder.append(", overwriteDownload = ");
		builder.append(overwriteDownload);
		builder.append(", comviva_Api = ");
		builder.append(comvivaApiMap);
		builder.append(", use_direct_connector_cv = ");
		builder.append(useDirectConnectorCV);
		builder.append("]");
		return builder.toString();
	}
	
}
