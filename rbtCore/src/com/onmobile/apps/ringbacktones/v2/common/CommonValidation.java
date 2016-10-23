package com.onmobile.apps.ringbacktones.v2.common;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;


public class CommonValidation {
	
	private static Logger logger = Logger.getLogger(CommonValidation.class);
	
	private RBTDBManager rbtDBManager = null;
	
	public boolean isSubscriberActive(String subscriberID) {
		
		rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		boolean status = false;

		if (subscriber != null
				&& (rbtDBManager.isSubscriberActivated(subscriber)
				|| rbtDBManager.isSubscriberActivationPending(subscriber))) {
			status = true;
		}
		logger.info("isSubscriberActive is returning :"+status);
		return status;
	}
	

}
