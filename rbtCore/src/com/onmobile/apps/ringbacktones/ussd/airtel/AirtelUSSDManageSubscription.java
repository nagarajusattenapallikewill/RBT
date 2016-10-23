package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDCacheManager;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;
import com.onmobile.apps.ringbacktones.ussd.common.USSDWebServiceController;

public class AirtelUSSDManageSubscription {

	private static Logger basicLogger = Logger.getLogger(AirtelUSSDManageSubscription.class);

	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public AirtelUSSDManageSubscription(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
	/*
	 * To show options for subscription management
	 */
	public void process() throws IOException {


		String mainMenu = USSDConfigParameters.getInstance().getParameter("MANAGESUB_MENU_ITEMS");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("Manage subscription menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			menu.add(new USSDNode(i, 0, mainMenuItems[i].substring(0,mainMenuItems[i].indexOf(":")), 
					USSDConfigParameters.getInstance().getUSSDHostURL() + "&" +mainMenuItems[i].substring(mainMenuItems[i].indexOf(":")+1)));
		}
		/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());*/
		new USSDResponse().sendResponse(response, getResponse());
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
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MANAGESUB_MENU_HEADING");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=manageSub", backOptionRequired,USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu",startIndex);
	}
	/*
	 * To show a message to user before proceeding for deactivation
	 */
	public void unSubscribe(){
		basicLogger.debug("unSubscribe ");
		String heading="";
		String message=USSDConfigParameters.getInstance().getParameter("UNSUBSCRIBE_CONFIRM_MESSGAE");
		StringBuilder responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		String confirmMsg=("`1`")+responseBuilder.toString()+"&action=unsubscribeConfirmed";
		try {

			new AirtelUSSDMessageDisplay(input,response,heading,message,input.get("action"),"manageSub",confirmMsg).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * To  unsubscribe user and disply result message
	 */
	public void unSubscribeConfirmed(){
		String heading="";
		String message=null;

		try {
			USSDWebServiceController ussdWebservice=new USSDWebServiceController();
			String resp=ussdWebservice.unSubscribeUser(input.get("subscriber"));
			if(resp!=null&&resp.equalsIgnoreCase("success")){
				message=USSDConfigParameters.getInstance().getParameter("UNSUBSCRIBE_SUCCESS");
			}else{
				message=USSDConfigParameters.getInstance().getParameter("UNSUBSCRIBE_FAILED")+" "+resp;;
			}
			new AirtelUSSDMessageDisplay(input,response,heading,message,input.get("action"),"manageSub",null).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/*
	 * Show advance rentalpack message
	 */
	public void showAdvanceRentalpackMessage(){
		String heading="";
		String pack=null;
		String amount=null;
		basicLogger.debug(" showAdvanceRentalpackMessage ");
		USSDCacheManager cacheMngr=new USSDCacheManager();
		if(input.get("action")!=null&&input.get("action").equalsIgnoreCase("3monthsadvrental")){
			pack=cacheMngr.getParameters("USSD", "THREEMONTHS_ADVRENTALPACK", "PACK30");

		}else{
			pack=cacheMngr.getParameters("USSD", "ONEYEAR_ADVRENTALPACK", "PACK100");
		}
		amount=cacheMngr.getSubclassAmount(pack);
		basicLogger.debug(" showAdvanceRentalpackMessage amount "+amount);
		String message=USSDConfigParameters.getInstance().getParameter("ADVRENTAL_MESSAGE")+" "+amount+USSDConfigParameters.getInstance().getParameter("COMMON_MESSAGE");
		StringBuilder responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		String confirmMsg=("`1`")+responseBuilder.toString()+"&action=rentalpackConfirmed&pack="+pack;
		try {

			new AirtelUSSDMessageDisplay(input,response,heading,message,input.get("action"),"manageSub",confirmMsg).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * To processAdvance rental pack
	 */
	public void processAdvanceRentalpack(){
		String heading="";
		String message=null;
		basicLogger.debug(" processAdvanceRentalpack ");
		try {
			USSDWebServiceController ussdWebservice=new USSDWebServiceController();
			String resp=ussdWebservice.upgradeSubscription(input.get("subscriber"), input.get("pack"));
			basicLogger.debug(" processAdvanceRentalpack response : "+resp);
			if(resp!=null&&resp.equalsIgnoreCase("success")){
				message=USSDConfigParameters.getInstance().getParameter("ADVRENTAL_SUCCESS");
			}else{
				message=USSDConfigParameters.getInstance().getParameter("ADVRENTAL_FAILED")+" "+resp;;
			}
			new AirtelUSSDMessageDisplay(input,response,heading,message,input.get("action"),"manageSub",null).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
