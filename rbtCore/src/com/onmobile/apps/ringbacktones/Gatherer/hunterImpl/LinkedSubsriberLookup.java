package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl;

import java.util.HashMap;

public class LinkedSubsriberLookup
{
    private HashMap<String, SiteQueueComponent> linkedComponentsMap = new HashMap<String, SiteQueueComponent>();

    public synchronized boolean add(SiteQueueComponent siteQueueComponent)
    {
        boolean result = true;
        String uniqueName = siteQueueComponent.getUniqueName();
        SiteQueueComponent exsisting = linkedComponentsMap.get(uniqueName);
        if (exsisting != null && exsisting != siteQueueComponent)
        {
            result = false;
            exsisting.addToLinkedQueue(siteQueueComponent);
        }
        else
        {
            linkedComponentsMap.put(uniqueName, siteQueueComponent);
        }
        return result;
    }

    public synchronized void remove(SiteQueueComponent siteQueueComponent)
    {
        String uniqueName = siteQueueComponent.getUniqueName();
        SiteQueueComponent fromMap = linkedComponentsMap.remove(uniqueName);
        if(fromMap != null)
        {
            SiteQueueComponent queueComponent = siteQueueComponent.getLinkedQueue();
            if(queueComponent != null)
            {
                linkedComponentsMap.put(uniqueName,queueComponent);
            }
        }
    }
}
