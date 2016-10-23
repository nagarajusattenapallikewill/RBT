package com.onmobile.apps.ringbacktones.hunterFramework.management.beans;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceContext;
import com.onmobile.apps.ringbacktones.hunterFramework.management.SNMPPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.Severity;
import com.onmobile.snmp.agentx.client.NotificationVariable;
import com.onmobile.snmp.agentx.client.OID;
import com.onmobile.snmp.agentx.client.SeverityLevel;
import com.onmobile.snmp.agentx.client.util.OMNotification;

public class SNMPAdaptor {

	private static Logger logger = Logger.getLogger(SNMPAdaptor.class);

	public static SNMPData createSNMPData(
			SNMPPerformanceMonitor snmpPerformanceMonitor) {

		logger.debug("Converting SNMPData. snmpPerformanceMonitor: "
				+ snmpPerformanceMonitor);
		SNMPData snmpData = new SNMPData();

		snmpData.setComponentName(snmpPerformanceMonitor.getComponentName());

		OMNotification omNotification = snmpPerformanceMonitor
				.getLastOMNotification();
		OID oidObj = omNotification.getNotificationOID();
		String oid = oidObj.getOID();
		snmpData.setEntityOID(oid);

		SeverityLevel severityLevel = omNotification.getSeverityLevel();

		snmpData.setSeverityLevel(severityLevel.getSeverityLevel());

		String nofiticationMessage = omNotification.getNotificationMessage();

		snmpData.setAlarmMessage(nofiticationMessage);

		NotificationVariable[] checkedVariables = omNotification
				.getCheckedVariables();

		if (null != checkedVariables && checkedVariables.length > 0) {
			String alarmMonitorOID = checkedVariables[0].getVariableOID()
					.getOID();
			String performanceMonitorName = checkedVariables[0]
					.getVariableValue().toString();

			snmpData.setAlarmMonitorOID(alarmMonitorOID);
			snmpData.setPerformanceMonitorName(performanceMonitorName);

			String alarmEntityOID = checkedVariables[1].getVariableOID()
					.getOID();
			String componentName = String.valueOf(checkedVariables[1]
					.getVariableValue());

			snmpData.setAlarmEntityOID(alarmEntityOID);
			snmpData.setComponentName(componentName);
		}

		NotificationVariable[] unCheckedVariables = omNotification
				.getUncheckedVariables();

		if (null != unCheckedVariables && unCheckedVariables.length > -1) {

			String alarmMessageOID = unCheckedVariables[0].getVariableOID()
					.getOID();

			snmpData.setAlarmMessageOID(alarmMessageOID);
		}

		PerformanceContext performanceContext = snmpPerformanceMonitor
				.getLastPerformanceContext();

		Severity severity = performanceContext.getSeverity();

		snmpData.setSeverity(severity.ordinal());

		String alarmMesssge = performanceContext.getAlarmMessage();

		snmpData.setPerformanceContextMessage(alarmMesssge);

		String httpUrl = performanceContext.getHttpUrl();

		snmpData.setHttpUrl(httpUrl);

		logger.debug("Converted snmpPerformanceMonitor: "
				+ snmpPerformanceMonitor + " to " + snmpData);

		return snmpData;
	}

	public static OMNotification createOMNotification(SNMPData snmpData) {
		OMNotification omNotification = null;
		logger.debug("Converting snmpData to OMNotification. snmpData: "+snmpData);
		try {

			String alarmMonitorOID = snmpData.getAlarmMonitorOID();
			String performanceMonitorName = snmpData
					.getPerformanceMonitorName();
			String alarmEntityOID = snmpData.getAlarmEntityOID();
			String componentName = snmpData.getComponentName();
			String alarmMessageOID = snmpData.getAlarmMessageOID();
			String alarmMessage = snmpData.getAlarmMessage();
			String entityOIDStr = snmpData.getEntityOID();
			int severityLevelValue = snmpData.getSeverityLevel();

			SeverityLevel severityLevel = SeverityLevel.CRITICAL;

			if (severityLevelValue == SeverityLevel.CLEAR_ALARM
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.CLEAR_ALARM;
			} else if (severityLevelValue == SeverityLevel.CRITICAL
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.CRITICAL;
			} else if (severityLevelValue == SeverityLevel.DEBUG
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.DEBUG;
			} else if (severityLevelValue == SeverityLevel.ERROR
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.ERROR;
			} else if (severityLevelValue == SeverityLevel.INFO
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.INFO;
			} else if (severityLevelValue == SeverityLevel.WARNING
					.getSeverityLevel()) {
				severityLevel = SeverityLevel.WARNING;
			}

			NotificationVariable[] checkedVariables = new NotificationVariable[2];
			checkedVariables[0] = new NotificationVariable(new OID(
					alarmMonitorOID), performanceMonitorName);
			checkedVariables[1] = new NotificationVariable(new OID(
					alarmEntityOID), componentName);

			NotificationVariable[] unCheckedVariables = new NotificationVariable[1];
			unCheckedVariables[0] = new NotificationVariable(new OID(
					alarmMessageOID), alarmMessage);
			OID entityOID = new OID(entityOIDStr);

			omNotification = new OMNotification(entityOID, severityLevel,
					alarmMessage, checkedVariables, unCheckedVariables);
		} catch (Exception e) {
			logger.error("Failed to convert snmpData to omNotification. Exception: "+e.getMessage(), e);
		}
		
		logger.debug("Converted snmpData to omNotification. snmpData"
				+ snmpData + ", omNotification: "+omNotification);

		return omNotification;
	}

	public static PerformanceContext createPerformanceContext(SNMPData snmpData) {
		logger.debug("Converting snmpData to performanceContext. snmpData: "
				+ snmpData);
		PerformanceContext performanceContext = null;
		try {

			int severityValue = snmpData.getSeverity();
			String reason = snmpData.getPerformanceContextMessage();
			String httpUrl = snmpData.getHttpUrl();

			Severity severity = Severity.CRITICAL;

			for (Severity s : Severity.values()) {
				if (s.ordinal() == severityValue) {
					severity = s;
				}
			}
			
			performanceContext = new PerformanceContext(severity, reason,
					httpUrl);
		} catch (Exception e) {
			logger.error(
					"Failed to converted snmpData to performanceContext. Exception: "
							+ e.getMessage(), e);
		}

		logger.debug("Converted snmpData to performanceContext. snmpData: "
				+ snmpData + ", performanceContext: " + performanceContext);
		return performanceContext;

	}
	
}
