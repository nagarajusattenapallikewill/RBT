package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IRBTSelectionService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dto.SelRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;


public class SelectionRequestResolver extends AbstractSelectionRequestResolver {
	
	@Autowired
	private ApplicationContext applicationContext;
	private ResponseErrorCodeMapping errorCodeMapping;
	private static Logger logger = Logger.getLogger(SelectionRequestResolver.class);
	
	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		super.setErrorCodeMapping(errorCodeMapping);
		this.errorCodeMapping = errorCodeMapping;
	}

	
	public void setApplicationContext(ApplicationContext context) {
		this.applicationContext = context;
	}
	
	@Override
	public PlayRule activateSong(PlayRule playRule,String subscriberID,
			String mode) throws UserException {
		String firstName = null;
		String lastName = null;
		AssetType type = playRule.getAsset().getType();
		// Added for common logic
		String callerId = getCallerId(playRule);
		validateEphemeralSelectionForCaller(playRule);
		if(playRule!=null && playRule.getCallingparty()!=null){
			firstName = playRule.getCallingparty().getFirstname();
			lastName =  playRule.getCallingparty().getLastname();
		}
		SelRequestDTO selRequestDTO = new SelRequestDTO(playRule, null, mode, callerId, subscriberID,firstName,lastName);
		SelectionRequest selectionRequest = getSelectionRequest(selRequestDTO);
		
		if(type.toString().equals("SHUFFLELIST"))
			 RBTClient.getInstance().processUDPSelection(selectionRequest);
		else
			 RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse().toLowerCase();
		
		if ((response.equalsIgnoreCase(SUCCESS) || response
				.equalsIgnoreCase(SUCCESS_DOWNLOAD_EXISTS))) {
			playRule = responseResolver.prepareActivateSongResponse(playRule,subscriberID,callerId);
		} else {
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.ACT_SETTING_MESSAGE);
		}
		logger.info("Activation response for Caller generated successfully");
		return playRule;		
	}
	
	
	@Override
	public PlayRuleList getPlayRules(String type, String msisdn, String id, String status) throws UserException {
		PlayRuleList playRuleList = null;
		//
		
		
		//
		if(type != null && type.trim().isEmpty()) {
			type = null;
		}
		
		if(id != null && id.trim().isEmpty()) {
			id = null;
		}
			try {
			/*	IRBTSelectionService selectionService = (IRBTSelectionService) ConfigUtil
						.getBean(BeanConstant.RBT_SELECTION_SERVICE_IMPL);
				settingList = selectionService.getSettings(type, msisdn, id, status);*/
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn,type,id,status);
				Setting[] settings = RBTClient.getInstance().getSettings(rbtDetailsRequest).getSettings();
				logger.info("--> settings"+settings);
				/*settingList=Arrays.asList(settings);
				playRuleList = getPlayRuleList(settingList);
				*/
				
				playRuleList = responseResolver.prepareGetPlayRuleListResponse(settings);

			} catch (IllegalArgumentException e) {
				logger.error("Exception Occured: " + e, e);
				ServiceUtil.throwCustomUserException(errorCodeMapping, INVALID_PARAMETER, MessageResource.INVALID_PARAMETER_MESSAGE);
			} catch (NoSuchBeanDefinitionException e) {
				logger.error("Exception Occured: " + e, e);
			}
		return playRuleList;
	}

}
