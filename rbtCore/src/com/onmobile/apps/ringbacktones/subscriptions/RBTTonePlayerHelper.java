package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class RBTTonePlayerHelper extends Thread
{
	private static Logger logger = Logger.getLogger(RBTTonePlayerHelper.class);
	
    String[] m_validServerIP = null;
    String m_countryPrefix = "91";

    boolean m_usePoolDefault = true;
    boolean m_usePool = m_usePoolDefault;

    private static Object m_lock = new Object();
    private static Hashtable m_shuffleTable = null;
    private static String m_feedFile = null;
    private static RBTTonePlayerHelper rbtTonePlayer = null;

	private static final long ONV_CACHE_REFRESH_TIME_SECS = 5 * 60;

	private static Object m_initLock = new Object();
	
    public static RBTTonePlayerHelper init()
    {
        if (rbtTonePlayer == null)
        {
            synchronized(m_initLock)
			{
				if(rbtTonePlayer != null)
					return rbtTonePlayer;
				try
				{
					rbtTonePlayer = new RBTTonePlayerHelper();
	            }
		        catch (Exception e)
			    {
				    rbtTonePlayer = null;
				}
			}
        }

        return rbtTonePlayer;
    }

    private RBTTonePlayerHelper() throws Exception
    {
        Tools.init("RBT_WAR_TONEPLAYER", false);
        String validServerIP = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_SERVER_IP", null);
        if (validServerIP != null)
        {
        	StringTokenizer tokens = new StringTokenizer(validServerIP, ",");
        	List ipList = new ArrayList();
        	while (tokens.hasMoreTokens())
        	{
        		ipList.add(tokens.nextToken());
        	}
        	if (ipList.size() > 0)
        	{
        		m_validServerIP = (String[]) ipList.toArray(new String[0]);
        	}
        }
        else
        {
        	logger.info("VALID SERVER IP MISSING IN CONFIGURATION");
        }

        start();
        //initializeShuffleTable();
        //            m_last_content_update_date = Tools.changeDateFormat(Calendar
        //                    .getInstance().getTime());

        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
    }

    private void initializeShuffleTable()
    {
        Hashtable tempTable = new Hashtable();
        tempTable = RBTDBManager.getInstance()
                .getShuffleCategories();
		FeedStatus feedStatus = RBTDBManager.getInstance().getFeedStatus("CRICKET");
		String s = null;
		if(feedStatus != null)
		{
			if(feedStatus.status() != null && feedStatus.status().equalsIgnoreCase("ON"))
			{
				String tmp = feedStatus.file();
				if(tmp != null)
				{
					s = tmp;
					if(tmp.indexOf(",") != -1)
						s = tmp.substring(tmp.lastIndexOf(","));
				}
			}
		}

        if (tempTable != null && tempTable.size() > 0)
        {
            synchronized (m_lock)
            {
                m_shuffleTable = tempTable;
				m_feedFile = s;
            }
        }
    }

    public Hashtable getShuffleTable()
    {
        synchronized (m_lock)
        {
            return m_shuffleTable;
        }
    }

    public String getFeedFile()
    {
        synchronized (m_lock)
        {
            return m_feedFile;
        }
    }

	public boolean isValidServerIP(String strIP)
    {
        if(m_validServerIP != null)
		{
			for (int i = 0; i < m_validServerIP.length; i++)
			{
				if (strIP.equalsIgnoreCase(m_validServerIP[i]))
					return true;
			}
		}
		return false;
    }

    public SubscriberStatus getRBTwavFile(String strSubID, String callerID,
            String type, Hashtable shuffleTable)
    {
        return (RBTDBManager.getInstance()
                .getSubscriberFile(strSubID, callerID, type, shuffleTable, m_feedFile));
    }

    public void run()
	{
		while(true)
		{
			RBTTonePlayerHelper.init().initializeShuffleTable();
			try
			{
				Thread.sleep(ONV_CACHE_REFRESH_TIME_SECS * 1000);
			}
			catch(Exception e)
			{
		
			}
		}
	}
	
	public void doPlayerHangUp(String subscriberID,
            SubscriberStatus subscriberStatus, Hashtable shuffleTable)
    {
        RBTDBManager.getInstance()
                .doPlayerHangup(subscriberID, subscriberStatus, shuffleTable);
    }
	
	public String subID(String strSubID) 
    { 
        return (RBTDBManager.getInstance().subID(strSubID)); 
    } 


}