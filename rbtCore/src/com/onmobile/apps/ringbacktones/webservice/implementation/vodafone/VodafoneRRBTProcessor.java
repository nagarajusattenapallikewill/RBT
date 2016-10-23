/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodafone;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.filters.RbtFilterParser;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class VodafoneRRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(VodafoneRRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#processSelection(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public String processSelection(WebServiceContext webServiceContext)
	{
		String response = ERROR;

		try
		{
			String subscriberID = webServiceContext.getString(param_subscriberID);
			
			String callerID = (!webServiceContext.containsKey(param_callerID) || 
					webServiceContext.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
							: webServiceContext.getString(param_callerID);
			if (callerID != null)
			{
				logger.info("Personal Selections not allowed. Returning response: " + ERROR);
				writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, getClip(webServiceContext), getCriteria(webServiceContext));
				return ERROR;
			}

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
				writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
				return INVALID_PARAMETER;
			}

			DecimalFormat decimalFormat = new DecimalFormat("00");
			int fromTime = Integer.parseInt(fromHrs + decimalFormat.format(fromMinutes));
			int toTime = Integer.parseInt(toHrs + decimalFormat.format(toMinutes));

			String interval = webServiceContext.getString(param_interval);
			if (interval != null)
				interval = interval.toUpperCase();
			if (!Utility.isValidSelectionInterval(interval))
			{
				logger.info("Invalid interval. Returning response: " + INVALID_PARAMETER);
				writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
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
					writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
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
					writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
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
					writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
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
				writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
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

			Clip clip = getClip(webServiceContext);
			String contentNotExists = DataUtils.isContentExists(webServiceContext, category, clip);
			if (contentNotExists != null)
			{
				logger.info("response: " + contentNotExists);
				writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, clip, getCriteria(webServiceContext));
				return contentNotExists;
			}

			String contentExpired = DataUtils.isContentExpired(category, clip);
			boolean activateEvenContentExpired = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "ACTIVATE_EVEN_CONTENT_EXPIRED", "TRUE");
			if (!activateEvenContentExpired && contentExpired != null)
			{
				logger.info("response: " + contentExpired);
				writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, clip, getCriteria(webServiceContext));
				return contentExpired;	
			}

			webServiceContext.put(param_requestFromSelection,"true");
			response = processActivation(webServiceContext);

			if (!response.equalsIgnoreCase(SUCCESS) && !Utility.isUserActive(response))
				return response;
			else if (Utility.isUserActive(response)
					&& webServiceContext.containsKey(param_ignoreActiveUser)
					&& webServiceContext.getString(param_ignoreActiveUser).equalsIgnoreCase(YES))
				return response;

			// preprocesses the request to check if any content is blocked for any particular requests
			String filterResponse = RbtFilterParser.getRbtFilter().filterSelection(webServiceContext);
			if (filterResponse != null)
				return filterResponse;

			// Subscriber object is stored in webServiceContext by processActivation method.
			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			CosDetails cos = null;
			if (null!=subscriber && subscriber.cosID() != null){
				cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
			}
			SubscriberStatus sameContentTypeSelection = null;
			if (!webServiceContext.containsKey(param_activatedNow)
					&& !webServiceContext.containsKey(param_cricketPack))
			{
				SubscriberStatus[] selections = rbtDBManager.getAllActiveSubscriberSettings(subscriberID);
				if (selections != null)
				{
					for (SubscriberStatus selection : selections)
					{
						if (category != null && Utility.isShuffleCategory(category.getCategoryTpe()))
						{
							if (category.getCategoryId() == selection.categoryID())
							{
								logger.info("response: " + ALREADY_EXISTS);
								writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, clip, getCriteria(webServiceContext));
								return ALREADY_EXISTS;
							}

							Category selectionCategory = rbtCacheManager.getCategory(selection.categoryID());
							String selectionContentType = selectionCategory.getCategoryInfo(Category.CategoryInfoKeys.CONTENT_TYPE);

							String contentType = category.getCategoryInfo(Category.CategoryInfoKeys.CONTENT_TYPE);

							if (selectionContentType != null && selectionContentType.equalsIgnoreCase(contentType))
							{
								sameContentTypeSelection = selection;
								break;
							}
						}
						else
						{
							Clip selectionClip = rbtCacheManager.getClipByRbtWavFileName(selection.subscriberFile());
							if (selectionClip != null)
							{
								if (selectionClip.getClipId() == clip.getClipId())
								{
									logger.info("response: " + ALREADY_EXISTS);
									writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, clip, getCriteria(webServiceContext));
									return ALREADY_EXISTS;
								}

								if (selectionClip.getContentType() != null
										&& selectionClip.getContentType().equalsIgnoreCase(
												clip.getContentType()))
								{
									sameContentTypeSelection = selection;
									break;
								}
							}
						}
					}
				}
			}
			
			String circleID = DataUtils.getUserCircle(webServiceContext);

			String selectedBy = getMode(webServiceContext);
			String selectionInfo = getModeInfo(webServiceContext);
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(webServiceContext);

			String sbnID = "";
			if (sameContentTypeSelection != null)
			{
				String selectionStatus = Utility.getSubscriberSettingStatus(sameContentTypeSelection, true);
				if (!selectionStatus.equals(ACTIVE) && !selectionStatus.equals(DEACTIVE))
				{
					writeEventLog(subscriberID, getMode(webServiceContext), "404", CUSTOMIZATION, getClip(webServiceContext), getCriteria(webServiceContext));
					logger.info("response: " + selectionStatus);
					return selectionStatus;
				}
				
				Map<String, String> updateClauseMap = new HashMap<String, String>();
				updateClauseMap.put("SEL_STATUS", iRBTConstant.STATE_DEACTIVATED);
				updateClauseMap.put("DESELECTED_BY", selectedBy);
				updateClauseMap.put("LOOP_STATUS", String.valueOf(iRBTConstant.LOOP_STATUS_EXPIRED));
				updateClauseMap.put("REF_ID", null);

				Map<String, String> whereClauseMap = new HashMap<String, String>();
				whereClauseMap.put("CALLER_ID", callerID);
				whereClauseMap.put("SUBSCRIBER_WAV_FILE", sameContentTypeSelection.subscriberFile());
				whereClauseMap.put("CATEGORY_ID", String.valueOf(sameContentTypeSelection.categoryID()));
				whereClauseMap.put("STATUS", String.valueOf(sameContentTypeSelection.status()));

				rbtDBManager.deactivateSubscriberSelections(subscriberID, updateClauseMap, whereClauseMap);

				Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(sameContentTypeSelection.extraInfo());
				sbnID = (extraInfoMap == null) ? "" : extraInfoMap.get("SBN_ID");
				
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				Date curDate = new Date();
				String subscriberType = sameContentTypeSelection.prepaidYes() ? "P" : "B";
				
				StringBuilder tLogBuilder = new StringBuilder();
				tLogBuilder.append(circleID).append("|").append(subscriberID).append("|").append(subscriberType);
				tLogBuilder.append("|").append(sbnID).append("|").append("RBT_SEL_").append(sameContentTypeSelection.classType());
				tLogBuilder.append("|").append("D").append("||0|").append(selectedBy).append("|mmp|").append(format.format(sameContentTypeSelection.setTime()));
				tLogBuilder.append("|").append(format.format(curDate)).append("|").append(format.format(curDate)).append("|0|C|0|||D|||||");

				RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG, tLogBuilder.toString());
			}

			String language = webServiceContext.getString(param_language);
			if (!webServiceContext.containsKey(param_activatedNow) && language != null
					&& (subscriber.language() == null || !subscriber.language().equalsIgnoreCase(language)))
			{
				rbtDBManager.setSubscriberLanguage(subscriberID, language);
				subscriber.setLanguage(language);
			}

			String subYes = subscriber.subYes();
			boolean isPrepaid = subscriber.prepaidYes();

			Categories categoriesObj = CategoriesImpl.getCategory(category);
			String classType = null;

			boolean useSubManager = true;

			String messagePath = null;
			Parameters messagePathParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MESSAGE_PATH", null);
			if (messagePathParam != null)
				messagePath = messagePathParam.getValue().trim();

			boolean changeSubType = true;
			boolean inLoop = false;
			String transID = null;

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

					if (schedule != null)
					{
						startDate = schedule.startTime();
						endDate = schedule.endTime();
						classType = schedule.classType();
					}
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
			if (webServiceContext.containsKey(param_status))
				status = Integer.parseInt(webServiceContext.getString(param_status));

			String action = webServiceContext.getString(param_action);
			// For gift selections no need to increment the selection count
			boolean incrSelCountParam = getParamAsBoolean(iRBTConstant.COMMON, "INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");
			boolean incrSelCount = incrSelCountParam || !action.equalsIgnoreCase(action_acceptGift);

			boolean allowPremiumContent = webServiceContext.getString(param_allowPremiumContent) != null
					&& webServiceContext.getString(param_allowPremiumContent).equalsIgnoreCase(YES);

			HashMap<String, Object> clipMap = new HashMap<String, Object>();
			if (category != null && Utility.isShuffleCategory(category.getCategoryTpe()))
			{
				Clip[] clips = rbtCacheManager.getActiveClipsInCategory(category.getCategoryId(), browsingLanguage);
				
				
				
				
				if (!DataUtils.isContentAllowed(cos, clips) && !allowPremiumContent)
				{
					String cosid = (subscriber != null) ?  subscriber.cosID(): null;

					List<String> currentContentType = new ArrayList<String>();
					for (Clip clipItem : clips) {
						currentContentType.add(clipItem.getContentType());
					}
					// RBT-14835 Blocking PPL content for specific service	
					if(rbtDBManager.isContentTypeBlockedForCosIdorUdsType(cosid, currentContentType)){
						logger.info("Content type ::" + currentContentType + " is blocked for cosid:: "+ cosid);
						logger.info("VodafoneRBTProcessor :: processSelection() response :"+LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED );
						return LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
					}
					writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
					if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER", "FALSE"))
					{
						webServiceContext.put(param_info, VIRAL_DATA);
						webServiceContext.put(param_type, "SELCONFPENDING");
						webServiceContext.put(param_info + "_CATEGORY_ID", webServiceContext.getString(param_categoryID));
						if (!RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED", "FALSE"))
						{
							removeData(webServiceContext);
						}

						addData(webServiceContext);
					}

					return LITE_USER_PREMIUM_BLOCKED;
				}

				clip = clips[0];
			}
			else if (!webServiceContext.containsKey(param_cricketPack))
			{
				if (webServiceContext.containsKey(param_profileHours)
						|| (category != null
								&& (category.getCategoryTpe() == iRBTConstant.RECORD
								|| category.getCategoryTpe() == iRBTConstant.KARAOKE)))
				{
					String rbtFile = webServiceContext.getString(param_clipID);
					if (rbtFile.toLowerCase().endsWith(".wav"))
						rbtFile = rbtFile.substring(0, rbtFile.length() - 4);

					clipMap.put("CLIP_WAV", rbtFile);
				}
			}

			if (clip != null)
			{	
				
				
				if (!DataUtils.isContentAllowed(cos, clip) && !allowPremiumContent)
				{
					// RBT-14835 Blocking PPL content for specific service
					String cosid = (subscriber == null) ? null : subscriber
							.cosID();
					List<String> currentContentType = new ArrayList<String>();
					currentContentType.add(clip.getContentType());

					if (rbtDBManager.isContentTypeBlockedForCosIdorUdsType(cosid, currentContentType)) {
						logger.info("Content type ::" + currentContentType
								+ " is blocked for cosid:: " + cosid);
						logger.info("VodafoneRBTProcessor :: processSelection() response :"
								+ LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED);
						return LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED;
					}
					writeEventLog( subscriberID , getMode(webServiceContext) , "404" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
					if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER", "FALSE"))
					{
						webServiceContext.put(param_info, VIRAL_DATA);
						webServiceContext.put(param_type, "SELCONFPENDING");
						webServiceContext.put(param_info + "_CATEGORY_ID", webServiceContext.getString(param_categoryID));
						if (!RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED", "FALSE"))
						{
							removeData(webServiceContext);
						}

						addData(webServiceContext);
					}

					return LITE_USER_PREMIUM_BLOCKED;
				}

				webServiceContext.put(session_clip, clip);

				clipMap.put("CLIP_CLASS", clip.getClassType());
				clipMap.put("CLIP_END", clip.getClipEndTime());
				clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
				clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
				clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
				clipMap.put("CLIP_NAME", clip.getClipName());

				if (clip.getContentType() != null && clip.getContentType().equalsIgnoreCase(CONTENT_TYPE_FEED))
				{
					// Changed for RBT-1058 (Infotainment RRBT and PreCall)
					// If clip content type is FEED, get CategoryID from configuration.
					// Pass categoryInfo corresponding to that categoryID
					if (category == null || (category != null && !Utility.isShuffleCategory(category
									.getCategoryTpe()))) {
						String feedCategoryID = CacheManagerUtil.getParametersCacheManager()
								.getParameter(iRBTConstant.COMMON, "FEED_CATEGORY_ID", "3")
								.getValue();
						Category feedCategory = rbtCacheManager.getCategory(Integer
								.parseInt(feedCategoryID));

						if (feedCategory != null)
							categoriesObj = CategoriesImpl.getCategory(feedCategory);
					}
				}
			}

			if (classType == null)
				classType = getChargeClass(webServiceContext, subscriber, category, clip);

			String chargingPackage = getChargingPackage(webServiceContext, subscriber, category, clip);

			if (webServiceContext.containsKey(param_inLoop) && webServiceContext.getString(param_inLoop).equalsIgnoreCase(YES))
				inLoop = true;

			transID = webServiceContext.getString(param_transID);

			int selectionType = 0;
			if (webServiceContext.containsKey(param_selectionType))
				selectionType = Integer.parseInt(webServiceContext.getString(param_selectionType));

			boolean useUIChargeClass = webServiceContext.containsKey(param_useUIChargeClass)
					&& webServiceContext.getString(param_useUIChargeClass).equalsIgnoreCase(YES);

			String selResponse = null;
			if (webServiceContext.containsKey(param_offerID))
				selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, webServiceContext.getString(param_offerID));

			if (sbnID != null && sbnID.length() > 0)
				selectionInfoMap.put("SBN_ID", sbnID);

			boolean isDirectActivation = sameContentTypeSelection != null;

			/*
			 * If below parameter is TRUE, we allow overlapping Time of Day and Day of Week selections.
			 * 
			 * If we set doTODCheck to false, then at DB layer, we don't check for the overlapping selections.
			 */
			boolean isOverlapAllowed = RBTParametersUtils.getParamAsBoolean("COMMON", "IS_OVERLAP_ALLOWED_FOR_TIME_AND_DAY_SELECTIONS", "FALSE");
			boolean doTODCheck = !isOverlapAllowed;

			String refID = (sameContentTypeSelection == null) ? null : sameContentTypeSelection.refID();
			selResponse = rbtDBManager.addSubscriberSelections(subscriberID, callerID, categoriesObj, clipMap,
					null, startDate, endDate, status, selectedBy, selectionInfo, 0, isPrepaid, changeSubType,
					messagePath, fromTime, toTime, classType, useSubManager, doTODCheck, "VUI", chargingPackage,
					subYes, null, circleID, incrSelCount, false, transID, false, false, inLoop, subscriber.subscriptionClass(),
					subscriber, selectionType, interval, selectionInfoMap, useUIChargeClass, refID, isDirectActivation);

			if(selResponse.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED ))
			{
				writeEventLog( subscriberID , getMode(webServiceContext) , "204" , PURCHASE , getClip(webServiceContext));
				writeEventLog( subscriberID , getMode(webServiceContext) , "402" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
			}
			else
			{
				writeEventLog( subscriberID , getMode(webServiceContext) , "201" , PURCHASE , getClip(webServiceContext));
				writeEventLog( subscriberID , getMode(webServiceContext) , "402" , CUSTOMIZATION , getClip(webServiceContext) , getCriteria(webServiceContext));
			}

			response = Utility.getResponseString(selResponse);

			boolean removeGiftIfAlreadyExists = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "REMOVE_GIFT_IF_SAME_SONG_EXISTS", "true");
			if (response.contains(SUCCESS) || (removeGiftIfAlreadyExists && response.equals(ALREADY_EXISTS)))
			{
				if (action.equalsIgnoreCase(action_acceptGift))
				{
					String gifterID = webServiceContext.getString(param_gifterID);
					Date sentTime = dateFormat.parse(webServiceContext.getString(param_giftSentTime));

					rbtDBManager.updateViralPromotion(gifterID, subscriberID, sentTime, "GIFTED", "ACCEPT_ACK", new Date(), null, null);
				}
			}

			if (response.contains(SUCCESS) && sameContentTypeSelection != null)
			{
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				Date curDate = new Date();
				String subscriberType = sameContentTypeSelection.prepaidYes() ? "P" : "B";
				
				StringBuilder tLogBuilder = new StringBuilder();
				tLogBuilder.append(circleID).append("|").append(subscriberID).append("|").append(subscriberType);
				tLogBuilder.append("|").append(sbnID).append("|").append("RBT_SEL_").append(sameContentTypeSelection.classType());
				tLogBuilder.append("|").append("A").append("||0|").append(selectedBy).append("|mmp|").append(format.format(curDate));
				tLogBuilder.append("|").append(format.format(curDate)).append("||0|C|0|||P|||").append("[CHG=1,0.0,,,,,Already Charged]").append("||");

				RBTEventLogger.logEvent(RBTEventLogger.Event.TLOG, tLogBuilder.toString());
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext webserviceContext, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(webserviceContext, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}
}
