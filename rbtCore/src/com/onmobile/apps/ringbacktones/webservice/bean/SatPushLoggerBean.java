package com.onmobile.apps.ringbacktones.webservice.bean;

import java.util.Calendar;

public class SatPushLoggerBean {

	private String subscriberId;
	private String requestUrl;
	private String response;
	private String timeTaken;
	private Calendar requestSentDate;

	public Calendar getRequestSentDate() {
		return requestSentDate;
	}

	public void setRequestSentDate(Calendar requestSentDate) {
		this.requestSentDate = requestSentDate;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String request_url) {
		this.requestUrl = request_url;
	}

	public String getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(String time_taken) {
		this.timeTaken = time_taken;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
