package com.onmobile.apps.ringbacktones.rbt2.processor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.onmobile.apps.ringbacktones.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberImpl;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus;
import com.onmobile.apps.ringbacktones.rbt2.db.ISubscriber;
import com.onmobile.apps.ringbacktones.rbt2.processor.ISubscriptionProcessor;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO.OfferType;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTOBuilder;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.OfferMemcache;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * 
 * @author md.alam
 *
 */

@SuppressWarnings("unchecked")
public abstract class SubscriptionAbstractProcessor implements ISubscriptionProcessor,WebServiceConstants,Constants {

	private static Logger logger = Logger.getLogger(SubscriptionAbstractProcessor.class);

	@Autowired
	private OfferMemcache offerMemcache;
	
	RBTDaemonManager rbtDaemonManager = null;

	public SubscriptionAbstractProcessor() {
		RBTDaemonManager.m_rbtDaemonManager = new RBTDaemonManager();
		rbtDaemonManager = RBTDaemonManager.m_rbtDaemonManager;
		rbtDaemonManager.getConfigValues();
	}

	/**
	 * MSISDN validation is done with prefix and MNP check.
	 * If request is from new or deactivated subscriber
	 * then it activates the subscriber else it upgrade
	 * or downgrade the service based on service key.
	 * Subscribers with deactivation pending or activation pending
	 * status are not allowed for upgrade or downgrade.
	 */
	
	@Override
	public final Subscriber createSubscription(String msisdn, String serviceKey,
			String mode) throws UserException {
		logger.info("Create Subscription Method invoked!");
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtdbManager.getSubscriber(msisdn);
		validateSubscriber(msisdn);
		WebServiceContext task = getTask(msisdn, mode, serviceKey);
		Offer offer = getOfferFromServiceKey(msisdn, mode, serviceKey, OfferType.subscription);
		if (offer != null && !(offer.getOfferID().equals("-1") || offer.getOfferID().equals("1"))) 
			task.put(param_offerID, offer.getOfferID());
			
		if (rbtdbManager.isSubscriberDeactivated(subscriber)) {
			return processActivation(msisdn, serviceKey, mode, task);
		} else 
			return processUpgradeOrDowngrade(subscriber, serviceKey, mode);
	}


