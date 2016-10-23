package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.tools.*;
import com.onmobile.common.exception.OnMobileException;

public class ReminderCleanUp extends Thread implements ConstantsTools
{
	private static Logger logger = Logger.getLogger(ReminderCleanUp.class);
	
	public static ReminderCleanUp reminderCleanUp = null;
	public static Object lock = new Object();
	public static Date runDate = null;
	public static ReminderCleanUp getInstance()
	{
		if(reminderCleanUp != null)
			return reminderCleanUp;
		synchronized (lock)
		{
			if(reminderCleanUp != null)
				return reminderCleanUp;
			reminderCleanUp = new ReminderCleanUp();
			reminderCleanUp.start();
			return reminderCleanUp;
		}
	}
	
	
	public void run()
	{
		while(true)
		{
			logger.info("Going to call bulkDeleteTnbSubscriber");
			try
			{
				bulkDeleteTnbSubscriber();
			}
			catch (OnMobileException oe)
			{
				logger.error("", oe);
			}
			logger.info("After bulkDeleteTnbSubscriber");
			logger.info("Going to call bulkDeleteTrialSelection");
			try
			{
				bulkDeleteTrialSelection();
			}
			catch (OnMobileException oe)
			{
				logger.error("", oe);
			}
			logger.info("After bulkDeleteTrialSelection");
			try
			{
				Thread.sleep(24*60*60*1000);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}


	private void bulkDeleteTrialSelection() throws OnMobileException
	{
		DBConfigTools.bulkDeleteTrialSelection();
	}


	private void bulkDeleteTnbSubscriber() throws OnMobileException
	{
		DBConfigTools.bulkDeleteTnbSubscriber();
	}
}
