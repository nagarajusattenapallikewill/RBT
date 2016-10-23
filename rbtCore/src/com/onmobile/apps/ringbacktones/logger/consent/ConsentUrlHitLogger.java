package com.onmobile.apps.ringbacktones.logger.consent;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class ConsentUrlHitLogger
{
	static Logger cgUrlLogger;
	static Logger smActualUrlLogegr; 
	static Logger logger = Logger.getLogger(ConsentUrlHitLogger.class);
	
	public static final String URL = "URL";
	public static final String TIME_TAKEN = "TIME_TAKEN";
	public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
	public static final String HTTP_CODE = "HTTP_CODE";
	public static final String ENTITY = "ENTITY";
	
	
	public static final String ENTITY_BASE = "BASE";
	public static final String ENTITY_CONTENT = "SEL";
	public static final String ENTITY_COMBO_BASE = "COMBO";
	
	
	static
	{
		try
		{
			String loggerName = "CG_URL";
			cgUrlLogger = RbtLogger.createRollingFileLogger(RbtLogger.cgUrlPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing CG url Transaction Logger", e);
		}
	}
	
	//TIMESTAMP, HTTP_RESPONSE, HTTP_CODE, TIME_TAKEN, ENTITY, URL, PARAMS

	public static void log(HashMap<String, String> m, HashMap<String, String> params)
	{
		try
		{
			if(!cgUrlLogger.isEnabledFor(Level.INFO))
				return;
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(",");
			
			String response = m.get(HTTP_RESPONSE);
			if(response != null)
			{
				response = response.trim();
				if(response.indexOf(",") != -1)
					response = response.replaceAll(",", ";");
			}
			strBuilder.append(response).append(",");
			strBuilder.append(m.get(HTTP_CODE)).append(",");
			strBuilder.append(m.get(TIME_TAKEN)).append(",");
			strBuilder.append(m.get(ENTITY)).append(",");
			strBuilder.append(m.get(URL)).append(",");
			
			if(params != null && params.size() > 0)
			{
				Iterator<String> iterator = params.keySet().iterator();
				while(iterator.hasNext())
				{
					String key = iterator.next();
					String value = params.get(key);
					strBuilder.append(key);
					strBuilder.append("=");
					strBuilder.append(value);
					strBuilder.append("&");
				}	
			}
			cgUrlLogger.info(strBuilder.toString());
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing  bg url hit logs", e);
		}
	}
}
