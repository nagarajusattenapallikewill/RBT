package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;

/**
 * @author vasipalli.sreenadh
 *
 */
public class RBTBulkUploadTaskDAO extends RBTPrimitive
{
	private static Logger logger = Logger.getLogger(RBTBulkUploadTaskDAO.class);
	
	private static final String TABLE_NAME 					= "RBT_BULK_UPLOAD_TASKS";
	private static final String TASK_ID						= "TASK_ID";
	private static final String TASK_NAME					= "TASK_NAME";
	private static final String CIRCLE_ID					= "CIRCLE_ID";
	private static final String ACTIVATION_CLASS			= "ACTIVATION_CLASS";
	private static final String SELECTION_CLASS				= "SELECTION_CLASS";
	private static final String SELECTION_TYPE 				= "SELECTION_TYPE";
	private static final String TASK_TYPE 					= "TASK_TYPE";
	private static final String ACTIVATED_BY				= "ACTIVATED_BY";
	private static final String ACT_INFO 					= "ACT_INFO";
	private static final String UPLOAD_TIME					= "UPLOAD_TIME";
	private static final String END_TIME					= "END_TIME";
	private static final String PROCESS_TIME				= "PROCESS_TIME";
	private static final String TASK_STATUS					= "TASK_STATUS";
	private static final String TASK_MODE					= "TASK_MODE";
	private static final String TASK_INFO 					= "TASK_INFO";
	
	private static String DATABASE_TYPE = getDBSelectionString();
	
	private static final String GET_BULKUPLOAD_TASK 		= "SELECT * FROM "+ TABLE_NAME+" WHERE "+TASK_ID +"= ?";
	private static final String GET_ALL_BULKUPLOAD_TASK		= "SELECT * FROM "+ TABLE_NAME ;
	
