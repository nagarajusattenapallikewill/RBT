package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Servlet implementation class BulkUploadTask
 */
public class BulkUploadTask extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(BulkUploadTask.class);

	public BulkUploadTask() 
	{
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String responseText = Utility.getErrorXML();

		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_BulkUploadTask);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);

			responseText = RBTAdminFacade.getBulkProcessTaskResponse(task);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}
}
