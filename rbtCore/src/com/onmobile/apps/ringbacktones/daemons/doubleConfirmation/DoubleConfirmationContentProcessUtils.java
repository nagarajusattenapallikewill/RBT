package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTCallBackEvent;
import com.onmobile.apps.ringbacktones.logger.RbtLogger;
import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.logger.BasicCDRLogger;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.common.exception.OnMobileException;

public class DoubleConfirmationContentProcessUtils implements iRBTConstant {
	
	static Logger logger = Logger
			.getLogger(DoubleConfirmationContentProcessUtils.class);
	private static RBTDBManager dbManager = RBTDBManager.getInstance();
	private static String loggerName = "CONTENT.PROCESS";
	private static Logger contentProcessLogger = RbtLogger
			.createRollingFileLogger(RbtLogger.consentTransactionPrefix
					+ loggerName, ROLLING_FREQUENCY.HOURLY);
	//Added for TTG-14814
	Logger third_party_logger = Logger.getLogger("ThirdPartyLogger");
	//End of TTG-14814
	
	public DoubleConfirmationContentProcessUtils() {

	}
	
	public String processRecord(DoubleConfirmationRequestBean reqBean) {
		return processRecord(reqBean, null);
	}

	public String processRecord(DoubleConfirmationRequestBean reqBean, String reqTransId) {
		String response = "failure";

		if (reqBean == null) {
			return "success";
		}
		try {
			long initContentTimeToProcess = System.currentTimeMillis();
			List<DoubleConfirmationRequestBean> selConsentBeans = null;
			if (reqBean.getRequestType().equalsIgnoreCase("ACT") || reqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
				logger.info("Processing consent activation request. consent transId: "
						+ reqBean.getTransId());
				Map<String, String> extraInfoMap = DBUtility
						.getAttributeMapFromXML(reqBean.getExtraInfo());
				boolean isPorcessSelectionRequest = !RBTParametersUtils
						.getParamAsBoolean("COMMON",
								"MAKE_ENTRY_CONSENT_ACT_USER_SEL", "TRUE");

				String makeConsentForConfigChargeClass = RBTParametersUtils
						.getParamAsString(COMMON, "CHARGE_CLASS_FOR_CONSENT",
								null);
				List<String> consentChargeClass = null;
				if (makeConsentForConfigChargeClass != null) {
					consentChargeClass = Arrays
							.asList(makeConsentForConfigChargeClass.split(","));
				}

				boolean isConsentChargeClassProcessed = false;

				String selectionTransId = null;
				if (extraInfoMap != null
						&& extraInfoMap.containsKey("TRANS_ID")) { //Combo Consent Request
					selectionTransId = extraInfoMap.get("TRANS_ID");
					logger.info("Processing combo request. selectionTransId: "
							+ selectionTransId);
					String tpcgId = extraInfoMap.get("TPCGID");
					selConsentBeans = dbManager
							.getDoubleConfirmationRequestBeanForStatus(null,
									selectionTransId,
									reqBean.getSubscriberID(), null, true);
					if (selConsentBeans != null && selConsentBeans.size() > 0) {
						DoubleConfirmationRequestBean requestBean = selConsentBeans
								.get(0);
						if (consentChargeClass != null
								&& requestBean.getClassType() != null
								&& consentChargeClass.contains(requestBean
										.getClassType())) {
							isConsentChargeClassProcessed = true;
						}
						String selResponse = processSelections(requestBean,
								reqBean, tpcgId, reqTransId);
						response = selResponse;
						logger.info("Successfully processed combo request. tpcgId: "
								+ tpcgId + ", selResponse: " + selResponse);
					}
				} else { // Once base consent request
					logger.debug("Processing base activation request. selectionTransId: "
							+ selectionTransId);
					response = processActivation(reqBean, reqTransId);
					logger.info("Successfully processed base activation request. transId: "
							+ reqBean.getTransId()
							+ ", selResponse: "
							+ response);
				}

				if (isPorcessSelectionRequest && !isConsentChargeClassProcessed) {
					String tpcgId = null;
					if (extraInfoMap != null
							&& extraInfoMap.containsKey("TPCGID")) {
						tpcgId = extraInfoMap.get("TPCGID");
					}
					selConsentBeans = dbManager.getSelectionConsentRequests(
							reqBean.getSubscriberID(), "SEL", "1", false);

					for (DoubleConfirmationRequestBean requestBean : selConsentBeans) {
						if (consentChargeClass != null
								&& requestBean.getClassType() != null
								&& consentChargeClass.contains(requestBean
										.getClassType())) {
							continue;
						}
						String selResponse = processSelections(requestBean,
								null, tpcgId, reqTransId);
					}
					logger.info("Returning response: " + response
							+ ", for request type ACT. transID: "
							+ reqBean.getTransId());
					return response;
				}
			} else if (reqBean.getRequestType().equalsIgnoreCase("SEL")
					|| reqBean.getRequestType().equalsIgnoreCase("DWN")) {
				logger.info("Processing consent selection request. consent transId: "
						+ reqBean.getTransId());
				response = processSelections(reqBean, null,
						reqBean.getTransId(), reqTransId);
				logger.info("Successfully processed consent selection request. response: "
						+ response
						+ ", consent transId: "
						+ reqBean.getTransId());
			}
			long endContentTimeToProcess = System.currentTimeMillis();
			long timeTakenToProcess = endContentTimeToProcess
					- initContentTimeToProcess;
			contentProcessLogger.info("TIME_TAKEN_TO_PROCESS_CONTENT = "
					+ timeTakenToProcess + " , REQUEST_BEAN = " + reqBean
					+ " , SEL_BEAN" + selConsentBeans);
		} catch (Exception ex) {
			logger.info("Exception while Processing Consent records......");
			ex.printStackTrace();
		}
		return response;
	}

