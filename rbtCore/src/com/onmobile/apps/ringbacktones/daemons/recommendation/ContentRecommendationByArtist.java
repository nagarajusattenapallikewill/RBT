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

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
/**
 * @author vinayasimha.patil
 *
 */
public class ContentRecommendationByArtist extends TimerTask implements DaemonThread
{
	private static Logger logger = Logger.getLogger(ContentRecommendationByArtist.class);

	private RBTDBManager rbtDBManager = null;
	protected static ParametersCacheManager parametersCacheManager = null;
	
	/**
	 * 
	 */
	public ContentRecommendationByArtist()
	{
		rbtDBManager = RBTDBManager.getInstance();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		logger.info("RBT:: Artist recommendation started");
		Date currentDate = new Date();
		Date lastRecommendationDate = getLastRecommendationDate();
		
		HashMap<String, ArrayList<String>> artistClipNameMap = rbtDBManager.getClipMapForArtistByStartTime(lastRecommendationDate, currentDate);

		if(artistClipNameMap != null && artistClipNameMap.size() > 0)
		{
			int recommendationClipCount = 1;
			Parameters recClipCountParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_CLIP_COUNT", "1");
			if(recClipCountParam != null)
				recommendationClipCount = Integer.parseInt(recClipCountParam.getValue().trim());
			
			String recommendationSMS = null;
			Parameters recSMSParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "ARTIST_RECOMMENDATION_SMS", null);
			if(recSMSParam != null)
				recommendationSMS = recSMSParam.getValue().trim();
			
			HashMap<String, String> smsTextMap = new HashMap<String, String>();
			Set<String> artists = artistClipNameMap.keySet();
			for (String artist : artists)
			{
				ArrayList<String> clipList = artistClipNameMap.get(artists);
				String newClips = "";
				if (clipList != null)
				{
					for (int i = 0; i < clipList.size() && i < recommendationClipCount; i++)
					{
						newClips += clipList.get(i) + ", ";
					}
				}
				if (!newClips.equals(""))
					newClips.substring(0, newClips.lastIndexOf(","));

				String tmpRecSMS = recommendationSMS;
				if (tmpRecSMS != null)
				{
					tmpRecSMS = tmpRecSMS.replaceAll("%CLIPS%", newClips);
					tmpRecSMS = tmpRecSMS.replaceAll("%ARTIST%", artist);
				}
				smsTextMap.put(artist, tmpRecSMS);
			}

			logger.info("RBT:: smsTextMap: "+ smsTextMap);
			if(smsTextMap != null && smsTextMap.size() >= 0)
			{
				HashMap<String, ArrayList<String>> subscriberMap = null;
				
				String recommendationMode = "DOWNLOADS";
				Parameters recommendationModeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_MODE", "DOWNLOADS");
				if(recommendationModeParam != null)
					recommendationMode = recommendationModeParam.getValue().trim();
				
				int fetchDays = 90;
				Parameters fetchDaysParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_FETCH_DAYS", "90");
				if(fetchDaysParam != null)
					fetchDays = Integer.parseInt(fetchDaysParam.getValue().trim());
				
				int maxRecSMSPerSubscriber = 1;
				Parameters maxRecSMSPerSubscriberParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "MAX_RECOMMENDATION_PER_SUBSCRIBER", "1");
				if(maxRecSMSPerSubscriberParam != null)
					maxRecSMSPerSubscriber = Integer.parseInt(maxRecSMSPerSubscriberParam.getValue().trim());
				
				if(recommendationMode.equalsIgnoreCase("SELECTIONS"))
					subscriberMap = rbtDBManager.getRecommendationByArtistsFromSelections(artistClipNameMap, fetchDays, maxRecSMSPerSubscriber);
				else
					subscriberMap = rbtDBManager.getRecommendationByArtistsFromDownloads(artistClipNameMap, fetchDays, maxRecSMSPerSubscriber);

				logger.info("RBT:: subscriberMap: " + subscriberMap);

				if(subscriberMap != null && subscriberMap.size() > 0)
				{
					String smsNo = null;
					Parameters smsNoParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_SMS_NO", null);
					if(smsNoParam != null)
						smsNo = smsNoParam.getValue().trim();
					
					boolean checkNewsLetterForRecommendation = false;
					Parameters checkNewsLetterForRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "CHECK_NEWSLETTER_FOR_RECOMMENDATION", "FALSE");
					if(checkNewsLetterForRecommendationParam != null)
						checkNewsLetterForRecommendation = checkNewsLetterForRecommendationParam.getValue().trim().equalsIgnoreCase("TRUE");
					
					artists = smsTextMap.keySet();
					for(Iterator<String> iterator = artists.iterator(); iterator.hasNext();)
					{
						String artist = iterator.next();
						String recSMS = smsTextMap.get(artist);

						ArrayList<String> subscriberList = subscriberMap.get(artist);
						if(subscriberList != null && subscriberList.size() > 0)
						{
							try
							{
								String fileName = artist;
								File listFile = Utility.getInstance(parametersCacheManager).createListFile(fileName, subscriberList, rbtDBManager, checkNewsLetterForRecommendation);
								logger.info("RBT:: listFile for artist recommendation = " + listFile.getAbsolutePath());
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
					logger.info("RBT:: subscriberMap for artist recommendation= null");
			}
			else
				logger.info("RBT:: No content release found for the period " + lastRecommendationDate + " and " + currentDate + ".");
		}

		updateLastRecommendationDate(currentDate);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.recommendation.DaemonThread#start()
	 */
	public void start()
	{
		Parameters artistRecTimeParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "ARTIST_RECOMMENDATION_TIME", null);
		if(artistRecTimeParam != null)
		{
			String artistRecTime = artistRecTimeParam.getValue().trim();
			String[] tokens = artistRecTime.split(",");
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
			Parameters lastRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "LAST_ARTIST_RECOMMENDATION_DATE", null);
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

		Parameters lastRecommendationParam = parametersCacheManager.getParameter(iRBTConstant.DAEMON, "LAST_ARTIST_RECOMMENDATION_DATE", null);
		if(lastRecommendationParam != null)
			parametersCacheManager.updateParameter(iRBTConstant.DAEMON, "LAST_ARTIST_RECOMMENDATION_DATE", dateFormat.format(date));
		else
			parametersCacheManager.addParameter(iRBTConstant.DAEMON, "LAST_ARTIST_RECOMMENDATION_DATE", dateFormat.format(date));
	}
}
