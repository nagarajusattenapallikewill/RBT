package com.onmobile.apps.ringbacktones.rbt2.command;

import com.onmobile.apps.ringbacktones.v2.exception.RestrictionException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public abstract class FeatureListRestrictionCommand {

	private String respose;
	
	public abstract void executeCalback(String msisdn);
	
	public abstract String executeInlineCall(SelectionRequest selectionRequest, String clipID) throws RestrictionException;
	
	public String getResponse() {
		return respose;
	}
	
	public void setResponse(String response) {
		this.respose = response;
	}
	
}
