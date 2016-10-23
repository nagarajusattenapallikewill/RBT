package com.onmobile.apps.ringbacktones.v2.bean;

import java.util.Map;

import com.onmobile.apps.ringbacktones.v2.common.Constants;

public class ResponseErrorCodeMapping implements Constants {
	
	private Map<String, ResponseCode> errorCodes;

	public void setErrorCodes(Map<String, ResponseCode> errorCodes) {
		this.errorCodes = errorCodes;
	}
	
	public ResponseCode getErrorCode(String key) {
		ResponseCode responseCode = errorCodes.get(key);
		if (responseCode == null) {
			responseCode = new ResponseCode();
			responseCode.setCode(UNKNOWN_ERROR);
			responseCode.setStatusCode(400);
		}
		return responseCode;
	}
	
	
	
	
	public static class ResponseCode {
		
		
		private int statusCode;
		private String code;
		
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		
	}

}
