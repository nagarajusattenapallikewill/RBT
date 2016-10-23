package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.FeedSchedule;
public class FeedScheduleImpl extends RBTPrimitive implements FeedSchedule
{
	private static Logger logger = Logger.getLogger(FeedScheduleImpl.class);
	
    private static final String TABLE_NAME = "RBT_FEED_SCHEDULE";
    private static final String FEED_ID_COL = "FEED_ID";
    private static final String FEED_TYPE_COL = "FEED_TYPE";
    private static final String FEED_SCHEDULE_NAME_COL = "FEED_SCHEDULE_NAME";
    private static final String FEED_SUB_KEYWORD_COL = "FEED_SUB_KEYWORD";
    private static final String FEED_START_TIME_COL = "FEED_START_TIME";
    private static final String FEED_END_TIME_COL = "FEED_END_TIME";
    private static final String CLASS_TYPE_COL = "CLASS_TYPE";
    private static final String FEED_ON_SUCCESS_SMS_COL = "FEED_ON_SUCCESS_SMS";
    private static final String FEED_ON_FAILURE_SMS_COL = "FEED_ON_FAILURE_SMS";
    private static final String PACK_TYPE_COL = "PACK_TYPE";
    private static final String STATUS_COL = "STATUS";
	
    private int m_feedID;
    private String m_type;
    private String m_name;
	private String m_subKeyword;
	private Date m_startTime;
	private Date m_endTime;
	private String m_classType;
	private String m_smsFeedOnSuccess;
	private String m_smsFeedOnFailure;
	private String m_packType;
	private int m_status;
	private static String m_databaseType=getDBSelectionString();
	
	private FeedScheduleImpl(int feedID, String type, String name,
			String subKeyword, Date startTime, Date endTime,
			String classType, String smsFeedOnSuccess, String smsFeedOnFailure, String packType, int status)
	{
		m_feedID = feedID;
		m_type = type;
		m_name = name;
		m_subKeyword = subKeyword;
		m_startTime = startTime;
		m_endTime = endTime;
		m_classType = classType;
		m_smsFeedOnSuccess = smsFeedOnSuccess;
		m_smsFeedOnFailure = smsFeedOnFailure;
		m_packType = packType;
		m_status = status;
	}
	
	public int feedID()
    {
        return m_feedID;
    }
	
	public String type()
    {
        return m_type;
    }
	
	public String name()
    {
        return m_name;
    }

	public String subKeyword()
	{
	    return m_subKeyword;
	}
	
	public Date startTime()
	{
	    return m_startTime;
	}
	
	public Date endTime()
	{
	    return m_endTime;
	}
	
	public String classType()
	{
	    return m_classType;
	}
	
	public String smsFeedOnSuccess()
	{
	    return m_smsFeedOnSuccess;
	}
	
	public String smsFeedOnFailure()
	{
	    return m_smsFeedOnFailure;
	}
	
	public String packType() {
		return m_packType;
	}

	public int status() {
		return m_status;
	}

