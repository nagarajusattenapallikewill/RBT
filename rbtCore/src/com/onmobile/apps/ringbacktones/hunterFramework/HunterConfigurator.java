package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class is the one which is used to define the hunter configration.
 * The implementation of this class should override the getParameterValue, to 
 * give the parameter value from underlying configuration.
 * 
 * @author nandakishore
 *
 */
public abstract class HunterConfigurator
{
    public static final String FetchSizeParam = "fetchSize";
    public static final String UpperThresholdParam = "upperThreshold";
    public static final String LowerThresholdParam = "lowerThreshold";
    public static final String MaxWorkerThreadsParam = "maxWorkerThreads";
    public static final String WorkerThreadFactorParam = "workerThreadFactor";
	public static final String EnableParam = "enable";

    public static int DefaultSiteFetchSize = 2000;
    public static int DefaultSiteUpperThreshold = 10000;
    public static int DefaultSiteLowerThreshold = 8000;
    public static int DefaultSiteMaxWorkerThreads = 25;
    public static int DefaultSiteWorkerThreadFactor = 50;

    public static int DefaultCidFetchSize = 2000;
    public static int DefaultCidUpperThreshold = 10000;
    public static int DefaultCidLowerThreshold = 8000;
    public static int DefaultCidMaxWorkerThreads = 75;
    public static int DefaultCidWorkerThreadFactor = 50;

    public int getParameterValue(String paramName, int defaultValue)
    {
        return defaultValue;
    }

    /**
     * Configures the hunter from using the getParameterValue method implementation.
     * @param hunter
     */
    public void configure(Hunter hunter)
    {
        HashMap<String, QueueContainer> siteQueContainer = hunter.getSiteQueContainer();
        Set<Entry<String, QueueContainer>> entrySet = siteQueContainer.entrySet();
        for (Entry<String, QueueContainer> entry : entrySet)
        {
            QueueContainer queueContainer = entry.getValue();
            configure(hunter, queueContainer, true);
        }
        QueueContainer cidQueue = hunter.getCidQueue();
        if (cidQueue != null)
        {
            configure(hunter, cidQueue, false);
        }
    }
    private boolean enable = true;

	public void configure(Hunter hunter, QueueContainer queueContainer, boolean isForSite)
    {
        String prefix = null;
        if (isForSite)
        {
            prefix = hunter.getHunterName();
        }
        else
        {
            prefix = hunter.getHunterName() +"."+ queueContainer.getQueueContainerName();
        }
        enable = getParameterValue(prefix + "." + EnableParam, 1) == 1;
        queueContainer.setUpperWaterMark(getParameterValue(prefix + "." + UpperThresholdParam, (isForSite ? DefaultSiteUpperThreshold : DefaultCidUpperThreshold)));
        queueContainer.setLowerWaterMark(getParameterValue(prefix + "." + LowerThresholdParam, (isForSite ? DefaultSiteLowerThreshold : DefaultCidLowerThreshold)));
        queueContainer.setMaxAllowedWorkers(getParameterValue(prefix + "." + MaxWorkerThreadsParam, (isForSite ? DefaultSiteMaxWorkerThreads : DefaultCidMaxWorkerThreads)));
        queueContainer.setUpperWaterMark(getParameterValue(prefix + "." + UpperThresholdParam, (isForSite ? DefaultSiteUpperThreshold : DefaultCidUpperThreshold)));
        Publisher publisher = queueContainer.getPublisher();
        if (publisher != null)
        {
            publisher.setMaxFetchSize(getParameterValue(prefix + "." + FetchSizeParam, (isForSite ? DefaultSiteFetchSize : DefaultCidFetchSize)));
        }
    }
    
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
