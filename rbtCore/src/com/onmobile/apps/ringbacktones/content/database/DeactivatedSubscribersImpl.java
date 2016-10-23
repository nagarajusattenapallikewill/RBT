package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.DeactivatedSubscribers;

public class DeactivatedSubscribersImpl extends RBTPrimitive implements DeactivatedSubscribers
{
	private static Logger logger = Logger.getLogger(DeactivatedSubscribersImpl.class);
	
    private static final String TABLE_NAME = "RBT_DEACTIVATED_SUBSCRIBERS";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String DEACTIVATED_TIME_COL = "DEACTIVATED_TIME";
	private static final String DEACTIVATED_BY_COL = "DEACTIVATED_BY";
	private static final String COS_ID_COL = "COS_ID";

    private String m_subscriberId;
	private Date m_deactivatedTime;
	private String m_deactivatedBy;
	private String m_cosID;
	private static String m_databaseType=getDBSelectionString();

	private DeactivatedSubscribersImpl(String subscriberId, Date deactivatedTime, String deactivatedBy, String cosID)
	{
		m_subscriberId = subscriberId;
		m_deactivatedTime = deactivatedTime;
		m_deactivatedBy = deactivatedBy;
		m_cosID = cosID;
	}
	
	public String subscriberId()
    {
        return m_subscriberId;
    }
	
	public Date deactivatedTime()
	{
		return m_deactivatedTime;
	}
	
	public String deactivatedBy()
	{
		return m_deactivatedBy;
	}
	
	public String cosID()
	{
		return m_cosID;
	}
	
    static boolean insert(Connection conn, String subscriberId, String deactivatedBy, String cosID)
    {
        logger.info("RBT::inside insert");
        
   		String query = null;
   		Statement stmt = null;

		int count = 0;

   		if (m_databaseType.equals(DB_SAPDB)) {
   			query = "INSERT INTO " + TABLE_NAME + "(";
   	   		query += SUBSCRIBER_ID_COL;
   	   		query += ", " + DEACTIVATED_TIME_COL;
   	   		query += ", " + DEACTIVATED_BY_COL;
   	   		query += ", " + COS_ID_COL;
   	   		query += ")";
   	   		
   	   		query += " VALUES(" + sqlString(subscriberId);
   	   		query += ", "+SAPDB_SYSDATE+"";
   	   		query += ", " + sqlString(deactivatedBy);
   	   		query += ", " + sqlString(cosID);
   	   		query += ")";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "INSERT INTO " + TABLE_NAME + "(";
   	   		query += SUBSCRIBER_ID_COL;
   	   		query += ", " + DEACTIVATED_TIME_COL;
   	   		query += ", " + DEACTIVATED_BY_COL;
   	   		query += ", " + COS_ID_COL;
   	   		query += ")";
   	   		
   	   		query += " VALUES(" + sqlString(subscriberId);
   	   		query += ", "+MYSQL_SYSDATE+"";
   	   		query += ", " + sqlString(deactivatedBy);
   	   		query += ", " + sqlString(cosID);
   	   		query += ")";
		}
   		
   		logger.info("RBT::query "+query);
   	
           try
           {
               logger.info("RBT::inside try block "+query);
   			    stmt = conn.createStatement();
   				count = stmt.executeUpdate(query);
           }
           catch(SQLException se)
           {
        	   logger.error("", se);
				return false;
		   }
		   finally
		   {
				try
				{
					stmt.close();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
		   }
		   return (count > 0);  
    }

	static DeactivatedSubscribers getLastDeactivatedDetail(Connection conn, String subscriberId)
    {
        logger.info("RBT::inside getLastDeactivatedDetail");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		DeactivatedSubscribers deactDetail = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberId + "' ORDER BY " + DEACTIVATED_TIME_COL + " DESC";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
				deactDetail = getDeactivatedSubscribersFromRS(results);
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            return null;
        }
		finally
		{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
        return deactDetail;
    }
	
	static DeactivatedSubscribers getLastPromoDeactivatedDetail(Connection conn, String subscriberId, String promoKey)
    {
        logger.info("RBT::inside getPromoDetail");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		DeactivatedSubscribers deactDetail = null;
		
		if(promoKey != null)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberId + "' AND " + DEACTIVATED_BY_COL + " LIKE '%" + promoKey + "%' ORDER BY " + DEACTIVATED_TIME_COL + " DESC";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberId + "' ORDER BY " + DEACTIVATED_TIME_COL + " DESC";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
				deactDetail = getDeactivatedSubscribersFromRS(results);
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            return null;
        }
        catch(Exception e)
        {
        	logger.error("", e);
            return null;
        }
		finally
		{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
        return deactDetail;
    }
	
	public static DeactivatedSubscribers[] getUserCosDeactDetail(Connection conn, String subscriberID, String cosID)
	{
		ArrayList list = new ArrayList();
		
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " +
						sqlString(subscriberID) + " AND " + COS_ID_COL + " = " + sqlString(cosID);
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				list.add(getDeactivatedSubscribersFromRS(rs));
			}
				
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if(stmt != null)
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		
		if(list.size() > 0)
		{
			logger.info("RBT::User " + subscriberID + " has deact detail");
			return (DeactivatedSubscribers[])list.toArray(new DeactivatedSubscribers[0]);
		}
		else
			logger.info("RBT::User " + subscriberID + " has no deact detail");
		
		return null;
	}
	
	private static DeactivatedSubscribers getDeactivatedSubscribersFromRS(ResultSet rs) throws SQLException
	{
		DeactivatedSubscribers deactDetail = null;
		
		if(rs != null)
		{
			String subscriberID = rs.getString(SUBSCRIBER_ID_COL);
			Date deactivatedTime = rs.getDate(DEACTIVATED_TIME_COL);
			String deactivatedBy = rs.getString(DEACTIVATED_BY_COL);
			String cosID = rs.getString(COS_ID_COL);
			
			deactDetail = new DeactivatedSubscribersImpl(subscriberID, deactivatedTime, deactivatedBy, cosID);
		}
		return deactDetail;
	}
}