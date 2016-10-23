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
/*
 * This class is used to show plain text messages with back/more functionality
 */
public class AirtelUSSDMessageDisplay {
	private static Logger basicLogger = Logger.getLogger(AirtelUSSDMessageDisplay.class);

	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	private String heading=null;
	private String message=null;
	private String action=null;
	private String backAction=null;
	private String confirmMessage=null;
	String inputResp=null;
	public AirtelUSSDMessageDisplay(Map<String, String> input, HttpServletResponse response,String heading,String message,String action,String backAction,String confirmMsg) {
		this.input = input;
		this.response = response;
		this.message=message;
		this.heading=heading;
		this.action=action;
		this.backAction=backAction;
		this.confirmMessage=confirmMsg;
		basicLogger.info(" message len "+message.length()+" action : "+action+" backAction : "+backAction);
		inputResp=StringUtils.addInputParameters(input);
	}

	public void displayMessage() throws IOException {	
		String resp=null;
		String []mainMenuItems = StringUtils.getTextRows(message, USSDConfigParameters.getInstance().getMessageLength()-12-2*heading.length());
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i], 
					inputResp+ "&" +"action="+action));
		}
		resp=getResponse();
		if(confirmMessage!=null)
			resp=resp+confirmMessage;
		/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(resp);*/
		new USSDResponse().sendResponse(response, resp);
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
			backUrl=inputResp + "&action="+backAction;
		}else{
			backUrl=inputResp + "&action="+action;
		}

		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = heading;
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelUSSDResponseBuilder.convertToMessageResponse(welcomeMessage, output, true, 
				inputResp+ "&action="+action, backOptionRequired,backUrl,startIndex);
	}


}
