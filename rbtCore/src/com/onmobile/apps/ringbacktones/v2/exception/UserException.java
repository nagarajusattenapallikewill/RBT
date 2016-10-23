package com.onmobile.apps.ringbacktones.v2.exception;

public class UserException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2739961331862296996L;
	
	private int statusCode = 400;
	private String code;
	private String response;
	
	public UserException(){
		super();
	}
	
	public UserException(String response) {
		super(response);
		this.response = response;
	}
	
	public UserException(String code, String response){
		super();
		this.code = code;
		this.response = response;
	}
	
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
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
}
