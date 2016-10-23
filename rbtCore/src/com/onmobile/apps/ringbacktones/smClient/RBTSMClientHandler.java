package com.onmobile.apps.ringbacktones.smClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.prism.client.SMClient;
import com.onmobile.prism.client.core.SMComboRequest;
import com.onmobile.prism.client.core.SMConstants;
import com.onmobile.prism.client.core.SMException;
import com.onmobile.prism.client.core.SMGiftRequest;
import com.onmobile.prism.client.core.SMPrechargeRequest;
import com.onmobile.prism.client.core.SMRequest;
import com.onmobile.prism.client.core.SMResponse;
import com.onmobile.prism.client.core.SMUpgradeRequest;

/**
 * This is a singleton class that will communicate with SM Client and gets the relevant data. The
 * class also submits the user requests to SM Client regarding activation, deactivation, new
 * selections etc
 * 
 * @author Sreekar
 * @since 2009-11-04
 */

public class RBTSMClientHandler {
	// All constants
	public static final String REF_ID = "REF_ID";

	public static final String CLIP = "clip";
	public static final String SUB_XTRA_INFO = "subXtraInfo";
	public static final String PRE_CHARGE = "preCharge";

	public static final String SEL_INFO_SONGNAME = "songname";
	public static final String SEL_INFO_SONGCODE = "songcode";
	public static final String SEL_INFO_SONGTYPE = "songtype";
	public static final String SEL_INFO_SONGTYPE_UGC = "UGC";
	public static final String SEL_INFO_MOVIENAME = "moviename";
	public static final String SEL_INFO_CALLER = "cli";
	public static final String SEL_INFO_COSID = "cosid";

	public static final String EXTRA_PARAM_USERINFO = "userInfo";
	public static final String EXTRA_PARAM_LINKED_USERINFO = "linkedUserInfo";

	private static final Logger logger = Logger.getLogger(RBTSMClientHandler.class);
	private static final Object _syncObj = new Object();
	private static SMClient _smClient = null;
	private static RBTSMClientHandler _handler = null;

	private static Map<String, Set<String>> offerTypeBlockedModesMap = null;
	/**
	 * The constructor for the class which will instantiate the SMClient object.
	 * 
	 * @author Sreekar
	 * @throws RBTException - if not able to instantiate SMClient object
	 * @see SMClient
	 */
	private RBTSMClientHandler() throws RBTException {
		try {
			_smClient = SMClient.getInstance();
		}
		catch (SMException e) {
			logger.error("Exception from SMClient", e);
			throw new RBTException("Couldn't instantiate SMClient");
		}
	}

	/**
	 * This is the method which will create the singleton instance for the RBTSMClientHandler
	 * 
	 * @author Sreekar
	 * @throws RBTException - If not able to create the singleton instance for any reason
	 */
	public static RBTSMClientHandler getInstance() throws RBTException {
		if (_handler == null || _smClient == null) {
			synchronized (_syncObj) {
				if (_handler == null)
					_handler = new RBTSMClientHandler();
			}
		}

		if (_handler == null)
			throw new RBTException("Couldn't initialise RBTSMClientHandler");

		return _handler;
	}

	/**
	 * @param subID - MSISDN for which the offer is to be presented
	 * @param channel - channel through which user is accessing the service, for each channel
	 *            provided by the UI we need to pass a mode to SM which will be used for reporting
	 *            purpose
	 * @param offerType - offer type subscription/selection/combo
	 * @param userType - subscriber type p - prepaid, b - postpaid. Pass some default value if you
	 *            dont know the type
	 * @param srvKey - This determines what kind of offer to be presented like RBT_ACT for
	 *            subscription offer, RBT_SEL for selection offers
	 * @return array of Offer objects to be presented to the user
	 * @author Sreekar
	 */
	@SuppressWarnings("unchecked")
	public Offer[] getOffer(String subID, String channel, int offerType, String userType,
			HashMap<String, String> extraInfo) {
		return getOffer(subID, channel, offerType, userType, null, extraInfo);
	}
	
