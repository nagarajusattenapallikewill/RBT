package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.GCMRegistration;

/**
 * @author sridhar.sindiri
 *
 */
public class GCMRegistrationImpl extends RBTPrimitive implements GCMRegistration
{
	private static final Logger logger = Logger.getLogger(GCMRegistrationImpl.class);

	private static final String TABLE_NAME			= "RBT_GCM_REGISTRATIONS";
	private static final String REGISTRATION_ID_COL = "REGISTRATION_ID";
	private static final String SUBSCRIBER_ID_COL 	= "SUBSCRIBER_ID";
	private static final String OS_TYPE_COL 	= "OS_TYPE";
	private static final String NOTIFICATION_ENABLED_COL 	= "NOTIFICATION_ENABLED";

	public String registrationID;
	public String subscriberID;
	public String os_type;
	public String notificationEnabled;

	/**
	 * @param registrationID
	 * @param subscriberID
	 */
	public GCMRegistrationImpl(String registrationID, String subscriberID)
	{
		this.registrationID = registrationID;
		this.subscriberID = subscriberID;
	}

	public GCMRegistrationImpl(String registrationID, String subscriberID, String os_type)
	{
		this.registrationID = registrationID;
		this.subscriberID = subscriberID;
		this.os_type = os_type;
	}

	@Override
	public String registrationID() {
		return registrationID;
	}

	@Override
	public String subscriberID() {
		return subscriberID;
	}

	@Override
	public String os_type() {
		return os_type;
	}

	@Override
	public String notificationEnabled() {
		return notificationEnabled;
	}

