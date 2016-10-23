/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

/**
 * @author vinayasimha.patil
 *
 */
public class Cos
{
	private int cosID;
	private Date startDate = null;
	private Date endDate = null;
	private String cirlceID = null;
	private String userType = null;
	private String subscriptionClass = null;
	private String chargeClass = null;
	private int validDays;
	private int noOfFreeSongs;
	private int noOfFreeMusicboxes;
	private String promoClips = null;
	private boolean isDefault;
	private String accessMode = null;
	private String smsKeyword = null;
	private String operatorCode = null;

	/**
	 * 
	 */
	public Cos()
	{

	}

	/**
	 * @param cosID
	 * @param startDate
	 * @param endDate
	 * @param cirlceID
	 * @param userType
	 * @param subscriptionClass
	 * @param chargeClass
	 * @param validDays
	 * @param noOfFreeSongs
	 * @param noOfFreeMusicboxes
	 * @param promoClips
	 * @param isDefault
	 * @param accessMode
	 * @param smsKeyword
	 * @param operatorCode
	 */
	public Cos(int cosID, Date startDate, Date endDate, String cirlceID,
			String userType, String subscriptionClass, String chargeClass,
			int validDays, int noOfFreeSongs, int noOfFreeMusicboxes,
			String promoClips, boolean isDefault, String accessMode,
			String smsKeyword, String operatorCode)
	{
		this.cosID = cosID;
		this.startDate = startDate;
		this.endDate = endDate;
		this.cirlceID = cirlceID;
		this.userType = userType;
		this.subscriptionClass = subscriptionClass;
		this.chargeClass = chargeClass;
		this.validDays = validDays;
		this.noOfFreeSongs = noOfFreeSongs;
		this.noOfFreeMusicboxes = noOfFreeMusicboxes;
		this.promoClips = promoClips;
		this.isDefault = isDefault;
		this.accessMode = accessMode;
		this.smsKeyword = smsKeyword;
		this.operatorCode = operatorCode;
	}

