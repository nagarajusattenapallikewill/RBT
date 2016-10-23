package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTBulkUploadSubscriber;

public class RBTBulkUploadSubscriberDAO extends RBTPrimitive
{
	private static Logger logger = Logger.getLogger(RBTBulkUploadSubscriberDAO.class);
	
	private static final String TABLE_NAME 		= "RBT_BULK_UPLOAD_SUBSCRIBERS";
	private static final String TASK_ID 		= "TASK_ID";
	private static final String SUBSCRIBER_ID 	= "SUBSCRIBER_ID";
	private static final String SUBSCRIBER_TYPE	= "SUBSCRIBER_TYPE";
	private static final String CIRCLE_ID 		= "CIRCLE_ID";
	private static final String CONTENT_ID 		= "CONTENT_ID";
	private static final String STATUS 			= "STATUS";
	private static final String REASON 			= "REASON";	
	private static final String PROCESS_TIME	= "PROCESS_TIME";

	private static String DATABASE_TYPE = getDBSelectionString();

	public static boolean createBulkSubscriber(RBTBulkUploadSubscriber rbtBulkUploadSubscriber)
	{
		logger.info("RBT::creating BulkSubscriber");

		String sysdate = SAPDB_SYSDATE;
		if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
			sysdate = MYSQL_SYSDATE;
		
		String query = null;	
		int rowsUpdated = 0;
		
		query = "INSERT INTO " + TABLE_NAME + " ( " + TASK_ID;
		query += ", " + SUBSCRIBER_ID;
		query += ", " + SUBSCRIBER_TYPE;
		query += ", " + CIRCLE_ID;
		query += ", " + CONTENT_ID;
		query += ", " + STATUS;
		query += ", " + PROCESS_TIME;
		query += ", " + REASON;
		query += ")";

		query += " VALUES ( " + rbtBulkUploadSubscriber.getTaskId();
		query += ", " + sqlString(rbtBulkUploadSubscriber.getSubscriberId());
		query += ", '" + rbtBulkUploadSubscriber.getSubscriberType()+"'";
		query += ", " + sqlString(rbtBulkUploadSubscriber.getCircleId());
		query += ", " + sqlString(rbtBulkUploadSubscriber.getContentId());
		query += ", " + rbtBulkUploadSubscriber.getStatus();
		query += ", " + sysdate;
		query += ", " + sqlString(rbtBulkUploadSubscriber.getReason());
		query += ")";

		Connection conn = null;
		Statement stmt = null;

		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query);
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, stmt, null);
			}catch(Exception e)
			{
			}
		}

		return rowsUpdated > 0;
	}
	
	public static List<RBTBulkUploadSubscriber> getRBTBulkUploadSubscribers(int taskId)
	{

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		RBTBulkUploadSubscriber  rbtBulkUploadSubscriber = null;
		List<RBTBulkUploadSubscriber> bulkUploadSubList = new ArrayList<RBTBulkUploadSubscriber>();

		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement("SELECT * FROM "+ TABLE_NAME+" WHERE "+TASK_ID +"= ? ");
			preparedStatement.setInt(1, taskId);
			preparedStatement.execute();
			rs = preparedStatement.getResultSet();

			while(rs.next())
			{
				rbtBulkUploadSubscriber = prepareFromRS(rs);
				bulkUploadSubList.add(rbtBulkUploadSubscriber);
			}

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, rs);
			}catch(Exception e)
			{
			}
		}
		return bulkUploadSubList;
	}

	/**
	 * @param taskId
	 * @param status subscriber status success/failure
	 * @return
	 */
	public static List<RBTBulkUploadSubscriber> getRBTBulkUploadSubscribers(int taskId, int status)
	{

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		RBTBulkUploadSubscriber  rbtBulkUploadSubscriber = null;
		List<RBTBulkUploadSubscriber> bulkUploadSubList = new ArrayList<RBTBulkUploadSubscriber>();

		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement("SELECT * FROM "+ TABLE_NAME+" WHERE "+TASK_ID +"= ? AND "+STATUS +" = ?");
			preparedStatement.setInt(1, taskId);
			preparedStatement.setInt(2, status); // New Requests
			preparedStatement.execute();
			rs = preparedStatement.getResultSet();

			while(rs.next())
			{
				rbtBulkUploadSubscriber = prepareFromRS(rs);
				bulkUploadSubList.add(rbtBulkUploadSubscriber);
			}

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, rs);
			}catch(Exception e)
			{
			}
		}
		return bulkUploadSubList;
	}
	
	public static HashMap<String, Integer> getBulkSubscriberCount(Integer[] taskIDs)
	{
		Connection conn = null;
		Statement stmt = null;
		String sql = null;
		ResultSet rs = null;
		
		String taskIDStr = null;
		if (taskIDs != null && taskIDs.length > 0)
		{
			taskIDStr = "";
			for (int i : taskIDs) 
			{
				taskIDStr += i+",";
			}
			taskIDStr = taskIDStr.substring(0, taskIDStr.length()-1);
		}
		HashMap<String, Integer> circleCountMap = new HashMap<String, Integer>();
		
		sql = "SELECT " + CIRCLE_ID + ", COUNT(*) AS COUNT FROM " + TABLE_NAME 
				+ " WHERE " + PROCESS_TIME + " >="
				+ getSysdateString(true) + " AND " + PROCESS_TIME + " <="
				+ getSysdateString(false);
		if (taskIDStr != null)
			sql += " AND "+TASK_ID +" NOT IN ("+taskIDStr+")";
				
			sql	+= " GROUP BY " + CIRCLE_ID;
		
		logger.info("RBT::sql >"+sql);

		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				String circleID = rs.getString(CIRCLE_ID);
				int count = rs.getInt("COUNT");

				circleCountMap.put(circleID, count);
			}
			
		}
		catch(SQLException sqle)
		{
			logger.error("", sqle);
		}
		finally
		{
			try
			{
				releaseConnection(conn, stmt, rs);
			}catch(Exception e)
			{
			}
		}
		return circleCountMap;
	}
	
	public static List<RBTBulkUploadSubscriber> getBulkUploadSubscriber(String subscriberID)
	{
		Connection conn = null;
		Statement stmt = null;
		String sql = null;
		ResultSet rs = null;

		List<RBTBulkUploadSubscriber> subscriberList = new ArrayList<RBTBulkUploadSubscriber>();

		sql = "SELECT * FROM "+TABLE_NAME+" WHERE "+SUBSCRIBER_ID+" = "+sqlString(subscriberID);
		logger.info("RBT::sql >"+sql);
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				subscriberList.add(prepareFromRS(rs));
			}
		}
		catch(SQLException sqle)
		{
			logger.error("", sqle);
		}
		finally
		{
			try
			{
				releaseConnection(conn, stmt, rs);
			}catch(Exception e)
			{
			}
		}
		return subscriberList;
	}

	public static boolean updateRBTBulkUploadSubscriber(RBTBulkUploadSubscriber rbtBulkUploadSubscriber)
	{
		Connection conn = null;
		PreparedStatement preparedStatement	= null;
		int rowsUpdated = 0;
		
		String sql = buildUpdateQuery();
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, rbtBulkUploadSubscriber.getCircleId());
			preparedStatement.setString(2, rbtBulkUploadSubscriber.getContentId());
			preparedStatement.setInt(3, rbtBulkUploadSubscriber.getStatus());
			preparedStatement.setString(4, rbtBulkUploadSubscriber.getReason());
			preparedStatement.setInt(5, rbtBulkUploadSubscriber.getTaskId());
			preparedStatement.setString(6, rbtBulkUploadSubscriber.getSubscriberId());
			rowsUpdated = preparedStatement.executeUpdate();

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, null);
			}catch(Exception e)
			{
			}
		}
		return (rowsUpdated > 0);
	}

	public static boolean updateRBTBulkUploadSubscribers(List<RBTBulkUploadSubscriber> subList)
	{

		Connection conn = null;
		PreparedStatement preparedStatement	= null;
		String sql = buildUpdateQuery();
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(sql);

			for (RBTBulkUploadSubscriber rbtBulkUploadSubscriber : subList) 
			{
				preparedStatement.setString(1, rbtBulkUploadSubscriber.getCircleId());
				preparedStatement.setString(2, rbtBulkUploadSubscriber.getContentId());
				preparedStatement.setInt(3, rbtBulkUploadSubscriber.getStatus());
				preparedStatement.setString(4, rbtBulkUploadSubscriber.getReason());
				preparedStatement.setInt(5, rbtBulkUploadSubscriber.getTaskId());
				preparedStatement.setString(6, rbtBulkUploadSubscriber.getSubscriberId());
				preparedStatement.addBatch();
			}
			int [] updateCounts = preparedStatement.executeBatch();

			for (int i=0; i<updateCounts.length; i++)  // CHECK 
			{
				if (updateCounts[i] >= 0) 
				{
					return true;
				} 
				else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
					return false;
				}
			}

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, null);
			}catch(Exception e)
			{
			}
		}
		return false;
	}
	
	public static boolean deleteRBTBulkUploadSubscriber(RBTBulkUploadSubscriber rbtBulkUploadSubscriber)
	{
		Connection conn = null;
		PreparedStatement preparedStatement	= null;
		int rowsUpdated = 0;
		
		String sql = "DELETE FROM "+TABLE_NAME + " WHERE "+ TASK_ID +" =? AND "+SUBSCRIBER_ID +" = ?";
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setInt(1, rbtBulkUploadSubscriber.getTaskId());
			preparedStatement.setString(2, rbtBulkUploadSubscriber.getSubscriberId());
			
			rowsUpdated = preparedStatement.executeUpdate();

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, null);
			}catch(Exception e)
			{
			}
		}
		return (rowsUpdated > 0);
	}
	
	public static boolean deleteSubscriberByTaskID(int taskID)
	{
		Connection conn = null;
		PreparedStatement preparedStatement	= null;
		int rowsUpdated = 0;

		String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + TASK_ID + " = ?";
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setInt(1, taskID);

			rowsUpdated = preparedStatement.executeUpdate();

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				releaseConnection(conn, preparedStatement, null);
			}
			catch(Exception e)
			{
			}
		}
		return (rowsUpdated > 0);
	}

	private static RBTBulkUploadSubscriber prepareFromRS(ResultSet rs)
	{
		RBTBulkUploadSubscriber rbtBulkUploadSubscriber = new RBTBulkUploadSubscriber();
		try
		{
			rbtBulkUploadSubscriber.setTaskId(rs.getInt(TASK_ID));
			rbtBulkUploadSubscriber.setSubscriberId(rs.getString(SUBSCRIBER_ID));
			rbtBulkUploadSubscriber.setSubscriberType(rs.getString(SUBSCRIBER_TYPE).charAt(0));
			rbtBulkUploadSubscriber.setCircleId(rs.getString(CIRCLE_ID));
			rbtBulkUploadSubscriber.setContentId(rs.getString(CONTENT_ID));
			rbtBulkUploadSubscriber.setStatus(rs.getInt(STATUS));
			rbtBulkUploadSubscriber.setReason(rs.getString(REASON));
			
			rbtBulkUploadSubscriber.setProcessTime(rs.getTimestamp(PROCESS_TIME));

		}
		catch(SQLException sqle)
		{
			logger.error("", sqle);
		}
		return rbtBulkUploadSubscriber;
	}
	
	private static String buildUpdateQuery()
	{
		String query = null;
		query = "UPDATE "+TABLE_NAME+ " SET "+CIRCLE_ID +"=?";
		query += ", "+CONTENT_ID +"=?";
		query += ", "+STATUS +"=?";
		query += ", "+REASON +"=?";
		query += " WHERE "+TASK_ID +"=?  AND "+SUBSCRIBER_ID +"=?";
		
		return query;
	}
	
	private static String getSysdateString(boolean isStartDate)
	{
		String sysdateString = null; 
		
		Calendar cal = Calendar.getInstance();

		if(DATABASE_TYPE.equalsIgnoreCase(DB_SAPDB)) 
		{
			sysdateString = "TO_DATE('"+cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH);
			sysdateString += isStartDate?" 00:00:00'":" 23:59:59'";
			sysdateString += ", 'YYYY/MM/DD HH24:MI:SS')";
		}
		else
		{
			sysdateString = "DATE_FORMAT('"+cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH);
			sysdateString += isStartDate?" 00:00:00'": " 23:59:59'";
			sysdateString += ", '%Y/%m/%d %H:%i:%s')";
		}

		return sysdateString;
			
	}
}
