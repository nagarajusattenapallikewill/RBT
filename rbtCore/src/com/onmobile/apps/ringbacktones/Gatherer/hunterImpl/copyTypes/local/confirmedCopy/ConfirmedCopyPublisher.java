package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.confirmedCopy;

import java.sql.SQLException;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class ConfirmedCopyPublisher extends ProgressiveCopyPublisher
{
    public ConfirmedCopyPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        ConfirmedCopyQueueComponent result = new ConfirmedCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            ConfirmedCopyQueueComponent result = new ConfirmedCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }
    @Override
    public void setPresentQueryCount(int addCount, int count)
    {
        if(count == 0 || addCount == 0)
        {
            ThreadUtil.sleepSec(30);
        }
        
    }

    @Override
    protected String getSqlQuery(int count)
    {
        String query = ViralSMSTableImpl.getSMSTypeLookupQuery(copyType, -1, -1,count,null);
    	return query;
    }

}
