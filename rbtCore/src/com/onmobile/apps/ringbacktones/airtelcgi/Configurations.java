/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


/**
 * @author vinayasimha.patil
 *
 */
public class Configurations implements DSProtocolConstants
{
	private static Logger logger = Logger.getLogger(Configurations.class);

	ResourceBundle resourceBundle;

	private Map<String, String> prfixIPMap;
	private Map<String, String> siteMap;
	private Map<String, String> ipPortMap;

	private String countryCodePrefix;

	private int serverPort;
	private int noOfDSMessageProcessors;
	private int serverEventWaitTime;

	private int httpConnectionTimeout;
	private int httpDataTimeout;
	private boolean useProxy;
	private String proxyHost;
	private int proxyPort;

	private String subscriberProfileURI;
	private String toneCopyURI;
	private String toneGiftURI;

	private String sdrWorkingDir;
	private int sdrSize;
	private long sdrInterval;
	private String sdrRotation;
	private boolean sdrBillingOn;

	private String httpServerHostName;
	private int httpServerPort;
	private int httpServerMaxConnections;
	private String httpServerAppBase;

	private int httpRequestTimeout;

	private String commonGatewayIP;

	private boolean debugModeOn;
	private Map<String, String> debugNumbersMap;
	private List<String> omTestCircleIPList;
	private String btslTestClientIP;

	public Configurations() throws IOException
	{
		InputStream inputStream = new FileInputStream("airtelcgi.properties");
		resourceBundle = new PropertyResourceBundle(inputStream);

		prfixIPMap = new HashMap<String, String>();
		siteMap = new HashMap<String, String>();
		ipPortMap = new HashMap<String, String>();
		initilizePrfixIPMap();
		initilizeIPPortMap();

		serverPort = 9090;
		noOfDSMessageProcessors = 5;
		serverEventWaitTime = 10000;

		httpConnectionTimeout = 6000;
		httpDataTimeout = 6000;
		useProxy = false;
		proxyHost = null;
		proxyPort = 0;

		subscriberProfileURI = "rbt/rbt_subscriberprofile.jsp";
		toneCopyURI = "rbt/rbt_tonecopy.jsp";
		toneGiftURI = "rbt/rbt_cross_gift.jsp";

		sdrWorkingDir = ".";
		sdrSize = 1000;
		sdrInterval = 24;
		sdrRotation = "size";
		sdrBillingOn = true;

		httpServerHostName = "localhost";
		httpServerPort = 8080;
		httpServerMaxConnections = 10;
		httpServerAppBase = ".";

		httpRequestTimeout = 30000;

		commonGatewayIP = null;

		debugModeOn = false;
		debugNumbersMap = new HashMap<String, String>();
		omTestCircleIPList = new ArrayList<String>();
		btslTestClientIP = null;

		initConfigParameters();

		if(debugModeOn)
			initilizeDebugNumbersAndIP();

		printConfigurations();
	}

	private void initilizePrfixIPMap()
	{
		String omSites = resourceBundle.getString(OM_SITES);
		String btslSites = resourceBundle.getString(BTSL_SITES);

		StringTokenizer ipTokenizer = new StringTokenizer(omSites, ",");
		while (ipTokenizer.hasMoreTokens())
		{
			String ipAddress = ipTokenizer.nextToken().trim();
			siteMap.put(ipAddress, OM_SITES);

			String prefixes = resourceBundle.getString(ipAddress);
			StringTokenizer prefixTokenizer = new StringTokenizer(prefixes, ",");
			while (prefixTokenizer.hasMoreTokens())
			{
				String prefix = prefixTokenizer.nextToken().trim();
				if(prfixIPMap.containsKey(prefix))
					ipAddress = ipAddress +","+ prfixIPMap.get(prefix);

				prfixIPMap.put(prefix, ipAddress);
			}
		}

		ipTokenizer = new StringTokenizer(btslSites, ",");
		while (ipTokenizer.hasMoreTokens())
		{
			String ipAddress = ipTokenizer.nextToken().trim();
			siteMap.put(ipAddress, BTSL_SITES);

			String prefixes = resourceBundle.getString(ipAddress);
			StringTokenizer prefixTokenizer = new StringTokenizer(prefixes, ",");
			while (prefixTokenizer.hasMoreTokens())
			{
				String prefix = prefixTokenizer.nextToken().trim();
				if(prfixIPMap.containsKey(prefix))
					ipAddress = ipAddress +","+ prfixIPMap.get(prefix);

				prfixIPMap.put(prefix, ipAddress);
			}
		}
	}

