package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class GiftInbox 
{
	private Gift[] gifts = null;

	/**
	 * 
	 */
	public GiftInbox()
	{

	}

	/**
	 * @param gifts
	 */
	public GiftInbox(Gift[] gifts)
	{
		this.gifts = gifts;
	}

	/**
	 * @return the gifts
	 */
	public Gift[] getGifts()
	{
		return gifts;
	}

	/**
	 * @param gifts the gifts to set
	 */
	public void setGifts(Gift[] gifts)
	{
		this.gifts = gifts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(gifts);
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
		if (!(obj instanceof GiftInbox))
			return false;
		GiftInbox other = (GiftInbox) obj;
		if (!Arrays.equals(gifts, other.gifts))
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
		builder.append("GiftInbox[gifts = ");
		builder.append(Arrays.toString(gifts));
		builder.append("]");
		return builder.toString();
	}
}
