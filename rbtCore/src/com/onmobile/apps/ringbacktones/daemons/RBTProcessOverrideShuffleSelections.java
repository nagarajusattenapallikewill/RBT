package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;

public class RBTProcessOverrideShuffleSelections extends Thread
{
	private static Logger logger = Logger.getLogger(RBTProcessOverrideShuffleSelections.class);
	
	private RBTDaemonManager m_mainDaemonThread = null; 
	private static RBTDBManager rbtDBManager = null;
	private static RBTCacheManager rbtCacheManager = null;
	ParametersCacheManager rbtParamCacheManager = null;
	
	private String DATABASE_TYPE = "MYSQL";
	long m_nextConfPendingUploadTime = -1;
	private String shuffleDeselectedBy = "VOICE";

	protected RBTProcessOverrideShuffleSelections (RBTDaemonManager mainDaemonThread) 
	{
		try
		{
			setName("RBTProcessOverrideShuffleSelections");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating RBTProcessOverrideShuffleSelections", e);
		}
	}

	public void init()
	{
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		rbtCacheManager = RBTCacheManager.getInstance();
		
		rbtDBManager = RBTDBManager.getInstance();
		DATABASE_TYPE = ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
		
	}
	
	public void run()
	{
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			if(m_nextConfPendingUploadTime == -1 || System.currentTimeMillis() >= m_nextConfPendingUploadTime)
			{
				shuffleDeselectedBy = getParamAsString("DAEMON", "OVERRIDE_SHUFFLE_DESLECTEDBY", "VOICE");
				processOverrideShuffle();
				m_nextConfPendingUploadTime = getnexttime(getParamAsInt("DAEMON", "OVERRIDE_SHUFFLE_DEACTIVATION_HOUR", 1) );
			}
		}
	}
	
	public long getnexttime(int hour)
    {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        long nexttime = now.getTime().getTime();
        if (nexttime < System.currentTimeMillis())
        {
            nexttime = nexttime + (24 * 3600 * 1000);
        }
        return nexttime;
    }
	
	private void processOverrideShuffle()
	{
		Category[] categories = rbtCacheManager.getCategoryByType("10");
		if (categories == null || categories.length == 0)
			return;
		
		List<String> activatedByList = Arrays.asList(getParamAsString("DAEMON", "OVERRIDE_SHUFFLE_ACTIVATEDBY", "FESTIVAL,SMSSEARCH").split(","));
		List<String> categoryIdList = new ArrayList<String>();
		
		for (Category category : categories) 
		{
			Date categoryEndTime = category.getCategoryEndTime();
			Date currentTime = new Date();
			logger.info("categoryID >"+category.getCategoryId()+" & categoryEndTime >"+categoryEndTime);
			int diffDays = (int) ((currentTime.getTime() - categoryEndTime.getTime()) / (1000 * 60 * 60 * 24));
			logger.info("diff days >"+diffDays);
			if (currentTime.after(categoryEndTime) && (diffDays == 0))
				 categoryIdList.add(String.valueOf(category.getCategoryId()));
		}
		
		String categoryIdStr = "";
		for (String categoryId : categoryIdList) 
		{
			categoryIdStr += ",'"+categoryId+"'";
		}
		logger.info("categoryIdStr1 >"+categoryIdStr);
		if(categoryIdStr.equalsIgnoreCase(""))
			return;

		categoryIdStr = categoryIdStr.substring(1);
		logger.info("categoryIdStr >"+categoryIdStr);
		
		List<String> shuffleSubscribers = getActiveOverrideShuffleSubscribers(categoryIdStr);
		if (shuffleSubscribers == null || shuffleSubscribers.size() == 0)
		{
			logger.info(" No active override shuffle subscriber selections");
			return;
		}
		for (String subscriberId : shuffleSubscribers) 
		{
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			
			if (activatedByList.size() > 0)
			{
				if (!activatedByList.contains(subscriber.activatedBy()))
						continue;
			}
			processSubscriber(subscriber, categoryIdStr);
			
		}
	}
	
	private void processSubscriber(Subscriber subscriber, String categoryIds)
	{
		String selQuery  = "SELECT * FROM RBT_SUBSCRIBER_SELECTIONS WHERE SUBSCRIBER_ID = ? AND END_TIME > SYSDATE()";
		if(!DATABASE_TYPE.equalsIgnoreCase("MYSQL"))
			selQuery  = "SELECT * FROM RBT_SUBSCRIBER_SELECTIONS WHERE SUBSCRIBER_ID = ? AND END_TIME > SYSDATE";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		
		try
		{
			conn = getConnection();
			pstmt = conn.prepareStatement(selQuery);
			pstmt.setString(1, subscriber.subID());
			pstmt.execute();
			rs = pstmt.getResultSet();
			
			String deactQuery = null;
			if(rs.next())
			{
				logger.info("active selection exists");
				deactQuery = "UPDATE RBT_SUBSCRIBER_SELECTIONS SET SEL_STATUS='D', DESELECTED_BY=?, NEXT_CHARGING_DATE = TIMESTAMP('2037-12-31') WHERE SUBSCRIBER_ID=? AND CATEGORY_ID IN ("+categoryIds+") AND SEL_STATUS IN ('A','W','N','B')";
				if(!DATABASE_TYPE.equalsIgnoreCase("MYSQL"))
					deactQuery = "UPDATE RBT_SUBSCRIBER_SELECTIONS SET SEL_STATUS='D', DESELECTED_BY=?, NEXT_CHARGING_DATE = TO_DATE('20371231','yyyyMMdd') WHERE SUBSCRIBER_ID=? AND CATEGORY_ID IN ("+categoryIds+") AND SEL_STATUS IN ('A','W','N','B')";
				
				pstmt1 = conn.prepareStatement(deactQuery);
				pstmt1.setString(1, shuffleDeselectedBy);
				pstmt1.setString(2, subscriber.subID());
				pstmt1.executeUpdate();
				
			}
			else
			{
				logger.info("active selections doesn't exists");
				deactQuery = "UPDATE RBT_SUBSCRIBER SET SUBSCRIPTION_YES='D', DEACTIVATED_BY=?, END_DATE=SYSDATE(), NEXT_CHARGING_DATE = TIMESTAMP('2037-12-31')  WHERE SUBSCRIBER_ID=? AND END_DATE > SYSDATE()";
				if(!DATABASE_TYPE.equalsIgnoreCase("MYSQL"))
					deactQuery = "UPDATE RBT_SUBSCRIBER SET SUBSCRIPTION_YES='D', DEACTIVATED_BY=?, END_DATE=SYSDATE , NEXT_CHARGING_DATE = TO_DATE('20371231','yyyyMMdd') WHERE SUBSCRIBER_ID=? AND END_DATE > SYSDATE";
				
				pstmt1 = conn.prepareStatement(deactQuery);
				pstmt1.setString(1, shuffleDeselectedBy);
				pstmt1.setString(2, subscriber.subID());
				pstmt1.executeUpdate();
			}
			
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn, pstmt, null);
			try
			{
				if(pstmt1 != null)
					pstmt1.close();
			}
			catch(Throwable e)
			{
				logger.error("Exception in closing db statement", e);	
			}
		}
	}
	
	private List<String> getActiveOverrideShuffleSubscribers(String categoryIds)
	{
	  String sql = "SELECT DISTINCT(SUBSCRIBER_ID) FROM RBT_SUBSCRIBER_SELECTIONS WHERE CATEGORY_TYPE='10' AND CATEGORY_ID IN ("+categoryIds+")";
	  Connection conn = null;
	  Statement stmt = null;
	  ResultSet rs = null;
	  List<String> list = new ArrayList<String>();
	  try
	  {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
				list.add(rs.getString("SUBSCRIBER_ID"));
		}
	  	catch(Throwable e)
		{
	  		logger.error("Exception before release connection", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		logger.info("size >"+list.size());
		return list;	
	}
		
	private Connection getConnection()
	{
		return rbtDBManager.getConnection();
	}
	
	private static boolean releaseConnection(Connection conn, Statement stmt, ResultSet rs)
	{
		try
		{
			if(rs != null)
				rs.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing resultSet", e);
		}
		
		try
		{
			if(stmt != null)
				stmt.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing statement", e);
		}
		
		return RBTDBManager.getInstance().releaseConnection(conn);
	}
	
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return rbtParamCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	
	 public int getParamAsInt(String type, String param, int defaultVal)
	    {
	    	try{
	    		String paramVal = rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
	    		return Integer.valueOf(paramVal);   		
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param );
	    		return defaultVal;
	    	}
	    }
	
}
