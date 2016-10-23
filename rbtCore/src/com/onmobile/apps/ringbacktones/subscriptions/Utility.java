package com.onmobile.apps.ringbacktones.subscriptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.nametunes.PropertiesProvider;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.mnp.MnpService;
import com.onmobile.mnp.MnpServiceFactory;
import com.onmobile.mnp.model.CustomerCircle;

/**
 * @author sridhar.sindiri
 * 
 */
public class Utility {
	private static Logger logger = Logger.getLogger(Utility.class);
	public static HashMap<String, String> m_customerMap = new HashMap<String, String>();
	public static Hashtable<String, SitePrefix> m_sitePrefixTable = new Hashtable<String, SitePrefix>();

	public static String m_ContentUrl = null;
	public static boolean useProxyContent = false;
	public static String proxyHostContent = null;
	public static int proxyPortContent = -1;

	private static HttpClient m_httpClient = null;
	private static Map<String, List<String>> modeOperatorCircleMap = new HashMap<String, List<String>>();

	public static Map<String, List<String>> subscriptClsMap=new HashMap<String, List<String>>();
	public static PropertiesProvider propertiesProvider = new PropertiesProvider("config.properties");
	
	static {
		initSitePrefixTable();
		initCustomerMapping();
		initContentUrl();
		initHttp();
		initConfigProperties();
	}

	public static String getSubscriberOperator(String subID, String ip, String mode) {
		Parameters params = CacheManagerUtil.getParametersCacheManager()
				.getParameter("RDC", "USE_MNP", "FALSE");
		boolean useMNP = params.getValue().equalsIgnoreCase("TRUE");
		String operator = "UNKNOWN";
		if (useMNP) {
			try {
				MnpServiceFactory mnpServiceFactory = MnpServiceFactory
						.getInstance();
				MnpService mnpService = mnpServiceFactory.getMnpService();
				CustomerCircle custCircle = mnpService
						.getCustomerCircleFromExternalSource(subID);
				if (custCircle != null) {
					String mnpCustomer = custCircle.getCustomer()
							.getCustomerName();
					operator = getMappedCustomer(mnpCustomer);
				}
			} catch (Throwable e) {
				logger.info("Throwable caught : " + e);
				logger.error("", e);
			}
			return operator;
		} else {
			if (m_sitePrefixTable == null || m_sitePrefixTable.size() <= 0)
				return null;

			Iterator<String> operatorsNamesItr = m_sitePrefixTable.keySet()
					.iterator();
			while (operatorsNamesItr.hasNext()) {
				String oprName = operatorsNamesItr.next();
				SitePrefix sitePrefix = m_sitePrefixTable.get(oprName);
				Hashtable<Integer, ArrayList<String>> prefixList = getPrefixTable(sitePrefix
						.getSitePrefix());

				if (isValidTypePrefix(subID, prefixList))
					return oprName;
			}

			return "UNKNOWN";
		}
	}
	
	public static boolean isRequestBlockedForModeOperatorCircle(String mode, String circleName, 
        String operator) {
		String modeBasedConfig = RBTParametersUtils.getParamAsString(
				"CONTENT_INTER_OPERATORABILITY", "MODE_OPERATOR_AND_CIRCLE_MAP_FOR_BLOCKING", null);
		if (modeBasedConfig != null && modeOperatorCircleMap.size() == 0) {
			String str[] = modeBasedConfig.split(";");
			for (String s : str) {
				List<String> operatorList = null;
				String st[] = s.split("=");
				if (st.length == 2) {
					if (modeOperatorCircleMap.containsKey(st[0])) { 
						operatorList = modeOperatorCircleMap.get(st[0]);
					} else {
						operatorList = new ArrayList<String>();
					}
					operatorList.addAll(Arrays.asList(st[1].split(",")));
					modeOperatorCircleMap.put(st[0], operatorList);
				}
			}
		}

		List<String> allowedCirclesList = modeOperatorCircleMap.get(mode+"_"+operator);
		if (allowedCirclesList != null && allowedCirclesList.contains(circleName)) {
			return true;
		}

		return false;

	}
	
