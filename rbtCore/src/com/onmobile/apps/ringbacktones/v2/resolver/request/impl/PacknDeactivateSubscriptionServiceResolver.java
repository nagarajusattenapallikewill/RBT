package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IUpdateSubscriptionService;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISubscriptionResponse;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

public class PacknDeactivateSubscriptionServiceResolver implements IUpdateSubscriptionService {

	private IUpdateSubscriptionService deactivate;

	private String freetrial;

	public String getFreetrial() {
		return freetrial;
	}

	public void setFreetrial(String freetrial) {
		this.freetrial = freetrial;
	}

	public void setDeactivate(IUpdateSubscriptionService deactivate) {
		this.deactivate = deactivate;
	}

	private static Logger logger = Logger.getLogger(PacknDeactivateSubscriptionServiceResolver.class);

	protected ISubscriptionResponse responseResolver;

	public void setResponseResolver(ISubscriptionResponse responseResolver) {
		this.responseResolver = responseResolver;
	}

	@Override
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId,
			String subscriptionClass) throws UserException {

		logger.info("Free trial property configured in bean" + freetrial);
		SubscriptionRequest subscriptionRequest = getSubscriptionRequestForUpdateSubscriber(msisdn, mode);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = null;
		if (freetrial.equalsIgnoreCase(subscriptionClass)) {
			String freeTrialCosId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_FREE_TRIAL_COS_ID",
					"1");
			if (freeTrialCosId != null) {
				logger.info("Deactivate Pack :" + freeTrialCosId);
				subscriptionRequest.setPackCosId(Integer.parseInt(freeTrialCosId));
				subscriber = RBTClient.getInstance().deactivatePack(subscriptionRequest);
				logger.info("RBT:: DeactivatePack *** response is : " + subscriptionRequest.getResponse());

			}

		}

		else {
			logger.info("Deactivate subscriber");
			return deactivate.updateSubscriber(status, msisdn, mode, catalogSubscriptionId, subscriptionClass);
		}
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
