package com.onmobile.apps.ringbacktones.webservice.client.beans;


/**
 * @author senthil.raja
 *
 */
public class BiDownloadHistory
{
	private String promoId = null;

	/**
	 * 
	 */
	public BiDownloadHistory()
	{

	}

	/**
	 * @param type
	 * @param amount
	 * @param mode
	 * @param date
	 */
	public BiDownloadHistory(String promoId)
	{
		this.promoId = promoId;
	}

	/**
	 * @return the promoId
	 */
	public String getPromoId()
	{
		return promoId;
	}


	/**
	 * @param promoId the promoId to set
	 */
	public void setPromoId(String promoId)
	{
		this.promoId = promoId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((promoId == null) ? 0 : promoId.hashCode());
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
		if (!(obj instanceof BiDownloadHistory))
			return false;
		BiDownloadHistory other = (BiDownloadHistory) obj;
		if (promoId == null)
		{
			if (other.promoId != null)
				return false;
		}
		else if (!promoId.equals(other.promoId))
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
		builder.append("BiDownloadHistory[promoId = ");
		builder.append(promoId);
		return builder.toString();
	}
}
