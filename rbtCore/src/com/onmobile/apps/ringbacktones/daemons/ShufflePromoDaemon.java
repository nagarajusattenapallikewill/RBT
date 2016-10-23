package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;

public class ShufflePromoDaemon extends Thread implements iRBTConstant,Constants{
	
	private static Logger logger = Logger.getLogger(ShufflePromoDaemon.class);
	
	private RBTDaemonManager mainDaemonThread = null;
	private static RBTCacheManager rbtCacheManager = null;
	private static RBTDBManager rbtDBManager = null;
	private static ParametersCacheManager rbtParamCacheManager = null;
	private static Long sleepTime = null;
	private static String dummyCatId = null;
	
	protected ShufflePromoDaemon(RBTDaemonManager mainDaemonThread)
	{
		this.mainDaemonThread = mainDaemonThread;
		init();
		setName("ShufflePromoDaemon");
	}

	private void init()
	{
		try
		{
			rbtCacheManager = RBTCacheManager.getInstance();
			rbtDBManager = RBTDBManager.getInstance();
			rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
			sleepTime = Long.valueOf(param(DAEMON,SHUFFLE_PROMO_SLEEP_TIME,"5000"));
			dummyCatId = param(SMS,ESIA_SHUFFLE_PROMO_DUMMY_CATEGORYID,"3");
		}
		catch (Exception e)
		{
			logger.error("Issue in creating ShufflePromoDaemon", e);
		}
	}
	@Override
	public void run() {
		while(mainDaemonThread != null && mainDaemonThread.isAlive())
		{
			rbtDBManager.processShufflePromo(dummyCatId);
			try {
				Thread.sleep(sleepTime);
			} catch (NumberFormatException e) {
				logger.error(""+e);
			} catch (InterruptedException e) {
				logger.error(""+e);
			}
			
		}
	}
	public  static String param(String type, String paramName, String defaultVal) {
		Parameters param = rbtParamCacheManager.getParameter(type, paramName, defaultVal);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return defaultVal;
	}

}