package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.dto.OfferDTO;
import com.onmobile.apps.ringbacktones.v2.dto.SelRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IComboRequest;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.smsgateway.ivm.ISDRHandler;

/**
 * 
 * @author koyel.mahata
 *
 */
 
public abstract class AbstractComboRequestResolver extends AbstractSelectionRequestResolver implements IComboRequest, MessageResource {
	
	protected ResponseErrorCodeMapping errorCodeMapping;
	
	private static Logger logger = Logger.getLogger(AbstractComboRequestResolver.class);
	
	@Override
	protected SelectionRequest getSelectionRequest(SelRequestDTO selRequestDTO) {

		Subscriber subscriber = selRequestDTO.getSubscriber();
		Subscription subscription = selRequestDTO.getSubscription();
		
		SelectionRequest selectionRequest = super.getSelectionRequest(selRequestDTO);
		if(subscription != null) {

			selectionRequest.setOperatorUserType(subscriber.getOperatorUserType());
			String dtocAppClass = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DTOC_FREETRIAL_APP_SERVICE_CLASS", null);
			boolean allowUpgradation = true;
			if (dtocAppClass != null) {
				String[] dtocServiceClassArr = dtocAppClass.split(",");
				if (dtocServiceClassArr.length > 0) {
					List<String> serviceClass = Arrays.asList(dtocServiceClassArr);
					if (serviceClass.contains(subscription.getSrvKey())) {
						allowUpgradation = false;
					}
				}
			}
			

			if(!allowUpgradation && Utility.isUserActive(subscriber.getStatus()) && 
					(subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.TRADITIONAL.getDefaultValue())
							|| subscriber.getOperatorUserType().equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue()))){
	               selectionRequest.setSubscriptionClass(subscription.getSrvKey());    
			}
			else if (Utility.isUserActive(subscriber.getStatus())) {
	               selectionRequest.setRentalPack(subscription.getSrvKey());
			} else {
	               selectionRequest.setSubscriptionClass(subscription.getSrvKey());
			}
			
			//Addded for direct indirect
			selectionRequest.setIsDirectActivation(getIsSupportDirectActDct());
			
			//added for offer integration
			try {
				SubscriptionRequestResolver subscriptionRequestResolver = new SubscriptionRequestResolver();
				Offer offer = subscriptionRequestResolver.getOfferFromServiceKey(selRequestDTO.getSubscriberId(),selRequestDTO.getMode(), subscription.getSrvKey(), OfferDTO.OfferType.subscription);
				if(offer != null){
					selectionRequest.setOfferID(offer.getOfferID());
				}
			} catch (Exception e) {
				logger.info("Exception occured while getting offer...");
			}
		}
		
		//If extra info is available then setting user info into subscription extra info
		if(selRequestDTO.getSubscription()!=null && selRequestDTO.getSubscription().getExtraInfo()!=null){
			HashMap<String, String> subExtraInfo = new HashMap<String, String>(selRequestDTO.getSubscription().getExtraInfo());
			selectionRequest.setUserInfoMap(subExtraInfo);
		}
		if(selRequestDTO.getRbtFile()!=null)
			selectionRequest.setRbtFile(selRequestDTO.getRbtFile());
		return selectionRequest;
	}
	
	protected void validateSubscription(Subscriber subscriber, Subscription subscription) throws UserException {
		logger.info("Validating Subscription");
		if(!Utility.isUserActive(subscriber.getStatus()) && subscription == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, SUBSCRIPTION_IS_REQUIRED, COMBO_REQUEST_MESSAGE_FOR);
	}
	
	protected void validateOperator(Set<String> operatorsAllowed, String operatorName) throws UserException {
		logger.info("Operators Allowed: "+operatorsAllowed);
		logger.info("Validating Operator: "+operatorName);
		if(!operatorsAllowed.contains(operatorName))
			ServiceUtil.throwCustomUserException(errorCodeMapping, OPERATOR_NOT_SUPPORTED,COMBO_REQUEST_MESSAGE_FOR);
	}
	
	protected void validateToneId(String toneId) throws UserException {
		logger.info("Validating Tone Id: "+toneId);
		Clip clip = RBTCacheManager.getInstance().getClip(toneId);
		if(clip == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, CLIP_NOT_EXIST, COMBO_REQUEST_MESSAGE_FOR);
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		super.setErrorCodeMapping(errorCodeMapping);
		this.errorCodeMapping = errorCodeMapping;
	}

	
}
