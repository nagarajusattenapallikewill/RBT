package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vivabahrain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.telefonica.TelefonicaSmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VivaSmsProcessor extends TelefonicaSmsProcessor {
	protected static Logger logger = Logger.getLogger(VivaSmsProcessor.class);

	String azaanRefId = null;
	String azaanCosId = null;

	String dateFormat = param(SMS, "DCT_MANAGE_DATE_FORMAT", "dd/mm/yyyy");

	public VivaSmsProcessor() throws RBTException {
		super();
	}

	public String processDeactivation(Task task) {
		boolean isAzaanPackExists = false;
		logger.info("RBT:: processDeactivation : " + task);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = subscriber.getSubscriberID();
		String subscriberId = subscriber.getSubscriberID();
		boolean isDirectDctRequest = false;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (isThisFeature(task, SmsKeywordsStore.deactivateProfileKeywordsSet,
				TEMPORARY_OVERRIDE_CANCEL_MESSAGE)) {
			logger.info("Redirecting the request to profile deactivation block");
			processRemoveTempOverride(task);
			return null;
		}

		if (isThisFeature(task, SmsKeywordsStore.unrandomizeKeywordSet,
				UNRANDOMIZE_KEYWORD)) {
			logger.info("Redirecting the request to profile deactivation block");
			disableRandomization(task);
			return null;
		}

		if (task.containsKey(param_isdirectdct))
			isDirectDctRequest = task.getString(param_isdirectdct)
					.equalsIgnoreCase("true");

		if (isDirectDctRequest) {
			if (subscriber.getStatus().equalsIgnoreCase(
					WebServiceConstants.DEACTIVE)) {
				task.setObject(param_responseSms, "NOTACTIVE");
				return null;
			}
		} else if (!isUserActive(subscriber.getStatus())) {
			String deactSmsText = getSMSTextForID(task,
					"DEACT_NOT_ALLOWED_FOR_"
							+ subscriber.getStatus().toUpperCase(), null,
					subscriber.getLanguage());
			if (deactSmsText == null) {
				deactSmsText = getSMSTextForID(task, HELP_SMS_TEXT,
						m_helpDefault, subscriber.getLanguage());
			}
			task.setObject(param_responseSms, deactSmsText);
			return null;
		} else if (!param(COMMON, "ALLOW_DEACTIVATION_FOR_GRACE_USERS", true)
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.GRACE)) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task,
							DEACTIVATION_NOT_ALLOWED_FOR_GRACE_SMS,
							m_deactivationNotAllowedForGraceDefault,
							subscriber.getLanguage()));
			return null;
		}

		if (subscriber.getUserInfoMap() != null
				&& subscriber.getUserInfoMap().get("DELAY_DEACT") != null
				&& subscriber.getUserInfoMap().get("DELAY_DEACT")
						.equalsIgnoreCase("true")) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task,
							DEACTIVATION_NOT_ALLOWED_FOR_DELAYED_DEACT_SMS,
							m_deactivationNotAllowedForDelayedDeactDefault,
							subscriber.getLanguage()));
			return null;
		}

		if (param(SMS, CONFIRM_DEACTIVATION, false)) {
			processDeactivationConfirm(task);
			return null;
		}
		task.setObject(param_SMSTYPE, "SUBSCRIBER_DC_CONFIRM_PENDING");
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerID);
		// rbtDetailsRequest.setMode("CCC");
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
				rbtDetailsRequest);
		Library library = rbt == null ? null : rbt.getLibrary();
		Downloads downloads = library == null ? null : library.getDownloads();
		boolean proceedBaseDCT = false;
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		HashMap<String, String> extraInfoMap = subscriber.getUserInfoMap();
		if (extraInfoMap != null && extraInfoMap.containsKey(PACK)) {
			String circleID = subscriber.getCircleID();
			String[] cosIds = extraInfoMap.get(PACK).split(",");

			for (String cosID : cosIds) {
				CosDetails cosDetails = (CosDetails) CacheManagerUtil
						.getCosDetailsCacheManager().getActiveCosDetail(cosID,
								circleID);
				if (cosDetails != null
						&& (RBTParametersUtils.getParamAsString(SMS,
								"SMS_AZAAN_COS_TYPES", "AZAAN")
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					List<ProvisioningRequests> response = ProvisioningRequestsDao
							.getBySubscriberIDTypeAndNonDeactivatedStatus(
									subscriber.getSubscriberID(),
									Integer.parseInt(cosID));
					if (response != null && !response.isEmpty()) {
						for (ProvisioningRequests provreq : response) {
							if (provreq.getStatus() == iRBTConstant.PACK_ACTIVATED) {
								isAzaanPackExists = true;
								azaanRefId = provreq.getTransId();
								azaanCosId = cosID;
								break;
							}
						}
					}
				}
			}
		}
		if ((downloads == null || downloads.getDownloads() == null || downloads
				.getNoOfActiveDownloads() == 0) && !isAzaanPackExists) {
			logger.info("Making base deactivation as true since no of active downloads are empty");
			proceedBaseDCT = true;
		}
		if (downloads != null && downloads.getNoOfActiveDownloads() == 1
				&& !isAzaanPackExists
				&& (smsList == null || smsList.size() == 0)) {
			ArrayList<String> smsLi = new ArrayList<String>();
			Clip clip = getClipById(String.valueOf(downloads.getDownloads()[0]
					.getToneID()));
			smsLi.add(String.valueOf(clip.getClipPromoId()));
			task.setObject(param_smsText, smsLi);
			processDeactivateDownload(task);
		} else if ((downloads == null || downloads.getNoOfActiveDownloads() == 0)
				&& isAzaanPackExists
				&& (smsList == null || smsList.size() == 0)) {
			processDeactivateAzaan(task);
		} else if ((downloads != null && downloads.getNoOfActiveDownloads() >= 1)
				|| isAzaanPackExists || (smsList != null && smsList.size() > 0)) {
			processSongManageDeact(task);
		}
		String smsText = null;
		if (proceedBaseDCT) {
			// super.processDeactivationConfirmed(task,
			// "SUBSCRIBER_DC_CONFIRM");
			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					subscriberId);
			String mode = task.getString(param_mode);
			subscriptionRequest.setMode(mode);
			RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);
			task.setObject(param_responseSms, subscriptionRequest.getResponse());
		}

		if (callerID.equalsIgnoreCase("ALL")) {
			callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
		}
		smsText = task.getString(param_responseSms);
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger.info("Response after processing deactivation request: "
				+ task.getString(param_responseSms));
		logger.info("modified task object :" + task);
		return task.getString(param_responseSms);
	}

	@SuppressWarnings("unchecked")
	public void processDeactivateDownload(Task task) {
		boolean isAzaanPackExists = false;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = subscriber.getSubscriberID();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		boolean isCheckCategorySmsAlias = param(SMS, CHECK_CATEGORY_SMS_ALIAS,
				false);
		boolean makeEntryForViralDCT = false;
		boolean proceedDownloadDCT = false;
		boolean doubleConfirmationDCT = false;

		String subscriberId = subscriber.getSubscriberID();
		Date nextBillingDate = null;
		if (!isUserActive(subscriber.getStatus())) {
			String deactDownloadSmsText = getSMSTextForID(task,
					"DEACT_DOWNLOAD_NOT_ALLOWED_FOR_"
							+ subscriber.getStatus().toUpperCase(), null,
					subscriber.getLanguage());
			logger.info("Unable to process deactive download request, subscriber: "
					+ subscriberId
					+ " is not active . Deact Download Sms Text ="
					+ deactDownloadSmsText);
			if (deactDownloadSmsText == null) {
				deactDownloadSmsText = getSMSTextForID(task, HELP_SMS_TEXT,
						m_helpDefault, subscriber.getLanguage());
			}
			task.setObject(param_responseSms, deactDownloadSmsText);
			return;
		}
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);

		logger.debug("Processing deactive download request for subscriber: "
				+ subscriberId + ", keywords: " + smsList);
		if ((smsList == null || smsList.size() < 1)
				&& !task.containsKey(param_promoID)) {
			logger.info("Unable to process deactive download request for subscriber: "
					+ subscriberId + ", sms keywords does not exists");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, DOWNLOAD_DEACT_INVALID_PROMO_ID,
							m_downloadDeactInvalidPromoId,
							subscriber.getLanguage()));
			return;
		}

		String keyword = smsList.get(0).trim();
		if (keyword == null)
			keyword = task.getString(param_promoID);
		logger.debug("Checking clip by promo id: " + keyword
				+ " for subscriber: " + subscriberId);
		Clip clip = getClipByPromoId(keyword, subscriber.getLanguage());
		String categoryId = null;
		String wavFile = null;
		Category category = null;
		if (clip != null) {
			wavFile = clip.getClipRbtWavFile();
		} else {
			logger.debug("Since clip doesnt exists, checking for category for"
					+ " subscriber: " + subscriberId);
			category = getCategoryByPromoId(keyword, subscriber.getLanguage());
			if (null == category && isCheckCategorySmsAlias) {
				logger.debug("Checking category based on sms alias for"
						+ " subscriber: " + subscriberId);
				category = getCategoryBySMSAlias(keyword,
						subscriber.getLanguage());
			}
			if (category != null) {
				categoryId = String.valueOf(category.getCategoryId());
			}
		}

		if (wavFile == null && categoryId == null) {
			logger.info("Unable to process deactive download request for subscriber: "
					+ subscriberId
					+ ", wave file and category id are not found");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, DOWNLOAD_DEACT_INVALID_PROMO_ID,
							m_downloadDeactInvalidPromoId,
							subscriber.getLanguage()));
			return;
		}

		if(task.getString(param_smsSent).contains("OFF")){
			task.setObject(param_SMSTYPE, "SUBSCRIBER_DC_CONFIRM_PENDING");
		}else if(task.getString(param_smsSent).contains("DEL")){
			task.setObject(param_SMSTYPE, "DOWNLOAD_DC_CONFIRM_PENDING");
		}
		ViralData context[] = getViraldata(task);
		boolean isDownloadsModel = RBTParametersUtils.getParamAsBoolean(
				"COMMON", "ADD_TO_DOWNLOADS", "FALSE");
		int noOfSongs = 0;
		boolean isPromoCodeExistsInViral = false;
		boolean isSongPresent = false;
		String language = subscriber.getLanguage();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerID);
		// rbtDetailsRequest.setMode("CCC");
		rbtDetailsRequest.setInfo("subscriber,downloads");
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
				rbtDetailsRequest);

		Library library = rbt == null ? null : rbt.getLibrary();
		Downloads downloads = library == null ? null : library.getDownloads();

		if (downloads == null || downloads.getDownloads() == null
				|| downloads.getNoOfActiveDownloads() == 0) {
			logger.info("Unable to process deactive download request since download entry is empty");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, TECHNICAL_FAILURE,
							m_technicalFailuresDefault, language));
			return;
		}

		Download[] downloadArray = downloads.getDownloads();

		subscriber = rbt == null ? null : rbt.getSubscriber();
		List<Download> subscriberDownloads = Arrays.asList(downloadArray);
		Download selectedDownload = null;

		if (!isSongPresent && isDownloadsModel) {
			noOfSongs = downloads.getNoOfActiveDownloads();
			if (subscriberDownloads != null
					&& downloads.getNoOfActiveDownloads() > 0) {
				for (Download download : subscriberDownloads) {
					if (download.getRbtFile()
							.contains(clip.getClipRbtWavFile())) {
						selectedDownload = download;
						nextBillingDate = download.getNextBillingDate();
						isSongPresent = true;
						break;
					}
				}
			}
		}
		if (selectedDownload == null || !isSongPresent) {
			logger.info("selected download is not found in the downloads list");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, SELECTED_SONG_NOT_IN_DOWNLOAD,
							m_nopromoIdDefault, language));
			return;
		}

		if (context != null && context.length > 0) {
			Date viralDataSmsTime = null;
			for (ViralData vt : context) {
				String viralDataPromoId = vt.getClipID();
				isPromoCodeExistsInViral = false;
				if (clip != null) {
					isPromoCodeExistsInViral = (clip.getClipPromoId()
							.equalsIgnoreCase(viralDataPromoId));
				} else if (category != null) {
					isPromoCodeExistsInViral = (category.getCategoryPromoId()
							.equalsIgnoreCase(viralDataPromoId));
				}
				if (isPromoCodeExistsInViral) {
					viralDataSmsTime = vt.getSentTime();
					logger.info("download deactivation is present in the viral sms table");
					break;
				}
			}
			if (isPromoCodeExistsInViral) {
				long configuredTime = RBTParametersUtils.getParamAsLong(COMMON,
						"DOWNLOAD_DEACTIVATION_TIME", 60);
				long smsSendTime = new Date().getTime();
				if (null != viralDataSmsTime) {
					smsSendTime = viralDataSmsTime.getTime();
				}
				long sentTimeAfterDuration = smsSendTime
						+ (configuredTime * 60 * 1000L);
				if (System.currentTimeMillis() < sentTimeAfterDuration) {
					doubleConfirmationDCT = true;
				} else {
					logger.info("download deactivation entry is present in the viral sms table and it is expired.");
					makeEntryForViralDCT = true;
				}
				if(task.getString(param_smsSent).contains("OFF")){
					removeViraldata(subscriber.getSubscriberID(), callerID,
							"SUBSCRIBER_DC_CONFIRM_PENDING");
				}else if(task.getString(param_smsSent).contains("DEL")){
					removeViraldata(subscriber.getSubscriberID(), callerID,
							"DOWNLOAD_DC_CONFIRM_PENDING");
				}
			} else {
				makeEntryForViralDCT = true;
			}
		} else if (context == null || context.length < 1 || context[0] == null
				|| !isPromoCodeExistsInViral) {
			logger.info("download deactivation entry is not present in the viral sms table.");
			makeEntryForViralDCT = true;
		}
		String contentName = "";
		String smsText = null;
		hashMap.put("PROMO_ID", keyword);
		if (clip != null)
			task.setObject(param_clipid, "" + clip.getClipId());
		if (callerID == null)
			callerID = "ALL";
		if (callerID.equalsIgnoreCase("ALL")) {
			callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
		}

		if (clip != null)
			contentName = clip.getClipName();
		else if (category != null)
			contentName = category.getCategoryName();
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SONG_NAME", contentName);
		if (makeEntryForViralDCT) {
			long daysLeft = 0;
			long daysAllowed = param(COMMON,
					"ALLOW_DOWNLOAD_DEACTIVATION_BEFORE_N_DAYS", 0);
			if (nextBillingDate != null) {
				daysLeft = (nextBillingDate.getTime() - System
						.currentTimeMillis()) / (1000 * 24 * 60 * 60);
			}
			hashMap.put("DAYS_LEFT", String.valueOf(daysLeft));
			hashMap.put("DEACT_CONFIRM_DAYS", String.valueOf(daysAllowed));
			String renewalDate = new SimpleDateFormat(dateFormat)
					.format(nextBillingDate);
			hashMap.put("RENEWAL_DATE", renewalDate);
			if (noOfSongs > 1) {
				if ((daysLeft <= daysAllowed) || doubleConfirmationDCT) {
					logger.info("Proceeding to direct deactivation since number of days left is less than the number of days allowed and no of songs: "
							+ noOfSongs);
					proceedDownloadDCT = true;
				} else {
					ViralData viralData = insertViralData(subscriber, callerID,
							keyword, "DEL");
					if (viralData == null) {
						logger.info("Error occured during viral table insertion");
						smsText = getSMSTextForID(task,
								DOWNLOAD_DEACT_VIRAL_FAILURE,
								m_technicalFailuresDefault,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", smsText);
						smsText = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsText);
						return;
					} else {
						smsText = getSMSTextForID(task,
								DOWNLOAD_DEACT_VIRAL_SUCCESS,
								m_DctDownloadDoubleConfirmationDefault,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", smsText);
						smsText = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsText);
						return;
					}
				}

			} else if (noOfSongs == 1) {
				HashMap<String, String> extraInfoMap = subscriber
						.getUserInfoMap();
				if (extraInfoMap != null && extraInfoMap.containsKey(PACK)) {
					String circleID = subscriber.getCircleID();
					String[] cosIds = extraInfoMap.get(PACK).split(",");

					for (String cosID : cosIds) {
						CosDetails cosDetails = (CosDetails) CacheManagerUtil
								.getCosDetailsCacheManager()
								.getActiveCosDetail(cosID, circleID);
						if (cosDetails != null
								&& (RBTParametersUtils.getParamAsString(SMS,
										"SMS_AZAAN_COS_TYPES", "AZAAN")
										.equalsIgnoreCase(cosDetails
												.getCosType()))) {
							List<ProvisioningRequests> response = ProvisioningRequestsDao
									.getBySubscriberIDTypeAndNonDeactivatedStatus(
											subscriber.getSubscriberID(),
											Integer.parseInt(cosID));
							isAzaanPackExists = (response != null);
							break;
						}
					}
				}
				if (daysLeft <= daysAllowed) {
					logger.info("Proceeding to direct deactivation since number of days left is less than the number of days allowed and no of songs: "
							+ noOfSongs);
					proceedDownloadDCT = true;
				} else {
					ViralData viralData = insertViralData(subscriber, callerID,
							keyword, task.getString(param_smsSent).contains("OFF")?"OFF":"DEL");
					if (viralData == null) {
						logger.info("Error occured during viral table insertion");
						smsText = getSMSTextForID(task,
								DOWNLOAD_DEACT_VIRAL_FAILURE,
								m_technicalFailuresDefault,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", smsText);
						smsText = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsText);
						return;
					} else {
						smsText = getSMSTextForID(task,
								DOWNLOAD_DEACT_VIRAL_SUCCESS,
								m_DctDownloadDoubleConfirmationDefault,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", smsText);
						smsText = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsText);
						return;
					}
				}
			}
		}
		smsText = null;
		if (proceedDownloadDCT || doubleConfirmationDCT) {
			//String deactivateSelResponse = processDeactivateSelection(task);
			task.setObject(param_catid, "" + selectedDownload.getCategoryID());
			String deleteSubscriberDownload = deleteSubscriberDownload(task);
			if (!isAzaanPackExists
					&& noOfSongs == 1
					&& WebServiceConstants.SUCCESS
							.equalsIgnoreCase(deleteSubscriberDownload)) {
				super.processDeactivationConfirmed(task,
						"SUBSCRIBER_DC_CONFIRM");
			}
			logger.info("delete subscriber download response: "
					+ deleteSubscriberDownload);

			if (deleteSubscriberDownload
					.equalsIgnoreCase(WebServiceConstants.SUCCESS))
				smsText = getSMSTextForID(task, DOWNLOAD_DEACT_SUCCESS,
						m_downloadDeactSuccessDefault, subscriber.getLanguage());
			else
				smsText = getSMSTextForID(task, DOWNLOAD_DEACT_FAILURE,
						m_downloadDeactFailureDefault, subscriber.getLanguage());
		}
		logger.info("Configured sms text for deactivation: " + smsText);
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		hashMap.put("PROMO_ID", keyword);
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger.info("Response after processing deactivation request: "
				+ task.getString(param_responseSms));
	}

	public void processDeactivateAzaan(Task task) {
		boolean isAzaanPackExists = false;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = subscriber.getSubscriberID();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		boolean makeEntryForViralDCT = false;
		boolean proceedAzaanDCT = false;
		boolean doubleConfirmationDCT = false;
		int noOfPacks = 0;
		String subscriberId = subscriber.getSubscriberID();
		Date nextBillingDate = null;
		boolean deActBase = false;
		String cosId = null;
		String cosType = null;
		String provRefId = null;
		if (!isUserActive(subscriber.getStatus())) {
			String deactAzaanSmsText = getSMSTextForID(task,
					"DEACT_AZAAN_NOT_ALLOWED_FOR_"
							+ subscriber.getStatus().toUpperCase(), null,
					subscriber.getLanguage());
			logger.info("Unable to process deactive azaan request for subscriber: "
					+ subscriberId
					+ " is not active . Deact Azaan Sms Text ="
					+ deactAzaanSmsText);
			if (deactAzaanSmsText == null) {
				deactAzaanSmsText = getSMSTextForID(task, HELP_SMS_TEXT,
						m_helpDefault, subscriber.getLanguage());
			}
			task.setObject(param_responseSms, deactAzaanSmsText);
			return;
		}

		HashMap<String, String> extraInfoMap = subscriber.getUserInfoMap();
		if (extraInfoMap != null && extraInfoMap.containsKey(PACK)) {
			String circleID = subscriber.getCircleID();
			String[] cosIds = extraInfoMap.get(PACK).split(",");
			if (cosIds.length == 1) {
				noOfPacks = 1;
			}
			String azaanCosTypes = RBTParametersUtils.getParamAsString(SMS,
					"SMS_AZAAN_COS_TYPES", "AZAAN");
			logger.info("Configured azaan cos types are: " + azaanCosTypes);
			if (azaanCosTypes != null && !azaanCosTypes.isEmpty()) {
				List<String> azaanCosTypesList = Arrays.asList(azaanCosTypes
						.split(","));
				for (String cosID : cosIds) {
					CosDetails cosDetails = (CosDetails) CacheManagerUtil
							.getCosDetailsCacheManager().getActiveCosDetail(
									cosID, circleID);
					if (cosDetails != null
							&& azaanCosTypesList.contains(cosDetails
									.getCosType())) {
						cosId = cosID;
						cosType = cosDetails.getCosType();
						List<ProvisioningRequests> response = ProvisioningRequestsDao
								.getBySubscriberIDTypeAndNonDeactivatedStatus(
										subscriber.getSubscriberID(),
										Integer.parseInt(cosID));
						if (response != null && !response.isEmpty()) {
							for (ProvisioningRequests provreq : response) {
								if (provreq.getStatus() == iRBTConstant.PACK_ACTIVATED) {
									isAzaanPackExists = true;
									provRefId = provreq.getTransId();
									break;
								}
							}
						}
					}
				}
			}
		}
		if (!isAzaanPackExists) {
			logger.info("Unable to process deactive azaan deactivate request for subscriber: "
					+ subscriberId + ", is not having any active azaan pack");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, AZAAN_DEACT_FAILURE,
							m_downloadDeactFailureDefault,
							subscriber.getLanguage()));
			return;
		}

		if(task.getString(param_smsSent).contains("OFF")){
			task.setObject(param_SMSTYPE, "SUBSCRIBER_DC_CONFIRM_PENDING");
		}else if(task.getString(param_smsSent).contains("UNSUB")){
			task.setObject(param_SMSTYPE, "AZAAN_DC_CONFIRM_PENDING");
		}
		ViralData context[] = getViraldata(task);
		boolean isAzaanExistsInViral = false;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerID);
		// rbtDetailsRequest.setMode("CCC");
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
				rbtDetailsRequest);

		Library library = rbt == null ? null : rbt.getLibrary();
		Downloads downloads = library == null ? null : library.getDownloads();

		if (downloads == null || downloads.getDownloads() == null
				|| downloads.getNoOfActiveDownloads() == 0) {
			logger.info("Making base deactivation as true since no of active downloads are empty");
			deActBase = true;
		}
		SubscriberPack[] subscriberPacksArr = rbt.getSubscriberPacks();
		List<SubscriberPack> subscriberPacks = Arrays
				.asList(subscriberPacksArr);
		for (SubscriberPack subPack : subscriberPacks) {
			if (subPack.getCosId().equalsIgnoreCase(cosId)
					&& subPack.getCosType().equalsIgnoreCase(cosType)
					&& subPack.getIntRefId().equalsIgnoreCase(provRefId)) {
				nextBillingDate = subPack.getNextChargingDate();
			}
		}
		if (context != null && context.length > 0) {
			Date viralDataSmsTime = null;
			for (ViralData vt : context) {
				if (subscriberId.equalsIgnoreCase(vt.getSubscriberID())
						&& task.getString(param_SMSTYPE).equalsIgnoreCase(
								vt.getType())) {
					isAzaanExistsInViral = true;
					viralDataSmsTime = vt.getSentTime();
					logger.info("azaan deactivation is present in the viral sms table");
					break;
				}
			}
			if (isAzaanExistsInViral) {
				long configuredTime = RBTParametersUtils.getParamAsLong(COMMON,
						"AZAAN_DEACTIVATION_TIME", 60);
				long smsSendTime = new Date().getTime();
				if (null != viralDataSmsTime) {
					smsSendTime = viralDataSmsTime.getTime();
				}
				long sentTimeAfterDuration = smsSendTime
						+ (configuredTime * 60 * 1000L);
				if (System.currentTimeMillis() < sentTimeAfterDuration) {
					doubleConfirmationDCT = true;
				} else {
					logger.info("azaan deactivation entry is present in the viral sms table and it is expired.");
					makeEntryForViralDCT = true;
				}
				if(task.getString(param_smsSent).contains("OFF")){
					removeViraldata(subscriber.getSubscriberID(), callerID,
							"SUBSCRIBER_DC_CONFIRM_PENDING");
				}else if(task.getString(param_smsSent).contains("UNSUB")){
					removeViraldata(subscriber.getSubscriberID(), callerID,
							"AZAAN_DC_CONFIRM_PENDING");
				}
			} else {
				makeEntryForViralDCT = true;
			}
		} else if (context == null || context.length < 1 || context[0] == null
				|| !isAzaanExistsInViral) {
			logger.info("azaan deactivation entry is not present in the viral sms table.");
			makeEntryForViralDCT = true;
		}
		String smsText = null;
		if (makeEntryForViralDCT) {
			long daysLeft = 0;
			long daysAllowed = param(COMMON,
					"ALLOW_AZAAN_DEACTIVATION_BEFORE_N_DAYS", 0);
			if (nextBillingDate != null) {
				daysLeft = (nextBillingDate.getTime() - System
						.currentTimeMillis()) / (1000 * 24 * 60 * 60);
			}
			hashMap.put("DAYS_LEFT", String.valueOf(daysLeft));
			hashMap.put("DEACT_CONFIRM_DAYS", String.valueOf(daysAllowed));
			if ((daysLeft <= daysAllowed) || doubleConfirmationDCT) {
				logger.info("Proceeding for direct deactivation since number of days left is less than the number of days allowed to deactivate");
				proceedAzaanDCT = true;
			} else {
				ViralData viralData = insertViralData(subscriber, callerID,
						null, task.getString(param_smsSent).contains("OFF")?"OFF":"UNSUB");
				if (viralData == null) {
					logger.info("Error occured during viral table insertion");
					smsText = getSMSTextForID(task, AZAAN_DEACT_VIRAL_FAILURE,
							m_technicalFailuresDefault,
							subscriber.getLanguage());
					hashMap.put("SMS_TEXT", smsText);
					smsText = finalizeSmsText(hashMap);
					task.setObject(param_responseSms, smsText);
					return;
				} else {
					smsText = getSMSTextForID(task, AZAAN_DEACT_VIRAL_SUCCESS,
							m_DctDownloadDoubleConfirmationDefault,
							subscriber.getLanguage());
					hashMap.put("SMS_TEXT", smsText);
					smsText = finalizeSmsText(hashMap);
					task.setObject(param_responseSms, smsText);
					return;
				}
			}
		}
		smsText = null;
		String deActPackResponse = null;
		if (proceedAzaanDCT || doubleConfirmationDCT) {
			if (deActBase) {
				logger.info("Trying to deactivate base and pack");
				task.setObject(param_cosid, Integer.parseInt(cosId));
				task.setObject(param_refID, provRefId);
				logger.info("Trying to deactivate pack with cos id: " + cosId
						+ " and refId: " + provRefId);
				deActPackResponse = super.processDeactivationPack(task);
				logger.info("Response after processing pack deactivation: "
						+ deActPackResponse);
				if (noOfPacks == 1
						&& WebServiceConstants.SUCCESS
								.equalsIgnoreCase(deActPackResponse)) {
					logger.info("In base deactivation");
					super.processDeactivationConfirmed(task,
							"SUBSCRIBER_DC_CONFIRM");
				}

			} else {
				logger.info("Trying to deactivate only pack and ignored base since the subscriber is havig active downloads");
				logger.info("Trying to deactivate pack with cos id: " + cosId
						+ " and refId: " + provRefId);
				task.setObject(param_cosid, Integer.parseInt(cosId));
				task.setObject(param_refID, provRefId);
				deActPackResponse = super.processDeactivationPack(task);
				logger.info("Response after processing pack deactivation: "
						+ deActPackResponse);
			}
		}

		if (callerID.equalsIgnoreCase("ALL")) {
			callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
		}

		if (WebServiceConstants.SUCCESS.equalsIgnoreCase(deActPackResponse))
			smsText = getSMSTextForID(task, AZAAN_DEACT_SUCCESS,
					m_downloadDeactSuccessDefault, subscriber.getLanguage());
		else
			smsText = getSMSTextForID(task, AZAAN_DEACT_FAILURE,
					m_downloadDeactFailureDefault, subscriber.getLanguage());

		logger.info("Configured sms text for deactivation: " + smsText);
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger.info("Response after processing deactivation request: "
				+ task.getString(param_responseSms));
	}

	@Override
	public void processSongManageDeact(Task task) {
		try {
			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			String language = subscriber.getLanguage();
			@SuppressWarnings("unchecked")
			ArrayList<String> smsList = (ArrayList<String>) task
					.getObject(param_smsText);
			ViralData[] viralDataArray = getViraldata(
					subscriber.getSubscriberID(), null, SMS_DCT_SONG_MANAGE);
			if (smsList == null || smsList.size() <= 0) {
				logger.debug("in the request specific selection is not there so sending the fresh dowload list");
				if (viralDataArray != null && viralDataArray.length > 0) {
					logger.debug("present in viral entry so making direct deactivation of base");
					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
							subscriber.getSubscriberID());
					String mode = task.getString(param_mode);
					subscriptionRequest.setMode(mode);
					RBTClient.getInstance().deactivateSubscriber(
							subscriptionRequest);
					task.setObject(param_responseSms,
							subscriptionRequest.getResponse());
					removeViraldata(subscriber.getSubscriberID(),
							SMS_DCT_SONG_MANAGE);
				} else {
					listAndSendActiveDownloads(1, task);
				}
				return;
			}
			String selectionString = smsList.get(0).toUpperCase();
			logger.debug("the requested tokens are :" + selectionString);

			if ((viralDataArray == null || viralDataArray.length <= 0)
					&& selectionString.length() > 1) {
				logger.debug("the request session expired as there are no entry in the viral sms table so send the fresh downloads");
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, DCT_MANAGE_SESSION_EXPIRED,
								m_smsSessionExpireDefault, language));
				return;
			}

			String optionPrefix = getParamAsString(param_sms,
					DCT_SONG_OPTION_PREFIX, null);
			if (viralDataArray != null && viralDataArray.length > 0) {
				if (optionPrefix == null) {
					optionPrefix = "";
				}
				ViralData viralData = viralDataArray[0];
				HashMap<String, String> extraInfoMap = viralData.getInfoMap();
				logger.info("extraInfo of viralData :" + viralData.toString());
				boolean keyFound = extraInfoMap.containsKey(selectionString);

				if (keyFound && optionPrefix != null
						&& selectionString.equalsIgnoreCase(optionPrefix + "0")) {
					int alphaStartIndex = Integer.valueOf(extraInfoMap
							.get(selectionString));
					logger.info("the next alphabet for more is :"
							+ extraInfoMap.get(selectionString)
							+ " and index is " + alphaStartIndex);
					task.setObject(param_EXTRAINFO, viralData.getInfoMap());
					listAndSendActiveDownloads(alphaStartIndex, task);
				} else if (keyFound) {
					String id = extraInfoMap.get(selectionString);
					if (id.contains("AZAAN")) {
						logger.info("Trying to deactivate pack with cos id: "
								+ azaanCosId + " and refId: " + azaanRefId);
						task.setObject(param_cosid,
								Integer.parseInt(azaanCosId));
						task.setObject(param_refID, id.replace("AZAAN:", ""));
						String deActPackResponse = super
								.processDeactivationPack(task);
						String smsText = null;
						if (WebServiceConstants.SUCCESS
								.equalsIgnoreCase(deActPackResponse))
							smsText = getSMSTextForID(task,
									AZAAN_DEACT_SUCCESS,
									m_downloadDeactSuccessDefault,
									subscriber.getLanguage());
						else
							smsText = getSMSTextForID(task,
									AZAAN_DEACT_FAILURE,
									m_downloadDeactFailureDefault,
									subscriber.getLanguage());
						task.setObject(param_responseSms, smsText);
					} else {
						Clip selectionClip = rbtCacheManager.getClip(id,
								language);
						logger.debug("the selection and is processed selection rbtwavfile :"
								+ extraInfoMap.get(selectionString)
								+ "for selection String :"
								+ selectionString
								+ " selectionClip :" + selectionClip);
						task.setObject(CLIP_OBJ, selectionClip);
						task.setObject(param_promoID,
								selectionClip.getClipPromoId());

						if (param(SMS, CONFIRM_SONG_DEACTIVATION, false)) {
							task.setObject(param_promoID,
									selectionClip.getClipPromoId());
							task.setObject(CLIP_OBJ, selectionClip);
							int categoryId = getDownloadCategoryId(
									subscriber.getSubscriberID(),
									selectionClip.getClipRbtWavFile());
							task.setObject(param_catid,
									String.valueOf(categoryId));
							processSongDeactivationConfirmed(task);
						}
					}
					removeViraldata(subscriber.getSubscriberID(),
							SMS_DCT_SONG_MANAGE);
				} else {
					logger.debug("user sent the invalid reuest string:"
							+ selectionString + " for :" + viralData.toString());
					task.setObject(
							param_responseSms,
							getSMSTextForID(task, INVALID_USER_REQUEST,
									m_smsRequestFailureDefault, language));
					return;
				}
			}
		} catch (Exception e) {
			logger.info("RBT:: processSongManageDeact : " + e.getMessage());
		}
	}

	public void listAndSendActiveDownloads(int accessCount, Task task) {
		boolean isAzaanPackExists = false;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String language = subscriber.getLanguage();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriber.getSubscriberID());
		rbtDetailsRequest.setStatus("" + status);
		Downloads downloads = RBTClient.getInstance().getDownloads(
				rbtDetailsRequest);
		if (downloads == null || downloads.getDownloads() == null
				|| downloads.getNoOfActiveDownloads() == 0) {
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, NO_DOWNLOADS,
							m_downloadsNoSelDefault, language));
			return;
		}

		Download[] downloadsArray = downloads.getDownloads();
		@SuppressWarnings("unchecked")
		Map<String, String> indexMap = (task.getObject(param_EXTRAINFO) != null ? (Map<String, String>) task
				.getObject(param_EXTRAINFO) : new HashMap<String, String>());
		String smsHeaderText = getSMSTextForID(task, DCT_MANAGE_MSSG_HEADER,
				null, subscriber.getLanguage());
		String smsFooterText = getSMSTextForID(task, DCT_MANAGE_MSSG_FOOTER,
				null, subscriber.getLanguage());
		String clipTextFormat = getSMSTextForID(task, DCT_MANAGE_BASE_TEXT,
				m_dctManageBaseText, subscriber.getLanguage());
		String clipMoreText = getSMSTextForID(task, DCT_MANAGE_MORE_TEXT,
				m_dctManageMoreText, subscriber.getLanguage());
		int maxClipsAllowed = Integer.valueOf(getParamAsString(param_sms,
				DCT_MANAGE_REQUEST_MAX_CLIPS_IN_LIST, "3"));
		int songMaxChar = Integer.valueOf(getParamAsString(param_sms,
				DCT_MANAGE_SONG_MAX_CHAR_ALLOWED, "15"));
		int artistMaxChar = Integer.valueOf(getParamAsString(param_sms,
				DCT_MANAGE_ARTIST_MAX_CHAR_ALLOWED, "10"));
		String optionPrefix = getParamAsString(param_sms,
				DCT_SONG_OPTION_PREFIX, null);
		if (optionPrefix == null) {
			logger.info("DCT_SONG_OPTION_PREFIX is not configured");
			optionPrefix = "";
		}
		optionPrefix = optionPrefix.toUpperCase();
		String alphabet;
		StringBuilder smsBuilder = new StringBuilder();
		int activeDownloadsCount = 0;
		for (Download download : downloadsArray) {
			if (download != null
					&& download.getDownloadStatus().equalsIgnoreCase(
							WebServiceConstants.ACTIVE))
				activeDownloadsCount++;
		}
		HashMap<String, String> extraInfoMap = subscriber.getUserInfoMap();
		String actPrompt = null;
		Date azaanNextBillDate = null;
		String azaanCosType = null;
		if (extraInfoMap != null && extraInfoMap.containsKey(PACK)) {
			String circleID = subscriber.getCircleID();
			String[] cosIds = extraInfoMap.get(PACK).split(",");

			for (String cosID : cosIds) {
				CosDetails cosDetails = (CosDetails) CacheManagerUtil
						.getCosDetailsCacheManager().getActiveCosDetail(cosID,
								circleID);
				if (cosDetails != null
						&& (RBTParametersUtils.getParamAsString(SMS,
								"SMS_AZAAN_COS_TYPES", "AZAAN")
								.equalsIgnoreCase(cosDetails.getCosType()))) {
					List<ProvisioningRequests> response = ProvisioningRequestsDao
							.getBySubscriberIDTypeAndNonDeactivatedStatus(
									subscriber.getSubscriberID(),
									Integer.parseInt(cosID));
					if (response != null && !response.isEmpty()) {
						for (ProvisioningRequests provreq : response) {
							if (provreq.getStatus() == iRBTConstant.PACK_ACTIVATED) {
								isAzaanPackExists = true;
								azaanRefId = provreq.getTransId();
								azaanCosId = cosID;
								azaanCosType = cosDetails.getCosType();
								actPrompt = cosDetails.getActivationPrompt();
								break;
							}
						}
					}
				}
			}
		}
		int maxClipsAllowedInPage = maxClipsAllowed;
		for (int i = accessCount; i <= downloadsArray.length
				&& maxClipsAllowed > 0; i++) {
			Download download = downloadsArray[i - 1];
			logger.info("download is " + download);
			String downloadStatus = download.getDownloadStatus();
			if (!downloadStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE))
				continue;

			String downloadWavFile = download.getRbtFile();
			if (downloadWavFile == null)
				continue;
			if (downloadWavFile.endsWith(".wav"))
				downloadWavFile = downloadWavFile.substring(0,
						downloadWavFile.length() - 4);
			Clip downloadClip = rbtCacheManager.getClipByRbtWavFileName(
					downloadWavFile, language);
			if (downloadClip == null)
				continue;
			String clipName = downloadClip.getClipName();
			if (clipName.length() > songMaxChar)
				clipName = clipName.substring(0, songMaxChar);
			alphabet = optionPrefix + accessCount;
			String clipText = clipTextFormat;
			clipText = clipText.replace("%ALPHABET%", alphabet);
			clipText = clipText.replace("%SONG_NAME%", clipName);

			String artistName = downloadClip.getArtist() != null ? downloadClip
					.getArtist() : "";
			if (artistName.length() > artistMaxChar)
				artistName = artistName.substring(0, artistMaxChar);
			clipText = clipText.replace("%ARTIST_NAME%", artistName);
			String promoID = downloadClip.getClipPromoId() != null ? downloadClip
					.getClipPromoId() : "";
			clipText = clipText.replace("%PROMO_ID%", promoID);
			String nextBillingDate = new SimpleDateFormat(dateFormat)
					.format(download.getNextBillingDate());
			clipText = clipText.replace("%RENEWAL_DATE%", nextBillingDate);

			smsBuilder.append(clipText);
			indexMap.put(alphabet, String.valueOf(downloadClip.getClipId()));
			accessCount++;
			maxClipsAllowed--;
		}
		String finalSMSText = "";
		if (smsHeaderText != null) {
			finalSMSText = smsHeaderText;
		}

		finalSMSText = finalSMSText + smsBuilder.toString();

		if (activeDownloadsCount >= accessCount
				|| (((activeDownloadsCount == maxClipsAllowedInPage)
						&& isAzaanPackExists && maxClipsAllowed == 0))) {
			clipMoreText = clipMoreText.replace("%ALPHABET%", optionPrefix
					+ "0");
			finalSMSText = finalSMSText + clipMoreText;
			indexMap.put(optionPrefix + "0", String.valueOf(accessCount));
		} else {
			if (isAzaanPackExists) {
				RbtDetailsRequest rbtPackRequest = new RbtDetailsRequest(
						subscriber.getSubscriberID());
				// rbtPackRequest.setMode("CCC");
				Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
						rbtPackRequest);
				SubscriberPack[] subscriberPacksArr = rbt.getSubscriberPacks();
				List<SubscriberPack> subscriberPacks = Arrays
						.asList(subscriberPacksArr);
				for (SubscriberPack subPack : subscriberPacks) {
					if (subPack.getCosId().equalsIgnoreCase(azaanCosId)
							&& subPack.getCosType().equalsIgnoreCase(
									azaanCosType)
							&& subPack.getIntRefId().equalsIgnoreCase(
									azaanRefId)) {
						azaanNextBillDate = subPack.getNextChargingDate();
					}
				}
				indexMap.put(optionPrefix + accessCount, "AZAAN:"
						+ (azaanRefId != null ? azaanRefId : ""));
				String azaanBillDate = azaanNextBillDate != null ? new SimpleDateFormat(
						dateFormat).format(azaanNextBillDate) : "";
				finalSMSText = finalSMSText + " " + optionPrefix
						+ (accessCount) + ":" + actPrompt + " " + azaanBillDate;
			}
		}
		if (smsFooterText != null) {
			finalSMSText += smsFooterText;
		}

		task.setObject(param_sms_type, SMS_DCT_SONG_MANAGE);
		task.setObject(param_responseSms, finalSMSText);
		task.setObject(param_info, WebServiceConstants.VIRAL_DATA);
		task.setObject(param_SEARCHCOUNT, String.valueOf(accessCount));
		removeViraldata(subscriber.getSubscriberID(), SMS_DCT_SONG_MANAGE);
		removeViraldata(subscriber.getSubscriberID(), null,
				SMS_DCT_SONG_CONFIRM);
		logger.info("indexMap size :" + indexMap.size());
		logger.info("extra info after DBUtility.getAttributeXMLFromMap(indexMap) : "
				+ DBUtility.getAttributeXMLFromMap(indexMap));
		task.setObject(param_EXTRAINFO,
				DBUtility.getAttributeXMLFromMap(indexMap));
		logger.info("the updated viraldata is inserted into viraltable");
		addViraldata(task);
		return;
	}

	private ViralData insertViralData(Subscriber subscriber, String callerID,
			String keyword, String dactType) {
		String dctType = null;
		if (dactType != null && !dactType.isEmpty()) {
			if (dactType.contains("OFF")) {
				dctType = "SUBSCRIBER_DC_CONFIRM_PENDING";
			}
			if (dactType.contains("DEL")) {
				dctType = "DOWNLOAD_DC_CONFIRM_PENDING";
			}
			if (dactType.contains("UNSUB")) {
				dctType = "AZAAN_DC_CONFIRM_PENDING";
			}
		}
		DataRequest dataRequest = new DataRequest(subscriber.getSubscriberID(),
				callerID, dctType, keyword, new Date(), "SMS");
		ViralData viralData = rbtClient.addViralData(dataRequest);
		return viralData;
	}

	protected static ViralData addViraldata(Task task) {
		int count = 0;
		DataRequest viraldataRequest = new DataRequest(
				task.getString(param_callerid), task.getString(param_SMSTYPE));
		viraldataRequest.setSubscriberID(task.getString(param_subscriberID));
		viraldataRequest.setClipID(task.getString(param_CLIPID));
		if (task.getString(param_SEARCHCOUNT) != null)
			count = Integer.parseInt(task.getString(param_SEARCHCOUNT));
		viraldataRequest.setCount(count);
		viraldataRequest.setSentTime((Date) task.getObject(param_DATE));
		viraldataRequest.setMode(task.getString(param_SELECTED_BY));
		HashMap<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(task.getString(param_EXTRAINFO));
		viraldataRequest.setInfoMap(extraInfoMap);
		return rbtClient.addViralData(viraldataRequest);
	}

	public int getDownloadCategoryId(String subscriberId, String rbtWavFile) {
		int categoryId = -1;
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberId);
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
				rbtDetailsRequest);
		Library library = rbt == null ? null : rbt.getLibrary();
		Downloads downloads = library == null ? null : library.getDownloads();
		if (downloads != null && downloads.getNoOfActiveDownloads() > 0) {
			for (Download download : downloads.getDownloads()) {
				if (download.getRbtFile().replace(".wav", "")
						.equals(rbtWavFile)) {
					categoryId = download.getCategoryID();
					break;
				}
			}
		}
		return categoryId;
	}
}