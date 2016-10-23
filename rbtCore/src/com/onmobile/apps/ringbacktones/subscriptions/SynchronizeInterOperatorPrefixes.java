package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

public class SynchronizeInterOperatorPrefixes extends Thread implements iRBTConstant{
	
	private static Logger logger = Logger.getLogger(SynchronizeInterOperatorPrefixes.class);
	
	String _class = "SynchronizeInterOperatorPrefixes";
    private RBTDBManager rbtDBManager = null;
    private CommonCopyHelper m_parentCopyHelperThread = null;
    int m_syncHour = 1;
    long nextSyncTime = -1;
    String m_jboss_ip = null;
    
    public SynchronizeInterOperatorPrefixes (CommonCopyHelper rbtCopyHelperThread) throws Exception
    {
    	m_parentCopyHelperThread = rbtCopyHelperThread;
    	if(init())
    		start();
    	else
    		throw new Exception(" In SynchronizeInterOperatorPrefixes: Cannot init Parameters"); 
    }
    
    private boolean init()
    {
        rbtDBManager = RBTDBManager.getInstance();
        m_syncHour =  m_parentCopyHelperThread.getParameterAsInt("INTER_OP_SYNC_PREFIX_HOUR", 1);
        m_jboss_ip = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "JBOSS_IP", "localhost");
        return true;
    }
    
    public void run()
    {
    	while(m_parentCopyHelperThread.isAlive())
    	{
			if(nextSyncTime== -1 || (System.currentTimeMillis()> nextSyncTime))
			{
				getPrefixesFromSites();
				nextSyncTime = m_parentCopyHelperThread.getnexttime(m_syncHour);
			}
			try {
				logger.info("RBT::sleeeping.....");
				Thread.sleep(5*60*1000);
				logger.info("RBT::after sleeep.....");
			} catch (Exception e) {
				logger.error("", e);
			}
    	}
    }
    
    private String getPrefixesFromSites()
	{
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
            strUrl = prefixes.get(i).getSiteUrl();
            if (strUrl != null && strUrl.length() > 0)
            {
                strUrl = Tools.findNReplace(strUrl, "rbt_sms.jsp","rbt_prefix_sync.jsp");
                strUrl = Tools.findNReplace(strUrl, "?","");
                strUrl += "?ACTION=GET&PARAM=OPR_PREFIX";
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
        return allOperatorPrefixes;
	}
	
	}

