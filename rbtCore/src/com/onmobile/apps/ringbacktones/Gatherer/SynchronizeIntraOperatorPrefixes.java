package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class SynchronizeIntraOperatorPrefixes extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(SynchronizeIntraOperatorPrefixes.class);
	String _class = "SynchronizeIntraOperatorPrefixes";
	
	private RBTDBManager rbtDBManager = null;
	private RBTConnector rbtConnector=null;
	private RBTGatherer m_parentGathererThread = null;
	int m_syncHour = 1;
	long nextSyncTime = -1;

	public SynchronizeIntraOperatorPrefixes (RBTGatherer rbtGathererThread) throws Exception
	{
		m_parentGathererThread = rbtGathererThread;
		if(init())
			start();
		else
			throw new Exception(" In SynchronizeIntraOperatorPrefixes: Cannot init Parameters"); 
	}

	private boolean init()
	{
		logger.info("Entering");
		rbtConnector=RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();

		logger.info("Exiting");
		return true;
	}

	public void run()
	{
		logger.info("Entering");
		while(m_parentGathererThread.isAlive())
		{
			logger.info("Entering while loop");
			if(nextSyncTime== -1 || (System.currentTimeMillis()> nextSyncTime))
			{
				String allSitePrefixes = getPrefixesFromSites();
				logger.info("allSitePrefixes = "+allSitePrefixes);
				if(allSitePrefixes != null && allSitePrefixes.length() > 1)
					updatePrefixesAtSites(allSitePrefixes.substring(1));
				nextSyncTime = m_parentGathererThread.getnexttime(getParamAsInt("INTRA_OP_SYNC_PREFIX_HOUR", 1));
			}
			try {
				Date next_run_time = m_parentGathererThread.roundToNearestInterVal(getParamAsInt("GATHERER_SLEEP_INTERVAL", 5));
				long sleeptime = m_parentGathererThread.getSleepTime(next_run_time);
				if(sleeptime < 100)
					sleeptime = 500;
				logger.info(_class + " Thread : sleeping for "+sleeptime + " mSecs.");
				Thread.sleep(sleeptime);
				logger.info(_class + " Thread : waking up.");
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("Exiting");
	}

	private String getPrefixesFromSites()
	{
		logger.info("Entering");
		String allOperatorPrefixes = "";
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() == 0)
		{
			logger.info("Site Prefix Table is empty ");
			return null;
		}
		String sitePrefixes = null;
		StringBuffer response = null;
		Integer statusCode = null;
		String strUrl = null;

		for (int i = 0; i < prefixes.size(); i++)
		{
			if (prefixes.get(i).getSiteUrl() == null)
			{
				allOperatorPrefixes += "," + prefixes.get(i).getSitePrefix().trim();
				continue;
			}
			strUrl = prefixes.get(i).getSiteUrl();
			if (strUrl != null && strUrl.length() > 0)
			{
				strUrl = Tools.findNReplace(strUrl, "rbt_sms.jsp","rbt_prefix_sync.jsp");
				strUrl = Tools.findNReplace(strUrl, "?","");
				strUrl += "?ACTION=GET&CIRCLE_ID="+prefixes.get(i).getCircleID();
				try
				{
					response = new StringBuffer();
					statusCode = new Integer(-1);
					if (Tools.callURL(strUrl, statusCode, response))
					{
						sitePrefixes = response.toString().trim();
						logger.info("Response is -> "+ sitePrefixes);
						if (sitePrefixes != null && sitePrefixes.length() > 0 && sitePrefixes.indexOf("ERROR") == -1)
							allOperatorPrefixes += "," + sitePrefixes;
						else
						{
							allOperatorPrefixes += "," + prefixes.get(i).getSitePrefix().trim();
							continue;
						}

						if (!prefixes.get(i).getSitePrefix().trim().equalsIgnoreCase(response.toString().trim()))
						{
                            SitePrefix sitePrefix = prefixes.get(i).clone();
                            sitePrefix.setSitePrefix(sitePrefixes);
                            CacheManagerUtil.getSitePrefixCacheManager().updateSitePrefix(sitePrefix);
                        }
					}
					else
						allOperatorPrefixes += ","+ prefixes.get(i).getSitePrefix().trim();
				}
				catch (Exception e)
				{
					logger.error("", e);
					continue;
				}
			}
		}
		logger.info("Exiting with allOperatorPrefixes = "+allOperatorPrefixes);
		return allOperatorPrefixes;
	}

	private void updatePrefixesAtSites(String allOperatorPrefixes)
	{

		logger.info("Entering");
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() == 0)
		{
			logger.info("Site Prefix Table is empty ");
			return;
		}

		logger.info("allOperatorPrefixes " + allOperatorPrefixes);
		for (int i = 0; i < prefixes.size(); i++)
		{
			String strUrl = prefixes.get(i).getSiteUrl();
			if (strUrl == null || strUrl.length() <= 0)
				strUrl = "http://"+getParamAsString("SMS","JBOSS_IP", null)+":8080/rbt/rbt_prefix_sync.jsp?";
			else
				strUrl = Tools.findNReplace(strUrl, "rbt_sms.jsp","rbt_prefix_sync.jsp");
			strUrl += "ACTION=UPDATE&PARAM=OPERATOR_PREFIX&VALUE="+ allOperatorPrefixes.trim();

			StringBuffer response = new StringBuffer();
			Integer statusCode = new Integer(-1);
			Tools.callURL(strUrl, statusCode, response);
			logger.info("response is " + response.toString());
		}
		logger.info("Exiting");
	}


	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defualtVal);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}

	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}


}
