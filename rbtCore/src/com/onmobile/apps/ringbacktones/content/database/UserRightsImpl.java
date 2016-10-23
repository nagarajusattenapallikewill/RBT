package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.UserRights;

public class UserRightsImpl extends RBTPrimitive implements UserRights
{
	private static Logger logger = Logger.getLogger(UserRightsImpl.class);
	
    private static final String TABLE_NAME = "RBT_USER_RIGHTS";
    private static final String USER_TYPE_COL = "USER_TYPE";
    private static final String USER_RIGHTS_COL = "USER_RIGHTS";
   
    private String m_type;
    private String m_rights;
	private static String m_databaseType=getDBSelectionString();
    
    private UserRightsImpl(String type, String rights)
	{
	    m_type = type;
	    m_rights = rights;
	}
	
	public String type()
	{
	    return m_type;
	}

	public String rights()
	{
	    return m_rights;
	}
	
	static UserRights insert(Connection conn, String type, String rights)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		UserRightsImpl userRights = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + USER_TYPE_COL;
		query += ", " + USER_RIGHTS_COL;
		query += ")";

		query += " VALUES ( " + "'" + type + "'";
		query += ", " + "'" + rights + "'";
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
            logger.info("RBT::insertion to RBT_USER_RIGHTS table successful");
            userRights = new UserRightsImpl(type, rights);
            return userRights;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_USER_RIGHTS table failed");
            return null;
        }
    }
	
    static boolean update(Connection conn, String type, String rights)   
    {
        logger.info("RBT::inside update");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 USER_RIGHTS_COL + " = " + "'" + rights + "'" + 
				" WHERE " + USER_TYPE_COL + " = " + "'" + type + "'";
		
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
	
  	static UserRights getUserRights(Connection conn, String userType)
    {
  		logger.info("RBT::inside getUserRights");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String type = null;
		String rights = null;
		
		UserRightsImpl userRights = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_TYPE_COL + " = " + "'" + userType + "'";
		
		logger.info("RBT::query "+query);
		
        try
        {
        	logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				type = results.getString(USER_TYPE_COL);
				rights = results.getString(USER_RIGHTS_COL);
		
				userRights = new UserRightsImpl(type, rights);
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
        return userRights;
    }
	
	static boolean remove(Connection conn, String userType)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + USER_TYPE_COL + " = " + "'" + userType + "'";
		
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
	
	static UserRights[] getUserRights(Connection conn) 
    { 
        logger.info("RBT::inside getUserRights"); 
 
        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 
 
        String type = null; 
        String rights = null; 
        
        UserRightsImpl userRights = null; 
        List rightsList = new ArrayList(); 
 
        query = "SELECT * FROM " + TABLE_NAME; 
 
        logger.info("RBT::query "+query); 
 
        try 
        { 
            logger.info("RBT::inside try block"); 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            while (results.next()) 
            { 
            	type = results.getString(USER_TYPE_COL); 
            	rights = results.getString(USER_RIGHTS_COL); 
            	
            	userRights = new UserRightsImpl(type, rights); 
            	rightsList.add(userRights); 
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
        if (rightsList.size() > 0) 
        { 
            logger.info("RBT::retrieving records from RBT_USER_RIGHTS successful"); 
            return (UserRights []) rightsList.toArray(new UserRights[0]); 
        } 
        else 
        { 
            logger.info("RBT::no records in RBT_USER_RIGHTS"); 
            return null; 
        } 
    } 

}