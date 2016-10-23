package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants;
import com.onmobile.snmp.agentx.client.NotificationVariable;
import com.onmobile.snmp.agentx.client.OID;
import com.onmobile.snmp.agentx.client.SeverityLevel;
import com.onmobile.snmp.agentx.client.Subagent;
import com.onmobile.snmp.agentx.client.SubagentFactory;
import com.onmobile.snmp.agentx.client.exceptions.DuplicateOIDException;
import com.onmobile.snmp.agentx.client.exceptions.MasterNotAliveException;
import com.onmobile.snmp.agentx.client.exceptions.SendingNotificationException;
import com.onmobile.snmp.agentx.client.exceptions.SubagentCreationException;
import com.onmobile.snmp.agentx.client.exceptions.SubagentNotReadyException;
import com.onmobile.snmp.agentx.client.util.DuplicateNotificationManager;
import com.onmobile.snmp.agentx.client.util.OMNotification;



/**
 * @author vasipalli.sreenadh
 *
 */
public class SNMPTrapRaiser implements AMConstants
{
	private static SNMPTrapRaiser snmpTrapRaiser = null;
	private static final Object _syncObj = new Object();
	
	private ResourceBundle resourceBundle;
	private String defaultEnterpriseOIDString = "1.3.6.1.4.1.10377.7.9999.1.9999";
	private String alarmEntityOID = "1.3.6.1.4.1.10377.5.2.1";
	private String alarmMessageOID = "1.3.6.1.4.1.10377.5.2.2";
	private String alarmUrlOID = "1.3.6.1.4.1.10377.5.2.3";
		
	private boolean isMonitoringEnabled = false;
	private boolean isThresholdTypeOfAlarm = false;
	private Subagent subagent = null;
	private DuplicateNotificationManager duplicateNotificationManager = null;
	
	private static final Logger logger = Logger.getLogger(SNMPTrapRaiser.class);
	
	public static SNMPTrapRaiser getInstance()
	{
		if (snmpTrapRaiser == null)
		{
			synchronized (_syncObj) 
			{
				if(snmpTrapRaiser == null)
				{
					snmpTrapRaiser = new SNMPTrapRaiser();
				}
			}
		}
		return snmpTrapRaiser;
	}
	
	private SNMPTrapRaiser()
	{
		init();
		try
		{
			subagent = SubagentFactory.createSubagent(null);
			duplicateNotificationManager = new DuplicateNotificationManager(subagent, 1000);
		}
		catch (DuplicateOIDException e1) 
		{
			logger.error("RBT:: Exception ", e1);
		}
		catch (SubagentCreationException e) 
		{
			logger.error("RBT:: Exception ", e);
		}
		catch (Exception e) 
		{
			logger.error("RBT:: Exception ", e);
		}
	}
	
	public boolean init()
	{
		return loadSnmpProperties();
	}

	public boolean loadSnmpProperties()
	{
		try
		{
			resourceBundle = ResourceBundle.getBundle("snmp");
			isMonitoringEnabled = resourceBundle.getString("isMonitoringEnabled").equalsIgnoreCase("true");
			String isThresholdTypeOfAlarmStr = resourceBundle.getString("isThresholdTypeOfAlarm");
			if (isThresholdTypeOfAlarmStr != null)
				isThresholdTypeOfAlarm = isThresholdTypeOfAlarmStr.trim().equalsIgnoreCase("true");
			alarmEntityOID = resourceBundle.getString("alarm.entity.OID").trim();
			alarmMessageOID = resourceBundle.getString("alarm.message.OID").trim();
			alarmUrlOID = resourceBundle.getString("alarm.url.OID").trim();
		}
		catch(Exception fnf)
		{
			logger.error("RBT:: Exception in loadSnmpProperties", fnf);
			return false;
		}
		return true;
	}
	
	public boolean isMonitoringEnabled()
	{
		return isMonitoringEnabled;	
	}
	
	public boolean isThresholdTypeOfAlarm()
	{
		return isThresholdTypeOfAlarm;	
	}
	
	public OMNotification raiseSNMPAlarm(String componentName, String url, SeverityLevel alarmSeverityLevel, Severity severity, String alarmMsg)
	{
		String entityOIDStr = resourceBundle.getString(componentName+"_"+String.valueOf(severity));
		
		if (entityOIDStr == null)
			entityOIDStr = defaultEnterpriseOIDString;
		
		OID entityOID = new OID(entityOIDStr);
	
		NotificationVariable[] checkedVariables = new NotificationVariable[2];
		checkedVariables[0] = new NotificationVariable(new OID(alarmUrlOID), url);
		checkedVariables[1] = new NotificationVariable(new OID(alarmEntityOID), componentName);
		
		NotificationVariable[] unCheckedVariables = new NotificationVariable[1];
		OMNotification omNotification = null;
		 
		try 
		{
			unCheckedVariables[0] = new NotificationVariable(new OID(alarmMessageOID), alarmMsg);
			omNotification = new OMNotification(entityOID, alarmSeverityLevel, alarmMsg, checkedVariables, unCheckedVariables);
			duplicateNotificationManager.sendNotification(omNotification);

		}
		catch (SubagentNotReadyException e) 
		{
			logger.error("RBT:: SubagentNotReadyException", e);
		}
		catch (MasterNotAliveException e) 
		{
			logger.error("RBT:: MasterNotAliveException", e);
		}
		catch (SendingNotificationException e) 
		{
			logger.error("RBT:: SendingNotificationException", e);
		}
		return omNotification;
	}
	
	public boolean clearAlarm(OMNotification omNotification)
	{
		boolean isCleared = false;
		try 
		{
			duplicateNotificationManager.clearNotification(omNotification, "normalcy is restored");
			isCleared = true;
		}
		catch (SubagentNotReadyException e) 
		{
			logger.error("RBT:: Exception", e);
			isCleared = false;
		}
		catch (MasterNotAliveException e) 
		{
			logger.error("RBT:: Exception", e);
			isCleared = false;
		}
		catch (SendingNotificationException e) 
		{
			logger.error("RBT:: Exception", e);
			isCleared = false;
		}
		return isCleared;
	}
	

}
