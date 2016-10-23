package com.onmobile.apps.ringbacktones.common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.SitePrefixCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SmsTextCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.common.exception.OnMobileException;

public class RBTContestUtils {

	private static final Logger CONTEST_URL_TXN_LOG = Logger
			.getLogger("ContestUrlTransactionLogger");

	private static final String POINTS_CONTEST_ID_TIME_VALIDITY_MAP = "POINTS_CONTEST_ID_TIME_VALIDITY_MAP";

	private static final String R_NO_OF_COPY_LEFT = "%NO_OF_COPY_LEFT%";

	private static final String R_NO_OF_COPY = "%NO_OF_COPY%";

	private static final String R_THRESHOLD_NUMBER = "%THRESHOLD_NUMBER%";

	private static final String R_NO_OF_POINTS_LEFT = "%NO_OF_POINTS_LEFT%";

	private static final String R_TOTAL_NO_OF_POINTS = "%TOTAL_NO_OF_POINTS%";

	private static final String R_NO_OF_POINTS_OBTAINED = "%NO_OF_POINTS_OBTAINED%";

	private static final String ALL_CONTEST_IDS = "ALL_CONTEST_IDS";

	private static final String ALL_POINTS_CONTEST_IDS = "ALL_POINTS_CONTEST_IDS";

	private static final String _CONTEST_IDS = "_CONTEST_IDS";

	private static final String _POINTS_CONTEST_IDS = "_POINTS_CONTEST_IDS";

	private static final String COMMON = "COMMON";

	private static final String CONTEST_ID = "CONTEST_ID";

	private static final String POINTS_CONTEST_ID = "POINTS_CONTEST_ID";

	private static final String NO_OF_COPIES = "NO_OF_COPIES";

	private static final String NO_OF_POINTS = "NO_OF_POINTS";

	private static final SmsTextCacheManager SMS_TEXT_CACHE_MANAGER = CacheManagerUtil
			.getSmsTextCacheManager();

	private static final SitePrefixCacheManager SITE_PREFIX_CACHE_MANAGER = CacheManagerUtil
			.getSitePrefixCacheManager();

	private static Logger logger = Logger.getLogger(RBTParametersUtils.class);

	private static Map<String, List<Date>> copyContestIDsTimeValidityMap = new HashMap<String, List<Date>>();

	private static Map<String, List<Date>> pointsContestIDsTimeValidityMap = new HashMap<String, List<Date>>();

	private static final RBTDBManager m_rbtDBManager = RBTDBManager
			.getInstance();

	private static String nationalContestIDs = RBTParametersUtils
			.getParamAsString(COMMON, ALL_CONTEST_IDS, null);

	private static List<String> nationalContestIDsList = ListUtils
			.convertToList(nationalContestIDs, ",");

	private static String nationalPointsContestIDs = RBTParametersUtils
			.getParamAsString(COMMON, ALL_POINTS_CONTEST_IDS, null);

	private static List<String> nationalPointsContestIDsList = ListUtils
			.convertToList(nationalPointsContestIDs, ",");

	private static int thresholdNoOfCopies = RBTParametersUtils.getParamAsInt(
			COMMON, "THRESHOLD_NUMBER_OF_COPIES_OF_COPY_CONTEST", 0);

	private static int finalNoOfCopies = RBTParametersUtils.getParamAsInt(
			COMMON, "FINAL_NO_OF_COPIES_OF_COPY_CONTEST", 0);

	private static int thresholdNoOfPoints = RBTParametersUtils.getParamAsInt(
			COMMON, "THRESHOLD_NUMBER_OF_POINTS_OF_CONTEST", 0);

	private static int finalNoOfPoints = RBTParametersUtils.getParamAsInt(
			COMMON, "FINAL_NO_OF_POINTS_OF_CONTEST", 0);

