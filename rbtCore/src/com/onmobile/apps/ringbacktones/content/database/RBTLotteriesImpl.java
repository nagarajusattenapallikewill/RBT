package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTLotteries;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTLotteriesImpl extends RBTPrimitive implements RBTLotteries
{
	private static final Logger logger = Logger.getLogger(RBTLotteriesImpl.class);

	private static final String TABLE_NAME				= "RBT_LOTTERIES";
	private static final String LOTTERY_ID_COL 			= "LOTTERY_ID";
	private static final String LOTTERY_NUMBER_SIZE_COL = "LOTTERY_NUMBER_SIZE";
	private static final String MAX_ENTRIES_COL = "MAX_ENTRIES";

	private int lotteryID;
	private int lotteryNumberSize;
	private long maxEntries;

	/**
	 * @param lotteryID
	 * @param lotteryNumberSize
	 * @param maxEntries
	 */
	private RBTLotteriesImpl(int lotteryID, int lotteryNumberSize, long maxEntries)
	{
		this.lotteryID = lotteryID;
		this.lotteryNumberSize = lotteryNumberSize;
		this.maxEntries = maxEntries;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteries#lotteryID()
	 */
	@Override
	public int lotteryID()
	{
		return lotteryID;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteries#lotteryNumberSize()
	 */
	@Override
	public int lotteryNumberSize()
	{
		return lotteryNumberSize;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteries#maxEntries()
	 */
	@Override
	public long maxEntries()
	{
		return maxEntries;
	}


	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.RBTLotteries#setMaxEntries(long)
	 */
	@Override
	public void setMaxEntries(long maxEntries)
	{
		this.maxEntries = maxEntries;
	}

	static boolean insert(Connection conn, int lotteryID, int lotteryNumberSize, long maxEntries)
	{
		Statement stmt = null;

		int id = -1;
		String sql = "INSERT INTO " + TABLE_NAME + "(" + LOTTERY_ID_COL;
		sql += ", " + LOTTERY_NUMBER_SIZE_COL;
		sql += ", " + MAX_ENTRIES_COL + ") VALUES(";
		sql += lotteryID + ", ";
		sql += lotteryNumberSize + ", ";
		sql += maxEntries + ")";

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

	static RBTLotteries[] getAllLotteries(Connection conn)
	{
		Statement stmt = null;
		ResultSet rs = null;

		RBTLotteries rbtLottery = null;
		List<RBTLotteries> rbtLotteriesList = new ArrayList<RBTLotteries>();

		String sql = "SELECT * FROM " + TABLE_NAME;

		logger.info("RBT:: query - " + sql);
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			int lotteryID;
			int lotteryNumberSize;
			long maxEntries;
			while (rs.next())
			{
				lotteryID = rs.getInt(LOTTERY_ID_COL);
				lotteryNumberSize = rs.getInt(LOTTERY_NUMBER_SIZE_COL);
				maxEntries = rs.getLong(MAX_ENTRIES_COL);

				rbtLottery = new RBTLotteriesImpl(lotteryID, lotteryNumberSize, maxEntries);
				rbtLotteriesList.add(rbtLottery);
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

		if (rbtLotteriesList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_LOTTERY_ENTRIES successful");
			return rbtLotteriesList.toArray(new RBTLotteries[0]);
		} 

		return null;
	}
}
