package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RDCGroupMembers;
import com.onmobile.apps.ringbacktones.content.RDCGroups;

public class RDCGroupMembersImpl extends RBTPrimitive implements RDCGroupMembers,iRBTConstant
{
	private static Logger logger = Logger.getLogger(RDCGroupMembersImpl.class);
	
    private static final String TABLE_NAME = "RBT_RDC_GROUP_MEMBERS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String CALLER_ID_COL = "CALLER_ID";
    private static final String CALLER_NAME_COL = "CALLER_NAME";
    private static final String STATUS_COL = "STATUS";
  //1 - To be create group in all circle, 2 - To be delete group in all circle, 3 - Error in create group, 4 - Error in delete group, 5 - send callback to affiliate, 6 - success
    private static final String GROUP_MEMBER_STATUS_COL = "GROUP_MEMBER_STATUS";
    private static final String OPT_NAME_COL = "OPT_NAME";
    private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	
	private int m_groupID;
	private String m_callerID;
	private String m_callerName;
	private String m_status;
	private String m_groupMemberStatus;
	private String m_extraInfo;
	private String m_optName;
	
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
	
	public String groupMemberStatus() {
		return m_groupMemberStatus;
	}
	
	public String extraInfo() {
		return m_extraInfo;
	}
	
	public String optName() {
		return m_optName;
	}

	private RDCGroupMembersImpl(int groupID,String callerID,String callerName,String status, String groupMemberStatus, String optName, String extraInfo)
	{
		m_groupID = groupID;
		m_callerID = callerID;
		m_callerName = callerName;
		m_status = status;
		m_groupMemberStatus = groupMemberStatus;
		m_optName = optName;
		m_extraInfo = extraInfo;
	}
	
	static RDCGroupMembers insert(Connection conn, int groupID, String callerID, String callerName,
			String status, String optName)
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		RDCGroupMembers groupMembers = null;
		String groupMemberStatus = "1";

		query = "INSERT INTO " + TABLE_NAME + " ( " + GROUP_ID_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CALLER_NAME_COL;
		query += ", " + STATUS_COL;
		query += ", " + GROUP_MEMBER_STATUS_COL;
		query += ", " + OPT_NAME_COL;
		query += ")";

		query += " VALUES ( " + groupID;
		query += ",'" + callerID + "'";
		query += ", " + sqlString(callerName);
		query += ",'" + status + "'";
		query += "," + groupMemberStatus;
		query += "," + sqlString(optName);
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
			groupMembers = new RDCGroupMembersImpl(groupID, callerID, callerName, status, groupMemberStatus, optName, null);
			return groupMembers;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_GROUP_MEMBERS table failed");
			return null;
		}
	}
	
	static RDCGroupMembers[] getMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupMembersImpl groupMembers = null;
		ArrayList<RDCGroupMembers> groupMembersList = new ArrayList<RDCGroupMembers>();

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
            return groupMembersList.toArray(new RDCGroupMembers[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static RDCGroupMembers getMemberFromGroups(Connection conn, String callerID, int[] groupIDs)
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
		
		RDCGroupMembersImpl groupMember = null;
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
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " ='D', " + GROUP_MEMBER_STATUS_COL + " = 2 WHERE " + GROUP_ID_COL + " = '"
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
	
	static RDCGroupMembers[] getActiveMembersForGroupID(Connection conn, int groupID)
    {
        logger.info("RBT::inside getActiveMembersForGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupMembersImpl groupMembers = null;
		ArrayList<RDCGroupMembers> groupMembersList = new ArrayList<RDCGroupMembers>();

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
            return groupMembersList.toArray(new RDCGroupMembers[0]);
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
				 STATUS_COL + "='D', " + GROUP_MEMBER_STATUS_COL + " = 2 WHERE " + GROUP_ID_COL + "=" + groupID + " AND " + CALLER_ID_COL + "='" + callerID + "'"; 
		
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
	
	static ArrayList<RDCGroupMembers> playerGetAddGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RDCGroupMembers> addGroupMembers = new ArrayList<RDCGroupMembers>();
		
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
	
	static ArrayList<RDCGroupMembers> playerGetDelGroupMembers(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_DEACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RDCGroupMembers> delGroupMembers = new ArrayList<RDCGroupMembers>();
		
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
	
	static List<RDCGroupMembers> getGroupMembersByGroupMemberStatus(Connection conn, String operatorName)
    {
        logger.info("RBT::inside getGroupsByGroupStatus");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupMembers groups = null;
		ArrayList<RDCGroupMembers> groupsList = new ArrayList<RDCGroupMembers>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_MEMBER_STATUS_COL + " IN (1,2,3,4) AND " + OPT_NAME_COL + " = " + sqlString(operatorName);
		
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


	static boolean updateGroupMemberStatus(Connection conn, int groupID, int groupStatus, String extraInfo, String callerId) {
		String query = "UPDATE " + TABLE_NAME + " SET " + GROUP_MEMBER_STATUS_COL + " = " + groupStatus + ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " WHERE " +
				GROUP_ID_COL + " = " + groupID + " AND " + CALLER_ID_COL + " = " + sqlString(callerId);
		
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
	
	private static RDCGroupMembersImpl getGroupMembersFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String callerID = results.getString(CALLER_ID_COL);
		String callerName = results.getString(CALLER_NAME_COL);
		String status = results.getString(STATUS_COL);
		String groupMemberStatus = results.getString(GROUP_MEMBER_STATUS_COL);
		String optName = results.getString(OPT_NAME_COL);
		String extraInfo = results.getString(EXTRA_INFO_COL);
		
		return new RDCGroupMembersImpl(groupID, callerID, callerName, status, groupMemberStatus, optName, extraInfo);
	}
}

