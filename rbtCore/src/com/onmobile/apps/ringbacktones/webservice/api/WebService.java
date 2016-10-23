package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction;
import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceActionFactory;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author sridhar.sindiri
 *
 */
public class WebService extends HttpServlet implements WebServiceConstants
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7320370701816846212L;

	private static Logger logger = Logger.getLogger(WebService.class);
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public WebService()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		WebServiceResponse webServiceResponse = null;
		String action = request.getParameter(WebServiceConstants.param_action);
		if (action == null)
		{
			action = getServletConfig().getInitParameter("action");
		}

		WebServiceAction webServiceAction = WebServiceActionFactory.getWebServiceActionProcessor(action);
		if (webServiceAction == null)
		{
			logger.error("Action name not registered: " + action);
			String responseString = Utility.getResponseXML(INVALID_ACTION);
			webServiceResponse = new WebServiceResponse(responseString);
			webServiceResponse.setContentType("text/xml; charset=utf-8");
			ResponseWriter responseWriter = WebServiceResponseFactory.getResponseWriter(StringResponseWriter.class);
			webServiceResponse.setResponseWriter(responseWriter);
		}
		else
		{
			HashMap<String, String> requestParamsMap = Utility.getRequestParamsMap(getServletConfig(), request, response, api_WebService);

			WebServiceContext webServiceContext = Utility.getTask(requestParamsMap);
			logger.info("webServiceContext: " + webServiceContext);
			webServiceResponse = webServiceAction.processAction(webServiceContext);
		}

		logger.info("webServiceResponse : " + webServiceResponse);
		webServiceResponse.getResponseWriter().writeResponse(webServiceResponse, response);
	}  	

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
