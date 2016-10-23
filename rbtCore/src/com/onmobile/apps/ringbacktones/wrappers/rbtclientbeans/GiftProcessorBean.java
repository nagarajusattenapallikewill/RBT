package com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans;

import java.util.Date;

public class GiftProcessorBean {
	
	private String subId;
	private String gifterId;
	private Date sentTime;
	private String callerId=null;
	private String catId="-1";
	private String toneId="-1";
	private boolean isPrepaid=true;
	
public boolean isPrepaid() {
		return isPrepaid;
}
public void setPrepaid(boolean isPrepaid) {
		this.isPrepaid = isPrepaid;
}
public Date getSentTime() {
	return sentTime;
}
public void setSentTime(Date sentTime) {
	this.sentTime = sentTime;
}
public String getCallerId() {
	return callerId;
}
public void setCallerId(String callerId) {
	this.callerId = callerId;
}
public String getCatId() {
	return catId;
}
public void setCatId(String catId) {
	this.catId = catId;
}
public String getToneId() {
	return toneId;
}
public void setToneId(String toneId) {
	this.toneId = toneId;
}
	public String getGifterId() {
		return gifterId;
	}
	public void setGifterId(String gifterId) {
		this.gifterId = gifterId;
	}
	public String getSubId() {
		return subId;
	}
	public void setSubId(String subId) {
		this.subId = subId;
	}
	
}
