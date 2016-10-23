package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;

public class UGCDaemonStarter {
	
	private static Logger logger = Logger.getLogger(UGCDaemonStarter.class);
	
	public UGCDaemonStarter(){
		Tools.init("UGCDaemonStarter", true);
	 	logger.info("Started Initialising");
	 	
	}
	
	/**
	 * initialise method
	 */
	public void init(){
//		UGCContentUploadDaemon ugContentUploadDaemon = new UGCContentUploadDaemon();
//		ugContentUploadDaemon.start();
		UGCContentPopulateDaemon ugContentPopulateDaemon = new UGCContentPopulateDaemon();
		ugContentPopulateDaemon.start();
	}
	public static void main(String[] args) {
		UGCDaemonStarter ugcDaemonStarter = new UGCDaemonStarter();
		ugcDaemonStarter.init();
	}

}
