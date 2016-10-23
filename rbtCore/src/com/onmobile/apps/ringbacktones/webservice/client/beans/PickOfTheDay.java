/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;


/**
 * @author vinayasimha.patil
 *
 */
public class PickOfTheDay
{
	private String playDate = null;
	private int clipID;
	private String clipName;
	private int categoryID;
	private String circleID = null;
	private String userType = null;
	private String profile = null;
	private String language = null;

	/**
	 * 
	 */
	public PickOfTheDay()
	{

	}

	/**
	 * @param playDate
	 * @param clipID
	 * @param clipName
	 * @param categoryID
	 * @param circleID
	 * @param userType
	 * @param profile
	 */
	public PickOfTheDay(String playDate, int clipID, String clipName,
			int categoryID, String circleID, String userType, String profile)
	{
		this.playDate = playDate;
		this.clipID = clipID;
		this.clipName = clipName;
		this.categoryID = categoryID;
		this.circleID = circleID;
		this.userType = userType;
		this.profile = profile;
	}
	
	public PickOfTheDay(String playDate, int clipID, String clipName,
			int categoryID, String circleID, String userType, String profile, String language)
	{
		this.playDate = playDate;
		this.clipID = clipID;
		this.clipName = clipName;
		this.categoryID = categoryID;
		this.circleID = circleID;
		this.userType = userType;
		this.profile = profile;
		this.language = language;
	}

	/**
	 * @return the playDate
	 */
	public String getPlayDate()
	{
		return playDate;
	}

	/**
	 * @return the clipID
	 */
	public int getClipID()
	{
		return clipID;
	}

	/**
	 * @return the clipName
	 */
	public String getClipName()
	{
		return clipName;
	}

	/**
	 * @return the categoryID
	 */
	public int getCategoryID()
	{
		return categoryID;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @return the userType
	 */
	public String getUserType()
	{
		return userType;
	}

	/**
	 * @return the profile
	 */
	public String getProfile()
	{
		return profile;
	}

	/**
	 * @param playDate the playDate to set
	 */
	public void setPlayDate(String playDate)
	{
		this.playDate = playDate;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(int clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @param clipName the clipName to set
	 */
	public void setClipName(String clipName)
	{
		this.clipName = clipName;
	}

	/**
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(int categoryID)
	{
		this.categoryID = categoryID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile)
	{
		this.profile = profile;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryID;
		result = prime * result + ((circleID == null) ? 0 : circleID.hashCode());
		result = prime * result + clipID;
		result = prime * result + ((clipName == null) ? 0 : clipName.hashCode());
		result = prime * result + ((playDate == null) ? 0 : playDate.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((userType == null) ? 0 : userType.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
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
		if (!(obj instanceof PickOfTheDay))
			return false;
		PickOfTheDay other = (PickOfTheDay) obj;
		if (categoryID != other.categoryID)
			return false;
		if (circleID == null)
		{
			if (other.circleID != null)
				return false;
		}
		else if (!circleID.equals(other.circleID))
			return false;
		if (clipID != other.clipID)
			return false;
		if (clipName == null)
		{
			if (other.clipName != null)
				return false;
		}
		else if (!clipName.equals(other.clipName))
			return false;
		if (playDate == null)
		{
			if (other.playDate != null)
				return false;
		}
		else if (!playDate.equals(other.playDate))
			return false;
		if (profile == null)
		{
			if (other.profile != null)
				return false;
		}
		else if (!profile.equals(other.profile))
			return false;
		if (userType == null)
		{
			if (other.userType != null)
				return false;
		}
		else if (!userType.equals(other.userType))
			return false;
		else if(!language.equals(other.language))
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
		builder.append("PickOfTheDay[categoryID = ");
		builder.append(categoryID);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", clipID = ");
		builder.append(clipID);
		builder.append(", clipName = ");
		builder.append(clipName);
		builder.append(", playDate = ");
		builder.append(playDate);
		builder.append(", profile = ");
		builder.append(profile);
		builder.append(", userType = ");
		builder.append(userType);
		builder.append(", language = ");
		builder.append(language);
		builder.append("]");
		return builder.toString();
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}
}
