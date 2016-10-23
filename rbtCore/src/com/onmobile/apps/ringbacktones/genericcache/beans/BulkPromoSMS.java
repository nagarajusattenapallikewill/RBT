package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * ParametersBean used by hibernate to persist into the RBT_BULK_PROMO_SMS
 * table.
 * 
 * @author bikash.panda
 */
public class BulkPromoSMS implements Serializable
{
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 26754387765432L;

	private String bulkpromoID;
	private String smsDate;
	private String smsText;
	private String smsSent;

	/**
	 * 
	 */
	public BulkPromoSMS()
	{

	}

	/**
	 * @param bulkpromoID
	 * @param smsDate
	 * @param smsText
	 * @param smsSent
	 */
	public BulkPromoSMS(String bulkpromoID, String smsDate, String smsText,
			String smsSent)
	{
		this.bulkpromoID = bulkpromoID;
		this.smsDate = smsDate;
		this.smsText = smsText;
		this.smsSent = smsSent;
	}
	
	/**
	 * @return the serialversionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the bulkpromoID
	 */
	public String getBulkpromoID()
	{
		return bulkpromoID;
	}

	/**
	 * @param bulkpromoID the bulkpromoID to set
	 */
	public void setBulkpromoID(String bulkpromoID)
	{
		this.bulkpromoID = bulkpromoID;
	}

	/**
	 * @return the smsDate
	 */
	public String getSmsDate()
	{
		return smsDate;
	}

	/**
	 * @param smsDate the smsDate to set
	 */
	public void setSmsDate(String smsDate)
	{
		this.smsDate = smsDate;
	}

	/**
	 * @return the smsText
	 */
	public String getSmsText()
	{
		return smsText;
	}

	/**
	 * @param smsText the smsText to set
	 */
	public void setSmsText(String smsText)
	{
		this.smsText = smsText;
	}

	/**
	 * @return the smsSent
	 */
	public String getSmsSent()
	{
		return smsSent;
	}

	/**
	 * @param smsSent the smsSent to set
	 */
	public void setSmsSent(String smsSent)
	{
		this.smsSent = smsSent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("BulkPromoSMS[bulkpromoID = ");
		builder.append(bulkpromoID);
		builder.append(", smsDate = ");
		builder.append(smsDate);
		builder.append(", smsSent = ");
		builder.append(smsSent);
		builder.append(", smsText = ");
		builder.append(smsText);
		builder.append("]");
		return builder.toString();
	}
}
