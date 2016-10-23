package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDServlet;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDCacheManager;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;
import com.onmobile.apps.ringbacktones.ussd.common.USSDWebServiceController;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class AirtleUSSDProcessSelction {
	private static Logger basicLogger = Logger.getLogger(AirtleUSSDProcessSelction.class);
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	private String backAction=null;
	String inputResp=null;
	int calleridLen=9;
	public AirtleUSSDProcessSelction(Map<String, String> input, HttpServletResponse response,String selType,String message) {
		this.input = input;
		this.response = response;
		inputResp=StringUtils.addInputParameters(input);
		backAction=getBackAction(input.get("action"), input.get("selType"));
		
	}
	
  public void processISUDA() throws IOException {
    System.out.println(" processISUDA() ");		
	String message=USSDConfigParameters.getInstance().getParameter("ISUDA_MESSAGE")+USSDConfigParameters.getInstance().getParameter("SELECT_REJECT_MESSAGE");
	StringBuilder responseBuilder=new StringBuilder();
	responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
	String confirmMsg=("`1`")+inputResp+"&action=callertypes&isUdaYes=y";
	confirmMsg=confirmMsg+("`2`")+inputResp+"&action=callertypes&isUdaYes=n";
	try {
		
		new AirtelUSSDMessageDisplay(input,response,"",message,input.get("action"),backAction,confirmMsg).displayMessage();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
  public void processCallerType() throws IOException {
         String isUda="false";
		basicLogger.debug(" processCallerType : ");
		if(input.get("isUdaYes")!=null&&input.get("isUdaYes").equalsIgnoreCase("y")){
			String resp=new USSDWebServiceController().makeUDSOn(input.get("subscriber"));
			//if(resp!=null&&(resp.equalsIgnoreCase("success")){
			//	isUda="true";
			//}else{
			//	isUda="false";
			//}
			isUda="true";
		}else if(input.get("isUdaYes")!=null&&input.get("isUdaYes").equalsIgnoreCase("yes")){
            isUda="true";
		}
		String mainMenu = USSDConfigParameters.getInstance().getParameter("CALLERS_TYPE");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("CallerType menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			
			menu.add(new USSDNode(i, 0, mainMenuItems[i], inputResp+"&action=processcaller&callertype="+mainMenuItems[i]+"&isUda="+isUda));
		}
		/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getCallerTypeResponse());*/
		new USSDResponse().sendResponse(response, getCallerTypeResponse());
	}
  public String getCallerTypeResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=true;
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
		
		
		
		
		
		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = "";
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				inputResp+"&action=callertypes", backOptionRequired,inputResp+ "&action="+backAction,startIndex);
	}
  
  public void enterSpecialCallerId(){
	  String answer = input.get("answer");
		String subscriberId = input.get("subscriber");
		if( answer==null || answer.length()<calleridLen) {//null == session
          
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug(" subscriberId " + subscriberId + " answer: " + answer);
			}
			AirtelUSSDSearchSessionManager.createSearchSession(subscriberId);
			String msearchMessage = USSDConfigParameters.getInstance().getParameter("SPECIALCALER_MESSAGE");
			StringBuilder sb=new StringBuilder();
			sb.append(" ");
			sb.append(USSDConfigParameters.getInstance().getMessageNewLine());
			sb.append("`#`");
			sb.append(USSDConfigParameters.getInstance().getUSSDHostURL());
			sb.append("`*`");
			sb.append(inputResp+"&action="+backAction);
//			response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			try {
				/*response.getWriter().println(msearchMessage+sb.toString());*/
				new USSDResponse().sendResponse(response, msearchMessage+sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		} else {
		    input.put("callerid", answer);
		    if(input.get("isUda")!=null&&input.get("isUda").equalsIgnoreCase("false")){
		    	 showIsLoop();
		    }else{
		    	//input.put("isLoop", "false");
		    	showChargeMsg();
		    }
		   
		}
  }
  public void showIsLoop(){
		
		String message=USSDConfigParameters.getInstance().getParameter("ISLOOP_MESSAGE")+" "+USSDConfigParameters.getInstance().getParameter("SELECT_REJECT_MESSAGE");
		String confirmMsg=("`1`")+inputResp+"&action=showchargemsg&callerid="+input.get("callerid")+"&isLoop=true";
		confirmMsg=confirmMsg+("`2`")+inputResp+"&action=showchargemsg&callerid="+input.get("callerid")+"&isLoop=false";
		try {
			
			new AirtelUSSDMessageDisplay(input,response,"",message,input.get("action"),"processcaller",confirmMsg).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
  public void showChargeMsg(){
	  String amount=new USSDCacheManager().getchargeAmount(input.get("catid").trim());
	  String message=USSDConfigParameters.getInstance().getParameter("SONG_CHARGE_MESSAGE1")+" "+amount+" "+USSDConfigParameters.getInstance().getParameter("SONG_CHARGE_MESSAGE2")+" "+USSDConfigParameters.getInstance().getParameter("COMMON_MESSAGE");
	   if(input.get("selType")!=null&&input.get("selType").equalsIgnoreCase("freeZone")){
		  message=USSDConfigParameters.getInstance().getParameter("FREE_SONG_CHARGE_MESSAGE")+" "+USSDConfigParameters.getInstance().getParameter("COMMON_MESSAGE");
	  }
		String confirmMsg=("`1`")+inputResp+"&action=processsel";
		//if(!input.containsKey("callerid"))
			confirmMsg=confirmMsg+"&callerid="+input.get("callerid");
		try {
			
			new AirtelUSSDMessageDisplay(input,response,"",message,input.get("action"),"processcaller",confirmMsg).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  public String getBackAction(String currAction,String selType){
	  if(currAction==null || selType==null)
		  return null;
	   if(currAction.equalsIgnoreCase("showchargemsg")){
		  return "callertypes";
	  }else if(currAction.equalsIgnoreCase("processcaller")){
		  return "callertypes";
	  }
	  else if(selType.equalsIgnoreCase("copytune")){
		  if(currAction.equalsIgnoreCase("processsel") ||currAction.equalsIgnoreCase("isuda") ||currAction.equalsIgnoreCase("callertypes")){
			  return "copyTune";
		  }
		  
	  }else  if(selType.equalsIgnoreCase("popularsong")){
		  if(currAction.equalsIgnoreCase("processsel") ||currAction.equalsIgnoreCase("isuda") || currAction.equalsIgnoreCase("callertypes"))
		      return "popularSong";
	  }else  if(selType.equalsIgnoreCase("top10")){
		  if(currAction.equalsIgnoreCase("processsel") ||currAction.equalsIgnoreCase("isuda") || currAction.equalsIgnoreCase("callertypes"))
		      return "top10";
	  }else  if(selType.equalsIgnoreCase("freezone")){
		  if(currAction.equalsIgnoreCase("processsel") ||currAction.equalsIgnoreCase("isuda")|| currAction.equalsIgnoreCase("callertypes"))
		      return "freeZone";
	  }
	  return null;
  }
  public void processSelection(){
	  String resp=null;
	  String selMessage=null;
	  String isLoop=input.get("isLoop");
	  String clipName=input.get("clipname");
	  String callerId=input.get("callerid");
	  int status=0;
	  String isUda=input.get("isUda");
	  try{
		  status=Integer.parseInt(input.get("status"));
	  }catch (Exception e) {
		// TODO: handle exception
	}
	  USSDWebServiceController webControl=new USSDWebServiceController();
	  Subscriber sub=webControl.getSubscriberObject(input.get("subscriber"));
	  AirtelUSSDSelectionBean selBean=new AirtelUSSDSelectionBean();
	  if(sub!=null){
		  selBean.setCircleId(sub.getCircleID());
		  selBean.setPrepaid(sub.isPrepaid());
		   if(isUda!=null&&isUda.equalsIgnoreCase("true")){
			  selBean.setUdsOn(true);
			  
		  }
	  }
	  selBean.setCallerId(callerId);
	  selBean.setCatID(input.get("catid"));
	  if(input.get("selType")!=null&&input.get("selType").equalsIgnoreCase("freeZone")){
		  
		  String chargeClass=USSDConfigParameters.getInstance().getParameter("FREE_CHARGECLASS");
		  selBean.setChargeClass(chargeClass);
	  }
	  
	  selBean.setClipId(input.get("clipid"));
	  selBean.setLoop((input.get("isLoop")!=null&&input.get("isLoop").equalsIgnoreCase("true"))?true:false);
	  selBean.setStatus(status);
	  selBean.setSubscriberId(input.get("subscriber"));
	  resp=webControl.addSelections(selBean);
	  
	  if(resp!=null&&resp.equalsIgnoreCase("success")){
		  if((isLoop!=null&&isLoop.equalsIgnoreCase("true")) || (isUda!=null&&isUda.equalsIgnoreCase("true"))){
			  selMessage=USSDConfigParameters.getInstance().getParameter("THANKS_MESSAGE")+" "+clipName+" "+USSDConfigParameters.getInstance().getParameter("SELECTION_SUCCESS_ALBUM")+" "+callerId;
		  }else
			  selMessage=USSDConfigParameters.getInstance().getParameter("THANKS_MESSAGE")+" "+clipName+" "+USSDConfigParameters.getInstance().getParameter("SELECTION_SUCCESS_RBT")+" "+callerId;
	  }else{
		  selMessage=USSDConfigParameters.getInstance().getParameter("SELECTION_FAILURE")+" "+resp;
	  }
	  try {
			
			new AirtelUSSDMessageDisplay(input,response,"",selMessage,input.get("action"),backAction,null).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  public void processCopySelection(){
	  String selMessage=null;
	  String isLoop=input.get("isLoop");
	  USSDWebServiceController webControll=new USSDWebServiceController();
	  String resp=webControll.makeCopySelection(input.get("subscriber"),input.get("copynum"),input.get("catid"),input.get("clipid"),input.get("status"),input.get("callerid"), "USSD");
	  if(resp!=null&&resp.equalsIgnoreCase("success")){
		  if(isLoop!=null&&isLoop.equalsIgnoreCase("true")){
			  selMessage=USSDConfigParameters.getInstance().getParameter("THANKS_MESSAGE")+" "+USSDConfigParameters.getInstance().getParameter("COPYTUNE_SUCCESS_ALBUM");
		  }else
			  selMessage=USSDConfigParameters.getInstance().getParameter("THANKS_MESSAGE")+" "+USSDConfigParameters.getInstance().getParameter("COPYTUNE_SUCCESS_RBT");
		  
	  }else{
		  selMessage=USSDConfigParameters.getInstance().getParameter("COPYTUNE_FAILED")+" "+resp;;
	  }
	  try {
			new AirtelUSSDMessageDisplay(input,response,"",selMessage,input.get("action"),backAction,null).displayMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
}
