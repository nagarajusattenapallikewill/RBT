package com.onmobile.apps.ringbacktones.daemons.socialRBT.copy;

import org.hibernate.Session;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveHqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.srbt.db.TransactionManager;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialCopy;
import com.onmobile.apps.ringbacktones.wrappers.SRBTDaoWrapper;

public class ProgressiveSocialRBTCopyUpdate extends ProgressiveHqlQueryPublisher {

	private int status = -1;
	public ProgressiveSocialRBTCopyUpdate( int status) {
		super();
		this.status = status;
	}
	@Override
	public String getHqlQuery(int count) {
		String queryString=SRBTDaoWrapper.getInstance().getCopyUpdateQueryString(status,count,getPresentSequenceId());
		return queryString;
	}

	@Override
	public QueueComponent getNextQueueComponent(Object beanObject){
		RbtSocialCopy rbtSocialCopy = (RbtSocialCopy)beanObject;
		return new SocialRBTCopyQueueComponent(rbtSocialCopy);
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
		return RbtSocialCopy.class;
	}
}
