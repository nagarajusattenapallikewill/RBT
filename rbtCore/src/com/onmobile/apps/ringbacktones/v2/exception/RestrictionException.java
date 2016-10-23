package com.onmobile.apps.ringbacktones.v2.exception;

public class RestrictionException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2739961331862296996L;
	
	private String response;
	
	public RestrictionException(){
		super();
	}
	
	public RestrictionException(String response) {
		super(response);
		this.response = response;
	}
	
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
}
