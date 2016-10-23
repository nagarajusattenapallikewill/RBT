package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.gemalto.services.pushservice.PushServiceImplServiceStub;
import com.gemalto.services.pushservice.PushServiceImplServiceStub.ExecuteService;
import com.gemalto.services.pushservice.PushServiceImplServiceStub.ExecuteServiceE;
import com.gemalto.services.pushservice.PushServiceImplServiceStub.ExecuteServiceResponseE;
import com.gemalto.services.pushservice.PushServiceImplServiceStub.ExecuteServiceType;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.actions.WriteCDRLog;
import com.onmobile.apps.ringbacktones.webservice.bean.SatPushLoggerBean;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class RBTSATPushDaemon implements Runnable, WebServiceConstants {
	private RBTDBManager rbtDBManager = null;
	private String offerText = null;
	private DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
	private static Logger logger = Logger.getLogger(RBTSATPushDaemon.class);
	private static Logger cdr_logger = Logger.getLogger("CDR_LOGGER");
	private String notificationUrl = null;
	private String password = null;
	private String satAcceptKeyword = null;
	private String satPort = null;
	private static String failureResponse = "failure";
	private String satSuccessKeyword = null;
	private int requestPriority = 1;
	private String username = "sample";
	private int maxRetry = 4;
	private int successTime = 60;
	private int failureTime = 10;
	private Map<String, String> channelCode = null;
	private String defaultLanguage = "eng";
//	private static int statusForConsentPending = 0;
	private static int statusAfterSuccessfulHit = 1;
	private static int statusAfterConsentFailure = 3;
	private static int comboselstatusAfterSuccessfulHit = 1;
	private boolean isComboReq = false;
	private String selTransId = null;
	private String targetEndpoint= null;
	private String peruGemaltoCountryCode= "51";
	public RBTSATPushDaemon(
			DoubleConfirmationRequestBean doubleConfirmationRequestBean) {
		if (doubleConfirmationRequestBean == null) {
			throw new IllegalArgumentException(
					"doubleConfirmationRequestBean cannot be null.");
		}
		this.doubleConfirmationRequestBean = doubleConfirmationRequestBean;
		init();
	}

	public void init() {
		rbtDBManager = RBTDBManager.getInstance();
		targetEndpoint = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_TARGET_URL", null);;
		notificationUrl = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_NOTIFICATION_URL", null);
		satSuccessKeyword = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_SUCCESS_RESPONSE_KEYWORD", "OK");
		satAcceptKeyword = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_ACCEPT_KEYWORD", "ACEPTO");
		satPort = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_PORT", "8889");
		password = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_PASSWORD", null);
		requestPriority = RBTParametersUtils.getParamAsInt("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_PRIORITY", 1);
		username = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_USERNAME", "sample");
		maxRetry = RBTParametersUtils.getParamAsInt("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_MAX_RETRY", 4);
		successTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_RETRY_TIME_AFTER_SUCCESS", 60);
		failureTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"DOUBLE_OPT_IN_SAT_REQUEST_RETRY_TIME_AFTER_FAILURE", 10);
		peruGemaltoCountryCode = RBTParametersUtils.getParamAsString("DAEMON","PERU_GEMALTO_COUNTRY_CODE", "51");
		String paramValue = RBTParametersUtils.getParamAsString("DAEMON",
				"DOUBLE_OPT_IN_CHANNEL_CODE_MAP", null);
		if (paramValue != null) {
			channelCode = MapUtils.convertToMap(paramValue.toUpperCase(), ";",
					":", null);
			logger.info("channelCode: " + channelCode);
		}

	}

	@Override
	public void run() {
		logger.info("Inside run RBTSATDaemon: doubleConfirmationRequestBean : "
				+ doubleConfirmationRequestBean);
		String requestType = doubleConfirmationRequestBean.getRequestType();
		// Date requestTime = doubleConfirmationRequestBean.getRequestTime();
		int consentStatus = doubleConfirmationRequestBean.getConsentStatus();
		int retryCount = getRetryCount();
		String subscriberId = doubleConfirmationRequestBean.getSubscriberID();
		String transId = doubleConfirmationRequestBean.getTransId();
		String extraInfo = doubleConfirmationRequestBean.getExtraInfo();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberId);
		String response = null;
		Date date = new Date();
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(
				rbtDetailsRequest);
		logger.info("RBTSATDaemon: requestType : " + requestType
				+ " subscriberId:" + subscriberId + "subscriber: " + subscriber
				+ "retryCount: " + retryCount);
		if (extraInfo != null) {
			Map<String, String> extraInfoMap = DBUtility
					.getAttributeMapFromXML(extraInfo);
			if (extraInfoMap != null
					&& extraInfoMap.containsKey("TRANS_ID")) {
				selTransId = extraInfoMap.get("TRANS_ID");
				isComboReq = true;
				
			}
		}
		SatPushLoggerBean satPushLoggerBean = new SatPushLoggerBean();
		satPushLoggerBean.setSubscriberId(subscriberId);
		satPushLoggerBean.setRequestUrl(targetEndpoint+"?retry="+(retryCount+1)+"&retrycbckcount="+getCBCKRetryCount());
		if ("ACT".equalsIgnoreCase(requestType)
				&& (subscriber == null || !Utility.isSubActive(subscriber))
				&& (retryCount < maxRetry)) {
			defaultLanguage = (subscriber != null && subscriber.getLanguage() != null) ? subscriber
					.getLanguage() : defaultLanguage;
			offerText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					DOUBLE_OPT_IN_ACTIVATION_NOTIFICATION_SMS, defaultLanguage);
			logger.info("RBTSATDaemon: requestType : " + requestType);
			if(isComboReq){
				offerText = CacheManagerUtil.getSmsTextCacheManager()
						.getSmsText(DOUBLE_OPT_IN_COMBO_NOTIFICATION_SMS,
								defaultLanguage);
			}
			offerText = getSMStext(subscriber, offerText);
			logger.info("RBTSATDaemon: offerText : " + offerText);
			satPushLoggerBean.setRequestSentDate(Calendar.getInstance());
			response = pushSATRequest();
			satPushLoggerBean.setTimeTaken(""+(Calendar.getInstance().getTimeInMillis() - satPushLoggerBean.getRequestSentDate().getTimeInMillis()));
			satPushLoggerBean.setResponse(response);
			
			WriteCDRLog.writeSatPushCDRLog(satPushLoggerBean);

		} else if ("SEL".equalsIgnoreCase(requestType)
				&& (subscriber != null && Utility.isSubActive(subscriber))
				&& (retryCount < maxRetry)) {
			defaultLanguage = ((subscriber != null) && subscriber.getLanguage() != null) ? subscriber
					.getLanguage() : defaultLanguage;
			offerText = CacheManagerUtil.getSmsTextCacheManager().getSmsText(
					DOUBLE_OPT_IN_SELECTION_NOTIFICATION_SMS,
					defaultLanguage);
			offerText = getSMStext(subscriber, offerText);
			logger.info("RBTSATDaemon: offerText : " + offerText);
			satPushLoggerBean.setRequestSentDate(Calendar.getInstance());
			response = pushSATRequest();
			satPushLoggerBean.setTimeTaken(""+(Calendar.getInstance().getTimeInMillis() - satPushLoggerBean.getRequestSentDate().getTimeInMillis()));
			satPushLoggerBean.setResponse(response);
			WriteCDRLog.writeSatPushCDRLog(satPushLoggerBean);
		}

		logger.info("RBTSATDaemon-->pushSATRequest: response : " + response);
		if (retryCount >= maxRetry) {
			consentStatus = statusAfterConsentFailure;
			try {
				logger.info("RBTSATDaemon: maximum retry limit reached moving to consentStatus: "
						+ consentStatus);
				rbtDBManager.updateConsentStatusOfConsentRecord(subscriberId,
						transId, String.valueOf(consentStatus));
				if (isComboReq) {
					logger.info("RBTSATDaemon: maximum retry limit reached Updating selection of combo request for"
							+ subscriberId);
					rbtDBManager.updateConsentStatusOfConsentRecord(
							subscriberId, selTransId, String.valueOf(consentStatus));
				}
			} catch (OnMobileException e) {
				logger.info("Updating failure");
			}
		} else if (null != response) {
			if (response.equalsIgnoreCase(satSuccessKeyword)) {
				try {
					logger.info("RBTSATDaemon: updating consentStatus: "
							+ consentStatus);
					consentStatus = statusAfterSuccessfulHit;
					date.setMinutes(date.getMinutes() + successTime);
					extraInfo = updateRetryCount(extraInfo);
					rbtDBManager.updateExtraInfoAndStatusWithReqTime(
							subscriberId, transId, extraInfo,
							String.valueOf(consentStatus), date);
					if (isComboReq) {
						logger.info("RBTSATDaemon: Updating selection of combo request for"
								+ subscriberId);
						rbtDBManager.updateExtraInfoAndStatusWithReqTime(
								subscriberId, selTransId, "null",
								String.valueOf(comboselstatusAfterSuccessfulHit), date);
					}
				} catch (OnMobileException e) {
					logger.info("Updating failure");
				}
			} else {
				try {
					logger.info("RBTSATDaemon:  updating consentStatus: "
							+ consentStatus);
					date.setMinutes(date.getMinutes() + failureTime);
					if(response.equalsIgnoreCase(failureResponse)){
						date.setMinutes(date.getMinutes() + successTime);
					}
					extraInfo = updateRetryCount(extraInfo);
					rbtDBManager.updateExtraInfoAndStatusWithReqTime(
							subscriberId, transId, extraInfo,
							null, date);
					if (isComboReq) {
						logger.info("RBTSATDaemon: Updating selection of combo request for"
								+ subscriberId);
						rbtDBManager.updateExtraInfoAndStatusWithReqTime(
								subscriberId, selTransId, "null",
								null, date);
					}
				} catch (OnMobileException e) {
					logger.info("Updating failure");
				}

			}

		}
	}

	private String pushSATRequest() {
		try {
			String channelID = getChannelCode(doubleConfirmationRequestBean.getMode());
			logger.info("SAT PUSH Request SMS text  for msisdn: "
					+ doubleConfirmationRequestBean.getSubscriberID() + " "
					+ offerText+", satAcceptKeyword:"+satAcceptKeyword+", notificationUrl:"+notificationUrl+
					", requestPriority:"+requestPriority+", username:"+username+", channelID:"+channelID);
			PushServiceImplServiceStub pushServiceImplServiceStub = new PushServiceImplServiceStub(targetEndpoint);
			logger.info("pushServiceImplServiceStub"+pushServiceImplServiceStub);
			ExecuteServiceE executeServiceE = new ExecuteServiceE();
			logger.info("executeServiceE"+executeServiceE);
			ExecuteService executeService = new ExecuteService();
			logger.info("executeService"+executeService);
			ExecuteServiceType executeServiceType = new ExecuteServiceType();
			logger.info("executeServiceType"+executeServiceType);
			executeServiceType.setAcceptKeyword(satAcceptKeyword);
			// -> For RBT: 8889, For Dating: 3677
			executeServiceType.setAcceptTPDA(satPort);
			// -> For example: 1:OBD, 2: SMS, 3: VP, etc.
			executeServiceType
					.setChannelId(channelID);
			executeServiceType.setMsisdn(peruGemaltoCountryCode + doubleConfirmationRequestBean.getSubscriberID());
			executeServiceType.setNotificationUrl(notificationUrl);
			executeServiceType.setOfferText(offerText);
			executeServiceType.setPassword(password);
			executeServiceType.setRequestPriority(requestPriority);
			executeServiceType.setUsername(username);

			executeService.setExecuteServiceType(executeServiceType);
			logger.info("executeService"+executeService.toString());
			executeServiceE.setExecuteService(executeService);
			logger.info("Test executeServiceE"+executeServiceE.toString());
			ExecuteServiceResponseE responeE = pushServiceImplServiceStub
					.executeService(executeServiceE);
			logger.info("Test response"+responeE.toString());
			String response = responeE.getExecuteServiceResponse()
					.getExecuteServiceResponseType().getResponse();
			logger.info("SAT response"+response);
			return response;
		}catch(Exception e){
			e.printStackTrace();
			return failureResponse;
		}catch (Throwable e) {
			logger.info("Sat push exception, returning failure Response: "
					+ failureResponse + " ," + e.getMessage());
			return failureResponse;
		}

	}

	private String getChannelCode(String mode) {
		if(channelCode == null || channelCode.isEmpty()){
			return "";
		}
		mode = (mode != null) ? mode.toUpperCase() : "";
		return channelCode.get(mode);
	}

	private int getRetryCount() {
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(doubleConfirmationRequestBean
						.getExtraInfo());
		if (extraInfoMap != null && extraInfoMap.containsKey("RETRY_COUNT")) {
			return Integer.parseInt(extraInfoMap.get("RETRY_COUNT"));
		}

		return -1;
	}

	private String updateRetryCount(String extraInfo) {
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (extraInfoMap == null) {
			extraInfoMap = new HashMap<String, String>();
			extraInfoMap.put("RETRY_COUNT", "0");
		} else if (extraInfoMap != null
				&& !extraInfoMap.containsKey("RETRY_COUNT")) {
			extraInfoMap.put("RETRY_COUNT", "0");
		} else if (extraInfoMap.containsKey("RETRY_COUNT")) {
			extraInfoMap.put("RETRY_COUNT", String.valueOf(Integer
					.parseInt(extraInfoMap.get("RETRY_COUNT")) + 1));
		}
		extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
		return extraInfo;

	}

	private String getSMStext(Subscriber subscriber, String smsText) {
		HashMap<String, String> map = new HashMap<String, String>();
		String subscriptionClass = subscriber.getSubscriptionClass();
		SubscriptionClass subClass = CacheManagerUtil
				.getSubscriptionClassCacheManager().getSubscriptionClass(
						subscriptionClass);
		String subAmount = null;
		if (subClass != null) {
			subAmount = subClass.getSubscriptionAmount();
		}
		map.put("SMS_TEXT", smsText);
		map.put("CIRCLE_ID", subscriber.getCircleID());
		map.put("ACT_AMT", subAmount == null ? "" : subAmount);
		smsText = SmsProcessor.finalizeSmsText(map);
		logger.info("smsText: " + smsText);
		return smsText;
	}
	
	private String getCBCKRetryCount() {
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(doubleConfirmationRequestBean
						.getExtraInfo());
		if (extraInfoMap != null && extraInfoMap.containsKey("RETRY_CBCK_COUNT") && !extraInfoMap.get("RETRY_CBCK_COUNT").isEmpty()) {
			return extraInfoMap.get("RETRY_CBCK_COUNT");
		}

		return "0";
	}

}
