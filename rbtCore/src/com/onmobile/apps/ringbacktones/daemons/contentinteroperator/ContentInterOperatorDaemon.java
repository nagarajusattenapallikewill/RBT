package com.onmobile.apps.ringbacktones.daemons.contentinteroperator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads.AffiliateGroupDBFetcher;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads.AffiliateGroupMembersDBFetcher;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads.ContentInterOperatorDBFetcher;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads.FTPContentInterOperatorThread;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorDaemon
{
	private static Logger logger = Logger.getLogger(ContentInterOperatorDaemon.class);
	private static List<Integer> operatorList = new ArrayList<Integer>();

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			logger.info("ContentInterOperatorDaemon Started !!");
			listLiveOperators();
			if(operatorList.size() == 0)
			{
				logger.error("No live operators found. Exiting..");
				return;
			}

			//resolve the operator id
			startMnpPushingThreads();
			//resolve the content mapping
			startContentResolvingThreads();
			//send the request to respective operators
			startOperatorPushingThreads();
			//clean up
			startCleanUpThreads();
			//process the failed MNP call back requests
			startFtpPullThread();
			//process the group thread
			startGroupPushingOperatorThreads();
			//process the group member thread
			startGroupMemberPushingOperatorThreads();
		}
		catch(Exception e)
		{
			logger.error("Exception in main thread ", e);
		}
	}

	/**
	 * 
	 */
	private static void startFtpPullThread()
	{
		FTPContentInterOperatorThread ftpThread = new FTPContentInterOperatorThread();
		ftpThread.setName("FTPPullThread");
		ftpThread.start();
	}

	/**
	 * 
	 */
	private static void listLiveOperators()
	{
		String operatorIDsStr = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "LIVE_OPERATORS", null);
		if (operatorIDsStr == null || operatorIDsStr.trim().length() == 0)
			return;

		StringTokenizer stk = new StringTokenizer(operatorIDsStr.trim(), ",");
		while (stk.hasMoreTokens())
			operatorList.add(Integer.parseInt(stk.nextToken()));
		logger.info("operatorList = " + operatorList);
	}

	/**
	 * 
	 */
	private static void startMnpPushingThreads()
	{
		ContentInterOperatorDBFetcher dbFetcher = new ContentInterOperatorDBFetcher(0, 0);
		dbFetcher.setName("MnpPush-Db-Fetcher");
		dbFetcher.start();
	}

	/**
	 * 
	 */
	private static void startCleanUpThreads()
	{
		ContentInterOperatorDBFetcher dbFetcher = new ContentInterOperatorDBFetcher(0, 1);
		dbFetcher.setName("CleanUp-Db-Fetcher");
		dbFetcher.start();
	}

	/**
	 * 
	 */
	private static void startContentResolvingThreads()
	{
		for (int  operatorId : operatorList)
		{
			ContentInterOperatorDBFetcher dbFetcher = new ContentInterOperatorDBFetcher(operatorId, 2);
			dbFetcher.setName("ContentResolver-Db-Fetcher-Opr-" + operatorId);
			dbFetcher.start();
		}
	}

	/**
	 * 
	 */
	private static void startOperatorPushingThreads()
	{
		for (int  operatorId : operatorList)
		{
			ContentInterOperatorDBFetcher dbFetcher = new ContentInterOperatorDBFetcher(operatorId, 4);
			dbFetcher.setName("OprPush-Db-Fetcher-Opr-" + operatorId);
			dbFetcher.start();
		}	
	}
	
	private static void startGroupPushingOperatorThreads()
	{
		for (int  operatorId : operatorList)
		{
			AffiliateGroupDBFetcher dbFetcher = new AffiliateGroupDBFetcher(operatorId);
			dbFetcher.setName("Group-Db-Fetcher-Opr-" + operatorId);
			dbFetcher.start();
		}	
	}
	
	private static void startGroupMemberPushingOperatorThreads()
	{
		for (int  operatorId : operatorList)
		{
			AffiliateGroupMembersDBFetcher dbFetcher = new AffiliateGroupMembersDBFetcher(operatorId);
			dbFetcher.setName("GroupMember-Db-Fetcher-Opr-" + operatorId);
			dbFetcher.start();
		}	
	}
	
}
