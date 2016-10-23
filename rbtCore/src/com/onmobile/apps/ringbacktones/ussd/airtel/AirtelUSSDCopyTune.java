package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDWebServiceController;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;

public class AirtelUSSDCopyTune {

	private static Logger basicLogger = Logger.getLogger(AirtelUSSDCopyTune.class);
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public AirtelUSSDCopyTune(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}

	public void process() throws IOException {
		getResponse();
	}
	public void getResponse() throws IOException {
		String answer = input.get("answer");
		String subscriberId = input.get("subscriber");

		if("#".equals(answer) || "%23".equals(answer) || "*".equalsIgnoreCase(answer)) {
			System.out.println(" coming here "+answer);
			if(basicLogger.isInfoEnabled()) {
				basicLogger.info("Invalidating session: subscriberId: " + subscriberId + " answer: " + answer);
			}
			AirtelUSSDSearchSessionManager.invalidateSearchSession(subscriberId);
			AirtelUSSDMainMenu mainMenu = new AirtelUSSDMainMenu(input, response);
			mainMenu.process();
			return;
		}
		if( answer==null ||  answer.length()<8) {//null == session
            System.out.println(" coming here sessn is null");
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug(" subscriberId " + subscriberId + " answer: " + answer);
			}
			AirtelUSSDSearchSessionManager.createSearchSession(subscriberId);
			String msearchMessage = USSDConfigParameters.getInstance().getParameter("COPYTUNE_COPYNUM");
			StringBuilder sb=new StringBuilder();
			sb.append(" ");
			sb.append(USSDConfigParameters.getInstance().getMessageNewLine());
			sb.append("`#`");
			sb.append(USSDConfigParameters.getInstance().getUSSDHostURL());
			sb.append("`*`");
			sb.append(USSDConfigParameters.getInstance().getUSSDHostURL()+"action=mainmenu");
			/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			response.getWriter().println(msearchMessage+sb.toString());*/
			new USSDResponse().sendResponse(response, msearchMessage+sb.toString());
			return;
		} else {
			
			basicLogger.debug("Copy num   : " + answer+" subID is : "+input.get("subscriber"));
			CopyData copyData=new USSDWebServiceController().getCopyTunes(input.get("subscriber"),answer.trim());
			if(copyData==null){
				
				String message=USSDConfigParameters.getInstance().getParameter("COPYTUNE_NOSONG");
				try {
					new AirtelUSSDMessageDisplay(input,response,"",message,input.get("action"),"copyTune",null).displayMessage();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			String songName=copyData.getToneName();
			String message=USSDConfigParameters.getInstance().getParameter("COPYTUNE_SONGNAME")+" "+songName+" "+USSDConfigParameters.getInstance().getParameter("COPYTUNE_MESSAGE");
			StringBuilder responseBuilder=new StringBuilder();
			responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
			String confirmMsg=("`1`")+responseBuilder.toString()+"&action=isuda&copynum="+answer+"&catid="+copyData.getCategoryID()+"&clipid="+copyData.getToneID()+"&status="+copyData.getStatus()+"&selType=copytune"+"&clipname="+copyData.getToneName();
			try {
				new AirtelUSSDMessageDisplay(input,response,"",message,input.get("action"),"copyTune",confirmMsg).displayMessage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public void copyTune(){
        
	}
}
