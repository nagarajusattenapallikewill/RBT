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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;


/**
 * Servlet implementation class Promotion
 */
public class PromotionPreConsent extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;
	
	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PromotionPreConsent()
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
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_Promotion);

		Utility.sqlInjectionInRequestParam(requestParams);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Promotion);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		try
		{
			responseText = AdminFacade.processPromotionPreConsentRequest(requestParams);
			//responseText = getModifiedResponse(responseText, requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Sms.class).error("RBT:: " + e.getMessage(), e);
		}

		if (!requestParams.containsKey("ENCODING") || requestParams.get("ENCODING") == null)
		{
			if (contentType != null)
				response.setContentType(contentType);
		}
		else
		{
			response.setContentType ("text/html;charset="+requestParams.get("ENCODING"));
		}
		Logger.getLogger(Sms.class).info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	private String getModifiedResponse(String responseText,
			HashMap<String, String> requestParams)
	{
		
		String taskAction = requestParams.get("TASK_ACTION");
		String alteredResponseReq = requestParams.get("ALTER_RESPONSE");
		String modifiedResponseRequired = requestParams.get("ALTER_STATUS_RES");
		String pack = null;
		if(taskAction != null && taskAction.equalsIgnoreCase("status") && modifiedResponseRequired != null)
		{
			if(responseText == null)
				responseText = "ERROR#OTHER";
			else if (responseText.equalsIgnoreCase(Resp_Inactive))
				responseText = "NONACTIVE";
			else if (responseText.indexOf("ACTIVE_") != -1)
			{
				pack = requestParams.get("PACK_STATUS");
				responseText = "ACTIVE";
			}
			else
				responseText = "ERROR#OTHER";
			if(requestParams.get("STATUS_DATE") != null && responseText.indexOf("ERROR") == -1)
				responseText = responseText + "#"+requestParams.get("STATUS_DATE");
			if(pack != null)
				responseText = responseText + "#" + pack;
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(action_activate) && alteredResponseReq!=null&&alteredResponseReq.equalsIgnoreCase("true")){
				responseText = Utility.getConfiguredResponse(responseText,action_activate);
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(action_deactivate) && alteredResponseReq!=null&&alteredResponseReq.equalsIgnoreCase("true")){
				responseText = Utility.getConfiguredResponse(responseText,action_deactivate);
		}
		return responseText;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
