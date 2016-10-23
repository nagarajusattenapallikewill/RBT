package com.onmobile.apps.ringbacktones.ussd.aircel;
import com.onmobile.apps.ringbacktones.ussd.common.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class AircelUSSDMainMenu {

	private static Logger basicLogger = Logger.getLogger(AircelUSSDMainMenu.class);

	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public AircelUSSDMainMenu(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
   /*
    * Process to show mainmenu
    */
	public void process() throws IOException {

		String subscriberId = input.get("subscriber");
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

		String mainMenu = USSDConfigParameters.getInstance().getParameter("MAIN_MENU_ITEMS");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Main menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i].substring(0,mainMenuItems[i].indexOf(":")), 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" +mainMenuItems[i].substring(mainMenuItems[i].indexOf(":")+1)+"&isprepaid="+true));
		}
		response.setContentType(AircelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());
	}

	public String getResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=false;
		int startIndex = 0;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}

		if(startIndex<0)
			startIndex=0;

		if(startIndex!=0)
			backOptionRequired=true;



		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("TOPMESSAGE_MAIN_MENU");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AircelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu", backOptionRequired,USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu",startIndex);
	}


}
