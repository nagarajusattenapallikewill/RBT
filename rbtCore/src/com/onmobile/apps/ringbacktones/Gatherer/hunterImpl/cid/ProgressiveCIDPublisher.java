package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveSqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public class ProgressiveCIDPublisher extends ProgressiveSqlQueryPublisher
{
	private static Logger logger = Logger.getLogger(ProgressiveCIDPublisher.class);
	
    private int workerThreadPrority = 5;
    private String smsType = null;

    public ProgressiveCIDPublisher(String smsType, int workerThreadPrority)
    {
        super();
        this.smsType = smsType;
        this.workerThreadPrority = workerThreadPrority;
    }
    
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
    protected QueueComponent getNextQueueComponent() throws QueryException
    {
        try
        {
            ViralSMSTableImpl viralSMSTableImpl = ViralSMSTableImpl.getNextViralSMS(getRset());
            CIDQueueComponent cidQueueComponent = new CIDQueueComponent(viralSMSTableImpl);
            return cidQueueComponent;
        }
        catch (SQLException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }
    }

    public String getSmsType()
    {
        return smsType;
    }

    @Override
    protected String getSqlQuery(int count)
    {
        String sql = ViralSMSTableImpl.getSMSTypeLookupQuery(smsType,getPresentSequenceId(),-1,count,null);
        return sql;
    }

    @Override
    public int getWorkerThreadPriority()
    {
        return workerThreadPrority;
    }

    public int getWorkerThreadPrority()
    {
        return workerThreadPrority;
    }

    @Override
    public void setPresentQueryCount(int addCount, int count)
    {
        if(addCount == 0)
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
        else if (count == 0)
        {
            ThreadUtil.sleepSec(30);
        }

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

    public void setSmsType(String smsType)
    {
        this.smsType = smsType;
    }

    public void setWorkerThreadPrority(int workerThreadPrority)
    {
        this.workerThreadPrority = workerThreadPrority;
    }
}
