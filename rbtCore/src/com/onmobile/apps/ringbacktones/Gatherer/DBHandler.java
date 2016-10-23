package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

/**
 * Created on Nov 20, 2004
 * @author shrihari
 */

public class DBHandler
{
	private static Logger logger = Logger.getLogger(DBHandler.class);
	
	RbtGenericCacheWrapper rbtGenericCache = null;
	RBTDBManager rbtDBManager = null;
	
    private final String _DATEFORMAT12 = "yyyyMMddhhmmss a";
    private final String _SQLDATEFORMAT12 = "yyyymmddhhmiss AM";
    private final String _TXTFILE_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DB_SAPDB="SAPDB";
    public static final String DB_MYSQL="MYSQL";
 
    private Hashtable m_db_incr_tables = null;
 
    private String m_folderName = null;
    private String[] m_prefixes;

    public DBHandler()
    {
    }

    public boolean init()
    {
        try
        {
            DriverManager.registerDriver(new com.sap.dbtech.jdbc.DriverSapDB());
        }
        catch (SQLException e)
        {
            logger.error("", e);
            return false;
        }
        return getParams();
    }

    private boolean getParams()
    {
    	rbtGenericCache = RbtGenericCacheWrapper.getInstance();
		rbtDBManager = RBTDBManager.getInstance();

    	List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
    	ArrayList<String> arl = new ArrayList<String>();
    	if (prefix != null && prefix.size() > 0)
    	{
    		for (int i = 0; i < prefix.size(); i++)
    		{
    			StringTokenizer stk = new StringTokenizer(prefix.get(i).getSitePrefix(), ",");
    			while (stk.hasMoreTokens())
    				arl.add(stk.nextToken());
    		}
    		m_prefixes = (String[]) arl.toArray(new String[0]);
    	}


    	String tmp = getParamAsString("GATHERER","DB_INCR_TABLES", null);
    	if (tmp != null && tmp.length() > 0)
    	{
    		m_db_incr_tables = new Hashtable();
    		StringTokenizer incrTokens = new StringTokenizer(tmp, ",");
    		while (incrTokens.hasMoreTokens())
    		{
    			String table_name = incrTokens.nextToken();
    			String query = getParamAsString("GATHERER", table_name + "_QUERY", null);
    			if (query != null)
    			{
    				m_db_incr_tables.put(table_name, Tools.findNReplace(query, "''", "'"));
    			}
    		}

    	}

    	return true;
    }
    private void collectDataFromTable(String TABLE_NAME)
    {
        String query = "SELECT * FROM " + TABLE_NAME;
		String reportFileName = null;

	   reportFileName = m_folderName + "/" + TABLE_NAME + ".txt";
       File reportfile = new File(reportFileName);
	   FileOutputStream fout = null;
		
		try
		{
			if (!reportfile.exists())
				reportfile.createNewFile();

			fout = new FileOutputStream(reportfile);
		}
		catch(Exception e)
		{
	
		}

        executeSingleQueryAndCreateFile(query, TABLE_NAME, fout);

		try
		{
			fout.close();
		}
		catch(Exception e)
		{
			
		}
    }

