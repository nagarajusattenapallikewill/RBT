package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RBTHunterConfigurator;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveSqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.ThreadManager;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

/*
 * This daemon is used for TNB optin, TNB optout and ESIA trial feature. The details can be found in confluence under RBT Current 
 * Components at SMS daemon ans SMS daemon Re-designed. 
 * 
 * This daemon van be started in three ways. a) Stand-alone daemon in console, b) Stand-alone daemon ozonized and c) As a thread of SM Daemon
 * It uses hunter framework.
 * 
 * To make sure that only one instance of this Reminder Daemon is running, it blocks a port 25025 by default. The port number is configurable.
 * 
 * */
public class ReminderDaemon extends Thread implements ConstantsTools
{
	private static Logger logger = Logger.getLogger(ReminderDaemon.class);
	
	public static ReminderDaemon singletonRD = null;
	private static Object lock = new Object();
	private static ReminderPublisherRotator rpRotator = null;
	
	public static void main(String[] args)
	{
		System.out.println("Starting Reminder Daemon");
		ReminderDaemon.getInstance().run();
	}
	
	public static ReminderDaemon getInstance()
	{
		if(singletonRD != null)
			return singletonRD;
		
		synchronized (lock)
		{
			if(singletonRD != null)
				return singletonRD;
			singletonRD = new ReminderDaemon();
			singletonRD.setName("ReminderDaemon");
			return singletonRD;
		}
	}

	public void run()
	{
		ReminderDaemon.getInstance();
		singletonRD.initialize();
	}
	
	
	private void initialize()
	{
		Tools.init("RBTReminderDaemon", true);
		logger.info("Starting Reminder Daemon");
		ReminderTool.init();
		if(ReminderTool.siteList != null && ReminderTool.siteList.size() > 0)
		{
			rpRotator = new ReminderPublisherRotator();
			rpRotator.setUniqueName(ReminderPublisherRotator.class.getName());
			setUpHunters();
			boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
			HashMap<String,ArrayList<Integer>> tnbOptinMap = null;
			if(!is121TnbEnabled) {
				ReminderTool.getTNBOptinClasses();
				tnbOptinMap = ReminderTool.tnbOptinMap;
			}
			else {
				ReminderTool.getOldTNBOptinClasses();
				tnbOptinMap = ReminderTool.tnbOldOptinMap;
			}
			if(tnbOptinMap.size() > 0 || ReminderTool.tnbOptoutMap.size() > 0 || ReminderTool.esiaTrialMap.size() > 0)
			{
				rpRotator.setUp();
				ThreadManager.getThreadManager().addManagedThread(rpRotator);
				if(DBConfigTools.getParameter(DAEMON, START_REMINDER_CLEANUP, true))
					ReminderCleanUp.getInstance();
			}
			else
				logger.info("No reminder charge packs or days found. The Reminder daemon will remain passive.");
		}
	}

	private void setUpHunters()
	{
		logger.info("SMS Daemon mode="+ReminderTool.reminderDaemonModes);
		if(ReminderTool.reminderDaemonModes == null || ReminderTool.reminderDaemonModes.size() == 0)
			return;
		if(ReminderTool.reminderDaemonModes.contains("SUB"))
			initTNBOptinDaemon();
		if(ReminderTool.reminderDaemonModes.contains("SEL"))
			initTNBOptoutDaemon();
		if(ReminderTool.reminderDaemonModes.contains("SUB_CLASS"))
			initTrialDaemon();
		logger.info("Exit");
	}

	private void initTNBOptinDaemon()
	{
		boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
		HashMap<String,ArrayList<Integer>> tnbOptinMap = null;
		if(!is121TnbEnabled) {
			ReminderTool.getTNBOptinClasses();
			tnbOptinMap = ReminderTool.tnbOptinMap;
		}
		else {
			ReminderTool.getOldTNBOptinClasses();
			tnbOptinMap = ReminderTool.tnbOldOptinMap;
		}

		logger.info("ReminderTool.tnbOptinMap="+tnbOptinMap);
		if(tnbOptinMap == null || tnbOptinMap.size() == 0)
			return;
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(TNB_OPTIN);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ReminderTool.initTnbOptinTransactionFile();
		for(String sites : ReminderTool.siteList)
		{	
			
			for(String key : tnbOptinMap.keySet())
			{
				SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(key);
				int subscriptionPeriod = subscriptionClass.getSubscriptionPeriodInDays();
				
				ArrayList<Integer> reminderDays = tnbOptinMap.get(key);
				if(reminderDays == null || reminderDays.size() == 0)
					continue;
				int i = 0;
				reminderDays.add(-1);
				for(Integer dayNumber : reminderDays)
				{	
					ProgressiveSqlQueryPublisher reminderPublisher = new ReminderPublisher(TNB_OPTIN, key, sites, i++, dayNumber.intValue(), subscriptionPeriod);
					reminderPublisher.setPublisherActive(false);
					QueueContainer queueContainer = new QueueContainer(reminderPublisher);
					String queueContainerName = TNB_OPTIN+"_"+sites+"_"+key+"_"+dayNumber.intValue();
					queueContainer.setQueueContainerName(queueContainerName);
					nameQueueContainerMap.put(queueContainerName, queueContainer);
				}
			}
		}
		hunter.setSiteQueContainer(nameQueueContainerMap);
		printHunter(hunter);
		hunter.register();
		rpRotator.addHunter(hunter);
	}

