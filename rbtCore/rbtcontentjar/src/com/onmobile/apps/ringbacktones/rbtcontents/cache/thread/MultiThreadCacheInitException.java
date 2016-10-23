package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

public class MultiThreadCacheInitException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public MultiThreadCacheInitException(String message) {
		super(message);
	}
	
	public MultiThreadCacheInitException(Throwable t) {
		super(t);
	}
	
	public MultiThreadCacheInitException(String message, Throwable t) {
		super(message, t);
	}
}
