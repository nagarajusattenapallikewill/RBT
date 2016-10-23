package com.onmobile.apps.ringbacktones.v2.processor;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.rbt2.thread.ThreadExecutor;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;


public class SubscriptionProcessorImpl implements ISubscriptionProcessor, WebServiceConstants {
		
	private ThreadExecutor executor;
	
	RBTDaemonManager rbtDaemonManager = null;
	
	public SubscriptionProcessorImpl() {
		
	}
		
	public void setExecutor(ThreadExecutor executor) {
		this.executor = executor;
	}

	@Override
	public void startProcessingProcessACT(WebServiceContext task) {
		if(task.containsKey(param_subscriberID)){
		RBTDaemonManager.m_rbtDaemonManager = new RBTDaemonManager();
		rbtDaemonManager = RBTDaemonManager.m_rbtDaemonManager;
		rbtDaemonManager.getConfigValues();
		
		executor.getExecutor().execute(rbtDaemonManager);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber((String)task.get(param_subscriberID));
		rbtDaemonManager.processSubscriptionsAct(subscriber); //need to ask
		}
		
		
	}


	@Override
	public void startProcessingProcessDCT(WebServiceContext task) {
		if(task.containsKey(param_subscriberID)){
		RBTDaemonManager.m_rbtDaemonManager = new RBTDaemonManager();
		rbtDaemonManager = RBTDaemonManager.m_rbtDaemonManager;
		rbtDaemonManager.getConfigValues();
		
		executor.getExecutor().execute(rbtDaemonManager);
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber((String)task.get(param_subscriberID));
		rbtDaemonManager.processSubscriptionsDct(subscriber); //need to ask
		}
	}



	
	
	
	
}
