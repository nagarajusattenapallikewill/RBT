package com.onmobile.apps.ringbacktones.common;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.OnVoxUser;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class GUIUser 
{
	private static Logger logger = Logger.getLogger(GUIUser.class);
	
	private String sessionID = null;
	
	private String userID = null;
	private String password = null;
	private int userType;
	
	private GUIUser(String userID, String password, int userType)
	{
		this.userID = userID;
		this.password = password;
		this.userType = userType;
	}
	
	public String getSessionID() 
	{
		return sessionID;
	}
	
	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}
	
	public String getUserID() 
	{
		return userID;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	public int getUserType()
	{
		return userType;
	}
	
	public static GUIUser isValidUser(String userID, String password)
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		logger.info("RBT:: initilized dbmanager");
		
		OnVoxUser onVoxUser = rbtDBManager.getOnVoxUser(userID);
		if(onVoxUser == null)
		{
			return null;
		}
		else
		{
			if(password.equals(onVoxUser.getPassword()))
				return(new GUIUser(userID, password, onVoxUser.getUserType()));
			else
				return null;
		}			
	}
	
	public boolean isValidSession(String sessionID)
	{
		if(getSessionID() == null)
		{
			setSessionID(sessionID);
			return true;
		}
		else
		{
			if(getSessionID().equals(sessionID))
				return true;
			else
				return false;
		}
	}
}
