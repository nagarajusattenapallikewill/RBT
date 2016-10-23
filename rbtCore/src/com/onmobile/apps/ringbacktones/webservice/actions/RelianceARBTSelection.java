/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author Sreekar
 */
public class RelianceARBTSelection implements WebServiceAction, WebServiceConstants, iRBTConstant {

	private static final Logger _logger = Logger.getLogger(RelianceARBTSelection.class);

	private static final String DEFAULT_CHARGE_CLASS = "DEFAULT";

	private static final String RESP_SELECTION_SUCCESS = "2000";
	private static final String RESP_SELECTION_ERROR = "2100";
	private static final String RESP_SELECTION_INACTIVE = "2101";
	private static final String RESP_SELECTION_LOW_BALANCE = "2102";
	private static final String RESP_SELECTION_SONG_ALREADY_EXISTS = "2103";
	private static final String RESP_SELECTION_INVALID_CONTENT = "2104";
	private static final String RESP_SELECTION_RETRY = "2200";

	public static final Map<String, String> _responseMap = new HashMap<String, String>();

	private static final ParametersCacheManager _parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
	private static final ChargeClassCacheManager _chargeClassCacheManager = CacheManagerUtil.getChargeClassCacheManager();

	private static RBTHttpClient _rbtHttpClient;
	
	private static final DateFormat _formatter = new SimpleDateFormat("yyyyMMddHHHmmss");

	static {
		initHttpClient();
		initResponseMap();
	}
	
	private static void initHttpClient() {
		HttpParameters httpParameters = new HttpParameters();

		String param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_CONNECTION_TIMEOUT_MS", "1000");
		httpParameters.setConnectionTimeout(Integer.parseInt(param));

		param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_SOCKET_TIMEOUT_MS", "0");
		httpParameters.setSoTimeout(Integer.parseInt(param));

		param = _parametersCacheManager.getParameterValue(COMMON, "ARBT_PROXY", null);
		if (param != null) {
			int index = param.indexOf(":");
			httpParameters.setProxyHost(param.substring(0, index));
			httpParameters.setProxyPort(Integer.parseInt(param.substring(index + 1)));
		}
		_rbtHttpClient = new RBTHttpClient(httpParameters);
	}
	
	private static void initResponseMap() {
		_responseMap.put(RESP_SELECTION_SUCCESS, "SUCCESS");
		_responseMap.put(RESP_SELECTION_ERROR, "ERROR");
		_responseMap.put(RESP_SELECTION_INACTIVE, "INACTIVE");
		_responseMap.put(RESP_SELECTION_LOW_BALANCE, "LOW_BALANCE");
		_responseMap.put(RESP_SELECTION_SONG_ALREADY_EXISTS, "SONG_ALREADY_EXISTS");
		_responseMap.put(RESP_SELECTION_INVALID_CONTENT, "INVALID_CONTENT");
		_responseMap.put(RESP_SELECTION_RETRY, "RETRY");
	}
	
	private WebServiceResponse getWebServiceResponse(String response) {
		WebServiceResponse webserviceResponse = new WebServiceResponse(_responseMap.get(response));
		webserviceResponse.addResponseHeader("_status", response);
		webserviceResponse.setResponseWriter(WebServiceResponseFactory.getResponseWriter(StringResponseWriter.class));
		return webserviceResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones
	 * .webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		_logger.info("starting with context " + webServiceContext);
		String subscriberId = webServiceContext.getString(param_msisdn);
		if (subscriberId == null) {
			_logger.info("no subscriber id passed");
			return getWebServiceResponse(RESP_SELECTION_ERROR);
		}

		String status = null;
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		if (subscriber != null)
			status = subscriber.subYes();
		if (status == null || !status.equals(STATE_ACTIVATED)) {
			_logger.info("not an active subscriber. status is " + status);
			return getWebServiceResponse(RESP_SELECTION_INACTIVE);
		}
		webServiceContext.put(param_subscriber, subscriber);
		
		String chargeClassStr = webServiceContext.getString(param_chargeclass);
		if(chargeClassStr != null) {
			ChargeClass chargeClass = _chargeClassCacheManager.getChargeClass(chargeClassStr);
			if(chargeClass == null) {
				_logger.info("invalid chargeclass " + chargeClassStr);
				return getWebServiceResponse(RESP_SELECTION_ERROR);
			}
		}
		else
			chargeClassStr = DEFAULT_CHARGE_CLASS;
		webServiceContext.put(param_chargeclass, chargeClassStr);
		
		String clipPromoId = webServiceContext.getString(param_contentid);
		if(clipPromoId == null) {
			_logger.info("no contentid passed ");
			return getWebServiceResponse(RESP_SELECTION_ERROR);
		}
		Clip clip = RBTCacheManager.getInstance().getClipByPromoId(clipPromoId);
		if(clip == null) {
			_logger.info("invalid contentid " + clipPromoId);
			return getWebServiceResponse(RESP_SELECTION_ERROR);
		}
		webServiceContext.put(param_clip, clip);
		
		SubscriberDownloads download = RBTDBManager.getInstance().getActiveSubscriberDownload(subscriberId, clip.getClipRbtWavFile());
		if(download != null) {
			_logger.info("download already exists");
			return getWebServiceResponse(RESP_SELECTION_SONG_ALREADY_EXISTS);
		}
		
		String categoryIdStr = webServiceContext.getString(param_categoryid);
		int categoryId = 3;
		if(categoryIdStr != null) {
			try {
				categoryId = Integer.parseInt(categoryIdStr);
			}
			catch(Exception e) {
				_logger.info("invalid categoryid " + categoryIdStr);
				return getWebServiceResponse(RESP_SELECTION_ERROR);
			}
		}
		Category category = RBTCacheManager.getInstance().getCategory(categoryId);
		if(category == null) {
			_logger.info("no category with id " + categoryId);
			return getWebServiceResponse(RESP_SELECTION_ERROR);
		}
		webServiceContext.put(param_category, category);

		// insert into DB
		insertIntoDB(webServiceContext, false, true);

		String smResponse = hitSM(webServiceContext);
		
		// if failure from SM delete from DB
		if(!smResponse.equals(RESP_SELECTION_SUCCESS)) {
			RBTDBManager.getInstance().deleteSubscriberDownload(subscriberId, clip.getClipRbtWavFile(),
					category.getCategoryId(), category.getCategoryTpe());
		}
		else
			insertIntoDB(webServiceContext, true, false);
		
		return getWebServiceResponse(smResponse);
	}
	
