package com.onmobile.apps.ringbacktones.webservice.implementation.util;


public class RBTProtocolBean {
	private Long protocolNum;
	private String subscriberId;
	private String transactionType;
	private String requestTime;
	
	public Long getProtocolNum() {
		return protocolNum;
	}
	public void setProtocolNum(Long protocolNum) {
		this.protocolNum = protocolNum;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
	
}
