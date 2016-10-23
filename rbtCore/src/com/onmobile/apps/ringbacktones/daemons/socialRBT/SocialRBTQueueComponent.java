package com.onmobile.apps.ringbacktones.daemons.socialRBT;

import java.util.Date;

import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SocialRBTBaseUpdatePublisher;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class SocialRBTQueueComponent extends QueueComponent{

	private RBTSocialUpdate rbtSocialUpdate = null;
	public SocialRBTQueueComponent(RBTSocialUpdate rbtSocialUpdate) {
		super();
		this.rbtSocialUpdate = rbtSocialUpdate;
	}

	@Override
	public void execute(QueueContext queueContext) {
//		System.err.println(getDisplayName());
		new SocialRBTBaseUpdatePublisher().updatePublisher(rbtSocialUpdate);
//		rbtSocialUpdate.setStatus(2);
//		rbtSocialUpdate.update();
	}

	@Override
	public void failed(QueueContext queContext, Throwable e){
		
	}

	@Override
	public long getSequenceNo() {
		return rbtSocialUpdate.getId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("MSISDN:"+rbtSocialUpdate.getMsisdn());
		buffer.append("CallerId:"+rbtSocialUpdate.getCallerId());
		buffer.append("Event Type:"+rbtSocialUpdate.getEventType());
		buffer.append("Start Time:"+rbtSocialUpdate.getStartTime());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return rbtSocialUpdate.getStartTime();
	}

}
