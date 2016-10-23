package com.onmobile.apps.ringbacktones.daemons.reminder;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.ReconciliationLogger;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.content.database.TnbSubscriberImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class TNBOptoutQueueComponent extends ReminderQueueComponent
{
	private static Logger logger = Logger.getLogger(TNBOptoutQueueComponent.class);
	
	TnbSubscriberImpl tnbSubscriberImpl = null;
	public TNBOptoutQueueComponent(TnbSubscriberImpl tnbSubscriberImpl) 
	{
		this.tnbSubscriberImpl = tnbSubscriberImpl;
	}

	private void executeQueueComponent(QueueContext queueContext) throws Exception
	{
		ReminderTool.processTNBOptout(tnbSubscriberImpl, smsDay);	
	}
	
	public void execute(QueueContext queueContext)
	{
	    try
	    {
	        executeQueueComponent(queueContext);
	    }
	    catch(RetryableException e)
	    {
	        logger.error("", e);
	        ReconciliationLogger.log(tnbSubscriberImpl.seqID(), e);
	        queueContext.getQueueContainer().getPublisher().addQueueComponent(this);
	    }
	    catch(Throwable e)
	    {
	        logger.error("", e);
	        ReconciliationLogger.log(tnbSubscriberImpl.seqID(), e);
	        //Utility.markForManualReconciliation(viralSMS);
	    }
	}

	public String getDisplayName()
	{
		return tnbSubscriberImpl.seqID()+"";
	}

	public long getSequenceNo()
	{
		return tnbSubscriberImpl.seqID();
	}

	public String getUniqueName()
	{
		return tnbSubscriberImpl.seqID()+"";
	}

}
