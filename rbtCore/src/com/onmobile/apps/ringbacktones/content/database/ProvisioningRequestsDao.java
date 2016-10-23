/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;

/**
 * @author Sreekar
 *
 */
public class ProvisioningRequestsDao extends RBTPrimitive {

	private static final Logger logger = Logger.getLogger(ProvisioningRequestsDao.class);
	
	private static final String TABLE_NAME = "RBT_PROVISIONING_REQUESTS";
	private static final String COL_SUBSCRIBER_ID = "SUBSCRIBER_ID";
	private static final String COL_TYPE = "TYPE";
	private static final String COL_MODE = "MODE";
	private static final String COL_MODE_INFO = "MODE_INFO";
	private static final String COL_TRANS_ID = "TRANS_ID";
	private static final String COL_REQUEST_ID = "REQUEST_ID";
	private static final String COL_STATUS = "STATUS";
	private static final String COL_CHARGING_CLASS = "CHARGING_CLASS";
	private static final String COL_CREATION_TIME = "CREATION_TIME";
	private static final String COL_NEXT_RETRY_TIME = "NEXT_RETRY_TIME";
	private static final String COL_RETRY_COUNT = "RETRY_COUNT";
	private static final String COL_EXTRA_INFO = "EXTRA_INFO";
	private static final String COL_NUM_MAX_SELECTIONS = "NUM_MAX_SELECTIONS";
	private static final String COL_SM_SUBSCRIPTION_VALIDITY_STATUS = "SM_SUBSCRIPTION_VALIDITY_STATUS";
	private static final int BASE_ACTIVATION_PENDING = 30;
	private static final int PACK_TO_BE_ACTIVATED = 31;
	private static final int PACK_ACTIVATION_PENDING = 32;
	private static final int PACK_ACTIVATED = 33;
	private static final int PACK_ACTIVATION_ERROR = 34;
	private static final int PACK_SUSPEND = 35;
	private static final int PACK_GRACE = 36;
	private static final int PACK_TO_BE_DEACTIVATED = 41;
	private static final int PACK_DEACTIVATION_PENDING = 42;
	private static final int PACK_DEACTIVATED = 43;
	private static final int PACK_DEACTIVATION_ERROR = 44;
    private static final int PACK_ODA_REFRESH = 50;
	
	public static final int TYPE_ARBT = 1;
	
	public static final int STATUS_NEW = 1;
	
	public static ProvisioningRequests createProvisioningRequest(ProvisioningRequests provisioningRequest) {
		Date createDate = new Date();
		provisioningRequest.setCreationTime(createDate);
		
		String createTime = RBTPrimitive.mySqlTime(createDate);
		if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
			createTime = RBTPrimitive.sqlTime(createDate);
		
		String nextRetryTime = RBTPrimitive.mySqlTime(provisioningRequest.getNextRetryTime());
		if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
			nextRetryTime = RBTPrimitive.sqlTime(provisioningRequest.getNextRetryTime());
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO ").append(TABLE_NAME).append(" (");
		addToQuery(query, COL_SUBSCRIBER_ID, true);
		addToQuery(query, COL_TYPE, false);
		addToQuery(query, COL_MODE, false);
		addToQuery(query, COL_MODE_INFO, false);
		addToQuery(query, COL_TRANS_ID, false);
		addToQuery(query, COL_STATUS, false);
		addToQuery(query, COL_CREATION_TIME, false);
		addToQuery(query, COL_CHARGING_CLASS, false);
		addToQuery(query, COL_NEXT_RETRY_TIME, false);
		addToQuery(query, COL_RETRY_COUNT, false);
		addToQuery(query, COL_EXTRA_INFO, false);
		addToQuery(query, COL_NUM_MAX_SELECTIONS, false);

		query.append(") VALUES (");
		addToQuery(query, sqlString(provisioningRequest.getSubscriberId()), true);
		addToQuery(query, provisioningRequest.getType() + "", false);
		addToQuery(query, sqlString(provisioningRequest.getMode()), false);
		addToQuery(query, sqlString(provisioningRequest.getModeInfo()), false);
		addToQuery(query, sqlString(provisioningRequest.getTransId()), false);
		addToQuery(query, provisioningRequest.getStatus() + "", false);
		addToQuery(query, createTime, false);
		addToQuery(query, sqlString(provisioningRequest.getChargingClass()), false);
		addToQuery(query, nextRetryTime, false);
		addToQuery(query, provisioningRequest.getRetryCount() + "", false);
		addToQuery(query, sqlString(provisioningRequest.getExtraInfo()), false);
		addToQuery(query, provisioningRequest.getNumMaxSelections() + "", false);
		query.append(")");

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		
		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
			if(rowsUpdated > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next())
					provisioningRequest.setRequestId(rs.getLong(1));
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn, stmt, null);
		}

