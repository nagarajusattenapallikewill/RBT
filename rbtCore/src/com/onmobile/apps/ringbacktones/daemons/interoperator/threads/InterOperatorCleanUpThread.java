package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class InterOperatorCleanUpThread extends Thread 
{
	private InterOperatorDBFetcher dbFetcher = null;
	static Logger logger = Logger.getLogger(InterOperatorDBFetcher.class);
	static Logger copyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestDao.class);
	static Logger oldCopyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestBean.class);
	
	public InterOperatorCleanUpThread(InterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}
	
	public void run()
	{
		while(true)
		{
			InterOperatorCopyRequestBean copyRequest = null; 
			try
			{
				synchronized (dbFetcher.copyQueue)
				{
					if(dbFetcher.copyQueue.size() > 0)
					{
						logger.info("Clean up thread found copyrequest."+dbFetcher.copyQueue.get(0));
						copyRequest = dbFetcher.copyQueue.remove(0);
						dbFetcher.pendingQueue.add(copyRequest);
					}
					else
					{
						try {
							logger.info("Clean up thread waiting as queue size="+dbFetcher.copyQueue.size());
							dbFetcher.copyQueue.wait();
						} catch (InterruptedException e) {
							logger.info("Clean up thread interrupted. Will check queue now");
						}
						continue;
					}	
				}
				
				logger.info("deleting "+copyRequest);
				copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(copyRequest));
				oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(copyRequest));
				InterOperatorUtility.writeEventLog(copyRequest, "UNKNOWN_OPERATOR");
				InterOperatorCopyRequestDao.delete(copyRequest.getCopyId());
			}
			catch(Exception e)
			{
				logger.error("Exception", e);
			}
			if(copyRequest != null)
				dbFetcher.pendingQueue.remove(copyRequest);
		}	
	}

	}