	public static String getSubscriberOperator(String subID, String ip) {
		Parameters params = CacheManagerUtil.getParametersCacheManager()
				.getParameter("RDC", "USE_MNP", "FALSE");
		boolean useMNP = params.getValue().equalsIgnoreCase("TRUE");
		String operator = "UNKNOWN";
		if (useMNP) {
			try {
				MnpServiceFactory mnpServiceFactory = MnpServiceFactory
						.getInstance();
				MnpService mnpService = mnpServiceFactory.getMnpService();
				CustomerCircle custCircle = mnpService
						.getCustomerCircleFromExternalSource(subID);
				if (custCircle != null) {
					String mnpCustomer = custCircle.getCustomer()
							.getCustomerName();
					operator = getMappedCustomer(mnpCustomer);
				}
			} catch (Throwable e) {
				logger.info("Throwable caught : " + e);
				logger.error("", e);
			}
			return operator;
		} else {
			if (m_sitePrefixTable == null || m_sitePrefixTable.size() <= 0)
				return null;

			Iterator<String> operatorsNamesItr = m_sitePrefixTable.keySet()
					.iterator();
			while (operatorsNamesItr.hasNext()) {
				String oprName = operatorsNamesItr.next();
				SitePrefix sitePrefix = m_sitePrefixTable.get(oprName);
				Hashtable<Integer, ArrayList<String>> prefixList = getPrefixTable(sitePrefix
						.getSitePrefix());

				if (isValidTypePrefix(subID, prefixList))
					return oprName;
			}

			return "UNKNOWN";
		}
	}

	public static Hashtable<Integer, ArrayList<String>> getPrefixTable(
			String prefixList) {
		int iPrefixLen = -1;
		String prefixTempStr = null;
		ArrayList<String> prefixAList = null;
		Hashtable<Integer, ArrayList<String>> returnTable = null;
		if (prefixList != null && prefixList.length() > 0) {
			StringTokenizer stk = new StringTokenizer(prefixList, ",");
			returnTable = new Hashtable<Integer, ArrayList<String>>();
			while (stk.hasMoreTokens()) {
				prefixTempStr = stk.nextToken().trim();
				iPrefixLen = prefixTempStr.length();
				prefixAList = null;
				if (iPrefixLen <= 0)
					continue;

				if (returnTable.containsKey(new Integer(iPrefixLen)))
					prefixAList = returnTable.get(new Integer(iPrefixLen));
				else
					prefixAList = new ArrayList<String>();

				prefixAList.add(prefixTempStr);
				returnTable.put(new Integer(iPrefixLen), prefixAList);
			}
		}
		logger.info(" with param as " + prefixList + " returning Table as "
				+ returnTable);
		return returnTable;
	}

	public static boolean isValidTypePrefix(String subscriberID,
			Hashtable<Integer, ArrayList<String>> m_prefixMap) {
		if (subscriberID == null || subscriberID.length() < 7
				|| subscriberID.length() > 15 || m_prefixMap == null)
			return false;
		else {
			try {
				Long.parseLong(subID(subscriberID));
			} catch (Throwable e) {
				return false;
			}
		}
		if (m_prefixMap.size() == 0)
			return true;

		int prefixLength = -1;
		String thisPrefix = null;
		ArrayList<String> prefixArrayListTemp = null;
		Integer prefixKey = null;
		Iterator<Integer> prefixIteror = m_prefixMap.keySet().iterator();
		while (prefixIteror.hasNext()) {
			prefixKey = prefixIteror.next();
			prefixLength = prefixKey.intValue();
			thisPrefix = subscriberID.substring(0, prefixLength);
			prefixArrayListTemp = m_prefixMap.get(prefixKey);
			if (prefixArrayListTemp.contains(thisPrefix)) {
				logger.info("RBT:prefix true");
				return true;
			}
		}
		logger.info("RBT:prefix false");
		return false;
	}

	public static String subID(String strSubID) {
		return (RBTDBManager.getInstance().subID(strSubID));
	}

	private static void initSitePrefixTable() {
		logger.info("Entered");
		List<SitePrefix> prefixes = CacheManagerUtil
				.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;

		for (int i = 0; i < prefixes.size(); i++)
			m_sitePrefixTable.put(prefixes.get(i).getSiteName(),
					prefixes.get(i));

		logger.info("Exiting with m_sitePrefixTable as " + m_sitePrefixTable);
	}

	public static String getOperatorURL(String operatorName) {
		String operatorUrl = null;
		SitePrefix sitePrefix = m_sitePrefixTable.get(operatorName);
		if (sitePrefix != null)
			operatorUrl = sitePrefix.getSiteUrl();

		return operatorUrl;
	}

