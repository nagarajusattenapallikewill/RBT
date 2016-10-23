/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: rajesh.karavadi $
 * $Id: PendingConfirmationsRemainder.java,v 1.3 2012/07/11 09:11:37 rajesh.karavadi Exp $
 * $Revision: 1.3 $
 * $Date: 2012/07/11 09:11:37 $
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Date;

public class PendingConfirmationsRemainder implements Comparable<PendingConfirmationsRemainder> {
    
    private String subscriberId;
    private int remaindersLeft;
    private Date lastRemainderSent;
    private Date smsReceivedTime;
    private String remainderText;
    private long smsId;
    private String sender;
    
    public PendingConfirmationsRemainder() {
        super();
    }
    
    /**
     * @param subscriberId
     * @param remaindersLeft
     * @param lastRemainderSent
     * @param smsReceivedTime
     * @param smsText
     * @param smsId
     * @param sender
     */
    public PendingConfirmationsRemainder(String subscriberId, int remaindersLeft,
            Date lastRemainderSent, Date smsReceivedTime,String smsText, long smsId,String sender) {
        super();
        this.subscriberId = subscriberId;
        this.remaindersLeft = remaindersLeft;
        this.lastRemainderSent = lastRemainderSent;
        this.smsReceivedTime = smsReceivedTime;
        this.remainderText = smsText;
        this.smsId = smsId;
        this.sender = sender;
    }
    
    public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public int getRemaindersLeft() {
		return remaindersLeft;
	}

	public void setRemaindersLeft(int remaindersLeft) {
		this.remaindersLeft = remaindersLeft;
	}

	public Date getLastRemainderSent() {
		return lastRemainderSent;
	}

	public void setLastRemainderSent(Date lastRemainderSent) {
		this.lastRemainderSent = lastRemainderSent;
	}

	public Date getSmsReceivedTime() {
		return smsReceivedTime;
	}

	public void setSmsReceivedTime(Date smsReceivedTime) {
		this.smsReceivedTime = smsReceivedTime;
	}

	public String getRemainderText() {
		return remainderText;
	}

	public void setRemainderText(String smsRemainderText) {
		this.remainderText = smsRemainderText;
	}

	public long getSmsId() {
		return smsId;
	}

	public void setSmsId(long smsId) {
		this.smsId = smsId;
	}

	@Override
    public int compareTo(PendingConfirmationsRemainder arg0) {
        return 0;
    }

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	@Override
	public String toString() {
		return "PendingConfirmationsRemainder [lastRemainderSent="
				+ lastRemainderSent + ", remainderText=" + remainderText
				+ ", remaindersLeft=" + remaindersLeft + ", sender=" + sender
				+ ", smsId=" + smsId + ", smsReceivedTime=" + smsReceivedTime
				+ ", subscriberId=" + subscriberId + "]";
	}

}
