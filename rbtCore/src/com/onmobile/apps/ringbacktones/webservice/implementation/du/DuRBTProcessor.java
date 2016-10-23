/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.du;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.ExtraInfoKey;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Status;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests.Type;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.filters.RbtFilterParser;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class DuRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(DuRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}
	
	@Override
	public String processSelection(WebServiceContext task) {
		String response = ERROR;
		boolean isAnyResponseSuccess = false;
		logger.info("Processing selection for task: "+task);
		String subscriberID = task.getString(param_subscriberID);
		try {
			
			if(task.containsKey(param_chargeClass) 
					&& overrideChargeClassMap.containsKey(task.getString(param_chargeClass))) {
				task.put(param_chargeClass, overrideChargeClassMap.get(task.getString(param_chargeClass)));
			}
			
			if (getParamAsString(iRBTConstant.COMMON,
					"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION", null) != null) {
				Subscriber sub = DataUtils.getSubscriber(task);
				String extraInfo = (sub != null) ? sub.extraInfo() : null;
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(extraInfo);
				if (extraInfoMap != null && extraInfoMap.containsKey("UNSUB_DELAY")
						&& sub.endDate().after(new Date())) {
					return NOT_ALLOWED;
				}
			}


			String callerId = (!task.containsKey(param_callerID) || task
					.getString(param_callerID).equalsIgnoreCase(ALL)) ? null
					: task.getString(param_callerID);
			// array of comma seperated caller Ids
			String[] validCallerIds = (callerId != null) ? DataUtils
					.getValidCallerIds(callerId) : null;
			if (validCallerIds == null) {
				validCallerIds = new String[] { null };
			} else if (validCallerIds.length == 0) {
				logger.info("Invalid callerID. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			/*
			 * if (callerID != null && !callerID.startsWith("G")) { // callerID
			 * null means for ALL callers and if starts with 'G' // means
			 * groupID.
			 * 
			 * Parameters parameter = parametersCacheManager.getParameter(
			 * iRBTConstant.COMMON, "MINIMUM_CALLER_ID_LENGTH", "7"); int
			 * minCallerIDLength = Integer.parseInt(parameter.getValue());
			 * 
			 * boolean validCallerID = false; if (callerID.length() >=
			 * minCallerIDLength) { try { Long.parseLong(callerID);
			 * validCallerID = true; } catch (NumberFormatException e) { } }
			 * 
			 * if (!validCallerID) {
			 * logger.info("Invalid callerID. Returning response: " +
			 * INVALID_PARAMETER); writeEventLog(subscriberID, getMode(task),
			 * "404", CUSTOMIZATION, getClip(task), getCriteria(task)); return
			 * INVALID_PARAMETER; } }
			 */

			int status = 1;
			int fromHrs = 0;
			int toHrs = 23;
			int fromMinutes = 0;
			int toMinutes = 59;

			// Time based selection
			if (task.containsKey(param_fromTime) || task.containsKey(param_toTime)) {
				status = 80;
			}
			
			if (task.containsKey(param_fromTime))
				fromHrs = Integer.parseInt(task.getString(param_fromTime));
			if (task.containsKey(param_toTime))
				toHrs = Integer.parseInt(task.getString(param_toTime));
			if (task.containsKey(param_toTimeMinutes))

				toMinutes = Integer.parseInt(task
						.getString(param_toTimeMinutes));
			if (task.containsKey(param_fromTimeMinutes))
				fromMinutes = Integer.parseInt(task
						.getString(param_fromTimeMinutes));

			if (fromHrs < 0 || fromHrs > 23 || toHrs < 0 || toHrs > 23
					|| fromMinutes < 0 || fromMinutes > 59 || toMinutes < 0
					|| toMinutes > 59) {
				logger.info("Invalid fromTime or toTime. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			DecimalFormat decimalFormat = new DecimalFormat("00");
			int fromTime = Integer.parseInt(fromHrs
					+ decimalFormat.format(fromMinutes));
			int toTime = Integer.parseInt(toHrs
					+ decimalFormat.format(toMinutes));

			String interval = task.getString(param_interval);
			if (interval != null)
				interval = interval.toUpperCase();
			if (!Utility.isValidSelectionInterval(interval)) {
				logger.warn("Invalid interval. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			// Added By Sandeep for profile selection
			int selType = -1;
			if (task.containsKey(param_selectionType)) {
				String strSelType = task.getString(param_selectionType);
				try {
					selType = Integer.parseInt(strSelType);
				} catch (NumberFormatException ne) {
				}
			}

			Calendar endCal = Calendar.getInstance();
			endCal.set(2037, 0, 1);
			Date endDate = endCal.getTime();
			Date startDate = null;

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			if (task.containsKey(param_selectionStartTime)) {
				String startTimeStr = task.getString(param_selectionStartTime);
				if (startTimeStr.length() != 8 && startTimeStr.length() != 17) {
					logger.info("Invalid selectionStartTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				if (startTimeStr.length() == 8)
					startTimeStr += "000000000";

				startDate = dateFormat.parse(startTimeStr);
			}

			if (task.containsKey(param_selectionEndTime)) {
				String endTimeStr = task.getString(param_selectionEndTime);
				if (endTimeStr.length() != 8 && endTimeStr.length() != 17) {
					logger.info("Invalid selectionEndTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				if (endTimeStr.length() == 8)
					endTimeStr += "235959000";
				else if (endTimeStr.endsWith("000000000"))
					endTimeStr = endTimeStr.substring(0, 8) + "235959000";

				endDate = dateFormat.parse(endTimeStr);
			}

			if (task.containsKey(param_selectionStartTime)
					&& task.containsKey(param_selectionEndTime)) {
				if (startDate != null && startDate.compareTo(endDate) >= 0) {
					logger.info("selectionStartTime is not less than selectionEndTime. Returning response: "
							+ INVALID_PARAMETER);
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return INVALID_PARAMETER;
				}

				// If selectionStartTime & selectionEndTime passed, then
				// selection interval will be ignored.
				interval = null;
			}

			if (!task.containsKey(param_categoryID)
					&& !task.containsKey(param_categoryPromoID)
					&& !task.containsKey(param_categorySmsAlias)) {
				logger.info("categoryID parameter not passed. Returning response: "
						+ INVALID_PARAMETER);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, getClip(task), getCriteria(task));
				return INVALID_PARAMETER;
			}

			String browsingLanguage = task.getString(param_browsingLanguage);
			Category category = null;
			if (task.containsKey(param_categoryID))
				category = rbtCacheManager.getCategory(
						Integer.parseInt(task.getString(param_categoryID)),
						browsingLanguage);
			else if (task.containsKey(param_categoryPromoID))
				category = rbtCacheManager
						.getCategoryByPromoId(
								task.getString(param_categoryPromoID),
								browsingLanguage);
			else if (task.containsKey(param_categorySmsAlias))
				category = RBTCacheManager.getInstance().getCategoryBySMSAlias(
						task.getString(param_categorySmsAlias));

			Clip clip = getClip(task);
			String contentNotExists = DataUtils.isContentExists(task, category,
					clip);
			logger.info("Got the clip from cache. " + clip
					+ ", contentNotExists: " + contentNotExists);
			if (contentNotExists != null) {
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, clip, getCriteria(task));
				return contentNotExists;
			}

			// If categoryPromoID or categorySmsAlias is passed, then populate
			// the categoryID parameter for further references.
			if (category != null)
				task.put(param_categoryID,
						String.valueOf(category.getCategoryId()));
			// If clipPromoID or clipSmsAlias is passed, then populate the
			// clipID parameter for further references.
			if (clip != null)
				task.put(param_clipID, String.valueOf(clip.getClipId()));

			String contentExpired = DataUtils.isContentExpired(task, category,
					clip, selType);
			boolean activateEvenContentExpired = RBTParametersUtils
					.getParamAsBoolean(iRBTConstant.COMMON,
							"ACTIVATE_EVEN_CONTENT_EXPIRED", "TRUE");
			if (contentExpired != null
					&& (!activateEvenContentExpired || category
							.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE)) {
				logger.info("response: " + contentExpired);
				writeEventLog(subscriberID, getMode(task), "404",
						CUSTOMIZATION, clip, getCriteria(task));
				return contentExpired;
			}

			String action = task.getString(param_action);
			
			if(rbtDBManager.isOverwirteSongPack(subscriberID, task)) {
				action = action_overwrite;
			}
			
			if (action.equalsIgnoreCase(action_overwrite)
					|| action.equalsIgnoreCase(action_overwriteGift)) {
				@SuppressWarnings("null")
				// Clip will not be null here exception Record My own or karaoke
				String subscriberWavFile = null;
				if (!task.containsKey(param_cricketPack)) {
					if (task.containsKey(param_profileHours)
							|| selType == iRBTConstant.PROFILE_SEL_TYPE
							|| category.getCategoryTpe() == iRBTConstant.RECORD
							|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
						String rbtFile = task.getString(param_clipID);
						if (rbtFile.toLowerCase().endsWith(".wav"))
							rbtFile = rbtFile
									.substring(0, rbtFile.length() - 4);

						subscriberWavFile = rbtFile;
					}
				}
				if (clip != null) {
					subscriberWavFile = clip.getClipRbtWavFile();
				}

				if (subscriberWavFile == null) {
					throw new Exception(
							"Wavfile is null, not able to overite selection");
				}
				SubscriberDownloads subscriberDownload = rbtDBManager
						.getActiveSubscriberDownload(subscriberID,
								subscriberWavFile);
				if (subscriberDownload == null
						&& !rbtDBManager.isDownloadAllowed(subscriberID, task)) {
					if(task.containsKey("MUSIC_PACK_DOWNLOAD_REACHED")) {
						return OVERLIMIT;
					}
					response = deleteTone(task);
					if (!response.equalsIgnoreCase(SUCCESS))
						return response;
				}

				if (action.equalsIgnoreCase(action_overwriteGift)) {
					action = action_acceptGift;
					task.put(param_action, action_acceptGift);
				}
			}
			
			task.put(param_requestFromSelection, "true");

			boolean isLimitedPackRequest = false;
			if (task.containsKey(param_cosID)) {
				/*
				 * Limited pack requests flow.
				 */
				String cosID = task.getString(param_cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);
				logger.info("Checking cosType for "
						+ "LIMITED_DOWNLOADS. cosID: " + cosID
						+ ", cosDetails: " + cosDetails);

				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					isLimitedPackRequest = true;
				}
			}

			boolean isContentAllowed = DataUtils.isContentAllowed(task,
					category, clip);
			logger.info("Checking content allowed or not. "
					+ "isContentAllowed: " + isContentAllowed);

			if (!isContentAllowed) {
				// When a LITE user tries to buy a premium content and the below
				// parameter is configured,
				// then the base is upgraded to DEFAULT COS configured.
				String upgrdCosID = RBTParametersUtils.getParamAsString(
						iRBTConstant.COMMON,
						"UPGRADE_COSID_FOR_LITE_USER_PREMIUM_SELECTION", null);
				logger.info("The content is not allowed, checking upgradeCosID "
						+ " is configured or not. upgradeCosID: " + upgrdCosID);
				
				Subscriber subscriber = DataUtils.getSubscriber(task);
				if (subscriber != null) {
					Map<String, String> upgradeCodIdMap = MapUtils.convertToMap(upgrdCosID, ";",
						                                   	"=", ",");
					if (upgradeCodIdMap != null && upgradeCodIdMap.containsKey(subscriber.cosID())) {
					  String upgradeCosID = upgradeCodIdMap.get(subscriber.cosID());
					  if (upgradeCosID != null) {
						String subscriberStatus = subscriber.subYes();
						logger.info("Since the content is not allowed,"
								+ " checking upgradeCosID: " + upgradeCosID
								+ ", status: " + subscriberStatus
								+ " for subscriberId: " + subscriber.subID());
						if (subscriberStatus
								.equals(iRBTConstant.STATE_ACTIVATED)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_CHANGE)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_GRACE)
								|| subscriberStatus
										.equals(iRBTConstant.STATE_ACTIVATION_PENDING)) {
							CosDetails upgradeCos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											upgradeCosID);
							if (upgradeCos != null) {
								logger.info("Upgrading cos id. upgradeCos: "
										+ upgradeCos + " for subscriberId: "
										+ subscriber.subID());
								task.put(param_rentalPack,
										upgradeCos.getSubscriptionClass());
								task.put(param_cosID, upgradeCos.getCosId());
							} else {
								logger.warn("Not upgrading cos id, upgradeCos "
										+ "is not found." + " upgradeCos: "
										+ upgradeCos + " for subscriberId: "
										+ subscriber.subID());
							}
						}
					}
				  }
				}
			}

			/*
			 * Below if-else block will do the following actions. 1. If a
			 * selection request for AUTO_DOWNLOAD_SHUFFLE category comes, it is
			 * accepted only if the user is new user or user already active on
			 * some other AUTO_DOWNLOAD_SHUFFLE category.
			 * 
			 * 2. If a normal selection request comes for a user who is active
			 * on AUTO_DOWNLOAD pack, shuffle and loop selections are blocked if
			 * it is for all caller.
			 */
			if (category != null
					&& category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE) {
				logger.info("Category type is AUTO_DOWNLOAD_SHUFFLE i.e 21, for categoryId: "
						+ category.getCategoryId());
				Subscriber subscriber = DataUtils.getSubscriber(task);
				if (!rbtDBManager.isSubscriberDeactivated(subscriber)
						&& !rbtDBManager
								.isAutoDownloadPackActivated(subscriber)) {
					logger.warn("Not processing activation. Subscriber is non deactive"
							+ " and auto download pack is not activated. subscriberID: "
							+ subscriberID);
					return ACTIVATION_BLOCKED;
				}

				String circleID = DataUtils.getUserCircle(task);
				String isPrepaid = DataUtils.isUserPrepaid(task) ? YES : NO;
				List<CosDetails> cosList = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetailsByCosType(
								iRBTConstant.COS_TYPE_AUTO_DOWNLOAD, circleID,
								isPrepaid);
				if (cosList == null || cosList.size() == 0) {
					logger.warn("Not processing activation, cos not exists. subscriberID: "
							+ subscriberID);
					return COS_NOT_EXISTS;
				}

				task.put(param_packCosId, cosList.get(0).getCosId());
				// task.put(param_status, "0");
				status = 0;

				boolean useUIChargeClass = false;
				if (task.containsKey(param_useUIChargeClass))
					useUIChargeClass = task.getString(param_useUIChargeClass)
							.equalsIgnoreCase(YES)
							&& (task.containsKey(param_chargeClass));

				if (!useUIChargeClass)
					task.put(param_chargeClass, category.getClassType());

				if (!task.containsKey(param_subscriptionClass))
					task.put(param_subscriptionClass, cosList.get(0)
							.getSubscriptionClass());

				task.put(param_useUIChargeClass, YES);
			} else {
				logger.info("Category is null or category type is non "
						+ "AUTO_DOWNLOAD_SHUFFLE i.e 21, for categoryId: "
						+ category.getCategoryId());
				if (task.containsKey(param_status))
					status = Integer.parseInt(task.getString(param_status));

				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				
				if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
					List<ProvisioningRequests> provList = null;
					boolean isAutoDownloadPackActivated = false;
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(subscriber.extraInfo());
					logger.info("Checking for the PACK attribute of subscriber. "
							+ subscriberID + ", extraInfoMap: " + extraInfoMap);
					if (extraInfoMap != null
							&& extraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_PACK)) {
						String packStr = extraInfoMap
								.get(iRBTConstant.EXTRA_INFO_PACK);
						String[] packs = (packStr != null && packStr.trim().length() > 0) ? packStr.trim()
								.split(",") : null;
						for (int i = 0; packs != null && i < packs.length; i++) {
							String activePackCosId = packs[i];
							CosDetails activeCosDet = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											activePackCosId);
							String activeCosType = activeCosDet.getCosType();
							
							if (activeCosType != null
									&& activeCosType
											.equalsIgnoreCase(iRBTConstant.COS_TYPE_AUTO_DOWNLOAD)) {
								logger.info("Checking pack status. "
										+ "activePackCosId: " + activePackCosId);
								provList = ProvisioningRequestsDao
										.getBySubscriberIDTypeAndNonDeactivatedStatus(
												subscriber.subID(),
												Integer.parseInt(activePackCosId));
								isAutoDownloadPackActivated = provList != null;
								logger.info("Verified pack status. subscriberID: "
										+ subscriberID
										+ ", activePackCosId: "
										+ activePackCosId
										+ ", isAutoDownloadPackActivated: "
										+ isAutoDownloadPackActivated);
								
							} else {
								logger.info("Cos type is not AUTO_DOWNLOAD. "
										+ "activePackCosId: " + activePackCosId
										+ ", activeCosType: " + activeCosType
										+ ", subscriberId: " + subscriberID);
							}
						}
					}

					if (isAutoDownloadPackActivated) {
						if (callerId == null
								&& !task.containsKey(param_profileHours)
								&& !task.containsKey(param_cricketPack)) {
							if (Utility.isShuffleCategory(category
									.getCategoryTpe()))
								return NOT_ALLOWED;

							task.put(param_inLoop, NO); // For ALL caller
														// selections, loop
														// selections are to be
														// added in override
														// mode as per the
														// requirement.

							Map<String, String> packExtraInfoMap = DBUtility
									.getAttributeMapFromXML(provList.get(0)
											.getExtraInfo());
							if (packExtraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_PACK_MAX_ALLOWED)) {
								int maxAllowed = Integer
										.parseInt(packExtraInfoMap
												.get(iRBTConstant.EXTRA_INFO_PACK_MAX_ALLOWED));
								if (maxAllowed != 0
										&& maxAllowed <= provList.get(0)
												.getNumMaxSelections()) {
									return LIMIT_EXCEEDED;
								}
							}
						}
					}
				} else {
					logger.warn("Not checking pack details, subscriber is deactive or not exists. "
							+ "subscriber: " + subscriber);
				}
			}

			// RBT-7725 : Rejecting new purchase request if base already pending
			// request in queue
			String baseStatusForSelBlock = RBTParametersUtils.getParamAsString(
					"COMMON", "BASE_STATUS_FOR_BLOCKING_SELECTION", null);
			if (baseStatusForSelBlock != null) {
				List<String> baseStatusForSelBlockList = Arrays
						.asList(baseStatusForSelBlock.split(","));
				Subscriber subscriber = rbtDBManager
						.getSubscriber(subscriberID);
				String baseStatus = Utility.getSubscriberStatus(subscriber);
				if (baseStatusForSelBlockList != null
						&& baseStatusForSelBlockList.contains(baseStatus)) {
					return SELECTIONS_BLOCKED + "_" + baseStatus;
				}
			}

			// VF-Spain changes for Resubscription RBT-7448
			String deactivatedWithSameSong = checkIfDeactivatedWithSameSongActive(task);
			if (deactivatedWithSameSong != null) {
				return deactivatedWithSameSong;
			}

			// Selection offer check
			if (getParamAsBoolean(iRBTConstant.COMMON,
					iRBTConstant.ALLOW_GET_OFFER, "FALSE")
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.IS_SEL_OFFER_MANDATORY, "FALSE")) {
				boolean downloadExists = true;
				if (getParamAsBoolean(iRBTConstant.COMMON, "ADD_TO_DOWNLOADS",
						"FALSE")) {
					SubscriberDownloads download = rbtDBManager
							.getActiveSubscriberDownload(subscriberID,
									clip.getClipRbtWavFile());
					if (download == null)
						downloadExists = false;

					if (!downloadExists && !task.containsKey(param_offerID))
						return WebServiceConstants.OFFER_NOT_FOUND;
				} else {
					if (!task.containsKey(param_offerID))
						return WebServiceConstants.OFFER_NOT_FOUND;
				}
			}

			if (task.containsKey(param_cosID)) {
				String songBasedCosId = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "SONG_BASED_COS_ID", null);
				if(songBasedCosId != null) {
					List<String> cosIdsList = Arrays.asList(songBasedCosId.split(","));
					String cosId = task.getString(param_cosID);
					Subscriber subscriber = DataUtils.getSubscriber(task);
					if(cosIdsList.contains(cosId) && !rbtDBManager.isSubActive(subscriber)) {
						boolean isShuffleCategory = false;
						if (null != category) {
							isShuffleCategory = Utility
									.isShuffleCategory(category
											.getCategoryTpe());
						}
						logger.info("Performing COS validation. cosId: "
								+ cosId + ", category: " + category
								+ ", isShuffleCategory: " + isShuffleCategory);
						if(task.containsKey(param_profileHours) || task.containsKey(param_cricketPack) || 
								(task.getString(param_status) != null && task.getString(param_status).equalsIgnoreCase("99")) || 
								(task.containsKey(param_callerID) && !task.getString(param_callerID).equalsIgnoreCase("ALL")) || 
								(task.containsKey(param_interval) || (fromTime != 0 || toTime != 2359))
								|| isShuffleCategory) {
							return COSID_BLOCKED_CIRCKET_PROFILE;
						}
//						task.put(param_userInfo + "_"  + iRBTConstant.UDS_OPTIN, "TRUE");
					}
					
					
				}
			}
			boolean isSubscriberAlreadyNotDeactive = rbtDBManager.isSubscriberActivated(subscriberID);  
			logger.info("isSubscriberAlreadyNotDeactive = "+isSubscriberAlreadyNotDeactive);
			if (Arrays.asList(getParamAsString("COMMON", "MODES_FOR_CHECKING_IF_COMBO_REQUEST_PENDING", "")
							.split(",")).contains(task.getString(param_mode))) {
				List<DoubleConfirmationRequestBean> baseConsentPendingRecordList = rbtDBManager
						.getConsentPendingRecordListByMsisdnNType(subscriberID, "ACT");
				List<DoubleConfirmationRequestBean> selConsentPendingRecordList = rbtDBManager
						.getConsentPendingRecordListByMsisdnNType(subscriberID, "SEL");
				if (baseConsentPendingRecordList != null && selConsentPendingRecordList != null
						&& selConsentPendingRecordList.size() > 0
						&& baseConsentPendingRecordList.size() > 0) {
					return ACT_SEL_CONSENT_PENDING;
				}
			}
			
			task.put(IS_COMBO_REQUEST, true);
			if (isLimitedPackRequest) {
				response = upgradeSelectionPack(task);
			} else {
				response = processActivation(task);
				logger.info("Processed activation. response: " + response
						+ ", subscriberId: " + subscriberID);
			}
            boolean isBlackListed = false;
            if(response!=null&&response.equalsIgnoreCase("black_listed")){
            	isBlackListed = true;
            }
			if (YES.equals(task.getString(param_songAlreadyAdded)))
				return response;

			String classType = null;
			Parameters contentTypeParameter = parametersCacheManager
					.getParameter("COMMON", "OFFER_CONTENT_TYPES", null);
			if (contentTypeParameter != null && clip != null) {
				logger.info("The clip is " + clip);
				String offerContentTypes = contentTypeParameter.getValue();
				List<String> contentTypeList = Arrays.asList(offerContentTypes
						.split(","));
				if (contentTypeList.contains(clip.getContentType())) {
					logger.info("The content type matches with the offer content type "
							+ clip.getContentType());
					try {
						HashMap<String, String> offerExtraInfo = new HashMap<String, String>();
						offerExtraInfo.put("CLIP_CONTENT_TYPE",
								clip.getContentType());
						com.onmobile.apps.ringbacktones.smClient.beans.Offer[] allOffers = RBTSMClientHandler
								.getInstance()
								.getOffer(
										subscriberID,
										getMode(task),
										com.onmobile.apps.ringbacktones.smClient.beans.Offer.OFFER_TYPE_SELECTION,
										DataUtils.isUserPrepaid(task) ? "p"
												: "b", clip.getClassType(),
										offerExtraInfo);
						if (allOffers == null || allOffers.length < 1)
							return OFFER_NOT_FOUND;

						classType = allOffers[0].getSrvKey();
						task.put(param_offerID, allOffers[0].getOfferID());

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			if (!(task.containsKey(param_isPreConsentBaseSelRequest) && response.equalsIgnoreCase(preConsentBaseSelSuccess))) {
				if (!response.equalsIgnoreCase(SUCCESS)
						&& !response.equalsIgnoreCase(PACK_ALREADY_ACTIVE)
						&& !Utility.isUserActive(response))
					return response;
				else if (Utility.isUserActive(response)
						&& task.containsKey(param_ignoreActiveUser)
						&& task.getString(param_ignoreActiveUser)
								.equalsIgnoreCase(YES))
					return response;
			}
			// preprocesses the request to check if any content is blocked for
			// any particular requests
			String filterResponse = RbtFilterParser.getRbtFilter()
					.filterSelection(task);
			if (filterResponse != null)
				return filterResponse;


			// For Wind Italy: If any previous download is in pending state and
			// the new selection's charge class is configured, then block the
			// new selection
			try {
				String tmpResponse = Utility.isPreviousSelPending(task,
						subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}

			// RBT-5442
			try {
				String tmpResponse = Utility
						.isPreviousSelPendingWithSameChargeClass(task,
								subscriberID, category, clip);
				if (tmpResponse != null)
					return tmpResponse;
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}
			// Subscriber object is stored in task by processActivation method.
			Subscriber subscriber = DataUtils.getSubscriber(task);
			CosDetails cos = null;
			if (subscriber == null
					&& task.containsKey(param_isPreConsentBaseSelRequest)) {
				cos = (CosDetails) task.get("SUB_COS_CONSENT");
			} else {
				if (subscriber.cosID() != null) {
					cos = rbtDBManager.getCosForActiveSubscriber(task,
							subscriber);
					logger.info("Updated subscriber Cos. subscriber cosId: "
							+ subscriber.cosID() + ", returned cos: " + cos
							+ " for subscriberID: " + subscriberID);
				}
			}
			if (task.containsKey(param_removeExistingSetting)
					&& task.getString(param_removeExistingSetting)
							.equalsIgnoreCase(YES)) {
				for (String callerID : validCallerIds) {
					HashMap<String, String> requestParams = new HashMap<String, String>();
					requestParams.put(param_action, action_deleteSetting);
					requestParams.put(param_subscriberID, subscriberID);
					requestParams.put(param_callerID, callerID);
					requestParams.put(param_status, String.valueOf(1));
					requestParams.put(param_fromTime, String.valueOf(0));
					requestParams.put(param_toTime, String.valueOf(23));

					WebServiceContext tempTask = Utility.getTask(requestParams);
					deleteSetting(tempTask);
				}
			}

			String circleID = DataUtils.getUserCircle(task);

			String language = task.getString(param_language);
			String subYes = null;
			boolean isPrepaid = false;
			String subClass = null;
			int rbtType = 0;
			if (subscriber == null
					&& task.containsKey(param_isPreConsentBaseSelRequest)) {
				isPrepaid = task.get("IS_PREPAID_CONSENT")!=null?(Boolean) task.get("IS_PREPAID_CONSENT"):false;
				subClass = (String) task.get("SUB_CLASS_CONSENT");
				try{
				     rbtType = Integer.parseInt(task.getString(param_rbtType));
				}catch(Exception ex){
					rbtType = 0;
				}
			} else {
				if (!task.containsKey(param_activatedNow)
						&& language != null
						&& subscriber!=null && (subscriber.language() == null || !subscriber
								.language().equalsIgnoreCase(language))) {
					rbtDBManager.setSubscriberLanguage(subscriberID, language);
					subscriber.setLanguage(language);
				}

				subYes = subscriber.subYes();
				isPrepaid = subscriber.prepaidYes();
				subClass = subscriber.subscriptionClass();
				try{
				     rbtType = Integer.parseInt(task.getString(param_rbtType));
				}catch(Exception ex){
					rbtType = subscriber.rbtType();
				}

			}

			Categories categoriesObj = CategoriesImpl.getCategory(category);

			String selectedBy = getMode(task);
			String selectionInfo = getModeInfo(task);
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
			
			logger.info("selectedBy: " + selectedBy + ", selectionInfo: "
					+ selectionInfo + ", selectionInfoMap: " + selectionInfoMap);
			
			if(task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				selectionInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
						task.getString(iRBTConstant.EXTRA_INFO_TPCGID));
			}
			
			boolean useSubManager = true;

			String messagePath = null;
			Parameters messagePathParam = parametersCacheManager.getParameter(
					iRBTConstant.COMMON, "MESSAGE_PATH", null);
			if (messagePathParam != null)
				messagePath = messagePathParam.getValue().trim();

			boolean changeSubType = true;
			boolean inLoop = false;
			String transID = null;
			String feedType = null;
			String feedSubType = null;
			if (task.containsKey(param_cricketPack)) {
				String cricketPack = task.getString(param_cricketPack);
				logger.info("Request is cricket pack. cricketPack: "
						+ cricketPack);
				if (!cricketPack.equalsIgnoreCase("DEFAULT")) {
					FeedSchedule schedule = rbtDBManager.getFeedSchedule(
							"CRICKET", cricketPack);
					if (schedule == null) {
						Parameters cricketIntervalParm = parametersCacheManager
								.getParameter(iRBTConstant.COMMON,
										"CRICKET_INTERVAL", "2");
						int cricketInterval = Integer
								.parseInt(cricketIntervalParm.getValue().trim());

						FeedSchedule[] schedules = rbtDBManager
								.getFeedSchedules("CRICKET", cricketPack,
										cricketInterval);
						if (schedules != null && schedules.length > 0)
							schedule = schedules[0];
					}
					feedType = schedule.type();
					startDate = schedule.startTime();
					endDate = schedule.endTime();
					classType = schedule.classType();
					feedSubType = task.getString(param_cricketPack);
					
					task.put(param_useUIChargeClass, YES);
				}
				String thirdPartyConfirmationModes = CacheManagerUtil
						.getParametersCacheManager().getParameterValue(
								iRBTConstant.DOUBLE_CONFIRMATION, "TPCG_MODES",
								null);
                 if (thirdPartyConfirmationModes != null
						&& !task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)){
					selectionInfoMap.put("FEED_SUB_TYPE", feedSubType);
                 } 
				status = 90;
			} else if (task.containsKey(param_profileHours)) {
				String profileHours = task.getString(param_profileHours);
				logger.info("Request is for profile hours. profileHours: "
						+ profileHours);
				/*
				 * When profileHours is not followed by 'D' or 'M' and the clip
				 * sms alias is configured under days category, then 'D' is
				 * added before profileHours to ensure the profile selection is
				 * made for n days instead of n hours.
				 */
				if (!profileHours.startsWith("D")
						&& !profileHours.startsWith("M")
						&& isDurationDays((clip != null) ? clip
								.getClipSmsAlias() : null)) {
					profileHours = "D" + profileHours;
				}

				int minutes;
				if (profileHours.startsWith("D")) {
					minutes = Integer.parseInt(profileHours.substring(1));
					minutes *= 24 * 60;
				} else if (profileHours.startsWith("M")) {
					minutes = Integer.parseInt(profileHours.substring(1));
				} else
					minutes = Integer.parseInt(profileHours) * 60;

				endCal = Calendar.getInstance();
				endCal.add(Calendar.MINUTE, minutes);

				if (endCal.getTime().before(endDate)) {
					// Making sure that endDate will not exceed 2037-01-01
					endDate = endCal.getTime();
				}
				status = 99;
			}

			// ADDED BY SANDEEP FOR PROFILE SELECTION
			else if (task.containsKey(param_selectionType)) {
				int selectionType = Integer.parseInt(task
						.getString(param_selectionType));
				logger.info("Request is for selectionType. selectionType: "
						+ selectionType);
				if (selectionType == iRBTConstant.PROFILE_SEL_TYPE) {
					if (!task.containsKey(param_selectionStartTime)
							&& !task.containsKey(param_selectionEndTime)) {
						int minutes = 60;
						endCal = Calendar.getInstance();
						endCal.add(Calendar.MINUTE, minutes);
						if (endCal.getTime().before(endDate)) {
							endDate = endCal.getTime();
						}
					} else if (task.containsKey(param_selectionStartTime)
							&& !task.containsKey(param_selectionEndTime)) {
						int minutes = 60;
						endCal = Calendar.getInstance();
						endCal.setTime(startDate);
						endCal.add(Calendar.MINUTE, minutes);
						if (endCal.getTime().before(endDate)) {
							endDate = endCal.getTime();
						}
					}
					status = 99;
				}
			}
			
			logger.info("Verifed all the features. status: " + status
					+ ", subscriber: " + subscriber);
			
			// Pack cosid should be two digits.
			if (status == 99) {
				HashMap<String, String> existingExtraInfoMap = rbtDBManager
						.getExtraInfoMap(subscriber);
				String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
						.get(iRBTConstant.EXTRA_INFO_PACK) : null;
				if (existingPacks != null) { // User has packs
					String[] packCosIds = existingPacks.split("\\,");
					for (String packCosId : packCosIds) {
						CosDetails cosDetails = CacheManagerUtil
								.getCosDetailsCacheManager().getCosDetail(
										packCosId, subscriber.circleID());
						String cosType = (cosDetails != null) ? cosDetails
								.getCosType() : null;
						if (cosType == null
								|| !cosType
										.equalsIgnoreCase(iRBTConstant.PROFILE_COS_TYPE))
							continue;
						int type = Integer.parseInt(packCosId);
						List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
								.getBySubscriberIDAndTypeOrderByCreationTime(
										subscriberID, type);
						int size = provReqList.size();
						ProvisioningRequests pack = (size > 0) ? provReqList
								.get(size - 1) : null;
						if (pack != null) {
							int packStatus = pack.getStatus();
							if (!Utility.isPackActive(packStatus)) {
								return Utility.getSubscriberPackStatus(pack);
							}
						}
						subClass = pack.getChargingClass();
					}
				}

			}
			
			//For Idea Consent. If active user makes profile selection, then mode will be passed as configured mode
			String profileMode = RBTParametersUtils.getParamAsString("COMMON", "PROFILE_MODE_FOR_ACTIVE_USER", null);
			boolean isSubActivated = iRBTConstant.STATE_ACTIVATED.equals(subYes);
			logger.info("Verifying profile mode. profileMode: " + profileMode
					+ ", isSubActivated: " + isSubActivated + ", status: "
					+ status);
			if(status == 99 && profileMode != null && isSubActivated) {
				selectedBy = profileMode;
			}


			// for populating retailer id:
			if (task.containsKey(param_retailerID)) {
				selectionInfoMap.put("RET", task.getString(param_retailerID));
			}

			boolean useUIChargeClass = task.containsKey(param_useUIChargeClass)
					&& task.getString(param_useUIChargeClass).equalsIgnoreCase(
							YES);

			String mode = getMode(task);
			boolean isModeAffiliated = affiliatedContentModeList.contains(mode);
			boolean isNotAllowedMPForAffMode = !(isModeAffiliated && isMpByPassedForAffiliate);

			// to distinguish the selections/downloads made through different
			// packs
			boolean isNotShuffleCategory = !Utility.isShuffleCategory(category
					.getCategoryTpe());
			boolean isPackRequest = rbtDBManager.isPackRequest(cos);
			logger.debug("Checking the values to update pack attribute "
					+ "in selection extrainfo. isPackRequest: " + isPackRequest
					+ ", isNotShuffleCategory: " + isNotShuffleCategory
					+ ", isNotAllowedMPForAffMode: " + isNotAllowedMPForAffMode
					+ ", isMpByPassedForAffiliate: " + isMpByPassedForAffiliate
					+ ", isModeAffiliated: " + isModeAffiliated);
			
			if ((status == 0 && category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE)
					|| (isPackRequest && !useUIChargeClass
							&& isNotShuffleCategory && isNotAllowedMPForAffMode)) {

				selectionInfoMap.put(iRBTConstant.PACK, cos.getCosId());
			}

			if (category.getCategoryTpe() == iRBTConstant.BOX_OFFICE_SHUFFLE) {
				endDate = category.getCategoryEndTime();
				if (endDate.before(new Date())) {
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return CATEGORY_EXPIRED;
				}

				status = 92;
			} else if (category.getCategoryTpe() == iRBTConstant.FESTIVAL_SHUFFLE) {
				endDate = category.getCategoryEndTime();
				if (endDate.before(new Date())) {
					writeEventLog(subscriberID, getMode(task), "404",
							CUSTOMIZATION, getClip(task), getCriteria(task));
					return CATEGORY_EXPIRED;
				}

				status = 93;
			}
			if (task.containsKey(param_status))
				status = Integer.parseInt(task.getString(param_status));

			/*
			 * Added by SenthilRaja Get the offerid if SUPPORT_SMCLIENT_API
			 */
			String baseOfferID = null;
			String selOfferID = null;
			if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
				baseOfferID = getOfferID(task, COMBO_SUB_OFFERTYPE);
				selOfferID = getOfferID(task, SELECTION_OFFERTYPE);
			}

			// For gift selections no need to increment the selection count
			boolean incrSelCountParam = getParamAsBoolean(iRBTConstant.COMMON,
					"INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");
			boolean incrSelCount = incrSelCountParam
					|| !action.equalsIgnoreCase(action_acceptGift);

			boolean allowPremiumContent = task
					.getString(param_allowPremiumContent) != null
					&& task.getString(param_allowPremiumContent)
							.equalsIgnoreCase(YES);

			if (!allowPremiumContent) {
				allowPremiumContent = RBTParametersUtils.getParamAsBoolean(
						iRBTConstant.COMMON,
						"DIRECT_ALLOW_LITE_USER_PREMIUM_CONTENT", "FALSE");
			}
			
			/*
			 * If below parameter is TRUE, we allow overlapping
			 * Time of Day and Day of Week selections.
			 * 
			 * If we set doTODCheck to false, then at DB layer,
			 * we don't check for the overlapping selections.
			 */
			boolean isOverlapAllowed = RBTParametersUtils
					.getParamAsBoolean(
							"COMMON",
							"IS_OVERLAP_ALLOWED_FOR_TIME_AND_DAY_SELECTIONS",
							"FALSE");
			boolean doTODCheck = !isOverlapAllowed;

			String packExtraInfo = null;
			String packRefID = null;
			String refId = null;
			
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift) && !task.containsKey(param_offerID)) {
				
				boolean allowBaseOffer = RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_GET_OFFER, "FALSE");
				if(allowBaseOffer) {
					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, task.getString(param_mode), 
							Offer.OFFER_TYPE_SELECTION, null, null, null); 
					if(offer != null && offer.length > 0) {
						useUIChargeClass = true;						
						classType = offer[0].getSrvKey();
						task.put(param_offerID, offer[0].getOfferID());
						selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offer[0].getOfferID());
					}
				}
			}
			
			if (RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") || RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_SEL_OFFER, "FALSE")) {
				
				
				if(RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") && !task.containsKey(param_offerID)) {
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
					rbtDetailsRequest.setMode(task.getString(param_mode));
					rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SELECTION_STR);
					Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
					
					if(offers != null && offers.length != 0) {
						useUIChargeClass = true;						
						classType = offers[0].getSrvKey();
						task.put(param_offerID, offers[0].getOfferID());
						selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offers[0].getOfferID());
					}
				}
				
				String offerId = task.getString(param_offerID);
				String offerDaysLimit = RBTParametersUtils.getParamAsString("COMMON",
						iRBTConstant.OFFER_DAYS_LIMIT, "");
				Date date = new Date();
				logger.info("Going for Selection Offer....");
				logger.info("SubscriberID = " + subscriberID + " , Mode = "
						+ task.getString(param_mode) + " , Offer Type = 2(Selection) , ClassType = "
						+ classType);
				SubscriberDownloads[] activeSubscriberDownloads = rbtDBManager
						.getActiveSubscriberDownloads(subscriberID);

				boolean isActPendingRecord = false;
				if (activeSubscriberDownloads != null && offerId!=null && !offerId.equalsIgnoreCase("-1")) {
					for (SubscriberDownloads subDwn : activeSubscriberDownloads) {
						String xtraInfo = subDwn.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(xtraInfo);
						char dwnStatus = subDwn.downloadStatus();
						if ((dwnStatus == 'n' || dwnStatus == 'p') && xtraInfoMap != null
								&& xtraInfoMap.containsKey("OFFER_ID")
								&& Arrays.asList(offerDaysLimit.split(","))
								.contains(date.getDay() + "")) { 
							isActPendingRecord = true;
							break;
						}
					}
				}
				if (isActPendingRecord) {
					logger.info("Already one download is pending to be charged.So, Not allowing to download/selection the song");
					return NOT_ALLOWED;
				}

				String offerIdForBlacklisted = RBTParametersUtils.getParamAsString("COMMON",
						"SEL_OFFER_ID_FOR_BLACKLISTED_SUBSCRIBER", "");

				if (Arrays.asList(offerIdForBlacklisted.split(",")).contains(
						offerId)) {
					String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
							"ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriberID);
					String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("OFFER",
							"BLACKLISTED_OFFER_SEL_TEXT", language);
					task.put(param_smsText, smsText);
					sendSMS(task);
					return SUCCESS;
				}
				
				if (offerId != null && !offerId.equalsIgnoreCase("-1")) {
				    selectionInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				}
				
			}
			
			// outer for loop added by Sandeep for multiple callerIDs
			for (String callerID : validCallerIds) {
				if (category.getCategoryTpe() == iRBTConstant.DYNAMIC_SHUFFLE) {
					String chargingPackage = getChargingPackage(task,
							subscriber, category, null);

					String clipIDStr = task.getString(param_clipID);
					String[] clipIDs = clipIDStr.split(",");

					response = SUCCESS;

					Clip[] clips = new Clip[clipIDs.length];
					for (int i = 0; i < clipIDs.length; i++) {
						task.put(param_clipID, clipIDs[i]);
						clips[i] = getClip(task);
					}
					if (!DataUtils.isContentAllowed(cos, clips)
							&& !allowPremiumContent) {
						writeEventLog(subscriberID, getMode(task), "404",
								CUSTOMIZATION, getClip(task), getCriteria(task));
						if (RBTParametersUtils.getParamAsBoolean(
								iRBTConstant.COMMON,
								"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
								"FALSE")
								&& !(cos != null && PROFILE1
										.equalsIgnoreCase(cos.getCosType()))) {
							task.put(param_info, VIRAL_DATA);
							task.put(param_type, "SELCONFPENDING");
							task.put(param_info + "_CATEGORY_ID",
									task.getString(param_categoryID));
							if (!RBTParametersUtils
									.getParamAsBoolean(
											iRBTConstant.COMMON,
											"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
											"FALSE")) {
								removeData(task);
							}

							addData(task);
						}
						if (pplContentRejectionLogger != null)
							pplContentRejectionLogger
									.PPLContentRejectionTransaction(
											subscriberID, getMode(task), "-1",
											category.getCategoryId() + "",
											new Date());
						return DataUtils
								.getUnAllowedContentResponse(cos, clips);
					}
					String selResponse = "";
					boolean isNoConsentRequired = false;
					isNoConsentRequired = isNoConsentRequired(task, subscriber, isSubscriberAlreadyNotDeactive, classType, mode);
//					logger.info("isSubscriberAlreadyNotDeactive:"+isSubscriberAlreadyNotDeactive+ " classType :" + classType + " mode:"+mode + "isNoConsentRequired :" + isNoConsentRequired);
					if(isNoConsentRequired){
						task.put(param_byPassConsent, "true");
					}
					
					for (int i = 0; i < clips.length; i++) {
						clip = clips[i];

						HashMap<String, Object> clipMap = new HashMap<String, Object>();
						clipMap.put("CLIP_CLASS", clip.getClassType());
						clipMap.put("CLIP_END", clip.getClipEndTime());
						clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
						clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
						clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
						clipMap.put("CLIP_NAME", clip.getClipName());

						classType = null;
						if (i > 0) {
							classType = "FREE";
							inLoop = true;
						}

						
						// IDEA consent
						if (task.containsKey(param_isPreConsentBaseSelRequest) && !isNoConsentRequired) {
							if(isBlackListed){
								return response;
							}
							String extraInfoMap = null;
							String requestType = "SEL";
							if (Utility.getSubscriberStatus(subscriber)
									.equalsIgnoreCase(NEW_USER)
									|| Utility.getSubscriberStatus(subscriber)
											.equalsIgnoreCase(DEACTIVE)) {
								requestType = "ACT_SEL";
							}
							Clip clipObj = null;
							if (task.containsKey(param_clipID))
								clipObj = rbtCacheManager.getClip(task
										.getString(param_clipID));

							if (clipObj != null)
								task.put(param_promoID, clip.getClipPromoId());


							String consentTransID = UUID.randomUUID()
									.toString();

							selResponse = rbtDBManager.addSelectionConsent(
									consentTransID, subscriberID, callerID,
									task.getString(param_categoryID),
									task.getString(srvkey), selectedBy,
									startDate, endDate, status, classType,
									task.getString(param_cosID),
									task.getString(param_packCosId),
									task.getString(param_clipID), interval,
									fromTime, toTime, selectionInfo, selType,
									inLoop, "selection", useUIChargeClass,
									category.getCategoryTpe(),
									task.getString(param_profileHours),
									isPrepaid, feedSubType,
									(String) clipMap.get("CLIP_WAV"), rbtType,
									circleID, language, new Date(),
									extraInfoMap, requestType, 0, subscriber,
									task, categoriesObj, doTODCheck);


							return selResponse;
						}
						if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
							selectionInfoMap.put(param_offerID, selOfferID);
							if (selOfferID.equals("-2") && i == 0)
								classType = task.getString(param_chargeClass);
							else if (i == 0)
								classType = task.getString(param_chargeClass);
							else if (i > 0)
								selOfferID = "-2";

							HashMap<String, String> responseParams = new HashMap<String, String>();

							selResponse = rbtDBManager
									.smAddSubscriberSelections(subscriberID,
											callerID, categoriesObj, clipMap,
											null, startDate, endDate, status,
											selectedBy, selectionInfo, 0,
											isPrepaid, changeSubType,
											messagePath, fromTime, toTime,
											classType, useSubManager, true,
											"VUI", chargingPackage, subYes,
											null, circleID, incrSelCount,
											false, transID, false, false,
											inLoop, subClass, subscriber, 0,
											interval, selectionInfoMap,
											responseParams);

							if (selResponse
									.equalsIgnoreCase(iRBTConstant.SELECTION_SUCCESS)) {
								HashMap<String, String> xtraInfoMap = rbtDBManager
										.getExtraInfoMap(subscriber);
								if (task.containsKey(param_scratchNo)) {
									if (xtraInfoMap.containsKey("SRCS")) {
										logger.info("Updating extra info");
										rbtDBManager.updateExtraInfo(
												subscriberID, "SRCS", "1");
										rbtDBManager
												.updateScratchCard(
														task.getString(param_scratchNo),
														"1");
									}
								}
								HashMap<String, String> extraParams = getSelectionExtraParams(
										subscriber, clip, category, callerID,
										selectionInfo, selectionInfoMap);

								String selectionRefID = "";
								if (responseParams.containsKey("REF_ID")) {
									selectionRefID = responseParams.get(REF_ID);
								}
								boolean isSuccess = smClientRquestForSelection(
										task, subscriberID, subscriber,
										classType, baseOfferID, selOfferID,
										selectedBy, selectionRefID, isPrepaid,
										i, extraParams);

								if (!isSuccess)
									break;
							} else {
								if (selResponse
										.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
									writeEventLog(subscriberID, getMode(task),
											"207", PURCHASE, getClip(task));
								} else if (selResponse
										.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
									writeEventLog(subscriberID, getMode(task),
											"204", PURCHASE, getClip(task));
									writeEventLog(subscriberID, getMode(task),
											"402", CUSTOMIZATION,
											getClip(task), getCriteria(task));
								} else {
									writeEventLog(subscriberID, getMode(task),
											"201", PURCHASE, getClip(task));
									writeEventLog(subscriberID, getMode(task),
											"402", CUSTOMIZATION,
											getClip(task), getCriteria(task));
								}
							}

						} else {
							
							logger.info("Adding selection for subscriberID: "
									+ subscriberID + ", selectedBy: "
									+ selectedBy + ", classType: " + classType
									+ ", subClass: " + subClass
									+ ", selectionInfoMap: " + selectionInfoMap);
							
							refId = null;
							if(task.containsKey(param_refID) && !Utility.isModeConfiguredForIdeaConsent(selectedBy)) {
								refId = task.getString(param_refID);
							}
							if (refId == null) {
								refId = UUID.randomUUID().toString();
							}
							
	                        if(task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
	                        	clipMap.put("CONSENT_SUBSCRIPTION_INSERT", "true"); 
	                        }
	                        if(task.containsKey(param_allowPremiumContent)){
	                        	clipMap.put(param_allowPremiumContent, "y");
	                        }
                            if(task.containsKey(param_isUdsOn)){
                            	clipMap.put(param_isUdsOn, task.getString(param_isUdsOn));
                            }
							selResponse = rbtDBManager.addSubscriberSelections(
									subscriberID, callerID, categoriesObj,
									clipMap, null, startDate, endDate, status,
									selectedBy, selectionInfo, 0, isPrepaid,
									changeSubType, messagePath, fromTime,
									toTime, classType, useSubManager,
									doTODCheck, "VUI", chargingPackage, subYes,
									null, circleID, incrSelCount, false,
									transID, false, false, inLoop, subClass,
									subscriber, 0, interval, selectionInfoMap,
									useUIChargeClass, refId, false);
							logger.info("Added selection, response: "
									+ selResponse + ", for subscriberID: "
									+ subscriberID + ", selectionInfo: "
									+ selectionInfo + ", classType: "
									+ classType + ", selectionInfoMap: "
									+ selectionInfoMap);
						}
                        if(clipMap.containsKey("RECENT_CLASS_TYPE")){
                        	task.put("RECENT_CLASS_TYPE", clipMap.remove("RECENT_CLASS_TYPE"));
                        }
						response = Utility.getResponseString(selResponse);
						if (response.equalsIgnoreCase(SUCCESS)) {
							logger.info("Selection is success");
							
							//For consent
							task.put("CONSENTID",clipMap.remove("CONSENTID"));
							task.put("CONSENTCLASSTYPE", clipMap.remove("CONSENTCLASSTYPE"));
							task.put("CONSENTSUBCLASS", clipMap.remove("CONSENTSUBCLASS"));
							task.put("CONSENT_SERVICE_ID",clipMap.remove("CONSENT_SERVICE_ID"));
							task.put("CONSENT_SERVICE_CLASS",clipMap.remove("CONSENT_SERVICE_CLASS"));
							
							
							HashMap<String, String> xtraInfoMap = rbtDBManager
									.getExtraInfoMap(subscriber);
							if (task.containsKey(param_scratchNo)) {
								if (xtraInfoMap.containsKey("SRCS")) {
									logger.info("Updating extra info");
									rbtDBManager.updateExtraInfo(subscriberID,
											"SRCS", "1");
									rbtDBManager.updateScratchCard(
											task.getString(param_scratchNo),
											"1");
								}
							}
						} else {
							if (selResponse
									.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
								writeEventLog(subscriberID, getMode(task),
										"207", PURCHASE, getClip(task));
							} else if (selResponse
									.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
								writeEventLog(subscriberID, getMode(task),
										"204", PURCHASE, getClip(task));
								writeEventLog(subscriberID, getMode(task),
										"402", CUSTOMIZATION, getClip(task),
										getCriteria(task));
							} else {
								writeEventLog(subscriberID, getMode(task),
										"201", PURCHASE, getClip(task));
								writeEventLog(subscriberID, getMode(task),
										"402", CUSTOMIZATION, getClip(task),
										getCriteria(task));
							}

						}
						if (!response.equalsIgnoreCase(SUCCESS))
							break;
					}
				} else {
					HashMap<String, Object> clipMap = new HashMap<String, Object>();
					if (Utility.isShuffleCategory(category.getCategoryTpe())) {
						Clip[] clips = rbtCacheManager
								.getActiveClipsInCategory(
										category.getCategoryId(),
										browsingLanguage);
						if (!DataUtils.isContentAllowed(cos, clips)
								&& !allowPremiumContent) {
							writeEventLog(subscriberID, getMode(task), "404",
									CUSTOMIZATION, getClip(task),
									getCriteria(task));
							if (RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
									"FALSE")
									&& !(cos != null && PROFILE1
											.equalsIgnoreCase(cos.getCosType()))) {
								task.put(param_info, VIRAL_DATA);
								task.put(param_type, "SELCONFPENDING");
								task.put(param_info + "_CATEGORY_ID",
										task.getString(param_categoryID));
								if (!RBTParametersUtils
										.getParamAsBoolean(
												iRBTConstant.COMMON,
												"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
												"FALSE")) {
									removeData(task);
								}

								addData(task);
							}
							if (pplContentRejectionLogger != null)
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID, getMode(task),
												"-1", category.getCategoryId()
														+ "", new Date());
							return DataUtils.getUnAllowedContentResponse(cos,
									clips);
						}

						int index = 0;
						if (category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE) {
							List<ProvisioningRequests> provReqList = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(
											subscriberID,
											Integer.parseInt(cos.getCosId()));
							if (provReqList != null && provReqList.size() > 0) {
								packRefID = provReqList.get(0).getTransId();
								packExtraInfo = provReqList.get(0)
										.getExtraInfo();
								Map<String, String> extraInfoMap = DBUtility
										.getAttributeMapFromXML(packExtraInfo);
								if (extraInfoMap == null)
									extraInfoMap = new HashMap<String, String>();

								String catID = extraInfoMap
										.get(iRBTConstant.EXTRA_INFO_PACK_CATID);
								if (catID == null
										|| !catID.equals(String
												.valueOf(category
														.getCategoryId()))) {
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_CATID,
											String.valueOf(category
													.getCategoryId()));
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_INDEX,
											"0");
								} else {
									index = Integer
											.parseInt(extraInfoMap
													.get(iRBTConstant.EXTRA_INFO_PACK_INDEX));
									index = (index + 1) % clips.length;
									extraInfoMap.put(
											iRBTConstant.EXTRA_INFO_PACK_INDEX,
											String.valueOf(index));
								}
								packExtraInfo = DBUtility
										.getAttributeXMLFromMap(extraInfoMap);
							}
						}

						clip = clips[index];
					} else if (!task.containsKey(param_cricketPack)) {
						if (task.containsKey(param_profileHours)
								|| selType == iRBTConstant.PROFILE_SEL_TYPE
								|| category.getCategoryTpe() == iRBTConstant.RECORD
								|| category.getCategoryTpe() == iRBTConstant.KARAOKE) {
							String rbtFile = task.getString(param_clipID);
							if (rbtFile.toLowerCase().endsWith(".wav"))
								rbtFile = rbtFile.substring(0,
										rbtFile.length() - 4);

							clipMap.put("CLIP_WAV", rbtFile);
						}
					}

					if (clip != null) {
						if (!DataUtils.isContentAllowed(cos, clip)
								&& !allowPremiumContent) {
							writeEventLog(subscriberID, getMode(task), "404",
									CUSTOMIZATION, getClip(task),
									getCriteria(task));
							if (RBTParametersUtils.getParamAsBoolean(
									iRBTConstant.COMMON,
									"IS_PREMIUM_CONTENT_ALLOWED_FOR_LITE_USER",
									"FALSE")
									&& !(cos != null && PROFILE1
											.equalsIgnoreCase(cos.getCosType()))) {
								task.put(param_info, VIRAL_DATA);
								task.put(param_type, "SELCONFPENDING");
								task.put(param_info + "_CATEGORY_ID",
										task.getString(param_categoryID));
								if (!RBTParametersUtils
										.getParamAsBoolean(
												iRBTConstant.COMMON,
												"IS_MULTIPLE_PREMIUM_CONTENT_PENDING_ALLOWED",
												"FALSE")) {
									removeData(task);
								}

								addData(task);
							}
							if (pplContentRejectionLogger != null)
								pplContentRejectionLogger
										.PPLContentRejectionTransaction(
												subscriberID, getMode(task),
												clip.getClipId() + "", "-1",
												new Date());
							return DataUtils.getUnAllowedContentResponse(cos,
									clip);
						}

						/*
						 * if (clip.getContentType() != null &&
						 * !clip.getContentType
						 * ().equalsIgnoreCase(COS_TYPE_LITE) && cosType != null
						 * && .equalsIgnoreCase(COS_TYPE_LITE)) return
						 * LITE_USER_PREMIUM_BLOCKED;
						 */

						task.put(session_clip, clip);

						clipMap.put("CLIP_CLASS", clip.getClassType());
						clipMap.put("CLIP_END", clip.getClipEndTime());
						clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
						clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
						clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
						clipMap.put("CLIP_NAME", clip.getClipName());

						if (clip.getContentType() != null
								&& clip.getContentType().equalsIgnoreCase(
										CONTENT_TYPE_FEED) && !Utility.isShuffleCategory(category.getCategoryTpe())) {
							// Changed for RBT-1058 (Infotainment RRBT and
							// PreCall)
							// If clip content type is FEED, get CategoryID from
							// configuration.
							// Pass categoryInfo corresponding to that
							// categoryID
							String feedCategoryID = CacheManagerUtil
									.getParametersCacheManager()
									.getParameter(iRBTConstant.COMMON,
											"FEED_CATEGORY_ID", "3").getValue();
							Category feedCategory = rbtCacheManager
									.getCategory(Integer
											.parseInt(feedCategoryID));

							if (feedCategory != null)
								categoriesObj = CategoriesImpl
										.getCategory(feedCategory);
						}
					}

					if (classType == null)
						classType = getChargeClass(task, subscriber, category,
								clip);

					//RBT-12158 Premium content charging priority over cosid
					String tempClassType = Utility.getCosOverrideClass(clip,
							useUIChargeClass, subscriber);
					if (tempClassType != null) {
						classType = tempClassType;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
						incrSelCount = false;
					}
					
					
					// Christmas promotions
					String christmasClassType = checkChristmasPeriod(classType,
							task, subscriber, category, clip);
					if (christmasClassType != null
							&& !christmasClassType.equals(classType)) {
						classType = christmasClassType;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
					}
					
					String contentBaseClasstype = getContentBasedClassType(task, clip);
					if(contentBaseClasstype != null) {
						classType = contentBaseClasstype;
						useUIChargeClass = true;
						task.put(param_useUIChargeClass, YES);
					}

					String chargingPackage = getChargingPackage(task,
							subscriber, category, clip);

					if (task.containsKey(param_inLoop)
							&& task.getString(param_inLoop).equalsIgnoreCase(
									YES))
						inLoop = true;

					transID = task.getString(param_transID);

					int selectionType = 0;
					if (task.containsKey(param_selectionType))
						selectionType = Integer.parseInt(task
								.getString(param_selectionType));

					useUIChargeClass = task.containsKey(param_useUIChargeClass)
							&& task.getString(param_useUIChargeClass)
									.equalsIgnoreCase(YES);

					String selResponse = null;
					boolean isNoConsentRequired = false;
					isNoConsentRequired = isNoConsentRequired(task, subscriber, isSubscriberAlreadyNotDeactive, classType, mode);
