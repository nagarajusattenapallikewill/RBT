package com.onmobile.apps.ringbacktones.daemons.inline.shutdown;

import org.apache.log4j.Logger;

public class Hook {
	
	private static final Logger logger = Logger.getLogger(Hook.class);
	private boolean keepRunning = true;
	private final Thread thread;
	
	Hook(Thread thread) {
		this.thread = thread;
	}
	
	public boolean keepRunning() {
		return keepRunning;
	}
	
	public void shutdown() {
		
		keepRunning = false;		
		thread.interrupt();		
		try {		
			thread.join(200);		
		} catch (InterruptedException e) {		
			logger.error("Error shutting down thread with hook", e);		
		}	
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}
}
