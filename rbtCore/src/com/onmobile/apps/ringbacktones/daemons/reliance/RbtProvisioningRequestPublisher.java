/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.daemons.executor.Command;
import com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtProvisioningRequestPublisher extends AbstractQueuePublisher
{
	/**
	 * @param publishInterval
	 * @param timeUnit
	 */
	public RbtProvisioningRequestPublisher(long publishInterval,
			TimeUnit timeUnit)
	{
		super(publishInterval, timeUnit, 1.0f, true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.reliance.QueuePublisher#publish
	 * ()
	 */
	@Override
	protected void publish()
	{
		int requestStatus = ProvisioningRequests.Status.TOBE_PROCESSED
				.getStatusCode();
		int requestType = ProvisioningRequests.Type.SELECTION.getTypeCode();
		int fetchSize = getFetchSize();

		List<ProvisioningRequests> provisioningRequests = ProvisioningRequestsDao
				.getByTypeStatus(requestType, requestStatus, fetchSize);
		if (provisioningRequests != null)
		{
			for (ProvisioningRequests provisioningRequest : provisioningRequests)
			{
				RbtProvisioningRequestCommand command = new RbtProvisioningRequestCommand(
						executor, provisioningRequest);

				LinkedList<Command> runningCommands = duplicateRequestMap
						.get(command.getUniqueName());
				if (runningCommands == null)
				{
					runningCommands = new LinkedList<Command>();
					runningCommands.add(command);
					duplicateRequestMap.put(command.getUniqueName(),
							runningCommands);

					executor.execute(command);
				}
				else
				{
					synchronized (runningCommands)
					{
						runningCommands.add(command);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher#getFetchSize()
	 */
	@Override
	protected int getFetchSize()
	{
		int fetchSize = super.getFetchSize();

		int noOfDuplicateRequests = 0;
		Collection<LinkedList<Command>> commandList = duplicateRequestMap
				.values();
		for (LinkedList<Command> linkedList : commandList)
		{
			noOfDuplicateRequests += linkedList.size();
		}

		return (fetchSize - noOfDuplicateRequests);
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher#clone()
	 */
	@Override
	protected QueuePublisher clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
}
