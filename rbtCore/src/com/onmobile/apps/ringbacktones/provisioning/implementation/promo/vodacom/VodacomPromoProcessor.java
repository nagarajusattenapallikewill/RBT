package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.vodacom;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
/**
 * 
 * @author rony.gregory
 */
public class VodacomPromoProcessor extends PromoProcessor {

	public VodacomPromoProcessor() throws RBTException {
		super();
	}

	@Override
	public Subscriber processActivation(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		//<RBT-10742>
		String requestRbtType = task.getString(Constants.param_RBTTYPE);
		String status = subscriber.getStatus();
		String subscriptionClass = task.getString(param_SUBSCRIPTION_CLASS);
		if (requestRbtType != null && requestRbtType.equals("1")) {	
			logger.info("ADRBT request receieved.");
			if (status.equals(WebServiceConstants.NEW_USER) || status.equals(WebServiceConstants.DEACTIVE)) {
				logger.info("Subscriber is either a new user or a deactive user.");
				task.setObject(param_subclass, subscriptionClass);
			} else {
				if (task.containsKey(param_SUBSCRIPTION_CLASS)) {
					task.setObject(param_ADVANCE_RENTAL_CLASS, subscriptionClass);
				}
				task.setObject(param_actby, task.getString(param_ACTIVATED_BY));
				task.setObject(param_rbttype, requestRbtType);
				logger.info("Subscriber to be upgraded.");
				return processUpgradeSubscription(task);
			}
		}
		//</RBT-10742>
		return super.processActivation(task);
	}
}
