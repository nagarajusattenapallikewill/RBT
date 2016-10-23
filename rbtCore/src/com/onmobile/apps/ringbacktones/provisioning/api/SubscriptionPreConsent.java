package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.api.Subscription;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class SubscriptionPreConsent extends HttpServlet implements Constants {
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(SubscriptionPreConsent.class);

	public SubscriptionPreConsent() {
		super();
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// subscriberID=&MODE=&callerID=&
		logger
				.debug("Recieved SubscriptionPreConsent request. Requst parameters are: "
						+ request.getQueryString());
		String responseText = Utility.getErrorXML();
		try {
			HashMap<String, String> requestParams = Utility
					.getRequestParamsMap(getServletConfig(), request, response,
							api_SubscriptionPreConsent);

			WebServiceContext task = Utility.getTask(requestParams);
			responseText = RBTAdminFacade
						.getPreConsentSubscriptionResponseXML(task);
			

		} catch (Exception e) {
			Logger.getLogger(Sms.class).error("RBT:: " + e.getMessage(), e);
		}

		response.getWriter().write(responseText);

	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
