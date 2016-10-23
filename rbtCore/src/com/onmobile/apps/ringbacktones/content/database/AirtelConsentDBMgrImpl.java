package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.ConsentHitFailureException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.DoubleConfirmationConsentPushThread;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class AirtelConsentDBMgrImpl extends VirginDbMgrImpl {

	private static Logger logger = Logger
			.getLogger(AirtelConsentDBMgrImpl.class);

	private static List<String> modesForNotToGetConsentList = null;
	private static Map<String, String> modeMapping = null;
	private static Map<String, String> amtSubClassMapping = null;
	private static Map<String, String> amtChargeClassMapping = null;

	static {	
		String modesForNotToGetConsent = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "MODES_FOR_NOT_CONSENT_HIT", null);
		logger.info("modesForNotToGetConsent=" + modesForNotToGetConsent);
		if (modesForNotToGetConsent != null)
			modesForNotToGetConsentList = Arrays.asList(modesForNotToGetConsent
					.split(","));
		logger.info("modesForNotToGetConsentList="
				+ modesForNotToGetConsentList);

		String modeMappingStr = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "TPCG_MODES_MAP", null);
		logger.info("modeMappingStr=" + modeMappingStr);
		modeMapping = MapUtils.convertToMap(modeMappingStr, ";", "=", ",");
		logger.info("modeMapping=" + modeMapping);

		String amtSubClassStr = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "TPCG_SUB_CLASS_MAP", null);
		logger.info("amtSubClassStr=" + amtSubClassStr);
		amtSubClassMapping = MapUtils.convertToMap(amtSubClassStr, ";", "=",
				",");
		logger.info("amtSubClassMapping=" + amtSubClassMapping);

		String amtChargeClassStr = RBTParametersUtils.getParamAsString(
				"DOUBLE_CONFIRMATION", "TPCG_CHARGE_CLASS_MAP", null);
		logger.info("amtChargeClassStr=" + amtChargeClassStr);
		amtChargeClassMapping = MapUtils.convertToMap(amtChargeClassStr, ";",
				"=", ",");
		logger.info("amtChargeClassMapping=" + amtChargeClassMapping);
	}

	/**
	 * Third Party confirmation changes for activation
	 * 
	 */
	@Override
	protected Subscriber checkModeAndInsertIntoConsent(String subscriberID,
			String mode, Date startDate, Date endDate,
			boolean isDirectActivation, int rbtType, Connection conn,
			String prepaid, String subscription, String activationInfo,
			String cosID, String subscriptionClass, String refId,
			HashMap<String, String> extraInfoMap, String circleId, boolean isComboRequest,
			Map<String, String> xtraParametersMap) throws OnMobileException {
		Subscriber subscriber = SubscriberImpl.getSubscriber(conn,
				subID(subscriberID));
		logger.info("Checking mode for third party confirmation. subscriber: "
				+ subscriberID + ", mode: " + mode);
		if (Utility.isThirdPartyConfirmationRequired(mode, extraInfoMap)) {

			// RBT-9873 Added for bypassing CG flow
			if (Utility
					.isSubscriptionClassConfiguredForNotCGFlow(subscriptionClass)) {
				return null;
			}

			if (xtraParametersMap != null
					&& xtraParametersMap
							.containsKey(WebServiceConstants.param_isUdsOn)) {
				String udsOn = xtraParametersMap
						.get(WebServiceConstants.param_isUdsOn);
				if (udsOn.equalsIgnoreCase("true")) {
					if (extraInfoMap == null) {
						extraInfoMap = new HashMap<String,String>();
					}
					extraInfoMap.put(UDS_OPTIN, "TRUE");
				}
			}
			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			logger.info("Diverting normal flow for subscription, insert into Consent"
					+ " table for subscriber: " + subscriberID);

			String consentUniqueId = Utility
					.generateConsentIdRandomNumber(subscriberID);
			if (consentUniqueId != null) {
				refId = consentUniqueId;
			}

			if (null == refId) {
				refId = UUID.randomUUID().toString();
			}

			boolean isCGHitRequired = modesForNotToGetConsentList == null
					|| !modesForNotToGetConsentList.contains(mode);

			if (isComboRequest) {
				Map<String, String> mappedModeMap = MapUtils.convertToMap(
						RBTParametersUtils.getParamAsString(
								"DOUBLE_CONFIRMATION",
								"SWAPPED_MODES_MAPPING_FOR_CONSENT", ""), ";",
						"=", ",");
				if (mode != null && mappedModeMap != null
						&& mappedModeMap.containsKey(mode.toUpperCase())) {
					mode = mappedModeMap.get(mode.toUpperCase());
				}

			}
			String response = null;
			if (isCGHitRequired && !isComboRequest) {
				logger.info("Checking mode cg integration flow: "
						+ subscriberID + ", mode: " + mode);
				response = constructConsentUrlandHit(refId, mode, subscriberID,
						null, null, null, null, circleId, null, 's',
						subscriptionClass, null, "ACT", null, subscriber, null,
						null);
			}
			if (response == null
					|| response.equals(WebServiceConstants.SUCCESS)) {
				String subscriberRefId = UUID.randomUUID().toString();
				if (extraInfoMap == null) {
					extraInfoMap = new HashMap<String, String>();
				}
				extraInfoMap.put(iRBTConstant.EXTRA_INFO_TRANS_ID, refId);
				String language = xtraParametersMap
						.get(Constants.param_LANG_CODE);
				if (!isComboRequest) {
					extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					if (subscriber != null) {
						boolean success = SubscriberImpl.update(conn,
								subID(subscriberID), mode,
								null, startDate, endDate, prepaid, null, null, 0,
								activationInfo, subscriptionClass, null, null,
								null, subscription, 0, cosID, cosID, rbtType,
								((language==null)?subscriber.language():language), extraInfo,
								isDirectActivation, circleId, subscriberRefId);
						if (!success) {
							logger.warn("Subscription is not updated  into DB, subscriberID: "
									+ subscriberID + ". Returning count: 0");
							return null;
						}
						subscriber = SubscriberImpl.getSubscriber(conn,
								subID(subscriberID));
					} else {
						subscriber = SubscriberImpl.insert(conn,
								subID(subscriberID), mode, null, startDate,
								endDate, prepaid, null, null, 0, activationInfo,
								subscriptionClass, null, null, null, subscription,
								0, cosID, cosID, rbtType, language, isDirectActivation,
								extraInfo, circleId, subscriberRefId);
					}
				} else {
					subscriber = new SubscriberImpl(subID(subscriberID), mode, null,
							startDate, endDate, prepaid, null, null, 0, activationInfo,
							subscriptionClass, subscription, null, null, null, 0,
							cosID, null, rbtType, language, null, extraInfo, circleId,
							subscriberRefId);
				}
				if (xtraParametersMap != null) {
					xtraParametersMap
					.put("CONSENT_SUBSCRIPTION_INSERT", "TRUE");
				}
			}  else if (response != null && !response.equalsIgnoreCase("SUCCESS")) {
				throw new ConsentHitFailureException("CG Hit Failed with response: " + response);
			}
		}
		return subscriber;
	}

	/**
	 * Third Party confirmation changes for selection
	 */
	@Override
	protected SubscriberStatus checkModeAndInsertIntoConsent(
			String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime,
			Date endTime, int status, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, String classType,
			int fromTime, int toTime, String sel_status, int categoryType,
			char loopStatus, int rbtType, String selInterval, String transId,
			String circleId, Connection conn, HashMap<String, String> selXtraInfoMap,
			boolean useUIChargeClass, String baseConsentId, String feedSubType,
			String subscriptionClass, boolean isConsentActRecordInserted,
			boolean isAllowPremiumContent, boolean isUdsOnRequest,
			String slice_duration, boolean modeCheckForVfUpgrade,
			String oldSubscriptionClass, boolean smActivation, int nextPlus, Subscriber sub)
			throws OnMobileException {
		SubscriberStatus subscriberStatus = null;
		boolean isConsentByPassForOnlyActive = false;
		Subscriber subscriber = getSubscriber(subscriberID);
		if (Arrays.asList(
				RBTParametersUtils.getParamAsString(COMMON,
						"MODES_BYPASSING_CONSENT_GATEWAY_FOR_SELECTION", "")
						.split(",")).contains(selectedBy)) {
			isConsentByPassForOnlyActive = subscriber != null ? subscriber
					.subYes().equalsIgnoreCase("B") : false;
		}

		if ((Utility.isThirdPartyConfirmationRequired(selectedBy, selXtraInfoMap) && !isConsentByPassForOnlyActive)
				|| modeCheckForVfUpgrade) {

			String consentUniqueId = Utility
					.generateConsentIdRandomNumber(subscriberID);
			if (consentUniqueId != null) {
				transId = consentUniqueId;
			}

			if (null == transId) {
				transId = UUID.randomUUID().toString();
			}

			if (selXtraInfoMap == null) {
				selXtraInfoMap = new HashMap<String, String>();
			}
			if (isAllowPremiumContent) {
				selXtraInfoMap.put(
						WebServiceConstants.param_allowPremiumContent, "y");
			}

			if (isUdsOnRequest && !isConsentActRecordInserted) {
				selXtraInfoMap.put(UDS_OPTIN, "TRUE");
			}
			if (slice_duration != null) {
				selXtraInfoMap.put("slice_duration", slice_duration);
			}
			if (Utility.isChargeClassConfiguredForNotCGFlow(classType)
					&& !isConsentActRecordInserted) {
				return null;
			}
			// rbt_type has to be send it to sel_type column- Jira RBT-
			// RBT-12645
			/*
			 * boolean isConsentInserted =
			 * ConsentTableImpl.insertSelectionRecord( conn, refID,
			 * subscriberID, callerID, String.valueOf(categoryID),
			 * subscriptionClass, selectedBy, startTime, endTime, status,
			 * classType, null, null, null, selInterval, fromTime, toTime,
			 * selectionInfo, rbtType, isInLoop, null, useUIChargeClass,
			 * categoryType, null, true, feedSubType, subscriberWavFile, 0,
			 * circleId, null, null, extraInfo, "SEL", consentStatus);
			 */
			boolean isCGHitRequired = modesForNotToGetConsentList == null
					|| !modesForNotToGetConsentList.contains(selectedBy);

			String comboTransId = null;
			if (isConsentActRecordInserted) {
				comboTransId = transId;
			}
			
			String response = null;
			if (isCGHitRequired) {
				logger.info("Checking mode cg integration flow: "
						+ subscriberID + ", mode: " + selectedBy);
				Clip clipObj = RBTCacheManager.getInstance()
						.getClipByRbtWavFileName(subscriberWavFile);
				String baseServiceID = null;
				String consentType = null;
				SubscriptionClass subClass = null;
				if (isConsentActRecordInserted) {
					consentType = "ACTSEL";
					baseServiceID = DoubleConfirmationConsentPushThread
							.getServiceValue("SERVICE_ID",
									subscriptionClass, classType,
									circleId, true, false, false);
					subClass = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(
									subscriptionClass);
				} else {
					consentType = "SEL";
				}
				String selectionServiceID = DoubleConfirmationConsentPushThread
						.getServiceValue("SERVICE_ID", null, classType,
								circleId, false, true, false);
				String charge = "0";
				ChargeClass catCharge = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(classType);
				String catAmount = "0";
				String subAmount = "0";
				if (null != catCharge) {
					catAmount = catCharge.getAmount();
				}
				if (null != subClass) {
					subAmount = subClass.getSubscriptionAmount();
				}
				charge = String.valueOf(Integer.parseInt(catAmount)
						+ Integer.parseInt(subAmount));

				response = constructConsentUrlandHit(transId, selectedBy,
						subscriberID, clipObj, baseServiceID,
						selectionServiceID, charge, circleId,
						subscriberWavFile, loopStatus, subscriptionClass,
						classType, consentType, null, subscriber,
						selectionInfo, comboTransId);
			}
			if (response == null
					|| response.equals(WebServiceConstants.SUCCESS)) {
				String selRefId = UUID.randomUUID().toString();
				if (comboTransId != null) {
					String subscriberRefId = sub.refID();
					selXtraInfoMap.put("TRANS_ID", comboTransId);
					selXtraInfoMap.put("COMBO_TRANS_ID", comboTransId);
					Map<String, String> subExtraInfoMap = null;
					if (subscriber != null && subscriber.extraInfo() != null) {
						subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
					} else {
						subExtraInfoMap = new HashMap<String, String>();
					}
					subExtraInfoMap.put(iRBTConstant.EXTRA_INFO_TRANS_ID, comboTransId);
					String extraInfo = DBUtility.getAttributeXMLFromMap(subExtraInfoMap);
					if (sub != null) {
						sub.setExtraInfo(extraInfo);
					}
					if (subscriber != null) {
						boolean success = SubscriberImpl.update(conn,
								subID(subscriberID), selectedBy,
								null, sub.startDate(), sub.endDate(), prepaid, null, null, 0,
								sub.activationInfo(), sub.subscriptionClass(), null, null,
								null, "A", 0, sub.cosID(), sub.cosID(), rbtType,
								sub.language(), extraInfo,
								false, circleId, subscriberRefId);
						if (!success) {
							logger.warn("Subscription is not updated  into DB, subscriberID: "
									+ subscriberID + ". Returning count: 0");
							return null;
						}
						subscriber = SubscriberImpl.getSubscriber(conn,
								subID(subscriberID));
					} else {
						subscriber = SubscriberImpl.insert(conn,
								subID(subscriberID), selectedBy, null, sub.startDate(),
								sub.endDate(), prepaid, null, null, 0, sub.activationInfo(),
								sub.subscriptionClass(), null, null, null, "A",
								0, sub.cosID(), sub.cosID(), rbtType, sub.language(), false,
								extraInfo, circleId, subscriberRefId);
					}	
				} else {
					selXtraInfoMap.put("TRANS_ID", transId);
				}
				String selExtraInfo = DBUtility.getAttributeXMLFromMap(selXtraInfoMap);
				subscriberStatus = SubscriberStatusImpl.insert(conn,
						subID(subscriberID), callerID, categoryID,
						subscriberWavFile, setTime, startTime, endTime, status,
						classType, selectedBy, selectionInfo, nextChargingDate,
						prepaid, fromTime, toTime, smActivation, sel_status,
						null, null, categoryType, loopStatus, nextPlus,
						rbtType, selInterval, selExtraInfo, selRefId, circleId, null, null);
			} else if (response != null && !response.equalsIgnoreCase("SUCCESS")) {
				throw new ConsentHitFailureException("CG Hit Failed with response: " + response);
			}
		}
		return subscriberStatus;
	}

	private String constructConsentUrlandHit(String tid, String mode,
			String subscriberID, Clip clipObj, String baseServiceID,
			String selectionServiceID, String charge, String circleId,
			String wavFile, char loopStatus, String subClass, String classType,
			String consentType, Date requestTime, Subscriber subscriber,
			String selectionInfo, String comboTransID) {
		try {
			long timeTakenToHITConsent = 0;
			String consentUrl = RBTParametersUtils.getParamAsString(
					"DOUBLE_CONFIRMATION",
					"DOUBLE_CONFIRMATION_CONSENT_PUSH_URL", null);
			String pPrice = "-1";
			String pVal = "-1";
			String clipId = null;
			String vcode = null;
			boolean isActAndSelRequest = false;
			boolean isSelRequest = false;
			boolean isActRequest = false;
			if (consentType.equals("ACT")) {
				isActRequest = true;
				logger.debug("isActRequest: " + isActRequest);
			} else if (consentType.equals("ACTSEL")) {
				isActAndSelRequest = true;
				logger.debug("Combo Charge class = " + classType
						+ ", isActAndSelRequest: " + isActAndSelRequest);
				vcode = getVcode(clipObj, wavFile);
			} else if (consentType.equals("SEL")) {
				isSelRequest = true;
				vcode = getVcode(clipObj, clipId);
				logger.debug("isSelRequest: " + isSelRequest);
			}

			Boolean isSeparatePriceAndValidityForComboEnabled = Boolean
					.parseBoolean(CacheManagerUtil.getParametersCacheManager()
							.getParameterValue(
									iRBTConstant.COMMON,
									"SEPARATE_PRICE AND VALIDITY_FO"
											+ "R_COMBO_ENABLED", "FALSE"));
			if (isSelRequest) {
				pPrice = String.valueOf(DoubleConfirmationConsentPushThread
						.getPrice(amtChargeClassMapping, classType));
				pVal = String.valueOf(DoubleConfirmationConsentPushThread
						.getValidityPeriod(amtChargeClassMapping, classType));
			} else {
				int pPrice1 = DoubleConfirmationConsentPushThread.getPrice(
						amtSubClassMapping, subClass);
				int pPrice2 = DoubleConfirmationConsentPushThread.getPrice(
						amtChargeClassMapping, classType);
				if (isSeparatePriceAndValidityForComboEnabled
						&& classType != null) {
					pPrice = String.valueOf(pPrice1 + "|" + pPrice2);
					pVal = DoubleConfirmationConsentPushThread
							.getValidityPeriod(amtSubClassMapping, subClass)
							+ "|"
							+ DoubleConfirmationConsentPushThread
									.getValidityPeriod(amtChargeClassMapping,
											classType);
				} else {
					pPrice = String.valueOf(pPrice1 + pPrice2);
					pVal = String.valueOf(DoubleConfirmationConsentPushThread
							.getValidityPeriod(amtSubClassMapping, subClass));
				}
			}

			logger.debug("pPrice == " + pPrice + " pVal === " + pVal);

			String reqMode = (modeMapping != null && modeMapping
					.containsKey(mode)) ? modeMapping.get(mode) : mode;
			String reqType = consentType;
			if ((reqType != null && reqType.startsWith("ACT"))
					|| comboTransID != null) {
				if (comboTransID != null
						&& isSeparatePriceAndValidityForComboEnabled) {
					reqType = "Subscription|SongDownload";
				} else {
					reqType = "Subscription";
				}
			} else {
				reqType = "SongDownload";
			}
			String bParty = getBParty(selectionInfo);

			consentUrl = consentUrl.replaceAll("%MSISDN%", subscriberID);
			consentUrl = consentUrl.replaceAll("%MODE%", reqMode);
			consentUrl = consentUrl.replaceAll("%TRANS_ID%", tid);
			consentUrl = consentUrl.replaceAll("%CIRCLE_ID%", circleId);

			consentUrl = consentUrl.replaceAll("%REQ_TYPE%", reqType);
			consentUrl = consentUrl.replaceAll("%PRICE%", pPrice + "");
			consentUrl = consentUrl.replaceAll("%VALIDITY%", pVal + "");

			if (bParty != null) {
				consentUrl = consentUrl.replaceAll("%BPARTY%", bParty);
			}

			if (vcode != null) {
				consentUrl = consentUrl.replaceAll("%VCODE%", vcode);
			}

			logger.info("Consent url: " + consentUrl);
			long initConsentPushMillis = System.currentTimeMillis();
			String response = DoubleConfirmationHttpUtils
					.getResponse(consentUrl);
			long endConsentPushMillis = System.currentTimeMillis();
			timeTakenToHITConsent = endConsentPushMillis
					- initConsentPushMillis;
			logger.info("Consent url: " + consentUrl + ", time taken: "
					+ timeTakenToHITConsent + ", response: " + response);
			String[] responses = response.split("\\-");
			String urlResponse = null;
			int responseCode = 0;
			try {
				responseCode = Integer.parseInt(responses[0]);
			} catch (Exception e) {
				urlResponse = responses[0];
			}
			// CG Integration Flow - Jira -12806
			if (response != null
					&& (responseCode == 200 || responseCode == 534 || (urlResponse != null && urlResponse
							.equalsIgnoreCase("SUCCESS")))) {
				logger.info("Successfully Hit CG. Response: "
						+ WebServiceConstants.FAILURE);
				return WebServiceConstants.SUCCESS;
			} else {
				logger.info("CG Hit failed. Response: "
						+ WebServiceConstants.FAILURE);
				return WebServiceConstants.FAILURE;
			}

		} catch (Exception ex) {
			logger.info("Exception while hitting Consent url...", ex);
			return WebServiceConstants.FAILURE;
		}
	}

	public int createSubscriberStatus(String subscriberID, String callerID,
			int categoryID, String subscriberWavFile, Date setTime,
			Date startTime, Date endTime, int status, String selectedBy,
			String selectionInfo, Date nextChargingDate, String prepaid,
			String classType, boolean changeSubType, int fromTime, int toTime,
			String sel_status, boolean smActivation, @SuppressWarnings("rawtypes") HashMap clipMap,
			int categoryType, boolean useDate, char loopStatus, boolean isTata,
			int nextPlus, int rbtType, String selInterval,
			@SuppressWarnings("rawtypes") HashMap extraInfoMap, String refID, boolean isDirectActivation, 
			String circleId, Subscriber sub, boolean useUIChargeClass, boolean isFromDownload) {
		int count = super.createSubscriberStatus(subscriberID, callerID, categoryID,
				subscriberWavFile, setTime, startTime, endTime, status,
				selectedBy, selectionInfo, nextChargingDate, prepaid,
				classType, changeSubType, fromTime, toTime, sel_status,
				smActivation, clipMap, categoryType, useDate, loopStatus,
				isTata, nextPlus, rbtType, selInterval, extraInfoMap, refID,
				isDirectActivation, circleId, sub, useUIChargeClass,
				isFromDownload);
		if (count == 2) {
			// For consent case we are setting count as 2 in
			// super.createSubscriberStatus(). But in Airtel consent flow,
			// selection entry has already been made in rbt_subscriber_selection
			// table. Hence the count should be 1 instead of 2.
			return count = 1;
		}
		return count;
	}
	private String getVcode(Clip clipObj, String wavFileName) {
		if (clipObj == null)
			return null;
		String vcode = null;
		String wavFile = null;
		if (clipObj.getClipId() != -1) {
			if (clipObj != null) {
				wavFile = clipObj.getClipRbtWavFile();
			}
		}
		if (wavFile == null) {
			wavFile = wavFileName;
		}
		if (wavFile != null) {
			vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
		}
		return vcode;
	}

	private String getBParty(String selInfo) {
		String bparty = null;
		if (selInfo != null && selInfo.indexOf("CP") != -1) {
			String str[] = selInfo.split("\\|");
			for (int i = 0; i < str.length; i++) {
				String ss = str[i];
				if (ss.indexOf("CP") != -1) {
					ss = ss.replaceAll("CP:", "").replaceAll(":CP", "");
					String str1[] = ss != null ? ss.split("-") : null;
					if (str1 != null && str1.length == 2) {
						bparty = str1[1];
					}
				}
			}
		}
		return bparty;
	}
}