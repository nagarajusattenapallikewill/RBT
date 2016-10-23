/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtBlockingQueue;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtProvisioningRequestExecutor extends RbtThreadPoolExecutor
{
	private static Logger logger = Logger
			.getLogger(RbtProvisioningRequestExecutor.class);

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param queuePublisher
	 */
	private RbtProvisioningRequestExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			RbtBlockingQueue workQueue, RbtThreadFactory threadFactory,
			QueuePublisher queuePublisher)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, queuePublisher);
	}

	public static class Builder
	{
		private int corePoolSize = 5;
		private int maximumPoolSize = 5;
		private long keepAliveTime = 0;
		private int queueSize = 5000;
		private long publishInterval = 5;

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
					"PROVISIONING_REQUEST_EXECUTOR_CORE_POOL_SIZE", 5);
			maximumPoolSize = corePoolSize;
			keepAliveTime = RBTParametersUtils
					.getParamAsLong(
							iRBTConstant.DAEMON,
							"PROVISIONING_REQUEST_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_SECONDS",
							60);
			queueSize = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
					"PROVISIONING_REQUEST_EXECUTOR_QUEUE_SIZE", 5000);
			publishInterval = RBTParametersUtils
					.getParamAsLong(
							iRBTConstant.DAEMON,
							"PROVISIONING_REQUEST_EXECUTOR_QUEUE_PUBLISH_INTERVAL_IN_SECONDS",
							5);

			workQueue = new RbtBlockingQueue(queueSize);
			threadFactory = new RbtThreadFactory(
					"RbtProvisioningRequestExecutor");
			queuePublisher = new RbtProvisioningRequestPublisher(
					publishInterval, TimeUnit.SECONDS);
		}

		public RbtProvisioningRequestExecutor build()
		{
			logger.info("RbtProvisioningRequestExecutor Configuration: " + this);
			return new RbtProvisioningRequestExecutor(corePoolSize,
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
			builder.append("RbtProvisioningRequestExecutor.Builder [corePoolSize=");
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
