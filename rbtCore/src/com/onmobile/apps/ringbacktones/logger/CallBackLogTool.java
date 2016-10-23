package com.onmobile.apps.ringbacktones.logger;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallback;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class CallBackLogTool
{
	static Logger callBackLogger;
	static Logger logger = Logger.getLogger(CallBackLogTool.class);
	static HashSet<String> trackedParams = new HashSet<String>();
	static
	{
		try
		{
			String loggerName = "SM_CALLBACKS";
			callBackLogger = RbtLogger.createRollingFileLogger(RbtLogger.rbtWebserverTransactionPrefix+loggerName, ROLLING_FREQUENCY.HOURLY);
			trackedParams.add("msisdn");trackedParams.add("action");trackedParams.add("status");trackedParams.add("type");
			trackedParams.add("srvkey");trackedParams.add("refid");trackedParams.add("amount_charged");trackedParams.add("cosid");
			trackedParams.add("mode");
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing SM Callback Transaction Logger", e);
		}
	}
	
	
	
	private static Object getEntity(Map<String, String[]> paramtersMap, SMCallback smCallback)
	{
	
		if(smCallback != null)
		{
			Class classObj = smCallback.getClass();
			String className = classObj.getName().toLowerCase();
			if(className.contains("gift"))
				return "GIFT";
			else if (className.contains("untick"))
				return "UNTICK";
			else if (className.contains("tick"))
				return "TICK";
		}
		String srvkey =  paramtersMap.get("srvkey") != null ? paramtersMap.get("srvkey")[0] : null;
		if(srvkey == null)
			return null;
		if(srvkey.toUpperCase().startsWith("RBT_ACT"))
			return "BASE";
		else if(srvkey.toUpperCase().startsWith("RBT_SEL"))
			return "CONTENT";
		
		return null;
	}

	public static void writeCallBackTransactionLog(Map<String, String[]> parametersMap, String rbtResponse, long timeDiff, String circleId, SMCallback smCallback)
	{
		try
		{
			if(!callBackLogger.isEnabledFor(Level.INFO))
				return;
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(",");
			strBuilder.append(timeDiff).append(",");
			strBuilder.append(parametersMap.get("msisdn") != null ? parametersMap.get("msisdn")[0] : null).append(",");
			strBuilder.append(circleId).append(",");
			
			strBuilder.append(getEntity(parametersMap, smCallback)).append(",");
			strBuilder.append(parametersMap.get("action") != null ? parametersMap.get("action")[0] : null).append(",");
			strBuilder.append(parametersMap.get("status") != null ? parametersMap.get("status")[0] : null).append(",");
			strBuilder.append(rbtResponse).append(",");
			strBuilder.append(parametersMap.get("mode") != null ? parametersMap.get("mode")[0] : null).append(",");
			strBuilder.append(parametersMap.get("srvkey") != null ? parametersMap.get("srvkey")[0] : null).append(",");
			strBuilder.append(parametersMap.get("cosid") != null ? parametersMap.get("cosid")[0] : null).append(",");
			strBuilder.append(parametersMap.get("type") != null ? parametersMap.get("type")[0] : null).append(",");
			strBuilder.append(parametersMap.get("refid") != null ? parametersMap.get("refid")[0] : null).append(",");
			strBuilder.append(parametersMap.get("amount_charged") != null ? parametersMap.get("amount_charged")[0] : null).append(",");
			strBuilder.append(getOtherParams(parametersMap));
			callBackLogger.info(strBuilder.toString());
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing callback logs", e);
		}
	}
	
	private static String getOtherParams(Map<String, String[]> parametersMap)
	{
		StringBuilder otherParamsBuilder = new StringBuilder();
		Object[] keyObjects = parametersMap.keySet().toArray();
		if(keyObjects == null || keyObjects.length == 0)
			return "";
		for(Object object : keyObjects)
		{
			String key = (String)object; 
			if(!trackedParams.contains(key))
			{
				String paramValue = parametersMap.get(key)[0];
				otherParamsBuilder.append(",").append(key).append("=").append(paramValue.replaceAll(",", ";"));
			}
		}
		String returnValue = otherParamsBuilder.toString();
		if(returnValue.startsWith(","))
			returnValue = returnValue.substring(1);
		return returnValue;
	}
}
