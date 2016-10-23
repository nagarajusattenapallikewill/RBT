package com.onmobile.apps.ringbacktones.daemons.reminder;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.ReconciliationLogger;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.content.database.TrialSelectionImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class TrialQueueComponent extends ReminderQueueComponent
{
	private static Logger logger = Logger.getLogger(TrialQueueComponent.class);
	
	TrialSelectionImpl trialSelectionImpl = null;
	public TrialQueueComponent(TrialSelectionImpl trialSelectionImpl) 
	{
		this.trialSelectionImpl = trialSelectionImpl;
	}

	private void executeQueueComponent(QueueContext queueContext) throws Exception
	{
		ReminderTool.processTrialSelection(trialSelectionImpl, smsDay);	
	}
	
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
	        ReconciliationLogger.log(trialSelectionImpl.seqID(), e);
	        queueContext.getQueueContainer().getPublisher().addQueueComponent(this);
	        isRetryable = true;
	    }
	    catch(Throwable e)
	    {
	        logger.error("", e);
	        ReconciliationLogger.log(trialSelectionImpl.seqID(), e);
	        //Utility.markForManualReconciliation(viralSMS);
	    }
	}

	public String getDisplayName()
	{
		return trialSelectionImpl.seqID()+"";
	}

	public long getSequenceNo()
	{
		return trialSelectionImpl.seqID();
	}

	public String getUniqueName()
	{
		return trialSelectionImpl.seqID()+"";
	}

}
