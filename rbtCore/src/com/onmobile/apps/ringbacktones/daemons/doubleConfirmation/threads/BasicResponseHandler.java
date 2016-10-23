package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;

public class BasicResponseHandler {

	public String processResponse(int responseCode, String responseStr ){
		return responseCode+"-"+responseStr;
	}
	
}
