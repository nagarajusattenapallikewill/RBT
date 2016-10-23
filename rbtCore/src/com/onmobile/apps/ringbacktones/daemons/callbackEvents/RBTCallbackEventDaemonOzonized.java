package com.onmobile.apps.ringbacktones.daemons.callbackEvents;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class RBTCallbackEventDaemonOzonized extends Ozonized
{
	private static Logger logger = Logger
			.getLogger(RBTCallbackEventDaemonOzonized.class);

	private static final String COMPONENT_NAME = "RBTCallbackEventDaemon";

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getComponentName()
	 */
	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		logger.info("inside startComponent");
		@SuppressWarnings("unused")
		Log4jSysOutConnector l = new Log4jSysOutConnector();
		RBTCallbackEventsBootStrap.startUp();
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{

	}
}
