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
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;

/**
 * Servlet implementation class Sms
 */
public class Sms extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Sms()
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
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_Sms);

		RBTNode node = RBTMonitorManager.getInstance().startNode(requestParams.get(param_subID), RBTNode.NODE_SMS);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Sms);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		
		try
		{
			responseText = AdminFacade.processSmsRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Sms.class).error("RBT:: " + e.getMessage(), e);
		}

		Logger.getLogger(Sms.class).info("RBT:: responseText: " + responseText);
		RBTMonitorManager.getInstance().endNode(requestParams.get(param_subID), node, responseText);
		
		if (!requestParams.containsKey("ENCODING") || requestParams.get("ENCODING") == null)
		{
			if (contentType != null)
				response.setContentType(contentType);
		}
		else
		{
			response.setContentType ("text/html;charset="+requestParams.get("ENCODING"));
		}

		if(responseText!=null)
			response.getWriter().write(responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
