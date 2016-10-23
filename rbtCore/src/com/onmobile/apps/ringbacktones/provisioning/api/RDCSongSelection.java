package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RDCSongSelection extends HttpServlet implements Constants
{
	private static Logger logger = Logger.getLogger(RDCSongSelection.class);
    private static List<String> modesSupportedForDirectSelList = null;
	private static final long serialVersionUID = 1L;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		String modesSupportedForDirectSel = RBTParametersUtils.getParamAsString("COMMON", "MODES_FOR_DIRECT_SELECTION", null);
		if(modesSupportedForDirectSel != null)
		   modesSupportedForDirectSelList = Arrays.asList(modesSupportedForDirectSel.split(",")); 
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
			logger.info("Request recieved with parameters: "
					+ request.getParameterMap());
			subscriberID = request.getParameter("MSISDN");
			String clipID = request.getParameter("TONE_ID");
			String categoryID = request.getParameter("CATEGORY_ID");
			String inLoop = request.getParameter("ADD_IN_LOOP");
			String subClass = request.getParameter("SUB_CLASS");
			String mode = request.getParameter("MODE");
            String modeInfo = request.getParameter("MODE_INFO");
            
            // Affiliate portal changes
            String useUiChargeClass = request.getParameter(param_USE_UI_CHARGE_CLASS);
            String chargeClass = request.getParameter(param_CHARGE_CLASS);
            
            String fromTime = request.getParameter("FROM_TIME");
            String fromTimeInMins = request.getParameter("FROM_TIME_MINUTES");
            String toTime = request.getParameter("TO_TIME");
            String toTimeInMins = request.getParameter("TO_TIME_MINUTES");
            
            String interval = request.getParameter("INTERVAL");
   
            String selStartTime = request.getParameter("SELECTION_START_TIME");
            String selEndTime = request.getParameter("SELECTION_END_TIME");	
            
            String callerID = request.getParameter("CALLER_ID");
            // Affiliate portal changes ends
	
            if (subscriberID == null || clipID == null)
				responseText = "INVALID PARAMETERS";
			else
			{
				HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, null);
				
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
				Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
				if (subscriber.isValidPrefix())
				{
					Clip clip = null;
					try
					{
						int toneID = Integer.parseInt(clipID);
						clip = RBTCacheManager.getInstance().getClip(toneID);
					}
					catch(Exception e)
					{
					}

					String smsNo = RBTParametersUtils.getParamAsString("SMS", "SMS_NO", "123456");
					CosDetails cos = null;
					if (!subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE) && subscriber.getCosID() != null)
						cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.getCosID());

					if (clip == null || clip.getClipEndTime().getTime() < System.currentTimeMillis())
					{
						responseText = "INVALID CLIP";
						String smsText = RBTParametersUtils.getParamAsString("SMS", "RDC_INTER_OP_SELECTION_FAILURE_SMS", "The song u have selected is invalid.");
						Tools.sendSMS(smsNo, subscriberID, smsText, false);
					}
					else if (!DataUtils.isContentAllowed(cos, clip))
					{
						responseText = "LITE USER PREMIUM BLOCKED";
						String smsText = RBTParametersUtils.getParamAsString("SMS", "RDC_INTER_OP_LITE_USER_PREMIUM_BLOCKED_SMS", "The song u have selected is premium content.");
						Tools.sendSMS(smsNo, subscriberID, smsText, false);
					}
					else
					{	
						if (canAllow(subscriber.getStatus()))
						{
							if(mode!=null && modesSupportedForDirectSelList!=null && modesSupportedForDirectSelList.contains(mode)){
								SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
								selectionRequest.setCategoryID(categoryID);
								selectionRequest.setClipID(clipID);
								selectionRequest.setMode(mode);
								selectionRequest.setInLoop(inLoop!=null?inLoop.equalsIgnoreCase("true"):false);
								selectionRequest.setModeInfo(modeInfo);
								selectionRequest.setSubscriptionClass(subClass);
								
								// Affiliate portal changes
								if(null != useUiChargeClass) {
									boolean isUseUiChargeClass = Boolean.valueOf(useUiChargeClass);
									selectionRequest.setUseUIChargeClass(isUseUiChargeClass);
								}
								
								if (null != chargeClass) {
									selectionRequest
											.setChargeClass(chargeClass);
								}
								
								if (null != fromTime) {
									selectionRequest.setFromTime(Integer
											.parseInt(fromTime));
								}

								if (null != toTime) {
									selectionRequest.setToTime(Integer
											.parseInt(toTime));
								}

								if (null != fromTimeInMins) {
									selectionRequest.setFromTimeMinutes(Integer
											.parseInt(fromTimeInMins));
								}

								if (null != toTimeInMins) {
									selectionRequest.setToTimeMinutes(Integer
											.parseInt(toTimeInMins));
								}

								if (null != interval) {
									selectionRequest.setInterval(interval);
								}
								
								if (null != selStartTime && null != selEndTime) {
									SimpleDateFormat sdf = new SimpleDateFormat(
											"yyyyMMddhhmmssSSS");
									Date startTime = sdf.parse(selStartTime);
									selectionRequest
											.setSelectionStartTime(startTime);
									Date endTime = sdf.parse(selEndTime);
									selectionRequest
											.setSelectionEndTime(endTime);
								}
								
								if(null != callerID) {
									selectionRequest.setCallerID(callerID);
								}
								
								logger.debug("Making selection request. selectionRequest: "
										+ selectionRequest);
								// Affiliate portal changes ends
								
								RBTClient.getInstance().addSubscriberSelection(selectionRequest);
								// add charge class
								responseText = selectionRequest.getResponse();
							}else{
								DataRequest dataRequest = new DataRequest(subscriberID, null, "RDC_SEL_PENDING");
								dataRequest.setClipID(clipID);
	
								HashMap<String, String> extraInfoMap = new HashMap<String, String>();
								if (categoryID != null)
									extraInfoMap.put("CATEGORY_ID", categoryID);
	
								if (inLoop != null)
									extraInfoMap.put("ADD_IN_LOOP", inLoop);
	
								if (subClass != null)
									extraInfoMap.put("SUB_CLASS", subClass);
	
								if (chargeClass != null)
									extraInfoMap.put("CHARGE_CLASS", chargeClass);
								
								if (useUiChargeClass != null)
									extraInfoMap.put("USE_UI_CHARGE_CLASS", useUiChargeClass.equalsIgnoreCase("true")?"y":"n");
								
								if (mode != null)
									extraInfoMap.put("MODE", mode);
								
								if(modeInfo!=null)
									extraInfoMap.put("MODE_INFO", modeInfo);
								
								dataRequest.setInfoMap(extraInfoMap);
								RBTClient.getInstance().addViralData(dataRequest);
								if (dataRequest.getResponse().equals(WebServiceConstants.SUCCESS))
								{
									String smsText = RBTParametersUtils.getParamAsString("SMS", "RDC_INTER_OP_SELECTION_OPTIN_SMS", "please send YES to 5545XX to set the <song> as your dialer tone");
									if (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.NEW_USER)
											|| subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.DEACTIVE))
									{
										smsText = RBTParametersUtils.getParamAsString("SMS", "RDC_INTER_OP_ACT_N_SELECTION_OPTIN_SMS", "please send YES to 5545XX to activate and set the <song> as your dialer tone");
									}
	
									if(smsText != null)
										smsText = smsText.replaceAll("<song>", clip.getClipName());
	
									Tools.sendSMS(smsNo, subscriberID, smsText, false);
									responseText = "SUCCESS";
								}
								else
								{
									responseText = "FAILURE";
									logger.warn("Could not insert into RBT_VIRAL_SMS table " + dataRequest);
								}
     						}
						}
						else
						{
							if (subscriber.getStatus().equals(WebServiceConstants.SUSPENDED))
								responseText = "FAILURE:USER IS SUSPENDED";
							else
								if (subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING) 
										|| subscriber.getStatus().equals(WebServiceConstants.DEACT_ERROR))
									responseText = "FAILURE:DEACT PENDING";
								else
									responseText = "FAILURE:USER NOT ALLOWED";
						}
					}
				}
				else
				{
					String redirectionURL = getRedirectionURL(subscriber);
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
						responseText = "FAILURE:INVALID PREFIX";
					}
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

	/**
	 * @param subscriberStatus
	 * @return
	 */
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

	/**
	 * @param subscriberID
	 * @return
	 */
	private String getRedirectionURL(Subscriber subscriber)
	{
		String redirectURL = null;

		String circleID = subscriber.getCircleID();
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
		logger.info("circleID: " + circleID + " redirectURL: Site " + sitePrefix);
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

				redirectURL += "rbt_rdc_song_selection.do?";
			}
		}
		logger.info("subscriberID: " + subscriber.getSubscriberID() + " redirectURL: " + redirectURL);
		return redirectURL;
	}
}
