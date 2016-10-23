package com.onmobile.apps.ringbacktones.rbt2.helper.impl;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.helper.AbstractIntegrationHelper;
import com.onmobile.apps.ringbacktones.rbt2.response.IResponseHandler;
import com.onmobile.apps.ringbacktones.rbt2.response.JSONGriffResponseHandler;

public class GriffIntegrationHelperImpl extends AbstractIntegrationHelper {
	
	private static final String PARAM_MSISDN = "msisdn";
	private static final String PARAM_CIRCLE = "circle";
	private static final String PARAM_OPERATOR = "operator";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_TYPE = "type";
	private static final String PARAM_XML = "xml";
	private static final String PARAM_ACTION_UPDATE = "UPDATE";
	private static final String PARAM_ACTION_DEACTIVATE = "DEL";
	
	

	@Override
	public String getCircleId(Subscriber subscriber) {
		if(subscriber != null && subscriber.circleID() != null){
			return subscriber.circleID().split("_")[0];
		}
		return null;
	}
	
	@Override
	public HashMap<String, String> getRequestParam(String subId, int action,
		String xml, String type, String circleID) {
		
		HashMap<String, String> requestParams = new HashMap<String, String>();
		
		requestParams.put(PARAM_MSISDN, subId);
		
		if (action == ACTION_TYPE_ADD_SEL
				|| action == ACTION_TYPE_REMOVE_SEL
				|| action == ACTION_TYPE_UPDATE_SUB) {
			requestParams.put(PARAM_ACTION, PARAM_ACTION_UPDATE);
			requestParams.put(PARAM_XML, xml);
		} else if (action == ACTION_TYPE_DEACT_USER) {
			requestParams
					.put(PARAM_ACTION, PARAM_ACTION_DEACTIVATE);
		}
		
		requestParams.put(PARAM_TYPE, type);

		String[] operator_circle = circleID.split("_");
		String operator = circleID.split("_")[0];
		String circle = circleID.split("_")[0];
		if(operator_circle.length > 1){
			 circle = circleID.split("_")[1];
		}
		requestParams.put(PARAM_CIRCLE, circle);
		requestParams.put(PARAM_OPERATOR, operator);
		
		return requestParams;
	}
	
	@Override
	public String handleResponse(String response) {
		IResponseHandler griffResponseHandler = (IResponseHandler) ConfigUtil.getBean(BeanConstant.RESPONSE_HANDLER_BEAN);
		String rspn = griffResponseHandler.processResponse(response);
		return rspn;
	}
}