	private void initilizeIPPortMap()
	{
		String ipPorts = resourceBundle.getString("IP_PORT_MAP");
		StringTokenizer ipPortTokenizer = new StringTokenizer(ipPorts, ",");
		while (ipPortTokenizer.hasMoreTokens())
		{
			String ipPort = ipPortTokenizer.nextToken().trim();
			String ipAddress = ipPort.substring(0, ipPort.indexOf(':'));
			String port = ipPort.substring(ipPort.indexOf(':') + 1);
			ipPortMap.put(ipAddress, port);
		}
	}

	private void initConfigParameters()
	{
		try
		{
			countryCodePrefix = resourceBundle.getString("COUNTRY_CODE_PREFIX").trim();

			serverPort = Integer.parseInt(resourceBundle.getString("SERVER_PORT").trim());
			noOfDSMessageProcessors = Integer.parseInt(resourceBundle.getString("NO_OF_DSMESSAGE_PROCESSORS").trim());
			serverEventWaitTime = Integer.parseInt(resourceBundle.getString("SERVER_EVENT_WAIT_TIME").trim());

			httpConnectionTimeout = Integer.parseInt(resourceBundle.getString("HTTP_CONNECTION_TIMEOUT").trim());
			httpDataTimeout = Integer.parseInt(resourceBundle.getString("HTTP_DATA_TIMEOUT").trim());
			useProxy = Boolean.valueOf(resourceBundle.getString("USE_PROXY").trim()).booleanValue();
			proxyHost = resourceBundle.getString("PROXY_HOST").trim();
			proxyPort = Integer.parseInt(resourceBundle.getString("PROXY_PORT").trim());

			subscriberProfileURI = resourceBundle.getString("SUBSCRIBER_PROFILE_URI").trim();
			toneCopyURI = resourceBundle.getString("TONE_COPY_URI").trim();
			toneGiftURI = resourceBundle.getString("TONE_GIFT_URI").trim();

			sdrWorkingDir = resourceBundle.getString("SDR_WORKING_DIR").trim();
			sdrSize = Integer.parseInt(resourceBundle.getString("SDR_SIZE").trim());
			sdrInterval = Long.parseLong(resourceBundle.getString("SDR_INTERVAL").trim());
			sdrRotation = resourceBundle.getString("SDR_ROTATION").trim();
			sdrBillingOn = Boolean.valueOf(resourceBundle.getString("SDR_BILLING_ON").trim()).booleanValue();

			httpServerHostName = resourceBundle.getString("HTTPSERVER_HOST_NAME").trim();
			httpServerPort = Integer.parseInt(resourceBundle.getString("HTTPSERVER_PORT").trim());
			httpServerMaxConnections = Integer.parseInt(resourceBundle.getString("HTTPSERVER_MAX_CONNECTIONS").trim());
			httpServerAppBase = resourceBundle.getString("HTTPSERVER_APP_BASE").trim();

			httpRequestTimeout = Integer.parseInt(resourceBundle.getString("HTTP_RQUEST_TIMEOUT").trim());

			commonGatewayIP = resourceBundle.getString("COMMON_GATEWAY_IP").trim();

			debugModeOn = Boolean.valueOf(resourceBundle.getString("DEBUG_MODE_ON").trim()).booleanValue();
			if(debugModeOn)
				btslTestClientIP = resourceBundle.getString("BTSL_TEST_CLIENT_IP").trim();
		}
		catch(MissingResourceException e)
		{
			logger.error("", e);
		}
	}

