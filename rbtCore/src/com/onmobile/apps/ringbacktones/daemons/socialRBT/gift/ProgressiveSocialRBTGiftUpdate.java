package com.onmobile.apps.ringbacktones.daemons.socialRBT.gift;

import org.hibernate.Session;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveHqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.srbt.db.TransactionManager;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialGift;
import com.onmobile.apps.ringbacktones.wrappers.SRBTDaoWrapper;

public class ProgressiveSocialRBTGiftUpdate extends ProgressiveHqlQueryPublisher {

	private String status = "-1";
	public ProgressiveSocialRBTGiftUpdate(String status) {
		super();
		this.status = status;
	}

	@Override
	public String getHqlQuery(int count) {
		String queryString=SRBTDaoWrapper.getInstance().getGiftUpdateQueryString(status,count, getPresentSequenceId());
		return queryString;
	}

	@Override
	public QueueComponent getNextQueueComponent(Object beanObject){
		RbtSocialGift rbtSocialGift = (RbtSocialGift)beanObject;
		return new SocialRBTGiftQueueComponent(rbtSocialGift);
	}

	@Override
	public Session getSession() 
	{
		return TransactionManager.getSessionFactory().openSession();
	}

	@Override
	protected void setPresentQueryCount(int addCount, int count) {
		if(count == 0)
        {
            ThreadUtil.sleepSec(30);// TODO read from config
        }
	}

	@Override
	public Class getBeanType() {
		return RbtSocialGift.class;
	}
}
