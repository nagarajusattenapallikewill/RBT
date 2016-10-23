package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class USSDUnsubscription {

	private static Logger basicLogger = Logger.getLogger(USSDUnsubscription.class);

	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	
	public USSDUnsubscription(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}
	
	public void process() throws IOException {
		response.setContentType(USSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());
	}
	
	public String getResponse() {
		RBTClient rbtClient = null;
		try {
			rbtClient = RBTClient.getInstance();
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
		String subscriberId = input.get("subscriber");
		String confirmUnsubscription = input.get("confirm");
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Processing Unsubscription. subscriberId: " + subscriberId + " confirmUnsubscription: " + confirmUnsubscription);
		}
		
		if(StringUtils.isEmpty(confirmUnsubscription)) {
			
			//confirm the selection/subscription
			StringBuilder chargingInfo = new StringBuilder();
			chargingInfo.append(USSDConfigParameters.getInstance().getParameter("CONFIRM_UNSUBSCRIPTION"));
//			chargingInfo.append(" Reply with 1 to continue and 2 to cancel");
			chargingInfo.append("`1`").append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&action=unsubscribe").append("&confirm=true");
			chargingInfo.append("`2`").append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&action=menu");
			return USSDResponseBuilder.convertToResponse(chargingInfo.toString(), new ArrayList<USSDNode>(0), false, null, 0);

		}
		
		if(StringUtils.isNotEmpty(confirmUnsubscription)) {
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberId);
			subscriptionRequest.setMode("USSD");
			Subscriber subscriber = rbtClient.deactivateSubscriber(subscriptionRequest);
//			if(! subscriptionRequest.getResponse().equalsIgnoreCase("success")) {
//				
//			}
			if(null != subscriber && WebServiceConstants.DEACT_PENDING.equalsIgnoreCase(subscriber.getStatus())) {
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_UNSUBSCRIPTION_SUCCESS"),
																	new ArrayList<USSDNode>(0), false, null, 0);
			} else {
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_UNSUBSCRIPTION_FAILURE"), 
																	new ArrayList<USSDNode>(0), false, null, 0);
			}
		}
		basicLogger.error("Invalid Unsubscription option. " + " subscriberId: " + subscriberId);

		return "";
	}
}
