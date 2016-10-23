package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Downloads 
{
	private int noOfActiveDownloads;
	private Download[] downloads = null;

	/**
	 * 
	 */
	public Downloads()
	{

	}

	/**
	 * @param noOfActiveDownloads
	 * @param downloads
	 */
	public Downloads(int noOfActiveDownloads, Download[] downloads)
	{
		this.noOfActiveDownloads = noOfActiveDownloads;
		this.downloads = downloads;
	}

	/**
	 * @return the noOfActiveDownloads
	 */
	public int getNoOfActiveDownloads()
	{
		return noOfActiveDownloads;
	}

	/**
	 * @return the downloads
	 */
	public Download[] getDownloads()
	{
		return downloads;
	}

	/**
	 * @param noOfActiveDownloads the noOfActiveDownloads to set
	 */
	public void setNoOfActiveDownloads(int noOfActiveDownloads)
	{
		this.noOfActiveDownloads = noOfActiveDownloads;
	}

	/**
	 * @param downloads the downloads to set
	 */
	public void setDownloads(Download[] downloads)
	{
		this.downloads = downloads;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(downloads);
		result = prime * result + noOfActiveDownloads;
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
		if (!(obj instanceof Downloads))
			return false;
		Downloads other = (Downloads) obj;
		if (!Arrays.equals(downloads, other.downloads))
			return false;
		if (noOfActiveDownloads != other.noOfActiveDownloads)
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
		builder.append("Downloads[downloads = ");
		builder.append(Arrays.toString(downloads));
		builder.append(", noOfActiveDownloads = ");
		builder.append(noOfActiveDownloads);
		builder.append("]");
		return builder.toString();
	}
}
