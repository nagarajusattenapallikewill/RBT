package com.onmobile.apps.ringbacktones.ussd.airtel;

public class AirtelUSSDSession {

	private long sessionCreatedTime = 0L;
	
	private long lastAccessedTime = 0L;
	
	private boolean isSessionActive = false;
	
	private String subscriberId = null;
	
	public AirtelUSSDSession(String subscriberId) {
		sessionCreatedTime = System.currentTimeMillis();
		lastAccessedTime = sessionCreatedTime;
		isSessionActive = true;
		this.subscriberId = subscriberId;
	}
	
	public long getSessionCreatedTime() {
		return sessionCreatedTime;
	}
	
	public void setSessionCreatedTime(long sessionCreatedTime) {
		this.sessionCreatedTime = sessionCreatedTime;
	}
	
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}
	
	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}
	
	public boolean isSessionActive() {
		return isSessionActive;
	}
	
	public void setSessionActive(boolean isSessionActive) {
		this.isSessionActive = isSessionActive;
	}
}
