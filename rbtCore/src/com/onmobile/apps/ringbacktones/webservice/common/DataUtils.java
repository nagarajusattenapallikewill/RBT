/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.common;

import static com.onmobile.apps.ringbacktones.common.iRBTConstant.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.RDCGroupMembers;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ChargeClassCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.client.HttpConnector;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BulkSelectionRequest;

/**
 * @author vinayasimha.patil
 * 
 */
public class DataUtils implements WebServiceConstants {
	private static Logger logger = Logger.getLogger(DataUtils.class);

	private static List<String> overrideChargeClassList = null;
	private static Map<String, String> dailyShuffleModeChargeClassMap = null;
	private static Map<String, String> adRbtModeChargeClassesMap = null;
	private static Map<Integer, String> adRbtCategoryChargeClassMap = null;
	private static Map<String, ArrayList<String>> cosTypeContentTypeMap = null;
	private static List<String> udsSelectionModeList = null;
	
	static {
		String overrideChargeClasses = RBTParametersUtils.getParamAsString(
				"COMMON", "OVERRIDE_CHARGE_CLASSES", "");
		overrideChargeClassList = Arrays.asList(overrideChargeClasses
				.toUpperCase().split(","));

		dailyShuffleModeChargeClassMap = new HashMap<String, String>();
		String dailyShuffleModeChargeClasses = RBTParametersUtils
				.getParamAsString("COMMON", "MODE_CHRG_CLASS", "");
		String[] modeAndChargeClasses = dailyShuffleModeChargeClasses
				.split(";");
		for (String modeAndChargeClass : modeAndChargeClasses) {
			String[] values = modeAndChargeClass.split(",");
			if (values.length > 1)
				dailyShuffleModeChargeClassMap.put(values[0], values[1]);
		}

		adRbtModeChargeClassesMap = new HashMap<String, String>();
		String adRbtModeChargeClasses = RBTParametersUtils.getParamAsString(
				"COMMON", "SELECTION_MODE_ADRBT_CLASS_TYPE", "");
		modeAndChargeClasses = adRbtModeChargeClasses.split(";");
		for (String modeAndChargeClass : modeAndChargeClasses) {
			String[] values = modeAndChargeClass.split(",");
			if (values.length > 1)
				adRbtModeChargeClassesMap.put(values[0], values[1]);
		}

		adRbtCategoryChargeClassMap = new HashMap<Integer, String>();
		String adRbtCategoryChargeClasses = RBTParametersUtils
				.getParamAsString("COMMON", "CATEGORY_ID_ADRBT_CLASS_TYPE", "");
		modeAndChargeClasses = adRbtCategoryChargeClasses.split(";");
		for (String modeAndChargeClass : modeAndChargeClasses) {
			String[] values = modeAndChargeClass.split(",");
			if (values.length > 1)
				adRbtCategoryChargeClassMap.put(new Integer(values[0]),
						values[1]);
		}

		String udsSelectionModes = RBTParametersUtils.getParamAsString(
				"COMMON", "UDS_SELECTION_MODES", "");
		udsSelectionModeList = Arrays.asList(udsSelectionModes.split(","));
	
		String liteContentTypesStr = RBTParametersUtils.getParamAsString("COMMON", "COS_TYPE_AND_CONTENT_TYPES_FOR_CONTENT_TYPE_BASED_COS_ACTIVATION",null);
		if(liteContentTypesStr != null)
		{
			cosTypeContentTypeMap = new HashMap<String, ArrayList<String>>();
			StringTokenizer stkParent = new StringTokenizer(liteContentTypesStr, ";");
			while(stkParent.hasMoreTokens())
			{
				StringTokenizer stkChild = new StringTokenizer(stkParent.nextToken(), ":");
				if(stkChild.countTokens() != 2)
					continue;
				String cosType = stkChild.nextToken();
				String contentTypes = stkChild.nextToken();
				StringTokenizer stkGrandChild = new StringTokenizer(contentTypes, ",");
				ArrayList<String> contentTypesList = new ArrayList<String>();
				while(stkGrandChild.hasMoreTokens())
				{
					contentTypesList.add(stkGrandChild.nextToken());
				}
				cosTypeContentTypeMap.put(cosType, contentTypesList);
			}
			logger.info("cosTypeContentTypeMap="+cosTypeContentTypeMap);
		}
	}

	public static SubscriberDetail getSubscriberDetail(WebServiceContext task) {
		SubscriberDetail subscriberDetail = null;
		if (task.containsKey(param_subscriberDetail))
			subscriberDetail = (SubscriberDetail) task
					.get(param_subscriberDetail);
		else {
			String subscriberID = task.getString(param_subscriberID);
			String mode = task.getString(param_mode);
			subscriberDetail = RbtServicesMgr
					.getSubscriberDetail(new MNPContext(subscriberID, mode));
			task.put(param_subscriberDetail, subscriberDetail);
		}

		return subscriberDetail;
	}

	public static String getUserCircle(WebServiceContext task) {
		String circleID = null;
		Subscriber subscriber = (Subscriber) task.get(param_subscriber);

		if (task.containsKey(param_circleID))
			circleID = task.getString(param_circleID);
		else if (subscriber != null
				&& !RBTDBManager.getInstance().isSubscriberDeactivated(
						subscriber))
			circleID = subscriber.circleID();
		else {
			SubscriberDetail subscriberDetail = getSubscriberDetail(task);
			if (subscriberDetail != null)
				circleID = subscriberDetail.getCircleID();
		}

		// User Circle ID is added to taskSession, so that no need query the
		// DB for getting Circle ID in preparing other parts of the XML
		task.put(param_circleID, circleID);

		logger.info("Returning circleID: " + circleID + ", subscriberID: "
				+ subscriber);
		return circleID;
	}

	public static boolean isUserPrepaid(WebServiceContext task) {
		boolean isPrepaid = false;
		Subscriber subscriber = (Subscriber) task.get(param_subscriber);

		if (task.containsKey(param_isPrepaid))
			isPrepaid = task.getString(param_isPrepaid).equalsIgnoreCase(YES);
		else if (subscriber != null)
			isPrepaid = subscriber.prepaidYes();
		else {
			SubscriberDetail subscriberDetail = getSubscriberDetail(task);
			if (subscriberDetail != null)
				isPrepaid = subscriberDetail.isPrepaid();
		}

		// User type is added to task, so that no need query the
		// subscriber object for getting user type in preparing other parts of
		// the XML
		task.put(param_isPrepaid, isPrepaid ? YES : NO);

		return isPrepaid;
	}

	public static SubscriberStatus[] getRecentActiveSettings(
			RBTDBManager rbtDBManager, SubscriberStatus[] settings,
			String mode, String visibleStatusTypes) {
		if (settings == null || settings.length == 0)
			return null;

		List<String> statusList = Arrays.asList(RBTParametersUtils
				.getParamAsString("COMMON",
						"ALLOW_SELECTIONS_WITH_SAME_STATUS", "").split(","));
		Map<String, List<SubscriberStatus>> hashMap = new LinkedHashMap<String, List<SubscriberStatus>>();
        Set<String> provRefIdSet = new HashSet<String>();

		for (SubscriberStatus setting : settings) {
			String callerID = setting.callerID();
			int status = setting.status();
			int fromTime = setting.fromTime();
			int toTime = setting.toTime();
			char loopStatus = setting.loopStatus();
			String selInterval = setting.selInterval();
            String extraInfo = setting.extraInfo();
            if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
					"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")
					&& extraInfo != null
					&& extraInfo.indexOf("PROV_REF_ID") != -1) {
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				String provRefId = extraInfoMap.get("PROV_REF_ID");
				if (provRefId != null) {
					if (provRefIdSet.contains(provRefId)) {
						continue;
					} else {
						provRefIdSet.add(provRefId);
					}
				}
			}
            
