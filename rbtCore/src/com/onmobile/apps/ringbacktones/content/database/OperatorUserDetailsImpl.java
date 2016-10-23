package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;

public class OperatorUserDetailsImpl extends RBTPrimitive implements OperatorUserDetails {

	private static Logger logger = Logger.getLogger(OperatorUserDetailsImpl.class);
	private static final String TABLE_NAME = "RBT_OPERATOR_USER_DETAILS";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String STATUS_ID_COL = "STATUS";
	private static final String SERVICE_KEY_ID_COL = "SERVICE_KEY";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String OPERATOR_NAME_COL = "OPERATOR_NAME";
	private String subscriberId;
	private String status;
	private String serviceKey;
	private String operatorName;
	private String circleId;
	

	public OperatorUserDetailsImpl(String msisdn, String serviceKey, String status, String operatorName, String circleID ) {

		this.subscriberId = msisdn;
		this.serviceKey = serviceKey;
		this.status = status;
		this.operatorName = operatorName;
		this.circleId = circleID;
	}

	public OperatorUserDetailsImpl() {
	}

	@Override
	public String subID() {
		return this.subscriberId;
	}

	@Override
	public String status() {
		return this.status;
	}

	@Override
	public String serviceKey() {
		return this.serviceKey;
	}

	@Override
	public String operatorName() {
		return this.operatorName;
	}
	
	@Override
	public String circleId() {
		return this.circleId;
	}
	
	static OperatorUserDetails getUserDetails(Connection conn, String subID) {
		Statement stmt = null;
		ResultSet results = null;
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "'";

		logger.info("Executing the query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.first()) {
				return getUserDetailsFromRS(results);
			}
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
		return null;
	}

	static OperatorUserDetails insert(Connection conn, String subscriberID, String serviceKey, String status, String operatorName,String circleID) {

		String query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + SERVICE_KEY_ID_COL;
		query += ", " + STATUS_ID_COL;
		query += ", " + OPERATOR_NAME_COL;
		query += ", " + CIRCLE_ID_COL;
;
		query += ")";

		query += " VALUES ( " + "'" + subscriberID + "'";
		query += ", " + sqlString(serviceKey);
		query += ", " + sqlString(status);
		query += ", " + sqlString(operatorName);
		query += ", " + sqlString(circleID);
		query += ")";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			logger.info("Insertion into RBT_USER_DETAILS table is SUCCESS for subscriber: " + subscriberID);
			return new OperatorUserDetailsImpl(subscriberID, serviceKey, status, operatorName , circleID);
		} else {
			logger.info("Insertion into RBT_SUBSCRIBER table is FAILED for subscriber: " + subscriberID);
			return null;
		}
	}

	private static OperatorUserDetails getUserDetailsFromRS(ResultSet results) throws SQLException {
		String subscriberID = results.getString(SUBSCRIBER_ID_COL);
		String serviceKey = results.getString(SERVICE_KEY_ID_COL);
		String status = results.getString(STATUS_ID_COL);
		String operatorName = results.getString(OPERATOR_NAME_COL);
		String circleID = results.getString(CIRCLE_ID_COL);

		return new OperatorUserDetailsImpl(subscriberID, serviceKey, status, operatorName , circleID);
	}

	static boolean remove(Connection conn, String subscriberID) {
		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		return (n == 1);
	}

	static OperatorUserDetails update(Connection conn, String subscriberID, String serviceKey, String status, String operatorName,String circleID) {
		String query = "UPDATE " + TABLE_NAME +" SET " ;
		if (circleID == null && serviceKey == null && status == null && operatorName == null){
			logger.info("cannot update as all param are null");
			return null;
		}
		if (status != null) {
			query += STATUS_ID_COL + " = '" + status + "' ,";
		}

		if (serviceKey != null) {
			query +=  SERVICE_KEY_ID_COL + " = '" + serviceKey + "' ,";
		}

		if (operatorName != null) {
			query += OPERATOR_NAME_COL + " = '" + operatorName + "' ,";
		}
		
		if (circleID != null) {
			query += CIRCLE_ID_COL + " = '" + circleID + "' ,";
		}
		query = query.substring(0, query.length() - 1);
		query += "WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query, Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query);
		if (n == 1) {
			return new OperatorUserDetailsImpl(subscriberID, serviceKey, status ,operatorName ,circleID);
		}
		return null;
	}

	private static int executeUpdateQuery(Connection conn, String query) {
		int updateCount = 0;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		} catch (SQLException se) {
			logger.error("", se);
			return updateCount;
		} finally {
			closeStatementAndRS(stmt, null);
		}
		return updateCount;
	}

	public static List<OperatorUserDetails> getAllUserDetails(Connection conn, int limit) {

		Statement stmt = null;
		List<OperatorUserDetails> operatorUserDetails = new ArrayList<OperatorUserDetails>();
		ResultSet results = null;
		String lowerLimit = String.valueOf(limit*1000);
		String upperLimit = String.valueOf((limit+1)*1000);
		
		String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+ SUBSCRIBER_ID_COL+ " LIMIT " + lowerLimit +" , "+ upperLimit;

		logger.info("Executing the query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next()) {
				operatorUserDetails.add(getUserDetailsFromRS(results));
			}

			return operatorUserDetails;
		} catch (SQLException se) {
			logger.error("", se);
			return null;
		} finally {
			closeStatementAndRS(stmt, results);
		}
	}

}
