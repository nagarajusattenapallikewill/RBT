package com.onmobile.apps.ringbacktones.hunterFramework.management.snmp;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceContext;
import com.onmobile.apps.ringbacktones.hunterFramework.management.SNMPPerformanceMonitor;
import com.onmobile.snmp.agentx.client.NotificationVariable;
import com.onmobile.snmp.agentx.client.util.OMNotification;

public class RBTSnmpManager {

	private static Logger logger = Logger
			.getLogger(SNMPPerformanceMonitor.class);

	private Snmp snmp = null;

	private static RBTSnmpManager rbtSnmpManager = null;
	
	private static ResourceBundle resourceBundle = null;

	private static String trapReceiverAddress = null;
	
	private static String trapReceiverPort = null;
	
	private static long startTime;
	
	private RBTSnmpManager() {

		logger.info("Initializing RBTSnmpManager.");
		try {
			TransportMapping transportMapping = new DefaultUdpTransportMapping();
			transportMapping.listen();
			snmp = new Snmp(transportMapping);
		} catch (IOException ioe) {
			logger.error("IOException during snmp initialization: " + ioe.getMessage(),
					ioe);
		}

		try {
			resourceBundle = ResourceBundle.getBundle("snmp");
			trapReceiverAddress = resourceBundle.getString("trapReceiverAddress");
			trapReceiverPort = resourceBundle.getString("trapReceiverPort");
		} catch (Exception e) {
			logger.error("Failed to load snmp bundle. Exception: " + e.getMessage(),
					e);
		}

		startTime = System.currentTimeMillis();
		
		logger.info("Successfully initialized RBTSnmpManager.");
	}

	public static RBTSnmpManager getInstance() {
		if (null == rbtSnmpManager) {
			synchronized (RBTSnmpManager.class) {
				if (null == rbtSnmpManager) {
					rbtSnmpManager = new RBTSnmpManager();
				}
			}
		}
		return rbtSnmpManager;
	}

	public void sendTrap(PerformanceContext performanceContext,
			OMNotification omNotification) {
		int severity = RBTSnmpSeverity.getSeverity(performanceContext
				.getSeverity().name());
		
		logger.info("Sending trap. performanceContext: " + performanceContext
				+ ", omNotification: "
				+ omNotification.getNotificationOID().getOID()
				+ "Sending trap for severity: " + severity);
		
		CommunityTarget communityTarget = new CommunityTarget();
		
		communityTarget.setAddress(new UdpAddress(trapReceiverAddress + "/" + trapReceiverPort));
		communityTarget.setCommunity(new OctetString("public"));
		communityTarget.setRetries(2);
		communityTarget.setTimeout(1500);
		communityTarget.setVersion(SnmpConstants.version2c);

		String oid = omNotification.getNotificationOID().getOID();
		
		PDU pdu = new PDU();
		long currTime = System.currentTimeMillis();
		
		long diff = currTime - startTime;
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(diff)));
		
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		
		NotificationVariable[] checkedVariables = omNotification
				.getCheckedVariables();
		
		NotificationVariable[] uncheckedVariables = omNotification
				.getUncheckedVariables();

		if (null != checkedVariables) {
			for(NotificationVariable nv : checkedVariables) {
				OID oid1 = new OID(nv.getVariableOID().getOID());
				OctetString value = new OctetString(String.valueOf(nv.getVariableValue()));
				pdu.add(new VariableBinding(oid1, value));
			}
		}
		
		if (null != uncheckedVariables) {
			for(NotificationVariable nv : uncheckedVariables) {
				OID oid1 = new OID(nv.getVariableOID().getOID());
				OctetString value = new OctetString(String.valueOf(nv.getVariableValue()));
				pdu.add(new VariableBinding(oid1, value));
			}
		}

		OID severityOID = new OID(".1.3.6.1.4.1.10377.100.1.1.");
		OctetString severityStr = new OctetString(String.valueOf(severity));
		pdu.add(new VariableBinding(severityOID, severityStr));
		
		pdu.setType(PDU.NOTIFICATION);
		
		// Sending PDU
		ResponseEvent responseEvent = null;
		try {
			logger.info("Sending PDU. requestId: " + pdu.getRequestID()
					+ ", VariableBindings: " + pdu.getVariableBindings());
			
			responseEvent = snmp.send(pdu, communityTarget);
			
			logger.info("Successfully sent PDU. responseEvent: " + responseEvent);
		} catch (IOException ioe) {
			logger.error(
					"Unable to send PDU. IOException: " + ioe.getMessage(), ioe);
		}
	}
	
}
