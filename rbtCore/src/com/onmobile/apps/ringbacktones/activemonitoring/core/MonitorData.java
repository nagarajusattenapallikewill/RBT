package com.onmobile.apps.ringbacktones.activemonitoring.core;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants.Severity;

public class MonitorData 
{
	private String componentName = null;
	private String monitorKey = null;
	private Severity severity = null;
	private String message = null;
	
	

	/**
	 * @param componentName
	 * @param monitorKey
	 * @param severity
	 * @param message
	 */
	public MonitorData(String componentName, String monitorKey,
			Severity severity, String message) 
	{
		if (monitorKey != null)
		{
			int index = monitorKey.indexOf('?');
			if (index > -1)
				monitorKey = monitorKey.substring(0, index);
		}
		this.componentName = componentName;
		this.monitorKey = monitorKey;
		this.severity = severity;
		this.message = message;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the monitorKey
	 */
	public String getMonitorKey() {
		return monitorKey;
	}

	/**
	 * @param monitorKey the monitorKey to set
	 */
	public void setMonitorKey(String monitorKey) 
	{
		if (monitorKey != null)
		{
			int index = monitorKey.indexOf('?');
			if (index > -1)
				monitorKey = monitorKey.substring(0, index);
		}
		this.monitorKey = monitorKey;
	}

	/**
	 * @return the severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MonitorData[componentName=");
		builder.append(componentName);
		builder.append(", message=");
		builder.append(message);
		builder.append(", monitorKey=");
		builder.append(monitorKey);
		builder.append(", severity=");
		builder.append(severity);
		builder.append("]");
		return builder.toString();
	}
	
}
