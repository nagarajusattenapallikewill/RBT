package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.du;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class DuSmsProcessor extends SmsProcessor{

	public DuSmsProcessor() throws RBTException {
		super();
	}
	
	
	public void processTNBActivation(Task task) {
		logger.debug("Processing TNB Activation");
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		
		if (!status.equalsIgnoreCase(WebServiceConstants.NEW_USER) && !status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
		{
			//Send sms informing existing subscription
			task.setObject(param_responseSms, getSMSTextForID(task,TNB_ACTIVATION_EXISTINGUSER, m_tnbExistingUser, subscriber.getLanguage()));
			return;
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		String promoId = (smsList.size() >= 1 ? smsList.get(0) : null);
		task.setObject(param_PROMO_ID, promoId);
		
		String subscriptionClass = null;
		Parameters param = parameterCacheManager.getParameter("SMS", "TNB_OFFER_SUBSCRIPTION_CLASS", null);
		if(param != null) {
			subscriptionClass = param.getValue();
		}
		
		String userType = subscriber.isPrepaid()?"p":"b";
		try {
			Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
					subscriber.getSubscriberID(), "TNB",
					Offer.OFFER_TYPE_SUBSCRIPTION, userType, subscriptionClass, null);
			if (offers == null || offers.length == 0)
			{
				task.setObject(param_responseSms, getSMSTextForID(task,TNB_ACTIVATION_OFFERUSED, m_tnbOfferUsed, subscriber.getLanguage()));
				return;
			}
			task.setObject(param_offerID, offers[0].getOfferID());
		} catch (RBTException e) {
			logger.error(e.getMessage(), e);
			task.setObject(param_responseSms, getSMSTextForID(task,TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber.getLanguage()));
			return;
		}
		
		task.setObject(param_subclass, subscriptionClass);
		task.setObject(param_alreadyGetBaseOffer, true);
		
		
		//calling selection offer, if smsText has song promo code
		if (promoId != null) {
			
			try {
				Offer[] offers = RBTSMClientHandler.getInstance().getOffer(
						subscriber.getSubscriberID(), "TNB",
						Offer.OFFER_TYPE_SELECTION, userType, null, null);
				if (offers == null || offers.length == 0)
				{
					task.setObject(param_responseSms, getSMSTextForID(task,TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber.getLanguage()));
					return;
				}
				String chargeClass = offers[0].getSrvKey();
				String selOfferId = offers[0].getOfferID();
				task.setObject(param_sel_offerID, selOfferId);				
				task.setObject(param_chargeclass, chargeClass);
				task.setObject(param_USE_UI_CHARGE_CLASS, true);
				task.setObject(param_alreadyGetSelOffer, true);
			} catch (RBTException e) {
				logger.error(e.getMessage(), e);
				task.setObject(param_responseSms, getSMSTextForID(task,TECHNICAL_FAILURE, m_technicalFailuresDefault, subscriber.getLanguage()));
				return;
			}			
		}
		logger.debug("Task ::::: " + task);
		super.processTNBActivation(task);
	}
}