		if(rowsUpdated > 0)
			return provisioningRequest;
		return null;
	}
	
	public static boolean removeByRequestId(String subscriberId, long requestId) {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberId));
		query.append(" AND ");
		query.append(COL_REQUEST_ID);
		query.append(" = ");
		query.append(requestId);

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, null);
			}
			catch (Exception e) {
			}
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}

	public static boolean removeBySubscriberIDAndType(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		
		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, null);
			}
			catch (Exception e) {
			}
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean removeBySubscriberID(String subscriberID) {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);

		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, null);
			}
			catch (Exception e) {
			}
		}

		if (rowsUpdated > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean updateRequest(String subscriberID, String refId , String status , boolean appendNextChargeTime, int numMaxSelections)
	{
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(sqlString(status));
		if(appendNextChargeTime)
		{
			String nextDate = "SYSDATE";
			if(!getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
			{
				nextDate = "SYSDATE()";
			}
			query.append(" , ");
			query.append(COL_NEXT_RETRY_TIME);
			query.append(" = ");
			query.append(nextDate);

			
		}

		if (numMaxSelections >= 0)
		{
			query.append(" , ");
			query.append(COL_NUM_MAX_SELECTIONS);
			query.append(" = ");
			query.append(String.valueOf(numMaxSelections));
		}

		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(refId));
		

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);

		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, null);
			}
			catch (Exception e) {
			}
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean updateOnActFailure(String subscriberID, String cosID , String status , boolean appendNextChargeTime)
	{
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(sqlString(status));
		if(appendNextChargeTime)
		{
			String nextDate = "SYSDATE";
			if(!getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
			{
				nextDate = "SYSDATE()";
			}
			query.append(" , ");
			query.append(COL_NEXT_RETRY_TIME);
			query.append(" = ");
			query.append(nextDate);

			
		}
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(sqlString(cosID));
		

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);

		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean deactivateRequest(String subscriberID, String refId , String status, String extraInfo)
	{
		
		StringBuilder query = new StringBuilder();
		String nextDate = "TO_DATE('20371231','yyyyMMdd')";
		if(!getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "TIMESTAMP('2037-12-31')";
		}
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(sqlString(status));
		query.append(" , ");
		query.append(COL_NEXT_RETRY_TIME);
		query.append(" = ");
		query.append(nextDate);
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append("'" + extraInfo + "'");
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(refId));
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);

		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		return rowsUpdated > 0 ? true : false;
	}
	
	static boolean updateNextRetryTime(String subscriberID, String refID, Date nextChargingDate , String classType, int cosID)
	{
		String nextDate = "SYSDATE";
		if(nextChargingDate != null)
			nextDate = sqlTime(nextChargingDate);

		
		if(!getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE()";
			if(nextChargingDate != null)
				nextDate = mySQLDateTime(nextChargingDate);
		}
		
		
		String query = "UPDATE " + TABLE_NAME + " SET " +
		COL_STATUS + " = '"+PACK_ACTIVATED+"', " +
		COL_NEXT_RETRY_TIME + " = " + nextDate + ", " + 
		COL_CHARGING_CLASS + " = " + sqlString(classType) + ", " + 
		COL_NUM_MAX_SELECTIONS + " = 0 "  ;

		if (cosID > -1)
			query += ", " + COL_TYPE + " = " + cosID;

		query += " WHERE " + COL_SUBSCRIBER_ID  + " = " + "'" + subscriberID + "' AND " + COL_TRANS_ID + " = "+ sqlString(refID)+	
		" AND " + COL_STATUS + " in ('"+PACK_ACTIVATION_PENDING+"' ,'"+PACK_TO_BE_ACTIVATED+"' ,'"+PACK_ACTIVATED+"' , '"+PACK_SUSPEND+"','"+PACK_ACTIVATION_ERROR+"')";


		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);

		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
		
	}


