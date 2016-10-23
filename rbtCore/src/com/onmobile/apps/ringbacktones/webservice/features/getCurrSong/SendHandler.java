package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.Calendar;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLogImpl.CallLogHistoryImpl;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.daemons.CurrentSongDaemon;
import com.onmobile.apps.ringbacktones.daemons.RBTCurrentPlayingSongDaemon;
import com.onmobile.apps.ringbacktones.daemons.RBTCurrentPlayingSongExecutors;
import com.onmobile.apps.ringbacktones.daemons.RBTVoltronCurrentPlayingSongDaemon;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;



public class SendHandler implements SendData.Iface {

	public static Logger logger = Logger.getLogger(SendHandler.class);

	@Override
	public void send(String called, String caller, String wavefile) throws org.apache.thrift.TException {
		sendWithCategory(called , caller, wavefile, -1);
	}

	@Override
	public void sendWithCategory(String called, String caller, String wavefile, int category_id) throws org.apache.thrift.TException
	{	//Changes are done for handling the voldemort issues.
		logger.info("called: " + called + ", caller: " + caller + ", wavefile: " + wavefile + ", categoryId: "+category_id);
		if (called != null || caller != null) {
			final CurrentPlayingSongBean bean = new CurrentPlayingSongBean();
			bean.setCalledId(called);
			bean.setWavFileName(wavefile);
			bean.setCallerId(caller);
			bean.setCategoryId(category_id);
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, CurrentSongDaemon.expiryInSeconds);
			addToMemcache(bean);
		} else {
			logger.info("Both calledId and callerId are null. Not inserted into memcache.");
		}
	}
	//Changes are done for handling the voldemort issues.
	public static void addToMemcache(CurrentPlayingSongBean bean) {
		boolean useRBTVOLTRONDaemon = RBTParametersUtils.getParamAsBoolean(iRBTConstant.DAEMON,
				"USE_RBT_VOLTRON_DAEMON_FOR_CALLLOG_UPDATION_OF_SONG", "FALSE");
		RBTCurrentPlayingSongDaemon currentPlayingSongRequest = null;
		if (useRBTVOLTRONDaemon) {
			currentPlayingSongRequest = new RBTVoltronCurrentPlayingSongDaemon(bean);
		} else {
			currentPlayingSongRequest = new RBTCurrentPlayingSongDaemon(bean);
		}
		RBTCurrentPlayingSongExecutors.assginCurrentPlyingSongDetail(currentPlayingSongRequest);

	}
	//Changes are done for handling the voldemort issues.
	public static void addToMemcache(CurrentPlayingSongBean bean, Calendar cal){
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
		//RBTLoginUser user = Utility.getRBTLoginUserBasedOnAppName(bean.getCalledId(), null);
		CallLogHistory callLogHistory = new CallLogHistoryImpl();
		boolean isSignalUser = (signalUserSet != null && (signalUserSet
				.contains(bean.getCalledId()) || signalUserSet.contains(bean
				.getCallerId()))) ? true : false;
		if (signalUserSet != null && signalUserSet.contains(bean.getCalledId())) {
			callLogHistory = new CallLogHistoryImpl();
			
			boolean isAdded = MemcacheClientForCurrentPlayingSong
					.getInstance()
					.getMemcache()
					.set(GetCurrentPlayingSong.getKeyForCurrentPlayingSong(bean.getCalledId(),
							bean.getCallerId(), "calledId"), bean, cal.getTime());

			logger.info("bean: " + bean + " tried to be inserted to memcache for calledId. Insertion status: " + isAdded);

		}
		if (isSignalUser) {
			callLogHistory.save(bean,"calledId");
		}
		//user = Utility.getRBTLoginUserBasedOnAppName(bean.getCallerId(), null);

		if (signalUserSet != null && signalUserSet.contains(bean.getCallerId())) {
			
			String privateNumberConfig = CacheManagerUtil.getParametersCacheManager().getParameterValue("DAEMON", "CALLER_ID_FOR_PRIVATE_NUMBER",null);
			if(privateNumberConfig == null || !privateNumberConfig.trim().equalsIgnoreCase(bean.getCallerId())){
				boolean isAdded = MemcacheClientForCurrentPlayingSong
						.getInstance()
						.getMemcache()
						.set(GetCurrentPlayingSong.getKeyForCurrentPlayingSong(bean.getCalledId(),
								bean.getCallerId(), "callerId"), bean, cal.getTime());

				logger.info("bean: " + bean + " tried to be inserted to memcache for callerId. Insertion status: " + isAdded);
			}

		}
		if (isSignalUser) {
			callLogHistory.save(bean,"callerId");
		}
	}
}
