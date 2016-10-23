package com.onmobile.apps.ringbacktones.daemons.socialRBT.copy;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.selection.SocialRBTSelectionQueueComponent;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTCopyUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialCopy;

public class SocialRBTCopyQueueComponent extends QueueComponent
{
	Logger logger = Logger.getLogger(SocialRBTSelectionQueueComponent.class);
	private RbtSocialCopy rbtSocialCopy = null;
	public SocialRBTCopyQueueComponent(RbtSocialCopy rbtSocialCopy) {
		super();
		this.rbtSocialCopy = rbtSocialCopy;
	}

	@Override
	public void execute(QueueContext queueContext) {
		HashMap<String,Object> implementationMap = SocialRBTBootStrap.getImplementationMap();
		if(implementationMap!=null && implementationMap.containsKey("COPY_UPDATE_PUBLISHER_CLASS")){
			SRBTCopyUpdatePublisher srbtCopyUpdatePublisher =(SRBTCopyUpdatePublisher) implementationMap.get("COPY_UPDATE_PUBLISHER_CLASS");
			boolean success = srbtCopyUpdatePublisher.publishCopyUpdate(rbtSocialCopy);
			if(!success && rbtSocialCopy!=null && (rbtSocialCopy.getRetryCount() < SRBTUtility.getParamAsInt("SRBT_UPDATE_RETRY_COUNT", 4))){
				rbtSocialCopy.setRetryCount(rbtSocialCopy.getRetryCount()+1);
				queueContext.getQueueContainer().addQueueComponent(this);
			}
			else
			{
				try
				{
					RbtSocialSelectionsDAO.delete(rbtSocialCopy);
				}
				catch (Exception e)
				{
					logger.error("Exception", e);
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
		return rbtSocialCopy.getSequenceId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("sequence id:"+rbtSocialCopy.getSequenceId());
		buffer.append("MSISDN id:"+rbtSocialCopy.getUserSqnIdReqBy());
		buffer.append("copy msisdn id:"+rbtSocialCopy.getUserSqnIdReqFor());
		buffer.append("copy msisdn:"+rbtSocialCopy.getCopyForMsisdn());
		buffer.append("Cat Id:"+rbtSocialCopy.getCatId());
		buffer.append("Clip Id:"+rbtSocialCopy.getClipId());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return rbtSocialCopy.getStartDate();
	}

}
