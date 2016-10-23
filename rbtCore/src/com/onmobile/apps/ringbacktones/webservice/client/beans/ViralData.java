/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vinayasimha.patil
 * 
 */
public class ViralData implements Comparable<ViralData>
{
	private long smsID;
	private String subscriberID = null;
	private String callerID = null;
	private String type = null;
	private String clipID = null;
	private Date sentTime = null;
	private Date setTime = null;
	private int count;
	private String selectedBy = null;
	private HashMap<String, String> infoMap = null;

	/**
	 * 
	 */
	public ViralData()
	{

	}

	/**
	 * @param smsID
	 * @param subscriberID
	 * @param callerID
	 * @param type
	 * @param clipID
	 * @param sentTime
	 * @param setTime
	 * @param count
	 * @param selectedBy
	 * @param infoMap
	 */
	public ViralData(long smsID, String subscriberID, String callerID,
			String type, String clipID, Date sentTime, Date setTime, int count,
			String selectedBy, HashMap<String, String> infoMap)
	{
		super();
		this.smsID = smsID;
		this.subscriberID = subscriberID;
		this.callerID = callerID;
		this.type = type;
		this.clipID = clipID;
		this.sentTime = sentTime;
		this.setTime = setTime;
		this.count = count;
		this.selectedBy = selectedBy;
		this.infoMap = infoMap;
	}

	/**
	 * @return the smsID
	 */
	public long getSmsID()
	{
		return smsID;
	}

	/**
	 * @param smsID
	 *            the smsID to set
	 */
	public void setSmsID(long smsID)
	{
		this.smsID = smsID;
	}

	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the clipID
	 */
	public String getClipID()
	{
		return clipID;
	}

	/**
	 * @return the sentTime
	 */
	public Date getSentTime()
	{
		return sentTime;
	}

	/**
	 * @return the setTime
	 */
	public Date getSetTime()
	{
		return setTime;
	}

	/**
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy()
	{
		return selectedBy;
	}

	/**
	 * @return the infoMap
	 */
	public HashMap<String, String> getInfoMap()
	{
		return infoMap;
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
	 * @param callerID
	 *            the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @param clipID
	 *            the clipID to set
	 */
	public void setClipID(String clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @param sentTime
	 *            the sentTime to set
	 */
	public void setSentTime(Date sentTime)
	{
		this.sentTime = sentTime;
	}

	/**
	 * @param setTime
	 *            the setTime to set
	 */
	public void setSetTime(Date setTime)
	{
		this.setTime = setTime;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * @param selectedBy
	 *            the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy)
	{
		this.selectedBy = selectedBy;
	}

	/**
	 * @param infoMap
	 *            the infoMap to set
	 */
	public void setInfoMap(HashMap<String, String> infoMap)
	{
		this.infoMap = infoMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callerID == null) ? 0 : callerID.hashCode());
		result = prime * result + ((clipID == null) ? 0 : clipID.hashCode());
		result = prime * result + count;
		result = prime * result + ((infoMap == null) ? 0 : infoMap.hashCode());
		result = prime * result
				+ ((selectedBy == null) ? 0 : selectedBy.hashCode());
		result = prime * result
				+ ((sentTime == null) ? 0 : sentTime.hashCode());
		result = prime * result + ((setTime == null) ? 0 : setTime.hashCode());
		result = prime * result + (int) (smsID ^ (smsID >>> 32));
		result = prime * result
				+ ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		ViralData other = (ViralData) obj;
		if (callerID == null)
		{
			if (other.callerID != null)
				return false;
		}
		else if (!callerID.equals(other.callerID))
			return false;
		if (clipID == null)
		{
			if (other.clipID != null)
				return false;
		}
		else if (!clipID.equals(other.clipID))
			return false;
		if (count != other.count)
			return false;
		if (infoMap == null)
		{
			if (other.infoMap != null)
				return false;
		}
		else if (!infoMap.equals(other.infoMap))
			return false;
		if (selectedBy == null)
		{
			if (other.selectedBy != null)
				return false;
		}
		else if (!selectedBy.equals(other.selectedBy))
			return false;
		if (sentTime == null)
		{
			if (other.sentTime != null)
				return false;
		}
		else if (!sentTime.equals(other.sentTime))
			return false;
		if (setTime == null)
		{
			if (other.setTime != null)
				return false;
		}
		else if (!setTime.equals(other.setTime))
			return false;
		if (smsID != other.smsID)
			return false;
		if (subscriberID == null)
		{
			if (other.subscriberID != null)
				return false;
		}
		else if (!subscriberID.equals(other.subscriberID))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ViralData [callerID=");
		builder.append(callerID);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", count=");
		builder.append(count);
		builder.append(", infoMap=");
		builder.append(infoMap);
		builder.append(", selectedBy=");
		builder.append(selectedBy);
		builder.append(", sentTime=");
		builder.append(sentTime);
		builder.append(", setTime=");
		builder.append(setTime);
		builder.append(", smsID=");
		builder.append(smsID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ViralData viralData)
	{
		return (int)(this.smsID - viralData.smsID);
	}
}