	private void printHunter(Hunter hunter)
	{
		if(hunter == null)
			return;
		String name = hunter.getHunterName();
		logger.info("Hunter made="+name);
		if(hunter.getSiteQueContainer() == null)
		{
			logger.info("No queue container found in hunter "+name);
			return;
		}
		
		for(QueueContainer queueContainer : hunter.getSiteQueContainer().values())
			logger.info(queueContainer.getQueueContainerName() + " queue Container found in hunter "+name);
	}

	private void initTNBOptoutDaemon()
	{
		ReminderTool.getTNBOptoutClasses();
		logger.info("ReminderTool.tnbOptoutMap="+ReminderTool.tnbOptoutMap);
		if(ReminderTool.tnbOptoutMap == null || ReminderTool.tnbOptoutMap.size() == 0)
			return;
		ReminderTool.initTnbOptoutTransactionFile();
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(TNB_OPTOUT);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : ReminderTool.siteList)
		{
			for(String key : ReminderTool.tnbOptoutMap.keySet())
			{
				SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(key);
				int subscriptionPeriod = subscriptionClass.getSubscriptionPeriodInDays();
				
				ArrayList<Integer> reminderDays = ReminderTool.tnbOptoutMap.get(key);
				if(reminderDays == null || reminderDays.size() == 0)
					continue;
				int i = 0;
				for(Integer dayNumber : reminderDays)
				{	
					ProgressiveSqlQueryPublisher reminderPublisher = new ReminderPublisher(TNB_OPTOUT, key, sites, i++, dayNumber.intValue(), subscriptionPeriod);
					reminderPublisher.setPublisherActive(false);
					QueueContainer queueContainer = new QueueContainer(reminderPublisher);
					String queueContainerName = TNB_OPTOUT+"_"+sites+"_"+key+"_"+dayNumber.intValue();
					queueContainer.setQueueContainerName(queueContainerName);
					nameQueueContainerMap.put(queueContainerName, queueContainer);
				}
			}
		}
		hunter.setSiteQueContainer(nameQueueContainerMap);
		printHunter(hunter);
		hunter.register();
		rpRotator.addHunter(hunter);
	}

	private void initTrialDaemon()
	{
		ReminderTool.getTrialClassesToProcess();
		logger.info("ReminderTool.esiaTrialMap="+ReminderTool.esiaTrialMap);
		if(ReminderTool.esiaTrialMap == null || ReminderTool.esiaTrialMap.size() == 0)
			return;
		ReminderTool.initTrialTransactionFile();
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(TRIAL);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : ReminderTool.siteList)
		{
			for(String key : ReminderTool.esiaTrialMap.keySet())
			{
				SubscriptionClass subscriptionClass = DBConfigTools.getSubscriptionClass(key);
				int subscriptionPeriod = subscriptionClass.getSubscriptionPeriodInDays();
				
				ArrayList<Integer> reminderDays = ReminderTool.esiaTrialMap.get(key);
				if(reminderDays == null || reminderDays.size() == 0)
					continue;
				int i = 0;
				for(Integer dayNumber : reminderDays)
				{	
					ProgressiveSqlQueryPublisher reminderPublisher = new ReminderPublisher(TRIAL, key, sites, i++, dayNumber.intValue(), subscriptionPeriod);
					reminderPublisher.setPublisherActive(false);
					QueueContainer queueContainer = new QueueContainer(reminderPublisher);
					String queueContainerName = TRIAL+"_"+sites+"_"+key+"_"+dayNumber.intValue();
					queueContainer.setQueueContainerName(queueContainerName);
					nameQueueContainerMap.put(queueContainerName, queueContainer);
				}
			}
		}
		hunter.setSiteQueContainer(nameQueueContainerMap);
		printHunter(hunter);
		hunter.register();
		rpRotator.addHunter(hunter);
	}
}
