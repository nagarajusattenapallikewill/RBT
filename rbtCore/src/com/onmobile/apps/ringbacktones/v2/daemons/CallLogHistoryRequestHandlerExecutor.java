package com.onmobile.apps.ringbacktones.v2.daemons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class CallLogHistoryRequestHandlerExecutor {
	private static Logger logger = Logger
			.getLogger(CallLogHistoryRequestHandlerExecutor.class);
	private static ExecutorService executor;

	static {
		int threadPoolCount = RBTParametersUtils.getParamAsInt(
				iRBTConstant.DAEMON, "CALL_LOG_REQUEST_EXECUTOR_POOL_SIZE", 30);
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat("CallLogHistoryRequestExecutor-%d").build();
		executor = new ThreadPoolExecutor(threadPoolCount, threadPoolCount, 0L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				namedThreadFactory);
	}

	public static void assginCallLogRequestDetail(
			CallLogHistoryRequestHandlerDaemon callLogDeamon) {
		try {
			if (null != callLogDeamon) {
				executor.execute(callLogDeamon);
			}
		} catch (Exception e) {
			logger.debug("Exception occured in processing records"
					+ e.getMessage());
			executor.shutdown();
		}
	}
}
