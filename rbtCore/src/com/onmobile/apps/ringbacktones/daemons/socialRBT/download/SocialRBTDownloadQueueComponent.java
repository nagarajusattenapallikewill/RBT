package com.onmobile.apps.ringbacktones.daemons.socialRBT.download;

import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.SocialRBTBootStrap;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTDownloadUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialDownloads;

public class SocialRBTDownloadQueueComponent extends QueueComponent{

	private RbtSocialDownloads rbtSocialDownloads = null;
	public SocialRBTDownloadQueueComponent(RbtSocialDownloads rbtSocialDownloads) {
		super();
		this.rbtSocialDownloads = rbtSocialDownloads;
	}

	@Override
	public void execute(QueueContext queueContext) {
		HashMap<String,Object> implementationMap = SocialRBTBootStrap.getImplementationMap();
		if(implementationMap!=null && implementationMap.containsKey("DOWNLOAD_UPDATE_PUBLISHER_CLASS")){
			SRBTDownloadUpdatePublisher srbtDownloadUpdatePublisher =(SRBTDownloadUpdatePublisher) implementationMap.get("DOWNLOAD_UPDATE_PUBLISHER_CLASS");
			boolean success = srbtDownloadUpdatePublisher.publishDownloadUpdate(rbtSocialDownloads);
			if(!success && rbtSocialDownloads!=null && (rbtSocialDownloads.getRetryCount() < SRBTUtility.getParamAsInt("SRBT_UPDATE_RETRY_COUNT", 4))){
				rbtSocialDownloads.setRetryCount(rbtSocialDownloads.getRetryCount()+1);
				queueContext.getQueueContainer().addQueueComponent(this);
			}
			else
			{
				//RBTConnector.getInstance().getSrbtDaoWrapper().removeSRBTSelectionsUpdate(rbtSocialSelections);
				try
				{
					RbtSocialSelectionsDAO.delete(rbtSocialDownloads);
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
		return rbtSocialDownloads.getSequenceId();
	}

	@Override
	public String getUniqueName() {
		return getSequenceNo()+"";
	}

	@Override
	public String getDisplayName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("sequence id:"+rbtSocialDownloads.getSequenceId());
		buffer.append("MSISDN id:"+rbtSocialDownloads.getUserSqnId());
		buffer.append("Cat Id:"+rbtSocialDownloads.getCatId());
		buffer.append("Clip Id:"+rbtSocialDownloads.getClipId());
		return buffer.toString();
	}

	@Override
	public Date getObjectCreationTime() {
		return null;
	}

}
