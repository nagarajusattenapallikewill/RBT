package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberCDR;

public class SubscriberCDRImpl extends RBTPrimitive implements SubscriberCDR
{
    private static Logger logger = Logger.getLogger(SubscriberCDRImpl.class);
    
    private static final String TABLE_NAME = "RBT_SUBSCRIBER_CDR";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String SELECTION_TIME_COL = "SELECTION_TIME";
    private static final String CALLER_ID_COL = "CALLER_ID";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
    private static final String SUBSCRIBER_WAV_FILE_COL = "SUBSCRIBER_WAV_FILE";
    private static final String STATUS_COL = "STATUS";
    private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String CLASS_TYPE_COL = "CLASS_TYPE";
	private static final String SELECTED_BY_COL = "SELECTED_BY";
	private static final String SELECTION_INFO_COL = "SELECTION_INFO";
  
    private String m_subscriberID;
	private Date m_selectionDate;
	private String m_callerID;
	private int m_categoryID;
    private String m_subscriberWavFile;
    private int m_status;
    private String m_prepaid;
	private String m_classType;
	private String m_selectedBy;
	private String m_selectionInfo;
	private static String m_databaseType=getDBSelectionString();

	private SubscriberCDRImpl(String subscriberID, Date selectionDate, String callerID, int categoryID, String subscriberWavFile, int status, String prepaid, String classType, String selectedBy, String selectionInfo)
	{
		m_subscriberID = subscriberID;
		m_selectionDate = selectionDate;
		m_callerID = callerID;
		m_categoryID = categoryID;
	    m_subscriberWavFile = subscriberWavFile;
	    m_status = status;
	    m_prepaid = prepaid;
		m_classType = classType;
		m_selectedBy = selectedBy;
		m_selectionInfo = selectionInfo;
	}

    public String subID()
	{
		return m_subscriberID;
	}

	public Date selectionDate()
	{
		return m_selectionDate;
	}
	
	public String callerID()
	{
		return m_callerID;
	}

    public int categoryID()
	{
		return m_categoryID;
	}
	
	public String subscriberFile()
	{
		return m_subscriberWavFile;
	}
	
	public int status()
	{
		return m_status;
	}
	
	public boolean prepaidYes()
	{
		if(m_prepaid!= null)
		return m_prepaid.equalsIgnoreCase("y");
		return false;
	}

	public String classType()
	{
		return m_classType;
	}

	public String selectedBy()
	{
		return m_selectedBy;
	}

	public String selectionInfo()
	{
		return m_selectionInfo;
	}
    	
    public String date(Date date)
    {
    	DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	
        return sqlTimeFormat.format(date);
    }

    static SubscriberCDR insert(Connection conn, String subscriberID, Date selectionDate, String callerID, int categoryID, String subscriberWavFile, int status, String prepaid, String classType, String selectedBy, String selectionInfo)
    {
        logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;
		
		String date = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			date = "SYSDATE";
			if(selectionDate != null)
				date = sqlTime(selectionDate);
		}
		else
		{
			date = "SYSDATE()";
			if(selectionDate != null)
				date = mySQLDateTime(selectionDate);
		}

		SubscriberCDRImpl subscriberCDR = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + SELECTION_TIME_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CATEGORY_ID_COL;
		query += ", " + SUBSCRIBER_WAV_FILE_COL;
		query += ", " + STATUS_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + CLASS_TYPE_COL;
		query += ", " + SELECTED_BY_COL;		
		query += ", " + SELECTION_INFO_COL;
		query += ")";
			
