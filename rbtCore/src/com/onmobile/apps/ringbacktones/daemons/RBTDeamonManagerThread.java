package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.monitor.RBTMonitorManager;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;

public class RBTDeamonManagerThread extends Thread
{
	private static Logger logger = Logger.getLogger(RBTDeamonManagerThread.class);
	
	String threadType = null;
	ArrayList typeTable = null;
	RBTDaemonManager m_rbtDaemonManager = null;
	boolean m_Continue = true;
	public RBTDeamonManagerThread(RBTDaemonManager rbtDaemonManager, String type, ArrayList queue)
	{
		logger.info("Created with type "+type);
		threadType = type;
		m_rbtDaemonManager = rbtDaemonManager;
		typeTable = queue;
	}

	public void run()
	{
		while(m_Continue && m_rbtDaemonManager != null && m_rbtDaemonManager.isAlive())
		{
			//logger.info("entered while");
			Object obj = null;
			try
			{
				synchronized(typeTable)
				{
					// If the typeTable is still not populated it waits until gets a notification after population
					while(typeTable.size()==0)
						typeTable.wait();

						obj = typeTable.get(0);
						if(obj.toString().startsWith("PROCESSING")) 
                            continue;

						typeTable.remove(0);
						
						typeTable.add(typeTable.size(), "PROCESSING"+obj.toString()); 
				}

				processObject(obj);

			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
				logger.error("", ie);
				break;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("", e);
				
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				logger.error("", t);
			}
			finally
			{
				synchronized(typeTable) 
				{ 
					if(obj != null)
					  typeTable.remove("PROCESSING"+obj.toString()); 
			    }
			}
		}
	}

	private void processObject(Object obj)
	{
		if(threadType.equalsIgnoreCase("BASE-ACT-DIRECT")) {
			Subscriber sub = (Subscriber) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(sub.subID(), RBTNode.NODE_SM_DAEMON_ACT);
			boolean result = m_rbtDaemonManager.processDirectRecordsSub(sub);
			RBTMonitorManager.getInstance().endNode(sub.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("BASE-DEACT-DIRECT")) {
			Subscriber sub = (Subscriber) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(sub.subID(), RBTNode.NODE_SM_DAEMON_DCT);
			boolean result = m_rbtDaemonManager.processDirectRecordsDct(sub);
			RBTMonitorManager.getInstance().endNode(sub.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("BASE-ACT") || threadType.equalsIgnoreCase("BASE-ACT_LOW")) {
			Subscriber sub = (Subscriber) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(sub.subID(), RBTNode.NODE_SM_DAEMON_ACT);
			boolean result = m_rbtDaemonManager.processSubscriptionsAct(sub);
			RBTMonitorManager.getInstance().endNode(sub.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("BASE-DEACT")) {
			Subscriber sub = (Subscriber) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(sub.subID(), RBTNode.NODE_SM_DAEMON_DCT);
			boolean result = m_rbtDaemonManager.processSubscriptionsDct((Subscriber) obj);
			RBTMonitorManager.getInstance().endNode(sub.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("SEL-DEACT")) {
			SubscriberStatus sub = (SubscriberStatus) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(sub.subID(), RBTNode.NODE_SM_DAEMON_SEL_DCT);
			boolean result = m_rbtDaemonManager.processSelectionsDct((SubscriberStatus) obj);
			RBTMonitorManager.getInstance().endNode(sub.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("SEL-REN"))   // this is for all sel renewal requests, valid only in ESIA
			m_rbtDaemonManager.processSelectionsRen((SubscriberStatus) obj);
		else if(threadType.equalsIgnoreCase("SEL-ACT") || threadType.equalsIgnoreCase("SEL-ACT_LOW")) {
			SubscriberStatus ss = (SubscriberStatus) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(ss.subID(), RBTNode.NODE_SM_DAEMON_SEL_ACT);
			boolean result = m_rbtDaemonManager.processSelectionsAct(ss);
			RBTMonitorManager.getInstance().endNode(ss.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("SEL-ACT-DIRECT")) {
			SubscriberStatus ss = (SubscriberStatus) obj;
			RBTNode node = RBTMonitorManager.getInstance().startNode(ss.subID(), RBTNode.NODE_SM_DAEMON_SEL_ACT);
			boolean result = m_rbtDaemonManager.processDirectRecordsSel((SubscriberStatus) obj);
			RBTMonitorManager.getInstance().endNode(ss.subID(), node, result);
		}
		else if(threadType.equalsIgnoreCase("DOWNLOAD-ACT")){
			m_rbtDaemonManager.processActDownload((SubscriberDownloads) obj);
		}
		else if(threadType.equalsIgnoreCase("DOWNLOAD-DEACT")){
			m_rbtDaemonManager.processDeactDownload((SubscriberDownloads) obj);
		}
		else if(threadType.equalsIgnoreCase("PACK-ACT")){
			m_rbtDaemonManager.processPacksAct((ProvisioningRequests) obj);
		}
		else if(threadType.equalsIgnoreCase("PACK-DEACT")){
			m_rbtDaemonManager.processPacksDct((ProvisioningRequests) obj);
		}	}

	public void interupptMe()
	{
		try
		{
			this.interrupt();
		}
		catch(Throwable t)
		{	
			logger.error("", t);
		}

		m_Continue = false;

		try
		{
			this.join();
		}
		catch(Throwable t)
		{	
			logger.error("", t);
		}
	
	}
}

