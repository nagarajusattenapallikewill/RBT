package com.onmobile.apps.ringbacktones.monitor.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.common.Utility;

/**
 * Servlet class for monitoring
 * 
 * @author Sreekar
 * @since 2010-01-19
 */
public class Monitor extends HttpServlet {
	private static Logger _logger = Logger.getLogger(Monitor.class);
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(request);
		_logger.info("RBT::starting monitor with params->" + requestParams);
		String responseStr = RBTMonitorManager.getInstance().monitor(requestParams);
		_logger.info("RBT::response->" + responseStr);
		String userAgent = request.getHeader("user-agent");
		if(userAgent != null)
			userAgent = userAgent.trim().toLowerCase();
		if (userAgent != null
				&& (userAgent.indexOf("msie") != -1 || userAgent.indexOf("firefox") != -1
						|| userAgent.indexOf("chrome") != -1 || userAgent.indexOf("opera") != -1)) {
			_logger.info("RBT::setting response as text/xml for userAgent->" + userAgent);
			response.setContentType("text/xml");
		}
		response.getWriter().write(responseStr);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}