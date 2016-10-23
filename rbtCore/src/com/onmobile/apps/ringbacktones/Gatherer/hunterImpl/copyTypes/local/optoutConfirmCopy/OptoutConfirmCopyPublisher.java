package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optoutConfirmCopy;

import java.sql.SQLException;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class OptoutConfirmCopyPublisher extends ProgressiveCopyPublisher
{
    public OptoutConfirmCopyPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        OptoutConfirmCopyQueueComponent result = new OptoutConfirmCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            OptoutConfirmCopyQueueComponent result = new OptoutConfirmCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

    @Override
    protected String getSqlQuery(int count)
    {
        String query = ViralSMSTableImpl.getSMSTypeLookupQueryForOptOut(copyType,getPresentSequenceId(),count,null);
    	return query;
    }

    @Override
    public void setPresentQueryCount(int addCount, int count)
    {
        if(count == 0 || addCount == 0)
        {
            ThreadUtil.sleepSec(30);// TODO read from config
        }

    }

}
