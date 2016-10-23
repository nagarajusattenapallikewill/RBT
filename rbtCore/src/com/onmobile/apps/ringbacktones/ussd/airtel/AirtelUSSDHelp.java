package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;


public class AirtelUSSDHelp {
	private static Logger basicLogger = Logger.getLogger(AirtelUSSDMainMenu.class);
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	public AirtelUSSDHelp(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
	public void process() throws IOException {

		String mainMenu = USSDConfigParameters.getInstance().getParameter("HELP_MENU_ITEMS");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Help menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i], 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" +"action=help"+(i+1)+"&isprepaid="+input.get("isprepaid")));
		}
		/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());*/
		new USSDResponse().sendResponse(response, getResponse());
	}

	public String getResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=true;
		int startIndex = 0;
		String backUrl=null;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}

		if(startIndex<0)
			startIndex=0;
		if(startIndex==0){
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu";
		}else{
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=help";
		}

		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("HELP_MENU_HEADING");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=help", backOptionRequired,backUrl,startIndex);
	}

	public void processHelplevel2() throws IOException {

		String mainMenu = USSDConfigParameters.getInstance().getParameter("HELP_MENU_LEVEL2_ITEMS");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Help menu2 is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i], 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" +"action=helplevel2"+(i+1)+"&isprepaid="+input.get("isprepaid")));
		}
		/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getHelplevel2Response());*/
		new USSDResponse().sendResponse(response, getHelplevel2Response());
	}
	public String getHelplevel2Response() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=true;
		int startIndex = 0;
		String backUrl=null;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}

		if(startIndex<0)
			startIndex=0;
		if(startIndex==0){
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=help";
		}else{
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=helplevel2";
		}





		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("HELP_MENU_HEADING");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=help2", backOptionRequired,backUrl,startIndex);
	}
}
