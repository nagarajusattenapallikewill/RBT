package com.onmobile.apps.ringbacktones.daemons;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.StopwatchSample;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceContext;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.Severity;

public class SMDaemonPerformanceMonitor
{
	private static Logger logger = Logger.getLogger(SMDaemonPerformanceMonitor.class);
	
	private static SMDaemonPerformanceMonitor smDaemonPerformanceMonitor = null;
	
	private static Map<String, PerformanceMonitor> dbQueueMonitors = null;
	
	private static Map<String, PerformanceMonitor> tpsMonitors = null;
	private static Map<String, Split> tpsSimonHolder = null;
	private static Map<String, Integer> memoryQueueCount = null;
	
	private PerformanceMonitor memoryUtilizationMonitor = null;
	
	protected static final Timer timer = new Timer();
	
	private SMDaemonPerformanceMonitor()
	{
		initPerformanceMonitors();
	}

	public static SMDaemonPerformanceMonitor startSMPerformanceMonitorDaemon()
	{
		if (smDaemonPerformanceMonitor == null)
		{
			synchronized (SMDaemonPerformanceMonitor.class)
			{
				if (smDaemonPerformanceMonitor == null)
				{
					smDaemonPerformanceMonitor = new SMDaemonPerformanceMonitor();
					
					logger.info("RBT:: SMPerformanceMonitorDaemon started");
				}
			}
		}
		return smDaemonPerformanceMonitor;
	}
	
	
	public void initPerformanceMonitors()
	{
		dbQueueMonitors = new HashMap<String, PerformanceMonitor>();
	
		tpsMonitors = new HashMap<String, PerformanceMonitor>();
		tpsSimonHolder = new HashMap<String, Split>();
		memoryQueueCount = new HashMap<String, Integer>();
		
		
		String componentName = "SMDaemon";
		memoryUtilizationMonitor = PerformanceMonitorFactory
				.newPerformanceMonitor(componentName, "Memory Utilization",
						PerformanceDataType.FLOAT, "Megabytes");
		startSampling();
	}
	
	public static void recordDbQueueCount(String counterName, int count)
	{
		if (dbQueueMonitors == null)
			return;

		PerformanceMonitor performanceMonitor = dbQueueMonitors.get(counterName);
		if (performanceMonitor == null)
		{
			performanceMonitor = PerformanceMonitorFactory
					.newPerformanceMonitor("SMDaemon", counterName,
							PerformanceDataType.INTEGER, "Records");

			dbQueueMonitors.put(counterName, performanceMonitor);
		}
	
		performanceMonitor.logPerformance(new PerformanceContext(count));
	}
	
	public static void startTpsSampling(String threadName, int initCount)
	{
		/*try
		{
			if (tpsSimonHolder.containsKey(threadName))
				tpsSimonHolder.remove(threadName);
		
			Split split = SimonManager.getStopwatch(threadName).start();
			tpsSimonHolder.put(threadName, split);
			logger.info("count of record for threadName:"+threadName+" is "+initCount + ", split is " + split);
			memoryQueueCount.put(threadName, initCount);
		}
		catch(Exception e)
		{
			logger.error("Exception", e);
		}
	*/
	}
	
	public static void endTpsSampling(String threadName)
	{
		/*try
		{
			PerformanceMonitor performanceMonitor = tpsMonitors.get(threadName);
			if (performanceMonitor == null)
			{
				performanceMonitor = PerformanceMonitorFactory
						.newPerformanceMonitor("SMDaemon", threadName,
								PerformanceDataType.INTEGER, "TPS");
	
				tpsMonitors.put(threadName, performanceMonitor);
			}
			
			int tpsCount = 0;
			Split split = tpsSimonHolder.get(threadName);
			logger.info("split :"+split);
			if (split != null && split.isRunning())
			{
				split.stop();
				StopwatchSample sample = split.getStopwatch().sampleAndReset();
				long timeTakenForCycleInSec = (sample.getTotal()/(1000*1000*1000));
				if (timeTakenForCycleInSec > 0) {
					int initRecordsCounts = memoryQueueCount.get(threadName);
					logger.info("total in nanoseconds" + timeTakenForCycleInSec + " & count :" + initRecordsCounts);
					tpsCount = (int) (initRecordsCounts / timeTakenForCycleInSec);
					logger.info("tpsCount ::" + tpsCount);
				}
			}
			
			performanceMonitor.logPerformance(new PerformanceContext(tpsCount));
		}
		catch(Exception e)
		{
			logger.error("Exception", e);
		}
		*/
	}
	
	private void checkMemoryUtilization()
	{
		Runtime runtime = Runtime.getRuntime();

		long maxMemory = Runtime.getRuntime().maxMemory();
		long totalMemory = runtime.totalMemory();
		long usedMemory = totalMemory - runtime.freeMemory();

		float usedMemoryInMB = (float) usedMemory / (1024 * 1024);
		int memoryUsedInPercent = (int) (((float) usedMemory / maxMemory) * 100);

		PerformanceContext performanceContext = new PerformanceContext(
				usedMemoryInMB);
		memoryUtilizationMonitor.logPerformance(performanceContext);

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		boolean alarmRaised = false;

		Parameters parameter = parametersCacheManager
				.getParameter(iRBTConstant.DAEMON,
						"alarm.memoryUtilization.upperThreshold");
		if (parameter != null)
		{
			// Raising CRITICAL alarm if memory utilization crossed upper
			// threshold.
			int upperThreshold = Integer.parseInt(parameter.getValue().trim());
			if (memoryUsedInPercent >= upperThreshold)
			{
				performanceContext = new PerformanceContext(Severity.CRITICAL,
						"Memory utilization reached upper threshold ("
								+ upperThreshold + "%). Memory Used: "
								+ memoryUsedInPercent + "%");
				alarmRaised = memoryUtilizationMonitor
						.raiseAlarm(performanceContext);
			}
		}

		if (!alarmRaised)
		{
			// Raising WARNING alarm if not raised as CRITICAL and memory
			// utilization crossed lower threshold.
			parameter = parametersCacheManager.getParameter(
					iRBTConstant.DAEMON,
					"alarm.memoryUtilization.lowerThreshold");
			if (parameter != null)
			{
				int lowerThreshold = Integer.parseInt(parameter.getValue()
						.trim());
				if (memoryUsedInPercent >= lowerThreshold)
				{
					performanceContext = new PerformanceContext(
							Severity.WARNING,
							"Memory utilization reached lower threshold ("
									+ lowerThreshold + "%). Memory Used: "
									+ memoryUsedInPercent + "%");
					alarmRaised = memoryUtilizationMonitor
							.raiseAlarm(performanceContext);
				}
			}
		}

		if (!alarmRaised)
		{
			// Clearing the alarm as memory utilization is normal (below
			// threshold).
			performanceContext = new PerformanceContext(Severity.CLEAR,
					"Memory utilization is normal (below threshold). Memory Used: "
							+ memoryUsedInPercent + "%");
			memoryUtilizationMonitor.clearAlarm(performanceContext);
		}
	}
	
	protected class MemoryLogTimerTask extends TimerTask 
	{
		public void run() 
		{
			logger.info("checking memoryUtilisation");
			checkMemoryUtilization();
		}
		
	}
	protected void startSampling() 
	{
		timer.schedule(new MemoryLogTimerTask(), 2*60*1000, 3*60*1000);
	}
}
