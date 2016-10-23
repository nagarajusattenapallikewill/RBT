package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.ttml;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

/**
 * @author vasipalli.sreenadh
 *
 */
public class TTMLPromoProcessor extends PromoProcessor implements Constants 
{
	public TTMLPromoProcessor() throws RBTException 
	{
		super();
		logger = Logger.getLogger(TTMLPromoProcessor.class);
	}
	
	protected void processAdRBTActivation(Task task) 
	{
		String preActivationChkResp = performAdRBTPreActivationCheck(task);
		// Allowing ADRBT activation even if user is already RBT Subscriber
		if(preActivationChkResp != null && !preActivationChkResp.equalsIgnoreCase(Resp_alreadyActiveOnRBT))  
		{
			task.setObject(param_response, preActivationChkResp);
			return;
		}
		
		String subscriptionClass = "ADRBT";
		if (task.containsKey(param_SUBSCRIPTION_CLASS))
			subscriptionClass = task.getString(param_SUBSCRIPTION_CLASS);

		String activatedBy = "ADRBT";
		if (task.containsKey(param_ACTIVATED_BY))
			activatedBy = task.getString(param_ACTIVATED_BY);
		
		if (preActivationChkResp == null) 
		{	
			// New User
			task.setObject(param_TRANS_TYPE, "ADRBT_ACT");
			task.setObject(param_ACTIVATED_BY, activatedBy);
			task.setObject(param_rbttype, 1);
			task.setObject(param_SUBSCRIPTION_CLASS, subscriptionClass);

			addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_ACTIVATION, "true");
			
			processActivation(task);
			populateAdRBTEesponse(task);
		}
		else 
		{
			if(checkTransID(task, "ADRBT_ACT")) 
			{
				task.setObject(param_response, Resp_invalidTransID);
				return;
			}
			// Existing User, upgarding to ADRBT
			task.setObject(param_rbttype, 1);
			task.setObject(param_playerStatus, "A");
			Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
			HashMap<String, String> extraInfo = subscriber.getUserInfoMap();
			if(extraInfo == null)
				extraInfo = new HashMap<String, String>();
			extraInfo.put(EXTRA_INFO_ADRBT_ACTIVATION, "true");
			task.setObject(param_userInfoMap, extraInfo);
			updateSubscription(task);
			//upgradeSubscription(task, true);
		}
	}
	
	protected void processAdRBTDeactivation(Task task)
	{
		String preDeactivationChkResp = performAdRBTPreDeactivationCheck(task);
		if(preDeactivationChkResp != null) 
		{
			task.setObject(param_response, preDeactivationChkResp);
			return;
		}
	
		if(checkTransID(task, "ADRBT_DEACT")) 
		{
			task.setObject(param_response, Resp_invalidTransID);
			return;
		}
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		if(subscriber.getSubscriptionClass().equalsIgnoreCase(param(COMMON, "ADRBT_SUB_CLASS","ADRBT")))
		{
			task.setObject(param_DEACTIVATED_BY, "ADRBT");
			processDeactivation(task);
		}
		else
		{
			task.setObject(param_rbttype, 0);
			task.setObject(param_playerStatus, "A");
			HashMap<String, String> extraInfo = subscriber.getUserInfoMap();
			if(extraInfo == null)
				extraInfo = new HashMap<String, String>();
			extraInfo.put(EXTRA_INFO_ADRBT_DEACTIVATION, "true");
			task.setObject(param_userInfoMap, extraInfo);
			updateSubscription(task);
		}	
	}
	
	private void upgradeSubscription(Task task, boolean isAdRbtActivation)
	{
		String actDctBy = "ADRBT";
		String transType = "ADRBT_DEACT";
		if (isAdRbtActivation)
		{
			transType = "ADRBT_ACT";
			actDctBy = task.containsKey(param_ACTIVATED_BY) ? task.getString(param_ACTIVATED_BY): "ADRBT" ;
		}
		else
			actDctBy = task.containsKey(param_DEACTIVATED_BY) ? task.getString(param_DEACTIVATED_BY): "ADRBT" ;
		
		if(checkTransID(task, transType)) 
		{
			task.setObject(param_response, Resp_invalidTransID);
			return;
		}
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String pendingStatus = getSubscriberPendingStatus(subscriber);
		if(pendingStatus != null)
		{
			if(pendingStatus.equals(Resp_ActPending))
				task.setObject(param_response, Resp_actPendingAdRBT);
			else if(pendingStatus.equals(Resp_DeactPending))
				task.setObject(param_response, Resp_deactPendingAdRBT);
			return;
		}
		
		SubscriptionRequest request = new SubscriptionRequest(subscriber.getSubscriberID());
		request.setMode(actDctBy);
		request.setModeInfo(task.getString(param_actInfo));
		request.setRentalPack(task.getString(param_ADVANCE_RENTAL_CLASS));
		request.setRbtType((Integer)task.getObject(param_NEW_RBT_TYPE));
		subscriber = RBTClient.getInstance().activateSubscriber(request);
		task.setObject(param_response, request.getResponse());
		setActivationResponse(task);
	}
	
}
