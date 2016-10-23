/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtBlockingQueue;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtTCPServerHandlerExecutor extends RbtThreadPoolExecutor
{
	private static Logger logger = Logger
			.getLogger(RbtTCPServerHandlerExecutor.class);

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 */
	private RbtTCPServerHandlerExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			RbtBlockingQueue workQueue, RbtThreadFactory threadFactory)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, null);
	}

	public static class Builder
	{
		private int corePoolSize = 5;
		private int maximumPoolSize = 5;
		private long keepAliveTime = 0;
		private int queueSize = 10000;

		private RbtBlockingQueue workQueue;
		private RbtThreadFactory threadFactory;

		/**
 * 
 */
		public Builder()
		{
			corePoolSize = RBTParametersUtils.getParamAsInt(
					iRBTConstant.DAEMON,
					"TCP_HANDLER_EXECUTOR_CORE_POOL_SIZE", 5);
			maximumPoolSize = corePoolSize;
			keepAliveTime = RBTParametersUtils
					.getParamAsLong(
							iRBTConstant.DAEMON,
							"TCP_HANDLER_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_SECONDS",
							30);
			queueSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"TCP_HANDLER_REQUEST_EXECUTOR_QUEUE_SIZE", 10000);

			workQueue = new RbtBlockingQueue(queueSize);
			threadFactory = new RbtThreadFactory(
					"RbtTCPServerHandlerExecutor");
		}

		public RbtTCPServerHandlerExecutor build()
		{
			if (logger.isInfoEnabled())
				logger.info("RbtTCPServerHandlerExecutor Configuration: "
						+ this);

			return new RbtTCPServerHandlerExecutor(corePoolSize,
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
			builder.append("RbtTCPServerHandlerExecutor.Builder [corePoolSize=");
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
}
