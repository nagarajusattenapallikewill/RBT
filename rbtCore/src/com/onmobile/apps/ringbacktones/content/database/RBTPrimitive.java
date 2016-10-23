package com.onmobile.apps.ringbacktones.content.database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.ResourceReader;


public class RBTPrimitive
{

	private static Logger logger = Logger.getLogger(RBTPrimitive.class);
	
    protected static final String sqlTimeSpec = "YYYY/MM/DD HH24:MI:SS";
    protected static final String mySqlTimeSpec = "%Y/%m/%d %H:%i:%s";
    public static final String DB_SAPDB="SAPDB";
    public static final String DB_MYSQL="MYSQL";
    public static final String MYSQL_SYSDATE="SYSDATE()";
    public static final String SAPDB_SYSDATE="SYSDATE";
    
    protected static String sqlString(String s)
    {
        if (s != null)
        {
			StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('\'');
            int from = 0;
            int next;
            while ((next = s.indexOf('\'', from)) != -1)
            {
                stringBuilder.append(s.substring(from, next + 1));
                stringBuilder.append('\'');
                from = next + 1;

            }

            if (from < s.length())
                stringBuilder.append(s.substring(from));

            stringBuilder.append('\'');
            return stringBuilder.toString();
        }

        return "NULL";
    }

    protected static String sqlID(int n)
    {
        if (n > 0)
            return String.valueOf(n);
        return null;
    }

    protected static String sqlInt(int n)
    {
        if (n > 0)
            return String.valueOf(n);
        else
            return null;

    }

    protected static String sqlChar(int n, String valid)
    {
        if (n != 0 && valid.indexOf((char) n) != -1)
        {
            if (n == '\'')
                return "''''";
            return "'" + (char) n + "'";
        }
        return null;
    }

    protected static String sqlTime(java.util.Date date)
    {
    	DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	
        if (date != null)
            return toSQLDate(date, sqlTimeSpec, sqlTimeFormat);
        return null;
    }
    
    protected static String mySqlTime(java.util.Date date)
    {
    	DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	
        if (date != null)
            return toMySQLDate(date, mySqlTimeSpec, sqlTimeFormat);
        return null;
    }

    protected static String toSQLDate(java.util.Date date, String spec,
            DateFormat format)
    {
        return "TO_DATE('" + format.format(date) + "', '" + spec + "')";
    }
    
    protected static String toMySQLDate(java.util.Date date, String spec, DateFormat format)
    {
        return "DATE_FORMAT('" + format.format(date) + "', '" + spec + "')";
    }
    
    protected static String mySQLDateTime(java.util.Date date)
    {
    	DateFormat mySqlTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
        if(date != null)
        	return "TIMESTAMP('" + mySqlTimeFormat.format(date) + "')";
        else return null;
    }
    
    protected static String getStackTrace(Throwable ex)
    {
        StringWriter stringWriter = new StringWriter();
        String trace = "";
        if (ex instanceof Exception)
        {
            Exception exception = (Exception) ex;
            exception.printStackTrace(new PrintWriter(stringWriter));
            trace = stringWriter.toString();
            trace = trace.substring(0, trace.length() - 2);
            trace = System.getProperty("line.separator") + " \t" + trace;
        }
        return trace;
    }
    
    public  static String getDBSelectionString(){
		return ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
    }
    
    protected static String getNullForWhere(String str)
    {
    	if (str == null)
    		return " IS NULL ";
    	else 
    		return " = "+sqlString(str)+" ";  
    }
    
    protected static void closeStatementAndRS(Statement stmt, ResultSet rs) 
    {
    	try
		{
			if(rs !=null)
				rs.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing db resultset", e);
		}
		try
		{
			if(stmt !=null)
				stmt.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing db statement", e);
		}	
	}
	
	public static void addToQuery(StringBuilder query, String value, boolean firstParam) {
		if(!firstParam)
			query.append(", ");
		query.append(value);
	}

	public static Connection getConnection()
	{
		return RBTDBManager.getInstance().getConnection();
	}
	
	public static boolean releaseConnection(Connection conn)
	{
		return RBTDBManager.getInstance().releaseConnection(conn);
	}
	
	public static boolean releaseConnection(Connection conn, Statement stmt, ResultSet rs)
	{
		try
		{
			if(rs != null)
				rs.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing db resultset", e);
		}
		
		try
		{
			if(stmt != null)
				stmt.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing statement", e);
		}
		
		return RBTDBManager.getInstance().releaseConnection(conn);
	}
}