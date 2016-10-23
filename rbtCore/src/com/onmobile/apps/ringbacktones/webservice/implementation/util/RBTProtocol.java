package com.onmobile.apps.ringbacktones.webservice.implementation.util;

import java.io.Serializable;

public class RBTProtocol implements Serializable {

	private static final long serialVersionUID = -156234763154619072L;

	private Long protocolId;
	private String subscriberId;
	private String staticText;

	public void setProtocolId(Long protocolId) {
		this.protocolId = protocolId;
	}

	public Long getProtocolId() {
		return protocolId;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getStaticText() {
		return staticText;
	}

	public void setStaticText(String staticText) {
		this.staticText = staticText;
	}

	@Override
	public String toString() {
		return "RBTProtocol [protocolId=" + protocolId + ", subscriberId="
				+ subscriberId + ", staticText=" + staticText + "]";
	}

}
