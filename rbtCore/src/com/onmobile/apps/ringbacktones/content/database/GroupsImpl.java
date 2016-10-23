package com.onmobile.apps.ringbacktones.content.database;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Groups;

public class GroupsImpl extends RBTPrimitive implements Groups, iRBTConstant
{
	private static Logger logger = Logger.getLogger(GroupsImpl.class);
	
    private static final String TABLE_NAME = "RBT_GROUPS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String PRE_GROUP_ID_COL = "PRE_GROUP_ID";
    private static final String GROUP_NAME_COL = "GROUP_NAME";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String GROUP_PROMO_ID_COL = "GROUP_PROMO_ID";
    private static final String STATUS_COL = "STATUS";
    
	private int m_groupID;
	private String m_preGroupID;
	private String m_subscriberID;
	private String m_groupName;
	private String m_groupPromoID;
	private String m_status;
	
	public int groupID() {
		
		return m_groupID;
	}

	public String preGroupID() {
		
		return m_preGroupID;
	}

	public String groupName() {
		
		return m_groupName;
	}

	public String subID() {
		
		return m_subscriberID;
	}

	public String groupPromoID() {
		
		return m_groupPromoID;
	}

	public String status() {
		
		return m_status;
	}
	
	private GroupsImpl(int groupID,String preGroupID,String groupName,
						String subscriberID,String groupPromoID,String status)
	{
		m_groupID = groupID;
		m_preGroupID = preGroupID;
		m_groupName = groupName;
		m_subscriberID = subscriberID;
		m_groupPromoID = groupPromoID;
		m_status = status;
		
	}
	
	static boolean insert(Connection conn, String preGroupID, String groupName,
							String subscriberID, String groupPromoID, String status)
    {
        logger.info("RBT::inside insert");
        
		int id = -1;
		String query = null;
		PreparedStatement pstmt = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + GROUP_NAME_COL;
		query += ", " + PRE_GROUP_ID_COL;
		query += ", " + GROUP_PROMO_ID_COL;
		query += ", " + STATUS_COL;
		query += ")";

		query += " VALUES (?, ?, ?, ?, ?)";
		
		byte[] groupNameBytes = null;
		try {
			groupNameBytes = groupName.getBytes("UTF-8");
		} 
		catch (UnsupportedEncodingException e1) {
			groupNameBytes = groupName.getBytes();
		}
		
        try
        {
            logger.info("RBT::inside try block");
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, subscriberID);
            pstmt.setBytes(2, groupNameBytes);
            pstmt.setString(3, preGroupID);
            pstmt.setString(4, groupPromoID);
            pstmt.setString(5, status);
            
            logger.info("RBT::query " + pstmt.toString());
			if (pstmt.executeUpdate() > 0)
				id = 0;
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
				pstmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
        if (id == 0)
        {
            logger.info("RBT::insertion to RBT_GROUPS table successful");
            return true;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_GROUPS table failed");
            return false;
        }
    }
	
	static boolean insert(Connection conn, String groupId, String preGroupID,
			String groupName, String subscriberID, String groupPromoID,
			String status) {
		logger.info("Adding groupName: "+groupName);

		int id = -1;
		String query = null;
		PreparedStatement pstmt = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + GROUP_ID_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + GROUP_NAME_COL;
		query += ", " + PRE_GROUP_ID_COL;
		query += ", " + GROUP_PROMO_ID_COL;
		query += ", " + STATUS_COL;
		query += ")";

		query += " VALUES (?, ?, ?, ?, ?, ?)";

		byte[] groupNameBytes = null;
		try {
			groupNameBytes = groupName.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			groupNameBytes = groupName.getBytes();
		}

		try {
			logger.info("RBT::inside try block");
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, groupId);
			pstmt.setString(2, subscriberID);
			pstmt.setBytes(3, groupNameBytes);
			pstmt.setString(4, preGroupID);
			pstmt.setString(5, groupPromoID);
			pstmt.setString(6, status);

			logger.info("RBT::query " + pstmt.toString());
			if (pstmt.executeUpdate() > 0)
				id = 0;
		} catch (SQLException se) {
			logger.error("", se);
			return false;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (id == 0) {
			logger.info("RBT::insertion to RBT_GROUPS table successful");
			return true;
		} else {
			logger.info("RBT::insertion to RBT_GROUPS table failed");
			return false;
		}
	}
	
