package com.onmobile.apps.ringbacktones.daemons.callbackEvents;

import java.util.Date;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class RBTCallbackEventQueueComponent extends QueueComponent
{
	private RBTCallBackEvent rbtCallBackEvent = null;
	
	public RBTCallbackEventQueueComponent(RBTCallBackEvent rbtCallBackEvent) 
	{
		super();
		this.rbtCallBackEvent = rbtCallBackEvent;
	}
	
	@Override
	public void execute(QueueContext queueContext) 
	{
		CallbackUtils.executeEvent(rbtCallBackEvent);
	}

	@Override
	public void failed(QueueContext queContext, Throwable e) 
	{
		
	}

	@Override
	public String getDisplayName() 
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("SUBSCRIBER_ID:"+rbtCallBackEvent.getSubscriberID());
		buffer.append("EVENT_TYPE:"+rbtCallBackEvent.getEventType());
		buffer.append("MODULE_ID:"+rbtCallBackEvent.getModuleID());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() 
	{
		return null;
	}

	@Override
	public long getSequenceNo() 
	{
		return rbtCallBackEvent.getSequenceID();
	}

	@Override
	public String getUniqueName() 
	{
		return rbtCallBackEvent.getSequenceID()+"";
	}

}
