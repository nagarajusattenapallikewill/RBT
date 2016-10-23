package com.onmobile.apps.ringbacktones.content.database;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RDCGroups;

public class RDCGroupsImpl extends RBTPrimitive implements RDCGroups, iRBTConstant
{
	private static Logger logger = Logger.getLogger(RDCGroupsImpl.class);
	
    private static final String TABLE_NAME = "RBT_RDC_GROUPS";
	private static final String GROUP_ID_COL = "GROUP_ID";
    private static final String PRE_GROUP_ID_COL = "PRE_GROUP_ID";
    private static final String GROUP_NAME_COL = "GROUP_NAME";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String GROUP_PROMO_ID_COL = "GROUP_PROMO_ID";
    
    //1 - To be create group in all circle, 2 - To be delete group in all circle, 3 - Error in create group, 4 - Error in delete group, 5 - send callback to affiliate, 6 - success
    private static final String GROUP_STATUS_COL = "GROUP_STATUS";
    private static final String EXTRA_INFO_COL = "EXTRA_INFO";
    private static final String OPT_NAME_COL = "OPT_NAME";
    private static final String REF_ID_COL = "REF_ID_COL";
    private static final String STATUS_COL = "STATUS";
    
	private int m_groupID;
	private String m_preGroupID;
	private String m_subscriberID;
	private String m_groupName;
	private String m_groupPromoID;
	private String m_status;
	private String m_groupStatus;
	private String m_extraInfo;
	private String m_optName;
	private String m_refID;
	
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
	
	public String groupStatus() {
		return m_groupStatus;
	}
	
	public String extraInfo() {
		return m_extraInfo;
	}
	
	public String optName() {
		return m_optName;
	}
	
	public String refID() {
		return m_refID;
	}
	
	private RDCGroupsImpl(int groupID,String preGroupID,String groupName,
						String subscriberID,String groupPromoID,String status, String optName, String groupStatus, String groupExtraInfo, String refId)
	{
		m_groupID = groupID;
		m_preGroupID = preGroupID;
		m_groupName = groupName;
		m_subscriberID = subscriberID;
		m_groupPromoID = groupPromoID;
		m_status = status;
		m_optName = optName;
		m_groupStatus = groupStatus;
		m_extraInfo = groupExtraInfo;
		m_refID = refId;
		
	}
	
	static boolean insert(Connection conn, String preGroupID, String groupName,
							String subscriberID, String groupPromoID, String status, String refID, String optName)
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
		query += ", " + REF_ID_COL;
		query += ", " + OPT_NAME_COL;
		query += ", " + GROUP_STATUS_COL;
		query += ")";

		query += " VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
		
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
            pstmt.setString(6, refID);
            pstmt.setString(7, optName);
            
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
			String status, String refID, String optName) {
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
		query += ", " + REF_ID_COL;
		query += ", " + OPT_NAME_COL;
		query += ", " + GROUP_STATUS_COL;
		query += ")";

		query += " VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

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
			pstmt.setString(7, refID);
            pstmt.setString(8, optName);

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
	
	static RDCGroups[] getPredefinedGroupsAddedForSubscriber(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getPredefinedGroupsAddedForSubscriber");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupsImpl groups = null;
		ArrayList<RDCGroups> groupsList = new ArrayList<RDCGroups>();

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
            return groupsList.toArray(new RDCGroups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static RDCGroups getGroupByPreGroupID(Connection conn, String preGroupID, String subscriberID)
    {
        logger.info("RBT::inside getGroupByPreGroupID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupsImpl groups = null;
		

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
	
	static RDCGroups getActiveGroupByGroupName(Connection conn, String groupName, String subscriberID)
    {
        logger.info("RBT::inside getActiveGroupByGroupName");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		String groupNam = null;
		RDCGroupsImpl groups = null;
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
	
	static RDCGroups[] getGroupsForSubscriberID(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getGroupsForSubscriberID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupsImpl groups = null;
		ArrayList<RDCGroups> groupsList = new ArrayList<RDCGroups>();

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
            return groupsList.toArray(new RDCGroups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	static RDCGroups[] getActiveGroupsForSubscriberID(Connection conn, String subscriberID)
    {
        logger.info("RBT::inside getActiveGroupsForSubscriberID");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupsImpl groups = null;
		ArrayList<RDCGroups> groupsList = new ArrayList<RDCGroups>();

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
            return groupsList.toArray(new RDCGroups[0]);
        } 
		else
        {
            logger.info("RBT::no records in "+TABLE_NAME);
            return null;
        }
    }
	
	
	static List<RDCGroups> getGroupsByGroupStatus(Connection conn, String operatorName)
    {
        logger.info("RBT::inside getGroupsByGroupStatus");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		RDCGroupsImpl groups = null;
		ArrayList<RDCGroups> groupsList = new ArrayList<RDCGroups>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_STATUS_COL + " IN (1,2,3,4,5) AND " + OPT_NAME_COL + " = " + sqlString(operatorName);
		
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
            return groupsList;
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
				 STATUS_COL + "= 'D', " + GROUP_STATUS_COL+ " = 2 WHERE " + GROUP_ID_COL + " = " + groupID; 
		
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
    
	static boolean updateGroupStatus(Connection conn, int groupID, int groupStatus, String extraInfo) {
		String query = "UPDATE " + TABLE_NAME + " SET " + GROUP_STATUS_COL + " = " + groupStatus + ", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo) + " WHERE " +
				GROUP_ID_COL + " = " + groupID;
		
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
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " ='D', " + GROUP_STATUS_COL+ "= 2 WHERE " + SUBSCRIBER_ID_COL + " = '"
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
	
	static RDCGroups getGroup(Connection conn, int groupID) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = " + groupID;
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		RDCGroups group = null;
		
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
	
	static RDCGroups getGroupByRefID(Connection conn, String refID) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + REF_ID_COL + " = " + sqlString(refID);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		RDCGroups group = null;
		
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
	
	static ArrayList<RDCGroups> playerGetAddGroups(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_ACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RDCGroups> addGroups = new ArrayList<RDCGroups>();
		
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
	
	static ArrayList<RDCGroups> playerGetDelGroups(Connection conn) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = "
				+ sqlString(STATE_TO_BE_DEACTIVATED);
		logger.info("RBT::query - " + query);
		
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<RDCGroups> delGroups = new ArrayList<RDCGroups>();
		
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
	
	private static RDCGroupsImpl getGroupsFromRS(ResultSet results) throws SQLException {
		int groupID  = results.getInt(GROUP_ID_COL);
		String preGroupID = results.getString(PRE_GROUP_ID_COL);
		String groupName = results.getString(GROUP_NAME_COL);
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String groupPromoID = results.getString(GROUP_PROMO_ID_COL);
		String status = results.getString(STATUS_COL);
		String optName = results.getString(OPT_NAME_COL);
		String groupStatus = results.getString(GROUP_STATUS_COL);
		String groupExtraInfo = results.getString(EXTRA_INFO_COL);
		String refId = results.getString(EXTRA_INFO_COL);
		
	
		return new RDCGroupsImpl(groupID, preGroupID, groupName, subscriberID, groupPromoID,
				status, optName, groupStatus, groupExtraInfo, refId);
	}
}
