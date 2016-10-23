package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet implementation class for Servlet: Gift
 *
 */
public class Gift extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Gift.class);
	private ParametersCacheManager parametersCacheManager = null;

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Gift()
	{
		super();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
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
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Gift);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);
			
			//RBT-10480
			String mode = task.getString(param_mode);
			String configuredModesForValidation = parametersCacheManager.getParameterValue("WEBSERVICE", "MODES_FOR_GIFT_API_VALIDATION", null);
			logger.info("configuredModesForValidation: " + configuredModesForValidation);		
			if (mode != null && configuredModesForValidation != null) {
				List<String> modeList = ListUtils.convertToList(configuredModesForValidation, ",");
				logger.info("mode: " + mode);
				if (modeList.contains(mode) || modeList.contains(mode.toUpperCase())) {
					logger.info("mode present in configuredModesForValidation. Doing validation.");
					task.put(param_subscriberID, task.get(param_gifterID));
					task.put(param_number, task.get(param_gifteeID));
					responseText = RBTAdminFacade.getValidateNumberResponseXML(task);
					String responseInTask = task.getString(RESPONSE);
					if (responseInTask != null && !responseInTask.equals(VALID)) {
						response.getWriter().write(responseText);
						return;
					}
					task.remove(param_subscriberID);
					task.remove(param_number);					
				}
			}
			
			responseText = RBTAdminFacade.getGiftResponseXML(task);
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