/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTLoginUser
{
	private String userID = null;
	private String password = null;
	private String subscriberID = null;
	private String type = null;
	private HashMap<String, String> userInfo = null;
	private Date creationTime = null;
	private int passwordExipryLeftDays = -1;
	private boolean passwordExpired = false;
	private Boolean newUser = null; 

	public Boolean getNewUser() {
		return newUser;
	}

	public void setNewUser(Boolean newUser) {
		this.newUser = newUser;
	}

	/**
	 * 
	 */
	public RBTLoginUser()
	{

	}

	/**
	 * @param userID
	 * @param password
	 * @param subscriberID
	 * @param type
	 * @param userInfo
	 */
	public RBTLoginUser(String userID, String password, String subscriberID,
			String type, HashMap<String, String> userInfo)
	{
		this.userID = userID;
		this.password = password;
		this.subscriberID = subscriberID;
		this.type = type;
		this.userInfo = userInfo;
	}

	/**
	 * @return the userID
	 */
	public String getUserID()
	{
		return userID;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the userInfo
	 */
	public HashMap<String, String> getUserInfo()
	{
		return userInfo;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID)
	{
		this.userID = userID;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(HashMap<String, String> userInfo)
	{
		this.userInfo = userInfo;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime()
	{
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}

	
	public int getPasswordExipryLeftDays() {
		return passwordExipryLeftDays;
	}

	public void setPasswordExipryLeftDays(int passwordExipryLeftDays) {
		this.passwordExipryLeftDays = passwordExipryLeftDays;
	}

	public boolean isPasswordExpired() {
		return passwordExpired;
	}

	public void setPasswordExpired(boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
		result = prime * result + ((creationTime == null) ? 0 : creationTime.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RBTLoginUser))
			return false;
		RBTLoginUser other = (RBTLoginUser) obj;
		if (password == null)
		{
			if (other.password != null)
				return false;
		}
		else if (!password.equals(other.password))
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		if (userID == null)
		{
			if (other.userID != null)
				return false;
		}
		else if (!userID.equals(other.userID))
			return false;
		if (userInfo == null)
		{
			if (other.userInfo != null)
				return false;
		}
		else if (!userInfo.equals(other.userInfo))
			return false;
		if (creationTime == null)
		{
			if (other.creationTime != null)
				return false;
		}
		else if (!creationTime.equals(other.creationTime))
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RBTLoginUser[password = ");
		builder.append(password);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", userID = ");
		builder.append(userID);
		builder.append(", userInfo = ");
		builder.append(userInfo);
		builder.append(", creationTime = ");
		builder.append(creationTime);
		builder.append(", passwordExipryLeftDays = ");
		builder.append(passwordExipryLeftDays);
		builder.append(", passwordExpired = ");
		builder.append(passwordExpired);
		builder.append("]");
		return builder.toString();
	}
}