	private void initilizeDebugNumbersAndIP()
	{
		try
		{
			String debugNumbers = resourceBundle.getString("DEBUG_NUMERS_MAP").trim();
			StringTokenizer tokenizer = new StringTokenizer(debugNumbers, ",");
			while(tokenizer.hasMoreTokens())
			{
				String[] keyValue = tokenizer.nextToken().split(":");
				debugNumbersMap.put(keyValue[0], keyValue[1]);
			}

			String testIPS = resourceBundle.getString("OM_TEST_CIRCLE_IP_LIST").trim();
			tokenizer = new StringTokenizer(testIPS, ",");
			while(tokenizer.hasMoreTokens())
				omTestCircleIPList.add(tokenizer.nextToken());
		}
		catch(MissingResourceException e)
		{
			logger.error("", e);
		}
	}

	public String[] getIPAddresses(String subscriberID)
	{
		String subID = subID(subscriberID);
		if(debugModeOn && debugNumbersMap.containsKey(subID))
		{
			String[] ipAddresses = {debugNumbersMap.get(subID)};
			return ipAddresses;
		}

		String prefix = subID.substring(0, 4);
		String ipAddresses = prfixIPMap.get(prefix);
		if(ipAddresses != null)
			return (ipAddresses.split(","));

		return null;
	}

	public String getSite(String ipAddress)
	{
		if(debugModeOn)
		{
			if(debugNumbersMap.values().contains(ipAddress))
				return OM_SITES;
			else if(btslTestClientIP != null && ipAddress.equals(btslTestClientIP))
				return BTSL_SITES;
		}

		return (siteMap.get(ipAddress));
	}

