/**
 * 
 */
package com.onmobile.apps.ringbacktones.Gatherer.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.CopyBootstrapOzonized;
import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.QueuePerformance;
import com.onmobile.apps.ringbacktones.hunterFramework.ThreadManager;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceContext;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.Severity;

/**
 * Monitors the performance of the Gatherer component. Logs the performance of
 * gatherer hunters and raises alarms if there are any issues in the
 * application.
 * 
 * @author vinayasimha.patil
 */
public class PerformanceMonitorDaemon extends ManagedDaemon
{
	private static Logger logger = Logger.getLogger(PerformanceMonitorDaemon.class);
	/**
	 * Holds the reference of PerformanceMonitorDaemon. This is used make this
	 * class as singleton.
	 */
	private static PerformanceMonitorDaemon performanceMonitorDaemon = null;

	/**
	 * Sleep interval constant for this daemon. After completing one iteration
	 * daemon will sleep for this amount of time. And also this represents the
	 * one time slot. Sleep Interval is defined in milliseconds.
	 */
	private static final long SLEEP_INTERVAL = 15000;

	/**
	 * Keeps track of the number of time slots from the start of the daemon.
	 */
	private long timeSlots = 0;

	/**
	 * Holds the reference of PerformanceMonitor for memory utilization.
	 */
	private PerformanceMonitor memoryUtilizationMonitor = null;

	/**
	 * Holds the references of PerformanceMonitors for DB Queue monitoring.
	 */
	private Map<String, PerformanceMonitor> dbQueueMonitors = null;

	/**
	 * Holds the references of PerformanceMonitors for Hunter Queue monitoring.
	 */
	private Map<String, PerformanceMonitor> inMemoryQueueMonitors = null;

	/**
	 * Holds the references of PerformanceMonitors for worker threads turn
	 * around time monitoring.
	 */
	private Map<String, PerformanceMonitor> workerTATMonitors = null;

	/**
	 * Holds the references of PerformanceMonitors for total(End-to-End) turn
	 * around time monitoring.
	 */
	private Map<String, PerformanceMonitor> totalTATMonitors = null;

	/**
	 * Holds the time interval value for memory utilization monitoring.
	 */
	private int noOfTimeSlotsForMemoryUtilization = 0;

	/**
	 * Holds the time interval value for DB Queue monitoring.
	 */
	private int noOfTimeSlotsForDBQueue = 0;

	/**
	 * Holds the time interval value for Hunter Queue monitoring.
	 */
	private int noOfTimeSlotsForInMemoryQueue = 0;

	/**
	 * Holds the time interval value for worker threads turn around time
	 * monitoring.
	 */
	private int noOfTimeSlotsForWorkerTAT = 0;

	/**
	 * Holds the time interval value for total(End-to-End) turn around time
	 * monitoring.
	 */
	private int noOfTimeSlotsForTotalTAT = 0;

	/**
	 * Constructs PerformanceMonitorDaemon and registers the daemon in
	 * {@link ThreadManager}
	 */
	private PerformanceMonitorDaemon()
	{
		initPerformanceMonitors();
		initTimeSlots();

		setUniqueName("PerformanceMonitor");
		ThreadManager.getThreadManager().addManagedThread(this);
	}

	/**
	 * Starts the performance monitor daemon and returns the reference for the
	 * same. If the daemon has already started, then returns the reference for
	 * the already running daemon.
	 * 
	 * @return the PerformanceMonitorDaemon instance
	 */
	public static PerformanceMonitorDaemon startPerformanceMonitorDaemon()
	{
		if (performanceMonitorDaemon == null)
		{
			synchronized (PerformanceMonitorDaemon.class)
			{
				if (performanceMonitorDaemon == null)
				{
					performanceMonitorDaemon = new PerformanceMonitorDaemon();
					logger.info("RBT:: PerformanceMonitorDaemon started");
				}
			}
		}

		return performanceMonitorDaemon;
	}

