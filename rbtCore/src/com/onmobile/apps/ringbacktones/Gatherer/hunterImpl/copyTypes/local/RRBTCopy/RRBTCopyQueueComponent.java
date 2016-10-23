package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.RRBTCopy;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.ReconciliationLogger;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class RRBTCopyQueueComponent extends QueueComponent
{
	private static Logger logger = Logger.getLogger(RRBTCopyQueueComponent.class);
   
	 private ViralSMSTableImpl viralSMS = null;

     public RRBTCopyQueueComponent(ViralSMSTableImpl viralSMS)
     {
         super();
         this.viralSMS = viralSMS;
       
     }
         
	  @Override
	    public String getUniqueName()
	    {
	        return viralSMS.callerID();
	    }
	  @Override
	    public long getSequenceNo()
	    {
	        return viralSMS.getSmsId();
	    }

	    @Override
	    public void execute(QueueContext queueContext)
	    {
	        boolean isRetryable = false;
	        try
	        {
	            executeQueueComponent(queueContext);
	        }
	         catch(Throwable e)
	        {
	        	logger.error("", e);
	            ReconciliationLogger.log(viralSMS.getSmsId(), e);
	            Utility.markForManualReconciliation(viralSMS);
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
	    	return result.toString();
	    }

	    @Override
	    public Date getObjectCreationTime()
	    {
	        return viralSMS.sentTime();
	    }


    @Override
    public void failed(QueueContext queContext, Throwable e)
    {
        // TODO Auto-generated method stub

    }
    public ViralSMSTableImpl getViralSMS()
    {
        return viralSMS;
    }

    public void setViralSMS(ViralSMSTableImpl viralSMS)
    {
        this.viralSMS = viralSMS;
    }
   
    protected void executeQueueComponent(QueueContext queueContext)
    {
    	logger.info("Inside execute");
    	ViralSMSTableImpl vst = getViralSMS();
    	logger.info("Gonna process the RRBT req");
        Utility.processRRBTCopy(vst);
    }

}
