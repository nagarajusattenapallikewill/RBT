
package com.onmobile.apps.ringbacktones.v2.resolver.request;

import java.util.List;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 *
 */

public interface ISubscriptionRequest {
	
	public Subscription createSubscription(String msisdn, String serviceKey, Subscription subscriptionDto) throws UserException;
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId,String subscriptionClass) throws UserException;
	public List<Object> getAllowedSubscription(String msisdn, String mode) throws UserException;
	public Subscription getProfile(String msisdn, String mode) throws UserException;

}