	private void insertIntoDB(WebServiceContext webServiceContext, boolean insertSelection, boolean insertDownload) {
		String subscriberId = webServiceContext.getString(param_msisdn);
		String mode = webServiceContext.getString(param_mode);
		if (mode == null)
			mode = "ARBT";
		String modeInfo = mode;
		if (webServiceContext.containsKey(param_ipAddress))
			modeInfo = mode + ":" + webServiceContext.getString(param_ipAddress);
		webServiceContext.put(param_mode, mode);
		webServiceContext.put(param_modeInfo, modeInfo);
		String chargeClass = webServiceContext.getString(param_chargeclass);
		boolean isPrepaid = true;
		String subType = webServiceContext.getString(param_subtype);
		if (subType != null && subType.toLowerCase().startsWith("post"))
			isPrepaid = false;

		Clip clip = (Clip) webServiceContext.get(param_clip);
		Category category = (Category) webServiceContext.get(param_category);

		String callerId = webServiceContext.getString(param_callerid);
		if (callerId == null || callerId.equalsIgnoreCase("all"))
			callerId = null;

		Subscriber subscriber = (Subscriber) webServiceContext.get(param_subscriber);

		boolean inLoop = false;
		String inLoopStr = webServiceContext.getString(param_inloop);
		if (inLoopStr != null && inLoopStr.equalsIgnoreCase("true"))
			inLoop = true;

		if (insertDownload) {
			RBTDBManager.getInstance().addSubscriberDownload(subscriberId, clip.getClipRbtWavFile(),
					category.getCategoryId(), null, true, category.getCategoryTpe(), chargeClass, mode, modeInfo);

			SubscriberDownloads download = RBTDBManager.getInstance().getActiveSubscriberDownload(subscriberId,
					clip.getClipRbtWavFile());
			webServiceContext.put(param_downloadRefID, download.refID());
		}
		if (insertSelection)
			RBTDBManager.getInstance().addSubscriberSelections(subscriberId, callerId, category.getCategoryId(),
					clip.getClipRbtWavFile(), null, null, null, 1, mode, modeInfo, 0, isPrepaid, false, null, 0, 2359,
					"SETTING", true, true, null, null, subscriber.subYes(), null, true, false, inLoop,
					subscriber.subscriptionClass(), subscriber, null, null);
	}
	
	public static final String getSMSubType(String subType) {
		if(subType == null || subType.toLowerCase().startsWith("pre"))
			return "P";
		return "B";
	}

