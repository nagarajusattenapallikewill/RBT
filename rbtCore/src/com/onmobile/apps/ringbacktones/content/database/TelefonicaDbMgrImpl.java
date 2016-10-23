/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 * 
 */
public class TelefonicaDbMgrImpl extends RBTDBManager {
	private static Logger logger = Logger.getLogger(TelefonicaDbMgrImpl.class);

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
		return addSubscriberSelections(subscriberID, callerID, categories,
				clipMap, setTime, startTime, endTime, status, selectedBy,
				selectionInfo, freePeriod, isPrepaid, changeSubType,
				messagePath, fromTime, toTime, chargeClassType, smActivation,
				doTODCheck, mode, regexType, subYes, promoType, circleID,
				incrSelCount, useDate, transID, OptIn, isTata, inLoop,
				subClass, sub, rbtType, selInterval, extraInfo,
				useUIChargeClass, false, null);
	}

	public String smAddSubscriberSelections(String subscriberID,
			String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath,
			int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID,
			boolean incrSelCount, boolean useDate, String transID,
			boolean OptIn, boolean isTata, boolean inLoop, String subClass,
			Subscriber sub, int rbtType, String selInterval, HashMap extraInfo,
			HashMap<String, String> responseParams) {

		// Added useUIChargeClass & isSmClientModel params and passing as true,
		// true
		String response = addSubscriberSelections(subscriberID, callerID,
				categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid,
				changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType,
				circleID, incrSelCount, useDate, transID, OptIn, isTata,
				inLoop, subClass, sub, rbtType, selInterval, extraInfo, true,
				true, responseParams);
		return response;
	}

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
			boolean isSmClientModel, HashMap<String, String> responseParams) {
		logger.info("Adding subscriber selections. subscriberID: "
				+ subscriberID + ", subClass: " + subClass
				+ ", chargeClassType: " + chargeClassType + ", extraInfo: "
				+ extraInfo + ", useUIChargeClass: " + useUIChargeClass);
		Connection conn = getConnection();
		if (conn == null)
			return null;

		int count = 0;
		String addResult = null;
		try {
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

			if (selInterval != null && selInterval.indexOf(",") != -1) {
				List days = new ArrayList();
				StringTokenizer stk = new StringTokenizer(selInterval, ",");
				while (stk.hasMoreTokens())
					days.add(stk.nextToken());

				if (selInterval.startsWith("Y")){
					selInterval = Utility.getYearBasedInterval(selInterval);
				} else if (days.size() == 7) {
					selInterval = null;
				} else {
					// RBT-9999 Added for removing sorting for monthly based
					// selection
					if (!Utility.isMonthBasedInterval(selInterval)) {
						Collections.sort(days);
						selInterval = "";
						for (int i = 0; i < days.size(); i++) {
							selInterval = selInterval + days.get(i);
							if (i != days.size() - 1)
								selInterval = selInterval + ",";
						}
					}
				}
			}

			if (callerID != null) {
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
						subscriberID);
				if (groups != null && groups.length > 0) {
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++) {
						groupIDs[i] = groups[i].groupID();
					}
					GroupMembers groupMember = GroupMembersImpl
							.getMemberFromGroups(conn, callerID, groupIDs);
					if (groupMember != null) {
						for (Groups group : groups) {
							if (groupMember.groupID() == group.groupID()) {
								if (group.preGroupID() != null
										&& group.preGroupID().equals("99")) // Blocked
																			// Caller
									return SELECTION_FAILED_CALLER_BLOCKED;
								else if (group.preGroupID() == null
										|| !group.preGroupID().equals("98"))
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}

			if (sub != null && rbtType != 2)
				rbtType = sub.rbtType();

			if (sub != null && sub.subYes() != null
					&& (sub.subYes().equals("Z") || sub.subYes().equals("z"))) {
				//IF CONFIGURATION IS TRUE, SO DON'T RETURN ,ALLOW SUSPENDED USERS ALSO
				// ALLOW_SELECTION_FOR_SUSPENDED_USER
				String isSuspendedStatusAllow = CacheManagerUtil.getParametersCacheManager().getParameterValue(WEBSERVICE, ALLOW_SELECTION_FOR_SUSPENDED_USER, "FALSE");
				if(!Boolean.parseBoolean(isSuspendedStatusAllow)) {
					logger.info(subscriberID + " is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				}
			}

			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection)
				isSelSuspended = isSelSuspended(subscriberID, callerID);

			if (isSelSuspended) {
				logger.info("selection of " + subscriberID + " for " + callerID
						+ " is suspended. Returning false.");
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
			String clipClassType = null;
			String subscriberWavFile = null;
			if (clipMap != null) {
				if (clipMap.containsKey("CLIP_CLASS"))
					clipClassType = (String) clipMap.get("CLIP_CLASS");
				if (clipMap.containsKey("CLIP_END"))
					clipEndTime = (Date) clipMap.get("CLIP_END");
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
							String firstAmountStr = catCharge.getAmount();
							String secondAmountStr = clipCharge.getAmount();
							firstAmountStr = firstAmountStr.replace(",", ".");
							secondAmountStr = secondAmountStr.replace(",", ".");

							float firstAmount = Float
									.parseFloat(firstAmountStr);
							float secondAmount = Float
									.parseFloat(secondAmountStr);

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
						String firstAmountStr = first.getAmount();
						String secondAmountStr = second.getAmount();
						firstAmountStr = firstAmountStr.replace(",", ".");
						secondAmountStr = secondAmountStr.replace(",", ".");

						float firstAmount = Float.parseFloat(firstAmountStr);
						float secondAmount = Float.parseFloat(secondAmountStr);

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
			// RBT-10375 Added status checking for time based selection
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
			String campaignCode = null;
			String treatmentCode = null;
			String offerCode = null;
			if (extraInfo.containsKey(iRBTConstant.CAMPAIGN_CODE)
					&& extraInfo.containsKey(iRBTConstant.OFFER_CODE)
					&& extraInfo.containsKey(iRBTConstant.TREATMENT_CODE)) {
				campaignCode = (String)extraInfo.get(iRBTConstant.CAMPAIGN_CODE);
				treatmentCode = (String)extraInfo.get(iRBTConstant.TREATMENT_CODE);
				offerCode = (String)extraInfo.get(iRBTConstant.OFFER_CODE);
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
			SubscriberStatus subscriberStatus = getAvailableSelection(conn,
					subID(subscriberID), subID(callerID), subscriberSelections,
					categories, subscriberWavFile, status, fromTime, toTime,
					startDate, endDate, doTODCheck, inLoop, rbtType,
					selInterval, selectedBy);
			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
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
				// status 2 has been added by Sreekar for feature RBT-4119 (bug
				// RBT-4291)
				if ((status == 1 || status == 75 || status == 79
						|| status == 80 || status == 92 || status == 93
						|| status == 95 || status == 2)
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

					boolean isConsentActRecordInserted = false;
					boolean isAllowPremiumContent = false;
					boolean isUdsOnRequest = false;
					if(clipMap.containsKey("CONSENT_SUBSCRIPTION_INSERT")) {
						isConsentActRecordInserted = true;
					}
					if(clipMap.containsKey(WebServiceConstants.param_allowPremiumContent)){
						isAllowPremiumContent = true;
					}
					if (clipMap.containsKey(WebServiceConstants.param_isUdsOn)) {
						String udson = (String) clipMap.get(WebServiceConstants.param_isUdsOn);
						if (udson.equalsIgnoreCase("true")) {
							isUdsOnRequest = true;
						}
					}
					String baseConsentId = null;
					Subscriber subscriber = getSubscriber(subscriberID);
					if(isSubscriberDeactivated(subscriber) && sub != null ) {
						baseConsentId = sub.refID();
					}
					subscriberStatus = null;
					if (extraInfo != null && !extraInfo.containsKey(EXTRA_INFO_TPCGID)) {
						if (com.onmobile.apps.ringbacktones.services.common.Utility.isModeConfiguredForConsent(selectedBy)) {
							SubscriberDownloads downLoad = getSubscriberDownload(
									subID(subscriberID), subscriberWavFile, categories.id(),
									categories.type());

							if (downLoad == null
									|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad
									.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
								if (!isDownloadAllowed(subscriberID)) {
									return "FAILURE:DOWNLOAD_OVERLIMIT";
								}
							}
							if (downLoad != null) {
								char downStat = downLoad.downloadStatus();
								if (downStat == STATE_DOWNLOAD_DEACTIVATION_PENDING
										|| downStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED)
									return "FAILURE:DOWNLOAD_DEACT_PENDING";
								else if (downStat == STATE_DOWNLOAD_ACT_ERROR
										|| downStat == STATE_DOWNLOAD_DEACT_ERROR)
									return "FAILURE:DOWNLOAD_ERROR";
								else if (downStat == STATE_DOWNLOAD_SUSPENSION)
									return "FAILURE:DOWNLOAD_SUSPENDED";
							}
						}
						subscriberStatus = checkModeAndInsertIntoConsent(subscriberID, callerID, categories.id(), subscriberWavFile, 
								setTime, startTime, clipEndTime, status, selectedBy, selectionInfo,
								nextChargingDate, prepaid, classType, fromTime, toTime, sel_status,
								categories.type(), loopStatus, rbtType, selInterval, null, circleID, 
								conn, extraInfo, useUIChargeClass, baseConsentId, null, subClass, 
								isConsentActRecordInserted, isAllowPremiumContent, isUdsOnRequest, null, 
								false, null, smActivation, nextPlus, null);
					}
					if (subscriberStatus != null) {
						clipMap.put("RECENT_CLASS_TYPE", classType);
						//For consent
						if (clipMap != null && subscriberStatus != null) {
							clipMap.put(param_isSelConsentInserted, "true");
							String consentId = null;
							Map<String, String> selExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo());
							
							if(selExtraInfoMap != null) {
								consentId = selExtraInfoMap.get(EXTRA_INFO_TRANS_ID);
							}
							
							if(consentId == null) {
								consentId = subscriberStatus.refID();
							}
							
							clipMap.put("CONSENTID", consentId);
							if (baseConsentId != null
									&& isSubscriberDeactivated(subscriber)) {
								clipMap.put("CONSENTID", baseConsentId);
								if (sub != null)
									clipMap.put("CONSENTSUBCLASS", sub
											.subscriptionClass());
								clipMap.put("CONSENT_SERVICE_ID", DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_ID", sub.subscriptionClass(), classType, sub.circleID(), false, true, true));
								clipMap.put("CONSENT_SERVICE_CLASS", DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_CLASS", sub.subscriptionClass(), classType, sub.circleID(), false, true, true));
							}
							else if(subscriber != null)
							{
								clipMap.put("CONSENT_SERVICE_ID", DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_ID", subscriber.subscriptionClass(), classType, subscriber.circleID(), false, true, false));
								clipMap.put("CONSENT_SERVICE_CLASS", DoubleConfirmationConsentPushThread.getServiceValue("SERVICE_CLASS", subscriber.subscriptionClass(), classType, subscriber.circleID(), false, true, false));
							}
							clipMap.put("CONSENTCLASSTYPE", classType);
						}
						
						
						return SELECTION_SUCCESS;
					}
					logger.info("Adding download for subscriberId: "
							+ subscriberID + ", classType: " + classType
							+ ", extraInfo: " + extraInfo);
					addResult = addSubscriberDownloadRW(subscriberID,
							subscriberWavFile, categories, null, isSubActive,
							classType, selectedBy, selectInfo, extraInfo,
							incrSelCount, useUIChargeClass, isSmClientModel,
							responseParams, null);
					logger.info("Added download, result: " + addResult
							+ " for subscriberId: " + subscriberID
							+ ", classType: " + classType + ", extraInfo: "
							+ extraInfo);
					if (addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1)
						sel_status = STATE_TO_BE_ACTIVATED;
					else if (addResult.indexOf("SUCCESS:DOWNLOAD_GRACE") != -1)
						sel_status = STATE_BASE_ACTIVATION_PENDING;
					else if (addResult.indexOf("SUCCESS") == -1)
						return addResult;

					if (addResult.indexOf("SUCCESS") != -1
							&& !addResult
									.equalsIgnoreCase("SUCCESS:DOWNLOAD_ALREADY_ACTIVE")) {
						logger.info("Adding Recent Charge Class For download= "
								+ classType);
						clipMap.put("RECENT_CLASS_TYPE", classType);
					}

					classType = "FREE";
				} else if (!useUIChargeClass
						&& ((!RBTParametersUtils.getParamAsBoolean(COMMON,
								"IGNORE_COS_FOR_PROFILES_N_CRICKET", "FALSE")) || (status != 90 && status != 99))) {
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
				if (extraInfo != null) {
					extraInfo.remove(iRBTConstant.CAMPAIGN_CODE);
					extraInfo.remove(iRBTConstant.TREATMENT_CODE);
					extraInfo.remove(iRBTConstant.OFFER_CODE);
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

				count = createSubscriberStatus(subscriberID, callerID,
						categories.id(), subscriberWavFile, setTime, startDate,
						endDate, status, selectedBy, selectInfo,
						nextChargingDate, prepaid, classType, changeSubType,
						fromTime, toTime, sel_status, true, clipMap,
						categories.type(), useDate, loopStatus, isTata,
						nextPlus, rbtType, selInterval, extraInfo, null, false,
						circleID);

				logger.info("Checking to update num max selections or not."
						+ " count: " + count + " isPackSel: " + isPackSel);
				if (count > 0 && addResult != null
						&& !(addResult.indexOf("SUCCESS:DOWNLOAD_ADDED") != -1)
						&& !(addResult.indexOf("SUCCESS:DOWNLOAD_REACTIVATED") != -1)
						&& campaignCode != null && treatmentCode != null && offerCode != null) {
					String msg = iRBTConstant.CAMPAIGN_CODE + "=" + campaignCode + ","
							+ iRBTConstant.TREATMENT_CODE + "=" + treatmentCode + ","
							+ iRBTConstant.OFFER_CODE + "=" + offerCode + ","
							+ iRBTConstant.RETRY_COUNT + "=0";
					RBTCallBackEvent rbtCallBackEvent = new RBTCallBackEvent();
					rbtCallBackEvent.setClipID(-1);
					rbtCallBackEvent.setEventType(RBTCallBackEvent.SM_CALLBACK_PENDING);
					rbtCallBackEvent.setMessage(msg);
					rbtCallBackEvent.setModuleID(RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION);
					rbtCallBackEvent.setSelectedBy(mode);
					rbtCallBackEvent.setSelectionInfo((String) clipMap.get("SELECTION_REF_ID"));
					rbtCallBackEvent.setSubscriberID(subscriberID);
					logger.info("Inserting rbtCallBackEvent: " + rbtCallBackEvent);
					rbtCallBackEvent.createCallbackEvent(rbtCallBackEvent);
				}
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

	public HashMap<String, String> getSubscriberInfo(String subscriberID) {
		HashMap<String, String> subscriberInfoMap = new HashMap<String, String>();
		Integer statusCode = new Integer(0);
		StringBuffer response = new StringBuffer();
		String url = null;
		subscriberInfoMap.put("STATUS", "INVALID");

		url = m_validateMsisdnURL + subscriberID;
		Tools.callURL(url, statusCode, response, false, null, 80);
		if (response != null
				&& response.toString().trim().equalsIgnoreCase("SUCCESS")) {
			subscriberInfoMap.put("STATUS", "VALID");
		}

		logger.info("RBT:: subscriberInfoMap = " + subscriberInfoMap);
		return subscriberInfoMap;
	}

	public String canBeGifted(String subscriberId, String callerId,
			String contentID) {
		String method = "canBeGifted";
		logger.info("inside " + method);

		HashMap<String, String> callerInfo = getSubscriberInfo(callerId);
		if (!callerInfo.get("STATUS").equalsIgnoreCase("VALID"))
			return GIFT_FAILURE_GIFTEE_INVALID;

		return GIFT_SUCCESS;
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

		String prepaid = isPrepaid ? "y" : "n";
		Subscriber subscriber = null;

		if (!RBTParametersUtils.getParamAsBoolean("COMMON",
				"DEL_SELECTION_ON_DEACT", "TRUE")) {
			List<ProvisioningRequests> provisioningRequestsList = getAciveProvisioningRequests(subscriberID);
			Set<String> packSet = new HashSet<String>();
			for (ProvisioningRequests provReq : provisioningRequestsList) {
				packSet.add(provReq.getType() + "");
			}

			List<String> packs = null;
			if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PACK)) {
				packs = Arrays.asList(((String) extraInfo.get(EXTRA_INFO_PACK))
						.split("\\,"));
				packSet.addAll(packs);
			}

			String pack = "";
			for (String packId : packSet) {
				pack = packId + ",";
			}
			if (pack != null && pack.length() > 0) {
				extraInfo.put(EXTRA_INFO_PACK,
						pack.substring(0, pack.length() - 1));
			}

		}
		try {
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

			String isResenveCharge = (String) extraInfo
					.remove("RESERVE_CHARGE");
			if (isResenveCharge != null && isResenveCharge.equals("TRUE")) {
				subscription = STATE_ACTIVATION_PENDING;
			}

			if (isDirectActivation)
				subscription = STATE_ACTIVATED;

			String cosID = null;
			String subscriptionClass = classType;
			if (cos != null) {
				cosID = cos.getCosId();
				if ((subscriptionClass == null || subscriptionClass
						.equalsIgnoreCase("DEFAULT"))
						&& cos.getSubscriptionClass() != null
						&& !cos.getSubscriptionClass().equalsIgnoreCase(
								"DEFAULT"))
					subscriptionClass = cos.getSubscriptionClass();
			}

			if (subscriptionClass == null)
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

			// update ExtraInfo
			String subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String finalRefID = UUID.randomUUID().toString();

			// Added for JIRA-RBT-6321
			if (isDirectActivation && refId != null)
				finalRefID = refId;

			if (subscriber != null
					&& (!com.onmobile.apps.ringbacktones.services.common.Utility
							.isModeConfiguredForConsent(activate) || extraInfo
							.containsKey(iRBTConstant.EXTRA_INFO_TPCGID))) {
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

				if (extraInfo != null
						&& extraInfo.containsKey(EXTRA_INFO_TPCGID)
						&& extraInfo.get(EXTRA_INFO_TPCGID).equals("DUMMY_TO_BE_REMOVED")) {
					extraInfo.remove(EXTRA_INFO_TPCGID);
					subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
				}
				boolean success = SubscriberImpl.update(conn,
						subID(subscriberID), activate, null, startDate,
						endDate, prepaid, null, null, 0, activationInfo,
						subscriptionClass, deactivatedBy, deactivationDate,
						null, subscription, 0, cosID, cosID, rbtType,
						subscriber.language(), subExtraInfo, circleId,
						finalRefID, isDirectActivation);
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
				subscriber = checkModeAndInsertIntoConsent(subscriberID,
						activate, startDate, endDate, isDirectActivation,
						rbtType, conn, prepaid, subscription, activationInfo,
						cosID, subscriptionClass, finalRefID, extraInfo, circleId, isComboRequest,xtraParametersMap);
				subExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
				if (subscriber == null) {
					subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
							activate, null, startDate, endDate, prepaid, null,
							null, 0, activationInfo, subscriptionClass, null, null,
							null, subscription, 0, cosID, cosID, rbtType, null,
							isDirectActivation, subExtraInfo, circleId, finalRefID);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
	}

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
			} else
				subscriber = SubscriberImpl.insert(conn, subID(subscriberID),
						activate, null, date, endDate, prepaid, lastAccessDate,
						nextChargingDate, 0, activationInfo, subscriptionClass,
						null, null, activationDate, subscription, 0, cosID,
						cosID, rbtType, null, false, null, circleId, null);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriber;
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
			if (sub == null || sub.subYes() == null)
				return null;

			String subYes = sub.subYes();
			if (!(subYes.equalsIgnoreCase("B") || subYes.equalsIgnoreCase("O")
					|| subYes.equals("Z") || subYes.equals("z")
					|| subYes.equalsIgnoreCase("G")
					|| subYes.equalsIgnoreCase("A") || subYes
						.equalsIgnoreCase("N")))
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
				if (sub != null) {
					userInfoXml = sub.extraInfo();
					HashMap<String, String> userInfoMap = DBUtility
							.getAttributeMapFromXML(userInfoXml);
					if (userInfoMap != null
							&& userInfoMap.containsKey("UNSUB_DELAY")) {
						deactivate = userInfoMap.get("UNSUB_DELAY");
						userInfoMap.remove("UNSUB_DELAY");
						userInfoXml = DBUtility
								.getAttributeXMLFromMap(userInfoMap);
						// Uncomemnted the lines below for TS-1914
						// Commented following 2 lines as part of bug fixed done
						// by Sreekar for TS-1593
						// if(userInfoXml == null)
						// userInfoXml = "";
					}
				}

				SubscriberImpl.deactivate(conn, subID(subscriberID),
						deactivate, date, sendToHLR, smDeactivation, false,
						isDirectDeact, m_isMemCachePlayer, dctInfo, sub,
						userInfoXml);
				// deact info null for all except airtel.

				if (delSelections) {
					SubscriberDownloadsImpl.expireAllSubscriberDownloadBaseDct(
							conn, subID(subscriberID), deactivate);
					SubscriberStatusImpl.deactivate(conn, subID(subscriberID),
							date, smDeactivation, false, deactivate, rbtType);
				}

				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn,
						subscriberID);
				if (groups != null) {
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++)
						groupIDs[i] = groups[i].groupID();
					GroupMembersImpl.deleteGroupMembersOfGroups(conn, groupIDs);
					GroupsImpl.deleteGroupsOfSubscriber(conn, subscriberID);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return ret == null ? "SUCCESS" : ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.content.database.RBTDBManager#
	 * isDownloadAllowed(java.lang.String)
	 */
	@Override
	public boolean isDownloadAllowed(String subscriberID, WebServiceContext task) {
		int maxDownloadsAllowed = -1;
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter("COMMON", "MAX_DOWNLOADS_ALLOWED");

		// Parameters muiscPackCosIdParam =
		// CacheManagerUtil.getParametersCacheManager()
		// .getParameter("COMMON", "DOWNLOAD_LIMIT_SONG_PACK_COS_IDS");

		// List<String> musicPackCosIdList = null;
		int packNumMaxSelection = -1;

		String cosId = null;

		Subscriber subscriber = null;
		if (task != null
				&& task.containsKey(WebServiceConstants.param_subscriber))
			subscriber = (Subscriber) task
					.get(WebServiceConstants.param_subscriber);
		else
			subscriber = getSubscriber(subscriberID);

		HashMap<String, String> extraInfo = getExtraInfoMap(subscriber);
		if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PACK)) {
			String subscriberCosIds = extraInfo.get(EXTRA_INFO_PACK);

			// Get the entries from provisioning requests table by
			// subscriber id and type i.e. cosid if status is 33
			// then update sub_type to azaan.
			List<String> subscriberCosIdList = ListUtils.convertToList(
					subscriberCosIds, ",");
			logger.debug("subscriber Extrainfo cosIds: " + subscriberID);
			// One of the subscriber cos id is MusicPack cos. So, update the
			// player status.
			Iterator<String> iterator = subscriberCosIdList.iterator();
			while (iterator.hasNext()) {
				String packCosId = iterator.next();
				int cos = Integer.parseInt(packCosId);
				CosDetails finalCos = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(packCosId);
				if (finalCos != null
						&& finalCos.getCosType().equalsIgnoreCase(
								iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
					packNumMaxSelection = isPackActive(subscriberID, cos);
					cosId = cos + "";
				}
			}
		}

		if (task != null && isSubscriberDeactivated(subscriber)
				&& task.containsKey(WebServiceConstants.param_cosID)) {
			String tempCosId = task.getString(WebServiceConstants.param_cosID);
			CosDetails finalCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(tempCosId);
			if (finalCos != null
					&& finalCos.getCosType() != null 
					&& finalCos.getCosType().equalsIgnoreCase(
							iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
				cosId = tempCosId;
			}
		}

		if (task != null && isSubscriberDeactivated(subscriber)
				&& task.containsKey(WebServiceConstants.param_packCosId)) {
			String tempCosId = task
					.getString(WebServiceConstants.param_packCosId);
			CosDetails finalCos = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(tempCosId);
			if (finalCos != null
					&& finalCos.getCosType().equalsIgnoreCase(
							iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
				cosId = tempCosId;
			}
		}

		CosDetails cosDetails = null;
		if (cosId != null) {
			cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
					.getCosDetail(cosId);
			if (cosDetails != null
					&& cosDetails.getCosType().equalsIgnoreCase(
							iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)) {
				maxDownloadsAllowed = cosDetails.getFreeSongs();
			}
		}

		maxDownloadsAllowed = maxDownloadsAllowed >= packNumMaxSelection ? maxDownloadsAllowed
				: packNumMaxSelection;

		// if(muiscPackCosIdParam != null) {
		// Subscriber subscriber = null;
		// if (task != null &&
		// task.containsKey(WebServiceConstants.param_subscriber))
		// subscriber = (Subscriber)
		// task.get(WebServiceConstants.param_subscriber);
		// else
		// subscriber = getSubscriber(subscriberID);
		// String subStatus = Utility.getSubscriberStatus(subscriber);
		//
		// musicPackCosIdList =
		// ListUtils.convertToList(muiscPackCosIdParam.getValue(), ",");
		//
		//
		// // if(!isSubscriberDeactivated(subscriber)) {
		// HashMap<String, String> extraInfo = getExtraInfoMap(subscriber);
		// if (extraInfo != null && extraInfo.containsKey(EXTRA_INFO_PACK)) {
		// String subscriberCosIds = extraInfo.get(EXTRA_INFO_PACK);
		//
		// // Get the entries from provisioning requests table by
		// // subscriber id and type i.e. cosid if status is 33
		// // then update sub_type to azaan.
		// List<String> subscriberCosIdList = ListUtils.convertToList(
		// subscriberCosIds, ",");
		// Set<String> commonCosIds = ListUtils.intersection(
		// musicPackCosIdList, subscriberCosIdList);
		// logger.debug("MusicPack cos Ids: " + commonCosIds
		// + " for subscriber: " + subscriberID);
		// // One of the subscriber cos id is MusicPack cos. So, update the
		// // player status.
		// Iterator<String> iterator = commonCosIds.iterator();
		// if (iterator.hasNext()) {
		// int cos = Integer.parseInt(iterator.next());
		// packNumMaxSelection = isPackActive(subscriberID, cos);
		// cosId = cos + "";
		// } else {
		// logger.debug("Not adding sub_type. subscriberCosIds: "
		// + subscriberCosIds + ", are not configured: "
		// + musicPackCosIdList);
		// }
		//
		// }
		// // }
		// //
		// //
		// if (task != null && isSubscriberDeactivated(subscriber) &&
		// task.containsKey(WebServiceConstants.param_cosID)) {
		// String tempCosId = task.getString(WebServiceConstants.param_cosID);
		// cosId = musicPackCosIdList.contains(tempCosId) ? tempCosId : cosId;
		// }
		//
		// if(task != null && isSubscriberDeactivated(subscriber) &&
		// task.containsKey(WebServiceConstants.param_packCosId)) {
		// String tempCosId =
		// task.getString(WebServiceConstants.param_packCosId);
		// cosId = musicPackCosIdList.contains(tempCosId) ? tempCosId : cosId;
		// }
		//
		// if(cosId != null) {
		// CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager()
		// .getCosDetail(cosId);
		// if( cosDetails != null) {
		// maxDownloadsAllowed = cosDetails.getFreeSongs();
		// }
		// }
		//
		// maxDownloadsAllowed = maxDownloadsAllowed >= packNumMaxSelection ?
		// maxDownloadsAllowed : packNumMaxSelection;
		// }

		if (param != null && maxDownloadsAllowed == -1) {
			try {
				maxDownloadsAllowed = Integer.parseInt(param.getValue());
			} catch (NumberFormatException e) {
				logger.error("", e);
			}
		}

		if (maxDownloadsAllowed > 0) {
			SubscriberDownloads[] subDownloads = getNonDeactiveSubscriberDownloads(subscriberID);

			if ((subDownloads != null && subDownloads.length >= maxDownloadsAllowed)) {
				if (cosDetails != null
						&& cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT)
						&& maxDownloadsAllowed == packNumMaxSelection
						&& task != null) {
					task.put("MUSIC_PACK_DOWNLOAD_REACHED", "TRUE");
				}
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.content.database.RBTDBManager#
	 * updateSubscriberId(java.lang.String, java.lang.String)
	 */
	public String updateSubscriberId(String newSubscriberId, String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			Subscriber sub = getSubscriber(subID(newSubscriberId));
			if (sub != null)
				return "FAILURE:NEW MSISDN ALREADY EXISTS";
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if (subscriber == null)
				return "FAILURE:MSISDN DOESN'T EXIST";
			if (!isValidPrefix(newSubscriberId))
				return "FAILURE:NEW MSISDN INVALID";
			boolean success = SubscriberImpl.updateSubscriberId(conn,
					subID(newSubscriberId), subID(subscriberId));
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
			if (success) {
				Date smsSentdate = new Date();
				ViralSMSTableImpl.insert(conn, subscriberId, smsSentdate,
						"CHANGEMSISDN", newSubscriberId, null, 0, null, null,
						null);
			}
			return success ? "SUCCESS" : "FAILURE:TECHNICAL FAULT";
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL FAULT";
	}

	public String getChargeClass(String subscriberID, String circleID,
			String subscriberWavFile, String classType,
			boolean useUIChargeClass, int categoryID, int categoryType,
			String categoryClassType, Date clipEndTime, String selectedBy,
			String clipClassType) {
		if (classType == null)
			classType = "DEFAULT";
		subscriberID = subID(subscriberID);
		String nextClass = null;
		if (categoryType == 0) {
			Category category = getCategory(categoryID);
			String catChargeClass = null;
			if (category != null && category.getClassType() != null)
				catChargeClass = category.getClassType();

			if (catChargeClass == null
					|| catChargeClass.equalsIgnoreCase("null")
					|| catChargeClass.equals(""))
				catChargeClass = "DEFAULT";
			ChargeClass charge = CacheManagerUtil.getChargeClassCacheManager()
					.getChargeClass(classType);
			ChargeClass catCharge = CacheManagerUtil
					.getChargeClassCacheManager()
					.getChargeClass(catChargeClass);

			if ((classType == null || classType.equalsIgnoreCase("DEFAULT"))
					&& charge != null && catCharge != null
					&& charge.getAmount() != null
					&& catCharge.getAmount() != null) {
				try {
					String firstAmountStr = charge.getAmount();
					String secondAmountStr = catCharge.getAmount();
					firstAmountStr = firstAmountStr.replace(",", ".");
					secondAmountStr = secondAmountStr.replace(",", ".");

					float firstAmount = Float.parseFloat(firstAmountStr);
					float secondAmount = Float.parseFloat(secondAmountStr);
					if ((firstAmount < secondAmount)
							|| (m_overrideChargeClasses != null && m_overrideChargeClasses
									.contains(catChargeClass.toLowerCase())))
						classType = catChargeClass;
				} catch (Throwable e) {
				}
			}
			nextClass = classType;
		} else {
			nextClass = getNextChargeClass(subscriberID);
			if (nextClass == null)
				nextClass = "DEFAULT";
			if (nextClass.equalsIgnoreCase("DEFAULT"))
				nextClass = classType;
			if (nextClass == null || nextClass.equalsIgnoreCase("DEFAULT")) {

				Category category = getCategory(categoryID);
				if ((classType == null || classType.equalsIgnoreCase("DEFAULT"))
						&& category != null && category.getClassType() != null)
					classType = category.getClassType();

				if (classType == null || classType.equalsIgnoreCase("null")
						|| classType.equals(""))
					classType = "DEFAULT";
				ChargeClass charge = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass clipCharge = null;
				if (clipClassType != null) {
					clipCharge = CacheManagerUtil.getChargeClassCacheManager()
							.getChargeClass(clipClassType);
				}

				if (charge != null && clipCharge != null
						&& charge.getAmount() != null
						&& clipCharge.getAmount() != null) {
					try {
						String firstAmountStr = charge.getAmount();
						String secondAmountStr = clipCharge.getAmount();
						firstAmountStr = firstAmountStr.replace(",", ".");
						secondAmountStr = secondAmountStr.replace(",", ".");

						float firstAmount = Float.parseFloat(firstAmountStr);
						float secondAmount = Float.parseFloat(secondAmountStr);
						if ((firstAmount < secondAmount)
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses
										.contains(clipClassType.toLowerCase())))
							classType = clipClassType;
					} catch (Throwable e) {
					}
				}
				nextClass = classType;
			}
		}

		if (m_overrideChargeClasses != null && classType != null
				&& m_overrideChargeClasses.contains(classType.toLowerCase()))
			nextClass = classType;
		return nextClass;
	}

	private int isPackActive(String subscriberId, int cos) {
		logger.debug("Checking pack status. subscriberId: " + subscriberId
				+ ", cos: " + cos);
		List<ProvisioningRequests> provRequests = getAciveProvisioningRequests(
				subscriberId, cos);
		if (provRequests.size() > 0) {
			ProvisioningRequests provisioningRequests = provRequests.get(0);
			int status = provisioningRequests.getStatus();
			if (status == PACK_ACTIVATED || status == PACK_TO_BE_ACTIVATED
					|| status == PACK_ACTIVATION_PENDING) {
				logger.info("Returning true, pack is status active. provisioning"
						+ " requestId: "
						+ provisioningRequests.getRequestId()
						+ ", transId: " + provisioningRequests.getTransId());
				return provisioningRequests.getNumMaxSelections();
			}
		}
		logger.info("Returning false, pack is status NOT active. "
				+ "provRequests: " + provRequests);
		return -1;
	}

}
