package com.onmobile.apps.ringbacktones.v2.dao;

public class DataAccessException extends Exception {
	
private static final long serialVersionUID = 1L;
	
	public DataAccessException(String message) {
		super(message);
	}
	
	public DataAccessException(Throwable t) {
		super(t);
	}
	
	public DataAccessException(String message, Throwable t) {
		super(message, t);
	}

}
