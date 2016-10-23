/**
 * 
 */
package com.onmobile.apps.ringbacktones.hunterFramework.management;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTJsonUtils;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.hunterFramework.management.beans.SNMPAdaptor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.beans.SNMPData;
import com.onmobile.apps.ringbacktones.hunterFramework.management.snmp.RBTSnmpManager;
import com.onmobile.snmp.agentx.client.NotificationVariable;
import com.onmobile.snmp.agentx.client.OID;
import com.onmobile.snmp.agentx.client.SeverityLevel;
import com.onmobile.snmp.agentx.client.Subagent;
import com.onmobile.snmp.agentx.client.SubagentFactory;
import com.onmobile.snmp.agentx.client.util.DuplicateNotificationManager;
import com.onmobile.snmp.agentx.client.util.OMNotification;

/**
 * {@link PerformanceMonitor} implementation with only SNMP Alarm raising
 * functionality. This Implementation does not support the performance logging.
 * 
 * @author vinayasimha.patil
 * @see PerformanceMonitorFactory#newPerformanceMonitor(String, String,
 *      com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType,
 *      String)
 * @see OzonePIRnSNMPPerformanceMonitor
 */
public class SNMPPerformanceMonitor extends AbstractPerformanceMonitor
{
	private static Logger logger = Logger
			.getLogger(SNMPPerformanceMonitor.class);
	/**
	 * Holds the reference to snmp.properties resource bundle.
	 */
	private static ResourceBundle resourceBundle = null;

	/**
	 * Default entity OID, used if the alarming entity OID is not defined.
	 */
	private String defaultEnterpriseOID = "1.3.6.1.4.1.10377.7.9999.1.9999";

	/**
	 * Holds the OID value for alarming entity.
	 */
	private static String alarmEntityOID = "1.3.6.1.4.1.10377.5.2.5.0";

	/**
	 * Holds the OID value for alarm message.
	 */
	private static String alarmMessageOID = "1.3.6.1.4.1.10377.100.1.3.0";

	/**
	 * Holds the OID value for alarming monitor.
	 */
	private static String alarmMonitorOID = "1.3.6.1.4.1.10377.100.0.6.0";

	/**
	 * Holds the reference to the OMNotification if alarm raised by this
	 * PerformanceMonitor. Once the alarm is cleared this will be assigned
	 * <tt>null</tt>.
	 */
	private OMNotification lastOMNotification = null;

	/**
	 * Holds the reference to the PerformanceContext if alarm raised by this
	 * PerformanceMonitor. Once the alarm is cleared this will be assigned
	 * <tt>null</tt>.
	 */
	private PerformanceContext lastPerformanceContext = null;

	/**
	 * Holds the reference of SNMP notification manager.
	 */
//	private static DuplicateNotificationManager duplicateNotificationManager = null;
	
	private static RBTSnmpManager rbtSnmpManager = null;
	
	private static Object object = new Object();
	
	
	/**
	 * 
	 */
	private static String masterAgentIp = "tcp://localhost/162";

