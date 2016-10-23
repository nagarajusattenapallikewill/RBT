/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.contest;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author vinayasimha.patil
 * 
 */
public class ContestUtils
{
	private static Logger logger = Logger.getLogger(ContestUtils.class);
	private static RBTHttpClient rbtHttpClient = null;

	public static String getContestEndTime()
	{
		int contestPeriod = RBTParametersUtils.getParamAsInt(
				iRBTConstant.CONTEST, "CONTEST_PRERIOD_IN_HOURS", -1);
		if (contestPeriod > 0)
		{
			Calendar calendar = Calendar.getInstance();
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int contestEndTime = currentHour + contestPeriod;

			int contestPeriodThreshold = RBTParametersUtils.getParamAsInt(
					iRBTConstant.CONTEST,
					"CONTEST_PRERIOD_THRESHOLD_IN_MINUTES", 0);

			int currentMinutes = calendar.get(Calendar.MINUTE);
			int timeLeftForContest = (contestPeriod * 60) - currentMinutes;
			if (timeLeftForContest < contestPeriodThreshold)
				contestEndTime++;

			String ampm = ((contestEndTime / 12) & 1) == 1 ? "PM" : "AM";
			contestEndTime = contestEndTime % 12;

			if (contestEndTime == 0)
				contestEndTime = 12;

			String contestEndTimeStr = contestEndTime + ampm;

			return contestEndTimeStr;
		}

		return null;
	}

	/**
	 * @param subscriberID
	 * @param clip
	 * 
	 * Based on the content's clipInfo, hits the contest url with the subscriberID 
	 */
	public static void hitContestUrlForSpecificContent(String subscriberID, Clip clip)
	{
		try
		{
			String contestID = getContestIDForClip(clip);
			if (contestID == null || contestID.isEmpty())
				return;

			String contestUrl = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CONTEST_URL_FOR_SPECIFIC_CONTENT", null);
			if (contestUrl == null)
			{
				logger.warn("Contest url not configured, so not hitting the contest for subscriber : " + subscriberID);
				return;
			}

			if (rbtHttpClient == null)
			{
				initializeHttpClient();
			}

			contestUrl = contestUrl.replaceAll("%msisdn", subscriberID);
			contestUrl = contestUrl.replaceAll("%contestid", contestID);
			contestUrl = contestUrl.replaceAll("%clipid", String.valueOf(clip.getClipId()));

			if (clip.getClipPromoId() != null)
				contestUrl = contestUrl.replaceAll("%promoid", clip.getClipPromoId());

			logger.info("Contest url : " + contestUrl);
			HttpResponse httpResponse = rbtHttpClient.makeRequestByGet(contestUrl, null);
			if (logger.isInfoEnabled())
				logger.info("Response from the contest url, httpResponse : " + httpResponse);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private static void initializeHttpClient()
	{
		HttpParameters httpParameters = new HttpParameters();
		String contestUrlHttpConfig = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CONTEST_URL_HTTP_CONFIG", null);
		if (contestUrlHttpConfig != null)
		{
			String[] tokens = contestUrlHttpConfig.split(",");
			if (tokens.length > 0)
				httpParameters.setConnectionTimeout(Integer.parseInt(tokens[0]));
			if (tokens.length > 1)
				httpParameters.setSoTimeout(Integer.parseInt(tokens[1]));
			if (tokens.length > 2)
				httpParameters.setProxyHost(tokens[2]);
			if (tokens.length > 3)
				httpParameters.setProxyPort(Integer.parseInt(tokens[3]));
		}
		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	/**
	 * @param clip
	 * @return
	 */
	private static String getContestIDForClip(Clip clip)
	{
		if (clip == null || clip.getClipInfo() == null)
		{
			logger.info("Clip or clipInfo is null, so not getting the contestID");
			return null;
		}

		String clipInfo = clip.getClipInfo();
		int contestIdIndex = clipInfo.indexOf("CONTEST_ID=");
		if (contestIdIndex == -1)
		{
			logger.info("CONTEST_ID not configured for the clipID : "
					+ clip.getClipId());
			return null;
		}

		String subStr = clipInfo.substring(contestIdIndex + 11);
		String contestID = null;
		try
		{
			int index = subStr.indexOf("|");
			if (index != -1)
				contestID = subStr.substring(0, index);
			else
				contestID = subStr;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return contestID.trim();
	}
}
