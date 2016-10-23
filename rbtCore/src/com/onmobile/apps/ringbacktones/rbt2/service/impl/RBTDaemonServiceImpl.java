package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTDaemonService;


public class RBTDaemonServiceImpl implements RBTDaemonService {

	private static Logger logger = Logger.getLogger(RBTDaemonServiceImpl.class);
	@Override
	public boolean updateSubscribersInPlayer(Subscriber subscribersToUpdate, boolean suspend) {
		logger.info("Updating Subscriber");
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscribersToUpdate.subID());
		return RBTPlayerUpdateDaemonWrapper.getInstance().updateSubscribersInTonePlayer(subscriber, suspend);
	}

	@Override
	public boolean deactivateUsersInPlayerDB(Subscriber subscriberToDeactivate) {
		logger.info("Deactivating Subscriber");
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberToDeactivate.subID());
		return RBTPlayerUpdateDaemonWrapper.getInstance().deactivateUsersInTonePlayerDB(subscriber);
	}

}
