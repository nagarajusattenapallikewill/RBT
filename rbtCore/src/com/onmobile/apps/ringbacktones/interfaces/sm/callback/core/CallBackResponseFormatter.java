package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;

public class CallBackResponseFormatter
{
	private static HashMap<String, String> responseStringMap = new HashMap<String, String>();
	
	static
	{
		responseStringMap.put("SUCCESS", "SUCCESS");
	}
	
	public static void formatResponse(CallbackWorkUnit callbackWorkUnit)
	{
		String responseString = callbackWorkUnit.getResponseString();
		String mappedResponse = responseStringMap.get(responseString);
		if(mappedResponse != null)
			responseString = mappedResponse;
		callbackWorkUnit.setResponseString(responseString);
	}
}
