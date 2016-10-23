package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTPlayerUpdateDaemon;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.processor.ISubscriptionProcessor;
import com.onmobile.apps.ringbacktones.rbt2.service.ISubscriptionService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTOBuilder;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.ISubscriptionRequest;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IUpdateSubscriptionService;
import com.onmobile.apps.ringbacktones.v2.resolver.response.IDownloadResponse;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISubscriptionResponse;
import com.onmobile.apps.ringbacktones.v2.util.OfferMemcache;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

@SuppressWarnings("unchecked")
public abstract class AbstractSubscriptionRequestResolver implements ISubscriptionRequest,Constants,WebServiceConstants{
    
	protected ISubscriptionResponse responseResolver;
	
	private IUpdateSubscriptionService dctRequest;

	

	
	public IUpdateSubscriptionService getDctRequest() {
		return dctRequest;
	}

	public void setDctRequest(IUpdateSubscriptionService dctRequest) {
		this.dctRequest = dctRequest;
	}

	public void setResponseResolver(ISubscriptionResponse responseResolver) {
		this.responseResolver = responseResolver;
	}
	
	@Autowired
	private OfferMemcache offerMemcache;
				
	public void setOfferMemcache(OfferMemcache offerMemcache) {
		this.offerMemcache = offerMemcache;
	}

	private static Logger logger = Logger.getLogger(AbstractSubscriptionRequestResolver.class);
	
	@Override
	public Subscription updateSubscriber(String status, String msisdn, String mode, String catalogSubscriptionId, String subscriptionClass)
			throws UserException {
			
			if (msisdn.trim().isEmpty() || status.trim().isEmpty() || mode.trim().isEmpty() || catalogSubscriptionId.trim().isEmpty()) 
				throw new UserException(Constants.INVALID_PARAMETER);
		
			if (SubscriptionStatus.CANCELED.toString().equalsIgnoreCase(status)) {

				if (dctRequest == null) {
					throw new UserException("INVALIDPARAMETER", "SERVICE_NOT_AVAILABLE");
				}
				logger.info("SubscriptionStatus is CANCELED");
				return dctRequest.updateSubscriber(status, msisdn, mode, catalogSubscriptionId,subscriptionClass);

			} else {
				throw new UserException("INVALIDPARAMETER","INVAILD STATUS");
			}
			
	}

	private SubscriptionRequest getSubscriptionRequestForUpdateSubscriber(String msisdn, String mode) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(msisdn);
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setIsDirectDeactivation(false);
		return subscriptionRequest;
	}

	/**
	 * hit offer management to get offer and store offer details in memcache and return the same to client.
	 * it will valid subscriber id is not valid / prefix not valid then will return sub doesn't exist
	 * if offer management system is not up / no offer, then will return offer not found
	 * 
	 */
	@Override
	public List<Object> getAllowedSubscription(String msisdn, String mode)
			throws UserException {

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
			//vikrant-1
			//Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
			com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
					.getSubscriber(rbtDetailsRequest);
			ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
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



	@Override
	public Subscription getProfile(String msisdn, String mode) throws UserException {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		return responseResolver.prepareGetSubscriptionResponse(mode, msisdn, subscriber);
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

	public Offer getOfferFromServiceKey(String msisdn, String mode, String serviceKey, OfferDTO.OfferType offerType) throws UserException{
		
		
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
	 // vikrant-2
	//	Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
				.getSubscriber(rbtDetailsRequest);
		ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
		Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
		msisdn = subscriber != null ? subscriber.subID() : RBTDBManager.getInstance().subID(msisdn);
		
		logger.info("Entering getOffer:: msisdn: " + msisdn + ", mode: " + mode);

		//Consturcting HashMap
		HashMap<String,Object> map = new HashMap<String,Object>(2);
		map.put("subscriberID",msisdn);
		map.put("subscriber", subscriber);
		
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
	
	protected void validateSubscriber(String msisdn) throws UserException {
		logger.info("Validating MSISDN: "+msisdn);
		HashMap<String,String> map = new HashMap<String,String>(1);
		map.put(param_subscriberID,msisdn);

		WebServiceContext task = Utility.getTask(map);
		String response = DataUtils.isValidUser(task, null);
		if(!response.equalsIgnoreCase(VALID))
			throw new UserException(INVALID_SUBSCRIBER);
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
