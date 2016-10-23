package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.ReconciliationLogger;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.Publisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;

public class CIDQueueComponent extends QueueComponent
{
	private static Logger logger = Logger.getLogger(CIDQueueComponent.class);
	
    private ViralSMSTableImpl viralSMSTableImpl = null;

    public CIDQueueComponent(ViralSMSTableImpl viralSMSTableImpl)
    {
        super();
        this.viralSMSTableImpl = viralSMSTableImpl;
    }

    public ViralSMSTableImpl getViralSMSTableImpl()
    {
        return viralSMSTableImpl;
    }

    public void setViralSMSTableImpl(ViralSMSTableImpl viralSMSTableImpl)
    {
        this.viralSMSTableImpl = viralSMSTableImpl;
    }

    @Override
    public void execute(QueueContext queueContext)
    {
        try
        {
            logger.info("vst="+getViralSMSTableImpl());
            String circleId = getViralSMSTableImpl().getCircleId();
            boolean circleIdPersisted = circleId != null && !circleId.trim().equals("");
            logger.info("circleIdPersisted="+circleIdPersisted);
            if (!circleIdPersisted)
            {
                circleId = getCircleId();
            }
            boolean added = false;
            QueueContainer siteQueue = queueContext.getQueueContainer().getHunter().getSiteQueueContainer(circleId);
            Publisher sitePublisher = siteQueue.getPublisher();

            if (circleId.equals(Utility.CIRCLE_RESOLVE_NON_RETRY_ERROR))
            {
                Utility.markForManualReconcile(this);
                return;
            }
            else if (circleId.equals(Utility.CIRCLE_RESOLVE_RETRY_ERROR))
            {
                queueContext.getQueueContainer().getHunter().getCidQueue().getPublisher().addQueueComponent(this);
                return;
            }
            else
            {
                if (!sitePublisher.isPublisherActive())
                {
                    if (sitePublisher.canAddMoreQueComponents())
                    {
                        // SiteQueueComponent queueComponent = new SiteQueueComponent(viralSMSTableImpl);
                        sitePublisher.addQueueComponent(this);
                        added = true;
                    }
                    else
                    {
                        sitePublisher.setPublisherActive(true);
                    }
                }
            }
            if (!added && !circleIdPersisted)
            {
                Utility.updateCircleIdForCopyRecord(this, circleId);
            }
        }
        catch(RetryableException e)
        {
        	logger.error("", e);
            ReconciliationLogger.log(viralSMSTableImpl.getSmsId(), e);
            queueContext.getQueueContainer().getPublisher().addQueueComponent(this);
        }
        catch (Throwable e)
        {
        	logger.error("", e);
            ReconciliationLogger.log(viralSMSTableImpl.getSmsId(), e);
            Utility.markForManualReconciliation(viralSMSTableImpl);
        }
    }

    private String getCircleId() throws RetryableException
    {
    	MNPContext mnpContext = new MNPContext(getViralSMSTableImpl().callerID(), "COPY");
		mnpContext.setOnlineDip(Utility.getParamAsBoolean(Utility.COPY_ONLINE_DIP, "FALSE"));
        SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
        String circleId = subscriberDetail.getCircleID();
        logger.info("circleId from service layer="+circleId);
        circleId = Utility.getCopyCircleId(circleId, subscriberDetail.getSubscriberID());
        logger.info("circleId after copy logic="+circleId);
        getViralSMSTableImpl().setCircleId(circleId);
        return circleId;
    }

    @Override
    public void failed(QueueContext queContext, Throwable e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public long getSequenceNo()
    {
        return viralSMSTableImpl.getSmsId();
    }

    @Override
    public String getUniqueName()
    {
        return viralSMSTableImpl.getSmsId() + "";
    }

    @Override
    public String getDisplayName()
    {
        StringBuffer result = new StringBuffer();
        result.append("Caller Id=");
        result.append(viralSMSTableImpl.callerID());
        result.append(", Clip Id= ");
        result.append(viralSMSTableImpl.clipID());
        return result.toString();
    }

    @Override
    public Date getObjectCreationTime()
    {
        return viralSMSTableImpl.sentTime();
    }

}
