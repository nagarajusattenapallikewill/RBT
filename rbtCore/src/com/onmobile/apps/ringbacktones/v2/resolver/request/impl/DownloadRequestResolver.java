package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.dto.LibrayRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author koyel.mahata
 *
 */
 
 

public class DownloadRequestResolver extends AbstractDownloadRequestResolver {
	
	private static Logger logger = Logger.getLogger(DownloadRequestResolver.class);
	private ResponseErrorCodeMapping errorCodeMapping;
	private Boolean isSupportDirectActDct = true;

	@Override
	public Purchase likeContent(String msisdn, String mode,
			LibrayRequestDTO dtoResource) throws UserException {
		
		String cType = dtoResource.getType();
		String toneId = dtoResource.getToneId();
		
		boolean addToLibrary = dtoResource.getAddToLibrary();
		if(!addToLibrary){
			Purchase purchase = new Purchase();
			purchase.setPurchaseID("0");
			return purchase;
		}
		//changed for subtype support
		validateContentType(dtoResource);
		validateSubscriber(msisdn);
				
		SelectionRequest selectionRequest = getSelectionReqObj(msisdn, mode, toneId, cType);
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(msisdn);
		//Added for logger
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtRequest);
		String operatorName = ServiceUtil.getOperatorName(subscriber);
		AbstractOperatorUtility operatorUtility = (AbstractOperatorUtility) getConsentObject(operatorName);
		operatorUtility.setOperatorName(operatorName);
		Rbt rbt = operatorUtility.addSubscriberConsentDownload(selectionRequest);
		String response = selectionRequest.getResponse();		
		return responseResolver.prepareLikeContentResponse(msisdn,response,rbt,selectionRequest,operatorUtility);
		
	}

	public Boolean getIsSupportDirectActDct() {
		return isSupportDirectActDct;
	}


	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		super.setIsSupportDirectActDct(isSupportDirectActDct);
		this.isSupportDirectActDct = isSupportDirectActDct;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		super.setErrorCodeMapping(errorCodeMapping);
		this.errorCodeMapping = errorCodeMapping;
	}
}