	private static void initCustomerMapping() {
		logger.info("Entering");
		String customerMapString = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue("RDC", "CUSTOMER_MAP", null);
		logger.info("customerMapString is : " + customerMapString);
		if (customerMapString == null) {
			logger.info("Parameter CUSTOMER_MAP is null.In MNP scenario, this will break customer validation logic.");
			return;
		}
		StringTokenizer stk = new StringTokenizer(customerMapString, ";");
		while (stk.hasMoreTokens()) {
			StringTokenizer stk2 = new StringTokenizer(stk.nextToken(), ",");
			if (stk2.countTokens() != 2)
				continue;
			String mnpCustomer = stk2.nextToken().trim();
			String omCustomer = stk2.nextToken().trim();
			m_customerMap.put(mnpCustomer, omCustomer);
		}
		logger.info("m_customerMap is " + m_customerMap);
		logger.info("Exiting");
	}

	public static String getMappedCustomer(String mnpCustomer) {
		String finalCircle = "UNKNOWN";
		if (m_customerMap.containsKey(mnpCustomer))
			finalCircle = m_customerMap.get(mnpCustomer);

		return finalCircle;
	}

	private static void initContentUrl() {
		String strContentDetails = null;
		Parameters contentUrlParams = CacheManagerUtil
				.getParametersCacheManager().getParameter("RDC", "CONTENT_URL",
						null);
		if (contentUrlParams != null && contentUrlParams.getValue() != null)
			strContentDetails = contentUrlParams.getValue();

		if (strContentDetails != null && strContentDetails.length() > 0) {
			StringTokenizer stk = new StringTokenizer(strContentDetails, ",");
			if (stk.hasMoreTokens())
				m_ContentUrl = stk.nextToken().trim();
			if (stk.hasMoreTokens())
				useProxyContent = stk.nextToken().trim()
						.equalsIgnoreCase("true");
			if (stk.hasMoreTokens())
				proxyHostContent = stk.nextToken().trim();
			try {
				if (stk.hasMoreTokens())
					proxyPortContent = Integer.parseInt(stk.nextToken().trim());
			} catch (Exception e) {
				proxyPortContent = -1;
			}
		}
	}

