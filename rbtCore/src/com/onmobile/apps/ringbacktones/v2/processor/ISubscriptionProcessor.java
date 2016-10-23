package com.onmobile.apps.ringbacktones.v2.processor;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public interface ISubscriptionProcessor {
	
	public void startProcessingProcessACT(WebServiceContext task);
	
	public void startProcessingProcessDCT(WebServiceContext task);

}