//					logger.info("isSubscriberAlreadyNotDeactive:"+isSubscriberAlreadyNotDeactive+ " classType :" + classType + " mode:"+mode + "isNoConsentRequired :" + isNoConsentRequired);
					if(isNoConsentRequired){
						task.put(param_byPassConsent, "true");
					}
					// RBT-10785
					boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
							"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
					if(addProtocolNumber) {
						String wavFile = null;
						if (clip != null) {
							wavFile = clip.getClipRbtWavFile();
						}	
						SubscriberDownloads subscriberDownload = rbtDBManager
								.getActiveSubscriberDownload(subscriberID,
										wavFile);
						if (null == subscriberDownload
								|| (null != subscriberDownload && subscriberDownload
										.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_DEACTIVATED)) {
							selectionInfo = appendProtocolNumber(subscriberID,
									selectionInfo);
						} else {
							logger.warn("Since download already exists, not appending protocol number. subscriberID: "
									+ subscriberID + ", wavFile: " + wavFile);
						}
					
					}
					
					if (task.containsKey(param_isPreConsentBaseSelRequest) && !isNoConsentRequired) {
						if(isBlackListed){
							return response;
						}
						String extraInfoMap = null;
						String requestType = "Selection";
						if (Utility.getSubscriberStatus(subscriber)
								.equalsIgnoreCase(NEW_USER)
								|| Utility.getSubscriberStatus(subscriber)
										.equalsIgnoreCase(DEACTIVE)) {
							requestType = "BaseSelection";
						}
						Clip clipObj = null;
						if (task.containsKey(param_clipID))
							clipObj = rbtCacheManager.getClip(task
									.getString(param_clipID));

						if (clipObj != null)
							task.put(param_promoID, clip.getClipPromoId());

						String consentTransID = UUID.randomUUID().toString();
						
						// idea
						selResponse = rbtDBManager.addSelectionConsent(
								consentTransID, subscriberID, callerID,
								task.getString(param_categoryID),
								task.getString(srvkey), selectedBy, startDate,
								endDate, status, classType,
								task.getString(param_cosID),
								task.getString(param_packCosId),
								task.getString(param_clipID), interval,
								fromTime, toTime, selectionInfo, selectionType,
								inLoop, "selection", useUIChargeClass,
								category.getCategoryTpe(),
								task.getString(param_profileHours), isPrepaid,
								feedSubType, (String) clipMap.get("CLIP_WAV"),
								rbtType, circleID, language, new Date(),
								extraInfoMap, requestType, 0, subscriber, task, categoriesObj, doTODCheck);

						return selResponse;
					}
					// if (getParamAsBoolean(iRBTConstant.COMMON,
					// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
					if (isSupportSMClientModel(task, SELECTION_OFFERTYPE)) {
						logger.info("Support client API. Adding selection"
								+ " for subscriberID: " + subscriberID
								+ ", selectionInfoMap: " + selectionInfo);
						selResponse = smAddSubScriberSelection(
								selectionInfoMap, selOfferID, classType, task,
								selResponse, subscriberID, callerID,
								categoriesObj, startDate, endDate, status,
								selectedBy, selectionInfo, isPrepaid,
								messagePath, fromTime, toTime, useSubManager,
								chargingPackage, subYes, circleID, transID,
								subscriber, interval, baseOfferID, -1,
								changeSubType, inLoop, clipMap, clip, category);
					} else {
						if (task.containsKey(param_offerID)) {
							selectionInfoMap.put(
									iRBTConstant.EXTRA_INFO_OFFER_ID,
									task.getString(param_offerID));
						}

						logger.info("Adding selection for subscriberID: "
								+ subscriberID + ", selectedBy: " + selectedBy
								+ ", classType: " + classType + ", subClass: "
								+ subClass + ", selectionInfoMap: " + selectionInfoMap);

						refId = null;
						if(task.containsKey(param_refID) && !Utility.isModeConfiguredForIdeaConsent(selectedBy)) {
							refId = task.getString(param_refID);
						}
						if (refId == null) {
							refId = UUID.randomUUID().toString();
						}
						//RBT-9873 Added for bypassing CG flow
                        if(task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
                        	clipMap.put("CONSENT_SUBSCRIPTION_INSERT", "true"); 
                        }
                        if(task.containsKey(param_allowPremiumContent)){
                        	clipMap.put(param_allowPremiumContent, "y");
                        }
                        if(task.containsKey(param_isUdsOn)){
                        	clipMap.put(param_isUdsOn, task.getString(param_isUdsOn));
                        }

                        selResponse = rbtDBManager.addSubscriberSelections(
								subscriberID, callerID, categoriesObj, clipMap,
								null, startDate, endDate, status, selectedBy,
								selectionInfo, 0, isPrepaid, changeSubType,
								messagePath, fromTime, toTime, classType,
								useSubManager, doTODCheck, "VUI",
								chargingPackage, subYes, null, circleID,
								incrSelCount, false, transID, false, false,
								inLoop, subClass, subscriber, selectionType,
								interval, selectionInfoMap, useUIChargeClass,
								refId, false);
					}
					
					if (selResponse
							.equals(iRBTConstant.SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS)) {
						writeEventLog(subscriberID, getMode(task), "207",
								PURCHASE, getClip(task));
					} else if (selResponse
							.equals(iRBTConstant.SELECTION_FAILED_SUBSCRIBER_SUSPENDED)) {
						writeEventLog(subscriberID, getMode(task), "204",
								PURCHASE, getClip(task));
						writeEventLog(subscriberID, getMode(task), "402",
								CUSTOMIZATION, getClip(task), getCriteria(task));
					} else {
						writeEventLog(subscriberID, getMode(task), "201",
								PURCHASE, getClip(task));
						writeEventLog(subscriberID, getMode(task), "402",
								CUSTOMIZATION, getClip(task), getCriteria(task));
					}

					// Allow corporate user update as total black list user.
					if (selectionType == 2
							&& subscriber != null
							&& getParamAsBoolean("COMMON",
									"ALLOW_CORP_USER_AS_TOTAL_BLKLIST_USER",
									"FALSE")) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(2037, 0, 1);
						Date viralEndDate = calendar.getTime();
						rbtDBManager.insertViralBlackList(subscriberID, null,
								viralEndDate, "TOTAL");
					}

					response = Utility.getResponseString(selResponse);

					if(clipMap.containsKey("RECENT_CLASS_TYPE")){
                    	task.put("RECENT_CLASS_TYPE", clipMap.remove("RECENT_CLASS_TYPE"));
                    }

					if(clipMap.containsKey(iRBTConstant.param_isSelConsentInserted)){
						task.put(iRBTConstant.param_isSelConsentInserted,
								clipMap.remove(iRBTConstant.param_isSelConsentInserted));
                    }

					if(response.equalsIgnoreCase(SUCCESS)) {
						//For consent
						task.put("CONSENTID",clipMap.remove("CONSENTID"));
						task.put("CONSENTCLASSTYPE", clipMap.remove("CONSENTCLASSTYPE"));
						task.put("CONSENTSUBCLASS", clipMap.remove("CONSENTSUBCLASS"));
						task.put("CONSENT_SERVICE_ID",clipMap.remove("CONSENT_SERVICE_ID"));
						task.put("CONSENT_SERVICE_CLASS",clipMap.remove("CONSENT_SERVICE_CLASS"));
					}
				}

				if (!task.containsKey(param_isPreConsentBaseSelRequest)) {
					if (response.equalsIgnoreCase(SUCCESS)) {

						task.put("CURRENT_REF_ID", refId);//Done for RBT-12247
						HashMap<String, String> xtraInfoMap = rbtDBManager
								.getExtraInfoMap(subscriber);
						if (xtraInfoMap != null && !xtraInfoMap.isEmpty())
							logger.info("Inf map" + xtraInfoMap.toString());

						if (task.containsKey(param_scratchNo)) {
							logger.info("contains scratch no");

							if (xtraInfoMap.containsKey("SCRS")) {
								logger.info("Updating extra info");
								rbtDBManager.updateExtraInfo(subscriberID,
										"SCRS", "1");
								rbtDBManager.updateScratchCard(
										task.getString(param_scratchNo), "1");
							}
						}

						if (category.getCategoryTpe() == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE
								&& packExtraInfo != null) {
							ProvisioningRequestsDao.updateExtraInfo(
									subscriberID, packRefID, packExtraInfo);
						}
						
						//<RBT-10520>
						String ad2cUrl = task.getString(param_ad2cUrl); 
						if (ad2cUrl != null && !ad2cUrl.trim().isEmpty()) {
							logger.info("Parameter " + param_ad2cUrl + " = " +  ad2cUrl + " present in request.");
							RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent();
							rbtCallBackEvent.setClipID(clip.getClipId());
							rbtCallBackEvent.setEventType(RBTCallBackEvent.AD2C_PENDING_CALLBACK);
							rbtCallBackEvent.setMessage(ad2cUrl);
							rbtCallBackEvent.setModuleID(RBTCallBackEvent.MODULE_ID_AD2C);
							rbtCallBackEvent.setSelectedBy(mode);
							rbtCallBackEvent.setSelectionInfo(refId);
							rbtCallBackEvent.setSubscriberID(subscriberID);
							logger.info("Inserting rbtCallBackEvent: " + rbtCallBackEvent);
							rbtCallBackEvent.createCallbackEvent(rbtCallBackEvent);
						}
						//</RBT-10520>
					}

					boolean removeGiftIfAlreadyExists = RBTParametersUtils
							.getParamAsBoolean(iRBTConstant.COMMON,
									"REMOVE_GIFT_IF_SAME_SONG_EXISTS", "true");
					if (response.contains(SUCCESS)
							|| (removeGiftIfAlreadyExists && response
									.equals(ALREADY_EXISTS))) {
						if (action.equalsIgnoreCase(action_acceptGift)) {
							String gifterID = task.getString(param_gifterID);
							Date sentTime = dateFormat.parse(task
									.getString(param_giftSentTime));

							rbtDBManager.updateViralPromotion(gifterID,
									subscriberID, sentTime, "GIFTED",
									"ACCEPT_ACK", new Date(), null, null);
						}
					}
					Parameters corpSelSusAllowed = parametersCacheManager
							.getParameter(iRBTConstant.COMMON,
									"VOL_SUS_NON_CORP", null);
					if (corpSelSusAllowed != null) {
						if (corpSelSusAllowed.getValue() != null
								&& corpSelSusAllowed.getValue()
										.equalsIgnoreCase("true")) {
							String[] corpCatIDsArr = null;
							Parameters corpCatIdsParam = parametersCacheManager
									.getParameter(iRBTConstant.WEBSERVICE,
											"CORP_CAT_ID_LIST", "1");
							if (corpCatIdsParam != null
									&& corpCatIdsParam.getValue() != null)
								corpCatIDsArr = corpCatIdsParam.getValue()
										.trim().split(",");

							ArrayList<String> corpCatIDs = null;
							if (corpCatIDsArr != null
									&& corpCatIDsArr.length > 0) {
								corpCatIDs = new ArrayList<String>();
								for (int count = 0; count < corpCatIDsArr.length; count++) {
									corpCatIDs.add(corpCatIDsArr[count]);
								}
							}
							if (corpCatIDs == null) {
								corpCatIDs = new ArrayList<String>();
								corpCatIDs.add("1");
							}
							if (!corpCatIDs.contains(""
									+ category.getCategoryId())) {
								task.put(param_suspend, "n");
								processSuspension(task);
								task.remove(param_suspend);
							}
						}
					}
					if (response.equalsIgnoreCase(SUCCESS)) {
						isAnyResponseSuccess = true;
					}

					if (response.startsWith(SUCCESS)) {
						sendAcknowledgementSMS(task, "SELECTION");
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Processed selection request, response: " + response
				+ ", subscriberID: " + subscriberID);
		if (isAnyResponseSuccess) {
			return SUCCESS;
		}
		return response;
	}
	
	
	@Override
	public String processActivation(WebServiceContext task) {
		String response = ERROR;
		String subscriberID = null;
		logger.info("Processing activation for task: " + task);
		try {
			subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = DataUtils.getSubscriber(task);
			boolean isLimitedPackRequest = false;

			response = isValidUser(task, subscriber);
			if (!response.equals(VALID)) {
				logger.warn("Unable to process activation, invalid subscriber: "
						+ subscriberID);
				writeEventLog(subscriberID, getMode(task), "104", SUBSCRIPTION,
						null);
				return response;
			}
			
			if(rbtDBManager.isSubscriberDeactivated(subscriber)) {
				logger.info("Updated megaPromo_newuser in request, subscriber is null"
						+ " or deactive. subscriberID: " + subscriberID);
				task.put("megaPromo_newuser","true");
			}

			HashMap<String, String> userInfoMap = getUserInfoMap(task);
			if (task.containsKey(param_scratchNo)) {
				userInfoMap.put("SCRN", task.getString(param_scratchNo));
				userInfoMap.put("SCRS", "2");

			}

			Boolean isCosIdPresentInRequestAndIsAPackCosId = false;
			Boolean isCosIdPresentInRequest = false;
			
			if (task.containsKey(param_subscriptionClass)
					&& overrideSubscriptionClassMap.containsKey(task.getString(param_subscriptionClass))) {
				task.put(param_subscriptionClass, overrideSubscriptionClassMap.get(task.getString(param_subscriptionClass)));
			}
			
			if (task.containsKey(param_cosID)) {
				String cosID = task.getString(param_cosID);
				CosDetails cosDetails = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(cosID);

				if (cosDetails == null) {
					logger.error("COSID: " + cosID + 
							" is not configured in rbt_cos_details. Returning " + COS_NOT_EXISTS);
					return COS_NOT_EXISTS;
				}
				
				isCosIdPresentInRequest = true;
				isCosIdPresentInRequestAndIsAPackCosId = cosDetails != null
						&& cosDetails.getCosType() != null 
						&& (cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.SONG_PACK)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_DOWNLOADS)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE)
						|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT));
				
				if (cosDetails != null
						&& (iRBTConstant.LIMITED_DOWNLOADS
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
								.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.AZAAN
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					isLimitedPackRequest = true;
				}
				
				logger.info("Validating requested cos: " + cosID
						+ ", subscriberID: " + subscriberID
						+ ", isLimitedPackRequest: " + isLimitedPackRequest
						+ ", cosDetails: " + cosDetails);

				/*
				 * If Cos Id contains in the Task, it will be assumed as music
				 * pack and add it to the task with pack Cos id.
				 */
				if (cosDetails != null
						&& cosDetails.getCosType() != null
						&& (cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.SONG_PACK)
								|| cosDetails.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.LIMITED_DOWNLOADS) || cosDetails
								.getCosType().equalsIgnoreCase(
										iRBTConstant.UNLIMITED_DOWNLOADS) || cosDetails
										.getCosType().equalsIgnoreCase(
												iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || cosDetails
												.getCosType().equalsIgnoreCase(
														iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
					task.put(param_packCosId, cosDetails.getCosId());
				}
			}

			// Added by Sandeep... Verifying packCosID is valid or not
			CosDetails cosDetails = null;
			if (task.containsKey(param_packCosId)) {
				String cosID = task.getString(param_packCosId);
				cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
						.getCosDetail(cosID, DataUtils.getUserCircle(task));
				if (cosDetails == null
						|| !cosDetails.getEndDate().after(new Date())) {
					logger.warn("Returning Invalid Pack Cos. pack cos is expired. cos: "
							+ cosID + ", subscriberID: " + subscriberID);
					return INVALID_PACK_COS_ID;
				}
			}

			// Added by Deepak Kumar for UNSUB_DELAY: Only If the request is for
			// UPgradation , then we allow to process in this case
			// Otherwise for all other cases, it should not allow.

			String subXtraInfo = (subscriber != null) ? subscriber.extraInfo()
					: null;
			HashMap<String, String> subXtraInfoMap = DBUtility
					.getAttributeMapFromXML(subXtraInfo);
			boolean isUnsubDelayedTimeConfiguredForSms = CacheManagerUtil
					.getParametersCacheManager().getParameterValue(
							iRBTConstant.SMS,
							"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
							null) != null;
			if (!isUnsubDelayedTimeConfiguredForSms && subXtraInfoMap != null
					&& subXtraInfoMap.containsKey("UNSUB_DELAY") && subscriber.endDate().after(new Date())
					&& !task.containsKey(param_isPreConsentBaseSelRequest)) {
				if (!task.containsKey(param_rentalPack))
					return NOT_ALLOWED;
				boolean isUpgradAllowedForUnsubDelayed = CacheManagerUtil
						.getParametersCacheManager()
						.getParameterValue(iRBTConstant.COMMON,
								"ALLOW_UPGRADATION_FOR_UNSUB_DELAYED_USER",
								"FALSE").equalsIgnoreCase("TRUE");
				boolean isUnsubDelayedTimeConfigured = CacheManagerUtil
						.getParametersCacheManager()
						.getParameterValue(
								iRBTConstant.COMMON,
								"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
								null) != null;

				if (isUnsubDelayedTimeConfigured
						&& !isUpgradAllowedForUnsubDelayed) {
					return NOT_ALLOWED;
				}
			}

			// added by sridhar.sindiri
			// adds "PCA=TRUE" in extraInfo when a RRBT request comes for a
			// subscriber which has already active announcements
			if (RBTDeploymentFinder.isRRBTSystem()
					&& getParamAsBoolean(iRBTConstant.COMMON,
							iRBTConstant.PROCESS_ANNOUNCEMENTS, "FALSE")) {
				SubscriberAnnouncements[] subscriberAnnouncements = rbtDBManager
						.getActiveSubscriberAnnouncemets(task
								.getString(param_subscriberID));
				if (subscriberAnnouncements != null
						&& subscriberAnnouncements.length != 0)
					userInfoMap.put(iRBTConstant.EXTRA_INFO_PCA_FLAG, "TRUE");
			}

			boolean useSubManager = true;

			// Upgrade ADRBT user to a charged subscription class
			if (getParamAsBoolean(iRBTConstant.COMMON,
					"UPGRADE_ADRBT_ON_RBT_ACT", "false")
					&& !task.containsKey(param_isPreConsentBaseSelRequest)) {
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					if (subscriber.rbtType() == 1
							&& subscriber.subscriptionClass() != null
							&& subscriber.subscriptionClass().equalsIgnoreCase(
									getParamAsString(iRBTConstant.COMMON,
											"ADRBT_SUB_CLASS", "ADRBT"))) {
						task.put(
								param_rentalPack,
								getParamAsString(iRBTConstant.COMMON,
										"ADRBT_SUB_CLASS", "ADRBT"));
						task.put(param_rbtType, "1");
					}

				}
			}

			if (task.containsKey(param_rentalPack)) {
				logger.debug("Processing rental pack. subscriberID: "
						+ subscriberID);

				boolean suspendedUsersAllowed = false;
				if (task.containsKey(param_suspendedUsersAllowed)) {
					suspendedUsersAllowed = Boolean.valueOf(task
							.getString(param_suspendedUsersAllowed));
				}

				if (DataUtils.isSubscriberAllowedForUpgradation(subscriber,
						suspendedUsersAllowed)) {
					if (!task.containsKey(param_scratchNo)) {
						if (userInfoMap.containsKey("SCRS"))
							userInfoMap.remove("SCRS");
						if (userInfoMap.containsKey("SCRN"))
							userInfoMap.remove("SCRN");
					}

					String subscriptionClass = task.getString(param_rentalPack);
					String activatedBy = task.getString(param_mode);
					String activationInfo = task.getString(param_modeInfo);
					boolean success = false;
					if (isSupportSMClientModel(task, BASE_OFFERTYPE)) {
						success = smConvertSubscription(task,
								subscriptionClass, subscriberID, subscriber,
								activatedBy, activationInfo,
								isLimitedPackRequest);
					} else {
						int newRbtType = 0;
						if (task.containsKey(param_rbtType))
							newRbtType = Integer.parseInt(task
									.getString(param_rbtType));

						@SuppressWarnings("null")
						int oldRbtType = subscriber.rbtType();

						HashMap<String, String> extraInfoMap = new HashMap<String, String>();
						if (task.containsKey(param_scratchNo)) {
							extraInfoMap.put("SCRN",
									task.getString(param_scratchNo));
							extraInfoMap.put("SCRS", "2");
						}

						if (isLimitedPackRequest) {
							if (task.containsKey(param_mode)) {
								logger.info("Request contains mode and it is limited pack request");
								extraInfoMap.put("PACK_MODE",
										task.getString(param_mode));
								activatedBy = null;
							}
						}

						String cosID = task.getString(param_cosID);
						if (cosID != null) {
							CosDetails cos = CacheManagerUtil
									.getCosDetailsCacheManager().getCosDetail(
											cosID);
							if (cos == null) {
								logger.info("Invalid cosID. Returning response: "
										+ COS_NOT_EXISTS);
								return COS_NOT_EXISTS;
							}
							
							//
							if(task.containsKey(param_cosID)) {
								String songBasedCosId = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "SONG_BASED_COS_ID", null);
								if(songBasedCosId != null) {
									List<String> cosIdsList = Arrays.asList(songBasedCosId.split(","));
									String cosId = task.getString(param_cosID);
									if(cosIdsList.contains(cosId)) {
										SubscriberStatus[] activeSubscriberStatus = rbtDBManager.getAllActiveSubscriberSettings(subscriber.subID());
										boolean hasNoSelection = true;
										if(activeSubscriberStatus != null) {
											for(SubscriberStatus activeSelection : activeSubscriberStatus) {
												if(activeSelection.status() == 1) {
													hasNoSelection = false;
												}
											}
										}
										if(hasNoSelection) {
											return COS_NOT_UPGRADE_USER_NO_SELECTION;
										}
//										extraInfoMap.put(iRBTConstant.UDS_OPTIN,"TRUE");
									}
									
									if(cosIdsList.contains(cosID) && !cosIdsList.contains(cosId)) {
										extraInfoMap.put(iRBTConstant.UDS_OPTIN,"FALSE");
									}
								}
							}
							
							
							extraInfoMap.put(iRBTConstant.EXTRA_INFO_COS_ID,
									cosID);
						} else {
							String cosForSubClass = getParamAsString(
									iRBTConstant.COMMON,
									iRBTConstant.SUBCLASS_COS_MAPPING, null);
							if (cosForSubClass != null) {
								StringTokenizer stkParent = new StringTokenizer(
										cosForSubClass, ";");
								while (stkParent.hasMoreTokens()) {
									StringTokenizer stkChild = new StringTokenizer(
											stkParent.nextToken(), ",");
									{
										if (stkChild.countTokens() == 2) {
											String pack = stkChild.nextToken()
													.trim();
											String mappedCos = stkChild
													.nextToken().trim();
											if (pack.equalsIgnoreCase(subscriptionClass)) {
												CosDetails cosObj = CacheManagerUtil
														.getCosDetailsCacheManager()
														.getCosDetail(
																mappedCos,
																subscriber
																		.circleID());
												if (cosObj != null
														&& cosObj.getCosId() != null) {
													extraInfoMap
															.put(iRBTConstant.EXTRA_INFO_COS_ID,
																	cosObj.getCosId());
												}
												break;
											}
										}
									}
								}
							}
						}

						boolean canUpgradeGraceAndSuspended = false;
						if (task.containsKey(param_upgradeGraceAndSuspended)) {
							canUpgradeGraceAndSuspended = Boolean.valueOf(task
									.getString(param_upgradeGraceAndSuspended));
						}

						logger.info("Checking to upgrade subscriber. "
								+ "Subscriber status: " + subscriber.subYes()
								+ ", canUpgradeGraceAndSuspended: "
								+ canUpgradeGraceAndSuspended);
						if (subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)
								|| canUpgradeGraceAndSuspended) {
							if (oldRbtType != newRbtType) // If AdRbt
															// upgradation
							{
								newRbtType = ((oldRbtType != newRbtType) ? newRbtType
										: oldRbtType);
								extraInfoMap
										.put((newRbtType == 1 ? iRBTConstant.EXTRA_INFO_ADRBT_ACTIVATION
												: iRBTConstant.EXTRA_INFO_ADRBT_DEACTIVATION),
												"TRUE");
							}

							String extraInfo = subscriber.extraInfo();
							HashMap<String, String> subExtraInfo = DBUtility
									.getAttributeMapFromXML(extraInfo);
							//Fix for RBT-12391,RBT-12394
							if (subExtraInfo == null) {
								subExtraInfo = new HashMap<String, String>();
							} else {
								if (subExtraInfo
										.containsKey(Constants.param_SR_ID)) {
									subExtraInfo.remove(Constants.param_SR_ID);
								}
								if (subExtraInfo
										.containsKey(Constants.param_vendor
												.toUpperCase())) {
									subExtraInfo.remove(Constants.param_vendor
											.toUpperCase());
								}
								if (subExtraInfo
										.containsKey(Constants.param_ORIGINATOR)) {
									subExtraInfo
											.remove(Constants.param_ORIGINATOR);
								}
							}
							if (!task.containsKey(param_scratchNo)
									&& extraInfo != null) {
								subExtraInfo.remove("SCRS");
								subExtraInfo.remove("SCRN");
							}
							
							subExtraInfo.remove(iRBTConstant.EXTRA_INFO_TPCGID);
							
                            if (userInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
										userInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID));
							}
                            //RBT-9213
                            if (userInfoMap.containsKey(Constants.param_sdpomtxnid)) {
								extraInfoMap.put(Constants.param_sdpomtxnid,
										userInfoMap.get(Constants.param_sdpomtxnid));
							}
                            if (userInfoMap.containsKey(Constants.param_seapitype)) {
								extraInfoMap.put(Constants.param_seapitype,
										userInfoMap.get(Constants.param_seapitype));
							}
                            //Added extra info column to update the sr_id and originator info 
        					// as per the jira id RBT-11962
                            if (userInfoMap.containsKey(Constants.param_SR_ID)) {
                            	extraInfoMap.put(Constants.param_SR_ID,
            							userInfoMap.get(Constants.param_SR_ID));
            				}
            				if (userInfoMap.containsKey(Constants.param_ORIGINATOR)) {
            					extraInfoMap.put(Constants.param_ORIGINATOR,
            							userInfoMap.get(Constants.param_ORIGINATOR));
            				}
            				if (userInfoMap.containsKey(Constants.param_vendor.toUpperCase())) {
            					extraInfoMap.put(Constants.param_vendor.toUpperCase(),
            							userInfoMap.get(Constants.param_vendor.toUpperCase()));
            				}
                            //end                            
							subExtraInfo.putAll(extraInfoMap);

							if (task.containsKey(param_bIOffer)) {
								subExtraInfo.put("BI_OFFER", "TRUE");
							}
							String oldActBy = subscriber.activatedBy();
							subExtraInfo.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, oldActBy);
							extraInfo = DBUtility
									.getAttributeXMLFromMap(subExtraInfo);

							boolean concatActivationInfo = true;
							
