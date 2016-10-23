package com.onmobile.apps.ringbacktones.daemons.socialRBT.subscriber;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSubscriberUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSubscriberDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSubscriber;

public class SocialRBTSubscriberQueueComponent extends QueueComponent
{
	Logger logger = Logger.getLogger(SocialRBTSubscriberQueueComponent.class);
	private RbtSocialSubscriber rbtSocialSubscriber = null;
	public SocialRBTSubscriberQueueComponent(RbtSocialSubscriber rbtSocialSubscriber) {
		super();
		this.rbtSocialSubscriber = rbtSocialSubscriber;
	}

	@Override
	public void execute(QueueContext queueContext) {
		HashMap<String,Object> implementationMap = SocialRBTBootStrap.getImplementationMap();
		if(implementationMap!=null && implementationMap.containsKey("SUB_UPDATE_PUBLISHER_CLASS")){
			
			SRBTSubscriberUpdatePublisher srbtSubUpdatePublisher =(SRBTSubscriberUpdatePublisher) implementationMap.get("SUB_UPDATE_PUBLISHER_CLASS");
			boolean success = srbtSubUpdatePublisher.publishSubscriberUpdate(rbtSocialSubscriber);
			
			if(!success && rbtSocialSubscriber!=null && (rbtSocialSubscriber.getRetryCount() < SRBTUtility.getParamAsInt("SRBT_UPDATE_RETRY_COUNT", 4)))
			{
				rbtSocialSubscriber.setRetryCount(rbtSocialSubscriber.getRetryCount()+1);
				queueContext.getQueueContainer().addQueueComponent(this);
			}
			else
			{
				logger.debug("Is publish success ?? "+success);
				// Sreenadh
				try
				{
					//RBTConnector.getInstance().getSrbtDaoWrapper().removeSRBTSubscriberUpdate(rbtSocialSubscriber);
					RbtSocialSubscriberDAO.delete(rbtSocialSubscriber);
					if((rbtSocialSubscriber !=null) && (rbtSocialSubscriber.getEvtType() == iRBTConstant.evtTypeForAccountExpiry))
						SRBTUtility.deactivateSocialSiteUser(rbtSocialSubscriber.getMsisdn() + "");
				}
				catch(Exception e)
				{
					// Don't know what to do ?? 
					logger.error("Exception in deleting entry", e);
				}
				
			}
		}
	}

	@Override
	public void failed(QueueContext queContext, Throwable e){
		queContext.getQueueContainer().addQueueComponent(this);
	}

	@Override
	public long getSequenceNo() {
		return rbtSocialSubscriber.getSequenceId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Sequence Id:"+rbtSocialSubscriber.getSequenceId());
		buffer.append("MSISDN:"+rbtSocialSubscriber.getSocialUserId());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return rbtSocialSubscriber.getStartDate();
	}

}
