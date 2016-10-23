package com.onmobile.apps.ringbacktones.hunterFramework.management.beans;

public class SNMPData {

	private String entityOID;

	private String alarmEntityOID;

	private String alarmMessageOID;

	private String componentName;

	private int severityLevel;

	private int severity;

	private String performanceContextMessage;

	private String alarmMessage;

	private String performanceMonitorName;

	private String alarmMonitorOID;

	private String httpUrl;

	public String getEntityOID() {
		return entityOID;
	}

	public void setEntityOID(String entityOID) {
		this.entityOID = entityOID;
	}

	public String getAlarmEntityOID() {
		return alarmEntityOID;
	}

	public void setAlarmEntityOID(String alarmEntityOID) {
		this.alarmEntityOID = alarmEntityOID;
	}

	public String getAlarmMessageOID() {
		return alarmMessageOID;
	}

	public void setAlarmMessageOID(String alarmMessageOID) {
		this.alarmMessageOID = alarmMessageOID;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public int getSeverityLevel() {
		return severityLevel;
	}

	public void setSeverityLevel(int severityLevel) {
		this.severityLevel = severityLevel;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getPerformanceContextMessage() {
		return performanceContextMessage;
	}

	public void setPerformanceContextMessage(String performanceContextMessage) {
		this.performanceContextMessage = performanceContextMessage;
	}

	public String getAlarmMessage() {
		return alarmMessage;
	}

	public void setAlarmMessage(String alarmMessage) {
		this.alarmMessage = alarmMessage;
	}

	public String getPerformanceMonitorName() {
		return performanceMonitorName;
	}

	public void setPerformanceMonitorName(String performanceMonitorName) {
		this.performanceMonitorName = performanceMonitorName;
	}

	public String getAlarmMonitorOID() {
		return alarmMonitorOID;
	}

	public void setAlarmMonitorOID(String alarmMonitorOID) {
		this.alarmMonitorOID = alarmMonitorOID;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	@Override
	public String toString() {
		return "SNMPData [entityOID=" + entityOID + ", alarmEntityOID="
				+ alarmEntityOID + ", alarmMessageOID=" + alarmMessageOID
				+ ", componentName=" + componentName + ", severityLevel="
				+ severityLevel + ", severity=" + severity
				+ ", performanceContextMessage=" + performanceContextMessage
				+ ", alarmMessage=" + alarmMessage
				+ ", performanceMonitorName=" + performanceMonitorName
				+ ", alarmMonitorOID=" + alarmMonitorOID + ", httpUrl="
				+ httpUrl + "]";
	}


}
