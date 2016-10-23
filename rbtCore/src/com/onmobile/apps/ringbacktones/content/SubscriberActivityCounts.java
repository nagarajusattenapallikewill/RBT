/**
 * 
 */
package com.onmobile.apps.ringbacktones.content;

import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;

/**
 * @author vinayasimha.patil
 */
public class SubscriberActivityCounts
{
	private String subscriberID = null;
	private Date date = null;
	private String counts = null;

	private int serviceGiftsCount = 0;
	private int toneGiftsCount = 0;

	private static final String SERVICE_GIFTS_KEY = "SERVICE_GIFTS";
	private static final String TONE_GIFTS_KEY = "TONE_GIFTS";

	/**
	 * 
	 */
	public SubscriberActivityCounts()
	{

	}

	/**
	 * @param subscriberID
	 * @param date
	 */
	public SubscriberActivityCounts(String subscriberID, Date date)
	{
		this.subscriberID = subscriberID;
		this.date = date;
	}

	/**
	 * @param subscriberID
	 * @param date
	 * @param counts
	 */
	public SubscriberActivityCounts(String subscriberID, Date date,
			String counts)
	{
		this.subscriberID = subscriberID;
		this.date = date;
		this.counts = counts;
		assignCounts();
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @param subscriberID
	 *            the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

	/**
	 * @return the serviceGiftsCount
	 */
	public int getServiceGiftsCount()
	{
		return serviceGiftsCount;
	}

	/**
	 * @param serviceGiftsCount
	 *            the serviceGiftsCount to set
	 */
	public void setServiceGiftsCount(int serviceGiftsCount)
	{
		this.serviceGiftsCount = serviceGiftsCount;
		createCountsXML();
	}

	public void incrementServiceGiftsCount()
	{
		serviceGiftsCount++;
		createCountsXML();
	}

	public void decrementServiceGiftsCount()
	{
		serviceGiftsCount--;
		createCountsXML();
	}

	/**
	 * @return the toneGiftsCount
	 */
	public int getToneGiftsCount()
	{
		return toneGiftsCount;
	}

	/**
	 * @param toneGiftsCount
	 *            the toneGiftsCount to set
	 */
	public void setToneGiftsCount(int toneGiftsCount)
	{
		this.toneGiftsCount = toneGiftsCount;
		createCountsXML();
	}

	public void incrementToneGiftsCount()
	{
		toneGiftsCount++;
		createCountsXML();
	}

	public void decrementToneGiftsCount()
	{
		toneGiftsCount--;
		createCountsXML();
	}

	/**
	 * @return the counts
	 */
	public String getCounts()
	{
		return counts;
	}

	/**
	 * @param counts
	 *            the counts to set
	 */
	public void setCounts(String counts)
	{
		this.counts = counts;
		assignCounts();
	}

	private void assignCounts()
	{
		HashMap<String, String> countsMap = DBUtility
				.getAttributeMapFromXML(counts);
		if (countsMap == null)
			return;

		if (countsMap.containsKey(SERVICE_GIFTS_KEY))
			serviceGiftsCount = Integer.valueOf(countsMap
					.get(SERVICE_GIFTS_KEY));

		if (countsMap.containsKey(TONE_GIFTS_KEY))
			toneGiftsCount = Integer.valueOf(countsMap.get(TONE_GIFTS_KEY));
	}

	private String createCountsXML()
	{
		HashMap<String, String> countsMap = DBUtility
				.getAttributeMapFromXML(counts);
		if (countsMap == null)
			countsMap = new HashMap<String, String>();

		if (serviceGiftsCount <= 0)
			countsMap.remove(SERVICE_GIFTS_KEY);
		else
			countsMap.put(SERVICE_GIFTS_KEY, String.valueOf(serviceGiftsCount));

		if (toneGiftsCount <= 0)
			countsMap.remove(TONE_GIFTS_KEY);
		else
			countsMap.put(TONE_GIFTS_KEY, String.valueOf(toneGiftsCount));

		counts = DBUtility.getAttributeXMLFromMap(countsMap);
		return counts;
	}

	public boolean create()
	{
		return (SubscriberActivityCountsDAO
				.createSubscriberActivityCounts(this) != null);
	}

	public boolean update()
	{
		return (SubscriberActivityCountsDAO
				.updateSubscriberActivityCounts(this) != null);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriberActivityCounts[counts = ");
		builder.append(counts);
		builder.append(", date = ");
		builder.append(date);
		builder.append(", serviceGiftsCount = ");
		builder.append(serviceGiftsCount);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", toneGiftsCount = ");
		builder.append(toneGiftsCount);
		builder.append("]");
		return builder.toString();
	}
}
