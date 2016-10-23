package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.ActivateSubscriber;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.DeactivateSubscriber;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SubscriberDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SubscriberResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Subscriber_type0;

public class SubscriberDOSkeletonImpl extends SubscriberDOSkeleton {
	Logger logger = Logger.getLogger(SubscriberDOSkeletonImpl.class);

	@Override
	public SubscriberResponse activateSubscriber(
			ActivateSubscriber activateSubscriber3) {
		SubscriptionRequest request = new SubscriptionRequest(
				String.valueOf(activateSubscriber3.getSubscriberID()));
		request.setMode(activateSubscriber3.getMode());
		request.setModeInfo(activateSubscriber3.getModeInfo());
		request.setSubscriptionClass(activateSubscriber3.getSubscriptionClass());
		int offerId=activateSubscriber3.getOfferID();
		request.setOfferID((offerId>0)?String.valueOf(offerId):"");
		logger.info("SubscriptionRequest :" + request);
		return activateOrDeactivateSubscriber(request,
				activateSubscriber3.getAction());
	}

	@Override
	public SubscriberResponse deactivateSubscriber(
			DeactivateSubscriber deactivateSubscriber1) {
		SubscriptionRequest request = new SubscriptionRequest(
				String.valueOf(deactivateSubscriber1.getSubscriberID()));
		request.setMode(deactivateSubscriber1.getMode());
		request.setModeInfo(deactivateSubscriber1.getModeInfo());
		return activateOrDeactivateSubscriber(request,
				deactivateSubscriber1.getAction());
	}

	private SubscriberResponse activateOrDeactivateSubscriber(
			SubscriptionRequest request, String action) {
		Rbt_type0 rbtType = new Rbt_type0();
		SubscriberResponse response = new SubscriberResponse();
		Document document = RBTClient.getInstance().subscriberAction(request,
				action);
		Element responseElement = (Element) document.getElementsByTagName(
				"response").item(0);
		String responseMsg = responseElement.getTextContent();
		logger.info("Webservice response: " + responseMsg);
		if (!responseMsg.equalsIgnoreCase("success")) {
			rbtType.setResponse(responseMsg);
			response.setRbt(rbtType);
			return response;
		}
		Subscriber_type0 subscriberType = new Subscriber_type0();
		try {
			Element subscriberElement = (Element) document
					.getElementsByTagName("subscriber").item(0);
			subscriberType.setAccessCount(subscriberElement
					.getAttribute("access_count"));
			subscriberType.setActivatedBy(subscriberElement
					.getAttribute("activated_by"));
			subscriberType.setCanAllow(subscriberElement
					.getAttribute("can_allow"));
			subscriberType.setCircleId(subscriberElement
					.getAttribute("circle_id"));
			if (subscriberElement.hasAttribute("cos_id")) {
				subscriberType.setCosId(Integer
						.parseInt(subscriberElement
								.getAttribute("cos_id")));
			}
			if (subscriberElement.hasAttribute("is_prepaid")) {
				subscriberType.setIsPrepaid(subscriberElement
						.getAttribute("is_prepaid"));
			}
			if (subscriberElement.hasAttribute("is_valid_prefix")) {
				subscriberType.setIsValidPrefix(subscriberElement
						.getAttribute("is_valid_prefix"));
			}
			if (subscriberElement.hasAttribute("language")) {
				subscriberType.setLanguage(subscriberElement
						.getAttribute("language"));
			}
			if (subscriberElement.hasAttribute("next_charging_date")) {
				subscriberType.setNextBillingDate(Long
						.parseLong(subscriberElement
								.getAttribute("next_charging_date")));
			}
			if (subscriberElement.hasAttribute("ref_id")) {
				subscriberType.setRefId(subscriberElement
						.getAttribute("ref_id"));
			}
			if (subscriberElement.hasAttribute("status")) {
				subscriberType.setStatus(subscriberElement
						.getAttribute("status"));
			}
			if (subscriberElement.hasAttribute("subscriber_id")) {
				subscriberType.setSubscriebrId(Long
						.parseLong(subscriberElement
								.getAttribute("subscriber_id")));
			}
			if (subscriberElement.hasAttribute("subscription_class")) {
				subscriberType.setSubscriptionClass(subscriberElement
						.getAttribute("subscription_class"));
			}
			if (subscriberElement.hasAttribute("user_type")) {
				subscriberType.setUserType(subscriberElement
						.getAttribute("user_type"));
			}
			if (subscriberElement.hasAttribute("VOLUNTARY")) {
				subscriberType.setVoluntary(subscriberElement
						.getAttribute("VOLUNTARY"));
			}
			if (subscriberElement.hasAttribute("activation_date")) {
				subscriberType.setActivation_date(subscriberElement
						.getAttribute("activation_date"));
			}
			if (subscriberElement.hasAttribute("start_date")) {
				subscriberType.setStart_date(subscriberElement
						.getAttribute("start_date"));
			}
			if (subscriberElement.hasAttribute("end_date")) {
				subscriberType.setEnd_date(subscriberElement
						.getAttribute("end_date"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		rbtType.setSubscriber(subscriberType);
		rbtType.setResponse(responseMsg);
		response.setRbt(rbtType);
		logger.info("Response: " + response);
		return response;
	}

}