	/**
	 * @param subID - MSISDN for which the offer is to be presented
	 * @param channel - channel through which user is accessing the service, for each channel
	 *            provided by the UI we need to pass a mode to SM which will be used for reporting
	 *            purpose
	 * @param offerType - offer type subscription/selection/combo
	 * @param userType - subscriber type p - prepaid, b - postpaid. Pass some default value if you
	 *            dont know the type
	 * @param srvKey - This determines what kind of offer to be presented like RBT_ACT for
	 *            subscription offer, RBT_SEL for selection offers
	 * @param classType - This can either be subscriptionClass or chargeClass. if we pass this parameter
	 * 			to SMClient the offers will be based on the classTye too
	 * @return array of Offer objects to be presented to the user
	 * @author Sreekar
	 */
	@SuppressWarnings("unchecked")
	public Offer[] getOffer(String subID, String channel, int offerType, String userType, String classType,
			HashMap<String, String> extraInfo) {
		subID = RBTDBManager.getInstance().subID(subID);
		ArrayList<Offer> allOffers = new ArrayList<Offer>();
		try {
			String srvKey = "RBT_ACT";
			String requestType = "activation";
			if(offerType == Offer.OFFER_TYPE_SUBSCRIPTION) {
				
				boolean isValidateNextBillingDate = RBTParametersUtils.getParamAsBoolean(
						"COMMON", "VALIDATE_NEXT_BILLING_DATE_FOR_OFFERS", "false");
				if(isValidateNextBillingDate) {
					
					if (isBillDateNotPassed(subID)) {
						logger.info("Returning null, subscriber bill date "
								+ "is not passed. subscriberId: " + subID);
						return null;
					} else {
						logger.info("Subscriber bill date is passed, so availing offer. subscriberId: "
								+ subID);
					}
				} 
				
			} else if (offerType == Offer.OFFER_TYPE_SELECTION)
				srvKey = "RBT_SEL";
			else if (offerType == Offer.OFFER_TYPE_ADVANCE_RENTAL)
				requestType = "upgradation";
			else if (offerType == Offer.OFFER_TYPE_CHURN)
				requestType = "deactivation";
			else if (offerType == Offer.OFFER_TYPE_PACK)
				srvKey = "RBT_PACK";
			
			// for RRBT offers the request should be made with RRBT_ACT/RRBT_SEL
			if (RBTDeploymentFinder.isRRBTSystem()) {
				srvKey = srvKey.replaceAll("RBT", "RRBT");
			}
			
			// append the classType to srvKey to obtain the actual srvKey
			srvKey += (classType == null?"":"_"+classType);
			if (logger.isDebugEnabled()) {
				logger.debug("RBT::offer parameters->subID:" + subID + "|srvKey:" + srvKey
					+ "|userType:" + userType + "|mode:" + channel + "|channel:" + channel
					+ "|extraInfo:" + extraInfo + "|group:RBT|requestType:" + requestType);
			}
			
			//Get subscriberdetail object
			WebServiceContext task = new WebServiceContext();
			task.put(WebServiceConstants.param_subscriberID, subID);
			task.put(WebServiceConstants.param_mode, channel);			
			SubscriberDetail subscriber = DataUtils.getSubscriberDetail(task);
			String circleId = null;
			if(subscriber != null && subscriber.getCircleID() != null) {
				circleId = subscriber.getCircleID();
			}
			
			if(extraInfo == null) {
				extraInfo = new HashMap<String, String>();
			}
			if(circleId != null) {
				extraInfo.put("siteid",circleId);
			}
			
			long startTime = System.currentTimeMillis();
			ArrayList allSMOffers = _smClient.getOffers(subID, srvKey, userType, channel, channel,
					extraInfo, "RBT", requestType);
			logger.info("Invoked the SM Client API for getOffer subID:" + subID + "|srvKey:" + srvKey + " Time taken  "
					+ (System.currentTimeMillis() - startTime) + " ms");
			
			logger.info("Offer::::" + allSMOffers);

			/*
			 * Filtering some of the offer objects based on offerType returned
			 * by SM and the mode from which offers are requested.
			 */
			if (offerTypeBlockedModesMap == null)
			{
				offerTypeBlockedModesMap = new HashMap<String, Set<String>>();
				String offerTypesBlockedModesStr = RBTParametersUtils.getParamAsString("COMMON", "OFFER_TYPES_BLOCKED_MODES_MAP", "");
				logger.info("Debug11111:::" + offerTypesBlockedModesStr);
				if((offerTypesBlockedModesStr = offerTypesBlockedModesStr.trim()).length() != 0) {
					String[] offerTypeConfigs = offerTypesBlockedModesStr.split(";");
					for (String eachOfferTypeConfig : offerTypeConfigs)
					{
						String[] offerTypeModesTokens = eachOfferTypeConfig.split(":");
						offerTypeBlockedModesMap.put(offerTypeModesTokens[0].trim(), new HashSet<String>(Arrays.asList(offerTypeModesTokens[1].trim().split(","))));
					}
				}
			}

			if (allSMOffers != null && allSMOffers.size() > 0) {
				for (int i = 0; i < allSMOffers.size(); i++)
				{
					boolean isOfferAllowed = true;
					Offer offer = new Offer((com.onmobile.prism.client.core.Offer) allSMOffers.get(i), offerType);
					logger.debug("RBT::Checking for offer - " + offer);
					if (offer.getSmOfferType() != null)
					{
						Set<String> blockedModesSet = offerTypeBlockedModesMap.get(offer.getSmOfferType());
						if (blockedModesSet != null && blockedModesSet.contains(channel))
						{
							isOfferAllowed = false;
						}
					}

					if (isOfferAllowed) {
						allOffers.add(new Offer((com.onmobile.prism.client.core.Offer) allSMOffers
								.get(i), offerType));
					}
				}
			}
			else
				logger.debug("RBT::didn't get any offers from SM subID:" + subID + "|srvKey:" + srvKey);
		}
		catch (SMException e) {
			logger.error("SMClient threw exception while getting offers", e);
		}
		catch (Exception e) {
			logger.error("Exception getting offers", e);
		}
		
		if ("ccc".equalsIgnoreCase(channel)) {
			ArrayList<Offer> cccOffers = getCCCOffers(subID, offerType, userType, extraInfo);
			if (cccOffers != null && cccOffers.size() > 0) {
				if (allOffers == null || allOffers.size() < 1)
					allOffers = cccOffers;
				else
					allOffers.addAll(cccOffers);
			}
		}
		logger.debug("RBT::total offers presenting -> " + allOffers.size());
		if (allOffers == null || allOffers.size() < 1)
			return null;

		return allOffers.toArray(new Offer[0]);
	}
	
	
	/*
	 * API will hit SDP through Prism to get packagename (srvkey) and amount.
	 * This API only for Subscription.
	 */
	public Offer[] getPackageOffer(String subID, String offerId, int offerType, String clipId, String classType,String  mode) {
		subID = RBTDBManager.getInstance().subID(subID);
		ArrayList<Offer> allOffers = new ArrayList<Offer>();
		try {
			String srvKey = "RBT_ACT_DEFAULT";
			
			
			if(offerType == Offer.OFFER_TYPE_SELECTION) {
			   srvKey = "RBT_SEL";
//			   Clip clip =  RBTCacheManager.getInstance().getClip(clipId);
//			   if(clip!=null){
//				   String classType = clip.getClassType();
//				   if(classType!=null)
//					   srvKey = srvKey +"_" + classType;  
//			   }
			   if(classType != null)
				   srvKey = srvKey +"_" + classType;
			} else if (offerType == Offer.OFFER_TYPE_SUBSCRIPTION) {
				srvKey = "RBT_ACT";
				if (classType != null){
					srvKey = srvKey + "_" + classType;
				} else{
					srvKey = "RBT_ACT_DEFAULT";
				}
			}
			
			String requestType = "activation";
			
			ArrayList allSMOffers = null;			
			
			HashMap<String,String> keyMap = new HashMap<String, String>();
			keyMap.put("isPackageRequest", "true");
			if(clipId != null) {
				keyMap.put("assetID", clipId);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("RBT::offer parameters->subID:" + subID + "|srvKey:" + srvKey
					+ "|group:RBT|requestType:" + requestType + "|clipId:" + clipId + "|mapKeys: " + keyMap+"|offerType: "+offerType);
			}
			long startTime = System.currentTimeMillis();
			
			
			
			//RBT-17431
			//Passing mode for Offer.do to hit PRISM
			allSMOffers = _smClient.getOffers(subID, srvKey, null, mode, mode, keyMap, "RBT", requestType); 
			
//			allSMOffers = _smClient.getOffers(subID, srvKey, offerId, true, clipId);
			logger.info("Invoked the SM Client API for getOffer subID:" + subID + "|srvKey:" + srvKey + " Time taken  "
					+ (System.currentTimeMillis() - startTime) + " ms");
			
			for(int i=0; allSMOffers!=null && i < allSMOffers.size(); i++) {
				allOffers.add(new Offer((com.onmobile.prism.client.core.Offer) allSMOffers
						.get(i), offerType));
			}

			if(allOffers.size() == 0)
				logger.debug("RBT::didn't get any offers from SM subID:" + subID + "|srvKey:" + srvKey);
		}
		catch (SMException e) {
			String message = e.getMessage();
			if (message != null	&& message.equalsIgnoreCase(SMConstants.MSG_TECHINICAL_ERROR)
					&& Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"MODES_FOR_TECHNICAL_ERROR_OFFER", "").split(",")).contains(mode)) {
				if(allOffers.size() == 0){
					Offer offer = new Offer();
					offer.setOfferID("-1");
					allOffers.add(offer); 
				}
			}
			logger.error("SMClient threw exception while getting offers", e);
		}
		catch (Exception e) {
			logger.error("Exception getting offers", e);
		}
		
		logger.debug("RBT::total offers presenting -> " + allOffers.size());
		if (allOffers == null || allOffers.size() < 1)
			return null;

		return allOffers.toArray(new Offer[0]);		
	}

	public ArrayList<Offer> getCCCOffers(String subID, int offerType, String userType,
			HashMap<String, String> extraInfo) {
		ArrayList<Offer> cccOffers = new ArrayList<Offer>();
		// TODO get any special offers for CCC if needed
		return cccOffers;
	}

	public Offer[] getSubscriptionOffer(String subID, String channel, String userType,
			HashMap<String, String> extraInfo) {
		return getOffer(subID, channel, Offer.OFFER_TYPE_SUBSCRIPTION, userType, extraInfo);
	}

	public Offer[] getSelectionOffer(String subID, String channel, String userType,
			HashMap<String, String> extraInfo) {
		return getOffer(subID, channel, Offer.OFFER_TYPE_SELECTION, userType, extraInfo);
	}

	// public Offer[] getComboOffer(String subID, String channel, String userType,
	// HashMap<String,String> extraInfo) {
	// return getOffer(subID, channel, Offer.OFFER_TYPE_COMBO, userType, extraInfo);
	// }

	/**
	 * @param subID
	 * @param channel
	 * @param userType
	 * @param offerTypes int array of offer types
	 * @param classType
	 * @return returns array of offers with different types
	 */
	public Offer[] getMultipleTypeOffers(String subID, String channel, String userType,
			Integer[] offerTypes, HashMap<String, String> extraInfo, String classType) {
		if (offerTypes == null)
			return null;
		ArrayList<Offer> diffTypeOffers = new ArrayList<Offer>();
		for (int i = 0; i < offerTypes.length; i++) {
			Offer[] offers = getOffer(subID, channel, offerTypes[i], userType, classType, extraInfo);
			addOffersArrayToArrayList(offers, diffTypeOffers);
		}
		return diffTypeOffers.toArray(new Offer[0]);
	}
	
	/**
	 * @param subID
	 * @param channel
	 * @param userType
	 * @param offerTypes int array of offer types
	 * @return returns array of offers with different types
	 */
	public Offer[] getMultipleTypeOffers(String subID, String channel, String userType,
			Integer[] offerTypes, HashMap<String, String> extraInfo) {
		return getMultipleTypeOffers(subID, channel, userType, offerTypes, extraInfo, null);
	}

	private void addOffersArrayToArrayList(Offer[] array, ArrayList<Offer> arrayList) {
		if (array == null || arrayList == null)
			return;
		for (int i = 0; i < array.length; i++)
			arrayList.add(array[i]);
	}

	/**
	 * Submits the activation request to SM Client
	 * 
	 * @param subscriberID subscriber msisdn
	 * @param isPrepaid true if user is a prepaid subscriber & false if postpaid
	 * @param subClass subscription class as provided by SMClient or pass null
	 * @param offerID offer id as obtained by SM Client or null if we want to enforce a subscription
	 *            class
	 * @param mode mode through which user activated
	 * @param extraParams extra parameters if something needs to be passed
	 * @return Returns RBTSMClientResponse object. Response in this object will be SUCCESS if the
	 *         response from SM client is success, FAILURE if we get error/exception, ERROR if we
	 *         get non retryable error.
	 * @see RBTSMClientResponse
	 */
	public RBTSMClientResponse activateSubscriber(String subscriberID, boolean isPrepaid,
			String subClass, String offerID, String mode, HashMap<String, String> extraParams,
			boolean isBulkTask) {
		if (!isBulkTask && checkPreChargedActivation(mode))
			return activatePreChargedSubscriber(subscriberID, isPrepaid, subClass, offerID, mode,
					extraParams);
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_ACT_"
				+ subClass, mode);
		if (offerID != null)
			smRequest.setOfferid(offerID);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			if (!isBulkTask) {
				smResponse = _smClient.subscribe(smRequest);
			}
			else {
				smResponse = _smClient.bulkSubscribe(smRequest);
			}
			logger.info("Invoked the SMClient for subscriber Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SMClient for subscriber");			
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	/**
	 * Submits the pre-charged activation request to SM Client
	 * 
	 * @param subscriberID subscriber msisdn
	 * @param isPrepaid true if user is a prepaid subscriber & false if postpaid
	 * @param subClass subscription class as provided by SMClient or pass null
	 * @param offerID offer id as obtained by SM Client or null if we want to enforce a subscription
	 *            class
	 * @param mode mode through which user activated
	 * @param extraParams extra parameters if something needs to be passed
	 * @return Returns RBTSMClientResponse object. Response in this object will be SUCCESS if the
	 *         response from SM client is success, FAILURE if we get error/exception, ERROR if we
	 *         get non retryable error.
	 * @see RBTSMClientResponse
	 */
	public RBTSMClientResponse activatePreChargedSubscriber(String subscriberID, boolean isPrepaid,
			String subClass, String offerID, String mode, HashMap<String, String> extraParams) {
		SMPrechargeRequest smPrechargeRequest = new SMPrechargeRequest(subscriberID, "0",
				isPrepaid ? "p" : "b", "RBT_ACT_" + subClass, mode, true);
		if (offerID != null)
			smPrechargeRequest.setOfferid(offerID);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smPrechargeRequest);
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.preCharge(smPrechargeRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating pre charged subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	/**
	 * Sends a normal selection request to SM Client
	 * 
	 * @param subscriberID subscriber msisdn
	 * @param isPrepaid true if user is a prepaid subscriber & false if postpaid
	 * @param chargeClass charge class as provided by SMClient or pass null
	 * @param offerID offer id as obtained by SM Client or null if we want to enforce a charge class
	 * @param mode mode through which user made the selection
	 * @param refID unique UUID generated while for the selection/download
	 * @param extraParams HashMap of extra parameters if something needs to be passed, in this map
	 *            clip object has to be passed for all song selections the same needs to be sent to
	 *            SM Client for reporting
	 * @return
	 */
	public RBTSMClientResponse activateSelection(String subscriberID, boolean isPrepaid,
			String chargeClass, String offerID, String mode, String refID, HashMap<String, String> extraParams,
			boolean isBulkTask) {
		if (!isBulkTask && checkPreChargedSelection(mode))
			return activatePreChargedSelection(subscriberID, isPrepaid, chargeClass, offerID, mode,
					refID, extraParams);
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_SEL_"
				+ chargeClass, mode, refID);
		/*// setting content related info to send to SM for reporting reasons
		setSelInfo(smRequest, extraParams);*/
		if (offerID != null)
			smRequest.setOfferid(offerID);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			if (!isBulkTask)
				smResponse = _smClient.subscribe(smRequest);
			else {
				smResponse = _smClient.bulkSubscribe(smRequest);
			}
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error making a selection", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	private boolean sendPrecharged() {
		return CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON",
				"SEND_ACTIVE_EASY", "FALSE").getValue().equalsIgnoreCase("TRUE");
	}

	/**
	 * Checks if the activation is to be sent as pre-charged
	 * 
	 * @param mode mode through which the activation is made
	 * @return Returns true if the mode passed is in the pre-charged list
	 */
	private boolean checkPreChargedActivation(String mode) {
		if (sendPrecharged())
			return Arrays.asList(
					CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON",
							"ACTIVATED_PRE_CHRG", "").getValue().toLowerCase().split(","))
					.contains(mode.toLowerCase());
		return false;
	}

	/**
	 * Checks if the selection is to be sent as pre-charged
	 * 
	 * @param mode mode through which the selection is made
	 * @return Returns true if the mode is configured
	 */
	private boolean checkPreChargedSelection(String mode) {
		if (sendPrecharged())
			return Arrays.asList(
					CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON",
							"SELECTED_PRE_CHRG", "").getValue().toLowerCase().split(",")).contains(
					mode.toLowerCase());
		return false;
	}

	/**
	 * Sends a pre-charged charged selection request to SM Client
	 * 
	 * @param subscriberID subscriber msisdn
	 * @param isPrepaid true if user is a prepaid subscriber & false if postpaid
	 * @param chargeClass charge class as provided by SMClient or pass null
	 * @param offerID offer id as obtained by SM Client or null if we want to enforce a charge class
	 * @param mode mode through which user made the selection
	 * @param refID unique UUID generated while for the selection/download
	 * @param extraParams HashMap of extra parameters if something needs to be passed, in this map
	 *            clip object has to be passed for all song selections the same needs to be sent to
	 *            SM Client for reporting
	 */
	public RBTSMClientResponse activatePreChargedSelection(String subscriberID, boolean isPrepaid,
			String chargeClass, String offerID, String mode, String refID, HashMap<String, String> extraParams) {
		SMPrechargeRequest smPrechargeRequest = new SMPrechargeRequest(subscriberID, "0",
				isPrepaid ? "p" : "b", "RBT_SEL_" + chargeClass, mode, refID, true);
		if (offerID != null)
			smPrechargeRequest.setOfferid(offerID);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smPrechargeRequest);
