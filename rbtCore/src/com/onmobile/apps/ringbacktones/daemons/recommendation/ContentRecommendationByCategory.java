/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.recommendation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;

/**
 * @author vinayasimha.patil
 *
 */
public class ContentRecommendationByCategory extends TimerTask implements DaemonThread
{
	private static Logger logger = Logger.getLogger(ContentRecommendationByCategory.class);

	private RBTDBManager rbtDBManager = null;
	protected static RBTCacheManager rbtCacheManager = null;
	protected static ParametersCacheManager parametersCacheManager = null;
	
	/**
	 * 
	 */
	public ContentRecommendationByCategory()
	{
		rbtDBManager = RBTDBManager.getInstance();
		
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();

	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		try {
			logger.info("RBT:: Category recommendation started");
			Date currentDate = new Date();
			Date lastRecommendationDate = getLastRecommendationDate();
			
			HashMap<String, ArrayList<String>> updatedCategories = rbtDBManager.getClipMapByStartTime(lastRecommendationDate, currentDate);
			if(updatedCategories != null && updatedCategories.size() > 0)
			{
				String[] nonRecommendationCats = null;
				Parameters nonRecCatsParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "NON_RECOMMENDATION_CATEGORIES", null);
				if(nonRecCatsParam != null)
					nonRecommendationCats = nonRecCatsParam.getValue().trim().split(",");
				
				logger.info("RBT:: updated  categories not null" );
				for(int i = 0; nonRecommendationCats != null && i < nonRecommendationCats.length; i++)
				{
					logger.info("RBT:: nonRecommendationCats = " + nonRecommendationCats[i]);
					updatedCategories.remove(nonRecommendationCats[i]);
				}
				
				int recommendationClipCount = 1;
				Parameters recClipCountParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_CLIP_COUNT", "1");
				if(recClipCountParam != null)
					recommendationClipCount = Integer.parseInt(recClipCountParam.getValue().trim());
	
				String recommendationSMS = null;
				Parameters recSMSParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "CATEGORY_RECOMMENDATION_SMS", null);
				if(recSMSParam != null)
					recommendationSMS = recSMSParam.getValue().trim();
	
				HashMap<String, String> smsTextMap = new HashMap<String, String>();
				HashMap<String, String> categoryMap = new HashMap<String, String>();
				Set<String> categories = updatedCategories.keySet();
				String[] categoryIDs = null;
				for (String categoryID : categories)
				{
					Category category = rbtDBManager.getCategory(Integer.parseInt(categoryID));
					if(category == null)
						continue;
	
					ArrayList<String> clipList = updatedCategories.get(categoryID);
	
					String newClips = "";
					for (int i = 0; i < clipList.size() && i < recommendationClipCount; i++)
					{
						newClips += clipList.get(i) + ", ";
					}
					newClips.substring(0, newClips.lastIndexOf(","));
	
					String tmpRecSMS = recommendationSMS;
					if (tmpRecSMS != null)
					{
						tmpRecSMS = tmpRecSMS.replaceAll("%CLIPS%", newClips);
						tmpRecSMS = tmpRecSMS.replaceAll("%CATEGORY%", category.getName());
					}
					smsTextMap.put(categoryID, tmpRecSMS);
					categoryMap.put(categoryID, category.getName());
				}
	
				if(smsTextMap != null && smsTextMap.size() >= 0)
				{
					int maxRecSMSPerSubscriber = 1;
					Parameters maxRecSMSPerSubscriberParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "MAX_RECOMMENDATION_PER_SUBSCRIBER", "1");
					if(maxRecSMSPerSubscriberParam != null)
						maxRecSMSPerSubscriber = Integer.parseInt(maxRecSMSPerSubscriberParam.getValue().trim());
					
					int fetchDays = 90;
					Parameters fetchDaysParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_FETCH_DAYS", "90");
					if(fetchDaysParam != null)
						fetchDays = Integer.parseInt(fetchDaysParam.getValue().trim());
					
					int recDownloadCount = 1;
					Parameters recDownloadCountParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "CATEGORY_RECOMMENDATION_DOWNLOAD_COUNT", "1");
					if(recDownloadCountParam != null)
						recDownloadCount = Integer.parseInt(recDownloadCountParam.getValue().trim());
	
					String recommendationMode = "DOWNLOADS";
					Parameters recommendationModeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_MODE", "DOWNLOADS");
					if(recommendationModeParam != null)
						recommendationMode = recommendationModeParam.getValue();
	