	static void setEndTime(Connection conn, String type, Date endTime)
	{
		logger.info("RBT::inside setEndTime");

		String query = null;
		Statement stmt = null;

		String endDate = "SYSDATE";
		if (m_databaseType.equals(DB_SAPDB)) 
		{
			endDate = (endTime != null ? sqlTime(endTime): "SYSDATE"); 
		} 
		else if (m_databaseType.equals(DB_MYSQL))
		{
			endDate = (endTime != null ? mySqlTime(endTime): "SYSDATE()"); 
		}

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " +
			FEED_END_TIME_COL + " = " + endDate  +
			" WHERE " + FEED_TYPE_COL  + " = " + sqlString(type) + " AND " + FEED_END_TIME_COL + " >= "+SAPDB_SYSDATE+
			" AND TO_CHAR( " + FEED_END_TIME_COL + " , 'YYYY/MM/DD') = TO_CHAR( "+SAPDB_SYSDATE+", 'YYYY/MM/DD')";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "UPDATE " + TABLE_NAME + " SET " +
			FEED_END_TIME_COL + " = " + endDate  +
			" WHERE " + FEED_TYPE_COL  + " = " + sqlString(type) + " AND " + FEED_END_TIME_COL + " >= "+MYSQL_SYSDATE+
			" AND DATE_FORMAT( " + FEED_END_TIME_COL + " , '%Y/%m/%d') = DATE_FORMAT("+MYSQL_SYSDATE+", '%Y/%m/%d')";
		}
		logger.info("RBT::query "+query);

		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return;
	}

	
	static FeedSchedule insert(Connection conn, int feedID, String type,
			String name, String subKeyword, Date startTime, Date endTime,
			String classType, String smsFeedOnSuccess, String smsFeedOnFailure, String packType, int status)
    {
		int id = -1;
		String query = null;
		Statement stmt = null;

		FeedSchedule feedSchedule = null;

		if (m_databaseType.equals(DB_SAPDB)) {
			query = "INSERT INTO " + TABLE_NAME + " ( " + FEED_ID_COL;
			query += ", " + FEED_TYPE_COL;
			query += ", " + FEED_SCHEDULE_NAME_COL;
			query += ", " + FEED_SUB_KEYWORD_COL;
			query += ", " + FEED_START_TIME_COL;
			query += ", " + FEED_END_TIME_COL;
			query += ", " + CLASS_TYPE_COL;
			query += ", " + FEED_ON_SUCCESS_SMS_COL;
			query += ", " + FEED_ON_FAILURE_SMS_COL;
			query += ", " + PACK_TYPE_COL;
			query += ", " + STATUS_COL;
			query += ")";

			query += " VALUES ( " + sqlInt(feedID);
			query += ", " + sqlString(type);
			query += ", " + sqlString(name);
			query += ", " + sqlString(subKeyword);
			query += ", " + sqlTime(startTime);
			query += ", " + sqlTime(endTime);
			query += ", " + sqlString(classType);
			query += ", " + sqlString(smsFeedOnSuccess);
			query += ", " + sqlString(smsFeedOnFailure);
			query += ", " + sqlString(packType);
			query += ", " + sqlInt(status);
			query += ")";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "INSERT INTO " + TABLE_NAME + " ( " + FEED_ID_COL;
			query += ", " + FEED_TYPE_COL;
			query += ", " + FEED_SCHEDULE_NAME_COL;
			query += ", " + FEED_SUB_KEYWORD_COL;
			query += ", " + FEED_START_TIME_COL;
			query += ", " + FEED_END_TIME_COL;
			query += ", " + CLASS_TYPE_COL;
			query += ", " + FEED_ON_SUCCESS_SMS_COL;
			query += ", " + FEED_ON_FAILURE_SMS_COL;
			query += ", " + PACK_TYPE_COL;
			query += ", " + STATUS_COL;
			query += ")";

			query += " VALUES ( " + sqlInt(feedID);
			query += ", " + sqlString(type);
			query += ", " + sqlString(name);
			query += ", " + sqlString(subKeyword);
			query += ", " + mySqlTime(startTime);
			query += ", " + mySqlTime(endTime);
			query += ", " + sqlString(classType);
			query += ", " + sqlString(smsFeedOnSuccess);
			query += ", " + sqlString(smsFeedOnFailure);
			query += ", " + sqlString(packType);
			query += ", " + sqlInt(status);
			query += ")";
		}
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
			closeStatementAndRS(stmt, null);
		}
        if(id == 0)
        {
            logger.info("RBT::insertion to RBT_FEED_SCHEDULE table successful");
            feedSchedule = new FeedScheduleImpl(feedID, type, name, subKeyword, startTime, endTime, classType, smsFeedOnSuccess, smsFeedOnFailure, packType, status);
            return feedSchedule;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_FEED_SCHEDULE table failed");
            return null;
        }
    }
		
    static boolean update(Connection conn, String type, String name, String subKeyword, Date startTime, Date endTime, int validityPeriod, String classType, String smsFeedOnSuccess, String smsFeedOnFailure)  
    {
		int n = -1;
		String query = null;
		Statement stmt = null;
	
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " +
			 FEED_END_TIME_COL + " = " + sqlTime(endTime) + ", " +
			 CLASS_TYPE_COL + " = " + sqlString(classType) + ", " +
			 FEED_ON_SUCCESS_SMS_COL + " = " + sqlString(smsFeedOnSuccess) + ", " +
			 FEED_ON_FAILURE_SMS_COL + " = " + sqlString(smsFeedOnFailure) + 
			" WHERE " + FEED_TYPE_COL + " = " + sqlString(type) + " AND " + FEED_SCHEDULE_NAME_COL + " = " + sqlString(name) + 
			" AND " + FEED_SUB_KEYWORD_COL + " = " + sqlString(subKeyword) + 
			" AND TO_CHAR( " + FEED_START_TIME_COL + " , 'YYYY/MM/DD') = TO_CHAR( " + sqlTime(startTime) + ", 'YYYY/MM/DD')";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "UPDATE " + TABLE_NAME + " SET " +
			 FEED_END_TIME_COL + " = " + mySqlTime(endTime) + ", " +
			 CLASS_TYPE_COL + " = " + sqlString(classType) + ", " +
			 FEED_ON_SUCCESS_SMS_COL + " = " + sqlString(smsFeedOnSuccess) + ", " +
			 FEED_ON_FAILURE_SMS_COL + " = " + sqlString(smsFeedOnFailure) + 
			" WHERE " + FEED_TYPE_COL + " = " + sqlString(type) + " AND " + FEED_SCHEDULE_NAME_COL + " = " + sqlString(name) + 
			" AND " + FEED_SUB_KEYWORD_COL + " = " + sqlString(subKeyword) + 
			" AND DATE_FORMAT( " + FEED_START_TIME_COL + " , '%Y/%m/%d') = DATE_FORMAT( " + mySqlTime(startTime) + ", '%Y/%m/%d')";
		}
		
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
    }
	
	static FeedSchedule getFeedSchedule(Connection conn, String feedType, String feedKeyWord)
    {
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		FeedSchedule feedSchedule = null;
		
		if(feedKeyWord != null)
		{
			feedKeyWord = feedKeyWord.toLowerCase();
		}
		
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(feedType) + " AND LOWER(" + FEED_SUB_KEYWORD_COL + ") = " + sqlString(feedKeyWord) + " AND " + FEED_START_TIME_COL + " <= "+SAPDB_SYSDATE+" AND "+SAPDB_SYSDATE+" <= " + FEED_END_TIME_COL;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(feedType) + " AND LOWER(" + FEED_SUB_KEYWORD_COL + ") = " + sqlString(feedKeyWord) + " AND " + FEED_START_TIME_COL + " <= "+MYSQL_SYSDATE+" AND "+MYSQL_SYSDATE+" <= " + FEED_END_TIME_COL;
		}
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				feedSchedule = getFeedScheduleFromRS(results);
			}
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            return null;
        }
		finally
		{
			closeStatementAndRS(stmt, results);
		}
        return feedSchedule;
    }
	
	static FeedSchedule[] getFeedSchedules(Connection conn, String feedType, String feedKeyWord, int period)
    {
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		List<FeedSchedule> feedScheduleList = new ArrayList<FeedSchedule>();
		
		if(feedKeyWord != null)
		{
			feedKeyWord = feedKeyWord.toLowerCase();
		}
		
		if (m_databaseType.equals(DB_SAPDB))
		{
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL
					+ " = " + sqlString(feedType) + " AND "
					+ "LOWER("+FEED_SUB_KEYWORD_COL+")" + " = " + sqlString(feedKeyWord)
					+ " AND " + FEED_START_TIME_COL + " BETWEEN "
					+ SAPDB_SYSDATE + " AND ( " + SAPDB_SYSDATE + " + "
					+ period + " ) ORDER BY " + FEED_START_TIME_COL;
		}
		else if (m_databaseType.equals(DB_MYSQL))
		{
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
						+ FEED_TYPE_COL + " = " + sqlString(feedType) + " AND "
						+ "LOWER("+FEED_SUB_KEYWORD_COL+")" + " = " + sqlString(feedKeyWord)
						+ " AND " + FEED_START_TIME_COL + " BETWEEN "
						+ MYSQL_SYSDATE + " AND TIMESTAMPADD(DAY,"+ period +",SYSDATE())"
						+ " ORDER BY " + FEED_START_TIME_COL;
		}
		
		logger.info("RBT::query "+query);
		
        try
        {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				FeedSchedule feedSchedule = getFeedScheduleFromRS(results);
				feedScheduleList.add(feedSchedule);
			}
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            return null;
        }
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(feedScheduleList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_FEED_SCHEDULE successful");
            return (FeedSchedule[])feedScheduleList.toArray(new FeedSchedule[0]);
        } 
		else
        {
			logger.info("RBT::no records in RBT_FEED_SCHEDULE");
            return null;
        }
    }
	
	static FeedSchedule [] getActiveFeedSchedules(Connection conn, String feedType, String feedKeyWord, int period)
    {
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		List<FeedSchedule> feedScheduleList = new ArrayList<FeedSchedule>();
		
		String whereClause = "";
		
		if(feedType != null)
			whereClause = FEED_TYPE_COL +" = "+ sqlString(feedType);
		
		if(feedKeyWord != null)
		{
			if(!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += FEED_SUB_KEYWORD_COL +" = "+ sqlString(feedKeyWord);
		}
		
		if(!whereClause.equals(""))
			whereClause += " AND ";
		if (m_databaseType.equals(DB_SAPDB))
			whereClause += FEED_START_TIME_COL +" <= ( "+ SAPDB_SYSDATE +" + "+ period +" ) AND "+ FEED_END_TIME_COL +" > "+ SAPDB_SYSDATE;
		else
			whereClause += FEED_START_TIME_COL +" <= TIMESTAMPADD(DAY,"+ period +",SYSDATE()) AND "+ FEED_END_TIME_COL +" > "+ MYSQL_SYSDATE;
		
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + whereClause + " ORDER BY " + FEED_START_TIME_COL; 
		
		logger.info("RBT::query "+query);
		
        try
        {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				FeedSchedule feedSchedule = getFeedScheduleFromRS(results);
				feedScheduleList.add(feedSchedule);
			}
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            return null;
        }
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(feedScheduleList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_FEED_SCHEDULE successful");
            return (FeedSchedule[])feedScheduleList.toArray(new FeedSchedule[0]);
        } 
		else
        {
			logger.info("RBT::no records in RBT_FEED_SCHEDULE");
            return null;
        }
    }
	
	static boolean remove(Connection conn, String type, String name, String subKeyword, Date startTime)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(type) + " AND " + FEED_SCHEDULE_NAME_COL + " = " + sqlString(name) + " AND " + FEED_SUB_KEYWORD_COL + " = " + sqlString(subKeyword) + " AND TO_CHAR( " + FEED_START_TIME_COL + " , 'YYYY/MM/DD') = TO_CHAR( " + sqlTime(startTime) + ", 'YYYY/MM/DD')";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(type) + " AND " + FEED_SCHEDULE_NAME_COL + " = " + sqlString(name) + " AND " + FEED_SUB_KEYWORD_COL + " = " + sqlString(subKeyword) + " AND DATE_FORMAT( " + FEED_START_TIME_COL + " , '%Y/%m/%d') = DATE_FORMAT( " + mySqlTime(startTime) + ", '%Y/%m/%d')";
		}
		
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
	}
	
	static FeedSchedule[] getActiveFeedSchedule(Connection conn, int interval) {
		String sysdateStr = RBTDBManager.getSysdateString();
		if(interval != -1)
			sysdateStr = sysdateStr + "+" + interval;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_START_TIME_COL + " <= "
				+ sysdateStr + " AND " + FEED_END_TIME_COL + " > " + sysdateStr;
		
		logger.info("RBT::query - " + query);
		
		ArrayList<FeedSchedule> activeList = new ArrayList<FeedSchedule>();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				activeList.add(getFeedScheduleFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}

		if(activeList.size() > 0) {
			logger.info("RBT::retreiving records successful from " + TABLE_NAME);
			return activeList.toArray(new FeedSchedule[0]);
		}
		else {
			logger.info("RBT::no records to retreive from " + TABLE_NAME);
			return null;
		}
	}
	
	static FeedSchedule[] getAvailableFeedSchedule(Connection conn, int interval) {
		String sysdateStr = RBTDBManager.getSysdateString();
		String intervalDateStr = sysdateStr;
		if (interval != -1)
			intervalDateStr = intervalDateStr + "+" + interval;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE (" + FEED_START_TIME_COL + " <= "
				+ sysdateStr + " OR " + FEED_START_TIME_COL + " <= " + intervalDateStr + ") AND "
				+ FEED_END_TIME_COL + " > " + sysdateStr;

		logger.info("RBT::query - " + query);

		ArrayList<FeedSchedule> activeList = new ArrayList<FeedSchedule>();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next())
			{
				activeList.add(getFeedScheduleFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}

		if (activeList.size() > 0) {
			logger.info("RBT::retreiving records successful from " + TABLE_NAME);
			return activeList.toArray(new FeedSchedule[0]);
		}
		else {
			logger.info("RBT::no records to retreive from " + TABLE_NAME);
			return null;
		}
	}
	
	private static FeedSchedule getFeedScheduleFromRS(ResultSet rs) throws SQLException {
		RBTResultSet results = new RBTResultSet(rs);
		int feedID = results.getInt(FEED_ID_COL);
		String feedType = results.getString(FEED_TYPE_COL);
		String feedScheduleName = results.getString(FEED_SCHEDULE_NAME_COL);
		String feedSubKeyWord = results.getString(FEED_SUB_KEYWORD_COL);
		Date feedStartTime = results.getTimestamp(FEED_START_TIME_COL);
		Date feedEndTime = results.getTimestamp(FEED_END_TIME_COL);
		String classType = results.getString(CLASS_TYPE_COL);
		String feedOnSuccessSMS = results.getString(FEED_ON_SUCCESS_SMS_COL);
		String feedOnFailureSMS = results.getString(FEED_ON_FAILURE_SMS_COL);
		String packType = results.getString(PACK_TYPE_COL);
		int status = results.getInt(STATUS_COL);

		return new FeedScheduleImpl(feedID, feedType, feedScheduleName, feedSubKeyWord,
				feedStartTime, feedEndTime, classType, feedOnSuccessSMS, feedOnFailureSMS, packType, status);
	}
	
	public static FeedSchedule[] getActiveFeedSchedulesByStatus(Connection conn, int status) 
	{
		String sysdateStr = RBTDBManager.getSysdateString();
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_START_TIME_COL + " <= "
				+ sysdateStr + " AND " + FEED_END_TIME_COL + " > " + sysdateStr
				+ " AND " + STATUS_COL + " = " + status;
		
		logger.info("RBT::query - " + query);
		
		ArrayList<FeedSchedule> activeList = new ArrayList<FeedSchedule>();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next())
			{
				activeList.add(getFeedScheduleFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error(e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}
		if(activeList.size() > 0) {
			logger.info("RBT::retreiving records successful from " + TABLE_NAME);
			return activeList.toArray(new FeedSchedule[0]);
		}
		else {
			logger.info("RBT::no records to retreive from " + TABLE_NAME);
			return null;
		}
	}

	static boolean updateStatus(Connection conn, int feedID, int status)  
    {
		int n = -1;
		String query = null;
		Statement stmt = null;
	
		if (m_databaseType.equals(DB_SAPDB))
		{
			query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlInt(status) + 
			" WHERE " + FEED_ID_COL + " = " + sqlInt(feedID);
		}
		else if (m_databaseType.equals(DB_MYSQL)) 
		{
			query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = " + sqlInt(status) +
			" WHERE " + FEED_ID_COL + " = " + sqlInt(feedID);
		}
		
		logger.info("RBT::query "+query);
		try
        {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
        }
        catch (SQLException se)
        {
        	logger.error("", se);
            return false;
        }
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n == 1);
    }
}