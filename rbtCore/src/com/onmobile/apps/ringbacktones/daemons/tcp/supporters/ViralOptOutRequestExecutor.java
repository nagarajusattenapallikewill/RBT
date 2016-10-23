package com.onmobile.apps.ringbacktones.daemons.tcp.supporters;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtBlockingQueue;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author sridhar.sindiri
 *
 */
public class ViralOptOutRequestExecutor extends RbtThreadPoolExecutor
{
	private static Logger logger = Logger
			.getLogger(ViralOptOutRequestExecutor.class);

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param queuePublisher
	 */
	private ViralOptOutRequestExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			RbtBlockingQueue workQueue, RbtThreadFactory threadFactory,
			QueuePublisher queuePublisher)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, queuePublisher);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable)
	{
		return;
	}

	public static class Builder
	{
		private int corePoolSize = 5;
		private int maximumPoolSize = 5;
		private long keepAliveTime = 60;
		private int queueSize = 5000;
		private long publishInterval = 60;

		private RbtBlockingQueue workQueue;
		private RbtThreadFactory threadFactory;
		private QueuePublisher queuePublisher = null;

		/**
		 * 
		 */
		public Builder()
		{
			corePoolSize = RBTParametersUtils.getParamAsInt(
					iRBTConstant.DAEMON,
					"VIRAL_OPTOUT_REQUEST_EXECUTOR_CORE_POOL_SIZE", 5);
			maximumPoolSize = corePoolSize;
			keepAliveTime = RBTParametersUtils
					.getParamAsLong(
							iRBTConstant.DAEMON,
							"VIRAL_OPTOUT_REQUEST_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_SECONDS",
							60);
			queueSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"VIRAL_OPTOUT_REQUEST_EXECUTOR_QUEUE_SIZE", 5000);
			publishInterval = RBTParametersUtils
					.getParamAsLong(
							iRBTConstant.DAEMON,
							"VIRAL_OPTOUT_REQUEST_EXECUTOR_QUEUE_PUBLISH_INTERVAL_IN_SECONDS",
							60);

			workQueue = new RbtBlockingQueue(queueSize);
			threadFactory = new RbtThreadFactory(
					"ViralOptOutRequestExecutor");
			queuePublisher = new ViralOptOutRequestPublisher(
					publishInterval, TimeUnit.SECONDS);
		}

		/**
		 * @return
		 */
		public ViralOptOutRequestExecutor build()
		{
			if (logger.isInfoEnabled())
				logger.info("ViralOptOutRequestExecutor Configuration: "
						+ this);

			return new ViralOptOutRequestExecutor(corePoolSize,
					maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
					workQueue, threadFactory, queuePublisher);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("ViralOptOutRequestExecutor.Builder [corePoolSize=");
			builder.append(corePoolSize);
			builder.append(", maximumPoolSize=");
			builder.append(maximumPoolSize);
			builder.append(", keepAliveTime=");
			builder.append(keepAliveTime);
			builder.append(", queueSize=");
			builder.append(queueSize);
			builder.append(", publishInterval=");
			builder.append(publishInterval);
			builder.append("]");
			return builder.toString();
		}
	}
}
