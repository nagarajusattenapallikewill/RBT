package com.onmobile.apps.ringbacktones.daemons.socialRBT;

import org.hibernate.Session;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveHqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.srbt.db.TransactionManager;
import com.onmobile.apps.ringbacktones.wrappers.RBTHibernateDBImplementationWrapper;

public class ProgressiveSocialRBTUpdatePublisher extends ProgressiveHqlQueryPublisher {

	private long type = -1;
	private int status = -1;
	public ProgressiveSocialRBTUpdatePublisher(long type, int status) {
		super();
		this.type = type;
		this.status = status;
	}

	@Override
	public String getHqlQuery(int count) {
		String queryString=RBTHibernateDBImplementationWrapper.getInstance().getUpdateQueryString(status, type, count, getPresentSequenceId());
		return queryString;
	}

	@Override
	public QueueComponent getNextQueueComponent(Object beanObject){
		RBTSocialUpdate rbtSocialUpdate = (RBTSocialUpdate)beanObject;
		return new SocialRBTQueueComponent(rbtSocialUpdate);
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
		return RBTSocialUpdate.class;
	}
}
