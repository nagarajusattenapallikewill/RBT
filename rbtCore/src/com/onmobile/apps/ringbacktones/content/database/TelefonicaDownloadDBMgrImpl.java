package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.util.TPTransactionLogger;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.vodafoneqatar.DownloadSetTimeComparator;
import com.onmobile.common.exception.OnMobileException;

public class TelefonicaDownloadDBMgrImpl extends GrameenDbMgrImpl {

	Logger log = Logger.getLogger(VodafoneSpainDbMgrImpl.class);
	private static Logger cdr_logger = Logger.getLogger("TEF_LIMIT_EXCEED_LOGGER");
	static MiPlaylistDBImpl miPlaylistDBImpl = null;

	private static String cricClassType = RBTParametersUtils.getParamAsString(WEBSERVICE, "BYPASS_CRICKET_CHARGE_CLASS", null);

	private static List<String> cricClassList = new ArrayList<String>();

	static {
		miPlaylistDBImpl = MiPlaylistDBImpl.getInstance();
		if (cricClassType != null) {
			String[] cricClassArray = cricClassType.split(",");
			cricClassList = Arrays.asList(cricClassArray);
		}
	}

	// RBT-14044 VF ES - MI Playlist functionality for RBT core
	public String removeMiPlaylistDownloadTrack(String subscriberID, String promoID, int categoryID, int categoryType,
			String callerId, int status) {

		boolean removed = miPlaylistDBImpl.removeMiPlaylistTrackFromDownload(subscriberID, promoID, categoryID, categoryType,
				callerId, status);
		log.info("Remove status in removeMiPlaylistDownloadTrack of download is: " + removed);
		return removed ? "true" : "false";
	}

	public String addDownloadForTrackingMiPlaylist(String subID, String promoID, int catID, int catType, String refId,
			String classType, String selBy, int status, int selType) {
		String resp = null;
		if (!(status == 90 || status == 99 || selType == 2)
				&& null == getActiveSubscriberDownloadByStatus(subID, promoID, "t", catID, catType)) {
			resp = miPlaylistDBImpl.addDownloadRowforTracking(subID, promoID, catID, null, false, catType, classType, selBy,
					null, null, false, refId);
		}
		log.info("Download inserted in addDownloadForTracking for miplaylist :" + resp);
		return resp;

	}

