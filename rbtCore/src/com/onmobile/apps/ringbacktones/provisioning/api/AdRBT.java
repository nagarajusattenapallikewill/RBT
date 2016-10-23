package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.HashMap;

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
 * @author Sreekar
 * Servlet implementation class AdRBT.
 */
public class AdRBT extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;
	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdRBT()
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
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_Promotion, false);

		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Promotion);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);

		try
		{
			responseText = AdminFacade.processAdRBTRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Sms.class).error("RBT:: " + e.getMessage(), e);
		}

		Logger.getLogger(Sms.class).info("RBT:: responseText: " + responseText);
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
