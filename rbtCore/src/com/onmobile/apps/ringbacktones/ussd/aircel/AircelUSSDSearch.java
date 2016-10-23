package com.onmobile.apps.ringbacktones.ussd.aircel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.ussd.common.USSDSession;
import com.onmobile.apps.ringbacktones.ussd.common.USSDSearchSessionManager;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class AircelUSSDSearch {

	private static Logger basicLogger = Logger.getLogger(AircelUSSDSearch.class);

	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public AircelUSSDSearch(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}

	public void process() throws IOException {
		getResponse();
	}

	public void getResponse() throws IOException {
		String answer = input.get("answer");
		String subscriberId = input.get("subscriber");

		if("#".equals(answer) || "%23".equals(answer)) {
			//user press #. so end the search session.
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Invalidating session: subscriberId: " + subscriberId + " answer: " + answer);
			}
			USSDSearchSessionManager.invalidateSearchSession(subscriberId);

			//go to main menu
			AircelUSSDMainMenu mainMenu = new AircelUSSDMainMenu(input, response);
			mainMenu.process();
			return;
		}
		USSDSession session = USSDSearchSessionManager.getActiveSearchSession(subscriberId);
		if(null == session) {
			//new user. send the message requesting the user to type the search keywords 
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Returning the intro message. subscriberId " + subscriberId + " answer: " + answer);
			}
			//create a new session now
			USSDSearchSessionManager.createSearchSession(subscriberId);
			//get the welcome message
			String msearchMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_SEARCH");
			response.setContentType(AircelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			response.getWriter().println(msearchMessage);
			return;
		} else {
			//user entered the query and it is sent in the answer parameter from UMP
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("subscriberId: " + subscriberId + " answer: " + answer);
			}

			String msearchResult = "";
			try {
				//Hit MSearch URL and return the response as it is.
				msearchResult = getMSearchResponse(subscriberId, answer);
				//append url which is hit for any response like 1,2,3 etc
				String msearchConfirmMessage = USSDConfigParameters.getInstance().getParameter("MSEARCH_CONFIRM_MESSAGE");
				if(msearchResult.endsWith(msearchConfirmMessage)) {
					//append the help menu
					String msearchGotoMenu = USSDConfigParameters.getInstance().getParameter("MSEARCH_GOTO_MENU_HELP");
					msearchResult += msearchGotoMenu;
					response.setContentType(AircelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
					response.getWriter().println(AircelUSSDResponseBuilder.convertToResponse(msearchResult, new ArrayList<USSDNode>(0), false, null, false, null, 0));
					return;

				}
				response.setContentType(AircelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
				response.getWriter().println(msearchResult);

				return;
			} catch(Exception e) {
				basicLogger.error(e);
				response.setContentType(AircelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
				response.getWriter().println(AircelUSSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_TECHNICAL_DIFFICULTIES"),
						new ArrayList<USSDNode>(0), false, null, false, null, 0));

				return;
			}
		}
	}


	private String getMSearchResponse(String subscriberId, String answer) throws Exception {
		HttpParameters httpParams = new HttpParameters();
		httpParams.setUrl(USSDConfigParameters.getInstance().getParameter("MSEARCH_URL"));
		String proxyHost = USSDConfigParameters.getInstance().getParameter("MESARCH_PROXY_HOST");
		if(StringUtils.isNotEmpty(proxyHost)) {
			httpParams.setProxyHost(proxyHost);
			String proxyPort = USSDConfigParameters.getInstance().getParameter("MESARCH_PROXY_PORT");
			httpParams.setProxyPort(Integer.parseInt(proxyPort));
		}
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("smsText", answer);
		parameters.put("userNumber", subscriberId);

		String msearchResult = "";
		HttpResponse response = RBTHttpClient.makeRequestByGet(httpParams, parameters);
		if(HttpServletResponse.SC_OK == response.getResponseCode()) {
			msearchResult = response.getResponse();
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("subscriberId: " + subscriberId + " answer: " + answer + " Response: " + response);
			}
			return msearchResult;
		} else {
			basicLogger.error("subscriberId: " + subscriberId + " answer: " + answer + " Response: " + response);
			throw new Exception("Status code " + response.getResponseCode());
		}
	}
}
