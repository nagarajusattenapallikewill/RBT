package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants;
import com.onmobile.apps.ringbacktones.activemonitoring.common.ActiveMonitoringException;
import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants.TaskType;
import com.onmobile.snmp.agentx.client.OID;
import com.onmobile.snmp.agentx.client.Subagent;
import com.onmobile.snmp.agentx.client.SubagentFactory;
import com.onmobile.snmp.agentx.client.exceptions.DuplicateOIDException;
import com.onmobile.snmp.agentx.client.exceptions.ManagedObjectNotRegisteredException;
import com.onmobile.snmp.agentx.client.exceptions.SubagentCreationException;


/**
 * @author vasipalli.sreenadh
 *
 */
public class ProcessorStats
{
	private static final Object _syncObj = new Object();
	protected int lowestBucketInMins = 3;
	private String moduleName = "com.onmobile.apps.ringbacktones";
	private static ProcessorStats processorStats = null;
	
	protected PerformanceSampler perfSamplerSMActUrl;
	protected PerformanceSampler perfSamplerSMDctUrl;
	protected PerformanceSampler perfSamplerSMRenUrl;
	protected PerformanceSampler perfSamplerSMUpgUrl;
	protected PerformanceSampler perfSamplerPlayerUrl;
	
	protected PerformanceSampler smActQueueClearanceSampler;
	protected PerformanceSampler smDctQueueClearanceSampler;
	
	
	private ResourceBundle resourceBundle;
	protected ObjectName objName;
	
	private static final Logger logger =  Logger.getLogger(ProcessorStats.class);
	
	public static ProcessorStats getInstance() throws ActiveMonitoringException
	{
		if (processorStats == null)
		{
			synchronized (_syncObj) 
			{
				if (processorStats == null)
				{
					processorStats = new ProcessorStats();
				}
			}
		}
		return processorStats;
	}
	
	private ProcessorStats() throws ActiveMonitoringException
	{
		resourceBundle = ResourceBundle.getBundle("snmp");
		
		String timerTaskInteval = resourceBundle.getString("lowestBucketDurationInMins");
		if (timerTaskInteval != null)
			lowestBucketInMins = Integer.parseInt(timerTaskInteval);
		
		// Gets the list of processes running in Exe/JVM
		String processesInJVM = resourceBundle.getString("processesInJVM");
		if (processesInJVM == null)
			processesInJVM = "SMDAEMON,PLAYERDAEMON,GATHERER";
		List<String>processList = Arrays.asList(processesInJVM.split(","));

		logger.debug("RBT:: processList in Jvm  "+processList);
		// Holds the list of managed objects to register with SNMPMaster.
		// Each performance object should have unique OID
		List<PerformanceSampler> counterList = new ArrayList<PerformanceSampler>();
		
		if(processList.contains(AMConstants.SMDAEMON))
		{	
			objName = new ObjectName(moduleName, AMConstants.SMDAEMON, "processActUrl"); 
			perfSamplerSMActUrl = new PerformanceSampler(objName, lowestBucketInMins, new OID(resourceBundle.getString("Act_Url_OID")));
	
			objName = new ObjectName(moduleName, AMConstants.SMDAEMON, "processDctUrl"); 
			perfSamplerSMDctUrl = new PerformanceSampler(objName, lowestBucketInMins, new OID(resourceBundle.getString("Dct_Url_OID")));
	
			objName = new ObjectName(moduleName, AMConstants.SMDAEMON, "processRenUrl"); 
			perfSamplerSMRenUrl = new PerformanceSampler(objName, lowestBucketInMins, new OID(resourceBundle.getString("Ren_Url_OID")));
			
			objName = new ObjectName(moduleName, AMConstants.SMDAEMON, "processUpgUrl");
			perfSamplerSMUpgUrl = new PerformanceSampler(objName, lowestBucketInMins, new OID(resourceBundle.getString("Upg_Url_OID")));
			
			objName = new ObjectName(moduleName, AMConstants.SMDAEMON, "processPlayerUrl");
			perfSamplerPlayerUrl = new PerformanceSampler(objName, lowestBucketInMins, new OID(resourceBundle.getString("Player_Url_OID")));
			
			counterList.add(perfSamplerSMActUrl);
			counterList.add(perfSamplerSMDctUrl);
			counterList.add(perfSamplerSMRenUrl);
			counterList.add(perfSamplerSMUpgUrl);
			counterList.add(perfSamplerPlayerUrl);
		}
		
		registerWithAgent(counterList);
	}
	
	public void registerWithAgent(List<PerformanceSampler> processList)
	{
		try 
		{
			Subagent subagent;
			subagent = SubagentFactory.createSubagent(processList);
			logger.info("RBT:: failedObjectsMap >"+subagent.getFailedManagedObjects());
		}	
		catch (DuplicateOIDException e1) 
		{
			logger.error("RBT:: DuplicateOIDException", e1);
		}
		catch (ManagedObjectNotRegisteredException e) 
		{
			logger.error("RBT:: ManagedObjectNotRegisteredException", e);
		}
		catch (SubagentCreationException e) 
		{
			logger.error("RBT:: SubagentCreationException", e);
		}
	}

	public void addDelay(TaskType taskType, long delay)
	{
		if(taskType == TaskType.TASKTYPE_SMACTURL)
			perfSamplerSMActUrl.addSample(delay);
		else if(taskType == TaskType.TASKTYPE_SMDCTURL)
			perfSamplerSMDctUrl.addSample(delay);
		else if(taskType == TaskType.TASKTYPE_SMRENURL)
			perfSamplerSMRenUrl.addSample(delay);
		else if(taskType == TaskType.TASKTYPE_SMUPGURL)
			perfSamplerSMRenUrl.addSample(delay);
		else if(taskType == TaskType.TASKTYPE_PLAYERURL)
			perfSamplerPlayerUrl.addSample(delay);
	}
	
	public void markStartOfSampling(TaskType taskType)
	{
		if(taskType == TaskType.TASKTYPE_SMACTURL)
			smActQueueClearanceSampler.markStartOfSampling();
		else if(taskType == TaskType.TASKTYPE_SMDCTURL)
			smDctQueueClearanceSampler.markStartOfSampling();
	}
		
}
