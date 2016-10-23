package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTLotteryNumber;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTLotteryNumberImpl extends RBTPrimitive implements RBTLotteryNumber
{
	private static final Logger logger = Logger.getLogger(RBTLotteryNumberImpl.class);

	private static final String TABLE_NAME			= "RBT_LOTTERY_NUMBER";
	private static final String SEQUENCE_ID_COL 	= "SEQUENCE_ID";
	private static final String LOTTERY_ID_COL 		= "LOTTERY_ID";
	private static final String LOTTERY_NUMBER_COL 	= "LOTTERY_NUMBER";
	private static final String ACCESS_COUNT_COL 	= "ACCESS_COUNT";

	private long sequenceID;
	private int lotteryID;
	private String lotteryNumber;
	private int accessCount;

	/**
	 * @param sequenceID
	 * @param lotteryID
	 * @param lotteryNumber
	 */
	private RBTLotteryNumberImpl(long sequenceID, int lotteryID, String lotteryNumber, int accessCount)
	{
		this.sequenceID = sequenceID;
		this.lotteryID = lotteryID;
		this.lotteryNumber = lotteryNumber;
		this.accessCount = accessCount;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryNumber#sequenceID()
	 */
	@Override
	public long sequenceID()
	{
		return sequenceID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryNumber#lotteryID()
	 */
	@Override
	public int lotteryID()
	{
		return lotteryID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryNumber#lotteryNumber()
	 */
	@Override
	public String lotteryNumber()
	{
		return lotteryNumber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryNumber#accessCount()
	 */
	@Override
	public int accessCount()
	{
		return accessCount;
	}
	/**
	 * @param conn
	 * @param lotteryID
	 * @param subscriberID
	 * @param entryTime
	 * @param lotteryNumber
	 * @param clipID
	 * @return
	 */
	static boolean insert(Connection conn, int lotteryID, String lotteryNumber)
	{
		Statement stmt = null;

		int id = -1;
		String sql = "INSERT INTO " + TABLE_NAME + "(" + LOTTERY_ID_COL;
		sql += ", " + LOTTERY_NUMBER_COL + ") VALUES(";
		sql += lotteryID + ", ";
		sql += sqlString(lotteryNumber) + ")";

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
	 * @param lotteryID
	 * @return
	 */
	static RBTLotteryNumber getOldestLotteryNumberUnderLotteryID(Connection conn, int lotteryID)
	{
		Statement stmt = null;
		ResultSet rs = null;

		RBTLotteryNumber rbtLotteryNumber = null;

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ LOTTERY_ID_COL + " = " + lotteryID
				+ " AND " + ACCESS_COUNT_COL + " = 0"
				+ " ORDER BY SEQUENCE_ID LIMIT 1";

		logger.info("RBT:: query - " + sql);
		try
		{
			synchronized (RBTLotteryNumberImpl.class)
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);

				long sequenceID = -1;
				String lotteryNumber;
				int accessCount;
				if (rs.next())
				{
					sequenceID = rs.getLong(SEQUENCE_ID_COL);
					lotteryNumber = rs.getString(LOTTERY_NUMBER_COL);
					accessCount = rs.getInt(ACCESS_COUNT_COL);

					rbtLotteryNumber = new RBTLotteryNumberImpl(sequenceID, lotteryID, lotteryNumber, accessCount);
				}

				if (rbtLotteryNumber != null)
					updateAccessCount(conn, 1, sequenceID);
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

		return rbtLotteryNumber;
	}

	static RBTLotteryNumber getOldestLotteryNumberNotUnderLotteryID(Connection conn, int lotteryID)
	{
		Statement stmt = null;
		ResultSet rs = null;

		RBTLotteryNumber rbtLotteryNumber = null;

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ LOTTERY_ID_COL + " != " + lotteryID
				+ " ORDER BY SEQUENCE_ID LIMIT 1";

		logger.info("RBT:: query - " + sql);
		try
		{
			synchronized (RBTLotteryNumberImpl.class)
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);

				long sequenceID = -1;
				String lotteryNumber;
				int accessCount;
				if (rs.next())
				{
					sequenceID = rs.getLong(SEQUENCE_ID_COL);
					lotteryID = rs.getInt(LOTTERY_ID_COL);
					lotteryNumber = rs.getString(LOTTERY_NUMBER_COL);
					accessCount = rs.getInt(ACCESS_COUNT_COL);

					rbtLotteryNumber = new RBTLotteryNumberImpl(sequenceID, lotteryID, lotteryNumber, accessCount);
				}

				if (rbtLotteryNumber != null)
					updateAccessCount(conn, 1, sequenceID);
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

		return rbtLotteryNumber;
	}

	/**
	 * @param accessCount
	 * @param sequenceID
	 */
	static boolean updateAccessCount(Connection conn, int accessCount, long sequenceID)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + ACCESS_COUNT_COL + " = " + accessCount
				+ " WHERE " + SEQUENCE_ID_COL + " = " + sequenceID;

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

	/**
	 * @param conn
	 * @param sequenceID
	 * @return
	 */
	static boolean deleteBySequenceID(Connection conn, long sequenceID)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SEQUENCE_ID_COL + " = " + sequenceID;

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

	/**
	 * @param conn
	 * @param lotteryID
	 * @return
	 */
	static long getCountByLotteryID(Connection conn, int lotteryID)
	{
		Statement stmt = null;
		ResultSet rs = null;

		String sql = "SELECT COUNT(*) AS CNT FROM " + TABLE_NAME + " WHERE "
				+ LOTTERY_ID_COL + " = " + lotteryID;

		logger.info("RBT:: query - " + sql);
		long count = -1;
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			if (rs.next())
			{
				count = rs.getLong("CNT");
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

		return count;
	}
}

