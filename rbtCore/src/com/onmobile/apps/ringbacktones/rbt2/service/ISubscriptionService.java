
package com.onmobile.apps.ringbacktones.rbt2.service;

import java.util.List;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 *
 */

public interface ISubscriptionService {
	
	public Subscription createSubscription(String msisdn, String serviceKey, String mode, String catalogSubscriptionId) throws UserException;
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId) throws UserException;
	public List<Object> getAllowedSubscription(String msisdn, String mode) throws UserException;
	public Subscription getProfile(String msisdn, String mode) throws UserException;

}
