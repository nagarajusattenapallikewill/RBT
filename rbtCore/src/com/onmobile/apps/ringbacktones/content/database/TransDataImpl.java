package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.TransData;

public class TransDataImpl extends RBTPrimitive implements TransData
{
	private static Logger logger = Logger.getLogger(TransDataImpl.class);

	private static final String TABLE_NAME = "RBT_TRANS_DATA";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String TRANS_ID_COL = "TRANS_ID";
	private static final String TRANS_DATE_COL = "TRANS_DATE";
	private static final String TYPE_COL = "TYPE";
	private static final String ACCESS_COUNT_COL = "ACCESS_COUNT";

	private String m_subscriberID;
	private String m_transID;
	private Date m_transDate;
	private String m_type;
	private String m_accessCount;
	private static String m_databaseType=getDBSelectionString();

	private TransDataImpl(String subscriberID, String transID, Date transDate, String type, String accessCount)
	{
		m_subscriberID = subscriberID;
		m_transID = transID;
		m_transDate = transDate;
		m_type = type;
		m_accessCount = accessCount;
	}

	public String subscriberID()
	{
		return m_subscriberID;
	}

	public String transID()
	{
		return m_transID;
	}

	public Date transDate()
	{
		return m_transDate;
	}

	public String type()
	{
		return m_type;
	}

	public String accessCount()
	{
		return m_accessCount;
	}

