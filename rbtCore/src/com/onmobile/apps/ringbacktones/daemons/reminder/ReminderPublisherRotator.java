package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

public class ReminderPublisherRotator extends ManagedDaemon implements ConstantsTools
{

	private static Logger logger = Logger.getLogger(ReminderPublisherRotator.class);
	
	private static HashSet<Hunter> hunterSet = new HashSet<Hunter>();
	private static HashMap<String,ArrayList<ReminderPublisher>> hunterPublisherMap = new HashMap<String, ArrayList<ReminderPublisher>>();
	private static int remPubRotateNum = 1;
	@Override
	protected void execute()
	{
		logger.info("Entering with remPubRotateNum="+remPubRotateNum);
		for(String key : hunterPublisherMap.keySet())
		{
			logger.info("hunter name="+key);
			for(ReminderPublisher remPub : hunterPublisherMap.get(key))
				logger.info("rempub name="+remPub.getUniqueName());	
		}
		for(ArrayList<ReminderPublisher> remPubList : hunterPublisherMap.values())
		{
			int counter=0;
			for(int i = 0; i < remPubList.size(); i++)
			{
				if(remPubRotateNum >= remPubList.size())
				{
					ReminderPublisher reminderPublisher = remPubList.get(i);
					logger.info("reminderPublisher count is very less. Activating reminderPublisher="+reminderPublisher.getUniqueName());
					reminderPublisher.setPublisherActive(true);
				}
				else
				{
					if(i < remPubRotateNum)
					{
						ReminderPublisher reminderPublisher = remPubList.remove(0);
						logger.info("reminderPublisher count is high. Activating reminderPublisher="+reminderPublisher.getUniqueName());
						reminderPublisher.setPublisherActive(true);
						remPubList.add(reminderPublisher);
					}
					else
					{
						ReminderPublisher reminderPublisher = remPubList.get(counter);
						counter++;
						logger.info("reminderPublisher count is high. Deactivating reminderPublisher="+reminderPublisher.getUniqueName());
						reminderPublisher.setPublisherActive(false);
					}
					
				}	
			}
		}
		makeThreadSleep();
	}

	private void makeThreadSleep()
	{
		int sleepSecs = DBConfigTools.getParameter(DAEMON, REM_PUB_ROTATE_SLEEP_SEC, 300);
		try
		{
			Thread.sleep(sleepSecs*1000);
		}
		catch(InterruptedException i)
		{
			logger.error("", i);
		}
		
	}

	@Override
	public Object getLockObject()
	{
		return null;
	}

	public void addHunter(Hunter hunter)
	{
		hunterSet.add(hunter);
	}

	public void setUp()
	{
		logger.info("hunterSet="+hunterSet);
		for(Hunter hunter : hunterSet)
		{
			String name = hunter.getHunterName();
			ArrayList<ReminderPublisher> remPubList = new ArrayList<ReminderPublisher>();
			for(QueueContainer queueContainer : hunter.getSiteQueContainer().values())
			{
				ReminderPublisher remPublisher = (ReminderPublisher)queueContainer.getPublisher();
				remPubList.add(remPublisher);
			}
			hunterPublisherMap.put(name, remPubList);
		}
		for(String key : hunterPublisherMap.keySet())
		{
			logger.info("hunter name="+key);
			for(ReminderPublisher remPub : hunterPublisherMap.get(key))
				logger.info("rempub name="+remPub.getUniqueName());	
		}
		logger.info("hunterPublisherMap="+hunterPublisherMap);
		remPubRotateNum = DBConfigTools.getParameter(DAEMON, REM_PUB_ROTATE_NUM, 1);
	}
}
