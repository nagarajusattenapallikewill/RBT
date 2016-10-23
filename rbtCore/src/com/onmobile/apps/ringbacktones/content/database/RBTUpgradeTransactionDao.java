package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTUpgradeTransaction;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTUpgradeTransactionDao extends RBTPrimitive
{
	private static Logger logger = Logger.getLogger(RBTUpgradeTransactionDao.class);

	private static final String TABLE_NAME 					= "RBT_UPGRADE_TRANSACTIONS";
	private static final String SEQUENCE_ID_COL				= "SEQUENCE_ID";
	private static final String SUBSCRIBER_ID_COL			= "SUBSCRIBER_ID";
	private static final String TRANSACTION_TYPE_COL		= "TRANSACTION_TYPE";
	private static final String SUBSCRIPTION_CLASS_COL		= "SUBSCRIPTION_CLASS";
	private static final String COS_ID_COL					= "COS_ID";
	private static final String OFFER_ID_COL				= "OFFER_ID";
	private static final String CHARGE_CLASS_COL			= "CHARGE_CLASS";
	private static final String CREATION_TIME_COL			= "CREATION_TIME";
	private static final String ACTIVATED_BY_COL			= "ACTIVATED_BY";
	private static final String ACTIVATION_INFO_COL			= "ACTIVATION_INFO";
	private static final String EXTRA_INFO_COL				= "EXTRA_INFO";
	private static final String RBT_TYPE_COL				= "RBT_TYPE";

	private static String DATABASE_TYPE = getDBSelectionString();
	
	/**
	 * @param subscriberID
	 * @param type
	 * @return
	 */
	public static RBTUpgradeTransaction getOldestRBTUpgradeTransaction(String subscriberID, int type)
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		RBTUpgradeTransaction  rbtUpgradeTransaction = null;
		String query = null;
		try
		{
			conn = getConnection();
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ TRANSACTION_TYPE_COL + " = " + type + " ORDER BY SEQUENCE_ID";
			statement = conn.createStatement();
			rs = statement.executeQuery(query);
			if (rs.first())
				rbtUpgradeTransaction = prepareFromRS(rs);

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
		return rbtUpgradeTransaction;
	}

	/**
	 * @param subscriberID
	 * @param type
	 * @return
	 */
	public static List<RBTUpgradeTransaction> getAllRBTUpgradeTransactions(String subscriberID, int type)
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		List<RBTUpgradeTransaction> rbtUpgradeTransactionList = new ArrayList<RBTUpgradeTransaction>();
		String query = null;
		try
		{
			conn = getConnection();
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ TRANSACTION_TYPE_COL + " = " + type + " ORDER BY SEQUENCE_ID";
			statement = conn.createStatement();
			rs = statement.executeQuery(query);
			while (rs.next())
			{
				rbtUpgradeTransactionList.add(prepareFromRS(rs));
			}

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
		return rbtUpgradeTransactionList;
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static RBTUpgradeTransaction prepareFromRS(ResultSet rs) throws SQLException
	{
		if (rs == null)
			return null;

		RBTUpgradeTransaction rbtUpgradeTransaction = new RBTUpgradeTransaction();
		rbtUpgradeTransaction.setSequenceID(rs.getInt(SEQUENCE_ID_COL));
		rbtUpgradeTransaction.setSubscriberID(rs.getString(SUBSCRIBER_ID_COL));
		rbtUpgradeTransaction.setTransactionType(rs.getInt(TRANSACTION_TYPE_COL));
		rbtUpgradeTransaction.setSubscriptionClass(rs.getString(SUBSCRIPTION_CLASS_COL));
		rbtUpgradeTransaction.setCosID(rs.getString(COS_ID_COL));
		rbtUpgradeTransaction.setOfferID(rs.getString(OFFER_ID_COL));
		rbtUpgradeTransaction.setChargeClass(rs.getString(CHARGE_CLASS_COL));
		rbtUpgradeTransaction.setCreationTime(rs.getTimestamp(CREATION_TIME_COL));
		rbtUpgradeTransaction.setActivatedBy(rs.getString(ACTIVATED_BY_COL));
		rbtUpgradeTransaction.setActivationInfo(rs.getString(ACTIVATION_INFO_COL));
		rbtUpgradeTransaction.setExtraInfo(rs.getString(EXTRA_INFO_COL));
		rbtUpgradeTransaction.setRbtType(rs.getInt(RBT_TYPE_COL));

		return rbtUpgradeTransaction;
	}
	
	/**
	 * @param rbtUpgradeTransaction
	 * @return
	 */
	public static boolean createRBTUpgradeTransaction(RBTUpgradeTransaction rbtUpgradeTransaction)
	{
		String creationTimeStr = sqlTime(rbtUpgradeTransaction.getCreationTime());
		if(DATABASE_TYPE.equalsIgnoreCase(DB_MYSQL))
		{	
			creationTimeStr = mySQLDateTime(rbtUpgradeTransaction.getCreationTime());
		}

		Connection conn = null;
		Statement statement = null;
		int count = 0;
		try
		{
			conn = getConnection();
			String query = "INSERT INTO " + TABLE_NAME + "(";
			query += " " + SUBSCRIBER_ID_COL;
			query += ", " + TRANSACTION_TYPE_COL;
			query += ", " + SUBSCRIPTION_CLASS_COL;
			query += ", " + COS_ID_COL;
			query += ", " + OFFER_ID_COL;
			query += ", " + CHARGE_CLASS_COL;
			query += ", " + CREATION_TIME_COL;
			query += ", " + ACTIVATED_BY_COL;
			query += ", " + ACTIVATION_INFO_COL;
			query += ", " + EXTRA_INFO_COL;
			query += ", " + RBT_TYPE_COL;
			query += ")";

			query += " VALUES (";
			query += " " + sqlString(rbtUpgradeTransaction.getSubscriberID());
			query += ", " + rbtUpgradeTransaction.getTransactionType();
			query += ", " + sqlString(rbtUpgradeTransaction.getSubscriptionClass());
			query += ", " + sqlString(rbtUpgradeTransaction.getCosID());
			query += ", " + sqlString(rbtUpgradeTransaction.getOfferID());
			query += ", " + sqlString(rbtUpgradeTransaction.getChargeClass());
			query += ", " + creationTimeStr;
			query += ", " + sqlString(rbtUpgradeTransaction.getActivatedBy());
			query += ", " + sqlString(rbtUpgradeTransaction.getActivationInfo());
			query += ", " + sqlString(rbtUpgradeTransaction.getExtraInfo());
			query += ", " + rbtUpgradeTransaction.getRbtType();
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
	
	/**
	 * @param sequenceID
	 * @return
	 */
	public static boolean removeRBTUpgradeTransaction(int sequenceID)
	{
		int n = -1;
		Connection conn = null;
		String query = null;
		Statement statement = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SEQUENCE_ID_COL + " = " + sequenceID;
		logger.info("RBT::query "+query);
		try
		{
			logger.info( "RBT::inside try block");
			conn = getConnection();
			statement = conn.createStatement();
			n= statement.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(statement, null);
			releaseConnection(conn);
		}
		return (n == 1);
	
	}

	/**
	 * @param subscriberID
	 * @param type
	 * @return
	 */
	public static boolean removeRBTUpgradeTransactionsBySubscriberIDAndType(String subscriberID, int type)
	{
		int n = -1;
		Connection conn = null;
		String query = null;
		Statement statement = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND "
					+ TRANSACTION_TYPE_COL + " = " + type;
		logger.info("RBT::query "+query);
		try
		{
			logger.info( "RBT::inside try block");
			conn = getConnection();
			statement = conn.createStatement();
			n= statement.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(statement, null);
			releaseConnection(conn);
		}
		return (n == 1);
	
	}
}