	static TransData getTransData(Connection conn, String strTransID, String type)
	{
		logger.info("RBT::inside getTransData");

		String subscriberId;
		String transId;
		Date transDate;
		String accessCount;

		Statement stmt = null;
		ResultSet rs = null;

		TransData transData = null;

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + TRANS_ID_COL
				+ " ='" + strTransID + "' AND "+ TYPE_COL + " = " + sqlString(type);

		logger.info("RBT:: query - " + sql);

		try
		{
			logger.info("RBT:: inside try");

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql);

			if (rs.next())
			{
				subscriberId = rs.getString(SUBSCRIBER_ID_COL);
				transId = rs.getString(TRANS_ID_COL);
				transDate = rs.getDate(TRANS_DATE_COL);
				accessCount = rs.getString(ACCESS_COUNT_COL);

				transData = new TransDataImpl(subscriberId, transId, transDate, type, accessCount);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (transData != null)
		{
			logger.info("RBT:: retrieving records from RBT_TRANS_DATA successful");
			return transData;
		}
		return null;
	}

	static TransData getTransDataAndUpdateAccessCount(Connection conn, String strTransID, String type)
	{
		logger.info("RBT::inside getTransData");

		String subscriberId = null;
		String transId = null;
		Date transDate = null;
		String accessCount = null;

		Statement stmt = null;
		ResultSet rs = null;

		TransData transData = null;

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + TRANS_ID_COL
				+ " = " + sqlString(strTransID) + " AND " + TYPE_COL + " = " + sqlString(type) + " AND " + ACCESS_COUNT_COL + " = '0'";

		logger.info("RBT:: query - " + sql);
		try
		{
			synchronized (TransDataImpl.class)
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);

				if (rs.next())
				{
					subscriberId = rs.getString(SUBSCRIBER_ID_COL);
					transId = rs.getString(TRANS_ID_COL);
					transDate = rs.getDate(TRANS_DATE_COL);
					accessCount = rs.getString(ACCESS_COUNT_COL);

					transData = new TransDataImpl(subscriberId, transId, transDate, type, accessCount);
				}

				if (transData != null)
					update(conn, transId, type, subscriberId, transDate, "1");
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (transData != null)
		{
			logger.info("RBT:: retrieving records from RBT_TRANS_DATA successful");
			return transData;
		}

		return null;
	}

	static TransData[] getTransDataBySubscriberID(Connection conn, String subscriberID, String type)
	{
		logger.info("RBT::inside getTransDataBySubscriberID");

		String subscriberId;
		String transId;
		Date transDate;
		String accessCount;

		Statement stmt = null;
		ResultSet rs = null;

		TransData transData = null;
		ArrayList<TransData> transDataList = new ArrayList<TransData>();

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND "
				+ TYPE_COL + " = " + sqlString(type);

		logger.info("RBT:: query - " + sql);

		try
		{
			logger.info("RBT:: inside try");

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql);

			while (rs.next())
			{
				subscriberId = rs.getString(SUBSCRIBER_ID_COL);
				transId = rs.getString(TRANS_ID_COL);
				transDate = rs.getDate(TRANS_DATE_COL);
				accessCount = rs.getString(ACCESS_COUNT_COL);

				transData = new TransDataImpl(subscriberId, transId, transDate, type, accessCount);
				transDataList.add(transData);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if(transDataList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_TRANS_DATA successful");
			return transDataList.toArray(new TransData[0]);
		} 

		return null;
	}

	static TransData getFirstTransDataByType(Connection conn, String type)
	{
		String subscriberId = null;
		String transId = null;
		Date transDate = null;
		String accessCount = null;

		Statement stmt = null;
		ResultSet rs = null;

		TransData transData = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ TYPE_COL + " = " + sqlString(type) + " AND "
				+ ACCESS_COUNT_COL + " = '0'";

		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " AND ROWNUM <= 1";
		else
			query += " LIMIT 1";

		logger.info("RBT:: query - " + query);
		try
		{
			synchronized (TransDataImpl.class)
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);

				if (rs.next())
				{
					subscriberId = rs.getString(SUBSCRIBER_ID_COL);
					transId = rs.getString(TRANS_ID_COL);
					transDate = rs.getDate(TRANS_DATE_COL);
					accessCount = rs.getString(ACCESS_COUNT_COL);

					transData = new TransDataImpl(subscriberId, transId, transDate, type, accessCount);
				}

				if (transData != null)
					update(conn, transId, type, subscriberId, transDate, "1");
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		return transData;
	}

	static TransData insert(Connection conn, String strTransID, String strSubID, String type,Date transDate)
	{
		logger.info("RBT::inside insert");

		Statement stmt = null;

		int id = -1;
		TransData transData = null;

		if(transDate==null){
			transDate = Calendar.getInstance().getTime();
		}
		
		String transDateString = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			transDateString = sqlTime(transDate);
		else
			transDateString = mySQLDateTime(transDate);

		String sql = "INSERT INTO " + TABLE_NAME + "(" + SUBSCRIBER_ID_COL;
		sql += "," + TRANS_ID_COL;
		sql += "," + TRANS_DATE_COL; 
		sql += "," + TYPE_COL + ") VALUES('";
		sql += strSubID + "', ";
		sql += sqlString(strTransID) + ", ";
		sql += transDateString + ", ";
		sql += sqlString(type) + ")";

		logger.info("RBT:: query - " + sql);

		try
		{
			logger.info("RBT::inside try");

			stmt = conn.createStatement();

			id = stmt.executeUpdate(sql);

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		if (id > 0)
		{
			logger.info("RBT::insertion to RBT_BULK_PROMO table successful");
			transData = new TransDataImpl(strSubID, strTransID, transDate, type, null);
		}
		return transData;
	}

	static int removeOldTransData(Connection conn, float duration)
	{
		logger.info("RBT::inside removeOldTransData");

		int count = 0;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + TRANS_DATE_COL
			+ " <= ( now() -" + duration + ")";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + TRANS_DATE_COL
			+ " <= TIMESTAMPADD( DAY, -"+duration+",SYSDATE())";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			count = stmt.executeUpdate(query);
			//n = stmt.getUpdateCount();
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return -1;
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return count;
	}

	static boolean removeTransData(Connection conn, String strTransID, String type)
	{
		logger.info("RBT::inside removeOldTransData");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + TRANS_ID_COL + " ='"
				+ strTransID + "' AND " + TYPE_COL + " = " + sqlString(type);

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
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
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static boolean removeListOfTransData(Connection conn, String strTransIDs)
	{
		logger.info("RBT::inside removeListOfTransData");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + TRANS_ID_COL + " IN ("+ strTransIDs + ")";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
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
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return (n == 1);
	}
	
	static boolean update(Connection conn, String strTransID, String type, String subscriberID, Date transDate, String accessCount)
	{
		logger.info("RBT::inside updateSubscriberID");

		if(subscriberID == null && transDate == null)
		{
			logger.info("RBT:: Nothing is there to update");
			return false;
		}

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + SUBSCRIBER_ID_COL + " = "+ sqlString(subscriberID);

		if(transDate != null)
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query += ", " + TRANS_DATE_COL + " = "+ sqlTime(transDate);
			else 
				query += ", " + TRANS_DATE_COL + " = "+ mySqlTime(transDate);
		}

		if(accessCount != null)
			query += ", " + ACCESS_COUNT_COL + " = "+ sqlString(accessCount);

		query += " WHERE " + TRANS_ID_COL + " ='" + strTransID + "' AND " + TYPE_COL + " = " + sqlString(type);

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		return (n > 0);
	}
	
	static List<TransData> getTransDataByType(Connection conn, String type,
			int limit) {
		logger.info("RBT::inside getTransDataByType");
		String subscriberId;
		String transId;
		Date transDate;
		String accessCount;
		Statement stmt = null;
		ResultSet rs = null;
		List<TransData> transDataLst = new ArrayList<TransData>();
		TransData transData = null;
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE_COL
				+ " IN ( " + type + " ) ";
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sql += " AND ROWNUM <= " + limit;
		else
			sql += " LIMIT " + limit;

		logger.info("RBT:: query - " + sql);
		try {
			logger.info("RBT:: inside try");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				subscriberId = rs.getString(SUBSCRIBER_ID_COL);
				transId = rs.getString(TRANS_ID_COL);
				transDate = rs.getDate(TRANS_DATE_COL);
				accessCount = rs.getString(ACCESS_COUNT_COL);
				type = rs.getString(TYPE_COL);
				transData = new TransDataImpl(subscriberId, transId, transDate,
						type, accessCount);
				transDataLst.add(transData);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (transDataLst != null && !transDataLst.isEmpty()) {
			logger.info("RBT:: retrieving records from RBT_TRANS_DATA successful");
			return transDataLst;
		}
		return null;
	}
	
	static List<TransData> getTransDataByTypeAndTransDate(Connection conn, String type,
			int limit,Date transDate) {
		logger.info("RBT::inside getTransDataByType");
		String subscriberId;
		String transId;
		String accessCount;
		Statement stmt = null;
		ResultSet rs = null;
		List<TransData> transDataLst = new ArrayList<TransData>();
		TransData transData = null;
		
		String transDateString = null;
		if(transDate==null){
			transDate = Calendar.getInstance().getTime();
		}
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			transDateString = sqlTime(transDate);
		else
			transDateString = mySQLDateTime(transDate);
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE_COL
				+ " IN ( " + type + " ) AND "+TRANS_DATE_COL+" <= "+transDateString;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			sql += " AND ROWNUM <= " + limit;
		else
			sql += " LIMIT " + limit;

		logger.info("RBT:: query - " + sql);
		try {
			logger.info("RBT:: inside try");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				subscriberId = rs.getString(SUBSCRIBER_ID_COL);
				transId = rs.getString(TRANS_ID_COL);
				transDate = rs.getDate(TRANS_DATE_COL);
				accessCount = rs.getString(ACCESS_COUNT_COL);
				type = rs.getString(TYPE_COL);
				transData = new TransDataImpl(subscriberId, transId, transDate,
						type, accessCount);
				transDataLst.add(transData);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (transDataLst != null && !transDataLst.isEmpty()) {
			logger.info("RBT:: retrieving records from RBT_TRANS_DATA successful");
			return transDataLst;
		}
		return null;
	}
}