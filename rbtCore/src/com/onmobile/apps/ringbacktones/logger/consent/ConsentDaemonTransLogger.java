package com.onmobile.apps.ringbacktones.logger.consent;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class ConsentDaemonTransLogger
{
	static Logger transLogger;
	static Logger logger = Logger.getLogger(ConsentDaemonTransLogger.class);

	static
	{
		try
		{
			String loggerName = "CONSENT_DAEMON_TRANS";
			transLogger = RbtLogger.createRollingFileLogger(RbtLogger.consentDaemonTransPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing Consent Daemon Trans Transaction Logger", e);
		}
	}
	
	public static void log(String reason, DoubleConfirmationRequestBean bean)
	{
		try
		{
			if(!transLogger.isEnabledFor(Level.INFO))
				return;
			transLogger.info(reason + "," + bean);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing consent daemon trans hit logs", e);
		}
	}
}
