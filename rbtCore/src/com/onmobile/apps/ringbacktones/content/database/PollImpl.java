package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Poll;

public class PollImpl extends RBTPrimitive implements Poll
{
	private static Logger logger = Logger.getLogger(PollImpl.class);

	private static final String TABLE_NAME = "RBT_POLL";
	private static final String POLL_ID_COL = "POLL_ID";
	private static final String NO_OF_YES_INCIRCLE_COL = "NO_OF_YES_INCIRCLE";
	private static final String NO_OF_NO_INCIRCLE_COL = "NO_OF_NO_INCIRCLE";
	private static final String NO_OF_YES_OUTCIRCLE_COL = "NO_OF_YES_OUTCIRCLE";
	private static final String NO_OF_NO_OUTCIRCLE_COL = "NO_OF_NO_OUTCIRCLE";
	private static final String NO_OF_YES_OTHEROPERATOR_COL = "NO_OF_YES_OTHEROPERATOR";
	private static final String NO_OF_NO_OTHEROPERATOR_COL = "NO_OF_NO_OTHEROPERATOR";
	
	private String m_pollID;
	private int m_noOfYesIncircle;
	private int m_noOfNoIncircle;
	private int m_noOfYesOutcircle;
	private int m_noOfNoOutcircle;
	private int m_noOfYesOtherOperator;
	private int m_noOfNoOtherOperator;
	
	private PollImpl(String pollID, int noOfYesIncircle, int noOfNoIncircle, int noOfYesOutcircle, int noOfNoOutcircle, int noOfYesOtherOperator, int noOfNoOtherOperator)
	{
		m_pollID = pollID;
		m_noOfYesIncircle = noOfYesIncircle;
		m_noOfNoIncircle = noOfNoIncircle;
		m_noOfYesOutcircle = noOfYesOutcircle;
		m_noOfNoOutcircle = noOfNoOutcircle;
		m_noOfYesOtherOperator = noOfYesOtherOperator;
		m_noOfNoOtherOperator = noOfNoOtherOperator;
	}
	/* Returns Poll ID*/
 	public String pollID()
 	{
 		return m_pollID;
 	}

	
	/*Returns no of yes for inCircle*/
    public int noOfYes_Incircle()
    {
    	return m_noOfYesIncircle;
    }

	/*Increment no of yes for inCircle*/
    public int incrementNoOfYes_Incircle()
    {
    	return ++m_noOfYesIncircle;
    }
    
    /*Returns no of no for inCircle*/
    public int noOfNo_Incircle()
    {
    	return m_noOfNoIncircle;
    }

	/*Increment no of no for inCircle*/
    public int incrementNoOfNo_Incircle()
    {
    	return ++m_noOfNoIncircle;
    }
	
	
    /*Returns no of yes for outCircle*/
    public int noOfYes_Outcircle()
    {
    	return m_noOfYesOutcircle;
    }

	/*Increment no of yes for outCircle*/
    public int incrementNoOfYes_outcircle()
    {
    	return ++m_noOfYesOutcircle;
    }

    /*Returns no of no for outCircle*/
    public int noOfNo_Outcircle()
    {
    	return m_noOfNoOutcircle;
    }

	/*Increment no of no for outCircle*/
    public int incrementNoOfNo_outcircle()
    {
    	return ++m_noOfNoOutcircle;
    }

    /*Returns no of yes for OtherOperator*/
    public int noOfYes_OtherOperator()
    {
    	return m_noOfYesOtherOperator;
    }

	/*Increment no of yes for OtherOperator*/
    public int incrementNoOfYes_OtherOperator()
    {
    	return ++m_noOfYesOtherOperator;
    }

    /*Returns no of no for OtherOperator*/
    public int noOfNo_OtherOperator()
    {
    	return m_noOfNoOtherOperator;
    }

	/*Increment no of no for OtherOperator*/
    public int incrementNoOfNo_OtherOperator()
    {
    	return ++m_noOfNoOtherOperator;
    }
    public int totalYesCount()
    {
    	return m_noOfYesIncircle+m_noOfYesOutcircle+m_noOfYesOtherOperator;
    }

    public int totalNoCount()
    {
    	return m_noOfNoIncircle+m_noOfNoOutcircle+m_noOfNoOtherOperator;
    }
    /*To update record in the database*/
	
	static Poll insert(Connection conn, String pollID) 
	{
		logger.info("RBT::inside insert");

		int id = -1;
		String query = null;
		Statement stmt = null;

		PollImpl poll = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + POLL_ID_COL;
		query += ", " + NO_OF_YES_INCIRCLE_COL;
		query += ", " + NO_OF_NO_INCIRCLE_COL;
		query += ", " + NO_OF_YES_OUTCIRCLE_COL;
		query += ", " + NO_OF_NO_OUTCIRCLE_COL;
		query += ", " + NO_OF_YES_OTHEROPERATOR_COL;
		query += ", " + NO_OF_NO_OTHEROPERATOR_COL;
		query += ")";

		query += " VALUES ( '" + pollID + "',0,0,0,0,0,0)";
		
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
			logger.info("RBT::insertion to RBT_POLL table successful");
			poll = new PollImpl(pollID, 0,0,0,0,0,0);
			return poll;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_POLL table failed");
			return null;
		}
	}

	public static boolean update(Connection conn, Poll poll)   
	{
		logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
		NO_OF_YES_INCIRCLE_COL + " = "  + poll.noOfYes_Incircle() +  ", " +
		NO_OF_NO_INCIRCLE_COL + " = " + poll.noOfNo_Incircle() + ", " +
		NO_OF_YES_OUTCIRCLE_COL + " = " + poll.noOfYes_Outcircle() + ", " +
		NO_OF_NO_OUTCIRCLE_COL + " = " + poll.noOfNo_Outcircle() + ", " +
		NO_OF_YES_OTHEROPERATOR_COL + " = " + poll.noOfYes_OtherOperator() + ", " +
		NO_OF_NO_OTHEROPERATOR_COL + " = " + poll.noOfNo_OtherOperator() + 
		" WHERE " + POLL_ID_COL + " = '" + poll.pollID()+"'";
		
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

	

	public static Poll getPoll(Connection conn, String pollID)
	{
		logger.info("RBT::inside getPoll");

		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		int noOfYesIncircle = 0;
		int noOfNoIncircle = 0;
		int noOfYesOutcircle = 0;
		int noOfNoOutcircle = 0;
		int noOfYesOtherOperator = 0;
		int noOfNoOtherOperator = 0;
		
		PollImpl poll = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + POLL_ID_COL + " = '" + pollID + "'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			if (results.next())
			{
				noOfYesIncircle = results.getInt(NO_OF_YES_INCIRCLE_COL);
				noOfNoIncircle = results.getInt(NO_OF_NO_INCIRCLE_COL);
				noOfYesOutcircle = results.getInt(NO_OF_YES_OUTCIRCLE_COL);
				noOfNoOutcircle = results.getInt(NO_OF_NO_OUTCIRCLE_COL);
				noOfYesOtherOperator = results.getInt(NO_OF_YES_OTHEROPERATOR_COL);
				noOfNoOtherOperator = results.getInt(NO_OF_NO_OTHEROPERATOR_COL);
				
				poll = new PollImpl(pollID, noOfYesIncircle, noOfNoIncircle, noOfYesOutcircle, noOfNoOutcircle, noOfYesOtherOperator, noOfNoOtherOperator);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
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
		return poll;
	}

	}
