/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class AbstractQueuePublisher extends TimerTask implements
		QueuePublisher
{
	private static Logger logger = Logger
			.getLogger(AbstractQueuePublisher.class);

	protected RbtThreadPoolExecutor executor = null;
	protected long publishInterval;
	protected TimeUnit timeUnit;
	protected float publishFactor;

	protected Map<String, LinkedList<Command>> duplicateRequestMap;

	/**
	 * @param publishInterval
	 * @param timeUnit
	 * @param publishFactor
	 * @param cacheDuplicateRequests
	 * @param parentExecutor
	 */
	public AbstractQueuePublisher(long publishInterval, TimeUnit timeUnit,
			float publishFactor, boolean cacheDuplicateRequests)
	{
		super();

		if (timeUnit == null)
			throw new NullPointerException("timeUnit can not be null");

		if (publishFactor <= 0 || Float.isNaN(publishFactor))
		{
			throw new IllegalArgumentException("Illegal publish factor: "
					+ publishFactor);
		}

		this.publishInterval = publishInterval;
		this.timeUnit = timeUnit;
		this.publishFactor = publishFactor;

		if (cacheDuplicateRequests)
			duplicateRequestMap = new ConcurrentHashMap<String, LinkedList<Command>>();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher#getExecutor
	 * ()
	 */
	/**
	 * @return the executor
	 */
	@Override
	public RbtThreadPoolExecutor getExecutor()
	{
		return executor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher#setExecutor
	 * (com.onmobile.apps.ringbacktones.daemons.executor.RbtThreadPoolExecutor)
	 */
	/**
	 * @param executor
	 *            the executor to set
	 */
	@Override
	public void setExecutor(RbtThreadPoolExecutor executor)
	{
		if (executor == null)
		{
			throw new NullPointerException("executor can not be null");
		}

		this.executor = executor;
	}

	/**
	 * @return the publishInterval
	 */
	public long getPublishInterval()
	{
		return publishInterval;
	}

	/**
	 * @param publishInterval
	 *            the publishInterval to set
	 */
	public void setPublishInterval(long publishInterval)
	{
		this.publishInterval = publishInterval;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher#
	 * getDuplicateRequestMap()
	 */
	/**
	 * @return the duplicateRequestMap
	 */
	@Override
	public Map<String, LinkedList<Command>> getDuplicateRequestMap()
	{
		return duplicateRequestMap;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher#start()
	 */
	@Override
	public void start()
	{
		start(new Date());
	}

	protected void start(Date startDate)
	{
		if (executor == null)
			throw new IllegalStateException("Executor is not initialized");

		if (logger.isInfoEnabled())
			logger.info("Starting " + this.getClass().getSimpleName()
					+ " at : " + startDate);

		Timer timer = new Timer(this.getClass().getSimpleName());
		timer.scheduleAtFixedRate(this, startDate,
				timeUnit.toMillis(publishInterval));
	}

	protected void reschedule(Date newTime)
	{
		try
		{
			stop();

			QueuePublisher queuePublisher = clone();
			queuePublisher.setExecutor(executor);
			executor.setQueuePublisher(queuePublisher);

			if (logger.isInfoEnabled())
				logger.info("Restarting " + this.getClass().getSimpleName()
						+ " at : " + newTime);

			Timer timer = new Timer(queuePublisher.getClass().getSimpleName());
			timer.scheduleAtFixedRate((TimerTask) queuePublisher, newTime,
					timeUnit.toMillis(publishInterval));

		}
		catch (CloneNotSupportedException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher#stop()
	 */
	@Override
	public void stop()
	{
		cancel();
	}

	@Override
	public void run()
	{
		try
		{
			RbtBlockingQueue workQueue = (RbtBlockingQueue) executor.getQueue();

			if (logger.isDebugEnabled())
			{
				logger.debug("workQueue.remainingCapacity(): "
						+ workQueue.remainingCapacity()
						+ ", executor.getActiveCount(): "
						+ executor.getActiveCount());
			}

			if (publishFactor == 1.0f)
			{
				// Making sure that workQueue is empty and no other work is in
				// progress by checking active worker count.
				if (workQueue.isEmpty() && executor.getActiveCount() == 0)
					publish();
			}
			else
			{
				int fetchSize = getFetchSize();
				int queueCapacity = workQueue.getCapacity();

				if (((float) fetchSize / queueCapacity) >= publishFactor)
					publish();
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	protected abstract void publish();

	protected void publish(Command command)
	{
		if (duplicateRequestMap == null)
		{
			executor.execute(command);

			if (logger.isDebugEnabled())
				logger.debug("Command published : " + command);

			return;
		}

		LinkedList<Command> runningCommands = duplicateRequestMap.get(command
				.getUniqueName());
		if (runningCommands == null)
		{
			runningCommands = new LinkedList<Command>();
			runningCommands.add(command);
			duplicateRequestMap.put(command.getUniqueName(), runningCommands);

			executor.execute(command);

			if (logger.isDebugEnabled())
				logger.debug("Command published : " + command);
		}
		else
		{
			synchronized (runningCommands)
			{
				runningCommands.add(command);

				if (logger.isDebugEnabled())
					logger.debug("Command stored in duplicateRequestMap : "
							+ command);
			}
		}
	}

	protected int getFetchSize()
	{
		return executor.getQueue().remainingCapacity();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	abstract protected QueuePublisher clone() throws CloneNotSupportedException;
}
