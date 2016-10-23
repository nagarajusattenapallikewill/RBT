package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.List;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

public class RBTLoginUserMemcacheLoad extends Thread {
	Logger logger = Logger.getLogger(RBTLoginUserMemcacheLoad.class);
	private boolean running;
	private RBTDBManager rbtDBManager = null;
	private MemCachedClient mc = null;

	public RBTLoginUserMemcacheLoad() {
		rbtDBManager = RBTDBManager.getInstance();
		mc = MemcacheClientForCurrentPlayingSong.getInstance().getMemcache();
		this.setDaemon(true);
		this.setName("RBTLoginUserMemcacheLoad");
	}

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Start the thread.
	 */
	public void run() {
		this.running = true;
		logger.info("Starting the RBTLoginUserMemcacheLoad");
		int initial = 0;
		mc.flushAll();
		List<String> configuredAppNamesForTPSupport = Utility
				.getConfiguredAppNamesForTPSupport();
		if (null != configuredAppNamesForTPSupport) {
			String type = "";
			for (String appName : configuredAppNamesForTPSupport) {
				type += (Utility.getMobileClientTypeWithAppName(appName) + ",");
			}
			if (type.length() > 0)
				type = type.substring(0, type.length() - 1);
			logger.info("type value is: " + type);
			RBTLoginUser[] rbtLoginUsers = rbtDBManager.getRBTLoginUsers(type,
					String.valueOf(initial));
			do {
				if (!putLoginUserDetailsInMemCache(rbtLoginUsers)) {
					break;
				}
				initial = initial
						+ Integer
								.parseInt(iRBTConstant.LIMIT_TO_FETCH_LOGIN_USER_DATA);
				rbtLoginUsers = rbtDBManager.getRBTLoginUsers(type,
						String.valueOf(initial));
			} while (true);
			logger.info("Added login user info into memcache");
		}
	}

	private boolean putLoginUserDetailsInMemCache(RBTLoginUser[] rbtLoginUsers) {
		if (null != rbtLoginUsers) {
			for (RBTLoginUser loginUser : rbtLoginUsers) {
				logger.info("Added the user info into memcache for the subscriberID: "
						+ loginUser.subscriberID());
				mc.add(loginUser.subscriberID(), true);
			}
			return true;
		} else {
			logger.info("Loaded all signal users from db/No singal user's please check the type");
			return false;
		}
	}

}
