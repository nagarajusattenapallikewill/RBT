package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Bookmarks
{
	private Bookmark[] bookmarks = null;

	/**
	 * 
	 */
	public Bookmarks()
	{

	}

	/**
	 * @param bookmarks
	 */
	public Bookmarks(Bookmark[] bookmarks)
	{
		this.bookmarks = bookmarks;
	}

	/**
	 * @return the bookmarks
	 */
	public Bookmark[] getBookmarks()
	{
		return bookmarks;
	}

	/**
	 * @param bookmarks the bookmarks to set
	 */
	public void setBookmarks(Bookmark[] bookmarks)
	{
		this.bookmarks = bookmarks;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bookmarks);
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
		if (!(obj instanceof Bookmarks))
			return false;
		Bookmarks other = (Bookmarks) obj;
		if (!Arrays.equals(bookmarks, other.bookmarks))
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
		builder.append("Bookmarks[bookmarks = ");
		builder.append(Arrays.toString(bookmarks));
		builder.append("]");
		return builder.toString();
	}
}
