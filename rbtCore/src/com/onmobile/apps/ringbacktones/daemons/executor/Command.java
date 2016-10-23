/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class Command implements Runnable
{
	private static Logger logger = Logger.getLogger(Command.class);

	protected RbtThreadPoolExecutor executor;
	protected CommandStatus commandStatus;

	/**
	 * @param executor
	 */
	public Command(RbtThreadPoolExecutor executor)
	{
		super();
		this.executor = executor;
		commandStatus = CommandStatus.VIRGIN;
	}

	/**
	 * @return the executor
	 */
	public RbtThreadPoolExecutor getExecutor()
	{
		return executor;
	}

	/**
	 * @param executor
	 *            the executor to set
	 */
	public void setExecutor(RbtThreadPoolExecutor executor)
	{
		this.executor = executor;
	}

	/**
	 * @return the commandStatus
	 */
	public CommandStatus getCommandStatus()
	{
		return commandStatus;
	}

	/**
	 * @param commandStatus
	 *            the commandStatus to set
	 */
	public void setCommandStatus(CommandStatus commandStatus)
	{
		this.commandStatus = commandStatus;
	}

	public abstract String getUniqueName();

	public void rejectedExecution(ThreadPoolExecutor threadPoolExecutor)
	{
		try
		{
			RbtThreadPoolExecutor executor = (RbtThreadPoolExecutor) threadPoolExecutor;
			QueuePublisher queuePublisher = executor.getQueuePublisher();
			if (queuePublisher == null)
				return;

			Map<String, LinkedList<Command>> duplicateRequestMap = queuePublisher
					.getDuplicateRequestMap();
			if (duplicateRequestMap != null)
			{
				Command command = this;
				LinkedList<Command> commands = duplicateRequestMap.get(command
						.getUniqueName());
				if (commands != null)
				{
					synchronized (commands)
					{
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [commandStatus=");
		builder.append(commandStatus);
		builder.append("]");
		return builder.toString();
	}

	public enum CommandStatus
	{
		VIRGIN,
		RETRIABLE,
		REJECTED,
		DISCARDED,
		EXECUTED
	}
}
