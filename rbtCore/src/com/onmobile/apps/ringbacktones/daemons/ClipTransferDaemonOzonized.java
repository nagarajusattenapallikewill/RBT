package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

/**
 * 
 * @author md.alam
 *
 */

public class ClipTransferDaemonOzonized extends Ozonized{
	
	private static Logger logger = Logger.getLogger(ClipTransferDaemonOzonized.class);
	
	private static final String COMPONENT_NAME = "CLIPTRANSFERDAEMONTHREAD";
	private ClipTransferDaemon clipTransferDaemonThread = null;
	
	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {
		try {
			clipTransferDaemonThread = ClipTransferDaemon.getClipTransferDaemonInstance();
			Thread daemonThread = new Thread(clipTransferDaemonThread);
			daemonThread.setName("CLIP_TRANSFER_DAEMON");
			daemonThread.start();
			
			return JAVA_COMPONENT_SUCCESS;
		} catch(Exception e) {
			logger.error("Exception Occured: "+e,e);
		}
		return JAVA_COMPONENT_FAILURE;
	}

	@Override
	public void stopComponent() {
		clipTransferDaemonThread.stopThread();		
	}

}
