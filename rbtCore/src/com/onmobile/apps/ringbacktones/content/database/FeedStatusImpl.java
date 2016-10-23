package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.FeedStatus;

public class FeedStatusImpl extends RBTPrimitive implements FeedStatus
{
	private static Logger logger = Logger.getLogger(FeedStatusImpl.class);
	
    private static final String TABLE_NAME = "RBT_FEED_STATUS";
    private static final String FEED_TYPE_COL = "FEED_TYPE";
    private static final String FEED_STATUS_COL = "FEED_STATUS";
    private static final String FEED_FILE_COL = "FEED_FILE";
    private static final String FEED_SMS_KEYWORD_COL = "FEED_SMS_KEYWORD";
    private static final String FEED_SUB_KEYWORD_COL = "FEED_SUB_KEYWORD";
    private static final String FEED_ON_SUCCESS_SMS_COL = "FEED_ON_SUCCESS_SMS";
    private static final String FEED_ON_FAILURE_SMS_COL = "FEED_ON_FAILURE_SMS";
    private static final String FEED_OFF_SUCCESS_SMS_COL = "FEED_OFF_SUCCESS_SMS";
    private static final String FEED_OFF_FAILURE_SMS_COL = "FEED_OFF_FAILURE_SMS";
    private static final String FEED_FAILURE_SMS_COL = "FEED_FAILURE_SMS";
    private static final String FEED_NON_ACTIVE_SUB_SMS_COL = "FEED_NON_ACTIVE_SUB_SMS";

    private String m_type;
    private String m_status;
	private String m_file;
	private String m_smsKeyword;
	private String m_subKeyword;
	private String m_smsFeedOnSuccess;
	private String m_smsFeedOnFailure;
	private String m_smsFeedOffSuccess;
	private String m_smsFeedOffFailure;
	private String m_smsFeedFailure;
	private String m_smsFeedNonActiveSub;
	
	private FeedStatusImpl(String type, String status, String file, String smsKeyword, String subKeyword, String smsFeedOnSuccess, String smsFeedOnFailure, String smsFeedOffSuccess, String smsFeedOffFailure, String smsFeedFailure, String smsFeedNonActiveSub)
	{
		m_type = type;
		m_status = status;
		m_file = file;
		m_smsKeyword = smsKeyword;
		m_subKeyword = subKeyword;
		m_smsFeedOnSuccess = smsFeedOnSuccess;
		m_smsFeedOnFailure = smsFeedOnFailure;
		m_smsFeedOffSuccess = smsFeedOffSuccess;
		m_smsFeedOffFailure = smsFeedOffFailure;
		m_smsFeedFailure = smsFeedFailure;
		m_smsFeedNonActiveSub = smsFeedNonActiveSub;
	}
	
	public String type()
    {
        return m_type;
    }
	
	public String status()
    {
        return m_status;
    }

	public String file()
	{
		return m_file;
	}
	
	public String smsKeyword()
	{
	    return m_smsKeyword;
	}
	
	public String subKeyword()
	{
	    return m_subKeyword;
	}
	
	public String smsFeedOnSuccess()
	{
	    return m_smsFeedOnSuccess;
	}
	
	public String smsFeedOnFailure()
	{
	    return m_smsFeedOnFailure;
	}
	
	public String smsFeedOffSuccess()
	{
	    return m_smsFeedOffSuccess;
	}
	
	public String smsFeedOffFailure()
	{
	    return m_smsFeedOffFailure;
	}
	
	public String smsFeedFailure()
	{
	    return m_smsFeedFailure;
	}
	
	public String smsFeedNonActiveSub()
	{
	    return m_smsFeedNonActiveSub;
	}
	
	static void setStatus(Connection conn, String type, String status)
    {
        logger.info("RBT::inside setStatus");
        
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				FEED_STATUS_COL + " = " + sqlString(status) +
				" WHERE " + FEED_TYPE_COL  + " = " + sqlString(type);
		
		logger.info("RBT::query "+query);

		try
        {
            logger.info("RBT::inside try block");			
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
        }
        catch(SQLException se)
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
		return;
    }

	static void setFile(Connection conn, String type, String file)
    {
        logger.info("RBT::inside setFile");
        
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				FEED_FILE_COL + " = " + sqlString(file)  +
				" WHERE " + FEED_TYPE_COL  + " = " + sqlString(type);
		
		logger.info("RBT::query "+query);

		try
        {
            logger.info("RBT::inside try block");			
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
        }
        catch(SQLException se)
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
		return;
    }

