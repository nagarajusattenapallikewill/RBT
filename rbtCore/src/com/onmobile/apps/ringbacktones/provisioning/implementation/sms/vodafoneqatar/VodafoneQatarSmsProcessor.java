package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.vodafoneqatar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VodafoneQatarSmsProcessor extends SmsProcessor {

	public VodafoneQatarSmsProcessor() throws RBTException {
	}

	@SuppressWarnings("unchecked")
	public void processDeactivateDownload(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String callerID = subscriber.getSubscriberID();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		boolean isCheckCategorySmsAlias = param(SMS, CHECK_CATEGORY_SMS_ALIAS,
				false);
		boolean makeEntryForViralDCT = false;
		boolean proceedDownloadDCT = false;
		boolean doubleConfirmationDCT = false;
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		String subscriberId = subscriber.getSubscriberID();
		Date nextBillingDate = null;
		logger.debug("Processing deactive download request for subscriber: "
				+ subscriberId + ", keywords: " + smsList);

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

		// /Check the promoid is available in viral sms table.
		task.setObject(param_SMSTYPE, "DOWNLOAD_DC_CONFIRM_PENDING");
		ViralData context[] = getViraldata(task);
		boolean isDownloadsModel = RBTParametersUtils.getParamAsBoolean(
				"COMMON", "ADD_TO_DOWNLOADS", "FALSE");
		String classType = "";
		int noOfSongs = 0;
		int amount = -1;
		boolean isPromoCodeExistsInViral = false;
		boolean isSongPresent = false;
		// based on download or selection model it will check the selection is
		// exists or not.
		// it is having a active downloads so get no of song for that user.
		// Based on download or selection model
		String language = subscriber.getLanguage();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(callerID);
		rbtDetailsRequest.setMode("CCC");
		rbtDetailsRequest.setInfo("subscriber,downloads");
		Rbt rbt = RBTClient.getInstance().getRBTUserInformation(
				rbtDetailsRequest);

		Library library = rbt == null ? null : rbt.getLibrary();
		Downloads downloads = library == null ? null : library.getDownloads();

		if (downloads == null || downloads.getDownloads() == null
				|| downloads.getNoOfActiveDownloads() == 0) {
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
						classType = download.getChargeClass();
						isSongPresent = true;
						break;
					}
				}
			}
		}
		if (selectedDownload == null || !isSongPresent) {
			// no active downloads.send the sms.
			logger.info("selected download is not found in the downloads list");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, SELECTED_SONG_NOT_IN_DOWNLOAD,
							m_nopromoIdDefault, language));
			return;
		}

		// If the viral table is having the data's then that need to be
		// validated whether the promoId is already present there or not.
		ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager()
				.getChargeClass(classType);
		// get the amount to identify the song as free song or not.
		amount = (chargeClass.getAmount() != null && !chargeClass.getAmount()
				.equalsIgnoreCase("")) ? Integer.parseInt(chargeClass
				.getAmount()) : -1;
		if (context != null && context.length > 0) {
			Date viralDataSmsTime = null;
			// Logic for checking the viral entry for the same promotion Id.
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
			// If the promotion id matches that need to be validated in the
			// for expired or not.if the content is expired then it
			// will be removed the old entry.
			if (isPromoCodeExistsInViral) {
				long configuredTime = RBTParametersUtils.getParamAsLong(
						"COMMON", "DOWNLOAD_DEACTIVATION_TIME", 60);
				long smsSendTime = new Date().getTime();
				if (null != viralDataSmsTime) {
					smsSendTime = viralDataSmsTime.getTime();
				}
				// Validate the time difference in minutes for viral entry and
				// current time.
				Date sysDate = new Date(System.currentTimeMillis());
				long sentTimeAfterDuration = smsSendTime
						+ (configuredTime * 60 * 1000L);
				if (System.currentTimeMillis() < sentTimeAfterDuration) {
					doubleConfirmationDCT = true;
				} else {
					logger.info("download deactivation entry is present in the viral sms table and it is expired.");
					makeEntryForViralDCT = true;
				}
				removeViraldata(subscriber.getSubscriberID(), callerID,
						"DOWNLOAD_DC_CONFIRM_PENDING");
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
			if (noOfSongs > 1) {
				logger.info("noOfSongs is " + noOfSongs + " amount: " + amount
						+ " daysAllowed " + daysAllowed + " daysLeft "
						+ daysLeft);
				if (amount == 0
						|| (amount != 0 && ((daysLeft <= daysAllowed) || doubleConfirmationDCT))) {
					proceedDownloadDCT = true;
				} else {
					ViralData viralData = insertViralData(subscriber, callerID,
							keyword);
					if (viralData == null) {
						// failed to insert into viral sms table.
						smsText = getSMSTextForID(task,
								DOWNLOAD_DEACT_VIRAL_FAILURE,
								m_technicalFailuresDefault,
								subscriber.getLanguage());
						hashMap.put("SMS_TEXT", smsText);
						smsText = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsText);
						return;
					} else {
						// Ask for double confirmation sms.
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
				if (amount == 0) {
					nextBillingDate = subscriber.getNextBillingDate();
					if (nextBillingDate != null) {
						daysLeft = (nextBillingDate.getTime() - System
								.currentTimeMillis()) / (1000 * 24 * 60 * 60);
					}
					hashMap.put("DAYS_LEFT", String.valueOf(daysLeft));
					hashMap.put("DEACT_CONFIRM_DAYS",
							String.valueOf(daysAllowed));
					logger.info("noOfSongs is " + noOfSongs + " amount: "
							+ amount + " daysAllowed " + daysAllowed
							+ " daysLeft " + daysLeft);
					if (daysLeft <= daysAllowed) {
						proceedDownloadDCT = true;
					} else {
						ViralData viralData = insertViralData(subscriber,
								callerID, keyword);
						if (viralData == null) {
							// failed to insert into viral sms table.
							smsText = getSMSTextForID(task,
									DOWNLOAD_DEACT_VIRAL_FAILURE,
									m_technicalFailuresDefault,
									subscriber.getLanguage());
							hashMap.put("SMS_TEXT", smsText);
							smsText = finalizeSmsText(hashMap);
							task.setObject(param_responseSms, smsText);
							return;
						} else {
							// Ask for double confirmation sms.
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
		}
		contentName = "";
		smsText = null;
		if (proceedDownloadDCT || doubleConfirmationDCT) {
			if (clip != null)
				task.setObject(param_clipid, "" + clip.getClipId());
			if (category != null)
				task.setObject(param_catid, "" + category.getCategoryId());
			if (callerID == null)
				callerID = "ALL";

			// deactivate selection
			String deactivateSelResponse = processDeactivateSelection(task);
			String deleteSubscriberDownload = deleteSubscriberDownload(task);
			logger.info("deactivate selection Response: "
					+ deactivateSelResponse
					+ ", delete subscriber download response: "
					+ deleteSubscriberDownload);

			if (callerID.equalsIgnoreCase("ALL")) {
				callerID = param(SMS, SMS_TEXT_FOR_ALL, "ALL");
			}

			if (clip != null)
				contentName = clip.getClipName();
			else if (category != null)
				contentName = category.getCategoryName();

			if (deleteSubscriberDownload
					.equalsIgnoreCase(WebServiceConstants.SUCCESS))
				smsText = getSMSTextForID(task, DOWNLOAD_DEACT_SUCCESS,
						m_downloadDeactSuccessDefault, subscriber.getLanguage());
			else
				smsText = getSMSTextForID(task, DOWNLOAD_DEACT_FAILURE,
						m_downloadDeactFailureDefault, subscriber.getLanguage());
		}
		hashMap.put("CALLER_ID", callerID);
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", contentName);
		hashMap.put("CIRCLE_ID", subscriber.getCircleID());
		hashMap.put("PROMO_ID", keyword);
		task.setObject(param_responseSms, finalizeSmsText(hashMap));
	}

	private ViralData insertViralData(Subscriber subscriber, String callerID,
			String keyword) {
		DataRequest dataRequest = new DataRequest(callerID,
				subscriber.getSubscriberID(), "DOWNLOAD_DC_CONFIRM_PENDING",
				keyword, new Date(), "SMS");
		ViralData viralData = rbtClient.addViralData(dataRequest);
		return viralData;
	}
}
