package com.onmobile.apps.ringbacktones.daemons;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLogImpl.CallLogHistoryImpl;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;

public class RBTVoltronCurrentPlayingSongDaemon extends RBTCurrentPlayingSongDaemon implements iRBTConstant, Runnable {
	private static Logger logger = Logger.getLogger(RBTVoltronCurrentPlayingSongDaemon.class);
	final CurrentPlayingSongBean bean;
	public RBTVoltronCurrentPlayingSongDaemon(CurrentPlayingSongBean bean) {
		super(bean);
		this.bean = bean;
	}

	@Override
	public void run() {
		logger.info("New Thread has been Started for Adding to memcache");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, expiryInSecs);
		addToMemcacheVol(bean, cal);
	}

	public static void addToMemcacheVol(CurrentPlayingSongBean bean, Calendar cal) {
		Thread t1 = Thread.currentThread();
		MemcacheClientForCurrentPlayingSong.getInstance().checkCacheInitialized();
		CallLogHistory callLogHistory = new CallLogHistoryImpl();
		boolean isAdded = MemcacheClientForCurrentPlayingSong.getInstance().getMemcache().set(
				GetCurrentPlayingSong.getKeyForCurrentPlayingSong(bean.getCalledId(), bean.getCallerId(), "calledId"),
				bean, cal.getTime());
		logger.info("bean: " + bean + " tried to be inserted to memcache for calledId. Insertion status: " + isAdded);
		callLogHistory.save(bean, "calledId");
		String privateNumberConfig = CacheManagerUtil.getParametersCacheManager().getParameterValue("DAEMON",
				"CALLER_ID_FOR_PRIVATE_NUMBER", null);
		if (privateNumberConfig == null || !privateNumberConfig.trim().equalsIgnoreCase(bean.getCallerId())) {
			boolean isAdded2 = MemcacheClientForCurrentPlayingSong.getInstance().getMemcache().set(GetCurrentPlayingSong
					.getKeyForCurrentPlayingSong(bean.getCalledId(), bean.getCallerId(), "callerId"), bean,
					cal.getTime());
			logger.info(
					"bean: " + bean + " tried to be inserted to memcache for callerId. Insertion status: " + isAdded2);
		}
		callLogHistory.save(bean, "callerId");
	}
}
