package com.onmobile.apps.ringbacktones.ussd.aircel;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDResponseBuilder;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDServlet;
import com.onmobile.apps.ringbacktones.ussd.common.*;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

import org.apache.log4j.Logger;

public class AircelUSSDServlet extends HttpServlet {

	private static final long serialVersionUID = 6414086072576094870L;

	private static Logger basicLogger = Logger.getLogger(AircelUSSDServlet.class);

	public static final String CONTENT_TYPE_REQUEST_ANSWER = "application/X-USSD-request+ans";
	public static final String CONTENT_TYPE_PLAIN_REQUEST = "plain/X-USSD-request";

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long now = System.currentTimeMillis();

		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Query string: " + request.getQueryString());
		}
		String action = request.getParameter("action");
		String actionType = request.getParameter("actionType");
		String invalidResp=request.getParameter("invalidResp");
		String heading=null;
		String message=null;
		Map input = processRequestParameters(request);
		basicLogger.debug(" Action and ActionType : " + action+" : "+actionType);
		String subscriberId = request.getParameter("subscriber");
		Subscriber subscriber = new USSDWebServiceController().getSubscriberObject(subscriberId);
		//we are just giving the info only and hence using the below content type
		response.setContentType(AircelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
		if(null != subscriber && subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING)) {
			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_DEACTIVATION_PENDING_USER"));
			return;
		}
		if(null != subscriber && subscriber.getStatus().equals(WebServiceConstants.SUSPENDED)) {
			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_SUSPENDED_USER"));
			return;
		}
		if(null != subscriber && !subscriber.isCanAllow()) {
			//subscriber is not allowed as the subscriber is blacklisted
			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_BLACKLISTED_USER"));
			return;
		}
		if(invalidResp!=null&&invalidResp.equalsIgnoreCase("invalidresponse")){
			String resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("ERROR_MESSAGE"),StringUtils.processInputParameters(input), (String)input.get("next"), StringUtils.processInputParameters(input));
			response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(resp);
		}
		else if("mainmenu".equals(action) || StringUtils.isEmpty(action)) {

				//AircelUSSDMainMenu mainMenu = new AircelUSSDMainMenu(input, response);
				//mainMenu.process();
			AircelUSSDSearch airtleSearch=new AircelUSSDSearch(input,response);
			airtleSearch.process();
			
		}else if("search".equals(action)) {
			//System.out.println(" response "+response);
			AircelUSSDSearch airtleSearch=new AircelUSSDSearch(input,response);
			airtleSearch.process();
		}else{
			//default
			basicLogger.error("Invalid action: " + action + " request parameters: " + input);
		}

		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Query string: " + request.getQueryString() + " processing time: " + (System.currentTimeMillis() - now) + " ms");
		}
	}

	@SuppressWarnings({"unchecked"})
	private Map<String, String> processRequestParameters(final HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			params.put(paramName, request.getParameter(paramName));
		}
		return params;
	}
}
