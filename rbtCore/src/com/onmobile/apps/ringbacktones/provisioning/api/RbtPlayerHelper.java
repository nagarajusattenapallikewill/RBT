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
 * Servlet implementation class Copy
 */
public class RbtPlayerHelper extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RbtPlayerHelper()
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
		
		//call copy/cross copy/ cross copy rdc 
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_rbtplayhelp);
		RBTNode node = RBTMonitorManager.getInstance().startCopyNode(requestParams);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_service);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);

		try
		{
			responseText = AdminFacade.processRbtHelperRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(RbtPlayerHelper.class).error("RBT:: " + e.getMessage(), e);
		}
		System.out.println("resp text.."+responseText); 
		Logger.getLogger(RbtPlayerHelper.class).info("RBT:: responseText: " + responseText);
		RBTMonitorManager.getInstance().endCopyNode(requestParams, node, responseText);
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

