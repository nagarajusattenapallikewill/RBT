package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.PreDefinedGroups;

public class PreDefinedGroupsImpl extends RBTPrimitive implements PreDefinedGroups
{
	private static Logger logger = Logger.getLogger(PreDefinedGroupsImpl.class);
	
	private static final String TABLE_NAME = "RBT_PREDEFINED_GROUPS";
	private static final String PRE_GROUP_ID_COL = "PRE_GROUP_ID";
    private static final String PRE_GROUP_NAME_COL = "PRE_GROUP_NAME";
	
	private String m_preGroupID;
	private String m_preGroupName;
	
	public String preGroupID() {
		
		return m_preGroupID;
	}

	public String preGroupName() {
		
		return m_preGroupName;
	}

	private PreDefinedGroupsImpl(String preGroupID,String preGroupName)
	{
		m_preGroupID = preGroupID;
		m_preGroupName = preGroupName;
	}
	
	static PreDefinedGroups insert(Connection conn, String preGroupID, String preGroupName)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		PreDefinedGroupsImpl preDefinedGroups = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + PRE_GROUP_ID_COL;
		query += ", " + PRE_GROUP_NAME_COL;
		query += ")";

		query += " VALUES ( " + sqlString(preGroupID);
		query += ", " + sqlString(preGroupName);
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
            logger.info("RBT::insertion to RBT_PREDEFINED_GROUPS table successful");
            preDefinedGroups = new PreDefinedGroupsImpl(preGroupID, preGroupName);
            return preDefinedGroups;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_PREDEFINED_GROUPS table failed");
            return null;
        }
    }
	
	
	static PreDefinedGroups[] getPredefinedGroups(Connection conn)
    {
        logger.info("RBT::inside getPredefinedGroups");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		PreDefinedGroupsImpl preDefinedGroups = null;
		ArrayList preDefinedGroupsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME;
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				preDefinedGroups = getPreDefinedGroupsFromRS(results);
				preDefinedGroupsList.add(preDefinedGroups);
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
		
		if(preDefinedGroupsList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return (PreDefinedGroups[])preDefinedGroupsList.toArray(new PreDefinedGroups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	private static PreDefinedGroupsImpl getPreDefinedGroupsFromRS(ResultSet results) throws SQLException {
		String preGroupID = results.getString(PRE_GROUP_ID_COL);
		String preGroupName = results.getString(PRE_GROUP_NAME_COL);
		
		return new PreDefinedGroupsImpl(preGroupID, preGroupName);
	}
}
