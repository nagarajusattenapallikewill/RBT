/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;

/**
 * Factory class to create the PerformanceMaonitor objects. It creates the
 * object of {@link OzonePIRnSNMPPerformanceMonitor} and
 * {@link HttpPerformanceMonitor}.
 * 
 * @author vinayasimha.patil
 */
public class PerformanceMonitorFactory
{
	private static Logger logger = Logger.getLogger(PerformanceMonitorFactory.class);
	/**
	 * Creates the {@link PerformanceMonitor} implementation object.
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
	 * @return the {@link PerformanceMonitor} implementation object
	 */
	public static PerformanceMonitor newPerformanceMonitor(
			String componentName, String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		Parameters parameter = parametersCacheManager
				.getParameter(
						iRBTConstant.COMMON,
						"performanceMonitor.implementation",
						"com.onmobile.apps.ringbacktones.hunterFramework.management.SNMPPerformanceMonitor");

		String className = parameter.getValue();
		PerformanceMonitor performanceMonitor = createPerformanceMonitor(
				className, componentName, performanceMonitorName,
				performanceDataType, performanceUnit);

		return performanceMonitor;
	}

	/**
	 * Creates the {@link HttpPerformanceMonitor} object.
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
	 * @return the {@link HttpPerformanceMonitor} object
	 */
	public static HttpPerformanceMonitor newHttpPerformanceMonitor(
			String componentName, String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		HttpPerformanceMonitor httpPerformanceMonitor = null;
		try
		{
			// As of now only HttpPerformanceMonitor implementation is
			// coded that's why className is hard coded. It can be made as
			// configurable.
			String className = "com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor";
			PerformanceMonitor performanceMonitor = createPerformanceMonitor(
					className, componentName, performanceMonitorName,
					performanceDataType, performanceUnit);

			httpPerformanceMonitor = (HttpPerformanceMonitor) performanceMonitor;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return httpPerformanceMonitor;
	}

	/**
	 * Creates the {@link PerformanceMonitor} implementation object for class
	 * mentioned in the <tt>className</tt> parameter.
	 * 
	 * @param className
	 *            the class name of the PerformanceMonitor implementation class
	 * @param componentName
	 *            the name of the component to which this performance monitor
	 *            belongs
	 * @param performanceMonitorName
	 *            the name for this performance monitor
	 * @param performanceDataType
	 *            the data type of the performance data
	 * @param performanceUnit
	 *            the unit of the performance data
	 * @return the {@link PerformanceMonitor} implementation object
	 */
	private static PerformanceMonitor createPerformanceMonitor(
			String className, String componentName,
			String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		PerformanceMonitor performanceMonitor = null;
		try
		{
			@SuppressWarnings("unchecked")
			Class<PerformanceMonitor> performanceMonitorClass = (Class<PerformanceMonitor>) Class
					.forName(className);

			Constructor<PerformanceMonitor> constructor = performanceMonitorClass
					.getDeclaredConstructor(String.class, String.class,
							PerformanceDataType.class, String.class);
			constructor.setAccessible(true);

			performanceMonitor = constructor.newInstance(componentName,
					performanceMonitorName, performanceDataType,
					performanceUnit);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return performanceMonitor;
	}
}
