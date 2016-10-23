package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class TelefonicaPeruDbMgrImpl extends TelefonicaDbMgrImpl {
	private static Logger logger = Logger.getLogger(TelefonicaPeruDbMgrImpl.class);
	private static boolean useDoubleOptInSMSNotification = RBTParametersUtils
			.getParamAsBoolean(iRBTConstant.WEBSERVICE,
					"USE_OPT_IN_SMS_NOTIFICATION", "FALSE");
	
	@Override
	protected Subscriber checkModeAndInsertIntoConsent(String subscriberID,
			String mode, Date startDate, Date endDate,
			boolean isDirectActivation, int rbtType, Connection conn,
			String prepaid, String subscription, String activationInfo,
			String cosID, String subscriptionClass, String refId,
			HashMap<String,String> extraInfoMap, String circleId, boolean isComboRequest,
			Map<String, String> xtraParametersMap) throws OnMobileException {
		Subscriber subscriber = null;
		logger.info("Checking mode for third party confirmation. subscriber: "
				+ subscriberID + ", mode: " + mode);
		if (Utility.isThirdPartyConfirmationRequired(mode, extraInfoMap)) {

			if(Utility.isSubscriptionClassConfiguredForNotCGFlow(subscriptionClass)) {
				return null;
			}

			removeTPCGIDFromMap(extraInfoMap);
			// For Enabling UDS_OPTIN through Consent Model for New user
			if (xtraParametersMap != null && xtraParametersMap.containsKey(WebServiceConstants.param_isUdsOn)) {
				String udsOn = xtraParametersMap.get(WebServiceConstants.param_isUdsOn);
				if (udsOn.equalsIgnoreCase("true")) {
					extraInfoMap.put(UDS_OPTIN, "TRUE");
				}
			}

			String extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			logger
			.info("Diverting normal flow for subscription, insert into Consent"
					+ " table for subscriber: " + subscriberID);

			String consentUniqueId = Utility.generateConsentIdRandomNumber(subscriberID);
			if (consentUniqueId != null) {
				refId = consentUniqueId;
			}

			if (null == refId) {
				refId = UUID.randomUUID().toString();
			}
			boolean isPresent = false;


			if (!RBTParametersUtils.getParamAsBoolean(DOUBLE_CONFIRMATION,
					"ACCEPT_MULTIPLE_BASE_CONSENT", "FALSE"))
				isPresent = ConsentTableImpl.isSameConsentActivationRequest(
						conn, subscriberID, null, "0,1,2");

			if (isPresent) {
				refId = null;
			}

			if (!isPresent) {
				int consentStatus = WebServiceConstants.sat_consent_pending_status;
				if(useDoubleOptInSMSNotification){
					consentStatus = WebServiceConstants.consent_pending_status;
				}
				if (isComboRequest) {
					Map<String, String> mappedModeMap = MapUtils.convertToMap(RBTParametersUtils
							.getParamAsString("DOUBLE_CONFIRMATION",
									"SWAPPED_MODES_MAPPING_FOR_CONSENT", ""), ";", "=", ",");
					if (mode != null && mappedModeMap != null
							&& mappedModeMap.containsKey(mode.toUpperCase())) {
						mode = mappedModeMap.get(mode.toUpperCase());
					}

				}
				boolean success = ConsentTableImpl.insertSubscriptionRecord(conn, subscriberID,
						mode, startDate, endDate, activationInfo, prepaid,
						subscriptionClass, cosID, rbtType, isDirectActivation,
						extraInfo, circleId, refId, consentStatus, false, null,null);
				if(success && xtraParametersMap != null) {
					xtraParametersMap.put("CONSENT_SUBSCRIPTION_INSERT", "TRUE");
				}
			}
			subscriber = new SubscriberImpl(subID(subscriberID), mode, null,
					startDate, endDate, prepaid, null, null, 0, activationInfo,
					subscriptionClass, subscription, null, null, null, 0,
					cosID, null, rbtType, null, null, extraInfo, circleId,
					refId);
		} else {
			removeTPCGIDFromMap(extraInfoMap);
		}
		return subscriber;
	}

	@Override
	protected SubscriberStatus checkModeAndInsertIntoConsent(
			String subscriberID, String callerID, int categoryID,
			String subscriberWavFile, Date setTime, Date startTime,
			Date endTime, int status, String selectedBy, String selectionInfo,
			Date nextChargingDate, String prepaid, String classType,
			int fromTime, int toTime, String sel_status, int categoryType,
			char loopStatus, int rbtType, String selInterval, String refID,
			String circleId, Connection conn, HashMap<String, String> selXtraInfoMap,
			boolean useUIChargeClass, String baseConsentId, String feedSubType,
			String subscriptionClass, boolean isConsentActRecordInserted,
			boolean isAllowPremiumContent, boolean isUdsOnRequest,
			String slice_duration, boolean modeCheckForVfUpgrade,
			String oldSubscriptionClass, boolean smActivation, int nextPlus,
			Subscriber sub2) throws OnMobileException {

		SubscriberStatus subscriberStatus = null;
		boolean isConsentByPassForOnlyActive = false;
		if (Arrays.asList(RBTParametersUtils.getParamAsString(COMMON, "MODES_BYPASSING_CONSENT_GATEWAY_FOR_SELECTION", "").split(",")).contains(selectedBy)){
			Subscriber sub =  getSubscriber(subscriberID);
			isConsentByPassForOnlyActive = sub!=null?sub.subYes().equalsIgnoreCase("B"):false;
		}

		if ((Utility.isThirdPartyConfirmationRequired(selectedBy, selXtraInfoMap) && !isConsentByPassForOnlyActive) || modeCheckForVfUpgrade) {

			String consentUniqueId = Utility.generateConsentIdRandomNumber(subscriberID);
			if(consentUniqueId != null) {
				refID = consentUniqueId;
			}

			if (null == refID) {
				refID = UUID.randomUUID().toString();
			}

			boolean isInLoop = false;
			if(loopStatus == LOOP_STATUS_LOOP_INIT || loopStatus == LOOP_STATUS_LOOP || loopStatus == LOOP_STATUS_LOOP_FINAL) {
				isInLoop = true;
			}
			if(selXtraInfoMap == null){
				selXtraInfoMap = new HashMap<String,String> ();
			}
			if(isAllowPremiumContent){
				selXtraInfoMap.put(WebServiceConstants.param_allowPremiumContent, "y");
			}

			if(isUdsOnRequest && !isConsentActRecordInserted){
				selXtraInfoMap.put(UDS_OPTIN, "TRUE");
			}
			if(slice_duration!=null){
				selXtraInfoMap.put("slice_duration", slice_duration);
			}
			int consentStatus = WebServiceConstants.sat_consent_pending_status;
			if(useDoubleOptInSMSNotification){
				consentStatus = WebServiceConstants.consent_pending_status;
			}
			boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
					.getParamAsBoolean(COMMON,
							"ADD_BASE_CONSENT_ID_IN_EXTRA_INFO", "FALSE");
			if(baseConsentId != null) {
				List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = getDoubleConfirmationRequestBeanForStatus(null, baseConsentId, subscriberID, null, true);
				if(doubleConfirmReqBeans != null && doubleConfirmReqBeans.size() > 0) {
					consentStatus = WebServiceConstants.sat_sel_combo_consent_pending_status;
					if(useDoubleOptInSMSNotification){
						consentStatus = WebServiceConstants.sel_combo_consent_pending_status;
					}
					DoubleConfirmationRequestBean doubleConfirmReqBean = doubleConfirmReqBeans.get(0);
					Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(doubleConfirmReqBean.getExtraInfo());
					if(extraInfoMap == null) {
						extraInfoMap = new HashMap<String, String>();
					}
					extraInfoMap.put("TRANS_ID", refID);
					String extraInfoXml = DBUtility.getAttributeXMLFromMap(extraInfoMap);
					updateConsentExtrInfoAndStatus(doubleConfirmReqBean.getSubscriberID(), baseConsentId, extraInfoXml, null);
					Map<String, String> mappedModeMap = MapUtils.convertToMap(RBTParametersUtils
							.getParamAsString("DOUBLE_CONFIRMATION", "SWAPPED_MODES_MAPPING_FOR_CONSENT", ""),";" , "=" , ",");
					if(selectedBy != null && mappedModeMap!=null && mappedModeMap.containsKey(selectedBy.toUpperCase())) { 
						selectedBy = mappedModeMap.get(selectedBy.toUpperCase()); 
					}
					if (modeCheckForVfUpgrade) {
						selectedBy = VfRBTUpgardeConsentFeatureImpl
								.getMappedModeForUpgrade(oldSubscriptionClass,
										doubleConfirmReqBean
										.getSubscriptionClass(),
										selectedBy);
					}
				}
				if (addBaseConsentInSelExtraInfo) {
					selXtraInfoMap.put("TRANS_ID", baseConsentId);
				}
			}
			//CG Integration Flow - Jira -12806
			String extraInfo = DBUtility.getAttributeXMLFromMap(selXtraInfoMap);
			//RBT-9873 Added for bypassing CG flow
			if(Utility.isChargeClassConfiguredForNotCGFlow(classType) && !isConsentActRecordInserted) {
				return null;
			}
			removeTPCGIDFromMap(selXtraInfoMap);
			//rbt_type has to be send it to sel_type column- Jira RBT- RBT-12645
			boolean isConsentInserted = ConsentTableImpl.insertSelectionRecord(
					conn, refID, subscriberID, callerID,
					String.valueOf(categoryID), null, selectedBy, startTime,
					endTime, status, classType, null, null, null, selInterval, fromTime,
					toTime, selectionInfo, rbtType, isInLoop, null, useUIChargeClass, categoryType, null, true,
					feedSubType, subscriberWavFile, 0, circleId, null, null, extraInfo, "SEL",
					consentStatus,null,null);

			logger.info("Diverting normal flow for selection, "
					+ "inserted into Consent table for subscriber: "
					+ subscriberID + ", refId: " + refID
					+ ", isConsentInserted: " + isConsentInserted);

			subscriberStatus = new SubscriberStatusImpl(subscriberID, callerID,
					categoryID, subscriberWavFile, setTime, startTime, endTime,
					status, classType, selectedBy, selectionInfo,
					nextChargingDate, prepaid, fromTime, toTime, sel_status,
					null, null, categoryType, loopStatus, rbtType, selInterval,
					refID, extraInfo, circleId,null);
		} else {
			removeTPCGIDFromMap(selXtraInfoMap);
		}
		return subscriberStatus;
	}

	@Override
	protected SubscriberDownloads checkModeAndInsertIntoConsent(
			String subscriberId, String subscriberWavFile, Date endDate,
			boolean isSubActive, String mode, String selectionInfo,
			boolean isSmClientModel, Connection conn, int categoryID,
			int categoryType, String nextClass, HashMap<String, String> extraInfo,
			String baseConsentId, boolean useUIChargeClass)
			throws OnMobileException {
		SubscriberDownloads subscriberDownlaods = super.checkModeAndInsertIntoConsent(subscriberId, subscriberWavFile,
				endDate, isSubActive, mode, selectionInfo, isSmClientModel, conn,
				categoryID, categoryType, nextClass, extraInfo, baseConsentId,
				useUIChargeClass);
//		removeTPCGIDFromMap(extraInfo);
		return subscriberDownlaods;
	}
	
	private void removeTPCGIDFromMap(HashMap<String, String> extraInfoMap) {
		if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.EXTRA_INFO_TPCGID)) {
			extraInfoMap.remove(iRBTConstant.EXTRA_INFO_TPCGID);
		}
	}
}