package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants;
import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants.Severity;
import com.onmobile.apps.ringbacktones.activemonitoring.core.MonitorData;
import com.onmobile.apps.ringbacktones.activemonitoring.core.UrlResponseSampler;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.subscriptions.RBTDeamonHelperUtil;
import com.onmobile.apps.ringbacktones.subscriptions.TataGsmDateUtil;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class TataGsmDbMgrImpl extends RBTDBManager {

	private static Logger logger = Logger.getLogger(TataGsmDbMgrImpl.class);
	public static String m_DEFAULT_DOWNLOAD = null;
	public static int m_MAX_DOWNLOADS_ALLOWED = -1;
	private static String m_success = "SUCCESS";
	private static String m_failure = "FAILURE";
	private static boolean m_addDefaultSelInLoop = false;
	private static boolean m_addPersonalizedSelInLoop = false;
	private static String wdsHTTPLink = null;
	private static boolean m_useWDSMap = false;
	private static boolean m_makeWDSRequest = false;
	private static ArrayList<String> allowedSubscriberClasses = new ArrayList<String>();

	private static UrlResponseSampler urlResponseSampler = null;

	private TataGsmDateUtil tataGsmDateUtil;
	
	@Override
	public void init() {

		logger.info("inside init method");

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		Parameters param = parametersCacheManager.getParameter("COMMON",
				"MAX_DOWNLOADS_ALLOWED", "-1");
		m_MAX_DOWNLOADS_ALLOWED = Integer.parseInt(param.getValue());

		param = parametersCacheManager.getParameter("COMMON",
				"DEFAULT_DOWNLOAD");
		if (param != null)
			m_DEFAULT_DOWNLOAD = param.getValue();

		param = parametersCacheManager.getParameter("COMMON",
				"DEFAULT_SEL_IN_LOOP");
		if (param != null && param.getValue() != null)
			m_addDefaultSelInLoop = param.getValue().equalsIgnoreCase("true");

		param = parametersCacheManager.getParameter("COMMON",
				"PERSONALIZED_SEL_IN_LOOP");
		if (param != null && param.getValue() != null)
			m_addPersonalizedSelInLoop = param.getValue().equalsIgnoreCase(
					"true");

		param = CacheManagerUtil.getParametersCacheManager().getParameter(
				iRBTConstant.COMMON, "WDS_HTTP_LINK");
		if (param != null)
			wdsHTTPLink = param.getValue().trim();

		m_useWDSMap = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
				"USE_WDS_CIRCLE_MAP", "FALSE");
		m_makeWDSRequest = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON, "MAKE_WDS_REQUEST", "FALSE");

		String allowedSubClass = getAllowedSubscriberClass();
		if (allowedSubClass != null) {
			StringTokenizer paramTokenizer = new StringTokenizer(
					allowedSubClass, ",");
			while (paramTokenizer.hasMoreTokens())
				allowedSubscriberClasses.add(paramTokenizer.nextToken());
		}
		urlResponseSampler = UrlResponseSampler.getInstance();
		;
		logger.info("allowedSubscriberClasses : " + allowedSubscriberClasses);
	}

	@Override
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType,
			String selInterval) {
		return addSubscriberSelections(subscriberID, callerID, categories,
				clipMap, setTime, startTime, endTime, status, selectedBy,
				selectionInfo, freePeriod, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, null,
				incrSelCount, false, null, OptIn, false, inLoop, subClass, sub,
				0, selInterval, null, false, null, false);
	}

	@Override
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime,
			int toTime, String chargeClassType, boolean smActivation,
			boolean doTODCheck, String mode, String regexType, String subYes,
			String promoType, String circleID, boolean incrSelCount,
			boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType,
			String selInterval, HashMap extraInfo, boolean useUIChargeClass,
			String refID, boolean isDirectActivation) {

		Connection conn = getConnection();
		if (conn == null)
			return null;

		int count = 0;
		try {
			String wdsQueryResult = null;
			if (extraInfo != null
					&& extraInfo.containsKey(EXTRA_INFO_WDS_QUERY_RESULT)
					&& extraInfo.get(EXTRA_INFO_WDS_QUERY_RESULT) != null) {
				wdsQueryResult = (String) extraInfo
						.get(EXTRA_INFO_WDS_QUERY_RESULT);
				extraInfo.remove(EXTRA_INFO_WDS_QUERY_RESULT);
			}

			String mappedCircleID = null;
			String wdsAllow = null;
			String wdsResult = null;
			HashMap<String, String> subscriberInfo = null;
			if (m_makeWDSRequest) {
				if (sub != null && wdsQueryResult == null)
					subscriberInfo = getSubscriberInfo(subscriberID);
				else if (wdsQueryResult != null)
					subscriberInfo = getSubscriberInfo(subscriberID,
							wdsQueryResult);
				if (subscriberInfo != null) {
					if (subscriberInfo.containsKey("USER_TYPE")
							&& subscriberInfo.get("USER_TYPE") != null)
						isPrepaid = subscriberInfo.get("USER_TYPE")
								.equalsIgnoreCase("PREPAID");
					if (subscriberInfo.containsKey("CIRCLE_ID")
							&& subscriberInfo.get("CIRCLE_ID") != null)
						mappedCircleID = subscriberInfo.get("CIRCLE_ID");
					if (subscriberInfo.containsKey("STATUS")
							&& subscriberInfo.get("STATUS") != null)
						wdsAllow = subscriberInfo.get("STATUS");
					if (subscriberInfo.containsKey("WDS_RESPONSE")
							&& subscriberInfo.get("WDS_RESPONSE") != null)
						wdsResult = subscriberInfo.get("WDS_RESPONSE");
				}
				if (wdsAllow == null
						|| (wdsAllow != null && !wdsAllow
								.equalsIgnoreCase("VALID")))
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				if (wdsResult == null)
					return SELECTION_FAILED_WDS_FAILED;
				if (m_useWDSMap && mappedCircleID == null)
					return SELECTION_FAILED_WDS_FAILED;
			}
			if (m_useWDSMap)
				circleID = mappedCircleID;
			else
				circleID = getCircleId(subscriberID);

			char prepaidYes = 'n';
			if (sub != null && sub.prepaidYes() != isPrepaid)
				sub.setPrepaidYes(isPrepaid);
			prepaidYes = isPrepaid ? 'y' : 'n';

			int categoryID = categories.id();
			categories = getCategory(categoryID, circleID, prepaidYes);

			Date nextChargingDate = null;
			Date startDate = startTime;
			String selectInfo = selectionInfo;
			String sel_status = STATE_BASE_ACTIVATION_PENDING;
			int nextPlus = -1;
			boolean updateEndDate = false;
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null
					&& subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (categories != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(categories.type()))
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;

			if (selInterval != null && selInterval.indexOf(",") != -1) {
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while (stk.hasMoreTokens())
					days.add(stk.nextToken());

				if (days.size() == 7)
					selInterval = null;
				else {
					Collections.sort(days);
					selInterval = "";
					for (int i = 0; i < days.size(); i++) {
						selInterval = selInterval + days.get(i);
						if (i != days.size() - 1)
							selInterval = selInterval + ",";
					}
				}
			}

			if (sub != null && rbtType != 2)
				rbtType = sub.rbtType();
			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				if (logger.isInfoEnabled())
					logger.info(subscriberID
							+ " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection)
				isSelSuspended = isSelSuspended(subscriberID, callerID);
			if (isSelSuspended) {
				if (logger.isInfoEnabled())
					logger.info("selection of " + subscriberID + " for "
							+ callerID + " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}

			Date endDate = endTime;
			if (endDate == null)
				endDate = m_endDate;

			// If chargeClassType is null, then useUIChargeClass parameter will
			// be ignored
			useUIChargeClass = useUIChargeClass && chargeClassType != null;

			String classType = "DEFAULT";
			if (useUIChargeClass)
				classType = chargeClassType;
			else if (categories != null)
				classType = categories.classType();

			Date clipEndTime = null;
			String clipGrammar = null;
			String clipClassType = null;
			String subscriberWavFile = null;
			String slice_duration = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
				if(clipMap.containsKey(WebServiceConstants.param_slice_duration))
					slice_duration = (String)clipMap.get(WebServiceConstants.param_slice_duration);
			}

			if (subscriberWavFile == null) {
				if (status != 90)
					return SELECTION_FAILED_NULL_WAV_FILE;
				subscriberWavFile = "CRICKET";
			}

			if (subYes != null
					&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
							.equalsIgnoreCase(STATE_EVENT))) {
				if (!isPackActivationPendingForContent(sub, categories,
						subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(subClass)) {
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				if (clipEndTime.getTime() < System.currentTimeMillis())
					return SELECTION_FAILED_CLIP_EXPIRED;
				if (categories != null
						&& (categories.type() == DAILY_SHUFFLE || categories
								.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT")
						&& classType != null
						&& !clipClassType.equalsIgnoreCase(classType)) {
					ChargeClass catCharge = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									classType);
					ChargeClass clipCharge = CacheManagerUtil
							.getChargeClassCacheManager().getChargeClass(
									clipClassType);

					if (catCharge != null && clipCharge != null
							&& catCharge.getAmount() != null
							&& clipCharge.getAmount() != null) {
						try {
							int firstAmount = Integer.parseInt(catCharge
									.getAmount());
							int secondAmount = Integer.parseInt(clipCharge
									.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType
													.toLowerCase())))
								classType = clipClassType;
						} catch (Throwable e) {
						}
					}
					if (clipClassType.startsWith("TRIAL") && categories != null
							&& categories.id() != 26)
						classType = clipClassType;
				}
			}
			if (!useUIChargeClass && chargeClassType != null) {
				ChargeClass first = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(
								chargeClassType);

				if (first != null && second != null
						&& first.getAmount() != null
						&& second.getAmount() != null) {
					try {
						int firstAmount = Integer.parseInt(first.getAmount());
						int secondAmount = Integer.parseInt(second.getAmount());
						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType
										.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType
										.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses
										.contains(chargeClassType.toLowerCase())))
							classType = chargeClassType;
					} catch (Throwable e) {
						classType = chargeClassType;
					}
				} else
					classType = chargeClassType;

				if (first != null && first.getChargeClass().startsWith("TRIAL")
						&& categories != null && categories.id() != 26)
					classType = first.getChargeClass();
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy))
				classType = (String) m_modeChargeClass.get(selectedBy);

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null)
					selectInfo = selectInfo + ":" + "viral";
			}
			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			String afterTrialClassType = "DEFAULT";
			if (OptIn)
				afterTrialClassType = "DEFAULT_OPTIN";

			/**
			 * If user enabled UDS , then all his selections should go in Loop
			 */
			if (!inLoop) {
				HashMap<String, String> subExtraInfoMap = DBUtility
						.getAttributeMapFromXML(sub.extraInfo());
				if (subExtraInfoMap != null
						&& subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN))
							.equalsIgnoreCase("TRUE");
			}

			if (selInterval != null && status != 80) {

				if (selInterval.startsWith("W") || selInterval.startsWith("M"))
					status = 75;

				if (selInterval.startsWith("Y")) {

					status = 95;
					String date = selInterval.substring(1);
					Date parseDate = null;
					if (date.length() == 8) {
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"ddMMyy");
						Date currentDate = new Date();
						parseDate = dateFormat.parse(date);
						if (parseDate.before(currentDate)
								|| parseDate.equals(currentDate))
							return SELECTION_FAILED_INVALID_PARAMETER;
						Calendar cal = Calendar.getInstance();
						cal.setTime(parseDate);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						endDate = cal.getTime();
					}
					if (date.length() == 4)
						endDate = m_endDate;
				}
			}

			// Added for checking the selection limit
			/**
			 * Since Sprint 4 RBT 2.0, RBT 15670
			 * One more parameter udpId has been added in
			 * getSubscriberSelections method. If udpId is present then 
			 * query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*if(extraInfo.containsKey(WebServiceConstants.param_udpId))
			udpId = (String) extraInfo.get(UDP_ID);*/
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl
					.getSubscriberSelections(conn, subID(subscriberID),
							subID(callerID), rbtType, udpId);
            
			String sliceRBTWavFile = null;
			if(slice_duration!=null){
				Clip clip = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
						.getInstance().getClipByRbtWavFileName(subscriberWavFile);
				if(clip!=null)
				      sliceRBTWavFile = "rbt_slice_" + clip.getClipId()+ "_" + slice_duration + "_rbt";
			}
			
			String subscriberWavFileStr = subscriberWavFile;
			if(sliceRBTWavFile!=null){
				subscriberWavFileStr = sliceRBTWavFile;
			}
			/* time of the day changes */
			SubscriberStatus subscriberStatus = getAvailableSelection(conn,
					subID(subscriberID), subID(callerID), subscriberSelections,
					categories, subscriberWavFileStr, status, fromTime, toTime,
					startDate, endDate, doTODCheck, inLoop, rbtType,
					selInterval, selectedBy);
			
			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
				if (m_addDefaultSelInLoop && callerID == null)
					inLoop = true;
				if (m_addPersonalizedSelInLoop && callerID != null)
					inLoop = true;
				if (inLoop
						&& (categories.type() == SHUFFLE || status == 90
								|| status == 99 || status == 0)
						&& !m_putSGSInUGS)
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(
						conn, subID(subscriberID), subID(callerID), status,
						rbtType);
				if (subscriberStatus != null) {
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE
							&& !m_putSGSInUGS)
						inLoop = false;
				}
				// else
				// inLoop = false; // this else will make all first callerID
				// selection as override :), not needed actually

				// @added by sreekar if user's last selection is a trail
				// selection his next selection should override the old one
				char loopStatus = getLoopStatusForNewSelection(inLoop,
						subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null)
					actBy = sub.activatedBy();
				if (m_trialChangeSubTypeOnSelection && actBy != null
						&& actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;
						if (!convertSubscriptionTypeTrial(subID(subscriberID),
								subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				boolean isPackSel = false;
				String packCosID = null;
				if (m_overridableCategoryTypes.contains("" + categories.type())
						|| m_overridableSelectionStatus.contains("" + status))
					incrSelCount = false;
				else if (!useUIChargeClass) {
					String subPacks = null;
					HashMap<String, String> subExtraInfoMap = DBUtility
							.getAttributeMapFromXML(sub.extraInfo());
					if (subExtraInfoMap != null
							&& subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;
					if (subPacks != null) {
						com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getCategory(categories.id());
						Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getClipByRbtWavFileName(
										subscriberWavFile);
						CosDetails cosDetail = getCosDetailsForContent(
								subscriberID, subPacks, category, clipObj,
								status, callerID);
						List<ProvisioningRequests> packList = null;
						if (cosDetail != null) {
							packList = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(
											subscriberID, Integer
													.parseInt(cosDetail
															.getCosId()));
						}
						if (packList != null
								&& (isSubscriberPackActivated(packList.get(0)) || isSubscriberPackActivationPending(packList
										.get(0)))) {
							int selCount = sub.maxSelections();
							if (isPackRequest(cosDetail)) {
								selCount = packList.get(0)
										.getNumMaxSelections();
								if (cosDetail.getFreeSongs() > selCount)
									isPackSel = true;
							}

							nextClass = getChargeClassFromCos(cosDetail,
									selCount);
							packCosID = cosDetail.getCosId();
						} else {
							nextClass = getNextChargeClass(sub);
						}
					} else {
						nextClass = getNextChargeClass(sub);
					}

					if (nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if (!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}

				if (!useUIChargeClass) {
					if (status == 80 && rbtType == 2)
						classType = clipClassType;
					else {
						for (int i = 0; subscriberSelections != null
								&& i < subscriberSelections.length; i++) {
							if (subscriberSelections[i].selType() == 2) {
								HashMap selectionExtraInfo = DBUtility
										.getAttributeMapFromXML(subscriberSelections[i]
												.extraInfo());
								int campaignId = -1;
								if (selectionExtraInfo != null
										&& selectionExtraInfo
												.containsKey(iRBTConstant.CAMPAIGN_ID)
										&& selectionExtraInfo
												.get(iRBTConstant.CAMPAIGN_ID) != null) {
									try {
										campaignId = Integer
												.parseInt(""
														+ selectionExtraInfo
																.get(iRBTConstant.CAMPAIGN_ID));
									} catch (Exception e) {
										campaignId = -1;
									}
								}
								logger.info("The value of campaign id - "
										+ campaignId);
								if (campaignId != -1) {
									RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO
											.getRBTBulkUploadTask(campaignId);
									if (m_corporateDiscountChargeClass != null
											&& m_corporateDiscountChargeClass
													.containsKey(bulkUploadTask
															.getTaskMode())) {
										logger.info("The value of m_corporateDiscountChargeClass id - "
												+ m_corporateDiscountChargeClass
														.toString());
										HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass
												.get(bulkUploadTask
														.getTaskMode());
										if (discountClassMap != null
												&& discountClassMap
														.containsKey(classType))
											classType = (String) discountClassMap
													.get(classType);
									}
								}
								break;
							}
						}
					}
				}

				String checkSelStatus = checkSelectionLimit(
						subscriberSelections, subID(callerID), inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				// Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil
						.getParametersCacheManager().getParameter("COMMON",
								"SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if (parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn,
						subID(subscriberID), subID(callerID), status, fromTime,
						toTime, graceDeselectedBy, rbtType);

				try {
					tataGsmDateUtil = (TataGsmDateUtil) ConfigUtil.getBean(BeanConstant.TATA_GSM_DATE_UTIL);
				} catch (Throwable e) {
					logger.error("Exception : "+e.getMessage(), e);
				}
				if(tataGsmDateUtil!=null && status != iRBTConstant.PROFILE_SEL_TYPE){
					endDate = getUpdatedEndDate(clipEndTime,categories);
				}
				count = createSubscriberStatus(subscriberID, callerID,
						categories.id(), subscriberWavFile, setTime, startDate,
						endDate, status, selectedBy, selectInfo,
						nextChargingDate, prepaid, classType, changeSubType,
						fromTime, toTime, sel_status, true, clipMap,
						categories.type(), useDate, loopStatus, isTata,
						nextPlus, rbtType, selInterval, extraInfo, refID,
						isDirectActivation, circleID, sub, useUIChargeClass,
						false);
				logger.info("Checking to update num max selections or not."
						+ " count: " + count + ", isPackSel: " + isPackSel
						+ " incrSelCount: " + incrSelCount);
				if (incrSelCount && isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn,
							subscriberID, packCosID);
				else if (incrSelCount && count == 1)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));

				if (updateEndDate)
					SubscriberImpl.updateEndDate(conn, subID(subscriberID),
							endDate, null);
			} else
				return SELECTION_FAILED_SELECTION_OVERLAP;
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return count > 0 ? SELECTION_SUCCESS : SELECTION_FAILED_INTERNAL_ERROR;
	}

	@Override
	public boolean isValidPrefix(String subscriberID) {

		if (subscriberID == null || subscriberID.length() < 7
				|| subscriberID.length() > 15)
			return false;
		else {
			try {
				Long.parseLong(subID(subscriberID));
			} catch (Throwable e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval) {

		HashMap dummy = new HashMap();
		dummy = null;

		return addSubscriberSelections(subscriberID, callerID, categoryID,
				subscriberWavFile, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, 0, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, incrSelCount,
				OptIn, inLoop, subClass, subscriber, selInterval, dummy);
	}

	@Override
	public boolean addSubscriberSelections(String subscriberID,
			String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType,
			boolean incrSelCount, boolean OptIn, boolean inLoop,
			String subClass, Subscriber subscriber, String selInterval,
			HashMap extraInfo) {
		Subscriber sub = getSubscriber(subscriberID);
		String mappedCircleID = null;
		String wdsAllow = null;
		String wdsResult = null;
		HashMap<String, String> subscriberInfo = null;
		if (m_makeWDSRequest) {
			if (sub != null) {
				if (extraInfo == null
						|| (extraInfo != null && extraInfo
								.get(EXTRA_INFO_WDS_QUERY_RESULT) == null)) {
					subscriberInfo = getSubscriberInfo(subscriberID);
				} else {

					subscriberInfo = getSubscriberInfo(subscriberID,
							(String) extraInfo.get(EXTRA_INFO_WDS_QUERY_RESULT));
					extraInfo.remove(EXTRA_INFO_WDS_QUERY_RESULT);
				}

				if (subscriberInfo != null) {
					if (subscriberInfo.containsKey("USER_TYPE")
							&& subscriberInfo.get("USER_TYPE") != null)
						isPrepaid = subscriberInfo.get("USER_TYPE")
								.equalsIgnoreCase("PREPAID");

					if (subscriberInfo.containsKey("CIRCLE_ID")
							&& subscriberInfo.get("CIRCLE_ID") != null)
						mappedCircleID = subscriberInfo.get("CIRCLE_ID");

					if (subscriberInfo.containsKey("STATUS")
							&& subscriberInfo.get("STATUS") != null)
						wdsAllow = subscriberInfo.get("STATUS");

					if (subscriberInfo.containsKey("WDS_RESPONSE")
							&& subscriberInfo.get("WDS_RESPONSE") != null)
						wdsResult = subscriberInfo.get("WDS_RESPONSE");
				}
				if (wdsAllow == null
						|| (wdsAllow != null && !wdsAllow
								.equalsIgnoreCase("VALID"))) {
					return false;
				}

				if (wdsResult == null) {
					return false;
				}

				if (m_useWDSMap && mappedCircleID == null) {
					return false;
				}

			}
		}
		String circleID = null;
		if (m_useWDSMap)
			circleID = mappedCircleID;
		else
			circleID = getCircleId(subscriberID);
		char prepaidYes = 'n';

		if (sub.prepaidYes() != isPrepaid)
			sub.setPrepaidYes(isPrepaid);

		prepaidYes = isPrepaid ? 'y' : 'n';

		Categories categories = getCategory(categoryID, circleID, prepaidYes);
		ClipMinimal clips = getClipRBT(subscriberWavFile);
		HashMap clipMap = new HashMap();
		if (clips != null) {
			clipMap.put("CLIP_CLASS", clips.getClassType());
			clipMap.put("CLIP_END", clips.getEndTime());
			clipMap.put("CLIP_GRAMMAR", clips.getGrammar());
			clipMap.put("CLIP_WAV", clips.getWavFile());
			clipMap.put("CLIP_ID", "" + clips.getClipId());
			clipMap.put("CLIP_NAME", clips.getClipName());
		} else {
			clipMap.put("CLIP_WAV", subscriberWavFile);
		}
		String ret = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				null, incrSelCount, false, null, OptIn, false, inLoop,
				subClass, subscriber, 0, selInterval, extraInfo, false, null,
				false);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}

	@Override
	public String deactivateSubscriber(Connection conn, String subscriberID,
			String deactivate, Date date, boolean delSelections,
			boolean sendToHLR, boolean smDeactivation, boolean isDirectDeact,
			boolean checkSubClass, int rbtType, Subscriber sub, String dctInfo,
			String userInfoXml) {
		String ret = null;
		if (conn == null)
			return null;
		try {
			logger.info("Testing deactivate Subscriber inside try");
			boolean deact = true;
			boolean success = false;
			if (sub == null || sub.subYes() == null)
				return null;

			String subYes = sub.subYes();
			if (!(subYes.equalsIgnoreCase("B") || subYes.equalsIgnoreCase("O")
					|| subYes.equals("Z") || subYes.equals("z") || subYes
						.equalsIgnoreCase("G")))
				ret = "ACT_PENDING";
			else if (checkSubClass) {
				logger.info("Testing deactivate Subscriber inside sub!=null");
				SubscriptionClass temp = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(sub.subscriptionClass());
				if (temp != null && temp.isDeactivationNotAllowed())
					ret = "DCT_NOT_ALLOWED";
			}

			if (sub.rbtType() == TYPE_RBT_RRBT
					|| sub.rbtType() == TYPE_SRBT_RRBT)
				convertSubscriptionType(subID(subscriberID),
						sub.subscriptionClass(), "DEFAULT", null, rbtType,
						true, null, sub);
			else {
				success = SubscriberImpl.deactivate(conn, subID(subscriberID),
						deactivate, date, sendToHLR, smDeactivation, false,
						isDirectDeact, m_isMemCachePlayer, dctInfo, sub,
						userInfoXml); // dctInfo= null for all apart from airtel
				if (success)
					SubscriberStatusImpl.deactivate(conn, subID(subscriberID),
							date, smDeactivation, false, deactivate, rbtType);
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return ret == null ? "SUCCESS" : ret;
	}

	// RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo,
			String circleId, String refId, boolean isComboRequest,
			Map<String, String> xtraParametersMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		Subscriber subscriber = null;
		try {
			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
			String wdsAllow = null;
			String wdsResult = null;
			String mappedCircleID = null;
			String wdsQueryResult = null;

			if (extraInfo != null
					&& extraInfo.get(EXTRA_INFO_WDS_QUERY_RESULT) != null)
				wdsQueryResult = (String) extraInfo
						.get(EXTRA_INFO_WDS_QUERY_RESULT);

			HashMap<String, String> subscriberInfo = new HashMap();
			if (m_makeWDSRequest) {
				if (wdsQueryResult == null) {
					subscriberInfo = getSubscriberInfo(subscriberID);
					if (subscriberInfo.get("WDS_RESPONSE") != null)
						extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT,
								subscriberInfo.get("WDS_RESPONSE"));
				} else if (wdsQueryResult != null)
					subscriberInfo = getSubscriberInfo(subscriberID,
							wdsQueryResult);
				if (subscriberInfo != null) {
					if (subscriberInfo.containsKey("USER_TYPE")
							&& subscriberInfo.get("USER_TYPE") != null)
						isPrepaid = subscriberInfo.get("USER_TYPE")
								.equalsIgnoreCase("PREPAID");
					if (subscriberInfo.containsKey("CIRCLE_ID")
							&& subscriberInfo.get("CIRCLE_ID") != null)
						mappedCircleID = subscriberInfo.get("CIRCLE_ID");
					if (subscriberInfo.containsKey("STATUS")
							&& subscriberInfo.get("STATUS") != null)
						wdsAllow = subscriberInfo.get("STATUS");
					if (subscriberInfo.containsKey("WDS_RESPONSE")
							&& subscriberInfo.get("WDS_RESPONSE") != null)
						wdsResult = subscriberInfo.get("WDS_RESPONSE");
				}
				if (wdsAllow == null
						|| (wdsAllow != null && !wdsAllow
								.equalsIgnoreCase("VALID"))) {
					logger.info("subscriber status invalid as from WDS. Returning subscriber as null");
					return null;
				}
				if (wdsResult == null)
					return null;
				if (m_useWDSMap && mappedCircleID == null)
					return null;
				// RBT-12024
				else if (mappedCircleID != null && m_useWDSMap) {
					circleId = mappedCircleID;
				}

			}
			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";
			if (!isTNBNewFlow && classType != null
					&& tnbSubscriptionClasses.contains(classType)
					&& endDate == null) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, +1);
				endDate = endCal.getTime();
			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
			}
			if (cos == null)
				cos = super.getCos(null, subscriberID, subscriber, circleId,
						isPrepaid ? "y" : "n", activate, classType);
			if (cos != null && !cos.isDefaultCos()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays() - 1);
				endDate = cal.getTime();
			}

			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;
			Date nextChargingDate = null;
			Date activationDate = null;
			if (isDirectActivation) {
				subscription = STATE_ACTIVATED;
				nextChargingDate = Calendar.getInstance().getTime();
				activationDate = Calendar.getInstance().getTime();
			}

			String cosID = null;
			if (cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";
			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();
				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null)
					activationInfo = activationInfo + ":" + "viral";
			}

			if (cos != null && !cos.isDefaultCos())
				if ((subscriptionClass == null || subscriptionClass
						.equalsIgnoreCase("DEFAULT"))
						&& cos.getSubscriptionClass() != null
						&& !cos.getSubscriptionClass().equalsIgnoreCase(
								"DEFAULT"))
					subscriptionClass = cos.getSubscriptionClass();

			boolean isRequestForConsent = true;
			if (null != extraInfo
					&& extraInfo.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				isRequestForConsent = false;
			}

			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);

			String finalRefID = UUID.randomUUID().toString();
			// Added for JIRA-RBT-6321
			if (isDirectActivation && refId != null)
				finalRefID = refId;

			if (isRequestForConsent
					&& (subscriber == null || subscriber.subYes().equals("X"))) {
				Subscriber tempSubscriber = checkModeAndInsertIntoConsent(
						subscriberID, activate, startDate, endDate,
						isDirectActivation, rbtType, conn, prepaid,
						subscription, activationInfo, cosID, subscriptionClass,
						finalRefID, extraInfo, circleId, isComboRequest,
						xtraParametersMap);

				if (tempSubscriber != null) {
					return tempSubscriber;
				}
			}

			if (subscriber != null) {
				String subsciptionYes = subscriber.subYes();
				if (!isDirectActivation
						&& subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {
						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);
						}
					}
					return subscriber;
				}
				if (!isDirectActivation
						&& (subsciptionYes.equals("D")
								|| subsciptionYes.equals("P")
								|| subsciptionYes.equals("F")
								|| subsciptionYes.equals("x")
								|| subsciptionYes.equals("Z") || subsciptionYes
									.equals("z")))
					return null;

				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();

				boolean success = SubscriberImpl.update(conn,
						subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, nextChargingDate, 0,
						activationInfo, subscriptionClass, deactivatedBy,
						deactivationDate, activationDate, subscription, 0,
						cosID, cosID, rbtType, subscriber.language(),
						subExtraInfo, circleId, finalRefID, isDirectActivation);
				if (startDate == null)
					startDate = new Date(System.currentTimeMillis());

				if (success) {
					subscriber = new SubscriberImpl(subID(subscriberID),
							activate, null, startDate, m_endDate, prepaid,
							null, nextChargingDate, 0, activationInfo,
							subscriptionClass, subscription, deactivatedBy,
							deactivationDate, activationDate, 0, cosID, cosID,
							rbtType, subscriber.language(),
							subscriber.oldClassType(), subExtraInfo, circleId,
							finalRefID);
				} else
					subscriber = null;
			} else {
				// Third Party confirmation chages
				/*
				 * if(isRequestForConsent) { //RBT-9873 Added xtraParametersMap
				 * for CG flow subscriber =
				 * checkModeAndInsertIntoConsent(subscriberID, activate,
				 * startDate, endDate, isDirectActivation, rbtType, conn,
				 * prepaid, subscription, activationInfo, cosID,
				 * subscriptionClass, finalRefID, subExtraInfo, circleId,
				 * isComboRequest,xtraParametersMap); }
				 */
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, startDate, endDate, prepaid, null,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						null, null, activationDate, subscription, 0, cosID,
						cosID, rbtType, null, isDirectActivation, subExtraInfo,
						circleId, finalRefID);
			}

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	@Override
	public Subscriber trialActivateSubscriber(String subscriberID,
			String activate, Date date, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, String selClass,
			String subscriptionType, String circleId) {
		int rbtType = 0;
		Connection conn = getConnection();
		if (conn == null)
			return null;
		Subscriber subscriber = null;
		try {
			String mappedCircleID = null;
			String wdsResult = null;
			if (m_makeWDSRequest) {
				wdsResult = queryWDS(subscriberID);
				if (wdsResult != null) {
					wdsResult = wdsResult.trim();
					StringTokenizer wdsST = new StringTokenizer(wdsResult, "|");
					if (wdsST.countTokens() >= 13) {
						String tempString = null;
						for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++) {
							tempString = wdsST.nextToken();
							if (tokenCount == 3) {
								if (tempString.startsWith("Pre")) {
									logger.info("RBT::User is prepaid as from WDS");
									isPrepaid = true;
								} else {
									logger.info("RBT::User is postpaid as from WDS");
									isPrepaid = false;
								}
							} else if (tokenCount == 5) {
								if (tempString.equals("1")) {
									logger.info("RBT::can allow subscriber as from WDS");
								} else {
									logger.info("RBT::cannot allow subscriber as from WDS");
									return null;
								}
							} else if (tokenCount == 9) {
								if (m_useWDSMap) {
									String circleID = tempString;
									mappedCircleID = "Default";
									if (circleID != null) {
										mappedCircleID = getMappedCircleID(circleID);
										if (mappedCircleID
												.equalsIgnoreCase("Default")) {
											logger.info("No circle id mapped for "
													+ circleID);
											return null;
										}
									}
									break;
								}
							}
						}

					} else {
						logger.info("WDS query returned less tokens than expected");
						return null;
					}
				} else {
					logger.info("WDS query returned null");
					return null;
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			Date endDate = null;
			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));

			if (activate.equalsIgnoreCase("TNB") && classType != null
					&& classType.equalsIgnoreCase("ZERO")) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(endDate);
				endCal.add(Calendar.DATE, -1);
				endDate = endCal.getTime();

			} else if (m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(classType)) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(classType);
				endDate = getNextDate(subClass.getSubscriptionPeriod());

			} else if (selClass != null && selClass.startsWith("TRIAL")) {
				ChargeClass chargeClass = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(selClass);
				if (chargeClass != null
						&& chargeClass.getSelectionPeriod() != null
						&& chargeClass.getSelectionPeriod().startsWith("D")) {
					String selectionPeriod = chargeClass.getSelectionPeriod()
							.substring(1);
					if ("OPTIN".equalsIgnoreCase(subscriptionType)) {
						Calendar endCal = Calendar.getInstance();
						endCal.add(Calendar.DATE,
								Integer.parseInt(selectionPeriod) - 1);
						endDate = endCal.getTime();
						if (actInfo == null)
							actInfo = ":optin:";
						else
							actInfo = actInfo + ":optin:";

					} else if ("OPTOUT".equalsIgnoreCase(subscriptionType)) {
						endDate = m_endDate;
					}
				}
			} else {
				endDate = m_endDate;
			}

			Date nextChargingDate = null;
			Date lastAccessDate = null;
			Date activationDate = null;
			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;

			if (m_useWDSMap)
				circleId = mappedCircleID;

			CosDetails cos = getCos(null, subscriberID, subscriber, circleId,
					isPrepaid ? "y" : "n", activate, classType);

			if (cos != null && !cos.isDefaultCos()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, cos.getValidDays() - 1);
				endDate = cal.getTime();
				if (endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}
			String cosID = null;
			if (cos != null)
				cosID = cos.getCosId();

			String subscriptionClass = classType;
			if (classType == null)
				subscriptionClass = "DEFAULT";

			SubscriberPromo subscriberPromo = SubscriberPromoImpl
					.getActiveSubscriberPromo(conn, subID(subscriberID),
							"ICARD");
			if (subscriberPromo != null) {
				if (subscriberPromo.activatedBy() != null)
					subscriptionClass = subscriberPromo.activatedBy();

				SubscriberPromoImpl
						.endPromo(conn, subID(subscriberID), "ICARD");
			}

			if (activate != null && !activate.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null) {
					activationInfo = activationInfo + ":" + "viral";
				}
			}

			if (subscriber != null) {
				String subsciptionYes = subscriber.subYes();
				if (subscriber.endDate().getTime() > getDbTime(conn)) {
					if (subsciptionYes.equals("B")
							&& (subscriber.rbtType() == TYPE_RBT
									|| subscriber.rbtType() == TYPE_RRBT || subscriber
									.rbtType() == TYPE_SRBT)
							&& subscriber.rbtType() != rbtType) {
						if ((subscriber.rbtType() == TYPE_RBT && rbtType != TYPE_SRBT)
								|| (subscriber.rbtType() == TYPE_SRBT && rbtType != TYPE_RBT)) {

						} else {
							if (subscriber.rbtType() == TYPE_RBT)
								rbtType = TYPE_RBT_RRBT;
							else if (subscriber.rbtType() == TYPE_SRBT)
								rbtType = TYPE_SRBT_RRBT;
							convertSubscriptionType(subID(subscriberID),
									subscriber.subscriptionClass(),
									m_comboSubClass, null, rbtType, true, null,
									subscriber);
						}
					}
					return subscriber;
				}

				// String subsciptionYes = subscriber.subYes();
				if (subsciptionYes.equals("D") || subsciptionYes.equals("P")
						|| subsciptionYes.equals("F")
						|| subsciptionYes.equals("x")
						|| subsciptionYes.equals("Z")
						|| subsciptionYes.equals("z")) {
					return null;
				}

				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();

				String refID = UUID.randomUUID().toString();
				SubscriberImpl.update(conn, subID(subscriberID), activate,
						null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						deactivatedBy, deactivationDate, activationDate,
						subscription, 0, cosID, cosID, 0,
						subscriber.language(), null, circleId, refID, false);
				Date startDate = date;
				if (date == null)
					startDate = new Date(System.currentTimeMillis());
				subscriber = new SubscriberImpl(subID(subscriberID), activate,
						null, startDate, m_endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						subscription, deactivatedBy, deactivationDate,
						activationDate, 0, cosID, cosID, rbtType,
						subscriber.language(), subscriber.oldClassType(), null,
						circleId, refID);
				if (subscriber != null) {
					if (m_makeWDSRequest)
						updateExtraInfo(subID(subscriberID),
								EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
				}
			} else {
				// Third Party confirmation chages
				String refID = UUID.randomUUID().toString();
				// RBT-9873 Added xtraParametersMap for CG flow
				subscriber = checkModeAndInsertIntoConsent(subscriberID,
						activate, date, endDate, false, rbtType, conn, prepaid,
						subscription, activationInfo, cosID, subscriptionClass,
						refID, null, circleId, false,
						new HashMap<String, String>());
				if (null == subscriber) {
					subscriber = SubscriberImpl.insert(conn,
							subID(subscriberID), activate, null, date, endDate,
							prepaid, lastAccessDate, nextChargingDate, 0,
							activationInfo, subscriptionClass, null, null,
							activationDate, subscription, 0, cosID, cosID,
							rbtType, null, false, null, circleId, null);
				}
				if (subscriber != null) {
					if (m_makeWDSRequest)
						updateExtraInfo(subID(subscriberID),
								EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

	@Override
	public boolean retailerCheck(String strSubID) {
		Retailer ret = getRetailer(strSubID, "RETAILER");
		return ret != null ? true : false;
	}

	public boolean doRetailerCheck() {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		boolean retVal = false;
		try {
			Parameters cp = CacheManagerUtil.getParametersCacheManager()
					.getParameter("COMMON", "DO_RETAILER_CHECK");
			if (cp != null && cp.getValue() != null) {
				try {
					retVal = cp.getValue().equalsIgnoreCase("TRUE");
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return retVal;
	}

	@Override
	public String queryWDS(String subscriberId) {

		String m_class = "TataGsmDBMgrImpl";
		String wdsHttpQuery = null;
		String result = null;

		try {

			wdsHttpQuery = wdsHTTPLink;// + "&mdn=" + subscriberId;
			logger.info("query is " + wdsHttpQuery);

			HashMap params = new HashMap();
			params.put("MDN", subscriberId);
			HttpParameters httpParameters = new HttpParameters(wdsHttpQuery);
			String wdsResult = RBTHTTPProcessing.postFile(httpParameters,
					params, null);

			result = wdsResult;

			logger.info("result for " + subscriberId + "is " + wdsResult);

		} catch (Exception e) {
			logger.error("", e);
			result = null;
		}

		Severity severity = Severity.CLEAR;
		String message = null;
		if (result == null) {
			message = "Couldn't establish connection to remote url";
			severity = Severity.CRITICAL;
		}
		urlResponseSampler.recordUrlResponse(new MonitorData(
				AMConstants.THIRDPARTY, wdsHttpQuery, severity, message));

		return result;
	}

	@Override
	public HashMap<String, String> getSubscriberInfo(String subscriberID,
			String result) {

		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();

		subscriberInfoMap.put("STATUS", "VALID");
		subscriberInfoMap.put("CIRCLE_ID", null);
		subscriberInfoMap.put("USER_TYPE", "POSTPAID");
		subscriberInfoMap.put("WDS_RESPONSE", null);
		try {
			if (m_makeWDSRequest) {
				String mappedCircleID = null;
				String operatorName = null;
				if (result != null) {
					result = result.trim();
					subscriberInfoMap.put("WDS_RESPONSE", result);
					StringTokenizer wdsST = new StringTokenizer(result, "#");
					if (wdsST.countTokens() >= 13) {
						String tempString = null;
						for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++) {
							tempString = wdsST.nextToken();
							if (tokenCount == 4) {
								if (tempString != null
										&& Integer.parseInt(tempString) == 2) {
									logger.info("RBT::User is prepaid as from WDS");
									subscriberInfoMap.put("USER_TYPE",
											"PREPAID");
								} else {
									if (tempString != null
											&& Integer.parseInt(tempString) == 1) {
										logger.info("RBT::User is postpaid as from WDS");
										subscriberInfoMap.put("USER_TYPE",
												"POSTPAID");
									} else {
										subscriberInfoMap
												.put("STATUS", "ERROR");
									}
								}
							} else if (tokenCount == 5) {
								if (tempString.equals("1")) {
									logger.info("RBT::can allow subscriber as from WDS");
									subscriberInfoMap.put("STATUS", "VALID");
								} else {
									logger.info("RBT::cannot allow subscriber as from WDS");
									subscriberInfoMap.put("STATUS", "INVALID");
								}
							} else if (tokenCount == 8) {
								if (!allowedSubscriberClasses
										.contains(tempString)) {
									logger.info("RBT::subscriber class from WDS->"
											+ tempString
											+ ". Not allowing as allowed cases are "
											+ allowedSubscriberClasses);
									subscriberInfoMap.put("STATUS", "INVALID");
								}
								// RBT-12024
								else if (RBTParametersUtils.getParamAsString(
										"COMMON",
										"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP",
										null) != null) {
									String subClassOperatorNameMapStr = RBTParametersUtils
											.getParamAsString(
													"COMMON",
													"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP",
													null);
									Map<String, String> subClassOperatorMap = MapUtils
											.convertToMap(
													subClassOperatorNameMapStr,
													";", ":", null);
									operatorName = subClassOperatorMap
											.get(tempString);
								}
							} else if (tokenCount == 9) {
								String tempCircleID = tempString;
								mappedCircleID = "Default";
								if (m_useWDSMap) {
									if (tempCircleID != null) {
										mappedCircleID = getMappedCircleID(tempCircleID);
										subscriberInfoMap.put("CIRCLE_ID",
												mappedCircleID);
										if (mappedCircleID
												.equalsIgnoreCase("Default")) {
											logger.info("No circle id mapped for "
													+ tempCircleID);
											subscriberInfoMap.put("CIRCLE_ID",
													null);
											subscriberInfoMap.put("STATUS",
													"INVALID");
										}// RBT-12024
										else if (operatorName != null) {
											mappedCircleID = operatorName + "_"
													+ mappedCircleID;
											subscriberInfoMap.put("CIRCLE_ID",
													mappedCircleID);
										}
									}
									break;
								}
							}
						}

					} else {
						subscriberInfoMap.put("STATUS", "ERROR");
						logger.info("WDS query returned less tokens than expected");
						return subscriberInfoMap;
					}
				} else {
					subscriberInfoMap.put("STATUS", "ERROR");
					logger.info("WDS query returned null");
					return subscriberInfoMap;
				}

			}

		}

		catch (Exception t) {
			logger.error("", t);
		}

		return subscriberInfoMap;
	}

	@Override
	public HashMap<String, String> getSubscriberInfo(String subscriberID) {
		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();
		subscriberInfoMap.put("STATUS", "VALID");
		subscriberInfoMap.put("CIRCLE_ID", null);
		subscriberInfoMap.put("USER_TYPE", "POSTPAID");
		subscriberInfoMap.put("WDS_RESPONSE", null);
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try {
			if (m_makeWDSRequest) {
				startTime = System.currentTimeMillis();
				String wdsResult = queryWDS(subscriberID);
				endTime = System.currentTimeMillis();
				long timeGap = (endTime - startTime);
				String mappedCircleID = null;
				String operatorName = null;
				if (wdsResult != null) {
					wdsResult = wdsResult.trim();
					subscriberInfoMap.put("WDS_RESPONSE",
							wdsResult.replaceAll("\\|", "#") + "|start:"
									+ startTime + "|delay:" + timeGap);
					logger.info("WDSResult:" + wdsResult);
					StringTokenizer wdsST = new StringTokenizer(wdsResult, "|");
					if (wdsST.countTokens() >= 13) {
						String tempString = null;
						for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++) {
							tempString = wdsST.nextToken();
							if (tokenCount == 4) {
								if (tempString != null
										&& Integer.parseInt(tempString) == 2) {
									logger.info("RBT::User is prepaid as from WDS");
									subscriberInfoMap.put("USER_TYPE",
											"PREPAID");
								} else {
									if (tempString != null
											&& Integer.parseInt(tempString) == 1) {
										logger.info("RBT::User is postpaid as from WDS");
										subscriberInfoMap.put("USER_TYPE",
												"POSTPAID");
									} else {
										subscriberInfoMap
												.put("STATUS", "ERROR");
									}
								}
							} else if (tokenCount == 5) {
								if (tempString.equals("1")) {
									logger.info("RBT::can allow subscriber as from WDS");
									subscriberInfoMap.put("STATUS", "VALID");
								} else {
									logger.info("RBT::cannot allow subscriber as from WDS");
									subscriberInfoMap.put("STATUS", "INVALID");
								}
							} else if (tokenCount == 8) {
								if (!allowedSubscriberClasses
										.contains(tempString)) {
									logger.info("RBT::subscriber class from WDS->"
											+ tempString
											+ ". Not allowing as allowed cases are "
											+ allowedSubscriberClasses);
									subscriberInfoMap.put("STATUS", "INVALID");
								}// RBT-12024
								else if (RBTParametersUtils.getParamAsString(
										"COMMON",
										"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP",
										null) != null) {
									String subClassOperatorNameMapStr = RBTParametersUtils
											.getParamAsString(
													"COMMON",
													"SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP",
													null);
									Map<String, String> subClassOperatorMap = MapUtils.convertToMap(
													subClassOperatorNameMapStr,";", ":", null);
									operatorName = subClassOperatorMap.get(tempString);
								}
							} else if (tokenCount == 9) {
								String tempCircleID = tempString;
								mappedCircleID = "Default";
								if (tempCircleID != null) {
									mappedCircleID = getMappedCircleID(tempCircleID);
									subscriberInfoMap.put("CIRCLE_ID",
											mappedCircleID);
									if (mappedCircleID
											.equalsIgnoreCase("Default")) {
										logger.info("No circle id mapped for "
												+ tempCircleID);
										subscriberInfoMap
												.put("CIRCLE_ID", null);
										subscriberInfoMap.put("STATUS",
												"INVALID");
									}
									else {
										boolean isPrefixExists = false;
										subscriberID = subID(subscriberID);
										SitePrefix sitePrefix = CacheManagerUtil
												.getSitePrefixCacheManager()
												.getSitePrefixes(mappedCircleID);
										if (sitePrefix != null) {
											String thisPrefix = subscriberID
													.substring(0, 4);
											String[] sitePrefixList = sitePrefix
													.getSitePrefix().split(",");
											for (String prefix : sitePrefixList) {
												if (subscriberID
														.startsWith(prefix)) {
													isPrefixExists = true;
													break;
												}
											}
											if (!isPrefixExists) {
												sitePrefix
														.setSitePrefix(sitePrefix
																.getSitePrefix()
																+ ","
																+ thisPrefix);
												CacheManagerUtil
														.getSitePrefixCacheManager()
														.updateSitePrefix(
																sitePrefix);
											}
										}
									}
									// RBT-12024
									if (operatorName != null) {
										mappedCircleID = operatorName + "_"
												+ mappedCircleID;
										subscriberInfoMap.put("CIRCLE_ID",
												mappedCircleID);
									}

								}
								break;
							}
						}

					} else {
						subscriberInfoMap.put("STATUS", "ERROR");
						subscriberInfoMap.put("WDS_STATUS",
								"WDS_ERROR_RESPONSE");
						logger.info("WDS query returned less tokens than expected");
						return subscriberInfoMap;
					}
				} else {
					subscriberInfoMap.put("STATUS", "ERROR");
					subscriberInfoMap.put("WDS_STATUS", "WDS_ERROR");
					logger.info("WDS query returned null");
					return subscriberInfoMap;
				}

			}

			// write event log with Subscriber ID, wds start time, wds end Time,
			// wds Time GAP

		}

		catch (Exception t) {
			logger.error("", t);
		}
		return subscriberInfoMap;
	}

	@Override
	public String canBeGifted(String subscriberId, String callerId,
			String contentID) {
		HashMap subscriberInfo = getSubscriberInfo(callerId);
		if (subscriberInfo.containsKey("STATUS")
				&& subscriberInfo.get("STATUS") != null
				&& (((String) subscriberInfo.get("STATUS"))
						.equalsIgnoreCase("VALID")))
			return GIFT_SUCCESS;

		return GIFT_FAILURE_GIFTEE_INVALID;
	}

	private Date getUpdatedEndDate(Date clipEndDate, Categories categories) {
		Date endDate = new Date();
		if (clipEndDate != null) {
			endDate = clipEndDate;
			if (categories!=null && com.onmobile.apps.ringbacktones.webservice.common.Utility
					.isShuffleCategory(categories.type())) {
				endDate = categories.endTime();
			}
		}
		return endDate;
	}
}