	@Override
	public char getLoopStatusForNewMiPlayListSelection(String subscriberID, int status, String callerID, int catType,
			char loopStatus) {
		loopStatus = miPlaylistDBImpl.getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID, catType, loopStatus);
		return loopStatus;
	}

	@Override
	public SubscriberDownloads[] getSubscriberDownloadsByDownloadStatus(String subscriberID, String downloadStatus) {
		SubscriberDownloads[] subscriberDownloads = null;
		subscriberDownloads = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID), downloadStatus);
		return subscriberDownloads;
	}

	public void addOldMiplayListSelections(SubscriberStatus subscriberStatus) {
		miPlaylistDBImpl.addOldMiplayListSelections(subscriberStatus);
	}

	public SubscriberDownloads getSubscriberDownload(String subscriberId, String wavFile, int categoryID, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownload(conn, subID(subscriberId), wavFile, categoryID, categoryType,
					true);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	public SubscriberDownloads getActiveSubscriberDownloadByStatus(String subscriberId, String promoId, String downloadStatus,
			int categoryId, int categoryType) {
		SubscriberDownloads activeSubscriberDownloadByStatus = miPlaylistDBImpl.getActiveSubscriberDownloadByStatus(
				subID(subscriberId), promoId, downloadStatus, categoryId, categoryType);
		return activeSubscriberDownloadByStatus;
	}

	public SubscriberDownloads getDownloadToBeDeactivated(String subscriberID, String promoID, int categoryId, int categoryType) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getDownloadToBeDeactivated(conn, subID(subscriberID), promoID, categoryId,
					categoryType, true);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	@Override
	public boolean expireSubscriberDownload(String subscriberId, String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo, boolean isDirectDeactivation) {
		return expireSubscriberDownloadAndUpdateExtraInfo(subscriberId, wavFile, categoryId, categoryType, deactivateBy,
				deselectionInfo, null, isDirectDeactivation);
	}

	@Override
	public boolean expireSubscriberDownloadAndUpdateExtraInfo(String subscriberId, String wavFile, int categoryId,
			int categoryType, String deactivateBy, String deselectionInfo, String extraInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.expireSubscriberDownload(conn, subID(subscriberId), wavFile, deactivateBy, categoryId,
					categoryType, deselectionInfo, extraInfo, true, isDirectDeactivation);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	

	public SubscriberStatus[] getPendingDefaultSubscriberSelections(String subID, String callerID, int status,
			String shuffleSetTime) {

		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberStatusImpl.getPendingDefaultSubscriberSelections(conn, subID, callerID, status, shuffleSetTime);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;

	}

	public boolean removeDownloadsWithTStatus(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		try {
			boolean deleted = SubscriberDownloadsImpl.removeDownloadsWithTStatus(conn, subscriberID);
			log.info("status of deleting downloads with t status is : " + deleted);
			return deleted;
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public String addSubscriberSelections(String subscriberID, String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status, String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath, int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode, String regexType, String subYes, String promoType,
			String circleID, boolean incrSelCount, boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType, String selInterval, HashMap extraInfo,
			boolean useUIChargeClass, String refID, boolean isDirectActivation) {
		return addSubscriberSelections(subscriberID, callerID, categories, clipMap, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, freePeriod, isPrepaid, changeSubType, messagePath, fromTime, toTime, chargeClassType,
				smActivation, doTODCheck, mode, regexType, subYes, promoType, circleID, incrSelCount, useDate, transID, OptIn,
				isTata, inLoop, subClass, sub, rbtType, selInterval, extraInfo, useUIChargeClass, false, null);
	}

	public String addSubscriberSelections(String subscriberID, String callerID, Categories categories, HashMap clipMap,
			Date setTime, Date startTime, Date endTime, int status, String selectedBy, String selectionInfo, int freePeriod,
			boolean isPrepaid, boolean changeSubType, String messagePath, int fromTime, int toTime, String chargeClassType,
			boolean smActivation, boolean doTODCheck, String mode, String regexType, String subYes, String promoType,
			String circleID, boolean incrSelCount, boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType, String selInterval, HashMap extraInfo,
			boolean useUIChargeClass, boolean isSmClientModel, HashMap<String, String> responseParams) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		int count = 0;
		String addResult = null;
		try {
			if (sub != null && sub.subYes() != null && sub.subYes().equalsIgnoreCase("Z")) {
				String subscriberExtraInfo = sub.extraInfo();
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
				if (subExtraInfoMap.get("VOLUNTARY") != null && subExtraInfoMap.get("VOLUNTARY").equals("SM_SUSPENDED")) {
					log.info(subscriberID + " is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				}
			}

			if (categories != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(categories.type())) {
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;
			}
			Date nextChargingDate = null;
			Date startDate = startTime;
			String selectInfo = selectionInfo;
			String sel_status = STATE_BASE_ACTIVATION_PENDING;
			int nextPlus = -1;
			boolean updateEndDate = false;
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			boolean isShuffleCategory = false;
			SimpleDateFormat currentdateFormat = new SimpleDateFormat(iRBTConstant.kDateFormatwithTime);
			String currentTime = currentdateFormat.format(new Date());
			if (subscriberID != null && callerID != null && subscriberID.equals(callerID))
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

			if (callerID != null) {
				Groups[] groups = GroupsImpl.getGroupsForSubscriberID(conn, subscriberID);
				if (groups != null && groups.length > 0) {
					int[] groupIDs = new int[groups.length];
					for (int i = 0; i < groups.length; i++) {
						groupIDs[i] = groups[i].groupID();
					}
					GroupMembers groupMember = GroupMembersImpl.getMemberFromGroups(conn, callerID, groupIDs);
					if (groupMember != null) {
						for (Groups group : groups) {
							if (groupMember.groupID() == group.groupID()) {
								if (group.preGroupID() != null && group.preGroupID().equals("99")) // Blocked
																									// Caller
									return SELECTION_FAILED_CALLER_BLOCKED;
								else if (group.preGroupID() != null && !group.preGroupID().equals("98")) // caller
																											// is
																											// in
																											// caller
																											// group.
																											// so
																											// can
																											// be
																											// ignored
									return SELECTION_FAILED_CALLER_ALREADY_IN_GROUP;
							}
						}
					}
				}
			}

			if (sub != null && rbtType != 2)
				rbtType = sub.rbtType();
			String subExtraInfo = sub.extraInfo();
			HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subExtraInfo);
			boolean voluntarySuspended = false;
			if (extraInfoMap != null && extraInfoMap.containsKey(VOLUNTARY)
					&& extraInfoMap.get(VOLUNTARY).equalsIgnoreCase("TRUE")) {
				voluntarySuspended = true;
			}
			if (sub != null && sub.subYes() != null && (sub.subYes().equals("Z") || sub.subYes().equals("z"))
					&& !(sub.subYes().equalsIgnoreCase("Z") && voluntarySuspended)) {
				log.info(subscriberID + " is suspended. Returning false.");
				return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
			}

			boolean isSelSuspended = false;
			if (m_checkForSuspendedSelection)
				isSelSuspended = isSelSuspended(subscriberID, callerID);

			if (isSelSuspended) {
				log.info("selection of " + subscriberID + " for " + callerID + " is suspended. Returning false.");
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
			String clipGrammar = null;
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

			if (subYes != null && (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes.equalsIgnoreCase(STATE_EVENT))) {
				if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null && m_subOnlyChargeClass.containsKey(subClass)) {
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				if (categories != null && (categories.type() == DAILY_SHUFFLE || categories.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				if (!useUIChargeClass && clipClassType != null && !clipClassType.equalsIgnoreCase("DEFAULT") && classType != null
						&& !clipClassType.equalsIgnoreCase(classType)) {
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null && catCharge.getAmount() != null
							&& clipCharge.getAmount() != null) {
						try {
							float firstAmount = Float.parseFloat(catCharge.getAmount());
							float secondAmount = Float.parseFloat(clipCharge.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses.contains(clipClassType
											.toLowerCase())))
								classType = clipClassType;
						} catch (Throwable e) {
						}
					}
					if (clipClassType.startsWith("TRIAL") && categories != null && categories.id() != 26)
						classType = clipClassType;
				}
			}

			if (!useUIChargeClass && chargeClassType != null) {
				ChargeClass first = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
				ChargeClass second = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClassType);

				if (first != null && second != null && first.getAmount() != null && second.getAmount() != null) {
					try {
						float firstAmount = Float.parseFloat(first.getAmount());
						float secondAmount = Float.parseFloat(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType.equalsIgnoreCase("DEFAULT_10")
								|| (m_overrideChargeClasses != null && m_overrideChargeClasses.contains(chargeClassType
										.toLowerCase())))
							classType = chargeClassType;
					} catch (Throwable e) {
						classType = chargeClassType;
					}
				} else {
					classType = chargeClassType;
				}

				if (first != null && first.getChargeClass().startsWith("TRIAL") && categories != null && categories.id() != 26)
					classType = first.getChargeClass();
			}

			if (!useUIChargeClass && categories != null && categories.type() == 10 && m_modeChargeClass != null
					&& m_modeChargeClass.containsKey(selectedBy))
				classType = (String) m_modeChargeClass.get(selectedBy);

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn, subID(subscriberID), null);
				if (viralSMS != null)
					selectInfo = selectInfo + ":" + "viral";
			}

			String prepaid = "n";
			if (isPrepaid)
				prepaid = "y";

			/**
			 * If user enabled UDS , then all his selections should go in Loop
			 */
			if (!inLoop && status == 1) {
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
				if (subExtraInfoMap != null && subExtraInfoMap.containsKey(UDS_OPTIN)) {
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
				}

				if (inLoop) {
					if (isShufflePresentSelection(subID(subscriberID), callerID, 0))
						inLoop = false;
					else if (categories.type() == 0 || categories.type() == 10 || categories.type() == 11
							|| categories.type() == 12 || categories.type() == 20)
						return SELECTION_FAILED_SHUFFLES_FOR_UDA_OPTIN;
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

						SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
						Date currentDate = new Date();
						parseDate = dateFormat.parse(date);
						if (parseDate.before(currentDate) || parseDate.equals(currentDate)) {
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
			 * Since Sprint 4 RBT 2.0, RBT 15670 One more parameter udpId has
			 * been added in getSubscriberSelections method. If udpId is present
			 * then query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*
			 * if(extraInfo.containsKey(WebServiceConstants.param_udpId)) udpId
			 * = (String) extraInfo.get(UDP_ID);
			 */
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl.getSubscriberSelections(conn, subID(subscriberID),
					subID(callerID), rbtType, udpId);

			if (fromTime == 0 && toTime == 2359 && status == 80)
				status = 1;

			/* time of the day changes */
			SubscriberStatus subscriberStatus = getAvailableSelection(conn, subID(subscriberID), subID(callerID),
					subscriberSelections, categories, subscriberWavFile, status, fromTime, toTime, startDate, endDate,
					doTODCheck, inLoop, rbtType, selInterval, selectedBy);
			if (subscriberStatus == null) {
				log.info("RBT::no matches found");
				if (inLoop && (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0) && !m_putSGSInUGS)
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(conn, subID(subscriberID), subID(callerID),
						status, rbtType);
				if (subscriberStatus != null) {
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE && !m_putSGSInUGS)
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
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				// RBT-14044 VF ES - MI Playlist functionality for RBT core
				loopStatus = getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID, categories.type(), loopStatus);

				String actBy = null;
				if (sub != null)
					actBy = sub.activatedBy();

				if (m_trialChangeSubTypeOnSelection && actBy != null && actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if (!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT", sub))
							return SELECTION_FAILED_TNB_TO_DEFAULT_FAILED;
					}
				}

				if (!useUIChargeClass) {
					if (status == 80 && rbtType == 2) {
						classType = clipClassType;
					} else {
						for (int i = 0; subscriberSelections != null && i < subscriberSelections.length; i++) {
							if (subscriberSelections[i].selType() == 2) {
								HashMap selectionExtraInfo = DBUtility
										.getAttributeMapFromXML(subscriberSelections[i].extraInfo());
								int campaignId = -1;

								if (selectionExtraInfo != null && selectionExtraInfo.containsKey(iRBTConstant.CAMPAIGN_ID)
										&& selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID) != null) {

									try {
										campaignId = Integer.parseInt("" + selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID));
									} catch (Exception e) {
										campaignId = -1;
									}
								}
								log.info("The value of campaign id - " + campaignId);
								if (campaignId != -1) {
									RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO.getRBTBulkUploadTask(campaignId);

									if (m_corporateDiscountChargeClass != null
											&& m_corporateDiscountChargeClass.containsKey(bulkUploadTask.getTaskMode())) {
										log.info("The value of m_corporateDiscountChargeClass id - "
												+ m_corporateDiscountChargeClass.toString());
										HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass.get(bulkUploadTask
												.getTaskMode());
										if (discountClassMap != null && discountClassMap.containsKey(classType))
											classType = (String) discountClassMap.get(classType);
									}
								}
								break;
							}

						}
					}
				}

				// Added by Sreekar for Vodafone Spain 20/01/2013
				boolean addSelectionInVoulentarySuspension = false;
				if (CacheManagerUtil.getParametersCacheManager()
						.getParameter(COMMON, "ADD_SELECTION_FOR_VOULENTARY_SUSPENSION_USERS", "FALSE").getValue()
						.equalsIgnoreCase("true"))
					addSelectionInVoulentarySuspension = true;

				boolean isPackSel = false;
				String packCosID = null;
				String provRefId = UUID.randomUUID().toString();

				// status 2 has been added by Sreekar for feature RBT-4119 (bug
				// RBT-4291)
				if ((status == 1 || status == 75 || status == 79 || status == 80 || status == 92 || status == 93 || status == 95 || status == 2)
						&& rbtType != 2) {
					sel_status = STATE_BASE_ACTIVATION_PENDING;

					boolean isSubActive = false;
					if (subYes != null && (subYes.equalsIgnoreCase(STATE_ACTIVATED) || subYes.equalsIgnoreCase(STATE_EVENT))) {
						if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status, callerID))
							isSubActive = true;
					}

					if (!isSubActive && sub.subYes().equalsIgnoreCase("Z")) {
						String subscriberExtraInfo = sub.extraInfo();
						HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
						if (addSelectionInVoulentarySuspension && subExtraInfoMap != null
								&& subExtraInfoMap.get("VOLUNTARY") != null && subExtraInfoMap.get("VOLUNTARY").equals("TRUE")) {
							isSubActive = true;
						}
					}

					if (Utility.isShuffleCategory(categories.type())) {
						isShuffleCategory = true;
					}

					addResult = addSubscriberDownloadRows(subscriberID, subscriberWavFile, categories, null, isSubActive,
							classType, selectedBy, selectInfo, extraInfo, incrSelCount, useUIChargeClass, isSmClientModel,
							responseParams, null, -1, null, null, provRefId);
					if(addResult.indexOf(DOWNLOAD_MONTHLY_LIMIT_REACHED) != -1){
						cdr_logger.info(currentTime + "," + subscriberID + "," + categories.id() + "," + subscriberWavFile + ","
								+ sub.maxSelections() + "," + selectedBy);
					}
					if (addResult.indexOf("SUCCESS:DOWNLOAD_ALREADY_ACTIVE") != -1) {
						sel_status = STATE_TO_BE_ACTIVATED;
						sendAcknowledgementSMS(subscriberID, false, categories.id());
					} else if (addResult.indexOf("SUCCESS:DOWNLOAD_GRACE") != -1)
						sel_status = STATE_BASE_ACTIVATION_PENDING;
					else if (addResult.indexOf("SUCCESS") == -1)
						return addResult;
					log.info("ADD RESULT === " + addResult);
					if (addResult.indexOf("SUCCESS") != -1 && !addResult.equalsIgnoreCase("SUCCESS:DOWNLOAD_ALREADY_ACTIVE")) {
						log.info("Adding Recent Charge Class For Download = " + classType);
						clipMap.put("RECENT_CLASS_TYPE", classType);
					}

					// RBT-16608
					classType = CacheManagerUtil.getParametersCacheManager()
							.getParameter(WEBSERVICE, "SELECTION_DEFAULT_CHARGE_CLASS", "FREE").getValue();
				} else {
					String subPacks = null;
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub.extraInfo());
					if (subExtraInfoMap != null && subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;

					if (status == 90) {
						nextClass = getNextChargeClass(sub);
					} else if (!useUIChargeClass) {
						if (subPacks != null) {
							com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
									.getInstance().getCategory(categories.id());
							Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager.getInstance()
									.getClipByRbtWavFileName(subscriberWavFile);
							CosDetails cosDetail = getCosDetailsForContent(subscriberID, subPacks, category, clipObj, status,
									callerID);
							List<ProvisioningRequests> packList = null;
							if (cosDetail != null) {
								packList = ProvisioningRequestsDao.getBySubscriberIDTypeAndNonDeactivatedStatus(subscriberID,
										Integer.parseInt(cosDetail.getCosId()));
							}
							if (packList != null
									&& (isSubscriberPackActivated(packList.get(0)) || isSubscriberPackActivationPending(packList
											.get(0)))) {
								int selCount = sub.maxSelections();
								if (isPackRequest(cosDetail)) {
									selCount = packList.get(0).getNumMaxSelections();
									if (cosDetail.getFreeSongs() > selCount)
										isPackSel = true;
								}

								nextClass = getChargeClassFromCos(cosDetail, selCount);
								packCosID = cosDetail.getCosId();
							} else {
								nextClass = getNextChargeClass(sub);
							}
						} else {
							nextClass = getNextChargeClass(sub);
						}
					}

					if (nextClass != null && cricClassList.contains(nextClass) && status == 90) {
						classType = nextClass;
					} else if (nextClass != null && !nextClass.equalsIgnoreCase("DEFAULT") && status != 90) {
						classType = nextClass;
					}

				}

				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID), inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				// Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON",
						"SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if (parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID), subID(callerID), status,
						fromTime, toTime, graceDeselectedBy, rbtType);

				if (sub.subYes().equalsIgnoreCase("Z")) {
					String subscriberExtraInfo = sub.extraInfo();
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
					if (!addSelectionInVoulentarySuspension && subExtraInfoMap.get("VOLUNTARY") != null
							&& subExtraInfoMap.get("VOLUNTARY").equals("TRUE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;
					}
				}

				count = createSubscriberStatuses(subscriberID, callerID, categories.id(), subscriberWavFile, setTime, startDate,
						endDate, status, selectedBy, selectInfo, nextChargingDate, prepaid, classType, changeSubType, fromTime,
						toTime, sel_status, true, clipMap, categories.type(), useDate, loopStatus, isTata, nextPlus, rbtType,
						selInterval, extraInfo, null, false, circleID, sub, useUIChargeClass, false, provRefId);

				log.info("Checking to update num max selections or not. count: " + count + ", isPackSel: " + isPackSel);
				
				if (count == 3) {
					log.info("Download limit reached for subscriber = " + subscriberID + "category id = " + categories.id()
							+ "and clip = " + subscriberWavFile);
					cdr_logger.info(currentTime + "," + subscriberID + "," + categories.id() + "," + subscriberWavFile + ","
							+ sub.maxSelections() + "," + selectedBy);
					return WebServiceConstants.DOWNLOAD_MONTHLY_LIMIT_REACHED;
				}
				if (isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);
				else if (cricClassList.contains(classType) && incrSelCount && count == 1) {
					SubscriberImpl.updateNumMaxSelections(conn, subscriberID, sub.maxSelections() + 1);
				}

				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
				}
			} else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		
		if (count > 0)
			return SELECTION_SUCCESS;
		else
			return SELECTION_FAILED_INTERNAL_ERROR;
	}

	public int createSubscriberStatuses(String subscriberID, String callerID, int categoryID, String subscriberWavFile,
			Date setTime, Date startTime, Date endTime, int status, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, String classType, boolean changeSubType, int fromTime, int toTime,
			String sel_status, boolean smActivation, HashMap clipMap, int categoryType, boolean useDate, char loopStatus,
			boolean isTata, int nextPlus, int rbtType, String selInterval, HashMap extraInfoMap, String refID,
			boolean isDirectActivation, String circleId, Subscriber sub, boolean useUIChargeClass, boolean isFromDownload,
			String provRefId) {

		log.info("Adding subscriber selections, subscriberId: " + subscriberID + ", classType: " + classType + ", extraInfoMap: "
				+ extraInfoMap);

		Connection conn = getConnection();
		if (conn == null)
			return 0;

		int count = 0;
		try {
			if (!isTata) {
				subscriberID = subID(subscriberID);
				callerID = subID(callerID);
			}
			if (isTata)
				smActivation = false;

			if (isDirectActivation) {
				Date curDate = new Date();
				nextChargingDate = curDate;
				startTime = curDate;
				nextPlus = 0;
				sel_status = STATE_ACTIVATED;
			}
			SubscriberStatus subscriberStatus = null;

			Category category = getCategory(categoryID);

			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				extraInfoMap.put("PROV_REF_ID", provRefId);
			}
			String selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				SubscriberDownloads[] subscriberDownloads = getSubscriberActiveDownloadsByDownloadStatusAndCategory(subscriberID,
						categoryID, categoryType);

				log.info("going for ODA Shuffle Selections....");
				log.info("Shuffle downloads ...=" + Arrays.toString(subscriberDownloads));
				if (subscriberDownloads != null) {
					boolean isFreemiumSubscriber = false;
					boolean isUpgradeRequired = false;
					if (clipMap.containsKey("FREEMIUM_USER")) {
						isFreemiumSubscriber = true;
					}

					if (clipMap.remove("UPGRADE_REQUIRED") != null) {
						isUpgradeRequired = true;
					}

					// Added for TS-6705
					SubscriberDownloads subDownload = getSubscriberDownloadsByDownloadStatus(subscriberID, categoryID,
							categoryType, "t");
					if (subDownload != null) {
						classType = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, "PLAY_LIST_SONG_CHARGE_CLASS",
								classType);
						isFreemiumSubscriber = true;
					}
					// End of TS-6705

					String provisioningRefId = insertODAPackProvisioningRequestAndGetRefId(subID(subscriberID), category,
							selectedBy, selectionInfo, provRefId, callerID, fromTime, toTime, status, selInterval, classType,
							isFreemiumSubscriber, isUpgradeRequired);

					int selCount = 0;
					log.info("provisioningRefId :" + provisioningRefId);
					for (SubscriberDownloads subscriberDownload : subscriberDownloads) {
						if (selCount == 0) {
							loopStatus = 'o';
						} else {
							loopStatus = 'l';
						}
						HashMap<String, String> downloadExtraInfo = new HashMap<String, String>();
						downloadExtraInfo = DBUtility.getAttributeMapFromXML(subscriberDownload.extraInfo());
						extraInfoMap.put("PROV_REF_ID", provisioningRefId);
						selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

						Clip clip = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager.getInstance()
								.getClipByRbtWavFileName(subscriberDownload.promoId());
						subscriberWavFile = clip.getClipRbtWavFile();
						String odaSelRefID = UUID.randomUUID().toString();
						if ((subscriberDownload.downloadStatus() + "").equals(STATE_DOWNLOAD_TO_BE_ACTIVATED + "")
								|| (subscriberDownload.downloadStatus() + "").equals("w")) {
							sel_status = "W";
						} else {
							sel_status = "A";
						}
						classType = "FREE";
						subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID), callerID, categoryID,
								subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy, selectionInfo,
								nextChargingDate, prepaid, fromTime, toTime, smActivation, sel_status, null, null, categoryType,
								loopStatus, nextPlus, rbtType, selInterval, selExtraInfo, odaSelRefID, circleId, null, null);
						if (subscriberStatus != null) {
							selCount++;
						}
					}
				} else {
					return 3;
				}

			} else {
				String contentType = null;
				if (subscriberWavFile != null && !subscriberWavFile.equals("")) {
					Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberWavFile);
					if (clip != null) {
						contentType = clip.getContentType();
					}
				}
				loopStatus = miPlaylistDBImpl.getLoopStatusForNewMiPlayListSelection(subscriberID, status, callerID,
						categoryType, loopStatus, contentType);
				// Added for TS-6705
				String myPlaylistChargeClass = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE,
						"MY_PLAY_LIST_FIRST_SONG_CHARGE_CLASS", "DEFAULT");
				SubscriberDownloads subDownload = getSubscriberDownloadsByDownloadStatus(subscriberID, subscriberWavFile, "t");
				if (subDownload != null && classType != null && classType.equalsIgnoreCase(myPlaylistChargeClass)) {
					log.info("Selection waveFileName:-->" + subscriberWavFile + " subDownload object: " + subDownload.toString()
							+ " classType: " + classType);
					classType = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, "MY_PLAY_LIST_SONG_CHARGE_CLASS",
							classType);
				}
				// End of TS-6705
				subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID), callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status, classType, selectedBy, selectionInfo,
						nextChargingDate, prepaid, fromTime, toTime, smActivation, sel_status, null, null, categoryType,
						loopStatus, nextPlus, rbtType, selInterval, selExtraInfo, refID, circleId, null, null);
			}

			if (subscriberStatus == null) {
				log.warn("Selection is not populated into DB, refId: " + refID + ". Returning count: 0");
				return 0;
			} else {
				if (!clipMap.containsKey("RECENT_CLASS_TYPE")) {
					log.info("Adding Recent class type for selection = " + classType);
					clipMap.put("RECENT_CLASS_TYPE", classType);
				}
				log.info("Selection is inserted into Selections table" + ". Returning count: 1");
				count = 1;
			}

			int clipID = -1;
			String clipName = null;
			if (clipMap != null) {
				String s = (String) clipMap.get("CLIP_ID");
				try {
					clipID = Integer.parseInt(s);
				} catch (Exception e) {
					clipID = -1;
				}
				clipName = (String) clipMap.get("CLIP_NAME");
			}
			if (clipID != -1) {
				Date currentDate = null;
				if (useDate) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					currentDate = calendar.getTime();
				}
				Date date = new Date(System.currentTimeMillis());

				DateFormat timeFormatYYYY = new SimpleDateFormat("yyyy");
				String year = timeFormatYYYY.format(date);
				if (currentDate != null)
					year = timeFormatYYYY.format(currentDate);

				DateFormat timeFormatMM = new SimpleDateFormat("MM");
				String month = timeFormatMM.format(date);
				if (currentDate != null)
					month = timeFormatMM.format(currentDate);

				Access access = AccessImpl.getAccess(conn, clipID, year, month, currentDate);
				if (access == null)
					access = AccessImpl.insert(conn, clipID, clipName, year, month, 0, 0, 0, currentDate);
				else {
					access.incrementNoOfAccess();
					if (subscriberWavFile != null && subscriberWavFile.indexOf("rbt_ugc_") != -1)
						access.incrementNoOfPlays();
					access.update(conn);
				}
			}
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		log.info("Returning count: " + count);
		return count;

	}

	public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, int categoryId, int categoryType,
			String downloadStatus) {
		SubscriberDownloads subscriberDownload = null;
		subscriberDownload = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID), categoryId,
				categoryType, downloadStatus);
		return subscriberDownload;
	}

	public SubscriberDownloads getSubscriberDownloadsByDownloadStatus(String subscriberID, String wavFileName,
			String downloadStatus) {
		SubscriberDownloads subscriberDownload = null;
		subscriberDownload = miPlaylistDBImpl.getSubscriberDownloadsByDownloadStatus(subID(subscriberID), wavFileName,
				downloadStatus);
		return subscriberDownload;
	}

	protected String insertODAPackProvisioningRequestAndGetRefId(String subscriberID, Category category, String mode,
			String modeInfo, String transId, String callerId, int fromTime, int toTime, int status, String selInterval,
			String classType, boolean isFreemiumSubscriber, boolean isUpgradeRequired) {
		if (category == null || category.getType() != PLAYLIST_ODA_SHUFFLE) {
			log.info("Category type is not ODA Shuffle");
			return null;
		}

		log.info("insertODAPackProvisioningRequestAndGetRefId :: subscriberID = " + subscriberID + ", CategoryID = "
				+ category.getID() + " , mode = " + mode + " ,modeInfo = " + modeInfo + " ,transId = " + transId
				+ " , CallerID = " + callerId);
		;
		RBTDBManager m_rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = m_rbtDBManager.getSubscriber(subscriberID);
		int packStatus = m_rbtDBManager.getPackStatusToInsert(subscriber);
		int type = category.getID();
		String chargingClass = category.getClassType();
		if (isFreemiumSubscriber) {
			log.info("Freemium  Subscriber. Hence using cos based classtype");
			chargingClass = classType;
			// packStatus = 30;
			// status = 30;
		}

		if (isUpgradeRequired) {
			packStatus = 30;
		}

		ProvisioningRequests provisioningReqs = new ProvisioningRequests(subscriberID, type, mode, modeInfo, transId,
				chargingClass, packStatus);
		HashMap<String, String> xtraInfoMap = new HashMap<String, String>();
		boolean isSpecialCallerRequest = false;
		if (callerId != null) {
			xtraInfoMap.put("CALLER_ID", callerId);
			isSpecialCallerRequest = true;
		}

		xtraInfoMap.put("FROM_TIME", fromTime + "");
		xtraInfoMap.put("TO_TIME", toTime + "");
		xtraInfoMap.put("STATUS", status + "");
		if (selInterval != null) {
			xtraInfoMap.put("SEL_INTERVAL", selInterval + "");
		}

		String packExtraInfoXml = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
		provisioningReqs.setExtraInfo(packExtraInfoXml);
		int packNumMaxSelection = -1;
		if (packNumMaxSelection != -1) {
			provisioningReqs.setNumMaxSelections(packNumMaxSelection);
		}
		ProvisioningRequests provisioningRequestsTable = null;
		try {
			SubscriberDownloads[] downLoad = getSubscriberDownloadsByDownloadStatusAndCategory(subscriberID, category.getID(),
					category.getType(), "t");
			List<ProvisioningRequests> provisioningRequests = RBTDBManager.getInstance().getProvisioningRequests(subscriberID,
					category.getID());
			List<ProvisioningRequests> pendingProvisioningRequests = RBTDBManager.getInstance()
					.getPacksToBeActivatedBySubscriberIDAndActpendingType(subscriberID, category.getID());
			if (provisioningRequests != null && !provisioningRequests.isEmpty() && downLoad != null && downLoad.length > 0) {
				boolean isSameSettingAvailable = false;
				String sameSettingRefId = null;
				for (ProvisioningRequests provisioningRequest : provisioningRequests) {
					HashMap<String, String> infoMap = DBUtility.getAttributeMapFromXML(provisioningRequest.getExtraInfo());
					if (isSpecialCallerRequest && infoMap.containsKey("CALLER_ID") && infoMap.get("CALLER_ID").equals(callerId)) {
						if (infoMap.get("FROM_TIME").equals(fromTime + "")
								&& infoMap.get("TO_TIME").equals(toTime + "")
								&& infoMap.get("STATUS").equals(status + "")
								&& ((selInterval != null && infoMap.containsKey("SEL_INTERVAL") && selInterval.equals(infoMap
										.get("SEL_INTERVAL"))) || (selInterval == null && !infoMap.containsKey("SEL_INTERVAL")))) {
							RBTDBManager.getInstance().updateProvisioningRequestsStatusAndExtraInfo(subscriberID,
									provisioningRequest.getTransId(), 33, packExtraInfoXml);

							isSameSettingAvailable = true;
							sameSettingRefId = provisioningRequest.getTransId();

						}

					} else if (!isSpecialCallerRequest && !infoMap.containsKey("CALLER_ID")) {
						if (infoMap.get("FROM_TIME").equals(fromTime + "")
								&& infoMap.get("TO_TIME").equals(toTime + "")
								&& infoMap.get("STATUS").equals(status + "")
								&& ((selInterval != null && infoMap.containsKey("SEL_INTERVAL") && selInterval.equals(infoMap
										.get("SEL_INTERVAL"))) || (selInterval == null && !infoMap.containsKey("SEL_INTERVAL")))) {
							RBTDBManager.getInstance().updateProvisioningRequestsStatusAndExtraInfo(subscriberID,
									provisioningRequest.getTransId(), 33, packExtraInfoXml);
							isSameSettingAvailable = true;
							sameSettingRefId = provisioningRequest.getTransId();

						}
					}

				}

				if (isSameSettingAvailable) {
					RBTDBManager.getInstance().updateProvisioningRequestsStatusAndExtraInfo(subscriberID, sameSettingRefId, 33,
							packExtraInfoXml);
					sendAcknowledgementSMS(subscriberID, true, category.getID());
					return sameSettingRefId;
				} else {
					provisioningReqs.setStatus(33);
					provisioningRequestsTable = m_rbtDBManager.insertProvisioningRequestsTable(provisioningReqs);

				}

			} else {
				if (pendingProvisioningRequests != null && pendingProvisioningRequests.size() > 0) {
					provisioningReqs.setStatus(30);
				}
				provisioningRequestsTable = m_rbtDBManager.insertProvisioningRequestsTable(provisioningReqs);
				log.info("provisioningRequestsTable insertion result  : = " + provisioningRequestsTable != null);
			}
		} catch (OnMobileException e) {
			e.printStackTrace();
		}
		return provisioningRequestsTable.getTransId();
	}

	public void addTrackingOfPendingSelections(SubscriberStatus ss) {

		if (Utility.isShuffleCategory(ss.categoryType())) {
			DateFormat mySqlTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String shuffleSetTime = mySqlTimeFormat.format(ss.setTime());
			SubscriberStatus[] pendingSelections = getPendingDefaultSubscriberSelections(ss.subID(), null, 1, shuffleSetTime);
			if (pendingSelections != null && pendingSelections.length > 0) {
				for (SubscriberStatus sel : pendingSelections) {
					String resp = addDownloadForTrackingMiPlaylist(sel.subID(), sel.subscriberFile(), sel.categoryID(),
							sel.categoryType(), null, sel.classType(), sel.selectedBy(), sel.status(), sel.selType());
					log.info("response of tracking added for selections: " + sel.toString() + " is " + resp);
				}
			}
		}
	}

	@Override
	public boolean isODAPackRequest(String subscriberId, String refId) {
		Connection conn = getConnection();
		if (conn == null)
			return false;
		ProvisioningRequests provisioningRequest = ProvisioningRequestsDao.getByTransId(subscriberId, refId);
		if (provisioningRequest != null) {
			int categoryId = provisioningRequest.getType();
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = RBTCacheManager.getInstance().getCategory(
					categoryId);
			if (category != null) {
				int categoryType = category.getCategoryTpe();
				return categoryType == PLAYLIST_ODA_SHUFFLE;
			}
		}
		return false;
	}

	@Override
	public SubscriberStatus[] getActiveNormalSelByCallerIdAndByStatus(String subscriberID, String callerID, int status) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		SubscriberStatus[] subscriberStatus = null;
		try {
			subscriberStatus = SubscriberStatusImpl.getActiveNormalSelByCallerIdAndByStatus(conn, subID(subscriberID), callerID,
					status);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return subscriberStatus;
	}

	public String addSubscriberDownloadRows(String subscriberId, String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy, String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass, boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, int status, String callerId, String downloadStatus, String provRefId) {
		Connection conn = getConnection();
		boolean incrSelCountParamForGift = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON,
				"INCREMENT_SEL_COUNT_FOR_GIFT", "FALSE");

		if (conn == null)
			return null;

		try {
			SubscriberDownloads results = null;
			ClipMinimal clip = getClipRBT(subscriberWavFile);
			int categoryID = categories.id();
			int categoryType = categories.type();
			if (endDate == null)
				endDate = m_endDate;
			subscriberId = subID(subscriberId);
			String nextClass = null;

			boolean isPackSel = false;
			String packCosID = null;
			String[] chargeClassStr;
			if (useUIChargeClass)
				nextClass = classType;
			else {
				if (com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(categoryType)) {
					chargeClassStr = getChargeClassForShuffleCatgory(subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel, packCosID, selBy, extraInfo, nextClass, classType);
				} else {
					chargeClassStr = getChargeClassForNonShuffleCatgory(subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel, packCosID, selBy, extraInfo, nextClass, classType);
				}
				if (chargeClassStr != null) {
					if (chargeClassStr.length > 4) {
						nextClass = chargeClassStr[2];
						if (nextClass != null
								&& (nextClass.equalsIgnoreCase(SELECTION_FAILED_INTERNAL_ERROR) || nextClass
										.equalsIgnoreCase("FAILURE:TECHNICAL_FAULT"))) {
							return nextClass;
						}
						incrSelCount = (chargeClassStr[0].equalsIgnoreCase("true") ? true : false);
						isPackSel = (chargeClassStr[1].equalsIgnoreCase("true") ? true : false);
						classType = chargeClassStr[3];
						packCosID = chargeClassStr[4];
					}

				}

				if (m_overrideChargeClasses != null && classType != null
						&& m_overrideChargeClasses.contains(classType.toLowerCase()))
					nextClass = classType;
			}

			SubscriberDownloads downLoad = null;
			boolean isFreemiumUser = false ;
			String freemiumSubClass = RBTParametersUtils.getParamAsString(COMMON, "FREEMIUM_SUB_CLASSES", null);
			Subscriber subscriber = getSubscriber(subID(subscriberId));
			if (freemiumSubClass != null && !freemiumSubClass.isEmpty()) {
				List<String> freemiumSubClassList = Arrays.asList(freemiumSubClass.split(","));
				if (freemiumSubClassList.contains(subscriber.subscriptionClass())) {
					isFreemiumUser = true;
				}
			}
			
			if (!(categories != null && categories.type() == PLAYLIST_ODA_SHUFFLE) || categories == null) {
				downLoad = getSubscriberDownload(subID(subscriberId), subscriberWavFile, categoryID, categoryType);
			}

			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				}
			}
			String campaignCode = extraInfo != null ? extraInfo.remove(iRBTConstant.CAMPAIGN_CODE) : null;
			String treatmentCode = extraInfo != null ? extraInfo.remove(iRBTConstant.TREATMENT_CODE) : null;
			String offerCode = extraInfo != null ? extraInfo.remove(iRBTConstant.OFFER_CODE) : null;

			String downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);

			String refId = null;
			if (consentSubscriber != null) {
				refId = consentSubscriber.refID();
			}
			
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED || downStat == STATE_DOWNLOAD_CHANGE)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_DEACTIVATION_PENDING || downStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED)
					return "FAILURE:DOWNLOAD_DEACT_PENDING";
				else if (downStat == STATE_DOWNLOAD_ACT_ERROR || downStat == STATE_DOWNLOAD_DEACT_ERROR)
					return "FAILURE:DOWNLOAD_ERROR";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId), subscriberWavFile, categoryID, categoryType);
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(subscriberId, subscriberWavFile,
							endDate, isSubActive, selBy, selectionInfo, isSmClientModel, conn, categoryID, categoryType,
							nextClass, extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {
						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY", "SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());

					}

					downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

					if (null == subscriberDownloads) {
						if (null != extraInfo) {
							extraInfo.remove("CALLER_ID");
							extraInfo.remove("STATUS");
							extraInfo.remove("FROM_TIME");
							extraInfo.remove("TO_TIME");
							extraInfo.remove("INTERVAL");
							extraInfo.remove("LOOP_STATUS");
						}

						subscriberDownloads = SubscriberDownloadsImpl.insertRW(conn, subID(subscriberId), subscriberWavFile,
								categoryID, endDate, isSubActive, categoryType, nextClass, selBy, selectionInfo,
								downloadExtraInfo, isSmClientModel, downloadStatus, null);

						// RBT2.0 changes
						if (subscriberDownloads != null && subscriberDownloads.downloadStatus() == 'y') {

							TPTransactionLogger.getTPTransactionLoggerObject("download").writeTPTransLog(subscriber.circleID(),
									subID(subscriberId), "NA", -1, -1, -1, "NA", categoryType, -1, subscriberWavFile, categoryID,
									-1, nextClass, subscriberDownloads.startTime(), subscriberDownloads.endTime(), "NA");
						}
					}

					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID", subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT") && incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberId, packCosID);
					else if (selBy != null && (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift) && incrSelCount)
						SubscriberImpl.setSelectionCount(conn, subID(subscriberId));

					return "SUCCESS:DOWNLOAD_ADDED";
				} else if (downStat == STATE_DOWNLOAD_ACTIVATION_PENDING || downStat == STATE_DOWNLOAD_TO_BE_ACTIVATED
						|| downStat == STATE_DOWNLOAD_BASE_ACT_PENDING)
					return "SUCCESS:DOWNLOAD_PENDING_ACTIAVTION";
				else if (downStat == STATE_DOWNLOAD_SUSPENSION)
					return "FAILURE:DOWNLOAD_SUSPENDED";
				else if (downStat == STATE_DOWNLOAD_GRACE)
					return "SUCCESS:DOWNLOAD_GRACE";
				else if (downStat == STATE_DOWNLOAD_DEACTIVATED) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(subscriberId, subscriberWavFile,
							endDate, isSubActive, selBy, selectionInfo, isSmClientModel, conn, categoryID, categoryType,
							nextClass, extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {

						// Add following fields to extraInfo

						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY", "SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());
						extraInfo.put("EVENTYPE", "1");
						extraInfo.put("CONSENTCLASSTYPE", subscriberDownloads.classType());
					}

					downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);

					if (subscriberDownloads == null) {
						List<Clip> clipsAfterLimitCheck =  new ArrayList<Clip>() ; 
						if(!isFreemiumUser){
						 clipsAfterLimitCheck = clipstoAddAfterLimitCheck(subscriberId, categoryID, subscriberWavFile);
						}
						log.info("clipsAfterLimitCheck  :" + clipsAfterLimitCheck) ;
						if (isFreemiumUser || (clipsAfterLimitCheck != null && clipsAfterLimitCheck.size() > 0)) {
							subscriberDownloads = SubscriberDownloadsImpl.reactivateRW(conn, subID(subscriberId),
									subscriberWavFile, categoryID, endDate, categoryType, isSubActive, nextClass, selBy,
									selectionInfo, downloadExtraInfo, isSmClientModel, downloadStatus);
						} else {
							return WebServiceConstants.DOWNLOAD_MONTHLY_LIMIT_REACHED;
						}
						// RBT2.0 changes
						if (subscriberDownloads != null && subscriberDownloads.downloadStatus() == 'y') {

							TPTransactionLogger.getTPTransactionLoggerObject("download").writeTPTransLog(subscriber.circleID(),
									subID(subscriberId), "NA", -1, -1, -1, "NA", categoryType, -1, subscriberWavFile, categoryID,
									-1, nextClass, subscriberDownloads.startTime(), subscriberDownloads.endTime(), "NA");
						}

						if (subscriberDownloads != null && campaignCode != null && treatmentCode != null && offerCode != null) {
							String msg = iRBTConstant.CAMPAIGN_CODE + "=" + campaignCode + "," + iRBTConstant.TREATMENT_CODE
									+ "=" + treatmentCode + "," + iRBTConstant.OFFER_CODE + "=" + offerCode + ","
									+ iRBTConstant.RETRY_COUNT + "=0";
							RBTCallBackEvent.insert(subscriberId, subscriberDownloads.refID(), msg,
									RBTCallBackEvent.SM_CALLBACK_PENDING, RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
									clip.getClipId(), selBy);
						}

					}
					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID", subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT") && incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberId, packCosID);
					else if (selBy != null && (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift) && incrSelCount)
						SubscriberImpl.setSelectionCount(conn, subID(subscriberId));

					return "SUCCESS:DOWNLOAD_REACTIVATED";
				}
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null)
					return response;
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(downloadExtraInfo);

				SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(subscriberId, subscriberWavFile, endDate,
						isSubActive, selBy, selectionInfo, isSmClientModel, conn, categoryID, categoryType, nextClass, extraInfo,
						refId, useUIChargeClass);

				if (null != subscriberDownloads) {
					extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY", "SUCCESS");
					extraInfo.put("CONSENTID", subscriberDownloads.refID());
					extraInfo.put("EVENTYPE", "1");
					extraInfo.put("CONSENTCLASSTYPE", subscriberDownloads.classType());
				}

				downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
				List<Clip> clipsAfterLimitCheck = new ArrayList<Clip>() ;
				if(!isFreemiumUser){
				clipsAfterLimitCheck = clipstoAddAfterLimitCheck(subscriberId, categoryID, subscriberWavFile);
				}
				log.info("clipsAfterLimitCheck  :" + clipsAfterLimitCheck) ;

				if (subscriberDownloads == null) {
					if (categories != null && categories.type() == PLAYLIST_ODA_SHUFFLE) {
						if (isFreemiumUser || (clipsAfterLimitCheck != null && !clipsAfterLimitCheck.isEmpty())) {
							List<ProvisioningRequests> provisioningRequests = RBTDBManager.getInstance().getProvisioningRequests(
									subscriberId, categoryID);
							SubscriberDownloads[] downLoadEntry = getSubscriberDownloadsByDownloadStatusAndCategory(subscriberId,
									categoryID, categories.type(), "t");
							if (provisioningRequests != null && provisioningRequests.size() > 0 && downLoadEntry != null
									&& downLoadEntry.length > 0) {
								provRefId = provisioningRequests.get(0).getTransId();
								downloadStatus = "n";
							} else {
								downloadStatus = "w";
							}

							extraInfoMap.put("PROV_REF_ID", provRefId);
							downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
							log.info("going for ODA Shuffle Selections....");

							log.info("Shuffle clips ...=" + (clipsAfterLimitCheck));
							if (clipsAfterLimitCheck != null) {

								for (Clip clips : clipsAfterLimitCheck) {

									subscriberWavFile = clips.getClipRbtWavFile();

									classType = "FREE";
									subscriberDownloads = SubscriberDownloadsImpl.insertRW(conn, subID(subscriberId),
											subscriberWavFile, categoryID, endDate, isSubActive, categoryType, classType, selBy,
											selectionInfo, downloadExtraInfo, isSmClientModel, downloadStatus, null);

								}
							}
						} else {
							return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
						}
					} else if (isFreemiumUser || (clipsAfterLimitCheck != null && !clipsAfterLimitCheck.isEmpty())) {
						subscriberDownloads = SubscriberDownloadsImpl.insertRW(conn, subID(subscriberId), subscriberWavFile,
								categoryID, endDate, isSubActive, categoryType, nextClass, selBy, selectionInfo,
								downloadExtraInfo, isSmClientModel, downloadStatus, null);

					} else {
						return WebServiceConstants.DOWNLOAD_MONTHLY_LIMIT_REACHED;
					}

					// RBT2.0 changes
					if (subscriberDownloads != null && subscriberDownloads.downloadStatus() == 'y') {

						TPTransactionLogger.getTPTransactionLoggerObject("download").writeTPTransLog(subscriber.circleID(),
								subID(subscriberId), "NA", -1, -1, -1, "NA", categoryType, -1, subscriberWavFile, categoryID, -1,
								nextClass, subscriberDownloads.startTime(), subscriberDownloads.endTime(), "NA");
					}

					log.info("Campaign Code=" + campaignCode + ",TreatmentCode=" + treatmentCode + ",OfferCount=" + offerCode);
					if (subscriberDownloads != null && campaignCode != null && treatmentCode != null && offerCode != null) {
						String msg = iRBTConstant.CAMPAIGN_CODE + "=" + campaignCode + "," + iRBTConstant.TREATMENT_CODE + "="
								+ treatmentCode + "," + iRBTConstant.OFFER_CODE + "=" + offerCode + ","
								+ iRBTConstant.RETRY_COUNT + "=0";
						RBTCallBackEvent.insert(subscriberId, subscriberDownloads.refID(), msg,
								RBTCallBackEvent.SM_CALLBACK_PENDING, RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								clip.getClipId(), selBy);
					}
				}
				if (subscriberDownloads == null)
					return "FAILURE:INSERTION_FAILED";

				if (subscriberDownloads != null && responseParams != null) {
					responseParams.put("REF_ID", subscriberDownloads.refID());
					responseParams.put("CLASS_TYPE", nextClass);
				}

				if (selBy != null && !selBy.equalsIgnoreCase("GIFT") && incrSelCount && isPackSel)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberId, packCosID);
				else if (selBy != null && (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift) && incrSelCount)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberId));

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
	}

	public String addSubscriberDownloadRoW(String subscriberId, String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy, String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass, boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, int status, String callerId) {
		Connection conn = getConnection();

		if (conn == null)
			return null;

		try {
			ClipMinimal clip = getClipRBT(subscriberWavFile);
			int categoryID = categories.id();
			int categoryType = categories.type();
			if (endDate == null)
				endDate = m_endDate;
			subscriberId = subID(subscriberId);
			String nextClass = classType;

			SubscriberDownloads downLoad = getActiveSubscriberDownloadByStatus(subID(subscriberId), subscriberWavFile, "t",
					categoryID, categoryType);

			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				}
			}

			String downloadExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfo);
			String refId = null;
			if (responseParams != null) {
				refId = responseParams.get("SELECTION_REF_ID");
			}
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED || downStat == STATE_DOWNLOAD_CHANGE
						|| downStat == STATE_DOWNLOAD_SEL_TRACK)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId), subscriberWavFile, categoryID, categoryType);

					miPlaylistDBImpl.addDownloadRowforTracking(subscriberId, subscriberWavFile, categoryID, endDate, isSubActive,
							categoryType, nextClass, selBy, selectionInfo, downloadExtraInfo, isSmClientModel, refId);

					/*
					 * SubscriberDownloads subscriberDownloads =
					 * SubscriberDownloadsImpl.insertRW( conn,
					 * subID(subscriberId), subscriberWavFile, categoryID,
					 * endDate, isSubActive, categoryType, nextClass, selBy,
					 * selectionInfo, downloadExtraInfo, isSmClientModel,
					 * STATE_DOWNLOAD_SEL_TRACK+"", refId);
					 */

					return "SUCCESS:DOWNLOAD_ADDED";
				}
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null) {
					return response;
				}

				String addDownloadRowforTracking = miPlaylistDBImpl.addDownloadRowforTracking(subscriberId, subscriberWavFile,
						categoryID, endDate, isSubActive, categoryType, nextClass, selBy, selectionInfo, downloadExtraInfo,
						isSmClientModel, refId);

				/*
				 * SubscriberDownloads subscriberDownloads =
				 * SubscriberDownloadsImpl.insertRW( conn, subID(subscriberId),
				 * subscriberWavFile, categoryID, endDate, isSubActive,
				 * categoryType, nextClass, selBy, selectionInfo,
				 * downloadExtraInfo, isSmClientModel,
				 * STATE_DOWNLOAD_SEL_TRACK+"", refId);
				 */

				if (addDownloadRowforTracking.equalsIgnoreCase("false"))
					return "FAILURE:INSERTION_FAILED";

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
	}

	@Override
	public String addSubscriberDownloadRW(String subscriberId, String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy, String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass, boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, int status, String callerId, String downloadStatus) {

		// Normal song selection and special caller
		// Special song selection status != 1 and not shuffle
		String updateDownload = extraInfo == null ? "FALSE" : extraInfo.remove("UPDATE_DOWNLOAD");
		if (updateDownload != null
				&& updateDownload.equals("TRUE")
				|| (categories.type() != PLAYLIST_ODA_SHUFFLE && (((callerId == null || callerId.equalsIgnoreCase("all")) && status == 1) || Utility
						.isShuffleCategory(categories.type())))) {
			// add to download
			return addSubscriberDownloadRoW(subscriberId, subscriberWavFile, categories, endDate, isSubActive, classType, selBy,
					selectionInfo, extraInfo, incrSelCount, useUIChargeClass, isSmClientModel, responseParams, null, status,
					callerId);

		}
		return null;
	}

	public void deactivateOldODAPackOnSuccessCallback(String strSubID, String refID, String callerID, int categoryType,
			SubscriberStatus subStatus, boolean odaPackSelectionCallback, String extraInfo) {
		log.info("deactivateOldODAPackOnSuccessCallback = SubscriberID = " + strSubID + " ,RefId = " + refID + " , callerID = "
				+ callerID + ", extraInfo = " + extraInfo);
		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if (!odaPackSelectionCallback && categoryType == PLAYLIST_ODA_SHUFFLE) {
			String selectionStatus = subStatus.status() + "";
			if (callerID == null && (selectionStatus == null || selectionStatus.trim().equals("1"))) {
				SubscriberStatus[] activeNormalSel = getActiveNormalSelByCallerIdAndByStatus(strSubID, null, 1);
				if (activeNormalSel != null && activeNormalSel.length > 0) {
					for (int i = 0; i < activeNormalSel.length; i++) {
						deactivateSubscriberRecordsByRefId(strSubID, "SM", activeNormalSel[i].refID());
					}
				}
			}

		}

		int fromTime = 0;
		int toTime = 2359;
		int subSelstatus = 1;
		String selInterval = null;
		if (extraInfoMap != null) {
			if (extraInfoMap.containsKey("FROM_TIME")) {
				fromTime = Integer.parseInt(extraInfoMap.get("FROM_TIME"));
			}
			if (extraInfoMap.containsKey("TO_TIME")) {
				toTime = Integer.parseInt(extraInfoMap.get("TO_TIME"));
			}
			if (extraInfoMap.containsKey("STATUS")) {
				subSelstatus = Integer.parseInt(extraInfoMap.get("STATUS"));
			}
			if (extraInfoMap.containsKey("SEL_INTERVAL")) {
				selInterval = extraInfoMap.get("SEL_INTERVAL");
			}
		}

		if (extraInfo == null && subStatus != null) {
			fromTime = subStatus.fromTime();
			toTime = subStatus.toTime();
			subSelstatus = subStatus.status();
			selInterval = subStatus.selInterval();
		}
		ProvisioningRequests currPack = RBTDBManager.getInstance().getProvisioningRequestFromRefId(strSubID, refID);
		List<ProvisioningRequests> activeODAPackBySubscriberID = RBTDBManager.getInstance().getActiveODAPackBySubscriberID(
				strSubID);
		if (activeODAPackBySubscriberID != null && activeODAPackBySubscriberID.size() > 0) {
			for (ProvisioningRequests provReq : activeODAPackBySubscriberID) {
				String refid = provReq.getTransId();
				if (!refid.equalsIgnoreCase(refID)
						&& (currPack == null || (currPack != null && provReq.getCreationTime().before(currPack.getCreationTime())))) {
					if (subStatus != null) {
						String xtraInfo = subStatus.extraInfo();
						HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
						if (xtraInfoMap != null && xtraInfoMap.containsKey("PROV_REF_ID")) {
							String provRefId = xtraInfoMap.get("PROV_REF_ID");
							if (provRefId.equalsIgnoreCase(refid))
								continue;
						}
					}
					String packExtraInfo = provReq.getExtraInfo();
					HashMap<String, String> packExtraInfoMap = DBUtility.getAttributeMapFromXML(packExtraInfo);
					int status = provReq.getStatus();
					log.info("ODA Pack status = " + status);
					String packCallerId = null;
					if (packExtraInfoMap != null) {
						packCallerId = packExtraInfoMap.get("CALLER_ID");
					}

					int packFromTime = 0;
					int packToTime = 2359;
					int packSelStatus = 1;
					String packSelInterval = null;

					if (packExtraInfoMap != null) {
						if (packExtraInfoMap.containsKey("FROM_TIME")) {
							packFromTime = Integer.parseInt(packExtraInfoMap.get("FROM_TIME"));
						}
						if (packExtraInfoMap.containsKey("TO_TIME")) {
							packToTime = Integer.parseInt(packExtraInfoMap.get("TO_TIME"));
						}
						if (packExtraInfoMap.containsKey("STATUS")) {
							packSelStatus = Integer.parseInt(packExtraInfoMap.get("STATUS"));
						}
						if (packExtraInfoMap.containsKey("SEL_INTERVAL")) {
							packSelInterval = packExtraInfoMap.get("SEL_INTERVAL");
						}
					}

					if (!(fromTime == packFromTime && toTime == packToTime && subSelstatus == packSelStatus && ((selInterval == null && packSelInterval == null) || (selInterval != null
							&& packSelInterval != null && selInterval.equalsIgnoreCase(packSelInterval))))) {
						continue;
					}

					if (packExtraInfoMap == null) {
						packExtraInfoMap = new HashMap<String, String>();
					}
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_MODE, "SM");
					packExtraInfoMap.put(iRBTConstant.EXTRA_INFO_PACK_DEACTIVATION_TIME, new Date().toString());
					packExtraInfo = DBUtility.getAttributeXMLFromMap(packExtraInfoMap);
					if ((packCallerId == null && callerID == null)
							|| (packCallerId != null && packCallerId.equalsIgnoreCase(callerID))) {
						if (odaPackSelectionCallback
								&& (status == 32 || status == 33 || status == 34 || status == 35 || status == 50)) {
							RBTDBManager.getInstance().smPackUpdationSuccess(strSubID, refid, "SM", PACK_DEACTIVATED + "", false,
									-1);

						} else if (status == 30 || status == 31) {
							RBTDBManager.getInstance()
									.directDeactivateActiveODAPack(strSubID, packCallerId, refid, packExtraInfo);
						} else {
							RBTDBManager.getInstance().smPackUpdationSuccess(strSubID, refid, "SM", PACK_DEACTIVATED + "", false,
									-1);

						}

						Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
						int categoryId = provReq.getType();
						Category category = getCategory(categoryId);
						// com.onmobile.apps.ringbacktones.rbtcontents.beans.Category
						// category =
						// RBTCacheManager.getInstance().getCategory(provReq.getType());
						int categoryType1 = -1;
						if (category != null) {
							categoryType1 = category.getType();
						}
						if (category == null || categoryType1 != PLAYLIST_ODA_SHUFFLE) {
							return;
						}
						deactivateSelectionsUnderODAPack(subscriber, category, packCallerId, provReq.getTransId());

					}
				}
			}
		}

	}

	private void deactivateSelectionsUnderODAPack(Subscriber subscriber, Category category, String callerID, String refID) {
		log.info("Going to deactivate Selections under ODA Pack for Subscriber = " + subscriber);
		SubscriberStatus[] settings = getActiveSelectionBasedOnCallerId(subscriber.subID(), callerID);
		if (settings != null && settings.length > 0) {
			for (SubscriberStatus setting : settings) {
				String extraInfo = setting.extraInfo();
				HashMap<String, String> xtraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				String provRefId = null;
				if (xtraInfoMap != null && xtraInfoMap.containsKey("PROV_REF_ID")) {
					provRefId = xtraInfoMap.get("PROV_REF_ID");
				}
				if (category.getType() == PLAYLIST_ODA_SHUFFLE && category.getID() == setting.categoryID() && provRefId != null
						&& provRefId.equalsIgnoreCase(refID)) {
					expireSubscriberSelection(subscriber.subID(), setting.refID(), "DAEMON");
				}
			}
		}
	}

	
	/*
	 * TO check whether user has crossed the monthly download limited or NOT
	 * Returning the list of clips to be added after limit check for both single
	 * song or playlist scenario and simultaneously updating subscriber extra
	 * Info keeping a cunt on subscriber's monthly download
	 */
	/*Case 1 : Subscriber Going for single song 
	a)Subscriber count in extra Info is less than monthly limit configured in DB it will return the selected clip in clip list
		and update subscriber extraInfo with count and month 
		
	b)Other case we will return null
	 */
	
	/*Case 2 : Subscriber select a playlist 
	a)Subscriber selecting the playlist for the first time : Then we willl check the total number of songs in playlist and 
	 will compare it with limit and accordingly will return either null or all the clips in playlist 
			 
	b)Subscriber selecting already selecting playlist : We will identify the number of new songs added to playlist  
	and will add those based on subscriber's count and monthly limit  
	 */	
	@Override
	public List<Clip> clipstoAddAfterLimitCheck(String subscriberId, int categoryID, String subscriberWavFile) {
		log.info("Getting clips to be added  for Subscriber = " + subscriberId);
		com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
				.getInstance().getCategory(categoryID);

		int categoryType = category.getCategoryTpe();
		String subscriberMonth = null;
		String subscriberCount = null;
		int Limitcount = 1;
		Clip selectedClip = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager.getInstance()
				.getClipByRbtWavFileName(subscriberWavFile);
		Calendar cal = Calendar.getInstance();

		int currentYEAR = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		Subscriber subscriber = getSubscriber(subID(subscriberId));
		String subscriberExtraInfo = subscriber.extraInfo();
		HashMap<String, String> subExtraInfoMap = new HashMap<String, String>();
		if (subscriberExtraInfo != null && !subscriberExtraInfo.isEmpty()) {
			subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriberExtraInfo);
			subscriberMonth = subExtraInfoMap.get("MONTH");
			subscriberCount = subExtraInfoMap.get("COUNT");
		}
		log.info("subscriberMonth " + subscriberMonth + " and subscriberCount" + subscriberCount + "  for Subscriber = "
				+ subscriberId);
		int downloadLimit = RBTParametersUtils.getParamAsInt(WEBSERVICE, "USER_DOWNLOAD_LIMIT", 0);
		log.info("USER_DOWNLOAD_LIMIT :" + downloadLimit);
		List<Clip> clipsTobeAdded = new ArrayList<Clip>();
		ArrayList<String> downloadRefIdsTobeDactivated = new ArrayList<String>();
		SubscriberDownloads[] subscriberDownloads = getSubscriberActiveDownloadsByDownloadStatusAndCategory(subscriberId,
				categoryID, categoryType);
		if (com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(categoryType)) {

			List<Clip> activeClipsInCategory = Arrays.asList(RBTCacheManager.getInstance().getActiveClipsInCategory(categoryID));

			if (activeClipsInCategory != null && activeClipsInCategory.size() > 0) {
				HashMap<String, Clip> wavFileClipMap = new HashMap<String, Clip>();
				for (Clip clip : activeClipsInCategory) {
					wavFileClipMap.put(clip.getClipRbtWavFile(), clip);
				}

				if (subscriberDownloads != null && subscriberDownloads.length > 0) {
					for (int i = 0; i < subscriberDownloads.length; i++) {
						if (wavFileClipMap != null && wavFileClipMap.containsKey(subscriberDownloads[i].promoId())) {
							wavFileClipMap.remove(subscriberDownloads[i].promoId());
						} else {
							downloadRefIdsTobeDactivated.add(subscriberDownloads[i].refID());
						}
					}
					for (String key : wavFileClipMap.keySet()) {

						clipsTobeAdded.add(wavFileClipMap.get(key));

					}
				} else {
					clipsTobeAdded = activeClipsInCategory;
				}

			}
		} else {
			clipsTobeAdded.add(selectedClip);

		}

		int subscriberCurrentCount = 0;
		if (downloadLimit > 0) {
			if (subscriberMonth != null && subscriberCount != null
					&& subscriberMonth.equalsIgnoreCase(currentMonth + "-" + currentYEAR)) {
				subscriberCurrentCount = Integer.parseInt(subscriberCount);
				if ((subscriberCurrentCount + clipsTobeAdded.size()) <= downloadLimit) {
					subExtraInfoMap.put("COUNT", subscriberCurrentCount + clipsTobeAdded.size() + "");
					RBTDBManager.getInstance().updateExtraInfo(subscriberId, DBUtility.getAttributeXMLFromMap(subExtraInfoMap));
					if (downloadRefIdsTobeDactivated != null) {
						for (String rfId : downloadRefIdsTobeDactivated) {
							RBTDBManager.getInstance().smDownloadDeActivation(subscriberId, rfId,
									STATE_DOWNLOAD_TO_BE_DEACTIVATED);
						}
					}
					return clipsTobeAdded;
				}

				return null;
			} else {

				if (clipsTobeAdded.size() <= downloadLimit) {
					subscriberMonth = currentMonth + "-" + currentYEAR;
					subExtraInfoMap.put("MONTH", subscriberMonth);
					subExtraInfoMap.put("COUNT", clipsTobeAdded.size() + "");
					RBTDBManager.getInstance().updateExtraInfo(subscriberId, DBUtility.getAttributeXMLFromMap(subExtraInfoMap));
					return clipsTobeAdded;

				}
				return null;
			}
		}
		return clipsTobeAdded;

	}

	public boolean sendAcknowledgementSMS(String subscriberID, boolean isPlayList, int catId) {

		com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
				.getInstance().getCategory(catId);
		String type = "WEBSERVICE";
		String subType = "REACTIVATION_SUCCESS";
		if (isPlayList) {
			subType = "PLAYLIST" + "_" + subType;
		} else {
			subType = "MiPLAYLIST" + "_" + subType;
		}
		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, subType);
		log.debug(subType + ":subType " + type + "SMS text not configured for type: " + rbtText + ", subType: " + subType);

		if (rbtText == null) {
			if (log.isDebugEnabled())
				log.debug("SMS text not configured for type: " + type + ", subType: " + subType);
			return false;
		}

		String text = rbtText.getText();
		if (text == null) {
			if (log.isInfoEnabled())
				log.info("SMS text is configured as null for type: " + type + ", subType: " + subType);
			return false;
		}
		if (category != null) {
			text = text.replace("%CATEGORY_NAME%", category.getCategoryName());
		}
		String senderID = RBTParametersUtils.getParamAsString(iRBTConstant.WEBSERVICE, "ACK_SMS_SENDER_NO", null);
		log.debug(senderID + "SMS text not configured for type: " + rbtText + ", subType: " + subType);
		if (senderID == null) {
			log.info("SENDER_NO is not configured, so not sending the SMS");
			return false;
		}

		boolean sendSMSResponse = false;
		try {
			sendSMSResponse = Tools.sendSMS(senderID, subscriberID, text, false);
		} catch (OnMobileException e) {
			log.error(e.getMessage(), e);
		}

		if (sendSMSResponse)
			return true;
		else
			return false;
	}

	@Override
	public boolean isDownloadActivated(SubscriberDownloads subscriberDownloads) {
		if (subscriberDownloads != null
				&& (subscriberDownloads.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_ACTIVATED || subscriberDownloads
						.downloadStatus() == iRBTConstant.STATE_DOWNLOAD_CHANGE))
			return true;
		return false;
	}

	@Override
	public boolean updateDownloadStatusByDownloadStatus(String subscriberID, String promoID, String deselectedBy,
			String callerId, int status, String downloadStatus, String oldDownloadStatus, int catID, int catType) {

		boolean updated = false;
		;
		try {
			if (deselectedBy != null && !deselectedBy.equalsIgnoreCase("SM")) {
				updated = miPlaylistDBImpl.removeMiPlaylistTrackFromDownload(subscriberID, promoID, catID, catType, callerId,
						status);
				/*
				 * SubscriberDownloadsImpl.updateDownloadStatusByDownloadStatus(conn
				 * , subID(subscriberID), promoID, deselectedBy, downloadStatus,
				 * oldDownloadStatus,catType);
				 */
			}
		} catch (Throwable e) {
			log.error("Exception while removing downloads:", e);
		}
		return updated;

	}

	public String smUpdateDownloadDeactivationCallback(String subscriberID, String wavFile, String refID, String status,
			boolean m_noDownloadDeactSub, String type) {
		Connection conn = getConnection();
		if (conn == null)
			return m_connectionError;
		boolean success = false;
		boolean selDeactStatus = false;
		String downloadStatusStr = "FAILURE";
		String selStatusStr = "FAILURE";
		char downStat = STATE_DOWNLOAD_ACTIVATED;
		SubscriberDownloads subDownloadObj = null;
		String newChargeClass = null;
		boolean freeChargeIndc = false;
		try {
			if (status.equals("SUCCESS")) {
				// JIRA-ID - RBT-7933 : VFQ related changes
				Parameters parameterObj = CacheManagerUtil.getParametersCacheManager().getParameter("WEBSERVICE",
						"UPGRADE_CHARGE_CLASS_FOR_NEXT_SELECTION", "FALSE");
				subDownloadObj = SubscriberDownloadsImpl.getSubscriberDownloadByRefID(conn, subscriberID, refID);
				if (parameterObj.getValue().equalsIgnoreCase("TRUE")) {
					Subscriber subscriberObj = SubscriberImpl.getSubscriber(conn, subscriberID);

					CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriberObj.cosID());
					if (cosDetails != null && cosDetails.getFreechargeClass() != null) {
						if (cosDetails.getFreechargeClass().contains(",")) {
							newChargeClass = cosDetails.getFreechargeClass().split(",")[0];
						} else {
							newChargeClass = cosDetails.getFreechargeClass();
						}
					}
					if (subDownloadObj.classType().equalsIgnoreCase(newChargeClass)) {
						freeChargeIndc = true;
					}
				}
				downStat = STATE_DOWNLOAD_DEACTIVATED;
				success = SubscriberDownloadsImpl.smDownloadDeActivation(conn, subscriberID, refID, downStat);
				if (success && subDownloadObj != null) {
					// RBT-14044 VF ES - MI Playlist functionality for RBT core
				 	deleteDownloadwithTstatus(subscriberID, wavFile, subDownloadObj.categoryID() + "");
					selDeactStatus = SubscriberStatusImpl.deactivateSettingDownloadDeactBasedOnCategory(conn, subscriberID,
							wavFile, subDownloadObj.categoryID() + "", subDownloadObj.categoryType() + "");
				}

				if (success && m_noDownloadDeactSub) {
					SubscriberDownloads[] subDownloads = SubscriberDownloadsImpl.getSubscriberAllActiveDownloads(conn,
							subscriberID);
					if (subDownloads == null) {
						noDownloadDeactSub(conn, m_noDownloadDeactSub, subscriberID, type, subDownloads, false);
						// JIRA-ID - RBT-7933 : VFQ related changes
					} else if ((parameterObj.getValue().equalsIgnoreCase("TRUE")) && freeChargeIndc) {
						if (subDownloads != null) {
							if (subDownloads.length > 1) {
								Arrays.sort(subDownloads, new DownloadSetTimeComparator());
							}
							HashMap<String, String> oldExtraInfoMap = DBUtility.getAttributeMapFromXML(subDownloads[0]
									.extraInfo());
							if (oldExtraInfoMap == null) {
								oldExtraInfoMap = new HashMap<String, String>();
							}

							oldExtraInfoMap.put("OLD_CLASS_TYPE", subDownloads[0].classType());
							String oldSelExtraInfo = DBUtility.getAttributeXMLFromMap(oldExtraInfoMap);
							if (newChargeClass != null) {
								updateDownloads(subscriberID, subDownloads[0].refID(), 'c', oldSelExtraInfo, newChargeClass);
							}
						}
					}
				}
			} else
				success = SubscriberDownloadsImpl.smDownloadDeActivation(conn, subscriberID, refID,
						STATE_DOWNLOAD_TO_BE_DEACTIVATED);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		if (success) {
			downloadStatusStr = "SUCCESS";
			if (selDeactStatus)
				selStatusStr = "sel" + "SUCCESS";
			return downloadStatusStr + "_" + selStatusStr;
		} else
			return downloadStatusStr + "_" + selStatusStr;
	}
}
