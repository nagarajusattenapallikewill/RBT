package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.BulkPromoSMS;

public class BulkPromoSMSImpl extends RBTPrimitive implements BulkPromoSMS
{
	private static Logger logger = Logger.getLogger(BulkPromoSMSImpl.class);

	private static final String TABLE_NAME = "RBT_BULK_PROMO_SMS";
	private static final String BULK_PROMO_ID_COL = "BULK_PROMO_ID";
	private static final String SMS_DATE_COL = "SMS_DATE";
	private static final String SMS_TEXT_COL = "SMS_TEXT";
	private static final String SMS_SENT_COL = "SMS_SENT";

	private String m_promoId;
	private String m_smsDate;
	private String m_smsText;
	private String m_smsSent;

	private static DateFormat format = new SimpleDateFormat("yyyyMMdd");

	BulkPromoSMSImpl(String promoId, String smsDate, String smsText,
			String smsSent)
			{
		m_promoId = promoId;
		m_smsDate = smsDate;
		m_smsText = smsText;
		m_smsSent = smsSent;
			}

	public String bulkPromoId()
	{
		return m_promoId;
	}

	public String smsDate()
	{
		return m_smsDate;
	}

	public String smsText()
	{
		return m_smsText;
	}

	public boolean smsSent()
	{
		return (m_smsSent.equalsIgnoreCase("y")
				|| m_smsSent.equalsIgnoreCase("yes") || m_smsSent
				.equals("true"));
	}

	public static BulkPromoSMS insert(Connection conn, String promoId, String smsDate, String smsText, String sentSMS) 
	{ 
		logger.info("RBT::inside insert"); 

		Statement stmt = null; 

		int id = -1; 
		try 
		{ 
			String sql = "INSERT INTO " + TABLE_NAME + " (" + BULK_PROMO_ID_COL; 
			sql += ", " + SMS_DATE_COL; 
			sql += ", " + SMS_TEXT_COL; 
			sql += ", " + SMS_SENT_COL + ")"; 

			sql += " VALUES("; 
			sql += sqlString(promoId); 
			sql += ", " + sqlString(smsDate); 
			sql += ", " + sqlString(smsText); 
			sql += ", "+ sqlString(sentSMS) +")"; 

			logger.info("RBT::query = " + sql); 

			stmt = conn.createStatement(); 

			id = stmt.executeUpdate(sql); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
		} 
		finally 
		{ 
			try 
			{ 
				stmt.close(); 
			} 
			catch(Exception e) 
			{ 
				logger.error("", e); 
			} 
		} 
		if(id > 0) 
		{ 
			logger.info("RBT::insertion into RBT_BULK_PROMO_SMS successful"); 
			return new BulkPromoSMSImpl(promoId, smsDate, smsText, sentSMS); 
		} 
		return null; 
	} 

