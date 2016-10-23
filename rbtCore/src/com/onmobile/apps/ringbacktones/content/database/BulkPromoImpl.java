package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.BulkPromo;

public class BulkPromoImpl extends RBTPrimitive implements BulkPromo
{
	private static Logger logger = Logger.getLogger(BulkPromoImpl.class);
	
	private static final String TABLE_NAME = "RBT_BULK_PROMO";
	private static final String BULK_PROMO_ID_COL = "BULK_PROMO_ID";
	private static final String START_DATE_COL = "PROMO_START_DATE";
	private static final String END_DATE_COL = "PROMO_END_DATE";
	private static final String PROCESSED_DEACTIVATION_COL = "PROCESSED_DEACTIVATION";
	private static final String COS_ID_COL = "COS_ID";	
	
	private String m_promoId;
	private Date m_startDate;
	private Date m_endDate;
	private String m_processedDeactivation;
	private String m_cosID;
	private static String m_databaseType=getDBSelectionString();
	
	public BulkPromoImpl(String promoId, Date startDate, Date endDate, String processedDeactivation)
	{
		m_promoId = promoId;
		m_startDate = startDate;
		m_endDate = endDate;
		m_processedDeactivation = processedDeactivation;
	}
	
	public BulkPromoImpl(String promoId, Date startDate, Date endDate, String processedDeactivation, String cosID)
	{
		m_promoId = promoId;
		m_startDate = startDate;
		m_endDate = endDate;
		m_processedDeactivation = processedDeactivation;
		m_cosID = cosID;
	}
	
	public String bulkPromoId()
	{
		return m_promoId;
	}
	
	public Date promoStartDate()
	{
		return m_startDate;
	}
	
	public Date promoEndDate()
	{
		return m_endDate;
	}
	
	public String processedDeactivation()
	{
		return m_processedDeactivation;
	}
	
	public String cosID()
	{
		return m_cosID;
	}
	
	/*public static BulkPromo insert(Connection conn, String bulkPromoId, Date startDate, Date endDate)
	{
		logger.info("RBT::inside insert");
		
		Statement stmt = null;
		
		int id = -1;
		BulkPromo promo = null;
		
		String startDateString = "SYSDATE";
		if(startDate != null)
			startDateString = sqlTime(startDate);
		
		String endDateString = "SYSDATE+15";
		if(endDate != null)
			endDateString = sqlTime(endDate);
		
		String sql = "INSERT INTO " + TABLE_NAME + "(" +BULK_PROMO_ID_COL;
		sql += "," + START_DATE_COL;
		sql += "," + END_DATE_COL;
		sql += "," + PROCESSED_DEACTIVATION_COL + ") VALUES('";
		sql += bulkPromoId + "', ";
		sql += startDateString + ", ";
		sql += endDateString + ", ";
		sql += "'n')";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT::inside try");
			
			stmt = conn.createStatement();
			
			id = stmt.executeUpdate(sql);
			
		}
		catch(Exception e)
		{
			Tools.logException(_class, "insert", e);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				 Tools.logWarning(_class, "insert", "RBT::" +getStackTrace(e));
			}
		}
		
		if(id > 0)
		{
            logger.info("RBT::insertion to RBT_BULK_PROMO table successful");
            promo = new BulkPromoImpl(bulkPromoId, startDate, endDate, "n");
		}
		return promo;
	}*/
	
