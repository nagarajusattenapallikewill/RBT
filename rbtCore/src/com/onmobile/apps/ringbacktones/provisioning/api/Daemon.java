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

public class Daemon extends HttpServlet implements Constants 
{
	private static final long serialVersionUID = 1L;
	private ServletConfig servletConfig = null;
       
    public Daemon() 
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
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_Sms);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Sms);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);
		Logger.getLogger(Copy.class).error("RBT:: " + requestParams);
		try
		{
			responseText = AdminFacade.processDirectActivationRequest(requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Copy.class).error("RBT:: " + e.getMessage(), e);
		}
		
		Logger.getLogger(Copy.class).info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	
}