	private boolean updateConsentStatus(DoubleConfirmationRequestBean reqBean) {
		boolean isUpdate = false;
		try {
			logger.debug("Updating consent status. reqBean: " + reqBean);
			isUpdate = dbManager.updateConsentStatusOfConsentRecord(
					reqBean.getSubscriberID(), reqBean.getTransId(), "3", null, reqBean.getInlineFlag());
		} catch (OnMobileException e) {
			logger.error("Exception", e);
		}
		logger.info("Updated consent status. isUpdated: " + isUpdate
				+ ", reqBean: " + reqBean);
		return isUpdate;
	}

	private String processActivation(DoubleConfirmationRequestBean reqBean, String reqTransId) {
		String response = "failure";
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				reqBean.getSubscriberID());
		subscriptionRequest.setActivationMode(reqBean.getMode());
		//commented RBT-18219	Daemon is unable to make entry of subscriber in rbt_subscriber table
		//subscriptionRequest.setCircleID(reqBean.getCircleId());
		// int cosId = reqBean.getCosId();
		if (reqBean.getCosId() != null && reqBean.getCosId() != -1) {
			subscriptionRequest.setCosID(reqBean.getCosId());
		}
		subscriptionRequest.setMode(reqBean.getMode());
		subscriptionRequest.setModeInfo(reqBean.getSelectionInfo());
		subscriptionRequest
				.setSubscriptionClass(reqBean.getSubscriptionClass());
		int packCosId = reqBean.getPackCosID();
		if (packCosId != -1 && packCosId != 0) {
			subscriptionRequest.setPackCosId(reqBean.getPackCosID());
		}
		subscriptionRequest.setSubscriberEndDate(reqBean.getEndTime());
		boolean isUdsOptinRequestAlso = false;
		String extraInfo = reqBean.getExtraInfo();
		HashMap<String, String> xtraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		String udsType=null;
		if (xtraInfoMap != null) {
			if (xtraInfoMap.containsKey("TPCGID")) {
				String tpcgID = xtraInfoMap.get("TPCGID");
				subscriptionRequest.setTpcgID(tpcgID);
			}
			if (xtraInfoMap.containsKey(UDS_OPTIN)) {
				isUdsOptinRequestAlso = true;
				udsType=xtraInfoMap.get(UDS_OPTIN);
			}
		}
		//this for normal user upgradation. RBT - 13221.
		if (reqBean.getRequestType() != null
				&& reqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
			subscriptionRequest.setRentalPack(subscriptionRequest
					.getSubscriptionClass());
			xtraInfoMap.put("UPGRADE_CONSENT", "true");
		}
		boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.DOUBLE_CONFIRMATION,
				"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
		//this for normal user upgradation. RBT - 13221.
		boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
				.getParamAsBoolean(COMMON,
						"ADD_BASE_CONSENT_ID_IN_EXTRA_INFO", "FALSE");
		if (checkCGFlowForBSNL || addBaseConsentInSelExtraInfo) {
			if (reqTransId != null) {
				xtraInfoMap.put("TRANS_ID", reqTransId);
			} else {
				xtraInfoMap.put("TRANS_ID", reqBean.getTransId());
			}
		}
		xtraInfoMap.put("ISDAEMON", "true");
		subscriptionRequest.setUserInfoMap(xtraInfoMap);
		subscriptionRequest.setLanguage(reqBean.getLanguage());
		subscriptionRequest.setRbtType(reqBean.getRbtType());
		logger.debug("Activating subscriber. subscriptionRequest: "
				+ subscriptionRequest);
		Subscriber subscriber = RBTClient.getInstance().activateSubscriber(
				subscriptionRequest);
		response = subscriptionRequest.getResponse();
		logger.info("Activated subscriber. response: " + response
				+ ", subscriber: " + subscriber);
		if (response.equalsIgnoreCase("success")
				|| Utility.isUserActive(response)) {
			response = "success";
		}
		long intialTime=System.currentTimeMillis();
		updateConsentStatus(reqBean);
		long finalTime=System.currentTimeMillis();
		long timeTaken=finalTime-intialTime;
		if (isUdsOptinRequestAlso) {
			UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(null);
			updateRequest.setSubscriberID(reqBean.getSubscriberID());
			updateRequest.setIsUdsOn(true);
			updateRequest.setUdsType(udsType);
			RBTClient.getInstance().setSubscriberDetails(updateRequest);
		}
		
