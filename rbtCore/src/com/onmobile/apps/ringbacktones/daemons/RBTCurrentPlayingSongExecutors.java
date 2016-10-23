package com.onmobile.apps.ringbacktones.daemons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class RBTCurrentPlayingSongExecutors {
	private static Logger logger = Logger
			.getLogger(RBTCurrentPlayingSongExecutors.class);
	private static ExecutorService executor;

	static {
		int threadPoolCount = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON,
				"CURRENT_PLAYING_SONG_REQUEST_EXECUTOR_POOL_SIZE", 1000);
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat("CurrentPlayingSongExecutorThread-%d").build();
		executor = new ThreadPoolExecutor(threadPoolCount, threadPoolCount, 0L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				namedThreadFactory);

		/*
		 * Executors.newFixedThreadPool(threadPoolCount, namedThreadFactory);
		 */
	}

	public static void assginCurrentPlyingSongDetail(
			RBTCurrentPlayingSongDaemon currentPlayingSongDaemon) {
		try {
			if (null != currentPlayingSongDaemon){
				executor.execute(currentPlayingSongDaemon);
			}
		} catch (Exception e) {
			logger.debug("Exception occured in processing records"
					+ e.getMessage());
			executor.shutdown();
		}
	}
}
