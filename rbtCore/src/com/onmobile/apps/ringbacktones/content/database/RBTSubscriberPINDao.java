package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTSubscriberPIN;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTSubscriberPINDao extends RBTPrimitive
{
	private static Logger logger = Logger.getLogger(RBTSubscriberPINDao.class);

	private static final String TABLE_NAME 					= "RBT_SUBSCRIBER_PINS";
	private static final String SUBSCRIBER_ID_COL			= "SUBSCRIBER_ID";
	private static final String PIN_ID_COL					= "PIN_ID";
	private static final String ACTIVATIONS_COUNT_COL		= "ACTIVATIONS_COUNT";
	private static final String SELECTIONS_COUNT_COL		= "SELECTIONS_COUNT";
	private static final String CREATION_TIME_COL			= "CREATION_TIME";
	private static final String UPDATION_TIME_COL			= "UPDATION_TIME";

	private static String DATABASE_TYPE = getDBSelectionString();

	/**
	 * @param subscriberID
	 * @param pinID
	 * @return
	 */
	public static RBTSubscriberPIN getRBTSubsriberPIN(String subscriberID, String pinID)
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		RBTSubscriberPIN  rbtSubscriberPIN = null;
		String query = null;
		try
		{
			conn = getConnection();
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + PIN_ID_COL + " = '" + pinID + "'";

			logger.info("RBT::SQL QUERY >" + query);
			statement = conn.createStatement();
			rs = statement.executeQuery(query);
			if (rs.first())
				rbtSubscriberPIN = prepareFromRS(rs);

		}
		catch(SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(statement, rs);
			releaseConnection(conn);
		}
		return rbtSubscriberPIN;
	}

	private static RBTSubscriberPIN prepareFromRS(ResultSet rs) throws SQLException
	{
		if (rs == null)
			return null;

		RBTSubscriberPIN rbtSubscriberPIN = new RBTSubscriberPIN();
		rbtSubscriberPIN.setSubscriberID(rs.getString(SUBSCRIBER_ID_COL));
		rbtSubscriberPIN.setPinID(rs.getString(PIN_ID_COL));
		rbtSubscriberPIN.setActivationsCount(rs.getInt(ACTIVATIONS_COUNT_COL));
		rbtSubscriberPIN.setSelectionsCount(rs.getInt(SELECTIONS_COUNT_COL));
		rbtSubscriberPIN.setCreationTime(rs.getTimestamp(CREATION_TIME_COL));
		rbtSubscriberPIN.setUpdationTime(rs.getTimestamp(UPDATION_TIME_COL));

		return rbtSubscriberPIN;
	}

	/**
	 * @param rbtSubscriberPIN
	 * @return
	 */
	public static boolean updateRBTSubscriberPIN(RBTSubscriberPIN rbtSubscriberPIN) throws SQLException
	{
		String subscriberID = rbtSubscriberPIN.getSubscriberID();
		String pinID = rbtSubscriberPIN.getPinID();
		int activationsCount = rbtSubscriberPIN.getActivationsCount();
		int selectionsCount = rbtSubscriberPIN.getSelectionsCount();
		Date creationTime = rbtSubscriberPIN.getCreationTime();
		Date updationTime = rbtSubscriberPIN.getUpdationTime();

		String creationTimeStr = sqlTime(creationTime);
		String updationTimeStr = sqlTime(updationTime);

		if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
		{	
			creationTimeStr = mySQLDateTime(creationTime);
			updationTimeStr = mySQLDateTime(updationTime);
		}

		Connection conn = null;
		Statement statement = null;
		String query = null;
		int count = 0;
		try
		{
			conn = getConnection();
			query = "UPDATE " + TABLE_NAME + " SET " + ACTIVATIONS_COUNT_COL + " = " + activationsCount
					+ ", " + SELECTIONS_COUNT_COL + " = " + selectionsCount
					+ ", " + CREATION_TIME_COL + " = " + creationTimeStr
					+ ", " + UPDATION_TIME_COL + " = " + updationTimeStr
					+ " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + PIN_ID_COL + " = '" + pinID + "'";
			statement = conn.createStatement();

			logger.info("RBT::SQL QUERY >" + query);
			count = statement.executeUpdate(query);
		}
		catch (SQLException e)
		{
			logger.error("", e);
			throw e;
		}
		finally
		{
			closeStatementAndRS(statement, null);
			releaseConnection(conn);
		}
		return (count == 1);
	}

	/**
	 * @param rbtSubscriberPIN
	 * @return
	 */
	public static boolean createRBTSubscriberPIN(RBTSubscriberPIN rbtSubscriberPIN)
	{
		String creationTimeStr = sqlTime(rbtSubscriberPIN.getCreationTime());
		String updationTimeStr = sqlTime(rbtSubscriberPIN.getUpdationTime());
		if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
		{	
			creationTimeStr = mySQLDateTime(rbtSubscriberPIN.getCreationTime());
			updationTimeStr = mySQLDateTime(rbtSubscriberPIN.getUpdationTime());
		}

		Connection conn = null;
		Statement statement = null;
		int count = 0;
		try
		{
			conn = getConnection();
			String query = "INSERT INTO " + TABLE_NAME + "(";
			query += " " + SUBSCRIBER_ID_COL;
			query += ", " + PIN_ID_COL;
			query += ", " + ACTIVATIONS_COUNT_COL;
			query += ", " + SELECTIONS_COUNT_COL;
			query += ", " + CREATION_TIME_COL;
			query += ", " + UPDATION_TIME_COL;
			query += ")";

			query += " VALUES (";
			query += " " + rbtSubscriberPIN.getSubscriberID();
			query += ", " + rbtSubscriberPIN.getPinID();
			query += ", " + rbtSubscriberPIN.getActivationsCount();
			query += ", " + rbtSubscriberPIN.getSelectionsCount();
			query += ", " + creationTimeStr;
			query += ", " + updationTimeStr;
			query += ")";

			logger.info("RBT::SQL QUERY >" + query);

			statement = conn.createStatement();
			count = statement.executeUpdate(query);
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			closeStatementAndRS(statement, null);
			releaseConnection(conn);
		}
		return (count == 1);
	}
}