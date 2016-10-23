package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.rbt2.bean.ConsentProcessBean;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelComvivaUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class SubscriptionResponseResolver extends AbstractSubscriptionResponseResolver {

	Logger logger = Logger.getLogger(SubscriptionResponseResolver.class);

	@Override
	public Subscription prepareCreateSubscriptionResponse(AbstractOperatorUtility operatorUtility,
			Subscriber subscriber, Rbt rbt, SubscriptionRequest subscriptionRequest, String operatorName,
			int catalogSubscriptionId) throws UserException {
		Subscription subscription = null;
		if (subscriber == null) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, INTERNAL_SERVER_ERROR, null);
		}
		if (subscriptionRequest != null && subscriptionRequest.getResponse().equalsIgnoreCase(SUCCESS)) {
			ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			subscription = helper.convertSubscriberToSubscription(subscriber);
			String cgUrl = makeConsentCGUrl(operatorUtility, rbt, subscriptionRequest, operatorName, subscriber);
			String rUrl = makeRUrl(operatorUtility);
			ThirdPartyConsent thirdPartyConsent = buildThirdPartyConsent(operatorUtility, cgUrl, rUrl);
			if (thirdPartyConsent != null)
				subscription.setThirdpartyconsent(thirdPartyConsent);
		} else {
			ServiceUtil.throwCustomUserException(errorCodeMapping, "subscriber_" + subscriptionRequest.getResponse(),
					null);
		}
		// Added for RBT-17405 Create subscription
		// api->"catalog_subscription_id" is always returned as 0 in response
		subscription.setCatalogSubscriptionID(catalogSubscriptionId);
		return subscription;

	}

	@Override
	public Subscription prepareGetSubscriptionResponse(String mode, String msisdn, Subscriber subscriber)
			throws UserException {
		if (subscriber != null) {
			Subscription subscription = null;
			ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			subscription = helper.convertSubscriberToSubscription(subscriber);
			return subscription;
		} else {
			WebServiceContext task = new WebServiceContext();
			task.put("subscriberID", msisdn);
			task.put("mode", mode);
			SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
			if (null == subscriberDetail || !subscriberDetail.isValidSubscriber()) {
				logger.error(SUB_DONT_EXIST);
				ServiceUtil.throwCustomUserException(errorCodeMapping, SUB_DONT_EXIST,
						MessageResource.SUB_DONT_EXIST_MESSAGE);
			}
		}
		return null;
	}

	private String makeConsentCGUrl(AbstractOperatorUtility operatorUtility, Rbt rbt,
			SubscriptionRequest subscriptionRequest, String operatorName, Subscriber subscriber) {
		String cgUrl = null;
		if (rbt != null) {
			Consent consent = rbt.getConsent();
			ConsentProcessBean consentProcessBean = buildConsentProcessBean(consent, subscriptionRequest, subscriber);

			if (consentProcessBean != null) {
				if (consent.getClass() == ComvivaConsent.class && operatorUtility.getClass() == AirtelUtility.class) {
					operatorUtility = (AbstractOperatorUtility) getComvivaConsentUtlityObject(operatorName);
					if (operatorUtility instanceof AirtelComvivaUtility) {
						String[] arr = subscriber.getCircleID().trim().split("_");
						String circleId = arr.length == 2 ? arr[1] : arr[0];
						consentProcessBean.setCircleID(circleId);
					}
				}

				operatorUtility.setOperatorName(operatorName.toLowerCase());
				operatorUtility.setConsentProcessBean(consentProcessBean);
				cgUrl = operatorUtility.makeConsentCgUrl();
			}
		}
		return cgUrl;
	}

	private String makeRUrl(AbstractOperatorUtility operatorUtility) {
		String rUrl = operatorUtility.makeRUrl();
		return rUrl;
	}

	private ThirdPartyConsent buildThirdPartyConsent(AbstractOperatorUtility operatorUtility, String cgUrl,
			String rUrl) {
		ThirdPartyConsent thirdPartyConsent = null;
		if (cgUrl != null) {
			thirdPartyConsent = new ThirdPartyConsent();
			thirdPartyConsent.setThirdPartyUrl(cgUrl);
			thirdPartyConsent.setReturnUrl(rUrl);
			thirdPartyConsent.setId(operatorUtility.getTransId());
		}
		return thirdPartyConsent;
	}

	private ConsentProcessBean buildConsentProcessBean(Consent consent, SubscriptionRequest subscriptionRequest,
			Subscriber subscriber) {
		if (consent == null)
			return null;
		ConsentProcessBean consentProcessBean = new ConsentProcessBean();
		consentProcessBean.setConsent(consent);
		consentProcessBean.setResponse(subscriptionRequest.getResponse());
		consentProcessBean.setSubscriberId(subscriber.getSubscriberID());

		return consentProcessBean;
	}

	private IOperatorUtility getComvivaConsentUtlityObject(String operatorName) {
		if (operatorName != null) {
			if (operatorName.toUpperCase().startsWith("AIRTEL")) {
				logger.debug("Returning AirtelConsentUtility.");
				return new AirtelComvivaUtility();
			}
		}
		logger.debug("Returning AirtelConsentUtility.");
		return new AirtelUtility();
	}

	@Override
	public Subscription prapareUpdateSubscriberResponse(Subscriber subscriber, String catalogSubscriptionId,
			String response) throws UserException {
		Subscription subscription = null;
		if (subscriber == null) {
			if(response.equalsIgnoreCase("pack_already_deactive"))
			{
				ServiceUtil.throwCustomUserException(errorCodeMapping, PACK_ALREADY_DEACTIVE, null);
			}
			else if (response.equalsIgnoreCase("already_delay_deact"))
			{
				ServiceUtil.throwCustomUserException(errorCodeMapping, PACK_ALREADY_DELAY_DEACTIVE, null);
			}
			else if (response.equalsIgnoreCase("user_not_exists"))
			{
				ServiceUtil.throwCustomUserException(errorCodeMapping,USER_NOT_EXISTS, null);
			}
			
			ServiceUtil.throwCustomUserException(errorCodeMapping, INTERNAL_SERVER_ERROR, null);
		}
		if (response.equalsIgnoreCase(SUCCESS)) {
			ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			if (subscriber != null)
				subscription = helper.convertSubscriberToSubscription(subscriber);
		} else {
			ServiceUtil.throwCustomUserException(errorCodeMapping, "subscriber_" + response, null);
		}
		subscription.setCatalogSubscriptionID(Integer.parseInt(catalogSubscriptionId));
		return subscription;
	}

}
