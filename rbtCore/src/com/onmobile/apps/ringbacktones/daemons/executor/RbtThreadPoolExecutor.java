/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.executor.Command.CommandStatus;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class RbtThreadPoolExecutor extends ThreadPoolExecutor
{
	private static Logger logger = Logger
			.getLogger(RbtThreadPoolExecutor.class);

	private QueuePublisher queuePublisher;

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param queuePublisher
	 */
	public RbtThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			RbtBlockingQueue workQueue, RbtThreadFactory threadFactory,
			QueuePublisher queuePublisher)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, new RbtRejectedExecutionHandler());

		allowCoreThreadTimeOut(true);

		if (queuePublisher != null)
		{
			queuePublisher.setExecutor(this);
			queuePublisher.start();
			this.queuePublisher = queuePublisher;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable)
	{
		super.afterExecute(runnable, throwable);

		try
		{
			if (throwable != null)
			{
				logger.error(
						"Exception occured while processing the request, so this request and associated requests will be discarded.",
						throwable);
				getRejectedExecutionHandler().rejectedExecution(runnable, this);
				return;
			}

			Command command = (Command) runnable;
			if (command.getCommandStatus() == CommandStatus.REJECTED)
			{
				logger.debug("Command rejected");
				getRejectedExecutionHandler().rejectedExecution(runnable, this);
				return;
			}

			if (command.getCommandStatus() == CommandStatus.RETRIABLE)
			{
				logger.debug("Command needs to be retried");
				execute(command);
				return;
			}

			if (queuePublisher == null)
				return;

			Map<String, LinkedList<Command>> duplicateRequestMap = queuePublisher
					.getDuplicateRequestMap();
			if (duplicateRequestMap != null)
			{
				LinkedList<Command> commands = duplicateRequestMap.get(command
						.getUniqueName());
				if (commands != null && !commands.isEmpty())
				{
					synchronized (commands)
					{
						commands.remove();
						Runnable nextCommand = commands.peek();
						if (nextCommand != null)
							execute(nextCommand);
						else
							duplicateRequestMap.remove(command.getUniqueName());
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#shutdown()
	 */
	@Override
	public void shutdown()
	{
		if (queuePublisher != null)
			queuePublisher.stop();
		super.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadPoolExecutor#shutdownNow()
	 */
	@Override
	public List<Runnable> shutdownNow()
	{
		if (queuePublisher != null)
			queuePublisher.stop();
		return super.shutdownNow();
	}

	/**
	 * @return the queuePublisher
	 */
	public QueuePublisher getQueuePublisher()
	{
		return queuePublisher;
	}

	/**
	 * @param queuePublisher
	 *            the queuePublisher to set
	 */
	public void setQueuePublisher(QueuePublisher queuePublisher)
	{
		this.queuePublisher = queuePublisher;
	}

	/**
	 * @author vinayasimha.patil
	 */
	public static class RbtRejectedExecutionHandler implements
			RejectedExecutionHandler
	{
		/**
		 * Creates a <tt>RbtRejectedExecutionHandler</tt>.
		 */
		public RbtRejectedExecutionHandler()
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
			try
			{
				Command command = (Command) runnable;
				if (logger.isInfoEnabled())
					logger.info("Command rejected: " + command);

				command.rejectedExecution(threadPoolExecutor);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
}
