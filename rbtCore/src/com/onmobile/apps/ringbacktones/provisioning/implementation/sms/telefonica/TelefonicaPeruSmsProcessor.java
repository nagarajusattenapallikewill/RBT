package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.telefonica;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.DoubleConfirmationContentProcessUtils;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RecentSelection;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class TelefonicaPeruSmsProcessor extends TelefonicaSmsProcessor {

	private static final Logger logger = Logger
			.getLogger(TelefonicaPeruSmsProcessor.class);

	public TelefonicaPeruSmsProcessor() throws RBTException {
		super();
	}

	@Override
	public void processDoubleOptInConfirmation(Task task) {
		String smsText = null;
		String subscriberId = task.getString(param_subscriberID);
		Subscriber subscriber = getSubscriber(task);
		String language = null;

		if (subscriber != null)
			language = subscriber.getLanguage();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		List<DoubleConfirmationRequestBean> doubleConfimationList = RBTDBManager
				.getInstance()
				.getAllDoubleConfirmationRequestBeanForSATUpgrade(
						subscriberId,
						String.valueOf(WebServiceConstants.consent_pending_status),
						null);
		if (doubleConfimationList != null && doubleConfimationList.size() != 0) {
			doubleConfirmationRequestBean = doubleConfimationList.get(0);
			if (doubleConfimationList.size() > 1
					&& doubleConfirmationRequestBean.getRequestType().equalsIgnoreCase("SEL")
					&& (RBTDBManager.getInstance().getSubscriber(subscriberId) == null || (subscriber != null
							&& !subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)))) {
				doubleConfirmationRequestBean = doubleConfimationList.get(1);
			}
			if (doubleConfirmationRequestBean != null) {
				if (doubleConfirmationRequestBean.getRequestType()
						.equalsIgnoreCase("ACT")) {
					logger.debug("");
					String extraInfo = doubleConfirmationRequestBean
							.getExtraInfo();
					Map<String, String> extraInfoMap = null;
					if (extraInfo != null) {
						extraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
					}
					if (extraInfoMap != null
							&& extraInfoMap
									.containsKey(iRBTConstant.EXTRA_INFO_TRANS_ID)) {
						// combo request
						String selTransId = extraInfoMap
								.get(iRBTConstant.EXTRA_INFO_TRANS_ID);
						List<DoubleConfirmationRequestBean> selBeanList = RBTDBManager
								.getInstance()
								.getAllDoubleConfirmationRequestBeanForSATUpgrade(
										subscriberId,
										String.valueOf(WebServiceConstants.sel_combo_consent_pending_status),
										selTransId);
						DoubleConfirmationRequestBean selBean = selBeanList
								.get(0);
						String response = processSelections(selBean,
								doubleConfirmationRequestBean, null);
						if (response != null
								&& response.equalsIgnoreCase("success")) {
							// combo request success
							HashMap<String, String> hashMap = getSelSMSHashMap(
									task, language, selBean,
									doubleConfirmationRequestBean);
							subscriber = getSubscriber(task);
							hashMap.put("CIRCLE_ID", subscriber.getCircleID());
							smsText = finalizeSmsText(hashMap);
						} else {
							// combo request failure
							HashMap<String, String> hashMap = new HashMap<String, String>();
							hashMap.put(
									"SMS_TEXT",
									getSMSTextForID(task,
											DOUBLE_OPT_IN_SEL_FAILURE,
											m_doubleOptInSelFailed, language));
							hashMap.put("CIRCLE_ID", subscriber.getCircleID());
							smsText = finalizeSmsText(hashMap);
						}
					} else {
						// Activation request
						String response = processActivation(doubleConfirmationRequestBean);
						if (response != null
								&& response.equalsIgnoreCase("success")) {
							HashMap<String, String> hashMap = new HashMap<String, String>();
							hashMap.put(
									"SMS_TEXT",
									getSMSTextForID(task,
											DOUBLE_OPT_IN_ACT_SUCCESS,
											m_doubleOptInActSuccess, language));
							subscriber = getSubscriber(task);
							String subAmount = null;
							String subscriptionClass = doubleConfirmationRequestBean
									.getSubscriptionClass();
							if (subscriptionClass != null) {
								SubscriptionClass subClass = CacheManagerUtil
										.getSubscriptionClassCacheManager()
										.getSubscriptionClass(subscriptionClass);
								if (subClass != null) {
									subAmount = subClass
											.getSubscriptionAmount();
								}
							}
							hashMap.put("ACT_AMT", subAmount == null ? ""
									: subAmount);
							hashMap.put("CIRCLE_ID", subscriber.getCircleID());
							smsText = finalizeSmsText(hashMap);
						} else {
							HashMap<String, String> hashMap = new HashMap<String, String>();
							hashMap.put(
									"SMS_TEXT",
									getSMSTextForID(task,
											DOUBLE_OPT_IN_ACT_FAILURE,
											m_doubleOptInActFailed, language));
							hashMap.put("CIRCLE_ID", subscriber.getCircleID());
							smsText = finalizeSmsText(hashMap);
						}
					}
				} else {
					// selection request
					String response = processSelections(
							doubleConfirmationRequestBean, null, null);
					if (response != null
							&& response.equalsIgnoreCase("success")) {
						HashMap<String, String> hashMap = getSelSMSHashMap(
								task, language, doubleConfirmationRequestBean,
								null);
						smsText = finalizeSmsText(hashMap);
					} else {
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put(
								"SMS_TEXT",
								getSMSTextForID(task,
										DOUBLE_OPT_IN_SEL_FAILURE,
										m_doubleOptInSelFailed, language));
						hashMap.put("CIRCLE_ID", subscriber.getCircleID());
						smsText = finalizeSmsText(hashMap);
					}
				}
			}
		}
		if (smsText == null) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put(
					"SMS_TEXT",
					getSMSTextForID(task, DOUBLE_OPT_IN_NO_ENTRIES_FOUND,
							m_doubleOptInNoEntriesFound, language));
			hashMap.put("CIRCLE_ID", subscriber.getCircleID());
			smsText = finalizeSmsText(hashMap);
		}
		task.setObject(param_responseSms, smsText);
		logger.info("smsText: " + smsText);
	}

	private HashMap<String, String> getSelSMSHashMap(Task task,
			String language, DoubleConfirmationRequestBean selBean,
			DoubleConfirmationRequestBean actBean) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (actBean != null) {
			hashMap.put(
					"SMS_TEXT",
					getSMSTextForID(task, DOUBLE_OPT_IN_COMBO_SUCCESS,
							m_doubleOptInComboSuccess, language));
			String subAmount = null;
			String subscriptionClass = actBean.getSubscriptionClass();
			if (subscriptionClass != null) {
				SubscriptionClass subClass = CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(subscriptionClass);
				if (subClass != null) {
					subAmount = subClass.getSubscriptionAmount();
				}
			}
			hashMap.put("ACT_AMT", subAmount == null ? "" : subAmount);
		} else {
			hashMap.put(
					"SMS_TEXT",
					getSMSTextForID(task, DOUBLE_OPT_IN_SEL_SUCCESS,
							m_doubleOptInSelSuccess, language));
		}
		String wavFileName = selBean.getWavFileName();
		String clipName = null;
		String artist = null;
		String album = null;
		String promoId = null;
		if (wavFileName != null) {
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					wavFileName);
			if (clip != null) {
				clipName = clip.getClipName();
				artist = clip.getArtist();
				album = clip.getAlbum();
				promoId = clip.getClipPromoId();
			}
		}
		String categoryId = String.valueOf(selBean.getCategoryID()); 
		Category category = getCategory(categoryId);
		if (category != null && com.onmobile.apps.ringbacktones.webservice.common.Utility
				.isShuffleCategory(category.getCategoryTpe())) {
			clipName = category.getCategoryName(); 
			promoId = category.getCategoryPromoId();
		}
		String callerId = selBean.getCallerID();
		String classType = selBean.getClassType();
		String selAlmount = null;
		if (classType != null) {
			ChargeClass chargeClass = CacheManagerUtil
					.getChargeClassCacheManager().getChargeClass(classType);
			if (chargeClass != null) {
				selAlmount = chargeClass.getAmount();
			}
		}
		hashMap.put("SONG_NAME", clipName == null ? "" : clipName);
		hashMap.put("ARTIST", artist == null ? "" : artist);
		hashMap.put("ALBUM", album == null ? "" : album);
		hashMap.put("PROMO_ID", promoId == null ? "" : promoId);
		hashMap.put("CALLER_ID",
				callerId == null ? param(SMS, SMS_TEXT_FOR_ALL, "all")
						: callerId);
		hashMap.put("SEL_AMT", selAlmount == null ? "" : selAlmount);
		logger.debug(hashMap);
		return hashMap;
	}

	private String processActivation(DoubleConfirmationRequestBean reqBean) {
		String response = "failure";
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				reqBean.getSubscriberID());
		subscriptionRequest.setActivationMode(reqBean.getMode());
		subscriptionRequest.setCircleID(reqBean.getCircleId());
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
		String udsType = null;
		if (xtraInfoMap != null) {
			if (xtraInfoMap.containsKey("TPCGID")) {
				String tpcgID = xtraInfoMap.get("TPCGID");
				subscriptionRequest.setTpcgID(tpcgID);
			}
			if (xtraInfoMap.containsKey(UDS_OPTIN)) {
				isUdsOptinRequestAlso = true;
				udsType = xtraInfoMap.get(UDS_OPTIN);
			}
		}
		// this for normal user upgradation. RBT - 13221.
		if (reqBean.getRequestType() != null
				&& reqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
			subscriptionRequest.setRentalPack(subscriptionRequest
					.getSubscriptionClass());
			xtraInfoMap.put("UPGRADE_CONSENT", "true");
		}
		// this for normal user upgradation. RBT - 13221.
		boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
				.getParamAsBoolean(COMMON, "ADD_BASE_CONSENT_ID_IN_EXTRA_INFO",
						"FALSE");
		if (addBaseConsentInSelExtraInfo) {
			xtraInfoMap.put("TRANS_ID", reqBean.getTransId());
		}
		subscriptionRequest.setUserInfoMap(xtraInfoMap);
		subscriptionRequest.setLanguage(reqBean.getLanguage());
		subscriptionRequest.setRbtType(reqBean.getRbtType());
		subscriptionRequest.setTpcgID("DUMMY_TO_BE_REMOVED");
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
		deleteConsentEntries(reqBean);
		if (isUdsOptinRequestAlso) {
			UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(null);
			updateRequest.setSubscriberID(reqBean.getSubscriberID());
			updateRequest.setIsUdsOn(true);
			updateRequest.setUdsType(udsType);
			RBTClient.getInstance().setSubscriberDetails(updateRequest);
		}

		return response;
	}

	private String processSelections(DoubleConfirmationRequestBean reqBean,
			DoubleConfirmationRequestBean actReqBean, String tpcgID) {
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
		selectionRequest.setCircleID(reqBean.getCircleId());
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
		String udsType = null;
		int fromTime[] = DoubleConfirmationContentProcessUtils.getTime(reqBean
				.getFromTime());
		int toTime[] = DoubleConfirmationContentProcessUtils.getTime(reqBean
				.getToTime());
		if (!(reqBean.getFromTime() == 0)) {
			selectionRequest.setFromTime(fromTime[0]);
			selectionRequest.setFromTimeMinutes(fromTime[1]);
		}
		if (!(reqBean.getToTime() == 2359)) {
			selectionRequest.setToTime(toTime[0]);
			selectionRequest.setToTimeMinutes(toTime[1]);
		}
		/*
		 * if (xtraInfoMap.containsKey("TPCGID")) { tpcgID =
		 * xtraInfoMap.get("TPCGID"); } if (tpcgID != null) {
		 * xtraInfoMap.put("TPCGID", tpcgID); }
		 */
		if (xtraInfoMap
				.containsKey(WebServiceConstants.param_allowPremiumContent)) {
			selectionRequest.setAllowPremiumContent(true);
			selectionRequest.setAllowDirectPremiumSelection(true); // RBT-13636:Vodafone
			// In:-UDS
			// subscriber
			// not able
			// to select
			// Song
		}
		if (xtraInfoMap.containsKey(UDS_OPTIN)) {
			isUdsOptinRequestAlso = true;
			udsType = xtraInfoMap.get(UDS_OPTIN);
		}
		selectionRequest.setTpcgID(tpcgID);
		if (reqBean.getInLoop() != null) {
			selectionRequest.setInLoop(reqBean.getInLoop()
					.equalsIgnoreCase("l"));
		}

		String transId = reqBean.getTransId();
		if (actReqBean != null) {
			transId = actReqBean.getTransId();
		}
		boolean addBaseConsentInSelExtraInfo = RBTParametersUtils
				.getParamAsBoolean(COMMON, "ADD_BASE_CONSENT_ID_IN_EXTRA_INFO",
						"FALSE");
		if (addBaseConsentInSelExtraInfo) {
			xtraInfoMap.put("TRANS_ID", transId);
		}
		if (xtraInfoMap.containsKey("slice_duration")) {
			selectionRequest.setSlice_duration(xtraInfoMap
					.get("slice_duration"));
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
			selectionRequest.setCircleID(actReqBean.getCircleId());
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
					udsType = actExtraInfoMap.get(UDS_OPTIN);
				}
				actExtraInfoMap.remove(iRBTConstant.EXTRA_INFO_TRANS_ID);
			}
			if (actReqBean.getRequestType() != null
					&& actReqBean.getRequestType().equalsIgnoreCase("UPGRADE")) {
				actExtraInfoMap.put("UPGRADE_CONSENT", "true");
			}
			selectionRequest.setUserInfoMap(actExtraInfoMap);
			selectionRequest.setLanguage(actReqBean.getLanguage());
			selectionRequest.setRbtType(actReqBean.getRbtType());
			logger.info("Request is combo request, updated activation params in selection request."
					+ " selectionRequest: " + selectionRequest);
		}
		selectionRequest.setTpcgID("DUMMY_TO_BE_REMOVED");
		RBTClient.getInstance().addSubscriberSelection(selectionRequest);

		deleteConsentEntries(reqBean);

		response = selectionRequest.getResponse();
		logger.debug("response: " + response);

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

	private void deleteConsentEntries(DoubleConfirmationRequestBean reqBean) {
		boolean isDeleted = RBTDBManager.getInstance().deleteConsentRecord(
				null, null, null, null, reqBean.getSubscriberID(),false);
		logger.debug("Deleting consent entry. transId: " + reqBean.getTransId()
				+ ", subscriberId: " + reqBean.getSubscriberID()
				+ ", isDeleted: " + isDeleted);
	}

	@Override
	protected String getChargeClassFromSelections(Task task,
			String viralDataCallerID, Clip clip) {
		Rbt rbt = (Rbt) task.getObject(param_rbt_object);
		String amount = null;
		if (rbt != null && rbt.getLibrary() != null) {
			RecentSelection recentSelection = rbt.getLibrary()
					.getRecentSelection();
			logger.info("Recent Selection = " + recentSelection);
			if (recentSelection != null
					&& recentSelection.getClassType() != null) {
				String classType = recentSelection.getClassType();
				amount = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(classType).getAmount();
			}
		}
		logger.debug("selection amount: " + amount);
		return amount;
	}

}