	public static BulkPromoSMS insert(Connection conn, String promoId,
			int smsDay, String smsText)
	{
		logger.info("RBT::inside insert");

		Statement stmt = null;

		int id = -1;
		String smsDateString = null;

		try
		{
			BulkPromo promo = BulkPromoImpl.getBulkPromo(conn, promoId);

			Date promoStartDate = promo.promoStartDate();

			if (smsDay == -1)
			{
				smsDateString = "Welcome";
			}
			else if (smsDay == -2)
			{
				smsDateString = "Termination";
			}
			else
			{
				Date smsDate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDateString = format.format(smsDate);
			}

			String sql = "INSERT INTO " + TABLE_NAME + " (" + BULK_PROMO_ID_COL;
			sql += ", " + SMS_DATE_COL;
			sql += ", " + SMS_TEXT_COL;
			sql += ", " + SMS_SENT_COL + ")";

			sql += " VALUES(";
			sql += "'" + promoId + "'";
			sql += ", '" + smsDateString + "'";
			sql += ", " + sqlString(smsText);
			sql += ", 'n')";

			 logger.info("RBT::query - " + sql);

			stmt = conn.createStatement();

			id = stmt.executeUpdate(sql);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (id > 0)
		{
			logger.info("RBT::insertion into RBT_BULK_PROMO_SMS successful");
			return new BulkPromoSMSImpl(promoId, smsDateString, smsText, "n");
		}
		return null;
	}

	static BulkPromoSMS[] getBulkPromoSMSes(Connection conn)
	{
		logger.info("RBT::inside getBulkPromoSMSes");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String bulkPromoId = null;
		String smsDate = null;
		String smsText = null;
		String smsSent = null;

		BulkPromoSMSImpl bulkPromoSms = null;
		List bulkPromoSmsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+ BULK_PROMO_ID_COL;

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				bulkPromoId = results.getString(BULK_PROMO_ID_COL);
				smsDate = results.getString(SMS_DATE_COL);
				smsText = results.getString(SMS_TEXT_COL);
				smsSent = results.getString(SMS_SENT_COL);

				bulkPromoSms = new BulkPromoSMSImpl(bulkPromoId, smsDate,
						smsText, smsSent);
				bulkPromoSmsList.add(bulkPromoSms);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (bulkPromoSmsList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_BULK_PROMO_SMS successful");
			return (BulkPromoSMS[]) bulkPromoSmsList
			.toArray(new BulkPromoSMS[0]);
		}
		else
		{
			logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
			return null;
		}
	}

	public static BulkPromoSMS getBulkPromoSMS(Connection conn, String promoId,
			int smsDay)
	{
		logger.info("RBT::inside getBulkPromoSMS");

		Statement stmt = null;
		RBTResultSet rs = null;
		String sql = null;

		String smsDateString;
		String smsText;
		String smsSent;

		try
		{
			stmt = conn.createStatement();

			BulkPromo promo = BulkPromoImpl.getBulkPromo(conn, promoId);

			Date promoStartDate = promo.promoStartDate();

			if (smsDay == -1)
			{
				smsDateString = "Welcome";
			}
			else if (smsDay == -2)
			{
				smsDateString = "Termination";
			}
			else
			{
				Date smsDate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDateString = format.format(smsDate);
			}
			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL
			+ " = '" + promoId + "' AND " + SMS_DATE_COL + " = '"
			+ smsDateString + "'";

			logger.info("RBT:: query - " + sql);

			rs = new RBTResultSet(stmt.executeQuery(sql));

			if (rs.next())
			{
				smsText = rs.getString(SMS_TEXT_COL);
				smsSent = rs.getString(SMS_SENT_COL);

				return new BulkPromoSMSImpl(promoId, smsDateString, smsText,
						smsSent);
			}

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return null;
	}

	public static BulkPromoSMS updateBulkPromoSMS(Connection conn,
			String promoId, int smsDay, String smsText)
	{
		logger.info("RBT::inside updateBulkPromoSMS");

		Statement stmt = null;
		int id = -1;
		String sql = null;

		String smsDateString;

		try
		{
			stmt = conn.createStatement();

			BulkPromo promo = BulkPromoImpl.getBulkPromo(conn, promoId);

			Date promoStartDate = promo.promoStartDate();

			if (smsDay == -1)
			{
				smsDateString = "Welcome";
			}
			else if (smsDay == -2)
			{
				smsDateString = "Termination";
			}
			else
			{
				Date smsDate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDateString = format.format(smsDate);
			}
			sql = "UPDATE " + TABLE_NAME + " SET " + SMS_TEXT_COL + " = '"
			+ smsText + "' WHERE " + BULK_PROMO_ID_COL + " = '"
			+ promoId + "' AND " + SMS_DATE_COL + " = '"
			+ smsDateString + "'";

			logger.info("RBT:: query " + sql);

			id = stmt.executeUpdate(sql);

			if (id >= 0)
			{
				return new BulkPromoSMSImpl(promoId, smsDateString, smsText,
						"n");
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return null;
	}

	public static boolean updateSMSSent(Connection conn, String promoId,
			int smsDay, String smsSent)
	{
		logger.info("RBT::inside updateSMSSent");

		Statement stmt = null;
		int id = -1;
		String sql = null;

		String smsDateString;

		try
		{
			stmt = conn.createStatement();

			BulkPromo promo = BulkPromoImpl.getBulkPromo(conn, promoId);

			Date promoStartDate = promo.promoStartDate();

			if (smsDay == -1)
			{
				smsDateString = "Welcome";
			}
			else if (smsDay == -2)
			{
				smsDateString = "Termination";
			}
			else
			{
				Date smsDate = addDaysToDate(promoStartDate, smsDay - 1);
				smsDateString = format.format(smsDate);
			}
			sql = "UPDATE " + TABLE_NAME + " SET " + SMS_SENT_COL + " = '"
			+ smsSent + "' WHERE " + BULK_PROMO_ID_COL + " = '"
			+ promoId + "' AND " + SMS_DATE_COL + " = '"
			+ smsDateString + "'";

			logger.info("RBT:: query " + sql);

			id = stmt.executeUpdate(sql);

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return (id >= 0);
	}

	public static boolean updateSMSSent(Connection conn, String promoId,
			String smsDay, String smsSent)
	{
		logger.info("RBT::inside updateSMSSent");

		Statement stmt = null;
		int id = -1;
		String sql = null;

		try
		{
			stmt = conn.createStatement();
			sql = "UPDATE " + TABLE_NAME + " SET " + SMS_SENT_COL + " = '"
			+ smsSent + "' WHERE " + BULK_PROMO_ID_COL + " = '"
			+ promoId + "' AND " + SMS_DATE_COL + " = '" + smsDay + "'";
			logger.info("RBT:: query " + sql);

			id = stmt.executeUpdate(sql);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return (id >= 0);
	}

	public static BulkPromoSMS[] getBulkPromoSMSForDate(Connection conn,
			Date smsDate)
	{
		logger.info("RBT::inside getBulkPromoSMSForDate");

		Statement stmt = null;
		RBTResultSet rs = null;

		ArrayList smsList = new ArrayList();

		String smsText;
		String bulkPromoId;

		if (smsDate == null)
			smsDate = new Date();

		String smsDateString = format.format(smsDate);

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_DATE_COL
		+ " = '" + smsDateString + "' AND " + SMS_SENT_COL + " = 'n'";

		logger.info("RBT query is "	+ sql);

		try
		{
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(sql));

			while (rs.next())
			{
				bulkPromoId = rs.getString(BULK_PROMO_ID_COL);
				smsText = rs.getString(SMS_TEXT_COL);

				BulkPromoSMS sms = new BulkPromoSMSImpl(bulkPromoId,
						smsDateString, smsText, "y");
				smsList.add(sms);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			return null;
		}
		finally
		{
			if (rs != null)
			{
				try
				{
					rs.close();
					rs = null;
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
			if (stmt != null)
			{
				try
				{
					stmt.close();
					stmt = null;
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
		}
		if (smsList.size() > 0)
		{
			logger.info("RBT::Retrieving records from RBT_BULK_PROMO_SMS successful");
			return (BulkPromoSMSImpl[]) smsList
			.toArray(new BulkPromoSMSImpl[0]);
		}
		else
		{
			logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
			return null;
		}
	}

	private static Date addDaysToDate(Date oldDate, int days)
	{
		Calendar cal = Calendar.getInstance();

		cal.setTime(oldDate);

		cal.add(Calendar.DATE, days);

		Date newDate = cal.getTime();

		return newDate;
	}

	public static BulkPromoSMS getBulkPromoSMSForDaemon(Connection conn,
			String promoId, int smsDate)
	{
		logger.info("RBT::inside getBulkPromoSMS");

		Statement stmt = null;
		RBTResultSet rs = null;
		String sql = null;

		String smsText;
		String smsSent;

		try
		{
			stmt = conn.createStatement();

			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL
			+ " = '" + promoId + "' AND " + SMS_DATE_COL + " = '"
			+ smsDate + "'";

			logger.info("RBT:: query - " + sql);

			rs = new RBTResultSet(stmt.executeQuery(sql));

			if (rs.next())
			{
				smsText = rs.getString(SMS_TEXT_COL);
				smsSent = rs.getString(SMS_SENT_COL);

				return new BulkPromoSMSImpl(promoId, String.valueOf(smsDate),
						smsText, smsSent);
			}

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return null;
	}

	//	added by gautham

	static BulkPromoSMS getBulkPromoSMS(Connection conn, String promoId,
			String sDate)
	{
		logger.info("RBT::inside getBulkPromoSMS");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String bulkPromoId = null;
		String smsDate = null;
		String smsText = null;
		String smsSent = null;

		BulkPromoSMSImpl bulkPromoSms = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL
		+ " = " + "'" + promoId + "' ";
		if (sDate != null)
			query = query + " AND " + SMS_DATE_COL + " = " + "'" + sDate + "'";
		else
			query = query + " AND " + SMS_DATE_COL + " IS NULL";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				bulkPromoId = results.getString(BULK_PROMO_ID_COL);
				smsDate = results.getString(SMS_DATE_COL);
				smsText = results.getString(SMS_TEXT_COL);
				smsSent = results.getString(SMS_SENT_COL);

				bulkPromoSms = new BulkPromoSMSImpl(bulkPromoId, smsDate,
						smsText, smsSent);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return bulkPromoSms;
	}

	static BulkPromoSMS[] getAllPromoIDSMSes(Connection conn, String promoId)
	{
		logger.info("RBT::inside getAllPromoIDSMSes");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String bulkPromoId = null;
		String smsDate = null;
		String smsText = null;
		String smsSent = null;

		BulkPromoSMSImpl bulkPromoSms = null;
		List bulkPromoSmsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL
		+ " = " + "'" + promoId + "'";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				bulkPromoId = results.getString(BULK_PROMO_ID_COL);
				smsDate = results.getString(SMS_DATE_COL);
				smsText = results.getString(SMS_TEXT_COL);
				smsSent = results.getString(SMS_SENT_COL);

				bulkPromoSms = new BulkPromoSMSImpl(bulkPromoId, smsDate,
						smsText, smsSent);
				bulkPromoSmsList.add(bulkPromoSms);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (bulkPromoSmsList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_BULK_PROMO_SMS successful");
			return (BulkPromoSMS[]) bulkPromoSmsList
			.toArray(new BulkPromoSMS[0]);
		}
		else
		{
			logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
			return null;
		}
	}

	static BulkPromoSMS[] getDistinctPromoIds(Connection conn)
	{
		logger.info("RBT::inside getDistinctPromoIds");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String bulkPromoId = null;

		BulkPromoSMSImpl bulkPromoSms = null;
		List bulkPromoSmsList = new ArrayList();

		query = "SELECT DISTINCT(" + BULK_PROMO_ID_COL + ")  FROM "
		+ TABLE_NAME + " ORDER BY " + BULK_PROMO_ID_COL;

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				bulkPromoId = results.getString(BULK_PROMO_ID_COL);

				bulkPromoSms = new BulkPromoSMSImpl(bulkPromoId, null, null,
						null);
				bulkPromoSmsList.add(bulkPromoSms);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (bulkPromoSmsList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_BULK_PROMO_SMS successful");
			return (BulkPromoSMS[]) bulkPromoSmsList
			.toArray(new BulkPromoSMS[0]);
		}
		else
		{
			logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
			return null;
		}
	}

	static boolean update(Connection conn, String bulkPromoId, String smsDate,
			String smsText, String smsSent)
	{
		logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + SMS_TEXT_COL + " = "
		+ sqlString(smsText) + ", " + SMS_SENT_COL + " = "
		+ sqlString(smsSent) + " WHERE " + BULK_PROMO_ID_COL + " = "
		+ "'" + bulkPromoId + "' ";
		if (smsDate != null && !smsDate.trim().equalsIgnoreCase("null"))
			query = query + " AND " + SMS_DATE_COL + " = " + "'" + smsDate
			+ "'";
		else
			query = query + " AND " + SMS_DATE_COL + " IS NULL";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return (n == 1);
	}

	static BulkPromoSMS[] getBulkPromoSMSes(Connection conn, String promoId)
	{
		logger.info("RBT::inside getSMSDays");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		String bulkPromoId = null;
		String smsDate = null;
		String smsText = null;
		String smsSent = null;

		BulkPromoSMSImpl bulkPromoSms = null;
		List bulkPromoSmsList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL
		+ " = " + "'" + promoId + "'";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				bulkPromoId = results.getString(BULK_PROMO_ID_COL);
				smsDate = results.getString(SMS_DATE_COL);
				smsText = results.getString(SMS_TEXT_COL);
				smsSent = results.getString(SMS_SENT_COL);

				bulkPromoSms = new BulkPromoSMSImpl(bulkPromoId, smsDate,
						smsText, smsSent);
				bulkPromoSmsList.add(bulkPromoSms);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		if (bulkPromoSmsList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_BULK_PROMO_SMS successful");
			return (BulkPromoSMS[]) bulkPromoSmsList
			.toArray(new BulkPromoSMS[0]);
		}
		else
		{
			logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
			return null;
		}
	}

	public static BulkPromoSMS getBulkPromoSMSForDate(Connection conn, String bulkPromoID, String smsDate)
	{
		logger.info("RBT::inside getBulkPromoSMSForDate");

		Statement stmt = null;
		RBTResultSet rs = null;

		String smsText;
		String bulkPromoId;

		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ BULK_PROMO_ID_COL + " = '" + bulkPromoID + "' AND "
				+ SMS_DATE_COL + getNullForWhere(smsDate);

		logger.info("RBT query is " + sql);

		try
		{
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(sql));

			if(rs.next())
			{
				bulkPromoId = rs.getString(BULK_PROMO_ID_COL);
				smsText = rs.getString(SMS_TEXT_COL);

				BulkPromoSMS sms = new BulkPromoSMSImpl(bulkPromoId, smsDate, smsText, "y");
				return sms;
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			return null;
		}
		finally
		{
			if(rs != null)
			{
				try
				{
					rs.close();
					rs = null;
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			if(stmt != null)
			{
				try
				{
					stmt.close();
					stmt = null;
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
		}

		logger.info("RBT::no records in RBT_BULK_PROMO_SMS");
		return null;

	}
}