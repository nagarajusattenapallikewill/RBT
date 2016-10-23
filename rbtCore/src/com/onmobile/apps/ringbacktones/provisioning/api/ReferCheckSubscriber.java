package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class ReferCheckSubscriber extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ReferCheckSubscriber.class);

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReferCheckSubscriber()
	{
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
		
	}

	private String getFinalResponse(String inputxml, String responseText)
	{
		HashMap<String,String> xmlMap = Utility.getMapFromXML("Check" ,inputxml);
		String subscriber = null;
		String date = null;
		logger.info("The xmlMap is " + xmlMap);
		if(xmlMap == null)
		{
			responseText = responseText.replaceAll("%status%",refer_UNSPECIFIED);
		}
		if(xmlMap != null && xmlMap.size()>0)
		{
			if(xmlMap.containsKey(refer_MSISDN))
			{
				subscriber = xmlMap.get(refer_MSISDN);
			}
			if(xmlMap.containsKey(refer_DateTime))
			{
				date = xmlMap.get(refer_DateTime);
			}
			
			if(subscriber == null || date == null)
			{
				responseText = responseText.replaceAll("%status%",refer_UNSPECIFIED);
			}
			else
			{
				responseText = responseText.replaceAll("%msisdn%",subscriber);
				responseText = responseText.replaceAll("%datetime%",date);
			}
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber);
			Subscriber sub = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
			logger.info("The subscriber object is " + sub);
			if(sub == null)
			{
				responseText = responseText.replaceAll("%status%",refer_UNSUB);
			}
			else if(!Utility.isUserActive(sub.getStatus()))
			{
				responseText = responseText.replaceAll("%status%",refer_UNSUB);
			}
			else if(sub.getStatus().equalsIgnoreCase(SUSPENDED))
			{
				responseText = responseText.replaceAll("%status%",refer_UNSUB);
			}
			else if(sub.getStatus().equalsIgnoreCase("act_pending") || sub.getStatus().equalsIgnoreCase(RENEWAL_PENDING))
			{
				responseText = responseText.replaceAll("%status%",refer_PENDING_MSISDN);
			}
			else 
			{
				responseText = responseText.replaceAll("%status%",refer_SUB);
			}
			
			if(sub != null && sub.getSubscriberID() != null && sub.getSubscriptionClass() != null)
			{
				
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager()
				.getSubscriptionClass(sub.getSubscriptionClass());
				logger.info("The sub class is " + subClass);
				if(subClass != null)
				{
					responseText = responseText.replaceAll("%productid%",subClass.getOperatorCode1() != null ? subClass.getOperatorCode1() : "");
					responseText = responseText.replaceAll("%productname%",subClass.getOperatorCode2() != null ? subClass.getOperatorCode2() : "");
					responseText = responseText.replaceAll("%packid%",subClass.getOperatorCode3() != null ? subClass.getOperatorCode3() : "");
					responseText = responseText.replaceAll("%packname%",subClass.getOperatorCode4() != null ? subClass.getOperatorCode4() : "");
					responseText = responseText.replaceAll("%price%",subClass.getSubscriptionAmount() != null ? subClass.getSubscriptionAmount() : "");

				}
				
			}
			
			if(sub != null && sub.getSubscriberID() != null )
			{
				
				SubscriberStatus[] settings = RBTDBManager.getInstance().getAllActiveSubscriberSettings(sub.getSubscriberID());
				logger.info("The sub settings are " + settings);
				if(settings != null && settings.length>0)
				{
					if(settings[0].subscriberFile() != null)
					{
						Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(settings[0].subscriberFile());
						logger.info("Getting clip " + clip + " using wav file name " + settings[0].subscriberFile() );
						if(clip != null)
						{
							responseText = responseText.replaceAll("%content%",clip.getClipRbtWavFile() != null ? clip.getClipRbtWavFile().substring(4, clip.getClipRbtWavFile().length()-4) : "");
							responseText = responseText.replaceAll("%contentname%",clip.getClipName() != null ? clip.getClipName() : "");
							responseText = responseText.replaceAll("%contentexpiry%",clip.getClipEndTime().toString() != null ? clip.getClipEndTime().toString() : "");
						}
					}
					
				}
				
			}
			
		}
		return responseText;
	
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		response.setContentType("text/xml; charset=utf-8");
		String xml = (String)request.getParameter("xml");
		String responseXml = null;
		
		String responseText = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE","REFER_CHECK_RESPONSE_XML", null);
		responseXml = responseText.replaceAll("%status%", refer_UNSPECIFIED);
		
		logger.info("The input xml is " + xml);
		if(xml != null)
		{
			responseXml = getFinalResponse(xml, responseText);
		}
		
		responseXml = responseXml.replaceAll("%[a-z]*\\%", "");
		logger.info("RBT:: responseText: " + responseXml);
		response.getWriter().write(responseXml);
		
	}
}
