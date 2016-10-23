/**
 * 
 */
package com.onmobile.apps.ringbacktones.services.msisdninfo;

/**
 * @author vinayasimha.patil
 */
public class MNPContext
{
	private String subscriberID = null;
	private String mode = null;
	private boolean isOnlineDip = false;

	/**
	 * @param subscriberID
	 */
	public MNPContext(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @param subscriberID
	 * @param mode
	 */
	public MNPContext(String subscriberID, String mode)
	{
		this.subscriberID = subscriberID;
		this.mode = mode;
	}

	/**
	 * @param subscriberID
	 * @param mode
	 * @param isOnlineDip
	 */
	public MNPContext(String subscriberID, String mode, boolean isOnlineDip)
	{
		this.subscriberID = subscriberID;
		this.mode = mode;
		this.isOnlineDip = isOnlineDip;
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
	 * @return the mode
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
	}

	/**
	 * @return the isOnlineDip
	 */
	public boolean isOnlineDip()
	{
		return isOnlineDip;
	}

	/**
	 * @param isOnlineDip
	 *            the isOnlineDip to set
	 */
	public void setOnlineDip(boolean isOnlineDip)
	{
		this.isOnlineDip = isOnlineDip;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MNPContext[subscriberID = ");
		builder.append(subscriberID);
		builder.append(", mode = ");
		builder.append(mode);
		builder.append(", isOnlineDip = ");
		builder.append(isOnlineDip);
		builder.append("]");
		return builder.toString();
	}
}
