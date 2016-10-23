package com.onmobile.apps.ringbacktones.daemons.genericftp;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtBlockingQueue;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author sridhar.sindiri
 *
 */
public class FTPCampaignExecutor extends RbtThreadPoolExecutor
{
	private static Logger logger = Logger.getLogger(FTPCampaignExecutor.class);

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param queuePublisher
	 */
	public FTPCampaignExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, RbtBlockingQueue workQueue,
			RbtThreadFactory threadFactory)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, null);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable)
	{
		// Implemented this method to avoid the execution of the same method in RbtThreadPoolExecutor
	}

	public static class Builder
	{
		private int corePoolSize = 5;
		private int maximumPoolSize = 5;
		private long keepAliveTime = 0;
		private int queueSize = 5000;

		private RbtBlockingQueue workQueue;
		private RbtThreadFactory threadFactory;

		/**
		 * 
		 */
		public Builder()
		{
			corePoolSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"FTP_CAMPAIGN_EXECUTOR_CORE_POOL_SIZE", 5);
			maximumPoolSize = corePoolSize;
			keepAliveTime = RBTParametersUtils.getParamAsLong(
					iRBTConstant.DAEMON,
					"FTP_CAMPAIGN_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_SECONDS",
					60);
			queueSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"FTP_CAMPAIGN_EXECUTOR_QUEUE_SIZE", 50);

			workQueue = new RbtBlockingQueue(queueSize);
			threadFactory = new RbtThreadFactory("FTPCampaignExecutor");
		}

		public FTPCampaignExecutor build()
		{
			logger.info("FTPCampaignExecutor Configuration: " + this);
			return new FTPCampaignExecutor(corePoolSize,
					maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
					workQueue, threadFactory);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("FTPCampaignExecutor.Builder [corePoolSize=");
			builder.append(corePoolSize);
			builder.append(", maximumPoolSize=");
			builder.append(maximumPoolSize);
			builder.append(", keepAliveTime=");
			builder.append(keepAliveTime);
			builder.append(", queueSize=");
			builder.append(queueSize);
			builder.append("]");
			return builder.toString();
		}
	}

	/**
	 * @author sridhar.sindiri
	 *
	 */
	public static class FTPRejectedExecutionHandler implements RejectedExecutionHandler
	{
		/**
		 * Creates a <tt>RbtRejectedExecutionHandler</tt>.
		 */
		public FTPRejectedExecutionHandler()
		{
		}

		/**
		 * @param runnable
		 *            the runnable task requested to be executed
		 * @param threadPoolExecutor
		 *            the executor attempting to execute this task
		 */
		@Override
		public void rejectedExecution(Runnable runnable,
				ThreadPoolExecutor threadPoolExecutor)
		{
			throw new RejectedExecutionException();
		}
	}
}
