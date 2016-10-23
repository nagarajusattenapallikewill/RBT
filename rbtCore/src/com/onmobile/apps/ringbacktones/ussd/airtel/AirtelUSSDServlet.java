package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSD;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDMainMenu;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDResponseBuilder;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSDServlet;
import com.onmobile.apps.ringbacktones.ussd.common.*;

import org.apache.log4j.Logger;

public class AirtelUSSDServlet extends HttpServlet {

	private static final long serialVersionUID = 6414086072576094870L;

	private static Logger basicLogger = Logger.getLogger(AirtelUSSDServlet.class);

	public static final String CONTENT_TYPE_REQUEST_ANSWER = "application/X-USSD-request+ans";
	public static final String CONTENT_TYPE_PLAIN_REQUEST = "plain/X-USSD-request";

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long now = System.currentTimeMillis();

		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Query string: " + request.getQueryString());
		}
		String action = request.getParameter("action");
		String actionType = request.getParameter("actionType");
		String invalidResp=request.getParameter("invalidResp");
		String heading=null;
		String message=null;

		Map input = processRequestParameters(request);
		basicLogger.debug(" Action and ActionType : " + action+" : "+actionType);
		if(invalidResp!=null&&invalidResp.equalsIgnoreCase("invalidresponse")){
			String resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("ERROR_MESSAGE"),StringUtils.processInputParameters(input), (String)input.get("next"), StringUtils.processInputParameters(input));
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(resp);*/
			new USSDResponse().sendResponse(response, resp);
		}else if("languageprofile".equals(action)) {
			//language profile
			AirtelProfileUSSDMainMenu mainMenu = new AirtelProfileUSSDMainMenu(input, response);
			mainMenu.process();
		}
		else if("mainmenu".equals(action) || StringUtils.isEmpty(action)) {


			if(USSDConfigParameters.getInstance().getParameter("AIRTELPROFILE_APP").equalsIgnoreCase("yes")){
				AirtelProfileUSSDMainMenu mainMenu = new AirtelProfileUSSDMainMenu(input, response);
				mainMenu.process();
			}else{
				AirtelUSSDMainMenu mainMenu = new AirtelUSSDMainMenu(input, response);
				mainMenu.process();
			}
		}else if("whatIsHot".equals(action)) {
			heading=USSDConfigParameters.getInstance().getParameter("TOPMESSAGE_WHATISHOT");
			message=USSDConfigParameters.getInstance().getParameter("MESSAGE_WHATISHOT");
			new AirtelUSSDMessageDisplay(input,response,heading,message,action,"mainmenu",null).displayMessage();
		}else if("search".equals(action)) {
			AirtelUSSDSearch airtleSearch=new AirtelUSSDSearch(input,response);
			airtleSearch.process();
		}else if("popularSong".equals(action)) {
			new AirtelUSSDShowSongList(input,response,"popularSong").process();
		}else if("top10".equals(action)) {
			new AirtelUSSDShowSongList(input,response,"top10").process();
		}else if("freeZone".equals(action)) {
			new AirtelUSSDShowSongList(input,response,"freeZone").process();
		}else if("cricPack".equals(action)) {
			heading=USSDConfigParameters.getInstance().getParameter("TOPMESSAGE_CRICPACK");
			message=USSDConfigParameters.getInstance().getParameter("MESSAGE_CRICPACK");
			new AirtelUSSDMessageDisplay(input,response,heading,message,action,"mainmenu",null).displayMessage();
		}else if("manageSub".equals(action)) {
			new AirtelUSSDManageSubscription(input,response).process();
		}else if("unsubscribe".equals(action)) {
			new AirtelUSSDManageSubscription(input,response).unSubscribe();
		}else if("unsubscribeConfirmed".equals(action)) {
			new AirtelUSSDManageSubscription(input,response).unSubscribeConfirmed();
		}else if("3monthsadvrental".equals(action) || "1yearadvrental".equals(action)) {
			new AirtelUSSDManageSubscription(input,response).showAdvanceRentalpackMessage();
		}else if("rentalpackConfirmed".equals(action)) {
			new AirtelUSSDManageSubscription(input,response).processAdvanceRentalpack();
		}else if("copyTune".equals(action)) {
			new AirtelUSSDCopyTune(input,response).process();
		}else if("rbtprofile".equals(action)){

			AirtelProfileUSSD rbtProfile=new AirtelProfileUSSD(input,response);
			if(invalidResp!=null&&invalidResp.equalsIgnoreCase("invalidresponse")){
				String resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("ERROR_MESSAGE"),rbtProfile.processInputParameters(input), (String)input.get("next"), rbtProfile.processInputParameters(input));
				/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
				response.getWriter().println(resp);*/
				new USSDResponse().sendResponse(response, resp);
			}else if(actionType!=null&&actionType.equalsIgnoreCase("noofhrs")){
				//enter no of hrs
				System.out.println("no of hrs");
				rbtProfile.processDurationInput();
			}else if(actionType!=null&&actionType.equalsIgnoreCase("callerid")){
				rbtProfile.processCallerType();
			}else if(actionType!=null&&actionType.equalsIgnoreCase("chargemsg")){
				if(request.getParameter("callerType").equalsIgnoreCase("ALL")){
					rbtProfile.processShowChargeMessage();
				}else{
					rbtProfile.processSpecialCallerId();
				}
			}else if(actionType!=null&&actionType.equalsIgnoreCase("confirm")){
				rbtProfile.processConfirmSelection();
			}
			else{
				//show profile menu
				rbtProfile.processProfileMenu();
			}


		}else if("help".equals(action)) {
			new AirtelUSSDHelp(input,response).process();
		}else if("help1".equals(action)) {
			heading=USSDConfigParameters.getInstance().getParameter("TOPMESSAGE_HELP1");
			message=USSDConfigParameters.getInstance().getParameter("MESSAGE_HELP1");
			new AirtelUSSDMessageDisplay(input,response,heading,message,action,"help",null).displayMessage();
		}else if("help2".equals(action)) {
			new AirtelUSSDHelp(input,response).processHelplevel2();
		}else if("helplevel21".equals(action)) {
			heading=USSDConfigParameters.getInstance().getParameter("TOPMESSAGE_HELP2");
			message=USSDConfigParameters.getInstance().getParameter("MESSAGE_HELP2");
			new AirtelUSSDMessageDisplay(input,response,heading,message,action,"help2",null).displayMessage();
		}else if("isuda".equalsIgnoreCase(action)){
			String selType=(String)input.get("selType");
			boolean isUDA=new USSDWebServiceController().isUDAOn((String)input.get("subscriber"));
			 if(selType.equalsIgnoreCase("copytune")){
            	isUDA=true;
            }
            basicLogger.debug(" IsUDA : " + isUDA);
			if(!isUDA){
                 basicLogger.debug(" IsUDA : processIsuda " );
				new AirtleUSSDProcessSelction(input, response, selType, null).processISUDA();
			}else{
                 basicLogger.debug(" IsUDA : already uda is on " );
				input.put("isUdaYes", "yes");
				new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).processCallerType();
			}
		}else if("callertypes".equalsIgnoreCase(action)){
			new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).processCallerType();
		}else if("processcaller".equalsIgnoreCase(action)){
			AirtleUSSDProcessSelction process=new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null);
			if(input.get("callertype")!=null&&input.get("callertype").equals("All Caller")){
				input.put("callerid", "ALL");
				if(input.get("isUda")!=null&&((String)input.get("isUda")).equalsIgnoreCase("false")){

					process.showIsLoop();
				}else{

					process.showChargeMsg();
				}
			}else{
				new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).enterSpecialCallerId();
			}

		}else if("showchargemsg".equalsIgnoreCase(action)){
			new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).showChargeMsg();
		}else if("processsel".equalsIgnoreCase(action)){
			if(input.get("selType")!=null&&((String)input.get("selType")).equalsIgnoreCase("copytune")){
				new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).processCopySelection();
			}else
				new AirtleUSSDProcessSelction(input, response, (String)input.get("selType"), null).processSelection();
		}
		else {
			//default
			basicLogger.error("Invalid action: " + action + " request parameters: " + input);
		}

		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Query string: " + request.getQueryString() + " processing time: " + (System.currentTimeMillis() - now) + " ms");
		}
	}

	@SuppressWarnings({"unchecked"})
	private Map<String, String> processRequestParameters(final HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			params.put(paramName, request.getParameter(paramName));
		}
		return params;
	}
}
