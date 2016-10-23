package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.PickOfTheDay;

public class PickOfTheDayImpl extends RBTPrimitive implements PickOfTheDay
{
	private static Logger logger = Logger.getLogger(PickOfTheDayImpl.class);

	private static final String TABLE_NAME = "RBT_PICK_OF_THE_DAY";
	private static final String CATEGORY_ID_COL = "CATEGORY_ID";
	private static final String CLIP_ID_COL = "CLIP_ID";
	private static final String PLAY_DATE_COL = "PLAY_DATE";
	private static final String CIRCLE_ID_COL = "CIRCLE_ID";
	private static final String PREPAID_YES_COL = "PREPAID_YES";
	private static final String PROFILE_COL = "PROFILE";
	private static final String LANGUAGE_COL = "LANGUAGE";

	private int m_categoryID;
	private int m_clipID;
	private String m_playDate;
	private String m_circleId;
	private char m_prepaidYes = 'b';
	private String m_profile;
	private String m_language;
	private static String m_databaseType=getDBSelectionString();

	private PickOfTheDayImpl(int categoryID, int clipID, String playDate)
	{
		m_categoryID = categoryID;
		m_clipID = clipID;
		m_playDate = playDate;
	}

	private PickOfTheDayImpl(int categoryID, int clipID, String playDate, String circleId, char prepaidYes, String profile)
	{
		m_categoryID = categoryID;
		m_clipID = clipID;
		m_playDate = playDate;
		m_circleId = circleId;
		m_prepaidYes = prepaidYes;
		m_profile = profile;
	}
	
	private PickOfTheDayImpl(int categoryID, int clipID, String playDate, String circleId, char prepaidYes, String profile, String language)
	{
		m_categoryID = categoryID;
		m_clipID = clipID;
		m_playDate = playDate;
		m_circleId = circleId;
		m_prepaidYes = prepaidYes;
		m_profile = profile;
		m_language = language;
	}

	public int categoryID()
	{
		return m_categoryID;
	}

	public int clipID()
	{
		return m_clipID;
	}

	public String playDate()
	{
		return m_playDate;
	}

	public String circleID()
	{
		return m_circleId;
	}

	public char prepaidYes()
	{
		return m_prepaidYes;
	}

	public String profile() 
	{ 
		return m_profile; 
	}
	
	public String language()
	{
		return m_language;
	}

	/*static PickOfTheDay insert(Connection conn, int categoryID, int clipID, String playDate)
    {
        logger.info("RBT::inside insert");        

		int id = -1;
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		PickOfTheDayImpl pickOfTheDay = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + CATEGORY_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + PLAY_DATE_COL;
		query += ")";

		query += " VALUES ( " + categoryID;
		query += ", " + clipID;
		if(playDate == null)
			query += ", TO_CHAR(SYSDATE, 'DD/MM/YYYY')";
		else
			query += ", '" + playDate + "' ";
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
            logger.info("RBT::insertion to RBT_PICK_OF_THE_DAY table successful");
            pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, playDate);
            return pickOfTheDay;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_PICK_OF_THE_DAY table failed");
            return null;
        }
    }*/