	/**
	 * @return the cosID
	 */
	public int getCosID()
	{
		return cosID;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @return the cirlceID
	 */
	public String getCirlceID()
	{
		return cirlceID;
	}

	/**
	 * @return the userType
	 */
	public String getUserType()
	{
		return userType;
	}

	/**
	 * @return the subscriptionClass
	 */
	public String getSubscriptionClass()
	{
		return subscriptionClass;
	}

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass()
	{
		return chargeClass;
	}

	/**
	 * @return the validDays
	 */
	public int getValidDays()
	{
		return validDays;
	}

	/**
	 * @return the noOfFreeSongs
	 */
	public int getNoOfFreeSongs()
	{
		return noOfFreeSongs;
	}

	/**
	 * @return the noOfFreeMusicboxes
	 */
	public int getNoOfFreeMusicboxes()
	{
		return noOfFreeMusicboxes;
	}

	/**
	 * @return the promoClips
	 */
	public String getPromoClips()
	{
		return promoClips;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault()
	{
		return isDefault;
	}

	/**
	 * @return the accessMode
	 */
	public String getAccessMode()
	{
		return accessMode;
	}

	/**
	 * @return the smsKeyword
	 */
	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	/**
	 * @return the operatorCode
	 */
	public String getOperatorCode()
	{
		return operatorCode;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(int cosID)
	{
		this.cosID = cosID;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @param cirlceID the cirlceID to set
	 */
	public void setCirlceID(String cirlceID)
	{
		this.cirlceID = cirlceID;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	/**
	 * @param subscriptionClass the subscriptionClass to set
	 */
	public void setSubscriptionClass(String subscriptionClass)
	{
		this.subscriptionClass = subscriptionClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass)
	{
		this.chargeClass = chargeClass;
	}

	/**
	 * @param validDays the validDays to set
	 */
	public void setValidDays(int validDays)
	{
		this.validDays = validDays;
	}

	/**
	 * @param noOfFreeSongs the noOfFreeSongs to set
	 */
	public void setNoOfFreeSongs(int noOfFreeSongs)
	{
		this.noOfFreeSongs = noOfFreeSongs;
	}

	/**
	 * @param noOfFreeMusicboxes the noOfFreeMusicboxes to set
	 */
	public void setNoOfFreeMusicboxes(int noOfFreeMusicboxes)
	{
		this.noOfFreeMusicboxes = noOfFreeMusicboxes;
	}

	/**
	 * @param promoClips the promoClips to set
	 */
	public void setPromoClips(String promoClips)
	{
		this.promoClips = promoClips;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	/**
	 * @param smsKeyword the smsKeyword to set
	 */
	public void setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
	}

	/**
	 * @param accessMode the accessMode to set
	 */
	public void setAccessMode(String accessMode)
	{
		this.accessMode = accessMode;
	}

	/**
	 * @param operatorCode the operatorCode to set
	 */
	public void setOperatorCode(String operatorCode)
	{
		this.operatorCode = operatorCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessMode == null) ? 0 : accessMode.hashCode());
		result = prime * result + ((chargeClass == null) ? 0 : chargeClass.hashCode());
		result = prime * result + ((cirlceID == null) ? 0 : cirlceID.hashCode());
		result = prime * result + cosID;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (isDefault ? 1231 : 1237);
		result = prime * result + noOfFreeMusicboxes;
		result = prime * result + noOfFreeSongs;
		result = prime * result + ((operatorCode == null) ? 0 : operatorCode.hashCode());
		result = prime * result + ((promoClips == null) ? 0 : promoClips.hashCode());
		result = prime * result + ((smsKeyword == null) ? 0 : smsKeyword.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((subscriptionClass == null) ? 0 : subscriptionClass.hashCode());
		result = prime * result + ((userType == null) ? 0 : userType.hashCode());
		result = prime * result + validDays;
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
		if (!(obj instanceof Cos))
			return false;
		Cos other = (Cos) obj;
		if (accessMode == null)
		{
			if (other.accessMode != null)
				return false;
		}
		else if (!accessMode.equals(other.accessMode))
			return false;
		if (chargeClass == null)
		{
			if (other.chargeClass != null)
				return false;
		}
		else if (!chargeClass.equals(other.chargeClass))
			return false;
		if (cirlceID == null)
		{
			if (other.cirlceID != null)
				return false;
		}
		else if (!cirlceID.equals(other.cirlceID))
			return false;
		if (cosID != other.cosID)
			return false;
		if (endDate == null)
		{
			if (other.endDate != null)
				return false;
		}
		else if (!endDate.equals(other.endDate))
			return false;
		if (isDefault != other.isDefault)
			return false;
		if (noOfFreeMusicboxes != other.noOfFreeMusicboxes)
			return false;
		if (noOfFreeSongs != other.noOfFreeSongs)
			return false;
		if (operatorCode == null)
		{
			if (other.operatorCode != null)
				return false;
		}
		else if (!operatorCode.equals(other.operatorCode))
			return false;
		if (promoClips == null)
		{
			if (other.promoClips != null)
				return false;
		}
		else if (!promoClips.equals(other.promoClips))
			return false;
		if (smsKeyword == null)
		{
			if (other.smsKeyword != null)
				return false;
		}
		else if (!smsKeyword.equals(other.smsKeyword))
			return false;
		if (startDate == null)
		{
			if (other.startDate != null)
				return false;
		}
		else if (!startDate.equals(other.startDate))
			return false;
		if (subscriptionClass == null)
		{
			if (other.subscriptionClass != null)
				return false;
		}
		else if (!subscriptionClass.equals(other.subscriptionClass))
			return false;
		if (userType == null)
		{
			if (other.userType != null)
				return false;
		}
		else if (!userType.equals(other.userType))
			return false;
		if (validDays != other.validDays)
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
		builder.append("Cos[accessMode = ");
		builder.append(accessMode);
		builder.append(", chargeClass = ");
		builder.append(chargeClass);
		builder.append(", cirlceID = ");
		builder.append(cirlceID);
		builder.append(", cosID = ");
		builder.append(cosID);
		builder.append(", endDate = ");
		builder.append(endDate);
		builder.append(", isDefault = ");
		builder.append(isDefault);
		builder.append(", noOfFreeMusicboxes = ");
		builder.append(noOfFreeMusicboxes);
		builder.append(", noOfFreeSongs = ");
		builder.append(noOfFreeSongs);
		builder.append(", operatorCode = ");
		builder.append(operatorCode);
		builder.append(", promoClips = ");
		builder.append(promoClips);
		builder.append(", smsKeyword = ");
		builder.append(smsKeyword);
		builder.append(", startDate = ");
		builder.append(startDate);
		builder.append(", subscriptionClass = ");
		builder.append(subscriptionClass);
		builder.append(", userType = ");
		builder.append(userType);
		builder.append(", validDays = ");
		builder.append(validDays);
		builder.append("]");
		return builder.toString();
	}
}
