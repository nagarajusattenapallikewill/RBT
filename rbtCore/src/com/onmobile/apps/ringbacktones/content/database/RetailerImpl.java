package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Retailer;

public class RetailerImpl extends RBTPrimitive implements Retailer
{
	private static Logger logger = Logger.getLogger(RetailerImpl.class);
	
    private static final String TABLE_NAME = "RBT_RETAILER_TABLE";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String TYPE_COL = "TYPE";
    private static final String NAME_COL = "NAME";
    
    private String m_subscriberID;
    private String m_type;
    private String m_name;
    private static String m_databaseType=getDBSelectionString();
    
    private RetailerImpl(String subscriberID, String type, String name)
    {
        m_subscriberID = subscriberID;
        m_type = type;
        m_name = name;
    }
	
    public String subID()
    {
        return m_subscriberID;
    }
	
    public String type()
    {
        return m_type;
    }
    
    public String name()
    {
    	return m_name;
    }
    
    static Retailer insert(Connection conn, String subscriberID, String type, String name)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;
		
		RetailerImpl retailer = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + TYPE_COL;
		query += ", " + NAME_COL;
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		query += ", " + sqlString(type);
		query += ", " + sqlString(name);
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
            logger.info("RBT::insertion to RBT_RETAILER_TABLE table successful");
            retailer = new RetailerImpl(subscriberID, type, name);
            return retailer;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_RETAILER_TABLE table failed");
            return null;
        }
    }

    static Retailer getRetailer(Connection conn, String subID)
    {
        logger.info("RBT::inside getRetailer");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		String type = null;
		String name = null;
		
		RetailerImpl retailer = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subID);
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						subscriberID = results.getString(SUBSCRIBER_ID_COL);
						type = results.getString(TYPE_COL);
						name = results.getString(NAME_COL);

						retailer = new RetailerImpl(subscriberID, type, name);
					}
		}
        catch(SQLException se)
        {
            logger.error("", se);
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
        return retailer;
    }
    
    static Retailer [] getRetailers(Connection conn)
    {
        logger.info("RBT::inside getRetailers");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		String type = null;
		String name = null;
		
		RetailerImpl retailer = null;
		List retailerList = new ArrayList();

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
						type = results.getString(TYPE_COL);
						name = results.getString(NAME_COL);

						retailer = new RetailerImpl(subscriberID, type, name);
						retailerList.add(retailer);
					}
		}
        catch(SQLException se)
        {
            logger.error("", se);
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
		if(retailerList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_RETAILER_TABLE successful");
            return (Retailer[])retailerList.toArray(new Retailer[0]);
        } 
		else
        {
			logger.info("RBT::no records in RBT_RETAILER_TABLE");
            return null;
        }
    }
    
    static boolean remove(Connection conn, String subscriberID, String type)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + TYPE_COL + " = " + sqlString(type);
		
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