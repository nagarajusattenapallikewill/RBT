package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

//RBT-7621:-Idea RBT Consent Logic Implementation for IVR
/**
 * This servlet class will receive request for consent selection pre-processing.
 */
public class SelectionPreConsent extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SelectionPreConsent.class);
	private static Logger selectionPreConsentLogger = Logger.getLogger("PreConsentLogger");

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public SelectionPreConsent()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/xml; charset=utf-8");
		String responseText = Utility.getErrorXML();
		RBTNode node = null;
		String msisdn = null;
		RBTMonitorManager monitorManager = RBTMonitorManager.getInstance();
		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Selection);
			if(monitorManager.validWebServiceNode(requestParams.get(param_mode))) {
				msisdn = requestParams.get(param_subscriberID);
				node = RBTMonitorManager.getInstance().startNode(msisdn, requestParams.get(param_mode));
			}
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("Request received with params: " + task);

			responseText = RBTAdminFacade.getConsentPreSelectionResponseXML(task);
			
			selectionPreConsentLogger.info(request.getRemoteAddr() + ", " + request.getQueryString() + ", " + responseText);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		RBTMonitorManager.getInstance().endNode(msisdn, node, responseText);

		logger.info("Response: " + responseText);
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