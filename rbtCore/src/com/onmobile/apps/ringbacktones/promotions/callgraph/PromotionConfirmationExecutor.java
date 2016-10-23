/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtBlockingQueue;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadFactory;
import com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor;

/**
 * @author vinayasimha.patil
 * 
 */
public class PromotionConfirmationExecutor extends RbtThreadPoolExecutor
{
	private static Logger logger = Logger
			.getLogger(PromotionConfirmationExecutor.class);

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param queuePublisher
	 */
	private PromotionConfirmationExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			RbtBlockingQueue workQueue, RbtThreadFactory threadFactory,
			QueuePublisher queuePublisher)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, queuePublisher);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#terminated()
	 */
	@Override
	protected void terminated()
	{
		super.terminated();

		System.exit(0);
	}

	public static class Builder
	{
		private int corePoolSize = 5;
		private int maximumPoolSize = 5;
		private long keepAliveTime = 60;
		private int queueSize = 5000;
		private long publishInterval = 30;

		private RbtBlockingQueue workQueue;
		private RbtThreadFactory threadFactory;
		private QueuePublisher queuePublisher = null;

		/**
		 * 
		 */
		public Builder()
		{
			workQueue = new RbtBlockingQueue(queueSize);
			threadFactory = new RbtThreadFactory(
					"PromotionConfirmationExecutor");
			queuePublisher = new PromotionConfirmationPublisher(
					publishInterval, TimeUnit.SECONDS);
		}

		public PromotionConfirmationExecutor build()
		{
			if (logger.isInfoEnabled())
				logger.info("PromotionConfirmationExecutor Configuration: "
						+ this);

			return new PromotionConfirmationExecutor(corePoolSize,
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
			builder.append("Builder [corePoolSize=");
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