//	public static ProvisioningRequests getByRequestId(long requestId) {
//		StringBuilder query = new StringBuilder();
//		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
//		query.append(COL_REQUEST_ID);
//		query.append(" = ");
//		query.append(requestId);
//
//		logger.info("query: " + query);
//
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		ProvisioningRequests returnValue = null;
//		
//		try
//		{
//			conn = getConnection();
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(query.toString());
//			if(rs.next())
//				returnValue = getObjectFromRS(rs);
//		}
//		catch (Throwable e)
//		{
//			logger.error("", e);
//		}
//		finally
//		{
//			releaseConnection(conn, stmt, rs);
//		}
//		return returnValue;
//	}
	
	public static ProvisioningRequests getByTransId(String subscriberId, String transId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberId));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(transId));

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ProvisioningRequests returnValue = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			if(rs.next()) {
				returnValue = getObjectFromRS(rs);
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		return returnValue;
	}
	
	public static List<ProvisioningRequests> getBySubscriberId(String subscriberId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberId));

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try 
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next())
				returnList.add(getObjectFromRS(rs));
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		logger.info("Returning null, no records found");
		return null;
	}
	
	public static List<ProvisioningRequests> getBySubscriberIdAndStatus(String subscriberId, String status) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberId));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(sqlString(status));
		
		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try 
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next())
				returnList.add(getObjectFromRS(rs));
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		logger.info("Returning null, no records found");
		return null;
	}

	
	public static List<ProvisioningRequests> getByTypeStatus(int type, int status, int fetchSize) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(status);

		logger.info("query: " + query);

		if (fetchSize > 0)
			query.append(" LIMIT ").append(fetchSize);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}

	public static List<ProvisioningRequests> getBySubscriberIDAndType(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to retrieve. SQLException: " + e.getMessage(),
					e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		logger.info("Returning provisioning requests list: " + returnList);
		return returnList;
	}
	
	
	public static List<ProvisioningRequests> getActiveProvisioningBySubscriberID(String subscriberID)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(PACK_ACTIVATED);
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to retrieve. SQLException: " + e.getMessage(),
					e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		logger.info("Returning provisioning requests list: " + returnList);
		return returnList;
	}

	
	public static List<ProvisioningRequests> getActiveProvisioningBySubscriberIDAndType(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(PACK_ACTIVATED);
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to retrieve. SQLException: " + e.getMessage(),
					e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		logger.info("Returning provisioning requests list: " + returnList);
		return returnList;
	}
	
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public static List<ProvisioningRequests> getActAndActPendingProvisioningBySubIDAndType(
			String subscriberID, int type) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND " + COL_STATUS + " IN (" + PACK_ACTIVATED + ","
				+ PACK_ACTIVATION_PENDING + ") ");
		query.append(" ORDER BY REQUEST_ID");
		logger.info("query: " + query);
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve. SQLException: " + e.getMessage(),
					e);
		} finally {
			try {
				releaseConnection(conn, stmt, rs);
			} catch (Exception e) {
			}
		}
		logger.info("Returning provisioning requests list: " + returnList);
		return returnList;
	}
	
	public static List<ProvisioningRequests> getPacksToBeActivatedBySubscriberIDAndType(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND "+COL_STATUS+" IN ("+PACK_TO_BE_ACTIVATED+","+BASE_ACTIVATION_PENDING+") ");
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	public static List<ProvisioningRequests> getBySubscriberIDAndTypeOrderByCreationTime(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" ORDER BY ");
		query.append(COL_CREATION_TIME);

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	
	public static List<ProvisioningRequests> getBySubscriberIDTypeAndNonDeactivatedStatus(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN ");
		query.append("(").append(iRBTConstant.BASE_ACTIVATION_PENDING)
				.append(", ").append(iRBTConstant.PACK_TO_BE_ACTIVATED)
				.append(", ").append(iRBTConstant.PACK_ACTIVATION_PENDING)
				.append(", ").append(iRBTConstant.PACK_DEACTIVATION_PENDING)
				.append(", ").append(iRBTConstant.PACK_TO_BE_DEACTIVATED)
				.append(", ").append(iRBTConstant.PACK_SUSPEND)
				.append(", ").append(iRBTConstant.PACK_ACTIVATED).append(", ")
				.append(iRBTConstant.PACK_ACTIVATION_ERROR).append(", ")
				.append(iRBTConstant.PACK_GRACE + ")");
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	public static List<ProvisioningRequests> getBySubscriberIDAndStatus(String subscriberID)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN("+PACK_ACTIVATED+","+BASE_ACTIVATION_PENDING+") ");
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try 
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next())
				returnList.add(getObjectFromRS(rs));
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}

	public static List<ProvisioningRequests> getActiveODAPackBySubscriberID(String subscriberID)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN(" );
		query.append(iRBTConstant.PACK_BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_ACTIVATED +",");
		query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
		query.append(iRBTConstant.PACK_GRACE + ",");
		query.append(iRBTConstant.PACK_ODA_REFRESH + ",");
		query.append(iRBTConstant.PACK_SUSPENDED + ")");

		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try 
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next())
				returnList.add(getObjectFromRS(rs));
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}

	private static ProvisioningRequests getObjectFromRS(ResultSet rs) throws SQLException {
		long requestId = rs.getLong(COL_REQUEST_ID);
		String subscriberId = rs.getString(COL_SUBSCRIBER_ID);
		int type = rs.getInt(COL_TYPE);
		String extraInfo = rs.getString(COL_EXTRA_INFO);
		String mode = rs.getString(COL_MODE);
		String modeInfo = rs.getString(COL_MODE_INFO);
		String transId = rs.getString(COL_TRANS_ID);
		int status = rs.getInt(COL_STATUS);
		Date createTime = rs.getTimestamp(COL_CREATION_TIME);
		String chargingClass = rs.getString(COL_CHARGING_CLASS);
		Date nextRetryTime = rs.getTimestamp(COL_NEXT_RETRY_TIME);
		String retryCount = rs.getString(COL_RETRY_COUNT);
		int numMaxSels = rs.getInt(COL_NUM_MAX_SELECTIONS);

		ProvisioningRequests pr = new ProvisioningRequests(chargingClass, createTime, extraInfo, mode, modeInfo, nextRetryTime, requestId,
				retryCount, status, subscriberId, transId, type);
		pr.setNumMaxSelections(numMaxSels);
		return pr;
	}

	public static boolean deactivateAllPacks(String subscriberID, String packExtraInfo,boolean isReactivationSupported, boolean isStatusCheckRequired) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		if (isReactivationSupported) {
			query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		} else {
			query.append(iRBTConstant.PACK_DEACTIVATED);
		}
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		
		// RBT 19124 
		if(isStatusCheckRequired){
			query.append(" AND " + COL_STATUS + " IN (");
			query.append(iRBTConstant.BASE_ACTIVATION_PENDING + ",");
			query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ",");
			query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
			query.append(iRBTConstant.PACK_ACTIVATED + ",");
			query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
			query.append(iRBTConstant.PACK_GRACE + ",");
			query.append(iRBTConstant.PACK_SUSPENDED + ")");
		}

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);
	}

	public static boolean deactivateActiveODAPack(String subscriberID, String internalRefId, String packExtraInfo){
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));
		
		query.append(" WHERE ");
		if(internalRefId != null) {
			query.append(COL_TRANS_ID);
			query.append(" = '");
			query.append(internalRefId + "' ");
		}
		
		query.append(" AND " + COL_STATUS + " IN (");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_ACTIVATED +",");
		query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
		query.append(iRBTConstant.PACK_GRACE + ",");
		query.append(iRBTConstant.PACK_SUSPENDED + ")");

		logger.info("deactivateActiveODAPack query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);

	}

	public static boolean directDeactivateODAPack(String subscriberID, String internalRefId, String packExtraInfo){
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_DEACTIVATED);
		
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));
		
		query.append(" WHERE ");
		if(internalRefId != null) {
			query.append(COL_TRANS_ID);
			query.append(" = '");
			query.append(internalRefId + "' ");
		}
		
		query.append(" AND " + COL_STATUS + " IN (");
		query.append(iRBTConstant.PACK_BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ")");

		logger.info("deactivateActiveODAPack query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);

	}
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public static boolean directDeactivatePack(String subscriberID,
			String internalRefId, String packExtraInfo) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_DEACTIVATED);

		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));

		query.append(" WHERE ");
		if (internalRefId != null) {
			query.append(COL_TRANS_ID);
			query.append(" = '");
			query.append(internalRefId + "' ");
		}
		query.append(" AND " + COL_STATUS + " IN (");
		query.append(iRBTConstant.PACK_BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ")");

		logger.info("deactivateActiveODAPack query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);

	}
	
	public static boolean deactivateODAPack(String subscriberID, String categoryID, String internalRefId, String packExtraInfo,String callerId){
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));

		if(categoryID != null) {
			query.append(" AND ");		
			query.append(COL_TYPE);
			query.append(" = ");
			query.append(categoryID);
		}

		if(internalRefId != null) {
			query.append(" AND ");		
			query.append(COL_TRANS_ID);
			query.append(" = '");
			query.append(internalRefId + "' ");
		}
		
		query.append(" AND " + COL_STATUS + " IN (");
		query.append(iRBTConstant.BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
		query.append(iRBTConstant.PACK_GRACE + ",");
		query.append(iRBTConstant.PACK_SUSPENDED + ")");

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);

	}
	
	public static boolean deactivatePack(String subscriberID, String packCosId, String internalRefId, String packExtraInfo) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(packExtraInfo));
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		
		if(packCosId != null) {
			query.append(" AND ");		
			query.append(COL_TYPE);
			query.append(" = ");
			query.append(packCosId);
		}
		
		if(internalRefId != null) {
			query.append(" AND ");		
			query.append(COL_TRANS_ID);
			query.append(" = '");
			query.append(internalRefId + "' ");
		}
		
		query.append(" AND " + COL_STATUS + " IN (");
		query.append(iRBTConstant.BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
		query.append(iRBTConstant.PACK_GRACE + ",");
		query.append(iRBTConstant.PACK_SUSPENDED + ")");

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(null, query.toString());
		return (n > 0);
	}

	public static boolean smUpdatePackStatusOnBaseAct(Connection conn, String subID) {
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN ('" );
		query.append(iRBTConstant.BASE_ACTIVATION_PENDING);
		query.append( "' )");
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query.toString());
		return (n > 0);
	}
	
	public static boolean smURLPackActivation(Connection conn, String subID) {
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN ('" );
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED);
		query.append( "' )");
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(),Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query.toString());
		return (n > 0);
	}
	
