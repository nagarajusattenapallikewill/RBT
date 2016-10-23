package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

@RestController
@RequestMapping("/user")
public class OperatorUserDetailsCacheController {

	private static Logger logger = Logger.getLogger(OperatorUserDetailsCacheController.class);

	@Autowired
	private ServiceResolver serviceResolver;

	public void setServiceResolver(ServiceResolver serviceResolver) {
		this.serviceResolver = serviceResolver;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Object getUserDetails(@RequestParam(value = "subscriberId", required = true) String msisdn)
			throws UserException {
		logger.info("Request received for fetch subscriberId: " + msisdn);
		IUserDetailsService service = serviceResolver.getUserDetailsServiceImpl();
		
		if (service == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}
		return service.getUserDetails(msisdn);

	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object createUserDetails(@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "serviceKey", required = true) String serviceKey,
			@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "operatorName", required = true) String operatorName,
			@RequestParam(value = "circleId", required = true) String circleId) throws UserException {
		logger.info("Request received for put subscriberId: " + msisdn);
		IUserDetailsService service = serviceResolver.getUserDetailsServiceImpl();
		if (service == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}

		return service.putUserDetails(msisdn, serviceKey, status, operatorName, circleId);

	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public Object removeUserDetails(@RequestParam(value = "subscriberId", required = true) String msisdn)
			throws UserException {
		logger.info("Request received for delete subscriberId: " + msisdn);
		IUserDetailsService service = serviceResolver.getUserDetailsServiceImpl();
		if (service == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}

		return service.removeUserDetails(msisdn);

	}

	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	public Object updateUserDetails(@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "serviceKey", required = false) String serviceKey,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "operatorName", required = false) String operatorName,
			@RequestParam(value = "circleId", required = false) String circleId) throws UserException {
		logger.info("Request received for update subscriberId: " + msisdn);
		IUserDetailsService service = serviceResolver.getUserDetailsServiceImpl();
		if (service == null) {
			throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
		}

		return service.updateUserDetails(msisdn, serviceKey, status, operatorName, circleId);

	}
}
