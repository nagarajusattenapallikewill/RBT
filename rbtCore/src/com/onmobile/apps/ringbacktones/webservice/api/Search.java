package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.content.RBTContentProviderFactory;

public class Search extends HttpServlet implements WebServiceConstants{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Search.class);

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Search()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */

	@Override
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException{
		response.setContentType("text/xml; charset=utf-8");
		String responseText = Utility.getErrorXML();
		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Search);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);
            task.put(param_action, api_Search);
			responseText = RBTContentProviderFactory.processContentRequest(task);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);

	}
	
	@Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException{
		   doGet(request,response);
	}

}
