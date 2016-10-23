/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

/**
 * Abstract PerformanceMonitor class. Implemented with the functionality of
 * getting the information of the PerformanceMonitor. Logging performance and
 * Alarming functionality has to be implemented in the specific
 * PerformanceMonitor implementation classes and those classes has to extend
 * this abstract class.
 * 
 * @author vinayasimha.patil
 * @see HttpPerformanceMonitor
 * @see OzonePIRnSNMPPerformanceMonitor
 */
public abstract class AbstractPerformanceMonitor implements PerformanceMonitor
{
	protected String componentName = null;
	protected String performanceMonitorName = null;
	protected PerformanceDataType performanceDataType = null;
	protected String performanceUnit = null;

	/**
	 * Constructs the PerformanceMonitor object.
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
	protected AbstractPerformanceMonitor(String componentName,
			String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		this.componentName = componentName;
		this.performanceMonitorName = performanceMonitorName;
		this.performanceDataType = performanceDataType;
		this.performanceUnit = performanceUnit;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName()
	{
		return componentName;
	}

	/**
	 * Sets the componentName.
	 * 
	 * @param componentName
	 *            the componentName to set
	 */
	public void setComponentName(String componentName)
	{
		this.componentName = componentName;
	}

	/**
	 * @return the performanceMonitorName
	 */
	public String getPerformanceMonitorName()
	{
		return performanceMonitorName;
	}

	/**
	 * Sets the performanceMonitorName.
	 * 
	 * @param performanceMonitorName
	 *            the performanceMonitorName to set
	 */
	public void setPerformanceMonitorName(String performanceMonitorName)
	{
		this.performanceMonitorName = performanceMonitorName;
	}

	/**
	 * @return the performanceDataType
	 */
	public PerformanceDataType getPerformanceDataType()
	{
		return performanceDataType;
	}

	/**
	 * Sets the performanceDataType.
	 * 
	 * @param performanceDataType
	 *            the performanceDataType to set
	 */
	public void setPerformanceDataType(PerformanceDataType performanceDataType)
	{
		this.performanceDataType = performanceDataType;
	}

	/**
	 * @return the performanceUnit
	 */
	public String getPerformanceUnit()
	{
		return performanceUnit;
	}

	/**
	 * Sets the performanceUnit.
	 * 
	 * @param performanceUnit
	 *            the performanceUnit to set
	 */
	public void setPerformanceUnit(String performanceUnit)
	{
		this.performanceUnit = performanceUnit;
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
		builder.append("AbstractPerformanceMonitor[componentName = ");
		builder.append(componentName);
		builder.append(", performanceDataType = ");
		builder.append(performanceDataType);
		builder.append(", performanceMonitorName = ");
		builder.append(performanceMonitorName);
		builder.append(", performanceUnit = ");
		builder.append(performanceUnit);
		builder.append("]");
		return builder.toString();
	}
}
