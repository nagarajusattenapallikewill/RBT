package com.onmobile.apps.ringbacktones.v2.resolver.request;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IUpdateSubscriptionService {
	
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId,String subscriptionClass) throws UserException;

	
	
}
