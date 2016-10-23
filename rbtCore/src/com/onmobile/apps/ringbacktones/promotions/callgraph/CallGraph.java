/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author vinayasimha.patil
 * 
 */
public class CallGraph
{
	private long callGraphID;
	private String subscriberID = null;
	private Map<String, Set<Long>> callersDetails = null;
	private Set<String> frequentCallers = null;

	private Date createdTime = null;
	private Date updatedTime = null;

	private boolean confirmedForPromotion = false;
	private Date promotionConfirmedTime = null;
	private Date promotedTime = null;

	private int rbtClipID = 0;
	private PromotionStatus promotionStatus = null;

	/**
	 * 
	 */
	public CallGraph()
	{
		promotionStatus = PromotionStatus.INITIAL;
	}

	/**
	 * @return the callGraphID
	 */
	public long getCallGraphID()
	{
		return callGraphID;
	}

	/**
	 * @param callGraphID
	 *            the callGraphID to set
	 */
	public void setCallGraphID(long callGraphID)
	{
		this.callGraphID = callGraphID;
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
	 * @return the callersDetails
	 */
	public Map<String, Set<Long>> getCallersDetails()
	{
		return callersDetails;
	}

	/**
	 * @param callersDetails
	 *            the callersDetails to set
	 */
	public void setCallersDetails(Map<String, Set<Long>> callersDetails)
	{
		this.callersDetails = callersDetails;
	}

	/**
	 * @return the frequentCallers
	 */
	public Set<String> getFrequentCallers()
	{
		return frequentCallers;
	}

	/**
	 * @param frequentCallers
	 *            the frequentCallers to set
	 */
	public void setFrequentCallers(Set<String> frequentCallers)
	{
		this.frequentCallers = frequentCallers;
	}

	/**
	 * @return the createdTime
	 */
	public Date getCreatedTime()
	{
		return createdTime;
	}

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public void setCreatedTime(Date createdTime)
	{
		this.createdTime = createdTime;
	}

	/**
	 * @return the updatedTime
	 */
	public Date getUpdatedTime()
	{
		return updatedTime;
	}

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public void setUpdatedTime(Date updatedTime)
	{
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the confirmedForPromotion
	 */
	public boolean isConfirmedForPromotion()
	{
		return confirmedForPromotion;
	}

	/**
	 * @param confirmedForPromotion
	 *            the confirmedForPromotion to set
	 */
	public void setConfirmedForPromotion(boolean confirmedForPromotion)
	{
		this.confirmedForPromotion = confirmedForPromotion;
	}

	/**
	 * @return the promotionConfirmedTime
	 */
	public Date getPromotionConfirmedTime()
	{
		return promotionConfirmedTime;
	}

	/**
	 * @param promotionConfirmedTime
	 *            the promotionConfirmedTime to set
	 */
	public void setPromotionConfirmedTime(Date promotionConfirmedTime)
	{
		this.promotionConfirmedTime = promotionConfirmedTime;
	}

	/**
	 * @return the promotedTime
	 */
	public Date getPromotedTime()
	{
		return promotedTime;
	}

	/**
	 * @param promotedTime
	 *            the promotedTime to set
	 */
	public void setPromotedTime(Date promotedTime)
	{
		this.promotedTime = promotedTime;
	}

	/**
	 * @return the rbtClipID
	 */
	public int getRbtClipID()
	{
		return rbtClipID;
	}

	/**
	 * @param rbtClipID
	 *            the rbtClipID to set
	 */
	public void setRbtClipID(int rbtClipID)
	{
		this.rbtClipID = rbtClipID;
	}

	/**
	 * @return the promotionStatus
	 */
	public PromotionStatus getPromotionStatus()
	{
		return promotionStatus;
	}

	/**
	 * @param promotionStatus
	 *            the promotionStatus to set
	 */
	public void setPromotionStatus(PromotionStatus promotionStatus)
	{
		this.promotionStatus = promotionStatus;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CallGraph [callGraphID=");
		builder.append(callGraphID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", callersDetails=");
		builder.append(callersDetails);
		builder.append(", frequentCallers=");
		builder.append(frequentCallers);
		builder.append(", createdTime=");
		builder.append(createdTime);
		builder.append(", updatedTime=");
		builder.append(updatedTime);
		builder.append(", confirmedForPromotion=");
		builder.append(confirmedForPromotion);
		builder.append(", promotionConfirmedTime=");
		builder.append(promotionConfirmedTime);
		builder.append(", promotedTime=");
		builder.append(promotedTime);
		builder.append(", rbtClipID=");
		builder.append(rbtClipID);
		builder.append(", promotionStatus=");
		builder.append(promotionStatus);
		builder.append("]");
		return builder.toString();
	}

	public enum PromotionStatus
	{
		INITIAL,
		CONFIRMATION_PENDING,
		CONFIRMED,
		PROMOTION_SENT
	}
}