	public static BulkPromo insert(Connection conn, String bulkPromoId, Date startDate, Date endDate, String cosID)
	{
		logger.info("RBT::inside insert");
		
		Statement stmt = null;
		
		int id = -1;
		BulkPromo promo = null;
		
		String startDateString = "SYSDATE";
		if(startDate != null){
			if (m_databaseType.equals(DB_SAPDB)) {
				startDateString = sqlTime(startDate);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				startDateString = mySqlTime(startDate);
			}
		}
			
		
		String endDateString = "SYSDATE+15";
		if(endDate != null){
			if (m_databaseType.equals(DB_SAPDB)) {
				endDateString = sqlTime(endDate);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				endDateString = mySqlTime(endDate);
			}
		}
			
		
		String sql = "INSERT INTO " + TABLE_NAME + "(" +BULK_PROMO_ID_COL;
		sql += "," + START_DATE_COL;
		sql += "," + END_DATE_COL;
		sql += "," + PROCESSED_DEACTIVATION_COL;
		if(cosID!=null)
			sql += "," + COS_ID_COL;
		sql += ") VALUES('";
		sql += bulkPromoId + "', ";
		sql += startDateString + ", ";
		sql += endDateString + ", ";
		sql += "'n', ";
		if(cosID != null)
			sql += sqlString(cosID);
		sql += ")";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT::inside try");
			
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
            logger.info("RBT::insertion to RBT_BULK_PROMO table successful");
            promo = new BulkPromoImpl(bulkPromoId, startDate, endDate, "n", cosID);
		}
		return promo;
	}
	
	/*public static BulkPromo getBulkPromo(Connection conn, String bulkPromoId)
	{
		logger.info("RBT::inside getBulkPromo");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		BulkPromo promo = null;
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL + " ='" + bulkPromoId + "'";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			if(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				
				promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation);
			}
		}
		catch(Exception e)
		{
			Tools.logException(_class, "getBulkPromo", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				Tools.logException(_class, "getBulkPromo", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				Tools.logException(_class, "getBulkPromo", e);
			}
		}
		if(promo != null)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return promo;
		}
		return null;
	}*/
	
	public static BulkPromo getBulkPromo(Connection conn, String bulkPromoId)
	{
		logger.info("RBT::inside getBulkPromo");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		BulkPromo promo = null;
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL + " ='" + bulkPromoId + "'";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			if(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(promo != null)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return promo;
		}
		return null;
	}
	
	/*public static BulkPromo getActiveBulkPromo(Connection conn, String bulkPromoId)
	{
		logger.info("RBT::inside getActiveBulkPromo");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		BulkPromo promo = null;
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL + " = '" + bulkPromoId + "' AND " + END_DATE_COL + " >= SYSDATE";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			if(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				
				promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation);
			}
		}
		catch(Exception e)
		{
			Tools.logException(_class, "getActiveBulkPromo", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				Tools.logException(_class, "getActiveBulkPromo", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				Tools.logException(_class, "getActiveBulkPromo", e);
			}
		}
		if(promo != null)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return promo;
		}
		return null;
	}*/
	
	public static BulkPromo getActiveBulkPromo(Connection conn, String bulkPromoId)
	{
		logger.info("RBT::inside getActiveBulkPromo");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		BulkPromo promo = null;
		
		String sql=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL + " = '" + bulkPromoId + "' AND " + END_DATE_COL + " >= "+SAPDB_SYSDATE+"";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + BULK_PROMO_ID_COL + " = '" + bulkPromoId + "' AND " + END_DATE_COL + " >= "+MYSQL_SYSDATE+"";
		}
		
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			if(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(promo != null)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return promo;
		}
		return null;
	}
	
	/*public static BulkPromo[] getActiveBulkPromos(Connection conn)
	{
		logger.info("RBT::inside getActiveBulkPromos");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		ArrayList activePromoList = new ArrayList();
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " >= SYSDATE";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation);
				
				activePromoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(activePromoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])activePromoList.toArray(new BulkPromo[0]);
		}
		return null;
	}*/
	
	public static BulkPromo[] getBulkPromos(Connection conn)
	{
		logger.info("RBT::inside getActiveBulkPromos");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		ArrayList activePromoList = new ArrayList();
		
		String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+ START_DATE_COL + " DESC";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
				
				activePromoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(activePromoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])activePromoList.toArray(new BulkPromo[0]);
		}
		return null;
	}
	
	/*public static BulkPromo[] getPromosToDeactivateSubscribers(Connection conn)
	{
		logger.info("RBT::inside getPromosToDeactivateSubscribers");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		ArrayList promoList = new ArrayList();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		
		String dateStirng = toSQLDate(cal.getTime(), "YYYYMMDD", new SimpleDateFormat("yyyyMMdd"));
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " = " + dateStirng + " AND " + PROCESSED_DEACTIVATION_COL + " = 'n'";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation);
				
				promoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(promoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])promoList.toArray(new BulkPromo[0]);
		}
		return null;
	}*/
	
	/*public static boolean updateProcessedDeactivation(Connection conn, String bulkPromoId, String processedDeactivation)
	{
		logger.info("RBT::inside updateProcessedDeactivation");
		
		Statement stmt = null;
		int id = -1;
		
		String sql = null;

		sql = "UPDATE " + TABLE_NAME + " SET " + PROCESSED_DEACTIVATION_COL + " = '" + processedDeactivation + "' WHERE " + BULK_PROMO_ID_COL + " = '" + bulkPromoId + "'";
		
		logger.info("RBT::query - " +sql);
		
		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(sql);
		}
		catch(Exception e)
		{
			Tools.logException(_class, "updateProcessedDeactivation", e);
		}
		finally
		{
			
		}
		return (id >= 0);
	}*/
	
	public static BulkPromo[] getBulkPromosByStartDate(Connection conn, Date startDate)
	{
		logger.info("RBT::inside getActiveBulkPromos");
		
		String promoId;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		ArrayList activePromoList = new ArrayList();
		
		String sql=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			sql = "SELECT * FROM " + TABLE_NAME +" WHERE "+ START_DATE_COL +" = "+ sqlTime(startDate);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			sql = "SELECT * FROM " + TABLE_NAME +" WHERE "+ START_DATE_COL +" = "+ mySqlTime(startDate);
		}
		
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
				
				activePromoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(activePromoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])activePromoList.toArray(new BulkPromo[0]);
		}
		return null;
	}
	
	public static BulkPromo[] getBulkPromosByEndDate(Connection conn, Date endDate)
	{
		logger.info("RBT::inside getActiveBulkPromos");
		
		String promoId;
		Date startDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		ArrayList activePromoList = new ArrayList();
		
		String sql=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			sql = "SELECT * FROM " + TABLE_NAME +" WHERE "+ END_DATE_COL +" = "+ sqlTime(endDate);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			sql = "SELECT * FROM " + TABLE_NAME +" WHERE "+ END_DATE_COL +" = "+ mySqlTime(endDate);
		}
		
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
				
				activePromoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(activePromoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])activePromoList.toArray(new BulkPromo[0]);
		}
		return null;
	}
	
	public static BulkPromo[] getActiveBulkPromos(Connection conn)
	{
		logger.info("RBT::inside getActiveBulkPromos");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		ArrayList activePromoList = new ArrayList();
		String sql=null;
		if (m_databaseType.equals(DB_SAPDB)) {
			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " >= "+SAPDB_SYSDATE+"";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " >= "+MYSQL_SYSDATE+"";
		}
		
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
				
				activePromoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(activePromoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])activePromoList.toArray(new BulkPromo[0]);
		}
		return null;
	}
	
	public static BulkPromo[] getPromosToDeactivateSubscribers(Connection conn)
	{
		logger.info("RBT::inside getPromosToDeactivateSubscribers");
		
		String promoId;
		Date startDate;
		Date endDate;
		String processecDeactivation;
		String cosID;
		
		Statement stmt = null;
		RBTResultSet rs = null;
		
		ArrayList promoList = new ArrayList();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		
		String dateStirng = toSQLDate(cal.getTime(), "YYYYMMDD", new SimpleDateFormat("yyyyMMdd"));
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " = " + dateStirng + " AND " + PROCESSED_DEACTIVATION_COL + " = 'n'";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = new RBTResultSet(stmt.executeQuery(sql));
			
			while(rs.next())
			{
				promoId = rs.getString(BULK_PROMO_ID_COL);
				startDate = rs.getDate(START_DATE_COL);
				endDate = rs.getDate(END_DATE_COL);
				processecDeactivation = rs.getString(PROCESSED_DEACTIVATION_COL);
				cosID = rs.getString(COS_ID_COL);
				
				BulkPromo promo = new BulkPromoImpl(promoId, startDate, endDate, processecDeactivation, cosID);
				
				promoList.add(promo);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(promoList.size() > 0)
		{
			logger.info("RBT:: retrieving records from RBT_BULK_PROMO successful");
			return (BulkPromo[])promoList.toArray(new BulkPromo[0]);
		}
		return null;
	}
	
	public static boolean updateProcessedDeactivation(Connection conn, String bulkPromoId, String processedDeactivation)
	{
		logger.info("RBT::inside updateProcessedDeactivation");
		
		Statement stmt = null;
		int id = -1;
		
		String sql = null;

		sql = "UPDATE " + TABLE_NAME + " SET " + PROCESSED_DEACTIVATION_COL + " = '" + processedDeactivation + "' WHERE " + BULK_PROMO_ID_COL + " = '" + bulkPromoId + "'";
		
		logger.info("RBT::query - " +sql);
		
		try
		{
			stmt = conn.createStatement();
			id = stmt.executeUpdate(sql);
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			
		}
		return (id >= 0);
	}
}