	private static void initHttp() {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setStaleCheckingEnabled(true);
		connectionManager.getParams().setMaxConnectionsPerHost(
				HostConfiguration.ANY_HOST_CONFIGURATION, 10);
		connectionManager.getParams().setMaxTotalConnections(20);
		connectionManager.getParams().setConnectionTimeout(10000);
		m_httpClient = new HttpClient(connectionManager);
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				0, false);
		m_httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		m_httpClient.getParams().setSoTimeout(10000);
	}

	public static String callContentURL(String strURL, StringBuffer xtraInfo) {
		GetMethod get = null;
		try {
			strURL = strURL.trim();
			logger.info("URL = " + strURL);
			URL urlObj = new URL(strURL);
			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(urlObj.getHost(), urlObj.getPort());
			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			logger.info("Start time in millisecond: " + startTime);

			int httpResponseCode = m_httpClient.executeMethod(ohcfg, get);
			String response = get.getResponseBodyAsString();

			long endTime = System.currentTimeMillis();
			logger.info("End time in millisecond: " + endTime);
			logger.info("Diff in millisecond: " + (endTime - startTime));
			logger.info("Response Code:" + httpResponseCode);
			logger.info("Response ->" + response);
			if (httpResponseCode == 204) {
				xtraInfo.append("-1");
				return "COPYCONTENTMISSING";
			} else if (httpResponseCode == 200) {
				Header headerWav = get.getResponseHeader("TARGET_CLIPID");
				String wavFile = null;
				if (headerWav != null && headerWav.getValue() != null)
					wavFile = headerWav.getValue().trim();
				logger.info("wavFile in header ->" + wavFile);

				if (wavFile == null)
					throw new Exception(
							"Content Problem. No wavFile in header for status code 200.");

				if (wavFile.endsWith(".wav"))
					wavFile = wavFile.substring(0, wavFile.indexOf(".wav"));

				return wavFile;
			} else
				return "RETRY";
		} catch (Throwable e) {
			logger.error("", e);
			return "RETRY";
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

	public static boolean callURL(String strURL, StringBuffer statusCode,
			StringBuffer response, boolean useProxy, String proxyHost,
			int proxyPort) {
		GetMethod get = null;
		try {
			strURL = strURL.trim();
			logger.info("URL = " + strURL);

			URL urlObj = new URL(strURL);
			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(urlObj.getHost(), urlObj.getPort());
			if (useProxy && proxyHost != null && proxyPort != -1)
				ohcfg.setProxy(proxyHost, proxyPort);

			get = new GetMethod(strURL);
			long startTime = System.currentTimeMillis();
			logger.info("Start time in millisecond: " + startTime);
			int httpResponseCode = m_httpClient.executeMethod(ohcfg, get);
			statusCode.append(httpResponseCode);
			response.append(get.getResponseBodyAsString());
			long endTime = System.currentTimeMillis();
			logger.info("End time in millisecond: " + endTime);
			logger.info("Diff in millisecond: " + (endTime - startTime));
			logger.info("Response Code:" + statusCode);
			logger.info("Response ->" + response.toString().trim() + "");
			if (httpResponseCode == 200)
				return true;
			else
				return false;
		} catch (Throwable e) {
			logger.error("", e);
			return false;
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}
	
	// RBT-18434 D2C : Get subscription API changes to support new user operator type
	public static void initConfigProperties(){
		for(OperatorUserTypes key : OperatorUserTypes.values()){
			String value = propertiesProvider.getPropertyValue(key.name());
			if(value!=null){
				String[] subCls= value.split(",");
				if(subCls!=null && subCls.length>=0){
					subscriptClsMap.put(key.name(), Arrays.asList(subCls));
				}else{
					logger.error("Subscription Class:"+subCls+" Doesn't have values in config properties file");
				}
			}
		}
	}
	
	public static String getOperatorUserType(String subscripClas){
		String oprUserType=null;
		Iterator<Map.Entry<String, List<String>>> itr = subscriptClsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, List<String>> entry = itr.next();
				if(entry.getValue().contains(subscripClas)){
					oprUserType = OperatorUserTypes.valueOf(entry.getKey()).getDefaultValue();
				}    				
		}	        		
		if(oprUserType==null){
			logger.error("OPERATOR USER TYPE IS NOT FOUND IN CONFIGURATION FOR SUBSCRIPTION CLASS:"+subscripClas);
		}
		return oprUserType;
	}
	
	public static OperatorUserDetails getOperatorUserDetails(String msisdn,Subscriber subscriber,int freeTrailCosId,boolean isD2CDeployed){
		OperatorUserDetails operatorUserDetails = null;
		
		List<ProvisioningRequests> list = RBTDBManager.getInstance().getProvisioningRequests(msisdn,freeTrailCosId);

		if (list == null || list.isEmpty()) {
			String oprUserType = isD2CDeployed ? OperatorUserTypes.LEGACY.getDefaultValue() : OperatorUserTypes.TRADITIONAL.getDefaultValue();
			operatorUserDetails = new OperatorUserDetailsImpl(msisdn, oprUserType,
					subscriber.subscriptionClass(), ServiceUtil.getOperatorName(subscriber),
					getCirclerName(subscriber));
		}

		for (ProvisioningRequests provisioningRequests : list) {
			if (provisioningRequests.getStatus() == 43) {
				String oprUserType = isD2CDeployed ? OperatorUserTypes.LEGACY.getDefaultValue() : OperatorUserTypes.TRADITIONAL.getDefaultValue();
				operatorUserDetails = new OperatorUserDetailsImpl(msisdn,
						oprUserType, subscriber.subscriptionClass(),
						ServiceUtil.getOperatorName(subscriber), getCirclerName(subscriber));
			} else if (provisioningRequests.getStatus() == 33 || provisioningRequests.getStatus() == 41
					|| provisioningRequests.getStatus() == 42 || provisioningRequests.getStatus() == 44) {
				
				String oprUserType = isD2CDeployed ? OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue() : OperatorUserTypes.TRADITIONAL_FREE_TRIAL.getDefaultValue();
				operatorUserDetails = new OperatorUserDetailsImpl(msisdn, oprUserType,
						subscriber.subscriptionClass(), ServiceUtil.getOperatorName(subscriber),
						getCirclerName(subscriber));
			} else {
				String oprUserType = isD2CDeployed ? OperatorUserTypes.LEGACY.getDefaultValue() : OperatorUserTypes.TRADITIONAL.getDefaultValue();
				operatorUserDetails = new OperatorUserDetailsImpl(msisdn, oprUserType,
						subscriber.subscriptionClass(), ServiceUtil.getOperatorName(subscriber),
						getCirclerName(subscriber));
			}
		}

		return operatorUserDetails;
	}
	

	public static String getCirclerName(Subscriber subscriber) {
		String circle = "";
		if (subscriber.circleID() == null || subscriber.circleID().isEmpty()) {
			return circle;
		}
		if (subscriber.circleID().trim().contains("_"))
			circle = subscriber.circleID().trim().split("_")[1];
		else
			circle = subscriber.circleID();

		return circle;
	}
}
