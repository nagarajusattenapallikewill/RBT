package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.PromoMaster;

public class PromoMasterImpl extends RBTPrimitive implements PromoMaster
{
	private static Logger logger = Logger.getLogger(PromoMasterImpl.class);

	private static final String TABLE_NAME = "RBT_PROMO_MASTER";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String PROMO_TYPE_COL = "PROMO_TYPE";
	private static final String PROMO_CODE_COL = "PROMO_CODE";
	
	private static final String REF_TABLE_NAME = "RBT_CLIPS";

	private String m_clip_id;
	private String m_promo_type;
	private String m_promo_code;

	private PromoMasterImpl(String id, String type, String code)
	{
		m_clip_id = id;
		m_promo_type = type;
		m_promo_code = code;
	}

	public String clipID()
	{
		return m_clip_id;
	}

	public String promoType()
	{
		return m_promo_type;
	}

	public String promoCode()
	{
		return m_promo_code;
	}

	static PromoMaster insert(Connection conn, String clip_id, String type,
			String code)
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		PromoMasterImpl promoMaster = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + CLIP_ID_COL;
		query += ", " + PROMO_TYPE_COL;
		query += ", " + PROMO_CODE_COL;
		query += ")";

		query += " VALUES ( " + sqlString(clip_id);
		query += ", " + sqlString(type);
		query += ", " + sqlString(code);
		query += ")";

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			if (stmt.executeUpdate(query) > 0)
				id = 0;
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
		if (id == 0)
		{
			logger.info("RBT::insertion to RBT_SITE_PREFIX table successful");
			promoMaster = new PromoMasterImpl(clip_id, type, code);
			return promoMaster;
		}
		else
		{
			logger.info("RBT::insertion to RBT_SITE_PREFIX table failed");
			return null;
		}
	}

	static PromoMaster insertWithSequence(Connection conn, String clip_id, String type)
	{
		logger.info("RBT::inside insertWithSequence");

		int id = -1;
		String query = null;
		String querySeq = null;
		String SEQ = null;
		Statement stmt = null;
		ResultSet results = null;

		PromoMasterImpl promoMaster = null;

		querySeq = "select  RBT_PROMO_MASTER_SEQ.NEXTVAL from dual";


		logger.info("RBT::query " +query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(querySeq);
			results.next();
			SEQ = results.getString(1);

			query = "INSERT INTO " + TABLE_NAME + " ( " + CLIP_ID_COL;
			query += ", " + PROMO_TYPE_COL;
			query += ", " + PROMO_CODE_COL;
			query += ")";

			query += " VALUES ( " + sqlString(clip_id);
			query += ", " + sqlString(type);
			query += ", " + sqlString(SEQ);
			query += ")";


			if (stmt.executeUpdate(query) > 0)
				id = 0;
		}
		catch(SQLException se)
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
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(id == 0)
		{
			logger.info("RBT::insertion to RBT_PROMO_MASTER table successful");
			promoMaster = new PromoMasterImpl(clip_id, type, SEQ);
			return promoMaster;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_PROMO_MASTER table failed");
			return null;
		}
	}

	static boolean update(Connection conn, String code, String type)
	{
		logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + PROMO_TYPE_COL + " = "
		+ sqlString(type) + " WHERE " + PROMO_CODE_COL + " = "
		+ sqlString(code);

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

	static boolean remove(Connection conn, String id)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "DELETE FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL
		+ " = " + "'" + id + "'";

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

	static PromoMaster[] getAllPromos(Connection conn)
	{
		logger.info("RBT::inside getAllPromos");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String id = null;
		String type = null;
		String code = null;

		PromoMasterImpl promos = null;
		ArrayList<PromoMaster> promoList = new ArrayList<PromoMaster>();

		query = "SELECT * FROM " + TABLE_NAME;

		logger.info("RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				id = results.getString(CLIP_ID_COL);
				type = results.getString(PROMO_TYPE_COL);
				code = results.getString(PROMO_CODE_COL);

				promos = new PromoMasterImpl(id, type, code);
				promoList.add(promos);
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

		if (promoList.size() > 0)
		{
			logger.info("RBT::retrieving records from " + TABLE_NAME
					+ " successful");
			return promoList.toArray(new PromoMaster[0]);
		}
		else
		{
			logger.info("RBT::no records in "
					+ TABLE_NAME);
			return null;
		}
	}

	static PromoMaster getPromoForTypeAndCode(Connection conn, String type,
			String code)
	{
		logger.info("RBT::inside getPromoForTypeAndCode");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String id = null;
		String strType = null;
		String strCode = null;

		PromoMasterImpl promos = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER("
		+ PROMO_TYPE_COL + ") = " + sqlString(type.toLowerCase())
		+ " AND LOWER(" + PROMO_CODE_COL + ") = "
		+ sqlString(code.toLowerCase());

		logger.info("RBT::query "
				+ query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				id = results.getString(CLIP_ID_COL);
				strType = results.getString(PROMO_TYPE_COL);
				strCode = results.getString(PROMO_CODE_COL);

				promos = new PromoMasterImpl(id, strType, strCode);
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
		return promos;
	}

	static PromoMaster[] getPromoForCode(Connection conn, String code)
	{
		logger.info(
		"RBT::inside getPromoForCode");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String id = null;
		String strType = null;
		String strCode = null;

		PromoMasterImpl promos = null;
		ArrayList<PromoMaster> promoList = new ArrayList<PromoMaster>();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER("
		+ PROMO_CODE_COL + ") = " + sqlString(code.toLowerCase());

		logger.info( "RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				id = results.getString(CLIP_ID_COL);
				strType = results.getString(PROMO_TYPE_COL);
				strCode = results.getString(PROMO_CODE_COL);

				promos = new PromoMasterImpl(id, strType, strCode);
				promoList.add(promos);
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
		if (promoList.size() > 0)
		{
			logger.info("RBT::retrieving records from " + TABLE_NAME
					+ " successful");
			return promoList.toArray(new PromoMaster[0]);
		}
		else
		{
			logger.info("RBT::no records in "
					+ TABLE_NAME);
			return null;
		}
	}

	static PromoMaster getPromoByCode(Connection conn, String code)
	{
		logger.info("RBT::inside getPromoForCode");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String id = null;
		String strType = null;
		String strCode = null;

		PromoMasterImpl promos = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROMO_CODE_COL
				+ " = " + sqlString(code);

		logger.info( "RBT::query " + query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				id = results.getString(CLIP_ID_COL);
				strType = results.getString(PROMO_TYPE_COL);
				strCode = results.getString(PROMO_CODE_COL);

				promos = new PromoMasterImpl(id, strType, strCode);
				return(promos);
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
		return null;
	}
	
	static void getPromoIDClipIDMap(Connection conn, Hashtable<String, String> promoIDMap) {
		String method = "getPromoIDClipIDMap";
		logger.info("RBT::inside " + method);
		
		String query = "SELECT A." + PROMO_TYPE_COL + ", A." + PROMO_CODE_COL + " FROM " + TABLE_NAME
				+ " A, " + REF_TABLE_NAME + " B WHERE A.PROMO_TYPE = B.CLIP_ID AND"
				+ " B.CLIP_PROMO_ID IS NOT NULL AND B.CLIP_START_TIME < SYSDATE AND"
				+ " B.CLIP_END_TIME >= SYSDATE";
		
		logger.info("RBT:: query is " + query);
		Statement stmt = null;
		RBTResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = new RBTResultSet(stmt.executeQuery(query));
			while(rs.next()) {
				promoIDMap.put(rs.getString(PROMO_CODE_COL), rs.getString(PROMO_TYPE_COL));
			}
		}
		catch (SQLException e) {
			logger.error("", e);
		}
		finally {
			try {
				if(rs != null)
					rs.close();
			}
			catch(Exception e) {
				
			}
			try {
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e) {
				
			}
		}
	}
	
	static PromoMaster getPromoCodeByPromoType(Connection conn, String type){ 
	    
        logger.info("RBT::inside getPromoCodeByPromoType"); 

        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 

        String id = null; 
        String strType = null; 
        String strCode = null; 

        PromoMaster promoList = null; 
        if(type!=null && type.length()>0){ 
                type=type.trim(); 
        }else{ 
                return null; 
        } 
        query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" 
        + PROMO_TYPE_COL + ") = " + sqlString(type.toLowerCase()); 

        logger.info("RBT::query " + query); 

        try 
        { 
                logger.info("RBT::inside try block"); 
                stmt = conn.createStatement(); 
                results = stmt.executeQuery(query); 
                if(results.next()) 
                { 
                        id = results.getString(CLIP_ID_COL); 
                        strType = results.getString(PROMO_TYPE_COL); 
                        strCode = results.getString(PROMO_CODE_COL); 

                        promoList = new PromoMasterImpl(id, strType, strCode); 
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
        if (promoList!= null) 
        { 
                logger.info("RBT::retrieving records from " + TABLE_NAME 
                                + " successful"); 
                return  promoList; 
        } 
        else 
        { 
                logger.info("RBT::no records in " 
                                + TABLE_NAME); 
                return null; 
        } 
	} 

}