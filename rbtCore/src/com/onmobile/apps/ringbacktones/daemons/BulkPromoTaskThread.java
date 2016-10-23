package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.apps.ringbacktones.bulkreporter.ZipFiles;
import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.RBTSMS;
import com.onmobile.apps.ringbacktones.common.RBTSMSImpl;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.webservice.implementation.tata.TataUtility;
import com.onmobile.common.exception.OnMobileException;

public class BulkPromoTaskThread extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(BulkPromoTaskThread.class);

	private static TATARBTDaemonOzonized m_ozoneMainThread = null;
	private static TATARBTDaemonMain m_tataRBTDaemonMain = null;

	private boolean isStarted = false;
	private static BulkPromoTaskThread m_taskThread;

	List<BulkPromoSMS> allBulkPromoSMS = null;
	BulkPromo[] activeBulkPromosForActivationMessage = null;
	BulkPromo[] activeBulkPromosForDeactivationMessage = null;
	BulkPromo[] promosForDeactivation = null;

	private static int intervalToSendBulkActSMS = 3 * 60;
	private Calendar calSendBulkActSMS = Calendar.getInstance();

	private static int intervalToSendBulkDeactSMS = 3 * 60;
	private Calendar calSendBulkDeactSMS = Calendar.getInstance();

	boolean sentCCBulkDeactivationSMS = false;

	private static String db_url = null;
	private static int sleepMinutes = 1;
	// private static String allowCOS = "false";

	// Added by N.SenthilRaja
	private static int days = 30;
	// private static String rbtSMSDb_Url = null;
	private static int intervalToRBTSMS = 6; // In hours

	private static int recommendationFrequency = 7;
	private static int lastSMSSentDay = 9;
	private static int lastSMSSentDayForPickOfTheDay = 9;
	private static List<Integer> recommendationDays = new ArrayList<Integer>();

	private static String[] nonRecommendationCats = null;
	private static String autoRecommendationSMS = null;
	private static String pickOfTheDayRecommendationSMS = null;
	private static String autoRecommendationSMSForMB = null;
	private static int recommendationClipCount = 1;
	protected static String promoSMSFilePath = "E:\\";
	protected static String pickofDaySMSFilePath = "E:\\";
	private static String recommendationReportMode = "FTP";
	private static String spiderPath = null;

	private static String ftpServer = "10.9.11.16";
	private static int ftpPort = 21;
	private static long ftpWaitPeriod = 300000l;
	private static String ftpUser = "onmobile";
	private static String ftpPassword = "qwerty12#";
	private static int ftpRetries = 1;
	private static String ftpDir = "spider\\local\\uploads";
	private static int ftpTimeout = 7200000;

	//Added By Sreekar
	private static String _subscriptionConfirmedSMS = null;
	
	public static BulkPromoTaskThread getInstance(TATARBTDaemonOzonized ozoneMainThread) {
		m_ozoneMainThread = ozoneMainThread;
		m_tataRBTDaemonMain = TATARBTDaemonMain.getInstance();
		db_url = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "DB_URL", null);
		//Changes done for URL Encryption and Decryption
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
		sleepMinutes = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", 0);
		intervalToSendBulkActSMS = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "TIME_INTERVAL_TO_SEND_BULK_ACTIVATION_WELCOME_SMS_IN_MINUTES", 0);
		intervalToSendBulkDeactSMS = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "TIME_INTERVAL_TO_SEND_BULK_ACTIVATION_TERMINATION_SMS_IN_MINUTES", 0);

		// Added by SenthilRaja
		days = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "RBT_SMS_DAYS", 30);
		
		recommendationFrequency = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "RECOMMENDATION_FREQUENCY", 1);
		
		String[] recDays = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "RECOMMENDATION_DAYS", "0").split(",");
		for (String recDay : recDays)
		{
			recommendationDays.add(Integer.parseInt(recDay));
		}
		nonRecommendationCats = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "NON_RECOMMENDATION_CATEGORIES", "").split(",");
		autoRecommendationSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "AUTO_RECOMMENDATION_SMS", null);
		pickOfTheDayRecommendationSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PICK_OF_THE_DAY_RECOMMENDATION_SMS", null);
		autoRecommendationSMSForMB = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "AUTO_RECOMMENDATION_SMS_FOR_MB", null);
		recommendationClipCount = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "RECOMMENDATION_CLIP_COUNT", 1);
		String tempPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "BULK_PROMO_SMS_FILE_PATH", "");
		if(tempPath != null)
			promoSMSFilePath = tempPath;
		tempPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PICK_OF_DAY_SMS_FILE_PATH", null);
		if(tempPath != null)
			pickofDaySMSFilePath = tempPath;

		recommendationReportMode = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "RECOMMENDATION_REPORT_MODE", "FTP");
		
		spiderPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SPIDER_PATH", null);

		ftpServer = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_SERVER", null);
		ftpPort = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_PORT", -1);
		ftpWaitPeriod = RBTParametersUtils.getParamAsLong(iRBTConstant.REPORTER, "FTP_WAIT", 3000);
		ftpUser = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_USER", null);
		ftpPassword = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_PWD", null);
		ftpRetries = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_RETRIES", -1);
		ftpDir = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_DIR", null);
		ftpTimeout = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_TIMEOUT", 10000);

		_subscriptionConfirmedSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "BULK_SUBSCRIPTION_CONFIRMED", null);
		
		if(m_taskThread == null)
			m_taskThread = new BulkPromoTaskThread();

		return m_taskThread;
	}

	public void run() {
		try {
			isStarted = true;
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			RBTSMS rbtSms = new RBTSMSImpl();
			long time = System.currentTimeMillis() - (intervalToRBTSMS * (1000 * 60 * 60));
			while (TATARBTDaemonOzonized.isOzoneThreadLive()) {
				Calendar calPresent = Calendar.getInstance();
				// if (!allowCOS.equalsIgnoreCase("true"))
				// processCosUpdations();
				allBulkPromoSMS = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSForDate(new Date());
				promosForDeactivation = rbtDBManager.getPromosToDeactivateSubscribers();
				if(calSendBulkActSMS.before(calPresent) || calSendBulkActSMS.equals(calPresent)) {
					activeBulkPromosForActivationMessage = rbtDBManager.getActiveBulkPromos();
					calSendBulkActSMS.add(Calendar.MINUTE, intervalToSendBulkActSMS);
				}
				else
					activeBulkPromosForActivationMessage = null;

				if(calSendBulkDeactSMS.before(calPresent) || calSendBulkDeactSMS.equals(calPresent)) {
					sentCCBulkDeactivationSMS = false;
					activeBulkPromosForDeactivationMessage = rbtDBManager.getActiveBulkPromos();
					calSendBulkDeactSMS.add(Calendar.MINUTE, intervalToSendBulkDeactSMS);
				}
				else
					activeBulkPromosForDeactivationMessage = null;

				if(promosForDeactivation != null)
					deactivateSubscribersForBulkPromos();

				if(allBulkPromoSMS != null)
					sendBulkPromoSMS();

				if(activeBulkPromosForActivationMessage != null)
					sendBulkPromoActivationSMS();

				if(!sentCCBulkDeactivationSMS) {
					sentCCBulkDeactivationSMS = true;
					if(activeBulkPromosForDeactivationMessage != null)
						sendBulkPromoDeactivationSMS();
					m_tataRBTDaemonMain.sendBulkCCDeactivationMessage(m_ozoneMainThread);
				}

				deleteOldSubscriberPromos();

				// Added By SenthilRaja
				if(((System.currentTimeMillis() - time) >= (intervalToRBTSMS * (1000 * 60 * 60)))) {
					rbtSms.deleteOldEntry(days);
					time = System.currentTimeMillis();
				}

				try {
					Parameters autoRecParameter = CacheManagerUtil.getParametersCacheManager().getParameter("TATA_RBT_DAEMON",
					"LAST_RECOMMENDATION_DATE");
					
					Parameters autoRecParameterNext = CacheManagerUtil.getParametersCacheManager().getParameter("TATA_RBT_DAEMON",
					"NEXT_LAST_RECOMMENDATION_DATE");
					
					if(autoRecParameter != null && autoRecParameterNext != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date autoRecDate = dateFormat.parse(autoRecParameter.getValue());
						Date autoRecDateNext = dateFormat.parse(autoRecParameterNext.getValue());
						int currentDayOfWeek = calPresent.getTime().getDay();
						int count = 0;
						int tempCount = 0;
						boolean sendSMSToday = false;
						for(count = 0; count < recommendationDays.size(); count++) {
							logger.info("RBT::current day=="
											+ currentDayOfWeek);
							tempCount = ((Integer)recommendationDays.get(count)).intValue();
							logger.info("RBT::day in arrayList==" + tempCount);
							if(tempCount == currentDayOfWeek) {
								sendSMSToday = true;
								break;
							}
						}
						if(recommendationDays.size() == 1) {
							lastSMSSentDay = currentDayOfWeek;
						}
						if(sendSMSToday && lastSMSSentDay != currentDayOfWeek) {

							// }
							// if ((autoRecDate.getTime() +
							// (recommendationFrequency * 86400000)) <=
							// calPresent
							// .getTime().getTime())
							// {+;
							logger.info("RBT::ready to send sms with count=="
									+ count);
							logger.info("RBT::current day=="
											+ currentDayOfWeek);
//							if(count == 0) {
//								sendPromoRecommendationSMS(autoRecDate, calPresent.getTime(), count);
//							}
//							else {
							sendPromoRecommendationSMS(autoRecDate, autoRecDateNext, count);
//							}
							lastSMSSentDay = currentDayOfWeek;
							logger.info("RBT::last sms sent  day=="
									+ lastSMSSentDay);
							if(count == (recommendationDays.size() - 1)) {
			
								CacheManagerUtil.getParametersCacheManager().updateParameter(
												"TATA_RBT_DAEMON",
												"LAST_RECOMMENDATION_DATE",
												dateFormat
														.format(new Date(
																autoRecDate
																		.getTime()
																		+ (recommendationFrequency * 86400000))));
								CacheManagerUtil.getParametersCacheManager().updateParameter(
										"TATA_RBT_DAEMON",
										"NEXT_LAST_RECOMMENDATION_DATE",
										dateFormat
												.format(new Date(
														autoRecDateNext
																.getTime()
																+ (recommendationFrequency * 86400000))));
							}
							//else if(count == 0) {
											//}
						}
					}
				}
				catch (Exception e) {
					logger.error("", e);
				}
				try {
					Parameters sendPickOfDaySMS = CacheManagerUtil.getParametersCacheManager().getParameter("TATA_RBT_DAEMON",
					"SEND_PICK_OF_DAY_SMS");
					
					// lastSMSSentDayForPickOfTheDay
					int currentDayOfWeek = calPresent.getTime().getDay();
					logger.info("RBT::last sms sent  day=="
							+ lastSMSSentDayForPickOfTheDay);
					logger.info("RBT::current day==" + currentDayOfWeek);

					if(sendPickOfDaySMS != null && sendPickOfDaySMS.getValue().length() > 0) {
						String strSendPickOfDaySMS = sendPickOfDaySMS.getValue().trim();
						if(strSendPickOfDaySMS.indexOf("true") != -1) {
							Parameters getPickOfDayCat = CacheManagerUtil.getParametersCacheManager().getParameter(
									"TATA_RBT_DAEMON", "PICK_OF_DAY_CAT");
							if(getPickOfDayCat.getValue() != null
									&& getPickOfDayCat.getValue().length() > 0
									&& lastSMSSentDayForPickOfTheDay != currentDayOfWeek) {
								logger.info("RBT::sending sms for today "
										+ calPresent.getTime());
								String pickOfDayCat = getPickOfDayCat.getValue().trim();
								sendPickOfDayRecommendationSMS(calPresent.getTime(), pickOfDayCat);
								lastSMSSentDayForPickOfTheDay = currentDayOfWeek;
							}
							else {
								logger.info("RBT::sms already been sent for today");
							}
						}
					}
				}
				catch (Exception e) {
					logger.error("", e);
				}

				int actualSleeepTime = 0;

				actualSleeepTime = sleepMinutes;

				if(actualSleeepTime > 0) {
					try {
						logger.info("RBT::inside try to sleep for "
								+ actualSleeepTime + " minutes");
						Thread.sleep(1000 * 60 * actualSleeepTime);
						logger.info("RBT::inside try after sleep for "
								+ actualSleeepTime + " minutes");
					}
					catch (Exception e) {
						logger.info("RBT::exception " + e);
					}
				}

			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		finally {
			logger.info("RBT:: TATARBTDaemonOzonized.isOzoneThreadLive() = "
					+ TATARBTDaemonOzonized.isOzoneThreadLive());
			logger.info("RBT:: exited BulkPromoTaskThread");
		}
	}

	void sendBulkPromoActivationSMS() {
		for(int i = 0; i < activeBulkPromosForActivationMessage.length; i++) {
			try {
				m_tataRBTDaemonMain.sendBulkActivationMessage(
						(BulkPromo)activeBulkPromosForActivationMessage[i], m_ozoneMainThread);
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	void sendBulkPromoDeactivationSMS() {
		for(int i = 0; i < activeBulkPromosForDeactivationMessage.length; i++) {
			try {
				m_tataRBTDaemonMain.sendBulkDeactivationMessage(
						(BulkPromo)activeBulkPromosForDeactivationMessage[i], m_ozoneMainThread);
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	void sendBulkPromoSMS() {
		for(int i = 0; i < allBulkPromoSMS.size(); i++) {
			try {
				m_tataRBTDaemonMain.sendBulkPromoSMS(allBulkPromoSMS.get(i), m_ozoneMainThread);
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	void deactivateSubscribersForBulkPromos() {
		try {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			if(promosForDeactivation != null) {
				logger.info("RBT:: no of promosForDeactivation = "
						+ promosForDeactivation.length);
				for(int i = 0; i < promosForDeactivation.length; i++) {
					Subscriber[] allbulkSubscribers = rbtDBManager
							.getBulkPromoSubscribers(promosForDeactivation[i].bulkPromoId());
					if(allbulkSubscribers != null) {
						for(int subCount = 0; subCount < allbulkSubscribers.length; subCount++) {
							if(!allbulkSubscribers[subCount].subYes().equals(STATE_ACTIVATED))// &&
								// !allbulkSubscribers[subCount].availedPromoDownload())
								deactivateSubscriber(allbulkSubscribers[subCount], rbtDBManager);
						}
					}
					rbtDBManager.updateProcessedDeactivation(
							promosForDeactivation[i].bulkPromoId(), "y");
					;
				}
			}
			else {
				logger.info("RBT:: no bulk promotions to deactivate subscribers");
			}

		}
		catch (Exception e) {
			logger.error("", e);
		}
	}

	/*
	 * private void processCosUpdations() { RBTDBManager rbtDBManager =
	 * RBTDBManager.init(db_url, m_nConn); CosDetail[] cosDetails =
	 * rbtDBManager.getCosForPromoUpdate(); if (cosDetails != null) { for (int i =
	 * 0; i < cosDetails.length; i++) { if
	 * (cosDetails[i].accessMode().indexOf("BULK") < 0) { BulkPromoSMS
	 * bulkPromoSMS = rbtDBManager
	 * .getBulkPromoSMSForDate(cosDetails[i].cosID(), cosDetails[i].endDate());
	 * if (bulkPromoSMS == null) {
	 * rbtDBManager.addBulkPromoSMS(cosDetails[i].cosID(),
	 * cosDetails[i].endDate(), null, "y");
	 * rbtDBManager.renewPromoSubscribers(cosDetails[i] .cosID(),
	 * cosDetails[i].renewalCosID()); } } } } }
	 */

	private boolean deactivateSubscriber(Subscriber subscriber, RBTDBManager rbtDBManager) {
		if(canBeDeactivated(subscriber))
			return rbtDBManager.deactivateSubscriber(subscriber.subID(), subscriber.activatedBy(),
					subscriber.endDate(), true, true, true);
		else {
			rbtDBManager.setSubscriptionYes(subscriber.subID(), iRBTConstant.STATE_ACTIVATED);
			if(_subscriptionConfirmedSMS != null) {
				try {
					Tools.sendSMS(db_url, m_tataRBTDaemonMain.m_smsNo, subscriber.subID(),
							_subscriptionConfirmedSMS, m_tataRBTDaemonMain.insertSMSInDuplicate);
				}
				catch (OnMobileException e) {
					logger.error("", e);
				}
			}
		}
		return false;
	}
	
	/**
	 * @author Sreekar
	 * @return Returns if user can be deactivated as per Huawei. First it checks
	 *         if COS id is the same and if same checks users library to be
	 *         empty to deactivate
	 */
	private boolean canBeDeactivated(Subscriber sub) {
		boolean retVal = true;
		
		retVal = checkSubStatusToDeactivate(sub);
		if(retVal)
			retVal = checkSubLibToDeactivate(sub);
		logger.info("RBT::returning " + retVal + " for sub " + sub.subID());
		return retVal;
	}
	
	private boolean checkSubLibToDeactivate(Subscriber sub) {
		boolean retVal = true;

		try {
			String httpLink = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "").getValue();
			StringBuffer urlstrBuf = new StringBuffer(httpLink);
			String querySongsPage = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.TATADAEMON, "QUERY_SONGS_PAGE", "").getValue();
			urlstrBuf.append(TataUtility.getOperatorAccount(null) + "&");
			urlstrBuf.append(TataUtility.getOperatorPassword(null) + "&");
			urlstrBuf.append("phonenumber=" + sub.subID() + "&");
			urlstrBuf.append(TataUtility.getOperatorCode(null));

			String url = urlstrBuf.toString();
			Date requestTime = new Date();
			String result = RBTHTTPProcessing.getInstance().makeRequest1(url, sub.subID(),
					TATARBTDaemonMain.app);
			Date responseTime = new Date();

			String event = "QUERY_PERSONAL_TONE";
			String requestType = "query_songs";
			String diffTime = String.valueOf(responseTime.getTime() - requestTime.getTime());
			String prepaidStatus = sub.prepaidYes() ? "PRE_PAID" : "POST_PAID";

			if(result != null) {
				if(result.length() > 1 && (result.indexOf("|") != -1)) {
					addToAccounting(event, sub.subID(), prepaidStatus, requestType,
							"proper_response", requestTime, diffTime, url, result);
					logger.info("RBT::User " + sub.subID()
							+ " has downloaded a song. Not deactivating");
					retVal = false;
				}
				addToAccounting(event, sub.subID(), prepaidStatus, requestType, "error_response",
						requestTime, diffTime, url, result);
			}
			else
				addToAccounting(event, sub.subID(), prepaidStatus, requestType, "null_response",
						requestTime, diffTime, url, result);
		}
		catch (Exception e) {
			logger.error("", e);
		}
		return retVal;
	}
	
	private boolean checkSubStatusToDeactivate(Subscriber sub) {
		boolean retVal = true;

		try {
			String httpLink = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.TATADAEMON, "HTTP_LINK", "").getValue();
			StringBuffer urlstrBuf = new StringBuffer(httpLink);
			String querySongsPage = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.TATADAEMON, "QUERY_SONGS_PAGE", "").getValue();
			urlstrBuf.append(querySongsPage);
			urlstrBuf.append(TataUtility.getOperatorAccount(null) + "&");
			urlstrBuf.append(TataUtility.getOperatorPassword(null) + "&");
			urlstrBuf.append("phonenumber=" + sub.subID() + "&");
			urlstrBuf.append(TataUtility.getOperatorCode(null));

			String url = urlstrBuf.toString();
			Date requestTime = new Date();
			String result = RBTHTTPProcessing.getInstance().makeRequest1(url, sub.subID(),
					TATARBTDaemonMain.app);
			Date responseTime = new Date();

			String event = "QUERY_PERSONAL_TONE";
			String requestType = "query_songs";
			String diffTime = String.valueOf(responseTime.getTime() - requestTime.getTime());
			String prepaidStatus = sub.prepaidYes() ? "PRE_PAID" : "POST_PAID";

			if(result != null) {
				if(result.length() > 1 && (result.indexOf("|") != -1)) {
					addToAccounting(event, sub.subID(), prepaidStatus, requestType,
							"proper_response", requestTime, diffTime, url, result);
					logger.info("RBT::User " + sub.subID()
							+ " has downloaded a song. Not deactivating");
					retVal = false;
				}
				addToAccounting(event, sub.subID(), prepaidStatus, requestType, "error_response",
						requestTime, diffTime, url, result);
			}
			else
				addToAccounting(event, sub.subID(), prepaidStatus, requestType, "null_response",
						requestTime, diffTime, url, result);
		}
		catch (Exception e) {
			logger.error("", e);
		}
		return retVal;
	}

	public boolean didStart() {
		return isStarted;
	}

	private void deleteOldSubscriberPromos() {
		RBTDBManager.getInstance().deleteOldSubscriberPromos();
	}

	private void sendPromoRecommendationSMS(Date autoRecDate, Date curDate, int count) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		HashMap updatedCategories = rbtDBManager.getClipMapByStartTime(autoRecDate, curDate);
		HashMap mbMap = rbtDBManager.getMBMapByStartTime(autoRecDate, curDate);

		if(updatedCategories == null)
			updatedCategories = mbMap;
		else if(mbMap != null && mbMap.size() > 0) {
			Set mbcats = mbMap.keySet();
			for(Iterator iterator = mbcats.iterator(); iterator.hasNext();) {
				String categoryID = (String)iterator.next();
				updatedCategories.put(categoryID, mbMap.get(categoryID));
			}
		}

		if(updatedCategories == null || updatedCategories.size() == 0) {
			logger.info("RBT:: No conetent release found for the period " + autoRecDate + " and "
							+ curDate + ".");
			return;
		}
		for(int i = 0; nonRecommendationCats != null && i < nonRecommendationCats.length; i++) {
			logger.info("RBT:: nonRecommendationCats = "
					+ nonRecommendationCats[i]);
			updatedCategories.remove(nonRecommendationCats[i]);
		}

		HashMap smsTextMap = new HashMap();
		HashMap categoryMap = new HashMap();
		Set categories = updatedCategories.keySet();
		String[] categoryIDs = null;
		for(Iterator iterator = categories.iterator(); iterator.hasNext();) {
			String categoryID = (String)iterator.next();
			Category category = rbtDBManager.getCategory(Integer.parseInt(categoryID));
			if(category == null)
				continue;

			ArrayList clipList = (ArrayList)updatedCategories.get(categoryID);
			String newClips = "";
			int tmpRecClipCnt = recommendationClipCount;
			int i = recommendationClipCount * count;
			if(!((clipList.size()) > recommendationClipCount * count)) {
				continue;
			}
			for(; i < clipList.size() && tmpRecClipCnt > 0; i++, tmpRecClipCnt--)

			{
				String clipName = (String)clipList.get(i);
				newClips += clipName + ", ";
			}
			if(newClips != null && newClips.length() > 0) {
				newClips = newClips.trim();
				newClips = newClips.substring(0, newClips.lastIndexOf(","));
			}
			// if(tmpRecClipCnt > 0 && i < clipList.size())
			// newClips +=clipList.get(clipList.size() - 1);

			String tmpautoRecSMS = autoRecommendationSMS;
			if(category.getType() == BOUQUET)
				tmpautoRecSMS = autoRecommendationSMSForMB;

			if(tmpautoRecSMS == null) {
				logger.info("RBT:: Auto Recommendation SMS not defined");
				return;
			}

			tmpautoRecSMS = Tools.findNReplace(tmpautoRecSMS, "%CLIPS%", newClips);
			tmpautoRecSMS = Tools.findNReplace(tmpautoRecSMS, "%CATEGORY%", category.getName());
			logger.info("RBT:: adding a new sms text as " + tmpautoRecSMS
					+ "with categoryId" + categoryID);
			smsTextMap.put(categoryID, tmpautoRecSMS);
			logger.info("RBT:: adding a new category in map  as "
					+ category.getName() + "with categoryId" + categoryID);

			categoryMap.put(categoryID, category.getName());
		}

		if(smsTextMap != null && smsTextMap.size() >= 0) {
			PopulateDupDownloads downloads = new PopulateDupDownloads();
			downloads.runDownloadUpdate();
			categories = null;
			categories = smsTextMap.keySet();
			categoryIDs = (String[])categories.toArray(new String[0]);
			//HashMap subscriberMap = downloads.getSubscribersForRecommendation(categoryIDs);
			HashMap subscriberMap = null;
			if(categoryIDs!=null && categoryIDs.length>0){ 
                subscriberMap = downloads.getSubscribersForRecommendation(categoryIDs); 
			} 
			Calendar calendar = Calendar.getInstance();
			String dateStr = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH) + 1)
					+ "-" + calendar.get(Calendar.YEAR);
			String zippedlistFileName = promoSMSFilePath
					+ "/AUTO_RECOMMENDATION/Auto_Recommendation_" + dateStr + ".zip";
			String zipTempFolderName = promoSMSFilePath + "/zipTempFolder";
			File zipTempFolder = new File(zipTempFolderName);
			if(!zipTempFolder.exists())
				zipTempFolder.mkdirs();

			File zippedlistFile = null;

			if(subscriberMap != null && subscriberMap.size() > 0) {
				HashMap subsCountMap = new HashMap();
				String[] fileNames = new String[subscriberMap.size()];
				int flCnt = 0;
				categories = smsTextMap.keySet();
				for(Iterator iterator = categories.iterator(); iterator.hasNext();) {
					String categoryID = (String)iterator.next();
					String categoryName = (String)categoryMap.get(categoryID);
					String tmpautoRecSMS = (String)smsTextMap.get(categoryID);
					logger.info("RBT:: sms text for text file is"
							+ tmpautoRecSMS);
					ArrayList subscriberList = (ArrayList)subscriberMap.get(categoryID);
					if(subscriberList != null && subscriberList.size() > 0) {
						subsCountMap.put(categoryID, subscriberList.size() + "");
						try {
							String fileName = categoryID + "_" + categoryName;
							File listFile = createListFile(fileName, subscriberList);
							logger.info("RBT:: listFile = " + listFile.getAbsolutePath());
							if(listFile != null) {
								fileNames[flCnt++] = fileName;
								logger.info("RBT:: flCnt = " + flCnt);
								copyFile(listFile.getAbsolutePath(), zipTempFolderName + "/"
										+ fileName + ".txt");
								m_tataRBTDaemonMain.sendPromoRecommendationSMS(listFile
										.getAbsolutePath(), tmpautoRecSMS, m_ozoneMainThread);
							}
						}
						catch (Throwable e) {
							logger.error("", e);
						}
					}

				}

				zippedlistFile = ZipFiles.zipFiles(zippedlistFileName, zipTempFolder.listFiles(),
						"recommendation");
				generateRecommendationReport(zippedlistFile, fileNames, smsTextMap, subsCountMap);
				try {
					File[] files = zipTempFolder.listFiles();
					for(int f = 0; f < files.length; f++)
						files[f].delete();
				}
				catch (Exception e) {
					logger.error("", e);
				}

			}
			else
				logger.info("RBT:: subscriberMap = null");
		}
		else
			logger.info("RBT:: No conetent release found for the period " + autoRecDate + " and "
							+ curDate + ".");
	}

	private File createListFile(String fileName, ArrayList subscriberList) {
		SimpleDateFormat fileNameFormatter = new SimpleDateFormat("MMddHHmmssSSS");
		fileName = fileName + "_" + fileNameFormatter.format(new Date()) + ".txt";

		File listFile = null;

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			File dir = new File(promoSMSFilePath);
			if(!dir.exists())
				dir.mkdirs();
			File recDir = new File(dir, "AUTO_RECOMMENDATION");
			if(!recDir.exists())
				recDir.mkdirs();
			listFile = new File(recDir, fileName);

			fileWriter = new FileWriter(listFile.getAbsolutePath());
			bufferedWriter = new BufferedWriter(fileWriter);

			for(Iterator iterator = subscriberList.iterator(); iterator.hasNext();) {
				String subscriberID = (String)iterator.next();
				bufferedWriter.write(subscriberID);
				bufferedWriter.newLine();
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		finally {
			try {
				bufferedWriter.close();
				fileWriter.close();
			}
			catch (IOException e) {
				logger.error("", e);
			}
		}

		return listFile;
	}

	private void generateRecommendationReport(File zippedlistFile, String[] fileNames,
			HashMap smsTextMap, HashMap subsCountMap) {
		String htmlReportAct = "<HTML><BODY style=\"font-family: 'tahoma'\">";
		htmlReportAct += "<H4 align=\"center\" style=\"color:'#0055AA'\"><U>Auto Recommendation Report</U></H4>";
		htmlReportAct += "<TABLE border=\"2\" width=\"100%\">";
		htmlReportAct += "<TR bgcolor=\"#0055AA\" style=\"font-size: 12\">";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Category ID</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Category Name</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">SMS Text</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">No. Of Subscribers</FONT></TH>";
		htmlReportAct += "</TR>";

		for(int i = 0; i < fileNames.length; i++) {
			String categoryID = fileNames[i].substring(0, fileNames[i].indexOf('_'));
			String categoryName = fileNames[i].substring(fileNames[i].indexOf('_') + 1);
			String smsText = (String)smsTextMap.get(categoryID);
			String subCount = (String)subsCountMap.get(categoryID);

			htmlReportAct += "<TR style=\"font-size: 12\">";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + categoryID + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + categoryName + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + smsText + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + subCount + "</TD>";
			htmlReportAct += "</TR>";
		}
		htmlReportAct += "</TABLE>";
		htmlReportAct += "</BODY></HTML>";

		Calendar calendar = Calendar.getInstance();
		String dateStr = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH) + 1)
				+ "-" + calendar.get(Calendar.YEAR);
		String htmlReportFileName = promoSMSFilePath + "/AUTO_RECOMMENDATION/Auto_Recommendation_"
				+ dateStr + ".html";

		try {
			FileWriter fileWriter = new FileWriter(htmlReportFileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(htmlReportAct);
			bufferedWriter.flush();
			bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		}
		catch (IOException e) {
			logger.error("", e);
		}

		File htmlReportFile = new File(htmlReportFileName);
		if(recommendationReportMode.equalsIgnoreCase("FTP")) {
			FTPClient ftp = null;
			for(int i = 0; i < ftpRetries; i++) {
				try {
					logger.info("RBT:: Creating FTP Connection");
					ftp = new FTPClient(ftpServer, ftpPort);
					ftp.setTimeout(ftpTimeout);
					ftp.login(ftpUser, ftpPassword);
					ftp.setConnectMode(FTPConnectMode.PASV);
					ftp.setType(FTPTransferType.BINARY);
					ftp.chdir(ftpDir);
					Calendar curcal = Calendar.getInstance();
					String dirDate = getMONTH(curcal.get(Calendar.MONTH))
							+ curcal.get(Calendar.DATE);
					try {
						ftp.mkdir(dirDate);
						ftp.chdir(dirDate);
					}
					catch (Exception exe) {
						ftp.chdir(dirDate);
					}

					if(zippedlistFile != null && zippedlistFile.exists()) {
						ftp.put(zippedlistFile.getAbsolutePath(), zippedlistFile.getName());
						logger.info("RBT:: "
								+ zippedlistFile.getName() + " uploaded successfully...!");
						zippedlistFile.delete();
					}
					if(htmlReportFile != null && htmlReportFile.exists()) {
						ftp.put(htmlReportFile.getAbsolutePath(), htmlReportFile.getName());
						logger.info("RBT:: "
								+ htmlReportFile.getName() + " uploaded successfully...!");
						htmlReportFile.delete();
					}

					break;
				}
				catch (Exception exe) {
					logger.error("", exe);
					try {
						Thread.sleep(ftpWaitPeriod);
					}
					catch (Exception ex) {
					}
					continue;
				}
				finally {
					if(ftp != null) {
						try {
							ftp.quit();
						}
						catch (FTPException e) {
							e.printStackTrace();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		else if(recommendationReportMode.equalsIgnoreCase("SPIDER")) {
			if(spiderPath != null) {
				File spiderDir = new File(spiderPath);
				File zippedlistSpiderFile = new File(spiderDir, zippedlistFile.getName());
				File htmlReportSpiderFile = new File(spiderDir, htmlReportFile.getName());
				Tools.moveFile(zippedlistFile, zippedlistSpiderFile);
				Tools.moveFile(htmlReportFile, htmlReportSpiderFile);
			}
			else {
				logger.info("RBT:: Spider path not defined");
			}
		}
	}

	private void copyFile(String source, String destination) throws IOException {
		try {
			logger.info("Copying: " + source + " To: " + destination);
			FileInputStream fis = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();
			logger.info(destination + " copied.");
		}
		catch (IOException ioe) {
			logger.error("", ioe);
			throw ioe;
		}
	}

	public static String getMONTH(int i_mm) {
		String month[] = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct",
				"nov", "dec" };
		return month[i_mm];
	}

	private void sendPickOfDayRecommendationSMS(Date curDate, String pickofTheDayCat) {

		try {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			HashMap smsTextMap = new HashMap();
			List<SitePrefix> sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
			ArrayList circleIDs = new ArrayList();
			for(int countPrefix = 0; countPrefix < sitePrefix.size(); countPrefix++) {
				if(sitePrefix.get(countPrefix).getCircleID() != null
						&& sitePrefix.get(countPrefix).getCircleID().length() > 0) {
					circleIDs.add(sitePrefix.get(countPrefix).getCircleID().trim());
				}
			}
			PickOfTheDay[] pickOfTheDay = rbtDBManager.getPickOfTheDayForTATAForAllCircle(null);
			String smsTextKey = null;
			char temp1 = 'y';
			int clipId = -1;
			String newClips = "";
			Clips clip = null;

			if(pickOfTheDay != null && pickOfTheDay.length > 0) {
				for(int countArr = 0; countArr < pickOfTheDay.length; countArr++) {
					  if (pickOfTheDay[countArr].profile()==null ||(pickOfTheDay[countArr].profile()!=null && !pickOfTheDay[countArr].profile().equalsIgnoreCase("1+1"))) {

						smsTextKey = pickOfTheDay[countArr].circleID().trim();
						temp1 = pickOfTheDay[countArr].prepaidYes();
						clipId = pickOfTheDay[countArr].clipID();
						clip = rbtDBManager.getClip(clipId);
						String tmpautoRecSMS = pickOfTheDayRecommendationSMS;
						if (tmpautoRecSMS == null) {
							logger.info("RBT:: Auto Recommendation SMS not defined");
							return;
						}
						if (clip == null) {
							continue;
						} else {
							newClips = clip.name();
						}
						if (temp1 == 'y') {
							if (smsTextKey != null
									&& smsTextKey.equalsIgnoreCase("all")) {
								for (int i = 0; i < circleIDs.size(); i++) {
									smsTextKey = (String) circleIDs.get(i);
									smsTextKey = smsTextKey + "_prepaid";
									if (!smsTextMap.containsKey(smsTextKey)) {
										tmpautoRecSMS = Tools.findNReplace(
												tmpautoRecSMS, "%CLIPS%",
												newClips);
										smsTextMap.put(smsTextKey,
												tmpautoRecSMS);
									}
								}
							} else {
								smsTextKey = smsTextKey + "_prepaid";
								if (!smsTextMap.containsKey(smsTextKey)) {
									tmpautoRecSMS = Tools.findNReplace(
											tmpautoRecSMS, "%CLIPS%", newClips);
									smsTextMap.put(smsTextKey, tmpautoRecSMS);
								}
							}
						} else if (temp1 == 'n') {
							if (smsTextKey != null
									&& smsTextKey.equalsIgnoreCase("all")) {
								for (int i = 0; i < circleIDs.size(); i++) {
									smsTextKey = (String) circleIDs.get(i);
									smsTextKey = smsTextKey + "_postpaid";
									if (!smsTextMap.containsKey(smsTextKey)) {
										tmpautoRecSMS = Tools.findNReplace(
												tmpautoRecSMS, "%CLIPS%",
												newClips);
										smsTextMap.put(smsTextKey,
												tmpautoRecSMS);
									}
								}
							} else {
								smsTextKey = smsTextKey + "_postpaid";
								if (!smsTextMap.containsKey(smsTextKey)) {
									tmpautoRecSMS = Tools.findNReplace(
											tmpautoRecSMS, "%CLIPS%", newClips);
									smsTextMap.put(smsTextKey, tmpautoRecSMS);
								}
							}
						} else {
							if (smsTextKey != null
									&& smsTextKey.equalsIgnoreCase("all")) {
								for (int i = 0; i < circleIDs.size(); i++) {
									smsTextKey = (String) circleIDs.get(i);
									smsTextKey = smsTextKey + "_postpaid";
									if (!smsTextMap.containsKey(smsTextKey)) {
										tmpautoRecSMS = Tools.findNReplace(
												tmpautoRecSMS, "%CLIPS%",
												newClips);
										smsTextMap.put(smsTextKey,
												tmpautoRecSMS);
									}
								}
								for (int i = 0; i < circleIDs.size(); i++) {
									smsTextKey = (String) circleIDs.get(i);
									smsTextKey = smsTextKey + "_prepaid";
									if (!smsTextMap.containsKey(smsTextKey)) {
										tmpautoRecSMS = Tools.findNReplace(
												tmpautoRecSMS, "%CLIPS%",
												newClips);
										smsTextMap.put(smsTextKey,
												tmpautoRecSMS);
									}
								}
							} else {
								if (!smsTextMap.containsKey(smsTextKey
										+ "_prepaid")) {
									tmpautoRecSMS = Tools.findNReplace(
											tmpautoRecSMS, "%CLIPS%", newClips);
									smsTextMap.put(smsTextKey + "_prepaid",
											tmpautoRecSMS);
								}
								if (!smsTextMap.containsKey(smsTextKey
										+ "_postpaid")) {
									tmpautoRecSMS = Tools.findNReplace(
											tmpautoRecSMS, "%CLIPS%", newClips);
									smsTextMap.put(smsTextKey + "_postpaid",
											tmpautoRecSMS);
								}
							}
						}
						smsTextKey = null;
						temp1 = 'y';
						clipId = -1;
						newClips = "";
					}
				}
			}
			else {
				logger.info("RBT:: No pick of day available for" + curDate.getDay() + "-"
								+ curDate.getMonth());
				return;
			}
			Set setTemp = smsTextMap.keySet();
			Iterator iter = setTemp.iterator();
			while (iter.hasNext()) {
				String tempKey = (String)iter.next();
				String tempSMSText = (String)smsTextMap.get(tempKey);
				logger.info("RBT:: smsm text=="
						+ tempSMSText + " for key==" + tempKey);
			}
			if(smsTextMap != null && smsTextMap.size() >= 0) {
				PopulateDupDownloads downloads = new PopulateDupDownloads();
				downloads.runDownloadUpdate();// make sure its modified for
												// pick of the day as well as
												// for mulitple autoreco
				String[] categoryIDs = { pickofTheDayCat };
				HashMap categorySubsMap=null;
				if(pickofTheDayCat!=null && pickofTheDayCat.length()>0){ 
                    categorySubsMap=downloads.getSubscribersForRecommendationForHotSongs(categoryIDs); 
				} 
				ArrayList prepaidSubs = new ArrayList();
				ArrayList postpaidSubs = new ArrayList();

				if(categorySubsMap != null && categorySubsMap.size() > 0) {
					Set keys = categorySubsMap.keySet();
					Iterator iterator = keys.iterator();
					while (iterator.hasNext()) {
						String categoryID = (String)iterator.next();
						ArrayList subscriberList = (ArrayList)categorySubsMap.get(categoryID);
						for(int i = 0; i < subscriberList.size(); i++) {
							String subId = (String)subscriberList.get(i);
							if(subId != null) {
								subId = subId.trim();
								Subscriber subs = rbtDBManager.getSubscriber(subId);
								if(rbtDBManager.isSubscriberDeactivated(subs))
									continue;
								if(subs.prepaidYes()) {
									prepaidSubs.add(subId);
									// logger.info("RBT::
									// adding subscriber id=="+subId+" in
									// prepaid arraylist");
								}
								else {
									postpaidSubs.add(subId);
									// logger.info("RBT::
									// adding subscriber id=="+subId+" in
									// postpaid arraylist");
								}
							}

						}
					}
				}
				HashMap subscriberMap = new HashMap();
				for(int i = 0; i < prepaidSubs.size(); i++) {
					String tempKey = null;
					String cirId = rbtDBManager.getCircleId((String)prepaidSubs.get(i));
					if(cirId != null && cirId.length() > 0) {
						cirId = cirId.trim();
					}
					tempKey = cirId + "_prepaid";
					if(subscriberMap.containsKey(tempKey)) {
						ArrayList subsList = (ArrayList)subscriberMap.get(tempKey);
						String tempSub = (String)prepaidSubs.get(i);
						subsList.add(tempSub);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)subsList.get(subsList.size()-1)+" in
						// prepaid arraylist and then into hashmap for
						// key=="+tempKey);
						subscriberMap.put(tempKey, subsList);
						// ArrayList
						// tempArr=(ArrayList)subscriberMap.get(tempKey);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)tempArr.get(tempArr.size()-1)+" in
						// prepaid arraylist and then into hashmap for
						// key=="+tempKey);
					}
					else {
						ArrayList subsList = new ArrayList();
						String tempSub = (String)prepaidSubs.get(i);
						subsList.add(tempSub);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)subsList.get(subsList.size()-1)+" in
						// prepaid arraylist and then into hashmap for
						// key=="+tempKey);
						subscriberMap.put(tempKey, subsList);
						// ArrayList
						// tempArr=(ArrayList)subscriberMap.get(tempKey);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)tempArr.get(tempArr.size()-1)+" in
						// prepaid arraylist and then into hashmap for
						// key=="+tempKey);
					}
				}
				for(int i = 0; i < postpaidSubs.size(); i++) {
					String tempKey = null;
					String cirId = rbtDBManager.getCircleId((String)postpaidSubs.get(i));
					if(cirId != null && cirId.length() > 0) {
						cirId = cirId.trim();
					}
					tempKey = cirId + "_postpaid";
					if(subscriberMap.containsKey(tempKey)) {
						ArrayList subsList = (ArrayList)subscriberMap.get(tempKey);
						String tempSub = (String)postpaidSubs.get(i);
						subsList.add(tempSub);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)subsList.get(subsList.size()-1)+" in
						// postpaid arraylist and then into hashmap for
						// key=="+tempKey);
						subscriberMap.put(tempKey, subsList);
						// ArrayList
						// tempArr=(ArrayList)subscriberMap.get(tempKey);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)tempArr.get(tempArr.size()-1)+" in
						// postpaid arraylist and then into hashmap for
						// key=="+tempKey);
					}
					else {
						ArrayList subsList = new ArrayList();
						String tempSub = (String)postpaidSubs.get(i);
						subsList.add(tempSub);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)subsList.get(subsList.size()-1)+" in
						// postpaid arraylist and then into hashmap for
						// key=="+tempKey);
						subscriberMap.put(tempKey, subsList);
						// ArrayList
						// tempArr=(ArrayList)subscriberMap.get(tempKey);
						// logger.info("RBT:: adding
						// subscriber
						// id=="+(String)tempArr.get(tempArr.size()-1)+" in
						// postpaid arraylist and then into hashmap for
						// key=="+tempKey);
					}
				}

				Set setTemp1 = subscriberMap.keySet();
				Iterator iter1 = setTemp1.iterator();
				while (iter1.hasNext()) {
					String tempKey = (String)iter1.next();
					ArrayList tempSMSText = (ArrayList)subscriberMap.get(tempKey);
					logger.info("RBT::key=="
							+ tempKey + " and arraylist size ==" + tempSMSText.size());
				}

				Calendar calendar = Calendar.getInstance();
				String dateStr = calendar.get(Calendar.DATE) + "-"
						+ (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR);
				String zippedlistFileName = pickofDaySMSFilePath
						+ "/PICK_OF_THE_DAY/Pick_Of_Day_Recommendation_" + dateStr + ".zip";
				String zipTempFolderName = pickofDaySMSFilePath + "/zipTempFolder";
				File zipTempFolder = new File(zipTempFolderName);
				if(!zipTempFolder.exists())
					zipTempFolder.mkdirs();

				File zippedlistFile = null;

				if(subscriberMap != null && subscriberMap.size() > 0 && (categoryIDs!=null && categoryIDs.length>0)){
					logger.info("RBT::  subscriberMap != null && subscriberMap.size() > 0 and map size=="
									+ subscriberMap.size());
					HashMap subsCountMap = new HashMap();
					String[] fileNames = new String[subscriberMap.size()];
					int flCnt = 0;
					Set keyset = smsTextMap.keySet();
					logger.info("RBT:: total no of keys in smsTextMap==" + keyset.size());
					for(Iterator iterator = keyset.iterator(); iterator.hasNext();) {
						String keyVal = (String)iterator.next();
						logger.info("RBT:: looking for keyVal = " + keyVal);
						String tmpautoRecSMS = (String)smsTextMap.get(keyVal);
						logger.info("RBT:: smstext for keyVal = " + tmpautoRecSMS);
						ArrayList subscriberList = null;
						if(subscriberMap != null && subscriberMap.containsKey(keyVal)) {
							subscriberList = (ArrayList)subscriberMap.get(keyVal);
							logger.info("RBT:: subscriber Map Contains keyVal = " + keyVal);
						}
						else {
							logger.info("RBT:: subscriber Map dont Contains keyVal = " + keyVal);
						}
						if(subscriberList != null && subscriberList.size() > 0) {
							logger.info("RBT:: keyVal = " + keyVal + " tmpautoRecSMS==" + tmpautoRecSMS
											+ " subscriber arraylist size=="
											+ subscriberList.size());

							subsCountMap.put(keyVal, subscriberList.size() + "");
							try {
								String fileName = keyVal;
								File listFile = createListFileForPickOfDay(fileName, subscriberList);
								logger.info("RBT:: listFile = " + listFile.getAbsolutePath());
								if(listFile != null) {
									fileNames[flCnt++] = fileName;
									logger.info("RBT:: flCnt = " + flCnt);
									copyFile(listFile.getAbsolutePath(), zipTempFolderName + "/"
											+ fileName + ".txt");
									m_tataRBTDaemonMain.sendPromoRecommendationSMS(listFile
											.getAbsolutePath(), tmpautoRecSMS, m_ozoneMainThread);
								}
							}
							catch (Throwable e) {
								logger.error("", e);
							}

						}
						else {
							logger.info("RBT:: keyVal = " + keyVal + " tmpautoRecSMS==" + tmpautoRecSMS
											+ " subscriber arraylist size is zero");
							continue;
						}
					}
					logger.info("RBT:: ready to create zip file");
					zippedlistFile = ZipFiles.zipFiles(zippedlistFileName, zipTempFolder
							.listFiles(), "recommendation");
					generateRecommendationReportForPickOfTheDay(zippedlistFile, fileNames,
							smsTextMap, subsCountMap);
					// make sure zipped file generation is changed accordingly
					// for pick of the day
					try {
						File[] files = zipTempFolder.listFiles();
						for(int f = 0; f < files.length; f++)
							files[f].delete();
					}
					catch (Exception e) {
						logger.error("", e);
					}

				}
				else
					logger.info("RBT:: subscriberMap = null");
			}
			else
				logger.info("RBT:: No conetent release found pick of day for " + curDate + ".");
		}
		catch (Exception e) {
			   logger.error("", e);
			e.printStackTrace();
		}

	}

	private File createListFileForPickOfDay(String fileName, ArrayList subscriberList) {

		SimpleDateFormat fileNameFormatter = new SimpleDateFormat("MMddHHmmssSSS");
		fileName = fileName + "_" + fileNameFormatter.format(new Date()) + ".txt";
		File listFile = null;
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			File dir = new File(pickofDaySMSFilePath);
			if(!dir.exists())
				dir.mkdirs();
			File recDir = new File(dir, "PICK_OF_THE_DAY");
			if(!recDir.exists())
				recDir.mkdirs();
			listFile = new File(recDir, fileName);
			fileWriter = new FileWriter(listFile.getAbsolutePath());
			bufferedWriter = new BufferedWriter(fileWriter);
			for(Iterator iterator = subscriberList.iterator(); iterator.hasNext();) {
				String subscriberID = (String)iterator.next();
				bufferedWriter.write(subscriberID);
				bufferedWriter.newLine();
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		finally {
			try {
				bufferedWriter.close();
				fileWriter.close();
			}
			catch (IOException e) {
				logger.error("", e);
			}
		}
		return listFile;
	}

	private void generateRecommendationReportForPickOfTheDay(File zippedlistFile,
			String[] fileNames, HashMap smsTextMap, HashMap subsCountMap) {

		String htmlReportAct = "<HTML><BODY style=\"font-family: 'tahoma'\">";
		htmlReportAct += "<H4 align=\"center\" style=\"color:'#0055AA'\"><U>Pick Of The Day-Recommendation Report</U></H4>";
		htmlReportAct += "<TABLE border=\"2\" width=\"100%\">";
		htmlReportAct += "<TR bgcolor=\"#0055AA\" style=\"font-size: 12\">";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Circle ID</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">User Type</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">SMS Text</FONT></TH>";
		htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">No. Of Subscribers</FONT></TH>";
		htmlReportAct += "</TR>";
		for(int i = 0; i < fileNames.length; i++) {
			String circelId = fileNames[i].substring(0, fileNames[i].indexOf('_'));
			String userType = fileNames[i].substring(fileNames[i].indexOf('_') + 1);
			String smsText = (String)smsTextMap.get(fileNames[i]);
			String subCount = (String)subsCountMap.get(fileNames[i]);

			htmlReportAct += "<TR style=\"font-size: 12\">";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + circelId + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + userType + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + smsText + "</TD>";
			htmlReportAct += "<TD width=\"10%\" align=\"center\">" + subCount + "</TD>";
			htmlReportAct += "</TR>";
		}
		htmlReportAct += "</TABLE>";
		htmlReportAct += "</BODY></HTML>";
		Calendar calendar = Calendar.getInstance();
		String dateStr = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH) + 1)
				+ "-" + calendar.get(Calendar.YEAR);
		String htmlReportFileName = pickofDaySMSFilePath
				+ "/PICK_OF_THE_DAY/Pick_Of_Day_Recommendation_" + dateStr + ".html";
		try {
			FileWriter fileWriter = new FileWriter(htmlReportFileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(htmlReportAct);
			bufferedWriter.flush();
			bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		}
		catch (IOException e) {
			logger.error("", e);
		}
		File htmlReportFile = new File(htmlReportFileName);
		if(recommendationReportMode.equalsIgnoreCase("FTP")) {
			FTPClient ftp = null;
			for(int i = 0; i < ftpRetries; i++) {
				try {
					logger.info("RBT:: Creating FTP Connection");
					ftp = new FTPClient(ftpServer, ftpPort);
					ftp.setTimeout(ftpTimeout);
					ftp.login(ftpUser, ftpPassword);
					ftp.setConnectMode(FTPConnectMode.PASV);
					ftp.setType(FTPTransferType.BINARY);
					ftp.chdir(ftpDir);
					Calendar curcal = Calendar.getInstance();
					String dirDate = getMONTH(curcal.get(Calendar.MONTH))
							+ curcal.get(Calendar.DATE);
					try

					{
						ftp.mkdir(dirDate);
						ftp.chdir(dirDate);
					}
					catch (Exception exe) {
						ftp.chdir(dirDate);
					}

					if(zippedlistFile != null && zippedlistFile.exists()) {
						ftp.put(zippedlistFile.getAbsolutePath(), zippedlistFile.getName());
						logger.info("RBT:: "
								+ zippedlistFile.getName() + " uploaded successfully...!");
						zippedlistFile.delete();
					}
					if(htmlReportFile != null && htmlReportFile.exists()) {
						ftp.put(htmlReportFile.getAbsolutePath(), htmlReportFile.getName());
						logger.info("RBT:: "
								+ htmlReportFile.getName() + " uploaded successfully...!");
						htmlReportFile.delete();
					}

					break;
				}
				catch (Exception exe) {
					logger.error("", exe);
					try {
						Thread.sleep(ftpWaitPeriod);
					}
					catch (Exception ex) {
					}
					continue;
				}
				finally {
					if(ftp != null) {
						try {
							ftp.quit();
						}
						catch (FTPException e) {
							e.printStackTrace();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		else if(recommendationReportMode.equalsIgnoreCase("SPIDER")) {
			if(spiderPath != null) {
				File spiderDir = new File(spiderPath);
				File zippedlistSpiderFile = new File(spiderDir, zippedlistFile.getName());
				File htmlReportSpiderFile = new File(spiderDir, htmlReportFile.getName());
				Tools.moveFile(zippedlistFile, zippedlistSpiderFile);
				Tools.moveFile(htmlReportFile, htmlReportSpiderFile);
			}
			else {
				logger.info("RBT:: Spider path not defined");
			}
		}
	}
	
	private void addToAccounting(String event, String subID, String prepaidStatys,
			String requestType, String responseType, Date requestTime, String differenceTime,
			String urlstr, String result) {
		WriteSDR.addToAccounting(m_tataRBTDaemonMain.daemonQueriesLogPath,
				m_tataRBTDaemonMain.rotationSize, event, subID, prepaidStatys, requestType,
				responseType, TATARBTDaemonMain.formatter.format(requestTime), differenceTime,
				"RBT_DAEMON", urlstr, result);
	}
}