	static
	{
		try
		{
			resourceBundle = ResourceBundle.getBundle("snmp");

			alarmEntityOID = resourceBundle.getString("alarm.entity.OID");
			alarmMessageOID = resourceBundle.getString("alarm.message.OID");
			alarmMonitorOID = resourceBundle.getString("alarm.monitor.OID");
			
			masterAgentIp = resourceBundle.getString("masteragent.ip");

			init();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	/**
	 * Creates SNMP notification manager.
	 */
	private static void init()
	{
		try
		{
			boolean isFcapsEnabled = RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.COMMON, "IS_FCAPS_ENABLED", "FALSE");
			if (!isFcapsEnabled)
			{
				logger.info("FCAPS not enabled, not initializing subagent");
				return;
			}

			System.setProperty("master_snmp_agent_address", masterAgentIp);
			
			Subagent subagent = null;
			if(RBTDaemonManager.managedObjectsList != null && RBTDaemonManager.managedObjectsList.size() > 0)
				subagent = SubagentFactory.createSubagent(RBTDaemonManager.managedObjectsList);
			else
				subagent = SubagentFactory.createSubagent(null);
//			duplicateNotificationManager = new DuplicateNotificationManager(
//					subagent, 300);
			
			logger.info("Initializing SNMPPerformanceMonitor");
			
			rbtSnmpManager = RBTSnmpManager.getInstance();
			
			logger.info("Successfully initialized SNMPPerformanceMonitor");
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

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
	protected SNMPPerformanceMonitor(String componentName,
			String performanceMonitorName,
			PerformanceDataType performanceDataType, String performanceUnit)
	{
		super(componentName, performanceMonitorName, performanceDataType,
				performanceUnit);
	}

	/**
	 * This implementation does not support performance logging.
	 * 
	 * @see OzonePIRnSNMPPerformanceMonitor
	 */
	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #logPerformance(com.onmobile.apps.ringbacktones.hunterFramework.monitor.
	 * Context)
	 */
	@Override
	public boolean logPerformance(PerformanceContext performanceContext)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #
	 * raiseAlarm(com.onmobile.apps.ringbacktones.hunterFramework.monitor.Context
	 * )
	 */
	@Override
	public boolean raiseAlarm(PerformanceContext performanceContext)
	{
//		if (duplicateNotificationManager == null)
//			return false;
		
		if(null == rbtSnmpManager) {
			logger.warn("Unable to send traps. RBTSnmpManager is not initialized.");
		}

		try
		{
			Severity severity = performanceContext.getSeverity();
			String alarmMessage = performanceContext.getAlarmMessage();
			SeverityLevel severityLevel = getSeverityLevel(severity);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("alarm.").append(componentName);
			stringBuilder.append(".").append(severity);
			stringBuilder.append(".OID");
			String entityOIDStr = resourceBundle.getString(stringBuilder
					.toString());

			if (entityOIDStr == null)
				entityOIDStr = defaultEnterpriseOID;

			OID entityOID = new OID(entityOIDStr);

			if (lastOMNotification == null) {

				logger.info("Loading notificatins from database."
						+ " componentName: " + componentName + ", entityOID: "
						+ entityOID.getOID());

				// Get the last loaded OID from database to send notification.
				String jsonString = RBTParametersUtils.getParamAsString("SNMP",
						entityOID.getOID(), null);
				
				if(null != jsonString) {
					
					SNMPData snmpData = (SNMPData) RBTJsonUtils
							.convertFromJson(jsonString, SNMPData.class);
					
					OMNotification lstOmNotification = SNMPAdaptor.createOMNotification(snmpData);
					PerformanceContext pContext = SNMPAdaptor.createPerformanceContext(snmpData);
					
					logger.info("Successfully loaded SNMPPerformanceMonitor"
							+ " from database. lastOmNotification OID: "
							+ lstOmNotification.getNotificationOID().getOID()
							+ ", performanceContext alarm message: "
							+ pContext.getAlarmMessage() + ", HttpUrl: "
							+ pContext.getHttpUrl());
					
					lastOMNotification = lstOmNotification;
					lastPerformanceContext = pContext;
				} else {
					logger.warn("No notification found from database."
							+ " componentName: " + componentName + ", entityOID: "
							+ entityOID.getOID());
				}

			}
			
			if (lastOMNotification != null)
			{
				
				String lUrl = trimUrl(lastPerformanceContext.getHttpUrl());
				String pUrl = trimUrl(performanceContext.getHttpUrl());
				boolean isSameUrl = lUrl.equals(pUrl);
				boolean isSameSeverity = (lastPerformanceContext.getSeverity() == performanceContext
						.getSeverity()) ? true : false;

				if (logger.isDebugEnabled()) {
					logger.debug("Checking Urls. isSameUrl: " + isSameUrl
							+ ", isSameSeverity: " + isSameSeverity);
				}
				
				if (isSameSeverity && isSameUrl)
				{
					if (logger.isDebugEnabled())
						logger.debug("RBT:: Alarm already raised with the same severity for "
									+ componentName + ": "
									+ performanceMonitorName
									+ ". performanceContext: "
									+ performanceContext);
					return true;
				}

				PerformanceContext tempPerformanceContext = new PerformanceContext(
						Severity.CLEAR, "Clearing to raise a new alarm",
						performanceContext.getHttpUrl());

				clearAlarm(tempPerformanceContext);
			}

			
			NotificationVariable[] checkedVariables = new NotificationVariable[2];
			checkedVariables[0] = new NotificationVariable(new OID(
					alarmMonitorOID), performanceMonitorName);
			checkedVariables[1] = new NotificationVariable(new OID(
					alarmEntityOID), componentName);

			NotificationVariable[] unCheckedVariables = new NotificationVariable[1];
			unCheckedVariables[0] = new NotificationVariable(new OID(
					alarmMessageOID), alarmMessage);
			
			OMNotification omNotification = new OMNotification(entityOID,
					severityLevel, alarmMessage, checkedVariables,
					unCheckedVariables);

//			duplicateNotificationManager.sendNotification(omNotification);
			
			rbtSnmpManager.sendTrap(performanceContext, omNotification);
			
			// holding the notification reference, so that same can be used
			// while clearing this alarm.
			lastOMNotification = omNotification;
			lastPerformanceContext = performanceContext;

			// To persist alarm into database.
			String tempString = RBTParametersUtils.getParamAsString("SNMP",	entityOID.getOID(), null); 
			if(tempString == null) {
				synchronized (object) {
					tempString = RBTParametersUtils.getParamAsString("SNMP",	entityOID.getOID(), null);
					if(tempString == null) {
						rbtSnmpManager.sendTrap(performanceContext, omNotification);
						SNMPData snmpData = SNMPAdaptor.createSNMPData(this);
						String jsonString = RBTJsonUtils.convertToJson(snmpData);
						RBTParametersUtils.addParameter("SNMP", entityOID.getOID(),
								jsonString, "");
					}
				}
			}

			if (logger.isInfoEnabled())
				logger.info("omNotification : " + omNotification);
			
			if (logger.isDebugEnabled()) {
				logger.debug("RBT:: Raised Alarm for " + componentName + ": "
						+ performanceMonitorName + ". performanceContext: "
						+ performanceContext);
			}
			
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to raise alarm. performanceContext: " 
						+ performanceContext + ", exception: " + e.getMessage(), e);
		}

		return false;
	}

	private String trimUrl(String httpUrl) {
		int endIndex = httpUrl.indexOf("?");
		if (endIndex > 0) {
			httpUrl = httpUrl.substring(0, endIndex);
		}
		return httpUrl;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.monitor.PerformanceMonitor
	 * #
	 * clearAlarm(com.onmobile.apps.ringbacktones.hunterFramework.monitor.Context
	 * )
	 */
	@Override
	public boolean clearAlarm(PerformanceContext performanceContext)
	{
		if (lastOMNotification == null)
			return true;

		try
		{
			if(RBTParametersUtils.getParamAsString("SNMP", lastOMNotification.getNotificationOID().getOID(), null) == null) {
				return true;
			}
//			String reasonForClearing = performanceContext.getAlarmMessage();
//			duplicateNotificationManager.clearNotification(lastOMNotification,
//					reasonForClearing);
			
			synchronized (object) {
				
				rbtSnmpManager.sendTrap(performanceContext, lastOMNotification);
				// To delete the persisted alarm from database.
				RBTParametersUtils.deleteParameter("SNMP", lastOMNotification.getNotificationOID().getOID());
			}


			lastOMNotification = null;
			lastPerformanceContext = null;

			logger.info("RBT:: Cleared Alarm " + componentName + ": "
							+ performanceMonitorName + ". performanceContext: "
							+ performanceContext);

			return true;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return false;
	}

	/**
	 * Converts the severity data to represent the third party data format.
	 * 
	 * @param severity
	 *            of the alarm
	 * @return the severity level in third party data format
	 */
	private SeverityLevel getSeverityLevel(Severity severity)
	{
		if (severity == Severity.CRITICAL)
			return SeverityLevel.CRITICAL;
		else if (severity == Severity.ERROR)
			return SeverityLevel.ERROR;
		else if (severity == Severity.WARNING)
			return SeverityLevel.WARNING;
		else if (severity == Severity.INFO)
			return SeverityLevel.INFO;
		else
			return SeverityLevel.CLEAR_ALARM;
	}

	public String getDefaultEnterpriseOID() {
		return defaultEnterpriseOID;
	}

	public void setDefaultEnterpriseOID(String defaultEnterpriseOID) {
		this.defaultEnterpriseOID = defaultEnterpriseOID;
	}

	public OMNotification getLastOMNotification() {
		return lastOMNotification;
	}

	public void setLastOMNotification(OMNotification lastOMNotification) {
		this.lastOMNotification = lastOMNotification;
	}

	public PerformanceContext getLastPerformanceContext() {
		return lastPerformanceContext;
	}

	public void setLastPerformanceContext(
			PerformanceContext lastPerformanceContext) {
		this.lastPerformanceContext = lastPerformanceContext;
	}


	@Override
	public String toString() {
		return "SNMPPerformanceMonitor [defaultEnterpriseOID="
				+ defaultEnterpriseOID + ", lastOMNotification="
				+ lastOMNotification + ", lastPerformanceContext="
				+ lastPerformanceContext + "]";
	}
}
