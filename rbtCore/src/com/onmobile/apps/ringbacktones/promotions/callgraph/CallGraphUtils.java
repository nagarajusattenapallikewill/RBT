/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 * 
 */
public class CallGraphUtils
{
	private static Logger logger = Logger.getLogger(CallGraphUtils.class);

	public static CallGraph createCallGraphFromTonePlayerCDR(
			TonePlayerCDR tonePlayerCDR)
	{
		try
		{
			CallGraph callGraph = new CallGraph();
			callGraph.setSubscriberID(tonePlayerCDR.getCalledID());

			Map<String, Set<Long>> callersDetails = new HashMap<String, Set<Long>>();
			callersDetails.put(tonePlayerCDR.getCallerID(), Collections
					.singleton(tonePlayerCDR.getCalledTime().getTime()));
			callGraph.setCallersDetails(callersDetails);

			callGraph.setRbtClipID(tonePlayerCDR.getClipID());

			CallGraphDao.save(callGraph);

			return callGraph;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public static void updateCallGraphWithTonePlayerCDR(CallGraph callGraph,
			TonePlayerCDR tonePlayerCDR)
	{
		try
		{
			Map<String, Set<Long>> callersDetails = callGraph
					.getCallersDetails();
			String callerID = tonePlayerCDR.getCallerID();

			Set<Long> calledTimes = callersDetails.get(callerID);
			if (calledTimes == null || calledTimes.size() == 0)
			{
				callersDetails
						.put(callerID, Collections.singleton(tonePlayerCDR
								.getCalledTime().getTime()));

				return;
			}

			calledTimes.add(tonePlayerCDR.getCalledTime().getTime());

			Set<String> frequentCallers = callGraph.getFrequentCallers();
			if (frequentCallers == null)
			{
				frequentCallers = new HashSet<String>();
				callGraph.setFrequentCallers(frequentCallers);
			}

			if (!frequentCallers.contains(callerID)
					&& isFrequentCaller(calledTimes))
			{
				frequentCallers.add(callerID);
			}
			else if (frequentCallers.isEmpty())
				callGraph.setFrequentCallers(null);

			callGraph.setUpdatedTime(new Date());
			CallGraphDao.update(callGraph);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private static boolean isFrequentCaller(Set<Long> calledTimes)
	{
		int nonBusinessHoursCallCount = 0;

		Calendar calendar = Calendar.getInstance();
		for (Long calledTime : calledTimes)
		{
			calendar.setTimeInMillis(calledTime);
			if (!isCalledTimeInBusinessHours(calendar))
				nonBusinessHoursCallCount++;
		}

		return (nonBusinessHoursCallCount > 2);
	}

	private static boolean isCalledTimeInBusinessHours(Calendar calendar)
	{
		int calledDay = calendar.get(Calendar.DAY_OF_WEEK);
		if (calledDay == Calendar.SATURDAY || calledDay == Calendar.SUNDAY)
			return false;

		int calledHour = calendar.get(Calendar.HOUR_OF_DAY);
		if (calledHour >= 9 && calledHour <= 18)
			return true;

		return false;
	}
}
