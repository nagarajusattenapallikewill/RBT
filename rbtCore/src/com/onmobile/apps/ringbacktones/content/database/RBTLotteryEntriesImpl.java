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

import com.onmobile.apps.ringbacktones.content.RBTLotteryEntries;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTLotteryEntriesImpl extends RBTPrimitive implements RBTLotteryEntries
{
	private static final Logger logger = Logger.getLogger(RBTLotteryEntriesImpl.class);

	private static final String TABLE_NAME			= "RBT_LOTTERY_ENTRIES";
	private static final String SEQUENCE_ID_COL 		= "SEQUENCE_ID";
	private static final String LOTTERY_ID_COL 		= "LOTTERY_ID";
	private static final String SUBSCRIBER_ID_COL 	= "SUBSCRIBER_ID";
	private static final String ENTRY_TIME_COL 		= "ENTRY_TIME";
	private static final String LOTTERY_NUMBER_COL 	= "LOTTERY_NUMBER";
	private static final String CLIP_ID_COL	 		= "CLIP_ID";

	private long sequenceID;
	private int lotteryID;
	private String subscriberID;
	private Date entryTime;
	private String lotteryNumber;
	private int clipID;
	private static String m_databaseType = getDBSelectionString();

	/**
	 * @param lotteryID
	 * @param subscriberID
	 * @param entryTime
	 * @param lotteryNumber
	 * @param clipID
	 */
	private RBTLotteryEntriesImpl(long sequenceID, int lotteryID, String subscriberID, Date entryTime, String lotteryNumber, int clipID)
	{
		this.sequenceID = sequenceID;
		this.lotteryID = lotteryID;
		this.subscriberID = subscriberID;
		this.entryTime = entryTime;
		this.lotteryNumber = lotteryNumber;
		this.clipID = clipID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#sequenceID()
	 */
	@Override
	public long sequenceID()
	{
		return sequenceID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#lotteryID()
	 */
	@Override
	public int lotteryID()
	{
		return lotteryID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#subscriberID()
	 */
	@Override
	public String subscriberID()
	{
		return subscriberID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#entryTime()
	 */
	@Override
	public Date entryTime()
	{
		return entryTime;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#lotteryNumber()
	 */
	@Override
	public String lotteryNumber()
	{
		return lotteryNumber;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteryEntries#clipID()
	 */
	@Override
	public int clipID()
	{
		return clipID;
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
	static boolean insert(Connection conn, int lotteryID, String subscriberID,
			Date entryTime, String lotteryNumber, int clipID)
	{
		Statement stmt = null;

		int id = -1;
		if (entryTime == null)
			entryTime = Calendar.getInstance().getTime();

		String entryTimeStr = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
			entryTimeStr = sqlTime(entryTime);
		else
			entryTimeStr = mySQLDateTime(entryTime);

		String sql = "INSERT INTO " + TABLE_NAME + "(" + LOTTERY_ID_COL;
		sql += ", " + SUBSCRIBER_ID_COL;
		sql += ", " + ENTRY_TIME_COL;
		sql += ", " + LOTTERY_NUMBER_COL;
		sql += ", " + CLIP_ID_COL + ") VALUES(";
		sql += lotteryID + ", ";
		sql += sqlString(subscriberID) + ", ";
		sql += entryTimeStr + ", ";
		sql += sqlString(lotteryNumber) + ", ";
		sql += clipID + ")";

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
	 * @param fetchSize
	 * @return
	 */
	static RBTLotteryEntries[] getUnProcessedLotteryEntries(Connection conn, int fetchSize)
	{
		Statement stmt = null;
		ResultSet rs = null;

		RBTLotteryEntries rbtLotteryEntry = null;
		List<RBTLotteryEntries> rbtLotteryEntriesList = new ArrayList<RBTLotteryEntries>();

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ LOTTERY_ID_COL + " = -1 AND "
				+ LOTTERY_NUMBER_COL + " IS NULL";

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			rs = stmt.executeQuery(sql);

			long sequenceID;
			int lotteryID;
			String subscriberID;
			Date entryTime;
			String lotteryNumber;
			int clipID;
			while (rs.next())
			{
				sequenceID = rs.getLong(SEQUENCE_ID_COL);
				lotteryID = rs.getInt(LOTTERY_ID_COL);
				subscriberID = rs.getString(SUBSCRIBER_ID_COL);
				entryTime = rs.getTimestamp(ENTRY_TIME_COL);
				lotteryNumber = rs.getString(LOTTERY_NUMBER_COL);
				clipID = rs.getInt(CLIP_ID_COL);

				rbtLotteryEntry = new RBTLotteryEntriesImpl(sequenceID, lotteryID, subscriberID, entryTime, lotteryNumber, clipID);
				rbtLotteryEntriesList.add(rbtLotteryEntry);
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

		if (rbtLotteryEntriesList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_LOTTERY_ENTRIES successful");
			return rbtLotteryEntriesList.toArray(new RBTLotteryEntries[0]);
		} 

		return null;
	}

	/**
	 * @param conn
	 * @param lotteryID
	 * @param lotteryNumber
	 * @param sequenceID
	 * @return
	 */
	static boolean updateLotteryIdAndLotteryNumber(Connection conn, int lotteryID, String lotteryNumber, long sequenceID)
	{
		if(lotteryID == -1 || lotteryNumber == null)
		{
			logger.info("RBT:: Nothing is there to update");
			return false;
		}

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + LOTTERY_ID_COL + " = " + lotteryID;
		query += ", " + LOTTERY_NUMBER_COL + " = "+ sqlString(lotteryNumber);

		query += " WHERE " + SEQUENCE_ID_COL + " = " + sequenceID;

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
	 * @param subscriberID
	 * @return
	 */
	static RBTLotteryEntries[] getLotteryEntriesBySubscriberID(Connection conn, String subscriberID)
	{
		Statement stmt = null;
		ResultSet rs = null;

		RBTLotteryEntries rbtLotteryEntry = null;
		List<RBTLotteryEntries> rbtLotteryEntriesList = new ArrayList<RBTLotteryEntries>();

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
				+ " AND " + LOTTERY_ID_COL + " != -1 AND "
				+ LOTTERY_NUMBER_COL + " IS NOT NULL";

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			long sequenceID;
			int lotteryID;
			String subID;
			Date entryTime;
			String lotteryNumber;
			int clipID;
			while (rs.next())
			{
				sequenceID = rs.getLong(SEQUENCE_ID_COL);
				lotteryID = rs.getInt(LOTTERY_ID_COL);
				subID = rs.getString(SUBSCRIBER_ID_COL);
				entryTime = rs.getDate(ENTRY_TIME_COL);
				lotteryNumber = rs.getString(LOTTERY_NUMBER_COL);
				clipID = rs.getInt(CLIP_ID_COL);

				rbtLotteryEntry = new RBTLotteryEntriesImpl(sequenceID, lotteryID, subID, entryTime, lotteryNumber, clipID);
				rbtLotteryEntriesList.add(rbtLotteryEntry);
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

		if (rbtLotteryEntriesList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_LOTTERY_ENTRIES successful");
			return rbtLotteryEntriesList.toArray(new RBTLotteryEntries[0]);
		} 

		return null;
	}
}
