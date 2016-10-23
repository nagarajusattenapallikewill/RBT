package com.onmobile.apps.ringbacktones.logger;

import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class ContestInfluencerWhilelistLogger
{
	static Logger contestInfluencerWhilelistLogger;
	static Logger logger = Logger.getLogger(CallBackLogTool.class);
	public static final String MSISDN = "MSISDN";
	public static final String CIRCLE_ID = "CIRCLE_ID";
	
	
	static
	{
		try
		{
			String loggerName = "CONTEST_INFLUENCER_WHITELIST";
			contestInfluencerWhilelistLogger = RbtLogger.createRollingFileLogger(RbtLogger.contestInfluencerWhitelistPrefix + loggerName, ROLLING_FREQUENCY.DAILY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing SM Daemon Transaction Logger", e);
		}
	}
	
	public static void writeWhiteListedNumber(Subscriber subscriber)
	{
		try
		{
			if(!contestInfluencerWhilelistLogger.isEnabledFor(Level.INFO))
				return;
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(",");
			strBuilder.append(subscriber.subID()).append(",");
			strBuilder.append(subscriber.circleID());

			contestInfluencerWhilelistLogger.info(strBuilder.toString());
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing rbt to sm http hit logs", e);
		}
	}
}
