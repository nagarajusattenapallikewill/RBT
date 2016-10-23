/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.Severity;

/**
 * PerformanceContext holds the data required for any performance monitoring
 * functionality. Like for logging the performance <tt>performanceData</tt> will
 * be stored in the PerformanceContext and the same will be passed to
 * {@link PerformanceMonitor#logPerformance(PerformanceContext)}. Similarly
 * <tt>severity</tt> used for
 * {@link PerformanceMonitor#raiseAlarm(PerformanceContext)}.
 * 
 * @author vinayasimha.patil
 */
public class PerformanceContext
{
	/**
	 * Holds the performance data in any number format (Integer, Long, Float or
	 * Double).
	 */
	private Number performanceData;

	/**
	 * Represents the severity of the alarm.
	 */
	private Severity severity = null;

	/**
	 * Alarm message.
	 */
	private String alarmMessage = null;

	/**
	 * Holds the url string. This will be used in {@link HttpPerformanceMonitor}
	 * .
	 */
	private String httpUrl = null;

	/**
	 * Constructs the PerformanceContext with <tt>performanceData</tt>.
	 * 
	 * @param performanceData
	 *            the performance data to be logged
	 */
	public PerformanceContext(Number performanceData)
	{
		this.performanceData = performanceData;
	}

	/**
	 * Constructs the PerformanceContext with <tt>performanceData</tt> and
	 * <tt>httpUrl</tt>.
	 * 
	 * @param performanceData
	 *            the performance data to be logged
	 * @param httpUrl
	 *            the url to which this performance data has to be logged
	 */
	public PerformanceContext(Number performanceData, String httpUrl)
	{
		this.performanceData = performanceData;
		this.httpUrl = httpUrl;
	}

	/**
	 * Constructs the PerformanceContext with <tt>severity</tt> and
	 * <tt>alarmMessage</tt>.
	 * 
	 * @param severity
	 *            the severity level of the alarm
	 * @param alarmMessage
	 *            the alarm message
	 */
	public PerformanceContext(Severity severity, String alarmMessage)
	{
		this.severity = severity;
		this.alarmMessage = alarmMessage;
	}

	/**
	 * Constructs the PerformanceContext with <tt>severity</tt>,
	 * <tt>alarmMessage</tt> and <tt>httpUrl</tt>.
	 * 
	 * @param severity
	 *            the severity level of the alarm
	 * @param httpUrl
	 *            the url to which alarm has to be raised/cleared
	 * @param alarmMessage
	 *            the alarm message
	 */
	public PerformanceContext(Severity severity, String alarmMessage,
			String httpUrl)
	{
		this.severity = severity;
		this.alarmMessage = alarmMessage;
		this.httpUrl = httpUrl;
	}

	/**
	 * Returns the performance data.
	 * 
	 * @return the performanceData
	 */
	public Number getPerformanceData()
	{
		return performanceData;
	}

	/**
	 * Sets the performance data.
	 * 
	 * @param performanceData
	 *            the performanceData to set
	 */
	public void setPerformanceData(Number performanceData)
	{
		this.performanceData = performanceData;
	}

	/**
	 * Returns the severity.
	 * 
	 * @return the severity
	 */
	public Severity getSeverity()
	{
		return severity;
	}

	/**
	 * Sets the severity.
	 * 
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(Severity severity)
	{
		this.severity = severity;
	}

	/**
	 * Returns the alarmMessage.
	 * 
	 * @return the alarmMessage
	 */
	public String getAlarmMessage()
	{
		return alarmMessage;
	}

	/**
	 * Sets the alarmMessage.
	 * 
	 * @param alarmMessage
	 *            the alarmMessage to set
	 */
	public void setAlarmMessage(String alarmMessage)
	{
		this.alarmMessage = alarmMessage;
	}

	/**
	 * Returns the httpUrl.
	 * 
	 * @return the httpUrl
	 */
	public String getHttpUrl()
	{
		return httpUrl;
	}

	/**
	 * Sets the httpUrl.
	 * 
	 * @param httpUrl
	 *            the httpUrl to set
	 */
	public void setHttpUrl(String httpUrl)
	{
		this.httpUrl = httpUrl;
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
		builder.append("PerformanceContext[alarmMessage = ");
		builder.append(alarmMessage);
		builder.append(", httpUrl = ");
		builder.append(httpUrl);
		builder.append(", performanceData = ");
		builder.append(performanceData);
		builder.append(", severity = ");
		builder.append(severity);
		builder.append("]");
		return builder.toString();
	}
}
