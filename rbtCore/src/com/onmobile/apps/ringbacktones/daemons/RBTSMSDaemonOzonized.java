package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;

public class RBTSMSDaemonOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(RBTSMSDaemonOzonized.class);
	private static final String COMPONENT_NAME = "RBTSMSDaemonManager";

	private RBTSMSDaemon rbtSMSDaemon = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		rbtSMSDaemon = new RBTSMSDaemon();
		@SuppressWarnings("unused")
		Log4jErrorConnector r = new Log4jErrorConnector();
		@SuppressWarnings("unused")
		Log4jSysOutConnector l = new Log4jSysOutConnector();
		logger.info("going to start sms daemon");
		rbtSMSDaemon.start();
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		if (rbtSMSDaemon != null)
			rbtSMSDaemon.stopThread();
	}
}
