package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;

/**
 * 
 * @author rajesh.karavadi
 * @Since Oct 24, 2013
 */
public class RBTMultiOpCopyContentReslover extends Thread {

	private static Logger logger = Logger
			.getLogger(RBTMultiOpCopyContentReslover.class);

	private static final String DAEMON = "DAEMON";
	private static final String MULTI_OP_COPY_CONTENT_RESOLUTION_URL = "MULTI_OP_COPY_CONTENT_RESOLUTION_URL";
	private static final String MULTI_OP_COPY_CONTENT_FETCH_LIMIT = "MULTI_OP_COPY_CONTENT_FETCH_LIMIT";
	private static final String MULTI_OP_COPY_CONTENT_RETRY_COUNT = "MULTI_OP_COPY_CONTENT_RETRY_COUNT";
	private static final int COPIEER_OPERATOR_IDENTIFIED = 2;

	private boolean isAlive = false;
	private RBTDaemonManager rbtDaemonManager = null;
	
	private int fetchLimit = 2;
	private int sleepTime = 10000;
	private int maxRetries = 3;
	private String contentResolutionUrl = null;

	public RBTMultiOpCopyContentReslover() {
		init();
		isAlive = true;
	}

	public RBTMultiOpCopyContentReslover(RBTDaemonManager rbtDaemonManager) {
		init();
		this.rbtDaemonManager = rbtDaemonManager;
	}
	
	public void init() {
		contentResolutionUrl = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(DAEMON,
						MULTI_OP_COPY_CONTENT_RESOLUTION_URL,
						"http://[::1]:8081/test/contentResolver.jsp");

		String fetchLimitStr = CacheManagerUtil.getParametersCacheManager()
				.getParameterValue(DAEMON, MULTI_OP_COPY_CONTENT_FETCH_LIMIT,
						"5");
		fetchLimit = Integer.parseInt(fetchLimitStr);
		String maxRetriesStr = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(DAEMON,
						MULTI_OP_COPY_CONTENT_RETRY_COUNT, null);
		if (null != maxRetriesStr) {
			maxRetries = Integer.parseInt(maxRetriesStr);
		}
	}

	public void run() {

		if (null != contentResolutionUrl) {

			while ((rbtDaemonManager != null && rbtDaemonManager.isAlive()) || isAlive) {
				
				// Fetch database to get a list of records.
				int fetchFrom = 0;
				List<RBTMultiOpCopyRequest> list = fetchResolvableCopyContent(fetchFrom);
				
				logger.info("Fetched list is: " + list);
				while (list.size() > 0) {
					
					for (RBTMultiOpCopyRequest request : list) {
						
						logger.debug("Creating thread: " + request);
						RBTResolveMultiOpCopyContent rBTResolveMultiOpCopyContent = new RBTResolveMultiOpCopyContent(
								contentResolutionUrl, request, maxRetries);
						Thread t = new Thread(rBTResolveMultiOpCopyContent);
						t.setName("Thread-ContentId-" + request.getCopyId());
						t.start();
					}
					
					sleep();
					fetchFrom += fetchLimit;
					list = fetchResolvableCopyContent(fetchFrom);
				}
				
				sleep();
			}

		} else {
			logger.error("Unable to process, MULTI_OP_COPY_CONTENT_RESOLUTION_URL"
					+ " is not configured.");
		}
	}

	private void sleep() {
		try {
			logger.info("Sleeping for "+sleepTime);
			Thread.sleep(sleepTime);
			logger.info("Continue after sleep");
		} catch (Exception e) {
			logger.error("Exception e: "+e.getMessage(), e);
		}
	}

	private List<RBTMultiOpCopyRequest> fetchResolvableCopyContent(int fetchFrom) {
		List<RBTMultiOpCopyRequest> list = RBTMultiOpCopyHibernateDao
				.getInstance().fetch(fetchFrom, fetchLimit,
						COPIEER_OPERATOR_IDENTIFIED);
		return list;
	}

	public static void main(String[] args) {
		Thread t1 = new Thread(new RBTMultiOpCopyContentReslover());
		t1.start();
	}
}
