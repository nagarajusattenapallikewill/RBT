package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset.Entry;
import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO.OfferType;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.DefaultOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

/**
 * 
 * @author koyel.mahata
 *
 */
 

public class SubscriptionRequestResolver extends AbstractSubscriptionRequestResolver {

	private static Logger logger = Logger.getLogger(SubscriptionRequestResolver.class);

	@Override
	public Subscription createSubscription(String msisdn, String mode,Subscription subscriptionDto) throws UserException {
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(
				new RbtDetailsRequest(msisdn));
		SubscriptionRequest subscriptionRequest = null;
		String operatorName = null;
		AbstractOperatorUtility operatorUtility = null;
		Rbt rbt = null;
		if (subscriber != null) {
			subscriptionRequest = getSubscriptionRequestForCreateSubscription(subscriber, mode, subscriptionDto);
			operatorName = ServiceUtil.getOperatorName(subscriber);
			operatorUtility = (AbstractOperatorUtility) getConsentObject(operatorName);
			rbt = operatorUtility.activateSubscriber(subscriptionRequest);
		}
		return responseResolver.prepareCreateSubscriptionResponse(operatorUtility, subscriber, rbt, 
				subscriptionRequest, operatorName, subscriptionDto.getCatalogSubscriptionID());
	}

	private SubscriptionRequest getSubscriptionRequestForCreateSubscription(Subscriber subscriber, String mode, Subscription subscriptionDto) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());

		// RBT-18792 Upgradation request for active user->User is not activated
		if (subscriptionDto != null && subscriptionDto.getSrvKey() != null) {
			String dtocAppClass = RBTParametersUtils.getParamAsString(
					iRBTConstant.COMMON, "DTOC_FREETRIAL_APP_SERVICE_CLASS", null);
			boolean allowUpgradation = true;
			if (dtocAppClass != null) {
				String[] dtocServiceClassArr = dtocAppClass.split(",");
				if (dtocServiceClassArr.length > 0) {
					List<String> serviceClass = Arrays.asList(dtocServiceClassArr);
					if (serviceClass.contains(subscriptionDto.getSrvKey())) {
						allowUpgradation = false;
					}
				}
			}

			if (!allowUpgradation && Utility.isUserActive(subscriber.getStatus())
					&& (subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.TRADITIONAL.getDefaultValue()) 
							|| subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue()))) {
				subscriptionRequest.setSubscriptionClass(subscriptionDto.getSrvKey());
			} else if (Utility.isUserActive(subscriber.getStatus())) {
				subscriptionRequest.setRentalPack(subscriptionDto.getSrvKey());
			} else {
				subscriptionRequest.setSubscriptionClass(subscriptionDto.getSrvKey());
			}
		}
		/*if(Utility.isUserActive(subscriber.getStatus()) && (subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue())
				|| subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue()))) {
			subscriptionRequest.setRentalPack(subscriptionDto.getSrvKey());
		} else {
			subscriptionRequest.setSubscriptionClass(subscriptionDto.getSrvKey());				
		}*/
		subscriptionRequest.setMode(mode);
		try {
			Offer offer = getOfferFromServiceKey(subscriber.getSubscriberID(), mode, subscriptionDto.getSrvKey(), OfferType.subscription);
			if(offer != null)
				subscriptionRequest.setOfferID(offer.getOfferID());
		} catch (UserException e) {
			logger.info("Exception occured while getting offfer id...");
		}
		
		if (subscriptionDto.getExtraInfo() != null && subscriptionDto.getExtraInfo().size() > 0) {
			HashMap<String, String> subExtraInfo = new HashMap<String, String>(subscriptionDto.getExtraInfo());
			subscriptionRequest.setUserInfoMap(subExtraInfo);
		}
		return subscriptionRequest;

	}

	private IOperatorUtility getConsentObject(String operatorName) {

		IOperatorUtility operatorutility = null;

		try {
			operatorutility = (IOperatorUtility) ConfigUtil.getBean(operatorName.toLowerCase());
		} catch(NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e.getMessage()+", returning Default ConsentUtility Object");
			operatorutility = new DefaultOperatorUtility();
		}
		return operatorutility;
	}

}
