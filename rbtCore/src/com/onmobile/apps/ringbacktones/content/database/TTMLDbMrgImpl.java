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

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.MNPConstants;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class TTMLDbMrgImpl extends RBTDBManager {

	private static Logger logger = Logger.getLogger(TTMLDbMrgImpl.class);

	static boolean retailerInRefresh = false;
	public static String m_DEFAULT_DOWNLOAD = null;
	public static int m_MAX_DOWNLOADS_ALLOWED = -1;

	private static boolean makeWDSRequest = false;

	@Override
	public void init() {

		logger.info("inside init method");
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "MAX_DOWNLOADS_ALLOWED");
		if (param != null) {
			m_MAX_DOWNLOADS_ALLOWED = Integer.parseInt(param.getValue());
		}
		param = CacheManagerUtil.getParametersCacheManager().getParameter(
				"COMMON", "DEFAULT_DOWNLOAD");
		if (param != null) {
			m_DEFAULT_DOWNLOAD = param.getValue();
		}
		param = CacheManagerUtil.getParametersCacheManager().getParameter(
				"COMMON", "DEFAULT_SEL_IN_LOOP");

		makeWDSRequest = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON, "MAKE_WDS_REQUEST", "FALSE");
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
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		String addResult = null;
		boolean updateEndDate = false;
		try {
			if (makeWDSRequest
					&& (extraInfo == null || extraInfo
							.get(EXTRA_INFO_WDS_QUERY_RESULT) == null)) {
				String wdsResult = null;
				SubscriberDetail subscriberDetail = RbtServicesMgr
						.getSubscriberDetail(new MNPContext(subscriberID));
				if (subscriberDetail != null) {
					HashMap<String, String> subscriberDetailsMap = subscriberDetail
							.getSubscriberDetailsMap();
					if (subscriberDetailsMap != null)
						wdsResult = subscriberDetailsMap
								.get(MNPConstants.OPERATOR_USER_INFO);
				}

				if (wdsResult == null) {
					logger.info("Could not get WDS info, returning "
							+ SELECTION_FAILED_WDS_FAILED);
					return SELECTION_FAILED_WDS_FAILED;
				}

				if (extraInfo == null)
					extraInfo = new HashMap<String, String>();

				extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
			}

			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null
					&& subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (selInterval != null && selInterval.indexOf(",") != -1) {
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while (stk.hasMoreTokens())
					days.add(stk.nextToken());

				if (days.size() == 7) {
					selInterval = null;
				} else {
					Collections.sort(days);
					selInterval = "";
					for (int i = 0; i < days.size(); i++) {
						selInterval = selInterval + days.get(i);
						if (i != days.size() - 1)
							selInterval = selInterval + ",";
					}
				}
			}

			if (sub != null && rbtType != 2) {
				rbtType = sub.rbtType();
			}
			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				logger.info(subscriberID + " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}
			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection) {
				isSelSuspended = isSelSuspended(subscriberID, callerID);
			}
			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
				return SELECTION_FAILED_SELECTION_FOR_CALLER_SUSPENDED;
			}

			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); selectInfo = "free:" +
			 * selectInfo; }
			 */
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
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
				if (clipMap.containsKey("CLIP_GRAMMAR"))
					clipGrammar = (String) clipMap.get("CLIP_GRAMMAR");
				if (clipMap.containsKey("CLIP_WAV"))
					subscriberWavFile = (String) clipMap.get("CLIP_WAV");
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

				/*
				 * if (freePeriod == 0 && status != 99 && clipEndTime != null) {
				 * endDate = clipEndTime; }
				 */
				if (categories != null
						&& (categories.type() == DAILY_SHUFFLE || categories
								.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				/*
				 * if (clipGrammar != null &&
				 * clipGrammar.equalsIgnoreCase("UGC")) if (selectInfo == null)
				 * selectInfo = "UGC"; else selectInfo += ":UGC";
				 */
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
				} else {
					classType = chargeClassType;
				}

				if (first != null && first.getChargeClass().startsWith("TRIAL")
						&& categories != null && categories.id() != 26) {
					classType = first.getChargeClass();
				}
			}

			if (!useUIChargeClass && categories != null
					&& categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy)) {
				classType = (String) m_modeChargeClass.get(selectedBy);
			}

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(
						conn, subID(subscriberID), null);
				if (viralSMS != null) {
					selectInfo = selectInfo + ":" + "viral";
				}
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			// int count = 0;

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
						&& subExtraInfoMap.containsKey(UDS_OPTIN)) {
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN))
							.equalsIgnoreCase("TRUE");
				}
			}

			if (selInterval != null && status != 80) {

				if (selInterval.startsWith("W") || selInterval.startsWith("M")) {

					status = 75;
				}

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
								|| parseDate.equals(currentDate)) {
							return SELECTION_FAILED_INVALID_PARAMETER;
						}
						Calendar cal = Calendar.getInstance();
						cal.setTime(parseDate);
						// parseDate.setDate(parseDate.getDate()+1);
						cal.add(Calendar.DAY_OF_YEAR, 1);
						endDate = cal.getTime();
					}

					if (date.length() == 4) {

						endDate = m_endDate;
					}
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

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;

			subscriberStatus = getAvailableSelection(conn, subID(subscriberID),
					subID(callerID), subscriberSelections, categories,
					subscriberWavFile, status, fromTime, toTime, startDate,
					endDate, doTODCheck, inLoop, rbtType, selInterval, selectedBy);
			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
				// System.out.println("111111111111111111");
				if (inLoop && (status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (inLoop && categories.type() == SHUFFLE && !m_putSGSInUGS)
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
				// else // this else will make all first callerID selection as
				// override :), not needed actually
				// inLoop = false;

				/**
				 * @added by sreekar if user's last selection is a trail
				 *        selection his next selection should override the old
				 *        one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop,
						subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null) {
					actBy = sub.activatedBy();
					// oldSubClass = sub.oldClassType();
				}
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
						Category category = RBTCacheManager.getInstance()
								.getCategory(categories.id());
						Clip clip = RBTCacheManager.getInstance()
								.getClipByRbtWavFileName(subscriberWavFile);
						CosDetails cosDetail = getCosDetailsForContent(
								subscriberID, subPacks, category, clip, status,
								callerID);
						if (cosDetail != null) {
							int selCount = sub.maxSelections();
							nextClass = getChargeClassFromCos(cosDetail,
									selCount);
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
					if (status == 80 && rbtType == 2) {
						classType = clipClassType;
					} else {
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

				boolean isPackSel = false;
				String packCosID = null;
				boolean isNotForProfileCricketSelection = false;;
				
				HashMap<String, String> responseParams = new HashMap<String, String>();
				if ((status == 1 || status == 75 || status == 79
						|| status == 80 || status == 92 || status == 93 || status == 95)
						&& rbtType != 2) {
					sel_status = STATE_BASE_ACTIVATION_PENDING;

					boolean isSubActive = false;
					if (subYes != null
							&& (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes
									.equalsIgnoreCase(STATE_EVENT))) {
						if (!isPackActivationPendingForContent(sub, categories,
								subscriberWavFile, status, callerID))
							isSubActive = true;
					}

					
					addResult = addSubscriberDownloadRW(subscriberID,
							subscriberWavFile, categories, null, isSubActive,
							classType, selectedBy, selectInfo, extraInfo,
							incrSelCount, useUIChargeClass, false, responseParams, sub, null);
					if (addResult.indexOf("SUCCESS") == -1)
						return addResult;// changed from
											// SELECTION_FAILED_DOWNLOAD_FAILURE
											// to addResult Variable
					else if (addResult
							.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
						sel_status = STATE_TO_BE_ACTIVATED;
					else if (addResult.indexOf("SUCCESS:DOWNLOAD_GRACE") != -1)
						sel_status = STATE_BASE_ACTIVATION_PENDING;

					classType = "FREE";
					
					isNotForProfileCricketSelection = true;
				} else {
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

					if (nextClass != null
							&& !nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
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

				
				boolean isDownloadAddedSuccessfully = addResult != null && (addResult.indexOf("SUCCESS:DOWNLOAD_ADDED") != -1 || addResult.indexOf("SUCCESS:DOWNLOAD_REACTIVATED") != -1 ) ? true : false;
				
				if(Utility.isModeConfiguredForConsent(selectedBy) && !extraInfo.containsKey(iRBTConstant.EXTRA_INFO_TPCGID) && isDownloadAddedSuccessfully) {
					//update fromTime, toTime, ,selinterval, sel_status
					if(responseParams.containsKey("REF_ID")) {
						String downladConsentId = responseParams.get("REF_ID");
						ConsentTableImpl.updateConsentRecordForDownload(conn, subscriberID, downladConsentId, fromTime +"", toTime + "" , selInterval, status, loopStatus + "", callerID);
					}
					
					
					if (clipMap != null && responseParams.containsKey("REF_ID")) {
						clipMap.put("CONSENTID", responseParams.get("REF_ID"));
						if (sub.refID() != null
								&& isSubscriberDeactivated(getSubscriber(subscriberID))) {
							clipMap.put("CONSENTID", sub.refID());
							clipMap.put("CONSENTSUBCLASS", sub.subscriptionClass());
						}
						clipMap.put("CONSENTCLASSTYPE", responseParams.get("CLASS_TYPE"));
					}
					
					count = 1;
				}
				else {
					count = createSubscriberStatus(subscriberID, callerID,
							categories.id(), subscriberWavFile, setTime, startDate,
							endDate, status, selectedBy, selectInfo,
							nextChargingDate, prepaid, classType, changeSubType,
							fromTime, toTime, sel_status, true, clipMap,
							categories.type(), useDate, loopStatus, isTata,
							nextPlus, rbtType, selInterval, extraInfo, refID,
							isDirectActivation, circleID, null, false, isNotForProfileCricketSelection);
				}

				logger.info("Checking to update num max selections or not."
						+ " count: " + count + ", isPackSel: " + isPackSel);
				if (isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn,
							subscriberID, packCosID);

				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID),
							endDate, null);
				}

			} else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		if (count > 0 && addResult != null
				&& addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
			return SELECTION_SUCCESS_DOWNLOAD_ALREADY_EXISTS;
		else if (count > 0)
			return SELECTION_SUCCESS;
		else
			return SELECTION_FAILED_INTERNAL_ERROR;
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
		/*
		 * String circleID=getCircleId(subscriberID); Subscriber sub =
		 * getSubscriber(subscriberID);
		 */
		char prepaidYes = 'n';
		if (subscriber.prepaidYes())
			prepaidYes = 'y';
		Categories categories = getCategory(categoryID, subscriber.circleID(),
				prepaidYes);
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
				subClass, subscriber, 0, selInterval);
		if (ret != null && ret.startsWith("SELECTION_SUCCESS"))
			return true;
		else
			return false;
	}

	@Override
	public boolean isDownloadAllowed(String subscriberId, WebServiceContext task) {

		String method = "isDownloadAllowed";
		logger.info("inside " + method);
		SubscriberDownloads[] subDownloads = getNonDeactiveSubscriberDownloads(subscriberId);
		if (subDownloads == null
				|| subDownloads.length < m_MAX_DOWNLOADS_ALLOWED)
			return true;
		else
			return false;
	}

	// Added on 24/12 .Hariharan.

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
			boolean deact = true;
			boolean success = false;
			if (sub == null || sub.subYes() == null)
				return null;
			;
			String subYes = sub.subYes();
			if (!(subYes.equalsIgnoreCase("B") || subYes.equalsIgnoreCase("O")
					|| subYes.equals("Z") || subYes.equals("z") || subYes
						.equalsIgnoreCase("G")))
				ret = "ACT_PENDING";
			else if (checkSubClass) {
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
						userInfoXml);// deact info null for all except airtel.
				SubscriberDownloadsImpl.expireAllSubscriberDownloadBaseDct(
						conn, subID(subscriberID), deactivate);
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

	@Override
	public int cleanSubscribers(float period, boolean useSM) {
		int count = 0;
		String[] subscribers = getOldSubscribers(period, useSM);
		for (int i = 0; subscribers != null && i < subscribers.length; i++) {
			try {
				count++;
				String subID = subscribers[i].trim();
				removeSubscriberBookMark(subID(subID));
				cleanOldSubscriber(subID(subID));
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("RBT::cleaned subscribers older than " + period);
		return count;
	}
	//RBT-9873 Added xtraParametersMap for CG flow
	@Override
	public Subscriber activateSubscriber(String subscriberID, String activate,
			Date startDate, Date endDate, boolean isPrepaid,
			int activationTimePeriod, int freePeriod, String actInfo,
			String classType, boolean smActivation, CosDetails cos,
			boolean isDirectActivation, int rbtType, HashMap extraInfo,
			String circleId, String refId, boolean isComboRequest, Map<String,String> xtraParametersMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		Subscriber subscriber = null;
		try {
			if (makeWDSRequest
					&& (extraInfo == null || extraInfo
							.get(EXTRA_INFO_WDS_QUERY_RESULT) == null)) {
				String wdsResult = null;
				SubscriberDetail subscriberDetail = RbtServicesMgr
						.getSubscriberDetail(new MNPContext(subscriberID));
				if (subscriberDetail != null) {
					HashMap<String, String> subscriberDetailsMap = subscriberDetail
							.getSubscriberDetailsMap();
					if (subscriberDetailsMap != null)
						wdsResult = subscriberDetailsMap
								.get(MNPConstants.OPERATOR_USER_INFO);
				}
				if (wdsResult == null) {
					logger.info("Could not get WDS info, returning null");
					return null;
				}
				if (extraInfo == null)
					extraInfo = new HashMap<String, String>();
				extraInfo.put(EXTRA_INFO_WDS_QUERY_RESULT, wdsResult);
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			subscriber = SubscriberImpl
					.getSubscriber(conn, subID(subscriberID));
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
				if (endDate.after(cos.getEndDate()))
					endDate = cos.getEndDate();
			}

			String subscription = STATE_TO_BE_ACTIVATED;
			String activationInfo = actInfo;

			if (isDirectActivation)
				subscription = STATE_ACTIVATED;

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
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			// Added for JIRA-RBT-6321
			if (isDirectActivation && refId != null)
				finalRefID = refId;

			boolean isRequestForConsent = true;
			if(null != extraInfo && extraInfo.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
				isRequestForConsent = false;
			}

			if(isRequestForConsent && (subscriber == null || subscriber.subYes().equals("X"))) {
				Subscriber tempSubscriber = checkModeAndInsertIntoConsent(subscriberID, activate, startDate,
						endDate, isDirectActivation, rbtType, conn, prepaid, subscription,
						activationInfo, cosID, subscriptionClass, finalRefID, extraInfo,
						circleId, isComboRequest, xtraParametersMap);
				
				if(tempSubscriber != null) {
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
									.equals("z"))) {
					return null;
				}
				String deactivatedBy = subscriber.deactivatedBy();
				Date deactivationDate = subscriber.endDate();

				boolean success = SubscriberImpl.update(conn,
						subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo,
						subscriptionClass, deactivatedBy, deactivationDate,
						null, subscription, 0, cosID, cosID, rbtType,
						subscriber.language(), subExtraInfo,
						isDirectActivation, circleId, finalRefID);
				if (startDate == null)
					startDate = new Date(System.currentTimeMillis());

				if (success) {
					subscriber = new SubscriberImpl(subID(subscriberID),
							activate, null, startDate, m_endDate, prepaid,
							null, null, 0, activationInfo, subscriptionClass,
							subscription, deactivatedBy, deactivationDate,
							null, 0, cosID, cosID, rbtType,
							subscriber.language(), subscriber.oldClassType(),
							subExtraInfo, circleId, finalRefID);
				} else
					subscriber = null;
			} else {
				// Third Party confirmation chages 
				//RBT-9873 Added xtraParametersMap for CG flow
				/*subscriber = checkModeAndInsertIntoConsent(subscriberID,
						activate, startDate, endDate, false, rbtType, conn, prepaid,
						subscription, activationInfo, cosID, subscriptionClass,
						finalRefID, subExtraInfo, circleId, isComboRequest,xtraParametersMap);
				*/
					subscriber = SubscriberImpl.insert(conn,
							subID(subscriberID), activate, null, startDate,
							endDate, prepaid, null, null, 0, activationInfo,
							subscriptionClass, null, null, null, subscription,
							0, cosID, cosID, rbtType, null, isDirectActivation,
							subExtraInfo, circleId, finalRefID);
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

		String prepaid = "n";
		if (isPrepaid)
			prepaid = "y";

		Date endDate = null;
		Subscriber subscriber = null;
		try {
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

			/*
			 * if(freePeriod != 0) { nextChargingDate =
			 * Calendar.getInstance().getTime(); lastAccessDate =
			 * Calendar.getInstance().getTime(); activationDate =
			 * Calendar.getInstance().getTime(); subscription = "N";
			 * activationInfo = "free:" + activationInfo; }
			 */

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
						|| subsciptionYes.equals("z"))
					return null;
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
			} else {
				// Third Party confirmation chages 
				//RBT-9873 Added xtraParametersMap for CG flow
				String refID = UUID.randomUUID().toString();
				subscriber = checkModeAndInsertIntoConsent(subscriberID,
						activate, date, endDate, false, rbtType, conn, prepaid,
						subscription, activationInfo, cosID, subscriptionClass,
						refID, null, circleId, false,new HashMap<String,String>());
				if (null == subscriber) {
					subscriber = SubscriberImpl.insert(conn,
							subID(subscriberID), activate, null, date, endDate,
							prepaid, lastAccessDate, nextChargingDate, 0,
							activationInfo, subscriptionClass, null, null,
							activationDate, subscription, 0, cosID, cosID,
							rbtType, null, false, null, circleId, null);
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
	public String canBeGifted(String subscriberId, String callerId,
			String contentID) {
		String canBeGifted = null;
		subscriberId = subID(subscriberId);
		callerId = subID(callerId);
		Subscriber sub = getSubscriber(subscriberId);

		HashMap callerInfo = getSubscriberInfo(callerId);
		if (!((String) callerInfo.get("STATUS")).equalsIgnoreCase("VALID")) {
			canBeGifted = GIFT_FAILURE_GIFTEE_INVALID;
			return canBeGifted;
		}

		Subscriber caller = getSubscriber(callerId);
		if (isSubscriberActivationPending(caller))
			return GIFT_FAILURE_ACT_PENDING;
		if (isSubscriberDeactivationPending(caller))
			return GIFT_FAILURE_DEACT_PENDING;
		if (caller != null
				&& (caller.subYes().equals(STATE_ACTIVATION_GRACE)
						|| caller.subYes().equals(STATE_SUSPENDED_INIT) || caller
						.subYes().equals(STATE_SUSPENDED)))
			return GIFT_FAILURE_TECHNICAL_DIFFICULTIES;

		if (contentID == null && serviceGiftisSongGiftInUse(caller))
			return GIFT_FAILURE_GIFT_IN_USE;

		ViralSMSTable[] vst = getViralSMSByCaller(callerId);
		if (vst != null) {
			for (int i = 0; i < vst.length; i++) {
				if (contentID == null) {
					if (serviceGiftIsGiftPending(vst[i], caller))
						return GIFT_FAILURE_ACT_GIFT_PENDING;

					if (serviceGiftisServiceGiftInUse(vst[i], caller))
						return GIFT_FAILURE_GIFT_IN_USE;
				} else {
					if (songGiftIsGiftPending(contentID, vst[i], caller))
						return GIFT_FAILURE_SONG_GIFT_PENDING;

					if (isSubscriberDeactivated(caller)
							&& serviceGiftIsGiftPending(vst[i], caller))
						return GIFT_FAILURE_ACT_GIFT_PENDING;
				}
			}
		}

		boolean isClip = true;
		ClipMinimal cMin = null;
		String clipName = null;
		int catID = -1;
		if (contentID != null && !contentID.equals("null")) {
			if (contentID.startsWith("C")) {
				isClip = false;
				contentID = contentID.substring(1);
				catID = Integer.parseInt(contentID);
			} else {
				int clipId = Integer.parseInt(contentID);
				cMin = super.getClipMinimal(clipId, true);
				clipName = cMin.getWavFile();
			}

		}

		if (contentID != null && !contentID.equals("null")
				&& !isSubscriberDeactivated(caller)) {
			SubscriberDownloads[] subDownloads = getSubscriberDownloads(callerId);
			if (subDownloads != null) {
				for (int i = 0; i < subDownloads.length; i++) {
					char downloadStatus = subDownloads[i].downloadStatus();
					if ((isClip && subDownloads[i].promoId().equals(clipName))
							|| subDownloads[i].categoryID() == catID) {
						if (downloadStatus == 'n' || downloadStatus == 'p'
								|| downloadStatus == 'y'
								|| downloadStatus == 'd'
								|| downloadStatus == 's'
								|| downloadStatus == 'e'
								|| downloadStatus == 'f') {
							return GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS;
						}
					}
				}
			}
		}

		if (isSubscriberDeactivated(caller))
			canBeGifted = GIFT_SUCCESS_GIFTEE_NEW_USER;
		else
			canBeGifted = GIFT_SUCCESS_GIFTEE_ALREADY_ACTIVE;

		return canBeGifted;
	}

	public boolean songGiftIsGiftPending(String contentID, ViralSMSTable vst,
			Subscriber caller) {
		if (vst != null) {
			String clipID = vst.clipID();
			String type = vst.type();
			if (clipID != null && clipID.equals(contentID)) {
				if (type.equals(GIFT) || type.equals(GIFTCHRGPENDING)
						|| type.equals(GIFT_CHARGED) || type.equals(GIFTED))
					return true;
			}
		}
		return false;
	}

	public boolean serviceGiftIsGiftPending(ViralSMSTable vst, Subscriber caller) {
		if (vst != null) {
			String type = vst.type();
			if (type.equals(GIFT) || type.equals(GIFTCHRGPENDING)
					|| type.equals(GIFT_CHARGED) || type.equals(GIFTED)) {
				if (isSubscriberDeactivated(caller)
						|| (isSubscriberActivated(caller) && (vst.clipID() == null || vst
								.clipID().equals("null"))))
					return true;
			}
		}

		return false;
	}

	public boolean serviceGiftisSongGiftInUse(Subscriber caller) {
		if (!isSubscriberDeactivated(caller)
				&& caller.activatedBy().equalsIgnoreCase("GIFT")) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -30);
			Date compareDate = calendar.getTime();
			if (caller.startDate().after(compareDate))
				return true;
		}
		return false;
	}

	public boolean serviceGiftisServiceGiftInUse(ViralSMSTable vst,
			Subscriber caller) {
		if (vst != null) {
			String type = vst.type();
			if ((type.equals(ACCEPT_ACK) || type.equals(ACCEPTED) || type
					.equals(ACCEPT_PRE)) && vst.clipID() == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_MONTH, -30);
				Date compareDate = calendar.getTime();
				if (vst.sentTime().after(compareDate))
					return true;
			}
		}

		return false;
	}

	@Override
	public String updateSubscriberId(String newSubscriberId, String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		boolean success = false;
		try {
			Subscriber sub = getSubscriber(subID(newSubscriberId));
			if (sub != null) {
				return "FAILURE:NEW MSISDN ALREADY EXISTS";
			}
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if (subscriber == null) {
				return "FAILURE:MSISDN DOESN'T EXIST";
			}
			if (!isValidPrefix(newSubscriberId)) {
				return "FAILURE:NEW MSISDN INVALID";
			}
			success = SubscriberImpl.updatePlayerStatusAndId(conn,
					subID(newSubscriberId), subID(subscriberId), "A");
			SubscriberStatusImpl.updateSubscriberId(conn,
					subID(newSubscriberId), subID(subscriberId));
			SubscriberDownloadsImpl.updateSubscriberId(conn,
					subID(newSubscriberId), subID(subscriberId));
			GroupsImpl.updateSubscriberId(conn, subID(newSubscriberId),
					subID(subscriberId));
			ViralSMSTableImpl.updateSubscriberId(conn, subID(newSubscriberId),
					subID(subscriberId));
			ProvisioningRequestsDao.updateSubscriberID(conn,
					subID(newSubscriberId), subID(subscriberId));
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success ? "SUCCESS" : "FAILURE:TECHNICAL FAULT";
	}

	@Override
	public boolean smURLSelectionNotSendSMDeactivation(String subscriberID,
			String callerID, int status, Date setDate, boolean isSuccess,
			boolean isError, String subscriberWavFile, int rbtType,
			char oldLoopStatus) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		boolean success = false;
		try {
			char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
			if (oldLoopStatus == LOOP_STATUS_EXPIRED)
				newLoopStatus = oldLoopStatus;

			success = SubscriberStatusImpl.smURLSelectionNotSendSMDeactivation(
					conn, subID(subscriberID), subID(callerID), status,
					setDate, isSuccess, isError, rbtType, newLoopStatus);

			boolean currClipSelAvailable = false;
			if (subscriberWavFile != null) {
				SubscriberStatus[] actSelections = SubscriberStatusImpl
						.getAllActiveSubscriberSettings(conn,
								subID(subscriberID), rbtType);
				if (actSelections != null && actSelections.length > 0) {
					for (int counter = 0; counter < actSelections.length; counter++) {
						SubscriberStatus tempSel = actSelections[counter];
						if (tempSel != null
								&& subscriberWavFile.equalsIgnoreCase(tempSel
										.subscriberFile().trim())) {
							currClipSelAvailable = true;
							break;
						}
					}
				}
			}

			if (!currClipSelAvailable) {
				SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl
						.getActiveSubscriberDownload(conn, subscriberID,
								subscriberWavFile);
				if (subscriberDownloads != null) {
					SubscriberDownloadsImpl.expireSubscriberDownload(conn,
							subscriberID, subscriberWavFile, "SM",
							subscriberDownloads.categoryID(),
							subscriberDownloads.categoryType(), null, null,false, false);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return success;
	}
}