	public static int createBulkUploadTask(RBTBulkUploadTask rbtBulkUploadTask)
	{
		logger.info("RBT::creating bulkuploadTask");
		
		String query = null;	
		int taskID = -1;
		
		String uploadTime = MYSQL_SYSDATE;
		String endTime = mySQLDateTime(rbtBulkUploadTask.getEndTime());
		String processTime = mySQLDateTime(rbtBulkUploadTask.getProcessTime());
		if(DATABASE_TYPE.equalsIgnoreCase(DB_SAPDB))
		{
			uploadTime = SAPDB_SYSDATE;
			taskID = generateTaskID(); 
			endTime = sqlTime(rbtBulkUploadTask.getEndTime());
			processTime = sqlTime(rbtBulkUploadTask.getProcessTime());
		}
				
		query = "INSERT INTO " + TABLE_NAME + " ( " ;
		if(DATABASE_TYPE.equalsIgnoreCase(DB_SAPDB))
		{
			query += " " + TASK_ID +", ";
		}
		query += " "  + TASK_NAME;
		query += ", " + CIRCLE_ID;
		query += ", " + ACTIVATION_CLASS;
		query += ", " + SELECTION_CLASS;
		query += ", " + SELECTION_TYPE;
		query += ", " + TASK_TYPE;
		query += ", " + ACTIVATED_BY;
		query += ", " + ACT_INFO;
		query += ", " + UPLOAD_TIME;
		query += ", " + PROCESS_TIME;
		query += ", " + END_TIME;
		query += ", " + TASK_STATUS;
		query += ", " + TASK_INFO;
		query += ")";

		query += " VALUES ( " ;
		if(DATABASE_TYPE.equalsIgnoreCase(DB_SAPDB))
		{
			query += " " + taskID + ",";
		}
		query += " " + sqlString(rbtBulkUploadTask.getTaskName());
		query += ", " + sqlString(rbtBulkUploadTask.getCircleId());
		query += ", " + sqlString(rbtBulkUploadTask.getActivationClass());
		query += ", " + sqlString(rbtBulkUploadTask.getSelectionClass());
		query += ", " + rbtBulkUploadTask.getSelectionType();
		query += ", " + sqlString(rbtBulkUploadTask.getTaskType());
		query += ", " + sqlString(rbtBulkUploadTask.getActivatedBy());
		query += ", " + sqlString(rbtBulkUploadTask.getActInfo());
		query += ", " + uploadTime;
		query += ", " + processTime;
		query += ", " + endTime;
		query += ", " + rbtBulkUploadTask.getTaskStatus();
		query += ", " + sqlString(rbtBulkUploadTask.getTaskInfo());
		query += ")";
		
		logger.info("RBT::SQL >"+query);
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			int no = stmt.executeUpdate(query);
			
			if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
			{	
				rs = stmt.getGeneratedKeys(); 
				if(rs.next())
					taskID = rs.getInt(1); // Returns the TaskID created
			}
			else
			{
				if(no == 0)
					taskID = -1;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, stmt, rs);
		}
		return taskID;
	}
	
	private static int generateTaskID()
	{
		Connection con = null;
		Statement stmt = null;
		int taskID = -1;

		String sql  = "SELECT RBT_BULK_SEQ.NEXTVAL FROM DUAL";
		try{
			con = getConnection();
			stmt = con.createStatement();
			ResultSet rs  = stmt.executeQuery(sql);
			
			if(rs.first())
				taskID=rs.getInt(1);
			
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(con, stmt, null);
		}
		return taskID;
	}
	
	public static RBTBulkUploadTask getRBTBulkUploadTask(int taskId)
	{
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		RBTBulkUploadTask  rbtBulkUploadTask = null;
		
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(GET_BULKUPLOAD_TASK);
			preparedStatement.setInt(1, taskId);
			preparedStatement.execute();
			rs = preparedStatement.getResultSet();
			if(rs.first())
				rbtBulkUploadTask = prepareFromRS(rs);

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, preparedStatement, rs);
		}
		return rbtBulkUploadTask;
	}
	
	public static List<RBTBulkUploadTask> getRBTBulkTasks(int taskStatus, String taskType, String circleID, String mode)
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		
		String whereClause = null;
		if (taskStatus == -2) {
			// -2 is being passed from CCC to get all processed tasks
			whereClause = TASK_STATUS +" <> 0 ";
		} else {
			whereClause = TASK_STATUS +" = "+taskStatus;
		}
		if (taskType != null)
			whereClause += " AND "+TASK_TYPE +" = "+sqlString(taskType);
		else
			whereClause += " AND "+TASK_TYPE+ " != 'CORPORATE'";
		
		if (circleID != null)
			whereClause += " AND "+CIRCLE_ID +" = "+sqlString(circleID);
		if (mode != null)
			whereClause += " AND "+TASK_MODE +" = "+sqlString(mode);
				
		String sqlQuery = "SELECT * FROM "+ TABLE_NAME +" WHERE "+whereClause;

		List<RBTBulkUploadTask> uploadList = new ArrayList<RBTBulkUploadTask>();
		
		try
		{
			conn = getConnection();
			statement = conn.createStatement();
			rs = statement.executeQuery(sqlQuery);
			while(rs.next())
			{
				RBTBulkUploadTask  rbtBulkUploadTask = null;
				rbtBulkUploadTask = prepareFromRS(rs);
				uploadList.add(rbtBulkUploadTask);
			}
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, statement, rs);
		}
		return uploadList;
	}
	
	public static List<RBTBulkUploadTask> getAllRBTBulkUploadTasks()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		List<RBTBulkUploadTask> uploadList = new ArrayList<RBTBulkUploadTask>();
		
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(GET_ALL_BULKUPLOAD_TASK);
		
			while(rs.next())
			{
				RBTBulkUploadTask  rbtBulkUploadTask = null;
				rbtBulkUploadTask = prepareFromRS(rs);
				uploadList.add(rbtBulkUploadTask);
			}
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, stmt, rs);
		}
		return uploadList;
	}
	
	public static List<RBTBulkUploadTask> getTobeDeactivatedBulkTasks(int taskStatus, String taskType)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		String sysdate = MYSQL_SYSDATE;
		if(DATABASE_TYPE.equalsIgnoreCase(DB_SAPDB))
		{
			sysdate = SAPDB_SYSDATE;
		}
		
		String sqlQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ TASK_STATUS + " = " + taskStatus + " AND " + TASK_TYPE
				+ " = " + sqlString(taskType) + " AND " + END_TIME + " < "
				+ sysdate;
		
		List<RBTBulkUploadTask> uploadList = new ArrayList<RBTBulkUploadTask>();
		
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
		
			while(rs.next())
			{
				RBTBulkUploadTask  rbtBulkUploadTask = null;
				rbtBulkUploadTask = prepareFromRS(rs);
				uploadList.add(rbtBulkUploadTask);
			}
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, stmt, rs);
		}
		return uploadList;
	}
	
	public static boolean updateRBTBulkUploadTask(RBTBulkUploadTask rbtBulkUploadTask)
	{
		Connection conn = null;
		PreparedStatement preparedStatement	= null;
		int rowsUpdated = 0;
				
		String sql = buildUpdateQuery(rbtBulkUploadTask.getProcessTime(), rbtBulkUploadTask.getEndTime());
		try
		{
			conn = getConnection();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, rbtBulkUploadTask.getTaskName());
			preparedStatement.setString(2, rbtBulkUploadTask.getCircleId());
			preparedStatement.setString(3, rbtBulkUploadTask.getActivationClass());
			preparedStatement.setString(4, rbtBulkUploadTask.getSelectionClass());
			preparedStatement.setInt(5, rbtBulkUploadTask.getSelectionType());
			preparedStatement.setString(6, rbtBulkUploadTask.getTaskType());
			preparedStatement.setString(7, rbtBulkUploadTask.getActivatedBy());
			preparedStatement.setString(8, rbtBulkUploadTask.getActInfo());
			preparedStatement.setInt(9, rbtBulkUploadTask.getTaskStatus());
			preparedStatement.setString(10, rbtBulkUploadTask.getTaskMode());
			preparedStatement.setString(11, rbtBulkUploadTask.getTaskInfo());
			preparedStatement.setInt(12, rbtBulkUploadTask.getTaskId());
			
			rowsUpdated = preparedStatement.executeUpdate();

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, preparedStatement, null);
		}
		return (rowsUpdated > 0);
	}
	
	public static Integer[] getTaskIDsByTaskType(String taskType)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Integer> taskIDList = new ArrayList<Integer>();
		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT "+TASK_ID +" FROM "+TABLE_NAME +" WHERE "+TASK_TYPE +" = '"+taskType+"'");
		
			while(rs.next())
			{
				taskIDList.add(rs.getInt(TASK_ID));
			}
		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			releaseConnection(conn, stmt, rs);
		}
		return (Integer[])taskIDList.toArray(new Integer[0]);
	}
	
	public static boolean deleteTaskByTaskID(int taskID)
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
				releaseConnection(conn, preparedStatement, null);
		}
		return (rowsUpdated > 0);
	}
	
	private static RBTBulkUploadTask prepareFromRS(ResultSet rs)
	{
		RBTBulkUploadTask rbtBulkUploadTask = new RBTBulkUploadTask();
		try
		{
			rbtBulkUploadTask.setTaskId(rs.getInt(TASK_ID));
			rbtBulkUploadTask.setTaskName(rs.getString(TASK_NAME));
			rbtBulkUploadTask.setCircleId(rs.getString(CIRCLE_ID));
			rbtBulkUploadTask.setActivationClass(rs.getString(ACTIVATION_CLASS));
			rbtBulkUploadTask.setSelectionClass(rs.getString(SELECTION_CLASS));
			rbtBulkUploadTask.setSelectionType(rs.getInt(SELECTION_TYPE));
			rbtBulkUploadTask.setTaskType(rs.getString(TASK_TYPE));
			rbtBulkUploadTask.setActivatedBy(rs.getString(ACTIVATED_BY));
			rbtBulkUploadTask.setActInfo(rs.getString(ACT_INFO));
			rbtBulkUploadTask.setUploadTime(rs.getTimestamp(UPLOAD_TIME));
			rbtBulkUploadTask.setProcessTime(rs.getTimestamp(PROCESS_TIME));
			rbtBulkUploadTask.setEndTime(rs.getTimestamp(END_TIME));
			rbtBulkUploadTask.setTaskMode(rs.getString(TASK_MODE));
			rbtBulkUploadTask.setTaskStatus(rs.getInt(TASK_STATUS));
			rbtBulkUploadTask.setTaskInfo(rs.getString(TASK_INFO));
		}
		catch(SQLException sqle)
		{
			logger.error("", sqle);
		}
		
		return rbtBulkUploadTask;
	}
	
	private static String buildUpdateQuery(Date startDate, Date endDate)
	{
		String startTime = SAPDB_SYSDATE;
		if(startDate != null)
			startTime =  sqlTime(startDate);
		String endTime = sqlTime(endDate);
	
		if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
		{	
			endTime = mySQLDateTime(endDate);
			startTime = MYSQL_SYSDATE;
			if(startDate != null)
				startTime =  mySQLDateTime(startDate);
		}
		
		String query = null;
		query = "UPDATE "+TABLE_NAME+ " SET "+TASK_NAME +"=?";
		query += ", "+CIRCLE_ID +"=?";
		query += ", "+ACTIVATION_CLASS +"=?";
		query += ", "+SELECTION_CLASS +"=?";
		query += ", "+SELECTION_TYPE +"=?";
		query += ", "+TASK_TYPE +"=?";
		query += ", "+ACTIVATED_BY +"=?";
		query += ", "+ACT_INFO +"=?";
		query += ", "+PROCESS_TIME +"="+startTime;
		query += ", "+END_TIME +"="+endTime;
		query += ", "+TASK_STATUS +"=?";
		query += ", "+TASK_MODE +"=?";
		query += ", "+TASK_INFO +"=?";
		query += " WHERE "+TASK_ID +"=? ";
		
		return query;
	}
}