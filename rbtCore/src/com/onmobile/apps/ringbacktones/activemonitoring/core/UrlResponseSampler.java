package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants.Severity;
import com.onmobile.snmp.agentx.client.SeverityLevel;
import com.onmobile.snmp.agentx.client.util.OMNotification;


/**
 * @author vasipalli.sreenadh
 *
 */
public class UrlResponseSampler  
{
	private static final Object syncObj = new Object();
	private List<MonitorData> list = new ArrayList<MonitorData>();
	private Map<String, OMNotification> notificationObjMap = new HashMap<String, OMNotification>();
	private ResourceBundle resourceBundle;
	private static final Logger logger = Logger.getLogger(UrlResponseSampler.class);
	
	RealTimeAlarmTask thread = null;
	
	private boolean isMonitoringEnabled = false;
	private int sleepTimeInSec = 10;

	private static UrlResponseSampler urlResponseSampler = null;
	
	public static UrlResponseSampler getInstance()
	{
		if (urlResponseSampler == null)
		{
			synchronized (syncObj) 
			{
				if (urlResponseSampler == null)
				{
					urlResponseSampler = new UrlResponseSampler();
				}
			}
		}
		return urlResponseSampler;
	}
	
	public UrlResponseSampler()
	{
		resourceBundle = ResourceBundle.getBundle("snmp");
		isMonitoringEnabled = resourceBundle.getString("isMonitoringEnabled").equalsIgnoreCase("true");
		sleepTimeInSec = Integer.parseInt(resourceBundle.getString("ThreadSleepTimeInSec"));
		if (isMonitoringEnabled)
		{	
			list = Collections.synchronizedList(new ArrayList<MonitorData>(100));
			thread = new RealTimeAlarmTask();
			thread.start();
		}
	}

	/**
	 *  Records the url responses 
	 */
	public void recordUrlResponse(MonitorData monitorData)
	{
		if (!isMonitoringEnabled)
			return;
		try
		{
			list.add(monitorData);
		}
		catch(Exception e)
		{
			logger.error("RBT:: Exception in recording urlresponse", e);
		}
		return;
	}
	
	public void stopAlarmTaskSchduler()
	{
		if (thread != null)
			thread.stop();
	}
	
	protected class RealTimeAlarmTask implements Runnable
	{
		private boolean execute = true;
		private Thread RealTimeAlarmTask  = null;
		
		public void start()
		{
			execute = true;
			RealTimeAlarmTask = new Thread(this);
			RealTimeAlarmTask.start();
		}
		public void stop()
		{
			execute = false;
			if (RealTimeAlarmTask != null)
				RealTimeAlarmTask.interrupt();
		}
		
		public void run()
		{
			while(execute)
			{
				try
				{
					List<MonitorData> listOld = list;
					List<MonitorData> newList = Collections.synchronizedList(new ArrayList<MonitorData>(100));
					list = newList;
					
					for (MonitorData monitorData : listOld) 
					{
						if (monitorData.getSeverity().equals(Severity.CLEAR))
						{
							if (notificationObjMap.get(monitorData.getMonitorKey()) == null)
							{
								logger.debug("Severity is clear and NotificationObj is null .. continue >"+monitorData.getMonitorKey());
								continue;
							}
							else
							{
								logger.debug(" clearing the alarm  monitorkey >"+monitorData.getMonitorKey());
								if (SNMPTrapRaiser.getInstance().clearAlarm(notificationObjMap.get(monitorData.getMonitorKey())))
									notificationObjMap.remove(monitorData.getMonitorKey());
							}
						}
						else
						{
							SeverityLevel severityLevel = getSeverityLevel(monitorData.getSeverity());
							OMNotification omNotification = notificationObjMap.get(monitorData.getMonitorKey());
							if (omNotification == null)
							{
								logger.debug("raising alarm >"+monitorData.getMonitorKey() +" & severity level >"+monitorData.getSeverity());
								omNotification = SNMPTrapRaiser.getInstance().raiseSNMPAlarm(monitorData.getComponentName(), monitorData.getMonitorKey(),
										severityLevel, monitorData.getSeverity(), monitorData.getMessage());
								
								if (omNotification != null)
									notificationObjMap.put(monitorData.getMonitorKey(), omNotification);
							}
							else if (isSevereAlarm(severityLevel, omNotification.getSeverityLevel()))
							{
								logger.debug("more severe alarm raised.. clearing old less severe alarm and raising new severe alarm");
								if (SNMPTrapRaiser.getInstance().clearAlarm(notificationObjMap.get(monitorData.getMonitorKey())))
								{
									omNotification = SNMPTrapRaiser.getInstance().raiseSNMPAlarm(monitorData.getComponentName(), monitorData.getMonitorKey(),
											severityLevel, monitorData.getSeverity(), monitorData.getMessage());
									
									if (omNotification != null)
										notificationObjMap.put(monitorData.getMonitorKey(), omNotification);
								}
							}
							else
								logger.debug("Already raised alarm.. ignoring");
						}
					}
				}
				catch(Exception e)
				{
					logger.error("RBT::Exception in RealTimeAlarmTask ", e);
				}
				
				try 
				{
					Thread.sleep(sleepTimeInSec*1000);
				} catch (InterruptedException e) 
				{
				}
			}
		}
	}
	
	private SeverityLevel getSeverityLevel(Severity severity)
	{
		if (severity == Severity.ERROR)
			return SeverityLevel.ERROR;
		else if (severity == Severity.CRITICAL)
			return SeverityLevel.CRITICAL;
		else if (severity == Severity.WARNING)
			return SeverityLevel.WARNING;
		else if (severity == Severity.INFO)
			return SeverityLevel.INFO;
		else if (severity == Severity.DEBUG)
			return SeverityLevel.DEBUG;
		else 
			return SeverityLevel.CLEAR_ALARM;
	}
	
	private boolean isSevereAlarm(SeverityLevel severity, SeverityLevel alarmSeverity)
	{
		if (alarmSeverity == SeverityLevel.CRITICAL)
			return false;
		else if ((alarmSeverity != SeverityLevel.CRITICAL) && (severity == SeverityLevel.CRITICAL))
			return true;
		else 
			return false;
	}
}