	/**
	 * @param conn
	 * @param registrationID
	 * @param subscriberID
	 * @param type
	 * @return
	 */
	static boolean insert(Connection conn, String registrationID, String subscriberID, String type, String notificationEnabled)
	{
		Statement stmt = null;

		int id = -1;
		if(type == null) {
			type = "android"; 
		}
		String sql = "INSERT INTO " + TABLE_NAME + "(" + REGISTRATION_ID_COL + ", " + SUBSCRIBER_ID_COL + ", " + OS_TYPE_COL ;
		if(notificationEnabled != null) {
			sql += ", " + NOTIFICATION_ENABLED_COL;
		}		
		sql += ") VALUES(";
		sql += sqlString(registrationID) + ", ";
		sql += sqlString(subscriberID) + ", ";
		sql += sqlString(type);
		
		if(notificationEnabled != null) {
			sql += ", " + notificationEnabled;
		}
	    sql += ")";

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(sql);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
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
				logger.error(e.getMessage(), e);
			}
		}

		return (id > 0);
	}

	/**
	 * @param conn
	 * @param registrationID
	 * @param subscriberID
	 * @param type
	 * @return true if success. Else returns null.
	 */
	static Boolean setNotificationStatus(Connection conn, String subscriberID, String notificationEnabled, String osType, boolean toUpdateRegId, String regID)
	{
		Statement stmt = null;

		int id = -1;

		if (osType == null) {
			osType = "android";
		}
		
		if (notificationEnabled != null && subscriberID != null) {
			String sql = "UPDATE " + TABLE_NAME + " SET "
					+ NOTIFICATION_ENABLED_COL + " = " + notificationEnabled;
			
			if(toUpdateRegId && regID != null) {
				sql += ", " + REGISTRATION_ID_COL + " = " + sqlString(regID);
			}
			sql +=	 " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID) + " AND " + OS_TYPE_COL + " = " + sqlString(osType);
			logger.info("RBT:: query - " + sql);
			try {
				stmt = conn.createStatement();
				id = stmt.executeUpdate(sql);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return id > 0 ? true : null;
	}
	/**
	 * @param conn
	 * @param registrationID
	 * @param subscriberID
	 * @return
	 */
	static boolean deleteByRegistrationIDAndSubscriberID(Connection conn, String registrationID, String subscriberID)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + REGISTRATION_ID_COL
				+ " = " + sqlString(registrationID) + " AND " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subscriberID);

		logger.info("RBT::query " + query);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch (SQLException se)
		{
			logger.error(se.getMessage(), se);
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
				logger.error(e.getMessage(), e);
			}
		}

		return (n > 0);
	}

	static GCMRegistration[] getAllGCMRegistrationIDs(Connection conn, String osType)
	{
		Statement stmt = null;
		ResultSet rs = null;

		GCMRegistration gcmRegistration = null;
		List<GCMRegistration> gcmRegistrationsList = new ArrayList<GCMRegistration>();
		logger.info("RBT:: osType - " + osType);
		if(osType == null ) {
			osType = "android";
		}

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "+ OS_TYPE_COL+ "= "+sqlString(osType);

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			String subID;
			String regID;
			while (rs.next())
			{
				subID = rs.getString(SUBSCRIBER_ID_COL);
				regID = rs.getString(REGISTRATION_ID_COL);

				gcmRegistration = new GCMRegistrationImpl(regID, subID);
				gcmRegistrationsList.add(gcmRegistration);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}

		if (gcmRegistrationsList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_GCM_REGISTRATIONS successful");
			return gcmRegistrationsList.toArray(new GCMRegistration[0]);
		} 

		return null;
	}

	static GCMRegistration[] getAllGCMRegistrationIDs(Connection conn, int offset, int pagecount, String osType)
	{
		Statement stmt = null;
		ResultSet rs = null;

		GCMRegistration gcmRegistration = null;
		List<GCMRegistration> gcmRegistrationsList = new ArrayList<GCMRegistration>();
		if(osType == null ) {
			osType = "android";
		}

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "+ OS_TYPE_COL+ "= "+sqlString(osType)+ " LIMIT " + offset + "," + pagecount;

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			String subID;
			String regID;
			while (rs.next())
			{
				subID = rs.getString(SUBSCRIBER_ID_COL);
				regID = rs.getString(REGISTRATION_ID_COL);

				gcmRegistration = new GCMRegistrationImpl(regID, subID);
				gcmRegistrationsList.add(gcmRegistration);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			closeStatementAndRS(stmt, rs);
		}

		if (gcmRegistrationsList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_GCM_REGISTRATIONS successful");
			return gcmRegistrationsList.toArray(new GCMRegistration[0]);
		} 

		return null;
	}

	/**
	 * 
	 * @param conn
	 * @param subscriberId
	 * @param osType
	 * @return Notification status. Will be null, if record not found.
	 */
	static Boolean getNotificationStatus(Connection conn, String subscriberId, String osType) {
		Statement stmt = null;
		ResultSet rs = null;

		logger.info("RBT:: subscriberId - " + subscriberId);
		if(subscriberId == null ) {
			return null;
		}
		if(osType == null ) {
			osType = "android";
		}
		String sql = "SELECT " + NOTIFICATION_ENABLED_COL + " FROM " + TABLE_NAME + " WHERE "+ SUBSCRIBER_ID_COL + "= " + sqlString(subscriberId) 
				+ " AND " + OS_TYPE_COL + " = " + sqlString(osType);

		logger.info("RBT:: query - " + sql);
		Boolean notificationStatus = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				notificationStatus = rs.getBoolean(NOTIFICATION_ENABLED_COL);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			closeStatementAndRS(stmt, rs);
		}
		logger.info("Notification status returned: " + notificationStatus + " for subscriberId: "  + subscriberId);
		return notificationStatus;
	}
	
	/**
	 * 
	 * @param conn
	 * @param subscriberId
	 * @param osType - Default value - android
	 * @return Registration ID. Will be null, if record not found.
	 */
	static String getRegistrationIdBySubscriberIdAndType(Connection conn, String subscriberId, String osType) {
		Statement stmt = null;
		ResultSet rs = null;

		logger.info("RBT:: subscriberId - " + subscriberId);
		if(subscriberId == null ) {
			return null;
		}

		if (osType == null) {
			osType = "android";
		}
		String sql = "SELECT " + REGISTRATION_ID_COL + " FROM " + TABLE_NAME + " WHERE "+ SUBSCRIBER_ID_COL + " = " 
				+ sqlString(subscriberId) + " AND " + OS_TYPE_COL + " = " + sqlString(osType);

		logger.info("RBT:: query - " + sql);
		String registrationId = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				registrationId = rs.getString(REGISTRATION_ID_COL);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			closeStatementAndRS(stmt, rs);
		}
		logger.info("Registration Id returned: " + registrationId + " for subscriberId: "  + subscriberId + " and osType: " + osType);
		return registrationId;
	}

	/**
	 * To update registrationId for a particular subscriberId-osType combination
	 * @param conn
	 * @param subscriberId
	 * @param osType - Default value - android
	 * @param registrationId
	 * @param osType
	 * @return update result - True/False
	 */
	public static boolean updateRegistrationIdBySubscriberIdAndType(
			Connection conn, String subscriberId, String osType,
			String registrationId) {
		Statement stmt = null;

		int id = -1;

		if (osType == null) {
			osType = "android";
		}
		if (subscriberId != null && registrationId != null) {
			String sql = "UPDATE " + TABLE_NAME + " SET "
					+ REGISTRATION_ID_COL + " = " + sqlString(registrationId)
					+ " WHERE " + SUBSCRIBER_ID_COL + " = " + sqlString(subscriberId) + " AND " + OS_TYPE_COL + " = " + sqlString(osType);
			logger.info("RBT:: query - " + sql);
			try {
				stmt = conn.createStatement();
				id = stmt.executeUpdate(sql);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return id > 0;
	}
}