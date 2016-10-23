package com.onmobile.apps.ringbacktones.lucene;

public class RBTLuceneException extends Exception{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 162784555783L;

	public RBTLuceneException(){
		super();
	}
	
	public RBTLuceneException(Throwable th){
		super(th);
	}
	
	public RBTLuceneException(Throwable th, String message){
		super(message, th);
	}
	
	public RBTLuceneException(String message){
		super(message);
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(""+getClass()+": "+this.getMessage());
		return builder.toString();
	}
}
