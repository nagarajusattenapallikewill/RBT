package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.DTWebsiteEventLogger;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;
import com.onmobile.reporting.framework.capture.api.Configuration;
import com.onmobile.reporting.framework.capture.api.ReportingException;

/**
 * Servlet implementation class WebSongSelection
 */
public class WebSongSelection extends HttpServlet implements Constants
{
	private static Logger logger = Logger.getLogger(WebSongSelection.class);
	private static DTWebsiteEventLogger webLogger = null;
	private String dtWebsiteEventLoggingDir = RBTParametersUtils.getParamAsString("COMMON", "DT_WEBSITE_EVENT_LOGGING_DIR", null);
	private static final long serialVersionUID = 1L;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		if( dtWebsiteEventLoggingDir != null){

			new File(dtWebsiteEventLoggingDir).mkdirs();
			Configuration cfg = new Configuration(dtWebsiteEventLoggingDir);
			try {
				webLogger = new DTWebsiteEventLogger(cfg);
				logger.info("Loaded DT Website logger");
			} catch (IOException e) {
				logger.error("Unable to load dt website logger ", e);
			}

		}
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String responseText = "FAILED";
		String subscriberID = null;
		try
		{
			subscriberID = request.getParameter("MSISDN");
			if (subscriberID == null)
				subscriberID = request.getParameter("SUB_ID");

			String clipID = request.getParameter("TONE_ID");
			String callerID = request.getParameter("CALLER_ID");
			String categoryID = request.getParameter("CATEGORY_ID");
			String profileHours = request.getParameter("PROFILE_HOURS");
			String chargeClass = request.getParameter("CHARGE_CLASS");
			String useUIChargeClass = request.getParameter("USE_UI_CHARGE_CLASS");
			String subClass = request.getParameter("SUBSCRIPTION_CLASS");
			String actionType = request.getParameter("ACTION_TYPE");
			String smsNumber = request.getParameter("SMS_NUMBER");
			String mode = request.getParameter("MODE");
			
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, null);
			String redirectionURL = getRedirectionURL(subscriberID);
			if (redirectionURL != null)
			{
				HttpParameters httpParameters = new HttpParameters(redirectionURL);
				try
				{
					HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, requestParams);
					logger.info("httpResponse: " + httpResponse);

					responseText = httpResponse.getResponse();
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					responseText = "FAILURE";
				}
			}
			else 
			{
				SubscriberDetail subscriberDetails = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID, "RBT"));
				if(subscriberDetails == null || !subscriberDetails.isValidSubscriber()){
					responseText = "INVALID_NUMBER";
					response.getWriter().write(responseText);
					return;
				}

				if(smsNumber==null || smsNumber == "" ){
				    smsNumber = RBTParametersUtils.getParamAsString("SMS", "SMS_NUMBER", "123456");
				}

				if ("ACT".equalsIgnoreCase(actionType)) 
				{
					responseText = processActivationRequest(subscriberID, actionType,subClass,smsNumber,mode);
				}
				else if ("DCT".equalsIgnoreCase(actionType)) 
				{
					responseText = processDeActivationRequest(subscriberID, actionType,subClass,smsNumber);
				}
				else{
					responseText = processSelectionRequest(request, response,
							subscriberID, callerID, clipID, categoryID,
							profileHours, actionType, chargeClass,
							useUIChargeClass,subClass,smsNumber,mode);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			responseText = "FAILURE";
		}
		logger.info("subscriberID: " + subscriberID + " Response Text : " + responseText);
		response.getWriter().write(responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	private boolean canAllow(String subscriberStatus)
	{
		if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.NEW_USER)  		
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| subscriberStatus.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
			return true;

		return false;
	}

	private String getRedirectionURL(String subscriberID)
	{
		String redirectURL = null;

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);

		String circleID = subscriber.getCircleID();
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
//		logger.info("circleID: " + circleID + " redirectURL: Site " + sitePrefix);
		if (sitePrefix != null)
		{
			redirectURL = sitePrefix.getSiteUrl();
			if (redirectURL != null)
			{ 
				if (redirectURL.endsWith("?"))
					redirectURL = redirectURL.substring(0, redirectURL.length()-1);
				if (redirectURL.indexOf("/rbt_sms.jsp") != -1)
					redirectURL = redirectURL.substring(0, redirectURL.lastIndexOf("/rbt_sms.jsp"));

				if (!redirectURL.endsWith("/"))
					redirectURL += "/"; 

				redirectURL += "rbt_webselection.do?";
			}
		}
		logger.info("subscriberID: " + subscriberID + " redirectURL: " + redirectURL);
		return redirectURL;
	}

	private String processActivationRequest(String subscriberID, String actionType,String subClass,String smsNumber,String mode) throws OnMobileException{
		if(subscriberID == null)
			return "INVALID PARAMETERS";

		String responseText = "FAILED";
		DataRequest dataRequest = new DataRequest(subscriberID, null, "WEB_REQUEST");

		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		extraInfoMap.put("SMS_TYPE", "WEBSELECTION");

		if(actionType != null){
			extraInfoMap.put("ACTION_TYPE", actionType);
		}
		if(subClass!=null){
			extraInfoMap.put("SUBSCRIPTION_CLASS", subClass);			
		}
		if(mode!=null){
			extraInfoMap.put("MODE", mode);
		}

		dataRequest.setInfoMap(extraInfoMap);

		RBTClient.getInstance().addViralData(dataRequest);

		if (dataRequest.getResponse().equals(WebServiceConstants.SUCCESS))
		{
			String smsText = RBTParametersUtils.getParamAsString("SMS", "WEBSELECTION_OPTIN_SMS_ACT", 
					"please send YES to 5545XX to activate Ringbacktones");
            String prefix = prefixRequired(mode);
			Tools.sendSMS(smsNumber, subscriberID,  false,smsText,prefix);
			responseText = "SUCCESS";
		}
		else
		{
			responseText = "FAILURE";
			logger.warn("Could not insert into RBT_VIRAL_SMS table " + dataRequest);
		}
		
		return responseText;
	}
	
	private String processDeActivationRequest(String subscriberID, String actionType,String subClass,String smsNumber) throws OnMobileException{
		if(subscriberID == null)
			return "INVALID PARAMETERS";
		
		String responseText = "FAILED";
		DataRequest dataRequest = new DataRequest(subscriberID, null, "WEB_REQUEST");

		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		extraInfoMap.put("SMS_TYPE", "WEBSELECTION");

		if(actionType != null){
			extraInfoMap.put("ACTION_TYPE", actionType);
		}

		dataRequest.setInfoMap(extraInfoMap);

		RBTClient.getInstance().addViralData(dataRequest);

		if (dataRequest.getResponse().equals(WebServiceConstants.SUCCESS))
		{   
			String smsText = RBTParametersUtils.getParamAsString("SMS", "WEBSELECTION_OPTIN_SMS_DCT", 
					"please send YES to 5545XX to deactivate Ringbacktones");

			Tools.sendSMS(smsNumber, subscriberID, smsText, false);
			responseText = "SUCCESS";
		}
		else
		{
			responseText = "FAILURE";
			logger.warn("Could not insert into RBT_VIRAL_SMS table " + dataRequest);
		}
		
		return responseText;
	}
	
	private String processSelectionRequest(HttpServletRequest request, HttpServletResponse response, String subscriberID, String callerID,
			 String clipID, String categoryID, String profileHours, String actionType, String chargeClass, String useUIChargeClass,
			 String subClass,String smsNumber,String mode) throws OnMobileException, ReportingException{

		String responseText = "FAILED";
		if (subscriberID == null || clipID == null)
			return "INVALID PARAMETERS";
		

		Clip clip = null;
		try
		{
			int toneID = Integer.parseInt(clipID);
			clip = RBTCacheManager.getInstance().getClip(toneID);
		}catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		if(clip == null || clip.getClipEndTime().getTime() < System.currentTimeMillis())
		{
			if(webLogger != null)
			webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, "INVALID", "NA", "NO", "WEB","response:INVALIDCLIP" );
			return "INVALID CLIP";
		}
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);

		if (canAllow(subscriber.getStatus()) && !RBTDBManager.getInstance().isTotalBlackListSub(subscriberID))
		{	
			DataRequest dataRequest = new DataRequest(subscriberID, callerID, "WEB_REQUEST");
			dataRequest.setClipID(clipID);

			HashMap<String, String> extraInfoMap = new HashMap<String, String>();
			extraInfoMap.put("SMS_TYPE", "WEBSELECTION");
			if (categoryID != null)
				extraInfoMap.put("CATEGORY_ID", categoryID);

			if (profileHours != null)
				extraInfoMap.put("PROFILE_HOURS", profileHours);
			
			if(actionType != null)
				extraInfoMap.put("ACTION_TYPE", actionType);
			else
				extraInfoMap.put("ACTION_TYPE", "SEL");

			if (chargeClass != null)
				extraInfoMap.put("CHARGE_CLASS", chargeClass);
			if (useUIChargeClass != null)
				extraInfoMap.put("USE_UI_CHARGE_CLASS", useUIChargeClass);
			
			if(subClass!=null){
				extraInfoMap.put("SUBSCRIPTION_CLASS", subClass);
			}
			if(mode!=null){
				extraInfoMap.put("MODE", mode);
			}
			
			dataRequest.setInfoMap(extraInfoMap);

			RBTClient.getInstance().addViralData(dataRequest);

			if (dataRequest.getResponse().equals(WebServiceConstants.SUCCESS))
			{
				String smsText = RBTParametersUtils.getParamAsString("SMS", "WEBSELECTION_OPTIN_SMS", "please send YES <promo_code> to 5545XX to set the <song> as your dialer tone.");

				if(smsText != null)
					smsText = smsText.replaceAll("<song>", clip.getClipName());
				
				String promoId = clip.getClipPromoId();
				if(promoId != null){
					String[] promoIds = promoId.split(",");
					if(promoIds.length > 0)
						promoId = promoIds[0];
				}
				else{
					promoId = "";
				}
				
				if(smsText != null)
					smsText = smsText.replaceAll("<promo_code>", promoId);
				String prefix = prefixRequired(mode);
				Tools.sendSMS(smsNumber, subscriberID, false,smsText, prefix);
				responseText = "SUCCESS";
				if(webLogger != null)
				webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, clip.getClipName()!=null?clip.getClipName():"", promoId, "YES", "WEB","sms:"+smsText+"|response:"+responseText );
				
			}
			else
			{
				responseText = "FAILURE";
				if(webLogger != null)
				webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, clip.getClipName()!=null?clip.getClipName():"", clip.getClipPromoId()!=null?clip.getClipPromoId():"NA", "NO", "WEB","response:"+responseText );
				logger.warn("Could not insert into RBT_VIRAL_SMS table " + dataRequest);
			}
		}
		else
		{
			if (subscriber.getStatus().equals(WebServiceConstants.SUSPENDED))
			{
				responseText = "FAILURE:USER IS SUSPENDED";
				if(webLogger != null)
				webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, clip.getClipName()!=null?clip.getClipName():"", clip.getClipPromoId()!=null?clip.getClipPromoId():"NA", "NO", "WEB","response:"+responseText );
			}
			else if (subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING) 
						|| subscriber.getStatus().equals(WebServiceConstants.DEACT_ERROR)){
					responseText = "FAILURE:DEACT PENDING";
					if(webLogger != null)
					webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, clip.getClipName()!=null?clip.getClipName():"", clip.getClipPromoId()!=null?clip.getClipPromoId():"NA", "NO", "WEB","response:"+responseText );
				}
				else{
					responseText = "FAILURE:USER NOT ALLOWED";
					if(webLogger != null)
					webLogger.DTWebsiteLogger(new Timestamp(System.currentTimeMillis()), subscriberID, clip.getClipName()!=null?clip.getClipName():"", clip.getClipPromoId()!=null?clip.getClipPromoId():"NA", "NO", "WEB","response:"+responseText );
				}
		}
		return responseText;
	}
	
	private String prefixRequired(String mode){
		String prefix = null;
        if(mode!=null){
        	String confModes  =  RBTParametersUtils.getParamAsString("COMMON","MODES_FOR_SENDING_PREFIX",null);
        	if(confModes!=null){
        		ArrayList<String> list = new ArrayList<String>();
        		String modeArr[] = confModes.split(",");
        		if(modeArr!=null){
        			for(int i=0;i<modeArr.length;i++)
        			  list.add(modeArr[i]);	
        		}
        		prefix = list.contains(mode)?"y":"n";
        	}
        }
       return prefix;
	}
	
}
