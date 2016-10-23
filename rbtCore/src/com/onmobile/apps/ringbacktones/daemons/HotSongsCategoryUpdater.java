/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author vinayasimha.patil
 *
 */
public class HotSongsCategoryUpdater implements Runnable
{
	private static Logger logger = Logger.getLogger(HotSongsCategoryUpdater.class);
	
	private Object waitObject = null;
	private Thread hotSongsCategoryUpdaterThread = null;
	private Timer hotSongsCategoryUpdaterTimer = null;
	protected static ParametersCacheManager parametersCacheManager = null;
	private CategoryRefreshTask categoryRefreshTask = null;
	private ContentSyncTask contentSyncTask = null;

	public HotSongsCategoryUpdater()
	{
		waitObject = new Object();
		hotSongsCategoryUpdaterTimer = new Timer();
		hotSongsCategoryUpdaterThread = new Thread(this);
		hotSongsCategoryUpdaterThread.start();
	}

	/* (non-Javadoc)
	 * @see java.util.Runnable#run()
	 */
	public void run()
	{
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();

		int failureCount = 0;
		int backupDays = 0;
		String updatingTime = null;
		
		List hotSongsDays = null;
		Parameters param = parametersCacheManager.getParameter(iRBTConstant.TATADAEMON, "HOT_SONGS_DAYS", null);
		if(param != null)
		{
			hotSongsDays = getTokenizedList(param.getValue().trim(), ",", false);
		}
		
		param = parametersCacheManager.getParameter(iRBTConstant.TATADAEMON, "HOT_SONGS_CATEGORY_UPDATE_TIME", null);
		if(param != null)
		{
			updatingTime = param.getValue().trim();
		}
		
		param = parametersCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACCESS_TABLE_BACKUP_DAYS", "0");
		if(param != null)
		{
			try
			{
				backupDays = Integer.parseInt(param.getValue().trim());
			}
			catch(Exception e)
			{
				backupDays = 0;
			}
		}
		
		
		Date updatingStartTime = new Date();
		Calendar calender = Calendar.getInstance();
		Date curDate = new Date();
		if(updatingTime != null)
		{
			StringTokenizer tokens = new StringTokenizer(updatingTime, ":");
			if(tokens.countTokens() == 2)
			{
				int hour = Integer.parseInt(tokens.nextToken());
				int minute = Integer.parseInt(tokens.nextToken());
        
				calender.set(Calendar.HOUR_OF_DAY, hour);
				calender.set(Calendar.MINUTE, minute);
			}
		}
		
		Date tmpUpdateTime = calender.getTime();
		if(curDate.after(calender.getTime()))
			calender.add(Calendar.DAY_OF_YEAR, 1);
		updatingStartTime = calender.getTime();
		
		for(int i = 0; i < hotSongsDays.size(); i++)
		{
			int hotSongsCategoryID = -1;
			int noOfhotSongsInCategory = 0;
			int noOfhotSongsDays = 0;

			if(Integer.parseInt((String)hotSongsDays.get(i)) != 0 && null != hotSongsDays.get(i))
			{
				noOfhotSongsDays = Integer.parseInt(((String)hotSongsDays.get(i)).trim());
				String hotSongsCategoryIDStr = null;
				param = parametersCacheManager.getParameter(iRBTConstant.TATADAEMON, "HOT_SONGS_CATEGORY_NAME_"+noOfhotSongsDays, null);
				if(param != null)
				{
					hotSongsCategoryIDStr = param.getValue().trim();
				}
				
				try
				{
					hotSongsCategoryID = Integer.parseInt(hotSongsCategoryIDStr);
				}
				catch(NumberFormatException nfe)
				{
					logger.info("RBT:: Invalid CategoryID = "+ hotSongsCategoryIDStr);
				}
				
				
				param = parametersCacheManager.getParameter(iRBTConstant.TATADAEMON, "NO_OF_HOT_SONGS_IN_CATEGORY_"+ noOfhotSongsDays, "0");
				if(param != null)
				{
					try
					{
						noOfhotSongsInCategory = Integer.parseInt(param.getValue().trim());
					}
					catch(Exception e)
					{
						noOfhotSongsInCategory = 0;
					}
				}
			}

			if(hotSongsCategoryID != -1)
			{
				logger.info("RBT:: hotSongsCategoryID = "+ hotSongsCategoryID + " for noOfhotSongsDays = "+ noOfhotSongsDays);

				if(curDate.after(tmpUpdateTime))
				{
					HotSongsCategoryUpdateTask firstUpdateTask = new HotSongsCategoryUpdateTask(hotSongsCategoryID, noOfhotSongsInCategory, noOfhotSongsDays);
					firstUpdateTask.run();
				}

				HotSongsCategoryUpdateTask hotSongsCategoryUpdateTask = new HotSongsCategoryUpdateTask(hotSongsCategoryID, noOfhotSongsInCategory, noOfhotSongsDays);
				hotSongsCategoryUpdaterTimer.scheduleAtFixedRate(hotSongsCategoryUpdateTask, updatingStartTime, 1000*60*60*24);
			}
			else
			{
				logger.info("RBT:: hotSongsCategoryName not defined for noOfhotSongsDays = "+ noOfhotSongsDays);
				failureCount++;

			}
		}

		ClearAccessTableTask clearAccessTableTask = new ClearAccessTableTask(backupDays);
		hotSongsCategoryUpdaterTimer.scheduleAtFixedRate(clearAccessTableTask, updatingStartTime, 1000*60*60*24);

		logger.info("RBT:: updatingStartTime = "+ updatingStartTime);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance(); 

		String[] categoriesStr = null;
		param = parametersCacheManager.getParameter("TATA_RBT_DAEMON", "REFRESH_CATEGORIES");
		if(param != null)
		{
			categoriesStr = getTokenizedList(param.getValue().trim(), ",", false).toArray(new String[0]);
		}
		
		Parameters catRfshParameter = parametersCacheManager.getParameter("TATA_RBT_DAEMON", "CATEGORY_REFRESH_DATE"); 
		if(catRfshParameter != null && categoriesStr != null && categoriesStr.length > 0) 
		{
			int[] categories = new int[categoriesStr.length];
			for (int j = 0; j < categoriesStr.length; j++)
			{
				try
				{
					categories[j] = Integer.parseInt(categoriesStr[j]);
				}
				catch (NumberFormatException nfe)
				{
					logger.info("RBT:: Invalid Category Id = "+ categoriesStr[j]);
				}
			}
			categoryRefreshTask = new CategoryRefreshTask(categories, 7);
		} 

		Parameters contentSyncParameter = parametersCacheManager.getParameter("TATA_RBT_DAEMON", "CONTENT_SYNC_DATE"); 
		if(contentSyncParameter != null) 
		{ 
			contentSyncTask = new ContentSyncTask(); 
		} 


		synchronized (waitObject)
		{
			try 
			{
				waitObject.wait();
			} 
			catch (InterruptedException e) 
			{
				logger.error("", e);
			}
		}
	}
	
	public void stop()
	{
		if(hotSongsCategoryUpdaterThread.isAlive())
		{
			if(categoryRefreshTask != null) 
				categoryRefreshTask.stop();
			if(contentSyncTask != null) 
				contentSyncTask.stop(); 
			hotSongsCategoryUpdaterTimer.cancel();
			hotSongsCategoryUpdaterThread.interrupt();
			logger.info("RBT:: Stopped HotSongsCategoryUpdater");
		}
		else
		{
			logger.info("RBT:: HotSongsCategoryUpdater is not alive");
		}
	}
	
	public List<String> getTokenizedList(String string, String delimeter, boolean makeLowerCase) 
	{
		if (string == null) return null;
		StringTokenizer st = new StringTokenizer(string, delimeter);
		List<String> list = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			if (makeLowerCase) 
				list.add(st.nextToken().trim().toLowerCase());
			else
				list.add(st.nextToken().trim());
		}
		
		return list;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new HotSongsCategoryUpdater();
	}
}
