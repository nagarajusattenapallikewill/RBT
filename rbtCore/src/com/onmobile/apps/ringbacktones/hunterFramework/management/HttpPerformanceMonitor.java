/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

import java.util.HashMap;

/**
 * If the application wants to monitor multiple http requests, then this class
 * can be used instead of creating and maintaining array performance monitors.
 * Http performance monitor contains the list of performance monitors
 * representing an URL.
 * 
 * @author vinayasimha.patil
 * @see PerformanceMonitorFactory#newHttpPerformanceMonitor(String, String,
 *      com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType,
 *      String)
 */
public class HttpPerformanceMonitor extends AbstractPerformanceMonitor
{
	/**
	 * Holds the performance monitors representing urls.
	 */
	private HashMap<String, PerformanceMonitor> httpPerformanceMonitors = new HashMap<String, PerformanceMonitor>();

	/**
	 * Constructs the HttpPerformanceMonitor object.
	 * 
	 * @param componentName
	 *            the name of the component to which this performance monitor
	 *            belongs
	 * @param performanceMonitorName
	 *            the name for this performance monitor
	 * @param performanceDataType
	 *            the data type of the performance data
	 * @param performanceUnit
	 *            the unit of the performance data
	 */
	private HttpPerformanceMonitor(String componentName,
			String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		super(componentName, performanceMonitorName, performanceDataType,
				performanceUnit);
	}

	/**
	 * Returns the PerformanceMonitor for the requested <tt>url</tt>. If
	 * PerformanceMonitor does not exist, new PerformanceMonitor will be created
	 * for the requested <tt>url</tt> and returns the newly created
	 * PerformanceMonitor.
	 * 
	 * @param url
	 *            the url for which PerformanceMonitor is required
	 * @return the PerformanceMonitor for the requested <tt>url</tt>
	 */
	private PerformanceMonitor getPerformanceMonitor(String url)
	{
		// HttpPerformanceMonitor name will be in 'Http Url -
		// ip:port/context/fileName' format. So extracting
		// ip:port/context/fileName from url.
		int startIndex = 7; // ignoring starting 7 characters ('http://')
		int endIndex = url.indexOf('?'); // considering up to file name
		if (endIndex == -1)
			endIndex = url.length();

		String performanceMonitorName = url.substring(startIndex, endIndex);
		performanceMonitorName = "Http Url - " + performanceMonitorName;

		PerformanceMonitor performanceMonitor = httpPerformanceMonitors
				.get(performanceMonitorName);
		if (performanceMonitor == null)
		{
			synchronized (HttpPerformanceMonitor.class)
			{
				performanceMonitor = httpPerformanceMonitors
						.get(performanceMonitorName);
				if (performanceMonitor == null)
				{
					performanceMonitor = PerformanceMonitorFactory
							.newPerformanceMonitor(componentName,
									performanceMonitorName,
									performanceDataType, performanceUnit);
					httpPerformanceMonitors.put(performanceMonitorName,
							performanceMonitor);
				}
			}
		}

		return performanceMonitor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #logPerformance(com.onmobile.apps.ringbacktones.hunterFramework.monitor.
	 * Context)
	 */
	/**
	 * @param performanceContext
	 *            the performanceConext must contain the url for which
	 *            performance data has to be logged
	 */
	@Override
	public boolean logPerformance(PerformanceContext performanceContext)
	{
		String url = performanceContext.getHttpUrl();
		if (url != null)
		{
			PerformanceMonitor performanceMonitor = getPerformanceMonitor(url);
			return performanceMonitor.logPerformance(performanceContext);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #
	 * raiseAlarm(com.onmobile.apps.ringbacktones.hunterFramework.monitor.Context
	 * )
	 */
	/**
	 * @param performanceContext
	 *            the performanceConext must contain the url for which
	 *            alarm has to be raised
	 */
	@Override
	public boolean raiseAlarm(PerformanceContext performanceContext)
	{
		String url = performanceContext.getHttpUrl();
		if (url != null)
		{
			PerformanceMonitor performanceMonitor = getPerformanceMonitor(url);
			return performanceMonitor.raiseAlarm(performanceContext);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #
	 * clearAlarm(com.onmobile.apps.ringbacktones.hunterFramework.monitor.Context
	 * )
	 */
	/**
	 * @param performanceContext
	 *            the performanceConext must contain the url for which
	 *            alarm has to be cleared
	 */
	@Override
	public boolean clearAlarm(PerformanceContext performanceContext)
	{
		String url = performanceContext.getHttpUrl();
		if (url != null)
		{
			PerformanceMonitor performanceMonitor = getPerformanceMonitor(url);
			return performanceMonitor.clearAlarm(performanceContext);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("HttpPerformanceMonitor[httpPerformanceMonitors = ");
		builder.append(httpPerformanceMonitors);
		builder.append("]");
		return builder.toString();
	}
}
