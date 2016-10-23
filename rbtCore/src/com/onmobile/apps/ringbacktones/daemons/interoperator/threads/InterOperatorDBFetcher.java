package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.interoperator.InterOperatorCopyDaemon;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class InterOperatorDBFetcher extends Thread implements InterOperatorDBFetcherMBean
{
	public int operatorId = 0;
	public int status = -1;
	public Vector<InterOperatorCopyRequestBean> copyQueue = new Vector<InterOperatorCopyRequestBean>();
	public Vector<InterOperatorCopyRequestBean> pendingQueue = new Vector<InterOperatorCopyRequestBean>();
	static Logger logger = Logger.getLogger(InterOperatorDBFetcher.class);
	static int fetcherSleepTimeInSec = 120;
	static int mnpPushThreadPoolSize = 1;
	static int cleanUpThreadPoolSize = 1;
	static int contentAndOperatorPushThreadPoolSize = 1;
	static int fetchSize = 5000;
	static int cleanUpPeriodInMins = 24*60;

	public String getOperator()
	{
		return InterOperatorUtility.getRBTOperatorNameFromOperatorID(operatorId+"");
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public int getFetchSize()
	{
		return fetchSize;
	}
	
	public int getSleepTimeInSec()
	{
		return fetcherSleepTimeInSec;
	}
	
	public int getCopyQueueSize()
	{
		return copyQueue.size();
	}
	
	public int getPendingQueueSize()
	{
		return pendingQueue.size();
	}
	
	public String showCopyQueueRecords()
	{
		StringBuilder sBuilder = new StringBuilder();
		int count = 0;
		for (InterOperatorCopyRequestBean interOperatorCopyRequestBean : copyQueue)
		{
			if(count++ > 10)
				break;
			String currentBean = interOperatorCopyRequestBean.toString();
			sBuilder.append(currentBean);sBuilder.append("\n");
		}
		return sBuilder.toString();
	}
	
	public String showPendingQueueRecords()
	{
		StringBuilder sBuilder = new StringBuilder();
		for (InterOperatorCopyRequestBean interOperatorCopyRequestBean : pendingQueue)
		{
			String currentBean = interOperatorCopyRequestBean.toString();
			sBuilder.append(currentBean);sBuilder.append("\n");
		}
		return sBuilder.toString();
	}
	
	static
	{
		
		fetcherSleepTimeInSec = InterOperatorUtility.getParameterAsInt("RDC", "DB_FETCHER_SLEEP_TIME_IN_SEC", 30);
		mnpPushThreadPoolSize = InterOperatorUtility.getParameterAsInt("RDC", "MNP_PUSH_THREAD_POOL_SIZE", 1);
		cleanUpThreadPoolSize = InterOperatorUtility.getParameterAsInt("RDC", "CLEAN_UP_THREAD_POOL_SIZE", 1);
		contentAndOperatorPushThreadPoolSize = InterOperatorUtility.getParameterAsInt("RDC", "CONTENT_AND_OPERATOR_PUSH_THREAD_POOL_SIZE", 1);
		fetchSize = InterOperatorUtility.getParameterAsInt("RDC", "DB_FETCH_SIZE", 5000);	  
		cleanUpPeriodInMins = InterOperatorUtility.getParameterAsInt("RDC", "CLEAN_UP_PERIOD_IN_MIN", 24*60);	  
	}
	
	public void run()
	{
		while(true)
		{
			if(status == 1)
			{
				InterOperatorUtility.makeReportingFiles();
			}	
			synchronized (copyQueue)
			{
				if(copyQueue.size() == 0 && pendingQueue.size() == 0)
				{
					logger.info("copyQueue size=0 for status="+status+" and operatorId="+operatorId+". So hitting DB");
					List<InterOperatorCopyRequestBean> copyRequests = null;
					if(status == 4)
					{
						ArrayList<Integer> statusList = new ArrayList<Integer>();
						statusList.add(4);statusList.add(5);
						copyRequests = InterOperatorCopyRequestDao.listForOperatorAndInStatus(operatorId,statusList, fetchSize);
					}
					else if(status !=1)
						copyRequests = InterOperatorCopyRequestDao.listForStatusAndOperator(status,operatorId, fetchSize);
					else
						copyRequests = InterOperatorCopyRequestDao.listForLessThanTime(cleanUpPeriodInMins, fetchSize);
					if(copyRequests.size() != 0)
					{
						copyQueue.addAll(copyRequests);
						copyQueue.notifyAll();
					}
				}
				else
					logger.info("copyQueue size="+copyQueue.size()+". Now will sleep and let workers clear the queue.");
			}
			try 
			{
				logger.info("Sleeping for 30 sec");
				Thread.sleep(fetcherSleepTimeInSec*1000);
				logger.info("Woke up");
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	public InterOperatorDBFetcher(int operatorId, int status)
	{
		this.operatorId = operatorId;
		this.status = status;
		if(status == 0)
		{
			for(int i = 0; i < mnpPushThreadPoolSize; i++)
			{
				InterOperatorMnpPushingThread copyThreads = new InterOperatorMnpPushingThread(this);
				copyThreads.setName("MnpPush-Worker-"+i);
				copyThreads.start();
			}
			
		}
		else if(status == 1)
		{
			for(int i = 0; i < cleanUpThreadPoolSize; i++)
			{
				InterOperatorCleanUpThread copyThreads = new InterOperatorCleanUpThread(this);
				copyThreads.setName("CleanUp-Worker-"+i);
				copyThreads.start();
			}
			
		}
		else
		{
			
			for(int i = 0; i < contentAndOperatorPushThreadPoolSize; i++)
			{
				if(status == 2)
				{
					InterOperatorContentResolvingThread copyThreads = new InterOperatorContentResolvingThread(this);
					copyThreads.setName("ContentResolver-Opr-"+operatorId+"-Worker-"+i);
					copyThreads.start();
				}
				else if(status == 4)
				{
					InterOperatorOperatorPushingThread copyThreads = new InterOperatorOperatorPushingThread(this);
					copyThreads.setName("OprPush-Opr-"+operatorId+"-Worker-"+i);
					copyThreads.start();
				}
			}
		}
	}
}
