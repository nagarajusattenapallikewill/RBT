/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
public class TataUtility implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(TataUtility.class);

	private static RBTCacheManager rbtCacheManager = null;
	
	private static Map<String, OperatorDetail> operatorHash = null;

	static
	{
		rbtCacheManager = RBTCacheManager.getInstance();

		ParametersCacheManager parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
		String operatorAccount = parameter.getValue();
		
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
		String operatorPassword = parameter.getValue();
		
		parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR", "");
		String operator = parameter.getValue();
		
		operatorHash = Collections.synchronizedMap(new HashMap<String, OperatorDetail>());
		operatorHash.put("DEFAULT", new OperatorDetail(operatorAccount, operatorPassword, operator));
	}

	public static CosDetails getSubscriberCOS(WebServiceContext task, Subscriber subscriber)
	{
		String subscriberID = task.getString(param_subscriberID);
		String isPrepaid = task.getString(param_isPrepaid).toLowerCase();

		String circleID = DataUtils.getUserCircle(task);
		boolean defaultCOS = false;
		if (task.containsKey(param_mmContext))
		{
			String[] mmContext = task.getString(param_mmContext).split("\\|");
			for (String context : mmContext)
			{
				if (context.equalsIgnoreCase("USE_DEFAULT_COS"))
				{
					defaultCOS = true;
					break;
				}
			}
		}
		
		String mode = "VP";
		if (task.containsKey(param_mode))
			mode = task.getString(param_mode);

		CosDetails cos = null;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (task.containsKey(param_cosID) && rbtDBManager.isSubscriberDeactivated(subscriber))
			cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(task.getString(param_cosID), circleID);

		if (cos == null)
			cos = rbtDBManager.getSubscriberCos(subscriberID, circleID, isPrepaid, mode, defaultCOS);

		return cos;
	}

	public static boolean syncSubscriberStatus(WebServiceContext task, Subscriber subscriber) throws Exception
	{
		boolean subscriberStatusUpdated = false;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String subscriberID = task.getString(param_subscriberID);
		String isPrepaid = task.getString(param_isPrepaid);
		String userType = isPrepaid.equalsIgnoreCase(YES) ? "PRE_PAID" : "POST_PAID";

		String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
		int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		String subscriberStatus = "";
		String corpID = null;

		String sendSMSString = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SEND_SMS", "");
		boolean sendSMS = sendSMSString.startsWith("TRUE") ? true : false;

		boolean sendActivationSuccessSMS = false;
		String sendActivationSuccessSMSString = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SEND_ACTIVATION_SUCCESS_SMS", "");
		if (sendActivationSuccessSMSString.equalsIgnoreCase("TRUE"))
			sendActivationSuccessSMS = true;
		else
			sendActivationSuccessSMS = false;

		String activationSuccessSMSPostpaid = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_POSTPAID", "");
		String activationSuccessSMSPrepaid = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_PREPAID", "");
		String activationSuccessSMSPrepaidPromo = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_PREPAID_PROMO", "");
		String activationSuccessSMSPrepaidPromoWhenSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_PREPAID_PROMO_WHEN_SMS", "");
		String activationSuccessSMSPostpaidWhenSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_POSTPAID_WHEN_SMS", "");
		String activationSuccessSMSPrepaidWhenSMS = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "ACTIVATION_SUCCESS_SMS_PREPAID_WHEN_SMS", "");
		String prepaidServiceGiftSuccessSMSforGifter = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PREPAID_SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTER", "");
		String postpaidServiceGiftSuccessSMSforGifter = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "POSTPAID_SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTER", "");
		String serviceGiftSuccessSMSforGiftee = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SERVICE_GIFT_SUCCESS_SMS_FOR_GIFTEE", "");
		String smsNo = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SMS_NUMBER", "");
		String db_url = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "DB_URL", "");
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		// Changes done for URL Encryption and Decryption
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
		
		String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SUBSCRIBER_STATUS_PAGE", "");
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
		urlstr += "phonenumber=" + subscriberID + "&";
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

		RBTHTTPProcessing rbtHTTPProcessing = RBTHTTPProcessing.getInstance();

		Date requestedTimeStamp = new Date();
		String result = rbtHTTPProcessing.makeRequest1(urlstr, subscriberID, "RBT_VUI");
		Date responseTimeStamp = new Date();

		long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeString = formatter.format(requestedTimeStamp);

		logger.info("RBT::result = " + result);

		if (result != null)
		{
			String subStatus = null;

			result = result.trim();
			if (result.length() <= 2)
			{
				if (result.equals("9"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_is_suspended", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					subStatus = SUSPENDED;
					logger.info("RBT:: user" + subscriberID + " in suspended state");
				}
				else if (result.equals("5"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_is_blacklisted", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					subStatus = BLACK_LISTED;
					logger.info("RBT::" + subscriberID + " is a blacklisted user");
				}
				else if (result.equals("6"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "gifting_under_processing", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					logger.info("RBT::gifting under processing");
					subStatus = GIFTING_PENDING;
				}
				else if (result.equals("7"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_is_new_user", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					if (subscriber != null && !rbtDBManager.isSubscriberActivationPending(subscriber))
					{
						rbtDBManager.deactivateSubscriberForTATA(subscriberID);
						subscriberStatusUpdated = true;
					}
					/* In new user case from back end no need to set the status key as if the
					 request is still with us, as the status from huawei takes priority an will
					 override the actual pending status. even if the user is new it will be
					 handled @ subscriber == null check */
					//subStatus = NEW_USER;
				}
				else if (result.equals("10"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_express_copy_pending", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					logger.info("RBT::express copy in pending");
					if (subscriber != null && !rbtDBManager.isSubscriberActivationPending(subscriber))
					{
						rbtDBManager.deactivateSubscriberForTATA(subscriberID);
						subscriberStatusUpdated = true;
					}
					subStatus = COPY_PENDING;
				}
			}
			else if (result.length() > 2 && result.length() < 20)
			{
				StringTokenizer st = new StringTokenizer(result, "|");
				if (st.hasMoreTokens())
					subscriberStatus = st.nextToken();
				String prepaidStatus;
				if (st.hasMoreTokens())
				{
					prepaidStatus = st.nextToken();
					if (prepaidStatus.equals("1"))
					{
						userType = "PRE_PAID";
						if (subscriber != null && !subscriber.prepaidYes())
						{
							rbtDBManager.changeSubscriberType(subscriberID, true);
							subscriber = rbtDBManager.getSubscriber(subscriberID);
						}
						isPrepaid = YES;
					}
					else
					{
						userType = "POST_PAID";
						if (subscriber != null && subscriber.prepaidYes())
						{
							rbtDBManager.changeSubscriberType(subscriberID, false);
							subscriber = rbtDBManager.getSubscriber(subscriberID);
						}
						isPrepaid = NO;
					}
				}

				if (st.hasMoreTokens())
				{
					corpID = st.nextToken();
					task.put(param_corpID, corpID);
				}
				if (subscriberStatus.equals("1") || subscriberStatus.equals("6"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_activation_pending", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					logger.info("RBT::before open state going to next transition");
					subStatus = ACT_PENDING;

					String calledNo = task.getString(param_calledNo);
					boolean isPrepaidBol = isPrepaid.equalsIgnoreCase(YES) ? true : false;
					if (subscriber == null)
					{
						String circleID = DataUtils.getUserCircle(task);
						logger.info("RBT::making pending entry in our database");
						CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(circleID, isPrepaid);
						int activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);
						subscriber = rbtDBManager.activateSubscriber(subscriberID, "VP", null, isPrepaidBol,
								activationPeriod, 0, calledNo, cos.getSubscriptionClass(), true, cos, 0, circleID);

						rbtDBManager.smURLSubscription(subscriberID, true, false, null);

						subscriberStatusUpdated = true;
					}
				}
				else if (subscriberStatus.equals("5") || subscriberStatus.equals("7"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_deactivation_pending", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					logger.info("RBT::not allowed");
					subStatus = DEACT_PENDING;
				}
				else if (subscriberStatus.equals("2"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_is_active", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					if (!rbtDBManager.checkCanAddSetting(subscriber))
					{
						subscriberStatusUpdated = true;
						if (subscriber == null)
						{
							logger.info("RBT::subscriber is registered, but no detail in our database");

							String circleID = DataUtils.getUserCircle(task);
							String calledNo = task.getString(param_calledNo);
							boolean isPrepaidBol = isPrepaid.equalsIgnoreCase(YES) ? true : false;
							CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(DataUtils.getUserCircle(task), isPrepaid);
							int activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);
							subscriber = rbtDBManager.activateSubscriber(subscriberID, "VP", null, isPrepaidBol, activationPeriod, 0, calledNo, cos.getSubscriptionClass(), true, cos, 0, circleID);

							Calendar nextChargingDate = Calendar.getInstance();
							nextChargingDate.set(2035, 11, 31, 0, 0, 0);
							String type = isPrepaid.equalsIgnoreCase(YES) ? "P" : "B";
							rbtDBManager.smSubscriptionSuccess(subscriberID, nextChargingDate.getTime(), new Date(), type, subscriber.subscriptionClass(), true, cos, 0);
						}
						else
						{
							logger.info("RBT::subscriber is registered subscriber, updating database");
							CosDetails cos = rbtDBManager.getSubscriberCos(subscriberID, DataUtils.getUserCircle(task), isPrepaid, "VUI", false);

							Calendar nextChargingDate = Calendar.getInstance();
							nextChargingDate.set(2035, 11, 31, 0, 0, 0);

							String type = "B";
							if (subscriber.prepaidYes())
								type = "P";

							rbtDBManager.smSubscriptionSuccess(subscriberID, nextChargingDate.getTime(), new Date(), type, subscriber.subscriptionClass(), true, cos, 0);

							String activationSuccessSMS = "";
							if (subscriber.prepaidYes())
							{
								if (subscriber.activatedBy().indexOf("PROMO") >= 0)
								{
									activationSuccessSMS = activationSuccessSMSPrepaidPromo;
									if (subscriber.activatedBy().indexOf("SMS") >= 0)
										activationSuccessSMS = activationSuccessSMSPrepaidPromoWhenSMS;
								}
								else if (subscriber.activatedBy().equalsIgnoreCase("SMS"))
									activationSuccessSMS = activationSuccessSMSPrepaidWhenSMS;
								else
									activationSuccessSMS = activationSuccessSMSPrepaid;
							}
							else
							{
								if (subscriber.activatedBy().equalsIgnoreCase("SMS"))
									activationSuccessSMS = activationSuccessSMSPostpaidWhenSMS;
								else
									activationSuccessSMS = activationSuccessSMSPostpaid;
							}
							if (sendSMS && subscriber.activatedBy().equalsIgnoreCase("GIFT"))
							{
								String gifterID = subscriber.activationInfo();
								String gifteeID = subscriber.subID();
								String serviceGiftSuccessSMSforGifter = null;
								if (subscriber.prepaidYes())
									serviceGiftSuccessSMSforGifter = prepaidServiceGiftSuccessSMSforGifter;
								else
									serviceGiftSuccessSMSforGifter = postpaidServiceGiftSuccessSMSforGifter;
								if (serviceGiftSuccessSMSforGifter != null)
								{
									activationSuccessSMS = serviceGiftSuccessSMSforGifter.replaceAll("%NUMBER%", gifteeID);
									Tools.sendSMS(db_url, smsNo, gifterID, activationSuccessSMS, true);
								}
								if (serviceGiftSuccessSMSforGiftee != null)
								{
									activationSuccessSMS = serviceGiftSuccessSMSforGiftee.replaceAll("%NUMBER%", gifterID);
									Tools.sendSMS(db_url, smsNo, gifteeID, activationSuccessSMS, true);
								}
							}
							else if (sendSMS && sendActivationSuccessSMS)
								Tools.sendSMS(db_url, smsNo, subscriberID, activationSuccessSMS, true);
						}
					}
				}
				else if (subscriberStatus.equals("4"))
				{
					if (!rbtDBManager.isSubscriberActivationPending(subscriber))
					{
						// If user status is activation pending in OM DB, then the Huawei DB status will be ignored
						subStatus = DEACTIVE;
					}

					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "user_is_deactived", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
					logger.info("RBT::subscriber is unregistered subscriber, checking for promo");
					if ((subscriber != null) && (rbtDBManager.checkCanAddSetting(subscriber) || rbtDBManager.isSubscriberDeactivationPending(subscriber)))
					{
						CosDetails cos = rbtDBManager.getSubscriberCos(subscriberID, DataUtils.getUserCircle(task), isPrepaid, "VUI", false);
						rbtDBManager.deactivateSubscriberForTATA(subscriberID);
						if (!cos.isDefaultCos())
							rbtDBManager.addSubscriberToDeactivatedSubscribersTable(subscriberID, "VP", subscriber.activatedCosID());

						subscriberStatusUpdated = true;
					}
				}
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "error_response", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
				if (subscriber != null)
				{
					logger.info("RBT::unexpected response, but we have the details continuing");
					if (subscriber.prepaidYes())
					{
						userType = "PRE_PAID";
						isPrepaid = YES;
					}
					else
					{
						userType = "POST_PAID";
						isPrepaid = NO;
					}
				}
			}

			if (subStatus != null)
				task.put(param_subscriberStatus, subStatus);
		}
		else
		{
			if (subscriber == null)
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, "unknown", "subscriber_status", "null_error_response", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
			}
			else
			{
				logger.info("RBT::unexpected response, but we have the details continuing");
				if (subscriber.prepaidYes())
				{
					userType = "PRE_PAID";
					isPrepaid = YES;
				}
				else
				{
					userType =  "POST_PAID";
					isPrepaid = NO;
				}
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS", subscriberID, userType, "subscriber_status", "null_error_response", requestedTimeString, differenceTime + "", "RBT_VUI", urlstr, result);
			}
		}

		return subscriberStatusUpdated;
	}

	public static void syncSbscriberLibrary(WebServiceContext task,
			Subscriber subscriber, SubscriberDownloads[] downloads,
			SubscriberStatus[] settings)
	throws Exception
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		String subscriberID = task.getString(param_subscriberID);
		String calledNo = task.getString(param_calledNo);
		String browsingLanguage = task.getString(param_browsingLanguage);

		String userType = subscriber.prepaidYes() ? "PRE_PAID" : "POST_PAID";

		List<String> clipDownloadsFromBackEnd = getSubscriberClipDownloadsFromBackEnd(task, userType);
		List<String> musicBoxDownloadsFromBackEnd = getSubscriberMusicBoxDownloadsFromBackEnd(task, userType);

		String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
		int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		String clipsResult = task.getString(param_clipDownloadsFromBackEnd);

		List<String> settingsInBackEnd = new ArrayList<String>();

		String httpLink = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		String operatorAccount = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
		String operatorPassword = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
		String operator = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

		for (int setTypeCounter = 1; setTypeCounter < 3; setTypeCounter++)
		{
			String urlstrToGetSetting = httpLink;
			urlstrToGetSetting += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SETTING_PAGE", "");

			urlstrToGetSetting += operatorAccount + "&";
			urlstrToGetSetting += operatorPassword + "&";
			urlstrToGetSetting += "phonenumber=" + subscriberID+ "&";
			urlstrToGetSetting += "settype=" + setTypeCounter + "&";
			urlstrToGetSetting += operator;

			Date requestedTimeStamp = new Date();
			String settingResult = getClipOrMusicBoxSetting(task, urlstrToGetSetting);
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

			if (settingResult == null)
			{
				logger.info("RBT::got null setting result, leaving for loop");
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", subscriberID, userType, "query_setting", "error_got-null", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstrToGetSetting, settingResult);
				break;
			}

			boolean breakFlag = false;
			if (settingResult.length() <= 1)
			{
				if (settingResult.equals("4"))
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", subscriberID, userType, "query_setting", "no-setting", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstrToGetSetting, settingResult);
				else
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", subscriberID, userType, "query_setting", "error_got-null", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstrToGetSetting, settingResult);
			}
			else
			{

				StringTokenizer settingResultST = new StringTokenizer(settingResult, "&");
				String token = null;
				while (settingResultST.hasMoreTokens())
				{
					token = settingResultST.nextToken();
					StringTokenizer tokenST = new StringTokenizer(token, "|");
					if (tokenST.countTokens() < 9)
					{
						logger.info("RBT::unexpected result leaving first while");
						breakFlag = true;
						break;
					}

					int tokenKaTokenCounter = 0;
					String tokenKaToken = null;

					boolean clipYes = false;
					tokenKaToken = tokenST.nextToken();
					tokenKaTokenCounter++;
					if (tokenKaToken.equals("1"))
						clipYes = true;

					String toneCode = null;
					String callerID = null;
					String startDate = null;
					int loopNumber = 1;

					while (tokenST.hasMoreTokens())
					{
						tokenKaToken = tokenST.nextToken();
						tokenKaTokenCounter++;
						if (tokenKaTokenCounter == 3)
						{
							callerID = tokenKaToken;
							logger.info("RBT::callerID = " + callerID);
							if (callerID != null)
							{
								if (callerID.length() > 7 && callerID.length() < 15)
									continue;

								callerID = null;
								tokenKaTokenCounter++;
							}
						}
						else if (tokenKaTokenCounter == 7)
						{
							toneCode = tokenKaToken;
							logger.info("RBT::toneCode = " + toneCode);
						}
						else if (tokenKaTokenCounter == 8 && clipYes)
						{
							try
							{
								loopNumber = Integer.parseInt(tokenKaToken);
							}
							catch(Exception e)
							{

							}
							if (loopNumber > 1)
								loopNumber = 2;
						}
					}
					Calendar cal = Calendar.getInstance();
					if (clipYes)
					{
						startDate = getClipStartDateForLibrarySync(toneCode, clipsResult);
						logger.info("RBT:: startDate = " + startDate);
						if (startDate != null)
						{
							String yearString = startDate.substring(0,4);
							String monthString = startDate.substring(5,7);
							String dayString = startDate.substring(8,10);
							String hourString = startDate.substring(11,13);
							String minuteString = startDate.substring(14,16);
							String secondString = startDate.substring(17,19);

							int year = Integer.parseInt(yearString);
							int month = Integer.parseInt(monthString);
							int day = Integer.parseInt(dayString);
							int hour = Integer.parseInt(hourString);
							int minute = Integer.parseInt(minuteString);
							int second = Integer.parseInt(secondString);

							cal.set(year, month-1, day, hour, minute, second);
						}
					}
					Date startTime = cal.getTime();
					logger.info("RBT:: startTime = " + startTime);
					int categoryID = -1;

					Calendar nextChgDtCal = Calendar.getInstance();
					nextChgDtCal.set(2035, 11, 31, 0, 0, 0);

					String classType = null;
					if (clipYes)
					{
						Clip clip = rbtCacheManager.getClipByPromoId(toneCode, browsingLanguage);
						if (clip == null)
							continue;
						try
						{
							classType = clip.getClassType();
							categoryID = rbtDBManager.getClipCategoryId(clip.getClipId());
							Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
							if (category == null)
								categoryID = 3;
						}
						catch(Exception e)
						{

						}
					}
					else
					{
						Category musicbox = rbtCacheManager.getCategoryByPromoId(toneCode, browsingLanguage);
						if (musicbox == null)
							continue;
						classType = musicbox.getClassType();
						categoryID = 4;
					}
					if (classType == null)
						classType = "DEFAULT";

					//if we don't have the (clip in rbt_category_clip_map)/(music box in rbt_musicboxes)
					if (categoryID <= 0)
					{
						if (clipYes)
							categoryID = 3;
						else
							categoryID = 4;
					}

					int validityInt = 365;
					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					if (chargeClass != null)
					{
						String validity = chargeClass.getSelectionPeriod();
						validityInt = (validity != null) ? Integer.parseInt(validity.substring(1)) : 365;
						if (validity != null)
						{
							if (validity.startsWith("M"))
								validityInt = validityInt * 30;
							else if (validity.startsWith("Y"))
								validityInt = validityInt * 365;
						}
					}

					int maxSelections = subscriber.maxSelections();
					SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(subscriberID, toneCode);

					if (download == null)
					{
						rbtDBManager.addSubscriberDownload(subscriberID, toneCode, categoryID, true, (clipYes ? iRBTConstant.DTMF_CATEGORY : iRBTConstant.BOUQUET));
						rbtDBManager.updateDownloadStatusToDownloaded(subscriberID, toneCode, startTime, validityInt);
					}

					settingsInBackEnd.add(callerID + "," + toneCode);
					SubscriberStatus sebSel = rbtDBManager.getSubWavFileForCaller(subscriberID, callerID, toneCode);

					//adding active record selections table
					if (sebSel == null
							|| (!sebSel.selStatus().equalsIgnoreCase(iRBTConstant.STATE_TO_BE_DEACTIVATED)
									&& !sebSel.selStatus().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATED)))
					{
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(startTime);
						calendar.add(Calendar.DAY_OF_YEAR, (validityInt -1));
						Date endTime = calendar.getTime();
						rbtDBManager.addActiveSubSelections(subscriberID, callerID, categoryID, toneCode, startTime, startTime,
								endTime, loopNumber, "VP-Sync", calledNo + "|" + maxSelections, subscriber.prepaidYes(), 0, 2359,
								nextChgDtCal.getTime(), classType, true, clipYes ? iRBTConstant.DTMF_CATEGORY : iRBTConstant.BOUQUET);
					}
				}
				if (breakFlag)
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", subscriberID, userType, "query_setting", "error_wrong-code", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstrToGetSetting, settingResult);
				}
				else
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", subscriberID, userType, "query_setting", "success", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstrToGetSetting, settingResult);
				}
			}
		}

		List<String> allSelListFromBackEnd = new ArrayList<String>();
		for (String promoID : clipDownloadsFromBackEnd)
		{
			Clip clip = rbtCacheManager.getClipByPromoId(promoID, browsingLanguage);
			if (clip != null) 
				allSelListFromBackEnd.add(promoID);
		}
		int noOfClipsInBackEnd = allSelListFromBackEnd.size();

		for (String promoID : musicBoxDownloadsFromBackEnd)
		{
			Category musicbox = rbtCacheManager.getCategoryByPromoId(promoID, browsingLanguage);
			if (musicbox != null)
				allSelListFromBackEnd.add(promoID);
		}
		String[] allSubSelectionsFromBackEnd = allSelListFromBackEnd.toArray(new String[0]);

		//Adding the songs to our database which are not in our database but in Back End
		for (int i = 0; i < allSubSelectionsFromBackEnd.length; i++)
		{
			SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(subscriberID, allSubSelectionsFromBackEnd[i]);
			if (download == null)
			{
				int categoryID = 3;
				if (i >= noOfClipsInBackEnd)
					categoryID = 4;

				String classType = null;
				int validityInt = 365;
				try 
				{ 
					if (categoryID == 3) 
					{ 
						Clip clip = rbtCacheManager.getClipByPromoId(allSubSelectionsFromBackEnd[i], browsingLanguage);
						classType = clip.getClassType();
					} 
					else 
					{ 
						Category musicbox = rbtCacheManager.getCategoryByPromoId(allSubSelectionsFromBackEnd[i], browsingLanguage);
						classType = musicbox.getClassType();
					} 

					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
					if (chargeClass != null) 
					{ 
						String validity = chargeClass.getSelectionPeriod();
						validityInt = (validity != null) ? Integer.parseInt(validity.substring(1)) : 365;
						if (validity != null)
						{
							if (validity.startsWith("M"))
								validityInt = validityInt * 30;
							else if (validity.startsWith("Y"))
								validityInt = validityInt * 365;
						}
					} 

				} 
				catch (Exception e) 
				{
					logger.error("", e);
				} 

				rbtDBManager.addSubscriberDownload(subscriberID, allSubSelectionsFromBackEnd[i], categoryID,
						true, (categoryID == 3 ? iRBTConstant.DTMF_CATEGORY : iRBTConstant.BOUQUET));
				rbtDBManager.updateDownloadStatusToDownloaded(subscriberID, allSubSelectionsFromBackEnd[i], new Date(), validityInt);
			}
		}

		//deleting the songs in our database which are not in back end database
		if (downloads != null)
		{
			for (SubscriberDownloads subscriberDownload : downloads)
			{
				String wavFile = subscriberDownload.promoId();
				char downloadStatus = subscriberDownload.downloadStatus();
				if (downloadStatus == 'y')
				{
					logger.info("RBT:: downloadFile = " + wavFile + " exists = " + !(!clipDownloadsFromBackEnd.contains(wavFile) && !musicBoxDownloadsFromBackEnd.contains(wavFile)));
					if (!clipDownloadsFromBackEnd.contains(wavFile) && !musicBoxDownloadsFromBackEnd.contains(wavFile))
					{
						rbtDBManager.deactivateSubWavFile(subscriberID, wavFile, iRBTConstant.STATE_DEACTIVATED, "VP-Sync", null);
						rbtDBManager.deactivateSubscriberDownload(subscriberID, wavFile, "VP-Sync");
					}
				}
			}
		}

		//deleting the settings in our database which are not in back end database
		if (settings != null)
		{
			for (SubscriberStatus setting : settings)
			{
				String key = setting.callerID() + "," + setting.subscriberFile();
				logger.info("RBT:: Setting = " + key + " exists = " + settingsInBackEnd.contains(key));
				if (setting.selStatus().equals(iRBTConstant.STATE_ACTIVATED) && !settingsInBackEnd.contains(key))
					rbtDBManager.deactivateSubWavFileForCaller(subscriberID, setting.callerID(), setting.subscriberFile(),
							iRBTConstant.STATE_DEACTIVATED, "VP-Sync", null, false);
			}
		}
	}

	public static List<String> getSubscriberClipDownloadsFromBackEnd(WebServiceContext task,
			String userType) throws Exception
			{
		String subscriberID = task.getString(param_subscriberID);
		ArrayList<String> clipsList = new ArrayList<String>();

		String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
		int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SONGS_PAGE", "");

		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
		urlstr += "phonenumber=" +subscriberID + "&";
		urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

		RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

		Date requestedTimeStamp = new Date();
		String result = rbthttpProcessing.makeRequest1(urlstr, subscriberID, "RBT_VUI");
		Date responseTimeStamp = new Date();

		long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeString = formatter.format(requestedTimeStamp);

		if (result != null)
		{
			result = result.trim();

			logger.info("RBT:: result = " + result);

			if (result.length() > 1)
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", subscriberID, userType, "query_songs", "success", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
				task.put(param_clipDownloadsFromBackEnd, result);
				StringTokenizer st = new StringTokenizer(result, "&");
				while (st.hasMoreTokens())
				{					
					String newString = st.nextToken();
					StringTokenizer tempStringTokenizer = new StringTokenizer(newString, "|");
					if (tempStringTokenizer.hasMoreTokens())
					{
						String tempString = tempStringTokenizer.nextToken().trim();
						clipsList.add(tempString);
					}					
				}
			}
			else if (result.equals("4"))
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", subscriberID, userType, "query_songs", "no-clips", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", subscriberID, userType, "query_songs", "error_response", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			}
		}
		else
		{
			WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", subscriberID, userType, "query_songs", "null_error_response", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			logger.info("RBT::null response");
			throw new Exception("Null response");
		}

		return clipsList;
			}

	public static List<String> getSubscriberMusicBoxDownloadsFromBackEnd(WebServiceContext task, String userType) throws Exception
	{
		String subscriberID = task.getString(param_subscriberID);
		ArrayList<String> mbList = new ArrayList<String>();

		String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
		int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

		String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_MUSICBOXES_PAGE", "");

		urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
		urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
		urlstr+= "phonenumber=" +subscriberID+ "&";
		urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

		RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

		Date requestedTimeStamp = new Date();
		String result = rbthttpProcessing.makeRequest1(urlstr, subscriberID, "RBT_VUI");
		Date responseTimeStamp = new Date();

		long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestedTimeString = formatter.format(requestedTimeStamp);

		if (result != null)
		{
			result = result.trim();

			logger.info("RBT:: result = " + result);
			if (result.length() > 1)
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", subscriberID, userType, "query_musicboxes", "success", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
				task.put(param_musicBoxDownloadsFromBackEnd, result);
				StringTokenizer st = new StringTokenizer(result, "&");
				while (st.hasMoreTokens())
				{
					StringTokenizer tempStringTokenizer = new StringTokenizer(st.nextToken(), "|");
					mbList.add(tempStringTokenizer.nextToken());
				}
			}
			else if (result.equals("4"))
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", subscriberID, userType, "query_musicboxes", "no-musicboxes", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", subscriberID, userType, "query_musicboxes", "error_response", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			}
		}
		else
		{
			WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", subscriberID, userType, "query_musicboxes", "null_error_response", requestedTimeString, differenceTime+ "", "RBT_VUI", urlstr, result);
			logger.info("RBT::null response");
			throw new Exception("Null response");
		}

		return mbList;
	}

	public static String getClipOrMusicBoxSetting(WebServiceContext task, String urlstrToGetSetting)
	{
		String subscriberID = task.getString(param_subscriberID);
		String returnValue = null;
		try
		{
			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();			
			returnValue = rbthttpProcessing.makeRequest1(urlstrToGetSetting, subscriberID, "RBT_VUI");

			if (returnValue != null)
				returnValue = returnValue.trim();
		}
		catch(Exception e)
		{
			logger.error("", e);
		}

		return returnValue;
	}

	public static String getClipStartDateForLibrarySync(String toneCode, String result)
	{
		try
		{
			if (result == null)
			{
				logger.info("RBT::got null result");
				return null;
			}

			result = result.trim();
			StringTokenizer clipTokenizer = new StringTokenizer(result, "&");
			while (clipTokenizer.hasMoreTokens())
			{
				String token = clipTokenizer.nextToken();
				if (token.startsWith(toneCode))
				{
					StringTokenizer tokenKaTokenizer = new StringTokenizer(token, "|");
					for (int counter = 1; counter < 4; counter++)
					{
						String tempResult = tokenKaTokenizer.nextToken();
						if (counter == 3)
							return tempResult;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return null;
	}

	public static SubscriberDownloads[] getNonDefaultSongs(String subscriberID,
			SubscriberStatus[] settings, SubscriberDownloads[] subscriberDownloads) 
	{ 
		if (subscriberDownloads == null || subscriberDownloads.length == 0) 
			return null;

		ArrayList<SubscriberDownloads> toneList = new ArrayList<SubscriberDownloads>();
		for (SubscriberDownloads subscriberDownload : subscriberDownloads)
		{
			boolean clipYes = false;
			if (subscriberDownload.categoryType() == iRBTConstant.DTMF_CATEGORY)
				clipYes = true;

			if (clipYes && subscriberDownload.downloadStatus() != 'y') 
				continue;

			SubscriberStatus[] subscriberStatuses = getSubscriberStatus(settings, subscriberDownload.promoId());
			boolean isDefault = false;
			if (subscriberStatuses != null)
			{
				for (SubscriberStatus subscriberStatus : subscriberStatuses)
				{
					String callerID = subscriberStatus.callerID();
					if (callerID == null && subscriberStatus.endTime().getTime() >= System.currentTimeMillis()) 
					{ 
						if (!clipYes) 
							return null;
						isDefault = true;
						break;
					} 
				} 
			}

			if (!isDefault && clipYes) 
				toneList.add(subscriberDownload);
		} 	

		if (toneList.size() > 0) 
			return toneList.toArray(new SubscriberDownloads[0]);

		return null;
	}

	public static SubscriberStatus[] getSubscriberStatus(SubscriberStatus[] settings, String promoID)
	{
		if (settings == null || settings.length == 0)
			return null;

		ArrayList<SubscriberStatus> list = new ArrayList<SubscriberStatus>();
		for (SubscriberStatus setting : settings)
		{
			if (setting.subscriberFile().equalsIgnoreCase(promoID))
				list.add(setting);
		}

		if (list.size() > 0)
			return (list.toArray(new SubscriberStatus[0]));

		return null;
	}
	
	public static String upgradeSubscription(WebServiceContext webServiceContext, Subscriber subscriber, CosDetails cos)
	{
		if (cos == null)
			return COS_NOT_EXISTS;

		String response = ERROR;
		try
		{
			ParametersCacheManager parameterCacheManager = CacheManagerUtil
					.getParametersCacheManager();
			Parameters parameter = parameterCacheManager.getParameter(
					iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			String httpLink = parameter.getValue();

			StringBuilder urlBuilder = new StringBuilder(httpLink);
			parameter = parameterCacheManager.getParameter(
					iRBTConstant.TATADAEMON,
					"ASSIGN_COS_PAGE", "");
			String assigncosPage = parameter.getValue();
			urlBuilder.append(assigncosPage);

			urlBuilder.append(TataUtility.getOperatorAccount(cos)).append("&");
			urlBuilder.append(TataUtility.getOperatorPassword(cos)).append("&");
			urlBuilder.append(TataUtility.getOperatorCode(cos)).append("&");

			String subscriberID = webServiceContext
					.getString(param_subscriberID);
			urlBuilder.append("phonenumber=").append(subscriberID).append("&");
			urlBuilder.append("cosid=").append(cos.getCosId());

			HttpParameters httpParameters = new HttpParameters(
					urlBuilder.toString());

			parameter = parameterCacheManager.getParameter(
					iRBTConstant.TATADAEMON,
					"HTTP_CONNECTION_TIME_OUT", "6000");
			httpParameters.setConnectionTimeout(Integer.parseInt(parameter
					.getValue()));

			parameter = parameterCacheManager.getParameter(
					iRBTConstant.TATADAEMON,
					"HTTP_SOCKET_TIME_OUT", "6000");
			httpParameters.setSoTimeout(Integer.parseInt(parameter.getValue()));

			if (logger.isDebugEnabled())
				logger.debug("httpParameters: " + httpParameters);

			Date requestedTime = new Date();
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			if (logger.isDebugEnabled())
				logger.debug("httpResponse: " + httpResponse);

			long responseTime = 0;
			String responseString = "";
			if (httpResponse != null && httpResponse.getResponse() != null)
			{
				responseTime = httpResponse.getResponseTime();
				responseString = httpResponse.getResponse().trim();
				int responseValue = Integer.parseInt(responseString);
				switch (responseValue)
				{
					case 0:
						response = SUCCESS;
						break;
					case 1:
					case 2:
						response = TECHNICAL_DIFFICULTIES;
						break;
					case 3:
						response = INVALID_PARAMETER;
						break;
					case 4:
						response = COS_NOT_EXISTS;
						break;
					case 8:
					case 19:
						response = FAILED;
						break;
					case 14:
						response = NOT_ALLOWED;
						break;
					case 17:
						response = ALREADY_ACTIVE;
						break;
					case 20:
						response = ALREADY_USED;
						break;
					default:
						response = ERROR;
				}
			}

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTime);
			String userType = subscriber.prepaidYes() ? "PRE_PAID"
					: "POST_PAID";

			String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

			WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "ASSIGN_COS",
					subscriberID, userType, "ASSIGN_COS", response,
					requestedTimeString, String.valueOf(responseTime),
					"RBT_VUI", urlBuilder.toString(), responseString);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = TECHNICAL_DIFFICULTIES;
		}
		
		return response;
	}
	
	public static String getOperatorAccount(CosDetails cos)
	{
		OperatorDetail operatorDetail = getOperatorDetail(cos);
		return operatorDetail.getOperatorAccount();
	}

	public static String getOperatorPassword(CosDetails cos)
	{
		OperatorDetail operatorDetail = getOperatorDetail(cos);
		return operatorDetail.getOperatorPassword();
	}

	public static String getOperatorCode(CosDetails cos)
	{
		OperatorDetail operatorDetail = getOperatorDetail(cos);
		return operatorDetail.getOperator();
	}

	private static OperatorDetail getOperatorDetail(CosDetails cos)
	{
		OperatorDetail operatorDetail = null;
		if (cos != null && cos.getOperator() != null)
		{
			if (!operatorHash.containsKey(cos.getOperator()))
				populateOperatorDetail(cos.getOperator());
			if (operatorHash.containsKey(cos.getOperator()))
				operatorDetail = operatorHash.get(cos.getOperator());
		}
		if (operatorDetail == null)
			operatorDetail = operatorHash.get("DEFAULT");
		return operatorDetail;
	}

	private static void populateOperatorDetail(String operatorCode)
	{
		String operator = null;
		String operatorAccount = null;
		String operatorPassword = null;
		
		ParametersCacheManager parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_" + operatorCode, null);
		if (parameter != null)
			operator = parameter.getValue();
		if (operator != null)
		{
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT_" + operatorCode, null);
			if (parameter != null)
				operatorAccount = parameter.getValue();
			parameter = parameterCacheManager.getParameter(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD_" + operatorCode, null);
			if (parameter != null)
				operatorPassword = parameter.getValue();
			
			operatorHash.put(operatorCode, new OperatorDetail(operator, operatorAccount, operatorPassword));
		}
	}

	private static class OperatorDetail
	{
		private String operator;
		private String operatorAccount;
		private String operatorPassword;

		/**
		 * @param operator
		 * @param operatorAccount
		 * @param operatorPassword
		 */
		public OperatorDetail(String operator, String operatorAccount,
				String operatorPassword)
		{
			this.operator = operator;
			this.operatorAccount = operatorAccount;
			this.operatorPassword = operatorPassword;
		}

		/**
		 * @return the operator
		 */
		public String getOperator()
		{
			return operator;
		}

		/**
		 * @return the operatorAccount
		 */
		public String getOperatorAccount()
		{
			return operatorAccount;
		}

		/**
		 * @return the operatorPassword
		 */
		public String getOperatorPassword()
		{
			return operatorPassword;
		}
	}
}
