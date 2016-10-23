package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.TnbSubscriber;

public class TnbSubscriberImpl extends RBTPrimitive implements TnbSubscriber
{
	private static Logger logger = Logger.getLogger(TnbSubscriberImpl.class);
	
	static String tableName = "RBT_TNB_SUBSCRIBER";
	static String seqIdCol = "SEQ_ID";
	static String subIdCol = "SUBSCRIBER_ID";
	static String circleIdCol = "CIRCLE_ID";
	static String chargePackCol = "CHARGE_PACK";
	static String startDateCol = "START_DATE";
	static String iterIdCol = "ITER_ID";
	
	private long seqId = -1;
	private String subId = null;
	private String circleId = null;
	private String chargePack = null;
	private Date startDate = null;
	private int iterId = -1;
	
	private static String seqName = "RBT_TNB_SEQUENCE";
	private static String m_databaseType = getDBSelectionString();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	public String toString()
	{
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("seq="+seqId);
		sBuilder.append(", subId="+subId);
		sBuilder.append(", circleId="+circleId);
		sBuilder.append(", chargePack="+chargePack);
		sBuilder.append(", startDate="+sdf.format(startDate));
		sBuilder.append(", iterId="+iterId);
		return sBuilder.toString();
	}
	public String chargepack()
	{
		return chargePack;
	}

	public String circleID()
	{
		return circleId;
	}

	public int iterID()
	{
		return iterId;
	}

	public long seqID()
	{
		return seqId;
	}

	public Date startDate()
	{
		return startDate;
	}

	public String subID()
	{
		return subId;
	}

	public void setChargepack(String chargePack)
	{
		this.chargePack = chargePack;
	}

	public void setCircleID(String circleId)
	{
		this.circleId = circleId;
	}

	public void setIterID(int iterId)
	{
		this.iterId = iterId;
	}

