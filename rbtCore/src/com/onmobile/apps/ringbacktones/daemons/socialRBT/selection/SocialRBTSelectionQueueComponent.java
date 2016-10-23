package com.onmobile.apps.ringbacktones.daemons.socialRBT.selection;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSelectionUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSelections;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class SocialRBTSelectionQueueComponent extends QueueComponent
{
	Logger logger = Logger.getLogger(SocialRBTSelectionQueueComponent.class);
	private RbtSocialSelections rbtSocialSelections = null;
	public SocialRBTSelectionQueueComponent(RbtSocialSelections rbtSocialSelections) {
		super();
		this.rbtSocialSelections = rbtSocialSelections;
	}

	@Override
	public void execute(QueueContext queueContext) {
		HashMap<String,Object> implementationMap = SocialRBTBootStrap.getImplementationMap();
		if(implementationMap!=null && implementationMap.containsKey("SEL_UPDATE_PUBLISHER_CLASS")){
			SRBTSelectionUpdatePublisher srbtSelUpdatePublisher =(SRBTSelectionUpdatePublisher) implementationMap.get("SEL_UPDATE_PUBLISHER_CLASS");
			boolean success = srbtSelUpdatePublisher.publishSelectionUpdate(rbtSocialSelections);
			if(!success && rbtSocialSelections!=null && (rbtSocialSelections.getRetryCount() < SRBTUtility.getParamAsInt("SRBT_UPDATE_RETRY_COUNT", 4))){
				rbtSocialSelections.setRetryCount(rbtSocialSelections.getRetryCount()+1);
				queueContext.getQueueContainer().addQueueComponent(this);
			}else
			{
				//RBTConnector.getInstance().getSrbtDaoWrapper().removeSRBTSelectionsUpdate(rbtSocialSelections);
				try
				{
					RbtSocialSelectionsDAO.delete(rbtSocialSelections);
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
		return rbtSocialSelections.getSequenceId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Sequence Id:"+rbtSocialSelections.getSequenceId());
		buffer.append("MSISDN:"+rbtSocialSelections.getSocialUserId());
		buffer.append("CallerId:"+rbtSocialSelections.getCallerMsisdn());
		buffer.append("Cat Id:"+rbtSocialSelections.getCatId());
		buffer.append("Clip Id:"+rbtSocialSelections.getClipId());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return rbtSocialSelections.getStartDate();
	}

}
