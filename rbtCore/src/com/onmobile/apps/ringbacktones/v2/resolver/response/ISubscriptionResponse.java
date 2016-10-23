package com.onmobile.apps.ringbacktones.v2.resolver.response;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

public interface ISubscriptionResponse {
	
		public Subscription prepareCreateSubscriptionResponse(AbstractOperatorUtility operatorUtility,
			Subscriber subscriber, Rbt rbt, SubscriptionRequest subscriptionRequest, String operatorName, int catalogSubscriptionId)
			throws UserException;
	
	public Subscription prepareGetSubscriptionResponse(String mode, String msisdn, Subscriber subscriber) throws UserException;

	public Subscription prapareUpdateSubscriberResponse(com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber,String catalogSubscriptionId, String response) throws UserException;

}
