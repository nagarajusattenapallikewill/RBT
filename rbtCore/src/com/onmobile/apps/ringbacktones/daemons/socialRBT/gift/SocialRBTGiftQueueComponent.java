package com.onmobile.apps.ringbacktones.daemons.socialRBT.gift;

import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTGiftUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialGift;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class SocialRBTGiftQueueComponent extends QueueComponent{

	private RbtSocialGift rbtSocialGift = null;
	public SocialRBTGiftQueueComponent(RbtSocialGift rbtSocialGift) {
		super();
		this.rbtSocialGift = rbtSocialGift;
	}

	@Override
	public void execute(QueueContext queueContext) {
		HashMap<String,Object> implementationMap = SocialRBTBootStrap.getImplementationMap();
		if(implementationMap!=null && implementationMap.containsKey("GIFT_UPDATE_PUBLISHER_CLASS")){
			SRBTGiftUpdatePublisher srbtGiftUpdatePublisher =(SRBTGiftUpdatePublisher) implementationMap.get("GIFT_UPDATE_PUBLISHER_CLASS");
			boolean success = srbtGiftUpdatePublisher.publishGiftUpdate(rbtSocialGift);
			if(!success && rbtSocialGift!=null && (rbtSocialGift.getRetryCount() < SRBTUtility.getParamAsInt("SRBT_UPDATE_RETRY_COUNT", 4))){
				rbtSocialGift.setRetryCount(rbtSocialGift.getRetryCount()+1);
				queueContext.getQueueContainer().addQueueComponent(this);
			}
			else
			{
				//RBTConnector.getInstance().getSrbtDaoWrapper().removeSRBTSelectionsUpdate(rbtSocialSelections);
				try
				{
					RbtSocialSelectionsDAO.delete(rbtSocialGift);
				}
				catch (Exception e)
				{
					
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
		return rbtSocialGift.getSequenceId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("sequence id:"+rbtSocialGift.getSequenceId());
		buffer.append("MSISDN id:"+rbtSocialGift.getGifterMsisdn());
		buffer.append("copy msisdn:"+rbtSocialGift.getGifteeMsisdn());
		buffer.append("Cat Id:"+rbtSocialGift.getCatId());
		buffer.append("Clip Id:"+rbtSocialGift.getClipId());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return rbtSocialGift.getStartDate();
	}

}
