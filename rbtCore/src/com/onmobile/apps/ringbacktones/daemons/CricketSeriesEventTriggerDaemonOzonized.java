package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 * 
 */
public class CricketSeriesEventTriggerDaemonOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(CricketSeriesEventTriggerDaemonOzonized.class);

	private CricketSeriesEventTriggerDaemon cricketSeriesEventTriggerDaemon = null;
	private static final String COMPONENT_NAME = "CricketSeriesEventTrigger";

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		try
		{
			cricketSeriesEventTriggerDaemon = CricketSeriesEventTriggerDaemon.getInstance();
			cricketSeriesEventTriggerDaemon.setName("CRICKET_SERIES_EVENT_TRIGGER_DAEMON_OZONIFIED");
			cricketSeriesEventTriggerDaemon.start();

			return JAVA_COMPONENT_SUCCESS;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return JAVA_COMPONENT_FAILURE;
	}

	@Override
	public void stopComponent()
	{
		cricketSeriesEventTriggerDaemon.stopThread();
	}
}
