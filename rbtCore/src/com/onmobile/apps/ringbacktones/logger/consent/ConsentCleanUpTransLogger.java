package com.onmobile.apps.ringbacktones.logger.consent;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class ConsentCleanUpTransLogger
{
	static Logger transLogger;
	static Logger logger = Logger.getLogger(ConsentCleanUpTransLogger.class);

	static
	{
		try
		{
			String loggerName = "CONSENT_CLEANUP_TRANS";
			transLogger = RbtLogger.createRollingFileLogger(RbtLogger.consentCleanupTransPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing Consent Cleanup Trans Transaction Logger", e);
		}
	}
	
	public static void log(DoubleConfirmationRequestBean bean)
	{
		try
		{
			if(!transLogger.isEnabledFor(Level.INFO))
				return;
			transLogger.info(bean);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing consent Cleanup trans hit logs", e);
		}
	}
}
