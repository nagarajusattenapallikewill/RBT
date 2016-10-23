package com.onmobile.apps.ringbacktones.logger.consent;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

public class ConsentCallbackHitLogger
{
	static Logger cgUrlLogger;
	static Logger smActualUrlLogegr; 
	static Logger logger = Logger.getLogger(ConsentCallbackHitLogger.class);
	
	public static final String URL = "URL";
	public static final String TIME_TAKEN = "TIME_TAKEN";
	public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
	
	static
	{
		try
		{
			String loggerName = "CG_CALLBACK_URL";
			cgUrlLogger = RbtLogger.createRollingFileLogger(RbtLogger.cgCallbackPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing CG callback url Logger", e);
		}
	}
	
	//TIMESTAMP, HTTP_RESPONSE, TIME_TAKEN, URL

	public static void log(String response, long timeTaken, Task task)
	{
		try
		{
			if(!cgUrlLogger.isEnabledFor(Level.INFO))
				return;
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(",");
			
			if(response != null)
			{
				response = response.trim();
				if(response.indexOf(",") != -1)
					response = response.replaceAll(",", ";");
			}
			strBuilder.append(response).append(",");
			strBuilder.append(timeTaken).append(",");
			strBuilder.append(task).append(",");
			
			cgUrlLogger.info(strBuilder.toString());
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing  cg callback url hit logs", e);
		}
	}
}
