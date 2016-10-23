package com.onmobile.apps.ringbacktones.webservice.bean;

import java.util.Calendar;

public class LoggerBean {
	private String subscriberId;
	private String requestUrl;
	private String response;
	private int responseCode;
	private String timeTaken;
	private Calendar requestSentDate;
	private String timestamp;
	private String ldapResponse;
	private String tefResponse;
	private String deactivationMode;
	private String deactivationStatus;

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(String timeTaken) {
		this.timeTaken = timeTaken;
	}

	public Calendar getRequestSentDate() {
		return requestSentDate;
	}

	public void setRequestSentDate(Calendar requestSentDate) {
		this.requestSentDate = requestSentDate;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getLdapResponse() {
		return ldapResponse;
	}

	public void setLdapResponse(String ldapResponse) {
		this.ldapResponse = ldapResponse;
	}

	public String getTefResponse() {
		return tefResponse;
	}

	public void setTefResponse(String tefResponse) {
		this.tefResponse = tefResponse;
	}

	public String getDeactivationMode() {
		return deactivationMode;
	}

	public void setDeactivationMode(String deactivationMode) {
		this.deactivationMode = deactivationMode;
	}

	public String getDeactivationStatus() {
		return deactivationStatus;
	}

	public void setDeactivationStatus(String deactivationStatus) {
		this.deactivationStatus = deactivationStatus;
	}

	@Override
	public String toString() {
		return "LoggerBean [subscriberId=" + subscriberId + ", requestUrl=" + requestUrl + ", response=" + response
				+ ", timeTaken=" + timeTaken + ", requestSentDate=" + requestSentDate + ", reponseCode=" + responseCode
				+ "]";
	}
}