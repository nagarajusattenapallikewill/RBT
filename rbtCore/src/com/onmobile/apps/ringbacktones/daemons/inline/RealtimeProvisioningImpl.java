package com.onmobile.apps.ringbacktones.daemons.inline;

import org.springframework.beans.factory.annotation.Autowired;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class RealtimeProvisioningImpl implements IProvisioning {
	@Autowired
	private SMInlineHelper smHelper;
	
	public RealtimeProvisioningImpl() {
	}

	@Override
	public void provision(Object obj, WebServiceContext parameters) throws Throwable {
	}

	private String provisionSelAct() {
		return null;
	}
	
	private String provisionSelDct() {
		return null;
	}
	
	private String provisionBaseAct() {
		return null;
	}
	
	private String provisionBasDct() {
		return null;
	}
}
