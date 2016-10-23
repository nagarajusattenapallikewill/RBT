package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.expiredCopy;

import java.sql.SQLException;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class ExpiredCopyPublisher extends ProgressiveCopyPublisher
{
    public ExpiredCopyPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }


    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        ExpiredCopyQueueComponent result = new ExpiredCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected String getSqlQuery(int count)
    {
    	String query = "SELECT * FROM RBT_VIRAL_SMS_TABLE WHERE SMS_TYPE='"+copyType+"' AND SMS_ID > "+getPresentSequenceId()+"' AND SMS_SENT_TIME < TIMESTAMPADD(MINUTE,-" + Utility.getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION",30) + ",SYSDATE())" ;
    	return query;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            ExpiredCopyQueueComponent result = new ExpiredCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

}
