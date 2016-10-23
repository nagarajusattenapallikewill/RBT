package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
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
 * Servlet implementation class for Servlet: Selection
 *
 */
public class Selection extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Selection.class);

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Selection()
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
		RBTNode node = null;
		String msisdn = null;
		RBTMonitorManager monitorManager = RBTMonitorManager.getInstance();
		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Selection);
			if(monitorManager.validWebServiceNode(requestParams.get(param_mode))) {
				msisdn = requestParams.get(param_subscriberID);
				node = RBTMonitorManager.getInstance().startNode(msisdn, requestParams.get(param_mode));
			}
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("Request received with params: " + task);

			Parameters modeIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "MODE_IP_MAPPING_FOR_CONSENT", null);
			if(modeIPParam != null) {
				String ipAddress = request.getRemoteAddr();
				task.put(param_ipAddressConsent, ipAddress);
			}
			
			// RBT-13522 redirection logic
			boolean redirectionDone = false;
			if (requestParams.containsKey("redirectRequired")
					&& requestParams.get("redirectRequired").equalsIgnoreCase("true")) {
				Task newTask = new Task();
				newTask.setObject(Constants.param_URL, "Selection.do");
				newTask.setObject(param_subscriberID,
						task.getString(param_subscriberID));
				String redirectionURL = Processor.getRedirectionURL(newTask);
				if (redirectionURL != null) {
					HttpParameters httpParameters = new HttpParameters(
							redirectionURL);
					try {
						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, requestParams);
						logger.info("RBT:: httpResponse: " + httpResponse);
						responseText = httpResponse.getResponse();
						redirectionDone = true;
					} catch (Exception e) {
						logger.error("RBT:: " + e.getMessage(), e);
						responseText = Utility.getErrorXML();
					}

				}
			}
			if (!redirectionDone) {
				responseText = RBTAdminFacade.getSelectionResponseXML(task);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		RBTMonitorManager.getInstance().endNode(msisdn, node, responseText);

		logger.info("Response: " + responseText);
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