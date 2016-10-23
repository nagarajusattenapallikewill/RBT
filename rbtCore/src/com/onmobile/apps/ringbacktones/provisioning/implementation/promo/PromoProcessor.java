/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.promo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.features.airtel.UserSelectionRestrictionBasedOnSubClass;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TransData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Processor class used to process all the third party requests
 * 
 * @author vinayasimha.patil
 * @modified Sreekar
 */
public class PromoProcessor extends Processor {

	/**
	 * @throws RBTException
	 */
	ArrayList<String> blockedChargeClasses = new ArrayList<String>();
	HashMap<String, String> etopUpSubClassMap = null;
	ArrayList<String> blockedModes = new ArrayList<String>();
	String defaultCategoryId = "6";
	public HashSet<String> confAzaanCosIdList = null;

	public PromoProcessor() throws RBTException {
		super();
		logger = Logger.getLogger(PromoProcessor.class);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#getTask(java.util
	 * .HashMap)
	 */
	public Task getTask(HashMap<String, String> requestParams) {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);

		Task task = new Task(null, taskSession);
		String taskAction = requestParams.get(param_REQUEST);
		task.setTaskAction(taskAction);
		logger.info("RBT:: task: " + task);
		return task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#validateParameters
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String validateParameters(Task task) {
		String response = "VALID";
		String taskAction = task.getTaskAction();
		task.setObject(param_subscriberID, task.getString(param_MSISDN));

		// Setting mode so that MNP happens properly RBT-5491
		if (task.containsKey(param_ACTIVATED_BY))
			task.setObject(param_mode, task.getString(param_ACTIVATED_BY));
		if (task.containsKey(param_SELECTED_BY))
			task.setObject(param_mode, task.getString(param_SELECTED_BY));
		// End of RBT-5491 changes

		Subscriber subscriber = getSubscriber(task);
		logger.info("RBT::task is " + task);
		// RBT-14301: Uninor MNP changes.
		String circleIdInReq = "";
		boolean validateMNPCheck = true;
		Map<String, String> circleIdMap = null;
		if (task.containsKey(param_CIRCLE_ID)) {
			// RBT-14301: Uninor MNP changes.
			validateMNPCheck = validateCircleIdParam(task, subscriber);
			if (task.containsKey(param_response)
					&& task.getString(param_response).equalsIgnoreCase(
							Resp_invalidParam)) {
				logger.info("RBT:: Invalid CIRCLE_ID For Subscriber. response: "
						+ "INVALID"
						+ " & param_response: "
						+ task.getString(param_response));
				response = "INVALID";
				return response;
			}
		}

		String callerID = task.getString(param_CALLER_ID);
		if (callerID != null && callerID.length() != 0
				&& !callerID.equalsIgnoreCase("null")) {
			boolean validCallerID = false;
			if (callerID.length() >= param(COMMON, MINIMUM_CALLER_ID_LENGTH, 7)) {
				try {
					Long.parseLong(callerID);
					validCallerID = true;
				} catch (NumberFormatException e) {
				}
			}

			if (!validCallerID) {
				response = "INVALID";
				task.setObject(param_response, Resp_invalidParam);
				logger.info("RBT:: Invalid CALLER_ID. response: " + response
						+ " & param_response: "
						+ task.getString(param_response));
				return response;
			}
		}
		// RBT-14185,RBT-14089- Vodafone In:-Only activation in promotion.jsp is
		// inserting
		// record into rbt_subscriber table DB directly
		if (subscriber != null
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.DEACTIVE)) {
			if (subscriber.getUserInfoMap() != null) {
				HashMap<String, String> userInfoMap = subscriber
						.getUserInfoMap();
				if (userInfoMap.containsKey(EXTRA_INFO_TPCGID)) {
					userInfoMap.remove(EXTRA_INFO_TPCGID);
				}
				if (userInfoMap.containsKey(EXTRA_INFO_TRANS_ID)) {
					userInfoMap.remove(EXTRA_INFO_TRANS_ID);
				}
				subscriber.setUserInfoMap(userInfoMap);
				task.setObject(param_subscriber, subscriber);
			}
		}// RBT-14301: Uninor MNP changes.
		if (validateMNPCheck && !subscriber.isValidPrefix()) {
			String paramURL = "rbt_promotion.jsp";
			if (task.containsKey(param_AD_RBT_REQUEST))
				paramURL = "rbt_ad_rbt2.jsp";
			task.setObject(param_URL, paramURL);
			String redirectRequested = task.getString(param_REDIRECT_NATIONAL);
			if (subscriber.getCircleID() == null || redirectRequested == null
					|| !redirectRequested.equalsIgnoreCase("TRUE")
					|| getRedirectionURL(task) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_InvalidPrefix);
			}
		} else if (taskAction == null) {
			response = "INVALID";
			task.setObject(param_response, Resp_missingParameter);
		} else if (taskAction.equalsIgnoreCase(request_block)
				|| taskAction.equalsIgnoreCase(request_unblock)) {
			// for Black Listing/Unblocking Request, parameter validation is
			// done in the process method
		} else if (!subscriber.isCanAllow()
				&& !(param(COMMON,
						"ALLOW_DEACTIVATION_FOR_BLACKLISTED_SUBSCRIBER", false) && taskAction
						.equalsIgnoreCase(action_deactivate))) {
			response = "INVALID";
			task.setObject(param_response, Resp_BlackListedNo);

		} else if (subscriber.getStatus().equalsIgnoreCase(
				WebServiceConstants.LOCKED)
				&& !taskAction.equalsIgnoreCase(request_deactivate)
				&& !taskAction.equalsIgnoreCase(request_hsb_deact)
				&& !taskAction.equalsIgnoreCase(request_hsb_dct)
				&& !taskAction.equalsIgnoreCase(request_hsb_can)) {
			response = "INVALID";
			task.setObject(param_response, Resp_userLocked);
		} else if (task.containsKey(param_AD_RBT_REQUEST)) {
			// for AD-RBT Request parameter validation is done in the process
			// method
		} else if (taskAction.equalsIgnoreCase(request_activate)
				&& task.containsKey(param_COSID)) {
			if (task.getString(param_mode) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_invalidParam);
			}
		} else if (taskAction.equalsIgnoreCase(request_activate)) {
			if (task.getString(param_ACTIVATED_BY) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		} else if (taskAction.equalsIgnoreCase(request_upgrade)) {
			if (task.getString(param_mode) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_invalidParam);
			}
		} else if (taskAction.equalsIgnoreCase(request_selection)) {
			if (task.getString(param_SELECTED_BY) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		} else if (taskAction.equalsIgnoreCase(request_deactivate)) {
			if (task.getString(param_DEACTIVATED_BY) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		} else if (taskAction.equalsIgnoreCase(request_cricket)) {
			if (task.getString(param_ACTIVATED_BY) == null
					|| task.getString(param_FEED_STATUS) == null) {
				response = "INVALID";
				task.setObject(param_response, Resp_missingParameter);
			}
		}
		// hsb check
		else if (taskAction.equalsIgnoreCase(request_hsb_act)) {
			if (!task.containsKey(param_ACTIVATED_BY))
				task.setObject(param_ACTIVATED_BY, "HSB");
			if (!task.containsKey(param_SUB_TYPE))
				task.setObject(param_SUB_TYPE, "PREPAID");
		} else if (taskAction.equalsIgnoreCase(request_hsb_deact)
				|| taskAction.equalsIgnoreCase(request_hsb_dct)
				|| taskAction.equalsIgnoreCase(request_hsb_can)) {
			if (!task.containsKey(param_DEACTIVATED_BY))
				task.setObject(param_DEACTIVATED_BY, "HSB");
		}
		// meri dhun check
		else if (taskAction.equalsIgnoreCase(action_meriDhun)) {
			if (!task.containsKey(param_PROMO_ID)
					|| !task.containsKey(param_PROMO_TEXT))
				response = "INVALID";
		} else if (taskAction.equalsIgnoreCase(action_delete_selection)) {
			if (!task.containsKey(param_MSISDN)
					|| (!task.containsKey(param_TONE_ID) && !task
							.containsKey(param_PROMO_ID)))
				response = "INVALID";
			else
				task.setObject(param_subscriberID, task.getString(param_MSISDN));
		}

		logger.info("RBT:: response: " + response + " & param_response: "
				+ task.getString(param_response));
		return response;
	}

	/**
	 * @author Sreekar
	 * @param task
	 * @param type
	 * @return Returns true of the trans ID exists with the type
	 */
	protected boolean checkTransID(Task task, String type) {
		if (task.containsKey(param_TRANSID)) {
			DataRequest dataRequest = new DataRequest(
					task.getString(param_TRANSID));
			dataRequest.setType(type);
			TransData transData = rbtClient.getTransData(dataRequest);
			if (transData != null) {
				task.setObject(param_response, Resp_invalidTransID);
				return true;
			} else {
				dataRequest.setSubscriberID(task.getString(param_subscriberID));
				rbtClient.addTransData(dataRequest);
			}
		}
		return false;
	}

	protected String canActivateSubscriber(Subscriber subscriber) {
		String status = subscriber.getStatus();
		if (status.equals(WebServiceConstants.NEW_USER)
				|| status.equals(WebServiceConstants.DEACTIVE))
			return null;
		if (status.equals(WebServiceConstants.ACTIVE))
			return Resp_AlreadyActive;
		else if (status.equals(WebServiceConstants.SUSPENDED))
			return Resp_AlreadyActive;
		else if (status.equals(WebServiceConstants.ACT_PENDING))
			return Resp_AlreadyActive;
		else if (status.equals(WebServiceConstants.DEACT_PENDING))
			return Resp_DeactPending;
		else if (status.equals(WebServiceConstants.GIFTING_PENDING))
			return Resp_giftPending;
		else if (status.equals(WebServiceConstants.RENEWAL_PENDING))
			return Resp_AlreadyActive;
		else if (status.equals(WebServiceConstants.BLACK_LISTED))
			return Resp_BlackListedNo;
		else if (status.equals(WebServiceConstants.INVALID_PREFIX))
			return Resp_InvalidPrefix;
		else if (status.equals(WebServiceConstants.COPY_PENDING))
			return Resp_CopyPending;
		else
			return Resp_Failure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#getRedirectionURL
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */

	public Subscriber processActivation(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String canActivate = canActivateSubscriber(subscriber);

		String activatedBy = task.getString(param_ACTIVATED_BY);
		String modesStr = RBTParametersUtils.getParamAsString(
				iRBTConstant.COMMON, "REACTIVATION_SUPPORTED_MODES", "");
		List<String> supportedModes = Arrays.asList(modesStr.split(","));
		boolean allowUser = supportedModes.contains(activatedBy);

		if (canActivate != null
				&& !task.containsKey(param_ADVANCE_RENTAL_CLASS) && !allowUser) {
			task.setObject(param_response, canActivate);
			return subscriber;
		}
		String ip = task.getString(param_ipAddress);
		String actInfo = "";
		String transType = "PROMO_ACT";
		if (task.containsKey(param_TRANS_TYPE))
			transType = task.getString(param_TRANS_TYPE);
		if (checkTransID(task, transType))
			return subscriber;
		if (task.containsKey(param_ACTIVATION_INFO)) {
			actInfo = task.getString(param_ACTIVATION_INFO);
			if (ip != null)
				actInfo = actInfo + ":" + ip;
		} else {
			if (ip != null)
				actInfo = actInfo + ip + ":";
			actInfo = actInfo + activatedBy;
			if (task.containsKey(param_TRANSID))
				actInfo = actInfo + ":" + task.getString(param_TRANSID);
			if (task.containsKey(param_RETAILER_ID))
				actInfo = actInfo + ":RET-" + task.getString(param_RETAILER_ID);
		}
		task.setObject(param_actby, activatedBy);
		task.setObject(param_actInfo, actInfo);

		// Added extra info column in the update to update the sr_id and
		// originator info
		// as per the jira id RBT-11962
		// Fix for RBT-12391,RBT-12394
		HashMap<String, String> map = subscriber.getUserInfoMap();
		if (map == null) {
			map = new HashMap<String, String>();
		} else {
			if (map.containsKey(param_SR_ID))
				map.remove(param_SR_ID);
			if (map.containsKey(param_ORIGINATOR))
				map.remove(param_ORIGINATOR);
		}
		if (task.containsKey(param_SR_ID))
			map.put("SR_ID", task.getString(param_SR_ID));
		if (task.containsKey(param_ORIGINATOR))
			map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
		task.setObject(param_userInfoMap, map);

		if (task.containsKey(param_ADVANCE_RENTAL_CLASS)) {
			String status = subscriber.getStatus();
			if (status.equals(WebServiceConstants.ACTIVE))
				return processUpgradeSubscription(task);
			else if (!canActivateUserWithStatus(status)) {
				task.setObject(param_response, Resp_AlreadyActive);
				return subscriber;
			}
			task.setObject(param_SUBSCRIPTION_CLASS,
					task.getString(param_ADVANCE_RENTAL_CLASS));
		}

		String subClass = task.getString(param_SUBSCRIPTION_CLASS);
		if (task.containsKey(param_END_DATE)) {
			Date end_date = null;
			try {
				end_date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				sdf.setLenient(false);
				end_date = sdf.parse(task.getString(param_END_DATE));
			} catch (Exception E) {
				System.out.println("Invalid Date Format");
				end_date = null;
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			if (end_date != null && end_date.after(cal.getTime()))
				subClass = "TNB";
		}
		if (subClass != null)
			task.setObject(param_subclass, subClass);

		task.setObject(param_isPrepaid, subscriber.isPrepaid());
		task.setObject(param_mode, task.getString(param_ACTIVATED_BY));
		if (task.getString(param_RBTTYPE) != null) {
			task.setObject(param_rbttype,
					Integer.parseInt(task.getString(param_RBTTYPE)));
		}

		subscriber = super.processActivation(task);
		setActivationResponse(task);
		logger.info("RBT:: response: " + task.getString(param_response));
		return subscriber;
	}

	protected void setActivationResponse(Task task) {
		String response = task.getString(param_response);
		if (response.equals("success"))
			response = Resp_Success;
		else if (response.equals(suspended))
			response = Resp_SuspendedNo;
		else if (response.equals(activationPending))
			response = Resp_ActPending;
		else if (response.equals(deactivationPending))
			response = Resp_DeactPending;
		else if (response.equals(giftingPending))
			response = Resp_giftActPending;
		else if (response.equals(renewalPending))
			response = Resp_RenewalPending;
		else if (response.equals(blackListedSMSText))
			response = Resp_BlackListedNo;
		else if (response.equals(invalidPrefix))
			response = Resp_InvalidPrefix;
		else if (response.equals(expressCopyPending))
			response = Resp_CopyPending;
		else if (response.equals(activationBlocked))
			response = Resp_ActivationBlocked;
		// RBT-13585
		else if (response.equals(technicalFailure))
			response = Resp_Err;
		else if (response
				.equalsIgnoreCase(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)) {
			response = Resp_TnbSongSelectionNotAllowed;
		} else if (response
				.equalsIgnoreCase(WebServiceConstants.UPGRADE_NOT_ALLOWED)) {
			response = Resp_UpgradeNotAllowed;
		}
		task.setObject(param_response, response);
	}

	/**
	 * Process' pack upgrade request for a user
	 * 
	 * @author Sreekar
	 * @param task
	 *            containing all the parameters for activating/upgrading a user
	 * @return
	 */
	public Subscriber processUpgradeSubscription(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		if (!status.equals(WebServiceConstants.ACTIVE)) {
			task.setObject(param_response, Resp_Inactive);
			return subscriber;
		}
		SubscriptionRequest request = new SubscriptionRequest(
				subscriber.getSubscriberID());
		request.setMode(task.getString(param_actby));
		request.setModeInfo(task.getString(param_actInfo));
		request.setRentalPack(task.getString(param_ADVANCE_RENTAL_CLASS));
		if (task.containsKey(param_rbttype)) {
			request.setRbtType(Integer.parseInt(task.getString(param_rbttype)));
		}
		// Added for CRM Request Jira Id RBT-11962
		// Fix for RBT-12391,RBT-12394
		HashMap<String, String> map = subscriber.getUserInfoMap();
		if (map == null) {
			map = new HashMap<String, String>();
		} else {
			if (map.containsKey(param_SR_ID))
				map.remove(param_SR_ID);
			if (map.containsKey(param_ORIGINATOR))
				map.remove(param_ORIGINATOR);
		}
		if (task.containsKey(param_SR_ID))
			map.put("SR_ID", task.getString(param_SR_ID));
		if (task.containsKey(param_ORIGINATOR))
			map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
		request.setUserInfoMap(map);
		subscriber = rbtClient.activateSubscriber(request);
		task.setObject(param_response, request.getResponse());
		setActivationResponse(task);
		return subscriber;
	}

	/*
	 * @Deepak Kumarfor Enabling Randomization RBT-6062
	 */
	public void enableRandomization(Task task) {

		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber != null && subscriber.isUdsOn()) {
			task.setObject(param_response, ALREADY_SHUFFLE_ACTIVATED);
			return;
		}
		String mode = task.getString(param_mode);
		SelectionRequest selectionRequest = new SelectionRequest(null);
		selectionRequest.setSubscriberID(subscriberID);
		selectionRequest.setMode(mode);
		rbtClient.shuffleDownloads(selectionRequest);
		String response = selectionRequest.getResponse();
		if (getParamAsString(SMS, "ENABLE_UDS_OPTIN_THROUGH_RANDOMIZATION",
				"FALSE").equalsIgnoreCase("TRUE")) {
			UpdateDetailsRequest updateRequest = new UpdateDetailsRequest(null);
			updateRequest.setSubscriberID(subscriberID);
			updateRequest.setIsUdsOn(true);
			rbtClient.setSubscriberDetails(updateRequest);
			response = updateRequest.getResponse();
		}
		if (response.equalsIgnoreCase(SUCCESS)
				|| response
						.equalsIgnoreCase(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS)) {
			task.setObject(param_response, SUCCESS);
			return;
		}

		task.setObject(param_response, FAILURE);
		return;

	}

	/*
	 * @Deepak Kumar For Disabling Randomization RBT-6062
	 */
	public void disableRandomization(Task task) {
		String subscriberID = task.getString(param_subscriberID);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (getParamAsString(SMS, "ENABLE_UDS_OPTIN_THROUGH_RANDOMIZATION",
				"FALSE").equalsIgnoreCase("TRUE")
				&& !subscriber.isUdsOn()) {
			task.setObject(param_response, ALREADY_SHUFFLE_DEACTIVATED);
			return;
		}
		String mode = task.getString(param_mode);
		SelectionRequest selectionRequest = new SelectionRequest(null);
		selectionRequest.setSubscriberID(subscriberID);
		selectionRequest.setMode(mode);
		rbtClient.disableRandomization(selectionRequest);
		if (selectionRequest.getResponse().equalsIgnoreCase(SUCCESS)) {
			task.setObject(param_response, SUCCESS);
			return;
		}

		task.setObject(param_response, FAILURE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processDeactivation
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String processDeactivation(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		if (canActivateUserWithStatus(status)) {
			task.setObject(param_response, Resp_AlreadyInactive);
			return Resp_AlreadyInactive;
		}

		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if (pendingStatus != null) {
			task.setObject(param_response, pendingStatus);
			return pendingStatus;
		}

		if (!param(COMMON, "ALLOW_DEACTIVATION_FOR_GRACE_USERS", true)
				&& subscriber.getStatus().equalsIgnoreCase(
						WebServiceConstants.GRACE)) {
			task.setObject(param_response, Resp_NotAllowedGraceUser);
			return Resp_NotAllowedGraceUser;
		}
		String ip = task.getString(param_ipAddress);
		String transID = task.getString(param_TRANSID);
		String deactivatedBy = task.getString(param_DEACTIVATED_BY);
		String transType = "PROMO_DCT";
		if (task.containsKey(param_TRANS_TYPE))
			transType = task.getString(param_TRANS_TYPE);
		if (checkTransID(task, transType))
			return task.getString(param_response);
		String deactInfo = "";
		if (task.containsKey(param_DEACTIVATION_INFO))
			deactInfo = task.getString(param_DEACTIVATION_INFO);
		else {
			if (ip != null)
				deactInfo = deactInfo + ip + ":";
			deactInfo = deactInfo + deactivatedBy;
			if (transID != null)
				deactInfo = deactInfo + ":" + transID;
		}

		task.setObject(param_actby, deactivatedBy);
		task.setObject(param_actInfo, deactInfo);
		task.setObject(param_isPrepaid, subscriber.isPrepaid());
		if (task.getString(param_RBTTYPE) != null) { // XXX newly added
			task.setObject(param_rbttype,
					Integer.parseInt(task.getString(param_RBTTYPE)));
		}

		// Added for CRM Request Jira Id RBT-11962
		// Fix for RBT-12391,RBT-12394
		HashMap<String, String> map = subscriber.getUserInfoMap();
		if (map == null) {
			map = new HashMap<String, String>();
		} else {
			if (map.containsKey(param_SR_ID))
				map.remove(param_SR_ID);
			if (map.containsKey(param_ORIGINATOR))
				map.remove(param_ORIGINATOR);
		}
		if (task.containsKey(param_SR_ID))
			map.put("SR_ID", task.getString(param_SR_ID));
		if (task.containsKey(param_ORIGINATOR))
			map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
		if (map.size() > 0) {
			task.setObject(param_userInfoMap, map);
		}

		if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| status.equalsIgnoreCase(WebServiceConstants.LOCKED)
				|| status.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
				|| status.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| status.equalsIgnoreCase(WebServiceConstants.SUSPENDED)
				|| status
						.equalsIgnoreCase(WebServiceConstants.ACTIVATION_SUSPENDED))
			super.processDeactivation(task);
		setDeactivationResponse(task);
		logger.info("RBT:: response: " + task.getString(param_response));
		return task.getString(param_response);
	}

	public String processDeactivationPack(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();

		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if (pendingStatus != null) {
			task.setObject(param_response, pendingStatus);
			return pendingStatus;
		}

		String cosIdStr = task.getString(param_COSID);
		if (cosIdStr == null)
			return Resp_MissingCosId;

		int cosId = -1;
		try {
			cosId = Integer.parseInt(cosIdStr);
		} catch (Exception e) {
			logger.info("Invalid cos id");
			return Resp_InvalidCosId;
		}

		if (cosId != -1) {
			String ip = task.getString(param_ipAddress);
			String deactivatedBy = task.getString(param_DEACTIVATED_BY);
			String deactInfo = "";
			if (task.containsKey(param_DEACTIVATION_INFO))
				deactInfo = task.getString(param_DEACTIVATION_INFO);
			else {
				if (ip != null)
					deactInfo = deactInfo + ip + ":";
				deactInfo = deactInfo + deactivatedBy;
			}

			task.setObject(param_actby, deactivatedBy);
			task.setObject(param_actInfo, deactInfo);
			task.setObject(param_isPrepaid, subscriber.isPrepaid());
			task.setObject(param_cosid, cosId);
			super.processDeactivationPack(task);
		}
		logger.info("RBT:: response: " + task.getString(param_response));
		return task.getString(param_response);
	}

	private void setDeactivationResponse(Task task) {
		String response = task.getString(param_response);
		if (response.equalsIgnoreCase("success"))
			response = Resp_Success;
		else if (response.equals(notActiveText))
			response = Resp_Inactive;
		else if (response.equals(activationPending))
			response = Resp_ActPending;
		else if (response.equals(deactivationPending))
			response = Resp_DeactPending;
		else if (response.equals(invalidPrefix))
			response = Resp_InvalidPrefix;
		else
			// if(response.equals(giftingPending) ||
			// response.equals(renewalPending) ||
			// response.equals(blackListedSMSText) ||
			// response.equals(expressCopyPending) ||
			// response.equals(HELP) || response.equals(technicalFailure))
			response = Resp_Failure;
	}

	protected void populateActivatedBySelectedBy(Task task) {
		if (!task.containsKey(param_ACTIVATED_BY)
				&& task.containsKey(param_SELECTED_BY))
			task.setObject(param_ACTIVATED_BY,
					task.getObject(param_SELECTED_BY));
		if (!task.containsKey(param_SELECTED_BY)
				&& task.containsKey(param_ACTIVATED_BY))
			task.setObject(param_SELECTED_BY,
					task.getObject(param_ACTIVATED_BY));
		if (!task.containsKey(param_ACTIVATION_INFO)
				&& task.containsKey(param_SELECTION_INFO))
			task.setObject(param_ACTIVATION_INFO,
					task.getObject(param_SELECTION_INFO));
		if (!task.containsKey(param_SELECTION_INFO)
				&& task.containsKey(param_ACTIVATION_INFO))
			task.setObject(param_SELECTION_INFO,
					task.getObject(param_ACTIVATION_INFO));

		if (!task.containsKey(param_SELECTED_BY))
			task.setObject(param_SELECTED_BY, "TNB");
		if (!task.containsKey(param_SELECTION_INFO)) {
			String selInfo = "";
			if (task.containsKey(param_ipAddress))
				selInfo = selInfo + task.getString(param_ipAddress) + ":";
			selInfo = selInfo + task.getString(param_SELECTED_BY);
			if (task.containsKey(param_TRANSID))
				selInfo = selInfo + ":" + task.getString(param_TRANSID);
			if (task.containsKey(param_RETAILER_ID))
				selInfo = selInfo + ":RET-" + task.getString(param_RETAILER_ID);
			task.setObject(param_SELECTION_INFO, selInfo);
		} else {

			String selInfo = task.getString(param_SELECTION_INFO);
			if (task.containsKey(param_ipAddress)) {
				selInfo = selInfo + ":" + task.getString(param_ipAddress);
				task.setObject(param_SELECTION_INFO, selInfo);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.provisioning.Processor#processSelection
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processSelection(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		task.setObject(param_actby, task.getString(param_SELECTED_BY));
		if (task.getString(param_RBTTYPE) != null) {
			task.setObject(param_rbttype,
					Integer.parseInt(task.getString(param_RBTTYPE)));
		}

		if (checkTransID(task, "PROMO_SEL"))
			return;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber.getUserType().equals(WebServiceConstants.CORPORATE)
				&& allowCorpAllSelChange()) {
			if (!(RBTParametersUtils.getParamAsBoolean("SMS",
					"CORP_ALLOWED_PROFILE_SELECTION", "FALSE") && task
					.containsKey(param_PROFILE_HOURS))) {
				task.setObject(param_response, Resp_InvalidNumber);
				return;
			}
		}

		SelectionRequest selectionRequest = new SelectionRequest(
				subscriber.getSubscriberID());
		populateActivatedBySelectedBy(task);
		String subClass = task.getString(param_SUBSCRIPTION_CLASS);
		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			task.setObject(param_response, Resp_Failure);
		else if (canActivateUserWithStatus(status)) {
			String isActRequest = task.getString(param_ISACTIVATE);
			if (isActRequest == null || !isActRequest.equalsIgnoreCase("TRUE")) {
				task.setObject(param_response, Resp_Failure);
				return;
			}

			String ip = task.getString(param_ipAddress);
			String activatedBy = task.getString(param_ACTIVATED_BY);
			String actInfo = "";
			String transType = "PROMO_ACT";
			if (task.containsKey(param_TRANS_TYPE))
				transType = task.getString(param_TRANS_TYPE);
			if (checkTransID(task, transType)) {
				task.setObject(param_actFailed, "true");
				return;
			}
			if (task.containsKey(param_ACTIVATION_INFO)) {
				actInfo = task.getString(param_ACTIVATION_INFO);
				if (ip != null)
					actInfo = actInfo + ":" + ip;
			} else {
				if (ip != null)
					actInfo = actInfo + ip + ":";
				actInfo = actInfo + activatedBy;
				if (task.containsKey(param_TRANSID))
					actInfo = actInfo + ":" + task.getString(param_TRANSID);
				if (task.containsKey(param_RETAILER_ID))
					actInfo = actInfo + ":RET-"
							+ task.getString(param_RETAILER_ID);
			}
				
			selectionRequest.setMode(checkCdtUserNoConsentModeMap(task,subscriber,activatedBy));
			selectionRequest.setModeInfo(actInfo);

			if (task.containsKey(param_ADVANCE_RENTAL_CLASS))
				task.setObject(param_SUBSCRIPTION_CLASS,
						task.getString(param_ADVANCE_RENTAL_CLASS));

			if (task.containsKey(param_END_DATE)) {
				Date end_date = null;
				try {
					end_date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					sdf.setLenient(false);
					end_date = sdf.parse(task.getString(param_END_DATE));
				} catch (Exception E) {
					System.out.println("Invalid Date Format");
					end_date = null;
				}
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);
				if (end_date != null && end_date.after(cal.getTime()))
					subClass = "TNB";
			}

			if (task.getObject(param_COSID) != null) {
				try {
					selectionRequest.setCosID(Integer.valueOf(task
							.getString(param_COSID)));
				} catch (Exception e) {
					if (subClass != null)
						selectionRequest.setSubscriptionClass(subClass);
				}
			} else {
				if (subClass != null)
					selectionRequest.setSubscriptionClass(subClass);
			}

			if (task.getObject(param_PACK_COSID) != null) {
				selectionRequest.setPackCosId(Integer.valueOf(task
						.getString(param_PACK_COSID)));
			}

			selectionRequest
					.setRbtType((Integer) task.getObject(param_rbttype));
			selectionRequest.setFreePeriod((Integer) task
					.getObject(param_freeperiod));
			// selectionRequest.setCircleID(subscriber.getCircleID());
			// RBT-14301: Uninor MNP changes.
			if (task.containsKey(param_CIRCLE_ID)
					&& !task.getString(param_CIRCLE_ID).trim().isEmpty()) {
				selectionRequest.setCircleID(task.getString(param_CIRCLE_ID));
			} else {
				selectionRequest.setCircleID(subscriber.getCircleID());
			}
			selectionRequest.setOperatorUserInfo(subscriber
					.getOperatorUserInfo());
			HashMap<String, String> map = selectionRequest.getUserInfoMap();
			if (map == null)
				map = new HashMap<String, String>();
			if (task.containsKey(EXTRA_INFO_OFFER_ID))
				map.put(EXTRA_INFO_OFFER_ID,
						task.getString(EXTRA_INFO_OFFER_ID));
			if (task.containsKey(param_CONSENT_LOG))
				map.put("CONSENT_LOG", task.getString(param_CONSENT_LOG));
			if (task.containsKey(param_REFUND)
					&& task.getString(param_REFUND).equalsIgnoreCase("true"))
				map.put("REFUND", "TRUE");
			if (task.containsKey(EXTRA_INFO_TPCGID)) {
				map.put(EXTRA_INFO_TPCGID, task.getString(EXTRA_INFO_TPCGID));
			}
			String udsOn = task.getString(UDS_ON);
			if (udsOn != null) {// JIRA-ID: RBT-13626
				map.put(UDS_OPTIN, udsOn);
			}

			//Added for RBT-17883
			if (task.containsKey(param_ChargeMDN)) {
				if (task.getString(param_ChargeMDN) != null
						&& !task.getString(param_ChargeMDN).isEmpty()) {
					map.put(EXTRA_INFO_CHARGE_MDN, task.getString(param_ChargeMDN));
				}
			}
			//Ended for RBT-17883

			selectionRequest.setUserInfoMap(map);

			// if(param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false))
			// {
			// Offer offer = getBaseOffer(task);
			// if(offer != null) {
			// selectionRequest.setSubscriptionOfferID(offer.getOfferID());
			// selectionRequest.setSubscriptionClass(offer.getSrvKey());
			// }
			// }
			task.setObject(param_actRequested, "true");
		} else if (task.containsKey(param_ADVANCE_RENTAL_CLASS)) {
			String isActRequest = task.getString(param_ISACTIVATE);
			if (isActRequest == null || !isActRequest.equalsIgnoreCase("TRUE")) {
				task.setObject(param_response, Resp_Failure);
				return;
			}
			task.setObject(param_actRequested, "true");
			subscriber = processActivation(task);
			if (!task.getString(param_response).equalsIgnoreCase(Resp_Success)) {
				task.setObject(param_actFailed, "true");
				return;
			}
		}

		Clip clip = (Clip) task.getObject(param_clip);
		Category category = (Category) task.getObject(param_category);
		String categoryId = null;
		if (clip == null) {
			String toneId = task.getString(param_TONE_ID);
			if (toneId != null && !toneId.equals("")) {
				clip = getClipById(toneId);
			}
			String promoId = task.getString(param_PROMO_ID);
			if (clip == null && promoId != null && !promoId.equals("")) {
				clip = getClipByPromoId(promoId);
				if (clip == null && category == null)
					category = getCategoryByPromoId(promoId);
			}
			String wavfile = task.getString(param_WAV_FILE);
			if (clip == null && wavfile != null && !wavfile.equals("")) {
				clip = getClipByWavFile(wavfile);
			}
			String smsAlias = task.getString(param_SMS_ALIAS);
			if (clip == null && smsAlias != null && !smsAlias.equals("")) {
				String smsAliasLower = smsAlias.toLowerCase();
				if (isSmsAliasConfigured(smsAliasLower)) {
					String alias = smsAliasLower
							+ subscriber.getCircleID().toLowerCase();
					clip = getClipByAlias(alias);
					logger.debug("Alias: " + alias + " Clip: " + clip);
				} else {
					clip = getClipByAlias(smsAliasLower);
					logger.debug("SmsAlias: " + smsAliasLower + " Clip: "
							+ clip);
				}
			}
			String profileHours = task.getString(param_PROFILE_HOURS);
			if (clip == null && profileHours != null
					&& !profileHours.equals("")) {
				clip = getProfileClip(task);
				task.setObject(param_CATEGORY_ID, "99");
			}
		}
		if (category == null) {
			categoryId = task.getString(param_CATEGORY_ID);
			if (category == null) {
				if (categoryId == null)
					categoryId = "6";
				category = getCategory(categoryId);
			}
		} else
			categoryId = category.getCategoryId() + "";

		if ((clip == null && category == null)
				|| (clip == null
						&& category != null
						&& category.getCategoryTpe() != iRBTConstant.SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.WEEKLY_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.MONTHLY_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.DAILY_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.DYNAMIC_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.ODA_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.FEED_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.BOX_OFFICE_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.FESTIVAL_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.MONTHLY_ODA_SHUFFLE
						&& category.getCategoryTpe() != iRBTConstant.OVERRIDE_MONTHLY_SHUFFLE && category
						.getCategoryTpe() != iRBTConstant.PLAYLIST_ODA_SHUFFLE)) {
			task.setObject(param_response, Resp_ClipNotAvailable);
			return;
		}
		if (category != null && clip != null) {
			String catChargeClass = category.getClassType();
			if (blockedChargeClasses.contains(catChargeClass.toUpperCase())) {
				String modeStr = task.getString(param_SELECTED_BY);
				if (modeStr != null
						&& blockedModes.contains(modeStr.toUpperCase().trim())) {
					boolean isMapped = rbtCacheManager.isClipMappedToCatgeory(
							clip.getClipId() + "", category.getCategoryId()
									+ "");
					if (!isMapped) {
						categoryId = defaultCategoryId;
						category = getCategory(categoryId);
					}
				} else {
					logger.info("Selection not allowed as mode does not have the privilege to use this charge class");
					task.setObject(param_response, Resp_invalidParam);
					return;
				}
			}
		}
		String inLoopStr = task.getString(param_IN_LOOP);
		boolean inLoop = false;
		if (inLoopStr != null && inLoopStr.equalsIgnoreCase("TRUE"))
			inLoop = true;
		String callerID = (String) task.getObject(param_CALLER_ID);
		if (callerID != null && !callerID.equals("")
				&& !callerID.equalsIgnoreCase("null"))
			selectionRequest.setCallerID(callerID);
		if (category != null) {
			selectionRequest.setCategoryID(categoryId);
			if (clip == null) {
				Clip[] clips = getClipsByCatId(category.getCategoryId());
				if (clips != null)
					clip = clips[0];
			}
		}
		if (clip != null) {
			selectionRequest.setClipID("" + clip.getClipId());
			task.setObject(CLIP_OBJ, clip);
		}

		if (task.getObject(param_COSID) != null) {
			try {
				selectionRequest.setCosID(Integer.valueOf(task
						.getString(param_COSID)));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (subClass != null)
			selectionRequest.setSubscriptionClass(subClass);

		selectionRequest.setInLoop(inLoop);
		selectionRequest.setIsPrepaid(subscriber.isPrepaid());
		String activatedBy = task.getString(param_SELECTED_BY);
		selectionRequest.setMode(checkCdtUserNoConsentModeMap(task,subscriber, activatedBy));
		selectionRequest.setModeInfo(task.getString(param_SELECTION_INFO));

		if (selectionRequest.getSubscriptionClass() == null)
			selectionRequest.setSubscriptionClass(task
					.getString(param_SUBSCRIPTION_CLASS));

		HashMap<String, String> map = new HashMap<String, String>();
		if (task.containsKey(param_REFUND)
				&& task.getString(param_REFUND).equalsIgnoreCase("true"))
			map.put("REFUND", "TRUE");
		if (task.containsKey(param_CONSENT_LOG))
			map.put("CONSENT_LOG", task.getString(param_CONSENT_LOG));
		if (task.containsKey(EXTRA_INFO_TPCGID)) {
			map.put(EXTRA_INFO_TPCGID, task.getString(EXTRA_INFO_TPCGID));
		}
		// Added for CRM Request Jira Id RBT-11962
		if (task.containsKey(param_SR_ID))
			map.put("SR_ID", task.getString(param_SR_ID));
		if (task.containsKey(param_ORIGINATOR))
			map.put("ORIGINATOR", task.getString(param_ORIGINATOR));

		if (task.containsKey(CAMPAIGN_CODE)) {
			map.put(CAMPAIGN_CODE, task.getString(CAMPAIGN_CODE));
		}
		if (task.containsKey(TREATMENT_CODE)) {
			map.put(TREATMENT_CODE, task.getString(TREATMENT_CODE));
		}
		if (task.containsKey(OFFER_CODE)) {
			map.put(OFFER_CODE, task.getString(OFFER_CODE));
		}
		if (task.containsKey(UDS_ON)) {
			map.put(UDS_OPTIN, task.getString(UDS_ON));
		}

		//Added for RBT-17883
		if (task.containsKey(param_ChargeMDN)) {
			if (task.getString(param_ChargeMDN) != null
					&& !task.getString(param_ChargeMDN).isEmpty()) {
				map.put(EXTRA_INFO_CHARGE_MDN, task.getString(param_ChargeMDN));
			}
		}
		//Ended for RBT-17883

		selectionRequest.setSelectionInfoMap(map);

		task.setObject(param_clipid, selectionRequest.getClipID());
		task.setObject(param_catid, selectionRequest.getCategoryID());

		if (task.containsKey(param_CHARGE_CLASS))
			selectionRequest.setChargeClass(task.getString(param_CHARGE_CLASS));
		if (task.containsKey(param_USE_UI_CHARGE_CLASS)
				&& task.getString(param_USE_UI_CHARGE_CLASS).equalsIgnoreCase(
						"TRUE"))
			selectionRequest.setUseUIChargeClass(true);

		if (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false)
				|| param(COMMON, iRBTConstant.ALLOW_ONLY_BASE_OFFER, false)) {

			if (canActivateUserWithStatus(status)) {
				Offer offer = getBaseOffer(task);
				if (offer != null) {
					selectionRequest.setSubscriptionOfferID(offer.getOfferID());
					selectionRequest.setSubscriptionClass(offer.getSrvKey());
				}
			}
		}

		if (param(COMMON, iRBTConstant.ALLOW_GET_OFFER, false)) {
			Offer offer = getSelOffer(task);
			if (offer != null) {
				selectionRequest.setOfferID(offer.getOfferID());
				selectionRequest.setChargeClass(offer.getSrvKey());
				if (offer.getSmOfferType() != null) {
					HashMap<String, String> userInfoMap = selectionRequest
							.getUserInfoMap();
					if (userInfoMap == null)
						userInfoMap = new HashMap<String, String>();
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_TYPE,
							offer.getSmOfferType());
					selectionRequest.setSelectionInfoMap(userInfoMap);
				}
			}
		}

		//Added for RBT-17883
		if (task.containsKey(param_ChargeMDN)) {
			if (task.getString(param_ChargeMDN) != null
					&& !task.getString(param_ChargeMDN).isEmpty()) {
				HashMap<String, String> userInfoMap = selectionRequest
						.getUserInfoMap();
				if (userInfoMap == null)
					userInfoMap = new HashMap<String, String>();
				userInfoMap.put(iRBTConstant.EXTRA_INFO_CHARGE_MDN,
						task.getString(param_ChargeMDN));
				selectionRequest.setSelectionInfoMap(userInfoMap);
			}
		}
		if (task.containsKey(param_SelType)) {
			try{
			selectionRequest.setSelectionType(Integer.parseInt(task
					.getString(param_SelType)));
			}
			catch(NumberFormatException ne){
				ne.printStackTrace();
			}
		}
		//Ended for RBT-17883

		/*
		 * added by SenthilRaja. To add the from time and end time parameter.
		 */
		int fromHrs = 0;
		int toHrs = 23;
		int fromMinutes = 0;
		int toMinutes = 59;
		try {
			if (task.containsKey(param_FROMTIME)) {
				fromHrs = Integer.parseInt(task.getString(param_FROMTIME));
				selectionRequest.setFromTime(fromHrs);
			}
			if (task.containsKey(param_TOTIME)) {
				toHrs = Integer.parseInt(task.getString(param_TOTIME));
				selectionRequest.setToTime(toHrs);
			}
			if (task.containsKey(WebServiceConstants.param_TOTIMEMINUTES)) {
				toMinutes = Integer.parseInt(task
						.getString(WebServiceConstants.param_TOTIMEMINUTES));
				selectionRequest.setToTimeMinutes(toMinutes);
			}
			if (task.containsKey(WebServiceConstants.param_FROMTIMEMINUTES)) {
				fromMinutes = Integer.parseInt(task
						.getString(WebServiceConstants.param_FROMTIMEMINUTES));
				selectionRequest.setFromTimeMinutes(fromMinutes);
			}

			if (fromHrs < 0 || fromHrs > 23 || toHrs < 0 || toHrs > 23
					|| fromMinutes < 0 || fromMinutes > 59 || toMinutes < 0
					|| toMinutes > 59) {
				logger.error("RBT:: Invalid fromTime or toTime. Returning response: "
						+ WebServiceConstants.INVALID_PARAMETER);
				task.setObject(param_response, Resp_Failure);
				return;
			}

			if (task.containsKey(param_PROFILE_HOURS))
				selectionRequest.setProfileHours(task
						.getString(param_PROFILE_HOURS));

		} catch (NumberFormatException nme) {
			logger.error("RBT:: Invalid fromTime or toTime. Returning response: "
					+ WebServiceConstants.INVALID_PARAMETER);
			task.setObject(param_response, Resp_Failure);
			return;
		}

		if (task.containsKey(param_INTERVAL)) {
			String interval = task.getString(param_INTERVAL);
			if (!com.onmobile.apps.ringbacktones.webservice.common.Utility
					.isValidSelectionInterval(interval)) {
				logger.error("RBT:: Invalid interval. Returning response: "
						+ WebServiceConstants.INVALID_PARAMETER);
				task.setObject(param_response, Resp_Failure);
				return;
			}
			selectionRequest.setInterval(interval);
		}
		if (task.getString(param_RBTTYPE) != null) {
			task.setObject(param_rbttype,
					Integer.parseInt(task.getString(param_RBTTYPE)));
		}

		String allowPremiumContent = task.getString("ALLOW_PREMIUM_CONTENT");
		if (allowPremiumContent != null
				&& allowPremiumContent.equalsIgnoreCase("TRUE")) {
			selectionRequest.setAllowPremiumContent(true);
			selectionRequest.setAllowDirectPremiumSelection(true);
		}

		if (task.containsKey(WebServiceConstants.param_isPreConsentBaseSelRequest)) {
			selectionRequest.setConsentInd(true);
			Rbt rbt = rbtClient.addSubscriberConsentSelection(selectionRequest);
			if (rbt != null)
				task.setObject("consentObj", rbt.getConsent());

			logger.info("Consent Object = " + rbt.getConsent());
			if (rbt != null && rbt.getConsent() == null) {
				task.setObject(WebServiceConstants.param_byPassConsent, "true");
			}
		} else {
			rbtClient.addSubscriberSelection(selectionRequest);
		}
		task.remove(param_library);
		setSelectionResponse(selectionRequest.getResponse(), task, subscriber);
	}

	protected Clip getProfileClip(Task task) {
		Clip profileClip = null;
		String profileName = task.getString(param_PROFILE_NAME);
		Clip[] clips = rbtCacheManager.getClipsInCategory(99);
		if (clips == null || clips.length <= 0)
			return profileClip;

		String contentId = null;
		if (profileName != null)
			contentId = profileName;
		else
			return profileClip;
		String origLanguage = task.getString(param_language);
		String defaultLang = "eng";
		for (int i = 0; i < clips.length; i++) {
			if (clips[i].getClipRbtWavFile() != null
					&& (clips[i].getClipRbtWavFile().indexOf(
							"_" + origLanguage + "_") != -1
							|| (clips[i].getShortLanguage() != null && clips[i]
									.getShortLanguage().equals(origLanguage)) || (clips[i]
							.getLanguage() != null && clips[i].getLanguage()
							.equals(origLanguage)))) {
				if (clips[i].getClipSmsAlias() != null) {
					if (tokenizeArrayList(
							clips[i].getClipSmsAlias().toLowerCase(), ",")
							.contains(contentId.toLowerCase()))
						profileClip = clips[i];
				}
			}
		}

		if (profileClip == null) {
			logger.info("Since profile clip is not found, searching based on "
					+ "clipSmsAlias. clips length: " + clips.length);
			for (int i = 0; i < clips.length; i++) {
				if ((clips[i].getShortLanguage() != null && clips[i]
						.getShortLanguage().equals(defaultLang))
						|| (clips[i].getClipRbtWavFile() != null && (clips[i]
								.getClipRbtWavFile().indexOf(
										"_" + defaultLang + "_") != -1))) {
					if (clips[i].getClipSmsAlias() != null
							&& tokenizeArrayList(
									clips[i].getClipSmsAlias().toLowerCase(),
									",").contains(contentId.toLowerCase())) {
						profileClip = clips[i];
						break;
					}
				}
			}
		}

		if (profileClip == null) {
			logger.info("Returning null, profile clip is not found.");
			return null;
		}

		// String language = task.getString(param_language);
		String profileWavFile = Utility.findNReplaceAll(
				profileClip.getClipRbtWavFile(), "_eng_", "_" + origLanguage
						+ "_");
		Clip requestedProfileClip = getClipByWavFile(profileWavFile,
				origLanguage);
		if (requestedProfileClip == null)
			requestedProfileClip = profileClip;

		logger.info("Profile clip for the profileName : " + profileName
				+ " is : " + requestedProfileClip);
		return requestedProfileClip;
	}

	private void setSelectionResponse(String response, Task task,
			Subscriber subscriber) {
		logger.info("RBT::setting selection response-" + response
				+ "::taskSession-" + task.getTaskSession());
		String finalResp = Resp_Success;
		if (response.equals(WebServiceConstants.CLIP_NOT_EXISTS))
			finalResp = Resp_ClipNotAvailable;
		else if (response.equals(WebServiceConstants.CLIP_EXPIRED))
			finalResp = Resp_ClipExpired;
		else if (response.equals(WebServiceConstants.ALREADY_EXISTS))
			finalResp = Resp_songSetSelectionAlreadyExists;
		else if (response.equals(WebServiceConstants.OFFER_NOT_FOUND))
			finalResp = Resp_offerNotFound;
		else if (response.equals(WebServiceConstants.SUCCESS_DOWNLOAD_EXISTS))
			finalResp = Resp_songSetDownloadAlreadyExists;
		else if (response.equals(WebServiceConstants.TECHNICAL_DIFFICULTIES))
			finalResp = Resp_Err;
		else if (response.equals(WebServiceConstants.ERROR))
			finalResp = Resp_Err;
		else if (response.equals(WebServiceConstants.SELECTION_OVERLIMIT)
				|| response
						.equals(WebServiceConstants.LOOP_SELECTION_OVERLIMIT))
			finalResp = Resp_selectionLimit;
		else if (response
				.equals(WebServiceConstants.PERSONAL_SELECTION_OVERLIMIT))
			finalResp = Resp_maxCallerSel;
		else if (response.equals(WebServiceConstants.LITE_USER_PREMIUM_BLOCKED))
			finalResp = Resp_liteUserPremiumBlocked;
		else if (response
				.equals(WebServiceConstants.LITE_USER_PREMIUM_CONTENT_NOT_PROCESSED))
			finalResp = Resp_liteUserPremiumNotProcessed;
		else if (response
				.contains(WebServiceConstants.COS_MISMATCH_CONTENT_BLOCKED))
			finalResp = response.toUpperCase();
		else if (response.equals(WebServiceConstants.OVERLIMIT))
			finalResp = Resp_downloadOverlimit;
		else if (response.equals(WebServiceConstants.ACTIVATION_BLOCKED))
			finalResp = Resp_ActivationBlocked;
		// RBT-13585
		else if (response
				.equalsIgnoreCase(WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)) {
			finalResp = Resp_TnbSongSelectionNotAllowed;
		} else if (response
				.equalsIgnoreCase(WebServiceConstants.UPGRADE_NOT_ALLOWED)) {
			finalResp = Resp_UpgradeNotAllowed;
		}

		// RBT-12419
		else if (response
				.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED_DOWNLOAD_DELETED))
			finalResp = Constants.Resp_ClipExpiredDwnDeleted;
		else if (response
				.equalsIgnoreCase(WebServiceConstants.CATEGORY_EXPIRED_DOWNLOAD_DELETED))
			finalResp = Constants.Resp_CatExpiredDwnDeleted;
		else if (response
				.equalsIgnoreCase(WebServiceConstants.CATEGORY_EXPIRED))
			finalResp = Constants.Resp_CatExpired;
		
		else if (response
				.equalsIgnoreCase(WebServiceConstants.RBT_CORPORATE_NOTALLOW_SELECTION))
			finalResp = Constants.Corporate_Selection_Not_Allowed;
		//RBT-18975
		else if (response
				.equalsIgnoreCase(WebServiceConstants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE))
			finalResp = Constants.SELECTION_NOT_ALLOWED_FOR_USER_ON_BLOCKED_SERVICE;
		else if (response
				.equalsIgnoreCase(WebServiceConstants.DOWNLOAD_MONTHLY_LIMIT_REACHED))
			finalResp = Constants.Download_monthly_limit_reached;
		else if (!response.contains(WebServiceConstants.SUCCESS)) {
			String subscriberID = task.getString(param_subscriberID);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriberID);
			rbtDetailsRequest.setMode(task.getString(param_mode));
			Subscriber latestSubObj = rbtClient
					.getSubscriber(rbtDetailsRequest);
			if (subscriber.getStatus() != latestSubObj.getStatus()) {
				if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACTIVE))
					finalResp = Corporate_Selection_Not_Allowed;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.SUSPENDED))
					finalResp = Resp_SuspendedNo;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.ACT_PENDING))
					finalResp = Resp_ActPending;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.DEACT_PENDING))
					finalResp = Resp_DeactPending;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.GIFTING_PENDING))
					finalResp = Resp_giftActPending;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.RENEWAL_PENDING))
					finalResp = Resp_RenewalPending;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.BLACK_LISTED))
					finalResp = Resp_BlackListedNo;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.INVALID_PREFIX))
					finalResp = Resp_InvalidPrefix;
				else if (latestSubObj.getStatus().equalsIgnoreCase(
						WebServiceConstants.COPY_PENDING))
					finalResp = Resp_CopyPending;
				else
					finalResp = Resp_Failure;
			} else
				finalResp = Resp_Failure;
		}
		task.setObject(param_response, finalResp);
	}

	public void processDeleteSelection(Task task) {
		SelectionRequest selection = new SelectionRequest(
				task.getString(param_subscriberID));
		String callerID = task.getString(param_CALLER_ID);
		if (callerID != null)
			selection.setCallerID(callerID);
		else
			selection.setCallerID("all");
		String mode = task.getString(param_DESELECTED_BY);
		selection.setMode(mode);
		selection.setModeInfo(mode + ":" + task.getString(param_ipAddress));
		// Added for CRM Request Jira Id RBT-11962
		// Fix for RBT-12391,RBT-12394
		HashMap<String, String> map = selection.getUserInfoMap();
		if (map == null) {
			map = new HashMap<String, String>();
		} else {
			if (map.containsKey(param_SR_ID))
				map.remove(param_SR_ID);
			if (map.containsKey(param_ORIGINATOR))
				map.remove(param_ORIGINATOR);
		}
		if (task.containsKey(param_SR_ID))
			map.put("SR_ID", task.getString(param_SR_ID));
		if (task.containsKey(param_ORIGINATOR))
			map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
		selection.setUserInfoMap(map);
		if (task.containsKey(param_TONE_ID))
			selection.setClipID(task.getString(param_TONE_ID));
		else if (task.containsKey(param_PROMO_ID)) {
			String promoID = task.getString(param_PROMO_ID);
			if (promoID != null && promoID.startsWith("S")) {
				promoID = promoID.substring(1);
			}
			Clip clip = rbtCacheManager.getClipByPromoId(promoID);
			Category category = null;
			if (clip == null) {
				category = rbtCacheManager.getCategoryByPromoId(promoID);
			}
			if (clip != null) {
				selection.setClipID(clip.getClipId() + "");
			} else if (category != null) {
				selection.setCategoryID(category.getCategoryId() + "");
			}
		}
		rbtClient.deleteSubscriberSelection(selection);
		task.remove(param_library);
		setDeleteSelectionResponse(selection.getResponse(), task);
	}

	private void setDeleteSelectionResponse(String response, Task task) {
		String finalResponse = response;
		if (response.equals(WebServiceConstants.FAILED))
			finalResponse = Resp_SelNotExists;
		else if (response.equals(WebServiceConstants.SUCCESS))
			finalResponse = Resp_Success;
		task.setObject(param_response, finalResponse);
	}

	/*
	 * public void processCheck(Task task) { Subscriber subscriber =
	 * (Subscriber) task.getObject(param_subscriber); String status =
	 * subscriber.getStatus(); if
	 * (!status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING) &&
	 * status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
	 * task.setObject(param_response, Resp_Eligible); else
	 * task.setObject(param_response, Resp_NonEligible);
	 * logger.info("RBT:: response: " + task.getString(param_response)); }
	 * 
	 * public void processStatus(Task task) { Subscriber subscriber =
	 * (Subscriber) task.getObject(param_subscriber); String status =
	 * subscriber.getStatus(); if
	 * (!status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING) &&
	 * status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) { if
	 * (subscriber.isPrepaid()) task.setObject(param_response,
	 * resp_ActivePrepaid); else task.setObject(param_response,
	 * resp_ActivePostpaid); } else task.setObject(param_response,
	 * Resp_Inactive); logger.info("RBT:: response: " +
	 * task.getString(param_response)); }
	 */

	/**
	 * Returns the eligible/not-eligible response for the "check" query
	 * 
	 * @author Sreekar
	 */
	public void processSubProfileRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		long time = (System.currentTimeMillis() - subscriber.getEndDate()
				.getTime()) / (1000 * 60 * 60 * 24);
		if (time <= 30)
			task.setObject(param_response, Resp_Eligible);
		else
			task.setObject(param_response, Resp_NonEligible);
	}

	@Override
	/**
	 * Returns if subscriber is active or inactive.
	 * 
	 * @author Sreekar
	 */
	public void processSubStatusRequest(Task task) {
		/*
		 * Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		 * String status = subscriber.getStatus();
		 * logger.info("RBT::subscriber-" + subscriber);
		 * if(!(isSubActive(subscriber) ||
		 * status.equals(WebServiceConstants.SUSPENDED) ||
		 * status.equals(WebServiceConstants.GRACE)))
		 * task.setObject(param_response, Resp_Inactive); else {
		 * if(subscriber.isPrepaid()) task.setObject(param_response,
		 * resp_ActivePrepaid); else task.setObject(param_response,
		 * resp_ActivePostpaid); }
		 */
		
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		
		String status = null;
		if (subscriber == null) {
			status = "SUBSCRIBER_NOT_EXIST";
		} else {
			if (subscriber.isValidPrefix()) {
				status = subscriber.getStatus().toUpperCase();
				if (status.equals("ACTIVE")) {
					SubscriptionClass subClass = CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.getSubscriptionClass(
									subscriber.getSubscriptionClass());
					if (subClass != null && subClass.getOperatorCode2() != null) {
						String opCode = subClass.getOperatorCode2();
						String[] tokens = opCode.split(",");
						Clip clip = getClipById(tokens[0].trim());
						if (clip != null) {
							status = "COMBO_ACTIVE_"
									+ subscriber.getSubscriptionClass();
						}
					}

					String subClasses = getParamAsString(COMMON,
							"BLOCKED_SUBSCRIPTION_CLASS", null);
					if (subClasses != null) {
						List blockedSubClassesList = Arrays.asList(subClasses
								.split(","));
						if (blockedSubClassesList != null
								&& blockedSubClassesList.contains(subscriber
										.getSubscriptionClass())) {
							status = "ACTIVE_USER_IN_BLOCKED_SERVICE";
						}
					}
				}
			} else
				status = "INVALID";
		}
		logger.info("RBT::subscriber status is : " + status);
		task.setObject(param_response,
				getParamAsString(PROMOTION, status, status));
	}

	@Override
	public void processViralData(Task task) {
	}

	private boolean isSubActive(Subscriber subscriber) {
		if (subscriber == null)
			return false;
		String status = subscriber.getStatus();
		if (status.equals(WebServiceConstants.ACTIVE)
				|| status.equals(WebServiceConstants.ACT_PENDING))
			return true;
		return false;
	}

	private boolean allowCorpAllSelChange() {
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter(SMS, CORP_CHANGE_SELECTION_ALL_BLOCK);
		return (param != null && param.getValue().equalsIgnoreCase("true"));
	}

	private boolean allowFeedUpgrade() {
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter(COMMON, "ALLOW_FEED_UPGRADE");
		return (param != null && param.getValue().equalsIgnoreCase("true"));
	}

	private boolean allowCricketPass() {
		return true;
	}

	@Override
	/**
	 * Process the cricket request from the third parties
	 * 
	 * @author Sreekar
	 * 
	 */
	public void processFeed(Task task) {
		String feedStatus = task.getString(param_FEED_STATUS);
		logger.info("Processing cricket selection. feedStatus: " + feedStatus);
		if (feedStatus == null
				|| (!feedStatus.equalsIgnoreCase("on") && !feedStatus
						.equalsIgnoreCase("off"))) {
			task.setObject(param_invalidParam, param_FEED_STATUS);
			task.setObject(param_response, Resp_missingParameter);
			logger.warn("Unable to processing cricket request, Invalid feedstatus: "
					+ feedStatus);
			return;
		}
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber.getUserType().equals(WebServiceConstants.CORPORATE)
				&& !allowCorpAllSelChange()) {
			task.setObject(param_response, Resp_InvalidNumber);
			return;
		}
		if (feedStatus.equalsIgnoreCase("off"))
			processCricketOff(task);
		else if (feedStatus.equalsIgnoreCase("on")) {
			processCricketOnRequest(task);
		}
	}

	private void processCricketOnRequest(Task task) {
		populateActivatedBySelectedBy(task);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		logger.debug("Processing cricket on request. subscriber: " + subscriber);
		if (canActivateUserWithStatus(subscriber.getStatus())) {
			logger.debug("Activating user. " + subscriber);
			task.setObject(param_ISACTIVATE, "true");
			subscriber = processActivation(task);
		}
		String response = task.getString(param_response);
		if (canActivateUserWithStatus(subscriber.getStatus())
				|| (response != null && !response
						.equalsIgnoreCase(WebServiceConstants.SUCCESS))) {
			logger.debug("Failed to activating user. "
					+ subscriber.getSubscriberID()
					+ ", not processing cricket selection");
			task.setObject(param_response, Resp_Inactive);
		} else {
			if (!allowFeedUpgrade() && hasSelectionWithStatus(task, 90)) {
				task.setObject(param_response, Resp_SelExists);
				return;
			}
			String addResponse = processAddCricketSelection(task);
			task.setObject(param_response, addResponse);
		}
	}

	private String processAddCricketSelection(Task task) {
		FeedStatus cricketFeed = getCricketFeedStatus();
		ArrayList<String> passList = new ArrayList<String>();
		if (cricketFeed != null)
			passList = Utility.tokenizeArrayList(cricketFeed.getSubKeywords(),
					",");
		String cricPass = "DP";
		String feedPass = task.getString(param_FEED_PASS);
		if (!allowCricketPass())
			cricPass = null;
		else if (passList.contains(feedPass.toLowerCase()))
			cricPass = feedPass;
		if (cricPass != null)
			cricPass = cricPass.toUpperCase();
		task.setObject(param_feedPass, cricPass);
		if (allowCricketPass()) {
			Feed feed = getCricketFeed(cricPass);
			if (feed == null)
				return Resp_ClipNotAvailable;
		}
		String response = addCricketSelection(task);
		logger.debug("Adding cricket selection. response: " + response);
		return response;
	}

	private String addCricketSelection(Task task) {
		SelectionRequest request = new SelectionRequest(
				task.getString(param_subscriberID));
		if (task.containsKey(param_feedPass))
			request.setCricketPack(task.getString(param_feedPass));
		else
			request.setCricketPack("DEFAULT");
		request.setCategoryID(10 + "");
		request.setMode(task.getString(param_SELECTED_BY));
		request.setModeInfo(task.getString(param_SELECTION_INFO));
		if (task.containsKey(param_REFUND)
				&& task.getString(param_REFUND).equalsIgnoreCase("true")) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("REFUND", "TRUE");
			request.setSelectionInfoMap(map);
		}
		if (task.containsKey(param_CONSENT_LOG)) {
			HashMap<String, String> map = request.getSelectionInfoMap();
			if (map == null)
				map = new HashMap<String, String>();
			map.put("CONSENT_LOG", task.getString(param_CONSENT_LOG));
		}

		if (task.containsKey(param_FROMTIME)) {
			Integer fromTime = Integer.parseInt(task.getString(param_FROMTIME));
			request.setFromTime(fromTime);

		}
		if (task.containsKey(param_TOTIME)) {
			Integer toTime = Integer.parseInt(task.getString(param_TOTIME));
			request.setToTime(toTime);
		}

		rbtClient.addSubscriberSelection(request);

		String response = request.getResponse();
		logger.info("Added cricket selection, response " + response);

		if (response.toLowerCase().indexOf("success") != -1) {
			response = Resp_Success;
		} else {
			response = Resp_Failure;
		}

		task.remove(param_library);
		return response;
	}

	private FeedStatus getCricketFeedStatus() {
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType("CRICKET");
		return rbtClient.getFeedStatus(request);
	}

	private Feed getCricketFeed(String pass) {
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType("CRICKET");
		request.setName(pass);
		return rbtClient.getFeed(request);
	}

	private void processCricketOff(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		if (!status.equals(WebServiceConstants.ACTIVE))
			task.setObject(param_response, Resp_Inactive);
		else if (!hasSelectionWithStatus(task, 90))
			task.setObject(param_response, Resp_SelNotExists);
		else {
			SelectionRequest selectionRequest = new SelectionRequest(
					subscriber.getSubscriberID());
			selectionRequest.setStatus(90);
			rbtClient.deleteSubscriberSelection(selectionRequest);
			task.remove(param_library);
			task.setObject(param_response, Resp_Success);
		}
	}

	protected boolean canActivateUserWithStatus(String status) {
		if (status == null)
			return false;
		return status.equals(WebServiceConstants.NEW_USER)
				|| status.equals(WebServiceConstants.DEACTIVE);
	}

	protected Library getSubscrbierLibrary(Task task) {
		Library library = null;
		if (task.containsKey(param_library))
			library = (Library) task.getObject(param_library);
		else {
			RbtDetailsRequest request = new RbtDetailsRequest(
					task.getString(param_subscriberID));
			request.setStatus(WebServiceConstants.ALL);
			if (task.containsKey(param_status))
				request.setStatus(task.getString(param_status));
			library = rbtClient.getLibrary(request);
			task.setObject(param_library, library);
		}
		return library;
	}

	protected Setting[] getSubscrbierSettings(Task task) {
		RbtDetailsRequest request = new RbtDetailsRequest(
				task.getString(param_subscriberID));
		request.setStatus(WebServiceConstants.ALL);
		if (task.containsKey(param_status))
			request.setStatus(task.getString(param_status));
		Settings settings = rbtClient.getSettings(request);
		if (settings != null)
			return settings.getSettings();
		return null;
	}

	protected boolean hasSelectionWithStatus(Task task, int status) {

		task.setObject(param_status, status + "");
		Setting[] settings = getSubscrbierSettings(task);
		if (settings != null) {
			for (int i = 0; settings != null && i < settings.length; i++) {
				if (settings[i].getStatus() == status)
					return true;
			}
		}
		return false;
	}

	protected String getSubscriberBlockedStatus(Subscriber subscriber) {
		String status = subscriber.getStatus();
		if (status.equals(WebServiceConstants.BLACK_LISTED))
			return Resp_BlackListedNo;
		else if (status.equals(WebServiceConstants.SUSPENDED)
				|| status.equals(WebServiceConstants.ACTIVATION_SUSPENDED))
			return Resp_SuspendedNo;
		else if (!subscriber.isValidPrefix())
			return Resp_InvalidPrefix;
		else if (!subscriber.isCanAllow())
			return Resp_BlackListedNo;

		return null;
	}

	protected String getSubscriberPendingStatus(Subscriber subscriber) {
		String status = subscriber.getStatus();
		if (status.equals(WebServiceConstants.ACT_PENDING))
			return Resp_ActPending;
		else if (status.equals(WebServiceConstants.DEACT_PENDING))
			return Resp_DeactPending;
		return null;
	}

	@Override
	public void processHSBRequest(Task task) {
		String request = task.getTaskAction();
		if (request.equalsIgnoreCase(request_hsb_act)) {
			if (!task.containsKey(param_CATEGORY_ID)) {
				task.setObject(param_CATEGORY_ID, "2");
			}
			if (task.containsKey(param_PROMO_ID)
					&& task.getString(param_PROMO_ID) != null) {
				task.setObject(param_ISACTIVATE, "true");
				if (!task.containsKey(param_SUBSCRIPTION_CLASS))
					task.setObject(param_SUBSCRIPTION_CLASS, "HSB");
				Clip clip = getClipByPromoId(task.getString(param_PROMO_ID));
				if (clip == null)
					task.setObject(param_response, Resp_ClipNotAvailable);
				else
					processSelection(task);
			} else
				processActivation(task);

			String response = task.getString(param_response);
			if (!response.equals(Resp_Success)
					&& !response.equals(Resp_AlreadyActive)
					&& !response.equals(Resp_liteUserPremiumBlocked))
				response = Resp_Failure;
		} else if (request.equalsIgnoreCase(request_hsb_can)
				|| request.equalsIgnoreCase(request_hsb_dct)
				|| request.equalsIgnoreCase(request_hsb_deact)) {
			processDeactivation(task);
			String response = task.getString(param_response);
			if (!response.equals(Resp_Success)
					&& !response.equals(Resp_Inactive)
					&& !response.equals(Resp_AlreadyInactive))
				response = Resp_Failure;
		}
	}

	/**
	 * Process' the gift request
	 * 
	 * @author Sreekar
	 * @param task
	 *            should contain
	 *            MSISDN-giftee,CALLER_ID-gifter,SELECTED_BY-mode,clip-tone
	 *            gifted
	 * @return returns the webservice response from sendGift API
	 */
	public String processGift(Task task) {
		GiftRequest request = new GiftRequest();
		request.setGifteeID(task.getString(param_MSISDN));
		request.setGifterID(task.getString(param_CALLER_ID));
		request.setMode(task.getString(param_SELECTED_BY));
		if (task.containsKey(param_category)) {
			Category cat = (Category) task.getObject(param_category);
			request.setCategoryID(cat.getCategoryId() + "");
		} else if (task.containsKey(param_clip)) {
			Clip clip = (Clip) task.getObject(param_clip);
			request.setToneID(clip.getClipId() + "");
		}
		rbtClient.sendGift(request);
		setGiftResponse(task, request.getResponse());
		return task.getString(param_response);
	}

	protected void setGiftResponse(Task task, String response) {
		if (response == null || response.equals("")
				|| response.equalsIgnoreCase("null"))
			task.setObject(param_response, Resp_Err);
		else if (response.equals(WebServiceConstants.GIFTER_NOT_ACT))
			task.setObject(param_response, Resp_giftGifterNotActive);
		else if (response.equals(WebServiceConstants.GIFTEE_ACT_PENDING))
			task.setObject(param_response, Resp_giftGifteeActPending);
		else if (response.equals(WebServiceConstants.GIFTEE_DEACT_PENDING))
			task.setObject(param_response, Resp_giftGifteeDeactPending);
		else if (response.equals(WebServiceConstants.GIFTEE_GIFT_ACT_PENDING))
			task.setObject(param_response, Resp_giftActPending);
		else if (response.equals(WebServiceConstants.GIFTEE_GIFT_IN_USE))
			task.setObject(param_response, Resp_giftInUse);
		else if (response.equals(WebServiceConstants.EXISTS_IN_GIFTEE_LIBRAY))
			task.setObject(param_response, Resp_giftAlreadyPreset);
		else if (response.equals(WebServiceConstants.FAILED))
			task.setObject(param_response, Resp_Failure);
		else if (response.equals(WebServiceConstants.SUCCESS))
			task.setObject(param_response, Resp_Success);
		else {
			logger.error("RBT::invalid response-" + response);
			task.setObject(param_response, Resp_Failure);
		}
	}

	/**
	 * Process' the copy request
	 * 
	 * @author Sreekar
	 * @param task
	 *            Should contains MSISDN-who tries to copy,CALLER_ID-from which
	 *            the song to be copied,SELECTED_BY-mode
	 * @return Returns the promo responses
	 */
	public void processCopyRequest(Task task) {
		String dstCallerID = task.getString(param_CALLER_ID);
		String subscriberID = task.getString(param_MSISDN);
		if (subscriberID == null) {
			task.setObject(param_response, Resp_InvalidNumber);
			return;
		}
		if (dstCallerID == null || dstCallerID.equals(subscriberID)) {
			task.setObject(param_invalidParam, param_CALLER_ID);
			task.setObject(param_response, Resp_missingParameter);
			return;
		}
		CopyRequest copyDetailRequest = new CopyRequest(subscriberID,
				dstCallerID);
		CopyDetails copyDetails = rbtClient.getCopyData(copyDetailRequest);
		CopyData copydata[] = copyDetails.getCopyData();
		String detailResponse = copyDetailRequest.getResponse();
		if (detailResponse == null)
			task.setObject(param_response, Resp_Err);
		else if (detailResponse.equals(WebServiceConstants.NOT_ALLOWED)
				|| detailResponse.equals(WebServiceConstants.PERSONAL_MESSAGE))
			task.setObject(param_response, Resp_copyCantCopy);
		else if (detailResponse.equals(WebServiceConstants.NOT_RBT_USER)) {
			task.setObject(param_invalidParam, param_CALLER_ID);
			task.setObject(param_response, Resp_missingParameter);
		} else if (detailResponse.equals(WebServiceConstants.ALBUM_RBT))
			task.setObject(param_response, Resp_copyShuffleSelection);
		else if (detailResponse.equals(WebServiceConstants.DEFAULT_RBT)) {
			task.setObject(param_CLIPID, null);
			addCopyRequestToViral(task);
		} else if (detailResponse.equals(WebServiceConstants.SUCCESS)) {
			CopyData songCopyData = copydata[0];
			String clipID = songCopyData.getPreviewFile()
					.replaceAll(".wav", "")
					+ ":"
					+ songCopyData.getCategoryID()
					+ ":"
					+ songCopyData.getStatus();
			String callerID = task.getString(param_SPECIAL_CALLER_ID);
			if (callerID != null)
				clipID = clipID + "|" + callerID;
			task.setObject(param_CLIPID, clipID);
			addCopyRequestToViral(task);
		}
		task.setObject(param_response, Resp_Success);
	}

	private void addCopyRequestToViral(Task task) {
		task.setObject(param_callerid, task.getString(param_MSISDN));
		task.setObject(param_subscriberID, task.getString(param_CALLER_ID));
		task.setObject(param_SMSTYPE, "COPY");
		addViraldata(task);
		task.setObject(param_response, Resp_Success);
	}

	public void processSongPackRequest(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String cosID = task.getString(param_COSID);
		try {
			CosDetails cosDetails = null;
			if (cosID != null) {
				cosDetails = cosDetailsCacheManager.getCosDetail(cosID,
						subscriber.getCircleID());
				if (cosDetails == null) {
					logger.error("Subscriber id: "
							+ subscriber.getSubscriberID()
							+ " COSID "
							+ cosID
							+ " is not configured in rbt_cos_details. Returning failure");
					task.setObject(param_response, Resp_Failure);
					return;
				}
				// if cos request is song pack, will upgrade the selection pack
				if (cosDetails.getCosType() != null
						&& (cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.SONG_PACK)
								|| cosDetails.getCosType().equalsIgnoreCase(
										iRBTConstant.UNLIMITED_DOWNLOADS)
								|| cosDetails
										.getCosType()
										.equalsIgnoreCase(
												iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE) || confAzaanCosIdList
									.contains(cosID))
						|| cosDetails.getCosType().equalsIgnoreCase(
								iRBTConstant.AZAAN)) {
					logger.info("Subscriber id: "
							+ subscriber.getSubscriberID()
							+ " SongPack request");
					SelectionRequest selectionRequest = new SelectionRequest(
							subscriber.getSubscriberID());
					selectionRequest.setIsPrepaid(subscriber.isPrepaid());
					selectionRequest.setCosID(Integer.parseInt(cosDetails
							.getCosId()));
					selectionRequest.setMode(task.getString(param_mode));
					// Added for CRM Request Jira Id RBT-11962
					// Fix for RBT-12391,RBT-12394
					HashMap<String, String> map = selectionRequest
							.getUserInfoMap();
					if (map == null) {
						map = new HashMap<String, String>();
					} else {
						if (map.containsKey(param_SR_ID))
							map.remove(param_SR_ID);
						if (map.containsKey(param_ORIGINATOR))
							map.remove(param_ORIGINATOR);
					}
					if (task.containsKey(param_SR_ID))
						map.put("SR_ID", task.getString(param_SR_ID));
					if (task.containsKey(param_ORIGINATOR))
						map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
					selectionRequest.setUserInfoMap(map);
					RBTClient.getInstance().upgradeSelectionPack(
							selectionRequest);

					if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.SUCCESS))
						task.setObject(param_response, Resp_Success);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.FAILED))
						task.setObject(param_response, Resp_Failure);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.ACT_PENDING))
						task.setObject(param_response, Resp_ActPending);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.ALREADY_ACTIVE))
						task.setObject(param_response, Resp_AlreadyActive);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.COS_NOT_EXISTS))
						task.setObject(param_response, Resp_Err);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.OVERLIMIT))
						task.setObject(param_response, Resp_downloadOverlimit);
					else if (selectionRequest.getResponse().equalsIgnoreCase(
							WebServiceConstants.PACK_ALREADY_ACTIVE))
						task.setObject(param_response, Resp_PackAlreadyActive);
					else
						task.setObject(param_response, Resp_Err);

					return;
				}
			}
			// if user is active, the update the base pack
			if (subscriber.getStatus().equalsIgnoreCase(
					WebServiceConstants.ACTIVE)) {
				SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
						subscriber.getSubscriberID());
				subscriptionRequest.setMode(task.getString(param_mode));
				subscriptionRequest.setRentalPack(task
						.getString(param_SUBSCRIPTION_CLASS));
				if (cosDetails != null) {
					subscriptionRequest.setCosID(Integer.parseInt(cosDetails
							.getCosId()));
				}
				// Added for CRM Request Jira Id RBT-11962
				// Fix for RBT-12391,RBT-12394
				HashMap<String, String> map = subscriptionRequest
						.getUserInfoMap();
				if (map == null) {
					map = new HashMap<String, String>();
				} else {
					if (map.containsKey(param_SR_ID))
						map.remove(param_SR_ID);
					if (map.containsKey(param_ORIGINATOR))
						map.remove(param_ORIGINATOR);
				}
				if (task.containsKey(param_SR_ID))
					map.put("SR_ID", task.getString(param_SR_ID));
				if (task.containsKey(param_ORIGINATOR))
					map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
				subscriptionRequest.setUserInfoMap(map);
				RBTClient.getInstance().activateSubscriber(subscriptionRequest);

				if (subscriptionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.SUCCESS))
					task.setObject(param_response, Resp_Success);
				else if (subscriptionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.FAILED))
					task.setObject(param_response, Resp_Failure);
				else if (subscriptionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.OVERLIMIT))
					task.setObject(param_response, Resp_downloadOverlimit);
				else if (subscriptionRequest.getResponse().equalsIgnoreCase(
						WebServiceConstants.PACK_ALREADY_ACTIVE))
					task.setObject(param_response, Resp_PackAlreadyActive);
			}
		} catch (Exception e) {
			logger.error("Exception in processing songpack request >"
					+ e.getMessage());
			task.setObject(param_response, Resp_Failure);
		}
	}

	public void processTopupRequest(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String subClass = task.getString(param_SUBSCRIPTION_CLASS);
		// Added for Aircel for inactive subscribers to have a different
		// subscription class
		logger.info("Subscriber status is :" + subscriber.getStatus());
		if (etopUpSubClassMap != null
				&& !(isSubActive(subscriber) || subscriber.getStatus()
						.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
				&& subClass != null) {
			if (etopUpSubClassMap.containsKey(subClass))
				subClass = etopUpSubClassMap.get(subClass);
		}
		String allowUpgadeForSmaeSubClass = RBTParametersUtils
				.getParamAsString(iRBTConstant.COMMON,
						"ALLOW_UPGRADE_FOR_SAME_SUB_CLASS", "TRUE");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		subscriptionRequest.setIsPrepaid(subscriber.isPrepaid());
		subscriptionRequest.setMode(task.getString(param_ACTIVATED_BY));
		subscriptionRequest.setRentalPack(subClass);
		subscriptionRequest.setPreCharged(true);
		// RBT-14301: Uninor MNP changes.
		if (task.containsKey(param_CIRCLE_ID)
				&& !task.getString(param_CIRCLE_ID).trim().isEmpty()) {
			subscriptionRequest.setCircleID(task.getString(param_CIRCLE_ID));
		}
		HashMap<String, String> map = subscriptionRequest.getUserInfoMap();
		if (map == null)
			map = new HashMap<String, String>();
		if (task.containsKey(EXTRA_INFO_TPCGID)) {
			map.put(EXTRA_INFO_TPCGID, task.getString(EXTRA_INFO_TPCGID));
		} else {
			map.remove(EXTRA_INFO_TPCGID);
		}
		subscriptionRequest.setUserInfoMap(map);
		try {

			if (allowUpgadeForSmaeSubClass.equals("FALSE")
					&& subscriber.getSubscriptionClass().equalsIgnoreCase(
							subClass)) {
				task.setObject(param_response, Resp_Failure);
				return;
			}
			RBTClient.getInstance().activateSubscriber(subscriptionRequest);
			if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS)) {
				task.setObject(param_response, Resp_Success);
				String upgradeBaseRes = Resp_Success;
				if (task.containsKey(param_TONE_ID)
						|| task.containsKey(param_PROMO_ID)
						|| task.containsKey(param_WAV_FILE)) {
					processSelection(task);
					if (!task.getString(param_response).equalsIgnoreCase(
							"success")) {
						task.setObject(param_response, upgradeBaseRes + "_"
								+ task.getString(param_response));
					}
				}
			} else if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)) {
				task.setObject(param_response, Resp_TnbSongSelectionNotAllowed);
			} else if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.UPGRADE_NOT_ALLOWED)) {
				task.setObject(param_response, Resp_UpgradeNotAllowed);
			} else
				task.setObject(param_response, Resp_Failure);

		} catch (Exception e) {
			logger.error("Exception in processing songpack request >"
					+ e.getMessage());
			logger.error("Exception", e);
			task.setObject(param_response, Resp_Failure);
		}
	}

	@Override
	public void processGiftAckRequest(Task task) {
	}

	@Override
	public boolean isValidPrefix(String subId) {
		return false;
	}

	@Override
	public void processAdRBTRequest(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String suspendStatus = getSubscriberBlockedStatus(subscriber);
		if (suspendStatus != null) {
			task.setObject(param_response, suspendStatus);
			return;
		}

		String action = task.getTaskAction();
		if (action.equalsIgnoreCase(request_ad_rbt_act))
			processAdRBTActivation(task);
		else if (action.equalsIgnoreCase(request_ad_rbt_deact))
			processAdRBTDeactivation(task);
		else if (action.equalsIgnoreCase(request_ad_rbt_act_convert))
			processAdRBTActivationConversion(task);
		else if (action.equalsIgnoreCase(request_ad_rbt_deact_convert))
			processAdRBTDeactivationConversion(task);
	}

	protected void processAdRBTDeactivationConversion(Task task) {
		if (checkTransID(task, "ADRBT_DEACT")) {
			task.setObject(param_response, Resp_invalidTransID);
			return;
		}
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if (pendingStatus != null) {
			if (pendingStatus.equals(Resp_ActPending))
				task.setObject(param_response, Resp_actPendingAdRBT);
			else if (pendingStatus.equals(Resp_DeactPending))
				task.setObject(param_response, Resp_deactPendingAdRBT);
			return;
		}

		String status = subscriber.getStatus();
		boolean canActivate = canActivateUserWithStatus(status);
		if (canActivate)
			task.setObject(param_response, Resp_InactiveOnRBT);
		else if (status.equals(WebServiceConstants.ACTIVE)) {
			if (!subscriber.getUserType().equals(WebServiceConstants.AD_RBT)) {
				task.setObject(param_response, Resp_InactiveOnAdRBT);
				return;
			}
			task.setObject(param_NEW_RBT_TYPE, 0);
			updateSubscriberRBTType(task);
		} else
			task.setObject(param_response, Resp_Failure);
		populateAdRBTEesponse(task);
	}

	protected void processAdRBTActivationConversion(Task task) {
		if (checkTransID(task, "ADRBT_ACT")) {
			task.setObject(param_response, Resp_invalidTransID);
			return;
		}
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if (pendingStatus != null) {
			if (pendingStatus.equals(Resp_ActPending))
				task.setObject(param_response, Resp_actPendingAdRBT);
			else if (pendingStatus.equals(Resp_DeactPending))
				task.setObject(param_response, Resp_deactPendingAdRBT);
			return;
		}
		String status = subscriber.getStatus();
		boolean canActivate = canActivateUserWithStatus(status);
		if (!canActivate) {
			if (subscriber.getUserType().equals(WebServiceConstants.AD_RBT)) {
				task.setObject(param_response, Resp_alreadyActiveOnAdRBT);
				return;
			}
			task.setObject(param_NEW_RBT_TYPE, 1);
			updateSubscriberRBTType(task);
			return;
		}
		if (canActivate)
			processAdRBTActivation(task);
		populateAdRBTEesponse(task);
	}

	private void updateSubscriberRBTType(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		SubscriptionRequest request = new SubscriptionRequest(
				subscriber.getSubscriberID());
		request.setRbtType((Integer) task.getObject(param_NEW_RBT_TYPE));
		request.setPlayerStatus("A");
		rbtClient.updateSubscription(request);
		String response = request.getResponse();
		if (response.equals(WebServiceConstants.SUCCESS)) {
			makeAdRBTServerRequest(task);
			task.setObject(param_response, Resp_Success);
		} else
			task.setObject(param_response, Resp_Failure);
	}

	private void makeAdRBTServerRequest(Task task) {
		if (!canMakeAdServerRequest())
			return;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String request = task.getTaskAction();
		String url = null;
		if (request.equals(request_ad_rbt_act_convert))
			url = getParameter(DAEMON, "ADRBT_ACT_URL");
		else if (request.equals(request_ad_rbt_deact_convert))
			url = getParameter(DAEMON, "ADRBT_DEACT_URL");
		if (url == null)
			return;
		url = url + "MSISDN=" + subscriber.getSubscriberID();

		HttpParameters httpParameters = new HttpParameters(url);
		String useProxyHost = getParameter(DAEMON, "USE_PROXY_ADRBT");
		if (useProxyHost != null && useProxyHost.equalsIgnoreCase("true")) {
			httpParameters.setUseProxy(true);
			httpParameters
					.setProxyHost(getParameter(DAEMON, "PROXY_HOST_ADRBT"));
			httpParameters.setProxyPort(Integer.parseInt(getParameter(DAEMON,
					"PROXY_PORT_ADRBT")));
		}
		String connectionTimeOut = getParameter(DAEMON,
				"ADRBT_CONNECTION_TIME_OUT");
		if (connectionTimeOut != null)
			httpParameters.setConnectionTimeout(Integer
					.parseInt(connectionTimeOut));

		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			Logger.getLogger(PromoProcessor.class).info(
					"RBT:: httpResponse: " + httpResponse);
		} catch (Exception e) {
			Logger.getLogger(PromoProcessor.class).error(
					"RBT:: " + e.getMessage(), e);
		}
	}

	protected void processAdRBTDeactivation(Task task) {
		String preDeactivationChkResp = performAdRBTPreDeactivationCheck(task);
		if (preDeactivationChkResp != null) {
			task.setObject(param_response, preDeactivationChkResp);
			return;
		}
		task.setObject(param_TRANS_TYPE, "ADRBT_DEACT");
		task.setObject(param_DEACTIVATED_BY, "ADRBT");
		processDeactivation(task);
		populateAdRBTEesponse(task);
	}

	protected String performAdRBTPreDeactivationCheck(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String response = null;
		if (!status.equals(WebServiceConstants.ACTIVE))
			response = Resp_InactiveOnRBT;
		else {
			if (!subscriber.getUserType().equals(WebServiceConstants.AD_RBT))
				response = Resp_InactiveOnAdRBT;
		}
		return response;
	}

	protected void processAdRBTActivation(Task task) {
		String preActivationChkResp = performAdRBTPreActivationCheck(task);
		boolean isSelRequest = false;
		String toneID = task.getString(param_TONE_ID);
		if (toneID != null) {
			Clip clip = rbtCacheManager.getClip(toneID);
			if (clip != null && clip.getClipEndTime().after(new Date())) {
				isSelRequest = true;
			}
		}

		if (preActivationChkResp != null && !isSelRequest) {
			task.setObject(param_response, preActivationChkResp);
			return;
		}

		String subscriptionClass = "ADRBT";
		if (task.containsKey(param_SUBSCRIPTION_CLASS))
			subscriptionClass = task.getString(param_SUBSCRIPTION_CLASS);

		String activatedBy = "ADRBT";
		if (task.containsKey(param_ACTIVATED_BY))
			activatedBy = task.getString(param_ACTIVATED_BY);

		task.setObject(param_TRANS_TYPE, "ADRBT_ACT");
		task.setObject(param_SUBSCRIPTION_CLASS, subscriptionClass);
		task.setObject(param_ACTIVATED_BY, activatedBy);
		task.setObject(param_rbttype, 1);

		if (isSelRequest)
			processSelection(task);
		else
			processActivation(task);

		populateAdRBTEesponse(task);
	}

	protected String performAdRBTPreActivationCheck(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String response = null;
		if (!canActivateUserWithStatus(status)) {
			if (status.equals(WebServiceConstants.ACTIVE)) {
				if (subscriber.getUserType().equals(WebServiceConstants.AD_RBT))
					response = Resp_alreadyActiveOnAdRBT;
				else
					response = Resp_alreadyActiveOnRBT;
			} else
				response = Resp_Failure;
		}
		return response;
	}

	protected void populateAdRBTEesponse(Task task) {
		String response = task.getString(param_response);
		if (response == null
				|| (!response.equalsIgnoreCase(Resp_Success) && !response
						.equalsIgnoreCase(Resp_invalidTransID)))
			task.setObject(param_response, Resp_Failure);
	}

	private boolean canMakeAdServerRequest() {
		boolean value = false;
		String valueStr = getParameter(DAEMON, "ADRBT_SERVER_URL_HIT");
		if (valueStr != null)
			value = valueStr.equalsIgnoreCase("true");
		return value;
	}

	public void processMeriDhunRequest(Task task) {
		logger.info("RBT::task-" + task);
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String isActivate = task.getString(param_ISACTIVATE);
		if (canActivateUserWithStatus(subscriber.getStatus())
				&& (isActivate == null || !isActivate.equalsIgnoreCase("true"))) {
			task.setObject(param_response, Resp_Failure);
			return;
		}
		String promoId = task.getString(param_PROMO_ID);
		Clip clip = getClipByPromoId(promoId);
		if (clip == null) {
			task.setObject(param_response, Resp_ClipNotAvailable);
			return;
		}
		task.setObject(param_clipid, clip.getClipId() + "");
		task.setObject(param_smsText, task.getObject(param_PROMO_TEXT));
		super.processMeriDhunRequest(task);
		setMeriDhunResponse(task);
	}

	private void setMeriDhunResponse(Task task) {
		String response = task.getString(param_response);
		logger.info("RBT::response-" + response);
		if (response == null)
			response = Resp_Err;
		else if (response.equals(SUCCESS))
			response = Resp_Success;
		else if (response.equals(CLIP_NOT_AVAILABLE))
			response = Resp_ClipNotAvailable;
		else
			response = Resp_Failure;
		task.setObject(param_response, response);
	}

	public void processSuspensionRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UtilsRequest utilsRequest = new UtilsRequest(
				subscriber.getSubscriberID(), true, "PROMOTION");
		try {
			RBTClient.getInstance().suspension(utilsRequest);
			if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_response, Resp_Success);
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.ALREADY_VOLUNTARILY_SUSPENDED))
				task.setObject(param_response, Resp_alreadyVoluntarySuspended);
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUSPENSION_NOT_ALLOWED))
				task.setObject(param_response, Resp_suspensionNotAllowed);
			else
				task.setObject(param_response, Resp_Failure);

		} catch (Exception e) {
			logger.error("exception in processing the suspension request >"
					+ e.getMessage());
			task.setObject(param_response, Resp_Failure);
		}

	}

	public void processResumptionRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		UtilsRequest utilsRequest = new UtilsRequest(
				subscriber.getSubscriberID(), false, "PROMOTION");
		try {
			RBTClient.getInstance().suspension(utilsRequest);
			if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_response, Resp_Success);
			else if (utilsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.NOT_VOLUNTARILY_SUSPENDED))
				task.setObject(param_response, Resp_notVoluntarySuspended);
			else
				task.setObject(param_response, Resp_Failure);

		} catch (Exception e) {
			logger.error("exception in processing the resumption request >"
					+ e.getMessage());
			task.setObject(param_response, Resp_Failure);
		}
	}

	@Override
	public void addToBlackList(Task task) {
		try {
			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			if (!subscriber.isCanAllow()) {
				// Already black listed
				task.setObject(param_response, Resp_alreadyBlackListed);
				return;
			}
			UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
					subscriber.getSubscriberID(), true, "TOTAL");
			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_response, Resp_Success);
			else if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.INVALID_PREFIX))
				task.setObject(param_response, Resp_InvalidNumber);
			else if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.BLACK_LISTED))
				task.setObject(param_response, Resp_alreadyBlackListed);
			else
				task.setObject(param_response, Resp_Failure);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			task.setObject(param_response, Resp_Err);
		}
	}

	@Override
	public void removeFromBlackList(Task task) {
		try {
			Subscriber subscriber = (Subscriber) task
					.getObject(param_subscriber);
			if (subscriber.isCanAllow()) {
				// Not black listed, that is why returning success
				task.setObject(param_response, Resp_Success);
				return;
			}

			UpdateDetailsRequest updateDetailsRequest = new UpdateDetailsRequest(
					subscriber.getSubscriberID(), false, "TOTAL");
			RBTClient.getInstance().setSubscriberDetails(updateDetailsRequest);

			if (updateDetailsRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_response, Resp_Success);
			else
				task.setObject(param_response, Resp_Failure);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			task.setObject(param_response, Resp_Err);
		}
	}

	public static Subscriber getSubscriber(Task task) {
		String subscriberID = task.getString(param_subscriberID);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
				subscriberID);
		rbtDetailsRequest.setMode(task.getString(param_mode));
		if (task.containsKey(param_consent_status)) {
			rbtDetailsRequest.setConsentInd(Boolean.valueOf(task
					.getString(param_consent_status)));
		}
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		String prepaidStr = task.getString(param_SUB_TYPE);
		if (prepaidStr != null) {
			if (prepaidStr.toLowerCase().startsWith("pre"))
				subscriber.setPrepaid(true);
			else if (prepaidStr.toLowerCase().startsWith("post"))
				subscriber.setPrepaid(false);
		}
		task.setObject(param_subscriber, subscriber);
		return subscriber;
	}

	// Added by Sreekar for BSNL RBT and can be generic too
	protected void updateSubscription(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		if (task.containsKey(param_ACTIVATED_BY))
			subscriptionRequest.setMode(task.getString(param_ACTIVATED_BY));
		if (task.containsKey(param_userInfoMap))
			subscriptionRequest.setUserInfoMap((HashMap<String, String>) task
					.getObject(param_userInfoMap));
		if (task.containsKey(param_playerStatus))
			subscriptionRequest.setPlayerStatus(task
					.getString(param_playerStatus));
		if (task.containsKey(param_rbttype))
			subscriptionRequest.setRbtType((Integer) task
					.getObject(param_rbttype));
		if (task.containsKey(param_TRANSID)) {
			String actInfo = subscriber.getActivationInfo();
			if (actInfo == null)
				actInfo = "";
			subscriptionRequest.setModeInfo(actInfo
					+ task.getString(param_TRANSID));
		}
		subscriber = rbtClient.updateSubscription(subscriptionRequest);
		task.setObject(param_response, subscriptionRequest.getResponse());
		setUpgradeSubscriptionResponse(task);
	}

	// This method should be used for all upgrades. Currently renewal trigger is
	// supported. Modification will be required to support other upgardes.
	public void upgradeSubscription(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isSubActive(subscriber)) {
			task.setObject(param_response, Resp_InactiveUserUpgradeFailure);
			return;
		}
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		String requestType = task.getString(param_REQUEST);
		if (requestType != null && requestType.equalsIgnoreCase("UP_VALIDITY")) {
			subscriptionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);
			subscriptionRequest.setMode(task.getString(param_MODE));
			subscriptionRequest.setModeInfo(task.getString(param_ipAddress));

			String subscriptionClass = task.getString(param_SUBSCRIPTION_CLASS);
			if (subscriptionClass != null)
				subscriptionRequest.setSubscriptionClass(subscriptionClass);
			else {
				task.setObject(param_response, Resp_missingParameter);
				return;
			}

		}

		subscriber = rbtClient.updateSubscription(subscriptionRequest);
		task.setObject(param_response, subscriptionRequest.getResponse());
		setUpgradeSubscriptionResponse(task);
	}

	private void setUpgradeSubscriptionResponse(Task task) {
		String response = task.getString(param_response);
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS))
			response = Resp_Success;
		else
			response = Resp_Failure;
		task.setObject(param_response, response);
	}

	/**
	 * 
	 * @param task
	 * @param key
	 *            Key to be added to subscriber extra info
	 * @param value
	 *            Vaule for the key to be added to subscriber extra info
	 */
	protected void addToSubscriberExtraInfo(Task task, String key, String value) {
		if (key == null || value == null)
			return;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber == null && !task.containsKey(param_userInfoMap))
			return;
		HashMap<String, String> userInfoMap = null;
		if (task.containsKey(param_userInfoMap))
			userInfoMap = (HashMap<String, String>) task
					.getObject(param_userInfoMap);
		if (userInfoMap == null)
			userInfoMap = subscriber.getUserInfoMap();
		if (userInfoMap == null)
			userInfoMap = new HashMap<String, String>();
		userInfoMap.put(key, value);
		task.setObject(param_userInfoMap, userInfoMap);
	}

	protected boolean inform3rdPartyAdRBT() {
		return parameterCacheManager
				.getParameter(COMMON, ADRBT_SERVER_URL_HIT, "FALSE").getValue()
				.equalsIgnoreCase("TRUE");
	}

	protected void init() {
		logger.info("RBT:: inside init(): ");
		String blockedClassesStr = param(COMMON,
				CHECK_MAPPING_MANDATORY_CHARGE_CLASS, null);
		if (blockedClassesStr != null) {
			StringTokenizer stk = new StringTokenizer(blockedClassesStr, ",");
			while (stk.hasMoreTokens())
				blockedChargeClasses.add(stk.nextToken().trim().toUpperCase());
		}
		String blockedModesStr = param(COMMON,
				CHECK_CATEGORY_CLIP_MAPPING_MANDATORY_MODES, null);
		if (blockedModesStr != null) {
			StringTokenizer stk = new StringTokenizer(blockedModesStr, ",");
			while (stk.hasMoreTokens())
				blockedModes.add(stk.nextToken().trim().toUpperCase());
		}
		String defaultCategoryStr = param(COMMON,
				DEFAULT_PROMOTION_CATEGORY_ID, "6");
		if (defaultCategoryStr != null)
			defaultCategoryId = defaultCategoryStr.trim();
		String etopupMapStr = param(PROMOTION,
				ETOPUP_SUB_CLASS_MAP_FOR_INACTIVE_USER, null);
		if (etopupMapStr != null) {
			StringTokenizer stk = new StringTokenizer(etopupMapStr, ";");
			etopUpSubClassMap = new HashMap<String, String>();
			while (stk.hasMoreTokens()) {
				String pairStr = stk.nextToken();
				StringTokenizer pairTokens = new StringTokenizer(pairStr, ",");
				if (pairTokens.countTokens() != 2)
					continue;
				String firstSubClass = pairTokens.nextToken();
				String secondSubClass = pairTokens.nextToken();
				etopUpSubClassMap.put(firstSubClass, secondSubClass);
			}
		}
		String cosIds = param(DAEMON, AZAAN_COS_ID_LIST, null);
		confAzaanCosIdList = new HashSet<String>(ListUtils.convertToList(
				cosIds, ","));

		logger.info("RBT:: inside init(): blockedModes is " + blockedModes);
		logger.info("RBT:: inside init(): blockedChargeClasses is "
				+ blockedChargeClasses);
		logger.info("RBT:: inside init(): defaultCategoryId is "
				+ defaultCategoryId);
		logger.info("RBT:: inside init(): confAzaanCosIdList is "
				+ confAzaanCosIdList);
		logger.info("RBT:: exiting init()");

	}

	private boolean isSmsAliasConfigured(String smsAlias) {
		Parameters param = CacheManagerUtil.getParametersCacheManager()
				.getParameter(PROVISIONING, SMS_ALIAS_STRINGS);
		boolean aliasConfigured = false;
		if (param == null || param.getValue() == null
				|| param.getValue().trim().equals("")) {
			aliasConfigured = false;
		} else {
			String[] tokens = param.getValue().toLowerCase().split(",");
			for (int indx = 0; indx < tokens.length; indx++) {
				if (tokens[indx].equals(smsAlias)) {
					aliasConfigured = true;
					break;
				}
			}
		}
		return aliasConfigured;
	}

	public void processTNBActivation(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		String toneId = task.getString(param_TONE_ID);
		String promoId = task.getString(param_PROMO_ID);
		String wavfile = task.getString(param_WAV_FILE);
		String smsAlias = task.getString(param_SMS_ALIAS);
		logger.info("Subscriber's status : " + subscriber.getStatus()
				+ ", TONE_ID : " + toneId + ", PROMO_ID : " + promoId);
		if (subscriber.getStatus().equals(WebServiceConstants.ACTIVE)
				|| subscriber.getStatus().equals(
						WebServiceConstants.ACT_PENDING)
				|| subscriber.getStatus().equals(WebServiceConstants.GRACE)) {
			processSelection(task);
		} else if (subscriber.getStatus().equals(WebServiceConstants.NEW_USER)
				|| subscriber.getStatus().equals(WebServiceConstants.DEACTIVE)) {
			String userType = subscriber.isPrepaid() ? "p" : "b";
			try {
				String subClass = "ZERO";
				if (task.containsKey(param_SUBSCRIPTION_CLASS))
					subClass = task.getString(param_SUBSCRIPTION_CLASS);
				com.onmobile.apps.ringbacktones.smClient.beans.Offer[] offers = RBTSMClientHandler
						.getInstance().getOffer(subscriber.getSubscriberID(),
								"TNB", Offer.OFFER_TYPE_SUBSCRIPTION, userType,
								subClass, null);

				if (offers == null || offers.length == 0) {
					task.setObject(param_response, Resp_OffersNotAvailable);
					return;
				} else {
					boolean match = false;
					for (com.onmobile.apps.ringbacktones.smClient.beans.Offer offer : offers) {
						if (offer.getSrvKey() != null
								&& offer.getSrvKey().equalsIgnoreCase(subClass)) {
							match = true;
							break;
						}
					}
					if (!match) {
						task.setObject(param_response, Resp_OffersNotAvailable);
						return;
					}
				}

				task.setObject(param_SUBSCRIPTION_CLASS, subClass);
				task.setObject(EXTRA_INFO_OFFER_ID, offers[0].getOfferID());

				if ((toneId == null || toneId.equals(""))
						&& (promoId == null || promoId.equals(""))
						&& (wavfile == null || wavfile.equals(""))
						&& (smsAlias == null || smsAlias.equals(""))) {
					processActivation(task);
				} else {
					task.setObject(param_ISACTIVATE, "TRUE");
					processSelection(task);
				}
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			task.setObject(param_response, subscriber.getStatus());
		}
	}

	protected String getParamAsString(String type, String param,
			String defaultVal) {
		try {
			return parameterCacheManager.getParameter(type, param, defaultVal)
					.getValue();
		} catch (Exception e) {
			logger.info("getParameterAsBoolean unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal;
		}
	}

	/*
	 * @added by sridhar.sindiri
	 * 
	 * If new user, activation is done. If active user, upgradation is done.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.onmobile.apps.ringbacktones.provisioning.Processor#
	 * processBaseUpgradationRequest
	 * (com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public void processBaseUpgradationRequest(Task task) {
		logger.info("RBT::taskSession-" + task.getTaskSession());
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		String checkStatus = task.getString("CHECK_STATUS");
		if (checkStatus != null && checkStatus.equalsIgnoreCase("true")
				&& !isSubActive(subscriber)) {
			task.setObject(param_response, Resp_InactiveUserUpgradeFailure);
			return;
		}

		String subClass = task.getString(param_SUBSCRIPTION_CLASS);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
				subscriber.getSubscriberID());
		subscriptionRequest.setMode(task.getString(param_ACTIVATED_BY));
		subscriptionRequest.setRentalPack(subClass);
		try {
			// Added for CRM Request Jira Id RBT-11962
			// Fix for RBT-12391,RBT-12394
			HashMap<String, String> map = subscriber.getUserInfoMap();
			if (map == null) {
				map = new HashMap<String, String>();
			} else {
				if (map.containsKey(param_SR_ID))
					map.remove(param_SR_ID);
				if (map.containsKey(param_ORIGINATOR))
					map.remove(param_ORIGINATOR);
			}
			if (task.containsKey(param_SR_ID))
				map.put("SR_ID", task.getString(param_SR_ID));
			if (task.containsKey(param_ORIGINATOR))
				map.put("ORIGINATOR", task.getString(param_ORIGINATOR));
			subscriptionRequest.setUserInfoMap(map);
			if (task.containsKey(WebServiceConstants.param_isPreConsentBaseRequest)) {
				subscriptionRequest.setConsentInd(true);
				Rbt rbt = rbtClient
						.activateSubscriberPreConsent(subscriptionRequest);
				if (rbt != null)
					task.setObject("consentObj", rbt.getConsent());
			} else {
				RBTClient.getInstance().activateSubscriber(subscriptionRequest);
			}
			if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.SUCCESS))
				task.setObject(param_response, Resp_Success);
			// RBT-13585
			else if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.TNB_SONG_SELECTON_NOT_ALLOWED)) {
				task.setObject(param_response, Resp_TnbSongSelectionNotAllowed);
			} else if (subscriptionRequest.getResponse().equalsIgnoreCase(
					WebServiceConstants.UPGRADE_NOT_ALLOWED)) {
				task.setObject(param_response, Resp_UpgradeNotAllowed);
			} else
				task.setObject(param_response, Resp_Failure);

		} catch (Exception e) {
			logger.error("Exception in processing processBaseUpgradationRequest >"
					+ e.getMessage());
			task.setObject(param_response, Resp_Failure);
		}
	}

	// RBT-14301: Uninor MNP changes.
	protected boolean validateCircleIdParam(Task task, Subscriber subscriber) {
		// RBT-14301: Uninor MNP changes.
		String circleIdInReq = "";
		boolean validateMNPCheck = true;
		Map<String, String> circleIdMap = null;
		if (task.containsKey(param_CIRCLE_ID)) {
			circleIdInReq = task.getString(param_CIRCLE_ID);
			logger.info("param_CIRCLE_ID: " + circleIdInReq);
			String strCircleIdMap = getParamAsString(COMMON,
					"CIRCLEID_MAPPING_FOR_THIRD_PARTY", null);
			logger.info("strCircleIdMap: " + strCircleIdMap);
			circleIdMap = MapUtils.convertToMap(strCircleIdMap, ";", ":", null);
			if (circleIdInReq.trim().isEmpty()) {
				validateMNPCheck = true;
			} else {
				if (subscriber == null
						|| (subscriber.getStatus()
								.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
						|| subscriber.getStatus().equalsIgnoreCase(
								WebServiceConstants.NEW_USER)) {
					logger.info("newuser circleId maping value from parameter : "
							+ circleIdMap.containsKey(circleIdInReq));
					if (!circleIdMap.isEmpty()) {
						task.setObject(
								param_CIRCLE_ID,
								(circleIdMap.containsKey(circleIdInReq)) ? circleIdMap
										.get(circleIdInReq) : circleIdInReq);
					}
					validateMNPCheck = false;
				} else {
					String subscriberCircleId = subscriber.getCircleID();
					if (!circleIdMap.isEmpty()
							&& circleIdMap.containsKey(circleIdInReq)) {
						circleIdInReq = circleIdMap.get(circleIdInReq);
						logger.info("activeUser circleId  from parameter : "
								+ circleIdMap.containsKey(circleIdInReq));
					}
					validateMNPCheck = (subscriberCircleId != null && subscriberCircleId
							.equalsIgnoreCase(circleIdInReq)) ? true : false;
					logger.info("activeUser circleId  from table : "
							+ subscriberCircleId);
					if (!validateMNPCheck) {
						task.setObject(param_response, Resp_invalidParam);
						logger.info("RBT:: Invalid CIRCLE_ID For Subscriber. response: "
								+ "INVALID"
								+ " & param_response: "
								+ task.getString(param_response));
					}
				}
			}
		}
		return validateMNPCheck;
	}

	//Added a new method for deactivate tone
	public void processDeleteTone(Task task) {
		String toneId = task.getString(param_TONE_ID);
		String categoryId = task.getString(param_CATEGORY_ID);
		String wavFileName=task.getString(param_wavfile);
		Clip clip = null;
		Category category = null;
		//checks for category id if it is null then it will throw an internal error
		if (categoryId == null) {
			task.setObject(param_response, Resp_InternalErr);
		} else {
			if (toneId != null) {
				//checks for clip object & category object with the given promo id
				clip = rbtCacheManager.getClipByPromoId(toneId);
				category = rbtCacheManager.getCategoryByPromoId(toneId);
				if (clip == null) {
					//if the clip object is null with the given promo id then it will check with tone id
					clip = rbtCacheManager.getClip(toneId);
				}
			}
			if (clip == null) {
				if(wavFileName!=null)
				//if the clip object is null with the given tone id and promo id then it will check with wavfilename
				clip = rbtCacheManager.getClipByRbtWavFileName(wavFileName);
			}
			if (category == null) {
				//if the category object is null with the given promo id then it will check with category id
				category = rbtCacheManager.getCategory(Integer
						.parseInt(categoryId));
			}
			if (category == null) {
				//if the category object is still null then it will throw an internal error
				task.setObject(param_response, Resp_InternalErr);
			} else {
				SelectionRequest selection = new SelectionRequest(
						task.getString(param_MSISDN));
				String mode = task.getString(param_DEACTIVATED_BY);
				selection.setMode(mode);
				if (clip != null) {
					selection.setClipID(clip.getClipId() + "");
					task.setObject(param_WAV_FILE, clip.getClipRbtWavFile());
				}
				if (category != null) {
					selection.setCategoryID(category.getCategoryId() + "");
					task.setObject(param_CATEGORY_ID, categoryId);
				}
				selection.setSubscriberID(task.getString(param_MSISDN));
				rbtClient.deleteSubscriberDownload(selection);
				task.remove(param_library);
				setDeleteToneResponse(selection.getResponse(), task);
			}
		}
	}

	private void setDeleteToneResponse(String response, Task task) {
		String finalResponse = response;
		if (response.equalsIgnoreCase(WebServiceConstants.FAILED)) {
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					task.getString(param_MSISDN));
			Library library = RBTClient.getInstance().getLibraryHistory(
					rbtDetailsRequest);
			Downloads downloads = library.getDownloads();
			Download[] downloadArr = null;
			if(downloads!=null){
				downloadArr=downloads.getDownloads();
			}
			Download download=null;
			if(downloadArr!=null){
			for (int i = 0; i < downloadArr.length; i++) {
					if (task.getString(param_WAV_FILE) != null
							&& task.getString(param_CATEGORY_ID) != null
							&& downloadArr[i]
									.getRbtFile()
									.replace(".wav", "")
									.equalsIgnoreCase(
											task.getString(param_WAV_FILE))
							&& downloadArr[i].getCategoryID() == Integer
									.parseInt(task.getString(param_CATEGORY_ID))) {
						download = downloadArr[i];
						break;
					}  else if (task.getString(param_WAV_FILE) == null
							&& task.getString(param_CATEGORY_ID) != null
							&& downloadArr[i].getCategoryID() == Integer
									.parseInt(task.getString(param_CATEGORY_ID))) {
						download = downloadArr[i];
						break;
					}
				}
			}
			if(download!=null){
				if (download.getDownloadStatus().equalsIgnoreCase(
						WebServiceConstants.DEACT_PENDING)) {
					finalResponse = Resp_DeactPending;
				} else if (download.getDownloadStatus()
						.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {
					finalResponse = Resp_Already_Deactive;
				} else
					finalResponse = Resp_SelNotExists;
			}
			else{
				finalResponse = Resp_SelNotExists;
			}

		}
		else if (response.equals(WebServiceConstants.SUCCESS))
			finalResponse = Resp_Success;
		task.setObject(param_response, finalResponse);
	}
	
	// Checking if subscriber is CDT / NDT user and not PreConsent BaseSel request 
	//then we are chaninging mode with mapping value
	private String checkCdtUserNoConsentModeMap(Task task, Subscriber subscriber,String activatedBy){
		boolean isActiveUDSUser = false;
		boolean isCDTNDTUser = false;
		if(subscriber !=null){
			String isUdsUserOptInTrue = com.onmobile.apps.ringbacktones.webservice.common.Utility.isUDSUser(subscriber.getUserInfoMap(), false);
			if(isUdsUserOptInTrue!=null){
				isActiveUDSUser = true;
			}
			
			if(subscriber.getStatus() != null && subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.ACTIVE)){
				isCDTNDTUser =  com.onmobile.apps.ringbacktones.Gatherer.Utility.isUserCDTNDT(subscriber.getCosID());	
			}
			Boolean isPreConsentBaseSelRequest = (Boolean) task.getObject(WebServiceConstants.param_isPreConsentBaseSelRequest);
			boolean isAllCaller = true ;
			if (task.containsKey(param_CALLER_ID)) {
				String callerId = task.getString(param_CALLER_ID);
				isAllCaller = ((callerId == null || callerId.equalsIgnoreCase(WebServiceConstants.ALL)) ? true : false);
			}
			if(isCDTNDTUser && !isActiveUDSUser && isAllCaller && !(isPreConsentBaseSelRequest!=null && isPreConsentBaseSelRequest)){
				String cdtUsesrNoConsentModeMapStr = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.WEBSERVICE,"CDT_USER_NO_CONSENT_MODE_MAP", "");
				Map<String,String> cdtUsesrNoConsentModeMap = MapUtils.convertIntoMap(cdtUsesrNoConsentModeMapStr, ";", "=", ",");
				// reinitialize mode/activatedBy with mapped value
				if(cdtUsesrNoConsentModeMap.containsKey(activatedBy) && cdtUsesrNoConsentModeMap.get(activatedBy)!=null)
					activatedBy = cdtUsesrNoConsentModeMap.get(activatedBy);
			}
		}
		return activatedBy;
	}
}