		//Added for TTG-14814
		third_party_logger.info(timeTaken+", Base "+", "+(subscriber!=null?subscriber.getRefID():null)+", "+"null, "+(reqBean!=null?reqBean.getTransId():null)+", "+(reqBean!=null?reqBean.getMode():null));
		//End of TTG-14814
		
		return response;
	}

	private String processSelections(DoubleConfirmationRequestBean reqBean,
			DoubleConfirmationRequestBean actReqBean, String tpcgID, String reqTransId) {
		logger.info("Processing selection. reqBean: " + reqBean
				+ ", base actReqBean: " + actReqBean + ", tpcgID: " + tpcgID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				reqBean.getSubscriberID());
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(
				rbtDetailsRequest);
		if (null == actReqBean && !Utility.isUserActive(subscriber.getStatus())) {
			return "error";
		}

		String response = "failure";
		SelectionRequest selectionRequest = new SelectionRequest(
				reqBean.getSubscriberID());
		selectionRequest.setCallerID(reqBean.getCallerID());
		selectionRequest.setCategoryID(reqBean.getCategoryID() + "");
		selectionRequest.setChargeClass(reqBean.getClassType());
		//commented RBT-18219	Daemon is unable to make entry of subscriber in rbt_subscriber table
		//selectionRequest.setCircleID(reqBean.getCircleId());
		String extraInfo = reqBean.getExtraInfo();
		HashMap<String, String> xtraInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfo);
		if (xtraInfoMap == null) {
			xtraInfoMap = new HashMap<String, String>();
		}
		if (null != actReqBean) {
			if (actReqBean.getRequestType() != null
					&& actReqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
				selectionRequest.setRentalPack(actReqBean
						.getSubscriptionClass());
				xtraInfoMap.put("UPGRADE_CONSENT", "true");
				HashMap<String, String> actExtraInfoMap = DBUtility
						.getAttributeMapFromXML(actReqBean.getExtraInfo());
				if (actExtraInfoMap.containsKey("P2P_UPGRADE")) {
					xtraInfoMap.put("P2P_UPGRADE", "true");
				}
			} else {
				selectionRequest.setSubscriptionClass(actReqBean
						.getSubscriptionClass());
			}
		}
		
		int packCosId = reqBean.getPackCosID();
		if (packCosId != -1 && packCosId != 0) {
			selectionRequest.setPackCosId(packCosId);
		}
		String wavFileName = reqBean.getWavFileName();
		if (wavFileName != null) {
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					wavFileName);
			if (clip != null) {
				selectionRequest.setClipID(clip.getClipId() + "");
			}
			// added for cut rbt and ugc 
			if(clip == null || wavFileName.contains("_cut_")){
				selectionRequest.setClipID(wavFileName);
			}
		} else {
			if (reqBean.getClipID() != -1) {
				selectionRequest.setClipID(reqBean.getClipID() + "");
			}
		}
		if (reqBean.getCosId() != null && reqBean.getCosId() != -1) {
			selectionRequest.setCosID(reqBean.getCosId());
		}
		if (reqBean.getStatus() == 90) {
			selectionRequest.setCricketPack(reqBean.getFeedType());
		}

		selectionRequest.setSelectionType(reqBean.getSelType());
		if (reqBean.getStatus() == 99) {
			selectionRequest.setSelectionStartTime(reqBean.getStartTime());
			selectionRequest.setSelectionEndTime(reqBean.getEndTime());
			selectionRequest.setSelectionType(99);
		}
		// RBT-18793
		if (reqBean.getStatus() == 200 && reqBean.getEndTime()!=null ) {
			
			selectionRequest.setSelectionEndTime(reqBean.getEndTime());
			selectionRequest.setSelectionType(200);
		}

		// For RMO or Karoke
		Category category = RBTCacheManager.getInstance().getCategory(
				reqBean.getCategoryID());
		boolean isProfileRecordAllowed = false;
		if (category != null) {
			String catIDS = RBTParametersUtils.getParamAsString("COMMON",
					"CATGEORY_IDS_FOR_RECORD_TYPE_SEL", null);
			if (catIDS != null) {
				isProfileRecordAllowed = Arrays.asList(catIDS.split(","))
						.contains(reqBean.getCategoryID() + "");
			}
		}
		if (category != null
				&& (category.getCategoryTpe() == iRBTConstant.RECORD
						|| category.getCategoryTpe() == iRBTConstant.KARAOKE || isProfileRecordAllowed)) {
			selectionRequest.setClipID(reqBean.getWavFileName() + ".wav");
		}

		selectionRequest.setStatus(reqBean.getStatus());
		// selectionRequest.setProfileHours(reqBean.getProfileHrs());
		selectionRequest.setInterval(reqBean.getSelInterval());
		boolean isUdsOptinRequestAlso = false;
		String udsType=null;
		int fromTime[] = getTime(reqBean.getFromTime());
		int toTime[] = getTime(reqBean.getToTime());
		if (!(reqBean.getFromTime() == 0)) {
			selectionRequest.setFromTime(fromTime[0]);
			selectionRequest.setFromTimeMinutes(fromTime[1]);
		}
		if (!(reqBean.getToTime() == 2359)) {
			selectionRequest.setToTime(toTime[0]);
			selectionRequest.setToTimeMinutes(toTime[1]);
		}		
		if (xtraInfoMap.containsKey("TPCGID")) {
			tpcgID = xtraInfoMap.get("TPCGID");
		}
		if (tpcgID != null) {
			xtraInfoMap.put("TPCGID", tpcgID);
		}
		xtraInfoMap.put("ISDAEMON", "true");
		if (xtraInfoMap
				.containsKey(WebServiceConstants.param_allowPremiumContent)) {
			selectionRequest.setAllowPremiumContent(true);
			selectionRequest.setAllowDirectPremiumSelection(true); //RBT-13636:Vodafone In:-UDS subscriber not able to select Song
		}
		if (xtraInfoMap.containsKey(UDS_OPTIN)) {
			isUdsOptinRequestAlso = true;
			udsType=xtraInfoMap.get(UDS_OPTIN);
		}
		selectionRequest.setTpcgID(tpcgID);
		if (reqBean.getInLoop() != null)
			selectionRequest.setInLoop(reqBean.getInLoop()
					.equalsIgnoreCase("l"));
		
		String transId = reqBean.getTransId();
		if(actReqBean != null) {
			transId = actReqBean.getTransId();
		}
		boolean checkCGFlowForBSNL = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.DOUBLE_CONFIRMATION,
				"CG_INTEGRATION_FLOW_FOR_BSNL", "false");
		boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
				.getParamAsBoolean(COMMON,
						"ADD_BASE_CONSENT_ID_IN_EXTRA_INFO", "FALSE");
		if (checkCGFlowForBSNL || addBaseConsentInSelExtraInfo) {
			if(reqTransId != null) {
				xtraInfoMap.put("TRANS_ID", reqTransId);
			}
			else {
				xtraInfoMap.put("TRANS_ID", transId);
			}
		}
		if(xtraInfoMap.containsKey("slice_duration")){
			selectionRequest.setSlice_duration(xtraInfoMap.get("slice_duration"));
			xtraInfoMap.remove("slice_duration");
		}
		selectionRequest.setSelectionInfoMap(xtraInfoMap);
		selectionRequest.setMode(reqBean.getMode());
		selectionRequest.setModeInfo(reqBean.getSelectionInfo());
		if (reqBean.getUseUIChargeClass() != null)
			selectionRequest.setUseUIChargeClass(reqBean.getUseUIChargeClass()
					.equalsIgnoreCase("y"));

		// check if act request bean is not null
		if (actReqBean != null
				&& (!Utility.isUserActive(subscriber.getStatus()) || actReqBean
						.getRequestType().equalsIgnoreCase("UPGRADE"))) {
			selectionRequest.setActivationMode(actReqBean.getMode());
			//commented RBT-18219	Daemon is unable to make entry of subscriber in rbt_subscriber table
			//selectionRequest.setCircleID(actReqBean.getCircleId());
			// int cosId = actReqBean.getCosId();
			if (actReqBean.getCosId() != null && actReqBean.getCosId() != -1) {
				selectionRequest.setCosID(actReqBean.getCosId());
			}
			selectionRequest.setMode(actReqBean.getMode());
			selectionRequest.setModeInfo(actReqBean.getSelectionInfo());
			selectionRequest.setSubscriptionClass(actReqBean
					.getSubscriptionClass());
			int actPackCosId = actReqBean.getPackCosID();
			if (actPackCosId != -1 && actPackCosId != 0) {
				selectionRequest.setPackCosId(actReqBean.getPackCosID());
			}
			selectionRequest.setSubscriberEndDate(actReqBean.getEndTime());
			String actExtraInfo = actReqBean.getExtraInfo();
			HashMap<String, String> actExtraInfoMap = DBUtility
					.getAttributeMapFromXML(actExtraInfo);
			if (actExtraInfoMap != null) {
				if (actExtraInfoMap.containsKey("TPCGID")) {
					String actTpcgID = actExtraInfoMap.get("TPCGID");
					selectionRequest.setTpcgID(actTpcgID);
				}
				if (actExtraInfoMap.containsKey(UDS_OPTIN)) {
					isUdsOptinRequestAlso = true;
					udsType=actExtraInfoMap.get(UDS_OPTIN);
				}
			}
			if (checkCGFlowForBSNL || addBaseConsentInSelExtraInfo) {
				if(reqTransId != null) {
					actExtraInfoMap.put("TRANS_ID", reqTransId);
				}
				else {
					actExtraInfoMap.put("TRANS_ID", actReqBean.getTransId());
				}
			}
			if (actReqBean.getRequestType() != null
					&& actReqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
				actExtraInfoMap.put("UPGRADE_CONSENT", "true");
				if (actExtraInfoMap.containsKey("P2P_UPGRADE")) {
					actExtraInfoMap.put("P2P_UPGRADE", "true");
				}
			}
			actExtraInfoMap.put("ISDAEMON", "true");
			selectionRequest.setUserInfoMap(actExtraInfoMap);
			selectionRequest.setLanguage(actReqBean.getLanguage());
			selectionRequest.setRbtType(actReqBean.getRbtType());
			logger.info("Request is combo request, updated activation params in selection request."
					+ " selectionRequest: " + selectionRequest);
		}
		
		//Changes done for TTG-14814
		Rbt rbt = null;
		String reqType="Base";
		long intialTime=System.currentTimeMillis();
		long finalTime=0;
		if (reqBean.getRequestType().equalsIgnoreCase("SEL")) {
			rbt = RBTClient.getInstance().addSubscriberSelection(
					selectionRequest);
			finalTime=System.currentTimeMillis();
			reqType="Selection";
			
		} else if (reqBean.getRequestType().equalsIgnoreCase("DWN")){
			RBTClient.getInstance().addSubscriberDownload(selectionRequest);
			rbt = RBTClient.getInstance().addSubscriberSelection(
					selectionRequest);
			finalTime=System.currentTimeMillis();
			reqType="Download";
		}else {
			rbt = RBTClient.getInstance().addSubscriberSelection(
					selectionRequest);
			finalTime=System.currentTimeMillis();
			reqType="Base";
			// RBTClient.getInstance().addSubscriberDownload(selectionRequest);
		}
		long timeTaken=finalTime-intialTime;
		third_party_logger.info(timeTaken+writeLogger(rbt,reqType,reqBean!=null?reqBean.getTransId():null,actReqBean!=null?actReqBean.getTransId():null));
		//End of TTG-14814
		
		if(reqBean.getInlineFlag() != null)
			reqBean.setInlineFlag(null); //resetting inline flag to null after complete processing or 2?
		
		updateConsentStatus(reqBean);

		if (null != actReqBean) {
			updateConsentStatus(actReqBean);
		}

		response = selectionRequest.getResponse();
		logger.debug("response: " + response);
		logger.debug("RBT: " + rbt);
		if (rbt != null && rbt.getLibrary() != null) {
			logger.debug("RBT_LIBRARY: " + rbt.getLibrary());
			if (rbt.getLibrary().getSettings() != null) {
				logger.debug("RBT_SETTINGs: " + rbt.getLibrary().getSettings());
			} else {
				logger.debug("RBT Settings is null!");
			}
		} else {
			logger.debug("RBT Library is null!");
		}
		if (rbt != null && response.equals(WebServiceConstants.SUCCESS)
				&& rbt.getLibrary() != null
				&& rbt.getLibrary().getSettings() != null) {
			
			//Added for cut rbt logger 
			writeNOVALogForCUTRBT(rbt.getLibrary().getSettings());
			
			if (RBTParametersUtils.getParamAsBoolean("DAEMON",
					"ENABLE_AD2C_FEATURE", "FALSE")) {
				Settings settings = rbt.getLibrary().getSettings();
				String refId = null;
				for (Setting setting : settings.getSettings()) {
					if (setting.getIsCurrentSetting() != null
							&& setting.getIsCurrentSetting()) {
						refId = setting.getRefID();
						logger.debug("Selection refId: " + refId);
					}
				}
				if (refId != null) {
					boolean updateStatus = RBTCallBackEvent.update(
							reqBean.getSubscriberID(), reqBean.getTransId(),
							refId);
					if (updateStatus) {
						logger.debug("Selection refId: "
								+ refId
								+ " updated in RBT_CALL_BACK_EVENT for subscriberId: "
								+ reqBean.getSubscriberID() + ", refId: "
								+ refId);
					} else {
						logger.debug("Failed to update Selection refId: "
								+ refId
								+ " in RBT_CALL_BACK_EVENT for subscriberId: "
								+ reqBean.getSubscriberID() + ", refId: "
								+ refId);
					}

				} else {
					logger.debug("RefId Null");
				}
			}
		}

		if (isUdsOptinRequestAlso) {
			UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(null);
			updateRequest.setSubscriberID(actReqBean.getSubscriberID());
			updateRequest.setIsUdsOn(true);
			updateRequest.setUdsType(udsType);
			RBTClient.getInstance().setSubscriberDetails(updateRequest);
		}
		logger.info("Processed selection. returning response: " + response
				+ ", selectionRequest: " + selectionRequest);
		return response;
	}

	private void writeNOVALogForCUTRBT(Settings settings) {
		String refId = null;
		for (Setting setting : settings.getSettings()) {
			if (setting.getIsCurrentSetting() != null
					&& setting.getIsCurrentSetting()) {
				refId = setting.getRefID();
				logger.debug("Selection refId: " + refId);
				//Added for cur rbt logger 
				String rbtFile = setting.getRbtFile();
				logger.info("rbtFile"+rbtFile);
				if(refId != null && setting.getCutRBTStartTime()!=null){
					logger.info("rbtFile.contains _cut_");
					writeLoggerForCutRbtSelectionSuccess(setting);
				}
			}
		}
		
	}

	private void writeLoggerForCutRbtSelectionSuccess(Setting setting) {
		LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CUTRBT_LOGGER_DTO_BEAN);
		logger.info("loggerDTO"+loggerDTO);
		BasicCDRLogger selectionActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_ACT_CUTRBT_LOGGER_BEAN);
		logger.info(":---> Vikrant"+setting.toString());
		logger.info(":---> Vikrant"+setting.getStartTime());
		
		loggerDTO = selectionActLogger.getLoggerDTOForCutRbtSelACTSuccess(loggerDTO, setting);
	//	loggerDTO.setResponesStatus(response);
	    selectionActLogger.writeCDRLog(loggerDTO);
	}

	public static int[] getTime(int itime) {

		String strTime = itime + "";
		int[] times = new int[2];
		if (strTime.length() <= 2) {
			times[0] = 0;
			times[1] = Integer.parseInt(strTime);
			return times;
		}

		String time = strTime.substring(0, strTime.length() - 2);
		String mins = strTime.substring(strTime.length() - 2);

		if (time != null && time.trim().length() > 0)
			times[0] = Integer.parseInt(time);
		if (mins != null && mins.trim().length() > 0)
			times[1] = Integer.parseInt(mins);

		return times;
	}
	
	//Addded a new method to write the logs for TTG-14814
	public static String writeLogger(Rbt rbt, String reqType,String selConsentId,String comboConsentId) {
		StringBuffer sb = new StringBuffer();
		String mode = null;
		String refId = null, linkedRefId = null, consentRefId = selConsentId;
		if (rbt != null) {
			Subscriber sub = rbt.getSubscriber();
			Library library = rbt.getLibrary();
			Setting[] cnt = null;
			if ((mode == null || mode.isEmpty()) && sub != null) {
				mode = sub.getActivatedBy();
			}
			if (rbt.getLibrary() != null && library.getSettings() != null
					&& library.getSettings().getSettings() != null)
				cnt = library.getSettings().getSettings();
			if (sub != null && cnt != null) {
				if (reqType.equals("Selection")) {
					reqType = "Combo";
					consentRefId = comboConsentId;
				}
				if (null != sub) {
					linkedRefId = sub.getRefID();
				}
				if (null != cnt) {
					for (int i = 0; i < cnt.length; i++) {
						refId = cnt[i].getRefID();
					}
				}
			} else if (sub != null && cnt == null) {
				if (null != sub) {
					refId = sub.getRefID();
				}
			} else if (cnt != null) {
				for (int i = 0; i < cnt.length; i++) {
					refId = cnt[i].getRefID();
				}
			}
			
		}
		sb.append(", " + reqType).append(", " + refId)
		.append(", " + linkedRefId).append(", " + consentRefId)
		.append(", " + mode);
		return sb.toString();
	}
	//End of TTG-14814
}
