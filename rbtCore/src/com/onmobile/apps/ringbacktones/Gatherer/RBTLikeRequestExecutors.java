package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class RBTLikeRequestExecutors extends Thread {
	private static Logger logger = Logger
			.getLogger(RBTLikeRequestExecutors.class);
	private RBTGatherer rbtGathererThread;
	private static ExecutorService executor;
	private int fetchSize = 5;
	private long publishInterval = 60;

	public RBTLikeRequestExecutors(RBTGatherer rbtGathererThread) {
		this.rbtGathererThread = rbtGathererThread;
		init();
	}

	public void init() {
		int threadPoolCount = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON,
				"LIKEPENDING_REQUEST_EXECUTOR_CORE_POOL_SIZE", 5);
		fetchSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"NO_OF_LIKEPENDING_REQUEST_TO_PROCESS", 500);
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat("LikeExecutorThread-%d").build();
		executor = Executors.newFixedThreadPool(threadPoolCount,
				namedThreadFactory);
		publishInterval = RBTParametersUtils
				.getParamAsLong(
						iRBTConstant.DAEMON,
						"LIKEPENDING_REQUEST_EXECUTOR_QUEUE_PUBLISH_INTERVAL_IN_MINUTES",
						1);
	}

	public void run() {
		while (rbtGathererThread.isAlive()) {
			ViralSMSTable[] hasLikeviralDatas = RBTDBManager.getInstance()
					.getViralSMSByTypeAndLimit("LIKE", fetchSize);
			if (hasLikeviralDatas != null) {
				for (ViralSMSTable viralData : hasLikeviralDatas) {
					// RBTLikeDaemon likeRequest;
					Runnable likeRequest;
					try {
						likeRequest = new RBTLikeDaemon(viralData);
						executor.execute(likeRequest);
						RBTDBManager.getInstance().deleteViralPromotionBySMSID(
								viralData.getSmsId(), viralData.type());
						logger.debug("Deleted published record:"
								+ viralData.getSmsId());
					} catch (Exception e) {
						logger.debug("Exception occured in processing records"
								+ e.getMessage());
					}
				}
				logger.debug("Published Records: " + hasLikeviralDatas.length);
			}

			try {
				Thread.sleep(publishInterval * (60 * 1000));
			} catch (InterruptedException e) {
				logger.error(
						"Thread is InterruptedException stoping thread exceution",
						e);
				break;
			}
		}
		logger.debug("rbtGathererThread is not alive so we are stoping the RBTLikeRequestProcessor thread");
		executor.shutdown();
	}
}
