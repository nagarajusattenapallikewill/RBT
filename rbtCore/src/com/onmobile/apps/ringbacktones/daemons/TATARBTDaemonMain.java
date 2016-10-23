package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.webservice.implementation.tata.TataUtility;
import com.onmobile.common.exception.OnMobileException;

class TATARBTDaemonMain implements iRBTConstant {
	private static Logger logger = Logger.getLogger(TATARBTDaemonMain.class);
	protected static final String app = "TATARBTDaemon";
	private static TATARBTDaemonMain tatarbtDaemonMain = null;
	private String db_url = null;
	protected String m_smsNo = null;

	private boolean sendSMS = true;
														
	private String activationPendingSMS = null;
	/* Used in 1+1 feature, sent while downloading the charged version of the free song */

	protected boolean insertSMSInDuplicate = true;

	protected String daemonQueriesLogPath = "";
	protected int rotationSize = 8000;

	protected String reportpath = "E:\\";

	protected static SimpleDateFormat fileNameFormatter = new SimpleDateFormat("MMddHHmmssSSS");
	protected static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	protected static SimpleDateFormat endDateformatter = new SimpleDateFormat("yyyyMMdd");
	protected static SimpleDateFormat onlyDayformatter = new SimpleDateFormat("yyyyMMdd");

	private Map<String,List<String>> mapMusicChannelsCategoryTypes = new HashMap<String,List<String>>();

//	private long graceRetryMaxHours = 360;
//	private int graceRetryIntervalHours = 24;
	
	private ArrayList<String> graceIntervalHours = new ArrayList<String>();

	protected static ParametersCacheManager parameterCacheManager = null;
	
