package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberCharging;

public class SubscriberChargingImpl extends RBTPrimitive implements SubscriberCharging
{
	private static Logger logger = Logger.getLogger(SubscriberChargingImpl.class);
	
    private static final String TABLE_NAME = "RBT_SUBSCRIBER_CHARGING_CLASS";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String MAX_SELECTIONS_COL = "MAX_SELECTIONS";
   
    private String m_subscriberID;
	private String m_classType;
	private int m_maxSelections;
	private static String m_databaseType=getDBSelectionString();

	private SubscriberChargingImpl(String subscriberID, String classType, int maxSelections)
	{
		m_subscriberID = subscriberID;
		m_classType = classType;
		m_maxSelections = maxSelections;
	}

	public String subID()
	{
		return m_subscriberID;
	}
	
	public String classType()
	{
		return m_classType;
	}

	public int maxSelections()
	{
		return m_maxSelections;
	}

	public void setMaxSelections(Connection conn, String subscriberID, String classType, int maxSelections)
	{
        logger.info("RBT::inside setMaxSelections");
        
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 MAX_SELECTIONS_COL + " = " + maxSelections +  
				" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + CLASS_TYPE_COL + "= '" + classType + "'";
		
		logger.info("RBT::query "+query);

		try
        {
            logger.info("RBT::inside try block");			
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
        }
        catch(SQLException se)
        {
            logger.error("", se);
            return;
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
		return;
    }

	static SubscriberCharging insert(Connection conn, String subscriberID, String classType, int maxSelections)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		SubscriberChargingImpl subscriberCharging = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + MAX_SELECTIONS_COL;
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		query += ", " + sqlString(classType);
		query += ", " + maxSelections;
		query += ")";
		
		logger.info("RBT::query " +query);
		
        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
				if (stmt.executeUpdate(query) > 0)
					id = 0;
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
        if(id == 0)
        {
            logger.info("RBT::insertion to RBT_SUBSCRIBER_CHARGING_CLASS table successful");
            subscriberCharging = new SubscriberChargingImpl(subscriberID, classType, maxSelections);
            return subscriberCharging;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_SUBSCRIBER_CHARGING_CLASS table failed");
            return null;
        }
    }
	
    static boolean update(Connection conn, String subscriberID, String classType, int maxSelections)
    {
        logger.info("RBT::inside update");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 MAX_SELECTIONS_COL + " = " + maxSelections +
				" WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + CLASS_TYPE_COL + " = " + sqlString(classType);
		
		logger.info("RBT::query "+query);

		try
        {
		    logger.info("RBT::inside try block");			
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
    }

	static SubscriberCharging [] getSubscriberCharging(Connection conn, String subID)
    {
        logger.info("RBT::inside getSubscriberCharging");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		String classType = null;
		int maxSelections = -1;
		
		SubscriberChargingImpl subscriberCharging = null;
		List subscriberChargingList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						subscriberID = results.getString(SUBSCRIBER_ID_COL);
						classType = results.getString(CLASS_TYPE_COL);
						maxSelections = results.getInt(MAX_SELECTIONS_COL);
		
						subscriberCharging = new SubscriberChargingImpl(subscriberID, classType, maxSelections);
						subscriberChargingList.add(subscriberCharging);
					}
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
        if(subscriberChargingList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_SUBSCRIBER_CHARGING_CLASS successful");
            return (SubscriberCharging[])subscriberChargingList.toArray(new SubscriberCharging[0]);
        } 
		else
        {
            logger.info("RBT::no records in RBT_SUBSCRIBER_CHARGING_CLASS");
            return null;
        }
    }
	
	static SubscriberCharging [] getAllCharging(Connection conn)
    {
        logger.info("RBT::inside getAllCharging");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		String classType = null;
		int maxSelections = -1;
		
		SubscriberChargingImpl subscriberCharging = null;
		List subscriberChargingList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME;
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						subscriberID = results.getString(SUBSCRIBER_ID_COL);
						classType = results.getString(CLASS_TYPE_COL);
						maxSelections = results.getInt(MAX_SELECTIONS_COL);
		
						subscriberCharging = new SubscriberChargingImpl(subscriberID, classType, maxSelections);
						subscriberChargingList.add(subscriberCharging);
					}
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
        if(subscriberChargingList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_SUBSCRIBER_CHARGING_CLASS successful");
            return (SubscriberCharging[])subscriberChargingList.toArray(new SubscriberCharging[0]);
        } 
		else
        {
            logger.info("RBT::no records in RBT_SUBSCRIBER_CHARGING_CLASS");
            return null;
        } 
    }

	static boolean remove(Connection conn, String subscriberID)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		
		logger.info("RBT::query "+query);
		
		try
		{
		    logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}

	static boolean removeType(Connection conn, String subscriberID, String classType)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + CLASS_TYPE_COL + " = " +sqlString(classType);
		
		logger.info("RBT::query "+query);
		
		try
		{
		    logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
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
		return(n==1);
	}
}