	static PickOfTheDay insert(Connection conn, int categoryID, int clipID, String playDate, String circleId, char prepaidYes, String profile, String language)
	{
		logger.info("RBT::inside insert");
		
		int id = -1;
		String query = null;
		Statement stmt = null;

		PickOfTheDayImpl pickOfTheDay = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + CATEGORY_ID_COL;
		query += ", " + CLIP_ID_COL;
		query += ", " + PLAY_DATE_COL;
		query += ", " + CIRCLE_ID_COL;
		query += ", " + PREPAID_YES_COL;
		query += ", " + PROFILE_COL;
		query += ", " + LANGUAGE_COL;
		query += ")";

		query += " VALUES ( " + categoryID;
		query += ", " + clipID;
		if(playDate == null)
		{
			if (m_databaseType.equals(DB_SAPDB))
				query += ", TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY')";
			else if (m_databaseType.equals(DB_MYSQL))
				query += ", DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y')";
		}
		else
			query += ", '" + playDate + "' ";
		
		query += ", " + sqlString(circleId);
		query += ", '" + prepaidYes + "'";
		query += ", " + sqlString(profile);
		query += ", " + sqlString(language);
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
			logger.info("RBT::insertion to RBT_PICK_OF_THE_DAY table successful");
			//if(circleId!=null && (prepaidYes == 'Y'||prepaidYes == 'y'||prepaidYes == 'n'||prepaidYes == 'N'))
			pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, playDate, circleId, prepaidYes, profile,language);
			//else
			//	pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, playDate);
			return pickOfTheDay;
		} 
		else
		{
			logger.info("RBT::insertion to RBT_PICK_OF_THE_DAY table failed");
			return null;
		}
	}


	static PickOfTheDay [] getPickOfTheDays(Connection conn, String startDate, String endDate , String circleId)
	{
		logger.info("RBT::inside getPickOfTheDays");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;
		String prepaidYesString = null;
		char prepaidYes;
		String profile = null;
		String language = null;

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE TO_DATE("+ PLAY_DATE_COL + ",'DD/MM/YYYY') BETWEEN " 
			+ sqlString(startDate) + " AND " + sqlString(endDate) ;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "SELECT * FROM " + TABLE_NAME + " WHERE STR_TO_DATE("+ PLAY_DATE_COL + ",'%d/%m/%Y') BETWEEN " 
			+ sqlString(startDate) + " AND " + sqlString(endDate)  ;
		}

			query = query + " AND " + PROFILE_COL + " IN ('REN','ACT')" ;

		if(circleId!=null)
			query = query + " AND " + CIRCLE_ID_COL + " = '" + circleId + "' " ;
		
		query = query + " ORDER BY "+ CIRCLE_ID_COL + ", "+ PREPAID_YES_COL + ", "+ PLAY_DATE_COL;

		logger.info("RBT::query "+query);

		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);

				if(circleId!=null)
				{
					prepaidYesString = results.getString(PREPAID_YES_COL);

					prepaidYes = prepaidYesString.charAt(0);
					profile = results.getString(PROFILE_COL);
					language = results.getString(LANGUAGE_COL);

					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId, prepaidYes, profile, language);
				}
				else
				{
					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
				}
				
				pickList.add(pickOfTheDay);
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
		if(pickList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
			return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
			return null;
		}
	}
	
	/*static boolean update(Connection conn, int categoryID, int clipID, String playDate)
    {
        logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 CATEGORY_ID_COL + " = " + categoryID + ", " +
    			 CLIP_ID_COL + " = " + clipID;
				 if(playDate == null)
				 { 
					query = query + " WHERE " + PLAY_DATE_COL + " = TO_CHAR(SYSDATE, 'DD/MM/YYYY')";
				 }
				 else
				 {
					query = query + " WHERE " + PLAY_DATE_COL + " = '" + playDate + "'" ;
				 }

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
    }*/

	static boolean update(Connection conn, int categoryID, int clipID, String playDate, String circleId, char prepaidYes, String profile, String language)
	{
		logger.info("RBT::inside update");

		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " + CATEGORY_ID_COL + " = "
				+ categoryID + ", " + CLIP_ID_COL + " = " + clipID;
		if(playDate == null)
		{ 
			if (m_databaseType.equals(DB_SAPDB))
				query += " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY')";
			else if (m_databaseType.equals(DB_MYSQL))
				query += " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y')";
		}
		else
		{
			query += " WHERE " + PLAY_DATE_COL + " = '" + playDate + "'" ;
		}


		if(circleId != null)
			query += " AND " + CIRCLE_ID_COL + " = '" + circleId + "'";

		query += " AND " + PREPAID_YES_COL + " = '" + prepaidYes + "'";

		if(profile != null)
			query += " AND " + PROFILE_COL + " = '" + profile + "'";
		
		if(language != null)
			query += " AND " + LANGUAGE_COL + " = '" + language + "'";

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

	/*static PickOfTheDay [] getAllPickOfTheDays(Connection conn)
    {
        logger.info("RBT::inside getAllPickOfTheDays");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null; 

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME;

		logger.info("RBT::query "+query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						categoryID = results.getInt(CATEGORY_ID_COL);
						clipID = results.getInt(CLIP_ID_COL);
						date = results.getString(PLAY_DATE_COL);

						pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
						pickList.add(pickOfTheDay);
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
        if(pickList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
            return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
        } 
		else
        {
            logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
            return null;
        }
    }*/

	static PickOfTheDay [] getAllPickOfTheDays(Connection conn, String circleId, char isPrepaid, String playDate)
	{
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;
		String prepaidYesString = null;
		char prepaidYes;
		String profile = null;
		String language = null;

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL
				+ " = '" + playDate + "' AND " + CIRCLE_ID_COL + " IN ('"
				+ circleId + "', 'ALL')";
		if (isPrepaid != 'b')
			query += " AND " + PREPAID_YES_COL + " IN ('" + isPrepaid + "', 'b')";

		logger.info("RBT::query "+query);

		try
		{
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);
				if(circleId != null){
					prepaidYesString = results.getString(PREPAID_YES_COL);
					prepaidYes = prepaidYesString.charAt(0);
					profile = results.getString(PROFILE_COL);
					circleId = results.getString(CIRCLE_ID_COL);
					language = results.getString(LANGUAGE_COL);
					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId, prepaidYes, profile, language);
				}
				else{
					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
				}
				pickList.add(pickOfTheDay);
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
		if(pickList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
			return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
			return null;
		}
	}

	/*static PickOfTheDay [] getPickOfTheDays(Connection conn, String range)
    {
        logger.info("RBT::inside getPickOfTheDays");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null; 

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		if(range == null)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR(SYSDATE, 'DD/MM/YYYY')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " LIKE '%" + range + "%'";

		logger.info("RBT::query "+query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						categoryID = results.getInt(CATEGORY_ID_COL);
						clipID = results.getInt(CLIP_ID_COL);
						date = results.getString(PLAY_DATE_COL);

						pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
						pickList.add(pickOfTheDay);
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
        if(pickList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
            return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
        } 
		else
        {
            logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
            return null;
        }
    }*/

	static PickOfTheDay [] getPickOfTheDays(Connection conn, String range, String circleId)
	{
		logger.info("RBT::inside getPickOfTheDays");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;
		String prepaidYesString = null;
		char prepaidYes;
		String profile = null;
		String language = null;

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		if(range == null){  
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY') ";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y') ";
			}
		}		
		else
			{
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " LIKE '%" + range + "%' " ;
			}

		if(circleId!=null)
			query = query + " AND " + CIRCLE_ID_COL + " = '" + circleId + "' " ;
		
		query = query + " ORDER BY "+ CIRCLE_ID_COL + ", "+ PREPAID_YES_COL + ", "+ PLAY_DATE_COL;

		logger.info("RBT::query "+query);

		
		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);

				if(circleId!=null){
					prepaidYesString = results.getString(PREPAID_YES_COL);

					prepaidYes = prepaidYesString.charAt(0);
					profile = results.getString(PROFILE_COL);
					language = results.getString(LANGUAGE_COL); 

					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId, prepaidYes, profile, language);
				}
				else{
					pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
				}
				pickList.add(pickOfTheDay);
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
		if(pickList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
			return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
			return null;
		}
	}

	/*static PickOfTheDay getPickOfTheDay(Connection conn, String playDate)
    {
        logger.info("RBT::inside getPickOfTheDay");

      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null; 

		PickOfTheDayImpl pickOfTheDay = null;

		if(playDate == null)
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR(SYSDATE, 'DD/MM/YYYY')";
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";

		logger.info("RBT::query "+query);

        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						categoryID = results.getInt(CATEGORY_ID_COL);
						clipID = results.getInt(CLIP_ID_COL);
						date = results.getString(PLAY_DATE_COL);

						pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
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
        return pickOfTheDay;
    }*/

	static PickOfTheDay[] getPickOfTheDay(Connection conn, String playDate, String circleId, char prepaidYes, String profile, boolean checkProfile, String language, boolean checkLanguage)
	{
		logger.info("RBT::inside getPickOfTheDay");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;
		//char prepaidYes; 

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		if(playDate == null)
		{
			if (m_databaseType.equals(DB_SAPDB))
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY') " ;
			else if (m_databaseType.equals(DB_MYSQL))
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y') " ;
		}
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";

		if(circleId != null)
			query += " AND " + CIRCLE_ID_COL + " IN ('ALL','" + circleId + "')";
		
		if(prepaidYes != 'b')
			query += " AND " + PREPAID_YES_COL + " IN ('b','" + prepaidYes + "')";

		if(checkProfile)
		{
			if(profile != null)
				query += " AND (LCASE("+ PROFILE_COL +") = LCASE('"+ profile +"') OR "+ PROFILE_COL +" IS NULL)";
			else
				query += " AND "+ PROFILE_COL +" IS NULL";
		}
		
		if(checkLanguage)
		{
			if(language != null)
				query += " AND (LCASE("+ LANGUAGE_COL +") = LCASE('"+ language +"') OR "+ LANGUAGE_COL +" IS NULL)";
			else
				query += " AND "+ LANGUAGE_COL +" IS NULL";
		}
		
		logger.info("RBT::query "+query);

		
		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);
				circleId = results.getString(CIRCLE_ID_COL);
				profile = results.getString(PROFILE_COL);
				language = results.getString(LANGUAGE_COL);
				pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId, prepaidYes, profile, language);
				pickList.add(pickOfTheDay);
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

		if(pickList.size() > 0) 
		{ 
			logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful"); 
			return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]); 
		} 
		else 
		{ 
			logger.info("RBT::no records in RBT_PICK_OF_THE_DAY"); 
			return null; 
		}

		//return pickOfTheDay;
	}

	static PickOfTheDay getPickOfTheDay(Connection conn, String playDate, String profile)
	{
		logger.info("RBT::inside getPickOfTheDay");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;

		PickOfTheDayImpl pickOfTheDay = null;

		if(playDate == null)
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY') " ;
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y') " ;
			}
			
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";

		if(profile != null)
			query = query + " AND LCASE("+ PROFILE_COL +") = LCASE('"+ profile +"')";
		else
			query = query + " AND "+ PROFILE_COL +" IS NULL";

		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);

				pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date);
				return(pickOfTheDay);
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

		logger.info("RBT::no records in RBT_PICK_OF_THE_DAY"); 
		return null; 
	}
	
	static PickOfTheDay getDownloadfTheDay(Connection conn, String playDate , String type)
	{
		logger.info("RBT::inside getPickOfTheDay for " + playDate);

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int clipID = -1;
		String date = null;
		int chargeClass = -1;
		

		PickOfTheDayImpl pickOfTheDay = null;

		if(playDate == null){
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY') " ;
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y') " ;
			}
		}	
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";
		
		query = query + " AND "+ PROFILE_COL +" = "+sqlString(type);

		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			if(results.next())
			{
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);
				chargeClass = results.getInt(CATEGORY_ID_COL);
				

				pickOfTheDay = new PickOfTheDayImpl(chargeClass , clipID , playDate);
				return(pickOfTheDay);
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

		logger.info("RBT::no records in RBT_PICK_OF_THE_DAY"); 
		return null; 
	
	}

	
	
	static ArrayList<String> getAllDownloadOfTheDayDates(Connection conn, String type)
	{
		logger.info("RBT::inside getPickOfTheDay");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;

		ArrayList<String> downloadOfTheDayDates = new ArrayList<String>();

		query = "SELECT " + PLAY_DATE_COL + " FROM " + TABLE_NAME + " WHERE " + PROFILE_COL + " = " + sqlString(type) ;
			
		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while(results.next())
			{
				downloadOfTheDayDates.add(results.getString(PLAY_DATE_COL));
				
			}
			
			return(downloadOfTheDayDates);
			
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
	}
	

	/*ADDED FOR TATA*/
	static PickOfTheDay [] getPickOfTheDayForTATAPrepaid(Connection conn, String playDate, String circleId)
	{
		logger.info("RBT::inside getPickOfTheDays");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int categoryID = -1;
		int clipID = -1;
		String date = null;
		String profile = null;

		PickOfTheDayImpl pickOfTheDay = null;
		List pickList = new ArrayList();

		if(playDate == null)
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY') AND " + CIRCLE_ID_COL + " = '" + circleId + "' AND " + PREPAID_YES_COL + " = 'y'";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y') AND " + CIRCLE_ID_COL + " = '" + circleId + "' AND " + PREPAID_YES_COL + " = 'y'";
			}
			
		else
			query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "' AND " + CIRCLE_ID_COL + " = '" + circleId + "' AND " + PREPAID_YES_COL + " = 'y'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info("RBT::inside try block");
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				categoryID = results.getInt(CATEGORY_ID_COL);
				clipID = results.getInt(CLIP_ID_COL);
				date = results.getString(PLAY_DATE_COL);
				profile = results.getString(PROFILE_COL);

				pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId, 'y', profile);
				pickList.add(pickOfTheDay);
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
		if(pickList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful");
			return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_PICK_OF_THE_DAY");
			return null;
		}
	}

	static boolean remove(Connection conn, String playDate)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;

		if(playDate == null)
			if (m_databaseType.equals(DB_SAPDB)) {
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY')";
			} else if (m_databaseType.equals(DB_MYSQL)) {
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y')";
			}
			
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info( "RBT::inside try block");
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
	
	static int removeOldEntries(Connection conn, String playDate)
	{
		logger.info("RBT::inside removeOldEntries");

		int count = -1;
		String query = null;
		Statement stmt = null;

		if(playDate == null)
			return -1 ;
		
		if (m_databaseType.equals(DB_SAPDB)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE TO_DATE("+ PLAY_DATE_COL + ",'DD/MM/YYYY') < " 
			+ sqlString(playDate)  ;
		} else if (m_databaseType.equals(DB_MYSQL)) {
			query = "DELETE FROM " + TABLE_NAME + " WHERE STR_TO_DATE("+ PLAY_DATE_COL + ",'%d/%m/%Y') < " 
			+ sqlString(playDate)  ;
		}

		logger.info("RBT::query "+query);

		try
		{
			logger.info( "RBT::inside try block");
			stmt = conn.createStatement();
			count = stmt.executeUpdate(query);
			//n = stmt.getUpdateCount();
		}
		catch(SQLException se)
		{
			logger.error("", se);
			return -1;
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
		return count;
	}

	static boolean remove(Connection conn, String playDate, String circleID, char prepaidYes, String profile, String language)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;

		if(playDate == null)
		{
			if (m_databaseType.equals(DB_SAPDB))
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR("+SAPDB_SYSDATE+", 'DD/MM/YYYY')";
			else if (m_databaseType.equals(DB_MYSQL))
				query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = DATE_FORMAT("+MYSQL_SYSDATE+", '%d/%m/%Y')";
		}
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'";

		if(circleID != null)
			query += " AND " + CIRCLE_ID_COL + " = '" + circleID + "'";
		
		query += " AND " + PREPAID_YES_COL + " = '" + prepaidYes + "'";
		
		if(profile != null)
			query += " AND " + PROFILE_COL + " = '" + profile + "'";
		
		if(language != null)
			query += " AND " + LANGUAGE_COL + " = '" + language + "'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info( "RBT::inside try block");
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
		return(n >= 0);
	}

	static boolean remove(Connection conn, int clipId, String circleId)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;

		if(circleId == null)
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipId;
		else
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + CLIP_ID_COL + " = " + clipId + " AND " + CIRCLE_ID_COL + " = '" + circleId + "'";

		logger.info("RBT::query "+query);

		try
		{
			logger.info( "RBT::inside try block");
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
	
	static PickOfTheDay [] getPickOfTheDayForTATAForAllCircle(Connection conn, String playDate){ 
	    
        logger.info("RBT::inside getPickOfTheDays"); 
        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 
        int categoryID = -1; 
        int clipID = -1; 
        String date = null; 
        String profile = null; 
        String circleId=null; 
        String prepaidYes=null; 
        PickOfTheDayImpl pickOfTheDay = null; 
        List pickList = new ArrayList(); 
        if(playDate == null) 
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = TO_CHAR(SYSDATE, 'DD/MM/YYYY') "; 
        else 
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PLAY_DATE_COL + " = " + "'" + playDate + "'" ; 

        logger.info("RBT::query "+query); 
        try { 
            logger.info("RBT::inside try block"); 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            while (results.next()) { 
                categoryID = results.getInt(CATEGORY_ID_COL); 
                clipID = results.getInt(CLIP_ID_COL); 
                date = results.getString(PLAY_DATE_COL); 
                profile = results.getString(PROFILE_COL); 
                circleId=results.getString(CIRCLE_ID_COL); 
                prepaidYes=results.getString(PREPAID_YES_COL); 
                if(prepaidYes!=null&& prepaidYes.length()>0){ 
                        prepaidYes=prepaidYes.trim(); 
                        if(prepaidYes.equalsIgnoreCase("y")){ 
                                pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId,'y', profile); 
                        } 
                        else{ 
                                pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId,'n', profile); 
                        } 
                        pickList.add(pickOfTheDay); 
                }else{ 
                        pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId,'y', profile); 
                        pickList.add(pickOfTheDay); 
                        pickOfTheDay = new PickOfTheDayImpl(categoryID, clipID, date, circleId,'n', profile); 
                        pickList.add(pickOfTheDay); 
                } 
            } 
        } 
        catch(SQLException se) { 
	        logger.error("", se); 
	        return null; 
        } 
        finally { 
            try { 
                    stmt.close(); 
            } 
            catch(Exception e) { 
                    logger.error("", e);
            } 
        } 
        if(pickList.size() > 0) { 
            logger.info("RBT::retrieving records from RBT_PICK_OF_THE_DAY successful"); 
            return (PickOfTheDay[])pickList.toArray(new PickOfTheDay[0]); 
        } 
        else { 
            logger.info("RBT::no records in RBT_PICK_OF_THE_DAY"); 
            return null; 
        }
	} 

}