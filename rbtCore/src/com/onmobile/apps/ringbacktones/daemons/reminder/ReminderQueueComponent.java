package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.util.Date;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.ReconciliationLogger;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RetryableException;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.TnbSubscriberImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class ReminderQueueComponent extends QueueComponent
{
	public int smsDay = -1;
	
	@Override
	public void execute(QueueContext queueContext)
	{
	}

	private void executeQueueComponent(QueueContext queueContext) throws Exception
	{
		
	}

	@Override
	public void failed(QueueContext queContext, Throwable e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getDisplayName()
	{
		return null;
	}

	@Override
	public Date getObjectCreationTime()
	{
		return null;
	}

	@Override
	public long getSequenceNo()
	{
		return -1;
	}

	@Override
	public String getUniqueName()
	{
		return null;
	}
	
	public void setSmsDay(int smsDay)
	{
		this.smsDay = smsDay;
	}
}