	/**
	 * Creates the required PerformanceMonitors for Memory Utilization, DB
	 * Queues, InMemory Queues and Turn Around Time monitoring.
	 */
	private void initPerformanceMonitors()
	{
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.GATHERER, "pir.memoryUtilization.enable", "false");
		if (parameter.getValue().equalsIgnoreCase("true"))
		{
			String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
			memoryUtilizationMonitor = PerformanceMonitorFactory
					.newPerformanceMonitor(componentName, "Memory Utilization",
							PerformanceDataType.FLOAT, "Megabytes");
		}

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.dbQueue.enable", "false");
		boolean dbQueueMonitoringEnabled = parameter.getValue()
				.equalsIgnoreCase("true");
		if (dbQueueMonitoringEnabled)
			dbQueueMonitors = new HashMap<String, PerformanceMonitor>();

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.inMemoryQueue.enable", "false");
		boolean inMemoryQueueMonitoringEnabled = parameter.getValue()
				.equalsIgnoreCase("true");
		if (inMemoryQueueMonitoringEnabled)
			inMemoryQueueMonitors = new HashMap<String, PerformanceMonitor>();

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.workerTurnAroundTime.enable", "false");
		boolean workerTATMonitoringEnabled = parameter.getValue()
				.equalsIgnoreCase("true");
		if (workerTATMonitoringEnabled)
			workerTATMonitors = new HashMap<String, PerformanceMonitor>();

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.totalTurnAroundTime.enable", "false");
		boolean totalTATMonitoringEnabled = parameter.getValue()
				.equalsIgnoreCase("true");
		if (totalTATMonitoringEnabled)
			totalTATMonitors = new HashMap<String, PerformanceMonitor>();

		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> hunterMap = hunterContainer.getHunters();
		Collection<Hunter> hunters = hunterMap.values();
		for (Hunter hunter : hunters)
		{
			if (dbQueueMonitoringEnabled)
				createDBQueueMonitor(hunter);

			QueueContainer cidQueueContainer = hunter.getCidQueue();
			if (cidQueueContainer != null)
			{
				if (inMemoryQueueMonitoringEnabled)
					createInMemoryQueueMonitor(cidQueueContainer);
				if (workerTATMonitoringEnabled)
					createWorkerTATMonitor(cidQueueContainer);
			}

			HashMap<String, QueueContainer> siteQueueContainerMap = hunter
					.getSiteQueContainer();
			Collection<QueueContainer> queueContainers = siteQueueContainerMap
					.values();
			for (QueueContainer queueContainer : queueContainers)
			{
				if (inMemoryQueueMonitoringEnabled)
					createInMemoryQueueMonitor(queueContainer);
				if (workerTATMonitoringEnabled)
					createWorkerTATMonitor(queueContainer);
				if (totalTATMonitoringEnabled)
					createTotalTATMonitor(queueContainer);
			}
		}
	}

	/**
	 * Creates the PerformanceMonitor for DB Queue.
	 * 
	 * @param hunter
	 *            the hunter for which PerformanceMonitor has to be
	 *            created
	 */
	private void createDBQueueMonitor(Hunter hunter)
	{
		String hunterName = hunter.getHunterName();
		if (hunterName.equalsIgnoreCase(Utility.HunterNameFailedCopy))
		{
			// Ignoring Failed Copy hunter as data for this hunter will not be
			// there in the DB
			return;
		}

		String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
		String performanceMonitorName = hunterName + ": DB-Queue";

		PerformanceMonitor performanceMonitor = PerformanceMonitorFactory
				.newPerformanceMonitor(componentName, performanceMonitorName,
						PerformanceDataType.INTEGER, "Records");

		dbQueueMonitors.put(hunterName, performanceMonitor);
	}

	/**
	 * Creates the PerformanceMonitor for InMemory Queue.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which PerformanceMonitor has to be
	 *            created
	 */
	private void createInMemoryQueueMonitor(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();

		String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
		String performanceMonitorName = hunterName + ": " + queueContainerName
				+ "-Queue";

		PerformanceMonitor performanceMonitor = PerformanceMonitorFactory
				.newPerformanceMonitor(componentName, performanceMonitorName,
						PerformanceDataType.INTEGER, "Records");

		inMemoryQueueMonitors.put(hunterName + ":" + queueContainerName,
				performanceMonitor);
	}

	/**
	 * Creates the PerformanceMonitor for monitoring worker threads turn around
	 * time of
	 * QueueConatiner.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which worker threads turn around time
	 *            has to be monitored
	 */
	private void createWorkerTATMonitor(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();

		String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
		String performanceMonitorName = hunterName + ": " + queueContainerName
				+ "-Worker TurnAround Time";

		PerformanceMonitor performanceMonitor = PerformanceMonitorFactory
				.newPerformanceMonitor(componentName, performanceMonitorName,
						PerformanceDataType.LONG, "Milliseconds");
		workerTATMonitors.put(hunterName + ":" + queueContainerName,
				performanceMonitor);
	}

	/**
	 * Creates the PerformanceMonitor for monitoring total(End-to-End) turn
	 * around time of
	 * QueueConatiner.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which total(End-to-End) turn around
	 *            time has to be monitored
	 */
	private void createTotalTATMonitor(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();

		String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
		String performanceMonitorName = hunterName + ": " + queueContainerName
				+ "-Total TurnAround Time";

		PerformanceMonitor performanceMonitor = PerformanceMonitorFactory
				.newPerformanceMonitor(componentName, performanceMonitorName,
						PerformanceDataType.LONG, "Milliseconds");
		totalTATMonitors.put(hunterName + ":" + queueContainerName,
				performanceMonitor);
	}

	/**
	 * Calculates the time slots for Memory Utilization, DB Queues, InMemory
	 * Queues and Turn Around Time monitoring.
	 */
	private void initTimeSlots()
	{
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.GATHERER, "pir.memoryUtilization.interval", "15");
		noOfTimeSlotsForMemoryUtilization = (int) (Integer.parseInt(parameter
				.getValue()) / (SLEEP_INTERVAL / 1000));

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.dbQueue.interval", "300");
		noOfTimeSlotsForDBQueue = (int) (Integer.parseInt(parameter.getValue()) / (SLEEP_INTERVAL / 1000));

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.inMemoryQueue.interval", "60");
		noOfTimeSlotsForInMemoryQueue = (int) (Integer.parseInt(parameter
				.getValue()) / (SLEEP_INTERVAL / 1000));

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.workerTurnAroundTime.interval", "60");
		noOfTimeSlotsForWorkerTAT = (int) (Integer.parseInt(parameter
				.getValue()) / (SLEEP_INTERVAL / 1000));

		parameter = parametersCacheManager.getParameter(iRBTConstant.GATHERER,
				"pir.totalTurnAroundTime.interval", "60");
		noOfTimeSlotsForTotalTAT = (int) (Integer
				.parseInt(parameter.getValue()) / (SLEEP_INTERVAL / 1000));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon#execute()
	 */
	/**
	 * Checks the status of all monitors based on there time slots and sleeps
	 * for the time defined in {@link #SLEEP_INTERVAL}.
	 */
	@Override
	protected void execute()
	{
		timeSlots++;

		if (memoryUtilizationMonitor != null
				&& timeSlots % noOfTimeSlotsForMemoryUtilization == 0)
			checkMemoryUtilization();
		if (dbQueueMonitors != null && timeSlots % noOfTimeSlotsForDBQueue == 0)
			checkDBQueueStatus();
		if (inMemoryQueueMonitors != null
				&& timeSlots % noOfTimeSlotsForInMemoryQueue == 0)
			checkInMemoryQueueStatus();
		if (workerTATMonitors != null
				&& timeSlots % noOfTimeSlotsForWorkerTAT == 0)
			checkWorkerTAT();
		if (totalTATMonitors != null
				&& timeSlots % noOfTimeSlotsForTotalTAT == 0)
			checkTotalTAT();

		try
		{
			Thread.sleep(SLEEP_INTERVAL);
		}
		catch (InterruptedException e)
		{
			logger.error("", e);
		}
	}

	/**
	 * Checks the status of memory utilization and logs the used JVM memory in
	 * megabytes(MB).
	 */
	private void checkMemoryUtilization()
	{
		Runtime runtime = Runtime.getRuntime();

		long maxMemory = runtime.maxMemory();
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
				.getParameter(iRBTConstant.GATHERER,
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
					iRBTConstant.GATHERER,
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

	/**
	 * Checks the DB Queue status for all register hunters.
	 */
	private void checkDBQueueStatus()
	{
		HashMap<String, Integer> queueSizeMap = getDBQueueSize();

		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> hunterMap = hunterContainer.getHunters();
		Collection<Hunter> hunters = hunterMap.values();
		for (Hunter hunter : hunters)
		{
			if (hunter.getHunterName().equalsIgnoreCase(
					Utility.HunterNameFailedCopy))
			{
				// Ignoring Failed Copy hunter as data for this hunter will not
				// be there in the DB
				continue;
			}

			logDBQueueStatus(hunter, queueSizeMap);
		}
	}

	/**
	 * Queries the DB to get the queue size for all hunters.
	 * 
	 * @return the map of queue size for all hunters
	 */
	private HashMap<String, Integer> getDBQueueSize()
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String[] smsTypes = { "COPY", "COPYSTAR", "COPYCONFIRMED",
				"COPYCONFIRM" };
		HashMap<String, Integer> queueSizeMap = rbtDBManager
				.getCountForViralSmsTypes(smsTypes);

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.GATHERER, "WAIT_TIME_DOUBLE_CONFIRMATION", "30");
		int waitTime = Integer.parseInt(parameter.getValue().trim());

		int expiredCopyCount = rbtDBManager.getCountForViralSmsType(
				"COPYCONFPENDING", waitTime);
		queueSizeMap.put("COPYCONFPENDING", expiredCopyCount);

		return queueSizeMap;
	}

	/**
	 * Logs the DB Queue status. And raises the alarm if reached the threshold.
	 * 
	 * @param hunter
	 *            the hunter for which DB Queue status has to be logged
	 * @param queueSizeMap
	 *            the map containing queue size
	 */
	private void logDBQueueStatus(Hunter hunter,
			HashMap<String, Integer> queueSizeMap)
	{
		String hunterName = hunter.getHunterName();
		PerformanceMonitor performanceMonitor = dbQueueMonitors.get(hunterName);

		int queueSize = 0;
		String copyType = Utility.getCopyTypeForHunter(hunterName);
		if (queueSizeMap.containsKey(copyType))
			queueSize = queueSizeMap.get(copyType);

		PerformanceContext performanceContext = new PerformanceContext(
				queueSize);
		performanceMonitor.logPerformance(performanceContext);

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		boolean alarmRaised = false;

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.GATHERER, "alarm.queueSize.upperThreshold");
		if (parameter != null)
		{
			// Raising CRITICAL alarm if queue size crossed upper threshold.
			int upperThreshold = Integer.parseInt(parameter.getValue().trim());
			if (queueSize >= upperThreshold)
			{
				performanceContext = new PerformanceContext(Severity.CRITICAL,
						"Queue size reached upper threshold (" + upperThreshold
								+ "). Queue Size: " + queueSize);
				alarmRaised = performanceMonitor.raiseAlarm(performanceContext);
			}
		}

		if (!alarmRaised)
		{
			// Raising WARNING alarm if not raised as CRITICAL and queue size
			// crossed lower threshold.
			parameter = parametersCacheManager.getParameter(
					iRBTConstant.GATHERER, "alarm.queueSize.lowerThreshold");
			if (parameter != null)
			{
				int lowerThreshold = Integer.parseInt(parameter.getValue()
						.trim());
				if (queueSize >= lowerThreshold)
				{
					performanceContext = new PerformanceContext(
							Severity.WARNING,
							"Queue size reached lower threshold ("
									+ lowerThreshold + "). Queue Size: "
									+ queueSize);
					alarmRaised = performanceMonitor
							.raiseAlarm(performanceContext);
				}
			}
		}

		if (!alarmRaised)
		{
			// Clearing the alarm as queue size is normal (below threshold).
			performanceContext = new PerformanceContext(Severity.CLEAR,
					"Queue size is normal (below threshold). Queue Size: "
							+ queueSize);
			performanceMonitor.clearAlarm(performanceContext);
		}
	}

	/**
	 * Checks the InMemeory Queue status for all queue containers of register
	 * hunters.
	 */
	private void checkInMemoryQueueStatus()
	{
		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> hunterMap = hunterContainer.getHunters();
		Collection<Hunter> hunters = hunterMap.values();
		for (Hunter hunter : hunters)
		{
			QueueContainer cidQueueContainer = hunter.getCidQueue();
			if (cidQueueContainer != null)
				logInMemoryQueueStatus(cidQueueContainer);

			HashMap<String, QueueContainer> siteQueueContainerMap = hunter
					.getSiteQueContainer();
			Collection<QueueContainer> queueContainers = siteQueueContainerMap
					.values();
			for (QueueContainer queueContainer : queueContainers)
			{
				logInMemoryQueueStatus(queueContainer);
			}
		}
	}

	/**
	 * Logs the InMemory Queue status.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which Queue status has to be logged
	 */
	private void logInMemoryQueueStatus(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();
		PerformanceMonitor performanceMonitor = inMemoryQueueMonitors
				.get(hunterName + ":" + queueContainerName);

		int queueSize = queueContainer.getQueueSize();
		PerformanceContext performanceContext = new PerformanceContext(
				queueSize);
		performanceMonitor.logPerformance(performanceContext);
	}

	/**
	 * Checks the worker threads turn around time status for all queue
	 * containers of registered hunters.
	 */
	private void checkWorkerTAT()
	{
		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> hunterMap = hunterContainer.getHunters();
		Collection<Hunter> hunters = hunterMap.values();
		for (Hunter hunter : hunters)
		{
			QueueContainer cidQueueContainer = hunter.getCidQueue();
			if (cidQueueContainer != null)
				logWorkerTAT(cidQueueContainer);

			HashMap<String, QueueContainer> siteQueueContainerMap = hunter
					.getSiteQueContainer();
			Collection<QueueContainer> queueContainers = siteQueueContainerMap
					.values();
			for (QueueContainer queueContainer : queueContainers)
			{
				logWorkerTAT(queueContainer);
			}
		}
	}

	/**
	 * Logs worker threads turn around time status for all queue containers of
	 * registered hunters. And raises the alarm if reached the threshold.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which worker threads turn around time
	 *            status has to be logged
	 */
	private void logWorkerTAT(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();
		PerformanceMonitor performanceMonitor = workerTATMonitors
				.get(hunterName + ":" + queueContainerName);

		QueuePerformance queuePerformance = queueContainer
				.getQueuePerformance();
		long turnAroundTime = queuePerformance.getWorkerTAT()
				.getPresentTrunAroundTime();
		PerformanceContext performanceContext = new PerformanceContext(
				turnAroundTime);
		performanceMonitor.logPerformance(performanceContext);

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		boolean alarmRaised = false;

		Parameters parameter = parametersCacheManager.getParameter(
				iRBTConstant.GATHERER,
				"alarm.workerTurnAroundTime.upperThreshold");
		if (parameter != null)
		{
			// Raising CRITICAL alarm if turn around time crossed upper
			// threshold.
			long upperThreshold = Long.parseLong(parameter.getValue().trim());
			if (turnAroundTime >= upperThreshold)
			{
				performanceContext = new PerformanceContext(Severity.CRITICAL,
						"Worker Threads turn around reached upper threshold ("
								+ upperThreshold + "ms). Turn Around Time: "
								+ turnAroundTime + "ms");
				alarmRaised = performanceMonitor.raiseAlarm(performanceContext);
			}
		}

		if (!alarmRaised)
		{
			// Raising WARNING alarm if not raised as CRITICAL and turn around
			// time crossed lower threshold.
			parameter = parametersCacheManager.getParameter(
					iRBTConstant.GATHERER,
					"alarm.workerTurnAroundTime.lowerThreshold");
			if (parameter != null)
			{
				long lowerThreshold = Long.parseLong(parameter.getValue()
						.trim());
				if (turnAroundTime >= lowerThreshold)
				{
					performanceContext = new PerformanceContext(
							Severity.WARNING,
							"Worker Threads turn around reached lower threshold ("
									+ lowerThreshold
									+ "ms). Turn Around Time: "
									+ turnAroundTime + "ms");
					alarmRaised = performanceMonitor
							.raiseAlarm(performanceContext);
				}
			}
		}

		if (!alarmRaised)
		{
			// Clearing the alarm as turn around time is normal (below
			// threshold).
			performanceContext = new PerformanceContext(
					Severity.CLEAR,
					"Worker Threads turn around time is normal (below threshold). Turn Around Time: "
							+ turnAroundTime + "ms");
			performanceMonitor.clearAlarm(performanceContext);
		}
	}

	/**
	 * Checks the total(End-to-End) turn around time status for all queue
	 * containers of registered hunters.
	 */
	private void checkTotalTAT()
	{
		HunterContainer hunterContainer = HunterContainer.getHunterContainer();
		HashMap<String, Hunter> hunterMap = hunterContainer.getHunters();
		Collection<Hunter> hunters = hunterMap.values();
		for (Hunter hunter : hunters)
		{
			HashMap<String, QueueContainer> siteQueueContainerMap = hunter
					.getSiteQueContainer();
			Collection<QueueContainer> queueContainers = siteQueueContainerMap
					.values();
			for (QueueContainer queueContainer : queueContainers)
			{
				logTotalTAT(queueContainer);
			}
		}
	}

	/**
	 * Logs total(End-to-End) turn around time status for all queue containers
	 * of registered hunters.
	 * 
	 * @param queueContainer
	 *            the queueContainer for which total(End-to-End) turn around
	 *            time status has to be logged
	 */
	private void logTotalTAT(QueueContainer queueContainer)
	{
		String hunterName = queueContainer.getHunter().getHunterName();
		String queueContainerName = queueContainer.getQueueContainerName();
		PerformanceMonitor performanceMonitor = totalTATMonitors.get(hunterName
				+ ":" + queueContainerName);

		QueuePerformance queuePerformance = queueContainer
				.getQueuePerformance();
		long turnAroundTime = queuePerformance.getTotalTAT()
				.getPresentTrunAroundTime();
		PerformanceContext performanceContext = new PerformanceContext(
				turnAroundTime);
		performanceMonitor.logPerformance(performanceContext);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon#getLockObject
	 * ()
	 */
	@Override
	public Object getLockObject()
	{
		return null;
	}
}
