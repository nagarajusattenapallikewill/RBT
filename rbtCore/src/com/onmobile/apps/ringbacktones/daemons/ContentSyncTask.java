/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author vinayasimha.patil
 *
 */
public class ContentSyncTask implements Runnable
{
	private static Logger logger = Logger.getLogger(ContentSyncTask.class);
	private final String m_class = "ContentSyncTask";

	private boolean runThread;
	private Thread contentSyncTaskThread = null;
	
	private int contentSyncFrequency = 1;
	private RBTDBManager rbtDBManager = null;	

	private String httpLink = null;
	private String operatorAccount = null;
	private String operatorPassword = null;
	private String operator = null;
	private String querySongInfoPage = null;

	private static int fetchSize = 1000;

	private String daemonQueriesLogPath = "";
	private int rotationSize = 8000;
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

	public ContentSyncTask()
	{
		httpLink = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		operatorAccount = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
		operatorPassword = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
		operator = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");
		querySongInfoPage = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SONG_INFO_PAGE", "");

		fetchSize = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "FETCH_SIZE", 1000);
		contentSyncFrequency = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "CONTENT_SYNC_FREQUENCY", 1);

		daemonQueriesLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_DAEMON_LOG_PATH", null);
		rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		rbtDBManager = RBTDBManager.getInstance();
		
		runThread = true;
		contentSyncTaskThread = new Thread(this);
		contentSyncTaskThread.start();
	}
	
	public final void stop()
	{
		runThread = false;
		contentSyncTaskThread.interrupt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while(runThread)
		{
			Parameters contentSyncParameter = CacheManagerUtil.getParametersCacheManager().getParameter("TATA_RBT_DAEMON", "CONTENT_SYNC_DATE");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date contentSyncDate;
			try
			{
				contentSyncDate = dateFormat.parse(contentSyncParameter.getValue());
				if(contentSyncDate.getTime() <= System.currentTimeMillis())
				{
					updateClipsEndDate();
					
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DAY_OF_YEAR, contentSyncFrequency);
					contentSyncDate = calendar.getTime();
					CacheManagerUtil.getParametersCacheManager().updateParameter("TATA_RBT_DAEMON", "CONTENT_SYNC_DATE", dateFormat.format(contentSyncDate));
				}

				Thread.sleep(RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", 0) * 1000 * 60);
			}
			catch (ParseException e)
			{
				logger.error("", e);
			}
			catch (NumberFormatException e)
			{
				logger.error("", e);
			}
			catch (InterruptedException e)
			{
				logger.error("", e);
			}
		}
	}
	public void updateClipsEndDate()
	{
		Clips[] clipsToBeUpdated = getClipsToBeUpdated(0);
		while(clipsToBeUpdated != null && clipsToBeUpdated.length > 0)
		{
			logger.info("RBT:: Updating for clip range :"+ clipsToBeUpdated[0].id() +" to "+ clipsToBeUpdated[clipsToBeUpdated.length - 1].id());
			for (int i = 0; i < clipsToBeUpdated.length; i++)
			{
				updateClip(clipsToBeUpdated[i]);
			}

			clipsToBeUpdated = getClipsToBeUpdated(clipsToBeUpdated[clipsToBeUpdated.length - 1].id());
		}
	}

	private Clips[] getClipsToBeUpdated(int clipStartRange)
	{
		Clips[] clipsToBeUpdated = null;

		clipsToBeUpdated = rbtDBManager.getClipsToBeUpdated(clipStartRange, fetchSize);

		return clipsToBeUpdated;
	}

	private void updateClip(Clips clipToBeUpdated)
	{
		String clipName = clipToBeUpdated.name();
		String[] songCodeNExpiryDate = new String[2];

		String response = getSongCodeNExpiryDate(clipName, songCodeNExpiryDate);

		if(response.equalsIgnoreCase("song_found"))
		{
			if(songCodeNExpiryDate[0].equals(clipToBeUpdated.promoID()))
			{
				logger.info("RBT:: Song found. Promo ID = "+ songCodeNExpiryDate[0]);
				try
				{
					Timestamp timeStamp = Timestamp.valueOf(songCodeNExpiryDate[1]);
					Date expiryDate = new Date(timeStamp.getTime());
					logger.info("RBT:: Expiry Date = "+ expiryDate);
					
					rbtDBManager.updateClipEndDateForTATA(clipToBeUpdated.id(), expiryDate);
				}
				catch(IllegalArgumentException iae)
				{
					logger.info("RBT:: Expiry Date format is wrong: "+ songCodeNExpiryDate[1]);
					logger.error("", iae);
				}
			}
			else
			{
				logger.info("RBT:: Promo IDs are differnt. Our End = "+ clipToBeUpdated.promoID() +" Back End = "+ songCodeNExpiryDate[0]);
				rbtDBManager.updateClipEndDateForTATA(clipToBeUpdated.id(), clipToBeUpdated.endTime());
			}
		}
		else if(response.equalsIgnoreCase("song_not_found"))
		{
			logger.info("RBT:: Song not found");
			rbtDBManager.updateClipEndDateForTATA(clipToBeUpdated.id(), new Date());
		}
	}

	private String getSongCodeNExpiryDate(String clipName, String[] songCodeNExpiryDate)
	{
		String response = "general_error";
		
		String encodedClipName = Tools.findNReplaceAll(clipName, " ", "%20");
		logger.info("RBT:: encodedClipName: "+ encodedClipName);

		String urlstr = httpLink;
		urlstr += querySongInfoPage;

		urlstr += operatorAccount+"&";
		urlstr += operatorPassword+"&";
		urlstr += "songname="+encodedClipName+"&";
		urlstr += operator;

		RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();			
		String result = null;

		Date requestedTimeStamp = new Date();
		try
		{
			result = rbthttpProcessing.makeRequest1(urlstr, null, null);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		Date responseTimeStamp = new Date();
		long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

		String requestedTimeString = formatter.format(requestedTimeStamp);

		if(result == null)
		{
			WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONG_INFO", "NA", "NA", "query_song_info", "null_error_reponse", requestedTimeString, differenceTime+"", m_class, urlstr, result);
			return "null";
		}

		result = result.trim();

		if(result.length() <= 1)
		{
			if(result.equals("3"))
			{
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONG_INFO", "NA", "NA", "query_song_info", "song_not_found", requestedTimeString, differenceTime+"", m_class, urlstr, result);
				response = "song_not_found";
			}
			else
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONG_INFO", "NA", "NA", "query_song_info", "error_got-null", requestedTimeString, differenceTime+"", m_class, urlstr, result);
		}
		else
		{
			WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONG_INFO", "NA", "NA", "query_song_info", "song_found", requestedTimeString, differenceTime+"", m_class, urlstr, result);
			StringTokenizer sonInfoTokens = new StringTokenizer(result, "|", true);
			int sonInfoTokensCount = 1; 
			while(sonInfoTokens.hasMoreTokens())
			{
				String token = sonInfoTokens.nextToken();

				if(token.equalsIgnoreCase("|"))
				{
					sonInfoTokensCount++;
					continue;
				}

				if(sonInfoTokensCount == 2)
					songCodeNExpiryDate[0] = token;
				else if(sonInfoTokensCount == 12)
				{
					songCodeNExpiryDate[1] = token;
					response = "song_found";
					break;
				}
			}
		}
		
		return response;
	}
}
