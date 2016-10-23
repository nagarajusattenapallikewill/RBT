package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class GiftOutbox 
{
	private Gift[] gifts = null;

	/**
	 * 
	 */
	public GiftOutbox()
	{

	}

	/**
	 * @param gifts
	 */
	public GiftOutbox(Gift[] gifts)
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
		if (!(obj instanceof GiftOutbox))
			return false;
		GiftOutbox other = (GiftOutbox) obj;
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
		builder.append("GiftOutbox[gifts = ");
		builder.append(Arrays.toString(gifts));
		builder.append("]");
		return builder.toString();
	}
}
