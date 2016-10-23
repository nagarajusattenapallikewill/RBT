package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberPromo;

public class SubscriberPromoImpl extends RBTPrimitive implements SubscriberPromo
{
    private static Logger logger = Logger.getLogger(SubscriberPromoImpl.class);
    
    private static final String TABLE_NAME = "RBT_SUBSCRIBER_PROMO";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
    private static final String FREEDAYS_COL = "NUM_FREE_DAYS";
	private static final String START_DATE_COL = "START_DATE";
	private static final String END_DATE_COL = "END_DATE";
	private static final String ACTIVATED_BY_COL = "ACTIVATED_BY";
	private static final String SUBSCRIPTION_TYPE_COL = "SUBSCRIPTION_TYPE";
	
    private String m_subscriberID;
	private int m_free_days;
    private Date m_startDate;
	private Date m_endDate;
    private boolean m_isPrepaid;
	private String m_activatedBy;
	private String m_subscription_type;
	private static String m_databaseType=getDBSelectionString();
	
	private SubscriberPromoImpl(String subscriberID, int freedays, Date startDate, Date endDate, boolean isPrepaid, String activatedBy, String sub_type)
	{
		m_subscriberID	= subscriberID;
        m_free_days	= freedays;
		m_startDate	= startDate;
        m_endDate = endDate;
        m_isPrepaid	= isPrepaid;
		m_activatedBy = activatedBy;
		m_subscription_type = sub_type;
	}

    public String subID()
	{
		return m_subscriberID;
	}

    public int freedays()
	{
		return m_free_days;
	}

	public boolean isPrepaid()
	{
		return m_isPrepaid;
	}

	public Date startDate()
	{
		return m_startDate;
	}

	public Date endDate()
	{
		return m_endDate;
	}
	
	public String activatedBy()
	{
		return m_activatedBy;
	}

	public String subType()
	{
		return m_subscription_type;
	}

