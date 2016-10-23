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
import java.util.HashSet;
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
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.RBTBulkUploadTask;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class TelefonicaSelectionDBMgrImpl extends RBTDBManager{

	private static Logger logger = Logger.getLogger(TelefonicaSelectionDBMgrImpl.class);
	
	public boolean expireSubscriberDownloadAndUpdateExtraInfo(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo,String extraInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.removeSubscriberDownload(conn,
					subID(subscriberId), wavFile, categoryId, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	public boolean expireSubscriberDownload(String subscriberId,
			String wavFile, int categoryId, int categoryType,
			String deactivateBy, String deselectionInfo, boolean isDirectDeactivation) {
		Connection conn = getConnection();
		if (conn == null)
			return false;

		try {
			return SubscriberDownloadsImpl.removeSubscriberDownload(conn,
					subID(subscriberId), wavFile, categoryId, categoryType);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}
	
	
	public void deactivateActiveSubscriberDownloads(String subID) {
		Connection conn = getConnection();
		if (conn == null)
			return;
		try {
			SubscriberDownloads[] downloads = SubscriberDownloadsImpl.getActiveSubscriberDownloads(conn, subID(subID));
			if(downloads == null || downloads.length == 0) {
				return;
			}
			for(SubscriberDownloads download : downloads) {
				String extraInfo = download.extraInfo();
				Map<String, String> infoMap = null;
				if(extraInfo != null) {
					infoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				}
				else {
					infoMap = new HashMap<String, String>();
				}
				infoMap.put("BASE_DCT_DATE", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
				extraInfo = DBUtility.getAttributeXMLFromMap(infoMap);
				SubscriberDownloadsImpl.updateDownloadStatusExtrainfoNChargeclass(conn, subID, download.refID(), STATE_DOWNLOAD_SEL_TRACK, extraInfo, null);
			}
		}catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		
	}
	
	public void removeDeactivateSubscriberDownloads(String subID) {
		Connection conn = getConnection();
		int dctDwnReActNoOfDays = RBTParametersUtils.getParamAsInt(COMMON,"DCT_DOWNLOAD_REACT_NO_OF_DAYS", 60);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -(dctDwnReActNoOfDays));
		if (conn == null)
			return;
		try {
			SubscriberDownloads[] downloads = SubscriberDownloadsImpl.getActiveSubscriberDownloads(conn, subID(subID));
			if(downloads == null || downloads.length == 0) {
				return;
			}
			for(SubscriberDownloads download : downloads) {
				String extraInfo = download.extraInfo();
				Map<String, String> infoMap = null;
				if(extraInfo != null) {
					infoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				}
				
				if(infoMap == null || !infoMap.containsKey("BASE_DCT_DATE")) {
					continue;
				}

				Date lastDctDate = new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(infoMap.remove("BASE_DCT_DATE"));
				
				if(calendar.getTime().before(lastDctDate)) {					
					extraInfo = DBUtility.getAttributeXMLFromMap(infoMap);
					SubscriberDownloadsImpl.updateDownloadStatusExtrainfoNChargeclass(conn, subID, download.refID(), STATE_DOWNLOAD_SEL_TRACK, extraInfo, null);
				}
				else {
					SubscriberDownloadsImpl.removeSubscriberDownload(conn,
							subID, download.promoId(), download.categoryID(), download.categoryType());
				}
			}
		}catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		
	}
	
	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams, Subscriber consentSubscriber, int status, String callerId) {
		Connection conn = getConnection();
		
		if (conn == null)
			return null;
		
		
		String updateDownload = extraInfo==null ? "FALSE" : extraInfo.remove("UPDATE_DOWNLOAD");
		if (updateDownload != null && !updateDownload.equals("TRUE") && categories.type() == PLAYLIST_ODA_SHUFFLE ) {
			return null;
		}

		try {
			ClipMinimal clip = getClipRBT(subscriberWavFile);
			int categoryID = categories.id();
			int categoryType = categories.type();
			if (endDate == null)
				endDate = m_endDate;
			subscriberId = subID(subscriberId);
			String nextClass = classType;


			
			 SubscriberDownloads downLoad = getSubscriberDownload(     
					subID(subscriberId), subscriberWavFile, categoryID,
					categoryType);
			
			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad
							.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				}
			}

			String downloadExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfo);
			
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED
						|| downStat == STATE_DOWNLOAD_CHANGE || downStat == STATE_DOWNLOAD_SEL_TRACK)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId),
							subscriberWavFile, categoryID, categoryType);
					
					SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl.insertRW(
							conn, subID(subscriberId), subscriberWavFile,
							categoryID, endDate, isSubActive, categoryType,
							nextClass, selBy, selectionInfo,
							downloadExtraInfo, isSmClientModel, STATE_DOWNLOAD_SEL_TRACK+"", null);
					
					return "SUCCESS:DOWNLOAD_ADDED";
				} 
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null) {
					return response;
				}

				
				SubscriberDownloads subscriberDownloads = SubscriberDownloadsImpl.insertRW(
						conn, subID(subscriberId), subscriberWavFile,
						categoryID, endDate, isSubActive, categoryType,
						nextClass, selBy, selectionInfo, downloadExtraInfo,
						isSmClientModel, STATE_DOWNLOAD_SEL_TRACK+"", null);
				
				if (subscriberDownloads == null)
					return "FAILURE:INSERTION_FAILED";

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";
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
	
	public ViralSMSTable insertViralSMSTableMap(String subscriberID,
			Date sentTime, String type, String callerID, String clipID,
			int count, String selectedBy, Date setTime, HashMap extraInfoMap) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		
		boolean isSupportRecomGiftFlow = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON", "ENABLE_GIFT_RECOMMENDATION","FALSE").getValue().equalsIgnoreCase("TRUE");
		
		if(isSupportRecomGiftFlow && ("GIFT".equalsIgnoreCase(type) || "INIT_GIFT".equalsIgnoreCase(type) || "PRE_GIFT".equalsIgnoreCase(type))) {
			type = "GIFT_CHARGED";
		}

		try {
			String finalExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfoMap);
			
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("VIRAL", "VIRAL_SMS_TYPE_FOR_SMS_FLOW",null);
			if(viralSmsTypeListForNewTable != null) {
				
				if(type != null && viralSmsTypeListForNewTable.contains(type)) {
					return ViralSMSNewImpl.insert(conn, subID(subscriberID),
							sentTime, type, subID(callerID), clipID, count, selectedBy,
							setTime, finalExtraInfo);
				}
			}
			
			return ViralSMSTableImpl.insert(conn, subID(subscriberID),
					sentTime, type, subID(callerID), clipID, count, selectedBy,
					setTime, finalExtraInfo);
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	@Override
	public String addSubscriberSelections(String subscriberID, String callerID,
			Categories categories, HashMap clipMap, Date setTime, Date startTime, Date endTime,
			int status, String selectedBy, String selectionInfo, int freePeriod, boolean isPrepaid,
			boolean changeSubType, String messagePath, int fromTime, int toTime,
			String chargeClassType, boolean smActivation, boolean doTODCheck, String mode,
			String regexType, String subYes, String promoType, String circleID,
			boolean incrSelCount, boolean useDate, String transID, boolean OptIn, boolean isTata,
			boolean inLoop, String subClass, Subscriber sub, int rbtType, String selInterval,
			HashMap extraInfo, boolean useUIChargeClass, String refID, boolean isDirectActivation) {

		logger.info("Adding selection. subscriberID: " + subscriberID + ", selectedBy: "
				+ selectedBy + ", circleID: " + circleID + ", refID: " + refID);

		Connection conn = getConnection();
		if (conn == null)
			return null;
		int count = 0;
		Date nextChargingDate = null;
		Date startDate = startTime;
		String selectInfo = selectionInfo;
		String sel_status = STATE_BASE_ACTIVATION_PENDING;
		int nextPlus = -1;
		HashMap subscriberExtraInfo = new HashMap();
		boolean updateEndDate = false;
		boolean isVoluntarySuspendedSub = false;
		try {
			subscriberID = subID(subscriberID);
			callerID = subID(callerID);
			if (subscriberID != null && callerID != null && subscriberID.equals(callerID))
				return SELECTION_FAILED_OWN_NUMBER;

			if (categories != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(categories.type())) {
				if (categories.endTime().before(new Date()))
					return SELECTION_FAILED_CATEGORY_EXPIRED;
			}

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
				subscriberExtraInfo = getExtraInfoMap(sub);
				if (subscriberExtraInfo != null && subscriberExtraInfo.containsKey(VOLUNTARY)) {
					isVoluntarySuspendedSub = ("" + subscriberExtraInfo.get(VOLUNTARY))
							.equalsIgnoreCase("true");
				}
				if (!isVoluntarySuspendedSub) {
					logger.info(subscriberID + " is suspended. Returning false.");
					return SELECTION_FAILED_SUBSCRIBER_SUSPENDED;
				} else {
					sel_status = STATE_TO_BE_ACTIVATED;
				}
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
				if (!isPackActivationPendingForContent(sub, categories, subscriberWavFile, status,
						callerID))
					sel_status = STATE_TO_BE_ACTIVATED;
			}

			if (subClass != null && m_subOnlyChargeClass != null
					&& m_subOnlyChargeClass.containsKey(subClass)) {
				chargeClassType = (String) m_subOnlyChargeClass.get(subClass);
				updateEndDate = true;
			}
			if (clipEndTime != null) {
				if (clipEndTime.getTime() < System.currentTimeMillis()) {
					return SELECTION_FAILED_CLIP_EXPIRED;
				}
				if (categories != null
						&& (categories.type() == DAILY_SHUFFLE || categories.type() == MONTHLY_SHUFFLE)) {
					endDate = categories.endTime();
					status = 79;
				}

				if (!useUIChargeClass && clipClassType != null
						&& !clipClassType.equalsIgnoreCase("DEFAULT") && classType != null
						&& !clipClassType.equalsIgnoreCase(classType)) {
					ChargeClass catCharge = CacheManagerUtil.getChargeClassCacheManager()
							.getChargeClass(classType);
					ChargeClass clipCharge = CacheManagerUtil.getChargeClassCacheManager()
							.getChargeClass(clipClassType);

					if (catCharge != null && clipCharge != null && catCharge.getAmount() != null
							&& clipCharge.getAmount() != null) {
						try {
							int firstAmount = Integer.parseInt(catCharge.getAmount());
							int secondAmount = Integer.parseInt(clipCharge.getAmount());

							if ((firstAmount < secondAmount)
									|| (m_overrideChargeClasses != null && m_overrideChargeClasses
											.contains(clipClassType.toLowerCase())))
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
				ChargeClass first = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(
						classType);
				ChargeClass second = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(
						chargeClassType);

				if (first != null && second != null && first.getAmount() != null
						&& second.getAmount() != null) {
					try {
						int firstAmount = Integer.parseInt(first.getAmount());
						int secondAmount = Integer.parseInt(second.getAmount());

						if (firstAmount <= secondAmount
								|| secondAmount == 0
								|| chargeClassType.equalsIgnoreCase("YOUTHCARD")
								|| chargeClassType.equalsIgnoreCase("DEFAULT_10")
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

			if (!useUIChargeClass && categories != null && categories.type() == 10
					&& m_modeChargeClass != null && m_modeChargeClass.containsKey(selectedBy)) {
				classType = (String) m_modeChargeClass.get(selectedBy);
			}

			if (selectedBy != null && !selectedBy.equalsIgnoreCase("VPO")) {
				ViralSMSTable viralSMS = ViralSMSTableImpl.getViralPromotion(conn,
						subID(subscriberID), null);
				if (viralSMS != null) {
					selectInfo = selectInfo + ":" + "viral";
				}
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
			/**
			 * Since Sprint 4 RBT 2.0, RBT 15670
			 * One more parameter udpId has been added in
			 * getSubscriberSelections method. If udpId is present then 
			 * query will filter it with udpId also otherwise old flow.
			 */
			String udpId = null;
			/*if(extraInfo.containsKey(WebServiceConstants.param_udpId))
			udpId = (String) extraInfo.get(UDP_ID);*/
			SubscriberStatus[] subscriberSelections = SubscriberStatusImpl.getSubscriberSelections(
					conn, subID(subscriberID), subID(callerID), rbtType, udpId);

			if (!inLoop && status == 1) // If user opted for UDS
			{
				HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub
						.extraInfo());
				if (subExtraInfoMap != null && subExtraInfoMap.containsKey(UDS_OPTIN))
					inLoop = ((String) subExtraInfoMap.get(UDS_OPTIN)).equalsIgnoreCase("TRUE");
				if (inLoop) {
					if (isShufflePresentSelection(subID(subscriberID), callerID, 0))
						inLoop = false;
					else if (categories.type() == 0 || categories.type() == 10
							|| categories.type() == 11 || categories.type() == 12
							|| categories.type() == 20)
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

			/* time of the day changes */
			SubscriberStatus subscriberStatus = null;
			
			if (fromTime == 0 && toTime == 2359 && status == 80)
				status = 1;

			subscriberStatus = getAvailableSelection(conn, subID(subscriberID), subID(callerID),
					subscriberSelections, categories, subscriberWavFile, status, fromTime, toTime,
					startDate, endDate, doTODCheck, inLoop, rbtType, selInterval, selectedBy);

			if (subscriberStatus == null) {
				logger.info("RBT::no matches found");
				if (inLoop
						&& (categories.type() == SHUFFLE || status == 90 || status == 99 || status == 0))
					inLoop = false;
				if (fromTime == 0 && toTime == 2359 && status == 80)
					status = 1;

				subscriberStatus = SubscriberStatusImpl.smSubscriberSelections(conn,
						subID(subscriberID), subID(callerID), status, rbtType);
				if (subscriberStatus != null) {
					if (inLoop && subscriberStatus.categoryType() == SHUFFLE)
						inLoop = false;
				}
				/**
				 * @added by sreekar if user's last selection is a trail
				 *        selection his next selection should override the old
				 *        one
				 */
				char loopStatus = getLoopStatusForNewSelection(inLoop, subscriberID, isPrepaid);

				String actBy = null;
				if (sub != null) {
					actBy = sub.activatedBy();
					// oldSubClass = sub.oldClassType();
				}
				if (m_trialChangeSubTypeOnSelection && actBy != null && actBy.equals("TNB")
						&& (subClass != null && subClass.equals("ZERO"))) {
					if (classType != null && classType.equals("FREE")) {
						sel_status = STATE_BASE_ACTIVATION_PENDING;

						if (!convertSubscriptionTypeTrial(subID(subscriberID), subClass, "DEFAULT",
								sub))
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
					HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(sub
							.extraInfo());
					if (subExtraInfoMap != null && subExtraInfoMap.containsKey(EXTRA_INFO_PACK))
						subPacks = subExtraInfoMap.get(EXTRA_INFO_PACK);

					String nextClass = null;
					if (subPacks != null) {
						com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getCategory(categories.id());
						Clip clipObj = com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager
								.getInstance().getClipByRbtWavFileName(subscriberWavFile);
						CosDetails cosDetail = getCosDetailsForContent(subscriberID, subPacks,
								category, clipObj, status, callerID);
						List<ProvisioningRequests> packList = null;
						if (cosDetail != null) {
							packList = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(subscriberID,
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

					if (nextClass == null)
						return SELECTION_FAILED_INTERNAL_ERROR;
					if (!nextClass.equalsIgnoreCase("DEFAULT"))
						classType = nextClass;
				}

				if (!useUIChargeClass && m_overrideChargeClasses != null && chargeClassType != null
						&& m_overrideChargeClasses.contains(chargeClassType.toLowerCase()))
					classType = chargeClassType;

				if (!useUIChargeClass) {
					if (status == 80 && rbtType == 2) {
						classType = clipClassType;
					} else {
						for (int i = 0; subscriberSelections != null
								&& i < subscriberSelections.length; i++) {
							if (subscriberSelections[i].selType() == 2) {
								HashMap selectionExtraInfo = DBUtility
										.getAttributeMapFromXML(subscriberSelections[i].extraInfo());
								int campaignId = -1;

								if (selectionExtraInfo != null
										&& selectionExtraInfo.containsKey(iRBTConstant.CAMPAIGN_ID)
										&& selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID) != null) {

									try {
										campaignId = Integer.parseInt(""
												+ selectionExtraInfo.get(iRBTConstant.CAMPAIGN_ID));
									} catch (Exception e) {
										campaignId = -1;
									}
								}
								logger.info("The value of campaign id - " + campaignId);
								if (campaignId != -1) {
									RBTBulkUploadTask bulkUploadTask = RBTBulkUploadTaskDAO
											.getRBTBulkUploadTask(campaignId);

									if (m_corporateDiscountChargeClass != null
											&& null != bulkUploadTask
											&& bulkUploadTask.getTaskMode() != null
											&& m_corporateDiscountChargeClass
													.containsKey(bulkUploadTask.getTaskMode())) {
										logger.info("The value of m_corporateDiscountChargeClass id - "
												+ m_corporateDiscountChargeClass.toString());
										HashMap discountClassMap = (HashMap) m_corporateDiscountChargeClass
												.get(bulkUploadTask.getTaskMode());
										if (discountClassMap != null && classType != null
												&& discountClassMap.containsKey(classType))
											classType = (String) discountClassMap.get(classType);
									}
								}
								break;
							}

						}
					}
				} 
				boolean isFreemiumUpgradeRequired = false;
				if(m_FreemiumUpgradeChargeClass.contains(classType) && freemiumSubClassList.contains(sub.subscriptionClass())){
//					String confFreeNoSelForChrgClass = m_FreemiumChrgClassesNumMaxMap.get(classType);
//					int currentNoOfSel = 0;
//					if (sub != null) {
//						currentNoOfSel = sub.maxSelections();
//					}
//					if(confFreeNoSelForChrgClass.equals(currentNoOfSel+"")){
					  sel_status = STATE_BASE_ACTIVATION_PENDING;   
					  isFreemiumUpgradeRequired = true;
					  clipMap.put("UPGRADE_REQUIRED", true);
//					}
				} 
				if(sub!=null && freemiumSubClassList.contains(sub.subscriptionClass())){
					clipMap.put("FREEMIUM_USER", true);
				}
				
				String checkSelStatus = checkSelectionLimit(subscriberSelections, subID(callerID),
						inLoop);
				if (!checkSelStatus.equalsIgnoreCase("SUCCESS"))
					return checkSelStatus;

				// Added the grace selection deact mode for JIRA-RBT-6338
				String graceDeselectedBy = selectedBy;
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(
						"COMMON", "SYSTEM_GRACE_SELECTION_DEACT_MODE", null);
				if (parameter != null && parameter.getValue() != null)
					graceDeselectedBy = parameter.getValue();

				SubscriberStatusImpl.deactivateSubscriberGraceRecords(conn, subID(subscriberID),
						subID(callerID), status, fromTime, toTime, graceDeselectedBy, rbtType);

				count = createSubscriberStatus(subscriberID, callerID, categories.id(),
						subscriberWavFile, setTime, startDate, endDate, status, selectedBy,
						selectInfo, nextChargingDate, prepaid, classType, changeSubType, fromTime,
						toTime, sel_status, true, clipMap, categories.type(), useDate, loopStatus,
						isTata, nextPlus, rbtType, selInterval, extraInfo, refID,
						isDirectActivation, circleID, sub, useUIChargeClass, false);
				logger.info("Checking to update num max selections or not." + " count: " + count
						+ ", isPackSel: " + isPackSel + " incrSelCount: " + incrSelCount);
				if (incrSelCount && isPackSel && count == 1)
					ProvisioningRequestsDao.updateNumMaxSelections(conn, subscriberID, packCosID);
				else if (incrSelCount && count == 1)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberID));
                
				if(count == 1){
					/*FREEMIUM MODEL CHANGES*/
					if (isFreemiumUpgradeRequired) {
						com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offer = RBTSMClientHandler
								.getInstance().getOffer(subscriberID,
										selectedBy,
										Offer.OFFER_TYPE_SUBSCRIPTION, null, null, null);
						String freemiumRentalPack = null;
						String extraInfo1 = null;
						HashMap<String, String> attributeMapFromXML = DBUtility
								.getAttributeMapFromXML(sub.extraInfo());
						if (attributeMapFromXML == null) {
							attributeMapFromXML = new HashMap<String, String>();
						}
						if (offer != null && offer.length > 0) {
							freemiumRentalPack = offer[0].getSrvKey();
							String offerId = offer[0].getOfferID();
							attributeMapFromXML.put("OFFER_ID", offerId);
						} else {
							freemiumRentalPack = RBTParametersUtils.getParamAsString("COMMON",
									"FREEMIUM_RENTAL_PACK", null);
						}
						attributeMapFromXML.put(iRBTConstant.EXTRA_INFO_OLD_ACT_BY, sub.activatedBy()); 
						extraInfo1 = DBUtility
								.getAttributeXMLFromMap(attributeMapFromXML);
						RBTDBManager.getInstance().convertSubscriptionType(subscriberID,
								sub.subscriptionClass(), freemiumRentalPack,
								selectedBy, null, false, rbtType, true,
								extraInfo1, sub);

					}
				}
				if (updateEndDate) {
					SubscriberImpl.updateEndDate(conn, subID(subscriberID), endDate, null);
				}

			} else {
				return SELECTION_FAILED_SELECTION_OVERLAP;
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return count > 0 ? SELECTION_SUCCESS : SELECTION_FAILED_INTERNAL_ERROR;

	}
	
	@Override
	public int createSubscriberStatus(String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime, Date endTime, int status,
			String selectedBy, String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime, String sel_status,
			boolean smActivation, HashMap clipMap, int categoryType, boolean useDate,
			char loopStatus, boolean isTata, int nextPlus, int rbtType, String selInterval,
			HashMap extraInfoMap, String refID, boolean isDirectActivation, String circleId,
			Subscriber sub, boolean useUIChargeClass, boolean isFromDownload) {
		logger.info("Adding subscriber selections, subscriberId: " + subscriberID + ", classType: "
				+ classType + ", extraInfoMap: " + extraInfoMap);
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
				loopStatus = getLoopStatusToUpateSelection(loopStatus, subscriberID,
						prepaid.equalsIgnoreCase("y"));
			}
			SubscriberStatus subscriberStatus = null;

			String feedSubType = null;
			if (extraInfoMap != null) {
				feedSubType = (String) extraInfoMap.get("FEED_SUB_TYPE");
				extraInfoMap.remove("FEED_SUB_TYPE");
				logger.info("Feed sub type for Consent = " + feedSubType);
			}
			Category category = getCategory(categoryID); 
			String provRefId = UUID.randomUUID().toString();
			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				extraInfoMap.put("PROV_REF_ID", provRefId);
			}
			String selExtraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			
			if (category != null && category.getType() == PLAYLIST_ODA_SHUFFLE) {
				
				logger.info("going for ODA Shuffle Selections....");
				Clip[] activeClipsInCategory = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryID); 
				logger.info("Shuffle clips ...="+Arrays.toString(activeClipsInCategory));
				if (activeClipsInCategory != null) {
					boolean isFreemiumSubscriber = false;
					boolean isUpgradeRequired = false;
					if(clipMap.containsKey("FREEMIUM_USER")){
						isFreemiumSubscriber = true;
					}
					
					if(clipMap.remove("UPGRADE_REQUIRED") != null) {
						isUpgradeRequired = true;
					}
					
					insertODAPackProvisioningRequest(subID(subscriberID), category,
							selectedBy, selectionInfo, provRefId,callerID, fromTime,
							toTime, status, selInterval,classType,isFreemiumSubscriber, isUpgradeRequired);
					//deactiveODAPackOnMakingSelection(subscriberID,callerID,fromTime,toTime);
					int selCount = 0;
					for(Clip clip : activeClipsInCategory) {
						if(selCount == 0){
							loopStatus = 'o';
						}else{
						    loopStatus = 'l';
						}
						subscriberWavFile = clip.getClipRbtWavFile();
						String odaSelRefID = UUID.randomUUID().toString();
						sel_status = "W";
						classType="FREE";
					    subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID),
							callerID, categoryID, subscriberWavFile, setTime, startTime, endTime,
							status, classType, selectedBy, selectionInfo, nextChargingDate,
							prepaid, fromTime, toTime, smActivation, sel_status, null, null,
							categoryType, loopStatus, nextPlus, rbtType, selInterval, selExtraInfo,
							odaSelRefID, circleId,null, null);
					    if(subscriberStatus!=null){
					    	selCount++;
					    }
					}
				}

			} else {
				 subscriberStatus = SubscriberStatusImpl.insert(conn, subID(subscriberID), callerID,
						categoryID, subscriberWavFile, setTime, startTime, endTime, status,
						classType, selectedBy, selectionInfo, nextChargingDate, prepaid, fromTime,
						toTime, smActivation, sel_status, null, null, categoryType, loopStatus,
						nextPlus, rbtType, selInterval, selExtraInfo, refID, circleId,null, null);
			}

			if (subscriberStatus == null) {
				logger.warn("Selection is not populated into DB, refId: " + refID
						+ ". Returning count: 0");
				return 0;
			} else {
				// count will be incremented when it is successfully inserted
				// into subscriber selections table.
				if (!clipMap.containsKey("RECENT_CLASS_TYPE")) {
					logger.info("Adding Recent class type for selection = " + classType);
					clipMap.put("RECENT_CLASS_TYPE", classType);
				}
				logger.info("Selection is inserted into Selections table" + ". Returning count: 1");
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
					access = AccessImpl.insert(conn, clipID, clipName, year, month, 0, 0, 0,
							currentDate);
				else {
					access.incrementNoOfAccess();
					if (subscriberWavFile != null && subscriberWavFile.indexOf("rbt_ugc_") != -1)
						access.incrementNoOfPlays();
					access.update(conn);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		logger.info("Returning count: " + count);
		return count;
	}
	
	
	protected boolean insertODAPackProvisioningRequest(String subscriberID, Category category,
			String mode, String modeInfo, String transId, String callerId, int fromTime, 
			int toTime, int status, String selInterval,String classType,boolean isFreemiumSubscriber, boolean isUpgradeRequired) {
		if (category == null || category.getType() != PLAYLIST_ODA_SHUFFLE) {
			logger.info("Category type is not ODA Shuffle");
			return false;
		}

		logger.info("InsertODAPackProvisioningRequest :: subscriberID = " + subscriberID
				+ ", CategoryID = " + category.getID() + " , mode = " + mode + " ,modeInfo = "
				+ modeInfo + " ,transId = " + transId + " , CallerID = " + callerId);
		;
		RBTDBManager m_rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = m_rbtDBManager.getSubscriber(subscriberID);
		int packStatus = m_rbtDBManager.getPackStatusToInsert(subscriber);
		int type = category.getID();
		String chargingClass = category.getClassType();
		if(isFreemiumSubscriber){
			logger.info("Freemium  Subscriber. Hence using cos based classtype");
			chargingClass = classType;
//			packStatus = 30;
//			status = 30;
		}
		
		if(isUpgradeRequired) {
			packStatus = 30;
		}
		
		ProvisioningRequests provisioningReqs = new ProvisioningRequests(subscriberID, type, mode,
				modeInfo, transId, chargingClass, packStatus);
		HashMap<String, String> xtraInfoMap = new HashMap<String, String>();
		if (callerId != null) {
			xtraInfoMap.put("CALLER_ID", callerId);
		}
		
		xtraInfoMap.put("FROM_TIME", fromTime+"");
		xtraInfoMap.put("TO_TIME", toTime+"");
		xtraInfoMap.put("STATUS", status+"");
		if(selInterval != null) {	
			xtraInfoMap.put("SEL_INTERVAL", selInterval+"");
		}
		
		String packExtraInfoXml = DBUtility.getAttributeXMLFromMap(xtraInfoMap);
		provisioningReqs.setExtraInfo(packExtraInfoXml);
		int packNumMaxSelection = -1;
		if (packNumMaxSelection != -1) {
			provisioningReqs.setNumMaxSelections(packNumMaxSelection);
		}
		ProvisioningRequests provisioningRequestsTable = null;
		try {
			provisioningRequestsTable = m_rbtDBManager
					.insertProvisioningRequestsTable(provisioningReqs);
			logger.info("provisioningRequestsTable insertion result  : = "
					+ provisioningRequestsTable != null);
		} catch (OnMobileException e) {
			e.printStackTrace();
		}
		return provisioningRequestsTable != null;
	}	
	
	@Override
	public boolean isODAPackRequest(String subscriberId,String refId){
		Connection conn = getConnection();
		if (conn == null)
			return false;
		ProvisioningRequests provisioningRequest = ProvisioningRequestsDao.getByTransId(
				subscriberId, refId);
		if (provisioningRequest != null) {
			int categoryId = provisioningRequest.getType();
			com.onmobile.apps.ringbacktones.rbtcontents.beans.Category category = RBTCacheManager.getInstance()
					.getCategory(categoryId);
			if (category != null) {
				int categoryType = category.getCategoryTpe();
				return categoryType == PLAYLIST_ODA_SHUFFLE;
			}
		}
		return false;
	}
	
	public void deactiveODAPackOnMakingSelection(String subscriberID,String callerID,int fromTime,int toTime){
		Set<String> refIdList = new HashSet<String>();
		SubscriberStatus[] activeSubscriberStatus = getActiveSubscriberStatus(subscriberID,callerID, fromTime, toTime);
		if (activeSubscriberStatus != null && activeSubscriberStatus.length > 0) {
			for (SubscriberStatus subStatus : activeSubscriberStatus) {
				if (subStatus.callerID() != null && subStatus.callerID().equalsIgnoreCase(callerID)
						|| (callerID == null && subStatus.callerID() == null)) {
					String extraInfo2 = subStatus.extraInfo();
					HashMap<String, String> attributeMapFromXML = DBUtility
							.getAttributeMapFromXML(extraInfo2);
					if (attributeMapFromXML != null
							&& attributeMapFromXML.containsKey("PROV_REF_ID")) {
						refIdList.add(attributeMapFromXML.get("PROV_REF_ID"));
					}
				}
			}

			logger.info("Number of ODA Packs to be deactivated = " + refIdList.size());
			if (refIdList.size() > 0) {
				for (String refId : refIdList) {
					ProvisioningRequests provisioningRequest = getProvisioningRequestFromRefId(
							subscriberID, refId);
					String extraInfo = provisioningRequest.getExtraInfo();
					HashMap<String, String> xtraInfoMap = DBUtility
							.getAttributeMapFromXML(extraInfo);
					if (xtraInfoMap == null)
						xtraInfoMap = new HashMap<String, String>();
					// xtraInfoMap.putAll(packExtraInfoMap);
					boolean isPackDeactivated = deactivateODAPack(subscriberID, null, refId,
							xtraInfoMap, null);
					logger.info("Pack Deactivation response : " + isPackDeactivated);
				}
			}

		}

	}
}
