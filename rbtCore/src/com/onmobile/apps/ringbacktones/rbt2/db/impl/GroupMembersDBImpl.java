package com.onmobile.apps.ringbacktones.rbt2.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;



import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.common.db.OnMobileDBServices;

public class GroupMembersDBImpl{
	  
	
		static Logger logger = Logger.getLogger(GroupMembersDBImpl.class);
		private static final String TABLE_NAME = "RBT_GROUP_MEMBERS";
		private static final String GROUP_ID_COL = "GROUP_ID";
	    private static final String CALLER_ID_COL = "CALLER_ID";
	    private static final String CALLER_NAME_COL = "CALLER_NAME";
	    private static final String STATUS_COL = "STATUS";

	
	public static boolean deleteGroupMembers(int groupId, List<GroupMember> groupMembers) throws Exception{
		logger.info(" inside deleteGroupMembers...");
		boolean deleted = false;
		PreparedStatement preparedStatement = null;
		String deleteQuery = "DELETE FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COL + " = "+groupId+"  AND " + CALLER_ID_COL + " = ?";
		Connection connection = null;
		try {
			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(deleteQuery);
			for(GroupMember groupMember: groupMembers){
				preparedStatement.setString(1, groupMember.getMemberID());
				preparedStatement.addBatch();
			}
			
			int[] executeBatch = preparedStatement.executeBatch();
			if(executeBatch.length > 0){
				deleted=  true;
			}
		} catch (Exception e) {
			logger.info("Exception occured while deleting group member: "+e,e);
			throw new Exception(Constants.FAILURE);
		} finally {
			closeStatementAndRS(preparedStatement, null, connection);
		}
		return deleted;
	}

	
	public static boolean addGroupMembers(int groupId, List<GroupMember> groupMembers) throws Exception{
		logger.info("inside addGroupMembers...");
		boolean added = false;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		String query = "INSERT INTO " + TABLE_NAME + " ( " + GROUP_ID_COL + ", " + CALLER_ID_COL+ ", " + CALLER_NAME_COL+", " + STATUS_COL+ ")"
		+ " VALUES ( " + groupId + ",?,?,?)";
		try {
			connection = RBTDBManager.getInstance().getConnection();
			preparedStatement =  connection.prepareStatement(query);
			for(GroupMember groupMember: groupMembers){
				preparedStatement.setString(1, groupMember.getMemberID());
				preparedStatement.setString(2, groupMember.getMemberName());
				preparedStatement.setString(3,groupMember.getMemberStatus());
				preparedStatement.addBatch();
			}
			
			int[] executeBatch = preparedStatement.executeBatch();
			if(executeBatch.length > 0){
				added=  true;
			}
		} catch (Exception e) {
			logger.info("Exception occured while inserting group member: "+e,e);
			if(e.getMessage().contains("Duplicate")){
				throw  new Exception(Constants.DUPLICATE_CALLER_ENTRY);
			}
			throw new Exception(Constants.FAILURE);
		} finally {
			closeStatementAndRS(preparedStatement, null, connection);
		}
		return added;
	}
	
	public static int getGroupMemberCount(GroupMember groupMember) {
		logger.info("getGroupMember invoked...");
		StringBuffer query = new StringBuffer();
		query.append("SELECT COUNT(*) FROM ").append(TABLE_NAME);
		if(groupMember.getMemberID() != null && !groupMember.getMemberID().isEmpty()
				&& groupMember.getGroupID() != null && !groupMember.getGroupID().isEmpty()) {

			query.append(" WHERE ").append(CALLER_ID_COL).append(" = ").append("'").append(groupMember.getMemberID()).append("'")
			.append(" AND ").append(GROUP_ID_COL).append(" = ").append("'").append(groupMember.getGroupID()).append("'")
			.append(" AND ").append(STATUS_COL).append(" IN ").append("(").append("'W',").append("'A',")
			.append("'N',").append("'B',").append("'Z',").append("'G'")
			.append(" )");
		}

		int rowCount = 0;
		Connection connection = RBTDBManager.getInstance().getConnection();
		Statement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.createStatement();
			String finalQuery = query.toString();
			logger.info("Executing query: "+finalQuery);
			resultSet = stmt.executeQuery(finalQuery);
			if (resultSet.next())
				rowCount = resultSet.getInt(1);
		} catch (SQLException e) {
			logger.error("Exception Occured", e);
		} finally {
			closeStatementAndRS(stmt, resultSet, connection);
		}
		logger.info("Returning Count: "+rowCount);
		return rowCount;
	}
	
	
	 public static void closeStatementAndRS(Statement stmt, ResultSet rs, Connection connection) 
	    {
	    	try
			{
				if(rs !=null)
					rs.close();
			}
			catch(Throwable t)
			{
				logger.error("Exception in closing db resultset " + t,t);
			}
			try
			{
				if(stmt !=null)
					stmt.close();
			}
			catch(Throwable t)
			{
				logger.error("Exception in closing db statement " + t, t);
			}	
			
			try {
				if (connection != null)
					OnMobileDBServices.releaseConnection(connection);
			} catch (Throwable t) {
				logger.error("Exception in closing db connection " + t, t);
			}
		}
	
}