	/**
	 * Invokes SM URL via HTTP
	 * 
	 * @param webServiceContext
	 * @return
	 */
	private String hitSM(WebServiceContext webServiceContext) {
		String response = RESP_SELECTION_RETRY;
		String url = _parametersCacheManager.getParameterValue(COMMON, "ARBT_SEL_URL", null);
		if (url == null) {
			_logger.fatal("ARBT selection url not configured");
			return response;
		}

		Subscriber subscriber = (Subscriber)webServiceContext.get(param_subscriber);
		
		String subscriberId = webServiceContext.getString(param_msisdn);
		String mode = webServiceContext.getString(param_mode);
		String chargeClass = webServiceContext.getString(param_chargeclass);
		String userType = getSMSubType(webServiceContext.getString(param_subtype));
		String modeInfo = webServiceContext.getString(param_modeInfo);
		String refId = webServiceContext.getString(param_downloadRefID);
		Clip clip = (Clip)webServiceContext.get(param_clip);
		Category category = (Category)webServiceContext.get(param_category);
		String caller = webServiceContext.getString(param_callerid);
		if(caller == null)
			caller = "all";
		String language = subscriber.language();
		if(language == null)
			language = "";

		url = url.replace("%MSISDN%", subscriberId);
		url = url.replace("%MODE%", mode);
		url = url.replace("%MODE_INFO%", modeInfo);
		url = url.replace("%USER_TYPE%", userType);
		url = url.replace("%CHARGE_CLASS%", chargeClass);
		url = url.replace("%REF_ID%", refId);
		url = url.replace("%LANGUAGE%", language);
		url = url.replace("%SONG_NAME%", clip.getClipName());
		url = url.replace("%SONG_CODE%", clip.getClipPromoId());
		url = replaceUrlValue(url, "%SONG_INFO%", clip.getClipInfo());
		url = replaceUrlValue(url, "%SONG_ARTIST%", clip.getArtist());
		url = replaceUrlValue(url, "%MOVIE_NAME%", clip.getAlbum());
		url = url.replace("%SONG_EXPIRY%", clip.getClipEndTime().toString());
		url = url.replace("%CALLER%", caller);
		url = url.replace("%SONG_ID%", clip.getClipId()+"");
		url = url.replace("%CATEGORY_ID%", category.getCategoryId()+"");
		url = url.replace("%CATEGORY_NAME%", category.getCategoryName());
		url = url.replace("%CATEGORY_TYPE%", category.getCategoryTpe()+"");
		
		
		_logger.info("arbt selection url " + url);
		
		HttpResponse httpResponse = null;

		Date startTime = new Date();
		try {
			httpResponse = _rbtHttpClient.makeRequestByGet(url, null);
		}
		catch (Exception e) {
			_logger.error("Exception while calling ARBT activation url " + e.getMessage(), e);
			return response;
		}
		finally {
			if (httpResponse != null) {
				String smResponse = httpResponse.getResponse();
				response = processSMResponse(smResponse);
			}

			Date endTime = new Date();
			StringBuilder sb = new StringBuilder();
			sb.append(subscriberId);
			sb.append(",");
			sb.append("ARBT_RT_DOWNLOAD");
			sb.append(",");
			sb.append(response);
			sb.append(",");
			sb.append(url);
			sb.append(",");
			if (httpResponse != null) {
				String smResponse = httpResponse.getResponse();
				smResponse = smResponse.replace("\n", "");
				smResponse = smResponse.replace("\r", "");
				smResponse = smResponse.replace("\t", "");
				sb.append(smResponse);
				sb.append("|");
				sb.append(httpResponse.getResponseCode());
			}
			else
				sb.append("EXCEPTION");
			sb.append(",");
			sb.append(_formatter.format(startTime));
			sb.append(",");
			sb.append(endTime.getTime() - startTime.getTime());

			RBTEventLogger.logEvent(RBTEventLogger.Event.ARBT, sb.toString());
		}
		
		return response;
	}
	
	/** 
	 * sets WebServiceResponse based on SM response
	 * 
	 * @param smResponse XML response received from SM for the real time subscription request
	 * @return
	 */
	private String processSMResponse(String smResponse) {
		_logger.info("SM response is " + smResponse);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(smResponse.getBytes()));
			
			Node statusNode = document.getElementsByTagName("status").item(0);
			String status = statusNode.getTextContent();
			
			Node reasonNode = document.getElementsByTagName("reason").item(0);
			String reason = reasonNode.getTextContent();
			
			if(status.equalsIgnoreCase("SUCCESS"))
				return RESP_SELECTION_SUCCESS;
			else if(reason.equalsIgnoreCase("LOW_BALANCE"))
				return RESP_SELECTION_LOW_BALANCE;
			else
				return RESP_SELECTION_ERROR;
		}
		catch (ParserConfigurationException e) {
			_logger.error(e.getMessage(), e);
		}
		catch (SAXException e) {
			_logger.error(e.getMessage(), e);
		}
		catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
		
		return RESP_SELECTION_RETRY;
	}
	
	public static String replaceUrlValue(String url, String target, String replacement) {
		if (replacement == null)
			replacement = "null";
		try {
			return url.replace(target, URLEncoder.encode(replacement, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			_logger.error(e.getMessage(), e);
		}
		return url;
	}
}