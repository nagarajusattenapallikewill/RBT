package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Map;

public class MobileAppRegistration {

	private String smsText = null;
	private Map<String, String> registerIdSubscriberIdMap = null;

	public String getSmsText() {
		return smsText;
	}
	public void setSmsText(String smsText) {
		this.smsText = smsText;
	}
	public Map<String, String> getRegisterIdSubscriberIdMap() {
		return registerIdSubscriberIdMap;
	}
	public void setRegisterIdSubscriberIdMap(Map<String, String> registerIdSubscriberIdMap) {
		this.registerIdSubscriberIdMap = registerIdSubscriberIdMap;
	}
	@Override
	public String toString() {
		return "MobileAppRegistration [smsText=" + smsText
				+ ", registerIdSubscriberIdMap=" + registerIdSubscriberIdMap
				+ "]";
	}
}
