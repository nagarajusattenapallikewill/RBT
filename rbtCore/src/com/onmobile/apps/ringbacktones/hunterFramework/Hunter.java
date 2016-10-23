package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class represents one Hunter in the application. 
 * This is a holder of one CID Queue Container and map of Site-vice queue containers.
 * 
 * @author nandakishore
 * 
 */
public class Hunter
{
    /**
     * The CID Queue container.
     * This can be null for hunters which does not require circle identifiers.
     */
    private QueueContainer cidQueue = null;

    /**
     * The default hunter config source. Ideally the caller MUST give an 
     * implementation of this of this class overriding the getParameterValue
     * method.
     */
    private HunterConfigurator configurator = new HunterConfigurator(){};
    

    /**
     * Site vice Queue containers.
     * This can contain only one site queue container for hunters that does not 
     * require Site vice queue containers. But at run time this cannot be empty.
     */
    private HashMap<String, QueueContainer> siteQueContainer = new HashMap<String, QueueContainer>();
    
    /**
     * The name of the hunter this object represents.
     * This MUST be a unique hunter name else the register will throw an Exception.
     */
    private String hunterName = null;
    
    public void register()
    {
        initConfig();
        if(!configurator.isEnable())
        {
        	return;
        }
        HunterContainer.getHunterContainer().registerHunter(this);
        Set<Entry<String, QueueContainer>> entrySet = siteQueContainer.entrySet();
        for (Entry<String, QueueContainer> entry : entrySet) 
        {
			QueueContainer queueContainer = entry.getValue();
			queueContainer.setHunter(this);
			queueContainer.start();
		}
        if(cidQueue != null)
        {
            cidQueue.setHunter(this);
            cidQueue.start();
        }
    }

    protected void initConfig()
    {
        if(configurator != null)
        {
            configurator.configure(this);
        }
    }

    public void unRegister()
    {
        Set<Entry<String, QueueContainer>> entrySet = siteQueContainer.entrySet();
        for (Entry<String, QueueContainer> entry : entrySet) 
        {
            QueueContainer queueContainer = entry.getValue();
            queueContainer.setHunter(this);
            queueContainer.stop();
        }
        if(cidQueue != null)
        {
            cidQueue.setHunter(this);
            cidQueue.stop();
        }
        HunterContainer.getHunterContainer().unRegisterHunter(this);
    }

    
    public QueueContainer getCidQueue()
    {
        return cidQueue;
    }

    public String getHunterName()
    {
        return hunterName;
    }

    public QueueContainer getSiteQueueContainer(String siteName)
    {
        return siteQueContainer.get(siteName);

    }

    public HashMap<String, QueueContainer> getSiteQueContainer()
    {
        return siteQueContainer;
    }

    public void setCidQueue(QueueContainer cidQueue)
    {
        this.cidQueue = cidQueue;
    }

    public void setHunterName(String daemonName)
    {
        this.hunterName = daemonName;
    }

    public void setSiteQueContainer(HashMap<String, QueueContainer> siteQueContainer)
    {
        this.siteQueContainer = siteQueContainer;
    }

    public HunterConfigurator getConfigurator()
    {
        return configurator;
    }

    public void setConfigurator(HunterConfigurator configurator)
    {
        this.configurator = configurator;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    /**
	 * Returns the string representation of this class.
	 * 
	 * @return the string representation of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Hunter[hunterName = ");
		builder.append(hunterName);
		builder.append("]");
		return builder.toString();
	}
}
