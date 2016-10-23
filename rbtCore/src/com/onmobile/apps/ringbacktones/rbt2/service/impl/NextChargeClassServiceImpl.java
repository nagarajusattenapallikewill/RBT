package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
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
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;

public class NextChargeClassServiceImpl implements INextChargeClassService,
		Constants {

	@Autowired
	private CommonValidation commonValidation;
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;

	private static Logger logger = Logger
			.getLogger(NextChargeClassServiceImpl.class);

	@Override
	public NextServiceCharge getNextChargeAndServiceClass(String subscriberId,
			String chargeClass, String categoryID, String clipID,
			String subscriptionClass, String mode) throws UserException {
		NextServiceCharge nextServiceCharge = new NextServiceCharge();
		try {
			NextChargeClassAbstractProcessor nextChargeClassAbstractProcessor = (NextChargeClassAbstractProcessor) ConfigUtil
					.getBean(BeanConstant.CHARGE_CLASS_PROCESSOR);
			subscriberId = RBTDBManager.getInstance().subID(subscriberId);
			nextServiceCharge = nextChargeClassAbstractProcessor
					.getNextChargeAndServiceClass(subscriberId, chargeClass,
							categoryID, clipID, subscriptionClass, mode, null,
							null);
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: " + e, e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: " + e, e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(),
					MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (UserException e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getMessage(),
					MessageResource.NEXT_CHARGE_CLASS_MESSAGE_FOR);
		}

		return nextServiceCharge;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
}
