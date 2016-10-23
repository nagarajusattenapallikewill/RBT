package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class USSDSelectContent {

	private static Logger basicLogger = Logger.getLogger(USSDSelectContent.class);

	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	
	public USSDSelectContent(Map<String, String> input, HttpServletResponse response) {
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
		String parentCategoryId = input.get("pcatid");
		String categoryId = input.get("catid");
		String clipId = input.get("cid");
		String confirmSelection = input.get("confirm");
		String subscriptionClass= input.get("subscription_class");
		
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Processing clip selection. subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
					+ " categoryId: " + categoryId + " clipId: " + clipId + " confirmSelection: " + confirmSelection);
		}
		boolean isPersonalizationAllowed = false;
		boolean isTimeOfTheDayAllowed = false;
		String personalizationAllowed = USSDConfigParameters.getInstance().getParameter("RBT_PERSONALIZATION_ALLOWED");
		if(StringUtils.isNotEmpty(personalizationAllowed) && ("YES".equalsIgnoreCase(personalizationAllowed)
																|| "TRUE".equalsIgnoreCase(personalizationAllowed))) {
			isPersonalizationAllowed = true;
		}
		String timeOfTheDayAllowed = USSDConfigParameters.getInstance().getParameter("RBT_TIME_OF_THE_DAY_ALLOWED");
		if(StringUtils.isNotEmpty(timeOfTheDayAllowed) && ("YES".equalsIgnoreCase(timeOfTheDayAllowed)
															|| "TRUE".equalsIgnoreCase(timeOfTheDayAllowed))) {
			isTimeOfTheDayAllowed = true;
		}
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);

		//level 1
		if(StringUtils.isEmpty(confirmSelection)) {
			if(subscriber.isValidPrefix() && subscriber.isCanAllow()) {
				//confirm the selection/subscription
				StringBuilder chargingInfo = new StringBuilder();
				if(WebServiceConstants.DEACTIVE.equalsIgnoreCase(subscriber.getStatus()) 
						|| WebServiceConstants.NEW_USER.equalsIgnoreCase(subscriber.getStatus())) {
					//confirm subscription + selection charge message
					chargingInfo.append(USSDConfigParameters.getInstance().getParameter("CONFIRM_CHARGE_SUBSCRIPTION_N_SELECTION"));
				} else {
					//confirm selection charge message
					chargingInfo.append(USSDConfigParameters.getInstance().getParameter("CONFIRM_CHARGE_SELECTION_ONLY"));
				}
//				chargingInfo.append(" Reply with 1 to continue and 2 to cancel");
				chargingInfo.append(USSDConfigParameters.getInstance().getMessageNewLine());
				chargingInfo.append("`1`").append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&action=selectclip");
				chargingInfo.append("&pcatid=").append(parentCategoryId).append("&catid=").append(categoryId).append("&cid=").append(clipId).append("&confirm=true");
				chargingInfo.append("`2`").append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&action=catbrowse");
				//confirm=false is added to identify the users who goes back to category browsing
				chargingInfo.append("&pcatid=").append(parentCategoryId).append("&catid=").append(categoryId).append("&confirm=false");
				return USSDResponseBuilder.convertToResponse(chargingInfo.toString(), new ArrayList<USSDNode>(0), false, null, 0);
			} else {
				basicLogger.info("Invalid user. subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
						+ " categoryId: " + categoryId + " clipId: " + clipId + " confirmSelection: " + confirmSelection);
				//return invalid_user
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_RBT_USER_INVALID"),
																	new ArrayList<USSDNode>(0), false, null, 0);
			}
		}

		//level 2
		if(StringUtils.isNotEmpty(confirmSelection) && !isPersonalizationAllowed && !isTimeOfTheDayAllowed) {
			//user confirmed the selection, personalization is not allowed, time of the day is not allowed 
			//activate the user
			SelectionRequest selectionRequest = new SelectionRequest(subscriberId);
			//used for personalization - selectionRequest.setCallerID(callerID);
			selectionRequest.setOperatorUserInfo(subscriber.getOperatorUserInfo());
			selectionRequest.setCategoryID(categoryId);
			selectionRequest.setCircleID(subscriber.getCircleID());
			selectionRequest.setClipID(clipId);
			selectionRequest.setMode("USSD");
			if(subscriptionClass==null)
				subscriptionClass = USSDConfigParameters.getInstance().getParameter("SUBSCRIPTION_CLASS");
			selectionRequest.setSubscriptionClass(subscriptionClass);
			selectionRequest.setIsPrepaid(subscriber.isPrepaid());
			Rbt rbt = rbtClient.addSubscriberSelection(selectionRequest);
			//log selection request
			if(WebServiceConstants.SUCCESS.equals(selectionRequest.getResponse())) {
				//selection success
				basicLogger.info("Processed clip selection. subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
						+ " categoryId: " + categoryId + " clipId: " + clipId + " confirmSelection: " + confirmSelection + " status: success");
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_ADD_SUBSCRIBER_SELECTIONS_SUCCESS"),
																	new ArrayList<USSDNode>(0), false, null, 0);
			} else if(WebServiceConstants.ALREADY_EXISTS.equals(selectionRequest.getResponse())) {
				//selection already exists
				basicLogger.info("Processed clip selection. subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
						+ " categoryId: " + categoryId + " clipId: " + clipId + " confirmSelection: " + confirmSelection + " status: already exists");
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_ADD_SUBSCRIBER_SELECTIONS_ALREADY_EXISTS"),
																	new ArrayList<USSDNode>(0), false, null, 0);
			} else {
				//selection failed
				basicLogger.info("Processed clip selection. subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
						+ " categoryId: " + categoryId + " clipId: " + clipId + " confirmSelection: " + confirmSelection + " status: failed");
				return USSDResponseBuilder.convertToResponse(USSDConfigParameters.getInstance().getParameter("MESSAGE_ADD_SUBSCRIBER_SELECTIONS_FAILURE"),
																	new ArrayList<USSDNode>(0), false, null, 0);
			}
		}
		
		//level 3
		basicLogger.error("Invalid SelectContent option. " + " subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
							+ " categoryId: " + categoryId);

		return "";
	}
	
	public static void main(String[] args) {
		
	}
}
