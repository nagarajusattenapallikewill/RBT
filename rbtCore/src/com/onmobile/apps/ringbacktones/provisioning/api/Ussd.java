package com.onmobile.apps.ringbacktones.provisioning.api;

import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class Ussd extends HttpServlet implements Constants 
{
	static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;
	private static final String CLASSNAME = "USSD";

	public Ussd()
	{
		super();
	}

	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
		this.servletConfig = servletConfig;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_service);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_service);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);
		Logger.getLogger(Ussd.class).error("RBT:: " + requestParams);
		try
		{
			responseText = AdminFacade.processUSSDSubscriptionRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Ussd.class).error("RBT:: " + e.getMessage(), e);
		}

		Logger.getLogger(Ussd.class).info(CLASSNAME + "RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
