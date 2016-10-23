package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.failedCopy;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.LinkedSubsriberLookup;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class FailedCopyQueueComponent extends SiteQueueComponent
{
    private FileDeleteListener fileDeleteListener = null;
    private HashMap<String, String> infoMap = null;
    public FailedCopyQueueComponent(ViralSMSTableImpl viralSMSTableImpl, LinkedSubsriberLookup linkedSubsriberLookup, FileDeleteListener fileDeleteListener, HashMap<String, String> infoMap)
    {
        super(viralSMSTableImpl, linkedSubsriberLookup);
        this.fileDeleteListener = fileDeleteListener;
        this.infoMap = infoMap;
    }

    @Override
    public void failed(QueueContext queContext, Throwable e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void executeQueueComponent(QueueContext queueContext)
    {
    	ViralSMSTableImpl vst = getViralSMS();
        String response = Utility.addFailedCopy(vst, infoMap);
        fileDeleteListener.deleteFileIfNeeded();
    }

}