	/**
	 * Processes the activation request for a new
	 * or deactivated subscriber
	 * @author md.alam
	 * @param msisdn
	 * @param serviceKey
	 * @param mode
	 * @param task
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected Subscriber processActivation(String msisdn, String serviceKey, String mode, WebServiceContext task) throws UserException {
		Subscriber subscriber = null;
		
		task.put(param_subscriptionClass, serviceKey);
		CosDetails cos = getCos(task, subscriber);
		String subscriptionClass = getSubscriptionClass(task, cos);
		subscriber = getSubscriber(msisdn, task);
		ISubscriber subscriberImpl = (ISubscriber) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_IMPL);
		
		//VOL-947
		subscriberImpl.deleteSubscriberById(msisdn);
		
		boolean isInserted = subscriberImpl.insert(subscriber);
		if (isInserted) {
			rbtDaemonManager.processSubscriptionsAct(subscriber);
		} else 
			throw new UserException(UNKNOWN_ERROR);
		return subscriber;
	}


	@Override
	public final Subscriber processUpgradeOrDowngrade(Subscriber subscriber, String serviceKey, String mode) throws UserException {
		
		if (isUpgradeOrDowngradeAllowed(subscriber, serviceKey)) {
			WebServiceContext task = getTask(subscriber.subID(), mode, serviceKey);
			task.put(param_subscriber, subscriber);
			task.put(param_rentalPack, serviceKey);
			/*CosDetails cos = getCos(task, subscriber);
			String subscriptionClass = getSubscriptionClass(task, cos);*/
			subscriber = getSubscriber(subscriber.subID(), task);
			ISubscriber subscriberImpl = (ISubscriber) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_IMPL);
			boolean isUpdated = subscriberImpl.updateSubscriber(subscriber);
			if (isUpdated) {
				rbtDaemonManager.processSubscriptionsAct(subscriber);
			} else
				throw new UserException(UNKNOWN_ERROR);
		}
		return subscriber;		
	}

	@Override
	public final Subscriber deactivateSubscriber(String msisdn, String mode, boolean isDelayDeactivation) throws UserException{
		logger.info("Deactive Subscription Method invoked!");
		validateSubscriber(msisdn);
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtdbManager.getSubscriber(msisdn);
		if(isDeactivationAllowed(subscriber)){
			if(isDelayDeactivation){
				return processDelayDeactivation(subscriber,mode);
			}else{
				return processDeactivation(subscriber,mode);
			}
			
		}
		return null;
	}

	@Override
	public final Subscriber suspendOrResumeSubscriber(String msisdn, String mode, boolean isSuspend) throws UserException{
		logger.info("Suspend/Resume Subscriber Method invoked!");
		validateSubscriber(msisdn);
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtdbManager.getSubscriber(msisdn);
		if(isSuspend){
			return  processSuspendSubscriber(subscriber,mode);
		}else{
			return processResumeSuspention(subscriber,mode);
		}
	}


	/**
	 * Prepares the task object based on
	 * parameters paased
	 * @author md.alam
	 * @param msisdn
	 * @param mode
	 * @param serviceKey
	 * @return {@link WebServiceContext}
	 */
	private WebServiceContext getTask(String msisdn, String mode, String serviceKey) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(param_subscriberID, msisdn);
		map.put(param_mode, mode);
		map.put(param_isPrepaid, YES);
		WebServiceContext task = Utility.getTask(map);
		String circleId = DataUtils.getUserCircle(task);
		if (circleId != null)
			task.put(param_circleID, circleId);
		return task;
	}


	/**
	 * Based on the parameters provided,
	 * it will get the CosDetails
	 * @param task
	 * @param subscriber
	 * @return {@link CosDetails}
	 */
	protected CosDetails getCos(WebServiceContext task,
			Subscriber subscriber) {
		return DataUtils.getCos(task, subscriber);
	}


	/**
	 * Based on the parameters provided,
	 * it will get the SubscriptionClass
	 * @param task
	 * @param cos
	 * @return SubscriptionClass
	 */
	protected String getSubscriptionClass(WebServiceContext task, CosDetails cos) {
		String subscriptionClass = null;
		logger.info("Getting the subscription class. task: " + task + ", cos: "
				+ cos);

		//String action = task.getString(param_action);
		if (task.containsKey(param_subscriptionClass)) {
			subscriptionClass = task.getString(param_subscriptionClass);
			logger.info("Returning subscriptionClass: " + subscriptionClass
					+ ", request contains subscriptionClass");
		} else if (task.containsKey(param_rentalPack)) {
			subscriptionClass = task.getString(param_rentalPack);
			logger.info("Returning subscriptionClass: " + subscriptionClass
					+ ", request contains rental pack");
		}

		if (cos != null && subscriptionClass == null) {
			subscriptionClass = cos.getSubscriptionClass();
			logger.info("Fetched cos subscriptionClass: " + subscriptionClass
					+ ", subscriptionClass is null");
		}

		// Overriding the Subscription Class if ContentType is supported ????
		if (cos != null && cos.getContentTypes() != null) {
			subscriptionClass = cos.getSubscriptionClass();
			logger.info("Fetched subscriptionClass: " + subscriptionClass
					+ ", cos content types are exists");
		}

		if (subscriptionClass == null) {
			logger.warn("Since subscriptionClass is null, assigning DEFAULT.");
			subscriptionClass = "DEFAULT";
		}

		logger.info("Returning subscriptionClass: " + subscriptionClass);
		return subscriptionClass;
	}


	protected boolean isUpgradeOrDowngradeAllowed(Subscriber subscriber, String serviceKey) throws UserException {
		boolean isAllowed = true;

		if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATION_PENDING) || subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_TO_BE_DEACTIVATED))
			throw new UserException(SUB_DEACT_PENDING);
		else if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_PENDING))
			throw new UserException(Constants.SUB_ACT_PENDING);
		else if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_GRACE))
			throw new UserException(SUB_ACT_GRACE);
		else if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATION_ERROR))
			throw new UserException(SUB_ACT_ERROR);
		else if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_SUSPENDED))
			throw new UserException(SUB_SUSPENDED);
		else if (subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_TO_BE_ACTIVATED))
			throw new UserException(Constants.SUB_ACT_PENDING);
		
		if (subscriber.subscriptionClass().equalsIgnoreCase(serviceKey))
			throw new UserException(SUB_STATUS_CHANGE_NOT_ALLOWED);
		
		return isAllowed;
	}



	protected void validateSubscriber(String msisdn) throws UserException {
		logger.info("Validating MSISDN: "+msisdn);
		HashMap<String,String> map = new HashMap<String,String>(1);
		map.put(param_subscriberID,msisdn);

		WebServiceContext task = Utility.getTask(map);
		String response = DataUtils.isValidUser(task, null);
		if(!response.equalsIgnoreCase(VALID))
			throw new UserException(INVALID_SUBSCRIBER);
	}
	
	protected Subscriber upgradeSubscriber(Subscriber subscriber, WebServiceContext task) {
		
		return subscriber;
	}
	
	protected Subscriber getSubscriber(String msisdn, WebServiceContext task) {
		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		CosDetails cos = getCos(task, subscriber);
		String subscriptionClass = getSubscriptionClass(task, cos);
		String mode = task.getString(param_mode);
		String deactivatedBy = null;
		Date startDate = null;
		/*Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(2037, Calendar.DECEMBER, 31,00,00,00);*/
		Date endDate = null;
		String prepaid = task.containsKey(param_isPrepaid) ? task.getString(param_isPrepaid) : NO;
		Date accessDate = null;
		Date nextChargingDate = null;
		int access = 1;
		String info = mode;
		String subscription = iRBTConstant.STATE_TO_BE_ACTIVATED;
		String lastDeactivationInfo = null;
		Date lastDeactivationDate = null;
		Date activationDate = null;
		int maxSelections = 0;
		String cosId = cos.getCosId();
		String activatedCosId = cos.getCosId();
		int rbtType = 0;
		String language = "eng";
		String oldClassType = null;
		String extraInfo = null;
		String circleId = null;
		if (subscriber != null) {
			oldClassType = subscriber.subscriptionClass();
			subscription = iRBTConstant.STATE_CHANGE;
			access = subscriber.noOfAccess()+1;
			extraInfo = subscriber.extraInfo();
			language = subscriber.language();
			Map<String, String> extraInfoMap = null;
			if(extraInfo != null) {
				extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			}
			else {
				extraInfoMap = new HashMap<String, String>();
			}
			cos = getCos(task, null);
			subscriptionClass = getSubscriptionClass(task, cos);
			if(cos != null) {
				extraInfoMap.put("COS_ID", cos.getCosId());
			}
			extraInfoMap.put("OLD_ACT_BY", mode);
			extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			
		}		
		if (task.containsKey(param_circleID)) 
			circleId = task.getString(param_circleID);
		String refId = UUID.randomUUID().toString();
		subscriber = new SubscriberImpl(msisdn, mode, deactivatedBy, startDate, endDate, prepaid, 
				accessDate, nextChargingDate, access, info, subscriptionClass,
				subscription, lastDeactivationInfo, lastDeactivationDate, activationDate,
				maxSelections, cosId, activatedCosId, rbtType, language, oldClassType, extraInfo, circleId, refId);
		return subscriber;

	}


	/**
	 * 1. Validate subscriber id and prefix valid check
	 * 2. get offer services from prism
	 * 3. store into memcache
	 * 4. convert from Offer to OfferDTO
	 * 5. return list
	 * @author senthil.raja
	 * @param msisdn
	 * @param mode
	 * @return
	 * @throws
	 */
	@Override
	public final List<Object> getAllowedSubscription(String msisdn, String mode) throws UserException {
		logger.info("Entering getAllowedSubscription:: msisdn: " + msisdn + ", mode: " + mode);
		validateSubscriber(msisdn);
		List<Offer> offers = getOffers(msisdn, mode, OfferDTO.OfferType.subscription);
		storeMemcache(offers, msisdn);
		List<Object> offerDTOList = new ArrayList<Object>();
		
		if(offers!=null)
		{
		for(Offer offer : offers) {
			OfferDTO offerDTO = new OfferDTOBuilder(offer.getOfferID()).
					setAmount(offer.getAmount()).setOfferDesc(offer.getOfferDescription())
					.setOfferStatus(offer.getOfferStatus()).setOfferType(offer.getOfferType()).setServiceKey(offer.getSrvKey())
					.setValidityDays(offer.getValidityDays()+"")
					.buildOfferDTO();
			offerDTOList.add(offerDTO);
		}}
		//Added for getting srv key from cos
		if(offerDTOList.size() == 0){
			logger.info("getting subscription class from cos...");
			WebServiceContext task = new WebServiceContext();
			task.put(param_subscriberID, msisdn);
			task.put(param_mode, mode);
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
			CosDetails cos = DataUtils.getCos(task, subscriber);
			if(cos != null && cos.getSubscriptionClass() != null){
				String subscriptionClassName = cos.getSubscriptionClass();
				SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriptionClassName);
				if(subscriptionClass != null){
					OfferDTO offerDTO = new OfferDTOBuilder(null).
							setAmount(Double.parseDouble(subscriptionClass.getSubscriptionAmount())).setOfferDesc(subscriptionClassName)
							.setOfferStatus(null).setOfferType(1).setServiceKey(subscriptionClassName)
							.setValidityDays(getValidityInDays(subscriptionClass.getSubscriptionPeriod()))
							.buildOfferDTO();
					offerDTOList.add(offerDTO);
				}
				
			}
		}
		logger.info("Returing subscription class list size is :"+offerDTOList.size());
		return offerDTOList;
	}


	/**
	 * store the offer information in memcache, data will be persistence in memcache for some time.
	 * @param offers
	 * @param msisdn
	 */
	protected void storeMemcache(List<Offer> offers, String msisdn) throws UserException{
		if(offerMemcache.saveData(offers, msisdn)) {
			logger.info("Successfully stored " + msisdn + " offers in memcache");
		}
		else{
			logger.error("Successfully not stored " + msisdn + " offers in memcache, due to memcache is down / not accessable");
		}
	}

	protected boolean toFallbackToPrism(){
		return false;
	}

	protected Offer getOfferFromServiceKey(String msisdn, String mode, String serviceKey, OfferDTO.OfferType offerType) throws UserException{
		msisdn = RBTDBManager.getInstance().subID(msisdn);
		List<Offer> offers = (List<Offer>) offerMemcache.getData(msisdn);
		if(offers == null && toFallbackToPrism()) {
			offers = getOffers(msisdn, mode, offerType);
		}
		
		if(offers == null) {
			return null;
		}
		
		Offer tempOffer = null;
		for(Offer offer : offers) {
			if(!offer.getSrvKey().equalsIgnoreCase(serviceKey)) {
				continue;
			}
			switch(offerType)
			{
				case subscription:
					if(offer.getOfferType() == Offer.OFFER_TYPE_SUBSCRIPTION) {
						tempOffer = offer;
					}
					break;
				case selection:
					if(offer.getOfferType() == Offer.OFFER_TYPE_SELECTION) {
						tempOffer = offer;
					}
					break;
				default:
					break;
			}
			
			if(tempOffer != null) {
				break;
			}
		}
		
		return tempOffer;
	}
	
	/**
	 * hit prism and get offer
	 * @param msisdn
	 * @param mode
	 * @return
	 */
	protected List<Offer> getOffers(String msisdn, String mode, OfferDTO.OfferType offerType) throws UserException{
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		msisdn = subscriber != null ? subscriber.subID() : RBTDBManager.getInstance().subID(msisdn);
		
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);

		//Consturcting HashMap
		HashMap<String,Object> map = new HashMap<String,Object>(2);
		map.put(param_subscriberID,msisdn);
		map.put(param_subscriber, subscriber);
		
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);

		WebServiceContext task = new WebServiceContext(map);

		//hitting prism and get subscription offer
		String userType = Utility.getSubscriberType(task);
		
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);
		
		try {
			Offer[] offers = null;
			
			switch(offerType) {
				case subscription:
					offers = RBTSMClientHandler.getInstance().getSubscriptionOffer(msisdn, mode, userType, null);
					break;
				case selection:
					offers = RBTSMClientHandler.getInstance().getSelectionOffer(msisdn, mode, userType, null);
					break;
				
			}
			
			logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);
			/*
			if(offers != null && offers.length != 0) {
				return Arrays.asList(offers);
			}*/
			
			return Arrays.asList(offers);
		} catch (RBTException e) {
			// TODO Auto-generated catch block
			logger.error("Exception while getting offer: ", e);
			throw new UserException(OFFER_NOT_INITILIAZED);
		} catch(Exception e) {
			logger.error("Exception while getting offer:" , e);
		}
		
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);
		return null;
		//throw new UserException(Constants.OFFER_NOT_FOUND);
	}
	
	/**
	 * Return weather deactivation is allowed or not
	 * @param subYes
	 * @return {@link Boolean}
	 * @throws UserException
	 */
	protected boolean isDeactivationAllowed(Subscriber subscriber) throws UserException{
	logger.info("isDeactivationAllowed Method invoked!");
		boolean isAllowed = false;
	if(subscriber == null){
			isAllowed = false;
			logger.error(SUB_DONT_EXIST);
			throw new UserException(SUB_DONT_EXIST);
		}
		
		String subYes = subscriber.subYes();

		if (subYes != null && (subYes.equalsIgnoreCase(iRBTConstant.STATE_ACTIVATED) || subYes.equalsIgnoreCase(iRBTConstant.STATE_SUSPENDED))){
			isAllowed = true;
			logger.info("Deactivation allowed returning true");
		}
		else {
			isAllowed = false;
			logger.error(SUB_DONT_EXIST);
			if(subYes == null) {
				throw new UserException(SUB_DONT_EXIST);
			}
			else {
				throw new UserException(SubscriptionStatus.getSubscriptionStatus(subYes));
			}
		}
		return isAllowed;
		
	}
	
	/**
	 * Processes the deactivated request for a active or suspended subscriber
	 * @param subscriber
	 * @param mode
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected Subscriber processDeactivation(Subscriber subscriber, String mode) throws UserException {
		logger.info("Inside processDeactivation:" + subscriber + ", mode:" + mode);
		if (subscriber == null) {
			logger.info("Subscriber: " + subscriber );
			throw new UserException(SUB_DONT_EXIST);
		}
		Date date = null;
		boolean delSelections = true;
		boolean sendToHLR = true;
		boolean smDeactivation = true;
		boolean isDirectDeact = false;
		boolean checkSubClass = false;
		String subscriberID = subscriber.subID();
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		String response = rbtdbManager.deactivateSubscriber(subscriberID, mode, date, delSelections,
				sendToHLR, smDeactivation, isDirectDeact, checkSubClass, subscriber.rbtType(), subscriber, mode,
				subscriber.extraInfo());
		if("SUCCESS".equalsIgnoreCase(response)){
			boolean smResponse = rbtDaemonManager.processSubscriptionsDct(rbtdbManager.getSubscriber(subscriberID));
			logger.info("Is subscription deactivation request send successfully to prism: " + smResponse);
		}
		
		return rbtdbManager.getSubscriber(subscriberID);

	}
	
	/**
	 * Processes the delay deactivated request for a active or suspended subscriber
	 * @param subscriber
	 * @param mode
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected Subscriber processDelayDeactivation(Subscriber subscriber, String mode) throws UserException {
		logger.info("Inside processDelayDeactivation:" + subscriber +", mode:" +mode);
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		if (subscriber == null) {
			logger.info("Subscriber: " + subscriber );
			throw new UserException(SUB_DONT_EXIST);
		}
		String subscriberID = subscriber.subID();
			HashMap<String, String> subExtraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			if (subExtraInfoMap == null)
				subExtraInfoMap = new HashMap<String, String>();
			if (subExtraInfoMap.containsKey("DELAY_DEACT")
					&& subExtraInfoMap.get("DELAY_DEACT").equalsIgnoreCase(
							"TRUE")) {
				logger.info("subExtraInfo contains delay deact so sending back ALREADY_DELAY_DEACT");
				throw new UserException(ALREADY_DELAY_DEACT);
			}

			subExtraInfoMap.put("DELAY_DEACT", "TRUE");
			subExtraInfoMap.put("SUB_YES", subscriber.subYes());
			String extraInfo = DBUtility
					.getAttributeXMLFromMap(subExtraInfoMap);
			String response = rbtdbManager.updateExtraInfoNStatusNDeactBy(
					 subscriber.subID(), extraInfo, "C", mode);
		logger.info("response::"+response);
			if("SUCCESS".equalsIgnoreCase(response)){
					logger.info("processSubscriptionsAct method invoked:: subscriber:" + subscriber + ", mode:" + mode);
				boolean smResponse = rbtDaemonManager.processSubscriptionsAct(rbtdbManager.getSubscriber(subscriberID));
				logger.info("Is subscription delay deactivation request send successfully to prism: " + smResponse);
			}
			
		
		
		return rbtdbManager.getSubscriber(subscriberID);

	}
	
	/**
	 * Processes the suspension request for a active subscriber
	 * @param subscriber
	 * @param mode
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected Subscriber processSuspendSubscriber(Subscriber subscriber, String mode) throws UserException {
		logger.info("Inside processSuspendSubscriber:" + subscriber + ", mode:" + mode);
		if (subscriber == null) {
			throw new UserException(SUB_DONT_EXIST);
		}
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		HashMap<String, String> extraInfoMap = rbtdbManager.getExtraInfoMap(subscriber);
		String subscriberID = subscriber.subID();
		if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.VOLUNTARY)) {
			if (extraInfoMap.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE")) {
					logger.info("response: " + ALREADY_VOLUNTARILY_SUSPENDED);
					throw new UserException(ALREADY_VOLUNTARILY_SUSPENDED);
			}
		}

		if (!subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_ACTIVATED)) {
			throw new UserException(SUSPENSION_NOT_ALLOWED);
		}

		String suspentionUrlParam = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
				"VOLUNTARY_SUSPENSION_ON_URL", null);
		logger.info("processSubscriptionSuspension method invoked:: subscriber:" + subscriber + ", suspentionUrlParam:"
				+ suspentionUrlParam + ", mode:" + mode);
		processSubscriptionSuspension(subscriber, suspentionUrlParam, mode);
		return rbtdbManager.getSubscriber(subscriberID);
	}

	/**
	 * Processes the resume request for a suspended subscriber
	 * @param subscriber
	 * @param mode
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected Subscriber processResumeSuspention(Subscriber subscriber, String mode) throws UserException {
		logger.info("Inside processResumeSuspention:" + subscriber +", mode:" +mode);
		if (subscriber == null) {
			logger.info("Subscriber: " + subscriber );
			throw new UserException(ERROR);
		}
		String subscriberID = subscriber.subID();
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		HashMap<String, String> extraInfoMap = rbtdbManager.getExtraInfoMap(subscriber);
		if (extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.VOLUNTARY)) {
			if (!extraInfoMap.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE")) {
					logger.info("response: NOT_VOLUNTARILY_SUSPENDED_USER");
					throw new UserException(NOT_VOLUNTARILY_SUSPENDED_USER);
			}
		}
		String suspentionUrlParam = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
				"VOLUNTARY_SUSPENSION_OFF_URL", null);
		if (!subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_SUSPENDED)) {
			throw new UserException("NOT_A_SUSPENDED_USER");
		}
		logger.info("processSubscriptionSuspension method invoked:: subscriber:" + subscriber + ", suspentionUrlParam:"
			+ suspentionUrlParam + ", mode:" + mode);
		String response = processSubscriptionSuspension(subscriber, suspentionUrlParam, mode);
		if(response != null && response.equalsIgnoreCase(ALREADY_PROCESSED)){
			throw new UserException(ALREADY_PROCESSED);
		}
		return rbtdbManager.getSubscriber(subscriberID);
	}

	/**
	 * Processes the suspension request and to hit to smClient for a active subscriber
	 * @param subscriber
	 * @param mode
	 * @return {@link Subscriber}
	 * @throws UserException
	 */
	protected String processSubscriptionSuspension(Subscriber subscriber,
			String suspensionUrl, String mode) throws UserException {
		String response = ERROR;

		try {
			String subscriberID = subscriber.subID();
			suspensionUrl = suspensionUrl.replaceAll("%SUBSCRIBER_ID%",
					subscriberID);
			suspensionUrl = suspensionUrl.replaceAll("%SERVICE_KEY%",
					"RBT_ACT_" + subscriber.subscriptionClass());
			suspensionUrl = suspensionUrl.replaceAll("%MODE%", mode);
			suspensionUrl = suspensionUrl.replaceAll("%REFID%", "null");

			HttpParameters httpParameters = new HttpParameters(suspensionUrl);
			Utility.setSubMgrProxy(httpParameters);
			logger.info("httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			logger.info("httpResponse: " + httpResponse);

			if (httpResponse.getResponseCode() == 724) {
				return ALREADY_PROCESSED;
			} else {
				String[] status = httpResponse.getResponse().trim()
						.split("\\|");
				response = status[0];
				if(!response.equalsIgnoreCase("SUCCESS")) {
					throw new UserException(PRISM_SERVER_ERROR);
				}
			}
		} catch (Exception e) {
			logger.error("Exception while suspending user: ", e);
			throw new UserException("UNKNOWN_ERROR");
		}

		return response;
	}
	
	// Added for validity 
	private String getValidityInDays(String suscriptionPeriod){

		if (suscriptionPeriod == null)
			suscriptionPeriod = "M1";
		int type = 0;
		int number = 0;
		if (suscriptionPeriod.startsWith("D"))
			type = 0;
		else if (suscriptionPeriod.startsWith("W"))
			type = 1;
		else if (suscriptionPeriod.startsWith("M"))
			type = 2;
		else if (suscriptionPeriod.startsWith("Y"))
			type = 3;
		else if (suscriptionPeriod.startsWith("B"))
			type = 4;
		else if (suscriptionPeriod.startsWith("O"))
			type = 5;

		if (type != 4 && type != 5) {
			try {
				number = Integer.parseInt(suscriptionPeriod.substring(1));
			} catch (Exception e) {
				type = 2;
				number = 1;
			}
		}

		int days = 0;
		switch (type) {
		case 0:
			days = number;
			break;
		case 1:
			days = 7 * number; //WEEK_OF_YEAR
			break;
		case 2:
			days = 30 * number; //MONTH
			break;
		case 3:
			days = 365 * number; //YEAR
			break;
		case 4:
			days = 365 * 50; //YEAR, 50);
			break;
		case 5:
			days = 365 * 50; //YEAR, 50);
			break;
		default:
			days = 30;  //1 MONTH;
			break;
		}
		return String.valueOf(days);
	
		
	}
}
