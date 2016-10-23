package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IUpdateSubscriptionService;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISubscriptionResponse;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

public class DeactivationSubscriptionServiceResolver implements IUpdateSubscriptionService {
	private static Logger logger = Logger.getLogger(DeactivationSubscriptionServiceResolver.class);

	protected ISubscriptionResponse responseResolver;

	public void setResponseResolver(ISubscriptionResponse responseResolver) {
		this.responseResolver = responseResolver;
	}

	@Override
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId,String subscriptionClass)
			throws UserException {
		logger.info(":--> Inside Update");
		SubscriptionRequest subscriptionRequest = getSubscriptionRequestForUpdateSubscriber(msisdn, mode);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = RBTClient.getInstance()
				.deactivateSubscriber(subscriptionRequest);
		return responseResolver.prapareUpdateSubscriberResponse(subscriber, catalogSubscriptionId,
				subscriptionRequest.getResponse());
	}

	private SubscriptionRequest getSubscriptionRequestForUpdateSubscriber(String msisdn, String mode) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(msisdn);
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setIsDirectDeactivation(false);
		return subscriptionRequest;
	}

}
