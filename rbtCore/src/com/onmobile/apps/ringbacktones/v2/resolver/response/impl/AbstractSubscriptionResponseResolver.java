package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISubscriptionResponse;

public abstract class AbstractSubscriptionResponseResolver implements ISubscriptionResponse,Constants {
	
	@Autowired
	protected ResponseErrorCodeMapping errorCodeMapping;

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

}
