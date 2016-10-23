package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.onmobile.apps.ringbacktones.content.Access;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class AccessImpl extends RBTPrimitive implements Access
{
	private static Logger logger = Logger.getLogger(AccessImpl.class);

	private static final String TABLE_NAME = "RBT_ACCESS_TABLE";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String CLIP_NAME_COL = "CLIP_NAME";
	private static final String YEAR_COL = "YEAR";
	private static final String MONTH_COL = "MONTH";
	private static final String NO_OF_PREVIEWS_COL = "NO_OF_PREVIEWS";
	private static final String NO_OF_ACCESS_COL = "NO_OF_ACCESS";
	private static final String NO_OF_PLAYS_COL = "NO_OF_PLAYS";
	private static final String ACCESS_DATE_COL = "ACCESS_DATE";

	private int m_clipID;
	private String m_name;
	private String m_year;
	private String m_month;
	private int m_noOfPreviews;
	private int m_noOfAccess;
	private int m_noOfPlays;
	private Date m_accessDate;
	private static String m_databaseType=getDBSelectionString();

	private AccessImpl(int clipID, String name, String year, String month, int noOfPreviews, int noOfAccess, int noOfPlays, Date accessDate)
	{
		m_clipID = clipID;
		m_name = name;
		m_year = year;
		m_month = month;
		m_noOfPreviews = noOfPreviews;
		m_noOfAccess = noOfAccess;
		m_noOfPlays = noOfPlays;
		m_accessDate = accessDate;
	}

	public int clipID()
	{
		return m_clipID;
	}

	public String clipName()
	{
		return m_name;
	}

	public String year()
	{
		return m_year;
	}

	public String month()
	{
		return m_month;
	}

	public int noOfPreviews()
	{
		return m_noOfPreviews;
	}

	public void incrementNoOfPreviews()
	{
		this.m_noOfPreviews++;
	}

	public int noOfAccess()
	{
		return m_noOfAccess;
	}

	public void incrementNoOfAccess()
	{
		this.m_noOfAccess++;
	}

	public int noOfPlays()
	{
		return m_noOfPlays;
	}

	public void incrementNoOfPlays()
	{
		this.m_noOfPlays++;
	}

	public Date accessDate()
	{
		return m_accessDate;
	}


	static Access insert(Connection conn, int clipID, String name, String year, String month, int noOfPreviews, int noOfAccess, int noOfPlays, Date accessDate)
	{
		try
		{
			return insert(conn, clipID, name, year, month, noOfPreviews, noOfAccess, noOfPlays,accessDate, false);
		}
		catch(SQLException se)
		{
			return null;
		}
	}

	static Access insert(Connection conn, int clipID, String name, String year, String month, int noOfPreviews, int noOfAccess, int noOfPlays,Date accessDate, boolean bShouldExceptionBeThrown) throws SQLException
	{
		int id = -1;
		String query = null;
		Statement stmt = null;

		AccessImpl access = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + CLIP_ID_COL;
		query += ", " + CLIP_NAME_COL;
		query += ", " + YEAR_COL;
		query += ", " + MONTH_COL;
		query += ", " + NO_OF_PREVIEWS_COL;
		query += ", " + NO_OF_ACCESS_COL;
		query += ", " + NO_OF_PLAYS_COL;
		query += ", " + ACCESS_DATE_COL;
		query += ")";

		query += " VALUES ( " + clipID;
		query += ", " + sqlString(name);
		query += ", " + sqlString(year);
		query += ", " + sqlString(month);
		query += ", " + noOfPreviews;
		query += ", " + noOfAccess;
		query += ", " + noOfPlays;
		if(m_databaseType.equals(DB_SAPDB)){
			query += ", " + sqlTime(accessDate);
		}else{
			query += ", " + mySqlTime(accessDate);
		}
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
			closeStatementAndRS(stmt, null);
		}
		if(id == 0)
		{
			logger.info("RBT::insertion to RBT_ACCESS_TABLE table successful");
			access = new AccessImpl(clipID, name, year, month, noOfPreviews,  noOfAccess, noOfPlays, accessDate);
			return access;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_ACCESS_TABLE table failed");
			return null;
		}
	}

	public void update(String dbUrl, int nConn)
	{
		Connection conn = getConnection();
		if(conn == null)
			return;
		
		boolean success = false;
		try
		{
			success = update(conn, this.m_clipID, this.m_name, this.m_year, this.m_month, this.m_noOfPreviews, this.m_noOfAccess, this.m_noOfPlays, this.m_accessDate);
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(conn);
		}
		logger.info("Update into RBT_ACCESS_TABLE " + (success ? "successful" : "failed"));
	}

	public void update(Connection conn)
	{
		boolean success = update(conn, this.m_clipID, this.m_name, this.m_year, this.m_month, this.m_noOfPreviews, this.m_noOfAccess, this.m_noOfPlays, this.m_accessDate);
		logger.info("Update into RBT_ACCESS_TABLE " + (success ? "successful" : "failed"));
	}

	static boolean update(Connection conn, int clipID, String name, String year, String month, int noOfPreviews, int noOfAccess, int noOfPlays, Date accessDate)   
	{
		int n = -1;
		Statement stmt = null;

		String query = "UPDATE " + TABLE_NAME + " SET " +
		CLIP_NAME_COL + " = " + "'" + name + "'" + ", " +
		NO_OF_PREVIEWS_COL + " = " + noOfPreviews + ", " +
		NO_OF_ACCESS_COL + " = " + noOfAccess + ", " +
		NO_OF_PLAYS_COL + " = " + noOfPlays + ", " ;
		if(m_databaseType.equals(DB_SAPDB)){
			query=query+ACCESS_DATE_COL + " = " + sqlTime(accessDate); 
		}else if(m_databaseType.equals(DB_MYSQL)){
			query=query+ACCESS_DATE_COL + " = " + mySqlTime(accessDate);
		}
		query=query+" WHERE " + CLIP_ID_COL + " = " + clipID;
		
		if(accessDate != null) {
			if(m_databaseType.equals(DB_SAPDB)){
				query = query + " AND "+ ACCESS_DATE_COL  + " = " + sqlTime(accessDate);
			}else if(m_databaseType.equals(DB_MYSQL)){
				query = query + " AND "+ ACCESS_DATE_COL  + " = " + mySqlTime(accessDate);
			}
			
		} else {
			query = query + " AND " + YEAR_COL + " = " + "'" + year + "'" + " AND " + MONTH_COL  + " = " + "'" + month + "'";
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

	static Access getAccess(Connection conn, int clipID, String year, String month,Date accessDate)
	{
		try
		{
			return getAccess(conn, clipID, year, month,accessDate , false);
		}
		catch(SQLException se)
		{
			return null;
		}
	}

	static Access getAccess(Connection conn, int clipID, String year, String month,Date curDate, boolean bShouldExceptionBeThrown) throws SQLException
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		AccessImpl access = null;

		if(curDate != null){
			if(m_databaseType.equals(DB_SAPDB)){
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipID + " AND " + ACCESS_DATE_COL + " = " + sqlTime(curDate);
			}else if(m_databaseType.equals(DB_MYSQL)){
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipID + " AND " + ACCESS_DATE_COL + " = " + mySqlTime(curDate);
			}
			
		}
		else{ 
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipID + " AND " + YEAR_COL + " = " + "'" + year + "'" + " AND " + MONTH_COL  + " = " + "'" + month + "'";
		}

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			if (results.next())
			{
				String name = results.getString(CLIP_NAME_COL);
				int noOfPreviews = results.getInt(NO_OF_PREVIEWS_COL);
				int noOfAccess = results.getInt(NO_OF_ACCESS_COL);
				int noOfPlays = results.getInt(NO_OF_PLAYS_COL);
				Date accessDate = results.getTimestamp(ACCESS_DATE_COL);

				access = new AccessImpl(clipID, name, year, month, noOfPreviews, noOfAccess, noOfPlays,accessDate);
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
			closeStatementAndRS(stmt, results);
		}
		return access;
	}

	static Integer[] getMostAccesses(Connection conn, int fetchSize,int noOfhotSongsDays)
	{
		String query = null;
		Statement stmt = null;
		RBTResultSet results = null;

		Integer clipID = null;
		Date currentDays = null;
		Date daysBack = null;

		ArrayList<Integer> clipList = new ArrayList<Integer>();

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		currentDays = calendar.getTime();
		calendar.add(Calendar.DATE, noOfhotSongsDays * -1);
		daysBack = calendar.getTime(); 
		
		if(m_databaseType.equals(DB_SAPDB)){
			query = "SELECT CLIP_ID, SUM(NO_OF_ACCESS) AS NO_OF_ACCESS FROM " + TABLE_NAME +
			" WHERE ACCESS_DATE BETWEEN " + sqlTime(daysBack) + " AND " + sqlTime(currentDays) +
			" GROUP BY CLIP_ID ORDER BY NO_OF_ACCESS DESC";
		}else if(m_databaseType.equals(DB_MYSQL)){
			query = "SELECT CLIP_ID, SUM(NO_OF_ACCESS) AS NO_OF_ACCESS FROM " + TABLE_NAME +
			" WHERE ACCESS_DATE BETWEEN " + mySqlTime(daysBack) + " AND " + mySqlTime(currentDays) +
			" GROUP BY CLIP_ID ORDER BY NO_OF_ACCESS DESC";
		}

		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.setMaxRows(fetchSize);
			results = new RBTResultSet(stmt.executeQuery(query));
			while(results.next())
			{
				clipID = new Integer(results.getString(CLIP_ID_COL));
				clipList.add(clipID);
			}
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

		logger.info("Total records retrieved from RBT_ACCESS_TABLE table: " + clipList.size());
		if(clipList.size() > 0)
		{
			return((Integer[]) clipList.toArray(new Integer[0]));
		}

		return null;
	}


	static boolean remove(Connection conn, int clipID, String year, String month, Date accessDate)
	{
		int n = -1;
		Statement stmt = null;

		String query = "DELETE FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipID;
		
		if(accessDate != null) {
			if (m_databaseType.equals(DB_SAPDB)) {
				query = query + " AND "+ ACCESS_DATE_COL  + " = " + sqlTime(accessDate);
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = query + " AND "+ ACCESS_DATE_COL  + " = " + mySqlTime(accessDate);
			}
			
		} else {
			query = query + " AND " + YEAR_COL + " = " + "'" + year + "'" + " AND " + MONTH_COL  + " = " + "'" + month + "'";
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

	static boolean removeAccessToDate(Connection conn, int noOfhotSongsDaysToRemove)
	{
		String query = null;
		Statement stmt = null;

		Date daysBack = null;
		boolean success = false;

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DATE, noOfhotSongsDaysToRemove * -1);
		daysBack = calendar.getTime(); 
		
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCESS_DATE_COL + " <= " + sqlTime(daysBack);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCESS_DATE_COL + " <= " + mySqlTime(daysBack);
		}
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			int n = stmt.getUpdateCount();
			if(n >= 0) {
				success = true;
			}
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

		return success;
	}
	
	static boolean clearAccessTable(Connection conn, int backupDays)
	{
		String query = null;
		Statement stmt = null;

		Date daysBack = null;
		boolean success = false;

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DATE, backupDays * -1);
		daysBack = calendar.getTime(); 
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCESS_DATE_COL + " <= " + sqlTime(daysBack);
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCESS_DATE_COL + " <= " + mySqlTime(daysBack);
		}
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
			int n = stmt.getUpdateCount();
			if(n >= 0) {
				success = true;
			}
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

		return success;
	}
	
	static AccessImpl[] getAllUGCAccess(Connection conn, String year, String month)
	{
		Statement stmt = null;
		RBTResultSet results = null;
 
		ArrayList<AccessImpl> accessList = new ArrayList<AccessImpl>();
		AccessImpl access = null;
 
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " >= 10000000 AND "+ YEAR_COL + " = '" +
				year + "' AND " + MONTH_COL + " = '" + month + "'";
		
		logger.info("Executing query: " + query);
		try
		{
			stmt = conn.createStatement();
			results = new RBTResultSet(stmt.executeQuery(query));
			while (results.next())
			{
				int clipId = results.getInt(CLIP_ID_COL);
				String name = results.getString(CLIP_NAME_COL);
				int noOfPreviews = results.getInt(NO_OF_PREVIEWS_COL);
				int noOfAccess = results.getInt(NO_OF_ACCESS_COL);
				int noOfPlays = results.getInt(NO_OF_PLAYS_COL);
				Date accessDate = results.getTimestamp(ACCESS_DATE_COL);
 
				access = new AccessImpl(clipId, name, null, null, noOfPreviews, noOfAccess, noOfPlays,accessDate);	  
				accessList.add(access);
			}
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return null;
		}
		finally
		{
			closeStatementAndRS(stmt, results);
		}
 
        logger.info("Total records retrieved from RBT_ACCESS_TABLE table: " + accessList.size());
		if (accessList.size() > 0)
		{
			return (AccessImpl[]) accessList.toArray(new AccessImpl[0]);
		}
		return null;
	}
	 
	static Access[] getTopUGCAccesses(Connection conn, String year, String month)
	{
		Statement stmt = null;
		RBTResultSet results = null;
 
		ArrayList<Access> accessList = new ArrayList<Access>();
		AccessImpl access = null;
 
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " >= 10000000 AND " + 
				YEAR_COL + " = '" + year + "' AND " + MONTH_COL + " = '" + month + "' ORDER BY " + 
				NO_OF_PLAYS_COL + " DESC";
 
         logger.info("RBT::query "+query);
         try
         {
             stmt = conn.createStatement();
             results = new RBTResultSet(stmt.executeQuery(query));
             while (results.next())
             {
             	int clipId = results.getInt(CLIP_ID_COL);
                String name = results.getString(CLIP_NAME_COL);
                int noOfPreviews = results.getInt(NO_OF_PREVIEWS_COL);
                int noOfAccess = results.getInt(NO_OF_ACCESS_COL);
                int noOfPlays = results.getInt(NO_OF_PLAYS_COL);
                Date accessDate = results.getTimestamp(ACCESS_DATE_COL);
 
                access = new AccessImpl(clipId, name, null, null, noOfPreviews, noOfAccess, noOfPlays,accessDate);
                accessList.add(access);
             }
         }
         catch(SQLException se)
         {
        	 logger.error("", se);
             return null;
         }
         finally
         {
         	closeStatementAndRS(stmt, results);
         }
 
         logger.info("Total records retrieved from RBT_ACCESS_TABLE table: " + accessList.size());
         if (accessList.size() > 0)
         {
             return (AccessImpl[]) accessList.toArray(new AccessImpl[0]);
         }
         return null;
     }
	
	static Integer[] getClipsInMostAccessOrder(Connection conn, int[] clips, int accessDays) 
    { 
        String query = null; 
        Statement stmt = null; 
        RBTResultSet results = null; 
 
        String clipID = null; 
        Date currentDays = null; 
        Date daysBack = null; 
 
        ArrayList<Integer> clipList = new ArrayList<Integer>(); 
 
        Calendar calendar = Calendar.getInstance(); 
        calendar.set(Calendar.HOUR_OF_DAY, 0); 
        calendar.set(Calendar.MINUTE, 0); 
        calendar.set(Calendar.SECOND, 0); 
        calendar.set(Calendar.MILLISECOND, 0); 
        currentDays = calendar.getTime(); 
        calendar.add(Calendar.DATE, accessDays * -1); 
        daysBack = calendar.getTime(); 
 
        String clipIDsQueryString = CLIP_ID_COL +" IN ("; 
        for(int i = 0; i < clips.length-1; i++) 
            clipIDsQueryString += clips[i]+", "; 
        clipIDsQueryString += clips[clips.length-1]+") "; 
        
        if (m_databaseType.equals(DB_SAPDB)) {
        	query = "SELECT CLIP_ID, SUM(NO_OF_ACCESS) AS NO_OF_ACCESS FROM " 
                + TABLE_NAME + " WHERE " + clipIDsQueryString 
                + " AND ACCESS_DATE BETWEEN " + sqlTime(daysBack) + " AND " 
                + sqlTime(currentDays) 
                + " GROUP BY CLIP_ID ORDER BY NO_OF_ACCESS DESC";
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT CLIP_ID, SUM(NO_OF_ACCESS) AS NO_OF_ACCESS FROM " 
                + TABLE_NAME + " WHERE " + clipIDsQueryString 
                + " AND ACCESS_DATE BETWEEN " + mySqlTime(daysBack) + " AND " 
                + mySqlTime(currentDays) 
                + " GROUP BY CLIP_ID ORDER BY NO_OF_ACCESS DESC";
		}
         
        logger.info("Executing query: " + query); 
        try 
        { 
            stmt = conn.createStatement(); 
            results = new RBTResultSet(stmt.executeQuery(query)); 
            while(results.next()) 
            { 
                clipID = results.getString(CLIP_ID_COL); 
                clipList.add(new Integer(clipID)); 
            } 
        } 
        catch(SQLException se) 
        { 
        	logger.error("", se); 
            return null; 
        } 
        finally 
        {
        	closeStatementAndRS(stmt, results);
        } 

        logger.info("Total records retrieved from RBT_ACCESS_TABLE table: " + clipList.size());
        if(clipList.size() > 0) 
        { 
            return((Integer[]) clipList.toArray(new Integer[0])); 
        } 
        return null; 
    } 
}
