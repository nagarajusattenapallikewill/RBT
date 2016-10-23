package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTLogin;

public class RBTLoginImpl extends RBTPrimitive implements RBTLogin
{
	private static Logger logger = Logger.getLogger(RBTLoginImpl.class);
	
    private static final String TABLE_NAME = "ONVOX_USER";
	private static final String USER_NAME_COL = "USER_NAME";
    private static final String PASSWD_COL = "PASSWD";
    private static final String ASK_PASSWD_COL="ASK_PASSWD";
    private static final String USER_TYPE_COL = "USER_TYPE";
    private static final String MENU_ORDER_COL="MENU_ORDER";

    private String m_askPasswd=null;
    private String m_userName=null;
    private String m_passwd=null;
	private String m_userType=null;
	private String[] m_menuOrder=null;
	private static String m_databaseType=getDBSelectionString();
	
	private RBTLoginImpl(String user, String passwd, String type)
	{
		m_userName = user;
        m_passwd = passwd;
        m_userType = type;
	}
	private RBTLoginImpl(String user, String passwd, String type,String menuoeder,String askPasswd)
	{
		m_userName = user;
        m_passwd = passwd;
        m_userType = type;
        m_askPasswd=askPasswd;
        ArrayList menuOrder=new ArrayList();
        
        StringTokenizer st=new StringTokenizer(menuoeder,",");
        while(st.hasMoreElements()){
        	menuOrder.add(st.nextToken());
        }
        m_menuOrder=(String[])menuOrder.toArray(new String[0]);
	}
	public String askPassword() {
		// TODO Auto-generated method stub
		return m_askPasswd;
	}
	public String user()
    {
        return m_userName;
    }
	
	public String[] menuOrder()
	{
		return m_menuOrder;
	}
	
	public String pwd()
	{
		return m_passwd;
	}

	public String userType()
	{
		return m_userType;
	}

	static RBTLogin insert(Connection conn, String user, String passwd, String type)
    {
        logger.info("RBT::inside insert");
   
		int id = -1;
		String query = null;
		Statement stmt = null;

		RBTLoginImpl logins = null;

		query = "INSERT INTO " + TABLE_NAME + " ( " + USER_NAME_COL;
		query += ", " + PASSWD_COL;
		query += ", " + USER_TYPE_COL;
		query += ")";

		query += " VALUES ( " + sqlString(user);
		query += ", " + sqlString(passwd);
		query += ", " + sqlString(type);
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
            logger.info("RBT::insertion to RBT_SITE_PREFIX table successful");
            logins = new RBTLoginImpl(user, passwd, type);
            return logins;
        } 
		else
        {
		    logger.info("RBT::insertion to RBT_SITE_PREFIX table failed");
            return null;
        }
    }

    
	static boolean update(Connection conn, String user, String passwd, String type)
    {
        logger.info("RBT::inside update");
        
		int n = -1;
		String query = null;
		Statement stmt = null;

		query = "UPDATE " + TABLE_NAME + " SET " +
				 PASSWD_COL + " = " + sqlString(passwd) + ", " +
				 USER_TYPE_COL + " = " + sqlString(type) + 
				" WHERE " + USER_NAME_COL + " = " + sqlString(user);
		
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
    
	static RBTLogin[] checkGUIPwd(Connection conn,String strUser,String strPasswd) {
		logger.info("RBT::inside getURL");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String user = null;
		String passwd = null;
		String askPasswd=null;
		int type =0;
		String temp1=null;
		boolean check=false;
		String strMenuOrder=null;
		
		 RBTLoginImpl logins = null; 
	     List loginList = new ArrayList(); 
		//query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" + PASSWD_COL + ") = '" + C + "' AND LOWER(" + USER_NAME_COL + ") = '" + strUser + "' AND " + USER_TYPE_COL +" IS NOT NULL";
		query = "SELECT * FROM  RBT_GUI_PWD  WHERE " + USER_NAME_COL + " = '" + strUser + "' AND " + USER_TYPE_COL +" IS NOT NULL";
		int userType=1;
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				user = results.getString(USER_NAME_COL);
				passwd = results.getString(PASSWD_COL);
				type = results.getInt(USER_TYPE_COL);
				askPasswd=results.getString(ASK_PASSWD_COL);
				strMenuOrder=results.getString(MENU_ORDER_COL);
				if(type==userType){
				if(passwd.equals(strPasswd)){
					check=true;
				}
				}
				temp1=(""+type).trim();
				if(type==1)
					logins = new RBTLoginImpl(user, passwd, temp1,strMenuOrder,askPasswd);
				else
					logins = new RBTLoginImpl(user, passwd, temp1);
				
				loginList.add(logins);
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
		if (loginList.size() > 0) 
        { 
            logger.info("RBT::retrieving records from RBT_GUI_PWD successful"); 
            if(check==true){
            return (RBTLogin []) loginList.toArray(new RBTLogin[0]); 
            }
            else{
            	return null;
            }
        } 
        else 
        { 
            logger.info("RBT::no records in RBT_GUI_PWD"); 
            return null; 
        } 
	}
	
	static RBTLogin getUser(Connection conn, String strUser, String strPasswd)
    {
        logger.info("RBT::inside getURL");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String user = null;
		String passwd = null;
		String type = null;
		
		RBTLoginImpl logins = null;

		//query = "SELECT * FROM " + TABLE_NAME + " WHERE LOWER(" + PASSWD_COL + ") = '" + strPasswd + "' AND LOWER(" + USER_NAME_COL + ") = '" + strUser + "' AND " + USER_TYPE_COL +" IS NOT NULL";
		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PASSWD_COL + " = '" + strPasswd + "' AND LOWER(" + USER_NAME_COL + ") = '" + strUser + "' AND " + USER_TYPE_COL +" IS NOT NULL";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
			while (results.next())
			{
				user = results.getString(USER_NAME_COL);
				passwd = results.getString(PASSWD_COL);
				type = results.getString(USER_TYPE_COL);
				
				logins = new RBTLoginImpl(user, passwd, type);
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
        return logins;
    }
	
	static RBTLogin[] getAllUsers(Connection conn) 
    { 
        logger.info("RBT::inside getAllUsers"); 
 
        String query = null; 
        Statement stmt = null; 
        ResultSet results = null; 
        
        String user = null; 
        String passwd = null; 
        String type = null; 
 
        RBTLoginImpl logins = null; 
        List loginList = new ArrayList(); 
 
        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_TYPE_COL +" IS NOT NULL"; 
        
        logger.info("RBT::query "+query); 
 
        try 
        { 
            logger.info("RBT::inside try block"); 
            stmt = conn.createStatement(); 
            results = stmt.executeQuery(query); 
            while (results.next()) 
            { 
            	user = results.getString(USER_NAME_COL); 
            	passwd = results.getString(PASSWD_COL); 
            	type = results.getString(USER_TYPE_COL); 
 
            	logins = new RBTLoginImpl(user, passwd, type); 
            	loginList.add(logins); 
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
        if (loginList.size() > 0) 
        { 
            logger.info("RBT::retrieving records from ONVOX_USER successful"); 
            return (RBTLogin []) loginList.toArray(new RBTLogin[0]); 
        } 
        else 
        { 
            logger.info("RBT::no records in ONVOX_USER"); 
            return null; 
        } 
    } 

	
	static boolean remove(Connection conn, String user)
	{
		logger.info("RBT::inside remove");

		int n = -1;
		String query = null;
		Statement stmt = null;
		
		query = "DELETE FROM " + TABLE_NAME + " WHERE " + USER_NAME_COL + " = " + "'" + user + "'";
		
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