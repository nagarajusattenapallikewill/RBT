package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class AdPartnerRequestProducer extends Thread {
	boolean iContinue = false;
	int pendingRequestCount = 0;
	static Logger logger = Logger.getLogger(AdPartnerRequestProducer.class);
	public static RBTDBManager rbtDBManager = null;
	ThreadPoolExecutor tpe;
	ArrayList<Future<String>> futureList;
	// AD_PARTNER_CALL_BACK
	RequestType requestType = RequestType.AD_PARTNER_CALL_BACK;
	static int threadPoolCount = 5;
	static int threadSleepTime = 1000;
	private RBTDaemonManager m_mainDaemonThread;
	static Collection<String> adPartnerValuesLst = new ArrayList<String>();
	static String adPartnerTypes = "";
	static int limit = 100;
	static Map<String, String> adPartnerUrlMap = new HashMap<String, String>();
	private static RBTHttpClient rbtHttpClient = null;

	static {
		logger.info("Inside AdPartnerRequestProducer thread started==>");
		threadPoolCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"AD_PARTNER_REQUEST_EXECUTOR_POOL_SIZE", 5);
		threadSleepTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"AD_PARTNER_REQUEST_THREAD_SLEEP_TIME", 1000);
		String adPartnerKeys = RBTParametersUtils.getParamAsString("DAEMON",
				"AD_PARTNER_KEY_VALUES", "kp=KIMIA;Token=DMG");
		limit = RBTParametersUtils.getParamAsInt("DAEMON",
				"AD_PARTNER_REQUEST_LIMIT", 100);
		Map<String, String> adPartnerMap = MapUtils.convertIntoMap(
				adPartnerKeys, ";", "=", null);
		adPartnerValuesLst = adPartnerMap.values();
		logger.info("adPartnerValuesLst=" + adPartnerValuesLst.size());
		if (adPartnerValuesLst != null && !adPartnerValuesLst.isEmpty()) {
			for (String adPartner : adPartnerValuesLst) {
				adPartnerTypes += "'" + adPartner + "',";
			}
			adPartnerTypes = (adPartnerTypes != null && !adPartnerTypes
					.isEmpty()) ? adPartnerTypes.substring(0,
					adPartnerTypes.length() - 1) : "";
			logger.info("adPartnerTypes=" + adPartnerTypes);
		}
		rbtDBManager = RBTDBManager.getInstance();
		logger.info("Inside AdPartnerRequestProducer thread completed==>");
	}

	public AdPartnerRequestProducer(RBTDaemonManager mainDaemonThread) {
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		ThreadFactory threadFactory = new RequestThreadFactory(requestType);
		tpe = new ThreadPoolExecutor(threadPoolCount, threadPoolCount, 5,
				TimeUnit.MINUTES, queue, threadFactory);
		setName(requestType + "-Producer");
		this.m_mainDaemonThread = mainDaemonThread;
		init();
		initHttpClient();
		logger.info("Created AdPartnerRequestProducer for type=" + requestType);
	}

	public void init() {
		String urlMapString = RBTParametersUtils
				.getParamAsString(iRBTConstant.DAEMON, "AD_PARTNER_URL_MAP",
						"KIMIA~http://212.166.239.106:8085/VFSpain/preBuy.do?kp=%TRANS_ID%");
		adPartnerUrlMap = MapUtils.convertIntoMap(urlMapString, ";", "~", null);
	}

	private static void initHttpClient() {
		HttpParameters httpParameters = new HttpParameters();
		String param = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(iRBTConstant.DAEMON,
						"AD_PARTNER_CALL_BACK_URL_CONNECTION_TIMEOUT_MS",
						"1000");
		httpParameters.setConnectionTimeout(Integer.parseInt(param));
		param = CacheManagerUtil.getParametersCacheManager().getParameterValue(
				iRBTConstant.DAEMON,
				"AD_PARTNER_CALL_BACK_URL_SOCKET_TIMEOUT_MS", "1000");
		httpParameters.setSoTimeout(Integer.parseInt(param));
		int maxHostConn = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.DAEMON, "MAX_HOST_HTTP_CONNCETIONS", "5"));
		int maxTotalConn = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.DAEMON, "MAX_TOTAL_HTTP_CONNCETIONS", "5"));
		httpParameters.setMaxHostConnections(maxHostConn);
		httpParameters.setMaxTotalConnections(maxTotalConn);
		param = CacheManagerUtil.getParametersCacheManager().getParameterValue(
				iRBTConstant.DAEMON, "AD_PARTNER_CALL_BACK_URL_PROXY", null);
		if (param != null) {
			int index = param.indexOf(":");
			httpParameters.setProxyHost(param.substring(0, index));
			httpParameters.setProxyPort(Integer.parseInt(param
					.substring(index + 1)));
		}
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public AdPartnerRequestProducer(boolean iContinue) {
		this.iContinue = iContinue;
	}

	@Override
	public void run() {
		logger.info("Started AdPartnerRequestProducer thread");
		while (m_mainDaemonThread.isAlive() || iContinue) {
			getPendingRequests();
			if (pendingRequestCount == 0)
				sleepNow();
			else
				getResults();
		}
	}

	private void getResults() {
		logger.info("cheking futures of type " + requestType);
		for (int i = 0; i < futureList.size(); i++) {
			try {
				futureList.get(i).get();
			} catch (Exception e) {
			}
		}
		logger.info("done cheking futures of type " + requestType);
	}

	private void sleepNow() {
		try {
			Thread.sleep(threadSleepTime);
			logger.info(Thread.currentThread().getName() + " is sleeping for "
					+ threadSleepTime);
		} catch (Exception e) {
		}
	}

	private void getPendingRequests() {
		logger.info("getting ad partner requests of type " + requestType);
		Date currTime=new Date();
		List<TransData> adPartnerList = rbtDBManager.getTransDataByTypeAndTransDate(
				adPartnerTypes, limit,currTime);
		pendingRequestCount = adPartnerList == null ? 0 : adPartnerList.size();
		if (pendingRequestCount == 0)
			return;
		futureList = new ArrayList<Future<String>>();
		AdPartnerRequestDeamon adPartnerRequest = null;
		for (TransData transData : adPartnerList) {
			adPartnerRequest = new AdPartnerRequestDeamon(transData,
					rbtHttpClient, adPartnerUrlMap);
			futureList.add(tpe.submit(adPartnerRequest));
			rbtDBManager.removeTransData(transData.transID(), transData.type());
		}
	}

	public static void main(String[] args) {
		String method = "main()";
		System.out.println("Entering " + method);
		AdPartnerRequestProducer adPartnerRequestProducer = new AdPartnerRequestProducer(
				true);
		adPartnerRequestProducer.start();
	}

}
