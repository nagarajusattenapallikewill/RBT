/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.promotions.callgraph.CallGraph.PromotionStatus;

/**
 * @author vinayasimha.patil
 * 
 */
public class PromotionPublisher extends AbstractQueuePublisher
{
	/**
	 * @param publishInterval
	 * @param timeUnit
	 */
	public PromotionPublisher(long publishInterval, TimeUnit timeUnit)
	{
		super(publishInterval, timeUnit, 1.0f, false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #start()
	 */
	@Override
	public void start()
	{
		Calendar calendar = Calendar.getInstance();

		int publishingStartHour = 9; // 9:00 AM
		int publishingEndtHour = 19; // 7:59 PM

		int courrentHour = calendar.get(Calendar.HOUR_OF_DAY);
		if (courrentHour < publishingStartHour
				|| courrentHour > publishingEndtHour)
		{
			// If 7PM (Up to 19:59, 1 hour grace) is already passed in current
			// day, then publisher will start at 9AM next day. Otherwise it will
			// start at 9AM in current day only.
			if (courrentHour > publishingEndtHour)
				calendar.add(Calendar.DAY_OF_YEAR, 1);

			calendar.set(Calendar.HOUR_OF_DAY, publishingStartHour);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}

		super.start(calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #publish()
	 */
	@Override
	protected void publish()
	{
		if (needToReschedulePublisher())
		{
			reschedulePublisher();
			return;
		}

		int fetchSize = getFetchSize();
		List<CallGraph> callGraphs = CallGraphDao.getByPromotionStatus(
				PromotionStatus.CONFIRMED, fetchSize);
		if (callGraphs != null)
		{
			for (CallGraph callGraph : callGraphs)
			{
				Promoter promoter = new Promoter(executor, callGraph);
				publish(promoter);
			}
		}
	}

	private boolean needToReschedulePublisher()
	{
		Calendar calendar = Calendar.getInstance();

		int publishingEndtHour = 19; // 7:59 PM

		int courrentHour = calendar.get(Calendar.HOUR_OF_DAY);
		return (courrentHour > publishingEndtHour);
	}

	private void reschedulePublisher()
	{
		Calendar calendar = Calendar.getInstance();

		int publishingStartHour = 9; // 9:00 AM

		// Publisher will start at 9AM next day.
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, publishingStartHour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		reschedule(calendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #clone()
	 */
	@Override
	protected QueuePublisher clone() throws CloneNotSupportedException
	{
		QueuePublisher queuePublisher = new PromotionPublisher(publishInterval,
				timeUnit);
		return queuePublisher;
	}
}
