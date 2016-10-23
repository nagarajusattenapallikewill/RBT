package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;

public class SubscriberDetail
{
	String subscriberID;
	String circleID;
	String mnpOperatorName = null;
	String mnpCircleName = null;
	boolean isPrepaid;
	boolean isValidSubscriber;
	boolean isValidOperator = true;
	HashMap<String, String> subscriberDetailsMap = new HashMap<String, String>();

	/**
	 * 
	 */
	public SubscriberDetail()
	{

	}

	/**
	 * @param subscriberID
	 * @param circleID
	 * @param isPrepaid
	 * @param isValidSubscriber
	 * @param subscriberDetailsMap
	 */
	public SubscriberDetail(String subscriberID, String circleID,
			boolean isPrepaid, boolean isValidSubscriber,
			HashMap<String, String> subscriberDetailsMap)
	{
		this.subscriberID = subscriberID;
		this.circleID = circleID;
		this.isPrepaid = isPrepaid;
		this.isValidSubscriber = isValidSubscriber;
		this.subscriberDetailsMap = subscriberDetailsMap;
	}


	/**
	 * @return the subscriberID
	 */
	public String getSubscriberID()
	{
		return subscriberID;
	}

	public SubscriberDetail(String subscriberID, String circleID,
			boolean isPrepaid, boolean isValidSubscriber,
			HashMap<String, String> subscriberDetailsMap,String mnpOperatorName,String mnpCircleName) {
		this.subscriberID = subscriberID;
		this.circleID = circleID;
		this.mnpOperatorName = mnpOperatorName;
		this.mnpCircleName = mnpCircleName;
		this.isPrepaid = isPrepaid;
		this.isValidSubscriber = isValidSubscriber;
		this.isValidOperator = isValidOperator;
		this.subscriberDetailsMap = subscriberDetailsMap;
	}

	/**
	 * @param subscriberID the subscriberID to set
	 */
	public void setSubscriberID(String subscriberID)
	{
		this.subscriberID = subscriberID;
	}

	/**
	 * @return the circleID
	 */
	public String getCircleID()
	{
		return circleID;
	}

	/**
	 * @param circleID the circleID to set
	 */
	public void setCircleID(String circleID)
	{
		this.circleID = circleID;
	}

	/**
	 * @return the isPrepaid
	 */
	public boolean isPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setPrepaid(boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @return the isValidSubscriber
	 */
	public boolean isValidSubscriber()
	{
		return isValidSubscriber;
	}

	/**
	 * @param isValidSubscriber the isValidSubscriber to set
	 */
	public void setValidSubscriber(boolean isValidSubscriber)
	{
		this.isValidSubscriber = isValidSubscriber;
	}

	public boolean isValidOperator() {
		return isValidOperator;
	}

	public void setValidOperator(boolean isValidOperator) {
		this.isValidOperator = isValidOperator;
	}

	/**
	 * @return the subscriberDetailsMap
	 */
	public HashMap<String, String> getSubscriberDetailsMap()
	{
		return subscriberDetailsMap;
	}

	/**
	 * @param subscriberDetailsMap the subscriberDetailsMap to set
	 */
	public void setSubscriberDetailsMap(HashMap<String, String> subscriberDetailsMap)
	{
		this.subscriberDetailsMap = subscriberDetailsMap;
	}

	public String getMnpOperatorName() {
		return mnpOperatorName;
	}

	public void setMnpOperatorName(String mnpOperatorName) {
		this.mnpOperatorName = mnpOperatorName;
	}

	public String getMnpCircleName() {
		return mnpCircleName;
	}

	public void setMnpCircleName(String mnpCircleName) {
		this.mnpCircleName = mnpCircleName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriberDetail[circleID = ");
		builder.append(circleID);
		builder.append(", isPrepaid = ");
		builder.append(isPrepaid);
		builder.append(", isValidSubscriber = ");
		builder.append(isValidSubscriber);
		builder.append(", isValidOperator = ");
		builder.append(isValidOperator);
		builder.append(", subscriberDetailsMap = ");
		builder.append(subscriberDetailsMap);
		builder.append(", subscriberID = ");
		builder.append(subscriberID);
		builder.append(", mnpOperatorName = ");
		builder.append(mnpOperatorName);
		builder.append(", mnpCircleName = ");
		builder.append(mnpCircleName);
		builder.append("]");
		return builder.toString();
	}
}
