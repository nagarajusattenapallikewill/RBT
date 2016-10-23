package com.onmobile.apps.ringbacktones.thirdparty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author sridhar.sindiri
 *
 */
public class AirtelThirdPartyRequestHandler
{
	private static Logger logger = Logger.getLogger(AirtelThirdPartyRequestHandler.class);

	private static Map<String, String> m_ussdResponseMap = new HashMap<String, String>();
	private static Map<String, String> m_ecResponseMap = new HashMap<String, String>();
	private static Map<String, String> m_envIOResponseMap = new HashMap<String, String>();

	private static String ussdUrlReplacePackage = "/sms";
	private static String autodialUrlReplacePackage = "/autodial";
	private static String envIOUrlReplacePackage = "/envio";
	private static String ecUrlReplacePackage = "/easycharge";
	private static String modUrlReplacePackage = "/mod";

	public static final int USSD_UNKNOWN_ERROR = 400;
	public static final int USSD_SUBSCRIBER_INVALID = 410;

	public static final int ENVIO_ERROR = 10;
	public static final int ENVIO_SUBSCRIBER_INVALID = 5;

	public static final int AUTO_ERROR = -1;
	public static final int AUTO_SUBSCRIBER_INVALID = 6;

	public static int EC_0_ERROR = -1;
	public static int EC_0_SUBSCRIBER_INVALID = 6;

	public static final int MOD_ERROR = -1;
	public static final int MOD_SUBSCRIBER_INVALID = 5;

	private static RBTHttpClient rbtHttpClient = null;
	
	static
	{
		initHttpClient();
		initWarParams();
	}
	
	/**
	 * 
	 */
	private AirtelThirdPartyRequestHandler()
	{
	}

	/**
	 * 
	 */
	private static void initHttpClient()
	{
		HttpParameters httpParameters = new HttpParameters();

		String param = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "AIRTEL_THIRD_PARTY_CONNECTION_TIMEOUT_MS", "1000");
		httpParameters.setConnectionTimeout(Integer.parseInt(param));

