package com.onmobile.apps.ringbacktones.features.airtel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.implementation.airtel.AirtelRBTProcessor;

public class UserSelectionRestrictionBasedOnSubClass {

	private Subscriber subscriber;
	private List<String> blockedSubClassList;
	private List modesList;
	protected ParametersCacheManager parametersCacheManager = null;
	private static Logger logger = Logger
			.getLogger(UserSelectionRestrictionBasedOnSubClass.class);

	public UserSelectionRestrictionBasedOnSubClass(String notSendSmsForModes) {
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		String subscriptionClass = parametersCacheManager.getParameterValue(
				iRBTConstant.COMMON, "BLOCKED_SUBSCRIPTION_CLASS", "");
		logger.debug("List of Blocked SubscriptionClass Configured "
				+ subscriptionClass);
		logger.debug("List of modes, sms not to be sent " + notSendSmsForModes);
		if (subscriptionClass != null) {
			blockedSubClassList = Arrays.asList(subscriptionClass.split(","));
		} else {
			logger.debug("COMMON, BLOCKED_SUBSCRIPTION_CLASS PARAMETER NOT CONFIGURED, Hence considering the List to be empty ");
			blockedSubClassList = new ArrayList<String>();
		}
		modesList = Arrays.asList(notSendSmsForModes.split(","));
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	public List getBlockedSubClassList() {
		return blockedSubClassList;
	}

	// are all method by def static
	public boolean restrictUserSelection() {
		if (subscriber != null
				&& subscriber.subYes() != null
				&& (subscriber.subYes().equals("B")
						|| subscriber.subYes().equals("A") || subscriber
						.subYes().equals("N"))
				&& isSubscriptionClassRestricted(subscriber.subscriptionClass())) {
			return true;
		}
		return false;
	}

	public boolean isSubscriptionClassRestricted(String subscriptionClass) {
		if (subscriptionClass != null
				&& blockedSubClassList.contains(subscriptionClass)) {
			logger.debug("Service Key " + subscriptionClass
					+ " is Blocked For New Selection.");
			return true;
		}
		return false;
	}

	public boolean isSmsToBeSentForMode(String mode) {
		if (mode != null && modesList != null && modesList.contains(mode)) {
			return false;
		}
		return true;
	}

}
