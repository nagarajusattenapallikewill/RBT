/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;

/**
 * @author vinayasimha.patil
 */
public class SubscriberActivityCountsDAO extends RBTPrimitive
{
	private static Logger logger = Logger.getLogger(SubscriberActivityCountsDAO.class);
	
	private static final String TABLE_NAME = "RBT_SUBSCRIBER_ACTIVITY_COUNTS";
	private static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	private static final String ACTIVITY_DATE = "ACTIVITY_DATE";
	private static final String COUNTS = "COUNTS";

	public static SubscriberActivityCounts createSubscriberActivityCounts(
			SubscriberActivityCounts subscriberActivityCounts)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO ").append(TABLE_NAME).append(" (");
		query.append(" ").append(SUBSCRIBER_ID);
		query.append(", ").append(ACTIVITY_DATE);
		query.append(", ").append(COUNTS);
		query.append(" )");
		query.append(" VALUES (");
		query.append(" '").append(subscriberActivityCounts.getSubscriberID());
		query.append("', '");
		query.append(dateFormat.format(subscriberActivityCounts.getDate()));
		query.append("', ").append(
				sqlString(subscriberActivityCounts.getCounts()));
		query.append(" )");

		logger.info("RBT:: SQL Query: " + query);

		Connection connection = null;
		Statement statement = null;
		try
		{
			connection = getConnection();
			statement = connection.createStatement();
			int insertCount = statement.executeUpdate(query.toString());
			if (insertCount > 0)
				return subscriberActivityCounts;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
			}

			releaseConnection(connection);
		}

		return null;
	}

	public static SubscriberActivityCounts getSubscriberActivityCountsForDate(
			String subscriberID, Date date)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME);
		query.append(" WHERE ").append(SUBSCRIBER_ID).append(" = '");
		query.append(subscriberID).append("' AND ");
		query.append(ACTIVITY_DATE).append(" = '");
		query.append(dateFormat.format(date)).append("'");

		logger.info("RBT:: SQL Query: " + query);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try
		{
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query.toString());

			if (resultSet.next())
			{
				SubscriberActivityCounts subscriberActivityCounts = prepareFromResultSet(resultSet);
				return subscriberActivityCounts;
			}
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
			}
			try
			{
				if (resultSet != null)
					resultSet.close();
			}
			catch (Exception e)
			{
			}

			releaseConnection(connection);
		}

		return null;
	}

	public static List<SubscriberActivityCounts> getSubscriberActivityCountsForDays(
			String subscriberID, int requiredNoOfDays)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME);
		query.append(" WHERE ").append(SUBSCRIBER_ID).append(" = '");
		query.append(subscriberID).append("'");

		Calendar calendar = Calendar.getInstance();
		if (requiredNoOfDays == 1)
		{
			query.append(" AND ").append(ACTIVITY_DATE).append(" = '");
			query.append(dateFormat.format(calendar.getTime()));
			query.append("'");
		}
		else
		{
			calendar.add(Calendar.DAY_OF_YEAR, requiredNoOfDays * -1);
			query.append(" AND ").append(ACTIVITY_DATE).append(" > '");
			query.append(dateFormat.format(calendar.getTime()));
			query.append("'");
		}

		logger.info("RBT:: SQL Query: " + query);

		List<SubscriberActivityCounts> subscriberActivityCountsList = new ArrayList<SubscriberActivityCounts>();

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try
		{
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query.toString());

			while (resultSet.next())
			{
				SubscriberActivityCounts subscriberActivityCounts = prepareFromResultSet(resultSet);
				subscriberActivityCountsList.add(subscriberActivityCounts);
			}
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
			}
			try
			{
				if (resultSet != null)
					resultSet.close();
			}
			catch (Exception e)
			{
			}

			releaseConnection(connection);
		}

		return subscriberActivityCountsList;
	}

	public static SubscriberActivityCounts updateSubscriberActivityCounts(
			SubscriberActivityCounts subscriberActivityCounts)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		StringBuilder query = new StringBuilder();
		query.append("UPDATE ").append(TABLE_NAME).append(" SET");
		query.append(" ").append(COUNTS).append(" = ");
		query.append(sqlString(subscriberActivityCounts.getCounts()));
		query.append(" WHERE ").append(SUBSCRIBER_ID).append(" = '");
		query.append(subscriberActivityCounts.getSubscriberID());
		query.append("' AND ").append(ACTIVITY_DATE).append(" = '");
		query.append(dateFormat.format(subscriberActivityCounts.getDate()));
		query.append("'");

		logger.info("RBT:: SQL Query: " + query);

		Connection connection = null;
		Statement statement = null;
		try
		{
			connection = getConnection();
			statement = connection.createStatement();
			int updateCount = statement.executeUpdate(query.toString());
			if (updateCount > 0)
				return subscriberActivityCounts;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
			}

			releaseConnection(connection);
		}

		return null;
	}

	public static int deleletSubscriberActivityCountsForDays(int cleanUpDays)
	{
		int count = 0;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, cleanUpDays * -1);

		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM ").append(TABLE_NAME);
		query.append(" WHERE ").append(ACTIVITY_DATE).append(" <= '");
		query.append(dateFormat.format(calendar.getTime()));
		query.append("'");

		logger.info("RBT:: SQL Query: " + query);

		Connection connection = null;
		Statement statement = null;
		try
		{
			connection = getConnection();
			statement = connection.createStatement();
			count = statement.executeUpdate(query.toString());

		}
		catch (Exception e)
		{
			logger.error("", e);
			return -1;
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
			}

			releaseConnection(connection);
		}

		return count;
	}

	private static SubscriberActivityCounts prepareFromResultSet(
			ResultSet resultSet)
	{
		SubscriberActivityCounts subscriberActivityCounts = new SubscriberActivityCounts();
		try
		{
			subscriberActivityCounts.setSubscriberID(resultSet
					.getString(SUBSCRIBER_ID));
			subscriberActivityCounts.setDate(resultSet.getDate(ACTIVITY_DATE));
			subscriberActivityCounts.setCounts(resultSet.getString(COUNTS));
		}
		catch (SQLException e)
		{
			logger.error("", e);
		}

		return subscriberActivityCounts;
	}
}