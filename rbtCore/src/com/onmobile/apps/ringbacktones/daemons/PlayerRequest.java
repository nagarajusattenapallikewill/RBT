package com.onmobile.apps.ringbacktones.daemons;

import java.util.concurrent.Callable;

import org.apache.log4j.MDC;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;

public class PlayerRequest implements Callable<String>, iRBTConstant
{
	String subscriberId;
	Subscriber subscriber;
	ViralSMSTable viralRecord;
	RequestType requestType;
	
	//Split split = null;
	public PlayerRequest(RequestType requestType)
	{
		this.requestType = requestType;
	}
	
	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	public ViralSMSTable getViralRecord() {
		return viralRecord;
	}

	public void setViralRecord(ViralSMSTable viralRecord) {
		this.viralRecord = viralRecord;
	}

	@Override
	public String call() throws Exception
	{
		setMsisdnInMDC();
		switch (this.requestType)
		{
			case PLAYER_BASE_ACT:
				processBaseAct();
				break;
			case PLAYER_BASE_DCT:
				processBaseDeact();
				break;
			case PLAYER_BASE_SUS:
				processBaseSus();
				break;
			case PLAYER_SEL_ACT:
				processSelAct();
				break;
			case PLAYER_SEL_DCT:
				processSelDeact();
				break;
			case PLAYER_CHANGE_MSISDN:
				processChangeMsisdn();
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
		if (getSubscriberId() != null)
			msisdn = getSubscriberId();
		else if(getSubscriber() != null)
			msisdn = getSubscriber().subID();
		else if (getViralRecord() != null)
			msisdn = getViralRecord().subID();
		
		if(msisdn != null)
			MDC.put(mdc_msisdn, msisdn);
	}

	private void processBaseAct()
	{
		Subscriber subscriber = getSubscriber();
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_PLAYER_DAEMON_SUB);
		boolean response = RBTPlayerUpdateDaemon.updateSubscribersInPlayer(subscriber, false);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, response);
	}

	private void processBaseDeact()
	{
		Subscriber subscriber = getSubscriber();
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_PLAYER_DAEMON_SUB);
		boolean response = RBTPlayerUpdateDaemon.deactivateUsersInPlayerDB(subscriber);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, response);
	}

	private void processBaseSus()
	{
		Subscriber subscriber = getSubscriber();
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_PLAYER_DAEMON_SUB);
		boolean response = RBTPlayerUpdateDaemon.updateSubscribersInPlayer(subscriber,true);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, response);
	}

	private void processSelAct()
	{
		//split = RBTPlayerUpdateDaemon.playerSelActStopwatch.start();
		String subscriberId = getSubscriberId();
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_PLAYER_DAEMON_SEL);
		boolean response = RBTPlayerUpdateDaemon.addSelectionsToplayer(subscriber);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, response);
		//split.stop();
	}

	private void processSelDeact()
	{
		String subscriberId = getSubscriberId();
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		RBTNode node = RBTMonitorManager.getInstance().startNode(subscriber.subID(), RBTNode.NODE_PLAYER_DAEMON_SEL);
		boolean response = RBTPlayerUpdateDaemon.removeSelectionsFromplayer(subscriber);
		RBTMonitorManager.getInstance().endNode(subscriber.subID(), node, response);
	}
	
	private void processChangeMsisdn()
	{
		ViralSMSTable viralRecord = getViralRecord();
		boolean success = RBTPlayerUpdateDaemon.sendDeactivationRequestToPlayer(viralRecord.subID(), viralRecord.callerID());
		
		if(success)
			RBTPlayerUpdateDaemon.dbManager.updateViralPromotion(viralRecord.subID(), viralRecord.callerID(), viralRecord.sentTime(),"CHANGEMSISDN","CHANGEDMSISDN",null,null);
	}
}
