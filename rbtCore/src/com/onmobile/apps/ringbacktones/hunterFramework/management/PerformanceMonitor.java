/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

/**
 * Interface to represent the functionalities required in performance monitoring
 * of the application.
 * 
 * @author vinayasimha.patil
 * @see AbstractPerformanceMonitor
 * @see HttpPerformanceMonitor
 * @see OzonePIRnSNMPPerformanceMonitor
 */
public interface PerformanceMonitor
{
	/**
	 * Returns the name of the Component to which this performance monitor
	 * belongs.
	 * 
	 * @return the name of the Component to which this performance monitor
	 *         belongs
	 */
	public String getComponentName();

	/**
	 * Returns the name of this performance monitor.
	 * 
	 * @return the name of this performance monitor
	 */
	public String getPerformanceMonitorName();

	/**
	 * Returns the data type of the performance data.
	 * 
	 * @return the data type of the performance data
	 */
	public PerformanceDataType getPerformanceDataType();

	/**
	 * Returns the unit of the performance data.
	 * 
	 * @return the unit of the performance data
	 */
	public String getPerformanceUnit();

	/**
	 * Logs the performance data.
	 * 
	 * @param performanceContext
	 *            the {@link PerformanceContext} containing the performance data
	 * @return <tt>true</tt> if successfully logged the performance data,
	 *         otherwise <tt>false</tt>
	 */
	public boolean logPerformance(PerformanceContext performanceContext);

	/**
	 * Raises the alarm with the severity and message mentioned in the
	 * <tt>performanceContext</tt>.
	 * 
	 * @param performanceContext
	 *            the {@link PerformanceContext} containing the alarm
	 *            information
	 * @return <tt>true</tt> if successfully raised the alarm, otherwise
	 *         <tt>false</tt>
	 */
	public boolean raiseAlarm(PerformanceContext performanceContext);

	/**
	 * Clears the alarm already raised by this performance monitor.
	 * 
	 * @param performanceContext
	 *            the {@link PerformanceContext} containing the alarm
	 *            information
	 * @return <tt>true</tt> if successfully cleared the alarm, otherwise
	 *         <tt>false</tt>
	 */
	public boolean clearAlarm(PerformanceContext performanceContext);

	/**
	 * A performance data type. A performance data can be one of the
	 * following data types:
	 * <ul>
	 * <li>{@link #INTEGER}<br>
	 * Represents the integer performance data type.</li>
	 * <li>{@link #LONG}<br>
	 * Represents the long performance data type.</li>
	 * <li>{@link #FLOAT}<br>
	 * Represents the float performance data type.</li>
	 * <li>{@link #DOUBLE}<br>
	 * Represents the double performance data type.</li>
	 * </ul>
	 * 
	 * @author vinayasimha.patil
	 */
	public enum PerformanceDataType
	{
		/**
		 * Performance data type to represent integer value.
		 */
		INTEGER,

		/**
		 * Performance data type to represent long value.
		 */
		LONG,

		/**
		 * Performance data type to represent float value.
		 */
		FLOAT,

		/**
		 * Performance data type to represent double value.
		 */
		DOUBLE
	}

	/**
	 * An alarm severity level. An alarm can be in one of the
	 * following level:
	 * <ul>
	 * <li>{@link #CRITICAL}<br>
	 * Represents the critical level of an alarm.</li>
	 * <li>{@link #ERROR}<br>
	 * Represents the error level of an alarm.</li>
	 * <li>{@link #WARNING}<br>
	 * Represents the warning level of an alarm.</li>
	 * <li>{@link #INFO}<br>
	 * Represents the info level of an alarm.</li>
	 * <li>{@link #CLEAR}<br>
	 * Represents the clear alarm request.</li>
	 * </ul>
	 * 
	 * @author vinayasimha.patil
	 */
	public enum Severity
	{
		/**
		 * Used to represent the critical problems in the application, which
		 * needs immediate attention.
		 */
		CRITICAL,

		/**
		 * Used to represent the application behaving abnormally.
		 */
		ERROR,

		/**
		 * Used to represent possibility of the application may behave
		 * abnormally.
		 */
		WARNING,

		/**
		 * Used to represents the status of the application.
		 */
		INFO,

		/**
		 * Used to clear the already raised alarms.
		 */
		CLEAR
	}
}
