package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.RDCGroupMembers;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;

public class AffiliateGroupMembersDBFetcher extends Thread
{
	private static Logger logger = Logger.getLogger(AffiliateGroupDBFetcher.class);

	public int operatorId = 0;
	public int status = -1;

	public List<RDCGroupMembers> contentQueue = new ArrayList<RDCGroupMembers>();
	public List<RDCGroupMembers> pendingQueue = new ArrayList<RDCGroupMembers>();

	private static int fetcherSleepTimeInSec = 120;
	private static int groupThreadPoolSize = 1;

	static
	{
		fetcherSleepTimeInSec = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "DB_FETCHER_SLEEP_TIME_IN_SEC", 30);
		groupThreadPoolSize = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "GROUP_THREAD_POOL_SIZE", 1);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while(true)
		{
			synchronized (contentQueue)
			{
				if (contentQueue.size() == 0)
				{
					String operatorName = ContentInterOperatorUtility.getRBTOperatorNameFromOperatorID(String.valueOf(operatorId));
					logger.info("contentQueue size = 0 for operatorName = " + operatorName + ". So hitting DB");
					List<RDCGroupMembers> contentRequests = null;
					contentRequests = RBTDBManager.getInstance().getAffiliateGroupMembersByGroupMemberStatus(operatorName);
					if (contentRequests != null && contentRequests.size() != 0)
					{
						contentQueue.addAll(contentRequests);
						contentQueue.notifyAll();
					}
				}
				else
					logger.info("contentQueue size = " + contentQueue.size() + ". Now will sleep and let workers clear the queue.");
			}
			try 
			{
				logger.info("Sleeping for " + fetcherSleepTimeInSec + " secs");
				Thread.sleep(fetcherSleepTimeInSec * 1000);
				logger.info("Woke up");
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param operatorId
	 * @param status
	 */
	public AffiliateGroupMembersDBFetcher(int operatorId)
	{
		this.operatorId = operatorId;
		for (int i = 0; i < groupThreadPoolSize; i++)
		{
			AffiliateGroupMembersProcessThread groupMemberThread = new AffiliateGroupMembersProcessThread(this);
			groupMemberThread.setName("RDCGroupMember-Worker-" + i);
			groupMemberThread.start();
		}
	}
}

