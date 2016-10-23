package com.onmobile.apps.ringbacktones.daemons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.content.RbtTempGroupMembers;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

/**
 * @author sridhar.sindiri
 *
 */
public class RbtTempGroupMemberThread extends Thread
{
	private static final Logger logger = Logger.getLogger(RBTUninorJMSRetailProcessor.class);
	private RBTDaemonManager rbtDaemonManager = null;

	private QueueConnection queueConnection = null;
	private QueueReceiver queueReceiver = null;

	/**
	 * @param rbtDaemonManager
	 * @throws JMSException 
	 * @throws NamingException 
	 */
	public RbtTempGroupMemberThread(RBTDaemonManager rbtDaemonManager) throws NamingException, JMSException
	{
		this.rbtDaemonManager = rbtDaemonManager;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			while (rbtDaemonManager != null && rbtDaemonManager.isAlive())
			{
				try {
					processRbtTempGroupMembers();

				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.debug("RBTUninorRetailProcessor Thread is Stopped working..");
		}
		catch(Exception e) {
			
		}
	}
	
	/**
	 * 
	 */
	private void processRbtTempGroupMembers()
	{
		try
		{
			List<RbtTempGroupMembers> list = RBTDBManager.getInstance().getGroupMembersByGroupMemberStatus();
			if(list == null || list.size() == 0) {
				return;
			}
			for(RbtTempGroupMembers tempGroupMember : list) {
				GroupRequest groupRequest = new GroupRequest(tempGroupMember.subscriberId());
				groupRequest.setGroupID("G" + tempGroupMember.groupID());
				groupRequest.setMemberID(tempGroupMember.callerID());
				groupRequest.setMemberName(tempGroupMember.callerName());
				if(tempGroupMember.groupMemberStatus() == 1) {
					RBTClient.getInstance().addGroupMember(groupRequest);
				}
				else if(tempGroupMember.groupMemberStatus() == 2) {
					RBTClient.getInstance().removeGroupMember(groupRequest);
				}
				
				RBTDBManager.getInstance().removeCallerFromRbtTempGroupMember(tempGroupMember.groupID(), tempGroupMember.callerID(), tempGroupMember.subscriberId());
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
