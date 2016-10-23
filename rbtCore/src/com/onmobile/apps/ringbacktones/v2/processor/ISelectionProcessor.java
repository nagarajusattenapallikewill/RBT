package com.onmobile.apps.ringbacktones.v2.processor;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public interface ISelectionProcessor {
	
	public void startProcessing(WebServiceContext task,Subscriber subscriber);

}