	static FeedStatus insert(Connection conn, String type, String status, String file, String smsKeyword, String subKeyword, String smsFeedOnSuccess, String smsFeedOnFailure, String smsFeedOffSuccess, String smsFeedOffFailure, String smsFeedFailure, String smsFeedNonActiveSub)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		FeedStatusImpl feedStatus = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + FEED_TYPE_COL;
		query += ", " + FEED_STATUS_COL;
		query += ", " + FEED_FILE_COL;
		query += ", " + FEED_SMS_KEYWORD_COL;
		query += ", " + FEED_SUB_KEYWORD_COL;
		query += ", " + FEED_ON_SUCCESS_SMS_COL;
		query += ", " + FEED_ON_FAILURE_SMS_COL;
		query += ", " + FEED_OFF_SUCCESS_SMS_COL;
		query += ", " + FEED_OFF_FAILURE_SMS_COL;
		query += ", " + FEED_FAILURE_SMS_COL;
		query += ", " + FEED_NON_ACTIVE_SUB_SMS_COL;
		query += ")";

		query += " VALUES ( " + sqlString(type);
		query += ", " + sqlString(status);
		query += ", " + sqlString(file);
		query += ", " + sqlString(smsKeyword);
		query += ", " + sqlString(subKeyword);
		query += ", " + sqlString(smsFeedOnSuccess);
		query += ", " + sqlString(smsFeedOnFailure);
		query += ", " + sqlString(smsFeedOffSuccess);
		query += ", " + sqlString(smsFeedOffFailure);
		query += ", " + sqlString(smsFeedFailure);
		query += ", " + sqlString(smsFeedNonActiveSub);
		query += ")";
		
		logger.info("RBT::query " +query);
		
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
            logger.info("RBT::insertion to RBT_FEED_STATUS table successful");
            feedStatus = new FeedStatusImpl(type, status, file, smsKeyword, subKeyword, smsFeedOnSuccess, smsFeedOnFailure, smsFeedOffSuccess, smsFeedOffFailure, smsFeedFailure, smsFeedNonActiveSub);
            return feedStatus;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_FEED_STATUS table failed");
            return null;
        }
    }
		
    static boolean update(Connection conn, String type, String status, String file, String smsKeyword, String subKeyword, String smsFeedOnSuccess, String smsFeedOnFailure, String smsFeedOffSuccess, String smsFeedOffFailure, String smsFeedFailure, String smsFeedNonActiveSub)  
    {
        logger.info("RBT::inside update");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 FEED_STATUS_COL + " = " + sqlString(status) + ", " +
				 FEED_FILE_COL + " = " + sqlString(file) + ", " +
				 FEED_SMS_KEYWORD_COL + " = " + sqlString(smsKeyword) + ", " +
				 FEED_SUB_KEYWORD_COL + " = " + sqlString(subKeyword) + ", " +
				 FEED_ON_SUCCESS_SMS_COL + " = " + sqlString(smsFeedOnSuccess) + ", " +
				 FEED_ON_FAILURE_SMS_COL + " = " + sqlString(smsFeedOnFailure) + ", " +
				 FEED_OFF_SUCCESS_SMS_COL + " = " + sqlString(smsFeedOffSuccess) + ", " +
				 FEED_OFF_FAILURE_SMS_COL + " = " + sqlString(smsFeedOffFailure) + ", " +
				 FEED_FAILURE_SMS_COL + " = " + sqlString(smsFeedFailure) + ", " +
				 FEED_NON_ACTIVE_SUB_SMS_COL + " = " + sqlString(smsFeedNonActiveSub) + ", " +
				" WHERE " + FEED_TYPE_COL + " = " + sqlString(type);
		
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
	
	static FeedStatus getFeedStatus(Connection conn, String type)
    {
        logger.info("RBT::inside getFeedStatus");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String status = null;
		String file = null;
		String smsKeyword = null;
		String subKeyword = null;
		String smsFeedOnSuccess = null;
		String smsFeedOnFailure = null;
		String smsFeedOffSuccess = null;
		String smsFeedOffFailure = null;
		String smsFeedFailure = null;
		String smsFeedNonActiveSub = null;
		
		FeedStatusImpl feedStatus = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(type);
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						status = results.getString(FEED_STATUS_COL);
						file = results.getString(FEED_FILE_COL);
						smsKeyword = results.getString(FEED_SMS_KEYWORD_COL);
						subKeyword = results.getString(FEED_SUB_KEYWORD_COL);
						smsFeedOnSuccess = results.getString(FEED_ON_SUCCESS_SMS_COL);
						smsFeedOnFailure = results.getString(FEED_ON_FAILURE_SMS_COL);
						smsFeedOffSuccess = results.getString(FEED_OFF_SUCCESS_SMS_COL);
						smsFeedOffFailure = results.getString(FEED_OFF_FAILURE_SMS_COL);
						smsFeedFailure = results.getString(FEED_FAILURE_SMS_COL);
						smsFeedNonActiveSub = results.getString(FEED_NON_ACTIVE_SUB_SMS_COL);

						feedStatus = new FeedStatusImpl(type, status, file, smsKeyword, subKeyword, smsFeedOnSuccess, smsFeedOnFailure, smsFeedOffSuccess, smsFeedOffFailure, smsFeedFailure, smsFeedNonActiveSub);
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
        return feedStatus;
    }
	
	static boolean remove(Connection conn, String type)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + FEED_TYPE_COL + " = " + sqlString(type);
		
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