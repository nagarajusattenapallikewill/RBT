package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class USSDManageYourProfile {

	private static Logger basicLogger = Logger.getLogger(USSDMainMenu.class);
	
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	
	public USSDManageYourProfile(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}

	public void process() throws IOException {
		String manageUrProfileMenu = USSDConfigParameters.getInstance().getParameter("MENU_MANAGE_PROFILE");
		if(StringUtils.isEmpty(manageUrProfileMenu)) {
			basicLogger.error("MENU_MANAGE_PROFILE is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = manageUrProfileMenu.split("\\$");
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
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_MANAGE_PROFILE");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return USSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=menu", startIndex);
	}
	
	public static void main(String[] args) {
		Map<String, String> input = new HashMap<String, String>();
//		input.put("next", "4");
		USSDManageYourProfile mainMenu = new USSDManageYourProfile(input, null);
		System.out.println(mainMenu.getResponse());
	}
}
