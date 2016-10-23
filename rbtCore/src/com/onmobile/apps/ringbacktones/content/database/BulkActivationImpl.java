/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.BulkActivation;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkActivationImpl implements BulkActivation
{
	private static Logger logger = Logger.getLogger(BulkActivationImpl.class);

	private static final String TABLE_NAME = "RBT_BULK_ACTIVATION";
	private static final String FILE_NAME_COL = "FILE_NAME";
	private static final String BULK_PROMO_ID_COL = "BULK_PROMO_ID";
	private static final String IN_DATE_COL = "IN_DATE";
	private static final String STATUS_COL = "STATUS";
	private static final String PROMO_ID_COL = "PROMO_ID";
	private static final String CATEGORY_TYPE_COL = "CATEGORY_TYPE";

	private String fileName = null;
	private String bulkPromoID = null;
	private Timestamp inDate = null;
	private String status = null;
	private String promoID = null;
	private int categoryType;

	

	private BulkActivationImpl(String fileName, String bulkPromoID, Timestamp inDate, String status, String promoID, int categoryType)
	{
		this.fileName = fileName;
		this.bulkPromoID = bulkPromoID;
		this.inDate =inDate;
		this.status = status;
		this.promoID = promoID;
		this.categoryType = categoryType;
	}

	public String fileName() 
	{
		return fileName;
	}

	public String bulkPromoID() 
	{
		return bulkPromoID;
	}

	public Timestamp inDate() 
	{
		return inDate;
	}

	public String status() 
	{
		return status;
	}

	public String promoID()
	{
		return promoID;
	}

	public int categoryType()
	{
		return categoryType;
	}

	public static BulkActivation insert(Connection conn, String fileName, String bulkPromoID, String status, String promoID, int categoryType)
	{
		logger.info("RBT::inside insert");

		Statement stmt = null;

		int id = -1;
		Timestamp inDate = new Timestamp(System.currentTimeMillis());
		try
		{			
			stmt = conn.createStatement();

			String sql = "INSERT INTO " + TABLE_NAME + " (" + FILE_NAME_COL;
			sql += ", " + BULK_PROMO_ID_COL;
			sql += ", " + IN_DATE_COL;
			sql += ", " + STATUS_COL;
			sql += ", " + PROMO_ID_COL;
			sql += ", " + CATEGORY_TYPE_COL + ")";

			sql += " VALUES(";
			sql += "'" + fileName + "'";
			sql += ", '" + bulkPromoID + "'";
			sql += ", '" + inDate + "'";
			sql += ", '" + status + "'";
			sql += ", '" + promoID + "'";
			sql += ", " + categoryType + ")";

			logger.info("RBT::query="+ sql);

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
			logger.info("RBT::insertion into RBT_BULK_ACTIVATION_STATUS successful");
			return new BulkActivationImpl(fileName, bulkPromoID, inDate, status, promoID, categoryType);
		}
		return null;
	}
	
	public static BulkActivation[] getAllBulkActivation(Connection conn)
	{
		logger.info("RBT::inside getAllBulkActivation");
		Statement stmt = null;
		RBTResultSet rs = null;
		String sql = null;

		BulkActivation bulkActivation = null;
		ArrayList bulkActivationList = new ArrayList();
		try
		{
			stmt = conn.createStatement();

			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = 'PENDING' ORDER BY " + FILE_NAME_COL;
			logger.info("RBT::query="+ sql);
			rs = new RBTResultSet(stmt.executeQuery(sql));

			while(rs.next())
			{			
				bulkActivation = getBulkActivationFromRS(rs);
				bulkActivationList.add(bulkActivation);
			}

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
		if(bulkActivationList.size() > 0)
			return((BulkActivation[]) bulkActivationList.toArray(new BulkActivationImpl[0]));
			
		return null;
	}

	public static BulkActivation getPendingBulkActivation(Connection conn)
	{
		logger.info("RBT::inside getPendingBulkActivation");
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;

		try
		{
			stmt = conn.createStatement();

			sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COL + " = 'PENDING' ORDER BY " + FILE_NAME_COL;
			logger.info("RBT::query="+ sql);
			rs = stmt.executeQuery(sql);

			if(rs.next())
			{			
				return (getBulkActivationFromRS(rs));
			}

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
		return null;
	}

	public static boolean updateBulkActivationStatus(Connection conn, String fileName, String status)
	{
		logger.info("RBT::inside updateBulkActivationStatus");

		Statement stmt = null;
		int id = -1;
		String sql = null;

		try
		{
			stmt = conn.createStatement();

			sql = "UPDATE " + TABLE_NAME + " SET " + STATUS_COL + " = '" + status + "' WHERE " + FILE_NAME_COL + " = '" + fileName + "'";

			logger.info("RBT:: query " + sql);

			id = stmt.executeUpdate(sql);

			if(id > 0)
			{
				logger.info("RBT::updation to RBT_BULK_ACTIVVATION_STATUS successful");
				return true;
			}

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
		return false;
	}
	
	private static BulkActivationImpl getBulkActivationFromRS(ResultSet rs) throws SQLException
	{
		RBTResultSet resultSet = new RBTResultSet(rs);
		if(resultSet != null)
		{
			String fileName = resultSet.getString(FILE_NAME_COL);
			String bulkPromoID = resultSet.getString(BULK_PROMO_ID_COL);
			Timestamp inDate = resultSet.getTimestamp(IN_DATE_COL);
			String status = resultSet.getString(STATUS_COL);
			String promoID = resultSet.getString(PROMO_ID_COL);
			int categoryType = resultSet.getInt(CATEGORY_TYPE_COL);

			return(new BulkActivationImpl(fileName, bulkPromoID, inDate, status, promoID, categoryType));
		}
		return null;
	}
}
