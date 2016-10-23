package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.bsnl;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;

public class BSNLSMSProcessor extends SmsProcessor{
	
	public BSNLSMSProcessor() throws RBTException
	{
		
	}

	public void preProcess(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);  
		getFeature(task);
		if(isUserActive(subscriber.getStatus()) && smsList.size() > 0)
		{
			if(task.getTaskAction() != null && !task.getTaskAction().equalsIgnoreCase(RETAILER_FEATURE))
				isThisFeature(task,tokenizeArrayList(param(SMS,ACTIVATION_KEYWORD,null), null),null);
		}
		super.preProcess(task);
	}
	
	@Override
	public void processTNBActivation(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String userType = subscriber.isPrepaid()?"p":"b";
		try {
			String subscriptionClass = param("SMS", "SMS_TNB_SUBSCRIPTION_CLASS", "ZERO");
			Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
					subscriber.getSubscriberID(), "TNB",
					Offer.OFFER_TYPE_SUBSCRIPTION, userType, subscriptionClass, null);
			if (offers == null || offers.length == 0)
			{
				task.setObject(param_responseSms, getSMSTextForID(task,TNB_ACTIVATION_OFFERUSED, m_tnbOfferUsed, subscriber.getLanguage()));
				return;
			}
			task.setObject(param_subclass, offers[0].getSrvKey()); // pass subscriptionClass
			task.setObject(param_offerID, offers[0].getOfferID());
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
		}
		
		super.processTNBActivation(task);	
	}

}
