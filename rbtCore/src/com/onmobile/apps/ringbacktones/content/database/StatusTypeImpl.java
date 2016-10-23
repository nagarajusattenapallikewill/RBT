package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.StatusType;

public class StatusTypeImpl extends RBTPrimitive implements StatusType
{
	private static Logger logger = Logger.getLogger(StatusTypeImpl.class);
	
    private static final String TABLE_NAME = "RBT_TYPE_CODE";
    private static final String STATUS_CODE_COL = "STATUS_CODE";
    private static final String STATUS_DESC_COL = "STATUS_DESC";
    private static final String SHOW_ON_GUI_COL = "SHOW_ON_GUI";
	private static final String SHOW_ON_VUI_COL = "SHOW_ON_VUI";

    private int m_code;
    private String m_desc;
	private String m_gui;
	private String m_vui;
	private static String m_databaseType=getDBSelectionString();

	private StatusTypeImpl(int code, String desc, String gui, String vui)
	{
		m_code = code;
        m_desc = desc;
        m_gui = gui;
        m_vui = vui;
	}
	
	public int code()
    {
        return m_code;
    }
	
	public String desc()
    {
        return m_desc;
    }
	
	public boolean showGUI()
	{
		if(m_gui != null)
			return m_gui.equalsIgnoreCase("y");
		else
			logger.info("RBT:: gui column is null" +m_code);
		
		return false;
	}

	public boolean showVUI()
	{
	    if(m_vui != null)
			return m_vui.equalsIgnoreCase("y");
		else
			logger.info("RBT:: vui column is null" +m_code);
		
		return false;
	}

	static StatusType insert(Connection conn, int code, String desc, String gui, String vui)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		StatusTypeImpl statusType = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + STATUS_CODE_COL;
		query += ", " + STATUS_DESC_COL;
		query += ", " + SHOW_ON_GUI_COL;
		query += ", " + SHOW_ON_VUI_COL;
		query += ")";

		query += " VALUES ( " + code;
		query += ", " + sqlString(desc);
		query += ", " + sqlString(gui);
		query += ", " + sqlString(vui);
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
            logger.info("RBT::insertion to RBT_TYPE_CODE table successful");
            statusType = new StatusTypeImpl(code, desc, gui, vui);
            return statusType;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_TYPE_CODE table failed");
            return null;
        }
    }
		
    static boolean update(Connection conn, int code, String desc, String gui, String vui)   
    {
        logger.info("RBT::inside update");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 STATUS_DESC_COL + " = " + sqlString(desc) + ", " +
				 SHOW_ON_GUI_COL + " = " + sqlString(gui) + ", " +
				 SHOW_ON_VUI_COL + " = " + sqlString(vui) + 
				" WHERE " + STATUS_CODE_COL + " = " + code;
		
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
	
	static StatusType [] getStatusTypes(Connection conn)
    {
        logger.info("RBT::inside getStatusTypes");
        
		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int code = -1;
		String desc = null;
		String gui = null;
		String vui = null;

		StatusTypeImpl statusType = null;
		List typeList = new ArrayList();
		
		query = "SELECT * FROM " + TABLE_NAME;
		
		logger.info("RBT::query "+query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						code = results.getInt(STATUS_CODE_COL);
						desc = results.getString(STATUS_DESC_COL);
						gui = results.getString(SHOW_ON_GUI_COL);
						vui = results.getString(SHOW_ON_VUI_COL);
		
						statusType = new StatusTypeImpl(code, desc, gui, vui);
						typeList.add(statusType);
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
        if(typeList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_TYPE_CODE successful");
            return (StatusType[])typeList.toArray(new StatusType[0]);
        } 
		else
        {
            logger.info("RBT::no records in RBT_TYPE_CODE");
            return null;
        }
    }

	static StatusType getStatusType(Connection conn, int code)
    {
        logger.info("RBT::inside getStatusType");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String desc = null;
		String gui = null;
		String vui = null;
		
		StatusTypeImpl statusType = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_CODE_COL + " = " + code;
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						desc = results.getString(STATUS_DESC_COL);
						gui = results.getString(SHOW_ON_GUI_COL);
						vui = results.getString(SHOW_ON_VUI_COL);

						statusType = new StatusTypeImpl(code, desc, gui, vui);
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
        return statusType;
    }
	
	static boolean remove(Connection conn, int code)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + STATUS_CODE_COL + " = " + code;
		
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