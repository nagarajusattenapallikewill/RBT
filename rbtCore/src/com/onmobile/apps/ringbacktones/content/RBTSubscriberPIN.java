package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTSubscriberPIN 
{

	private String subscriberID = null;
	private String pinID = null;
	private int activationsCount = 0;
	private int selectionsCount = 0;
	private Date creationTime = null;
	private Date updationTime = null;
	
	/**
	 * 
	 */
	public RBTSubscriberPIN()
	{
		
	}

	/**
	 * @param subscriberID
	 * @param pinID
	 * @param activationsCount
	 * @param selectionsCount
	 */
	public RBTSubscriberPIN(String subscriberID, String pinID, int activationsCount, int selectionsCount)
	{
		super();
		this.subscriberID = subscriberID;
		this.pinID = pinID;
		this.activationsCount = activationsCount;
		this.selectionsCount = selectionsCount;
	}
	
	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID() {
		return subscriberID;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the pinID
	 */
	public String getPinID() {
		return pinID;
	}

	/**
	 * @param pinID the pinID to set
	 */
	public void setPinID(String pinID) {
		this.pinID = pinID;
	}

	/**
	 * @return the activationsCount
	 */
	public int getActivationsCount() {
		return activationsCount;
	}

	/**
	 * @param activationsCount the activationsCount to set
	 */
	public void setActivationsCount(int activationsCount) {
		this.activationsCount = activationsCount;
	}

	/**
	 * @return the selectionsCount
	 */
	public int getSelectionsCount() {
		return selectionsCount;
	}

	/**
	 * @param selectionsCount the selectionsCount to set
	 */
	public void setSelectionsCount(int selectionsCount) {
		this.selectionsCount = selectionsCount;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return the updationTime
	 */
	public Date getUpdationTime() {
		return updationTime;
	}

	/**
	 * @param updationTime the updationTime to set
	 */
	public void setUpdationTime(Date updationTime) {
		this.updationTime = updationTime;
	}

	@Override
	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RBTSubscriberPIN[subscriberID=");
		builder.append(subscriberID);
		builder.append(", pinID=");
		builder.append(pinID);
		builder.append(", activationsCount=");
		builder.append(activationsCount);
		builder.append(", selectionsCount=");
		builder.append(selectionsCount);
		builder.append(", creationTime=");
		builder.append(creationTime);
		builder.append(", updationTime=");
		builder.append(updationTime);
		builder.append("]");

		return builder.toString();
	}
}
