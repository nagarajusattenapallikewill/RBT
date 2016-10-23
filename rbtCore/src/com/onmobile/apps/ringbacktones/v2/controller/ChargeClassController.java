package com.onmobile.apps.ringbacktones.v2.controller;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.INextChargeClassService;
import com.onmobile.apps.ringbacktones.rbt2.service.ISubscriptionService;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.sun.corba.se.impl.orbutil.closure.Constant;

@RestController
@RequestMapping("/utils")
public class ChargeClassController {

	private static Logger logger = Logger.getLogger(Subscription.class);

	@Autowired
	@Qualifier(value = BeanConstant.NEXTCHARGECLASS_RESOLVER)
	private INextChargeClassService nextChargeClassResolver;
	
	
	public void setNextChargeClassResolver(INextChargeClassService nextChargeClassResolver) {
		this.nextChargeClassResolver = nextChargeClassResolver;
	}



	@RequestMapping(method = RequestMethod.GET, value = "/nextchargeclass")
	@ResponseBody
	public Object getAllowedSubscription(@RequestParam(value = "subscriberId", required = true) String subscriberId,
			@RequestParam(value = "categoryID", required = true) String categoryID,
			@RequestParam(value = "clipID", required = true) String clipID,
			@RequestParam(value = "subscriptionClass", required = false) String subscriptionClass,
			@RequestParam(value = "chargeClass", required = false) String chargeClass,
			@RequestParam(value = "mode", required = true) String mode) throws UserException {

		logger.info("nextchargeclass request reached: subscriberId : " + subscriberId + ", chargeClass: " + chargeClass
				+ ", categoryID: " + categoryID + ", clipID: " + clipID + ", subscriptionClass: " + subscriptionClass
				+ ", mode: " + mode);

		// INextChargeClassService nextChargeClass = (INextChargeClassService)
		// serviceResolver
		// .getChargeClassImpl();
		if (nextChargeClassResolver == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}
		return nextChargeClassResolver.getNextChargeAndServiceClass(subscriberId, chargeClass, categoryID, clipID,
				subscriptionClass, mode);
	}

}
