package com.onmobile.apps.ringbacktones.common;

public class HttpResponseBean {
	int status=-1;
	String response=null;
	boolean responseReceived=false;
	
	public HttpResponseBean(boolean responseReceived,int status,String response){
		this.response=response;
		this.responseReceived=responseReceived;
		this.status=status;
	}
	

	public boolean isResponseReceived() {
		return responseReceived;
	}

	public void setResponseReceived(boolean responseReceived) {
		this.responseReceived = responseReceived;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
