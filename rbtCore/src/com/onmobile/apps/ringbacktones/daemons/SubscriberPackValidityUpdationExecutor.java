package com.onmobile.apps.ringbacktones.daemons;

import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class SubscriberPackValidityUpdationExecutor implements Runnable, WebServiceConstants {
	private static Logger logger = Logger.getLogger(SubscriberPackValidityUpdationExecutor.class);
	private int failureTime = 10;
	private RBTDBManager rbtdbManager = null;
	private ProvisioningRequests provisioningRequests = null;
	String smValidityUpdationStatus = "FAILURE";
	private static int statusAfterSuccessfulHit = 2;
	private static int statusAfterFailure = 1;
	private static int statusAfterMaxRetyrFailure = 3;
	private static int maxRetry = 4;
	

	public SubscriberPackValidityUpdationExecutor() {
	}

	public SubscriberPackValidityUpdationExecutor(ProvisioningRequests provisioningRequests) {
		if (provisioningRequests == null) {
			throw new IllegalArgumentException("ProvisioningRequests cannot be null.");
		}
		this.provisioningRequests = provisioningRequests;
		init();
	}

	private void init() {
		rbtdbManager = RBTDBManager.getInstance();
		failureTime = RBTParametersUtils.getParamAsInt("DAEMON",
				"SUBSCRIBER_PACK_VALIDITY_UPDATION_REQUEST_RETRY_TIME_AFTER_FAILURE", 10);
		statusAfterFailure = RBTParametersUtils.getParamAsInt("DAEMON", "SM_SUBCRIPTION_ACTIVATION_SUCCESS_STATUS_FOR_PROVISIONING_REQUEST",1);
		maxRetry = RBTParametersUtils.getParamAsInt("DAEMON", "SUBSCRIBER_PACK_VALIDITY_UPDATION_REQUEST_MAX_RETRY",
				4);
		statusAfterSuccessfulHit = RBTParametersUtils.getParamAsInt("DAEMON", "SM_SUBCRIPTION_VALIDITY_UPDATION_SUCCESS_STATUS_FOR_PROVISIONING_REQUEST",2);
		statusAfterMaxRetyrFailure = RBTParametersUtils.getParamAsInt("DAEMON", "SM_SUBCRIPTION_ACTIVATION_MAX_RETRY_STATUS_FOR_PROVISIONING_REQUEST",3);
	}

	@Override
	public void run() {
		String subscriberId = provisioningRequests.getSubscriberId();
		if(getRetryCount() >= maxRetry -1) {
			rbtdbManager.updateSmStatusRetryCountAndTime(subscriberId, provisioningRequests.getTransId(), null, provisioningRequests.getNextRetryTime(), statusAfterMaxRetyrFailure);
			return;
		}
		try {

			WebServiceContext webServiceContext = new WebServiceContext();
			Subscriber subscriber = rbtdbManager.getSubscriber(subscriberId);
			webServiceContext.put(param_subscriberID, subscriberId);
			webServiceContext.put(param_subscriber, subscriber);
			String cosID = String.valueOf(provisioningRequests.getType());
			CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(cosID);
			webServiceContext.put(param_subscriptionClass, cosDetails.getSubscriptionClass());

			smValidityUpdationStatus = Utility.sendRenewalRequestToSubMgr(webServiceContext);

			logger.info("Returning dtoc sm validity updation status: " + smValidityUpdationStatus);
		} catch (Exception e) {
			logger.error("sm validity updation failed " + smValidityUpdationStatus);
		}
		if(smValidityUpdationStatus.equalsIgnoreCase("SUCCESS")){
			rbtdbManager.updateSmStatusRetryCountAndTime(subscriberId, provisioningRequests.getTransId(), null, provisioningRequests.getNextRetryTime(), statusAfterSuccessfulHit);
		}else{
			rbtdbManager.updateSmStatusRetryCountAndTime(subscriberId, provisioningRequests.getTransId(), getNextRetryCount(), getNextRetryTime(), statusAfterFailure);
		}
	}
	
	private int getRetryCount(){
		String retryCount = provisioningRequests.getRetryCount();
		int retryCnt = 0;
		try{
			retryCnt =Integer.parseInt(retryCount);
		}catch(NumberFormatException e){
			retryCnt = 0;
		}
		
		return retryCnt;
	}
	
	private String getNextRetryCount(){
		return String.valueOf(getRetryCount() + 1 );
	}
	
	private Date getNextRetryTime(){
		Date date = provisioningRequests.getNextRetryTime();
		date.setMinutes(date.getMinutes() + failureTime);
		return date;
	}

}