		query += " VALUES ( " + "'" + subscriberID + "'";
		query += ", " + date;
		if(callerID == null)
		query += ", " + null;
		else
		query += ", " + sqlString(callerID);
		query += ", " + sqlInt(categoryID);
		query += ", " + sqlString(subscriberWavFile);
		query += ", " + status;
		query += ", " + "'" + prepaid + "'";
		query += ", " + sqlString(classType);
		query += ", " + sqlString(selectedBy);
		query += ", " + sqlString(selectionInfo);
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
            logger.info("RBT::insertion to RBT_SUBSCRIBER_CDR table successful");
            subscriberCDR = new SubscriberCDRImpl(subscriberID, selectionDate, callerID, categoryID, subscriberWavFile, status, prepaid, classType, selectedBy, selectionInfo);
            return subscriberCDR;
        } 
		else
        {
            logger.info("RBT:insertion to RBT_SUBSCRIBER_CDR table failed");
            return null;
        }
    }
		
    static SubscriberCDR [] getSubscriberCDR(Connection conn, String date)
    {
        logger.info("RBT::inside getSubscriberCDR");
        
   		String query = null;
   		Statement stmt = null;
   		ResultSet results = null;

   		String subscriberID = null;
		Date selectionDate = null;
		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		int status = -1;
		String prepaid = null;
		String classType = null;
		String selectedBy = null;
		String selectionInfo = null;

		SubscriberCDRImpl subscriberCDR = null;
		List subscriberCDRList = new ArrayList();

   		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SELECTION_TIME_COL + "<= "+date;
   		
   		logger.info("RBT::query "+query);
   	
           try
           {
              logger.info("RBT::inside try block");
   				stmt = conn.createStatement();
   				results = stmt.executeQuery(query);
   					while (results.next())
   					{
   						subscriberID = results.getString(SUBSCRIBER_ID_COL);
   						selectionDate = results.getTimestamp(SELECTION_TIME_COL);
   						callerID = results.getString(CALLER_ID_COL);
						categoryID = results.getInt(CATEGORY_ID_COL);
						subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
						status = results.getInt(STATUS_COL);
						prepaid = results.getString(PREPAID_YES_COL);
						classType = results.getString(CLASS_TYPE_COL);
						selectedBy = results.getString(SELECTED_BY_COL);
						selectionInfo = results.getString(SELECTION_INFO_COL);

   						subscriberCDR = new SubscriberCDRImpl(subscriberID, selectionDate, callerID, categoryID, subscriberWavFile, status, prepaid, classType, selectedBy, selectionInfo);
						subscriberCDRList.add(subscriberCDR);
   					}
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
		   if(subscriberCDRList.size() > 0)
           {
               logger.info("RBT::retrieving records from RBT_SUBSCRIBER_CDR successful");
               return (SubscriberCDR[])subscriberCDRList.toArray(new SubscriberCDR[0]);
           } 
   		   else
           {
               logger.info("RBT::no records in RBT_SUBSCRIBER_CDR");
               return null;
           }
    }

	static SubscriberCDR [] getCDR(Connection conn, String subID)
	{
		 logger.info("RBT::inside getCDR");
        
   		String query = null;
   		Statement stmt = null;
   		ResultSet results = null;

   		String subscriberID = null;
		Date selectionDate = null;
		String callerID = null;
		int categoryID = -1;
		String subscriberWavFile = null;
		int status = -1;
		String prepaid = null;
		String classType = null;
		String selectedBy = null;
		String selectionInfo = null;

		SubscriberCDRImpl subscriberCDR = null;
		List subscriberCDRList = new ArrayList();

	
   		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
   			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND " + PREPAID_YES_COL + " IN ('y', 'Y') AND TO_CHAR(" + SELECTION_TIME_COL + ", 'YYYY/MM/DD') LIKE TO_CHAR(SYSDATE, 'YYYY/MM/DD')";
   		else
   			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND " + PREPAID_YES_COL + " IN ('y', 'Y') AND DATE_FORMAT(" + SELECTION_TIME_COL + ", '%Y %m %d') LIKE DATE_FORMAT(SYSDATE(), '%Y %m %d')";
   		
   		logger.info("RBT::query "+query);
   	
           try
           {
              logger.info("RBT::inside try block");
   				stmt = conn.createStatement();
   				results = stmt.executeQuery(query);
   					while (results.next())
   					{
   						subscriberID = results.getString(SUBSCRIBER_ID_COL);
   						selectionDate = results.getTimestamp(SELECTION_TIME_COL);
   						callerID = results.getString(CALLER_ID_COL);
						categoryID = results.getInt(CATEGORY_ID_COL);
						subscriberWavFile = results.getString(SUBSCRIBER_WAV_FILE_COL);
						status = results.getInt(STATUS_COL);
						prepaid = results.getString(PREPAID_YES_COL);
						classType = results.getString(CLASS_TYPE_COL);
						selectedBy = results.getString(SELECTED_BY_COL);
						selectionInfo = results.getString(SELECTION_INFO_COL);

   						subscriberCDR = new SubscriberCDRImpl(subscriberID, selectionDate, callerID, categoryID, subscriberWavFile, status, prepaid, classType, selectedBy, selectionInfo);
						subscriberCDRList.add(subscriberCDR);
   					}
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
		   if(subscriberCDRList.size() > 0)
           {
               logger.info("RBT::retrieving records from RBT_SUBSCRIBER_CDR successful");
               return (SubscriberCDR[])subscriberCDRList.toArray(new SubscriberCDR[0]);
           } 
   		   else
           {
               logger.info("RBT::no records in RBT_SUBSCRIBER_CDR");
               return null;
           }
	}
        
	static boolean remove(Connection conn, String subscriberID, Date selectionTime)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SELECTION_TIME_COL + " <= " + sqlTime(selectionTime);
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = " + "'" + subscriberID + "' AND " + SELECTION_TIME_COL + " <= " + mySqlTime(selectionTime);
		
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
	
	static boolean remove(PreparedStatement stmt, String subscriberID, Date selectionTime)
	{
		logger.info("RBT::inside remove");

		try
		{
			stmt.setString(1, subscriberID);
			stmt.setTimestamp(2, new Timestamp(selectionTime.getTime()));
			stmt.addBatch();
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return false;
		}
		return true;
	}

	static int removeSubscriberCDR(Connection conn, String lasttime)
	{
		logger.info("RBT::inside removeSubscriberCDR");
		int count = 0;
		String query;	
		Statement stmt = null;
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SELECTION_TIME_COL + " <= " + lasttime;
		
		logger.info("RBT::iquery "+query);
		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			count = stmt.getUpdateCount();
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return 0;
		}
		return count;
	}
	
	static int removeCDR(Connection conn, String subscriberID)
	{
		logger.info("RBT::inside removeCDR");
		int count = 0;
		String query;	
		Statement stmt = null;
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		
		logger.info("RBT::iquery "+query);
		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			count = stmt.getUpdateCount();
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return 0;
		}
		return count;
	}

	static boolean changetoPostpaid(Connection conn, String subscriberID)
	{
		logger.info("RBT::inside changetoPostpaid");
		String query;	
		Statement stmt = null;
		query = "UPDATE " + TABLE_NAME + " SET " + PREPAID_YES_COL + " = 'n' WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		
		logger.info("RBT::iquery "+query);
		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return false;
		}
		return true;
	}
	
	static boolean changetoPrepaid(Connection conn, String subscriberID)
	{
		logger.info("RBT::inside changetoPrepaid");
		String query;	
		Statement stmt = null;
		query = "UPDATE " + TABLE_NAME + " SET " + PREPAID_YES_COL + " = 'y' WHERE " + SUBSCRIBER_ID_COL + " ='" + subscriberID + "'";
		
		logger.info("RBT::iquery "+query);
		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return false;
		}
		return true;
	}

	static PreparedStatement prepareForCdrRemoval(Connection conn)
	{
         
		String query = null;
		PreparedStatement stmt = null;

		query = "DELETE  FROM " + TABLE_NAME + " WHERE " +
				 SUBSCRIBER_ID_COL + " = ? " +
				" AND " + SELECTION_TIME_COL  + " = ?";

		try
		{
			stmt = conn.prepareStatement(query);
		}
		catch(Exception se)
		{
			logger.error("", se);
			return null;
		}
		return stmt;
	}

	static boolean removeCdr(PreparedStatement stmt, String subscriberID, Date selectionTime)
	{        
		try
		{
			stmt.setString(1, subscriberID);
   		    stmt.setTimestamp(2, new Timestamp(selectionTime.getTime()));
			stmt.addBatch();
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return false;
		}
		return true;
	}

	static int batchCdrRemoval(PreparedStatement stmt)
	{
        logger.info("RBT::inside batchCdrRemoval");
		int [] results = null;
        
		try
		{
			results = stmt.executeBatch();
		}
		catch(Exception se)
		{
		    logger.error("", se);
			return 0;
		}
		return results.length;
	}
}