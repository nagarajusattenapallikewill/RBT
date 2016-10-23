package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.interoperator;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class InterOperatorCopyPublisher extends ProgressiveCopyPublisher
{
	private static Logger logger = Logger.getLogger(InterOperatorCopyPublisher.class);
	
    public InterOperatorCopyPublisher(String circleId, String copyType)
    {
        super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        InterOperatorCopyQueueComponent result = new InterOperatorCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(), getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws QueryException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            InterOperatorCopyQueueComponent result = new InterOperatorCopyQueueComponent(viralSMSTableImpl, getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }

    }

}
