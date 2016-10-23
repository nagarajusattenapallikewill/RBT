package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbt2.db.impl.D2CMigrationDBImpl;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTProvisioningRequests;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriber;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberSelection;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public class MigrationRequestResolver {

	private static Logger logger = Logger.getLogger(MigrationRequestResolver.class);
	private static Logger migrate_logger = Logger.getLogger("B2B_MIGRATION_LOGGER");

	public String migrateUser(RBTSubscriberDetails rbtSubscriberDetails) throws UserException {
		D2CMigrationDBImpl migrationDb = new D2CMigrationDBImpl();
		RBTSubscriber subscriber = rbtSubscriberDetails.getRbtSubscriber();
		List<RBTSubscriberSelection> subscriberSelections = rbtSubscriberDetails.getRbtSubscriberSelections();
		List<RBTProvisioningRequests> provList = rbtSubscriberDetails.getRbtProvisioningRequests();
		boolean isSubEntrySuccessfull = false;
		if (subscriber != null) {
			isSubEntrySuccessfull = migrationDb.saveSubscriber(subscriber);
		}
		if (subscriberSelections != null && subscriberSelections.isEmpty() && !isSubEntrySuccessfull) {
			logger.info("No selection found");
			return "FAILED";
		}
		migrate_logger.info("MigrateUserExecutor:: migrating Subscriber Entry : SubscriberId:"
				+ subscriber.getSubscriber_id() + " , Number of selection found: " + subscriberSelections.size()
				+ ", isMigrated: " + isSubEntrySuccessfull);
		// removingOperatorUserInfo(subscriberId);
		for (RBTSubscriberSelection rbtSubscriberSelection : subscriberSelections) {
			migrationDb.saveSubscriberSelection(rbtSubscriberSelection);
		}

		for (RBTProvisioningRequests rbtProvisioningRequests : provList) {
			migrationDb.saveProv(rbtProvisioningRequests);
		}
		
		return "SUCCESS";
	}

}
