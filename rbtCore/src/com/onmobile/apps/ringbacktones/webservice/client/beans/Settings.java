package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Settings 
{
	private int noOfDefaultSettings;
	private int noOfSpecialSettings;
	private Setting[] settings = null;

	/**
	 * 
	 */
	public Settings()
	{

	}

	/**
	 * @param noOfDefaultSettings
	 * @param noOfSpecialSettings
	 * @param settings
	 */
	public Settings(int noOfDefaultSettings, int noOfSpecialSettings, Setting[] settings)
	{
		this.noOfDefaultSettings = noOfDefaultSettings;
		this.noOfSpecialSettings = noOfSpecialSettings;
		this.settings = settings;
	}

	/**
	 * @return the noOfDefaultSettings
	 */
	public int getNoOfDefaultSettings()
	{
		return noOfDefaultSettings;
	}

	/**
	 * @return the noOfSpecialSettings
	 */
	public int getNoOfSpecialSettings()
	{
		return noOfSpecialSettings;
	}

	/**
	 * @return the settings
	 */
	public Setting[] getSettings()
	{
		return settings;
	}

	/**
	 * @param noOfDefaultSettings the noOfDefaultSettings to set
	 */
	public void setNoOfDefaultSettings(int noOfDefaultSettings)
	{
		this.noOfDefaultSettings = noOfDefaultSettings;
	}

	/**
	 * @param noOfSpecialSettings the noOfSpecialSettings to set
	 */
	public void setNoOfSpecialSettings(int noOfSpecialSettings)
	{
		this.noOfSpecialSettings = noOfSpecialSettings;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(Setting[] settings)
	{
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + noOfDefaultSettings;
		result = prime * result + noOfSpecialSettings;
		result = prime * result + Arrays.hashCode(settings);
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
		if (!(obj instanceof Settings))
			return false;
		Settings other = (Settings) obj;
		if (noOfDefaultSettings != other.noOfDefaultSettings)
			return false;
		if (noOfSpecialSettings != other.noOfSpecialSettings)
			return false;
		if (!Arrays.equals(settings, other.settings))
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
		builder.append("Settings[noOfDefaultSettings = ");
		builder.append(noOfDefaultSettings);
		builder.append(", noOfSpecialSettings = ");
		builder.append(noOfSpecialSettings);
		builder.append(", settings = ");
		builder.append(Arrays.toString(settings));
		builder.append("]");
		return builder.toString();
	}
}
