package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.HashMap;

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
 * Servlet implementation class MOD
 */
public class ThirdParty extends HttpServlet implements Constants {
	private static final long serialVersionUID = 1L;
	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ThirdParty() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		this.servletConfig = servletConfig;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request,
				response, api_Promotion, false);
		String requestType = servletConfig.getInitParameter("RBT_REQUEST");
		RBTNode node = RBTMonitorManager.getInstance().startNode(requestParams.get(param_subID), requestType);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Promotion);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		if (contentType != null)
			response.setContentType(contentType);

		try {
			responseText = AdminFacade.processThirdPartyRequest(requestParams, requestType);
		}
		catch (Exception e) {
			Logger.getLogger(ThirdParty.class).error("RBT:: " + e.getMessage(), e);
		}

		if(requestType != null && requestType.equalsIgnoreCase("USSD")) {
			int index = responseText.indexOf(":");
			String statusStr = responseText.substring(0, index);
			responseText = responseText.substring(index + 1);
			int status = 200;
			try {
				status = Integer.parseInt(statusStr);
			}
			catch(Exception e) {

				Logger.getLogger(ThirdParty.class).error("RBT::exception-", e);
			}
			response.setStatus(status);
		}
		RBTMonitorManager.getInstance().endNode(requestParams.get(param_subID), node, responseText);
		Logger.getLogger(ThirdParty.class).info("RBT:: final responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}