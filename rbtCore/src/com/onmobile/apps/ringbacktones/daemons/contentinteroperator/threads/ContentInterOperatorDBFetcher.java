package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;


/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorDBFetcher extends Thread
{
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);

	public int operatorId = 0;
	public int status = -1;

	public List<ContentInterOperatorRequestBean> contentQueue = new ArrayList<ContentInterOperatorRequestBean>();
	public List<ContentInterOperatorRequestBean> pendingQueue = new ArrayList<ContentInterOperatorRequestBean>();

	private static int fetcherSleepTimeInSec = 120;
	private static int mnpPushThreadPoolSize = 1;
	private static int cleanUpThreadPoolSize = 1;
	private static int contentAndOperatorPushThreadPoolSize = 1;
	private static int fetchSize = 5000;
	private static int cleanUpTimeInMins = 120;

	static
	{
		fetcherSleepTimeInSec = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "DB_FETCHER_SLEEP_TIME_IN_SEC", 30);
		mnpPushThreadPoolSize = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "MNP_PUSH_THREAD_POOL_SIZE", 1);
		cleanUpThreadPoolSize = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "CLEAN_UP_THREAD_POOL_SIZE", 1);
		contentAndOperatorPushThreadPoolSize = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "CONTENT_AND_OPERATOR_PUSH_THREAD_POOL_SIZE", 1);
		fetchSize = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "DB_FETCH_SIZE", 5000);
		cleanUpTimeInMins = RBTParametersUtils.getParamAsInt("CONTENT_INTER_OPERATORABILITY", "CLEAN_UP_TIME_IN_MINS", 120);
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
					logger.info("contentQueue size = 0 for status = " + status + " and operatorId = " + operatorId + ". So hitting DB");
					List<ContentInterOperatorRequestBean> contentRequests = null;
					if (status == 4)
					{
						ArrayList<Integer> statusList = new ArrayList<Integer>();
						statusList.add(4);
						statusList.add(5);
						contentRequests = ContentInterOperatorRequestDao.listForOperatorAndInStatus(operatorId, statusList, fetchSize);
					}
					else if (status != 1)
						contentRequests = ContentInterOperatorRequestDao.listForStatusAndOperator(status, operatorId, fetchSize);
					else
						contentRequests = ContentInterOperatorRequestDao.listForLessThanTime(cleanUpTimeInMins, fetchSize);
					if (contentRequests.size() != 0)
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
	public ContentInterOperatorDBFetcher(int operatorId, int status)
	{
		this.operatorId = operatorId;
		this.status = status;
		if (status == 0)
		{
			for (int i = 0; i < mnpPushThreadPoolSize; i++)
			{
				ContentInterOperatorMnpPushingThread copyThreads = new ContentInterOperatorMnpPushingThread(this);
				copyThreads.setName("MnpPush-Worker-" + i);
				copyThreads.start();
			}
		}
		else if (status == 1)
		{
			for (int i = 0; i < cleanUpThreadPoolSize; i++)
			{
				ContentInterOperatorCleanUpThread copyThreads = new ContentInterOperatorCleanUpThread(this);
				copyThreads.setName("CleanUp-Worker-" + i);
				copyThreads.start();
			}
		}
		else
		{
			for (int i = 0; i < contentAndOperatorPushThreadPoolSize; i++)
			{
				if (status == 2)
				{
					InterOperatorContentResolvingThread copyThreads = new InterOperatorContentResolvingThread(this);
					copyThreads.setName("ContentResolver-Opr-" + operatorId + "-Worker-" + i);
					copyThreads.start();
				}
				else if (status == 4)
				{
					ContentInterOperatorOperatorPushingThread copyThreads = new ContentInterOperatorOperatorPushingThread(this);
					copyThreads.setName("OprPush-Opr-" + operatorId + "-Worker-" + i);
					copyThreads.start();
				}
			}
		}
	}
}
