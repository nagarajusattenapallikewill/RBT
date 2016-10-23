package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * ParametersBean used by hibernate to persist into the RBT_SITE_PREFIX table.
 * 
 * @author bikash.panda
 */
public class SitePrefix implements Serializable, Cloneable
{
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	String circleID;
	String siteName;
	String sitePrefix;
	String siteUrl;
	String accessAllowed;
	String supportedLanguage;
	String playerUrl;
	String playerUncharged;

	/**
	 * 
	 */
	public SitePrefix()
	{

	}

	/**
	 * @param circleID
	 * @param siteName
	 * @param sitePrefix
	 * @param siteUrl
	 * @param accessAllowed
	 * @param supportedLanguage
	 * @param playerUrl
	 * @param playerUncharged
	 */
	public SitePrefix(String circleID, String siteName, String sitePrefix,
			String siteUrl, String accessAllowed, String supportedLanguage,
			String playerUrl, String playerUncharged)
	{
		this.circleID = circleID;
		this.siteName = siteName;
		this.sitePrefix = sitePrefix;
		this.siteUrl = siteUrl;
		this.accessAllowed = accessAllowed;
		this.supportedLanguage = supportedLanguage;
		this.playerUrl = playerUrl;
		this.playerUncharged = playerUncharged;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the siteName
	 */
	public String getSiteName()
	{
		return siteName;
	}

	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName)
	{
		this.siteName = siteName;
	}

	/**
	 * @return the sitePrefix
	 */
	public String getSitePrefix()
	{
		return sitePrefix;
	}

	/**
	 * @param sitePrefix the sitePrefix to set
	 */
	public void setSitePrefix(String sitePrefix)
	{
		this.sitePrefix = sitePrefix;
	}

	/**
	 * @return the siteUrl
	 */
	public String getSiteUrl()
	{
		return siteUrl;
	}

	/**
	 * @param siteUrl the siteUrl to set
	 */
	public void setSiteUrl(String siteUrl)
	{
		this.siteUrl = siteUrl;
	}

	/**
	 * @return the accessAllowed
	 */
	public String getAccessAllowed()
	{
		return accessAllowed;
	}

	/**
	 * @param accessAllowed the accessAllowed to set
	 */
	public void setAccessAllowed(String accessAllowed)
	{
		this.accessAllowed = accessAllowed;
	}

	/**
	 * @return the supportedLanguage
	 */
	public String getSupportedLanguage()
	{
		return supportedLanguage;
	}

	/**
	 * @param supportedLanguage the supportedLanguage to set
	 */
	public void setSupportedLanguage(String supportedLanguage)
	{
		this.supportedLanguage = supportedLanguage;
	}

	/**
	 * @return the playerUrl
	 */
	public String getPlayerUrl()
	{
		return playerUrl;
	}

	/**
	 * @param playerUrl the playerUrl to set
	 */
	public void setPlayerUrl(String playerUrl)
	{
		this.playerUrl = playerUrl;
	}

	/**
	 * @return the playerUncharged
	 */
	public String getPlayerUncharged()
	{
		return playerUncharged;
	}

	/**
	 * @param playerUncharged the playerUncharged to set
	 */
	public void setPlayerUncharged(String playerUncharged)
	{
		this.playerUncharged = playerUncharged;
	}

	public String getSiteLanguage()
	{
		String supportedLanguages = getSupportedLanguage();
		if (supportedLanguages != null)
		{
			String[] languages = supportedLanguages.split(",");
			return languages[0];
		}

		return null;
	}

	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	public boolean playUncharged(boolean isPrepaid)
	{
		if (isPrepaid)
			return (playerUncharged.equalsIgnoreCase("p") || playerUncharged.equalsIgnoreCase("y"));
		else
			return (playerUncharged.equalsIgnoreCase("b") || playerUncharged.equalsIgnoreCase("y"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SitePrefix clone() throws CloneNotSupportedException
	{
		return (SitePrefix) super.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SitePrefix[accessAllowed = ");
		builder.append(accessAllowed);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", playerUncharged = ");
		builder.append(playerUncharged);
		builder.append(", playerUrl = ");
		builder.append(playerUrl);
		builder.append(", siteName = ");
		builder.append(siteName);
		builder.append(", sitePrefix = ");
		builder.append(sitePrefix);
		builder.append(", siteUrl = ");
		builder.append(siteUrl);
		builder.append(", supportedLanguage = ");
		builder.append(supportedLanguage);
		builder.append("]");
		return builder.toString();
	}
}
