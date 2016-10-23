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

public class RbtRdcToCgiSongSelection extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RbtRdcToCgiSongSelection()
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
		
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_startcopy);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_startcopy);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);

		try
		{
			requestParams.put("HittedUtl", request.getRequestURL() + "?" + request.getQueryString());
			responseText = AdminFacade.processRDCToCgiSongSelectionRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(StartCopy.class).error("StartCopy RBT:: " + e.getMessage(), e);
		}
		
		Logger.getLogger(StartCopy.class).info("StartCopy RBT:: StartCopy responseText: " + responseText);
		if(responseText!=null)
		     response.getWriter().write(responseText);
		else
			 response.getWriter().write(" Error Occured while Processing. ");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}