	static Groups[] getPredefinedGroupsAddedForSubscriber(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getPredefinedGroupsAddedForSubscriber");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupsImpl groups = null;
		ArrayList<Groups> groupsList = new ArrayList<Groups>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "' AND "+PRE_GROUP_ID_COL+" IS NOT NULL AND "
				+STATUS_COL+" NOT IN ('D','X')";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupsFromRS(results);
				groupsList.add(groups);
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
		
		if(groupsList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groupsList.toArray(new Groups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static Groups getGroupByPreGroupID(Connection conn, String preGroupID, String subscriberID)
    {
        logger.info("RBT::inside getGroupByPreGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupsImpl groups = null;
		

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " +PRE_GROUP_ID_COL+"='"+preGroupID+"'"; 
		
		if (null != subscriberID) {
			query = query + " AND " + SUBSCRIBER_ID_COL + "='" + subscriberID
					+ "'";
		}
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupsFromRS(results);
				
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
		
		if(groups != null)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groups;
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static Groups getActiveGroupByGroupName(Connection conn, String groupName, String subscriberID)
    {
        logger.info("RBT::inside getActiveGroupByGroupName");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String groupNam = null;
		GroupsImpl groups = null;
		if(groupName != null)
		{
			groupNam = groupName.toLowerCase();
		}

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "='"+subscriberID+"' AND LOWER("+GROUP_NAME_COL+")='"
					+groupNam+"' AND "+ STATUS_COL + " NOT IN ('D','X')"; 
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupsFromRS(results);
				
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
		
		if(groups != null)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groups;
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static Groups[] getGroupsForSubscriberID(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getGroupsForSubscriberID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupsImpl groups = null;
		ArrayList<Groups> groupsList = new ArrayList<Groups>();

		if(subscriberID != null)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " =" + sqlString(subscriberID);
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " IS NULL ";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupsFromRS(results);
				groupsList.add(groups);
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
		
		if(groupsList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groupsList.toArray(new Groups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static Groups[] getActiveGroupsForSubscriberID(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getActiveGroupsForSubscriberID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupsImpl groups = null;
		ArrayList<Groups> groupsList = new ArrayList<Groups>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID+"' AND "+STATUS_COL+" NOT IN ('D','X')";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupsFromRS(results);
				groupsList.add(groups);
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
		
		if(groupsList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groupsList.toArray(new Groups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static boolean deactivateGroup(Connection conn, int groupID)
    {
        logger.info("RBT::inside deleteGroup");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 STATUS_COL + "='D' WHERE " + GROUP_ID_COL + "=" + groupID; 
		
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
	
	static boolean deleteGroup(Connection conn, int groupID)
    {
		int deleteCount = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = " + groupID;
		logger.info("RBT::query "+query);

		try
        {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			deleteCount = stmt.getUpdateCount();
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
		return(deleteCount == 1);
    }
	
	static boolean updateSubscriberId(Connection conn, String newSubscriberId, String subscriberId)
	{
		logger.info("RBT::inside updateSubscriberId");

		String query = null;
		Statement stmt = null;
		int n = -1;

		query = "UPDATE " + TABLE_NAME + " SET " +
		SUBSCRIBER_ID_COL + " = '" + newSubscriberId +
		"' WHERE " + SUBSCRIBER_ID_COL  + " = '" + subscriberId + "'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");			
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
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
		return (n>0);
	}
	
	static boolean deleteGroupsOfSubscriber(Connection conn, String subID)
    {
        logger.info("RBT::inside deleteGroupsOfSubscriber");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + "='" + subID+"'"; 
		
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
		return(n > 0);
    }
	
    static boolean updateGroupNameForGroupID(Connection conn,int groupID,String groupName,String subscriberID){
    	
    	String query ="UPDATE " + TABLE_NAME + " SET " + GROUP_NAME_COL + " = " + sqlString(groupName) 
    	              + " WHERE " + GROUP_ID_COL + " = " + groupID;
    	if(null != subscriberID) {
			query = query + " AND " + SUBSCRIBER_ID_COL + " = " + subscriberID;
    	}
    	logger.info("RBT::query - " + query);
    	Statement stmt = null;
    	int n = -1;
    	
    	try {
				stmt = conn.createStatement();
				n = stmt.executeUpdate(query);
			} catch (SQLException e) {
				logger.error("", e);
			}
    		finally{
    			try {
    				if(stmt != null)
    					stmt.close();
    			}
    			catch(Exception e) {
    			}

    		}
             return (n>0);    	
    }
    
	static boolean updateGroupStatus(Connection conn, int groupID, String newStatus,
			String groupPromoID) {
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlString(newStatus);
		
		if(groupPromoID != null)
			query += ", " + GROUP_PROMO_ID_COL + " = " + sqlString(groupPromoID);
		query += " WHERE " + GROUP_ID_COL + " = " + groupID;
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		int n = -1;
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		
		return (n > 0);
	}
	
	static boolean updateAllGroupsStatusForSubscriber(Connection conn, String subscriberID, String newStatus, String oldStatus)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlString(newStatus)+" WHERE " + SUBSCRIBER_ID_COL + " = '"
						+ subscriberID+"' AND "+STATUS_COL+"='"+oldStatus+"'";
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		int n = -1;
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		
		return (n > 0);
	}
	
	static boolean expireAllGroupsForSubscriber(Connection conn, String subscriberID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " ='D' WHERE " + SUBSCRIBER_ID_COL + " = '"
						+ subscriberID+"'";
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		int n = -1;
		
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		
		return (n > 0);
	}
	
	static Groups getGroup(Connection conn, int groupID) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = " + groupID;
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		Groups group = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next())
				group = getGroupsFromRS(rs);
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		return group;
	}
	
	static ArrayList<Groups> playerGetAddGroups(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Groups> addGroups = new ArrayList<Groups>();
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				addGroups.add(getGroupsFromRS(rs));
			}
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		
		if(addGroups.size() > 0) {
			logger.info("RBT::found " + addGroups.size() + " records");
			return addGroups;
		}
		else
			logger.info("RBT::No records");
		
		return null;
	}
	
	static ArrayList<Groups> playerGetDelGroups(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_DEACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Groups> delGroups = new ArrayList<Groups>();
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				delGroups.add(getGroupsFromRS(rs));
			}
		}
		catch(SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
			}
		}
		
		if(delGroups.size() > 0) {
			logger.info("RBT::found " + delGroups.size() + " records");
			return delGroups;
		}
		else
			logger.info("RBT::No records");
		
		return null;
	}
	
	private static GroupsImpl getGroupsFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String preGroupID = results.getString(PRE_GROUP_ID_COL);
		String groupName = results.getString(GROUP_NAME_COL);
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String groupPromoID = results.getString(GROUP_PROMO_ID_COL);
		String status = results.getString(STATUS_COL);
		
		return new GroupsImpl(groupID, preGroupID, groupName, subscriberID, groupPromoID,
				status);
	}
}
