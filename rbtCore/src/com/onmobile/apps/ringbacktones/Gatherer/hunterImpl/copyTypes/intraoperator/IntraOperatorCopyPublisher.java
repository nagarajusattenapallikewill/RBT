package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.intraoperator;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;

public class IntraOperatorCopyPublisher extends ProgressiveCopyPublisher
{
	private static Logger logger = Logger.getLogger(IntraOperatorCopyPublisher.class);
	
    public IntraOperatorCopyPublisher(String circleId, String copyType)
    {
    	super(circleId, copyType);
    }

    @Override
    protected SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent)
    {
        IntraOperatorCopyQueueComponent result = new IntraOperatorCopyQueueComponent(cidQueueComponent.getViralSMSTableImpl(),getLinkedSubsriberLookup());
        return result;
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            IntraOperatorCopyQueueComponent result = new IntraOperatorCopyQueueComponent(viralSMSTableImpl,getLinkedSubsriberLookup());
            return result;
        }
        catch (SQLException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }
    }

}
