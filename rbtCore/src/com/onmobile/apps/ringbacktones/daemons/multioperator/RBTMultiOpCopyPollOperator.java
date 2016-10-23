package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

/**
 * @author rajesh.karavadi
 * @Since Oct 24, 2013
 */
public class RBTMultiOpCopyPollOperator extends Thread {

	private static Logger logger = Logger
			.getLogger(RBTMultiOpCopyPollOperator.class);

	private static final String DAEMON = "DAEMON";
	private static final String MULTI_OP_COPY_CONTENT_FETCH_LIMIT = "MULTI_OP_COPY_CONTENT_FETCH_LIMIT";
	private static final String MULTI_OP_COPY_OPERATOR_RETRY_COUNT = "MULTI_OP_COPY_OPERATOR_RETRY_COUNT";
	private static final String MULTI_OP_COPY_POLL_OPERATOR_SLEEP_TIME = "MULTI_OP_COPY_POLL_OPERATOR_SLEEP_TIME";

	private static final int VALID_CONTENT_STATUS = 4;
	private int fetchLimit = 2;
	private int sleepTime = 10000;
	private int maxRetries = 3;

	private boolean isAlive = false;
	private RBTDaemonManager rbtDaemonManager = null;

	public RBTMultiOpCopyPollOperator() {
		init();
		isAlive = true;
	}

	public RBTMultiOpCopyPollOperator(RBTDaemonManager rbtDaemonManager) {
		init();
		this.rbtDaemonManager = rbtDaemonManager;
	}

	private void init() {
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		String fetchLimitStr = parametersCacheManager.getParameterValue(DAEMON,
				MULTI_OP_COPY_CONTENT_FETCH_LIMIT, "5");
		fetchLimit = Integer.parseInt(fetchLimitStr);

		String maxRetriesStr = parametersCacheManager.getParameterValue(DAEMON,
				MULTI_OP_COPY_OPERATOR_RETRY_COUNT, "3");
		maxRetries = Integer.parseInt(maxRetriesStr);

		String sleepTimeStr = parametersCacheManager.getParameterValue(DAEMON,
				MULTI_OP_COPY_POLL_OPERATOR_SLEEP_TIME, null);

		if (null != sleepTimeStr) {
			sleepTime = Integer.parseInt(sleepTimeStr);
		}

	}

	public void run() {

		while ((rbtDaemonManager != null && rbtDaemonManager.isAlive())
				|| isAlive) {

			// Fetch database to get a list of records.
			int fetchFrom = 0;
			List<RBTMultiOpCopyRequest> list = fetchResolvableCopyContent(fetchFrom);

			while (list.size() > 0) {

				// Thread pool
				for (RBTMultiOpCopyRequest request : list) {
					try {
						RBTMultiOpCopyOperatorUpdater rbtMultiOpCopyOperatorUpdater = new RBTMultiOpCopyOperatorUpdater(
								maxRetries, request);
						Thread t = new Thread(rbtMultiOpCopyOperatorUpdater);
						t.setName("Thread-ContentId-"
								+ request.getSourceContentId());
						t.start();
					} catch (Exception e) {
						logger.error("Failed to update operator. request: "
								+ request, e);
					} catch (Throwable t) {
						logger.error("Failed to update operator. request: "
								+ request, t);
					}

					sleep();
					fetchFrom += fetchLimit;
					list = fetchResolvableCopyContent(fetchFrom);
				}
			}

			sleep();
		}
	}

	private void sleep() {
		try {
			logger.info("Sleeping for " + sleepTime);
			Thread.sleep(sleepTime);
			logger.info("Continue after sleep");
		} catch (Exception e) {

		}
	}

	private List<RBTMultiOpCopyRequest> fetchResolvableCopyContent(int fetchFrom) {
		List<RBTMultiOpCopyRequest> list = RBTMultiOpCopyHibernateDao
				.getInstance().fetch(fetchFrom, fetchLimit,
						VALID_CONTENT_STATUS);
		return list;
	}

	public void stopDaemon() {
		isAlive = false;
	}

	public static void main(String[] args) {

		RBTMultiOpCopyPollOperator rBTMultiOpCopyPollOperator = new RBTMultiOpCopyPollOperator();
		Thread t = new Thread(rBTMultiOpCopyPollOperator);
		t.start();
	}
}
