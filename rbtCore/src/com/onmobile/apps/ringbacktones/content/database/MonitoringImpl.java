package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Monitoring;

/**
 * query implementation for RBT_MONITORING table
 * 
 * @author Sreekar
 * @since 2010-01-11
 */
public class MonitoringImpl extends RBTPrimitive implements Monitoring, iRBTConstant {
	private static Logger logger = Logger.getLogger(MonitoringImpl.class);
	
	private static final String TABLE_NAME = "RBT_MONITORING";
	private static final String MSISDN_COL = "MSISDN";
	private static final String CREATE_TIME_COL = "CREATE_TIME";
	private static final String TRACE_TYPE_COL = "TRACE_TYPE";
	private static final String STATUS_COL = "STATUS";
	private static final String TRACE_RESULT_COL = "TRACE_RESULT";
	private String _msisdn;
	private Date _createTime;
	private String _traceType;
	private char _status;
	private String _traceResult;

	private static String _databaseType = getDBSelectionString();

	protected MonitoringImpl(String msisdn, Date createTime, String traceType, char status,
			String traceResult) {
		_msisdn = msisdn;
		_createTime = createTime;
		_traceType = traceType;
		_status = status;
		_traceResult = traceResult;
	}

	public Date createTime() {
		return _createTime;
	}

	public String msisdn() {
		return _msisdn;
	}

	public char status() {
		return _status;
	}

	public String traceResult() {
		return _traceResult;
	}

	public String traceType() {
		return _traceType;
	}

	public String toString() {
		return "msisdn->" + _msisdn + ":createTime->" + _createTime + ":traceType->" + _traceType
				+ ":status->" + _status + ":traceResult->" + _traceResult;
	}
	protected static boolean insert(Connection conn, String msisdn, Date createTime,
			String traceType, char status, String traceResult) {
		if (traceResult == null)
			traceResult = "";
		String query = "INSERT INTO " + TABLE_NAME + " (" + MSISDN_COL + ", " + CREATE_TIME_COL
				+ ", " + TRACE_TYPE_COL + ", " + STATUS_COL + ", " + TRACE_RESULT_COL
				+ ") VALUES (" + sqlString(msisdn) + ", ";

		if (_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += sqlTime(createTime);
		else
			query += mySQLDateTime(createTime);

		query += ", " + sqlString(traceType) + ", " + sqlString(status + "") + ", "
				+ sqlString(traceResult) + ")";

		logger.info("RBT::query->" + query);

		Statement stmt = null;
		int n = 0;
		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatement(stmt);
		}
		return n == 1;
	}

	protected static Monitoring getPendingSubscriberOrTypeMonitor(Connection conn, String msisdn,
			String traceType) {
		Monitoring monitoring = null;
		Statement stmt = null;
		RBTResultSet rs = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE (" + MSISDN_COL + " = "
				+ sqlString(msisdn) + " OR " + TRACE_TYPE_COL + " = " + sqlString(traceType)
				+ ") AND " + STATUS_COL + " = " + sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			if (rs.next())
				monitoring = createMonitoringFromRS(rs);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeResultSet(rs);
			closeStatement(stmt);
		}
		return monitoring;
	}

	protected static Monitoring getPendingSubscriberMonitor(Connection conn, String msisdn) {
		Monitoring monitoring = null;
		Statement stmt = null;
		RBTResultSet rs = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + MSISDN_COL + " = "
				+ sqlString(msisdn) + " AND " + STATUS_COL + " = "
				+ sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			if (rs.next())
				monitoring = createMonitoringFromRS(rs);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeResultSet(rs);
			closeStatement(stmt);
		}
		return monitoring;
	}

	protected static boolean endActiveMonitor(Connection conn, String msisdn, String traceType) {
		String query = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = "
				+ sqlString(MONITOR_STATE_FINISHED + "") + " WHERE " + MSISDN_COL + " = "
				+ sqlString(msisdn) + " AND " + TRACE_TYPE_COL + " = " + sqlString(traceType)
				+ " AND " + STATUS_COL + " = " + sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);

		Statement stmt = null;
		int n = 0;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatement(stmt);
		}
		return (n >= 1);
	}

	protected static boolean updateActiveMonitor(Connection conn, String msisdn, String traceResult) {
		if (traceResult == null)
			traceResult = "";
		String query = "UPDATE " + TABLE_NAME + " SET " + TRACE_RESULT_COL + " = "
				+ sqlString(traceResult) + " WHERE " + MSISDN_COL + " = " + sqlString(msisdn)
				+ " AND " + STATUS_COL + " = " + sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);

		Statement stmt = null;
		int n = 0;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatement(stmt);
		}
		return (n >= 1);
	}

	protected static boolean concatToActiveMonitor(Connection conn, String msisdn,
			String traceResultToAppend) {
		if (traceResultToAppend == null)
			return true;
		String query = "UPDATE " + TABLE_NAME + " SET " + TRACE_RESULT_COL + " = CONCAT("
				+ TRACE_RESULT_COL + ", " + sqlString(traceResultToAppend) + ") WHERE "
				+ MSISDN_COL + " = " + sqlString(msisdn) + " AND " + STATUS_COL + " = "
				+ sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);

		Statement stmt = null;
		int n = 0;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatement(stmt);
		}
		return (n >= 1);
	}

	protected static boolean updateActiveMonitor(Connection conn, String msisdn, String traceType,
			String traceResult) {
		if (traceResult == null)
			traceResult = "";
		String query = "UPDATE " + TABLE_NAME + " SET " + TRACE_RESULT_COL + " = "
				+ sqlString(traceResult) + " WHERE " + MSISDN_COL + " = " + sqlString(msisdn)
				+ " AND " + TRACE_TYPE_COL + " = " + sqlString(traceType) + " AND " + STATUS_COL
				+ " = " + sqlString(MONITOR_STATE_STARTED + "");
		logger.info("RBT::query->" + query);

		Statement stmt = null;
		int n = 0;

		try {
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			closeStatement(stmt);
		}
		return (n >= 1);
	}

	private static Monitoring createMonitoringFromRS(RBTResultSet rs) throws SQLException {
		String msisdn = rs.getString(MSISDN_COL);
		Date createTime = rs.getTimestamp(CREATE_TIME_COL);
		String traceType = rs.getString(TRACE_TYPE_COL);
		char status = rs.getString(STATUS_COL).charAt(0);
		String traceResult = rs.getString(TRACE_RESULT_COL);

		return new MonitoringImpl(msisdn, createTime, traceType, status, traceResult);
	}

	private static void closeResultSet(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		}
		catch (Exception e) {

		}
	}

	private static void closeStatement(Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
		}
		catch (Exception e) {

		}
	}
}