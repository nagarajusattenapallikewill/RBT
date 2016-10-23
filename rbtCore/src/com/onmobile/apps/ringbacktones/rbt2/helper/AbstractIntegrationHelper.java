package com.onmobile.apps.ringbacktones.rbt2.helper;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.content.Subscriber;

public abstract class AbstractIntegrationHelper {
	
	protected static final int ACTION_TYPE_ADD_SEL = 1;
	protected static final int ACTION_TYPE_REMOVE_SEL = 2;
	protected static final int ACTION_TYPE_DEACT_USER = 3;
	protected static final int ACTION_TYPE_UPDATE_SUB = 4;
	
	abstract public String getCircleId(Subscriber subscriber);
	
	abstract public String handleResponse(String response);

	public abstract HashMap<String, String> getRequestParam(String subId, int action, String xml, String type,
			String circleID);

}
