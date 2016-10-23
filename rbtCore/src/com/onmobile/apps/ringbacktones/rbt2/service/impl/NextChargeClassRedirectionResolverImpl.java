package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.processor.impl.NextChargeClassAbstractProcessor;
import com.onmobile.apps.ringbacktones.rbt2.service.INextChargeClassService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class NextChargeClassRedirectionResolverImpl implements INextChargeClassService, Constants {

	@Autowired
	private CommonValidation commonValidation;
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;

	private static Logger logger = Logger.getLogger(NextChargeClassRedirectionResolverImpl.class);

	@Override
	public NextServiceCharge getNextChargeAndServiceClass(String subscriberId, String chargeClass, String categoryID,
			String clipID, String subscriptionClass, String mode) throws UserException {
		NextServiceCharge nextServiceCharge = new NextServiceCharge();
		try {

			String subscriberStatus = "";
			SelectionRequest selectionRequest = getSelectionRequest(subscriberId, chargeClass, categoryID, clipID,
					subscriptionClass, mode);
			nextServiceCharge = RBTClient.getInstance().getNextServiceCharge(selectionRequest);

			if (!selectionRequest.getResponse().equalsIgnoreCase("success")) {
				logger.info("\n\n\t:--->Response :" + selectionRequest.getResponse());
				throw new UserException(selectionRequest.getResponse());
			}
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
			com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient
					.getInstance().getSubscriber(rbtDetailsRequest);
			logger.info("\n\n :---> clientsubscriber" + clientsubscriber.toString());
			subscriberStatus = clientsubscriber.getStatus();
			nextServiceCharge.setSubscriberStatus(subscriberStatus);
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: " + e, e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: " + e, e);
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getBeanName(),
					MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (UserException e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(),
					MessageResource.NEXT_CHARGE_CLASS_MESSAGE_FOR);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, INTERNAL_SERVER_ERROR,
					MessageResource.NEXT_CHARGE_CLASS_MESSAGE_FOR);
		}

		return nextServiceCharge;
	}

	protected SelectionRequest getSelectionRequest(String subscriberId, String chargeClass, String categoryID,
			String clipID, String subscriptionClass, String mode) throws UserException {

		SelectionRequest selectionRequest = new SelectionRequest(subscriberId);
		if (chargeClass != null)
			selectionRequest.setChargeClass(chargeClass);
		selectionRequest.setCategoryID(categoryID);
		selectionRequest.setClipID(clipID);
		if (subscriptionClass != null)
			selectionRequest.setSubscriptionClass(subscriptionClass);
		selectionRequest.setMode(mode);

		return selectionRequest;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
}
