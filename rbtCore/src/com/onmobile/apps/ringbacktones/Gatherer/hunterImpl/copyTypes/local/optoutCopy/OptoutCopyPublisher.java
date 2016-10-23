package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optoutCopy;

import java.sql.SQLException;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class OptoutCopyPublisher extends ProgressiveCopyPublisher
{

    public OptoutCopyPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        OptoutCopyQueueComponent result = new OptoutCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            OptoutCopyQueueComponent result = new OptoutCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

}