		param = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "AIRTEL_THIRD_PARTY_SOCKET_TIMEOUT_MS", "0");
		httpParameters.setSoTimeout(Integer.parseInt(param));

		param = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "AIRTEL_THIRD_PARTY_PROXY", null);
		if (param != null)
		{
			int index = param.indexOf(":");
			httpParameters.setProxyHost(param.substring(0, index));
			httpParameters.setProxyPort(Integer.parseInt(param.substring(index + 1)));
		}
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	/**
	 * 
	 */
	private static void initWarParams()
	{
		List<Parameters> warParameters = CacheManagerUtil.getParametersCacheManager().getParameters("WAR");
		Map<String, String> warParamMap = new HashMap<String, String>();
		for (int i = 0; warParameters != null && i < warParameters.size(); i++)
		{
			warParamMap.put(warParameters.get(i).getParam(), warParameters.get(i).getValue());
		}

		if(warParamMap.containsKey("USSD_URL_REPLACE_PACKAGE"))
			ussdUrlReplacePackage = warParamMap.get("USSD_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("AUTODIAL_URL_REPLACE_PACKAGE"))
			autodialUrlReplacePackage = warParamMap.get("AUTODIAL_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("ENVIO_URL_REPLACE_PACKAGE"))
			envIOUrlReplacePackage = warParamMap.get("ENVIO_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("EC_URL_REPLACE_PACKAGE"))
			ecUrlReplacePackage = warParamMap.get("EC_URL_REPLACE_PACKAGE");
		if(warParamMap.containsKey("MOD_URL_REPLACE_PACKAGE"))
			modUrlReplacePackage = warParamMap.get("MOD_URL_REPLACE_PACKAGE");
	}

	/**
	 * @param subscriberID TODO
	 * @param requestParams
	 * @param ipAddress
	 * @param requestType
	 * @return
	 */
	public static Map<String, String> processThirdPartyRequest(String subscriberID, Map<String, String[]> requestParams, String ipAddress, String requestType)
	{
		String url = null;
		String response = "INVALID_REQUEST";
		int status = 200;

		logger.info("RBT:: RequestType : " + requestType + ", user : " + subscriberID + ", RequestParams : " + requestParams);
		try
		{
			url = getUrlForSub(subscriberID, requestType);
			if (url != null)
			{

				Map<String, String> httpParamMap = getHttpParamsMap(requestParams);
				logger.info("URL : " + url + ", HttpParamsMap : " + httpParamMap);
				HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(url, httpParamMap);
				logger.info("httpResponse : " + httpResponse);
				String urlResponse = null;
				if (httpResponse != null && httpResponse.getResponse() != null)
					urlResponse = httpResponse.getResponse().trim();

				logger.info("Response from the url : " + urlResponse);
				if(urlResponse != null)
				{
					int index = urlResponse.indexOf("|");
					if(index != -1)
					{
						response = urlResponse.substring(index + 1);
						status = Integer.parseInt(urlResponse.substring(0, index));
					}
					else
						response = urlResponse;
				}

			}
			else
			{
				logger.info("request type is " + requestType); 
				if(requestType.equalsIgnoreCase("ussd"))
				{ 
					Map<String, String> map = getUSSDHashMap(USSD_SUBSCRIBER_INVALID);
					response = map.get(new Integer(USSD_SUBSCRIBER_INVALID));
					status = USSD_SUBSCRIBER_INVALID; 
				} 
				else if(requestType.equalsIgnoreCase("envio"))
				{ 
					Map<String, String> map = getEnvIOHashMap(ENVIO_SUBSCRIBER_INVALID); 
					int result = Integer.parseInt(map.get("result")); 
					String message = map.get("message"); 
					response = getEnvioResponseAsString(result, message); 
				} 
				else if (requestType.equalsIgnoreCase("autodial"))
				{ 
					response = String.valueOf(AUTO_SUBSCRIBER_INVALID); 
				} 
				else if (requestType.equalsIgnoreCase("easycharge"))
				{ 
					String message = getECMessage(EC_0_SUBSCRIBER_INVALID, String.valueOf(2)); 
					response = getEasyChargeResponseAsString(EC_0_SUBSCRIBER_INVALID, message); 
				} 
				else if (requestType.equalsIgnoreCase("mod"))
				{ 
					response = String.valueOf(MOD_SUBSCRIBER_INVALID); 
				} 
			}
		}
		catch (Exception e)
		{
			if(requestType.equalsIgnoreCase("ussd"))
			{
				Map<String, String> map = getUSSDHashMap(USSD_UNKNOWN_ERROR);
				response = map.get(String.valueOf(USSD_UNKNOWN_ERROR));
				status = USSD_UNKNOWN_ERROR; 
			} 
			else if(requestType.equalsIgnoreCase("envio"))
			{ 
				Map<String, String> map = getEnvIOHashMap(ENVIO_ERROR); 
				int result = Integer.parseInt(map.get("result")); 
				String message = map.get("message"); 
				response = getEnvioResponseAsString(result, message); 
			} 
			else if (requestType.equalsIgnoreCase("autodial"))
			{ 
				response = String.valueOf(AUTO_ERROR); 
			} 
			else if (requestType.equalsIgnoreCase("easycharge"))
			{ 
				String message = getECMessage(EC_0_ERROR, String.valueOf(2)); 
				response = getEasyChargeResponseAsString(EC_0_ERROR, message); 

			}
			else if (requestType.equalsIgnoreCase("mod"))
			{
				response = String.valueOf(MOD_ERROR);
			}
			logger.error("", e);
		}

		Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put("response", response);
		responseMap.put("status", String.valueOf(status));
		return responseMap;
	}

	/**
	 * @param subID
	 * @param requestType
	 * @return
	 */
	private static String getUrlForSub(String subID, String requestType)
	{
		if (subID == null)
			return null;

		String urlReplacePackage = "";
		String jspName = "";

		if (requestType.equalsIgnoreCase("USSD"))
		{
			urlReplacePackage = ussdUrlReplacePackage;
			jspName = "/ussd.jsp?";
		}
		else if (requestType.equalsIgnoreCase("envio"))
		{
			urlReplacePackage = envIOUrlReplacePackage;
			jspName = "/envio.jsp?";
		}
		else if (requestType.equalsIgnoreCase("easycharge"))
		{
			urlReplacePackage = ecUrlReplacePackage;
			jspName = "/easycharge.jsp?";
		}
		else if (requestType.equalsIgnoreCase("autodial"))
		{
			urlReplacePackage = autodialUrlReplacePackage;
			jspName = "/autodial.jsp?";
		}
		else if (requestType.equalsIgnoreCase("mod"))
		{
			urlReplacePackage = modUrlReplacePackage;
			jspName = "/mod.jsp?";
		}

		String circleID = RbtServicesMgr.getSubscriberDetail(new MNPContext(subID)).getCircleID();
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
		String url = null;
		if (sitePrefix != null)
			url = sitePrefix.getSiteUrl();
		if (sitePrefix != null && url != null)
		{
			url = url.replaceAll("/rbt_sms.jsp", "");
			url = url.replaceAll("\\?", "");
			if (!sitePrefix.getAccessAllowed().equalsIgnoreCase("true"))
			{
				url = url.replaceAll("/rbt", urlReplacePackage);
				if (requestType.equalsIgnoreCase("USSD"))
					jspName = "/USSD.jsp?";
			}
			url = url + jspName;
			return url;
		}
		else
			return null;
	}

	/**
	 * @param result
	 * @param message
	 * @return
	 */
	private static String getEnvioResponseAsString(int result, String message)
	{ 
		return "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">" 
				+ result 
				+ "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>" 
				+ message
				+ "</td> <!-- response message (msg) --></tr></table></body></html>"; 

	}

	/**
	 * @param status
	 * @return
	 */
	public static Map<String, String> getUSSDHashMap(int status)
	{
		if (!m_ussdResponseMap.containsKey("USSD_RESPONSE_" + status))
		{
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "USSD_RESPONSE_" + status);
			if (param != null)
				m_ussdResponseMap.put(param.getParam(), param.getValue());
		}
		Map<String, String> retMap = new HashMap<String, String>();
		retMap.put("status", String.valueOf(status));
		String retMessage = m_ussdResponseMap.get("USSD_RESPONSE_" + status);
		retMap.put("message", retMessage);
		return retMap;
	}

	/**
	 * @param code
	 * @return
	 */
	public static Map<String, String> getEnvIOHashMap(int code)
	{
		if (!m_envIOResponseMap.containsKey("ENVIO_RESPONSE_" + code))
		{
			Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "ENVIO_RESPONSE_" + code);
			if (param != null)
				m_envIOResponseMap.put(param.getParam(), param.getValue());
		}
		Map<String, String> retMap = new HashMap<String, String>();
		String retMessage = m_envIOResponseMap.get("ENVIO_RESPONSE_" + code);
		retMap.put("result", String.valueOf(code));
		retMap.put("message", retMessage);
		return retMap;
	}

	/**
	 * @param code
	 * @param flag
	 * @return
	 */
	public static String getECMessage(int code, String flag)
	{
		String param = "EC_RESPONSE_" + flag + "_" + code;
		String response = "ERROR";
		if (m_ecResponseMap.containsKey(param))
			response = m_ecResponseMap.get(param);
		else
		{
			Parameters tempParam = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", param);
			if (tempParam != null)
				response = tempParam.getValue();
			m_ecResponseMap.put(param, response);
		}
		return response;
	}

	/**
	 * @param result
	 * @param message
	 * @return
	 */
	private static String getEasyChargeResponseAsString(int result, String message)
	{
		return "<html><head></head><body><table width=755 border=0 cellpadding=0 cellspacing=0 bgcolor=\"5A6F8A\"><tr><td width=65 align=center class=tit height=\"26\">Value</td><td width=170 align=center class=tit ></td><td width=390 align=center class=tit>Remarks</td></tr></table><table width=755 border=0 cellpadding=0 cellspacing=0 ><tr><td width=65 align=center height=\"26\">"
				+ result
				+ "</td> <!-- response code (res) --><td width=170 align=center ></td><td width=390 align=center nowrap>"
				+ message
				+ "</td> <!-- response message (msg) --></tr></table></body></html>";
	}
	
	/**
	 * @param requestParamsMap
	 * @return
	 */
	private static Map<String, String> getHttpParamsMap(Map<String, String[]> requestParamsMap)
	{
		Map<String, String> httpParamsMap = new HashMap<String, String>();
		Set<Entry<String, String[]>> entrySet = requestParamsMap.entrySet();
		for (Entry<String, String[]> entry : entrySet)
		{
			httpParamsMap.put(entry.getKey(), entry.getValue()[0]);
		}

		return httpParamsMap;
	}
}