//		setSelInfo(smPrechargeRequest, extraParams);
		
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.preCharge(smPrechargeRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error making a pre charged selection", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	/**
	 * This method sets the response string and message parameters in the response to be returned
	 * 
	 * @param smResponse SM client response
	 * @param response response to be returned to the application
	 */
	private static void setSMClientResponse(SMResponse smResponse, RBTSMClientResponse response) {
		String responseStr = RBTSMClientResponse.FAILURE;
		if (smResponse.isSuccess())
			responseStr = RBTSMClientResponse.SUCCESS;
		else if (!smResponse.isRetriable())
			responseStr = RBTSMClientResponse.ERROR;
		response.setResponse(responseStr);
		response.setmessage(smResponse.getMessage());
	}

	/**
	 * This method invokes the confirm charge API in SMClient. One place where this is used is in
	 * Airtel comes with music opt-in feature where user will confirm the continuation for the
	 * service.
	 * 
	 * @param subscriberID
	 * @param isPrepaid
	 * @param subscriptionClass
	 * @param mode
	 * @return
	 */
	public RBTSMClientResponse confirmCharge(String subscriberID, boolean isPrepaid, String srvKey,
			String mode, String refID) {
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", srvKey, mode);
		if (refID != null)
			smRequest.setReqRefId(refID);
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.confirmCharge(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error in confirm charge", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse confirmSubscription(String subscriberID, boolean isPrepaid,
			String subscriptionClass, String mode) {
		return confirmCharge(subscriberID, isPrepaid, "RBT_ACT_" + subscriptionClass, mode, null);
	}

	public RBTSMClientResponse confirmSelection(String subscriberID, boolean isPrepaid,
			String chargeClass, String mode, String refID) {
		return confirmCharge(subscriberID, isPrepaid, "RBT_SEL_" + chargeClass, mode, refID);
	}

	public RBTSMClientResponse deactivateSubscriber(String subscriberID, boolean isPrepaid,
			String mode, String subClass, HashMap<String, String> extraParams, boolean isBulkTask) {
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_ACT_"
				+ subClass, mode);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			if (!isBulkTask) {
				smResponse = _smClient.unsubscribe(smRequest);
			}
			else {
				smResponse = _smClient.bulkUnsubscribe(smRequest);
			}
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error deactivating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse deactivateSelection(String subscriberID, boolean isPrepaid,
			String mode, boolean isBulkTask, String refID, String chargeClass, HashMap<String, String> extraParams) {
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_SEL_"
				+ chargeClass, mode, refID);
		if(extraParams != null && extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			if (!isBulkTask) {
				smResponse = _smClient.unsubscribe(smRequest);
			}
			else {
				smResponse = _smClient.bulkUnsubscribe(smRequest);
			}
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error deactivating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse upgradeSubscription(String subscriberID, boolean isPrepaid,
			String mode, String oldSubClass, String newSubscriptionClass, String offerID,
			String oldOfferID, HashMap<String, String> extraParams) {
		SMUpgradeRequest smRequest = new SMUpgradeRequest(subscriberID, "0", isPrepaid ? "p" : "b",
				"RBT_ACT_" + oldSubClass, mode, "RBT_ACT_" + newSubscriptionClass, null, true);
		RBTSMClientResponse response = new RBTSMClientResponse();
		if(extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		try {
			smRequest.setOfferid(offerID);
			smRequest.setOldOfferId(oldOfferID);
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.upgradeService(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error upgrading subscription", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public Offer[] getOffer(String subID, String channel, int offerType, String userType) {
		return getOffer(subID, channel, offerType, userType, new HashMap<String, String>());
	}

	public RBTSMClientResponse comboRequest(String subscriberID, boolean isPrepaid, String mode,
			String chargeClass, String baseChargeClass, String offerID, String baseOfferID,
			HashMap<String, String> extraParams, boolean isBulkTask, String refID) {
		if (!isBulkTask && checkPreChargedSelection(mode))
			return preChargedCombo(subscriberID, isPrepaid, mode, chargeClass, baseChargeClass,
					offerID, baseOfferID, extraParams, refID);
		SMComboRequest smRequest = new SMComboRequest(subscriberID, "0", isPrepaid ? "p" : "b",
				"RBT_SEL_" + chargeClass, mode, false, "RBT_ACT_" + baseChargeClass, null, null);
		smRequest.setReqRefId(refID);
		if (offerID != null)
			smRequest.setOfferid(offerID);
		if (baseOfferID != null)
			smRequest.setLinkeofferid(baseOfferID);
		if(extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		if(extraParams.containsKey(EXTRA_PARAM_LINKED_USERINFO))
			setLinkedUserInfo(extraParams.get(EXTRA_PARAM_LINKED_USERINFO), smRequest);
		
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			if (!isBulkTask) {
				smResponse = _smClient.subscribeCombo(smRequest);
			}
			else {
				smResponse = _smClient.bulkSubscribeCombo(smRequest);
			}
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse preChargedCombo(String subscriberID, boolean isPrepaid, String mode,
			String chargeClass, String baseChargeClass, String offerID, String baseOfferID,
			HashMap<String, String> extraParams, String refID) {
		SMPrechargeRequest smRequest = new SMPrechargeRequest(subscriberID, "0", isPrepaid ? "p"
				: "b", "RBT_SEL_" + chargeClass, mode, false, "RBT_ACT_" + baseChargeClass, null,
				null);
		smRequest.setReqRefId(refID);
		if (offerID != null)
			smRequest.setOfferid(offerID);
		if (baseOfferID != null)
			smRequest.setLinkeofferid(baseOfferID);
		if(extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		if(extraParams.containsKey(EXTRA_PARAM_LINKED_USERINFO))
			setLinkedUserInfo(extraParams.get(EXTRA_PARAM_LINKED_USERINFO), smRequest);
		
		RBTSMClientResponse response = new RBTSMClientResponse();
		try {
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.preChargeCombo(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;

	}

	public RBTSMClientResponse renewSelection(String subscriberID, boolean isPrepaid,
			String subClass, String offerID, String mode, String refID) {
		RBTSMClientResponse response = new RBTSMClientResponse();
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_SEL_"
				+ subClass, mode, refID);
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			smResponse = _smClient.renewalTrigger(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse suspendSubscription(String subscriberID, boolean isPrepaid, String subClass,
			String mode) {
		RBTSMClientResponse response = new RBTSMClientResponse();
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_ACT_"
				+ subClass, mode,null);
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			smResponse = _smClient.suspend(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}
	public RBTSMClientResponse suspendSelections(String subscriberID, boolean isPrepaid, String classType,
			String mode,String refId) {
		RBTSMClientResponse response = new RBTSMClientResponse();
		SMRequest smRequest = new SMRequest(subscriberID, "0", isPrepaid ? "p" : "b", "RBT_SEL_"
				+ classType, mode,refId);
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			smResponse = _smClient.suspend(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}

	public RBTSMClientResponse chargeGift(String subscriberID, boolean isPrepaid, String subClass,
			String mode, String gifteeId, String gifteeStatus, String chargeClass, String refId,
			String baseOfferId, String selOfferId, Hashtable<String, String> userInfo) {

		subClass = "RBT_ACT_" + subClass;
		chargeClass = "RBT_SEL_" + chargeClass + "_GIFT";
		RBTSMClientResponse response = new RBTSMClientResponse();
		SMGiftRequest smRequest = new SMGiftRequest(subscriberID, "0", isPrepaid ? "p" : "b",
				subClass, mode, gifteeId, "0", gifteeStatus, chargeClass, null);
		if (selOfferId != null) {
			smRequest.setOfferid(selOfferId);
		}
		if (baseOfferId != null) {
			smRequest.setLinkedOfferid(baseOfferId);
		}
		if (refId != null) {
			smRequest.setReqRefId(refId);
		}
		smRequest.setUserinfo(userInfo);
		try {
			SMResponse smResponse = null;
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			smResponse = _smClient.chargeGift(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error activating subscriber", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}
	
	private void setUserInfo(String userInfoStr, SMRequest smRequest) {
		if(userInfoStr == null)
			return;
		Hashtable<String, String> userInfo = getUserInfo(userInfoStr);
		smRequest.setUserinfo(userInfo);
	}
	
	private void setLinkedUserInfo(String userInfoStr, SMComboRequest smRequest) {
		if(userInfoStr == null)
			return;
		smRequest.setLinkedUserInfo(userInfoStr);
	}
	
	private Hashtable<String, String> getUserInfo(String userInfoStr) {
		StringTokenizer stk = new StringTokenizer(userInfoStr, "|");
		Hashtable<String, String> userInfo = new Hashtable<String, String>();
		while(stk.hasMoreTokens()) {
			String thisToken = stk.nextToken();
			int index = thisToken.indexOf(":");
			if(index == -1)
				continue;
			userInfo.put(thisToken.substring(0, index), thisToken.substring(index+1));
		}
		return userInfo;
	}

	public RBTSMClientResponse upgradeSelection(String subscriberID, boolean isPrepaid,
			String mode, String oldSubClass, String newSubscriptionClass, String offerID,
			String oldOfferID, HashMap<String, String> extraParams) {
		SMUpgradeRequest smRequest = new SMUpgradeRequest(subscriberID, "0", isPrepaid ? "p" : "b",
				"RBT_SEL_" + oldSubClass, mode, "RBT_SEL_" + newSubscriptionClass, null, true);
		RBTSMClientResponse response = new RBTSMClientResponse();
		if(extraParams.containsKey(EXTRA_PARAM_USERINFO))
			setUserInfo(extraParams.get(EXTRA_PARAM_USERINFO), smRequest);
		try {
			smRequest.setOfferid(offerID);
			smRequest.setOldOfferId(oldOfferID);
			logger.info("Before Invoke the SM Client API ");
			long startTime = System.currentTimeMillis();
			SMResponse smResponse = _smClient.upgradeService(smRequest);
			logger.info("Invoked the SM Client API Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API ");
			if (smResponse != null)
				setSMClientResponse(smResponse, response);
		}
		catch (SMException e) {
			logger.error("RBT::Error upgrading subscription", e);
		}
		logger.info("RBT::returning response -> " + response);
		return response;
	}
	
	public Offer[] getOffer(String group, String offerID) 
	{
		Offer[] offers = new Offer[1];
		try {
			offers[0] = new Offer(_smClient.getOffer(group, offerID));
		}
		catch(SMException sme) {
			logger.error("RBT::Error upgrading subscription", sme);
		}
		return offers;
	}
	
	@SuppressWarnings("unchecked")
	public Offer[] getOffers(String subscriberID, String group, String offerID) 
	{
		List<Offer> allOffers = new ArrayList<Offer>();
		try {
			long startTime = System.currentTimeMillis();
			ArrayList allSMOffers = _smClient.getOffers(subscriberID, group, offerID);
			logger.info("Invoked the SM Client API for getOffer Time taken  " + (System.currentTimeMillis() - startTime) + " ms");
			logger.info("Invoked the SM Client API for getOffers with arguments msisdn : " + subscriberID + "offerID : " + offerID);
			if (allSMOffers != null && allSMOffers.size() > 0) {
				for (int i = 0; i < allSMOffers.size(); i++)
					allOffers.add(new Offer((com.onmobile.prism.client.core.Offer) allSMOffers.get(i)));
			}
			else
				logger.debug("RBT::didn't get any offers from SM");
		}
		catch(SMException sme) {
			logger.error("RBT::Error upgrading subscription", sme);
		}
		
		if (allOffers.size() < 1)
			return null;

		return allOffers.toArray(new Offer[0]);
	}
	
	protected static boolean isBillDateNotPassed(String subscriberID) {
		logger.info("Validating subscriber next billing date. "
				+ "subscriberId: " + subscriberID);
		try {
			HashMap<String, String> resultMap = Utility
					.getNextBillingDateOfServices(subscriberID);
			String nextBillingDate = resultMap.get(subscriberID);

		
			if (null != nextBillingDate) {
				Date date = new Date();
				SimpleDateFormat rbtDateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				Date nextBillingDateObj = rbtDateFormat.parse(nextBillingDate);
				boolean isNextBillingDateAfterCurDate = nextBillingDateObj.after(date);
				logger.info("Received next billing date: " + nextBillingDateObj
						+ ", subscriberId: " + subscriberID
						+ ", isNextBillingDateAfterCurDate: "
						+ isNextBillingDateAfterCurDate);
				logger.warn("Returning isNextBillingDateAfterCurDate: "
						+ isNextBillingDateAfterCurDate
						+ ", for subscriberId: " + subscriberID);
				return isNextBillingDateAfterCurDate;

			} else {
				logger.warn("Return true, since nextBillDate is null,"
						+ " subscriberId: " + subscriberID);
				// Next billing date is not available, so subscriber is new subscriber.
			}
		} catch (Exception e) {
			logger.error(
					"Unable to validate next billing date. Exception: "
							+ e.getMessage(), e);
		}
		logger.info("Returning false, subscriberId: "+subscriberID);
		return false;
	}
}