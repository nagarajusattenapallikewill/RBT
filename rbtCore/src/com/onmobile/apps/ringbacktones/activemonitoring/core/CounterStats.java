package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants;
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
public class CounterStats 
{
	private static final Object _syncObj = new Object();
	private static CounterStats counterStats = null;
	private Subagent subagent = null;

	// List of SMDaemon counterStats which are available for SNMP GET
	protected Counter baseActCounter = null;
	protected Counter baseDctCounter = null;
	protected Counter selActCounter = null;
	protected Counter selDctCounter = null;
	protected Counter dwnActCounter = null;
	protected Counter dwnDctCounter = null;
	
	// Gatherer counterStats
	protected Counter copyCounter = null;
	
	// PlayerDaemon counterStats
	protected Counter playerBaseActCounter = null;
	protected Counter playerBaseDctCounter = null;
	protected Counter playerSelActCounter = null;
	protected Counter playerSelDctCounter = null;
	
	private ResourceBundle resourceBundle;
	private static final Logger logger =  Logger.getLogger(CounterStats.class);

	public static CounterStats getInstance()
	{
		if (counterStats == null)
		{
			synchronized (_syncObj) 
			{
				if(counterStats == null)
				{
					counterStats = new CounterStats();
				}
			}
		}
		return counterStats;
	}

	private CounterStats()
	{
		try
		{
			resourceBundle = ResourceBundle.getBundle("snmp");

			// Gets the list of processes running in Exe/JVM
			String processesInJVM = resourceBundle.getString("processesInJVM");
			if (processesInJVM == null)
				processesInJVM = "SMDAEMON,PLAYERDAEMON,GATHERER";
			List<String>processList = Arrays.asList(processesInJVM.split(","));

			logger.debug("RBT:: processList in Jvm  "+processList);
			// Holds the list of counter managed objects to register with SNMPMaster.
			// Each counter object should have unique OID
			List<Counter> counterList = new ArrayList<Counter>();
			
			if(processList.contains(AMConstants.SMDAEMON))
			{
				baseActCounter = new Counter(new OID(resourceBundle.getString("Base_Act_Count_OID")), "BASE_ACT");
				baseDctCounter = new Counter(new OID(resourceBundle.getString("Base_Dct_Count_OID")), "BASE_DCT");
				selActCounter = new Counter(new OID(resourceBundle.getString("Sel_Act_Count_OID")), "SEL_ACT");
				selDctCounter = new Counter(new OID(resourceBundle.getString("Sel_Dct_Count_OID")), "SEL_DCT");
				dwnActCounter = new Counter(new OID(resourceBundle.getString("Dwn_Act_Count_OID")), "DOWNLOAD_ACT");
				dwnDctCounter = new Counter(new OID(resourceBundle.getString("Dwn_Dct_Count_OID")), "DOWNLOAD_DCT");

				counterList.add(baseActCounter);
				counterList.add(baseDctCounter);
				counterList.add(selActCounter);
				counterList.add(selDctCounter);
				counterList.add(dwnActCounter);
				counterList.add(dwnDctCounter);

			}
			if(processList.contains(AMConstants.PLAYERDAEMON))
			{
				playerBaseActCounter = new Counter(new OID(resourceBundle.getString("Player_Base_Act_Count_OID")), "PLAYER_BASE_ACT");
				playerBaseDctCounter = new Counter(new OID(resourceBundle.getString("Player_Base_Dct_Count_OID")), "PLAYER_BASE_DCT");
				playerSelActCounter = new Counter(new OID(resourceBundle.getString("Player_Sel_Act_Count_OID")), "PLAYER_SEL_ACT");
				playerSelDctCounter = new Counter(new OID(resourceBundle.getString("Player_Sel_Dct_Count_OID")), "PLAYER_SEL_DCT");
				
				counterList.add(playerBaseActCounter);
				counterList.add(playerBaseDctCounter);
				counterList.add(playerSelActCounter);
				counterList.add(playerSelDctCounter);
			}
			if(processList.contains(AMConstants.GATHERER))
			{	
				copyCounter = new Counter(new OID(resourceBundle.getString("Copy_Count_OID")), "COPY");
				
				counterList.add(copyCounter);
			}
			// Registers the counter managed objects with Master
			registerCountersWithAgent(counterList);
			
		}
		catch(Exception e)
		{
			logger.error("RBT:: CounterStats()", e);
		}
	}

	private boolean registerCountersWithAgent(List<Counter> counterList)
	{
		try 
		{
			subagent = SubagentFactory.createSubagent(counterList);
			logger.info("RBT:: failedObjectsMap >"+subagent.getFailedManagedObjects());
		}	
		catch (DuplicateOIDException e1) 
		{
			logger.error("RBT:: DuplicateOIDException", e1);
			return false;
		}
		catch (ManagedObjectNotRegisteredException e) 
		{
			logger.error("RBT:: ManagedObjectNotRegisteredException", e);
		}
		catch (SubagentCreationException e) 
		{
			logger.error("RBT:: SubagentCreationException", e);
			return false;
		}
		return true;
	}

	/** SMDaemon Set Methods **/
	
	public void setBaseActCount(int count, Date date)
	{
		if (baseActCounter != null)
		{	
			logger.info("RBT:: setting baseactCount "+count);
			baseActCounter.setCounter(count);
			baseActCounter.setDate(date);
		}
	}

	public void setBaseDctCount(int count, Date date)
	{
		if (baseDctCounter != null)
		{	
			baseDctCounter.setCounter(count);
			baseDctCounter.setDate(date);
		}
	}

	public void setSelActCount(int count, Date date)
	{
		if (selActCounter != null)
		{
			logger.info("RBT:: setting selactCount "+count);
			selActCounter.setCounter(count);
			selDctCounter.setDate(date);
		}
	}

	public void setSelDctCount(int count, Date date)
	{
		if (selDctCounter != null)
		{	
			selDctCounter.setCounter(count);
			selDctCounter.setDate(date);
		}
	}
	
	public void setDwnActCount(int count, Date date)
	{
		if (dwnActCounter != null)
		{	
			dwnActCounter.setCounter(count);
			dwnActCounter.setDate(date);
		}
	}

	public void setDwnDctCount(int count, Date date)
	{
		if (dwnDctCounter != null)
		{	
			dwnDctCounter.setCounter(count);
			dwnDctCounter.setDate(date);
		}
	}
	
	
	/** Gatherer **/
	
	public void setCopyCount(int count, Date date)
	{
		if (copyCounter != null)
		{	
			copyCounter.setCounter(count);
			copyCounter.setDate(date);
		}
	}
	
	/** Player counters **/
	public void setPlayerBaseActCount(int count, Date date)
	{
		if (playerBaseActCounter != null)
		{	
			playerBaseActCounter.setCounter(count);
			playerBaseActCounter.setDate(date);
		}
	}
	
	public void setPlayerBaseDctCount(int count, Date date)
	{
		if (playerBaseDctCounter != null)
		{
			playerBaseDctCounter.setCounter(count);
			playerBaseDctCounter.setDate(date);
		}
	}
	
	public void setPlayerSelActCount(int count, Date date)
	{
		if (playerSelActCounter != null)
		{
			playerSelActCounter.setCounter(count);
			playerSelActCounter.setDate(date);
		}
	}
	
	public void setPlayerSelDctCount(int count, Date date)
	{
		if (playerSelDctCounter != null)
		{	
			playerSelDctCounter.setCounter(count);
			playerSelDctCounter.setDate(date);
		}
	}

}