//							ganesh added it
							if(task.containsKey(WebServiceConstants.param_isPreConsentBaseRequest)) {
								// please check these parameters which all
								// required and which need to be ignored.
								String circleID = DataUtils.getUserCircle(task);
								boolean isPrepaid = DataUtils.isUserPrepaid(task);
								String upgradeRefID = UUID.randomUUID().toString();
								success = rbtDBManager
										.convertSubscriptionTypeConsentUpgrde(
												subscriberID, activatedBy,
												null, null, activationInfo,
												isPrepaid, cosID, newRbtType,extraInfo, circleID,
												upgradeRefID, "0", subscriber.subscriptionClass(), subscriptionClass);
								logger.info("Updated status changed, update "
										+ "status: " + success
										+ " for subscriber: " + subscriberID +"subscriptionClass: " +subscriptionClass);
							}else if(!task.containsKey(WebServiceConstants.param_isPreConsentBaseSelRequest)){
								success = rbtDBManager.convertSubscriptionType(
										subscriberID,
										subscriber.subscriptionClass(),
										subscriptionClass, activatedBy,
										activationInfo, concatActivationInfo,
										newRbtType, true, extraInfo, subscriber);
								logger.info("Updated status changed, update "
										+ "status: " + success
										+ " for subscriber: "
										+ subscriberID);
							}
							
						} else {
							// Subscriber is in pending state, so request will
							// be stored in transaction table.
							SubscriptionClass subClass = CacheManagerUtil
									.getSubscriptionClassCacheManager()
									.getSubscriptionClass(subscriptionClass);
							if (subClass == null)
								return INVALID_SUBSCRIPTION_CLASS;

							extraInfoMap.put(ExtraInfoKey.RBT_TYPE.toString(),
									String.valueOf(newRbtType));

							if (task.containsKey(param_bIOffer)) {
								extraInfoMap.put("BI_OFFER", "TRUE");
							}
							
                            if (userInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
								extraInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
										userInfoMap.get(iRBTConstant.EXTRA_INFO_TPCGID));
							}
                            
							String oldActBy = subscriber.activatedBy();
							extraInfoMap.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, oldActBy);

							String extraInfo = DBUtility
									.getAttributeXMLFromMap(extraInfoMap);

							ProvisioningRequests provisioningRequest = new ProvisioningRequests(
									subscriptionClass, new Date(), extraInfo,
									activatedBy, activationInfo, null, 0, null,
									Status.TOBE_PROCESSED.getStatusCode(),
									subscriberID, null,
									Type.BASE_UPGRADATION.getTypeCode());

							provisioningRequest = ProvisioningRequestsDao
									.createProvisioningRequest(provisioningRequest);
							logger.info("Added provisioning request: "
									+ provisioningRequest + " for subscriber: "
									+ subscriberID);
							success = (provisioningRequest != null);
						}
					}
					if (success) {
						response = SUCCESS;
						subscriber = rbtDBManager.getSubscriber(subscriberID);

						// Updated Subscriber object is storing in taskSession &
						// it will be used to build the response element

						boolean isUpgradAllowedForUnsubDelayed = CacheManagerUtil
								.getParametersCacheManager()
								.getParameterValue(
										iRBTConstant.COMMON,
										"ALLOW_UPGRADATION_FOR_UNSUB_DELAYED_USER",
										"FALSE").equalsIgnoreCase("TRUE");
						boolean isUnsubDelayedTimeConfigured = CacheManagerUtil
								.getParametersCacheManager()
								.getParameterValue(
										iRBTConstant.COMMON,
										"CONF_UNSUB_DELAY_TIME_IN_MINUTES_ON_DEACTIVATION",
										null) != null;
						String xtraInfo = subscriber.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility
								.getAttributeMapFromXML(xtraInfo);
						if (xtraInfoMap != null
								&& xtraInfoMap.containsKey("UNSUB_DELAY")
								&& isUpgradAllowedForUnsubDelayed
								&& isUnsubDelayedTimeConfigured) {
							xtraInfoMap.remove("UNSUB_DELAY");
							xtraInfo = DBUtility
									.getAttributeXMLFromMap(xtraInfoMap);
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyyMMdd HH:mm:ss");
							Date defaultEndDate = null;
							try {
								defaultEndDate = sdf.parse("20371231 00:00:00");
							} catch (Exception ex) {
								logger.info("exception in processResubscriptionRequest() while parsing");
							}
							rbtDBManager.updateEndDateAndExtraInfo(
									subscriberID, defaultEndDate, xtraInfo);
							subscriber = rbtDBManager
									.getSubscriber(subscriberID);
						}
						task.put(param_subscriber, subscriber);

					} else {
						response = FAILED;
					}
					logger.info("Processed rental pack, response: " + response);
					return response;
				}
			}

			// Below if block processes the acceptance Gift Service request for
			// already active users.
			// It just updates the Gift(ViralSMSTable) entry.
			// boolean acceptGift =
			// task.getString(param_action).equalsIgnoreCase(action_acceptGift);
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift)) {
				if (subscriber != null
						&& subscriber.subYes().equals(
								iRBTConstant.STATE_ACTIVATED)) {
					String gifterID = task.getString(param_gifterID);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmssSSS");
					Date sentTime = dateFormat.parse(task
							.getString(param_giftSentTime));
					String acceptStatus = "ACCEPT_PRE";

					ViralSMSTable gift = rbtDBManager.getViralPromotion(
							gifterID, subscriberID, sentTime, "GIFTED");
					if (gift.clipID() == null) {
						rbtDBManager.updateViralPromotion(gifterID,
								subscriberID, sentTime, "GIFTED", acceptStatus,
								new Date(), null, null);

						logger.info("Processed gift request, response: "
								+ SUCCESS);
						return SUCCESS;
					}
				}
			}

			String packExtraInfoXml = null;
			boolean isDirectActivation = false;
			if (task.containsKey(param_isDirectActivation)
					&& task.getString(param_isDirectActivation)
							.equalsIgnoreCase(YES))
				isDirectActivation = true;

			String circleID = DataUtils.getUserCircle(task);
			boolean isPrepaid = DataUtils.isUserPrepaid(task);

			CosDetails cos = null;
			boolean isUpgradeDownloadLimitSongPack = RBTParametersUtils.getParamAsBoolean("COMMON","UPGRAGE_DOWNLOAD_LIMIT_SONG_PACK", "FALSE");
			if(task.containsKey(param_cosID)) {				
				CosDetails tempCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_cosID));
				String cosType = (null != tempCos) ? tempCos.getCosType() : null;
				
				if (iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE
						.equalsIgnoreCase(cosType)
						|| (isUpgradeDownloadLimitSongPack && iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT
								.equalsIgnoreCase(cosType))) {
					cos = tempCos;
				}
				logger.info("Since request contains cosID, cosType is validated. Final cos: " + cos
						+ ", subscriberID: " + subscriberID);
			}
