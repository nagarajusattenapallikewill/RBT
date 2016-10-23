package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.PromoTable;

public class PromoTableImpl extends RBTPrimitive implements PromoTable
{
	private static Logger logger = Logger.getLogger(PromoTableImpl.class);
	
    private static final String TABLE_NAME = "RBT_PROMO_TABLE";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String PROMO_ID_COL = "PROMO_ID";
	private static final String PROMO_CLIPS_SMS_COL = "PROMO_CLIPS_SMS";

	private String m_subscriberId;
    private String m_promoId;
    private String m_promoClipsSMS;
    
	private PromoTableImpl(String subscriberId, String promoId, String promoClipsSMS)
	{
		m_subscriberId = subscriberId;
		m_promoId = promoId;
		m_promoClipsSMS = promoClipsSMS;
	}
	
	public String subscriberId()
	{
		return m_subscriberId;
	}
	
	public String promoId()
    {
        return m_promoId;
    }
	
	public String promoClipsSMS()
    {
        return m_promoClipsSMS;
    }
	
	static PromoTable insert(Connection conn, String subscriberId, String promoId, String promoClipsSMS)
	{
		logger.info("RBT::inside insert");
		
      	String query = null;
		Statement stmt = null;
		
		PromoTableImpl promoTable = null;
		
		int id = -1;
		
		query = "INSERT INTO " + TABLE_NAME + " (" + SUBSCRIBER_ID_COL + ", " + PROMO_ID_COL + ", " + PROMO_CLIPS_SMS_COL + ") ";
				query+="VALUES('" + subscriberId + "', '" + promoId + "', '" + promoClipsSMS + "')";
				
		logger.info("RBT::query "+query);
		
		try
		{
            logger.info("RBT::inside try block");  
            stmt = conn.createStatement();
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
            logger.info("RBT::insertion to RBT_PROMO_TABLE table successful");
            promoTable = new PromoTableImpl(subscriberId, promoId, promoClipsSMS);
            return promoTable;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_PROMO_TABLE table failed");
            return null;
        }
	}
	
	static PromoTable getPromoTable(Connection conn, String subscriberId)
	{
		try
		{
			return getPromoTable(conn, subscriberId, false);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	static PromoTable getPromoTable(Connection conn, String subscriberId, boolean bShouldExceptionBeThrown) throws SQLException
    {
        logger.info("RBT::inside getPromoDetail");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		String promoId = null;
		String promoClipsSMS = null;
		
		PromoTableImpl promoTable = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberId + "'";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					if (results.next())
					{
						promoId = results.getString(PROMO_ID_COL);
						promoClipsSMS = results.getString(PROMO_CLIPS_SMS_COL);
						
						promoTable = new PromoTableImpl(subscriberId, promoId, promoClipsSMS);
					}
		}
        catch(SQLException se)
        {
        	logger.error("", se);
            if(bShouldExceptionBeThrown)
			{
					throw se;
			}
			else
			{
					return null;
			}
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
        return promoTable;
    }
	
	static boolean remove(Connection conn, String subscriberID)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		
		logger.info("RBT::query "+query);
		
		try
		{
		    logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
		}
		catch(SQLException se)
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
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		return(n==1);
	}
}