public static boolean smURLPackActivationOnBaseAct(Connection conn, String subID,String cosID) {
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(sqlString(cosID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN (" );
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED+","+iRBTConstant.BASE_ACTIVATION_PENDING);
		query.append( " )");
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query.toString());
		return (n > 0);
	}
	
	
	public static boolean smUpdatePackStatusOnBaseDeact(Connection conn, String subID) {
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN ('" );
		query.append(iRBTConstant.PACK_ACTIVATED);
		query.append("', '" );
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING);
		query.append("', '" );
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED);
		query.append("', '" );
		query.append(iRBTConstant.BASE_ACTIVATION_PENDING);
		query.append( "' )");
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query.toString());
		return (n > 0);
	}
	
	public static boolean smURLPackDeactivation(Connection conn, String subID) {
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_DEACTIVATION_PENDING);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN ('" );
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);
		query.append( "' )");
		
		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int n = executeUpdateQuery(conn, query.toString());
		return (n > 0);
	}

	public static ProvisioningRequests[] smGetActivatedPacks(Connection conn, int fetchSize) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED);

		logger.info("query: " + query);

		if (fetchSize > 0)
			query.append(" LIMIT ").append(fetchSize);
		
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try
		{
			if(conn==null)
			{
				conn = getConnection();
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return convertProvisioningReqListToArray(returnList);
		}
		return null;
	}
	
	public static ProvisioningRequests[] smGetDeactivatedPacks(Connection conn, int fetchSize) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED);

		logger.info("query: " + query);

		if (fetchSize > 0)
			query.append(" LIMIT ").append(fetchSize);
		
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			if(conn==null){
				conn = getConnection();
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return convertProvisioningReqListToArray(returnList);
		}
		return null;
	}
	
	private static int executeUpdateQuery(Connection conn, String query)
	{
		boolean releaseConn = false;
		if(conn == null)
		{
			conn = getConnection();
			if(conn != null)
				releaseConn = true;
			else
				return 0;
		}
		
		int updateCount = 0;
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return updateCount;
		}
		finally
		{
			if(releaseConn)
				releaseConnection(conn, stmt, null);
			else
				closeStatementAndRS(stmt, null);
		}
		return updateCount;
	}

	private static ProvisioningRequests[] convertProvisioningReqListToArray(List<ProvisioningRequests> packList) {
		if (packList.size() > 0) {
			return (ProvisioningRequests[]) packList.toArray(new ProvisioningRequests[0]);
		} else {
			return null;
		}
	}

	public static void updateNumMaxSelections(Connection conn, String subscriberID, String cosID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + COL_NUM_MAX_SELECTIONS + " = "
		+ COL_NUM_MAX_SELECTIONS + " + 1 WHERE " + COL_SUBSCRIBER_ID + " = " + "'"
		+ subscriberID + "' AND " + COL_TYPE + " = " + cosID;

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	public static void decrementNumMaxSelections(Connection conn, String subscriberID, String cosID)
	{
		String query = "UPDATE " + TABLE_NAME + " SET " + COL_NUM_MAX_SELECTIONS + " = "
		+ COL_NUM_MAX_SELECTIONS + " - 1 WHERE " + COL_SUBSCRIBER_ID + " = " + "'"
		+ subscriberID + "' AND " + COL_TYPE + " = " + cosID;

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		executeUpdateQuery(conn, query);
		return;
	}

	public static boolean updateExtraInfo(String subscriberID, String refId, String extraInfo)
	{
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(extraInfo));
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(refId));

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		Connection conn = null;
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, null);
			}
			catch (Exception e) {
			}
		}

		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}

	public static boolean updateRetryCountAndTime(Connection conn, String subscriberID, String refID, String retryCount, Date retryTime)
	{
		String query = null;
		if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + COL_RETRY_COUNT + " = "
					+ sqlString(retryCount) + " , " + COL_NEXT_RETRY_TIME
					+ " = " + sqlTime(retryTime) + " WHERE "
					+ COL_SUBSCRIBER_ID + " = " + "'" + subscriberID + "'"
					+ " AND " + COL_TRANS_ID + " = " + sqlString(refID);
		}
		else {
			query = "UPDATE " + TABLE_NAME + " SET " + COL_RETRY_COUNT + " = "
					+ sqlString(retryCount) + " , " + COL_NEXT_RETRY_TIME
					+ " = " + mySQLDateTime(retryTime) + " WHERE "
					+ COL_SUBSCRIBER_ID + " = " + "'" + subscriberID + "'"
					+ " AND " + COL_TRANS_ID + " = " + sqlString(refID);
		}

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int count = executeUpdateQuery(conn, query);
		return (count > 0);
	}

	public static boolean updateSubscriberID(Connection conn,String newSubscriberID,String subscriberID){
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(newSubscriberID);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(subscriberID);
		Statement stmt = null;
		int rowsUpdated = 0;
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		try {
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		} catch (SQLException e){
			logger.error("", e);
		}
		if(rowsUpdated>0)
			return true;

		return false;
	}
	
	//RBT-11752
	public static List<ProvisioningRequests> getByStatus(Connection conn,int status) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(status);
		    
		logger.info("query: " + query);
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		try 
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		logger.info("Returning null, no records found");
		return null;
	}
	
	public static boolean updateStatus(Connection conn,String subscriberID, String refId, int status)
	{
		
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(status);
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(refId));

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		
		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean updateRequestStatusAndExtraInfo(Connection conn,String subscriberID, String refId, int status,String extraInfo){
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(status);
		query.append(" , ");
		query.append(COL_EXTRA_INFO);
		query.append(" = ");
		query.append(sqlString(extraInfo));
		query.append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TRANS_ID);
		query.append(" = ");
		query.append(sqlString(refId));

		logger.info("query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		Statement stmt = null;
		int rowsUpdated = 0;

		try {
			stmt = conn.createStatement();
			rowsUpdated = stmt.executeUpdate(query.toString());
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		
		if(rowsUpdated > 0) {
			return true;
		}
		return false;
	
	}
	
	
	public static List<ProvisioningRequests> getActiveProvisioningByType(String type[], int retryCount , int smSubStatus)
	{
		StringBuilder query = new StringBuilder();
	
		String nextDate = "SYSDATE";
		if(!getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
		{
			nextDate = "SYSDATE()";
		}
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(PACK_ACTIVATED);
		if(type != null && type.length > 0){
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" IN ( ");
		query.append(type[0]);
		for (int i = 1; i < type.length; i++) {
			query.append(" , "+type[i]);
		}
		query.append(" ) ");
		}
		
		query.append(" AND ( ");
		query.append(COL_RETRY_COUNT);
		query.append(" < ");
		query.append(retryCount);
		query.append(" OR "+ COL_RETRY_COUNT +" IS NULL ) ");
		query.append(" AND ");
		query.append(COL_SM_SUBSCRIPTION_VALIDITY_STATUS);
		query.append(" = ");
		query.append(smSubStatus);
		query.append(" AND ");
		query.append(COL_NEXT_RETRY_TIME);
		query.append(" < ");
		query.append(nextDate);
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to retrieve. SQLException: " + e.getMessage(),
					e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		logger.info("Returning provisioning requests list: " + returnList);
		return returnList;
	}

	public static boolean updateSmStatusRetryCountAndTime( String subscriberID, String refID, String retryCount, Date retryTime, int smStatus)
 {
		Connection conn = getConnection();
		String query = null;
		if (getDBSelectionString().equalsIgnoreCase(DB_SAPDB)) {
			query = "UPDATE " + TABLE_NAME + " SET " + COL_RETRY_COUNT + " = " + sqlString(retryCount) + " , "
					+ COL_NEXT_RETRY_TIME + " = " + sqlTime(retryTime) + " , " + COL_SM_SUBSCRIPTION_VALIDITY_STATUS + " = "
					+ sqlInt(smStatus) + " WHERE " + COL_SUBSCRIBER_ID + " = " + "'" + subscriberID + "'" + " AND "
					+ COL_TRANS_ID + " = " + sqlString(refID);
		} else {
			query = "UPDATE " + TABLE_NAME + " SET " + COL_RETRY_COUNT + " = " + sqlString(retryCount) + " , "
					+ COL_NEXT_RETRY_TIME + " = " + mySQLDateTime(retryTime) + " , " + COL_SM_SUBSCRIPTION_VALIDITY_STATUS + " = "
					+ sqlInt(smStatus) + " WHERE " + COL_SUBSCRIBER_ID + " = " + "'" + subscriberID + "'" + " AND "
					+ COL_TRANS_ID + " = " + sqlString(refID);
		}

		logger.info("Executing the query: " + query);
		RBTEventLogger.logEvent(RBTEventLogger.Event.DML_BACKUP_LOG, query.toString(), Constants.SQL_TYPE_LOGGER);
		int count = executeUpdateQuery(conn, query);
		return (count > 0);
	}
	
	
	public static List<ProvisioningRequests> getPacksToBeActivatedBySubscriberIDAndActpendingType(String subscriberID, int type)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND "+COL_STATUS+" IN ("+PACK_TO_BE_ACTIVATED+","+PACK_ACTIVATION_PENDING+") ");
		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	
	public static List<ProvisioningRequests> getBySubscriberIdTypeAndStatus(String subscriberID ,int type, int status, int fetchSize) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" = ");
		query.append(status);

		logger.info("query: " + query);

		if (fetchSize > 0)
			query.append(" LIMIT ").append(fetchSize);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next()) {
				returnList.add(getObjectFromRS(rs));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				releaseConnection(conn, stmt, rs);
			}
			catch (Exception e) {
			}
		}
		
		if(returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	
	public static List<ProvisioningRequests> getActiveODAPackBySubscriberIDAndType(String subscriberID , int type )
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_TYPE);
		query.append(" = ");
		query.append(type);
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN(" );
		query.append(iRBTConstant.PACK_BASE_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_TO_BE_ACTIVATED + ",");
		query.append(iRBTConstant.PACK_ACTIVATION_PENDING + ",");
		query.append(iRBTConstant.PACK_ACTIVATED +",");
		query.append(iRBTConstant.PACK_ACTIVATION_ERROR + ",");
		query.append(iRBTConstant.PACK_GRACE + ",");
		query.append(iRBTConstant.PACK_ODA_REFRESH + ",");
		query.append(iRBTConstant.PACK_SUSPENDED + ")");

		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();
		
		try 
		{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while(rs.next())
				returnList.add(getObjectFromRS(rs));
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
				releaseConnection(conn, stmt, rs);
		}
		
		if(returnList.size() > 0)
		{
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	public static List<ProvisioningRequests> getDeactiveODAPackBySubscriberID(
			String subscriberID) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN(");
		query.append(iRBTConstant.PACK_DEACTIVATED + ",");
		query.append(iRBTConstant.PACK_DEACTIVATION_ERROR + ")");

		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next())
				returnList.add(getObjectFromRS(rs));
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			releaseConnection(conn, stmt, rs);
		}

		if (returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}
	
	public static List<ProvisioningRequests> getDeactivePendingODAPackBySubscriberID(
			String subscriberID) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ");
		query.append(COL_SUBSCRIBER_ID);
		query.append(" = ");
		query.append(sqlString(subscriberID));
		query.append(" AND ");
		query.append(COL_STATUS);
		query.append(" IN(");
		query.append(iRBTConstant.PACK_TO_BE_DEACTIVATED + ",");
		query.append(iRBTConstant.PACK_DEACTIVATION_PENDING + ")");

		query.append(" ORDER BY REQUEST_ID");

		logger.info("query: " + query);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<ProvisioningRequests> returnList = new ArrayList<ProvisioningRequests>();

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next())
				returnList.add(getObjectFromRS(rs));
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			releaseConnection(conn, stmt, rs);
		}

		if (returnList.size() > 0) {
			logger.info("returning " + returnList.size() + " elements");
			return returnList;
		}
		return null;
	}

}