//			if(cos == null && task.containsKey(param_cosID) && RBTParametersUtils.getParamAsBoolean("COMMON","UPGRAGE_DOWNLOAD_LIMIT_SONG_PACK", "FALSE")) {
//				Parameters muiscPackCosIdParam = CacheManagerUtil.getParametersCacheManager()
//						.getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");
//				
//				List<String> musicPackCosIdList = null;
//				
//				if(muiscPackCosIdParam != null) {
//					musicPackCosIdList = ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
//					if(musicPackCosIdList.contains(task.getString(param_cosID))) {
//						cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_cosID));
//					}
//				}				 
//			}
			
			if(cos == null) {
				cos = getCos(task, subscriber);
			}
			if (cos != null
					&& cos.getCosType() != null
					&& (cos.getCosType().equalsIgnoreCase(
							iRBTConstant.SONG_PACK)
							|| cos.getCosType().equalsIgnoreCase(iRBTConstant.AZAAN)
							|| cos.getCosType().equalsIgnoreCase(
									iRBTConstant.LIMITED_DOWNLOADS) || cos
							.getCosType().equalsIgnoreCase(
									iRBTConstant.UNLIMITED_DOWNLOADS) || cos
									.getCosType().equalsIgnoreCase(
											iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || cos
											.getCosType().equalsIgnoreCase(
													iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT))) {
				logger.info("Since CosType is: " + cos.getCosType()
						+ ", updated cos: " + cos+", in request");
				cosDetails = cos;
				task.put(param_packCosId, cos.getCosId());
			}

			if (task.containsKey(param_packCosId)) {
				/*
				 * If packOfferId is present in the task, then create and put
				 * offerId into the extra info map and again put extra info map
				 * into the task.
				 */
				String offerId = task.getString(param_packOfferID);
				if (offerId != null) {
					Map<String, String> packExtraInfoMap = new HashMap<String, String>();
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							offerId);
					packExtraInfoXml = DBUtility
							.getAttributeXMLFromMap(packExtraInfoMap);
				}				

				if (packCosIdCosIdMap.containsKey(task.getString(param_packCosId))) {
					if (!isCosIdPresentInRequest || isCosIdPresentInRequestAndIsAPackCosId) {
						String cosId = packCosIdCosIdMap.get(task.getString(param_packCosId));
						cos = CacheManagerUtil
								.getCosDetailsCacheManager()
								.getCosDetail(cosId);

						logger.info("cosId replaced with DB config. New cosId: " + cosId);
					} 
				} else {
					cos = CacheManagerUtil
							.getCosDetailsCacheManager()
							.getDefaultCosDetail(circleID, ((isPrepaid) ? YES : NO));
					logger.info("Since request contains packCosId, fetched cos by circleID: "
							+ circleID + ", cos: " + cos);
				}
			}
			// If user entry is already there in DB, then current status of the
			// user will be returned.
			if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {

				if (task.containsKey(param_isPreConsentBaseSelRequest)) {
					String status = Utility.getSubscriberStatus(subscriber);
					task.put("IS_PREPAID_CONSENT", subscriber.prepaidYes());
					task.put("CIRCLE_ID_CONSENT", subscriber.circleID());
					task.put("SUB_CLASS_CONSENT",
							subscriber.subscriptionClass());
					task.put("RBT_TYPE_CONSENT", subscriber.rbtType());
					logger.warn("Retuning status: " + status + ", subscriber: "
							+ subscriberID + ", is non-deactive and "
							+ "request is pre-consent base selection request");
					return status;
				}

				/*
				 * If user is already active and purchasing first song thru
				 * COPY, then there should be one update query for putting the
				 * below flag in subscriber's extraInfo
				 */
				String subExtraInfo = subscriber.extraInfo();
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(subExtraInfo);
				if (userInfoMap != null
						&& (userInfoMap
								.containsKey(iRBTConstant.FREE_COPY_AVAILED) || userInfoMap
								.containsKey(iRBTConstant.MOBILE_APP_FREE))
						&& (subExtraInfoMap == null
								|| !subExtraInfoMap
										.containsKey(iRBTConstant.FREE_COPY_AVAILED) || !subExtraInfoMap
									.containsKey(iRBTConstant.MOBILE_APP_FREE))) {
					if (subExtraInfoMap == null)
						subExtraInfoMap = new HashMap<String, String>();
					if (userInfoMap.containsKey(iRBTConstant.MOBILE_APP_FREE)) {
						subExtraInfoMap.put(iRBTConstant.MOBILE_APP_FREE,
								userInfoMap.get(iRBTConstant.MOBILE_APP_FREE));
					}
					if (userInfoMap.containsKey(iRBTConstant.FREE_COPY_AVAILED)) {
						subExtraInfoMap
								.put(iRBTConstant.FREE_COPY_AVAILED,
										userInfoMap
												.get(iRBTConstant.FREE_COPY_AVAILED));
					}
					rbtDBManager.updateExtraInfo(subscriberID,
							DBUtility.getAttributeXMLFromMap(subExtraInfoMap));
				}

				String status = Utility.getSubscriberStatus(subscriber);
				// added by Sandeep for profile pack when user not deactivated
				if (task.containsKey(param_packCosId)) {
					String cosID = (String) task.get(param_packCosId);
					logger.info("Checking for proifile pack entry into provisioning_requests table for subscriberID "
							+ subscriberID + ", cosID: " + cosID);
					if (cosDetails != null
							&& cosDetails.getEndDate().after(new Date())) {
						if (rbtDBManager
								.isPackActivated(subscriber, cosDetails)) {
							logger.warn("Pack is already active. subscriberID "
									+ subscriberID + ", cosID: " + cosID);
							return PACK_ALREADY_ACTIVE;
						} else {							
							
							String packDeactPending = isSongPackDeactivationPending(subscriberID, subExtraInfoMap);
							if(packDeactPending != null) {
								return packDeactPending;
							}
							
							int packNumMaxSelection = -1;
							
							if(isDownloadSongPackOverLimitReached(cosID, subscriber.subID(), task)) {
								return OVERLIMIT;
							}
							
							if(task.containsKey("PACK_NUM_MAX_SELECTON")) {
								packNumMaxSelection = Integer.parseInt((String)task.remove("PACK_NUM_MAX_SELECTON"));
							}
							
							HashMap<String, String> existingExtraInfoMap = rbtDBManager
									.getExtraInfoMap(subscriber);
							String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
									.get(iRBTConstant.EXTRA_INFO_PACK) : null;
							String newPack = task.getString(param_packCosId);
							String updatedPacks = (existingPacks != null) ? existingPacks
									+ "," + newPack
									: newPack;
							rbtDBManager.updateExtraInfo(subscriber.subID(),
									iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
							task.remove(param_subscriber);
							task.put(param_subscriber,
									DataUtils.getSubscriber(task));

							String chargingClass = (cosDetails != null) ? cosDetails
									.getSmsKeyword() : null;
							int type = Integer.parseInt(cosID);
							int packStatus = rbtDBManager
									.getPackStatusToInsert(subscriber);
							String mode = getMode(task);
							String modeInfo = getModeInfo(task);
							String transId = UUID.randomUUID().toString();
							ProvisioningRequests provisioningReqs = new ProvisioningRequests(
									subscriber.subID(), type, mode, modeInfo,
									transId, chargingClass, packStatus);
							provisioningReqs.setExtraInfo(packExtraInfoXml);
							if(packNumMaxSelection != -1) {
								provisioningReqs.setNumMaxSelections(packNumMaxSelection);
							}
							if (rbtDBManager
									.insertProvisioningRequestsTable(provisioningReqs) != null) {
								task.put(param_activatedPackNow, YES);
								logger.info("Returning success, inserted into"
										+ " provisioning request. "
										+ "subscriberID " + subscriberID
										+ ", cosID: " + cosID
										+ ", provisioningReqs: "
										+ provisioningReqs);
								return SUCCESS;
							}
							logger.info("Returning status: " + status
									+ " cosID: " + cosID + ", subscriberID "
									+ subscriberID);
							return status;
						}
					} else {
						logger.warn("Returning INVALID_PACK_COS_ID, cosID: "
								+ cosID + ", subscriberID " + subscriberID);
						return INVALID_PACK_COS_ID;
					}

				}

				// If request is Direct Activation then user will be allowed to
				// activate directly
				logger.info("Checking direct activation request or not. isDirectActivation: "
						+ isDirectActivation + ", task: " + task);
				if (!isDirectActivation) {
					logger.warn("Not processing activation, returning " + status
							+ ", request is not direct activation");
					
					String packDeactPending = isSongPackDeactivationPending(subscriberID, subExtraInfoMap);
					if(packDeactPending != null) {
						return packDeactPending;
					}
					
					return status;
				}
			}

			String activatedBy = getMode(task);
			if (task.containsKey(param_actMode)) {
				if (task.getString(param_actMode) != null
						|| !(task.getString(param_actMode).equals(""))) {
					activatedBy = task.getString(param_actMode);
				}
			}
			String activationInfo = getModeInfo(task);

			int activationPeriod = 0;

			int freePeriod = 0;
			if (task.containsKey(param_freePeriod))
				freePeriod = Integer.parseInt(task.getString(param_freePeriod));

			int rbtType = 0;
			if (task.containsKey(param_rbtType))
				rbtType = Integer.parseInt(task.getString(param_rbtType));

			Date startDate = null;

			Date endtDate = null;
			if (task.containsKey(param_subscriptionPeriod)) {
				String subscriptionPeriod = task
						.getString(param_subscriptionPeriod);
				int validityPeriod = Utility
						.getValidityPeriod(subscriptionPeriod);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, validityPeriod - 1);
				endtDate = calendar.getTime();
			}

			// Get the subscription class to activate subscriber 
			String subscriptionClass = getSubscriptionClass(task, cos);

			if ((task.containsKey(param_isPreConsentBaseRequest)
					|| task.containsKey(param_isPreConsentBaseSelRequest))
					&& Arrays.asList(iRBTConstant.COMMON, "ADRBT_SUB_CLASS", "")
							.contains(subscriptionClass)) {
                 rbtType = 1;
			}
			// add code for SRBT
			HashMap<String, String> selectionInfoMap = getSelectionInfoMap(task);
			if (selectionInfoMap.get("ALLOW_PUBLISH") != null) {
				userInfoMap.put("ALLOW_PUBLISH",
						selectionInfoMap.get("ALLOW_PUBLISH"));
			}

			/*
			 * Added by SenthilRaja Logic for activate the subscriber in SM
			 * model Get the offer from UI, activate the subscriber as P, invoke
			 * the SM request If request fails, remove the subscriber.
			 */
			// if(getParamAsBoolean(iRBTConstant.COMMON,
			// iRBTConstant.SUPPORT_SMCLIENT_API, "FALSE"))
			int offerType;
			if (task.containsKey(param_requestFromSelection)) {
				offerType = COMBO_SUB_OFFERTYPE;
			} else {
				offerType = BASE_OFFERTYPE;
			}

			// for putting retailer id in extra info :
			if (task.containsKey(param_retailerID)) {
				userInfoMap.put("RET", task.getString(param_retailerID));
			}

			if (Arrays.asList(
					RBTParametersUtils.getParamAsString("COMMON",
							"PRE_PROMPT_SUPPORTED_SUB_CLASSES", "").split(","))
					.contains(subscriptionClass)) {
				userInfoMap.put(iRBTConstant.EXTRA_INFO_INTRO_PROMPT_FLAG,
						iRBTConstant.ENABLE_PRESS_STAR_INTRO);
				userInfoMap.put(iRBTConstant.EXTRA_INFO_SYSTEM_INIT_PROMPT,
						iRBTConstant.YES);
			}else if(RBTParametersUtils.getParamAsString("COMMON",
							"SUB_CLASSES_AND_PRE_RBT_WAV_MAPPING", null)!=null){
				String preRbtWavFile = getPreRBTWavFileFromSubClassConfig(subscriptionClass);
				userInfoMap.put(iRBTConstant.EXTRA_INFO_PRE_RBT_WAV, preRbtWavFile);
			}
			
			
			//Get Offer for Gift Tef-Spain new RBT re-pricing
			if (task.getString(param_action) != null && task.getString(param_action)
					.equalsIgnoreCase(action_acceptGift) &&((!task.containsKey(param_requestFromSelection)
							&& !task.containsKey(param_offerID)) || (task.containsKey(param_requestFromSelection)
									&& !task.containsKey(param_subscriptionOfferID)))) {
				
				boolean allowBaseOffer = RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_GET_OFFER, "FALSE") || RBTParametersUtils.getParamAsBoolean("GIFT",iRBTConstant.ALLOW_ONLY_BASE_OFFER, "FALSE");
				if(allowBaseOffer) {
					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, activatedBy, 
							Offer.OFFER_TYPE_SUBSCRIPTION, null, cos.getSmsKeyword(), null); 
					if(offer != null && offer.length > 0) {
						subscriptionClass = offer[0].getSrvKey();
						
						if(task.containsKey(param_requestFromSelection) ){
							task.put(param_subscriptionOfferID, offer[0].getOfferID());
						}
						else {
							task.put(param_offerID, offer[0].getOfferID());
						}		
						
						userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offer[0].getOfferID());
					}
				}
			}
			
			if (RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
					"FALSE") || RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER,
							"FALSE")) {
				
				
				if(RBTParametersUtils.getParamAsBoolean("COMMON", iRBTConstant.ALLOW_BASE_OFFER_DU,
						"FALSE") && (!task.containsKey(param_requestFromSelection)
							&& !task.containsKey(param_offerID)) || (task.containsKey(param_requestFromSelection)
									&& !task.containsKey(param_subscriptionOfferID))) {
					
					RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
					rbtDetailsRequest.setMode(activatedBy);
					rbtDetailsRequest.setOfferType(Offer.OFFER_TYPE_SUBSCRIPTION_STR);
					Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
					
//					com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriberID, activatedBy, 
//							Offer.OFFER_TYPE_SUBSCRIPTION, null, cos.getSmsKeyword(), null); 
					if(offers != null && offers.length > 0) {
						subscriptionClass = offers[0].getSrvKey();
						
						if(task.containsKey(param_requestFromSelection) ){
							task.put(param_subscriptionOfferID, offers[0].getOfferID());
						}
						else {
							task.put(param_offerID, offers[0].getOfferID());
						}		
						
						userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offers[0].getOfferID());
					}					
				}
				
				String offerId = task.getString(param_subscriptionOfferID);
				if(offerId == null){
					offerId = task.getString(param_offerID);
				}
				logger.info("Going for Subscription Offer....");
				logger.info("SubscriberID = " + subscriberID + " , Mode = "
						+ task.getString(param_mode) + " , OFFER_ID = " + offerId
						+ " , Offer Type = 2(Subscription) , SubscriptionClass = " + subscriptionClass);

				String offerIdForBlacklisted = RBTParametersUtils.getParamAsString("COMMON",
						"BASE_OFFER_ID_FOR_BLACKLISTED_SUBSCRIBER", "");
				if (Arrays.asList(offerIdForBlacklisted.split(",")).contains(offerId)) {
					String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
							"ACK_SMS_SENDER_NO", null);
					task.put(param_senderID, senderID);
					task.put(param_receiverID, subscriberID);
					String smsText = CacheManagerUtil.getSmsTextCacheManager().getSmsText("OFFER",
							"BLACKLISTED_OFFER_BASE_TEXT", null);
					task.put(param_smsText, smsText);
					sendSMS(task);
					return SUCCESS;
				}
                 if(offerId!=null && !offerId.equalsIgnoreCase("-1")){
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID, offerId);
				}
			}

			if (task.containsKey(param_isPreConsentBaseRequest)
					|| task.containsKey(param_isPreConsentBaseSelRequest)) {
				String consentTransID = UUID.randomUUID().toString();
				// idea
				if (cos != null)
					task.put("SUB_COS_CONSENT", cos);
				task.put("IS_PREPAID_CONSENT", isPrepaid);
				task.put("CIRCLE_ID_CONSENT", circleID);
				task.put("SUB_CLASS_CONSENT", subscriptionClass);
				task.put("RBT_TYPE_CONSENT", rbtType);
				response = rbtDBManager.insertPreConsentSubscriptionRequest(
						subscriberID, activatedBy, startDate, endtDate,
						isPrepaid, activationPeriod, freePeriod,
						activationInfo, subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, userInfoMap, circleID,
						consentTransID, task);
				if (task.containsKey(param_isPreConsentBaseSelRequest) && !response.equalsIgnoreCase(SUCCESS)) {
					response = preConsentBaseSelSuccess;
				}
				if (response.equalsIgnoreCase(SUCCESS))
					task.put(param_transID, consentTransID);

				task.put(param_response, response);
				return response;
			}
			if (isSupportSMClientModel(task, offerType)) {
				subscriber = smActivateSubscriber(task, userInfoMap,
						subscriber, subscriberID, activatedBy, startDate,
						endtDate, isPrepaid, activationPeriod, freePeriod,
						activationInfo, subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, circleID);
			} else {
				if (!task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_offerID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_offerID));
				else if (task.containsKey(param_requestFromSelection)
						&& task.containsKey(param_subscriptionOfferID))
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_ID,
							task.getString(param_subscriptionOfferID));
				
				if(task.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
					userInfoMap.put(iRBTConstant.EXTRA_INFO_TPCGID,
							task.getString(iRBTConstant.EXTRA_INFO_TPCGID));
				}
				boolean isComboReq = false;
                if(task.containsKey(IS_COMBO_REQUEST)){
                	isComboReq = true;
                }
				// Christmas promotions
				subscriptionClass = checkChristmasPeriod(subscriptionClass,
						task, null, null, null);
				logger.debug("Processing subscriber activation. subscriberId: "
						+ subscriberID + ", subscrptionclass: "
						+ subscriptionClass + ", cos: " + cos
						+ ", userInfoMap: " + userInfoMap);
				
				String refId = task.getString(param_linkedRefId);
				if(refId == null) {
					refId = task.getString(param_refID);
				}
				
				//RBT-9873 Added xtraParametersMap for CG flow
				Map<String,String> xtraParametersMap = new HashMap<String,String>();
				
				// RBT-10785
				boolean addProtocolNumber = RBTParametersUtils.getParamAsBoolean(
						"WEBSERVICE", "ADD_PROTOCOL_NUMBER", "FALSE");
				if(addProtocolNumber) {
					activationInfo = appendProtocolNumber(subscriberID, activationInfo);
				}
				if(task.containsKey(param_isUdsOn)){
					xtraParametersMap.put(param_isUdsOn, task.getString(param_isUdsOn)); 
				}
				// Added extra info column to update the sr_id and originator
				// info
				// as per the jira id RBT-11962
				final String strSR_ID = param_selectionInfo + "_"
						+ Constants.param_SR_ID;
				final String strORIGINATOR = param_selectionInfo + "_"
						+ Constants.param_ORIGINATOR;
				final String strVENDOR = param_selectionInfo + "_"
						+ Constants.param_vendor.toUpperCase();
				if (task.containsKey(strSR_ID)) {
					userInfoMap.put(Constants.param_SR_ID,
							task.getString(strSR_ID));
				}
				if (task.containsKey(strORIGINATOR)) {
					userInfoMap.put(Constants.param_ORIGINATOR,
							task.getString(strORIGINATOR));
				}
				if (task.containsKey(strVENDOR)) {
					userInfoMap.put(Constants.param_vendor.toUpperCase(),
							task.getString(strVENDOR));
				}
				// end 
				subscriber = rbtDBManager.activateSubscriber(subscriberID,
						activatedBy, startDate, endtDate, isPrepaid,
						activationPeriod, freePeriod, activationInfo,
						subscriptionClass, useSubManager, cos,
						isDirectActivation, rbtType, userInfoMap, circleID,
						refId, isComboReq, xtraParametersMap);
				
				if(xtraParametersMap.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
					response= SUCCESS;
					task.put("CONSENT_SUBSCRIPTION_INSERT", "true");
					task.put(param_activatedNow, YES);
					logger.info("Subscription Consent Record Inserted");
				}

				boolean is121TnbEnabled = Boolean.parseBoolean(RBTParametersUtils.getParamAsString("COMMON","121_TNB_SUBSCRIPTION_CLASS_ENABLED", "FALSE"));
				if (rbtDBManager.isTnbReminderEnabled(subscriber) && !is121TnbEnabled)
					rbtDBManager.insertTNBSubscriber(subscriber.subID(),
							subscriber.circleID(),
							subscriber.subscriptionClass(),
							subscriber.startDate(), 0);
			}

			// Activated Subscriber object is storing in taskSession & it will
			// be used to build the response element
			task.put(param_subscriber, subscriber);

			if (!task.containsKey(param_isPreConsentBaseRequest)
					&& !task.containsKey(param_isPreConsentBaseSelRequest) && !task.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {

				if (!rbtDBManager.isSubscriberDeactivated(subscriber)) {
					response = SUCCESS;
					task.put(param_activatedNow, YES);

					String language = task.getString(param_language);
					if (language != null
							&& (subscriber.language() == null || !subscriber
									.language().equalsIgnoreCase(language))) {
						rbtDBManager.setSubscriberLanguage(subscriberID,
								language);
						subscriber.setLanguage(language);
					}

					if (task.getString(param_action).equalsIgnoreCase(
							action_acceptGift)) {
						String gifterID = task.getString(param_gifterID);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmmssSSS");
						Date sentTime = dateFormat.parse(task
								.getString(param_giftSentTime));
						String acceptStatus = "ACCEPT_ACK";

						ViralSMSTable gift = rbtDBManager.getViralPromotion(
								gifterID, subscriberID, sentTime, "GIFTED");

						// Clip ID null means Gift Service Request & only in
						// case of
						// Gift Service request gift entry will be updated
						if (gift.clipID() == null)
							rbtDBManager.updateViralPromotion(gifterID,
									subscriberID, sentTime, "GIFTED",
									acceptStatus, new Date(), null, null);
					}

					// added by Sandeep for profile Pack
					if (task.containsKey(param_packCosId) && cosDetails != null) {

						HashMap<String, String> existingExtraInfoMap = rbtDBManager
								.getExtraInfoMap(subscriber);
						String existingPacks = (existingExtraInfoMap != null) ? existingExtraInfoMap
								.get(iRBTConstant.EXTRA_INFO_PACK) : null;
						String newPack = task.getString(param_packCosId);
						String updatedPacks = (existingPacks != null) ? existingPacks
								+ "," + newPack
								: newPack;
						rbtDBManager.updateExtraInfo(subscriber.subID(),
								iRBTConstant.EXTRA_INFO_PACK, updatedPacks);
						task.remove(param_subscriber);
						task.put(param_subscriber,
								DataUtils.getSubscriber(task));

						String cosID = (String) task.get(param_packCosId);
						int type = Integer.parseInt(cosID);
						logger.info("Request contains packCosId, making entry into "
								+ "provisioning_requests table. cosID: "
								+ cosID + ", subscriberID: " + subscriberID);
						String chargingClass = cosDetails.getSmsKeyword();
						int packStatus = rbtDBManager
								.getPackStatusToInsert(subscriber);
						String mode = getMode(task);
						String modeInfo = getModeInfo(task);
						String transId = UUID.randomUUID().toString();
						ProvisioningRequests provisioningReqs = new ProvisioningRequests(
								subscriber.subID(), type, mode, modeInfo,
								transId, chargingClass, packStatus);
						provisioningReqs.setExtraInfo(packExtraInfoXml);
						ProvisioningRequests provisioningRequests = rbtDBManager
								.insertProvisioningRequestsTable(provisioningReqs);
						logger.info("Inserted pack request into provisioning"
								+ " requests table. subscriberId: "
								+ subscriberID + ", requestId: "
								+ provisioningRequests.getRequestId());
					}
					
					//Tef-Spain yavoy new RBT service re-pricing RBT-11113 : to remove expired donwloads with status STATE_DOWNLOAD_SEL_TRACK(t).
					rbtDBManager.removeDeactivateSubscriberDownloads(subscriber.subID());
					
				} else
					response = FAILED;

				if (response.equals(SUCCESS)) {
					sendAcknowledgementSMS(task, "ACTIVATION");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("Processed subscriber activation, response: " + response
				+ ", subscriberId: " + subscriberID);
		return response;
	}


}
