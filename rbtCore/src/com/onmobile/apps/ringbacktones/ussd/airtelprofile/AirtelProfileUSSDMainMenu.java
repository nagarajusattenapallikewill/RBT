package com.onmobile.apps.ringbacktones.ussd.airtelprofile;
import com.onmobile.apps.ringbacktones.ussd.airtel.USSDResponse;
import com.onmobile.apps.ringbacktones.ussd.common.*;

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

public class AirtelProfileUSSDMainMenu {

	private static Logger basicLogger = Logger.getLogger(AirtelProfileUSSDMainMenu.class);
	
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	
	public AirtelProfileUSSDMainMenu(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
	
	public void process() throws IOException {
		RBTClient rbtClient = null;
		try {
			
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		String subscriberId = input.get("subscriber");
		Subscriber subscriber = new USSDWebServiceController().getSubscriberObject(subscriberId);
		//we are just giving the info only and hence using the below content type
//		response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
		if(null != subscriber && subscriber.getStatus().equals(WebServiceConstants.DEACT_PENDING)) {
//			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_DEACTIVATION_PENDING_USER"));
			new USSDResponse().sendResponse(response, USSDConfigParameters.getInstance().getParameter("MESSAGE_DEACTIVATION_PENDING_USER"));
			return;
		}
		if(null != subscriber && subscriber.getStatus().equals(WebServiceConstants.SUSPENDED)) {
//			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_SUSPENDED_USER"));
			new USSDResponse().sendResponse(response, USSDConfigParameters.getInstance().getParameter("MESSAGE_SUSPENDED_USER"));
			return;
		}
		if(null != subscriber && !subscriber.isCanAllow()) {
			//subscriber is not allowed as the subscriber is blacklisted
//			response.getWriter().println(USSDConfigParameters.getInstance().getParameter("MESSAGE_BLACKLISTED_USER"));
			new USSDResponse().sendResponse(response, USSDConfigParameters.getInstance().getParameter("MESSAGE_BLACKLISTED_USER"));
			return;
		}
		
		String mainMenu = USSDConfigParameters.getInstance().getParameter("LANGUAGE_VALUE");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Main menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i], 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" + "action=rbtprofile&"+"language="+mainMenuItems[i]+"&isprepaid="+subscriber.isPrepaid()));
		}
		/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());*/
		new USSDResponse().sendResponse(response, getResponse());
	}
	
	public String getResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=false;
		int startIndex = 0;
		String backUrl=null;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}
	
		if(startIndex!=0){
			backOptionRequired=true;
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=languageprofile";
		}
		
		
		
		if(startIndex<0)
			startIndex=0;
		
		if(USSDConfigParameters.getInstance().getParameter("AIRTELPROFILE_APP").equalsIgnoreCase("no")&&startIndex==0){
			backOptionRequired=true;
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu";
		}
		
		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_BROWSE_MAIN_MENU");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelProfileUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=languageprofile", backOptionRequired,backUrl,startIndex);
	}
	
	public static void main(String[] args) {
		Map<String, String> input = new HashMap<String, String>();
		input.put("next", "0");
		
		AirtelProfileUSSDMainMenu mainMenu = new AirtelProfileUSSDMainMenu(input, null);
		try {
			mainMenu.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(mainMenu.getResponse());
	}
}
