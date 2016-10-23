package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.bean.ConsentProcessBean;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.factory.BuildAssetFactory;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.SelectionRequestResolver;
import com.onmobile.apps.ringbacktones.v2.resolver.response.IDownloadResponse;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelComvivaUtility;
import com.onmobile.apps.ringbacktones.v2.util.AirtelUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public abstract class AbstractDownloadResponeResolver implements IDownloadResponse {

	@Autowired
	protected ApplicationContext applicationContext;
	protected ResponseErrorCodeMapping errorCodeMapping;
	protected static Logger logger = Logger.getLogger(SelectionRequestResolver.class);
	
	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	
	protected String makeConsentCGUrl(AbstractOperatorUtility operatorUtility, Rbt rbt, SelectionRequest selectionRequest, String operatorName, Subscriber subscriber) {
		String cgUrl = null;
		if(rbt != null) {
			boolean isComvivaCircle = false;
			Consent consent = rbt.getConsent();
			ConsentProcessBean consentProcessBean = buildConsentProcessBean(consent, selectionRequest, subscriber, isComvivaCircle);
			
			if(consentProcessBean != null) {
				if(consent.getClass() == ComvivaConsent.class && operatorUtility.getClass() == AirtelUtility.class){
					operatorUtility = (AbstractOperatorUtility) getComvivaConsentUtlityObject(operatorName);
				}				
				
				operatorUtility.setOperatorName(operatorName.toLowerCase());
				operatorUtility.setConsentProcessBean(consentProcessBean);
				cgUrl = operatorUtility.makeConsentCgUrl();			
			}
		}
		return cgUrl;
	}
	
	protected String makeConsentCGUrl(AbstractOperatorUtility operatorUtility, Rbt rbt, SelectionRequest selectionRequest, String operatorName, com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber) {
		String cgUrl = null;
		if(rbt != null) {
			boolean isComvivaCircle = false;
			Consent consent = rbt.getConsent();
			ConsentProcessBean consentProcessBean = buildConsentProcessBean(consent, selectionRequest, subscriber, isComvivaCircle);
			
			if(consentProcessBean != null) {
				if(consent.getClass() == ComvivaConsent.class && operatorUtility.getClass() == AirtelUtility.class){
					operatorUtility = (AbstractOperatorUtility) getComvivaConsentUtlityObject(operatorName);
				}				
				
				operatorUtility.setOperatorName(operatorName.toLowerCase());
				operatorUtility.setConsentProcessBean(consentProcessBean);
				cgUrl = operatorUtility.makeConsentCgUrl();			
			}
		}
		return cgUrl;
	}
	
	protected String makeRUrl(AbstractOperatorUtility operatorUtility) {
		String rUrl = operatorUtility.makeRUrl();		
		return rUrl;
	}
	
	protected ThirdPartyConsent buildThirdPartyConsent(AbstractOperatorUtility operatorUtility, String cgUrl, String rUrl) {
		ThirdPartyConsent thirdPartyConsent = null;
		if(cgUrl != null) {
			thirdPartyConsent = new ThirdPartyConsent();
			thirdPartyConsent.setThirdPartyUrl(cgUrl);
			thirdPartyConsent.setReturnUrl(rUrl);
			thirdPartyConsent.setId(operatorUtility.getTransId());			
		}
		return thirdPartyConsent;
	}
	
	private ConsentProcessBean buildConsentProcessBean(Consent consent, SelectionRequest selectionRequest, Subscriber subscriber, boolean isComvivaCircle) {
		if(consent == null)
			return null;
		ConsentProcessBean consentProcessBean = new ConsentProcessBean();
		if(consent.getClipId() == null || consent.getClipId().equalsIgnoreCase(""))
			consent.setClipId(selectionRequest.getClipID());
		if(consent.getCatId() == null || consent.getCatId().equalsIgnoreCase(""))
			consent.setCatId(selectionRequest.getCategoryID());
		consentProcessBean.setConsent(consent);
		consentProcessBean.setResponse(selectionRequest.getResponse());
		consentProcessBean.setSubscriberId(subscriber.subID());
		String circleId = subscriber.circleID().trim().split("_")[1];
		if(isComvivaCircle) {
			consentProcessBean.setCircleID(circleId);
			String callerId = selectionRequest.getCallerID();
			if(callerId == null)
				callerId = "ALL";
			consentProcessBean.setCallerId(callerId);
		}
		return consentProcessBean;
	}
	
	private ConsentProcessBean buildConsentProcessBean(Consent consent, SelectionRequest selectionRequest, com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber, boolean isComvivaCircle) {
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
		String circleId = subscriber.getCircleID().trim().split("_")[1];
		if(isComvivaCircle) {
			consentProcessBean.setCircleID(circleId);
			String callerId = selectionRequest.getCallerID();
			if(callerId == null)
				callerId = "ALL";
			consentProcessBean.setCallerId(callerId);
		}
		return consentProcessBean;
	}
	
	
	private IOperatorUtility getComvivaConsentUtlityObject(String operatorName) {
		if (operatorName != null) {
			if (operatorName.toUpperCase().startsWith("AIRTEL")) {
				logger.debug("Returning AirtelComvivaUtility.");
				return new AirtelComvivaUtility();
			}
		}
		logger.debug("Returning AirtelUtility.");
		return new AirtelUtility();
	}
	
	protected Download getDownload(Rbt rbt) {
		Download download = null;
		Download[] downloads = rbt.getLibrary().getDownloads().getDownloads();
		if(downloads!=null && downloads.length > 0){
			download = downloads[downloads.length - 1];
		}
		
		return download;
	}
	
	protected Purchase getPurchase(Download download) {
		Purchase purchase = null;
		if(download!=null){
			purchase = new Purchase();
			String downloadrefID = download.getRefID();
			purchase.setPurchaseID(downloadrefID);
		}		
		return purchase;
	}
	
	protected Asset getAsset(Download download){

		return BuildAssetFactory.createBuildAssetFactory().buildAssetFactoryFromDownload(download);	
	}
	
}