            String key = callerID + "_" + status + "_" + fromTime + "_"
            		+ toTime + "_" + selInterval;
			if (((loopStatus == iRBTConstant.LOOP_STATUS_OVERRIDE_INIT
					|| loopStatus == iRBTConstant.LOOP_STATUS_OVERRIDE || loopStatus == iRBTConstant.LOOP_STATUS_OVERRIDE_FINAL) && !statusList
					.contains(status + ""))
					|| !hashMap.containsKey(key)) {
				List<SubscriberStatus> list = new ArrayList<SubscriberStatus>();
				list.add(setting);
				hashMap.remove(key);
				hashMap.put(key, list);
			} else {
				List<SubscriberStatus> list = hashMap.get(key);
				list.add(setting);
			}
		}

		List<SubscriberStatus> list = new ArrayList<SubscriberStatus>();

		Set<String> keySet = hashMap.keySet();
		for (String key : keySet) {
			List<SubscriberStatus> tempList = hashMap.get(key);
			list.addAll(tempList);
		}

		if (visibleStatusTypes != null) {
			if (!visibleStatusTypes.equalsIgnoreCase(ALL)) {
				List<String> visibleStatusList = Arrays
						.asList(visibleStatusTypes.split(","));
				for (Iterator<SubscriberStatus> iterator = list.iterator(); iterator
						.hasNext();) {
					SubscriberStatus subscriberStatus = iterator.next();
					String statusStr = String
							.valueOf(subscriberStatus.status());
					if (!visibleStatusList.contains(statusStr))
						iterator.remove();
				}
			}
		} else {
			Parameters invisibleStatusTypeParam = CacheManagerUtil
					.getParametersCacheManager().getParameter(mode,
							"INVISIBLE_STATUS_TYPES", "0,90,99");
			List<String> invisibleStatusList = Arrays
					.asList(invisibleStatusTypeParam.getValue().trim().split(
							","));

			for (Iterator<SubscriberStatus> iterator = list.iterator(); iterator
					.hasNext();) {
				SubscriberStatus subscriberStatus = iterator.next();
				String statusStr = String.valueOf(subscriberStatus.status());
				if (invisibleStatusList.contains(statusStr))
					iterator.remove();
			}
		}

		logger.info("Retuning active settings: " + list + ", size: "
				+ list.size());
		return (list.toArray(new SubscriberStatus[0]));
	}

	public static SubscriberStatus[] getFilteredSettingsHistory(
			WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Set<String> provRefIdSet = new HashSet<String>();
		String clipName = task.getString(param_clipName);
		Date startDate = null;
		Date endDate = null;
		try {
			if (task.containsKey(param_startDate))
				startDate = dateFormat.parse(task.getString(param_startDate));
			if (task.containsKey(param_endDate))
				endDate = dateFormat.parse(task.getString(param_endDate));
		} catch (ParseException e) {
			logger.error("", e);
		}

		SubscriberStatus[] settings = RBTDBManager.getInstance()
				.getSubscriberRecords(subscriberID);
		if (settings != null) {
			List<SubscriberStatus> list = new ArrayList<SubscriberStatus>();

			RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
			boolean displayActiveSettings = RBTParametersUtils
					.getParamAsBoolean(iRBTConstant.COMMON,
							"DISPLAY_ACTIVE_SETTINGS_IN_LIBRARY_HISTORY",
							"FALSE");
			for (SubscriberStatus setting : settings) {
				Date setTime = setting.setTime();
				if (!displayActiveSettings
						|| !(RBTDBManager.getInstance().isSelectionActivated(
								setting)
								|| RBTDBManager.getInstance()
										.isSelectionActivationPending(setting) || RBTDBManager
								.getInstance().isSelectionGrace(setting))) {
					if (endDate != null && setTime.after(endDate))
						continue;
					if (startDate != null && setTime.before(startDate))
						continue;
				}

				if (clipName != null) {
					String browsingLanguage = task
							.getString(param_browsingLanguage);
					Clip clip = rbtCacheManager.getClipByRbtWavFileName(setting
							.subscriberFile(), browsingLanguage);
					if (clip != null
							&& !clip.getClipName().toLowerCase().contains(
									clipName.toLowerCase()))
						continue;
				}
				 String extraInfo = setting.extraInfo();
				 if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
							"ENABLE_ODA_PACK_PLAYLIST_FEATURE", "FALSE")
							&& extraInfo != null
							&& extraInfo.indexOf("PROV_REF_ID") != -1) {
						HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
						String provRefId = extraInfoMap.get("PROV_REF_ID");
						if (provRefId != null) {
							if (provRefIdSet.contains(provRefId)) {
								continue;
							} else {
								provRefIdSet.add(provRefId);
							}
						}
					}

				list.add(setting);
			}

			if (list.size() > 0)
				settings = list.toArray(new SubscriberStatus[0]);
			else
				settings = null;
		}

		return settings;
	}

	public static SubscriberDownloads[] getFilteredDownloadHistory(
			WebServiceContext task) {
		String subscriberID = task.getString(param_subscriberID);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		String clipName = task.getString(param_clipName);
		Date startDate = null;
		Date endDate = null;
		try {
			if (task.containsKey(param_startDate))
				startDate = dateFormat.parse(task.getString(param_startDate));
			if (task.containsKey(param_endDate))
				endDate = dateFormat.parse(task.getString(param_endDate));
		} catch (ParseException e) {
			logger.error("", e);
		}

		boolean displayActiveSettings = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON,
				"DISPLAY_ACTIVE_SETTINGS_IN_LIBRARY_HISTORY", "FALSE");
		SubscriberDownloads[] subscriberDownloads = RBTDBManager.getInstance()
				.getSubscriberDownloads(subscriberID);
		if (subscriberDownloads != null) {
			List<SubscriberDownloads> list = new ArrayList<SubscriberDownloads>();

			RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
			for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
				Date setTime = subscriberDownload.setTime();
				if (!displayActiveSettings
						|| !(RBTDBManager.getInstance().isDownloadActivated(
								subscriberDownload)
								|| RBTDBManager.getInstance()
										.isDownloadActivationPending(
												subscriberDownload) || RBTDBManager
								.getInstance().isDownloadGrace(
										subscriberDownload))) {
					if (endDate != null && setTime.after(endDate))
						continue;
					if (startDate != null && setTime.before(startDate))
						continue;
				}

				if (clipName != null) {
					String browsingLanguage = task
							.getString(param_browsingLanguage);
					Clip clip = rbtCacheManager.getClipByRbtWavFileName(
							subscriberDownload.promoId(), browsingLanguage);
					if (clip != null
							&& !clip.getClipName().toLowerCase().contains(
									clipName.toLowerCase()))
						continue;
				}

				list.add(subscriberDownload);
			}

			if (list.size() > 0)
				subscriberDownloads = list.toArray(new SubscriberDownloads[0]);
			else
				subscriberDownloads = null;
		}

		return subscriberDownloads;
	}

	public static HashMap<String, GroupMembers[]> getGroupMembersByGroupID(
			Groups[] groups) {
		HashMap<String, GroupMembers[]> groupMemberMap = new HashMap<String, GroupMembers[]>();

		if (groups != null) {
			for (Groups group : groups) {
				String groupID = "G" + group.groupID();
				GroupMembers[] groupMembers = RBTDBManager.getInstance()
						.getMembersForGroupID(group.groupID());
				if (groupMembers != null && groupMembers.length > 0)
					groupMemberMap.put(groupID, groupMembers);
			}
		}

		return groupMemberMap;
	}
	
	public static HashMap<String, RDCGroupMembers[]> getAffiliateGroupMembersByGroupID(
			RDCGroups[] groups) {
		HashMap<String, RDCGroupMembers[]> groupMemberMap = new HashMap<String, RDCGroupMembers[]>();

		if (groups != null) {
			for (RDCGroups group : groups) {
				String groupID = "G" + group.groupID();
				RDCGroupMembers[] groupMembers = RBTDBManager.getInstance()
						.getAffilateMembersForGroupID(group.groupID());
				if (groupMembers != null && groupMembers.length > 0)
					groupMemberMap.put(groupID, groupMembers);
			}
		}

		return groupMemberMap;
	}

	public static PickOfTheDay[] getPickOfTheDays(WebServiceContext task,
			String circleID, char isPrepaid) {
		PickOfTheDay[] pickOfTheDays = null;
		if (task.containsKey(param_pickOfTheDays))
			pickOfTheDays = (PickOfTheDay[]) task.get(param_pickOfTheDays);
		else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			String playDate = dateFormat.format(new Date());
			pickOfTheDays = RBTDBManager.getInstance().getAllPickOfTheDays(
					circleID, isPrepaid, playDate);
			task.put(param_pickOfTheDays, pickOfTheDays);
		}

		return pickOfTheDays;
	}

	public static PickOfTheDay[] getPickOfTheDay(WebServiceContext task,
			String circleID, char isPrepaid, String profile) {
		PickOfTheDay[] pickOfTheDays = getPickOfTheDays(task, circleID,
				isPrepaid);
		List<PickOfTheDay> pickOfTheDaysList = new ArrayList<PickOfTheDay>();

		if (pickOfTheDays != null) {
			// Checking for circle specific
			for (PickOfTheDay pickOfTheDay : pickOfTheDays) {
				if (circleID.equalsIgnoreCase(pickOfTheDay.circleID())
						&& (profile == null && pickOfTheDay.profile() == null)
						|| (profile != null && profile
								.equalsIgnoreCase(pickOfTheDay.profile())))
					pickOfTheDaysList.add(pickOfTheDay);
			}

			if (pickOfTheDaysList.size() > 0)
				return pickOfTheDaysList.toArray(new PickOfTheDay[0]);

			// Checking for 'ALL' circle
			for (PickOfTheDay pickOfTheDay : pickOfTheDays) {
				if (pickOfTheDay.circleID().equalsIgnoreCase(ALL)
						&& (profile == null && pickOfTheDay.profile() == null)
						|| (profile != null && profile
								.equalsIgnoreCase(pickOfTheDay.profile())))
					pickOfTheDaysList.add(pickOfTheDay);
			}
		}

		return pickOfTheDaysList.toArray(new PickOfTheDay[0]);
	}

	public static ChargeClass getValidChargeClass(String parentClassType,
			String childClassType) {
		ChargeClass chargeClass = null;

		ChargeClassCacheManager chargeClassCacheManager = CacheManagerUtil
				.getChargeClassCacheManager();
		List<String> overrideChargeClassestList = RBTDBManager.getInstance()
				.getOverrideChargeClassestList();

		if (parentClassType == null && childClassType == null)
			return null;

		if (parentClassType != null && childClassType != null
				&& parentClassType.equalsIgnoreCase(childClassType))
			chargeClass = chargeClassCacheManager
					.getChargeClass(parentClassType);
		else if (childClassType == null
				|| childClassType.equalsIgnoreCase("DEFAULT"))
			chargeClass = chargeClassCacheManager
					.getChargeClass(parentClassType);
		else if ((overrideChargeClassestList != null && overrideChargeClassestList
				.contains(childClassType.toLowerCase()))
				|| parentClassType == null
				|| childClassType.startsWith("TRIAL"))
			chargeClass = chargeClassCacheManager
					.getChargeClass(childClassType);
		else {
			ChargeClass parentChargeClass = chargeClassCacheManager
					.getChargeClass(parentClassType);
			ChargeClass childChargeClass = chargeClassCacheManager
					.getChargeClass(childClassType);

			if (parentChargeClass != null && childChargeClass != null
					&& parentChargeClass.getAmount() != null
					&& childChargeClass.getAmount() != null) {
				try {
					float parentAmount = Float.parseFloat(parentChargeClass
							.getAmount().replace(",","."));
					float childAmount = Float.parseFloat(childChargeClass
							.getAmount().replace(",","."));

					if (parentAmount > childAmount)
						chargeClass = parentChargeClass;
					else
						chargeClass = childChargeClass;
				} catch (Throwable e) {
					logger.error("Exception while parsing currency!!"+e);
				}
			}
		}

		return chargeClass;
	}

	public static CosDetails getCos(WebServiceContext task,
			Subscriber subscriber) {
		String subscriberID = task.getString(param_subscriberID);
		String circleID = getUserCircle(task);
		String isPrepaid = isUserPrepaid(task) ? YES : NO;
		String mode = task.containsKey(param_mode) ? task.getString(param_mode)
				: "VP";

		CosDetails cos = null;

		boolean isContentLite = true;
		boolean isDirectActivation = false;

		// -------------RBT Lite - VF enhancement June 2010 release
		// ------------- To make a User LITE user only if the following
		// parameter is true or null.
		ParametersCacheManager parametersCache = CacheManagerUtil
				.getParametersCacheManager();
		Parameters activateCosBasedOnContentParameter = parametersCache
				.getParameter("COMMON",
						"ACTIVATE_COS_BASED_ON_CONTENT_FOR_INACTIVE_SUBSCRIBER");
		boolean activateCosBasedOnContentForInActUser = (activateCosBasedOnContentParameter != null && activateCosBasedOnContentParameter
				.getValue().equalsIgnoreCase("false")) ? false : true;

		/*
		 * Check if the category is present in the task If yes, check whether
		 * shuffle or dynamic shuffle category If dynamic shuffle category or
		 * shuffle category, then check if all the clips are LITE contentType.
		 * If any one of the clips has a normal contentType then set the
		 * isContentLite boolean to false
		 */

		String clipContentType = null;
		Clip clip = null;
		if (activateCosBasedOnContentForInActUser) {
			if (task.getString(param_categoryID) != null) {
				String liteContentTypesStr = RBTParametersUtils
						.getParamAsString("COMMON", "LITE_CONTENT_TYPES",
								"LITE");
				String[] liteContentTypes = liteContentTypesStr.split(",");
				List<String> liteContentTypesList = new ArrayList<String>();
				if (liteContentTypes != null && liteContentTypes.length > 0)
					liteContentTypesList = Arrays.asList(liteContentTypes);

				RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();

				int categoryID = Integer.parseInt(task
						.getString(param_categoryID));
				Category category = rbtCacheManager.getCategory(categoryID);
				if (category.getCategoryTpe() == iRBTConstant.DYNAMIC_SHUFFLE) {
					String clipIDStr = task.getString(param_clipID);
					String[] clipIDs = clipIDStr.split(",");
					Clip[] clips = rbtCacheManager.getClips(clipIDs);
					if (clips != null) {
						for (Clip myClip : clips) {
							if (myClip != null
									&& (myClip.getContentType() == null || !liteContentTypesList
											.contains(myClip.getContentType()))) {
								isContentLite = false;
								break;
							} else if (myClip != null) {
								clipContentType = addClipContentType(
										clipContentType, myClip.getContentType());
							}
						}
					}
				} else if (Utility.isShuffleCategory(category.getCategoryTpe())) {
					Clip[] clips = rbtCacheManager
							.getActiveClipsInCategory(categoryID);
					if (clips != null) {
						for (Clip myClip : clips) {
							if (myClip != null
									&& (myClip.getContentType() == null || !liteContentTypesList
											.contains(myClip.getContentType()))) {
								isContentLite = false;
								break;
							} else if (myClip != null) {
								clipContentType = addClipContentType(
										clipContentType, myClip.getContentType());
							}
						}
					}
				} else if (category.getCategoryTpe() == iRBTConstant.KARAOKE
						|| category.getCategoryTpe() == iRBTConstant.RECORD) {
					isContentLite = false;
				} else {
					/*
					 * Check for the single clip contentType if it belongs to
					 * LITE content
					 */
					String clipID = task.getString(param_clipID);
					if (clipID != null) {
						try {
							clip = rbtCacheManager.getClip(clipID);
						} catch (NumberFormatException e) {
							logger.debug(e.getMessage(), e);
						}

						if (clip == null)
							clip = rbtCacheManager.getClipByPromoId(clipID);
						if (clip == null)
							clip = rbtCacheManager
									.getClipByRbtWavFileName(clipID);

						String contentType = null;
						if (clip != null)
							contentType = clip.getContentType();
						if (contentType == null
								|| !liteContentTypesList.contains(contentType))
							isContentLite = false;
						else
							clipContentType = contentType;
					} else {
						isContentLite = false;
					}
				}
			} else {
				isContentLite = false;
			}
		} else {
			isContentLite = false;
		}
		
		
		// The following block will choose cos based on content type. It is not limited to cos type LITE as in the block above. 
		// However, it supports only clips in the first phase.
		CosDetails contentBasedCos = null;
		if(activateCosBasedOnContentForInActUser && cosTypeContentTypeMap != null && clip != null && clip.getContentType() != null && !clip.getContentType().equals("NORMAL"))
		{
			for(String key : cosTypeContentTypeMap.keySet())
			{
				ArrayList<String> cosTypesList = cosTypeContentTypeMap.get(key);
				if(cosTypesList.contains(clip.getContentType()))
				{
					List<CosDetails> cosList = CacheManagerUtil.getCosDetailsCacheManager().getCosDetailsByCosType(key, circleID, isPrepaid);
					if (cosList != null && cosList.size() > 0)
					{
						for (CosDetails cosDetails : cosList)
						{
							if (cosDetails != null && cosDetails.getContentTypes() != null && cosDetails.getAccessMode() != null)
							{
								if (Arrays.asList(cosDetails.getContentTypes().split(",")).contains(clip.getContentType()) 
								&& (cosDetails.getAccessMode().equals("ALL") || Arrays.asList(cosDetails.getAccessMode().split(",")).contains(mode)))
									contentBasedCos = cosDetails;
							}
						}
					}
				}	
			}
		}
		
		if (task.containsKey(param_isDirectActivation)
				&& task.getString(param_isDirectActivation).equalsIgnoreCase(
						YES))
			isDirectActivation = true;

		boolean useDefaultCos = false;
		if (task.containsKey(param_mmContext)) {
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			if (mmContext[0].equalsIgnoreCase("RBT_PROMOTION")
					|| (mmContext[0].equalsIgnoreCase("RBT_CATEGORY") && mmContext.length > 2)
					|| (mmContext[0].equalsIgnoreCase("RBT_CLIP") && mmContext.length > 3))
				useDefaultCos = true;
		}
		if (task.containsKey(param_action)
				&& task.getString(param_action).equalsIgnoreCase(
						action_acceptGift))
			useDefaultCos = true;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (useDefaultCos && rbtDBManager.isSubscriberDeactivated(subscriber)) {
			cos = CacheManagerUtil.getCosDetailsCacheManager()
					.getDefaultCosDetail(circleID, isPrepaid);
		} else if (task.containsKey(param_cosID)
				&& (isDirectActivation || rbtDBManager
						.isSubscriberDeactivated(subscriber))) {
			cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(
					task.getString(param_cosID), circleID);
		} else if (rbtDBManager.isSubscriberDeactivated(subscriber)
				&& isContentLite && activateCosBasedOnContentForInActUser) {
			// gets Cos based on clip's content type
			cos = getLITECosBasedOnContentType(clipContentType, circleID,
					isPrepaid);

			if (logger.isDebugEnabled())
				logger.debug("LITE cos for the subscriber is : " + cos);
		} else if (rbtDBManager.isSubscriberDeactivated(subscriber) && contentBasedCos != null)
		{
			cos = contentBasedCos;
			if (logger.isDebugEnabled())
				logger.debug("Content based cos for the subscriber is : " + cos);
		}
		else {
			String subscriptionClass = null;
			if (task.containsKey(param_subscriptionClass))
				subscriptionClass = task.getString(param_subscriptionClass);
			else if (task.containsKey(param_rentalPack))
				subscriptionClass = task.getString(param_rentalPack);

			cos = rbtDBManager.getCos(task, subscriberID, subscriber, circleID,
					isPrepaid, mode, subscriptionClass);
		}

		// If contentType is Lite always activate with Lite COS
		if (!useDefaultCos && rbtDBManager.isSubscriberDeactivated(subscriber)
				&& !isContentLite) {
			String subscriptionClass = null;
			if (task.containsKey(param_subscriptionClass))
				subscriptionClass = task.getString(param_subscriptionClass);
			else if (task.containsKey(param_rentalPack))
				subscriptionClass = task.getString(param_rentalPack);

			if (!rbtDBManager.isPackRequest(cos)
					&& subscriptionClass != null
					&& !subscriptionClass.equalsIgnoreCase(cos
							.getSubscriptionClass()))
				cos = CacheManagerUtil.getCosDetailsCacheManager()
						.getDefaultCosDetail(circleID, isPrepaid);
		}

		return cos;
	}

	private static String addClipContentType(String contentTypesStr,
			String clipContentType) {
		if (contentTypesStr == null)
			contentTypesStr = clipContentType;
		else if (!Arrays.asList(contentTypesStr.split(",")).contains(
				clipContentType)) {
			contentTypesStr += "," + clipContentType;
			;
		}

		return contentTypesStr;
	}

	private static CosDetails getLITECosBasedOnContentType(
			String clipContentType, String circleID, String isPrepaid) {
		List<CosDetails> liteCosDetailsList = CacheManagerUtil
				.getCosDetailsCacheManager().getCosDetailsByCosType(
						COS_TYPE_LITE, circleID, isPrepaid);
		if (liteCosDetailsList == null || liteCosDetailsList.size() == 0)
			return null;

		for (CosDetails cosDetails : liteCosDetailsList) {
			if (cosDetails != null && cosDetails.getContentTypes() != null) {
				List<String> allowedContentTypes = Arrays.asList(cosDetails
						.getContentTypes().split(","));
				String[] contentTypes = clipContentType.split(",");
				boolean isContentTypeAllowed = true;
				for (String eachContentType : contentTypes) {
					if (!allowedContentTypes.contains(eachContentType)) {
						isContentTypeAllowed = false;
						break;
					}
				}

				if (isContentTypeAllowed)
					return cosDetails;
			}
		}

		return null;
	}

	public static boolean isSongGiftInPending(String contentID,
			ViralSMSTable viralSMSEntry, Subscriber caller) {
		if (viralSMSEntry != null) {
			String clipID = viralSMSEntry.clipID();
			String type = viralSMSEntry.type();
			if (clipID != null && clipID.equals(contentID)) {
				if (type.equals(iRBTConstant.GIFT)
						|| type.equals(iRBTConstant.GIFTCHRGPENDING)
						|| type.equals(iRBTConstant.GIFT_CHARGED)
						|| type.equals(iRBTConstant.GIFTED))
					return true;
			}
		}

		return false;
	}

	public static boolean isServiceGiftInPending(ViralSMSTable viralSMSEntry,
			Subscriber caller) {
		if (viralSMSEntry != null) {
			String type = viralSMSEntry.type();
			if (type.equals(iRBTConstant.GIFT)
					|| type.equals(iRBTConstant.GIFTCHRGPENDING)
					|| type.equals(iRBTConstant.GIFT_CHARGED)
					|| type.equals(iRBTConstant.GIFTED)) {
				RBTDBManager rbtDBManager = RBTDBManager.getInstance();
				if (rbtDBManager.isSubscriberDeactivated(caller)
						|| (rbtDBManager.isSubscriberActivated(caller) && (viralSMSEntry
								.clipID() == null || viralSMSEntry.clipID()
								.equals("null"))))
					return true;
			}
		}

		return false;
	}

	public static boolean isUserActivatedByGift(Subscriber caller) {
		if (!RBTDBManager.getInstance().isSubscriberDeactivated(caller)
				&& caller.activatedBy().equalsIgnoreCase("GIFT")) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -30);
			Date compareDate = calendar.getTime();
			if (caller.startDate().after(compareDate))
				return true;
		}

		return false;
	}

	public static boolean isServiceGiftInUse(ViralSMSTable viralSMSEntry,
			Subscriber caller) {
		if (viralSMSEntry != null) {
			String type = viralSMSEntry.type();
			if ((type.equals(iRBTConstant.ACCEPT_ACK)
					|| type.equals(iRBTConstant.ACCEPTED) || type
					.equals(iRBTConstant.ACCEPT_PRE))
					&& viralSMSEntry.clipID() == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_MONTH, -30);
				Date compareDate = calendar.getTime();
				if (viralSMSEntry.sentTime().after(compareDate))
					return true;
			}
		}

		return false;
	}

	public static boolean isContentAllowed(CosDetails cos, Clip[] clips) {
		for (Clip clip : clips) {
			if (!isContentAllowed(cos, clip)) {
				logger.info("Returning false, content is not allowed. " + clip
						+ ", cos: " + cos);
				return false;
			}
		}
		logger.info("Returning true, content is allowed. " + clips
				+ ", cos: " + cos);
		return true;
	}

	public static String getUnAllowedContentResponse(CosDetails cos,
			Clip[] clips) {
		String response = LITE_USER_PREMIUM_BLOCKED;
		logger.info("Checking for Cos : " + cos + " clip : " + clips);
		String clipContentType = null;
		for (Clip clip : clips) {
			if (!isContentAllowed(cos, clip))
				clipContentType = clip.getContentType();
		}
		response = COS_MISMATCH_CONTENT_BLOCKED + cos.getCosType() + "_"
				+ clipContentType;
		if (cos != null && cos.getCosType() != null
				&& cos.getCosType().equalsIgnoreCase(LITE)) {
			response = LITE_USER_PREMIUM_BLOCKED;
			logger.info("User cos type is LITE");
		}
		logger.info("Returning response from data utils :" + response);
		return response;
	}

	public static String getUnAllowedContentResponse(CosDetails cos, Clip clip) {
		String response = LITE_USER_PREMIUM_BLOCKED;
		logger.info("Checking for Cos : " + cos + " clip : " + clip);
		if (cos != null && cos.getContentTypes() != null) {
			List<String> allowedContentTypes = Arrays.asList(cos
					.getContentTypes().split(","));
			if (clip != null && clip.getContentType() != null) {
				String contentType = clip.getContentType();
				if (!allowedContentTypes.contains(contentType))
					response = COS_MISMATCH_CONTENT_BLOCKED + cos.getCosType()
							+ "_" + contentType;
			}
		}

		if (cos != null && cos.getCosType() != null
				&& cos.getCosType().equalsIgnoreCase(LITE)) {
			response = LITE_USER_PREMIUM_BLOCKED;
			logger.info("User cos type is LITE");
		}

		logger.info("Returning response from data utils :" + response);
		return response;

	}

	public static String getUnAllowedContentResponse(
			WebServiceContext webServiceContext, Category category, Clip clip)
			throws RBTException {
		String browsingLanguage = webServiceContext
				.getString(param_browsingLanguage);
		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);

		CosDetails cos = RBTDBManager.getInstance().getCosForActiveSubscriber(
				null, subscriber);
		if (category != null
				&& category.getCategoryTpe() == iRBTConstant.DYNAMIC_SHUFFLE) {
			String clipIDStr = webServiceContext.getString(param_clipID);
			String[] clipIDs = clipIDStr.split(",");

			Clip[] clips = new Clip[clipIDs.length];
			for (int i = 0; i < clipIDs.length; i++) {
				clips[i] = RBTCacheManager.getInstance().getClip(clipIDs[i]);
			}

			return DataUtils.getUnAllowedContentResponse(cos, clips);
		} else if (category != null
				&& Utility.isShuffleCategory(category.getCategoryTpe())) {
			Clip[] clips = RBTCacheManager.getInstance()
					.getActiveClipsInCategory(category.getCategoryId(),
							browsingLanguage);
			return DataUtils.getUnAllowedContentResponse(cos, clips);
		} else if (clip != null) {
			return DataUtils.getUnAllowedContentResponse(cos, clip);
		}

		return LITE_USER_PREMIUM_BLOCKED;
	}

	public static boolean isContentAllowed(CosDetails cos, Clip clip) {
		if (cos != null && cos.getContentTypes() != null) {
			List<String> cosContentTypes = Arrays.asList(cos
					.getContentTypes().split(","));
			logger.info("Validating cos content type with clip content type."
					+ " cosContentTypes: " + cosContentTypes);
			if (clip != null && clip.getContentType() != null) {
				String clipContentType = clip.getContentType();
				boolean isAllowed = cosContentTypes
						.contains(clipContentType);
				logger.info("Clip content type: " + clipContentType
						+ ", isAllowed: " + isAllowed);
				return isAllowed;
			}
		} 
		logger.info("Returning true, cos or cosContentTypes are null");
		return true;
	}

	/**
	 * Returns null if content exists, otherwise CATEGORY_NOT_EXISTS if category
	 * does not exist or INVALID_CATEGORY if category type 7(PARENT) or
	 * 3(BOUQUET) or CLIP_NOT_EXISTS if clip does not exist.
	 * 
	 * @param webServiceContext
	 * @param category
	 * @param clip
	 * 
	 * @return
	 */
	public static String isContentExists(WebServiceContext webServiceContext,
			Category category, Clip clip) {
		int selType = -1;
		logger.info("Checking the content. category: "
				+ category + ", clip: " + clip);
		if (webServiceContext.containsKey(param_selectionType)) {
			String strSelType = webServiceContext
					.getString(param_selectionType);
			try {
				selType = Integer.parseInt(strSelType);
			} catch (NumberFormatException ne) {
			}
		}

		if (category == null) {
			logger.info("Category not exits");
			return CATEGORY_NOT_EXISTS;
		}

		if (category.getCategoryTpe() == iRBTConstant.PARENT
				|| category.getCategoryTpe() == iRBTConstant.BOUQUET) {
			logger.info("Invalid Category, categoryType: "+category.getCategoryTpe());
			return INVALID_CATEGORY;
		}

		if (!Utility.isShuffleCategory(category.getCategoryTpe())
				&& clip == null) {
			if (!webServiceContext.containsKey(param_cricketPack)
					&& !webServiceContext.containsKey(param_profileHours)
					&& selType != iRBTConstant.PROFILE_SEL_TYPE
					&& category.getCategoryTpe() != iRBTConstant.RECORD
					&& category.getCategoryTpe() != iRBTConstant.KARAOKE
					&& category.getCategoryTpe() != iRBTConstant.DYNAMIC_SHUFFLE) {
				logger.info("Clip not exists. categoryType: "+category.getCategoryTpe());
				return CLIP_NOT_EXISTS;
			}
		}

		if (clip != null
				&& clip.getClipStartTime().getTime() > System
						.currentTimeMillis()) {
			logger.info("Clip is expired, returning clip not exists. clipId: "
					+ clip.getClipId() + ", clip startTime: "
					+ clip.getClipStartTime().getTime());
			return CLIP_NOT_EXISTS;
		}
		return null;
	}

	public static boolean isContentAllowed(WebServiceContext webServiceContext,
			Category category, Clip clip) throws RBTException {
		String browsingLanguage = webServiceContext
				.getString(param_browsingLanguage);
		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);

		CosDetails cos = RBTDBManager.getInstance().getCosForActiveSubscriber(
				webServiceContext, subscriber);
		if (category != null
				&& category.getCategoryTpe() == iRBTConstant.DYNAMIC_SHUFFLE) {
			String clipIDStr = webServiceContext.getString(param_clipID);
			String[] clipIDs = clipIDStr.split(",");

			Clip[] clips = new Clip[clipIDs.length];
			for (int i = 0; i < clipIDs.length; i++) {
				clips[i] = RBTCacheManager.getInstance().getClip(clipIDs[i]);
			}

			return DataUtils.isContentAllowed(cos, clips);
		} else if (category != null
				&& Utility.isShuffleCategory(category.getCategoryTpe())) {
			Clip[] clips = RBTCacheManager.getInstance()
					.getActiveClipsInCategory(category.getCategoryId(),
							browsingLanguage);
			return DataUtils.isContentAllowed(cos, clips);
		} else if (clip != null) {
			return DataUtils.isContentAllowed(cos, clip);
		}

		return true;
	}

	public static boolean isContentAllowedForCos(CosDetails cos,
			Category category, Clip clip) {
		/*
		 * if (category != null && category.getCategoryTpe() ==
		 * iRBTConstant.DYNAMIC_SHUFFLE) { String clipIDStr =
		 * webServiceContext.getString(param_clipID); String[] clipIDs =
		 * clipIDStr.split(",");
		 * 
		 * Clip[] clips = new Clip[clipIDs.length]; for (int i = 0; i <
		 * clipIDs.length; i++) { clips[i] =
		 * RBTCacheManager.getInstance().getClip(clipIDs[i]); }
		 * 
		 * return DataUtils.isContentAllowed(cos, clips); } else
		 */
		if (category != null
				&& Utility.isShuffleCategory(category.getCategoryTpe())) {
			Clip[] clips = RBTCacheManager.getInstance()
					.getActiveClipsInCategory(category.getCategoryId());
			return DataUtils.isContentAllowed(cos, clips);
		} else if (clip != null) {
			return DataUtils.isContentAllowed(cos, clip);
		}
		logger.info("Returning true, category and clip are null");
		return true;
	}

	/**
	 * Returns null if content is not expired, otherwise CATEGORY_EXPIRED if
	 * category is shuffle and its expired or CLIP_EXPIRED if clip expired.
	 * 
	 * @param category
	 * @param clip
	 * @return
	 */

	public static String isContentExpired(Category category, Clip clip) {
		return isContentExpired(null, category, clip, -1);
	}

	public static String isContentExpired(WebServiceContext task, Category category,
			Clip clip, int selType) {
		if(task.containsKey(SKIP_CONTENT_CHECK))
			return null;
			
		if (category == null)
			return CATEGORY_NOT_EXISTS;

		if (Utility.isShuffleCategory(category.getCategoryTpe())) {
			if (category.getCategoryEndTime().getTime() < System
					.currentTimeMillis())
				return CATEGORY_EXPIRED;
			else
				return null;
		}
		boolean activateInvalidContent = false;
		if (RBTParametersUtils.getParamAsBoolean(COMMON,
				"ACTIVATE_INVALID_CONTENT", "FALSE")
				&& category.getCategoryTpe() == iRBTConstant.RECORD) {
			activateInvalidContent = true;
		}
		if (clip == null && selType != iRBTConstant.PROFILE_SEL_TYPE
				&& (task == null || !task.containsKey(param_cricketPack))
				&& !activateInvalidContent)
			return CLIP_NOT_EXISTS;

		/* Allow selection from expired UGC content */
		try {
			boolean allowUGCContentRecentlyCreated = RBTParametersUtils
					.getParamAsBoolean("COMMON",
							"ALLOW_UGC_CONTENT_RECENTLY_CREATED", "FALSE");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			if (clip != null
					&& clip.getClipGrammar().equalsIgnoreCase("UGC")
					&& allowUGCContentRecentlyCreated
					&& clip.getClipEndTime().getTime() == sdf.parse(
							"2004/01/01").getTime()) {
				return null;
			}
		} catch (Exception e) {
		}

		if (clip != null
				&& clip.getClipEndTime().getTime() < System.currentTimeMillis())
			return CLIP_EXPIRED;

		return null;
	}

	public static boolean isServiceGiftLimitExceeded(
			List<SubscriberActivityCounts> subscriberActivityCounts, int limit) {
		if (subscriberActivityCounts == null
				|| subscriberActivityCounts.size() == 0)
			return false;

		int count = 0;
		for (SubscriberActivityCounts subscriberActivityCount : subscriberActivityCounts) {
			count += subscriberActivityCount.getServiceGiftsCount();
		}

		return (count >= limit);
	}

	public static boolean isToneGiftLimitExceeded(
			List<SubscriberActivityCounts> subscriberActivityCounts, int limit) {
		if (subscriberActivityCounts == null
				|| subscriberActivityCounts.size() == 0)
			return false;

		int count = 0;
		for (SubscriberActivityCounts subscriberActivityCount : subscriberActivityCounts) {
			count += subscriberActivityCount.getToneGiftsCount();
		}

		return (count >= limit);
	}

	public static int[] processBulkTaskForRemoteSubscriber(
			HashMap<String, List<String>> temp, StringBuilder success,
			StringBuilder failure, WebServiceContext task) {
		Set<String> circleIdKeys = temp.keySet();
		int[] count = new int[2];
		count[0] = 0;
		count[1] = 0;
		for (String circleId : circleIdKeys) {
			File uploadFile = null;
			File downloadedFile = null;
			try {
				List<String> subscriberList = temp.get(circleId);
				uploadFile = makeUploadFile(subscriberList, circleId);
				BulkSelectionRequest request = new BulkSelectionRequest();
				request.prepareRequestParams(task);
				request.setConnectorClass(HttpConnector.class);
				request.setBulkTaskFile(uploadFile.getAbsolutePath());
				request.setCircleID(circleId);
				downloadedFile = RBTClient.getInstance().bulkSelection(request);
				if (downloadedFile == null) {
					int failureCount = processRemoteError(subscriberList,
							failure);
					count[1] = count[1] + failureCount;
					continue;
				}
				int[] tempCount = findSuccessFailureCount(downloadedFile,
						success, failure);
				count[0] = count[0] + tempCount[0]; // Success
				count[1] = count[1] + tempCount[1]; // Failure
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (uploadFile != null) {
					uploadFile.delete();
				}
				if (downloadedFile != null) {
					downloadedFile.delete();
				}

			}
		}
		return count;
	}

	private static int processRemoteError(List<String> subscriberList,
			StringBuilder failure) {
		for (String line : subscriberList) {
			String[] arr = line.trim().split(",");
			String subId = arr[0].trim();
			failure.append("Selection failed for ");
			failure.append(subId).append("(").append(
					" Remote Forward Bulk Selection Error").append(")");
			failure.append(System.getProperty("line.separator"));
		}
		return subscriberList.size();
	}

	private static int[] findSuccessFailureCount(File file,
			StringBuilder success, StringBuilder failure) throws IOException {
		int[] count = new int[2];
		count[0] = 0;
		count[1] = 0;
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.equalsIgnoreCase("Success Result")) {
					br.readLine();
					count[0] = readBulkResponseFile(br, success);
				} else if (line.equalsIgnoreCase("Failure Result")) {
					br.readLine();
					count[1] = readBulkResponseFile(br, failure);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
			if (fr != null) {
				fr.close();
			}
		}
		return count;
	}

	private static int readBulkResponseFile(BufferedReader br,
			StringBuilder builder) throws IOException {
		int tempCount = 0;
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				return tempCount;
			}
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
			tempCount++;
		}
		return tempCount;
	}

	private static File makeUploadFile(List<String> subscriberList,
			String circleId) {
		String fileName = generateFileName(circleId);
		String tempPath = System.getProperty("java.io.tmpdir");
		File file = new File(tempPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(tempPath, fileName);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			for (String line : subscriberList) {
				bw.write(line);
				bw.newLine();
			}
		} catch (IOException ie) {
			logger.error("", ie);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
			}
		}
		return file;
	}

	private static String generateFileName(String circleId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return "BulkTask_" + circleId + "_" + sdf.format(new Date());
	}

	/**
	 * @param pinID
	 * @param msisdn
	 * @return
	 */
	public static String isValidPIN(String pinID, String msisdn) {
		String response = ERROR;
		Parameters params = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "PVM_PIN_VALIDATION_URL");
		if (params != null && params.getValue() != null) {
			String url = params.getValue().trim();
			response = getPINUrlResponse(url, pinID, msisdn);
			logger.info("Response from PIN Verification URL : " + response);

			if (response.equalsIgnoreCase(SUCCESS))
				response = VALID;
			else if (response.equalsIgnoreCase(TECHNICAL_DIFFICULTIES))
				response = TECHNICAL_DIFFICULTIES;
			else
				response = INVALID;
		}
		logger.info("Response from isValidPIN : " + response);
		return response;
	}

	/**
	 * @param pinID
	 * @param msisdn
	 *            TODO
	 * @return
	 */
	public static boolean blacklistPIN(String pinID, String msisdn) {
		boolean response = false;
		Parameters params = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "PVM_PIN_BLACKLIST_URL");
		if (params != null && params.getValue() != null) {
			String url = params.getValue().trim();
			String blacklistResponse = getPINUrlResponse(url, pinID, msisdn);
			response = blacklistResponse.equalsIgnoreCase("USED");
		}

		logger.info("Response from blacklistPIN : " + response);
		return response;
	}

	/**
	 * @param url
	 * @param pinID
	 * @param msisdn
	 * @return
	 */
	private static String getPINUrlResponse(String url, String pinID,
			String msisdn) {
		String response = ERROR;
		url = url.replaceAll("%pin%", pinID);
		url = url.replaceAll("%msisdn%", msisdn);

		try {
			HttpParameters httpParameters = new HttpParameters(url);
			if (logger.isInfoEnabled())
				logger.info("httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			if (logger.isInfoEnabled())
				logger.info("httpResponse: " + httpResponse);

			response = httpResponse.getResponse().trim();
		} catch (Exception e) {
			logger.error("", e);
			response = TECHNICAL_DIFFICULTIES;
		}

		return response;
	}

	public static boolean isSubscriberAllowedForUpgradation(
			Subscriber subscriber) {
		if (subscriber == null)
			return false;

		String subStatus = subscriber.subYes();
		if (subStatus.equals(iRBTConstant.STATE_ACTIVATED))
			return true;

		boolean isActPendingUsersAllowed = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.COMMON,
						"ALLOW_ACT_PENDING_USERS_FOR_UPGRADATION", "FALSE")
				.equalsIgnoreCase("TRUE");
		
		if (isActPendingUsersAllowed
				&& (subStatus.equals(STATE_CHANGE)
						|| subStatus.equals(STATE_TO_BE_ACTIVATED)
						|| subStatus.equals(STATE_GRACE) || subStatus
						.equals(STATE_ACTIVATION_PENDING))) {
			return true;
		}

		return false;
	}
	
	public static boolean isSubscriberAllowedForUpgradation(
			Subscriber subscriber, boolean isSuspendedUsersAllowed) {
		boolean isAllowed = false;
		if (subscriber == null) {
			logger.debug("Unable to check subscriber. subscriber is null.");
			return isAllowed;
		}

        String subStatus = subscriber.subYes();
        String allowedStatusForUpgradation = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON,
						"STATUS_ALLOWED_FOR_BASE_UPGRADATION", "B");
        List<String> statusAllowedForUpgrdList = null;
        boolean isBaseUpgradationAllowed = false;
        if(allowedStatusForUpgradation!=null){
            statusAllowedForUpgrdList = Arrays.asList(allowedStatusForUpgradation.split(","));
            isBaseUpgradationAllowed = statusAllowedForUpgrdList.contains(subStatus);
        }
            logger.debug("Subscriber Status: " + subStatus +"isBaseUpgradationAllowed = "+isBaseUpgradationAllowed);
            if (isBaseUpgradationAllowed) {
                  logger.debug("Subscriber is Active. Returning true.");
                  return true;
            }

		boolean isActPendingUsersAllowed = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.COMMON,
						"ALLOW_ACT_PENDING_USERS_FOR_UPGRADATION", "FALSE")
				.equalsIgnoreCase("TRUE");

		if (isActPendingUsersAllowed
				&& (subStatus.equals(STATE_CHANGE)
						|| subStatus.equals(STATE_TO_BE_ACTIVATED)
						|| subStatus.equals(STATE_GRACE) || subStatus
						.equals(STATE_ACTIVATION_PENDING))) {
			logger
					.debug("Activation of pending users are allowd. Returning true");
			return true;
		}

		// Accept the request, if mode is configured and subscriber
		// status C, A, G, N, Z, z
		if (isSuspendedUsersAllowed
				&& ((subStatus.equals(STATE_TO_BE_ACTIVATED)
						|| subStatus.equals(STATE_CHANGE)
						|| subStatus.equals(STATE_GRACE)
						|| subStatus.equals(STATE_ACTIVATION_PENDING)
						|| subStatus.equals(STATE_SUSPENDED_INIT) || subStatus
						.equals(STATE_SUSPENDED)))) {
			logger.debug("Suspended users are allowed. Returning true");
			return true;
		}
		logger.debug("Returning false. isActPendingUsersAllowed: "
				+ isActPendingUsersAllowed + ", isSuspendedUsersAllowed: "
				+ isSuspendedUsersAllowed);
		return false;
	}

	public static Subscriber getSubscriber(WebServiceContext webServiceContext)
			throws RBTException {
		Subscriber subscriber = null;
		if (webServiceContext.containsKey(param_subscriber)) {
			subscriber = (Subscriber) webServiceContext.get(param_subscriber);
			logger.debug("Returning subscriber: " + subscriber);
			return subscriber;
		}

		subscriber = RBTDBManager.getInstance().getSubscriber(
				webServiceContext.getString(param_subscriberID), true);
		logger.debug("Fetched the subscriber and updated in request, returning"
				+ " subscriber: " + subscriber);
		webServiceContext.put(param_subscriber, subscriber);
		return subscriber;
	}

	/**
	 * Returns the next ChargeClass with below priority:
	 * <ol>
	 * <li>If useUIChargeClass is 'y' and chargeClassType is not null then
	 * chargeClassType</li>
	 * <li>If Corporate selection then Clip ChargeClass</li>
	 * <li>If Ad-Rbt Selection then based on COMMON-CATEGORY_ID_ADRBT_CLASS_TYPE
	 * and COMMON-SELECTION_MODE_ADRBT_CLASS_TYPE parameter</li>
	 * <li>If UDS User then based on COMMON-UDSOPTIN_CHARGECLASS parameter</li>
	 * <li>If COS ChargeClass is non DEFAULT then COS ChargeClass</li>
	 * <li>Is Category is DAILY_SHUFFLE(10) then based on COMMON-MODE_CHRG_CLASS
	 * parameter</li>
	 * <li>If Clip ChargeClass is non DEFAULT and its amount is greater than
	 * Category or if it is configured in COMMON-OVERRIDE_CHARGE_CLASSES
	 * parameter then Clip ChargeClass otherwise Category ChargeClass</li>
	 * <li>If ChargeClassType is passed and its amount is greater than the
	 * chargeClass derived from above step or if it is configured in
	 * COMMON-OVERRIDE_CHARGE_CLASSES parameter or if it is 'YOUTHCARD' or
	 * 'DEFAULT_10' then chargeClassType otherwise chargeClass from above step</li>
	 * <ol>
	 * 
	 * @param webServiceContext
	 * @param subscriber
	 * @param category
	 * @param clip
	 * @return
	 */
	public static ChargeClass getNextChargeClassForSubscriber(
			WebServiceContext webServiceContext, Subscriber subscriber,
			Category category, Clip clip) {
		String mode = webServiceContext.getString(param_mode);
		String clipClassType = (clip != null) ? clip.getClassType() : null;
		int status = getStatusFromWebServiceContext(webServiceContext);

		String chargeClassType = webServiceContext.getString(param_chargeClass);
		boolean useUIChargeClass = YES.equalsIgnoreCase(webServiceContext
				.getString(param_useUIChargeClass));
		useUIChargeClass = useUIChargeClass && (chargeClassType != null);
		if (useUIChargeClass) {
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(
							chargeClassType);
			if (logger.isInfoEnabled())
				logger.info("chargeClass: " + chargeClass);
			return chargeClass;
		}
		
		String tempClassType = Utility.getCosOverrideClass(clip,
				useUIChargeClass, subscriber);
		if (tempClassType != null) {
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(
							tempClassType);
			if (logger.isInfoEnabled())
				logger.info("chargeClass: " + chargeClass);
			return chargeClass;
		}
		

		String classType = "DEFAULT";
		CosDetails cos = getCos(webServiceContext, subscriber);
		//Idea-Combo DT New Model RBT-14087
		classType = RBTDBManager.getInstance().getCosChargeClass(subscriber,
				category, clip, cos, webServiceContext);

		if (classType == null || classType.equalsIgnoreCase("DEFAULT")) {
			if (category != null
					&& category.getCategoryTpe() == iRBTConstant.DAILY_SHUFFLE
					&& dailyShuffleModeChargeClassMap.containsKey(mode)) {
				classType = dailyShuffleModeChargeClassMap.get(mode);
			} else {
				classType = "DEFAULT";
				if (category != null && category.getClassType() != null)
					classType = category.getClassType();

				if (clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& !clipClassType.equalsIgnoreCase(classType)
						&& (category == null || !Utility
								.isShuffleCategory(category.getCategoryTpe()))) {
					ChargeClass categoryChargeClass = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					ChargeClass clipChargeClass = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									clipClassType);

					if (categoryChargeClass != null && clipChargeClass != null
							&& categoryChargeClass.getAmount() != null
							&& clipChargeClass.getAmount() != null) {
						try {
							float categoryAmount = Float
									.parseFloat(categoryChargeClass.getAmount().replaceAll("\\,","\\."));
							float clipAmount = Float.parseFloat(clipChargeClass
									.getAmount().replaceAll("\\,","\\."));

							if ((categoryAmount < clipAmount)
									|| (overrideChargeClassList
											.contains(clipClassType)))
								classType = clipClassType;
						} catch (Throwable e) {
							logger.debug(e.getMessage(), e);
						}
					}

					if (clipClassType.startsWith("TRIAL") && category != null
							&& category.getCategoryId() != 26
							&& category.getCategoryId() != 23)
						classType = clipClassType;
				}

				if (chargeClassType != null) {
					ChargeClass first = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					ChargeClass second = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									chargeClassType);

					if (first != null && second != null
							&& first.getAmount() != null
							&& second.getAmount() != null) {
						try {
							float firstAmount = Float.parseFloat(first
									.getAmount().replaceAll("\\,","\\."));
							float secondAmount = Float.parseFloat(second
									.getAmount().replaceAll("\\,","\\."));

							if (firstAmount <= secondAmount
									|| secondAmount == 0
									|| chargeClassType
											.equalsIgnoreCase("YOUTHCARD")
									|| chargeClassType
											.equalsIgnoreCase("DEFAULT_10")
									|| (overrideChargeClassList
											.contains(chargeClassType)))
								classType = chargeClassType;
						} catch (Throwable e) {
							classType = chargeClassType;
						}
					} else {
						classType = chargeClassType;
					}

					if (first != null
							&& first.getChargeClass().startsWith("TRIAL")
							&& category != null
							&& category.getCategoryId() != 26
							&& category.getCategoryId() != 23) {
						classType = first.getChargeClass();
					}
				}
			}
		}

		HashMap<String, String> subExtraInfo = null;
		if (RBTDBManager.getInstance().isSubActive(subscriber))
			subExtraInfo = DBUtility.getAttributeMapFromXML(subscriber
					.extraInfo());

		// Change in logic for UDS User
		// Validation will happen it by the UDS_OPTIN in extra info column
		// If the UDS_OPTIN present in the subscriber extra info column and the
		// key value is either true or the configured charge class then he is a
		// UDS user and the corresponding charge class will be passed & update
		// in the Subscriber table.
		//JIRA-ID: RBT-13626
		String premiumChargeClass = null;
		boolean isUDSSubscriber = false;
		premiumChargeClass = Utility.isUDSUser(subExtraInfo,false);
		isUDSSubscriber =  (premiumChargeClass != null);

		String blockedContentTypesStr = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(
						iRBTConstant.COMMON, "UDS_BLOCKED_CONTENT_TYPES", "");
		List<String> blockedContentTypesList = null;
		if (blockedContentTypesStr != null) {
			blockedContentTypesList = Arrays.asList(blockedContentTypesStr
					.split(","));
		}
		boolean isFound = false;
		if (isUDSSubscriber && clip != null
				&& blockedContentTypesList != null && blockedContentTypesList.contains(clip
						.getContentType()) && RBTParametersUtils.getParamAsBoolean(
								iRBTConstant.COMMON,
								"IS_PREMIUM_CONTENT_ALLOWED_FOR_UDS_USER", "FALSE")) {
			// Change in logic for UDS User.
			if (premiumChargeClass != null
					&& !premiumChargeClass.equalsIgnoreCase("NULL")) {
				classType = premiumChargeClass;
				isFound = true;
			} else if (RBTParametersUtils.getParamAsBoolean(
					iRBTConstant.COMMON,
					PREMIUM_SELECTION_IS_CHARGE_CLASS_FROM_CLIP, "FALSE")) {
				classType = clip.getClassType();
				isFound = true;
			}
		} 
		if (!isFound && isUDSSubscriber
				&& (category == null || (category.getCategoryTpe() == iRBTConstant.SONGS || category
						.getCategoryTpe() == iRBTConstant.DTMF_CATEGORY))
				&& status != 90 && status != 99) {
			String udsChargeClass = RBTParametersUtils.getParamAsString(
					"COMMON", "UDSOPTIN_CHARGECLASS", null);
			if (udsChargeClass != null) {
				if (udsSelectionModeList.contains(mode)) {
					ChargeClass chargeClass = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					if (chargeClass != null) {
						float amount = Float
								.parseFloat(chargeClass.getAmount().replaceAll("\\,","\\."));
						if (amount != 0)
							classType = udsChargeClass;
					} else
						classType = udsChargeClass;
				} else
					classType = udsChargeClass;
			}
		}

		int selectionType = 0;
		if (webServiceContext.containsKey(param_selectionType))
			selectionType = Integer.parseInt(webServiceContext
					.getString(param_selectionType));

		if (selectionType == 1) // AD RBT Selection
		{
			if (category != null
					&& adRbtCategoryChargeClassMap.containsKey(category
							.getCategoryId())) {
				classType = adRbtCategoryChargeClassMap.get(category
						.getCategoryId());
			} else if (adRbtModeChargeClassesMap.containsKey(mode)) {
				classType = adRbtModeChargeClassesMap.get(mode);
			} else if (category != null
					&& !category.getClassType().equals("FREE") && status != 99) {
				classType = "ADRBT";
			}
		} else if (selectionType == 2 && status == 80 && clipClassType != null) {
			classType = clipClassType;
		}

		if (classType == null)
			classType = "DEFAULT";

		// Implementation for Christmas promotions.
		String christmasPeriod = RBTParametersUtils.getParamAsString("COMMON",
				"CHRISTMAS_PERIOD", null);
		logger.debug("christmasPeriod: " + christmasPeriod);
		if (null != christmasPeriod) {
			String[] times = christmasPeriod.split("-");
			if (times.length == 2) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
					Date stDate = sdf.parse(times[0]);
					Date endDate = sdf.parse(times[1]);

					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);

					Date cdate = c.getTime();

					if ((cdate.after(stDate) || cdate.equals(stDate))
							&& (cdate.before(endDate) || cdate.equals(endDate))) {

						String userStatusMegaPromo = RBTParametersUtils.getParamAsString(
								"COMMON", "USER_STATUS_MEGA_PROMO", null);
						
						String megaPromoMode = RBTParametersUtils.getParamAsString(
								"COMMON", "MEGA_PROMO_MODES", null);
						
						List<String> megaPromoModeList = null;
						if(megaPromoMode != null) {
							megaPromoModeList = Arrays.asList(megaPromoMode.split(","));
						}
						
						boolean isMegaPromo = true;
						if(userStatusMegaPromo != null && userStatusMegaPromo.equalsIgnoreCase("INACTIVE") && (!webServiceContext.containsKey("megaPromo_newuser") && !RBTDBManager.getInstance().isSubscriberDeactivated(subscriber))) {
							logger.debug("USER_STATUS_MEGA_PROMO configured " + userStatusMegaPromo + " but user is already active. srvKey: " + classType);
							isMegaPromo = false;
						}
						
						if(mode == null) {
							mode = "VP";
						}
						
						if(isMegaPromo && megaPromoModeList != null && !megaPromoModeList.contains(mode.toUpperCase())) {
							logger.debug(mode + " not configured in MEGA_PROMO_MODES. srvKey: " + classType);
							isMegaPromo = false;
						}
						
						String christmasSubClass = RBTParametersUtils
								.getParamAsString("COMMON",
										"CHRISTMAS_SUB_CLASS", null);
						if(isMegaPromo) {
							classType = classType.concat(christmasSubClass);
						}
						logger.debug("classType: " + classType);
					}
				} catch (ParseException pe) {
					logger.error("Unable to parse  CHRISTMAS_PERIOD. pe: "
							+ pe.getMessage(), pe);
				}
			} else {
				logger.debug("Wrong configuration for CHRISTMAS_PERIOD");
			}
		}
		
		if(!useUIChargeClass && clip != null && clip.getContentType() != null) {
			String contentTypeChageClass = CacheManagerUtil
					.getParametersCacheManager().getParameterValue("COMMON", "CLIP_" + clip.getContentType().toUpperCase() + "_CHARGE_CLASS", null);
			logger.debug("Content type charge class: " + contentTypeChageClass);
			if(contentTypeChageClass != null) {
				classType = contentTypeChageClass;
			}
		}
		
		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager()
				.getChargeClass(classType);
		if (logger.isInfoEnabled())
			logger.info("chargeClass: " + chargeClass);

		return chargeClass;
	}

	public static int getStatusFromWebServiceContext(
			WebServiceContext webserviceContext) {
		int status = 1;

		if (webserviceContext.containsKey(param_status))
			status = Integer
					.parseInt(webserviceContext.getString(param_status));
		else if (webserviceContext.containsKey(param_cricketPack))
			status = 90;
		else if (webserviceContext.containsKey(param_profileHours))
			status = 99;

		return status;
	}

	public static String isValidUser(WebServiceContext task,
			Subscriber subscriber) {
		String isValid = VALID;

		String subscriberID = task.getString(param_subscriberID);
		if (RBTDBManager.getInstance().isSubscriberDeactivated(subscriber)) {
			if (task.containsKey(param_circleID)) {
				String circleID = task.getString(param_circleID);
				SitePrefix sitePrefix = CacheManagerUtil
						.getSitePrefixCacheManager().getSitePrefixes(circleID);
				if (sitePrefix == null || sitePrefix.getSiteUrl() != null) {
					// If Site does not exists or if it is not a local site,
					// then returning INVALID_PREFIX.
					isValid = INVALID_PREFIX;
				}
			} else {
				SubscriberDetail subscriberDetail = DataUtils
						.getSubscriberDetail(task);
				if (subscriberDetail == null
						|| !subscriberDetail.isValidSubscriber()
						|| subscriberDetail.getCircleID() == null)
					isValid = INVALID_PREFIX;
			}
		}

		if (RBTDBManager.getInstance().isTotalBlackListSub(subscriberID))
			isValid = BLACK_LISTED;
		else if ((!task.containsKey(param_isDirectActivation) || task
				.getString(param_isDirectActivation).equalsIgnoreCase(NO))
				&& (!task.containsKey(param_preCharged) || task.getString(
						param_preCharged).equalsIgnoreCase(NO))
				&& RBTDBManager.getInstance().isSubscriberSuspended(subscriber)) {
			// Ignoring voluntarily suspended users
			HashMap<String, String> extraInfo = RBTDBManager.getInstance()
					.getExtraInfoMap(subscriber);
			if (extraInfo == null
					|| !extraInfo.containsKey(iRBTConstant.VOLUNTARY)
					|| (extraInfo.get(iRBTConstant.VOLUNTARY) != null && extraInfo
							.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase(
									"SM_SUSPENDED"))) {
				boolean suspendedUsersAllowed = false;
				if(task.containsKey(param_suspendedUsersAllowed)) {
					suspendedUsersAllowed = Boolean.valueOf(task.getString(param_suspendedUsersAllowed));
				}
				// Suspended users are also considered as valid subscriber. 
				if (!suspendedUsersAllowed) {
					isValid = SUSPENDED;
				}
			}
		}

		logger.info("Verified subscriber status, isValid: " + isValid
				+ ", subscriberID: " + subscriberID);
		return isValid;
	}

	public static boolean isUserInDelayedDeactivationState(Subscriber subscriber) {
		if (subscriber == null)
			return false;

		int delayedDeactivationHours = RBTParametersUtils.getParamAsInt(
				iRBTConstant.COMMON, "DELAYED_DEACTIVATION_HOURS", 0);
		if (delayedDeactivationHours > 0) {
			long subscriberEndTime = subscriber.endDate().getTime();
			if (subscriberEndTime > System.currentTimeMillis()
					&& subscriberEndTime <= (System.currentTimeMillis() + (delayedDeactivationHours * 60 * 60 * 1000))
					&& subscriber.deactivatedBy() != null) {
				// If subscriber endDate is greater than system time (active)
				// and endDate is less than DELAYED_DEACTIVATION_HOURS and
				// deactivatedBy is not null then user is in delayed
				// deactivation state.
				return true;
			}
		}

		return false;
	}

	/*
	 * validates and returns valid callerId array from comma seperated
	 * callerIdStr parameter
	 */
	public static String[] getValidCallerIds(String callerIdStr) {
		List<String> validCallerIdList = new ArrayList<String>();
		String[] validCallerIds = null;
		if (callerIdStr != null) {
			String[] callerIds = callerIdStr.trim().split(",");
			for (int i = 0; i < callerIds.length; i++) {
				String callerID = callerIds[i];
				
				//Added for RBT-15167 Fail to do the selection with caller=PRIVATE
				if(callerID != null && callerID.equalsIgnoreCase("PRIVATE")){
					validCallerIdList.add(callerID);
				}else if (callerID != null && !callerID.startsWith("G")) {
					// callerID null means for ALL callers and if starts with
					// 'G'
					// means groupID.

					Parameters parameter = CacheManagerUtil
							.getParametersCacheManager().getParameter(
									iRBTConstant.COMMON,
									"MINIMUM_CALLER_ID_LENGTH", "7");
					int minCallerIDLength = Integer.parseInt(parameter
							.getValue());

					boolean isValidCallerId = false;
					if (callerID.length() >= minCallerIDLength) {
						try {
							callerID = RBTDBManager.getInstance().subID(callerID);
							Long.parseLong(callerID);
							isValidCallerId = true;
						} catch (NumberFormatException e) {
						}
					}

					if (!isValidCallerId) {
						/*
						 * logger.info("Invalid callerID. Returning response: "+
						 * INVALID_PARAMETER); writeEventLog(subscriberID,
						 * getMode(task), "404", CUSTOMIZATION, getClip(task),
						 * getCriteria(task)); return INVALID_PARAMETER;
						 */
					} else {
						validCallerIdList.add(callerID);
					}
				} else if (callerID.startsWith("G")) {
					validCallerIdList.add(callerID);
				}
			}
			validCallerIds = validCallerIdList
					.toArray(new String[validCallerIdList.size()]);
		}
		return validCallerIds;
	}
}
