package com.onmobile.apps.ringbacktones.rbt2.helper.impl;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.helper.AbstractIntegrationHelper;
import com.onmobile.apps.ringbacktones.rbt2.response.IResponseHandler;
import com.onmobile.apps.ringbacktones.rbt2.response.StringGriffResponseHandler;

public class TPIntegrationHelperImpl extends AbstractIntegrationHelper {

	
	private static final String PARAM_SUB_ID = "SUB_ID";
	private static final String PARAM_ACTION = "ACTION";
	private static final String PARAM_TYPE = "TYPE";
	private static final String PARAM_XML = "XML";
	private static final String PARAM_ACTION_UPDATE = "UPDATE";
	private static final String PARAM_ACTION_DEACTIVATE = "DEL";
	private static final String PARAM_CIRCLE_ID = "CIRCLE_ID";
	
	
	@Override
	public String getCircleId(Subscriber subscriber) {
		return subscriber.circleID();
	}
	
	@Override
	public HashMap<String, String> getRequestParam(String subId, int action,
		String xml, String type, String circleID) {
		
		HashMap<String, String> requestParams = new HashMap<String, String>();
		
		requestParams.put(PARAM_SUB_ID, subId);
		
		if (action == ACTION_TYPE_ADD_SEL
				|| action == ACTION_TYPE_REMOVE_SEL
				|| action == ACTION_TYPE_UPDATE_SUB) {
			requestParams.put(PARAM_ACTION, PARAM_ACTION_UPDATE);
			requestParams.put(PARAM_XML, xml);
		} else if (action == ACTION_TYPE_DEACT_USER) {
			requestParams
					.put(PARAM_ACTION, PARAM_ACTION_DEACTIVATE);
		}
		
		//empty chk
		if(type !=null && !type.trim().equals("")){
		 requestParams.put(PARAM_TYPE, type);
		}
		
		requestParams.put(PARAM_CIRCLE_ID, circleID);
		
		return requestParams;
	}
	
	@Override
	public String handleResponse(String response) {
		IResponseHandler stringResponseHandler = new StringGriffResponseHandler();
		return stringResponseHandler.processResponse(response);
	}
}
