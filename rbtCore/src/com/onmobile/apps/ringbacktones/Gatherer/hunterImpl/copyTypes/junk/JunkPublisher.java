package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.junk;

import java.sql.SQLException;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class JunkPublisher extends ProgressiveCopyPublisher
{
    public JunkPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }


    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        JunkCopyQueueComponent result = new JunkCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            JunkCopyQueueComponent result = new JunkCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

}
