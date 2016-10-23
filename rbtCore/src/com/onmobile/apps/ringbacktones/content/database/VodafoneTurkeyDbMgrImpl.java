package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.v2.util.TPTransactionLogger;

public class VodafoneTurkeyDbMgrImpl extends GrameenDbMgrImpl {

	private static Logger logger = Logger
			.getLogger(VodafoneTurkeyDbMgrImpl.class);

	@Override
	public String addSubscriberDownloadRW(String subscriberId,
			String subscriberWavFile, Categories categories, Date endDate,
			boolean isSubActive, String classType, String selBy,
			String selectionInfo, HashMap<String, String> extraInfo,
			boolean incrSelCount, boolean useUIChargeClass,
			boolean isSmClientModel, HashMap<String, String> responseParams,
			Subscriber consentSubscriber, int status, String callerId,
			String downloadStatus) {

		Connection conn = getConnection();
		boolean incrSelCountParamForGift = RBTParametersUtils
				.getParamAsBoolean(iRBTConstant.COMMON,
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
				if (com.onmobile.apps.ringbacktones.webservice.common.Utility
						.isShuffleCategory(categoryType)) {
					chargeClassStr = getChargeClassForShuffleCatgory(
							subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel,
							packCosID, selBy, extraInfo, nextClass, classType);
				} else {
					chargeClassStr = getChargeClassForNonShuffleCatgory(
							subscriberId, consentSubscriber, categories, clip,
							incrSelCount, subscriberWavFile, isPackSel,
							packCosID, selBy, extraInfo, nextClass, classType);
				}
				if (chargeClassStr != null) {
					if (chargeClassStr.length > 4) {
						nextClass = chargeClassStr[2];
						if (nextClass != null
								&& (nextClass
										.equalsIgnoreCase(SELECTION_FAILED_INTERNAL_ERROR) || nextClass
										.equalsIgnoreCase("FAILURE:TECHNICAL_FAULT"))) {
							return nextClass;
						}
						incrSelCount = (chargeClassStr[0]
								.equalsIgnoreCase("true") ? true : false);
						isPackSel = (chargeClassStr[1].equalsIgnoreCase("true") ? true
								: false);
						classType = chargeClassStr[3];
						packCosID = chargeClassStr[4];
					}

				}

				if (m_overrideChargeClasses != null
						&& classType != null
						&& m_overrideChargeClasses.contains(classType
								.toLowerCase()))
					nextClass = classType;
			}

			SubscriberDownloads downLoad = getSubscriberDownload(
					subID(subscriberId), subscriberWavFile, categoryID,
					categoryType);
			String validateDwnlLimitRes = validateDownloadLimitForCos(subscriberId);
			if (downLoad == null
					|| (downLoad != null && (downLoad.downloadStatus() == STATE_DOWNLOAD_DEACTIVATED || downLoad
							.downloadStatus() == STATE_DOWNLOAD_BOOKMARK))
					|| validateDwnlLimitRes != null) {
				if (!isDownloadAllowed(subscriberId)) {
					return "FAILURE:DOWNLOAD_OVERLIMIT";
				} else if (validateDwnlLimitRes != null) {
					return validateDwnlLimitRes;
				}
			}
			String campaignCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.CAMPAIGN_CODE) : null;
			String treatmentCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.TREATMENT_CODE) : null;
			String offerCode = extraInfo != null ? extraInfo
					.remove(iRBTConstant.OFFER_CODE) : null;

			String downloadExtraInfo = DBUtility
					.getAttributeXMLFromMap(extraInfo);

			String refId = null;
			if (consentSubscriber != null) {
				refId = consentSubscriber.refID();
			}
			if (downLoad != null) {
				char downStat = downLoad.downloadStatus();
				if (downStat == STATE_DOWNLOAD_ACTIVATED
						|| downStat == STATE_DOWNLOAD_CHANGE)
					return "SUCCESS:DOWNLOAD_ALREADY_ACTIVE";
				else if (downStat == STATE_DOWNLOAD_DEACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_DEACTIVATED)
					return "FAILURE:DOWNLOAD_DEACT_PENDING";
				else if (downStat == STATE_DOWNLOAD_ACT_ERROR
						|| downStat == STATE_DOWNLOAD_DEACT_ERROR)
					return "FAILURE:DOWNLOAD_ERROR";
				else if (downStat == STATE_DOWNLOAD_BOOKMARK) {
					String response = isContentExpired(clip, categories);
					if (response != null)
						return response;
					deleteSubscriberDownload(subID(subscriberId),
							subscriberWavFile, categoryID, categoryType);
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
							subscriberId, subscriberWavFile, endDate,
							isSubActive, selBy, selectionInfo, isSmClientModel,
							conn, categoryID, categoryType, nextClass,
							extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {
						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY",
								"SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());
					}

					downloadExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);

					if (null == subscriberDownloads) {
						if (null != extraInfo) {
							extraInfo.remove("CALLER_ID");
							extraInfo.remove("STATUS");
							extraInfo.remove("FROM_TIME");
							extraInfo.remove("TO_TIME");
							extraInfo.remove("INTERVAL");
							extraInfo.remove("LOOP_STATUS");
						}

						subscriberDownloads = SubscriberDownloadsImpl.insertRW(
								conn, subID(subscriberId), subscriberWavFile,
								categoryID, endDate, isSubActive, categoryType,
								nextClass, selBy, selectionInfo,
								downloadExtraInfo, isSmClientModel,
								downloadStatus, null);

						// RBT2.0 changes
						if (subscriberDownloads != null
								&& subscriberDownloads.downloadStatus() == 'y') {
							Subscriber subscriber = getSubscriber(subID(subscriberId));

							TPTransactionLogger.getTPTransactionLoggerObject(
									"download").writeTPTransLog(
									subscriber.circleID(), subID(subscriberId),
									"NA", -1, -1, -1, "NA", categoryType, -1,
									subscriberWavFile, categoryID, -1,
									nextClass, subscriberDownloads.startTime(),
									subscriberDownloads.endTime(), "NA");
						}
					}

					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID",
								subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
							&& incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn,
								subscriberId, packCosID);
					else if (selBy != null
							&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
							&& incrSelCount)
						SubscriberImpl.setSelectionCount(conn,
								subID(subscriberId));

					return "SUCCESS:DOWNLOAD_ADDED";
				} else if (downStat == STATE_DOWNLOAD_ACTIVATION_PENDING
						|| downStat == STATE_DOWNLOAD_TO_BE_ACTIVATED
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
					HashMap<String, String> extraInfoMap = DBUtility
							.getAttributeMapFromXML(downloadExtraInfo);

					SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
							subscriberId, subscriberWavFile, endDate,
							isSubActive, selBy, selectionInfo, isSmClientModel,
							conn, categoryID, categoryType, nextClass,
							extraInfo, refId, useUIChargeClass);

					if (null != subscriberDownloads) {

						// Add following fields to extraInfo

						extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY",
								"SUCCESS");
						extraInfo.put("CONSENTID", subscriberDownloads.refID());
						extraInfo.put("EVENTYPE", "1");
						extraInfo.put("CONSENTCLASSTYPE",
								subscriberDownloads.classType());
					}

					downloadExtraInfo = DBUtility
							.getAttributeXMLFromMap(extraInfoMap);

					if (subscriberDownloads == null) {
						subscriberDownloads = SubscriberDownloadsImpl
								.reactivateRW(conn, subID(subscriberId),
										subscriberWavFile, categoryID, endDate,
										categoryType, isSubActive, nextClass,
										selBy, selectionInfo,
										downloadExtraInfo, isSmClientModel,
										downloadStatus);

						// RBT2.0 changes
						if (subscriberDownloads != null
								&& subscriberDownloads.downloadStatus() == 'y') {
							Subscriber subscriber = getSubscriber(subID(subscriberId));

							TPTransactionLogger.getTPTransactionLoggerObject(
									"download").writeTPTransLog(
									subscriber.circleID(), subID(subscriberId),
									"NA", -1, -1, -1, "NA", categoryType, -1,
									subscriberWavFile, categoryID, -1,
									nextClass, subscriberDownloads.startTime(),
									subscriberDownloads.endTime(), "NA");
						}

						if (subscriberDownloads != null && campaignCode != null
								&& treatmentCode != null && offerCode != null) {
							String msg = iRBTConstant.CAMPAIGN_CODE + "="
									+ campaignCode + ","
									+ iRBTConstant.TREATMENT_CODE + "="
									+ treatmentCode + ","
									+ iRBTConstant.OFFER_CODE + "=" + offerCode
									+ "," + iRBTConstant.RETRY_COUNT + "=0";
							RBTCallBackEvent.insert(subscriberId,
									subscriberDownloads.refID(), msg,
									RBTCallBackEvent.SM_CALLBACK_PENDING,
									RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
									clip.getClipId(), selBy);
						}

					}
					if (subscriberDownloads != null && responseParams != null) {
						responseParams.put("REF_ID",
								subscriberDownloads.refID());
						responseParams.put("CLASS_TYPE", nextClass);
					}
					if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
							&& incrSelCount && isPackSel)
						ProvisioningRequestsDao.updateNumMaxSelections(conn,
								subscriberId, packCosID);
					else if (selBy != null
							&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
							&& incrSelCount)
						SubscriberImpl.setSelectionCount(conn,
								subID(subscriberId));

					return "SUCCESS:DOWNLOAD_REACTIVATED";
				}
			} else {

				String response = isContentExpired(clip, categories);
				if (response != null)
					return response;
				HashMap<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(downloadExtraInfo);

				SubscriberDownloads subscriberDownloads = checkModeAndInsertIntoConsent(
						subscriberId, subscriberWavFile, endDate, isSubActive,
						selBy, selectionInfo, isSmClientModel, conn,
						categoryID, categoryType, nextClass, extraInfo, refId,
						useUIChargeClass);

				if (null != subscriberDownloads) {
					extraInfo.put("CONSENT_INSERTED_SUCCESSFULLY", "SUCCESS");
					extraInfo.put("CONSENTID", subscriberDownloads.refID());
					extraInfo.put("EVENTYPE", "1");
					extraInfo.put("CONSENTCLASSTYPE",
							subscriberDownloads.classType());
				}

				downloadExtraInfo = DBUtility
						.getAttributeXMLFromMap(extraInfoMap);

				if (subscriberDownloads == null) {
					subscriberDownloads = SubscriberDownloadsImpl.insertRW(
							conn, subID(subscriberId), subscriberWavFile,
							categoryID, endDate, isSubActive, categoryType,
							nextClass, selBy, selectionInfo, downloadExtraInfo,
							isSmClientModel, downloadStatus, null);

					// RBT2.0 changes
					if (subscriberDownloads != null
							&& subscriberDownloads.downloadStatus() == 'y') {
						Subscriber subscriber = getSubscriber(subID(subscriberId));

						TPTransactionLogger.getTPTransactionLoggerObject(
								"download").writeTPTransLog(
								subscriber.circleID(), subID(subscriberId),
								"NA", -1, -1, -1, "NA", categoryType, -1,
								subscriberWavFile, categoryID, -1, nextClass,
								subscriberDownloads.startTime(),
								subscriberDownloads.endTime(), "NA");
					}

					logger.info("Campaign Code=" + campaignCode
							+ ",TreatmentCode=" + treatmentCode
							+ ",OfferCount=" + offerCode);
					if (subscriberDownloads != null && campaignCode != null
							&& treatmentCode != null && offerCode != null) {
						String msg = iRBTConstant.CAMPAIGN_CODE + "="
								+ campaignCode + ","
								+ iRBTConstant.TREATMENT_CODE + "="
								+ treatmentCode + "," + iRBTConstant.OFFER_CODE
								+ "=" + offerCode + ","
								+ iRBTConstant.RETRY_COUNT + "=0";
						RBTCallBackEvent.insert(subscriberId,
								subscriberDownloads.refID(), msg,
								RBTCallBackEvent.SM_CALLBACK_PENDING,
								RBTCallBackEvent.MODULE_ID_IBM_INTEGRATION,
								clip.getClipId(), selBy);
					}
				}
				if (subscriberDownloads == null)
					return "FAILURE:INSERTION_FAILED";

				if (subscriberDownloads != null && responseParams != null) {
					responseParams.put("REF_ID", subscriberDownloads.refID());
					responseParams.put("CLASS_TYPE", nextClass);
				}

				if (selBy != null && !selBy.equalsIgnoreCase("GIFT")
						&& incrSelCount && isPackSel)
					ProvisioningRequestsDao.updateNumMaxSelections(conn,
							subscriberId, packCosID);
				else if (selBy != null
						&& (!selBy.equalsIgnoreCase("GIFT") || incrSelCountParamForGift)
						&& incrSelCount)
					SubscriberImpl.setSelectionCount(conn, subID(subscriberId));

				return "SUCCESS:DOWNLOAD_ADDED";
			}
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return "FAILURE:TECHNICAL_FAULT";

	}

	private String validateDownloadLimitForCos(String subscriberId)
			throws RBTException {
		// Checking Charge Class Downloads Limit
		Subscriber subscriber = getSubscriber(subID(subscriberId));
		String cosID = subscriber.cosID();
		CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager()
				.getCosDetail(cosID);
		if (cosDetail!=null && iRBTConstant.SUB_CLASS.equalsIgnoreCase(cosDetail
				.getCosType())) {
			int chargeClassCount = 0;
			String[] chargeClassTokens = cosDetail.getFreechargeClass().split(",");
			for (String chargeClassToken : chargeClassTokens) {
				int startIndex = chargeClassToken.indexOf('*');
				// extraInfoMap.put(param_alreadyGetSelOffer, "true");
				if (startIndex != -1) {
					chargeClassCount = Integer.parseInt(chargeClassToken
							.substring(startIndex + 1));
				} else {
					logger.error("CHARGE CLASS IS NOT CONFIGURED SO BY DEFAULT DOWNLOAD COUNT IS CONSIDERING AS ZERO");
					return "FAILURE:TECHNICAL_FAULT";
				}
			}
			if (subscriber != null
					&& subscriber.maxSelections() >= chargeClassCount) {
				return iRBTConstant.PACK_DOWNLOAD_LIMIT_REACHED;
			}
		}
		return null;
	}

}
