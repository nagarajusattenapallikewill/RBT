package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.genericcache.dao.RbtSupportDao;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RbtSupportDaemon extends Thread implements WebServiceConstants {
	
	private static Logger logger = Logger.getLogger(RbtSupportDaemon.class);
	private static Logger transLogger = Logger.getLogger("PESS_DOWNLOAD_TRANS");
	
	private static final String HTTP_TIRDPARTY_DOWNLOAD_URL = "HTTP_TIRDPARTY_DOWNLOAD_URL";

	private static final String PROCESS_CIRCLE_FOR_PRESS_DOWNLOAD = "PROCESS_CIRCLE_FOR_PRESS_DOWNLOAD";
	private static final String PRESS_DOWNLOAD_KEY_URL_MAP = "PRESS_DOWNLOAD_KEY_URL_MAP";
	private static final String PRESS_DOWNLOAD_URL_HIT_RETRY = "PRESS_DOWNLOAD_URL_HIT_RETRY";
	
	private static final String URL_FAILURE = "URL_FAILURE";
	
	private RBTDaemonManager m_mainDaemonThread = null;
	private RBTDBManager m_rbtDBManager = null;
	private RBTCacheManager rbtCacheManager = null;
	private ParametersCacheManager m_rbtParamCacheManager = null;
	
	private Set<RbtSupport> failureRbtSupportSet = new HashSet<RbtSupport>();
	private long latestSequenceId = 0;

	private Map<String,String> keyUrlMap = null;
	private List<String> process_circle;
	private List<String> keyList = null;
	private int urlHitRetry = 0;

	protected RbtSupportDaemon(RBTDaemonManager mainDaemonThread)
	{
		try
		{
			setName("RbtSupportDaemon");
			m_mainDaemonThread = mainDaemonThread;		
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RbtSupportDaemon", e);
		}
	}
	
	public void init(){
		m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();

		m_rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		
		process_circle = tokenizeArrayList(getParamAsString(PROCESS_CIRCLE_FOR_PRESS_DOWNLOAD, null), ",", true);

		keyList = new ArrayList<String>();
		keyUrlMap = new HashMap<String, String>();
		
		List<String> pressDownloadList = tokenizeArrayList(getParamAsString("COMMON", PRESS_DOWNLOAD_KEY_URL_MAP, null), ";", false);
		for(String value : pressDownloadList) {
			String[] arr = value.split(",");
			if(arr.length == 2) {
				String key = arr[0].toLowerCase();
				keyUrlMap.put(key,arr[1]);
				keyList.add(key);
			}
		}
		urlHitRetry = getParamAsInt(PRESS_DOWNLOAD_URL_HIT_RETRY, 3);

	}
	
	public void run() {
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) {
			
			try {
				processRbtSupportDatas();
			}
			catch(Exception e) {
				logger.error("Exception",e);
				latestSequenceId = 0;
				failureRbtSupportSet = new HashSet<RbtSupport>();
			}
			
			try
			{
				logger.info("Gift Thread Sleeping for 5 minutes............");
				Thread.sleep(getParamAsInt("SLEEP_INTERVAL_MINUTES",5) * 60 * 1000);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	private void processRbtSupportDatas() throws RBTException{
		List<RbtSupport> rbtSupportList = RbtSupportDao.getRbtSupports(latestSequenceId, RbtSupport.PROCESS_PENDING, 100);
		if(failureRbtSupportSet.size() > 0) {
			rbtSupportList.addAll(failureRbtSupportSet);
		}
		for(RbtSupport support : rbtSupportList) {
			long callerId = support.getCallerId();
			
			SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(String.valueOf(callerId), "DAEMON"));

			boolean allowHitUrl = false;
			
			String circleId = subscriberDetail.getCircleID();
			
			String response = null;
			//will hit third party url for only local circle only if circle is LOCAL
			if(subscriberDetail.isValidSubscriber() && process_circle != null && process_circle.size() == 1 && process_circle.contains("local")) {
				allowHitUrl = true;
			}			
			else if (circleId != null && process_circle != null && process_circle.size() == 1 && process_circle.contains("all")) {
				//will hit third party url for all circle if circle value is ALL
				allowHitUrl = true;				
			}
			else if (circleId != null && process_circle != null && process_circle.size() > 0 && process_circle.contains(circleId.toLowerCase())) {
				//will hit third party url for configured circle
				allowHitUrl = true;
			}
			else{
				response = "SUBSCRIBER NOT ALLOWED TO HIT URL";
			}
			
			
			String url = null;
			Map<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(support.getExtraInfo());
			if(allowHitUrl && support.getRetryCount() < urlHitRetry) {
				if(support.getRetryCount() == 0) {
					latestSequenceId = support.getId();
				}
				
				Clip clip = rbtCacheManager.getClip(support.getClipId());
				
				if(clip == null) {
					response = "CLIP_NOT_EXISTS";
				}
				else {
					url = makeUrl(support.getSubscriberId(), support.getCallerId(), support.getClipId(), clip.getClipPromoId(), extraInfoMap.get("KEY").toLowerCase());
					response = hitThirdPartyUrl(url);
				}
				 
				if(response.equalsIgnoreCase(URL_FAILURE)) {
					support.setRetryCount(1+support.getRetryCount());
					failureRbtSupportSet.add(support);
//					continue;
				}
				
			}
			
			if(!response.equalsIgnoreCase(URL_FAILURE) || support.getRetryCount() >= urlHitRetry) {
				transLogger.info("URL not processed: " + url + " Response: " + response + " caller: " + support.getCallerId() + " called: " + support.getSubscriberId() + " clipId: " + support.getClipId() + " keyPressed: " + extraInfoMap.get("KEY") + " Retried: " + support.getRetryCount());
				failureRbtSupportSet.remove(support);
				RbtSupportDao.delete(support);	
			}
		}
	}
	
	private String hitThirdPartyUrl(String url) throws RBTException{
		String response = URL_FAILURE;

		HttpParameters httpParam = new HttpParameters();
		httpParam.setUrl(url);
		httpParam.setConnectionTimeout(6000);
		
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParam, null);
			response = httpResponse.getResponse();
			if(httpResponse.getResponseCode() != 200) {
				response = URL_FAILURE;
			}
		}
		catch(Exception e) {
			logger.error("Exception while hitting url: " +  url, e);			
		}
		return response;
	}
	
	private String makeUrl(long subscriberId, long callerId, int clipId, String promoId, String key) throws RBTException {
		String url = null;
		if(key != null) {
			
			for(String temp : keyList) {
				if(key.indexOf(temp) != -1) {
					url = keyUrlMap.get(temp);
					break;
				}
			}			
		}
		
		if(url == null || (url = url.trim()).length() == 0 || url.equalsIgnoreCase("null")) {
			throw new RBTException("Either url is not configured properly or key is not configured under parameter " + PRESS_DOWNLOAD_KEY_URL_MAP + "key: " + key + " subscriberId: " + subscriberId + " callerId: " + callerId);
		}

		url = url.replaceAll("<caller>", String.valueOf(callerId));
		url = url.replaceAll("<called>", String.valueOf(subscriberId));
		url = url.replaceAll("<clipid>", String.valueOf(clipId));
		url = url.replaceAll("<promoid>", promoId);
		
		return url;
	}
	
	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal+"").getValue();
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	
	private String getParamAsString(String type, String param, String defaultVal)
	{
		try{
			String paramVal = m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
			return paramVal;   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}
	
	private String getParamAsString(String param, String defaultVal)
	{
		return getParamAsString("DAEMON", param, defaultVal);
	}

	private boolean getParamAsBoolean(String param, String defaultVal)
	{
		try{
			return m_rbtParamCacheManager.getParameter("DAEMON", param, defaultVal).getValue().equalsIgnoreCase("TRUE");
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}

	private static List<String> tokenizeArrayList(String stringToTokenize, String delimiter, boolean makeLowerCase)
	{
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens()) {
			String value = tokens.nextToken();
			if(makeLowerCase) {
				value = value.toLowerCase();
			}
			result.add(value);
		}

		return result;
	}
	
	public static void main(String args[]) {
		try {
			RbtSupportDaemon daemon = new RbtSupportDaemon(null);
			for(int i = 0; i < 5; i++) {
				daemon.processRbtSupportDatas();
			}
		}
		catch(Exception e) {
			logger.error(e);
		}
	}
}
