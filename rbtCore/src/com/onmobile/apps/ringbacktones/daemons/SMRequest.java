package com.onmobile.apps.ringbacktones.daemons;

import java.util.concurrent.Callable;

import org.apache.log4j.MDC;
import org.javasimon.Split;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;

public class SMRequest implements Callable<String>, iRBTConstant
{
	Subscriber subscriber;
	SubscriberStatus selection;
	SubscriberDownloads download;
	ProvisioningRequests provisioningRequests;
	RequestType requestType;
	//Split split = null;
	
	public SMRequest(RequestType requestType)
	{
		this.requestType = requestType;
	}
	
	
	public Subscriber getSubscriber() {
		return subscriber;
	}


	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}


	public SubscriberStatus getSelection() {
		return selection;
	}


	public void setSelection(SubscriberStatus selection) {
		this.selection = selection;
	}


	public SubscriberDownloads getDownload() {
		return download;
	}


	public void setDownload(SubscriberDownloads download) {
		this.download = download;
	}
	
	public ProvisioningRequests getProvisioningRequests() {
		return provisioningRequests;
	}


	public void setProvisioningRequests(ProvisioningRequests provisioningRequests) {
		this.provisioningRequests = provisioningRequests;
	}


	@Override
	public String call() throws Exception
	{
		setMsisdnInMDC();
		switch (this.requestType)
		{
			case SM_BASE_ACT:
				processBaseAct();
				break;
			case SM_BASE_ACT_LOW:
				processBaseAct();
				break;
			case SM_BASE_DCT:
				processBaseDeact();
				break;
			case SM_SEL_ACT:
				processSelAct();
				break;
			case SM_SEL_ACT_LOW:
				processSelAct();
				break;
			case SM_SEL_DCT:
				processSelDeact();
				break;
			case SM_SEL_ACT_DIR:
				processSelActDirect();
				break;
			case SM_SEL_REN:
				RBTDaemonManager.m_rbtDaemonManager.processSelectionsRen(getSelection());
				break;
			case SM_DWN_ACT:
				RBTDaemonManager.m_rbtDaemonManager.processActDownload(getDownload());
				break;
			case SM_DWN_DCT:
				RBTDaemonManager.m_rbtDaemonManager.processDeactDownload(getDownload());
				break;
			case SM_PACK_ACT:
				RBTDaemonManager.m_rbtDaemonManager.processPacksAct(getProvisioningRequests());
				break;
			case SM_PACK_DCT:
				RBTDaemonManager.m_rbtDaemonManager.processPacksDct(getProvisioningRequests());
				break;
			default:
				break;
		}
		removeMsisdnFromMDC();
		return "DONE";
	}

	private void removeMsisdnFromMDC()
	{
		MDC.remove(mdc_msisdn);
	}


	private void setMsisdnInMDC()
	{
		String msisdn = null;
		if(getSubscriber() != null)
			msisdn = getSubscriber().subID();
		else if (getSelection() != null)
			msisdn = getSelection().subID();
		else if (getDownload() != null)
			msisdn = getDownload().subscriberId();
		else if (getProvisioningRequests() != null)
			msisdn = getProvisioningRequests().getSubscriberId();
		
		if(msisdn != null)
			MDC.put(mdc_msisdn, msisdn);
	}

	private void processBaseAct()
	{
		//split = RBTDaemonManager.smBaseActStopwatch.start();
		Subscriber subscriber = getSubscriber();
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_SM_DAEMON_ACT);
		boolean result = RBTDaemonManager.m_rbtDaemonManager.processSubscriptionsAct(subscriber);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, result);
		//split.stop();
	}

	private void processBaseDeact()
	{
		//split = RBTDaemonManager.smBaseDctStopwatch.start();
		Subscriber subscriber = getSubscriber();
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_SM_DAEMON_DCT);
		boolean result = RBTDaemonManager.m_rbtDaemonManager.processSubscriptionsDct(subscriber);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, result);
		//split.stop();
	}

	private void processSelAct()
	{
		//split = RBTDaemonManager.smSelActStopwatch.start();
		SubscriberStatus selection = getSelection();
		RBTNode node = RBTMonitorManager.getInstance().startNode(selection.subID(), RBTNode.NODE_SM_DAEMON_SEL_ACT);
		boolean result = RBTDaemonManager.m_rbtDaemonManager.processSelectionsAct(selection);
		RBTMonitorManager.getInstance().endNode(selection.subID(), node, result);
		//split.stop();
	}

	private void processSelActDirect()
	{
		SubscriberStatus selection = getSelection();
		RBTNode node = RBTMonitorManager.getInstance().startNode(selection.subID(), RBTNode.NODE_SM_DAEMON_SEL_ACT);
		boolean result = RBTDaemonManager.m_rbtDaemonManager.processDirectRecordsSel(selection);
		RBTMonitorManager.getInstance().endNode(selection.subID(), node, result);
	}

	private void processSelDeact()
	{
		//split = RBTDaemonManager.smSelDctStopwatch.start();
		SubscriberStatus selection = getSelection();
		RBTNode node = RBTMonitorManager.getInstance().startNode(selection.subID(), RBTNode.NODE_SM_DAEMON_SEL_DCT);
		boolean result = RBTDaemonManager.m_rbtDaemonManager.processSelectionsDct(selection);
		RBTMonitorManager.getInstance().endNode(selection.subID(), node, result);
		//split.stop();
	}
}
