package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class USSDServlet extends HttpServlet {

	private static final long serialVersionUID = 6414086072576094870L;

	private static Logger basicLogger = Logger.getLogger(USSDServlet.class);
	
	public static final String CONTENT_TYPE_REQUEST_ANSWER = "application/X-USSD-request+ans";
	public static final String CONTENT_TYPE_PLAIN_REQUEST = "plain/X-USSD-request";
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		long now = System.currentTimeMillis();
		
		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Query string: " + request.getQueryString());
		}
		String action = request.getParameter("action");
		Map input = processRequestParameters(request);
		if("menu".equals(action) || StringUtils.isEmpty(action)) {
			//main menu
			USSDMainMenu mainMenu = new USSDMainMenu(input, response);
			mainMenu.process();
		} else if("search".equalsIgnoreCase(action)) {
			//set the content type in USSDSearch
			USSDSearch search = new USSDSearch(input, response);
			search.process();
		} else if("catbrowse".equalsIgnoreCase(action)) {
			//category browsing
			USSDBrowseContents browseMenu = new USSDBrowseContents(input, response);
			browseMenu.process();
		} else if("manage".equalsIgnoreCase(action)) {
			//manage profile
			USSDManageYourProfile manageMenu = new USSDManageYourProfile(input, response);
			manageMenu.process();
		} else if("selectclip".equalsIgnoreCase(action)) {
			//Clip selection 
			USSDSelectContent selectContent = new USSDSelectContent(input, response);
			selectContent.process();
		} else if("unsubscribe".equalsIgnoreCase(action)) {
			//Clip selection 
			USSDUnsubscription unsubscription = new USSDUnsubscription(input, response);
			unsubscription.process();
		} else if ("viewlibrary".equalsIgnoreCase(action)) {
			// Clip selection
			USSDViewLibrary viewLibrary = new USSDViewLibrary(input,
					response);
			viewLibrary.process();
		} else {
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
