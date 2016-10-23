package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet implementation class for Servlet: Subscription
 *
 */
public class Subscription extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Subscription.class);

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Subscription()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Recieved Subscription request. Requst parameters are: "
				+ request.getQueryString());
		String responseText = Utility.getErrorXML();
		RBTNode node = null;
		String msisdn = null;
		RBTMonitorManager monitorManager = RBTMonitorManager.getInstance();
		try {
			HashMap<String, String> requestParams = Utility
					.getRequestParamsMap(getServletConfig(), request, response,
							api_Subscription);
			if (monitorManager.validWebServiceNode(requestParams
					.get(param_mode))) {
				msisdn = requestParams.get(param_subscriberID);
				node = RBTMonitorManager.getInstance().startNode(msisdn,
						requestParams.get(param_mode));
			}
			WebServiceContext task = Utility.getTask(requestParams);
			/*
			 * There is a feature like a subscriber can have any number of child
			 * subscribers. When subscriber is updating his/her child subscriber
			 * numbers the tone player should be informed to update the same at
			 * tone player side. The player update daemon will update tone
			 * player only for the subscribers whose player status is A
			 */
			
			Parameters modeIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "MODE_IP_MAPPING_FOR_CONSENT", null);
			if(modeIPParam != null) {
				String ipAddress = request.getRemoteAddr();
				task.put(param_ipAddressConsent, ipAddress);
			}
			
			if(task.containsKey("userInfo_CHILD_MDN")) {
				task.put(param_playerStatus, "A");
			}

			logger.debug("RBT:: task: " + task);
			
			String simpleResponse = String.valueOf(request
					.getParameter("simpleResponse"));

			// If it is requested for simple response, the response should be plain text.
			// Otherwise it is an XML response.
			if ("y".equalsIgnoreCase(simpleResponse)) {
				responseText = RBTAdminFacade.getSubscriptionResponse(task).toUpperCase();
				logger.debug("Generating plain text response: " + responseText);
			} else {
				response.setContentType("text/xml; charset=utf-8");
				responseText = RBTAdminFacade.getSubscriptionResponseXML(task);
				logger.debug("Generating XML response: " + responseText);;
			}

		} catch (Exception e) {
			logger.error("Unable to process Subscription request. Exception: "
					+ e.getMessage(), e);
		}

		RBTMonitorManager.getInstance().endNode(msisdn, node, responseText);
		
		logger.info("Request: " + request.getQueryString()
				+ ", response: " + responseText);
				
		response.getWriter().write(responseText);
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}