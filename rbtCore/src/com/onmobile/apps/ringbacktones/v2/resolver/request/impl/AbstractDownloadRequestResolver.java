package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.IAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dto.LibrayRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IDownloadRequest;
import com.onmobile.apps.ringbacktones.v2.resolver.response.IDownloadResponse;
import com.onmobile.apps.ringbacktones.v2.util.DefaultOperatorUtility;
import com.onmobile.apps.ringbacktones.v2.util.IOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
/**
 * 
 * @author koyel.mahata
 *
 */

public abstract class AbstractDownloadRequestResolver implements IDownloadRequest, Constants {
	
	protected IDownloadResponse responseResolver;
	
	public void setResponseResolver(IDownloadResponse responseResolver) {
		this.responseResolver = responseResolver;
	}

	private enum assetType {SONG, RBTPLAYLIST,  RBTSTATION,  RBTUGC, SHUFFLE;}
	//Added for supporting subtype
	private Set<String> restrictedSubType;
	private Logger logger = Logger.getLogger(AbstractDownloadRequestResolver.class);
	@Autowired
	protected ApplicationContext applicationContext;
	private ResponseErrorCodeMapping errorCodeMapping;
	private Boolean isSupportDirectActDct = true;


	protected boolean isValidCType(String cType) throws UserException {
		assetType[] cTypes = assetType.values();

		for(int i=0; i< cTypes.length;i++){
			if(cType.equalsIgnoreCase(cTypes[i].toString())){
				return true; 
			}
		}
		return false;

	}
	
	@Override
	public AssetList getLibrary(String msisdn, String mode)
			throws UserException {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
		rbtDetailsRequest.setMode(mode);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Library library = RBTClient.getInstance().getLibrary(rbtDetailsRequest);
		return responseResolver.prepareGetLibraryResponse(library);
	}
	
	@Override
	public Map<String, String> deleteSongFromLibrary(String msisdn,
			String mode, String toneId, String ctype) throws UserException {
		
		isValidCType(ctype);
		SelectionReqBean reqBean = getSelReqBeanObj(msisdn, mode, true, toneId);
		reqBean.setIsDirectDeactivation(isSupportDirectActDct);
		IAssetUtilBuilder assetUtilBuilder = (IAssetUtilBuilder) ConfigUtil.getBean(ctype.toLowerCase());
		SelectionRequest selectionRequest = assetUtilBuilder.buildSelectionRequestForDeleteFromDownload(reqBean);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Library library = RBTClient.getInstance().deleteSubscriberDownload(selectionRequest);
		String response = selectionRequest.getResponse();
		return responseResolver.prepareDeleteDownloadResponse(response);
	}

	
	protected SelectionReqBean getSelReqBeanObj(String msisdn, String mode, boolean isDTOCrequest, String toneId) {
		SelectionReqBean reqBean = new SelectionReqBean();
		reqBean.setSubscriberId(msisdn);
		reqBean.setMode(mode);
		reqBean.setIsDtoCRequest(isDTOCrequest);
		reqBean.setToneID(toneId);
		return reqBean;
	}
	
	protected SelectionRequest getSelectionReqObj(String msisdn, String mode, String toneId, String cType) throws UserException {
		IAssetUtilBuilder assetUtilBuilder = (IAssetUtilBuilder) ConfigUtil.getBean(cType.toLowerCase());
		SelectionReqBean reqBean = getSelReqBeanObj(msisdn, mode, true, toneId);
		reqBean.setIsSelDirectActivation(isSupportDirectActDct);
		if(isSupportDirectActDct){
			reqBean.setSelectionStartTime(new Date());
		}
		SelectionRequest selectionRequest = null;
		try {
			selectionRequest = assetUtilBuilder.buildSelectionRequestForAddToDownload(reqBean);
		} catch (UserException e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(), MessageResource.LIKE_CONTENT_MESSAGE);
		}
		return selectionRequest;
	}

	protected IOperatorUtility getConsentObject(String operatorName) {
		
		IOperatorUtility operatorUtility = null;
		
		try {
			operatorUtility = (IOperatorUtility) ConfigUtil.getBean(operatorName.toLowerCase());
		} catch(NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e.getMessage()+", returning Default Operatotr Utility Object");
			operatorUtility = new DefaultOperatorUtility();
		}
		return operatorUtility;
	}	

	protected void validateContentType(LibrayRequestDTO dtoResource) throws UserException {
		String cType = dtoResource.getType();
		if(!isValidCType(cType)) {
			logger.info("Invalid ctype so throwing exception invalid_parameter.");
			ServiceUtil.throwCustomUserException(errorCodeMapping, INVALID_CONTENT_TYPE, MessageResource.INVALID_CONTENT_TYPE_MESSAGE);
		}
		validateContentSubType(dtoResource.getSubtype());
	}
	
	protected void validateSubscriber(String msisdn) throws UserException{
		//vikrant
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient.getInstance()
				.getSubscriber(rbtDetailsRequest);
		ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
		Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);
		//Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
		if(subscriber == null || RBTDBManager.getInstance().isSubscriberDeactivated(subscriber)) {
			logger.debug("Subscriber not found returning error resonse " + SUB_DONT_EXIST);
			ServiceUtil.throwCustomUserException(errorCodeMapping, SUB_DONT_EXIST, MessageResource.SUB_DONT_EXIST_MESSAGE);
		}
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		this.isSupportDirectActDct = isSupportDirectActDct;
	}
	
	//Addded for subtype support
	private void validateContentSubType(String cSubType) throws UserException {
		if (restrictedSubType.contains(cSubType.toUpperCase())) {
			logger.info("Invalid csubtype so throwing exception invalid_parameter.");
			ServiceUtil.throwCustomUserException(errorCodeMapping, INVALID_CONTENT_SUB_TYPE, MessageResource.INVALID_CONTENT_SUB_TYPE_MESSAGE);
	    }
	}

	@Override
	public Map<String, String> updateLibrary(String subscriberId,LibrayRequestDTO librayRequestDTO) throws UserException {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Rbt rbt =null;
		if(librayRequestDTO.getChargeClass()!=null){
			rbtDetailsRequest.setClipName(librayRequestDTO.getWavFileName());
			rbtDetailsRequest.setClassType(librayRequestDTO.getChargeClass());
			rbtDetailsRequest.setClipID(librayRequestDTO.getToneId());
			rbt = RBTClient.getInstance().processUpgradeDownload(rbtDetailsRequest);
		}
		return responseResolver.prepareUpdateDownloadResponse(rbtDetailsRequest.getResponse());
	}

	public Set<String> getRestrictedSubType() {
		return restrictedSubType;
	}



	public void setRestrictedSubType(Set<String> restrictedSubType) {
		this.restrictedSubType = restrictedSubType;
	}
}
