/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtThreadFactory implements ThreadFactory
{
	private static Logger logger = Logger.getLogger(RbtThreadFactory.class);

	private final ThreadGroup threadGroup;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String poolName;
	private final String namePrefix;

	/**
	 * 
	 */
	public RbtThreadFactory(String poolName)
	{
		SecurityManager securityManager = System.getSecurityManager();
		threadGroup = (securityManager != null) ? securityManager
				.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.poolName = poolName;
		namePrefix = poolName + "-thread-";
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable runnable)
	{
		Thread thread = new Thread(threadGroup, runnable, namePrefix
				+ threadNumber.getAndIncrement(), 0);
		if (thread.isDaemon())
			thread.setDaemon(false);
		if (thread.getPriority() != Thread.NORM_PRIORITY)
			thread.setPriority(Thread.NORM_PRIORITY);

		if (logger.isDebugEnabled())
			logger.debug("Created thread: " + thread);

		return thread;
	}

	/**
	 * @return the poolName
	 */
	public String getPoolName()
	{
		return poolName;
	}
}
