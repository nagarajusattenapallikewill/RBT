package com.onmobile.apps.ringbacktones.webservice.client;

import java.util.HashMap;
import java.util.Map;

public class ConnectorHandler {
	private Map<String, String> b2bUserInfo = null;
	private Connector connector = null;

	public ConnectorHandler() {
		b2bUserInfo = new HashMap<String, String>();
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public Map<String, String> getB2bUserInfo() {
		return b2bUserInfo;
	}

	public void setB2bUserInfo(Map<String, String> b2bUserInfo) {
		this.b2bUserInfo = b2bUserInfo;
	}

}
