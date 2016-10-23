/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.common.PIR.AverageFloatCounter;
import com.onmobile.common.PIR.AverageIntCounter;
import com.onmobile.common.PIR.PIRInterface;
import com.onmobile.common.cjni.O3InterfaceHelper;

/**
 * {@link PerformanceMonitor} implementation with the Ozone PIR logging and SNMP
 * Alarm raising functionality (Extending it from {@link SNMPPerformanceMonitor}
 * ).
 * 
 * @author vinayasimha.patil
 * @see PerformanceMonitorFactory#newPerformanceMonitor(String, String,
 *      com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType,
 *      String)
 * @see SNMPPerformanceMonitor
 */
public class OzonePIRnSNMPPerformanceMonitor extends SNMPPerformanceMonitor
{
	private static Logger logger = Logger.getLogger(OzonePIRnSNMPPerformanceMonitor.class);
	/**
	 * Holds the reference of Ozone PIRInterface through which performance
	 * logging can be done.
	 */
	private PIRInterface pirInterface = null;

	/**
	 * Holds the reference of AverageIntCounter created by Ozone PIRInterface.
	 * If the <tt>performanceDataType</tt> is
	 * {@link PerformanceMonitor.PerformanceDataType#INTEGER} or
	 * {@link PerformanceMonitor.PerformanceDataType#LONG} then only it holds
	 * the proper reference otherwise <tt>null</tt> reference will be stored.
	 */
	private AverageIntCounter averageIntCounter = null;

	/**
	 * Holds the reference of AverageFloatCounter created by Ozone PIRInterface.
	 * If the <tt>performanceDataType</tt> is
	 * {@link PerformanceMonitor.PerformanceDataType#FLOAT} or
	 * {@link PerformanceMonitor.PerformanceDataType#DOUBLE} then only it holds
	 * the proper reference otherwise <tt>null</tt> reference will be stored.
	 */
	private AverageFloatCounter averageFloatCounter = null;

	/**
	 * Constructs the OzonePIRnSNMPPerformanceMonitor object.
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
	private OzonePIRnSNMPPerformanceMonitor(String componentName,
			String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		super(componentName, performanceMonitorName, performanceDataType,
				performanceUnit);
		init();
	}

	/**
	 * Creates the Performance Counters.
	 */
	private void init()
	{
		try
		{
			String className = null;
			try
			{
				ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
				className = resourceBundle.getString("RBT_OZONE_COMPONENT");
			}
			catch (Exception e)
			{
				logger.error("", e);
			}

			if (className == null)
				className = "com.onmobile.apps.ringbacktones.Gatherer.CopyBootstrapOzonized";

			Class<?> classObj = Class.forName(className);
			Method method = classObj.getDeclaredMethod("getO3InterfaceHelper");
			O3InterfaceHelper o3InterfaceHelper = (O3InterfaceHelper) method
					.invoke(null);
			PIRInterface pirInterface = o3InterfaceHelper
					.getPIRInterfaceHelper();

			setPirInterface(pirInterface);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	/**
	 * Returns the pirInterface.
	 * 
	 * @return the pirInterface
	 */
	public PIRInterface getPirInterface()
	{
		return pirInterface;
	}

	/**
	 * Sets the pirInterface. And creates the counters based
	 * <tt>performanceDataType</tt>.
	 * 
	 * @param pirInterface
	 *            the pirInterface to set
	 */
	public void setPirInterface(PIRInterface pirInterface)
	{
		this.pirInterface = pirInterface;

		if (pirInterface == null)
			return;

		switch (getPerformanceDataType())
		{
			case INTEGER:
			case LONG:
				averageIntCounter = pirInterface.createAverageIntCounter(
						performanceMonitorName, performanceUnit, componentName);
				break;
			case FLOAT:
			case DOUBLE:
				averageFloatCounter = pirInterface.createAverageFloatCounter(
						performanceMonitorName, performanceUnit, componentName);
				break;
			default:
				break;
		}

	}

	/**
	 * Logs the performance data by using Ozone PIR.
	 */
	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.hunterFramework.management.
	 * SNMPPerformanceMonitor
	 * #logPerformance(com.onmobile.apps.ringbacktones.hunterFramework
	 * .management.PerformanceContext)
	 */
	@Override
	public boolean logPerformance(PerformanceContext performanceContext)
	{
		if (pirInterface == null)
			return false;

		try
		{
			switch (getPerformanceDataType())
			{
				case INTEGER:
					averageIntCounter.increase((Integer) performanceContext
							.getPerformanceData());
					break;
				case LONG:
					averageIntCounter.increase((Long) performanceContext
							.getPerformanceData());
					break;
				case FLOAT:
					averageFloatCounter.increase((Float) performanceContext
							.getPerformanceData());
					break;
				case DOUBLE:
					averageFloatCounter.increase((Double) performanceContext
							.getPerformanceData());
					break;
				default:
					break;
			}

			logger.info("RBT:: Logged performance for " + componentName
					+ ": " + performanceMonitorName + ". performanceContext: "
					+ performanceContext);

			return true;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return false;
	}
}
