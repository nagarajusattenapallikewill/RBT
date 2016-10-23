package com.onmobile.apps.ringbacktones.daemons.callbackEvents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveSqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public class ProgressiveCallbackEventsPublisher extends ProgressiveSqlQueryPublisher 
{
	private static Logger logger = Logger.getLogger(ProgressiveCallbackEventsPublisher.class);

	public ProgressiveCallbackEventsPublisher()
	{
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
	protected String getSqlQuery(int count) 
	{
		String query = "SELECT * FROM RBT_CALL_BACK_EVENT WHERE MODULE_ID="+CallbackUtils.Modules.RTCOPY.moduleID+" and sequence_id >"+getPresentSequenceId()+" limit "+count;
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
	protected QueueComponent getNextQueueComponent() throws HunterException 
	{
		RBTCallBackEvent rbtCallBackEvent = getEventFromResultSet(getRset());
		return new RBTCallbackEventQueueComponent(rbtCallBackEvent);
	}


	@Override
	public int getWorkerThreadPriority()
	{
		return 5;
	}
	
	@Override
	protected void setPresentQueryCount(int addCount, int count) 
	{
		if(count == 0)
		{
			ThreadUtil.sleepSec(30);// TODO read from config
		}
	}
	
	private RBTCallBackEvent getEventFromResultSet(ResultSet results)
	{
		
		RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent();
		try 
		{
			rbtCallBackEvent.setSequenceID(results.getLong("SEQUENCE_ID"));
			rbtCallBackEvent.setSubscriberID(results.getString("SUBSCRIBER_ID"));
			rbtCallBackEvent.setModuleID(results.getInt("MODULE_ID"));
			rbtCallBackEvent.setEventType(results.getInt("EVENT_TYPE"));
			rbtCallBackEvent.setClipID(results.getInt("CLIP_ID"));
			rbtCallBackEvent.setClassType(results.getString("CLASS_TYPE"));
			rbtCallBackEvent.setSelectedBy(results.getString("SELECTED_BY"));
			rbtCallBackEvent.setSelectionInfo(results.getString("SELECTION_INFO"));
			rbtCallBackEvent.setMessage(results.getString("MESSAGE"));
		}
		catch (SQLException e) 
		{
			logger.error("", e);
		}
		return rbtCallBackEvent;
	}
}
