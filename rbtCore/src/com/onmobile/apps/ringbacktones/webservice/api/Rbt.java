package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet implementation class for Servlet: Rbt
 *
 */
public class Rbt extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Rbt.class);

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Rbt()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/xml; charset=utf-8");
		String responseText = Utility.getErrorXML();
		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Rbt);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);

			String redirectionURL = null;
			String toSupportRedirection = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "REDIRECT_SUPPORT_RBT_URL", "FALSE");
			
			if(toSupportRedirection != null && toSupportRedirection.equalsIgnoreCase("TRUE")) {
				Task provTask = new Task();
				provTask.setObject(Constants.param_URL, "Rbt.do");
				provTask.setObject(param_subscriberID, task.getString(param_subscriberID));
				Processor processor = AdminFacade.getProcessorObject(Constants.api_Promotion);
				redirectionURL = Processor.getRedirectionURL(provTask);
				if (redirectionURL != null) {
					HttpParameters httpParameters = new HttpParameters(redirectionURL);
					try {
						HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
								httpParameters, requestParams);
						logger.info("RBT:: httpResponse: " + httpResponse);
						responseText = httpResponse.getResponse();
					} catch (Exception e) {
						logger.error("RBT:: " + e.getMessage(), e);
						responseText = Utility.getErrorXML();
					}
				}
			}
			
			
			if(redirectionURL == null) {
				responseText = RBTAdminFacade.getRBTInformationXML(task);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
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