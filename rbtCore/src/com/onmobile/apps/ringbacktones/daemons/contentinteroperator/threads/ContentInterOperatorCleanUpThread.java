package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorCleanUpThread extends Thread
{
	private ContentInterOperatorDBFetcher dbFetcher = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);
	
	/**
	 * @param dbFetcher
	 */
	public ContentInterOperatorCleanUpThread(ContentInterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while(true)
		{
			ContentInterOperatorRequestBean contentRequest = null; 
			synchronized (dbFetcher.contentQueue)
			{
				if(dbFetcher.contentQueue.size() > 0)
				{
					logger.info("Clean up thread found contentRequest : " + dbFetcher.contentQueue.get(0));
					contentRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(contentRequest);
				}
				else
				{
					try
					{
						logger.info("Clean up thread waiting as queue size = " + dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					}
					catch (InterruptedException e)
					{
						logger.info("Clean up thread interrupted. Will check queue now");
					}
					continue;
				}	
			}
			
			logger.info("deleting " + contentRequest);
			ContentInterOperatorRequestDao.delete(contentRequest.getSequenceID());
			
			dbFetcher.pendingQueue.remove(contentRequest);
		}	
	}
}
