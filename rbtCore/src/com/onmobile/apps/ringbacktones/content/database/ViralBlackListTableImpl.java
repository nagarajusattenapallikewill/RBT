package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ViralBlackListTable;

public class ViralBlackListTableImpl extends RBTPrimitive implements
        ViralBlackListTable
{
    private static Logger logger = Logger.getLogger(ViralBlackListTableImpl.class);

    private static final String TABLE_NAME = "RBT_VIRAL_BLACKLIST_TABLE";
    private static final String SUBSCRIBER_ID_COL = "SUBSCRIBER_ID";
    private static final String START_TIME_COL = "START_TIME";
    private static final String END_TIME_COL = "END_TIME";
    private static final String BLACKLIST_TYPE_COL = "BLACKLIST_TYPE";

    private String m_subscriberID;
    private Date m_startTime;
    private Date m_endTime;
    private String m_subType;
    private static String m_databaseType=getDBSelectionString();
    
	private ViralBlackListTableImpl(String subscriberID, Date startTime,
            Date endTime, String subType)
    {
        m_subscriberID = subscriberID;
        m_startTime = startTime;
        m_endTime = endTime;
        m_subType = subType;
    }

    public String subID()
    {
        return m_subscriberID;
    }

    public Date startTime()
    {
        return m_startTime;
    }

    public Date endTime()
    {
        return m_endTime;
    }

    public String subType()
    {
        return m_subType;
    }

    static ViralBlackListTable insert(Connection conn, String subscriberID,
            Date startTime, Date endTime, String subType)
    {
        logger.info("RBT::inside insert");

        int id = -1;
        String query = null;
        Statement stmt = null;

        String startDate = null;
        String endTimeStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
        	startDate = "SYSDATE";
        	if (startTime != null)
        		startDate = sqlTime(startTime);
        	endTimeStr =  sqlTime(endTime);
        }
        else
        {
        	startDate = "SYSDATE()";
        	if (startTime != null)
        		startDate = mySQLDateTime(startTime);
        	endTimeStr =  mySQLDateTime(endTime);
        }

        ViralBlackListTableImpl viralBlackList = null;

        query = "INSERT INTO " + TABLE_NAME + " ( " + SUBSCRIBER_ID_COL;
        query += ", " + START_TIME_COL;
        query += ", " + END_TIME_COL;
        query += ", " + BLACKLIST_TYPE_COL;
        query += ")";

        query += " VALUES ( " + "'" + subscriberID + "'";
        query += ", " + startDate;
        query += ", " + endTimeStr;
        query += ", '" + subType + "'";
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
            logger.info("RBT::insertion to RBT_VIRAL_BLACKLIST_TABLE table successful");
            viralBlackList = new ViralBlackListTableImpl(subscriberID,
                    startTime, endTime, subType);
            return viralBlackList;
        }
        else
        {
            logger.info("RBT::insertion to RBT_VIRAL_BLACKLIST_TABLE table failed");
            return null;
        }
    }

    static boolean update(Connection conn, String subscriberID, Date startTime,
            Date endTime, String subType)
    {
        logger.info("RBT::inside update");

        int n = -1;
        String query = null;
        Statement stmt = null;

        String startDate = null;
        String endTimeStr = null;
        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        {
        	startDate = "SYSDATE";
        	if (startTime != null)
        		startDate = sqlTime(startTime);
        	endTimeStr =  sqlTime(endTime);
        }
        else
        {
        	startDate = "SYSDATE()";
        	if (startTime != null)
        		startDate = mySQLDateTime(startTime);
        	endTimeStr =  mySQLDateTime(endTime);
        }
        
        query = "UPDATE " + TABLE_NAME + " SET " + START_TIME_COL + " = "
                + startDate + ", " + END_TIME_COL + " = " + endTimeStr
                + " WHERE " + SUBSCRIBER_ID_COL + " = '" + subscriberID
                + "' AND " + BLACKLIST_TYPE_COL + " = '" + subType + "'";

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

    static ViralBlackListTable getViralBlackList(Connection conn, String subID,
            String subType)
    {
        logger.info("RBT::inside getViralBlackList");

        String query = null;
        Statement stmt = null;
        ResultSet results = null;

        String subscriberID = null;
        Date startTime = null;
        Date endTime = null;
        String subscriberType = null;

        ViralBlackListTableImpl viralBlackList = null;

        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
                + " = '" + subID + "' AND " + BLACKLIST_TYPE_COL + " = '"
                + subType + "' AND " + START_TIME_COL + " <= SYSDATE AND "
                + END_TIME_COL + " >= SYSDATE ";
        else
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
            + " = '" + subID + "' AND " + BLACKLIST_TYPE_COL + " = '"
            + subType + "' AND " + START_TIME_COL + " <= SYSDATE() AND "
            + END_TIME_COL + " >= SYSDATE() ";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = stmt.executeQuery(query);
            while (results.next())
            {
                subscriberID = results.getString(SUBSCRIBER_ID_COL);
                startTime = results.getTimestamp(START_TIME_COL);
                endTime = results.getTimestamp(END_TIME_COL);
                subscriberType = results.getString(BLACKLIST_TYPE_COL);

                viralBlackList = new ViralBlackListTableImpl(subscriberID,
                        startTime, endTime, subscriberType);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
        }
        finally
        {
//            try
//            {
//                stmt.close();
//            }
//            catch (Exception e)
//            {
//            	logger.error("", e);
//            }
        	closeStatementAndRS(stmt, results);
        }
        return viralBlackList;
    }

    static ViralBlackListTable[] getActiveViralBlackLists(Connection conn)
    {
        logger.info("RBT::inside getActiveViralBlackLists");

        String query = null;
        Statement stmt = null;
        ResultSet results = null;

        String subscriberID = null;
        Date startTime = null;
        Date endTime = null;
        String subType = null;

        ViralBlackListTableImpl viralBlackList = null;
        List viralBlackListList = new ArrayList();

        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_TIME_COL
                + " <= SYSDATE AND " + END_TIME_COL + " >= SYSDATE ";
        else
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_TIME_COL
            + " <= SYSDATE() AND " + END_TIME_COL + " >= SYSDATE() ";

        logger.info("RBT::query "
                + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = stmt.executeQuery(query);
            while (results.next())
            {
                subscriberID = results.getString(SUBSCRIBER_ID_COL);
                startTime = results.getTimestamp(START_TIME_COL);
                endTime = results.getTimestamp(END_TIME_COL);
                subType = results.getString(BLACKLIST_TYPE_COL);

                viralBlackList = new ViralBlackListTableImpl(subscriberID,
                        startTime, endTime, subType);
                viralBlackListList.add(viralBlackList);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
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
        if (viralBlackListList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_VIRAL_BLACKLIST_TABLE successful");
            return (ViralBlackListTable[]) viralBlackListList
                    .toArray(new ViralBlackListTable[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_VIRAL_BLACKLIST_TABLE");
            return null;
        }
    }

    static ViralBlackListTable[] getAllViralBlackLists(Connection conn)
    {
        logger.info("RBT::inside getAllViralBlackLists");

        String query = null;
        Statement stmt = null;
        ResultSet results = null;

        String subscriberID = null;
        Date startTime = null;
        Date endTime = null;
        String subType = null;

        ViralBlackListTableImpl viralBlackList = null;
        List viralBlackListList = new ArrayList();

        if(m_databaseType.equalsIgnoreCase(DB_SAPDB))
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_TIME_COL
                + " <= SYSDATE AND " + END_TIME_COL + " >= SYSDATE ";
        else
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + START_TIME_COL
            + " <= SYSDATE() AND " + END_TIME_COL + " >= SYSDATE() ";

        logger.info("RBT::query "
                        + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = stmt.executeQuery(query);
            while (results.next())
            {
                subscriberID = results.getString(SUBSCRIBER_ID_COL);
                startTime = results.getTimestamp(START_TIME_COL);
                endTime = results.getTimestamp(END_TIME_COL);
                subType = results.getString(BLACKLIST_TYPE_COL);

                viralBlackList = new ViralBlackListTableImpl(subscriberID,
                        startTime, endTime, subType);
                viralBlackListList.add(viralBlackList);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
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
        if (viralBlackListList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_VIRAL_BLACKLIST_TABLE successful");
            return (ViralBlackListTable[]) viralBlackListList
                    .toArray(new ViralBlackListTable[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_VIRAL_BLACKLIST_TABLE");
            return null;
        }
    }

    static boolean remove(Connection conn, String subscriberID, String subType)
    {
        logger.info("RBT::inside remove");

        int n = -1;
        String query = null;
        Statement stmt = null;

        query = "DELETE FROM " + TABLE_NAME + " WHERE " + SUBSCRIBER_ID_COL
                + " = '" + subscriberID + "' AND " + BLACKLIST_TYPE_COL
                + " = '" + subType + "'";

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
}