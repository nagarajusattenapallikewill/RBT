package com.onmobile.apps.ringbacktones.v2.daemons;

import org.apache.log4j.Logger;

public abstract class WorkerThread implements Runnable{

	Logger logger = Logger.getLogger(WorkerThread.class);
	
	private Object object;
	private UGCBinaryTransferDaemon daemon;
	protected abstract void processExecute(Object object) throws IllegalAccessException;
	
	protected WorkerThread(UGCBinaryTransferDaemon daemon) {
		this.daemon = daemon;
	}
	
	public final void run() {
		try {
			if(daemon.isThreadAlive()) {
				processExecute(object);
			}
		} catch (IllegalAccessException e) {
			logger.error("UGCBinaryTransferDaemon will be killed. " + e.getMessage(), e);
			daemon.stopThread();
		}
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
