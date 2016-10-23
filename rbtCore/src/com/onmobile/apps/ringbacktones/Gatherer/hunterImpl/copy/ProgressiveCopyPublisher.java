package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.LinkedSubsriberLookup;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.CIDQueueComponent;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveSqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public abstract class ProgressiveCopyPublisher extends ProgressiveSqlQueryPublisher
{
	private static Logger logger = Logger.getLogger(ProgressiveCopyPublisher.class);
	
    private LinkedSubsriberLookup linkedSubsriberLookup = new LinkedSubsriberLookup();

    public LinkedSubsriberLookup getLinkedSubsriberLookup()
    {
        return linkedSubsriberLookup;
    }

    public  String circleId = null;
    public String copyType = null;
    private boolean raceAround = false;
        
    public ProgressiveCopyPublisher(String circleId, String copyType)
    {
    	this.circleId = circleId;
    	this.copyType = copyType;
    }

    @Override
    public boolean addQueueComponent(QueueComponent queComponent)
    {
        SiteQueueComponent copyQueueComponent = null;
        if (queComponent instanceof CIDQueueComponent)
        {
            CIDQueueComponent cidQueueComponent = (CIDQueueComponent) queComponent;
            copyQueueComponent = getCopyQueueComponent(cidQueueComponent);
        }
        else if (queComponent instanceof SiteQueueComponent)
        {
            copyQueueComponent = (SiteQueueComponent) queComponent;
        }
        
        if (linkedSubsriberLookup.add(copyQueueComponent))
        {
            return super.addQueueComponent(copyQueueComponent);
        }
        return true;
    }

    protected abstract  SiteQueueComponent getCopyQueueComponent(CIDQueueComponent cidQueueComponent);

    @Override
    protected Connection getConnection() throws QueryException
    {
        try
        {
            return OnMobileDBServices.getDBConnection();
        }
        catch (OnMobileException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }
    }

    @Override
    protected String getSqlQuery(int count)
    {
        String query = ViralSMSTableImpl.getSMSTypeLookupQuery(copyType,getPresentSequenceId(),getQueueContainer().getOldestSequenceId(), count,circleId);
    	return query;
    }

    @Override
    protected void releaseConnection(Connection connection) throws RBTException
    {
        try
        {
            OnMobileDBServices.releaseConnection(connection);
        }
        catch (OnMobileException e)
        {
        	logger.error("", e);
            throw new RBTException(e.getMessage());
        }

    }

    @Override
    public void setPresentQueryCount(int addCount, int count)
    {
    	if(raceAround)
    	{
    		return;
    	}
        if(addCount !=0 && count == 0)
        {
            boolean done = false;
        	try
        	{
        		logger.info("RBT:: addCount: " + addCount
								+ ", count: " + count + ", QueueCount: "
								+ getQueueContainer().getQueueSize());
        		setPublisherActive(false);
        		raceAround = true;
        		fetchNextCycle(true);
        		done = true;
        	}
        	finally
        	{
        		raceAround = false;
        		if(!done)
        		{
        		    setPublisherActive(true);
        		}
        	}
        }
        else if(addCount == 0)
        {
            try
            {
                /* 
                 * The QueueContainer will notify you once it its the lower water mark.
                 */
                waitOnThread();
            }
            catch (InterruptedException e)
            {
            	logger.error("", e);
            }
        }
    }

    @Override
    public int getWorkerThreadPriority()
    {
        return 5;
    }

}
