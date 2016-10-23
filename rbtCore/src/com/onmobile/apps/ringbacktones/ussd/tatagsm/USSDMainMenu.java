package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class USSDMainMenu {

	private static Logger basicLogger = Logger.getLogger(USSDMainMenu.class);
	
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	
	public USSDMainMenu(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
	
	public void process() throws IOException {
		RBTClient rbtClient = null;
		try {
			rbtClient = RBTClient.getInstance();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		String subscriberId = input.get("subscriber");
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		//we are just giving the info only and hence using the below content type
		response.setContentType(USSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
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
		
		
		String mainMenu = USSDConfigParameters.getInstance().getParameter("MENU_FIRST_LEVEL");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Main menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split("\\$");
		for(int i=0; i<mainMenuItems.length; i++) {
			String mainMenuItem = mainMenuItems[i];
			int separatorIndex = mainMenuItem.indexOf(':');
			menu.add(new USSDNode(i, 0, mainMenuItem.substring(0, separatorIndex), 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" + mainMenuItem.substring(separatorIndex+1)));
		}
		response.setContentType(USSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());
	}
	
	public String getResponse() {
		String nextNodeId = input.get("next");
		int startIndex = 0;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}
		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_BROWSE_MAIN_MENU");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return USSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=menu", startIndex);
	}
	
	public static void main(String[] args) {
		Map<String, String> input = new HashMap<String, String>();
//		input.put("next", "4");
		USSDMainMenu mainMenu = new USSDMainMenu(input, null);
		System.out.println(mainMenu.getResponse());
	}
}
