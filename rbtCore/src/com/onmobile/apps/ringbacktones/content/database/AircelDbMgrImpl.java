package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;

public class AircelDbMgrImpl extends RBTDBManager{

	private static Logger logger = Logger.getLogger(AircelDbMgrImpl.class);
	
	public void init()
	{
		logger.info("inside init method");
	}
	
	@Override
	public boolean convertSubscriptionType(String subscriberID, String initType, String finalType, String strActBy, String strActInfo, 
			boolean concatActInfo, int rbtType, boolean useRbtType, String extraInfo, Subscriber subscriber)
	{
		if(initType == null || finalType == null) 
			return false; 
	
		Connection conn = getConnection();
		if (conn == null)
			return false;
		
		boolean success = false;
		try
		{
			success = SubscriberImpl.convertSubscriptionType(conn, subID(subscriberID), initType, finalType, strActBy, strActInfo, 
					concatActInfo, rbtType, useRbtType, extraInfo, subscriber);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		return success;
	}

}

