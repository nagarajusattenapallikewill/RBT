/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.onmobile.amoeba.vui.cms.utils.CMSUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.RBTLogger;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.serviceparameters.utils.SPInteractor;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTContent implements WebServiceConstants
{
	private static final String CLASSNAME = "RBTContent";

	private static RBTContent rbtContent = null;

	private Object syncObject = null;

	private Map<String, String> contentMap;
	private Map<String, String> contentURLMap;
	private List<String> contentKeyList = null;

	private RBTHttpClient rbtHttpClient = null;

	private boolean cachingEnabled;
	private int noOfCategoryPagesToBeCached;
	private int noOfClipPagesToBeCached;

	private ContentClearTask contentClearTask = null;

	private RBTContent()
	{
		int httpConnectionTimeout = 6000;
		int httpSoTimeout = 6000;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = 0;
		int httpMaxTotalConnections = 20;
		int httpMaxHostConnections = 2;

		try
		{
			syncObject = new Object();

			contentMap = new HashMap<String, String>();
			contentURLMap = new HashMap<String, String>();
			String contentURL = getRBTServiceParameter("CONTENT_URL");
			String[] contentURLs = contentURL.split(",");
			for (String contentURLStr : contentURLs)
			{
				String[] tokens = contentURLStr.split("=");
				contentURLMap.put(tokens[0], tokens[1]);
			}
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: contentURLMap = " + contentURLMap);

			contentKeyList = new ArrayList<String>();
			String contentKeys = getRBTServiceParameter("CONTENT_KEYS");
			if (contentKeys != null)
				contentKeyList = Arrays.asList(contentKeys.split(","));
			else
				contentKeyList.add("CIRCLE_ID");
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: contentKeyList = " + contentKeyList);

			String httpConTimeOutStr = getRBTServiceParameter("HTTP_CONNECTION_TIMEOUT"); 
			if (httpConTimeOutStr != null)
				httpConnectionTimeout = Integer.parseInt(httpConTimeOutStr.trim());

			String httpSoTimeOutStr = getRBTServiceParameter("HTTP_SOCKECT_TIMEOUT"); 
			if (httpSoTimeOutStr != null)
				httpSoTimeout = Integer.parseInt(httpSoTimeOutStr.trim());

			String useProxyStr = getRBTServiceParameter("USE_PROXY"); 
			if (useProxyStr != null && useProxyStr.equalsIgnoreCase("true"))
				useProxy = true;

			if (useProxy)
			{
				proxyHost = getRBTServiceParameter("PROXY_HOST");
				proxyPort = Integer.parseInt(getRBTServiceParameter("PROXY_PORT").trim());
			}

			String httpMaxTotalConnectionsStr = getRBTServiceParameter("MAX_TOTAL_HTTP_CONNECTIONS"); 
			if (httpMaxTotalConnectionsStr != null)
				httpMaxTotalConnections = Integer.parseInt(httpMaxTotalConnectionsStr.trim());

			String httpMaxHostConnectionsStr = getRBTServiceParameter("MAX_HOST_HTTP_CONNCETIONS"); 
			if (httpSoTimeOutStr != null)
				httpMaxHostConnections = Integer.parseInt(httpMaxHostConnectionsStr.trim());

			HttpParameters httpParameters = new HttpParameters(useProxy,
					proxyHost, proxyPort, httpConnectionTimeout, httpSoTimeout,
					httpMaxTotalConnections, httpMaxHostConnections);
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: httpParameters: " + httpParameters);

			rbtHttpClient = new RBTHttpClient(httpParameters);

			cachingEnabled = true;
			String cachingEnabledStr = getRBTServiceParameter("CACHING_ENABLED");
			if (cachingEnabledStr != null)
				cachingEnabled = cachingEnabledStr.equalsIgnoreCase("TRUE");
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: cachingEnabled: " + cachingEnabled);

			noOfCategoryPagesToBeCached = 10;
			String noOfCategoryPagesToBeCachedStr = getRBTServiceParameter("NO_OF_CATEGORY_PAGES_TOBE_CACHED"); 
			if (noOfCategoryPagesToBeCachedStr != null)
				noOfCategoryPagesToBeCached = Integer.parseInt(noOfCategoryPagesToBeCachedStr.trim());
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: noOfCategoryPagesToBeCached = " + noOfCategoryPagesToBeCached);

			noOfClipPagesToBeCached = 1;
			String noOfClipPagesToBeCachedStr = getRBTServiceParameter("NO_OF_CLIP_PAGES_TOBE_CACHED"); 
			if (noOfClipPagesToBeCachedStr != null)
				noOfClipPagesToBeCached = Integer.parseInt(noOfClipPagesToBeCachedStr.trim());
			RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: noOfClipPagesToBeCached = " + noOfClipPagesToBeCached);

			String contentRefreshTime = getRBTServiceParameter("CONTENT_REFRESH_TIME");
			if (contentRefreshTime != null)
			{
				contentClearTask = new ContentClearTask();
				Timer timer = new Timer();

				Calendar calendar = Calendar.getInstance();
				String[] contentRefreshTimeTokens = contentRefreshTime.split(":");
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(contentRefreshTimeTokens[0]));
				calendar.set(Calendar.MINUTE, Integer.parseInt(contentRefreshTimeTokens[1]));
				if (calendar.getTime().before(new Date()))
					calendar.add(Calendar.DAY_OF_YEAR, 1);

				RBTLogger.logDetail(CLASSNAME, "RBTContent", "RBT:: Content Clear Task start time: " + calendar.getTime());
				timer.scheduleAtFixedRate(contentClearTask, calendar.getTime(), (1000*60*60*24));
			}
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "RBTContent", e);
		}
	}

	public String getRBTServiceParameter(String param)
	{
		String value = null;

		try
		{
			value = SPInteractor.getParameter("APP", "RBT", param);
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "getRBTServiceParameter", e);
		}

		return value;
	}

	synchronized public static RBTContent getInstance()
	{
		if (rbtContent == null)
			rbtContent = new RBTContent();

		return rbtContent;
	}

	synchronized public static RBTContent createRBTContent()
	{
		if (rbtContent != null)
			rbtContent.stopContentClearTask();

		rbtContent = new RBTContent();

		try
		{
			CMSUtils.clearRBTContentCache();
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "createRBTContent", e);
		}

		return rbtContent;
	}

	public String getContent(int contentID, String contentType, String circleID, String isPrepaid, String language, int pageNo)
	{
		String content = null;

		String key = getKey(contentID, contentType, circleID, isPrepaid, language, pageNo);
		if (cachingEnabled && contentMap.containsKey(key))
		{
			content = contentMap.get(key);
			RBTLogger.logDetail(CLASSNAME, "getContent", "RBT:: Content from cache: " + content);
			return content;
		}

		content = getContentFromWebService(contentID, contentType, circleID, isPrepaid, language, pageNo);

		if (cachingEnabled)
			addToContentMap(contentType, pageNo, key, content);

		return content;
	}

	private String getContentFromWebService(int contentID, String contentType,
			String circleID, String isPrepaid, String language, int pageNo)
	{
		try
		{
			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(param_contentID, String.valueOf(contentID));
			requestParams.put(param_contentType, contentType);
			if (circleID != null && contentKeyList.contains("CIRCLE_ID"))
				requestParams.put(param_circleID, circleID);
			if (isPrepaid != null && contentKeyList.contains("USER_TYPE"))
				requestParams.put(param_isPrepaid, isPrepaid);
			if (language != null && (contentKeyList.contains("LANGUAGE") || contentType.equalsIgnoreCase(CATEGORY_PROFILE_CLIPS)))
				requestParams.put(param_language, language);
			requestParams.put(param_pageNo, String.valueOf(pageNo));
			RBTLogger.logDetail(CLASSNAME, "getContentFromWebService", "RBT:: requestParams: " + requestParams);

			String url = getContentURL(circleID);
			RBTLogger.logDetail(CLASSNAME, "getContentFromWebService", "RBT:: url: " + url);

			HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(url, requestParams);
			RBTLogger.logDetail(CLASSNAME, "getContentFromWebService", "RBT:: httpResponse: " + httpResponse);
			RBTLogger.logDetail(CLASSNAME, "getContentFromWebService", "RBT:: " + rbtHttpClient.getConnectionPoolStatus(url));

			if (httpResponse != null && httpResponse.getResponse() != null)
				return httpResponse.getResponse().trim();
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "getContentFromWebService", e);
		}

		return null;
	}

	public String getKey(int contentID, String contentType, String circleID, String isPrepaid, String language, int pageNo)
	{
		String key = String.valueOf(contentID);
		if (isCategoryType(contentType))
			key = getKeyForCategory(contentID, circleID, isPrepaid, language);
		else if (contentType.equalsIgnoreCase(CATEGORY_PROFILE_CLIPS))
			key = String.valueOf(contentID) + "_" + language;
		key += "_" + pageNo;

		RBTLogger.logDetail(CLASSNAME, "getKey", "RBT:: key: " + key);
		return key;
	}

	private String getKeyForCategory(int contentID, String circleID, String isPrepaid, String language)
	{
		String key = String.valueOf(contentID);

		if (circleID != null && contentKeyList.contains("CIRCLE_ID"))
			key += "_" + circleID;
		if (isPrepaid != null && contentKeyList.contains("USER_TYPE"))
			key += "_" + isPrepaid;
		if (language != null && contentKeyList.contains("LANGUAGE"))
			key += "_" + language;

		RBTLogger.logDetail(CLASSNAME, "getKeyForCategory", "RBT:: key = " + key);
		return key;
	}

	private String getContentURL(String circleID)
	{
		String contentURL = null;
		if (circleID == null)
			contentURL = contentURLMap.get("ALL");
		else
		{
			contentURL = contentURLMap.get(circleID);
			if (contentURL == null)
				contentURL = contentURLMap.get("ALL");
		}

		return contentURL;
	}

	private boolean isCategoryType(String contentType)
	{
		if (contentType.equalsIgnoreCase(CATEGORY)
				|| contentType.equalsIgnoreCase(CATEGORY_PARENT)
				|| contentType.equalsIgnoreCase(CATEGORY_BOUQUET))
			return true;

		return false;
	}

	synchronized private void addToContentMap(String contentType, int pageNo, String key, String content)
	{
		if (content == null)
			return;

		if (isCategoryType(contentType) && pageNo > noOfCategoryPagesToBeCached)
		{
			RBTLogger.logDetail(CLASSNAME, "addToContentMap", "RBT:: Not Adding to contentMap pageNo(" + pageNo + ") > noOfCategoryPagesToBeCached(" + noOfCategoryPagesToBeCached + ")");
			return;
		}
		if (!isCategoryType(contentType) && pageNo > noOfClipPagesToBeCached)
		{
			RBTLogger.logDetail(CLASSNAME, "addToContentMap", "RBT:: Not Adding to contentMap pageNo(" + pageNo + ") > noOfClipPagesToBeCached(" + noOfClipPagesToBeCached + ")");
			return;
		}

		synchronized (syncObject)
		{
			contentMap.put(key, content);
			RBTLogger.logDetail(CLASSNAME, "addToContentMap", "RBT:: Adding to contentMap {" + key + "," + content + "}");
		}
	}

	synchronized public void clearContentMap()
	{
		synchronized (syncObject)
		{
			RBTLogger.logDetail(CLASSNAME, "clearContentMap", "RBT:: Clearing contentMap");
			contentMap.clear();
		}

		try
		{
			CMSUtils.clearRBTContentCache();
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "clearContentMap", e);
		}
	}

	public void clearContentMap(int contentID)
	{
		String contentIDStr = String.valueOf(contentID);

		Set<String> keySet = contentMap.keySet();
		for (String key : keySet)
		{
			if (key.startsWith(contentIDStr))
			{
				synchronized (syncObject)
				{
					RBTLogger.logDetail(CLASSNAME, "clearContentMap", "RBT:: Clearing Content " + key + " from contentMap");
					contentMap.remove(key);
				}
			}
		}

		CMSUtils.clearRBTContentCacheForKeyStartingWith(contentID); 
	}

	synchronized public void clearContentMap(String key)
	{
		synchronized (syncObject)
		{
			RBTLogger.logDetail(CLASSNAME, "clearContentMap", "RBT:: Clearing Content " + key + " from contentMap");
			contentMap.remove(key);
		}

		try
		{
			CMSUtils.clearRBTContentCacheForKey(key);
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "clearContentMap", e);
		}
	}

	public void stopContentClearTask()
	{
		if (contentClearTask != null)
			contentClearTask.stop();
	}

	public static void main(String[] args)
	{
		RBTContent rbtContent = RBTContent.getInstance();
		int contentID = Integer.parseInt(args[0]);
		String contentType = args[1];
		String circleID = args[2];
		String isPrepaid = args[3];
		if (isPrepaid.equalsIgnoreCase("NULL"))
			isPrepaid = null;
		String language = args[4];
		if (language.equalsIgnoreCase("NULL"))
			language = null;
		int pageNo = Integer.parseInt(args[5]);

		String response = rbtContent.getContent(contentID, contentType, circleID, isPrepaid, language, pageNo);
		System.out.println(response);
	}

	private class ContentClearTask extends TimerTask
	{
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		public void run()
		{
			RBTContent.getInstance().clearContentMap();
		}

		public void stop()
		{
			this.cancel();
		}
	}

}
