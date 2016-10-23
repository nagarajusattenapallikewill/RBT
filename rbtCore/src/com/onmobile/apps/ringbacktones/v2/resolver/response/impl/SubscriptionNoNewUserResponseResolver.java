package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class SubscriptionNoNewUserResponseResolver extends SubscriptionResponseResolver

{

	public Subscription prepareGetSubscriptionResponse(String mode, String msisdn, Subscriber subscriber)
			throws UserException {
		if (subscriber != null) {
			Subscription subscription = null;
			ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			subscription = helper.convertSubscriberToSubscription(subscriber);
			if (!subscription.getStatus().equalsIgnoreCase("new_user"))
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
}
