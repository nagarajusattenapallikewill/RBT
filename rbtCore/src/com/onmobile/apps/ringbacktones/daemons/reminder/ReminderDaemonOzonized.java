package com.onmobile.apps.ringbacktones.daemons.reminder;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

public class ReminderDaemonOzonized extends Ozonized
{
	private static Logger logger = Logger
			.getLogger(ReminderDaemonOzonized.class);

	private static final String COMPONENT_NAME = "ReminderDaemon";

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
			ReminderDaemon reminderDaemon = ReminderDaemon.getInstance();
			reminderDaemon.start();
			logger.info("Reminder Daemon start");
		}
		catch (Throwable t)
		{
			logger.info("Exception while starting ReminderDaemon.");
			logger.error("", t);
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{

		ReminderDaemon reminderDaemon = ReminderDaemon.getInstance();
		reminderDaemon.stop();
	}
}
