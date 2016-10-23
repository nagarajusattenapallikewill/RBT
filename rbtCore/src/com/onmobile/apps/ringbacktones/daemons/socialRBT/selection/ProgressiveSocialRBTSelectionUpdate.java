package com.onmobile.apps.ringbacktones.daemons.socialRBT.selection;

import org.hibernate.Session;

import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveHqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.srbt.db.TransactionManager;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSelections;
import com.onmobile.apps.ringbacktones.wrappers.SRBTDaoWrapper;

public class ProgressiveSocialRBTSelectionUpdate extends ProgressiveHqlQueryPublisher {

	public ProgressiveSocialRBTSelectionUpdate() {
		super();
	}

	@Override
	public String getHqlQuery(int count) {
		String queryString=SRBTDaoWrapper.getInstance().getSelectionUpdateQueryString(count, getPresentSequenceId());
		return queryString;
	}

	@Override
	public QueueComponent getNextQueueComponent(Object beanObject){
		RbtSocialSelections rbtSocialSelections = (RbtSocialSelections)beanObject;
		return new SocialRBTSelectionQueueComponent(rbtSocialSelections);
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
		return RbtSocialSelections.class;
	}
}
