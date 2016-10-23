package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RbtTempGroupMembers;

public class RbtTempGroupMembersImpl extends RBTPrimitive implements RbtTempGroupMembers,iRBTConstant
{
	private static Logger logger = Logger.getLogger(GroupMembersImpl.class);
	
    private static final String TABLE_NAME = "RBT_TEMP_GROUP_MEMBERS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String CALLER_ID_COL = "CALLER_ID";
    private static final String CALLER_NAME_COL = "CALLER_NAME";
    private static final String STATUS_COL = "STATUS";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String GROUP_MEMBER_STATUS_COL = "GROUP_MEMBER_STATUS";
    
    private static final int BATCH_UPDATE_SIZE = 100;
	
	private int m_groupID;
	private String m_callerID;
	private String m_callerName;
	private String m_status;
	private String m_subscriberId;
	private int m_groupMemberStatus;
	
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
	
	public String subscriberId() {
		
		return m_subscriberId;
	}

	public int groupMemberStatus() {
		
		return m_groupMemberStatus;
	}

	private RbtTempGroupMembersImpl(int groupID,String callerID,String callerName,String status, String subscriberId, int groupMemberStatus)
	{
		m_groupID = groupID;
		m_callerID = callerID;
		m_callerName = callerName;
		m_status = status;
		m_subscriberId = subscriberId;
		m_groupMemberStatus = groupMemberStatus;
	}
	
	static boolean batchInsert(Connection conn, int groupID, String callerID, String callerName,
			String status, String subscriberId, int groupMemberStatus)
	{
		logger.info("RBT::inside batchInsert");

		String query = null;
		PreparedStatement pstmt = null;
		
		query = "INSERT INTO " + TABLE_NAME + " ( " + GROUP_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CALLER_NAME_COL;
		query += ", " + STATUS_COL;
		query += ", " + SUBSCRIBER_ID_COL;
		query += ", " + GROUP_MEMBER_STATUS_COL;
		query += ")";

		query += " VALUES ( " + groupID + ",?,?,'" + status + "',?," + groupMemberStatus + ")";

		logger.info("RBT::query " +query);

		
		try
		{
			logger.info("RBT::inside try block");
			pstmt = conn.prepareStatement(query);
			String[] callerIds = callerID.split(",");
			String[] callerNames = null;
			if(callerName != null) {
				callerNames = callerName.split(",");
			}
						
			for(int i=0; i < callerIds.length; i++) {
				//RBT-15204 Added for member id empty check and removing space for List of issue in addMultipleContacts api
				if(callerIds[i] == null || callerIds[i].trim().equals("")){
					logger.info("continue the loop as caller id is empty.");
					continue;
				}
				pstmt.setString(1, callerIds[i].trim());
				String tempCallerName = null;
				if(callerNames != null) {
					if(callerNames.length >= i) {
						tempCallerName = callerNames[i];
					}
					else {
						tempCallerName = callerNames[callerNames.length - 1];
					}
				}
				pstmt.setString(2, tempCallerName);
				pstmt.setString(3, subscriberId);
				pstmt.addBatch();
				
				if((i%BATCH_UPDATE_SIZE == 0) || ((i+1) == callerIds.length)) {
					pstmt.executeBatch();
					pstmt.clearBatch();
				}				
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
				pstmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		return true;
	}
	
	static RbtTempGroupMembers[] getMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RbtTempGroupMembersImpl groupMembers = null;
		ArrayList<RbtTempGroupMembers> groupMembersList = new ArrayList<RbtTempGroupMembers>();

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
            return groupMembersList.toArray(new RbtTempGroupMembers[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static RbtTempGroupMembers getMemberFromGroups(Connection conn, String callerID, int[] groupIDs)
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
		
		RbtTempGroupMembersImpl groupMember = null;
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
	
	static boolean deleteCallerFromGroup(Connection conn, int groupID, String callerId, String subscriberId)
    {
		int deleteCount = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = "
				+ groupID + " AND " + CALLER_ID_COL + " = " + sqlString(callerId) + " AND " + SUBSCRIBER_ID_COL + " = "
				+ sqlString(subscriberId);
		
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
	
	static RbtTempGroupMembers[] getActiveMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getActiveMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RbtTempGroupMembersImpl groupMembers = null;
		ArrayList<RbtTempGroupMembers> groupMembersList = new ArrayList<RbtTempGroupMembers>();

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
            return groupMembersList.toArray(new RbtTempGroupMembers[0]);
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
	
	static ArrayList<RbtTempGroupMembers> playerGetAddGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RbtTempGroupMembers> addGroupMembers = new ArrayList<RbtTempGroupMembers>();
		
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
	
	static ArrayList<RbtTempGroupMembers> playerGetDelGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_DEACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RbtTempGroupMembers> delGroupMembers = new ArrayList<RbtTempGroupMembers>();
		
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
	
	static List<RbtTempGroupMembers> getGroupMembersByGroupMemberStatus(Connection conn)
    {
        logger.info("RBT::inside getGroupMembersByGroupMemberStatus");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RbtTempGroupMembers groups = null;
		ArrayList<RbtTempGroupMembers> groupsList = new ArrayList<RbtTempGroupMembers>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_MEMBER_STATUS_COL + " IN (1,2) ";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				groups = getGroupMembersFromRS(results);
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
            return groupsList;
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	private static RbtTempGroupMembersImpl getGroupMembersFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String callerID = results.getString(CALLER_ID_COL);
		String callerName = results.getString(CALLER_NAME_COL);
		String status = results.getString(STATUS_COL);
		String subscriberId = results.getString(SUBSCRIBER_ID_COL);
		int groupMemberStatus = results.getInt(GROUP_MEMBER_STATUS_COL);
		
		return new RbtTempGroupMembersImpl(groupID, callerID, callerName, status, subscriberId, groupMemberStatus);
	}
}

