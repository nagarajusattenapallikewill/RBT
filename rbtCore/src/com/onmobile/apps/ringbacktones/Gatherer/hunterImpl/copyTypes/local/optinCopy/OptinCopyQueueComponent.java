package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optinCopy;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.LinkedSubsriberLookup;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class OptinCopyQueueComponent extends SiteQueueComponent
{
    public OptinCopyQueueComponent(ViralSMSTableImpl viralSMSTableImpl, LinkedSubsriberLookup linkedSubsriberLookup)
    {
        super(viralSMSTableImpl, linkedSubsriberLookup);
    }

    @Override
    public void failed(QueueContext queContext, Throwable e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void executeQueueComponent(QueueContext queueContext)
    {
    	ViralSMSTable vst = getViralSMS();
        String response = Utility.processOptinCopy(vst);
    }

}
