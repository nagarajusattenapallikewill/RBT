package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorMnpPushingThread extends Thread
{
	private ContentInterOperatorDBFetcher dbFetcher = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);
	private static Logger mnpPushLogger = Logger.getLogger("mnpPushLogger");
	
	/**
	 * @param dbFetcher
	 */
	public ContentInterOperatorMnpPushingThread(ContentInterOperatorDBFetcher dbFetcher)
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
			ContentInterOperatorRequestBean copyRequest = null; 
			synchronized (dbFetcher.contentQueue)
			{
				if(dbFetcher.contentQueue.size() > 0)
				{
					logger.info("Mnp thread found contentrequest, " + dbFetcher.contentQueue.get(0));
					copyRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(copyRequest);
				}
				else
				{
					try
					{
						logger.info("Mnp thread waiting as queue size="+dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					}
					catch (InterruptedException e)
					{
						logger.info("Mnp thread interrupted. Will check queue now");
					}
					continue;
				}	
			}
			hitMnp(copyRequest);
			dbFetcher.pendingQueue.remove(copyRequest);
		}	
	}
	
	/**
	 * @param contentInterOperatorRequestBean
	 */
	private void hitMnp(ContentInterOperatorRequestBean contentInterOperatorRequestBean)
	{
		try
		{
			logger.info("Entered ");
			List<ContentInterOperatorRequestBean> pendingBeans = ContentInterOperatorRequestDao.listForMsisdnAndStatus(contentInterOperatorRequestBean.getMsisdn(), 1);
			if(pendingBeans.size() > 0)
			{
				contentInterOperatorRequestBean.setStatus(1);
				contentInterOperatorRequestBean.setMnpRequestTime(Calendar.getInstance().getTime());
				contentInterOperatorRequestBean.setMnpRequestType("DIRECT");
				ContentInterOperatorRequestDao.update(contentInterOperatorRequestBean);
				logger.info("Updated the mnpRequestType as DIRECT since a mnp request is pending for the subscriberID : "
						+ contentInterOperatorRequestBean.getMsisdn());
				return ;
			}	
			
			String mnpUrl = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "MNP_URL", null);
			logger.info("MNP url : " + mnpUrl);
			if (mnpUrl == null || mnpUrl.trim().length() == 0)
				return ;

			HashMap<String, String> parametersMap = new HashMap<String, String>();
			parametersMap.put("msisdn", contentInterOperatorRequestBean.getMsisdn());
			ContentInterOperatorHttpResponse ioHttpResponse = ContentInterOperatorHttpUtils.getResponse(mnpUrl, parametersMap, null);
			
			mnpPushLogger.info(ioHttpResponse.getFinalUrl() + "!" + ioHttpResponse.getHttpResponseCode());
			if (ioHttpResponse.getHttpResponseCode() == 202)
			{
				contentInterOperatorRequestBean.setStatus(1);
				contentInterOperatorRequestBean.setMnpRequestTime(Calendar.getInstance().getTime());
				contentInterOperatorRequestBean.setMnpRequestType("HTTP");
				ContentInterOperatorRequestDao.update(contentInterOperatorRequestBean);
			}
		}
		catch(Exception e)
		{
			logger.error("Exception while hitting mnp ", e);
		}
		catch(Throwable t)
		{
			logger.error("Throwable while hitting mnp ", t);
		}
	}
}
