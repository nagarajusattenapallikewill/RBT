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
public class EndMonitor extends HttpServlet {
	private static Logger _logger = Logger.getLogger(EndMonitor.class);
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(request);
		_logger.info("RBT::calling endMonitor monitor with params->" + requestParams);
		String responseStr = RBTMonitorManager.getInstance().endMonitor(requestParams);
		_logger.info("RBT::response->" + responseStr);
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