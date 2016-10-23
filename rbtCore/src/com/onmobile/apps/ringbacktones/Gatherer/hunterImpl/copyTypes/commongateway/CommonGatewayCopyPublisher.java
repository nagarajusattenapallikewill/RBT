package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.commongateway;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class CommonGatewayCopyPublisher extends ProgressiveCopyPublisher
{
	private static Logger logger = Logger.getLogger(CommonGatewayCopyPublisher.class);
	
    public CommonGatewayCopyPublisher(String circleId, String copyType)
    {
        super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        CommonGatewayCopyQueueComponent result = new CommonGatewayCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(), getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws QueryException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            CommonGatewayCopyQueueComponent result = new CommonGatewayCopyQueueComponent(viralSMSTableImpl, getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }

    }

}