	public String getPort(String ipAddress)
	{
		return (ipPortMap.get(ipAddress));
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort()
	{
		return serverPort;
	}

	/**
	 * @return the noOfDSMessageProcessors
	 */
	public int getNoOfDSMessageProcessors()
	{
		return noOfDSMessageProcessors;
	}

	/**
	 * @return the serverEventWaitTime
	 */
	public int getServerEventWaitTime()
	{
		return serverEventWaitTime;
	}

	/**
	 * @return the httpConnectionTimeout
	 */
	public int getHttpConnectionTimeout()
	{
		return httpConnectionTimeout;
	}

	/**
	 * @return the httpDataTimeout
	 */
	public int getHttpDataTimeout()
	{
		return httpDataTimeout;
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
	 * @return the subscriberProfileURI
	 */
	public String getSubscriberProfileURI()
	{
		return subscriberProfileURI;
	}

	/**
	 * @return the toneCopyURI
	 */
	public String getToneCopyURI()
	{
		return toneCopyURI;
	}

	/**
	 * @return the toneGiftURI
	 */
	public String getToneGiftURI()
	{
		return toneGiftURI;
	}

	/**
	 * @return the sdrWorkingDir
	 */
	public String getSdrWorkingDir()
	{
		return sdrWorkingDir;
	}

	/**
	 * @return the sdrSize
	 */
	public int getSdrSize()
	{
		return sdrSize;
	}

	/**
	 * @return the sdrInterval
	 */
	public long getSdrInterval()
	{
		return sdrInterval;
	}

	/**
	 * @return the sdrRotation
	 */
	public String getSdrRotation()
	{
		return sdrRotation;
	}

	/**
	 * @return the sdrBillingOn
	 */
	public boolean isSdrBillingOn()
	{
		return sdrBillingOn;
	}

	/**
	 * @return the httpServerHostName
	 */
	public String getHttpServerHostName()
	{
		return httpServerHostName;
	}

	/**
	 * @return the httpServerPort
	 */
	public int getHttpServerPort()
	{
		return httpServerPort;
	}

	/**
	 * @return the httpServerMaxConnections
	 */
	public int getHttpServerMaxConnections()
	{
		return httpServerMaxConnections;
	}

	/**
	 * @return the httpServerAppBase
	 */
	public String getHttpServerAppBase()
	{
		return httpServerAppBase;
	}

	/**
	 * @return the httpRequestTimeout
	 */
	public int getHttpRequestTimeout()
	{
		return httpRequestTimeout;
	}

	/**
	 * @return the commonGatewayIP
	 */
	public String getCommonGatewayIP()
	{
		return commonGatewayIP;
	}

	/**
	 * @return the debugModeOn
	 */
	public boolean isDebugModeOn()
	{
		return debugModeOn;
	}

	/**
	 * @return the debugNumbersMap
	 */
	public Map<String, String> getDebugNumbersMap()
	{
		return debugNumbersMap;
	}

	/**
	 * @return the omTestCircleIPList
	 */
	public List<String> getOmTestCircleIPList()
	{
		return omTestCircleIPList;
	}

	/**
	 * @return the btslTestClientIP
	 */
	public String getBtslTestClientIP()
	{
		return btslTestClientIP;
	}

	public String subID(String subscriberID)
	{
		if (subscriberID != null)
		{
			if (countryCodePrefix != null)
			{
				StringTokenizer stk = new StringTokenizer(countryCodePrefix, ",");
				while (stk.hasMoreTokens())
				{
					String token = stk.nextToken();
					if (subscriberID.startsWith("00"))
					{
						subscriberID = subscriberID.substring(2);
					}
					if (subscriberID.startsWith("+")
							|| subscriberID.startsWith("0"))
					{
						subscriberID = subscriberID.substring(1);
					}
					if (subscriberID.startsWith(token))
					{
						subscriberID = subscriberID.substring(token.length());
						break;
					}
				}
			}
		}
		return subscriberID;
	}

	private void printConfigurations()
	{
		logger.info("RBT:: countryCodePrefix = "+ countryCodePrefix);
		logger.info("RBT:: serverPort = "+ serverPort);
		logger.info("RBT:: noOfDSMessageProcessors = "+ noOfDSMessageProcessors);
		logger.info("RBT:: serverEventWaitTime = "+ serverEventWaitTime);
		logger.info("RBT:: httpConnectionTimeout = "+ httpConnectionTimeout);
		logger.info("RBT:: httpDataTimeout = "+ httpDataTimeout);
		logger.info("RBT:: boolean useProxy = "+ useProxy);
		logger.info("RBT:: proxyHost = "+ proxyHost);
		logger.info("RBT:: proxyPort = "+ proxyPort);
		logger.info("RBT:: subscriberProfileURI = "+ subscriberProfileURI);
		logger.info("RBT:: toneCopyURI = "+ toneCopyURI);
		logger.info("RBT:: toneGiftURI = "+ toneGiftURI);
		logger.info("RBT:: sdrWorkingDir = "+ sdrWorkingDir);
		logger.info("RBT:: sdrSize = "+ sdrSize);
		logger.info("RBT:: long sdrInterval = "+ sdrInterval);
		logger.info("RBT:: sdrRotation = "+ sdrRotation);
		logger.info("RBT:: sdrBillingOn = "+ sdrBillingOn);
		logger.info("RBT:: httpServerHostName = "+ httpServerHostName);
		logger.info("RBT:: httpServerPort = "+ httpServerPort);
		logger.info("RBT:: httpServerMaxConnections = "+ httpServerMaxConnections);
		logger.info("RBT:: httpServerAppBase = "+ httpServerAppBase);
		logger.info("RBT:: httpRequestTimeout = "+ httpRequestTimeout);
		logger.info("RBT:: commonGatewayIP = "+ commonGatewayIP);
		logger.info("RBT:: debugModeOn = "+ debugModeOn);
		logger.info("RBT:: debugNumbersMap = "+ debugNumbersMap);
	}
}
