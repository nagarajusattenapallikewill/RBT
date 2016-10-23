package com.onmobile.apps.ringbacktones.daemons.callbackEvents;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RBTHunterConfigurator;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;

public class RBTCallbackEventsBootStrap 
{
	
	private static final String HunterNameCallbackEvent = "RBTCallbackEvent";
	private static final String HunterAllTypes = "AllTypes";
	
	public void registerCallbackEventHunter() 
	{
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameCallbackEvent);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveCallbackEventsPublisher updatePublisher = new ProgressiveCallbackEventsPublisher();
		QueueContainer queueContainer = new QueueContainer(updatePublisher);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer); // Change this when other kinds of publishers are working like CrossPromo,Ugc etc
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
	}
	
	public static void startUp() 
	{
		RBTCallbackEventsBootStrap rbtCallbackEventsBootStrap = new RBTCallbackEventsBootStrap();
		rbtCallbackEventsBootStrap.registerCallbackEventHunter();
	}
}
