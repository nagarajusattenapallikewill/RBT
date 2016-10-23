package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ParametersBean used by hibernate to persist into the RBT_BULK_PROMO_SMS
 * table.
 * 
 * @author bikash.panda
 */
public class BulkPromo implements Serializable
{
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String bulkPromoID;
	private Timestamp smsStartDate;
	private Timestamp smsEndDate;
	private String processedDeactivation;
	private String cosID;

	/**
	 * 
	 */
	public BulkPromo()
	{

	}

	/**
	 * @param bulkPromoID
	 * @param smsStartDate
	 * @param smsEndDate
	 * @param processedDeactivation
	 * @param cosID
	 */
	public BulkPromo(String bulkPromoID, Timestamp smsStartDate,
			Timestamp smsEndDate, String processedDeactivation, String cosID)
	{
		this.bulkPromoID = bulkPromoID;
		this.smsStartDate = smsStartDate;
		this.smsEndDate = smsEndDate;
		this.processedDeactivation = processedDeactivation;
		this.cosID = cosID;
	}
	
	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the bulkPromoID
	 */
	public String getBulkPromoID()
	{
		return bulkPromoID;
	}

	/**
	 * @param bulkPromoID the bulkPromoID to set
	 */
	public void setBulkPromoID(String bulkPromoID)
	{
		this.bulkPromoID = bulkPromoID;
	}

	/**
	 * @return the smsStartDate
	 */
	public Timestamp getSmsStartDate()
	{
		return smsStartDate;
	}

	/**
	 * @param smsStartDate the smsStartDate to set
	 */
	public void setSmsStartDate(Timestamp smsStartDate)
	{
		this.smsStartDate = smsStartDate;
	}

	/**
	 * @return the smsEndDate
	 */
	public Timestamp getSmsEndDate()
	{
		return smsEndDate;
	}

	/**
	 * @param smsEndDate the smsEndDate to set
	 */
	public void setSmsEndDate(Timestamp smsEndDate)
	{
		this.smsEndDate = smsEndDate;
	}

	/**
	 * @return the processedDeactivation
	 */
	public String getProcessedDeactivation()
	{
		return processedDeactivation;
	}

	/**
	 * @param processedDeactivation the processedDeactivation to set
	 */
	public void setProcessedDeactivation(String processedDeactivation)
	{
		this.processedDeactivation = processedDeactivation;
	}

	/**
	 * @return the cosID
	 */
	public String getCosID()
	{
		return cosID;
	}

	/**
	 * @param cosID the cosID to set
	 */
	public void setCosID(String cosID)
	{
		this.cosID = cosID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("BulkPromo[bulkPromoID = ");
		builder.append(bulkPromoID);
		builder.append(", cosID = ");
		builder.append(cosID);
		builder.append(", processedDeactivation = ");
		builder.append(processedDeactivation);
		builder.append(", smsEndDate = ");
		builder.append(smsEndDate);
		builder.append(", smsStartDate = ");
		builder.append(smsStartDate);
		builder.append("]");
		return builder.toString();
	}
}
