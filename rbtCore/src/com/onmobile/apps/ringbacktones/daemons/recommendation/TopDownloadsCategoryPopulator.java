/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.recommendation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author vinayasimha.patil
 *
 */
public class TopDownloadsCategoryPopulator extends TimerTask implements DaemonThread
{
	private static Logger logger = Logger.getLogger(TopDownloadsCategoryPopulator.class);

	private RBTDBManager rbtDBManager = null;

	private HashMap<Integer, Integer> categoryNoOfDaysMap;
	private HashMap<Integer, Integer> categoryNoOfSongsMap;

	/**
	 * 
	 */
	public TopDownloadsCategoryPopulator()
	{
		rbtDBManager = RBTDBManager.getInstance();

		Parameters topDownloadsCategoriesParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "TOP_DOWNLOADS_CATEGORIES");
		logger.info("RBT:: topDownloadsCategoriesParam = "+ topDownloadsCategoriesParam);
		if(topDownloadsCategoriesParam != null)
		{
			categoryNoOfDaysMap = new HashMap<Integer, Integer>();
			categoryNoOfSongsMap = new HashMap<Integer, Integer>();
			StringTokenizer stringTokenizer = new StringTokenizer(topDownloadsCategoriesParam.getValue().trim(), ",");

			while (stringTokenizer.hasMoreTokens())
			{
				String token = stringTokenizer.nextToken();
				String[] values = token.split(":");
				int categoryID = Integer.parseInt(values[0]);
				int noOfDays = Integer.parseInt(values[1]);
				int noOfSongsInCategory = Integer.parseInt(values[2]);

				categoryNoOfDaysMap.put(categoryID, noOfDays);
				categoryNoOfSongsMap.put(categoryID, noOfSongsInCategory);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		boolean refreshedCategory = false;

		Set<Integer> keySet = categoryNoOfDaysMap.keySet();
		for (Integer categoryID : keySet)
		{
			try
			{
				Integer noOfDays = categoryNoOfDaysMap.get(categoryID);
				Integer noOfSongsInCategory = categoryNoOfSongsMap.get(categoryID);

				Category category = rbtDBManager.getCategory(categoryID);
				if(category == null)
				{
					logger.info("RBT:: category "+ categoryID +" does not exists");
					continue;
				}

				Integer[] topDownloadedClipIDs = rbtDBManager.getMostAccesses(noOfSongsInCategory, noOfDays);
				if(topDownloadedClipIDs == null || topDownloadedClipIDs.length == 0)
				{
					logger.info("RBT:: Not found any entry in Access Table for last "+ noOfDays +" days");
					continue;
				}

				logger.info("RBT:: Found "+ topDownloadedClipIDs.length + " songs for last "+ noOfDays +" days");

				refreshedCategory = true;

				int[] ClipIDsInCategory = new int[0];
				if(topDownloadedClipIDs.length < noOfSongsInCategory)
					ClipIDsInCategory = rbtDBManager.getClipIDsInCategory(categoryID);

				rbtDBManager.removeCategoryClips(categoryID);

				ArrayList<Integer> topDownloadList = new ArrayList<Integer>();

				int clipIndex = 1;
				for (int i = 0; i < topDownloadedClipIDs.length; i++)
				{
					topDownloadList.add(topDownloadedClipIDs[i]);
					Clips clip = rbtDBManager.getClip(topDownloadedClipIDs[i]);
					rbtDBManager.createCategoryClip(category, clip, "y", clipIndex, null);
					clipIndex++;
				}

				for(int i = 0; clipIndex <= noOfSongsInCategory && i < ClipIDsInCategory.length; i++)
				{
					if(!topDownloadList.contains(ClipIDsInCategory[i]))
					{
						Clips clip = rbtDBManager.getClip(ClipIDsInCategory[i]);
						rbtDBManager.createCategoryClip(category, clip, "y", clipIndex, null);
						clipIndex++;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		if(refreshedCategory)
			rbtDBManager.refreshClipCache();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.recommendation.DaemonThread#start()
	 */
	public void start()
	{
		Parameters topDownCatPopulatorTimeParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "TOP_DOWNLOADS_CATEGORY_POPULATOR_TIME");
		if(topDownCatPopulatorTimeParam != null)
		{
			String topDownCatPopulatorTime = topDownCatPopulatorTimeParam.getValue().trim();
			String[] timeTokens = topDownCatPopulatorTime.split(":");

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTokens[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(timeTokens[1]));
			if(System.currentTimeMillis() >= calendar.getTimeInMillis())
				calendar.add(Calendar.DAY_OF_YEAR, 1);

			Date startDate = calendar.getTime();
			logger.info("RBT:: startDate = "+ startDate);
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(this, startDate, 1000*60*60*24);
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.recommendation.DaemonThread#stop()
	 */
	public void stop()
	{
		this.cancel();		
	}

}
