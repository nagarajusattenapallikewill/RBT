package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class InterOperatorMnpPushingThread extends Thread 
{
	private InterOperatorDBFetcher dbFetcher = null;
	static Logger logger = Logger.getLogger(InterOperatorDBFetcher.class);
	static Logger mnpPushLogger = Logger.getLogger(InterOperatorMnpPushingThread.class);
	static Logger copyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestDao.class);
	static Logger oldCopyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestBean.class);
		
	public InterOperatorMnpPushingThread(InterOperatorDBFetcher dbFetcher)
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
						logger.info("Mnp thread found copyrequest."+dbFetcher.copyQueue.get(0));
						copyRequest = dbFetcher.copyQueue.remove(0);
						dbFetcher.pendingQueue.add(copyRequest);
					}
					else
					{
						try {
							logger.info("Mnp thread waiting as queue size="+dbFetcher.copyQueue.size());
							dbFetcher.copyQueue.wait();
						} catch (InterruptedException e) {
							logger.info("Mnp thread interrupted. Will check queue now");
						}
						continue;
					}	
				}
				hitMnp(copyRequest);
			}
			catch(Exception e)
			{
				logger.error("Exception", e);
			}
			if(copyRequest != null)
				dbFetcher.pendingQueue.remove(copyRequest);
		}	
	}
	
	private void hitMnp( InterOperatorCopyRequestBean interOperatorCopyRequestBean)
	{
		try
		{
			logger.info("Entered");
			List<InterOperatorCopyRequestBean> pendingBeans = InterOperatorCopyRequestDao.listForCopierAndStatus(interOperatorCopyRequestBean.getCopierMdn(), 1);
			if(pendingBeans.size() > 0)
			{
				interOperatorCopyRequestBean.setStatus(1);
				interOperatorCopyRequestBean.setMnpRequestTime(Calendar.getInstance().getTime());
				interOperatorCopyRequestBean.setMnpRequestType("DIRECT");
				InterOperatorCopyRequestDao.update(interOperatorCopyRequestBean);
				return ;
			}	
			
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "MNP_URL", null);
			String useSitePrefix = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "USE_SITE_PREFIX", "FALSE").getValue();
			logger.info("rdc url="+parameter);
			if((parameter == null || parameter.getValue() == null || parameter.getValue().trim().length() == 0) && useSitePrefix.equalsIgnoreCase("FALSE"))
				return ;
			
			if(useSitePrefix.equalsIgnoreCase("TRUE")) {
				SitePrefix sitePrefix = Utility.getPrefix(interOperatorCopyRequestBean.getCopierMdn()+"");
				int operatorID = 0;
				if(sitePrefix != null) {
					String operator = sitePrefix.getCircleID();
					String circleId = sitePrefix.getSiteName();
					operatorID = InterOperatorUtility.getOperatorIDFromMNPOperatorName(operator);
					operatorID = InterOperatorUtility.getInterchangedOperatorId(operatorID, circleId);
				}
				if(operatorID == 0) {
					interOperatorCopyRequestBean.setStatus(3);
					interOperatorCopyRequestBean.setMnpResponseTime(Calendar.getInstance().getTime());
					interOperatorCopyRequestBean.setMnpResponseType("HTTP");
					copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(interOperatorCopyRequestBean));
					oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(interOperatorCopyRequestBean));
					InterOperatorCopyRequestDao.delete(interOperatorCopyRequestBean.getCopyId());
				}
				else {
					interOperatorCopyRequestBean.setCopierOperatorId(operatorID);
					interOperatorCopyRequestBean.setStatus(2);
					interOperatorCopyRequestBean.setMnpRequestTime(Calendar.getInstance().getTime());
					interOperatorCopyRequestBean.setMnpRequestType("HTTP");
					InterOperatorCopyRequestDao.update(interOperatorCopyRequestBean);
				}
				return;
			}
			
			
			
			String url = parameter.getValue().trim();
			HashMap<String, String> parametersMap = new HashMap<String, String>();
			parametersMap.put("msisdn", interOperatorCopyRequestBean.getCopierMdn()+"");
			InterOperatorHttpResponse ioHttpResponse = InterOperatorHttpUtils.getResponse(url, parametersMap, null);
			
			mnpPushLogger.info(ioHttpResponse.getFinalUrl()+"!"+ ioHttpResponse.getHttpResponseCode());
			if(ioHttpResponse.getHttpResponseCode() == 202)
			{
				interOperatorCopyRequestBean.setStatus(1);
				interOperatorCopyRequestBean.setMnpRequestTime(Calendar.getInstance().getTime());
				interOperatorCopyRequestBean.setMnpRequestType("HTTP");
				InterOperatorCopyRequestDao.update(interOperatorCopyRequestBean);
			}
			else if (ioHttpResponse.getHttpResponseCode() == 400)
			{
				logger.info("deleting "+interOperatorCopyRequestBean);
				copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(interOperatorCopyRequestBean));
				oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(interOperatorCopyRequestBean));
				InterOperatorUtility.writeEventLog(interOperatorCopyRequestBean, "UNKNOWN_OPERATOR");
				InterOperatorCopyRequestDao.delete(interOperatorCopyRequestBean.getCopyId());
			}
			else
			{
				logger.info("MNP url response had issue in "+interOperatorCopyRequestBean);
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
