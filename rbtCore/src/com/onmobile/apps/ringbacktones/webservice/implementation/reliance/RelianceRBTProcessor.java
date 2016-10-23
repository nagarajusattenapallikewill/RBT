package com.onmobile.apps.ringbacktones.webservice.implementation.reliance;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Status;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class RelianceRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(RelianceRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processActivation(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processActivation(WebServiceContext webServiceContext)
	{
		if (webServiceContext.getString(param_api).equals(api_Subscription))
		{
			logger.info("response: " + FAILED);
			return FAILED;
		}

		return super.processActivation(webServiceContext);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.RBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public String processSelection(WebServiceContext webServiceContext)
	{
		String response = ERROR;

		try
		{
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			String validityResponse = isValidUser(webServiceContext, subscriber);
			if (!validityResponse.equalsIgnoreCase(VALID) && !validityResponse.equalsIgnoreCase(SUSPENDED))
			{
				logger.info("Returning response after subscriber validation : " + validityResponse);
				return validityResponse;
			}

			String subscriberStatus = Utility.getSubscriberStatus(subscriber);
			if (subscriberStatus.equals(ACT_ERROR)
					|| subscriberStatus.equals(ACT_PENDING)
					|| subscriberStatus.equals(LOCKED)
					|| subscriberStatus.equals(DEACT_ERROR)
					|| subscriberStatus.equals(DEACT_PENDING))
			{
				logger.info("Returning response after subscriber status check : " + subscriberStatus);
				response = subscriberStatus;
			}
			else if (!subscriberStatus.equals(NEW_USER) && !subscriberStatus.equals(DEACTIVE))
			{
				response = super.processSelection(webServiceContext);
			}
			else
			{		
				String subscriberID = webServiceContext.getString(param_subscriberID);

				String callerID = (!webServiceContext.containsKey(param_callerID) || 
						webServiceContext.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
								: webServiceContext.getString(param_callerID);

				if (callerID != null && !callerID.startsWith("G"))
				{
					// callerID null means for ALL callers and if starts with 'G' means groupID.

					Parameters parameter = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MINIMUM_CALLER_ID_LENGTH", "7");
					int minCallerIDLength = Integer.parseInt(parameter.getValue()); 

					boolean validCallerID = false;
					if (callerID.length() >= minCallerIDLength)
					{
						try
						{
							Long.parseLong(callerID);
							validCallerID = true;
						}
						catch (NumberFormatException e)
						{
						}
					}

					if (!validCallerID)
					{
						logger.info("Invalid callerID. Returning response: " + INVALID_PARAMETER);
						return INVALID_PARAMETER;
					}
				}

				@SuppressWarnings("unused")
				int status = 1;
				int fromHrs = 0;
				int toHrs = 23;
				int fromMinutes = 0;
				int toMinutes = 59;

				if (webServiceContext.containsKey(param_fromTime))
					fromHrs = Integer.parseInt(webServiceContext.getString(param_fromTime));
				if (webServiceContext.containsKey(param_toTime))
					toHrs = Integer.parseInt(webServiceContext.getString(param_toTime));
				if (webServiceContext.containsKey(param_toTimeMinutes))
					toMinutes = Integer.parseInt(webServiceContext.getString(param_toTimeMinutes));
				if (webServiceContext.containsKey(param_fromTimeMinutes))
					fromMinutes = Integer.parseInt(webServiceContext.getString(param_fromTimeMinutes));

				if (fromHrs < 0 || fromHrs > 23 || toHrs < 0 || toHrs > 23
						|| fromMinutes < 0 || fromMinutes > 59 || toMinutes < 0
						|| toMinutes > 59)
				{
					logger.info("Invalid fromTime or toTime. Returning response: " + INVALID_PARAMETER);
					return INVALID_PARAMETER;
				}

				DecimalFormat decimalFormat = new DecimalFormat("00");
				@SuppressWarnings("unused")
				int fromTime = Integer.parseInt(fromHrs + decimalFormat.format(fromMinutes));
				@SuppressWarnings("unused")
				int toTime = Integer.parseInt(toHrs + decimalFormat.format(toMinutes));

				String interval = webServiceContext.getString(param_interval);
				if (interval != null)
					interval = interval.toUpperCase();
				if (!Utility.isValidSelectionInterval(interval))
				{
					logger.info("Invalid interval. Returning response: " + INVALID_PARAMETER);
					return INVALID_PARAMETER;
				}

				Calendar endCal = Calendar.getInstance();
				endCal.set(2037, 0, 1);
				Date endDate = endCal.getTime();
				Date startDate = null;

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				if (webServiceContext.containsKey(param_selectionStartTime))
				{
					String startTimeStr = webServiceContext.getString(param_selectionStartTime);
					if (startTimeStr.length() != 8 && startTimeStr.length() != 17)
					{
						logger.info("Invalid selectionStartTime. Returning response: " + INVALID_PARAMETER);
						return INVALID_PARAMETER;
					}

					if (startTimeStr.length() == 8)
						startTimeStr += "000000000";

					startDate = dateFormat.parse(startTimeStr);
				}

				if (webServiceContext.containsKey(param_selectionEndTime))
				{
					String endTimeStr = webServiceContext.getString(param_selectionEndTime);
					if (endTimeStr.length() != 8 && endTimeStr.length() != 17)
					{
						logger.info("Invalid selectionEndTime. Returning response: " + INVALID_PARAMETER);
						return INVALID_PARAMETER;
					}

					if (endTimeStr.length() == 8)
						endTimeStr += "235959000";
					else if (endTimeStr.endsWith("000000000"))
						endTimeStr.replaceAll("000000000", "235959000");

					endDate = dateFormat.parse(endTimeStr);
				}

				if (webServiceContext.containsKey(param_selectionStartTime) && webServiceContext.containsKey(param_selectionEndTime))
				{
					if (startDate != null && startDate.compareTo(endDate) >= 0)
					{
						logger.info("selectionStartTime is not less than selectionEndTime. Returning response: " + INVALID_PARAMETER);
						return INVALID_PARAMETER;
					}

					// If selectionStartTime & selectionEndTime passed, then selection interval will be ignored.
					interval = null;
				}

				if (!webServiceContext.containsKey(param_categoryID)
						&& !webServiceContext.containsKey(param_categoryPromoID)
						&& !webServiceContext.containsKey(param_categorySmsAlias))
				{
					logger.info("categoryID parameter not passed. Returning response: " + INVALID_PARAMETER);
					return INVALID_PARAMETER;
				}

				String browsingLanguage = webServiceContext.getString(param_browsingLanguage);
				Category category = null;
				if (webServiceContext.containsKey(param_categoryID))
					category = rbtCacheManager.getCategory(Integer.parseInt(webServiceContext.getString(param_categoryID)), browsingLanguage);
				else if (webServiceContext.containsKey(param_categoryPromoID))
					category = rbtCacheManager.getCategoryByPromoId(webServiceContext.getString(param_categoryPromoID), browsingLanguage);
				else if (webServiceContext.containsKey(param_categorySmsAlias))
					category = RBTCacheManager.getInstance().getCategoryBySMSAlias(webServiceContext.getString(param_categorySmsAlias));

				if (category == null)
				{
					logger.info("response: " + CATEGORY_NOT_EXISTS);
					return CATEGORY_NOT_EXISTS;
				}

				webServiceContext.put(param_requestFromSelection,"true");

				String classType = null;

				String selectedBy = getMode(webServiceContext);
				String selectionInfo = getModeInfo(webServiceContext);

				if (webServiceContext.containsKey(param_cricketPack))
				{
					String cricketPack = webServiceContext.getString(param_cricketPack);
					if (!cricketPack.equalsIgnoreCase("DEFAULT"))
					{
						FeedSchedule schedule = rbtDBManager.getFeedSchedule("CRICKET", cricketPack);
						if (schedule == null)
						{
							Parameters cricketIntervalParm = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CRICKET_INTERVAL", "2");
							int cricketInterval = Integer.parseInt(cricketIntervalParm.getValue().trim());

							FeedSchedule[] schedules = rbtDBManager.getFeedSchedules("CRICKET", cricketPack, cricketInterval);
							if (schedules != null && schedules.length > 0)
								schedule = schedules[0];
						}

						if (schedule == null)
						{
							logger.info("response: " + CRICKET_PACK_NOT_EXISTS);
							return CRICKET_PACK_NOT_EXISTS;
						}

						startDate = schedule.startTime();
						endDate = schedule.endTime();
						classType = schedule.classType();
					}

					status = 90;
				}
				else if (webServiceContext.containsKey(param_profileHours))
				{
					String profileHours = webServiceContext.getString(param_profileHours);
					int hours;
					if (profileHours.startsWith("D"))
					{
						hours = Integer.parseInt(profileHours.substring(1));
						hours *= 24;
					}
					else
						hours = Integer.parseInt(profileHours);

					endCal = Calendar.getInstance();
					endCal.add(Calendar.MINUTE, hours * 60);

					if (endCal.getTime().before(endDate))
					{
						// Making sure that endDate will not exceed 2037-01-01
						endDate = endCal.getTime();
					}
					status = 99;
				}

				if (category.getCategoryTpe() == iRBTConstant.BOX_OFFICE_SHUFFLE)
				{
					endDate = category.getCategoryEndTime();
					if (endDate.before(new Date()))
						return CATEGORY_EXPIRED;

					status = 92;
				}
				else if (category.getCategoryTpe() == iRBTConstant.FESTIVAL_SHUFFLE)
				{
					endDate = category.getCategoryEndTime();
					if (endDate.before(new Date()))
						return CATEGORY_EXPIRED;

					status = 93;
				}
				if (webServiceContext.containsKey(param_status))
					status = Integer.parseInt(webServiceContext.getString(param_status));


				Clip clip = null;
				HashMap<String, Object> clipMap = new HashMap<String, Object>();
				if (Utility.isShuffleCategory(category.getCategoryTpe()))
				{
					Clip[] clips = rbtCacheManager.getActiveClipsInCategory(category.getCategoryId(), browsingLanguage);
					clip = clips[0];
				}
				else if (!webServiceContext.containsKey(param_cricketPack))
				{
					clip = getClip(webServiceContext);
					if (webServiceContext.containsKey(param_profileHours)
							|| category.getCategoryTpe() == iRBTConstant.RECORD
							|| category.getCategoryTpe() == iRBTConstant.KARAOKE)
					{
						String rbtFile = webServiceContext.getString(param_clipID);
						if (rbtFile.toLowerCase().endsWith(".wav"))
							rbtFile = rbtFile.substring(0, rbtFile.length() - 4);

						clipMap.put("CLIP_WAV", rbtFile);
					}
				}

				if (clip != null)
				{
					if (clip.getClipEndTime().before(new Date()))
					{
						logger.info("response: " + CLIP_EXPIRED);
						return CLIP_EXPIRED;
					}
					
					webServiceContext.put(session_clip, clip);

					clipMap.put("CLIP_CLASS", clip.getClassType());
					clipMap.put("CLIP_END", clip.getClipEndTime());
					clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
					clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
					clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
					clipMap.put("CLIP_NAME", clip.getClipName());
				}
				else
				{
					logger.info("response: " + CLIP_NOT_EXISTS);
					return CLIP_NOT_EXISTS;
				}
				boolean isOverlap = Utility.isSelectionOverlap(clip, category, subscriber);
				if(isOverlap)
				{
					logger.info("response: " + ALREADY_EXISTS);
					return ALREADY_EXISTS;
				}

				if (classType == null)
					classType = getChargeClass(webServiceContext, subscriber, category, clip);

				HashMap<String, String> prMap = new HashMap<String, String>();
				prMap.put(ExtraInfoKey.CATEGORY_ID.toString(), String.valueOf(category.getCategoryId()));
				prMap.put(ExtraInfoKey.CLIP_ID.toString(), clip.getClipId()+"");

				if(callerID != null)
					prMap.put(ExtraInfoKey.CALLER_ID.toString(), callerID);
				String prExtraInfo = DBUtility.getAttributeXMLFromMap(prMap);

				ProvisioningRequests pr = new ProvisioningRequests(classType, Calendar.getInstance().getTime(), prExtraInfo, selectedBy,
						selectionInfo, null,0, null, Status.TOBE_PROCESSED.getStatusCode(), subscriberID, "0", Type.SELECTION.getTypeCode());
				
				pr = ProvisioningRequestsDao.createProvisioningRequest(pr);
				response = (pr !=null) ? SUCCESS : FAILED;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
}
