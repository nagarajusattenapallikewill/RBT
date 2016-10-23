/**
 * OnMobile Ring Back Tone 
 * 
 * $Author: gautam.agrawal $
 * $Id: VodaCTservice.java,v 1.11 2013/07/13 10:58:41 gautam.agrawal Exp $
 * $Revision: 1.11 $
 * $Date: 2013/07/13 10:58:41 $
 */
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
 * Servlet implementation class VodaCTservice
 */
public class VodaCTservice extends HttpServlet implements Constants {
	
	private static final long serialVersionUID = -5073468296955372273L;

	private ServletConfig servletConfig = null;

	/*
	 * All the transactions made to VodaCTService will be logged to the file 
	 * which is declared for 'TRANSACTION_LOGGER'
	 */
	private static final Logger TRANSACTION_LOG = Logger.getLogger("TRANSACTION_LOGGER");
	
	private static final Logger LOG = Logger.getLogger(VodaCTservice.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VodaCTservice() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		this.servletConfig = servletConfig;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		LOG.debug("Recieved request: " + request.getQueryString());

		// Convert HTTP request parameters to HashMap.
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(
				servletConfig, request, response, api_VodaCTservice);
		Utility.sqlInjectionInRequestParam(requestParams);
		ResponseEncoder responseEncoder = AdminFacade
				.getResponseEncoderObject(api_service);
		
		String responseText = responseEncoder
				.getGenericErrorResponse(requestParams);
		
		String contentType = responseEncoder.getContentType(requestParams);
		String transactionID = requestParams.get(param_VODACT_TRANSID);
		
		try
		{
			responseText = AdminFacade.processVodaCTserviceRequest(requestParams);
			transactionID = requestParams.get(param_VODACT_TRANSID);
		}
		catch (Exception e)
		{
			LOG.error("RBT:: " + e.getMessage(), e);
		}

		LOG.info("Response: " + responseText);
		
		if (!requestParams.containsKey("ENCODING")
				|| requestParams.get("ENCODING") == null) {
			if (contentType != null) {
				response.setContentType(contentType);
			}
		} else {
			response.setContentType("text/html;charset="
					+ requestParams.get("ENCODING"));
		}

		if (responseText != null) {
			if (transactionID == null)
				transactionID = "null";

			responseText = responseText.replace("transID", transactionID);
			responseText = "RESPONSE_STATUS:" + responseText;
			response.getWriter().write(responseText);
		}


		TRANSACTION_LOG.info("REQ_REMOTE_ADDR:" + requestParams.get(param_ipAddress)
				+ "|REQ_START_TIME:" + requestParams.get(param_startTime)
				+ "|RESPONSE:" + responseText
				+ "|REQ_QUERY_STRING:" + requestParams.get(param_queryString));
		
		LOG.info("Final Response: " + responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
