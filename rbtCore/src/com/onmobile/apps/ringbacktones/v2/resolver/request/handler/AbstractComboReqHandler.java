package com.onmobile.apps.ringbacktones.v2.resolver.request.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.livewiremobile.store.storefront.dto.rbt.AssetSubType;
import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.rbt2.bean.ConsentProcessBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelComvivaUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelUtility;
import com.onmobile.apps.ringbacktones.v2.util.DefaultOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;


/**
 * 
 * @author md.alam
 *
 */
 
public abstract class AbstractComboReqHandler implements IComboReqHandler,Constants {
	
	
	private static Logger logger = Logger.getLogger(AbstractComboReqHandler.class);
	protected Boolean isSupportDirectActDct = true;
	@Autowired
	protected ResponseErrorCodeMapping errorCodeMapping;

	
	protected Rbt makeSelection(IOperatorUtility operatorUtility, SelectionRequest selectionRequest) {
		Rbt	rbt = operatorUtility.addSubscriberConsentSelection(selectionRequest);
		return rbt;
	}
	
	protected Rbt makePurchase(IOperatorUtility operatorUtility, SelectionRequest selectionRequest) {
		Rbt	rbt = operatorUtility.addSubscriberConsentDownload(selectionRequest);
		return rbt;
	}

	protected String makeConsentCGUrl(AbstractOperatorUtility operatorUtility, Rbt rbt, SelectionRequest selectionRequest, String operatorName, Subscriber subscriber) {
		String cgUrl = null;
		if(rbt != null) {
			//String circleId = subscriber.getCircleID().trim().split("_")[1];
			//boolean isComvivaCircle = consentUtility.getCvCircleId() != null && consentUtility.getCvCircleId().contains(circleId.toUpperCase());
			Consent consent = rbt.getConsent();
			ConsentProcessBean consentProcessBean = buildConsentProcessBean(consent, selectionRequest, subscriber);
			
			if(consentProcessBean != null) {
				if(consent.getClass() == ComvivaConsent.class && operatorUtility.getClass() == AirtelUtility.class){
					operatorUtility = (AbstractOperatorUtility) getComvivaConsentUtlityObject(operatorName);
					if(operatorUtility instanceof AirtelComvivaUtility) {
						String[] arr = subscriber.getCircleID().trim().split("_");
						String circleId = arr.length == 2 ? arr[1] : arr[0];
						consentProcessBean.setCircleID(circleId);
						String callerId = selectionRequest.getCallerID();
						if(callerId == null)
							callerId = "ALL";
						consentProcessBean.setCallerId(callerId);
					}
				}				
				
				operatorUtility.setOperatorName(operatorName.toLowerCase());
				operatorUtility.setConsentProcessBean(consentProcessBean);
				cgUrl = operatorUtility.makeConsentCgUrl();			
			}
		}
		return cgUrl;
	}
	
	protected String makeRUrl(AbstractOperatorUtility operatorutility) {
		String rUrl = operatorutility.makeRUrl();		
		return rUrl;
	}
	
	private ConsentProcessBean buildConsentProcessBean(Consent consent, SelectionRequest selectionRequest, Subscriber subscriber) {
		if(consent == null)
			return null;
		ConsentProcessBean consentProcessBean = new ConsentProcessBean();
		if(consent.getClipId() == null || consent.getClipId().equalsIgnoreCase(""))
			consent.setClipId(selectionRequest.getClipID());
		if(consent.getCatId() == null || consent.getCatId().equalsIgnoreCase(""))
			consent.setCatId(selectionRequest.getCategoryID());
		consentProcessBean.setConsent(consent);
		consentProcessBean.setResponse(selectionRequest.getResponse());
		consentProcessBean.setSubscriberId(subscriber.getSubscriberID());
		consentProcessBean.setSubscriber(subscriber);
		/*String circleId = subscriber.getCircleID().trim().split("_")[1];
		if(isComvivaCircle) {
			consentProcessBean.setCircleID(circleId);
			String callerId = selectionRequest.getCallerID();
			if(callerId == null)
				callerId = "ALL";
			consentProcessBean.setCallerId(callerId);
		}*/
		return consentProcessBean;
	}
	
	protected IOperatorUtility getOperatorUtilityObject(String operatorName) {
		
		IOperatorUtility consentUtility = null;
		
		try {
			consentUtility = (IOperatorUtility) ConfigUtil.getBean(operatorName.toLowerCase());
		} catch(NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e.getMessage()+", returning Default Operator Utility Object");
			consentUtility = new DefaultOperatorUtility();
		}
		return consentUtility;
	}
	
	protected ThirdPartyConsent buildThirdPartyConsent(AbstractOperatorUtility consentUtility, String cgUrl, String rUrl) {
		ThirdPartyConsent thirdPartyConsent = null;
		if(cgUrl != null) {
			thirdPartyConsent = new ThirdPartyConsent();
			thirdPartyConsent.setThirdPartyUrl(cgUrl);
			thirdPartyConsent.setReturnUrl(rUrl);
			thirdPartyConsent.setId(consentUtility.getTransId());			
		}
		return thirdPartyConsent;
	}
	
	private IOperatorUtility getComvivaConsentUtlityObject(String operatorName) {
		if (operatorName != null) {
			if (operatorName.toUpperCase().startsWith("AIRTEL")) {
				logger.debug("Returning AirtelConsentUtility.");
				return new AirtelComvivaUtility();
			}
		}
		logger.debug("Returning AirtelConsentUtility.");
		return new AirtelUtility();
	}
	

	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		this.isSupportDirectActDct = isSupportDirectActDct;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
	//Added for exception handling
	protected void responseValidation(String response) throws UserException{
		if(response == null || !response.equalsIgnoreCase(SUCCESS)){
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.COMBO_REQUEST_MESSAGE_FOR);
		}
	}
	
	
	protected AbstractAssetUtilBuilder getAssetBuilder(AssetType type, AssetSubType assetSubType){
		try{
		  AbstractAssetUtilBuilder assetBuilder = (AbstractAssetUtilBuilder)ConfigUtil.getBean(assetSubType.getType().toString().toLowerCase());
		  return assetBuilder;
		}catch(Exception e){
			 logger.info("Exception occured while getting asset builder by sub type so returning by type");
			 AbstractAssetUtilBuilder assetBuilder = (AbstractAssetUtilBuilder)ConfigUtil.getBean(type.toString().toLowerCase());
			 return assetBuilder;
		}
	}

}