    private void CollectDataFromIncrTable(String TABLE_NAME, String query)
    {
        String _method = "CollectDataFromIncrTable";

       String reportFileName = null;
        reportFileName = m_folderName + "/" + TABLE_NAME + "_"+ getParamAsInt("GATHERER","DB_COLLECTION_DAYS", 5) + ".txt";

       File reportfile = new File(reportFileName);
	   FileOutputStream fout = null;
		
		try
		{
			if (!reportfile.exists())
				reportfile.createNewFile();

			fout = new FileOutputStream(reportfile);
		}
		catch(Exception e)
		{
	
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		Date end = cal.getTime();

		cal.add(Calendar.DATE, -getParamAsInt("GATHERER","DB_COLLECTION_DAYS", 5));
		Date start = cal.getTime();


		while(start.before(end))
		{
			 Tools.addToLogFile("start date==" +start.toString());
			String first = Tools.getFormattedDate(start, "yyyy-MM-dd HH");
			Tools.addToLogFile("first date==" + first);
			cal.add(Calendar.HOUR_OF_DAY, getParamAsInt("GATHERER", "INTERVAL_HOURS_INCR_QUERY", getParamAsInt("GATHERER","DB_COLLECTION_DAYS", 5)*24));
	
			start = cal.getTime();
			 Tools.addToLogFile("end date==" +start.toString());
			String second = Tools.getFormattedDate(start, "yyyy-MM-dd HH");
			Tools.addToLogFile("second date==" + second);
			query = Tools.findNReplaceAll(query, "FIRST", first);
		    query = Tools.findNReplaceAll(query, "SECOND", second);
	
			executeSingleQueryAndCreateFile(query, TABLE_NAME, fout);

		}

		try
		{
			fout.close();
		}
		catch(Exception e)
		{
			
		}
    }

    private void CollectFullDataFromIncrTable(String TABLE_NAME)
    {
        String reportFileName = m_folderName + "/" + TABLE_NAME + ".txt";
        File reportfile = new File(reportFileName);
        FileOutputStream fout = null;
        boolean firstLine = false;
        Statement stmt = null;
        ResultSet rs = null;

        Connection conn = null;

        try
        {
        	conn = rbtDBManager.getConnection();
            if(conn == null)
            	return;
        	
        	if (!reportfile.exists())
                reportfile.createNewFile();
            fout = new FileOutputStream(reportfile);
            
            for (int index = 0; index < m_prefixes.length; index++)
	        {
	            long subsStartRange = Long.parseLong(m_prefixes[index] + "000000");
	            subsStartRange--;//subtract one, because of the query.
	            long subsEndRange = Long.parseLong(m_prefixes[index] + "999999");
	            long subsStartBlock = subsStartRange;
	            long subsEndBlock = 0;
	            boolean end_of_querying = false;
	            while (!end_of_querying)
	            {
	                subsEndBlock = subsStartBlock + getParamAsInt("GATHERER", "SUBSCRIBERS_PER_QUERY", 1000);
	                if (subsEndBlock > subsEndRange)
	                    subsEndBlock = subsEndRange;
	                if (subsStartBlock == subsEndBlock)
	                {
	                    end_of_querying = true;
	                    continue;
	                }
	
	                String query = "select * from " + TABLE_NAME;
	                query += " where subscriber_id > '" + String.valueOf(subsStartBlock) + "' and subscriber_id <= '" 
	                + String.valueOf(subsEndBlock) + "'";
	                logger.info("going to execute query: " + query);
	
	                try
	                {
	                    if(!ResourceReader.getString("rbt", "DB_TYPE", "MYSQL").equals(DB_SAPDB))
						{
							stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
							stmt.setFetchSize(5000);
						}
						else
							stmt = conn.createStatement();
	                    rs = stmt.executeQuery(query);
	
	                    ResultSetMetaData rsmd = rs.getMetaData();
	                    int numberOfColumns = rsmd.getColumnCount();
	
	                    if (!firstLine)
	                    {
	                        StringBuffer sbFirstLine = new StringBuffer();
	                        for (int rsIndex = 1; rsIndex <= numberOfColumns; rsIndex++)
	                        {
	                            if (rsIndex < numberOfColumns)
	                                sbFirstLine.append(rsmd.getColumnName(rsIndex)
	                                        + ",");
	                            else
	                                sbFirstLine.append(rsmd.getColumnName(rsIndex));
	                        }
	                        sbFirstLine.append(System.getProperty("line.separator"));
	                        fout.write(sbFirstLine.toString().getBytes());
	
	                        firstLine = true;
	                    }
	                    processResultsetAndWritetoFile(rs, fout);
	                }
	                catch (Exception e)
	                {
	                    logger.error("", e);
	                }
	                subsStartBlock = subsEndBlock;
	            }
	        }
        }
        catch (Throwable e)
        {
        	logger.error("Exception before release connection", e);
        }
        finally
        {
            try
            {
                fout.close();
            }
            catch (Exception e)
            {
                logger.error("", e);
            }
            try
            {
            	rs.close();
            }
            catch (Exception e)
            {
                logger.error("Exception in closing resultset", e);
            }
            try
            {
            	stmt.close();
            }
            catch (Exception e)
            {
                logger.error("Exception in closing statement", e);
            }
            rbtDBManager.releaseConnection(conn);
        }
    }

    private void executeSingleQueryAndCreateFile(String query,
            String TABLE_NAME, FileOutputStream fout)
    {
        logger.info("going to execute query: " + query);
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try
        {
            conn = rbtDBManager.getConnection();
            if (conn == null)
                return;
            
            if(!ResourceReader.getString("rbt", "DB_TYPE", "MYSQL").equals(DB_SAPDB))
			{
				stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
				  java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(Integer.MIN_VALUE);
			}
			else
				stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            //write first line
            StringBuffer sbFirstLine = new StringBuffer();

            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();

            for (int rsIndex = 1; rsIndex <= numberOfColumns; rsIndex++)
            {
                if (rsIndex < numberOfColumns)
                    sbFirstLine.append(rsmd.getColumnName(rsIndex) + ",");
                else
                    sbFirstLine.append(rsmd.getColumnName(rsIndex));
            }
            sbFirstLine.append(System.getProperty("line.separator"));
            fout.write(sbFirstLine.toString().getBytes());

            logger.info("executed the query. getting the results");
            processResultsetAndWritetoFile(rs, fout);
        }
        catch (Throwable e)
        {
        	logger.error("Exception before release connection", e);
        }
        finally
        {
			try
            {
                rs.close();
            }
            catch (Exception e)
            {
                logger.error("Exception in closing resultset", e);
            }
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
                logger.error("Exception in closing statement", e);
            }
            rbtDBManager.releaseConnection(conn);
        }

    }

    private void processResultsetAndWritetoFile(ResultSet rs,
            FileOutputStream fout) throws Exception
    {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        while (rs.next())
        {
            StringBuffer sb = new StringBuffer();
            for (int rsIndex = 1; rsIndex <= numberOfColumns; rsIndex++)
            {
                int type = rsmd.getColumnType(rsIndex);
                String value = null;
                if (type == Types.TIME || type == Types.TIMESTAMP
                        || type == Types.DATE)
                {
                    java.util.Date dt = rs.getTimestamp(rsmd.getColumnName(rsIndex));
                    if (dt != null)
                        value = Tools.getFormattedDate(dt, _TXTFILE_DATEFORMAT);
                }
                else if (type == Types.INTEGER)
                {
                    int num = rs.getInt(rsmd.getColumnName(rsIndex));
                    value = "" + num;
                }
                else
                {
                    value = rs.getString(rsmd.getColumnName(rsIndex));
                }
                if (value != null && value.indexOf(",") != -1)
                    value = "'" + value + "'";

                if (rsIndex < numberOfColumns)
                    sb.append(value + ",");
                else
                    sb.append(value);
            }

            sb.append(System.getProperty("line.separator"));
            fout.write(sb.toString().getBytes());
            fout.flush();
            sb = null;
        }
    }

    public String getParamAsString(String type, String param, String defualtVal)
    {
    	try{
    		return rbtGenericCache.getParameter(type, param, defualtVal);
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defualtVal;
    	}
    }
    private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtGenericCache.getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +" returning defaultVal >"+defaultVal);
			return defaultVal;
		}
	}

}