	private TATARBTDaemonMain() {
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = null;
		try {
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DB_URL", null);
			if (parameter != null) {
				db_url = parameter.getValue();
				// Changes done for URL Encryption and Decryption
				ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
				try {
					if (resourceBundle.getString("ENCRYPTION_MODEL") != null
							&& resourceBundle.getString("ENCRYPTION_MODEL")
									.equalsIgnoreCase("yes")) {
						db_url = URLEncryptDecryptUtil.decryptAndMerge(db_url);
					}
				} catch (MissingResourceException e) {
					logger.error("resource bundle exception: ENCRYPTION_MODEL");
				}
				// End of URL Encryption and Decryption
			}
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_NUMBER", "");
			m_smsNo = parameter.getValue();

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_SMS", "");
			String sendSMSString = parameter.getValue();
			if(sendSMSString.equalsIgnoreCase("TRUE"))
				sendSMS = true;
			else
				sendSMS = false;

			parameter = parameterCacheManager.getParameter(iRBTConstant.COMMON, "QUERIED_INTERFACES_DAEMON_LOG_PATH", "");
			if (parameter != null) 
				daemonQueriesLogPath = parameter.getValue();
						
			parameter = parameterCacheManager.getParameter(iRBTConstant.COMMON, "ROTATION_SIZE", "8000");
			rotationSize = Integer.parseInt(parameter.getValue());
			
			parameter = parameterCacheManager.getParameter(iRBTConstant.COMMON, "REPORT_PATH", reportpath);
			reportpath = parameter.getValue();
			
//			graceRetryMaxHours = tataRBTDaemonConfig.graceRetryMaxHours();
//			graceRetryIntervalHours = tataRBTDaemonConfig.graceRetryIntervalHours();
			
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "GRACE_RETRY_INTERVAL_HOURS", "");
			String valueStr = parameter.getValue();
			graceIntervalHours = new ArrayList<String>();
			StringTokenizer stk = new StringTokenizer(valueStr, ",");
			while(stk.hasMoreTokens()) {
				String token = stk.nextToken();
				String hours = token;
				int index = hours.indexOf("x");
				if(index != -1) {
					hours = token.substring(0, index);
					int count = Integer.parseInt(token.substring(index + 1));
					for(int i = 0; i < count; i++)
						graceIntervalHours.add(hours);
				}
				else
					graceIntervalHours.add(hours);
			}
			
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "MUSIC_CHANNEL_CATEGORY_TYPES", null);
			if(parameter != null) {
				List<String> list = Arrays.asList(parameter.getValue().split("\\;"));
				for(String temp : list) {
					String[] arr = temp.split("\\:");
					mapMusicChannelsCategoryTypes.put(arr[0], Arrays.asList(arr[1].split("\\,")));
				}
			}
			
		}
		catch(Exception e) {
			logger.error("", e);
		}
	}

	public static synchronized TATARBTDaemonMain getInstance() throws MissingResourceException {
		logger.info("RBT::entering");
		if(tatarbtDaemonMain == null)
			tatarbtDaemonMain = new TATARBTDaemonMain();
		return tatarbtDaemonMain;
	}
	
	/**
	 * This methos sends combined charging request for activation and download (both song and musicbox). This method is
	 * also used only to download song/musicbox
	 * 
	 * @param subscriber
	 * @param pendingSelection
	 * @param eventType
	 * @return
	 */
	public String combinedRequest(Subscriber subscriber, SubscriberStatus pendingSelection, String eventType) {
		try {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());

			String subscriberId = subscriber.subID();
			String toneCode = pendingSelection.subscriberFile();
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "RENEWAL_FAILURE_SMS",
					"");

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS_TO_RETAILER",
					"");
			String selectionFailedSMSToRetailer = parameter.getValue();
			boolean isOnePlusOneClip = pendingSelection.selectionInfo().indexOf("1+1") != -1;

			StringBuilder combinedUrl = new StringBuilder(parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,
					"HTTP_LINK", "").getValue());

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "COMBINED_CHARGING_PAGE", "");

			combinedUrl.append(parameter.getValue());
			combinedUrl.append(TataUtility.getOperatorAccount(cos) + "&");
			combinedUrl.append(TataUtility.getOperatorPassword(cos) + "&");
			combinedUrl.append(TataUtility.getOperatorCode(cos) + "&");
			combinedUrl.append("phonenumber=" + subscriber.subID());

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ALLOW_COS", "FALSE");
			String allowCosString = parameter.getValue();
			boolean allowCos = false;
			if (allowCosString != null && allowCosString.equalsIgnoreCase("TRUE"))
				allowCos = true;

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ALLOW_DEFAULT_COS", "FALSE");
			String allowDefaultCosString = parameter.getValue();
			boolean allowDefaultCos;
			if (allowDefaultCosString != null && allowDefaultCosString.equalsIgnoreCase("TRUE"))
				allowDefaultCos = true;
			else
				allowDefaultCos = false;
			if ((allowCos && !cos.isDefaultCos()) || (allowCos && allowDefaultCos && cos.isDefaultCos()))
				combinedUrl.append("&cosid=" + subscriber.cosID());

			combinedUrl.append("&tonecode=" + pendingSelection.subscriberFile());

			int contentType = 0;
			if (pendingSelection.categoryType() == BOUQUET)
				contentType = 1;
			
			Set<String> setMusicChannelsContentTypes = mapMusicChannelsCategoryTypes.keySet();
			for(String strContentType : setMusicChannelsContentTypes) {
				List<String> list = mapMusicChannelsCategoryTypes.get(strContentType);
				if(list.contains(pendingSelection.categoryType()+"")) {
					try {
						contentType = Integer.parseInt(strContentType);
					}
					catch(NumberFormatException e) {
						logger.error("Number format Exception please check configuration MUSIC_CHANNEL_CATEGORY_TYPES. Configuration should contains only integer values");
					}
					break;
				}
			}
			
			combinedUrl.append("&contenttype=" + contentType);

			combinedUrl.append("&starttime=" + formatter.format(new Date()));
			combinedUrl.append("&endtime=" + formatter.format(pendingSelection.endTime()));

			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(
					pendingSelection.classType());
			logger.info(" ChargeClass:" + chargeClass + " to append songprice and song validity ");
			if (null != chargeClass) {
				String songPrice = chargeClass.getAmount();
				String songValidity = convertValidityPeriodToDays(chargeClass.getSelectionPeriod());
				combinedUrl.append("&songprice=" + songPrice);
				combinedUrl.append("&songvalidity=" + songValidity);
			}

			if (pendingSelection.selectionInfo() != null) {
				String info = pendingSelection.selectionInfo();
				int index = info.indexOf("|");
				if (index != -1)
					info = info.substring(0, index);
				combinedUrl.append("&mmno=" + info);
			}

			String sCombinedUrl = combinedUrl.toString().toString();
			logger.info("Combined url for event type - " + eventType + " is " + sCombinedUrl);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(sCombinedUrl, subscriber.subID(), app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if (result != null)
				result = result.trim();

			if ((result == null || !result.equals("12")) && pendingSelection.selStatus().equals(STATE_GRACE))
				writeGraceSDR(eventType, subscriber.subID(), eventType + ":" + pendingSelection.subscriberFile(),
						result);

			String userType = subscriber.prepaidYes() ? "PRE_PAID" : "POST_PAID";

			if (result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						"download_tone", "null_error_response", requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);
				return "null_error_response";
			}

			// taking the first token if the response is of format 0|Price
			result = result.split("\\|")[0];
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);
			boolean isSelectionBulkActivated = isSelectionBulkActivated(pendingSelection);

			if (result.equals("0") || result.equals("15")) {
				String sdrResponse = "success";
				if (result.equals("15"))
					sdrResponse = "already_downloaded";

				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), sdrResponse, requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);
				// if subscriber status is A, updating same to B
				if (subscriber.subYes().equals(STATE_TO_BE_ACTIVATED)) {
					Calendar nextChargingDate = Calendar.getInstance();
					nextChargingDate.set(2035, 11, 31, 0, 0, 0);

					String type = "B";
					if (subscriber.prepaidYes())
						type = "P";

					rbtDBManager.smSubscriptionSuccess(subscriber.subID(), nextChargingDate.getTime(), new Date(),
							type, subscriber.subscriptionClass(), true, cos, subscriber.rbtType());
				}

				// validity is being sent as 365 as validity is not being used
				rbtDBManager.updateDownloadStatusToDownloaded(subscriber.subID(), pendingSelection.subscriberFile(),
						new Date(), 365);
				// add setting for the above selection as the download is successful
				// no need to worry about the same record being picked up by other thread, as this has just been updated
				/* 
				 * This has been commented because addsetting.do is being called twice because of this 
				 * 
				 * addClipForSubscriber(pendingSelection);*/

				return sdrResponse;
			}
			else if (result.equals("99") || result.equals("5") || result.equals("7") || result.equals("16")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), "provisional_success", requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);

				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_PENDING_SMS", "");
				String sendPendingSMSString = parameter.getValue();
				boolean sendPendingSMS;
				if (sendPendingSMSString.equalsIgnoreCase("TRUE"))
					sendPendingSMS = true;
				else
					sendPendingSMS = false;
				if (sendPendingSMS && sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriber.subID(), activationPendingSMS, insertSMSInDuplicate);

				// updating subscriber table only if subscription status is A
				if (subscriber.subYes().equals(STATE_TO_BE_ACTIVATED))
					rbtDBManager.smURLSubscription(subscriber.subID(), true, false, null);
				// updating downloads table
				rbtDBManager.updateDownloadStatus(pendingSelection.subID(), pendingSelection.subscriberFile(), 'p');
				return "activation-pending";
			}
			else if (result.equals("13") || result.equals("17")) {
				String sdrResponse = "song_doesnt_exist";
				if (result.equals("17"))
					sdrResponse = "disabled_in_user's_cos";

				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), sdrResponse, requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);

				deleteSubscriberSelection(subscriberId, toneCode, null, true, sdrResponse, true,
						pendingSelection.categoryID(), pendingSelection.prepaidYes(), true);

				decrementSubMaxSelection(subscriber, 1);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "song_not_in_huawei",
						pendingSelection.startTime(), null, pendingSelection.callerID(), toneCode);

				Clips clip = rbtDBManager.getClipByPromoID(pendingSelection.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
				String selectionFailedSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", clip.name());
				if (sendSMS && !isOnePlusOneClip && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS, insertSMSInDuplicate);
				if (isSelectionBulkActivated && subscriber.rbtType() != 10) {
					WriteSDR.addToAccounting(reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, eventType.toLowerCase(), sdrResponse,
							requestedTimeString, differenceTime + "", app, sCombinedUrl, result);
				}
				if (pendingSelection.selectedBy().indexOf(RETAILER_STRING) != -1 && !isOnePlusOneClip)
					sendRetailerSMS(pendingSelection.selectionInfo(), subscriberId, selectionFailedSMSToRetailer,
							clip.name(), null, null);
				return sdrResponse;
			}
			else if (result.equals("14")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), "user_library_full", requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);
				logger.info("RBT::deleting the song " + toneCode + " for subscriber " + subscriberId
						+ " from database, as the user's personal library is full");
				deleteSubscriberSelection(subscriberId, toneCode, null, true, "user_library_full", false, -1,
						pendingSelection.prepaidYes(), true);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_library_full",
						pendingSelection.startTime(), null, pendingSelection.callerID(), toneCode);

				decrementSubMaxSelection(subscriber, 1);

				Clips clip = rbtDBManager.getClipByPromoID(pendingSelection.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "CLIP_LIBRARY_FULL_SMS", "");
				String clipLibraryFullSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(clipLibraryFullSMS, "%TUNE%", clip.name());
				if (sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS, insertSMSInDuplicate);
				if (isSelectionBulkActivated && subscriber.rbtType() != 10) {
					WriteSDR.addToAccounting(reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tone", "user_library_full",
							requestedTimeString, differenceTime + "", app, sCombinedUrl, result);
				}
				if (pendingSelection.selectedBy().indexOf(RETAILER_STRING) != -1 & !isOnePlusOneClip)
					sendRetailerSMS(pendingSelection.selectionInfo(), subscriberId, selectionFailedSMSToRetailer,
							clip.name(), null, null);
				return "user_library_full";
			}
			// this response is valid only incase of download, so no need to check for subscriber
			else if (result.equals("18")) {
				String sdrResponse = "insufficient_balance";
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), sdrResponse, requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);

				logger.info("Selection status is " + pendingSelection.selStatus()
						+ " for insufficient balance response");
				if (pendingSelection.selStatus().equals(STATE_GRACE)
						|| pendingSelection.selStatus().equals(STATE_TO_BE_ACTIVATED)) {
					return processSelectionGrace(subscriber, pendingSelection, result, requestedTimeString,
							differenceTime, sCombinedUrl);
				}
				return "invalid_grace_response";
			}
			else if (result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriberId, userType,
						eventType.toLowerCase(), "user_not_valid", requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);
				logger.info("RBT::user is not registered user deleting from database");
				RBTDBManager.getInstance().deactivateSubscriberForTATA(subscriberId);
				return "user_not_valid";
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType, subscriber.subID(), userType,
						eventType.toLowerCase(), "response_not_handled", requestedTimeString, differenceTime + "", app,
						sCombinedUrl, result);
				return "response_not_handled";
			}
		}
		catch (Throwable t) {
			logger.error(t.getMessage(), t);
			return "exception:" + t.getMessage();
		}
	}
	
	private String processSelectionGrace(Subscriber subscriber, SubscriberStatus selection, String result,
			String requestedTimeString, long differenceTime, String sCombinedUrl) throws OnMobileException {
		String eventType = "RBT_DOWNLOAD_TONE";
		String subscriberId = selection.subID();
		String toneCode = selection.subscriberFile();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		boolean isSelectionBulkActivated = isSelectionBulkActivated(selection);
		boolean isOnePlusOneClip = selection.selectionInfo().indexOf("1+1") != -1;
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON,
				"SELECTION_FAILED_SMS_TO_RETAILER", "");
		String selectionFailedSMSToRetailer = parameter.getValue();
		String userType = subscriber.prepaidYes() ? "PRE_PAID" : "POST_PAID";

		int index = getNextIndex(null, selection);
		if (index == -1) {
			writeGraceSDR(eventType, selection.subID(), "download:" + toneCode, result + " - deactivating");
			deleteSubscriberSelection(subscriberId, toneCode, null, true, "insufficient_balance", false,
					selection.categoryID(), selection.prepaidYes(), true);

			decrementSubMaxSelection(subscriber, 1);

			Tools.writeSelectionToSDRFile(subscriberId, userType, "insufficient_balance", selection.startTime(), null,
					selection.callerID(), toneCode);

			Clips clip = rbtDBManager.getClipByPromoID(selection.subscriberFile());
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
			String selectionFailedSMS = parameter.getValue();
			String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", clip.name());
			if (sendSMS && !isOnePlusOneClip && !isSelectionBulkActivated && subscriber.rbtType() != 10)
				Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS, insertSMSInDuplicate);
			if (isSelectionBulkActivated && subscriber.rbtType() != 10) {
				WriteSDR.addToAccounting(reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
						eventType, subscriberId, userType, eventType.toLowerCase(), "insufficient_balance",
						requestedTimeString, differenceTime + "", app, sCombinedUrl, result);
			}
			if (selection.selectedBy().indexOf(RETAILER_STRING) != -1 && !isOnePlusOneClip)
				sendRetailerSMS(selection.selectionInfo(), subscriberId, selectionFailedSMSToRetailer, clip.name(),
						null, null);
			return "selection-failed";
		}
		else {
			Calendar nextRetryCal = Calendar.getInstance();
			Date nextChargingDate = selection.nextChargingDate();
			if (nextChargingDate != null)
				nextRetryCal.setTime(nextChargingDate);
			nextRetryCal.add(Calendar.HOUR, Integer.parseInt(graceIntervalHours.get(index)));
			logger.info("RBT::updating user " + subscriberId + " selection to grace");
			writeGraceSDR(eventType, subscriberId, "download:" + toneCode, result + " - moving_to_grace");
			if (!rbtDBManager.updateSelectionToGrace(selection.subID(), selection.callerID(),
					selection.subscriberFile(), nextRetryCal.getTime(), getGraceInfo(selection.selectionInfo(), index)))
				logger.info("RBT::updating subscriber to grace failed");
			return "selection-grace";
		}
	}

	public String activateSubscriber(Subscriber subscriber) {
		String result = null;
		String returnValue = null;
		String httpLink = "";
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		httpLink = parameter.getValue();
		try {
			logger.info("RBT::inside try.......");
			String eventType = "RBT_ACTIVATION";
			if(subscriber.subYes().equals(STATE_GRACE))
				eventType = "RBT_GRACE_ACTIVATION";
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String subscriberId = subscriber.subID();
			String cosID = subscriber.cosID();
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			if(subscriber.activatedBy().equalsIgnoreCase("GIFT")) {
				returnValue = giftService(subscriber, cos);
				return returnValue;
			}
			
			//Added by Sreekar for JIRA RBT-4281
			SubscriberStatus[] pendingSelections = rbtDBManager.getAllActiveSubscriberSettings(subscriberId);
			if(pendingSelections != null && pendingSelections.length > 0) {
				returnValue = combinedRequest(subscriber, pendingSelections[0], eventType);
				return returnValue;
			}

			StringBuilder urlstrBuf = new StringBuilder(httpLink);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPEN_ACCOUNT_PAGE", "");
			String openAccountPage = parameter.getValue();
			urlstrBuf.append(openAccountPage);

			logger.info("RBT::before adding parameters for subscriber " + subscriberId);

			urlstrBuf.append(TataUtility.getOperatorAccount(cos) + "&");
			urlstrBuf.append(TataUtility.getOperatorPassword(cos) + "&");
			urlstrBuf.append("phonenumber=" + subscriberId + "&");
			urlstrBuf.append(TataUtility.getOperatorCode(cos));
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ALLOW_COS", "FALSE");
			String allowCosString = parameter.getValue();
			boolean allowCos;
			if(allowCosString != null && allowCosString.equalsIgnoreCase("TRUE"))
				allowCos = true;
			else
				allowCos = false;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ALLOW_DEFAULT_COS", "FALSE");
			String allowDefaultCosString = parameter.getValue();
			boolean allowDefaultCos;
			if(allowDefaultCosString != null && allowDefaultCosString.equalsIgnoreCase("TRUE"))
				allowDefaultCos = true;
			else
				allowDefaultCos = false;
			if((allowCos && !cos.isDefaultCos()) || (allowCos && allowDefaultCos && cos.isDefaultCos()))
				urlstrBuf.append("&cosid=" + cosID);
			
			//appending mmno RBT-3949
			if(subscriber.activationInfo() != null)
				urlstrBuf.append("&mmno=" + subscriber.activationInfo());
			
			String urlstr = urlstrBuf.toString();

			logger.info("RBT::after adding parameters(before sending http request) for subscriber "
							+ subscriberId);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
				result = result.trim();
			
			if((result == null || !result.equals("15")) && subscriber.subYes().equalsIgnoreCase(STATE_GRACE))
				writeGraceSDR(eventType, subscriberId, "activation", result);

			logger.info("RBT::activation result for subscriber "
					+ subscriberId + " is:: " + result);

			if(result == null) {
				//Response = null_error_response
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::returning null from activateSubscriber for the subscriber "
								+ subscriberId);
			}
			else if(result.equals("13") || result.equals("99") || result.equals("5")) {
				//Response = provisional_success
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "provisional_success",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user "
										+ subscriberId
										+ " is been presented with the Coloring service and is under process, updating rbt_subscriber table as pending");
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_PENDING_SMS", "");
				String sendPendingSMSString = parameter.getValue();
				boolean sendPendingSMS;
				if(sendPendingSMSString.equalsIgnoreCase("TRUE"))
					sendPendingSMS = true;
				else
					sendPendingSMS = false;
				if(sendPendingSMS && sendSMS && !isSubscriberBulkActivated
						&& subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, activationPendingSMS,
							insertSMSInDuplicate);
				rbtDBManager.smURLSubscription(subscriberId, true, false, null);
				returnValue = "activation-pending";
			}
			else if(result.equals("4")) {
				//Response = already_subscribed
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "already_subscribed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " is already subscribed, updating rbt_subscriber table");

				Calendar nextChargingDate = Calendar.getInstance();
				nextChargingDate.set(2035, 11, 31, 0, 0, 0);

				String type = "B";
				if(subscriber.prepaidYes())
					type = "P";

				rbtDBManager.smSubscriptionSuccess(subscriberId, nextChargingDate.getTime(),
						new Date(), type, subscriber.subscriptionClass(), true, cos, subscriber
								.rbtType());

				Tools.writeSubscriberToSDRFile(subscriberId, userType, "already_subscribed",
						subscriber.startDate(), new Date());
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10) {
					sendActivationSuccessSMS(subscriber, cos);
				}
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_BULK_ACT_WELCOME_SMS", "");
				String sendBulkACTWelcomeSMSString = parameter.getValue();
				boolean sendBulkACTWelcomeSMS;
				if(sendBulkACTWelcomeSMSString.equalsIgnoreCase("TRUE"))
					sendBulkACTWelcomeSMS = true;
				else
					sendBulkACTWelcomeSMS = false;
				if(isSubscriberBulkActivated && sendBulkACTWelcomeSMS && subscriber.rbtType() != 10) {
					addBulkUserToFile(subscriberId, subscriber.activatedBy(), "activated");
				}
				returnValue = "already-subscribed";
			}
			else if(result.equals("3")) {
				//Response = invalid_user_number_activation_failed
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation",
						"invalid_user_number_activation_failed", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid user phone number(user in black list), deleting from database");
				// rbtDBManager.deleteSubscriber(subscriberId);
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
					// insertSMSInDuplicate);
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"invalid_user_number_activation_failed", requestedTimeString,
							differenceTime + "", app, urlstr, result);
				}
				returnValue = "black-listed";
			}
			else if(result.equals("8")) {
				//Response = portal_error_activation_failed
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "portal_error_activation_failed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::got 8 response, considering as negative response");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				// rbtDBManager.insertOurDateDuplicateSub(subscriberId, subscriber.startDate(),
				// "deactivated");//inserting into duplicate subscriber table
				Tools.writeSubscriberToSDRFile(subscriberId, userType,
						"portal_error_activation_failed", subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
					// insertSMSInDuplicate);
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"portal_error_activation_failed", requestedTimeString, differenceTime
									+ "", app, urlstr, result);
				}
				returnValue = "activation-failed";
			}
			else if(result.equals("14")) {
				//Response = invalid_cos_activation_failed
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "invalid_cos_activation_failed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid cos");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				// rbtDBManager.insertOurDateDuplicateSub(subscriberId, subscriber.startDate(),
				// "deactivated");//inserting into duplicate subscriber table
				Tools.writeSubscriberToSDRFile(subscriberId, userType,
						"invalid_cos_activation_failed", subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated)
					// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
					// insertSMSInDuplicate);
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"invalid_cos_activation_failed", requestedTimeString, differenceTime
									+ "", app, urlstr, result);
				}
				returnValue = "activation-failed";
			}
			else if(result.equals("15")) {
				//Response = insufficient_balance
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "insufficient_balance",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::insufficient balance");
				
				int index = getNextIndex(subscriber, null);

				if(index == -1) {
					writeGraceSDR(eventType, subscriberId, "activation", result + " - deactivating");
					rbtDBManager.deactivateSubscriberForTATA(subscriberId);
					Tools.writeSubscriberToSDRFile(subscriberId, userType, "insufficient_balance",
							subscriber.startDate(), null);
					if(sendSMS && !isSubscriberBulkActivated)
						sendActivationFailedSMS(subscriber);
					if(isSubscriberBulkActivated)
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_act_failure", rotationSize, eventType, subscriberId,
								userType, "activation", "insufficient_balance",
								requestedTimeString, differenceTime + "", app, urlstr, result);
					returnValue = "activation-failed";
				}
				else {
					Calendar nextRetryCal = Calendar.getInstance();
					Date nextChargingDate = subscriber.nextChargingDate();
					if(nextChargingDate != null)
						nextRetryCal.setTime(nextChargingDate);
					nextRetryCal.add(Calendar.HOUR, Integer.parseInt(graceIntervalHours.get(index)));
					logger.info("RBT::updating user activation to grace");
					writeGraceSDR(eventType, subscriberId, "activation", result + " - moving_to_grace");
					if(!rbtDBManager.updateSubscriberToGrace(subscriberId, nextRetryCal.getTime(),
							getGraceInfo(subscriber.activationInfo(), index)))
						logger.info("RBT::updating subscriber to grace failed");
					returnValue = "updated-to-grace";
				}
			}

			else if(result.equals("16")) {
				//Response = already_availed_cos
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "already_availed_cos",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::already_availed_cos");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				rbtDBManager.addSubscriberToDeactivatedSubscribersTable(subscriberId, "avl_cos",
						subscriber.activatedCosID());
				// rbtDBManager.insertOurDateDuplicateSub(subscriberId, subscriber.startDate(),
				// "deactivated");//inserting into duplicate subscriber table
				Tools.writeSubscriberToSDRFile(subscriberId, userType, "already_availed_cos",
						subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated) {
					// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
					// insertSMSInDuplicate);
					if(subscriber.rbtType() != 10){
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS_FOR_COS_AVAILED", "");
						String activationFailedSMSForCOSAvailed = parameter.getValue();
						Tools.sendSMS(db_url, m_smsNo, subscriber.subID(),
								activationFailedSMSForCOSAvailed, insertSMSInDuplicate);
					}
					// sending sms to retailer
					if(subscriber.activatedBy().indexOf(RETAILER_STRING) != -1) {
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS_TO_RETAILER", "");
						String activationFailedSMSToRetailer = parameter.getValue();
						String retSMS = getRetailerSubstitutedSMS(activationFailedSMSToRetailer,
								subscriber.subID(), null);
						Tools.sendSMS(db_url, m_smsNo, subscriber.activationInfo(), retSMS,
								insertSMSInDuplicate);
					}
				}
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"already_availed_cos", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "activation-failed";
			}
			else {
				//Response = invalid_response
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "invalid_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " request to activate was not sucessful, will try in the next loop");
				returnValue = "notok";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String checkActivationPendingSubscriber(Subscriber subscriber) {
		String result = null;
		String returnValue = null;
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NO_OF_HOURS_TO_HIT", "");
		int noOfHoursToHit = 1;
		try{
			noOfHoursToHit = Integer.parseInt(parameter.getValue().trim());
		}catch (NumberFormatException e) {
			noOfHoursToHit = 1;
		}
		try {
			String eventType = "RBT_ACTIVATION_POLLING";
			logger.info("RBT::inside try.......");
			String subscriberId = subscriber.subID();
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			{
				Date setDt = subscriber.startDate();
				Date nowDt = new Date();

				long setTimeInMillis = setDt.getTime();
				long nowTimeInMillis = nowDt.getTime();

				long differenceHours = (nowTimeInMillis - setTimeInMillis) / (1000 * 60 * 60);
				logger.info("RBT::differenceHours = " + differenceHours);
				logger.info("RBT::noOfHoursToHit = " + noOfHoursToHit);
				if(differenceHours >= noOfHoursToHit) {
					logger.info("RBT::the request to activate subscriber took more than expected time");
					rbtDBManager.deactivateSubscriberForTATA(subscriberId);
					// rbtDBManager.insertOurDateDuplicateSub(subscriberId, subscriber.startDate(),
					// "exceeded no. of hours");//inserting into duplicate subscriber table
					Tools.writeSubscriberToSDRFile(subscriberId, userType, "exceeded no. of hours",
							subscriber.startDate(), null);
					if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
						// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
						// insertSMSInDuplicate);
						sendActivationFailedSMS(subscriber);
					if(isSubscriberBulkActivated) {
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_act_failure", rotationSize, eventType, subscriberId,
								userType, "activation", "portal_error_activation_failed", formatter
										.format(subscriber.startDate()),
								(nowTimeInMillis - setTimeInMillis) + "", app, "NA", "NA");
					}
					returnValue = "exceed-max-hours";
					return returnValue;
				}
			}

			StringBuilder urlstrBuf = new StringBuilder(httpLink);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SUBSCRIBER_STATUS_PAGE", "");
			String subscriberStatusPage = parameter.getValue();
			urlstrBuf.append(subscriberStatusPage);
			urlstrBuf.append(TataUtility.getOperatorAccount(null) + "&");
			urlstrBuf.append(TataUtility.getOperatorPassword(null) + "&");
			urlstrBuf.append("phonenumber=" + subscriberId + "&");
			urlstrBuf.append(TataUtility.getOperatorCode(null));
			String urlstr = urlstrBuf.toString();
			Date requestedTimeStamp = new Date();
			result = querySubscriberStatus(urlstr, subscriberId);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			logger.info("RBT::activation result for subscriber " + subscriberId + " is:: " + result);

			String subscriberStatus = null;
			if(result != null) {
				result = result.trim();
				if(result.length() <= 2) {
					if(result.equals("9")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "user_is_suspended_activation_failed",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						logger.info("RBT:: "
								+ subscriberId + " is in suspended state");
						rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						// rbtDBManager.insertOurDateDuplicateSub(subscriberId,
						// subscriber.startDate(), "suspended");//inserting into duplicate
						// subscriber table
						Tools
								.writeSubscriberToSDRFile(subscriberId, userType,
										"user_is_suspended_activation_failed", subscriber
												.startDate(), null);
						if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
							// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
							// insertSMSInDuplicate);
							sendActivationFailedSMS(subscriber);
						if(isSubscriberBulkActivated) {
							WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
									+ "_act_failure", rotationSize, eventType,
									subscriberId, userType, "activation_polling",
									"user_is_suspended_activation_failed", requestedTimeString,
									differenceTime + "", app, urlstr, result);
						}
						returnValue = "suspended";
					}
					else if(result.equals("5")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "user_is_blacklisted_activation_failed",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						logger.info("RBT::"
								+ subscriberId + " is a blacklisted user");
						rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						// rbtDBManager.insertOurDateDuplicateSub(subscriberId,
						// subscriber.startDate(), "black-listed");//inserting into duplicate
						// subscriber table
						Tools.writeSubscriberToSDRFile(subscriberId, userType,
								"user_is_blacklisted_activation_failed", subscriber.startDate(),
								null);
						if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BLACK_LISTED_SMS", "");
							String blackListedSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, blackListedSMS,
									insertSMSInDuplicate);
						}
						returnValue = "black-listed";
					}
					else if(result.equals("6")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "gifting_under_processing",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						logger.info("RBT::gifting under processing");
						returnValue = "activation-pending";
					}
					else if(result.equals("7")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "user_is_new_user_activation_failed",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						if(!rbtDBManager.isSubscriberActivationPending(subscriber))
							rbtDBManager.deactivateSubscriberForTATA(subscriberId);
					}
					else if(result.equals("8")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "portal_error", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::portal-error");
						returnValue = "portal-error";
					}
					else if(result.equals("10")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "user_express_copy_pending",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						logger.info("RBT::express-copy-pending");
						returnValue = "express-copy-pending";
					}
					else {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "error", requestedTimeString, differenceTime
										+ "", app, urlstr, result);
						logger.info("RBT::unknown response");
						returnValue = "error";
					}
				}
				else if(result.length() > 2 && result.length() <= 20) {
					StringTokenizer st = new StringTokenizer(result, "|");
					if(st.hasMoreTokens())
						subscriberStatus = st.nextToken();
					String prepaidStatus;
					if(st.hasMoreTokens()) {
						prepaidStatus = st.nextToken();
						if(prepaidStatus.equals("1")) {
							if(subscriber != null && !subscriber.prepaidYes()) {
								rbtDBManager.changeSubscriberType(subscriberId, true);
								subscriber = rbtDBManager.getSubscriber(subscriberId);
							}
							userType = "PRE_PAID";
						}
						else {
							if(subscriber != null && subscriber.prepaidYes()) {
								rbtDBManager.changeSubscriberType(subscriberId, false);
								subscriber = rbtDBManager.getSubscriber(subscriberId);
							}
							userType = "POST_PAID";
						}
					}
					if(subscriberStatus.equals("1") || subscriberStatus.equals("6")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "activation_pending", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::before open state");
						returnValue = "activation-pending";
					}
					else if(subscriberStatus.equals("7") || subscriberStatus.equals("5")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "deactivation_pending", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::before closing state");
						rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						// rbtDBManager.insertOurDateDuplicateSub(subscriberId,
						// subscriber.startDate(), "before-close-state");//inserting into duplicate
						// subscriber table
						Tools.writeSubscriberToSDRFile(subscriberId, userType,
								"deactivation_pending", subscriber.startDate(), null);
						if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
							// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
							// insertSMSInDuplicate);
							sendActivationFailedSMS(subscriber);
						if(isSubscriberBulkActivated && subscriber.rbtType() != 10) {
							WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
									+ "_act_failure", rotationSize, eventType,
									subscriberId, userType, "activation_polling",
									"deactivation_pending", requestedTimeString, differenceTime
											+ "", app, urlstr, result);
						}

						returnValue = "before-close-state";
					}

					else if(subscriberStatus.equals("4")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "deactivated_user", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::unsubscribed user");
						
						int index = getNextIndex(subscriber, null);
						if(!subscriber.prepaidYes() || index == -1) {
							if(subscriber.prepaidYes())
								writeGraceSDR(eventType, subscriberId, "activation", result
										+ " - deactivating");
							rbtDBManager.deactivateSubscriberForTATA(subscriberId);
							Tools.writeSubscriberToSDRFile(subscriberId, userType,
									"deactivated_user", subscriber.startDate(), null);
							if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
								sendActivationFailedSMS(subscriber);
							if(isSubscriberBulkActivated && subscriber.rbtType() != 10) {
								WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
										+ "_act_failure", rotationSize, eventType,
										subscriberId, userType, "activation_polling",
										"deactivated_user", requestedTimeString, differenceTime
												+ "", app, urlstr, result);
							}
							returnValue = "deactivated";
						}
						else {
							Calendar nextRetryCal = Calendar.getInstance();
							Date nextChargingDate = subscriber.nextChargingDate();
							if(nextChargingDate != null)
								nextRetryCal.setTime(nextChargingDate);
							nextRetryCal.add(Calendar.HOUR, Integer
									.parseInt(graceIntervalHours.get(index)));
							logger.info("RBT::updating user activation to grace");
							writeGraceSDR(eventType, subscriberId, "activation", result
									+ " - moving_to_grace");
							if(!rbtDBManager.updateSubscriberToGrace(subscriberId, nextRetryCal
									.getTime(), getGraceInfo(subscriber.activationInfo(), index)))
								logger.info("RBT::updating subscriber to grace failed");
							returnValue = "updated-to-grace";
						}
					}
					else if(subscriberStatus.equals("2")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								eventType, subscriberId, userType,
								"activation_polling", "activated_user", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::subscriber is registered subscriber, updating database");

						if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10) {
							sendActivationSuccessSMS(subscriber, cos);
						}
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_BULK_ACT_WELCOME_SMS", "");
						String sendBulkACTWelcomeSMSString = parameter.getValue();
						boolean sendBulkACTWelcomeSMS;
						if(sendBulkACTWelcomeSMSString.equalsIgnoreCase("TRUE"))
							sendBulkACTWelcomeSMS = true;
						else
							sendBulkACTWelcomeSMS = false;
						if(isSubscriberBulkActivated && sendBulkACTWelcomeSMS
								&& subscriber.rbtType() != 10) {
							addBulkUserToFile(subscriberId, subscriber.activatedBy(), "activated");
						}

						Calendar nextChargingDate = Calendar.getInstance();
						nextChargingDate.set(2035, 11, 31, 0, 0, 0);

						String type = "B";
						if(subscriber.prepaidYes())
							type = "P";

						rbtDBManager.smSubscriptionSuccess(subscriberId,
								nextChargingDate.getTime(), new Date(), type, subscriber
										.subscriptionClass(), true, cos, subscriber.rbtType());
						// rbtDBManager.insertDuplicateSub(subscriberId,
						// subscriber.startDate());//inserting into duplicate subscriber table
						Tools.writeSubscriberToSDRFile(subscriberId, userType, "activated_user",
								subscriber.startDate(), new Date());
						returnValue = "activated";
					}
				}
				else {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							eventType, subscriberId, userType, "activation_polling",
							"error_response", requestedTimeString, differenceTime + "", app,
							urlstr, result);
					logger.info("RBT::unexpected response, will try in next loop");
					returnValue = "notok";
				}
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "activation_polling",
						"null_error_response", requestedTimeString, differenceTime + "", app,
						urlstr, result);
				logger.info("RBT::null response, will try in next loop");
				returnValue = "notok";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String deactivateSubscriber(Subscriber subscriber) {
		String result = null;
		String returnValue = null;
		
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			String subscriberId = subscriber.subID();
			boolean isSubscriberBulkDeactivated = isSubscriberBulkDeactivated(subscriber);

			boolean isSubscriberDeactivatedByCC = (subscriber.deactivatedBy().indexOf("CC") >= 0);

			// updating deactivated by column in subscriber table
			if(subscriber.deactivatedBy() == null)
				subscriber = updateDeactivatedBy(subscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_ACCOUNT_PAGE", "");
			String deleteAccountPage = parameter.getValue();
			urlstr += deleteAccountPage;

			String opAcc = TataUtility.getOperatorAccount(null);
			String opPass = TataUtility.getOperatorPassword(null);
			String op = TataUtility.getOperatorCode(null);
			if(isSubscriberBulkDeactivated) {
				CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
				opAcc = TataUtility.getOperatorAccount(cos);
				opPass = TataUtility.getOperatorPassword(cos);
				op = TataUtility.getOperatorCode(cos);
			}
			urlstr += opAcc + "&";
			urlstr += opPass + "&";
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += op;

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			logger.info("RBT::deactivation result for subscriber " + subscriberId + " is:: " + result);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				return null;
			}

			result = result.trim();

			if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "not_valid_user",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " is not a valid user. Treating deactivation as success");

				if(sendSMS && !(isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
						&& subscriber.rbtType() != 10){
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_SUCCESS_SMS", "");
					String deactivationSuccessSMS = parameter.getValue();
					Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationSuccessSMS,
							insertSMSInDuplicate);
				}
				if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
						&& subscriber.rbtType() != 10) {
					addBulkUserToFile(subscriberId, subscriber.deactivatedBy(), "deactivated");
				}

				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				rbtDBManager.addSubscriberToDeactivatedSubscribersTable(subscriberId, subscriber
						.deactivatedBy(), subscriber.activatedCosID());
				returnValue = "not-a-valid-user";
			}
			else if(result.equals("99") || result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "provisional_success",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " request is in pending, updating database accordingly");
				rbtDBManager.smURLUnSubscription(subscriberId, true, false);
				returnValue = "deactivation-pending";
			}
			else if(result.equals("9")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "corporate_subscriber",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user "
										+ subscriberId
										+ " request was not sucessful, cannot be deactivated, corporate subscriber");
				if(sendSMS && subscriber.rbtType() != 10){
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_FAILED_SMS", "");
					String deactivationFailedSMS = parameter.getValue();
					Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationFailedSMS,
							insertSMSInDuplicate);
				}
				rbtDBManager.deactivationFailedForTATA(subscriberId);
				returnValue = "corporate subscriber cannot be deactivated";
			}
			else if(result.equals("8")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "portal_error",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " request was not sucessful, portal-error. Will try in the next loop");
				returnValue = "portal-error";
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_DEACTIVATION",
						subscriberId, userType, "deactivation", "error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " request was not sucessful, will try in the next loop");
				returnValue = "notok";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String checkDeactivationPendingSubscriber(Subscriber subscriber) {
		String result = null;
		String returnValue = null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			logger.info("RBT::inside try.......");
			String subscriberId = subscriber.subID();

			boolean isSubscriberBulkDeactivated = isSubscriberBulkDeactivated(subscriber);
			boolean isSubscriberDeactivatedByCC = (subscriber.deactivatedBy().indexOf("CC") >= 0);
			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			{
				Date setDt = subscriber.endDate();
				Date nowDt = new Date();

				long setTimeInMillis = setDt.getTime();
				long nowTimeInMillis = nowDt.getTime();

				long differenceHours = (nowTimeInMillis - setTimeInMillis) / (1000 * 60 * 60);
				logger.info("RBT::differenceHours = " + differenceHours);
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NO_OF_HOURS_TO_HIT", "");
				int noOfHoursToHit = 1;
				try{
					noOfHoursToHit = Integer.parseInt(parameter.getValue().trim());
				}catch (NumberFormatException e) {
					noOfHoursToHit = 1;
				}
				logger.info("RBT::noOfHoursToHit = " + noOfHoursToHit);
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NO_OF_HOURS_TO_HIT_FOR_BULK_PROMO", "6");
				int noOfHoursToHitForBulkPromo = Integer.parseInt(parameter.getValue());
				if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
						&& differenceHours >= noOfHoursToHitForBulkPromo) {
					logger.info("RBT::the request to deactivate bulk subscriber took more than expected time");
					addBulkUserToFile(subscriberId, subscriber.deactivatedBy(),
							"deactivation_failed");
					return "exceeded-max-promo-hours";
				}

				if(differenceHours >= noOfHoursToHit) {
					logger.info("RBT::the request to deactivate subscriber took more than expected time");
					rbtDBManager.deactivationFailedForTATA(subscriberId);
					if(sendSMS && subscriber.rbtType() != 10){
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_FAILED_SMS", "");
						String deactivationFailedSMS = parameter.getValue();
						Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationFailedSMS,
								insertSMSInDuplicate);
					}
					return "exceeded-max-hours";
				}
			}
			StringBuilder urlstrBuf = new StringBuilder(httpLink);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SUBSCRIBER_STATUS_PAGE", "");
			String subscriberStatusPage = parameter.getValue();
			urlstrBuf.append(subscriberStatusPage);
			urlstrBuf.append(TataUtility.getOperatorAccount(null) + "&");
			urlstrBuf.append(TataUtility.getOperatorPassword(null) + "&");
			urlstrBuf.append("phonenumber=" + subscriberId + "&");
			urlstrBuf.append(TataUtility.getOperatorCode(null));
			String urlstr = urlstrBuf.toString();

			Date requestedTimeStamp = new Date();
			result = querySubscriberStatus(urlstr, subscriberId);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			logger.info("RBT::deactivation result for subscriber " + subscriberId + " is:: " + result);

			String subscriberStatus = null;
			if(result != null) {
				result = result.trim();
				if(result.length() <= 2) {
					if(result.equals("9")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "user_is_suspended", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT:: "
								+ subscriberId + " is in suspended state");
						// rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						returnValue = "suspended";
					}
					else if(result.equals("5")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "user_is_blacklisted", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::"
								+ subscriberId + " is a blacklisted user");
						// rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						if(sendSMS && subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BLACK_LISTED_SMS", "");
							String blackListedSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, blackListedSMS,
									insertSMSInDuplicate);
						}
						returnValue = "black-listed";
					}
					else if(result.equals("8")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "portal_error", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::portal-error");
						returnValue = "portal-error";
					}
					else if(result.equals("10")) {
						logger.info("RBT::express-copy-pending");
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "user_express_copy_pending",
								requestedTimeString, differenceTime + "", app, urlstr, result);

						rbtDBManager.deactivationFailedForTATA(subscriberId);

						if(sendSMS && (!isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_FAILED_SMS", "");
							String deactivationFailedSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationFailedSMS,
									insertSMSInDuplicate);
						}
						if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10) {
							addBulkUserToFile(subscriberId, subscriber.deactivatedBy(),
									"deactivation_failed");
						}
						returnValue = "express-copy-pending";
					}

				}
				else if(result.length() > 2 && result.length() <= 20) {
					StringTokenizer st = new StringTokenizer(result, "|");
					if(st.hasMoreTokens())
						subscriberStatus = st.nextToken();
					if(subscriberStatus.equals("1") || subscriberStatus.equals("6")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "activation_pending", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::before open state");
						rbtDBManager.deactivationFailedForTATA(subscriberId);
						if(sendSMS && (!isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_FAILED_SMS", "");
							String deactivationFailedSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationFailedSMS,
									insertSMSInDuplicate);
						}
						if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10) {
							addBulkUserToFile(subscriberId, subscriber.deactivatedBy(),
									"deactivation_failed");
						}

						returnValue = "activation-pending";
					}
					else if(subscriberStatus.equals("7") || subscriberStatus.equals("5")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "deactivation_pending",
								requestedTimeString, differenceTime + "", app, urlstr, result);
						logger.info("RBT::before closing state");
						returnValue = "deactivation-pending";
					}

					else if(subscriberStatus.equals("4")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "deactivated_user", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						logger.info("RBT::unsubscribed user");
						if(sendSMS && !(isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_SUCCESS_SMS", "");
							String deactivationSuccessSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationSuccessSMS,
									insertSMSInDuplicate);
						}
						if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10) {
							addBulkUserToFile(subscriberId, subscriber.deactivatedBy(),
									"deactivated");
						}

						rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						rbtDBManager.addSubscriberToDeactivatedSubscribersTable(subscriberId,
								subscriber.deactivatedBy(), subscriber.activatedCosID());
						returnValue = "deactivated";
					}
					else if(subscriberStatus.equals("2")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_DEACTIVATION_POLLING", subscriberId, userType,
								"deactivation_polling", "activated_user", requestedTimeString,
								differenceTime + "", app, urlstr, result);
						rbtDBManager.deactivationFailedForTATA(subscriberId);
						if(sendSMS && (!isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_FAILED_SMS", "");
							String deactivationFailedSMS = parameter.getValue();
							Tools.sendSMS(db_url, m_smsNo, subscriberId, deactivationFailedSMS,
									insertSMSInDuplicate);
						}
						if((isSubscriberBulkDeactivated || isSubscriberDeactivatedByCC)
								&& subscriber.rbtType() != 10) {
							addBulkUserToFile(subscriberId, subscriber.deactivatedBy(),
									"deactivation_failed");
						}

						returnValue = "activated";
					}
				}
				else {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DEACTIVATION_POLLING", subscriberId, userType,
							"deactivation_polling", "error_response", requestedTimeString,
							differenceTime + "", app, urlstr, result);
					logger.info("RBT::unexpected response, will try in next loop");
				}
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DEACTIVATION_POLLING", subscriberId, userType, "deactivation_polling",
						"null_error_response", requestedTimeString, differenceTime + "", app,
						urlstr, result);
				logger.info("RBT::got null from subscriber status query");
				returnValue = "notok";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String addClipForSubscriber(SubscriberStatus addClipForSubscriber) {
		String callerId = null;
		String returnValue = null;
		boolean downloadedNow = false;

		String name = null;
		String cost = null;
		String validity = null;
		String validityStr = null;
		String chargeClassStr = null;
		ChargeClass chargeClass = null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "RENEWAL_FAILURE_SMS", "");
		String renewalFailureSMS = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_SMS_FOR_PREPAID_DOWNLOAD", "");
		String sendSMSForPrepaidDownloadString = parameter.getValue();
		boolean sendSMSForPrepaidDownload;
		if(sendSMSForPrepaidDownloadString.equalsIgnoreCase("TRUE"))
			sendSMSForPrepaidDownload = true;
		else
			sendSMSForPrepaidDownload = false;
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS_TO_RETAILER", "");
		String selectionFailedSMSToRetailer = parameter.getValue();
		try {
			logger.info("RBT::inside try.....");

			String subscriberId = addClipForSubscriber.subID().trim();
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
			boolean sendSelSMS = true;
			if(addClipForSubscriber.status() > 2)
				sendSelSMS = false;
			boolean isOnePlusOneSelection = (addClipForSubscriber.selectionInfo().indexOf("1+1") != -1);
			if(!rbtDBManager.checkCanAddSetting(subscriber)) {
				logger.info("RBT::subscriber not registered yet cannot add song will try in next loop");
				return "not-a-subscriber";
			}

			boolean isSelectionBulkActivated = isSelectionBulkActivated(addClipForSubscriber);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);
			String userType = "";
			if(subscriber.prepaidYes()) {
				userType = "PRE_PAID";
				if(!sendSMSForPrepaidDownload) {
					sendSelSMS = false;
				}
			}
			else {
				userType = "POST_PAID";
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_SMS_FOR_POSTPAID_DOWNLOAD", "");
				String sendSMSForPostpaidDownloadString = parameter.getValue();
				boolean sendSMSForPostpaidDownload;
				if(sendSMSForPostpaidDownloadString.equalsIgnoreCase("TRUE"))
					sendSMSForPostpaidDownload = true;
				else
					sendSMSForPostpaidDownload = false;
				if(!sendSMSForPostpaidDownload) {
					sendSelSMS = false;
				}
			}

			if(addClipForSubscriber.callerID() != null)
				callerId = addClipForSubscriber.callerID().trim();

			int setType;

			if(callerId == null)
				setType = 1;
			else if(callerId.length() == 4)
				setType = 3;
			else if(callerId.length() == 10 || callerId.length() == 11)
				setType = 2;
			else
				setType = 1;

			String toneCode = addClipForSubscriber.subscriberFile().trim();

			int timeType = 0;
			String startTime = null;
			String endTime = null;

			int flag = addClipForSubscriber.status();
			if(flag > 2)
				flag = 2;

			int toneFlag = 0;

			try {
				if(addClipForSubscriber.categoryType() == BOUQUET)
					toneFlag = 1;

				if(toneFlag == 1) {
					String circleId = rbtDBManager.getCircleId(addClipForSubscriber.subID());
					Categories musicbox = rbtDBManager.getCategoryPromoID(toneCode, circleId,
							addClipForSubscriber.prepaidYes() ? 'y' : 'n');
					name = musicbox.name();
					chargeClassStr = musicbox.classType();
				}
				else {
					Clips clip = rbtDBManager.getClipByPromoID(toneCode);
					name = clip.name();
					chargeClassStr = clip.classType();
				}
			}
			catch(NullPointerException npe) {
				logger.error("", npe);
				// rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				// addClipForSubscriber.subscriberFile());
				// rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				// addClipForSubscriber.subscriberFile(), "Daemon-Clip/MB missing");

				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, callerId, toneCode,
				// addClipForSubscriber.startTime(), "cat/clip/MB missing/null entry");
				deleteSubscriberSelection(addClipForSubscriber.subID(), addClipForSubscriber
						.subscriberFile(), null, true, "Daemon-Clip/MB missing", false, -1,
						addClipForSubscriber.prepaidYes(), toneFlag != 1);
				Tools.writeSelectionToSDRFile(subscriberId, userType,
						"cat/clip/MB missing/null entry", addClipForSubscriber.setTime(), null,
						callerId, toneCode);

				return "cat/clip/MB missing/null entry";
			}

			if(timeType == 0) {
				startTime = "2003-01-01-00:00:00";
				endTime = "2003-01-01-23:59:59";
			}
			int validityInt = 365;

			chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(chargeClassStr);
			if(chargeClass != null) {
				cost = chargeClass.getAmount();
				validity = chargeClass.getSelectionPeriod();

				validityInt = (validity != null) ? Integer.parseInt(validity.substring(1)) : 365;

				if(validity.startsWith("M"))
					validityInt = validityInt * 30;
				else if(validity.startsWith("Y"))
					validityInt = validityInt * 365;
			}

			// checking for how long we have been hitting the request
			Date setDt = addClipForSubscriber.setTime();
			Date nowDt = new Date();

			long setTimeInMillis = setDt.getTime();
			long nowTimeInMillis = nowDt.getTime();

			long differenceHours = (nowTimeInMillis - setTimeInMillis) / (1000 * 60 * 60);
			// logger.info("RBT::nowTimeInMillis = " +
			// nowTimeInMillis);
			// logger.info("RBT::setTimeInMillis = " +
			// setTimeInMillis);
			logger.info("RBT::differenceHours = "
					+ differenceHours);
			// logger.info("RBT::noOfHoursToHit = " +
			// noOfHoursToHit);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NO_OF_HOURS_TO_HIT", "");
			int noOfHoursToHit = 1;
			try{
				noOfHoursToHit = Integer.parseInt(parameter.getValue().trim());
			}catch (NumberFormatException e) {
				noOfHoursToHit = 1;
			}
			if(differenceHours >= noOfHoursToHit) {
				logger.info("RBT::the request to add "
						+ addClipForSubscriber.subscriberFile() + " for subscriber "
						+ addClipForSubscriber.subID()
						+ " took more than specified time, taking it as negative");
				// rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				// addClipForSubscriber.subscriberFile());
				// rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				// addClipForSubscriber.subscriberFile(), "Daemon-Exceeded max hrs");
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, callerId, toneCode,
				// addClipForSubscriber.startTime(), "exceeded no. of hours");

				deleteSubscriberSelection(addClipForSubscriber.subID(), addClipForSubscriber
						.subscriberFile(), null, true, "Daemon-Exceeded max hrs", false, -1,
						addClipForSubscriber.prepaidYes(), toneFlag != 1);
				Tools.writeSelectionToSDRFile(subscriberId, userType, "exceeded no. of hours",
						addClipForSubscriber.setTime(), null, callerId, toneCode);

				if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
					String gifterID = addClipForSubscriber.selectionInfo();
					String gifteeID = addClipForSubscriber.subID();
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
					String songGiftFailedSMSforGifter = parameter.getValue();
					String actualSelFailedSMS = Tools.findNReplaceAll(songGiftFailedSMSforGifter,
							"%TUNE%", name);
					actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS, "%NUMBER%",
							gifteeID);
					if(sendSMS && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
								insertSMSInDuplicate);
				}
				// else
				else if(!addClipForSubscriber.selectedBy().equalsIgnoreCase("LOOP")) {
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
					String selectionFailedSMS = parameter.getValue();
					String actualSelFailedSMS = selectionFailedSMS;
					if(isOnePlusOneSelection)
						actualSelFailedSMS = renewalFailureSMS;
					actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS, "%TUNE%", name);
					if(isSelectionBulkActivated)
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_sel_failure", rotationSize, "RBT_ADD_SETTING", subscriberId,
								userType, "add_setting", "exceeded-max-hours", formatter
										.format(setDt), (nowTimeInMillis - setTimeInMillis) + "",
								app, "NA", "NA");
					if(sendSMS && actualSelFailedSMS != null && !isSelectionBulkActivated
							&& subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
								insertSMSInDuplicate);
					if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
						sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
								selectionFailedSMSToRetailer, name, null, null);
				}
				returnValue = "exceeded-max-hours";
				return returnValue;
			}

			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_ACTIVATION_DOWNLOAD_PARAMETER", "TRUE");
			boolean bulkActivationDownloadParameter = (new Boolean(parameter.getValue().trim())).booleanValue();
			if(bulkActivationDownloadParameter && subscriber.subYes().equals(STATE_EVENT)
					&& (toneFlag == 1 || subscriber.maxSelections() > cos.getFreeSongs()))
				subscriber = updateBulkActivatedSubscriber(subscriber);

			SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(subscriberId,
					toneCode);

			// downloading song/musicbox before setting it
			boolean toBeDownloaded = download != null && download.downloadStatus() == 'n';
			if(toBeDownloaded) {
				downloadedNow = true;
				
				/**
				 * Added by Sreekar as part of RBT-4281
				 */
				returnValue = combinedRequest(subscriber, addClipForSubscriber, "RBT_DOWNLOAD_TONE");
				if(returnValue.equals("success") || returnValue.equals("success"))
				
				/**
				 * Commented by Sreekar as part of RBT-4281 using combined charging API instead
				 */
				/*if(toneFlag != 1) {
					// if(!downloadSongForUser(addClipForSubscriber))
					if(!downloadSongForUser(addClipForSubscriber, validityInt))

					{
						logger.info("RBT::not downloaded/pending song returning false");
						returnValue = "tone-download-not-complete-yet";
					}
					else {
						if(!bulkActivationDownloadParameter
								&& subscriber.subYes().equals(STATE_EVENT)
								&& subscriber.maxSelections() > cos.getFreeSongs())
							subscriber = updateBulkActivatedSubscriber(subscriber);
					}
				}
				else {
					if(!downloadMusicboxForUser(addClipForSubscriber, validityInt)) {
						logger.info("RBT::not downloaded musicbox returning false");
						returnValue = "tonebox-download-not-complete-yet";
					}
					else {
						if(!bulkActivationDownloadParameter
								&& subscriber.subYes().equals(STATE_EVENT))
							subscriber = updateBulkActivatedSubscriber(subscriber);
					}
				}*/
				download = rbtDBManager.getActiveSubscriberDownload(subscriberId, toneCode);
			}
			else {
				if(download == null) {
					// Categories category =
					// rbtDBManager.getCategory(addClipForSubscriber.categoryID());
					/*
					 * download = rbtDBManager.addSubscriberDownload(addClipForSubscriber.subID(),
					 * addClipForSubscriber.subscriberFile(), addClipForSubscriber.categoryID(),
					 * toneFlag == 0?'y':'n', false,category.type());
					 */
					// download =
					rbtDBManager.addSubscriberDownload(addClipForSubscriber.subID(),
							addClipForSubscriber.subscriberFile(), addClipForSubscriber
									.categoryID(), false, toneFlag == 0 ? DTMF_CATEGORY : BOUQUET);
					logger.info("RBT::added to rbt_subscriber_downloads");
					returnValue = "not-downloaded-yet";
				}
				downloadedNow = false;
			}

			if(rbtDBManager.getSubWavFileForCaller(subscriberId, callerId, toneCode) == null) {
				return ("failed.multiple-reasons");
			}

			if(download != null) {
				char downloadStatus = download.downloadStatus();
				if(downloadStatus == 'p') {
					downloadedNow = true;
					logger.info("RBT::downloadedNow =  "
							+ downloadedNow + "changed downloadedNow bcoz downloadStatus == p");
					boolean queryLibrary = true;
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "CAN_QUERY_DOWNLOAD_STATUS", "");
					String canQueryDownloadStatusString = parameter.getValue();
					boolean canQueryDownloadStatus;
					if(canQueryDownloadStatusString.equalsIgnoreCase("TRUE"))
						canQueryDownloadStatus = true;
					else
						canQueryDownloadStatus = false;
					if(canQueryDownloadStatus && !toBeDownloaded)
						queryLibrary = queryDownloadStatus(addClipForSubscriber, toneFlag,
								validityInt);
					else if(toBeDownloaded)
						queryLibrary = false;

					if(queryLibrary) {
						downloadStatus = 'y';
						if(!bulkActivationDownloadParameter && isSubscriberBulkActivated
								&& !isSelectionBulkActivated
								&& !subscriber.subYes().equalsIgnoreCase(STATE_EVENT))
							subscriber = updateBulkActivatedSubscriber(subscriber);
						if(sendSMSForPrepaidDownload && subscriber.prepaidYes())
							sendSelSMS = false;
					}
					// }
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SHOULD_QUERY_PERSONAL_TONE", "");
					String shouldQueryPersonalToneString = parameter.getValue();
					boolean shouldQueryPersonalTone;
					if(shouldQueryPersonalToneString.equalsIgnoreCase("TRUE"))
						shouldQueryPersonalTone = true;
					else
						shouldQueryPersonalTone = false;
					if(shouldQueryPersonalTone) {
						logger.info("RBT::cannot add the clip, not downloaded yet pending, checking personal library");
						if(toneFlag != 1) {
							String[] clipsList = querySubscriberClipsFromLibrary(subscriberId, name);
							for(int i = 0; clipsList != null && i < clipsList.length; i++) {
								if(clipsList[i].equalsIgnoreCase(toneCode)) {
									logger.info("RBT::download success for subscriber" + subscriberId
													+ " for the tone " + toneCode
													+ " updating rbt_subscriber_downloads to 'y'");
									rbtDBManager.updateDownloadStatusToDownloaded(subscriberId,
											toneCode, new Date(), validityInt);
									// if(addClipForSubscriber.selectedBy().indexOf("PROMO") >= 0)
									// rbtDBManager.setAvailedPromoDownload(subscriberId, true);
									downloadStatus = 'y';
									break;
								}
							}
						}
						else {
							String[] musicboxesList = querySubscriberMusicboxesFromLibrary(
									subscriberId, name);
							for(int i = 0; musicboxesList != null && i < musicboxesList.length; i++) {
								if(musicboxesList[i].equalsIgnoreCase(toneCode)) {
									logger.info("RBT::download success for subscriber" + subscriberId
													+ " for the tone " + toneCode
													+ " updating rbt_subscriber_downloads to 'y'");
									rbtDBManager.updateDownloadStatusToDownloaded(subscriberId,
											toneCode, new Date(), validityInt);
									break;
								}
							}
						}
					}
				}

				if(downloadStatus == 'y') {

					// SubscriberStatus [] subscriberSettings =
					// rbtDBManager.getSubscriberRecordsWithWavFile(subscriberId, toneCode);

					// int numberOfRecords = 1;

					// if(subscriberSettings != null)
					// numberOfRecords = subscriberSettings.length;

					download = rbtDBManager.getActiveSubscriberDownload(subscriberId, toneCode);
					StringBuilder urlstrBuf = new StringBuilder(httpLink);
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ADD_SETTING_PAGE", "");
					String addSettingPage = parameter.getValue();
					urlstrBuf.append(addSettingPage);
					urlstrBuf.append(TataUtility.getOperatorAccount(null) + "&");
					urlstrBuf.append(TataUtility.getOperatorPassword(null) + "&");
					urlstrBuf.append("phonenumber=" + subscriberId + "&");
					urlstrBuf.append("specialphone=");
					if(callerId != null && !callerId.equalsIgnoreCase("null"))
						urlstrBuf.append(callerId);
					urlstrBuf.append("&settype=" + setType + "&");
					urlstrBuf.append("starttime=" + startTime + "&");
					urlstrBuf.append("endtime=" + endTime + "&");
					urlstrBuf.append("timetype=" + timeType + "&");
					urlstrBuf.append("tonecode=" + toneCode + "&");
					urlstrBuf.append("description=" + "&");
					urlstrBuf.append("flag=" + flag + "&");
					urlstrBuf.append("toneflag=" + toneFlag + "&");
					urlstrBuf.append(TataUtility.getOperatorCode(null));
					
					String urlstr = urlstrBuf.toString();

					RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

					Date requestedTimeStamp = new Date();
					String result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
					Date responseTimeStamp = new Date();

					long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp
							.getTime());

					String requestedTimeString = formatter.format(requestedTimeStamp);

					logger.info("RBT::adding tone/musicbox setting result for subscriber "
											+ subscriberId + " for the clip " + toneCode + "is:: "
											+ result);

					if(result == null) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"null_error_response", requestedTimeString, differenceTime + "",
								app, urlstr, result);
						return null;
					}

					result = result.trim();

					String selSuccessSMS;

					if(result.equals("0") || result.equals("9")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"success", requestedTimeString, differenceTime + "", app, urlstr,
								result);
						logger.info("RBT::added the clip "
								+ toneCode + " successfully. updating database");

						if(flag == 1)
							rbtDBManager.deactivateSubscriberRecords(subscriberId, callerId);
						else if(rbtDBManager
								.checkMBSettingExistsForCallerId(subscriberId, callerId))
							rbtDBManager.deactivateSubscriberRecords(subscriberId, callerId);

						rbtDBManager.clipToBeUpdatedForTATA(addClipForSubscriber);// ,
																					// download.endTime());
						// rbtDBManager.insertDuplicateSubSel(subscriberId, callerId, toneCode,
						// addClipForSubscriber.startTime());
						Tools.writeSelectionToSDRFile(subscriberId, userType, "success",
								addClipForSubscriber.setTime(), new Date(), callerId, toneCode);

						if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
							String gifterID = addClipForSubscriber.selectionInfo();
							String gifteeID = addClipForSubscriber.subID();
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_SUCCESS_SMS_FOR_GIFTER", "");
							String songGiftSuccessSMSforGifter = parameter.getValue();
							selSuccessSMS = Tools.findNReplaceAll(songGiftSuccessSMSforGifter,
									"%TUNE%", name);
							selSuccessSMS = Tools.findNReplaceAll(selSuccessSMS, "%NUMBER%",
									gifteeID);
							selSuccessSMS = Tools.findNReplaceAll(selSuccessSMS, "%COST%", cost);
							if(sendSMS && subscriber.rbtType() != 10)
								Tools.sendSMS(db_url, m_smsNo, gifterID, selSuccessSMS,
										insertSMSInDuplicate);
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_SUCCESS_SMS_FOR_GIFTEE", "");
							String songGiftSuccessSMSforGiftee = parameter.getValue();
							selSuccessSMS = Tools.findNReplaceAll(songGiftSuccessSMSforGiftee,
									"%TUNE%", name);
							selSuccessSMS = Tools.findNReplaceAll(selSuccessSMS, "%NUMBER%",
									gifterID);
							if(sendSMS)
								Tools.sendSMS(db_url, m_smsNo, gifteeID, selSuccessSMS,
										insertSMSInDuplicate);

							return "setting-successful";
						}
						String defaultSMS = "";
						String personalSMS = "";
						if(isOnePlusOneSelection) {
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "RENEWAL_DEFAULT_SUCCESS_SMS", "");
							String renewalDefaultSuccessSMS = parameter.getValue();
							defaultSMS = renewalDefaultSuccessSMS;
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "RENEWAL_PERSONAL_SUCCESS_SMS", "");
							String renewalPersonalSuccessSMS = parameter.getValue();
							personalSMS = renewalPersonalSuccessSMS;
						}
						else {
							if(toneFlag != 1) {
								logger.info("RBT::downloadedNow =  " + downloadedNow);
								if(downloadedNow) {
									if(cos != null
											&& !cos.isDefaultCos()
											&& addClipForSubscriber.selectedBy().indexOf(
													"COS" + cos.getCosId()) >= 0) {
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_TONE_SELECTION_SUCCESS_SMS_COS"+cos.getCosId(), "");
										defaultSMS = parameter.getValue();
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_TONE_SELECTION_SUCCESS_SMS_COS"+cos.getCosId(), "");
										personalSMS = parameter.getValue();
									}

									else {
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_TONE_SELECTION_SUCCESS_SMS", "");
										String defaultToneSelectionSuccessSMS = parameter.getValue();
										defaultSMS = defaultToneSelectionSuccessSMS;
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_TONE_SELECTION_SUCCESS_SMS", "");
										String personalToneSelectionSuccessSMS = parameter.getValue();
										personalSMS = personalToneSelectionSuccessSMS;
									}
								}
								else {
									parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_TONE_SELECTION_SUCCESS_SMS_LIB", "");
									String defaultToneSelectionSuccessSMSLib = parameter.getValue();
									defaultSMS = defaultToneSelectionSuccessSMSLib;
									parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_TONE_SELECTION_SUCCESS_SMS_LIB", "");
									String personalToneSelectionSuccessSMSLib = parameter.getValue();
									personalSMS = personalToneSelectionSuccessSMSLib;
								}
							}
							else {
								if(downloadedNow)// || numberOfRecords < 2)
								{
									if(cos != null
											&& !cos.isDefaultCos()
											&& addClipForSubscriber.selectedBy().indexOf(
													"COS" + cos.getCosId()) >= 0) {
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_MUSICBOX_SELECTION_SUCCESS_SMS_COS"+ cos.getCosId(), "");
										defaultSMS = parameter.getValue();
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_MUSICBOX_SELECTION_SUCCESS_SMS_COS"+ cos.getCosId(), "");
										personalSMS = parameter.getValue();
									}
									else {
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_MUSICBOX_SELECTION_SUCCESS_SMS", "");
										String defaultMusicboxSelectionSuccessSMS = parameter.getValue();
										defaultSMS = defaultMusicboxSelectionSuccessSMS;
										parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_MUSICBOX_SELECTION_SUCCESS_SMS", "");
										String personalMusicboxSelectionSuccessSMS = parameter.getValue();
										personalSMS = personalMusicboxSelectionSuccessSMS;
									}
								}
								else {
									parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEFAULT_MUSICBOX_SELECTION_SUCCESS_SMS_LIB", "");
									String defaultMusicboxSelectionSuccessSMSLib = parameter.getValue();
									defaultSMS = defaultMusicboxSelectionSuccessSMSLib;
									parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PERSONAL_MUSICBOX_SELECTION_SUCCESS_SMS_LIB", "");
									String personalMusicboxSelectionSuccessSMSLib = parameter.getValue();
									personalSMS = personalMusicboxSelectionSuccessSMSLib;
								}
							}
						}
						if(callerId == null || callerId == "" || callerId.equalsIgnoreCase("null"))
							selSuccessSMS = Tools.findNReplaceAll(defaultSMS, "%TUNE%", name);
						else {
							selSuccessSMS = Tools.findNReplaceAll(personalSMS, "%TUNE%", name);
							selSuccessSMS = Tools.findNReplaceAll(selSuccessSMS, "%NUMBER%",
									callerId);
						}

						if(chargeClass != null) {
							// cost = chargeClass.amount();
							// validity = chargeClass.selectionPeriod();

							String validFor = null;
							String validForNo = null;

							validForNo = validity.substring(1);

							if(validity.startsWith("D")) {
								validFor = "days";
							}
							else if(validity.startsWith("M")) {
								validFor = "months";
							}
							else if(validity.startsWith("Y")) {
								validFor = "years";
							}

							// selSuccessSMS = Tools.findNReplace(selSuccessSMS,
							// "%VALIDITY%", validForNo + " " + validFor);
							validityStr = validForNo + " " + validFor;
							selSuccessSMS = Tools.findNReplace(selSuccessSMS, "%VALIDITY%",
									validityStr);
							selSuccessSMS = Tools.findNReplace(selSuccessSMS, "%COST%", cost);
						}

						String actualSelSuccessSMS = selSuccessSMS;
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_BULK_ACT_WELCOME_SMS", "");
						String sendBulkACTWelcomeSMSString = parameter.getValue();
						boolean sendBulkACTWelcomeSMS;
						if(sendBulkACTWelcomeSMSString.equalsIgnoreCase("TRUE"))
							sendBulkACTWelcomeSMS = true;
						else
							sendBulkACTWelcomeSMS = false;
						if(isSelectionBulkActivated && !sendBulkACTWelcomeSMS)
							addBulkUserToFile(subscriberId, subscriber.activatedBy(), "selection");
						else if(sendSelSMS && sendSMS
								&& !addClipForSubscriber.selectedBy().equalsIgnoreCase("LOOP")
								&& subscriber.rbtType() != 10) // else if (sendSMS)
							Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelSuccessSMS,
									insertSMSInDuplicate);

						if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
								&& !isOnePlusOneSelection){
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_SUCCESS_SMS_TO_RETAILER", "");
							String selectionSuccessSMSToRetailer = parameter.getValue();
							sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
									selectionSuccessSMSToRetailer, name, cost, validityStr);
						}
						returnValue = "setting-successful";
					}
					else if(result.equals("12") || result.equals("10") || result.equals("6")
							|| result.equals("7") || result.equals("2") || result.equals("11")
							|| result.equals("14")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"general_error", requestedTimeString, differenceTime + "", app,
								urlstr, result);
						logger.info("RBT::cannot add the clip. response code is " + result
										+ " deleting from database");
						// rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
						// addClipForSubscriber.subscriberFile());
						// rbtDBManager.deleteSubscriberDownload(addClipForSubscriber.subID(),
						// addClipForSubscriber.subscriberFile());
						// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, callerId,
						// toneCode, addClipForSubscriber.startTime(), "invalid response from bak
						// end");
						deleteSubscriberSelection(subscriberId, toneCode, callerId, false, null,
								false, -1, addClipForSubscriber.prepaidYes(), toneFlag != 1);

						// rbtDBManager.deleteClipForSubscriber(addClipForSubscriber);
						Tools.writeSelectionToSDRFile(subscriberId, userType,
								"invalid response from bak end", addClipForSubscriber.setTime(),
								null, callerId, toneCode);

						if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
							String gifterID = addClipForSubscriber.selectionInfo();
							String gifteeID = addClipForSubscriber.subID();
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
							String songGiftFailedSMSforGifter = parameter.getValue();
							String actualSelFailedSMS = Tools.findNReplaceAll(
									songGiftFailedSMSforGifter, "%TUNE%", name);
							actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS,
									"%NUMBER%", gifteeID);
							if(sendSMS && subscriber.rbtType() != 10)
								Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
										insertSMSInDuplicate);
						}
						else if(!addClipForSubscriber.selectedBy().equalsIgnoreCase("LOOP")) // else
						{
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
							String selectionFailedSMS = parameter.getValue();
							String actualSelFailedSMS = selectionFailedSMS;
							if(isOnePlusOneSelection)
								actualSelFailedSMS = renewalFailureSMS;
							actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS,
									"%TUNE%", name);
							if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
								Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
										insertSMSInDuplicate);
							if(isSelectionBulkActivated) {
								WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
										+ "_sel_failure", rotationSize, "RBT_ADD_SETTING",
										subscriberId, userType, "add_setting", "general_error",
										requestedTimeString, differenceTime + "", app, urlstr,
										result);
							}
							if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
									&& !isOnePlusOneSelection)
								sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
										selectionFailedSMSToRetailer, name, null, null);
						}
						returnValue = "failed.multiple-reasons";
					}
					else if(result.equals("4")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"song_not_in_huawei", requestedTimeString, differenceTime + "",
								app, urlstr, result);
						logger.info("RBT::cannot add the clip. response code is "
												+ result
												+ " deleting from database, as song is not in huwai library");
						// rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
						// addClipForSubscriber.subscriberFile());

						// rbtDBManager.addToDeletedSelections(addClipForSubscriber);

						// String circleId = rbtDBManager.getCircleId(subscriberId);
						// Categories category =
						// rbtDBManager.getCategory(addClipForSubscriber.categoryID(), circleId,
						// addClipForSubscriber.prepaidYes()?'y':'n');

						// Clips clip =
						// rbtDBManager.getClipPromoID(addClipForSubscriber.subscriberFile());
						// rbtDBManager.removeCategoryClip(category, clip);
						// rbtDBManager.removePickOfTheday(clip.id());
						// rbtDBManager.deleteSubscriberDownload(addClipForSubscriber.subID(),
						// addClipForSubscriber.subscriberFile());

						/*
						 * String actualSelFailedSMS = null; if(toneFlag == 0) { Clips clip =
						 * rbtDBManager.getClipPromoID(toneCode);
						 * rbtDBManager.removeCategoryClip(category, clip);
						 * rbtDBManager.removePickOfTheday(clip.id());
						 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
						 * addClipForSubscriber.subscriberFile(), "Daemon-Not in bak end");
						 * actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						 * clip.name()); } else { Categories mb =
						 * rbtDBManager.getCategoryPromoID(toneCode);
						 * rbtDBManager.deactivateSubWavFile(subscriberId, toneCode,
						 * STATE_DEACTIVATED, "DAEMON", "n"); // deactivateSubWavFile(subscriberId,
						 * toneCode); rbtDBManager.deactivateSubscriberDownload(subscriberId,
						 * toneCode, "Daemon-Not in bak end"); rbtDBManager.removeCategory(mb);
						 * actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						 * mb.name()); }
						 */

						// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, callerId,
						// toneCode, addClipForSubscriber.startTime(), "not in huwaii");
						Tools.writeSelectionToSDRFile(subscriberId, userType, "song_not_in_huawei",
								addClipForSubscriber.setTime(), null, callerId, toneCode);

						deleteSubscriberSelection(addClipForSubscriber.subID(),
								addClipForSubscriber.subscriberFile(), null, true,
								"Daemon-Not in Huawei", false, addClipForSubscriber.categoryID(),
								addClipForSubscriber.prepaidYes(), toneFlag != 1);

						if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
							String gifterID = addClipForSubscriber.selectionInfo();
							String gifteeID = addClipForSubscriber.subID();
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
							String songGiftFailedSMSforGifter = parameter.getValue();
							String actualSelFailedSMS = Tools.findNReplaceAll(
									songGiftFailedSMSforGifter, "%TUNE%", name);
							actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS,
									"%NUMBER%", gifteeID);
							if(sendSMS && subscriber.rbtType() != 10)
								Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
										insertSMSInDuplicate);
						}
						else if(!addClipForSubscriber.selectedBy().equalsIgnoreCase("LOOP")) // else
						{
							parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
							String selectionFailedSMS = parameter.getValue();
							String actualSelFailedSMS = selectionFailedSMS;
							if(isOnePlusOneSelection)
								actualSelFailedSMS = renewalFailureSMS;
							actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS,
									"%TUNE%", name);
							if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
								Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
										insertSMSInDuplicate);
							if(isSelectionBulkActivated && subscriber.rbtType() != 10) {
								WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
										+ "_sel_failure", rotationSize, "RBT_ADD_SETTING",
										subscriberId, userType, "add_setting",
										"song_not_in_huawei", requestedTimeString, differenceTime
												+ "", app, urlstr, result);
							}
							if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
									&& !isOnePlusOneSelection)
								sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
										selectionFailedSMSToRetailer, name, null, null);
						}
						returnValue = "failed-not-in-huwaii";
					}
					else if(result.equals("8")) {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"portal_error", requestedTimeString, differenceTime + "", app,
								urlstr, result);
						logger.info("RBT::portal-error");
						returnValue = "portal-error";
					}
					else {
						WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
								"RBT_ADD_SETTING", subscriberId, userType, "add_setting",
								"error_response", requestedTimeString, differenceTime + "", app,
								urlstr, result);
						logger.info("RBT::cannot add the clip. response code is " + result
										+ ", will try in the next loop");
						returnValue = "invalid-result";
					}
				}
			}
			else {
				returnValue = "not-downloaded-yet";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String deleteSelectionForSubscriber(SubscriberDownloads deleteSelection) {
		String result = null;
		String returnValue = null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_SELECTION_SUCCESS_SMS", "");
		String deleteSelectionSuccessfulSMS = parameter.getValue();
		try {
			logger.info("RBT::inside try.......");
			String subscriberId = deleteSelection.subscriberId();
			String toneCode = deleteSelection.promoId();

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			// int parentCategoryId = deleteSelection.categoryID();
			// String m_circleId=rbtDBManager.getCircleId(subscriberId);
			// Categories parentCategory = rbtDBManager.getCategory(parentCategoryId, m_circleId,
			// subscriber.prepaidYes()?'y':'n');

			int toneFlag = 0;
			// if(parentCategory.type() == SHUFFLE)
			if(deleteSelection.categoryType() == BOUQUET)
				toneFlag = 1;

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_PERSONAL_LIB_PAGE", "");
			String deletePersonalLibPage = parameter.getValue();
			urlstr += deletePersonalLibPage;

			urlstr += TataUtility.getOperatorAccount(null) + "&";
			urlstr += TataUtility.getOperatorPassword(null) + "&";
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += "tonecode=" + toneCode + "&";
			urlstr += "toneflag=" + toneFlag + "&";
			urlstr += TataUtility.getOperatorCode(null);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			logger.info("RBT::deleteSelectionForSubscriber result for subscriber " + subscriberId
							+ " for tonecode " + toneCode + " is:: " + result);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"null_error_response", requestedTimeString, differenceTime + "", app,
						urlstr, result);
				return null;
			}

			result = result.trim();

			String name = "";
			boolean isOnePlusOneClip = false;
			try {
				// String circleId = rbtDBManager.getCircleId(subscriberId);
				// Categories category = rbtDBManager.getCategory(deleteSelection.categoryID(),
				// circleId, subscriber.prepaidYes()?'y':'n');
				// if(category.type() == SHUFFLE)
				if(deleteSelection.categoryType() == BOUQUET) {
					String circleId = rbtDBManager.getCircleId(subscriberId);
					Categories musicbox = rbtDBManager.getCategoryPromoID(toneCode, circleId,
							subscriber.prepaidYes() ? 'y' : 'n');
					name = musicbox.name();
				}
				else {
					Clips clip = rbtDBManager.getClipByPromoID(toneCode);
					name = clip.name();
					if(clip.grammar() != null && clip.grammar().equals("1+1")
							&& deleteSelection.endTime().before(new Date()))
						isOnePlusOneClip = true;
				}
			}
			catch(NullPointerException npe) {
				logger.error("", npe);
			}

			if(result.equals("0")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"success", requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The deleteSelectionForSubscriber for the user " + subscriberId
								+ " is successful updating the database");
				rbtDBManager.deleteClipForSubscriber(deleteSelection.subscriberId(),
						deleteSelection.promoId());
				rbtDBManager.deactivateSubscriberDownload(subscriberId, toneCode, "Daemon");

				String actualDelSelSuccessSMS = Tools.findNReplaceAll(deleteSelectionSuccessfulSMS,
						"%TUNE%", name);
				if(sendSMS && !isOnePlusOneClip && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualDelSelSuccessSMS,
							insertSMSInDuplicate);
				logger.info("RBT::Returned value is "
						+ returnValue);
				returnValue = "deletion-success";
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"user_not_valid", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				logger.info("RBT::The user "
						+ subscriberId
						+ " is not a registered user. Deleting the clip form database(2 tables)");
				// rbtDBManager.deleteClipForSubscriber(deleteSelection.subscriberId(),
				// deleteSelection.promoId());
				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"Daemon-invalid user", false, -1, false,
						deleteSelection.categoryType() == DTMF_CATEGORY);
				rbtDBManager.deleteFromToBeDeletedSelections(deleteSelection);
				// rbtDBManager.deactivateSubscriberDownload(subscriberId, toneCode, "Daemon-invalid
				// user");
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_SELECTION_FAILED_SMS", "");
				String deleteSelectionFailedSMS = parameter.getValue();
				String actualDelSelFailedSMS = Tools.findNReplaceAll(deleteSelectionFailedSMS,
						"%TUNE%", name);
				if(sendSMS && !isOnePlusOneClip && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualDelSelFailedSMS,
							insertSMSInDuplicate);
				returnValue = "deletion-failed.User-not-valid";
			}
			else if(result.equals("4") || result.equals("2")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"invalid_tonecode", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				logger.info("RBT::cannot delete the clip. response code is " + result
								+ ". But taking as success");
				// rbtDBManager.deleteClipForSubscriber(deleteSelection.subscriberId(),
				// deleteSelection.promoId());
				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"Daemon-invalid toneCode", false, -1, false,
						deleteSelection.categoryType() == DTMF_CATEGORY);
				rbtDBManager.deleteFromToBeDeletedSelections(deleteSelection);
				// rbtDBManager.deactivateSubscriberDownload(subscriberId, toneCode, "Daemon-invalid
				// toneCode");

				String actualDelSelSuccessfulSMS = Tools.findNReplaceAll(
						deleteSelectionSuccessfulSMS, "%TUNE%", name);
				if(sendSMS && !isOnePlusOneClip && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualDelSelSuccessfulSMS,
							insertSMSInDuplicate);
				returnValue = "success-as-cannot-delete";
			}
			else if(result.equals("8")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"portal_error", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				logger.info("RBT::The user "
										+ subscriberId
										+ " request of deleting song was not sucessful, portal-error. Will try in the next loop");
				returnValue = "portal-error";
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_DELETE_SELECTION", subscriberId, userType, "delete_selection",
						"error_response", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				logger.info("RBT::The user "
						+ subscriberId
						+ " request of deleting song was not sucessful, will try in the next loop");
				returnValue = "notok";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String deleteSettingForSubscriber(SubscriberStatus deleteSetting) {
		String result = null;
		String returnValue = null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			String subscriberId = deleteSetting.subID();
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			int setType = 1;
			if(deleteSetting.callerID() != null)
				setType = 2;

			String[] loopNoNToneGprId = new String[2];
			String response = querySongOrMBSetting(subscriberId, setType, deleteSetting
					.subscriberFile(), loopNoNToneGprId);
			if(response.equalsIgnoreCase("general_error")) {
				logger.info("RBT:: Failed to get LoopNoNToneGprId");
			}
			else if(response.equalsIgnoreCase("setting_not_exist")) {
				logger.info("RBT:: Setting does not exist");

				rbtDBManager.deactivateSubWavFileForCaller(subscriberId, deleteSetting.callerID(),
						deleteSetting.subscriberFile(), STATE_DEACTIVATED, deleteSetting
								.deSelectedBy(), "n");
				// removeToBeDeletedSetting(subscriberId, deleteSetting.callerID(),
				// deleteSetting.subscriberFile());
				Tools.writeToBeDeletedSettingToSDRFile(subscriberId, userType, "setting_not_exist",
						deleteSetting.endTime(), new Date(), deleteSetting.callerID(),
						deleteSetting.subscriberFile());
			}
			else {
				logger.info("RBT:: LoopNoNToneGprId = "
						+ loopNoNToneGprId[0] + " and " + loopNoNToneGprId[1]);

				String urlstr = httpLink;
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_TONE_SETTING_PAGE", "");
				String deleteToneSettingPage = parameter.getValue();
				urlstr += deleteToneSettingPage;

				urlstr += TataUtility.getOperatorAccount(null) + "&";
				urlstr += TataUtility.getOperatorPassword(null) + "&";
				urlstr += "phonenumber=" + subscriberId + "&";
				urlstr += "tonegroupid=" + loopNoNToneGprId[1] + "&";
				urlstr += "loopno=" + loopNoNToneGprId[0] + "&";
				urlstr += TataUtility.getOperatorCode(null);

				RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

				Date requestedTimeStamp = new Date();
				result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
				Date responseTimeStamp = new Date();

				long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

				String requestedTimeString = formatter.format(requestedTimeStamp);

				logger.info("RBT::deleteSettingForSubscriber result for subscriber " + subscriberId
								+ " is:: " + result);

				if(result == null) {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DELETE_SETTING", subscriberId, userType, "delete_setting",
							"null_error_response", requestedTimeString, differenceTime + "", app,
							urlstr, result);
					return null;
				}
				result = result.trim();

				if(result.equals("0")) {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DELETE_SETTING", subscriberId, userType, "delete_setting",
							"success", requestedTimeString, differenceTime + "", app, urlstr,
							result);
					logger.info("RBT::The deleteSettingForSubscriber for the user " + subscriberId
									+ " is successful updating the database");

					rbtDBManager.deactivateSubWavFileForCaller(subscriberId, deleteSetting
							.callerID(), deleteSetting.subscriberFile(), STATE_DEACTIVATED,
							deleteSetting.deSelectedBy(), "n");
					// removeToBeDeletedSetting(subscriberId, deleteSetting.callerID(),
					// deleteSetting.subscriberFile());
					Tools.writeToBeDeletedSettingToSDRFile(subscriberId, userType,
							"deleted_setting", deleteSetting.endTime(), new Date(), deleteSetting
									.callerID(), deleteSetting.subscriberFile());

					if(deleteSetting.status() == 3) {
						String name = "";
						try {
							String deSelectedBy = deleteSetting.deSelectedBy();
							String promoId = deSelectedBy.substring(deSelectedBy.indexOf(':') + 1);
							Clips clip = rbtDBManager.getClipByPromoID(promoId);
							name = clip.name();
						}
						catch(NullPointerException npe) {
							logger.error("", npe);
						}
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_SETTING_SUCCESS_SMS", "");
						String deleteSettingSuccessfulSMS = parameter.getValue();
						String actualDelSetSuccessfulSMS = Tools.findNReplaceAll(
								deleteSettingSuccessfulSMS, "%TUNE%", name);
						if(sendSMS && subscriber.rbtType() != 10)
							Tools.sendSMS(db_url, m_smsNo, subscriberId, actualDelSetSuccessfulSMS,
									insertSMSInDuplicate);
					}
				}
				else if(result.equals("4")) {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DELETE_SETTING", subscriberId, userType, "delete_setting",
							"invalid_tonegroupid/loopno", requestedTimeString, differenceTime + "",
							app, urlstr, result);
					logger.info("RBT::cannot delete the setting. response code is " + result
									+ ". But taking as success");

					rbtDBManager.deactivateSubWavFileForCaller(subscriberId, deleteSetting
							.callerID(), deleteSetting.subscriberFile(), STATE_DEACTIVATED,
							deleteSetting.deSelectedBy(), "n");
					// removeToBeDeletedSetting(subscriberId, deleteSetting.callerId(),
					// deleteSetting.promoId());
					Tools.writeToBeDeletedSettingToSDRFile(subscriberId, userType,
							"invalid_tonegroupid/loopno", deleteSetting.endTime(), new Date(),
							deleteSetting.callerID(), deleteSetting.subscriberFile());

					if(deleteSetting.status() == 3) {
						String name = "";
						try {
							String promoId = deleteSetting.subscriberFile();
							Clips clip = rbtDBManager.getClipByPromoID(promoId);
							name = clip.name();
						}
						catch(NullPointerException npe) {
							logger.error("", npe);
						}
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DELETE_SETTING_SUCCESS_SMS", "");
						String deleteSettingSuccessfulSMS = parameter.getValue();
						String actualDelSetSuccessfulSMS = Tools.findNReplaceAll(
								deleteSettingSuccessfulSMS, "%TUNE%", name);
						if(sendSMS && subscriber.rbtType() != 10)
							Tools.sendSMS(db_url, m_smsNo, subscriberId, actualDelSetSuccessfulSMS,
									insertSMSInDuplicate);
					}
				}
				else if(result.equals("8")) {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DELETE_SETTING", subscriberId, userType, "delete_setting",
							"portal_error", requestedTimeString, differenceTime + "", app, urlstr,
							result);
					logger.info("RBT::The user "
											+ subscriberId
											+ " request of deleting setting was not sucessful, portal-error. Will try in the next loop");
					returnValue = "portal-error";
				}
				else {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_DELETE_SETTING", subscriberId, userType, "delete_setting",
							"error_response", requestedTimeString, differenceTime + "", app,
							urlstr, result);
					logger.info("RBT::The user "
											+ subscriberId
											+ " request of deleting setting was not sucessful, will try in the next loop");
					returnValue = "notok";
				}
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String updateToDeactivate(Subscriber subscriber) {
		String result = null;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber updatedSubscriber = rbtDBManager.getSubscriber(subscriber.subID());
		CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(updatedSubscriber.cosID());

		boolean condition = (updatedSubscriber.subYes().equals(STATE_EVENT)
				|| (updatedSubscriber.subYes().equals(STATE_ACTIVATED) && cos.isDefaultCos()) || (updatedSubscriber
				.subYes().equals(STATE_ACTIVATED) && !cos.renewalAllowed()))
				&& !isSubscriberBulkActivated(updatedSubscriber);

		if(condition) {
			rbtDBManager.deactivateSubscriber(subscriber.subID(), "Daemon", new Date(), true, true,
					true);
			logger.info("RBT::" + subscriber.subID() + "'s state set to "
					+ STATE_TO_BE_DEACTIVATED);
			result = "user-deactivated";
		}
		else if(updatedSubscriber.subYes().equals(STATE_ACTIVATED)) {
			CosDetails renewalCos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cos.getRenewalCosid());
			Date endDate = null;

			Calendar cal = Calendar.getInstance();
			if(renewalCos.isDefaultCos()) {
				cal.set(2037, 0, 1, 0, 0, 0);
				endDate = cal.getTime();
			}
			else {
				cal.setTime(subscriber.endDate());
				cal.add(Calendar.DATE, renewalCos.getValidDays() - 1);
				endDate = cal.getTime();
			}

			rbtDBManager.updateSubscriber(subscriber.subID(), subscriber.activatedBy(), null,
					subscriber.startDate(), endDate, subscriber.prepaidYes() ? "y" : "n",
					subscriber.accessDate(), subscriber.nextChargingDate(), subscriber
							.maxSelections(), subscriber.noOfAccess(), subscriber.activationInfo(),
					subscriber.subscriptionClass(), subscriber.activationDate(), updatedSubscriber
							.subYes(), subscriber.lastDeactivationInfo(), subscriber
							.lastDeactivationDate(), cos.getRenewalCosid(), cos.getCosId());
			result = "use-subscription-continued";
		}

		return result;
	}

	public String sendBulkPromoSMS(BulkPromoSMS promoSMS, TATARBTDaemonOzonized ozoneThread) {
		logger.info("RBT::inside sendBulkPromoSMS");
		try {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
			String promoSMSFilePath = parameter.getValue();
			String dirPath = promoSMSFilePath;

			File dir = new File(dirPath);
			if(!dir.exists())
				dir.mkdirs();

			String fileName = null;
			String flNameForSearchGen = promoSMS.getBulkpromoID() + "_promoSMS_";
			String flNameForSearchSpec = promoSMS.getBulkpromoID() + "_promoSMS_"
					+ onlyDayformatter.format(new Date());

			logger.info("RBT::flNameForSearchSpec = "
					+ flNameForSearchSpec);

			Subscriber[] allbulkSubscribers = rbtDBManager.getBulkPromoSubscribers(promoSMS
					.getBulkpromoID());

			if(allbulkSubscribers != null) {
				File[] files = dir.listFiles();

				if(files != null) {
					for(int i = 0; i < files.length; i++) {
						int fndGen = (files[i].getName()).indexOf(flNameForSearchGen);
						if(files[i].isFile() && fndGen >= 0) {
							logger.info("RBT::files[" + i
									+ "].getName() = " + files[i].getName());
							int fndSpe = (files[i].getName()).indexOf(flNameForSearchSpec);
							if(fndSpe >= 0) {
								fileName = files[i].getName();
								break;
							}
							else {
								try {
									logger.info("RBT::deleting file " + files[i].getName());
									if(files[i].delete())
										logger.info("RBT::deleted file " + files[i].getName());
									else
										logger.info("RBT::deletion of file " + files[i].getName()
														+ " failed");
								}
								catch(Exception e) {
									logger.error("", e);
								}
							}
						}
					}
				}
				if(fileName == null) {
					fileName = promoSMS.getBulkpromoID() + "_promoSMS_"
							+ fileNameFormatter.format(new Date()) + ".txt";
					logger.info("RBT::creating new file "
							+ fileName);
				}
				String filepath = dirPath + File.separator + fileName;
				logger.info("RBT::filepath = " + filepath);

				File smsFile = new File(filepath);
				BulkPromo promo = RBTDBManager.getInstance().getActiveBulkPromo(
						promoSMS.getBulkpromoID());
				String cunDateStr = onlyDayformatter.format(new Date());
				String promoStartDate = onlyDayformatter.format(promo.promoStartDate());

				boolean sendBulkPromoSMS = false;
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_BULK_PROMO_SMS_ON_DAY1", "FALSE");
				boolean sendBulkPromoSMSOnDay1 = (new Boolean(parameter.getValue().trim())).booleanValue();
				if(!promoStartDate.equals(cunDateStr))
					sendBulkPromoSMS = true;
				else if(sendBulkPromoSMSOnDay1)
					sendBulkPromoSMS = true;

				if(sendBulkPromoSMS) {
					if(!smsFile.exists()) {
						for(int count = 0; count < allbulkSubscribers.length; count++) {
							if(allbulkSubscribers[count].subYes().equals(STATE_EVENT))
								addSubscriberToFile(filepath, allbulkSubscribers[count].subID());
						}
					}

					if(smsFile.exists()) {
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_PROMO_MESSAGE_ID", "BULKSMS_START");
						String promoSMSMsgID = parameter.getValue();
						String msgId = promoSMSMsgID;
						String data = "";
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", "10");
						int numOfPromSMSPerSec = Integer.parseInt(parameter.getValue().trim());
						int numberOfSMSPerSec = numOfPromSMSPerSec;
						String promoSMSCTemp = null;
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMSC", "");
						String promoSMSC = parameter.getValue();
						if(promoSMSC != null)
							promoSMSCTemp = promoSMSC;
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", "");
						String promoSMSStartTime = parameter.getValue();
						String smsStartTime = promoSMSStartTime;
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", "");
						String promoSMSEndTime = parameter.getValue();
						String smsEndTime = promoSMSEndTime;
						String smsEndDate = endDateformatter.format(promo.promoEndDate());

						data += "PROMO_NAME=blpro" + fileNameFormatter.format(new Date());
						data += "|PROMO_FILE=" + filepath;
						if(promoSMSCTemp != null)
							data += "|PROMO_SMSC=" + promoSMSCTemp;
						data += "|PROMO_TEXT=" + promoSMS.getSmsText();
						data += "|PROMO_START_DATE=" + promoSMS.getSmsDate();
						data += "|PROMO_START_TIME=" + smsStartTime;
						data += "|PROMO_END_DATE=" + smsEndDate;
						data += "|PROMO_END_TIME=" + smsEndTime;
						data += "|PROMO_SMS_PER_SEC=" + numberOfSMSPerSec;
						data += "|PROMO_SENDER=" + m_smsNo;
						data += "|PROMO_TEST_NUMBERS=";
						data += "|PROMO_STATUS_INTERVAL=10";
						data += "|PROMO_TEST_MODE=FALSE";
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_TYPE", "text");
						String promoSMSType = parameter.getValue();
						data += "|PROMO_MESSAGE_TYPE=" + promoSMSType;

						if(ozoneThread.createInfoMessage(msgId, data))
							rbtDBManager.updateSMSSent(promoSMS.getBulkpromoID(), promoSMS.getSmsDate(),
									"y");
					}

				}
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return null;
	}

	public String sendBulkActivationMessage(BulkPromo promo, TATARBTDaemonOzonized ozoneThread) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
		String promoSMSFilePath = parameter.getValue();
		String dirPath = promoSMSFilePath;

		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();

		String fileName = getFileNameForBulkPromo(promo.bulkPromoId(), "activated", false);
		if(fileName == null)
			fileName = getFileNameForBulkPromo(promo.bulkPromoId(), "selection", false);

		String filePath = dirPath + File.separator + fileName;
		File smsFile = new File(filePath);
		logger.info("RBT:: sms file path " + filePath);
		if(smsFile.exists()) {
			logger.info("RBT::activation sms file exists for "
					+ promo.bulkPromoId());
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_PROMO_MESSAGE_ID", "BULKSMS_START");
			String promoSMSMsgID = parameter.getValue();
			String msgId = promoSMSMsgID;
			String data = "";
			BulkPromoSMS promoSMS = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMS(promo.bulkPromoId(), -1);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", "10");
			int numOfPromSMSPerSec = Integer.parseInt(parameter.getValue().trim());
			int numberOfSMSPerSec = numOfPromSMSPerSec;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", "");
			String promoSMSStartTime = parameter.getValue();
			String smsStartTime = promoSMSStartTime;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", "");
			String promoSMSEndTime = parameter.getValue();
			String smsEndTime = promoSMSEndTime;
			String smsEndDate = endDateformatter.format(promo.promoEndDate());

			String dateString = fileNameFormatter.format(new Date());

			data += "PROMO_NAME=blact" + fileNameFormatter.format(new Date());
			data += "|PROMO_FILE=" + filePath;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMSC", "");
			String promoSMSC = parameter.getValue();
			if(promoSMSC != null)
				data += "|PROMO_SMSC=" + promoSMSC;
			data += "|PROMO_TEXT=" + promoSMS.getSmsText();
			data += "|PROMO_START_DATE=" + dateString;
			data += "|PROMO_START_TIME=" + smsStartTime;
			data += "|PROMO_END_DATE=" + smsEndDate;
			data += "|PROMO_END_TIME=" + smsEndTime;
			data += "|PROMO_SMS_PER_SEC=" + numberOfSMSPerSec;
			data += "|PROMO_SENDER=" + m_smsNo;
			data += "|PROMO_TEST_NUMBERS=";
			data += "|PROMO_STATUS_INTERVAL=10";
			data += "|PROMO_TEST_MODE=FALSE";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_TYPE", "text");
			String promoSMSType = parameter.getValue();
			data += "|PROMO_MESSAGE_TYPE=" + promoSMSType;

			if(ozoneThread.createInfoMessage(msgId, data)) {
				rbtDBManager.updateSMSSent(promoSMS.getBulkpromoID(), promoSMS.getSmsDate(), "y");
				return "processed-and-sent";
			}
			else
				return "processed-and-not-sent";
		}
		return "no-file-to-process";
	}

	public String sendBulkDeactivationMessage(BulkPromo promo, TATARBTDaemonOzonized ozoneThread) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
		String promoSMSFilePath = parameter.getValue();
		String dirPath = promoSMSFilePath;

		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();

		String fileName = getFileNameForBulkPromo(promo.bulkPromoId(), "deactivated", false);

		String filePath = dirPath + File.separator + fileName;
		File smsFile = new File(filePath);

		if(smsFile.exists()) {
			logger.info("RBT::deactivation sms file exists for "
					+ promo.bulkPromoId());
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_PROMO_MESSAGE_ID", "BULKSMS_START");
			String promoSMSMsgID = parameter.getValue();
			String msgId = promoSMSMsgID;
			String data = "";
			BulkPromoSMS promoSMS = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMS(promo.bulkPromoId(), -2);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", "10");
			int numOfPromSMSPerSec = Integer.parseInt(parameter.getValue().trim());
			int numberOfSMSPerSec = numOfPromSMSPerSec;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", "");
			String promoSMSStartTime = parameter.getValue();
			String smsStartTime = promoSMSStartTime;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", "");
			String promoSMSEndTime = parameter.getValue();
			String smsEndTime = promoSMSEndTime;
			String dateString = fileNameFormatter.format(new Date());
			String smsEndDate = endDateformatter.format(promo.promoEndDate());

			data += "PROMO_NAME=bldct" + fileNameFormatter.format(new Date());
			
			data += "|PROMO_FILE=" + filePath;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMSC", "");
			String promoSMSC = parameter.getValue();
			if(promoSMSC != null)
				data += "|PROMO_SMSC=" + promoSMSC;
			data += "|PROMO_TEXT=" + promoSMS.getSmsText();
			data += "|PROMO_START_DATE=" + dateString;
			data += "|PROMO_START_TIME=" + smsStartTime;
			data += "|PROMO_END_DATE=" + smsEndDate;
			data += "|PROMO_END_TIME=" + smsEndTime;
			data += "|PROMO_SMS_PER_SEC=" + numberOfSMSPerSec;
			data += "|PROMO_SENDER=" + m_smsNo;
			data += "|PROMO_TEST_NUMBERS=";
			data += "|PROMO_STATUS_INTERVAL=10";
			data += "|PROMO_TEST_MODE=FALSE";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_TYPE", "text");
			String promoSMSType = parameter.getValue();
			data += "|PROMO_MESSAGE_TYPE=" + promoSMSType;

			if(ozoneThread.createInfoMessage(msgId, data)) {
				rbtDBManager.updateSMSSent(promoSMS.getBulkpromoID(), promoSMS.getSmsDate(), "y");
				return "processed-and-sent";
			}
			else
				return "processed-and-not-sent";
		}
		return "no-file-to-process";
	}

	public String sendBulkCCDeactivationMessage(TATARBTDaemonOzonized ozoneThread) {
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
		String promoSMSFilePath = parameter.getValue();
		String dirPath = promoSMSFilePath;

		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();

		String fileName = getFileNameForBulkPromo("CC", "deactivated", false);

		String filePath = dirPath + File.separator + fileName;
		File aFile = new File(filePath);
		if(aFile.exists()) {
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_PROMO_MESSAGE_ID", "BULKSMS_START");
			String promoSMSMsgID = parameter.getValue();
			String msgId = promoSMSMsgID;
			String data = "";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DEACTIVATION_BY_CC_SMS", "");
			String deactivationByCCSMS = parameter.getValue();
			String smsText = deactivationByCCSMS;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", "10");
			int numOfPromSMSPerSec = Integer.parseInt(parameter.getValue().trim());
			int numberOfSMSPerSec = numOfPromSMSPerSec;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", "");
			String promoSMSStartTime = parameter.getValue();
			String smsStartTime = promoSMSStartTime;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", "");
			String promoSMSEndTime = parameter.getValue();
			String smsEndTime = promoSMSEndTime;
			String dateString = fileNameFormatter.format(new Date());

			Calendar endCal = Calendar.getInstance();
			endCal.add(Calendar.DAY_OF_YEAR, 1);

			String smsEndDate = endDateformatter.format(endCal.getTime());

			data += "PROMO_NAME=ccdct" + fileNameFormatter.format(new Date());
			data += "|PROMO_FILE=" + filePath;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMSC", "");
			String promoSMSC = parameter.getValue();
			if(promoSMSC != null)
				data += "|PROMO_SMSC=" + promoSMSC;
			data += "|PROMO_TEXT=" + smsText;
			data += "|PROMO_START_DATE=" + dateString;
			data += "|PROMO_START_TIME=" + smsStartTime;
			data += "|PROMO_END_DATE=" + smsEndDate;
			data += "|PROMO_END_TIME=" + smsEndTime;
			data += "|PROMO_SMS_PER_SEC=" + numberOfSMSPerSec;
			data += "|PROMO_SENDER=" + m_smsNo;
			data += "|PROMO_TEST_NUMBERS=";
			data += "|PROMO_STATUS_INTERVAL=10";
			data += "|PROMO_TEST_MODE=FALSE";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_TYPE", "text");
			String promoSMSType = parameter.getValue();
			data += "|PROMO_MESSAGE_TYPE=" + promoSMSType;

			if(ozoneThread.createInfoMessage(msgId, data))
				return "processed-and-sent";
			else
				return "processed-and-not-sent";
		}
		return "no-file-to-process";
	}

	// public boolean downloadSongForUser(SubscriberStatus addClipForSubscriber)
	private boolean downloadSongForUser(SubscriberStatus addClipForSubscriber, int validityOfSong) {
		boolean returnValue = false;
		boolean isOnePlusOneClip = addClipForSubscriber.selectionInfo().indexOf("1+1") != -1;
		if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
			returnValue = giftSong(addClipForSubscriber, validityOfSong);
			return returnValue;
		}

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS_TO_RETAILER", "");
		String selectionFailedSMSToRetailer = parameter.getValue();
		try {
			String eventType = "RBT_DOWNLOAD_TONE";
			logger.info("RBT::Inside try...");
			String subscriberId = addClipForSubscriber.subID();
			String toneCode = addClipForSubscriber.subscriberFile();

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			boolean isSelectionBulkActivated = isSelectionBulkActivated(addClipForSubscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstrForDownload = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DOWNLOAD_TONE_PAGE", "");
			String downloadTonePage = parameter.getValue();
			urlstrForDownload += downloadTonePage;

			String opAcc = TataUtility.getOperatorAccount(null);
			String opPass = TataUtility.getOperatorPassword(null);
			String op = TataUtility.getOperatorCode(null);
			if(isSelectionBulkActivated) {
				CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
				opAcc = TataUtility.getOperatorAccount(cos);
				opPass = TataUtility.getOperatorPassword(cos);
				op = TataUtility.getOperatorCode(cos);
			}

			urlstrForDownload += opAcc + "&";
			urlstrForDownload += opPass + "&";
			urlstrForDownload += "phonenumber=" + subscriberId + "&";
			urlstrForDownload += "tonecode=" + toneCode + "&";
			urlstrForDownload += op;
			//appending mmno RBT-3949
			if(addClipForSubscriber.selectionInfo() != null) {
				String info = addClipForSubscriber.selectionInfo();
				int index = info.indexOf("|");
				if(index != -1)
					info = info.substring(0, index);
				urlstrForDownload += "&mmno=" + info;
			}
			
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(addClipForSubscriber.classType());
			logger.info(" ChargeClass:"+chargeClass+" to append songprice and song validity ");
			if (null != chargeClass) {
				String songPrice = chargeClass.getAmount();
				String songValidity = convertValidityPeriodToDays(chargeClass
						.getSelectionPeriod());
				urlstrForDownload += "&songprice=" + songPrice;
				urlstrForDownload += "&songvalidity=" + songValidity;
				logger.info(" song price:"+songPrice+", song validity: "+songValidity+" successfully appended to the URL");
			}
			

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstrForDownload, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
				result = result.trim();
			
			if((result == null || !result.equals("15"))
					&& addClipForSubscriber.selStatus().equals(STATE_GRACE))
				writeGraceSDR("RBT_DOWNLOAD_SONG", subscriberId, "download-song:" + toneCode, result);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				return false;
			}

			logger.info("RBT::the result code for subscriber"
					+ subscriberId + " for the tone " + toneCode + " is " + result);
			if(result.equals("9") || result.equals("99")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "provisional_success",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'p'");
				rbtDBManager.updateDownloadStatus(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), 'p');
				returnValue = true;
			}
			else if(result.equals("0")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "success", requestedTimeString,
						differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'y'");
				rbtDBManager.updateDownloadStatusToDownloaded(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), new Date(), validityOfSong);
				// if(addClipForSubscriber.selectedBy().indexOf("PROMO") >= 0)
				// rbtDBManager.setAvailedPromoDownload(addClipForSubscriber.subID(), true);

				returnValue = true;
			}
			else if(result.equals("6")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "already_exists",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::Tone " + toneCode
						+ " already exist in subscriber" + subscriberId + " library");
				rbtDBManager.updateDownloadStatusToDownloaded(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), new Date(), validityOfSong);

				String callerID = addClipForSubscriber.callerID();
				if(callerID == null || callerID.equalsIgnoreCase("null")) {
					rbtDBManager.deleteClipForSubscriber(subscriberId, addClipForSubscriber
							.subscriberFile());
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SETTING_FAILED_SMS_FOR_TONE_ALREADY_EXISTS", "");
					String settingFailedSMSForToneAlreadyExist = parameter.getValue();
					String tmpSMS = settingFailedSMSForToneAlreadyExist;
					Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber
							.subscriberFile());
					tmpSMS = Tools.findNReplace(tmpSMS, "%TUNE%", clip.name());
					if(sendSMS && !isOnePlusOneClip && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, tmpSMS, insertSMSInDuplicate);

					returnValue = false;
				}
				else
					returnValue = true;
			}
			else if(result.equals("4") || result.equals("2")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "song_doesnt_exist",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::deleting the song " + toneCode
						+ " for subscriber " + subscriberId
						+ " from database, as the song doesn't exist in huwai library");
				/*
				 * rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile()); //
				 * rbtDBManager.addToDeletedSelections(addClipForSubscriber); String circleId =
				 * rbtDBManager.getCircleId(subscriberId); Clips clip =
				 * rbtDBManager.getClipPromoID(addClipForSubscriber.subscriberFile()); Categories
				 * category = rbtDBManager.getCategory(addClipForSubscriber.categoryID(), circleId,
				 * addClipForSubscriber.prepaidYes()?'y':'n');
				 * rbtDBManager.removeCategoryClip(category, clip);
				 * rbtDBManager.removePickOfTheday(clip.id());
				 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile(), "Daemon-Cannot Download");
				 * //rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				 * addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				 * addClipForSubscriber.startTime(), "not in huwaii");
				 * Tools.writeSelectionToSDRFile(subscriberId, userType, "song_not_in_huawei",
				 * addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(), toneCode);
				 */

				/*
				 * int maxSelection = subscriber.maxSelections() - 1; if(maxSelection < 0)
				 * maxSelection = 0; rbtDBManager.updateNumMaxSelections(subscriberId,
				 * maxSelection);
				 */

				deleteSubscriberSelection(subscriberId, toneCode, null, true, "song_not_in_huawei",
						true, addClipForSubscriber.categoryID(), addClipForSubscriber.prepaidYes(),
						true);

				decrementSubMaxSelection(subscriber, 1);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "song_not_in_huawei",
						addClipForSubscriber.startTime(), null, addClipForSubscriber.callerID(),
						toneCode);

				Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
				String selectionFailedSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						clip.name());
				if(sendSMS && !isOnePlusOneClip && !isSelectionBulkActivated
						&& subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated && subscriber.rbtType() != 10) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tone",
							"song_doesnt_exist", requestedTimeString, differenceTime + "", app,
							urlstrForDownload, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
						&& !isOnePlusOneClip)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, clip.name(), null, null);
				returnValue = false;
			}
			else if(result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "user_library_full",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::deleting the song " + toneCode
						+ " for subscriber " + subscriberId
						+ " from database, as the user's personal library is full");
				/*
				 * rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile()); //
				 * rbtDBManager.addToDeletedSelections(addClipForSubscriber);
				 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile(), "Daemon-cannot download lib full");
				 * //rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				 * addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				 * addClipForSubscriber.startTime(), "library full");
				 * Tools.writeSelectionToSDRFile(subscriberId, userType, "user_library_full",
				 * addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(), toneCode);
				 */

				/*
				 * int maxSelection = subscriber.maxSelections() - 1; if(maxSelection < 0)
				 * maxSelection = 0; rbtDBManager.updateNumMaxSelections(subscriberId,
				 * maxSelection);
				 */

				deleteSubscriberSelection(subscriberId, toneCode, null, true, "user_library_full",
						false, -1, addClipForSubscriber.prepaidYes(), true);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_library_full",
						addClipForSubscriber.startTime(), null, addClipForSubscriber.callerID(),
						toneCode);

				decrementSubMaxSelection(subscriber, 1);

				Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "CLIP_LIBRARY_FULL_SMS", "");
				String clipLibraryFullSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(clipLibraryFullSMS, "%TUNE%",
						clip.name());
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated && subscriber.rbtType() != 10) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tone",
							"user_library_full", requestedTimeString, differenceTime + "", app,
							urlstrForDownload, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
						& !isOnePlusOneClip)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, clip.name(), null, null);
				returnValue = false;
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "user_not_valid",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::user is not registered user deleting from database");
				RBTDBManager.getInstance().deactivateSubscriberForTATA(subscriberId);
				returnValue = false;
			}
			else if(result.equals("14")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "disabled_in_user's_cos",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::song download is disabled in user's cos");

				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"disabled_in_user's_cos", false, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), true);

				decrementSubMaxSelection(subscriber, 1);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "disabled_in_user's_cos",
						addClipForSubscriber.startTime(), null, addClipForSubscriber.callerID(),
						toneCode);

				Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
				String selectionFailedSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						clip.name());
				if(sendSMS && !isOnePlusOneClip && !isSelectionBulkActivated
						&& subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated && subscriber.rbtType() != 10) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tone",
							"disabled_in_user's_cos", requestedTimeString, differenceTime + "",
							app, urlstrForDownload, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
						&& !isOnePlusOneClip)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, clip.name(), null, null);
				returnValue = false;
			}
			else if(result.equals("15")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "insufficient_balance",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::insufficient_balance, download failed");
				int index = getNextIndex(null, addClipForSubscriber);
				if(index == -1) {
					writeGraceSDR(eventType, subscriberId, "download-song:" + toneCode,
							result + " - deactivating");
					deleteSubscriberSelection(subscriberId, toneCode, null, true,
							"insufficient_balance", false, addClipForSubscriber.categoryID(),
							addClipForSubscriber.prepaidYes(), true);

					decrementSubMaxSelection(subscriber, 1);

					Tools.writeSelectionToSDRFile(subscriberId, userType, "insufficient_balance",
							addClipForSubscriber.startTime(), null,
							addClipForSubscriber.callerID(), toneCode);

					Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber
							.subscriberFile());
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
					String selectionFailedSMS = parameter.getValue();
					String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
							clip.name());
					if(sendSMS && !isOnePlusOneClip && !isSelectionBulkActivated
							&& subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
								insertSMSInDuplicate);
					if(isSelectionBulkActivated && subscriber.rbtType() != 10) {
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_sel_failure", rotationSize, eventType, subscriberId,
								userType, "download_tone", "insufficient_balance",
								requestedTimeString, differenceTime + "", app, urlstrForDownload,
								result);
					}
					if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1
							&& !isOnePlusOneClip)
						sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
								selectionFailedSMSToRetailer, clip.name(), null, null);
				}
				else {
					Calendar nextRetryCal = Calendar.getInstance();
					Date nextChargingDate = addClipForSubscriber.nextChargingDate();
					if(nextChargingDate != null)
						nextRetryCal.setTime(nextChargingDate);
					nextRetryCal
							.add(Calendar.HOUR, Integer.parseInt(graceIntervalHours.get(index)));
					logger.info("RBT::updating user activation to grace");
					writeGraceSDR("RBT_DOWNLOAD_SONG", subscriberId, "download-song:" + toneCode,
							result + " - moving_to_grace");
					if(!rbtDBManager.updateSelectionToGrace(addClipForSubscriber.subID(),
							addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
							nextRetryCal.getTime(), getGraceInfo(addClipForSubscriber
									.selectionInfo(), index)))
						logger.info("RBT::updating subscriber to grace failed");
				}
				returnValue = false;
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "download_tone", "error_response",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::got invalid response will try in next loop");
				returnValue = false;
			}
		}
		catch(Throwable e) {
			logger.error("", e);
		}
		return returnValue;
	}

	// public boolean downloadMusicboxForUser(SubscriberStatus addClipForSubscriber)
	public boolean downloadMusicboxForUser(SubscriberStatus addClipForSubscriber, int validityOfMB) {
		boolean returnValue = false;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
		String selectionFailedSMS = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS_TO_RETAILER", "");
		String selectionFailedSMSToRetailer = parameter.getValue();
		try {
			String eventType = "RBT_DOWNLOAD_TONEBOX";
			logger.info("RBT::Inside try...");
			String subscriberId = addClipForSubscriber.subID();
			String toneCode = addClipForSubscriber.subscriberFile();

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			String circleId = rbtDBManager.getCircleId(subscriberId);
			boolean isSelectionBulkActivated = isSelectionBulkActivated(addClipForSubscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			Categories musicbox = rbtDBManager.getCategoryPromoID(toneCode, circleId, subscriber
					.prepaidYes() ? 'y' : 'n');
			String name = musicbox.name();

			String urlstrForDownloadtonebox = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "DOWNLOAD_TONEBOX_PAGE", "");
			String downloadToneboxPage = parameter.getValue();
			urlstrForDownloadtonebox += downloadToneboxPage;

			String opAcc = TataUtility.getOperatorAccount(null);
			String opPass = TataUtility.getOperatorPassword(null);
			String op = TataUtility.getOperatorCode(null);
			if(isSelectionBulkActivated) {
				CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subscriber.cosID());
				opAcc = TataUtility.getOperatorAccount(cos);
				opPass = TataUtility.getOperatorPassword(cos);
				op = TataUtility.getOperatorCode(cos);
			}

			urlstrForDownloadtonebox += opAcc + "&";
			urlstrForDownloadtonebox += opPass + "&";
			urlstrForDownloadtonebox += "phonenumber=" + subscriberId + "&";
			urlstrForDownloadtonebox += "tonegroupid=" + toneCode + "&";
			urlstrForDownloadtonebox += op;
			//appending mmno RBT-3949
			if(addClipForSubscriber.selectionInfo() != null)
				urlstrForDownloadtonebox += "&mmno=" + addClipForSubscriber.selectionInfo();

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstrForDownloadtonebox, subscriberId,
					app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"null_error_response", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				return false;
			}

			result = result.trim();

			logger.info("RBT::the result code for subscriber" + subscriberId + " for the tone "
							+ toneCode + " is " + result);

			if(result.equals("0")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"success", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'y'");
				rbtDBManager.updateDownloadStatusToDownloaded(subscriberId, toneCode, new Date(),
						validityOfMB);
				// if(addClipForSubscriber.selectedBy().indexOf("PROMO") >= 0)
				// rbtDBManager.setAvailedPromoDownload(addClipForSubscriber.subID(), true);
				returnValue = true;
			}
			else if(result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"already_exists", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'y'");
				rbtDBManager.updateDownloadStatusToDownloaded(subscriberId, toneCode, new Date(),
						validityOfMB);

				String callerID = addClipForSubscriber.callerID();
				if(callerID == null || callerID.equalsIgnoreCase("null")) {
					rbtDBManager.deleteClipForSubscriber(subscriberId, addClipForSubscriber
							.subscriberFile());
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SETTING_FAILED_SMS_FOR_TONEBOX_ALREADY_EXISTS", "");
					String settingFailedSMSForToneBoxAlreadyExist = parameter.getValue();
					String tmpSMS = settingFailedSMSForToneBoxAlreadyExist;
					tmpSMS = Tools.findNReplace(tmpSMS, "%TUNE%", name);
					if(sendSMS && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, tmpSMS, insertSMSInDuplicate);

					returnValue = false;
				}
				else
					returnValue = true;
			}
			else if(result.equals("99")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"provisional_success", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'p'");
				rbtDBManager.updateDownloadStatus(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), 'p');
				returnValue = true;
			}
			else if(result.equals("4") || result.equals("2")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"musicbox_doesnt_exist", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::deleting the song "
						+ toneCode + " for subscriber " + subscriberId
						+ ", as the song doesn't exist in huwai library");
				/*
				 * rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile()); //
				 * rbtDBManager.addToDeletedSelections(addClipForSubscriber);
				 * rbtDBManager.removeCategory(rbtDBManager.getCategoryPromoID(toneCode));
				 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile(), "Daemon-Not in bak end");
				 * //rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				 * addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				 * addClipForSubscriber.startTime(), "download failed");
				 */

				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"musicbox_doesnt_exist", true, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), false);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "musicbox_doesnt_exist",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						toneCode);
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tonebox",
							"musicbox_doesnt_exist", requestedTimeString, differenceTime + "", app,
							urlstrForDownloadtonebox, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, name, null, null);
				returnValue = false;
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"user_not_valid", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::user is not registered user deleting from database");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), "Daemon-invalid user");
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				// addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				// addClipForSubscriber.startTime(), "download failed");
				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_not_valid",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						toneCode);
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tonebox",
							"user_not_valid", requestedTimeString, differenceTime + "", app,
							urlstrForDownloadtonebox, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, name, null, null);
				returnValue = false;
			}
			else if(result.equals("7")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"user_library_full", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::deleting the song " + toneCode
						+ " for subscriber " + subscriberId
						+ " from database, as the user's personal library is full");
				/*
				 * rbtDBManager.deleteClipForSubscriber(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile()); //
				 * rbtDBManager.addToDeletedSelections(addClipForSubscriber);
				 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
				 * addClipForSubscriber.subscriberFile(), "Daemon-cannot download lib full");
				 * //rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				 * addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				 * addClipForSubscriber.startTime(), "library full");
				 */
				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"musicbox_doesnt_exist", false, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), false);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_library_full",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						toneCode);
				
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "MB_LIBRARY_FULL_SMS", "");
				String mbLibraryFullSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(mbLibraryFullSMS, "%TUNE%", name);
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tone",
							"user_library_full", requestedTimeString, differenceTime + "", app,
							urlstrForDownloadtonebox, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, name, null, null);
				returnValue = false;
			}
			else if(result.equals("14")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"disabled_in_user's_cos", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::musicbox download is disabled in user's cos");

				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"disabled_in_user's_cos", false, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), false);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "disabled_in_user's_cos",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						toneCode);
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tonebox",
							"disabled_in_user's_cos", requestedTimeString, differenceTime + "",
							app, urlstrForDownloadtonebox, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, name, null, null);
				returnValue = false;
			}
			else if(result.equals("15")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"insufficient_balance", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::insufficient_balance, download failed");

				deleteSubscriberSelection(subscriberId, toneCode, null, true,
						"insufficient_balance", false, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), false);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "insufficient_balance",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						toneCode);

				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSelectionBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSelectionBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							eventType, subscriberId, userType, "download_tonebox",
							"disabled_in_user's_cos", requestedTimeString, differenceTime + "",
							app, urlstrForDownloadtonebox, result);
				}
				if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
					sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
							selectionFailedSMSToRetailer, name, null, null);
				returnValue = false;
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						eventType, subscriberId, userType, "download_tonebox",
						"error_response", requestedTimeString, differenceTime + "", app,
						urlstrForDownloadtonebox, result);
				logger.info("RBT::invalid response will try in the next loop");
				returnValue = false;
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String[] querySubscriberClipsFromLibrary(String subscriberId, String name) {
		ArrayList<String> clipsList = new ArrayList<String>();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			logger.info("RBT::inside try.....");
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);
			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "QUERY_SONGS_PAGE", "");
			String querySongsPage = parameter.getValue();
			urlstr += querySongsPage;

			urlstr += TataUtility.getOperatorAccount(null);
			urlstr += TataUtility.getOperatorPassword(null);
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += TataUtility.getOperatorCode(null);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONGS",
						subscriberId, userType, "query_songs", "error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				return null;
			}

			result = result.trim();

			logger.info("RBT:: result = " + result);

			if(result.length() > 1) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONGS",
						subscriberId, userType, "query_songs", "success", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				StringTokenizer st = new StringTokenizer(result, "&");
				while(st.hasMoreTokens()) {
					String newString = st.nextToken();
					StringTokenizer tempStringTokenizer = new StringTokenizer(newString, "|");
					if(tempStringTokenizer.hasMoreTokens()) {
						String tempString = tempStringTokenizer.nextToken().trim();
						clipsList.add(tempString);
					}
				}
				String[] returnResult = clipsList.toArray(new String[0]);

				if(returnResult.length > 0)
					return returnResult;
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONGS",
						subscriberId, userType, "query_songs", "user_not_valid",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::user is not registered user deleting from database");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, null, name, new Date(),
				// "download failed, user not subscribed");
				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_not_valid", new Date(),
						null, null, name);
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
				String selectionFailedSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							"RBT_QUERY_SONGS", subscriberId, userType, "query_songs",
							"user_not_valid", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
			}
			else if(result.equals("4")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONGS",
						subscriberId, userType, "query_songs", "no_songs", requestedTimeString,
						differenceTime + "", app, urlstr, result);
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SONGS",
						subscriberId, userType, "query_songs", "error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return null;
	}

	public String[] querySubscriberMusicboxesFromLibrary(String subscriberId, String name) {
		ArrayList<String> musicboxesList = new ArrayList<String>();
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);
			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			logger.info("RBT::inside try.....");
			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "QUERY_MUSICBOXES_PAGE", "");
			String queryMusicboxesPage = parameter.getValue();
			urlstr += queryMusicboxesPage;

			urlstr += TataUtility.getOperatorAccount(null);
			urlstr += TataUtility.getOperatorPassword(null);
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += TataUtility.getOperatorCode(null);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
						"error_response", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				return null;
			}

			result = result.trim();

			logger.info("RBT:: result = "
					+ result);

			if(result.length() > 1) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
						"success", requestedTimeString, differenceTime + "", app, urlstr, result);
				StringTokenizer st = new StringTokenizer(result, "&");
				while(st.hasMoreTokens()) {
					String newString = st.nextToken();
					StringTokenizer tempStringTokenizer = new StringTokenizer(newString, "|");
					if(tempStringTokenizer.hasMoreTokens()) {
						String tempString = tempStringTokenizer.nextToken().trim();
						musicboxesList.add(tempString);
					}
				}
				String[] returnResult = musicboxesList.toArray(new String[0]);

				if(returnResult.length > 0)
					return returnResult;
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
						"user_not_valid", requestedTimeString, differenceTime + "", app, urlstr,
						result);
				logger.info("RBT::user is not registered user deleting from database");

				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId, null, name, new Date(),
				// "download failed, user not subscribed");
				Tools.writeSelectionToSDRFile(subscriberId, userType, "user_not_valid", new Date(),
						null, null, name);
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
				String selectionFailedSMS = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
						name);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
							insertSMSInDuplicate);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_sel_failure", rotationSize,
							"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
							"user_not_valid", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
			}
			else if(result.equals("4")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
						"no_musicboxes", requestedTimeString, differenceTime + "", app, urlstr,
						result);
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_MUSIXBOXES", subscriberId, userType, "query_musicboxes",
						"error_reponse", requestedTimeString, differenceTime + "", app, urlstr,
						result);
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}

		return null;
	}

	public String querySubscriberStatus(String urlstr, String subscriberId) {
		String result = null;
		try {
			logger.info("RBT::inside try.......");

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);

			logger.info("RBT::subscriber status result for "
					+ subscriberId + " is:: " + result);

			if(result != null)
				result = result.trim();
			return result;
		}
		catch(Exception e) {
			logger.error("", e);
			return null;
		}
	}

	// public boolean queryDownloadStatus(SubscriberStatus addClipForSubscriber, int resourceFlag)
	public boolean queryDownloadStatus(SubscriberStatus addClipForSubscriber, int resourceFlag,
			int validityOfTone) {
		String result = null;
		boolean returnValue = false;

		String subscriberId = null;
		String resourceId = null;
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS", "");
		String selectionFailedSMS = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SELECTION_FAILED_SMS_TO_RETAILER", "");
		String selectionFailedSMSToRetailer = parameter.getValue();
		try {
			logger.info("RBT::inside try.......");

			subscriberId = addClipForSubscriber.subID();
			resourceId = addClipForSubscriber.subscriberFile();

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "QUERY_DOWNLOAD_STATUS_PAGE", "");
			String queryDownloadStatusPage = parameter.getValue();
			urlstr += queryDownloadStatusPage;

			urlstr += TataUtility.getOperatorAccount(null) + "&";
			urlstr += TataUtility.getOperatorPassword(null) + "&";
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += "resourceid=" + resourceId + "&";
			urlstr += "resourceflag=" + resourceFlag + "&";
			urlstr += TataUtility.getOperatorCode(null);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "null_error_reponse", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				return false;
			}

			result = result.trim();

			if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "user_not_valid", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::cannot take any action");
			}
			else if(result.equals("4")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "invalid_resource_id", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), "Daemon-invalid toneCode");
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				// addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				// addClipForSubscriber.startTime(), "download failed, resource not found");
				Tools.writeSelectionToSDRFile(subscriberId, userType, "invalid_resource_id",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						addClipForSubscriber.subscriberFile());

				String actualSelFailedSMS;

				String name;
				deleteSubscriberSelection(subscriberId, resourceId, null, true,
						"Daemon-not in bakend", true, addClipForSubscriber.categoryID(),
						addClipForSubscriber.prepaidYes(), resourceFlag == 0);

				if(resourceFlag == 0) {
					logger.info("RBT::deleting the song "
							+ resourceId + " for subscriber " + subscriberId
							+ " from database, as the song doesn't exist in huwai library");
					/*
					 * rbtDBManager.deactivateSubWavFile(subscriberId, resourceId,
					 * STATE_DEACTIVATED, "Daemon", "n"); // deleteAllClipSettings(subscriberId,
					 * resourceId); // rbtDBManager.addToDeletedSelections(subscriberId,
					 * resourceId); String circleId = rbtDBManager.getCircleId(subscriberId); Clips
					 * clip = rbtDBManager.getClipPromoID(resourceId); Categories category =
					 * rbtDBManager.getCategory(addClipForSubscriber.categoryID(), circleId,
					 * subscriber.prepaidYes()?'y':'n'); rbtDBManager.removeCategoryClip(category,
					 * clip); rbtDBManager.removePickOfTheday(clip.id()); int maxSelection =
					 * subscriber.maxSelections() - 1; if(maxSelection < 0) maxSelection = 0;
					 */

					/*
					 * rbtDBManager.updateNumMaxSelections(subscriberId, maxSelection);
					 * actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%",
					 * clip.name());
					 */

					Clips clip = rbtDBManager.getClipByPromoID(resourceId);
					name = clip.name();
					decrementSubMaxSelection(subscriber, 1);
				}
				else {
					String circleId = rbtDBManager.getCircleId(subscriberId);
					Categories mb = rbtDBManager.getCategoryPromoID(resourceId, circleId,
							subscriber.prepaidYes() ? 'y' : 'n');
					/*
					 * rbtDBManager.removeCategory(mb); actualSelFailedSMS =
					 * Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", mb.name());
					 */
					name = mb.name();
				}

				if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
					String gifterID = addClipForSubscriber.selectionInfo();
					String gifteeID = addClipForSubscriber.subID();
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
					String songGiftFailedSMSforGifter = parameter.getValue();
					actualSelFailedSMS = Tools.findNReplaceAll(songGiftFailedSMSforGifter,
							"%TUNE%", name);
					actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS, "%NUMBER%",
							gifteeID);
					if(sendSMS && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
								insertSMSInDuplicate);
				}
				else {
					actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", name);
					if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
								insertSMSInDuplicate);
					if(isSubscriberBulkActivated) {
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_sel_failure", rotationSize, "RBT_QUERY_DOWNLOAD_STATUS",
								subscriberId, userType, "query_download_status",
								"invalid_resource_id", requestedTimeString, differenceTime + "",
								app, urlstr, result);
					}
					if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
						sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
								selectionFailedSMSToRetailer, name, null, null);
				}
			}
			else if(result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "already_downloaded", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::download success for subscriber" + subscriberId + " for the tone "
								+ resourceId + " updating rbt_subscriber_downloads to 'y'");
				rbtDBManager.updateDownloadStatusToDownloaded(subscriberId, resourceId, new Date(),
						validityOfTone);
				// if(addClipForSubscriber.selectedBy().indexOf("PROMO") >= 0)
				// rbtDBManager.setAvailedPromoDownload(addClipForSubscriber.subID(), true);

				returnValue = true;
			}
			else if(result.equals("6")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "download_in_progress", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::download in progress for subscriber" + subscriberId
								+ " for the tone " + resourceId);
			}
			else if(result.equals("7")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "download_record_not_found", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::download record not found, download failure, for subscriber"
								+ subscriberId + " for the tone " + resourceId
								+ " deleting clip from the database, for the subcriber");
				/*
				 * rbtDBManager.deactivateSubWavFile(subscriberId, resourceId, STATE_DEACTIVATED,
				 * "Daemon", "n");
				 */
				// deleteAllClipSettings(subscriberId, resourceId);
				// rbtDBManager.addToDeletedSelections(subscriberId, resourceId);
				// rbtDBManager.insertOurDateDuplicateSubSel(subscriberId,
				// addClipForSubscriber.callerID(), addClipForSubscriber.subscriberFile(),
				// addClipForSubscriber.startTime(), "download failed");
				deleteSubscriberSelection(subscriberId, resourceId, null, true,
						"Daemon-no download record", false, -1, subscriber.prepaidYes(),
						resourceFlag == 0);

				Tools.writeSelectionToSDRFile(subscriberId, userType, "download_record_not_found",
						addClipForSubscriber.setTime(), null, addClipForSubscriber.callerID(),
						addClipForSubscriber.subscriberFile());

				String actualSelFailedSMS;
				String name;
				if(resourceFlag == 0) {
					decrementSubMaxSelection(subscriber, 1);
					Clips clip = rbtDBManager.getClipByPromoID(resourceId);
					/*
					 * rbtDBManager.deactivateSubscriberDownload(addClipForSubscriber.subID(),
					 * addClipForSubscriber.subscriberFile(), "Daemon-download failed"); int
					 * maxSelection = subscriber.maxSelections() - 1; if(maxSelection < 0)
					 * maxSelection = 0; rbtDBManager.updateNumMaxSelections(subscriberId,
					 * maxSelection); actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS,
					 * "%TUNE%", clip.name());
					 */

					name = clip.name();
				}
				else {
					String circleId = rbtDBManager.getCircleId(subscriberId);
					Categories mb = rbtDBManager.getCategoryPromoID(resourceId, circleId,
							subscriber.prepaidYes() ? 'y' : 'n');

					/*
					 * rbtDBManager.deactivateSubWavFile(subscriberId, resourceId,
					 * STATE_DEACTIVATED, "Daemon", "n"); // deleteAllClipSettings(subscriberId,
					 * resourceId); rbtDBManager.deactivateSubscriberDownload(subscriberId,
					 * resourceId, "Daemon-download failed"); actualSelFailedSMS =
					 * Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", mb.name());
					 */
					name = mb.name();
				}

				if(addClipForSubscriber.selectedBy().equalsIgnoreCase("GIFT")) {
					String gifterID = addClipForSubscriber.selectionInfo();
					String gifteeID = addClipForSubscriber.subID();
					parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
					String songGiftFailedSMSforGifter = parameter.getValue();
					actualSelFailedSMS = Tools.findNReplaceAll(songGiftFailedSMSforGifter,
							"%TUNE%", name);
					actualSelFailedSMS = Tools.findNReplaceAll(actualSelFailedSMS, "%NUMBER%",
							gifteeID);
					if(sendSMS && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
								insertSMSInDuplicate);
				}
				else {
					actualSelFailedSMS = Tools.findNReplaceAll(selectionFailedSMS, "%TUNE%", name);
					if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
						Tools.sendSMS(db_url, m_smsNo, subscriberId, actualSelFailedSMS,
								insertSMSInDuplicate);
					if(isSubscriberBulkActivated) {
						WriteSDR.addToAccounting(reportpath + subscriber.activatedBy()
								+ "_sel_failure", rotationSize, "RBT_QUERY_DOWNLOAD_STATUS",
								subscriberId, userType, "query_download_status",
								"download_record_not_found", requestedTimeString, differenceTime
										+ "", app, urlstr, result);
					}
					if(addClipForSubscriber.selectedBy().indexOf(RETAILER_STRING) != -1)
						sendRetailerSMS(addClipForSubscriber.selectionInfo(), subscriberId,
								selectionFailedSMSToRetailer, name, null, null);
				}
			}
			else if(result.equals("9")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "gifting_in_progress", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::gifting in progress for subscriber" + subscriberId + " for the tone "
								+ resourceId);
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
						"RBT_QUERY_DOWNLOAD_STATUS", subscriberId, userType,
						"query_download_status", "error_reponse", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::unexpected result, will try in the next loop");
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}

		return returnValue;
	}

	public String querySongOrMBSetting(String subscriberId, int setType, String toneCode,
			String[] loopNoNToneGprId) {
		String returnValue = "general_error";
		try {
			logger.info("RBT::Inside try...");

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			String httpLink = parameter.getValue();
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
			String operatorAccount = parameter.getValue();
			
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
			String operatorPassword = parameter.getValue();
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR", "");
			String operator = parameter.getValue();

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "QUERY_SETTING_PAGE", "");
			String querySettingPage = parameter.getValue();
			urlstr += querySettingPage;

			urlstr += operatorAccount + "&";
			urlstr += operatorPassword + "&";
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += "settype=" + setType + "&";
			urlstr += operator;

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();
			String result = null;

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_QUERY_SETTING",
						subscriberId, userType, "query_setting", "null_error_reponse",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				return null;
			}

			result = result.trim();

			if(result.length() <= 1) {
				if(result.equals("4")) {
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_QUERY_SETTING", subscriberId, userType, "query_setting",
							"no-setting", requestedTimeString, differenceTime + "", app, urlstr,
							result);
					returnValue = "setting_not_exist";
				}
				else
					WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize,
							"RBT_QUERY_SETTING", subscriberId, userType, "query_setting",
							"error_got-null", requestedTimeString, differenceTime + "", app,
							urlstr, result);
			}
			else {
				returnValue = "setting_not_exist";
				StringTokenizer settingResultST = new StringTokenizer(result, "&");
				String token = null;
				while(settingResultST.hasMoreTokens()) {
					token = settingResultST.nextToken();
					StringTokenizer tokenST = new StringTokenizer(token, "|");
					if(tokenST.countTokens() < 9) {
						logger.info("RBT::unexpected result leaving first while");
						break;
					}
					else {
						int ignoreCount = 5;
						if(setType != 1)
							ignoreCount = 6;
						for(int i = 0; i < ignoreCount; i++)
							tokenST.nextToken();

						String tnCode = tokenST.nextToken();
						if(tnCode.equalsIgnoreCase(toneCode)) {
							returnValue = "setting_exist";
							loopNoNToneGprId[0] = tokenST.nextToken(); // LoopNo
							loopNoNToneGprId[1] = tokenST.nextToken(); // ToneGroupID
							break;
						}
					}
				}
			}

			logger.info("RBT::leaving");
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	protected void addSubscriberToFile(String filePath, String subscriberId) throws Exception {
		try {
			File file = new File(filePath);
			if(!file.exists())
				file.createNewFile();

			Tools.writeTFile(file, subscriberId);
		}
		catch(Exception e) {
			logger.error("", e);
			throw e;
		}
	}

	private boolean isSubscriberBulkActivated(Subscriber subscriber) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String activateBy = subscriber.activatedBy();

		if(activateBy.equals("VP") || activateBy.equals("SMS") || activateBy.equals("VP-PROMO")
				|| activateBy.equals("SMS-PROMO"))
			return false;

		BulkPromo bulkPromo = rbtDBManager.getActiveBulkPromo(subscriber.activatedBy());

		return (bulkPromo != null);
	}

	private boolean isSubscriberBulkDeactivated(Subscriber subscriber) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String deactivatedBy = subscriber.deactivatedBy();

		if(deactivatedBy.equals("VP") || deactivatedBy.equals("SMS")
				|| deactivatedBy.equals("VP-PROMO") || deactivatedBy.equals("SMS-PROMO"))
			return false;

		BulkPromo bulkPromo = rbtDBManager.getActiveBulkPromo(subscriber.deactivatedBy());

		return (bulkPromo != null);
	}

	private boolean isSelectionBulkActivated(SubscriberStatus selection) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		BulkPromo bulkPromo = rbtDBManager.getActiveBulkPromo(selection.selectedBy());

		return (bulkPromo != null);
	}

	private Subscriber updateBulkActivatedSubscriber(Subscriber subscriber) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		if(rbtDBManager.setSubscriptionYes(subscriber.subID(), STATE_ACTIVATED))
			subscriber = rbtDBManager.getSubscriber(subscriber.subID());

		return subscriber;
	}

	// this method will make deactivated by same as activated by
	private Subscriber updateDeactivatedBy(Subscriber subscriber) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String prepaid = "n";

		if(subscriber.prepaidYes())
			prepaid = "y";

		subscriber = rbtDBManager.updateSubscriber(subscriber.subID(), subscriber.activatedBy(),
				subscriber.activatedBy(), subscriber.startDate(), subscriber.endDate(), prepaid,
				subscriber.accessDate(), subscriber.nextChargingDate(), subscriber.maxSelections(),
				subscriber.noOfAccess(), subscriber.activationInfo(), subscriber
						.subscriptionClass(), subscriber.activationDate(), subscriber.subYes(),
				subscriber.lastDeactivationInfo(), subscriber.lastDeactivationDate(), subscriber
						.cosID(), subscriber.activatedCosID());

		return subscriber;
	}

	private void addBulkUserToFile(String subscriberId, String promoID, String mode)
			throws Exception {
		String fileName = getFileNameForBulkPromo(promoID, mode, true);
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
		String promoSMSFilePath = parameter.getValue();
		String dirPath = promoSMSFilePath;
		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();

		String filePath = dirPath + File.separator + fileName;

		addSubscriberToFile(filePath, subscriberId);
	}

	private String getFileNameForBulkPromo(String promoID, String mode, boolean newFile) {
		String fileName = null;

		String flNameForSearch = promoID + "_" + mode + "_";
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "E:\\");
		String promoSMSFilePath = parameter.getValue();
		String dirPath = promoSMSFilePath;
		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();

		File[] files = dir
				.listFiles(new TATARBTDaemonMain.BulkPromoFileNameFilter(flNameForSearch));

		if(files != null && files.length > 0) {
			fileName = files[0].getName();
		}
		if(newFile && fileName == null) {
			fileName = promoID + "_" + mode + "_" + fileNameFormatter.format(new Date()) + ".txt";
		}

		return fileName;
	}

	private void sendActivationSuccessSMS(Subscriber subscriber, CosDetails cos)
			throws OnMobileException {
		String activationSuccessSMS = null;

		if(subscriber.activatedBy().equalsIgnoreCase("GIFT")) {
			String gifterID = subscriber.activationInfo();
			String gifteeID = subscriber.subID();
			String serviceGiftSuccessSMSforGifter = null;
			if(subscriber.prepaidYes()){
				Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PREPAID_SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTER", "");
				String prepaidServiceGiftSuccessSMSforGifter = parameter.getValue();
				serviceGiftSuccessSMSforGifter = prepaidServiceGiftSuccessSMSforGifter;
			}else{
				Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "POSTPAID_SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTER", "");
				String postpaidServiceGiftSuccessSMSforGifter = parameter.getValue();
				serviceGiftSuccessSMSforGifter = postpaidServiceGiftSuccessSMSforGifter;
			}
			if(serviceGiftSuccessSMSforGifter != null) {
				activationSuccessSMS = Tools.findNReplace(serviceGiftSuccessSMSforGifter,
						"%NUMBER%", gifteeID);
				Tools
						.sendSMS(db_url, m_smsNo, gifterID, activationSuccessSMS,
								insertSMSInDuplicate);
			}
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTEE", "");
			String serviceGiftSuccessSMSforGiftee = parameter.getValue();
			if(serviceGiftSuccessSMSforGiftee != null) {
				activationSuccessSMS = Tools.findNReplace(serviceGiftSuccessSMSforGiftee,
						"%NUMBER%", gifterID);
				Tools
						.sendSMS(db_url, m_smsNo, gifteeID, activationSuccessSMS,
								insertSMSInDuplicate);
			}
			return;
		}
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_COS"+ cos.getCosId(), "");
		activationSuccessSMS = parameter.getValue();
		if(subscriber.activatedBy().indexOf("SMS") >= 0){
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_WHEN_SMS_COS"+ cos.getCosId(), "");
			activationSuccessSMS = parameter.getValue();
		}
		if(activationSuccessSMS == null) {
			if(subscriber.prepaidYes()){
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_PREPAID", "");
				String activationSuccessSMSPrepaid = parameter.getValue();
				activationSuccessSMS = activationSuccessSMSPrepaid;
			}else{
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_POSTPAID", "");
				String activationSuccessSMSPostpaid = parameter.getValue();
				activationSuccessSMS = activationSuccessSMSPostpaid;
			}
		}

		boolean hasSelections = false;
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_ACTIVATION_SUCCESS_SMS_ON_NO_SEL", "");
		String sendActivationSuccessSMSOnNoSelString = parameter.getValue();
		boolean sendActivationSuccessSMSOnNoSel;
		if(sendActivationSuccessSMSOnNoSelString.equalsIgnoreCase("TRUE"))
			sendActivationSuccessSMSOnNoSel = true;
		else
			sendActivationSuccessSMSOnNoSel = false;
		if(sendActivationSuccessSMSOnNoSel) {
			SubscriberStatus[] results = RBTDBManager.getInstance()
					.getAllSubscriberSelectionRecords(subscriber.subID(), null);
			// getAllActiveSubscriberRecords(subscriber.subID());
			if(results != null && results.length > 0)
				hasSelections = true;
		}
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_ACTIVATION_SUCCESS_SMS", "");
		String sendActivationSuccessSMSString = parameter.getValue();
		boolean sendActivationSuccessSMS;
		if(sendActivationSuccessSMSString.equalsIgnoreCase("TRUE"))
			sendActivationSuccessSMS = true;
		else
			sendActivationSuccessSMS = false;
		if(sendActivationSuccessSMS || hasSelections)
			Tools.sendSMS(db_url, m_smsNo, subscriber.subID(), activationSuccessSMS,
					insertSMSInDuplicate);

		if(subscriber.activatedBy().indexOf(RETAILER_STRING) != -1) {
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_TO_RETAILER", "");
			String activationSuccessSMSToRetailer = parameter.getValue();
			String retSMS = getRetailerSubstitutedSMS(activationSuccessSMSToRetailer, subscriber
					.subID(), null);
			Tools.sendSMS(db_url, m_smsNo, subscriber.activationInfo(), retSMS,
					insertSMSInDuplicate);
		}
	}

	private void sendActivationFailedSMS(Subscriber subscriber) throws OnMobileException {
		if(subscriber.activatedBy().equalsIgnoreCase("GIFT")) {
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SERVICE_GIFT_FAILED_SMS_FOR_GIFTER", "");
			String serviceGiftFailedSMSforGifter = parameter.getValue();
			String giftSrvcFailSMS = Tools.findNReplace(serviceGiftFailedSMSforGifter, "%NUMBER%",
					subscriber.subID());
			Tools.sendSMS(db_url, m_smsNo, subscriber.activationInfo(), giftSrvcFailSMS,
					insertSMSInDuplicate);
		}
		else {
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS", "");
			String activationFailedSMS = parameter.getValue();
			Tools.sendSMS(db_url, m_smsNo, subscriber.subID(), activationFailedSMS,
					insertSMSInDuplicate);
			// sending sms to retailer
			if(subscriber.activatedBy().indexOf(RETAILER_STRING) != -1) {
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS_TO_RETAILER", "");
				String activationFailedSMSToRetailer = parameter.getValue();
				String retSMS = getRetailerSubstitutedSMS(activationFailedSMSToRetailer, subscriber
						.subID(), null);
				Tools.sendSMS(db_url, m_smsNo, subscriber.activationInfo(), retSMS,
						insertSMSInDuplicate);
			}
		}
	}

	private class BulkPromoFileNameFilter implements FilenameFilter {
		private String flNameForSearch = null;

		public BulkPromoFileNameFilter(String flNameForSearch) {
			this.flNameForSearch = flNameForSearch;
		}

		public boolean accept(File dir, String name) {
			return (name.indexOf(flNameForSearch) >= 0);
		}

	}

	// /////////////////////////
	private String getRetailerSubstitutedSMS(String smsText, String strSubID, String clipName) {
		String returnSMS = smsText;
		if(strSubID != null)
			returnSMS = Tools.findNReplaceAll(smsText, "%NUMBER%", strSubID);
		if(clipName != null)
			returnSMS = Tools.findNReplaceAll(returnSMS, "%TUNE%", clipName);

		return returnSMS;
	}

	private void sendRetailerSMS(String actSelInfo, String subscriberId, String smsText,
			String clipName, String cost, String validity) {
		String retailerNo = actSelInfo.substring(0, actSelInfo.indexOf("|"));
		try {
			Long.parseLong(retailerNo);
			String retSMS = getRetailerSubstitutedSMS(smsText, subscriberId, clipName);

			if(cost != null)
				retSMS = Tools.findNReplaceAll(retSMS, "%COST%", cost);
			if(validity != null)
				retSMS = Tools.findNReplaceAll(retSMS, "%VALIDITY%", validity);

			Tools.sendSMS(db_url, m_smsNo, retailerNo, retSMS, insertSMSInDuplicate);
		}
		catch(Exception e) {

		}
	}

	private void deleteSubscriberSelection(String subID, String subWavFile, String callerID,
			boolean deleteDownload, String deactivatedBy, boolean deleteMap, int categoryID,
			boolean isPrepaid, boolean isClip) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if(deleteDownload) {
			// rbtDBManager.deleteAllClipSettings(subID, subWavFile);
			rbtDBManager.deleteClipForSubscriber(subID, subWavFile);
			rbtDBManager.deactivateSubscriberDownload(subID, subWavFile, deactivatedBy);
		}
		else
			rbtDBManager.deleteClipForSubscriber(subID, subWavFile, callerID);

		// rbtDBManager.addToDeletedSelections(subID, subWavFile);

		if(deleteMap) {
			String circleId = rbtDBManager.getCircleId(subID);
			if(isClip) {
				Clips clip = rbtDBManager.getClipByPromoID(subWavFile);
				Categories category = rbtDBManager.getCategory(categoryID, circleId,
						isPrepaid ? 'y' : 'n');
				rbtDBManager.removeCategoryClip(category, clip);
				rbtDBManager.removePickOfTheday(clip.id());
			}
			else {
				Categories musicbox = rbtDBManager.getCategoryPromoID(subWavFile, circleId,
						isPrepaid ? 'y' : 'n');
				rbtDBManager.removeCategory(musicbox);
			}
		}
	}

	private void decrementSubMaxSelection(Subscriber sub, int num) {
		int newNumMaxSel = sub.maxSelections() - 2;
		if(newNumMaxSel < 0)
			newNumMaxSel = 0;

		RBTDBManager.getInstance().updateNumMaxSelections(sub.subID(), newNumMaxSel);
	}

	protected static String getStackTrace(Throwable ex) {
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if(ex instanceof Exception) {
			Exception exception = (Exception) ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}

	public boolean giftSong(SubscriberStatus addClipForSubscriber, int validityOfSong) {
		boolean returnValue = false;
		String failReason = "";
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			logger.info("RBT::Inside try...");

			String gifteeID = addClipForSubscriber.subID();
			String gifterID = addClipForSubscriber.selectionInfo();
			String toneCode = addClipForSubscriber.subscriberFile();

			Subscriber subscriber = rbtDBManager.getSubscriber(gifteeID);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstrForDownload = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "GIFT_SONG_PAGE", "");
			String giftSongPage = parameter.getValue();
			urlstrForDownload += giftSongPage;

			urlstrForDownload += TataUtility.getOperatorAccount(null) + "&";
			urlstrForDownload += TataUtility.getOperatorPassword(null) + "&";
			urlstrForDownload += "phonenumber=" + gifterID + "&";
			urlstrForDownload += "acceptphonenumber=" + gifteeID + "&";
			urlstrForDownload += "tonecode=" + toneCode + "&";
			urlstrForDownload += "flag=1&";
			urlstrForDownload += TataUtility.getOperatorCode(null);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstrForDownload, gifteeID, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				return false;
			}

			result = result.trim();

			logger.info("RBT::the result code for subscriber"
					+ gifteeID + " for the tone " + toneCode + " is " + result);
			if(result.equals("0")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "success", requestedTimeString,
						differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::gift tone success for subscriber" + gifteeID + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'y'");
				rbtDBManager.updateDownloadStatusToDownloaded(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), new Date(), validityOfSong);

				returnValue = true;
			}
			else if(result.equals("99")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "provisional_success",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::download success for subscriber" + gifteeID + " for the tone "
								+ toneCode + " updating rbt_subscriber_downloads to 'p'");
				rbtDBManager.updateDownloadStatus(addClipForSubscriber.subID(),
						addClipForSubscriber.subscriberFile(), 'p');

				returnValue = true;
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "gifter_not_valid",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::gifter "
						+ gifterID + " is not valid");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("4")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "giftee_not_valid",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::giftee "
						+ gifteeID + " is not valid");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "giftee_is_not_CRBT_user",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::giftee "
						+ gifteeID + " is not CRBT user");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("6")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "giftee_is_in_blacklist",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::giftee "
						+ gifteeID + " is in blacklist");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("7")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "song_does_not_exist",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::song does not exist");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("9") || result.equals("10")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "already_in_personal_library",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT:: Already in personal library");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("11")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "giftee_library_full",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::giftee "
						+ gifteeID + " library is full");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("12")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "gifter_is_a_corp_user",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::gifter "
						+ gifterID + " is is a corp user");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("8")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "portal_error", requestedTimeString,
						differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT:: Portal Error");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("14")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "disabled_in_user's_COS",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT:: Disabled in user's COS");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else if(result.equals("15")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "insufficient_balance",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT::insufficient_balance");
				failReason = "gifter_not_valid";
				returnValue = false;
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_TONE",
						gifteeID, userType, "gifting_tone", "gifting_tone_failed",
						requestedTimeString, differenceTime + "", app, urlstrForDownload, result);
				logger.info("RBT:: Gifting tone failed");
				failReason = "gifter_not_valid";
				returnValue = false;
			}

			if(!returnValue) {
				deleteSubscriberSelection(gifteeID, toneCode, null, true, failReason, true,
						addClipForSubscriber.categoryID(), addClipForSubscriber.prepaidYes(), true);

				Clips clip = rbtDBManager.getClipByPromoID(addClipForSubscriber.subscriberFile());
				parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SONG_GIFT_FAILED_SMS_FOR_GIFTER", "");
				String songGiftFailedSMSforGifter = parameter.getValue();
				String actualSelFailedSMS = Tools.findNReplaceAll(songGiftFailedSMSforGifter,
						"%TUNE%", clip.name());
				actualSelFailedSMS = Tools
						.findNReplaceAll(actualSelFailedSMS, "%NUMBER%", gifteeID);
				if(sendSMS && subscriber.rbtType() != 10)
					Tools.sendSMS(db_url, m_smsNo, gifterID, actualSelFailedSMS,
							insertSMSInDuplicate);
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public String giftService(Subscriber subscriber, CosDetails cos) {
		String result = null;
		String returnValue = null;
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		try {
			logger.info("RBT::inside try.......");

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String gifteeID = subscriber.subID();
			String gifterID = subscriber.activationInfo();

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			String urlstr = httpLink;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "GIFT_SERVICE_PAGE", "");
			String giftServicePage = parameter.getValue();
			urlstr += giftServicePage;

			logger.info("RBT::before adding parameters for subscriber "
					+ gifteeID);

			urlstr += TataUtility.getOperatorAccount(null) + "&";
			urlstr += TataUtility.getOperatorPassword(null) + "&";
			urlstr += "phonenumber=" + gifterID + "&";
			urlstr += "acceptphonenumber=" + gifteeID + "&";
			urlstr += TataUtility.getOperatorCode(null);

			logger.info("RBT::after adding parameters(before sending http request) for subscriber "
							+ gifteeID);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, gifteeID, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
				result = result.trim();

			logger.info("RBT::gifting result for subscriber " + gifteeID
					+ " is:: " + result);

			if(result == null) {
				//Response = null_error_response
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::returning null from giftService for the subscriber " + gifteeID);
			}
			else if(result.equals("99")) {
				//Response = provisional_success
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "provisional_success",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user "
										+ gifteeID
										+ " is been presented with the Coloring service and is under process, updating rbt_subscriber table as pending");
				// rbtDBManager.updateActivationPendingSubscriberTATA(gifteeID);
				rbtDBManager.smURLSubscription(gifteeID, true, false, null);
				returnValue = "activation-pending";
			}
			else if(result.equals("3")) {
				//Response = invaild_gifter_number
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "invaild_gifter_number",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The gifter " + gifterID
						+ " is not valid");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "invaild_gifter_number",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("4")) {
				//Response = giftee_is_no_TTSL_subscriber
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "giftee_is_no_TTSL_subscriber",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + gifteeID
						+ " is not a TTSL subscriber");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "giftee_is_no_TTSL_subscriber",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("5")) {
				//Response = giftee_is_already_CRBT_user
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "giftee_is_already_CRBT_user",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + gifteeID
						+ " is already CRBT user");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "giftee_is_already_CRBT_user",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("6")) {
				//Response = giftee_is_in_black_list
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "giftee_is_in_black_list",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + gifteeID
						+ " is in back list");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "giftee_is_in_black_list",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("10")) {
				//Response = giftind_is_underprogress
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "giftind_is_underprogress",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + gifteeID
						+ " gifting is underprogress");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "giftind_is_underprogress",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("12")) {
				//Response = gifter_is_a_corp_user
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "gifter_is_a_corp_user",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::gifter " + gifterID
						+ " is a corp user");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "gifter_is_a_corp_user",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("8")) {
				//Response = portal_error
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "portal_error", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT:: Portal Error");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "portal_error", subscriber
						.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("14")) {
				//Response = disabled_in_user's_COS
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "disabled_in_user's_COS",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT:: Disabled in user's COS");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "disabled_in_user's_COS",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else if(result.equals("15")) {
				//Response = insufficient_balance
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "insufficient_balance",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT:: insufficient_balance");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "insufficient_balance",
						subscriber.startDate(), null);
				returnValue = "activation-failed";
			}
			else {
				//Response = gifting_failed 
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, "RBT_GIFT_SERVICE",
						gifteeID, userType, "gifting_service", "gifting_failed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT:: Gifting Failed");
				Tools.writeSubscriberToSDRFile(gifteeID, userType, "gifting_failed", subscriber
						.startDate(), null);
				returnValue = "activation-failed";
			}

			if(returnValue.equals("activation-failed")) {
				rbtDBManager.deactivateSubscriberForTATA(gifteeID);
				sendActivationFailedSMS(subscriber);
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}

	public boolean sendPromoRecommendationSMS(String subscriberFile, String smsText,
			TATARBTDaemonOzonized ozoneThread) {
		logger.info("RBT::inside sendPromoRecommendationSMS");
		try {
			File smsFile = new File(subscriberFile);
			String cunDateStr = onlyDayformatter.format(new Date());
			String promoStartDate = cunDateStr;

			if(!smsFile.exists()) {
				logger.info("RBT:: "
						+ smsFile.getAbsolutePath() + " file does not exist");
				return false;
			}
			Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SMS_PROMO_MESSAGE_ID", "BULKSMS_START");
			String promoSMSMsgID = parameter.getValue();
			String msgId = promoSMSMsgID;
			String data = "";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", "10");
			int numOfPromSMSPerSec = Integer.parseInt(parameter.getValue().trim());
			int numberOfSMSPerSec = numOfPromSMSPerSec;
			String promoSMSCTemp = null;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMSC", "");
			String promoSMSC = parameter.getValue();
			if(promoSMSC != null)
				promoSMSCTemp = promoSMSC;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", "");
			String promoSMSStartTime = parameter.getValue();
			String smsStartTime = promoSMSStartTime;
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", "");
			String promoSMSEndTime = parameter.getValue();
			String smsEndTime = promoSMSEndTime;

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, 3);
			String smsEndDate = endDateformatter.format(calendar.getTime());

			data += "PROMO_NAME=rec" + fileNameFormatter.format(new Date());
			data += "|PROMO_FILE=" + subscriberFile;
			if(promoSMSCTemp != null)
				data += "|PROMO_SMSC=" + promoSMSCTemp;
			data += "|PROMO_TEXT=" + smsText;
			data += "|PROMO_START_DATE=" + promoStartDate;
			data += "|PROMO_START_TIME=" + smsStartTime;
			data += "|PROMO_END_DATE=" + smsEndDate;
			data += "|PROMO_END_TIME=" + smsEndTime;
			data += "|PROMO_SMS_PER_SEC=" + numberOfSMSPerSec;
			data += "|PROMO_SENDER=" + m_smsNo;
			data += "|PROMO_TEST_NUMBERS=";
			data += "|PROMO_STATUS_INTERVAL=10";
			data += "|PROMO_TEST_MODE=FALSE";
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "PROMO_SMS_TYPE", "text");
			String promoSMSType = parameter.getValue();
			data += "|PROMO_MESSAGE_TYPE=" + promoSMSType;

			return (ozoneThread.createInfoMessage(msgId, data));

		}
		catch(Exception e) {
			logger.error("", e);
		}
		return false;
	}

	protected String updateSongToToBeDeleted(SubscriberDownloads download) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		boolean result = rbtDBManager.updateDownloadStatus(download.subscriberId(), download
				.promoId(), 'd');

		ClipMinimal clip = rbtDBManager.getClipRBT(download.promoId());
		boolean isOnePlusOneClip = false;
		if(clip != null && clip.getGrammar() != null && clip.getGrammar().equals("1+1"))
			isOnePlusOneClip = true;

		// adding selections for the charged clip
		if(result && isOnePlusOneClip) {
			Subscriber subscriber = rbtDBManager.getSubscriber(download.subscriberId());
			if(rbtDBManager.isSubDeactive(subscriber))
				return "subscriber-inactive";

			String chargedClipID = clip.getDemoFile();
			Clips chargedClip = null;
			try {
				chargedClip = rbtDBManager.getClip(Integer.parseInt(chargedClipID));
			}
			catch(Exception e) {

			}
			if(chargedClip == null)
				return "charged-clip-missing";

			SubscriberDownloads chargedDownload = rbtDBManager.getActiveSubscriberDownload(download
					.subscriberId(), chargedClip.wavFile());

			if(chargedDownload != null)
				return "charged-song-already-downloaded";

			/*
			 * chargedDownload = rbtDBManager.addSubscriberDownload(download.subscriberId(),
			 * download.promoId(), download.categoryID(), false, download.categoryType());
			 */

			SubscriberStatus[] selections = rbtDBManager.getSubscriberStatus(download
					.subscriberId(), download.promoId());
			// chargedDownload =
			rbtDBManager.addSubscriberDownload(download.subscriberId(), chargedClip.wavFile(),
					download.categoryID(), false, download.categoryType());

			for(int sc = 0; selections != null && sc < selections.length; sc++) {
				int status = 5;
				if(sc == 0)
					status = 2;
				rbtDBManager.addSubscriberSelections(selections[sc].subID(), selections[sc]
						.callerID(), selections[sc].categoryID(), chargedClip.wavFile(), null,
						null, selections[sc].endTime(), status, selections[sc].selectedBy(), "1+1",
						0, selections[sc].prepaidYes(), false, null, 0, 2359, null, true, false,
						null, null, subscriber.subYes(), null, rbtDBManager.getCircleId(download
								.subscriberId()), false, true, (selections[sc].status() == 2),
						subscriber, selections[sc].selInterval());
			}
		}
		return "success";
	}

	// Updated by SenthilRaja for BulkActivation Requirement
	public String bulkActivateSubscriber(Subscriber subscriber) {
//		String method = "bulkActivateSubscriber";
		String result = null;
		String returnValue = null;
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String httpLink = parameter.getValue();
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "SEND_BULK_ACT_WELCOME_SMS", "");
		String sendBulkACTWelcomeSMSString = parameter.getValue();
		boolean sendBulkACTWelcomeSMS;
		if(sendBulkACTWelcomeSMSString.equalsIgnoreCase("TRUE"))
			sendBulkACTWelcomeSMS = true;
		else
			sendBulkACTWelcomeSMS = false;
		try {
			String eventType = "RBT_BULK_ACTIVATION";
			logger.info("RBT::inside try.......");

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String subscriberId = subscriber.subID();
			String cosID = subscriber.cosID();
			CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
			boolean isSubscriberBulkActivated = isSubscriberBulkActivated(subscriber);

			String userType = "";
			if(subscriber.prepaidYes())
				userType = "PRE_PAID";
			else
				userType = "POST_PAID";

			if(subscriber.activatedBy().equalsIgnoreCase("GIFT")) {
				returnValue = giftService(subscriber, cos);
				return returnValue;
			}

			StringBuilder urlBuffer = new StringBuilder();
			urlBuffer.append(httpLink);
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPEN_BULK_ACCOUNT_PAGE", "");
			String openBulkAccountPage = parameter.getValue();
			urlBuffer.append(openBulkAccountPage);

			logger.info("RBT::before adding parameters for subscriber " + subscriberId);

			urlBuffer.append(TataUtility.getOperatorAccount(cos) + "&");
			urlBuffer.append(TataUtility.getOperatorPassword(cos) + "&");
			urlBuffer.append("phonenumber=" + subscriberId + "&");
			urlBuffer.append(TataUtility.getOperatorCode(cos));
			// if((allowCos && !cos.isDefault()) || (allowCos && allowDefaultCos &&
			// cos.isDefault()))
			urlBuffer.append("&cosid=" + cosID);

			String urlstr = urlBuffer.toString();

			logger.info("RBT::after adding parameters(before sending http request) for subscriber "
							+ subscriberId);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			result = rbthttpProcessing.makeRequest1(urlstr, subscriberId, app);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
				result = result.trim();

			logger.info("RBT::bulk activation result for subscriber " + subscriberId + " is:: "
							+ result);

			if(result == null) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "null_error_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::returning null from bulkActivateSubscriber for the subscriber "
								+ subscriberId);
			}
			else if(result.equals("0")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "bulk_activation_accepted",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::Bulk Activation Accepted");
				/*
				 * if(isSubscriberBulkActivated && sendBulkACTWelcomeSMS && subscriber.rbtType() !=
				 * 10) { addBulkUserToFile(subscriberId, subscriber.activatedBy(), "activated"); }
				 * rbtDBManager.smURLSubscription(subscriberId, true, false);
				 */
				Calendar nextChargingDate = Calendar.getInstance();
				nextChargingDate.set(2035, 11, 31, 0, 0, 0);

				String type = "B";
				if(subscriber.prepaidYes())
					type = "P";

				rbtDBManager.smSubscriptionSuccess(subscriberId, nextChargingDate.getTime(),
						new Date(), type, subscriber.subscriptionClass(), true, cos, subscriber
								.rbtType());

				Tools.writeSubscriberToSDRFile(subscriberId, userType, "accepted", subscriber
						.startDate(), new Date());
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10) {
					sendActivationSuccessSMS(subscriber, cos);
				}
				if(isSubscriberBulkActivated && sendBulkACTWelcomeSMS && subscriber.rbtType() != 10) {
					addBulkUserToFile(subscriberId, subscriber.activatedBy(), "activated");
				}
				// rbtDBManager.smURLSubscription(subscriberId, true, false);
				returnValue = "accepted";
			}
			else if(result.equals("99")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "bulk_activation_retry",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::Retry (Not able to store the request)");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"bulk_activation_retry", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "bulk-activation-retry";
			}
			else if(result.equals("1")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "invalid_operator_parameter",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid operator parameter");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"invalid_operator_parameter", requestedTimeString, differenceTime + "",
							app, urlstr, result);
				}
				returnValue = "invalid-operator-parameter";
			}
			else if(result.equals("2")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "invalid_parameter",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid parameter(s) formate");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"invalid_parameter", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "invalid-parameter";
			}
			else if(result.equals("3")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation",
						"invalid_user_number_activation_failed", requestedTimeString,
						differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid user phone number(user in black list), deleting from database");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"invalid_user_number_activation_failed", requestedTimeString,
							differenceTime + "", app, urlstr, result);
				}
				returnValue = "black-listed";
			}
			else if(result.equals("4")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "already_subscribed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " is already subscribed, updating rbt_subscriber table");

				Calendar nextChargingDate = Calendar.getInstance();
				nextChargingDate.set(2035, 11, 31, 0, 0, 0);

				String type = "B";
				if(subscriber.prepaidYes())
					type = "P";

				rbtDBManager.smSubscriptionSuccess(subscriberId, nextChargingDate.getTime(),
						new Date(), type, subscriber.subscriptionClass(), true, cos, subscriber
								.rbtType());

				Tools.writeSubscriberToSDRFile(subscriberId, userType, "already_subscribed",
						subscriber.startDate(), new Date());
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10) {
					sendActivationSuccessSMS(subscriber, cos);
				}
				if(isSubscriberBulkActivated && sendBulkACTWelcomeSMS && subscriber.rbtType() != 10) {
					addBulkUserToFile(subscriberId, subscriber.activatedBy(), "activated");
				}
				returnValue = "already-subscribed";
			}
			else if(result.equals("5")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "registration_under_processing",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::Registration is already under processing");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType,
						"registration_under_processing", subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"registration_under_processing", requestedTimeString, differenceTime
									+ "", app, urlstr, result);
				}
				returnValue = "registration-under-processing";
			}
			else if(result.equals("6")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "max_register_reached",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::Maximum number of register for the day  has reached");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType, "max_register_reached",
						subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"max_register_reached", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "max-register-reached";
			}
			else if(result.equals("7")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation", "gifted_with_service",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user is been presented (gifted) with service and is under process");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType, "gifted_with_service",
						subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"gifted_with_service", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "bulk-activation-failed";
			}
			else if(result.equals("8")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "bulk_activation",
						"portal_error_bulk_activation_failed", requestedTimeString, differenceTime
								+ "", app, urlstr, result);
				logger.info("RBT::got 8 response, considering as negative response");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType,
						"portal_error_activation_failed", subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "bulk_activation",
							"portal_error_bulk_activation_failed", requestedTimeString,
							differenceTime + "", app, urlstr, result);
				}
				returnValue = "portal-error";
			}
			else if(result.equals("14")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "invalid_cos_activation_failed",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::invalid cos");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType,
						"invalid_cos_activation_failed", subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated && subscriber.rbtType() != 10)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"invalid_cos_activation_failed", requestedTimeString, differenceTime
									+ "", app, urlstr, result);
				}
				returnValue = "invalid-cos";
			}
			else if(result.equals("15")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "insufficient_balance",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::insufficient balance");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				Tools.writeSubscriberToSDRFile(subscriberId, userType, "insufficient_balance",
						subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated)
					sendActivationFailedSMS(subscriber);
				if(isSubscriberBulkActivated)
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"invalid_cos_activation_failed", requestedTimeString, differenceTime
									+ "", app, urlstr, result);
				returnValue = "activation-failed";
			}
			else if(result.equals("16")) {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "already_availed_cos",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::already_availed_cos");
				rbtDBManager.deactivateSubscriberForTATA(subscriberId);
				rbtDBManager.addSubscriberToDeactivatedSubscribersTable(subscriberId, "avl_cos",
						subscriber.activatedCosID());
				// rbtDBManager.insertOurDateDuplicateSub(subscriberId, subscriber.startDate(),
				// "deactivated");//inserting into duplicate subscriber table
				Tools.writeSubscriberToSDRFile(subscriberId, userType, "already_availed_cos",
						subscriber.startDate(), null);
				if(sendSMS && !isSubscriberBulkActivated) {
					// Tools.sendSMS(db_url, m_smsNo, subscriberId, activationFailedSMS,
					// insertSMSInDuplicate);
					if(subscriber.rbtType() != 10){
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS_FOR_COS_AVAILED", "");
						String activationFailedSMSForCOSAvailed = parameter.getValue();
						Tools.sendSMS(db_url, m_smsNo, subscriber.subID(),
								activationFailedSMSForCOSAvailed, insertSMSInDuplicate);
					}
					// sending sms to retailer
					if(subscriber.activatedBy().indexOf(RETAILER_STRING) != -1) {
						parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "ACTIVATION_FAILED_SMS_TO_RETAILER", "");
						String activationFailedSMSToRetailer = parameter.getValue();
						String retSMS = getRetailerSubstitutedSMS(activationFailedSMSToRetailer,
								subscriber.subID(), null);
						Tools.sendSMS(db_url, m_smsNo, subscriber.activationInfo(), retSMS,
								insertSMSInDuplicate);
					}
				}
				if(isSubscriberBulkActivated) {
					WriteSDR.addToAccounting(
							reportpath + subscriber.activatedBy() + "_act_failure", rotationSize,
							eventType, subscriberId, userType, "activation",
							"already_availed_cos", requestedTimeString, differenceTime + "", app,
							urlstr, result);
				}
				returnValue = "activation-failed";
			}
			else {
				WriteSDR.addToAccounting(daemonQueriesLogPath, rotationSize, eventType,
						subscriberId, userType, "activation", "invalid_response",
						requestedTimeString, differenceTime + "", app, urlstr, result);
				logger.info("RBT::The user " + subscriberId
						+ " request to activate was not sucessful, will try in the next loop");
				returnValue = "bulk-activation-failed";
			}
		}
		catch(Exception e) {
			logger.error("", e);
		}
		return returnValue;
	}
	
	private void writeGraceSDR(String eventType, String subID, String requestDetail,
			String responseDetail) {
		WriteSDR.addToAccounting(reportpath + "GraceSDR", rotationSize, eventType, subID, "NA",
				"NA", "NA", onlyDayformatter.format(new Date()), "NA", app, requestDetail,
				responseDetail);
	}

	
	
	private int getNextIndex(Subscriber sub, SubscriberStatus sel) {
		int index = -1;
		try {
			if(sub != null) {
				String actInfo = sub.activationInfo();
				if(actInfo != null && actInfo.indexOf("#G") != -1)
					index = Integer.parseInt(actInfo.substring(actInfo.indexOf("#G") + 2, actInfo
							.indexOf("#GG")));
				if(index == -1)
					index = calcNextIndex(sub.startDate());
			}
			else if(sel != null) {
				String selInfo = sel.selectionInfo();
				if(selInfo != null && selInfo.indexOf("#G") != -1)
					index = Integer.parseInt(selInfo.substring(selInfo.indexOf("#G") + 2, selInfo
							.indexOf("#GG")));
				if(index == -1)
					index = calcNextIndex(sel.setTime());
			}
			
			if(index >= graceIntervalHours.size())
				return -1;
			
			//if(index != -1)
			//	return Integer.parseInt((String)graceIntervalHours.get(index));
		}
		catch(Exception e) {
			logger.error("", e);
		}
		logger.info("RBT::returning " + index);
		return index;
	}
	
	private int calcNextIndex(Date sd) {
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		for(int index = 0; index < graceIntervalHours.size(); index++) {
			cal.add(Calendar.HOUR, Integer.parseInt((String)graceIntervalHours.get(index)));
			Date calDate = cal.getTime();
			if(calDate.after(nowDate) || calDate.equals(nowDate))
				return index;
		}
		return -1;
	}
	
	private String getGraceInfo(String info, int index) {
		try {
			if(info == null)
				return "#G" + (index + 1) + "#GG";

			if(info.indexOf("#G") == -1)
				return info + "#G" + (index + 1) + "#GG";

			StringBuilder sb = new StringBuilder(info.substring(0, info.indexOf("#G")));
			sb.append("#G");
			sb.append(index + 1);
			sb.append("#GG");
			sb.append(info.indexOf("#GG") + 3);
			return sb.toString();
		}
		catch(Exception e) {
		}
		return "#G" + (index + 1) + "#GG" + info;
	}
	
	private String convertValidityPeriodToDays(String validityPeriod) {
		if (null != validityPeriod) {
			String periodType = validityPeriod.substring(0, 1);
			int period = Integer.parseInt(validityPeriod.substring(1));
			if (periodType.equals("D")) {
				return String.valueOf(period);
			} else if (periodType.equals("M")) {
				return String.valueOf(period * 30);
			} else if (periodType.equals("Y")) {
				return String.valueOf(period * 365);
			}
		}
		return "";
	}
}