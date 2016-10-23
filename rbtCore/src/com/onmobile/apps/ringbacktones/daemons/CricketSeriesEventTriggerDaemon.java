package com.onmobile.apps.ringbacktones.daemons;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author sridhar.sindiri
 *
 */
public class CricketSeriesEventTriggerDaemon extends Thread 
{
	private static Logger logger = Logger.getLogger(CricketSeriesEventTriggerDaemon.class);
	private static Logger smTriggerLogger = Logger.getLogger(CricketSeriesEventTriggerDaemon.class.getName() + ".log1");

	private static boolean runDaemon = true;

	private static final int status_untriggered = 0;
	private static final int status_triggered   = 1;

	private static CricketSeriesEventTriggerDaemon cricketSeriesEventTriggerDaemon = null;
	private static Object syncObj = new Object();

	/**
	 * @param args
	 */
	public static void main(String args[])
	{
		CricketSeriesEventTriggerDaemon cricketSeriesTriggerDaemon = CricketSeriesEventTriggerDaemon.getInstance();
		cricketSeriesTriggerDaemon.setName("CRICKET_SERIES_EVENT_TRIGGER_DAEMON");
		cricketSeriesTriggerDaemon.start();
	}

	/**
	 * @return the Instance of CricketSeriesEventTriggerDaemon
	 */
	public static CricketSeriesEventTriggerDaemon getInstance() 
	{
		if (cricketSeriesEventTriggerDaemon == null)
		{
			synchronized (syncObj) 
			{
				if (cricketSeriesEventTriggerDaemon == null) 
				{
					try 
					{
						cricketSeriesEventTriggerDaemon = new CricketSeriesEventTriggerDaemon();
					}
					catch (Throwable e) 
					{
						logger.error("", e);
						cricketSeriesEventTriggerDaemon = null;
					}
				}
			}
		}
		return cricketSeriesEventTriggerDaemon;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CRICKET", "SM_SERIES_EVENT_TRIGGER_URL");
		if (params == null || params.getValue() == null)
		{
			logger.error("SM_SERIES_EVENT_TRIGGER URL not configured, so stopping the daemon");
			stopThread();
		}

		while (runDaemon)
		{
			FeedSchedule[] feedSchedules = RBTDBManager.getInstance().getActiveFeedSchedulesByStatus(status_untriggered);
			if (feedSchedules != null && feedSchedules.length != 0)
			{
				for (FeedSchedule feedSchedule : feedSchedules)
				{
					int feedID = feedSchedule.feedID();
					Date feedStartTime = feedSchedule.startTime();
					Date feedEndTime = feedSchedule.endTime();
					long feedDurationInMillis = feedEndTime.getTime() - feedStartTime.getTime();
					long feedDurationInHours = feedDurationInMillis / (1000 * 60 * 60);
					if (feedDurationInHours >= 168)
						feedDurationInHours = 167;

					String smTriggerUrl = params.getValue(); 
					smTriggerUrl = smTriggerUrl.replaceAll("%triggerkey%", "RBT_SEL_" + feedSchedule.classType());
					smTriggerUrl = smTriggerUrl.replaceAll("%refid%", String.valueOf(feedID));
					smTriggerUrl = smTriggerUrl.replaceAll("%duration%", String.valueOf(feedDurationInHours));

					HttpParameters httpParameters = new HttpParameters(smTriggerUrl);
					logger.info("RBT:: SM URL: " + smTriggerUrl + " httpParameters: " + httpParameters);

					long triggerStartTime = System.currentTimeMillis();
					HttpResponse httpResponse = null;
					try {
						httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
					}
					catch (Exception e) {
						logger.error("", e);
					}

					if (httpResponse != null)
						smTriggerLogger.info(smTriggerUrl + ", " + httpResponse.getResponse().trim() + ", " + (System.currentTimeMillis() - triggerStartTime));
					else
						smTriggerLogger.info(smTriggerUrl + ", " + httpResponse + ", " + (System.currentTimeMillis() - triggerStartTime));

					if (httpResponse != null)
					{
						String[] responseStatus = httpResponse.getResponse().trim().split("\\|");
						String response = responseStatus[0];
						if (response.equalsIgnoreCase("SUCCESS"))
						{
							boolean success = RBTDBManager.getInstance().updateTriggerStatus(feedID, status_triggered);
							if (success)
								logger.info("Successfully updated status to "+ status_triggered + " in the RBT_FEED_SCHEDULE table for feedID:" + feedID);
							else
								logger.info("Failed updating status to " + status_triggered + " in the RBT_FEED_SCHEDULE table for feedID:" + feedID); 
						}
					}
				}
			}
			String sleepTimeStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("CRICKET", "SLEEP_TIME_IN_MINS_FOR_SERIES_TRIGGER_DAEMON", "2");
			try
			{
				logger.info("Sleeping for " + sleepTimeStr + " minutes ");
				Thread.sleep(Integer.parseInt(sleepTimeStr) * 60 * 1000);
			}
			catch(Exception e)
			{
				stopThread();
			}
		}
	}

	/**
	 * stops the daemon thread
	 */
	public void stopThread()
	{
		runDaemon = false;
	}

	/**
	 * starts the daemon thread
	 */
	public void startThread()
	{
		runDaemon = true;
	}
}
