package com.onmobile.apps.ringbacktones.logger.consent;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class ConsentCallbackTransLogger
{
	static Logger transLogger;
	static Logger logger = Logger.getLogger(ConsentCallbackTransLogger.class);

	static
	{
		try
		{
			String loggerName = "CONSENT_CALLBACK_TRANS";
			transLogger = RbtLogger.createRollingFileLogger(RbtLogger.consentCallbackTransPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing Consent Callback Trans Transaction Logger", e);
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
				logger.error("Issue in writing consent callback trans hit logs", e);
		}
	}
}
