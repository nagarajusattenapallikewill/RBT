package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.MigrationRequestResolver;

@RestController
@RequestMapping("/migration")
public class D2CMigrationController {

	private static Logger logger = Logger.getLogger(D2CMigrationController.class);

	@RequestMapping(method = RequestMethod.POST)
	public String migrateUser(@RequestBody(required = true) RBTSubscriberDetails rbtSubscriberDetails)
			throws UserException {
		if (rbtSubscriberDetails == null || rbtSubscriberDetails.getRbtSubscriber() == null) {
			throw new UserException("Invalid user details");
		}
		logger.info("Request received subscriberId: " + rbtSubscriberDetails.getRbtSubscriber().getSubscriber_id());
		MigrationRequestResolver migrationRequestResolver = new MigrationRequestResolver();
		return migrationRequestResolver.migrateUser(rbtSubscriberDetails);

	}
}