	public void setSeqID(long seqId)
	{
		this.seqId = seqId;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public void setSubID(String subId)
	{
		this.subId = subId;
	}

	private TnbSubscriberImpl(long seqId, String subId, String circleId,
			String chargePack, Date startDate, int iterId)
	{
		this.seqId = seqId;
		this.subId = subId;
		this.circleId = circleId;
		this.chargePack = chargePack;
		this.startDate = startDate;
		this.iterId = iterId;
	}
	
	public static void insert(Connection conn, String subId, String circleId, String chargePack, 
			Date startDate, int iterId)
	{
		String query = null;
		Statement stmt = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query = "INSERT INTO RBT_TNB_SUBSCRIBER ("+seqIdCol+","+subIdCol+","+circleIdCol+","+chargePackCol+","+
			startDateCol+","+iterIdCol+") VALUES ( "+ seqName+".NEXTVAL , "+sqlString(subId)+","+sqlString(circleId)+","+
			sqlString(chargePack)+","+sqlTime(startDate)+","+iterId+" )"; 
		}
		else
		{
			query = "INSERT INTO RBT_TNB_SUBSCRIBER ("+subIdCol+","+circleIdCol+","+chargePackCol+","+
			startDateCol+","+iterIdCol+") VALUES ("+sqlString(subId)+","+sqlString(circleId)+","+
			sqlString(chargePack)+","+mySQLDateTime(startDate)+","+iterId+" )"; 
		}
		
		logger.info("query=" +query);

		try
		{
			logger.info("inside try block");
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
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
	}
	
	static boolean updateIterId(Connection conn, long seqId , int iterId)  
	{
		logger.info("RBT::entering");

		int n = -1;
		String query = null;
		Statement stmt = null;

		
		query = "UPDATE " + tableName + " SET " + iterIdCol + " = " + iterId + 
		" WHERE " + seqIdCol + " = " + seqId;

		logger.info("query="+query);

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

	static TnbSubscriber[] getViralSMS(Connection conn, long a_seqId, String a_circleId, String a_chargePack, 
			Date a_startDate, int a_iterId, int a_count)
	{
		logger.info("Entering");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		long seqId = -1;
		String subscriberID = null;
		String chargePack = null;
		Date startDate = null;
		int iterId = -1;
		
		TnbSubscriber tnbSubscriber = null;
		ArrayList<TnbSubscriber> tnbSubscriberList = new ArrayList<TnbSubscriber>();

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
			query = "SELECT * FROM " + tableName + " WHERE " + seqIdCol + " > " + a_seqId + " AND "
				+ startDateCol + " = "+toSQLDate(a_startDate, "DD-MM-YYYY", new SimpleDateFormat("dd-MM-yyyy"))+ " AND "+ chargePackCol + " = "+sqlString(a_chargePack)
				+ " AND "+ iterIdCol + " = "+ a_iterId + " AND "+ circleIdCol + " = " + a_circleId;
		else
			query = "SELECT * FROM " + tableName + " WHERE " + seqIdCol + " > " + a_seqId + " AND "
		+ startDateCol + " = "+mySqlTime(a_startDate)+ " AND "+ chargePackCol + " = "+sqlString(a_chargePack)
		+ " AND "+ iterIdCol + " = "+ a_iterId + " AND "+ circleIdCol + " = " + a_circleId;

		
		logger.info("query="+query);

		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				seqId = results.getLong(seqIdCol);
				subscriberID = results.getString(subIdCol);
				tnbSubscriber = new TnbSubscriberImpl(seqId, subscriberID, a_circleId, chargePack, startDate, iterId);
				tnbSubscriberList.add(tnbSubscriber);
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
		if(tnbSubscriberList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_TNB_SUBSCRIBER successful");
			return (TnbSubscriber[])tnbSubscriberList.toArray(new TnbSubscriber[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_TNB_SUBSCRIBER");
			return null;
		}
	}

	public static TnbSubscriberImpl getNextTnbSubscriber(ResultSet results) throws SQLException
	{
		long seqId = results.getLong(seqIdCol);
		String subscriberID = results.getString(subIdCol);
        String circleId = results.getString(circleIdCol);
		String chargePack = results.getString(chargePackCol);
		Date startDate = results.getDate(startDateCol);
		int iterId = results.getInt(iterIdCol);
		TnbSubscriberImpl tnbSubscriberImpl = new TnbSubscriberImpl(seqId, subscriberID, circleId, chargePack, startDate, iterId);
		return tnbSubscriberImpl;
	}

	public static boolean delete(Connection conn, long seqId)
	{
		logger.info("RBT::entering");

		int n = -1;
		String query = null;
		Statement stmt = null;

		
		query = "DELETE FROM  " + tableName + " WHERE " + seqIdCol + " = " + seqId;

		logger.info("query="+query);

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
	public static boolean deleteSubscriber(Connection conn, String subID)
	{
		logger.info("RBT::entering");

		int n = -1;
		String query = null;
		Statement stmt = null;

		
		query = "DELETE FROM  " + tableName + " WHERE " + subIdCol + " = " + sqlString(subID);

		logger.info("query="+query);

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
	
	public static boolean bulkDelete(Connection conn)
	{
		logger.info("RBT::entering");

		int n = -1;
		String query = null;
		Statement stmt = null;

		
		if(getDBSelectionString().equalsIgnoreCase(DB_MYSQL))
			query = "DELETE FROM " + tableName + " WHERE " + startDateCol + " < TIMESTAMPADD(DAY,-97,SYSDATE())" ;
		else
			query = "DELETE FROM " + tableName + " WHERE " + startDateCol + " < SYSDATE-97" ;
		

		logger.info("query="+query);

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

	public static TnbSubscriberImpl getTNBSubscriber(Connection conn, String subId, String circleId)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			query = "SELECT " + subIdCol + "," + circleIdCol + " FROM RBT_TNB_SUBSCRIBER WHERE " + subIdCol  + " = " + sqlString(subId) + " AND " +
			circleIdCol + " = " + sqlString(circleId); 
		}
		else
		{
			query = "SELECT * FROM RBT_TNB_SUBSCRIBER WHERE " + subIdCol  + " = " + sqlString(subId) + " AND " +
			circleIdCol + " = " + sqlString(circleId); 
		}
		
		logger.info("query=" +query);

		try
		{
			logger.info("inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.first()) {
				RBTResultSet rbtResult = new RBTResultSet(results);
				return getNextTnbSubscriber(results);
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
