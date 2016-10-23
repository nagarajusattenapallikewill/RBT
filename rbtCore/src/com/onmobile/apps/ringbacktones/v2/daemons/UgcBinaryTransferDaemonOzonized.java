package com.onmobile.apps.ringbacktones.v2.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.ClipTransferDaemon;
import com.onmobile.apps.ringbacktones.daemons.ClipTransferDaemonOzonized;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class UgcBinaryTransferDaemonOzonized extends Ozonized {
	
	private static Logger logger = Logger.getLogger(UgcBinaryTransferDaemonOzonized.class);
	
	private static final String COMPONENT_NAME = "UGC_BINARY_TRANSFER_THREAD";
	private UGCBinaryTransferDaemon ugcBinaryTransferDaemon = null;
	
	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {
		try {
			ugcBinaryTransferDaemon = UGCBinaryTransferDaemon.getInBinaryTransferDaemon();
			Thread daemonThread = new Thread(ugcBinaryTransferDaemon);
			daemonThread.setName("UGC_BINARY_TRANSFER_DAEMON");
			daemonThread.start();
			
			return JAVA_COMPONENT_SUCCESS;
		} catch(Exception e) {
			logger.error("Exception Occured: "+e,e);
		}
		return JAVA_COMPONENT_FAILURE;
	}

	@Override
	public void stopComponent() {
		ugcBinaryTransferDaemon.stopThread();		
	}
}