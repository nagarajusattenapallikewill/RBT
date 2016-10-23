package com.onmobile.apps.ringbacktones.v2.util;


import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.livewiremobile.store.storefront.dto.rbt.StatusResponse;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

public interface IOperatorUtility {
	
	public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest);
	public Rbt addSubscriberConsentDownload(SelectionRequest selectionRequest);
	public String makeConsentCgUrl();
	public String makeRUrl();
	public Rbt activateSubscriber(SubscriptionRequest subscriptionRequest);
	public Set<String> getCvCircleId();
	public StatusResponse getStatusResponse(String circleId, String callerId, Map<String, String> requestParamMap,HttpServletRequest request,HttpServletResponse response) throws Exception;

}
