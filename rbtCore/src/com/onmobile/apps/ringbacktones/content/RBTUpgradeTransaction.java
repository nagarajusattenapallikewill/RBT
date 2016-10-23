package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTUpgradeTransaction 
{
	private int sequenceID;
	private String subscriberID = null;
	private int transactionType;
	private String subscriptionClass = null;
	private String cosID = null;
	private String offerID = null;
	private String chargeClass = null;
	private Date creationTime = null;
	private String activatedBy = null;
	private String activationInfo = null;
	private String extraInfo = null;
	private int rbtType;
	
	/**
	 * 
	 */
	public RBTUpgradeTransaction()
	{
		
	}
	
	/**
	 * @return
	 */
	public int getSequenceID() 
	{
		return sequenceID;
	}
	
	/**
	 * @param sequenceID
	 */
	public void setSequenceID(int sequenceID) 
	{
		this.sequenceID = sequenceID;
	}
	
	/**
	 * @return
	 */
	public String getSubscriberID() 
	{
		return subscriberID;
	}
	
	/**
	 * @param subscriberID
	 */
	public void setSubscriberID(String subscriberID) 
	{
		this.subscriberID = subscriberID;
	}
	
	/**
	 * @return
	 */
	public int getTransactionType() 
	{
		return transactionType;
	}
	
	/**
	 * @param transactionType
	 */
	public void setTransactionType(int type) 
	{
		this.transactionType = type;
	}
	
	/**
	 * @return
	 */
	public String getSubscriptionClass() 
	{
		return subscriptionClass;
	}
	
	/**
	 * @param subscriptionClass
	 */
	public void setSubscriptionClass(String subscriptionClass) 
	{
		this.subscriptionClass = subscriptionClass;
	}
	
	/**
	 * @return
	 */
	public String getCosID() 
	{
		return cosID;
	}
	
	/**
	 * @param cosID
	 */
	public void setCosID(String cosID) 
	{
		this.cosID = cosID;
	}
	
	/**
	 * @return
	 */
	public String getOfferID() 
	{
		return offerID;
	}
	
	/**
	 * @param offerID
	 */
	public void setOfferID(String offerID) 
	{
		this.offerID = offerID;
	}
	
	/**
	 * @return
	 */
	public String getChargeClass() 
	{
		return chargeClass;
	}
	
	/**
	 * @param chargeClass
	 */
	public void setChargeClass(String chargeClass) 
	{
		this.chargeClass = chargeClass;
	}
	
	/**
	 * @return
	 */
	public Date getCreationTime() 
	{
		return creationTime;
	}
	
	/**
	 * @param creationTime
	 */
	public void setCreationTime(Date creationTime) 
	{
		this.creationTime = creationTime;
	}
	
	/**
	 * @return
	 */
	public String getActivatedBy() 
	{
		return activatedBy;
	}
	
	/**
	 * @param activatedBy
	 */
	public void setActivatedBy(String activatedBy) 
	{
		this.activatedBy = activatedBy;
	}
	
	/**
	 * @return
	 */
	public String getActivationInfo() 
	{
		return activationInfo;
	}
	
	/**
	 * @param activationInfo
	 */
	public void setActivationInfo(String activationInfo) 
	{
		this.activationInfo = activationInfo;
	}
	
	/**
	 * @return
	 */
	public String getExtraInfo() 
	{
		return extraInfo;
	}
	
	/**
	 * @param extraInfo
	 */
	public void setExtraInfo(String extraInfo) 
	{
		this.extraInfo = extraInfo;
	}
	
	/**
	 * @return
	 */
	public int getRbtType() 
	{
		return rbtType;
	}

	/**
	 * @param rbtType
	 */
	public void setRbtType(int rbtType) 
	{
		this.rbtType = rbtType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RBTUpgradeTransaction[sequenceID=");
		builder.append(sequenceID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", transactionType=");
		builder.append(transactionType);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", cosID=");
		builder.append(cosID);
		builder.append(", offerID=");
		builder.append(offerID);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", creationTime=");
		builder.append(creationTime);
		builder.append(", activatedBy=");
		builder.append(activatedBy);
		builder.append(", activationInfo=");
		builder.append(activationInfo);
		builder.append(", extraInfo=");
		builder.append(extraInfo);
		builder.append(", rbtType=");
		builder.append(rbtType);
		builder.append("]");

		return builder.toString();
	}
}
