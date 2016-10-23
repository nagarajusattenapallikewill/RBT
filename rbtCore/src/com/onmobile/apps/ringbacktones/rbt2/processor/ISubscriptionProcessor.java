package com.onmobile.apps.ringbacktones.rbt2.processor;

import java.util.List;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

/**
 * 
 * @author md.alam
 *
 */

public interface ISubscriptionProcessor {
	
	/**
	 * It processes the subscription request
	 * @author md.alam
	 * @param msisdn
	 * @param serviceKey
	 * @param mode
	 * @return Subscriber
	 * @throws UserException
	 */
	public <T extends Object> T createSubscription(String msisdn, String serviceKey, String mode) throws UserException;
	public <T extends Object> T processUpgradeOrDowngrade(Subscriber subscriber, String serviceKey, String mode) throws UserException;
	public <T extends Object> T deactivateSubscriber(String msisdn, String mode, boolean isDelayDeactivation) throws UserException;
	public <T extends Object> T suspendOrResumeSubscriber(String msisdn, String mode, boolean isSuspend) throws UserException;
	public List<Object> getAllowedSubscription(String msisdn, String mode) throws UserException;
	

}
