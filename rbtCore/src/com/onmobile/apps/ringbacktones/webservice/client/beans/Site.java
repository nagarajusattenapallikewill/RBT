/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class Site
{
	private String siteName = null;
	private String[] sitePrefixes = null;
	private String siteURL = null;
	private String circleID = null;
	private boolean accessAllowed;
	private String[] supportedLanguages = null;
	private String playerURL = null;
	private String playUnchargedFor;

	/**
	 * 
	 */
	public Site()
	{

	}

	/**
	 * @param siteName
	 * @param sitePrefixes
	 * @param siteURL
	 * @param circleID
	 * @param accessAllowed
	 * @param supportedLanguages
	 * @param playerURL
	 * @param playUnchargedFor
	 */
	public Site(String siteName, String[] sitePrefixes, String siteURL,
			String circleID, boolean accessAllowed,
			String[] supportedLanguages, String playerURL,
			String playUnchargedFor)
	{
		this.siteName = siteName;
		this.sitePrefixes = sitePrefixes;
		this.siteURL = siteURL;
		this.circleID = circleID;
		this.accessAllowed = accessAllowed;
		this.supportedLanguages = supportedLanguages;
		this.playerURL = playerURL;
		this.playUnchargedFor = playUnchargedFor;
	}

	/**
	 * @return the siteName
	 */
	public String getSiteName()
	{
		return siteName;
	}

	/**
	 * @return the sitePrefixes
	 */
	public String[] getSitePrefixes()
	{
		return sitePrefixes;
	}

	/**
	 * @return the siteURL
	 */
	public String getSiteURL()
	{
		return siteURL;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @return the accessAllowed
	 */
	public boolean isAccessAllowed()
	{
		return accessAllowed;
	}

	/**
	 * @return the supportedLanguages
	 */
	public String[] getSupportedLanguages()
	{
		return supportedLanguages;
	}

	/**
	 * @return the playerURL
	 */
	public String getPlayerURL()
	{
		return playerURL;
	}

	/**
	 * @return the playUnchargedFor
	 */
	public String getPlayUnchargedFor()
	{
		return playUnchargedFor;
	}

	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName)
	{
		this.siteName = siteName;
	}

	/**
	 * @param sitePrefixes the sitePrefixes to set
	 */
	public void setSitePrefixes(String[] sitePrefixes)
	{
		this.sitePrefixes = sitePrefixes;
	}

	/**
	 * @param siteURL the siteURL to set
	 */
	public void setSiteURL(String siteURL)
	{
		this.siteURL = siteURL;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @param accessAllowed the accessAllowed to set
	 */
	public void setAccessAllowed(boolean accessAllowed)
	{
		this.accessAllowed = accessAllowed;
	}

	/**
	 * @param supportedLanguages the supportedLanguages to set
	 */
	public void setSupportedLanguages(String[] supportedLanguages)
	{
		this.supportedLanguages = supportedLanguages;
	}

	/**
	 * @param playerURL the playerURL to set
	 */
	public void setPlayerURL(String playerURL)
	{
		this.playerURL = playerURL;
	}

	/**
	 * @param playUnchargedFor the playUnchargedFor to set
	 */
	public void setPlayUnchargedFor(String playUnchargedFor)
	{
		this.playUnchargedFor = playUnchargedFor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (accessAllowed ? 1231 : 1237);
		result = prime * result + ((circleID == null) ? 0 : circleID.hashCode());
		result = prime * result + ((playUnchargedFor == null) ? 0 : playUnchargedFor.hashCode());
		result = prime * result + ((playerURL == null) ? 0 : playerURL.hashCode());
		result = prime * result + ((siteName == null) ? 0 : siteName.hashCode());
		result = prime * result + Arrays.hashCode(sitePrefixes);
		result = prime * result + ((siteURL == null) ? 0 : siteURL.hashCode());
		result = prime * result + Arrays.hashCode(supportedLanguages);
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
		if (!(obj instanceof Site))
			return false;
		Site other = (Site) obj;
		if (accessAllowed != other.accessAllowed)
			return false;
		if (circleID == null)
		{
			if (other.circleID != null)
				return false;
		}
		else if (!circleID.equals(other.circleID))
			return false;
		if (playUnchargedFor == null)
		{
			if (other.playUnchargedFor != null)
				return false;
		}
		else if (!playUnchargedFor.equals(other.playUnchargedFor))
			return false;
		if (playerURL == null)
		{
			if (other.playerURL != null)
				return false;
		}
		else if (!playerURL.equals(other.playerURL))
			return false;
		if (siteName == null)
		{
			if (other.siteName != null)
				return false;
		}
		else if (!siteName.equals(other.siteName))
			return false;
		if (!Arrays.equals(sitePrefixes, other.sitePrefixes))
			return false;
		if (siteURL == null)
		{
			if (other.siteURL != null)
				return false;
		}
		else if (!siteURL.equals(other.siteURL))
			return false;
		if (!Arrays.equals(supportedLanguages, other.supportedLanguages))
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
		builder.append("Site[accessAllowed = ");
		builder.append(accessAllowed);
		builder.append(", circleID = ");
		builder.append(circleID);
		builder.append(", playUnchargedFor = ");
		builder.append(playUnchargedFor);
		builder.append(", playerURL = ");
		builder.append(playerURL);
		builder.append(", siteName = ");
		builder.append(siteName);
		builder.append(", sitePrefixes = ");
		builder.append(Arrays.toString(sitePrefixes));
		builder.append(", siteURL = ");
		builder.append(siteURL);
		builder.append(", supportedLanguages = ");
		builder.append(Arrays.toString(supportedLanguages));
		builder.append("]");
		return builder.toString();
	}
}
