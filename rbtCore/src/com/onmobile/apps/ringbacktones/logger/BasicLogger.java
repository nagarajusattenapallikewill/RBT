package com.onmobile.apps.ringbacktones.logger;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class BasicLogger
{
	private static Logger selectionLogger;
	private static Logger downloadLogger;
	private static Logger subscribernLogger;
	private static Logger logger = Logger.getLogger(BasicLogger.class);
	static
	{
		try
		{
			String selectionsLoggerName = "SELECTIONS";
			String downloadsLoggerName = "DOWNLOADS";
			String baseLoggerName = "BASE";
			
			selectionLogger = RbtLogger.createRollingFileLogger(RbtLogger.incomingPrefix + selectionsLoggerName, ROLLING_FREQUENCY.HOURLY);
			downloadLogger = RbtLogger.createRollingFileLogger(RbtLogger.incomingPrefix + downloadsLoggerName, ROLLING_FREQUENCY.HOURLY);
			subscribernLogger = RbtLogger.createRollingFileLogger(RbtLogger.incomingPrefix + baseLoggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing Basic Logger", e);
		}
	}
	
	public static void logSelection(Subscriber sub, String response, String mode, String clipId, String clipType, Categories categories,
			char loopStatus, String classType, String callerID, int status, String sel_status, int rbtType, String selInterval,
			String prepaid)
	{
	
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(",");
		strBuilder.append(sub.subID()).append(",");
		strBuilder.append(sub.circleID()).append(",");
		strBuilder.append(response).append(",");
		strBuilder.append(mode).append(",");
		strBuilder.append(clipId).append(",");
		strBuilder.append(clipType).append(",");
		strBuilder.append(categories.id()).append(",");
		strBuilder.append(categories).append(",");
		strBuilder.append(loopStatus).append(",");
		strBuilder.append(classType).append(",");
		strBuilder.append(callerID).append(",");
		strBuilder.append(status).append(",");
		strBuilder.append(sel_status).append(",");
		strBuilder.append(rbtType).append(",");
		if(selInterval != null && selInterval.split(",").length == 7)
			selInterval = null;
		if(selInterval != null)
			selInterval = selInterval.replaceAll(",", ";");
		strBuilder.append(selInterval).append(",");
		strBuilder.append(prepaid);
		//TIMESTAMP,MSISDN,CIRCLE_ID,RESPONSE,MODE,CLIP_ID,CLIP_TYPE,CAT_ID,CAT_TYPE,LOOP,CHARGE_CLASS,CALLER_ID,
		//STATUS,SEL_STATUS,SUB_YES,SEL_TYPE,SEL_INTERVAL,END_TIME,SUB_TYPE
		
	}
	
	public static void logDownload()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		
	}
	
	public static void logBase()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		strBuilder.append(2).append(",");
		
	}
}
