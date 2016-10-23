package com.onmobile.apps.ringbacktones.ussd.airtelprofile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.ussd.airtel.USSDResponse;
import com.onmobile.apps.ringbacktones.ussd.common.*;

import org.apache.log4j.Logger;

public class AirtelProfileUSSDServlet extends HttpServlet {

	private static final long serialVersionUID = 6414086072576094870L;

	private static Logger basicLogger = Logger.getLogger(AirtelProfileUSSDServlet.class);

	public static final String CONTENT_TYPE_REQUEST_ANSWER = "application/X-USSD-request+ans";
	public static final String CONTENT_TYPE_PLAIN_REQUEST = "plain/X-USSD-request";

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long now = System.currentTimeMillis();

		if(basicLogger.isDebugEnabled()) {
			basicLogger.debug("Query string: " + request.getQueryString());
		}
		String action = request.getParameter("action");//action
		String actionType = request.getParameter("actionType");
		String invalidResp=request.getParameter("invalidResp");
		
		Map input = processRequestParameters(request);
		basicLogger.debug(" Action and ActionType : " + action+" : "+actionType);
		if("languageprofile".equals(action) || StringUtils.isEmpty(action)) {
			//main menu
			AirtelProfileUSSDMainMenu mainMenu = new AirtelProfileUSSDMainMenu(input, response);
			mainMenu.process();
		}else if("rbtprofile".equals(action)) {
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
