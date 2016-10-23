package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class RBTLoginUserCacheImpl implements LoadRBTLoginUser {
	private static Logger logger = Logger
			.getLogger(RBTLoginUserCacheImpl.class);

	@Override
	public Set<String> getRBTLoginUserData(String callerId, String calledId,
			String userId) {
		logger.info("Inside getRBTLoginUserData by cache:");
		Set<String> signaleUserSet = new HashSet<String>();
		addSignalUser(callerId, signaleUserSet);
		addSignalUser(calledId, signaleUserSet);
		return signaleUserSet;
	}

	private void addSignalUser(String susbcriberId, Set<String> signaleUserSet) {
		if (null != susbcriberId) {
			Boolean isSignalUser = (Boolean) MemcacheClientForCurrentPlayingSong
					.getInstance().getMemcache().get(susbcriberId);
			logger.info("getRBTLoginUserData by cache for susbcriberId:"
					+ susbcriberId + ", isSignalUser:" + isSignalUser);
			if (null != isSignalUser) {
				signaleUserSet.add(susbcriberId);
			}
		}
	}
}
