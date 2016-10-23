package com.onmobile.apps.ringbacktones.rbt2.logger.dto;


public class LoggerDTO {
	
	private Long subscriberId = null;
	private String circleId = "NA";
	private Long toneId = null;
	private String tonename = "NA";
	private String wavfileName = "NA";
	private Long categoryId = null;
	private String categorytype = "NA";
	private String udpId = "NA";
	private String status = "NA";
	private int fromtime = 0;
	private int totime =0;
	private String callerId = "NA";
	private String interval = "NA";
	private String refId = "NA";
	private String operatorName = "NA";
	private String countryName = "NA";
	private String subscriberStatus = "NA";
	private String subscriberSrvKey = "NA";
	private String songSrvKey = "NA";
	private String responesStatus = null;
	private int clipid = 0;
	private String fulltrackName = "NA";
	private String starttime = "NA";

	
	
	public Long getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(Long subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getCircleId() {
		return circleId;
	}
	public void setCircleId(String circleId) {
		this.circleId = circleId;
	}
	public Long getToneId() {
		return toneId;
	}
	public void setToneId(Long toineId) {
		this.toneId = toineId;
	}
	public String getTonename() {
		return tonename;
	}
	public void setTonename(String tonename) {
		this.tonename = tonename;
	}
	public String getWavfileName() {
		return wavfileName;
	}
	public void setWavfileName(String wavfileName) {
		this.wavfileName = wavfileName;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategorytype() {
		return categorytype;
	}
	public void setCategorytype(String categorytype) {
		this.categorytype = categorytype;
	}
	public String getUdpId() {
		return udpId;
	}
	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getFromtime() {
		return fromtime;
	}
	public void setFromtime(int fromtime) {
		this.fromtime = fromtime;
	}
	public int getTotime() {
		return totime;
	}
	public void setTotime(int totime) {
		this.totime = totime;
	}
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getSubscriberStatus() {
		return subscriberStatus;
	}
	public void setSubscriberStatus(String subscriberStatus) {
		this.subscriberStatus = subscriberStatus;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getSubscriberSrvKey() {
		return subscriberSrvKey;
	}
	public void setSubscriberSrvKey(String subscriberSrvKey) {
		this.subscriberSrvKey = subscriberSrvKey;
	}
	public String getSongSrvKey() {
		return songSrvKey;
	}
	public void setSongSrvKey(String songSrvKey) {
		this.songSrvKey = songSrvKey;
	}
	public String getResponesStatus() {
		return responesStatus;
	}
	public void setResponesStatus(String responesStatus) {
		this.responesStatus = responesStatus;
	}
	
	public void setDefaultValues(){
		subscriberId = null;
		circleId = "NA";
		toneId = null;
		tonename = "NA";
		wavfileName = "NA";
		categoryId = null;
		categorytype = "NA";
		udpId = "NA";
		status = "NA";
		fromtime = 0;
		totime =0;
		callerId = "NA";
		interval = "NA";
		refId = "NA";
		operatorName = "NA";
		countryName = "NA";
		subscriberStatus = "NA";
		subscriberSrvKey = "NA";
		songSrvKey = "NA";
		responesStatus = null;
	}
	public int getClipid() {
		return clipid;
	}
	public void setClipid(int clipid) {
		this.clipid = clipid;
	}
	public String getFulltrackName() {
		return fulltrackName;
	}
	public void setFulltrackName(String fulltrackName) {
		this.fulltrackName = fulltrackName;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	
}