	// SMS texts to be sent to the subscriber
	private static String copySmsTextForBelowThres = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.COPY_CONTEST_THRESHOLD_BELOW_SMS);

	private static String copySmsTextForAboveThres = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.COPY_CONTEST_THRESHOLD_ABOVE_SMS);

	private static String copySmsTextForFinalSms = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.COPY_CONTEST_FINAL_SMS);

	private static String pointsSmsTextForBelowThres = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.POINTS_CONTEST_THRESHOLD_BELOW_SMS);

	private static String pointsSmsTextForAboveThres = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.POINTS_CONTEST_THRESHOLD_ABOVE_SMS);

	private static String pointsSmsTextForFinalSms = SMS_TEXT_CACHE_MANAGER
			.getSmsText(Constants.POINTS_CONTEST_FINAL_SMS);

	private static final String SENDER_NO = RBTParametersUtils
			.getParamAsString("DAEMON", "SENDER_NO", null);

	private static final String srvKeyCallbackMapStr = RBTParametersUtils
			.getParamAsString("DAEMON", "SRVKEY_ACTION_MAP", null);

	private static Map<String, String> srvKeyCallbackMap = MapUtils
			.convertToMap(srvKeyCallbackMapStr, ";", "=", null);
	
	private static final String amountToPointsCallbackMapStr = RBTParametersUtils
			.getParamAsString("DAEMON", "AMOUNT_ACTION_TO_POINTS_MAP", null);

	private static Map<String, String> amountToPointsCallbackMap = MapUtils
			.convertToMap(amountToPointsCallbackMapStr, ";", "=", null);

	private static final String contestUrlForContent = RBTParametersUtils
			.getParamAsString("DAEMON", "CONTEST_URL_FOR_CONTENT", null);

	private static final String circleAndContestIdMapStr = RBTParametersUtils
			.getParamAsString("DAEMON", "CIRCLE_CONTEST_URL_MAP", null);

	private static final Map<String, String> circleAndContestIdMap = MapUtils
			.convertToMap(circleAndContestIdMapStr, ",", "=", null);

	public static void copyInfluencerContest(String strSubID,
			String selectionInfo) throws OnMobileException, IOException,
			HttpException {
		logger.info("Input parameters. strSubID: " + strSubID
				+ ", selectionInfo: " + selectionInfo);
		if (copyContestIDsTimeValidityMap.size() == 0) {
			initCopyContestIDsTimeMap();
		}

		String contestCallerID = RBTParametersUtils
				.getMsisdnFromSelectionInfo(selectionInfo);

		Subscriber contestSubscriber = getSubscriber(strSubID);
		Subscriber contestCaller = getSubscriber(contestCallerID);

		if (contestCaller != null) {
			String contestCallerCircleID = contestCaller.getCircleID();

			// By default validate contest caller is true.

			if (validContestaller(contestCaller)) {
				incrementNoOfCopies(contestCallerID, contestSubscriber,
						contestCaller, contestCallerCircleID);
			} else {

				// transfer request to other circle.
				redirectToContestCallerId(contestCallerID,
						contestCallerCircleID);
			}
		}
		logger.info("Processed Copy Content request. " + " contestSubscriber: "
				+ strSubID + ", contestCallerID: " + contestCallerID);
	}

	public static void pointsContest(String strSubID, int pointsEarned) {
		logger.info("Input parameters. strSubID: " + strSubID
				+ ", pointsEarned: " + pointsEarned);
		if (pointsContestIDsTimeValidityMap.size() == 0) {
			initPointsContestIDsTimeMap();
		}

		// String contestCallerID = RBTParametersUtils
		// .getMsisdnFromSelectionInfo(selectionInfo);

		Subscriber contestSubscriber = getSubscriber(strSubID);

		try {
			incrementNoOfPoints(contestSubscriber, pointsEarned);
		} catch (OnMobileException oe) {
			logger.error("Unable to process Points Content request. "
					+ " OnMobileException: " + oe.getMessage(), oe);
		} catch (Exception e) {
			logger.error("Unable to process Points Content request. "
					+ " Exception: " + e.getMessage(), e);

		}

	}

	private static boolean validContestaller(Subscriber contestCaller) {

		boolean isValid = true;
		boolean isValidPrefix = (null != contestCaller) ? contestCaller
				.isValidPrefix() : false;
		boolean isActive = (null != contestCaller) ? Utility
				.isUserActive(contestCaller.getStatus()) : false;
		logger.info("isValidPrefix: " + isValidPrefix + ", isActive: "
				+ isActive + ", isValid: " + isValid);
		isValid = (isValidPrefix && isActive);
		return isValid;

	}

	private static void redirectToContestCallerId(String contestCallerID,
			String contestCallerCircleID) throws IOException, HttpException {

		SitePrefix sitePrefix = SITE_PREFIX_CACHE_MANAGER
				.getSitePrefixes(contestCallerCircleID);
		String contestCallerIDSiteUrl = (sitePrefix != null) ? sitePrefix
				.getSiteUrl() : null;
		if (contestCallerIDSiteUrl!=null && contestCallerIDSiteUrl.indexOf("/rbt/") != -1) {
			contestCallerIDSiteUrl = contestCallerIDSiteUrl != null ? contestCallerIDSiteUrl
					.substring(0, contestCallerIDSiteUrl.indexOf("/rbt/"))
					: null;
		}

		if (null != contestCallerIDSiteUrl) {

			contestCallerIDSiteUrl += "/rbt/Subscription.do?action=copy_contest&subscriberID="
					+ contestCallerID;

			logger.info("Forwarding Copy Contest request to site: "
					+ contestCallerIDSiteUrl);
			HttpParameters httpParameters = new HttpParameters(
					contestCallerIDSiteUrl);
			RBTHttpClient rbtHttpClient = new RBTHttpClient(httpParameters);
			HashMap<String, String> contestRequestParams = new HashMap<String, String>();
			rbtHttpClient.makeRequestByGet(contestCallerIDSiteUrl,
					contestRequestParams);
			logger.info("Successfully redirected Copy Contest "
					+ "request to site: " + contestCallerIDSiteUrl
					+ ", contestCallerID: " + contestCallerID);
		} else {
			logger.warn("Unable to redirect to site. contestCallerID: "
					+ contestCallerID + ",contestCallerCircleID "
					+ contestCallerCircleID);
		}
	}

	private static boolean incrementNoOfCopies(String contestCallerID,
			Subscriber contestSubscriber, Subscriber contestCaller,
			String contestCallerCircleID) throws OnMobileException {
		boolean isProcessed = false;
		HashMap<String, String> contestCallerExtraInfoMap = contestCaller
				.getUserInfoMap();
		int noOfTimesSongCopied = 0;
		if (contestCallerExtraInfoMap != null
				&& contestCallerExtraInfoMap.containsKey(NO_OF_COPIES)) {
			String noOfCopies = contestCallerExtraInfoMap.get(NO_OF_COPIES);
			noOfTimesSongCopied = Integer.parseInt(noOfCopies);
		} else if (contestCallerExtraInfoMap == null) {
			contestCallerExtraInfoMap = new HashMap<String, String>();
		}
		String contestID = contestCallerExtraInfoMap.get(CONTEST_ID);
		boolean isValidContestID = true;

		logger.info("Caller extra info contains noOfTimesSongCopied: "
				+ noOfTimesSongCopied + ", contestID: " + contestID);

		if (!nationalContestIDsList.contains(contestID)) {

			List<String> circleContestIDsList = getContestIDList(contestCallerCircleID);

			if (!circleContestIDsList.contains(contestID)) {
				isValidContestID = false;
			} else {
				String contestSubCircleID = contestSubscriber.getCircleID();
				if (!contestSubCircleID.equalsIgnoreCase(contestCallerCircleID))
					isValidContestID = false;
			}
		}

		boolean isValidPeriod = false;
		List<Date> contestValidPeriodList = copyContestIDsTimeValidityMap
				.get(contestID);
		if (contestValidPeriodList != null
				&& contestValidPeriodList.get(0).before(new Date())
				&& contestValidPeriodList.get(1).after(new Date())) {
			isValidPeriod = true;
		}
		boolean update = false;
		logger.info(" isValidContestID = " + isValidContestID
				+ " isValidPeriod = " + isValidPeriod);
		if (isValidContestID && isValidPeriod) {
			noOfTimesSongCopied = noOfTimesSongCopied + 1;
			contestCallerExtraInfoMap.put(NO_OF_COPIES, noOfTimesSongCopied
					+ "");
			contestCallerExtraInfoMap.put(CONTEST_ID, contestID);
			String contestCallerExtraInfo = DBUtility
					.getAttributeXMLFromMap(contestCallerExtraInfoMap);
			update = m_rbtDBManager.updateExtraInfo(contestCallerID,
					contestCallerExtraInfo);
			logger.info("Sending SMS. Updated extra info of contest caller. "
					+ "contestCallerID: " + contestCallerID
					+ ", contestCallerExtraInfo: " + contestCallerExtraInfo);
			if (update) {
				isProcessed = sendSms(contestCallerID, noOfTimesSongCopied);
			}
		} else {
			logger.info("Getting current live contest: "
					+ contestCallerCircleID);
			contestID = getCurrentLiveContestID(contestCallerCircleID);
			if (contestID != null) {
				contestCallerExtraInfoMap.put(NO_OF_COPIES, 1 + "");
				noOfTimesSongCopied = noOfTimesSongCopied + 1;
				contestCallerExtraInfoMap.put(CONTEST_ID, contestID);
				String contestCallerExtraInfo = DBUtility
						.getAttributeXMLFromMap(contestCallerExtraInfoMap);
				update = m_rbtDBManager.updateExtraInfo(contestCallerID,
						contestCallerExtraInfo);
				logger.info("Sending SMS. Updated extra info of contest caller. "
						+ "contestCallerID: "
						+ contestCallerID
						+ ", contestCallerExtraInfo: " + contestCallerExtraInfo);
				if (update) {
					isProcessed = sendSms(contestCallerID, noOfTimesSongCopied);
				}
			} else {
				logger.warn("No live contest is found. contestCallerCircleID: "
						+ contestCallerCircleID);
			}
		}
		logger.info("Processed copy contest request. contestCallerID: "
				+ contestCallerID + ", isProcessed: " + isProcessed);
		return isProcessed;
	}

	private static boolean incrementNoOfPoints(Subscriber contestSubscriber,
			int pointsEarned) throws OnMobileException {
		boolean isProcessed = false;
		if (null != contestSubscriber) {
			HashMap<String, String> contestSubscriberExtraInfoMap = contestSubscriber
					.getUserInfoMap();
			String contestSubscriberCircleID = contestSubscriber.getCircleID();
			String contestSubscriberID = contestSubscriber.getSubscriberID();

			int noOfPoints = 0;
			if (contestSubscriberExtraInfoMap == null) {
				contestSubscriberExtraInfoMap = new HashMap<String, String>();
			}
			if (contestSubscriberExtraInfoMap.containsKey(NO_OF_POINTS)) {
				String noOfPointsStr = contestSubscriberExtraInfoMap
						.get(NO_OF_POINTS);
				noOfPoints = Integer.parseInt(noOfPointsStr);
			}

			String pointContestID = contestSubscriberExtraInfoMap
					.get(POINTS_CONTEST_ID);
			boolean isValidContestID = true;

			logger.info("Caller extra info contains noOfPoints: " + noOfPoints
					+ ", pointContestID: " + pointContestID);

			if (!nationalPointsContestIDsList.contains(pointContestID)) {

				List<String> circleContestIDsList = getPointContestIDList(contestSubscriberCircleID);

				if (!circleContestIDsList.contains(pointContestID)) {
					isValidContestID = false;
				}
			}

			boolean isValidPeriod = false;
			List<Date> contestValidPeriodList = pointsContestIDsTimeValidityMap
					.get(pointContestID);

			Date now = new Date();
			logger.info("Checking current date: " + now
					+ ", contestValidPeriodList: " + contestValidPeriodList);
			if (contestValidPeriodList != null
					&& contestValidPeriodList.get(0).before(now)
					&& contestValidPeriodList.get(1).after(now)) {
				isValidPeriod = true;
			}
			boolean update = false;
			logger.info(" isValidContestID = " + isValidContestID
					+ " isValidPeriod = " + isValidPeriod);
			if (isValidContestID && isValidPeriod) {
				noOfPoints = noOfPoints + pointsEarned;
				contestSubscriberExtraInfoMap
						.put(NO_OF_POINTS, noOfPoints + "");
				contestSubscriberExtraInfoMap.put(POINTS_CONTEST_ID,
						pointContestID);
				String contestSubscriberExtraInfo = DBUtility
						.getAttributeXMLFromMap(contestSubscriberExtraInfoMap);
				update = m_rbtDBManager.updateExtraInfo(contestSubscriberID,
						contestSubscriberExtraInfo);
				logger.info("Sending SMS. Updated extra info of contest caller. "
						+ "contestCallerID: "
						+ contestSubscriberID
						+ ", contestCallerExtraInfo: "
						+ contestSubscriberExtraInfo);
				if (update) {
					isProcessed = sendPointsSms(contestSubscriberID,
							noOfPoints, pointsEarned);
				}
			} else {
				logger.info("Getting current live contest: "
						+ contestSubscriberCircleID);
				pointContestID = getCurrentLivePointContestID(contestSubscriberCircleID);
				if (pointContestID != null) {
					noOfPoints = pointsEarned;
					contestSubscriberExtraInfoMap.put(NO_OF_POINTS, noOfPoints
							+ "");
					contestSubscriberExtraInfoMap.put(POINTS_CONTEST_ID,
							pointContestID);
					String contestSubscriberExtraInfo = DBUtility
							.getAttributeXMLFromMap(contestSubscriberExtraInfoMap);
					update = m_rbtDBManager.updateExtraInfo(
							contestSubscriberID, contestSubscriberExtraInfo);
					logger.info("Sending SMS. Updated extra info of "
							+ "contest caller. " + "contestCallerID: "
							+ contestSubscriberID
							+ ", contestCallerExtraInfo: "
							+ contestSubscriberExtraInfo);
					if (update) {
						isProcessed = sendPointsSms(contestSubscriberID,
								noOfPoints, pointsEarned);
					}
				} else {
					logger.warn("No live contest is found. contestCallerCircleID: "
							+ contestSubscriberCircleID);
				}
			}
			logger.info("Processed point contest request. contestCallerID: "
					+ contestSubscriberID + ", isProcessed: " + isProcessed);
		} else {
			logger.warn("Not processed point contest request. contestCallerID is null  ");
		}
		return isProcessed;
	}

	public static boolean sendSms(String contestCallerID,
			int noOfTimesSongCopied) throws OnMobileException {
		logger.info("Sending SMS to contestCallerID: " + " contestCallerID: "
				+ contestCallerID + ", noOfTimesSongCopied: "
				+ noOfTimesSongCopied);

		boolean isSentSms = false;

		StringBuilder finalSmsText = new StringBuilder();

		if (copySmsTextForBelowThres != null
				&& noOfTimesSongCopied < thresholdNoOfCopies) {
			String smsText = copySmsTextForBelowThres;
			smsText = smsText.replaceAll(
					R_NO_OF_COPY, noOfTimesSongCopied + "");
			smsText = smsText.replaceAll(
					R_NO_OF_COPY_LEFT,
					(thresholdNoOfCopies - noOfTimesSongCopied) + "");

			finalSmsText.append(smsText);

		} else if (copySmsTextForAboveThres != null
				&& noOfTimesSongCopied >= thresholdNoOfCopies) {
			String smsText = copySmsTextForAboveThres;
			smsText = smsText.replaceAll(
					R_NO_OF_COPY, noOfTimesSongCopied + "");
			smsText = smsText.replaceAll(
					R_THRESHOLD_NUMBER, thresholdNoOfCopies + "");
			smsText = smsText.replaceAll(
					R_NO_OF_COPY_LEFT,
					(thresholdNoOfCopies - noOfTimesSongCopied) + "");

			finalSmsText.append(smsText);

		} else if (copySmsTextForFinalSms != null
				&& noOfTimesSongCopied >= finalNoOfCopies) {
			String smsText = copySmsTextForFinalSms;

			smsText = smsText.replaceAll(
					R_NO_OF_COPY, noOfTimesSongCopied + "");

			finalSmsText.append(smsText);
		}

		// SMS text send as a final SMS, once the user copies
		// the specified number of copies
		if (SENDER_NO != null) {
			isSentSms = Tools.sendSMS(SENDER_NO, contestCallerID,
					finalSmsText.toString(), false);
			logger.info("Sent SMS. isSentSms: " + isSentSms
					+ ", finalSmsText: " + finalSmsText.toString());
		} else {
			logger.warn("Unable to Sent SMS. SENDER_NO is not configured");

		}

		return isSentSms;
	}

	public static boolean sendPointsSms(String contestCallerID, int noOfPoints,
			int pointsEarned) throws OnMobileException {
		logger.info("Sending SMS to contestCallerID: " + " contestCallerID: "
				+ contestCallerID + ", noOfPoints: " + noOfPoints);

		boolean isSentSms = false;

		StringBuilder finalSmsText = new StringBuilder();

		if (pointsSmsTextForFinalSms != null && noOfPoints >= finalNoOfPoints) {
			String smsText = pointsSmsTextForFinalSms;

			smsText = smsText.replaceAll(R_TOTAL_NO_OF_POINTS, noOfPoints + "");
			smsText = smsText.replaceAll(R_NO_OF_POINTS_OBTAINED, pointsEarned
					+ "");

			finalSmsText.append(smsText);
		} else if (pointsSmsTextForBelowThres != null
				&& noOfPoints < thresholdNoOfPoints) {

			String smsText = pointsSmsTextForBelowThres;

			smsText = smsText.replaceAll(R_NO_OF_POINTS_OBTAINED, pointsEarned
					+ "");
			smsText = smsText.replaceAll(R_TOTAL_NO_OF_POINTS, noOfPoints + "");
			smsText = smsText.replaceAll(R_THRESHOLD_NUMBER,
					thresholdNoOfPoints + "");
			smsText = smsText.replaceAll(R_NO_OF_POINTS_LEFT,
					(thresholdNoOfPoints - noOfPoints) + "");

			finalSmsText.append(smsText);

		} else if (pointsSmsTextForAboveThres != null
				&& noOfPoints >= thresholdNoOfPoints) {
			String smsText = pointsSmsTextForAboveThres;

			smsText = smsText.replaceAll(R_NO_OF_POINTS_OBTAINED, pointsEarned
					+ "");
			smsText = smsText.replaceAll(R_TOTAL_NO_OF_POINTS, noOfPoints + "");
			smsText = smsText.replaceAll(R_THRESHOLD_NUMBER,
					thresholdNoOfPoints + "");
			smsText = smsText.replaceAll(R_NO_OF_POINTS_LEFT,
					(finalNoOfPoints - noOfPoints) + "");

			finalSmsText.append(smsText);

		}

		// SMS text send as a final SMS, once the user copies
		// the specified number of copies
		if (SENDER_NO != null) {
			isSentSms = Tools.sendSMS(SENDER_NO, contestCallerID,
					finalSmsText.toString(), false);
			logger.info("Sent SMS for points. isSentSms: " + isSentSms
					+ ", finalSmsText: " + finalSmsText.toString());
		} else {
			logger.warn("Unable to Sent SMS. SENDER_NO is not configured");

		}
		return isSentSms;
	}

	private static List<String> getContestIDList(String contestCallerCircleID) {
		String circleContestIDs = RBTParametersUtils.getParamAsString(COMMON,
				contestCallerCircleID + _CONTEST_IDS, null);
		List<String> list = ListUtils.convertToList(circleContestIDs, ",");
		logger.debug("Configured contest ids for circle: "
				+ contestCallerCircleID + ", list: " + list);
		return list;
	}

	private static List<String> getPointContestIDList(
			String contestCallerCircleID) {
		String circlePointContestIDs = RBTParametersUtils.getParamAsString(
				COMMON, contestCallerCircleID + _POINTS_CONTEST_IDS, null);
		List<String> list = ListUtils.convertToList(circlePointContestIDs, ",");
		logger.debug("Configured points contest ids for circle: "
				+ contestCallerCircleID + ", list: " + list);
		return list;
	}

	private static Subscriber getSubscriber(String contestCallerID) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				contestCallerID);
		Subscriber contestCaller = RBTClient.getInstance().getSubscriber(
				rbtDetailsRequest);
		return contestCaller;
	}

	private static void initCopyContestIDsTimeMap() {
		String confParam = RBTParametersUtils.getParamAsString(COMMON,
				"CONTEST_ID_TIME_VALIDITY_MAP", null);
		if (confParam == null)
			return;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String str[] = confParam.split(";");
			for (String contestIdAndTime : str) {
				String aa[] = contestIdAndTime.split("=");
				if (aa.length == 2) {
					String contestID = aa[0];
					String timeduration = aa[1];
					String time[] = timeduration.split(":");
					Date startTime = sdf.parse(time[0]);
					Date endTime = sdf.parse(time[1]);
					List<Date> list = new ArrayList<Date>();
					list.add(startTime);
					list.add(endTime);
					copyContestIDsTimeValidityMap.put(contestID, list);
				}
			}
		} catch (Exception ex) {
			logger.info("Exception while initializing Copy Contest ID and Time Duration Mapping");
		}
		logger.info("Initialized copyContestIDsTimeValidityMap: "
				+ copyContestIDsTimeValidityMap);
	}

	private static void initPointsContestIDsTimeMap() {
		String confParam = RBTParametersUtils.getParamAsString(COMMON,
				POINTS_CONTEST_ID_TIME_VALIDITY_MAP, null);
		if (confParam == null)
			return;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String str[] = confParam.split(";");
			for (String contestIdAndTime : str) {
				String aa[] = contestIdAndTime.split("=");
				if (aa.length == 2) {
					String contestID = aa[0];
					String timeduration = aa[1];
					String time[] = timeduration.split(":");
					Date startTime = sdf.parse(time[0]);
					Date endTime = sdf.parse(time[1]);
					List<Date> list = new ArrayList<Date>();
					list.add(startTime);
					list.add(endTime);
					pointsContestIDsTimeValidityMap.put(contestID, list);
				}
			}
		} catch (Exception ex) {
			logger.info(
					"Exception while initializing Points Contest ID"
							+ " and Time Duration Mapping. Exception: "
							+ ex.getMessage(), ex);
		}
		logger.info("Initialized pointsContestIDsTimeValidityMap: "
				+ pointsContestIDsTimeValidityMap);
	}

	private static String getCurrentLiveContestID(String circleID) {

		String contestIDs = RBTParametersUtils.getParamAsString(COMMON,
				circleID + _CONTEST_IDS, null);
		List<String> contestIDsList = ListUtils.convertToList(contestIDs, ",");

		logger.info("Contest configured for circleID: " + circleID
				+ ", contestIDs: " + contestIDs + ", nationalContestIDs: "
				+ nationalContestIDs);

		if (contestIDs == null && nationalContestIDs == null)
			return null;
		String currentContestID = null;

		// checking the current date with common contest ids
		for (String id : nationalContestIDsList) {
			List<Date> list = copyContestIDsTimeValidityMap.get(id);
			if (list != null && list.get(0).before(new Date())
					&& list.get(1).after(new Date())) {
				currentContestID = id;
				break;
			}
		}

		// checking the current date with circle based contest ids
		for (String id : contestIDsList) {
			List<Date> list = copyContestIDsTimeValidityMap.get(id);
			if (list != null && list.get(0).before(new Date())
					&& list.get(1).after(new Date())) {
				currentContestID = id;
				break;
			}
		}
		logger.info("Returning current live contest Id: " + currentContestID);
		return currentContestID;
	}

	private static String getCurrentLivePointContestID(String circleID) {

		String pointContestIDs = RBTParametersUtils.getParamAsString(COMMON,
				circleID + _POINTS_CONTEST_IDS, null);
		List<String> contestIDsList = ListUtils.convertToList(pointContestIDs,
				",");

		logger.info("Contest configured for circleID: " + circleID
				+ ", pointContestIDs: " + pointContestIDs
				+ ", nationalPointsContestIDs: " + nationalPointsContestIDs);

		if (pointContestIDs == null && nationalPointsContestIDs == null)
			return null;
		String currentContestID = null;

		// checking the current date with common contest ids
		for (String id : nationalPointsContestIDsList) {
			List<Date> list = pointsContestIDsTimeValidityMap.get(id);
			if (list != null && list.get(0).before(new Date())
					&& list.get(1).after(new Date())) {
				currentContestID = id;
				break;
			}
		}

		// checking the current date with circle based contest ids
		for (String id : contestIDsList) {
			List<Date> list = pointsContestIDsTimeValidityMap.get(id);
			if (list != null && list.get(0).before(new Date())
					&& list.get(1).after(new Date())) {
				currentContestID = id;
				break;
			}
		}
		logger.info("Returning current live contest Id: " + currentContestID);
		return currentContestID;
	}
	
	/**
	 * Preference is given to amountCharged while calculating the points earned.
	 * @param type
	 * @param action
	 * @param srvKey
	 * @param amountCharged
	 * @return the points earned
	 */
	public static int getPointsEarned(String type, String action, String srvKey, String amountCharged) {

		logger.debug("Checking for srvKey: " + srvKey + ", action: " + action + ", amountCharged: " + amountCharged);
		Integer pointsToBeAdded = 0;
		String key = null;
		
		if (type != null && action != null) {
			boolean isPointCalculationFromAmount = false;
			if (amountCharged != null) {
				key = type.concat("_").concat(action).concat("_").concat(amountCharged);
				if (amountToPointsCallbackMap.containsKey(key)) {
					logger.debug("Amount is configured in parameter and so, retrieving the configured points "
							+ "for the amount.");
					isPointCalculationFromAmount = true;
					String value = amountToPointsCallbackMap.get(key);
					pointsToBeAdded = Integer.parseInt(value);
				}
			} 
			if (!isPointCalculationFromAmount) {
				if (srvKey != null) {
					key = type.concat("_").concat(action).concat("_").concat(srvKey);
					if (srvKeyCallbackMap.containsKey(key)) {
						logger.debug("Points to be retrieved from srvKeyCallbackMap,");
						String value = srvKeyCallbackMap.get(key);
						pointsToBeAdded = Integer.parseInt(value);
					}
				}
			}
		}
		logger.info("Returning pointsToBeAdded: " + pointsToBeAdded
				+ ", for key: " + key + ", srvKeyCallbackMap: "
				+ srvKeyCallbackMap + ", amountToPointsCallbackMap: " + amountToPointsCallbackMap);
		return pointsToBeAdded;
	}

	public static void hitContestUrl(String subscriberId, String circleId,
			String mode, String chargedAmount, String transactionType) {

		if (null == subscriberId) {
			logger.warn("Unable to hit contest url, subscriberId is null");
			return;
		}

		if (null == contestUrlForContent) {
			logger.debug("Not making a hit to contest url, CONTEST_URL_FOR_CONTENT is not configured");
			return;
		}

		if (null == circleId) {
			logger.warn("Unable to hit contest url, circleId is null");
			return;
		}

		if (null == transactionType) {
			logger.warn("Unable to hit contest url, transactionType is null");
			return;
		}

		String contestUrlResponse = null;
		try {
			String contestId = circleAndContestIdMap.get(circleId);
			/*
			 * To support test numbers configure key as circleId_subscriberId
			 * instead of circleId. To work this feature, there should not be a
			 * configuration contestId by circleId i.e. circleId=contestId.
			 */
			if (contestId == null) {
				String key = circleId + "_" + subscriberId;
				contestId = circleAndContestIdMap.get(key);
			}

			if (null != contestId) {

				String contestUrl = constructContestUrl(subscriberId,
						contestId, mode, chargedAmount, transactionType);

				logger.debug("Hitting contest url. subscriberId: "
						+ subscriberId + ", circleId: " + circleId + ", mode: "
						+ mode + ", chargedAmount: " + chargedAmount
						+ ", transactionType: " + transactionType
						+ ", contestUrl: " + contestUrl);

				HttpParameters httpParam = new HttpParameters();
				httpParam.setMaxTotalConnections(200);
				httpParam.setMaxHostConnections(200);
				httpParam.setConnectionTimeout(6000);
				httpParam.setSoTimeout(6000);

				RBTHttpClient rbtHttpClient = new RBTHttpClient(httpParam);

				HttpResponse httpResponse = null;
				try {
					httpResponse = rbtHttpClient.makeRequestByGet(contestUrl,
							null);
					contestUrlResponse = httpResponse.getResponse();
				} catch (HttpException he) {
					contestUrlResponse = he.getMessage();
					logger.error(
							"Unable to make a hit to contestUrl: " + contestUrl
									+ ", HttpException: " + he.getMessage(), he);
				} catch (IOException ioe) {
					contestUrlResponse = ioe.getMessage();
					logger.error(
							"Unable to make a hit to contestUrl: " + contestUrl
									+ ", IOException: " + ioe.getMessage(), ioe);
				} catch (Exception e) {
					contestUrlResponse = e.getMessage();
					logger.error("Unable to make a hit to contestUrl: "
							+ contestUrl + ", IOException: " + e.getMessage(),
							e);
				}

				CONTEST_URL_TXN_LOG.info("|Timestamp: "
						+ System.currentTimeMillis() + "|SubscriberId: "
						+ subscriberId + "|ContestUrl: " + contestUrl
						+ "|Response: " + contestUrlResponse);

				logger.info("Successfully made a hit. contestUrlResponse: "
						+ contestUrlResponse + ", to contestUrl: " + contestUrl);
			} else {
				logger.debug("Not making a hit to contestId is "
						+ "not configured for circleId: " + circleId
						+ ", contestId: " + contestId
						+ ", circleAndContestIdMap: " + circleAndContestIdMap);
			}

		} catch (Exception e) {
			logger.error("Not made any hit to contestUrl: "
					+ contestUrlForContent + ", Exception: " + e.getMessage(),
					e);
		}
		
	}

	private static String constructContestUrl(String subscriberId,
			String contestId, String mode, String chargedAmount,
			String transactionType) {

		String contestUrl = contestUrlForContent;

		String rSubscriberId = (null != subscriberId) ? subscriberId : "";
		String rContestId = (null != contestId) ? contestId : "";
		String rMode = (null != mode) ? mode : "";
		String rChargedAmount = (null != chargedAmount) ? chargedAmount : "";
		String rTransactionType = (null != transactionType) ? transactionType
				: "";

		contestUrl = contestUrl.replaceAll("<subscriberid>", rSubscriberId);
		contestUrl = contestUrl.replaceAll("<contestid>", rContestId);
		contestUrl = contestUrl.replaceAll("<channel>", rMode);
		contestUrl = contestUrl.replaceAll("<chargedamount>", rChargedAmount);
		contestUrl = contestUrl.replaceAll("<transactiontype>",
				rTransactionType);

		logger.info("Replaced contestUrl: " + contestUrl);
		return contestUrl;
	}

	public static void main(String[] args) {
		
		//int pointsEarned = 10; // RBTContestUtils.getPointsEarned("ACT", action,
		// classType);
//		if (pointsEarned > 0) {
//			RBTContestUtils.pointsContest("9886030884", pointsEarned);
//		}

		// String s1 = "Your %TOTAl_NO_OF_POINTS%";
		// String s2 = "%TOTAl_NO_OF_POINTS%";
		//
		// pointsSmsTextForAboveThres =
		// pointsSmsTextForAboveThres.replace(R_NO_OF_POINTS_OBTAINED,
		// "asdfasdf");
		// System.out.println("pointsSmsTextForAboveThres: " +
		// pointsSmsTextForAboveThres);
		
		RBTContestUtils.hitContestUrl("9885012345", "KK", null, null, "ACT");
		
//		try {
//			sendPointsSms("9887612345",45,5);
//			sendPointsSms("9887612346",50,6);
//			sendSms("9887612345",5);
//			sendSms("9887612346",50);
//		} catch (OnMobileException e) {
//			e.printStackTrace();
//		}
	}

}
