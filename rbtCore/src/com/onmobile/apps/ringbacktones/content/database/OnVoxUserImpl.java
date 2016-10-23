/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.OnVoxUser;

/**
 * @author vinayasimha.patil
 *
 */
public class OnVoxUserImpl implements OnVoxUser
{
	private static Logger logger = Logger.getLogger(OnVoxUserImpl.class);
	
	private static final String TABLE_NAME = "ONVOX_USER";
	private static final String USER_NAME = "USER_NAME";
	private static final String PASSWORD = "PASSWD";
	private static final String USER_TYPE = "USER_TYPE";
	
	String userName = null;
	String password = null;
	int userType;
	
	public String getUserName() 
	{
		return userName;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	public int getUserType() 
	{
		return userType;
	}
	
	private OnVoxUserImpl(String userName, String password, int userType)
	{
		this.userName = userName;
		this.password = password;
		this.userType = userType;
	}

	public static OnVoxUser getOnVoxUser(Connection conn, String userName)
	{
		logger.info("RBT::inside getOnVoxUser");
		
		String password = null;
		int userType;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		OnVoxUser onVoxUser = null;
		
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_NAME + " ='" + userName + "'";
		
		logger.info("RBT:: query -" + sql);
		
		try
		{
			logger.info("RBT:: inside try");
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			if(rs.next())
			{
				userName = rs.getString(USER_NAME);
				password = rs.getString(PASSWORD);
				userType = rs.getInt(USER_TYPE);
				
				onVoxUser = new OnVoxUserImpl(userName, password, userType);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			//closing result set
			try
			{
				if(rs != null)
					rs.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			//closing statement
			try
			{
				if(stmt != null)
					stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(onVoxUser != null)
		{
			logger.info("RBT:: retrieving records from ONVOX_USER successful");
			return onVoxUser;
		}
		return null;
	}
}
