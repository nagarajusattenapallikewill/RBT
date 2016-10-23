package com.onmobile.apps.ringbacktones.rbt2.common;

import java.util.HashMap;
import java.util.Map;

public enum SubscriptionStatus {
	
	ACTIVE("B"), ACTIVATIONPENDING("A"), CANCELLED("x"), CANCELED("X"), DEACTIVATIONPENDING("D"), SUSPENDED("UZ"), EXPIRED("Z"),DELAYED_DEACTIVATION("UX"), GRACE("G"),
	ACTIVATIONPENDING$("N"), DEACTIVATIONPENDING$("P"), CHANGE("C"), EXPIRED$("z"), ACTIVATION_ERROR("E") ,DEACTIVATION_ERROR("F");
	
	private final String subscriptionYes;
	
	private static Map<String, SubscriptionStatus> map =
            null;

    private SubscriptionStatus(final String subscriptionYes) {
    	this.subscriptionYes = subscriptionYes;
    }
    
    static {
    	map = new HashMap<String, SubscriptionStatus>();
    	for(SubscriptionStatus subscriptionStatus : SubscriptionStatus.values()) {
    		map.put(subscriptionStatus.subscriptionYes, subscriptionStatus);
    	}
    }

    public static String getSubscriptionStatus(String subscriptionYes) {
    	SubscriptionStatus  subscriptionStatus = map.get(subscriptionYes);
    	String tempValue = subscriptionStatus.toString();
    	if(tempValue.indexOf('$') != -1) {
    		tempValue = tempValue.replace("$", "");
    	}
    	return tempValue.toLowerCase();
    }

}