	static SubscriberPromo insert(Connection conn, String subscriberID, int freedays, boolean isPrepaid, String activatedBy, String subType)
    {
        logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		SubscriberPromoImpl subscriberPromo = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + FREEDAYS_COL;
		query += ", " + START_DATE_COL;
		query += ", " + END_DATE_COL;
		query += ", " + ACTIVATED_BY_COL;
		query += ", " + SUBSCRIPTION_TYPE_COL;
		query += ")";
			
		query += " VALUES ( " + "'" + subscriberID + "'";
		if(isPrepaid)
		query += ",'y'";
		else
		query += ",'n'";
		query += ", " + sqlInt(freedays);
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += ",SYSDATE";
		else
			query += ",SYSDATE()";
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += ",TO_DATE('20371231','YYYYMMDD')";
		else
			query += ",TIMESTAMP('2037-12-31')";
		query += ", " + sqlString(activatedBy);
		query += ", " + sqlString(subType);
		query += ")";
		
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
            logger.info("RBT::insertion to RBT_SUBSCRIBER_PROMO table successful");
			Calendar cal = Calendar.getInstance();
			cal.set(2037, Calendar.DECEMBER, 31, 0, 0, 0);
			Date endDate = cal.getTime();

			cal = Calendar.getInstance();
			Date startDate = cal.getTime();
            subscriberPromo = new SubscriberPromoImpl(subscriberID, freedays, startDate, endDate, isPrepaid, activatedBy, subType);
            return subscriberPromo;
        } 
		else
        {
            logger.info("RBT::insertion to RBT_SUBSCRIBER_PROMO table failed");
            return null;
        }
    }
	
    static boolean endPromo(Connection conn, String subscriberID, String type)
	{
        logger.info("RBT::inside endPromo");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " +
				 END_DATE_COL + " = SYSDATE " + 
				 " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + SUBSCRIPTION_TYPE_COL + " = " +sqlString(type);
		else
			query = "UPDATE " + TABLE_NAME + " SET " +
			 END_DATE_COL + " = SYSDATE() " + 
			 " WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "' AND " + SUBSCRIPTION_TYPE_COL + " = " +sqlString(type);

		logger.info("RBT::query "+query);
		
		try
        {
            logger.info("RBT::inside try block");		
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			n = stmt.getUpdateCount();
        }
        catch(Exception se)
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
	
    static SubscriberPromo getActiveSubscriberPromo(Connection conn, String subID, String type)
    {
        logger.info("RBT::inside getActiveSubscriberPromo");
     
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		int freedays = 0;
		boolean isPrepaid = false;
   		Date startDate = null;
   		Date endDate = null;
		String activatedBy = null;
		String subType = null;

		SubscriberPromoImpl subscriberPromo = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND START_DATE < SYSDATE AND END_DATE > SYSDATE AND "+ SUBSCRIPTION_TYPE_COL + " = " + sqlString(type);
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND START_DATE < SYSDATE() AND END_DATE > SYSDATE() AND "+ SUBSCRIPTION_TYPE_COL + " = " + sqlString(type);
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				freedays = results.getInt(FREEDAYS_COL);
				isPrepaid = results.getString(PREPAID_YES_COL).equalsIgnoreCase("y");
				startDate = results.getTimestamp(START_DATE_COL);
				endDate = results.getTimestamp(END_DATE_COL);
				activatedBy = results.getString(ACTIVATED_BY_COL);
				subType = results.getString(SUBSCRIPTION_TYPE_COL);

				subscriberPromo = new SubscriberPromoImpl(subscriberID, freedays, startDate, endDate, isPrepaid, activatedBy, subType);
			}
        }
        catch(Exception se)
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
		return subscriberPromo;
    }
    
    static SubscriberPromo getActiveSubscriberPromoSubType(Connection conn, String subID, String subType)
    {
    	String method = "getActiveSubscriberPromoFSubType";
    	logger.info("RBT::inside " + method);
    	
    	String query = null;
    	Statement stmt = null;
    	ResultSet results = null;
    	
    	String subscriberID = null;
		int freedays = 0;
		boolean isPrepaid = false;
   		Date startDate = null;
   		Date endDate = null;
		String activatedBy = null;
		//String subType1 = null;
	 
    	SubscriberPromo subscriberPromo = null;
    	
    	if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
    		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID +
    		"' AND START_DATE < SYSDATE AND END_DATE > SYSDATE AND " + SUBSCRIPTION_TYPE_COL + " = '" +
    		subType + "'";
    	else
    		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID +
    		"' AND START_DATE < SYSDATE() AND END_DATE > SYSDATE() AND " + SUBSCRIPTION_TYPE_COL + " = '" +
    		subType + "'";
	 
    	logger.info("RBT::query "+query);
	 
    	try
    	{
    		logger.info("RBT::inside try block");
    		stmt = conn.createStatement();
    		results = stmt.executeQuery(query);
    		while (results.next())
    		{
    			subscriberID = results.getString(SUBSCRIBER_ID_COL);
				freedays = results.getInt(FREEDAYS_COL);
				isPrepaid = results.getString(PREPAID_YES_COL).equalsIgnoreCase("y");
				startDate = results.getTimestamp(START_DATE_COL);
				endDate = results.getTimestamp(END_DATE_COL);
				activatedBy = results.getString(ACTIVATED_BY_COL);
				subType = results.getString(SUBSCRIPTION_TYPE_COL);

				subscriberPromo = new SubscriberPromoImpl(subscriberID, freedays, startDate, endDate, isPrepaid, activatedBy, subType);
    		}
    	}
    	catch(Exception se)
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
    	return subscriberPromo;
    }

	static SubscriberPromo getSubscriberPromo(Connection conn, String subID, String actBy, String type)
    {
        logger.info("RBT::inside getSubscriberPromo");
     
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		int freedays = 0;
		boolean isPrepaid = false;
   		Date startDate = null;
   		Date endDate = null;
		String activatedBy = null;
		String subType = null;

		SubscriberPromoImpl subscriberPromo = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + SUBSCRIPTION_TYPE_COL + " = " + sqlString(type);
		if(actBy != null)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subID + "' AND " + ACTIVATED_BY_COL + " = " + sqlString(actBy) + " AND " + SUBSCRIPTION_TYPE_COL + " = " + sqlString(type);;
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				freedays = results.getInt(FREEDAYS_COL);
				isPrepaid = results.getString(PREPAID_YES_COL).equalsIgnoreCase("y");
				startDate = results.getTimestamp(START_DATE_COL);
				endDate = results.getTimestamp(END_DATE_COL);
				activatedBy = results.getString(ACTIVATED_BY_COL);
				subType = results.getString(SUBSCRIPTION_TYPE_COL);

				subscriberPromo = new SubscriberPromoImpl(subscriberID, freedays, startDate, endDate, isPrepaid, activatedBy, subType);
			}
        }
        catch(Exception se)
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
		return subscriberPromo;
    }
	
	static boolean changeActivatedBy(Connection conn, String subscriberID, String activatedBy)
	{
        logger.info("RBT::inside changeActivatedBy");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 ACTIVATED_BY_COL + " = " + sqlString(activatedBy) + 
				" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'";
		
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
	
	//added by sreekar for non subscriber MM category limited time feature
	static boolean changeFreeDays(Connection conn, String subscriberID, int freeDays)
	{
        logger.info("RBT::inside changeFreeDays");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 FREEDAYS_COL + " = " + freeDays + 
				" WHERE " + SUBSCRIBER_ID_COL  + " = " + "'" + subscriberID + "'";
		
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

	static boolean remove(Connection conn, String subscriberID, String activatedBy, String type)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "'";
		if(activatedBy != null)
			query += " AND " + ACTIVATED_BY_COL + " = " + sqlString(activatedBy);
		if(type != null)
			query += " AND " + SUBSCRIPTION_TYPE_COL + " = " + sqlString(type);
		
		logger.info("RBT::query " +query);
		
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
	
	static SubscriberPromo[] getAllActiveSubscriberPromo(Connection conn)
    {
		String method = "getAllActiveSubscriberPromo";
        logger.info("RBT::inside " + method);
     
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		String subscriberID = null;
		int freedays = 0;
		boolean isPrepaid = false;
   		Date startDate = null;
   		Date endDate = null;
		String activatedBy = null;
		String subType = null;

		ArrayList subsList = new ArrayList();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " >= SYSDATE";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " >= SYSDATE()";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				freedays = results.getInt(FREEDAYS_COL);
				isPrepaid = results.getString(PREPAID_YES_COL).equalsIgnoreCase("y");
				startDate = results.getTimestamp(START_DATE_COL);
				endDate = results.getTimestamp(END_DATE_COL);
				activatedBy = results.getString(ACTIVATED_BY_COL);
				subType = results.getString(SUBSCRIPTION_TYPE_COL);

				SubscriberPromoImpl subscriberPromo = new SubscriberPromoImpl(subscriberID, freedays, startDate, endDate, isPrepaid, activatedBy, subType);
				subsList.add(subscriberPromo);
			}
        }
        catch(Exception se)
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
		
		if(subsList.size() > 0)
		{
			logger.info("RBT:: retrieving records from table " + TABLE_NAME + "successfull");
			return (SubscriberPromo[])subsList.toArray(new SubscriberPromo[0]);
		}
		else
		{
			logger.info("RBT:: no active records in table " + TABLE_NAME);
			return null;
		}
    }

	
	static void deleteOldSubscriberPromos(Connection conn)
	{
		String method = "deleteOldSubscriberPromos";
		logger.info("RBT::inside " + method);
	 
		String query = null;
		Statement stmt = null;
	 
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " < SYSDATE";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + END_DATE_COL + " < SYSDATE()";
	 
		logger.info("RBT::query "+query);
	 
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(Exception se)
		{
			logger.error("", se);
			return;
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
	}
	
}