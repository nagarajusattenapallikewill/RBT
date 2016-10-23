package com.onmobile.apps.ringbacktones.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.rbt2.service.ISubscriptionService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.ISubscriptionRequest;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

	private static Logger logger = Logger.getLogger(Subscription.class);

	@Autowired
	private ISubscriptionRequest subscription;

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	
	public void setSubscription(ISubscriptionRequest subscription) {
		this.subscription = subscription;
	}

	@RequestMapping(method = RequestMethod.GET ,value = "/{implPath}")
	@ResponseBody
	public List<Object> getSubscription(@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "mode", required = false, defaultValue = "DTOC") String mode,
			@PathVariable(value = "implPath") String implpath) throws UserException {
		logger.info("Request received subscriberId: " + msisdn);
		Object subscriber = subscription.getProfile(msisdn, mode);

		List<Object> list = new ArrayList<Object>(1);
		if(subscriber !=null){
			list.add(subscriber);
		}

		return list;

	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/service/{implPath}")
	public List<Object> getAllowedSubscription(@RequestParam(value="subscriberId", required = true) String msisdn,
			@RequestParam(value = "mode", required = true) String mode,
			@PathVariable(value = "implPath") String implpath) throws UserException{
		
		logger.info("getAllowedSubscription request reached: subscriberId : " + msisdn + ", mode: " + mode);
		return subscription.getAllowedSubscription(msisdn, mode);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{implpath}")
	public Object createSubscriber(@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "serviceKey", required = false) String serviceKey,
			@RequestParam(value = "mode", required = true) String mode,
			@RequestParam(value = "catalogSubscriptionId", required = false) String catalogSubscriptionId,
			@PathVariable(value = "implpath") String implpath,@RequestBody(required=false) Subscription subscriptionDto) throws UserException {
		if(subscriptionDto!=null){
			if(serviceKey==null)
				serviceKey = subscriptionDto.getSrvKey();
			if(catalogSubscriptionId==null && subscriptionDto.getCatalogSubscriptionID()>0)
				catalogSubscriptionId = Integer.toString(subscriptionDto.getCatalogSubscriptionID());			
		}
		logger.info("createSubscriber request reached: subscriberId: " + msisdn + ",serviceKey: " + serviceKey
				+ ", mode: " + mode + "implpath: " + implpath + " , catalogSubscriptionId:"+catalogSubscriptionId);
		if(serviceKey==null){
			ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.SERVICE_KEY_REQ, MessageResource.CREATE_SUBSCRIPTION_MESSAGE_FOR);
		}
		else if(catalogSubscriptionId==null){
			ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.CATALOG_SUBSCRIPTION_ID_REQ, MessageResource.CREATE_SUBSCRIPTION_MESSAGE_FOR);
		}
		
		if(subscriptionDto==null){
			subscriptionDto=new Subscription();
			subscriptionDto.setCatalogSubscriptionID(Integer.parseInt(catalogSubscriptionId));
			subscriptionDto.setSrvKey(serviceKey);
		}
		return subscription.createSubscription(msisdn, mode, subscriptionDto);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/update/{implpath}")
	public Object updateSubscriber(@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "mode", required = true) String mode,
			@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "catalogSubscriptionId", required = true) String catalogSubscriptionId,
			@RequestParam(value = "subscriptionClass", required = false) String subscriptionClass,
			@PathVariable(value = "implpath") String implpath) throws UserException {
		logger.info("Update Subscriber request reached: subscriberId: " + msisdn + ", mode: " + mode
				+ "implpath: " + implpath);
		return subscription.updateSubscriber(status, msisdn, mode, catalogSubscriptionId,subscriptionClass);

	}

}
