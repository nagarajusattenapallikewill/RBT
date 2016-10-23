package com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans;

import java.util.HashMap;


public class SelectionRequestBean {
private String subscriberId;
private String callerId=null;
private String catId;
private String toneId="-1";
private Boolean useUIChargeClass = null;
private boolean isPrepaid=true;
private int status=1;
private String selInterval;
private String setInLoop="false";
private String chargeClass;
private String freeOption="false";
private String setRBTOption;
private String futureDate=null;
private String dayOfTheWeek="-1";
private int fromTimeOfTheDay=0;
private int toTimeOfTheDay=23;
private int timeOfTheDayStatus=80;
private int futureDateStatus=95;
private int dayOfTheWeekStatus=75;
private String profileHour;
private String cricpack;
private String subscriptionClass=null;
private String mmContext = null;
private HashMap<String,String> extraInfo=null;
private HashMap<String, String> subscriberInfoMap = null;
private String subOfferId = null;
private String selOfferId = null;


public String getSubscriptionClass() {
	return subscriptionClass;
}
public void setSubscriptionClass(String subscriptionClass) {
	this.subscriptionClass = subscriptionClass;
}
public HashMap<String,String> getExtraInfo() {
	return extraInfo;
}
public Boolean getUseUIChargeClass() {
	return useUIChargeClass;
}
public void setUseUIChargeClass(Boolean useUIChargeClass) {
	this.useUIChargeClass = useUIChargeClass;
}
public HashMap<String, String> getSubscriberExtraInfo() {
	return subscriberInfoMap;
}
public void setExtraInfo(HashMap<String,String> extraInfo) {
	this.extraInfo = extraInfo;
}
public void setSubscriberExtraInfo(HashMap<String, String> subscriberInfoMap) {
	this.subscriberInfoMap = subscriberInfoMap;
}
public String getCricpack() {
	return cricpack;
}
public void setCricpack(String cricpack) {
	this.cricpack = cricpack;
}
public String getProfileHour() {
	return profileHour;
}
public void setProfileHour(String profileHour) {
	this.profileHour = profileHour;
}
public String getFutureDate() {
	return futureDate;
}
public void setFutureDate(String futureDate) {
	this.futureDate = futureDate;
}
public int getTimeOfTheDayStatus() {
	return timeOfTheDayStatus;
}
public void setTimeOfTheDayStatus(int timeOfTheDayStatus) {
	this.timeOfTheDayStatus = timeOfTheDayStatus;
}
public int getFutureDateStatus() {
	return futureDateStatus;
}
public void setFutureDateStatus(int futureDateStatus) {
	this.futureDateStatus = futureDateStatus;
}
public int getDayOfTheWeekStatus() {
	return dayOfTheWeekStatus;
}
public void setDayOfTheWeekStatus(int dayOfTheWeekStatus) {
	this.dayOfTheWeekStatus = dayOfTheWeekStatus;
}
public String getSetInLoop() {
	return setInLoop;
}

public String getDayOfTheWeek() {
	return dayOfTheWeek;
}
public void setDayOfTheWeek(String dayOfTheWeek) {
	this.dayOfTheWeek = dayOfTheWeek;
}
public int getFromTimeOfTheDay() {
	return fromTimeOfTheDay;
}
public void setFromTimeOfTheDay(int fromTimeOfTheDay) {
	this.fromTimeOfTheDay = fromTimeOfTheDay;
}
public int getToTimeOfTheDay() {
	return toTimeOfTheDay;
}
public void setToTimeOfTheDay(int toTimeOfTheDay) {
	this.toTimeOfTheDay = toTimeOfTheDay;
}
public String getSetRBTOption() {
	return setRBTOption;
}
public void setSetRBTOption(String setRBTOption) {
	this.setRBTOption = setRBTOption;
}
public String getFreeOption() {
	return freeOption;
}
public void setFreeOption(String freeOption) {
	this.freeOption = freeOption;
}
public String getChargeClass() {
	return chargeClass;
}
public void setChargeClass(String chargeClass) {
	this.chargeClass = chargeClass;
}
public String getSubscriberId() {
	return subscriberId;
}
public void setSubscriberId(String subscriberId) {
	this.subscriberId = subscriberId;
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
public boolean isPrepaid() {
	return isPrepaid;
}
public void setPrepaid(boolean isPrepaid) {
	this.isPrepaid = isPrepaid;
}
public int getStatus() {
	return status;
}
public void setStatus(int status) {
	this.status = status;
}
public String getSelInterval() {
	return selInterval;
}
public void setSelInterval(String selInterval) {
	this.selInterval = selInterval;
}
public String isSetInLoop() {
	return setInLoop;
}
public void setSetInLoop(String setInLoop) {
	this.setInLoop = setInLoop;
}

public String getMmContext() {
	return mmContext;
}
public void setMmContext(String mmContext) {
	this.mmContext = mmContext;
}
public String getSubOfferId() {
	return subOfferId;
}
public void setSubOfferId(String subOfferId) {
	this.subOfferId = subOfferId;
}
public String getSelOfferId() {
	return selOfferId;
}
public void setSelOfferId(String selOfferId) {
	this.selOfferId = selOfferId;
}



}
