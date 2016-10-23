package com.onmobile.apps.ringbacktones.smClient;

import java.util.HashMap;

import com.onmobile.prism.client.core.SMResponse;

/**
 * This class represents the response from sub manager and is like a wrapper class to SM client response object.
 * 
 * @author Sreekar
 * @since 2009-11-19
 */

public class RBTSMClientResponse {
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String ERROR = "ERROR";
	
	private String response = "FAILURE";
	private String message = "error response";
	
	protected RBTSMClientResponse() {
		
	}
	
	protected RBTSMClientResponse(SMResponse smResponse, HashMap<String, String> responseParams) {
		response = FAILURE;
		if(smResponse.isSuccess())
			response = SUCCESS;
		message = smResponse.getMessage();
	}
	
	public String getResponse() {
		return response;
	}
	
	public String getMessage() {
		return message;
	}
	
	protected void setResponse(String response) {
		this.response = response;
	}
	
	protected void setmessage(String message) {
		this.message = message;
	}
	
	public String toString() {
		return "response:" + response + "-message:" + message;
	}
}