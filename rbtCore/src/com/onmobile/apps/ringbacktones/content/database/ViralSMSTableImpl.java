/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: balachandar.p $
 * $Id: ViralSMSTableImpl.java,v 1.60 2015/04/20 06:16:54 balachandar.p Exp $
 * $Revision: 1.60 $
 * $Date: 2015/04/20 06:16:54 $
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class ViralSMSTableImpl extends RBTPrimitive implements ViralSMSTable
{
	private static Logger logger = Logger.getLogger(ViralSMSTableImpl.class);

    public static final String TABLE_NAME = "RBT_VIRAL_SMS_TABLE";
    private static final String SMS_ID_COL = "SMS_ID";
    private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
	private static final String SMS_SENT_TIME_COL = "SMS_SENT_TIME";
	private static final String SMS_TYPE_COL = "SMS_TYPE";
	private static final String CALLER_ID_COL = "CALLER_ID";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String SEARCH_COUNT_COL = "SEARCH_COUNT";
	private static final String SELECTED_BY_COL = "SELECTED_BY";
	private static final String SET_TIME_COL = "SET_TIME";
	private static final String EXTRA_INFO_COL = "EXTRA_INFO";
	private static final String NEXT_RETRY_TIME_COL = "NEXT_RETRY_TIME" ;

    private long smsId = -1;
    private String circleId = null;

    public long getSmsId()
    {
        return smsId;
    }

    public void setSmsId(long id)
    {
        this.smsId = id;
    }

    public String getCircleId()
    {
        return circleId;
    }

    public void setCircleId(String circleId)
    {
        this.circleId = circleId;
    }

    public int getErrorNo()
    {
        return errorNo;
    }

    public void setErrorNo(int errorNo)
    {
        this.errorNo = errorNo;
    }

    public String getErrrorRef()
    {
        return errrorRef;
    }

    public void setErrrorRef(String errrorRef)
    {
        this.errrorRef = errrorRef;
    }
    
	
    private int errorNo = -1;
    private String errrorRef = null;
	private String m_subscriberID;
	private Date m_smsTime;
	private String m_type;
	private String m_callerID;
	private String m_clipID;
	private int m_count;
	private String m_selectedBy;
	private Date m_setTime;
	private String m_extraInfo;
	private static String m_databaseType=getDBSelectionString();
	private String callerCircleId = null; 
	private boolean isTaken = false;
	private SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Date startDate = Calendar.getInstance().getTime();
	private Subscriber subscriber = null;
	private Date m_nextRetryTime;
	
	public ViralSMSTableImpl(String subscriberID, Date smsTime, String type,
			String callerID, String clipID, int count, String selectedBy,
			Date setTime, String extraInfo)
	{
		m_subscriberID = subscriberID;
		m_smsTime = smsTime;
		m_type = type;
		m_callerID = callerID;
		m_clipID = clipID;
		m_count = count;
		m_selectedBy = selectedBy;
		m_setTime = setTime;
		m_extraInfo = extraInfo; 
	}
	
	public ViralSMSTableImpl(String subscriberID, Date smsTime, String type,
			String callerID, String clipID, int count, String selectedBy,
			Date setTime, String extraInfo, int smsId)
	{
		m_subscriberID = subscriberID;
		m_smsTime = smsTime;
		m_type = type;
		m_callerID = callerID;
		m_clipID = clipID;
		m_count = count;
		m_selectedBy = selectedBy;
		m_setTime = setTime;
		m_extraInfo = extraInfo; 
		this.smsId = smsId; 
		 
	}
	
	public ViralSMSTableImpl(String subscriberID, Date smsTime, String type,
			String callerID, String clipID, int count, String selectedBy,
			Date setTime, String extraInfo ,int smsId, Date nextRetryTime )
	{
		m_subscriberID = subscriberID;
		m_smsTime = smsTime;
		m_type = type;
		m_callerID = callerID;
		m_clipID = clipID;
		m_count = count;
		m_selectedBy = selectedBy;
		m_setTime = setTime;
		m_extraInfo = extraInfo;
		this.smsId = smsId; 
		m_nextRetryTime = nextRetryTime;
		
	}
	
	public ViralSMSTableImpl(String subscriberID, Date smsTime, String type,
			String callerID, String clipID, int count, String selectedBy,
			Date setTime, String extraInfo , Date nextRetryTime )
	{
		m_subscriberID = subscriberID;
		m_smsTime = smsTime;
		m_type = type;
		m_callerID = callerID;
		m_clipID = clipID;
		m_count = count;
		m_selectedBy = selectedBy;
		m_setTime = setTime;
		m_extraInfo = extraInfo;
		m_nextRetryTime = nextRetryTime;
		
	}

	public String subID()
	{
		return m_subscriberID;
	}

	public Date sentTime()
	{
		return m_smsTime;
	}

	public String type()
	{
		return m_type;
	}

	public String callerID()
	{
		return m_callerID;
	}

	public String clipID()
	{
		return m_clipID;
	}

	public int count()
	{
		return m_count;
	}
	
	public void setCount(int count)
	{
		m_count = count;
	}
	public String selectedBy()
	{
		return m_selectedBy;
	}

	public Date setTime()
	{
		return m_setTime;
	}
	
	public String extraInfo()
	{
		return m_extraInfo;
	}

	public String getCallerCircleId()
	{
		return callerCircleId;
	}
	 
	public void setCallerCircleId(String circleId)
	{
		callerCircleId = circleId;
	}
	public boolean isTaken()
	{
		return isTaken;
	}
	 
	public void setTaken(boolean taken)
	{
		isTaken = taken;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
	public void setStartTime(Date date)
	{
		startDate = date;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.ViralSMSTable#getSubscriber()
	 */
	public Subscriber getSubscriber()
	{
		return subscriber;		
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.content.ViralSMSTable#setSubscriber(com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber)
	 */
	public void setSubscriber(Subscriber subscriber)
	{
		this.subscriber = subscriber;
	}
	 
	public Date retryTime() {
		return m_nextRetryTime;
	}

	public void setRetryTime(Date nextRetryTime)
	{
		m_nextRetryTime = nextRetryTime;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		append(sb, this.subID());
		appendDate(sb, this.sentTime());
		append(sb, this.type());
		append(sb, this.callerID());
		append(sb, this.clipID());
		append(sb, this.count()+"");
		append(sb, this.selectedBy());
		appendDate(sb, this.setTime());
		appendLast(sb, this.extraInfo());
		return sb.toString();
	}
	private void append(StringBuffer sb, String s)
	{
		if(s == null || s.trim().equals(""))
			s = "null";
		sb.append(s);
		sb.append("|");
	}
	private void appendDate(StringBuffer sb, Date d)
	{
		if(d == null)
			sb.append("null");
		else
			sb.append(sdFormat.format(d));
		sb.append("|");
	}
	private void appendLast(StringBuffer sb, String s)
	{
		if(s == null || s.trim().equals(""))
			s = "null";
		sb.append(s);
	}
	 
	static ViralSMSTableImpl insert(Connection conn, String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, String extraInfo)
    {
        return insert(conn, subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo, null);
    }

	static ViralSMSTableImpl insert(Connection conn, String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, String extraInfo,
			String circleId)
	{
		int id = -1;
		Statement stmt = null;

		if(subscriberID != null && subscriberID.trim().length() == 0)
			subscriberID = null;
		String sendDate = "SYSDATE";
		String setTimeStr = sqlTime(setTime);
		if(sentTime != null)
			sendDate = sqlTime(sentTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sendDate = "SYSDATE()";
			if(sentTime != null)
				sendDate = mySQLDateTime(sentTime);
			setTimeStr = mySQLDateTime(setTime);
		}
		ViralSMSTableImpl viralSMS = null;

		String query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
		query += ", " + SMS_SENT_TIME_COL;
		query += ", " + SMS_TYPE_COL;
		query += ", " + CALLER_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + SEARCH_COUNT_COL;
		query += ", " + SELECTED_BY_COL;
		query += ", " + SET_TIME_COL;
		query += ", " + EXTRA_INFO_COL;
        query += ", " + CIRCLE_ID_COL;
		query += ")";

		query += " VALUES ( " + sqlString(subscriberID);
		query += ", " + sendDate;
		query += ", " + "'" + type + "'";
		query += ", " + sqlString(callerID);
		query += ", " + sqlString(clipID);
		query += ", " + count;
		query += ", " + sqlString(selectedBy);
		query += ", " + setTimeStr;
		query += ", " + sqlString(extraInfo);
        query += ", " + sqlString(circleId);
		query += ")";

		logger.info("Executing query " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		if(id == 0)
		{
			logger.info("RBT::insertion to RBT_VIRAL_SMS_TABLE table successful");
			viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime,extraInfo);
			return viralSMS;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_VIRAL_SMS_TABLE table failed");
			return null;
		}
	}

	static boolean update(Connection conn, String subscriberID, Date sentTime, String type, String callerID, String clipID, int count, String selectedBy, Date setTime, String extraInfo)  
	{
		int n = -1;
		Statement stmt = null;

		String sendDate = "SYSDATE";
		String setTimeStr = sqlTime(setTime);
		if(sentTime != null)
			sendDate = sqlTime(sentTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sendDate = "SYSDATE()";
			if(sentTime != null)
				sendDate = mySQLDateTime(sentTime);
			setTimeStr = mySQLDateTime(setTime);
		}

		String query = "UPDATE " + TABLE_NAME + " SET " +
		SMS_SENT_TIME_COL + " = " + sendDate + ", " +
		CALLER_ID_COL + " = " + sqlString(callerID) + ", " +
		CLIP_ID_COL + " = " + sqlString(clipID) + ", " +
		SEARCH_COUNT_COL + " = " + count + ", " +
		SELECTED_BY_COL + " = " + sqlString(selectedBy) + ", " +
		SET_TIME_COL + " = " + setTimeStr + ", " +
		EXTRA_INFO_COL + " = " + sqlString(extraInfo)  +
		" WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(type);

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n==1);
	}

	static ViralSMSTable getViralSMS(Connection conn, long smsID)
	{
		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_ID_COL + " = " + smsID;
		
		Statement stmt = null;
		ResultSet results = null;
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				
				ViralSMSTable viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);
				return viralSMS;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		return null;
	}

	static ViralSMSTable [] getViralSMS(Connection conn, String subID)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "'";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL); 	
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable getViralSMSByTypeOrderedByTimeDesc(Connection conn, String subID, String smsType)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " ORDER BY "+ SMS_SENT_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL); 
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return viralSMS;
	}
	
	static boolean updateSubscriberId(Connection conn, String newSubscriberId, String subscriberId)
	{
		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " +
		CALLER_ID_COL + " = '" + newSubscriberId +
		"' WHERE " + CALLER_ID_COL  + " = '" + subscriberId + "' AND "+ 
		SMS_TYPE_COL + " IN ('GIFT','GIFTED','GIFT_CHARGED','ACCEPT_ACK','REJECT_ACK','ACCEPT_PRE','COPY','COPYCONFIRM','COPYCONFIRMED','COPYCONFPENDING','COPYSTAR')";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n>0);
	}

	static ViralSMSTable[] getViralSMSByTypeForCaller(Connection conn, String callerID, String smsType) {
		Statement stmt = null;
		ResultSet results = null;

		ArrayList<ViralSMSTable> viralSMS = new ArrayList<ViralSMSTable>();

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + callerID
		+ "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType);

		logger.info("Executing query: " + query);
		try {
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
            while (results.next())
            {
                viralSMS.add(getNextViralSMS(results));
			}
		}
		catch (SQLException se) {
			logger.error("", se);
		}
		finally {
			closeStatementAndRS(stmt, results);
		}
		if(viralSMS.size() > 0) {
			logger.info("RBT::retrieving records from " + TABLE_NAME + " successful");
			return (ViralSMSTable[])viralSMS.toArray(new ViralSMSTable[0]);
		}
		else {
			logger.info("RBT::no records to retrieve " + TABLE_NAME);
			return null;
		}
	}

    public static ViralSMSTableImpl getNextViralSMS(ResultSet results) throws SQLException
    {
        String subscriberID = results.getString(SUBSCRIBER_ID_COL);
        Timestamp sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
        String type = results.getString(SMS_TYPE_COL);
        String clipID = results.getString(CLIP_ID_COL);
        int count = results.getInt(SEARCH_COUNT_COL);
        String selectedBy = results.getString(SELECTED_BY_COL);
        Timestamp setTime = results.getTimestamp(SET_TIME_COL);
        String extraInfo = results.getString(EXTRA_INFO_COL);
        String callerID = results.getString(CALLER_ID_COL);
        ViralSMSTableImpl result = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
        result.setSmsId(results.getLong(SMS_ID_COL));
        result.setCircleId(results.getString(CIRCLE_ID_COL));
        return result;
    }
	/*
	 * added by sandeep
	 */
	static ViralSMSTable [] getViralSMSesByTypesForSubscriber(Connection conn, String subID, String[] smsTypes)
	{
		if (smsTypes == null || smsTypes.length == 0)
			return null;
		
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		int smsId;
		
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		String types = "";
		for (int i = 0; i < smsTypes.length; i++)
		{
			types += sqlString(smsTypes[i]) +",";
		}
		types = types.substring(0, types.length() -1);
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subID) + " AND " + SMS_TYPE_COL + " IN ("
				+ types + ") ORDER BY " + SMS_SENT_TIME_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				smsId = results.getInt(SMS_ID_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo, smsId);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable [] getViralSMSesByTypesForSubscriber(Connection conn, String subID, String[] smsTypes, String mode)
	{
		if (smsTypes == null || smsTypes.length == 0)
			return null;
		
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		int smsId;
		
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		String types = "";
		for (int i = 0; i < smsTypes.length; i++)
		{
			types += sqlString(smsTypes[i]) +",";
		}
		types = types.substring(0, types.length() -1);
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
				+ " = " + sqlString(subID) + " AND " + SMS_TYPE_COL + " IN ("
				+ types + ")  AND " + SELECTED_BY_COL + " = " + sqlString(mode) + " ORDER BY " + SMS_SENT_TIME_COL + " DESC";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				smsId = results.getInt(SMS_ID_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo, smsId);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	
	static ViralSMSTable[] getViralSMSByCaller(Connection conn, String callerId){
		
		Statement stmt = null;
		ResultSet results = null;
		
		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>();
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + "= '" + callerId + "'";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	/*
	 * 
	 */
	static ViralSMSTable getViralSMSByType(Connection conn, String subID, String smsType)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType);

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return viralSMS;
	}

	static ViralSMSTable getViralPromotion(Connection conn, String subID, Date date)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		String sentDate = "SYSDATE()";
		if(date != null)
			sentDate = mySQLDateTime(date);
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			sentDate = "SYSDATE";
			if(date != null)
				sentDate = sqlTime(date);
		}
		ViralSMSTableImpl viralSMS = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD') = TO_CHAR( " + sentDate + " , 'YYYY/MM/DD') AND UPPER(" + SMS_TYPE_COL + ") IN ('BASIC', 'CRICKET')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subID + "' AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d') = DATE_FORMAT( " + sentDate + " , '%Y %m %d') AND UPPER(" + SMS_TYPE_COL + ") IN ('BASIC', 'CRICKET')";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return viralSMS;
	}

	static int removeOldViralSMS(Connection conn, String type, float duration, boolean isDurationInHours)
	{
		int count = 0;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = " + sqlString(type) + " AND " + SMS_SENT_TIME_COL + " <= ( now() -" + duration + ")";
		else if(isDurationInHours)
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = " + sqlString(type) + " AND " + SMS_SENT_TIME_COL + " <= TIMESTAMPADD(HOUR, -" + duration + ",SYSDATE())";
		else 
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = " + sqlString(type) + " AND " + SMS_SENT_TIME_COL + " <= TIMESTAMPADD(DAY, -" + duration + ",SYSDATE())";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			count = stmt.executeUpdate(query);
			//count = stmt.getUpdateCount();
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return -1;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return count;
	}

	static ViralSMSTable[] getGiftInboxToBeCleared(Connection conn, float duration, String smsType)
	{	
		String query = null;
		Statement stmt = null;
		ResultSet results = null;
		
		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>();
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + " <= ( now() -" + duration + ")";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + " <= TIMESTAMPADD(DAY, -" + duration + ",SYSDATE())";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static boolean remove(Connection conn, String subscriberID, String type)
	{
		return remove(conn,subscriberID,type,null);
		
	}
	
	static boolean remove(Connection conn, String subscriberID, String type , Date sentTime)
	{
		int n = -1;
		Statement stmt = null;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(type);
		if(sentTime!=null)
		{
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				query = query + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		}

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
	}

	static boolean removeViralSMSOfCaller(Connection conn, String subscriberID, String type)
	{
		int n = -1;
		Statement stmt = null;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(type);

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
	}
	
	static boolean removeCopyPendingViralSMSOfCaller(Connection conn, String subscriberID,String smsType, int time)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = '"+smsType+"' AND " + SMS_SENT_TIME_COL + "> SYSDATE - " + time + "/1440 AND (" 
					+ SELECTED_BY_COL + " IS NULL OR " + SELECTED_BY_COL + " LIKE '%_XCOPY%')";

		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = '"+smsType+"' AND " + SMS_SENT_TIME_COL + "> TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE()) AND (" 
					+ SELECTED_BY_COL + " IS NULL OR " + SELECTED_BY_COL + " LIKE '%_XCOPY%')";
			
		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return(n>0);
	}
	
	static void setSearchCount(Connection conn, String subscriberID, String smsType, int count)
	{
		Statement stmt = null;

		String query = "UPDATE " + TABLE_NAME + " SET " +
		SEARCH_COUNT_COL + " = " + count + 
		" WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType);

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return;
	}

	static void setSearchCountCopy(Connection conn, String subscriberID, String smsType, int count, Date sent, String caller)
	{	
		String query = null;
		Statement stmt = null;
		String retryTimeQuery = " ";
		String retryTimePeriod = Utility.getParamAsString("RETRY_TIME_PERIOD");
		if (retryTimePeriod != null) {
			if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
				retryTimeQuery = " , " + NEXT_RETRY_TIME_COL + "= SYSDATE + "
						+ "(" + retryTimePeriod + "/1440) ";
			else
				retryTimeQuery = " , " + NEXT_RETRY_TIME_COL
						+ "= TIMESTAMPADD(MINUTE,+" + retryTimePeriod
						+ ",SYSDATE()) ";
		}

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "UPDATE " + TABLE_NAME + " SET " +
				SEARCH_COUNT_COL + " = " + count + retryTimeQuery  +
				" WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  " = " + sqlString(caller)  + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sent) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "UPDATE " + TABLE_NAME + " SET " +
			SEARCH_COUNT_COL + " = " + count + retryTimeQuery + 
			" WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  " = " + sqlString(caller)  + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sent) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return;
	}

	static boolean updateViralPromotion1(Connection conn, String subscriberID,
			String callerID, String newCallerID, Date sentTime, String oldType,
			String newType, Date setTime, String selectedBy, String extraInfo,
			String clipID)
	{
		int n = -1;
		Statement stmt = null;
		String setTimeStr = sqlTime(setTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeStr = mySQLDateTime(setTime);

		String query = "UPDATE " + TABLE_NAME + " SET " +
				SMS_TYPE_COL + " = " + sqlString(newType) + ", "+
				SET_TIME_COL + " = " + setTimeStr+","+CLIP_ID_COL +" = "+ sqlString(clipID);
		if (newCallerID != null)
			query = query + ", "+CALLER_ID_COL +" = "+sqlString(newCallerID); 
		if(selectedBy != null)
			query = query + 	", " + SELECTED_BY_COL + " = " + sqlString(selectedBy); 
		if(extraInfo != null)
			query = query + 	", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo); 
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	
	static boolean updateViralSearchCount(Connection conn, String subscriberID, String callerID, String type, Date sentTime, int count,String clipID)
	{
		int n = -1;
		Statement stmt = null;

		String whereClause = "";

		if (subscriberID != null)
			whereClause += " "+ SUBSCRIBER_ID_COL +" = "+ sqlString(subscriberID);

		if (callerID != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += CALLER_ID_COL +" = "+ sqlString(callerID);
		}

		if (type != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += SMS_TYPE_COL +" = "+ sqlString(type);
		}

		if (sentTime != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";

			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				whereClause += "TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				whereClause += "DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";
		}

		if (whereClause.equals(""))
			return false;

		String query = "UPDATE " + TABLE_NAME + " SET " + SEARCH_COUNT_COL + " = " + count ;
        
		if(clipID!=null && !clipID.equalsIgnoreCase("")){		
        	query += " , CLIP_ID = "+sqlString(clipID);
		}
        query += " WHERE " + whereClause;

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	static ViralSMSTable [] getViralSMSByType(Connection conn, String smsType)
	{
		Statement stmt = null;
		ResultSet results = null;
		String query = null;
		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		long smsId = 0L;
		ViralSMSTableImpl viralSMS = null;
		Date nextRetryTime = null ;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "'" + " AND " + NEXT_RETRY_TIME_COL + "<=SYSDATE ";
		else 
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "'" + " AND " + NEXT_RETRY_TIME_COL + "<=SYSDATE() ";
		

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				smsId = Long.parseLong(results.getString(SMS_ID_COL));
				nextRetryTime =  results.getDate(NEXT_RETRY_TIME_COL) ;
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo,nextRetryTime);
				viralSMS.setSmsId(smsId);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable [] getViralSMSByTypeAndLimit(Connection conn, String smsType, int max)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		int num = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' ORDER BY "+ SMS_SENT_TIME_COL;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (num < max && results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				long smsID = results.getLong(SMS_ID_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);

				viralSMSList.add(viralSMS);
				num++;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable [] getViralSMSByTypeAndLimitAndTime(Connection conn, String smsType, int time, int max)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		int num = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		String circleID = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>();
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "< SYSDATE - " + time + "/1440 ORDER BY " + SMS_SENT_TIME_COL;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "< TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE())  ORDER BY "+ SMS_SENT_TIME_COL;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (num < max && results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				circleID = results.getString(CIRCLE_ID_COL);
				long smsID = results.getLong(SMS_ID_COL);

				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setCircleId(circleID);
				viralSMS.setSmsId(smsID);
				viralSMSList.add(viralSMS);
				num++;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static ViralSMSTable [] getViralSMSByTypeAndCircle(Connection conn, String smsType, int time, String circleId)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		String circleID = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>();
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + CIRCLE_ID_COL + " = '" + circleId + "' AND ROWNUM < "+count;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + CIRCLE_ID_COL + " = '" + circleId + "' LIMIT "+count;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				circleID = results.getString(CIRCLE_ID_COL);
				long smsID = results.getLong(SMS_ID_COL);

				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setCircleId(circleID);
				viralSMS.setSmsId(smsID);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static ViralSMSTable [] getViralSMSByTypeAndTime(Connection conn, String smsType, int time)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		int smsID = -1;
		ViralSMSTableImpl viralSMS = null;
		Date nextRetryTime = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>();
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " + NEXT_RETRY_TIME_COL + " <=SYSDATE "  + " AND " + SMS_SENT_TIME_COL + "< SYSDATE - " + time + "/1440 ORDER BY " + SMS_SENT_TIME_COL;
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = '" + smsType + "' AND " +  NEXT_RETRY_TIME_COL+" <=SYSDATE() "  + " AND " + SMS_SENT_TIME_COL + "< TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE())  ORDER BY "+ SMS_SENT_TIME_COL;
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				smsID = results.getInt(SMS_ID_COL);
				nextRetryTime = results.getDate(NEXT_RETRY_TIME_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo,smsID,nextRetryTime);
				viralSMSList.add(viralSMS);
				
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	static ViralSMSTable getLatestViralSMSByTypeSubscriberAndTime(Connection conn, String callerID, String smsType, int time)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> SYSDATE - " + time + "/1440 ORDER BY " + SMS_SENT_TIME_COL + " DESC";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE())  ORDER BY "+ SMS_SENT_TIME_COL + " DESC";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				long smsID = results.getLong(SMS_ID_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMS != null)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return viralSMS;
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static ViralSMSTable[] getLatestViralSMSesByTypeSubscriberAndTime(Connection conn, String callerID, String smsType, int time)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> SYSDATE - " + time + "/1440 ORDER BY " + SMS_SENT_TIME_COL + " DESC";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE())  ORDER BY "+ SMS_SENT_TIME_COL + " DESC";
		
		logger.info("Executing query: " + query);
		List<ViralSMSTableImpl> viralSMSList = new ArrayList<ViralSMSTableImpl>(); 
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				long smsID = results.getLong(SMS_ID_COL);
				
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable getLatestViralSMSByTypeAndTime(Connection conn, String callerID, String smsType, int time)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> SYSDATE - " + time + "/1440 ORDER BY " + SMS_SENT_TIME_COL + " DESC";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + callerID + "' AND "+ SMS_TYPE_COL + " = '" + smsType + "' AND " + SMS_SENT_TIME_COL + "> TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE())  ORDER BY "+ SMS_SENT_TIME_COL + " DESC";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				long smsID = results.getLong(SMS_ID_COL);

				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMS != null)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return viralSMS;
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static ViralSMSTable [] getViralSMSesByType(Connection conn, String callID, String smsType)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = '" + callID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " ORDER BY " + SMS_SENT_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	
	static ViralSMSTable [] getViralSMSesByTypes(Connection conn, String callID, String[] smsTypes)
	{
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTable> viralSMSList = new ArrayList<ViralSMSTable>(); 

		
		String types = "";
		for (int i = 0; i < smsTypes.length; i++)
		{
			types += sqlString(smsTypes[i]) +",";
		}
		types = types.substring(0, types.length() -1);
		
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CALLER_ID_COL + " = " + sqlString(callID) + " AND " + SMS_TYPE_COL + " IN ( " + types + "  )ORDER BY " + SMS_SENT_TIME_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return (ViralSMSTable[])viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}

	static ViralSMSTable[] getViralSMSes(Connection conn, String subscriberID,
			String callerID, String type, String clipID, Date sentTime)
	{
		Statement stmt = null;
		ResultSet results = null;

		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;
		List<ViralSMSTableImpl> viralSMSList = new ArrayList<ViralSMSTableImpl>(); 

		String whereClause = "";

		if (subscriberID != null)
			whereClause += " "+ SUBSCRIBER_ID_COL +" = "+ sqlString(subscriberID);

		if (callerID != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += CALLER_ID_COL +" = "+ sqlString(callerID);
		}

		if (type != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += SMS_TYPE_COL +" = "+ sqlString(type);
		}

		if (clipID != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += CLIP_ID_COL +" = "+ sqlString(clipID);
		}

		if (sentTime != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";

			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				whereClause += "TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				whereClause += "DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";
		}
		
		String query = "SELECT * FROM " + TABLE_NAME;
		if (!whereClause.equals(""))
			query += " WHERE " + whereClause;
		query += " ORDER BY "+ SMS_ID_COL;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				long smsID = results.getLong(SMS_ID_COL);

				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				viralSMS.setSmsId(smsID);
				viralSMSList.add(viralSMS);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		if(viralSMSList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_VIRAL_SMS_TABLE successful");
			return viralSMSList.toArray(new ViralSMSTable[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_VIRAL_SMS_TABLE");
			return null;
		}
	}
	// RBT-14301: Uninor MNP changes.
	static boolean updateGiftCharge(Connection conn, String subscriberID, String callerID, String clipID, String setTime, String status,String selectedBy, String extraInfo,String circleId)
	{
		int n = -1;

		Statement stmt = null;
		String stat = "GIFT_CHARGED";
		if(!status.equalsIgnoreCase("SUCCESS"))
			stat = "GIFTFAILED";

		String query = "UPDATE " + TABLE_NAME + " SET " +
							SMS_TYPE_COL + " = " + sqlString(stat)+", "+
							EXTRA_INFO_COL + " = "+sqlString(extraInfo);
		if (circleId != null) {
			query += ", " + CIRCLE_ID_COL + " = " + sqlString(circleId);
		}
		if(stat.equals("GIFT_CHARGED") && selectedBy != null)
			query +=  "," + SELECTED_BY_COL + " = CONCAT(" + SELECTED_BY_COL + "," +sqlString(selectedBy) + ") "; 
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query += " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = 'GIFTCHRGPENDING' AND "  + CALLER_ID_COL +  " = " + sqlString(callerID) + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYYMMDDHH24MISS') = '"+setTime+"'";
		else
			query += " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = 'GIFTCHRGPENDING' AND "  + CALLER_ID_COL +  " = " + sqlString(callerID) + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y%m%d%H%i%s') = '"+setTime+"'";


		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	static boolean updateViralPromotion(Connection conn, String subscriberID, String callerID, String newCallerID, Date sentTime, String oldType, String newType, Date setTime, String selectedBy, String extraInfo, boolean updateSmsId)
	{
		int n = -1;
		Statement stmt = null;
		String setTimeStr = sqlTime(setTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeStr = mySQLDateTime(setTime);

		String query = "UPDATE " + TABLE_NAME + " SET " +
				SMS_TYPE_COL + " = " + sqlString(newType) + ", "+
				SET_TIME_COL + " = " + setTimeStr;
		if (newCallerID != null)
			query = query + ", "+CALLER_ID_COL +" = "+sqlString(newCallerID); 
		if(selectedBy != null)
			query = query + 	", " + SELECTED_BY_COL + " = " + sqlString(selectedBy); 
		if(extraInfo != null)
			query = query + 	", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo); 
		if(updateSmsId)
			query = query + 	", " + SMS_ID_COL + " = LAST_INSERT_ID()+1"; 
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}
	
	static boolean updateViralPromotion(Connection conn, String subscriberID,
			String callerID, String newCallerID, Date sentTime, String oldType,
			String newType, Date setTime, String selectedBy, String extraInfo,
			String clipID)
	{
		int n = -1;
		Statement stmt = null;
		String setTimeStr = sqlTime(setTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeStr = mySQLDateTime(setTime);

		String query = "UPDATE " + TABLE_NAME + " SET " +
				SMS_TYPE_COL + " = " + sqlString(newType) + ", "+
				SET_TIME_COL + " = " + setTimeStr;
		if (newCallerID != null)
			query = query + ", "+CALLER_ID_COL +" = "+sqlString(newCallerID); 
		if(selectedBy != null)
			query = query + 	", " + SELECTED_BY_COL + " = " + sqlString(selectedBy); 
		if(extraInfo != null)
			query = query + 	", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo); 
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		if (clipID == null)
			query += " AND " + CLIP_ID_COL + " IS NULL";
		else
			query += " AND " + CLIP_ID_COL + " = '" + clipID + "'";
		
		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}

	static boolean updateLatestViralSMSTypeOfCaller(Connection conn, String subscriberID, String oldType, String newType, int time)
	{
		return updateLatestViralSMSTypeOfCaller(conn,subscriberID,oldType,newType,time,null);
	}
	
	static boolean updateLatestViralSMSTypeOfCaller(Connection conn, String subscriberID, String oldType, String newType, int time , HashMap<String,String> extraInfoMap)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;
		String selected = "";
		Date sentTime = null;
		String extraInfo = null;
		String tempQuery = null;
		ResultSet results = null;
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			tempQuery = "SELECT * FROM "+TABLE_NAME+" WHERE "+CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) +" AND "+SMS_SENT_TIME_COL + " > SYSDATE - " + time + "/1440 ORDER BY "+SMS_SENT_TIME_COL+" DESC ";
		else
			tempQuery = "SELECT * FROM "+TABLE_NAME+" WHERE "+CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) +" AND "+SMS_SENT_TIME_COL + " > TIMESTAMPADD(MINUTE,-" + time + ",SYSDATE()) ORDER BY "+SMS_SENT_TIME_COL+" DESC ";
		logger.info("RBT::tempquery "+tempQuery);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(tempQuery);
			if (results.next())
			{
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		
		if (sentTime == null)
			return false;
		
		query = "UPDATE " + TABLE_NAME + " SET " +
				SMS_TYPE_COL + " = " + sqlString(newType);
		if(extraInfoMap!=null && extraInfoMap.size() > 0)
			query = query+" , "+ EXTRA_INFO_COL + " = "+ sqlString(getFinalExtraInfo(extraInfo, extraInfoMap)); 
				if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
					query = query + " WHERE " + CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
				else
					query = query + " WHERE " + CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		logger.info("RBT::query "+query);

		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		
		if(n > 0)
		{
			query = "UPDATE " + TABLE_NAME + " SET " +
			SMS_TYPE_COL + " = " + sqlString("COPYEXPIRED");
			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				query = query + " WHERE " + CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString("COPYCONFPENDING") + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') < TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				query = query + " WHERE " + CALLER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString("COPYCONFPENDING") + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') < DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";
			
			logger.info("query >"+query);
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				int count = stmt.getUpdateCount();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			finally
			{
				closeStatementAndRS(stmt, null);
			}
		}
		return (n > 0);
	}

	static void updateCopyViralPromotion(Connection conn, String subscriberID, String callerID, Date sentTime, String newType, Date setTime, String selectedBy, String extraInfo)
	{
		Statement stmt = null;
		String setTimeStr = sqlTime(setTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
			setTimeStr = mySQLDateTime(setTime);

		String query = "UPDATE " + TABLE_NAME + " SET " +
		SMS_TYPE_COL + " = " + sqlString(newType) + ", " + 
		SET_TIME_COL + " = " + setTimeStr;
		if(selectedBy != null)
			query = query + 	", " + SELECTED_BY_COL + " = " + sqlString(selectedBy); 
		if(extraInfo != null)
			query = query + 	", " + EXTRA_INFO_COL + " = " + sqlString(extraInfo); 
		
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " IN ('COPY','COPYCONFIRM','COPYCONFIRMED') AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " IN ('COPY','COPYCONFIRM','COPYCONFIRMED') AND "  + CALLER_ID_COL + getNullForWhere(callerID) +" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return;
	}
	static ViralSMSTable getViralPromotion(Connection conn, String subscriber, String caller, Date sent, String smsType)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriber + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)  + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sent) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriber + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)  + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sent) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);	
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return viralSMS;
	}

	static ViralSMSTable getViralPromotion(Connection conn, String subscriber, String caller, Date sent, String smsType, String clipID)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String subscriberID = null;
		Date sentTime = null;
		String type = null;
		String callerID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		ViralSMSTableImpl viralSMS = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriber + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)  + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sent) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriber + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)  + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sent) + ", '%Y %m %d %T')";

		if (clipID == null)
			query += " AND " + CLIP_ID_COL + " IS NULL";
		else
			query += " AND " + CLIP_ID_COL + " = '" + clipID + "'";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);	
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
		return viralSMS;
	}

	static boolean removeViralPromotion(Connection conn, String subscriberID, String caller, Date sent, String smsType)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)+" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sent) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(smsType) + " AND "  + CALLER_ID_COL +  getNullForWhere(caller)+" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sent) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
	}
	
	static boolean deleteViralPromotion(Connection conn, String subscriberID, String callerID, List<String> typeList, Date sentTime)
	{
		int n = -1;
		Statement stmt = null;

		String whereClause = "";
		
        StringBuffer strBuff = new StringBuffer();
        String type = null;
        if(typeList!=null && typeList.size()>0){
        	Iterator<String> iterator = typeList.iterator();
        	while(iterator.hasNext())
        		strBuff.append(",'"+iterator.next() + "'");
        	if(strBuff.length()>0)
        	    type = strBuff.toString().substring(1);
        }
        
		if (subscriberID != null)
			whereClause += " "+ SUBSCRIBER_ID_COL +" = "+ sqlString(subscriberID);

		if (callerID != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += CALLER_ID_COL +" = "+ sqlString(callerID);
		}

		if (type != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";
			whereClause += SMS_TYPE_COL +" IN ( " + type + " )";
		}

		if (sentTime != null)
		{
			if (!whereClause.equals(""))
				whereClause += " AND ";

			if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
				whereClause += "TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
			else
				whereClause += "DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";
		}

		if (whereClause.equals(""))
			return false;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + whereClause;

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return (n > 0);
	}
	
	public static boolean deleteViralPromotionBySMSID(Connection conn, long smsID)
	{
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("DELETE FROM ").append(TABLE_NAME);
		queryBuilder.append(" WHERE ").append(SMS_ID_COL);
		queryBuilder.append(" = ").append(smsID);

		logger.info("Executing query: " + queryBuilder.toString());
		Statement statement = null;
		try
		{
			statement = conn.createStatement();
			statement.executeUpdate(queryBuilder.toString());
			int deleteCount = statement.getUpdateCount();
			return (deleteCount > 0);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(statement, null);
		}

		return false;
	}

	static boolean removeCopyViralPromotion(Connection conn, String subscriberID, String caller, Date sent)
	{
		int n = -1;
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " IN ('COPY','COPYSTAR','COPYCONFIRM','COPYCONFIRMED') AND "  + CALLER_ID_COL +  getNullForWhere(caller)+" AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sent) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " IN ('COPY','COPYSTAR','COPYCONFIRM','COPYCONFIRMED') AND "  + CALLER_ID_COL +  getNullForWhere(caller)+" AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sent) + ", '%Y %m %d %T')";

		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return(n==1);
	}

	static void updateViralPromotion(Connection conn, String subscriberID,String callerID, Date sentTime, String oldType, String newType, String clipId, String extraInfo)
	{
		Statement stmt = null;

		String query = "UPDATE " + TABLE_NAME + " SET " +
        	SMS_TYPE_COL + " = " + sqlString(newType) + ", " + 
        	CLIP_ID_COL + " = " + sqlString(clipId) ;
		if(extraInfo != null)
			query = query + ","+EXTRA_INFO_COL + " = " + sqlString(extraInfo);
		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL +  getNullForWhere(callerID) + " AND TO_CHAR( " + SMS_SENT_TIME_COL + " , 'YYYY/MM/DD HH24:MI:SS') = TO_CHAR( " + sqlTime(sentTime) + ", 'YYYY/MM/DD HH24:MI:SS')";
		else
			query = query + " WHERE " + SUBSCRIBER_ID_COL + getNullForWhere(subscriberID) + " AND " + SMS_TYPE_COL + " = " + sqlString(oldType) + " AND "  + CALLER_ID_COL +  getNullForWhere(callerID) + " AND DATE_FORMAT( " + SMS_SENT_TIME_COL + " , '%Y %m %d %T') = DATE_FORMAT( " + mySQLDateTime(sentTime) + ", '%Y %m %d %T')";
	
		logger.info("Executing query: " + query);
		try
		{
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
			closeStatementAndRS(stmt, null);
		}
		return;
	}
	
	static void updateSetTime(Connection conn,String subId,String smsType){ 
	       
        String query = null; 
        Statement stmt = null; 
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB)){
	        query = "UPDATE " + TABLE_NAME + " SET " + 
	        SET_TIME_COL+" = SYSDATE"+ 
	        " WHERE " + SUBSCRIBER_ID_COL + " = '" + subId + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType); 
        }
        else{
        	query = "UPDATE " + TABLE_NAME + " SET " + 
	        SET_TIME_COL+" = SYSDATE()"+ 
	        " WHERE " + SUBSCRIBER_ID_COL + " = '" + subId + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType); 
        }

        logger.info("Executing query: " + query);
        try 
        { 
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
        	closeStatementAndRS(stmt, null);
        }
        return; 
	}
	
	public static boolean updateViralPromotionTypeBySubscriberIDAndType(Connection conn, String newType, String subscriberID, String smsType)
	{
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("UPDATE ").append(TABLE_NAME);
		queryBuilder.append(" SET ").append(SMS_TYPE_COL);
		queryBuilder.append(" = '").append(newType);
		queryBuilder.append("' WHERE ").append(SUBSCRIBER_ID_COL);
		queryBuilder.append(" = '").append(subscriberID);
		queryBuilder.append("' AND ").append(SMS_TYPE_COL);
		queryBuilder.append(" = '").append(smsType).append("'");

		logger.info("Executing query: " + queryBuilder.toString());
		Statement statement = null;
		try
		{
			statement = conn.createStatement();
			statement.executeUpdate(queryBuilder.toString());
			int updateCount = statement.getUpdateCount();
			return (updateCount > 0);
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(statement, null);
		}

		return false;
	}
	
	static void setClipId(Connection conn, String subscriberID, String smsType, String clipId){ 

        Statement stmt = null; 
        String query = "UPDATE " + TABLE_NAME + " SET " + 
        CLIP_ID_COL + " = '" + clipId +"'"+ 
        " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID + "' AND " + SMS_TYPE_COL + " = " + sqlString(smsType); 

        logger.info("Executing query: " + query);
        try 
        { 
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
        	closeStatementAndRS(stmt, null);
        } 
        return; 
	}
	
    public static String getSMSTypeLookupQuery(String smsType, long presentSequenceId, long startSequenceId, int count, String circleId)
    {
        StringBuffer result = new StringBuffer();
        result.append("SELECT * FROM " + ViralSMSTableImpl.TABLE_NAME + " where " + SMS_TYPE_COL + "=" + sqlString(smsType)); 
        if(startSequenceId >0)
        {
            result.append(" and ("+SMS_ID_COL+">"+presentSequenceId);
            result.append(" or "+SMS_ID_COL+"<"+startSequenceId+")");
        }
        else
        {
            result.append(" and "+SMS_ID_COL+">"+presentSequenceId);
        }
        if(circleId != null)
        {
            result.append(" and CIRCLE_ID='"+circleId+"'");
        }
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
            if(count >= 0)
            {
                result.append(" and rownum < "+count);
            }
            result.append(" order by sms_id");
        }
        else
        {
            result.append(" order by sms_id");
            if(count >= 0)
            {
                result.append(" limit "+count);
            }
        }
        return result.toString();
    }

    public static String getSMSTypeLookupQueryForOptOut(String smsType, long presentSequenceId, int count, String circleId)
    {
        StringBuffer result = new StringBuffer();
        result.append("SELECT * FROM " + ViralSMSTableImpl.TABLE_NAME + " where " + SMS_TYPE_COL + "=" + sqlString(smsType) + " and "+SMS_ID_COL+">"+presentSequenceId);
        if(circleId != null)
        {
            result.append(" and CIRCLE_ID='"+circleId+"'");
        }
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
            result.append(" AND "+SMS_SENT_TIME_COL + " < SYSDATE - " + Utility.getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION",30) + "/1440 ");
            if(count >= 0)
            {
                result.append(" and rownum < "+count);
            }
            result.append(" order by sms_id");
        }
        else
        {
            
            result.append(" AND SMS_SENT_TIME < TIMESTAMPADD(MINUTE,-" + Utility.getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION",30) + ",SYSDATE()) order by sms_id");
            if(count >= 0)
            {
                result.append(" limit "+count);
            }
        }
        return result.toString();
    }

    
    public String getResolveCircleIdIfRequired()
    {
        return null;
    }
	
	static boolean update(Connection conn, long smsID, String smsType, String circleID)
	{
		int updateCount = -1;
		Statement stmt = null;

		if (smsType == null && circleID == null)
			return false;

		StringBuilder updateString = new StringBuilder();
		if (smsType != null)
		{	
			updateString.append(SMS_TYPE_COL).append(" = ").append(
					sqlString(smsType));
		}
		if (circleID != null)
		{
			if (smsType != null)
				updateString.append(", ");

			updateString.append(CIRCLE_ID_COL).append(" = ").append(
					sqlString(circleID));
		}

		String query = "UPDATE " + TABLE_NAME + " SET " + updateString
				+ " WHERE " + SMS_ID_COL + " = " + smsID;

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			updateCount = stmt.getUpdateCount();
		}
		catch (SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}

		return (updateCount > 0);
	}
    
	static HashMap<String, Integer> getCountForSmsTypes(Connection conn,
			String[] smsTypes)
	{
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();

		StringBuilder smsTypesInCSV = new StringBuilder();
		for (int i = 0; i < smsTypes.length; i++)
		{
			if (i != 0)
				smsTypesInCSV.append(", ");

			smsTypesInCSV.append("'").append(smsTypes[i]).append("'");
		}

		String query = "SELECT " + SMS_TYPE_COL + ", COUNT(" + SMS_TYPE_COL
				+ ") AS COUNT FROM " + TABLE_NAME + " WHERE " + SMS_TYPE_COL
				+ " IN (" + smsTypesInCSV + ") GROUP BY " + SMS_TYPE_COL;

		logger.info("Executing query: " + query);
		Statement stmt = null;
		ResultSet results = null;
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				String smsType = results.getString(SMS_TYPE_COL);
				int count = results.getInt("COUNT");

				countMap.put(smsType, count);
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		return countMap;
	}
	
	static int getCountForSmsType(Connection conn, String smsType, int waitTime)
	{
		String query = null;
		if (m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query = "SELECT COUNT(" + SMS_TYPE_COL + ") AS COUNT FROM "
					+ TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = "
					+ sqlString(smsType) + " AND " + SMS_SENT_TIME_COL
					+ " < SYSDATE - " + waitTime + " / 1440";
		}
		else
		{
			query = "SELECT COUNT(" + SMS_TYPE_COL + ") AS COUNT FROM "
					+ TABLE_NAME + " WHERE " + SMS_TYPE_COL + " = "
					+ sqlString(smsType) + " AND " + SMS_SENT_TIME_COL
					+ " < TIMESTAMPADD(MINUTE,-" + waitTime + ",SYSDATE())";
		}

		logger.info("Executing query: " + query);
		Statement stmt = null;
		ResultSet results = null;
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
			{
				int count = results.getInt("COUNT");
				return count;
			}
		}
		catch (SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		return 0;
	}
	
	static boolean updateCircleIdForType(Connection conn, String copyType)
	{
		Statement stmt = null;
		int n = -1;

		String query = "UPDATE " + TABLE_NAME + " SET " + CIRCLE_ID_COL + " =  null WHERE " + SMS_TYPE_COL  + " = '" + copyType + "'";

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			n = stmt.executeUpdate(query);
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return false;
		}
		finally
		{
			closeStatementAndRS(stmt, null);
		}
		return (n>0);
	}
	private static String getFinalExtraInfo(String extraInfo, HashMap<String, String> extraInfoMap)
	{
		if(extraInfo == null)
			return DBUtility.getAttributeXMLFromMap(extraInfoMap);
		HashMap<String, String> existingEntries = DBUtility.getAttributeMapFromXML(extraInfo);
		existingEntries.putAll(extraInfoMap);
		String finalExtraInfoStr = DBUtility.getAttributeXMLFromMap(existingEntries);
		return finalExtraInfoStr;
	}

	public static ViralSMSTable getAllViralSMS(Connection conn, String subscriberID,
			String[] typeList, int duration, boolean order) {
		if(typeList == null || subscriberID == null ){
			return null;
		}
		Date sentTime = null;
		String type = null;
		String callerID = null;
		String clipID = null;
		int count = -1;
		String selectedBy = null;
		Date setTime = null;
		String extraInfo = null;
		String query = null ;
		
		String typeString = "";
		for (String tmp : typeList) {
			typeString += sqlString(tmp)+","; 
		}
		typeString = typeString.substring(0, typeString.length()-1);
		if (order) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ "AND " + SMS_TYPE_COL + " IN (" + typeString + ") AND "
					+ SMS_SENT_TIME_COL + " > TIMESTAMPADD(MINUTE,-" + duration
					+ ",SYSDATE())" + " UNION " + "SELECT * FROM " + TABLE_NAME
					+ " WHERE " + CALLER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + SMS_TYPE_COL
					+ " IN ( 'COPYCONFPENDING' ) AND " + SMS_SENT_TIME_COL
					+ " > TIMESTAMPADD(MINUTE,-" + duration
					+ ",SYSDATE()) ORDER BY " + SMS_SENT_TIME_COL + " ASC";

		} else {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ SUBSCRIBER_ID_COL + " = " + sqlString(subscriberID)
					+ "AND " + SMS_TYPE_COL + " IN (" + typeString + ") AND "
					+ SMS_SENT_TIME_COL + " > TIMESTAMPADD(MINUTE,-" + duration
					+ ",SYSDATE())" + " UNION " + "SELECT * FROM " + TABLE_NAME
					+ " WHERE " + CALLER_ID_COL + " = "
					+ sqlString(subscriberID) + "AND " + SMS_TYPE_COL
					+ " IN ( 'COPYCONFPENDING' ) AND " + SMS_SENT_TIME_COL
					+ " > TIMESTAMPADD(MINUTE,-" + duration
					+ ",SYSDATE()) ORDER BY " + SMS_SENT_TIME_COL + " DESC";

		}
		Statement stmt = null;
		ResultSet results = null;
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if (results.next())
			{
				subscriberID = results.getString(SUBSCRIBER_ID_COL);
				sentTime = results.getTimestamp(SMS_SENT_TIME_COL);
				type = results.getString(SMS_TYPE_COL);
				callerID = results.getString(CALLER_ID_COL);
				clipID = results.getString(CLIP_ID_COL);
				count = results.getInt(SEARCH_COUNT_COL);
				selectedBy = results.getString(SELECTED_BY_COL);
				setTime = results.getTimestamp(SET_TIME_COL);
				extraInfo = results.getString(EXTRA_INFO_COL);
				
				ViralSMSTable viralSMS = new ViralSMSTableImpl(subscriberID, sentTime, type, callerID, clipID, count, selectedBy, setTime, extraInfo);
				return viralSMS;
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}

		return null;
	}


}