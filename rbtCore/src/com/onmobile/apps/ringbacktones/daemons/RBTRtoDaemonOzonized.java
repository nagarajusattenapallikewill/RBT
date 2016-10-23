package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.RBTRtoExecutor;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class RBTRtoDaemonOzonized extends Ozonized {

	private static Logger logger = Logger.getLogger(RBTRtoDaemonOzonized.class);

	private RBTRtoExecutor rtoDaemon = null;
	private static final String COMPONENT_NAME = "RBTRtoDaemonOzonized";

	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent() {
		try {
			rtoDaemon = new RBTRtoExecutor();
			rtoDaemon.setName("RBT_RTO_DAEMON_OZONIFIED");
			rtoDaemon.start();
			return JAVA_COMPONENT_SUCCESS;
		} catch (Exception e) {
			logger.error("", e);
		}

		return JAVA_COMPONENT_FAILURE;
	}

	@Override
	public void stopComponent() {
		rtoDaemon.stopThread();
	}

}
