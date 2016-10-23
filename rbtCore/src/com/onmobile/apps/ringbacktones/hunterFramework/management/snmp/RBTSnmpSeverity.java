package com.onmobile.apps.ringbacktones.hunterFramework.management.snmp;

import org.apache.log4j.Logger;


public enum RBTSnmpSeverity {
	
	CRITICAL(5), ERROR(4), WARN(3), INFO(2), DEBUG(1), CLEAR(0), UNKNOWN(-1);
	
	private static Logger logger = Logger
			.getLogger(RBTSnmpSeverity.class);

	private int severity;

	private RBTSnmpSeverity(int severityLevel) {
		this.severity = severityLevel;
	}

	public static int getSeverity(String severity) {
		for (RBTSnmpSeverity rbtSnmpSeverity : RBTSnmpSeverity.values()) {
			if (rbtSnmpSeverity.toString().equals(severity)) {
				logger.info("Retruning: " + rbtSnmpSeverity.severity
						+ ", severity: " + severity);
				return rbtSnmpSeverity.severity;
			}
		}
		logger.info("Retruning -1 for UNKNOWN severity: " + severity);
		return RBTSnmpSeverity.UNKNOWN.getSeverity();
	}
	
	private int getSeverity() {
		return severity;
	}
	
}
