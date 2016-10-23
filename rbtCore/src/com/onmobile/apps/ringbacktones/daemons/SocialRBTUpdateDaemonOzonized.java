package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;

public class SocialRBTUpdateDaemonOzonized extends Ozonized {

	private static Logger logger = Logger
			.getLogger(SocialRBTUpdateDaemonOzonized.class);

	private static final String COMPONENT_NAME = "SocialRBTUpdateDaemon";

	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {
		logger.info("Starting SocialRBTBootStap");
		SocialRBTBootStrap.startUp();
		@SuppressWarnings("unused")
		Log4jErrorConnector r = new Log4jErrorConnector();
		@SuppressWarnings("unused")
		Log4jSysOutConnector l = new Log4jSysOutConnector();
		logger.info("Started SocialRBTBootStap");
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent() {

	}
}
