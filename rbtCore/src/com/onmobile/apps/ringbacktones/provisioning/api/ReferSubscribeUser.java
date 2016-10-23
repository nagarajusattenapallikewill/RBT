package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Servlet implementation class Copy
 */
public class ReferSubscribeUser extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ReferSubscribeUser.class);


	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReferSubscribeUser()
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		response.setContentType("text/xml; charset=utf-8");
		String xml = (String)request.getParameter("xml");
		String responseXml = null;
		
		String responseText = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE","REFER_SUBSCRIBE_RESPONSE_XML", null);
		
//		"<Subscribe>
//		<STATUS>OK</STATUS>
//		</Subscribe>


		if(xml != null)
		{
			responseXml = getFinalResponse(xml, responseText);
		}
		
		responseXml.replaceAll("%status%","ERROR");
		logger.info("RBT:: responseText: " + responseXml);
		response.getWriter().write(responseXml);
		
	}
	
	private String getFinalResponse(String inputxml, String responseText)
	{
		HashMap<String,String> xmlMap = Utility.getMapFromXML("Subscribe" ,inputxml);
		String subscriber = null;
		boolean needToActivate = false;
		boolean needToMakeSelection = false;
		boolean isSubClassPresent = false;
		Clip clip = null;
		Category category = null;
		SubscriptionClass subclass = null;
		String response = "ERROR";
		String defaultCateg = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON","REFER_DEFAULT_CATEGORY", null);

		logger.info("The xmlMap is " + xmlMap);
		if(xmlMap == null)
		{
			responseText = responseText.replaceAll("%status%","ERROR");
			return responseText;
		}
		if(xmlMap != null && xmlMap.size()>0)
		{

			if(xmlMap.containsKey(refer_MSISDN))
			{
				subscriber = xmlMap.get(refer_MSISDN);
			}
			else
			{
				return responseText.replaceAll("%status%","ERROR");
			}

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber);
			Subscriber sub = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
			if(sub == null)
			{
				return responseText.replaceAll("%status%","ERROR");
			}

			if(xmlMap.containsKey(refer_PackName) && xmlMap.get(refer_PackName) != null && !xmlMap.get(refer_PackName).equals(""))
			{
				List<SubscriptionClass> subclassList = CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses();
				for(int i=0;i<subclassList.size();i++)
				{
					if(subclassList.get(i) != null && subclassList.get(i).getOperatorCode4() != null)
					{
						if(subclassList.get(i).getOperatorCode4().equalsIgnoreCase(xmlMap.get(refer_PackName)))
						{
							isSubClassPresent = true;
							subclass = subclassList.get(i);
							logger.info("Subclass is present " + subclass);
						}
					}
				}
			}

			if(!Utility.isUserActive(sub.getStatus()))
			{
				// If user is new user activate him , if content Id is valid activate & make selection
				logger.info("User is not active need to activate him");
				needToActivate = true;

			}
			else if(isSubClassPresent)
			{
				needToActivate = true;
			}


			if(xmlMap.containsKey(refer_Content) && xmlMap.get(refer_Content) != null && !xmlMap.get(refer_Content).equals(""))
			{
				String clipName = xmlMap.get(refer_Content);
				clip = RBTCacheManager.getInstance().getClipByRbtWavFileName("rbt_"+clipName+"_rbt");
				logger.info("the clip corresponding to request is " + clip);
				if(clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis())
				{
					responseText = responseText.replaceAll("%status%",refer_CONTENT_EXPIRE);
				}

				if(clip == null)
				{

					category = RBTCacheManager.getInstance().getCategoryByPromoId(clipName);
					if(category != null && category.getCategoryEndTime().getTime() < System.currentTimeMillis())
					{
						responseText = responseText.replaceAll("%status%",refer_CONTENT_EXPIRE);
					}
					if(category == null)
					{
						if(!needToActivate)
							return responseText;
					}
				}
				if(clip != null || category!=null)
					needToMakeSelection = true;
				else if(clip == null && category == null)
					responseText = responseText.replaceAll("%status%",refer_CONTENT_INVALID);
			}




			if(needToMakeSelection)
			{
				SelectionRequest selectionRequest = new SelectionRequest(sub.getSubscriberID());
				if(clip != null)
				{
					selectionRequest.setClipID(clip.getClipId()+"");
					selectionRequest.setCategoryID(defaultCateg);
				}
				else
				{
					selectionRequest.setCategoryID(category.getCategoryId()+"");
				}
				logger.info(" Need to make referal selection , selection request is " + selectionRequest);
				if(isSubClassPresent)
					selectionRequest.setRentalPack(subclass.getSubscriptionClass());
				RBTClient.getInstance().addSubscriberSelection(selectionRequest);
				response = selectionRequest.getResponse();
				if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
					responseText = responseText.replaceAll("%status%","OK");
				else
				{
					responseText = responseText.replaceAll("%status%",FAILURE);
				}

			}
			else 
			{
				SubscriptionRequest subscriptionRequest = new SubscriptionRequest(sub.getSubscriberID());
				if(isSubClassPresent)
					subscriptionRequest.setRentalPack(subclass.getSubscriptionClass());
				logger.info(" Need to make referal subscription , subscription request is " + subscriptionRequest);
				RBTClient.getInstance().activateSubscriber(subscriptionRequest);
				response = subscriptionRequest.getResponse();
				if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
					responseText = responseText.replaceAll("%status%","OK");
				}
				else
				{
					responseText = responseText.replaceAll("%status%",FAILURE);
				}


			}
		}
		return responseText;
}
}