					categories = null;
					categories = smsTextMap.keySet();
					categoryIDs = categories.toArray(new String[0]);
					HashMap<String, ArrayList<String>> subscriberMap = null;
					if(categoryIDs != null && categoryIDs.length > 0)
					{
						if(recommendationMode.equalsIgnoreCase("SELECTIONS"))
							subscriberMap = rbtDBManager.getRecommendationByCategoryFromSelections(categoryIDs, recDownloadCount, fetchDays, maxRecSMSPerSubscriber);
						else
							subscriberMap = rbtDBManager.getRecommendationByCategoryFromDownloads(categoryIDs, recDownloadCount, fetchDays, maxRecSMSPerSubscriber);
					}
	
					if(subscriberMap != null && subscriberMap.size() > 0)
					{
						boolean checkNewsLetterForRecommendation = false;
						Parameters checkNewsLetterForRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "CHECK_NEWSLETTER_FOR_RECOMMENDATION", "FALSE");
						if(checkNewsLetterForRecommendationParam != null)
							checkNewsLetterForRecommendation = checkNewsLetterForRecommendationParam.getValue().trim().equalsIgnoreCase("TRUE");
						
						String smsNo = null;
						Parameters smsNoParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_SMS_NO", null);
						if(smsNoParam != null)
							smsNo = smsNoParam.getValue().trim();
	
						categories = smsTextMap.keySet();
						for(Iterator<String> iterator = categories.iterator(); iterator.hasNext();)
						{
							String categoryID = iterator.next();
							String categoryName = categoryMap.get(categoryID);
							String recSMS = smsTextMap.get(categoryID);
	
							ArrayList<String> subscriberList = subscriberMap.get(categoryID);
							if(subscriberList != null && subscriberList.size() > 0)
							{
								try
								{
									String fileName = categoryID + "_" + categoryName;
									File listFile = Utility.getInstance(parametersCacheManager).createListFile(fileName, subscriberList, rbtDBManager, checkNewsLetterForRecommendation);
									logger.info("RBT:: listFile for category recommendation = " + listFile.getAbsolutePath());
									if(listFile != null)
										Utility.getInstance(parametersCacheManager).sendPromoRecommendationSMS(listFile.getAbsolutePath(), recSMS, smsNo);
								}
								catch (Throwable e)
								{
									logger.error("", e);
								}
							}
	
						}
					}
					else
						logger.info("RBT:: subscriberMap = null for category recommendation");
				}
				else
					logger.info("RBT:: No content release found for the period " + lastRecommendationDate + " and " + currentDate + ".");
			}
			else
				logger.info("RBT:: No content release found for the period " + lastRecommendationDate + " and " + currentDate + ".");
	
			updateLastRecommendationDate(currentDate);
		}
		catch(Throwable t) {
			logger.error("Error executing Category recommendation. " + t.getMessage(), t);
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.recommendation.DaemonThread#start()
	 */
	public void start()
	{
		Parameters categoryRecTimeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "CATEGORY_RECOMMENDATION_TIME", null);
		if(categoryRecTimeParam != null)
		{
			String categoryRecTime = categoryRecTimeParam.getValue().trim();
			String[] tokens = categoryRecTime.split(",");
			String[] timeTokens = tokens[1].split(":");

			Calendar calendar = Calendar.getInstance();
			int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
			int recDay = Integer.parseInt(tokens[0]);
			calendar.add(Calendar.DAY_OF_YEAR, (recDay - currentDay));
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTokens[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(timeTokens[1]));
			if(System.currentTimeMillis() >= calendar.getTimeInMillis())
				calendar.add(Calendar.DAY_OF_YEAR, 7);

			Date startDate = calendar.getTime();
			logger.info("RBT:: startDate = "+ startDate);
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(this, startDate, 1000*60*60*24*7);
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.recommendation.DaemonThread#stop()
	 */
	public void stop()
	{
		this.cancel();
	}

	private Date getLastRecommendationDate()
	{
		Date lastRecommendationDate = null;
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Parameters lastRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "LAST_CATEGORY_RECOMMENDATION_DATE", null);
			lastRecommendationDate = dateFormat.parse(lastRecommendationParam.getValue());
		}
		catch(Exception e)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -7);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			lastRecommendationDate = calendar.getTime();
		}

		return lastRecommendationDate;
	}

	private void updateLastRecommendationDate(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Parameters lastRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "LAST_CATEGORY_RECOMMENDATION_DATE", null);
		logger.info("lastRecommendationDate " + lastRecommendationParam);

		if(lastRecommendationParam != null)
			parametersCacheManager.updateParameter(iRBTConstant.DAEMON, "LAST_CATEGORY_RECOMMENDATION_DATE", dateFormat.format(date));
		else
			parametersCacheManager.addParameter(iRBTConstant.DAEMON, "LAST_CATEGORY_RECOMMENDATION_DATE", dateFormat.format(date));

	}
}
