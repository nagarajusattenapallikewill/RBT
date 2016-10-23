package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public abstract class SiteQueueComponent extends QueueComponent
{
	private static Logger logger = Logger.getLogger(SiteQueueComponent.class);
	
    private ViralSMSTableImpl viralSMS = null;
    private LinkedSubsriberLookup linkedSubsriberLookup = null;

    public SiteQueueComponent(ViralSMSTableImpl viralSMS, LinkedSubsriberLookup linkedSubsriberLookup)
    {
        super();
        this.viralSMS = viralSMS;
        this.linkedSubsriberLookup = linkedSubsriberLookup;
    }

    private SiteQueueComponent linkedQueue = null;

    public SiteQueueComponent getLinkedQueue()
    {
        return linkedQueue;
    }

    public void addToLinkedQueue(SiteQueueComponent queueComponent)
    {
        
        if (linkedQueue == null)
        {
            linkedQueue = queueComponent;
        }
        else if(linkedQueue != queueComponent)
        {
            linkedQueue.addToLinkedQueue(queueComponent);
        }
    }

    public void setLinkedQueue(SiteQueueComponent linkedQueue)
    {
        this.linkedQueue = linkedQueue;
    }

    public ViralSMSTableImpl getViralSMS()
    {
        return viralSMS;
    }

    public void setViralSMS(ViralSMSTableImpl viralSMS)
    {
        this.viralSMS = viralSMS;
    }

    @Override
    public long getSequenceNo()
    {
        return viralSMS.getSmsId();
    }

    @Override
    public String getUniqueName()
    {
        return viralSMS.callerID();
    }

    @Override
    public void execute(QueueContext queueContext)
    {
        boolean isRetryable = false;
        try
        {
            executeQueueComponent(queueContext);
        }
        catch(RetryableException e)
        {
            logger.error("", e);
            ReconciliationLogger.log(viralSMS.getSmsId(), e);
            queueContext.getQueueContainer().getPublisher().addQueueComponent(this);
            isRetryable = true;
        }
        catch(Throwable e)
        {
        	logger.error("", e);
            ReconciliationLogger.log(viralSMS.getSmsId(), e);
            Utility.markForManualReconciliation(viralSMS);
        }
        finally
        {
            if(!isRetryable)
            {
                if(linkedSubsriberLookup != null)
                {
                    linkedSubsriberLookup.remove(this);
                }
                if (linkedQueue != null)
                {
                    linkedQueue.execute(queueContext);
                }
                
            }
        }
    }

    @Override
    public String getDisplayName()
    {
    	StringBuffer result = new StringBuffer();
    	result.append("Caller Id=");
    	result.append(viralSMS.callerID());
    	result.append(", Clip Id= ");
    	result.append(viralSMS.clipID());
    	if(linkedQueue != null)
    	{
    		result.append("| ");
    		result.append(linkedQueue.getDisplayName());
    	}
    	return result.toString();
    }

    @Override
    public Date getObjectCreationTime()
    {
        return viralSMS.sentTime();
    }

    abstract protected void executeQueueComponent(QueueContext queueContext) throws Exception;
}
