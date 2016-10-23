package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;

public class GroupMembersImpl extends RBTPrimitive implements GroupMembers,iRBTConstant
{
	private static Logger logger = Logger.getLogger(GroupMembersImpl.class);
	
    private static final String TABLE_NAME = "RBT_GROUP_MEMBERS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String CALLER_ID_COL = "CALLER_ID";
    private static final String CALLER_NAME_COL = "CALLER_NAME";
    private static final String STATUS_COL = "STATUS";
	
	private int m_groupID;
	private String m_callerID;
	private String m_callerName;
	private String m_status;
	
	public int groupID() {
		
		return m_groupID;
	}

	public String callerID() {
		
		return m_callerID;
	}

	public String callerName() {
		
		return m_callerName;
	}

	public String status() {
		
		return m_status;
	}

	private GroupMembersImpl(int groupID,String callerID,String callerName,String status)
	{
		m_groupID = groupID;
		m_callerID = callerID;
		m_callerName = callerName;
		m_status = status;
	}
	
	static GroupMembers insert(Connection conn, int groupID, String callerID, String callerName,
			String status)
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		GroupMembers groupMembers = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + GROUP_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CALLER_NAME_COL;
		query += ", " + STATUS_COL;
		query += ")";

		query += " VALUES ( " + groupID;
		query += ",'" + callerID + "'";
		query += ", " + sqlString(callerName);
		query += ",'" + status + "'";
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
			logger.info("RBT::insertion to RBT_GROUP_MEMBERS table successful");
			groupMembers = new GroupMembersImpl(groupID, callerID, callerName, status);
			return groupMembers;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_GROUP_MEMBERS table failed");
			return null;
		}
	}
	
	static GroupMembers[] getMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupMembersImpl groupMembers = null;
		ArrayList<GroupMembers> groupMembersList = new ArrayList<GroupMembers>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = " + groupID;
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groupMembers = getGroupMembersFromRS(results);
				groupMembersList.add(groupMembers);
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
		
		if(groupMembersList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groupMembersList.toArray(new GroupMembers[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static GroupMembers getMemberFromGroups(Connection conn, String callerID, int[] groupIDs)
    {
		Statement stmt = null;
		ResultSet results = null;
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " IN (";
		for (int groupID : groupIDs)
		{
			query += groupID + ", ";
		}
		// Trimming of last ", "
		query = query.substring(0, query.length() - 2);
		query += ") AND " + CALLER_ID_COL + " = '" + callerID + "'";
		
		logger.info("RBT::query "+query);
		
		GroupMembersImpl groupMember = null;
		try
        {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
				groupMember = getGroupMembersFromRS(results);
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
		
		return groupMember;
    }
	
	static boolean expireAllGroupMembersForGroup(Connection conn, int groupID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " ='D' WHERE " + GROUP_ID_COL + " = '"
						+ groupID+"'";
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
	
	static boolean deleteGroupMembersOfGroup(Connection conn, int groupID)
    {
        logger.info("RBT::inside deleteGroupMembersOfGroup");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + "=" + groupID; 
		
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
	
	static boolean deleteGroupMembersOfGroups(Connection conn, int[] groupIDs)
    {
		int deleteCount = -1;
		Statement stmt = null;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " IN (";
		for (int groupID : groupIDs)
		{
			query += groupID + ", ";
		}
		// Trimming of last ", "
		query = query.substring(0, query.length() - 2);
		query += ")";
		
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
		return(deleteCount > 0);
    }
	
	static boolean deleteCallerFromGroup(Connection conn, int groupID, String callerId)
    {
		int deleteCount = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = "
				+ groupID + " AND " + CALLER_ID_COL + " = "
				+ sqlString(callerId);
		
		logger.info("RBT::query "+query);

		try
        {
		    logger.info("RBT::inside try block");			
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
		return(deleteCount > 0);
    }
	
	static boolean updateGroupMemberName(Connection conn, int groupID, String callerID, String callerName) {
		String query = "UPDATE " + TABLE_NAME + " SET " + CALLER_NAME_COL + " = " + sqlString(callerName) + " WHERE " + GROUP_ID_COL
				+ " = " + groupID + " AND " + CALLER_ID_COL + " = " + sqlString(callerID);

		logger.info("RBT::query - " + query);

		Statement stmt = null;
		int n = -1;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e) {
			}
		}
		return (n >= 1);
	}
	
	static boolean updateGroupMember(Connection conn, int groupID, String callerID, String callerName,
			String newStatus) {
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlString(newStatus) + ", " + CALLER_NAME_COL + " = " + sqlString(callerName)
				+ " WHERE " + GROUP_ID_COL + " = " + groupID + " AND " + CALLER_ID_COL + " = "
				+ sqlString(callerID);
		
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
		return (n >= 1);
	}
	
	static GroupMembers[] getActiveMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getActiveMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		GroupMembersImpl groupMembers = null;
		ArrayList<GroupMembers> groupMembersList = new ArrayList<GroupMembers>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = " + groupID+" AND "+STATUS_COL+" NOT IN ('D','X')";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groupMembers = getGroupMembersFromRS(results);
				groupMembersList.add(groupMembers);
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
		
		if(groupMembersList.size() > 0)
        {
            logger.info("RBT::retrieving records from "+TABLE_NAME +" successful");
            return groupMembersList.toArray(new GroupMembers[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static boolean removeCallerFromGroup(Connection conn, int groupID, String callerID)
    {
        logger.info("RBT::inside removeCallerFromGroup");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 STATUS_COL + "='D' WHERE " + GROUP_ID_COL + "=" + groupID + " AND " + CALLER_ID_COL + "='" + callerID + "'"; 
		
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
		return(n>0);
    }
	
	static boolean updateGroupMemberStatus(Connection conn, int groupID, String callerID,
			String newStatus) {
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlString(newStatus)
				+ " WHERE " + GROUP_ID_COL + " = " + groupID + " AND " + CALLER_ID_COL + " = "
				+ sqlString(callerID);
		
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
		return (n >= 1);
	}
	
	static boolean updateGroupMembersStatusForGroup(Connection conn, int groupID, String newStatus, String oldStatus) {
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlString(newStatus)
				+ " WHERE " + GROUP_ID_COL + " = " + groupID + " AND " + STATUS_COL + "='"+oldStatus+"'";
		
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
		return (n >= 1);
	}
	
	static ArrayList<GroupMembers> playerGetAddGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<GroupMembers> addGroupMembers = new ArrayList<GroupMembers>();
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				addGroupMembers.add(getGroupMembersFromRS(rs));
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
		
		if(addGroupMembers.size() > 0) {
			logger.info("RBT::found " + addGroupMembers.size() + " records");
			return addGroupMembers;
		}
		else
			logger.info("RBT::No records");
		
		return null;
	}
	
	static ArrayList<GroupMembers> playerGetDelGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_DEACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<GroupMembers> delGroupMembers = new ArrayList<GroupMembers>();
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				delGroupMembers.add(getGroupMembersFromRS(rs));
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
		
		if(delGroupMembers.size() > 0) {
			logger.info("RBT::found " + delGroupMembers.size() + " records");
			return delGroupMembers;
		}
		else
			logger.info("RBT::No records");
		
		return null;
	}
	
	static boolean changeGroupForCaller(Connection conn, String callerID, int groupID, int newGroupID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + GROUP_ID_COL + " = "
				+ newGroupID + " WHERE " + GROUP_ID_COL + " = " + groupID;
		
		if(callerID != null)
			query += " AND " + CALLER_ID_COL + " = '" + callerID + "'";
		
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		int updateCount = -1;
		
		try
		{
			stmt = conn.createStatement();
			updateCount = stmt.executeUpdate(query);
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
			catch(Exception e) {
			}
		}
		return (updateCount >= 1);
	}
	
	private static GroupMembersImpl getGroupMembersFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String callerID = results.getString(CALLER_ID_COL);
		String callerName = results.getString(CALLER_NAME_COL);
		String status = results.getString(STATUS_COL);
		
		return new GroupMembersImpl(groupID, callerID, callerName, status);
	}
}
