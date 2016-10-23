package com.onmobile.apps.ringbacktones.daemons;

import java.util.Calendar;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLogImpl.CallLogHistoryImpl;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.LoadRBTLoginUser;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.RBTLoginUserFactory;

public class RBTCurrentPlayingSongDaemon implements iRBTConstant, Runnable {
	private static Logger logger = Logger
			.getLogger(RBTCurrentPlayingSongDaemon.class);
	final CurrentPlayingSongBean bean;
	public int expiryInSecs = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
			WebServiceConstants.CURRENT_PLAYING_SONG_MEMCACHE_EXPIRATION_LENGTH_IN_SECONDS, 5);

	public RBTCurrentPlayingSongDaemon(CurrentPlayingSongBean bean) {
		super();
		this.bean = bean;
	}

	@Override
	public void run() {
		logger.info("New Thread has been Started for Adding to memcache");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, expiryInSecs);
		addToMemcache(bean, cal);
	}

	public static void addToMemcache(CurrentPlayingSongBean bean, Calendar cal) {
		Thread t1 = Thread.currentThread();
		MemcacheClientForCurrentPlayingSong.getInstance()
				.checkCacheInitialized();
		boolean isCallLogMemCacheIsUp = MemcacheClientForCurrentPlayingSong
				.getInstance().isCacheAlive();
		String sourceOfLoginUser = (isCallLogMemCacheIsUp) ? "CACHE" : "DB";
		RBTLoginUserFactory loginUserFactory = new RBTLoginUserFactory();
		logger.info("sourceOfLoginUser: " + sourceOfLoginUser);
		LoadRBTLoginUser userSource = loginUserFactory
				.getRBTLoginUser(sourceOfLoginUser);
		Set<String> signalUserSet = userSource.getRBTLoginUserData(
				bean.getCallerId(), bean.getCalledId(), null);
		// RBTLoginUser user =
		// Utility.getRBTLoginUserBasedOnAppName(bean.getCalledId(), null);
		CallLogHistory callLogHistory = new CallLogHistoryImpl();
		boolean isSignalUser = (signalUserSet != null && (signalUserSet
				.contains(bean.getCalledId()) || signalUserSet.contains(bean
				.getCallerId()))) ? true : false;
		if (signalUserSet != null
				&& signalUserSet.contains(bean.getCalledId())) {
			callLogHistory = new CallLogHistoryImpl();
			boolean isAdded = MemcacheClientForCurrentPlayingSong
					.getInstance()
					.getMemcache()
					.set(GetCurrentPlayingSong.getKeyForCurrentPlayingSong(
							bean.getCalledId(), bean.getCallerId(),
							"calledId"), bean, cal.getTime());
			logger.info("bean: "
					+ bean
					+ " tried to be inserted to memcache for calledId. Insertion status: "
					+ isAdded);
		}
		if (isSignalUser) {
			callLogHistory.save(bean, "calledId");
		}
		// user = Utility.getRBTLoginUserBasedOnAppName(bean.getCallerId(),
		// null);
		if (signalUserSet != null
				&& signalUserSet.contains(bean.getCallerId())) {
			String privateNumberConfig = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							"DAEMON", "CALLER_ID_FOR_PRIVATE_NUMBER", null);
			if (privateNumberConfig == null
					|| !privateNumberConfig.trim().equalsIgnoreCase(
							bean.getCallerId())) {
				boolean isAdded = MemcacheClientForCurrentPlayingSong
						.getInstance()
						.getMemcache()
						.set(GetCurrentPlayingSong.getKeyForCurrentPlayingSong(
								bean.getCalledId(), bean.getCallerId(),
								"callerId"), bean, cal.getTime());
				logger.info("bean: "
						+ bean
						+ " tried to be inserted to memcache for callerId. Insertion status: "
						+ isAdded);
			}
		}
		if (isSignalUser) {
			callLogHistory.save(bean, "callerId");
		}
	}
}
