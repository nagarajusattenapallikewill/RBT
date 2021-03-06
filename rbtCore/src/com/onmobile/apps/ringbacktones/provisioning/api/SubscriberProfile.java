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
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;

/**
 * Servlet implementation class Copy
 */
public class SubscriberProfile extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public  SubscriberProfile()
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
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_subprof);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_service);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);

		try
		{
			responseText = AdminFacade.processSubProfileRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Copy.class).error("RBT:: " + e.getMessage(), e);
		}
		System.out.println("resp text.."+responseText); 
		Logger.getLogger(Copy.class).info("RBT:: responseText: " + responseText);
		if(response!=null)
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

