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

import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class SelectionConsentIntegration extends HttpServlet implements WebServiceConstants {
	private static final long serialVersionUID = 1L;
	
	private ServletConfig servletConfig = null;
	private static Logger logger = Logger.getLogger(SelectionConsentIntegration.class);

	public SelectionConsentIntegration()
	{
		super();
	}

	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	
		response.setContentType("text/xml; charset=utf-8");
		String responseText = Utility.getErrorXML();
		RBTNode node = null;
		String msisdn = null;
		RBTMonitorManager monitorManager = RBTMonitorManager.getInstance();

		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, "Consent");
			Utility.sqlInjectionInRequestParam(requestParams);
			//RBT-16138 Unable to make selection ->nullpointer exception is thrown
			requestParams.put("URL", "SelectionPreConsentInt.do");
			responseText = AdminFacade.getConsentSelIntegrationResponseXML(requestParams);
			if(monitorManager.validWebServiceNode(requestParams.get(param_mode))) {
				msisdn = requestParams.get(param_subscriberID);
				node = RBTMonitorManager.getInstance().startNode(msisdn, requestParams.get(param_mode));
			}
			logger.info("Request received with params: " + requestParams);
		}
		catch (Exception e)
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}

		logger.info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
